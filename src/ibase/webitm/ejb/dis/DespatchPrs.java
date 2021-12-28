package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;
import org.w3c.dom.*;

import javax.ejb.*;
import javax.naming.InitialContext;

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.*;
import ibase.webitm.utility.ITMException;
//import ibase.webitm.utility.GenericUtility;
import ibase.system.config.*;
import javax.ejb.Stateless; // added for ejb3


//public class DespatchPrsEJB extends ValidatorEJB implements SessionBean
@Stateless // added for ejb3
public class DespatchPrs extends ValidatorEJB implements DespatchPrsLocal, DespatchPrsRemote
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

    public String preSaveRec() throws RemoteException,ITMException
	{
		return "";
	}

    public String preSaveRec(String xmlString1,String domId,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{

		//System.out.println("DespatchPrsEJB called");
		Document dom = null;
		try
		{
			//System.out.println("xmlString1 in DespatchPosEJB :: preSaveRec \n"+xmlString1);			
			//System.out.println("domId in DespatchPosEJB :: preSaveRec \n"+domId);			
			if (xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom = genericUtility.parseString(xmlString1);
				if (xmlString1 != null && xmlString1.trim().length() > 0)
				{
					dom = genericUtility.parseString(xmlString1);
					//if( (xmlString1.toUpperCase()).contains("DETAIL2") ||  (xmlString1.toUpperCase()).contains("DETAIL3") )
					if( (xmlString1.toUpperCase()).contains("DETAIL3") )
					{
						objName="despatchwiz";
						detailNode="detail3";
						
					}
					else
					{
						objName="";
						detailNode="detail2";
					}
					executepreSaveRec(dom,domId,objContext,editFlag,xtraParams,conn);
				}
				
				
			}			
		}
	   	catch(Exception e)
		{
			//System.out.println("Exception :DespatchPrsEJB ::" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return "";
	}

	private String executepreSaveRec(Document dom, String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{
		Statement stmt = null,stmt1 = null;
		ResultSet rs = null, rs1 = null;
		PreparedStatement pstmt = null;
		String siteCode = "", itemCode = "", lineNo = "",locCode = "", lotNo = "", lotSl = "";
		String expLev = "", itemCodeOrd = "", sordNo = "",lineNoSord = "", sql = "";
		HashMap strAllocate = new HashMap();
		String sqlSordAlloc = "", sqlSord = "", sqlSordItem = "", line = "";
		double qtyAlloc = 0d, qtyStduom = 0d;
		int updateCnt = 0, retVal = 0, count =0;
		java.sql.Date tranDate = null;		   	
		//GenericUtility genericUtility = GenericUtility.getInstance();		
		String tranId = "",qtyStduomStr = "";
		NodeList hdrDom = null;
		Node currDetail = null; 
		String updateStatus = "";
		String sordNumber = "";//Modified by Rohini T on 28/06/2021
		try
		{
			stmt = conn.createStatement();
			stmt1 = conn.createStatement();			
			////System.out.println("TranId ::"+tranId);
			tranDate = new java.sql.Date(System.currentTimeMillis());
			//System.out.println("\n tranDate :"+tranDate);
			if("despatchwiz".equalsIgnoreCase(objName))
			{
				hdrDom = dom.getElementsByTagName("Detail2");
			}
			else
			{
			hdrDom = dom.getElementsByTagName("Detail1");
			}
			
			siteCode = genericUtility.getColumnValueFromNode("site_code",hdrDom.item(0));
			tranId = genericUtility.getColumnValueFromNode("desp_id",hdrDom.item(0));
			//System.out.println("Site Code :: "+siteCode);
			//System.out.println("Tran ID :: "+tranId);
			
			//sql = "SELECT SITE_CODE FROM DESPATCH WHERE DESP_ID = '"+tranId+"'";
			////System.out.println("sql :"+sql);
			//rs = stmt.executeQuery(sql);
			//if (rs.next())
			//{
			//	siteCode = rs.getString(1);
			//	//System.out.println("siteCode :"+siteCode);
			//}


			//sql = "SELECT ITEM_CODE, LINE_NO, LOC_CODE, LOT_NO, LOT_SL, QUANTITY__STDUOM, "
			//	  +"EXP_LEV, ITEM_CODE__ORD, SORD_NO, LINE_NO__SORD "
			//	  +"FROM DESPATCHDET WHERE DESP_ID = '"+tranId+"'";
			////System.out.println("sql :"+sql);
			//rs = stmt.executeQuery(sql);
			
			
			currDetail	 = getCurrentDetailFromDom(dom,domID);
			
			if (tranId == null || tranId.trim().length() == 0)
			{
				tranId  = genericUtility.getColumnValueFromNode("tran_id",currDetail);
			}
			//System.out.println("Tran ID from currDetail  ["+tranId + "]");
			
			updateStatus = getCurrentUpdateFlag(currDetail);
			
			//System.out.println("manohar 10/11/14 executepreSaveRec DomID ["+domID+"] Update Status ["+updateStatus + "]");
			
			//01/11/2012 manoj sharma for deallocation
			int length=0;
			NodeList parentNodeList = null;
			Node parentNode = null;
			
			parentNodeList = dom.getElementsByTagName(detailNode);
			parentNode = parentNodeList.item(0);
			length=dom.getElementsByTagName(detailNode).getLength();
			//System.out.println("Detail Node+++"+detailNode);
			
			String currlineNo	= "";
			String keyString="";
			String currkey="";
			double totAllocQty=0;
			if (currDetail != null ) //&& !updateStatus.equalsIgnoreCase("A")) // 22/02/10 manoharan if allocation found in sordalloc the deallocate
			{

				//if (!updateStatus.equalsIgnoreCase("A")) // 09/10/14 manoharan edit/delete mode get detail from database
				
				lineNo			= genericUtility.getColumnValueFromNode("line_no",currDetail);
         		currlineNo      =genericUtility.getColumnValueFromNode("line_no",currDetail);
				itemCode		= genericUtility.getColumnValueFromNode("item_code",currDetail);
				locCode			= genericUtility.getColumnValueFromNode("loc_code",currDetail);
				lotNo			= genericUtility.getColumnValueFromNode("lot_no",currDetail);
				lotSl			= genericUtility.getColumnValueFromNode("lot_sl",currDetail);
				qtyStduomStr	= genericUtility.getColumnValueFromNode("quantity__stduom",currDetail);
				expLev			= genericUtility.getColumnValueFromNode("exp_lev",currDetail);
				itemCodeOrd		= genericUtility.getColumnValueFromNode("item_code__ord",currDetail);
				sordNo			= genericUtility.getColumnValueFromNode("sord_no",currDetail);
				lineNoSord		= genericUtility.getColumnValueFromNode("line_no__sord",currDetail);

				itemCode = itemCode == null ? "" :itemCode;
				sordNo = sordNo == null ? "" :sordNo;
				lineNoSord = lineNoSord == null ? "" :lineNoSord;
				locCode = locCode == null ? "" :locCode;
				lotNo = lotNo == null ? "" :lotNo;
				lotSl = lotSl == null ? "" :lotSl;
				expLev = expLev == null ? "" :expLev;
				itemCodeOrd = itemCodeOrd == null ? "" :itemCodeOrd;

				currkey=itemCode.trim()+"@"+sordNo.trim()+"@"+lineNoSord.trim()+"@"+locCode.trim()+"@"+lotNo.trim()+"@"+lotSl.trim()+"@"+expLev.trim();
				//System.out.println("currkey---["+currkey + "]");
				sql="SELECT SUM(QUANTITY)" +
					" FROM DESPATCHDET B,DESPATCH A "
					+" WHERE B.SORD_NO = ?  " +
					 " AND A.DESP_ID = B.DESP_ID "
					+" AND B.LINE_NO__SORD = ? "
					+" AND B.EXP_LEV = ? "
					+" AND B.ITEM_CODE__ORD = ? "
					+" AND B.ITEM_CODE = ? "
					+" AND B.LOT_NO = ? "
					+" AND B.LOT_SL = ? "
					+" AND B.LOC_CODE = ? AND B.DESP_ID<>?" 
					+" AND(CASE WHEN A.CONFIRMED IS  NULL THEN 'N' ElSE A.CONFIRMED END)='N'" ;
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,sordNo);
				pstmt.setString(2,lineNoSord);
				pstmt.setString(3,expLev);
				pstmt.setString(4,itemCodeOrd);
				pstmt.setString(5,itemCode);
				pstmt.setString(6,lotNo);
				pstmt.setString(7,lotSl);
				pstmt.setString(8,locCode);
				pstmt.setString(9,tranId);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					//count = rs.getInt(1); // 
					totAllocQty = rs.getDouble(1);
				}
				//System.out.println("totAllocQty--from Other despatch----"+totAllocQty);
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
				
			}
			//System.out.println("Length of for loop>>"+length);
			for(int i =0; i<length ; i++)
			{
				itemCode=(genericUtility.getColumnValueFromNode("item_code",dom.getElementsByTagName(detailNode).item(i))).trim();
				lineNo=(genericUtility.getColumnValueFromNode("line_no",dom.getElementsByTagName(detailNode).item(i))).trim();
				sordNo=(genericUtility.getColumnValueFromNode("sord_no",dom.getElementsByTagName(detailNode).item(i))).trim();
				lineNoSord=(genericUtility.getColumnValueFromNode("line_no__sord",dom.getElementsByTagName(detailNode).item(i))).trim();
				qtyStduom=Double.parseDouble((genericUtility.getColumnValueFromNode("quantity__stduom",dom.getElementsByTagName(detailNode).item(i))).trim());
				//System.out.println("qtyStduom----"+qtyStduom);
				locCode=(genericUtility.getColumnValueFromNode("loc_code",dom.getElementsByTagName(detailNode).item(i))).trim();
				lotNo=(genericUtility.getColumnValueFromNode("lot_no",dom.getElementsByTagName(detailNode).item(i))).trim();
				lotSl=(genericUtility.getColumnValueFromNode("lot_sl",dom.getElementsByTagName(detailNode).item(i))).trim();
				expLev=(genericUtility.getColumnValueFromNode("exp_lev",dom.getElementsByTagName(detailNode).item(i))).trim();
				itemCodeOrd=(genericUtility.getColumnValueFromNode("item_code__ord",dom.getElementsByTagName(detailNode).item(i))).trim();
				keyString=itemCode.trim()+"@"+sordNo.trim()+"@"+lineNoSord.trim()+"@"+locCode.trim()+"@"+lotNo.trim()+"@"+lotSl.trim()+"@"+expLev.trim();
				//System.out.println("keyString----"+keyString);
				//System.out.println("currlineNo----"+currlineNo);
				//System.out.println("lineNo----"+lineNo);
				
				if(!currlineNo.trim().equalsIgnoreCase(lineNo.trim()))
				{
					if(currkey.equalsIgnoreCase(keyString))
					{
						totAllocQty+=qtyStduom;
					}
				}
				
				
			}
			//System.out.println("totAllocQty--from all Entries other than Currect Record----"+totAllocQty);
			
			//01/11/2012 manoj sharma for deallocation end
			if (currDetail != null ) //&& !updateStatus.equalsIgnoreCase("A")) // 22/02/10 manoharan if allocation found in sordalloc the deallocate
			{
				itemCode		= genericUtility.getColumnValueFromNode("item_code",currDetail);
				lineNo			= genericUtility.getColumnValueFromNode("line_no",currDetail);
				locCode			= genericUtility.getColumnValueFromNode("loc_code",currDetail);
				lotNo			= genericUtility.getColumnValueFromNode("lot_no",currDetail);
				lotSl			= genericUtility.getColumnValueFromNode("lot_sl",currDetail);
				qtyStduomStr	= genericUtility.getColumnValueFromNode("quantity__stduom",currDetail);
				expLev			= genericUtility.getColumnValueFromNode("exp_lev",currDetail);
				itemCodeOrd		= genericUtility.getColumnValueFromNode("item_code__ord",currDetail);
				sordNo			= genericUtility.getColumnValueFromNode("sord_no",currDetail);
				lineNoSord		= genericUtility.getColumnValueFromNode("line_no__sord",currDetail);
				// 04/04/12 manoharan
				if (qtyStduomStr == null || "null".equals(qtyStduomStr) || qtyStduomStr.trim().length() ==0 )
				{
					qtyStduomStr = "0";
				}
				qtyStduom = Double.parseDouble(qtyStduomStr);	
				// 22/02/10 manoharan if allocation found in sordalloc the deallocate
				if (updateStatus.equalsIgnoreCase("A"))
				{
					//sql = "SELECT COUNT(1) AS COUNT FROM SORDALLOC " // 04/04/12 manoharan
					sql = "SELECT QTY_ALLOC AS COUNT FROM SORDALLOC " // 04/04/12 manoharan
						  +"WHERE SALE_ORDER = ? "
						  +"AND LINE_NO = ? "
						  +"AND EXP_LEV = ? "
						  +"AND ITEM_CODE__ORD = ? "
						  +"AND ITEM_CODE = ? "
						  +"AND LOT_NO = ? "
						  +"AND LOT_SL = ? "
						  +"AND LOC_CODE = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,sordNo);
					pstmt.setString(2,lineNoSord);
					pstmt.setString(3,expLev);
					pstmt.setString(4,itemCodeOrd);
					pstmt.setString(5,itemCode);
					pstmt.setString(6,lotNo);
					pstmt.setString(7,lotSl);
					pstmt.setString(8,locCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						//count = rs.getInt(1); // 
						qtyAlloc = rs.getDouble(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					//System.out.println("Length of for loop>>"+length);
					//System.out.println("qtyallocation>>"+qtyAlloc);
					//System.out.println("totalallocation qty>>>>"+totAllocQty);
					//System.out.println("Qtystduom"+qtyStduom);
					// 22/02/10 manoharan if allocation found in sordalloc then deallocate
					//if (count > 0 ) // 04/04/12 manoharan
					//if (qtyAlloc > 0) // 04/04/12 manoharan
					if((qtyAlloc >= totAllocQty) && (qtyAlloc > 0))//01/11/2012 manoj sharma for deallocation
						//changed to qtyAlloc >=totAllocQty from qtyAlloc > totAllocQty by Priyanka Das 09/11/2015	
						//Added (qtyAlloc >0) condition by Priyanka Das 07/12/2015
					{
						if (qtyAlloc - qtyStduom <= 0)
						{
							sqlSord = "DELETE FROM SORDALLOC WHERE SALE_ORDER = ? "
								 +"AND LINE_NO = ? "
								 +"AND EXP_LEV = ? " 
								 +"AND ITEM_CODE__ORD = ? " 
								 +"AND ITEM_CODE = ? " 
								 +"AND LOT_NO = ? " 
								 +"AND LOT_SL = ? "
								 +"AND LOC_CODE = ? ";
							//System.out.println("Delete sql sqlSord :"+sqlSord);
							pstmt = conn.prepareStatement(sqlSord);
							pstmt.setString(1,sordNo);
							pstmt.setString(2,lineNoSord);
							pstmt.setString(3,expLev);
							pstmt.setString(4,itemCodeOrd);
							pstmt.setString(5,itemCode);
							pstmt.setString(6,lotNo);
							pstmt.setString(7,lotSl);
							pstmt.setString(8,locCode);
							updateCnt = pstmt.executeUpdate();
							pstmt.close();
							pstmt = null;
							//System.out.println("Deleted the no of records are : updateCnt :"+updateCnt);
						}
						else
						{
							sqlSord = "UPDATE SORDALLOC SET QTY_ALLOC = QTY_ALLOC - ? "
								  +"WHERE SALE_ORDER = ? "
								  +"AND LINE_NO = ? "
								  +"AND EXP_LEV = ? "
								  +"AND ITEM_CODE__ORD = ? "
								  +"AND ITEM_CODE = ? "
								  +"AND LOT_NO = ? "
								  +"AND LOT_SL = ? "
								  +"AND LOC_CODE = ? ";
							//System.out.println("Update sql sqlSord :"+sqlSord);
							pstmt = conn.prepareStatement(sqlSord);
							pstmt.setDouble(1,qtyStduom);
							pstmt.setString(2,sordNo);
							pstmt.setString(3,lineNoSord);
							pstmt.setString(4,expLev);
							pstmt.setString(5,itemCodeOrd);
							pstmt.setString(6,itemCode);
							pstmt.setString(7,lotNo);
							pstmt.setString(8,lotSl);
							pstmt.setString(9,locCode);
							updateCnt = pstmt.executeUpdate();
							pstmt.close();
							pstmt = null;
							//System.out.println("Updated the no of records are : updateCnt :"+updateCnt);
						}
						sqlSordItem = "UPDATE SORDITEM SET QTY_ALLOC = QTY_ALLOC - ? "
									 +"WHERE SALE_ORDER = ? "
									 +"AND LINE_NO = ? "
									 +"AND EXP_LEV = ? ";
						//System.out.println("Update sql sqlSordItem :"+sqlSordItem);
						pstmt = conn.prepareStatement(sqlSordItem);
						pstmt.setDouble(1,qtyStduom);
						pstmt.setString(2,sordNo);
						pstmt.setString(3,lineNoSord);
						pstmt.setString(4,expLev);
						updateCnt = pstmt.executeUpdate();
						pstmt.close();
						pstmt = null;
						//System.out.println("Updated the no of records : updateCnt :"+updateCnt);

						line = "   " + lineNo;
						//System.out.println("line :"+line);
						
						strAllocate.put("tran_date",tranDate);	
						
						strAllocate.put("ref_id",tranId);
						strAllocate.put("ref_line",line.substring(line.length()-3));
						strAllocate.put("site_code",siteCode);
						strAllocate.put("item_code",itemCode);
						strAllocate.put("loc_code",locCode);
						strAllocate.put("lot_no",lotNo);
						strAllocate.put("lot_sl",lotSl);
						strAllocate.put("alloc_qty",new Double(-1*qtyStduom)); 
						strAllocate.put("chg_user",genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
						strAllocate.put("chg_term",genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId"));
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
						String logMsg= tranId +" "+expLev+" "+line.substring(line.length()-3) + " "+"Deallocation of stock from DespatchPrs";
						strAllocate.put("alloc_ref",logMsg);	
						//added by nandkumar gadkari on 17/04/19-------end=----------
						//Calling DistStkUpdEJB
						//System.out.println("Calling DistStkUpdEJB.....");
						/*if (distStkUpd.updAllocTrace(strAllocate, conn) > 0)
						{
							//System.out.println("distStkUpd.UpdAllocTrace(hashMap) : Sucessuful!");
						}*/
						// commented by 	Nandkumar gadkari on 10/01/18 for invalid entry in invalloc_trace
						 InvAllocTraceBean invBean = new InvAllocTraceBean(); 
						 String errString = invBean.updateInvallocTrace(strAllocate,conn);
							if(errString == null && errString.trim().length() == 0){
							   //System.out.println("distStkUpd.UpdAllocTrace(hashMap) : Sucessuful!");
							} // commented by 	Nandkumar gadkari on 10/01/18 for invalid entry in invalloc_trace
					} //  end 22/02/10 manoharan

				}
				// end 22/02/10 manoharan
				else if (updateStatus.equalsIgnoreCase("E") || updateStatus.equalsIgnoreCase("D"))	//if codn added by jiten 19/05/06
				{
					//Commented And Changes Below - Gulzar 10/05/07
					//sql = "SELECT QUANTITY__STDUOM FROM DESPATCHDET WHERE DESP_ID = '"+tranId+"' AND LINE_NO = '"+lineNo+"'"; //Gulzar - 10/05/07
					
					lineNo = ("   " + lineNo) ;
					lineNo = lineNo.substring(lineNo.length()-3);

					//sql = "SELECT LINE_NO__SORD, EXP_LEV, ITEM_CODE__ORD, ITEM_CODE, LOC_CODE, LOT_NO, LOT_SL, QUANTITY__STDUOM  "//Modified by Rohini T on 28/06/2021
					sql = "SELECT LINE_NO__SORD, EXP_LEV, ITEM_CODE__ORD, ITEM_CODE, LOC_CODE, LOT_NO, LOT_SL, QUANTITY__STDUOM, SORD_NO  "
						 +"FROM DESPATCHDET WHERE DESP_ID = '"+tranId+"' AND LINE_NO = '"+lineNo+"'";
					//System.out.println("SQL ::"+sql);
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						lineNoSord	 = rs.getString("LINE_NO__SORD");  //Gulzar 10/05/07	
						expLev		 = rs.getString("EXP_LEV");		   //Gulzar 10/05/07	
						itemCodeOrd	 = rs.getString("ITEM_CODE__ORD"); //Gulzar 10/05/07	
						itemCode	 = rs.getString("ITEM_CODE");	   //Gulzar 10/05/07	
						locCode		 = rs.getString("LOC_CODE");	   //Gulzar 10/05/07			
						lotNo		 = rs.getString("LOT_NO");		   //Gulzar 10/05/07		
						lotSl		 = rs.getString("LOT_SL");		   //Gulzar 10/05/07	
						qtyStduomStr = rs.getString("QUANTITY__STDUOM");	
						sordNumber = rs.getString("SORD_NO");//Modified by Rohini T on 28/06/2021
						System.out.println("sordNumber......"+sordNumber);
						//End Changes - Gulzar 10/05/07
					

						qtyStduom = Double.parseDouble(qtyStduomStr);
						//sqlSordAlloc = "SELECT QTY_ALLOC FROM SORDALLOC WHERE SALE_ORDER = '"+sordNo+"' "
						sqlSordAlloc = "SELECT QTY_ALLOC FROM SORDALLOC WHERE SALE_ORDER = '"+sordNumber+"' "//Modified by Rohini T on 28/06/2021
										+"AND LINE_NO =	'"+lineNoSord+"' "
										+"AND EXP_LEV =	'"+expLev+"' "
										+"AND ITEM_CODE__ORD = '"+itemCodeOrd+"' "
										+"AND ITEM_CODE	= '"+itemCode+"' "
										+"AND LOC_CODE = '"+locCode+"' "
										+"AND LOT_NO = '"+lotNo+"' "
										+"AND LOT_SL = '"+lotSl+"' ";
									//	+"AND ALLOC_MODE = 'A' "; //Commented by Jiten 10/10/06 as Commented in PB - manual allocation should also get deallocated.
						//System.out.println("manohar Select SQL sqlSordAlloc ["+sqlSordAlloc + "]");
						//System.out.println("manohar executepreSaveRec DomID ["+domID+"] Update Status ["+updateStatus + "]");
						rs1 = stmt1.executeQuery(sqlSordAlloc);
						if (rs1.next())
						{
							qtyAlloc = rs1.getDouble(1);
							////System.out.println("qtyAlloc :"+qtyAlloc);

							if (String.valueOf(qtyAlloc) == null)
							{
								qtyAlloc = 0;
							}
							// 22/02/10 manoharan if allocation found in sordalloc then deallocate
							
							//System.out.println("manohar executepreSaveRec qtyAlloc ["+qtyAlloc+"] qtyStduom ["+qtyStduom + "]");
							if (qtyAlloc - qtyStduom <= 0)
							{
								//System.out.println("manohar executepreSaveRec deleting qtyAlloc ["+qtyAlloc+"] qtyStduom ["+qtyStduom + "]");
								sqlSord = "DELETE FROM SORDALLOC WHERE SALE_ORDER = ? "
									 +"AND LINE_NO = ? "
									 +"AND EXP_LEV = ? " 
									 +"AND ITEM_CODE__ORD = ? " 
									 +"AND ITEM_CODE = ? " 
									 +"AND LOT_NO = ? " 
									 +"AND LOT_SL = ? "
									 +"AND LOC_CODE = ? ";
								////System.out.println("Delete sql sqlSord :"+sqlSord);
								pstmt = conn.prepareStatement(sqlSord);
							//	pstmt.setString(1,sordNo);//Modified by Rohini T on 28/06/2021
								pstmt.setString(1,sordNumber);
								pstmt.setString(2,lineNoSord);
								pstmt.setString(3,expLev);
								pstmt.setString(4,itemCodeOrd);
								pstmt.setString(5,itemCode);
								pstmt.setString(6,lotNo);
								pstmt.setString(7,lotSl);
								pstmt.setString(8,locCode);
								updateCnt = pstmt.executeUpdate();
								pstmt.close();
								pstmt = null;
								//System.out.println("Deleted the no of records are : updateCnt :"+updateCnt);
							}
							else
							{
								//System.out.println("manohar executepreSaveRec updating qtyAlloc ["+qtyAlloc+"] qtyStduom ["+qtyStduom + "]");
								sqlSord = "UPDATE SORDALLOC SET QTY_ALLOC = QTY_ALLOC - ? "
									  +"WHERE SALE_ORDER = ? "
									  +"AND LINE_NO = ? "
									  +"AND EXP_LEV = ? "
									  +"AND ITEM_CODE__ORD = ? "
									  +"AND ITEM_CODE = ? "
									  +"AND LOT_NO = ? "
									  +"AND LOT_SL = ? "
									  +"AND LOC_CODE = ? ";
								////System.out.println("Update sql sqlSord :"+sqlSord);
								pstmt = conn.prepareStatement(sqlSord);
								pstmt.setDouble(1,qtyStduom);
								//pstmt.setString(2,sordNo);//Modified by Rohini T on 28/06/2021
								pstmt.setString(2,sordNumber);
								pstmt.setString(3,lineNoSord);
								pstmt.setString(4,expLev);
								pstmt.setString(5,itemCodeOrd);
								pstmt.setString(6,itemCode);
								pstmt.setString(7,lotNo);
								pstmt.setString(8,lotSl);
								pstmt.setString(9,locCode);
								updateCnt = pstmt.executeUpdate();
								pstmt.close();
								pstmt = null;
								//System.out.println("Updated the no of records are : updateCnt :"+updateCnt);
							}
						}
						rs1.close();
						rs1 = null;
						stmt1.close();
						stmt1 = null;
						sqlSordItem = "UPDATE SORDITEM SET QTY_ALLOC = QTY_ALLOC - ? "
									 +"WHERE SALE_ORDER = ? "
									 +"AND LINE_NO = ? "
									 +"AND EXP_LEV = ? ";
						//System.out.println("Update sql sqlSordItem :"+sqlSordItem);
						pstmt = conn.prepareStatement(sqlSordItem);
						pstmt.setDouble(1,qtyStduom);
						//pstmt.setString(2,sordNo);//Modified by Rohini T on 28/06/2021
						pstmt.setString(2,sordNumber);
						pstmt.setString(3,lineNoSord);
						pstmt.setString(4,expLev);
						updateCnt = pstmt.executeUpdate();
						pstmt.close();
						pstmt = null;
						//System.out.println("Updated the no of records : updateCnt :"+updateCnt);

					}
					rs.close();
					rs = null;
					stmt.close();
					stmt = null;
					
					line = "   " + lineNo;
					//System.out.println("line :"+line);
					
					strAllocate.put("tran_date",tranDate);					
					strAllocate.put("ref_id",tranId);
					strAllocate.put("ref_line",line.substring(line.length()-3));
					strAllocate.put("site_code",siteCode);
					strAllocate.put("item_code",itemCode);
					strAllocate.put("loc_code",locCode);
					strAllocate.put("lot_no",lotNo);
					strAllocate.put("lot_sl",lotSl);
					strAllocate.put("alloc_qty",new Double(-1*qtyStduom)); 
					strAllocate.put("chg_user",genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
					strAllocate.put("chg_term",genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId"));
					
					
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
					String logMsg= tranId +" "+expLev+" "+line.substring(line.length()-3) + " "+"Deallocation of stock from DespatchPrs";
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
				}
			}			
		}//try end
		catch (SQLException sqx)
		{
			//System.out.println("The SQLException occurs in DespatchPreSaveEJB :"+sqx);
			sqx.printStackTrace();
			throw new ITMException(sqx);
		}
		catch(Exception e)
		{
			//System.out.println("The SQLException occurs in DespatchPreSaveEJB :"+e);			
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(stmt != null)
				{
					stmt.close();
					stmt = null;
				}
				if (stmt1 != null)
				{
					stmt1.close();
					stmt1 = null;
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