/* 
	Developed by : HATIM LAXMIDHAR
	Discription	 : Post Save for the Adjustment Issue.
	Date		 : 01/01/2006
*/

package ibase.webitm.ejb.dis;
// commit check
import java.rmi.RemoteException;
import java.lang.String;
import java.sql.*;
import javax.ejb.*;
import java.util.HashMap;
import org.w3c.dom.*;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
//import ibase.webitm.utility.GenericUtility;
import ibase.utility.E12GenericUtility;
import ibase.system.config.*;
//public class AdjIssPosEJB extends ValidatorEJB implements SessionBean // commented for ejb3
import javax.ejb.Stateless; // added for ejb3
// commit comment
@Stateless // added for ejb3
public class AdjIssPos extends ValidatorEJB implements AdjIssPosLocal, AdjIssPosRemote  //added for ejb3
{
	/* commented for ejb3
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

	public String postSaveRec()throws RemoteException,ITMException
	{
		return "";
	}
	
	public String postSaveRec(String xmlString1,String domId,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{
		Document dom = null;
		System.out.println("AdjIssPosEJB called");
		try
		{
			E12GenericUtility genericUtility= new  E12GenericUtility();
			if (xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom = genericUtility.parseString(xmlString1);
				//dom = GenericUtility.getInstance().parseString(xmlString1);
				executepostSaveRec(dom,domId,objContext,editFlag,xtraParams,conn);
			}			
		}
	   	catch(Exception e)
		{
			System.out.println("Exception :AdjIssPosEJB ::" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return "";
	}

	private String executepostSaveRec(Document dom, String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{

		HashMap hashMap = new HashMap(); 
		String lineNo = ""; //kfld2
		String tranSer = "ADJISS";
		String line = "";
		String siteCode = "", itemCode = "", quantity = "", locCode = "", lotNo = "", lotSl = "";
		DistStkUpd distStkUpd = null; // for ejb3
		NodeList hdrDom = null;
		Node currDetail = null;
		String updateStatus = "",tranId = "";
		try
		{						
			E12GenericUtility genericUtility= new  E12GenericUtility();
			hdrDom = dom.getElementsByTagName("Detail1");
			/*siteCode = GenericUtility.getInstance().getColumnValueFromNode("site_code",hdrDom.item(0));
			tranId = GenericUtility.getInstance().getColumnValueFromNode("tran_id",hdrDom.item(0));*/
			siteCode = genericUtility.getColumnValueFromNode("site_code",hdrDom.item(0));
			tranId = genericUtility.getColumnValueFromNode("tran_id",hdrDom.item(0));
			
			System.out.println("tran_id (kfld1) : " + tranId);

			currDetail = getCurrentDetailFromDom(dom,domID);
			updateStatus = getCurrentUpdateFlag(currDetail);

		if (currDetail != null && !updateStatus.equalsIgnoreCase("D"))
			{
/*				itemCode = GenericUtility.getInstance().getColumnValueFromNode("item_code",currDetail);
				locCode = GenericUtility.getInstance().getColumnValueFromNode("loc_code",currDetail);
				lotNo = GenericUtility.getInstance().getColumnValueFromNode("lot_no",currDetail);
				lotSl = GenericUtility.getInstance().getColumnValueFromNode("lot_sl",currDetail);
				quantity = GenericUtility.getInstance().getColumnValueFromNode("quantity",currDetail);
				lineNo = GenericUtility.getInstance().getColumnValueFromNode("line_no",currDetail);*/
				itemCode = genericUtility.getColumnValueFromNode("item_code",currDetail);
				locCode = genericUtility.getColumnValueFromNode("loc_code",currDetail);
				lotNo = genericUtility.getColumnValueFromNode("lot_no",currDetail);
				lotSl = genericUtility.getColumnValueFromNode("lot_sl",currDetail);
				quantity = genericUtility.getColumnValueFromNode("quantity",currDetail);
				lineNo = genericUtility.getColumnValueFromNode("line_no",currDetail);
				System.out.println("lineNo:125 "+lineNo);

				line = "    "+lineNo.trim();
				hashMap.put("tran_date", new java.sql.Date(System.currentTimeMillis()));			
				hashMap.put("ref_ser",tranSer);
				hashMap.put("ref_id", tranId);
				hashMap.put("ref_line", line.substring(line.length()-4));
				hashMap.put("item_code", itemCode);
				hashMap.put("site_code", siteCode);
				hashMap.put("loc_code",locCode);
				hashMap.put("lot_no",lotNo);
				hashMap.put("lot_sl",lotSl);
				hashMap.put("alloc_qty", new Double(quantity.trim().length() == 0?"0":quantity));
				hashMap.put("chg_win","W_ADJ_ISS");
				/*hashMap.put("chg_user", GenericUtility.getInstance().getValueFromXTRA_PARAMS(xtraParams, "loginCode"));
				hashMap.put("chg_term", GenericUtility.getInstance().getValueFromXTRA_PARAMS(xtraParams, "termId"));*/
				hashMap.put("chg_user", genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode"));
				hashMap.put("chg_term", genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId"));
				//Calling DistStkUpdEJB
				System.out.println("Calling DistStkUpdEJB.....");
				distStkUpd = new DistStkUpd();
				if (distStkUpd.updAllocTrace(hashMap, conn) > 0)
				{						
					System.out.println("distStkUpd.UpdAllocTrace(HashMap, Connection) : Sucessuful!");
					//TO BE ASKED!!!
				}
				distStkUpd = null;
			}
		}
		/*catch(SQLException e)
		{
			System.out.println("Exception : AdjIssPosEJB : actionStock " +e.getMessage());
			throw new ITMException(e);
		}*/
		catch(Exception e)
		{
			System.out.println("Exception : AdjIssPosEJB : actionHandler :" +e.getMessage());
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