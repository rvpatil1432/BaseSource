package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.lang.String;
import java.sql.*;

import javax.ejb.*;
import javax.naming.InitialContext;
import java.util.HashMap;
import org.w3c.dom.*;

import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
//import ibase.webitm.utility.GenericUtility;
import ibase.system.config.*;
import javax.ejb.Stateless; // added for ejb3


//public class StockTransferPosEJB extends ValidatorEJB implements SessionBean
@Stateless // added for ejb3
public class StockTransferPos extends ValidatorEJB implements StockTransferPosLocal, StockTransferPosRemote
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
	public String postSaveRec()throws RemoteException,ITMException
	{
		return "";
	}

	public String postSaveRec(String xmlString1,String domId,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{
		Document dom = null;
		System.out.println("StockTransferPosEJB called123");
		try
		{
			if (xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom = genericUtility.parseString(xmlString1);
				if( (xmlString1.toUpperCase()).contains("DETAIL4"))
				{
					System.out.println(">>>>>>>>>>>>>>In POS Detail4:"+xmlString1);
					executepostSaveRec(dom,domId,objContext,editFlag,xtraParams,conn);
				}
				else
				{
					executepostSaveRec(dom,domId,objContext,editFlag,xtraParams,conn);
				}
				//executepostSaveRec(dom,domId,objContext,editFlag,xtraParams,conn);
			}			
		}
		catch(Exception e)
		{
			System.out.println("Exception :StockTransferPosEJB ::" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return "";
	}

	private String executepostSaveRec(Document dom, String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{

		HashMap hashMap = new HashMap(); 
		String lineNo = "", tranId = "", tranSer = "XFRX", line = "", updateStatus = ""; //change tran Series(XFRX) on 18/OCT/13 Kunal M
		String siteCode = "", itemCode = "", quantity = "", locCode = "", lotNo = "", lotSl = "";
		String sql = "";
		Double allocQty = 0.0 ,totAllocQty = 0.0, qty = 0.0 ,pendingQty = 0.0;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		DistStkUpd distStkUpd = null;
		NodeList hdrDom = null;
		Node currDetail = null;
		Node parentNode = null;
		try
		{
			System.out.println("objContext =[" + objContext + "]");
			System.out.println("domID =[" + domID + "]");			
			hdrDom = dom.getElementsByTagName("Detail1");
			//Start added by chandra shekar 0n 25-11-14
			parentNode = hdrDom.item(0);
			String objName = getObjName(parentNode);
			System.out.println("objName=["+objName+"]domID["+domID+"]");
			if(("w_qc_sample_wiz".equalsIgnoreCase(objName)) || ("w_grn_transfer_wiz".equalsIgnoreCase(objName)) )
			{
				hdrDom = dom.getElementsByTagName("Detail2");
			}
			//end added by chandra shekar 0n 25-11-14
			siteCode = genericUtility.getColumnValueFromNode("site_code",hdrDom.item(0));
			tranId = genericUtility.getColumnValueFromNode("tran_id",hdrDom.item(0));
			System.out.println("tran_id : " + tranId);
			System.out.println("objContext ::[" + objContext + "]");
			//currDetail = getCurrentDetailFromDom(dom,domID); Commented by gulzar on 12/24/2011
			currDetail = getCurrentDetailFromDom( dom, domID, objContext );//Change added by gulzar on 12/24/2011
			updateStatus = getCurrentUpdateFlag(currDetail);
			System.out.println(">>>>>>>currDetail@@@:"+currDetail);
			System.out.println(">>>>>>>updateStatus@@@:"+updateStatus);

			if (currDetail != null && !updateStatus.equalsIgnoreCase("D"))
			{
				itemCode = genericUtility.getColumnValueFromNode("item_code",currDetail);
				locCode = genericUtility.getColumnValueFromNode("loc_code__fr",currDetail);
				lotNo = genericUtility.getColumnValueFromNode("lot_no__fr",currDetail);
				lotSl = genericUtility.getColumnValueFromNode("lot_sl__fr",currDetail);
				quantity = genericUtility.getColumnValueFromNode("quantity",currDetail);
				lineNo = genericUtility.getColumnValueFromNode("line_no",currDetail);
				System.out.println("lineNo :"+lineNo+" itemCode :"+itemCode+" locCodeFr :"+locCode+" lotNoFr :"+lotNo+" lotSlFr :"+lotSl+" quantity :"+quantity);
				String stlOption = chkStockOption(itemCode, siteCode,conn);       // chnage by ritesh on 11-11-13
				System.out.println("stlOption  :"+stlOption);
				if (!stlOption.equals("0"))
				{
					if (quantity == null)
					{
						quantity = "0.000";
					}
					//temporary comment code by kunal on 16/NOV/13
					/*
					//added by kunal on 06/NOV/13 
					sql = "select ref_ser,ref_id ,trim(ref_line) as ref_line, sum(alloc_qty)  from invalloc_trace  where "
							+" site_code = ? and loc_code = ? and item_code = ? and lot_no = ? and lot_sl = ? "
							+"  group by ref_ser, ref_id ,trim( ref_line)  having sum(alloc_qty) > 0 order by  sum(alloc_qty)  desc  ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteCode);
					pstmt.setString(2, locCode);
					pstmt.setString(3, itemCode);
					pstmt.setString(4, lotNo);
					pstmt.setString(5, lotSl);
					rs = pstmt.executeQuery();
					while(rs.next())
					{
						//refSer = checkNull(rs.getString( "ref_ser" )) ;
						//refId = checkNull(rs.getString( "ref_id" ));
						System.out.println("ref ser="+rs.getString( "ref_ser" )+"   ref id="+rs.getString( "ref_id" ));
						//refLine = Integer.parseInt( checkNull(rs.getString("ref_line")).trim().length() == 0 ?"0":rs.getString("ref_line").trim()) ;
						allocQty = rs.getDouble(4);
						System.out.println("allocQty="+allocQty);
						totAllocQty = totAllocQty + allocQty;
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println("totAllocQty="+totAllocQty);

					qty = new Double(quantity.trim().length() == 0?"0":quantity);
					pendingQty = qty - totAllocQty;
					System.out.println("pendingQty="+pendingQty);
					if(pendingQty > 0)
					{
					*/
						//added by kunal on 06/NOV/13 END	
						hashMap.put("tran_date", new java.sql.Date(System.currentTimeMillis()));
						hashMap.put("ref_ser",tranSer);
						hashMap.put("ref_id", tranId);
						lineNo = "   " + lineNo;
						hashMap.put("ref_line", lineNo.substring(lineNo.length()-3));
						hashMap.put("item_code", itemCode);
						hashMap.put("site_code", siteCode);
						hashMap.put("loc_code",locCode);
						hashMap.put("lot_no",lotNo);
						hashMap.put("lot_sl",lotSl);
						hashMap.put("alloc_qty", new Double(quantity.trim().length() == 0?"0":quantity));
						
						//hashMap.put("alloc_qty", new Double(pendingQty));//changed by kunal on 06/NOV/13
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
						distStkUpd = null;
					//}
				}//end if
			}//end if
		}//end try
		catch(Exception e)
		{
			System.out.println("Exception : StockTransferPosEJB : actionHandler :" +e.getMessage());
			throw new ITMException(e);
		}
		return "";
	}
	//start added  by chandrashekar on 1-dec-14
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
	//end added  by chandrashekar on 01-dec-14
	private String chkStockOption(String itemCode, String siteCode,Connection conn)throws RemoteException,ITMException
	{
		//Connection conn = null;                                    // COMMENTED BY RITESH ON 11-11-13
		Statement stmt = null;
		ResultSet rs = null;
		String sql = "", stkOption = "";
		//ConnDriver connDriver = new ConnDriver();
		try
		{
			//conn = connDriver.getConnectDB("DriverITM");
			stmt = conn.createStatement();
			sql = "SELECT CASE WHEN STK_OPT IS NULL THEN 'N' ELSE STK_OPT END "
					+"FROM SITEITEM "
					+"WHERE ITEM_CODE = '"+itemCode+"' "
					+"AND SITE_CODE = '"+siteCode+"' ";
			System.out.println("sql :"+sql);
			rs = stmt.executeQuery(sql);
			if (rs.next())
			{
				stkOption = rs.getString(1);
			}
			if (stkOption == null || stkOption.equals("N"))
			{
				sql = "SELECT CASE WHEN STK_OPT IS NULL THEN '0' ELSE STK_OPT END "
						+"FROM ITEM "
						+"WHERE ITEM_CODE = '"+itemCode+"'";
				System.out.println("sql :"+sql);
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					stkOption = rs.getString(1);
				}
			}//end if
			System.out.println("stkOption :"+stkOption);
		}//end try
		catch(SQLException e)
		{
			System.out.println("Exception : StockTransferPosEJB : actionStock " +e.getMessage());
			throw new ITMException(e);
		}
		catch(Exception e)
		{
			System.out.println("Exception : StockTransferPosEJB : actionHandler :" +e.getMessage());
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
				if (stmt != null)
				{
					stmt.close();
					stmt = null;
				}
//				if(conn != null)
//				{
//					conn.close();
//					conn = null;
//				}
			}
			catch(Exception e)
			{
				System.out.println("The Exception occurs in StockTransferPosEJB "+e);	
			}
		}
		return stkOption;
	}
	//private Node getCurrentDetailFromDom(Document dom,String domId)//Commented by gulzar on 12/24/2011
	private Node getCurrentDetailFromDom( Document dom, String domId, String objContext ) //changed added by gulzar on 12/24/2011
	{
		NodeList detailList = null;
		Node currDetail = null,reqDetail = null;
		String currDomId = "";
		int	detailListLength = 0;
		System.out.println("objContext:["+objContext+"]");
		//detailList = dom.getElementsByTagName("Detail2");//Commented by gulzar on 12/24/2011
		detailList = dom.getElementsByTagName("Detail"+objContext);//change added by gulzar on 12/24/2011
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