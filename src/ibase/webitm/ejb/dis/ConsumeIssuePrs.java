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

@Stateless // added for ejb3
public class ConsumeIssuePrs extends ValidatorEJB implements ConsumeIssuePrsLocal, ConsumeIssuePrsRemote
{
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
		System.out.println("ConsumeIssuePrsEJB called");
		Document dom = null;
		try
		{
			E12GenericUtility genericUtility= new  E12GenericUtility();
			if (xmlString1 != null && xmlString1.trim().length() > 0)
			{
				//dom = GenericUtility.getInstance().parseString(xmlString1);
				dom = genericUtility.parseString(xmlString1);
				executepreSaveRec(dom,domId,objContext,editFlag,xtraParams,conn);
			}		
		}
		catch(Exception e)
		{
			System.out.println("Exception :ConsumeIssuePrsEJB ::" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return "";
	}
	
	private String executepreSaveRec(Document dom, String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{
		Statement stmt = null;
		ResultSet rs = null;
		String siteCodeReq = "", itemCode = "", lineNo = "",locCode = "", lotNo = "", lotSl = "";
		String sql = "", line = "", tranType = "";
		HashMap strAllocate = null;
		double quantity = 0d;
		int updateCnt = 0, retVal  = 0;
		java.sql.Date tranDate = null;
		DistStkUpd distStkUpd = null;
		String updateStatus = "",quantityStr = "",tranId = "";
		NodeList hdrDom = null;
		Node currDetail = null;
		E12GenericUtility genericUtility= new  E12GenericUtility();
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

			System.out.println("\n tranId :"+tranId);
			
			currDetail = getCurrentDetailFromDom(dom,domID);
			updateStatus = getCurrentUpdateFlag(currDetail);

			System.out.println("updateStatus :: "+updateStatus);

            System.out.println("currentDetail [" +genericUtility.serializeDom(currDetail) + "]");
			
			if (currDetail != null && !updateStatus.equalsIgnoreCase("A")&& !updateStatus.equalsIgnoreCase("N"))//&& !updateStatus.equalsIgnoreCase("N") CONDITION ADDED BY NANDKUMAR GADKARI ON 14/01/18
			{
				/*lineNo		= GenericUtility.getInstance().getColumnValueFromNode("line_no",currDetail);
				itemCode	= GenericUtility.getInstance().getColumnValueFromNode("item_code",currDetail);
				locCode		= GenericUtility.getInstance().getColumnValueFromNode("loc_code",currDetail);
				lotNo		= GenericUtility.getInstance().getColumnValueFromNode("lot_no",currDetail);
				lotSl		= GenericUtility.getInstance().getColumnValueFromNode("lot_sl",currDetail);
				quantityStr = GenericUtility.getInstance().getColumnValueFromNode("quantity",currDetail);*/
				lineNo		= genericUtility.getColumnValueFromNode("line_no",currDetail);
				itemCode	= genericUtility.getColumnValueFromNode("item_code",currDetail);
				locCode		= genericUtility.getColumnValueFromNode("loc_code",currDetail);
				lotNo		= genericUtility.getColumnValueFromNode("lot_no",currDetail);
				lotSl		= genericUtility.getColumnValueFromNode("lot_sl",currDetail);
				quantityStr = genericUtility.getColumnValueFromNode("quantity",currDetail);
				if (updateStatus.equalsIgnoreCase("E") || updateStatus.equalsIgnoreCase("D"))//if codn added by jiten 19/05/06
				{
					//Commented And Changes Below - Gulzar 15/05/07
					//sql = "SELECT QUANTITY FROM CONSUME_ISS_DET WHERE CONS_ISSUE = '"+tranId+"' AND LINE_NO = "+lineNo+""; //Gulzar 15/05/07
					sql = "SELECT ITEM_CODE, LOC_CODE, LOT_NO, LOT_SL, QUANTITY FROM CONSUME_ISS_DET WHERE CONS_ISSUE = '"+tranId+"' AND LINE_NO = "+lineNo+""; //Gulzar 15/05/07
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						itemCode	= rs.getString("ITEM_CODE"); //Gulzar 15/05/07
						locCode		= rs.getString("LOC_CODE"); //Gulzar 15/05/07
						lotNo		= rs.getString("LOT_NO"); //Gulzar 15/05/07
						lotSl		= rs.getString("LOT_SL"); //Gulzar 15/05/07
						quantityStr = rs.getString("QUANTITY");
					}
					//End Changes - Gulzar 15/05/07
				}
				System.out.println("lineNo :"+lineNo+" itemCode :"+itemCode+" locCode :"+locCode+" lotNo :"+lotNo+" lotSl :"+lotSl+" quantityStr :"+quantityStr); //Gulzar 15/05/07
				quantity = Double.parseDouble(quantityStr);
					
				if (tranType.equals("I"))
				{
					line = "   " + lineNo;
					System.out.println("line  :"+line);
					strAllocate = new HashMap();
					strAllocate.put("tran_date",tranDate);
					strAllocate.put("ref_ser","C-ISS");
					strAllocate.put("ref_id",tranId);
					strAllocate.put("ref_line",line.substring(line.length()-3));					
					strAllocate.put("site_code",siteCodeReq);
					strAllocate.put("item_code",itemCode);
					strAllocate.put("loc_code",locCode);
					strAllocate.put("lot_no",lotNo);
					strAllocate.put("lot_sl",lotSl);
					strAllocate.put("alloc_qty",new Double(-1*quantity)); 
					/*strAllocate.put("chg_user",GenericUtility.getInstance().getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
					strAllocate.put("chg_term",GenericUtility.getInstance().getValueFromXTRA_PARAMS(xtraParams,"termId"));*/
					strAllocate.put("chg_user",genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
					strAllocate.put("chg_term",genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId"));
					strAllocate.put("chg_win","W_CONSUME_ISSUE");
					//Calling DistStkUpdEJB
					//pavan R 20/jul/18 changed the lookup to creating instance of the class using new keyword.
					distStkUpd = new DistStkUpd();
					System.out.println("Calling DistStkUpdEJB.....");
					//if (distStkUpd.updAllocTrace(strAllocate) > 0)
					/*Changed by HATIM on 13/01/2006*/
					if (distStkUpd.updAllocTrace(strAllocate, conn) > 0)
					{
						distStkUpd = null;
						System.out.println("distStkUpd.UpdAllocTrace(HashMap, Connection) : Sucessuful!");
					}
					/*END*/
				}
			}
		}//try end
		catch (SQLException sqx)
		{
			System.out.println("The SQLException occurs in ConsumeIssuePrsEJB :"+sqx);
			sqx.printStackTrace();
			throw new ITMException(sqx);
		}
		catch(Exception e)
		{
			System.out.println("The SQLException occurs in ConsumeIssuePrsEJB :"+e);			
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
				if (rs != null)//if codn added by Jiten 19/05/06
				{
					rs.close();
					rs = null;
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