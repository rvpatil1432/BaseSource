package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.sql.*;

import org.w3c.dom.*;

import javax.ejb.*;
import javax.naming.InitialContext;

import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;
//import ibase.webitm.utility.GenericUtility;
import ibase.system.config.*;
import ibase.webitm.ejb.*;

import javax.ejb.Stateless; // added for ejb3


//public class DespatchPosEJB extends ValidatorEJB implements SessionBean
@Stateless // added for ejb3
public class DespatchPos extends ValidatorEJB implements DespatchPosLocal, DespatchPosRemote
{
	String objName="",detailNode="";
	E12GenericUtility genericUtility= new  E12GenericUtility();
	/*public void ejbCreate() throws RemoteException, CreateException 
	{
	}

	public void ejbRemove()
	{
	}

	public void ejbActivate() 
	{
	}

	public void ejbPassivate() 
	{
	}*/
	public String postSaveRec()throws RemoteException,ITMException
	{
		return "";
	}

	public String postSaveRec(String xmlString1,String domId,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{
		Document dom = null;
		String retString = "";
		//System.out.println("DespatchPosEJB called");
		//System.out.println("xmlString1: ["+xmlString1+"]");
		try
		{
			if (xmlString1 != null && xmlString1.trim().length() > 0)
			{
				//dom = GenericUtility.getInstance().parseString(xmlString1);
				dom = genericUtility.parseString(xmlString1);
				if( (xmlString1.toUpperCase()).contains("DESPATCHWIZ") )
				{
					objName="despatchwiz";
					detailNode="detail3";
					if( (xmlString1.toUpperCase()).contains("DETAIL3") )
					{						
						executepostSaveRec(dom,domId,objContext,editFlag,xtraParams,conn);
						
					}
				}
				
				else
				{
					objName="";
					detailNode="detail2";
					executepostSaveRec(dom,domId,objContext,editFlag,xtraParams,conn);
				}
				
			}	
			
		}
		catch(Exception e)
		{
			//System.out.println("Exception :DespatchPosEJB ::" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return "";
	}

	private String executepostSaveRec(Document dom, String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{
		Statement stmt = null, stmt1 = null, stmt2 = null;
		ResultSet rs = null, rs1 = null, rs2 = null, rs3 = null, rs4 = null, rs5 = null;
		PreparedStatement pstmt = null;
		String siteCode = "", itemCode = "", lineNo = "",locCode = "", lotNo = "", lotSl = "";
		String expLev = "", itemCodeOrd = "", unit = "", sordNo = "", custCode  = "", locGroup = "";
		String grade = "",siteCodeMfg = "", lineNoSord = "", unitStd = "", sql = "";
		HashMap strAllocate = new HashMap();
		String sqlUpdate = "", sqlInsert = "";
		double rate = 0d, qtyAlloc = 0d, convQtyStdUom = 0d, qtyStduom = 0d;
		int updateCnt = 0, retVal = 0;
		java.sql.Date expDate = null,mfgDate = null, currDate = null, tranDate = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();		
		NodeList hdrDom	= null,detailDom = null,currDetailList = null;
		Node currDetail = null;
		int detailListLength = 0,currDetailListLength = 0;
		String qtyStduomStr = "",updateStatus = "",nodeName = "",tranId = "";
		
		
		try
		{
			stmt = conn.createStatement();
			stmt1 = conn.createStatement();
			stmt2 = conn.createStatement();			
			currDate = new java.sql.Date(System.currentTimeMillis());
			tranDate = new java.sql.Date(System.currentTimeMillis());
			//System.out.println("\n CurrDate :"+currDate+"\n TranDate :"+tranDate);
			if("despatchwiz".equalsIgnoreCase(objName))
			{
				hdrDom = dom.getElementsByTagName("Detail2");
			}
			else
			{
				hdrDom = dom.getElementsByTagName("Detail1");
			}
			//hdrDom = dom.getElementsByTagName("Detail1");
			/*siteCode = GenericUtility.getInstance().getColumnValueFromNode("site_code",hdrDom.item(0));
			tranId = GenericUtility.getInstance().getColumnValueFromNode("desp_id",hdrDom.item(0));*/
			siteCode = genericUtility.getColumnValueFromNode("site_code",hdrDom.item(0));
			tranId = genericUtility.getColumnValueFromNode("desp_id",hdrDom.item(0));
			//System.out.println("10/11/14 manohar executepostSaveRec DomID ["+domID+"] Update Status ["+updateStatus + "]");
			currDetail  = getCurrentDetailFromDom(dom,domID);
			updateStatus = getCurrentUpdateFlag(currDetail);
			//System.out.println("Site Code :: "+siteCode);
			//System.out.println("Tran ID :: "+tranId);
			if (tranId == null || tranId.trim().length() == 0)
			{
				//tranId  = GenericUtility.getInstance().getColumnValueFromNode("tran_id",currDetail);
				tranId  = genericUtility.getColumnValueFromNode("tran_id",currDetail);
			}
			//System.out.println("Tran ID from currDetail  ["+tranId + "]");
			//System.out.println("DomID :: "+domID+"\n Update Status ::"+updateStatus);

			if (currDetail != null && !updateStatus.equalsIgnoreCase("D"))
			{
				itemCode = genericUtility.getColumnValueFromNode("item_code",currDetail);
				lineNo = genericUtility.getColumnValueFromNode("line_no",currDetail);
				locCode = genericUtility.getColumnValueFromNode("loc_code",currDetail);
				lotNo = genericUtility.getColumnValueFromNode("lot_no",currDetail);
				lotSl = genericUtility.getColumnValueFromNode("lot_sl",currDetail);
				qtyStduomStr = genericUtility.getColumnValueFromNode("quantity__stduom",currDetail);
				expLev = genericUtility.getColumnValueFromNode("exp_lev",currDetail);
				itemCodeOrd = genericUtility.getColumnValueFromNode("item_code__ord",currDetail);
				unit = genericUtility.getColumnValueFromNode("unit",currDetail);
				custCode = genericUtility.getColumnValueFromNode("cust_code",currDetail);
				sordNo = genericUtility.getColumnValueFromNode("sord_no",currDetail);
				lineNoSord = genericUtility.getColumnValueFromNode("line_no__sord",currDetail);
				lineNoSord = "   " + lineNoSord; 
				lineNoSord = lineNoSord.substring(lineNoSord.length()-3);
				qtyStduom = Double.parseDouble(qtyStduomStr);	
				sql = "SELECT CASE WHEN RATE IS NULL THEN 0 ELSE RATE END "
						+"FROM STOCK WHERE ITEM_CODE = '"+itemCode+"' "
						+"AND SITE_CODE = '"+siteCode+"' "
						+"AND LOC_CODE = '"+locCode+"' "
						+"AND LOT_NO = '"+lotNo+"' "
						+"AND LOT_SL = '"+lotSl+"'";
				//System.out.println("sql :"+sql);
				rs1 = stmt1.executeQuery(sql);
				if (rs1.next())
				{
					rate = rs1.getDouble(1);
					//System.out.println("rate :"+rate);
				}
				//stmt1.close();
				/* -- Commented and Changes by gulzar - 12/11/07
				sql = "UPDATE DESPATCHDET SET COST_RATE = ? "
							+"WHERE DESP_ID = ? "
							+"AND ITEM_CODE = ? "
							+"AND LINE_NO   = ? "
							+"AND LOC_CODE  = ? "
							+"AND LOT_NO    = ? "
							+"AND LOT_SL	= ? ";
				//System.out.println("update sql :"+sql);
				pstmt = conn.prepareStatement(sql);
				pstmt.setDouble(1,rate);
				pstmt.setString(2,tranId);
				pstmt.setString(3,itemCode);
				pstmt.setString(4,lineNo);
				pstmt.setString(5,locCode);
				pstmt.setString(6,lotNo);
				pstmt.setString(7,lotSl);
				updateCnt = pstmt.executeUpdate();
				//System.out.println("updateCnt in despatchDet :"+updateCnt);
				pstmt.clearParameters();
				 *///End Commented Gulzar - 12/11/07
				//Added By Gulzar - 12/11/07
				
				lineNo = ("   " + lineNo) ;
				lineNo = lineNo.substring(lineNo.length()-3);
				
				sql = "UPDATE DESPATCHDET SET COST_RATE = ? "
						+"WHERE DESP_ID = '"+tranId+"' "
						+"AND ITEM_CODE = '"+itemCode+"' "
						+"AND LINE_NO   = '"+lineNo+"' "
						+"AND LOC_CODE  = '"+locCode+"' "
						+"AND LOT_NO    = '"+lotNo+"' "
						+"AND LOT_SL	= '"+lotSl+"' ";
				//System.out.println("update sql is :"+sql);
				pstmt = conn.prepareStatement(sql);
				pstmt.setDouble(1,rate);
				updateCnt = pstmt.executeUpdate();
				pstmt.close(); pstmt = null;
				//System.out.println("updateCnt in despatchDet :"+updateCnt);
				//End Added - Gulzar - 12/11/07
				/* 10/10/14 manoharan commented
				sql = "SELECT QTY_ALLOC FROM SORDALLOC WHERE SALE_ORDER = '"+sordNo+"' "
						+"AND LINE_NO =	'"+lineNoSord+"' "
						+"AND EXP_LEV =	'"+expLev+"' "
						+"AND ITEM_CODE__ORD = '"+itemCodeOrd+"' "
						+"AND ITEM_CODE	= '"+itemCode+"' "
						+"AND LOC_CODE = '"+locCode+"' "
						+"AND LOT_NO = '"+lotNo+"' "
						+"AND LOT_SL = '"+lotSl+"' ";
				//+"AND ALLOC_MODE = 'M' ";//Manual allocation.	//Commented Jiten 10/10/06, It should check for both 'A' and 'M'. 
				//System.out.println("Select sql :"+sql);
				rs2 = stmt1.executeQuery(sql);
				*/
				/*
				if (rs2.next())
				{
					qtyAlloc = rs2.getDouble(1);
					//System.out.println("qtyAlloc :"+qtyAlloc);
				}
				 */
				 
				//if (!rs2.next())
				//{
					sql = "SELECT CUST_CODE,LOC_GROUP FROM SORDER WHERE SALE_ORDER ='"+sordNo+"' ";
					//System.out.println("sql  :"+sql);
					//stmt1 = conn.createStatement();
					rs3 = stmt1.executeQuery(sql);
					if (rs3.next())
					{
						custCode = rs3.getString(1);
						//System.out.println("custCode :"+custCode);
						locGroup = rs3.getString(2);
						//System.out.println("locGroup :"+locGroup);
					}
				//	if (locGroup == null || locGroup.trim().length() == 0)
				//	{
				//		sql = "SELECT A.EXP_DATE, A.GRADE, A.MFG_DATE, A.SITE_CODE__MFG "
				//				+"FROM STOCK A WHERE A.ITEM_CODE = '"+itemCode+"' "
				//				+"AND A.SITE_CODE = '"+siteCode+"' "
				//				+"AND A.LOC_CODE = '"+locCode+"' "
				//				+"AND A.LOT_NO = '"+lotNo+"' "
				//				+"AND A.LOT_SL = '"+lotSl+"'" ;
				//		//System.out.println("sql :"+sql);				
				//		rs4 = stmt1.executeQuery(sql);
				//		if (rs4.next())
				//		{
				//			expDate = rs4.getDate(1);
				//			//System.out.println("expDate :"+expDate);
				//			grade = rs4.getString(2);
				//			//System.out.println("grade :"+grade);
				//			mfgDate = rs4.getDate(3);
				//			//System.out.println("mfgDate :"+mfgDate);
				//			siteCodeMfg = rs4.getString(4);
				//			//System.out.println("siteCodeMfg :"+siteCodeMfg);
				//		}
				//	}
				//	else
				//	{
				//		sql = "SELECT A.EXP_DATE, A.GRADE, A.MFG_DATE, A.SITE_CODE__MFG "	
				//				+"FROM STOCK A, LOCATION B "
				//				+"WHERE A.LOC_CODE = B.LOC_CODE "
				//				+"AND B.LOC_GROUP = '"+locGroup+"' "
				//				+"AND A.ITEM_CODE = '"+itemCode+"' "
				//				+"AND A.SITE_CODE = '"+siteCode+"' "
				//				+"AND A.LOC_CODE = '"+locCode+"' "
				//				+"AND A.LOT_NO = '"+lotNo+"' "
				//				+"AND A.LOT_SL = '"+lotSl+"'";
				//		//System.out.println("sql :"+sql);
				//		rs4 = stmt1.executeQuery(sql);
				//		if (rs4.next())
				//		{
				//			expDate = rs4.getDate(1);
				//			//System.out.println("expDate :"+expDate);
				//			grade = rs4.getString(2);
				//			//System.out.println("grade :"+grade);
				//			mfgDate = rs4.getDate(3);
				//			//System.out.println("mfgDate :"+mfgDate);
				//			siteCodeMfg = rs4.getString(4);
				//			//System.out.println("siteCodeMfg :"+siteCodeMfg);
				//		}
				//	}//end else
				//	sql = "SELECT UNIT__STD, CONV__QTY_STDUOM FROM SORDDET "
				//			+"WHERE SALE_ORDER = '"+sordNo+"' "
				//			+"AND LINE_NO = '"+lineNoSord+"'";
				//	//System.out.println("sql :"+sql);
				//	rs5 = stmt1.executeQuery(sql);
				//	if (rs5.next())
				//	{
				//		unitStd = rs5.getString(1);
				//		//System.out.println("unitStd :"+unitStd);
				//		convQtyStdUom = rs5.getDouble(2);
				//		//System.out.println("convQtyStdUom :"+convQtyStdUom);
				//	}
					// 10/10/14 manoharan not required to insert
					/*sqlInsert = "INSERT INTO SORDALLOC (SALE_ORDER, LINE_NO, EXP_LEV, ITEM_CODE__ORD ,ITEM_CODE, "
							+"LOT_NO,	LOT_SL, LOC_CODE, ITEM_REF,	QUANTITY, UNIT, QTY_ALLOC, DATE_ALLOC, STATUS, "
							+"ALLOC_MODE,	SITE_CODE ,ITEM_GRADE, EXP_DATE, MFG_DATE, SITE_CODE__MFG, UNIT__STD, "
							+"CONV__QTY_STDUOM, QUANTITY__STDUOM )"
							+"values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
					//System.out.println("Insert sql from DespatchPostSaveEJB :"+sqlInsert);
					pstmt = conn.prepareStatement(sqlInsert);
					pstmt.setString(1,sordNo);
					pstmt.setString(2,lineNoSord);
					pstmt.setString(3,expLev);
					pstmt.setString(4,itemCodeOrd);
					pstmt.setString(5,itemCode);
					pstmt.setString(6,lotNo);
					pstmt.setString(7,lotSl);
					pstmt.setString(8,locCode);
					pstmt.setNull(9,java.sql.Types.VARCHAR);
					pstmt.setDouble(10,qtyStduom);
					pstmt.setString(11,unit);
					pstmt.setDouble(12,qtyStduom);
					pstmt.setDate(13,currDate);
					pstmt.setString(14,"P");
					pstmt.setString(15,"A");
					pstmt.setString(16,siteCode);
					pstmt.setString(17,grade);
					pstmt.setDate(18,expDate);
					pstmt.setDate(19,mfgDate);
					pstmt.setString(20,siteCodeMfg);
					pstmt.setString(21,unitStd);
					pstmt.setDouble(22,convQtyStdUom);
					pstmt.setDouble(23,qtyStduom);
					updateCnt = pstmt.executeUpdate();
					//System.out.println("Inserted the no of records : updateCnt :"+updateCnt);
					pstmt.clearParameters();
					*/
					/*strAllocate.put("tran_date",tranDate);
					strAllocate.put("ref_ser","S-DSP");
					strAllocate.put("ref_id",tranId);
					strAllocate.put("ref_line",line.substring(line.length()-3));
					strAllocate.put("site_code",siteCode);
					strAllocate.put("item_code",itemCode);
					strAllocate.put("loc_code",locCode);
					strAllocate.put("lot_no",lotNo);
					strAllocate.put("lot_sl",lotSl);
					strAllocate.put("alloc_qty",new Double(qtyStduom));
					strAllocate.put("chg_user",genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
					strAllocate.put("chg_term",genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId"));
					strAllocate.put("chg_win","W_DESPATCH");
					//Calling DistStkUpdEJB
					//System.out.println("Calling DistStkUpdEJB.....");
					if (distStkUpd.updAllocTrace(strAllocate, conn) > 0)
					{
						//System.out.println("distStkUpd.UpdAllocTrace(hashMap) : Sucessuful!");
					}
					sqlUpdate = "UPDATE SORDITEM SET QTY_ALLOC = QTY_ALLOC + ? "
							   +"WHERE SALE_ORDER = ? "
							   +"AND LINE_NO = ? "
							   +"AND EXP_LEV = ? ";
					//System.out.println("Update sql :"+sqlUpdate);
					pstmt = conn.prepareStatement(sqlUpdate);
					pstmt.setDouble(1,qtyStduom);
					pstmt.setString(2,sordNo);
					pstmt.setString(3,lineNoSord);
					pstmt.setString(4,expLev);
					updateCnt = pstmt.executeUpdate();
					//System.out.println("Updated the no of records : updateCnt :"+updateCnt);
					pstmt.clearParameters();*/
				//}
				//else{
				//	sqlUpdate = "UPDATE SORDALLOC "+
				//			"SET QTY_ALLOC = QTY_ALLOC + ? "+
				//			"WHERE SALE_ORDER = ? "+
				//			"AND LINE_NO = ? "+
				//			"AND EXP_LEV = ? "+
				//			"AND ITEM_CODE__ORD = ? "+
				//			"AND ITEM_CODE = ? "+
				//			"AND LOT_NO = ? "+
				//			"AND LOT_SL = ? "+
				//			"AND LOC_CODE = ? ";
				//	pstmt = conn.prepareStatement(sqlUpdate);
				//	pstmt.setDouble(1,qtyStduom);
				//	pstmt.setString(2,sordNo);
				//	pstmt.setString(3,lineNoSord);
				//	pstmt.setString(4,expLev);
				//	pstmt.setString(5,itemCodeOrd);
				//	pstmt.setString(6,itemCode);
				//	pstmt.setString(7,lotNo);
				//	pstmt.setString(8,lotSl);
				//	pstmt.setString(9,locCode);
				//	updateCnt = pstmt.executeUpdate();
				//	pstmt.clearParameters();
				//	pstmt.close();
				//	pstmt = null;
				//}
				/////Customer wise manual allocation/deallocatuin.
				/////If edit mode then only diff qty is re-allocated.
				String tranIdSOAlloc = "";
				long lineNoSOAlloc = 0;
				double pendingDeallocQty = 0,allocQty = 0,deAllocQty = 0;
				sql = "SELECT B.TRAN_ID, B.LINE_NO, (B.QUANTITY - B.DEALLOC_QTY), B.QUANTITY, B.DEALLOC_QTY "+
						"FROM SORD_ALLOC A, SORD_ALLOC_DET B "+
						"WHERE A.TRAN_ID   	= B.TRAN_ID	"+
						"AND A.CUST_CODE 	= ? "+
						"AND A.SITE_CODE   = ? "+
						"AND A.SALE_ORDER  IS NULL "+
						"AND B.ITEM_CODE 	= ? "+
						"AND B.LOC_CODE	 	= ? "+
						"AND B.LOT_NO		= ? "+
						"AND B.LOT_SL		= ? "+
						"AND B.QUANTITY - B.DEALLOC_QTY > 0 ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,custCode);
				pstmt.setString(2,siteCode);
				pstmt.setString(3,itemCodeOrd);
				pstmt.setString(4,locCode);	
				pstmt.setString(5,lotNo);
				pstmt.setString(6,lotSl);
				rs = pstmt.executeQuery();
				if(rs.next()){
					tranIdSOAlloc = rs.getString(1);
					lineNoSOAlloc = rs.getLong(2);
					pendingDeallocQty = rs.getDouble(3);
					allocQty = rs.getDouble(4);
					deAllocQty = rs.getDouble(5);					
				}
				if(pstmt !=null)
				{
				pstmt.close();
				}
				pstmt=null;
				rs = null;
				////////
				//conn = getConnection();
				//Statement stmt2 = conn.createStatement();
				double oldQty = 0,qtyUpdate = 0;
				if(tranIdSOAlloc != null && tranIdSOAlloc.trim().length() > 0){
					sql = "SELECT QUANTITY__STDUOM FROM DESPATCHDET WHERE DESP_ID = '"+tranId+"' AND LINE_NO = '"+lineNo+"'";
					rs = stmt2.executeQuery(sql);
					if(rs.next()){
						oldQty = rs.getDouble(1);
					}
					//stmt2.close();
					//stmt2 = null;
					//conn.close();
					//conn = null;
					rs = null;
					//////////////
					if(deAllocQty + qtyStduom - oldQty > allocQty){
						qtyUpdate = allocQty - deAllocQty;
					}
					else{
						qtyUpdate = qtyStduom - oldQty;
					}
					sql = "UPDATE SORD_ALLOC_DET "+
							"SET DEALLOC_QTY = DEALLOC_QTY + ? "+
							"WHERE TRAN_ID = ? "+
							"AND LINE_NO = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setDouble(1,qtyUpdate);
					pstmt.setString(2,tranIdSOAlloc);
					pstmt.setLong(3,lineNoSOAlloc);
					pstmt.executeUpdate();
					pstmt.close();
					pstmt = null;
				} 
				String line = "   " + lineNo;
				//System.out.println("line :"+line);
				//strAllocate.put("tran_date",tranDate);
				//strAllocate.put("ref_ser","S-DSP");
				strAllocate.put("ref_id",tranId);
				strAllocate.put("ref_line",line.substring(line.length()-3));
				strAllocate.put("site_code",siteCode);
				strAllocate.put("item_code",itemCode);
				strAllocate.put("loc_code",locCode);
				strAllocate.put("lot_no",lotNo);
				strAllocate.put("lot_sl",lotSl);
				strAllocate.put("alloc_qty",new Double(qtyStduom));
				strAllocate.put("chg_user",genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
				strAllocate.put("chg_term",genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId"));
				//strAllocate.put("chg_win","W_DESPATCH");
				if("despatchwiz".equalsIgnoreCase(objName))
				{
					strAllocate.put("chg_win","W_DESPATCHWIZ");
					strAllocate.put("ref_ser","S-WDSP");
				}
				else
				{
					strAllocate.put("chg_win","W_DESPATCH");
					strAllocate.put("ref_ser","S-DSP");
				}
				//added by nandkumar gadkari on 17/04/19-------start=----------
				String logMsg= tranId +" "+expLev+" "+line.substring(line.length()-3) + " "+"Allocation of stock from DespatchPos";
				strAllocate.put("alloc_ref",logMsg);	
				//added by nandkumar gadkari on 17/04/19-------end=----------
				//Calling DistStkUpdEJB
				//System.out.println("Calling DistStkUpdEJB.....");
				/*if (distStkUpd.updAllocTrace(strAllocate, conn) > 0)
				{
					//System.out.println("distStkUpd.UpdAllocTrace(hashMap) : Sucessuful!");
				}*/
				InvAllocTraceBean invBean = new InvAllocTraceBean(); 
				String errString = invBean.updateInvallocTrace(strAllocate,conn);
				if(errString == null && errString.trim().length() == 0){
					//System.out.println("distStkUpd.UpdAllocTrace(hashMap) : Sucessuful!");
				}
				sqlUpdate = "UPDATE SORDITEM SET QTY_ALLOC = QTY_ALLOC + ? "
						+"WHERE SALE_ORDER = ? "
						+"AND LINE_NO = ? "
						+"AND EXP_LEV = ? ";
				//System.out.println("Update sql :"+sqlUpdate);
				pstmt = conn.prepareStatement(sqlUpdate);
				pstmt.setDouble(1,qtyStduom);
				pstmt.setString(2,sordNo);
				pstmt.setString(3,lineNoSord);
				pstmt.setString(4,expLev);
				updateCnt = pstmt.executeUpdate();
				//System.out.println("Updated the no of records : updateCnt :"+updateCnt);
				pstmt.clearParameters();
			}
			
		}//try end
		catch (SQLException sqx)
		{
			//System.out.println("The SQLException occurs in DespatchPostSaveEJB :"+sqx);
			sqx.printStackTrace();
			throw new ITMException(sqx);
		}
		catch(Exception e)
		{
			//System.out.println("The SQLException occurs in DespatchPostSaveEJB :"+e);			
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
				if(rs1 != null)
				{
					rs1.close();
					rs1 = null;
				}
				if(rs2 != null)
				{
					rs2.close();
					rs2 = null;
				}
				if(rs3 != null)
				{
					rs3.close();
					rs3 = null;
				}
				if(rs4 != null)
				{
					rs4.close();
					rs4 = null;
				}
				if(rs5 != null)
				{
					rs5.close();
					rs5 = null;
				}
				if(stmt != null)
				{
					stmt.close();
					stmt = null;
				}
				if(stmt1 != null)
				{
					stmt1.close();
					stmt1 = null;
				}
				if(stmt2 != null)
				{
					stmt2.close();
					stmt2 = null;
				}
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}

			}
			catch(Exception e)
			{
				System.err.println("Exception :DBAccessEJB :getITMVersion :\n"+e.getMessage());
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return "";
	}
	//postSave() added by kunal on 11/07/13 FOR cross update
	
	public String postSave(String winName,String editFlag,String tranId,  String xtraParams,Connection conn) throws RemoteException,ITMException
	{
		String sql = "",saleOrder = "";
		int lineNo = 0;
		double grossWeight = 0,tareWeight = 0,netWeight = 0,noAart = 0,offinvAmt = 0,billbackAmt = 0 ;
		double quantityStduom = 0 ,rateStduom = 0,offinvAmtDet = 0,taxAmtDet = 0 ,discount = 0,totAmt = 0,amount = 0;  
		//GenericUtility genericUtility = GenericUtility.getInstance();
		PreparedStatement pstmt = null,pstmt1 = null;
		ResultSet rs = null,rs1 = null;

		////System.out.println("tranId="+tranId+"    xtraParams="+xtraParams);
		try
		{

			sql = " select sord_no,line_no__sord ,quantity__stduom,rate__stduom ,disc_schem_offinv_amt,tax_amt from despatchdet where desp_id  = ? ";
			pstmt1= conn.prepareStatement(sql);
			pstmt1.setString( 1, tranId );
			rs1 = pstmt1.executeQuery();
			while (rs1.next())
			{
				saleOrder = rs1.getString("sord_no");
				lineNo = rs1.getInt("line_no__sord");
				quantityStduom = rs1.getDouble("quantity__stduom");
				rateStduom = rs1.getDouble("rate__stduom");
				offinvAmtDet = rs1.getDouble("disc_schem_offinv_amt");
				taxAmtDet = rs1.getDouble("tax_amt");
				

				sql = " select  discount  from sorddet where sale_order = ? and line_no = ? ";
				pstmt= conn.prepareStatement(sql);
				pstmt.setString( 1, saleOrder );
				pstmt.setInt( 2, lineNo );
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					discount = rs.getDouble("discount");
				}	
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				//[(quantity__stduom*rate__stduom)-((quantity__stduom*rate__stduom*discount)/100)-disc_schem_offinv_amt+tax_amt]
				amount = (quantityStduom*rateStduom) - ((quantityStduom*rateStduom*discount)/100) - offinvAmtDet + taxAmtDet;
				//System.out.println("amount="+amount);
				totAmt = totAmt + amount;
			}	
			rs1.close();
			rs1 = null;
			pstmt1.close();
			pstmt1 = null;	
			//System.out.println("tot amt = "+totAmt);

			sql = "select sum(gross_weight),sum(tare_weight) ,sum(nett_weight) ,sum(no_art) ,sum(disc_schem_offinv_amt) ,sum(disc_schem_billback_amt) "
					+" from despatchdet where desp_id = ?  ";
			pstmt= conn.prepareStatement(sql);
			pstmt.setString( 1, tranId );
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				grossWeight = rs.getDouble(1);
				tareWeight = rs.getDouble(2);
				netWeight = rs.getDouble(3);
				noAart = rs.getDouble(4);
				offinvAmt = rs.getDouble(5);
				billbackAmt = rs.getDouble(6);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			//System.out.println("sum ="+grossWeight+"  "+tareWeight+"    "+netWeight+"    "+noAart+"    "+offinvAmt+"    "+billbackAmt);

			sql = " update despatch set gross_weight = ? , tare_weight = ?,nett_weight = ?,no_art = ?, "
					+" disc_offinv_amt_det = ?, disc_billback_amt_det = ? ,tot_value = ? where desp_id =  ? " ;
			pstmt= conn.prepareStatement( sql );
			pstmt.setDouble( 1, grossWeight );
			pstmt.setDouble( 2, tareWeight );
			pstmt.setDouble( 3, netWeight );
			pstmt.setDouble( 4, noAart );
			pstmt.setDouble( 5, offinvAmt );
			pstmt.setDouble( 6, billbackAmt );
			pstmt.setDouble( 7, totAmt );
			pstmt.setString( 8, tranId );

			pstmt.executeUpdate();
			pstmt.close();
			pstmt = null;
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
				if(pstmt != null)pstmt.close();
				pstmt = null;
			}catch(Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		return "";
	}
	
	public String postSaveWiz(String xmlString,String editFlag,String tranId,  String xtraParams,Connection conn) throws RemoteException,ITMException
	{
		//System.out.println("----------postSaveWiz method ......................."+tranId);
		//System.out.println("editFlag------>>["+editFlag+"]");		
		//System.out.println("xmlString------>>["+xmlString+"]");
		String sql = "",saleOrder = "",retString="",currAppdate="",lineNoS="",siteCode="",itemCode="",
				locCode="",lotNo="",lotSl="",quantityS="",chgUser="",chgTerm="";
		int lineNo = 0,cnt=0;
		Document dom = null;
		double grossWeight = 0,tareWeight = 0,netWeight = 0,noAart = 0,offinvAmt = 0,billbackAmt = 0,quantity=0,allqty=0 ;
		double quantityStduom = 0 ,rateStduom = 0,offinvAmtDet = 0,taxAmtDet = 0 ,discount = 0,totAmt = 0,amount = 0;  
		//GenericUtility genericUtility = GenericUtility.getInstance();
		PreparedStatement pstmt = null,pstmt1 = null;
		ResultSet rs = null,rs1 = null;
		boolean isError=false;
		java.sql.Timestamp currDate = null;
		SimpleDateFormat sdf = null;
		HashMap allocQtyMap=new HashMap();
		InvAllocTraceBean allocTraceBean=new InvAllocTraceBean();
		//GenericUtility genericUtility = GenericUtility.getInstance();
		////System.out.println("tranId="+tranId+"    xtraParams="+xtraParams);
		try
		{	
			tranId=tranId==null ? "" :tranId.trim();
			if (xmlString != null && xmlString.trim().length() > 0)
			{
				dom = parseString(xmlString);					
			}
			if(dom!=null ){
				//System.out.println("dom not null.........");
				tranId = genericUtility.getColumnValue("desp_id",dom);
				lineNoS =genericUtility.getColumnValue("line_no",dom); 
				 siteCode= genericUtility.getColumnValue("site_code",dom,"3");				
				 itemCode= genericUtility.getColumnValue("item_code",dom,"3");
				 
				 locCode= genericUtility.getColumnValue("loc_code",dom,"3");
				lotNo= genericUtility.getColumnValue("lot_no",dom,"3");
				lotSl= genericUtility.getColumnValue("lot_sl",dom,"3");
				quantityS= genericUtility.getColumnValue("quantity",dom,"3");
				
				chgUser= genericUtility.getColumnValue("chg_user",dom,"3");
				chgTerm= genericUtility.getColumnValue("chg_term",dom,"3");
				
			}
			//System.out.println("tranId----->>["+tranId+"]");
			//System.out.println("lineNoS----->>["+lineNoS+"]");
			//System.out.println("siteCode----->>["+siteCode+"]");
			//System.out.println("itemCode----->>["+itemCode+"]");
			//System.out.println("locCode----->>["+locCode+"]");
			//System.out.println("lotSl----->>["+lotSl+"]");
			//System.out.println("quantity----->>["+quantity+"]");
			
			//System.out.println("chgUser----->>["+chgUser+"]");
			//System.out.println("chgTerm----->>["+chgTerm+"]");
			conn=null;
			ConnDriver connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
			sql = " select sord_no,line_no__sord ,quantity__stduom,rate__stduom ,disc_schem_offinv_amt,tax_amt from despatchdet where desp_id  = ? ";
			pstmt1= conn.prepareStatement(sql);
			pstmt1.setString( 1, tranId );
			rs1 = pstmt1.executeQuery();
			while (rs1.next())
			{
				saleOrder = rs1.getString("sord_no");
				lineNo = rs1.getInt("line_no__sord");
				quantityStduom = rs1.getDouble("quantity__stduom");
				rateStduom = rs1.getDouble("rate__stduom");
				offinvAmtDet = rs1.getDouble("disc_schem_offinv_amt");
				taxAmtDet = rs1.getDouble("tax_amt");
				

				sql = " select  discount  from sorddet where sale_order = ? and line_no = ? ";
				pstmt= conn.prepareStatement(sql);
				pstmt.setString( 1, saleOrder );
				pstmt.setInt( 2, lineNo );
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					discount = rs.getDouble("discount");
				}	
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				//[(quantity__stduom*rate__stduom)-((quantity__stduom*rate__stduom*discount)/100)-disc_schem_offinv_amt+tax_amt]
				amount = (quantityStduom*rateStduom) - ((quantityStduom*rateStduom*discount)/100) - offinvAmtDet + taxAmtDet;
				//System.out.println("amount="+amount);
				totAmt = totAmt + amount;
			}	
			rs1.close();
			rs1 = null;
			pstmt1.close();
			pstmt1 = null;	
			//System.out.println("tot amt = "+totAmt);

			sql = "select sum(gross_weight),sum(tare_weight) ,sum(nett_weight) ,sum(no_art) ,sum(disc_schem_offinv_amt) ,sum(disc_schem_billback_amt) "
					+" from despatchdet where desp_id = ?  ";
			pstmt= conn.prepareStatement(sql);
			pstmt.setString( 1, tranId );
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				grossWeight = rs.getDouble(1);
				tareWeight = rs.getDouble(2);
				netWeight = rs.getDouble(3);
				noAart = rs.getDouble(4);
				offinvAmt = rs.getDouble(5);
				billbackAmt = rs.getDouble(6);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			//System.out.println("sum ="+grossWeight+"  "+tareWeight+"    "+netWeight+"    "+noAart+"    "+offinvAmt+"    "+billbackAmt);

			sql = " update despatch set gross_weight = ? , tare_weight = ?,nett_weight = ?,no_art = ?, "
					+" disc_offinv_amt_det = ?, disc_billback_amt_det = ? ,tot_value = ? where desp_id =  ? " ;
			pstmt= conn.prepareStatement( sql );
			pstmt.setDouble( 1, grossWeight );
			pstmt.setDouble( 2, tareWeight );
			pstmt.setDouble( 3, netWeight );
			pstmt.setDouble( 4, noAart );
			pstmt.setDouble( 5, offinvAmt );
			pstmt.setDouble( 6, billbackAmt );
			pstmt.setDouble( 7, totAmt );
			pstmt.setString( 8, tranId );

			pstmt.executeUpdate();
			pstmt.close();
			pstmt = null;
			
			
			
			
			/*sql = "select alloc_qty from stock where site_code = ? AND LOT_SL = ? AND QUANTITY > 0";
			pstmt= conn.prepareStatement(sql);
			pstmt.setString( 1, siteCode );
			pstmt.setString( 2, lotSl );
			
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				allqty = rs.getDouble(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			quantityS=quantityS ==null ? "0":quantityS.trim();
			quantity  = Double.parseDouble(quantityS);
			//System.out.println("allqty--------->>["+allqty+"]");
			//System.out.println("quantity--------->>["+quantity+"]");
		
			if(allqty < quantity)
			{
				//Allocating stock 
				allocQtyMap.put("tran_date", currAppdate);
				allocQtyMap.put("ref_ser", "S-WDSP");
				allocQtyMap.put("ref_id", tranId);
				allocQtyMap.put("ref_line",lineNoS);
				allocQtyMap.put("site_code",siteCode);
				allocQtyMap.put("item_code",itemCode);
				//hashMap.put("loc_code",locCode[iRow]);
				allocQtyMap.put("loc_code",locCode);
				allocQtyMap.put("lot_no",lotNo);
				allocQtyMap.put("lot_sl",lotSl);
				allocQtyMap.put("alloc_qty",Double.parseDouble(quantityS));
				allocQtyMap.put("chg_user",chgUser);
				allocQtyMap.put("chg_term",chgTerm);
				allocQtyMap.put("chg_win","W_DISTISSWIZ");
				//System.out.println("called allocTraceBean.updateInvallocTrace(allocQtyMap, conn);");
				retString=allocTraceBean.updateInvallocTrace(allocQtyMap, conn);
			}*/
			
			
		}
		catch(Exception e)
		{
			isError=true;
			e.printStackTrace();
			try
			{
				conn.rollback();
			}catch(Exception d){
				d.printStackTrace();				
			}
			throw new ITMException(e);
		}
		finally
		{
			//System.out.println("in Finally------>>["+isError+"]");
			try
			{
				if(pstmt != null)pstmt.close();
				pstmt = null;
			}catch(Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
			if(!isError){
				try
				{
					conn.commit();
				}
				catch(Exception d){
					d.printStackTrace();
				}
			}
			if(conn!=null){
				try
				{
				conn.close();
				conn=null;
				}
				catch(Exception e){
					e.printStackTrace();
				}
				
			}
		}
		return "";
	
	}
	

	private Node getCurrentDetailFromDom(Document dom,String domId)
	{
		NodeList detailList = null;
		Node currDetail = null,reqDetail = null;
		String currDomId = "";
		int	detailListLength = 0;
		if("despatchwiz".equalsIgnoreCase(objName))
		{
			detailList = dom.getElementsByTagName("Detail3");
		}
		else
		{
			detailList = dom.getElementsByTagName("Detail2");
		}
		//detailList = dom.getElementsByTagName("Detail2");
		detailListLength = detailList.getLength();
		for (int ctr = 0;ctr < detailListLength;ctr++)
		{
			currDetail = detailList.item(ctr);
			currDomId = currDetail.getAttributes().getNamedItem("domID").getNodeValue();
			if (currDomId.equals(domId))
			{
				reqDetail = currDetail;
				break;
			}			
		}
		return reqDetail;
	}

	private String getCurrentUpdateFlag(Node currDetail)
	{
		NodeList currDetailList = null;
		String updateStatus = "",nodeName = "";
		int currDetailListLength = 0;

		currDetailList = currDetail.getChildNodes();
		currDetailListLength = currDetailList.getLength();
		for (int i=0;i< currDetailListLength;i++)
		{
			nodeName = currDetailList.item(i).getNodeName();
			if (nodeName.equalsIgnoreCase("Attribute"))
			{
				updateStatus = currDetailList.item(i).getAttributes().getNamedItem("updateFlag").getNodeValue();
				break;
			}
		}
		return updateStatus;		
	}
}