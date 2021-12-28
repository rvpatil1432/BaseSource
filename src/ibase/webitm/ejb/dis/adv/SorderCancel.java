package ibase.webitm.ejb.dis.adv;

/*****
 * ADDED BY ANJALI R. ON [14/11/2018]
 * MIGRATED PB TO JAVA AS PER CURRENT NVO LOGIC 
***/
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.ejb.Stateless;
import ibase.webitm.ejb.DBAccessEJB;
import ibase.system.config.ConnDriver;

import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.dis.InvAllocTraceBean;
import ibase.webitm.utility.ITMException;
import ibase.webitm.utility.TransIDGenerator;

@Stateless
public class SorderCancel extends ActionHandlerEJB implements SorderCancelLocal,SorderCancelRemote
{
	String termId = "";
	String userId = "";
	String mStat = "";
	public String confirm() throws RemoteException,ITMException
	{
		return "";
	}

	public String confirm(String tranID, String xtraParams,String forcedFlag) throws RemoteException,ITMException
	{
		String retString = "";
		Connection conn = null;
		String tranIdFr = "";
		String tranIdTo = "";
		E12GenericUtility genericUtility = null;
		ITMDBAccessEJB itmdbAccessEJB = null;
		try
		{
			conn = getConnection();
			tranIdFr = tranID ;
			tranIdTo = tranID;
			genericUtility = new E12GenericUtility();
			UserInfoBean userInfo = new UserInfoBean();
			itmdbAccessEJB = new ITMDBAccessEJB();
			
			userInfo = getUserInfo();
			termId = userInfo.getLoginCode();
			userId = termId;
			System.out.println("termId--["+termId+"]");
			//termId = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId"));
			//userId = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode"));
			retString = confirm(tranIdFr,tranIdTo, xtraParams, forcedFlag, conn);
			if(retString != null && retString.trim().length()>0)
			{
				conn.rollback();
			}
			else
			{
				conn.commit();
				retString = itmdbAccessEJB.getErrorString("", "VTSOCANC02", userId,"",conn);
			}
		}
		catch(Exception e)
		{
			try 
			{
				conn.rollback();
			} 
			catch (SQLException e1)
			{
				e1.printStackTrace();
			}
			System.out.println("Exception --["+e.getMessage()+"]");
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(conn != null) 
				{
					conn.close();
					conn = null;
				}
			}
			catch(Exception e)
			{
				System.out.println("Exception --["+e.getMessage()+"]");
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return retString;
	}
	public String confirm(String tranIdFr,String tranIdTo, String xtraParams,String forcedFlag,Connection conn) throws RemoteException,ITMException
	{
		String retString = "";
		String remarks = "";	
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		PreparedStatement pstmt1 = null;
		ResultSet rs1 = null;
		String sql1 = "";
		PreparedStatement pstmt2 = null;
		//ResultSet rs2 = null;
		String sql2 = "";
		PreparedStatement pstmt3 = null;
		ResultSet rs3 = null;
		String sql3 = ""; 
		PreparedStatement pstmt4 = null;
		//ResultSet rs4 = null;
		String sql4 =""; 
		int cnt = 0;
		String database = "";
		String userId = "";

		int lineNo = 0;
		int lineNoContr = 0;
		int stat = 0;
		double quantity = 0.0;
		double qtyOrd = 0.0;

		String saleOrder = "";
		String confirmed = "";
		String status = "";
		String contractNo = "";
		String itemFlag ="";
		String itemCode = "";
		String quotNo = "";
		String autoCloseExecuted = "";
		boolean isDespatchExist = true;
		String despExist = "";

		E12GenericUtility genericUtility = null;
		ITMDBAccessEJB itmDbAccess = null;
		//SaleOrderRelease orderRelease = null;
		try
		{
			genericUtility = new E12GenericUtility();
			itmDbAccess = new ITMDBAccessEJB();
			//orderRelease = new SaleOrderRelease();

			database = CommonConstants.DB_NAME;
			if(tranIdFr.equalsIgnoreCase(tranIdTo))
			{
				saleOrder = tranIdFr;
			}
			if(("db2".equalsIgnoreCase(database)) || ("mysql".equalsIgnoreCase(database)))
			{
				sql = "SELECT confirmed, status, contract_no FROM sorder WHERE sale_order = ? for update";
			}
			else if("mssql".equalsIgnoreCase(database))
			{
				sql = "SELECT confirmed, status, contract_no FROM sorder (updlock) WHERE sale_order = ? ";
			}
			else
			{
				sql = "SELECT confirmed, status, contract_no FROM sorder WHERE sale_order = ? for update nowait";
			}
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, saleOrder);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				confirmed = checkNull(rs.getString("confirmed"));
				status = checkNull(rs.getString("status"));
				contractNo = checkNull(rs.getString("contract_no"));
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
			System.out.println("confirmed--["+confirmed+"]status--["+status+"]contractNo--["+contractNo+"]");
			if("X".equalsIgnoreCase(status))
			{
				retString = itmDbAccess.getErrorString("", "VTCANC2", userId,"",conn);
				return retString;
			}
			else if("C".equalsIgnoreCase(status)) 
			{
				retString = itmDbAccess.getErrorString("", "VTCANC5", userId,"",conn);
				return retString;
			}

			/*sql = "SELECT CONFIRMED  FROM DESPATCH WHERE SORD_NO = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, saleOrder);
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				despExist = checkNull(rs.getString("CONFIRMED"));
				if( despExist.trim().length() == 0 || "N".equalsIgnoreCase(despExist))
				{
					retString = itmDbAccess.getErrorString("", "SORDCANC01", userId,"",conn);
					return retString;
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
			}*/
			cnt = 0;
			sql = "SELECT COUNT(1) FROM despatch despatch,DESPATCHDET DESPATCHDET WHERE  DESPATCHDET.DESP_ID   = DESPATCH.DESP_ID  " + 
					"AND DESPATCHDET.SORD_NO = ? AND (CASE WHEN DESPATCH.CONFIRMED IS NULL or length(DESPATCH.CONFIRMED) = 0 THEN 'N' ELSE DESPATCH.CONFIRMED END) = 'N'";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, saleOrder);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				cnt = rs.getInt(1);
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
			if(cnt > 0)
			{
				retString = itmDbAccess.getErrorString("", "VTSOCANC01", userId,"",conn);
				return retString;
			}
			sql = "select item_flg, line_no__contr, line_no , item_code  , quantity from sorddet where sale_order = ?	order by line_no";
			pstmt  = conn.prepareStatement(sql);

			sql1 = "select quantity - qty_desp as qty_ord from sorditem where sale_order = ? and line_no = ?";
			pstmt1 = conn.prepareStatement(sql1);

			sql2 = "update scontractdet set rel_qty = rel_qty - ? where contract_no = ? and line_no = ?";
			pstmt2 = conn.prepareStatement(sql2);

			sql3 = "select quot_no  from sorder where sale_order = ?";
			pstmt3 = conn.prepareStatement(sql3);

			sql4 = "update sales_quotdet set  	rel_qty = (case when rel_qty is null then 0 else rel_qty end)  - ?, " + 
					"rel_date = ?,bal_qty = (case when bal_qty is null then 0 else bal_qty end)  + ? " + 
					"where quot_no = ? and item_code = ?";
			pstmt4 = conn.prepareStatement(sql4);

			pstmt.setString(1, saleOrder);
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				itemFlag = checkNull(rs.getString("item_flg"));
				lineNoContr = rs.getInt("line_no__contr");
				lineNo = rs.getInt("line_no");
				itemCode = checkNull(rs.getString("item_code"));
				quantity = rs.getDouble("quantity");

				if(contractNo != null && contractNo.trim().length()>0)
				{
					if(!"I".equalsIgnoreCase(itemFlag))
					{
						retString = getErrorString("status", "VTCANC5", userId);
						return retString;
					}
				}
				else
				{
					pstmt1.setString(1, saleOrder);
					pstmt1.setInt(2, lineNo);
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						qtyOrd = rs1.getDouble("qty_ord");
					}
					else
					{
						pstmt2.setDouble(1, qtyOrd);
						pstmt2.setString(2, contractNo);
						pstmt2.setInt(3, lineNoContr);
						cnt = pstmt2.executeUpdate();
						System.out.println("updated ["+cnt+"] rows in scontractdet table ");
						pstmt2.clearParameters();
					}
					pstmt1.clearParameters();
				}
				pstmt3.setString(1, saleOrder);
				rs3 = pstmt3.executeQuery();
				if(rs3.next())
				{
					quotNo = checkNull(rs3.getString("quot_no"));
				}
				pstmt3.clearParameters();
				if(quotNo != null && quotNo.trim().length() > 0)
				{
					cnt = 0;
					pstmt4.setDouble(1, quantity);
					pstmt4.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));
					pstmt4.setDouble(3, quantity);
					pstmt4.setString(4, quotNo);
					pstmt4.setString(5, itemCode);
					cnt = pstmt4.executeUpdate();
					System.out.println("updated ["+cnt+"] rows in sales_quotdet table. ");
					pstmt4.clearParameters();
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
			if(pstmt2 != null ) 
			{
				pstmt2.close();
				pstmt2 = null;
			}
			if(rs3 != null)
			{
				rs3.close();
				rs3 = null;
			}
			if(pstmt3 != null)
			{
				pstmt3.close();
				pstmt3 = null;
			}
			if(pstmt4 != null)
			{
				pstmt4.close();
				pstmt4 = null;
			}
			if(retString != null && retString.trim().length() > 0)
			{
				return retString;
			}
			if("N".equalsIgnoreCase(confirmed))
			{
				autoCloseExecuted = "N";
				mStat = "X";
			}
			else
			{
				autoCloseExecuted = "Y";
				retString = gfAutoClose(saleOrder, status, conn);
			}
			if(retString != null && retString.trim().length() > 0)
			{
				return retString;
			}
			if( autoCloseExecuted.equalsIgnoreCase("Y"))
			{
				cnt = 0;
				sql = "  UPDATE SORDER SET STATUS = ?,  STATUS_REMARKS = ?,ALLOC_FLAG = 'N'	WHERE SALE_ORDER 	= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, mStat);
				pstmt.setString(2, remarks);
				pstmt.setString(3, tranIdFr);
				cnt = pstmt.executeUpdate();
				System.out.println("Number Of Effected Row SORDER table ["+ cnt + "]" );
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}

				cnt = 0;
				sql = "UPDATE SORDITEM SET STATUS = ?	WHERE SALE_ORDER =  ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, mStat);
				pstmt.setString(2, tranIdFr);
				cnt = pstmt.executeUpdate();
				System.out.println("Effected Rows effectRow:::"+cnt);
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}

				if( "C".equalsIgnoreCase(status))
				{
					stat = 1;	
				}
				else
				{
					stat = 2;
				}
				java.sql.Timestamp toDay = new java.sql.Timestamp(System.currentTimeMillis());
				retString = sorderStatusLog(tranIdFr, toDay, stat, xtraParams, "", "", "", "");
				if( retString != null && retString.trim().length() > 0)
				{
					return retString;
				}
			}
			cnt = 0;
			sql = " UPDATE SORDER SET STATUS = ?, STATUS_REMARKS = ?, ALLOC_FLAG = 'N' WHERE SALE_ORDER 	= ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, mStat);
			pstmt.setString(2, remarks);
			pstmt.setString(3, tranIdFr);
			cnt = pstmt.executeUpdate();
			System.out.println("Number Of Effected Row SORDER table ["+ cnt + "]" );
			if(pstmt != null)
			{
				pstmt.close();
				pstmt = null;
			}
			if(retString != null && retString.trim().length() > 0)
			{
				return retString;
			}
			else
			{
				if("C".equalsIgnoreCase(status))
				{
					retString = itmDbAccess.getErrorString("status", "VTCANC4", userId,"",conn);
					return retString;
				}
				else if("X".equalsIgnoreCase(status))
				{
					retString = itmDbAccess.getErrorString("status", "VTCANC1", userId,"",conn);
					return retString;
				}
			}
		}
		catch(Exception e)
		{
			try 
			{
				conn.rollback();
			} 
			catch (SQLException e1)
			{
				e1.printStackTrace();
			}
			System.out.println("Exception --["+e.getMessage()+"]");
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
			}
			catch(Exception e)
			{
				try 
				{
					conn.rollback();
				} 
				catch (SQLException e1)
				{
					e1.printStackTrace();
				}
				System.out.println("Exception --["+e.getMessage()+"]");
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return retString;
	}
	public String gfAutoClose(String mSaleOrder, String mStatus,Connection conn) throws ITMException 
	{
		PreparedStatement pstmt = null, pstmt1 = null;
		ResultSet rs = null, rs1 = null;
		double ldQtyAlloc = 0, ldQtyDesp = 0, mQtyAlloc = 0 , mQtyDesp = 0;
		Date mtday = null;
		int	llCnt = 0;
		SimpleDateFormat sdf = null;
		String currDateStr = null;
		String sql = "";
		String confirmed = "";
		int cnt = 0;
		String errString = "";
		boolean isError = false;
		Timestamp currDate = null;

		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		try
		{
			sql = "SELECT CONFIRMED  FROM SORDER where sale_order = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, mSaleOrder.trim());
			rs = pstmt.executeQuery();
			if( rs.next() )
			{
				confirmed = checkNull(rs.getString("CONFIRMED"));
			}
			System.out.println("confirmed--@@@["+confirmed+"]");
			if ( rs != null )
			{
				rs.close();
				rs = null;
			}
			if( pstmt != null )
			{
				pstmt.close();
				pstmt = null;
			}
			if( confirmed == null || confirmed.trim().length() == 0 )
			{
				confirmed = "N";
			}
			if( "N".equalsIgnoreCase(confirmed))
			{
				mStatus = "C";
			}
			else
			{
				cnt = 0;
				/*sql = "SELECT COUNT(1)  FROM SORDALLOC WHERE SALE_ORDER = ? AND CASE WHEN STATUS IS NULL THEN 'P' ELSE STATUS END <> 'D' ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, mSaleOrder.trim());
				rs = pstmt.executeQuery();
				if( rs.next() )
				{
					cnt = rs.getInt(1);
				}
				if ( rs != null )
				{
					rs.close();
					rs = null;
				}
				if( pstmt != null )
				{
					pstmt.close();
					pstmt = null;
				}

				if(cnt > 0)
				{
					errString = itmDBAccess.getErrorString("", "VTALLOC02", "","",conn); ///COMMENTED  BY NANDKUMAR GADKARI ON 02/01/20
					return errString;	
				}*/
				//ADDED  BY NANDKUMAR GADKARI ON 02/01/20------------START--------------------
				sql = "SELECT COUNT(1)  FROM SORDALLOC WHERE SALE_ORDER = ? AND CASE WHEN STATUS IS NULL THEN 'P' ELSE STATUS END <> 'D' ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, mSaleOrder.trim());
				rs = pstmt.executeQuery();
				if( rs.next() )
				{
					cnt = rs.getInt(1);
				}
				if ( rs != null )
				{
					rs.close();
					rs = null;
				}
				if( pstmt != null )
				{
					pstmt.close();
					pstmt = null;
				}

				if(cnt > 0)
				{
					mStatus = "X";
				}
				//ADDED  BY NANDKUMAR GADKARI ON 02/01/20------------END--------------------
				sql = "SELECT SUM(QTY_ALLOC) AS LD_QTY_ALLOC, SUM(QTY_DESP) AS LD_QTY_DESP 	FROM SORDITEM WHERE SALE_ORDER = ?  AND LINE_TYPE = 'I'";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, mSaleOrder.trim());
				rs = pstmt.executeQuery();
				if( rs.next() )
				{
					ldQtyAlloc = rs.getDouble("LD_QTY_ALLOC");
					ldQtyDesp = rs.getDouble("LD_QTY_DESP");
				}
				if ( rs != null )
				{
					rs.close();
					rs = null;
				}
				if( pstmt != null )
				{
					pstmt.close();
					pstmt = null;
				}
				if( ldQtyAlloc == 0 && ldQtyDesp == 0 )
				{
					mStatus = "X";
				}
				else
				{
					sql = "select QTY_ALLOC, QTY_DESP from SORDITEM where SALE_ORDER = ? and LINE_TYPE = 'I'";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mSaleOrder.trim());
					rs = pstmt.executeQuery();
					while( rs.next() )
					{
						mQtyAlloc = rs.getDouble("QTY_ALLOC");
						mQtyDesp = rs.getDouble("QTY_DESP");

						if( mQtyAlloc <= mQtyDesp)
						{
							mStatus = "C";
						}
					}
					if ( rs != null )
					{
						rs.close();
						rs = null;
					}
					if( pstmt != null )
					{
						pstmt.close();
						pstmt = null;
					}
					errString = gfAutoCloseExt(mSaleOrder, mStatus,conn);
				}
			}
			if( errString == null || errString.trim().length() == 0 )
			{
				if( confirmed == "N")
				{
					llCnt = 0;
					sql = "UPDATE SORDER	SET STATUS = ?, STATUS_DATE = ? 	WHERE SALE_ORDER = ?";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, mStatus.trim());
					pstmt1.setTimestamp(2, currDate);
					pstmt1.setString(3, mSaleOrder.trim());
					llCnt = pstmt1.executeUpdate();
					System.out.println("Number od effect rows in SORDER table =====>"+llCnt);
					if( pstmt1 != null )
					{
						pstmt1.close();
						pstmt1 = null;
					}
				}
				else
				{
					llCnt = 0;
					sql = "UPDATE SORDITEM SET STATUS = ? WHERE SALE_ORDER = ?";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, mStatus.trim());
					pstmt1.setString(2, mSaleOrder.trim());
					llCnt = pstmt1.executeUpdate();
					System.out.println("Number od effect rows in else block SORDER table =====>"+llCnt);
					if( pstmt1 != null )
					{
						pstmt1.close();
						pstmt1 = null;
					}

					cnt = 0;
					sql = "SELECT COUNT(1)  FROM SORDITEM 	WHERE SALE_ORDER = ? AND (STATUS NOT IN ('C','X') OR STATUS IS NULL)";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, mSaleOrder.trim());
					rs1 = pstmt1.executeQuery();
					if( rs1.next() )
					{
						cnt = rs1.getInt(1);
					}
					if( pstmt1 != null )
					{
						pstmt1.close();
						pstmt1 = null;
					}
					if ( rs1 != null )
					{
						rs1.close();
						rs1 = null;
					}
					if( cnt == 0)
					{
						sql = "UPDATE SORDER	SET STATUS = ?, STATUS_DATE = ? 	WHERE SALE_ORDER = ?";
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1, mStatus.trim());
						pstmt1.setTimestamp(2, currDate);
						pstmt1.setString(3, mSaleOrder.trim());
						llCnt = pstmt1.executeUpdate();
						System.out.println("Number od effect rows in 2nd if  SORDER table =====>"+llCnt);
						if( pstmt1 != null )
						{
							pstmt1.close();
							pstmt1 = null;
						}	
					}
					else
					{
						errString = itmDBAccess.getErrorString("", "VTSOSTAUS", "","",conn);
						return errString;	
					}
					if(errString != null && errString.trim().length() > 0)
					{
						return errString;
					}
				}
			}
		}
		catch(Exception	e)
		{
			System.out.println("SorderCancel.gfAutoClose()==>>"+e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
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
				if( pstmt != null )
				{
					pstmt.close();
					pstmt = null;
				}
			}
			catch( Exception e)
			{
				System.out.println("SorderCancel.gfAutoClose() ====>"+e.getMessage());
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		mStat = mStatus ;
		return errString;
	}
	public String gfAutoCloseExt(String mSaleOrder, String mStat,Connection conn) throws ITMException
	{
		String retString = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		PreparedStatement pstmt1 = null;
		ResultSet rs1 = null;
		String sql1 = "";
		/*PreparedStatement pstmt2 = null;
		ResultSet rs2 = null;
		String sql2 = "";
		PreparedStatement pstmt3 = null;
		ResultSet rs3 = null;
		String sql3 = ""; 
		PreparedStatement pstmt4 = null;
		ResultSet rs4 = null;
		String sql4 =""; 
		int cnt = 0;
		String database = "";
		String userId = "";*/
		String errString = "";
		String lsSaleOrder = "";
		String asLineNo = "";
		String lsExpLev = "";
		String lsUnit = "";
		String lsSiteCode = "";
		String lsStatus = "";
		String lsItemCode = "";
		String lsItemCodeOrd = "";
		String lsLotNo = "";
		String lsLotSl = "";
		String lsLocCode = "";
		double  ldDespatchetQty = 0.0 , ldUserQty = 0.0, ldQtyChk = 0.0 ;
		double ldAllocatedQty = 0.0, ldOrdQty = 0.0;
		Date   mtday = null;
		SimpleDateFormat sdf = null;
		String currDateStr = null;

		try
		{
			sql = "SELECT SUM(QUANTITY) - SUM(QTY_DESP) AS LD_QTY_CHK, SUM(QTY_DESP) AS LD_DESPATCHED_QTY, SUM(QTY_ALLOC) AS LD_ALLOCATED_QTY FROM SORDITEM WHERE  SALE_ORDER = ? AND LINE_TYPE = 'I'";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, mSaleOrder.trim());
			rs = pstmt.executeQuery();
			if( rs.next() )
			{
				ldQtyChk = (Double) rs.getDouble("LD_QTY_CHK") == null ? 0 : rs.getDouble("LD_QTY_CHK");	
				ldDespatchetQty	= rs.getDouble("LD_DESPATCHED_QTY");
				ldAllocatedQty	= rs.getDouble("LD_ALLOCATED_QTY");
			}
			if ( rs != null )
			{
				rs.close();
				rs = null;
			}
			if( pstmt != null )
			{
				pstmt.close();
				pstmt = null;
			}
			if( ldQtyChk > 0 )
			{
				if( ldDespatchetQty > 0 )
				{
					mStat = "C";
				}
				else
				{
					mStat = "X";
				}
			}
			else
			{
				mStat = "C";
			}
			sql = " SELECT SALE_ORDER, LINE_NO, EXP_LEV FROM SORDITEM WHERE  SALE_ORDER = ? AND 	  LINE_TYPE = 'I'";
			pstmt = conn.prepareStatement(sql);

			sql1 = " Select 	SORDITEM.SALE_ORDER, SORDITEM.LINE_NO,	SORDITEM.EXP_LEV," +
					"	SORDALLOC.ITEM_CODE, SORDALLOC.ITEM_CODE__ORD, SORDALLOC.LOT_SL, " +
					" SORDALLOC.LOT_NO,  SORDALLOC.LOC_CODE, SORDALLOC.QUANTITY, SORDALLOC.QTY_ALLOC, " +
					" SORDALLOC.UNIT,  SORDALLOC.STATUS, SORDALLOC.SITE_CODE		 " +
					" from SORDALLOC, " +
					" SORDITEM  		" +
					" where ( SORDALLOC.SALE_ORDER = SORDITEM.SALE_ORDER )" +
					"  and  ( SORDALLOC.LINE_NO = SORDITEM.LINE_NO ) " +
					"  and  ( SORDALLOC.EXP_LEV = SORDITEM.EXP_LEV ) " +
					"  and  ( SORDALLOC.ITEM_CODE__ORD = SORDITEM.ITEM_CODE__ORD ) " +
					"  and  ( SORDALLOC.ITEM_CODE = SORDITEM.ITEM_CODE )  " +
					"  and  ( SORDITEM.SALE_ORDER = ?) " +
					"  and  ( TRIM(SORDITEM.LINE_NO)	= ?)" +
					"  and  ( SORDITEM.EXP_LEV	= ?)";
			pstmt1 = conn.prepareStatement(sql1);

			pstmt.setString(1, mSaleOrder.trim());
			rs = pstmt.executeQuery();
			while( rs.next() )
			{
				lsSaleOrder = checkNull(rs.getString("SALE_ORDER"));
				asLineNo = checkNull(rs.getString("LINE_NO"));
				lsExpLev = checkNull(rs.getString("EXP_LEV"));

				pstmt1.setString(1, lsSaleOrder.trim());
				pstmt1.setString(2, asLineNo);
				pstmt1.setString(3, lsExpLev);
				rs1 = pstmt1.executeQuery();
				while( rs1.next() )
				{
					lsSaleOrder = checkNull(rs1.getString("SALE_ORDER"));
					asLineNo = (rs1.getString("LINE_NO"));//REMOVED CHECK NULL BY NANDKUMAR GADKARI ON 02/01/20
					lsExpLev = checkNull(rs1.getString("EXP_LEV"));
					lsItemCode = checkNull(rs1.getString("ITEM_CODE"));
					lsItemCodeOrd = checkNull(rs1.getString("ITEM_CODE__ORD"));
					lsLotSl =checkNull( rs1.getString("LOT_SL"));
					lsLotNo = checkNull(rs1.getString("LOT_NO"));
					lsLocCode = checkNull(rs1.getString("LOC_CODE"));
					ldOrdQty = rs1.getDouble("QUANTITY");
					ldAllocatedQty = (Double)rs1.getDouble("QTY_ALLOC") == null ? 0 : rs1.getDouble("QTY_ALLOC");//SET RS1 BY NANDKUMAR GADKARI ON 02/01/20
					lsUnit = checkNull(rs1.getString("UNIT"));
					lsStatus = checkNull(rs1.getString("STATUS"));
					lsSiteCode = checkNull(rs1.getString("SITE_CODE"));

					if(ldAllocatedQty < 0)
					{
						ldAllocatedQty = 0;
					}
					ldUserQty = ldAllocatedQty * -1 ;
					errString = gbfAllocated(lsSaleOrder,asLineNo, lsSiteCode, lsItemCode, lsLocCode, lsLotNo, lsLotSl, ldUserQty, lsExpLev, lsItemCodeOrd, conn);
					if( errString != null && errString.trim().length() > 0)
					{
						return errString;
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
		}
		catch(Exception e)
		{
			System.out.println("Exception e ---["+e.getMessage()+"]");
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
			}
			catch(Exception e)
			{
				System.out.println("Exception e ---["+e.getMessage()+"]");
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return retString;
	}
	public String gbfAllocated(String lsSaleOrder,String asLineNo, String lsSiteCode,String lsItemCode, String lsLocCode, String lsLotNo, String lsLotSl,double adQty, String lsExpLev, String lsItemCodeOrd,Connection conn) throws ITMException
	{
		String  errCode = "",    lsGrade = "",    lssiteCodeMfg = "", munitStd =  "", ldtExpDateStr = "",
				lsCustCode = "", lsLocGroup = "", lsAllocFlag = "",  ldtMfgDateStr = "";
		double  ldQtyAllocated = 0,  mconvQtyStduom = 0,   mqtyStk = 0, mallocQty = 0, minputQty, mnetQty = 0, lcHoldQty = 0;
		Date ldToday ;
		Timestamp ldtExpDate = null, ldtMfgDate = null, currDate = null;
		int count = 0, effRow = 0;
		boolean isError = false;
		SimpleDateFormat sdf = null;
		String currDateStr = null;

		HashMap invallocTraceMap = new HashMap();
		InvAllocTraceBean allocTraceBean = new InvAllocTraceBean();
		E12GenericUtility genericUtility = null;
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		PreparedStatement pstmt1 = null;
		ResultSet rs1 = null;
		String sql1 = "";
		PreparedStatement pstmt2 = null;
		ResultSet rs2 = null;
		String sql2 = "";
		PreparedStatement pstmt3 = null;
		ResultSet rs3 = null;
		String sql3 = ""; 
		PreparedStatement pstmt4 = null;
		ResultSet rs4 = null;
		String sql4 =""; 
		int cnt = 0;
		try
		{
			genericUtility = new E12GenericUtility();
			ldToday = new Date();
			String applDateFormat = genericUtility.getApplDateFormat();
			sdf = new SimpleDateFormat(applDateFormat);
			currDateStr = sdf.format(ldToday);
			System.out.println("currDateStr ["+ currDateStr + "]");
			currDate = java.sql.Timestamp.valueOf( genericUtility.getValidDateString( currDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()+ " 00:00:00.00"));
			System.out.println("adQty--["+adQty+"]");
			if( adQty < 0)
			{
				sql = " SELECT COUNT(1) " +
						" FROM   SORDALLOC	" +
						" WHERE	 SALE_ORDER	=	? " +
						" AND	 LINE_NO		=	? " +
						" AND	 SITE_CODE	=	?	  " +
						" AND	 ITEM_CODE	=	?	" +
						" AND	 LOC_CODE	=	?	" +
						"	AND	 LOT_NO		=	?	" +
						"	AND	 LOT_SL		=	?	" +
						"	AND	 EXP_LEV	=	?	" +
						"	AND	 ITEM_CODE__ORD = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsSaleOrder);
				pstmt.setString(2, asLineNo);
				pstmt.setString(3, lsSiteCode);
				pstmt.setString(4, lsItemCode);
				pstmt.setString(5, lsLocCode);
				pstmt.setString(6, lsLotNo);
				pstmt.setString(7, lsLotSl);
				pstmt.setString(8, lsExpLev);
				pstmt.setString(9, lsItemCodeOrd);
				rs = pstmt.executeQuery();
				if( rs.next() )
				{
					count = rs.getInt(1);
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
				if( count == 0)
				{
					errCode = itmDBAccessLocal.getErrorString("", "VALCERR", "","",conn);
					return errCode;
				}
			}
			sql = " UPDATE SORDITEM " +
					" SET	QTY_ALLOC	= 	QTY_ALLOC + ? " +
					" WHERE	SALE_ORDER	= ? " +
					" AND	LINE_NO	= ? " +
					" AND	 EXP_LEV =	? " +
					" AND   LINE_TYPE   = 'I'";
			pstmt = conn.prepareStatement(sql);
			pstmt.setDouble(1, adQty);
			pstmt.setString(2, lsSaleOrder);
			pstmt.setString(3, asLineNo);
			pstmt.setString(4, lsExpLev);
			effRow = pstmt.executeUpdate();
			System.out.println("Update Sorder Item "+effRow);
			if(pstmt != null)
			{
				pstmt.close();
				pstmt = null;
			}

			sql = " SELECT QTY_ALLOC AS LD_QTY_ALLOCATED" +
					" FROM	SORDALLOC" +
					" WHERE	SALE_ORDER	=	? " +
					" AND	  LINE_NO		=	? " +
					" AND	 SITE_CODE	=	? " +
					" AND	 ITEM_CODE	=	? " +
					" AND	 LOC_CODE	=	? " +
					" AND	 LOT_NO		=	?  " +
					" AND	 LOT_SL		=	? " +
					" AND	 EXP_LEV	=	? " +
					" AND	 ITEM_CODE__ORD = ?";
			pstmt = conn.prepareStatement(sql);

			sql1 = " DELETE FROM	SORDALLOC" +
					" WHERE	 SALE_ORDER	=	? " +
					" AND	 LINE_NO =	? " +
					" AND	 SITE_CODE =	? " +
					" AND	 ITEM_CODE	=	? " +
					" AND	 LOC_CODE	=	? " +
					" AND	 LOT_NO		=	?  " +
					" AND	 LOT_SL		=	? " +
					" AND	 EXP_LEV	=	? " +
					" AND	 ITEM_CODE__ORD = ?";
			pstmt1 = conn.prepareStatement(sql1);

			sql2 = " UPDATE	SORDALLOC  " +
					" SET	 QTY_ALLOC	= 	QTY_ALLOC + ? " +
					" WHERE	 SALE_ORDER	=	? " +
					" AND	 LINE_NO		=	? " +
					" AND	 SITE_CODE	=	? " +
					" AND	 ITEM_CODE	=	? " +
					" AND	 LOC_CODE	=	? " +
					" AND	 LOT_NO		=	?  " +
					" AND	 LOT_SL		=	? " +
					" AND	 EXP_LEV	=	? " +
					" AND	 ITEM_CODE__ORD = ?";
			pstmt2 = conn.prepareStatement(sql2);

			pstmt.setString(1, lsSaleOrder);
			pstmt.setString(2, asLineNo);
			pstmt.setString(3, lsSiteCode);
			pstmt.setString(4, lsItemCode);
			pstmt.setString(5, lsLocCode);
			pstmt.setString(6, lsLotNo);
			pstmt.setString(7, lsLotSl);
			pstmt.setString(8, lsExpLev);
			pstmt.setString(9, lsItemCodeOrd);
			rs = pstmt.executeQuery();
			if( rs.next() )
			{
				ldQtyAllocated = rs.getDouble("LD_QTY_ALLOCATED");

				if((ldQtyAllocated + adQty) == 0)
				{

					pstmt1.setString(1, lsSaleOrder);
					pstmt1.setString(2, asLineNo);
					pstmt1.setString(3, lsSiteCode);
					pstmt1.setString(4, lsItemCode);
					pstmt1.setString(5, lsLocCode);
					pstmt1.setString(6, lsLotNo);
					pstmt1.setString(7, lsLotSl);
					pstmt1.setString(8, lsExpLev);
					pstmt1.setString(9, lsItemCodeOrd);
					effRow = pstmt1.executeUpdate();
					System.out.println("Delete record from sordalloc ==> "+effRow);
					pstmt1.clearParameters();
				}
				else
				{

					pstmt2.setDouble(1, adQty);
					pstmt2.setString(2, lsSaleOrder);
					pstmt2.setString(3, asLineNo);
					pstmt2.setString(4, lsSiteCode);
					pstmt2.setString(5, lsItemCode);
					pstmt2.setString(6, lsLocCode);
					pstmt2.setString(7, lsLotNo);
					pstmt2.setString(8, lsLotSl);
					pstmt2.setString(9, lsExpLev);
					pstmt2.setString(10, lsItemCodeOrd);
					effRow = pstmt2.executeUpdate();
					System.out.println("Update record from sordalloc ==> "+effRow);
					pstmt2.clearParameters();
				}
			}
			else
			{
				sql = "SELECT CUST_CODE,LOC_GROUP  FROM SORDER	WHERE SALE_ORDER = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsSaleOrder);
				rs = pstmt.executeQuery();
				if( rs.next() )
				{
					lsCustCode = checkNull(rs.getString("CUST_CODE"));
					lsLocGroup = checkNull(rs.getString("LOC_GROUP"));
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
				if( lsLocGroup == null || lsLocGroup.trim().length() == 0)
				{
					sql = " SELECT A.EXP_DATE AS LDT_EXP_DATE, A.GRADE AS LS_GRADE, A.MFG_DATE AS LDT_MFG_DATE, " +
							" A.SITE_CODE__MFG AS LS_SITE_CODE__MFG, A.QUANTITY AS MQTY_STK, A.ALLOC_QTY AS MALLOC_QTY, " +
							" A.HOLD_QTY AS LC_HOLD_QTY		" +
							" FROM STOCK A 		" +
							" WHERE A.ITEM_CODE = ? " +
							" AND   A.SITE_CODE = ? " +
							"	AND	A.LOC_CODE	= ?	" +
							"	AND	A.LOT_NO = ?	" +
							"	AND	A.LOT_SL = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsItemCode);
					pstmt.setString(2, lsSiteCode);
					pstmt.setString(3, lsLocCode);
					pstmt.setString(4, lsLotNo);
					pstmt.setString(5, lsLotSl);
					rs = pstmt.executeQuery();
					if( rs.next() )
					{
						ldtExpDateStr = checkNull(rs.getString("LDT_EXP_DATE"));
						lsGrade = checkNull(rs.getString("LS_GRADE"));
						ldtMfgDateStr = checkNull(rs.getString("LDT_MFG_DATE"));
						lssiteCodeMfg = checkNull(rs.getString("LS_SITE_CODE__MFG"));
						mqtyStk	=  rs.getDouble("MQTY_STK");
						mallocQty = rs.getDouble("MALLOC_QTY");
						lcHoldQty = rs.getDouble("LC_HOLD_QTY");
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
				else
				{
					sql = " SELECT A.EXP_DATE AS LDT_EXP_DATE, A.GRADE AS LS_GRADE, A.MFG_DATE AS LDT_MFG_DATE, " +
							" A.SITE_CODE__MFG AS LS_SITE_CODE__MFG, A.QUANTITY AS MQTY_STK, A.ALLOC_QTY AS MALLOC_QTY," +
							" A.HOLD_QTY AS LC_HOLD_QTY	" +
							" FROM STOCK A ,	 LOCATION B " +
							" WHERE A.LOC_CODE  =  B.LOC_CODE	" +
							"	AND	B.LOC_GROUP = ?		" +
							" AND	A.ITEM_CODE = ?		" +
							" AND A.SITE_CODE = ?		" +
							" AND	A.LOC_CODE	= ?		" +
							" AND	A.LOT_NO	= ?		" +
							" AND	A.LOT_SL	= ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsLocGroup);
					pstmt.setString(2, lsItemCode);
					pstmt.setString(3, lsSiteCode);
					pstmt.setString(4, lsLocCode);
					pstmt.setString(5, lsLotNo);
					pstmt.setString(6, lsLotSl);
					rs = pstmt.executeQuery();
					if( rs.next() )
					{
						ldtExpDateStr = checkNull(rs.getString("LDT_EXP_DATE"));
						lsGrade = checkNull(rs.getString("LS_GRADE"));
						ldtMfgDateStr = checkNull(rs.getString("LDT_MFG_DATE"));
						lssiteCodeMfg = checkNull(rs.getString("LS_SITE_CODE__MFG"));
						mqtyStk	=  rs.getDouble("MQTY_STK");
						mallocQty = rs.getDouble("MALLOC_QTY");
						lcHoldQty = (Double) rs.getDouble("LC_HOLD_QTY") == null ? 0 : rs.getDouble("LC_HOLD_QTY");
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

					sql = "SELECT UNIT__STD, CONV__QTY_STDUOM 	FROM SORDDET	WHERE SALE_ORDER = ?	AND LINE_NO = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsSaleOrder);
					pstmt.setString(2, asLineNo);
					rs = pstmt.executeQuery();
					if( rs.next() )
					{
						munitStd = checkNull(rs.getString("UNIT__STD"));
						mconvQtyStduom = rs.getInt("CONV__QTY_STDUOM");
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

					sql = " SELECT QUANTITY FROM	SORDITEM 	WHERE	SALE_ORDER	= ?	AND	LINE_NO	= ? 	AND	 EXP_LEV	=	 ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsSaleOrder);
					pstmt.setString(2, asLineNo);
					pstmt.setString(3, lsExpLev);
					rs = pstmt.executeQuery();
					if( rs.next() )
					{
						mnetQty = rs.getDouble("QUANTITY");
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
					if( (mqtyStk - mallocQty - lcHoldQty) <=  mnetQty)
					{
						minputQty = mqtyStk - mallocQty - lcHoldQty;
					}
					else
					{
						minputQty = mnetQty;
					}

					ldtMfgDate = java.sql.Timestamp.valueOf( genericUtility.getValidDateString( ldtMfgDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()+ " 00:00:00.00"));
					ldtExpDate = java.sql.Timestamp.valueOf( genericUtility.getValidDateString( ldtExpDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()+ " 00:00:00.00"));
					sql = " INSERT INTO SORDALLOC(SALE_ORDER,	LINE_NO,   EXP_LEV,   ITEM_CODE__ORD,   ITEM_CODE,  " +
							" LOT_NO,   LOT_SL,   LOC_CODE,  ITEM_REF,   QUANTITY,   UNIT,   QTY_ALLOC,   DATE_ALLOC,  " +
							" STATUS,   ALLOC_MODE,   SITE_CODE,	ITEM_GRADE,	EXP_DATE,	MFG_DATE,	SITE_CODE__MFG,	" +
							" UNIT__STD,CONV__QTY_STDUOM,	QUANTITY__STDUOM  ) " +
							" SELECT SORDITEM.SALE_ORDER, SORDITEM.LINE_NO,   SORDITEM.EXP_LEV,  SORDITEM.ITEM_CODE__ORD, " +
							" SORDITEM.ITEM_CODE,	? , ?, ?, SORDITEM.ITEM_REF, SORDITEM.QUANTITY,   SORDITEM.UNIT,   ?, ?, 'P','M', " +
							" ?, ?, ?,	?, ?,	?, ?, ?	" +
							" FROM  sorditem	WHERE sale_order = ?	" +
							" and	line_no	 = ?" +
							"	AND EXP_LEV	 = ?" +
							"	and line_type = 'I'";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsLotNo);
					pstmt.setString(2, lsLotSl);
					pstmt.setString(3, lsLocCode);
					pstmt.setDouble(4, adQty);
					pstmt.setTimestamp(5, currDate);
					pstmt.setString(6, lsSiteCode);
					pstmt.setString(7, lsGrade);
					pstmt.setTimestamp(8, ldtExpDate);
					pstmt.setTimestamp(9, ldtMfgDate);
					pstmt.setString(10, lssiteCodeMfg);
					pstmt.setString(11, munitStd);
					pstmt.setDouble(12, mconvQtyStduom);
					pstmt.setDouble(13, minputQty);
					effRow = pstmt.executeUpdate();
					System.out.println("SorderCancel.gbfAllocated() Insert no of row [ "+ effRow + "] in SORDALLOC table");
					if(pstmt != null)
					{
						pstmt.close();
						pstmt = null;
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

			if(pstmt1 != null)
			{
				pstmt1.close();
				pstmt1 = null;
			}
			if(pstmt2 != null)
			{
				pstmt2.close();
				pstmt2 = null;
			}

			if( adQty != 0 || adQty != 0.0) 
			{
				invallocTraceMap.put("tran_date", currDate);
				invallocTraceMap.put("ref_ser", "S-ORD");
				invallocTraceMap.put("ref_id", lsSaleOrder);
				invallocTraceMap.put("ref_line", asLineNo);
				invallocTraceMap.put("site_code", lsSiteCode);
				invallocTraceMap.put("item_code", lsItemCode);
				invallocTraceMap.put("loc_code", lsLocCode);
				invallocTraceMap.put("lot_no", lsLotNo);
				invallocTraceMap.put("lot_sl", lsLotSl);
				invallocTraceMap.put("alloc_qty", adQty);
				invallocTraceMap.put("chg_user", userId);
				invallocTraceMap.put("chg_term", termId);
				invallocTraceMap.put("chg_win", "W_SORDER");
				System.out.println("invallocTraceMap =====> ["+invallocTraceMap);
				errCode	= allocTraceBean.updateInvallocTrace(invallocTraceMap, conn);
				System.out.println("AsnConf.confirm() error_Code  ::"+errCode);
				invallocTraceMap.clear();
				if(errCode != null && errCode.trim().length() > 0)
				{
					return errCode;
				}
			}

			count = 0;
			sql = "select COUNT(1) FROM  SORDITEM where	SALE_ORDER	=	? and	(case when QTY_ALLOC is null then 0 else QTY_ALLOC end) > 0";//FROM KEY WORD ADDED BY NANDKUMAR GADKARI ON 02/01/20
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, lsSaleOrder);
			rs = pstmt.executeQuery();
			if( rs.next() )
			{
				count =	rs.getInt(1);
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
			if( count > 0)
			{
				lsAllocFlag = "Y";
			}
			else if( count == 0 )
			{
				lsAllocFlag = null;
			}
			effRow = 0;
			sql = "UPDATE SORDER SET ALLOC_FLAG = ? WHERE SALE_ORDER = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, lsAllocFlag);
			pstmt.setString(2, lsSaleOrder);
			effRow = pstmt.executeUpdate();
			System.out.println("Effected row in Sorder  ["+effRow+"] in Table");
			if(pstmt != null)
			{
				pstmt.close();
				pstmt = null;
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception--["+e.getMessage()+"]");
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{

			}
			catch(Exception e)
			{
				System.out.println("Exception--["+e.getMessage()+"]");
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return errCode;
	}
	public String sorderStatusLog(String tranId, Timestamp tod, double evtype, String xtraParams,String lineno,String explev,String reascode,String refdescr) throws  RemoteException,ITMException
	{
		int lskey = 0;
		Timestamp  todayDate = null ;
		String sql = "",siteCode= "";
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		ibase.utility.E12GenericUtility genericUtility= null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		String errString = "" ;
		String generatedtranId = "";
		String chgTerm = "",chgUser = "";
		int rows = 0;
		String edioption = "";
		Connection conn = null;
		String loginCode="",transDB="";//added by nandkumar gadkari on 19/12/19
		try
		{
			//conn = getConnection();//COMMENTED  by nandkumar gadkari on 19/12/19
			//added by nandkumar gadkari on 19/12/19--------------SATART----------for call from sorderclose process
			genericUtility = new  ibase.utility.E12GenericUtility();
			loginCode=genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
	    	DBAccessEJB dbAccess = new DBAccessEJB();
	    	UserInfoBean userInfo = dbAccess.createUserInfo(loginCode);
	    	transDB       = userInfo.getTransDB();
			ConnDriver connDriver = new ConnDriver();
			conn = connDriver.getConnectDB(transDB);
			if(termId== null ||userId== null || termId.trim().length() ==0 || userId.trim().length() ==0 )
			{
				termId =loginCode;
				userId = termId;
			}
			//added by nandkumar gadkari on 19/12/19-------------------END
			System.out.println("@@@@@ :: sorderStatusLog :::: called :::: ");
		//	genericUtility = new  ibase.utility.E12GenericUtility();
			itmDBAccessEJB = new ITMDBAccessEJB();
			chgTerm =  genericUtility.getValueFromXTRA_PARAMS( xtraParams, "CHG_TERM" );
			chgUser =  genericUtility.getValueFromXTRA_PARAMS( xtraParams, "CHG_USER" );
			java.util.Date dt = new java.util.Date();
			SimpleDateFormat sdf1= new SimpleDateFormat(genericUtility.getDBDateFormat());
			todayDate = java.sql.Timestamp.valueOf(sdf1.format(dt)+" 00:00:00.0");
			sql = "select site_code  from sorder where sale_order = ?" ; 
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,tranId);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				siteCode = checkNull(rs.getString("site_code"));
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
			lskey = 0;
			sql = "select count(key_string)  from transetup where tran_window = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,"w_sorder_stat_log");
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				lskey = rs.getInt(1) ;
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

			if(lskey == 0)
			{
				sql = "select count(key_string) from transetup where tran_window = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,"GENERAL");
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					if(rs.getInt(1) > 0);
					{
						System.out.println("-- key_string found in general --");
						errString = itmDBAccessEJB.getErrorString("","DS000","","",conn);
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
			generatedtranId = generateTranId( "w_sorder_stat_log",siteCode, conn );
			sql = " insert into sorder_stat_log values(?,?,?,?,?,?,?,?,?,?,?)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,generatedtranId);
			pstmt.setString(2,tranId);
			pstmt.setTimestamp(3,tod);
			pstmt.setDouble(4,evtype);
			pstmt.setString(5,lineno);
			pstmt.setString(6,explev);
			pstmt.setString(7,reascode);
			pstmt.setString(8,refdescr);
			pstmt.setTimestamp(9,todayDate);
			pstmt.setString(10,userId);
			pstmt.setString(11,termId);
			rows = pstmt.executeUpdate();
			if(pstmt != null)
			{
				pstmt.close();
				pstmt = null;
			}
			if(rows > 0)
			{
				if(tranId != null)
				{
					/*sql = " select edi_option from transetup where tran_window = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,"w_sorder_stat_log");
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						edioption = rs.getString(1);
					    String dataStr = "";
					    if("1".equalsIgnoreCase(edioption.trim()))
					    {
					    	CreateRCPXML createRCPXML = new CreateRCPXML("w_sorder_stat_log","tran_id");
							dataStr = createRCPXML.getTranXML( tranId, conn );
							System.out.println( "dataStr =[ "+ dataStr + "]" );
							Document ediDataDom = genericUtility.parseString(dataStr);
							E12CreateBatchLoadEjb e12CreateBatchLoad = new E12CreateBatchLoadEjb();
							String retString = e12CreateBatchLoad.createBatchLoad(ediDataDom, "w_sorder_stat_log", "0", xtraParams, conn );
							createRCPXML = null;
							e12CreateBatchLoad = null;
							if( retString != null && retString.indexOf("SUCCESS") > -1 )
							{
								System.out.println("retString from batchload ["+retString+"]");
							}
					    }

					}
					else
					{
						System.out.println(" edi option !found in transetup for w_sorder_stat_log ");
						errString = itmDBAccessEJB.getErrorString("","DS000","","",conn);
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
					}*/
				}
			}
			else
			{
				System.out.println("-- not insert into sorder_stat_log --");
				errString = itmDBAccessEJB.getErrorString("","DS000","","",conn);
			}
		}
		catch(Exception e)
		{
			try
			{
				if( errString != null && errString.trim().length() >  0 )
				{
					conn.rollback();
					System.out.println("--Transaction rollback in catch--");
				}
				System.out.println("Exception.. "+e.getMessage());
				e.printStackTrace();	
				errString=e.getMessage();
				throw new ITMException(e);
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
				errString=e.getMessage();
				throw new ITMException(e1);
			}			
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

				if(errString != null && errString.trim().length() >  0)
				{
					conn.rollback();
					//Added by Jaffar S. on 03-01-19 for closing connection
					conn.close();
					conn = null;
				}
				else
				{
					conn.commit();
					//Added by Jaffar S. on 03-01-19 for closing connection
					conn.close();
					conn = null;
				}
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
				errString=e1.getMessage();
				throw new ITMException(e1);
			}	
		}

		return errString;
	}
	private String generateTranId( String windowName,String siteCode, Connection conn )throws  RemoteException,ITMException
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String selSql = "";
		String tranId = "";
		String tranSer = "";
		String keyString = "";
		String keyCol = "";
		String xmlValues = "";
		java.sql.Timestamp currDate = null;
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		try
		{

			SimpleDateFormat sdfAppl = new SimpleDateFormat(genericUtility.getApplDateFormat());

			currDate = new java.sql.Timestamp(System.currentTimeMillis());
			String currDateStr = sdfAppl.format(currDate);

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
			catch(Exception e)
			{
				System.out.println("Exception ::" + e.getMessage() + ":");
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		System.out.println("@@@@@@@@@@@@@@@@@@@@@@tranId[[[[[[[[[[["+tranId+"]]]]]]]]]]]]]]]");
		return tranId;
	}
	public String checkNull(String inputStr) throws ITMException
	{
		String retString = "";
		try
		{
			retString = inputStr;
			
			if(retString == null || retString.trim().length() == 0)
			{
				retString = "";
			}
			else
			{
				retString = retString.trim();
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception in checknull--["+e.getMessage()+"]");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return retString;
	}
}
