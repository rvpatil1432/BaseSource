package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;
import javax.ejb.*;
import javax.naming.InitialContext;
import org.w3c.dom.*;

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.*;
import ibase.system.config.*;
import ibase.webitm.utility.ITMException;
//import ibase.webitm.utility.GenericUtility;
import javax.ejb.Stateless; // added for ejb3


//public class InvPackPosEJB extends ValidatorEJB implements SessionBean
@Stateless // added for ejb3
public class InvPackPos extends ValidatorEJB implements InvPackPosLocal, InvPackPosRemote
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

    public String postSaveRec()throws RemoteException,ITMException
	{
		return "";
	}

	public String postSaveRec(String xmlString1,String domId,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{
		Document dom = null;
		System.out.println("InvPackPosEJB called");
		try
		{
			if (xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom = genericUtility.parseString(xmlString1);
				executepostSaveRec(dom,domId,objContext,editFlag,xtraParams,conn);
			}			
		}
	   	catch(Exception e)
		{
			System.out.println("Exception :InvPackPosEJB ::" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return "";
	}

	private String executepostSaveRec(Document dom, String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{
		String siteCode = "", itemCode = "", lineNo = "",locCode = "", lotNo = "", lotSl = "";
		String sql = "", line = "", tranType = "", stockOpt = "", validateStock = "", errCode = "", errString = "";
		double quantity = 0d,excessShortQty = 0d;
		int updateCnt = 0;
		java.sql.Date tranDate = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		HashMap strAllocate = new HashMap();
		DistStkUpd distStkUpd = null;
		NodeList hdrDom = null;
		Node currDetail = null;
		String updateStatus = "",tranId = "",quantityStr = "",excessShortQtyStr = "";
		try
		{			
			tranDate = new java.sql.Date(System.currentTimeMillis());
			System.out.println("\n tranDate :"+tranDate);

			hdrDom = dom.getElementsByTagName("Detail1");
			tranType = genericUtility.getColumnValueFromNode("tran_type",hdrDom.item(0));
			siteCode = genericUtility.getColumnValueFromNode("site_code",hdrDom.item(0));
			validateStock = genericUtility.getColumnValueFromNode("validate_stock",hdrDom.item(0));
			tranId = genericUtility.getColumnValueFromNode("tran_id",hdrDom.item(0));
			System.out.println("\n tranId :"+tranId);
			System.out.println("validateStock :: "+validateStock);

			if (validateStock.equals("Y"))
			{
				currDetail = getCurrentDetailFromDom(dom,domID);
				updateStatus = getCurrentUpdateFlag(currDetail);

				if (currDetail != null && !updateStatus.equalsIgnoreCase("D"))
				{
					lineNo = genericUtility.getColumnValueFromNode("line_no",currDetail);
					itemCode = genericUtility.getColumnValueFromNode("item_code",currDetail);
					locCode = genericUtility.getColumnValueFromNode("loc_code",currDetail);
					lotNo = genericUtility.getColumnValueFromNode("lot_no",currDetail);
					lotSl = genericUtility.getColumnValueFromNode("lot_sl",currDetail);
					quantityStr = genericUtility.getColumnValueFromNode("quantity",currDetail);
					excessShortQtyStr=genericUtility.getColumnValueFromNode("excess_short_qty",currDetail);
					excessShortQtyStr  = (excessShortQtyStr == null ? "0" : excessShortQtyStr.trim());
					excessShortQty = Double.parseDouble(excessShortQtyStr);
					quantity = Double.parseDouble(quantityStr);
					quantity = quantity +excessShortQty;

					line = "   " + lineNo;
					strAllocate.put("tran_date",tranDate);
					strAllocate.put("ref_ser","I-PKI");
					strAllocate.put("ref_id",tranId);
					strAllocate.put("ref_line",line.substring(line.length()-3));					
					strAllocate.put("site_code",siteCode);
					strAllocate.put("item_code",itemCode);
					strAllocate.put("loc_code",locCode);
					strAllocate.put("lot_no",lotNo);
					strAllocate.put("lot_sl",lotSl);
					strAllocate.put("alloc_qty",new Double(quantity)); 
					strAllocate.put("chg_user",genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
					strAllocate.put("chg_term",genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId"));
					strAllocate.put("chg_win","W_INV_PACK");
					
					System.out.println("alloc_qty"+ strAllocate.get("alloc_qty"));
					//if (distStkUpd.updAllocTrace(strAllocate) > 0)
					/*Changed by HATIM on 13/01/2006*/
					//pavan R 20/jul/18 changed the lookup to creating instance of the class using new keyword.
					distStkUpd = new DistStkUpd();
					if (distStkUpd.updAllocTrace(strAllocate, conn) > 0)
					{
						distStkUpd = null;
						System.out.println("distStkUpd.UpdAllocTrace(HashMap, Connection) : Sucessuful!");
					}
					/*END*/
				}//End
			}//end if
		}//try end
		/*catch (SQLException sqx)
		{
			System.out.println("The SQLException occurs in InvPackPosEJB :"+sqx);
			sqx.printStackTrace();
			throw new ITMException(sqx);
		}*/
		catch(Exception e)
		{
			System.out.println("The SQLException occurs in InvPackPosEJB :"+e);			
			e.printStackTrace();
			throw new ITMException(e);
		}
		return "";
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
}