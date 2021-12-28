package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.lang.String;
import java.sql.*;
import javax.ejb.*;
import java.util.HashMap;
import org.w3c.dom.*;

import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
//import ibase.webitm.utility.GenericUtility;
import ibase.system.config.*;
import javax.ejb.Stateless; // added for ejb3

//public class StockTransferPrsEJB extends ValidatorEJB implements SessionBean
@Stateless // added for ejb3
public class StockTransferPrs extends ValidatorEJB implements StockTransferPrsLocal, StockTransferPrsRemote
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	/*public void ejbCreate() throws RemoteException, CreateException 
	{
		System.out.println("ejbCreate() method calling........");
	}
	public void ejbRemove()
	{
		System.out.println("ejbRemeove() method calling........");
	}
	public void ejbActivate() 
	{
		System.out.println("ejbActivate() method calling........");
	}
	public void ejbPassivate() 
	{
		System.out.println("ejbPassivate() method calling........");
	}*/
    public String preSaveRec() throws RemoteException,ITMException
	{
		return "";
	}
	
	public String preSaveRec(String xmlString1,String domId,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{
		System.out.println("StockTransferPrsEJB called");
		Document dom = null;
		try
		{
			System.out.println(">>>>>>>>objContext:"+objContext);
			System.out.println(">>>>>>>>xtraParams:"+xtraParams);
			if (xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom = genericUtility.parseString(xmlString1);
				if( (xmlString1.toUpperCase()).contains("DETAIL4"))
				{
					System.out.println(">>>>>>>>>>>>>>In PRS Detail4:"+xmlString1);
					//executePostSaveRec(dom,domId,objContext,editFlag,xtraParams, conn);
					executepreSaveRec(dom,domId,objContext,editFlag,xtraParams,conn);
				}
				//else if( "w_woi_scan".equals(windowName))
				else
				{
					//executePostSaveRec(dom,domId,objContext,editFlag,xtraParams, conn);
					executepreSaveRec(dom,domId,objContext,editFlag,xtraParams,conn);
				}
				
				//executepreSaveRec(dom,domId,objContext,editFlag,xtraParams,conn);
			}			
		}
	   	catch(Exception e)
		{
			System.out.println("Exception :StockTransferPrsEJB :preSaveRec() :" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return "";
	}

	private String executepreSaveRec(Document dom, String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{
		HashMap hashMap = new HashMap(); 
		String errString = "", lineNo = "", sql = "", line = "";
		String tranSer = "XFRX",updateStatus = "",tranId = ""; //change tran Series(XFRX) on 18/OCT/13 Kunal M 
		String siteCode = "", itemCode = "", quantity = "", locCodeFr = "", lotNoFr = "", lotSlFr = "";
		PreparedStatement pstmt = null;
		Statement stmt = null;
		ResultSet rs = null;
		double allocQty = 0d;
		DistStkUpd distStkUpd = null;
		NodeList hdrDom = null;
		Node currDetail = null;
		Node parentNode = null;
		try
		{			
			stmt = conn.createStatement();	
			hdrDom = dom.getElementsByTagName("Detail1");
			//Code Added by sagar on 25/11/14 Start..
			parentNode = hdrDom.item(0);
			String objName = getObjName(parentNode);
			System.out.println(">>>>>>>>>>>objName Detail2:"+objName);
			if(("w_qc_sample_wiz".equalsIgnoreCase(objName)) || ("w_grn_transfer_wiz".equalsIgnoreCase(objName)) )
			{
				System.out.println(">>>>>In Match obj name Detail2:"+objName);
				hdrDom = dom.getElementsByTagName("Detail2");
			}
			//Code Added by sagar on 25/11/14 End..
			siteCode = genericUtility.getColumnValueFromNode("site_code",hdrDom.item(0));
			tranId = genericUtility.getColumnValueFromNode("tran_id",hdrDom.item(0));
			System.out.println(">>>tran_id :" + tranId);
			System.out.println(">>>siteCode :" + siteCode);
			//currDetail = getCurrentDetailFromDom(dom,domID);
			currDetail = getCurrentDetailFromDom(dom,domID,objName); //Code Added by sagar 
			System.out.println(">>>>>>>currDetail:"+currDetail);
			updateStatus = getCurrentUpdateFlag(currDetail);
			System.out.println(">>>>>>>updateStatus:"+updateStatus);					
			if (currDetail != null && !updateStatus.equalsIgnoreCase("A"))
			{
				lineNo		= genericUtility.getColumnValueFromNode("line_no",currDetail);
				itemCode	= genericUtility.getColumnValueFromNode("item_code",currDetail);
				locCodeFr	= genericUtility.getColumnValueFromNode("loc_code__fr",currDetail);
				lotNoFr		= genericUtility.getColumnValueFromNode("lot_no__fr",currDetail);
				lotSlFr		= genericUtility.getColumnValueFromNode("lot_sl__fr",currDetail);
				quantity	= genericUtility.getColumnValueFromNode("quantity",currDetail);
				if (updateStatus.equalsIgnoreCase("E") || updateStatus.equalsIgnoreCase("D"))
				{
					//Commented And Changes Below - Gulzar 15/05/07
					//sql = "SELECT QUANTITY FROM STOCK_TRANSFER_DET WHERE TRAN_ID = '"+tranId+"' AND LINE_NO = "+lineNo+""; //Gulzar 15/05/07
					sql = "SELECT ITEM_CODE, LOC_CODE__FR, LOT_NO__FR, LOT_SL__FR, QUANTITY FROM STOCK_TRANSFER_DET WHERE TRAN_ID = '"+tranId+"' AND LINE_NO = "+lineNo+""; //Gulzar 15/05/07
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						itemCode	= rs.getString("ITEM_CODE"); //Gulzar 15/05/07
						locCodeFr	= rs.getString("LOC_CODE__FR"); //Gulzar 15/05/07
						lotNoFr		= rs.getString("LOT_NO__FR"); //Gulzar 15/05/07
						lotSlFr		= rs.getString("LOT_SL__FR"); //Gulzar 15/05/07
						quantity	= rs.getString("QUANTITY"); //Gulzar 15/05/07
					}
					//End Changes - Gulzar 15/05/07
				}
				System.out.println("lineNo :"+lineNo+" itemCode :"+itemCode+" locCodeFr :"+locCodeFr+" lotNoFr :"+lotNoFr+" lotSlFr :"+lotSlFr+" quantity :"+quantity);
				allocQty = -1 * Double.parseDouble(quantity);
				line = "   " + lineNo;
				hashMap.put("tran_date", new java.sql.Date(System.currentTimeMillis()));
				hashMap.put("ref_ser",tranSer);
				hashMap.put("ref_id", tranId);
				hashMap.put("ref_line", line.substring(line.length()-3));
				hashMap.put("item_code", itemCode);
				hashMap.put("site_code", siteCode);
				hashMap.put("loc_code",locCodeFr);
				hashMap.put("lot_no",lotNoFr);
				hashMap.put("lot_sl",lotSlFr);
				hashMap.put("alloc_qty", new Double(allocQty));
				hashMap.put("chg_win","W_STOCK_TRANSFER");
				hashMap.put("chg_user", genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode"));
				hashMap.put("chg_term", genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId"));
				
				//Calling DistStkUpdEJB
				System.out.println("Calling DistStkUpdEJB.....");
				distStkUpd = new DistStkUpd();
				if (distStkUpd.updAllocTrace(hashMap, conn) > 0)
				{
					System.out.println("distStkUpd.UpdAllocTrace(HashMap, Connection) : Sucessuful!");					
				}
			}			
		}
		catch(SQLException e)
		{
			System.out.println("Exception : StockTransferPrsEJB : actionStock " +e.getMessage());
			throw new ITMException(e);
		}
		catch(Exception e)
		{
			System.out.println("Exception : StockTransferPrsEJB : actionHandler :" +e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(distStkUpd != null)
				{
					distStkUpd = null;
				}
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
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
			}
			catch (Exception e)
			{
				System.out.println("Exception occurs in StockTransferPrsEJB :" +e);
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
	private Node getCurrentDetailFromDom(Document dom,String domId,String objName)
	{
		NodeList detailList = null;
		Node currDetail = null,reqDetail = null;
		String currDomId = "";
		int	detailListLength = 0;
		if(("w_qc_sample_wiz".equals(objName)) || ("w_grn_transfer_wiz".equals(objName)) )
		{
			System.out.println(">>>>>In Match obj name Detail4:"+objName);
			detailList = dom.getElementsByTagName("Detail4");
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
		System.out.println(">>>>>>currDetailList in getCurrentUpdateFlag():"+currDetailList);
		currDetailListLength = currDetailList.getLength();
		System.out.println(">>>>>>currDetailListLength in getCurrentUpdateFlag():"+currDetailListLength);
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

	//getObjName method added by sagar on 25/11/14
	private String getObjName(Node node) throws Exception
	{
		String objName = null;
		NodeList nodeList = null;
		Node detaulNode = null;
		Node detailNode = null;
		nodeList = node.getChildNodes();
		NamedNodeMap attrMap = node.getAttributes();
		objName = attrMap.getNamedItem( "objName" ).getNodeValue();
		/*
        for(int ctr = 0; ctr < nodeList.getLength(); ctr++ )
        {
            detailNode = nodeList.item(ctr);
            if(detailNode.getNodeName().equalsIgnoreCase("attribute") )
            {
                objName = detailNode.getAttributes().getNamedItem("objName").getNodeValue();
            }
        }
		 */
		return "w_" + objName;

	}
}