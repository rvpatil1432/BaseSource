package ibase.webitm.ejb.dis.adv;
//changed by Alka on 12/09/07 for changing the import statements from * to relevant classes.
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.sql.Timestamp;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

//import javax.ejb.SessionBean;
import javax.ejb.CreateException;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.utility.E12GenericUtility;
import ibase.system.config.ConnDriver;
import java.text.SimpleDateFormat;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class InvPackAct extends ActionHandlerEJB implements InvPackActLocal, InvPackActRemote
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

	public String actionHandler() throws RemoteException,ITMException
	{
		return "";
	}

	public String actionHandler(String actionType, String xmlString, String xmlString1, String objContext, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;

		String  retString = null;
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		try
		{
			System.out.println("XML String :"+xmlString+ " XML String1 :"+xmlString1);
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				dom = genericUtility.parseString(xmlString);
			}
			if(xmlString1 != null && xmlString1.trim().length()!=0)
			{
				dom1 = genericUtility.parseString(xmlString1);
				System.out.println("dom :"+dom+" dom1 :"+dom1);
			}

			System.out.println("actionType:"+actionType+":");


			if (actionType.equalsIgnoreCase("Stock"))
			{
				retString = actionStock(dom, dom1, objContext, xtraParams);
			}
			if (actionType.equalsIgnoreCase("Split Stock"))
			{
				System.out.println("SplitStock----ACTION SPLITE STOCK CALLING ");
				retString = actionSplitStock(dom, dom1, objContext, xtraParams);
			}
			if (actionType.equalsIgnoreCase("Packet"))
			{
				retString = actionPacket(dom, dom1, objContext, xtraParams);
			}
			if (actionType.equalsIgnoreCase("Default2"))
			{
				retString = actionDefault2(dom, dom1, objContext, xtraParams);
			}
			if (actionType.equalsIgnoreCase("Default3"))
			{
				retString = actionDefault3(dom, dom1, objContext, xtraParams);
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :InvPack :actionHandler(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning String from InvPack : actionHandler"+retString);
		return retString;
	}

	public String actionHandlerTransform(String actionType, String xmlString, String xmlString1, String objContext, String xtraParams, String selDataStr) throws RemoteException,ITMException
	{
		System.out.println("actionHandlerTransform is calling.............");
		Document dom = null;
		Document dom1 = null;
		Document selDataDom = null;

		String  retString = null;
		try
		{
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				dom = new  ibase.utility.E12GenericUtility().parseString(xmlString);
			}
			if(xmlString1 != null && xmlString1.trim().length()!=0)
			{
				dom1 = new  ibase.utility.E12GenericUtility().parseString(xmlString1);
			}
			if(selDataStr != null && selDataStr.length() > 0)
			{
				selDataDom = new  ibase.utility.E12GenericUtility().parseString(selDataStr);
			}

			System.out.println("actionType:"+actionType+":");

			if (actionType.equalsIgnoreCase("Stock"))
			{
				retString = stockTransform(dom, dom1, objContext, xtraParams, selDataDom);
			}
			if (actionType.equalsIgnoreCase("Packet"))
			{
				retString = packetTransform(dom, dom1, objContext, xtraParams, selDataDom);
			}
			//added by cpandey on 26/11/12 
//			if (actionType.equalsIgnoreCase("Split Stock"))
//			{
//				System.out.println("splitStockTransform  NEXT...........");
//				retString = splitStockTransform(dom, dom1, objContext, xtraParams, selDataDom);
//			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :InvPack :actionHandlerTransform(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning String from InvPack : actionHandlerTransform"+retString);
		return retString;
	}


	private String actionStock(Document dom, Document dom1, String objContext, String xtraParams) throws RemoteException,ITMException
	{
		String itemCode = "", locCode = "", siteCode = "", quantity = "", lineNoOrd = "", orderNo = "", issCriteria = "";
		String available = "", sql = "", trackShelfLife = "", mfgDate1 = "", expDate1 = "", sql1 = "" ;

		//Added by msalam on 21/06/07 to put filter for LotNo int query for req-id:: DI78GIN016
		String lotNo = null;
		String lotSl = null;
		//End of msalam


		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		java.sql.Date mfgDate = null, expDate = null;
		Statement stmt = null, stmt1 = null;
		ResultSet rs = null, rs1 = null;
		Connection conn = null;
		double cQuantity = 0d, quantity1 = 0d;
		ConnDriver connDriver = new ConnDriver();
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		String orderType = "", useInvStatus = "";
		try
		{
			if (dom != null)
			{
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
				stmt = conn.createStatement();
				stmt1 = conn.createStatement();

				itemCode = genericUtility.getColumnValue("item_code",dom);
				System.out.println("itemCode.... :"+itemCode);

				locCode = genericUtility.getColumnValue("loc_code",dom);
				System.out.println("locCode ......:"+locCode);
				quantity = genericUtility.getColumnValue("quantity",dom);
				siteCode = genericUtility.getColumnValue("site_code",dom1);
				lineNoOrd =  genericUtility.getColumnValue("line_no__ord", dom);
				orderNo =  genericUtility.getColumnValue("order_no", dom);

				//Added by msalam on 21/06/07 to put filter for LotNo int query for req-id:: DI78GIN016
				lotNo =  genericUtility.getColumnValue("lot_no", dom);
				System.out.println("Lot No :: " + lotNo);
				lotSl = genericUtility.getColumnValue("lot_sl", dom);
				System.out.println("Lot SL :: " + lotSl);
				//End of msalam

				//modified by alam as required for req-id:: DI78GIN016
				if (locCode == null || locCode.trim().length() == 0)
				{
					available = "'Y','N'";
				}
				else
				{
					locCode = locCode.trim() ;
					System.out.println("locCode :"+locCode);
					sql = "SELECT INVSTAT.AVAILABLE "					 //query modified by rajendra
							+"FROM INVSTAT, LOCATION "
							+"WHERE  LOCATION.INV_STAT = INVSTAT.INV_STAT "
							+"AND  LOCATION.LOC_CODE = '"+locCode+"' ";

					System.out.println("sql ....:"+sql);
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						available = rs.getString(1);
						available = "'" + available + "'";
						System.out.println("available :"+available);
                    }
                    //added by monika salla 4 dec 21 to close dirty connection 
                    rs.close();
					rs= null;
					//end
				}
				orderType = genericUtility.getColumnValue("order_type",dom1);
				useInvStatus = genericUtility.getColumnValue("use_invstatus",dom1);
				if( "N".equals(orderType))
				{
					if ( "U".equals(useInvStatus))
					{
						available = "'Y'";
					}
					else
					{
						available = "'N'";
					}
				}
				
				sql = "SELECT CASE WHEN TRACK_SHELF_LIFE IS NULL THEN 'N' ELSE TRACK_SHELF_LIFE END "
						+"FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
				System.out.println("sql :"+sql);
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					trackShelfLife = rs.getString(1);
                }
                //added by monika salla 4 dec 21 to close dirty connection 
                    rs.close();
					rs= null;
					//end
                
				System.out.println("trackShelfLife :"+trackShelfLife);
				//if (trackShelfLife.equals("N"))  //if condition commented as PB Code commented on 02/02/06
				//{
				/*
				//commented by msalam for req-id :: DI78GIN016 as sql will be made as per the values of lot_no, loc_code
					sql ="SELECT STOCK.ITEM_CODE, "
							+"STOCK.LOT_NO, "
							+"STOCK.LOT_SL, "
							+"STOCK.ALLOC_QTY, "
							+"STOCK.MFG_DATE, "
							+"STOCK.EXP_DATE, "
							+"STOCK.QUANTITY, "
							+"STOCK.LOC_CODE, "
							+"STOCK.NO_ART, "
							+"STOCK.RATE, "
							+"STOCK.QUANTITY - STOCK.ALLOC_QTY "
						+"FROM STOCK, INVSTAT "
						+"WHERE STOCK.INV_STAT = INVSTAT.INV_STAT "
							+"AND STOCK.ITEM_CODE = '"+itemCode+"' "
							+"AND STOCK.LOC_CODE LIKE '"+locCode+"' "
							+"AND STOCK.SITE_CODE = '"+siteCode+"' "
							+"AND STOCK.QUANTITY > 0 "
							+"AND INVSTAT.AVAILABLE IN ("+available+") "
							+"AND INVSTAT.STAT_TYPE = 'M'"
							+"AND (STOCK.PACK_REF IS NULL OR LENGTH(LTRIM(RTRIM(STOCK.PACK_REF))) = 0)";		 //add by rajendra
				 */

				/////////////////////////
				//added by msalam for req-id :: DI78GIN016 as sql will be made as per
				//the values of itemCode, lot_no, loc_code
				String tempSqlStr = "SELECT STOCK.ITEM_CODE, "
						+"STOCK.LOT_NO, "
						+"STOCK.LOT_SL, "
						+"STOCK.ALLOC_QTY, "
						+"STOCK.MFG_DATE, "
						+"STOCK.EXP_DATE, "
						//					+"STOCK.QUANTITY, "
						+"STOCK.QUANTITY - STOCK.ALLOC_QTY AS QUANTITY, "
						+"STOCK.LOC_CODE, "
						+"STOCK.NO_ART, "
						+"STOCK.RATE, "
						+"STOCK.QUANTITY - STOCK.ALLOC_QTY "
						+"FROM STOCK, INVSTAT "
						+"WHERE STOCK.INV_STAT = INVSTAT.INV_STAT ";

				String filterStr = null;
				filterStr = "";

				/*
				ITEM_CODE
				SITE_CODE
				LOC_CODE
				LOT_NO
				LOT_SL
				 */
				if(itemCode != null && itemCode.trim().length() > 0)
					filterStr += " AND STOCK.ITEM_CODE = '"+itemCode+"' "; //added by msalam for req-id :: DI78GIN016

				filterStr += " AND STOCK.SITE_CODE = '" + siteCode + "' ";

				if(locCode != null && locCode.trim().length() > 0)
					filterStr = filterStr + " AND STOCK.LOC_CODE = '" + locCode + "' "; //added by msalam for req-id :: DI78GIN016

				if(lotNo != null && lotNo.trim().length() > 0)
					filterStr += " AND STOCK.LOT_NO = '" + lotNo + "' "; //added by msalam for req-id :: DI78GIN016

				if(lotSl != null && lotSl.trim().length() > 0)
					filterStr += " AND STOCK.LOT_SL = '"+lotSl+"' "; //added by msalam for req-id :: DI78GIN016

				tempSqlStr = tempSqlStr + " "
						+ filterStr
						+ " AND STOCK.QUANTITY > 0 "
						+ " AND INVSTAT.AVAILABLE IN ("+available+") "
						+ " AND INVSTAT.STAT_TYPE = 'M' "
						+ " AND ( STOCK.PACK_REF IS NULL or LENGTH(LTRIM(RTRIM(STOCK.PACK_REF))) = 0) ";		 //add by rajendra

				sql = tempSqlStr;
				//End added by msalam for req-id :: DI78GIN016
				///////////////////////

				System.out.println("sql :"+sql);
				rs = stmt.executeQuery(sql);
				SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
				while (rs.next())
				{
					mfgDate = rs.getDate(5);
					if (mfgDate != null)
					{
						mfgDate1 = sdf.format(mfgDate);

					}
					expDate = rs.getDate(6);
					System.out.println("expDate :"+expDate);
					if (expDate != null)
					{
						expDate1 = sdf.format(expDate);
					}
					if (rs.getDouble(11) > 0)
					{
						//TRIM REMOVED BY ALKA 29/05/2007
						valueXmlString.append("<Detail>\r\n");
						//valueXmlString.append("<item_code>").append("<![CDATA[").append(rs.getString(1).trim()).append("]]>").append("</item_code>\r\n");
						valueXmlString.append("<item_code>").append("<![CDATA[").append(rs.getString(1)).append("]]>").append("</item_code>\r\n");
						//valueXmlString.append("<loc_code>").append("<![CDATA[").append(rs.getString(8).trim()).append("]]>").append("</loc_code>\r\n");
						valueXmlString.append("<loc_code>").append("<![CDATA[").append(rs.getString(8)).append("]]>").append("</loc_code>\r\n");
						//valueXmlString.append("<lot_no>").append("<![CDATA[").append(rs.getString(2).trim()).append("]]>").append("</lot_no>\r\n");
						valueXmlString.append("<lot_no>").append("<![CDATA[").append(rs.getString(2)).append("]]>").append("</lot_no>\r\n");
						//valueXmlString.append("<lot_sl>").append("<![CDATA[").append(rs.getString(3).trim()).append("]]>").append("</lot_sl>\r\n");
						valueXmlString.append("<lot_sl>").append("<![CDATA[").append(rs.getString(3)).append("]]>").append("</lot_sl>\r\n");
						valueXmlString.append("<quantity>").append("<![CDATA[").append(rs.getDouble(11)).append("]]>").append("</quantity>\r\n");
						valueXmlString.append("<rate>").append("<![CDATA[").append(rs.getDouble(10)).append("]]>").append("</rate>\r\n");
						valueXmlString.append("<mfg_date>").append("<![CDATA[").append(mfgDate1).append("]]>").append("</mfg_date>\r\n");
						valueXmlString.append("<exp_date>").append("<![CDATA[").append(expDate1).append("]]>").append("</exp_date>\r\n");
						valueXmlString.append("<no_art>").append("<![CDATA[").append(rs.getInt(9)).append("]]>").append("</no_art>\r\n");
						//valueXmlString.append("<alloc_qty>").append("<![CDATA[").append(rs.getDouble(4)).append("]]>").append("</alloc_qty>\r\n");
						valueXmlString.append("</Detail>\r\n");
						// END -- TRIM REMOVED BY ALKA 29/05/2007
					}
					mfgDate1 = "";
					expDate1 = "";
				}
                stmt.close();
                //added by monika salla 4 dec 21 to close dirty connection 
                    rs.close();
					rs= null;
					stmt= null;//end
                
			}

			valueXmlString.append("</Root>\r\n");
		}

		catch (SQLException sqx)
		{
			System.out.println("The sqlException occure in InvPack :"+sqx);
			throw new ITMException(sqx);
		}
		catch (Exception e)
		{
			System.out.println("The Exception occure in InvPakc :"+e);
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("Closing Connection.....");
				conn.close();
				conn = null;
			}
			catch (Exception e){}
		}
		System.out.println("valueXmlString return from InvPack  :"+valueXmlString.toString());
		return valueXmlString.toString();
	}

	private String stockTransform(Document dom,Document dom1, String objContext, String xtraParams, Document selDataDom)throws ITMException
	{
		System.out.println("stockTransform is calling.............");
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		String sql = "", detCnt = "0", errCode = "", errString = "", issCriteria = "";
		Connection conn = null;
		Statement stmt = null;
		ResultSet rset = null;
		Node currDetail = null;
		ConnDriver connDriver = new ConnDriver();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		try
		{
			String orderType = null;
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			stmt = conn.createStatement();
			orderType = genericUtility.getColumnValue("order_type", dom1);
			//detCnt = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"detCnt"); //Will be Uncommented later - Aviprash 30/01/06
			String icQtyOrdStr = genericUtility.getColumnValue("quantity", dom);
			System.out.println("icQtyOrdStr............." + icQtyOrdStr);
			double icQtyOrd = 0;
			/*
			if (icQtyOrdStr == null)
			{
				errCode = "VTREQQTY";
				errString = itmDBAccess.getErrorString("",errCode,"","",conn);
				conn.close();
				conn = null;
				return errString;
			}
			 */
			if(icQtyOrdStr != null && icQtyOrdStr.trim().length() > 0)
			{
				icQtyOrd = Double.parseDouble(icQtyOrdStr);
			}
			System.out.println("icQtyOrd  :"+icQtyOrd);
			String lineNoOrd = genericUtility.getColumnValue("line_no__ord", dom);
			String orderNo = genericUtility.getColumnValue("order_no", dom);
			System.out.println("lineNoOrd :"+lineNoOrd+" orderNo :"+orderNo);
			NodeList detailList = selDataDom.getElementsByTagName("Detail");
			int noOfDetails = detailList.getLength();
			String itemCode = genericUtility.getColumnValue("item_code", dom);
			for(int ctr = 0; ctr < noOfDetails; ctr++)
			{
				currDetail = detailList.item(ctr);
				valueXmlString.append("<Detail>");
				if (("P").equalsIgnoreCase(orderType) || ("D").equalsIgnoreCase(orderType) || ("W").equalsIgnoreCase(orderType))
				{
					valueXmlString.append("<line_no__ord isSrvCallOnChg='1'>").append("<![CDATA[").append(lineNoOrd).append("]]>").append("</line_no__ord>\r\n");
				}
				else
				{				
					valueXmlString.append("<line_no__ord isSrvCallOnChg='0'>").append("<![CDATA[").append(lineNoOrd).append("]]>").append("</line_no__ord>\r\n");
				}

				valueXmlString.append("<order_no isSrvCallOnChg='0'>").append("<![CDATA[").append(orderNo).append("]]>").append("</order_no>\r\n");
				String pickQty = genericUtility.getColumnValueFromNode("quantity", currDetail);
				String mfgDate = genericUtility.getColumnValueFromNode("mfg_date", currDetail);
				String expDate = genericUtility.getColumnValueFromNode("exp_date", currDetail);
				//String itemCode = genericUtility.getColumnValue("item_code", dom);
				System.out.println("pickQty :"+pickQty+"  mfgDate :"+mfgDate+" expDate :"+expDate+" itemCode :"+itemCode);
				sql = "SELECT ISS_CRITERIA FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
				System.out.println("sql :"+sql);
				rset = stmt.executeQuery(sql);
				if (rset.next())
				{
					issCriteria = rset.getString(1);
                }
                
                    //added by monika salla 4 dec 21 to close dirty connection 
                    rset.close();
					rset= null;
					//end
				if (issCriteria == null)
				{
					issCriteria = " ";
                }
                


				System.out.println("issCriteria :"+issCriteria);
				if (!issCriteria.equals("W"))
				{ 
					//if (Double.parseDouble(pickQty) > icQtyOrd)  //CHANGED BY ALKA ON 30/07/07 TO RETRIEVE DATA EVEN IF QUANTITY IS 0
					if (icQtyOrd != 0 && Double.parseDouble(pickQty) > icQtyOrd)
					{
						pickQty = String.valueOf(icQtyOrd);
					}
					else if (icQtyOrd == 0)
					{
						icQtyOrd = Double.parseDouble(pickQty);
					}
				}
				valueXmlString.append("<item_code isSrvCallOnChg='1'>").append("<![CDATA[").append(itemCode == null?"":itemCode).append("]]>").append("</item_code>\r\n");
				valueXmlString.append("<quantity isSrvCallOnChg='0'>").append("<![CDATA[").append(pickQty).append("]]>").append("</quantity>\r\n");
				icQtyOrd = icQtyOrd - Double.parseDouble(pickQty);
				String locCode = genericUtility.getColumnValueFromNode("loc_code", currDetail);
				//String locCodeDet = genericUtility.getColumnValue("loc_code", dom);
				System.out.println("locCode :"+locCode);
				/*if (locCodeDet == null || locCodeDet.trim().length() == 0)
				{
					valueXmlString.append("<loc_code isSrvCallOnChg='0'>").append("<![CDATA[").append(locCode == null?"":locCode).append("</loc_code>");
				}*/
				valueXmlString.append("<loc_code isSrvCallOnChg='1'>").append("<![CDATA[").append(locCode == null?"":locCode).append("]]>").append("</loc_code>\r\n");
				String lotNo = genericUtility.getColumnValueFromNode("lot_no", currDetail);
				valueXmlString.append("<lot_no isSrvCallOnChg='1'>").append("<![CDATA[").append(lotNo == null?"":lotNo.trim()).append("]]>").append("</lot_no>\r\n");
				String lotSl = genericUtility.getColumnValueFromNode("lot_sl", currDetail);
				valueXmlString.append("<lot_sl isSrvCallOnChg='1'>").append("<![CDATA[").append(lotSl == null?"":lotSl.trim()).append("]]>").append("</lot_sl>\r\n");
				// 17-05-2007 manoharan
				//valueXmlString.append("<mfg_date isSrvCallOnChg='0'>").append("<![CDATA[").append(mfgDate == null?"":mfgDate).append("]]>").append("</mfg_date>\r\n");
				if (mfgDate != null && mfgDate.trim().length() > 0)
				{
					valueXmlString.append("<mfg_date isSrvCallOnChg='0'>").append("<![CDATA[").append(mfgDate).append("]]>").append("</mfg_date>\r\n");
				}
				//valueXmlString.append("<exp_date isSrvCallOnChg='0'>").append("<![CDATA[").append(expDate == null?"":expDate).append("]]>").append("</exp_date>\r\n");
				if (expDate != null && expDate.trim().length() > 0)
				{
					valueXmlString.append("<exp_date isSrvCallOnChg='0'>").append("<![CDATA[").append(expDate).append("]]>").append("</exp_date>\r\n");
				}
				//update invstat set USABLE = 'N' where loc_code = 'FRSH ';
				// end 17-05-2007 manoharan
				valueXmlString.append("</Detail>");
			}// for end
			valueXmlString.append("</Root>");
		}// end try
		catch(ITMException itme)
		{
			throw itme;
		}
		catch(Exception e)
		{
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				conn.close();
				conn = null;
			}
			catch (Exception e){}
		}
		System.out.println("valueXmlString return from InvPack  :"+valueXmlString.toString());
		System.out.println("valueXmlString from Stock:"+valueXmlString.toString());
		return valueXmlString.toString();
	}

	private String actionSplitStock(Document dom, Document dom1, String objContext, String xtraParams) throws RemoteException,ITMException
	{
		String itemCode = "", siteCode = "", qtyPack = "", noArt = "", packInst = "", grossWtPack = "";
		String tareWtPack = "", packCode = "", dimension = "", locCode = "", lotNo = "", grossWt = "", tareWt = "";
		String maxLotSl = "", lotSl = "", sql = "", value1 = "0", value = "", str = "",lineNoOrd = "",orderNo = "";
		String orderType = "";
		char ch = ' ';
		long retNumber = 0l;
		boolean isCheckNum = true;
		Statement stmt = null;
		ResultSet rs = null;
		Connection conn = null;
		ArrayList retTokens = new ArrayList();
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		ConnDriver connDriver = new ConnDriver();
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		PreparedStatement pstmt = null; // 12/03/12 manoharan
		String locDescr = "", prefix = "",lead = "";
		double tareTmp = 0, grossTmp = 0, netTmp = 0; 
		int strLength = 0, numLength = 0;
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			orderType = genericUtility.getColumnValue("order_type", dom1);
			System.out.println("Order Type ::"+orderType);
			if (orderType != null && (orderType.equalsIgnoreCase("P") || orderType.equalsIgnoreCase("R") || orderType.equalsIgnoreCase("N")))
			{
				stmt = conn.createStatement();
				//changes by cpandey on 07/11/12
				lineNoOrd = genericUtility.getColumnValue("line_no__ord", dom);
				System.out.println("line no order-->> ["+lineNoOrd+"]");
				orderNo = genericUtility.getColumnValue("order_no", dom);
				System.out.println("order no--->> ["+orderNo+"]");
				//end of changes on 17/11/12
				itemCode = genericUtility.getColumnValue("item_code", dom);
				System.out.println("itemCode--->> ["+itemCode+"]");
				siteCode = genericUtility.getColumnValue("site_code", dom1);
				System.out.println("siteCode--->> ["+siteCode+"]");
				qtyPack = genericUtility.getColumnValue("qty_pack", dom);
				System.out.println("qtyPack--->> ["+qtyPack+"]");
				noArt = genericUtility.getColumnValue("no_art", dom);
				
				packInst = genericUtility.getColumnValue("pack_instr", dom);
				grossWtPack = genericUtility.getColumnValue("grosswt_pack", dom);
				tareWtPack = genericUtility.getColumnValue("tarewt_pack", dom);
				packCode = genericUtility.getColumnValue("pack_code", dom);
				dimension = genericUtility.getColumnValue("dimension", dom);
				locCode = genericUtility.getColumnValue("loc_code", dom);
				lotNo = genericUtility.getColumnValue("lot_no", dom);
				grossWt = genericUtility.getColumnValue("gross_weight", dom);
				tareWt = genericUtility.getColumnValue("tare_weight", dom);
				System.out.println("tareWt---->>["+tareWt+"]");
				sql = "SELECT CASE WHEN MAX(LOT_SL) IS NULL THEN '0' ELSE MAX(LOT_SL) END "
						+"FROM STOCK "
						+"WHERE SITE_CODE = '"+siteCode+"' "
						+"AND ITEM_CODE = '"+itemCode+"' "
						+"AND LOT_NO = '"+lotNo+"' "
						+"AND ISNUMBER(LOT_SL) = 0";
				System.out.println("sql :"+sql);
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					maxLotSl = rs.getString(1);
					System.out.println("maxLotSl --:"+maxLotSl);
				}
				else
				{
					maxLotSl = "0";
				}
				if (maxLotSl.indexOf(".") > -1)
				{
					maxLotSl = maxLotSl.substring(0,maxLotSl.indexOf(".")) ;
				}
				lotSl = maxLotSl;
				System.out.println("maxLotSl --:"+maxLotSl);
				isCheckNum = checkIsNumber(packInst);
				if (!isCheckNum)
				{
					retTokens = genericUtility.getTokenList(packInst, "-");
					System.out.println("retTokens :"+retTokens.size());
					//str = retTokens.get(1).toString();
					str = retTokens.get(0).toString();
					strLength = retTokens.get(1).toString().length();
					if (strLength == 0)
					{
						strLength = retTokens.get(0).toString().length();
					}
					for (int j = 0; j < str.length(); j++)
					{
						ch = str.charAt(j);
						System.out.println("ch --:"+ch);
						if (Character.isDigit(ch))
						{
							value1 = value1 + ch;
							System.out.println("Value1 :"+value1);
						}
						else
						{
							prefix = prefix + ch;
						}
					}//for end
					retNumber = Long.parseLong(value1);
					if (retNumber == 0)
					{
						retNumber = 1;
					}
					// 20/09/14 manoharan in case the entered range is like R1-R500 the str is failed as the 
					// the number 1 is not there in the 
					//retTokens = genericUtility.getTokenList(str, String.valueOf(retNumber));
					//System.out.println("retTokens --:"+retTokens.size());
					//str = retTokens.get(1).toString();
					System.out.println("prefix ["+prefix + "]");
					
					// 
				}//end if
				else
				{
					if (packInst.indexOf("-") > -1)
					{
						retTokens = genericUtility.getTokenList(packInst, "-");
						retNumber = Long.parseLong(retTokens.get(0).toString());
						strLength = retTokens.get(1).toString().length();
					}
					else
					{
						retNumber = Long.parseLong(packInst);
						strLength = packInst.length();
					}
					
                }

                ////added by monika salla 4 dec 21 to close dirty connection 
                    rs.close();
					rs= null;
					//end
                    
				// 12/03/12 manoharan
				sql =  "SELECT DESCR FROM LOCATION WHERE LOC_CODE = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, locCode);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					locDescr = rs.getString(1)==null?"":rs.getString(1);
				}
				pstmt.close();
				rs.close();
				pstmt = null;
				rs = null;
				if (strLength > 0 && prefix.length() > 0 )
				{
					numLength = strLength - prefix.length();
				}
				else 
				{
					numLength = strLength;
				}
				System.out.println("strLength ["+strLength + "] retNumber [" + retNumber + "] prefix [" + prefix + "] numLength [" + numLength + "]");
				for (int i = 0; i < Integer.parseInt(noArt); i++)
				{
					// 01/07/13 manoharan lot_sl found with spaces
					lotSl = lotSl.trim();
					// end 01/07/13 manoharan lot_sl found with spaces
					lotSl = String.valueOf(Long.parseLong(lotSl)+1);
					System.out.println("lotSl :"+lotSl);
					// 12/03/12 manoharan itemchanged should not happen for any column as it is working very slow
					valueXmlString.append("<Detail>\r\n");
					//changes done by cpandey on 07/11/12 line_no__ord
					valueXmlString.append("<line_no__ord isSrvCallOnChg='0'>").append("<![CDATA[").append(lineNoOrd).append("]]>").append("</line_no__ord>\r\n");
					valueXmlString.append("<order_no isSrvCallOnChg='0'>").append("<![CDATA[").append(orderNo).append("]]>").append("</order_no>\r\n");
					//end of changes done by cpandey on 07/11/12
					valueXmlString.append("<loc_code isSrvCallOnChg='0'>").append("<![CDATA[").append(locCode).append("]]>").append("</loc_code>\r\n");
					valueXmlString.append("<dimension isSrvCallOnChg='0'>").append("<![CDATA[").append(dimension).append("]]>").append("</dimension>\r\n");
					valueXmlString.append("<pack_code isSrvCallOnChg='0'>").append("<![CDATA[").append(packCode).append("]]>").append("</pack_code>\r\n");
					valueXmlString.append("<grosswt_pack isSrvCallOnChg='0'>").append("<![CDATA[").append(grossWtPack).append("]]>").append("</grosswt_pack>\r\n");
					valueXmlString.append("<tarewt_pack isSrvCallOnChg='0'>").append("<![CDATA[").append(tareWtPack).append("]]>").append("</tarewt_pack>\r\n");
					valueXmlString.append("<gross_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(grossWt).append("]]>").append("</gross_weight>\r\n");
					valueXmlString.append("<tare_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(tareWt).append("]]>").append("</tare_weight>\r\n");
					valueXmlString.append("<qty_pack isSrvCallOnChg='0'>").append("<![CDATA[").append(qtyPack).append("]]>").append("</qty_pack>\r\n");
					valueXmlString.append("<quantity isSrvCallOnChg='0'>").append("<![CDATA[").append(qtyPack).append("]]>").append("</quantity>\r\n");
					valueXmlString.append("<no_art isSrvCallOnChg='0'>").append("<![CDATA[").append(1).append("]]>").append("</no_art>\r\n");
					valueXmlString.append("<lot_sl isSrvCallOnChg='0'>").append("<![CDATA[").append(lotSl).append("]]>").append("</lot_sl>\r\n");
					// 12/03/12 manoharan additional values set
					if (tareWt == null) 
					{
						tareWt = "0";
					}
					if (grossWt == null) 
					{
						grossWt = "0";
					}
					tareTmp = Double.parseDouble(tareWt);
					grossTmp = Double.parseDouble(grossWt);
					netTmp = grossTmp - tareTmp; 
					valueXmlString.append("<net_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(netTmp).append("]]>").append("</net_weight>\r\n");

					valueXmlString.append("<location_descr isSrvCallOnChg='0'>").append("<![CDATA[").append(locDescr).append("]]>").append("</location_descr>\r\n");
					// end 12/03/12 manoharan
					if (numLength > 0 && String.valueOf(retNumber).length() < numLength)
					{
						lead = "000000000000000".substring( "000000000000000".length() - (numLength - (String.valueOf(retNumber).length()) ));
					}
					System.out.println("strLength ["+strLength + "] retNumber [" + retNumber + "] prefix [" + prefix + "] lead [" + lead + "] numLength [" + numLength + "]");
					//if (!checkIsNumber(str))
					//{
					//	packInst = str + String.valueOf(retNumber);
					//}
					//else
					//{
					//	packInst = String.valueOf(retNumber);
					//}
					
					packInst = prefix + lead + String.valueOf(retNumber);
					valueXmlString.append("<pack_instr isSrvCallOnChg='0'>").append("<![CDATA[").append(packInst).append("]]>").append("</pack_instr>\r\n");
					valueXmlString.append("</Detail>\r\n");
					retNumber = retNumber + 1;
				}//end for
				stmt.close();
			}
			valueXmlString.append("</Root>\r\n");
		}
		catch (SQLException sqx)
		{
			System.out.println("The SQLException occurs in invPakc for SplitStock button :"+sqx);
			throw new ITMException(sqx);
		}
		catch (Exception e)
		{
			System.out.println("The Exception occure in InvPakc :"+e);
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				conn.close();
				conn = null;
			}
			catch (Exception e){}
		}
		System.out.println("valueXmlString return from InvPack  :"+valueXmlString.toString());
		return valueXmlString.toString();
	}
	//added  by cpandey on 09/11/12
	//private String splitStockTransform(Document dom,Document dom1, String objContext, String xtraParams, Document selDataDom)throws ITMException
//	{
//		System.out.println("stockTransform is calling.............");
//		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
//		String sql = "", detCnt = "0", errCode = "", errString = "", issCriteria = "";
//		double tareTmp = 0, grossTmp = 0, netTmp = 0; 
//		Statement stmt = null;
//		PreparedStatement pstmt = null;
//		ResultSet rs = null;
//		Connection conn = null;
//		ResultSet rset = null;
//		Node currDetail = null;
//		ConnDriver connDriver = new ConnDriver();
//		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
//		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
//		try
//		{
//			String orderType = null;
//			conn = connDriver.getConnectDB("DriverITM");
//			stmt = conn.createStatement();
//			
//			orderType = genericUtility.getColumnValue("order_type", dom1);
//			//detCnt = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"detCnt"); //Will be Uncommented later - Aviprash 30/01/06
//			String icQtyOrdStr = genericUtility.getColumnValue("quantity", dom);
//			System.out.println("icQtyOrdStr............." + icQtyOrdStr);
//			double icQtyOrd = 0;
//			/*
//		if (icQtyOrdStr == null)
//		{
//			errCode = "VTREQQTY";
//			errString = itmDBAccess.getErrorString("",errCode,"","",conn);
//			conn.close();
//			conn = null;
//			return errString;
//		}*/
//			
//			String lineNoOrd = "",orderNo = "",itemCode = "",siteCode = "",qtyPack = "",noArt = "",packInst = "",grossWtPack = "",tareWtPack = "";
//			String 	packCode = "",dimension = "",locCode = "",lotNo = "",grossWt = "",tareWt = "",locDescr = "";
//			
//			lineNoOrd = genericUtility.getColumnValue("line_no__ord", dom);
//			System.out.println("lineNoOrd-->["+lineNoOrd+"]");
//			orderNo = genericUtility.getColumnValue("order_no", dom);
//			sql =  "SELECT DESCR FROM LOCATION WHERE LOC_CODE = ?";
//			pstmt = conn.prepareStatement(sql);
//			pstmt.setString(1, locCode);
//			rs =pstmt.executeQuery();
//			if (rs.next())
//			{
//				locDescr = rs.getString(1)==null?"":rs.getString(1);
//			}
//			pstmt.close();
//			rs.close();
//			pstmt = null;
//			rs = null;
//			System.out.println("orderNo-->["+orderNo+"]");
//			System.out.println("lineNoOrd :"+lineNoOrd+" orderNo :"+orderNo);
//			NodeList detailList = selDataDom.getElementsByTagName("Detail");
//			int noOfDetails = detailList.getLength();
//			itemCode = genericUtility.getColumnValue("item_code", dom);
//			System.out.println("itemCode-->["+itemCode+"]");
//			System.out.println("noOfDetails IN splite stock --->["+noOfDetails+"]-");
//			noArt = genericUtility.getColumnValueFromNode("no_art", currDetail);
//			System.out.println("noArt --->>["+noArt+"]");
//			for (int i = 0; i < Integer.parseInt(noArt); i++)
//			//for(int ctr = 0; ctr < noOfDetails; ctr++)
//			{
//				currDetail = detailList.item(i);
//				valueXmlString.append("<Detail>");
//				lineNoOrd = genericUtility.getColumnValueFromNode("line_no__ord", currDetail);
//				if (("P").equalsIgnoreCase(orderType) || ("R").equalsIgnoreCase(orderType))
//				{
//					valueXmlString.append("<line_no__ord isSrvCallOnChg='1'>").append("<![CDATA[").append(lineNoOrd).append("]]>").append("</line_no__ord>\r\n");
//				}
//				else
//				{				
//					valueXmlString.append("<line_no__ord isSrvCallOnChg='0'>").append("<![CDATA[").append(lineNoOrd).append("]]>").append("</line_no__ord>\r\n");
//				}
//				lineNoOrd = genericUtility.getColumnValueFromNode("line_no__ord", currDetail);
//				System.out.println("line no order-->> ["+lineNoOrd+"]");
//				orderNo = genericUtility.getColumnValueFromNode("order_no", currDetail);
//				System.out.println("order no--->> ["+orderNo+"]");
//				itemCode = genericUtility.getColumnValueFromNode("item_code", currDetail);
//				System.out.println("itemCode--->> ["+itemCode+"]");
//				siteCode = genericUtility.getColumnValueFromNode("site_code", currDetail);
//				System.out.println("siteCode--->> ["+siteCode+"]");
//				qtyPack = genericUtility.getColumnValueFromNode("qty_pack", currDetail);
//				System.out.println("qtyPack--->> ["+qtyPack+"]");
//				noArt = genericUtility.getColumnValueFromNode("no_art", currDetail);
//				System.out.println("noArt-->>["+noArt+"]");
//				packInst = genericUtility.getColumnValueFromNode("pack_instr", currDetail);
//				System.out.println("packInst-->>["+packInst+"]");
//				grossWtPack = genericUtility.getColumnValueFromNode("grosswt_pack", currDetail);
//				System.out.println("grossWtPack-->>["+grossWtPack+"]");
//				tareWtPack = genericUtility.getColumnValueFromNode("tarewt_pack", currDetail);
//				System.out.println("tareWtPack-->>["+tareWtPack+"]");
//				packCode = genericUtility.getColumnValueFromNode("pack_code",  currDetail);
//				System.out.println("packCode-->>["+packCode+"]");
//				dimension = genericUtility.getColumnValueFromNode("dimension", currDetail);
//				System.out.println("packCode-->>["+dimension+"]");
//				locCode = genericUtility.getColumnValueFromNode("loc_code", currDetail);
//				System.out.println("locCode-->>["+locCode+"]");
//				lotNo = genericUtility.getColumnValueFromNode("lot_no",currDetail);
//				System.out.println("lotNo-->>["+lotNo+"]");
//				grossWt =  genericUtility.getColumnValueFromNode("gross_weight", currDetail);
//				System.out.println("grossWt-->>["+grossWt+"]");
//				tareWt = genericUtility.getColumnValueFromNode("tare_weight", currDetail);
//				System.out.println("grossWt-->>["+tareWt+"]");
//				if(icQtyOrdStr != null && icQtyOrdStr.trim().length() > 0)
//				{
//					icQtyOrd = Double.parseDouble(icQtyOrdStr);
//					System.out.println("ic qty ord-->["+icQtyOrd+"]");
//				}
//				//end of addition on 09/11/12
//				//added by for xml appending on 09/11/22
//				 
//					//valueXmlString.append("<line_no__ord isSrvCallOnChg='0'>").append("<![CDATA[").append(lineNoOrd).append("]]>").append("</line_no__ord>\r\n");
//				valueXmlString.append("<order_no isSrvCallOnChg='0'>").append("<![CDATA[").append(orderNo).append("]]>").append("</order_no>\r\n");
//				valueXmlString.append("<line_no__ord isSrvCallOnChg='0'>").append("<![CDATA[").append(lineNoOrd).append("]]>").append("</line_no__ord>\r\n");
//				valueXmlString.append("<item_code isSrvCallOnChg='0'>").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code>\r\n");
//			//	valueXmlString.append("<site_code isSrvCallOnChg='0'>").append("<![CDATA[").append(siteCode).append("]]>").append("</site_code>\r\n");
//				valueXmlString.append("<qty_pack isSrvCallOnChg='0'>").append("<![CDATA[").append(qtyPack).append("]]>").append("</qty_pack>\r\n");
//				valueXmlString.append("<pack_instr isSrvCallOnChg='0'>").append("<![CDATA[").append(packInst).append("]]>").append("</pack_instr>\r\n");
//				valueXmlString.append("<grosswt_pack isSrvCallOnChg='0'>").append("<![CDATA[").append(grossWtPack).append("]]>").append("</grosswt_pack>\r\n");
//				valueXmlString.append("<gross_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(grossWt).append("]]>").append("</gross_weight>\r\n");
//				valueXmlString.append("<tarewt_pack isSrvCallOnChg='0'>").append("<![CDATA[").append(tareWtPack).append("]]>").append("</tarewt_pack>\r\n");
//				valueXmlString.append("<tare_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(tareWt).append("]]>").append("</tare_weight>\r\n");
//				valueXmlString.append("<pack_code isSrvCallOnChg='0'>").append("<![CDATA[").append(packCode).append("]]>").append("</pack_code>\r\n");
//				valueXmlString.append("<loc_code isSrvCallOnChg='0'>").append("<![CDATA[").append(locCode).append("]]>").append("</loc_code>\r\n");
//				valueXmlString.append("<no_art isSrvCallOnChg='0'>").append("<![CDATA[").append(noArt).append("]]>").append("</no_art>\r\n");
//				valueXmlString.append("<lot_no isSrvCallOnChg='0'>").append("<![CDATA[").append(lotNo).append("]]>").append("</lot_no>\r\n");
//				if (tareWt == null) 
//				{
//					tareWt = "0";
//				}
//				if (grossWt == null) 
//				{
//					grossWt = "0";
//				}
//				tareTmp = Double.parseDouble(tareWt);
//				grossTmp = Double.parseDouble(grossWt);
//				netTmp = grossTmp - tareTmp; 
//				valueXmlString.append("<net_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(netTmp).append("]]>").append("</net_weight>\r\n");
//				valueXmlString.append("<location_descr isSrvCallOnChg='0'>").append("<![CDATA[").append(netTmp).append("]]>").append("</location_descr>\r\n");
//				valueXmlString.append("<location_descr isSrvCallOnChg='0'>").append("<![CDATA[").append(locDescr).append("]]>").append("</location_descr>\r\n");
//				
//					//		valueXmlString.append("<order_no isSrvCallOnChg='0'>").append("<![CDATA[").append(orderNo).append("]]>").append("</order_no>\r\n");
//					//		//end of changes done by cpandey on 07/11/12
//					//		valueXmlString.append("<loc_code isSrvCallOnChg='0'>").append("<![CDATA[").append(locCode).append("]]>").append("</loc_code>\r\n");
//					//		valueXmlString.append("<dimension isSrvCallOnChg='0'>").append("<![CDATA[").append(dimension).append("]]>").append("</dimension>\r\n");
//					//		valueXmlString.append("<pack_code isSrvCallOnChg='0'>").append("<![CDATA[").append(packCode).append("]]>").append("</pack_code>\r\n");
//					//		valueXmlString.append("<grosswt_pack isSrvCallOnChg='0'>").append("<![CDATA[").append(grossWtPack).append("]]>").append("</grosswt_pack>\r\n");
//					//		valueXmlString.append("<tarewt_pack isSrvCallOnChg='0'>").append("<![CDATA[").append(tareWtPack).append("]]>").append("</tarewt_pack>\r\n");
//					//		valueXmlString.append("<gross_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(grossWt).append("]]>").append("</gross_weight>\r\n");
//					//		valueXmlString.append("<tare_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(tareWt).append("]]>").append("</tare_weight>\r\n");
//					//		valueXmlString.append("<qty_pack isSrvCallOnChg='0'>").append("<![CDATA[").append(qtyPack).append("]]>").append("</qty_pack>\r\n");
//					//		valueXmlString.append("<quantity isSrvCallOnChg='0'>").append("<![CDATA[").append(qtyPack).append("]]>").append("</quantity>\r\n");
//					//		valueXmlString.append("<no_art isSrvCallOnChg='0'>").append("<![CDATA[").append(1).append("]]>").append("</no_art>\r\n");
//					//		valueXmlString.append("<lot_sl isSrvCallOnChg='0'>").append("<![CDATA[").append(lotSl).append("]]>").append("</lot_sl>\r\n");
//					//		// 12/03/12 manoharan additional values set
//					//		if (tareWt == null) 
//					//		{
//					//			tareWt = "0";
//					//		}
//					//		if (grossWt == null) 
//					//		{
//					//			grossWt = "0";
//					//		}
//					//		tareTmp = Double.parseDouble(tareWt);
//					//		grossTmp = Double.parseDouble(grossWt);
//					//		netTmp = grossTmp - tareTmp; 
//					//		valueXmlString.append("<net_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(netTmp).append("]]>").append("</net_weight>\r\n");
//					//
//					//		valueXmlString.append("<location_descr isSrvCallOnChg='0'>").append("<![CDATA[").append(locDescr).append("]]>").append("</location_descr>\r\n");
//					//		// end 12/03/12 manoharan
//					//		if (!checkIsNumber(str))
//					//		{
//					//			packInst = str + String.valueOf(retNumber);
//					//		}
//					//		else
//					//		{
//					//			packInst = String.valueOf(retNumber);
//					//		}
//					//		valueXmlString.append("<pack_instr isSrvCallOnChg='0'>").append("<![CDATA[").append(packInst).append("]]>").append("</pack_instr>\r\n");
//                //end of xml appending on 09/11/12
//				valueXmlString.append("<order_no isSrvCallOnChg='0'>").append("<![CDATA[").append(orderNo).append("]]>").append("</order_no>\r\n");
//				String pickQty = genericUtility.getColumnValueFromNode("quantity", currDetail);
//				String mfgDate = genericUtility.getColumnValueFromNode("mfg_date", currDetail);
//				String expDate = genericUtility.getColumnValueFromNode("exp_date", currDetail);
//				//String itemCode = genericUtility.getColumnValue("item_code", dom);
//				System.out.println("pickQty :"+pickQty+"  mfgDate :"+mfgDate+" expDate :"+expDate+" itemCode :"+itemCode);
//				sql = "SELECT ISS_CRITERIA FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
//				System.out.println("sql :"+sql);
//				rset = stmt.executeQuery(sql);
//				if (rset.next())
//				{
//					issCriteria = rset.getString(1);
//				}
//				if (issCriteria == null)
//				{
//					issCriteria = " ";
//				}
//				System.out.println("issCriteria :"+issCriteria);
//				if (!issCriteria.equals("W"))
//				{ 
//					//					if (Double.parseDouble(pickQty) > icQtyOrd)  //CHANGED BY ALKA ON 30/07/07 TO RETRIEVE DATA EVEN IF QUANTITY IS 0
//					if (icQtyOrd != 0 && Double.parseDouble(pickQty) > icQtyOrd)
//					{
//						pickQty = String.valueOf(icQtyOrd);
//					}
//					else if (icQtyOrd == 0)
//					{
//						icQtyOrd = Double.parseDouble(pickQty);
//					}
//				}
//
//				valueXmlString.append("<item_code isSrvCallOnChg='1'>").append("<![CDATA[").append(itemCode == null?"":itemCode).append("]]>").append("</item_code>\r\n");
//				valueXmlString.append("<quantity isSrvCallOnChg='0'>").append("<![CDATA[").append(pickQty).append("]]>").append("</quantity>\r\n");
//				icQtyOrd = icQtyOrd - Double.parseDouble(pickQty);
//				locCode = genericUtility.getColumnValueFromNode("loc_code", currDetail);
//				//String locCodeDet = genericUtility.getColumnValue("loc_code", dom);
//				System.out.println("locCode :"+locCode);
//				/*if (locCodeDet == null || locCodeDet.trim().length() == 0)
//			{
//				valueXmlString.append("<loc_code isSrvCallOnChg='0'>").append("<![CDATA[").append(locCode == null?"":locCode).append("</loc_code>");
//			}*/
//				valueXmlString.append("<loc_code isSrvCallOnChg='1'>").append("<![CDATA[").append(locCode == null?"":locCode).append("]]>").append("</loc_code>\r\n");
//				lotNo = genericUtility.getColumnValueFromNode("lot_no", currDetail);
//				valueXmlString.append("<lot_no isSrvCallOnChg='1'>").append("<![CDATA[").append(lotNo == null?"":lotNo.trim()).append("]]>").append("</lot_no>\r\n");
//				String lotSl = genericUtility.getColumnValueFromNode("lot_sl", currDetail);
//				valueXmlString.append("<lot_sl isSrvCallOnChg='1'>").append("<![CDATA[").append(lotSl == null?"":lotSl.trim()).append("]]>").append("</lot_sl>\r\n");
//				// 17-05-2007 manoharan
//				//valueXmlString.append("<mfg_date isSrvCallOnChg='0'>").append("<![CDATA[").append(mfgDate == null?"":mfgDate).append("]]>").append("</mfg_date>\r\n");
//				if (mfgDate != null && mfgDate.trim().length() > 0)
//				{
//					valueXmlString.append("<mfg_date isSrvCallOnChg='0'>").append("<![CDATA[").append(mfgDate).append("]]>").append("</mfg_date>\r\n");
//				}
//				//valueXmlString.append("<exp_date isSrvCallOnChg='0'>").append("<![CDATA[").append(expDate == null?"":expDate).append("]]>").append("</exp_date>\r\n");
//				if (expDate != null && expDate.trim().length() > 0)
//				{
//					valueXmlString.append("<exp_date isSrvCallOnChg='0'>").append("<![CDATA[").append(expDate).append("]]>").append("</exp_date>\r\n");
//				}
//				// end 17-05-2007 manoharan
//				valueXmlString.append("</Detail>");
//			}// for end
//			valueXmlString.append("</Root>");
//		}// end try
//		catch(ITMException itme)
//		{
//			throw itme;
//		}
//		catch(Exception e)
//		{
//			throw new ITMException(e);
//		}
//		finally
//		{
//			try
//			{
//				conn.close();
//				conn = null;
//			}
//			catch (Exception e){}
//		}
//		System.out.println("valueXmlString return from InvPack  :"+valueXmlString.toString());
//		System.out.println("valueXmlString from Stock:"+valueXmlString.toString());
//		return valueXmlString.toString();
//	}
	//String actionSplitStockTransform button 
	//end of addition 

	private boolean checkIsNumber(String packInst)
	{
		char ch = ' ';
		int len = 0;
		len = packInst.length();
		boolean retBool = true;
		System.out.println("len :"+len);
		for (int i = 0; i < len; i++)
		{
			ch = packInst.charAt(i);
			if (Character.isLetter(ch))
			{
				retBool = false;
				break;
			}
			else
			{
				retBool = true;
			}
		}//end for
		return retBool;
	}

	private String actionPacket(Document dom, Document dom1, String objContext, String xtraParams) throws RemoteException,ITMException
	{
		String tranId = "", sql = "", packInstr = "", packIns = "", lineNoOrd = "", orderNo = "";
		Statement stmt = null;
		ResultSet rs = null;
		Connection conn = null;
		int count = 1;
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		ConnDriver connDriver = new ConnDriver();
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			stmt = conn.createStatement();

			tranId = genericUtility.getColumnValue("tran_id", dom);
			packInstr = genericUtility.getColumnValue("pack_instr", dom); //The column is to be changed to generate Packing Instruction - Jiten -20-12-05 - column to be added in datawindow for (N/Y) option
			lineNoOrd =  genericUtility.getColumnValue("line_no__ord", dom);
			orderNo =  genericUtility.getColumnValue("order_no", dom);

			sql ="SELECT INV_PACK_ISS.TRAN_ID, "
					+"INV_PACK_ISS.LINE_NO, "
					+"INV_PACK_ISS.ORDER_NO, "
					+"INV_PACK_ISS.LINE_NO__ORD, "
					+"INV_PACK_ISS.ITEM_CODE, "
					+"INV_PACK_ISS.LOC_CODE, "
					+"INV_PACK_ISS.LOT_NO, "
					+"INV_PACK_ISS.LOT_SL, "
					+"INV_PACK_ISS.QUANTITY, "
					+"INV_PACK_ISS.UNIT, "
					+"INV_PACK_ISS.PACK_CODE, "
					+"INV_PACK_ISS.PACK_INSTR, "
					+"INV_PACK_ISS.GROSS_WEIGHT, "
					+"INV_PACK_ISS.TARE_WEIGHT, "
					+"INV_PACK_ISS.NET_WEIGHT, "
					+"INV_PACK_ISS.LOC_CODE__EXCESS_SHORT, "
					+"INV_PACK_ISS.EXCESS_SHORT_QTY "
					+"FROM INV_PACK_ISS "
					+"WHERE INV_PACK_ISS.TRAN_ID = '"+tranId+"' ";
			System.out.println("sql :"+sql);
			rs = stmt.executeQuery(sql);
			while (rs.next())
			{
				valueXmlString.append("<Detail>\r\n");
				valueXmlString.append("<order_no>").append("<![CDATA[").append((rs.getString(3) == null) ? "":rs.getString(3)).append("]]>").append("</order_no>\r\n");
				valueXmlString.append("<line_no__ord>").append("<![CDATA[").append(lineNoOrd).append("]]>").append("</line_no__ord>\r\n");
				valueXmlString.append("<item_code>").append("<![CDATA[").append((rs.getString(5) == null) ? "":rs.getString(5)).append("]]>").append("</item_code>\r\n");
				valueXmlString.append("<quantity>").append("<![CDATA[").append((rs.getString(9) == null) ? "":rs.getString(9)).append("]]>").append("</quantity>\r\n");
				valueXmlString.append("<pack_code>").append("<![CDATA[").append((rs.getString(11) == null) ? "":rs.getString(11)).append("]]>").append("</pack_code>\r\n");

				if (packInstr != null && packInstr.trim().equalsIgnoreCase("Y"))// This code need to be change 15/12/05 i.e. the value of packInstr can be changed
				{
					if (count <= 9)
					{
						packIns = "00" + String.valueOf(count);
					}
					else if (count > 9 && count <= 99)
					{
						packIns = "0" + String.valueOf(count);
					}
					else if (count > 99)
					{
						packIns = String.valueOf(count);
					}
					System.out.println("packIns :"+packIns);
					valueXmlString.append("<pack_instr>").append("<![CDATA[").append((packIns == null) ? "":packIns).append("]]>").append("</pack_instr>\r\n");
				}
				else
				{
					valueXmlString.append("<pack_instr>").append("<![CDATA[").append((rs.getString(12) == null) ? "":rs.getString(12)).append("]]>").append("</pack_instr>\r\n");
				}
				valueXmlString.append("<loc_code>").append("<![CDATA[").append((rs.getString(6) == null) ? "":rs.getString(6)).append("]]>").append("</loc_code>\r\n");
				valueXmlString.append("<lot_no>").append("<![CDATA[").append((rs.getString(7) == null) ? "":rs.getString(7)).append("]]>").append("</lot_no>\r\n");
				valueXmlString.append("<gross_weight>").append("<![CDATA[").append(rs.getString(13)).append("]]>").append("</gross_weight>\r\n");
				valueXmlString.append("<tare_weight>").append("<![CDATA[").append(rs.getString(14)).append("]]>").append("</tare_weight>\r\n");
				valueXmlString.append("<net_weight>").append("<![CDATA[").append(rs.getString(15)).append("]]>").append("</net_weight>\r\n");
				//valueXmlString.append("<loc_code__excess_short>").append("<![CDATA[").append(rs.getString(16)).append("]]>").append("</loc_code__excess_short>\r\n");
				//valueXmlString.append("<excess_short_qty>").append("<![CDATA[").append(rs.getString(17)).append("]]>").append("</excess_short_qty>\r\n");
				//valueXmlString.append("<lot_sl>").append("<![CDATA[").append((rs.getString(8) == null) ? "":rs.getString(8)).append("]]>").append("</lot_sl>\r\n");
				//valueXmlString.append("<unit>").append("<![CDATA[").append((rs.getString(10) == null) ? "":rs.getString(10)).append("]]>").append("</unit>\r\n");
				//valueXmlString.append("<line_no>").append("<![CDATA[").append((rs.getString(2) == null) ? "":rs.getString(2)).append("]]>").append("</line_no>\r\n");
				valueXmlString.append("</Detail>\r\n");
				count++;
            }
            
            System.out.println("count :"+count);
            //added by monika salla 4 dec 21 to close dirty connection 
                    rs.close();
					rs= null;
					stmt.close();
					stmt= null;//end
			//stmt.close();
			valueXmlString.append("</Root>\r\n");
		}
		catch (SQLException sqx)
		{
			System.out.println("The SQLException occurs in invPakc for Packet button :"+sqx);
			throw new ITMException(sqx);
		}
		catch (Exception e)
		{
			System.out.println("The Exception occure in InvPakc  for Packet button:"+e);
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				conn.close();
				conn = null;
			}
			catch (Exception e){}
		}
		System.out.println("valueXmlString return from InvPack  :"+valueXmlString.toString());
		return valueXmlString.toString();
	}

	private String packetTransform(Document dom,Document dom1, String objContext, String xtraParams, Document selDataDom)throws ITMException
	{
		System.out.println("packetTransform is calling.............");
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		Node currDetail = null;
		try
		{
			ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
			String lineNoOrd = genericUtility.getColumnValue("line_no__ord", dom);
			String orderNo = genericUtility.getColumnValue("order_no", dom);
			System.out.println("lineNoOrd :"+lineNoOrd+" orderNo :"+orderNo);
			NodeList detailList = selDataDom.getElementsByTagName("Detail");
			int noOfDetails = detailList.getLength();
			System.out.println("noOfDetails-["+noOfDetails+"]-");
			for(int ctr = 0; ctr < noOfDetails; ctr++)
			{
				currDetail = detailList.item(ctr);
				valueXmlString.append("<Detail>");
				valueXmlString.append("<line_no__ord isSrvCallOnChg='0'>").append("<![CDATA[").append(lineNoOrd).append("]]>").append("</line_no__ord>\r\n");
				valueXmlString.append("<order_no isSrvCallOnChg='1'>").append("<![CDATA[").append(orderNo == null?"":orderNo).append("]]>").append("</order_no>\r\n");
				String packCode = genericUtility.getColumnValueFromNode("pack_code", currDetail);
				System.out.println("packCode :"+packCode);
				valueXmlString.append("<pack_code isSrvCallOnChg='1'>").append("<![CDATA[").append(packCode == null ?"":packCode.trim()).append("]]>").append("</pack_code>\r\n");
				String packInstr = genericUtility.getColumnValueFromNode("pack_instr", currDetail);
				System.out.println("packInstr :"+packInstr);
				valueXmlString.append("<pack_instr isSrvCallOnChg='1'>").append("<![CDATA[").append(packInstr == null ?"":packInstr.trim()).append("]]>").append("</pack_instr>\r\n");
				valueXmlString.append("</Detail>");
			}// for end
			valueXmlString.append("</Root>");
		}// end try
		catch(ITMException itme)
		{
			throw itme;
		}
		catch(Exception e)
		{
			throw new ITMException(e);
		}
		System.out.println("valueXmlString from Packet :"+valueXmlString.toString());
		return valueXmlString.toString();
	}

	private String actionDefault2(Document dom, Document dom1, String objContext, String xtraParams) throws RemoteException,ITMException
	{
		String itemCode = "",  siteCode = "", lineNoOrd = "", orderNo = "", lotNo = "", locCode = "";
		String lotSl = "", sql = "", orderType = "", returnString = "", userId = "", refSer = "";
		String detailCnt = "";
		int detCnt = 0, cnt = 0;
		Statement stmt = null;
		ResultSet rs = null;
		Connection conn = null;
		double reqQty = 0d;
		ConnDriver connDriver = new ConnDriver();
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		try
		{
			detailCnt = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "detCnt");
			if (detailCnt != null) {
				detCnt = Integer.parseInt(detailCnt);
				System.out.println("detCnt...........:: " + detCnt);
			}
			if (dom == null || detCnt > 1)
			{
				valueXmlString.append("</Root>\r\n");
				return valueXmlString.toString();
			}
			if (dom != null)
			{
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
				conn.setAutoCommit(false);  //ADDED BY ALKA 20/08/07 TO AVOID STOCK UPDATION COMMIT IN getStockDetails
				connDriver = null;
				stmt = conn.createStatement();
				userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
				siteCode = genericUtility.getColumnValue("site_code",dom1);
				orderType =  genericUtility.getColumnValue("order_type", dom1);
				orderNo =  genericUtility.getColumnValue("order_no", dom1);

				if (orderType.equalsIgnoreCase("D") || orderType.equalsIgnoreCase("S"))
				{
					if (siteCode == null || siteCode.trim().length() == 0)
					{
						returnString = itmDBAccess.getErrorString("","VTSITECD1","",userId, conn);
						return returnString;
					}
					if (orderType.equalsIgnoreCase("S"))
					{
						sql = " SELECT COUNT(1) FROM SORDITEM WHERE SALE_ORDER = '" + orderNo +"' AND LINE_TYPE = 'I'";
						rs = stmt.executeQuery(sql);
						if (rs.next())
						{
							cnt = rs.getInt(1);
						}
                        rs.close(); rs = null;
                        

						if (cnt > 0)
						{
							sql = " SELECT (QUANTITY - QTY_DESP), ITEM_CODE, LINE_NO FROM SORDITEM "
									+ " WHERE SALE_ORDER = '" + orderNo +"' AND LINE_TYPE = 'I'";
							System.out.println("sql :: " + sql);
							rs = stmt.executeQuery(sql);
							while (rs.next())
							{
								reqQty = rs.getDouble(1);
								itemCode = rs.getString(2);
								lineNoOrd = rs.getString(3);

								if (reqQty > 0)
								{
									returnString = getStockDetails(dom, siteCode, orderType, orderNo, lineNoOrd, itemCode, reqQty, lotNo, lotSl, "", locCode, conn);
									valueXmlString.append(returnString);
								}
							}
							rs.close(); rs = null;
							stmt.close(); stmt = null;
						}
					}
					else if (orderType.equalsIgnoreCase("D"))
					{
						sql = " SELECT (QTY_CONFIRM - QTY_SHIPPED), ITEM_CODE, LINE_NO FROM DISTORDER_DET "
								+ " WHERE DIST_ORDER = '" + orderNo +"'	ORDER BY LINE_NO";
						System.out.println("sql :: " + sql);
						rs = stmt.executeQuery(sql);
						while (rs.next())
						{
							reqQty = rs.getDouble(1);
							itemCode = rs.getString(2);
							lineNoOrd = rs.getString(3);
							System.out.println("Passing params to StockDetails *********************");
							System.out.println("reqQty :: [" + reqQty + "] itemCode ::: [" + itemCode + "]");
							System.out.println("lotNo :: [" + lotNo + "] lotSl ::: [" + lotSl + "]");
							if (reqQty > 0)
							{
								returnString = getStockDetails(dom, siteCode, orderType, orderNo, lineNoOrd, itemCode, reqQty, lotNo, lotSl, "", locCode, conn);
								valueXmlString.append(returnString);
							}
						}
						rs.close(); rs = null;
						stmt.close(); stmt = null;
					}
				}
				/*				else if (orderType.equalsIgnoreCase("P"))
				{h
					sql = " SELECT A.QUANTITY, A.ITEM_CODE, A.LOT_NO, A.LOT_SL, C.REF_LINE FROM STOCK A, INVTRACE C "
						+ " WHERE A.ITEM_CODE = C.ITEM_CODE AND A.LOC_CODE = C.LOC_CODE "
						+ " AND A.SITE_CODE = C.SITE_CODE AND A.LOT_NO = C.LOT_NO AND A.LOT_SL = C.LOT_SL "
						+ " AND A.ALLOC_QTY = 0 AND (A.PACK_REF IS NULL  OR LENGTH(TRIM(A.PACK_REF)) = 0 ) " // AND A.PACK_REF IS NULL  added by ms alam on 26/06/07
						+ " AND C.REF_SER = 'P-RCP' "
						+ " AND C.REF_ID = '" + orderNo +"' AND A.SITE_CODE = '" + siteCode +"' ";
					System.out.println("sql :: " + sql);
					rs = stmt.executeQuery(sql);
					while (rs.next())
					{
						reqQty = rs.getDouble(1);
						itemCode = rs.getString(2);
						lotNo = rs.getString(3);
						lotSl = rs.getString(4);
						lineNoOrd = rs.getString(5);
						System.out.println("Passing params to StockDetails *********************");
						System.out.println("reqQty :: [" + reqQty + "] itemCode ::: [" + itemCode + "]");
						System.out.println("lotNo :: [" + lotNo + "] lotSl ::: [" + lotSl + "]");
						if (reqQty > 0)
						{
							returnString = getStockDetails(dom, siteCode, orderType, orderNo, lineNoOrd, itemCode, reqQty, lotNo, lotSl, conn);
							valueXmlString.append(returnString);
						}
					}
					rs.close(); rs = null;
					stmt.close(); stmt = null;
				}
				else if (orderType.equalsIgnoreCase("W"))
				{
					sql = " SELECT A.QUANTITY, A.ITEM_CODE, A.LOT_NO, A.LOT_SL, C.REF_LINE FROM STOCK A, INVTRACE C "
						+ " WHERE A.ITEM_CODE = C.ITEM_CODE AND A.LOC_CODE = C.LOC_CODE "
						+ " AND A.SITE_CODE = C.SITE_CODE AND A.LOT_NO = C.LOT_NO AND A.LOT_SL = C.LOT_SL "
						+ " AND A.ALLOC_QTY = 0 AND (A.PACK_REF IS NULL  OR LENGTH(TRIM(A.PACK_REF)) = 0 ) " // AND A.PACK_REF IS NULL  added by ms alam on 26/06/07
						+ " AND C.REF_SER = 'W-RCP' "
						+ " AND C.REF_ID = '" + orderNo +"' AND A.SITE_CODE = '" + siteCode +"' ";
					//	+ " AND A.PACK_REF IS NULL ";						//Commented By Imteyaz As Per QC Reqirment, Bcoz query is not giving o/p due to this
//					THE ABOVE LINE IS COMMENTED AS THE CONDITION IS ALREADY HANDLED BEFORE.
					System.out.println("sql :: " + sql);
					rs = stmt.executeQuery(sql);
					while (rs.next())
					{
						reqQty = rs.getDouble(1);
						itemCode = rs.getString(2);
						lotNo = rs.getString(3);
						lotSl = rs.getString(4);
						lineNoOrd = rs.getString(5);
						System.out.println("Passing params to StockDetails *********************");
						System.out.println("reqQty :: [" + reqQty + "] itemCode ::: [" + itemCode + "]");
						System.out.println("lotNo :: [" + lotNo + "] lotSl ::: [" + lotSl + "]");

						if (reqQty > 0)
						{
							returnString = getStockDetails(dom, siteCode, orderType, orderNo, lineNoOrd, itemCode, reqQty, lotNo, lotSl, conn);
							valueXmlString.append(returnString);
						}
					}
					rs.close(); rs = null;
					stmt.close(); stmt = null;
				}
				else if (orderType.equalsIgnoreCase("R"))
				{
					sql = " SELECT A.QUANTITY, A.ITEM_CODE, A.LOT_NO, A.LOT_SL, C.REF_LINE FROM STOCK A, INVTRACE C "
						+ " WHERE A.ITEM_CODE = C.ITEM_CODE AND A.LOC_CODE = C.LOC_CODE "
						+ " AND A.SITE_CODE = C.SITE_CODE AND A.LOT_NO = C.LOT_NO AND A.LOT_SL = C.LOT_SL "
						+ " AND A.ALLOC_QTY = 0 AND (A.PACK_REF IS NULL  OR LENGTH(TRIM(A.PACK_REF)) = 0 ) " // AND A.PACK_REF IS NULL  added by ms alam on 26/06/07
						+ " AND C.REF_SER = 'D-RCP' "
						+ " AND C.REF_ID = '" + orderNo +"' AND A.SITE_CODE = '" + siteCode +"' ";

					System.out.println("sql :: " + sql);
					rs = stmt.executeQuery(sql);
					while (rs.next())
					{
						reqQty = rs.getDouble(1);
						itemCode = rs.getString(2);
						lotNo = rs.getString(3);
						lotSl = rs.getString(4);
						lineNoOrd = rs.getString(5);

						System.out.println("Passing params to StockDetails *********************");
						System.out.println("reqQty :: [" + reqQty + "] itemCode ::: [" + itemCode + "]");
						System.out.println("lotNo :: [" + lotNo + "] lotSl ::: [" + lotSl + "]");
						if (reqQty > 0)
						{
							returnString = getStockDetails(dom, siteCode, orderType, orderNo, lineNoOrd, itemCode, reqQty, lotNo, lotSl, conn);
							valueXmlString.append(returnString);
						}
					}
					rs.close(); rs = null;
					stmt.close(); stmt = null;
				}*/

				else if (orderType.equalsIgnoreCase("P") || orderType.equalsIgnoreCase("W") || orderType.equalsIgnoreCase("R"))
				{
					if (orderType.equalsIgnoreCase("P"))
					{
						refSer = "P-RCP";
					}
					else if (orderType.equalsIgnoreCase("W"))
					{
						refSer = "W-RCP";
					}
					else if (orderType.equalsIgnoreCase("R"))
					{
						refSer = "D-RCP";
					}

					/*					sql = " SELECT C.QUANTITY, A.ITEM_CODE, A.LOT_NO, A.LOT_SL, C.REF_LINE, A.LOC_CODE "
						+ " FROM STOCK A, INVTRACE C "
						+ " WHERE A.ITEM_CODE = C.ITEM_CODE AND A.LOC_CODE = C.LOC_CODE "
						+ " AND A.SITE_CODE = C.SITE_CODE AND A.LOT_NO = C.LOT_NO AND A.LOT_SL = C.LOT_SL "
						+ " AND A.ALLOC_QTY = 0 AND (A.PACK_REF IS NULL  OR LENGTH(TRIM(A.PACK_REF)) = 0 ) "
						+ " AND C.REF_SER = '" + refSer + "' AND A.LOT_NO = C.LOT_NO "
						+ " AND C.REF_ID = '" + orderNo +"' AND A.SITE_CODE = '" + siteCode +"' ";*/
					//					QUERY CHANGED BY ALKA 20/08/07 TO RETRIEVE DATA FROM INVTRACE AND THEN CHECKING FROM STOCK.
					sql = " SELECT A.QUANTITY, A.ITEM_CODE, A.LOT_NO, A.LOT_SL, A.REF_LINE, A.LOC_CODE "
							+ " FROM INVTRACE A WHERE A.REF_SER = '" + refSer + "' "
							+ " AND A.REF_ID = '" + orderNo +"' AND A.SITE_CODE = '" + siteCode +"' ";

					System.out.println("sql :: " + sql);
					rs = stmt.executeQuery(sql);
					while (rs.next())
					{
						reqQty = rs.getDouble(1);
						itemCode = rs.getString(2);
						lotNo = rs.getString(3);
						lotSl = rs.getString(4);
						lineNoOrd = rs.getString(5);
						locCode = rs.getString(6);
						System.out.println("Passing params to StockDetails *********************");
						System.out.println("reqQty :: [" + reqQty + "] itemCode ::: [" + itemCode + "] locCode ::: [" + locCode + "]");
						System.out.println("lotNo :: [" + lotNo + "] lotSl ::: [" + lotSl + "] refSer ::: [" + refSer );
						
						if (reqQty > 0)
						{
							returnString = getStockDetails(dom, siteCode, orderType, orderNo, lineNoOrd, itemCode, reqQty, lotNo, lotSl, refSer, locCode, conn);
							//							ADDED BY ALKA 20/08/07 TO HANDLE RETURNED ERROR
							if (returnString.indexOf("Errors") > -1)
							{
								return returnString;
							}
							//							ADDITION ENDED BY ALKA 20/08/07 TO HANDLE RETURNED ERROR
							valueXmlString.append(returnString);
						}
					}
					rs.close(); rs = null;
					stmt.close(); stmt = null;

				}
				valueXmlString.append("</Root>\r\n");
			}
		}
		catch (SQLException sqx)
		{
			System.out.println("The sqlException occure in InvPack :"+sqx);
			throw new ITMException(sqx);
		}
		catch (Exception e)
		{
			System.out.println("The Exception occure in InvPakc :"+e);
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (stmt != null)
				{
					stmt.close();
					stmt = null;
				}
				System.out.println("Closing Connection.....");
				conn.close();
				conn = null;
			}
			catch (Exception e){}
		}
		System.out.println("valueXmlString  **************** " + valueXmlString);
		return valueXmlString.toString();
	}

	private String getStockDetails(Document dom, String siteCode, String orderType, String orderNo, String lineNoOrd, String itemCode, double reqQty, String lotNo, String lotSl, String refSer, String locCode, Connection conn)
	{
		Statement stmt = null;
		PreparedStatement pstmtUpdate = null; //ALKA 17/08/07
		ResultSet rs = null;
		StringBuffer valueXmlString = new StringBuffer();
		double stockQty = 0, grossWeight = 0, tareWeight = 0, netWeight = 0, inputQty = 0, stkAllocQty = 0;
		String sql = "", mfgDateStr = "", expDateStr = "", itemDescr = "", unit = "", errString = "", newMessage="";
		String packCode = "", locDescr = "";
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB(); //ALKA 17/08/07
		Timestamp mfgDate = null, expDate = null;
		int update = 0; //ALKA 17/08/07

		try
		{
			stmt = conn.createStatement();
			if (orderType.equalsIgnoreCase("D") || orderType.equalsIgnoreCase("S"))
			{
				sql = " SELECT A.LOT_NO, A.LOT_SL, A.LOC_CODE, A.QUANTITY - A.ALLOC_QTY, A.GROSS_WEIGHT, A.TARE_WEIGHT, "
						//					+ " A.NET_WEIGHT, B.DESCR, B.UNIT, B.PACK_CODE, A.MFG_DATE, A.EXP_DATE, C.DESCR FROM STOCK A, "  //CHANGED BY ALKA 20/08/07 -- ADDED ALLOC_QTY FIELD
						+ " A.NET_WEIGHT, B.DESCR, B.UNIT, B.PACK_CODE, A.MFG_DATE, A.EXP_DATE, C.DESCR, A.ALLOC_QTY FROM STOCK A, "
						+ " ITEM B, LOCATION C WHERE A.ITEM_CODE = B.ITEM_CODE AND A.LOC_CODE = C.LOC_CODE "
						+ " AND (A.QUANTITY - A.ALLOC_QTY > 0) AND (A.PACK_REF IS NULL  OR LENGTH(TRIM(A.PACK_REF)) = 0 ) " //PACK_REF CONDITION ADDED BY ALKA 20/08/07
						+ " AND A.ITEM_CODE = '" + itemCode +"' AND A.SITE_CODE = '" + siteCode +"' "
						+ " ORDER BY EXP_DATE, RETEST_DATE ";
			}
			else if (orderType.equalsIgnoreCase("P") || orderType.equalsIgnoreCase("W") || orderType.equalsIgnoreCase("R"))
			{
				/*				QUERY CHANGED BY ALKA 02/08/07 TO RETRIEVE THE DATA FROM THE CONCERNED ORDER AND STOCK.
				 * 				sql = " SELECT A.LOT_NO, A.LOT_SL, A.LOC_CODE, A.QUANTITY, A.GROSS_WEIGHT, A.TARE_WEIGHT, "
					+ " A.NET_WEIGHT, B.DESCR, B.UNIT, B.PACK_CODE, A.MFG_DATE, A.EXP_DATE, C.DESCR FROM STOCK A, ITEM B, "
					+ " LOCATION C WHERE A.ITEM_CODE = B.ITEM_CODE AND A.LOC_CODE = C.LOC_CODE "
					+ " AND A.ITEM_CODE = '" + itemCode +"' AND A.SITE_CODE = '" + siteCode +"' "
					+ " AND LOT_NO = '" + lotNo + "' AND LOT_SL = '" + lotSl + "' "; */
				//				QUERY CHANGED BY ALKA 20/08/07 FOR CHECKING IN STOCK THE ITEM
				/*sql = " SELECT A.LOT_NO, A.LOT_SL, A.LOC_CODE, A.QUANTITY, A.GROSS_WEIGHT, A.TARE_WEIGHT, "
					+ " A.NET_WEIGHT, B.DESCR, B.UNIT, B.PACK_CODE, A.MFG_DATE, A.EXP_DATE, D.DESCR "
					+ " FROM STOCK A, ITEM B, INVTRACE C, LOCATION D WHERE A.ITEM_CODE = B.ITEM_CODE "
					+ " AND A.LOC_CODE = D.LOC_CODE AND A.ITEM_CODE = C.ITEM_CODE AND A.LOC_CODE = C.LOC_CODE "
					+ " AND A.SITE_CODE = C.SITE_CODE AND A.LOT_NO = C.LOT_NO AND A.LOT_SL = C.LOT_SL "
					+ " AND A.ITEM_CODE = '" + itemCode +"' AND A.SITE_CODE = '" + siteCode +"' "
					+ " AND A.LOT_NO = '" + lotNo +"' AND A.LOT_SL = '" + lotSl +"' AND A.LOC_CODE = '" + locCode +"' "
					+ " AND C.REF_SER = '" + refSer +"' AND C.REF_ID = '" + orderNo +"' ";*/

				sql = " SELECT A.LOT_NO, A.LOT_SL, A.LOC_CODE, A.QUANTITY - A.ALLOC_QTY, A.GROSS_WEIGHT, A.TARE_WEIGHT, "
						+ " A.NET_WEIGHT, B.DESCR, B.UNIT, B.PACK_CODE, A.MFG_DATE, A.EXP_DATE, C.DESCR, A.ALLOC_QTY "
						+ " FROM STOCK A, ITEM B, LOCATION C WHERE A.ITEM_CODE = B.ITEM_CODE "
						+ " AND (A.QUANTITY - A.ALLOC_QTY > 0) "
						+ " AND A.LOC_CODE = C.LOC_CODE AND A.ITEM_CODE = '" + itemCode +"' "
						+ " AND A.SITE_CODE = '" + siteCode +"' AND A.LOT_NO = '" + lotNo +"' "
						+ " AND A.LOT_SL = '" + lotSl +"' AND A.LOC_CODE = '" + locCode +"' "
						+ " AND (A.PACK_REF IS NULL  OR LENGTH(TRIM(A.PACK_REF)) = 0 ) ";
			}
			System.out.println("sql in Stock Details :: " + sql);
			rs = stmt.executeQuery(sql);
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			while (rs.next())
			{
				lotNo = rs.getString(1);
				lotSl = rs.getString(2);
				locCode = rs.getString(3);
				stockQty = rs.getDouble(4);
				grossWeight = rs.getDouble(5);
				tareWeight = rs.getDouble(6);
				netWeight = rs.getDouble(7);
				itemDescr = rs.getString(8);
				unit = rs.getString(9);
				packCode = rs.getString(10);
				mfgDate = rs.getTimestamp(11);
				if (mfgDate != null)
				{
					mfgDateStr = sdf.format(mfgDate);
					System.out.println("mfgDateStr :"+mfgDateStr);
				}

				expDate = rs.getTimestamp(12);
				if (expDate != null)
				{
					expDateStr = sdf.format(expDate);
					System.out.println("expDateStr :"+expDateStr);
				}
				locDescr = rs.getString(13);
				//ADDED BY ALKA 20/08/07 TO CHECK THE REQUIRED QUANTITY IS AVAILABLE IN STOCK
				stkAllocQty = rs.getDouble(14);

				System.out.println("STOCK ========== stkAllocQty :" + stkAllocQty + "::stockQty ==> :" + stockQty + ":");
				if (stockQty < reqQty)
				{
					errString = itmDBAccess.getErrorString("", "VTSTKINS", "", "", conn);
					newMessage = "STOCK FOR ITEM CODE ::: " + itemCode + " NOT SUFFICIENT IN SITE ::: " + siteCode;
					errString = updateMessage(errString, newMessage);
					return errString;
				}
				//ADDITION BY ALKA 20/08/07 TO CHECK THE REQUIRED QUANTITY IS AVAILABLE IN STOCK

				if (stockQty >= reqQty)
				{
					inputQty = reqQty;
					reqQty = 0;
				}
				else if (stockQty < reqQty)
				{
					inputQty = stockQty;
					reqQty = reqQty - inputQty;
				}
				//ADDED BY ALKA 20/08/07 TO UPDATE THE ALLOC_QTY IN STOCK FOR CALCULATION
				System.out.println("STOCK ========== reqQty :" + reqQty + ":: inputQty :" + inputQty + ":");

				sql = " UPDATE STOCK SET ALLOC_QTY = ? WHERE SITE_CODE = ? AND ITEM_CODE = ? AND  LOC_CODE = ?"
						+ " AND LOT_NO = ? AND LOT_SL = ? ";
				pstmtUpdate = conn.prepareStatement(sql);
				pstmtUpdate.setDouble(1, stkAllocQty + inputQty);
				pstmtUpdate.setString(2, siteCode);
				pstmtUpdate.setString(3, itemCode);
				pstmtUpdate.setString(4, locCode);
				pstmtUpdate.setString(5, lotNo);
				pstmtUpdate.setString(6, lotSl);
				update = pstmtUpdate.executeUpdate();
				System.out.println("Records updated in Stock for alloc_qty :: "+update);
				//ADDITION BY ALKA 20/08/07 TO UPDATE THE ALLOC_QTY IN STOCK FOR CALCULATION
				valueXmlString.append("<Detail>\r\n");
				valueXmlString.append("<order_no isSrvCallOnChg='0'>").append("<![CDATA[").append(orderNo).append("]]>").append("</order_no>\r\n");
				valueXmlString.append("<line_no__ord isSrvCallOnChg='0'>").append("<![CDATA[").append(lineNoOrd).append("]]>").append("</line_no__ord>\r\n");
				valueXmlString.append("<item_code isSrvCallOnChg='0'>").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code>\r\n");
				valueXmlString.append("<item_descr isSrvCallOnChg='0'>").append("<![CDATA[").append(itemDescr).append("]]>").append("</item_descr>\r\n");
				valueXmlString.append("<quantity isSrvCallOnChg='0'>").append("<![CDATA[").append(inputQty).append("]]>").append("</quantity>\r\n");
				valueXmlString.append("<unit isSrvCallOnChg='0'>").append("<![CDATA[").append(unit).append("]]>").append("</unit>\r\n");
				valueXmlString.append("<pack_code isSrvCallOnChg='0'>").append("<![CDATA[").append(packCode==null?"":packCode).append("]]>").append("</pack_code>\r\n");
				valueXmlString.append("<loc_code isSrvCallOnChg='0'>").append("<![CDATA[").append(locCode).append("]]>").append("</loc_code>\r\n");
				valueXmlString.append("<location_descr isSrvCallOnChg='0'>").append("<![CDATA[").append(locDescr).append("]]>").append("</location_descr>\r\n");
				valueXmlString.append("<lot_no isSrvCallOnChg='1'>").append("<![CDATA[").append(lotNo).append("]]>").append("</lot_no>\r\n");
				valueXmlString.append("<lot_sl isSrvCallOnChg='0'>").append("<![CDATA[").append(lotSl).append("]]>").append("</lot_sl>\r\n");
				valueXmlString.append("<gross_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(grossWeight).append("]]>").append("</gross_weight>\r\n");
				valueXmlString.append("<tare_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(tareWeight).append("]]>").append("</tare_weight>\r\n");
				valueXmlString.append("<net_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(netWeight).append("]]>").append("</net_weight>\r\n");
				if (mfgDate != null)
				{
					valueXmlString.append("<mfg_date isSrvCallOnChg='0'>").append("<![CDATA[").append(mfgDateStr).append("]]>").append("</mfg_date>\r\n");
				}
				if (expDate != null)
				{
					valueXmlString.append("<exp_date isSrvCallOnChg='0'>").append("<![CDATA[").append(expDateStr).append("]]>").append("</exp_date>\r\n");
				}
				valueXmlString.append("</Detail>\r\n");
				if (reqQty == 0)
				{
					break;
				}
			}
			rs.close(); rs = null;
            stmt.close(); stmt = null;
            //added by monika salla 4 dec 21 to close dirty connection 
                  
					pstmtUpdate.close();
					pstmtUpdate= null;//end

		}
		catch(Exception e)
		{
			System.out.println("The Exception occure in InvPakc :"+e);
			e.printStackTrace();
			return e.getMessage();
		}
		finally
		{
			try
			{
				if (rs != null)
				{
					rs.close(); rs = null;
				}
				if (stmt != null)
				{
					stmt.close(); stmt = null;
				}
				if (conn != null)
				{
					conn.rollback();
					conn.close(); conn = null;
				}
			}
			catch(Exception e)
			{}
		}
		System.out.println("valueXmlString  **************** " + valueXmlString);
		return valueXmlString.toString();
	}

	private String actionDefault3(Document dom, Document dom1, String objContext, String xtraParams) throws RemoteException,ITMException
	{
		String itemCode = "", locCodeNew = "", itemDescr = "", quantity = "", lineNoOrd = "", orderNo = "", unit = "";
		String packInstr = "", grossWeight = "", tareWeight = "", netWeight = "", lotNo = "", lotSl = "";
		String packCode = "", mfgDate = "", expDate = "", locDescr = "", sql = "";
		String updateFlag = null;
		String noArt = null;
		String detailCnt = "";
		int detCnt = 0;
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		Statement stmt = null;
		ResultSet rs = null;
		Connection conn = null;
		Node currDetail = null;
		ConnDriver connDriver = new ConnDriver();
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		try
		{
			detailCnt = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "detCnt");
			if (detailCnt != null) {
				detCnt = Integer.parseInt(detailCnt);
				System.out.println("detCnt...........:: " + detCnt);
			}
			if (dom == null || detCnt > 1)
			{
				valueXmlString.append("</Root>\r\n");
				return valueXmlString.toString();
			}
			if (dom != null)
			{
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
				connDriver = null;
				stmt = conn.createStatement();

				locCodeNew = genericUtility.getColumnValue("loc_code",dom);
				System.out.println("locCodeNew.... :"+locCodeNew);

				sql = "SELECT DESCR FROM LOCATION WHERE LOC_CODE = '" + locCodeNew + "' ";
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					locDescr = rs.getString(1);
				}
				rs.close(); rs = null;
				stmt.close(); stmt = null;
				System.out.println("locDescr :::: " + locDescr);

				NodeList detailList = dom1.getElementsByTagName("Detail2");
				int noOfDetails = detailList.getLength();
				for(int ctr = 0; ctr < noOfDetails; ctr++)
				{
					currDetail = detailList.item(ctr);
					//changed by Alka on 12/09/07 for deleted records not to be considered - REQUEST ID "DI78GIN042,DI78GIN043" - START				
					updateFlag = getCurrentUpdateFlag(currDetail);

					System.out.println("ctr ::" + ctr + "==== updateFlag :: "+updateFlag);

					if ("D".equalsIgnoreCase(updateFlag))
					{
						continue;
					}
					//changed by Alka on 12/09/07 for deleted records not to be considered - REQUEST ID "DI78GIN042,DI78GIN043" - END
					orderNo = genericUtility.getColumnValueFromNode("order_no", currDetail);
					lineNoOrd = genericUtility.getColumnValueFromNode("line_no__ord", currDetail);
					itemCode = genericUtility.getColumnValueFromNode("item_code", currDetail);
					itemDescr = genericUtility.getColumnValueFromNode("item_descr", currDetail);
					unit = genericUtility.getColumnValueFromNode("unit", currDetail);
					packInstr = genericUtility.getColumnValueFromNode("pack_instr", currDetail);
					quantity = genericUtility.getColumnValueFromNode("quantity", currDetail);
					grossWeight = genericUtility.getColumnValueFromNode("gross_weight", currDetail);
					tareWeight = genericUtility.getColumnValueFromNode("tare_weight", currDetail);
					netWeight = genericUtility.getColumnValueFromNode("net_weight", currDetail);
					lotNo = genericUtility.getColumnValueFromNode("lot_no", currDetail);
					lotSl = genericUtility.getColumnValueFromNode("lot_sl", currDetail);
					packCode = genericUtility.getColumnValueFromNode("pack_code", currDetail);
					mfgDate = genericUtility.getColumnValueFromNode("mfg_date", currDetail);
					expDate = genericUtility.getColumnValueFromNode("exp_date", currDetail);
					// changed by ALKA on 11/09/07 for no_art column added in the inv_pack_iss FOR REQUEST ID "DI78GIN042,DI78GIN043,MF78GIN013"
					noArt = genericUtility.getColumnValueFromNode("no_art", currDetail);
					valueXmlString.append("<Detail>");
					valueXmlString.append("<order_no isSrvCallOnChg='0'>").append("<![CDATA[").append(orderNo).append("]]>").append("</order_no>\r\n");
					valueXmlString.append("<line_no__ord isSrvCallOnChg='0'>").append("<![CDATA[").append(lineNoOrd).append("]]>").append("</line_no__ord>\r\n");
					valueXmlString.append("<item_code isSrvCallOnChg='0'>").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code>\r\n");
					valueXmlString.append("<item_descr isSrvCallOnChg='0'>").append("<![CDATA[").append(itemDescr).append("]]>").append("</item_descr>\r\n");
					valueXmlString.append("<unit isSrvCallOnChg='0'>").append("<![CDATA[").append(unit).append("]]>").append("</unit>\r\n");
					valueXmlString.append("<pack_instr isSrvCallOnChg='0'>").append("<![CDATA[").append(packInstr==null?"":packInstr).append("]]>").append("</pack_instr>\r\n");
					valueXmlString.append("<qty_pack isSrvCallOnChg='0'>").append("<![CDATA[").append(quantity).append("]]>").append("</qty_pack>\r\n");
					// changed by ALKA on 11/09/07 for no_art column added in the inv_pack_iss FOR REQUEST ID "DI78GIN042,DI78GIN043,MF78GIN013"
					//valueXmlString.append("<no_art isSrvCallOnChg='0'>").append("<![CDATA[").append("1").append("]]>").append("</no_art>\r\n");
					valueXmlString.append("<no_art isSrvCallOnChg='0'>").append("<![CDATA[").append(noArt).append("]]>").append("</no_art>\r\n");
					valueXmlString.append("<quantity isSrvCallOnChg='0'>").append("<![CDATA[").append(quantity).append("]]>").append("</quantity>\r\n");
					valueXmlString.append("<grosswt_pack isSrvCallOnChg='0'>").append("<![CDATA[").append(grossWeight==null?"":grossWeight).append("]]>").append("</grosswt_pack>\r\n");
					valueXmlString.append("<tarewt_pack isSrvCallOnChg='0'>").append("<![CDATA[").append(tareWeight==null?"":tareWeight).append("]]>").append("</tarewt_pack>\r\n");
					valueXmlString.append("<gross_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(grossWeight==null?"":grossWeight).append("]]>").append("</gross_weight>\r\n");
					valueXmlString.append("<tare_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(tareWeight==null?"":tareWeight).append("]]>").append("</tare_weight>\r\n");
					valueXmlString.append("<net_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(netWeight==null?"":netWeight).append("]]>").append("</net_weight>\r\n");
					valueXmlString.append("<loc_code isSrvCallOnChg='0'>").append("<![CDATA[").append(locCodeNew).append("]]>").append("</loc_code>\r\n");
					valueXmlString.append("<location_descr isSrvCallOnChg='0'>").append("<![CDATA[").append(locDescr).append("]]>").append("</location_descr>\r\n");
					valueXmlString.append("<lot_no isSrvCallOnChg='0'>").append("<![CDATA[").append(lotNo==null?"":lotNo).append("]]>").append("</lot_no>\r\n");
					valueXmlString.append("<lot_sl isSrvCallOnChg='0'>").append("<![CDATA[").append(lotSl==null?"":lotSl).append("]]>").append("</lot_sl>\r\n");
					if (mfgDate != null)
					{
						valueXmlString.append("<mfg_date isSrvCallOnChg='0'>").append("<![CDATA[").append(mfgDate).append("]]>").append("</mfg_date>\r\n");
					}
					if (expDate != null)
					{
						valueXmlString.append("<exp_date isSrvCallOnChg='0'>").append("<![CDATA[").append(expDate==null?"":expDate).append("]]>").append("</exp_date>\r\n");
					}
					valueXmlString.append("<pack_code isSrvCallOnChg='0'>").append("<![CDATA[").append(packCode==null?"":packCode).append("]]>").append("</pack_code>\r\n");
					valueXmlString.append("</Detail>\r\n");
				}
				valueXmlString.append("</Root>\r\n");
			}
		}
		catch (SQLException sqx)
		{
			System.out.println("The sqlException occur in InvPackAct :"+sqx);
			throw new ITMException(sqx);
		}
		catch (Exception e)
		{
			System.out.println("The Exception occur in InvPackAct :"+e);
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (stmt != null)
				{
					stmt.close();
					stmt = null;
				}
				System.out.println("Closing Connection.....");
				conn.close();
				conn = null;
			}
			catch (Exception e){}
		}
		System.out.println("valueXmlString return from InvPackAct :"+ valueXmlString.toString());
		return valueXmlString.toString();
	}

	private String updateMessage(String resultString,String message)
	{
		StringBuffer stbf = new StringBuffer();
		try{
			System.out.println("resultString : "+resultString);
			stbf.append(resultString.substring(0,resultString.indexOf("<trace>")));
			if(message != null && message.trim().length() > 0){
				stbf.append("<trace>"+message+"</trace>");
				stbf.append(resultString.substring(resultString.indexOf("</trace>")+8));
			}else{
				stbf.append(resultString.substring(resultString.indexOf("<trace>")));
			}
			System.out.println("Resulting String : "+stbf.toString());

		}catch(Exception e){
			System.out.println("Exception in updateMessage : "+e);
			e.printStackTrace();
		}
		return stbf.toString();   
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