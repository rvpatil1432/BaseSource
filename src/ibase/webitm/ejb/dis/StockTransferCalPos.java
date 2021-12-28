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
import ibase.webitm.ejb.dis.*;
//import ibase.webitm.utility.GenericUtility;
import ibase.system.config.*;
import javax.ejb.Stateless; // added for ejb3


//public class StockTransferPosEJB extends ValidatorEJB implements SessionBean
@Stateless // added for ejb3
public class StockTransferCalPos extends ValidatorEJB implements StockTransferCalPosLocal, StockTransferCalPosRemote
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
		System.out.println("StockTransferPosEJB called");
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
			System.out.println("Exception :StockTransferPosEJB ::" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return "";
	}

	private String executepostSaveRec(Document dom, String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{

		HashMap hashMap = new HashMap(); 
		String lineNo = "", tranId = "", tranSer = "XFRX", line = "", updateStatus = "";
		String siteCode = "", itemCode = "", quantity = "", locCode = "", lotNo = "", lotSl = "", sql = "";
		DistStkUpd distStkUpd = null;
		NodeList hdrDom = null;
		Node currDetail = null;
		double dQuantity = 0, shipperSize = 0, stkQty = 0;
		String sNoArt = "0";
		int noArt = 0; 
		PreparedStatement pstmt = null;
		ResultSet rs =null;
		
		try
		{
			System.out.println("objContext =[" + objContext + "]");
			System.out.println("domID =[" + domID + "]");					
			hdrDom = dom.getElementsByTagName("Detail1");
			siteCode = genericUtility.getColumnValueFromNode("site_code",hdrDom.item(0));
			tranId = genericUtility.getColumnValueFromNode("tran_id",hdrDom.item(0));
			System.out.println("tran_id : " + tranId);

			//currDetail = getCurrentDetailFromDom(dom,domID); Commented by gulzar on 12/24/2011
			currDetail = getCurrentDetailFromDom( dom, domID, objContext );//Change added by gulzar on 12/24/2011
			updateStatus = getCurrentUpdateFlag(currDetail);

			if (currDetail != null && !updateStatus.equalsIgnoreCase("D"))
			{
				itemCode = genericUtility.getColumnValueFromNode("item_code",currDetail);
				locCode = genericUtility.getColumnValueFromNode("loc_code__fr",currDetail);
				lotNo = genericUtility.getColumnValueFromNode("lot_no__fr",currDetail);
				lotSl = genericUtility.getColumnValueFromNode("lot_sl__fr",currDetail);
				quantity = genericUtility.getColumnValueFromNode("quantity",currDetail);
				lineNo = genericUtility.getColumnValueFromNode("line_no",currDetail);
				sNoArt = genericUtility.getColumnValueFromNode("no_art",currDetail);
				//Changed By Pragyan 05/03/12 to get the quantity inputs
				stkQty = Double.parseDouble(checkNull(quantity));
			
				System.out.println("lineNo :"+lineNo+" itemCode :"+itemCode+" locCodeFr :"+locCode+" lotNoFr :"+lotNo+" lotSlFr :"+lotSl+" quantity :"+stkQty);
				String stlOption = chkStockOption(itemCode, siteCode);
				if (!stlOption.equals("0"))
				{
					if (sNoArt == null || sNoArt.trim().length() == 0)
					{
						sNoArt = "0";
					}
					if (quantity == null || quantity.trim().length() ==0)
					{
						quantity = "0";
					}
					noArt = Integer.parseInt(sNoArt);
					dQuantity = Double.parseDouble(quantity);
					System.out.println("manohar before 22/09/12 quantity ["+ quantity + "] noArt [" + noArt + "]" );
					
					if (noArt > 0)
					{
						sql="select shipper_size from item_lot_packsize WHERE ITEM_CODE = ? AND LOT_NO__FROM <= ? AND LOT_NO__TO >= ? ";
						pstmt =conn.prepareStatement(sql);
						pstmt.setString(1,itemCode);
						pstmt.setString(2,lotNo);
						pstmt.setString(3,lotNo);
						rs=pstmt.executeQuery();
						if(rs.next())
						{
							shipperSize=rs.getDouble(1);
						}
						rs.close();
						pstmt.close();
						rs=null;
						pstmt =null;
						//start added by chandrashekar on 16-jan-2014
						System.out.println("shipperSize>>>>"+shipperSize);
						if(shipperSize <=0 )
						{
							sql="select qty_per_art from stock where site_code=? and item_code=? and lot_no=? and lot_sl=? and loc_code=? ";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1,siteCode);
							pstmt.setString(2,itemCode);
							pstmt.setString(3,lotNo);
							pstmt.setString(4,lotSl);
							pstmt.setString(5,locCode);
							rs=pstmt.executeQuery();
							if(rs.next())
							{
								shipperSize=rs.getDouble(1);
							}
							rs.close();
							rs=null;
							pstmt.close();
							pstmt=null;
						}
						//End added by chandrashekar on 16-jan-2014
						//Changed By Pragyan 05/03/12 to get the quantity less then shipper and not zero
						//Changed by Rohan on 04-03-12 if quantity less than shipper size then set dQuantity as quantity.start
						//dQuantity = (noArt * shipperSize);
						System.out.println("quantity>>"+stkQty+"shipperSize>>"+shipperSize);
						//if(Double.parseDouble(quantity) >  < shipperSize)
						if(stkQty > 0 && stkQty < shipperSize)
						{
							dQuantity = Double.parseDouble(quantity);
						}
						else
						{
							dQuantity = (noArt * shipperSize);
						}
						//Changed by Rohan on 04-03-12 if quantity less than shipper size then set dQuantity as quantity.start
						
						quantity = ("" + dQuantity).trim();
					}
					else if (dQuantity == 0)
					{
						sql = "SELECT (quantity - case when alloc_qty is null then 0 else alloc_qty end)  as quantity,no_art FROM stock "
						+ " WHERE ITEM_CODE = ? "
						+ " AND SITE_CODE = ? "
						+ " AND LOC_CODE = ? "
						+ " AND LOT_NO = ? "
						+ " AND LOT_SL = ? ";
						pstmt = conn.prepareStatement( sql );
						pstmt.setString( 1, itemCode );
						pstmt.setString( 2, siteCode );
						pstmt.setString( 3, locCode );
						pstmt.setString( 4, lotNo );
						pstmt.setString( 5, lotSl );
						rs = pstmt.executeQuery();
						if( rs.next() )       
						{
							dQuantity = rs.getDouble(1);
							noArt = rs.getInt(2);
							
						}
					}
					System.out.println("dQuantity::"+dQuantity+"noArt::"+noArt+"quantity::"+quantity);
					System.out.println("lineNumber["+lineNo+"]");
					lineNo=lineNo.trim();
					System.out.println("lineNo---"+lineNo);
					//changed by sankara on 20/08/14 passed line no for multiple stock transfer
					//sql="update stock_transfer_det set quantity = ?, no_art = ? where tran_id = ? ";
					sql="update stock_transfer_det set quantity = ?, no_art = ? where tran_id = ? and line_no = ? ";
					pstmt =conn.prepareStatement(sql);
					pstmt.setDouble(1,dQuantity);
					pstmt.setDouble(2,noArt);
					pstmt.setString(3,tranId);
					//changed by sankara on 20/08/14 passed line no for multiple stock transfer
					//pstmt.setString(4,lineNo);
					pstmt.setInt(4,Integer.parseInt(lineNo));
					pstmt.executeUpdate();
					pstmt.close();
					pstmt =null;
					
					System.out.println("manohar after 22/09/12 quantity ["+quantity + "] noArt [" + noArt + "]" );
					
					hashMap.put("tran_date", new java.sql.Date(System.currentTimeMillis()));
					hashMap.put("ref_ser",tranSer);
					hashMap.put("ref_id", tranId);
					hashMap.put("ref_line", lineNo);
					hashMap.put("item_code", itemCode);
					hashMap.put("site_code", siteCode);
					hashMap.put("loc_code",locCode);
					hashMap.put("lot_no",lotNo);
					hashMap.put("lot_sl",lotSl);
					hashMap.put("alloc_qty", new Double(quantity.trim().length() == 0?"0":quantity));
					hashMap.put("chg_win","W_STOCK_TRANSFER_MULTI");
					hashMap.put("chg_user", genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode"));
					hashMap.put("chg_term", genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId"));
					distStkUpd = new DistStkUpd();
					//Calling DistStkUpdEJB					
					System.out.println("Calling DistStkUpdEJB.....");						
					if (distStkUpd.updAllocTrace(hashMap, conn) > 0)
					{ 
						distStkUpd = null;
						System.out.println("distStkUpd.UpdAllocTrace(HashMap, Connection) : Sucessuful!");
					}
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

	private String chkStockOption(String itemCode, String siteCode)throws RemoteException,ITMException
	{
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String sql = "", stkOption = "";
		ConnDriver connDriver = new ConnDriver();
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
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
				conn.close();
				conn = null;
				if (stmt != null)
				{
					stmt.close();
					stmt = null;
				}
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

	//Changed By Pragyan 05/03/13 To check set the valu if null.start
	private String checkNull( String inputVal )
	{
		if ( inputVal == null )
		{
			inputVal = "";
		}
		return inputVal;
	}
	//Changed By Pragyan 05/03/13 To check set the valu if null.end
}