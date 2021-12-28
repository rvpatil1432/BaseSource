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

public class InvHoldConf extends ActionHandlerEJB implements InvHoldConfLocal ,InvHoldConfRemote  //SessionBean
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
	
    public String confirm(String tranId,String xtraParams, String forcedFlag ,Connection conn ,boolean conStat) throws RemoteException,ITMException
    {
        //Connection conn = null;
        PreparedStatement pstmt = null;

        ResultSet rs = null;
        String sql = "",lockLevel="";
        ConnDriver connDriver = null;
		String loginEmpCode = null;

        ibase.utility.E12GenericUtility genericUtility= null;

		Document dom = null;
		int count=0;
		ITMDBAccessEJB itmDBAccessEJB = null;
		String errString = null;
		int upd = 0;
		boolean conStatus = conStat;
		
		//changed by Dharmesh on 12-08-2011 [WM1ESUN004].start
		String itemCode = null;
		String siteCode = null;
		String locCode = null;
		String lotNo = null;
		String lotSl = null;
		String lineNoSl = null;
		String tranIdTrace = null;
		String sqlUpdate = null;
		String sqlSelStck = null;
		String sqlInsert = null;
		String sqlHoldDet = null;
		String sqlNoSl = null;
		String lockCode = null;
		String lineNo="";
		int lineNoIn=0;
		double holdQty = 0.0;
		double qtyPerArt = 0.0;
		double stockQty = 0.0;
		
		PreparedStatement pUpdate = null;
		PreparedStatement pSelStck = null;
		PreparedStatement pstmtInsert = null;
		PreparedStatement pHoldDet = null;
		
		
		ResultSet rsHoldDet = null;
		ResultSet rsSelStck = null;
		//ResultSet rsNoSl = null;
		//changed by Dharmesh on 12-08-2011 [WM1ESUN004].end
		
        try
		{
            itmDBAccessEJB = new ITMDBAccessEJB();
			genericUtility = new ibase.utility.E12GenericUtility();

			/*  
			connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			*/
			
			loginEmpCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			//check if there is record in detal
			int countDet = 0;
			
			//changed by Dharmesh on 11-08-2011 [WM1ESUN004]  to bind variable dynamically instead of statically.start
			/*
			sql = " select count( 1 ) cnt from inv_hold_det where tran_id = '" + tranId.trim() + "' ";
			pstmt = conn.prepareStatement( sql );
			rs = pstmt.executeQuery();
			*/
			sql = "select count( 1 ) cnt from inv_hold_det where tran_id = ?" ;
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
				sql = " select count( 1 ) cnt from inv_hold where tran_id = '" + tranId.trim() + "' AND CONFIRMED = 'Y' ";
				pstmt = conn.prepareStatement( sql );
				rs = pstmt.executeQuery();
				*/
				sql = "select count( 1 ) cnt from inv_hold where tran_id = ? AND CONFIRMED = 'Y' " ;
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
					sql = "SELECT LOCK_CODE FROM INV_HOLD WHERE TRAN_ID = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,tranId);
					rs = pstmt.executeQuery();
					if ( rs.next() )
					{
						lockCode = rs.getString("LOCK_CODE");
					}
					rs.close(); rs = null;
					pstmt.close(); pstmt = null;
					/*------changes on 18-NOV-14 by mahendra-----------*/
					System.out.println("lockCode :"+lockCode);
					
					sql = "select lock_level from inv_lock where lock_code= ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,lockCode);
					rs = pstmt.executeQuery();
					if ( rs.next() )
					{
						lockLevel = rs.getString("lock_level");
					}
					rs.close(); rs = null;
					pstmt.close(); pstmt = null;
					
					System.out.println("lockLevel : "+lockLevel);
					
					
					sqlUpdate = "UPDATE STOCK SET HOLD_QTY = ? WHERE ITEM_CODE = ? AND SITE_CODE = ? AND LOC_CODE = ? AND LOT_NO = ? AND LOT_SL = ? ";
					//pUpdate = conn.prepareStatement ( sqlUpdate );by chandrashekar on 17-11-14
					
					sqlSelStck = "SELECT QUANTITY, QTY_PER_ART, HOLD_QTY FROM STOCK WHERE ITEM_CODE = ? AND SITE_CODE = ? AND LOC_CODE = ? AND LOT_NO = ? AND LOT_SL = ? " ;
					//pSelStck = conn.prepareStatement( sqlSelStck );by chandrashekar on 17-11-14
					
					//sqlInsert = " INSERT INTO INV_HOLD_REL_TRACE ( TRAN_ID, ITEM_CODE, SITE_CODE, LOC_CODE, LOT_NO, LOT_SL, REF_NO, HOLD_QTY, LOCK_CODE ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ? ) " ; //comment added by sagar on 23/07/15
					sqlInsert = " INSERT INTO INV_HOLD_REL_TRACE ( TRAN_ID, ITEM_CODE, SITE_CODE, LOC_CODE, LOT_NO, LOT_SL, REF_NO, HOLD_QTY, LOCK_CODE, REF_LINE ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) " ; // REF_LINE added by sagar on 23/07/15
					//pstmtInsert = conn.prepareStatement(sqlInsert);by chandrashekar on 17-11-14
					
					//sqlHoldDet = "SELECT ITEM_CODE, SITE_CODE, LOC_CODE, LOT_NO, LOT_SL, LINE_NO_SL FROM INV_HOLD_DET WHERE TRAN_ID = ?" ; // comment added by sagar on 23/07/15
					sqlHoldDet = "SELECT ITEM_CODE, SITE_CODE, LOC_CODE, LOT_NO, LOT_SL, LINE_NO_SL, LINE_NO FROM INV_HOLD_DET WHERE TRAN_ID = ?" ; // LINE_NO added by sagar on 23/07/15
					pHoldDet = conn.prepareStatement(sqlHoldDet);
					pHoldDet.setString(1,tranId);
					rsHoldDet = pHoldDet.executeQuery();
				//	pHoldDet.clearParameters();
					
					while ( rsHoldDet.next() )
					{
						itemCode = checkNull(rsHoldDet.getString("ITEM_CODE"));
						siteCode = checkNull(rsHoldDet.getString("SITE_CODE"));
						locCode = checkNull(rsHoldDet.getString("LOC_CODE"));
						lotNo = checkNull(rsHoldDet.getString("LOT_NO"));
						lotSl = checkNull(rsHoldDet.getString("LOT_SL"));
						lineNoSl = checkNull(rsHoldDet.getString("LINE_NO_SL"));
						lineNo = checkNull(rsHoldDet.getString("LINE_NO")); 
						System.out.println("itemCode : "+itemCode);
						System.out.println("lotNo : "+lotNo);
						System.out.println("lineNo : "+lineNo);
						
						if(lineNo !=null && lineNo.trim().length() > 0) // condition added by sagar on 23/07/15
						{
							lineNoIn= Integer.parseInt(lineNo.trim());
						}
						System.out.println("lineNoIn:"+lineNoIn);
						
						if(lockLevel != null && "I".equalsIgnoreCase(lockLevel))
						{
							sql = "update item set HOLD_FLAG = 'Y' where ITEM_CODE = ? ";
							pstmt  = conn.prepareStatement(sql);
							pstmt.setString(1,itemCode);
							int row = pstmt.executeUpdate();
							System.out.println("updated rows@@@ =="+row);
							//conn.commit();
							pstmt.close();
							pstmt = null;
							System.out.println("For Item level(item master),Updated hold flag 'Y' for item_code:"+itemCode);
						}
						
						if(lockLevel != null && "L".equalsIgnoreCase(lockLevel))
						{
							sql = "update item_lot_info set HOLD_FLAG = 'Y' where ITEM_CODE = ? and LOT_NO =? ";
							pstmt  = conn.prepareStatement(sql);
							pstmt.setString(1,itemCode);
							pstmt.setString(2,lotNo);
							int row = pstmt.executeUpdate();
							System.out.println("updated rows@@@ =="+row);
							//conn.commit();
							pstmt.close();
							pstmt = null;
							System.out.println("For Lot level(item lot info master),Updated hold flag 'Y' for item_code:"+itemCode);
							
						}
						
						System.out.println("1111111lineNoSl["+lineNoSl+"]");
						if( lineNoSl != null && lineNoSl.trim().length() > 0 ) //i.e. Line_sl is present
						{
							//tranIdTrace = generateTranId("w_inv_hold_rel_trace", conn);
							tranIdTrace = generateTranId("w_inv_hold_rel_trace",siteCode, conn); //CHANGED   BY RITESH ON 13/MAR/14 
							
							pSelStck = conn.prepareStatement( sqlSelStck );//Addded by chadra shekar on 17-nov-14
							pSelStck.setString( 1,itemCode );
							pSelStck.setString( 2,siteCode );
							pSelStck.setString( 3,locCode );
							pSelStck.setString( 4,lotNo );
							pSelStck.setString( 5,lotSl );
							rsSelStck = pSelStck.executeQuery();
							
							if( rsSelStck.next() ) 
							{
								stockQty = rsSelStck.getDouble("QUANTITY");
								qtyPerArt = rsSelStck.getDouble("QTY_PER_ART");	
								holdQty = rsSelStck.getDouble("HOLD_QTY");
							}
							if(pSelStck != null)
							{
								pSelStck.close();pSelStck = null;//Addded by chadra shekar on 17-nov-14
							}
							if( rsSelStck != null )
							{
								rsSelStck.close();
								rsSelStck = null;
							}
							
							//pSelStck.clearParameters();
							System.out.println("2222222");
							if(holdQty != stockQty)
							{
								holdQty = holdQty + qtyPerArt;
								pUpdate = conn.prepareStatement ( sqlUpdate );//Addded by chadra shekar on 17-nov-14
								if( holdQty > stockQty )
								{
									pUpdate.setDouble( 1, stockQty );
								}
								else//i.e.( holdQty <= stockQty )
								{
									pUpdate.setDouble( 1, holdQty );
								}
								pUpdate.setString( 2, itemCode);
								pUpdate.setString( 3, siteCode);
								pUpdate.setString( 4, locCode);
								pUpdate.setString( 5, lotNo);
								pUpdate.setString( 6, lotSl);
								pUpdate.executeUpdate();
								if(pUpdate != null)
								{
									pUpdate.close();pUpdate = null;//Addded by chadra shekar on 17-nov-14
								}
								//pUpdate.clearParameters();
							}
							pstmtInsert = conn.prepareStatement(sqlInsert);//Addded by chadra shekar on 17-nov-14
							pstmtInsert.setString( 1, tranIdTrace); 
							pstmtInsert.setString( 2, itemCode ); 
							pstmtInsert.setString( 3, siteCode );
							pstmtInsert.setString( 4, locCode );
							pstmtInsert.setString( 5, lotNo);
							pstmtInsert.setString( 6, lotSl );
							pstmtInsert.setString( 7, tranId);
							pstmtInsert.setDouble( 8,qtyPerArt );
							pstmtInsert.setString( 9, lockCode ); 
							pstmtInsert.setInt( 10, lineNoIn ); 
							
							pstmtInsert.executeUpdate();
							if(pstmtInsert != null)
							{
								pstmtInsert.close();pstmtInsert = null;//Addded by chadra shekar on 17-nov-14
							}
							//pstmtInsert.clearParameters();
						}
						else //i.e. line_sl is not found
						{
							//System.out.println("333333");
						
							//pHoldDet1 = conn.prepareStatement(sqlHoldDet);
							//pHoldDet1.setString( 1, tranId );
							//rsNoSl = pHoldDet1.executeQuery();
							//while ( rsNoSl.next() )
							//{
								//itemCode = checkNull(rsNoSl.getString("ITEM_CODE"));
								//siteCode = checkNull(rsNoSl.getString("SITE_CODE"));
								//locCode = checkNull(rsNoSl.getString("LOC_CODE"));
								//lotNo = checkNull(rsNoSl.getString("LOT_NO"));
								//lotSl = checkNull(rsNoSl.getString("LOT_SL"));
								
								
								sql = "SELECT ITEM_CODE,SITE_CODE,LOC_CODE,LOT_NO,LOT_SL,QUANTITY, HOLD_QTY,QTY_PER_ART FROM STOCK WHERE  QUANTITY > 0 " ;

								//ArrayList pkList = new ArrayList();

								if ( itemCode != null && itemCode.length() > 0 )
								{
									/*pkKeyValMap.put("ITEM_CODE", itemCode);
									pkList.add("ITEM_CODE");*/
									sql = sql + " AND ITEM_CODE ='"+itemCode+"'";
								}
								if ( siteCode != null && siteCode.length() > 0 )
								{
									/*pkKeyValMap.put("SITE_CODE", siteCode);
									pkList.add("SITE_CODE");*/
									sql = sql + "AND SITE_CODE = '"+siteCode+"' ";
									
								}
								if ( locCode != null && locCode.length() > 0 )
								{
									/*pkKeyValMap.put("LOC_CODE", locCode);
									pkList.add("LOC_CODE");*/
									
									sql = sql + "AND LOC_CODE = '"+locCode+"' ";
								}
								if ( lotNo != null && lotNo.length() > 0 )
								{
									/*pkKeyValMap.put("LOT_NO", lotNo);
									pkList.add("LOT_NO");*/
									
									sql = sql + "AND LOT_NO = '"+lotNo+"' ";
								}
								if ( lotSl != null && lotSl.length() > 0 )
								{
									/*pkKeyValMap.put("LOT_SL", lotSl);
									pkList.add("LOT_SL");*/
									
									sql = sql + "AND LOT_SL = '"+lotSl+"' ";
								}
								//System.out.println("pkKeyValMap :"+pkKeyValMap);
								/*System.out.println("4444444");
								String key = "";
								for ( int i = 0; i < pkList.size(); i++ )
								{
									key = pkList.get(i).toString();
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
								
								*/
								PreparedStatement pstmtStock = null;
								ResultSet rsStock = null;
								pstmtStock = conn.prepareStatement(sql);
								rsStock = pstmtStock.executeQuery();
								while ( rsStock.next() )
								{
									System.out.println("siteCode[[[["+siteCode+"]]]");
								//	tranIdTrace = generateTranId("w_inv_hold_rel_trace", conn);
									tranIdTrace = generateTranId("w_inv_hold_rel_trace",siteCode, conn);  // CHANGED BY RITESH  ON 13/MAR/14
									itemCode = checkNull(rsStock.getString("ITEM_CODE"));
									siteCode = checkNull(rsStock.getString("SITE_CODE"));
									locCode = checkNull(rsStock.getString("LOC_CODE"));
									lotNo = checkNull(rsStock.getString("LOT_NO"));
									lotSl = checkNull(rsStock.getString("LOT_SL"));
									stockQty = rsStock.getDouble("QUANTITY");
									System.out.println("Quantity :" + stockQty );
									holdQty = rsStock.getDouble("HOLD_QTY");
									qtyPerArt = rsStock.getDouble("QTY_PER_ART");
									System.out.println("555552");
									if(holdQty < stockQty )
									{	
										pUpdate = conn.prepareStatement ( sqlUpdate );//Addded by chadra shekar on 17-nov-14
										pUpdate.setDouble( 1, stockQty);
										pUpdate.setString( 2, itemCode);
										pUpdate.setString( 3, siteCode);
										pUpdate.setString( 4, locCode);
										pUpdate.setString( 5, lotNo);
										pUpdate.setString( 6, lotSl);
										pUpdate.executeUpdate();
										if(pUpdate != null)
										{
											pUpdate.close();pUpdate = null;//Addded by chadra shekar on 17-nov-14
										}
										//pUpdate.clearParameters();
									}	
									if( pstmt != null )
									{
										pstmt.close();
										pstmt = null;
									}
									
									pstmtInsert = conn.prepareStatement(sqlInsert);
									pstmtInsert.setString( 1, tranIdTrace);
									pstmtInsert.setString( 2, itemCode ); 
									pstmtInsert.setString( 3, siteCode );
									pstmtInsert.setString( 4, locCode );
									pstmtInsert.setString( 5, lotNo );
									pstmtInsert.setString( 6, lotSl );
									pstmtInsert.setString( 7, tranId );
									pstmtInsert.setDouble( 8, stockQty );
									pstmtInsert.setString( 9, lockCode ); 
									pstmtInsert.setInt( 10, lineNoIn ); 
									pstmtInsert.executeUpdate();
									if(pstmtInsert != null)
									{
										pstmtInsert.close();pstmtInsert = null;//Addded by chadra shekar on 17-nov-14
									}
									//pstmtInsert.clearParameters();camented by chadra shekar on 17-nov-14
									//pstmtStock.clearParameters();camented by chadra shekar on 17-nov-14
									
									if( rs != null )
									{
										rs.close();
										rs = null;
									}
								}
								//pkList = null;
								if(pstmtStock != null)
								{
									pstmtStock.close();pstmtStock = null;//Addded by chadra shekar on 17-nov-14
								}
								if(rsStock != null)
								{
									rsStock.close();
									rsStock = null;
								}
							//}//end of while()
							
						}//end of else
						
						if( rs != null )
						{
							rs.close();
							rs = null;
						}
						
						System.out.println("end of while!!!!");
					}//end of while()
					if(pHoldDet != null)
					{
						pHoldDet.close();pHoldDet = null;//Addded by chadra shekar on 17-nov-14
					}
					//pHoldDet.clearParameters();	camented by chadra shekar on 17-nov-14
				}				
				//changed by Dharmesh on 12-08-2011 [WM1ESUN004] to commit unconfirmed transaction.end
			}

			if( errString == null || errString.length() == 0 )
			{
				//changed by Dharmesh on 11-08-2011 [WM1ESUN004]  to bind variable dynamically instead of statically.start
				/*
				sql = "update inv_hold set confirmed = 'Y', emp_code__aprv = '" + loginEmpCode + "', CONF_DATE = ? where tran_id = '" + tranId + "'";
				pstmt = conn.prepareStatement( sql );
				pstmt.setTimestamp( 1, new java.sql.Timestamp( System.currentTimeMillis() ) );
				pstmt.executeUpdate();
				*/
				sql = "update inv_hold set confirmed = 'Y', emp_code__aprv = ? , CONF_DATE = ? where tran_id = ?" ;
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,loginEmpCode);
				pstmt.setTimestamp( 2, new java.sql.Timestamp( System.currentTimeMillis() ) );
				pstmt.setString(3,tranId);
				pstmt.executeUpdate();
				//changed by Dharmesh on 11-08-2011 [WM1ESUN004]  to bind variable dynamically instead of statically.end
				
				pstmt.close();
				pstmt = null;
				//changed by Dharmesh on 11-08-2011 [WM1ESUN004]  to bind variable dynamically instead of statically.start
				/*
				sql = "update inv_hold_det set STATUS_DATE = ? where tran_id = '" + tranId.trim() + "'";
				pstmt = conn.prepareStatement( sql );
				pstmt.setTimestamp( 1, new java.sql.Timestamp( System.currentTimeMillis() ) );
				pstmt.executeUpdate();
				*/
				sql = "update inv_hold_det set STATUS_DATE = ? where tran_id = ?" ;
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp( 1, new java.sql.Timestamp( System.currentTimeMillis() ) );
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
			{
				if(conStatus)
				{
					conn.commit();
				}
				errString = itmDBAccessEJB.getErrorString("","VTCNFSUCC","","",conn);
				System.out.println("Returnng String From InvHoldConfEJB : .." + errString);
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
            System.out.println("Returnng String From InvHoldConfEJB :"+errString);
            return errString;
        }catch(Exception e){
            System.out.println("Returnng String From InvHoldConfEJB :"+e);
            try{
				conn.rollback();
			}catch(Exception t){}
            e.printStackTrace();
            errString = itmDBAccessEJB.getErrorString("","VTDESNCONF","","",conn);
            System.out.println("Returnng String From InvHoldConfEJB :"+errString);
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
				if(conn != null && conStatus==true)
				{
					conn.close();
					conn = null;
				}
				
				//changed by Dharmesh on 12-08-2011 [WM1ESUN004]  to close preparedStatements and to make null.start 
				if( pstmtInsert != null )
				{
					pstmtInsert.close();
					pstmtInsert = null;
				}
				
				if(pHoldDet != null)
				{
					pHoldDet.close();
					pHoldDet = null;
				}
				if(pSelStck != null)
				{
					pSelStck.close();
					pSelStck = null;
				}
				if(pUpdate != null)
				{
					pUpdate.close();
					pUpdate = null;
				}				
				//changed by Dharmesh on 12-08-2011 [WM1ESUN004]  to close preparedStatements and to make null.end
				
            }catch(Exception e){System.out.println("Exception : "+e);e.printStackTrace();}
        }
        System.out.println("InvHoldConfEJB :"+errString);
        return errString;
	}
	//changed by Dharmesh on 12-08-2011 [WM1ESUN004] to auto genrate Transacion Id for inv_hold_rel_trace.start 
	// METHOD ARGS CHANGED  BY RITESH  ON 13/MAR/14
    public String generateTranId( String windowName,String siteCode, Connection conn )throws ITMException
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
			xmlValues = xmlValues +        "<site_code>" + siteCode + "</site_code>";   // ADDED BY RITESH ON 13/MAR/14
			xmlValues = xmlValues +        "<tran_date>" + currDateStr + "</tran_date>"; // ADDED BY RITESH ON 13/MAR/14
			xmlValues = xmlValues + "</Detail1></Root>";
			System.out.println("xmlValues  :["+xmlValues+"]");
			TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", CommonConstants.DB_NAME);
			tranId = tg.generateTranSeqID(tranSer, keyCol, keyString, conn);
			System.out.println("tranId NEWLY WT SITECODE & DATE :"+tranId);
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
	private String checkNull( String inputVal )
	{
		if ( inputVal == null )
		{
			inputVal = "";
		}
		return inputVal;
	}
}
//changed by Dharmesh on 12-08-2011 [WM1ESUN004] to auto genrate Transacion Id for inv_hold_rel_trace.end