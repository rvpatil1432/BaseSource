package ibase.webitm.ejb.dis.adv;

import java.util.*;
import java.sql.*;
import java.rmi.RemoteException;
import java.text.*;

import javax.ejb.SessionContext;
import javax.ejb.CreateException;
//import javax.ejb.SessionBean;
import ibase.system.config.ConnDriver;
import ibase.utility.CommonConstants;
import ibase.utility.UserInfoBean;
import ibase.webitm.servlet.RSItemChange;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.utility.TransIDGenerator;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.utility.*;

import ibase.utility.*;
import ibase.webitm.ejb.*;
import ibase.system.config.*;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3

public class InvHoldRelConf extends ActionHandlerEJB implements InvHoldRelConfLocal , InvHoldRelConfRemote  //SessionBean
{
	/* public void ejbCreate() throws RemoteException,CreateException{
    }
    public void ejbRemove(){

    }
    public void ejbActivate(){

    }
    public void ejbPassivate(){


    }
    public void setSessionContext(SessionContext se){

    }*/
	public String confirm(String tranID,String xtraParams, String forcedFlag) throws RemoteException,ITMException
	{

		String retString = "",errCode = "",confirmed = "";
		Connection conn = null;
		ConnDriver connDriver = new ConnDriver();		
		boolean isError = false;
		ResultSet rs = null;
		PreparedStatement pStmt = null;
		String actionSet = "";
		boolean isConn= true;
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;

			retString =this.confirm( tranID,xtraParams, forcedFlag ,conn ,isConn);

			if ( retString != null && retString.length() > 0 )
			{
				throw new Exception("Exception while calling confirm for tran  Id:["+tranID+"]");
			}

		}

		catch(Exception exception)
		{
			System.out.println("Exception in [InvHoldConfEJB] getCurrdateAppFormat " + exception.getMessage());
		}
		return retString;
	}

	public String confirm(String tranId,String xtraParams, String forcedFlag ,Connection conn,boolean isConn) throws RemoteException,ITMException
	{
		//        Connection conn = null;
		PreparedStatement pstmt = null;

		ResultSet rs = null;
		String sql = "";
		//        ConnDriver connDriver = null;
		String loginEmpCode = null;
		ibase.utility.E12GenericUtility genericUtility= null;

		Document dom = null;
		int count=0;
		ITMDBAccessEJB itmDBAccessEJB = null;
		String errString = null;
		int upd = 0;
		int lineNo= 0;
		//changed by Dharmesh on 12-08-2011 [WM1ESUN004].start

		String itemCode = null;
		String siteCode = null;
		String locCode = null;
		String lotNo = null;
		String lotSl = null;
		String lineNoSl = null;
		String tranIdTrace = null;
		String tranIdHold = null;
		//String lineNoHold = null;

		double holdQtyStock = 0.0;
		double qtyPerArt = 0.0;
		double holdQtyTrace = 0.0;
		double totalHoldStock = 0.0;
		double relQtyTrace = 0.0;
		double holdQty = 0.0;
		double stockQty = 0.0;
		double totalHoldTrace = 0.0;
		double totalRelTrace = 0.0;
		double balQtyHold = 0.0;
		double qtyToHold = 0.0;
		double relQty = 0.0;

		int lineNoHold = 0;

		String sqlUpdate = null;
		String sqlHoldDet = null;
		String sqlSelStck = null;
		String sqlSelTrace = null;
		String sqlInsert = null;
		String sqlDetail = null;
		String lockCode = null;

		PreparedStatement pUpdate = null;
		PreparedStatement pHoldDet = null;
		PreparedStatement pSelStock = null;
		PreparedStatement pSelTrace = null;
		PreparedStatement pInsert = null;
		PreparedStatement pDetail = null;

		ResultSet rsHoldDet = null;
		ResultSet rsSelStck = null;
		ResultSet rsSelTrace = null;
		ResultSet rsDetail = null;
		String holdStatus = ""; // 14/04/14 manoharan 

		//changed by Dharmesh on 12-08-2011 [WM1ESUN004].end


		try
		{
			itmDBAccessEJB = new ITMDBAccessEJB();
			genericUtility = new ibase.utility.E12GenericUtility();		

			//            connDriver = new ConnDriver();
			//            //Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			//            conn.setAutoCommit(false);

			loginEmpCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			//Changed by sumit on 02/10/12 getting loginempCode in case of null start.
			if( loginEmpCode.trim().length() == 0 || loginEmpCode == null)
			{
				loginEmpCode = "SYSTEM";
			}
			//Changed by sumit on 02/10/12 getting loginempCode in case of null end.
			//check if there is record in detal
			int countDet = 0;

			//changed by Dharmesh on 11-08-2011 [WM1ESUN004]  to bind variable dynamically instead of statically.start
			/*
			sql = " select count( 1 ) cnt from inv_hold_rel_det where tran_id = '" + tranId.trim() + "' ";
			pstmt = conn.prepareStatement( sql );
			rs = pstmt.executeQuery();
			 */
			sql = "select count( 1 ) cnt from inv_hold_rel_det where tran_id = ?" ;
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,tranId);
			rs = pstmt.executeQuery();
			//changed by Dharmesh on 11-08-2011 [WM1ESUN004]  to bind variable dynamically instead of statically.end

			if( rs.next() )
			{
				countDet = rs.getInt( "cnt" );
			}

			pstmt.close();
			pstmt = null;				
			rs.close();
			rs = null;

			if( countDet == 0 )
			{
				errString = itmDBAccessEJB.getErrorString("","VTNODET","","",conn);
			}

			if( errString == null || errString.length() == 0 )
			{
				//changed by Dharmesh on 11-08-2011 [WM1ESUN004]  to bind variable dynamically instead of statically.start
				/*
				sql = " select count( 1 ) cnt from inv_hold_rel where tran_id = '" + tranId.trim() + "' AND CONFIRMED = 'Y' ";	
				pstmt = conn.prepareStatement( sql );
				rs = pstmt.executeQuery();
				 */
				sql = "select count( 1 ) cnt from inv_hold_rel where tran_id = ? AND CONFIRMED = 'Y'";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,tranId);
				rs = pstmt.executeQuery();
				//changed by Dharmesh on 11-08-2011 [WM1ESUN004]  to bind variable dynamically instead of statically.end
				if( rs.next() )
				{
					countDet = rs.getInt( "cnt" );
				}

				pstmt.close();
				pstmt = null;				
				rs.close();
				rs = null;

				if( countDet > 0 )
				{
					errString = itmDBAccessEJB.getErrorString("","VTCONFMD","","",conn);
				}

				//changed by Dharmesh on 12-08-2011 [WM1ESUN004] to commit unconfirmed transaction.start
				else
				{
					sqlUpdate = "UPDATE STOCK SET HOLD_QTY = ? WHERE ITEM_CODE = ? AND SITE_CODE = ? AND LOC_CODE = ? AND LOT_NO = ? AND LOT_SL = ?";
					pUpdate = conn.prepareStatement(sqlUpdate);

					sqlSelStck = "SELECT ITEM_CODE, SITE_CODE, LOC_CODE, LOT_NO, LOT_SL, QTY_PER_ART, HOLD_QTY FROM STOCK WHERE ITEM_CODE = ? AND SITE_CODE = ? AND LOC_CODE = ? AND LOT_NO = ? AND LOT_SL = ? " ;
					pSelStock = conn.prepareStatement(sqlSelStck);

					sqlSelTrace = "SELECT SUM( case when HOLD_QTY is null then 0 else HOLD_QTY end  ) AS HOLD_QTY_TRACE, SUM( case when REL_QTY is null then 0 else REL_QTY end  ) AS REL_QTY_TRACE FROM INV_HOLD_REL_TRACE WHERE ITEM_CODE = ? AND SITE_CODE = ? AND LOC_CODE = ? AND LOT_NO = ? AND LOT_SL = ? " ;
					pSelTrace = conn.prepareStatement(sqlSelTrace);

					//sqlInsert = " INSERT INTO INV_HOLD_REL_TRACE ( TRAN_ID, ITEM_CODE, SITE_CODE, LOC_CODE, LOT_NO, LOT_SL, REF_NO, REL_QTY, LOCK_CODE ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ? ) " ; // comment added by sagar on 23/07/15
					sqlInsert = " INSERT INTO INV_HOLD_REL_TRACE ( TRAN_ID, ITEM_CODE, SITE_CODE, LOC_CODE, LOT_NO, LOT_SL, REF_NO, REL_QTY, LOCK_CODE, REF_LINE ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) " ; // REF_LINE added by sagar on 23/07/15
					pInsert = conn.prepareStatement(sqlInsert);
					// 14/04/14 manoharan hold_status added it should not be R
					sqlHoldDet = "SELECT ITEM_CODE, SITE_CODE, LOC_CODE, LOT_NO, LOT_SL, LINE_NO_SL, HOLD_STATUS FROM INV_HOLD_DET WHERE TRAN_ID = ? AND LINE_NO = ?" ;
					pHoldDet = conn.prepareStatement(sqlHoldDet);

					/*sqlDetail = " SELECT HOLDDET.TRAN_ID__HOLD, HOLDDET.LINE_NO__HOLD, IHOLD.LOCK_CODE "+
								" FROM INV_HOLD_REL_DET HOLDDET, INV_HOLD IHOLD "+
								" WHERE HOLDDET.TRAN_ID__HOLD = IHOLD.TRAN_ID AND HOLDDET.TRAN_ID = ? ";*/ // comment added by sagar on 27/07/15
					sqlDetail = " SELECT HOLDDET.TRAN_ID__HOLD, HOLDDET.LINE_NO__HOLD, IHOLD.LOCK_CODE, HOLDDET.LINE_NO "+
					" FROM INV_HOLD_REL_DET HOLDDET, INV_HOLD IHOLD "+
					" WHERE HOLDDET.TRAN_ID__HOLD = IHOLD.TRAN_ID AND HOLDDET.TRAN_ID = ? "; // LINE_NO added by sagar on 27/07/15
					pDetail = conn.prepareStatement( sqlDetail );
					pDetail.setString( 1,tranId );
					rsDetail = pDetail.executeQuery();
					pDetail.clearParameters();

					while( rsDetail.next() )
					{
						tranIdHold	= rsDetail.getString("TRAN_ID__HOLD");
						lineNoHold	= rsDetail.getInt("LINE_NO__HOLD");
						lockCode	= rsDetail.getString("LOCK_CODE");
						lineNo	= rsDetail.getInt("LINE_NO");
						System.out.println("tranIdHold =[" + tranIdHold + "]");
						System.out.println("lineNoHold =[" + lineNoHold + "]");
						System.out.println("lockCode =[" + lockCode + "]");
						System.out.println(">>>>Release Line No:"+ lineNo);
						pHoldDet.setString( 1,tranIdHold );
						pHoldDet.setInt( 2,lineNoHold );

						rsHoldDet = pHoldDet.executeQuery();
						pHoldDet.clearParameters();

						while( rsHoldDet.next() )
						{
							itemCode = checkNull(rsHoldDet.getString("ITEM_CODE"));
							siteCode = checkNull(rsHoldDet.getString("SITE_CODE"));
							locCode = checkNull(rsHoldDet.getString("LOC_CODE"));
							lotNo = checkNull(rsHoldDet.getString("LOT_NO"));
							lotSl = checkNull(rsHoldDet.getString("LOT_SL"));
							lineNoSl = checkNull(rsHoldDet.getString("LINE_NO_SL"));
							// 14/04/14 manoharan if hold_status = "R" the give error
							holdStatus = checkNull(rsHoldDet.getString("HOLD_STATUS"));
							if ("R".equals(holdStatus))
							{
								errString = itmDBAccessEJB.getErrorString("","VTHOLDST","","",conn);
								break;
							}
							// end 14/04/14 manoharan if hold_status = "R" the give error
							if( lineNoSl != null && lineNoSl.trim().length() > 0 ) //i.e. Line_sl is found
							{
								//tranIdTrace = generateTranId("w_inv_hold_rel_trace", conn);
								tranIdTrace = generateTranId("w_inv_hold_rel_trace",siteCode, conn); // CHANGED  BY RITESH ON 13/MAR/14

								pSelStock.setString( 1, itemCode );
								pSelStock.setString( 2, siteCode );
								pSelStock.setString( 3, locCode );
								pSelStock.setString( 4, lotNo );
								pSelStock.setString( 5, lotSl );

								rsSelStck = pSelStock.executeQuery();
								pSelStock.clearParameters();

								if( rsSelStck.next() ) 
								{
									qtyPerArt = rsSelStck.getDouble("QTY_PER_ART");	
									holdQtyStock = rsSelStck.getDouble("HOLD_QTY");
								}

								if( rsSelStck != null )
								{
									rsSelStck.close();
									rsSelStck = null;
								}

								pSelTrace.setString( 1, itemCode );
								pSelTrace.setString( 2, siteCode );
								pSelTrace.setString( 3, locCode );
								pSelTrace.setString( 4, lotNo );
								pSelTrace.setString( 5, lotSl );
								rsSelTrace = pSelTrace.executeQuery();

								pSelTrace.clearParameters();

								if( rsSelTrace.next() ) 
								{
									holdQtyTrace = rsSelTrace.getDouble("HOLD_QTY_TRACE");
									relQtyTrace = rsSelTrace.getDouble("REL_QTY_TRACE");
								}

								if(rsSelTrace != null)
								{
									rsSelTrace.close();
									rsSelTrace = null;
								}

								balQtyHold = holdQtyTrace - ( Math.abs(relQtyTrace) + qtyPerArt );

								if( balQtyHold == 0 )
								{	
									pUpdate.setDouble( 1, balQtyHold);
									pUpdate.setString( 2, itemCode);
									pUpdate.setString( 3, siteCode);
									pUpdate.setString( 4, locCode);
									pUpdate.setString( 5, lotNo);
									pUpdate.setString( 6, lotSl);
									pUpdate.executeUpdate();
									pUpdate.clearParameters();
								}
								else if ( balQtyHold < holdQtyStock )
								{
									balQtyHold = holdQtyStock - balQtyHold ;
									pUpdate.setDouble( 1, balQtyHold);
									pUpdate.setString( 2, itemCode);
									pUpdate.setString( 3, siteCode);
									pUpdate.setString( 4, locCode);
									pUpdate.setString( 5, lotNo);
									pUpdate.setString( 6, lotSl);
									pUpdate.executeUpdate();
									pUpdate.clearParameters();
								}
								System.out.println(">>>>In if line_no_sl is not null then lineNo:"+ lineNo);
								pInsert.setString( 1, tranIdTrace);
								pInsert.setString( 2, itemCode ); 
								pInsert.setString( 3, siteCode );
								pInsert.setString( 4, locCode );
								pInsert.setString( 5, lotNo );
								pInsert.setString( 6, lotSl );
								pInsert.setString( 7, tranId );
								pInsert.setDouble( 8, (-qtyPerArt) );
								pInsert.setString( 9, lockCode );
								pInsert.setInt( 10, lineNo ); 

								pInsert.executeUpdate();
								pInsert.clearParameters();

							}//end of Line_sl present
							else //i.e. line_sl is not found
							{
								HashMap pkKeyValMap = new HashMap();	

								// Changed by Manish on 30/09/15 for Ms Sql Server.
								StringBuffer sb ;
								String DB = CommonConstants.DB_NAME;
								System.out.println("DB  ==========>>>>>"+DB);
								if("mssql".equalsIgnoreCase(DB))
								{
									sb = new StringBuffer("SELECT S.ITEM_CODE, S.SITE_CODE, S.LOC_CODE, S.LOT_NO, S.LOT_SL, S.QUANTITY, S.HOLD_QTY, S.QTY_PER_ART FROM STOCK S ");//change on 09/jun/14 compare with inv_hold_rel_trace table
									sb.append(" inner join (select ITEM_CODE,SITE_CODE,LOC_CODE,LOT_NO,LOT_SL from  inv_hold_rel_trace where ref_no  ='"+tranIdHold+"') A on (S.ITEM_CODE = A.ITEM_CODE AND S.SITE_CODE = A.SITE_CODE AND S.LOC_CODE = A.LOC_CODE AND S.LOT_NO = A.LOT_NO AND S.LOT_SL = A.LOT_SL ) ") ;
									sb.append(" where S.QUANTITY > 0 and S.HOLD_QTY > 0  and ") ;
								}
								else
								{
									sb = new StringBuffer("SELECT ITEM_CODE,SITE_CODE,LOC_CODE,LOT_NO,LOT_SL,QUANTITY, HOLD_QTY, QTY_PER_ART FROM STOCK WHERE ");//change on 09/jun/14 compare with inv_hold_rel_trace table
									sb.append(" ( ITEM_CODE,SITE_CODE,LOC_CODE,LOT_NO,LOT_SL ) in (select ITEM_CODE,SITE_CODE,LOC_CODE,LOT_NO,LOT_SL from  inv_hold_rel_trace where ref_no  ='"+tranIdHold+"') and STOCK.QUANTITY > 0 and STOCK.HOLD_QTY > 0  and ") ;//ADDED BY KUNAL ON 27/MAY/14 add extra condition for data filter
								}
								// Changed by Manish on 30/09/15 for Ms Sql Server.

								ArrayList pkList = new ArrayList();

								if ( itemCode != null && itemCode.length() > 0 )
								{
									pkKeyValMap.put("ITEM_CODE", itemCode);
									pkList.add("ITEM_CODE");
								}
								if ( siteCode != null && siteCode.length() > 0 )
								{
									pkKeyValMap.put("SITE_CODE", siteCode);
									pkList.add("SITE_CODE");
								}
								if ( locCode != null && locCode.length() > 0 )
								{
									pkKeyValMap.put("LOC_CODE", locCode);
									pkList.add("LOC_CODE");
								}
								if ( lotNo != null && lotNo.length() > 0 )
								{
									pkKeyValMap.put("LOT_NO", lotNo);
									pkList.add("LOT_NO");
								}
								if ( lotSl != null && lotSl.length() > 0 )
								{
									pkKeyValMap.put("LOT_SL", lotSl);
									pkList.add("LOT_SL");
								}
								System.out.println("pkKeyValMap :"+pkKeyValMap);

								String key = "";
								for ( int i = 0; i < pkList.size(); i++ )
								{
									key = pkList.get(i).toString();
									
									if("mssql".equalsIgnoreCase(DB))
									{
										sb.append("S.");             // S added by manish for mssql
									}
									
									sb.append(key).append(" = ? and ");
								}
								String stockQuery = sb.toString();
								stockQuery = stockQuery.substring(0,(stockQuery.length() - 4));
								System.out.println("stockQuery :"+stockQuery);
								sb = null;
								PreparedStatement pstmtStock = null;
								ResultSet rsStock = null;
								pstmtStock = conn.prepareStatement(stockQuery);
								for ( int i = 0; i < pkList.size(); i++ )
								{
									key = pkList.get(i).toString();
									if ( pkKeyValMap.containsKey(key) )
									{
										System.out.println("Key =["+key+ "] and Value =["+pkKeyValMap.get(key)+"]");
										pstmtStock.setString(i+1, pkKeyValMap.get(key).toString());
									}
								}
								/*
								pSelTrace.setString( 1, itemCode );
								pSelTrace.setString( 2, siteCode );
								pSelTrace.setString( 3, locCode );
								pSelTrace.setString( 4, lotNo );
								pSelTrace.setString( 5, lotSl );
								rsSelTrace = pSelTrace.executeQuery();
								pSelTrace.clearParameters();

								if(rsSelTrace.next())
								{
									holdQtyTrace = rsSelTrace.getDouble("HOLD_QTY_TRACE");
									relQtyTrace = rsSelTrace.getDouble("REL_QTY_TRACE");
								}

								if( rsSelTrace != null)
								{
									rsSelTrace.close();
									rsSelTrace = null;
								}
								 */
								rsStock = pstmtStock.executeQuery();
								pstmtStock.clearParameters();

								while( rsStock.next() )
								{
									//tranIdTrace = generateTranId("w_inv_hold_rel_trace", conn);
									tranIdTrace = generateTranId("w_inv_hold_rel_trace",siteCode, conn); // CHANGED  BY RITESH ON 13/MAR/14
									itemCode = checkNull(rsStock.getString("ITEM_CODE"));
									siteCode = checkNull(rsStock.getString("SITE_CODE"));
									locCode = checkNull(rsStock.getString("LOC_CODE"));
									lotNo = checkNull(rsStock.getString("LOT_NO"));
									lotSl = checkNull(rsStock.getString("LOT_SL"));
									holdQtyStock = rsStock.getDouble("HOLD_QTY");
									stockQty = rsStock.getDouble("QUANTITY");

									System.out.println("Quantity =[" + stockQty + "]");
									System.out.println("holdQtyStock =[" + holdQtyStock + "]");

									//Selecting sum(hold_qty) and sum(rel_qty) from trace table
									pSelTrace.setString( 1, itemCode );
									pSelTrace.setString( 2, siteCode );
									pSelTrace.setString( 3, locCode );
									pSelTrace.setString( 4, lotNo );
									pSelTrace.setString( 5, lotSl );
									rsSelTrace = pSelTrace.executeQuery();
									pSelTrace.clearParameters();

									if(rsSelTrace.next())
									{
										holdQtyTrace = rsSelTrace.getDouble("HOLD_QTY_TRACE");
										relQtyTrace = rsSelTrace.getDouble("REL_QTY_TRACE");
									}
									rsSelTrace.close();
									rsSelTrace = null;

									//qtyToHold = totalHoldTrace - (totalRelTrace + relQtyTrace) ;

									System.out.println("holdQtyTrace =[" + holdQtyTrace + "]");
									System.out.println("relQtyTrace =[" + relQtyTrace + "]");
									balQtyHold = holdQtyTrace - ( Math.abs(relQtyTrace) + holdQtyStock ) ;

									System.out.println("balQtyHold =[" + balQtyHold + "]");

									if( balQtyHold == 0 )
									{
										//holdQtyStock = holdQtyStock - relQtyTrace ;
										pUpdate.setDouble( 1, balQtyHold );
										pUpdate.setString( 2, itemCode);
										pUpdate.setString( 3, siteCode);
										pUpdate.setString( 4, locCode);
										pUpdate.setString( 5, lotNo);
										pUpdate.setString( 6, lotSl);
										pUpdate.executeUpdate();
										pUpdate.clearParameters();
									}
									else if ( balQtyHold < holdQtyStock )
									{
										balQtyHold = holdQtyStock - balQtyHold;
										pUpdate.setDouble( 1, balQtyHold );
										pUpdate.setString( 2, itemCode);
										pUpdate.setString( 3, siteCode);
										pUpdate.setString( 4, locCode);
										pUpdate.setString( 5, lotNo);
										pUpdate.setString( 6, lotSl);
										pUpdate.executeUpdate();
										pUpdate.clearParameters();
									}
									System.out.println(">>>>In else line_no_sl is null then lineNo:"+ lineNo);
									pInsert.setString( 1, tranIdTrace );
									pInsert.setString( 2, itemCode );
									pInsert.setString( 3, siteCode );
									pInsert.setString( 4, locCode );
									pInsert.setString( 5, lotNo );
									pInsert.setString( 6, lotSl );
									pInsert.setString( 7, tranId );
									pInsert.setDouble( 8, (-holdQtyStock) );
									pInsert.setString( 9, lockCode ); 
									pInsert.setInt( 10, lineNo ); 
									pInsert.executeUpdate();
									pInsert.clearParameters();
								}
								pkList = null;
								if(rsStock != null)
								{
									rsStock.close();
									rsStock = null;
								}
							}//end of else

							pHoldDet.clearParameters();
						}//end of outer while()
						if(rsHoldDet != null)
						{
							rsHoldDet.close();
							rsHoldDet = null;
						}
						// 14/04/14 manoharan
						if( errString != null && errString.trim().length() > 0 )
						{
							break;
						}
					}//while( rsDetail.next() )
					if(rsDetail != null)
					{
						rsDetail.close();
						rsDetail = null;
					}
					/*// 14/04/14 manoharan
					if( errString != null || errString.trim().length() > 0 )
					{
						break;
					}*/

				}				
				//changed by Dharmesh on 12-08-2011 [WM1ESUN004] to commit unconfirmed transaction.end
			}
			//

			if( errString == null || errString.length() == 0 )
			{
				//changed by Dharmesh on 11-08-2011 [WM1ESUN004]  to bind variable dynamically instead of statically.start
				/*
				sql = "update inv_hold_rel set confirmed = 'Y', emp_code__aprv = '" + loginEmpCode + "', CONF_DATE = ?  where tran_id = '" + tranId + "'";
				pstmt = conn.prepareStatement( sql );
				pstmt.setTimestamp( 1, new java.sql.Timestamp( System.currentTimeMillis() ) );
				pstmt.executeUpdate();
				 */
				sql = "update inv_hold_rel set confirmed = 'Y', emp_code__aprv = ? , CONF_DATE = ?  where tran_id = ?" ;
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,loginEmpCode);
				pstmt.setTimestamp( 2 , new java.sql.Timestamp( System.currentTimeMillis() ) );
				pstmt.setString(3,tranId);
				pstmt.executeUpdate();
				//changed by Dharmesh on 11-08-2011 [WM1ESUN004]  to bind variable dynamically instead of statically.end

				pstmt.close();
				pstmt = null;

				//changed by Dharmesh on 11-08-2011 [WM1ESUN004]  to bind variable dynamically instead of statically.start
				/*
				sql = " update inv_hold_det hd set "
					+"	hd.hold_status = 'R', "
					+"	hd.STATUS_DATE = ? " 
					+" where (hd.tran_id, hd.line_no ) "
					+"	in ( select rd.tran_id__hold, rd.line_no__hold "
					+"			from inv_hold_rel_det rd "
					+"		 where tran_id = '" + tranId + "' ) ";

				pstmt = conn.prepareStatement( sql );
				pstmt.setTimestamp( 1, new java.sql.Timestamp( System.currentTimeMillis() ) );
				pstmt.executeUpdate();
				 */

				//Changed by Manish on 16/09/2015 for Ms Sql Server.
				String DB = CommonConstants.DB_NAME;

				if("mssql".equalsIgnoreCase(DB))
				{
					sql = " update inv_hold_det set "
						+"	hold_status = 'R', "
						+"	STATUS_DATE = ? " 
						+" from inv_hold_det hd "
						+"	inner join ( select rd.tran_id__hold, rd.line_no__hold "
						+"	from inv_hold_rel_det rd "
						+"where tran_id = ? ) A on (hd.tran_id = A.TRAN_ID__HOLD and hd.line_no = A.line_no__hold)";
				}

				else
				{
					sql = " update inv_hold_det hd set "
						+"	hd.hold_status = 'R', "
						+"	hd.STATUS_DATE = ? " 
						+" where (hd.tran_id, hd.line_no ) "
						+"	in ( select rd.tran_id__hold, rd.line_no__hold "
						+"			from inv_hold_rel_det rd "
						+"		 where tran_id = ? )";
				}
				//Changed by Manish on 16/09/2015 for Ms Sql Server.

				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp( 1 , new java.sql.Timestamp( System.currentTimeMillis() ) );
				pstmt.setString(2,tranId);
				pstmt.executeUpdate();
				//changed by Dharmesh on 11-08-2011 [WM1ESUN004]  to bind variable dynamically instead of statically.end

				pstmt.close();
				pstmt = null;				

			}
			if( errString != null && errString.length() > 0 )
			{
				System.out.println("Returning Result "+errString);
				conn.rollback();
				return errString;
			}
			else
			{	// added by shamim
				if(isConn)
				{
					conn.commit();
				}
				errString = itmDBAccessEJB.getErrorString("","VTCNFSUCC","","",conn);
				System.out.println("Returnng String From InvHoldRelConfEJB : .." + errString);
			}
			System.out.println("errString : "+errString);
		}catch(ITMException ie)
		{
			System.out.println("ITMException : "+ie);
			try{
				conn.rollback();
			}catch(Exception t){}
			ie.printStackTrace();
			errString = itmDBAccessEJB.getErrorString("","VTDESNCONF","","",conn);
			System.out.println("Returnng String From InvHoldRelConfEJB :"+errString);
			return errString;
		}catch(Exception e){
			System.out.println("Returnng String From InvHoldRelConfEJB :"+e);
			try{
				conn.rollback();
			}catch(Exception t){}
			e.printStackTrace();
			errString = itmDBAccessEJB.getErrorString("","VTDESNCONF","","",conn);
			System.out.println("Returnng String From InvHoldRelConfEJB :"+errString);
			return errString;
		}
		finally
		{
			try{
				if( pstmt != null )
				{
					pstmt.close();
				}
				pstmt = null;
				//changed by Dharmesh on 12-08-2011 [WM1ESUN004]  to close preparedStatements and to make null.start 
				if( pInsert != null )
				{
					pInsert.close();
					pInsert = null;
				}
				if(pHoldDet != null)
				{
					pHoldDet.close();
					pHoldDet = null;
				}
				if(pSelStock != null)
				{
					pSelStock.close();
					pSelStock = null;
				}
				if(pUpdate != null)
				{
					pUpdate.close();
					pUpdate = null;
				}				
				if(pDetail != null)
				{
					pDetail.close();
					pDetail = null;
				}

				//changed by Dharmesh on 12-08-2011 [WM1ESUN004]  to close preparedStatements and to make null.end

				if(conn != null && isConn ==true)
				{
					conn.close();
					conn = null;
				}

			}catch(Exception e){System.out.println("Exception : "+e);e.printStackTrace();}
		}
		System.out.println("InvHoldRelConfEJB :"+errString);
		return errString;
	}

	//changed by Dharmesh on 12-08-2011 [WM1ESUN004] to auto genrate Transacion Id for inv_hold_rel_trace.start
	// METHOD ARGS CHANGED BY RITESH ON 13/MAR/14
	public String generateTranId(String windowName,String siteCode,Connection conn)throws ITMException
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

		// ADDED  BY RITESH ON 13/MAR/14 START
		java.sql.Timestamp currDate = null;
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();

		try
		{
			SimpleDateFormat sdfAppl = new SimpleDateFormat(genericUtility.getApplDateFormat());

			currDate = new java.sql.Timestamp(System.currentTimeMillis());
			String currDateStr = sdfAppl.format(currDate);
			// ADDED  BY RITESH ON 13/MAR/14 END

			selSql = "SELECT KEY_STRING, TRAN_ID_COL, REF_SER FROM TRANSETUP WHERE TRAN_WINDOW = ? ";
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
			xmlValues = xmlValues + "<tran_id></tran_id>";
			xmlValues = xmlValues +        "<site_code>" + siteCode + "</site_code>";   // ADDED  BY RITESH ON 13/MAR/14
			xmlValues = xmlValues +        "<tran_date>" + currDateStr + "</tran_date>"; // ADDED  BY RITESH ON 13/MAR/14
			xmlValues = xmlValues + "</Detail1></Root>";
			System.out.println("xmlValues  :["+xmlValues+"]");
			TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", CommonConstants.DB_NAME);
			tranId = tg.generateTranSeqID(tranSer, keyCol, keyString, conn);
			System.out.println("tranId NEWLY WT SITECODE & DATE :"+tranId);;
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
	//changed by Dharmesh on 12-08-2011 [WM1ESUN004] to auto genrate Transacion Id for inv_hold_rel_trace.end
	private String checkNull( String inputVal )
	{
		if ( inputVal == null )
		{
			inputVal = "";
		}
		return inputVal;
	}
}
