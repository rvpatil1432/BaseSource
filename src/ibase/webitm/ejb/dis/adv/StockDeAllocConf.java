/********************************************************
	Title : StockDeAllocConf[W14CSUN004]
	Date  : 10/06/2014
	Developer: Chandrashekar

 ********************************************************/

package ibase.webitm.ejb.dis.adv;

import ibase.system.config.ConnDriver;
import ibase.utility.CommonConstants;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.dis.InvAllocTraceBean;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import javax.ejb.Stateless;

@Stateless
public class StockDeAllocConf extends ActionHandlerEJB implements StockDeAllocConfLocal, StockDeAllocConfRemote//SessionBean {
{
	public String confirm(String tranID,String xtraParams, String forcedFlag) throws RemoteException,ITMException
	{
		System.out.println("----------------confirmed method (StockDeAllocConf)---------");
		String retString = "";		
		Connection conn = null;	
		ConnDriver connDriver = null;
		try
		{
			//connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 13-06-2016 :START
			//conn = connDriver.getConnectDB("Driver");
			//conn = getConnection();
			//Changes and Commented By Bhushan on 13-06-2016 :END
			//conn.setAutoCommit(false);

			retString = confirm( tranID, xtraParams, forcedFlag,conn);		

		}
		catch(Exception exception)
		{
			System.out.println("Exception in [StockDeAllocConf] confirm " + exception.getMessage());
		}
		return retString;
	}
	public String confirm(String tranId,String xtraParams, String forcedFlag ,Connection conn) throws RemoteException,ITMException
	{
		PreparedStatement pstmt = null, pstmt1 = null ;
		ResultSet rs = null, rs1 = null;
		String sql = "";
		String confirm = "";
		String errString = "" ; 
		ConnDriver connDriver = null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		double availQty =0.0;
		String sqlSorditem = "";
		PreparedStatement psmtSord = null;
		boolean isLocal=false;
		try
		{	
			if (conn==null)
			{
				connDriver = new ConnDriver();
				//Changes and Commented By Bhushan on 13-06-2016 :START
				//conn = connDriver.getConnectDB("Driver");
				conn = getConnection();
				//Changes and Commented By Bhushan on 13-06-2016 :END
				conn.setAutoCommit(false);
				isLocal=true;
			}
			itmDBAccessEJB = new ITMDBAccessEJB();

			sql = "select confirmed,site_code from sord_alloc where tran_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,tranId);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				confirm = rs.getString("confirmed");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if(confirm != null  && "Y".equalsIgnoreCase(confirm))
			{
				System.out.println("The Selected transaction is already confirmed");
				errString = itmDBAccessEJB.getErrorString("","VTMCONF1","","",conn);
				return errString;
			}
			else
			{
				sql = "SELECT SALE_ORDER, LINE_NO__SORD, SUM(QUANTITY) FROM SORD_ALLOC_DET WHERE TRAN_ID = ? " +
				" GROUP BY SALE_ORDER, LINE_NO__SORD ORDER BY SALE_ORDER, LINE_NO__SORD";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();

				while( rs.next())
				{
					if ("db2".equalsIgnoreCase(CommonConstants.DB_NAME ) ||  "mysql".equalsIgnoreCase(CommonConstants.DB_NAME ))
					{
						sqlSorditem = " select * from sorditem where sale_order   = ? and line_no = ? for update ";
					}
					else if ( "mssql".equalsIgnoreCase(CommonConstants.DB_NAME ))
					{
						sqlSorditem = " select * from sorditem (updlock) where sale_order   = ? and line_no = ? ";
					}
					else
					{
						sqlSorditem = " select * from sorditem where sale_order   = ? and line_no = ? for update nowait ";
					}

					psmtSord = conn.prepareStatement(sqlSorditem);
					psmtSord.setString(1, rs.getString("SALE_ORDER"));
					psmtSord.setString(2, rs.getString("LINE_NO__SORD"));
					psmtSord.executeQuery();
					if(psmtSord!=null)
						psmtSord.close();
					psmtSord= null;

				}

				pstmt.close();pstmt = null;
				rs.close();rs = null;
				if(errString != null && errString.trim().length() > 0)
				{
					return errString;
				}
				else
				{

					errString = this.sorderDeAllocate(tranId,xtraParams,conn);
				}

			}
		} //end of try
		catch(Exception e)
		{	
			System.out.println("Exception ::"+e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("Inside finally commit"+ errString+"isLocal["+isLocal+"]");
				if(errString != null && errString.trim().length() > 0 && isLocal)
				{
					System.out.println("Inside finally commit"+ errString);
					if(errString.indexOf("CONFSUCCES") > -1 )
					{
						conn.commit();
					}
					else
					{
						conn.rollback();
					}
					conn.close();
					conn = null;
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

			}
			catch(Exception e)
			{
				System.out.println("Exception : "+e);e.printStackTrace();
				throw new ITMException(e);
			}

		}
		return errString;
	} //end of confirm method

	private	String sorderDeAllocate( String tranId, String xtraParams, Connection conn )throws RemoteException,ITMException
	{
		String getDataSql = null;
		String saleOrder = null;
		String lineNo = null;
		String tranIDD = null;
		String lotSl= null;
		String lotNo = null;
		String locCode = null;
		String lineNoSord = null;
		String itemCode = null;
		String siteCodeShip = null;  
		String expLev = null;
		String updateSorditem = null;
		String updateSql= null;
		String empCode = "";
		String dateNow ="";
		String errString = "";
		double qtyToBeDeAllocated = 0d;
		double quantity =0;
		double pendingQuantity = 0;
		PreparedStatement pstmt = null;
		PreparedStatement pSordAllocDet = null;

		ResultSet rs = null ;
		ITMDBAccessEJB itmDBAccessEJB = null;
		PreparedStatement st = null;
		PreparedStatement st1 = null;
		ResultSet rsQtyAlloc=null;
		String SqlQtyAlloc="",DelQtyAlloc="";
		InvAllocTraceBean invAllocTrace = new InvAllocTraceBean();
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		HashMap strDeAllocate = null;
		try
		{
			String dbDateFormat = genericUtility.getDBDateFormat();
			String applDateFormat = genericUtility.getApplDateFormat();
			itmDBAccessEJB = new ITMDBAccessEJB();
			java.util.Date currDate1 = new java.util.Date();
			SimpleDateFormat sdf = new SimpleDateFormat(applDateFormat);
			String currDateStr = sdf.format(currDate1);
			java.sql.Timestamp tranDate = java.sql.Timestamp.valueOf(genericUtility.getValidDateString( currDateStr , applDateFormat, dbDateFormat ) + " 00:00:00.0") ;

			getDataSql = "select  d.tran_id ,d.line_no ,d.sale_order ,d.line_no__sord ,d.item_code , "
				+" d.loc_code ,d.lot_no ,d.lot_sl ,d.quantity ,d.dealloc_qty,h.site_code__ship , "       
				+" d.exp_lev,d.pending_qty "
				+" from  sord_alloc_det d , sord_alloc h "
				+" where h.tran_id =  d.tran_id and d.tran_id = ? "
				+" order by  line_no  asc   "; 
			pSordAllocDet = conn.prepareStatement(getDataSql);
			pSordAllocDet.setString(1,tranId);
			rs = pSordAllocDet.executeQuery();
			System.out.println("tranId------["+ tranId);

			int updCount = 0;
			while(rs.next())
			{


				tranIDD = rs.getString(1); 
				lineNo = rs.getString(2);  
				saleOrder = rs.getString(3);
				lineNoSord = rs.getString(4);
				lineNoSord = "   " + lineNoSord.trim();                        
				lineNoSord = lineNoSord.substring( lineNoSord.length()-3 );
				itemCode = rs.getString(5);
				locCode = rs.getString(6);
				lotNo = rs.getString(7);
				lotSl = rs.getString(8);
				quantity = rs.getDouble(9);
				//siteCode = rs.getString(10);   
				siteCodeShip = rs.getString(11); 
				expLev = rs.getString(12);
				pendingQuantity = rs.getDouble(13);

				qtyToBeDeAllocated = quantity;
				System.out.println("pendingQuantity["+pendingQuantity+"]");
				System.out.print("qtyToBeDeAllocated = " + qtyToBeDeAllocated);

				strDeAllocate = new HashMap();
				strDeAllocate.put("tran_date",tranDate);
				strDeAllocate.put("ref_ser","D-ALOC");
				strDeAllocate.put("site_code",siteCodeShip);     
				strDeAllocate.put("item_code",itemCode);
				strDeAllocate.put("loc_code",locCode);
				strDeAllocate.put("lot_no",lotNo);
				strDeAllocate.put("lot_sl",lotSl);
				strDeAllocate.put("alloc_qty",new Double(-1 * qtyToBeDeAllocated));
				System.out.print("DeallocQty = [" + (-1 * qtyToBeDeAllocated)+"]");
				strDeAllocate.put("chg_user",new  ibase.utility.E12GenericUtility().getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
				strDeAllocate.put("chg_term",new  ibase.utility.E12GenericUtility().getValueFromXTRA_PARAMS(xtraParams,"termId"));
				strDeAllocate.put("chg_win","W_SORD_DEALLOC");

				if (qtyToBeDeAllocated > 0 && saleOrder.trim().length()>0)
				{
					strDeAllocate.put("ref_id",saleOrder);
					strDeAllocate.put("ref_line",lineNo);
					errString = invAllocTrace.updateInvallocTrace(strDeAllocate,conn);

					if (errString != null && errString.trim().length() > 0)
					{
						System.out.println("errString :::"+ errString );
						return errString;
					}
					System.out.println("expLev["+expLev+"]"+"saleOrder["+saleOrder+"]");
					updateSorditem ="UPDATE SORDITEM  SET QTY_ALLOC = QTY_ALLOC - " + new Double(qtyToBeDeAllocated).toString()
					+" WHERE SALE_ORDER = '" + saleOrder + "' "
					+" AND LINE_NO = '" + lineNoSord + "' "
					+" AND EXP_LEV = '" + expLev + "' ";

					System.out.println("updateSql------->"+updateSorditem);
					st = conn.prepareStatement(updateSorditem);
					// Changed by Manish on 28/09/15 [start]
					// The method executeUpdate() cannot take arguments on a PreparedStatement
					//st.executeUpdate(updateSorditem);

					st.executeUpdate();

					// Changed by Manish on 28/09/15 [End]

					System.out.println("UPDATE  SUCCESS FOR SORDITEM....>>>>>>>>");
					st.close();
					st = null;
					updateSql = " UPDATE SORDALLOC SET QTY_ALLOC =  QTY_ALLOC - " + new Double(qtyToBeDeAllocated).toString()
					+ " WHERE SALE_ORDER = '" + saleOrder + " ' "
					+ " AND LINE_NO = '" + lineNoSord + "' "
					+ " AND EXP_LEV = '" + expLev + "' "
					+ " AND ITEM_CODE = '" + itemCode + "' "
					+ " AND LOT_NO = '" + lotNo + "' "
					+ " AND LOT_SL = '" + lotSl + "' "
					+ " AND LOC_CODE = '" + locCode + "' " ;
					System.out.println("updateSql:::>>>>"+ updateSql);
					st = conn.prepareStatement(updateSql);

					// Changed by Manish on 28/09/15 [start]
					// The method executeUpdate() cannot take arguments on a PreparedStatement
					//updCount=st.executeUpdate(updateSql);
					updCount=st.executeUpdate();
					// Changed by Manish on 28/09/15 [End]

					if ( updCount > 0 )
					{
						System.out.println("UPDATE  SUCCESS FOR SORDITEM....>>>>>>>>");
					}
					st.close();
					st = null;
					System.out.println("UPDATE  SUCCESS FOR SORDALLOC....");
					SqlQtyAlloc = "SELECT QTY_ALLOC FROM SORDALLOC "
						+ " WHERE SALE_ORDER = '" + saleOrder + " ' "
						+ " AND LINE_NO = '" + lineNoSord + "' "
						+ " AND EXP_LEV = '" + expLev + "' "
						+ " AND ITEM_CODE = '" + itemCode + "' "
						+ " AND LOT_NO = '" + lotNo + "' "
						+ " AND LOT_SL = '" + lotSl + "' "
						+ " AND LOC_CODE = '" + locCode + "' " ;
					System.out.println("SqlQtyAlloc:::>>>>"+ SqlQtyAlloc);
					st = conn.prepareStatement(SqlQtyAlloc);

					// Changed by Manish on 28/09/15
					// The method executeUpdate() cannot take arguments on a PreparedStatement
					//rsQtyAlloc  = st.executeQuery(SqlQtyAlloc);

					rsQtyAlloc  = st.executeQuery();

					// Changed by Manish on 28/09/15

					if (rsQtyAlloc.next())
					{
						System.out.println("Updated Allocated Qty :::"+rsQtyAlloc.getDouble(1));
						if(rsQtyAlloc.getDouble(1)<=0)
						{
							DelQtyAlloc = "DELETE FROM SORDALLOC"
								+ " WHERE SALE_ORDER = '" + saleOrder + " ' "
								+ " AND LINE_NO = '" + lineNoSord + "' "
								+ " AND EXP_LEV = '" + expLev + "' "
								+ " AND ITEM_CODE = '" + itemCode + "' "
								+ " AND LOT_NO = '" + lotNo + "' "
								+ " AND LOT_SL = '" + lotSl + "' "
								+ " AND LOC_CODE = '" + locCode + "' " ;
							System.out.println("DelQtyAlloc:::"+DelQtyAlloc);
							st1 = conn.prepareStatement(DelQtyAlloc);

							// Changed by Manish on 28/09/15
							// The method executeUpdate() cannot take arguments on a PreparedStatement								
							//st1.executeUpdate(DelQtyAlloc);

							st1.executeUpdate();

							// Changed by Manish on 28/09/15

							System.out.println("Delete completed ");
							st1.close();
							st1 = null;
						}
					}
					rsQtyAlloc.close();
					rsQtyAlloc = null;
					st.close();
					st = null;
				}
				/*//For saleOrder is null and you have cust_code
					else if(qtyToBeDeAllocated > 0 && tranId.trim().length()>0)
					{
						strDeAllocate.put("ref_id",tranId);
						System.out.println("alloc_tranid::----"+tranId);
						strDeAllocate.put("ref_line",lineNo);
						//System.out.println("siteCode::"+siteCodeShip+"itemCode:"+itemCode+"locCode::"+locCode +"lotNo:"+lotNo+"lotSl:"+"QtyToBeDeallocated::"+(-1*qtyToBeDeAllocated)+"alloc_tranid::"+tranId+"allocLineno::"+allocLineno);
						errString = invAllocTrace.updateInvallocTrace(strDeAllocate,conn);
						if (errString != null && errString.trim().length() > 0)
						{
							System.out.println("errString :::"+ errString );
							return errString;
						}
						updateSordAllocDet= " UPDATE SORD_ALLOC_DET SET DEALLOC_QTY = DEALLOC_QTY + "+  new Double(qtyToBeDeAllocated).toString()
								+" WHERE SORD_ALLOC_DET.TRAN_ID='"+ tranId +"'"
								+" AND SORD_ALLOC_DET.LINE_NO ="+ lineNo ;
						System.out.println("updateSordAllocDet::"+ updateSordAllocDet);
						st = conn.prepareStatement(updateSordAllocDet);
						st.executeUpdate(updateSordAllocDet);
						System.out.println("updateSordAllocDet completed successfully");
						st.close();
						st = null;
					}*/

			}//end of while 
			rs.close(); rs = null;
			pSordAllocDet.clearParameters();
			pSordAllocDet.close(); pSordAllocDet = null;

			empCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());
			dateNow = simpleDateFormat.format(currentDate.getTime());
			java.sql.Timestamp confDate = java.sql.Timestamp.valueOf(genericUtility.getValidDateString( dateNow , genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat() ) + " 00:00:00.0") ;
			updateSql = "UPDATE SORD_ALLOC SET CONFIRMED =  'Y', emp_code__aprv = ?, conf_date = ? WHERE TRAN_ID = ? ";
			pstmt = conn.prepareStatement(updateSql);
			pstmt.setString(1,empCode);
			pstmt.setTimestamp(2,confDate);
			pstmt.setString(3,tranId);
			updCount = pstmt.executeUpdate();
			pstmt.close(); pstmt = null;

			if( updCount > 0 )
			{
				errString = itmDBAccessEJB.getErrorString("","CONFSUCCES","","",conn);
				return errString; 
			}	 
		}
		catch(SQLException se)
		{
			System.out.println("SQLException :" + se);
			se.printStackTrace();
			errString = se.getMessage();
			return errString;
		}

		catch(Exception e)
		{
			System.out.println("Exception :" + e);
			errString = e.getMessage();
			e.printStackTrace();
			
			try
			{
				conn.rollback();
			}
			catch(Exception e1)
			{
				e = e1;
			}
			return errString ;
		}
		return errString;
	}// end of sorderDeAllocate
}
