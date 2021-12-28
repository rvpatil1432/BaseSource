/* This file is Cteated it incorporate the Pre Save logic from Window
 * Gulzar 08/09/06
 */
package ibase.webitm.ejb.dis;

import javax.ejb.*;
import java.sql.*;
import java.rmi.RemoteException;
import java.util.*;

import org.w3c.dom.*;

import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;
//import ibase.webitm.utility.GenericUtility;
import ibase.system.config.*;
import ibase.webitm.ejb.*;
import javax.ejb.Stateless; // added for ejb3


//public class SReturnPrsEJB extends ValidatorEJB implements SessionBean
@Stateless // added for ejb3
public class SReturnPrs extends ValidatorEJB implements SReturnPrsLocal, SReturnPrsRemote
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	/*
	public void ejbCreate() throws RemoteException, CreateException
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
	public String preSaveRec() throws RemoteException, ITMException
	{
		return "";
	}
	public String preSaveRec(String xmlString1, String domId, String objContext, String editFlag, String xtraParams, Connection conn) throws RemoteException, ITMException
	{
		System.out.println("SReturnPrsEJB Called");
		Document dom = null;
		try
		{
			System.out.println("SReturnPrs xmlString1 [" + xmlString1 + "]");
			System.out.println("SReturnPrs domId [" + domId + "]");
			if (xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom = genericUtility.parseString(xmlString1);
				return (executepreSaveRec(dom,domId,objContext,editFlag,xtraParams,conn));
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :SReturnPrsEJB :preSaveRec(): " + e.getMessage()+ ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return "";
	}
	private String executepreSaveRec(Document dom, String domID, String ObjContext, String editFlag, String xtraParams, Connection conn) throws RemoteException, ITMException
	{
		String siteCode = "", itemCode = "", lineNo = "",locCode = "", lotNo = "", lotSl = "";
		String tranId = "", updateStatus = "", qtyStr = "",errString = "", winName = "";
		String sqlStr = "", retFlg = "";
		double quantity = 0d;
		java.util.Date tranDate = null;
		DistStkUpd distStkUpd = null;
		NodeList hdrDom = null;
		Node currDetail = null;
		Statement stmt = null;
		ResultSet rs = null;
		HashMap strAllocate = new HashMap();
		InvAllocTraceBean invAllocTrace = new InvAllocTraceBean();
		NodeList parentNodeList = null;
		Node parentNode = null;
		String retRepFlag = "";//Added by wasim on 19-MAY-17
		try
		{			
			tranDate = new java.sql.Date(System.currentTimeMillis());
			System.out.println("\n tranDate :"+tranDate);
			hdrDom = dom.getElementsByTagName("Detail1");
			tranId = genericUtility.getColumnValueFromNode("tran_id",hdrDom.item(0));
			siteCode = genericUtility.getColumnValueFromNode("site_code",hdrDom.item(0));
			System.out.println("\n tranId :"+tranId);
			System.out.println("\n siteCode :"+siteCode);
			currDetail 	= getCurrentDetailFromDom(dom,domID);
			updateStatus = getCurrentUpdateFlag(currDetail);
			// 03/03/15 manoharan
			parentNodeList = dom.getElementsByTagName("Detail1");
			parentNode = parentNodeList.item( 0 );
			winName = getWinName(parentNode);
			// end 03/03/15 manoharan
			
			if (currDetail != null && !updateStatus.equalsIgnoreCase("A") && !updateStatus.equalsIgnoreCase("N"))//&& !updateStatus.equalsIgnoreCase("N") CONDITION ADDED BY NANDKUMAR GADKARI ON 14/01/18
			{
				if (conn == null)
				{
					System.out.println("<-----------Connection is null----------------------->");
				}
			
				retFlg = genericUtility.getColumnValueFromNode("ret_rep_flag",currDetail);
				
				//Changed by wasim on 19-MAY-2017 to comment this condition as retFlag will be checked below from database, if P-->Deallocate else if R-->bypass
				//if (retFlg.equalsIgnoreCase("P"))
				{
					lineNo = genericUtility.getColumnValueFromNode("line_no",currDetail);
					itemCode = genericUtility.getColumnValueFromNode("item_code",currDetail);
					locCode = genericUtility.getColumnValueFromNode("loc_code",currDetail);
					lotNo = genericUtility.getColumnValueFromNode("lot_no",currDetail);
					lotSl = genericUtility.getColumnValueFromNode("lot_sl",currDetail);

					stmt = conn.createStatement();
					//Changed by wasim on 19-May-2017 for checking ret_rep_flag if it is R(Return) present in database then not allow to deallocate [START]
					/*
					sqlStr = "SELECT QUANTITY,item_code,loc_code,lot_no,lot_sl FROM SRETURNDET WHERE TRAN_ID = '"+tranId+"'" +
					" AND LINE_NO = "+lineNo+""; //Omited the single quotes as it is not working in DB2 //Gulzar 09-01-07
					*/
					sqlStr = "SELECT QUANTITY,ITEM_CODE,LOC_CODE,LOT_NO,LOT_SL,RET_REP_FLAG FROM SRETURNDET WHERE TRAN_ID = '"+tranId+"'" +
							" AND LINE_NO = "+lineNo+""; 
					//Changed by wasim on 19-May-2017 for checking ret_rep_flag if it is R(Return) present in database then not allow to deallocate [END]
					System.out.println("sqlStr :: " + sqlStr);
					rs = stmt.executeQuery(sqlStr);
					if(rs.next())
					{
						qtyStr = rs.getString(1);
						itemCode = rs.getString("item_code");
						locCode = rs.getString("loc_code");
						lotNo = rs.getString("lot_no");
						lotSl = rs.getString("lot_sl");
						//Added by wasim on 19-May-2017
						retRepFlag = rs.getString("RET_REP_FLAG");
					}
					rs.close();
					rs = null;
					stmt.close();
					stmt = null;
					
					//Changed by wasim on 19-May-2017 for checking ret_rep_flag if it is R(Return) present in database then not allow to deallocate [START]
					System.out.println("Database Old ReturnFlag ["+retRepFlag+"]");
					if("R".equalsIgnoreCase(retRepFlag))
					{
						System.out.println("Previous return type was R (Return) so stock will not be De-allocated");
					}
					else //Old logic inside else
					{
					//Changed by wasim on 19-May-2017 for checking ret_rep_flag if it is R(Return) present in database then not allow to deallocate [END]	
						lineNo ="   " + lineNo;
						System.out.println("Line No : lineNo := "+lineNo);
						quantity = Double.parseDouble(qtyStr);
						/*
						strAllocate.put("tran_date",tranDate);
						strAllocate.put("ref_ser","S-RET");
						strAllocate.put("ref_id",tranId);
						strAllocate.put("ref_line",lineNo.substring(lineNo.length()-3));					
						strAllocate.put("site_code",siteCode);
						strAllocate.put("item_code",itemCode);
						strAllocate.put("loc_code",locCode);
						strAllocate.put("lot_no",lotNo);
						strAllocate.put("lot_sl",lotSl);
						strAllocate.put("alloc_qty",new Double(-1*quantity)); 
						strAllocate.put("chg_user",GenericUtility.getInstance().getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
						strAllocate.put("chg_term",GenericUtility.getInstance().getValueFromXTRA_PARAMS(xtraParams,"termId"));
						strAllocate.put("chg_win","W_SALESRETURN");
						distStkUpd = new DistStkUpd();
						if (distStkUpd.updAllocTrace(strAllocate, conn) > 0) 
						{
							System.out.println("distStkUpd.UpdAllocTrace(HashMap, Connection) : Sucessuful!");
						}
						else
						{
							throw new Exception ("Exception while deallocating old allocation");
						}
						*/
						strAllocate = null;
						strAllocate = new HashMap();
						strAllocate.put("tran_date",tranDate);
						strAllocate.put("ref_ser","S-RET");
						strAllocate.put("ref_id",tranId);
						strAllocate.put("ref_line",lineNo.substring(lineNo.length()-3));					
						strAllocate.put("site_code",siteCode);
						strAllocate.put("item_code",itemCode);
						strAllocate.put("loc_code",locCode);
						strAllocate.put("lot_no",lotNo);
						strAllocate.put("lot_sl",lotSl);
						strAllocate.put("alloc_qty",new Double(-1*quantity)); 
						strAllocate.put("chg_user",genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
						strAllocate.put("chg_term",genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId"));
						strAllocate.put("chg_win",winName);
						
						errString = invAllocTrace.updateInvallocTrace(strAllocate, conn);
						if (errString == null || errString.trim().length() == 0 )
						{
							System.out.println("invAllocTrace.updateInvallocTrace : Sucessuful!");
						}
						else
						{
							throw new Exception ("Exception while deallocating old allocation");
						}
					}
				}
			}
		}
		catch (SQLException sqe)
		{
			System.out.println("The SQLException occurs in SReturnPrsEJB :"+sqe);
			sqe.printStackTrace();
			throw new ITMException(sqe);
		}
		catch(Exception e)
		{
			System.out.println("The Exception occurs in SReturnPrsEJB :"+e);			
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
			}
			catch(Exception ef)
			{
				//System.err.println("Exception :SReturnPrsEJB :\n"+ef.getMessage());
				//ef.printStackTrace();
				//throw new ITMException(ef);
			}
		}
	return errString;
	}
	private Node getCurrentDetailFromDom(Document dom,String domId)
	{
		NodeList detailList = null;
		Node currDetail = null,reqDetail = null;
		String currDomId = "";
		int	detailListLength = 0;

		detailList = dom.getElementsByTagName("Detail2");
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
	private String getWinName(Node node) throws Exception
	{
		String objName = null;
		NodeList nodeList = null;
		Node detaulNode = null;
		Node detailNode = null;
		nodeList = node.getChildNodes();
		NamedNodeMap attrMap = node.getAttributes();
		objName = attrMap.getNamedItem( "objName" ).getNodeValue();
		return "w_" + objName;

	}
}
