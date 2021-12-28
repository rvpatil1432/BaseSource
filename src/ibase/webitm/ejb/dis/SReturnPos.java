/* This file is Cteated it incorporate the Post Save logic from Window
 * Gulzar 11/09/06
 */
package ibase.webitm.ejb.dis;

import java.sql.*;
import java.rmi.RemoteException;
import java.util.*;

import org.w3c.dom.*;
import javax.ejb.*;
import javax.naming.InitialContext;

import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;
//import ibase.webitm.utility.GenericUtility;
import ibase.system.config.*;
import ibase.webitm.ejb.*;
import javax.ejb.Stateless; // added for ejb3


//public class SReturnPosEJB extends ValidatorEJB implements SessionBean
@Stateless // added for ejb3
public class SReturnPos extends ValidatorEJB implements SReturnPosLocal, SReturnPosRemote
{
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
	public String postSaveRec() throws RemoteException, ITMException
	{
		return "";
	}
	public String postSaveRec(String xmlString1, String domId, String objContext, String editFlag, String xtraParams, Connection conn) throws RemoteException, ITMException
	{
		System.out.println("SReturnPosEJB Called............");
		Document dom = null;
		try
		{
			System.out.println("SReturnPos xmlString1 [" + xmlString1 + "]");
			System.out.println("SReturnPos domId [" + domId + "]");
		
			if (xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom = genericUtility.parseString(xmlString1);
				return (executepostSaveRec(dom,domId,objContext,editFlag,xtraParams,conn));
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :SReturnPosEJB :postSaveRec(): " + e.getMessage()+ ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return "";
	}
	private String executepostSaveRec(Document dom, String domID, String ObjContext, String editFlag, String xtraParams, Connection conn) throws RemoteException, ITMException
	{
		String siteCode = "", itemCode = "", lineNo = "",locCode = "", lotNo = "", lotSl = "";
		String tranId = "", updateStatus = "", qtyStr = "", errString = "", winName = "";
		String sqlStr = "", retFlg = "";
		double quantity = 0d;
		java.util.Date tranDate = null;
		NodeList hdrDom = null;
		Node currDetail = null;
		//Statement stmt = null;
		//ResultSet rs = null;
		HashMap strAllocate = new HashMap();
		InvAllocTraceBean invAllocTrace = new InvAllocTraceBean();
		NodeList parentNodeList = null;
		Node parentNode = null;

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
			
			if (currDetail != null && !updateStatus.equalsIgnoreCase("D"))
			{
				//stmt = conn.createStatement();
				if (conn == null)
				{
					System.out.println("<-----------Connection is null----------------------->");
				}
				retFlg = genericUtility.getColumnValueFromNode("ret_rep_flag",currDetail);
				
				if (retFlg.equalsIgnoreCase("P"))
				{
					lineNo = genericUtility.getColumnValueFromNode("line_no",currDetail);
					itemCode = genericUtility.getColumnValueFromNode("item_code",currDetail);
					locCode = genericUtility.getColumnValueFromNode("loc_code",currDetail);
					lotNo = genericUtility.getColumnValueFromNode("lot_no",currDetail);
					lotSl = genericUtility.getColumnValueFromNode("lot_sl",currDetail);
					qtyStr = genericUtility.getColumnValueFromNode("quantity",currDetail);
					quantity = Double.parseDouble(qtyStr);
				
					lineNo ="   " + lineNo;
					/*strAllocate.put("tran_date",tranDate);
					strAllocate.put("ref_ser","S-RET");
					strAllocate.put("ref_id",tranId);
					strAllocate.put("ref_line",lineNo.substring(lineNo.length()-3));					
					strAllocate.put("site_code",siteCode);
					strAllocate.put("item_code",itemCode);
					strAllocate.put("loc_code",locCode);
					strAllocate.put("lot_no",lotNo);
					strAllocate.put("lot_sl",lotSl);
					strAllocate.put("alloc_qty",new Double(quantity)); 
					strAllocate.put("chg_user",GenericUtility.getInstance().getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
					strAllocate.put("chg_term",GenericUtility.getInstance().getValueFromXTRA_PARAMS(xtraParams,"termId"));
					strAllocate.put("chg_win","W_SALESRETURN");					
					DistStkUpd distStkUpd = new DistStkUpd();
					if (distStkUpd.updAllocTrace(strAllocate, conn) > 0) 
					{
						distStkUpd=null;
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
					strAllocate.put("alloc_qty",new Double(quantity)); 
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
						throw new Exception ("Exception while allocating stock ");
					}
					

				}
			}
		}
		catch (SQLException sqe)
		{
			System.out.println("The SQLException occurs in SReturnPosEJB...... :"+sqe);
			sqe.printStackTrace();
			throw new ITMException(sqe);
		}
		catch(Exception e)
		{
			System.out.println("The Exception occurs in SReturnPosEJB........ :"+e);			
			e.printStackTrace();
			throw new ITMException(e);
		}
		/*finally
		{
			try
			{
				if(stmt != null)
				{
					stmt.close();
					stmt = null;
				}
			}
			catch(Exception e)
			{
				System.err.println("Exception :SReturnPosEJB......... :\n"+e.getMessage());
				e.printStackTrace();
				throw new ITMException(e);
			}
		}*/
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
