package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.*;
import java.util.HashMap;

import org.w3c.dom.*;

import javax.ejb.*;

import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
//import ibase.webitm.utility.GenericUtility;
import ibase.system.config.*;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class SordAllocDef extends ActionHandlerEJB implements SordAllocDefLocal, SordAllocDefRemote
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

    public String actionHandler() throws RemoteException,ITMException
	{
		return "";
	}

	public String actionHandler(String actionType, String xmlString, String objContext, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		String  retString = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		try
		{
			if (actionType.equalsIgnoreCase("Default"))
			{
				System.out.println("-XML- String :"+xmlString);
				dom = genericUtility.parseString(xmlString); 
			}
			retString = actionDefault(dom, xtraParams);
		}
	   	catch(Exception e)
		{
			System.out.println("Exception :SordAllocDef :actionHandler(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning from actionDefault actionHandler"+retString);
	    return (retString);
	}

	private String actionDefault(Document dom, String xtraParams) throws RemoteException , ITMException
	{
		String creditTerm = "", linenosord = "", siteCode = "", itemCode = "", lotSl = "",lotNo = "", saleOrder = "";
		String errCode = "";
		String errString = "";
		String sql = "";
		String resrvLoc  = "",casePickLoc = "", activePickLoc = "", deepStoreLoc = "", partialResrvLoc = "";
		boolean isActives = false;
		String orderByStkStr = "",updateFlag = "";
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String detCnt = "0";
		int minSelfLife = 0;
		NodeList parentNodeList = null;
		Node parentNode = null;
		int parentNodeListLength = 0;
		double quantity = 0.0, pendingQty = 0.0, qtyAvail = 0.0,quantityP = 0.0,quantityPnd = 0.0;
		
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		//GenericUtility genericUtility = GenericUtility.getInstance();
		ConnDriver connDriver = new ConnDriver();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		try
		{
			DistCommon discommon = new DistCommon();
			System.out.println(" sys sys xtraParams ["+xtraParams+"]");
			detCnt = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"detCnt"); //Will be Uncommented later - Jiten
			System.out.println(" sumit detCnt ["+detCnt+"]");
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			if (detCnt == null || detCnt.trim().length() == 0)
			{
				detCnt = "0";
			}
			if(detCnt.equals("1"))
			{
				
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNodeListLength = parentNodeList.getLength();				
				System.out.println("parentNodeListLength*********sumit******************************"+parentNodeListLength);
		        for(int row = 0; row < parentNodeListLength; row++)
				{
		        	parentNode = parentNodeList.item(row);					
		        	updateFlag = getAttribValue(parentNode,"attribute", "updateFlag"); //(parentNode, "attribute", "status"));
					//System.out.println(" updateFlag ---- > "+updateFlag);
		        	
					if( !"D".equalsIgnoreCase(updateFlag) && !"N".equalsIgnoreCase(updateFlag) && !"E".equalsIgnoreCase(updateFlag))
					{
						
			        	linenosord = genericUtility.getColumnValueFromNode("line_no__sord", dom.getElementsByTagName("Detail2").item(row));
			        	siteCode = genericUtility.getColumnValueFromNode("site_code", dom.getElementsByTagName("Detail2").item(row));
			        	//siteDescr = GenericUtility.getInstance().getColumnValueFromNode("site_descr", dom.getElementsByTagName("Detail2").item(row));
			        	saleOrder = genericUtility.getColumnValueFromNode("sale_order", dom.getElementsByTagName("Detail2").item(row));
			        	itemCode = genericUtility.getColumnValueFromNode("item_code", dom.getElementsByTagName("Detail2").item(row));
			        	lotNo = genericUtility.getColumnValueFromNode("lot_no", dom.getElementsByTagName("Detail2").item(row));
			        	lotSl = genericUtility.getColumnValueFromNode("lot_sl", dom.getElementsByTagName("Detail2").item(row));
			        	
			        	System.out.println("sumit quantity ["+genericUtility.getColumnValueFromNode("quantity", dom.getElementsByTagName("Detail2").item(row)));
			        	//quantity = quantity + Integer.parseInt(GenericUtility.getInstance().getColumnValueFromNode("quantity", dom.getElementsByTagName("Detail2").item(row)));
			        	quantity = Double.parseDouble(genericUtility.getColumnValueFromNode("quantity", dom.getElementsByTagName("Detail2").item(row)));
			        	quantityP = Double.parseDouble(genericUtility.getColumnValueFromNode("pending_qty", dom.getElementsByTagName("Detail2").item(row)));
					//}
					        System.out.println(" linenosord ["+linenosord+"] quantity ["+quantity+"] siteCode ["+siteCode+"]");
							
							
							sql = " select MIN_SHELF_LIFE, (SORDITEM.QUANTITY - SORDITEM.QTY_DESP - SORDITEM.QTY_ALLOC) PENDING_QUANTITY from sorditem where sale_order = ? AND line_no = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, saleOrder);
							pstmt.setString(2, linenosord);
							rs = pstmt.executeQuery();
							
							if(rs.next())
							{
								minSelfLife = rs.getInt("MIN_SHELF_LIFE");
								pendingQty = rs.getDouble("PENDING_QUANTITY");
							}
							
							if(pstmt != null)
							{
								pstmt.close();
								pstmt = null;
							}
							rs.close();rs = null;
							
							resrvLoc  = discommon.getDisparams("999999","RESERV_LOCATION",conn);	            
							 casePickLoc  = discommon.getDisparams("999999","CASE_PICK_INVSTAT",conn);
							 activePickLoc  = discommon.getDisparams("999999","ACTIVE_PICK_INVSTAT",conn);
							 deepStoreLoc = discommon.getDisparams("999999","DEEP_STORE_INVSTAT",conn);
							 partialResrvLoc = discommon.getDisparams("999999","PRSRV_INVSTAT",conn);
							 
							 
							String  sSingleLotSql = "SELECT STOCK.LOT_NO,STOCK.LOT_SL,"
									   +"STOCK.LOC_CODE, "
									   +"STOCK.UNIT,  "
									   +"(STOCK.QUANTITY - STOCK.ALLOC_QTY - (CASE WHEN STOCK.HOLD_QTY IS NULL THEN 0 ELSE STOCK.HOLD_QTY END)) AS QTY_AVAIL_ALLOC ,"
									   +"STOCK.GRADE,STOCK.EXP_DATE,STOCK.CONV__QTY_STDUOM,STOCK.QUANTITY, " 					   
									   +"STOCK.MFG_DATE,STOCK.SITE_CODE__MFG, "
									   +"STOCK.NO_ART,INVSTAT.INV_STAT,ITEM.LOC_TYPE__PARENT,ITEM.LOC_TYPE,ITEM.LOC_ZONE__PREF " 
									   +"FROM STOCK,ITEM,LOCATION,INVSTAT " 
									   +"WHERE (ITEM.ITEM_CODE = STOCK.ITEM_CODE) "
									   +"AND (LOCATION.LOC_CODE = STOCK.LOC_CODE ) "
									   +"AND (LOCATION.INV_STAT = INVSTAT.INV_STAT) "
									   +"AND INVSTAT.AVAILABLE = 'Y' "
									   +"AND STOCK.ITEM_CODE = ? AND STOCK.SITE_CODE = ? "
									   +"AND (STOCK.QUANTITY - STOCK.ALLOC_QTY - (CASE WHEN STOCK.HOLD_QTY IS NULL THEN 0 ELSE STOCK.HOLD_QTY END) > 0) "
									   +"AND STOCK.LOT_NO IN (SELECT LOT_NO FROM STOCK WHERE ITEM_CODE =? AND SITE_CODE =? AND LOT_NO = ? "
									   +"GROUP BY LOT_NO HAVING SUM(QUANTITY - ALLOC_QTY - (CASE WHEN STOCK.HOLD_QTY IS NULL THEN 0 ELSE STOCK.HOLD_QTY END) ) > 0) " 
									   +"AND (MONTHS_BETWEEN(TO_DATE(STOCK.EXP_DATE),SYSDATE) > ? ) ";
							 		   //+"AND NOT EXISTS (SELECT 1 FROM INV_RESTR I WHERE I.INV_STAT = INVSTAT.INV_STAT AND I.REF_SER = 'S-DSP' ) ";
							 
							 HashMap itemVolMap = getItemVoumeMap(itemCode, "", conn);
							 double packSize = (Double)itemVolMap.get("PACK_SIZE");
							 double itemWeight = (Double)itemVolMap.get("ITEM_WEIGHT");
							 System.out.println("itemWeight =["+itemWeight+"] packSize ["+packSize+"]");
							 if((pendingQty % packSize) > 0)
							{
								isActives = true;
								orderByStkStr = "AND LOCATION.INV_STAT IN(?,?,?,?,?) ORDER BY STOCK.EXP_DATE,LOCATION.INV_STAT, STOCK.LOC_CODE ";
							}
							else
							{
								isActives = false;
								orderByStkStr = "AND LOCATION.INV_STAT IN(?,?,?,?) ORDER BY STOCK.EXP_DATE, STOCK.LOC_CODE ";
							}			
									
							pstmt = conn.prepareStatement(sSingleLotSql + orderByStkStr);
							//pstmt.setDouble(1,quantity);
							pstmt.setString(1,itemCode);
							pstmt.setString(2,siteCode);
							//pstmt.setString(3,lotSl);
							pstmt.setString(3,itemCode);
							pstmt.setString(4,siteCode);						
							pstmt.setString(5,lotNo);
							//pstmt.setDouble(6,pendingQty);
							pstmt.setInt(6,minSelfLife);
							pstmt.setString(7,resrvLoc);
							pstmt.setString(8,casePickLoc);
							pstmt.setString(9,activePickLoc);
							pstmt.setString(10,deepStoreLoc);				
							if(isActives)
							{
								pstmt.setString(11,partialResrvLoc);
							}					
							rs = pstmt.executeQuery();	
							lotSl = "";
							//quantity = 0.0;
							quantityPnd = quantityP;
							//quantityP = quantity;
							while( rs.next() && quantityP >= quantity)
							{
								qtyAvail = rs.getDouble("QTY_AVAIL_ALLOC");  //quantity
								lotSl = rs.getString("LOT_SL");
								System.out.println("qtyAvail before"+qtyAvail);
								System.out.println("quantity"+qtyAvail);
								/*if((quantityP - quantity)  >= qtyAvail )
								{
									quantity = quantity  + qtyAvail;
								}
								else 
								{
									
									qtyAvail = quantityP - quantity ;
									quantity = quantity  + qtyAvail;								
									//quantity = 0;
								}*/
								
								if(quantity > qtyAvail)
								{
									quantity = quantity - qtyAvail;
								}
								else
								{
									qtyAvail = quantity;
									quantity = 0;
								}
								System.out.println("quantity"+quantity);
								System.out.println("qtyAvail after"+qtyAvail);
								
								valueXmlString.append("<Detail>\r\n");
								valueXmlString.append("<sale_order>").append("<![CDATA[").append(saleOrder).append("]]>").append("</sale_order>\r\n");
								valueXmlString.append("<line_no__sord>").append("<![CDATA[").append(linenosord).append("]]>").append("</line_no__sord>\r\n");
								valueXmlString.append("<exp_lev>").append("<![CDATA[").append(genericUtility.getColumnValueFromNode("exp_lev", dom.getElementsByTagName("Detail2").item(row))).append("]]>").append("</exp_lev>\r\n");
								valueXmlString.append("<site_code>").append("<![CDATA[").append(siteCode).append("]]>").append("</site_code>\r\n");
								valueXmlString.append("<site_descr>").append("<![CDATA[").append(genericUtility.getColumnValueFromNode("site_descr", dom.getElementsByTagName("Detail2").item(row))).append("]]>").append("</site_descr>\r\n");
								valueXmlString.append("<item_code>").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code>\r\n");
								//valueXmlString.append("<item_descr>").append("<![CDATA[").append(itemDescr).append("]]>").append("</item_descr>\r\n");						
								valueXmlString.append("<lot_no>").append("<![CDATA[").append(lotNo).append("]]>").append("</lot_no>\r\n");
								valueXmlString.append("<lot_sl>").append("<![CDATA[").append(lotSl).append("]]>").append("</lot_sl>\r\n");
								valueXmlString.append("<loc_code>").append("<![CDATA[").append(rs.getString("LOC_CODE")).append("]]>").append("</loc_code>\r\n");
								valueXmlString.append("<quantity>").append("<![CDATA[").append(qtyAvail).append("]]>").append("</quantity>\r\n");
								valueXmlString.append("<pending_qty>").append("<![CDATA[").append(quantityPnd).append("]]>").append("</pending_qty>\r\n");
								valueXmlString.append("<dealloc_qty>").append("<![CDATA[").append("0").append("]]>").append("</dealloc_qty>\r\n");
								valueXmlString.append("</Detail>\r\n");
								
								
								quantityPnd = quantityPnd - qtyAvail;
								System.out.println(" quantity ["+quantity+"] quantityP ["+quantityP+"]");
								/*System.out.println(" quantity ["+quantity+"] quantityP ["+quantityP+"]");
								if(quantityP == quantity )
								{
									break;
								}*/
								
								if(quantity <= 0)
								{
									break;
								}
								
							}		 
							 rs.close();rs = null;
							 pstmt.close(); pstmt = null;				 
							 System.out.println("CASE_PICK_INVSTAT"+casePickLoc+"ACTIVE_PICK_INVSTAT"+activePickLoc+"RESERV_LOCATION"+resrvLoc+"PARTIAL RESERVE LOC["+partialResrvLoc+"]");
							 
								/*valueXmlString.append("<Detail2>\r\n");
								valueXmlString.append("<sale_order>").append("<![CDATA[").append(saleOrder).append("]]>").append("</sale_order>\r\n");
								valueXmlString.append("<line_no__sord>").append("<![CDATA[").append(linenosord).append("]]>").append("</line_no__sord>\r\n");
								//valueXmlString.append("<exp_lev>").append("<![CDATA[").append(expLev).append("]]>").append("</exp_lev>\r\n");
								valueXmlString.append("<site_code>").append("<![CDATA[").append(siteCode).append("]]>").append("</site_code>\r\n");
								//valueXmlString.append("<site_descr>").append("<![CDATA[").append(siteDescr).append("]]>").append("</site_descr>\r\n");
								valueXmlString.append("<item_code>").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code>\r\n");
								//valueXmlString.append("<item_descr>").append("<![CDATA[").append(itemDescr).append("]]>").append("</item_descr>\r\n");
								valueXmlString.append("<lot_no>").append("<![CDATA[").append(lotSl).append("]]>").append("</lot_no>\r\n");
								valueXmlString.append("<quantity>").append("<![CDATA[").append(qtyAvail).append("]]>").append("</quantity>\r\n");
								valueXmlString.append("<pending_qty>").append("<![CDATA[").append("").append("]]>").append("</pending_qty>\r\n");
								valueXmlString.append("<dealloc_qty>").append("<![CDATA[").append("").append("]]>").append("</dealloc_qty>\r\n");
								valueXmlString.append("</Detail2>\r\n");*/
							//}
							
							//valueXmlString.append("</Root>\r\n");
					}
				}
		        valueXmlString.append("</Root>\r\n");
		        System.out.println("valueXmlString["+valueXmlString.toString()+"]");
			}
			else
			{
				errCode = "VTDETCNT";
			}
			if (!errCode.equals(""))
			{
				errString = itmDBAccess.getErrorString("",errCode,"","",conn);
				return errString;
			}
		}
		catch(SQLException e)
		{
			System.out.println("Exception : SordAllocDef : actionVoucher " +e.getMessage());
			throw new ITMException(e);
		}
		catch(Exception e)
		{
			System.out.println("Exception : SordAllocDef : actionHandler " +e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try{
			conn.close();
			}catch(Exception e){}
		}
		System.out.println("valueXmlString.toString() "+valueXmlString.toString());
		return valueXmlString.toString();		
	}
	
	private HashMap getItemVoumeMap(String itemCode,String lotNo,Connection con)throws Exception
	{
		double packSize = 0,itemSize = 0,lotSize = 0;
		PreparedStatement pstmt = null;
		String sql="";
		ResultSet rs = null;
		double itmLen = 0,itmWidth = 0,itmHeight = 0,itemWeight = 0,lotLen = 0 ,lotHeight = 0,lotWidth = 0,lotWeight = 0;
		HashMap dataVolumeMap = new HashMap();
		
		try {
			
			sql = "SELECT I.LENGTH ITEM_LEN,I.WIDTH ITEM_WID,I.HEIGHT ITEM_HEIGHT,I.GROSS_WEIGHT ITEM_WEIGHT,"
				  +" L.LENGTH LITEM_LEN,L.WIDTH LITEM_WID,L.HEIGHT LITEM_HEIGHT,L.SHIPPER_SIZE SHIPSIZE,L.GROSS_WEIGHT LOT_WEIGHT FROM"
				  +" ITEM I,ITEM_LOT_PACKSIZE L"
				  +" WHERE I.ITEM_CODE = L.ITEM_CODE"
				  +" AND L.LOT_NO__FROM <= ? AND L.LOT_NO__TO >= ?"
				  +" AND  I.ITEM_CODE = ?";
				  
			
			pstmt = con.prepareStatement(sql);
			if(lotNo != null && lotNo.length() > 0)
			{
				pstmt.setString(1, lotNo);
				pstmt.setString(2, lotNo);
			}
			else
			{
				pstmt.setString(1, "00");
				pstmt.setString(2, "ZZ");
			}
			pstmt.setString(3, itemCode);
			rs = pstmt.executeQuery();
			
			if(rs.next())
			{
				itmLen = rs.getDouble("ITEM_LEN");
				itmWidth = rs.getDouble("ITEM_WID");
				itmHeight = rs.getDouble("ITEM_HEIGHT");
				itemWeight = rs.getDouble("ITEM_WEIGHT");
				lotLen = rs.getDouble("LITEM_LEN");
				lotWidth = rs.getDouble("LITEM_WID");
				lotHeight = rs.getDouble("LITEM_HEIGHT");				
				packSize = rs.getDouble("SHIPSIZE");				
				lotWeight = rs.getDouble("LOT_WEIGHT");				
			}
			
			//packSize = (lotHeight * lotWidth * lotLen)/(itmLen * itmWidth * itmHeight);
			/*itemSize = Math.floor(itmLen * itmWidth * itmHeight);
			lotSize = Math.floor((lotHeight * lotWidth * lotLen));*/
			itemSize = itmLen * itmWidth * itmHeight;
			lotSize = lotHeight * lotWidth * lotLen;
			
			dataVolumeMap.put("PACK_SIZE", packSize);
			dataVolumeMap.put("ITEM_SIZE", itemSize);
			dataVolumeMap.put("LOT_SIZE", lotSize);
			dataVolumeMap.put("ITEM_WEIGHT", itemWeight);
			dataVolumeMap.put("PACK_WEIGHT", lotWeight);
			
			if(pstmt != null)
			{
				pstmt.close();
				pstmt = null;
		
			}
			if(rs != null)
			{
				rs.close();
				rs = null;
		
			}
			
			
			
		} catch (Exception e) {
			// TODO: handle exception

			throw e;
		}
		finally
		{
			if(pstmt != null)
			{
				pstmt.close();
				pstmt = null;
		
			}
			if(rs != null)
			{
				rs.close();
				rs = null;
		
			}
		}
		
		return dataVolumeMap;
	}	
	private String getAttribValue(Node detailNode, String nodeName, String attribStr)
	{
		String attribValue = "";
		try
		{
			String domStr = genericUtility.serializeDom(detailNode);
			Document dom = genericUtility.parseString(domStr);
			if( dom != null /*&& dom.getAttributes() != null*/)
			{
				Node attributeNode = dom.getElementsByTagName( nodeName ).item(0);
				attribValue = getAttribValue(attributeNode, attribStr);
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception : getAttribValue :" + e.getMessage());
		}
		return attribValue;
	}
	private String getAttribValue(Node detailNode, String attribStr)
	{
		String attribValue = "";
		try
		{
			if( detailNode != null && detailNode.getAttributes() != null)
			{
				Node attribNode = detailNode.getAttributes().getNamedItem( attribStr );
				if( attribNode != null )
				{
					attribValue = checkNull( attribNode.getNodeValue() );
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception : getAttribValue :" + e.getMessage());
		}
		return attribValue;
	}
	
	private String checkNull(String str)
	{
		if( str == null)
		{
			str = "";
		}
		
		return str;
	}
}