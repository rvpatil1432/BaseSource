package ibase.webitm.ejb.dis.adv;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.utility.E12GenericUtility;

//import ibase.system.config.*;
import ibase.utility.CommonConstants;

import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;
import java.text.NumberFormat;//added by nisar on 11/23/2007
import org.w3c.dom.*;
import javax.ejb.*;
//import javax.naming.InitialContext;
import java.io.*;

import java.text.SimpleDateFormat;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;

import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.fin.FinCommon;

import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class DistIssueAct extends ActionHandlerEJB implements DistIssueActLocal, DistIssueActRemote
{
	CommonConstants commonConstants = new CommonConstants();
	//Updated by nisar on 11/23/2007
	//Changed by msalam on 22/09/07 start 
	//to remove itemchange call in actionDefault and bring all the itemchange valued in one query
	boolean isDistOrderValuedSet = false;

	ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
	//Changed by msalam on 22/09/07 end 
	/* public void ejbCreate() throws RemoteException, CreateException 
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
	} */

	public String actionHandler() throws RemoteException,ITMException
	{
		return "";
	}

	public String actionHandler(String actionType, String xmlString, String objContext, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		String retString = "";
		//ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		try
		{
			if(xmlString != null && xmlString.trim().length() > 0)
			{
				dom = genericUtility.parseString(xmlString);			
			}
			if (actionType.equalsIgnoreCase("PackList"))
			{
				retString = actionPackList(dom,objContext,xtraParams);
			}
		}
		catch(Exception se)
		{
			System.out.println("Exception :: ActionHandlerService :: "+se.getMessage());
		}
		return retString;
	}

	public String actionHandler(String actionType, String xmlString, String xmlString1, String objContext, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		String  retString = null;
		//ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();

		try
		{
			if(xmlString != null && xmlString.trim().length() > 0)
			{
				dom = genericUtility.parseString(xmlString); 			
			}
			if(xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom1 = genericUtility.parseString(xmlString1);			
			}
			if (actionType.equalsIgnoreCase("Stock"))
			{
				retString = actionStock(dom,dom1,objContext,xtraParams);
			}
			if (actionType.equalsIgnoreCase("Allocate"))
			{
				retString = actionAllocate(dom,dom1,objContext,xtraParams);
			}
			if (actionType.equalsIgnoreCase("Default"))
			{
				retString = actionDefault(xmlString,xmlString1,objContext,xtraParams);
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :DistIssue :actionHandler(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning String from DistIssue : actionHandler"+retString);
		return retString;
	}

	public String actionHandlerTransform(String actionType, String xmlString, String xmlString1, String objContext, String xtraParams, String selDataStr) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document selDataDom = null;
		String  retString = null;
		try
		{
			System.out.println("xmlString ::"+xmlString);
			System.out.println("xmlString1 ::"+xmlString1);
			System.out.println("selDataStr ::"+selDataStr);

			if(xmlString != null && xmlString.trim().length()!=0)
			{
				dom = genericUtility.parseString(xmlString); 
				dom1 = genericUtility.parseString(xmlString1);
				if(selDataStr != null && selDataStr.length() > 0)
				{
					selDataDom = genericUtility.parseString(selDataStr);
				}
			}
			System.out.println("actionType:"+actionType+":");

			if (actionType.equalsIgnoreCase("Stock"))
			{
				retString = stockTransform(dom, dom1, objContext, xtraParams, selDataDom);
			}
			// 19/03/12 manoharan
			if (actionType.equalsIgnoreCase("PackList"))
			{
				retString = packListTransform(dom, dom1, objContext, xtraParams, selDataDom);
			}
			// end 19/03/12 manoharan
		}
		catch(Exception e)
		{
			System.out.println("Exception :DistIssueActEJB :actionHandlerTransform(String xmlString):" +e);
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning String from DistIssueActEJB : actionHandlerTransform"+retString);
		return retString;
	}

	private String actionStock(Document dom, Document dom1, String objContext, String xtraParams) throws RemoteException, ITMException
	{
		String siteCode = "", quantity = "", locCode = "", itemCode = "";
		String availableYn = "", tracShelfLife = "", mfgDate1 = "";
		String expDate1 = "", lotNo = "", lotSl = "";
		java.sql.Date currDate = null;
		java.sql.Date mfgDate = null;
		java.sql.Date expDate = null;
		String sql = "";
		ResultSet rs = null;
		Connection conn = null;
		//Statement stmt = null;
		PreparedStatement pstmt = null;
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		//ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		//ConnDriver connDriver = new ConnDriver();
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			//stmt = conn.createStatement();
			//String detailDom= genericUtility.serializeDom(dom);	
			//System.out.println("detailDomlist::::"+detailDom);
			
		if(dom != null )
		{	
			itemCode = genericUtility.getColumnValue("item_code",dom);	
			locCode = genericUtility.getColumnValue("loc_code",dom);	
			quantity = genericUtility.getColumnValue("quantity",dom);	
			currDate = new java.sql.Date(System.currentTimeMillis());
			siteCode = genericUtility.getColumnValue("site_code",dom1);	
			availableYn = genericUtility.getColumnValue("available_yn",dom1);	
			if (locCode == null || locCode.equals("") || locCode.trim().length() == 0)
			{
				locCode = "%";					
			}
			else
			{
				locCode = locCode.trim() + "%";
			}
			sql ="SELECT CASE WHEN TRACK_SHELF_LIFE IS NULL THEN 'N' ELSE TRACK_SHELF_LIFE END "
					+"FROM ITEM WHERE ITEM_CODE = ?";	//'"+itemCode+"'";
			//System.out.println("sql :"+sql);
			//rs = stmt.executeQuery(sql);
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, itemCode);
			rs = pstmt.executeQuery();					
			if (rs.next())
			{
				tracShelfLife = rs.getString(1);
			}
			rs.close(); rs = null;
			pstmt.close(); pstmt = null;
			if (tracShelfLife.equals("N"))
			{
				sql = "SELECT STOCK.ITEM_CODE, "   
						+"STOCK.LOT_NO, "   
						+"STOCK.LOT_SL, "   
						+"STOCK.ALLOC_QTY, "   
						+"STOCK.MFG_DATE, "   
						+"STOCK.EXP_DATE, "   
						+"STOCK.QUANTITY, "
						+"STOCK.LOC_CODE, "
						+"STOCK.NO_ART, "
						+"STOCK.RATE, "
						+"STOCK.QUANTITY - STOCK.ALLOC_QTY AS BALANCE_QTY "
						+"FROM STOCK, INVSTAT "  
						+"WHERE STOCK.INV_STAT = INVSTAT.INV_STAT "
						+"AND STOCK.ITEM_CODE = '"+itemCode+"' "
						+"AND STOCK.LOC_CODE LIKE '"+locCode+"%' "
						+"AND STOCK.SITE_CODE = '"+siteCode+"' "
						+"AND STOCK.QUANTITY > 0 "
						+"AND INVSTAT.AVAILABLE = '"+availableYn+"' "
						+"AND INVSTAT.STAT_TYPE = 'M'"; 
			}
			else
			{
				sql = "SELECT STOCK.ITEM_CODE, "
						+"STOCK.LOT_NO, "
						+"STOCK.LOT_SL, "
						+"STOCK.ALLOC_QTY, "
						+"STOCK.MFG_DATE, "   
						+"STOCK.EXP_DATE, "
						+"STOCK.QUANTITY, "
						+"STOCK.LOC_CODE, "
						+"STOCK.NO_ART, "
						+"STOCK.RATE, "
						+"STOCK.QUANTITY - STOCK.ALLOC_QTY AS BALANCE_QTY "
						+"FROM STOCK,INVSTAT "  
						+"WHERE STOCK.INV_STAT = INVSTAT.INV_STAT "
						+"AND STOCK.ITEM_CODE = '"+itemCode+"' "
						+"AND STOCK.LOC_CODE LIKE '"+locCode+"%' "
						+"AND STOCK.SITE_CODE = '"+siteCode+"' "
						+"AND STOCK.QUANTITY > 0 "
						+"AND INVSTAT.AVAILABLE = '"+availableYn+"' "
						+"AND INVSTAT.STAT_TYPE = 'M' "
						+"ORDER BY STOCK.EXP_DATE ASC";
			}
			//System.out.println("sql :"+sql);
			//stmt.close();
			//stmt = null;
			//stmt = conn.createStatement();
			//rs = stmt.executeQuery(sql);			
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				mfgDate1 = "";
				expDate1 = "";
				mfgDate = rs.getDate(5);
				//System.out.println("mfgDate :"+mfgDate);
				expDate = rs.getDate(6);
				//System.out.println("expDate :"+expDate);
				if(mfgDate != null)
				{
					mfgDate1 = sdf.format(mfgDate);
					//System.out.println("mfgDate1 :"+mfgDate1);
				}
				if(expDate != null)
				{
					expDate1 = sdf.format(expDate);
					//System.out.println("expDate1 :"+expDate1);
				}
				// 12/10/09 manoharan to handle empty/null
				if (mfgDate1 == null || "null".equals(mfgDate1))
				{
					mfgDate1 = "";
				}
				if (expDate1 == null || "null".equals(expDate1))
				{
					expDate1 = "";
				}
				if (expDate1 == null || "null".equals(expDate1))
				{
					expDate1 = "";
				}
				lotNo = rs.getString(2);
				if (lotNo == null || "null".equals(lotNo) || lotNo.trim().length() == 0)
				{
					lotNo = "               ";
				}
				lotSl =  rs.getString(3);
				if (lotSl == null || "null".equals(lotSl) || lotSl.trim().length() == 0)
				{
					lotSl = "     ";
				}

				// end 12/10/09 manoharan to handle empty/null
				valueXmlString.append("<Detail>\r\n");
				valueXmlString.append("<item_code>").append("<![CDATA[").append(rs.getString(1).trim()).append("]]>").append("</item_code>\r\n");
				valueXmlString.append("<lot_no>").append("<![CDATA[").append(rs.getString(2).trim()).append("]]>").append("</lot_no>\r\n");
				valueXmlString.append("<lot_sl>").append("<![CDATA[").append(rs.getString(3).trim()).append("]]>").append("</lot_sl>\r\n");
				valueXmlString.append("<alloc_qty>").append("<![CDATA[").append(rs.getDouble(4)).append("]]>").append("</alloc_qty>\r\n");
				valueXmlString.append("<mfg_date>").append("<![CDATA[").append(mfgDate1).append("]]>").append("</mfg_date>\r\n");
				valueXmlString.append("<exp_date>").append("<![CDATA[").append(expDate1).append("]]>").append("</exp_date>\r\n");
				valueXmlString.append("<quantity>").append("<![CDATA[").append(rs.getDouble(7)).append("]]>").append("</quantity>\r\n");
				valueXmlString.append("<balance_qty>").append("<![CDATA[").append(rs.getDouble(11)).append("]]>").append("</balance_qty>\r\n");
				valueXmlString.append("<loc_code>").append("<![CDATA[").append(rs.getString(8).trim()).append("]]>").append("</loc_code>\r\n");
				valueXmlString.append("<no_art>").append("<![CDATA[").append(rs.getInt(9)).append("]]>").append("</no_art>\r\n");
				//Added arun pal 12-oct-2017
				//valueXmlString.append("<rate>").append("<![CDATA[").append(rs.getDouble(10)).append("]]>").append("</rate>\r\n");
				valueXmlString.append("</Detail>\r\n");
			}
			//Add by Ajay on 02/05/18:START
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			//Add by Ajay on 02/05/18:END
			//while end
		}
			valueXmlString.append("</Root>\r\n");		
		}
		catch(Exception e)
		{
			System.out.println("Exception :DistIssueEJB actionStock:" + e.getMessage() + ":");
			e.printStackTrace();
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
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}				
				if(conn != null)
				{
					System.out.println("Closing Connection.....");
					conn.close();
					conn = null;
				}
			}catch(Exception e){}
		}
		System.out.println("valueXmlString.toString() "+valueXmlString.toString());
		return valueXmlString.toString();		
	}

	private String stockTransform(Document dom,Document dom1, String objContext, String xtraParams, Document selDataDom)throws ITMException
	{
		String qtyStr = "0";
		String itemCode = "",siteCode = "",packCode = "",distOrder = "",tranType = "",noArt = "", type = "", unitAlt = "";
		String checkIntegralQty = "",lineNoOrd ="",sql = "",pickQtyStr = "", taxClass = "", taxChap = "", taxEnv = "";
		String locCode = "",lotNo = "",lotSl = "",errCode = "", errString = "", priceList = "", priceListClg = "", sundryCode = "";
		NodeList detailList = null;
		Node currDetail = null;
		int detailListLength = 0,noArt1 = 0, noArt2 = 0;
		Connection conn = null;
		//Statement stmt = null;
		ResultSet rs = null, rs1 = null;
		//ConnDriver connDriver = new ConnDriver();
		double ordQty = 0,pickQty = 0,integralQty = 0, stkQty = 0, remainingQty = 0, rate1 = 0, rateClg = 0, convQtyAlt = 0;
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		//ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();  //ADDED BY ALKA ON 24/07/07
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB(); //ADDED BY ALKA ON 24/07/07
		double shipperSize=0,shipQty=0,noArt11=0,remainder=0, amount = 0, disAmount = 0, disCountPer = 0;
		double integralqty=0, grossWt = 0, tareWt  = 0;

		double noArt12=0,acShipperQty=0,acIntegralQty=0, potencyPerc = 0, grossPer = 0,tarePer = 0, qtyPerArt= 0, netWt = 0 ;
		String tranDate = "", unit = "", siteCodeMfg = "", batchNo = "", grade = "", dimension = "",suppCodeMfg = "",packInstr = "", locationDescr = "", itemDescr= "" ;
		PreparedStatement pstmt = null;
		java.sql.Timestamp expDate = null, mfgDate = null, retestDate = null;
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			//stmt = conn.createStatement();
			if (qtyStr.equals("0"))
			{
				qtyStr = genericUtility.getColumnValue("quantity",dom);
				if (qtyStr == null)
				{
					qtyStr = "0";
				}
				//System.out.println("qtyStr ::"+qtyStr);
			}	
			//lineNoOrd = new  ibase.utility.E12GenericUtility().getColumnValue("line_no_ord",dom);
			tranDate = genericUtility.getColumnValue("tran_date",dom1);
			lineNoOrd = genericUtility.getColumnValue("line_no_dist_order",dom);
			itemCode = genericUtility.getColumnValue("item_code",dom);
			siteCode = genericUtility.getColumnValue("site_code",dom1);
			packCode = genericUtility.getColumnValue("pack_code",dom);
			distOrder = genericUtility.getColumnValue("dist_order",dom);
			tranType = genericUtility.getColumnValue("tran_type",dom1);
			noArt = genericUtility.getColumnValue("no_art",dom);
			ordQty = Double.parseDouble(qtyStr);  //SHIFTED BY ALKA ON 24/07/07 FROM BELOW 
			System.out.println("ordQty ::"+ordQty); //SHIFTED BY ALKA ON 24/07/07 FROM BELOW 

			//			ADDED BY ALKA ON 19/07/07 FOR SELECTED STOCK TO BE ENTERED IN THE DETAIL IF THE QUANTITY IS 0
			detailList = selDataDom.getElementsByTagName("Detail");
			int noOfDetails = detailList.getLength();
			for (int ctr = 0; ctr < noOfDetails; ctr++) {
				currDetail = detailList.item(ctr);
				//				stkQty = stkQty + Double.parseDouble((genericUtility.getColumnValueFromNode("quantity",	currDetail)));
				stkQty = stkQty + Double.parseDouble((genericUtility.getColumnValueFromNode("balance_qty",	currDetail)));
			}
			//System.out.println("stkQty  :" + stkQty);

			if (ordQty != 0 && stkQty < ordQty) //CHANGED BY ALKA 19/07/07 FOR HANDLING NO QUANTITY ENTERED.
			{
				errCode = "VTSTOCK1";
				errString = itmDBAccessEJB.getErrorString("", errCode, "", "", conn);
				conn.close();
				conn = null;
				return errString;
			}

			if (ordQty == 0)
			{
				remainingQty = stkQty;
			}
			else
			{
				remainingQty = ordQty;
			}
			System.out.println("quantity from dom at the start of the forloop :::: " + ordQty);
			System.out.println("remainingQty at the start of the forloop :::: " + remainingQty);
			//			ADDITION ENDED BY ALKA 19/07/07

			if (tranType != null && tranType.trim().length() > 0)
			{
				sql = "SELECT CHECK_INTEGRAL_QTY FROM DISTORDER_TYPE WHERE TRAN_TYPE = ? ";//'"+tranType+"'";
				//rs = stmt.executeQuery(sql);
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranType);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					checkIntegralQty = rs.getString(1);	
				}
				rs.close(); rs = null;
				pstmt.close(); pstmt = null;
				if (checkIntegralQty == null || checkIntegralQty.equals(""))
				{
					checkIntegralQty = "Y";
				}
			}
			detailList = selDataDom.getElementsByTagName("Detail");
			detailListLength = detailList.getLength(); 
			System.out.println("DetailLength ::"+detailListLength);
			//			ordQty = Double.parseDouble(qtyStr);
			//			System.out.println("ordQty ::"+ordQty);
			for (int ctr = 0;ctr < detailListLength  && remainingQty > 0; ctr++)
			{
				//				if (ordQty <= 0)
				//				{
				//					break;
				//				}
				currDetail = detailList.item(ctr);
				pickQtyStr = genericUtility.getColumnValueFromNode("balance_qty",currDetail);
				locCode = checkNull(genericUtility.getColumnValueFromNode("loc_code",currDetail));//ADDED BY PRIYANKA DAS FOR CHECKNULL CONDITION
				lotNo = checkNull(genericUtility.getColumnValueFromNode("lot_no",currDetail));
				lotSl = checkNull(genericUtility.getColumnValueFromNode("lot_sl",currDetail));
				pickQty = Double.parseDouble(pickQtyStr);
				if (pickQty > remainingQty)
				{
					pickQty = remainingQty;
				}
				System.out.println("pickQty ::"+pickQty+"remainingQty ::"+remainingQty);
				if (pickQty == 0) 
				{	
					continue;
				}
				sql = "SELECT  A.EXP_DATE, A.UNIT,  a.GROSS_WT_PER_ART, A.TARE_WT_PER_ART, A.QTY_PER_ART,  "
					+" A.SITE_CODE__MFG, A.MFG_DATE, A.POTENCY_PERC, A.PACK_CODE, A.BATCH_NO, A.GRADE , "
					+" A.GROSS_WEIGHT, A.TARE_WEIGHT, A.NET_WEIGHT, A.DIMENSION, A.RETEST_DATE , "
					+" A.SUPP_CODE__MFG, A.PACK_INSTR,A.RATE,C.DESCR LOCATION_DESCR, D.DESCR ITEM_DESCR "
					+" FROM STOCK A, INVSTAT B, LOCATION C , ITEM D"
					+" WHERE A.INV_STAT = B.INV_STAT "
					+" AND A.LOC_CODE = C.LOC_CODE "
					+" AND D.ITEM_CODE = A.ITEM_CODE "
					+" AND A.ITEM_CODE = ? "
					+" AND A.SITE_CODE = ? "
					+" AND A.LOC_CODE  = ?  "
					+" AND A.LOT_NO = ? "
					+" AND A.LOT_SL = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,itemCode);
				pstmt.setString(2,siteCode);
				pstmt.setString(3,locCode);
				pstmt.setString(4,lotNo);
				pstmt.setString(5,lotSl);
				rs = pstmt.executeQuery();
				if ( rs.next() )
				{
					expDate =  rs.getTimestamp("EXP_DATE");
					unit =  rs.getString("UNIT");
					siteCodeMfg =  rs.getString("SITE_CODE__MFG");
					mfgDate =  rs.getTimestamp("MFG_DATE");
					potencyPerc = rs.getDouble("POTENCY_PERC");
					packCode =  rs.getString("PACK_CODE"); 
					batchNo =  rs.getString("BATCH_NO");
					grade =  rs.getString("GRADE");
					dimension =  rs.getString("DIMENSION");
					retestDate =  rs.getTimestamp("RETEST_DATE");
					suppCodeMfg =  rs.getString("SUPP_CODE__MFG");
					packInstr =  rs.getString("PACK_INSTR");
					locationDescr =  rs.getString("LOCATION_DESCR");
					itemDescr =  rs.getString("ITEM_DESCR");
					grossPer = rs.getDouble("GROSS_WT_PER_ART");
					tarePer = rs.getDouble("TARE_WT_PER_ART");
					qtyPerArt = rs.getDouble("QTY_PER_ART");
					grossWt = pickQty * grossPer;
					tareWt  = pickQty * tarePer;
					netWt = grossWt - tareWt;
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;
				System.out.println("checkIntegralQty ::"+checkIntegralQty);
				if (!checkIntegralQty.equalsIgnoreCase("N"))//added by jiten  - 04/10/06
				{
					integralQty = getIntegralQty(siteCode,itemCode,lotNo,packCode,checkIntegralQty);
					if (integralQty <= 0)
					{
						errCode = "VINTGRLQTY";
						break;
					}
					pickQty = pickQty - (pickQty % integralQty);
				}
				System.out.println("pickQty ::"+pickQty);
				if (pickQty == 0) 
				{	
					continue;
				}

				sql = "SELECT  PRICE_LIST, PRICE_LIST__CLG, SUNDRY_CODE FROM DISTORDER "
					+" WHERE DIST_ORDER = ? ";
					
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,distOrder);
				rs = pstmt.executeQuery();
				if ( rs.next() )
				{
					priceList =  rs.getString("PRICE_LIST");
					priceListClg =  rs.getString("PRICE_LIST__CLG");
					sundryCode =   rs.getString("SUNDRY_CODE");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;				
				
				sql ="select (case when shipper_size is null then 0 else shipper_size end) shipper_size"
					+" from item_lot_packsize where item_code = ? "
					+" and  ? >= lot_no__from "
					+" and  ? <= lot_no__to ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,itemCode);
				pstmt.setString(2,lotNo);
				pstmt.setString(3,lotNo);
				rs1 = pstmt.executeQuery();
				if (rs1.next())
				{
					shipperSize = rs1.getDouble(1);
				}
				rs1.close();
				rs1 = null;
				pstmt.close();
				pstmt = null;				

				if( shipperSize > 0)
				{
					shipQty = shipperSize;
					noArt11 = (pickQty - (pickQty % shipQty))/shipQty;
					remainder = pickQty % shipQty;

					sql ="select ( case when integral_qty is null then 0 else integral_qty end) integral_qty"
							+" from customeritem where cust_code = ? and item_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,sundryCode);
					pstmt.setString(2,itemCode);
					rs1 = pstmt.executeQuery();
					if (rs1.next())
					{
							integralqty = rs1.getDouble(1);
					}
					rs1.close();
					rs1 = null;
					pstmt.close();
					pstmt = null;					
					//System.out.println("integralqty .............:"+integralqty);
					if(integralqty ==0)
					{

						sql ="select  ( case when integral_qty is null then 0 else integral_qty end) integral_qty"
							+" from siteitem where site_code = ? and item_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,siteCode);
						pstmt.setString(2,itemCode);
						rs1 = pstmt.executeQuery();
						if (rs1.next())
						{
							integralqty = rs1.getDouble(1);
						}
						rs1.close();
						rs1 = null;
						pstmt.close();
						pstmt = null;						

						if(integralqty ==0)
						{
							sql ="select ( case when integral_qty is null then 0 else integral_qty end) integral_qty"
								+" from item where item_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,itemCode);
							rs1 = pstmt.executeQuery();
							if (rs1.next())
							{
								integralqty = rs1.getDouble(1);
							}
							rs1.close();
							rs1 = null;
							pstmt.close();
							pstmt = null;							
						}

					} 
					if(integralqty > 0)
					{
						noArt12 = (remainder -(remainder % integralqty))/integralqty;
					}
					if(noArt12 > 0)
					{
						noArt12 = 1;
					}
					noArt1			= (int)(noArt11 + noArt12);
					noArt = "" + noArt1;
					acShipperQty	= shipQty;
					acIntegralQty	= integralqty;
				}
				if(shipperSize ==0)
				{
					noArt1 = getNoArt(siteCode, sundryCode, itemCode, packCode, pickQty, 'B', acShipperQty, acIntegralQty);
					noArt = "" + noArt1;
				}
				
				sql = "SELECT  TAX_CLASS, TAX_CHAP, TAX_ENV, DISCOUNT,UNIT__ALT, CONV__QTY__ALT, RATE, RATE__CLG FROM DISTORDER_DET "
					+" WHERE DIST_ORDER = ? "
					+" AND LINE_NO =  ? ";
          					
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,distOrder);
				pstmt.setInt(2,Integer.parseInt(lineNoOrd));
				rs = pstmt.executeQuery();
				if ( rs.next() )
				{
					taxClass =  rs.getString("TAX_CLASS");
					taxChap =  rs.getString("TAX_CHAP");
					taxEnv =  rs.getString("TAX_ENV");
					unitAlt =  rs.getString("UNIT__ALT");
					convQtyAlt = rs.getDouble("CONV__QTY__ALT");
					rate1 = rs.getDouble("RATE");
					rateClg = rs.getDouble("RATE__CLG");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;				
				if (rate1 == 0)
				{
					type = Character.toString( priceListType(priceList) );
					if("I".equalsIgnoreCase(type))
					{
						rate1 = pickRate(priceList, tranDate, itemCode, siteCode + "\t"+locCode+"\t"+lotNo+"\t"+lotSl,'I');  // D	
					}
					else
					{
						rate1 = pickRate(priceList, tranDate, itemCode, lotNo,'D');
					}
				}
				if (rateClg == 0)
				{
					type = Character.toString( priceListType(priceListClg) );
					if("I".equalsIgnoreCase(type))
					{
						rateClg = pickRate(priceListClg, tranDate, itemCode, siteCode + "\t"+locCode+"\t"+lotNo+"\t"+lotSl,'I');  // D	
					}
					else
					{
						rateClg = pickRate(priceListClg, tranDate, itemCode, lotNo,'D');
					}
				}
				amount = rate1 * pickQty;
				disAmount = (amount * disCountPer / 100);



				valueXmlString.append("<Detail>");
				//valueXmlString.append("<line_no_ord isSrvCallOnChg='1'>").append(lineNoOrd).append("</line_no_ord>");
				// 12/10/09 manoharan to take care of empty/null
				if (lotNo == null || "null".equals(lotNo) || lotNo.trim().length() == 0)
				{
					lotNo = "               ";
				}
				if (lotSl == null || "null".equals(lotSl) || lotSl.trim().length() == 0)
				{
					lotSl = "     ";
				}
				// end 12/10/09 manoharan to take care of empty/null
				valueXmlString.append("<line_no_dist_order isSrvCallOnChg='0'>").append("<![CDATA[").append(lineNoOrd).append("]]>").append("</line_no_dist_order>");
				valueXmlString.append("<quantity isSrvCallOnChg='0'>").append("<![CDATA[").append(pickQty).append("]]>").append("</quantity>");
				valueXmlString.append("<loc_code isSrvCallOnChg='0'>").append("<![CDATA[").append(locCode).append("]]>").append("</loc_code>");
				valueXmlString.append("<lot_no isSrvCallOnChg='0'>").append("<![CDATA[").append(lotNo).append("]]>").append("</lot_no>");
				valueXmlString.append("<lot_sl isSrvCallOnChg='0'>").append("<![CDATA[").append(lotSl).append("]]>").append("</lot_sl>");
				/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				valueXmlString.append("<item_code isSrvCallOnChg='0'>").append("<![CDATA[").append(itemCode == null ? "" : itemCode).append("]]>").append("</item_code>\r\n");
				valueXmlString.append("<item_descr isSrvCallOnChg='0'>").append("<![CDATA[").append(itemDescr==null ? "" : itemDescr).append("]]>").append("</item_descr>\r\n");
				valueXmlString.append("<location_descr isSrvCallOnChg='0'>").append("<![CDATA[").append(locationDescr).append("]]>").append("</location_descr>\r\n");
				valueXmlString.append("<unit isSrvCallOnChg='0'>").append("<![CDATA[").append(unit).append("]]>").append("</unit>\r\n");
				valueXmlString.append("<unit__alt isSrvCallOnChg='0'>").append("<![CDATA[").append(unit).append("]]>").append("</unit__alt>\r\n");
				valueXmlString.append("<conv__qty__alt isSrvCallOnChg='0'>").append("<![CDATA[").append(1).append("]]>").append("</conv__qty__alt>\r\n");
				valueXmlString.append("<qty_order__alt isSrvCallOnChg='0'>").append("<![CDATA[").append(pickQty).append("]]>").append("</qty_order__alt>\r\n");
				valueXmlString.append("<rate isSrvCallOnChg='0'>").append("<![CDATA[").append(rate1).append("]]>").append("</rate>\r\n");
				valueXmlString.append("<rate__clg isSrvCallOnChg='0'>").append("<![CDATA[").append(rateClg).append("]]>").append("</rate__clg>\r\n"); 
				valueXmlString.append("<rate__alt isSrvCallOnChg='0'>").append("<![CDATA[").append(rate1).append("]]>").append("</rate__alt>\r\n");
				valueXmlString.append("<conv__rate_alt isSrvCallOnChg='0'>").append("<![CDATA[").append(1).append("]]>").append("</conv__rate_alt>\r\n");
         
              
         
				valueXmlString.append("<amount isSrvCallOnChg='0'>").append("<![CDATA[").append(amount).append("]]>").append("</amount>\r\n");						

				valueXmlString.append("<pack_code isSrvCallOnChg='0'>").append("<![CDATA[").append(packCode).append("]]>").append("</pack_code>\r\n");
				valueXmlString.append("<disc_amt isSrvCallOnChg='0'>").append("<![CDATA[").append(disAmount).append("]]>").append("</disc_amt>\r\n");
				valueXmlString.append("<tax_class isSrvCallOnChg='0'>").append("<![CDATA[").append( ( taxClass == null ? "": taxClass ) ).append("]]>").append("</tax_class>\r\n");
				valueXmlString.append("<tax_chap isSrvCallOnChg='0'>").append("<![CDATA[").append( ( taxChap == null ? "": taxChap ) ).append("]]>").append("</tax_chap>\r\n");
				valueXmlString.append("<tax_env isSrvCallOnChg='0'>").append("<![CDATA[").append( ( taxEnv == null ? "": taxEnv ) ).append("]]>").append("</tax_env>\r\n");
				grossWt = Double.parseDouble(getFormatedValue(grossWt,3));
				valueXmlString.append("<gross_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(grossWt).append("]]>").append("</gross_weight>\r\n");
				netWt = Double.parseDouble(getFormatedValue(netWt,3));
				valueXmlString.append("<net_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(netWt).append("]]>").append("</net_weight>\r\n");
				tareWt = Double.parseDouble(getFormatedValue(netWt,3));
				valueXmlString.append("<tare_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(tareWt).append("]]>").append("</tare_weight>\r\n");
				valueXmlString.append("<pack_instr isSrvCallOnChg='0'>").append("<![CDATA[").append((packInstr == null) ? "":packInstr).append("]]>").append("</pack_instr>\r\n");
				valueXmlString.append("<retest_date isSrvCallOnChg='0'>").append("<![CDATA[").append((retestDate == null) ? "":sdf.format(retestDate) ).append("]]>").append("</retest_date>\r\n");
				valueXmlString.append("<dimension isSrvCallOnChg='0'>").append("<![CDATA[").append((dimension == null) ? "":dimension).append("]]>").append("</dimension>\r\n");
				valueXmlString.append("<supp_code__mfg isSrvCallOnChg='0'>").append("<![CDATA[").append((suppCodeMfg == null) ? "":suppCodeMfg).append("]]>").append("</supp_code__mfg>\r\n");
				valueXmlString.append("<site_code__mfg isSrvCallOnChg='0'>").append("<![CDATA[").append((siteCodeMfg == null) ? "":siteCodeMfg).append("]]>").append("</site_code__mfg>\r\n");
				valueXmlString.append("<mfg_date isSrvCallOnChg='0'>").append("<![CDATA[").append((mfgDate == null) ? "":sdf.format(mfgDate)).append("]]>").append("</mfg_date>\r\n");
				valueXmlString.append("<exp_date isSrvCallOnChg='0'>").append("<![CDATA[").append((expDate == null) ? "":sdf.format(expDate)).append("]]>").append("</exp_date>\r\n");
				valueXmlString.append("<potency_perc isSrvCallOnChg='0'>").append("<![CDATA[").append(potencyPerc).append("]]>").append("</potency_perc>\r\n");
				valueXmlString.append("<no_art isSrvCallOnChg='0'>").append("<![CDATA[").append(noArt).append("]]>").append("</no_art>\r\n");
				valueXmlString.append("<batch_no isSrvCallOnChg='0'>").append("<![CDATA[").append((batchNo == null) ? "":batchNo).append("]]>").append("</batch_no>\r\n");
				valueXmlString.append("<grade isSrvCallOnChg='0'>").append("<![CDATA[").append( ( (grade == null) ? "": grade ) ).append("]]>").append("</grade>\r\n");
				/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				//				ordQty = ordQty - pickQty;			
				remainingQty = remainingQty - pickQty;  //ADDED BY ALKA ON 03/08/07 FOR THE VARIABLE CHANGED FOR REMAINING QTY -- FOR DETAIL TO BE UPDATED EVEN IF NO QTY ENTERED. 
				valueXmlString.append("</Detail>");
			}
			valueXmlString.append("</Root>");
		}
		catch (Exception e)
		{
			System.out.println("Exception :: "+e);
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(rs != null) {
					rs.close();	rs = null;
				}
				if(rs1 != null) {
					rs1.close(); rs1 = null;
				}
				if(pstmt != null){
					pstmt.close(); pstmt = null;
				}
				
				if(conn != null)
				{
					conn.close();
					conn = null;
				}				
			}
			catch( Exception e )
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return valueXmlString.toString();
	}
	private String actionPackList(Document dom1, String objContext, String xtraParams) throws RemoteException, ITMException
	{
		String errCode = "";
		String errString = "";
		String distOrder = "";
		String tranId = "";
		String siteCode = "";
		String sql = "";
		String detailCnt = "0";
		ResultSet rs = null;
		Connection conn = null;
		//Statement stmt = null;
		PreparedStatement pstmt = null, pstmt1 = null;
		String distIssue = "";
		String sql1 = "", sql2 = "", sql3 = "";
		ResultSet rs1 = null, rs2 = null, rs3 = null;
		//Statement stmt1 = null, stmt2 = null, stmt3 = null;
		String taxClass = "", taxChap = "", taxEnv = "";
		String priceList = "", siteCodeMfg = "";
		String mfgDate1 = "", expDate1 = "", retestDate1 = ""; 	 
		java.sql.Date mfgDate = null;
		java.sql.Date expDate = null;
		double potencyPerc = 0;
		int cnt = 0;
		String batchNo = "";
		String	grade = "", orderNo = "", tranIds = "", tranIdTemp = "";
		java.sql.Date retestDate = null;
		double rate = 0;
		//double grossWeight1 = 0, tareWeight1 = 0, netWeight1 = 0;
		double taxAmt = 0, amount = 0, netAmt = 0;
		java.sql.Timestamp tranDate = null;
		boolean flag = true;
		StringBuffer valueXmlString = new StringBuffer("<?xml version='1.0' encoding='"+commonConstants.ENCODING+"'?> \r\n<Root>\r\n");
		//ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		//ConnDriver connDriver = new ConnDriver();
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		/*		String grossWeight = "";
		String tareWeight = "";
		String netWeight = "";
		 */
		int count = 0;

		try
		{
			//detailCnt = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"detCnt");
			if(detailCnt.equals("0"))
			{
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
				SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
				//stmt = conn.createStatement();
				distOrder = genericUtility.getColumnValue("dist_order",dom1);	
				// 13/08/14 manoharan arguments changed from header dom to details dom, so site_code to be taken as login site
				siteCode =  genericUtility.getColumnValue("site_code",dom1);
				if (siteCode == null || "null".equals(siteCode) || siteCode.trim().length() == 0 )
				{
					siteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
				}
				// 13/08/14 manoharan arguments changed from header dom to details dom, so site_code to be taken as login site
				System.out.println("13/08/14 distOrder ["+distOrder + "] siteCode [" + siteCode + "] dom1 [" + genericUtility.serializeDom(dom1).toString() + "]");
				/*Commented for cross update need to entry in obj_forms
				grossWeight = genericUtility.getColumnValue("gross_weight",dom1);	
				tareWeight = genericUtility.getColumnValue("tare_weight",dom1);	
				netWeight = genericUtility.getColumnValue("net_weight",dom1);	
				 */
				sql = "SELECT COUNT(*) FROM INV_PACK WHERE ORDER_NO = ? AND ORDER_TYPE = 'D' and site_code = ? ";
				//System.out.println("sql :"+sql);
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,distOrder);
				pstmt.setString(2,siteCode);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					count = rs.getInt(1);
					System.out.println("Count :"+count);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if (count == 0)
				{
					errCode = "VTCNTNLL";
					errString = itmDBAccessEJB.getErrorString("",errCode,"","",conn);
					System.out.println("errString :"+errString);
					return errString;					
				}
				/*else if (count == 1)
				{
					sql = "SELECT TRAN_ID,SITE_CODE FROM INV_PACK WHERE ORDER_NO = '"+distOrder+"' "
						  +" AND ORDER_TYPE = 'D'";
					System.out.println("sql :"+sql);
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						tranId = rs.getString(1);
						siteCode = rs.getString(2);
					}
				}*/
				else if (count >= 1)//To be asked to Piyush Sir as it opens window for selecting a specific record. 
				{
					//sql = "SELECT TRAN_ID, TRAN_DATE, ORDER_NO, SITE_CODE FROM INV_PACK WHERE ORDER_NO = ? "; 

					sql = " SELECT inv_pack.TRAN_ID,inv_pack.TRAN_DATE,inv_pack.ORDER_NO,inv_pack.SITE_CODE FROM INV_PACK inv_pack  WHERE inv_pack.ORDER_NO = ? and inv_pack.site_code = ? ";  // modify by cpatil adding site_code joint on 5/11/12
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,distOrder);
					pstmt.setString(2,siteCode);
					rs = pstmt.executeQuery();
					while (rs.next())
					{
						tranId = rs.getString("TRAN_ID");  // to be ask to piyush sir about multiple records selection
						tranDate = rs.getTimestamp("TRAN_DATE");
						orderNo = rs.getString("ORDER_NO");
						siteCode = rs.getString("SITE_CODE");
						valueXmlString.append("<Detail>\r\n");
						valueXmlString.append("<tran_id>").append("<![CDATA[").append(tranId).append("]]>").append("</tran_id>\r\n");
						valueXmlString.append("<tran_date>").append("<![CDATA[").append(sdf.format(tranDate)).append("]]>").append("</tran_date>\r\n");
						valueXmlString.append("<order_no>").append("<![CDATA[").append(orderNo).append("]]>").append("</order_no>\r\n");
						valueXmlString.append("<site_code>").append("<![CDATA[").append(siteCode).append("]]>").append("</site_code>\r\n");
						valueXmlString.append("</Detail>\r\n");

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("</Root>\r\n");
				}
			}//end if(detailCnt)
		}//try end
		catch(Exception e)
		{
			System.out.println("Exception :DistIssueEJB actionPackList:" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(rs != null) {
					rs.close();	rs = null;
				}			
				if(pstmt != null){
					pstmt.close(); pstmt = null;
				}
				
				if(conn != null)
				{
					System.out.println("Closing Connection.....");
					conn.close();
					conn = null;
				}												
				//conn.close();
			}catch(Exception e){}
		}
		System.out.println("valueXmlString.toString() "+valueXmlString.toString());
		return valueXmlString.toString();		
	}
	private String packListTransform(Document dom,Document dom1, String objContext, String xtraParams, Document selDataDom)throws ITMException
	{
		String qtyStr = "0";
		String itemCode = "",siteCode = "",packCode = "",distOrder = "",tranType = "",noArt = "";
		String checkIntegralQty = "",lineNoOrd ="",sql = "",pickQtyStr = "", batchNo = "", grade = "", mfgDate1 = "";
		String locCode = "",lotNo = "",lotSl = "",errCode = "", errString = "", expDate1 = null, retestDate1 = "";
		String dimension = "",descr = "",dimension1 = "",discountPer = "";;
		String listType = "";
		NodeList detailList = null;
		Node currDetail = null;
		int detailListLength = 0, cnt = 0; 
		int counter = 0;//added by kunal on 24/04/13 ;
		Connection conn = null;
		PreparedStatement pstmt = null, pstmt1 = null;
		ResultSet rs = null, rs1 = null;
		//ConnDriver connDriver = new ConnDriver();
		double ordQty = 0,pickQty = 0,integralQty = 0, stkQty = 0, remainingQty = 0, rate = 0, amount = 0, netAmt = 0, taxAmt = 0, potencyPerc = 0;
		double palletWt = 0,palletWt1 = 0  ;//added by kunal on 06/02/12
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB(); 
		ArrayList tranIdArrList = new ArrayList();
		String tranIds = "", tranIdTemp = "", tranId = "", taxClass = "", taxChap  = "", taxEnv   = "", priceList = "", tranDate = "", siteCodeMfg = "";
		java.util.Date mfgDate = null, expDate = null, retestDate = null;
		int tranIdListSize = 0, noOfDetails = 0;
		boolean flag = false;
		//
		String unitAlt="";
		String sql2="";
		PreparedStatement pstmt2= null;
		ResultSet rs2 = null;
		String priceListClg="",itemCode_1="",lotNo_1="",lotSl_1="",lineNoOrd_1="",unit_1="",locCode_1="";
		double rateClg=0;
		double qty=0;
		DistCommon distcommon= new DistCommon();

		ArrayList convAr = null;
		double fact = 0, qtOrderAlt=0;
		String type="";

		//
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 

			lineNoOrd = genericUtility.getColumnValue("line_no_dist_order",dom);
			itemCode = genericUtility.getColumnValue("item_code",dom);
			siteCode = genericUtility.getColumnValue("site_code",dom1);
			packCode = genericUtility.getColumnValue("pack_code",dom);
			distOrder = genericUtility.getColumnValue("dist_order",dom);
			tranType = genericUtility.getColumnValue("tran_type",dom1);
			tranDate = genericUtility.getColumnValue("tran_date",dom1);	

			detailList = selDataDom.getElementsByTagName("Detail");
			noOfDetails = detailList.getLength();
			for (int ctr = 0; ctr < noOfDetails; ctr++) {
				currDetail = detailList.item(ctr);
				tranId = genericUtility.getColumnValueFromNode("tran_id",	currDetail);
				tranIdArrList.add(tranId);
			}
			tranIdListSize = tranIdArrList.size();
			tranIdTemp = "";
			for(int ctr = 0;ctr < tranIdListSize;ctr++){
				tranIdTemp = tranIdArrList.get(ctr).toString();
				tranIds	= tranIds + "'".concat(tranIdTemp).concat("',");
			}

			System.out.println("tranIds :: "+tranIds);

			if(tranIds != null && tranIds.indexOf(",") != -1){
				tranIds = tranIds.substring(0,tranIds.length()-1);
			}else{
				tranIds = "''";
			}

			sql = "SELECT i.LINE_NO__ORD, i.ITEM_CODE, i.LOT_NO, i.LOT_SL, i.QUANTITY,i.UNIT, i.PACK_CODE, "
					+"i.PACK_INSTR,i.LOC_CODE,i.GROSS_WEIGHT, i.TARE_WEIGHT, i.NET_WEIGHT, i.DIMENSION, i.NO_ART ,i.PALLET_WT  "
					+"FROM INV_PACK_RCP  i, inv_pack h "
					+"WHERE h.tran_id = i.tran_id "
					+ " and i.TRAN_ID in (" + tranIds + " ) "
					+ " AND (select s.quantity - case when s.alloc_qty is null then 0 else s.alloc_qty end from stock s where s.item_code = i.item_code and S.site_code = h.site_code " 
					+ " and i.loc_code = s.loc_code and i.lot_no = s.lot_no and i.lot_sl = s.lot_sl ) >= i.QUANTITY "
					+ " ORDER BY i.LINE_NO__ORD";
			pstmt = conn.prepareStatement(sql);
			rs =  pstmt.executeQuery();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			while (rs.next())
			{
				valueXmlString.append("<Detail>\r\n");
				//change done by kunal on 24/01/13 set isSrvCallOnChg='0' 
				valueXmlString.append("<dist_order isSrvCallOnChg='0' >").append("<![CDATA[").append(distOrder.trim()).append("]]>").append("</dist_order>\r\n");
				valueXmlString.append("<line_no_dist_order isSrvCallOnChg='0'>").append("<![CDATA[").append(rs.getString(1).trim()).append("]]>").append("</line_no_dist_order>\r\n");

				sql = "SELECT TAX_CLASS, TAX_CHAP, TAX_ENV, CASE WHEN RATE IS NULL THEN 0 ELSE RATE END, UNIT__ALT,DISCOUNT "
						+"FROM DISTORDER_DET WHERE DIST_ORDER = ? AND LINE_NO = ? ";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1,distOrder) ;
				pstmt1.setString(2,rs.getString(1)) ;
				rs1 = pstmt1.executeQuery();
				if (rs1.next())
				{
					taxClass = rs1.getString(1);
					taxChap  = rs1.getString(2);
					taxEnv   = rs1.getString(3);
					rate     = rs1.getDouble(4);
					unitAlt   = rs1.getString(5);
					discountPer = rs1.getString(6);

				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
				valueXmlString.append("<discount isSrvCallOnChg='0'>").append("<![CDATA[").append(discountPer).append("]]>").append("</discount>\r\n");
				valueXmlString.append("<item_code isSrvCallOnChg='0'>").append("<![CDATA[").append(rs.getString(2).trim()).append("]]>").append("</item_code>\r\n");
				itemCode = (rs.getString(2)==null?" ":rs.getString(2)).trim() ;
				//added by kunal ser item descr.  
				sql = "select descr from item where item_code = ? ";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1,itemCode) ;
				rs1 = pstmt1.executeQuery();
				if (rs1.next())
				{
					descr = rs1.getString(1) == null ?"":rs1.getString(1);

				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;

				valueXmlString.append("<item_descr isSrvCallOnChg='0'>").append("<![CDATA["+descr+"]]>").append("</item_descr>\r\n");

				valueXmlString.append("<quantity isSrvCallOnChg='0'>").append("<![CDATA[").append(rs.getString(5).trim()).append("]]>").append("</quantity>\r\n");
				valueXmlString.append("<unit isSrvCallOnChg='0'>").append("<![CDATA[").append(rs.getString(6).trim()).append("]]>").append("</unit>\r\n");
				valueXmlString.append("<loc_code isSrvCallOnChg='0'>").append("<![CDATA[").append(rs.getString(9).trim()).append("]]>").append("</loc_code>\r\n");

				qty=Double.parseDouble(rs.getString("quantity"));
				itemCode_1=rs.getString("item_code");
				lotNo_1=rs.getString("lot_no");
				lotSl_1=rs.getString("lot_sl");
				lineNoOrd_1= rs.getString(1);
				unit_1= rs.getString(6);
				locCode_1 = rs.getString(9);
				System.out.println("@@@@@@@@@@@@@@@@ 3[qty]::["+qty+"]itemCode_1["+itemCode_1+"]::lotNo_1["+lotNo_1+"]::lotSl_1["+lotSl_1+"]::lineNoOrd_1["+lineNoOrd_1+"]::unit_1["+unit_1+"]::locCode_1["+locCode_1+"]");
				if (rate == 0)
				{
					sql = "SELECT PRICE_LIST FROM DISTORDER WHERE DIST_ORDER = ? ";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1,distOrder) ;
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						priceList = rs1.getString(1);
						System.out.println("priceList :"+priceList);
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;

					// added by cpatil
					System.out.println("@@@@@@@@ testttttttt");
					type = Character.toString( priceListType(priceList) );
					System.out.println("@@@@@@@@ type:["+type+"]");
					if("I".equalsIgnoreCase(type))
					{
						rate = pickRate(priceList, tranDate, rs.getString("ITEM_CODE"), siteCode +"\t"+rs.getString("LOC_CODE")+"\t"+rs.getString("LOT_NO")+"\t"+rs.getString("lot_sl"),'I');  // D	
					}
					else
					{
						rate = pickRate(priceList, tranDate, rs.getString("ITEM_CODE"), rs.getString("LOT_NO"),'D');
					}

				}

				valueXmlString.append("<rate isSrvCallOnChg='0'>").append("<![CDATA[").append(rate).append("]]>").append("</rate>\r\n");
				valueXmlString.append("<lot_no isSrvCallOnChg='0'>").append("<![CDATA[").append(rs.getString(3).trim()).append("]]>").append("</lot_no>\r\n");
				valueXmlString.append("<lot_sl isSrvCallOnChg='0'>").append("<![CDATA[").append(rs.getString(4).trim()).append("]]>").append("</lot_sl>\r\n");
				valueXmlString.append("<pack_code isSrvCallOnChg='0'>").append("<![CDATA[").append(rs.getString(7).trim()).append("]]>").append("</pack_code>\r\n");
				valueXmlString.append("<pack_instr isSrvCallOnChg='0'>").append("<![CDATA[").append(rs.getString(8)).append("]]>").append("</pack_instr>\r\n");
				valueXmlString.append("<gross_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(rs.getString(10)).append("]]>").append("</gross_weight>\r\n");
				valueXmlString.append("<tare_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(rs.getString(11)).append("]]>").append("</tare_weight>\r\n");
				valueXmlString.append("<net_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(rs.getString(12)).append("]]>").append("</net_weight>\r\n");
				//valueXmlString.append("<dimension isSrvCallOnChg='0'>").append("<![CDATA[").append(rs.getString(13)).append("]]>").append("</dimension>\r\n");
				dimension1 = rs.getString(13) == null ?" ":rs.getString(13);
				//added by kunal on 31/01/12 set no art 
				System.out.println("no art = "+rs.getInt(14));
				valueXmlString.append("<no_art isSrvCallOnChg='0'>").append("<![CDATA[").append(rs.getInt(14)).append("]]>").append("</no_art>\r\n");
				palletWt1 = rs.getDouble(15);

				System.out.println("dimension:::"+dimension1+"  palletWt1:::"+palletWt1);

				valueXmlString.append("<quantity isSrvCallOnChg='0'>").append("<![CDATA[").append(rs.getString(5)).append("]]>").append("</quantity>\r\n");
				valueXmlString.append("<tax_class isSrvCallOnChg='0'>").append("<![CDATA[").append(taxClass == null ? "": taxClass).append("]]>").append("</tax_class>\r\n");
				valueXmlString.append("<tax_chap isSrvCallOnChg='0'>").append("<![CDATA[").append(taxChap == null ? "": taxChap).append("]]>").append("</tax_chap>\r\n");
				valueXmlString.append("<tax_env isSrvCallOnChg='0'>").append("<![CDATA[").append(taxEnv == null ? "": taxEnv).append("]]>").append("</tax_env>\r\n");
				taxAmt = 0;
				valueXmlString.append("<tax_amt isSrvCallOnChg='0'>").append("<![CDATA[").append(taxAmt).append("]]>").append("</tax_amt>\r\n");
				amount =  rate * 0;	
				valueXmlString.append("<amount isSrvCallOnChg='0'>").append("<![CDATA[").append(amount).append("]]>").append("</amount>\r\n");
				netAmt = amount + taxAmt;
				valueXmlString.append("<net_amt isSrvCallOnChg='0'>").append("<![CDATA[").append(netAmt).append("]]>").append("</net_amt>\r\n");					

				sql = "SELECT SITE_CODE__MFG,MFG_DATE,EXP_DATE,POTENCY_PERC, "
						+"BATCH_NO, GRADE,RETEST_DATE ,dimension,pallet_wt "
						+"FROM STOCK "
						+"WHERE ITEM_CODE = ? "
						+"AND SITE_CODE = ? "
						+"AND LOC_CODE = ? "
						+"AND LOT_NO = ? "
						+"AND LOT_SL = ? ";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1,rs.getString(2)) ;
				pstmt1.setString(2,siteCode) ;
				pstmt1.setString(3,rs.getString(9)) ;
				pstmt1.setString(4,rs.getString(3)) ;
				pstmt1.setString(5,rs.getString(4)) ;
				rs1 = pstmt1.executeQuery();
				if (rs1.next())
				{
					siteCodeMfg = rs1.getString(1);
					mfgDate = rs1.getDate(2);
					expDate = rs1.getDate(3);
					potencyPerc = rs1.getDouble(4);
					batchNo = rs1.getString(5);
					grade = rs1.getString(6);
					retestDate = rs1.getDate(7);

					dimension = rs1.getString(8) == null?" ":rs1.getString(8);
					palletWt = rs1.getDouble(9);
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;

				System.out.println("dimension s:::"+dimension+"  palletWt s:::"+palletWt);
				//added by kunal on 13/02/13 dimension ,palletWt set from inv_pack_rcp if not in stock 
				if(dimension == null || dimension.trim().length() == 0)
				{
					dimension = dimension1;
				}
				if(palletWt == 0)
				{
					palletWt = palletWt1;
				}


				//change done by kunal ON 25/01/13 for  null date  
				if (mfgDate != null)
				{
					mfgDate1 = sdf.format(mfgDate);
				}
				else
				{
					mfgDate1 = "";
				}
				if (expDate != null)
				{
					expDate1 = sdf.format(expDate);
				}
				else
				{
					expDate1 = "";
				}
				if (retestDate != null)
				{
					retestDate1 = sdf.format(retestDate);
				}
				else
				{
					retestDate1 = "";
				}
				valueXmlString.append("<site_code__mfg isSrvCallOnChg='0'>").append("<![CDATA[").append(siteCodeMfg).append("]]>").append("</site_code__mfg>\r\n");
				valueXmlString.append("<mfg_date isSrvCallOnChg='0'>").append("<![CDATA[").append(mfgDate1).append("]]>").append("</mfg_date>\r\n");
				valueXmlString.append("<exp_date isSrvCallOnChg='0'>").append("<![CDATA[").append(expDate1).append("]]>").append("</exp_date>\r\n");
				valueXmlString.append("<potency_perc isSrvCallOnChg='0'>").append("<![CDATA[").append(potencyPerc).append("]]>").append("</potency_perc>\r\n");
				valueXmlString.append("<batch_no isSrvCallOnChg='0'>").append("<![CDATA[").append(batchNo == null ? "" : batchNo).append("]]>").append("</batch_no>\r\n");
				valueXmlString.append("<grade isSrvCallOnChg='0'>").append("<![CDATA[").append(grade).append("]]>").append("</grade>\r\n");
				valueXmlString.append("<retest_date isSrvCallOnChg='0'>").append("<![CDATA[").append(retestDate1).append("]]>").append("</retest_date>\r\n");
				//added by kunal on 06/02/13 set Dimention , Pallet  Weight from stock .
				valueXmlString.append("<dimension isSrvCallOnChg='0'>").append("<![CDATA[").append(dimension).append("]]>").append("</dimension>\r\n");
				valueXmlString.append("<pallet_wt isSrvCallOnChg='0'>").append("<![CDATA[").append(palletWt).append("]]>").append("</pallet_wt>\r\n");

				// cpatil start
				System.out.println("@@@@@@@@ cpatil start @@@@@@@["+distOrder+"]");

				// 17-01-05 manoharan rate__clg from picelist price_list__clg
				// mdist_order = dw_detedit[ii_currformno].getitemstring(1,"dist_order")
				sql2 = " select price_list__clg from distorder where  dist_order = ? ";
				pstmt2 = conn.prepareStatement(sql2);
				pstmt2.setString(1,distOrder);
				rs2 = pstmt2.executeQuery(); //bug fix done by kunal on 24/04/13 replace pstmt to pstmt2
				if (rs2.next())
				{
					priceListClg=rs2.getString(1);
				}
				pstmt2.close();
				rs2.close();
				pstmt2 = null;
				rs2 = null;
				System.out.println("971 priceListClg ="+priceListClg);
				if ((priceListClg != null) && ( priceListClg.trim().length() > 0 ))
				{
					sql2 = " select count(1) from pricelist where  price_list = ? " +
							" and  list_type  = 'I' ";
					pstmt2 = conn.prepareStatement(sql2);
					pstmt2.setString(1,priceListClg);
					rs2 = pstmt2.executeQuery();
					if (rs2.next())
					{
						counter =rs2.getInt(1);
					}
					pstmt2.close();
					rs2.close();
					pstmt2 = null;
					rs2 = null;


					System.out.println("@@@@@@ 1 qty::"+ qty);
					if ( counter  > 0 )
					{
						if ( lotSl == null )
						{
							lotSl = "";
						}
						System.out.println("@@@@@@@@@@@@@@@@ testtttttttt[qty]::["+qty+"]itemCode_1["+itemCode_1+"]::lotNo_1["+lotNo_1+"]::lotSl_1["+lotSl_1+"]::lineNoOrd_1["+lineNoOrd_1+"]::unit_1["+unit_1+"]::locCode_1["+locCode_1+"]");

						rateClg = distcommon.pickRate( priceListClg, tranDate, itemCode, siteCode.trim() +"~t"+ locCode_1.trim() +"~t"+ lotNo_1.trim() + "~t" + lotSl_1.trim(),"I", qty , conn);
						//mrate_clg = i_nvo_gbf_func.gbf_pick_rate(mprice_list__clg,mtran_date,mitem_code,msite_code+'~t'+mloc_code+'~t'+mlot_no+'~t'+mlot_sl,'I',lc_qty)
						System.out.println("@@@@@1::rateClg["+rateClg+"]");
					}
					else
					{
						listType = distcommon.getPriceListType(priceListClg,conn);
						System.out.println("listType="+listType);
						//rate = pickRate(priceList, tranDate, rs.getString("item_code"), rs.getString("lot_no"),'L');
						//mrate = i_nvo_gbf_func.gbf_pick_rate(mprice_list, mtran_date, mitem_code, mlot_no,'L')             //Commented on 07/04/2k5 by Ruchira
						rateClg =  distcommon.pickRate( priceListClg, tranDate,  itemCode_1, lotNo_1,listType, qty, conn );
						//mrate_clg = i_nvo_gbf_func.gbf_pick_rate(mprice_list__clg, mtran_date, mitem_code, mlot_no,'I',lc_qty) //Added on 07/04/2k5 by Ruchira
						System.out.println("@@@@@2::rateClg["+rateClg+"]");
					}
					valueXmlString.append("<rate__clg isSrvCallOnChg='0'>").append("<![CDATA[").append(rateClg).append("]]>").append("</rate__clg>\r\n");
				}
				else
				{
					sql2 = " Select rate__clg From  distorder_det Where dist_order = ? and  line_no =   ? " ;
					pstmt2 = conn.prepareStatement(sql2);
					pstmt2.setString(1,distOrder);
					pstmt2.setString(2,lineNoOrd_1);  //lineNoOrd 
					rs2 = pstmt2.executeQuery();
					if (rs2.next())
					{
						//rateClg = rs2.getInt("rate__clg");
						rateClg = rs2.getDouble("rate__clg"); //added by kunal on 09/AUG/13 bug fix get decimal value for rate__clg 
					}
					pstmt2.close();
					rs2.close();
					pstmt2 = null;
					rs2 = null;

					System.out.println("@@@@@3::rateClg["+rateClg+"]");
					//	mrate = 0
					if ( rateClg == 0 )
					{ ////Ruchira 19/05/2k6, Added if condition to change the rate entered by user only if price list is defined for the item.
						if ( priceList != null &&  priceList.trim().length()  > 0 )
						{   
							valueXmlString.append("<rate__clg isSrvCallOnChg='0'>").append("<![CDATA[").append(rate).append("]]>").append("</rate__clg>\r\n");
							//w_detedit[ii_currformno].setitem(1,"rate__clg",mrate) // added by neelam 11-08-03
						}
						else
						{
							valueXmlString.append("<rate__clg isSrvCallOnChg='0'>").append("<![CDATA[").append(rateClg).append("]]>").append("</rate__clg>\r\n");	
							//	dw_detedit[ii_currformno].setitem(1,"rate__clg",mrate_clg) // added by neelam 11-08-03
						}
					}
					else
					{
						valueXmlString.append("<rate__clg isSrvCallOnChg='0'>").append("<![CDATA[").append(rateClg).append("]]>").append("</rate__clg>\r\n");
					}
				}

				// added by cpatil as per sugestion
				System.out.println("@@@@@1::unitAlt["+unitAlt+"]::::::unit:["+unit_1+"]");    //qty_order__alt   quantity__alt
				valueXmlString.append("<qty_order__alt isSrvCallOnChg='0'>").append("<![CDATA[").append( qty ).append("]]>").append("</qty_order__alt>\r\n");
				valueXmlString.append("<unit__alt isSrvCallOnChg='0'>").append("<![CDATA[").append( unitAlt ).append("]]>").append("</unit__alt>\r\n");      // by cpatil on 07/05/13 
				
				if( unitAlt.equalsIgnoreCase(unit_1))
				{
					valueXmlString.append("<conv__qty__alt isSrvCallOnChg='0'>").append("<![CDATA[").append( "1" ).append("]]>").append("</conv__qty__alt>\r\n");
				}
				else
				{     
					convAr=   distcommon.convQtyFactor(unitAlt, unit_1, itemCode_1, qty, fact, conn);
					//convAr = distCommon.convQtyFactor(unit, unitStd, itemCode, quantity, convFact, conn);
					fact = Double.parseDouble( convAr.get(0).toString() );
					qtOrderAlt = Double.parseDouble( convAr.get(1).toString() );
					System.out.println("@@@@@@@@ conv__qty__alt::fact["+fact+"]:::::qty_order__alt::qtOrderAlt["+qtOrderAlt+"]");
					valueXmlString.append("<conv__qty__alt isSrvCallOnChg='0'>").append("<![CDATA[").append( fact ).append("]]>").append("</conv__qty__alt>\r\n");
					valueXmlString.append("<qty_order__alt isSrvCallOnChg='0'>").append("<![CDATA[").append( qtOrderAlt ).append("]]>").append("</qty_order__alt>\r\n");
				}
				// cpatil end

				valueXmlString.append("</Detail>\r\n");
				cnt++;
			}//end while
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			valueXmlString.append("</Root>\r\n");

		}
		catch (Exception e)
		{
			System.out.println("Exception :: "+e);
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(rs != null) {
					rs.close(); rs = null;
				}
				if(pstmt != null)	{
					pstmt.close(); pstmt = null;
				}
				if(rs1 != null) {
					rs1.close(); rs1 = null;
				}					
				if(pstmt1 != null)	{
					pstmt1.close(); pstmt1 = null;
				}
				if(rs2 != null) {
					rs2.close(); rs2 = null;
				}					
				if(pstmt2 != null)	{
					pstmt2.close(); pstmt2 = null;
				}
				if(conn != null) {
					conn.close(); conn = null;
				}				
			}
			catch( Exception e )
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		System.out.println("valueXmlString.toString() ["+valueXmlString.toString() + "]");
		return valueXmlString.toString();
	}

	private String actionPackListOld(Document dom1, String objContext, String xtraParams) throws RemoteException, ITMException
	{
		String errCode = "";
		String errString = "";
		String distOrder = "";
		String tranId = "";
		String siteCode = "";
		String sql = "";
		String detailCnt = "0";
		ResultSet rs = null;
		Connection conn = null;
		//Statement stmt = null;
		PreparedStatement pstmt = null;
		String tranDate = "", distIssue = "";
		String sql1 = "", sql2 = "", sql3 = "";
		ResultSet rs1 = null, rs2 = null, rs3 = null;
		//Statement stmt1 = null, stmt2 = null, stmt3 = null;
		PreparedStatement pstmt1 = null; 
		String taxClass = "", taxChap = "", taxEnv = "";
		String priceList = "", siteCodeMfg = "";
		String mfgDate1 = "", expDate1 = "", retestDate1 = ""; 	 
		java.sql.Date mfgDate = null;
		java.sql.Date expDate = null;
		double potencyPerc = 0;
		int cnt = 0;
		String batchNo = "";
		String	grade = "";
		java.sql.Date retestDate = null;
		double rate = 0;
		//double grossWeight1 = 0, tareWeight1 = 0, netWeight1 = 0;
		double taxAmt = 0, amount = 0, netAmt = 0;
		boolean flag = true;
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		//ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		//ConnDriver connDriver = new ConnDriver();
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		/*		String grossWeight = "";
		String tareWeight = "";
		String netWeight = "";
		 */
		int count = 0;
		try
		{
			//detailCnt = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"detCnt");
			if(detailCnt.equals("0"))
			{
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
				//stmt = conn.createStatement();
				distOrder = genericUtility.getColumnValue("dist_order",dom1);	
				System.out.println("distOrder :"+distOrder);
				/*Commented for cross update need to entry in obj_forms
				grossWeight = genericUtility.getColumnValue("gross_weight",dom1);	
				tareWeight = genericUtility.getColumnValue("tare_weight",dom1);	
				netWeight = genericUtility.getColumnValue("net_weight",dom1);	
				 */
				//sql = "SELECT COUNT(*) FROM INV_PACK WHERE ORDER_NO = '"+distOrder+"' "+"AND ORDER_TYPE = 'D' ";
				sql = "SELECT COUNT(*) FROM INV_PACK WHERE ORDER_NO = ? AND ORDER_TYPE = 'D' ";
				//System.out.println("sql :"+sql);
				//rs = stmt.executeQuery(sql);
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, distOrder);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					count = rs.getInt(1);
					System.out.println("Count :"+count);
				}
				rs.close(); rs = null;
				pstmt.close(); pstmt = null;
				if (count == 0)
				{
					errCode = "VTCNTNLL";
					errString = itmDBAccessEJB.getErrorString("",errCode,"","",conn);
					System.out.println("errString :"+errString);
					return errString;					
				}
				else if (count == 1)
				{
					sql = "SELECT TRAN_ID,SITE_CODE FROM INV_PACK WHERE ORDER_NO = ? "  //'"+distOrder+"' "
							+" AND ORDER_TYPE = 'D'";
					//System.out.println("sql :"+sql);
					//rs = stmt.executeQuery(sql);
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, distOrder);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						tranId = rs.getString(1);
						siteCode = rs.getString(2);
					}
					rs.close(); rs = null;
					pstmt.close(); pstmt = null;
				}
				else if (count > 1)//To be asked to Piyush Sir as it opens window for selecting a specific record. 
				{
					sql = "SELECT INV_PACK.TRAN_ID, INV_PACK.TRAN_DATE, INV_PACK.ORDER_NO, INV_PACK.SITE_CODE "  
							+"FROM INV_PACK WHERE INV_PACK.ORDER_NO = ? ";	//'"+distOrder+"'"; 
					//System.out.println("sql :"+sql);
					//rs = stmt.executeQuery(sql);
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, distOrder);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						tranId = rs.getString(1);  // to be ask to piyush sir about multiple records selection
						System.out.println("tranId :"+tranId);
					}
					rs.close(); rs = null;
					pstmt.close(); pstmt = null;
					sql = "SELECT SITE_CODE FROM INV_PACK WHERE TRAN_ID = ? ";	//'"+tranId+"'";
					//System.out.println("sql :"+sql);
					//rs = stmt.executeQuery(sql);
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, tranId);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						siteCode = rs.getString(1);
						System.out.println("siteCode :"+siteCode);
					}
					rs.close(); rs = null;
					pstmt.close(); pstmt = null;
				}
				tranDate = genericUtility.getColumnValue("tran_date",dom1);	
				distIssue = genericUtility.getColumnValue("tran_id",dom1);	
				sql = "SELECT LINE_NO__ORD, ITEM_CODE, LOT_NO, LOT_SL, QUANTITY,UNIT, PACK_CODE, "
						+"PACK_INSTR,LOC_CODE,GROSS_WEIGHT, TARE_WEIGHT, NET_WEIGHT, DIMENSION, NO_ART "
						+"FROM INV_PACK_RCP "
						//+"WHERE TRAN_ID = '"+tranId+"'"+" ORDER BY LINE_NO__ORD";
						+"WHERE TRAN_ID = ? ORDER BY LINE_NO__ORD";
				//System.out.println("sql :"+sql);
				//rs = stmt.executeQuery(sql);
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();				
				/*stmt1 = conn.createStatement();
				stmt2 = conn.createStatement();
				stmt3 = conn.createStatement();*/
				SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
				while (rs.next())
				{
					valueXmlString.append("<Detail>\r\n");
					//valueXmlString.append("<tran_id>").append("<![CDATA[").append(distIssue.trim()).append("]]>").append("</tran_id>\r\n");
					valueXmlString.append("<dist_order>").append("<![CDATA[").append(distOrder.trim()).append("]]>").append("</dist_order>\r\n");
					//valueXmlString.append("<line_no_ord>").append("<![CDATA[").append(rs.getString(1).trim()).append("]]>").append("</line_no_ord>\r\n");
					valueXmlString.append("<line_no_dist_order>").append("<![CDATA[").append(rs.getString(1).trim()).append("]]>").append("</line_no_dist_order>\r\n");

					sql1 = "SELECT TAX_CLASS, TAX_CHAP, TAX_ENV, CASE WHEN RATE IS NULL THEN 0 ELSE RATE END "
							//+"FROM DISTORDER_DET WHERE DIST_ORDER = '"+distOrder+"'"+" AND LINE_NO = '"+rs.getString(1)+"'";
							+"FROM DISTORDER_DET WHERE DIST_ORDER = ? AND LINE_NO = ? ";
					//System.out.println("sql1 :"+sql1);
					//rs1 = stmt1.executeQuery(sql1);
					pstmt1 = conn.prepareStatement(sql1);
					pstmt1.setString(1, distOrder);
					pstmt1.setString(2, rs.getString(1));
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						taxClass = rs1.getString(1);
						taxChap  = rs1.getString(2);
						taxEnv   = rs1.getString(3);
						rate     = rs1.getDouble(4);
					}
					rs1.close(); rs1 = null;
					pstmt1.close(); pstmt1 = null;
					valueXmlString.append("<item_code>").append("<![CDATA[").append(rs.getString(2).trim()).append("]]>").append("</item_code>\r\n");
					valueXmlString.append("<quantity isSrvCallOnChg='1'>").append("<![CDATA[").append(rs.getString(5).trim()).append("]]>").append("</quantity>\r\n");
					valueXmlString.append("<unit>").append("<![CDATA[").append(rs.getString(6).trim()).append("]]>").append("</unit>\r\n");
					valueXmlString.append("<loc_code>").append("<![CDATA[").append(rs.getString(9).trim()).append("]]>").append("</loc_code>\r\n");

					if (rate == 0)
					{
						sql2 = "SELECT PRICE_LIST FROM DISTORDER WHERE DIST_ORDER = ? " ;//'"+distOrder+"'";
						//System.out.println("sql2 :"+sql2);
						//rs2 = stmt2.executeQuery(sql2);
						pstmt1 = conn.prepareStatement(sql2);
						pstmt1.setString(1, distOrder);						
						rs1 = pstmt1.executeQuery();						
						if (rs1.next())
						{
							priceList = rs1.getString(1);
							System.out.println("priceList :"+priceList);
						}
						rs1.close(); rs1 = null;
						pstmt1.close(); pstmt1 = null;
						//Calls nvo_business_object_dist gbf_pick_rate() method i.e. converted below as pickRate() method
						rate = pickRate(priceList, tranDate, rs.getString("ITEM_CODE"), rs.getString("LOT_NO"),'D');						
					}
					/*if (rate <= 0)
					{
						errCode = "VTRATE1";
						flag = false;
					}*/
					if (flag == true)
					{
						//Added arun 
						//valueXmlString.append("<rate>").append("<![CDATA[").append(rate).append("]]>").append("</rate>\r\n");
						valueXmlString.append("<lot_no>").append("<![CDATA[").append(rs.getString(3).trim()).append("]]>").append("</lot_no>\r\n");
						valueXmlString.append("<lot_sl>").append("<![CDATA[").append(rs.getString(4).trim()).append("]]>").append("</lot_sl>\r\n");
						valueXmlString.append("<pack_code>").append("<![CDATA[").append(rs.getString(7).trim()).append("]]>").append("</pack_code>\r\n");
						valueXmlString.append("<pack_instr>").append("<![CDATA[").append(rs.getString(8)).append("]]>").append("</pack_instr>\r\n");
						valueXmlString.append("<gross_weight>").append("<![CDATA[").append(rs.getString(10)).append("]]>").append("</gross_weight>\r\n");
						valueXmlString.append("<tare_weight>").append("<![CDATA[").append(rs.getString(11)).append("]]>").append("</tare_weight>\r\n");
						valueXmlString.append("<net_weight>").append("<![CDATA[").append(rs.getString(12)).append("]]>").append("</net_weight>\r\n");
						valueXmlString.append("<dimension>").append("<![CDATA[").append(rs.getString(13)).append("]]>").append("</dimension>\r\n");
						/*Commented for cross update need to entry in obj_forms				
						grossWeight1 = Double.parseDouble(grossWeight) + rs.getDouble(10);
						valueXmlString.append("<gross_weight>").append("<![CDATA[").append(grossWeight1).append("]]>").append("</gross_weight>\r\n");
						tareWeight1 = Double.parseDouble(tareWeight) + rs.getDouble(11);
						valueXmlString.append("<tare_weight>").append("<![CDATA[").append(tareWeight1).append("]]>").append("</tare_weight>\r\n");
						netWeight1 = Double.parseDouble(netWeight) + rs.getDouble(12);
						valueXmlString.append("<net_weight>").append("<![CDATA[").append(netWeight1).append("]]>").append("</net_weight>\r\n");
						 */
						//valueXmlString.append("<no_art>").append("<![CDATA[").append(rs.getInt(14)).append("]]>").append("</no_art>\r\n");
						valueXmlString.append("<quantity isSrvCallOnChg='1'>").append("<![CDATA[").append(rs.getString(5)).append("]]>").append("</quantity>\r\n");
						//valueXmlString.append("<tax_class>").append("<![CDATA[").append(taxClass).append("]]>").append("</tax_class>\r\n");
						//valueXmlString.append("<tax_chap>").append("<![CDATA[").append(taxChap).append("]]>").append("</tax_chap>\r\n");
						//valueXmlString.append("<tax_env>").append("<![CDATA[").append(taxEnv).append("]]>").append("</tax_env>\r\n");
						valueXmlString.append("<tax_class>").append("<![CDATA[").append(taxClass == null ? "": taxClass).append("]]>").append("</tax_class>\r\n");
						valueXmlString.append("<tax_chap>").append("<![CDATA[").append(taxChap == null ? "": taxChap).append("]]>").append("</tax_chap>\r\n");
						valueXmlString.append("<tax_env>").append("<![CDATA[").append(taxEnv == null ? "": taxEnv).append("]]>").append("</tax_env>\r\n");
						taxAmt = 0;
						//valueXmlString.append("<tax_amt>").append("<![CDATA[").append(taxAmt).append("]]>").append("</tax_amt>\r\n");
						valueXmlString.append("<tax_amt>").append("<![CDATA[").append(taxAmt).append("]]>").append("</tax_amt>\r\n");
						//amount = rs.getDouble(5) * rate; //Commented and replaced by below line
						amount =  rate * 0;				  // as it is in PB code it sets amdount as zero
						valueXmlString.append("<amount>").append("<![CDATA[").append(amount).append("]]>").append("</amount>\r\n");
						//valueXmlString.append("<tax_amt>").append("<![CDATA[").append(taxAmt).append("]]>").append("</tax_amt>\r\n");
						netAmt = amount + taxAmt;
						valueXmlString.append("<net_amt>").append("<![CDATA[").append(netAmt).append("]]>").append("</net_amt>\r\n");					

						sql3 = "SELECT SITE_CODE__MFG,MFG_DATE,EXP_DATE,POTENCY_PERC, "
								+"BATCH_NO, GRADE,RETEST_DATE "
								+"FROM STOCK "
								+"WHERE ITEM_CODE = '"+rs.getString(2)+"' "
								+"AND SITE_CODE = '"+siteCode+"' "
								+"AND LOC_CODE = '"+rs.getString(9)+"' "
								+"AND LOT_NO = '"+rs.getString(3)+"' "
								+"AND LOT_SL = '"+rs.getString(4)+"' ";
						//rs3 = stmt3.executeQuery(sql3);
						pstmt1 = conn.prepareStatement(sql3);
						pstmt1.setString(1, distOrder);						
						rs1 = pstmt1.executeQuery();
						if (rs1.next())
						{
							siteCodeMfg = rs1.getString(1);
							mfgDate = rs1.getDate(2);
							expDate = rs1.getDate(3);
							potencyPerc = rs1.getDouble(4);
							batchNo = rs1.getString(5);
							grade = rs1.getString(6);
							retestDate = rs1.getDate(7);
						}
						rs1.close(); rs1 = null;
						pstmt1.close(); pstmt1 = null;
						if (mfgDate != null)
						{
							mfgDate1 = sdf.format(mfgDate);
						}
						if (expDate != null)
						{
							expDate1 = sdf.format(expDate);
						}
						if (retestDate != null)
						{
							retestDate1 = sdf.format(retestDate);
						}
						valueXmlString.append("<site_code__mfg>").append("<![CDATA[").append(siteCodeMfg).append("]]>").append("</site_code__mfg>\r\n");
						valueXmlString.append("<mfg_date>").append("<![CDATA[").append(mfgDate1).append("]]>").append("</mfg_date>\r\n");
						valueXmlString.append("<exp_date>").append("<![CDATA[").append(expDate1).append("]]>").append("</exp_date>\r\n");
						valueXmlString.append("<potency_perc>").append("<![CDATA[").append(potencyPerc).append("]]>").append("</potency_perc>\r\n");
						//commented by rajendra 30/11/09 for temp purpose
						//	valueXmlString.append("<no_art>").append("<![CDATA[").append(rs.getInt(14)).append("]]>").append("</no_art>\r\n");
						//	valueXmlString.append("<no_art>").append("<![CDATA[").append(rs.getInt(14)).append("]]>").append("</no_art>\r\n");
						valueXmlString.append("<batch_no>").append("<![CDATA[").append(batchNo == null ? "" : batchNo).append("]]>").append("</batch_no>\r\n");
						valueXmlString.append("<grade>").append("<![CDATA[").append(grade).append("]]>").append("</grade>\r\n");
						valueXmlString.append("<retest_date>").append("<![CDATA[").append(retestDate1).append("]]>").append("</retest_date>\r\n");
						valueXmlString.append("</Detail>\r\n");
					}
					else
					{
						if (!errCode.equals(""))
						{
							errString = itmDBAccessEJB.getErrorString("",errCode,"","",conn);
							return errString;
						}
					}								
					cnt++;
				}//end while
				rs.close(); rs = null;
				pstmt.close(); pstmt = null;
				valueXmlString.append("</Root>\r\n");
				if (cnt == 0)
				{
					errCode = "VTCNTNLL";
					System.out.println("errCode :"+errCode);
					errString = itmDBAccessEJB.getErrorString("",errCode,"","",conn);
					return errString;
				}
			}//end if(detailCnt)
		}//try end
		catch(Exception e)
		{
			System.out.println("Exception :DistIssueEJB actionPackList:" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(rs != null) {
					rs.close(); rs = null;
				}
				if(pstmt != null)	{
					pstmt.close(); pstmt = null;
				}
				if(rs1 != null) {
					rs1.close(); rs1 = null;
				}					
				if(pstmt1 != null)	{
					pstmt1.close(); pstmt1 = null;
				}				
				if(conn != null) {
					System.out.println("Closing Connection.....");
					conn.close(); conn = null;
				}								
				//conn.close();
			}catch(Exception e){}
		}
		System.out.println("valueXmlString.toString() "+valueXmlString.toString());
		return valueXmlString.toString();		
	}

	private String actionAllocate(Document dom, Document dom1, String objContext, String xtraParams) throws RemoteException, ITMException
	{
		java.util.Date expDate = null,mfgDate = null, chkDate1 = null; 
		String availableYn = "", lineNo = "", tranId = "", siteCode = "", distOrder = "", itemCode = "";
		String lineNoOrd = "", quantity = "", unit = "", locCode = "", rate = "", rateClg = "";
		String costRate = "", lotNoDist = "", lotSLDist = "", taxClass = "", taxChap = "", taxEnv = "";
		String grossWeight = "", netWeight = "", tareWeight = "", packInstr = "", partQty = "";
		String tranDate = "", locCodeGit = "", noArt = "", errCode = "", errString = "", tracShelfLife = "";
		String tranType = "", checkIntegralQty = "", rate1 = "", active = "", itemDescr = "", locDescr = "";
		String sql = "",sql1 = "", sql2 = "", lotNo = "", packCode = "", discountAmt = "";
		String suppSour = "", trackShelfLife = "", siteCodeMfg = "", sundryCode = "", potencyPerc = ""; 
		String priceList = "", tabValue = "", priceListClg = "", chkDate = "", disCountPer = "",issCriteria="";
		String tranTypePparent = null;
		//Statement stmtS = null;
		ResultSet rs = null, rs1 = null, rs2 = null, rsS = null;
		Connection conn1 = null;
		//Statement stmt = null, stmt1 = null, stmt2 = null ;
		PreparedStatement pstmt = null, pstmt1 = null, pstmt2 = null;
		double mod = 0d, minputQty = 0d, remQuantity = 0d, stockQty = 0d, integralQty = 0d;
		double grossPer = 0d,netPer = 0d,tarePer = 0d, grossWt = 0d, netWt = 0d, tareWt =0d, rateClgVal = 0d, rate2 = 0d; 
		double disAmount = 0d, amount = 0d, shipperQty = 0d;
		int count = 0, minShelfLife = 0, noArt1 = 0, cnt = 0;
		String detailCnt = "0";	
		String qtyOrdAlt = "",unitAlt = "",convQtyAlt = "";
		String res = "", locCodeDamaged = ""; //Gulzar 01/03/07
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		//changed by msalam on 22/09/07 and declared as class level
		//ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		String tranTypeParent = null;
		//ConnDriver connDriver = new ConnDriver();
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();

		try
		{
			//detailCnt = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"detCnt");
			if(detailCnt.equals("0"))
			{
				//Changes and Commented By Bhushan on 13-06-2016 :START
				//conn1 = connDriver.getConnectDB("DriverITM");
				conn1 = getConnection();
				//Changes and Commented By Bhushan on 13-06-2016 :END
				/*stmt = conn1.createStatement();
				stmt1 = conn1.createStatement();
				stmt2 = conn1.createStatement();
				stmtS = conn1.createStatement();*/
				SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());

				availableYn = genericUtility.getColumnValue("available_yn",dom1);	
				//System.out.println("availableYn :"+availableYn);
				if (availableYn == null)   // Added on 05/04/06
				{
					//availableYn = "";
					availableYn = "Y";//Added by jiten  - 04/10/06
				}
				tranId = genericUtility.getColumnValue("tran_id",dom);
				tranDate = genericUtility.getColumnValue("tran_date",dom1);
				//System.out.println("tranId :"+tranId);
				lineNo = genericUtility.getColumnValue("line_no",dom);
				//System.out.println("lineNo :"+lineNo);
				siteCode = genericUtility.getColumnValue("site_code",dom1);
				//System.out.println("siteCode :"+siteCode);
				distOrder = genericUtility.getColumnValue("dist_order",dom);
				//System.out.println("distOrder :"+distOrder);
				//lineNoOrd = genericUtility.getColumnValue("line_no_ord",dom);

				lineNoOrd = genericUtility.getColumnValue("line_no_dist_order",dom);
				System.out.println("actionAllocate() ..................."+lineNoOrd);	  
				//System.out.println("lineNoOrd :"+lineNoOrd);
				//changed by msalam on 22/09/07 start 



				//itemCode = genericUtility.getColumnValue("item_code",dom);
				System.out.println("actionAllocate() method called--------------------------");	  
				System.out.println("itemCode :" + itemCode);

				//changed by msalam on 22/09/07
				//take these 2 values from itemchange as per parent_type of disorder_type
				itemCode = genericUtility.getColumnValue("item_code", dom);
				quantity =  genericUtility.getColumnValue("quantity", dom);
				qtyOrdAlt = genericUtility.getColumnValue("qty_order__alt", dom);
				unit = genericUtility.getColumnValue("unit", dom);
				unitAlt = genericUtility.getColumnValue("unit__alt", dom);
				convQtyAlt = genericUtility.getColumnValue("conv__qty__alt", dom);
				rate = genericUtility.getColumnValue("rate", dom);
				rateClg = genericUtility.getColumnValue("rate__clg", dom);
				taxClass = genericUtility.getColumnValue("tax_class", dom);
				taxChap = genericUtility.getColumnValue("tax_chap", dom);
				taxEnv = genericUtility.getColumnValue("tax_env", dom);
				disCountPer = genericUtility.getColumnValue("discount", dom);
				//changed by msalam on 22/09/07 start
				//quantity = genericUtility.getColumnValue("quantity",dom);
				//System.out.println("quantity :"+quantity);

				//qtyOrdAlt = genericUtility.getColumnValue("qty_order__alt",dom);
				//System.out.println("qty_order__alt :"+qtyOrdAlt);
				//changed by msalam on 010907 end 
				//unit = genericUtility.getColumnValue("unit",dom);
				//System.out.println("unit :" + unit);
				//unitAlt = genericUtility.getColumnValue("unit__alt",dom);
				//System.out.println("unit__alt :" + unitAlt);
				//convQtyAlt = genericUtility.getColumnValue("conv__qty__alt", dom);
				//System.out.println("conv__qty__alt :" + convQtyAlt);

				locCode = genericUtility.getColumnValue("loc_code", dom);	   
				//System.out.println("locCode :"+locCode);

				//rate = genericUtility.getColumnValue("rate", dom);
				//System.out.println("rate :"+rate);

				//rateClg = genericUtility.getColumnValue("rate__clg", dom);
				//System.out.println("rateClg :"+rateClg);
				costRate = genericUtility.getColumnValue("cost_rate", dom);
				//System.out.println("costRate :"+costRate);
				lotNoDist = genericUtility.getColumnValue("lot_no", dom);
				//System.out.println("lotNoDist :"+lotNoDist);
				lotSLDist = genericUtility.getColumnValue("lot_sl", dom);
				System.out.println("lotSLDist :"+lotSLDist);

				//taxClass = genericUtility.getColumnValue("tax_class",dom);
				//System.out.println("taxClass :"+taxClass);
				//taxChap = genericUtility.getColumnValue("tax_chap",dom);
				//System.out.println("taxChap :" + taxChap);
				//taxEnv = genericUtility.getColumnValue("tax_env",dom);
				//System.out.println("taxEnv :"+taxEnv);
				grossWeight = genericUtility.getColumnValue("gross_weight",dom);
				//System.out.println("grossWeight :"+grossWeight);
				netWeight = genericUtility.getColumnValue("net_weight",dom);
				//System.out.println("netWeight :"+netWeight);
				tareWeight = genericUtility.getColumnValue("tare_weight",dom);
				//System.out.println("tareWeight :"+tareWeight);

				packInstr = genericUtility.getColumnValue("pack_instr",dom);
				//System.out.println("packInstr :"+packInstr);
				partQty = genericUtility.getColumnValue("part_qty",dom1);
				//System.out.println("partQty  :"+partQty);
				tranDate = genericUtility.getColumnValue("tran_date",dom1);
				//System.out.println("tranDate :"+tranDate);
				locCodeGit = genericUtility.getColumnValue("loc_code__git",dom1);
				//System.out.println("locCodeGit :"+locCodeGit);
				noArt = genericUtility.getColumnValue("no_art",dom);
				//System.out.println("noArt :"+noArt);
				checkIntegralQty = "N";
				tranType = genericUtility.getColumnValue("tran_type",dom1);
				//System.out.println("tranType :"+tranType);


				if (tranType != null && tranType.trim().length() > 0)
				{
					sql = "SELECT CHECK_INTEGRAL_QTY, TRAN_TYPE__PARENT FROM DISTORDER_TYPE WHERE TRAN_TYPE = ? ";  //'" + tranType + "'";
					/*System.out.println("sql :"+sql);
					rs = stmt.executeQuery(sql);*/
					pstmt = conn1.prepareStatement(sql);
					pstmt.setString(1, tranType);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						//System.out.println( "CHECK_INTEGRAL_QTY :" + rs.getString( 1 ) );
						checkIntegralQty = rs.getString( 1 );
						tranTypeParent = rs.getString( 2 );
						//System.out.println( "tranTypeParent : " + tranTypePparent );

						if (checkIntegralQty == null || checkIntegralQty.trim().length() == 0)
						{
							checkIntegralQty = "Y";
						}
					}
					rs.close(); rs = null;
					pstmt.close(); pstmt = null;
				}
				else
				{
					itemCode = genericUtility.getColumnValue("item_code", dom);
					quantity =  genericUtility.getColumnValue("quantity", dom);
					qtyOrdAlt = genericUtility.getColumnValue("qty_order__alt", dom);
					unit = genericUtility.getColumnValue("unit", dom);
					unitAlt = genericUtility.getColumnValue("unit__alt", dom);
					convQtyAlt = genericUtility.getColumnValue("conv__qty__alt", dom);
					rate = genericUtility.getColumnValue("rate", dom);
					rateClg = genericUtility.getColumnValue("rate__clg", dom);
					taxClass = genericUtility.getColumnValue("tax_class", dom);
					taxChap = genericUtility.getColumnValue("tax_chap", dom);
					taxEnv = genericUtility.getColumnValue("tax_env", dom);
					disCountPer = genericUtility.getColumnValue("discount", dom);				
				}

				//quantitym & qtyOrdAlt set as per logic in in DistIssueEJB item change
				/*
				if (!tranType.equals(tranTypeParent) && this.isDistOrderValuedSet == true )
				{
					quantity = cQtyShipped + "";
					qtyOrdAlt = cQtyShipped + "";
				}
				else
				{
					quantity = cQtyConfirm + "";
					qtyOrdAlt = cQtyConfirm + "";
				}
				 */
				//System.out.println( "quantity :" + quantity );
				//System.out.println( "qty_order__alt :" + qtyOrdAlt );

				//changed by msalam on 22/09/07 to get item_descr in item query.
				//itemDescr can be taken from item query
				//then comment next 2 lines
				//itemDescr = genericUtility.getColumnValue("item_descr",dom);
				//System.out.println("itemDescr :"+itemDescr);
				//end then comment next 2 lines

				locDescr = genericUtility.getColumnValue("location_descr",dom);
				//System.out.println("locDescr :"+locDescr);

				//disCountPer = genericUtility.getColumnValue("discount",dom);
				//System.out.println("disCountPer :"+disCountPer);
				discountAmt = genericUtility.getColumnValue("disc_amt",dom);
				//System.out.println("discountAmt :"+discountAmt);
				if (rate == null || rate.equals(""))
				{
					rate = "0";
				}
				rate1 = rate;
				//changed by msalam on 210907 start
				//To be optimized by msalam 
				/*
				System.out.println("rate1 :"+rate1);
				sql = "SELECT CASE WHEN ACTIVE IS NULL THEN 'Y' ELSE ACTIVE END "
					 +"FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
				System.out.println("sql :"+sql);
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					active = rs.getString(1);
					System.out.println("active :"+active);
					if (active.equals("N"))
					{
						errCode = "VTITEM4";  
						errString = itmDBAccessEJB.getErrorString("",errCode,"","",conn);
						return errString;
					}
				}
				sql = "SELECT MIN_SHELF_LIFE FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
				System.out.println("sql :"+sql);
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					minShelfLife = rs.getInt(1);
					System.out.println("minShelfLife :"+minShelfLife);
					if (minShelfLife == 0)
					{
						minShelfLife = 1;
					}
				}
				 */

				//				sql1 = " SELECT CASE WHEN TRACK_SHELF_LIFE IS NULL THEN 'N' ELSE TRACK_SHELF_LIFE END, " 
				//					 + " CASE WHEN SUPP_SOUR IS NULL THEN 'M' ELSE SUPP_SOUR END "
				//					 + " FROM ITEM WHERE ITEM_CODE = '" + itemCode + "'";
				//							 
				//						System.out.println("sql1 :" + sql1);
				//						rs1 = stmt1.executeQuery(sql1);
				//						if (rs1.next())
				//						{
				//							 trackShelfLife = rs1.getString( 1 );
				//							 System.out.println("trackShelfLife : " + trackShelfLife);
				//							 suppSour = rs1.getString( 2 );
				//							 System.out.println("suppSour : " + suppSour);
				//						}

				//System.out.println("rate1 :" + rate1);
				sql =  " SELECT (CASE WHEN ACTIVE IS NULL THEN 'Y' ELSE ACTIVE END) ACT, MIN_SHELF_LIFE, "
						+ " (CASE WHEN TRACK_SHELF_LIFE IS NULL THEN 'N' ELSE TRACK_SHELF_LIFE END) TRK_SHELF_LIFE, "
						+ " (CASE WHEN SUPP_SOUR IS NULL THEN 'M' ELSE SUPP_SOUR END) SUP_SOUR, DESCR, ISS_CRITERIA "
						+ " FROM ITEM WHERE ITEM_CODE = ? ";	//'" + itemCode + "'";
				//System.out.println( "sql :" + sql );
				//rs = stmt.executeQuery(sql);
				pstmt = conn1.prepareStatement(sql);
				pstmt.setString(1, itemCode);
				rs = pstmt.executeQuery();
				if ( rs.next() )
				{
					active = rs.getString( 1 );

					minShelfLife = rs.getInt( 2 );

					trackShelfLife = rs.getString( 3 );

					suppSour = rs.getString( 4 );

					itemDescr = rs.getString( 5 ); 
					
					//Added By Pavan Rane on 29MAR2018[START][iss_criteria in item master is W. then system should not allow to issue partial quantity.]
					issCriteria = rs.getString( 6 );
					System.out.println("issCriteria  :[" + issCriteria+"]");
					//Added By Pavan Rane on 29MAR2018[END]
					/* System.out.println("active : " + active);
					System.out.println("minShelfLife : " + minShelfLife);
					System.out.println("trackShelfLife : " + trackShelfLife);
					System.out.println("suppSour : " + suppSour);
					System.out.println("itemDescr : " + itemDescr); */

					if( active.equals("N") )
					{
						rs.close(); rs = null;
						pstmt.close(); pstmt = null;
						errCode = "VTITEM4";  
						errString = itmDBAccessEJB.getErrorString("", errCode, "", "", conn1);
						return errString;
					}
				}

				rs.close();
				rs = null;

				pstmt.close();
				pstmt = null;

				//End To be optimized by msalam
				//

				//Added By Gulzar 01/03/07 as In PB AlSo Changed by Prajakta
				//changed by msalam on 21/09/07 so as to optimized the query and 
				//bring all DISTORDER attributes in one query

				sql = " SELECT LOC_CODE__DAMAGED, SUNDRY_CODE, PRICE_LIST, PRICE_LIST__CLG "
						+ " FROM DISTORDER WHERE DIST_ORDER = ? ";  //'" + distOrder + "'";


				//System.out.println("sql :"+sql);
				//stmt = conn1.createStatement();
				//rs = stmt.executeQuery(sql);
				pstmt = conn1.prepareStatement(sql);
				pstmt.setString(1, distOrder);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					locCodeDamaged = rs.getString("LOC_CODE__DAMAGED");
					//System.out.println("locCodeDamaged :["+locCodeDamaged+"]");

					sundryCode = rs.getString( 2 );
					priceList = rs.getString( 3 );
					//System.out.println("priceList :" + priceList);

					priceListClg = rs.getString( 4 );
					//System.out.println( "priceListClg :" + priceListClg );

					if (locCodeDamaged == null)
					{
						locCodeDamaged = "";
					}
					if (locCodeDamaged != null && locCodeDamaged.trim().length() > 0)
					{
						StringTokenizer st = new StringTokenizer(locCodeDamaged,",");
						while (st.hasMoreTokens())
						{
							res = res + "'" + st.nextToken() + "',";
						}
						res = res.substring(0,res.length()-1);
						//	System.out.println("res ::" + res);
						locCodeDamaged = res;
						//	System.out.println("locCodeDamaged After String Tockenized ::"+locCodeDamaged);
					}
				}

				rs.close();
				rs = null;

				pstmt.close();
				pstmt = null;

				//changed by msalam on 21/09/07 end 
				//End Add Gulzar 01/03/07

				//if Lot No is not entered by User in Detail.
				//System.out.println("lotNoDist  :"+lotNoDist);
				if (lotNoDist == null || lotNoDist.trim().length() == 0)
				{
					//	System.out.println("locCode in lotno if :"+locCode);
					if (locCode == null || locCode.trim().length() == 0)
					{
						locCode = "%";
					}
					else
					{
						//locCode = locCode + "%";//Commented by Jiten 04/10/06 as commented in PB
					}
					//	System.out.println("locCode :"+locCode);
					/* --Commened and Changes Below By Gulzar 01/03/07 as changed by Prajakta in PB code.
					if (availableYn != null && availableYn.equals("Y"))
					{
					//	sql = "SELECT COUNT(A.ITEM_CODE) " 
						sql = "SELECT COUNT(*) " 
							 +"FROM STOCK A, INVSTAT B, LOCATION C "
							 +"WHERE A.LOC_CODE = C.LOC_CODE "
							 +"AND C.INV_STAT  = B.INV_STAT "
							 +"AND A.ITEM_CODE = '"+itemCode+"' "
							 +"AND A.SITE_CODE = '"+siteCode+"' "
							 +"AND A.LOC_CODE  LIKE '"+locCode+"' "
							 +"AND B.AVAILABLE = '"+availableYn+"' "
							 +"AND B.USABLE = '"+availableYn+"' "
							 +"AND A.QUANTITY > 0 "
							 +"AND NOT EXISTS (SELECT 1 FROM INV_RESTR I "
											   +"WHERE I.INV_STAT = B.INV_STAT AND I.REF_SER = 'D-ISS')";
						System.out.println("sql :"+sql);
						rs = stmt.executeQuery(sql);
						if (rs.next())
						{
							count = rs.getInt(1);
							System.out.println("count :"+count);
						}
					}	
					else
					{
					//	sql = "SELECT COUNT(A.ITEM_CODE) "
						sql = "SELECT COUNT(*) "
							 +"FROM STOCK A, INVSTAT B, LOCATION C "
							 +"WHERE A.LOC_CODE = C.LOC_CODE "
							 +"AND C.INV_STAT  = B.INV_STAT	"
							 +"AND A.ITEM_CODE = '"+itemCode+"' "
							 +"AND A.SITE_CODE = '"+siteCode+"' "
							 +"AND A.LOC_CODE  LIKE '"+locCode+"' "
							 +"AND B.AVAILABLE = '"+availableYn+"' "
							 +"AND B.USABLE = '"+availableYn+"' "
							 +"AND A.QUANTITY  > 0 ";
						System.out.println("sql :"+sql);
						rs = stmt.executeQuery(sql);
						if (rs.next())
						{
							count = rs.getInt(1);
							System.out.println("count :"+count);
						}
					}
					 *///End Comment Gulzar 01/03/07
					//Added By Gulzar 01/03/07

					//changed by msalam on 22/09/07 end
					//this execution of query is removed and put a vaiable isRecordFound 
					//in record not found then prompt exception
					/*
					sql = "SELECT COUNT(*) " 
							 +"FROM STOCK A, INVSTAT B, LOCATION C "
							 +"WHERE A.LOC_CODE = C.LOC_CODE "
							 +"AND C.INV_STAT  = B.INV_STAT "
							 +"AND A.ITEM_CODE = '"+itemCode+"' "
							 +"AND A.SITE_CODE = '"+siteCode+"' "
							 +"AND A.LOC_CODE  LIKE '"+locCode+"' "
							 +"AND B.AVAILABLE = '"+availableYn+"' "
							 +"AND B.USABLE = '"+availableYn+"' "
							 +"AND A.QUANTITY - A.ALLOC_QTY > 0  ";
					if (availableYn.equals("Y"))
					{
						sql = sql + "AND NOT EXISTS (SELECT 1 FROM INV_RESTR I WHERE I.INV_STAT = B.INV_STAT AND I.REF_SER = 'D-ISS') ";
					}
					if (locCodeDamaged != null && locCodeDamaged.trim().length() > 0)
					{
						sql = sql + "AND A.LOC_CODE IN ("+locCodeDamaged+")";
					}
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						count = rs.getInt(1);
						System.out.println("count :"+count);
					}
					//End Addition Gulzar 01/03/07
					if (count == 0)
					{
						errCode = "VTDIST16";
						errString = itmDBAccessEJB.getErrorString("",errCode,"","",conn);
						return errString;
					}
					 */
					//changed by msalam on 22/09/07 end 

					boolean isRecordFound = false;

					sql = "SELECT A.LOT_NO, A.LOT_SL, A.QUANTITY, A.EXP_DATE, A.UNIT, A.ITEM_SER, "
							+" A.SITE_CODE__MFG, A.MFG_DATE, A.POTENCY_PERC, A.ALLOC_QTY, "
							+" A.PACK_CODE, A.LOC_CODE, A.BATCH_NO, A.GRADE , "
							+" A.GROSS_WEIGHT, A.TARE_WEIGHT, A.NET_WEIGHT, A.DIMENSION, A.RETEST_DATE, "
							+" A.SUPP_CODE__MFG, A.PACK_INSTR,A.RATE,C.DESCR DESCR "
							+" FROM STOCK A, INVSTAT B, LOCATION C "
							+" WHERE A.INV_STAT = B.INV_STAT "
							+" AND A.LOC_CODE = C.LOC_CODE "
							+" AND A.ITEM_CODE = '" + itemCode + "' "
							+" AND A.SITE_CODE = '" + siteCode + "' "
							+" AND A.LOC_CODE  LIKE '" + locCode + "%' "
							+" AND B.AVAILABLE = '" + availableYn + "' "
							+" AND B.USABLE = '" + availableYn + "' "
							+" AND B.STAT_TYPE = 'M' "
							+" AND A.QUANTITY - A.ALLOC_QTY > 0 ";
					if( availableYn != null && availableYn.equals("Y") )
					{
						sql = sql + " AND NOT EXISTS (SELECT 1 FROM INV_RESTR I "
								+"WHERE I.INV_STAT = B.INV_STAT AND I.REF_SER = 'D-ISS') ";
					}
					//Added By Gulzar 01/03/07 as Changes Made In PB Logic
					if( locCodeDamaged != null && locCodeDamaged.trim().length() > 0 )
					{
						sql = sql + "AND A.LOC_CODE IN (" + locCodeDamaged + ")";
					}
					//End Add gulzar 01/03/07
					sql =  sql + " ORDER BY A.EXP_DATE,A.CREA_DATE,A.LOT_NO, A.LOT_SL ";
					/*System.out.println( "sql :" + sql );
					stmt = conn1.createStatement();
					rs = stmt.executeQuery( sql );*/
					pstmt = conn1.prepareStatement(sql);
					rs  = pstmt.executeQuery();
					remQuantity = Double.parseDouble(quantity);
					//System.out.println("remQuantity :" + remQuantity);
					while (rs.next())
					{
						isRecordFound = true;

						lotNo = rs.getString(1);
						//System.out.println("lotNo :" + lotNo);
						packCode = rs.getString(11);
						//System.out.println("packCode :" + packCode);
						if (remQuantity == 0)
						{
							break;
						}
						stockQty = rs.getDouble(3) - rs.getDouble(10);
						//	System.out.println("stockQty :" + stockQty);
						if (stockQty == 0)
						{
							continue;
						}
						if (availableYn.equals("Y"))
						{
							if (minShelfLife > 0)
							{
								chkDate = calcExpiry(tranDate,minShelfLife); //calcExpiry function to be checked.
								//System.out.println("chkDate :" + chkDate);
								chkDate1 = sdf.parse(chkDate);
								java.sql.Date date1 = rs.getDate(4);
								//System.out.println("date1 :" + date1);
								java.util.Date date2 = null;
								if(date1 != null)
								{
									date2 = new java.util.Date(date1.getTime());
									//System.out.println("chkDate1 :" + chkDate1);
									//System.out.println("date2 :" + date2);
									if((chkDate1.compareTo(date2) > 0))
									{
										continue;
									} 
								}								
							}
						}//end if
						//System.out.println("checkIntegralQty :" + checkIntegralQty);
						//if (checkIntegralQty.equals("Y"))//Commented by jiten 04/10/06
						if (!checkIntegralQty.equals("N"))
						{
							//integralQty = getIntegralQty( siteCode, itemCode, lotNo, packCode );
							integralQty = getIntegralQty( siteCode, itemCode, lotNo, packCode, checkIntegralQty );
							//System.out.println("integralQty :"+integralQty);
							if (integralQty <= 0)
							{
								errCode = "VINTGRLQTY";
								errString = itmDBAccessEJB.getErrorString( "", errCode, "", "", conn1);
								//System.out.println("errString:" + errString + ":");
								return errString;
							}
						}
						System.out.println("stockQty before issCriteria[" + stockQty + "] remQuantity :[" + remQuantity+"]");
						//Added By Pavan Rane on 29MAR2018[START][iss_criteria in item master is W. then system should not allow to issue partial quantity.]
						System.out.println("--1--issCriteria stockQty[" + stockQty + "] remQuantity[" + remQuantity+"] minputQty["+minputQty+"]");
						if (issCriteria != null && ("W").equalsIgnoreCase(issCriteria))
						{
							
							minputQty = stockQty;	
							/*if(minputQty > remQuantity)//below if condn as per suggested  by sm sir on 03aug18 
							{
								System.out.println("--2--issCriteria stockQty[" + stockQty + "] remQuantity[" + remQuantity+"] minputQty["+minputQty+"]");
								continue;
							}*/
							if (stockQty >= remQuantity)
							{
								remQuantity = 0;
							}
							else
							{
								remQuantity = remQuantity - stockQty;
							}	
							System.out.println("--3--issCriteria stockQty[" + stockQty + "] remQuantity[" + remQuantity+"] minputQty["+minputQty+"]");
						}
						else 
						{
						//Added by Pavan Rane on 29MAR2018[END]
							if (stockQty >= remQuantity)
							{
								if (checkIntegralQty.equals("Y"))
								{
									//mod = (remQuantity % integralQty);
									//System.out.println("mod :"+mod);
									remQuantity = remQuantity - (remQuantity % integralQty);
									//System.out.println("remQuantity :"+remQuantity);
								}							
								minputQty = remQuantity;
								System.out.println("@@ 2214 minputQty :"+minputQty);
								remQuantity = 0;							
							} 
							else if (stockQty < remQuantity)
							{
								if (checkIntegralQty.equals("Y"))
								{
									//mod = (stockQty % integralQty);
									//System.out.println("mod :"+mod);
									//stockQty = stockQty - mod;
									stockQty = stockQty - (stockQty % integralQty);
									//System.out.println("stockQty :"+stockQty);
								}
								minputQty = stockQty;
								//System.out.println("minputQty :"+minputQty);
								remQuantity = remQuantity - stockQty;
								//System.out.println("remQuantity :"+remQuantity);							
							} 
						}
						//System.out.println("@@ 2233 minputQty :"+minputQty);
						System.out.println("--4--issCriteria stockQty[" + stockQty + "] remQuantity[" + remQuantity+"] minputQty["+minputQty+"]");
						if (minputQty == 0)
						{
							continue;
						}
						rate1 = rate;
						//System.out.println("rate1 :"+rate1);
						if (rate1.equals(""))
						{
							rate1 = "0";
						}
						//if (rate1.equals("0")) // Commented on 04/04/06
						if (Double.parseDouble(rate1) == 0) //Added on 04/04/06 - jiten
						{
							//changed by msalam on 21/09/07 start
							//PRICE_LIST is already coming in previous query
							//commented on 21/09/07
							/*
							sql1 = "SELECT PRICE_LIST FROM DISTORDER WHERE DIST_ORDER = '" + distOrder + "'";
							System.out.println("sql1 :"+sql1);
							rs1 = stmt1.executeQuery(sql1);
							 */
							//if (rs1.next()) commentd and modified line added by msalam
							//if (rs1.next())
							if( priceList != null && priceList.trim().length() > 0 )
							{
								//changed by msalam on 21/09/07 next 2 lines
								//priceList = rs1.getString(1);
								//System.out.println("priceList :" + priceList);
								//changed by msalam on 21/09/07 end 
								sql2 = "SELECT COUNT(*) FROM PRICELIST " 
										+ "WHERE PRICE_LIST = '" + priceList + "'" + " AND LIST_TYPE = 'I' ";
								//System.out.println("sql2 :" + sql2);
								//rs2 = stmt2.executeQuery(sql2);
								pstmt2 = conn1.prepareStatement(sql2);
								rs2 = pstmt2.executeQuery();
								if (rs2.next())
								{
									count = rs2.getInt(1);
								}
								rs2.close(); rs2 = null;
								pstmt2.close(); pstmt2 = null;
								System.out.println("count :" + count);
								if (count == 0)
								{

									rate2 = pickRate(priceList, tranDate, itemCode, rs.getString(1),'D');
									System.out.println("rate2 :" + rate2);
								}
								else
								{
									tabValue = siteCode + "\t" + rs.getString(12) + "\t" + rs.getString(1) + "\t";
									System.out.println("tabValue :[" + tabValue+"]");
									rate2 = pickRate(priceList, tranDate, itemCode, tabValue, 'I');
									System.out.println("rate2 :" + rate2);
								}
								/*if (rate2 <= 0)//Commented as during conversion it was uncommented in PB but at the time of Source walkthrough it was commented in PB
								{
									errCode = "VTRATE1"; 
									errString = itmDBAccessEJB.getErrorString("",errCode,"","",conn);
									return errString;
								} */
							}
							rate1 = Double.toString(rate2);	// Added on 04/04/06 - Jiten
						}
						//if (rateClg.equals(""))//Commented 0n 04/04/06
						if (rateClg == null || rateClg.equals("") || Double.parseDouble(rateClg) == 0) // Added on 04/04/06 - jiten
						{
							//changed by msalam on 21/09/07 start 
							//commented and modified lines andded 
							/*
							sql1 = " SELECT PRICE_LIST__CLG FROM DISTORDER WHERE DIST_ORDER = '" + distOrder + "'";
							System.out.println("sql1 :"+sql1);
							rs1 = stmt1.executeQuery(sql1);
							if (rs1.next())
							{

								priceListClg = rs1.getString(1);
							 */
							if (priceListClg != null && priceListClg.trim().length() > 0 )
							{
								//end chnaged by msalam on 21/09/07 end 
								rateClgVal = pickRate(priceListClg, tranDate, itemCode, rs.getString(1),'D');
								//System.out.println("rateClgVal :"+rateClgVal);
							}
							if (rateClgVal <= 0)
							{
								rateClgVal = rate2;
								//System.out.println("rateClgVal :"+rateClgVal);
							}
							rateClg = Double.toString(rateClgVal); // Added on 04/04/06 - Jiten
						}
						if (Double.parseDouble(rs.getString(3)) > 0)
						{
							grossPer    = rs.getDouble(15) / rs.getDouble(3);
							//System.out.println("grossPer :"+grossPer);
							netPer 	    = rs.getDouble(17) 	/ rs.getDouble(3);
							//System.out.println("netPer :"+netPer);
							tarePer 	= rs.getDouble(16) / rs.getDouble(3);
							//System.out.println("tarePer :"+tarePer);					 
							grossWt = minputQty * grossPer;
							//System.out.println("grossWt :"+grossWt);
							netWt   = minputQty * netPer;
							//System.out.println("netWt :"+netWt);
							tareWt  = minputQty * tarePer;
							//System.out.println("tareWt :"+tareWt);
						}
						//changed by msalam on 21/09/07 start
						//All the attributes will be brought in a single query
						/*
						sql1 = " SELECT CASE WHEN TRACK_SHELF_LIFE IS NULL THEN 'N' ELSE TRACK_SHELF_LIFE END, " 
							 + " CASE WHEN SUPP_SOUR IS NULL THEN 'M' ELSE SUPP_SOUR END "
							 + " FROM ITEM WHERE ITEM_CODE = '" + itemCode + "'";

						System.out.println("sql1 :" + sql1);
						rs1 = stmt1.executeQuery(sql1);
						if (rs1.next())
						{
							 trackShelfLife = rs1.getString( 1 );
							 System.out.println("trackShelfLife : " + trackShelfLife);
							 suppSour = rs1.getString( 2 );
							 System.out.println("suppSour : " + suppSour);
						}
						 */
						//changed by msalam on 21/09/07 end 
						//Commented - during writing the logic the PB Code was not commented but at the time of source walkthrough
						// it was commented.
						/*
						siteCodeMfg = rs.getString(7);
						System.out.println("siteCodeMfg :"+siteCodeMfg);
						if (siteCodeMfg == null && suppSour.equals("N"))
						{
							errCode = "VTSITEMFG1"; 
							errString = itmDBAccessEJB.getErrorString("",errCode,"","",conn);
							return errString;
						}
						if (rs.getDate("mfg_date") == null && trackShelfLife.equals("Y"))
						{
							errCode = "VTMFGDATE3"; 
							errString = itmDBAccessEJB.getErrorString("",errCode,"","",conn);
							return errString;
						}
						if (rs.getDate("exp_date") == null && trackShelfLife.equals("Y"))
						{
							errCode = "VTEXPDATE1";
							errString = itmDBAccessEJB.getErrorString("",errCode,"","",conn);
							return errString;
						} 
						 */

						disAmount = (amount * (Double.parseDouble(disCountPer) / 100));
						//System.out.println("disAmount :" + disAmount);

						//Changed by msalam on 21/09/07 start 
						//To be optimized by msalam on 21/09/07 start
						/*
						sql1 = " SELECT SUNDRY_CODE FROM DISTORDER WHERE DIST_ORDER = '" + distOrder + "'";
						System.out.println("sql1 :" + sql1);
						rs1 = stmt1.executeQuery(sql1);
						if (rs1.next())
						{
							sundryCode = rs1.getString(1);
							System.out.println("sundryCode :"+sundryCode);
							noArt1 = 0;
							noArt1 = getNoArt(siteCode, sundryCode, itemCode, packCode, minputQty, 'B', shipperQty, integralQty);
							System.out.println("noArt1 :"+noArt1);
							noArt = ""+noArt1; //Added By Gulzar 29/03/07
						}
						 */
						if( sundryCode != null && sundryCode.trim().length() > 0 )
						{
							noArt1 = 0;
							noArt1 = getNoArt(siteCode, sundryCode, itemCode, packCode, minputQty, 'B', shipperQty, integralQty);
							//System.out.println("noArt1 :"+noArt1);
							noArt = "" + noArt1; //Added By Gulzar 29/03/07
						}
						//To be optimized by msalam on 21/09/07 end
						//Changed by msalam on 21/09/07 end

						//changed by msalam on 27/09/07 start
						//for itemchange on following fields so as to avoid calling NVO for itemchange
						//1. quantity
						//2. lot_no
						//3. line_no_dist_order
						//same logic as in pb


						//changed by msalam on 27/09/07 end

						//added on 28/11/09 by rajendra
						//Select pack_code into :mpack_code from item where item_code = :mitem_code;
						//dw_detedit[ii_currformno].SetItem(1,"pack_code", mpack_code)
						double shipperSize=0,shipQty=0,noArt11=0,remainder=0;
						double integralqty=0;
						double noArt12=0,acShipperQty=0,acIntegralQty=0;
						sql ="select (case when shipper_size is null then 0 else shipper_size end) shipper_size"
								+" from item_lot_packsize where item_code = '"+itemCode+"'"
								+" and  '"+lotNo+"' >= lot_no__from "
								+" and  '"+lotNo+"'  <= lot_no__to ";
						//System.out.println("sql :"+sql);
						//rs1 = stmt1.executeQuery(sql);
						pstmt1 = conn1.prepareStatement(sql);
						rs1 = pstmt1.executeQuery();
						if (rs1.next())
						{
							shipperSize = rs1.getDouble(1);
						}
						rs1.close(); rs1 = null;
						pstmt1.close(); pstmt1 = null;
						System.out.println("shipperSize .............:"+shipperSize);	
						System.out.println("minputQty .............:"+minputQty);	
						if( shipperSize > 0)
						{
							shipQty = shipperSize;
							noArt11 = (minputQty - (minputQty % shipQty))/shipQty;
							//System.out.println("noArt11 .............:"+noArt11);
							remainder = minputQty % shipQty;
							//System.out.println("remainder .............:"+remainder);
							sql ="select ( case when integral_qty is null then 0 else integral_qty end) integral_qty"
									//+" from customeritem where cust_code = '"+sundryCode+"' and item_code ='"+itemCode+"'";
									+" from customeritem where cust_code = ? and item_code = ?";
							//System.out.println("sql :"+sql);
							//rs1 = stmt1.executeQuery(sql);
							pstmt1 = conn1.prepareStatement(sql);
							pstmt1.setString(1, sundryCode);
							pstmt1.setString(2, itemCode);
							rs1 = pstmt1.executeQuery();
							if (rs1.next())
							{
								integralqty = rs1.getDouble(1);

							}
							rs1.close(); rs1 = null;
							pstmt1.close(); pstmt1 = null;
							//System.out.println("integralqty .............:"+integralqty);
							if(integralqty ==0)
							{
								sql ="select  ( case when integral_qty is null then 0 else integral_qty end) integral_qty"
										//+" from siteitem where site_code = '"+siteCode+"' and item_code ='"+itemCode+"'";
										+" from siteitem where site_code = ? and item_code = ?";
								//System.out.println("sql :"+sql);
								//rs1 = stmt1.executeQuery(sql);
								pstmt1 = conn1.prepareStatement(sql);
								pstmt1.setString(1, siteCode);
								pstmt1.setString(2, itemCode);
								rs1 = pstmt1.executeQuery();
								if (rs1.next())
								{
									integralqty = rs1.getDouble(1);

								}
								rs1.close(); rs1 = null;
								pstmt1.close(); pstmt1 = null;
								//System.out.println("integralqty .............:"+integralqty);
								if(integralqty ==0)
								{
									sql ="select ( case when integral_qty is null then 0 else integral_qty end) integral_qty"
											+" from item where item_code = ? ";//'"+itemCode+"'";
									//System.out.println("sql :"+sql);
									//rs1 = stmt1.executeQuery(sql);
									pstmt1 = conn1.prepareStatement(sql);
									pstmt1.setString(1, itemCode);
									rs1 = pstmt1.executeQuery();
									if (rs1.next())
									{
										integralqty = rs1.getDouble(1);
										//System.out.println("integralqty .............:"+integralqty);
									}
									rs1.close(); rs1 = null;
									pstmt1.close(); pstmt1 = null;
								}

							} 
							//System.out.println("integralqty .............:"+integralqty);
							if(integralqty > 0)
							{
								noArt12 = (remainder -(remainder % integralqty))/integralqty;
								//System.out.println("noArt12 ....2.........:"+noArt12);
							}
							if(noArt12 > 0)
							{
								noArt12 =1;
								//System.out.println("noArt2 ....0.........:"+noArt12);
							}
							noArt1			= (int)(noArt11 + noArt12);
							noArt = "" + noArt1;
							//System.out.println("noArt .............:"+noArt);
							acShipperQty	= shipQty;
							acIntegralQty	= integralqty;
						}
						if(shipperSize ==0)
						{
							noArt1 = getNoArt(siteCode, sundryCode, itemCode, packCode, minputQty, 'B', acShipperQty, acIntegralQty);
							noArt = "" + noArt1;
							//System.out.println("noArt .............:"+noArt);
						}
						//ended on 28/11/09
						String abc =rs.getString(5);


						valueXmlString.append("<Detail>\r\n");
						valueXmlString.append("<tran_id isSrvCallOnChg='0'>").append("<![CDATA[").append(tranId).append("]]>").append("</tran_id>\r\n");
						valueXmlString.append("<dist_order isSrvCallOnChg='0'>").append("<![CDATA[").append(distOrder).append("]]>").append("</dist_order>\r\n");
						//valueXmlString.append("<line_no_ord isSrvCallOnChg='0'>").append("<![CDATA[").append(lineNoOrd).append("]]>").append("</line_no_ord>\r\n");
						//Temporarily commented & changed - jiten - 20/05/06
						//valueXmlString.append("<line_no_dist_order isSrvCallOnChg='0'>").append("<![CDATA[").append(lineNoOrd).append("]]>").append("</line_no_dist_order>\r\n");
						//changed by rajendra for item change on line_no_dist_order 10/21/2008

						valueXmlString.append("<line_no_dist_order isSrvCallOnChg='1'>").append("<![CDATA[").append(lineNoOrd).append("]]>").append("</line_no_dist_order>\r\n");
						// Jiten						
						valueXmlString.append("<item_code isSrvCallOnChg='0'>").append("<![CDATA[").append(itemCode == null ? "" : itemCode).append("]]>").append("</item_code>\r\n");
						valueXmlString.append("<item_descr isSrvCallOnChg='0'>").append("<![CDATA[").append(itemDescr==null ? "" : itemDescr).append("]]>").append("</item_descr>\r\n");
						valueXmlString.append("<location_descr isSrvCallOnChg='0'>").append("<![CDATA[").append(rs.getString(23)).append("]]>").append("</location_descr>\r\n");
						valueXmlString.append("<unit isSrvCallOnChg='0'>").append("<![CDATA[").append(rs.getString(5)).append("]]>").append("</unit>\r\n");
						valueXmlString.append("<unit__alt isSrvCallOnChg='0'>").append("<![CDATA[").append(unitAlt).append("]]>").append("</unit__alt>\r\n");

						valueXmlString.append("<conv__qty__alt isSrvCallOnChg='0'>").append("<![CDATA[").append(convQtyAlt).append("]]>").append("</conv__qty__alt>\r\n");
						//Below line commented as it will get itemchanged by itemchange of Quantity field. - jiten 20/05/06
						//valueXmlString.append("<qty_order__alt isSrvCallOnChg='0'>").append("<![CDATA[").append(qtyOrdAlt).append("]]>").append("</qty_order__alt>\r\n");
						String tLocCode = null;
						tLocCode = rs.getString(12);
						valueXmlString.append("<loc_code isSrvCallOnChg='0'>").append("<![CDATA[").append( (tLocCode == null ? "" : tLocCode.trim()) ).append("]]>").append("</loc_code>\r\n");
						valueXmlString.append("<rate isSrvCallOnChg='0'>").append("<![CDATA[").append(rate1).append("]]>").append("</rate>\r\n");
						//next line commented by mslam on 031108 as it gets set from item change on lot no
						// 27/10/10 manoharan commented as itemchanged is disabled 
						valueXmlString.append("<rate__clg isSrvCallOnChg='0'>").append("<![CDATA[").append(rateClg).append("]]>").append("</rate__clg>\r\n"); //Commented - jiten - 05/04/06 -  as set in itemChange of lot_no
						//for test this line being taken next to lot no
						//valueXmlString.append("<quantity isSrvCallOnChg='0'>").append("<![CDATA[").append(minputQty).append("]]>").append("</quantity>\r\n");
						valueXmlString.append("<amount isSrvCallOnChg='0'>").append("<![CDATA[").append(minputQty*Double.parseDouble(rate1)).append("]]>").append("</amount>\r\n");						
						String tLotSl = null;
						tLotSl = rs.getString(2);
						// 15/10/09 manoharan lot_sl should not be empty string
						//valueXmlString.append("<lot_sl isSrvCallOnChg='0'>").append("<![CDATA[").append( ( tLotSl == null ? "" : tLotSl.trim()) ).append("]]>").append("</lot_sl>\r\n");
						if (tLotSl == null || "null".equals(tLotSl) || tLotSl.trim().length() == 0 )
						{
							tLotSl = "     ";
						}
						valueXmlString.append("<lot_sl isSrvCallOnChg='0'>").append("<![CDATA[").append(tLotSl).append("]]>").append("</lot_sl>\r\n");

						//valueXmlString.append("<lot_no isSrvCallOnChg='1'>").append("<![CDATA[").append(( (rs.getString(1) == null) ? "": rs.getString(1).trim())).append("]]>").append("</lot_no>\r\n");
						lotNo = rs.getString(1);
						if (lotNo == null || "null".equals(lotNo) || lotNo.trim().length() == 0 )
						{
							lotNo = "               ";
						}
						valueXmlString.append("<lot_no isSrvCallOnChg='1'>").append("<![CDATA[").append(lotNo).append("]]>").append("</lot_no>\r\n");
						// 15/10/09 manoharan 
						valueXmlString.append("<pack_code isSrvCallOnChg='0'>").append("<![CDATA[").append((rs.getString(11) == null) ? "":rs.getString(11)).append("]]>").append("</pack_code>\r\n");
						valueXmlString.append("<disc_amt isSrvCallOnChg='0'>").append("<![CDATA[").append(disAmount).append("]]>").append("</disc_amt>\r\n");
						valueXmlString.append("<tax_class isSrvCallOnChg='0'>").append("<![CDATA[").append( ( taxClass == null ? "": taxClass ) ).append("]]>").append("</tax_class>\r\n");
						valueXmlString.append("<tax_chap isSrvCallOnChg='0'>").append("<![CDATA[").append( ( taxChap == null ? "": taxChap ) ).append("]]>").append("</tax_chap>\r\n");
						valueXmlString.append("<tax_env isSrvCallOnChg='0'>").append("<![CDATA[").append( ( taxEnv == null ? "": taxEnv ) ).append("]]>").append("</tax_env>\r\n");
						//updated by nisar on 11/23/2007 original : grossWt replace with getFormatedValue(grossWt,3)
						grossWt = Double.parseDouble(getFormatedValue(grossWt,3));//added by nisar on 11/23/2007
						System.out.println("[DistIssueActEJB] Gross Wt=============>"+grossWt);
						valueXmlString.append("<gross_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(grossWt).append("]]>").append("</gross_weight>\r\n");
						//updated by nisar on 11/23/2007 original : netWt replace with getFormatedValue(netWt,3)
						netWt = Double.parseDouble(getFormatedValue(netWt,3));//added by nisar on 11/23/2007
						System.out.println("[DistIssueActEJB] Net Wt=============>"+netWt);
						valueXmlString.append("<net_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(netWt).append("]]>").append("</net_weight>\r\n");
						//updated by nisar on 11/23/2007 original : tareWt replace with getFormatedValue(tareWt,3)
						tareWt = Double.parseDouble(getFormatedValue(netWt,3));//added by nisar on 11/23/2007
						System.out.println("[DistIssueActEJB] Tare Wt=============>"+tareWt);
						valueXmlString.append("<tare_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(tareWt).append("]]>").append("</tare_weight>\r\n");
						//valueXmlString.append("<pack_instr isSrvCallOnChg='0'>").append("<![CDATA[").append(rs.getString(21)).append("]]>").append("</pack_instr>\r\n"); //Gulzar 24/03/07
						valueXmlString.append("<pack_instr isSrvCallOnChg='0'>").append("<![CDATA[").append((rs.getString(21) == null) ? "":rs.getString(21)).append("]]>").append("</pack_instr>\r\n"); //Gulzar 24/03/07
						valueXmlString.append("<retest_date isSrvCallOnChg='0'>").append("<![CDATA[").append((rs.getDate(19) == null) ? "":sdf.format(rs.getDate(19))).append("]]>").append("</retest_date>\r\n");
						valueXmlString.append("<dimension isSrvCallOnChg='0'>").append("<![CDATA[").append((rs.getString(18) == null) ? "":rs.getString(18)).append("]]>").append("</dimension>\r\n");
						//valueXmlString.append("<supp_code__mfg isSrvCallOnChg='0'>").append("<![CDATA[").append((rs.getString(21) == null) ? "":rs.getString(21)).append("]]>").append("</supp_code__mfg>\r\n"); //Gulzar 24/03/07

						valueXmlString.append("<supp_code__mfg isSrvCallOnChg='0'>").append("<![CDATA[").append((rs.getString(20) == null) ? "":rs.getString(20)).append("]]>").append("</supp_code__mfg>\r\n"); //Gulzar 24/03/07
						valueXmlString.append("<site_code__mfg isSrvCallOnChg='0'>").append("<![CDATA[").append((rs.getString(7) == null) ? "":rs.getString(7)).append("]]>").append("</site_code__mfg>\r\n");
						valueXmlString.append("<mfg_date isSrvCallOnChg='0'>").append("<![CDATA[").append((rs.getDate(8) == null) ? "":sdf.format(rs.getDate(8))).append("]]>").append("</mfg_date>\r\n");
						valueXmlString.append("<exp_date isSrvCallOnChg='0'>").append("<![CDATA[").append((rs.getDate(4) == null) ? "":sdf.format(rs.getDate(4))).append("]]>").append("</exp_date>\r\n");
						valueXmlString.append("<potency_perc isSrvCallOnChg='0'>").append("<![CDATA[").append( ( (rs.getString(9) == null) ? "": rs.getString(9) ) ).append("]]>").append("</potency_perc>\r\n");
						//commented by rajendra on 10/11/09 noArt
						//valueXmlString.append("<no_art isSrvCallOnChg='0'>").append("<![CDATA[").append(noArt).append("]]>").append("</no_art>\r\n");
						valueXmlString.append("<no_art isSrvCallOnChg='0'>").append("<![CDATA[").append(noArt).append("]]>").append("</no_art>\r\n");
						valueXmlString.append("<batch_no isSrvCallOnChg='0'>").append("<![CDATA[").append( ( (rs.getString(13) == null) ? "":rs.getString(13) ) ).append("]]>").append("</batch_no>\r\n");
						valueXmlString.append("<grade isSrvCallOnChg='0'>").append("<![CDATA[").append( ( (rs.getString(14) == null) ? "": rs.getString(14) ) ).append("]]>").append("</grade>\r\n");

						valueXmlString.append("<quantity isSrvCallOnChg='1'>").append("<![CDATA[").append(minputQty).append("]]>").append("</quantity>\r\n");
						valueXmlString.append("</Detail>\r\n");
						noArt1 = 0;
						grossWt = 0;
						tareWt = 0;
						netWt = 0;
						cnt++;
						//System.out.println("The cnt :" + ++cnt);
						//Added By Pavan R on 29MAR2018[START][iss_criteria in item master is W. then system should not allow to issue partial quantity.]
						/*if (issCriteria != null && ("W").equalsIgnoreCase(issCriteria))
						{
							if (stockQty >=  remQuantity)
							{
								if(minputQty > 0)
								{
									System.out.println("break executed...");
									break;
								}
							}
						}*///Added by Pavan R on 29MAR2018[END]
					}//while end
					rs.close();	rs = null;
					pstmt.close(); pstmt = null;
					//changed by msalam on 22/09/07 start
					//if no record found then promp error
					System.out.println("manohar 08/08/11 isRecordFound [" + isRecordFound + "] partQty [" + partQty + "]" );
					if ( !isRecordFound  && "X".equalsIgnoreCase(partQty))
					{
						errCode = "VTDIST16";
						errString = itmDBAccessEJB.getErrorString("",errCode,"","",conn1);
						return errString;
					}

					//changed by msalam on 22/09/07 end 

					if (remQuantity > 0 && "X".equalsIgnoreCase(partQty))
					{
						//if (partQty.equalsIgnoreCase("W"))
						//{
						errCode = "VTSTKW";
						errString = itmDBAccessEJB.getErrorString("",errCode,"","",conn1);
						return errString;
						//}
					}
				}//end if
				else   // if Lot No is entered by user
				{
					double quantity1 = 0d;
					quantity1 = Double.parseDouble(quantity);
					//System.out.println("quantity1 :"+quantity1);
					if (Double.parseDouble(rate1) <= 0)
					{
						errCode = "VTRATE1";
						errString = itmDBAccessEJB.getErrorString("",errCode,"","",conn1);
						return errString;
					}
					//if (checkIntegralQty.equals("Y"))//Commented by jiten 04/10/06
					if (!checkIntegralQty.equals("N"))
					{
						integralQty = getIntegralQty(siteCode, itemCode, lotNo, packCode, checkIntegralQty);
						//System.out.println("integralQty :"+integralQty);
						if (integralQty <= 0)
						{
							errCode = "VINTGRLQTY"; 
							errString = itmDBAccessEJB.getErrorString("",errCode,"","",conn1);
							return errString;
						}
						mod = (quantity1 % integralQty);
						//System.out.println("mod :"+mod);
						quantity1 = quantity1 - mod;
						//System.out.println("quantity1 :"+quantity1);						
					}
					//sql1 = "SELECT COUNT(A.ITEM_CODE) " 
					/* -- Commented And changes Below By gulzar 01/03/07 as changes done by Prajakta in PB code.
					sql1 = "SELECT COUNT(*) " 
						 +"FROM STOCK A, INVSTAT B, LOCATION C "
						 +"WHERE A.LOC_CODE = C.LOC_CODE "
						 +"AND B.INV_STAT  = C.INV_STAT "
						 +"AND A.ITEM_CODE = '"+itemCode+"' "
						 +"AND A.SITE_CODE = '"+siteCode+"' "
						 +"AND A.LOC_CODE  = '"+locCode+"' "
						 +"AND A.LOT_NO    = '"+lotNoDist+"' "
						 +"AND A.LOT_SL	  = '"+lotSLDist+"' "
						 +"AND A.QUANTITY - A.ALLOC_QTY >= "+quantity1 
						 +" AND B.AVAILABLE = '"+availableYn+"' "
						 +"AND B.USABLE = '"+availableYn+"' "
						 +"AND NOT EXISTS (SELECT 1 FROM INV_RESTR I "
						 +"WHERE I.INV_STAT = B.INV_STAT AND I.REF_SER = 'D-ISS' )";
					 *///End Comment Gulzar 01/03/05
					//Added By Gulzar 01/03/07  as changes done by Prajakta in PB code.
					sql1 = "SELECT COUNT(*) " 
							+"FROM STOCK A, INVSTAT B, LOCATION C "
							+"WHERE A.LOC_CODE = C.LOC_CODE "
							+"AND B.INV_STAT  = C.INV_STAT "
							+"AND A.ITEM_CODE = '"+itemCode+"' "
							+"AND A.SITE_CODE = '"+siteCode+"' "
							+"AND A.LOC_CODE  = '"+locCode+"' "
							+"AND A.LOT_NO    = '"+lotNoDist+"' "
							+"AND A.LOT_SL	  = '"+lotSLDist+"' "
							+"AND A.QUANTITY - A.ALLOC_QTY >= "+quantity1 
							+" AND B.AVAILABLE = '" + availableYn + "' "
							+" AND B.STAT_TYPE = 'M' "
							+"AND B.USABLE = '"+ availableYn + "' " ;
					if (availableYn.equals("Y"))
					{
						sql1 = sql1 + "AND NOT EXISTS (SELECT 1 FROM INV_RESTR I WHERE I.INV_STAT = B.INV_STAT AND I.REF_SER = 'D-ISS') ";
					}
					if (locCodeDamaged != null && locCodeDamaged.trim().length() > 0)
					{
						sql1 = sql1 + "AND A.LOC_CODE IN ("+locCodeDamaged+")";
					}
					//End Add Gulzar 01/03/07
					//System.out.println("sql1 :"+sql1);
					//rs1 = stmt1.executeQuery(sql1);
					pstmt1 = conn1.prepareStatement(sql1);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						count = rs1.getInt(1);
						//System.out.println("count :"+count);
					}
					rs1.close(); rs1 = null;
					pstmt1.close(); pstmt1 = null;
					System.out.println("manohar 08/08/11 count [" + count + "] partQty [" + partQty + "]" );
					if (count == 0 && "X".equalsIgnoreCase(partQty))
					{
						errCode = "VTDIST16"; 
						errString = itmDBAccessEJB.getErrorString("",errCode,"","",conn1);
						return errString;
					}
					CommonConstants.setIBASEHOME();
					System.out.println("CommonConstants.DB_NAME :"+CommonConstants.DB_NAME);
					if (CommonConstants.DB_NAME.equalsIgnoreCase("db2"))
					{
						sql1 = "SELECT A.EXP_DATE, "
								+"A.SITE_CODE__MFG, "
								+"A.MFG_DATE, "
								+"A.POTENCY_PERC, "
								+"A.PACK_CODE, "
								+"A.RATE "
								+"FROM STOCK A, INVSTAT B "
								+"WHERE A.INV_STAT  = B.INV_STAT "
								+"AND A.ITEM_CODE = '"+itemCode+"' "
								+"AND A.SITE_CODE = '"+siteCode+"' "
								+"AND A.LOC_CODE  = '"+locCode+"' "
								+"AND A.LOT_NO    = '"+lotNoDist+"' "
								+"AND A.LOT_SL	 = '"+lotSLDist+"' " 
								+"AND B.AVAILABLE = '"+availableYn+"' " 
								+"AND B.USABLE    = '"+availableYn+"' "//FOR UPDATE  ommited as giving error -  For Update clause is not allowed here bcuz not updatable cursor";
								+" AND B.STAT_TYPE = 'M' "
								+"AND A.LOC_CODE IN ("+locCodeDamaged+")"; //Gulzar 01/03/07 as changes done by Prajakta in PB code.
						//System.out.println("sql1 :"+sql1);
						//rs1 = stmt1.executeQuery(sql1);
						pstmt1 = conn1.prepareStatement(sql1);
						rs1 = pstmt1.executeQuery();
						if (rs1.next())
						{
							expDate = rs1.getDate(1);
							//System.out.println("expDate :"+expDate);
							siteCodeMfg = rs1.getString(2);
							//System.out.println("siteCodeMfg :"+siteCodeMfg);
							mfgDate = rs1.getDate(3);
							//System.out.println("mfgDate :"+mfgDate);
							potencyPerc = rs1.getString(4);
							//System.out.println("potencyPerc :"+potencyPerc);
							packCode = rs1.getString(5);
							//System.out.println("packCode :"+packCode);
							rate2 = rs1.getDouble(6);
							//System.out.println("rate2 :"+rate2);
						}
						rs1.close(); rs1 = null;
						pstmt1.close(); pstmt1 = null;
					}
					else if (CommonConstants.DB_NAME.equalsIgnoreCase("mssql"))
					{
						sql1 = "SELECT A.EXP_DATE, "
								+"A.SITE_CODE__MFG, "
								+"A.MFG_DATE, " 
								+"A.POTENCY_PERC, "
								+"A.PACK_CODE, "
								+"A.RATE "
								+"FROM STOCK A (UPDLOCK), INVSTAT B	"
								+"WHERE A.INV_STAT  = B.INV_STAT "
								+"AND A.ITEM_CODE = '"+itemCode+"' "
								+"AND A.SITE_CODE = '"+siteCode+"' "
								+"AND A.LOC_CODE  = '"+locCode+"' "
								+"AND A.LOT_NO    = '"+lotNoDist+"' "
								+"AND A.LOT_SL	 = '"+lotSLDist+"' " 
								+"AND B.AVAILABLE = '"+availableYn+"' " 
								+"AND B.USABLE    = '"+availableYn+"' "
								+" AND B.STAT_TYPE = 'M' "
								+"AND A.LOC_CODE IN ("+locCodeDamaged+")"; //Gulzar 01/03/07 as changes done by Prajakta in PB code.
						//System.out.println("sql1 :"+sql1);
						//rs1 = stmt1.executeQuery(sql1);
						pstmt1 = conn1.prepareStatement(sql1);
						rs1 = pstmt1.executeQuery();
						if (rs1.next())
						{
							expDate = rs1.getDate(1);
							//System.out.println("expDate :"+expDate);
							siteCodeMfg = rs1.getString(2);
							//System.out.println("siteCodeMfg :"+siteCodeMfg);
							mfgDate = rs1.getDate(3);
							//System.out.println("mfgDate :"+mfgDate);
							potencyPerc = rs1.getString(4);
							//System.out.println("potencyPerc :"+potencyPerc);
							packCode = rs1.getString(5);
							//System.out.println("packCode :"+packCode);
							rate2 = rs1.getDouble(6);
							//System.out.println("rate2  :"+rate2);
						}
						rs1.close(); rs1 = null;
						pstmt1.close(); pstmt1 = null;
					}
					else
					{
						sql1 = "SELECT A.EXP_DATE, "
								+"A.SITE_CODE__MFG, "
								+"A.MFG_DATE, "
								+"A.POTENCY_PERC, "
								+"A.PACK_CODE, "
								+"A.RATE "
								+"FROM STOCK A, INVSTAT B "
								+"WHERE A.INV_STAT  = B.INV_STAT "
								+"AND A.ITEM_CODE = '"+itemCode+"' "
								+"AND A.SITE_CODE = '"+siteCode+"' "
								+"AND A.LOC_CODE  = '"+locCode+"' "
								+"AND A.LOT_NO    = '"+lotNoDist+"' "
								+"AND A.LOT_SL	 = '"+lotSLDist+"' " 
								+"AND B.AVAILABLE = '"+availableYn+"' " 
								+"AND B.USABLE = '"+availableYn+"' "  //+" FOR UPDATE NOWAIT"; // commented becz out of sequence error occurs
								+"AND A.LOC_CODE IN ('"+locCodeDamaged+"')"; //Gulzar 01/03/07 as changes done by Prajakta in PB code.
						//System.out.println("sql1 :"+sql1);
						//rs1 = stmt1.executeQuery(sql1);
						pstmt1 = conn1.prepareStatement(sql1);
						rs1 = pstmt1.executeQuery();
						if (rs1.next())
						{
							expDate = rs1.getDate(1);
							//System.out.println("expDate :"+expDate);
							siteCodeMfg = rs1.getString(2);
							//System.out.println("siteCodeMfg :"+siteCodeMfg);
							mfgDate = rs1.getDate(3);
							//System.out.println("mfgDate :"+mfgDate);
							potencyPerc = rs1.getString(4);
							//System.out.println("potencyPerc :"+potencyPerc);
							packCode = rs1.getString(5);
							//System.out.println("packCode :"+packCode);
							rate2 = rs1.getDouble(6);
							//System.out.println("rate2 :"+rate2);
						}
						rs1.close(); rs1 = null;
						pstmt1.close(); pstmt1 = null;
					}
					java.util.Date expDate1 = null;
					if (minShelfLife > 0)
					{
						chkDate = calcExpiry(tranDate, minShelfLife);
						//System.out.println("chkDate :"+chkDate);
						chkDate1 = sdf.parse(chkDate);
						//System.out.println("chkDate1 :"+chkDate1);
						expDate1 = new java.util.Date(expDate.getTime());
						//System.out.println("chkDate1 :"+chkDate1+" expDate1:"+expDate1);

						if ((chkDate1.compareTo(expDate1) > 0))
						{
							errCode = "VTSHELF01"; 
							errString = itmDBAccessEJB.getErrorString("",errCode,"","",conn1);
							return errString;
						} 
					}
					//changed by msalam on 21/09/07 start
					//TRACK_SHELF_LIFE, SUPP_SOUR were brought in previous query 
					//so as to bring all field in singel query
					/*
					sql1 = "SELECT CASE WHEN TRACK_SHELF_LIFE IS NULL THEN 'N' ELSE TRACK_SHELF_LIFE END, " 
							+"CASE WHEN SUPP_SOUR IS NULL THEN 'M' ELSE SUPP_SOUR END "
							+"FROM ITEM "
							+"WHERE ITEM_CODE = '"+itemCode+"' ";
					System.out.println("sql1 :"+sql1);
					rs1 = stmt1.executeQuery(sql1);
					if (rs1.next())
					{
						tracShelfLife = rs1.getString(1);
						System.out.println("tracShelfLife :"+tracShelfLife);
						suppSour = rs1.getString(2);
						System.out.println("suppSour :"+suppSour);
					}
					 */
					//changed by msalam on 21/09/07 end 

					if ((siteCodeMfg == null || siteCodeMfg.equals("") || siteCodeMfg.trim().length() == 0) && (suppSour.equals("M")))
					{
						errCode = "VTSHELF01"; 
						errString = itmDBAccessEJB.getErrorString("",errCode,"","",conn1);
						return errString;
					}
					if ((mfgDate == null) && (tracShelfLife.equals("Y")))
					{
						errCode = "VTMFGDATE3"; 
						errString = itmDBAccessEJB.getErrorString("",errCode,"","",conn1);
						return errString;
					}
					if ((expDate == null) && (tracShelfLife.equals("Y")))
					{
						errCode = "VTEXPDATE1"; 
						errString = itmDBAccessEJB.getErrorString("",errCode,"","",conn1);
						return errString;
					}
					disAmount = disAmount * (Double.parseDouble(disCountPer) / 100);
					//System.out.println("disAmount :"+disAmount);
					valueXmlString.append("<Detail>\r\n");
					valueXmlString.append("<tran_id isSrvCallOnChg='0'>").append("<![CDATA[").append(tranId).append("]]>").append("</tran_id>\r\n");
					valueXmlString.append("<dist_order isSrvCallOnChg='0'>").append("<![CDATA[").append(distOrder).append("]]>").append("</dist_order>\r\n");
					//valueXmlString.append("<line_no_ord isSrvCallOnChg='0'>").append("<![CDATA[").append(lineNoOrd).append("]]>").append("</line_no_ord>\r\n");
					//Temporarily commented and changed - jiten 20/05/06
					//valueXmlString.append("<line_no_dist_order isSrvCallOnChg='0'>").append("<![CDATA[").append(lineNoOrd).append("]]>").append("</line_no_dist_order>\r\n");

					valueXmlString.append("<line_no_dist_order isSrvCallOnChg='1'>").append("<![CDATA[").append(lineNoOrd).append("]]>").append("</line_no_dist_order>\r\n");
					//jiten 					
					valueXmlString.append("<item_code isSrvCallOnChg='0'>").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code>\r\n");
					valueXmlString.append("<item_descr isSrvCallOnChg='0'>").append("<![CDATA[").append(itemDescr).append("]]>").append("</item_descr>\r\n");
					valueXmlString.append("<location_descr isSrvCallOnChg='0'>").append("<![CDATA[").append(locDescr).append("]]>").append("</location_descr>\r\n");
					valueXmlString.append("<unit isSrvCallOnChg='0'>").append("<![CDATA[").append(unit).append("]]>").append("</unit>\r\n");
					valueXmlString.append("<unit__alt isSrvCallOnChg='0'>").append("<![CDATA[").append(unitAlt).append("]]>").append("</unit__alt>\r\n");
					valueXmlString.append("<conv__qty__alt isSrvCallOnChg='0'>").append("<![CDATA[").append(convQtyAlt).append("]]>").append("</conv__qty__alt>\r\n");
					//below line commented as it will get affected by itemchange of quantity - jiten -20/05/06
					//valueXmlString.append("<qty_order__alt isSrvCallOnChg='0'>").append("<![CDATA[").append(qtyOrdAlt).append("]]>").append("</qty_order__alt>\r\n");
					//this line taken next to lot no by msalam on 041108
					//valueXmlString.append("<quantity isSrvCallOnChg='1'>").append("<![CDATA[").append(quantity1).append("]]>").append("</quantity>\r\n");					
					// 22/05/10 manoharan
					if (lotSLDist == null || "null".equals(lotSLDist) || lotSLDist.trim().length() == 0 )
					{
						lotSLDist = "     ";
					}

					if (lotNoDist == null || "null".equals(lotNoDist) || lotNoDist.trim().length() == 0 )
					{
						lotNoDist = "               ";
					}
					// end 22/05/10 manoharan
					/*	valueXmlString.append("<quantity isSrvCallOnChg='1'>").append("<![CDATA[").append(quantity1).append("]]>").append("</quantity>\r\n");	 */				
					valueXmlString.append("<loc_code isSrvCallOnChg='0'>").append("<![CDATA[").append(locCode).append("]]>").append("</loc_code>\r\n");
					valueXmlString.append("<lot_sl isSrvCallOnChg='0'>").append("<![CDATA[").append(lotSLDist).append("]]>").append("</lot_sl>\r\n");
					valueXmlString.append("<pack_code isSrvCallOnChg='0'>").append("<![CDATA[").append((packCode == null) ? "":packCode.trim()).append("]]>").append("</pack_code>\r\n");
					valueXmlString.append("<rate isSrvCallOnChg='0'>").append("<![CDATA[").append(rate1).append("]]>").append("</rate>\r\n");
					//next line commented by msalam on 031108 as it gets updated from item change on lot no
					//valueXmlString.append("<rate__clg isSrvCallOnChg='0'>").append("<![CDATA[").append(rateClg).append("]]>").append("</rate__clg>\r\n");
					valueXmlString.append("<disc_amt isSrvCallOnChg='0'>").append("<![CDATA[").append(disAmount).append("]]>").append("</disc_amt>\r\n");
					valueXmlString.append("<tax_class isSrvCallOnChg='0'>").append("<![CDATA[").append(taxClass).append("]]>").append("</tax_class>\r\n");
					valueXmlString.append("<tax_chap isSrvCallOnChg='0'>").append("<![CDATA[").append(taxChap).append("]]>").append("</tax_chap>\r\n");
					valueXmlString.append("<tax_env isSrvCallOnChg='0'>").append("<![CDATA[").append(taxEnv).append("]]>").append("</tax_env>\r\n");
					valueXmlString.append("<gross_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(grossWeight).append("]]>").append("</gross_weight>\r\n");
					valueXmlString.append("<net_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(netWeight).append("]]>").append("</net_weight>\r\n");
					valueXmlString.append("<tare_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(tareWeight).append("]]>").append("</tare_weight>\r\n");
					valueXmlString.append("<amount isSrvCallOnChg='0'>").append("<![CDATA[").append(quantity1*Double.parseDouble(rate1)).append("]]>").append("</amount>\r\n");						
					/*	valueXmlString.append("<lot_no isSrvCallOnChg='1'>").append("<![CDATA[").append(lotNoDist).append("]]>").append("</lot_no>\r\n"); */

					valueXmlString.append("<pack_instr isSrvCallOnChg='1'>").append("<![CDATA[").append((packInstr == null) ? "":packInstr).append("]]>").append("</pack_instr>\r\n");
					valueXmlString.append("<lot_no isSrvCallOnChg='1'>").append("<![CDATA[").append(lotNoDist).append("]]>").append("</lot_no>\r\n");
					valueXmlString.append("<quantity isSrvCallOnChg='1'>").append("<![CDATA[").append(quantity1).append("]]>").append("</quantity>\r\n");					
					valueXmlString.append("<site_code__mfg isSrvCallOnChg='0'>").append("<![CDATA[").append((siteCodeMfg == null) ? "":siteCodeMfg.trim()).append("]]>").append("</site_code__mfg>\r\n");
					valueXmlString.append("<exp_date isSrvCallOnChg='0'>").append("<![CDATA[").append((expDate1 == null) ? "":sdf.format(expDate1)).append("]]>").append("</exp_date>\r\n");
					valueXmlString.append("<mfg_date isSrvCallOnChg='0'>").append("<![CDATA[").append((mfgDate == null) ? "":sdf.format(mfgDate)).append("]]>").append("</mfg_date>\r\n");
					valueXmlString.append("</Detail>\r\n");
				}//else end
				//System.out.println("The count :"+cnt);				
			}//end if(detailCnt)
			valueXmlString.append("</Root>\r\n");
			//conn1.close();	
		}//try end
		catch(Exception e)
		{
			//System.out.println("Excepton occurs in DistIssueActEJB Allocate :: " +e);
			e.printStackTrace();
		}
		finally 
		{
			try
			{
				if(rs != null) {
					rs.close(); rs = null;
				}
				if(pstmt != null)	{
					pstmt.close(); pstmt = null;
				}
				if(rs1 != null) {
					rs1.close(); rs1 = null;
				}					
				if(pstmt1 != null)	{
					pstmt1.close(); pstmt1 = null;
				}
				if(rs2 != null) {
					rs2.close(); rs2 = null;
				}					
				if(pstmt2 != null)	{
					pstmt2.close(); pstmt2 = null;
				}
				if(conn1 != null)
				{
					conn1.close();
					conn1 = null;
				}
			}
			catch (Exception e){}
		}
		System.out.println("valueXmlString from DistIssueActEJB Allocate ::"+valueXmlString);
		return valueXmlString.toString();
	}

	private String actionDefault(String xmlString,String xmlString1, String objContext,String xtraParams) throws RemoteException, ITMException
	{
		System.out.println("xmlString from DistIssueActEJB :"+xmlString+" \n xmlString1 :"+xmlString1);
		String sql = "", distOrder = "", locCode = "", itemChngXmlString = "", returnValue = "", childNodeName = "", childNodeName1 = "";
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root>");
		StringBuffer retStrFrAllocate  = new StringBuffer("<?xml version=\"1.0\"?><Root>");
		String finalStr = "";

		ResultSet rs = null;
		Connection conn = null;
		//Statement stmt = null;
		PreparedStatement pstmtS = null;
		PreparedStatement pstmt = null;	 //added by Jiten on 20/03/06 for change in sql
		Document dom = null, dom1 = null, domItmChng = null;
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		//ConnDriver connDriver = new ConnDriver();
		Node parentNode = null, parentNode1 = null, childNode = null , childNode1 = null;
		NodeList parentNodeList = null, childNodeList = null, parentNodeList1 = null, childNodeList1 = null;
		int parentNodeListLen = 0, childNodeListLen = 0, parentNodeListLen1 = 0, childNodeListLen1 = 0;

		String cItemCode = null;
		double cQtyConfirm = 0.0;
		double cQtyShipped = 0.0;
		String cTaxClass = null;
		String cTaxChap = null;
		String cTaxEnv = null;
		double cRate = 0.0;
		double cDiscount = 0.0;
		double cRateClg = 0.0;
		String cUnit = null;
		String cUnitAlt = null;
		String cPackInstr = null;
		double cConvQtyAlt = 0.0;
		double quantityNoArt = 0.0;
		String siteCode = null;
		String custCode = null;

		System.out.println("Action Default Entry time :: " );
		String retString = "";
		try
		{
			if(xmlString != null && xmlString.trim().length() > 0)
			{
				dom = genericUtility.parseString(xmlString); 			
			}
			else 
			{
				System.out.println("The xmlString found null");
			}
			if(xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom1 = genericUtility.parseString(xmlString1);			
			}
			else
			{
				System.out.println("The xmlString1 found null");
			}
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			//stmt = conn.createStatement();
			//distOrder = genericUtility.getColumnValue("dist_order",dom1);
			//COMMENTED ABOVE AND ADDED BELOW LINE FOR REQ ID DI89MAN022 //mukesh 24 sept 08
			distOrder = genericUtility.getColumnValue("dist_order",dom);
			System.out.println("distOrder :"+distOrder);
			if (dom != null )
			{
				locCode = genericUtility.getColumnValue("loc_code",dom);
			}
			if (locCode == null)
			{
				locCode = "";
			}
			System.out.println("locCode :"+locCode);
			//Changes done by Jiten on 20/03/06	as code changes in PB takes tran date in sql
			String tranDate = genericUtility.getColumnValue("tran_date",dom1);
			//sql = "SELECT LINE_NO FROM DISTORDER_DET WHERE DIST_ORDER = '"+distOrder+"' AND DUE_DATE <= ? ";//Commented by Jiten 04/10/06 as sql changed in PB
			//changed by msalam on 22/09/07 start
			//so as to remove item change call and bring all data coming in item change in next query only
			/*
			sql = "SELECT LINE_NO FROM DISTORDER_DET WHERE DIST_ORDER = '" + distOrder + "'" +
				 " AND (CASE WHEN SHIP_DATE IS NULL THEN DUE_DATE ELSE SHIP_DATE END) <= ? " +
				 " AND (CASE WHEN QTY_CONFIRM IS NULL THEN 0 ELSE QTY_CONFIRM END - CASE WHEN QTY_SHIPPED IS NULL THEN 0 ELSE QTY_SHIPPED END ) > 0 "; //Gulzar 01/03/07 as changes done by Gautam in PB code.
			 */

			String itemDescription = null;
			String packCode = null;
			String tranType = null;
			String tranTypeParent = null;

			//Statement stmtS = conn.createStatement();			
			ResultSet rsS = null;
			tranType = genericUtility.getColumnValue("tran_type",dom1);
			/*	sql = 	" SELECT dtl.LINE_NO, dtl.ITEM_CODE, "
				+"		 ( ( CASE WHEN dtl.QTY_CONFIRM IS NULL THEN 0 ELSE dtl.QTY_CONFIRM END ) - "
				+"		     ( CASE WHEN dtl.QTY_SHIPPED IS NULL THEN 0 ELSE dtl.QTY_SHIPPED END ) ), "
				+"		 ( ( CASE WHEN dtl.QTY_SHIPPED IS NULL THEN 0 ELSE dtl.QTY_SHIPPED END ) - "
				+"		     ( CASE WHEN dtl.QTY_RETURN IS NULL THEN 0 ELSE dtl.QTY_RETURN END ) ), "
				+"		 ( CASE WHEN dtl.TAX_CLASS IS NULL THEN '' ELSE dtl.TAX_CLASS END ), "
				+"		 ( CASE WHEN dtl.TAX_CHAP IS NULL THEN '' ELSE dtl.TAX_CHAP END ), "
				+"		 ( CASE WHEN dtl.TAX_ENV IS NULL THEN '' ELSE dtl.TAX_ENV END ), "
				+"		 ( CASE WHEN dtl.RATE IS NULL THEN 0 ELSE dtl.RATE END ), "
				+"		 ( CASE WHEN dtl.DISCOUNT IS NULL THEN 0 ELSE dtl.DISCOUNT END ), "
				+"		 dtl.RATE__CLG, dtl.UNIT, dtl.UNIT__ALT, dtl.PACK_INSTR, dtl.CONV__QTY__ALT, "
				+"		 m.DESCR, m.PACK_CODE, hdr.site_code, hdr.SUNDRY_CODE cust_code "
				+"	FROM DISTORDER hdr, DISTORDER_DET dtl, ITEM m "
				+" WHERE dtl.DIST_ORDER = '" + distOrder + "'"
				+"	and hdr.DIST_ORDER = dtl.DIST_ORDER "
				+"	and dtl.item_code = m.item_code(+) "
				+"	AND ( CASE WHEN dtl.SHIP_DATE IS NULL THEN dtl.DUE_DATE ELSE dtl.SHIP_DATE END ) <= ? "
				+"	AND (( CASE WHEN dtl.QTY_CONFIRM IS NULL THEN 0 ELSE dtl.QTY_CONFIRM END ) - "
				+"		  ( CASE WHEN dtl.QTY_SHIPPED IS NULL THEN 0 ELSE dtl.QTY_SHIPPED END) ) > 0 ";  */

			sql = 	" SELECT dtl.LINE_NO, dtl.ITEM_CODE, "
					+"		 ( ( CASE WHEN dtl.QTY_CONFIRM IS NULL THEN 0 ELSE dtl.QTY_CONFIRM END ) - "
					+"		     ( CASE WHEN dtl.QTY_SHIPPED IS NULL THEN 0 ELSE dtl.QTY_SHIPPED END ) ), "
					+"		 ( ( CASE WHEN dtl.QTY_SHIPPED IS NULL THEN 0 ELSE dtl.QTY_SHIPPED END ) - "
					+"		     ( CASE WHEN dtl.QTY_RETURN IS NULL THEN 0 ELSE dtl.QTY_RETURN END ) ), "
					+"		 ( CASE WHEN dtl.TAX_CLASS IS NULL THEN '' ELSE dtl.TAX_CLASS END ), "
					+"		 ( CASE WHEN dtl.TAX_CHAP IS NULL THEN '' ELSE dtl.TAX_CHAP END ), "
					+"		 ( CASE WHEN dtl.TAX_ENV IS NULL THEN '' ELSE dtl.TAX_ENV END ), "
					+"		 ( CASE WHEN dtl.RATE IS NULL THEN 0 ELSE dtl.RATE END ), "
					+"		 ( CASE WHEN dtl.DISCOUNT IS NULL THEN 0 ELSE dtl.DISCOUNT END ), "
					+"		 dtl.RATE__CLG, dtl.UNIT, dtl.UNIT__ALT, dtl.PACK_INSTR, dtl.CONV__QTY__ALT, "
					+"		 m.DESCR, m.PACK_CODE, hdr.site_code, hdr.SUNDRY_CODE cust_code "
					+"	FROM  DISTORDER_DET dtl left outer join DISTORDER hdr   on hdr.DIST_ORDER = dtl.DIST_ORDER  "
					+"	left outer join ITEM m on  dtl.item_code = m.item_code   "
					+"  WHERE dtl.DIST_ORDER = '" + distOrder + "'"
					+"	AND ( CASE WHEN dtl.SHIP_DATE IS NULL THEN dtl.DUE_DATE ELSE dtl.SHIP_DATE END ) <= ? "
					+"	AND (( CASE WHEN dtl.QTY_CONFIRM IS NULL THEN 0 ELSE dtl.QTY_CONFIRM END ) - "
					+"		  ( CASE WHEN dtl.QTY_SHIPPED IS NULL THEN 0 ELSE dtl.QTY_SHIPPED END) ) > 0 ";
			//System.out.println("sql :"+sql);
			pstmt = conn.prepareStatement(sql);
			pstmt.setTimestamp(1,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(tranDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
			rs = pstmt.executeQuery();
			//End Changes
			while (rs.next())
			{		
				cItemCode = rs.getString( 2 );
				cQtyConfirm = rs.getDouble( 3 );
				cQtyShipped = rs.getDouble( 4 );
				cTaxClass = rs.getString( 5 );
				cTaxChap = rs.getString( 6 );
				cTaxEnv = rs.getString( 7 );
				cRate = rs.getDouble( 8 );
				cDiscount = rs.getDouble( 9 );
				cRateClg = rs.getDouble( 10 );
				cUnit = rs.getString( 11 );
				cUnitAlt = rs.getString( 12 );
				cPackInstr = rs.getString( 13 );
				cConvQtyAlt = rs.getDouble( 14 );
				itemDescription = rs.getString( "DESCR" );  
				packCode = rs.getString( "PACK_CODE" ); 
				siteCode = rs.getString( "site_code" );
				custCode = rs.getString( "cust_code" );
				//changed by msalam on 020209 for multiple rows values table of last
				StringBuffer itemChngXmlStrBuff = new StringBuffer( "<?xml version=\"1.0\"?><Root><Detail2>" );
				itemChngXmlStrBuff.append("<item_code>").append("<![CDATA[").append(cItemCode).append("]]>").append("</item_code>");
				itemChngXmlStrBuff.append("<unit>").append("<![CDATA[").append(cUnit).append("]]>").append("</unit>");
				itemChngXmlStrBuff.append("<unit__alt>").append("<![CDATA[").append(cUnitAlt).append("]]>").append("</unit__alt>");
				itemChngXmlStrBuff.append("<pack_instr>").append("<![CDATA[").append(cPackInstr).append("]]>").append("</pack_instr>"); //Gulzar 01/03/07 as added by Fatima in in itemchange
				itemChngXmlStrBuff.append("<conv__qty__alt>").append("<![CDATA[").append(cConvQtyAlt).append("]]>").append("</conv__qty__alt>"); //Gulzar 24/03/07 it is not set in default button
				valueXmlString.append("<item_descr>").append("<![CDATA[").append( ( itemDescription == null ? "" : itemDescription ) ).append("]]>").append("</item_descr>");
				// 15/04/10 manoharan
				valueXmlString.append("<pack_code>").append("<![CDATA[").append( ( packCode == null ? "" : packCode ) ).append("]]>").append("</pack_code>");
				// end 15/04/10 manoharan
				sql = "SELECT TRAN_TYPE__PARENT FROM DISTORDER_TYPE WHERE TRAN_TYPE = ?";//'"+tranType+"'";
				//System.out.println("sql :"+sql);
				//rsS = stmtS.executeQuery(sql);
				pstmtS = conn.prepareStatement(sql);
				pstmtS.setString(1, tranType);
				rsS = pstmtS.executeQuery();
				if ( rsS.next() )
				{
					tranTypeParent = rsS.getString( "TRAN_TYPE__PARENT" );
					//System.out.println("tranTypeParent :"+tranTypeParent);
				}
				rsS.close();
				rsS = null;
				pstmtS.close(); pstmtS = null;
				if ( !tranType.equals( tranTypeParent ) )
				{
					quantityNoArt = cQtyShipped;
					itemChngXmlStrBuff.append("<quantity>").append("<![CDATA[").append(cQtyShipped).append("]]>").append("</quantity>");
					itemChngXmlStrBuff.append("<qty_order__alt>").append("<![CDATA[").append(cQtyShipped).append("]]>").append("</qty_order__alt>");
				}
				else
				{
					quantityNoArt = cQtyConfirm;
					itemChngXmlStrBuff.append("<quantity>").append("<![CDATA[").append(cQtyConfirm).append("]]>").append("</quantity>");
					itemChngXmlStrBuff.append("<qty_order__alt>").append("<![CDATA[").append(cQtyConfirm).append("]]>").append("</qty_order__alt>");
				}

				itemChngXmlStrBuff.append("<tax_class>").append("<![CDATA[").append((cTaxClass == null) ? "":cTaxClass).append("]]>").append("</tax_class>");
				itemChngXmlStrBuff.append("<tax_chap>").append("<![CDATA[").append((cTaxChap == null) ? "":cTaxChap).append("]]>").append("</tax_chap>");
				itemChngXmlStrBuff.append("<tax_env>").append("<![CDATA[").append((cTaxEnv == null) ? "":cTaxEnv).append("]]>").append("</tax_env>");

				itemChngXmlStrBuff.append("<rate>").append("<![CDATA[").append(cRate).append("]]>").append("</rate>");
				itemChngXmlStrBuff.append("<rate__clg>").append("<![CDATA[").append(cRateClg).append("]]>").append("</rate__clg>");
				itemChngXmlStrBuff.append("<discount>").append("<![CDATA[").append(cDiscount).append("]]>").append("</discount>");

				int noOfArt = 0;
				noOfArt = ( new DistCommon() ).getNoArt( siteCode, custCode, cItemCode, packCode, quantityNoArt , 'S', 0.0, 0.0, conn );
				System.out.println( "noOfArt :: " + noOfArt );
				itemChngXmlStrBuff.append("<no_art>").append("<![CDATA[").append(noOfArt).append("]]>").append("</no_art>");
				itemChngXmlStrBuff.append("</Detail2>");
				itemChngXmlStrBuff.append("</Root>");	

				//end alam 020209
				//if(valueXmlString.length() == 0)
				//{
				//	valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root>");
				//}
				valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root>");
				valueXmlString.append("<Detail>");
				//valueXmlString.append("<line_no_ord>").append("<![CDATA[").append(rs.getString(1)).append("]]>").append("</line_no_ord>");
				valueXmlString.append("<line_no_dist_order>").append("<![CDATA[").append(rs.getString(1)).append("]]>").append("</line_no_dist_order>");
				if (locCode.trim().length() > 0)
				{
					valueXmlString.append("<loc_code>").append("<![CDATA[").append(locCode).append("]]>").append("</loc_code>");
				}
				valueXmlString.append("</Detail>");
				valueXmlString.append("</Root>");
				//System.out.println("valueXmlString :"+valueXmlString.toString());
				Document domLineNoDist = genericUtility.parseString(valueXmlString.toString());			
				parentNodeList = domLineNoDist.getElementsByTagName("Detail");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNode1 = childNodeList.item(0);
				//System.out.println("childNode1 of valueXmlString :"+childNode1);

				parentNodeList1 = dom.getElementsByTagName("Detail2");
				//System.out.println("parentNodeList :"+parentNodeList);
				parentNodeListLen = parentNodeList1.getLength();
				//System.out.println("parentNodeListLen :"+parentNodeListLen);
				if( parentNodeListLen > 0)
				{
					for (int ctr = 0; ctr < parentNodeListLen; ctr++)
					{
						parentNode = parentNodeList1.item(ctr);
						childNodeList = parentNode.getChildNodes();
						//System.out.println("childNodeList :"+childNodeList);
						childNodeListLen = childNodeList.getLength();
						//System.out.println("childNodeListLen :"+childNodeListLen);
						for (int i = 0; i < childNodeListLen; i++)
						{
							childNode = childNodeList.item(i);
							//System.out.println("childNode :"+childNode);
							//childNode.setNodeValue(rs.getString(1)); 
							childNodeName = childNode.getNodeName();
							//System.out.println("childNodeName :"+childNodeName);
							childNodeName = childNode.getNodeName();
							//if (childNodeName.equals("line_no_ord"))
							if (childNodeName.equals("line_no_dist_order"))
							{
								Node updatedNode = dom.importNode(childNode1, true);
								//System.out.println("updatedNode :"+updatedNode);
								//								(dom.getElementsByTagName("Detail2").item(0)).replaceChild(updatedNode, parentNodeList.item(ctr));
								//System.out.println("update Node :"+updatedNode+" \n old Child Node :"+childNode+" \n childNode1 :"+childNode);
								//System.out.println("Before Replacing the dom :"+dom.getElementsByTagName("Detail2").item(0));
								(dom.getElementsByTagName("Detail2").item(0)).replaceChild(updatedNode, childNode);
								//System.out.println("After Replacing the dom :"+dom.getElementsByTagName("Detail2").item(0));
							}//end if
						}//end for
					}//end for
				}//end if  

				//changed by msalam on 21/09/07 start
				//ItemChange to be removed as per instruction from Manoharan sir on 20/09/07
				/*
				returnValue = serializeDom(dom);
				System.out.println("returnValue from serializeDom[line_no_dist_order] :"+returnValue);
				System.out.println("DBAccessEJB Created");
				itemChngXmlString = distIssueRemote.itemChanged(returnValue, xmlString1, "", objContext, "line_no_dist_order", "E", xtraParams);
				 */
				itemChngXmlString = itemChngXmlStrBuff.toString();
				//System.out.println("itemChngXmlString :"+itemChngXmlString);
				domItmChng = genericUtility.parseString(itemChngXmlString); 
				returnValue = serializeDom(domItmChng);	
				//System.out.println("\n\nreturnValue from serializeDom :"+returnValue);
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNodeList1 = domItmChng.getElementsByTagName("Detail2");
				//System.out.println("\n parentNodeList :"+parentNodeList+" \n parentNodeList1 :"+parentNodeList);
				parentNodeListLen = parentNodeList.getLength();
				parentNodeListLen1 = parentNodeList1.getLength();
				//System.out.println("\n parentNodeListLen :"+parentNodeListLen+"\n parentNodeListLen1 :"+parentNodeListLen1);
				int count = 0;
				for (int ctr = 0; ctr < parentNodeListLen1; ctr++)
				{
					parentNode1 = parentNodeList1.item(ctr);
					childNodeList1 = parentNode1.getChildNodes();
					//System.out.println("childNodeList1 :"+childNodeList1);
					childNodeListLen1 = childNodeList1.getLength();
					//System.out.println("childNodeListLen1 :"+childNodeListLen1);
					for (int i = 0; i < childNodeListLen1; i++)	 // 10 TAG OF ITEM CHANGE
					{
						childNode1 = childNodeList1.item(i);
						//System.out.println("childNode1 :"+childNode1);
						childNodeName1 = childNode1.getNodeName();
						//System.out.println("childNodeName1 :"+childNodeName1);
						for (int j = 0;j < parentNodeListLen; j++)	//OF DETAIL2  LENGTH  = 1
						{
							parentNode = parentNodeList.item(j);
							childNodeList = parentNode.getChildNodes();
							//System.out.println("childNodeList :"+childNodeList);
							childNodeListLen = childNodeList.getLength();
							//	System.out.println("childNodeListLen :"+childNodeListLen); // 44 
							for (int k = 0; k < childNodeListLen; k++)
							{
								childNode = childNodeList.item(k);
								//System.out.println("childNode :"+childNode);
								childNodeName = childNode.getNodeName();
								//System.out.println("childNodeName :"+childNodeName);
								if (childNodeName1.equals(childNodeName))
								{
									//System.out.println("childNode1 :"+childNode1+"\n childNode :"+childNode);
									Node updatedNode = dom.importNode(childNode1, true);
									//System.out.println("updatedNode :"+updatedNode);
									//System.out.println("Before Replacing the dom :"+dom.getElementsByTagName("Detail2").item(0));
									(dom.getElementsByTagName("Detail2").item(0)).replaceChild(updatedNode, childNode);
									//	System.out.println("After Replacing the dom :"+dom.getElementsByTagName("Detail2").item(0));
								}
							}//k for end
						}//j for end
					}//i for end
				}//ctr for end
				returnValue = serializeDom(dom);
				System.out.println("returnValue from serializeDom[line_no_dist_order] :"+returnValue);
				//Changed by msalam on 21/09/07 end 
				retString = actionAllocate(dom, dom1, objContext, xtraParams);
				//System.out.println("retString from Default :" + retString);
				if(retString.indexOf("<Detail>") != -1)
				{
					finalStr = retString.substring( retString.indexOf( "<Detail>" ), retString.lastIndexOf("</Detail>") + 9 );
					//System.out.println( "finalStr from Default :" + finalStr);
					retStrFrAllocate = retStrFrAllocate.append(String.valueOf(finalStr));
				}
				else
				{
					if (retString.indexOf("<error>") != -1)
					{
						finalStr = retString.substring(retString.indexOf("<error>"),retString.lastIndexOf("</error>")+9);
						//System.out.println("finalStr from Default :"+finalStr);
						//retStrFrAllocate = retStrFrAllocate.append(String.valueOf(finalStr));
					}
					else if (retString.indexOf("<Errors>") != -1)
					{
						finalStr = retString.substring(retString.indexOf("<Errors>"),retString.lastIndexOf("</Errors>")+9);
						//System.out.println("finalStr from Default :"+finalStr);
						retStrFrAllocate = retStrFrAllocate.append(String.valueOf(finalStr));
					}					
				}
				valueXmlString = new StringBuffer();
			}//end while
			//stmtS.close();
			//stmtS = null;
			rs.close(); rs = null;
			pstmt.close(); pstmt = null;
			retStrFrAllocate.append("</Root>");
			System.out.println("retStrFrAllocate :"+retStrFrAllocate);
		}//try end
		catch (SQLException sqx)
		{
			//System.out.println("The SQL Exception occurs in DistIssueEJB(Default) :"+sqx);
			sqx.printStackTrace();
			throw new ITMException(sqx);
		}
		catch (Exception e)
		{
			//System.out.println("The Exception occurs in DistIssueEJB(Default) :"+e);
			e.printStackTrace();
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
		//System.out.println("valueXmlString from distIssue :"+valueXmlString);
		System.out.println("Action Default ExIT time :: \n" + retStrFrAllocate.toString() + "\n**************\n" );
		return retStrFrAllocate.toString();
	}

	// Following are the functions:
	private String serializeDom(Node dom)throws ITMException
	{
		String retString = null;
		try
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Transformer serializer = TransformerFactory.newInstance().newTransformer();
			serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			serializer.transform(new DOMSource(dom), new javax.xml.transform.stream.StreamResult(out));
			retString = out.toString();
			out.flush();
			out.close();
			out = null;	
		}
		catch (Exception e)
		{
			System.out.println("Exception : MasterStatefulEJB : serializeDom :"+e);
			throw new ITMException(e);
		}
		return retString;
	}
	private double pickRate(String priceList, String tranDate, String itemCode, String lotNo, char type)
	{
		String siteCode = "", locCode = "", lotSL = "", priceListParent = "";
		double rate = 0, retValue = 0;
		String sql = "";
		ResultSet rs = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		//ConnDriver connDriver = new ConnDriver();//Gulzar 01/03/07
		System.out.println("priceList :"+priceList+" \n tranDate :"+tranDate+" \n itemCode :"+itemCode+" \n lotNo:"+lotNo+" \n type:"+type);
		type = priceListType(priceList);
		System.out.println("type :"+type);
		ArrayList returnList = null;
		//ConnDriver connDriver = new ConnDriver(); //Gulzar 01/03/07
		try
		{
			returnList = genericUtility.getTokenList(lotNo,"\t");
		}
		catch(Exception e){}
		System.out.println("returnList :"+returnList.size());
		if (returnList.size() > 1)
		{
			if (returnList.get(0) != null)
			{
				siteCode = returnList.get(0).toString();
			}
			if (returnList.get(1) != null)
			{
				locCode = returnList.get(1).toString();
			}
			if (returnList.get(2) != null)
			{
				lotNo = returnList.get(2).toString();
			}
			if (returnList.get(3) != null)
			{
				lotSL = returnList.get(3).toString();
			}			
		}
		switch (type)
		{
		case 'L' : //List Price
			try
			{
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
				sql = "SELECT RATE FROM PRICELIST WHERE PRICE_LIST = ? "
						+"AND ITEM_CODE  = ? AND LIST_TYPE = 'L' "
						+"AND EFF_FROM <= ? AND VALID_UPTO >= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,priceList);
				pstmt.setString(2,itemCode);
				//pstmt.setDate(3,java.sql.Date.valueOf(new SimpleDateFormat(genericUtility.getDBDateFormat()).format(tranDate)));
				//pstmt.setDate(4,java.sql.Date.valueOf(new SimpleDateFormat(genericUtility.getDBDateFormat()).format(tranDate)));
				pstmt.setTimestamp(3,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(tranDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));					
				pstmt.setTimestamp(4,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(tranDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));

				//System.out.println("sql :"+sql);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					rate = rs.getDouble(1);
					//System.out.println("rate :"+rate);
				}
				else
				{
					rs.close(); rs = null;
					pstmt.close(); pstmt = null;
					sql = "SELECT CASE WHEN PRICE_LIST__PARENT IS NULL  THEN '' ELSE PRICE_LIST__PARENT END "
							+"FROM PRICELIST WHERE PRICE_LIST = ? AND LIST_TYPE = 'L' ";
					//System.out.println("sql :"+sql);
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,priceList);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						priceListParent = rs.getString(1);
						System.out.println("priceListParent :"+priceListParent);
					}
					rs.close(); rs = null;
					pstmt.close(); pstmt = null;
					if (priceListParent.trim().length() > 0)
					{
						sql = "SELECT RATE FROM PRICELIST WHERE PRICE_LIST = ? "
								+"AND ITEM_CODE = ? AND LIST_TYPE = 'L' "
								+"AND EFF_FROM <= ? AND VALID_UPTO >= ? ";
						//System.out.println("sql :"+sql);
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,priceListParent);
						pstmt.setString(2,itemCode);
						//pstmt.setDate(3,java.sql.Date.valueOf(new SimpleDateFormat(genericUtility.getDBDateFormat()).format(tranDate)));
						//pstmt.setDate(4,java.sql.Date.valueOf(new SimpleDateFormat(genericUtility.getDBDateFormat()).format(tranDate)));

						pstmt.setTimestamp(3,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(tranDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));					
						pstmt.setTimestamp(4,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(tranDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));

						rs = pstmt.executeQuery();
						if (rs.next())
						{
							rate = rs.getDouble(1);
							System.out.println("rate :"+rate);
						}
						else
						{

							rs.close(); rs = null;
							pstmt.close(); pstmt = null;
							//retValue = -1;//Changed by Manoj dtd 09/10/2014
							return -1;
						}
					}//end if
					else
					{
						//retValue = -1;//Changed by Manoj dtd 09/10/2014
						return -1;
					}
				}//end else
			}//try end
			catch(Exception e) 
			{
				System.out.println("Exception occurs in Case 'L' "+e);
			}
			finally
			{
				try
				{
					if(rs != null) {
						rs.close(); rs = null;
					}
					if(pstmt != null) {
						pstmt.close(); pstmt = null;
					}
					if(conn != null) {
						conn.close(); conn = null;
					}
					//conn.close();
				}catch(Exception e){}
			}
			break;
		case 'F' : // Fixed Price on Date
			try
			{
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
				sql = "SELECT RATE FROM PRICELIST WHERE PRICE_LIST = ? "
						+"AND ITEM_CODE  = ? AND LIST_TYPE = 'F' "
						+"AND EFF_FROM <= ? AND VALID_UPTO >= ? ";
				//System.out.println("sql :"+sql);
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,priceList);
				pstmt.setString(2,itemCode);
				//					pstmt.setDate(3,java.sql.Date.valueOf(new SimpleDateFormat(genericUtility.getDBDateFormat()).format(tranDate)));
				//					pstmt.setDate(4,java.sql.Date.valueOf(new SimpleDateFormat(genericUtility.getDBDateFormat()).format(tranDate)));
				//Commented and Added on 04/04/06
				pstmt.setTimestamp(3,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(tranDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));					
				pstmt.setTimestamp(4,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(tranDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
				///
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					rate = rs.getDouble(1);
					System.out.println("rate :"+rate);
				}
				else
				{
					rs.close(); rs = null;
					pstmt.close(); pstmt = null;
					sql = "SELECT CASE WHEN PRICE_LIST__PARENT IS NULL  THEN '' ELSE PRICE_LIST__PARENT END "
							+"FROM PRICELIST WHERE PRICE_LIST = ? AND LIST_TYPE = 'L' ";
					//System.out.println("sql :"+sql);
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,priceList);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						priceListParent = rs.getString(1);
					}
					rs.close(); rs = null;
					pstmt.close(); pstmt = null;
					if (priceListParent.trim().length() > 0)
					{
						sql = "SELECT RATE FROM PRICELIST WHERE PRICE_LIST = ? "
								+"AND ITEM_CODE = ? AND LIST_TYPE = 'F' "
								+"AND EFF_FROM <= ? AND VALID_UPTO >= ? ";
						//System.out.println("sql :"+sql);
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,priceListParent);
						pstmt.setString(2,itemCode);
						//							pstmt.setDate(3,java.sql.Date.valueOf(new SimpleDateFormat(genericUtility.getDBDateFormat()).format(tranDate)));
						//							pstmt.setDate(4,java.sql.Date.valueOf(new SimpleDateFormat(genericUtility.getDBDateFormat()).format(tranDate)));
						pstmt.setTimestamp(3,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(tranDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));					
						pstmt.setTimestamp(4,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(tranDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));				
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							rate = rs.getDouble(1);
							System.out.println("rate :"+rate);
						}
						else
						{
							rs.close(); rs = null;
							pstmt.close(); pstmt = null;
							//retValue = -1;//Changed by Manoj dtd 09/10/2014
							return -1;
						}
					}//end if
					else
					{
						//retValue = -1;//Changed by Manoj dtd 09/10/2014
						return -1;
					}
				}//end else
			}//try end
			catch(Exception e) 
			{
				System.out.println("Exception occurs in Case 'L' "+e);
			}
			finally
			{
				try
				{
					if(rs != null) {
						rs.close(); rs = null;
					}
					if(pstmt != null) {
						pstmt.close(); pstmt = null;
					}
					if(conn != null) {
						conn.close(); conn = null;
					}
					//conn.close();
				}catch(Exception e){}
			}
			break;
		case 'D' : // Despatch
			try
			{
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
				sql = "SELECT RATE FROM PRICELIST WHERE PRICE_LIST = ? "
						+"AND ITEM_CODE  = ? AND LIST_TYPE = 'L' "
						+"AND EFF_FROM <= ? AND VALID_UPTO >= ?";
				//System.out.println("sql :"+sql);
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,priceList);
				pstmt.setString(2,itemCode);
				//					pstmt.setDate(3,java.sql.Date.valueOf(new SimpleDateFormat(genericUtility.getDBDateFormat()).format(tranDate)));
				//					pstmt.setDate(4,java.sql.Date.valueOf(new SimpleDateFormat(genericUtility.getDBDateFormat()).format(tranDate)));
				pstmt.setTimestamp(3,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(tranDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));					
				pstmt.setTimestamp(4,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(tranDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));				
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					rate = rs.getDouble(1);
					System.out.println("rate :"+rate);
				}
				else
				{
					rs.close(); rs = null;
					pstmt.close(); pstmt = null;
					sql = "SELECT CASE WHEN PRICE_LIST__PARENT IS NULL  THEN '' ELSE PRICE_LIST__PARENT END "
							+"FROM PRICELIST WHERE PRICE_LIST = ? AND LIST_TYPE = 'L' ";
					//System.out.println("sql :"+sql);
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,priceList);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						priceListParent = rs.getString(1);
					}
					rs.close(); rs = null;
					pstmt.close(); pstmt = null;
					if (priceListParent.trim().length() > 0)
					{
						sql = "SELECT RATE FROM PRICELIST WHERE PRICE_LIST = ? "
								+"AND ITEM_CODE = ? AND LIST_TYPE = 'L' "
								+"AND EFF_FROM <= ? AND VALID_UPTO >= ? ";
						pstmt = conn.prepareStatement(sql);
						System.out.println("sql :"+sql);
						pstmt.setString(1,priceListParent);
						pstmt.setString(2,itemCode);
						//							pstmt.setDate(3,java.sql.Date.valueOf(new SimpleDateFormat(genericUtility.getDBDateFormat()).format(tranDate)));
						//							pstmt.setDate(4,java.sql.Date.valueOf(new SimpleDateFormat(genericUtility.getDBDateFormat()).format(tranDate)));
						pstmt.setTimestamp(3,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(tranDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));					
						pstmt.setTimestamp(4,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(tranDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));				

						rs = pstmt.executeQuery();
						if (rs.next())
						{
							rate = rs.getDouble(1);
							System.out.println("rate :"+rate);
						}
						rs.close(); rs = null;
						pstmt.close(); pstmt = null;
					}//end if
					else
					{
						rate = 0;
						sql = "SELECT RATE FROM PRICELIST WHERE PRICE_LIST = ? "
								+"AND ITEM_CODE = ? AND LIST_TYPE = 'B' "
								+"AND LOT_NO__FROM <= ? "
								+"AND LOT_NO__TO  >= ? "
								+"AND EFF_FROM <= ? "
								+"AND VALID_UPTO >= ? ";
						//System.out.println("sql :"+sql);
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,priceList);
						pstmt.setString(2,itemCode);
						pstmt.setString(3,lotNo);
						pstmt.setString(4,lotNo);
						//							pstmt.setDate(5,java.sql.Date.valueOf(new SimpleDateFormat(genericUtility.getDBDateFormat()).format(tranDate)));
						//							pstmt.setDate(6,java.sql.Date.valueOf(new SimpleDateFormat(genericUtility.getDBDateFormat()).format(tranDate)));
						pstmt.setTimestamp(5,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(tranDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));					
						pstmt.setTimestamp(6,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(tranDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));				
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							rate = rs.getDouble(1);
						}
						else
						{
							rs.close(); rs = null;
							pstmt.close(); pstmt = null;
							sql = "SELECT CASE WHEN PRICE_LIST__PARENT IS NULL  THEN '' ELSE PRICE_LIST__PARENT END "
									+"FROM PRICELIST WHERE PRICE_LIST = ? AND LIST_TYPE = 'B'";
							//System.out.println("sql :"+sql);
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,priceList);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								priceListParent = rs.getString(1);
								System.out.println("priceListParent  :"+priceListParent);
							}
							rs.close(); rs = null;
							pstmt.close(); pstmt = null;
							if (priceListParent.trim().length() > 0)
							{
								sql = "SELECT RATE FROM PRICELIST WHERE PRICE_LIST = ? "
										+"AND ITEM_CODE = ? AND LIST_TYPE = 'B' "
										+"AND LOT_NO__FROM <= ? "
										+"AND LOT_NO__TO  >= ? "
										+"AND EFF_FROM <= ? "
										+"AND VALID_UPTO >= ? ";
								//System.out.println("sql :"+sql);
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,priceList);
								pstmt.setString(2,itemCode);
								pstmt.setString(3,lotNo);
								pstmt.setString(4,lotNo);
								//									pstmt.setDate(5,java.sql.Date.valueOf(new SimpleDateFormat(genericUtility.getDBDateFormat()).format(tranDate)));
								//									pstmt.setDate(6,java.sql.Date.valueOf(new SimpleDateFormat(genericUtility.getDBDateFormat()).format(tranDate)));
								pstmt.setTimestamp(5,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(tranDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));					
								pstmt.setTimestamp(6,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(tranDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));				

								rs = pstmt.executeQuery();
								if (rs.next())
								{
									rate = rs.getDouble(1);
									System.out.println("rate :"+rate);
								}
								else
								{
									rs.close(); rs = null;
									pstmt.close(); pstmt = null;
									//retValue = -1;//Changed by Manoj dtd 09/10/2014
									return -1;
								}
							}//end if
							else
							{
								//retValue = -1;//Changed by Manoj dtd 09/10/2014
								return -1;
							}
						}//end else
					}
				}//end else
			}//try end
			catch(Exception e) 
			{
				System.out.println("Exception occurs in Case 'L' "+e);
			}
			finally
			{
				try
				{
					if(rs != null) {
						rs.close(); rs = null;
					}
					if(pstmt != null) {
						pstmt.close(); pstmt = null;
					}
					if(conn != null) {
						conn.close(); conn = null;
					}
					//conn.close();
				}catch(Exception e){}
			}

			break;
		case 'B' :
			try
			{
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
				rate = 0;
				sql = "SELECT RATE FROM PRICELIST WHERE PRICE_LIST = ? "
						+"AND ITEM_CODE = ? AND LIST_TYPE = 'B' "
						+"AND LOT_NO__FROM <= ? "
						+"AND LOT_NO__TO  >= ? "
						+"AND EFF_FROM <= ? "
						+"AND VALID_UPTO >= ? ";
				//System.out.println("sql :"+sql);
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,priceList);
				pstmt.setString(2,itemCode);
				pstmt.setString(3,lotNo);
				pstmt.setString(4,lotNo);
				//					pstmt.setDate(5,java.sql.Date.valueOf(new SimpleDateFormat(genericUtility.getDBDateFormat()).format(tranDate)));
				//					pstmt.setDate(6,java.sql.Date.valueOf(new SimpleDateFormat(genericUtility.getDBDateFormat()).format(tranDate)));
				pstmt.setTimestamp(5,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(tranDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));					
				pstmt.setTimestamp(6,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(tranDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));				
				rs = pstmt.executeQuery();

				if (rs.next())
				{
					rate = rs.getDouble(1);
					System.out.println("rate :"+rate);
				}
				else
				{
					rs.close(); rs = null;
					pstmt.close(); pstmt = null;
					sql = "SELECT CASE WHEN PRICE_LIST__PARENT IS NULL  THEN '' ELSE PRICE_LIST__PARENT END "
							+"FROM PRICELIST WHERE PRICE_LIST = ? AND LIST_TYPE = 'B' ";
					//System.out.println("sql :"+sql);
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,priceList);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						priceListParent = rs.getString(1);
						System.out.println("priceListParent :"+priceListParent);
					}
					rs.close(); rs = null;
					pstmt.close(); pstmt = null;
					if (priceListParent.trim().length() > 0)
					{
						sql = "SELECT RATE FROM PRICELIST WHERE PRICE_LIST = ? "
								+"AND ITEM_CODE = ? AND LIST_TYPE = 'B' "
								+"AND LOT_NO__FROM <= ? "
								+"AND LOT_NO__TO  >= ? "
								+"AND EFF_FROM <= ? "
								+"AND VALID_UPTO >= ? ";
						//System.out.println("sql :"+sql);
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,priceListParent);
						pstmt.setString(2,itemCode);
						pstmt.setString(3,lotNo);
						pstmt.setString(4,lotNo);
						//							pstmt.setDate(5,java.sql.Date.valueOf(new SimpleDateFormat(genericUtility.getDBDateFormat()).format(tranDate)));
						//							pstmt.setDate(6,java.sql.Date.valueOf(new SimpleDateFormat(genericUtility.getDBDateFormat()).format(tranDate)));
						pstmt.setTimestamp(5,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(tranDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));					
						pstmt.setTimestamp(6,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(tranDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));				

						rs = pstmt.executeQuery();
						if (rs.next())
						{
							rate = rs.getDouble(1);
							System.out.println("rate :"+rate);
						}
						else
						{
							rs.close(); rs = null;
							pstmt.close(); pstmt = null;
							//retValue = -1;//Changed by Manoj dtd 09/10/2014
							return -1;
						}
					}//end if
					else
					{
						//retValue = -1;//Changed by Manoj dtd 09/10/2014
						return -1;
					}
				}//else end
			}//try end
			catch(Exception e) 
			{
				System.out.println("Exception occurs in Case 'L' "+e);
			}
			finally
			{
				try
				{
					if(rs != null) {
						rs.close(); rs = null;
					}
					if(pstmt != null) {
						pstmt.close(); pstmt = null;
					}
					if(conn != null) {
						conn.close(); conn = null;
					}
					//conn.close();
				}catch(Exception e){}
			}
			break;
		case 'M': // Discount PRICE
		case 'N': // Discount PRICE
			try
			{
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 

				rate = 0;
				sql = "SELECT RATE FROM PRICELIST WHERE PRICE_LIST = ? "
						+"AND ITEM_CODE = ? AND LIST_TYPE = '"+type+"' "
						+"AND EFF_FROM <= ? "
						+"AND VALID_UPTO >= ? ";
				//System.out.println("sql :"+sql);
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,priceList);
				pstmt.setString(2,itemCode);
				//					pstmt.setDate(3,java.sql.Date.valueOf(new SimpleDateFormat(genericUtility.getDBDateFormat()).format(tranDate)));
				//					pstmt.setDate(4,java.sql.Date.valueOf(new SimpleDateFormat(genericUtility.getDBDateFormat()).format(tranDate)));
				pstmt.setTimestamp(3,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(tranDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));					
				pstmt.setTimestamp(4,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(tranDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));				

				rs = pstmt.executeQuery();
				if (rs.next())
				{
					rate = rs.getDouble(1);
					System.out.println("rate :"+rate);
				}
				else
				{
					rs.close(); rs = null;
					pstmt.close(); pstmt = null;
					sql = "SELECT CASE WHEN PRICE_LIST__PARENT IS NULL  THEN '' ELSE PRICE_LIST__PARENT END "
							+"FROM PRICELIST WHERE PRICE_LIST = ? AND LIST_TYPE = ? ";
					//System.out.println("sql :"+sql);
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,priceList);
					pstmt.setString(2,String.valueOf(type));
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						priceListParent = rs.getString(1);
					}
					rs.close(); rs = null;
					pstmt.close(); pstmt = null;
					if (priceListParent.trim().length() > 0)
					{
						sql = "SELECT RATE FROM PRICELIST WHERE PRICE_LIST = ? "
								+"AND ITEM_CODE = ? AND LIST_TYPE = ? "
								+"AND EFF_FROM <= ? "
								+"AND VALID_UPTO >= ? ";
						//System.out.println("sql :"+sql);
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,priceListParent);
						pstmt.setString(2,itemCode);
						pstmt.setString(3,String.valueOf(type));
						//							pstmt.setDate(4,java.sql.Date.valueOf(new SimpleDateFormat(genericUtility.getDBDateFormat()).format(tranDate)));
						//							pstmt.setDate(5,java.sql.Date.valueOf(new SimpleDateFormat(genericUtility.getDBDateFormat()).format(tranDate)));
						pstmt.setTimestamp(4,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(tranDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));					
						pstmt.setTimestamp(5,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(tranDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));				

						rs = pstmt.executeQuery();
						if (rs.next())
						{
							rate = rs.getDouble(1);
							System.out.println("rate :"+rate);
						}
						else
						{
							rs.close(); rs = null;
							pstmt.close(); pstmt = null;
							//retValue = -1;//Changed by Manoj dtd 09/10/2014
							return -1;
						}
					}//end if
					else
					{
						//retValue = -1;//Changed by Manoj dtd 09/10/2014
						return -1;
					}
				}//else end
			}//try end
			catch(Exception e) 
			{
				System.out.println("Exception occurs in Case 'L' "+e);
			}
			finally
			{
				try
				{
					if(rs != null) {
						rs.close(); rs = null;
					}
					if(pstmt != null) {
						pstmt.close(); pstmt = null;
					}
					if(conn != null) {
						conn.close(); conn = null;
					}
					//conn.close();
				}catch(Exception e){}
			}
			break;
		case 'I' : //Inventory
			try
			{
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
				rate = 0;
				if (lotSL.equals("") || lotSL.trim().length() == 0)
				{
					sql = "SELECT RATE FROM STOCK WHERE ITEM_CODE = ? "
							+"AND SITE_CODE = ? "
							+"AND LOC_CODE  = ? "
							+"AND LOT_NO = ? ";
					//System.out.println("sql :"+sql);
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,itemCode);
					pstmt.setString(2,siteCode);
					pstmt.setString(3,locCode);
					pstmt.setString(4,lotNo);

					rs = pstmt.executeQuery();
					if (rs.next())
					{
						rate = rs.getDouble(1);
						System.out.println("rate :"+rate);
					}
					rs.close(); rs = null;
					pstmt.close(); pstmt = null;
				}
				else
				{
					sql ="SELECT RATE FROM STOCK WHERE ITEM_CODE = ? "
							+"AND SITE_CODE = ? "
							+"AND LOC_CODE  = ? "
							+"AND LOT_NO = ? "
							+"AND LOT_SL = ? ";
					//System.out.println("sql :"+sql);
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,itemCode);
					pstmt.setString(2,siteCode);
					pstmt.setString(3,locCode);
					pstmt.setString(4,lotNo);
					pstmt.setString(5,lotSL);

					rs = pstmt.executeQuery();
					if (rs.next())
					{
						rate = rs.getDouble(1);
						System.out.println("rate :"+rate);
					}
					rs.close(); rs = null;
					pstmt.close(); pstmt = null;
				}
			}
			catch(Exception e)
			{
				System.out.println("The sql Exception occurs :"+e);						
			}
			finally
			{
				try
				{
					if(rs != null) {
						rs.close(); rs = null;
					}
					if(pstmt != null) {
						pstmt.close(); pstmt = null;
					}
					if(conn != null) {
						conn.close(); conn = null;
					}
					//conn.close();
				}catch(Exception e){}
			}
		}//end switch
		System.out.println("rate :"+rate);
		return rate;
	}

	private char priceListType(String priceList)
	{
		char listType = ' ';
		System.out.println("priceList :"+priceList);
		String sql = "";
		ResultSet rs = null;
		Connection conn = null;
		//Statement stmt = null;
		PreparedStatement pstmt = null;
		//ConnDriver connDriver = new ConnDriver();
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			//stmt = conn.createStatement();
			sql = "SELECT LIST_TYPE FROM PRICELIST WHERE PRICE_LIST = ? ";//'"+priceList+"'";
			//System.out.println("sql  :"+sql);
			//rs = stmt.executeQuery(sql);
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, priceList);
			rs = pstmt.executeQuery();		
			if (rs.next())
			{
				System.out.println("rs.getString(1) :"+rs.getString(1));
				if(rs.getString(1) == null)
				{
					listType = ' ';
				}
				else
				{
					listType = rs.getString(1).charAt(0);
					System.out.println("listType :"+listType);
				}
			}
			else
			{
				listType = ' ';
			}
			rs.close(); rs = null;
			pstmt.close(); pstmt = null;
			//conn.close();
		}//try end
		catch(SQLException sqx)
		{
			System.out.println("The Sql Exception Occures in priceListType :"+sqx);
		}
		catch(Exception e)
		{
			System.out.println("The Exception Occures in priceListType :"+e);
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
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if(conn != null)
				{
					conn.close();
					conn = null;
				}
				//conn.close();
				//stmt = null;
			}catch(Exception e){}
		}// finally end
		System.out.println("listType :"+listType);
		return listType;
	}
	private String calcExpiry(String tranDate, int months)
	{
		java.util.Date expDate = new java.util.Date();
		java.util.Date retDate = new java.util.Date();
		String retStrInDate = "";
		System.out.println("tranDate :"+tranDate+"\nmonths :"+months);
		try
		{
			ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			if (months > 0)
			{
				Calendar  cal = Calendar.getInstance();
				expDate = sdf.parse(tranDate);
				System.out.println("expDate :"+expDate);
				cal.setTime(expDate);
				cal.add(Calendar.MONTH,months);
				//for last day of the month
				cal.add(Calendar.MONTH,1);
				cal.set(Calendar.DATE,0);
				//sets zero to get the last day of the given date
				retDate = cal.getTime();
				retStrInDate = sdf.format(retDate);
			}
			else
			{
				retStrInDate = tranDate;
			}
		}
		catch(Exception e)
		{
			System.out.println("The Exception occurs in calcExpiry :"+e);
		}
		System.out.println("retStrInDate :"+retStrInDate);
		return retStrInDate;
	}

	private double getIntegralQty(String siteCode, String itemCode, String lotNo, String packCode, String checkIntegralQty)
	{
		double integralQty = 0;
		String sql = "";
		ResultSet rs = null;
		Connection conn = null;
		//ConnDriver connDriver = new ConnDriver();
		//Statement stmt = null;
		PreparedStatement pstmt = null;
		try
		{
			//System.out.println("$%$%$%$%$%$%Inside getIntegralQty function&*&*&*&*&");
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			//stmt = conn.createStatement();
			char type = checkIntegralQty.charAt(0);
			//System.out.println("type==>"+type);
			switch (type)
			{
			case 'S':
				sql ="SELECT CASE WHEN SHIPPER_SIZE IS NULL THEN 0 ELSE SHIPPER_SIZE END "
						+"FROM ITEM_LOT_PACKSIZE "
						+"WHERE ITEM_CODE = ? "//'"+itemCode+"' "
						+"AND LOT_NO__FROM <= ? "//'"+lotNo+"' "
						+"AND LOT_NO__TO   >= ? ";//'"+lotNo+"' ";
				//System.out.println("sql :"+sql);
				//rs = stmt.executeQuery(sql);
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, itemCode);
				pstmt.setString(2, lotNo);
				pstmt.setString(3, lotNo);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					integralQty = rs.getDouble(1);
					//System.out.println("integralQty :"+integralQty);
				}
				rs.close(); rs = null;
				pstmt.close(); pstmt = null;
				if (integralQty == 0)
				{
					sql = "SELECT CASE WHEN CAPACITY IS NULL THEN 0 ELSE CAPACITY END "
							+"FROM PACKING WHERE PACK_CODE = ?"; //'"+packCode+"'";
					//System.out.println("sql :"+sql);
					//rs = stmt.executeQuery(sql);
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, packCode);					
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						integralQty = rs.getDouble(1);
						//System.out.println("integralQty :"+integralQty);
					}
					rs.close(); rs = null;
					pstmt.close(); pstmt = null;
					if (integralQty == 0)
					{
						sql = "SELECT REO_QTY FROM SITEITEM "
								//+"WHERE SITE_CODE = '"+siteCode+"'"+" AND ITEM_CODE = '"+itemCode+"'";
								+"WHERE SITE_CODE = ? AND ITEM_CODE = ?";
						//System.out.println("sql :"+sql);	
						//rs = stmt.executeQuery(sql);
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, siteCode);
						pstmt.setString(2, itemCode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							integralQty = rs.getDouble(1);
							//System.out.println("integralQty :"+integralQty);
						}
						rs.close(); rs = null;
						pstmt.close(); pstmt = null;
						if (integralQty == 0)
						{
							sql = "SELECT REO_QTY FROM ITEM "
									+"WHERE ITEM_CODE = ? ";//'"+itemCode+"'";
							//System.out.println("sql :"+sql);	
							//rs = stmt.executeQuery(sql);
							pstmt = conn.prepareStatement(sql);							
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								integralQty = rs.getDouble(1);
								//System.out.println("integralQty :"+integralQty);
							}
							rs.close(); rs = null;
							pstmt.close(); pstmt = null;
						}
					}
				}
				System.out.println("integralQty  = ["+integralQty+"]");
				break;
			case 'P':
				sql = "SELECT CASE WHEN CAPACITY IS NULL THEN 0 ELSE CAPACITY END "
						+"FROM PACKING WHERE PACK_CODE = ?";//'"+packCode+"'";
				//System.out.println("sql :"+sql);
				//rs = stmt.executeQuery(sql);
				pstmt = conn.prepareStatement(sql);							
				pstmt.setString(1, packCode);
				rs = pstmt.executeQuery();				
				if (rs.next())
				{
					integralQty = rs.getDouble(1);
					//System.out.println("integralQty :"+integralQty);
				}
				rs.close(); rs = null;
				pstmt.close(); pstmt = null;
				System.out.println("integralQty  = ["+integralQty+"]");
				break;
			case 'I':
				sql = "SELECT INTEGRAL_QTY FROM SITEITEM "
						//+"WHERE SITE_CODE = '"+siteCode+"'"+" AND ITEM_CODE = '"+itemCode+"'";
						+"WHERE SITE_CODE = ? AND ITEM_CODE = ?";
				//System.out.println("sql :"+sql);	
				//rs = stmt.executeQuery(sql);
				pstmt = conn.prepareStatement(sql);							
				pstmt.setString(1, siteCode);
				pstmt.setString(2, itemCode);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					integralQty = rs.getDouble(1);
					//System.out.println("integralQty :"+integralQty);
				}
				rs.close(); rs = null;
				pstmt.close(); pstmt = null;
				if (integralQty == 0)
				{
					sql = "SELECT INTEGRAL_QTY FROM ITEM WHERE ITEM_CODE = ? ";//'"+itemCode+"'";
					//System.out.println("sql :"+sql);
					//rs = stmt.executeQuery(sql);
					pstmt = conn.prepareStatement(sql);							
					pstmt.setString(1, itemCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						integralQty = rs.getDouble(1);
						//System.out.println("integralQty :"+integralQty);
					}
					rs.close(); rs = null;
					pstmt.close(); pstmt = null;
				}
				System.out.println("integralQty from getIntegralQty function = ["+integralQty+"]");
			}
			/* sql ="SELECT CASE WHEN SHIPPER_SIZE IS NULL THEN 0 ELSE SHIPPER_SIZE END "
				+"FROM ITEM_LOT_PACKSIZE "
				+"WHERE ITEM_CODE = '"+itemCode+"' "
				+"AND LOT_NO__FROM <= '"+lotNo+"' "
				+"AND LOT_NO__TO   >= '"+lotNo+"' ";
			System.out.println("sql :"+sql);
			rs = stmt.executeQuery(sql);
			if (rs.next())
			{
				integralQty = rs.getDouble(1);
				System.out.println("integralQty :"+integralQty);
			}
			if (integralQty == 0)
			{
				sql = "SELECT CASE WHEN CAPACITY IS NULL THEN 0 ELSE CAPACITY END "
					 +"FROM PACKING WHERE PACK_CODE = '"+packCode+"'";
				System.out.println("sql :"+sql);
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					integralQty = rs.getDouble(1);
					System.out.println("integralQty :"+integralQty);
				}
				if (integralQty == 0)
				{
					sql = "SELECT INTEGRAL_QTY FROM SITEITEM "
						 +"WHERE SITE_CODE = '"+siteCode+"'"+" AND ITEM_CODE = '"+itemCode+"'";
					System.out.println("sql :"+sql);	
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						integralQty = rs.getDouble(1);
						System.out.println("integralQty :"+integralQty);
					}
					if (integralQty == 0)
					{
						sql = "SELECT INTEGRAL_QTY FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
						System.out.println("sql :"+sql);
						rs = stmt.executeQuery(sql);
						if (rs.next())
						{
							 integralQty = rs.getDouble(1);
							System.out.println("integralQty :"+integralQty);
						}
					}
				}
			} */
			//conn.close();	
		}
		catch(Exception e)
		{
			System.out.println("the exception occurs in getIntegralQty :"+e);
		}finally 
		{
			try{
				if(rs != null) {
					rs.close(); rs = null;
				}
				if(pstmt != null) {
					pstmt.close(); pstmt = null;
				}
				if(conn != null) {
					conn.close(); conn = null;
				}
			}catch(Exception e){}							
		}
		System.out.println("integralQty :"+integralQty);
		return integralQty;
	}

	private int getNoArt(String siteCode, String custCode, String itemCode, String packCode, double qty, char type, double shipperQty, double integralQty1)
	{
		String sql = "";
		ResultSet rs = null;
		Connection conn = null;
		//ConnDriver connDriver = new ConnDriver();
		//Statement stmt = null;
		PreparedStatement pstmt = null;
		double reoQty = 0d, capacity = 0d, integralQty = 0d, mod = 0d, noArt3 = 0d;
		double noArt = 0, noArt1 = 0, noArt2 = 0; 
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			//stmt = conn.createStatement();
			System.out.println("type :"+type);
			switch (type)
			{
			case 'S':
				sql = "SELECT CASE WHEN CAPACITY IS NULL THEN 0 ELSE CAPACITY END " 
						+"FROM PACKING WHERE PACK_CODE = ?";//'"+packCode+"'";
				//rs = stmt.executeQuery(sql);
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, packCode);				
				rs = pstmt.executeQuery();
				
				if (rs.next())
				{
					capacity = rs.getDouble(1);
					System.out.println("capacity :"+capacity);
				}
				else
				{
					capacity = 0;
				}
				rs.close(); rs = null;
				pstmt.close(); pstmt = null;
				sql = "SELECT REO_QTY FROM SITEITEM WHERE SITE_CODE = ? "//'"+siteCode+"' " 
						+"AND ITEM_CODE = ?";	//'"+itemCode+"'";
				//System.out.println("sql :"+sql);
				//rs = stmt.executeQuery(sql);
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, siteCode);				
				pstmt.setString(2, itemCode);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					reoQty = rs.getDouble(1);
					System.out.println("reoQty :"+reoQty);
				}
				rs.close(); rs = null;
				pstmt.close(); pstmt = null;
				if( reoQty == 0 )
				{
					sql = "SELECT REO_QTY FROM ITEM WHERE ITEM_CODE = ?"; //'"+itemCode+"'";
					//System.out.println("sql :"+sql);
					//rs = stmt.executeQuery(sql);
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCode);									
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						reoQty = rs.getDouble(1);
						System.out.println("reoQty :"+reoQty);
					}
					rs.close(); rs = null;
					pstmt.close(); pstmt = null;
				}
				if (reoQty == 0)
				{
					reoQty = 0;
				}
				if (capacity > 0)
				{
					shipperQty = capacity;
					//System.out.println("shipperQty :"+shipperQty);
				}
				else
				{
					shipperQty = reoQty;
					//System.out.println("shipperQty :"+shipperQty);
				}
				System.out.println("shipperQty :"+shipperQty);
				if (shipperQty > 0)
				{
					mod = qty%shipperQty;
					//System.out.println("mod :"+mod);
					noArt = (qty - mod) / shipperQty;
				}
				System.out.println("noArt :"+noArt);
				break;
			case 'I':
				sql = "SELECT INTEGRAL_QTY FROM CUSTOMERITEM "
						//+"WHERE CUST_CODE = '"+custCode+"'"+" AND ITEM_CODE = '"+itemCode+"'";
						+"WHERE CUST_CODE = ? AND ITEM_CODE = ?";
				//System.out.println("sql :"+sql);
				//rs = stmt.executeQuery(sql);
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, custCode);
				pstmt.setString(2, itemCode);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					integralQty = rs.getDouble(1);
					//System.out.println("integralQty :"+integralQty);
				}
				rs.close(); rs = null;
				pstmt.close(); pstmt = null;
				if (integralQty == 0)
				{
					sql = "SELECT INTEGRAL_QTY FROM SITEITEM "
							//+"WHERE SITE_CODE = '"+siteCode+"'"+" AND ITEM_CODE = '"+itemCode+"'";
							+"WHERE SITE_CODE = ? AND ITEM_CODE = ?";
					//System.out.println("sql :"+sql);
					//rs = stmt.executeQuery(sql);
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteCode);
					pstmt.setString(2, itemCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						integralQty = rs.getDouble(1);
						//System.out.println("integralQty :"+integralQty);
					}
					rs.close(); rs = null;
					pstmt.close(); pstmt = null;
					if( integralQty == 0 )
					{
						sql = "SELECT INTEGRAL_QTY FROM ITEM WHERE ITEM_CODE = ?";//'"+itemCode+"'";
						//System.out.println("sql :"+sql);
						//rs = stmt.executeQuery(sql);
						pstmt = conn.prepareStatement(sql);						
						pstmt.setString(1, itemCode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							integralQty = rs.getDouble(1);
							//System.out.println("integralQty :"+integralQty);
						}
						rs.close(); rs = null;
						pstmt.close(); pstmt = null;
					}
				}
				if (integralQty > 0)
				{
					mod = qty%integralQty;
					//System.out.println("mod :"+mod);
					noArt = (qty - mod) / integralQty;
					//System.out.println("noArt :"+noArt);
				}
				break;
			case 'B' :
				sql = "SELECT CASE WHEN CAPACITY IS NULL THEN 0 ELSE CAPACITY END " 
						+"FROM PACKING WHERE PACK_CODE = ?";//'"+packCode+"'";
				//System.out.println("sql :"+sql);
				//rs = stmt.executeQuery(sql);
				pstmt = conn.prepareStatement(sql);						
				pstmt.setString(1, packCode);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					capacity = rs.getDouble(1);
					//System.out.println("capacity :"+capacity);
				}
				else
				{
					capacity = 0;
				}
				rs.close(); rs = null;
				pstmt.close(); pstmt = null;
				sql = "SELECT REO_QTY FROM SITEITEM WHERE SITE_CODE = ?" //'"+siteCode+"' " 
						+"AND ITEM_CODE = ? ";//'"+itemCode+"'";
				//System.out.println("sql :"+sql);
				//rs = stmt.executeQuery(sql);
				pstmt = conn.prepareStatement(sql);						
				pstmt.setString(1, siteCode);
				pstmt.setString(2, itemCode);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					reoQty = rs.getDouble(1);
					//System.out.println("reoQty :"+reoQty);
				}
				rs.close(); rs = null;
				pstmt.close(); pstmt = null;
				if( reoQty == 0 )
				{
					sql = "SELECT REO_QTY FROM ITEM WHERE ITEM_CODE = ?";	//'"+itemCode+"'";
					//System.out.println("sql :"+sql);
					//rs = stmt.executeQuery(sql);
					pstmt = conn.prepareStatement(sql);											
					pstmt.setString(1, itemCode);
					rs = pstmt.executeQuery();					
					if (rs.next())
					{
						reoQty = rs.getDouble(1);
						//System.out.println("reoQty :"+reoQty);
					}
					rs.close(); rs = null;
					pstmt.close(); pstmt = null;
				}
				if (capacity > 0)
				{
					shipperQty = capacity;
					//System.out.println("shipperQty :"+shipperQty);
				}
				else
				{
					shipperQty = reoQty;
					//System.out.println("shipperQty :"+shipperQty);
				}
				if (shipperQty > 0)
				{
					mod = (qty % shipperQty);
					//System.out.println("mod :"+mod);
					noArt1 = (qty - mod) / shipperQty;
					//System.out.println("noArt1 :"+noArt1);
				}
				sql = "SELECT INTEGRAL_QTY FROM CUSTOMERITEM "
						+"WHERE CUST_CODE = ? "//'"+custCode+"' "
						+"AND ITEM_CODE = ?";//'"+itemCode+"'";
				//System.out.println("sql :"+sql);
				//rs = stmt.executeQuery(sql);
				pstmt = conn.prepareStatement(sql);											
				pstmt.setString(1, custCode);
				pstmt.setString(2, itemCode);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					integralQty = rs.getDouble(1);
					//System.out.println("integralQty :"+integralQty);
				}
				rs.close(); rs = null;
				pstmt.close(); pstmt = null;
				if (integralQty == 0)
				{
					sql ="SELECT INTEGRAL_QTY FROM SITEITEM "
							+"WHERE SITE_CODE = ? "//'"+siteCode+"' " 
							+"AND ITEM_CODE =  ? ";//'"+itemCode+"'";
					//System.out.println("sql :"+sql);
					//rs = stmt.executeQuery(sql);
					pstmt = conn.prepareStatement(sql);											
					pstmt.setString(1, siteCode);
					pstmt.setString(2, itemCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						integralQty = rs.getDouble(1);
						//System.out.println("integralQty :"+integralQty);
					}
					rs.close(); rs = null;
					pstmt.close(); pstmt = null;
					if(integralQty == 0)
					{
						sql = "SELECT INTEGRAL_QTY FROM ITEM WHERE ITEM_CODE = ?";//'"+itemCode+"'";
						//System.out.println("sql :"+sql);
						//rs = stmt.executeQuery(sql);
						pstmt = conn.prepareStatement(sql);																	
						pstmt.setString(1, itemCode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							integralQty = rs.getDouble(1);
							//System.out.println("integralQty :"+integralQty);
						}
						rs.close(); rs = null;
						pstmt.close(); pstmt = null;
					}
				}
				double remainder1 = 0d;
				if (integralQty > 0)
				{
					remainder1 = mod % integralQty;
					//System.out.println("remainder1 :"+remainder1);
					noArt3 =(mod - remainder1) / integralQty;
					//System.out.println("noArt3 :"+noArt3);
					noArt2 = (int)noArt3;
					//System.out.println("noArt2 :"+noArt2);
				}
				if (noArt2 > 0)
				{
					noArt2 = 1;
				}
				noArt  = noArt1 + noArt2;
				System.out.println("noArt :"+noArt);
			}
			//conn.close();
			if (noArt == 0)
			{
				noArt = 0;
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception occures in getNoArt :"+e);
		}
		finally {
			try{
				if(rs != null) {
					rs.close(); rs = null;
				}
				if(pstmt != null) {
					pstmt.close(); pstmt = null;
				}
				if(conn != null)
				{
					conn.close(); conn = null;
				}
			}catch(Exception e){}					
		}
		System.out.println("(int)noArt :"+(int)noArt);
		return (int)noArt;
	}
	public String getFormatedValue(double actVal,int prec)throws RemoteException//This method is added by nisar on 11/23/2007
	{//this method is used to return double with appropriate precison
		NumberFormat numberFormat = NumberFormat.getIntegerInstance ();
		Double DoubleValue = new Double (actVal);
		numberFormat.setMaximumFractionDigits(prec);
		String strValue = numberFormat.format(DoubleValue);
		System.out.println(strValue);
		strValue = strValue.replaceAll(",","");
		return strValue;
	}
	private String checkNull(String input) {
		if (input == null) {
			input = " ";
		}
		return input;
	}
}