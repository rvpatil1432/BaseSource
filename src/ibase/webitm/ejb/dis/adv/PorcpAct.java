package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.sql.*;
import org.w3c.dom.*;

import javax.ejb.*;
import ibase.webitm.ejb.ITMDBAccessEJB;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.utility.E12GenericUtility;
import ibase.system.config.*;

import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class PorcpAct extends ActionHandlerEJB implements PorcpActLocal, PorcpActRemote
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
			if(xmlString != null && xmlString.trim().length() > 0)
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
			if (actionType.equalsIgnoreCase("PendingQty"))
			{
				retString = actionPendingQty(dom,dom1,objContext,xtraParams);
			}
		}
	   	catch(Exception e)
		{
			System.out.println("Exception :PorcpAct :actionHandler(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning String from PorcpAct : actionHandler"+retString);
	    return retString;
	}

	public String actionHandler(String actionType, String xmlString, String objContext, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		String  retString = null;
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		try
		{
			if(xmlString != null && xmlString.trim().length() > 0)
			{
				System.out.println("XML String :"+xmlString);
				dom = genericUtility.parseString(xmlString); 
			}
			System.out.println("actionType:"+actionType+":");			
			if (actionType.equalsIgnoreCase("PickSched"))
			{
				retString = actionPickSched(dom,objContext,xtraParams);
			}
		}
	   	catch(Exception e)
		{
			System.out.println("Exception :PorcpAct :actionHandler(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning String from PorcpAct : actionHandler"+retString);
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
						
			if (actionType.equalsIgnoreCase("PendingQty"))
			{
				retString = pendingQtyTransform(dom, dom1, objContext, xtraParams, selDataDom);
			}
		}
	   	catch(Exception e)
		{
			System.out.println("Exception :Porcp :actionHandlerTransform(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning String from Porcp : actionHandlerTransform"+retString);
	    return retString;
	}
	
	private String actionPickSched(Document dom, String objContext, String xtraParams) throws RemoteException , ITMException
	{
		Connection conn = null;
		//Statement stmt = null;//Modified by Rohini T on 12/04/2021
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		String errCode = "";
		String errString = "";
		String itemCode = "";
		String qty = "";
		String pono = "";
		String mfgDate = "";
		String expDate = "";
		String trackShelfLife = "";
		String locCode = "";
		String noArt = "";
		String lineNo = "";
		double inputArt = 0; 
		double quantity = 0; 
		double remQty = 0; 
		double inputQty = 0; 
		
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
	
		if (dom == null)
		{
			valueXmlString.append("</Root>\r\n");
			return valueXmlString.toString();
		}

		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ITMDBAccessEJB	itmDBAccess = new ITMDBAccessEJB();
		ConnDriver connDriver = new ConnDriver();
		try
		{				
			itemCode = genericUtility.getColumnValue("item_code",dom);
			qty = genericUtility.getColumnValue("quantity",dom);
			pono = genericUtility.getColumnValue("purc_order",dom);
			mfgDate = genericUtility.getColumnValue("mfg_date",dom);
			expDate = genericUtility.getColumnValue("expiry_date",dom);
			locCode = genericUtility.getColumnValue("loc_code",dom);
			noArt = genericUtility.getColumnValue("no_art",dom);
			
			System.out.println(" Values From DOM :itemCode:"+itemCode+":qty:"+qty+":pono:"+pono+":");
			System.out.println(" Values From DOM :mfgDate:"+mfgDate+":expDate:"+expDate+":");
			System.out.println(" Values From DOM :locCode:"+locCode+":noArt:"+noArt+":");

			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			//Modified by Rohini T on 12/04/2021
			//stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
		   	sql = "SELECT CASE WHEN TRACK_SHELF_LIFE IS NULL THEN 'N' ELSE TRACK_SHELF_LIFE END FROM ITEM WHERE ITEM_CODE ='"+itemCode+"'";
			System.out.println("Porcp:actionPickSched:sql:"+sql);
			//rs = stmt.executeQuery(sql);//Modified by Rohini T on 12/04/2021
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery(sql);
			if(rs.next())
			{
				/*ls_track_shelf_life*/
			   trackShelfLife = rs.getString(1);
			}
			System.out.println("trackShelfLife:"+trackShelfLife+":");
			
			if(qty == null || Double.parseDouble(qty) <= 0)
			{
				System.out.println("inside :(qty == null || Double.parseDouble(qty) <= 0) CONDITION");
				errCode ="VTQTY18";
				errString = itmDBAccess.getErrorString("",errCode,"","",conn);				
				conn.close();
				conn = null;
				return errString;
			}
			if(noArt == null || Double.parseDouble(noArt) <= 0)
			{
				System.out.println("inside :(noArt == null || Double.parseDouble(noArt) <= 0) CONDITION");
				errCode = "VTNARTNULL";			
				errString = itmDBAccess.getErrorString("",errCode,"","",conn);				
				conn.close();
				conn = null;
				return errString;
			}
			if(errCode == null || errCode.trim().length()== 0)
			{
				remQty = Double.parseDouble(qty);
				sql="SELECT LINE_NO,QUANTITY - CASE WHEN DLV_QTY IS NULL THEN 0 ELSE DLV_QTY END,LOC_CODE "+ //loc code added jiten 03/10/06
					" FROM PORDDET WHERE PURC_ORDER ='"+pono+"' " +
					" AND ITEM_CODE ='"+itemCode+"' " + 
					" AND QUANTITY - CASE WHEN DLV_QTY IS NULL THEN 0 ELSE DLV_QTY END > 0	"+
					" ORDER BY LINE_NO,ITEM_CODE ";
				System.out.println("RecordFetching Query:PORDDET:sql:"+sql);
				//rs = stmt.executeQuery(sql);//Modified by Rohini T on 12/04/2021
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery(sql);
				while(rs.next())
				{
					lineNo = rs.getString(1);
					quantity = rs.getDouble(2);
					locCode = rs.getString(3);//Added on 03/10/06 - Jiten
					if(locCode == null)
					{
						locCode = "";
					}
					System.out.println("lineNo:"+lineNo+":quantity:"+quantity+":");
					System.out.println("remQty:"+remQty+":");
					if(remQty == 0)
					{
						System.out.println("INSIDE:(remQty == 0)Condition");
						break;
					}
					else if(quantity >= remQty)
					{
						System.out.println("INSIDE:(quantity > = remQty)Condition");
						inputQty = remQty ;
						remQty = 0 ;
					}
					else if(quantity < remQty)
					{
						System.out.println("INSIDE:(quantity < remQty)Condition");
						inputQty = quantity;
						remQty 	 = remQty - quantity ;
					}
					inputArt = Math.round((Double.parseDouble(noArt)/Double.parseDouble(qty))*(inputQty));
					valueXmlString.append("<Detail>\r\n");
					valueXmlString.append("<item_code>").append("<![CDATA[").append(itemCode.trim()).append("]]>").append("</item_code>\r\n");
					valueXmlString.append("<line_no__ord>").append("<![CDATA[").append(lineNo.trim()).append("]]>").append("</line_no__ord>\r\n");
					valueXmlString.append("<purc_order>").append("<![CDATA[").append(pono.trim()).append("]]>").append("</purc_order>\r\n");
					valueXmlString.append("<quantity>").append("<![CDATA[").append(inputQty).append("]]>").append("</quantity>\r\n");
					if(!trackShelfLife.equalsIgnoreCase("N"))
					{
						valueXmlString.append("<mfg_date>").append("<![CDATA[").append(mfgDate).append("]]>").append("</mfg_date>\r\n");
						valueXmlString.append("<expiry_date>").append("<![CDATA[").append(expDate).append("]]>").append("</expiry_date>\r\n");
					}
					valueXmlString.append("<loc_code>").append("<![CDATA[").append(locCode.trim()).append("]]>").append("</loc_code>\r\n");
					valueXmlString.append("<no_art>").append("<![CDATA[").append(inputArt).append("]]>").append("</no_art>\r\n");
					valueXmlString.append("</Detail>\r\n");
				} //While End
				//Modified by Rohini T on 12/04/2021[Start]
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
				//Modified by Rohini T on 12/04/2021[End]
			}//errCode
			if (errCode != null  && errCode.trim().length() > 0)
			{
				System.out.println("errCode:"+errCode+":");
				errString = itmDBAccess.getErrorString("",errCode,"","",conn);				
				conn.close();
				conn = null;
				return errString;
			}			
			valueXmlString.append("</Root>\r\n");
		}
		catch(SQLException e)
		{
			System.out.println("Exception : Porcp : actionPickSched " +e.getMessage());
			throw new ITMException(e);
		}
		catch(Exception e)
		{
			System.out.println("Exception : Porcp : actionHandler :(Document dom)" +e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try{
				conn.close();
				conn = null;
			}catch(Exception e){}
		}
		System.out.println("valueXmlString.toString() "+valueXmlString.toString());
		return valueXmlString.toString();		
	}
	
	private String actionPendingQty(Document dom, Document dom1, String objContext, String xtraParams) throws RemoteException, ITMException
	{
		String poHeader = "";
		String poDetail = "";
		String purchaseOrder = "";
		String sql = "";
		ResultSet rs = null;
		Connection conn = null;
		//Statement stmt = null;//Modified by Rohini T on 12/04/2021
		PreparedStatement pstmt = null;
		java.sql.Date reqDate = null,dlvDate = null,statusDate = null;
		String reqDateStr = "",dlvDateStr = "",statusDateStr = "";
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		try
		{
		   	//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			//stmt = conn.createStatement();//Modified by Rohini T on 12/04/2021
			poHeader = genericUtility.getColumnValue("purc_order",dom1);	
			SimpleDateFormat sdf = new SimpleDateFormat(new  ibase.utility.E12GenericUtility().getApplDateFormat());
			System.out.println("PoHeader :"+poHeader);
			if (dom != null)
			{
				poDetail = genericUtility.getColumnValue("purc_order",dom);
			}
			System.out.println("poDetail :"+poDetail);
			if (poDetail == null || poDetail.trim().length() == 0)
			{
				purchaseOrder = poHeader;
			}
			else
			{
				purchaseOrder = poDetail;
			}
			sql = "SELECT PORDDET.ITEM_CODE, "   
						+"PORDDET.QUANTITY, "   
						+"ITEM.DESCR, "   
						+"PORDDET.PURC_ORDER, "   
						+"PORDDET.LINE_NO, "   
						+"PORDDET.UNIT, "   
						+"PORDDET.RATE, "   
						+"PORDDET.DISCOUNT, "   
						+"PORDDET.TAX_AMT, "   
						+"PORDDET.TAX_CLASS, "   
						+"PORDDET.TAX_CHAP, "   
						+"PORDDET.TAX_ENV, "   
						+"PORDDET.UNIT__RATE, "   
						+"PORDDET.QUANTITY__STDUOM, "   
						+"PORDDET.RATE__STDUOM, "   
						+"PORDDET.SITE_CODE, "   
						+"PORDDET.IND_NO, "   
						+"PORDDET.TOT_AMT, "   
						+"PORDDET.LOC_CODE, "   
						+"PORDDET.REQ_DATE, "   
						+"PORDDET.DLV_DATE, "   
						+"PORDDET.DLV_QTY, "   
						+"PORDDET.STATUS, "   
						+"PORDDET.STATUS_DATE, "   
						+"PORDDET.REMARKS, "   
						+"PORDDET.WORK_ORDER, "   
						+"PORDDET.CONV__QTY_STDUOM, "   
						+"PORDDET.CONV__RTUOM_STDUOM, "   
						+"PORDDET.UNIT__STD, "   
						+"PORDDET.PACK_CODE, "   
						+"PORDDET.NO_ART, "   
						+"PORDDET.PACK_INSTR " 
					+"FROM PORDDET, ITEM "  
						+"WHERE PORDDET.ITEM_CODE = ITEM.ITEM_CODE "
						+"AND PORDDET.PURC_ORDER ='"+purchaseOrder+"'";
			System.out.println("sql :"+sql);
			//rs = stmt.executeQuery(sql);//Modified by Rohini T on 12/04/2021
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery(sql);
			while (rs.next())
			{
				reqDate = rs.getDate(20);
				if (reqDate != null)
				{
					reqDateStr = sdf.format(reqDate);
				}
				dlvDate = rs.getDate(21);
				if (dlvDate != null)
				{
					dlvDateStr = sdf.format(dlvDate);
				}
				statusDate = rs.getDate(24);
				if (statusDate != null)
				{
					statusDateStr =  sdf.format(statusDate);
				}
				 
				valueXmlString.append("<Detail>\r\n");
					valueXmlString.append("<line_no>").append("<![CDATA[").append(rs.getString(5)).append("]]>").append("</line_no>\r\n");
					valueXmlString.append("<item_code>").append("<![CDATA[").append(rs.getString(1)).append("]]>").append("</item_code>\r\n");
					valueXmlString.append("<descr>").append("<![CDATA[").append(rs.getString(3)).append("]]>").append("</descr>\r\n");
					valueXmlString.append("<pendingqty>").append("<![CDATA[").append(rs.getDouble(2) - rs.getDouble(22)).append("]]>").append("</pendingqty>\r\n");
					valueXmlString.append("<quantity>").append("<![CDATA[").append(rs.getDouble(2)).append("]]>").append("</quantity>\r\n");
					valueXmlString.append("<unit>").append("<![CDATA[").append(rs.getString(6)).append("]]>").append("</unit>\r\n");
					valueXmlString.append("<rate>").append("<![CDATA[").append(rs.getDouble(7)).append("]]>").append("</rate>\r\n");
					valueXmlString.append("<discount>").append("<![CDATA[").append(rs.getDouble(8)).append("]]>").append("</discount>\r\n");
					valueXmlString.append("<tax_amt>").append("<![CDATA[").append(rs.getDouble(9)).append("]]>").append("</tax_amt>\r\n");
					valueXmlString.append("<tax_class>").append("<![CDATA[").append((rs.getString(10) == null) ? "":rs.getString(10) ).append("]]>").append("</tax_class>\r\n");
					valueXmlString.append("<tax_chap>").append("<![CDATA[").append((rs.getString(11) == null) ? "":rs.getString(11)).append("]]>").append("</tax_chap>\r\n");
					valueXmlString.append("<tax_env>").append("<![CDATA[").append((rs.getString(12) == null) ? "":rs.getString(12)).append("]]>").append("</tax_env>\r\n");
					valueXmlString.append("<unit__rate>").append("<![CDATA[").append(rs.getString(13)).append("]]>").append("</unit__rate>\r\n");
					valueXmlString.append("<quantity__stduom>").append("<![CDATA[").append(rs.getDouble(14)).append("]]>").append("</quantity__stduom>\r\n");
					valueXmlString.append("<rate__stduom>").append("<![CDATA[").append(rs.getString(15)).append("]]>").append("</rate__stduom>\r\n");
					valueXmlString.append("<site_code>").append("<![CDATA[").append((rs.getString(16) == null) ? "":rs.getString(16)).append("]]>").append("</site_code>\r\n");
					valueXmlString.append("<ind_no>").append("<![CDATA[").append((rs.getString(17) == null) ? "":rs.getString(17)).append("]]>").append("</ind_no>\r\n");
					valueXmlString.append("<tot_amt>").append("<![CDATA[").append(rs.getString(18)).append("]]>").append("</tot_amt>\r\n");
					valueXmlString.append("<loc_code>").append("<![CDATA[").append(rs.getString(19)).append("]]>").append("</loc_code>\r\n");
					valueXmlString.append("<req_date>").append("<![CDATA[").append(reqDateStr).append("]]>").append("</req_date>\r\n");
					valueXmlString.append("<dlv_date>").append("<![CDATA[").append(dlvDateStr).append("]]>").append("</dlv_date>\r\n");
					valueXmlString.append("<dlv_qty>").append("<![CDATA[").append(rs.getDouble(22)).append("]]>").append("</dlv_qty>\r\n");
					valueXmlString.append("<status>").append("<![CDATA[").append(rs.getString(23)).append("]]>").append("</status>\r\n");
					valueXmlString.append("<status_date>").append("<![CDATA[").append(statusDateStr).append("]]>").append("</status_date>\r\n");
					valueXmlString.append("<work_order>").append("<![CDATA[").append((rs.getString(26) == null) ? "":rs.getString(26)).append("]]>").append("</work_order>\r\n");
					valueXmlString.append("<conv__qty_stduom>").append("<![CDATA[").append(rs.getDouble(27)).append("]]>").append("</conv__qty_stduom>\r\n");
					valueXmlString.append("<conv__rtuom_stduom>").append("<![CDATA[").append(rs.getDouble(28)).append("]]>").append("</conv__rtuom_stduom>\r\n");
					valueXmlString.append("<unit__std>").append("<![CDATA[").append(rs.getString(29)).append("]]>").append("</unit__std>\r\n");
					valueXmlString.append("<pack_code>").append("<![CDATA[").append(rs.getString(30)).append("]]>").append("</pack_code>\r\n");
					valueXmlString.append("<no_art>").append("<![CDATA[").append(rs.getDouble(31)).append("]]>").append("</no_art>\r\n");
					valueXmlString.append("<pack_instr>").append("<![CDATA[").append((rs.getString(32) == null) ? "":rs.getString(32)).append("]]>").append("</pack_instr>\r\n");
					valueXmlString.append("<purc_order>").append("<![CDATA[").append(rs.getString(4)).append("]]>").append("</purc_order>\r\n");
					valueXmlString.append("<remarks>").append("<![CDATA[").append((rs.getString(25) == null) ? "":rs.getString(25)).append("]]>").append("</remarks>\r\n");
				valueXmlString.append("</Detail>\r\n");
				reqDateStr = "";
			}//while end
			
			if(rs != null)
			{
				rs.close();
				rs = null; 
			}
			//Modified by Rohini T on 12/04/2021[Start]
			if(pstmt != null)
			{
				pstmt.close();
				pstmt = null;
			}
			/*if(stmt != null)
			{
				stmt.close();
				stmt = null;
			}*/
			//Modified by Rohini T on 12/04/2021[End]
			valueXmlString.append("</Root>\r\n");			
		}//try end
		catch(Exception e)
		{
			System.out.println("Exception :Porcp :" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("Closing Connection.....");
				conn.close();
				conn = null;
			}catch(Exception e){}
		}
		System.out.println("valueXmlString.toString() @@@"+valueXmlString.toString());
		return valueXmlString.toString();		
	}
	
	private String pendingQtyTransform(Document dom,Document dom1, String objContext, String xtraParams, Document selDataDom)throws ITMException
	{
		System.out.println("pendingQtyTransform is calling.............@@@");
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		String sql = "";
		String detCnt = "0";
		Connection conn = null;
		//Statement stmt = null;//Modified by Rohini T on 12/04/2021
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		int count = 0;
		ConnDriver connDriver = new ConnDriver();
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			//stmt = conn.createStatement();//Modified by Rohini T on 12/04/2021
			//detCnt = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"detCnt"); //Will be Uncommented later - Aviprash 30/01/06
			String tranId = new  ibase.utility.E12GenericUtility().getColumnValue("tran_id", dom1);
			System.out.println("tranId  :"+tranId);
			NodeList detailList = selDataDom.getElementsByTagName("Detail");
			int noOfDetails = detailList.getLength();
			for(int ctr = 0; ctr < noOfDetails; ctr++)
			{
				Node currDetail = detailList.item(ctr);
				String purcOrder = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("purc_order", currDetail);
				String lineNo = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("line_no", currDetail);
				
				sql = "SELECT COUNT(*) FROM PORCPDET WHERE TRAN_ID = '"+tranId+"' " 
					 +"AND PURC_ORDER = '"+purcOrder+"' AND LINE_NO = '"+lineNo+"'";
				System.out.println("sql :"+sql);
				//rset = stmt.executeQuery(sql);//Modified by Rohini T on 12/04/2021
				pstmt = conn.prepareStatement(sql);
				rset = pstmt.executeQuery(sql);
				if (rset.next())
				{
					count = rset.getInt(1);
					System.out.println("count :"+count);
				}
				
				//Added by Jaffar S. on 03-01-19 for closing connection & statement --[Start]
				if(rset != null)
				{
					rset.close();
					rset = null; 
				}
				//Modified by Rohini T on 12/04/2021[Start]
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				/*if(stmt != null)
				{
					stmt.close();
					stmt = null;
				}*/
				//Modified by Rohini T on 12/04/2021[End]
				//Added by Jaffar S. on 03-01-19 for closing connection & statement --[End]
				
				if (count == 0 || detCnt.equals("0"))
				{
					valueXmlString.append("<Detail>");
					String itemCode = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("item_code", currDetail);
					String locCode = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("loc_code", currDetail);
					String cPendingQty = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("pendingqty", currDetail);
					String rate = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("rate", currDetail);
					String qtyStduom = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("quantity__stduom", currDetail);
					String unitStd = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("unit__std", currDetail);
					String rateStduom = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("rate__stduom", currDetail);
					String unit = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("unit", currDetail);
					String convQtyStduom = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("conv__qty_stduom", currDetail);
					String convrtuomStduom = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("conv__rtuom_stduom", currDetail);
					String unitRate = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("unit__rate", currDetail);
					String taxAmt = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("tax_amt", currDetail);

					valueXmlString.append("<purc_order isSrvCallOnChg='0'>").append("<![CDATA[").append(purcOrder).append("]]>").append("</purc_order>\r\n");
					valueXmlString.append("<item_code isSrvCallOnChg='0'>").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code>\r\n");
					valueXmlString.append("<line_no__ord isSrvCallOnChg='1'>").append("<![CDATA[").append(lineNo).append("]]>").append("</line_no__ord>\r\n");
					valueXmlString.append("<loc_code isSrvCallOnChg='0'>").append("<![CDATA[").append(locCode).append("]]>").append("</loc_code>\r\n");
					valueXmlString.append("<quantity isSrvCallOnChg='0'>").append("<![CDATA[").append(cPendingQty).append("]]>").append("</quantity>\r\n");
					valueXmlString.append("<rate isSrvCallOnChg='0'>").append("<![CDATA[").append(rate).append("]]>").append("</rate>\r\n");
					valueXmlString.append("<quantity__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(qtyStduom).append("]]>").append("</quantity__stduom>\r\n");
					valueXmlString.append("<unit__std isSrvCallOnChg='0'>").append("<![CDATA[").append(unitStd).append("]]>").append("</unit__std>\r\n");
					valueXmlString.append("<rate__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(rateStduom).append("]]>").append("</rate__stduom>\r\n");
					valueXmlString.append("<unit isSrvCallOnChg='0'>").append("<![CDATA[").append(unit).append("]]>").append("</unit>\r\n");
					valueXmlString.append("<conv__qty_stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(convQtyStduom).append("]]>").append("</conv__qty_stduom>\r\n");
					valueXmlString.append("<conv__rtuom_stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(convrtuomStduom).append("]]>").append("</conv__rtuom_stduom>\r\n");
					valueXmlString.append("<unit__rate isSrvCallOnChg='0'>").append("<![CDATA[").append(unitRate).append("]]>").append("</unit__rate>\r\n");
					valueXmlString.append("<tax_amt isSrvCallOnChg='0'>").append("<![CDATA[").append(taxAmt).append("]]>").append("</tax_amt>\r\n");
					valueXmlString.append("</Detail>");	
				}			
			}
			valueXmlString.append("</Root>");
		}
		catch(ITMException itme)
		{
			throw itme;
		}
		catch(Exception e)
		{
			throw new ITMException(e);
		}
		
		//Added by Jaffar S. on 03-01-19 for closing connection & statement --[Start]
		finally 
		{
    	  try
			{
				if (rset != null)
				{
					rset.close();
					rset = null;
				}
				//Modified by Rohini T on 12/04/2021[Start]
				if (pstmt != null )
				{
					pstmt.close();
					pstmt = null;
				}
				/*if (stmt != null )
				{
					stmt.close();
					stmt = null;
				}*/
				//Modified by Rohini T on 12/04/2021[End]
				if (conn != null )
				{
					conn.close();
					conn = null;
				}
				
			}
			catch(Exception e)
			{
				System.out.println("Exception :: "+e);
				e.printStackTrace();
			}
			
		}
		//Added by Jaffar S. on 03-01-19 for closing connection & statement --[End]
		
		System.out.println("valueXmlString from :"+valueXmlString.toString());
		return valueXmlString.toString();
	}
}