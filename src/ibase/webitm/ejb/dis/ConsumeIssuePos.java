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

//public class ConsumeIssuePos extends ValidatorEJB implements SessionBean //commented for ejb3
@Stateless // added for ejb3
public class ConsumeIssuePos extends ValidatorEJB implements ConsumeIssuePosLocal, ConsumeIssuePosRemote //added for ejb3
{
	/* commented for ejb
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
		System.out.println("ConsumeIssuePosEJB called");
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
			System.out.println("Exception :ConsumeIssuePosEJB ::" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return "";
	}
	
	private String executepostSaveRec(Document dom, String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{
		Statement stmt = null;
		ResultSet rs = null;
		String siteCodeReq = "", itemCode = "", lineNo = "",locCode = "", lotNo = "", lotSl = "";
		String sql = "", tranType = "", line = "";
		HashMap strAllocate = new HashMap();
		double quantity = 0d;
		int updateCnt = 0, retVal  = 0;
		java.sql.Date tranDate = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility= new  E12GenericUtility();
		DistStkUpd distStkUpd = null;
		NodeList hdrDom = null;
		Node currDetail = null;
		String updateStatus = "",tranId = "",quantityStr = "";
		try
		{
			stmt = conn.createStatement();
			tranDate = new java.sql.Date(System.currentTimeMillis());
			System.out.println("\n tranDate :"+tranDate);

			hdrDom = dom.getElementsByTagName("Detail1");
			/*tranType = GenericUtility.getInstance().getColumnValueFromNode("tran_type",hdrDom.item(0));
			siteCodeReq = GenericUtility.getInstance().getColumnValueFromNode("site_code__req",hdrDom.item(0));
		    tranId = GenericUtility.getInstance().getColumnValueFromNode("cons_issue",hdrDom.item(0));*/
			tranType = genericUtility.getColumnValueFromNode("tran_type",hdrDom.item(0));
			siteCodeReq = genericUtility.getColumnValueFromNode("site_code__req",hdrDom.item(0));
		    tranId = genericUtility.getColumnValueFromNode("cons_issue",hdrDom.item(0));

			currDetail = getCurrentDetailFromDom(dom,domID);
			updateStatus = getCurrentUpdateFlag(currDetail);

			if (currDetail != null && !updateStatus.equalsIgnoreCase("D"))
			{
				/*lineNo = GenericUtility.getInstance().getColumnValueFromNode("line_no",currDetail);
				itemCode = GenericUtility.getInstance().getColumnValueFromNode("item_code",currDetail);
				locCode = GenericUtility.getInstance().getColumnValueFromNode("loc_code",currDetail);
				lotNo = GenericUtility.getInstance().getColumnValueFromNode("lot_no",currDetail);
				lotSl = GenericUtility.getInstance().getColumnValueFromNode("lot_sl",currDetail);
				quantityStr = GenericUtility.getInstance().getColumnValueFromNode("quantity",currDetail);*/
				lineNo = genericUtility.getColumnValueFromNode("line_no",currDetail);
				itemCode = genericUtility.getColumnValueFromNode("item_code",currDetail);
				locCode = genericUtility.getColumnValueFromNode("loc_code",currDetail);
				lotNo = genericUtility.getColumnValueFromNode("lot_no",currDetail);
				lotSl = genericUtility.getColumnValueFromNode("lot_sl",currDetail);
				quantityStr = genericUtility.getColumnValueFromNode("quantity",currDetail);
				quantity = Double.parseDouble(quantityStr);

				if (tranType.equals("I"))
				{
					line = "   " + lineNo;
					
					strAllocate.put("tran_date",tranDate);
					strAllocate.put("ref_ser","C-ISS");
					strAllocate.put("ref_id",tranId);
					strAllocate.put("ref_line",line.substring(line.length()-3));
					strAllocate.put("site_code",siteCodeReq);
					strAllocate.put("item_code",itemCode);
					strAllocate.put("loc_code",locCode);
					strAllocate.put("lot_no",lotNo);
					strAllocate.put("lot_sl",lotSl);
					strAllocate.put("alloc_qty",new Double(quantity));
					strAllocate.put("chg_user",genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
					strAllocate.put("chg_term",genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId"));
					strAllocate.put("chg_win","W_CONSUME_ISSUE");
				
					System.out.println("Calling DistStkUpdEJB.....");
					//if (distStkUpd.updAllocTrace(strAllocate) > 0)
					/*Changed by HATIM on 13/01/2006*/					
					distStkUpd = new DistStkUpd();
					if (distStkUpd.updAllocTrace(strAllocate, conn) > 0)
					{						
						System.out.println("distStkUpd.UpdAllocTrace(HashMap, Connection) : Sucessuful!");
					}
					distStkUpd = null;
					/*END*/
				}
			}//while end
		}//try end
		catch (SQLException sqx)
		{
			System.out.println("The SQLException occurs in ConsumePostSaveEJB :"+sqx);
			sqx.printStackTrace();
			throw new ITMException(sqx);
		}
		catch(Exception e)
		{
			System.out.println("The SQLException occurs in ConsumePostSaveEJB  :"+e);			
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