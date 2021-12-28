package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;
import javax.ejb.*;
import org.w3c.dom.*;

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.*;
import ibase.system.config.*;
import ibase.webitm.utility.ITMException;
//import ibase.webitm.utility.GenericUtility;
import javax.ejb.Stateless; // added for ejb3


//public class InvPackPrsEJB extends ValidatorEJB implements SessionBean
@Stateless // added for ejb3
public class InvPackPrs extends ValidatorEJB implements InvPackPrsLocal, InvPackPrsRemote
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

    public String preSaveRec() throws RemoteException,ITMException
	{
		return "";
	}
	
	public String preSaveRec(String xmlString1,String domId,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{
		System.out.println("InvPackPrsEJB called");
		Document dom = null;
		try
		{
			if (xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom = genericUtility.parseString(xmlString1);
				executepreSaveRec(dom,domId,objContext,editFlag,xtraParams,conn);
			}			
		}
	   	catch(Exception e)
		{
			System.out.println("Exception :InvPackPrsEJB :preSave() :" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return "";
	}

	private String executepreSaveRec(Document dom, String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{
		String siteCode = "", itemCode = "", lineNo = "",locCode = "", lotNo = "", lotSl = "";
		String sql = "", line = "", tranType = "", stockOpt = "", validateStock = "", errCode = "", errString = "";
		HashMap strAllocate = new HashMap();
		double quantity = 0d;
		int updateCnt = 0;
		java.sql.Date tranDate = null;
		DistStkUpd distStkUpd = null;
		NodeList hdrDom = null;
		Node currDetail = null;
		Statement stmt = null;
		ResultSet rs = null;
		String updateStatus = "",quantityStr = "",tranId = "";
		try
		{			
			stmt = conn.createStatement();	
			hdrDom = dom.getElementsByTagName("Detail1");
			tranType = genericUtility.getColumnValueFromNode("tran_type",hdrDom.item(0));
			siteCode = genericUtility.getColumnValueFromNode("site_code",hdrDom.item(0));
			validateStock = genericUtility.getColumnValueFromNode("validate_stock",hdrDom.item(0));
			tranId = genericUtility.getColumnValueFromNode("tran_id",hdrDom.item(0));

			System.out.println("\n tranId :"+tranId);
			tranDate = new java.sql.Date(System.currentTimeMillis());
			System.out.println("validateStock :: "+validateStock);
			if (validateStock.equals("Y"))
			{
				currDetail = getCurrentDetailFromDom(dom,domID);
				updateStatus = getCurrentUpdateFlag(currDetail);

				if (currDetail != null && !updateStatus.equalsIgnoreCase("A"))
				{
					lineNo		= genericUtility.getColumnValueFromNode("line_no",currDetail);
					itemCode	= genericUtility.getColumnValueFromNode("item_code",currDetail);
					locCode		= genericUtility.getColumnValueFromNode("loc_code",currDetail);
					lotNo		= genericUtility.getColumnValueFromNode("lot_no",currDetail);
					lotSl		= genericUtility.getColumnValueFromNode("lot_sl",currDetail);
					quantityStr = genericUtility.getColumnValueFromNode("quantity",currDetail);
					if (updateStatus.equalsIgnoreCase("E") || updateStatus.equalsIgnoreCase("D"))	//if codn added by Jiten 19/05/06
					{
						//Commented And Changes Below - Gulzar 15/05/07
						//sql = "SELECT QUANTITY FROM INV_PACK_ISS WHERE TRAN_ID = '"+tranId+"' AND LINE_NO = "+lineNo+"";
						sql = "SELECT ITEM_CODE, LOC_CODE, LOT_NO, LOT_SL, QUANTITY FROM INV_PACK_ISS WHERE TRAN_ID = '"+tranId+"' AND LINE_NO = "+lineNo+""; //Gulzar 15/05/07
						rs = stmt.executeQuery(sql);
						if (rs.next())
						{
							itemCode	= rs.getString("ITEM_CODE"); //Gulzar 15/05/07
							locCode		= rs.getString("LOC_CODE"); //Gulzar 15/05/07
							lotNo		= rs.getString("LOT_NO"); //Gulzar 15/05/07
							lotSl		= rs.getString("LOT_SL"); //Gulzar 15/05/07
							quantityStr = rs.getString("QUANTITY"); //Gulzar 15/05/07
						}
						//End Changes - Gulzar 15/05/07
					}
					System.out.println("lineNo :"+lineNo+" itemCode :"+itemCode+" locCode :"+locCode+" lotNo :"+lotNo+" lotSl :"+lotSl+" quantityStr :"+quantityStr); //Gulzar 15/05/07
					
					quantity = Double.parseDouble(quantityStr);

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
					strAllocate.put("alloc_qty",new Double(-1*quantity)); 
					strAllocate.put("chg_user",genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
					strAllocate.put("chg_term",genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId"));
					strAllocate.put("chg_win","W_INV_PACK");
					//if (distStkUpd.updAllocTrace(strAllocate) > 0)
					/*Changed by HATIM on 13/01/2006*/
					distStkUpd = new DistStkUpd();
					if (distStkUpd.updAllocTrace(strAllocate, conn) > 0)
					{
						System.out.println("distStkUpd.UpdAllocTrace(HashMap, Connection) : Sucessuful!");
					}
					distStkUpd = null;
					/*END*/
				}//end 
			}//end if for checking confirmed Y			
		}//try end
		catch (SQLException sqx)
		{
			System.out.println("The SQLException occurs in InvPackPrs :"+sqx);
			sqx.printStackTrace();
			throw new ITMException(sqx);
		}
		catch(Exception e)
		{
			System.out.println("The SQLException occurs in InvPackPrsEJB :"+e);			
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally	//Finally block added by Jiten 19/05/06
		{
			try
			{
				if (stmt != null)
				{
					stmt.close();
					stmt = null;
				}
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
			}catch(Exception e){}
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
}//class end