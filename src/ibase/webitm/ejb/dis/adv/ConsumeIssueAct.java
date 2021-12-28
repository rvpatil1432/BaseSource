package ibase.webitm.ejb.dis.adv;

import java.util.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.rmi.RemoteException;
import java.io.*;
import org.w3c.dom.*;

import javax.ejb.*;

import ibase.system.config.*;
import ibase.webitm.ejb.*;
import ibase.webitm.ejb.dis.*;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;

import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import org.xml.sax.InputSource;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class ConsumeIssueAct extends ActionHandlerEJB implements ConsumeIssueActLocal, ConsumeIssueActRemote
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
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				System.out.println("XML String :"+xmlString);
				dom = genericUtility.parseString(xmlString); 
			}
			if(xmlString1 != null && xmlString1.trim().length()!=0)
			{
				System.out.println("XML String1 :"+xmlString1);
				dom1 = genericUtility.parseString(xmlString1);
			}

			System.out.println("actionType:"+actionType+":");

			if (actionType.equalsIgnoreCase("All Items"))
			{
				//retString = actionAllItems(dom, dom1, xtraParams); //Commented - Gulzar 25/04/07
				retString = actionAllItems(dom, dom1, xtraParams, actionType); //Added - Gulzar 25/04/07
			}
			if (actionType.equalsIgnoreCase("Stock"))
			{
				retString = actionStock(dom, dom1, xtraParams);
			}
			if (actionType.equalsIgnoreCase("Allocate"))
			{
				//retString = actionAllocate(dom, dom1, xtraParams); //Commented - Gulzar 25/04/07
				StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
				retString = actionAllocate(dom, dom1, xtraParams, actionType); //Added - - Gulzar 25/04/07
				valueXmlString.append(retString);
				valueXmlString.append("</Root>\r\n");
				retString = valueXmlString.toString();
			}
		}
	   	catch(Exception e)
		{
			System.out.println("Exception :ConsumeIssueAct :actionHandler(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning String from ConsumeIssueAct : actionHandler"+retString);
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
		}
	   	catch(Exception e)
		{
			System.out.println("Exception :ConsumeIssue :actionHandlerTransform(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning String from ConsumeIssue : actionHandlerTransform"+retString);
	    return retString;
	}
	
	//private String actionAllItems(Document dom, Document dom1, String xtraParams) throws RemoteException,ITMException //Commented - Gulzar 25/04/07
	private String actionAllItems(Document dom, Document dom1, String xtraParams, String actionType) throws RemoteException,ITMException //Added - - Gulzar 25/04/07
	{
		String consumeOrder = "", sql = "", locCode = "", availableYn = "", available = "";
		String errCode = "", errString = "";
		Statement stmt = null;
		ResultSet rs = null;
		Connection conn = null;
		ArrayList lineNo = new ArrayList();
		String lineNoStr = "";
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		StringBuffer allocateBuffer = new StringBuffer("");
		StringBuffer itemChgXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n"); //Gulzar 25/04/07
		String allocateStr = "";
		String detailCnt = ""; //Gulzar 21-02-07
		String itemChgRetStr = "";
		int detCnt = 0;//Gulzar 21-02-07
		Document temp = null;
		//Added By Gulzar 21-02-07
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		detailCnt = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"detCnt"); 
		if (detailCnt != null)
		{
			detCnt = Integer.parseInt(detailCnt);
			System.out.println("detCnt...........:: " + detCnt);
		}
		//End changes Gulzar 21-02-07
		/*ADDED BY HATIM ON 16/01/2006*/
		if (dom == null || detCnt > 1) //detCnt Condition Added By Gulzar 21-02-07
		{
			valueXmlString.append("</Root>\r\n");
			return valueXmlString.toString();
		}
		/*END*/
		ConnDriver connDriver = new ConnDriver();
		//ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility(); //gulzar 21/02/07
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
		//	stmt = conn.createStatement();
			PreparedStatement pstmt = null;// commented and added by kailasg on 16-april-21 as per suggestion of SM sir.
			consumeOrder = genericUtility.getColumnValue("cons_order",dom1);
			System.out.println("consumeOrder :"+consumeOrder);
			/*Commented by Manoj dtd 18/01/2014 not required
			sql = "SELECT LINE_NO FROM CONSUME_ORD_DET WHERE CONS_ORDER = '"+consumeOrder+"'";
			System.out.println("sql :"+sql);
			rs = stmt.executeQuery(sql);
			while(rs.next())
			{
				lineNo.add(new Integer(rs.getInt("LINE_NO")));
			}
			//Statement and Resultset closed by manoj dtd 18/01/2014
			rs.close();
			rs=null;
			stmt.close();
			stmt=null;*/
			locCode = genericUtility.getColumnValue("loc_code",dom);
			System.out.println("locCode :"+locCode);
			if(locCode != null && locCode.trim().length() > 0)
			{
				availableYn = genericUtility.getColumnValue("available_yn",dom1);
				System.out.println("availableYn :"+availableYn);
				sql = "SELECT B.AVAILABLE FROM LOCATION A , INVSTAT B "
					 +"WHERE A.INV_STAT = B.INV_STAT AND A.LOC_CODE  = '"+locCode+"'";
				System.out.println("sql :"+sql);
				pstmt = conn.prepareStatement(sql);
				//rs = stmt.executeQuery(sql);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					available = rs.getString(1);
					System.out.println("available :"+available);
				}
				//Statement and Resultset closed by manoj dtd 18/01/2014
				rs.close();
				rs=null;
				//stmt.close();
				//stmt=null;
				pstmt.close();
				pstmt=null;
				if (!available.equalsIgnoreCase(availableYn))
				{
					errCode = "VTAVAIL";
					errString = itmDBAccess.getErrorString("",errCode,"","",conn);
					System.out.println("errString :"+errString);
					return errString;
				}
			}
			System.out.println("The size of ArrayList :"+lineNo.size());
			/*
			 Block commentd by manoj dtd 18/01/2014 not required
			for (int i =0; i < lineNo.size(); i++)
			{
				lineNoStr = lineNo.get(i).toString();
				//valueXmlString.append("<Detail>\r\n");
				//valueXmlString.append("<line_no__ord isSrvCallOnChg='1'>").append("<![CDATA[").append(lineNo.get(i).toString()).append("]]>").append("</line_no__ord>\r\n");
				//valueXmlString.append("<loc_code>").append("<![CDATA[").append(locCode.trim()).append("]]>").append("</loc_code>\r\n");

				itemChgRetStr = getItemChanged(dom1,consumeOrder,lineNoStr,conn);
				itemChgXmlString.append(itemChgRetStr);
				/*-- Commented and Changed Below - Gulzar 25-04-07
				allocateStr = actionAllocate(dom, dom1, xtraParams);
				//Gulzar 22/02/07
				if (allocateStr.indexOf("<Errors>") > 0)
				{
					return allocateStr;
				}
				//Gulzar 22/02/07
				valueXmlString.append(allocateStr.substring(allocateStr.indexOf("<Detail>"),allocateStr.indexOf("</Root>"))); 
				//End Comment Gulzar 25-04-07
				//valueXmlString.append("</Detail>\r\n");
			}
			*/
			itemChgRetStr = getItemChanged(dom1,consumeOrder,conn);//Changed by manoj dtd 18/01/2014 no. of arguments changed
			itemChgXmlString.append(itemChgRetStr);
			//Added Changes - Gulzar - 25-04-07
			itemChgXmlString.append("</Root>\r\n");
			if(itemChgXmlString.toString() != null && itemChgXmlString.toString().trim().length()!=0)
			{
				System.out.println("After ItemChange Of Detail [itemChgXmlString] :"+itemChgXmlString.toString());
				dom = genericUtility.parseString(itemChgXmlString.toString()); 
			}
			allocateStr = actionAllocate(dom, dom1, xtraParams, actionType);
			if (allocateStr.indexOf("<Errors>") > 0)
			{
				return allocateStr;
			}
			//End Changes Gulzar 25-04-07 

			//stmt.close();
			//valueXmlString.append(allocateStr.substring(allocateStr.indexOf("<Detail>"),allocateStr.indexOf("</Root>"))); //Gulzar 25/04/07
			valueXmlString.append(allocateStr); //Gulzar 25/04/07
			valueXmlString.append("</Root>\r\n");
			/*if(allocateBuffer != null && allocateBuffer.toString().length() > 0){
				  valueXmlString = allocateBuffer;
			}*/
		}
		catch (SQLException sqx)
		{
			System.out.println("The sqlException occure in ConsumeIssueAct :"+sqx);
			throw new ITMException(sqx);
		}
		catch (Exception e)
		{
			System.out.println("The Exception occure in ConsumeIssueAct :"+e);
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
		System.out.println("valueXmlString return from ConsumeIssueAct[actionAllItems] :"+valueXmlString.toString());
		return valueXmlString.toString();
	}

	private String actionStock(Document dom, Document dom1, String xtraParams)throws RemoteException, ITMException
	{
		String siteCode = "", itemCode = "", quantity = "", availableYn = "", sql = "";
		String errCode = "", errString = "";
		String expDate1 = "", retestDate1 = "";
		java.sql.Date expDate = null, retestDate = null;
		int countItemCode = 0;
		Statement stmt = null;
		ResultSet rs = null;
		Connection conn = null;
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		/*ADDED BY HATIM ON 16/01/2006*/
		if (dom == null)
		{
			valueXmlString.append("</Root>\r\n");
			return valueXmlString.toString();
		}
		/*END*/
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		ConnDriver connDriver = new ConnDriver();
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 			
			stmt = conn.createStatement();
			PreparedStatement pstmt = null;//added by kailasg on 16-april-21 as per suggestion of SM sir.
			siteCode = genericUtility.getColumnValue("site_code__req",dom1);
			System.out.println("site_code__req :"+siteCode);
			itemCode = genericUtility.getColumnValue("item_code", dom);
			System.out.println("itemCode :"+itemCode);
			quantity = genericUtility.getColumnValue("quantity", dom);
			System.out.println("quantity :"+quantity);
			availableYn = genericUtility.getColumnValue("available_yn", dom1);
			System.out.println("availableYn :"+availableYn);

			sql = "SELECT COUNT(*) FROM STOCK A, INVSTAT B "
					+"WHERE A.INV_STAT  = B.INV_STAT "
					+"AND A.ITEM_CODE = '"+itemCode+"' "
					+"AND A.SITE_CODE = '"+siteCode+"' "
					+"AND B.AVAILABLE = '"+availableYn+"' "
					+"AND (CASE WHEN ((CASE WHEN A.QUANTITY IS NULL THEN 0 ELSE A.QUANTITY END) - (CASE WHEN A.ALLOC_QTY IS NULL THEN 0 ELSE A.ALLOC_QTY END)) IS NULL THEN 0 ELSE ((CASE WHEN A.QUANTITY IS NULL THEN 0 ELSE A.QUANTITY END) - (CASE WHEN A.ALLOC_QTY IS NULL THEN 0 ELSE A.ALLOC_QTY END))END)  > 0";
			System.out.println("SQL :"+sql);
			pstmt = conn.prepareStatement(sql);
			//rs = stmt.executeQuery(sql);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				countItemCode = rs.getInt(1);
				System.out.println("countItemCode :"+countItemCode);
            }
            //added by monika salla on 4 jan 21 to close prepared statements
                    rs.close();
					rs = null;
					pstmt.close();
					pstmt=null;
					
             //end
			if (countItemCode == 0)
			{
				errCode = "VTSTOCK1";
				errString = itmDBAccess.getErrorString("",errCode,"","",conn);
				System.out.println("errString :"+errString);
				return errString;
			}
			sql = "SELECT STOCK.ITEM_SER, "  
					+"STOCK.ITEM_CODE, " 
					+"STOCK.UNIT, " 
					+"STOCK.LOC_CODE, " 
					+"STOCK.LOT_NO," 
					+"STOCK.LOT_SL," 
					+"STOCK.QUANTITY - STOCK.ALLOC_QTY AS QUANTITY, " 
					+"STOCK.SITE_CODE, " 
					+"STOCK.EXP_DATE, " 
					+"STOCK.RETEST_DATE, " 
					+"STOCK.DIMENSION, " 
					+"STOCK.RATE "  
					+"FROM STOCK, INVSTAT " 
					+"WHERE STOCK.INV_STAT = INVSTAT.INV_STAT "
					+"AND STOCK.ITEM_CODE = '"+itemCode+"' "
					+"AND STOCK.SITE_CODE = '"+siteCode+"' "
					+"AND STOCK.QUANTITY - STOCK.ALLOC_QTY > 0 "
					+"AND INVSTAT.AVAILABLE = '"+availableYn+"' "
					+"ORDER BY CASE WHEN STOCK.EXP_DATE IS NULL THEN STOCK.CREA_DATE ELSE STOCK.EXP_DATE END ASC, STOCK.LOT_NO ASC, STOCK.LOT_SL ASC";
			System.out.println("sql :"+sql);						
			pstmt = conn.prepareStatement(sql);
			//rs = stmt.executeQuery(sql);
			rs = pstmt.executeQuery();
			
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			while (rs.next())
			{
				expDate = rs.getDate(9);
				if (expDate != null)
				{
					expDate1 = sdf.format(expDate);
					System.out.println("expDate1 :"+expDate1);
				}
				retestDate = rs.getDate(10);
				if (retestDate != null)
				{
					retestDate1 = sdf.format(retestDate); 
					System.out.println("retestDate1 :"+retestDate1);
				}
				valueXmlString.append("<Detail>\r\n");
				//valueXmlString.append("<line_no__ord>").append("<![CDATA[").append(genericUtility.getColumnValue("line_no__ord",dom)).append("]]>").append("</line_no__ord>\r\n");			
				//valueXmlString.append("<cons_order>").append("<![CDATA[").append(genericUtility.getColumnValue("cons_order",dom1)).append("]]>").append("</cons_order>\r\n");			
				valueXmlString.append("<item_ser>").append("<![CDATA[").append(rs.getString(1).trim()).append("]]>").append("</item_ser>\r\n");			
				valueXmlString.append("<item_code>").append("<![CDATA[").append(rs.getString(2).trim()).append("]]>").append("</item_code>\r\n");			
				valueXmlString.append("<unit>").append("<![CDATA[").append(rs.getString(3)).append("]]>").append("</unit>\r\n");			
				// 13-04-2007 manoharan trim removed
				//valueXmlString.append("<loc_code>").append("<![CDATA[").append(rs.getString(4).trim()).append("]]>").append("</loc_code>\r\n");			
				//valueXmlString.append("<lot_no>").append("<![CDATA[").append(rs.getString(5).trim()).append("]]>").append("</lot_no>\r\n");			
				//valueXmlString.append("<lot_sl>").append("<![CDATA[").append(rs.getString(6).trim()).append("]]>").append("</lot_sl>\r\n");			
				valueXmlString.append("<loc_code>").append("<![CDATA[").append(rs.getString(4)).append("]]>").append("</loc_code>\r\n");			
				valueXmlString.append("<lot_no>").append("<![CDATA[").append(rs.getString(5)).append("]]>").append("</lot_no>\r\n");			
				valueXmlString.append("<lot_sl>").append("<![CDATA[").append(rs.getString(6)).append("]]>").append("</lot_sl>\r\n");			
				// end 13-04-2007 manoharan
				
				valueXmlString.append("<quantity>").append("<![CDATA[").append(rs.getDouble(7)).append("]]>").append("</quantity>\r\n");			
				valueXmlString.append("<exp_date>").append("<![CDATA[").append(expDate1).append("]]>").append("</exp_date>\r\n");			
				valueXmlString.append("<retest_date>").append("<![CDATA[").append(retestDate1).append("]]>").append("</retest_date>\r\n");			
				//added Arun pal
				//valueXmlString.append("<rate>").append("<![CDATA[").append(rs.getDouble(12)).append("]]>").append("</rate>\r\n");			
				valueXmlString.append("<dimension>").append("<![CDATA[").append(rs.getString(11) == null?"":rs.getString(11).trim()).append("]]>").append("</dimension>\r\n");			
				valueXmlString.append("<site_code>").append("<![CDATA[").append(rs.getString(8).trim()).append("]]>").append("</site_code>\r\n");			
				valueXmlString.append("</Detail>\r\n");
				retestDate1 = "";
				expDate1 = "";
			}//while end
            stmt.close();
            //added by monika salla on 4 jan 21 to close prepared statements
                    rs.close();
					rs = null;
					pstmt.close();
					pstmt=null;
					
             //end
			valueXmlString.append("</Root>\r\n");			
		}//try end
		catch (SQLException sqx)
		{
			System.out.println("The SQLException occurs in ConsumeIssueAct for Packet button :"+sqx);
			throw new ITMException(sqx);
		}
		catch (Exception e)
		{
			System.out.println("The Exception occure in ConsumeIssueAct  for Packet button:"+e);
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
		System.out.println("valueXmlString return from ConsumeIssueAct[actionStock]] :"+valueXmlString.toString());
		return valueXmlString.toString();
	}

	private String stockTransform(Document dom,Document dom1, String objContext, String xtraParams, Document selDataDom)throws ITMException
	{
		System.out.println("stockTransform is calling.............");
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		String sql = "", issCriteria  = "", acctCodeInv = "", cctrCodeInv = "";
		String detCnt = "0", errCode = "", errString = "";
		Connection conn = null;
		Statement stmt = null;
		ResultSet rset = null;
		String quantity = "", acctCode = "", cctrCode = "", taxChap = "", taxClass = "", taxEnv = "";
		String consIssue = "", consOrder = "", lineNoOrd = "", siteCodeReq = "", itemCode = "", partyQty = "";   
		String locCode = "", lotNo = "", lotSl = "", unit = "", stockQty = "", rate = "";
		Node currDetail = null, currDetail1 = null; 
		int count = 0;
		double stkQty = 0, noArt = 0, qtyPerArt = 0;
		ConnDriver connDriver = new ConnDriver();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			//stmt = conn.createStatement();
			PreparedStatement pstmt = null;//added & commented by kailasg on 16-april-21 as per suggestion of SM sir.
			//detCnt = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"detCnt"); //Will be Uncommented later - Aviprash 30/01/06
			consIssue = new  ibase.utility.E12GenericUtility().getColumnValue("cons_issue", dom1);
			consOrder = new  ibase.utility.E12GenericUtility().getColumnValue("cons_order", dom);
			lineNoOrd = new  ibase.utility.E12GenericUtility().getColumnValue("line_no__ord", dom);
			siteCodeReq = new  ibase.utility.E12GenericUtility().getColumnValue("site_code__req", dom1);
			itemCode = new  ibase.utility.E12GenericUtility().getColumnValue("item_code", dom);
			quantity = new  ibase.utility.E12GenericUtility().getColumnValue("quantity", dom);
			acctCode = new  ibase.utility.E12GenericUtility().getColumnValue("acct_code", dom);
			cctrCode = new  ibase.utility.E12GenericUtility().getColumnValue("cctr_code", dom);
			taxChap = new  ibase.utility.E12GenericUtility().getColumnValue("tax_chap", dom);
			taxClass = new  ibase.utility.E12GenericUtility().getColumnValue("tax_class", dom);
			taxEnv = new  ibase.utility.E12GenericUtility().getColumnValue("tax_env", dom);
			double quantity1 = Double.parseDouble(quantity);
			sql = "SELECT ISS_CRITERIA FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
			System.out.println("sql :"+sql);
			pstmt = conn.prepareStatement(sql);
			//rs = stmt.executeQuery(sql);
			rset = pstmt.executeQuery();
			//rset = stmt.executeQuery(sql);
			if (rset.next())
			{
				issCriteria = rset.getString(1);
				System.out.println("issCriteria  :"+issCriteria);
			}
			// Changed by Manish on 01/04/16 for max cursor issue [start]
			if (rset !=null)
			{
				rset.close();
                rset = null;
                
            }
            
			// Changed by Manish on 01/04/16 for max cursor issue [end]
			NodeList detailList = selDataDom.getElementsByTagName("Detail");
			int noOfDetails = detailList.getLength();
			for(int ctr = 0; ctr < noOfDetails; ctr++)
			{
				currDetail = detailList.item(ctr);
				stkQty = stkQty + Double.parseDouble((new  ibase.utility.E12GenericUtility().getColumnValueFromNode("quantity", currDetail)));
			}
			System.out.println("stkQty  :"+stkQty);
			partyQty = new  ibase.utility.E12GenericUtility().getColumnValue("part_qty", dom1);
			System.out.println("partyQty  :"+partyQty);
			if (partyQty == null)
			{
				partyQty = "X";
			}
			if (partyQty.equals("X"))
			{
				if (stkQty < quantity1)
				{
					errCode = "VTSTOCK1";			
					errString = itmDBAccess.getErrorString("",errCode,"","",conn);				
					conn.close();
					conn = null;
					return errString;
				}
			}
			else if(partyQty.equals("W"))
			{
					errCode = "VTSTOCK2";			
					errString = itmDBAccess.getErrorString("",errCode,"","",conn);				
					System.out.println("errString :"+errString);
			}
			if (noOfDetails > 0)
			{
				for(int ctr = 0; ctr < noOfDetails && quantity1 > 0; ctr++)
				{
					valueXmlString.append("<Detail>");
					currDetail1 = detailList.item(ctr);
					locCode = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("loc_code", currDetail1);
					lotNo = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("lot_no", currDetail1);
					lotSl = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("lot_sl", currDetail1);
					unit = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("unit", currDetail1);
					stockQty = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("quantity", currDetail1);
					rate = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("rate", currDetail1);
					System.out.println("rate==>  :"+rate);

					valueXmlString.append("<cons_issue isSrvCallOnChg='0'>").append("<![CDATA[").append(consIssue).append("]]>").append("</cons_issue>\r\n");
					valueXmlString.append("<cons_order isSrvCallOnChg='0'>").append("<![CDATA[").append(consOrder).append("]]>").append("</cons_order>\r\n");
					valueXmlString.append("<line_no__ord isSrvCallOnChg='0'>").append("<![CDATA[").append(lineNoOrd).append("]]>").append("</line_no__ord>\r\n");
					valueXmlString.append("<item_code isSrvCallOnChg='0'>").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code>\r\n");
					valueXmlString.append("<unit isSrvCallOnChg='0'>").append("<![CDATA[").append(unit).append("]]>").append("</unit>\r\n");
					valueXmlString.append("<unit__std isSrvCallOnChg='0'>").append("<![CDATA[").append(unit).append("]]>").append("</unit__std>\r\n");
					valueXmlString.append("<loc_code isSrvCallOnChg='0'>").append("<![CDATA[").append(locCode).append("]]>").append("</loc_code>\r\n");
					//changed by ALKA on 11/09/07 for no_art column added in the consume_iss_det and itemChange enabled for lot_no for request id "DI78GIN041"			
					//valueXmlString.append("<lot_no isSrvCallOnChg='0'>").append("<![CDATA[").append(lotNo).append("]]>").append("</lot_no>\r\n");
					valueXmlString.append("<lot_no isSrvCallOnChg='1'>").append("<![CDATA[").append(lotNo).append("]]>").append("</lot_no>\r\n");
					
					//Modified by Anjali R. on [01/10/2018][Itemchange should be called on lot_sl field][Start]					
					//valueXmlString.append("<lot_sl isSrvCallOnChg='0'>").append("<![CDATA[").append(lotSl).append("]]>").append("</lot_sl>\r\n");
					valueXmlString.append("<lot_sl isSrvCallOnChg='1'>").append("<![CDATA[").append(lotSl).append("]]>").append("</lot_sl>\r\n");
					//Modified by Anjali R. on [01/10/2018][Itemchange should be called on lot_sl field][End]
					
					valueXmlString.append("<conv_qty_stduom isSrvCallOnChg='0'>").append("<![CDATA[").append("1").append("]]>").append("</conv_qty_stduom>\r\n");
					valueXmlString.append("<acct_code isSrvCallOnChg='0'>").append("<![CDATA[").append(acctCode).append("]]>").append("</acct_code>\r\n");
					valueXmlString.append("<cctr_code isSrvCallOnChg='0'>").append("<![CDATA[").append(cctrCode).append("]]>").append("</cctr_code>\r\n");
					valueXmlString.append("<tax_chap isSrvCallOnChg='0'>").append("<![CDATA[").append(taxChap == null?"":taxChap.trim()).append("]]>").append("</tax_chap>\r\n");
					valueXmlString.append("<tax_class isSrvCallOnChg='0'>").append("<![CDATA[").append(taxClass == null?"":taxClass.trim()).append("]]>").append("</tax_class>\r\n");
					valueXmlString.append("<tax_env isSrvCallOnChg='0'>").append("<![CDATA[").append(taxEnv == null?"":taxEnv.trim()).append("]]>").append("</tax_env>\r\n");
					//Added arun pal
					//valueXmlString.append("<rate isSrvCallOnChg='0'>").append("<![CDATA[").append(Double.parseDouble(rate)).append("]]>").append("</rate>\r\n");
					sql = "SELECT ACCT_CODE__INV, CCTR_CODE__INV, QTY_PER_ART FROM STOCK " // 18/10/13 manoharan qty_per_art added
						 +"WHERE ITEM_CODE = '"+itemCode+"' "
						 +"AND SITE_CODE = '"+siteCodeReq+"' "
						 +"AND LOC_CODE = '"+locCode+"' "
						 +"AND LOT_NO = '"+lotNo+"' "
						 +"AND LOT_SL = '"+lotSl+"' ";
					System.out.println("sql :"+sql);
					//rset = stmt.executeQuery(sql);
					pstmt = conn.prepareStatement(sql);
					//rs = stmt.executeQuery(sql);
					rset = pstmt.executeQuery();
					if (rset.next())
					{
						acctCodeInv = rset.getString(1);
						cctrCodeInv = rset.getString(2);
						qtyPerArt = rset.getDouble("QTY_PER_ART");
						System.out.println("acctCodeInv  :"+acctCodeInv+"\n cctrCodeInv :"+cctrCodeInv);
						valueXmlString.append("<acct_code__inv isSrvCallOnChg='0'>").append("<![CDATA[").append(acctCodeInv).append("]]>").append("</acct_code__inv>\r\n");
						valueXmlString.append("<cctr_code__inv isSrvCallOnChg='0'>").append("<![CDATA[").append(cctrCodeInv).append("]]>").append("</cctr_code__inv>\r\n");
					}
					// Changed by Manish on 01/04/16 for max cursor issue [start]
					if (rset !=null)
					{
						rset.close();
						rset = null;
					}
                    // Changed by Manish on 01/04/16 for max cursor issue [end]
                   
					if (Double.parseDouble(stockQty) <= quantity1)
					{
						valueXmlString.append("<quantity isSrvCallOnChg='0'>").append("<![CDATA[").append(stockQty).append("]]>").append("</quantity>\r\n");
						valueXmlString.append("<quantity isSrvCallOnChg='0'>").append("<![CDATA[").append(stockQty).append("]]>").append("</quantity>\r\n");
						valueXmlString.append("<quantity__std isSrvCallOnChg='0'>").append("<![CDATA[").append(stockQty).append("]]>").append("</quantity__std>\r\n");//updated by nisar on 30/11/07 
						quantity1 = quantity1 - Double.parseDouble(stockQty);
						noArt = (Double.parseDouble(stockQty)) / qtyPerArt;
						System.out.println("Manohar 17/10/13  before noArt [" + noArt + "] Math.floor(noArt) [" + Math.floor(noArt) + "]");
						if ( (noArt - Math.floor(noArt) ) != 0 )
						{
							noArt = Math.floor(noArt + 0.5d);
						}
						else
						{
							noArt = Math.floor(noArt);
						}
						System.out.println("Manohar 17/10/13  after noArt [" + noArt + "]");
					}
					else
					{
						if (issCriteria != null &&  issCriteria.equals("W"))
						{
							valueXmlString.append("<quantity isSrvCallOnChg='0'>").append("<![CDATA[").append(stockQty).append("]]>").append("</quantity>\r\n");
							valueXmlString.append("<quantity__std isSrvCallOnChg='0'>").append("<![CDATA[").append(stockQty).append("]]>").append("</quantity__std>\r\n");
							noArt = (Double.parseDouble(stockQty)) / qtyPerArt;
							System.out.println("Manohar 17/10/13  before noArt [" + noArt + "] Math.floor(noArt) [" + Math.floor(noArt) + "]");
							if ( (noArt - Math.floor(noArt) ) != 0 )
							{
								noArt = Math.floor(noArt + 0.5d);
							}
							else
							{
								noArt = Math.floor(noArt);
							}
							System.out.println("Manohar 17/10/13  after noArt [" + noArt + "]");
						}
						else
						{
							valueXmlString.append("<quantity isSrvCallOnChg='0'>").append("<![CDATA[").append(quantity1).append("]]>").append("</quantity>\r\n");
							valueXmlString.append("<quantity__std isSrvCallOnChg='0'>").append("<![CDATA[").append(quantity1).append("]]>").append("</quantity__std>\r\n");
							noArt = quantity1 / qtyPerArt;
							System.out.println("Manohar 17/10/13  before noArt [" + noArt + "] Math.floor(noArt) [" + Math.floor(noArt) + "]");
							if ( (noArt - Math.floor(noArt) ) != 0 )
							{
								noArt = Math.floor(noArt + 0.5d);
							}
							else
							{
								noArt = Math.floor(noArt);
							}
							System.out.println("Manohar 17/10/13  after noArt [" + noArt + "]");
						}
						valueXmlString.append("<no_art>").append("<![CDATA[").append(noArt).append("]]>").append("</no_art>\r\n");
						quantity1 = 0;
					}// end else
					valueXmlString.append("</Detail>");
				}// for end
			}// end if
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
				System.out.println("Closing Connection...");
				conn.close();
				conn = null;
			}
			catch (Exception e){}
		}
		System.out.println("valueXmlString from :"+valueXmlString.toString());
		return valueXmlString.toString();
	}
	//Added Changes By Gulzar - 24/04/07
	private String actionAllocate(Document dom, Document dom1, String xtraParams, String actionType)throws RemoteException, ITMException // Added - Gulzar 25/04/07
	{
		String tranType = "", itemSer = "", consIss = "", consOrd = "", lineOrd = "", siteCodeReq = "";     
		String itemCode = "", itemDescr = "", locCode = "", lotNo = "", lotSl = "", quantity = "";    
		String acctCode = "", cctrCode = "", taxChap = "", taxClass = "", taxEnv = "", availableYn = "";
		String allocDate = "", partQuantity = "", errCode = "", errString = "", sql = "", retResult = "";
		String acctCodeInv = "", cctrCodeInv = "", sql1 = "";
		String validateStr = "",lotSerial = ""; 
		double remainingQty = 0d, inputQty = 0d, qtyPerArt = 0, noArt = 0;
		int minShelfLife = 0, noOfStkDet = 0;
		String trackShelfLife = "", chkDate = "";
		ArrayList acctCodeInvArrLst = new ArrayList();
		java.util.Date chkDate1 = null, expDate1 = null;;
		java.sql.Date expDate = null;
		double  
		stockQuantity = 0d;
		
		String lotNum = "", locCode1 = "";
		double hmQty = 0d;
		NodeList detailList = null; //Gulzar 25/04/07
		Node currDetail = null;
		int	detailListLength = 0; //Gulzar 25/04/07
		HashMap hm = new HashMap(); //Gulzar 25/04/07

		Statement stmt = null, stmt1 = null;
		Connection conn = null;
		ResultSet rs = null, rs1 = null;
		ConnDriver connDriver = new ConnDriver();
		PreparedStatement pstmt = null;//added by kailasg on 16-april-21 as per suggestion of SM sir.
		PreparedStatement pstmt1 = null;//added by kailasg on 16-april-21 as per suggestion of SM sir.
		//StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n"); //Gulzar 25/04/07
		StringBuffer valueXmlString = new StringBuffer(""); //Gulzar 25/04/07
		boolean stkExpFlag = false;
		FinCommon fincommon = new FinCommon(); //added by kailasg on 19-april -21 for using getAcctDetrTtype() method instead of acctDetrTType()
		/*ADDED BY HATIM ON 16/01/2006*/
		if (dom == null)
		{
			valueXmlString.append("</Root>\r\n");
			return valueXmlString.toString();
		}
		/*END*/

		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		double rate=0;
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			System.out.println("[Validating .....]");
			//Added the validation code by Hatim 23/12/2005 		
			//Validator consumeIssueRemote = consumeIssueHome.create();
			//System.out.println("ConsumeIssue Created");

			//Added Changes - Gulzar - 25-04-07
			detailList = dom.getElementsByTagName("Detail2");
			detailListLength = detailList.getLength();
			System.out.println("detailListLength : " + detailListLength);
			System.out.println("actionType : " + actionType);
			if (actionType.equalsIgnoreCase("Allocate"))
			{
				detailListLength = 1;
			}
			for (int ctr = 0;ctr < detailListLength;ctr++)
			{
				stmt = conn.createStatement();
				currDetail = detailList.item(ctr);
				/* --Commented Temporarly - Gulzar 25/04/07
				validateStr = consumeIssueRemote.wfValData(dom, dom1, null, "2", "E", xtraParams);
				if (validateStr != null && validateStr.trim().length() > 0 ) 
				{
					System.out.println("[Validation Error]");
					return validateStr;
				}
				System.out.println("[Validation Compeleted!]");
				*///End Temporarly Comment
				// END VALIDATTION CODE - 
				//Added on 25/10/06
				String varValue = "",stkExpLoc = "";
				//varValue = itmDBAccess.getEnvDis("999999","NEAREXP_LOC",null);
				varValue = itmDBAccess.getEnvDis("999999","NEAREXP_LOC",conn);
				if(varValue != null && varValue.trim().length() > 0 && !(varValue.equalsIgnoreCase("NULLFOUND")))
				{
					stkExpLoc = varValue;
				}
				//End - 25/10/06
				SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
				/* --Commented - Gulzar - 25/04/07
				tranType = genericUtility.getColumnValue("tran_type", dom1);
				itemSer = genericUtility.getColumnValue("item_ser", dom1);
				consIss = genericUtility.getColumnValue("cons_issue", dom1);
				consOrd = genericUtility.getColumnValue("cons_order", dom);
				lineOrd = genericUtility.getColumnValue("line_no__ord", dom);
				siteCodeReq = genericUtility.getColumnValue("site_code__req", dom1);
				itemCode = genericUtility.getColumnValue("item_code", dom);
				itemDescr = genericUtility.getColumnValue("item_descr", dom);
				locCode = genericUtility.getColumnValue("loc_code",dom);
				lotNo = genericUtility.getColumnValue("lot_no", dom);
				lotSl =	genericUtility.getColumnValue("lot_sl", dom);
				quantity = genericUtility.getColumnValue("quantity", dom);
				acctCode = genericUtility.getColumnValue("acct_code", dom);
				cctrCode = genericUtility.getColumnValue("cctr_code", dom);
				taxChap = genericUtility.getColumnValue("tax_chap", dom);
				taxClass = genericUtility.getColumnValue("tax_class", dom);
				taxEnv = genericUtility.getColumnValue("tax_env", dom);
				availableYn = genericUtility.getColumnValue("available_yn", dom1);
				allocDate = genericUtility.getColumnValue("issue_date", dom1);
				partQuantity = genericUtility.getColumnValue("part_qty", dom1);
				*/// End Comment - Gulzar - 25/04/07
				//Added - Gulzar - 25/04/07
				tranType = genericUtility.getColumnValue("tran_type", dom1);
				itemSer = genericUtility.getColumnValue("item_ser", dom1);
				consIss = genericUtility.getColumnValue("cons_issue", dom1);
				consOrd = genericUtility.getColumnValueFromNode("cons_order", currDetail);
				lineOrd = genericUtility.getColumnValueFromNode("line_no__ord", currDetail);
				siteCodeReq = genericUtility.getColumnValue("site_code__req", dom1);
				itemCode = genericUtility.getColumnValueFromNode("item_code", currDetail);
				itemDescr = genericUtility.getColumnValueFromNode("item_descr", currDetail);
				locCode = genericUtility.getColumnValueFromNode("loc_code",currDetail);
				lotNo = genericUtility.getColumnValueFromNode("lot_no", currDetail);
				lotSl =	genericUtility.getColumnValueFromNode("lot_sl", currDetail);
				quantity = genericUtility.getColumnValueFromNode("quantity", currDetail);
				acctCode = genericUtility.getColumnValueFromNode("acct_code", currDetail);
				cctrCode = genericUtility.getColumnValueFromNode("cctr_code", currDetail);
				taxChap = genericUtility.getColumnValueFromNode("tax_chap", currDetail);
				taxClass = genericUtility.getColumnValueFromNode("tax_class", currDetail);
				taxEnv = genericUtility.getColumnValueFromNode("tax_env", currDetail);
				availableYn = genericUtility.getColumnValue("available_yn", dom1);
				allocDate = genericUtility.getColumnValue("issue_date", dom1);
				partQuantity = genericUtility.getColumnValue("part_qty", dom1);
				//End Add - Gulzar - 25/04/07
				System.out.println("locCode :"+locCode);
				//Changes done by Chandni  shah -01/09/10
				if(partQuantity == null || partQuantity.trim().length()== 0) // partial quantity is set as X when it is null
				{
					partQuantity = " ";
				}
				
				if (locCode == null || "null".equalsIgnoreCase(locCode) || locCode.trim().length() == 0)
				{
					System.out.println("If locCode = null || length() == 0");
					locCode = "%";			
					System.out.println("If locCodelocCode is null then [%] :" +locCode);
				}
				if (lotNo == null || lotNo.trim().length() == 0)
				// The stock join location master and join invstat with location master inv_st, 
				// join the location master loc_code with stock loc_code
				{
					//sql = "SELECT (CASE WHEN SUM(A.QUANTITY - A.ALLOC_QTY) IS NULL THEN 0 ELSE SUM(A.QUANTITY - A.ALLOC_QTY) END) "
					sql = "SELECT SUM(A.QUANTITY-CASE WHEN A.ALLOC_QTY IS NULL THEN 0 ELSE A.ALLOC_QTY END-CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END) "
						 +"FROM STOCK A, LOCATION L, INVSTAT B "
						 +"WHERE L.LOC_CODE = A.LOC_CODE "
						 + " AND L.INV_STAT  = B.INV_STAT "
						 +"AND A.ITEM_CODE = '"+itemCode+"' "
						 +"AND A.SITE_CODE = '"+siteCodeReq+"' "
						 +"AND B.AVAILABLE = '"+availableYn+"' "
						// +"AND A.QUANTITY  > 0 "
						 +"AND A.LOC_CODE  LIKE '"+locCode+"' "
						 +" HAVING SUM(A.QUANTITY-CASE WHEN A.ALLOC_QTY IS NULL THEN 0 ELSE A.ALLOC_QTY END-CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END) > 0 ";
					
					System.out.println("locCode in query ===" + locCode );
					
				}
				//Added by Chandni  shah -01/09/10
				else 
				{
					//sql = "SELECT (CASE WHEN SUM(A.QUANTITY - A.ALLOC_QTY) IS NULL THEN 0 ELSE SUM(A.QUANTITY - A.ALLOC_QTY) END) "
					sql = "SELECT SUM(A.QUANTITY-CASE WHEN A.ALLOC_QTY IS NULL THEN 0 ELSE A.ALLOC_QTY END-CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END) "
						 +"FROM STOCK A, LOCATION L, INVSTAT B "
						 +"WHERE L.LOC_CODE = A.LOC_CODE "
						 + " AND L.INV_STAT  = B.INV_STAT "
						  +"AND A.ITEM_CODE = '"+itemCode+"' "
						  +"AND A.SITE_CODE = '"+siteCodeReq+"' "
						  +"AND A.LOC_CODE  = '"+locCode+"' "
						  +"AND A.LOT_NO    = '"+lotNo+"' "
						  +"AND A.LOT_SL	 = '"+lotSl+"' "
					//	  +"AND (A.QUANTITY - A.ALLOC_QTY) >="+quantity
						  +" AND B.AVAILABLE = '"+availableYn+"'"
						  +" HAVING SUM(A.QUANTITY-CASE WHEN A.ALLOC_QTY IS NULL THEN 0 ELSE A.ALLOC_QTY END-CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END) > 0 ";
						  
				}//
				System.out.println("sql :"+sql);
				pstmt = conn.prepareStatement(sql);
				//rs = stmt.executeQuery(sql);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					stockQuantity = rs.getDouble(1);
					System.out.println("stockQuantity :"+stockQuantity);
					//acctCode = rs.getString(2);//
					//System.out.println("acctCode :"+acctCode);//
					//cctrCode = rs.getString(3);//
					//System.out.println("cctrCode :"+cctrCode);//
				}
				
				//Changes done by Chandni  shah -01/09/10
				
				if (stockQuantity == 0  && "X".equals (partQuantity))
				{
					errCode = "VTNOSTK";
					errString = itmDBAccess.getErrorString("",errCode,"","",conn);
					System.out.println("errString :"+errString);
					return errString;
				}
				else if (stockQuantity == 0)
				{
					continue;
				}	
				//
				rs.close();
				rs=null;
				//stmt.close();
				//stmt=null;
				pstmt.close();
				pstmt=null;
				
				if (lotNo == null || lotNo.trim().length() == 0) //Changes done by Chandni  shah -01/09/10 
				{
					// The stock join location master and join invstat with location master inv_st, 
					// join the location master loc_code with stock loc_code
					sql = "SELECT A.LOT_NO, A.LOT_SL,(A.QUANTITY - CASE WHEN A.ALLOC_QTY IS NULL THEN 0 ELSE A.ALLOC_QTY END -CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END), A.EXP_DATE, A.UNIT, "
						  +"A.ITEM_SER, A.SITE_CODE__MFG, A.MFG_DATE, A.POTENCY_PERC, A.ALLOC_QTY, " 
						  +"A.PACK_CODE, A.LOC_CODE, A.ACCT_CODE__INV,A.CCTR_CODE__INV, A.RATE, a.qty_per_art as QTY_PER_ART "  
						  +"FROM STOCK A, LOCATION L, INVSTAT B "
						  +"WHERE L.LOC_CODE = A.LOC_CODE "
						  + " AND L.INV_STAT  = B.INV_STAT "
						  +"AND A.ITEM_CODE = '"+itemCode+"' "
						  +"AND A.SITE_CODE = '"+siteCodeReq+"' "
						  +"AND B.AVAILABLE = '"+availableYn+"' "
						 // +"AND A.QUANTITY - A.ALLOC_QTY > 0 "
						  +"AND A.QUANTITY - CASE WHEN A.ALLOC_QTY IS NULL THEN 0 ELSE A.ALLOC_QTY END -CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END > 0 "
						  +"AND A.LOC_CODE  LIKE '"+locCode+"' "			
						  +"ORDER BY A.EXP_DATE, A.LOT_NO, A.LOT_SL ";
				}
				//Added else part by Chandni  shah -01/09/10
				else 
				{
					
					sql = "SELECT A.LOT_NO, A.LOT_SL,(A.QUANTITY - CASE WHEN A.ALLOC_QTY IS NULL THEN 0 ELSE A.ALLOC_QTY END -CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END), A.EXP_DATE, A.UNIT, "
						  +"A.ITEM_SER, A.SITE_CODE__MFG, A.MFG_DATE, A.POTENCY_PERC, A.ALLOC_QTY, " 
						  +"A.PACK_CODE, A.LOC_CODE, A.ACCT_CODE__INV,A.CCTR_CODE__INV, A.RATE, a.qty_per_art as QTY_PER_ART "  
						  +"FROM STOCK A, LOCATION L, INVSTAT B "
						  +"WHERE L.LOC_CODE = A.LOC_CODE "
						  + " AND L.INV_STAT  = B.INV_STAT "
						  +"AND A.ITEM_CODE = '"+itemCode+"' "
						  +"AND A.SITE_CODE = '"+siteCodeReq+"' "
						  +"AND A.LOC_CODE  = '"+locCode+"' "
						  +"AND A.LOT_NO    = '"+lotNo+"' "
						  +"AND A.LOT_SL	 = '"+lotSl+"' "
						  //+"AND (A.QUANTITY - A.ALLOC_QTY) >="+quantity
						  +"AND (A.QUANTITY - CASE WHEN A.ALLOC_QTY IS NULL THEN 0 ELSE A.ALLOC_QTY END -CASE WHEN A.HOLD_QTY IS NULL THEN 0 ELSE A.HOLD_QTY END) >="+quantity
						  +" AND B.AVAILABLE = '"+availableYn+"'";			
				}//
				System.out.println("sql :"+sql);
				//stmt = conn.createStatement(); //Gulzar 25/04/07
				//stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE); //Gulzar 25/04/07
				pstmt = conn.prepareStatement(sql,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE); //added and commented by kailasg on 19-april-21 as per suggestion from SM sir
				//rs = stmt.executeQuery(sql);
				rs = pstmt.executeQuery();
				while (rs.next())
				{
					lotNum = rs.getString(1); 
					locCode1 = rs.getString(12); 
					lotSerial = rs.getString(2); // 11/10/13 manoharan included lot_sl
					System.out.println("lotNum :"+lotNum);
					System.out.println("locCode1 :"+locCode1);
					//if (! hm.containsKey(itemCode+"~"+siteCodeReq+"~"+locCode1+"~"+lotNum)) // 11/10/13 manoharan commented and included lot_sl
					if (! hm.containsKey(itemCode+"~"+siteCodeReq+"~"+locCode1+"~"+lotNum + "~" + lotSerial ))
					{
						//hm.put(itemCode+"~"+siteCodeReq+"~"+locCode1+"~"+lotNum,new Double(rs.getDouble(3))); // 11/10/13 manoharan commented and included lot_sl
						hm.put(itemCode+"~"+siteCodeReq+"~"+locCode1+"~"+lotNum+"~"+ lotSerial,new Double(rs.getDouble(3)));
					}
                }
                //added by monika salla on 4 jan 21 to close prepared statements
                   // rs.close();
					//rs = null;
					
                //end//commented by monika salla 0n 21 jan 21
				System.out.println("Hashmap :"+hm);
				remainingQty = Double.parseDouble(quantity);
				System.out.println("remainingQty :"+remainingQty);
				rs.beforeFirst(); //Gulzar 25/04/07
				while (rs.next())
				{
					noOfStkDet++;
					lotNum = rs.getString(1);
					locCode1 = rs.getString(12); 
					lotSerial = rs.getString(2);
					qtyPerArt = rs.getDouble("QTY_PER_ART"); // 17/10/13 manoharan to calculate no_art
					System.out.print("Combination Key :  "+itemCode+"~"+siteCodeReq+"~"+locCode1+"~"+lotNum + "~" + lotSerial);
					hmQty = Double.parseDouble((hm.get(itemCode+"~"+siteCodeReq+"~"+locCode1+"~"+lotNum + "~" + lotSerial)).toString());
					System.out.println(" ::Value [hmQty] :: "+hmQty);
					if (hmQty == 0)
					{
						continue;
					}
					if (availableYn.equals("Y"))
					{
						sql1 = "SELECT MIN_SHELF_LIFE, (CASE WHEN TRACK_SHELF_LIFE IS NULL THEN 'N' ELSE TRACK_SHELF_LIFE END) " 
							 +"FROM ITEM WHERE ITEM_CODE = '"+itemCode+"' "; 
						System.out.println("sql1 :"+sql1);
						//stmt1 = conn.createStatement();
						//rs1 = stmt1.executeQuery(sql1);
						pstmt1 = conn.prepareStatement(sql1);
						rs1 = pstmt1.executeQuery();
						if (rs1.next())
						{
							minShelfLife = rs1.getInt(1);
							trackShelfLife = rs1.getString(2);
						}
						rs1.close();
						rs1=null;
						//stmt1.close(); //Gulzar 20/02/07
						//stmt1 = null; //Gulzar 20/02/07
						pstmt1.close(); //kailasg 19/04/21
						pstmt1 = null; //kailasg 19/04/21
						
						stkExpFlag = false;
						String token = "";
						StringTokenizer stToken = new StringTokenizer(stkExpLoc,",");
						while(stToken.hasMoreTokens())
						{
							token = stToken.nextToken();
							if(locCode.equalsIgnoreCase(token))
							{
								stkExpFlag = true;
								break;
							}
						}
						if(stkExpFlag == false){
							if (minShelfLife == 0)
							{
								minShelfLife = 1; 
							}
							if (trackShelfLife.equals("Y"))
							{
								chkDate = calcExpiry(allocDate,minShelfLife); 
								System.out.println("chkDate :"+chkDate);
								chkDate1 = sdf.parse(chkDate);
								System.out.println("chkDate1 :"+chkDate1);
								expDate = rs.getDate(4);
								System.out.println("expDate :"+expDate);
								if(expDate != null)
								{
									expDate1 = new java.util.Date(expDate.getTime());
									System.out.println("expDate1 :"+expDate1);
									if (chkDate1.compareTo(expDate1) > 0)
									{
										continue;
									}
								}							
							}
						}
					}
					System.out.println("remainingQty before if :"+remainingQty);
					System.out.println("rs.getDouble(3) :"+rs.getDouble(3));

					if (remainingQty == 0)
					{
						break;
					}
					//else if(rs.getDouble(3) >= remainingQty) //Gulzar 25/04/07
					else if(hmQty >= remainingQty)//Gulzar 25/04/07
					{
						inputQty = remainingQty;
						System.out.println("inputQty :"+inputQty);
						remainingQty = 0;
						hm.put(itemCode+"~"+siteCodeReq+"~"+locCode1+"~"+lotNum + "~" + lotSerial,new Double(hmQty - inputQty));
						//hm.put(itemCode+"~"+siteCodeReq+"~"+locCode1+"~"+lotNum,new Double(0));
						System.out.println("hmQty - remainingQty :"+(hmQty - inputQty));
						System.out.println("hm if [hmQty >= remainingQty] :"+hm);
					}
					//else if (rs.getDouble(3) < remainingQty) //Gulzar 25/04/07
					else if (hmQty < remainingQty) //Gulzar 25/04/07
					{
						//inputQty = rs.getDouble(3); //Gulzar 25/04/07
						inputQty = hmQty; //Gulzar 25/04/07
						System.out.println("inputQty :"+inputQty);
						//remainingQty = remainingQty - rs.getDouble(3);
						remainingQty = remainingQty - inputQty;
						System.out.println("remainingQty :"+remainingQty);
						hm.put(itemCode+"~"+siteCodeReq+"~"+locCode1+"~"+lotNum + "~" + lotSerial,new Double(0));
					}
					System.out.println("Hashmap :"+hm);
					valueXmlString.append("<Detail>\r\n");
					valueXmlString.append("<cons_issue>").append("<![CDATA[").append(consIss).append("]]>").append("</cons_issue>\r\n");
					System.out.println("rs.getDouble(15) :"+rs.getDouble(15));
					if (rs.getDouble(15) > 0)
					{
						rate=rs.getDouble(15);
						valueXmlString.append("<rate>").append("<![CDATA[").append(rs.getDouble(15)).append("]]>").append("</rate>\r\n");
					}
					else
					{
						sql1 = "SELECT RATE	FROM CONSUME_ORD_DET "
							  +"WHERE CONS_ORDER = '"+consOrd+"' "
							  +"AND LINE_NO = "+lineOrd+" ";		
						System.out.println("sql1 :"+sql1);
						//stmt1 = conn.createStatement();
						//rs1 = stmt1.executeQuery(sql1);
						pstmt1 = conn.prepareStatement(sql1);
						rs1 = pstmt1.executeQuery();
						if (rs1.next())
						{
							rate=rs1.getDouble(1);
							valueXmlString.append("<rate>").append("<![CDATA[").append(rate).append("]]>").append("</rate>\r\n");
						}
						//stmt1.close(); //Gulzar 20/02/07
                       // stmt1 = null //Gulzar 20/02/07
                        pstmt1.close(); //kailasg 19/04/21
						pstmt1 = null; //kailasg 19/04/21
                        
                        //added by monika salla on 4 jan 21 to close prepared statements
                    rs1.close();
					rs1 = null;
					
                     //end
					}
					//Manoj dtd 21/01/2013 to add  isSrvCallOnChg
					valueXmlString.append("<cons_order isSrvCallOnChg=\"0\">").append("<![CDATA[").append(consOrd).append("]]>").append("</cons_order>\r\n");
					valueXmlString.append("<line_no__ord isSrvCallOnChg=\"0\">").append("<![CDATA[").append(lineOrd).append("]]>").append("</line_no__ord>\r\n");
					valueXmlString.append("<item_code isSrvCallOnChg=\"0\">").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code>\r\n");
					valueXmlString.append("<item_descr isSrvCallOnChg=\"0\">").append("<![CDATA[").append(itemDescr).append("]]>").append("</item_descr>\r\n");
					valueXmlString.append("<quantity isSrvCallOnChg=\"0\">").append("<![CDATA[").append(inputQty).append("]]>").append("</quantity>\r\n");
					valueXmlString.append("<quantity__std isSrvCallOnChg=\"0\">").append("<![CDATA[").append(inputQty).append("]]>").append("</quantity__std>\r\n");
					valueXmlString.append("<unit isSrvCallOnChg=\"0\">").append("<![CDATA[").append(rs.getString(5)).append("]]>").append("</unit>\r\n");
					valueXmlString.append("<unit__std isSrvCallOnChg=\"0\">").append("<![CDATA[").append(rs.getString(5)).append("]]>").append("</unit__std>\r\n");
					valueXmlString.append("<loc_code isSrvCallOnChg=\"0\">").append("<![CDATA[").append(rs.getString(12)).append("]]>").append("</loc_code>\r\n");
					//changed by ALKA on 11/09/07 for no_art column added in the consume_iss_det and itemChange enabled for lot_no for request id "DI78GIN041"						
					//valueXmlString.append("<lot_no>").append("<![CDATA[").append(rs.getString(1)).append("]]>").append("</lot_no>\r\n");
					valueXmlString.append("<lot_sl isSrvCallOnChg=\"0\">").append("<![CDATA[").append(rs.getString(2)).append("]]>").append("</lot_sl>\r\n");
					valueXmlString.append("<lot_no isSrvCallOnChg='0'>").append("<![CDATA[").append(rs.getString(1)).append("]]>").append("</lot_no>\r\n");
					valueXmlString.append("<conv_qty_stduom isSrvCallOnChg=\"0\">").append("<![CDATA[").append(1).append("]]>").append("</conv_qty_stduom>\r\n");
					valueXmlString.append("<acct_code isSrvCallOnChg=\"0\">").append("<![CDATA[").append((acctCode == null) ? "":acctCode).append("]]>").append("</acct_code>\r\n");
					valueXmlString.append("<cctr_code isSrvCallOnChg=\"0\">").append("<![CDATA[").append(cctrCode).append("]]>").append("</cctr_code>\r\n");
					valueXmlString.append("<tax_chap isSrvCallOnChg=\"0\">").append("<![CDATA[").append((taxChap == null) ? "":taxChap).append("]]>").append("</tax_chap>\r\n");
					valueXmlString.append("<tax_class isSrvCallOnChg=\"0\">").append("<![CDATA[").append((taxClass == null) ? "":taxClass).append("]]>").append("</tax_class>\r\n");
					valueXmlString.append("<tax_env isSrvCallOnChg=\"0\">").append("<![CDATA[").append((taxEnv == null) ? "":taxEnv).append("]]>").append("</tax_env>\r\n");
					valueXmlString.append("<amount>").append("<![CDATA[").append(inputQty*rate).append("]]>").append("</amount>\r\n");
					
					// 17/10/13 Manoharan set no_art
					
					
					noArt = inputQty / qtyPerArt;
					System.out.println("Manohar 17/10/13  before noArt [" + noArt + "] Math.floor(noArt) [" + Math.floor(noArt) + "]");
					if ( (noArt - Math.floor(noArt) ) != 0 )
					{
						noArt = Math.floor(noArt + 0.5d);
					}
					else
					{
						noArt = Math.floor(noArt);
					}
					System.out.println("Manohar 17/10/13  after noArt [" + noArt + "]");
					
					System.out.println("rs.getString(13) :"+rs.getString(13)+" \nrs.getString(14) :"+rs.getString(14));
					if ((rs.getString(13) == null || rs.getString(13).trim().length() == 0) || (rs.getString(14) == null || rs.getString(14).trim().length() == 0)) 
					{
						//retResult = acctDetrTType(itemCode, rs.getString(6), "IN", tranType);
						retResult = fincommon.getAcctDetrTtype(itemCode, rs.getString(6), "IN", tranType,conn); // commented added by kailasg on 19-april-21 for getting nullpointer exception while On click of all item button
						System.out.println("retResult :"+retResult);
						/*if (retResult.substring(retResult.length()-5).equals("DS000")) //Change the Error Code
						{
							acctCodeInv = " ";
							cctrCodeInv = " ";
						}*/
						 if (retResult != null && retResult.trim().length() > 0) // commented & added by kailasg on 19-april-21 for getting nullpointer exception while On click of all item button
						  {
						  acctCodeInv = retResult.substring(0,retResult.indexOf(","));
						  cctrCodeInv = retResult.substring(retResult.indexOf(",")+1);
						  }
						else
						{
							acctCodeInvArrLst = genericUtility.getTokenList(retResult, "\t");
							System.out.println("acctCodeInvArrLst.size :"+acctCodeInvArrLst.size());
							System.out.println("acctCodeInvArrLst.get(1) :"+(String)acctCodeInvArrLst.get(1));
							acctCodeInv = (String)acctCodeInvArrLst.get(0); 
							System.out.println("acctCodeInv :"+acctCodeInv);
							cctrCodeInv = (String)acctCodeInvArrLst.get(1);
							System.out.println("cctrCodeInv :"+cctrCodeInv);
						}
					}
					if (rs.getString(13) == null)
					{
						valueXmlString.append("<acct_code__inv isSrvCallOnChg=\"0\">").append("<![CDATA[").append(acctCodeInv).append("]]>").append("</acct_code__inv>\r\n");
					}
					else
					{
						valueXmlString.append("<acct_code__inv isSrvCallOnChg=\"0\">").append("<![CDATA[").append(rs.getString(13)).append("]]>").append("</acct_code__inv>\r\n");
					}
					if (rs.getString(14) == null)
					{
						valueXmlString.append("<cctr_code__inv isSrvCallOnChg=\"0\">").append("<![CDATA[").append(cctrCodeInv).append("]]>").append("</cctr_code__inv>\r\n");
					}
					else
					{
						valueXmlString.append("<cctr_code__inv isSrvCallOnChg=\"0\">").append("<![CDATA[").append(rs.getString(14)).append("]]>").append("</cctr_code__inv>\r\n");
					}
					valueXmlString.append("<no_art isSrvCallOnChg=\"0\">").append("<![CDATA[").append(noArt).append("]]>").append("</no_art>\r\n");
					valueXmlString.append("</Detail>\r\n");
				}// while end
				/*if (noOfStkDet == 0)
				{
					errCode = "VTSTKW";
				}*/
				rs.close();
				rs=null;
				//stmt.close();
				//stmt=null;
				pstmt.close(); //kailasg 19/04/21
				pstmt = null; //kailasg 19/04/21
				if (remainingQty > 0)   //changed by Chandni Shah 01/09/10
				{
					if (partQuantity.equals("X")) 
					{
						errCode = "VTSTOCK1";
						errString = itmDBAccess.getErrorString("",errCode,"","",conn);
						System.out.println("errString :"+errString);
						return errString;
					
					}
				}
				//end if
				//Commented by Chandni  shah -01/09/10
				/*else
				{
					sql = "SELECT (CASE WHEN (A.QUANTITY - A.ALLOC_QTY) IS NULL THEN 0 ELSE (A.QUANTITY - A.ALLOC_QTY) END), "
						  +"ACCT_CODE__INV, CCTR_CODE__INV "
						  +"FROM STOCK A, INVSTAT B "
						  +"WHERE A.INV_STAT  = B.INV_STAT "
						  +"AND A.ITEM_CODE = '"+itemCode+"' "
						  +"AND A.SITE_CODE = '"+siteCodeReq+"' "
						  +"AND A.LOC_CODE  = '"+locCode+"' "
						  +"AND A.LOT_NO    = '"+lotNo+"' "
						  +"AND A.LOT_SL	 = '"+lotSl+"' "
						  +"AND (A.QUANTITY - A.ALLOC_QTY) >="+quantity
						  +" AND B.AVAILABLE = '"+availableYn+"'";			
					System.out.println("sql :"+sql);
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						stockQuantity = rs.getDouble(1);
						System.out.println("stockQuantity :"+stockQuantity);
						acctCode = rs.getString(2);
						System.out.println("acctCode :"+acctCode);
						cctrCode = rs.getString(3);
						System.out.println("cctrCode :"+cctrCode);
					}
					if (stockQuantity == 0 || stockQuantity < Double.parseDouble(quantity))
					{
						errCode = "VTSTOCK1";
						errString = itmDBAccess.getErrorString("",errCode,"","",conn);
						System.out.println("errString :"+errString);
						return errString;
					}
					valueXmlString.append("<Detail>\r\n");
					valueXmlString.append("<line_no__ord>").append("<![CDATA[").append(lineOrd).append("]]>").append("</line_no__ord>\r\n");
					valueXmlString.append("<loc_code>").append("<![CDATA[").append(locCode).append("]]>").append("</loc_code>\r\n");
					valueXmlString.append("<lot_no>").append("<![CDATA[").append(lotNo).append("]]>").append("</lot_no>\r\n");
					valueXmlString.append("<lot_sl>").append("<![CDATA[").append(lotSl).append("]]>").append("</lot_sl>\r\n");
					valueXmlString.append("<quantity>").append("<![CDATA[").append(quantity).append("]]>").append("</quantity>\r\n");
					valueXmlString.append("<conv_qty_stduom>").append("<![CDATA[").append(1).append("]]>").append("</conv_qty_stduom>\r\n");
					if ((acctCode == null || acctCode.trim().length() == 0) || (cctrCode == null || cctrCode.trim().length() == 0)) 
					{
						retResult = acctDetrTType(itemCode, itemSer, "IN", tranType);
						System.out.println("retResult :"+retResult);
						if (retResult.substring(retResult.length()-5).equals("DS000"))	//Error Code to be Changed
						{
							acctCodeInv = " ";
							cctrCodeInv = " ";
						}
						else
						{
							acctCodeInvArrLst = genericUtility.getTokenList(retResult, "\t");
							System.out.println("acctCodeInvArrLst.size :"+acctCodeInvArrLst.size());
							acctCodeInv = (String)acctCodeInvArrLst.get(0); 
							cctrCodeInv = (String)acctCodeInvArrLst.get(1);
						}
					}
					if (acctCode == null || acctCode.trim().length() == 0)
					{
						valueXmlString.append("<acct_code__inv>").append("<![CDATA[").append(acctCodeInv).append("]]>").append("</acct_code__inv>\r\n");
					}
					else
					{
						valueXmlString.append("<acct_code__inv>").append("<![CDATA[").append(acctCode).append("]]>").append("</acct_code__inv>\r\n");
					}
					if (cctrCode == null || cctrCode.trim().length() == 0)
					{
						valueXmlString.append("<cctr_code__inv>").append("<![CDATA[").append(cctrCodeInv).append("]]>").append("</cctr_code__inv>\r\n");
					}
					else
					{
						valueXmlString.append("<cctr_code__inv>").append("<![CDATA[").append(cctrCode).append("]]>").append("</cctr_code__inv>\r\n");
					}
					valueXmlString.append("</Detail>\r\n");
				}//else end		*/
				//stmt = null;
				if (errCode != null && errCode.trim().length() > 0)
				{
					System.out.println("errCode :"+errCode);
					errString = itmDBAccess.getErrorString("",errCode,"","",conn);
					System.out.println("errString :"+errString);
					return errString;
				}
				//valueXmlString.append("</Root>\r\n"); //Gulzar 25/04/07
			} //End Of For Loop
		}//try end
		catch (SQLException sqx)
		{
			System.out.println("The SQLException occurs in ConsumeIssueAct :(Allocate) Button :"+sqx);
			throw new ITMException(sqx);
		}
		catch (Exception e)
		{
			System.out.println("The Exception occurs in ConsumeIssueAct : (Allocate) Button :"+e);
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
		System.out.println("valueXmlString return from ConsumeIssueAct[actionAllocate] :"+valueXmlString.toString());
		return valueXmlString.toString();
	}
	//End Add Gulzar - 24/04/07
	private String acctDetrTType(String itemCode, String itemSer, String purpose, String tranType)throws Exception
	{
		System.out.println("acctDetrTType Calling................");
		System.out.println("The values of parameters are :\n itemCode :"+itemCode+" \n itemSer :"+itemSer+" \n purpose :"+purpose+" \n tranType :"+tranType);
		String sql = "", stkOption = "", acctCode = "", cctrCode = "", itemSer1 = "", retStr = "";
		Statement stmt = null;
		ResultSet rs = null;
		Connection conn = null;
		//ConnDriver connDriver = new ConnDriver();
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		//ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			stmt = conn.createStatement();
			if (purpose.equals("IN"))
			{ //if 1
				sql = "SELECT STK_OPT FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
				System.out.println("sql :"+sql);
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					stkOption = rs.getString(1);
					System.out.println("stkOption :"+stkOption);
                }
                //added by monika salla on 4 jan 21 to close prepared statements
                    rs.close();
					rs = null;
					
                 //end
				if (stkOption.equals("0"))
				{ //if II
					sql = "SELECT ACCT_CODE__PH,CCTR_CODE__PH FROM ITEM_ACCT_DETR "
				         +"WHERE ITEM_CODE = '"+itemCode+"' "
						 +"AND ITEM_SER = '"+itemSer+"' "
						 +"AND TRAN_TYPE = '"+tranType+"'";
					System.out.println("sql from if part :"+sql);
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						acctCode = rs.getString(1);
						System.out.println("acctCode :"+acctCode);
						cctrCode = rs.getString(2);
						System.out.println("cctrCode :"+cctrCode);
                    }
                    //added by monika salla on 4 jan 21 to close prepared statements
                    rs.close();
					rs = null;
					
                    //end
					//if (acctCode == null || acctCode.equals(""))
					//Chandni Shah 08/10/10 -DI90SUN041 --- change IF statement
					if (acctCode == null || acctCode.trim().length() == 0 )
					{
						sql = "SELECT ACCT_CODE__PH,CCTR_CODE__PH FROM ITEM_ACCT_DETR "
							 +"WHERE ITEM_SER = ' ' AND ITEM_CODE = '"+itemCode+"' "
							 +"AND TRAN_TYPE = '"+tranType+"'";
						System.out.println("sql :"+sql);
						rs = stmt.executeQuery(sql);
						if (rs.next())
						{
							acctCode = rs.getString(1);
							System.out.println("acctCode :"+acctCode);
							cctrCode = rs.getString(2);
							System.out.println("cctrCode :"+cctrCode);
                        }
                    //added by monika salla on 4 jan 21 to close prepared statements
                    rs.close();
					rs = null;
					
                    //end
						//Chandni Shah 08/10/10 -DI90SUN041 --- change IF statement
						if (acctCode == null || acctCode.trim().length() == 0 )
						{
							sql = "SELECT ACCT_CODE__PH,CCTR_CODE__PH FROM ITEM_ACCT_DETR "
								 +"WHERE ITEM_SER = ' ' AND ITEM_CODE = '"+itemCode+"' "
								 +"AND TRAN_TYPE = ' '";
							System.out.println("sql :"+sql);
							rs = stmt.executeQuery(sql);
							if (rs.next())
							{
								acctCode = rs.getString(1);
								System.out.println("acctCode :"+acctCode);
								cctrCode = rs.getString(2);
								System.out.println("cctrCode :"+cctrCode);
                            }
                    //added by monika salla on 4 jan 21 to close prepared statements
                    rs.close();
					rs = null;
					
                    //end
							//Chandni Shah 08/10/10 -DI90SUN041 --- change IF statement
							if (acctCode == null || acctCode.trim().length() == 0 )
							{// if III
								if (itemSer == null && itemSer.trim().length() == 0)
								{
									sql = "SELECT ITEM_SER FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
									System.out.println("sql :"+sql);
									rs = stmt.executeQuery(sql);
									if (rs.next())
									{
										itemSer1 = rs.getString(1);
										System.out.println("itemSer1 :"+itemSer1);
									}
								}
								else
								{
									itemSer1 = itemSer;
									System.out.println("itemSer1 :"+itemSer1);
                                }
                 //added by monika salla on 4 jan 21 to close prepared statements
                    rs.close();
					rs = null;
					
             //end
								sql = "SELECT ACCT_CODE__PH,CCTR_CODE__PH FROM ITEM_ACCT_DETR "
									 +"WHERE ITEM_SER = '"+itemSer1+"' "
									 +"AND ITEM_CODE = ' ' AND TRAN_TYPE = '"+tranType+"'";
								System.out.println("sql :"+sql);
								rs = stmt.executeQuery(sql);
								if (rs.next())
								{
									acctCode = rs.getString(1);
									System.out.println("acctCode :"+acctCode);
									cctrCode = rs.getString(2);
									System.out.println("cctrCode :"+cctrCode);
                                }
                                //added by monika salla on 4 jan 21 to close prepared statements
                    rs.close();
					rs = null;
					
             //end
								//Chandni Shah 08/10/10 -DI90SUN041 --- change IF statement
								if (acctCode == null || acctCode.trim().length() == 0 )
								{
									sql = "SELECT ACCT_CODE__PH,CCTR_CODE__PH FROM ITEM_ACCT_DETR "
										 +"WHERE ITEM_SER = '"+itemSer1+"' "
										 +"AND ITEM_CODE = ' ' AND TRAN_TYPE = ' '";
									System.out.println("sql :"+sql);
									rs = stmt.executeQuery(sql);
									if (rs.next())
									{
										acctCode = rs.getString(1);
										System.out.println("acctCode :"+acctCode);
										cctrCode = rs.getString(2);
										System.out.println("cctrCode :"+cctrCode);
                                    }
                                    //added by monika salla on 4 jan 21 to close prepared statements
                                      rs.close();
					                  rs = null;
					                  
                                    //end
									//Chandni Shah 08/10/10 -DI90SUN041 --- change IF statement
									if (acctCode == null || acctCode.trim().length() == 0 )
									{//if IV
										sql = "SELECT ACCT_CODE__PH,CCTR_CODE__PH FROM ITEMSER "
											 +"WHERE ITEM_SER = '"+itemSer;
										System.out.println("sql :"+sql);
										rs = stmt.executeQuery(sql);
										if (rs.next())
										{
											acctCode = rs.getString(1);
											System.out.println("acctCode :"+acctCode);
											cctrCode = rs.getString(2);
											System.out.println("cctrCode :"+cctrCode);
                                        }
                                        //added by monika salla on 4 jan 21 to close prepared statements
                                        rs.close();
					                    rs = null;
					                    
                                        //end
									}// end if IV
								}
							}
						}						
					} // end if III
				} // end if II
				else
				{
					sql = "SELECT ACCT_CODE__IN,CCTR_CODE__IN FROM ITEM_ACCT_DETR "
						 +"WHERE ITEM_CODE = '"+itemCode+"' "
						 +"AND ITEM_SER = '"+itemSer+"' "
						 +"AND TRAN_TYPE = '"+tranType+"'";
					System.out.println("sql from else part :"+sql);
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						acctCode = rs.getString(1);
						System.out.println("acctCode :"+acctCode);
						cctrCode = rs.getString(2);
						System.out.println("cctrCode :"+cctrCode);
                    }
                    //added by monika salla on 4 jan 21 to close prepared statements
                    rs.close();
					rs = null;
					
                     //end
					//Chandni Shah 08/10/10 -DI90SUN041 --- change IF statement
					if (acctCode == null || acctCode.trim().length() == 0 )
					{// if I
						sql = "SELECT ACCT_CODE__IN,CCTR_CODE__IN FROM ITEM_ACCT_DETR "
							 +"WHERE ITEM_SER = ' ' AND ITEM_CODE = '"+itemCode+"' "
							 +"AND TRAN_TYPE = '"+tranType+"'";
						System.out.println("sql from else part :"+sql);
						rs = stmt.executeQuery(sql);
						if (rs.next())
						{
							acctCode = rs.getString(1);
							System.out.println("acctCode :"+acctCode);
							cctrCode = rs.getString(2);
							System.out.println("cctrCode :"+cctrCode);
                        }
                        //added by monika salla on 4 jan 21 to close prepared statements
                    rs.close();
					rs = null;
					
                    //end
						//Chandni Shah 08/10/10 -DI90SUN041 --- change IF statement
						if (acctCode == null || acctCode.trim().length() == 0 )
						{// if II
							sql = "SELECT ACCT_CODE__IN,CCTR_CODE__IN FROM ITEM_ACCT_DETR "
						         +"WHERE ITEM_SER = ' ' AND ITEM_CODE = '"+itemCode+"' "
								 +"AND TRAN_TYPE = ' '";
							System.out.println("sql from else part :"+sql);
							rs = stmt.executeQuery(sql);
							if (rs.next())
							{
								acctCode = rs.getString(1);
								System.out.println("acctCode :"+acctCode);
								cctrCode = rs.getString(2);
								System.out.println("cctrCode :"+cctrCode);
                            }
                            //added by monika salla on 4 jan 21 to close prepared statements
                    rs.close();
					rs = null;
					
                         //end
							//Chandni Shah 08/10/10 -DI90SUN041 --- change IF statement
							if (acctCode == null || acctCode.trim().length() == 0 )
							{// if III
								if (itemSer == null && itemSer.trim().length() == 0)
								{
									sql = "SELECT ITEM_SER FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
									System.out.println("sql :"+sql);
									rs = stmt.executeQuery(sql);
									if (rs.next())
									{
										itemSer1 = rs.getString(1);
										System.out.println("itemSer1 :"+itemSer1);
                                    }
                                    
								}
								else
								{
									itemSer1 = itemSer;
									System.out.println("itemSer1 :"+itemSer1);
                                }
                                //added by monika salla on 4 jan 21 to close prepared statements
                    rs.close();
					rs = null;
					
             //end
								sql = "SELECT ACCT_CODE__IN,CCTR_CODE__IN FROM ITEM_ACCT_DETR "
									 +"WHERE ITEM_SER = '"+itemSer1+"' "
									 +"AND ITEM_CODE = ' ' AND TRAN_TYPE = '"+tranType+"'";
								System.out.println("sql from else part :"+sql);
								rs = stmt.executeQuery(sql);
								if (rs.next())
								{
									acctCode = rs.getString(1);
									System.out.println("acctCode :"+acctCode);
									cctrCode = rs.getString(2);
									System.out.println("cctrCode :"+cctrCode);
                                }
                                
                                //added by monika salla on 4 jan 21 to close prepared statements
                    rs.close();
					rs = null;
					
             //end
								//Chandni Shah 08/10/10 -DI90SUN041 --- change IF statement
								if (acctCode == null || acctCode.trim().length() == 0 )
								{// if IV
									sql = "SELECT ACCT_CODE__IN,CCTR_CODE__IN FROM ITEM_ACCT_DETR "
										 +"WHERE ITEM_SER = '"+itemSer1+"' "
										 +"AND ITEM_CODE = ' ' AND TRAN_TYPE = ' '";
									System.out.println("sql :"+sql);
									rs = stmt.executeQuery(sql);
									if (rs.next())
									{
										acctCode = rs.getString(1);
										System.out.println("acctCode :"+acctCode);
										cctrCode = rs.getString(2);
										System.out.println("cctrCode :"+cctrCode);
                                    }
                                    //added by monika salla on 4 jan 21 to close prepared statements
                    rs.close();
					rs = null;
					
             //end
									//Chandni Shah 08/10/10 -DI90SUN041 --- change IF statement
									if (acctCode == null || acctCode.trim().length() == 0 )
									{// IF V
										sql = "SELECT ACCT_CODE__IN,CCTR_CODE__IN FROM ITEMSER "
											 +"WHERE ITEM_SER = '"+itemSer+"'";
										System.out.println("sql :"+sql);
										rs = stmt.executeQuery(sql);
										if (rs.next())
										{
											acctCode = rs.getString(1);
											System.out.println("acctCode :"+acctCode);
											cctrCode = rs.getString(2);
											System.out.println("cctrCode :"+cctrCode);
                                        }
                                        //added by monika salla on 4 jan 21 to close prepared statements
                    rs.close();
					rs = null;
					
             //end
									}// end if V
								}// end if IV
							}//end if III
						}// end if II
					}// end if I
				}//end else
			}// end if I
		}//try end
		catch (SQLException sqx)
		{
			System.out.println("The exception occurs in acctDetrTType() :"+sqx);
			throw new ITMException(sqx);
		}
		catch (Exception e)
		{
			System.out.println("The exception occurs in acctDetrTType() :"+e);
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
		if (acctCode == null)
		{
			acctCode = "";
		}
		if (cctrCode == null)
		{
			cctrCode = "";
		}
		retStr = acctCode + "\t" + cctrCode;
		System.out.println("retStr :"+retStr);
		return retStr;
	}//end acctDertTType()
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
	}//end of calcExpiry

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
			/*
			if( ! retString.startsWith("<?xml"))
			{
				retString = "<?xml version=1.0?>"+retString;
			}
			*/
			out.flush();
			out.close();
			out = null;
		}
		catch (Exception e)
		{
			System.out.println("Exception : MasterStateful : serializeDom :"+e);
			throw new ITMException(e);
		}
		return retString;
	}

	/* -- Commented and Changes Below - Gulzar 25/04/07
	private void getItemChanged(Document dom,Document dom1,String consumeOrder,String lineNoStr,Connection conn) throws Exception
	{
		Statement stmt = null;
		ResultSet rs = null;
		String tranType = "",sql = "";
		String itemCode = "",unit = "",qtyStr = "",rate = "",taxChap = "",taxClass = "",taxEnv = "";
		String acctCode = "",cctrCode = "",locCode = "";
		String itemDescr = "",qcReqd = "",acctCodeInv = "",cctrCodeInv = "";
		String consIssue = "", tranIDIssue = ""; //Gulzar 21-02-07
		double qtyIssue = 0, quantity = 0,qtyStd = 0;

		ibase.utility.E12GenericUtility genericUtility= new ibase.utility.E12GenericUtility();
		try{
			tranType	= genericUtility.getColumnValue("tran_type",dom1);
			consIssue	= genericUtility.getColumnValue("cons_issue",dom1); //Gulzar 21-02-07
			tranIDIssue = genericUtility.getColumnValue("tran_id__iss",dom1); //Gulzar 21-02-07
			if (consIssue == null)
			{
				consIssue = "@@";
			}
			sql = "select item_code,unit,quantity,rate,tax_chap,tax_class,tax_env,acct_code,cctr_code,loc_code from consume_ord_det "+
					"where cons_order = '"+consumeOrder+"' and line_no = "+lineNoStr+"";
			System.out.println("SQL ::"+sql); //Gulzar
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			if(rs.next()){
			   itemCode = rs.getString("item_code");
			   unit = rs.getString("unit");
			   qtyStr = rs.getString("quantity");
			   rate = rs.getString("rate");
   			   taxChap = rs.getString("tax_chap");
   			   taxClass = rs.getString("tax_class");
   			   taxEnv = rs.getString("tax_env");
   			   acctCode = rs.getString("acct_code");
   			   cctrCode = rs.getString("cctr_code");
   			   locCode = rs.getString("loc_code");
			}
			System.out.println("itemCode ::"+itemCode); 
			System.out.println("unit     ::"+unit); 
			System.out.println("qtyStr	 ::"+qtyStr); 
			System.out.println("rate	 ::"+rate); 
			System.out.println("taxChap  ::"+taxChap); 
			System.out.println("taxClass ::"+taxClass); 
			System.out.println("taxEnv   ::"+taxEnv); 
			System.out.println("acctCode ::"+acctCode); 
			System.out.println("cctrCode ::"+cctrCode); 
			System.out.println("locCode  ::"+locCode); 

			sql = "select sum(case when b.tran_type = 'I' then a.quantity else (-1 * a.quantity) end) from consume_iss_det a, consume_iss b "+
				 " where a.cons_issue = b.cons_issue " +
				 " and b.cons_issue <> '"+consIssue+"'" + //Gulzar 21-02-07 
				 " and a.cons_order = '"+consumeOrder+"'" + 
				 " and a.line_no__ord = "+lineNoStr+"";
			System.out.println("SQL ::"+sql);
			rs = stmt.executeQuery(sql);
			if(rs.next()){
				qtyIssue = rs.getDouble(1);
			}
			quantity = Double.parseDouble(qtyStr) - qtyIssue;

			sql = "select descr,qc_reqd from item where item_code = '"+itemCode+"'";
			System.out.println("SQL ::"+sql);	
			rs = stmt.executeQuery(sql);
			if(rs.next()){
			   itemDescr = rs.getString("descr");
			   qcReqd  = rs.getString("qc_reqd");
			}
			setValueInDom(dom,"line_no__ord",lineNoStr);//Gulzar 20/02/07
			setValueInDom(dom,"item_code",itemCode);
			setValueInDom(dom,"item_descr",itemDescr);
			setValueInDom(dom,"unit",unit);
			setValueInDom(dom,"unit__std",unit);
			setValueInDom(dom,"conv_qty_stduom","1");
			setValueInDom(dom,"quantity",String.valueOf(quantity));
			setValueInDom(dom,"quantity__std",String.valueOf(quantity));
			setValueInDom(dom,"rate",rate);
			setValueInDom(dom,"tax_chap",taxChap);
			setValueInDom(dom,"tax_class",taxClass);
			setValueInDom(dom,"tax_env",taxEnv);
			setValueInDom(dom,"acct_code",acctCode);
			setValueInDom(dom,"cctr_code",cctrCode);
			if(locCode != null && locCode.trim().length() > 0){
				setValueInDom(dom,"loc_code",locCode);
			}
			if(tranType != null && tranType == "R"){
				setValueInDom(dom,"qc_reqd",qcReqd);
				/*-- Commented  and Changes Below By Gulzar 21-02-07
				sql = "select (case when sum(a.quantity) is null then 0 else sum(a.quantity) end), (case when sum(a.quantity__std) is null then 0 else sum(a.quantity__std) end) "+
						"from consume_iss_det a,consume_iss b where a.cons_issue = b.cons_issue and a.cons_order = '"+consumeOrder+"' "+
						"and (case when b.tran_type is null then 'I' else b.tran_type end) = 'I' and "+
						"(case when b.confirmed is null then 'N' else b.confirmed end) = 'Y' and "+
						"a.line_no__ord = '"+lineNoStr+"'";
				System.out.println("SQL ::"+sql);
				rs = stmt.executeQuery(sql);
				if(rs.next()){
					quantity = rs.getDouble(1);
					qtyStd = rs.getDouble(2);
				}
				setValueInDom(dom,"quantity",String.valueOf(quantity));
				setValueInDom(dom,"quantity__std",String.valueOf(qtyStd));
				//End Comment Gulzar 21-02-07
				if (tranIDIssue != null && tranIDIssue.trim().length() > 0)
				{
					sql = "SELECT SUM(CASE WHEN B.TRAN_TYPE = 'I' THEN A.QUANTITY ELSE (-1 * A.QUANTITY) END)" +
						 " FROM CONSUME_ISS_DET A, CONSUME_ISS B " +
						 " WHERE A.CONS_ISSUE = B.CONS_ISSUE " +
						 " AND B.CONS_ISSUE <> '"+consIssue+"'" +
						 " AND (B.CONS_ISSUE = '"+tranIDIssue+"' OR B.TRAN_ID__ISS = '"+tranIDIssue+"')" +
						 " AND A.CONS_ORDER =  '"+consumeOrder+"'" +
						 " AND A.LINE_NO__ORD = "+lineNoStr+"" ; 
					System.out.println("SQL ::"+sql);
					rs = stmt.executeQuery(sql);
					if(rs.next())
					{
						qtyIssue = rs.getDouble(1);
					}
					setValueInDom(dom,"quantity",String.valueOf(qtyIssue));
					setValueInDom(dom,"quantity__std",String.valueOf(qtyIssue));
				}
				//End Changes Gulzar 21-02-07
				sql = "select a.acct_code,a.cctr_code,a.acct_code__inv,a.cctr_code__inv	, a.rate "+
						"from consume_iss_det a,consume_iss b where  a.cons_issue = b.cons_issue "+
						"and b.tran_type = 'I' and a.cons_order = '"+consumeOrder+"' and "+
						"a.line_no__ord = "+lineNoStr+"";
				System.out.println("SQL ::"+sql);
				rs =stmt.executeQuery(sql);
				if(rs.next()){
					acctCode = rs.getString(1);
					cctrCode = rs.getString(2);
					acctCodeInv = rs.getString(3);
					cctrCodeInv = rs.getString(4);
					rate = rs.getString(5);
					/* --Commented and Move to Outside of if block //gulzar 21-02-07
					setValueInDom(dom,"acct_code",acctCode);
					setValueInDom(dom,"cctr_code",cctrCode);
					setValueInDom(dom,"acct_code__inv",acctCodeInv);
					setValueInDom(dom,"cctr_code__inv",cctrCodeInv);
					setValueInDom(dom,"rate",rate);
				}
				setValueInDom(dom,"acct_code",acctCode);
				setValueInDom(dom,"cctr_code",cctrCode);
				setValueInDom(dom,"acct_code__inv",acctCodeInv);
				setValueInDom(dom,"cctr_code__inv",cctrCodeInv);
				setValueInDom(dom,"rate",rate);
				//End Changes Gulzar 21-02-07
			}
		}catch(Exception e){
			System.out.println("Exception [ConsumeIssueAct][getItemChanged] :"+e);
			e.printStackTrace();
			throw e;
		}
	}
	*///End Comment Gulzar 25-04-07
//Added By Gulzar 25/04/07
	//private String getItemChanged(Document dom1,String consumeOrder,String lineNoStr,Connection conn) throws Exception
	private String getItemChanged(Document dom1,String consumeOrder,Connection conn) throws Exception//Changed by Manoj dtd 18/01/2014 lineNo need not to pass as argument
	{
		Statement stmt = null;
		ResultSet rs = null;
		String tranType = "",sql = "";
		String itemCode = "",unit = "",qtyStr = "",rate = "",taxChap = "",taxClass = "",taxEnv = "";
		String acctCode = "",cctrCode = "",locCode = "";
		String itemDescr = "",qcReqd = "",acctCodeInv = "",cctrCodeInv = "";
		String consIssue = "", tranIDIssue = ""; //Gulzar 21-02-07
		double qtyIssue = 0, quantity = 0,qtyStd = 0;
		
		StringBuffer valueXmlString = new StringBuffer("");
		ibase.utility.E12GenericUtility genericUtility=	new  ibase.utility.E12GenericUtility();//updated by nisar on 30/11/07 original :  genericUtility = new ibase.utility.E12GenericUtility();
		//Statement stmt1 = null;//Variables declared by manoj dtd 18/01/2014
		PreparedStatement pstmt = null;//added  & commented by kailasg on 19-april-21 as per suggestion of SM sir.
		PreparedStatement pstmt1 = null;//added by kailasg on 19-april-21 as per suggestion of SM sir.
		ResultSet rs1 = null;
		String lineNoStr="";
		try
		{
			tranType	= genericUtility.getColumnValue("tran_type",dom1);
			consIssue	= genericUtility.getColumnValue("cons_issue",dom1); //Gulzar 21-02-07
			tranIDIssue = genericUtility.getColumnValue("tran_id__iss",dom1); //Gulzar 21-02-07
			if (consIssue == null)
			{
				consIssue = "@@";
			}
			/*sql = "select item_code,unit,quantity,rate,tax_chap,tax_class,tax_env,acct_code,cctr_code,loc_code from consume_ord_det "+
					"where cons_order = '"+consumeOrder+"' and line_no = "+lineNoStr+"";*/
			
			sql = "select line_no,item_code,unit,quantity,rate,tax_chap,tax_class,tax_env,acct_code,cctr_code,loc_code from consume_ord_det "+
					"where cons_order = '"+consumeOrder+"' ";
			System.out.println("SQL ::"+sql); //Gulzar
			//stmt = conn.createStatement();
			//rs = stmt.executeQuery(sql);
			pstmt = conn.prepareStatement(sql); //commented and added by kailasg on 19-april-21 as per suggestion SM sir
			rs = pstmt.executeQuery();
			while(rs.next())
			{
			   itemCode = rs.getString("item_code");
			   unit = rs.getString("unit");
			   qtyStr = rs.getString("quantity");
			   rate = rs.getString("rate");
   			   taxChap = rs.getString("tax_chap");
   			   taxClass = rs.getString("tax_class");
   			   taxEnv = rs.getString("tax_env");
   			   acctCode = rs.getString("acct_code");
   			   cctrCode = rs.getString("cctr_code");
   			   locCode = rs.getString("loc_code");
   			   lineNoStr = rs.getString("line_no");
			
				System.out.println("itemCode ::"+itemCode); 
				System.out.println("unit     ::"+unit); 
				System.out.println("qtyStr	 ::"+qtyStr); 
				System.out.println("rate	 ::"+rate); 
				System.out.println("taxChap  ::"+taxChap); 
				System.out.println("taxClass ::"+taxClass); 
				System.out.println("taxEnv   ::"+taxEnv); 
				System.out.println("acctCode ::"+acctCode); 
				System.out.println("cctrCode ::"+cctrCode); 
				System.out.println("locCode  ::"+locCode); 
	
				sql = "select sum(case when b.tran_type = 'I' then a.quantity else (-1 * a.quantity) end) from consume_iss_det a, consume_iss b "+
					 " where a.cons_issue = b.cons_issue " +
					 " and b.cons_issue <> '"+consIssue+"'" + //Gulzar 21-02-07 
					 " and a.cons_order = '"+consumeOrder+"'" + 
					 " and a.line_no__ord = "+lineNoStr+"";
				System.out.println("SQL ::"+sql);
				//stmt1 = conn.createStatement();
				//rs1 = stmt1.executeQuery(sql);
				pstmt1 = conn.prepareStatement(sql); //commented and added by kailasg on 19-april-21 as per suggstion SM sir
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{
					qtyIssue = rs1.getDouble(1);
				}
				//Statement and Resultset closed by manoj dtd 18/01/2014
				rs1.close();
				rs1=null;
				//stmt1.close();
				//stmt1=null;
				pstmt1.close();
				pstmt1=null;
				
				quantity = Double.parseDouble(qtyStr) - qtyIssue;
	
				sql = "select descr,qc_reqd from item where item_code = '"+itemCode+"'";
				System.out.println("SQL ::"+sql);	
			//	stmt1 = conn.createStatement();
				//rs1 = stmt1.executeQuery(sql);
				pstmt1 = conn.prepareStatement(sql); //commented and added by kailasg on 19-april-21 as per suggstion SM sir
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{
				   itemDescr = rs1.getString("descr");
				   qcReqd  = rs1.getString("qc_reqd");
				}
				//Statement and Resultset closed by manoj dtd 18/01/2014
				rs1.close();
				rs1=null;
				//stmt1.close();
				//stmt1=null;
				pstmt1.close();
				pstmt1=null;
				if(locCode != null && locCode.trim().length() > 0)
				{
					//setValueInDom(dom,"loc_code",locCode); //Gulzar - 25/04/07
				}
				if(tranType != null && tranType == "R")
				{
					if (tranIDIssue != null && tranIDIssue.trim().length() > 0)
					{
						sql = "SELECT SUM(CASE WHEN B.TRAN_TYPE = 'I' THEN A.QUANTITY ELSE (-1 * A.QUANTITY) END)" +
							 " FROM CONSUME_ISS_DET A, CONSUME_ISS B " +
							 " WHERE A.CONS_ISSUE = B.CONS_ISSUE " +
							 " AND B.CONS_ISSUE <> '"+consIssue+"'" +
							 " AND (B.CONS_ISSUE = '"+tranIDIssue+"' OR B.TRAN_ID__ISS = '"+tranIDIssue+"')" +
							 " AND A.CONS_ORDER =  '"+consumeOrder+"'" +
							 " AND A.LINE_NO__ORD = "+lineNoStr+"" ; 
						System.out.println("SQL ::"+sql);
						//stmt1 = conn.createStatement();
						//rs1 = stmt1.executeQuery(sql);
						pstmt1 = conn.prepareStatement(sql); //commented and added by kailasg on 19-april-21 as per suggstion SM sir
						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{
							qtyIssue = rs1.getDouble(1);
						}
						//Statement and Resultset closed by manoj dtd 18/01/2014
						rs1.close();
						rs1=null;
					//	stmt1.close();
					//	stmt1=null;
						pstmt1.close();
						pstmt1=null;
						quantity = qtyIssue;
					}
					sql = "select a.acct_code,a.cctr_code,a.acct_code__inv,a.cctr_code__inv	, a.rate "+
							"from consume_iss_det a,consume_iss b where  a.cons_issue = b.cons_issue "+
							"and b.tran_type = 'I' and a.cons_order = '"+consumeOrder+"' and "+
							"a.line_no__ord = "+lineNoStr+"";
					System.out.println("SQL ::"+sql);
					//stmt1 = conn.createStatement();
					//rs1 =stmt1.executeQuery(sql);
					pstmt1 = conn.prepareStatement(sql); //commented and added by kailasg on 19-april-21 as per suggstion SM sir
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						acctCode = rs1.getString(1);
						cctrCode = rs1.getString(2);
						acctCodeInv = rs1.getString(3);
						cctrCodeInv = rs1.getString(4);
						rate = rs1.getString(5);
					}
					//Statement and Resultset closed by manoj dtd 18/01/2014
					rs1.close();
					rs1=null;
					//stmt1.close();
					//stmt1=null;
					pstmt1.close();
					pstmt1=null;
				}
				System.out.println("isSrvCallOnChg is set ---");
				//Manoj dtd 21/01/2013 to add  isSrvCallOnChg
				valueXmlString.append("<Detail2>");
				valueXmlString.append("<cons_order isSrvCallOnChg=\"0\">").append("<![CDATA[").append((consumeOrder == null) ? "":consumeOrder).append("]]>").append("</cons_order>\r\n");
				valueXmlString.append("<line_no__ord isSrvCallOnChg=\"0\">").append("<![CDATA[").append(lineNoStr).append("]]>").append("</line_no__ord>\r\n");
				valueXmlString.append("<item_code isSrvCallOnChg=\"0\">").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code>\r\n");
				valueXmlString.append("<item_descr isSrvCallOnChg=\"0\">").append("<![CDATA[").append(itemDescr).append("]]>").append("</item_descr>\r\n");
				valueXmlString.append("<unit isSrvCallOnChg=\"0\">").append("<![CDATA[").append(unit).append("]]>").append("</unit>\r\n");
				valueXmlString.append("<unit__std isSrvCallOnChg=\"0\">").append("<![CDATA[").append(unit).append("]]>").append("</unit__std>\r\n");
				valueXmlString.append("<conv_qty_stduom isSrvCallOnChg=\"0\">").append("<![CDATA[").append("1").append("]]>").append("</conv_qty_stduom>\r\n");
				valueXmlString.append("<quantity isSrvCallOnChg=\"0\">").append("<![CDATA[").append(quantity).append("]]>").append("</quantity>\r\n");
				valueXmlString.append("<quantity__std isSrvCallOnChg=\"0\">").append("<![CDATA[").append(quantity).append("]]>").append("</quantity__std>\r\n");
				valueXmlString.append("<rate isSrvCallOnChg=\"0\">").append("<![CDATA[").append(rate).append("]]>").append("</rate>\r\n");
				valueXmlString.append("<tax_chap isSrvCallOnChg=\"0\">").append("<![CDATA[").append((taxChap == null) ? "":taxChap).append("]]>").append("</tax_chap>\r\n");
				valueXmlString.append("<tax_class isSrvCallOnChg=\"0\">").append("<![CDATA[").append((taxClass == null) ? "":taxClass).append("]]>").append("</tax_class>\r\n");
				valueXmlString.append("<tax_env isSrvCallOnChg=\"0\">").append("<![CDATA[").append((taxEnv == null) ? "":taxEnv).append("]]>").append("</tax_env>\r\n");
				valueXmlString.append("<acct_code isSrvCallOnChg=\"0\">").append("<![CDATA[").append((acctCode == null) ? "":acctCode).append("]]>").append("</acct_code>\r\n");
				valueXmlString.append("<cctr_code isSrvCallOnChg=\"0\">").append("<![CDATA[").append((cctrCode == null) ? "":cctrCode).append("]]>").append("</cctr_code>\r\n");
				valueXmlString.append("<loc_code isSrvCallOnChg=\"0\">").append("<![CDATA[").append((locCode == null) ? "":locCode).append("]]>").append("</loc_code>\r\n");
				valueXmlString.append("<qc_reqd isSrvCallOnChg=\"0\">").append("<![CDATA[").append(qcReqd).append("]]>").append("</qc_reqd>\r\n");
				valueXmlString.append("<acct_code__inv isSrvCallOnChg=\"0\">").append("<![CDATA[").append((acctCodeInv == null) ? "":acctCodeInv).append("]]>").append("</acct_code__inv>\r\n");
				valueXmlString.append("<cctr_code__inv isSrvCallOnChg=\"0\">").append("<![CDATA[").append((cctrCodeInv == null) ? "":cctrCodeInv).append("]]>").append("</cctr_code__inv>\r\n");
				valueXmlString.append("<amount>").append("<![CDATA[").append(quantity*Double.parseDouble(((rate == null) ? "0":rate))).append("]]>").append("</amount>\r\n");
				valueXmlString.append("</Detail2>");
			}
			rs.close();
			rs=null;
		//	stmt.close();
		//	stmt=null;
			pstmt.close();
			pstmt=null;
			
		}
		catch(Exception e)
		{
			System.out.println("Exception [ConsumeIssueAct][getItemChanged] :"+e);
			e.printStackTrace();
			throw e;
		}
		System.out.println("valueXmlString return from ConsumeIssueAct[getItemChanged] :"+valueXmlString.toString());
		return valueXmlString.toString();
	}

//End Add Gulzar 25/04/07
	private void setValueInDom(Document dom,String field,String value) throws Exception
	{
		NodeList nodeList = null;
		Node childNode = null;
		/*--commented and Changes Below By Gulzar 21/02/7
		try{
			System.out.println("Dom : "+serializeDom(dom));
			nodeList = dom.getElementsByTagName(field);			
			childNode = nodeList.item(0);
			childNode.setNodeValue(value);
			System.out.println("Dom : "+serializeDom(dom));
		}catch(Exception e){
			System.out.println("Exception "+e);
			e.printStackTrace();
			throw e;
		}
		*///End Comment Gulzar 21/02/07
		try
		{
			System.out.println("==================Dom b4 setting Value================");
			System.out.println("Dom b4 setting Value : "+serializeDom(dom));
			System.out.println("setting Value For Field : ["+field+"] with value ["+value+"]");
			nodeList = dom.getElementsByTagName(field);			
			if (nodeList != null)
			{
				childNode = nodeList.item(0);
				if (childNode != null && value != null)
				{
					if (childNode.getFirstChild() != null)
					{
						childNode.getFirstChild().setNodeValue(value);
						System.out.println("After setting Value : childNode.getFirstChild() != null : "+ childNode.getFirstChild().getNodeValue());
					}
					else
					{
						childNode.appendChild(dom.createCDATASection(value));
						System.out.println("After setting Value : childNode.getFirstChild() == null : "+ childNode.getFirstChild().getNodeValue());
					}
				}
			}
			System.out.println("==================Dom After setting Value================");
			System.out.println("Dom After setting Value : "+serializeDom(dom));
		}
		catch(Exception e)
		{
			System.out.println("Exception "+e);
			e.printStackTrace();
			throw e;
		}
		//End Changes Gulzar 21/02/07
	}
}

