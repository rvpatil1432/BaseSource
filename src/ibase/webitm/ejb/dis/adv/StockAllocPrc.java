/********************************************************
	Title : StockAllocPrc
	Date  : 04/12/2011
	Developer: Dipak Chattar

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
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import javax.ejb.Stateless;
import javax.naming.PartialResultException;

@Stateless
public class StockAllocPrc extends ActionHandlerEJB implements StockAllocPrcLocal, StockAllocPrcRemote//SessionBean {
{
	// METHOD  ADDED BY RITESH ON 18/07/14
	public String confirm(String tranId,String xtraParams,String forcedFlag) throws RemoteException,ITMException
	{
		Connection conn=null;
		String errString="";
		errString=confirm(tranId,xtraParams,forcedFlag,conn);
		System.out.println("errString----"+errString);
		return errString;

	}
	
	public String confirm(String tranId,String xtraParams, String forcedFlag,Connection conn) throws RemoteException,ITMException
	{
		System.out.println("--confirm(String tranId,String xtraParams, String forcedFlag,Connection conn)");
		//Connection conn = null;
		PreparedStatement pstmt = null, pstmt1 = null ;
		ResultSet rs = null, rs1 = null;
		String sql = "";
		String confirm = "",batchId="";
		String errString = "" ; 
		ConnDriver connDriver = null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		double availQty =0.0;//Added by sumit on 21/12/12
		
		// ADDED ON 02/MAY/2014
		String sqlSorditem = "";
		PreparedStatement psmtSord = null;
		boolean isLocal=false;	
		try
		{
			itmDBAccessEJB = new ITMDBAccessEJB();
			//  ADDED BY RITESH ON 18/07/14 START
			if(conn==null)
			{
				connDriver = new ConnDriver();
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
				connDriver = null;
				conn.setAutoCommit(false);
				isLocal=true;
			}
//			// METHOD  ADDED BY RITESH ON 18/07/14 END
//			connDriver = new ConnDriver();
//			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
//			connDriver = null;
//			conn.setAutoCommit(false);

			sql = "select confirmed,site_code,BATCH_ID from sord_alloc where tran_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,tranId);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				confirm = rs.getString("confirmed");
				batchId = rs.getString("BATCH_ID");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("BATCH ID while validation ["+batchId+"]");
			if(confirm != null  && "Y".equalsIgnoreCase(confirm))
			{
				System.out.println("The Selected transaction is already confirmed");
				errString = itmDBAccessEJB.getErrorString("","VTMCONF1","","",conn);
				return errString;
			}
			else
			{
				
				//Changed by sumit on 21/12/12 checking sord_alloc_det quantity start.
				//errString = this.sorderAllocate(tranId,xtraParams,conn);
				sql = "SELECT SALE_ORDER, LINE_NO__SORD, SUM(QUANTITY) FROM SORD_ALLOC_DET WHERE TRAN_ID = ? " +
						" GROUP BY SALE_ORDER, LINE_NO__SORD ORDER BY SALE_ORDER, LINE_NO__SORD";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				
				
				sql = "SELECT (( QUANTITY * CONV__QTY_STDQTY ) - QTY_ALLOC) AS TOBEALLOC FROM SORDITEM WHERE SALE_ORDER = ? AND LINE_NO = ?";   // modify by cpatil on 29/06/13
				//sql = "SELECT ( QUANTITY  - QTY_ALLOC) AS TOBEALLOC FROM SORDITEM WHERE SALE_ORDER = ? AND LINE_NO = ?";
				pstmt1 = conn.prepareStatement(sql);
				
				while( rs.next())
				{
					// ADDED BY RITESH ON 02/MAY/2014 START
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
					
					// ADDED BY RITESH ON 02/MAY/2014 END
					
					pstmt1.setString(1, rs.getString("SALE_ORDER"));
					pstmt1.setString(2, rs.getString("LINE_NO__SORD"));
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						availQty = rs1.getDouble("TOBEALLOC");
					}
					rs1.close();rs1 = null;
					pstmt1.clearParameters();
					
					System.out.println("["+rs.getDouble(3)+"] SUM(QUANTITY) in sord_alloc_det and avilqty ["+availQty+"]");
					//Changed by Rohan on 13-06-13 for updating sorditem after checking validation.start
					/*
					if( rs.getDouble(3) <= availQty )
					{
						errString = this.sorderAllocate(tranId,xtraParams,conn);
					}					
					else
					{
						errString = itmDBAccessEJB.getErrorString("","INVQUANTY","","",conn);
						return errString;
					}
					*/
					if( rs.getDouble(3) > availQty )
					{
						
						errString = itmDBAccessEJB.getErrorString("","INVQUANTY","","",conn);
						return errString;
					}
					//Changed by Rohan on 13-06-13 for updating sorditem after checking validation.end
				}
				
				if(pstmt1 != null)
				{
					pstmt1.close();
					pstmt1 = null;
				}
				pstmt.close();pstmt = null;
				rs.close();rs = null;
				//Changed by sumit on 21/12/12 checking sord_alloc_det quantity end.
				
				if(errString != null && errString.trim().length() > 0)
				{
					return errString;
				}
				//Changed by Rohan on 13-06-13 for updating sorditem aftchecking validation.start
				else
				{
					
					errString = this.sorderAllocate(tranId,xtraParams,conn);
				}
				//Changed by Rohan on 13-06-13 for updating sorditem aftchecking validation.end

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
				if(errString != null && errString.trim().length() > 0)
				{
					if(errString.indexOf("CONFSUCCES") > -1)
					{
						if(isLocal)
						{
						conn.commit();
						System.out.println("Transaction commited while  StockAllocPrc ==");

						}
					}
					else
					{
						conn.rollback();
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
				//Changed by sumit on 21/12/12 start
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
				//Changed by sumit on 21/12/12 end
				if(conn!=null && isLocal)
				{
					conn.close();
					conn = null;
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

	private	String sorderAllocate( String tranId, String xtraParams, Connection conn )throws RemoteException,ITMException
	{
		System.out.println("updateSOrder calling ..............xcdfffhh");
		String getDataSql = null;
		String saleOrder = null;
		String lineNoo = null;
		String tranIDD = null;
		String sorditemSql=null;
		String itemCodeOrd = null ;
		String unitStd = null ;
		String lotSl= null;
		String lotNo = null;
		String locCode = null;
		String unit = null;
		String grade = null;
		String siteCodeMfg =null;
		String itemRef = null;
		String lineNoSord = null;
		String itemCode = null;
		//String siteCode = null;
		String siteCodeShip = null;  // change by cpatil for siteCode to siteCodeShip 
		Timestamp expDate1 = null,mfgDate1 = null;   //add by ritesh 
		String expLev = null;
		String updateSorditem = null,sqlst=null;
		String sordAllocSql = null;
		String insertSql = null;
		String updateSql= null;
		String updateSql1= null;
		String empCode = "";
		String dateNow ="";
		String errString = "",batchId="";
		int count1 = 0 ;
		int count2 = 0 ;
		double qtyToBeAllocated = 0d;
		double quantity =0;
		double convQtyStduom = 0 ;
		double quantityStduom = 0 ;
		double pendingQuantity = 0;
		java.sql.Date expDate = new java.sql.Date(System.currentTimeMillis());
		java.sql.Date mfgDate = new java.sql.Date(System.currentTimeMillis());
		java.sql.Date dateAlloc= new java.sql.Date(System.currentTimeMillis());
		

		PreparedStatement pstmt = null,pstmtStk=null;
		PreparedStatement pSordAllocDet = null;
		PreparedStatement pSelSordItem = null;
		PreparedStatement pUpdSordItem = null;
		PreparedStatement pSelSordAlloc = null;
		PreparedStatement pUpdSordAlloc = null;
		PreparedStatement pInsSordAlloc = null;

		ResultSet rs = null ,rsst=null;
		ResultSet rsTemp = null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		
		
		 
		InvAllocTraceBean invAllocTrace = new InvAllocTraceBean();
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		HashMap strAllocate = null;
		try
		{
			String dbDateFormat = genericUtility.getDBDateFormat();
			String applDateFormat = genericUtility.getApplDateFormat();
			itmDBAccessEJB = new ITMDBAccessEJB();
			java.util.Date currDate1 = new java.util.Date();
			SimpleDateFormat sdf = new SimpleDateFormat(applDateFormat);
			String currDateStr = sdf.format(currDate1);
			java.sql.Timestamp tranDate = java.sql.Timestamp.valueOf(genericUtility.getValidDateString( currDateStr , applDateFormat, dbDateFormat ) + " 00:00:00.0") ;



			sorditemSql = "SELECT ITEM_CODE,ITEM_CODE__ORD,UNIT, ITEM_REF"
					//+" QUANTITY - QTY_DESP PENDING_QUANTITY "              // remove comment by cpatil on 03/07/13
					+" FROM SORDITEM WHERE SALE_ORDER = ? "
					+" AND LINE_NO = ? "
					+" AND EXP_LEV = ? ";
			//System.out.println("sorditemSql:::"+sorditemSql);
			pSelSordItem = conn.prepareStatement(sorditemSql);

			updateSorditem ="UPDATE SORDITEM  SET QTY_ALLOC = CASE WHEN QTY_ALLOC IS NULL THEN 0 ELSE QTY_ALLOC END  +  ? " 
					+" WHERE SALE_ORDER = ? "
					+" AND LINE_NO = ? "
					+" AND EXP_LEV = ? ";
			
			pUpdSordItem = conn.prepareStatement(updateSorditem);

			sordAllocSql = "SELECT COUNT(1) FROM SORDALLOC " 
					+ " WHERE SALE_ORDER = ? "
					+ " AND LINE_NO = ? "
					+ " AND EXP_LEV = ? "
					+ " AND ITEM_CODE__ORD = ? "
					+ " AND ITEM_CODE = ? "
					+ " AND LOT_NO = ? "
					+ " AND LOT_SL = ? "
					+ " AND LOC_CODE = ? " ;
			pSelSordAlloc = conn.prepareStatement(sordAllocSql);

			updateSql = "UPDATE SORDALLOC"
					+ " SET QTY_ALLOC =  QTY_ALLOC + ? "
					//+ " BATCH_ID=?  "		//Changed by Pankaj R on 18/02/15.
					+ " WHERE SALE_ORDER = ? "
					+ " AND LINE_NO = ? "
					+ " AND EXP_LEV = ? "
					+ " AND ITEM_CODE__ORD = ? "
					+ " AND ITEM_CODE = ? "
					+ " AND LOT_NO = ? "
					+ " AND LOT_SL = ? "
					+ " AND LOC_CODE = ? ";
			pUpdSordAlloc = conn.prepareStatement(updateSql);
			System.out.println("****UPDATE sql at confirm :-"+updateSql);
			
			insertSql ="INSERT INTO SORDALLOC (SALE_ORDER,LINE_NO,EXP_LEV,ITEM_CODE__ORD,SITE_CODE ,"
					+"ITEM_CODE,QUANTITY ,LOT_NO, LOT_SL, LOC_CODE, UNIT, QTY_ALLOC,"
					+"ITEM_REF, DATE_ALLOC, STATUS,ITEM_GRADE, EXP_DATE, ALLOC_MODE, "
					+" CONV__QTY_STDUOM, UNIT__STD, QUANTITY__STDUOM, "
					+"MFG_DATE, SITE_CODE__MFG, REF_ID__ALLOC, REF_LINE__NO,"//REF_ID__ALLOC, REF_LINE__NO added by deepak sawant (24/10/13)
					+ " BATCH_ID) "  //VALLABH KADAM INSERT BATCH id in SORDALLOC
					+"VALUES ( ?, ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? ,"
					+" ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?) " ;
			
			pInsSordAlloc = conn.prepareStatement(insertSql);


			getDataSql = "select  d.tran_id ,d.line_no ,d.sale_order ,d.line_no__sord ,d.item_code , "
					+" d.loc_code ,d.lot_no ,d.lot_sl ,d.quantity ,d.dealloc_qty,h.site_code__ship , "        // replace h.site_code__ship instead of  d.site_code to get sitecodeship from header at the time of confirmation 
					//+" d.loc_code ,d.lot_no ,d.lot_sl ,d.quantity ,d.dealloc_qty, d.site_code , "     
					+" d.exp_lev,d.pending_qty,h.BATCH_ID "     // VALLABH KADAM select BATCH id
					+" from  sord_alloc_det d , sord_alloc h "
					+" where d.tran_id = ?  and  h.tran_id =  d.tran_id  "
					+" order by  line_no  asc   "; 
			pSordAllocDet = conn.prepareStatement(getDataSql);
			pSordAllocDet.setString(1,tranId);
			rs = pSordAllocDet.executeQuery();
			System.out.println("tranId------["+ tranId);
			
			int updCount = 0;
			while(rs.next())
			{
				System.out.println( "INSIDE WHILE LOOP.............");
				
				tranIDD = rs.getString(1);   //added by deepak sawant (24/10/13)
				
				lineNoo = rs.getString(2);   //added by deepak sawant (24/10/13)
				
				//SALE_ORDER
				saleOrder = rs.getString(3);
				//LINE_NO__SlineNoSordORD
				lineNoSord = rs.getString(4);
				lineNoSord = "   " + lineNoSord.trim();                        
				lineNoSord = lineNoSord.substring( lineNoSord.length()-3 );
				
//				if(lineNoSord != null)
//				{
//					lineNoSord = lineNoSord.trim();              // added by deepak sawant
//				}
//				System.out.println("lineNoSord batchId=ddddd["+lineNoSord+"]");
				//ITEM_CODE
				itemCode = rs.getString(5);
				//LOC_CODE
				locCode = rs.getString(6);
				//LOT_NO
				lotNo = rs.getString(7);
				//LOT_SL
				lotSl = rs.getString(8);
				//QUANTITY
				quantity = rs.getDouble(9);
				//SITE_CODE
				//siteCode = rs.getString(10);    // chage by cpatil for siteCode to siteCodeShip
				siteCodeShip = rs.getString(11);    // change by cpatil for siteCode to siteCodeShip
				//EXP_LEV
				expLev = rs.getString(12);
				pendingQuantity = rs.getDouble(13);
				batchId = rs.getString(14);        //VALLABH KADAM find BATCH id from sord_alloc for tranId.
				
				System.out.println("********* BATCH ID :- ["+batchId+"] for tranId :- ["+tranId+"]************");

				qtyToBeAllocated = quantity;

				// added by cpatil on 04/07/13
				 sqlst = " SELECT UNIT,CONV__QTY_STDUOM, exp_date ,MFG_DATE " +
				 		//"QUANTITY , quantity__stduom " + 
						" FROM STOCK " +
				 	
						" WHERE ITEM_CODE = ? AND LOC_CODE = ? AND  SITE_CODE = ?  AND LOT_NO = ? AND LOT_SL = ? "; 
				 pstmtStk = conn.prepareStatement(sqlst);
					pstmtStk.setString(1,itemCode);
					pstmtStk.setString(2,locCode);
					//pstmtStk.setString(3,siteCode);    // change by cpatil for siteCode to siteCodeShip
					pstmtStk.setString(3,siteCodeShip);    // change by cpatil for siteCode to siteCodeShip
					pstmtStk.setString(4,lotNo);
					pstmtStk.setString(5,lotSl);
					
					rsst = pstmtStk.executeQuery();
					if(rsst.next())
					{
						unit = rsst.getString("unit");
						convQtyStduom = rsst.getDouble("conv__qty_stduom");
						//quantityStduom = rsst.getDouble("quantity__stduom");
						expDate1 = rsst.getTimestamp("exp_date");
						mfgDate1 = rsst.getTimestamp("MFG_DATE");
					}
					rsst.close();
					rsst = null;
					pstmtStk.close(); 
					pstmtStk = null;
				System.out.println("@@@@ unit["+unit+"]:::convQtyStduom["+convQtyStduom+"]:::quantityStduom["+quantityStduom+"]");
				// end
				
				System.out.print("Allocatio Qty = " + qtyToBeAllocated);

				if (qtyToBeAllocated > 0) 
				{
					strAllocate = new HashMap();
					strAllocate.put("tran_date",tranDate);
					strAllocate.put("ref_ser","S-ALC");
					strAllocate.put("ref_id",saleOrder);
					strAllocate.put("ref_line", "" + lineNoSord);
					//strAllocate.put("site_code",siteCode);      // change by cpatil for siteCode to siteCodeShip
					strAllocate.put("site_code",siteCodeShip);      // change by cpatil for siteCode to siteCodeShip
					strAllocate.put("item_code",itemCode);
					strAllocate.put("loc_code",locCode);
					strAllocate.put("lot_no",lotNo);
					strAllocate.put("lot_sl",lotSl);
					strAllocate.put("alloc_qty",new Double(qtyToBeAllocated));
					strAllocate.put("chg_user",new  ibase.utility.E12GenericUtility().getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
					strAllocate.put("chg_term",new  ibase.utility.E12GenericUtility().getValueFromXTRA_PARAMS(xtraParams,"termId"));
					strAllocate.put("chg_win","W_SORDALLOC");

					errString = invAllocTrace.updateInvallocTrace(strAllocate, conn);
					System.out.println("errString ::: " + errString);
					if (errString != null && errString.trim().length() > 0 )
					{
						break;
					}
					strAllocate = null;

					//Selecting ITEM_CODE,ITEM_CODE__ORD,UNIT, ITEM_REF from SORDITEM TABLE
					pSelSordItem.setString(1, saleOrder);
					pSelSordItem.setString(2, lineNoSord);
					pSelSordItem.setString(3, expLev);
					rsTemp = pSelSordItem.executeQuery();
					pSelSordItem.clearParameters();
					if (rsTemp.next())
					{
						//ITEM_CODE
						itemCode = rsTemp.getString(1);
						System.out.println("itemCode::::"+ itemCode);
						//ITEM_CODE__ORDbatchId
						itemCodeOrd = rsTemp.getString(2);	
						//UNIT
						unitStd = rsTemp.getString(3);	
						//ITEM_REF
						itemRef = rsTemp.getString(4);	
						//PENDING_QUANTITY
						//pendingQuantity = rsTemp.getDouble(5);        // added comment by cpatil on 03/07/13
					}
					rsTemp.close(); rsTemp = null;
					
					//Updating the alloc quantity in SORDITEM TABLE
					pUpdSordItem.setDouble(1, qtyToBeAllocated );
					pUpdSordItem.setString(2, saleOrder);
					pUpdSordItem.setString(3, lineNoSord);
					pUpdSordItem.setString(4, expLev);
					updCount = pUpdSordItem.executeUpdate();
					pUpdSordItem.clearParameters();
					
					if ( updCount > 0 )
					{
						System.out.println("UPDATE  SUCCESS FOR SORDITEM....>>>>>>>>");
					}
					//new Double(qtyToBeAllocated).toString()
					//Counting the records in SORDALLOC TABLE
					pSelSordAlloc.setString(1, saleOrder);
					pSelSordAlloc.setString(2, lineNoSord);
					pSelSordAlloc.setString(3, expLev);
					pSelSordAlloc.setString(4, itemCodeOrd);
					pSelSordAlloc.setString(5, itemCode);
					pSelSordAlloc.setString(6, lotNo);
					pSelSordAlloc.setString(7, lotSl);
					pSelSordAlloc.setString(8, locCode);
					rsTemp = pSelSordAlloc.executeQuery();
					pSelSordAlloc.clearParameters();

					int count = 0 ;
					if (rsTemp.next())
					{
						count = rsTemp.getInt(1);
					}
					rsTemp.close(); rsTemp = null;
					System.out.println("SORDALLOC select :- "+count);
					if (count > 0 )
					{
						//Updating the alloc quantity in SORDALLOC TABLE
						pUpdSordAlloc.setDouble(1, qtyToBeAllocated );
						// pUpdSordAlloc.setString(2, batchId);	//Changed by Pankaj R on 18/02/15.
						pUpdSordAlloc.setString(2, saleOrder);
						pUpdSordAlloc.setString(3, lineNoSord);
						pUpdSordAlloc.setString(4, expLev);
						pUpdSordAlloc.setString(5, itemCodeOrd);
						pUpdSordAlloc.setString(6, itemCode);
						pUpdSordAlloc.setString(7, lotNo);
						pUpdSordAlloc.setString(8, lotSl);
						pUpdSordAlloc.setString(9, locCode);
						updCount = pUpdSordAlloc.executeUpdate();
						pUpdSordAlloc.clearParameters();
						System.out.println("SORDALLOC update :- "+updCount);
						if ( updCount > 0 )
						{
							System.out.println("UPDATE  SUCCESS FOR SORDALLOC....");
						}
					}
					else
					{
						//Inserting the records into SORDALLOC TABLE
						//SALE_ORDER
						pInsSordAlloc.setString(1, saleOrder);  
						System.out.println("saleOrder------->"+saleOrder);
						//LINE_NO
						pInsSordAlloc.setString(2, lineNoSord);
						System.out.println("lineNo------->"+lineNoSord);
						//EXP_LEV
						pInsSordAlloc.setString(3, expLev);
						System.out.println("EXP_LEV------->"+expLev);
						//ITEM_CODE__ORD
						pInsSordAlloc.setString(4, itemCodeOrd);
						System.out.println("item_code__ord------->"+itemCodeOrd);
						//SITE_CODE
						//pInsSordAlloc.setString(5, siteCode);      // change by cpatil for siteCode to siteCodeShip
						//System.out.println("siteCode------->"+siteCode);      // change by cpatil for siteCode to siteCodeShip
						pInsSordAlloc.setString(5, siteCodeShip);      // change by cpatil for siteCode to siteCodeShip
						System.out.println("siteCode------->"+siteCodeShip);      // change by cpatil for siteCode to siteCodeShip
						
						//ITEM_CODE
						pInsSordAlloc.setString(6, itemCode);
						System.out.println("itemCode------->"+itemCode);
						//QUANTITY***** set pending qty
						pInsSordAlloc.setDouble(7, pendingQuantity);
						System.out.println("pendingQuantity------->"+pendingQuantity);
						//LOT_NO
						pInsSordAlloc.setString(8, lotNo);
						System.out.println("lotNo------->"+lotNo);
						//LOT_SL
						pInsSordAlloc.setString(9, lotSl);
						System.out.println("lotSl------->"+lotSl);
						//LOC_CODE
						pInsSordAlloc.setString(10, locCode);
						System.out.println("locCode------->"+locCode);
						//UNIT
						pInsSordAlloc.setString(11, unit);
						System.out.println("unit------->"+unit);
						//QTY_ALLOC
						pInsSordAlloc.setDouble(12, qtyToBeAllocated);
						System.out.println("lotQtyToBeAllocated------->" +qtyToBeAllocated);
						//ITEM_REF
						pInsSordAlloc.setString(13,itemRef);
						System.out.println("itemRef------->"+itemRef);
						//DATE_ALLOC
						pInsSordAlloc.setDate(14,dateAlloc);
						System.out.println("dateAlloc------->"+dateAlloc);
						//STATUS
						pInsSordAlloc.setString(15,"P");
						System.out.println("status------->P");
						//ITEM_GRADE
						pInsSordAlloc.setString(16,grade);
						System.out.println("grade------->"+grade);
						//EXP_DATE
//						pInsSordAlloc.setDate(17,expDate);
//						System.out.println("expDate------->"+expDate);
						pInsSordAlloc.setTimestamp(17,expDate1);             // change by ritesh on 14/08/13
						System.out.println("expDate------->"+expDate1);
						//ALLOC_MODE
						pInsSordAlloc.setString(18,"M");
						//CONV__QTY_STDUOM
						pInsSordAlloc.setDouble(19,convQtyStduom);
						System.out.println("convQtyStduom------->"+convQtyStduom);
						//UNIT__STD
						pInsSordAlloc.setString(20,unitStd);
						System.out.println("unitStd------->"+unitStd);

						quantityStduom = convQtyStduom * qtyToBeAllocated ;
						//QUANTITY__STDUOM
						pInsSordAlloc.setDouble(21,quantityStduom);
						System.out.println("quantityStduom------->"+quantityStduom);
						//MFG_DATE
//						pInsSordAlloc.setDate(22,mfgDate);
//						System.out.println("mfgDate------->"+mfgDate);
						pInsSordAlloc.setTimestamp(22,mfgDate1);					// change by ritesh on 14/08/13
						System.out.println("mfgDate------->"+mfgDate1);
						//SITE_CODE__MFG
						pInsSordAlloc.setString(23,siteCodeMfg);
						System.out.println("siteCodeMfg------->"+siteCodeMfg);
						//QTY_DESP
						pInsSordAlloc.setString(24,tranIDD); //added by deepak sawant (24/10/13)
						
						pInsSordAlloc.setString(25,lineNoo); //added by deepak sawant (24/10/13)
						pInsSordAlloc.setString(26,batchId); //VALLABH KADAM INSERT BATCH id 
						int insCnt = 0;
						insCnt = pInsSordAlloc.executeUpdate();
						pInsSordAlloc.clearParameters();
						if ( insCnt > 0 )
						{
							System.out.println("insertion  success ...............>>>>>>>>");
						}
					}//end else
				}//end of if(qtyToBeAllocated > 0)
			}//end of while 
			rs.close(); rs = null;
			pSordAllocDet.clearParameters();
			pSordAllocDet.close(); pSordAllocDet = null;

			updateSql = "UPDATE sorder SET alloc_flag =  'M' WHERE sale_order = ? ";
			pstmt = conn.prepareStatement(updateSql);
			pstmt.setString(1,saleOrder);
			updCount = pstmt.executeUpdate();
			pstmt.close(); pstmt = null;

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
		finally
		{
			try
			{
				if(conn != null)
				{	
					//Changed by sumit on 20/08/12 closing prepared statement start.
					if( pSelSordItem != null)
					{
						pSelSordItem.close();
						pSelSordItem = null;
					}
					if(pSelSordAlloc != null)
					{
						pSelSordAlloc.close();
						pSelSordAlloc = null;
					}
					if( pUpdSordItem != null )
					{
						pUpdSordItem.close();
						pUpdSordItem = null;
					}
					if( pUpdSordAlloc != null)
					{
						pUpdSordAlloc.close();
						pUpdSordAlloc = null;
					}
					//Changed by sumit on 20/08/12 closing prepared statement end.
					if(pSordAllocDet != null)
					{
						pSordAllocDet.close();
						pSordAllocDet = null;
					}
					if(pInsSordAlloc != null)
					{
						pInsSordAlloc.close();
						pInsSordAlloc = null;
					}
				}
			}
			catch(Exception e)
			{
				errString = e.getMessage();
				e.printStackTrace();
				return errString ;

			}
			return errString;
		}

	}// end of sorderAllocate
}
