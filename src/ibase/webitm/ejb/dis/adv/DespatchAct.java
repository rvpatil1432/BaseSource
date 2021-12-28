package ibase.webitm.ejb.dis.adv;

import ibase.system.config.ConnDriver;
import ibase.utility.CommonConstants;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorLocal;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.utility.ITMException;

import java.io.ByteArrayOutputStream;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.ejb.Stateless;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Stateless // added for ejb3
public class DespatchAct extends ActionHandlerEJB implements DespatchActLocal, DespatchActRemote
{
	
	String DB = CommonConstants.DB_NAME;                         //Added by Manish on 10/10/2015
	ibase.utility.E12GenericUtility genericUtility= new ibase.utility.E12GenericUtility();
	String currentActionType="",packInstrStock="",packInstrInvPackRcp="";
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
		String retString = "";
		Document dom = null;
		try
		{
			System.out.println("Call method =Action handler");
			ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				System.out.println("XML String :"+xmlString);
				dom = genericUtility.parseString(xmlString);
			}
			if (actionType.equalsIgnoreCase("Alloc Item"))
			{
				retString = actionAllocItem(dom,objContext,xtraParams);
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :Dispatch :actionHandler(String xmlString):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		return retString;
	}

	//Call new method actionHandler with three parameters by Birendra Pandey

	public String actionHandler(String tranId, String forcedFlag,String xtraParams) throws RemoteException,ITMException
	{
		String retString = "";
		System.out.println("Call new method actionHandler with three parameters");
		System.out.println("Tran_ID="+tranId);
		Connection conn = null;
		PreparedStatement pstmt = null;	
		ResultSet rs = null;
		String status="";
		String confirmed="";
		int upcount=0;
		String sql="";

		java.sql.Timestamp currDate=new java.sql.Timestamp(System.currentTimeMillis());
		System.out.println("GET TIME===="+currDate);
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();		
		try {

			ConnDriver connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 

			sql="SELECT STATUS,CONFIRMED FROM  DESPATCH WHERE  DESP_ID= ?";
			//	(STATUS='O' OR STATUS=NULL) AND (CONFIRMED='N' OR CONFIRMED=NULL) AND;

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if ( rs.next() )
			{
				status      = rs.getString("STATUS");
				confirmed   = rs.getString("CONFIRMED");
			}
			status      =((status==null)?"":status.trim());
			confirmed   =((confirmed==null)?"":confirmed.trim());
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;
			if ((status.equalsIgnoreCase("O") || status.equalsIgnoreCase("")) && (confirmed.equalsIgnoreCase("N") || confirmed.equalsIgnoreCase("")) )
			{

				sql="UPDATE DESPATCH SET STATUS='H',status_date= ?  WHERE DESP_ID= ?";		    	
				pstmt =conn.prepareStatement(sql);
				pstmt.setTimestamp(1,currDate);
				pstmt.setString(2,tranId);
				upcount =pstmt.executeUpdate();
				pstmt.close();//added by Pavan R 10jan19[to handle open cursor issue]
				pstmt = null;
				if(upcount>0){
					conn.commit();
					retString = itmDBAccess.getErrorString("","VTDESHOLD ","","",conn);
				}

				else{
					retString = itmDBAccess.getErrorString("","VTENTNOTF ","","",conn);
				}
			}

			else
			{
				retString = itmDBAccess.getErrorString("","INVTR","","",conn);
			}


		}
		catch(SQLException e)
		{
			System.out.println("Exception : Dispatch : actionHandler " +e.getMessage());
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			throw new ITMException(e);
		}
		catch (Exception e) 
		{
			System.out.println("Exception : Dispatch : actionHandler " +e.getMessage());
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			throw new ITMException(e);

		}
		finally
		{
			try
			{				
				if( rs != null)
				{
					rs.close();
					rs = null;
				}
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;					
				}
				if(conn!=null)
				{
					conn.commit();
					conn.close();
					conn = null;
				}				
			}
			catch(Exception e)
			{
				System.out.println("Exception :Dispatch :actionHandler(String xmlString):" + e.getMessage() + ":");
				throw new ITMException(e);
			}
		}

		return retString;

	}

	//End of method actionHandler with three parameters

	//**************Added by Vishakha  For D14LSUN009 (Unfreezing Despatch) 06-APR-2015**************
	public String actionHandlerUnfreeze(String tranId, String forcedFlag,String xtraParams) throws RemoteException,ITMException
	{
		String retString = "";
		System.out.println("Call new method actionHandlerUnfreeze with three parameters");
		System.out.println("Tran_ID="+tranId);
		Connection conn = null;
		PreparedStatement pstmt = null;	
		ResultSet rs = null;
		String status="";
		String confirmed="";
		int upcount=0;
		String sql="";

		java.sql.Timestamp currDate=new java.sql.Timestamp(System.currentTimeMillis());
		System.out.println("GET TIME===="+currDate);
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();		
		try {

			ConnDriver connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 

			sql="SELECT STATUS,CONFIRMED FROM  DESPATCH WHERE  DESP_ID= ?";
			//	(STATUS='O' OR STATUS=NULL) AND (CONFIRMED='N' OR CONFIRMED=NULL) AND;

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if ( rs.next() )
			{
				status      = rs.getString("STATUS");
				confirmed   = rs.getString("CONFIRMED");
			}
			status      =((status==null)?"":status.trim());
			confirmed   =((confirmed==null)?"":confirmed.trim());
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;
			if (status.equalsIgnoreCase("H")  && (confirmed.equalsIgnoreCase("N") || confirmed.equalsIgnoreCase("")) )
			{

				sql="UPDATE DESPATCH SET STATUS=' ',status_date= ?  WHERE DESP_ID= ?";		//updated status as space on 8-APR-15 by vishakha    	
				pstmt =conn.prepareStatement(sql);
				pstmt.setTimestamp(1,currDate);
				pstmt.setString(2,tranId);
				upcount =pstmt.executeUpdate(); 
				pstmt.close();
				pstmt=null;
				if(upcount>0){
					retString = itmDBAccess.getErrorString("","VTDESREL ","","",conn);//depatch released successfully
				}

			}

			else
			{
				retString = itmDBAccess.getErrorString("","INVTRHLD","","",conn);//invalid status transaction should be unconfirmed
			}


		}
		catch(SQLException e)
		{
			System.out.println("Exception : Dispatch : actionHandlerUnfreeze " +e.getMessage());
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			throw new ITMException(e);
		}
		catch (Exception e) 
		{
			System.out.println("Exception : Dispatch : actionHandlerUnfreeze " +e.getMessage());
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			throw new ITMException(e);

		}
		finally
		{
			try
			{
				if( rs != null)
				{
					rs.close();
					rs = null;
				}
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;					
				}
				if(conn!=null)
				{		
					if(upcount>0)
					{
						conn.commit();
					} 
					conn.close();
					conn = null;
				}
			}
			catch(Exception e)
			{
				System.out.println("Exception :Dispatch :actionHandlerUnfreeze(String xmlString):" + e.getMessage() + ":");
				throw new ITMException(e);
			}
		}

		return retString;

	}

	//*********END of Unfreeze method**************

	public String actionHandler(String actionType, String xmlString, String xmlString1, String objContext, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;

		String  retString = null;
		try
		{
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				System.out.println("XML String :"+xmlString);
				dom = new  ibase.utility.E12GenericUtility().parseString(xmlString); 
				System.out.println("dom :"+dom);
			}
			if(xmlString1 != null && xmlString1.trim().length()!=0)
			{
				System.out.println("XML String1 :"+xmlString1);
				dom1 = new  ibase.utility.E12GenericUtility().parseString(xmlString1);
				System.out.println("dom1 :"+dom1);
			}
			System.out.println("actionType:"+actionType+":");
			currentActionType = actionType;	
			System.out.println("currentActionType:["+currentActionType+"]");

			if (actionType.equalsIgnoreCase("Stock"))
			{
				retString = actionStock(dom,dom1,objContext,xtraParams);
			}
			else if (actionType.equalsIgnoreCase("Get Pack List") ||  actionType.equalsIgnoreCase("Get SPack List") )
			{
				retString = actionGetPackList(dom,dom1,objContext,xtraParams);
			}
			else if (actionType.equalsIgnoreCase("Default"))
			{
				retString = actionDefault(dom, dom1, objContext, xtraParams);
			}
			else if (actionType.equalsIgnoreCase("Lot No"))
			{
				retString = actionLotNo(dom, dom1, objContext, xtraParams);
			}
			else if (actionType.equalsIgnoreCase("WOPackList"))
			{
				retString = actionWoGetPackList(dom, dom1, objContext, xtraParams);
			}
			else if (actionType.equalsIgnoreCase("Packing"))
			{
				retString = actionPacking(dom, dom1, objContext, xtraParams);
			}



		}
		catch(Exception e)
		{
			System.out.println("Exception :Dispatch :actionHandler(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning String from Despatch : actionHandler"+retString);
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
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				dom = new  ibase.utility.E12GenericUtility().parseString(xmlString); 
				dom1 = new  ibase.utility.E12GenericUtility().parseString(xmlString1);
				if(selDataStr != null && selDataStr.length() > 0)
				{
					selDataDom = new  ibase.utility.E12GenericUtility().parseString(selDataStr);
				}
			}
			System.out.println("actionType:"+actionType+":");

			if (actionType.equalsIgnoreCase("Stock"))
			{
				retString = stockTransform(dom, dom1, objContext, xtraParams, selDataDom);
			}
			if (actionType.equalsIgnoreCase("Alloc Item"))
			{
				retString = allocItemTransform(dom,dom1, objContext, xtraParams, selDataDom);
			}
			if (actionType.equalsIgnoreCase("WOPackList"))
			{
				retString = getWoPackListTransform(dom,dom1, objContext, xtraParams, selDataDom);
			}
			if (actionType.equalsIgnoreCase("PackList") ||  actionType.equalsIgnoreCase("SPackList") )//Gulzar 23-04-07 - 
			{
				retString = packListTransform(dom,dom1, objContext, xtraParams, selDataDom);
			}
			if (actionType.equalsIgnoreCase("Packing"))//Alka 23-04-07 - 
			{
				retString = actionPackingTransform(dom, dom1, objContext, xtraParams, selDataDom);
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :Dispatch :actionHandlerTransform(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		//System.out.println("returning String from Despatch : actionHandlerTransform"+retString);//Gulzar - 22/01/08
		System.out.println("returning String from Despatch.................."); //Gulzar - 22/01/08
		return retString;
	}

	private String actionAllocItem(Document dom, String objContext, String xtraParams) throws RemoteException , ITMException
	{
		String saleOrder = "";
		String errCode = "";
		String errString = "";
		String sql = "";
		ResultSet rs = null;
		Connection conn = null;
		//Statement stmt = null;//Changed and added  by Pavan R 10jan19[to handle open cursor issue]
		PreparedStatement pstmt = null;
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
			saleOrder = genericUtility.getColumnValue("sord_no",dom);	
			System.out.println("saleOrder :"+saleOrder);
			if(saleOrder != null && saleOrder.trim().length() > 0)
			{
				sql="SELECT "
					+"SORDALLOC.LINE_NO, "   
					+"SORDALLOC.ITEM_CODE, "
					+"SORDALLOC.LOC_CODE, "   
					+"SORDALLOC.LOT_NO, "   
					+"SORDALLOC.LOT_SL, "   
					+"ITEM.DESCR, "   
					+"SORDALLOC.QTY_ALLOC "  
					+"FROM SORDALLOC, ITEM "  
					+"WHERE SORDALLOC.ITEM_CODE = ITEM.ITEM_CODE "
					+"AND SORDALLOC.SALE_ORDER = ?";
				//Changed and added  by Pavan R 10jan19[to handle open cursor issue]
				//	+"AND SORDALLOC.SALE_ORDER = '"+ saleOrder +"'";				
				//System.out.println("Sorder SQL :="+sql);
				//stmt = conn.createStatement();
				//rs = stmt.executeQuery(sql);
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, saleOrder);
				rs = pstmt.executeQuery();
				while (rs.next())
				{
					valueXmlString.append("<Detail>\r\n");
					valueXmlString.append("<sord_no>").append("<![CDATA[").append(saleOrder).append("]]>").append("</sord_no>\r\n");
					valueXmlString.append("<line_no__sord>").append("<![CDATA[").append(rs.getString(1).trim()).append("]]>").append("</line_no__sord>\r\n");
					valueXmlString.append("<item_code>").append("<![CDATA[").append(rs.getString(2).trim()).append("]]>").append("</item_code>\r\n");
					valueXmlString.append("<lot_no>").append("<![CDATA[").append(rs.getString(4).trim()).append("]]>").append("</lot_no>\r\n");
					valueXmlString.append("<lot_sl>").append("<![CDATA[").append(rs.getString(5).trim()).append("]]>").append("</lot_sl>\r\n");
					valueXmlString.append("<loc_code>").append("<![CDATA[").append(rs.getString(3).trim()).append("]]>").append("</loc_code>\r\n");
					valueXmlString.append("<descr>").append("<![CDATA[").append(rs.getString(6).trim()).append("]]>").append("</descr>\r\n");
					valueXmlString.append("<qty_alloc>").append("<![CDATA[").append(rs.getString(7)).append("]]>").append("</qty_alloc>\r\n");
					valueXmlString.append("</Detail>\r\n");
				}
				//Added by Pavan R 10jan19[to handle open cursor issue]
				//stmt.close();
				rs.close();
				rs =   null;
				pstmt.close();
				pstmt = null;
				valueXmlString.append("</Root>\r\n");		
				String retXmlString = serializeDom(genericUtility.parseString(valueXmlString.toString()));
				valueXmlString = null;
				System.out.println("manohar 07/02/11 retXmlString 2 ["+ retXmlString +"]");
				valueXmlString =  new StringBuffer(retXmlString);

			}
			else
			{
				errCode = "VTNLLSORD";
			}
			if (!errCode.equals(""))
			{
				errString = itmDBAccess.getErrorString("",errCode,"","",conn);
				return errString;
			}
		}
		catch(SQLException e)
		{
			System.out.println("Exception : Dispatch : actionVoucher " +e.getMessage());
			throw new ITMException(e);
		}
		catch(Exception e)
		{
			System.out.println("Exception : Dispatch : actionHandler " +e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if (rs!=null)
				{
					rs.close();
					rs=null;
				}
				if (pstmt!=null)
				{
					pstmt.close();
					pstmt=null;
				}
				if(conn != null)
				{
					conn.close();
					conn = null;
				}
			}
			catch(Exception e){}
		}
		System.out.println("valueXmlString.toString() "+valueXmlString.toString());
		return valueXmlString.toString();		
	}

	private String allocItemTransform(Document dom,Document dom1, String objContext, String xtraParams, Document selDataDom)throws ITMException
	{
		double icQtyOrd = 0.0;
		double alocQty = 0.0;
		Connection conn = null;

		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");

		String icQtyOrdStr = new  ibase.utility.E12GenericUtility().getColumnValue("quantity", dom);
		String lineNoSord = new  ibase.utility.E12GenericUtility().getColumnValue("line_no__sord", dom);
		String expLev = new  ibase.utility.E12GenericUtility().getColumnValue("exp_lev", dom);

		System.out.println("icQtyOrdStr["+icQtyOrdStr+"] lineNoSord [ "+lineNoSord+"]  expLev ["+expLev+"]");

		if(icQtyOrdStr != null && icQtyOrdStr.trim().length() > 0)
		{
			icQtyOrd = Double.parseDouble(icQtyOrdStr);
		}
		ConnDriver connDriver = new ConnDriver();
		try
		{

			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			NodeList detailList = selDataDom.getElementsByTagName("Detail");
			int noOfDetails = detailList.getLength();
			System.out.println("No Of Selected Record:::-["+noOfDetails+"]");
			for(int ctr = 0; ctr < noOfDetails; ctr++)
			{
				if (icQtyOrd <= 0){
					break;
				}	

				alocQty = 0.0;

				valueXmlString.append("<Detail>");
				Node currDetail = detailList.item(ctr);

				String lineNo = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("line_no", currDetail);
				String lotNo = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("lot_no", currDetail);
				String lotSl = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("lot_sl", currDetail);
				String lotCode = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("loc_code", currDetail);
				String alocQtyStr = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("qty_alloc", currDetail);

				System.out.println("lineNo["+lineNo+"] lotNo [ "+lotNo+"]  lotSl ["+lotSl+"] lotCode ["+lotCode+"] alocQtyStr ["+alocQtyStr+"]");

				if(alocQtyStr != null && alocQtyStr.trim().length() > 0){
					alocQty = Double.parseDouble(alocQtyStr);
				} 
				if(alocQty > icQtyOrd){
					alocQty = icQtyOrd;
				}
				icQtyOrd = icQtyOrd - alocQty;

				valueXmlString.append("<line_no__sord isSrvCallOnChg='0'>").append(lineNoSord).append("</line_no__sord>");
				setNodeValue( dom, "line_no__sord", (lineNoSord == null) ? "":lineNoSord );
				lineNoSord = getChangeSord(dom,  dom1, "line_no__sord", xtraParams ,conn); // chg 21
				valueXmlString.append(lineNoSord);

				valueXmlString.append("<exp_lev isSrvCallOnChg='0'>").append(expLev).append("</exp_lev>");
				setNodeValue( dom, "exp_lev", (expLev == null) ? "":expLev );
				expLev = getChangeSord(dom,  dom1, "exp_lev", xtraParams ,conn); // chg 22
				valueXmlString.append(expLev);
				valueXmlString.append("<loc_code>").append(lotCode).append("</loc_code>"); //Gulzar 08-01-07 shifted to above of lot_no
				setNodeValue( dom, "loc_code", (lotCode == null) ? "":lotCode );
				valueXmlString.append("<lot_no>").append(lotNo).append("</lot_no>");
				setNodeValue( dom, "lot_no", (lotNo == null) ? "":lotNo );
				valueXmlString.append("<lot_sl isSrvCallOnChg='0'>").append(lotSl).append("</lot_sl>");				
				setNodeValue( dom, "lot_sl", (lotSl == null) ? "":lotSl );
				lotSl = getChangeSord(dom,  dom1, "lot_sl", xtraParams ,conn); // chg 23
				valueXmlString.append(lotSl);

				valueXmlString.append("<quantity isSrvCallOnChg='0'>").append(alocQty).append("</quantity>");
				setNodeValue( dom, "quantity", alocQty );
				//alocQty = Double.parseDouble(getChangeSord(dom,  dom1, "quantity", xtraParams ,conn)); // chg 24
				valueXmlString.append(getChangeSord(dom,  dom1, "quantity", xtraParams ,conn));

				valueXmlString.append("</Detail>");				
			}
			valueXmlString.append("</Root>");
			String retXmlString = serializeDom(genericUtility.parseString(valueXmlString.toString()));
			valueXmlString = null;
			System.out.println("manohar 07/02/11 retXmlString 3 ["+ retXmlString +"]");
			valueXmlString =  new StringBuffer(retXmlString);
		}
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
				System.out.println("Closing Connection.....");
				if(conn != null)
				{
					conn.close();
					conn = null;
				}
			}catch(Exception e){}
		}
		return valueXmlString.toString();
	}

	private String actionStock(Document dom,Document dom1, String objContext, String xtraParams) throws RemoteException , ITMException
	{
		Connection conn = null;
		//Statement stmt = null;//Changed by Pavan R 10jan19[to handle open cursor issue]
		ResultSet rs = null;
		String sql = "";
		PreparedStatement pstmt = null;
		String errCode = "",saleOrder ="",orderType="",nearExpiry="",mexplev="",mlineno="",despDate="",ls_track_shelf_life="";
		String itemCode = "",locCode = "",quantity = "",siteCode = "",available = "",stkItemCode = "";
		String stklotNo = "",stklotSl = "",stkAllocQty = "";
		java.sql.Date stkMfgDate = null;
		String stkMfgDate1 = ""; 
		java.sql.Date stkExpDate = null;
		String stkExpDate1 = "";
		String stkquantity = "";
		String stkLocCode = "";
		String stkNoArt = "" ;
		String stkRate = "";
		String balanceQty = "";
		String trackShelfLife = "";
		boolean nexpStock = false;
		int ll_min_shelf_life=0,ll_max_shelf_life = 0;
		Timestamp expDate = null,despTDate = null,ld_chk_date1=null,ld_chk_date2=null;
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		DistCommon distCommon = new DistCommon();
		ConnDriver connDriver = new ConnDriver();

		try
		{	
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			//stmt = conn.createStatement(); //Changed by Pavan R 22oct18[to handle open cursor issue]
			//Getting values from dom (current form), dom1(form no 1)
			itemCode	= genericUtility.getColumnValue("item_code",dom);
			locCode		= genericUtility.getColumnValue("loc_code",dom);
			quantity	= genericUtility.getColumnValue("quantity",dom);
			siteCode	= genericUtility.getColumnValue("site_code",dom1);
			saleOrder   = genericUtility.getColumnValue("sord_no",dom1);
			mexplev = (genericUtility.getColumnValue("exp_lev", dom));
			mlineno = (genericUtility.getColumnValue("line_no__sord", dom));
			mlineno = "   "+mlineno;
			mlineno = mlineno.substring(mlineno.length()-3,mlineno.length());
			despDate = genericUtility.getColumnValue("desp_date", dom1);
			if( despDate != null && despDate.trim().length() > 0 )
			{
				despTDate = Timestamp.valueOf(genericUtility.getValidDateString(despDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");
			}
			available	= "Y";
			
			sql = " select (case when track_shelf_life is null then 'N' else track_shelf_life end) " +
					"	from item where item_code = ?  ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, itemCode);
			rs = pstmt.executeQuery();
			if (rs.next()) 
			{
				trackShelfLife = rs.getString(1);
			}
			rs.close(); rs = null;
			pstmt.close(); pstmt = null;
			if(locCode == null || locCode.trim().length() == 0)
			{
				locCode = "%";
			}
			else
			{
				locCode = locCode.trim() + "%";
			}//end if
			sql="SELECT (CASE WHEN TRACK_SHELF_LIFE IS NULL THEN 'N' ELSE TRACK_SHELF_LIFE END) "+
			" FROM ITEM WHERE ITEM_CODE = ?"; //'"+itemCode+"'" ;
			//Changed and added by Pavan R 22oct18[to handle open cursor issue]
			//rs = stmt.executeQuery(sql);
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, itemCode);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				trackShelfLife =rs.getString(1);
			}
			rs.close();rs = null;
			pstmt.close(); pstmt = null;
			
			//added by kunal on on 10/1/2019 to get check Near expiry order type 
			sql = "Select order_type from sorder where sale_order = '"+saleOrder+"'";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				orderType = rs.getString(1);
			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;			
			
			if("Y".equalsIgnoreCase(trackShelfLife.trim()))
			{
				
					sql = " select min_shelf_life,max_shelf_life " +
							"	from sorditem where sale_order = ? " +
							"	and line_no = ? 	and exp_lev = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, saleOrder);
					pstmt.setString(2, mlineno);
					pstmt.setString(3, mexplev);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						ll_min_shelf_life = rs.getInt("min_shelf_life");
						ll_max_shelf_life = rs.getInt("max_shelf_life");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					System.out.println("despTDate:"+despTDate+":ll_min_shelf_life:"+ll_min_shelf_life);	
					ld_chk_date1 = distCommon.CalcExpiry(despTDate,ll_min_shelf_life + 1);
	
					ld_chk_date2 = distCommon.CalcExpiry(despTDate,ll_max_shelf_life);
			}
			//added by kunal on on 10/1/2019 to get check Near expiry order type end
			
			System.out.println("itemCode:"+itemCode+":locCode:"+locCode+":quantity:"+quantity+":siteCode:"+siteCode+":");
			//Changed and added by Pavan R 22oct18[to handle open cursor issue] Start
			sql="SELECT STOCK.ITEM_CODE,STOCK.LOT_NO, STOCK.LOT_SL,STOCK.ALLOC_QTY, STOCK.MFG_DATE, "+
					"  STOCK.EXP_DATE, STOCK.QUANTITY,STOCK.QUANTITY - STOCK.ALLOC_QTY AS BALANCE_QTY, " + 
					" STOCK.LOC_CODE,STOCK.NO_ART,STOCK.RATE "+
					"  FROM STOCK,INVSTAT "+
					"  WHERE ( STOCK.INV_STAT = INVSTAT.INV_STAT ) AND  " + 
					" ( ( STOCK.ITEM_CODE = ? ) AND " + 
					" ( STOCK.LOC_CODE LIKE ? ) AND  " + 
					" ( STOCK.SITE_CODE = ? ) AND  " + 
					" ( STOCK.QUANTITY > 0 ) AND  " + 
					" ( INVSTAT.AVAILABLE = ? )  AND " + 
					" ( INVSTAT.STAT_TYPE = 'M' ) ) " ;
			
			if(trackShelfLife.equalsIgnoreCase("N"))
			{
				/*sql="SELECT STOCK.ITEM_CODE,STOCK.LOT_NO, STOCK.LOT_SL,STOCK.ALLOC_QTY, STOCK.MFG_DATE, "+
				"  STOCK.EXP_DATE, STOCK.QUANTITY,STOCK.QUANTITY - STOCK.ALLOC_QTY AS BALANCE_QTY, " + 
				" STOCK.LOC_CODE,STOCK.NO_ART,STOCK.RATE "+
				"  FROM STOCK,INVSTAT "+
				"  WHERE ( STOCK.INV_STAT = INVSTAT.INV_STAT ) AND  " + 
				" ( ( STOCK.ITEM_CODE = '"+itemCode+"' ) AND " + 
				" ( STOCK.LOC_CODE LIKE '"+locCode+"' ) AND  " + 
				" ( STOCK.SITE_CODE = '"+siteCode+"' ) AND  " + 
				" ( STOCK.QUANTITY > 0 ) AND  " + 
				" ( INVSTAT.AVAILABLE = '"+available+"' )  AND " + 
				" ( INVSTAT.STAT_TYPE = 'M' ) ) " ;*/
			}
			else
			{
				/*sql="SELECT STOCK.ITEM_CODE,STOCK.LOT_NO, STOCK.LOT_SL,STOCK.ALLOC_QTY, STOCK.MFG_DATE, "+
				"  STOCK.EXP_DATE, STOCK.QUANTITY,STOCK.QUANTITY - STOCK.ALLOC_QTY AS BALANCE_QTY, " + 
				" STOCK.LOC_CODE,STOCK.NO_ART,STOCK.RATE "+
				"  FROM STOCK,INVSTAT "+
				"  WHERE ( STOCK.INV_STAT = INVSTAT.INV_STAT ) AND  " + 
				" ( ( STOCK.ITEM_CODE = '"+itemCode+"' ) AND " + 
				" ( STOCK.LOC_CODE LIKE '"+locCode+"' ) AND  " + 
				" ( STOCK.SITE_CODE = '"+siteCode+"' ) AND  " + 
				" ( STOCK.QUANTITY > 0 ) AND  " + 
				" ( INVSTAT.AVAILABLE = '"+available+"' )  AND " + 
				" ( INVSTAT.STAT_TYPE = 'M' ) ) ORDER BY STOCK.EXP_DATE ASC ";*/
				sql = sql + " ORDER BY STOCK.EXP_DATE ASC ";
			}
			//System.out.println("Despatch:actionStock:sql:"+sql);			
			//rs = stmt.executeQuery(sql);
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, itemCode);
			pstmt.setString(2, locCode);
			pstmt.setString(3, siteCode);
			pstmt.setString(4, available);
			rs = pstmt.executeQuery();
			//Changed and added by Pavan R 22oct18[to handle open cursor issue] End
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			while(rs.next())
			{
				stkItemCode = rs.getString("ITEM_CODE");
				stklotNo	= rs.getString("LOT_NO");
				stklotSl	= rs.getString("LOT_SL");
				stkAllocQty = rs.getString("ALLOC_QTY");
				stkMfgDate	= rs.getDate("MFG_DATE");
				if(stkMfgDate != null)
				{
					stkMfgDate1 = sdf.format(stkMfgDate);
				}
				else
				{
					stkMfgDate1 = "";
				}
				stkExpDate = rs.getDate("EXP_DATE");
				if(stkExpDate != null)
				{
					stkExpDate1 = sdf.format(stkExpDate);
					expDate = new Timestamp(stkExpDate.getTime());
				}
				else
				{
					stkExpDate1 = "";
					//continue;
				}
				//expDate = new Timestamp(stkExpDate.getTime());
				System.out.println("Inside loop expDate ["+expDate+"]");
				
				
				stkquantity = rs.getString("QUANTITY");
				balanceQty = rs.getString("BALANCE_QTY");
				stkLocCode = rs.getString("LOC_CODE");
				stkNoArt = rs.getString("NO_ART");
				if (stkNoArt == null)
				{
					stkNoArt = "1";
				}
				stkRate =rs.getString("RATE");
				
				//added by kunal on 10/1/2019 to check trackshelf and near expiry start
				if("Y".equalsIgnoreCase(trackShelfLife.trim()))
				{
					//added by kunal on 10/1/2019 to skip lot if expired
					if(expDate == null || expDate.before(despTDate) || expDate.equals(despTDate))
					{
						continue;
					}
					//added by kunal on 10/1/2019 to skip if stock not near expiry for NE
					if("NE".equalsIgnoreCase(orderType.trim()))
					{
						if ((expDate.after(ld_chk_date1)))
						{
							continue;
						}
						
					}
					//added by kunal on 10/1/2019 to skip if stock is near expiry and order type not NE
					else if((expDate.before(ld_chk_date1)))
					{
						continue;
					}
				}
				//added by kunal on 10/1/2019 to check trackshelf and near expiry end
				// 20/03/12 manoharan
				if(stkItemCode == null)
				{
					stkItemCode = "";
				}
				if(stkLocCode == null)
				{
					stkLocCode = "";
				}
				if(stklotNo == null)
				{
					stklotNo = "               ";
				}
				if(stklotSl == null)
				{
					stklotSl = "     ";
				}
				if(stkquantity == null)
				{
					stkquantity = "0";
				}
				if(stkAllocQty == null)
				{
					stkAllocQty = "0";
				}
				if(stkNoArt == null)
				{
					stkNoArt = "0";
				}
				if(stkRate == null)
				{
					stkRate = "0";
				}
				// end 20/03/12 manoharan
				//if (Integer.parseInt(balanceQty) > 0)
				if (Double.parseDouble(balanceQty) > 0)		
				{	
					valueXmlString.append("<Detail>\r\n");
					valueXmlString.append("<item_code>").append("<![CDATA[").append(stkItemCode).append("]]>").append("</item_code>\r\n");
					valueXmlString.append("<loc_code>").append("<![CDATA[").append(stkLocCode).append("]]>").append("</loc_code>\r\n");
					valueXmlString.append("<lot_no>").append("<![CDATA[").append(stklotNo).append("]]>").append("</lot_no>\r\n");
					valueXmlString.append("<lot_sl>").append("<![CDATA[").append(stklotSl).append("]]>").append("</lot_sl>\r\n");
					valueXmlString.append("<mfg_date>").append("<![CDATA[").append(stkMfgDate1).append("]]>").append("</mfg_date>\r\n");
					valueXmlString.append("<exp_date>").append("<![CDATA[").append(stkExpDate1).append("]]>").append("</exp_date>\r\n");
					//valueXmlString.append("<quantity>").append("<![CDATA[").append(balanceQty.trim()).append("]]>").append("</quantity>\r\n"); //Gulzar 13/03/07
					valueXmlString.append("<quantity>").append("<![CDATA[").append(stkquantity.trim()).append("]]>").append("</quantity>\r\n"); //Gulzar 13/03/07
					valueXmlString.append("<balance_qty>").append("<![CDATA[").append(balanceQty).append("]]>").append("</balance_qty>\r\n"); // Un-Commented By Gulzar 13/03/07
					valueXmlString.append("<alloc_qty>").append("<![CDATA[").append(stkAllocQty).append("]]>").append("</alloc_qty>\r\n");
					valueXmlString.append("<no_art>").append("<![CDATA[").append(stkNoArt).append("]]>").append("</no_art>\r\n");
					//valueXmlString.append("<rate>").append("<![CDATA[").append(stkRate).append("]]>").append("</rate>\r\n");
					//setNodeValue( dom, "rate", stkRate );
					// added arun pal 12-oct-2017
					valueXmlString.append("</Detail>\r\n");
				}
			}//end While loop
			//Added by Pavan R 22oct18[to handle open cursor issue]
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			valueXmlString.append("</Root>\r\n");
			String retXmlString = serializeDom(genericUtility.parseString(valueXmlString.toString()));
			valueXmlString = null;
			System.out.println("manohar 07/02/11 retXmlString 4 ["+ retXmlString +"]");
			valueXmlString =  new StringBuffer(retXmlString);
		}
		catch(Exception e)
		{
			System.out.println("Exception :Despatch :" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if (rs!=null)
				{
					rs.close();
					rs=null;
				}
				if (pstmt!=null)
				{
					pstmt.close();
					pstmt=null;
				}
				System.out.println("Closing Connection.....");
				if(conn != null)
				{				
					conn.close();
					conn = null;
				}
			}catch(Exception e){}
		}
		System.out.println("Despatch:actionStock:Final Value :valueXmlString.toString():"+valueXmlString.toString());
		return valueXmlString.toString();
	}
	/* -- Commented and Changes Below - Gulzar - 18/01/08
	private String stockTransform(Document dom,Document dom1, String objContext, String xtraParams, Document selDataDom)throws ITMException
	{
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		try
		{
			String icQtyOrdStr = new  ibase.utility.E12GenericUtility().getColumnValue("quantity", dom);
			double icQtyOrd = 0.0;
			if(icQtyOrdStr != null && icQtyOrdStr.trim().length() > 0)
			{
				icQtyOrd = Double.parseDouble(icQtyOrdStr);
			}
			String lineNoSord = new  ibase.utility.E12GenericUtility().getColumnValue("line_no__sord", dom);
			String expLevel = new  ibase.utility.E12GenericUtility().getColumnValue("exp_lev", dom);
			String packInstr = new  ibase.utility.E12GenericUtility().getColumnValue("pack_instr", dom);
			NodeList detailList = selDataDom.getElementsByTagName("Detail");
			int noOfDetails = detailList.getLength();
			for(int ctr = 0; ctr < noOfDetails; ctr++)
			{
				if (icQtyOrd > 0)
				{
					valueXmlString.append("<Detail>");
					Node currDetail = detailList.item(ctr);
					String lotNo = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("lot_no", currDetail);
					String lotSl = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("lot_sl", currDetail);
					System.out.println("Lot No In stockTransform :: " + lotNo);
					System.out.println("Lot Sl In stockTransform :: " + lotSl);
					if((lotSl == null) || (lotSl != null && lotSl.trim().length() == 0))
					{
						lotSl = " ";
					}
					String lotCode = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("loc_code", currDetail);
					String cQtyStr = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("quantity", currDetail);
					System.out.println("Location Code In stockTransform In Curr Detail :: " + lotCode);
					double cQty = 0.0;
					if(cQtyStr != null && cQtyStr.trim().length() > 0)
					{
						cQty = Double.parseDouble(cQtyStr);
					}
					String cAllocQtyStr = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("alloc_qty", currDetail);
					System.out.println("Alloc Quantity [cAllocQtyStr] : In stockTransform In Curr Detail Before Parsing:: " + cAllocQtyStr);
					double cAllocQty = 0.0;
					if(cAllocQtyStr != null && cAllocQtyStr.trim().length() > 0)
					{
						cAllocQty = Double.parseDouble(cAllocQtyStr);
					}
					double balQty = cQty - cAllocQty;
					if (balQty > icQtyOrd)
					{
						balQty  = icQtyOrd;
					}
					valueXmlString.append("<line_no__sord isSrvCallOnChg='1'>").append(lineNoSord).append("</line_no__sord>");
					valueXmlString.append("<exp_lev isSrvCallOnChg='1'>").append(expLevel).append("</exp_lev>");
					valueXmlString.append("<loc_code>").append(lotCode).append("</loc_code>");
					valueXmlString.append("<lot_no>").append(lotNo).append("</lot_no>");
					valueXmlString.append("<lot_sl isSrvCallOnChg='1'>").append(lotSl).append("</lot_sl>");
					valueXmlString.append("<quantity isSrvCallOnChg='1'>").append(balQty).append("</quantity>");
					valueXmlString.append("<pending_qty isSrvCallOnChg='0'>").append(icQtyOrd).append("</pending_qty>");
					valueXmlString.append("</Detail>");
					icQtyOrd = icQtyOrd - balQty;
				}//if (icQtyOrd > 0)
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
		return valueXmlString.toString();
	}*///End Comment Gulzar - 18/01/08

	private String stockTransform(Document dom,Document dom1, String objContext, String xtraParams, Document selDataDom)throws ITMException
	{
		Connection conn = null;
		//Statement stmtTemp = null;//Changed and added by Pavan R 10jan19[to handle open cursor issue] End
		PreparedStatement pstmt = null;
		//Statement stmt = null;
		ResultSet rsTemp = null;

		String sqlTemp = "";
		String sordNo = "";	 
		String itemCode = "";
		String itemCodeOrd = "";
		String itemDescr = "";
		String orderedQty = "";
		String siteCodeDet = "";
		String unit1 = "";
		String unitStd1 = "";
		String convQtyStduom = "";
		String itemType = "";
		String netWtUnit = "";
		String rateUnit = "";
		String rateOpt = "";
		String rateUnitSord = "";
		String qtyDetailStr = "";
		String custCode = "";
		String packCode = "";
		String locGroup = "";
		String orderType = "";
		String priceList = "";
		String priceListClg = "";
		String siteCodeMfg = "";
		String suppCodeMfg = "";
		String dimension = "";
		String trackShelfLife = "";
		String taxClass = "";
		String taxChap = "";
		String taxEnv = "";
		String lineNoSord = "";
		String expLevel = "";
		String packInstr = "";
		String lotNo = "";
		String lotSl = "";
		String locCode = "";
		String cQtyStr = "";
		String cAllocQtyStr = "";
		String icQtyOrdStr = "";
		String applyPrice = "";
		String rateStdUomStr = "";

		double inputQty = 0d ;
		double qtyStk = 0d;
		double conv = 0d; 
		double discount =0d ;
		double rateClg = 0d;
		double quantityStduom = 0d;
		double rateStduom = 0d;
		double grossWeight = 0d;
		double tareWeight = 0d;
		double netWeight = 0d;
		double grossWtPerArt = 0d;
		double tareWtPerArt = 0d;
		double qtyPerArt = 0d;
		double palletWt = 0d;
		double grossPer = 0d;
		double netPer = 0d;
		double tarePer = 0d;
		double noArt = 0d;
		double packQty = 0d;
		double convRtuomStd = 0d;
		double netWtPerArt = 0d ;
		double cQty = 0d;
		double cAllocQty = 0d;
		double balQty = 0d;
		double discAmt = 0d;
		double qtyStd = 0d;
		double packGrossWeight = 0d;
		double packNettWeight = 0d;

		int minShelfLife = 0;
		int maxShelfLife = 0;
		double grossWeight1 = 0d,tareWeight1 = 0d,netWeight1 = 0d;//////
		java.sql.Date expDate = null;
		java.sql.Date mfgDate = null;
		String sexpDate = null;
		String smfgDate = null, tempStr = "";

		ArrayList qtyFact = null;
		DistCommon distCommon = new DistCommon();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();

		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		DecimalFormat df = new DecimalFormat("#########.###");

		try
		{
			ConnDriver connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 

			icQtyOrdStr = genericUtility.getColumnValue("quantity", dom);
			double icQtyOrd = 0.0;
			if(icQtyOrdStr != null && icQtyOrdStr.trim().length() > 0)
			{
				icQtyOrd = Double.parseDouble(icQtyOrdStr);
			}

			lineNoSord		= genericUtility.getColumnValue("line_no__sord", dom);
			expLevel		= genericUtility.getColumnValue("exp_lev", dom);
			packInstr		= genericUtility.getColumnValue("pack_instr", dom);
			sordNo			= genericUtility.getColumnValue("sord_no", dom);
			itemCode		= genericUtility.getColumnValue("item_code", dom);
			itemCodeOrd		= genericUtility.getColumnValue("item_code__ord", dom);
			itemDescr		= genericUtility.getColumnValue("item_descr", dom);
			orderedQty		= genericUtility.getColumnValue("quantity__ord", dom);
			siteCodeDet		= genericUtility.getColumnValue("site_code", dom);
			unit1			= genericUtility.getColumnValue("unit", dom);
			unitStd1		= genericUtility.getColumnValue("unit__std", dom);
			convQtyStduom	= genericUtility.getColumnValue("conv__qty_stduom", dom);

			//stmtTemp = conn.createStatement();//Changed by Pavan R 10jan19[to handle open cursor issue] End			
			sqlTemp ="SELECT SORDDET.SALE_ORDER, "   
				+"SORDDET.LINE_NO, "   
				+"SORDDET.SITE_CODE, "   
				+"SORDITEM.ITEM_CODE, " 
				+"SORDDET.ITEM_FLG, "   
				+"SORDITEM.QUANTITY, "  
				+"SORDDET.UNIT, "   
				+"SORDDET.DSP_DATE, "   
				+"SORDDET.RATE, "   
				+"SORDDET.DISCOUNT, "   
				+"SORDDET.TAX_AMT, "   
				+"SORDDET.TAX_CLASS, "   
				+"SORDDET.TAX_CHAP, "   
				+"SORDDET.TAX_ENV, "   
				+"SORDDET.NET_AMT, "   
				+"SORDDET.REMARKS, "   
				+"SORDDET.STATUS, "   
				+"SORDDET.STATUS_DATE, "   
				+"SORDDET.CHG_DATE, "   
				+"SORDDET.CHG_USER, "   
				+"SORDDET.CHG_TERM, "   
				+"SORDDET.ITEM_DESCR, "   
				+"SORDDET.UNIT__RATE, "   
				+"SORDDET.CONV__QTY_STDUOM, "   
				+"SORDDET.CONV__RTUOM_STDUOM, "   
				+"SORDDET.UNIT__STD, "   
				+"SORDDET.QUANTITY__STDUOM, "   
				+"SORDDET.RATE__STDUOM, "   
				+"SORDDET.NO_ART, "   
				+"SORDDET.PACK_CODE, "   
				+"SORDDET.LINE_NO__CONTR, "   
				+"SORDDET.PACK_INSTR, "   
				+"SORDDET.SPEC_REF, "   
				+"SORDDET.PACK_QTY, "   
				+"SORDDET.ITEM_SER, "   
				+"SORDDET.RATE__CLG, "   
				+"SORDDET.MFG_CODE, "   
				+"SORDDET.CONTRACT_NO, "   
				+"PACKING.DESCR, "   
				+"SORDDET.SPEC_ID, "   
				+"SORDDET.ORD_VALUE, "   
				+"SORDDET.ITEM_SER__PROM, "   
				+"SORDDET.SPECIFIC_INSTR, "   
				+"SORDITEM.ITEM_CODE__ORD, "  
				+"SORDDET.PALLET_CODE, "   
				+"SORDDET.NO_PALLET, "   
				+"SORDDET.OVER_SHIP_PERC, "   
				+"SORDDET.COMM_PERC_1, "   
				+"SORDDET.COMM_PERC_2, "   
				+"SORDDET.COMM_PERC_3, "   
				+"SORDDET.COMM_PERC_ON_1, "   
				+"SORDDET.COMM_PERC_ON_2, "   
				+"SORDDET.COMM_PERC_ON_3, "   
				+"SORDDET.SALES_PERS_COMM_1, "   
				+"SORDDET.SALES_PERS_COMM_2, "   
				+"SORDDET.SALES_PERS_COMM_3, "   
				+"SORDDET.PRICE_LIST__DISC, "   
				+"SORDDET.RATE__STD, "   
				+"SORDER.FIN_SCHEME, "   
				+"SORDITEM.MAX_SHELF_LIFE, "  
				+"SORDITEM.MIN_SHELF_LIFE, "   
				+"SPACE(250) AS ITEM_SPECS, "   
				+"SPACE(250) AS ST_SHRINK, "   
				+"SORDDET.LOC_TYPE, "   
				+"SPACE(250) AS ST_SCHEME,  "
				+"SORDDET.SORDFORM_NO ,  "  
				+"SORDDET.LINE_NO__SFORM ,  "  ;

			//changed by manish on 10/10/15 for Ms sql server databse [start]
			if("mssql".equalsIgnoreCase(DB))
			{
				sqlTemp = sqlTemp + "dbo.FN_GET_ITMSTK(SORDDET.ITEM_CODE,SORDDET.SITE_CODE) STK_QTY,  "  ;
			}
			else
			{
				sqlTemp = sqlTemp + "FN_GET_ITMSTK(SORDDET.ITEM_CODE,SORDDET.SITE_CODE) STK_QTY,  "  ;
			}
			//changed by manish on 10/10/15 for Ms sql server databse [end]

			sqlTemp = sqlTemp + "SORDDET.APPL_SEG ,  "  
			+"UOM.DESCR,  "
			+"SORDITEM.EXP_LEV, " 
			+"SORDITEM.QTY_ALLOC,  " 
			+"SORDER.LOC_GROUP, "
			+"SORDER.ORDER_TYPE, "
			+"SORDER.CUST_CODE, "
			+"SORDER.PRICE_LIST, "
			+"SORDER.PRICE_LIST__CLG "
			+"FROM SORDDET LEFT OUTER JOIN PACKING ON SORDDET.PACK_CODE = PACKING.PACK_CODE LEFT OUTER JOIN UOM ON SORDDET.UNIT = UOM.UNIT,  "
			+"SORDER, "
			+"SORDITEM  "
			+"WHERE SORDER.SALE_ORDER = SORDDET.SALE_ORDER "
			+"AND SORDITEM.SALE_ORDER = SORDDET.SALE_ORDER "
			+"AND SORDITEM.LINE_NO = SORDDET.LINE_NO "
			+"AND SORDITEM.SALE_ORDER = ?" //'"+sordNo+"' "
			+"AND SORDDET.LINE_NO = ?" //'" + lineNoSord + "' "
			+"AND SORDITEM.LINE_TYPE = 'I' ORDER BY SORDITEM.LINE_NO , SORDITEM.EXP_LEV";
			//Changed and added by Pavan R 10jan19[to handle open cursor issue]
			//System.out.println("sql1 :"+sqlTemp);
			//rsTemp = stmtTemp.executeQuery(sqlTemp);
			pstmt = conn.prepareStatement(sqlTemp);
			pstmt.setString(1, sordNo);
			pstmt.setString(2, lineNoSord);
			rsTemp = pstmt.executeQuery();
			if (rsTemp.next())
			{
				convRtuomStd	= rsTemp.getDouble("CONV__RTUOM_STDUOM"); 
				rateUnitSord	= rsTemp.getString("UNIT__RATE");
				quantityStduom	= rsTemp.getDouble("QUANTITY__STDUOM");
				rateStduom		= rsTemp.getDouble("RATE__STDUOM");
				custCode		= rsTemp.getString("CUST_CODE");
				packCode		= rsTemp.getString("PACK_CODE");
				convRtuomStd	= rsTemp.getDouble("CONV__RTUOM_STDUOM"); 
				rateUnitSord	= rsTemp.getString("UNIT__RATE");
				locGroup		= rsTemp.getString("LOC_GROUP");
				orderType		= rsTemp.getString("ORDER_TYPE");
				minShelfLife	= rsTemp.getInt("MIN_SHELF_LIFE");
				maxShelfLife	= rsTemp.getInt("MAX_SHELF_LIFE");
				priceList		= rsTemp.getString("PRICE_LIST");
				priceListClg	= rsTemp.getString("PRICE_LIST__CLG");

				if (rateUnitSord == null)
				{
					rateUnitSord = "";
				}
				discount	= rsTemp.getDouble("DISCOUNT"); 
				rateClg		= rsTemp.getDouble("RATE__CLG");
				taxClass	= rsTemp.getString("TAX_CLASS");
				taxChap		= rsTemp.getString("TAX_CHAP"); 
				taxEnv		= rsTemp.getString("TAX_ENV"); 
			}
			rsTemp.close();
			rsTemp = null;
			pstmt.close();pstmt = null;//Added by Pavan R 10jan19[to handle open cursor issue]
			sqlTemp = "SELECT APPLY_PRICE  FROM BOM WHERE BOM_CODE = ?";//'"+ itemCode +"' ";
			//System.out.println("sql1 :"+sqlTemp)//Changed and added by Pavan R 10jan19[to handle open cursor issue]
			//rsTemp = stmtTemp.executeQuery(sqlTemp);
			pstmt = conn.prepareStatement(sqlTemp);
			pstmt.setString(1, itemCode);
			rsTemp = pstmt.executeQuery();
			if ( rsTemp.next() )
			{
				applyPrice = rsTemp.getString("APPLY_PRICE");
			}
			//stmtTemp.close();
			//stmtTemp = null;
			rsTemp.close();
			rsTemp = null;
			pstmt.close();pstmt = null;//Changed and added by Pavan R 10jan19[to handle open cursor issue]
			/*  chandni shah  - 02/02/11
			if ( applyPrice == null )
			{
				discAmt = (discount/100) * ( quantityStduom *  rateStduom ); 
			}
			System.out.println("discAmt :"+discAmt);
			 */
			if ( packCode != null && packCode.trim().length() > 0 )
			{
				//Changed and added by Pavan R 10jan19[to handle open cursor issue] start
				sqlTemp = "SELECT GROSS_WEIGHT, NETT_WEIGHT FROM PACKING WHERE PACK_CODE = ?";//'"+ packCode +"' ";
				//System.out.println("sqlTemp :: "+sqlTemp);
				//stmtTemp = conn.createStatement();
				//rsTemp = stmtTemp.executeQuery(sqlTemp);
				pstmt = conn.prepareStatement(sqlTemp);
				pstmt.setString(1, packCode);
				rsTemp = pstmt.executeQuery();				
				if ( rsTemp.next() )
				{
					packGrossWeight = rsTemp.getDouble("GROSS_WEIGHT");
					packNettWeight	= rsTemp.getDouble("NETT_WEIGHT");
				}
				//stmtTemp.close();
				//stmtTemp = null;
				rsTemp.close();
				rsTemp = null;
				pstmt.close();pstmt = null;
				//Changed and added by Pavan R 10jan19[to handle open cursor issue] End
			}

			NodeList detailList = selDataDom.getElementsByTagName("Detail");
			int noOfDetails = detailList.getLength();
			for(int ctr = 0; ctr < noOfDetails; ctr++)
			{
				if (icQtyOrd > 0)
				{
					Node currDetail = detailList.item(ctr);

					lotNo			= genericUtility.getColumnValueFromNode("lot_no", currDetail);
					lotSl			= genericUtility.getColumnValueFromNode("lot_sl", currDetail);
					locCode			= genericUtility.getColumnValueFromNode("loc_code", currDetail);
					cQtyStr			= genericUtility.getColumnValueFromNode("quantity", currDetail);
					cAllocQtyStr	= genericUtility.getColumnValueFromNode("alloc_qty", currDetail);

					System.out.println("Lot No In stockTransform :: " + lotNo);
					System.out.println("Lot Sl In stockTransform :: " + lotSl);
					System.out.println("Location Code In stockTransform In Curr Detail :: " + locCode);
					System.out.println("Alloc Quantity [cAllocQtyStr] : In stockTransform In Curr Detail Before Parsing:: " + cAllocQtyStr);

					if((lotSl == null) || (lotSl != null && lotSl.trim().length() == 0))
					{
						lotSl = "     ";
					}
					if((lotNo == null) || (lotNo != null && lotNo.trim().length() == 0))
					{
						lotNo = "               ";
					}

					if(cQtyStr != null && cQtyStr.trim().length() > 0)
					{
						cQty = Double.parseDouble(cQtyStr);
					}

					if(cAllocQtyStr != null && cAllocQtyStr.trim().length() > 0)
					{
						cAllocQty = Double.parseDouble(cAllocQtyStr);
					}

					balQty = cQty - cAllocQty;

					if (balQty > icQtyOrd)
					{
						balQty  = icQtyOrd;
					}
					sqlTemp ="SELECT A.EXP_DATE, " 
						+"A.QUANTITY, "
						+"A.SITE_CODE__MFG, "
						+"A.MFG_DATE, "
						+"A.PACK_CODE, " 
						+"A.GROSS_WEIGHT, "
						+"A.TARE_WEIGHT, "
						+"A.NET_WEIGHT, " 
						+"A.DIMENSION, "
						+"A.SUPP_CODE__MFG, " 
						+"A.QTY_PER_ART, "
						+"A.GROSS_WT_PER_ART, " 
						+"A.TARE_WT_PER_ART ,"  
						+"A.PALLET_WT  "
						+"FROM STOCK A "  //Changed and added by Pavan R 10jan19[to handle open cursor issue] start
						+"WHERE A.ITEM_CODE = ?" //'"+itemCode+"' "  
						+"AND A.SITE_CODE = ?"   //'"+siteCodeDet+"' "  
						+"AND A.LOC_CODE = ? "   //'"+locCode+"' "  
						+"AND A.LOT_NO = ? "     //'"+lotNo+"' "   
						+"AND A.LOT_SL = ? " ;    //'"+lotSl+"' ";   
					//System.out.println("sql2 :"+sqlTemp);
					//stmtTemp = conn.createStatement();
					//rsTemp = stmtTemp.executeQuery(sqlTemp);
					pstmt = conn.prepareStatement(sqlTemp);
					pstmt.setString(1, itemCode);
					pstmt.setString(2, siteCodeDet);
					pstmt.setString(3, locCode);
					pstmt.setString(4, lotNo);
					pstmt.setString(5, lotSl);
					rsTemp = pstmt.executeQuery();
					//Changed and added by Pavan R 10jan19[to handle open cursor issue] End
					if (rsTemp.next())
					{
						expDate			= rsTemp.getDate("EXP_DATE");
						qtyStk			= rsTemp.getDouble("QUANTITY");
						mfgDate			= rsTemp.getDate("MFG_DATE");
						siteCodeMfg		= rsTemp.getString("SITE_CODE__MFG");
						grossWeight		= rsTemp.getDouble("GROSS_WEIGHT");
						tareWeight		= rsTemp.getDouble("TARE_WEIGHT");
						netWeight		= rsTemp.getDouble("NET_WEIGHT");
						suppCodeMfg		= rsTemp.getString("SUPP_CODE__MFG");
						dimension		= rsTemp.getString("DIMENSION");
						grossWtPerArt	= rsTemp.getDouble("GROSS_WT_PER_ART");
						tareWtPerArt	= rsTemp.getDouble("TARE_WT_PER_ART");
						qtyPerArt		= rsTemp.getDouble("QTY_PER_ART");
						palletWt		= rsTemp.getDouble("PALLET_WT");
					}
					rsTemp.close();
					rsTemp = null;
					//Changed and added by Pavan R 10jan19[to handle open cursor issue] start
					pstmt.close();pstmt = null;
					sqlTemp ="SELECT (CASE WHEN TRACK_SHELF_LIFE IS NULL THEN 'N' ELSE TRACK_SHELF_LIFE END) AS TRACK_SHELF_LIFE , DESCR " 
						+"FROM ITEM WHERE ITEM_CODE = ? ";  // '"+itemCode+"' "; 
					//System.out.println("sql3  :"+sqlTemp);
					//rsTemp = stmtTemp.executeQuery(sqlTemp);
					pstmt = conn.prepareStatement(sqlTemp);
					pstmt.setString(1, itemCode);
					rsTemp = pstmt.executeQuery();
					//Changed and added by Pavan R 10jan19[to handle open cursor issue]End
					if (rsTemp.next())
					{
						trackShelfLife = rsTemp.getString("TRACK_SHELF_LIFE");
						System.out.println("trackShelfLife :"+trackShelfLife);
					}
					rsTemp.close();
					rsTemp = null;
					pstmt.close();pstmt = null;
					//start change 10-08-2010 
					System.out.println("palletWt.."+palletWt);
					System.out.println("qtyStk.."+qtyStk+"..grossWeight.."+grossWeight+"..grossWtPerArt.."+grossWtPerArt);
					System.out.println("netWeight.."+netWeight+"..tareWeight.."+tareWeight+"..tareWtPerArt.."+tareWtPerArt);
					if (qtyStk > balQty)
					{
						qtyStk = balQty;
					}
					if (qtyStk > 0)
					{
						grossPer = (grossWeight / qtyStk) ;
						grossPer = df.parse(df.format(grossPer)).doubleValue();
						System.out.println("grossPer :"+grossPer);
						netPer 	=  (netWeight / qtyStk);
						netPer = df.parse(df.format(netPer)).doubleValue();
						System.out.println("netPer 	:"+netPer);
						tarePer	=  (tareWeight / qtyStk);
						tarePer = df.parse(df.format(tarePer)).doubleValue();
						System.out.println("tarePer	:"+tarePer);									
						grossWeight1 = (balQty * grossPer);
						System.out.println("grossWeight1 :"+grossWeight1);
						netWeight1 = (balQty * netPer);
						System.out.println("netWeight1 :"+netWeight1);
						tareWeight1 = (balQty * tarePer);
						System.out.println("tareWeight1 :"+tareWeight1);	

						grossWeight1 = df.parse(df.format(grossWeight1)).doubleValue(); 	
						netWeight1	= df.parse(df.format(netWeight1)).doubleValue(); 	
						tareWeight1	= df.parse(df.format(tareWeight1)).doubleValue(); 
					}

					//end change 10-08-2010 
					/*-- Commented - and Change below - Gulzar - 31/01/08 - The Calculation done as per case "lot_no of nvo_bo_despatch
					if (qtyStk > 0)
					{
						grossPer = (grossWeight / qtyStk) ;
						grossPer = df.parse(df.format(grossPer)).doubleValue();
						System.out.println("grossPer :"+grossPer);
						netPer 	=  (netWeight / qtyStk);
						netPer = df.parse(df.format(netPer)).doubleValue();
						System.out.println("netPer 	:"+netPer);
						tarePer	=  (tareWeight / qtyStk);
						tarePer = df.parse(df.format(tarePer)).doubleValue();
						System.out.println("tarePer	:"+tarePer);									
						grossWeight = (balQty * grossPer);
						System.out.println("grossWeight :"+grossWeight);
						netWeight = (balQty * netPer);
						System.out.println("netWeight :"+netWeight);
						tareWeight = (balQty * tarePer);
						System.out.println("tareWeight :"+tareWeight);	
					}*/
					//End Comment - Gulzar - 31/01/08
					netWtPerArt = convRtuomStd;
					//Changed and added by Pavan R 10jan19[to handle open cursor issue] Start
					sqlTemp = "SELECT ITEM_TYPE, UNIT__NETWT, UNIT__RATE FROM ITEM WHERE ITEM_CODE = ?";  //'" + itemCodeOrd + "'";
					//System.out.println("sql4 :"+sqlTemp);
					//rsTemp = stmtTemp.executeQuery(sqlTemp);
					pstmt = conn.prepareStatement(sqlTemp);
					pstmt.setString(1, itemCodeOrd);
					rsTemp = pstmt.executeQuery();
					//Changed and added by Pavan R 10jan19[to handle open cursor issue] End
					if (rsTemp.next())
					{
						itemType = rsTemp.getString("ITEM_TYPE");
						if (itemType == null)
						{
							itemType = "";
						}
						netWtUnit = rsTemp.getString("UNIT__NETWT");
						if (netWtUnit == null)
						{
							netWtUnit = "";
						}
						rateUnit = rsTemp.getString("UNIT__RATE");
						if (rateUnit == null)
						{
							rateUnit = "";
						}
					}
					rsTemp.close();
					rsTemp = null;
					pstmt.close();pstmt = null;//Added by Pavan R 10jan19[to handle open cursor issue]
					if (itemType.trim().length() > 0)
					{
						//Changed and added by Pavan R 10jan19[to handle open cursor issue] Start
						sqlTemp = "SELECT RATE_OPT FROM ITEM_TYPE WHERE ITEM_TYPE = ?";   //'" + itemType + "'" ;
						//System.out.println("sql5 :"+sqlTemp);
						//rsTemp = stmtTemp.executeQuery(sqlTemp);
						pstmt = conn.prepareStatement(sqlTemp);
						pstmt.setString(1, itemType);
						rsTemp = pstmt.executeQuery();
						//Changed and added by Pavan R 10jan19[to handle open cursor issue] End
						if (rsTemp.next())
						{
							rateOpt = rsTemp.getString("RATE_OPT");
							if (rateOpt == null)
							{
								rateOpt = "";
							}
						}
						rsTemp.close();
						rsTemp = null;
						pstmt.close();pstmt = null;//Added by Pavan R 10jan19[to handle open cursor issue]
						if (rateOpt == "1")
						{
							if ( netWtUnit.trim().equals(rateUnit.trim()))
							{
								if (!unit1.trim().equals(rateUnitSord.trim()))
								{
									netWtPerArt = grossWtPerArt - tareWtPerArt;
								}
							}
						}
					}

					double shipperQtyNew = 0d;
					double integralQtyNew = 0d;
					double looseQty = 0d;
					double[] noArtInfo1 = getNoArt(siteCodeDet , custCode , itemCode , packCode , balQty , "S" , 0 , 0 , conn); 
					double balQty1 = balQty - ( shipperQtyNew * noArtInfo1[0]);
					double[] noArtInfo2 = getNoArt(siteCodeDet , custCode , itemCode , packCode , balQty1 , "I" , 0 , 0 , conn); 
					integralQtyNew = noArtInfo2[2];
					shipperQtyNew = shipperQtyNew * noArtInfo1[0];
					integralQtyNew = integralQtyNew * noArtInfo2[0];
					looseQty = balQty - ( shipperQtyNew + integralQtyNew );

					qtyDetailStr = "Shipper Quantity = " + shipperQtyNew +"  Integral Quantity = " + integralQtyNew + "  Loose Quantity = " + looseQty ;
					qtyFact = null;
					qtyFact = new ArrayList();
					if (!unit1.equals(unitStd1))
					{
						//qtyFact = distCommon.getConvQuantityFact(unitStd1, unit1, itemCode, balQty, conv, conn);
						//qtyFact = distCommon.getConvQuantityFact(unitStd1, unit1, itemCode, qtyStk, conv, conn);
						qtyFact = distCommon.getConvQuantityFact(unit1, unitStd1, itemCode, qtyStk, conv, conn);//Change by chandrashekar as per manoharan sir instruction
						System.out.println("qtyFact.get(1) :"+qtyFact.get(0));
					}
					else
					{
						qtyFact.add(Integer.toString(1));
						//qtyFact.add(Double.toString(balQty));
						qtyFact.add(Double.toString(qtyStk));


					}
					inputQty = Double.parseDouble(qtyFact.get(1).toString());
					System.out.println("manohar 20/09/10 inputQty ["+inputQty + "] qtyStk [" + qtyStk + "] balQty [ " + balQty + "]");
					System.out.println("qtyPerArt::::::::::::: "+qtyPerArt);
					if ( qtyPerArt > 0 )
					{
						inputQty = Double.parseDouble(qtyFact.get(1).toString());
						System.out.println("inputQty::::::::::::: "+inputQty);
						noArt = new Double((Double.parseDouble(qtyFact.get(1).toString())) / qtyPerArt).intValue();
						System.out.println("noArt [qtyFact.get(1).toString())) / qtyPerArt)] ::"+noArt);
						grossWeight = (df.parse(df.format(grossWtPerArt / qtyPerArt)).doubleValue()) * qtyStk;
						tareWeight	= (df.parse(df.format(tareWtPerArt / qtyPerArt)).doubleValue()) * qtyStk;
						netWeight	= df.parse(df.format(grossWeight - tareWeight)).doubleValue();
						System.out.println("grossWeight [if ( qtyPerArt > 0 )] ::"+grossWeight);
						System.out.println("tareWeight [if ( qtyPerArt > 0 )] ::"+tareWeight);
						System.out.println("netWeight [if ( qtyPerArt > 0 )] ::"+netWeight);
					}
					/* chandni shah  - 02/02/11
					if ( applyPrice == null )
					{
						discAmt = (discount/100) * ( inputQty *  rateStduom ); 
					}
					System.out.println("discAmt :"+discAmt);
					 */
					
					//Added by Pavan R on 17/JAN/18[start]to set taxchap in manual despatch
					String sql="",mVal="",mcode="",mitemflg="",mVal1="",nature="",lstaxchapsoitem="";
					mVal1 = genericUtility.getColumnValue("exp_lev",dom);
					mcode = genericUtility.getColumnValue("line_no__sord",dom);///genericUtility.
					mVal = genericUtility.getColumnValue("sord_no",dom);
					System.out.println("exp_lev CASE....:"+mVal1+"..."+mcode+"..."+mVal);
					//PreparedStatement pstmt=null; 
					ResultSet rs=null;
					
					sql = "Select item_flg from sorddet "
							+"where sale_order = ? and line_no = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,mVal);
						pstmt.setString(2,mcode);

						rs = pstmt.executeQuery();
						while(rs.next())
						{	
							mitemflg = rs.getString("item_flg");
							
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;						

						sql = "Select  nature, tax_chap from sorditem "
							+" where sale_order = ? and line_no = ? and "
							+" exp_lev = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,mVal);
						pstmt.setString(2,mcode);						
						pstmt.setString(3,mVal1);

						rs = pstmt.executeQuery();
						if(rs.next())
						{	
							nature=rs.getString("nature");
							lstaxchapsoitem = rs.getString("tax_chap");
							
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if( "B".equals(mitemflg) &&  "F".equals(nature) )
						{
							System.out.println("1518::Match Item Flag");
							taxChap = lstaxchapsoitem;
						}
						System.out.println("taxChap["+taxChap+"]");

					//Pavan R[End]
					
					if (noArt == 0)
					{
						noArt = 1;
					}
					if (noArt > 0)
					{
						packQty = 0d;
						packQty = (Double.parseDouble(qtyFact.get(1).toString())) / noArt;
						packQty = df.parse(df.format(packQty)).doubleValue(); 
					}
					if ( grossWeight == 0 ) //Gulzar - 25/01/08
					{
						grossWeight = packGrossWeight * noArt;
						netWeight = packNettWeight * noArt;
						tareWeight = grossWeight - netWeight;
					}

					grossWeight = df.parse(df.format(grossWeight)).doubleValue(); 	
					netWeight	= df.parse(df.format(netWeight)).doubleValue(); 	
					tareWeight	= df.parse(df.format(tareWeight)).doubleValue(); 	
					balQty		= df.parse(df.format(balQty)).doubleValue(); 				
					icQtyOrd	= df.parse(df.format(icQtyOrd)).doubleValue(); 	

					valueXmlString.append("<Detail>");
					valueXmlString.append("<sord_no isSrvCallOnChg='0'>").append("<![CDATA[").append(sordNo).append("]]>").append("</sord_no>\r\n"); 
					setNodeValue( dom, "sord_no", (sordNo == null) ? "":sordNo );
					valueXmlString.append("<line_no__sord isSrvCallOnChg='0'>").append(lineNoSord).append("</line_no__sord>"); //chg1
					setNodeValue( dom, "line_no__sord", (lineNoSord == null) ? "":lineNoSord );
					tempStr = getChangeSord(dom,  dom1, "line_no__sord", xtraParams ,conn);
					valueXmlString.append(tempStr);  /// 16-08
					//lineNoSord = getChangeSord(dom,  dom1, "line_no__sord", xtraParams ,conn);
					//valueXmlString.append(lineNoSord);  /// 16-08
					valueXmlString.append("<exp_lev isSrvCallOnChg='0'>").append((expLevel == null ? "":expLevel)).append("</exp_lev>");
					setNodeValue( dom, "exp_lev", (expLevel == null ? "":expLevel) );
					tempStr = getChangeSord(dom,  dom1, "exp_lev", xtraParams ,conn);
					valueXmlString.append(tempStr);

					valueXmlString.append("<item_code isSrvCallOnChg='0'>").append("<![CDATA[").append(itemCode.trim()).append("]]>").append("</item_code>\r\n");
					setNodeValue( dom, "item_code", (itemCode == null) ? "":itemCode );
					valueXmlString.append("<item_code__ord isSrvCallOnChg='0'>").append("<![CDATA[").append(itemCodeOrd).append("]]>").append("</item_code__ord>\r\n");
					setNodeValue( dom, "item_code__ord", (itemCodeOrd == null) ? "":itemCodeOrd );
					valueXmlString.append("<item_descr isSrvCallOnChg='0'>").append("<![CDATA[").append(itemDescr).append("]]>").append("</item_descr>\r\n");
					setNodeValue( dom, "item_descr", (itemDescr == null) ? "":itemDescr );
					valueXmlString.append("<quantity__ord isSrvCallOnChg='0'>").append("<![CDATA[").append(orderedQty).append("]]>").append("</quantity__ord>\r\n");
					setNodeValue( dom, "quantity__ord", orderedQty);
					valueXmlString.append("<site_code isSrvCallOnChg='0'>").append("<![CDATA[").append(siteCodeDet).append("]]>").append("</site_code>\r\n");
					setNodeValue( dom, "site_code", (siteCodeDet == null) ? "":siteCodeDet );
					valueXmlString.append("<quantity_real isSrvCallOnChg='0'>").append("<![CDATA[").append(balQty).append("]]>").append("</quantity_real>\r\n");
					setNodeValue( dom, "quantity_real", balQty );
					valueXmlString.append("<unit isSrvCallOnChg='0'>").append("<![CDATA[").append(unit1).append("]]>").append("</unit>\r\n");
					setNodeValue( dom, "unit", unit1 );
					valueXmlString.append("<unit__std isSrvCallOnChg='0'>").append("<![CDATA[").append(unitStd1).append("]]>").append("</unit__std>\r\n");
					setNodeValue( dom, "unit__std", unitStd1 );
					valueXmlString.append("<conv__qty_stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(convQtyStduom).append("]]>").append("</conv__qty_stduom>\r\n");
					setNodeValue( dom, "conv__qty_stduom", convQtyStduom );
					valueXmlString.append("<conv__rtuom_stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(netWtPerArt).append("]]>").append("</conv__rtuom_stduom>\r\n"); 
					setNodeValue( dom, "conv__rtuom_stduom", netWtPerArt );
					valueXmlString.append("<pallet_wt isSrvCallOnChg='0'>").append("<![CDATA[").append(palletWt).append("]]>").append("</pallet_wt>\r\n");
					//loc_code isSrvCallOnChg='0' to loc_code isSrvCallOnChg='1' by msalam on 051108 as rate__clg was not coming
					valueXmlString.append("<loc_code isSrvCallOnChg='0'>").append(locCode).append("</loc_code>");
					setNodeValue( dom, "loc_code", (locCode == null) ? "" : locCode );
					System.out.println("28/08/10 packQty 1 [" + packQty + "]");
					valueXmlString.append("<pack_qty isSrvCallOnChg='0'>").append("<![CDATA[").append(packQty).append("]]>").append("</pack_qty>\r\n"); 
					//valueXmlString.append("<quantity__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(balQty).append("]]>").append("</quantity__stduom>\r\n");
					valueXmlString.append("<quantity__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(inputQty).append("]]>").append("</quantity__stduom>\r\n");

					valueXmlString.append("<qty_details isSrvCallOnChg='0'>").append("<![CDATA[").append(qtyDetailStr).append("]]>").append("</qty_details>\r\n");  
					valueXmlString.append("<discount isSrvCallOnChg='0'>").append("<![CDATA[").append(discount).append("]]>").append("</discount>\r\n");
					/* commented on 03/02/11 - Chandni Shah
					valueXmlString.append("<disc_amt isSrvCallOnChg='0'>").append("<![CDATA[").append(discAmt).append("]]>").append("</disc_amt>\r\n");
					 */
					//System.out.println("kunal test 1382..");
					//comment <pending_qty> by kunal on 16/jan/13 pending_qty set in exp_lev itemchange     
					//valueXmlString.append("<pending_qty isSrvCallOnChg='0'>").append(icQtyOrd).append("</pending_qty>");
					//valueXmlString.append("<gross_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(grossWeight).append("]]>").append("</gross_weight>\r\n");
					//valueXmlString.append("<nett_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(netWeight).append("]]>").append("</nett_weight>\r\n");
					//valueXmlString.append("<tare_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(tareWeight).append("]]>").append("</tare_weight>\r\n");
					//start change 10-08-2010 
					valueXmlString.append("<gross_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(grossWeight1).append("]]>").append("</gross_weight>\r\n");
					valueXmlString.append("<nett_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(netWeight1).append("]]>").append("</nett_weight>\r\n");
					valueXmlString.append("<tare_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(tareWeight1).append("]]>").append("</tare_weight>\r\n");
					//end change 10-08-2010 
					valueXmlString.append("<no_art isSrvCallOnChg='0'>").append("<![CDATA[").append(noArt).append("]]>").append("</no_art>\r\n");
					valueXmlString.append("<dimension isSrvCallOnChg='0'>").append("<![CDATA[").append((dimension == null) ? "":dimension).append("]]>").append("</dimension>\r\n"); 
					valueXmlString.append("<site_code__mfg isSrvCallOnChg='0'>").append("<![CDATA[").append(siteCodeMfg).append("]]>").append("</site_code__mfg>\r\n");
					//valueXmlString.append("<rate__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(rateStduom).append("]]>").append("</rate__stduom>\r\n"); 
					//next line commented as it gets set from lot no item change on 041108 by msalam
					//valueXmlString.append("<rate__clg isSrvCallOnChg='0'>").append("<![CDATA[").append(rateClg).append("]]>").append("</rate__clg>\r\n"); 
					valueXmlString.append("<lot_sl isSrvCallOnChg='0'>").append(lotSl).append("</lot_sl>");
					setNodeValue( dom, "lot_sl", (lotSl == null) ? "":lotSl );
					valueXmlString.append("<lot_no isSrvCallOnChg='0'>").append(lotNo).append("</lot_no>");//chg2
					setNodeValue( dom, "lot_no", (lotNo == null) ? "":lotNo );

					//valueXmlString.append("<quantity isSrvCallOnChg='0'>").append(balQty).append("</quantity>");
					//setNodeValue( dom, "quantity", balQty );
					valueXmlString.append("<quantity isSrvCallOnChg='0'>").append(qtyStk).append("</quantity>");
					setNodeValue( dom, "quantity", qtyStk );
					valueXmlString.append("<tax_class isSrvCallOnChg='0'>").append("<![CDATA[").append((taxClass == null) ?"":taxClass).append("]]>").append("</tax_class>\r\n");
					valueXmlString.append("<tax_chap isSrvCallOnChg='0'>").append("<![CDATA[").append((taxChap == null) ?"":taxChap).append("]]>").append("</tax_chap>\r\n"); 
					valueXmlString.append("<tax_env isSrvCallOnChg='0'>").append("<![CDATA[").append((taxEnv == null) ?"":taxEnv).append("]]>").append("</tax_env>\r\n"); 
					//lotNo = getChangeSord(dom,  dom1, "lot_no", xtraParams ,conn);
					//valueXmlString.append(lotNo);  ///// 16-08
					// 27/06/09 manoharan set the mfg_date and exp_date from stock
					SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());	
					if (expDate != null)
					{
						sexpDate = sdf.format(expDate).toString();
					}
					else
					{
						sexpDate = "";
					}
					valueXmlString.append("<exp_date isSrvCallOnChg='0'>").append(sexpDate).append("</exp_date>");
					if (mfgDate != null)
					{
						smfgDate = sdf.format(mfgDate).toString();
					}
					else
					{
						smfgDate = "";
					}
					valueXmlString.append("<mfg_date isSrvCallOnChg='0'>").append(smfgDate).append("</mfg_date>");
					//lineNoSord = getChangeSord(dom,  dom1, "line_no__sord", xtraParams ,conn);
					//valueXmlString.append(lineNoSord);  /// 16-08
					tempStr = getChangeSord(dom,  dom1, "lot_no", xtraParams ,conn);

					valueXmlString.append(tempStr);  ///// 16-08
					// chandni shah  - 02/02/11
					rateStdUomStr = genericUtility.getColumnValue("rate__stduom", dom);
					if (rateStdUomStr == null || "null".equals(rateStdUomStr) || rateStdUomStr.trim().length() == 0)
					{
						rateStdUomStr = "0";
					}
					rateStduom = Double.parseDouble(rateStdUomStr);
					if ( applyPrice == null )
					{
						discAmt = (discount/100) * ( inputQty *  rateStduom ); 
					}
					System.out.println("discount from stockTransform 1 ::::"+discount);
					System.out.println("inputQty from stockTransform 1 ::::"+inputQty);
					System.out.println("rateStduom from stockTransform 1 ::::"+rateStduom);
					System.out.println("discAmt from stockTransform 1 ::::"+discAmt);


					valueXmlString.append("<disc_amt isSrvCallOnChg='0'>").append("<![CDATA[").append(discAmt).append("]]>").append("</disc_amt>\r\n");
					setNodeValue( dom, "disc_amt", discAmt );
					////////

					//

					// end 27/06/09 manoharan set the mfg_date and exp_date from stock
					valueXmlString.append("<quantity isSrvCallOnChg='0'>").append(qtyStk).append("</quantity>");
					setNodeValue( dom, "quantity", qtyStk );
					valueXmlString.append("<quantity__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(inputQty).append("]]>").append("</quantity__stduom>\r\n");
					setNodeValue( dom, "quantity__stduom", inputQty );

					//String retXmlString = serializeDom(dom);
					System.out.println("manohar 07/02/11 serializeDom 1 [ " + serializeDom(dom) + "]");

					tempStr = getChangeSord(dom,  dom1, "quantity", xtraParams ,conn);//chg3
					valueXmlString.append(tempStr);


					valueXmlString.append("<quantity_real isSrvCallOnChg='0'>").append("<![CDATA[").append(qtyStk).append("]]>").append("</quantity_real>\r\n");
					setNodeValue( dom, "quantity_real", qtyStk );

					valueXmlString.append("</Detail>");
					/*
					valueXmlString.append("<line_no__sord isSrvCallOnChg='1'>").append(lineNoSord).append("</line_no__sord>");
					valueXmlString.append("<exp_lev isSrvCallOnChg='1'>").append(expLevel).append("</exp_lev>");
					valueXmlString.append("<loc_code>").append(locCode).append("</loc_code>");
					valueXmlString.append("<lot_no>").append(lotNo).append("</lot_no>");
					valueXmlString.append("<lot_sl isSrvCallOnChg='1'>").append(lotSl).append("</lot_sl>");
					valueXmlString.append("<quantity isSrvCallOnChg='1'>").append(balQty).append("</quantity>");
					valueXmlString.append("<pending_qty isSrvCallOnChg='0'>").append(icQtyOrd).append("</pending_qty>");
					valueXmlString.append("</Detail>");
					 */
					//icQtyOrd = icQtyOrd - balQty;
					icQtyOrd = icQtyOrd - qtyStk;

				}//if (icQtyOrd > 0)
			}
			valueXmlString.append("</Root>");
			String retXmlString = serializeDom(genericUtility.parseString(valueXmlString.toString()));
			valueXmlString = null;
			System.out.println("manohar 07/02/11 retXmlString 5 ["+ retXmlString +"]");
			valueXmlString =  new StringBuffer(retXmlString);
		}
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
				System.out.println("Closing Connection................");
				/*if (stmtTemp != null)
				{
					stmtTemp.close();
					stmtTemp = null;
				}*/
				if(rsTemp != null)
				{
					rsTemp.close();
					rsTemp = null;
				}
				if (pstmt!=null)
				{
					pstmt.close();
					pstmt=null;
				}
				if (conn != null)
				{
					conn.close();
					conn = null;
				}
			}
			catch(Exception e)
			{
				throw new ITMException(e);
			}
		}
		System.out.println("Print valueXmlString.toString() \n["+valueXmlString.toString()+"]");
		return valueXmlString.toString();
	}

	/*--- Commented And Changes Below For Making as Service Handler 4 - Gulzar - 23-04-07
	private String actionGetPackList(Document dom, Document dom1, String objContext, String xtraParams) throws RemoteException, ITMException
	{
		String saleOrder = "",despId = "",sqlOrderNoSo = "",sqlOrderNoPo = "",sqlOrderNoDo = "";
		String orderNo = "",orderNoSo = "",orderNoPo = "",orderNoDo = "",invOrderNo = "";		
		String sql1 = "", sql = "";
		String orderType = "",spaces = "",expLev = "",saleOrd = "";
		String taxClass = "", taxChap = "", taxEnv = "", unit = "",unitStd = "";  
		String lineNoOrd ="", itemCode = "", locCode = "", lotNo = "", lotSL = "", packInstr  = "", dimension = "";   
		double quantity = 0, grossWeight = 0, tareWeight = 0, netWeight = 0, conv = 0, qtyStd = 0; 
		double detQty = 0;
		long   noArt = 0;
		ArrayList qty = new ArrayList();
		ArrayList orderNoArrList = new ArrayList();
		ResultSet rs = null;
		String detailCnt = "0";
		ResultSet rs1 = null;
		Connection conn = null;
		Statement stmt = null;
		Statement stmt1 = null;
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		String origInvOrderNo = "",sorder = "N";
		try
		{
			//detailCnt = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"detCnt");
			//if(detailCnt.equals("0"))
			//{
				conn = connDriver.getConnectDB("DriverITM");
				stmt = conn.createStatement();
				stmt1 = conn.createStatement();

				saleOrder = genericUtility.getColumnValue("sord_no",dom);
				despId = genericUtility.getColumnValue("desp_id",dom1);
				System.out.println("saleOrder :"+saleOrder);
				System.out.println("despId :"+despId);

				sqlOrderNoSo = "SELECT ORDER_NO FROM INV_PACK WHERE ORDER_NO ='"+saleOrder+"'"+" AND "
							  +"ORDER_TYPE IN ('S','I') AND CONFIRMED ='Y'";
				System.out.println("sqlOrderNoSo :"+sqlOrderNoSo);
				rs = stmt.executeQuery(sqlOrderNoSo);
				while (rs.next())
				{
					orderNoSo = rs.getString(1);
					if(orderNoSo != null){
						orderNoArrList.add(orderNoSo);
					}
					System.out.println("orderNoSo :"+orderNoSo);
				}
				stmt.close();
				stmt = null;
				sqlOrderNoPo = "SELECT ORDER_NO FROM INV_PACK WHERE ORDER_NO IN (SELECT PURC_ORDER FROM PORDER WHERE " 
							  +"SALE_ORDER ='"+saleOrder+"'"+") AND ORDER_TYPE = 'P' AND CONFIRMED = 'Y'";
				System.out.println("sqlOrderNoPo :"+sqlOrderNoPo);
				stmt = conn.createStatement();
				rs = stmt.executeQuery(sqlOrderNoPo);
				while (rs.next())
				{
					orderNoPo = rs.getString(1);
					if(orderNoPo != null){
						orderNoArrList.add(orderNoPo);
					}
					System.out.println("orderNoPo :"+orderNoPo);
				}
				stmt.close();
				stmt = null;
				sqlOrderNoDo = "SELECT ORDER_NO FROM INV_PACK WHERE ORDER_NO IN (SELECT DIST_ORDER FROM DISTORDER "
							  +"WHERE SALE_ORDER ='"+saleOrder+"' ) AND ORDER_TYPE ='D' AND CONFIRMED = 'Y'";	
				System.out.println("sqlOrderNoDo :"+sqlOrderNoDo);
				stmt = conn.createStatement();
				rs = stmt.executeQuery(sqlOrderNoDo);
				if (rs.next())
				{
					orderNoDo = rs.getString(1);
					if(orderNoDo != null){
						orderNoArrList.add(orderNoDo);
					}
					System.out.println("orderNoDo :"+orderNoDo);
				}
				stmt.close();
				stmt = null;
				int orderNoListSize = orderNoArrList.size();
				String orderNoTemp = "";
				for(int ctr = 0;ctr < orderNoListSize;ctr++){
					orderNoTemp = orderNoArrList.get(ctr).toString();
					orderNo	= "'".concat(orderNoTemp).concat("',");
				}

				System.out.println("orderNo :: "+orderNo);

				if(orderNo != null && orderNo.indexOf(",") != -1){
					orderNo = orderNo.substring(0,orderNo.length()-1);
				}else{
					orderNo = "''";
				}

				System.out.println("orderNo :: "+orderNo);				
				//orderNo = "'".concat(orderNoSo).concat("'").concat(",").concat("'").concat(orderNoPo).concat("'").concat(",").concat("'").concat(orderNoDo).concat("'");

				sql = "SELECT INV_PACK_RCP.TRAN_ID, "   
						 +"INV_PACK_RCP.LINE_NO, "   
						 +"INV_PACK_RCP.ORDER_NO, "   
						 +"INV_PACK_RCP.LINE_NO__ORD, "   
						 +"INV_PACK_RCP.ITEM_CODE, "   
						 +"INV_PACK_RCP.LOC_CODE, "   
						 +"INV_PACK_RCP.LOT_NO, "   
						 +"INV_PACK_RCP.LOT_SL, "   
						 +"INV_PACK_RCP.QUANTITY, "   
						 +"INV_PACK_RCP.UNIT, "   
						 +"INV_PACK_RCP.PACK_CODE, "   
						 +"INV_PACK_RCP.PACK_INSTR, "   
						 +"INV_PACK_RCP.GROSS_WEIGHT, "   
						 +"INV_PACK_RCP.TARE_WEIGHT, "   
						 +"INV_PACK_RCP.NET_WEIGHT, "   
						 +"INV_PACK_RCP.NO_ART, "   
						 +"INV_PACK_RCP.DIMENSION, "   
						 +"'Y' AS PROCESS_YN "  
					+"FROM INV_PACK, INV_PACK_RCP, STOCK "  
					+"WHERE INV_PACK.TRAN_ID = INV_PACK_RCP.TRAN_ID "
					+"AND INV_PACK_RCP.ITEM_CODE = STOCK.ITEM_CODE "
					+"AND INV_PACK.SITE_CODE = STOCK.SITE_CODE "
					+"AND INV_PACK_RCP.LOC_CODE = STOCK.LOC_CODE "
					+"AND INV_PACK_RCP.LOT_NO = STOCK.LOT_NO "
					+"AND INV_PACK_RCP.LOT_SL = STOCK.LOT_SL "
					+"AND INV_PACK.ORDER_NO IN(" +orderNo+") "
					+"AND INV_PACK.CONFIRMED  = 'Y' "
					+"AND STOCK.QUANTITY >= INV_PACK_RCP.QUANTITY "   
					+"ORDER BY INV_PACK_RCP.LINE_NO ASC";

				System.out.println("sql :"+sql);

				stmt = conn.createStatement();
				rs = stmt1.executeQuery(sql);
				while (rs.next())// here in PB it opens a window and prompts the user to select records and then the procssing continues for the selected records
				{
					invOrderNo = rs.getString(3);
					origInvOrderNo = invOrderNo;
					System.out.println("invOrderNo :"+invOrderNo);
					sql1 = "SELECT ORDER_TYPE FROM INV_PACK WHERE ORDER_NO ='"+invOrderNo+"'";
					System.out.println("sql1 :"+sql1);
					rs1 = stmt.executeQuery(sql1);
					if(rs1.next())
					{
						orderType = rs1.getString(1);
					}
					System.out.println("orderType :"+orderType);
					if (orderType != null && orderType.equalsIgnoreCase("D"))
					{
						sql1 = "SELECT SALE_ORDER FROM DISTORDER WHERE DIST_ORDER = '"+invOrderNo+"'";
						System.out.println("sql1 :"+sql1);
						rs1 = stmt.executeQuery(sql1);
						if(rs1.next())
						{
							saleOrd = rs1.getString(1);
							System.out.println("saleOrd :"+saleOrd);
						}
						invOrderNo = saleOrd;
					}
					else if (orderType != null && orderType.equalsIgnoreCase("P"))
					{
						sql1 = "SELECT SALE_ORDER FROM PORDER WHERE PURC_ORDER = '"+invOrderNo+"'";
						System.out.println("sql1 :"+sql1);
						rs1 = stmt.executeQuery(sql1);
						if(rs1.next())
						{
							saleOrd = rs1.getString(1);
							System.out.println("saleOrd :"+saleOrd);
						}
						invOrderNo = saleOrd;
					}
					else if(orderType != null && orderType.equalsIgnoreCase("I"))
					{
						sql1 = "SELECT SALE_ORDER FROM DISTORDER WHERE DIST_ORDER = '"+invOrderNo+"'";
						System.out.println("sql1 :"+sql1);
						rs1 = stmt.executeQuery(sql1);
						if(rs1.next())
						{
							saleOrd = rs1.getString(1);
							System.out.println("saleOrd :"+saleOrd);
						}
						if(saleOrd != null && saleOrd.trim().length() > 0){
							invOrderNo = saleOrd;
							sorder = "Y";
						}						
						System.out.println("invOrderNo :"+invOrderNo);
					}

					lineNoOrd = rs.getString(4);
					System.out.println("lineNoOrd :"+lineNoOrd);
					itemCode = rs.getString(5);
					System.out.println("itemCode :"+itemCode);
					locCode = rs.getString(6);
					System.out.println("locCode :"+locCode);
					lotNo = rs.getString(7);
					System.out.println("lotNo :"+lotNo);
					lotSL = rs.getString(8);
					System.out.println("lotSL :"+lotSL);
					quantity = rs.getDouble(9);
					System.out.println("quantity :"+quantity);
					grossWeight = rs.getDouble(13);
					System.out.println("grossWeight :"+grossWeight);
					tareWeight = rs.getDouble(14);
					System.out.println("tareWeight :"+tareWeight);
					netWeight = rs.getDouble(15);
					System.out.println("netWeight :"+netWeight);
					packInstr = rs.getString(12);
					System.out.println("packInstr :"+packInstr);
					noArt = rs.getLong(16);
					System.out.println("noArt :"+noArt);
					dimension = rs.getString(17);
					System.out.println("dimension :"+dimension);

					if(sorder == "Y"){
						sql = "select sale_order, line_no__sord from distorder_det where dist_order = '"+origInvOrderNo+"' and line_no = '"+lineNoOrd+"'";
						rs1 = stmt.executeQuery(sql);
						if(rs1.next()){
							invOrderNo = rs1.getString("sale_order");
							lineNoOrd = rs1.getString("line_no__sord");
						}
					}
					System.out.println("lineNoOrd ::"+lineNoOrd);
					if (lineNoOrd.trim().length() == 1)
					{
						spaces = "  ";
					}
					if (lineNoOrd.trim().length() == 2)
					{
						spaces = " ";
					}
					System.out.println("spaces :"+spaces);
					lineNoOrd = spaces.concat(lineNoOrd.trim());

					System.out.println("lineNoOrd :"+lineNoOrd);

					sql1 = "SELECT EXP_LEV FROM SORDITEM WHERE SALE_ORDER ='"+invOrderNo+"'"
						  +"AND LINE_NO ='"+lineNoOrd+"'"+" AND LINE_TYPE = 'I'";
					System.out.println("sql1 :"+sql1);
					rs1 = stmt.executeQuery(sql1);
					if(rs1.next())
					{
						expLev = rs1.getString(1);
					}
					System.out.println("expLev :"+expLev);
					sql1 = "SELECT TAX_CLASS,TAX_CHAP,TAX_ENV,UNIT,UNIT__STD FROM SORDDET "
						  +"WHERE SALE_ORDER = '"+invOrderNo+"'"+" AND LINE_NO = '"+lineNoOrd+"'"; 
					System.out.println("sql1 :"+sql1);
					rs1 = stmt.executeQuery(sql1);
					if(rs1.next())
					{
						taxClass = rs1.getString(1);
						System.out.println("taxClass :"+taxClass);
						taxChap = rs1.getString(2);
						System.out.println("taxChap :"+taxChap);
						taxEnv = rs1.getString(3);
						System.out.println("taxEnv :"+taxEnv);
						unit = rs1.getString(4);
						System.out.println("unit :"+unit);
						unitStd = rs1.getString(5);
						System.out.println("unitStd :"+unitStd);
					}
					valueXmlString.append("<Detail>\r\n");
					valueXmlString.append("<desp_id>").append("<![CDATA[").append(despId).append("]]>").append("</desp_id>\r\n");
					valueXmlString.append("<sord_no>").append("<![CDATA[").append(invOrderNo).append("]]>").append("</sord_no>\r\n");
					valueXmlString.append("<line_no__sord isSrvCallOnChg='1'>").append("<![CDATA[").append(lineNoOrd.trim()).append("]]>").append("</line_no__sord>\r\n");
					valueXmlString.append("<exp_lev isSrvCallOnChg='1'>").append("<![CDATA[").append(expLev.trim()).append("]]>").append("</exp_lev>\r\n");
					valueXmlString.append("<unit>").append("<![CDATA[").append(unit).append("]]>").append("</unit>\r\n");
					valueXmlString.append("<unit__std>").append("<![CDATA[").append(unitStd).append("]]>").append("</unit__std>\r\n");
					valueXmlString.append("<unit__std>").append("<![CDATA[").append(unitStd).append("]]>").append("</unit__std>\r\n");
					qtyStd = quantity;
					conv = 0;
					if (!unit.trim().equals(unitStd.trim()))  //REMOVED NOT SYMBOL
					{
						qty = itmDBAccess.getConvQuantityFact(unitStd, unit, itemCode, qtyStd, conv, conn);
						System.out.println("qty :"+qty);
					}
					else
					{
						qty.add(0, new Double(qtyStd));
						qty.add(1,new Double(1));
					}
					detQty = ((Double)qty.get(0)).doubleValue();
					conv = ((Double)qty.get(1)).doubleValue();
					System.out.println("conv :"+conv);
					valueXmlString.append("<quantity>").append("<![CDATA[").append(qty.get(0)).append("]]>").append("</quantity>\r\n");
					valueXmlString.append("<quantity_real>").append("<![CDATA[").append(qtyStd).append("]]>").append("</quantity_real>\r\n");
					valueXmlString.append("<quantity__stduom>").append("<![CDATA[").append(qtyStd).append("]]>").append("</quantity__stduom>\r\n");
					valueXmlString.append("<conv__qty_stduom>").append("<![CDATA[").append(1 / Double.parseDouble(qty.get(1).toString())).append("]]>").append("</conv__qty_stduom>\r\n");
					valueXmlString.append("<pack_qty>").append("<![CDATA[").append(quantity / noArt).append("]]>").append("</pack_qty>\r\n");
					valueXmlString.append("<loc_code>").append("<![CDATA[").append(locCode.trim()).append("]]>").append("</loc_code>\r\n");
					valueXmlString.append("<lot_no>").append("<![CDATA[").append(lotNo.trim()).append("]]>").append("</lot_no>\r\n");
					if (lotSL.equals(""))
					{
						lotSL = " ";
					}
					valueXmlString.append("<lot_sl>").append("<![CDATA[").append(lotSL.trim()).append("]]>").append("</lot_sl>\r\n");
					valueXmlString.append("<status>").append("<![CDATA[").append(" ").append("]]>").append("</status>\r\n");
					valueXmlString.append("<pack_instr>").append("<![CDATA[").append(packInstr).append("]]>").append("</pack_instr>\r\n");
					valueXmlString.append("<gross_weight>").append("<![CDATA[").append(grossWeight).append("]]>").append("</gross_weight>\r\n");
					valueXmlString.append("<tare_weight>").append("<![CDATA[").append(tareWeight).append("]]>").append("</tare_weight>\r\n");
					valueXmlString.append("<nett_weight>").append("<![CDATA[").append(netWeight).append("]]>").append("</nett_weight>\r\n");
					valueXmlString.append("<no_art>").append("<![CDATA[").append(noArt).append("]]>").append("</no_art>\r\n");
					valueXmlString.append("<dimension>").append("<![CDATA[").append(dimension).append("]]>").append("</dimension>\r\n");
					valueXmlString.append("<tax_class>").append("<![CDATA[").append((taxClass == null) ? "":taxClass).append("]]>").append("</tax_class>\r\n");
					valueXmlString.append("<tax_chap>").append("<![CDATA[").append((taxChap == null) ? "":taxChap).append("]]>").append("</tax_chap>\r\n");
					valueXmlString.append("<tax_env>").append("<![CDATA[").append((taxEnv == null) ? "":taxEnv).append("]]>").append("</tax_env>\r\n");
					valueXmlString.append("</Detail>\r\n");

					/////////Deallocate	not required as per Manoharan Sir 10/02/07
					/*String siteCode = "",itemCodeOrd = "",itemCode = "";
					double qtyAlloc = 0;
					HashMap strAllocate = new HashMap();
					String sql = "select site_code from sorddet where sale_order ='"+invOrderNo+"' and line_no = '"+lineNoOrd+"'";
					rs1 = stmt.executeQuery(sql);
					if(rs1.next){
						siteCode = rs1.getString("site_code");
					}
					sql = "select item_code__ord,item_code from sorditem where sale_order = '"+invOrderNo+"' and line_no = '"+lineNoOrd+"' and site_code = '"+siteCode+"' and exp_lev = '"+expLev+"'";
					rs1 = stmt.executeQuery(sql);
					if(rs1.next()){
						itemCodeOrd = rs1.getString("item_code__ord");
						itemCode = rs1.getString("item_code");
					}
					sql = "select qty_alloc from sordalloc where sale_order = '"+invOrderNo+"' and line_no = '"+lineNoOrd+"' "+
							"and exp_lev = '"+expLev+"' and item_code__ord = '"+itemCodeOrd+"' and item_code = '"+itemCode+"' loc_code = '"+locCode+"' "+
							"and lot_no = '"+lotNo+"' and lot_sl = '"+lotSL+"'";

					rs1 = stmt.executeQuery(sql);
					if(rs1.next()){
						qtyAlloc = rs1.getdouble("qty_alloc");
					}
					int upd = 0;	
					if(qtyAlloc >= detQty){
						if(qtyAlloc - detQty <= 0){
							sql = "delete from sordalloc where sale_order = '"+invOrderNo+"' and line_no = '"+lineNoOrd+"' "+
									"and exp_lev = '"+expLev+"' item_code__ord = '"+itemCodeOrd+"' item_code = '"+itemCode+"' and loc_code = '"+locCode+"' "+
									"and lot_no = '"+lotNo+"' and lot_sl = '"+lotSL+"'";
							upd = stmt.executeUpdate(sql);
							System.out.println("Records Deleted :: "+upd);
						}
						else{
							sql = "update sordalloc set qty_alloc = qtyAlloc - "+detQty+" where "+
									"sale_order = '"+invOrderNo+"' and line_no = '"+lineNoOrd+"' and exp_lev = '"+expLev+"' "+
									"and item_code__ord = '"+itemCodeOrd+"' and item_code = '"+itemCode+"' and loc_code = '"+locCode+"' "
									"and lot_no = '"+lotNo+"' lot_sl = '"+lotSL+"'";
							upd = stmt.executeUpdate(sql);
							System.out.println("Records Updated :: "+upd);
						}
						sql = "update sorditem set qty_alloc = qty_alloc - "+detQty+" where sale_order = '"+invOrderNo+"' and "+
								"line_no = '"+lineNoOrd+"' and exp_lev = '"+expLev+"'";
						strAllocate.put("ref_ser",);
						strAllocate.put("ref_id",);
						strAllocate.put("ref_line",);
						strAllocate.put("item_code",);
						strAllocate.put("site_code",);
						strAllocate.put("loc_code",);
						strAllocate.put("lot_no",);
						strAllocate.put("lot_sl",);
						strAllocate.put("alloc_qty",);
						strAllocate.put("chg_win",);
						strAllocate.put("chg_user",);
						strAllocate.put("chg_term",);  
					//}
					////////////
				}//while end
				valueXmlString.append("</Root>\r\n");			
			//}//end if(detailCount)
		}//try end
		catch(Exception e)
		{
			System.out.println("Exception :Despatch :" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("Closing Connection.....");
				conn.close();
			}catch(Exception e){}
		}
		System.out.println("valueXmlString.toString() "+valueXmlString.toString());
		return valueXmlString.toString();		
	}
	 *///End Comment Gulzar - 23-04-07 - 
	//Added Changes - Gulzar - 23-04-07
	private String actionGetPackList(Document dom, Document dom1, String objContext, String xtraParams) throws RemoteException, ITMException
	{
		String saleOrder = "",despId = "",sqlOrderNoSo = "",sqlOrderNoPo = "",sqlOrderNoDo = "";
		String orderNo = "",orderNoSo = "",orderNoPo = "",orderNoDo = "",invOrderNo = "";		
		String sql1 = "", sql = "";
		String orderType = "",spaces = "",expLev = "",saleOrd = "";
		String taxClass = "", taxChap = "", taxEnv = "", unit = "",unitStd = "";  
		String lineNoOrd ="", itemCode = "", locCode = "", lotNo = "", lotSL = "", packInstr  = "", dimension = "";   
		double quantity = 0, grossWeight = 0, tareWeight = 0, netWeight = 0, conv = 0, qtyStd = 0; 
		double detQty = 0;
		long   noArt = 0;
		Timestamp expDate = null ,mfgDate = null ,retestDate = null;
		ArrayList qty = new ArrayList();
		ArrayList orderNoArrList = new ArrayList();
		ResultSet rs = null;
		String detailCnt = "0",sExpDate= "",sMfgDate = "" ,sRetestDate = "";
		ResultSet rs1 = null;
		Connection conn = null;
		//Changed and added by Pavan R 10jan19[to handle open cursor issue] Start
		//Statement stmt = null;
		PreparedStatement pstmt = null;
		//Statement stmt1 = null;
		//Changed and added by Pavan R 10jan19[to handle open cursor issue]End
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		String origInvOrderNo = "",sorder = "N";
		StringBuffer ordString = new StringBuffer();
		try
		{
			//detailCnt = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"detCnt");
			//if(detailCnt.equals("0"))
			//{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			//stmt = conn.createStatement();//Changed by Pavan R 10jan19[to handle open cursor issue]
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			saleOrder = genericUtility.getColumnValue("sord_no",dom1);
			despId = genericUtility.getColumnValue("desp_id",dom1);
			System.out.println("saleOrder :"+saleOrder);
			System.out.println("despId :"+despId);

			// 27/02/10 manoharan changed as per PB code
			//sqlOrderNoSo = "SELECT ORDER_NO FROM INV_PACK WHERE ORDER_NO ='"+saleOrder+"'"+" AND "
			//			  +"ORDER_TYPE IN ('S','I') AND CONFIRMED ='Y'";
			sqlOrderNoSo = "select order_no from inv_pack "
				+ " where order_no 	= ?" //'" + saleOrder + "' "//Changed and added by Pavan R 10jan19[to handle open cursor issue] Start
				+ " and	order_type	in	('S','I') "
				+ " and	confirmed 	= 'Y' ";
			// 27/02/10 manoharan changed as per PB code
			//System.out.println("sqlOrderNoSo :"+sqlOrderNoSo);
			//rs = stmt.executeQuery(sqlOrderNoSo);
			pstmt = conn.prepareStatement(sqlOrderNoSo);
			pstmt.setString(1, saleOrder);
			rs = pstmt.executeQuery();//Changed and added by Pavan R 10jan19[to handle open cursor issue] End
			while (rs.next())
			{
				orderNoSo = rs.getString(1);
				if(orderNoSo != null){
					orderNoArrList.add(orderNoSo);
				}
				System.out.println("orderNoSo :"+orderNoSo);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			// 27/02/10 manoharan changed as per PB code
			//sqlOrderNoPo = "SELECT ORDER_NO FROM INV_PACK WHERE ORDER_NO IN (SELECT PURC_ORDER FROM PORDER WHERE " 
			//			  +"SALE_ORDER ='"+saleOrder+"'"+") AND ORDER_TYPE = 'P' AND CONFIRMED = 'Y'";
			sqlOrderNoPo = "select order_no from inv_pack "
				+ " where order_no 	in ( select purc_order from porder where sale_order = ?) "//'" + saleOrder + "') " 
				+ " and order_type	= 'I'  "
				+ " and confirmed 	= 'Y' ";
			//+ " and order_type	in	('P','I') "
			// end 27/02/10 manoharan changed as per PB code
			//System.out.println("sqlOrderNoPo :"+sqlOrderNoPo);
			//stmt = conn.createStatement();
			//rs = stmt.executeQuery(sqlOrderNoPo);
			pstmt = conn.prepareStatement(sqlOrderNoPo);
			pstmt.setString(1, saleOrder);
			rs = pstmt.executeQuery();			
			while (rs.next())
			{
				orderNoPo = rs.getString(1);
				if(orderNoPo != null){
					orderNoArrList.add(orderNoPo);
				}
				System.out.println("orderNoPo :"+orderNoPo);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			// 27/02/10 manoharan changed as per PB code
			//sqlOrderNoPo = "SELECT ORDER_NO FROM INV_PACK WHERE ORDER_NO IN (SELECT PURC_ORDER FROM PORDER WHERE " 
			//			  +"SALE_ORDER ='"+saleOrder+"'"+") AND ORDER_TYPE = 'P' AND CONFIRMED = 'Y'";
			sqlOrderNoPo = " select order_no from inv_pack where order_no "
				+ " in ( select sale_order from sorder where cust_pord "
				+ " in (Select purc_order from porder where sale_order = ?))" //'" + saleOrder + "')) "
				+ " and order_type	in	('S') "
				+ " and  confirmed 	= 'Y' "
				+ " and site_code in (select site_code__ship from sorder where sale_order = ?)"; //'" + saleOrder + "') ";
			// end 27/02/10 manoharan changed as per PB code
			//System.out.println("sqlOrderNoPo :"+sqlOrderNoPo);
			//stmt = conn.createStatement();
			//rs = stmt.executeQuery(sqlOrderNoPo);
			pstmt = conn.prepareStatement(sqlOrderNoPo);
			pstmt.setString(1, saleOrder);
			pstmt.setString(2, saleOrder);
			rs = pstmt.executeQuery();						
			while (rs.next())
			{
				orderNoPo = rs.getString(1);
				if(orderNoPo != null){
					orderNoArrList.add(orderNoPo);
				}
				System.out.println("orderNoPo :"+orderNoPo);
			}
			rs.close(); rs = null;
			pstmt.close();
			pstmt = null;
			// 27/02/10 manoharan changed as per PB code
			//sqlOrderNoDo = "SELECT ORDER_NO FROM INV_PACK WHERE ORDER_NO IN (SELECT DIST_ORDER FROM DISTORDER "
			//			  +"WHERE SALE_ORDER ='"+saleOrder+"' ) AND ORDER_TYPE ='D' AND CONFIRMED = 'Y'";	
			System.out.println("currentActionType:::::::::::::::::[[[["+currentActionType+"]]]]");
			if( currentActionType.equalsIgnoreCase("Get SPack List"))
			{
				sqlOrderNoDo = "select order_no from inv_pack  "
					+ " where order_no 	in ( select dist_order from distorder where sale_order = ?)"//'" + saleOrder + "') "
					+ " and order_type	= 'S'  "
					+ " and confirmed 	= 'Y' ";	
			}
			else
			{	
				sqlOrderNoDo = "select order_no from inv_pack  "
					+ " where order_no 	in ( select dist_order from distorder where sale_order = ?)"//'" + saleOrder + "') "
					+ " and order_type	= 'I'  "
					+ " and confirmed 	= 'Y' ";
			}
			//+ " and order_type	in	('D','I')  "
			// end 27/02/10 manoharan
			//System.out.println("sqlOrderNoDo :"+sqlOrderNoDo);
			//stmt = conn.createStatement();
			//rs = stmt.executeQuery(sqlOrderNoDo);
			pstmt = conn.prepareStatement(sqlOrderNoDo);
			pstmt.setString(1, saleOrder);			
			rs = pstmt.executeQuery();									
			while (rs.next())
			{
				orderNoDo = rs.getString(1);
				if(orderNoDo != null){
					orderNoArrList.add(orderNoDo);
				}
				System.out.println("orderNoDo :"+orderNoDo);
			}
			rs.close();rs = null;
			pstmt.close();
			pstmt = null;
			int orderNoListSize = orderNoArrList.size();
			String orderNoTemp = "";
			for(int ctr = 0;ctr < orderNoListSize;ctr++){
				orderNoTemp = orderNoArrList.get(ctr).toString();
				//Modified by Anjali R. on [25/05/2019][In prepared statement single quoted string not required for argument][Start]
				//orderNo	= orderNo + "'".concat(orderNoTemp).concat("',");
				orderNo	= orderNo + (orderNoTemp).concat(",");
				//Modified by Anjali R. on [25/05/2019][In prepared statement single quoted string not required for argument][End]
			}

			System.out.println("orderNo :: "+orderNo);
			
			if(orderNo == null || "".equalsIgnoreCase(orderNo.trim()))
			{				
				ordString.append("''");
			}
			StringTokenizer st3 = new StringTokenizer(orderNo, ",");			
			while (st3.hasMoreElements()) 
			{
				ordString.append("'").append(st3.nextElement()).append("'");				
				if(st3.hasMoreElements())
				{
					ordString.append(",");
				}
			}
			/*if(orderNo != null && orderNo.indexOf(",") != -1)
			{
				orderNo = orderNo.substring(0,orderNo.length()-1);
			}
			else
			{
				//Modified by Anjali R. on [25/05/2019][In prepared statement single quoted string not required for argument][Start]
				//orderNo = "''";
				orderNo = "";
				//Modified by Anjali R. on [25/05/2019][In prepared statement single quoted string not required for argument][End]
			}*/

			System.out.println("ordString ::"+ordString.toString()+"  orderNo :: "+orderNo);				
			//orderNo = "'".concat(orderNoSo).concat("'").concat(",").concat("'").concat(orderNoPo).concat("'").concat(",").concat("'").concat(orderNoDo).concat("'");

			sql = "SELECT INV_PACK_RCP.TRAN_ID, "   
				+"INV_PACK_RCP.LINE_NO, "   
				+"INV_PACK_RCP.ORDER_NO, "   
				+"INV_PACK_RCP.LINE_NO__ORD, "   
				+"INV_PACK_RCP.ITEM_CODE, "   
				+"INV_PACK_RCP.LOC_CODE, "   
				+"INV_PACK_RCP.LOT_NO, "   
				+"INV_PACK_RCP.LOT_SL, "   
				+"INV_PACK_RCP.QUANTITY, "   
				+"INV_PACK_RCP.UNIT, "   
				+"INV_PACK_RCP.PACK_CODE, "   
				+"INV_PACK_RCP.PACK_INSTR, "   
				+"INV_PACK_RCP.GROSS_WEIGHT, "   
				+"INV_PACK_RCP.TARE_WEIGHT, "   
				+"INV_PACK_RCP.NET_WEIGHT, "   
				+"INV_PACK_RCP.NO_ART, "   
				+"INV_PACK_RCP.DIMENSION, "   
				+"'Y' AS PROCESS_YN ,INV_PACK_RCP.PALLET_WT  "  ///+" STOCK.MFG_DATE, STOCK.EXP_DATE " 
				+"FROM INV_PACK, INV_PACK_RCP, STOCK "  
				+"WHERE INV_PACK.TRAN_ID = INV_PACK_RCP.TRAN_ID "
				+"AND INV_PACK_RCP.ITEM_CODE = STOCK.ITEM_CODE "
				+"AND INV_PACK.SITE_CODE = STOCK.SITE_CODE "
				+"AND INV_PACK_RCP.LOC_CODE = STOCK.LOC_CODE "
				+"AND INV_PACK_RCP.LOT_NO = STOCK.LOT_NO "
				+"AND INV_PACK_RCP.LOT_SL = STOCK.LOT_SL "
				+"AND INV_PACK.ORDER_NO IN(" + ordString.toString() + ") "   //+orderNo+") "  //Changed and added by Pavan R 10jan19[to handle open cursor issue] start     
				+"AND INV_PACK.ORDER_TYPE  in	('S','I')  "
				+"AND INV_PACK.CONFIRMED  = 'Y' "
				+"AND STOCK.QUANTITY >= INV_PACK_RCP.QUANTITY "   
				+"ORDER BY INV_PACK.TRAN_DATE ,INV_PACK.TRAN_ID,INV_PACK_RCP.LINE_NO ASC ";

			//System.out.println("sql :"+sql);

			//stmt = conn.createStatement();
			//rs = stmt.executeQuery(sql);
			pstmt = conn.prepareStatement(sql);
			//pstmt.setString(1, orderNo);						
			rs = pstmt.executeQuery();//Changed and added by Pavan R 10jan19[to handle open cursor issue] end
			while (rs.next())// here in PB it opens a window and prompts the user to select records and then the procssing continues for the selected records
			{
				valueXmlString.append("<Detail>\r\n");

				valueXmlString.append("<tran_id>").append("<![CDATA[").append(rs.getString(1)).append("]]>").append("</tran_id>\r\n");
				valueXmlString.append("<line_no>").append("<![CDATA[").append(rs.getInt(2)).append("]]>").append("</line_no>\r\n");
				valueXmlString.append("<order_no>").append("<![CDATA[").append(rs.getString(3)).append("]]>").append("</order_no>\r\n");
				valueXmlString.append("<line_no__ord>").append("<![CDATA[").append(rs.getInt(4)).append("]]>").append("</line_no__ord>\r\n");
				valueXmlString.append("<item_code>").append("<![CDATA[").append(rs.getString(5)).append("]]>").append("</item_code>\r\n");
				valueXmlString.append("<loc_code>").append("<![CDATA[").append(rs.getString(6)).append("]]>").append("</loc_code>\r\n");
				valueXmlString.append("<lot_no>").append("<![CDATA[").append(rs.getString(7)).append("]]>").append("</lot_no>\r\n");
				valueXmlString.append("<lot_sl>").append("<![CDATA[").append(rs.getString(8)).append("]]>").append("</lot_sl>\r\n");
				valueXmlString.append("<quantity>").append("<![CDATA[").append(rs.getDouble(9)).append("]]>").append("</quantity>\r\n");
				valueXmlString.append("<unit>").append("<![CDATA[").append(rs.getString(10)).append("]]>").append("</unit>\r\n");
				valueXmlString.append("<pack_code>").append("<![CDATA[").append(rs.getString(11)).append("]]>").append("</pack_code>\r\n");
				valueXmlString.append("<pack_instr>").append("<![CDATA[").append((rs.getString(12) == null) ? "":rs.getString(12)).append("]]>").append("</pack_instr>\r\n");
				//packInstrInvPackRcp =rs.getString(12);
				valueXmlString.append("<gross_weight>").append("<![CDATA[").append(rs.getDouble(13)).append("]]>").append("</gross_weight>\r\n");
				valueXmlString.append("<tare_weight>").append("<![CDATA[").append(rs.getDouble(14)).append("]]>").append("</tare_weight>\r\n");
				valueXmlString.append("<net_weight>").append("<![CDATA[").append(rs.getDouble(15)).append("]]>").append("</net_weight>\r\n");
				valueXmlString.append("<no_art>").append("<![CDATA[").append(rs.getInt(16)).append("]]>").append("</no_art>\r\n");
				String dim = rs.getString(17); // 03/12/10 manoharan
				if (dim == null || dim.trim().length() == 0)
				{
					dim = " ";
				}
				//valueXmlString.append("<dimension>").append("<![CDATA[").append((rs.getString(17) == null) ? " ":rs.getString(17)).append("]]>").append("</dimension>\r\n");
				valueXmlString.append("<dimension>").append("<![CDATA[").append(dim).append("]]>").append("</dimension>\r\n");
				// end 03/12/10
				valueXmlString.append("<process_yn>").append("<![CDATA[").append(rs.getString(18)).append("]]>").append("</process_yn>\r\n");

				//added by kunal on 13/02/13 add pallet wt.
				valueXmlString.append("<pallet_wt>").append("<![CDATA[").append(rs.getDouble(19)).append("]]>").append("</pallet_wt>\r\n");

				valueXmlString.append("</Detail>\r\n");
			}//while end
			rs.close(); rs = null;
			pstmt.close();pstmt = null;
			valueXmlString.append("</Root>\r\n");			
			//}//end if(detailCount)
		}//try end
		catch(Exception e)
		{
			System.out.println("Exception :[Despatch][actionGetPackList]: " + e.getMessage() + ":");
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("Closing Connection.....");
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if(conn != null)
				{
					conn.close();
					conn = null;
				}
			}catch(Exception e){}
		}
		System.out.println("valueXmlString.toString() "+valueXmlString.toString());
		return valueXmlString.toString();		
	}
	private String packListTransform(Document dom,Document dom1, String objContext, String xtraParams, Document selDataDom)throws ITMException
	{
		String saleOrder = "",despId = "",sqlOrderNoSo = "",sqlOrderNoPo = "",sqlOrderNoDo = "";
		String orderNo = "",orderNoSo = "",orderNoPo = "",orderNoDo = "",invOrderNo = "";		
		String sqlStr = "", origInvOrderNo = "", sorder = "N";
		String orderType = "",spaces = "",expLev = "",saleOrd = "";
		String taxClass = "", taxChap = "", taxEnv = "", unit = "",unitStd = "";  
		String lineNoOrd ="", itemCode = "", locCode = "", lotNo = "", lotSL = "", packInstr  = "", dimension = "", dimension1 = "";   
		String grossWt = "", tareWt = "", netWt = "", noArt1 = "";
		String sExpDate="" , sMfgDate="" ,sRetestDate ="" ,siteCode = "";
		String sql ="", tempStr = "",sql2="";
		PreparedStatement pstmt = null,pstmt1=null;
		Timestamp retestDate = null ,mfgDate = null, expDate = null;
		double quantity = 0, grossWeight = 0, tareWeight = 0, netWeight = 0, conv = 0, qtyStd = 0; 
		double detQty = 0, noArt = 0;
		double palletWt = 0 ,palletWt1 = 0 ;//added by kunal on 1/02/13
		NodeList detailList = null;
		Node currDetail = null;
		int detailListLength = 0;
		ArrayList qty = new ArrayList();
		DistCommon distCommon = new DistCommon();
		// 19-01-11 -Chandni Shah
		double packQty = 0d;
		DecimalFormat df = new DecimalFormat("#########.###");
		//
		Connection conn = null;
		//Statement stmt = null; 
		ResultSet rs = null;
		ResultSet rs1 = null,rs2=null;
		//Statement stmt1 = null;
		ConnDriver connDriver = new ConnDriver();
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			//stmt = conn.createStatement();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			despId = genericUtility.getColumnValue("desp_id",dom1);
			siteCode = genericUtility.getColumnValue("site_code",dom1);
			detailList = selDataDom.getElementsByTagName("Detail");
			detailListLength = detailList.getLength(); 
			for (int ctr = 0;ctr < detailListLength; ctr++)
			{
				currDetail = detailList.item(ctr);
				invOrderNo = genericUtility.getColumnValueFromNode("order_no",currDetail);
				origInvOrderNo = invOrderNo;
				sqlStr = "SELECT ORDER_TYPE FROM INV_PACK WHERE ORDER_NO = ? " ;    //'"+invOrderNo+"'";//Changed and added by Pavan R 10jan19[to handle open cursor issue] Start
				//rs = stmt.executeQuery(sqlStr);
				pstmt = conn.prepareStatement(sqlStr); 
				pstmt.setString(1, invOrderNo);
				rs = pstmt.executeQuery();//Changed and added by Pavan R 10jan19[to handle open cursor issue] end
				if(rs.next())
				{
					orderType = rs.getString("ORDER_TYPE");
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
				System.out.println("orderType :"+orderType);
				if (orderType != null && orderType.equalsIgnoreCase("D"))
				{
					sqlStr = "SELECT SALE_ORDER FROM DISTORDER WHERE DIST_ORDER = ?";    //'"+invOrderNo+"'";//Changed and added by Pavan R 10jan19[to handle open cursor issue] Start
					//rs = stmt.executeQuery(sqlStr);
					pstmt = conn.prepareStatement(sqlStr); 
					pstmt.setString(1, invOrderNo);
					rs = pstmt.executeQuery();//Changed and added by Pavan R 10jan19[to handle open cursor issue] end
					if(rs.next())
					{
						saleOrd = rs.getString("SALE_ORDER");
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;
					invOrderNo = saleOrd;
					sorder = "Y";
				}
				else if (orderType != null && orderType.equalsIgnoreCase("P"))
				{
					sqlStr = "SELECT SALE_ORDER FROM PORDER WHERE PURC_ORDER = ?";   // '"+invOrderNo+"'";//Changed and added by Pavan R 10jan19[to handle open cursor issue] Start
					//rs = stmt.executeQuery(sqlStr);
					pstmt = conn.prepareStatement(sqlStr); 
					pstmt.setString(1, invOrderNo);
					rs = pstmt.executeQuery();//Changed and added by Pavan R 10jan19[to handle open cursor issue] end
					if(rs.next())
					{
						saleOrd = rs.getString("SALE_ORDER");
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;					
					invOrderNo = saleOrd;
				}
				else if(orderType != null && orderType.equalsIgnoreCase("I"))
				{
					sqlStr = "SELECT SALE_ORDER FROM DISTORDER WHERE DIST_ORDER = ?";      //'"+invOrderNo+"'";//Changed and added by Pavan R 10jan19[to handle open cursor issue] Start
					//rs = stmt.executeQuery(sqlStr);
					pstmt = conn.prepareStatement(sqlStr); 
					pstmt.setString(1, invOrderNo);
					rs = pstmt.executeQuery();//Changed and added by Pavan R 10jan19[to handle open cursor issue] end
					if(rs.next())
					{
						saleOrd = rs.getString("SALE_ORDER");
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;					
					if(saleOrd != null && saleOrd.trim().length() > 0){
						invOrderNo = saleOrd;
						sorder = "Y";
					}						
				}
				lineNoOrd = genericUtility.getColumnValueFromNode("line_no__ord",currDetail);
				//if(sorder == "Y")
				if("Y".equalsIgnoreCase(sorder))
				{
					sqlStr = "SELECT SALE_ORDER, LINE_NO__SORD FROM DISTORDER_DET WHERE DIST_ORDER = ? AND LINE_NO = ?";  //'"+origInvOrderNo+"' AND LINE_NO = '"+lineNoOrd+"'";//Changed and added by Pavan R 10jan19[to handle open cursor issue] Start
					//rs = stmt.executeQuery(sqlStr);
					pstmt = conn.prepareStatement(sqlStr); 
					pstmt.setString(1, origInvOrderNo);
					pstmt.setString(2, lineNoOrd);
					rs = pstmt.executeQuery();//Changed and added by Pavan R 10jan19[to handle open cursor issue] end
					if(rs.next()){
						invOrderNo	= rs.getString("SALE_ORDER");
						lineNoOrd	= rs.getString("LINE_NO__SORD");
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;
				}
				// 03/12/10 manoharan
				if (lineNoOrd.trim().length() == 1)
				{
					spaces = "  ";
					lineNoOrd = spaces.concat(lineNoOrd.trim());
				}
				else if (lineNoOrd.trim().length() == 2)
				{
					spaces = " ";
					lineNoOrd = spaces.concat(lineNoOrd.trim());
				}
				else if (lineNoOrd.trim().length() == 3)
				{
					spaces = " ";
					lineNoOrd = lineNoOrd.trim();
				}
				// end 03/12/10 manoharan
				sqlStr = "SELECT EXP_LEV FROM SORDITEM WHERE SALE_ORDER = ?"//'"+invOrderNo+"'"
				//+"AND LINE_NO ='"+lineNoOrd+"'"+" AND LINE_TYPE = 'I'";
				+"AND LINE_NO = ? AND LINE_TYPE = 'I'";
				//rs = stmt.executeQuery(sqlStr);
				pstmt = conn.prepareStatement(sqlStr);
				pstmt.setString(1, invOrderNo);
				pstmt.setString(2, lineNoOrd);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					expLev = rs.getString("EXP_LEV");
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
				sqlStr = "SELECT TAX_CLASS,TAX_CHAP,TAX_ENV,UNIT,UNIT__STD FROM SORDDET "
					//+"WHERE SALE_ORDER = '"+invOrderNo+"'"+" AND LINE_NO = '"+lineNoOrd+"'"; //Changed and added by Pavan R 10jan19[to handle open cursor issue] Start
						+"WHERE SALE_ORDER = ? AND LINE_NO = ?";
				//rs = stmt.executeQuery(sqlStr);
				pstmt = conn.prepareStatement(sqlStr);
				pstmt.setString(1, invOrderNo);
				pstmt.setString(2, lineNoOrd);
				rs = pstmt.executeQuery();//Changed and added by Pavan R 10jan19[to handle open cursor issue] end
				if(rs.next())
				{
					taxClass = rs.getString("TAX_CLASS");
					taxChap = rs.getString("TAX_CHAP");
					taxEnv = rs.getString("TAX_ENV");
					unit = rs.getString("UNIT");
					unitStd = rs.getString("UNIT__STD");
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
				valueXmlString.append("<Detail>\r\n");
				valueXmlString.append("<desp_id isSrvCallOnChg='0'>").append("<![CDATA[").append((despId == null) ? "":despId).append("]]>").append("</desp_id>\r\n"); //44
				setNodeValue( dom, "desp_id", (despId == null) ? "":despId );
				valueXmlString.append("<sord_no isSrvCallOnChg='0'>").append("<![CDATA[").append(invOrderNo).append("]]>").append("</sord_no>\r\n");

				setNodeValue( dom, "sord_no", invOrderNo );

				valueXmlString.append("<line_no__sord isSrvCallOnChg='0'>").append("<![CDATA[").append(lineNoOrd).append("]]>").append("</line_no__sord>\r\n");
				setNodeValue( dom, "line_no__sord", lineNoOrd );
				tempStr = getChangeSord(dom,  dom1, "line_no__sord", xtraParams ,conn);//chg3
				System.out.println("manohar 03/12/10 line_no__sord [" + lineNoOrd + "]  tempStr [" + tempStr + "]");
				valueXmlString.append(tempStr);

				valueXmlString.append("<exp_lev isSrvCallOnChg='0'>").append("<![CDATA[").append(expLev).append("]]>").append("</exp_lev>\r\n");
				setNodeValue( dom, "exp_lev", expLev );
				System.out.println("manohar 03/12/10 exp_lev [" + expLev + "]");
				//expLev = getChangeSord(dom,  dom1, "exp_lev", xtraParams ,conn);//chg4
				//valueXmlString.append(expLev);
				valueXmlString.append("<unit isSrvCallOnChg='0'>").append("<![CDATA[").append(unit).append("]]>").append("</unit>\r\n");
				setNodeValue( dom, "unit", (unit == null) ? "":unit );
				valueXmlString.append("<unit__std isSrvCallOnChg='0'>").append("<![CDATA[").append(unitStd).append("]]>").append("</unit__std>\r\n");
				setNodeValue( dom, "unit__std", (unitStd == null) ? "":unitStd );
				//valueXmlString.append("<unit__std isSrvCallOnChg='0'>").append("<![CDATA[").append(unitStd).append("]]>").append("</unit__std>\r\n");
				//setNodeValue( dom, "desp_id", (despId == null) ? "":despId );
				itemCode = genericUtility.getColumnValueFromNode("item_code",currDetail);
				//valueXmlString.append("<item_code isSrvCallOnChg='0'>").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code>\r\n");
				//setNodeValue( dom, "item_code", (itemCode == null) ? "":itemCode );
				quantity = Double.parseDouble(genericUtility.getColumnValueFromNode("quantity",currDetail));
				qtyStd = 0;
				conv = 0;
				System.out.println("manohar 14/10/10 qtyStd 1 ["+qtyStd + "]");
				if (!unit.trim().equals(unitStd.trim()))  //REMOVED NOT SYMBOL
				{
					qty = distCommon.getConvQuantityFact(unit,unitStd, itemCode, quantity, conv, conn);
					System.out.println("Inside if qty="+qty);
				}
				else
				{
					qty.add(0, new Double(1));
					qty.add(1,new Double(quantity));
					System.out.println("Inside else qty="+qty);
				}
				//change done by kunal on 30/01/13 
				detQty = Double.parseDouble(qty.get(1).toString());
				qtyStd = Double.parseDouble(qty.get(1).toString());
				conv = Double.parseDouble(qty.get(0).toString());
				//change done by kunal on 30/01/13 end

				//detQty = ((Double)qty.get(1)).doubleValue();
				//qtyStd = ((Double)qty.get(1)).doubleValue();
				//conv = ((Double)qty.get(0)).doubleValue();

				System.out.println("conv :"+conv);

				locCode		= genericUtility.getColumnValueFromNode("loc_code",currDetail);
				lotNo		= genericUtility.getColumnValueFromNode("lot_no",currDetail);
				lotSL		= genericUtility.getColumnValueFromNode("lot_sl",currDetail);
				packInstr	= genericUtility.getColumnValueFromNode("pack_instr",currDetail);
				grossWt		= genericUtility.getColumnValueFromNode("gross_weight",currDetail);
				tareWt		= genericUtility.getColumnValueFromNode("tare_weight",currDetail);
				netWt		= genericUtility.getColumnValueFromNode("nett_weight",currDetail);
				noArt1		= genericUtility.getColumnValueFromNode("no_art",currDetail);

				if (grossWt != null)
				{
					grossWeight	= Double.parseDouble(grossWt);
				}
				else 
				{
					grossWeight = 0;
				}
				if (tareWt != null)
				{
					tareWeight	= Double.parseDouble(tareWt);
				}
				else 
				{
					tareWeight = 0;
				}
				if (netWt != null)
				{
					netWeight	= Double.parseDouble(netWt);
				}
				else 
				{
					netWeight = 0;
				}
				if (noArt1 != null)
				{
					noArt	= Double.parseDouble(noArt1);
				}
				else 
				{
					noArt = 1;
				}
				//19-01-11 -Chandni shah
				if (noArt > 0)
				{
					packQty = 0d;
					packQty = quantity / noArt;
					packQty = df.parse(df.format(packQty)).doubleValue(); 
				}
				//
				dimension1	= genericUtility.getColumnValueFromNode("dimension",currDetail);
				dimension1 = (dimension1 == null ? " ": dimension1 );
				palletWt1	= Double.parseDouble(genericUtility.getColumnValueFromNode("pallet_wt",currDetail) == null?"0":genericUtility.getColumnValueFromNode("pallet_wt",currDetail).trim());

				System.out.println("dimension::"+dimension1+"   palletWt::"+palletWt1);

				System.out.println("Double.parseDouble(qty.get(1).toString())"+Double.parseDouble(qty.get(1).toString()));
				//valueXmlString.append("<quantity isSrvCallOnChg='0'>").append("<![CDATA[").append(qty.get(1)).append("]]>").append("</quantity>\r\n");
				setNodeValue( dom, "quantity",quantity);
				//valueXmlString.append("<quantity__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(qtyStd).append("]]>").append("</quantity__stduom>\r\n");
				setNodeValue( dom, "quantity__stduom", qtyStd );
				//valueXmlString.append("<conv__qty_stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(1 / Double.parseDouble(qty.get(1).toString())).append("]]>").append("</conv__qty_stduom>\r\n");
				setNodeValue( dom, "conv__qty_stduom", (conv));
				//setNodeValue( dom, "quantity", (qty.get(0)));
				//valueXmlString.append("<quantity_real isSrvCallOnChg='0'>").append("<![CDATA[").append(qtyStd).append("]]>").append("</quantity_real>\r\n");
				//setNodeValue( dom, "quantity_real",qtyStd );
				//valueXmlString.append("<quantity_real isSrvCallOnChg='0'>").append("<![CDATA[").append(qty.get(1)).append("]]>").append("</quantity_real>\r\n");
				//19-01-11 -Chandni Shah
				/* commented on 19-01-11 -Chandni Shah
				valueXmlString.append("<pack_qty isSrvCallOnChg='0'>").append("<![CDATA[").append(quantity / noArt).append("]]>").append("</pack_qty>\r\n");
				setNodeValue( dom, "pack_qty", (quantity / noArt));
				 */
				valueXmlString.append("<pack_qty isSrvCallOnChg='0'>").append("<![CDATA[").append(packQty).append("]]>").append("</pack_qty>\r\n"); 				
				setNodeValue( dom, "pack_qty",packQty);
				//
				valueXmlString.append("<loc_code isSrvCallOnChg='0'>").append("<![CDATA[").append(locCode).append("]]>").append("</loc_code>\r\n");
				setNodeValue( dom, "loc_code", (locCode == null) ? "":locCode );
				valueXmlString.append("<lot_no isSrvCallOnChg='0'>").append("<![CDATA[").append(lotNo).append("]]>").append("</lot_no>\r\n");
				setNodeValue( dom, "lot_no", (lotNo == null) ? "":lotNo );
				if (lotSL.equals(""))
				{
					lotSL = " ";
				}
				valueXmlString.append("<lot_sl isSrvCallOnChg='0'>").append("<![CDATA[").append(lotSL).append("]]>").append("</lot_sl>\r\n");
				setNodeValue( dom, "lot_sl", (lotSL == null) ? "":lotSL );
				valueXmlString.append("<status isSrvCallOnChg='0'>").append("<![CDATA[").append(" ").append("]]>").append("</status>\r\n");
				setNodeValue( dom, "status",  " " );
				System.out.println("@@@@@@1 packInstr["+packInstr+"]::::packInstrStock["+packInstrStock+"]");
				valueXmlString.append("<pack_instr isSrvCallOnChg='0'>").append("<![CDATA[").append((packInstr == null) ? packInstrStock:packInstr).append("]]>").append("</pack_instr>\r\n");
				if(packInstr == null || packInstr.trim().length() == 0)
				{
					sql2 = "select pack_instr from stock where  item_code = ? and "
						+" site_code = ? and  loc_code  = ? and  "	
						+"lot_no = ? and  lot_sl 	= ? ";
					pstmt1 = conn.prepareStatement(sql2);
					pstmt1.setString(1,itemCode);
					pstmt1.setString(2,siteCode);
					pstmt1.setString(3,locCode);
					pstmt1.setString(4,lotNo);
					pstmt1.setString(5,lotSL);

					rs2 = pstmt1.executeQuery();
					if(rs2.next())
					{	
						packInstrStock = rs2.getString(1);
					}
					rs2.close();
					rs2 =null;
					pstmt1.close();
					pstmt1 = null;

				}
				packInstrInvPackRcp = packInstr;
				System.out.println("@@@@@@2 packInstr["+packInstr+"]::::packInstrStock["+packInstrStock+"]::::packInstrInvPackRcp:["+packInstrInvPackRcp+"]");
				setNodeValue( dom, "pack_instr", (packInstr == null) ? packInstrStock:packInstr );
				valueXmlString.append("<gross_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(grossWeight).append("]]>").append("</gross_weight>\r\n");
				setNodeValue( dom, "gross_weight", grossWeight );
				valueXmlString.append("<tare_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(tareWeight).append("]]>").append("</tare_weight>\r\n");
				setNodeValue( dom, "tare_weight", tareWeight );
				valueXmlString.append("<nett_weight isSrvCallOnChg='0'>").append("<![CDATA[").append((grossWeight - tareWeight)).append("]]>").append("</nett_weight>\r\n");
				setNodeValue( dom, "nett_weight", (grossWeight - tareWeight));
				//setNodeValue( dom, "nett_weight", netWeight);
				System.out.println("manohar 15/10/10 no_art from packlist [" + noArt + "]");
				valueXmlString.append("<no_art isSrvCallOnChg='0'>").append("<![CDATA[").append(noArt).append("]]>").append("</no_art>\r\n");
				setNodeValue( dom, "no_art", noArt );
				//valueXmlString.append("<dimension isSrvCallOnChg='0'>").append("<![CDATA[").append((dimension == null) ? "":dimension).append("]]>").append("</dimension>\r\n");
				//setNodeValue( dom, "dimension", (dimension == null) ? "":dimension );
				valueXmlString.append("<tax_class isSrvCallOnChg='0'>").append("<![CDATA[").append((taxClass == null) ? "":taxClass).append("]]>").append("</tax_class>\r\n");
				setNodeValue( dom, "tax_class", (taxClass == null) ? "":taxClass );
				valueXmlString.append("<tax_chap isSrvCallOnChg='0'>").append("<![CDATA[").append((taxChap == null) ? "":taxChap).append("]]>").append("</tax_chap>\r\n");
				setNodeValue( dom, "tax_chap", (taxChap == null) ? "":taxChap );
				valueXmlString.append("<tax_env isSrvCallOnChg='0'>").append("<![CDATA[").append((taxEnv == null) ? "":taxEnv).append("]]>").append("</tax_env>\r\n");
				setNodeValue( dom, "tax_env", (taxEnv == null) ? "":taxEnv );

				valueXmlString.append("<quantity isSrvCallOnChg='0'>").append("<![CDATA[").append(quantity).append("]]>").append("</quantity>\r\n");
				setNodeValue( dom, "quantity",quantity);
				valueXmlString.append("<quantity__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(qtyStd).append("]]>").append("</quantity__stduom>\r\n");
				setNodeValue( dom, "quantity__stduom", qtyStd );
				valueXmlString.append("<conv__qty_stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(conv).append("]]>").append("</conv__qty_stduom>\r\n");
				setNodeValue( dom, "conv__qty_stduom", (conv));
				valueXmlString.append("<quantity_real isSrvCallOnChg='0'>").append("<![CDATA[").append(qtyStd).append("]]>").append("</quantity_real>\r\n");
				valueXmlString.append(getChangeSord(dom , dom1 , "lot_sl", xtraParams , conn));
				valueXmlString.append("<quantity isSrvCallOnChg='0'>").append("<![CDATA[").append(quantity).append("]]>").append("</quantity>\r\n");
				setNodeValue( dom, "quantity",quantity);
				System.out.println("2496 quantity  = "+quantity);
				valueXmlString.append(getChangeSord(dom , dom1 , "quantity", xtraParams , conn));//added by kunal on 02/07/13 for discount amt calculation 
				valueXmlString.append("<quantity__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(qtyStd).append("]]>").append("</quantity__stduom>\r\n");
				setNodeValue( dom, "quantity__stduom", qtyStd );
				valueXmlString.append("<conv__qty_stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(conv).append("]]>").append("</conv__qty_stduom>\r\n");
				setNodeValue( dom, "conv__qty_stduom", (conv) );
				valueXmlString.append("<quantity_real isSrvCallOnChg='0'>").append("<![CDATA[").append(qtyStd).append("]]>").append("</quantity_real>\r\n");

				// Change 07-09-10 

				sql =" select retest_date , mfg_date , exp_date  ,dimension,pallet_wt from stock where  item_code = ? and "
					+" site_code = ? and  loc_code  = ? and  "	
					+"lot_no = ? and  lot_sl 	= ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,itemCode);
				pstmt.setString(2,siteCode);
				pstmt.setString(3,locCode);
				pstmt.setString(4,lotNo);
				pstmt.setString(5,lotSL);
				rs1 = pstmt.executeQuery();
				if(rs1.next())
				{
					retestDate = rs1.getTimestamp(1);
					mfgDate = rs1.getTimestamp(2);
					expDate = rs1.getTimestamp(3);

					dimension = rs1.getString(4) == null?" ":rs1.getString(4);
					palletWt = rs1.getDouble(5);

				}

				rs1.close();
				rs1 =null;
				pstmt.close();
				pstmt = null;

				System.out.println("dimension=="+dimension+"   palletWt="+palletWt);

				//added by kunal on 13/02/13 dimension ,palletWt set from inv_pack_rcp if not in stock
				if(dimension == null || dimension.trim().length() == 0)
				{
					dimension = dimension1;
				}
				if(palletWt == 0)
				{
					palletWt = palletWt1;
				}

				if(retestDate !=null)
				{
					sRetestDate = sdf.format(retestDate).toString();
					valueXmlString.append("<retest_date isSrvCallOnChg='0'>").append("<![CDATA[" + sRetestDate + "]]>").append("</retest_date>\r\n");
					setNodeValue( dom, "retest_date", sRetestDate );
				}
				else
				{
					sRetestDate = "";
					valueXmlString.append("<retest_date isSrvCallOnChg='0'>").append("<![CDATA[" + sRetestDate + "]]>").append("</retest_date>\r\n");
					setNodeValue( dom, "retest_date", sRetestDate );
				}
				if(mfgDate !=null)
				{
					sMfgDate = sdf.format(mfgDate).toString();
					valueXmlString.append("<mfg_date isSrvCallOnChg='0'>").append("<![CDATA[").append(sMfgDate).append("]]>").append("</mfg_date>\r\n");
					setNodeValue( dom, "mfg_date", sMfgDate );
				}
				else
				{
					sMfgDate = "";
					valueXmlString.append("<mfg_date isSrvCallOnChg='0'>").append("<![CDATA[").append(sMfgDate).append("]]>").append("</mfg_date>\r\n");
					setNodeValue( dom, "mfg_date", sMfgDate );
				}
				if(expDate !=null)
				{
					sExpDate = sdf.format(expDate).toString();
					valueXmlString.append("<exp_date isSrvCallOnChg='0'>").append("<![CDATA[").append(sExpDate).append("]]>").append("</exp_date>\r\n");
					setNodeValue( dom, "exp_date", sExpDate);
				}
				else
				{
					sExpDate = "";
					valueXmlString.append("<exp_date isSrvCallOnChg='0'>").append("<![CDATA[").append(sExpDate).append("]]>").append("</exp_date>\r\n");
					setNodeValue( dom, "exp_date", sExpDate);
				}
				// end change 07-09-10
				//added by kunal on 1/02/13 set Dimention , Pallet  Weight .
				valueXmlString.append("<dimension isSrvCallOnChg='0'>").append("<![CDATA[").append(dimension).append("]]>").append("</dimension>\r\n");
				valueXmlString.append("<pallet_wt isSrvCallOnChg='0'>").append("<![CDATA[").append(palletWt).append("]]>").append("</pallet_wt>\r\n");

				valueXmlString.append("</Detail>\r\n");
			}
			valueXmlString.append("</Root>\r\n");
			String retXmlString = genericUtility.serializeDom(genericUtility.parseString(valueXmlString.toString()));
			valueXmlString = null;
			System.out.println("manohar 07/02/11 retXmlString 6 ["+ retXmlString +"]");
			valueXmlString =  new StringBuffer(retXmlString);
		}
		catch (Exception e)
		{
			System.out.println("Exception :[Despatch][packListTransform] :" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		//21-07-10 close connection
		finally
		{
			try
			{
				System.out.println("Closing Connection.....");
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				/*if (stmt != null)
				{
					stmt.close();
					stmt = null;
				}*/
				if (rs1 != null)
				{
					rs1.close();
					rs1 = null;
				}if (rs2 != null)
				{
					rs2.close();
					rs2 = null;
				}
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if (pstmt1 != null)
				{
					pstmt1.close();
					pstmt1 = null;
				}
				if(conn != null)
				{
					conn.close();
					conn = null;
				}
			}catch(Exception e){}
		}
		System.out.println("10/12/10 manohar  valueXmlString.toString() "+valueXmlString.toString());
		return valueXmlString.toString();
	}
	//End Changes Gulzar 23-04-07
	/*--Commented and changes below - Gulzar - 23/01/08
	private String actionDefault(Document dom, Document dom1, String objContext, String xtraParams) throws RemoteException, ITMException
	{
		PreparedStatement pstmt = null;
		String unit1 = "", unitStd1 = "";
		String sordNo = "", locCode = "", lotSl = "", lotNo = "", siteCode = "", errCode = "", errString = "";
		String lineNo = "", expLev = "", itemCode = "", itemCodeOrd = "", itemCodeOld = ""; 
		String sql = "", sql1 = "", sql2 = "", sql3 = "";
		java.util.Date expDate1 = null, chkDate4 = null, chkDate3 = null;
		Statement stmt = null, stmt1 = null, stmt2 = null, stmt3 = null;
		Connection conn = null;
		ResultSet rs = null, rs1 = null, rs2 = null, rs3 = null;
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		double qtyStk = 0d, allocQty = 0d, inputQty = 0d, quantityStduom = 0d, convQtyStduom = 0d;
		double grossPer = 0d,netPer = 0d, tarePer = 0d, qtyStd = 0d;
		double grossWeight = 0d, tareWeight = 0d, netWeight = 0d, conv = 0;
		java.sql.Date expDate = null, mfgDate = null;
		String dimension = "", suppCodeMfg = "", despDt = "", trackShelfLife = "", orderType = "", siteCodeMfg = "";
		int minShelfLife = 0, maxShelfLife = 0, noOfItems = 0, cntItemCode = 0, updateCnt = 0, counter = 0; 
 		ArrayList qtyFact = new ArrayList();
		String locCode1 = "", lotSl1 = "", locGroup = "", lineNoSord = "";
		String chkDate1 = "", chkDate2 = "",detCnt = "0",lineNoOrd = ""; 
		ConnDriver connDriver = new ConnDriver();
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		HashMap qtyMap = new HashMap();
		Double qtyDbl = new Double(0.0);
		double qtyOrd = 0.0, qtyPerArt = 0.00;
		int noArt = 0;
		String updateStatus = "";
		try
		{
			detCnt = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"detCnt"); //Will be Uncommented later - Jiten
			if (detCnt == null || detCnt.trim().length() == 0)
			{
				detCnt = "0";
			}
			if (dom != null)
			{
				updateStatus = getCurrentUpdateFlag(dom);
				System.out.println("updateStatus :: "+updateStatus);
			}
			//Added Changes By Gulzar 28-02-07
			if ((Integer.parseInt(detCnt) > 1) || (!updateStatus.equals("A")))
			{
				errCode = "VTDESPD1";
				errString = itmDBAccess.getErrorString("",errCode,"");
				return errString;
			}
			//if (detCnt.equals("0")) //Gulzar 27-02-07
			else //End Changes Gulzar 28-02-08
			{
				conn = connDriver.getConnectDB("DriverITM");
				stmt = conn.createStatement();
				stmt1 = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
				stmt2 = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
				stmt3 = conn.createStatement();

				sordNo = genericUtility.getColumnValue("sord_no",dom1);	 
				System.out.println("sordNo :"+sordNo);
				SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
				if (sordNo == null && sordNo.trim().length() == 0)
				{
					errCode = "VTPCK1";
					errString = itmDBAccess.getErrorString("",errCode,"","",conn);
					return errString;
				}
				if (dom != null)
				{
					locCode1 = genericUtility.getColumnValue("loc_code",dom);	 
					lotSl1	 = genericUtility.getColumnValue("lot_sl",dom);	 
				}
				siteCode = genericUtility.getColumnValue("site_code",dom1);
				System.out.println("locCode :"+locCode1);
				System.out.println("lotSl :"+lotSl1);
				System.out.println("siteCode :"+siteCode);

				sql = "SELECT NO_OF_ITEMS FROM DOC_NO_ITEMS " 
					 +"WHERE SITE_CODE = '"+siteCode+"' "
					 +"AND MOD_NAME = UPPER('W_DESPATCH')";
				System.out.println("sql :"+sql);					
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					noOfItems = rs.getInt(1);
					System.out.println("noOfItems :"+noOfItems);
				}
				else
				{
					noOfItems = 0;
					System.out.println("noOfItems :"+noOfItems);
				}
				/*-- Commented And Changes Below as There is a Changes in PB logic
				sql ="SELECT SORDDET.SALE_ORDER, "   
						 +"SORDDET.LINE_NO, "   
						 +"SORDDET.SITE_CODE, "   
						 +"SORDDET.ITEM_CODE, "   
						 +"SORDDET.ITEM_FLG, "   
						 +"SORDDET.QUANTITY, "   
						 +"SORDDET.UNIT, "   
						 +"SORDDET.DSP_DATE, "   
						 +"SORDDET.RATE, "   
						 +"SORDDET.DISCOUNT, "   
						 +"SORDDET.TAX_AMT, "   
						 +"SORDDET.TAX_CLASS, "   
						 +"SORDDET.TAX_CHAP, "   
						 +"SORDDET.TAX_ENV, "   
						 +"SORDDET.NET_AMT, "   
						 +"SORDDET.REMARKS, "   
						 +"SORDDET.STATUS, "   
						 +"SORDDET.STATUS_DATE, "   
						 +"SORDDET.CHG_DATE, "   
						 +"SORDDET.CHG_USER, "   
						 +"SORDDET.CHG_TERM, "   
						 +"SORDDET.ITEM_DESCR, "   
						 +"SORDDET.UNIT__RATE, "   
						 +"SORDDET.CONV__QTY_STDUOM, "   
						 +"SORDDET.CONV__RTUOM_STDUOM, "   
						 +"SORDDET.UNIT__STD, "   
						 +"SORDDET.QUANTITY__STDUOM, "   
						 +"SORDDET.RATE__STDUOM, "   
						 +"SORDDET.NO_ART, "   
						 +"SORDDET.PACK_CODE, "   
						 +"SORDDET.LINE_NO__CONTR, "   
						 +"SORDDET.PACK_INSTR, "   
						 +"SORDDET.SPEC_REF, "   
						 +"SORDDET.PACK_QTY, "   
						 +"SORDDET.ITEM_SER, "   
						 +"SORDDET.RATE__CLG, "   
						 +"SORDDET.MFG_CODE, "   
						 +"SORDDET.CONTRACT_NO, "   
						 +"PACKING.DESCR, "   
						 +"SORDDET.SPEC_ID, "   
						 +"SORDDET.ORD_VALUE, "   
						 +"SORDDET.ITEM_SER__PROM, "   
						 +"SORDDET.SPECIFIC_INSTR, "   
						 +"SORDDET.ITEM_CODE__ORD, "   
						 +"SORDDET.PALLET_CODE, "   
						 +"SORDDET.NO_PALLET, "   
						 +"SORDDET.OVER_SHIP_PERC, "   
						 +"SORDDET.COMM_PERC_1, "   
						 +"SORDDET.COMM_PERC_2, "   
						 +"SORDDET.COMM_PERC_3, "   
						 +"SORDDET.COMM_PERC_ON_1, "   
						 +"SORDDET.COMM_PERC_ON_2, "   
						 +"SORDDET.COMM_PERC_ON_3, "   
						 +"SORDDET.SALES_PERS_COMM_1, "   
						 +"SORDDET.SALES_PERS_COMM_2, "   
						 +"SORDDET.SALES_PERS_COMM_3, "   
						 +"SORDDET.PRICE_LIST__DISC, "   
						 +"SORDDET.RATE__STD, "   
						 +"SORDER.FIN_SCHEME, "   
						 +"SORDDET.MAX_SHELF_LIFE, "   
						 +"SORDDET.MIN_SHELF_LIFE, "   
						 +"SPACE(250) AS ITEM_SPECS, "   
						 +"SPACE(250) AS ST_SHRINK, "   
						 +"SORDDET.LOC_TYPE, "   
						 +"SPACE(250) AS ST_SCHEME,  "
						 +"SORDDET.SORDFORM_NO ,  "  
						 +"SORDDET.LINE_NO__SFORM ,  " 
						 +"FN_GET_ITMSTK(SORDDET.ITEM_CODE,SORDDET.SITE_CODE) STK_QTY,  "  
						 +"SORDDET.APPL_SEG ,  "  
						 +"UOM.DESCR  "  
				 +"FROM SORDDET, PACKING, SORDER  "
				 +"WHERE SORDDET.PACK_CODE = PACKING.PACK_CODE " 
				 +"AND SORDER.SALE_ORDER = SORDDET.SALE_ORDER "
				 +"AND SORDDET.SALE_ORDER = '"+sordNo+"'";
				 // End Changes Gulzar 12/03/07
				 //Added By Gulzar 12/03/07 - Added join with SORDITEM as the default was not working  For Scheme item
				 sql ="SELECT SORDDET.SALE_ORDER, "   
						 +"SORDDET.LINE_NO, "   
						 +"SORDDET.SITE_CODE, "   
					//	 +"SORDDET.ITEM_CODE, " //Gulzar 14/03/07 - As it should take from SORDITEM
						 +"SORDITEM.ITEM_CODE, " //Gulzar 14/03/07
						 +"SORDDET.ITEM_FLG, "   
					//	 +"SORDDET.QUANTITY, "   //Gulzar 14/03/07 - As it should take from SORDITEM
						 +"SORDITEM.QUANTITY, "  //Gulzar 14/03/07
						 +"SORDDET.UNIT, "   
						 +"SORDDET.DSP_DATE, "   
						 +"SORDDET.RATE, "   
						 +"SORDDET.DISCOUNT, "   
						 +"SORDDET.TAX_AMT, "   
						 +"SORDDET.TAX_CLASS, "   
						 +"SORDDET.TAX_CHAP, "   
						 +"SORDDET.TAX_ENV, "   
						 +"SORDDET.NET_AMT, "   
						 +"SORDDET.REMARKS, "   
						 +"SORDDET.STATUS, "   
						 +"SORDDET.STATUS_DATE, "   
						 +"SORDDET.CHG_DATE, "   
						 +"SORDDET.CHG_USER, "   
						 +"SORDDET.CHG_TERM, "   
						 +"SORDDET.ITEM_DESCR, "   
						 +"SORDDET.UNIT__RATE, "   
						 +"SORDDET.CONV__QTY_STDUOM, "   
						 +"SORDDET.CONV__RTUOM_STDUOM, "   
						 +"SORDDET.UNIT__STD, "   
						 +"SORDDET.QUANTITY__STDUOM, "   
						 +"SORDDET.RATE__STDUOM, "   
						 +"SORDDET.NO_ART, "   
						 +"SORDDET.PACK_CODE, "   
						 +"SORDDET.LINE_NO__CONTR, "   
						 +"SORDDET.PACK_INSTR, "   
						 +"SORDDET.SPEC_REF, "   
						 +"SORDDET.PACK_QTY, "   
						 +"SORDDET.ITEM_SER, "   
						 +"SORDDET.RATE__CLG, "   
						 +"SORDDET.MFG_CODE, "   
						 +"SORDDET.CONTRACT_NO, "   
						 +"PACKING.DESCR, "   
						 +"SORDDET.SPEC_ID, "   
						 +"SORDDET.ORD_VALUE, "   
						 +"SORDDET.ITEM_SER__PROM, "   
						 +"SORDDET.SPECIFIC_INSTR, "   
					//	 +"SORDDET.ITEM_CODE__ORD, "   //Gulzar 14/03/07 - As it should take from SORDITEM
						 +"SORDITEM.ITEM_CODE__ORD, "  //Gulzar 14/03/07
						 +"SORDDET.PALLET_CODE, "   
						 +"SORDDET.NO_PALLET, "   
						 +"SORDDET.OVER_SHIP_PERC, "   
						 +"SORDDET.COMM_PERC_1, "   
						 +"SORDDET.COMM_PERC_2, "   
						 +"SORDDET.COMM_PERC_3, "   
						 +"SORDDET.COMM_PERC_ON_1, "   
						 +"SORDDET.COMM_PERC_ON_2, "   
						 +"SORDDET.COMM_PERC_ON_3, "   
						 +"SORDDET.SALES_PERS_COMM_1, "   
						 +"SORDDET.SALES_PERS_COMM_2, "   
						 +"SORDDET.SALES_PERS_COMM_3, "   
						 +"SORDDET.PRICE_LIST__DISC, "   
						 +"SORDDET.RATE__STD, "   
						 +"SORDER.FIN_SCHEME, "   
						 +"SORDDET.MAX_SHELF_LIFE, "   
						 +"SORDDET.MIN_SHELF_LIFE, "   
						 +"SPACE(250) AS ITEM_SPECS, "   
						 +"SPACE(250) AS ST_SHRINK, "   
						 +"SORDDET.LOC_TYPE, "   
						 +"SPACE(250) AS ST_SCHEME,  "
						 +"SORDDET.SORDFORM_NO ,  "  
						 +"SORDDET.LINE_NO__SFORM ,  "  
						 +"FN_GET_ITMSTK(SORDDET.ITEM_CODE,SORDDET.SITE_CODE) STK_QTY,  "  
						 +"SORDDET.APPL_SEG ,  "  
						 +"UOM.DESCR,  "
						 +"SORDITEM.EXP_LEV, " //Gulzar 14/03/07
						 +"SORDITEM.QTY_ALLOC  " //Gulzar 14/03/07
				 +"FROM SORDDET LEFT OUTER JOIN PACKING ON SORDDET.PACK_CODE = PACKING.PACK_CODE LEFT OUTER JOIN UOM ON SORDDET.UNIT = UOM.UNIT,  "
				 +"SORDER, "
				 +"SORDITEM  "
				 +"WHERE SORDER.SALE_ORDER = SORDDET.SALE_ORDER "
				 +"AND SORDITEM.SALE_ORDER = SORDDET.SALE_ORDER "
				 +"AND SORDITEM.LINE_NO = SORDDET.LINE_NO "
				 +"AND SORDITEM.SALE_ORDER = '"+sordNo+"'"
				 +"AND SORDITEM.LINE_TYPE = 'I' ";
				 //End Changes Gulzar 12/03/07
				 System.out.println("sql :"+sql);
				 rs = stmt.executeQuery(sql);
				 while (rs.next())
				 {
					if (noOfItems > 0 && counter >= noOfItems)
					{	
						errCode = "VTITMGRZRO";
						errString = itmDBAccess.getErrorString("",errCode,"","",conn);
						return errString;
					}
					lotSl = lotSl1; // Re-set with Picked-up from DOM.
					locCode = locCode1; // Re-set with Picked-up from DOM.
					//lineNo = rs.getString("LINE_NO");
					lineNoOrd = rs.getString("LINE_NO");
					System.out.println("lineNo :"+lineNoOrd);
					siteCode = genericUtility.getColumnValue("site_code",dom1);	
					//lineNoOrd = genericUtility.getColumnValue("line_no__sord",dom); //Commented as line_no from despatch is to be used
					System.out.println("siteCode :"+siteCode);

					/*-- Commented By Gulzar 14/03/07 - As the same is retrived from main query
					sql1 = "SELECT ITEM_CODE__ORD, QUANTITY,EXP_LEV, ITEM_CODE, QTY_ALLOC "
						  +"FROM SORDITEM WHERE SALE_ORDER = '"+sordNo+"' " // sordNo of Header is Used whereas in PB it taken from detail
						  +"AND LINE_NO = '"+lineNoOrd+"'"+" AND LINE_TYPE = 'I'"; 
					System.out.println("sql1 :"+sql1);
					rs1 = stmt1.executeQuery(sql1);
					if (rs1.next())
					{
						itemCodeOrd = rs1.getString(1);
						System.out.println("itemCodeOrd :"+itemCodeOrd);
						expLev = rs1.getString(3);
						System.out.println("expLev :"+expLev);
						itemCode = rs1.getString(4);
						System.out.println("itemCode :"+itemCode);
					}
					stmt1.close();
					stmt1 = null;
					//End Comment Gulzar 14/03/07

					//Added By Gulzar 14/03/07
					itemCodeOrd = rs.getString("ITEM_CODE__ORD");
					System.out.println("itemCodeOrd :"+itemCodeOrd);
					expLev = rs.getString("EXP_LEV");
					System.out.println("expLev :"+expLev);
					itemCode = rs.getString("ITEM_CODE");
					System.out.println("itemCode :"+itemCode);
					//End changes Gulzar 14/03/07

					sql1 = "SELECT UNIT__STD, CONV__QTY_STDUOM, UNIT, PACK_INSTR, QUANTITY__STDUOM, "
						  +"CASE WHEN NO_ART IS NULL THEN 0 ELSE NO_ART END "
						  +"FROM SORDDET WHERE SALE_ORDER = '"+sordNo+"'"+" AND LINE_NO ='"+lineNoOrd+"'";
					System.out.println("sql1 :"+sql1);
					stmt1 = conn.createStatement();
					rs1 = stmt1.executeQuery(sql1);
					if (rs1.next())
					{
						unit1 = rs1.getString("UNIT");
						unitStd1 = rs1.getString("UNIT__STD");
						convQtyStduom = rs1.getDouble(2);
						System.out.println("convQtyStduom :"+convQtyStduom);
						quantityStduom = rs1.getDouble(5);
						qtyOrd = quantityStduom;
						System.out.println("quantityStduom :"+quantityStduom);
					}
					stmt1.close();
					stmt1 = null;
					if (quantityStduom <= 0)
					{
						continue;
					}
					sql1 = "SELECT LOC_CODE,QUANTITY, EXP_LEV, ITEM_CODE, "
						  +"LOT_NO, LOT_SL, UNIT__STD, CONV__QTY_STDUOM, UNIT "
						  +"FROM SORDALLOC WHERE SALE_ORDER ='"+sordNo+"'"+" AND LINE_NO ='"+lineNoOrd+"'";
					System.out.println("sql1 :"+sql1);
					stmt1 = conn.createStatement();
					rs1 = stmt1.executeQuery(sql1);
					if (rs1.next())
					{
						if (locCode == null || locCode.trim().length() == 0)
						{
							//locCode = rs.getString(1); //Commented - Gulzar - 14/03/07 - As it should take from rs1 not rs 
							locCode = rs1.getString(1);
						}						
						System.out.println("locCode :"+locCode);
						lotNo = rs1.getString(5);
						System.out.println("lotNo :"+lotNo);
						lotSl = rs1.getString(6);
						System.out.println("lotSl :"+lotSl);
					}					
					stmt1.close();
					stmt1 = null;
					sql1 = "SELECT SORDALLOC.LOT_NO, "   
							 +"SORDALLOC.LOT_SL, "   
							 +"SORDALLOC.QTY_ALLOC, "   
							 +"SORDALLOC.DATE_ALLOC, "   
							 +"SORDALLOC.STATUS, "   
							 +"SORDALLOC.ITEM_GRADE, "   
							 +"SORDALLOC.EXP_DATE, "   
							 +"SORDALLOC.ALLOC_MODE, "   
							 +"SORDALLOC.SITE_CODE, "   
							 +"SORDALLOC.LOC_CODE, "   
							 +"SORDALLOC.SALE_ORDER, "   
							 +"SORDALLOC.LINE_NO, "   
							 +"SORDALLOC.EXP_LEV, "   
							 +"SORDALLOC.ITEM_CODE__ORD, "   
							 +"SORDALLOC.ITEM_CODE, "   
							 +"SORDALLOC.ITEM_REF, "   
							 +"SORDALLOC.QUANTITY, "   
							 +"SORDALLOC.UNIT, "   
							 +"LOCATION.DESCR, "   
							 +"SORDALLOC.CONV__QTY_STDUOM, "   
							 +"SORDALLOC.UNIT__STD, "   
							 +"SORDALLOC.QUANTITY__STDUOM, "   
							 +"SORDALLOC.MFG_DATE, "   
							 +"SORDALLOC.SITE_CODE__MFG "   
						+"FROM SORDALLOC, LOCATION "  
						+"WHERE SORDALLOC.LOC_CODE = LOCATION.LOC_CODE "
						+"AND SORDALLOC.SALE_ORDER = '"+sordNo+"' "
						+"AND SORDALLOC.LINE_NO = '"+lineNoOrd+"' "
						+"AND SORDALLOC.EXP_LEV = '"+expLev+"' "
						+"AND SORDALLOC.ITEM_CODE__ORD = '"+itemCodeOrd+"' "
						+"AND SORDALLOC.ITEM_CODE = '"+itemCode+"' "
						+"AND CASE WHEN SORDALLOC.STATUS IS NULL THEN ' ' ELSE SORDALLOC.STATUS END  <> 'D' ";
					System.out.println("sql1 :"+sql1);
					stmt1 = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
					rs1 = stmt1.executeQuery(sql1);
					if(!rs1.next())
					{
						if(locCode == null || locCode.trim().length() == 0)
						{
							locCode = "%";
							System.out.println("locCode :"+locCode);
						}
						else
						{
							locCode = locCode.trim() + "%";
							System.out.println("locCode :"+locCode);
						}
						if (lotSl == null || lotSl.trim().length() == 0)
						{
							lotSl = "%";
							System.out.println("lotSl :"+lotSl);
						}
						else
						{
							lotSl = lotSl.trim() + "%";
							System.out.println("lotSl :"+lotSl);

						}
						sql2 = "SELECT LOC_GROUP FROM SORDER "
								+"WHERE SALE_ORDER ='"+sordNo+"'";
						System.out.println("sql2 :"+sql2);
						rs2 = stmt2.executeQuery(sql2);
						if (rs2.next())
						{
							locGroup = rs2.getString(1);
							System.out.println("locGroup :"+locGroup);
						}
						if (locGroup == null || locGroup.trim().length() == 0)
						{
							//sql2 = "SELECT COUNT(A.ITEM_CODE) FROM STOCK A, INVSTAT B ,LOCATION C "
							sql2 = "SELECT COUNT(*) FROM STOCK A, INVSTAT B ,LOCATION C "
										+"WHERE A.INV_STAT  = B.INV_STAT "
										+"AND C.LOC_CODE = A.LOC_CODE "
										+"AND A.ITEM_CODE = '"+itemCode+"' "
										+"AND A.SITE_CODE = '"+siteCode+"' "
										+"AND A.LOC_CODE  LIKE '"+locCode+"' "
										+"AND A.LOT_SL    LIKE '"+lotSl+"' "
										+"AND B.AVAILABLE = 'Y' "
										+"AND B.USABLE = 'Y' "
										+"AND A.QUANTITY  > 0 ";
							System.out.println("sql2 :"+sql2);
						}
						else
						{
							//sql2 = "SELECT COUNT(A.ITEM_CODE) FROM STOCK A, INVSTAT B ,LOCATION C "
							sql2 = "SELECT COUNT(*) FROM STOCK A, INVSTAT B ,LOCATION C "
										+"WHERE A.INV_STAT  = B.INV_STAT "
										+"AND C.LOC_CODE = A.LOC_CODE "
										+"AND C.LOC_GROUP = '"+locGroup+"' "
										+"AND A.ITEM_CODE = '"+itemCode+"' "
										+"AND A.SITE_CODE = '"+siteCode+"' "
										+"AND A.LOC_CODE  LIKE '"+locCode+"' "
										+"AND A.LOT_SL    LIKE '"+lotSl+"' " 
										+"AND B.AVAILABLE = 'Y' "
										+"AND B.USABLE = 'Y' "
										+"AND A.QUANTITY  > 0 "; 
							System.out.println("sql2 :"+sql2);
						}
						rs2 = stmt2.executeQuery(sql2);
						if (rs2.next())
						{
							cntItemCode = rs2.getInt(1);
							System.out.println("cntItemCode :"+cntItemCode);
						}
						if (cntItemCode == 0)
						{
							continue;
						}
						else
						{
							if(itemCodeOld.indexOf(itemCode) == -1)
							{
								counter = counter + 1;
								System.out.println("Counter :"+counter);
							}
						}
						if (locGroup == null || locGroup.trim().length() == 0)
						{
							sql2 = "SELECT A.LOT_NO,A.LOT_SL, "
										+"A.QUANTITY,A.EXP_DATE, " 
										+"A.SITE_CODE__MFG, "
										+"A.MFG_DATE, "
										+"A.ALLOC_QTY, "
										+"A.PACK_CODE, " 
										+"A.LOC_CODE, "
										+"A.GROSS_WEIGHT, "
										+"A.TARE_WEIGHT, "
										+"A.NET_WEIGHT, " 
										+"A.DIMENSION, "
										+"A.SUPP_CODE__MFG, " 
										+"A.QTY_PER_ART "
								   +"FROM STOCK A,INVSTAT B " 
										+"WHERE A.INV_STAT = B.INV_STAT " 
										+"AND A.ITEM_CODE = '"+itemCode+"' "  
										+"AND A.SITE_CODE = '"+siteCode+"' "  
										+"AND A.LOC_CODE LIKE '"+locCode+"' "  
										+"AND A.LOT_SL LIKE '"+lotSl+"' "   
										+"AND B.AVAILABLE = 'Y' " 
										+"AND B.USABLE = 'Y' " 
										+"AND A.QUANTITY - A.ALLOC_QTY > 0 " 
										+"ORDER BY A.EXP_DATE,A.CREA_DATE, A.LOT_NO, A.LOT_SL ";
							System.out.println("sql2 :"+sql2);
						}
						else
						{
							sql2 = "SELECT A.LOT_NO, "
										+"A.LOT_SL, "
										+"A.QUANTITY, "
										+"A.EXP_DATE, " 
										+"A.SITE_CODE__MFG, "
										+"A.MFG_DATE, "
										+"A.ALLOC_QTY, "
										+"A.PACK_CODE, " 
										+"A.LOC_CODE, "
										+"A.GROSS_WEIGHT, "
										+"A.TARE_WEIGHT, "
										+"A.NET_WEIGHT, " 
										+"A.DIMENSION, "
										+"A.SUPP_CODE__MFG, " 
										+"A.QTY_PER_ART "
									+"FROM STOCK A,INVSTAT B,LOCATION C " 
										+"WHERE A.INV_STAT = B.INV_STAT " 
										+"AND C.LOC_CODE = A.LOC_CODE " 
										+"AND C.LOC_GROUP = '"+locGroup+"' " 
										+"AND A.ITEM_CODE = '"+itemCode+"' " 
										+"AND A.SITE_CODE = '"+siteCode+"' " 
										+"AND A.LOC_CODE  LIKE '"+locCode+"' " 
										+"AND A.LOT_SL    LIKE '"+lotSl+"' "   
										+"AND B.AVAILABLE = 'Y' " 
										+"AND B.USABLE = 'Y' " 
										+"AND A.QUANTITY - A.ALLOC_QTY > 0 " 
										+"ORDER BY A.EXP_DATE, A.CREA_DATE, A.LOT_NO, A.LOT_SL ";
							System.out.println("sql2 :"+sql2);
						}
						rs2 = stmt2.executeQuery(sql2);
						if (!rs2.next())
						{
							continue;
						}
						else
						{
							rs2.beforeFirst();
							while(rs2.next())
							{
								lotNo = rs2.getString(1);
								System.out.println("lotNo :"+lotNo);
								lotSl = rs2.getString(2);
								System.out.println("lotSl :"+lotSl);
								qtyStk = rs2.getDouble(3);
								System.out.println("qtyStk :"+qtyStk);
								expDate = rs2.getDate(4);
								System.out.println("expDate :"+expDate);
								siteCodeMfg = rs2.getString(5);
								System.out.println("siteCodeMfg :"+siteCodeMfg);
								mfgDate = rs2.getDate(6);
								System.out.println("mfgDate :"+mfgDate);
								allocQty = rs2.getDouble(7);
								System.out.println("allocQty :"+allocQty);
								locCode = rs2.getString(9);
								System.out.println("locCode :"+locCode);
								grossWeight = rs2.getDouble(10);
								System.out.println("grossWeight :"+grossWeight);
								tareWeight = rs2.getDouble(11);
								System.out.println("tareWeight :"+tareWeight);
								netWeight = rs2.getDouble(12);
								System.out.println("netWeight :"+netWeight);
								dimension = rs2.getString(13);
								System.out.println("dimension :"+dimension);
								suppCodeMfg = rs2.getString(14);
								System.out.println("suppCodeMfg :"+suppCodeMfg);
								despDt = genericUtility.getColumnValue("desp_date",dom1);	
								System.out.println("despDt :"+despDt);
								qtyPerArt = rs2.getDouble(15);

								sql3 = "SELECT CASE WHEN TRACK_SHELF_LIFE IS NULL THEN 'N' ELSE TRACK_SHELF_LIFE END " 
									  +"FROM ITEM WHERE ITEM_CODE = '"+itemCode+"' ";
								System.out.println("sql3  :"+sql3);
								rs3 = stmt3.executeQuery(sql3);
								if (rs3.next())
								{
									trackShelfLife = rs3.getString(1);
									System.out.println("trackShelfLife :"+trackShelfLife);
								}
								if (trackShelfLife.equals("Y"))
								{
									sql3 = "SELECT ORDER_TYPE FROM SORDER WHERE SALE_ORDER = '"+sordNo+"'";
									System.out.println("sql3 :"+sql3);
									rs3 = stmt3.executeQuery(sql3);
									if (rs3.next())
									{
										orderType = rs3.getString(1);
										System.out.println("orderType :"+orderType);
									}
									if (orderType.equals("NE"))
									{
										sql3 = "SELECT MIN_SHELF_LIFE,MAX_SHELF_LIFE FROM SORDITEM "
											  +"WHERE SALE_ORDER = '"+sordNo+"' "
											  +"AND LINE_NO = '"+lineNoOrd+"' "
											  +"AND EXP_LEV = '"+expLev+"'";
										System.out.println("sql3 :"+sql3);
										rs3 = stmt3.executeQuery(sql3);
										if (rs3.next())
										{
											minShelfLife = rs3.getInt(1);
											System.out.println("minShelfLife :"+minShelfLife);
											maxShelfLife = rs3.getInt(2);
											System.out.println("maxShelfLife :"+maxShelfLife);
										}

										chkDate1 = calcExpiry(despDt, minShelfLife + 1);
										System.out.println("chkDate1 :"+chkDate1);

										chkDate3 = sdf.parse(chkDate1);
										System.out.println("chkDate3 :"+chkDate3);

										chkDate2 = calcExpiry(despDt, maxShelfLife);
										System.out.println("chkDate2 :"+chkDate2);

										chkDate4 = sdf.parse(chkDate2);
										System.out.println("chkDate4 :"+chkDate4);
										if (expDate != null)
										{
											expDate1 = new java.util.Date(expDate.getTime());
											System.out.println("expDate1 :"+expDate1);

											if (!(expDate1.compareTo(chkDate3) >= 0 && expDate1.compareTo(chkDate4) <= 0))  
											{
												continue;
											}
										}										
									}
									else
									{
										sql3 = "SELECT MIN_SHELF_LIFE FROM SORDITEM "
												+"WHERE SALE_ORDER = '"+sordNo+"' " 
												+"AND LINE_NO = '"+lineNoOrd+"' " 
												+"AND EXP_LEV = '"+expLev+"'";
										System.out.println("sql3 :"+sql3);
										rs3 = stmt3.executeQuery(sql3);
										if (rs3.next())
										{
											minShelfLife = rs3.getInt(1);
											System.out.println("minShelfLife :"+minShelfLife);
										}
										chkDate1 = calcExpiry(despDt, minShelfLife);
										System.out.println("chkDate1 :"+chkDate1);

										chkDate4 = sdf.parse(chkDate1);
										System.out.println("chkDate4 :"+chkDate4);
										if (expDate != null)
										{
											expDate1 = new java.util.Date(expDate.getTime());
											System.out.println("expDate1 :"+expDate1);

											if (chkDate4.compareTo(expDate1) > 0) 
											{
												continue;
											}
										}										
									}//end else
								}//end if
								//Below section not present in PB code
								String qtyKeyStr = itemCode + siteCode + locCode + lotNo + lotSl;
								if(qtyMap.containsKey(qtyKeyStr))
								{
									qtyDbl = (Double)qtyMap.get(qtyKeyStr);
								}
								//qtyDbl = subtraction to be done on qtyDbl.doubleValue();
								//qtyMap.put(qtyKeyStr, new Double(subtracted value);
								allocQty = allocQty + qtyDbl.doubleValue();
								if (qtyStk - allocQty <= 0)
								{
									continue;
								}
								//End Section
								if (qtyStk - allocQty <= quantityStduom)
								{
									inputQty = qtyStk - allocQty;
									System.out.println("inputQty :"+inputQty);
									quantityStduom = quantityStduom - inputQty;
									System.out.println("quantityStduom :"+quantityStduom);
								}
								else
								{
									inputQty = quantityStduom;
									System.out.println("inputQty :"+inputQty);
									quantityStduom = quantityStduom - inputQty;
									System.out.println("quantityStduom :"+quantityStduom);
								}

								if (inputQty > 0)
								{
									if (qtyStk > 0)
									{
										grossPer = (grossWeight / qtyStk) ;
										System.out.println("grossPer :"+grossPer);
										netPer 	=  (netWeight / qtyStk);
										System.out.println("netPer 	:"+netPer);
										tarePer	=  (tareWeight / qtyStk);
										System.out.println("tarePer	:"+tarePer);

										grossWeight = (inputQty * grossPer);
										System.out.println("grossWeight :"+grossWeight);
										netWeight = (inputQty * netPer);
										System.out.println("netWeight :"+netWeight);
										tareWeight = (inputQty * tarePer);
										System.out.println("tareWeight :"+tareWeight);	
									}
									/*valueXmlString.append("<Detail>\r\n");
									valueXmlString.append("<loc_code>").append("<![CDATA[").append(locCode.trim()).append("]]>").append("</loc_code>\r\n");
									valueXmlString.append("</Detail>\r\n");
									//End Comment

									//unit1 = genericUtility.getColumnValue("unit",dom);	
									System.out.println("unit :"+unit1);
									//unitStd1 = genericUtility.getColumnValue("unit__std",dom);	
									System.out.println("unitStd :"+unitStd1);

									qtyStd = inputQty;
									System.out.println("qtyStd :"+qtyStd);
									if (!unit1.equals(unitStd1))
									//if (unit1 == null)
									{
										System.out.println("Calling getConvQuantity...........");
										System.out.println("unitStd1 :"+unitStd1+" \nunit1 :"+unit1+" \nitemCode :"+itemCode+" \nqtyStd :"+qtyStd+" \nconv :"+conv); 
										qtyFact = itmDBAccess.getConvQuantityFact(unitStd1, unit1, itemCode, qtyStd, conv, conn);
										System.out.println("qtyFact.get(1) :"+qtyFact.get(1));
										System.out.println("qtyFact.get(1) :"+qtyFact.get(2));
									}
									else
									{
										qtyFact.add(Integer.toString(1));
										qtyFact.add(Double.toString(qtyStd));
									}
									System.out.println("qtyFact.size() :"+qtyFact.size());
									conv = (Double.parseDouble(qtyFact.get(0).toString()));
									noArt = new Double((Double.parseDouble(qtyFact.get(1).toString())) / qtyPerArt).intValue();
									System.out.println("conv :"+conv);
									valueXmlString.append("<Detail>\r\n");
									valueXmlString.append("<line_no__sord isSrvCallOnChg='1'>").append("<![CDATA[").append(lineNoOrd.trim()).append("]]>").append("</line_no__sord>\r\n");
									valueXmlString.append("<item_code isSrvCallOnChg='0'>").append("<![CDATA[").append(itemCode.trim()).append("]]>").append("</item_code>\r\n");
									valueXmlString.append("<loc_code isSrvCallOnChg='1'>").append("<![CDATA[").append(locCode.trim()).append("]]>").append("</loc_code>\r\n"); //made isSrvCallOnChg='1' - Gulzar 14/03/07 - For seting of mfg_date and exp_date
									valueXmlString.append("<quantity isSrvCallOnChg='1'>").append("<![CDATA[").append(qtyFact.get(1).toString()).append("]]>").append("</quantity>\r\n");  //made isSrvCallOnChg='1'  - Gulzar 14/03/07
									valueXmlString.append("<exp_lev isSrvCallOnChg='1'>").append("<![CDATA[").append(expLev.trim()).append("]]>").append("</exp_lev>\r\n"); //Un-Commented and made isSrvCallOnChg='1' - Gulzar 14/03/07
									//valueXmlString.append("<quantity_real isSrvCallOnChg='0'>").append("<![CDATA[").append(qtyStd).append("]]>").append("</quantity_real>\r\n");
									//valueXmlString.append("<quantity__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(qtyStd).append("]]>").append("</quantity__stduom>\r\n");
									//valueXmlString.append("<conv__qty_stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(1 / conv).append("]]>").append("</conv__qty_stduom>\r\n");
									valueXmlString.append("<pending_qty isSrvCallOnChg='0'>").append("<![CDATA[").append(qtyOrd).append("]]>").append("</pending_qty>\r\n");
									valueXmlString.append("<lot_no isSrvCallOnChg='1'>").append("<![CDATA[").append(lotNo.trim()).append("]]>").append("</lot_no>\r\n");
									valueXmlString.append("<lot_sl isSrvCallOnChg='1'>").append("<![CDATA[").append(lotSl.trim()).append("]]>").append("</lot_sl>\r\n");
									valueXmlString.append("<gross_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(grossWeight).append("]]>").append("</gross_weight>\r\n");
									valueXmlString.append("<nett_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(netWeight).append("]]>").append("</nett_weight>\r\n");
									valueXmlString.append("<tare_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(tareWeight).append("]]>").append("</tare_weight>\r\n");
									valueXmlString.append("<no_art isSrvCallOnChg='0'>").append("<![CDATA[").append(noArt).append("]]>").append("</no_art>\r\n");
									//valueXmlString.append("<dimension isSrvCallOnChg='0'>").append("<![CDATA[").append(dimension).append("]]>").append("</dimension>\r\n"); //Gulzar 14/03/07
									valueXmlString.append("<dimension isSrvCallOnChg='0'>").append("<![CDATA[").append((dimension == null) ? "":dimension).append("]]>").append("</dimension>\r\n"); //Gulzar 14/02/07
									valueXmlString.append("<site_code__mfg isSrvCallOnChg='0'>").append("<![CDATA[").append(siteCodeMfg).append("]]>").append("</site_code__mfg>\r\n");
	//								valueXmlString.append("<mfg_date>").append("<![CDATA[").append(mfgDate).append("]]>").append("</mfg_date>\r\n");
	//								valueXmlString.append("<expDate>").append("<![CDATA[").append(mfgDate).append("]]>").append("</expDate>\r\n");
									valueXmlString.append("</Detail>\r\n");
									qtyOrd = qtyOrd - qtyStd;
									grossWeight = 0;
									tareWeight = 0;
									netWeight = 0;

									allocQty = allocQty + qtyStd;
									qtyMap.put(qtyKeyStr, new Double(qtyDbl.doubleValue() + qtyStd));
									qtyFact.clear();//Added on 24/01/06
								}
	/*							if(wf_val_data)
								{
								//Here is some code is to be ask to Jiten
								}
	//End Comment
							}
						}
					}
					else
					{
						if(itemCodeOld.indexOf(itemCode) == -1)
						{
							counter = counter + 1;
						}
						rs1.beforeFirst();
						while(rs1.next())
						{
							inputQty = rs1.getDouble("QTY_ALLOC");
							System.out.println("inputQty :"+inputQty);
							if (inputQty > 0)
							{
								expLev = rs1.getString("exp_lev");
								System.out.println("expLev :"+expLev);
								itemCodeOrd	= rs1.getString("item_code__ord");
								System.out.println("itemCodeOrd :"+itemCodeOrd);
								itemCode = rs1.getString("item_code");
								System.out.println("itemCode :"+itemCode);
								locCode = rs1.getString("loc_code");
								System.out.println("locCode :"+locCode);
								lotNo	= rs1.getString("lot_no");
								System.out.println("lotNo :"+lotNo);
								lotSl	= rs1.getString("lot_sl");
								System.out.println("lotSl :"+lotSl);
								mfgDate =	rs1.getDate("mfg_date");
								System.out.println("mfgDate :"+mfgDate);
								expDate =	rs1.getDate("exp_date");
								System.out.println("expDate :"+expDate);
								siteCodeMfg = rs1.getString("site_code__mfg");
								System.out.println("siteCodeMfg :"+siteCodeMfg);

								valueXmlString.append("<Detail>\r\n");
								valueXmlString.append("<line_no__sord isSrvCallOnChg='1'>").append("<![CDATA[").append(lineNoOrd.trim()).append("]]>").append("</line_no__sord>\r\n");
								valueXmlString.append("<item_code isSrvCallOnChg='0'>").append("<![CDATA[").append(itemCode.trim()).append("]]>").append("</item_code>\r\n");
								valueXmlString.append("<exp_lev isSrvCallOnChg='1'>").append("<![CDATA[").append(expLev.trim()).append("]]>").append("</exp_lev>\r\n"); //Un-Commented and made isSrvCallOnChg='1' - Gulzar 15/03/07
								valueXmlString.append("<loc_code isSrvCallOnChg='1'>").append("<![CDATA[").append(locCode.trim()).append("]]>").append("</loc_code>\r\n"); //Made isSrvCallOnChg='1' - Gulzar 15/03/07 - For seting of mfg_date and exp_date
								valueXmlString.append("<quantity isSrvCallOnChg='1'>").append("<![CDATA[").append(inputQty).append("]]>").append("</quantity>\r\n");
								valueXmlString.append("<quantity_real isSrvCallOnChg='0'>").append("<![CDATA[").append(inputQty).append("]]>").append("</quantity_real>\r\n");
								valueXmlString.append("<quantity__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(inputQty * convQtyStduom).append("]]>").append("</quantity__stduom>\r\n");
								valueXmlString.append("<lot_no isSrvCallOnChg='1'>").append("<![CDATA[").append(lotNo.trim()).append("]]>").append("</lot_no>\r\n");
								valueXmlString.append("<lot_sl isSrvCallOnChg='1'>").append("<![CDATA[").append(lotSl.trim()).append("]]>").append("</lot_sl>\r\n");
								valueXmlString.append("<site_code__mfg isSrvCallOnChg='0'>").append("<![CDATA[").append(siteCodeMfg).append("]]>").append("</site_code__mfg>\r\n");
	//							valueXmlString.append("<mfg_date>").append("<![CDATA[").append(mfgDate).append("]]>").append("</mfg_date>\r\n");
	//							valueXmlString.append("<exp_date>").append("<![CDATA[").append(expDate).append("]]>").append("</exp_date>\r\n");
								valueXmlString.append("</Detail>\r\n");

								//Update statement commented by Jiten as per Piyush Sir - 04/11/06
								/*sql2 = "UPDATE SORDALLOC " 
											+"SET QTY_ALLOC	= QTY_ALLOC - ? "
											+"WHERE SALE_ORDER = ? "
											+"AND LINE_NO =	? "
											+"AND EXP_LEV =	? "
											+"AND ITEM_CODE__ORD = ? "
											+"AND ITEM_CODE	= ? "
											+"AND LOC_CODE = ? "
											+"AND LOT_NO = ? "
											+"AND LOT_SL = ? ";
								System.out.println("The Update sql :"+sql2);
								pstmt = conn.prepareStatement(sql2);							
								pstmt.setDouble(1, inputQty);
								pstmt.setString(2, sordNo);
								pstmt.setString(3, lineNoOrd);
								pstmt.setString(4, expLev);
								pstmt.setString(5, itemCodeOrd);
								pstmt.setString(6, itemCode);
								pstmt.setString(7, locCode);
								pstmt.setString(8, lotNo);
								pstmt.setString(9, lotSl);
								updateCnt = pstmt.executeUpdate();
								System.out.println("update the no of records in sordalloc :"+updateCnt);
								sql2 = "UPDATE SORDITEM "
									  +"SET QTY_ALLOC =	QTY_ALLOC -"+inputQty
									  +"WHERE SALE_ORDER = '"+sordNo+"' "
									  +"AND LINE_NO	= '"+lineNoOrd+"' "
									  +"AND EXP_LEV	= '"+expLev+"' ";

								System.out.println("The update Sql :"+sql2);	
								pstmt = conn.prepareStatement(sql2);							
								updateCnt = pstmt.executeUpdate();
								System.out.println("update the no of records in sorditem :"+updateCnt);
								//End Comment
							}
						}//while end
					}//end else
					itemCodeOld = itemCodeOld+ " "+itemCode;
					System.out.println("itemCodeOld :"+itemCodeOld);
				}// while end
				//valueXmlString.append("</Root>\r\n");			
			}
			valueXmlString.append("</Root>\r\n");			
		}//try end
		catch(SQLException sqx)
		{
			System.out.println("The Exception caught from Despatch(Default) :"+sqx);
			throw new ITMException(sqx);
		}
		catch(Exception e)
		{
			System.out.println("The Exception caught from Despatch(Default) :"+e);
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				conn.close();
				conn = null;
				qtyMap = null;
			}catch(Exception e){}
		}
		System.out.println("valueXmlString.toString() :"+valueXmlString.toString());
		return valueXmlString.toString();
	}*///End Comment Gulzar - 23/01/08
	//Added Changes - Gulzar - 23/01/08
	private String actionDefault(Document dom, Document dom1, String objContext, String xtraParams) throws RemoteException, ITMException
	{
		PreparedStatement pstmt = null;
		String unit1 = "", unitStd1 = "";
		String sordNo = "", locCode = "", lotSl = "", lotNo = "", siteCode = "", errCode = "", errString = "";
		String lineNo = "", expLev = "", itemCode = "", itemCodeOrd = "", itemCodeOld = ""; 
		String sql = "", sql1 = "", sql2 = "", sql3 = "";
		java.util.Date expDate1 = null, chkDate4 = null, chkDate3 = null;
		//Statement stmt = null, stmt1 = null, stmt2 = null, stmt3 = null;//Changed and added by Pavan R 10jan19[to handle open cursor issue]
		PreparedStatement pstmt1 = null, pstmt2 = null, pstmt3 = null;
		Connection conn = null;
		ResultSet rs = null, rs1 = null, rs2 = null, rs3 = null;
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		double qtyStk = 0d, allocQty = 0d, inputQty = 0d, quantityStduom = 0d, convQtyStduom = 0d;
		double grossPer = 0d,netPer = 0d, tarePer = 0d, qtyStd = 0d;
		double grossWeight = 0d, tareWeight = 0d, netWeight = 0d, conv = 0;
		java.sql.Date expDate = null, mfgDate = null;
		String dimension = "", suppCodeMfg = "", despDt = "", trackShelfLife = "", orderType = "", siteCodeMfg = "";
		int minShelfLife = 0, maxShelfLife = 0, noOfItems = 0, cntItemCode = 0, updateCnt = 0, counter = 0; 
		ArrayList qtyFact = new ArrayList();
		DistCommon distCommon = new DistCommon();
		String locCode1 = "", lotSl1 = "", locGroup = "", lineNoSord = "";
		String chkDate1 = "", chkDate2 = "",detCnt = "0",lineNoOrd = ""; 
		ConnDriver connDriver = new ConnDriver();
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		HashMap qtyMap = new HashMap();
		Double qtyDbl = new Double(0.0);
		double qtyOrd = 0.0, qtyPerArt = 0.00;
		int noArt = 0;
		double grossWeight2 = 0d, tareWeight2 = 0d, netWeight2 = 0d;
		String updateStatus = "";
		// Added - Gulzar - 23/01/08
		String siteCodeDet = "", itemDescr = "";
		String applyPrice = "";
		String itemType = "";
		String netWtUnit = "";
		String rateUnit = "";
		String rateOpt = "";
		String rateUnitSord = "";
		String custCode = "";
		String packCode = "", tempStr = "";
		String sqlTemp ="",qtyDetailStr = "";
		String despatchId = "";
		String taxClass = "";
		String taxChap = "";
		String taxEnv = "";
		//String quantityXml = "";
		double discount = 0d;
		double shipperQtyNew = 0d;
		double integralQtyNew = 0d;
		double balQty = 0d;
		double looseQty = 0d;
		double grossWeight1 = 0d;
		double discAmt =0d ;
		double netWeight1 = 0d;
		double orderedQty = 0d;
		double despatchedQty = 0d; 
		double convRtuomStd = 0d; 
		double grossWtPerArt = 0d; 
		double tareWtPerArt = 0d;
		double rateStduom = 0d;
		double packQty = 0d;
		double netWtPerArt = 0d;
		double packGrossWeight = 0d;
		double packNettWeight = 0d;
		double rateClg = 0d;
		double palletWt = 0d;
		double OrdQty = 0;
		ResultSet rsTemp =null;
		DecimalFormat df = new DecimalFormat("#########.###");
		String lineType="",nature="";
		//Statement stmtTemp = null;
		// End Addition - Gulzar - 23/01/08
		try
		{
			detCnt = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"detCnt"); //Will be Uncommented later - Jiten
			if (detCnt == null || detCnt.trim().length() == 0)
			{
				detCnt = "0";
			}
			if (dom != null)
			{
				updateStatus = getCurrentUpdateFlag(dom);
				System.out.println("updateStatus :: "+updateStatus);
			}
			else
			{
				updateStatus="A";
				dom=buildDefaultDom();
			}
			//Added Changes By Gulzar 28-02-07
			if ((Integer.parseInt(detCnt) > 1) || (!updateStatus.equals("A")))
			{
				errCode = "VTDESPD1";
				errString = itmDBAccess.getErrorString("",errCode,"");
				return errString;
			}
			//if (detCnt.equals("0")) //Gulzar 27-02-07
			else //End Changes Gulzar 28-02-08
			{
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
				//stmt = conn.createStatement();		//Changed by Pavan R 10jan19[to handle open cursor issue] Start		
				//stmt1 = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
				//stmt2 = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
				//stmt3 = conn.createStatement();
				//stmtTemp = conn.createStatement(); // Added - Gulzar - 23/01/08 

				sordNo = genericUtility.getColumnValue("sord_no",dom1);	 
				System.out.println("sordNo :"+sordNo);
				SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
				if (sordNo == null && sordNo.trim().length() == 0)
				{
					errCode = "VTPCK1";
					errString = itmDBAccess.getErrorString("",errCode,"","",conn);
					return errString;
				}
				if (dom != null)
				{
					locCode1 = genericUtility.getColumnValue("loc_code",dom);	 
					lotSl1	 = genericUtility.getColumnValue("lot_sl",dom);	 
				}
				siteCode = genericUtility.getColumnValue("site_code",dom1);
				System.out.println("locCode :"+locCode1);
				System.out.println("lotSl :"+lotSl1);
				System.out.println("siteCode :"+siteCode);

				sql = "SELECT NO_OF_ITEMS FROM DOC_NO_ITEMS " 
					+"WHERE SITE_CODE = ? "    //Changed and added by Pavan R 10jan19[to handle open cursor issue] Start               // '"+siteCode+"' "
					+"AND MOD_NAME = UPPER('W_DESPATCH')";
				//System.out.println("sql :"+sql);					
				//rs = stmt.executeQuery(sql);
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, siteCode);
				rs = pstmt.executeQuery();//Changed and added by Pavan R 10jan19[to handle open cursor issue] end
				if (rs.next())
				{
					noOfItems = rs.getInt(1);
					System.out.println("noOfItems :"+noOfItems);
				}
				else
				{
					noOfItems = 0;
					System.out.println("noOfItems :"+noOfItems);
				}
				rs.close();	rs = null;
				pstmt.close(); pstmt = null;
				// Added - Gulzar - 23/01/08
				despatchId = genericUtility.getColumnValue("desp_id",dom1);	 
				System.out.println("Despatch Id :"+despatchId);
				if (despatchId == null)
				{
					despatchId = "";
				}
				// End Addition - Gulzar - 23/01/08
				/*-- Commented And Changes Below as There is a Changes in PB logic
				sql ="SELECT SORDDET.SALE_ORDER, "   
						 +"SORDDET.LINE_NO, "   
						 +"SORDDET.SITE_CODE, "   
						 +"SORDDET.ITEM_CODE, "   
						 +"SORDDET.ITEM_FLG, "   
						 +"SORDDET.QUANTITY, "   
						 +"SORDDET.UNIT, "   
						 +"SORDDET.DSP_DATE, "   
						 +"SORDDET.RATE, "   
						 +"SORDDET.DISCOUNT, "   
						 +"SORDDET.TAX_AMT, "   
						 +"SORDDET.TAX_CLASS, "   
						 +"SORDDET.TAX_CHAP, "   
						 +"SORDDET.TAX_ENV, "   
						 +"SORDDET.NET_AMT, "   
						 +"SORDDET.REMARKS, "   
						 +"SORDDET.STATUS, "   
						 +"SORDDET.STATUS_DATE, "   
						 +"SORDDET.CHG_DATE, "   
						 +"SORDDET.CHG_USER, "   
						 +"SORDDET.CHG_TERM, "   
						 +"SORDDET.ITEM_DESCR, "   
						 +"SORDDET.UNIT__RATE, "   
						 +"SORDDET.CONV__QTY_STDUOM, "   
						 +"SORDDET.CONV__RTUOM_STDUOM, "   
						 +"SORDDET.UNIT__STD, "   
						 +"SORDDET.QUANTITY__STDUOM, "   
						 +"SORDDET.RATE__STDUOM, "   
						 +"SORDDET.NO_ART, "   
						 +"SORDDET.PACK_CODE, "   
						 +"SORDDET.LINE_NO__CONTR, "   
						 +"SORDDET.PACK_INSTR, "   
						 +"SORDDET.SPEC_REF, "   
						 +"SORDDET.PACK_QTY, "   
						 +"SORDDET.ITEM_SER, "   
						 +"SORDDET.RATE__CLG, "   
						 +"SORDDET.MFG_CODE, "   
						 +"SORDDET.CONTRACT_NO, "   
						 +"PACKING.DESCR, "   
						 +"SORDDET.SPEC_ID, "   
						 +"SORDDET.ORD_VALUE, "   
						 +"SORDDET.ITEM_SER__PROM, "   
						 +"SORDDET.SPECIFIC_INSTR, "   
						 +"SORDDET.ITEM_CODE__ORD, "   
						 +"SORDDET.PALLET_CODE, "   
						 +"SORDDET.NO_PALLET, "   
						 +"SORDDET.OVER_SHIP_PERC, "   
						 +"SORDDET.COMM_PERC_1, "   
						 +"SORDDET.COMM_PERC_2, "   
						 +"SORDDET.COMM_PERC_3, "   
						 +"SORDDET.COMM_PERC_ON_1, "   
						 +"SORDDET.COMM_PERC_ON_2, "   
						 +"SORDDET.COMM_PERC_ON_3, "   
						 +"SORDDET.SALES_PERS_COMM_1, "   
						 +"SORDDET.SALES_PERS_COMM_2, "   
						 +"SORDDET.SALES_PERS_COMM_3, "   
						 +"SORDDET.PRICE_LIST__DISC, "   
						 +"SORDDET.RATE__STD, "   
						 +"SORDER.FIN_SCHEME, "   
						 +"SORDDET.MAX_SHELF_LIFE, "   
						 +"SORDDET.MIN_SHELF_LIFE, "   
						 +"SPACE(250) AS ITEM_SPECS, "   
						 +"SPACE(250) AS ST_SHRINK, "   
						 +"SORDDET.LOC_TYPE, "   
						 +"SPACE(250) AS ST_SCHEME,  "
						 +"SORDDET.SORDFORM_NO ,  "  
						 +"SORDDET.LINE_NO__SFORM ,  " 
						 +"FN_GET_ITMSTK(SORDDET.ITEM_CODE,SORDDET.SITE_CODE) STK_QTY,  "  
						 +"SORDDET.APPL_SEG ,  "  
						 +"UOM.DESCR  "  
				 +"FROM SORDDET, PACKING, SORDER  "
				 +"WHERE SORDDET.PACK_CODE = PACKING.PACK_CODE " 
				 +"AND SORDER.SALE_ORDER = SORDDET.SALE_ORDER "
				 +"AND SORDDET.SALE_ORDER = '"+sordNo+"'";*/// End Changes Gulzar 12/03/07
				//Added By Gulzar 12/03/07 - Added join with SORDITEM as the default was not working  For Scheme item
				sql ="SELECT SORDDET.SALE_ORDER, "   
					+"SORDDET.LINE_NO, "   
					+"SORDDET.SITE_CODE, "   
					//	 +"SORDDET.ITEM_CODE, " //Gulzar 14/03/07 - As it should take from SORDITEM
					+"SORDITEM.ITEM_CODE, " //Gulzar 14/03/07
					+"SORDDET.ITEM_FLG, "   
					//	 +"SORDDET.QUANTITY, "   //Gulzar 14/03/07 - As it should take from SORDITEM
					+"SORDITEM.QUANTITY - SORDITEM.QTY_DESP AS QUANTITY, "  //Gulzar 14/03/07
					+"SORDDET.UNIT, "   
					+"SORDDET.DSP_DATE, "   
					+"SORDDET.RATE, "   
					+"SORDDET.DISCOUNT, "   
					+"SORDDET.TAX_AMT, "   
					+"SORDDET.TAX_CLASS, "   
					+"SORDDET.TAX_CHAP, "   
					+"SORDDET.TAX_ENV, "   
					+"SORDDET.NET_AMT, "   
					+"SORDDET.REMARKS, "   
					+"SORDDET.STATUS, "   
					+"SORDDET.STATUS_DATE, "   
					+"SORDDET.CHG_DATE, "   
					+"SORDDET.CHG_USER, "   
					+"SORDDET.CHG_TERM, "   
					+"SORDDET.ITEM_DESCR, "   
					+"SORDDET.UNIT__RATE, "   
					+"SORDDET.CONV__QTY_STDUOM, "   
					+"SORDDET.CONV__RTUOM_STDUOM, "   
					+"SORDDET.UNIT__STD, "   
					+"SORDDET.QUANTITY__STDUOM, "   
					+"SORDDET.RATE__STDUOM, "   
					+"SORDDET.NO_ART, "   
					+"SORDDET.PACK_CODE, "   
					+"SORDDET.LINE_NO__CONTR, "   
					+"SORDDET.PACK_INSTR, "   
					+"SORDDET.SPEC_REF, "   
					+"SORDDET.PACK_QTY, "   
					+"SORDDET.ITEM_SER, "   
					+"SORDDET.RATE__CLG, "   
					+"SORDDET.MFG_CODE, "   
					+"SORDDET.CONTRACT_NO, "   
					+"PACKING.DESCR, "   
					+"SORDDET.SPEC_ID, "   
					+"SORDDET.ORD_VALUE, "   
					+"SORDDET.ITEM_SER__PROM, "   
					+"SORDDET.SPECIFIC_INSTR, "   
					//	 +"SORDDET.ITEM_CODE__ORD, "   //Gulzar 14/03/07 - As it should take from SORDITEM
					+"SORDITEM.ITEM_CODE__ORD, "  //Gulzar 14/03/07
					+"SORDDET.PALLET_CODE, "   
					+"SORDDET.NO_PALLET, "   
					+"SORDDET.OVER_SHIP_PERC, "   
					+"SORDDET.COMM_PERC_1, "   
					+"SORDDET.COMM_PERC_2, "   
					+"SORDDET.COMM_PERC_3, "   
					+"SORDDET.COMM_PERC_ON_1, "   
					+"SORDDET.COMM_PERC_ON_2, "   
					+"SORDDET.COMM_PERC_ON_3, "   
					+"SORDDET.SALES_PERS_COMM_1, "   
					+"SORDDET.SALES_PERS_COMM_2, "   
					+"SORDDET.SALES_PERS_COMM_3, "   
					+"SORDDET.PRICE_LIST__DISC, "   
					+"SORDDET.RATE__STD, "   
					+"SORDER.FIN_SCHEME, "   
					/* // Remarked and taken from sorditem  - Gulzar - 23/01/08
						 +"SORDDET.MAX_SHELF_LIFE, "   
						 +"SORDDET.MIN_SHELF_LIFE, "   
						 // End Remark - Gulzar - 23/01/08
					 */
					+"SORDITEM.MAX_SHELF_LIFE, "   // Taken from sorditem - Gulzar - 23/01/08
					+"SORDITEM.MIN_SHELF_LIFE, "   // Taken from sorditem - Gulzar - 23/01/08
					+"SORDDET.MAX_SHELF_LIFE, "   
					+"SORDDET.MIN_SHELF_LIFE, "   
					+"SPACE(250) AS ITEM_SPECS, "   
					+"SPACE(250) AS ST_SHRINK, "   
					+"SORDDET.LOC_TYPE, "   
					+"SPACE(250) AS ST_SCHEME,  "
					+"SORDDET.SORDFORM_NO ,  "  
					+"SORDDET.LINE_NO__SFORM ,  "  ;

				//changed by manish on 10/10/15 for Ms sql server databse [start]

				if("mssql".equalsIgnoreCase(DB))
				{
					sql =	sql + "dbo.FN_GET_ITMSTK(SORDDET.ITEM_CODE,SORDDET.SITE_CODE) STK_QTY,  " ; 
				}
				else
				{
					sql =	sql +"FN_GET_ITMSTK(SORDDET.ITEM_CODE,SORDDET.SITE_CODE) STK_QTY,  "  ;
				}
				//changed by manish on 10/10/15 for Ms sql server databse [end]

				sql =	sql + "SORDDET.APPL_SEG ,  "  
				+"UOM.DESCR,  "
				+"SORDITEM.EXP_LEV, " //Gulzar 14/03/07
				+"SORDITEM.QTY_ALLOC,  " //Gulzar 14/03/07
				+"SORDER.LOC_GROUP, "//Added by Gulzar - 23/01/08
				+"SORDER.ORDER_TYPE, "//Added by Gulzar - 23/01/08
				+"SORDER.CUST_CODE, "//Added by Gulzar - 23/01/08
				+"SORDDET.QUANTITY AS ORD_QTY "
				+",SORDITEM.NATURE ,SORDITEM.LINE_TYPE  " //ADDED BY NANDKUMAR GADKARI ON 27/09/19
				+"FROM SORDDET LEFT OUTER JOIN PACKING ON SORDDET.PACK_CODE = PACKING.PACK_CODE LEFT OUTER JOIN UOM ON SORDDET.UNIT = UOM.UNIT,  "
				+"SORDER, "
				+"SORDITEM  "
				+" WHERE SORDER.SALE_ORDER = SORDDET.SALE_ORDER "
				+" AND SORDITEM.SALE_ORDER = SORDDET.SALE_ORDER "
				+" AND SORDITEM.LINE_NO = SORDDET.LINE_NO "
				+" AND SORDITEM.SALE_ORDER = ? "                  //'"+sordNo+"'"//Changed and added by Pavan R 10jan19[to handle open cursor issue] Start
				+" AND SORDITEM.LINE_TYPE = 'I' "
				+" ORDER BY SORDITEM.LINE_NO, SORDITEM.EXP_LEV " ;
				//End Changes Gulzar 12/03/07
				//System.out.println("sql for all data ::"+sql);
				//rs = stmt.executeQuery(sql);
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, sordNo);
				rs = pstmt.executeQuery();//Changed and added by Pavan R 10jan19[to handle open cursor issue] end
				while (rs.next())
				{
					if (noOfItems > 0 && counter >= noOfItems)
					{	pstmt.close(); pstmt = null; //Changed and added by Pavan R 10jan19[closed and nulled]
						rs.close(); rs = null;
						errCode = "VTITMGRZRO";
						errString = itmDBAccess.getErrorString("",errCode,"","",conn);
						return errString;
					}
					lotSl = lotSl1; // Re-set with Picked-up from DOM.
					locCode = locCode1; // Re-set with Picked-up from DOM.
					//lineNo = rs.getString("LINE_NO");
					lineNoOrd = rs.getString("LINE_NO");
					System.out.println("lineNo :"+lineNoOrd);
					siteCode = genericUtility.getColumnValue("site_code",dom1);	
					//lineNoOrd = genericUtility.getColumnValue("line_no__sord",dom); //Commented as line_no from despatch is to be used
					System.out.println("siteCode :"+siteCode);

					/*-- Commented By Gulzar 14/03/07 - As the same is retrived from main query
					sql1 = "SELECT ITEM_CODE__ORD, QUANTITY,EXP_LEV, ITEM_CODE, QTY_ALLOC "
						  +"FROM SORDITEM WHERE SALE_ORDER = '"+sordNo+"' " // sordNo of Header is Used whereas in PB it taken from detail
						  +"AND LINE_NO = '"+lineNoOrd+"'"+" AND LINE_TYPE = 'I'"; 
					System.out.println("sql1 :"+sql1);
					rs1 = stmt1.executeQuery(sql1);
					if (rs1.next())
					{
						itemCodeOrd = rs1.getString(1);
						System.out.println("itemCodeOrd :"+itemCodeOrd);
						expLev = rs1.getString(3);
						System.out.println("expLev :"+expLev);
						itemCode = rs1.getString(4);
						System.out.println("itemCode :"+itemCode);
					}
					stmt1.close();
					stmt1 = null;
					 *///End Comment Gulzar 14/03/07

					//Added By Gulzar 14/03/07
					itemCodeOrd = rs.getString("ITEM_CODE__ORD");
					System.out.println("itemCodeOrd :"+itemCodeOrd);
					expLev = rs.getString("EXP_LEV");
					System.out.println("expLev :"+expLev);
					itemCode = rs.getString("ITEM_CODE");
					System.out.println("itemCode :"+itemCode);
					//End changes Gulzar 14/03/07

					//Start - Added by Gulzar - 23/01/08
					sql1 = "SELECT DESCR FROM ITEM WHERE ITEM_CODE = ?";  //'"+itemCode+"'";//Changed and added by Pavan R 10jan19[to handle open cursor issue] Start
					//stmt1 = conn.createStatement();
					//rs1 = stmt1.executeQuery(sql1);
					pstmt1 = conn.prepareStatement(sql1,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE); 
					pstmt1.setString(1, itemCode);
					rs1 = pstmt1.executeQuery();//Changed and added by Pavan R 10jan19[to handle open cursor issue] End
					if (rs1.next())
					{
						itemDescr = rs1.getString("DESCR");
					}
					rs1.close();rs1 = null;
					pstmt1.close(); pstmt1 = null;
					rateClg = rs.getDouble("RATE__CLG");
					orderedQty = rs.getDouble("QUANTITY");
					OrdQty = rs.getDouble("ORD_QTY");
					System.out.println("orderedQty :"+orderedQty);
					siteCodeDet = rs.getString("SITE_CODE");
					System.out.println("siteCodeDet :"+siteCodeDet);
					unit1 = rs.getString("UNIT");
					unitStd1 = rs.getString("UNIT__STD");
					convQtyStduom = rs.getDouble("CONV__QTY_STDUOM");
					System.out.println("convQtyStduom :"+convQtyStduom);
					quantityStduom = rs.getDouble("QUANTITY__STDUOM");
					rateStduom = rs.getDouble("RATE__STDUOM");
					custCode = rs.getString("CUST_CODE");
					packCode = rs.getString("PACK_CODE");

					convRtuomStd = rs.getDouble("CONV__RTUOM_STDUOM"); 
					rateUnitSord = rs.getString("UNIT__RATE");
					locGroup = rs.getString("LOC_GROUP");
					System.out.println("locGroup :"+locGroup);
					orderType = rs.getString("ORDER_TYPE");
					System.out.println("orderType :"+orderType);
					minShelfLife = rs.getInt("MIN_SHELF_LIFE");
					System.out.println("minShelfLife :"+minShelfLife);

					maxShelfLife = rs.getInt("MAX_SHELF_LIFE");
					System.out.println("maxShelfLife :"+maxShelfLife);
					if (rateUnitSord == null)
					{
						rateUnitSord = "";
					}
					sql1 = "SELECT SUM(QUANTITY__STDUOM) AS QUANTITY__STDUOM FROM DESPATCHDET WHERE SORD_NO = ?" + //'" + sordNo + "'" + 
					" AND LINE_NO__SORD = ?" +//'" + lineNoOrd + "'" +//Changed and added by Pavan R 10jan19[to handle open cursor issue] Start 
					" AND EXP_LEV = ? " +     //'" + expLev + "'" + 
					" AND DESP_ID <> ? ";     //'" + despatchId + "'";
					//stmt1 = conn.createStatement();
					//rs1 = stmt1.executeQuery(sql1);
					pstmt1 = conn.prepareStatement(sql1,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
					pstmt1.setString(1, sordNo);
					pstmt1.setString(2, lineNoOrd);
					pstmt1.setString(3, expLev);
					pstmt1.setString(4, despatchId);
					despatchedQty = 0d;
					rs1 = pstmt1.executeQuery();//Changed and added by Pavan R 10jan19[to handle open cursor issue] end
					if (rs1.next())
					{
						despatchedQty = rs1.getDouble("QUANTITY__STDUOM");
					}
					sql1 = "";
					rs1.close();rs1 = null;
					pstmt1.close();pstmt1 = null;
					System.out.println("despatchedQty--["+despatchedQty+"]");
					System.out.println("quantityStduom--["+quantityStduom+"]");
					quantityStduom = quantityStduom - despatchedQty; 
					System.out.println("quantityStduom--["+quantityStduom+"]");
					if (quantityStduom <= 0)
					{
						continue;
					}

					///qtyOrd = quantityStduom; // commented by manoharan 27/12/08
					qtyOrd = orderedQty; // added by manoharan 27/12/08
					System.out.println("quantityStduom :"+quantityStduom);
					//End - Added by Gulzar - 23/01/08


					// Commented - Gulzar - 23/01/08
					/**
					sql1 = "SELECT UNIT__STD, CONV__QTY_STDUOM, UNIT, PACK_INSTR, QUANTITY__STDUOM, "
						  +"CASE WHEN NO_ART IS NULL THEN 0 ELSE NO_ART END "
						  +"FROM SORDDET WHERE SALE_ORDER = '"+sordNo+"'"+" AND LINE_NO ='"+lineNoOrd+"'";
					System.out.println("sql1 :"+sql1);
					stmt1 = conn.createStatement();
					rs1 = stmt1.executeQuery(sql1);
					if (rs1.next())
					{
						unit1 = rs1.getString("UNIT");
						unitStd1 = rs1.getString("UNIT__STD");
						convQtyStduom = rs1.getDouble(2);
						System.out.println("convQtyStduom :"+convQtyStduom);
						quantityStduom = rs1.getDouble(5);
						qtyOrd = quantityStduom;
						System.out.println("quantityStduom :"+quantityStduom);
					}
					stmt1.close();
					stmt1 = null;


					if (quantityStduom <= 0)
					{
						continue;
					}
					sql1 = "SELECT LOC_CODE,QUANTITY, EXP_LEV, ITEM_CODE, "
						  +"LOT_NO, LOT_SL, UNIT__STD, CONV__QTY_STDUOM, UNIT "
						  +"FROM SORDALLOC WHERE SALE_ORDER ='"+sordNo+"'"+" AND LINE_NO ='"+lineNoOrd+"'";
					System.out.println("sql1 :"+sql1);
					stmt1 = conn.createStatement();
					rs1 = stmt1.executeQuery(sql1);
					if (rs1.next())
					{
						if (locCode == null || locCode.trim().length() == 0)
						{
							//locCode = rs.getString(1); //Commented - Gulzar - 14/03/07 - As it should take from rs1 not rs 
							locCode = rs1.getString(1);
						}						
						System.out.println("locCode :"+locCode);
						lotNo = rs1.getString(5);
						System.out.println("lotNo :"+lotNo);
						lotSl = rs1.getString(6);
						System.out.println("lotSl :"+lotSl);
					}					
					stmt1.close();
					stmt1 = null;
					// End Comment - Gulzar - 23/01/08
					 ***/

				//	added by nandkumar gadkari on 27-09-19--------start----------------
					nature  = rs.getString("NATURE") == null ? "" : (rs.getString("NATURE")).trim();
					lineType = rs.getString("LINE_TYPE") == null ? "": (rs.getString("LINE_TYPE")).trim();
					// C and P nature added by nandkumar gadkari on 06/08/19
					
					//added by nandkumar gadkari on 27-09-19--------end----------------
					sql1 = "SELECT SORDALLOC.LOT_NO, "   
						+"SORDALLOC.LOT_SL, "   
						+"SORDALLOC.QTY_ALLOC, "   
						+"SORDALLOC.DATE_ALLOC, "   
						+"SORDALLOC.STATUS, "   
						+"SORDALLOC.ITEM_GRADE, "   
						+"SORDALLOC.EXP_DATE, "   
						+"SORDALLOC.ALLOC_MODE, "   
						+"SORDALLOC.SITE_CODE, "   
						+"SORDALLOC.LOC_CODE, "   
						+"SORDALLOC.SALE_ORDER, "   
						+"SORDALLOC.LINE_NO, "   
						+"SORDALLOC.EXP_LEV, "   
						+"SORDALLOC.ITEM_CODE__ORD, "   
						+"SORDALLOC.ITEM_CODE, "   
						+"SORDALLOC.ITEM_REF, "   
						+"SORDALLOC.QUANTITY, "   
						+"SORDALLOC.UNIT, "   
						+"LOCATION.DESCR, "   
						+"SORDALLOC.CONV__QTY_STDUOM, "   
						+"SORDALLOC.UNIT__STD, "   
						+"SORDALLOC.QUANTITY__STDUOM, "   
						+"SORDALLOC.MFG_DATE, "   
						+"SORDALLOC.SITE_CODE__MFG "   
						+"FROM SORDALLOC, LOCATION "  
						+"WHERE SORDALLOC.LOC_CODE = LOCATION.LOC_CODE "
						+"AND SORDALLOC.SALE_ORDER = ? "          //'"+sordNo+"' " //Changed and added by Pavan R 10jan19[to handle open cursor issue] Start
						+"AND SORDALLOC.LINE_NO = ? "             //'"+lineNoOrd+"' "
						+"AND SORDALLOC.EXP_LEV = ? "             //'"+expLev+"' "
						+"AND SORDALLOC.ITEM_CODE__ORD = ? "      //'"+itemCodeOrd+"' "
						+"AND SORDALLOC.ITEM_CODE = ? "           //'"+itemCode+"' "
						+"AND CASE WHEN SORDALLOC.STATUS IS NULL THEN ' ' ELSE SORDALLOC.STATUS END  <> 'D' ";
					//System.out.println("sql1 :"+sql1);
					//stmt1 = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
					//rs1 = stmt1.executeQuery(sql1);
					pstmt1 = conn.prepareStatement(sql1, ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
					pstmt1.setString(1, sordNo);
					pstmt1.setString(2, lineNoOrd);
					pstmt1.setString(3, expLev);
					pstmt1.setString(4, itemCodeOrd);
					pstmt1.setString(5, itemCode);
					rs1 = pstmt1.executeQuery();//Changed and added by Pavan R 10jan19[to handle open cursor issue] End
					if(!rs1.next())
					{
						if(locCode == null || locCode.trim().length() == 0)
						{
							locCode = "%";
							System.out.println("locCode :"+locCode);
						}
						else
						{
							locCode = locCode.trim() + "%";
							System.out.println("locCode :"+locCode);
						}
						if (lotSl == null || lotSl.trim().length() == 0)
						{
							lotSl = "%";
							System.out.println("lotSl :"+lotSl);
						}
						else
						{
							lotSl = lotSl.trim() + "%";
							System.out.println("lotSl :"+lotSl);

						}

						/***
						// Commented - Gulzar - 23/01/08
						sql2 = "SELECT LOC_GROUP FROM SORDER "
								+"WHERE SALE_ORDER ='"+sordNo+"'";
						System.out.println("sql2 :"+sql2);
						rs2 = stmt2.executeQuery(sql2);
						if (rs2.next())
						{
							locGroup = rs2.getString(1);
							System.out.println("locGroup :"+locGroup);
						}
						if (locGroup == null || locGroup.trim().length() == 0)
						{
							//sql2 = "SELECT COUNT(A.ITEM_CODE) FROM STOCK A, INVSTAT B ,LOCATION C "
							sql2 = "SELECT COUNT(*) FROM STOCK A, INVSTAT B ,LOCATION C "
										+"WHERE A.INV_STAT  = B.INV_STAT "
										+"AND C.LOC_CODE = A.LOC_CODE "
										+"AND A.ITEM_CODE = '"+itemCode+"' "
										+"AND A.SITE_CODE = '"+siteCode+"' "
										+"AND A.LOC_CODE  LIKE '"+locCode+"' "
										+"AND A.LOT_SL    LIKE '"+lotSl+"' "
										+"AND B.AVAILABLE = 'Y' "
										+"AND B.USABLE = 'Y' "
										+"AND A.QUANTITY  > 0 ";
							System.out.println("sql2 :"+sql2);
						}
						else
						{
							//sql2 = "SELECT COUNT(A.ITEM_CODE) FROM STOCK A, INVSTAT B ,LOCATION C "
							sql2 = "SELECT COUNT(*) FROM STOCK A, INVSTAT B ,LOCATION C "
										+"WHERE A.INV_STAT  = B.INV_STAT "
										+"AND C.LOC_CODE = A.LOC_CODE "
										+"AND C.LOC_GROUP = '"+locGroup+"' "
										+"AND A.ITEM_CODE = '"+itemCode+"' "
										+"AND A.SITE_CODE = '"+siteCode+"' "
										+"AND A.LOC_CODE  LIKE '"+locCode+"' "
										+"AND A.LOT_SL    LIKE '"+lotSl+"' " 
										+"AND B.AVAILABLE = 'Y' "
										+"AND B.USABLE = 'Y' "
										+"AND A.QUANTITY  > 0 "; 
							System.out.println("sql2 :"+sql2);
						}
						rs2 = stmt2.executeQuery(sql2);
						if (rs2.next())
						{
							cntItemCode = rs2.getInt(1);
							System.out.println("cntItemCode :"+cntItemCode);
						}
						if (cntItemCode == 0)
						{
							continue;
						}
						else
						{
							if(itemCodeOld.indexOf(itemCode) == -1)
							{
								counter = counter + 1;
								System.out.println("Counter :"+counter);
							}
						}
						// End Comment - Gulzar - 23/01/08
						 ***/

						if (locGroup == null || locGroup.trim().length() == 0)
						{
							sql2 = "SELECT A.LOT_NO,A.LOT_SL, "
								+"A.QUANTITY,A.EXP_DATE, " 
								+"A.SITE_CODE__MFG, "
								+"A.MFG_DATE, "
								+"A.ALLOC_QTY, "
								+"A.PACK_CODE, " 
								+"A.LOC_CODE, "
								+"A.GROSS_WEIGHT, "
								+"A.TARE_WEIGHT, "
								+"A.NET_WEIGHT, " 
								+"A.DIMENSION, "
								+"A.SUPP_CODE__MFG, " 
								+"A.QTY_PER_ART, "
								+"A.GROSS_WT_PER_ART, " // Gulzar - 23/01/08
								+"A.TARE_WT_PER_ART, "  // Gulzar - 23/01/08
								+"A.PALLET_WT " //Gulzar - 30/01/08
								+"FROM STOCK A,INVSTAT B , LOCATION c " 
								+"WHERE C.LOC_CODE = A.LOC_CODE "
								+ " AND C.INV_STAT = B.INV_STAT " 
								+"AND A.ITEM_CODE = '"+itemCode+"' "  
								+"AND A.SITE_CODE = '"+siteCode+"' "  
								+"AND A.LOC_CODE LIKE '"+locCode+"' "  
								+"AND A.LOT_SL LIKE '"+lotSl+"' "   
								+"AND B.AVAILABLE = 'Y' " 
								+"AND B.USABLE = 'Y' " 
								+"AND A.QUANTITY - A.ALLOC_QTY > 0 " 
								+"ORDER BY a.partial_used,A.EXP_DATE,A.CREA_DATE, A.LOT_NO, A.LOT_SL ";
							System.out.println("sql2 :"+sql2);
						}
						else
						{
							sql2 = "SELECT A.LOT_NO, "
								+"A.LOT_SL, "
								+"A.QUANTITY, "
								+"A.EXP_DATE, " 
								+"A.SITE_CODE__MFG, "
								+"A.MFG_DATE, "
								+"A.ALLOC_QTY, "
								+"A.PACK_CODE, " 
								+"A.LOC_CODE, "
								+"A.GROSS_WEIGHT, "
								+"A.TARE_WEIGHT, "
								+"A.NET_WEIGHT, " 
								+"A.DIMENSION, "
								+"A.SUPP_CODE__MFG, " 
								+"A.QTY_PER_ART, "
								+"A.GROSS_WT_PER_ART, " // Gulzar - 23/01/08
								+"A.TARE_WT_PER_ART, "  // Gulzar - 23/01/08
								+"A.PALLET_WT " //Gulzar - 30/01/08
								+"FROM STOCK A,INVSTAT B,LOCATION C " 
								+"WHERE A.INV_STAT = B.INV_STAT " 
								+"AND C.LOC_CODE = A.LOC_CODE " 
								+"AND C.LOC_GROUP = '"+locGroup+"' " 
								+"AND A.ITEM_CODE = '"+itemCode+"' " 
								+"AND A.SITE_CODE = '"+siteCode+"' " 
								+"AND A.LOC_CODE  LIKE '"+locCode+"' " 
								+"AND A.LOT_SL    LIKE '"+lotSl+"' "   
								+"AND B.AVAILABLE = 'Y' " 
								+"AND B.USABLE = 'Y' " 
								+"AND A.QUANTITY - A.ALLOC_QTY > 0 " 
								+"ORDER BY a.partial_used,A.EXP_DATE, A.CREA_DATE, A.LOT_NO, A.LOT_SL ";
							System.out.println("sql2 :"+sql2);
						}
						//Changed and added by Pavan R 10jan19[to handle open cursor issue] Start
						//rs2 = stmt2.executeQuery(sql2);
						pstmt2 = conn.prepareStatement(sql2, ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
						rs2 = pstmt2.executeQuery();//Changed and added by Pavan R 10jan19[to handle open cursor issue] End
						if (!rs2.next())
						{
							rs2.close(); rs2 = null;
							pstmt2.close(); pstmt2 = null;
							continue;
						}
						else
						{
							//Start-Added by Gulzar - 23/01/08
							if(itemCodeOld.indexOf(itemCode) == -1)
							{
								counter = counter + 1;
								System.out.println("Counter :"+counter);
							}
							//End-Added by Gulzar - 23/01/08
							rs2.beforeFirst();
							while(rs2.next())
							{
								// 27/12/08 manoharan bug fix reinitilise
								qtyFact = null;
								qtyFact = new ArrayList();
								// end 27/12/08 manoharan bug fix reinitilise
								lotNo = rs2.getString(1);
								System.out.println("lotNo :"+lotNo);
								lotSl = rs2.getString(2);
								System.out.println("lotSl :"+lotSl);
								qtyStk = rs2.getDouble(3);
								System.out.println("qtyStk :"+qtyStk);
								expDate = rs2.getDate(4);
								System.out.println("expDate :"+expDate);
								siteCodeMfg = rs2.getString(5);
								System.out.println("siteCodeMfg :"+siteCodeMfg);
								mfgDate = rs2.getDate(6);
								System.out.println("mfgDate :"+mfgDate);
								allocQty = rs2.getDouble(7);
								System.out.println("allocQty :"+allocQty);
								locCode = rs2.getString(9);
								System.out.println("locCode :"+locCode);
								grossWeight = rs2.getDouble(10);
								System.out.println("grossWeight :"+grossWeight);
								tareWeight = rs2.getDouble(11);
								System.out.println("tareWeight :"+tareWeight);
								netWeight = rs2.getDouble(12);
								System.out.println("netWeight :"+netWeight);
								dimension = rs2.getString(13);
								System.out.println("dimension :"+dimension);
								suppCodeMfg = rs2.getString(14);
								System.out.println("suppCodeMfg :"+suppCodeMfg);
								despDt = genericUtility.getColumnValue("desp_date",dom1);	
								System.out.println("despDt :"+despDt);
								qtyPerArt = rs2.getDouble(15);
								grossWtPerArt = rs2.getDouble(16); // Added - Gulzar - 23/01/08
								tareWtPerArt = rs2.getDouble(17); // Added - Gulzar - 23/01/08
								palletWt	= rs2.getDouble("PALLET_WT"); //Added - Gulzar - 30/01/08

								/*
								// Commented - Gulzar - 23/01/08
								sql3 = "SELECT CASE WHEN TRACK_SHELF_LIFE IS NULL THEN 'N' ELSE TRACK_SHELF_LIFE END " 
									  +"FROM ITEM WHERE ITEM_CODE = '"+itemCode+"' ";
								// End Comment - Gulzar - 23/01/08
								 */

								// Added - Gulzar - 23/01/08
								//Changed and added by Pavan R 10jan19[to handle open cursor issue]
								sql3 = "SELECT CASE WHEN TRACK_SHELF_LIFE IS NULL THEN 'N' ELSE TRACK_SHELF_LIFE END , DESCR " 
									+"FROM ITEM WHERE ITEM_CODE = ?"; //'"+itemCode+"' "; 
								// End Add - Gulzar - 23/01/08

								//System.out.println("sql3  :"+sql3);
								//rs3 = stmt3.executeQuery(sql3);
								pstmt3 = conn.prepareStatement(sql3);
								pstmt3.setString(1, itemCode);
								rs3 = pstmt3.executeQuery();
								if (rs3.next())
								{
									trackShelfLife = rs3.getString(1);
									System.out.println("trackShelfLife :"+trackShelfLife);
								}
								rs3.close(); rs3 = null;
								pstmt3.close(); pstmt3 = null;
								if (trackShelfLife.equals("Y"))
								{
									/*
									// Commented - Gulzar - 23/01/08
									sql3 = "SELECT ORDER_TYPE FROM SORDER WHERE SALE_ORDER = '"+sordNo+"'";
									System.out.println("sql3 :"+sql3);
									rs3 = stmt3.executeQuery(sql3);
									if (rs3.next())
									{
										orderType = rs3.getString(1);
										System.out.println("orderType :"+orderType);
									}
									 */
									// End Comment - Gulzar - 23/01/08

									if (orderType.equals("NE"))
									{
										/*
										// Commented - Gulzar - 23/01/08
										sql3 = "SELECT MIN_SHELF_LIFE,MAX_SHELF_LIFE FROM SORDITEM "
											  +"WHERE SALE_ORDER = '"+sordNo+"' "
											  +"AND LINE_NO = '"+lineNoOrd+"' "
											  +"AND EXP_LEV = '"+expLev+"'";
										System.out.println("sql3 :"+sql3);
										rs3 = stmt3.executeQuery(sql3);
										if (rs3.next())
										{
											minShelfLife = rs3.getInt(1);
											System.out.println("minShelfLife :"+minShelfLife);
											maxShelfLife = rs3.getInt(2);
											System.out.println("maxShelfLife :"+maxShelfLife);
										}
										// End Comment - Gulzar - 23/01/08
										 */


										chkDate1 = calcExpiry(despDt, minShelfLife + 1);
										System.out.println("chkDate1 :"+chkDate1);

										chkDate3 = sdf.parse(chkDate1);
										System.out.println("chkDate3 :"+chkDate3);

										chkDate2 = calcExpiry(despDt, maxShelfLife);
										System.out.println("chkDate2 :"+chkDate2);

										chkDate4 = sdf.parse(chkDate2);
										System.out.println("chkDate4 :"+chkDate4);
										if (expDate != null)
										{
											expDate1 = new java.util.Date(expDate.getTime());
											System.out.println("expDate1 :"+expDate1);

											if (!(expDate1.compareTo(chkDate3) >= 0 && expDate1.compareTo(chkDate4) <= 0))  
											{
												continue;
											}
										}										
									}
									else
									{
										/*
										// Commented - Gulzar - 23/01/08
										sql3 = "SELECT MIN_SHELF_LIFE FROM SORDITEM "
												+"WHERE SALE_ORDER = '"+sordNo+"' " 
												+"AND LINE_NO = '"+lineNoOrd+"' " 
												+"AND EXP_LEV = '"+expLev+"'";
										System.out.println("sql3 :"+sql3);
										rs3 = stmt3.executeQuery(sql3);
										if (rs3.next())
										{
											minShelfLife = rs3.getInt(1);
											System.out.println("minShelfLife :"+minShelfLife);
										}
										// End Comment - Gulzar - 23/01/08
										 */

										chkDate1 = calcExpiry(despDt, minShelfLife);
										System.out.println("chkDate1 :"+chkDate1);

										chkDate4 = sdf.parse(chkDate1);
										System.out.println("chkDate4 :"+chkDate4);
										if (expDate != null)
										{
											expDate1 = new java.util.Date(expDate.getTime());
											System.out.println("expDate1 :"+expDate1);

											if (chkDate4.compareTo(expDate1) > 0) 
											{
												continue;
											}
										}										
									}//end else
								}//end if
								//Below section not present in PB code
								System.out.println("itemCode--["+itemCode+"]");
								System.out.println("siteCode--["+siteCode+"]");
								System.out.println("locCode--["+locCode+"]");
								System.out.println("lotNo--["+lotNo+"]");
								System.out.println("lotSl--["+lotSl+"]");
								String qtyKeyStr = itemCode + siteCode + locCode + lotNo + lotSl;
								System.out.println("qtyKeyStr--["+qtyKeyStr+"]");
								System.out.println("qtyMap.containsKey(qtyKeyStr)--["+qtyMap.containsKey(qtyKeyStr)+"]");
								qtyDbl = 0.0;//reintialize to 0 -- Abhijit - 26/04/17
								if(qtyMap.containsKey(qtyKeyStr))
								{
									qtyDbl = (Double)qtyMap.get(qtyKeyStr);
									System.out.println("qtyDbl---["+qtyDbl+"]");
								}
								//qtyDbl = subtraction to be done on qtyDbl.doubleValue();
								//qtyMap.put(qtyKeyStr, new Double(subtracted value);
								System.out.println("allocQty--["+allocQty+"]");
								System.out.println("qtyDbl.doubleValue()--["+qtyDbl.doubleValue()+"]");
								allocQty = allocQty + qtyDbl.doubleValue();
								System.out.println("allocQty--["+allocQty+"]");
								if (qtyStk - allocQty <= 0)
								{
									System.out.println("continue............");
									continue;
								}
								//End Section

								// if (qtyStk - allocQty <= quantityStduom) // Remarked - Gulzar - 23/01/08
								System.out.println("qtyStk--["+qtyStk+"]");
								System.out.println("allocQty--["+allocQty+"]");
								System.out.println("qtyOrd--["+qtyOrd+"]");
								if (qtyStk - allocQty <= qtyOrd) // Changed - Gulzar - 23/01/08
								{
									inputQty = qtyStk - allocQty;
									System.out.println("inputQty :"+inputQty);
									//quantityStduom = quantityStduom - inputQty; // Remarked - Gulzar - 23/01/08
									//System.out.println("quantityStduom :"+quantityStduom); // Remarked - Gulzar - 23/01/08
								}
								else
								{
									//inputQty = quantityStduom;  // Remarked - Gulzar - 23/01/08
									inputQty = qtyOrd;  // Changed - Piyush - 24/12/07
									System.out.println("inputQty else :"+inputQty);
									//quantityStduom = quantityStduom - inputQty; // Remarked - Gulzar - 23/01/08
									//System.out.println("quantityStduom :"+quantityStduom); // Remarked - Gulzar - 23/01/08
								}


								if (inputQty > 0)
								{

									//start change  10-08-2010 

									if (qtyStk > 0)
									{
										grossPer = (grossWeight / qtyStk) ;
										grossPer = df.parse(df.format(grossPer)).doubleValue();
										System.out.println("grossPer :"+grossPer);
										netPer 	=  (netWeight / qtyStk);
										netPer = df.parse(df.format(netPer)).doubleValue();
										System.out.println("netPer 	:"+netPer);
										tarePer	=  (tareWeight / qtyStk);
										tarePer = df.parse(df.format(tarePer)).doubleValue();
										System.out.println("tarePer	:"+tarePer);									
										grossWeight2 = (inputQty * grossPer);
										System.out.println("grossWeight2 :"+grossWeight2);
										netWeight2 = (inputQty * netPer);
										System.out.println("netWeight2 :"+netWeight2);
										tareWeight2 = (inputQty * tarePer);
										System.out.println("tareWeight2 :"+tareWeight2);	

										grossWeight2 = df.parse(df.format(grossWeight2)).doubleValue(); 	
										netWeight2	= df.parse(df.format(netWeight2)).doubleValue(); 	
										tareWeight2	= df.parse(df.format(tareWeight2)).doubleValue();
									}

									//end change 10-08-2010 

									/* -- Commented and Changes Below - Gulzar - 31/01/08 - The below calculation will be done as per case "lot_no" of nvo_bo_despatch
									if (qtyStk > 0)
									{
										grossPer = (grossWeight / qtyStk) ;
										grossPer = df.parse(df.format(grossPer)).doubleValue();
										System.out.println("grossPer :"+grossPer);
										netPer 	=  (netWeight / qtyStk);
										netPer = df.parse(df.format(netPer)).doubleValue();
										System.out.println("netPer 	:"+netPer);
										tarePer	=  (tareWeight / qtyStk);
										tarePer = df.parse(df.format(tarePer)).doubleValue();
										System.out.println("tarePer	:"+tarePer);

										grossWeight = (inputQty * grossPer);
										System.out.println("grossWeight :"+grossWeight);
										netWeight = (inputQty * netPer);
										System.out.println("netWeight :"+netWeight);
										tareWeight = (inputQty * tarePer);
										System.out.println("tareWeight :"+tareWeight);	
									}
									 */
									//End Comment - Gulzar - 31/01/08
									//Added - Gulzar - 23/01/08
									netWtPerArt = convRtuomStd;//Changed and added by Pavan R 10jan19[to handle open cursor issue]
									sql3 = "SELECT ITEM_TYPE, UNIT__NETWT, UNIT__RATE FROM ITEM WHERE ITEM_CODE = ?" ;    //'" + itemCodeOrd + "'";
									//rs3 = stmt3.executeQuery(sql3);
									pstmt3 = conn.prepareStatement(sql3);
									pstmt3.setString(1, itemCodeOrd);
									rs3 = pstmt3.executeQuery();									
									if (rs3.next())
									{
										itemType = rs3.getString(1);
										if (itemType == null)
										{
											itemType = "";
										}
										netWtUnit = rs3.getString(2);
										if (netWtUnit == null)
										{
											netWtUnit = "";
										}
										rateUnit = rs3.getString(3);
										if (rateUnit == null)
										{
											rateUnit = "";
										}
									}
									rs3.close();
									rs3 = null;
									pstmt3.close();pstmt3 = null;
									if (itemType.trim().length() > 0)
									{	//Changed and added by Pavan R 10jan19[to handle open cursor issue]
										sql3 = "SELECT RATE_OPT FROM ITEM_TYPE WHERE ITEM_TYPE = ?";	//'" + itemType + "'" ;
										//rs3 = stmt3.executeQuery(sql3);
										pstmt3 = conn.prepareStatement(sql3);
										pstmt3.setString(1, itemType);
										rs3 = pstmt3.executeQuery();
										if (rs3.next())
										{
											rateOpt = rs3.getString(1);
											if (rateOpt == null)
											{
												rateOpt = "";
											}
										}
										rs3.close();rs3 = null;
										pstmt3.close();pstmt3 = null;
										if (rateOpt == "1")
										{
											if ( netWtUnit.trim().equals(rateUnit.trim()))
											{
												if (!unit1.trim().equals(rateUnitSord.trim()))
												{
													netWtPerArt = grossWtPerArt - tareWtPerArt;
												}
											}
										}
									}
									// End Addition  - Gulzar - 23/01/08

									/*valueXmlString.append("<Detail>\r\n");
									valueXmlString.append("<loc_code>").append("<![CDATA[").append(locCode.trim()).append("]]>").append("</loc_code>\r\n");
									valueXmlString.append("</Detail>\r\n");*/

									//unit1 = genericUtility.getColumnValue("unit",dom);	
									System.out.println("unit :"+unit1);
									//unitStd1 = genericUtility.getColumnValue("unit__std",dom);	
									System.out.println("unitStd :"+unitStd1);

									qtyStd = inputQty;
									System.out.println("qtyStd :"+qtyStd);
									if (!unit1.equals(unitStd1))
										//if (unit1 == null)
									{
										System.out.println("Calling getConvQuantity...........");
										System.out.println("unitStd1 :"+unitStd1+" \nunit1 :"+unit1+" \nitemCode :"+itemCode+" \nqtyStd :"+qtyStd+" \nconv :"+conv); 
										qtyFact = distCommon.getConvQuantityFact(unitStd1, unit1, itemCode, qtyStd, conv, conn);
										//System.out.println("qtyFact.get(1) :"+qtyFact.get(1));
										//System.out.println("qtyFact.get(1) :"+qtyFact.get(2));
										
									}
									else
									{
										qtyFact.add(Integer.toString(1));
										qtyFact.add(Double.toString(qtyStd));
									}
									System.out.println("qtyFact.size() :"+qtyFact.size());
									conv = (Double.parseDouble(qtyFact.get(0).toString()));
									convQtyStduom = (Double.parseDouble(qtyFact.get(0).toString())); //Gulzar - 23/01/08
									System.out.println("conv :"+conv);
									System.out.println("convQtyStduom :["+convQtyStduom+"]");

									// Commented - Gulzar - 23/01/08
									//noArt = new Double((Double.parseDouble(qtyFact.get(1).toString())) / qtyPerArt).intValue();
									// End Comment - Gulzar - 23/01/08

									// Added - Gulzar - 23/01/08
									inputQty = (Double.parseDouble(qtyFact.get(1).toString()));
									qtyStd = inputQty;
									System.out.println ("qtyStd :::::::::::"+ qtyStd);
									System.out.println("bal Qty  (double) :: "+Double.parseDouble(qtyFact.get(1).toString()));
									double[] noArtInfo = getNoArt(siteCodeDet , custCode , itemCode , packCode , Double.parseDouble(qtyFact.get(1).toString()) , "B" , 0 , 0 , conn); 
									System.out.println("getNoArt :: "+noArtInfo[0]+" "+noArtInfo[1]+" "+noArtInfo[2]);
									/*-- Temporarly Commented  - Gulzar - 30/01/08
									if (noArtInfo[0] == 0 && qtyPerArt > 0)
									{
										System.out.println("qtyPerArt ::"+qtyPerArt);
										noArt = new Double((Double.parseDouble(qtyFact.get(1).toString())) / qtyPerArt).intValue();
										System.out.println("noArt [qtyFact.get(1).toString())) / qtyPerArt)] ::"+noArt);
									}*/
									//End Temporarly Commented  - Gulzar - 30/01/08
									System.out.println("qtyPerArt ::"+qtyPerArt);
									if ( qtyPerArt > 0 )
									{
										noArt = new Double((Double.parseDouble(qtyFact.get(1).toString())) / qtyPerArt).intValue();
										System.out.println("noArt [qtyFact.get(1).toString())) / qtyPerArt)] ::"+noArt);
										grossWeight = (df.parse(df.format(grossWtPerArt / qtyPerArt)).doubleValue()) * qtyStk;
										tareWeight	= (df.parse(df.format(tareWtPerArt / qtyPerArt)).doubleValue()) * qtyStk;
										netWeight	= df.parse(df.format(grossWeight - tareWeight)).doubleValue();
										System.out.println("grossWeight [if ( qtyPerArt > 0 )] ::"+grossWeight);
										System.out.println("tareWeight [if ( qtyPerArt > 0 )] ::"+tareWeight);
										System.out.println("netWeight [if ( qtyPerArt > 0 )] ::"+netWeight);
									}
									//End Add - Gulzar - 30/01/08
									packQty = 0d; 
									if ( noArt == 0)
									{
										noArt = 1;
									}
									if (noArt > 0)
									{
										packQty = 0d;
										packQty = (Double.parseDouble(qtyFact.get(1).toString())) / noArt;
										packQty = df.parse(df.format(packQty)).doubleValue();
									}

									shipperQtyNew = noArtInfo[1];
									integralQtyNew = noArtInfo[2];

									System.out.println("bal Qty  (double) :: "+Double.parseDouble(qtyFact.get(1).toString()));
									double[] noArtInfo1 = getNoArt(siteCodeDet , custCode , itemCode , packCode , Double.parseDouble(qtyFact.get(1).toString()) , "S" , 0 , 0 , conn); 
									balQty = Double.parseDouble(qtyFact.get(1).toString()) - ( shipperQtyNew * noArtInfo1[0]);
									double[] noArtInfo2 = getNoArt(siteCodeDet , custCode , itemCode , packCode , balQty , "I" , 0 , 0 , conn); 
									integralQtyNew = noArtInfo2[2];

									shipperQtyNew = shipperQtyNew * noArtInfo1[0];
									integralQtyNew = integralQtyNew * noArtInfo2[0];

									looseQty = Double.parseDouble(qtyFact.get(1).toString()) - ( shipperQtyNew + integralQtyNew );

									qtyDetailStr = "Shipper Quantity = " + shipperQtyNew +"  Integral Quantity = " + integralQtyNew + "  Loose Quantity = " + looseQty ;

									if ( packCode != null && packCode.trim().length() > 0 )
									{	//Changed and added by Pavan R 10jan19[to handle open cursor issue]
										sqlTemp = "	SELECT GROSS_WEIGHT,NETT_WEIGHT FROM  PACKING 	WHERE  PACK_CODE = ?";	//'"+ packCode +"' ";
										//System.out.println("sqlTemp ::"+sqlTemp);
										//rsTemp = stmtTemp.executeQuery(sqlTemp);
										pstmt3  = conn.prepareStatement(sqlTemp);
										pstmt3.setString(1, packCode);
										rsTemp = pstmt3.executeQuery();
										if ( rsTemp.next() )
										{
											packGrossWeight = rsTemp.getDouble("GROSS_WEIGHT");
											packNettWeight = rsTemp.getDouble("NETT_WEIGHT");
										}
										rsTemp.close();rsTemp = null;
										pstmt3.close();pstmt3 = null;
										System.out.println("packGrossWeight ::"+packGrossWeight);
										System.out.println("packNettWeight ::"+packNettWeight);
										System.out.println("noArt ::"+noArt);

										packGrossWeight = packGrossWeight * noArt;
										packNettWeight = packNettWeight * noArt;
										if ( grossWeight == 0 )
										{
											grossWeight = packGrossWeight;
											netWeight	= packNettWeight;
											tareWeight = grossWeight - netWeight;
										}
										//End Gulzar - 29/01/08
									}
									//Changed and added by Pavan R 10jan19[to handle open cursor issue]
									sqlTemp = "SELECT APPLY_PRICE  FROM BOM WHERE BOM_CODE = ?";	//'"+ itemCode +"' ";
									//System.out.println("sqlTemp ::"+sqlTemp);
									//rsTemp = stmtTemp.executeQuery(sqlTemp);
									pstmt3  = conn.prepareStatement(sqlTemp);
									pstmt3.setString(1, itemCode);
									rsTemp = pstmt3.executeQuery();
									if ( rsTemp.next() )
									{
										applyPrice = rsTemp.getString("APPLY_PRICE");
									}
									rsTemp.close();rsTemp = null;
									pstmt3.close();pstmt3 = null;
									if ( applyPrice == null )
									{	//Changed and added by Pavan R 10jan19[to handle open cursor issue]
										sqlTemp = "SELECT DISCOUNT FROM SORDDET WHERE SALE_ORDER = ?"  // '"+ sordNo +"' AND "
										+" AND LINE_NO = ?";	//'"+ lineNoOrd +"' ";
										//System.out.println("sqlTemp ::"+sqlTemp);
										//rsTemp = stmtTemp.executeQuery(sqlTemp);
										pstmt3  = conn.prepareStatement(sqlTemp);
										pstmt3.setString(1, sordNo);
										pstmt3.setString(2, lineNoOrd);
										rsTemp = pstmt3.executeQuery();										
										if ( rsTemp.next() )
										{
											discAmt = rsTemp.getDouble("DISCOUNT");
										}
										rsTemp.close();rsTemp = null;
										pstmt3.close();pstmt3 = null;
										discAmt = (discAmt/100) * ( qtyStd *  rateStduom ); 
										System.out.println("qtyStd from actionDefault 2 ::::::::" + qtyStd);
										System.out.println("rateStduom from actionDefault 2 ::::::::" + rateStduom);
										System.out.println("discAmt from actionDefault 2 ::::::::" + discAmt);
									}

									grossWeight = df.parse(df.format(grossWeight)).doubleValue();
									netWeight	= df.parse(df.format(netWeight)).doubleValue();
									tareWeight	= df.parse(df.format(tareWeight)).doubleValue();

									discount = rs.getDouble("DISCOUNT"); 
									taxClass = rs.getString("TAX_CLASS");
									taxChap	 = rs.getString("TAX_CHAP"); 
									taxEnv	 = rs.getString("TAX_ENV"); 

									//End - Add - Gulzar - 23/01/08
									/* -- Commented Changes Below - Gulzar - 23/01/08
									valueXmlString.append("<Detail>\r\n");
									valueXmlString.append("<line_no__sord isSrvCallOnChg='1'>").append("<![CDATA[").append(lineNoOrd.trim()).append("]]>").append("</line_no__sord>\r\n");
									valueXmlString.append("<item_code isSrvCallOnChg='0'>").append("<![CDATA[").append(itemCode.trim()).append("]]>").append("</item_code>\r\n");
									valueXmlString.append("<loc_code isSrvCallOnChg='1'>").append("<![CDATA[").append(locCode.trim()).append("]]>").append("</loc_code>\r\n"); //made isSrvCallOnChg='1' - Gulzar 14/03/07 - For seting of mfg_date and exp_date
									valueXmlString.append("<quantity isSrvCallOnChg='1'>").append("<![CDATA[").append(qtyFact.get(1).toString()).append("]]>").append("</quantity>\r\n");  //made isSrvCallOnChg='1'  - Gulzar 14/03/07
									valueXmlString.append("<exp_lev isSrvCallOnChg='1'>").append("<![CDATA[").append(expLev.trim()).append("]]>").append("</exp_lev>\r\n"); //Un-Commented and made isSrvCallOnChg='1' - Gulzar 14/03/07
									//valueXmlString.append("<quantity_real isSrvCallOnChg='0'>").append("<![CDATA[").append(qtyStd).append("]]>").append("</quantity_real>\r\n");
									//valueXmlString.append("<quantity__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(qtyStd).append("]]>").append("</quantity__stduom>\r\n");
									//valueXmlString.append("<conv__qty_stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(1 / conv).append("]]>").append("</conv__qty_stduom>\r\n");
									valueXmlString.append("<pending_qty isSrvCallOnChg='0'>").append("<![CDATA[").append(qtyOrd).append("]]>").append("</pending_qty>\r\n");
									valueXmlString.append("<lot_no isSrvCallOnChg='1'>").append("<![CDATA[").append(lotNo.trim()).append("]]>").append("</lot_no>\r\n");
									valueXmlString.append("<lot_sl isSrvCallOnChg='1'>").append("<![CDATA[").append(lotSl.trim()).append("]]>").append("</lot_sl>\r\n");
									valueXmlString.append("<gross_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(grossWeight).append("]]>").append("</gross_weight>\r\n");
									valueXmlString.append("<nett_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(netWeight).append("]]>").append("</nett_weight>\r\n");
									valueXmlString.append("<tare_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(tareWeight).append("]]>").append("</tare_weight>\r\n");
									valueXmlString.append("<no_art isSrvCallOnChg='0'>").append("<![CDATA[").append(noArt).append("]]>").append("</no_art>\r\n");
									//valueXmlString.append("<dimension isSrvCallOnChg='0'>").append("<![CDATA[").append(dimension).append("]]>").append("</dimension>\r\n"); //Gulzar 14/03/07
									valueXmlString.append("<dimension isSrvCallOnChg='0'>").append("<![CDATA[").append((dimension == null) ? "":dimension).append("]]>").append("</dimension>\r\n"); //Gulzar 14/02/07
									valueXmlString.append("<site_code__mfg isSrvCallOnChg='0'>").append("<![CDATA[").append(siteCodeMfg).append("]]>").append("</site_code__mfg>\r\n");
	//								valueXmlString.append("<mfg_date>").append("<![CDATA[").append(mfgDate).append("]]>").append("</mfg_date>\r\n");
	//								valueXmlString.append("<expDate>").append("<![CDATA[").append(mfgDate).append("]]>").append("</expDate>\r\n");
									valueXmlString.append("</Detail>\r\n");
									 */
									//End  Comment Gulzar - 23/01/08 
									valueXmlString.append("<Detail>\r\n");
									valueXmlString.append("<sord_no isSrvCallOnChg='0'>").append("<![CDATA[").append(sordNo).append("]]>").append("</sord_no>\r\n"); 
									setNodeValue( dom, "sord_no", (sordNo == null) ? "":sordNo );
									//commented by rajendra as per subrato sir 
									//valueXmlString.append("<line_no__sord isSrvCallOnChg='0'>").append("<![CDATA[").append(lineNoOrd).append("]]>").append("</line_no__sord>\r\n");

									valueXmlString.append("<line_no__sord isSrvCallOnChg='0'>").append("<![CDATA[").append(lineNoOrd).append("]]>").append("</line_no__sord>\r\n");
									setNodeValue( dom, "line_no__sord", (lineNoOrd == null) ? "":lineNoOrd );
									//lineNoOrd = getChangeSord(dom,  dom1, "line_no__sord", xtraParams ,conn); // chg4 
									//valueXmlString.append(lineNoOrd);
									valueXmlString.append("<exp_lev isSrvCallOnChg='0'>").append("<![CDATA[").append(expLev).append("]]>").append("</exp_lev>\r\n"); 
									setNodeValue( dom, "exp_lev", (expLev == null) ? "":expLev );
									//expLev = getChangeSord(dom,  dom1, "exp_lev", xtraParams ,conn); // chg5
									//valueXmlString.append(expLev);
									valueXmlString.append("<item_code isSrvCallOnChg='0'>").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code>\r\n");
									setNodeValue( dom, "item_code", (itemCode == null) ? "":itemCode );
									valueXmlString.append("<item_descr isSrvCallOnChg='0'>").append("<![CDATA[").append(itemDescr).append("]]>").append("</item_descr>\r\n");
									setNodeValue( dom, "item_descr", (itemDescr == null) ? "":itemDescr );
									valueXmlString.append("<item_code__ord isSrvCallOnChg='0'>").append("<![CDATA[").append(itemCodeOrd).append("]]>").append("</item_code__ord>\r\n");
									setNodeValue( dom, "item_code__ord", (itemCodeOrd == null) ? "":itemCodeOrd );
									//valueXmlString.append("<quantity__ord isSrvCallOnChg='0'>").append("<![CDATA[").append(orderedQty).append("]]>").append("</quantity__ord>\r\n");
									valueXmlString.append("<quantity__ord isSrvCallOnChg='0'>").append("<![CDATA[").append(OrdQty).append("]]>").append("</quantity__ord>\r\n");
									//setNodeValue( dom, "exp_lev", OrdQty );    // modify by cpatil on 31/01/13
									setNodeValue( dom, "quantity__ord", OrdQty );
									valueXmlString.append("<site_code isSrvCallOnChg='0'>").append("<![CDATA[").append(siteCodeDet).append("]]>").append("</site_code>\r\n");
									setNodeValue( dom, "site_code", (siteCodeDet == null) ? "":siteCodeDet );
									//valueXmlString.append("<quantity_real isSrvCallOnChg='0'>").append("<![CDATA[").append(qtyFact.get(1).toString()).append("]]>").append("</quantity_real>\r\n");
									//valueXmlString.append("<quantity_real isSrvCallOnChg='0'>").append("<![CDATA[").append(OrdQty).append("]]>").append("</quantity_real>\r\n");
									//setNodeValue( dom, "quantity_real", OrdQty);
									valueXmlString.append("<unit isSrvCallOnChg='0'>").append("<![CDATA[").append(unit1).append("]]>").append("</unit>\r\n");
									setNodeValue( dom, "unit", (unit1 == null) ? "":unit1 );
									valueXmlString.append("<unit__std isSrvCallOnChg='0'>").append("<![CDATA[").append(unitStd1).append("]]>").append("</unit__std>\r\n");
									setNodeValue( dom, "unit__std", (unitStd1 == null) ? "":unitStd1 );
									valueXmlString.append("<conv__qty_stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(convQtyStduom).append("]]>").append("</conv__qty_stduom>\r\n");
									setNodeValue( dom, "conv__qty_stduom", convQtyStduom );
									valueXmlString.append("<conv__rtuom_stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(netWtPerArt).append("]]>").append("</conv__rtuom_stduom>\r\n"); 
									setNodeValue( dom, "conv__rtuom_stduom", netWtPerArt );
									//for test by msalam
									valueXmlString.append("<loc_code isSrvCallOnChg='0'>").append("<![CDATA[").append(locCode).append("]]>").append("</loc_code>\r\n"); 
									setNodeValue( dom, "loc_code", (locCode == null) ? "" : locCode );
									System.out.println("28/08/10 packQty 2 [" + packQty + "]");
									//----valueXmlString.append("<pack_qty isSrvCallOnChg='0'>").append("<![CDATA[").append(packQty).append("]]>").append("</pack_qty>\r\n");  
									//----setNodeValue( dom, "pack_qty", packQty );
									valueXmlString.append("<quantity__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(qtyFact.get(1).toString()).append("]]>").append("</quantity__stduom>\r\n");

									//System.out.println( "RATE__STDUOM :: " + rateStduom );
									//commented by smalam on 041108 as this gets set in lot no item change
									//valueXmlString.append("<rate__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(rateStduom).append("]]>").append("</rate__stduom>\r\n");  
									//next line commented as it gets set from lot no item change on 041108 by msalam
									//valueXmlString.append("<rate__clg isSrvCallOnChg='0'>").append("<![CDATA[").append(rateClg).append("]]>").append("</rate__clg>\r\n"); 
									valueXmlString.append("<qty_details isSrvCallOnChg='0'>").append("<![CDATA[").append(qtyDetailStr).append("]]>").append("</qty_details>\r\n");  
									System.out.println("discAmt from actionDefault at append time 3 ::::::::" + discAmt);
									valueXmlString.append("<disc_amt isSrvCallOnChg='0'>").append("<![CDATA[").append(discAmt).append("]]>").append("</disc_amt>\r\n");  									
									valueXmlString.append("<pending_qty isSrvCallOnChg='0'>").append("<![CDATA[").append(qtyOrd).append("]]>").append("</pending_qty>\r\n");
									// start change 10-08-10 grossWeight = grossWeight2
									//----valueXmlString.append("<gross_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(grossWeight2).append("]]>").append("</gross_weight>\r\n");
									//----setNodeValue( dom, "gross_weight", grossWeight2 );
									//----valueXmlString.append("<nett_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(netWeight2).append("]]>").append("</nett_weight>\r\n");
									//----setNodeValue( dom, "nett_weight", netWeight2 );
									//----valueXmlString.append("<tare_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(tareWeight2).append("]]>").append("</tare_weight>\r\n");
									//----setNodeValue( dom, "tare_weight", tareWeight2 );
									// end change 10-08-10
									valueXmlString.append("<pallet_wt isSrvCallOnChg='0'>").append("<![CDATA[").append(palletWt).append("]]>").append("</pallet_wt>\r\n");
									setNodeValue( dom, "pallet_wt", palletWt );
									valueXmlString.append("<no_art isSrvCallOnChg='0'>").append("<![CDATA[").append(noArt).append("]]>").append("</no_art>\r\n");
									setNodeValue( dom, "no_art", noArt );
									valueXmlString.append("<dimension isSrvCallOnChg='0'>").append("<![CDATA[").append((dimension == null) ? "":dimension).append("]]>").append("</dimension>\r\n"); 
									valueXmlString.append("<site_code__mfg isSrvCallOnChg='0'>").append("<![CDATA[").append(siteCodeMfg).append("]]>").append("</site_code__mfg>\r\n");
									setNodeValue( dom, "site_code__mfg", (siteCodeMfg == null) ? "":siteCodeMfg );
									valueXmlString.append("<quantity isSrvCallOnChg='0'>").append("<![CDATA[").append(qtyFact.get(1).toString()).append("]]>").append("</quantity>\r\n");  // chg6 01-08-10
									valueXmlString.append("<quantity_real isSrvCallOnChg='0'>").append("<![CDATA[").append(qtyFact.get(1).toString()).append("]]>").append("</quantity_real>\r\n");
									setNodeValue( dom, "quantity_real", qtyFact.get(1).toString());

									//valueXmlString.append("<quantity isSrvCallOnChg='1'>").append("<![CDATA[").append(qtyFact.get(1).toString()).append("]]>").append("</quantity>\r\n");  
									System.out.println( "lot No 1 :: " + lotNo );
									valueXmlString.append("<lot_no isSrvCallOnChg='0'>").append("<![CDATA[").append(lotNo).append("]]>").append("</lot_no>\r\n");
									setNodeValue( dom, "lot_no", (lotNo == null) ? "":lotNo );
									//lotNo = getChangeSord(dom,  dom1, "lot_no", xtraParams ,conn); // chg7 16-08
									//valueXmlString.append(lotNo);
									//valueXmlString.append("<lot_sl isSrvCallOnChg='0'>").append("<![CDATA[").append(lotSl).append("]]>").append("</lot_sl>\r\n");
									//changed for item change as per subrto sir 
									System.out.println( "lot_sl :: " + lotSl );

									valueXmlString.append("<lot_sl isSrvCallOnChg='0'>").append("<![CDATA[").append(lotSl).append("]]>").append("</lot_sl>\r\n");
									setNodeValue( dom, "lot_sl", (lotSl == null) ? "":lotSl );
									//lotSl = getChangeSord(dom,  dom1, "lot_no", xtraParams ,conn); // chg8  16-08
									//valueXmlString.append(lotSl);
									valueXmlString.append("<discount isSrvCallOnChg='0'>").append(discount).append("</discount>"); //Gulzar - 21/01/08
									valueXmlString.append("<tax_class isSrvCallOnChg='0'>").append("<![CDATA[").append((taxClass == null) ?"":taxClass).append("]]>").append("</tax_class>\r\n");
									valueXmlString.append("<tax_chap isSrvCallOnChg='0'>").append("<![CDATA[").append((taxChap == null) ?"":taxChap).append("]]>").append("</tax_chap>\r\n"); 
									valueXmlString.append("<tax_env isSrvCallOnChg='0'>").append("<![CDATA[").append((taxEnv == null) ?"":taxEnv).append("]]>").append("</tax_env>\r\n"); 
									// 18-08 change 
									tempStr = getChangeSord(dom,  dom1, "line_no__sord", xtraParams ,conn); // chg4 
									valueXmlString.append(tempStr);
									tempStr = getChangeSord(dom,  dom1, "exp_lev", xtraParams ,conn); // chg5
									valueXmlString.append(tempStr);
									// 15/11/10 
									valueXmlString.append("<quantity isSrvCallOnChg='0'>").append("<![CDATA[").append(qtyFact.get(1).toString()).append("]]>").append("</quantity>\r\n");  // chg6 01-08-10
									setNodeValue( dom, "quantity", qtyFact.get(1).toString() );
									// 15/11/10
									tempStr = getChangeSord(dom,  dom1, "lot_no", xtraParams ,conn); // chg7 16-08
									valueXmlString.append(tempStr);
									//tempStr = getChangeSord(dom,  dom1, "lot_no", xtraParams ,conn); // chg8  16-08
									valueXmlString.append(tempStr);
									//end change
									setNodeValue( dom, "quantity__stduom", qtyFact.get(1).toString() );
									valueXmlString.append("<quantity isSrvCallOnChg='0'>").append("<![CDATA[").append(qtyFact.get(1).toString()).append("]]>").append("</quantity>\r\n");  // chg6 01-08-10
									setNodeValue( dom, "quantity", qtyFact.get(1).toString() );

									System.out.println("manohar 07/02/11 serializeDom 2 [ " + serializeDom(dom) + "]");

									tempStr = getChangeSord(dom,  dom1, "quantity", xtraParams ,conn); // chg7 16-08
									valueXmlString.append(tempStr);
									valueXmlString.append("<quantity_real isSrvCallOnChg='0'>").append("<![CDATA[").append(qtyFact.get(1).toString()).append("]]>").append("</quantity_real>\r\n");  // chg6 01-08-10
									valueXmlString.append("<quantity__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(qtyFact.get(1).toString()).append("]]>").append("</quantity__stduom>\r\n");
									setNodeValue( dom, "quantity__stduom", qtyFact.get(1).toString() );
//									added by nandkumar gadkari on 27-09-19--------start----------------
									if(!"B".equalsIgnoreCase(lineType))
									{
										if("F".equalsIgnoreCase(nature) || "B".equalsIgnoreCase(nature) || "S".equalsIgnoreCase(nature) || "I".equalsIgnoreCase(nature) || "V".equalsIgnoreCase(nature) || "P".equalsIgnoreCase(nature) )
										{
												valueXmlString.append("<line_type isSrvCallOnChg='0'>").append("<![CDATA["+nature+"]]>").append("</line_type>");
												setNodeValue(dom, "line_type", (nature));
										}
										else
										{
											valueXmlString.append("<line_type isSrvCallOnChg='0'>").append("<![CDATA["+lineType+"]]>").append("</line_type>");
										}
										if("C".equalsIgnoreCase(nature))//added by nandkumar gadkari on 19/08/19
										{
											valueXmlString.append("<line_type isSrvCallOnChg='0'>").append("<![CDATA["+nature+"]]>").append("</line_type>");
											setNodeValue(dom, "line_type", (nature));
										}
									}
//									added by nandkumar gadkari on 27-09-19--------END----------------
									valueXmlString.append("</Detail>\r\n");
									// End Addition - Piyush - 24/12/07
									System.out.println( "manohar 28/10/10 final string :: " + valueXmlString.toString() );
									qtyOrd = qtyOrd - qtyStd;
									qtyOrd = df.parse(df.format(qtyOrd)).doubleValue();// Added - Gukzar - 23/01/08 
									System.out.println("manohar 07/02/11 Quentity Pending after [" + qtyOrd + "]"); // Added - Gukzar - 23/01/08
									System.out.println("Quentity Standard: " + qtyStd);// Added - Gukzar - 23/01/08
									grossWeight = 0;
									tareWeight = 0;
									netWeight = 0;

									allocQty = allocQty + qtyStd;
									qtyMap.put(qtyKeyStr, new Double(qtyDbl.doubleValue() + qtyStd));
									qtyFact.clear();//Added on 24/01/06
								}
								/*							if(wf_val_data)
								{
								//Here is some code is to be ask to Jiten
								}
								 */
							}
						}
						rs2.close(); rs2 = null;
						pstmt2.close(); pstmt2 = null;
					}
					else
					{
						if(itemCodeOld.indexOf(itemCode) == -1)
						{
							counter = counter + 1;
						}
						rs1.beforeFirst();
						while(rs1.next())
						{
							inputQty = rs1.getDouble("QTY_ALLOC");
							System.out.println("inputQty :"+inputQty);
							if (inputQty > 0)
							{
								expLev = rs1.getString("exp_lev");
								System.out.println("expLev :"+expLev);
								itemCodeOrd	= rs1.getString("item_code__ord");
								System.out.println("itemCodeOrd :"+itemCodeOrd);
								itemCode = rs1.getString("item_code");
								System.out.println("itemCode :"+itemCode);
								locCode = rs1.getString("loc_code");
								System.out.println("locCode :"+locCode);
								lotNo	= rs1.getString("lot_no");
								System.out.println("lotNo :"+lotNo);
								lotSl	= rs1.getString("lot_sl");
								System.out.println("lotSl :"+lotSl);
								mfgDate =	rs1.getDate("mfg_date");
								System.out.println("mfgDate :"+mfgDate);
								expDate =	rs1.getDate("exp_date");
								System.out.println("expDate :"+expDate);
								siteCode = rs1.getString("site_code");
								System.out.println("siteCode :"+siteCode);
								siteCodeMfg = rs1.getString("site_code__mfg");
								System.out.println("siteCodeMfg :"+siteCodeMfg);

								/* -- Commented And Changes Below - Gulzar - 23/01/08
								valueXmlString.append("<Detail>\r\n");
								valueXmlString.append("<line_no__sord isSrvCallOnChg='1'>").append("<![CDATA[").append(lineNoOrd.trim()).append("]]>").append("</line_no__sord>\r\n");
								valueXmlString.append("<item_code isSrvCallOnChg='0'>").append("<![CDATA[").append(itemCode.trim()).append("]]>").append("</item_code>\r\n");
								valueXmlString.append("<exp_lev isSrvCallOnChg='1'>").append("<![CDATA[").append(expLev.trim()).append("]]>").append("</exp_lev>\r\n"); //Un-Commented and made isSrvCallOnChg='1' - Gulzar 15/03/07
								valueXmlString.append("<loc_code isSrvCallOnChg='1'>").append("<![CDATA[").append(locCode.trim()).append("]]>").append("</loc_code>\r\n"); //Made isSrvCallOnChg='1' - Gulzar 15/03/07 - For seting of mfg_date and exp_date
								valueXmlString.append("<quantity isSrvCallOnChg='1'>").append("<![CDATA[").append(inputQty).append("]]>").append("</quantity>\r\n");
								valueXmlString.append("<quantity_real isSrvCallOnChg='0'>").append("<![CDATA[").append(inputQty).append("]]>").append("</quantity_real>\r\n");
								valueXmlString.append("<quantity__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(inputQty * convQtyStduom).append("]]>").append("</quantity__stduom>\r\n");
								valueXmlString.append("<lot_no isSrvCallOnChg='1'>").append("<![CDATA[").append(lotNo.trim()).append("]]>").append("</lot_no>\r\n");
								valueXmlString.append("<lot_sl isSrvCallOnChg='1'>").append("<![CDATA[").append(lotSl.trim()).append("]]>").append("</lot_sl>\r\n");
								valueXmlString.append("<site_code__mfg isSrvCallOnChg='0'>").append("<![CDATA[").append(siteCodeMfg).append("]]>").append("</site_code__mfg>\r\n");
	//							valueXmlString.append("<mfg_date>").append("<![CDATA[").append(mfgDate).append("]]>").append("</mfg_date>\r\n");
	//							valueXmlString.append("<exp_date>").append("<![CDATA[").append(expDate).append("]]>").append("</exp_date>\r\n");
								valueXmlString.append("</Detail>\r\n");
								 */
								//End Comment Gulzar - 23/01/08
								//Added - Gulzar - 23/01/08
								valueXmlString.append("<Detail>\r\n");
								//next line changed by msalam on 311008 for itemchange on lot_no as per discussion with danish and modified line added
								//valueXmlString.append("<lot_no isSrvCallOnChg='0'>").append("<![CDATA[").append(lotNo.trim()).append("]]>").append("</lot_no>\r\n");
								//System.out.println( "lotNo :: " +  lotNo.trim() );

								valueXmlString.append("<line_no__sord isSrvCallOnChg='0'>").append("<![CDATA[").append(lineNoOrd).append("]]>").append("</line_no__sord>\r\n");
								setNodeValue( dom, "line_no__sord", (lineNoOrd == null) ? "":lineNoOrd );

								valueXmlString.append("<item_code isSrvCallOnChg='0'>").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code>\r\n");
								setNodeValue( dom, "item_code", (itemCode == null) ? "":itemCode );
								//Un-Commented and made isSrvCallOnChg='1' - Gulzar 15/03/07
								valueXmlString.append("<exp_lev isSrvCallOnChg='0'>").append("<![CDATA[").append(expLev).append("]]>").append("</exp_lev>\r\n"); //Un-Commented and made isSrvCallOnChg='1' - Gulzar 15/03/07
								setNodeValue( dom, "exp_lev", (expLev == null) ? "":expLev );

								//for test by msalam
								valueXmlString.append("<loc_code isSrvCallOnChg='0'>").append("<![CDATA[").append(locCode).append("]]>").append("</loc_code>\r\n"); //Made isSrvCallOnChg='1' - Gulzar 15/03/07 - For seting of mfg_date and exp_date
								setNodeValue( dom, "loc_code", (locCode == null) ? "":locCode );
								valueXmlString.append("<quantity_real isSrvCallOnChg='0'>").append("<![CDATA[").append(inputQty).append("]]>").append("</quantity_real>\r\n");
								valueXmlString.append("<quantity__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(inputQty * convQtyStduom).append("]]>").append("</quantity__stduom>\r\n");

								//valueXmlString.append("<lot_sl isSrvCallOnChg='1'>").append("<![CDATA[").append(lotSl.trim()).append("]]>").append("</lot_sl>\r\n");
								//changed for item change as per subrto sir
								//valueXmlString.append("<lot_sl isSrvCallOnChg='1'>").append("<![CDATA[").append(lotSl.trim()).append("]]>").append("</lot_sl>\r\n");
								//changed by msalam for test on 251008
								//System.out.println( "LOt Sl For test :: " + lotSl.trim() );
								valueXmlString.append("<site_code isSrvCallOnChg='0'>").append("<![CDATA[").append(siteCode).append("]]>").append("</site_code>\r\n");
								setNodeValue( dom, "site_code", (siteCode == null) ? "":siteCode );

								valueXmlString.append("<quantity isSrvCallOnChg='0'>").append("<![CDATA[").append(inputQty).append("]]>").append("</quantity>\r\n");
								setNodeValue( dom, "quantity", inputQty );


								valueXmlString.append("<lot_no isSrvCallOnChg='0'>").append("<![CDATA[").append(lotNo).append("]]>").append("</lot_no>\r\n");								
								setNodeValue( dom, "lot_no", (lotNo == null) ? "":lotNo );

								valueXmlString.append("<lot_sl isSrvCallOnChg='0'>").append("<![CDATA[").append(lotSl).append("]]>").append("</lot_sl>\r\n");
								setNodeValue( dom, "lot_sl", (lotSl == null) ? "":lotSl );

								valueXmlString.append("<site_code__mfg isSrvCallOnChg='0'>").append("<![CDATA[").append(siteCodeMfg).append("]]>").append("</site_code__mfg>\r\n");
								setNodeValue( dom, "site_code__mfg", (siteCodeMfg == null) ? "":siteCodeMfg );
								///
								tempStr = getChangeSord(dom,  dom1, "line_no__sord", xtraParams ,conn); // chg9
								valueXmlString.append(tempStr);
								tempStr = getChangeSord(dom,  dom1, "exp_lev", xtraParams ,conn); // chg9
								valueXmlString.append(tempStr);
								System.out.println("manohar 07/02/11 serializeDom 3 [ " + serializeDom(dom) + "]");

								valueXmlString.append(getChangeSord(dom,  dom1, "quantity", xtraParams ,conn)); // chg10						
								tempStr = getChangeSord(dom,  dom1, "lot_no", xtraParams ,conn); // chg11 16-08
								valueXmlString.append(tempStr);
								tempStr = getChangeSord(dom,  dom1, "lot_sl", xtraParams ,conn); // chg12  16-08
								valueXmlString.append(tempStr);
								valueXmlString.append("<quantity isSrvCallOnChg='0'>").append("<![CDATA[").append(inputQty).append("]]>").append("</quantity>\r\n");
								valueXmlString.append("<quantity_real isSrvCallOnChg='0'>").append("<![CDATA[").append(inputQty).append("]]>").append("</quantity_real>\r\n");
								valueXmlString.append("<quantity__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(inputQty * convQtyStduom).append("]]>").append("</quantity__stduom>\r\n");
								//added by nandkumar gadkari on 27-09-19--------start----------------
								if(!"B".equalsIgnoreCase(lineType))
								{
									if("F".equalsIgnoreCase(nature) || "B".equalsIgnoreCase(nature) || "S".equalsIgnoreCase(nature) || "I".equalsIgnoreCase(nature) || "V".equalsIgnoreCase(nature) || "P".equalsIgnoreCase(nature) )
									{
											valueXmlString.append("<line_type isSrvCallOnChg='0'>").append("<![CDATA["+nature+"]]>").append("</line_type>");
											setNodeValue(dom, "line_type", (nature));
									}
									else
									{
										valueXmlString.append("<line_type isSrvCallOnChg='0'>").append("<![CDATA["+lineType+"]]>").append("</line_type>");
									}
									if("C".equalsIgnoreCase(nature))//added by nandkumar gadkari on 19/08/19
									{
										valueXmlString.append("<line_type isSrvCallOnChg='0'>").append("<![CDATA["+nature+"]]>").append("</line_type>");
										setNodeValue(dom, "line_type", (nature));
									}
								}
//								added by nandkumar gadkari on 27-09-19--------END----------------
								/*
								valueXmlString.append("<rate__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(rateStduomrt).append("]]>").append("</rate__stduom>\r\n");
								 */

								valueXmlString.append("</Detail>\r\n");

								//End Added - Gulzar - 23/01/08

								//Update statement commented by Jiten as per Piyush Sir - 04/11/06
								/*sql2 = "UPDATE SORDALLOC " 
											+"SET QTY_ALLOC	= QTY_ALLOC - ? "
											+"WHERE SALE_ORDER = ? "
											+"AND LINE_NO =	? "
											+"AND EXP_LEV =	? "
											+"AND ITEM_CODE__ORD = ? "
											+"AND ITEM_CODE	= ? "
											+"AND LOC_CODE = ? "
											+"AND LOT_NO = ? "
											+"AND LOT_SL = ? ";
								System.out.println("The Update sql :"+sql2);
								pstmt = conn.prepareStatement(sql2);							
								pstmt.setDouble(1, inputQty);
								pstmt.setString(2, sordNo);
								pstmt.setString(3, lineNoOrd);
								pstmt.setString(4, expLev);
								pstmt.setString(5, itemCodeOrd);
								pstmt.setString(6, itemCode);
								pstmt.setString(7, locCode);
								pstmt.setString(8, lotNo);
								pstmt.setString(9, lotSl);
								updateCnt = pstmt.executeUpdate();
								System.out.println("update the no of records in sordalloc :"+updateCnt);
								sql2 = "UPDATE SORDITEM "
									  +"SET QTY_ALLOC =	QTY_ALLOC -"+inputQty
									  +"WHERE SALE_ORDER = '"+sordNo+"' "
									  +"AND LINE_NO	= '"+lineNoOrd+"' "
									  +"AND EXP_LEV	= '"+expLev+"' ";

								System.out.println("The update Sql :"+sql2);	
								pstmt = conn.prepareStatement(sql2);							
								updateCnt = pstmt.executeUpdate();
								System.out.println("update the no of records in sorditem :"+updateCnt);*/
							}
						}//while end
					}//end else
					rs1.close(); rs1 = null;
					pstmt1.close();pstmt1 = null;
					itemCodeOld = itemCodeOld+ " "+itemCode;
					System.out.println("itemCodeOld :"+itemCodeOld);
				}// while end
				rs.close(); rs = null;
				pstmt.close();pstmt = null;
				//valueXmlString.append("</Root>\r\n");			
			}
			valueXmlString.append("</Root>\r\n");			
			String retXmlString = serializeDom(genericUtility.parseString(valueXmlString.toString()));
			valueXmlString = null;
			System.out.println("manohar 07/02/11 retXmlString 7 ["+ retXmlString +"]");
			valueXmlString =  new StringBuffer(retXmlString);
		}//try end
		catch(SQLException sqx)
		{
			System.out.println("The Exception caught from Despatch(Default) :"+sqx);
			throw new ITMException(sqx);
		}
		catch(Exception e)
		{
			System.out.println("The Exception caught from Despatch(Default) :"+e);
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
				if (rs1 != null)
				{
					rs1.close();
					rs1 = null;
				}
				if (rs2 != null)
				{
					rs2.close();
					rs2 = null;
				}
				if (rs3 != null)
				{
					rs3.close();
					rs3 = null;
				}
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if (pstmt1 != null)
				{
					pstmt1.close();
					pstmt1 = null;
				}
				if (pstmt2 != null)
				{
					pstmt2.close();
					pstmt2 = null;
				}
				if (pstmt3 != null)
				{
					pstmt3.close();
					pstmt3 = null;
				}
				if(conn != null)
				{
					conn.close();
					conn = null;
				}
				qtyMap = null;
			}catch(Exception e){}
		}
		System.out.println("valueXmlString.toString() :"+valueXmlString.toString());
		return valueXmlString.toString();
	}

	//End Changes - Gulzar - 23/01/08

	private String actionLotNo(Document dom,Document dom1, String objContext, String xtraParams) throws RemoteException, ITMException	
	{
		String siteCode = "", sordNo = "", sql1 = "", sql = "", errCode = "", errString = "", itemChngXmlString = "";
		String itemCode = "", quantity = "", convQqtyStduom = "", locCode = "", noArt = "", locCode1 = "";
		String lotNo = "", lotSl = "", siteCodeMfg = "", packCode = "",	dimension = "", suppCodeMfg = "";
		double quantityStk = 0d, allocQty = 0d,	grossWeight = 0d, tareWeight = 0d;
		double netPer = 0d, tarePer = 0d, netWeight = 0d, quantity1 = 0d, inputQty = 0d, grossPer = 0d;
		java.sql.Date expDate = null, mfgDate = null;
		int count = 0;
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		Connection conn = null;
		//Statement stmt = null, stmt1 = null;//Changed and added by Pavan R 10jan19[to handle open cursor issue]
		PreparedStatement pstmt = null, pstmt1 = null;
		ResultSet rs = null, rs1 = null;
		ConnDriver connDriver = new ConnDriver();
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		Document domItmChng = null;
		ValidatorLocal validator = null;

		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			//stmt = conn.createStatement();//Changed and added by Pavan R 10jan19[to handle open cursor issue]Start
			//stmt1 = conn.createStatement();			
			siteCode = genericUtility.getColumnValue("site_code",dom1);
			sordNo = genericUtility.getColumnValue("sord_no",dom1);
			lotNo = genericUtility.getColumnValue("lot_no",dom);
			sql = "SELECT LINE_NO,EXP_LEV FROM SORDITEM WHERE SALE_ORDER = ? AND LINE_TYPE = 'I'"; //'"+sordNo+"' AND LINE_TYPE = 'I'";
			//System.out.println("sql :"+sql);
			//rs = stmt.executeQuery(sql);
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, sordNo);
			rs = pstmt.executeQuery();//Changed and added by Pavan R 10jan19[to handle open cursor issue] End
			while (rs.next())
			{
				//AppConnectParm appConnect = new AppConnectParm();
				//Properties props = appConnect.getProperty();
				//InitialContext ctx = new InitialContext(props);
				//DespatchHome despatchHome = (DespatchHome)ctx.lookup("Despatch");
				//Despatch despatchRemote = despatchHome.create();
				//ValidatorHome validatorHome = (ValidatorHome)ctx.lookup("Despatch");
				//changed by Pavan R on 24jul18 as pr SM sir start
				//validator = (ValidatorLocal)ctx.lookup("ibase/Despatch/local"); // for ejb3
				//chnages end
				//Validator validator = validatorHome.create();

				System.out.println("Despatch Created");

				//itemChngXmlString = validator.itemChanged(xmlString, xmlString1, "", objContext, "exp_lev", "E", xtraParams);
				itemChngXmlString = validator.itemChanged(dom, dom1, null, objContext, "exp_lev", "E", xtraParams);
				System.out.println("itemChngXmlString :"+itemChngXmlString);
				domItmChng = genericUtility.parseString(itemChngXmlString); 

				itemCode = genericUtility.getColumnValue("item_code",domItmChng);
				System.out.println("itemCode :"+itemCode);
				quantity = genericUtility.getColumnValue("quantity",domItmChng);
				System.out.println("quantity :"+quantity);
				convQqtyStduom = genericUtility.getColumnValue("conv__qty_stduom",domItmChng);
				System.out.println("convQqtyStduom :"+convQqtyStduom);
				locCode = genericUtility.getColumnValue("loc_code",dom);
				System.out.println("locCode :"+locCode);
				if (locCode == null || locCode.trim().length() == 0)
				{
					locCode = "%";
				}
				else
				{
					locCode1 = locCode;
					locCode = locCode.trim() + "%";
				}
				//sql1 = "SELECT COUNT(A.ITEM_CODE) FROM STOCK A, INVSTAT B "
				sql1 = "SELECT COUNT(*) FROM STOCK A, INVSTAT B "
					+"WHERE A.INV_STAT  = B.INV_STAT "//Changed and added by Pavan R 10jan19[to handle open cursor issue] Start
					+"AND A.ITEM_CODE = ? "           //'"+itemCode+"' "
					+"AND A.SITE_CODE = ? "           //'"+siteCode+"' "
					+"AND A.LOC_CODE  LIKE ? "       //"+locCode+"' " 
					+"AND B.AVAILABLE = 'Y' "
					+"AND B.USABLE = 'Y' "
					+"AND A.QUANTITY  > 0 ";
				//System.out.println("sql1 :"+sql1);
				//rs1 = stmt1.executeQuery(sql1);
				pstmt1 = conn.prepareStatement(sql1);
				pstmt1.setString(1, itemCode);
				pstmt1.setString(2, siteCode);
				pstmt1.setString(3, locCode);
				rs1 = pstmt1.executeQuery();//Changed and added by Pavan R 10jan19[to handle open cursor issue] End
				if (rs1.next())
				{
					count = rs1.getInt(1);
					System.out.println("count :"+count);
				}
				rs1.close(); rs1 = null;
				pstmt1.close(); pstmt1 = null;
				if (count == 0)
				{
					//errCode = "";//Commented code by Jiten 09/10/06 - if count is zero then break from loop.
					break;
				}
				sql1 = "SELECT A.LOT_NO, A.LOT_SL, A.QUANTITY, A.EXP_DATE,A.SITE_CODE__MFG, A.MFG_DATE, "
					+"A.ALLOC_QTY, A.PACK_CODE, A.LOC_CODE, A.GROSS_WEIGHT, A.TARE_WEIGHT, "
					+"A.NET_WEIGHT, A.DIMENSION,A.SUPP_CODE__MFG FROM STOCK A, INVSTAT B "
					+"WHERE A.INV_STAT  = B.INV_STAT " //Changed and added by Pavan R 10jan19[to handle open cursor issue] Start
					+"AND A.ITEM_CODE = ? "         //'"+itemCode+"' "
					+"AND A.SITE_CODE = ? "			//'"+siteCode+"' "
					+"AND A.LOC_CODE  LIKE ? "    //"+locCode+"' "
					+"AND B.AVAILABLE = 'Y' "
					+"AND B.USABLE = 'Y' "
					+"AND A.QUANTITY - A.ALLOC_QTY > 0 "
					+"AND A.LOT_NO = ? "             //'"+lotNo+"' "
					+"ORDER BY A.EXP_DATE, A.LOT_NO, A.LOT_SL ";
				//System.out.println("sql1 :"+sql1);
				//rs1 = stmt1.executeQuery(sql1);
				pstmt1 = conn.prepareStatement(sql1);
				pstmt1.setString(1, itemCode);
				pstmt1.setString(2, siteCode);
				pstmt1.setString(3, locCode);
				pstmt1.setString(4, lotNo);//Changed and added by Pavan R 10jan19[to handle open cursor issue] End
				rs1 = pstmt1.executeQuery();
				if (rs1.next())
				{
					lotNo = rs1.getString(1);
					System.out.println("lotNo :"+lotNo);
					lotSl = rs1.getString(2);
					System.out.println("lotSl :"+lotSl);
					quantityStk = rs1.getDouble(3);
					System.out.println("quantity :"+quantity);
					expDate = rs1.getDate(4);
					System.out.println("expDate :"+expDate);
					siteCodeMfg = rs1.getString(5);
					System.out.println("siteCodeMfg :"+siteCodeMfg);
					mfgDate = rs1.getDate(6);
					System.out.println("mfgDate :"+mfgDate);
					allocQty = rs1.getDouble(7);
					System.out.println("allocQty :"+allocQty);
					packCode = rs1.getString(8);
					System.out.println("packCode :"+packCode);
					locCode = rs1.getString(9);
					System.out.println("locCode :"+locCode);
					grossWeight = rs1.getDouble(10);
					System.out.println("grossWeight :"+grossWeight);
					tareWeight = rs1.getDouble(11);
					System.out.println("tareWeight :"+tareWeight);
					netWeight = rs1.getDouble(12);
					System.out.println("netWeight :"+netWeight);
					dimension = rs1.getString(13);
					System.out.println("dimension :"+dimension);
					suppCodeMfg = rs1.getString(14);
					System.out.println("suppCodeMfg :"+suppCodeMfg);
					quantity1 = Double.parseDouble(quantity);
					if (quantityStk - allocQty <= quantity1)
					{
						inputQty = quantityStk - allocQty;
						System.out.println("inputQty :"+inputQty);
						quantity1 = quantity1 - inputQty;
						System.out.println("quantity1 :"+quantity1);
					}
					else
					{
						inputQty = quantity1;
						System.out.println("inputQty :"+inputQty);
						quantity1 = quantity1 - quantityStk - allocQty;
						System.out.println("quantity1 :"+quantity1);						
					}
					if (inputQty > 0)
					{
						if (quantityStk > 0)
						{
							grossPer = (grossWeight/quantityStk);
							System.out.println("grossPer  :"+grossPer);
							netPer = (netWeight/quantityStk);
							System.out.println("netPer :"+netPer);
							tarePer = (tareWeight/quantityStk);
							System.out.println("tarePer :"+tarePer);

							grossWeight = (inputQty * grossPer);
							System.out.println("grossWeight :"+grossWeight);
							netWeight = (inputQty * netPer);
							System.out.println("netWeight :"+netWeight);
							tareWeight = (inputQty * tarePer);
							System.out.println("tareWeight :"+tareWeight);
						}
						System.out.println("valueXmlString280710...."+valueXmlString);
						valueXmlString.append("<Detail>\r\n");
						valueXmlString.append("<loc_code>").append("<![CDATA[").append(locCode1).append("]]>").append("</loc_code>\r\n");
						valueXmlString.append("<quantity>").append("<![CDATA[").append(inputQty).append("]]>").append("</quantity>\r\n");
						valueXmlString.append("<quantity_real>").append("<![CDATA[").append(inputQty).append("]]>").append("</quantity_real>\r\n");
						valueXmlString.append("<quantity__stduom>").append("<![CDATA[").append(inputQty * Double.parseDouble(convQqtyStduom)).append("]]>").append("</quantity__stduom>\r\n");
						valueXmlString.append("<lot_no>").append("<![CDATA[").append(lotNo).append("]]>").append("</lot_no>\r\n");
						valueXmlString.append("<lot_sl>").append("<![CDATA[").append(lotSl).append("]]>").append("</lot_sl>\r\n");
						valueXmlString.append("<gross_weight>").append("<![CDATA[").append(grossWeight).append("]]>").append("</gross_weight>\r\n");
						valueXmlString.append("<nett_weight>").append("<![CDATA[").append(netWeight).append("]]>").append("</nett_weight>\r\n");
						valueXmlString.append("<tare_weight>").append("<![CDATA[").append(tareWeight).append("]]>").append("</tare_weight>\r\n");
						valueXmlString.append("<dimension>").append("<![CDATA[").append(blanknull(dimension)).append("]]>").append("</dimension>\r\n");
						valueXmlString.append("<site_code__mfg>").append("<![CDATA[").append(suppCodeMfg).append("]]>").append("</site_code__mfg>\r\n");
						valueXmlString.append("<mfg_date>").append("<![CDATA[").append(mfgDate).append("]]>").append("</mfg_date>\r\n");
						valueXmlString.append("<exp_date>").append("<![CDATA[").append(expDate).append("]]>").append("</exp_date>\r\n");
						valueXmlString.append("</Detail>\r\n");
							
						grossWeight = 0;
						tareWeight = 0;
						netWeight = 0;
					}//end if
				}// end if
				rs1.close(); rs1 = null;
				pstmt1.close();pstmt1 = null;				
			}// while end
			rs.close(); rs = null;
			pstmt.close(); pstmt = null;
			valueXmlString.append("</Root>\r\n");
			System.out.println("valueXmlString2807...."+valueXmlString);
		}
		catch (SQLException sqx)
		{
			System.out.println("The SQLException occurs in DespatchAct :"+sqx);
			sqx.printStackTrace();
		}
		catch (Exception e)
		{
			System.out.println("The Exception occurs in DespatchAct :"+e);
		}
		//21-07-2010 connection close
		finally
		{
			try
			{
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (rs1!=null)
				{
					rs1.close();
					rs1 = null;
				}
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if (pstmt1 != null)
				{
					pstmt1.close();
					pstmt1 = null;
				}
				if(conn != null)
				{
					System.out.println("Closing Connection.....");
					conn.close();
					conn = null;
				}
			}
			catch(Exception e){}
		}
		System.out.println("valueXmlString :"+valueXmlString.toString());
		return valueXmlString.toString();
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
	private String getCurrentUpdateFlag(Document dom)
	{
		NodeList detailList = null;
		Node currDetail = null;
		NodeList currDetailList = null;
		String updateStatus = "",nodeName = "";
		int currDetailListLength = 0;
		int	detailListLength = 0;

		detailList = dom.getElementsByTagName("Detail2");
		detailListLength = detailList.getLength();
		for (int ctr = 0;ctr < detailListLength;ctr++)
		{
			currDetail = detailList.item(ctr);
		}
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
		System.out.println("updateStatus in [getCurrentUpdateFlag()] :: " + updateStatus);
		return updateStatus;
	}

	private String actionWoGetPackList(Document dom, Document dom1, String objContext, String xtraParams) throws RemoteException, ITMException
	{
		ResultSet rs = null;
		Connection conn = null;
		//Statement stmt = null;//Changed and added by Pavan R 10jan19[to handle open cursor issue]
		PreparedStatement pstmt = null;
		StringBuffer valueXmlString = null;
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		String sql=null;
		boolean isFound=false;
		String errString="";
		String errCode="";
		String saleOrder = "";
		ArrayList itemCodeList = new ArrayList();
		String itemCodeStr = "";
		HashMap itemMap = new HashMap();

		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			//stmt = conn.createStatement();

			saleOrder = genericUtility.getColumnValue("sord_no",dom1);

			sql = "select sorddet.line_no,sorddet.item_code,"+
			"sorddet.quantity,case when despatchdet.quantity is null " +
			"then 0 else despatchdet.quantity end from sorder,sorddet left outer join despatchdet "+ 
			"on sorddet.sale_order = despatchdet.sord_no and sorddet.line_no = despatchdet.line_no__sord "+ 
			"where sorder.sale_order = sorddet.sale_order "+
			"and sorder.confirmed = 'Y' and sorddet.status <> 'C' and " +
			"case when sorddet.quantity is null then 0 else sorddet.quantity end - " +
			"case when despatchdet.quantity is null then 0 else despatchdet.quantity end > 0 "+ 
			"and sorder.sale_order = ?";   //'"+saleOrder+"'";
			//Changed and added by Pavan R 10jan19[to handle open cursor issue] Start
			/*System.out.println("SQL :"+sql);
			rs = stmt.executeQuery(sql);*/
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, saleOrder);
			rs = pstmt.executeQuery();//Changed and added by Pavan R 10jan19[to handle open cursor issue] End
			while(rs.next()){
				itemCodeList.add(rs.getString(2));
				itemMap.put(rs.getString(2),rs.getString(1));
			}
			rs.close();rs = null; pstmt.close(); pstmt = null;
			for(int i = 0;i < itemCodeList.size(); i++){
				itemCodeStr = itemCodeStr+"'"+itemCodeList.get(i)+"',";
			}

			if(itemCodeStr.trim().length() > 0){
				itemCodeStr = itemCodeStr.substring(0,itemCodeStr.length()-1);
				System.out.println("itemCodeStr "+itemCodeStr);
			}else{
				itemCodeStr = "''";
			}


			sql="SELECT INV_PACK_RCP.TRAN_ID,INV_PACK_RCP.LINE_NO,"
				+" INV_PACK_RCP.ORDER_NO,INV_PACK_RCP.LINE_NO__ORD,INV_PACK_RCP.ITEM_CODE,"
				+" INV_PACK_RCP.LOC_CODE,INV_PACK_RCP.LOT_NO,INV_PACK_RCP.LOT_SL,"
				+" STOCK.QUANTITY,INV_PACK_RCP.UNIT,"
				+" INV_PACK_RCP.PACK_CODE,INV_PACK_RCP.PACK_INSTR,"
				+" INV_PACK_RCP.GROSS_WEIGHT,INV_PACK_RCP.TARE_WEIGHT,"
				+" INV_PACK_RCP.NET_WEIGHT,INV_PACK_RCP.NO_ART,INV_PACK_RCP.DIMENSION,"
				+" 'Y' AS PROCESS_YN "
				+" FROM INV_PACK, INV_PACK_RCP, STOCK "
				+" WHERE INV_PACK.TRAN_ID = INV_PACK_RCP.TRAN_ID "
				+" AND INV_PACK_RCP.ITEM_CODE IN (?) "                  //("+itemCodeStr+") "
				+" AND INV_PACK_RCP.ITEM_CODE = STOCK.ITEM_CODE "
				+" AND INV_PACK.SITE_CODE = STOCK.SITE_CODE "
				+" AND INV_PACK_RCP.LOC_CODE = STOCK.LOC_CODE "
				+" AND INV_PACK_RCP.LOT_NO = STOCK.LOT_NO "
				+" AND INV_PACK_RCP.LOT_SL = STOCK.LOT_SL "
				+" AND INV_PACK.CONFIRMED  = 'Y' "
				+" AND INV_PACK.ORDER_TYPE = 'W'"
				+" AND (STOCK.QUANTITY - STOCK.ALLOC_QTY) > 0 "                    
				+" AND STOCK.PACK_REF IS NOT NULL "
				+" ORDER BY INV_PACK_RCP.LINE_NO ASC";
			//Changed and added by Pavan R 10jan19[to handle open cursor issue] Start
			//System.out.println("SQL "+sql);

			//rs = stmt.executeQuery(sql);
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, itemCodeStr);
			rs = pstmt.executeQuery();//Changed and added by Pavan R 10jan19[to handle open cursor issue] End
			valueXmlString=new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
			while (rs.next())
			{				
				valueXmlString.append("<Detail>\r\n");
				valueXmlString.append("<tran_id>").append("<![CDATA[").append(blanknull(rs.getString(1))).append("]]>").append("</tran_id>\r\n");
				valueXmlString.append("<order_no>").append("<![CDATA[").append(blanknull(saleOrder)).append("]]>").append("</order_no>\r\n");
				valueXmlString.append("<line_no__sord>").append("<![CDATA[").append(itemMap.get(rs.getString(5)).toString()).append("]]>").append("</line_no__sord>\r\n");
				valueXmlString.append("<item_code>").append("<![CDATA[").append(blanknull(rs.getString(5))).append("]]>").append("</item_code>\r\n");
				valueXmlString.append("<loc_code>").append("<![CDATA[").append(blanknull(rs.getString(6))).append("]]>").append("</loc_code>\r\n");
				valueXmlString.append("<lot_no>").append("<![CDATA[").append(blanknull(rs.getString(7))).append("]]>").append("</lot_no>\r\n");
				valueXmlString.append("<lot_sl>").append("<![CDATA[").append(blanknull(rs.getString(8))).append("]]>").append("</lot_sl>\r\n");
				valueXmlString.append("<quantity>").append("<![CDATA[").append(rs.getDouble(9)).append("]]>").append("</quantity>\r\n");
				valueXmlString.append("<unit>").append("<![CDATA[").append(blanknull(rs.getString(10))).append("]]>").append("</unit>\r\n");
				valueXmlString.append("<pack_code>").append("<![CDATA[").append(blanknull(rs.getString(11))).append("]]>").append("</pack_code>\r\n");
				valueXmlString.append("<pack_instr>").append("<![CDATA[").append(blanknull(rs.getString(12))).append("]]>").append("</pack_instr>\r\n");
				valueXmlString.append("<gross_weight>").append("<![CDATA[").append(rs.getDouble(13)).append("]]>").append("</gross_weight>\r\n");
				valueXmlString.append("<tare_weight>").append("<![CDATA[").append(rs.getDouble(14)).append("]]>").append("</tare_weight>\r\n");	
				valueXmlString.append("<net_weight>").append("<![CDATA[").append(rs.getDouble(15)).append("]]>").append("</net_weight>\r\n");
				valueXmlString.append("<no_art>").append("<![CDATA[").append(rs.getString(16)).append("]]>").append("</no_art>\r\n");
				valueXmlString.append("<dimensions>").append("<![CDATA[").append(blanknull(rs.getString(17))).append("]]>").append("</dimensions>\r\n");

				valueXmlString.append("</Detail>\r\n");
				isFound=true;
				System.out.println("[DespatchActforWo]SQL :"+sql);
			}//end of while
			valueXmlString.append("</Root>\r\n");		
			rs.close();
			rs=null;
			pstmt.close();
			pstmt = null;
			System.out.println("[DespatchActforWo]valueXmlString=>"+valueXmlString.toString());		
			System.out.println("[DespatchActforWo]isFound=>"+isFound);	
			if(!isFound)
			{
				errCode="VTPORD9";
				errString = itmDBAccess.getErrorString("",errCode,"","",conn);
				return errString;
			}
		}//try end
		catch(Exception e)
		{
			System.out.println("Exception :Despatch :" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(rs!=null)
				{
					rs.close();
					rs=null;
				}
				if(pstmt!=null)
				{
					pstmt.close();
					pstmt=null;
				}
				System.out.println("Closing Connection.....");
				if(conn != null)
				{
					conn.close();
					conn = null;
				}
			}catch(Exception e){}
		}
		System.out.println("valueXmlString.toString() "+valueXmlString.toString());
		return valueXmlString.toString();		
	}	

	private String getWoPackListTransform(Document dom,Document dom1, String objContext, String xtraParams, Document selDataDom)throws ITMException
	{
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		String orderNo ="";
		String quantity ="0.0";
		String itemCode="";
		String itemDescr="";
		PreparedStatement pstmt=null,pstmt1=null;
		ResultSet rs=null,rs1=null;
		Connection conn=null;
		String sql=null,sql1=null;
		String lineNo = "", tempStr = "";
		String locCode = "",lotNo = "",lotSl = "";

		try
		{
			NodeList detailList = selDataDom.getElementsByTagName("Detail");
			int noOfDetails = detailList.getLength();
			ConnDriver connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 

			for(int ctr = 0; ctr < noOfDetails; ctr++)
			{
				valueXmlString.append("<Detail>");
				Node currDetail = detailList.item(ctr);

				orderNo = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("order_no", currDetail);
				quantity = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("quantity", currDetail);
				itemCode = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("item_code", currDetail);
				lineNo = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("line_no__sord", currDetail);
				locCode = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("loc_code", currDetail);
				lotNo = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("lot_no", currDetail);
				lotSl = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("lot_sl", currDetail);

				sql="select descr from item where item_code='"+itemCode+"'";

				pstmt=conn.prepareStatement(sql);
				rs=pstmt.executeQuery();
				if(rs.next())
				{
					itemDescr=rs.getString(1);
				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;

				System.out.println("sql in getPackListTransform :: "+sql);
				System.out.println("orderNo in getPackListTransform :: " + orderNo);
				System.out.println("quantity In getPackListTransform :: " + quantity);
				System.out.println("itemCode In getPackListTransform :: " + itemCode);

				valueXmlString.append("<sord_no isSrvCallOnChg='0'>").append(orderNo).append("</sord_no>");

				valueXmlString.append("<line_no__sord isSrvCallOnChg='0'><![CDATA[").append(lineNo).append("]]></line_no__sord>\r\n");
				setNodeValue( dom, "line_no__sord", (lineNo == null) ? "":lineNo );
				tempStr = getChangeSord(dom,  dom1, "line_no__sord", xtraParams ,conn); // chg13
				valueXmlString.append(tempStr);
				valueXmlString.append("<item_code isSrvCallOnChg='0'><![CDATA[").append(itemCode).append("]]></item_code>");
				valueXmlString.append("<item_descr isSrvCallOnChg='0'>").append(itemDescr).append("</item_descr>");				

				valueXmlString.append("<quantity isSrvCallOnChg='0'>").append(quantity).append("</quantity>");
				setNodeValue( dom, "quantity", quantity);
				System.out.println("manohar 07/02/11 serializeDom 4 [ " + serializeDom(dom) + "]");
				tempStr = getChangeSord(dom,  dom1, "quantity", xtraParams ,conn); // chg14
				System.out.println("manohar 07/02/11 tempStr 1 [" + tempStr + "]");
				valueXmlString.append(tempStr);
				valueXmlString.append("<loc_code>").append("<![CDATA[").append(locCode).append("]]>").append("</loc_code>\r\n");
				valueXmlString.append("<lot_no>").append("<![CDATA[").append(lotNo).append("]]>").append("</lot_no>\r\n");
				valueXmlString.append("<lot_sl>").append("<![CDATA[").append(lotSl).append("]]>").append("</lot_sl>\r\n");
				valueXmlString.append("</Detail>");								
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
		finally
		{
			try
			{
				if (rs != null)			
				{
					rs.close();
					rs = null;
				}
				if (rs1 != null)
				{
					rs1.close();
					rs1 = null;
				}
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if (pstmt1 != null)
				{
					pstmt1.close();
					pstmt1 = null;
				}			
				if(conn!=null)
				{
					conn.close();
					conn = null;
				}
			}
			catch (Exception e)
			{
				throw new ITMException(e);
			}
		}
		return valueXmlString.toString();
	}

	public String blanknull(String s)
	{
		if(s==null)
			return "";
		else
			return s.trim();
	}

	private String actionPacking(Document dom,Document dom1, String objContext, String xtraParams) throws RemoteException , ITMException
	{
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		String tranIdInvpack = "", siteCode = "";
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		ConnDriver connDriver = new ConnDriver();
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		try
		{	
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			siteCode = genericUtility.getColumnValue("site_code",dom1);

			sql = " SELECT DISTINCT B.TRAN_ID FROM INV_PACK A, INV_PACK_RCP B, STOCK C "
				+ " WHERE A.TRAN_ID = B.TRAN_ID AND A.CONFIRMED = 'Y' AND A.ORDER_TYPE = 'N' "
				+ " AND A.SITE_CODE = '" + siteCode + "'AND (A.STATUS IS NULL OR A.STATUS <> 'X') "
				+ " AND B.ITEM_CODE = C.ITEM_CODE AND A.SITE_CODE = C.SITE_CODE "
				+ " AND B.LOC_CODE = C.LOC_CODE AND B.LOT_NO=C.LOT_NO AND B.LOT_SL=C.LOT_SL "
				+ " AND C.QUANTITY-C.ALLOC_QTY >0 AND C.PACK_REF IS NOT NULL";
			System.out.println("sql for popup:::" + sql);
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				tranIdInvpack = rs.getString("TRAN_ID");
				//				orderType = rs.getString("ORDER_TYPE");
				//				orderNo = rs.getString("ORDER_NO");
				valueXmlString.append("<Detail>");
				valueXmlString.append("<inv_pack_id>").append("<![CDATA[").append(tranIdInvpack).append("]]>").append("</inv_pack_id>\r\n");
				valueXmlString.append("<site_code>").append("<![CDATA[").append(siteCode).append("]]>").append("</site_code>\r\n");
				valueXmlString.append("</Detail>");
			}//end While loop
			rs.close(); rs = null;
			pstmt.close();pstmt = null;
			valueXmlString.append("</Root>\r\n");
		}
		catch(Exception e)
		{
			System.out.println("Exception :Despatch :" + e.getMessage() + ":");
			e.printStackTrace();
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
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				System.out.println("Closing Connection.....");
				if(conn != null)
				{
					conn.close();
					conn = null;
				}
			}catch(Exception e){}
		}
		System.out.println("Despatch:actionStock:Final Value :valueXmlString.toString():"+valueXmlString.toString());
		return valueXmlString.toString();
	}

	private String actionPackingTransform(Document dom, Document dom1, String objContext, String xtraParams, Document selDataDom) throws ITMException
	{
		Connection conn = null;
		PreparedStatement pstmt = null, pstmtData = null, pstmtUpdate = null;
		ResultSet rs = null, rsData = null;
		NodeList detailList = null;
		String sql = "", errString = "", newMessage = ""; 
		String tranIdInvpack = "", custCode = "", itemCode = "", siteCode = "", locCode = "", lotSl = "", lotNo = "";
		String saleOrder = "", lineNoSord = "", expLevel = "", detailCnt = "", tempStr = "";
		double invPackQty = 0, pendQty = 0, despQty = 0, qtyAlloc = 0, availQty = 0, stkAllocQty = 0;
		int detCnt = 0;
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		ConnDriver connDriver = new ConnDriver();
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
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver = null;
			conn.setAutoCommit(false);
			custCode = genericUtility.getColumnValue("cust_code",dom1);
			siteCode = genericUtility.getColumnValue("site_code",dom1);
			detailList = selDataDom.getElementsByTagName("Detail");
			int noOfDetails = detailList.getLength();
			//			for(int ctr = 0; ctr < noOfDetails; ctr++)
			//			{
			//				Node currDetail = detailList.item(ctr);
			//				tranIdInvpack = genericUtility.getColumnValueFromNode("inv_pack_id", currDetail);
			//				tranIdInvpackStr = tranIdInvpackStr +"'"+tranIdInvpack+"',";
			//            }

			for(int ctr = 0; ctr < noOfDetails; ctr++)
			{
				Node currDetail = detailList.item(ctr);
				tranIdInvpack = genericUtility.getColumnValueFromNode("inv_pack_id", currDetail);
				System.out.println("tranIdInvpack In packingTransform :: " + tranIdInvpack + ":");

				//				sql = " SELECT A.QUANTITY - A.ALLOC_QTY, A.ITEM_CODE, A.LOT_NO, A.LOT_SL, A.LOC_CODE "
				//					+ " FROM STOCK A WHERE (A.ITEM_CODE,A.SITE_CODE,A.LOC_CODE,A.LOT_NO,A.LOT_SL) IN "
				//					+ " (SELECT DISTINCT C.ITEM_CODE,C.SITE_CODE,C.LOC_CODE,C.LOT_NO,C.LOT_SL "
				//					+ " FROM INVTRACE C WHERE C.REF_SER = 'I-PKR' AND "
				//					+ " C.REF_ID IN ("+ tranIdInvpackStr +") "
				//					+ " AND A.QUANTITY - A.ALLOC_QTY > 0 AND (A.PACK_REF IS NULL  OR LENGTH(TRIM(A.PACK_REF)) = 0 ) ";
				sql = " SELECT B.ITEM_CODE, B.QUANTITY, B.LOC_CODE, B.LOT_NO, B.LOT_SL "
					+ " FROM INV_PACK A, INV_PACK_RCP B, ITEM C "
					+ " WHERE A.TRAN_ID = B.TRAN_ID AND B.ITEM_CODE = C.ITEM_CODE AND A.CONFIRMED = 'Y' "
					+ " AND A.ORDER_TYPE = 'N' AND A.TRAN_ID = ? AND A.SITE_CODE = ?";
				System.out.println("sql :"+sql);
				pstmtData = conn.prepareStatement(sql);
				pstmtData.setString(1, tranIdInvpack);
				pstmtData.setString(2, siteCode);
				rsData = pstmtData.executeQuery();
				while (rsData.next())
				{
					itemCode = rsData.getString("ITEM_CODE");
					invPackQty = rsData.getDouble("QUANTITY");
					locCode = rsData.getString("LOC_CODE");
					lotNo = rsData.getString("LOT_NO");
					lotSl = rsData.getString("LOT_SL");

					sql = " SELECT QUANTITY, ALLOC_QTY FROM STOCK WHERE SITE_CODE = ? AND ITEM_CODE = ? AND  LOC_CODE = ?"
						+ " AND LOT_NO = ? AND LOT_SL = ? AND PACK_REF IS NOT NULL";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteCode);
					pstmt.setString(2, itemCode);
					pstmt.setString(3, locCode);
					pstmt.setString(4, lotNo);
					pstmt.setString(5, lotSl);
					System.out.println("sql in retrieving Stock Details :: "+sql);
					System.out.println("Setting Params :: siteCode ==> :" + siteCode + ": itemCode ==> :" + itemCode + ":");
					System.out.println("Setting Params :: locCode ==> :" + locCode + ": lotNo ==> :" + lotNo + ":");
					System.out.println("Setting Params :: lotSl ==> :" + lotSl + ":");
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						availQty = rs.getDouble(1);
						stkAllocQty = rs.getDouble(2);
					}
					rs.close(); rs = null;
					pstmt.close(); pstmt = null;
					System.out.println("STOCK ========== availQty :" + availQty + ":: stkAllocQty :" + stkAllocQty + "::invPackQty ==> :" + invPackQty + ":");
					if ((availQty-stkAllocQty) < invPackQty)
					{
						errString = itmDBAccess.getErrorString("", "VTSTKINS", "", "", conn);
						newMessage = "STOCK FOR ITEM CODE ::: " + itemCode + " NOT SUFFICIENT IN SITE ::: " + siteCode;
						errString = updateMessage(errString, newMessage);
						return errString;
					}

					sql = " SELECT A.SALE_ORDER, A.LINE_NO, A.QTY_ORD-A.QTY_DESP-A.QTY_ALLOC, A.EXP_LEV, "
						+ " A.QTY_ALLOC FROM SORDITEM A, SORDER B "
						+ " WHERE A.SALE_ORDER = B.SALE_ORDER AND B.STATUS = 'P' "
						+ " AND A.QTY_ORD-A.QTY_DESP-A.QTY_ALLOC > 0 AND A.ITEM_CODE__ORD = ? "
						+ " AND B.CUST_CODE = ? AND A.SITE_CODE = ? "
						+ " ORDER BY B.DUE_DATE, A.SALE_ORDER, A.LINE_NO ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCode);
					pstmt.setString(2, custCode);
					pstmt.setString(3, siteCode);
					System.out.println("sql in retrieving Sorder Details :: "+sql);
					System.out.println("Setting Params :: itemCode ==> :" + itemCode + ": custCode ==> :" + custCode + ":");
					System.out.println("Setting Params :: siteCode ==> :" + siteCode + ":");
					rs = pstmt.executeQuery();
					while (rs.next() && invPackQty > 0)
					{
						saleOrder = rs.getString(1);
						lineNoSord = rs.getString(2);
						pendQty = rs.getDouble(3);
						expLevel = rs.getString(4);
						qtyAlloc = rs.getDouble(5);
						System.out.println("invPackQty ==> :" + invPackQty + ": qtyAlloc ==> :" + qtyAlloc + ": pendQty ==> :" + pendQty + ":");
						System.out.println(" saleOrder ==> :" + saleOrder + ": lineNoSord ==> :" + lineNoSord + ":");

						if (invPackQty > pendQty)
						{
							despQty = pendQty;
							invPackQty = invPackQty - despQty;
						}
						else if (invPackQty <= pendQty)
						{
							despQty = invPackQty;
							invPackQty = 0;
						}
						valueXmlString.append("<Detail>");
						valueXmlString.append("<sord_no isSrvCallOnChg='0'>").append("<![CDATA[").append(saleOrder).append("]]>").append("</sord_no>");

						valueXmlString.append("<line_no__sord isSrvCallOnChg='0'>").append("<![CDATA[").append(lineNoSord).append("]]>").append("</line_no__sord>");
						setNodeValue( dom, "line_no__sord", (lineNoSord == null) ? "":lineNoSord );
						tempStr = getChangeSord(dom,  dom1, "line_no__sord", xtraParams ,conn); // chg15
						valueXmlString.append(tempStr);
						valueXmlString.append("<exp_lev isSrvCallOnChg='0'>").append("<![CDATA[").append(expLevel).append("]]>").append("</exp_lev>");
						valueXmlString.append("<loc_code>").append("<![CDATA[").append(locCode).append("]]>").append("</loc_code>");
						valueXmlString.append("<lot_no>").append("<![CDATA[").append(lotNo).append("]]>").append("</lot_no>");

						valueXmlString.append("<lot_sl isSrvCallOnChg='0'>").append("<![CDATA[").append(lotSl).append("]]>").append("</lot_sl>");
						setNodeValue( dom, "lot_sl", (lotSl == null) ? "":lotSl );
						tempStr = getChangeSord(dom,  dom1, "lot_sl", xtraParams ,conn); // chg16
						valueXmlString.append(tempStr);

						valueXmlString.append("<quantity protect = \"1\" isSrvCallOnChg='0'>").append("<![CDATA[").append(despQty).append("]]>").append("</quantity>");
						setNodeValue( dom, "quantity", despQty);

						valueXmlString.append(getChangeSord(dom,  dom1, "quantity", xtraParams ,conn)); // chg17
						//valueXmlString.append("<quantity protect = \"1\" isSrvCallOnChg='1'>").append("<![CDATA[").append(despQty).append("]]>").append("</quantity>");
						valueXmlString.append("<tran_id__invpack isSrvCallOnChg='0'>").append("<![CDATA[").append(tranIdInvpack).append("]]>").append("</tran_id__invpack>");
						valueXmlString.append("</Detail>");

						sql = " UPDATE SORDITEM SET QTY_ALLOC = ? WHERE SALE_ORDER = ? AND LINE_NO = ? "
							+ " AND EXP_LEV = ? ";
						pstmtUpdate = conn.prepareStatement(sql);
						System.out.println("qtyAlloc + despQty :: " + (qtyAlloc + despQty) + " for saleorder ::" + saleOrder + ": lineNo ::" + lineNoSord + ":");
						pstmtUpdate.setDouble(1, qtyAlloc + despQty);
						pstmtUpdate.setString(2, saleOrder);
						pstmtUpdate.setString(3, lineNoSord);
						pstmtUpdate.setString(4, expLevel);
						int update = pstmtUpdate.executeUpdate();
						System.out.println("Records updated in Sorditem for qty alloc :: "+update);

						pstmtUpdate.close(); pstmtUpdate = null;

						sql = " UPDATE STOCK SET ALLOC_QTY = ? WHERE SITE_CODE = ? AND ITEM_CODE = ? AND  LOC_CODE = ?"
							+ " AND LOT_NO = ? AND LOT_SL = ? ";
						pstmtUpdate = conn.prepareStatement(sql);
						pstmtUpdate.setDouble(1, stkAllocQty + despQty);
						pstmtUpdate.setString(2, siteCode);
						pstmtUpdate.setString(3, itemCode);
						pstmtUpdate.setString(4, locCode);
						pstmtUpdate.setString(5, lotNo);
						pstmtUpdate.setString(6, lotSl);
						update = pstmtUpdate.executeUpdate();
						pstmtUpdate.close(); pstmtUpdate = null;
						System.out.println("Records updated in Stock for alloc_qty :: "+update);
					}
					rs.close(); rs=null;
					pstmt.close(); pstmt=null;
				}//END WHILE
				rsData.close(); rsData = null;
				pstmtData.close(); pstmtData = null;

			}
			valueXmlString.append("</Root>");
			String retXmlString = serializeDom(genericUtility.parseString(valueXmlString.toString()));
			valueXmlString = null;
			System.out.println("manohar 07/02/11 retXmlString 1 ["+ retXmlString +"]");
			valueXmlString =  new StringBuffer(retXmlString);


		}
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
				if(rs!=null)
				{
					rs.close(); rs = null;
				}
				if(pstmt!=null)
				{	
					pstmt.close(); pstmt = null;
				}
				if (rsData != null)
				{
					rsData.close(); rsData = null;
				}
				if (pstmtData != null)
				{
					pstmtData.close(); pstmtData = null;
				}
				if (pstmtUpdate != null)
				{
					pstmtUpdate.close(); pstmtUpdate = null;
				}
				if (conn != null)
				{
					conn.rollback();
					conn.close(); conn = null;
				}
			}
			catch(Exception e)
			{
				errString = e.getMessage();
				return errString;
			}
		}
		System.out.println("valueXmlString from CaseNoTransform :::" + valueXmlString.toString());
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
	//Added By Gulzar - 23/01/08
	private double[] getNoArt(String siteCode , String custCode , String itemCode , String packCode , double qty , String qtyType , double shipperQty , double integralQty, Connection conn)
	{
		String tempSql = "";
		String tempSql1 = "";
		String tempSql2 = "";
		ResultSet rsTemp = null;
		ResultSet rsTemp1 = null;
		ResultSet rsTemp2 = null;
		//Changed and added by Pavan R 10jan19[to handle open cursor issue]
		//Statement stmt = null;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		PreparedStatement pstmt2 = null;
		double capacity = 0d;
		double reoQty = 0d;
		double shipperQty1 = 0d;
		double remainder = 0d;
		double integralQty1 = 0d;
		double result[] = new double[3];
		double noArt1 = 0d, noArt2 = 0d, noArt = 0d;
		try
		{
			//stmt = conn.createStatement();
			if (qtyType.equals("S"))
			{
				tempSql = "SELECT (CASE WHEN CAPACITY IS NULL THEN 0 ELSE CAPACITY END) AS CAPACITY FROM PACKING WHERE PACK_CODE = ?";  //'" + packCode + "'";
				//System.out.println("SQL :: "+tempSql);
				//rsTemp = stmt.executeQuery(tempSql);
				pstmt = conn.prepareStatement(tempSql);
				pstmt.setString(1, packCode);
				rsTemp = pstmt.executeQuery();
				if (rsTemp.next())
				{
					capacity = rsTemp.getDouble("CAPACITY");
				}
				else
				{
					capacity = 0d;
				}
				tempSql = "";
				rsTemp.close();rsTemp = null;
				pstmt.close(); pstmt = null;
				tempSql = "SELECT  REO_QTY " + 
				" FROM SITEITEM WHERE SITE_CODE = ?"  //'" + siteCode + "' " +  //by jaimin 
				+ " AND ITEM_CODE = ? ";                //'" + itemCode + "' ";
				//System.out.println("SQL :: "+tempSql);
				//rsTemp = stmt.executeQuery(tempSql);
				pstmt = conn.prepareStatement(tempSql);
				pstmt.setString(1, siteCode);
				pstmt.setString(2, itemCode);
				rsTemp = pstmt.executeQuery();
				if (rsTemp.next())
				{
					reoQty = rsTemp.getDouble("REO_QTY");
				}
				else
				{
					tempSql1 = "SELECT REO_QTY FROM ITEM  WHERE ITEM_CODE = ? ";    //'"+ itemCode +"' ";
					//System.out.println("SQL :: "+tempSql1);
					//rsTemp1 = stmt.executeQuery(tempSql1);
					pstmt1 = conn.prepareStatement(tempSql1);
					pstmt1.setString(1, itemCode);
					rsTemp1 = pstmt1.executeQuery();
					if (rsTemp1.next())
					{
						reoQty = rsTemp1.getDouble("REO_QTY");
					}
					rsTemp1.close();rsTemp1= null;
					tempSql1 = "";
					pstmt1.close(); pstmt1 = null;
				}
				tempSql = "";
				rsTemp.close();rsTemp = null;
				pstmt.close(); pstmt = null;
				if ( capacity > 0 )
				{
					shipperQty1 = capacity;
				}
				else
				{
					shipperQty1 = reoQty;
				}
				if ( shipperQty1 > 0 )
				{
					noArt = (qty - (qty % shipperQty1)) / shipperQty1;
				}
				else
				{
					noArt =1;
				}
			}		
			else if ( qtyType.equals("I") )
			{//Changed and added by Pavan R 10jan19[to handle open cursor issue] Start
				tempSql = "SELECT INTEGRAL_QTY FROM CUSTOMERITEM WHERE CUST_CODE = ?"  //'"+ custCode +"' "
				+" AND ITEM_CODE = ?";   //'"+ itemCode +"' ";
				//System.out.println("SQL :: "+tempSql);
				//rsTemp = stmt.executeQuery(tempSql);
				pstmt = conn.prepareStatement(tempSql);
				pstmt.setString(1, custCode);
				pstmt.setString(2, itemCode);
				rsTemp = pstmt.executeQuery();
				if (rsTemp.next())
				{
					integralQty1 = rsTemp.getDouble("INTEGRAL_QTY");
				}
				else
				{
					tempSql1 = "SELECT INTEGRAL_QTY FROM SITEITEM WHERE SITE_CODE = ?"     //'"+ siteCode +"' " +
						+ " AND  ITEM_CODE = ?";   // '"+ itemCode +"' ";
					//System.out.println("SQL :: "+tempSql1);
					//rsTemp1 = stmt.executeQuery(tempSql1);
					pstmt1 = conn.prepareStatement(tempSql1);
					pstmt1.setString(1, siteCode);
					pstmt1.setString(2, itemCode);
					rsTemp1 = pstmt1.executeQuery();
					if (rsTemp1.next())
					{
						integralQty1 = rsTemp1.getDouble("INTEGRAL_QTY");
					}
					else
					{
						tempSql2 = "SELECT INTEGRAL_QTY FROM ITEM WHERE ITEM_CODE = ?";   //'"+ itemCode +"' ";
						//System.out.println("SQL :: "+tempSql2);
						//rsTemp2 = stmt.executeQuery(tempSql2);
						pstmt2 = conn.prepareStatement(tempSql2);
						pstmt2.setString(1, itemCode);
						rsTemp2 = pstmt2.executeQuery();
						if ( rsTemp2.next() )
						{
							integralQty1 = rsTemp2.getDouble("INTEGRAL_QTY");
						}
						rsTemp2.close();rsTemp2 = null;						
						tempSql2 = "";
						pstmt2.close();pstmt2 = null; 
					}
					rsTemp1.close();rsTemp1 = null;
					tempSql1 = "";
					pstmt1.close();pstmt1 = null;
				}
				tempSql = "";
				rsTemp.close();rsTemp = null;
				pstmt.close();pstmt = null;
				//Changed and added by Pavan R 10jan19[to handle open cursor issue] End
				if ( integralQty1 > 0 )
				{
					noArt = (qty - (qty % integralQty1)) / integralQty1;
				}
				else
				{
					noArt = 1;
				}
			}
			else if ( qtyType.equals("B") )
			{	//Changed and added by Pavan R 10jan19[to handle open cursor issue] Start
				tempSql = "SELECT (CASE WHEN CAPACITY IS NULL THEN 0 ELSE CAPACITY END) AS CAPACITY FROM PACKING WHERE PACK_CODE = ?";  //'"+ packCode +"'";
				//System.out.println("SQL :: "+tempSql);
				//rsTemp = stmt.executeQuery(tempSql);
				pstmt = conn.prepareStatement(tempSql);
				pstmt.setString(1, packCode);
				rsTemp = pstmt.executeQuery();//Changed and added by Pavan R 10jan19[to handle open cursor issue] End
				if (rsTemp.next())
				{
					capacity = rsTemp.getDouble("CAPACITY");
				}
				else
				{
					capacity = 0d;
				}
				tempSql = "";//Changed and added by Pavan R 10jan19[to handle open cursor issue] Start
				rsTemp.close();rsTemp = null;
				pstmt.close();pstmt = null;
				tempSql = "SELECT REO_QTY "
					+" FROM SITEITEM "
					+" WHERE SITE_CODE = ?"   //'"+ siteCode +"' " //jaimin
					+" AND ITEM_CODE = ?";    // '"+ itemCode +"' ";
				//System.out.println("SQL :: "+tempSql);
				//rsTemp = stmt.executeQuery(tempSql);
				pstmt = conn.prepareStatement(tempSql);
				//Changed and added by Pavan R 10jan19[to handle open cursor issue] End
				pstmt.setString(1, siteCode);
				pstmt.setString(2, itemCode);
				rsTemp = pstmt.executeQuery();				
				if (rsTemp.next())
				{
					reoQty = rsTemp.getDouble("REO_QTY");
				}
				else
				{
					tempSql1 = "SELECT REO_QTY FROM ITEM WHERE ITEM_CODE = ?"; 	//'"+ itemCode +"' ";
					//System.out.println("SQL :: "+tempSql1);//Changed and added by Pavan R 10jan19[to handle open cursor issue]
					//rsTemp1 =stmt.executeQuery(tempSql1);
					pstmt1 = conn.prepareStatement(tempSql1);					
					pstmt1.setString(1, itemCode);
					rsTemp1 = pstmt1.executeQuery();	
					if (rsTemp1.next())
					{
						reoQty = rsTemp1.getDouble("REO_QTY");
					}
					rsTemp1.close();rsTemp1 = null;//Added by Pavan R 10jan19[to handle open cursor issue]
					tempSql1 = "";
					pstmt1.close();pstmt1 = null;
				}
				tempSql = "";
				rsTemp.close();rsTemp = null;//Added by Pavan R 10jan19[to handle open cursor issue]
				pstmt.close();pstmt = null;

				if ( capacity > 0 )
				{
					shipperQty1 = capacity;
				}
				else
				{
					shipperQty1 = reoQty;
				}
				if ( shipperQty1 > 0 )
				{
					noArt1 = (qty - (qty % shipperQty1)) / shipperQty1;
					remainder = ( qty % shipperQty1 );
				}

				tempSql = "SELECT INTEGRAL_QTY FROM CUSTOMERITEM WHERE CUST_CODE = ? "//'"+ custCode +"' "
				+" AND ITEM_CODE = ?";      //'"+ itemCode +"' ";//Changed and added by Pavan R 10jan19[to handle open cursor issue]
				//System.out.println("SQL :: "+tempSql);
				//rsTemp = stmt.executeQuery(tempSql);
				pstmt = conn.prepareStatement(tempSql);
				pstmt.setString(1, custCode);
				pstmt.setString(2, itemCode);
				if (rsTemp.next())
				{
					integralQty1 = rsTemp.getDouble("INTEGRAL_QTY");
				}
				else
				{
					tempSql1 =  "SELECT INTEGRAL_QTY "
						+"  FROM SITEITEM WHERE SITE_CODE = '"+ siteCode +"' "
						+" 	AND ITEM_CODE = '"+ itemCode +"' ";
					//System.out.println("SQL :: "+tempSql1);
					//rsTemp1 = stmt.executeQuery(tempSql1);//Changed and added by Pavan R 10jan19[to handle open cursor issue]
					pstmt = conn.prepareStatement(tempSql1);
					pstmt.setString(1, custCode);
					pstmt.setString(2, itemCode);
					rsTemp1 = pstmt.executeQuery();
					if ( rsTemp1.next() )
					{
						integralQty1 = rsTemp1.getDouble("INTEGRAL_QTY");
					}
					else
					{	
						tempSql2 = "SELECT INTEGRAL_QTY FROM ITEM WHERE ITEM_CODE = ?"; //'"+ itemCode +"' ";
						//System.out.println("SQL :: "+tempSql2);//Changed and added by Pavan R 10jan19[to handle open cursor issue]
						//rsTemp2 = stmt.executeQuery(tempSql1);
						pstmt1 = conn.prepareStatement(tempSql2);						
						pstmt1.setString(1, itemCode);
						rsTemp2 = pstmt1.executeQuery();
						if ( rsTemp2.next() )
						{
							integralQty1 = rsTemp2.getDouble("INTEGRAL_QTY");
						}
						tempSql2 = "";
						rsTemp2.close(); rsTemp2 = null;
						pstmt1.close();pstmt1 = null;//Added by Pavan R 10jan19[to handle open cursor issue]
					}
					tempSql1 = "";
					rsTemp1.close();rsTemp1 = null;//Added by Pavan R 10jan19[to handle open cursor issue]
					pstmt.close();pstmt = null;
				}
				if ( integralQty1 > 0 )
				{
					noArt2 = (remainder - (remainder % integralQty1)) / integralQty1;
				}
				if ( noArt2 > 0 )
				{
					noArt2 = 1;
				}
				noArt = noArt1 + noArt2;
			}
			else
			{
				noArt = 0;
			}			
		}
		catch (Exception e)
		{
			System.out.println("Exception in getNoArt :: "+e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			try
			{			
				if (rsTemp != null)
				{
					rsTemp.close();
					rsTemp = null;
				}
				if (rsTemp1 != null)
				{
					rsTemp1.close();
					rsTemp1 = null;
				}
				if (rsTemp2 != null)
				{
					rsTemp2.close();
					rsTemp2 = null;
				}
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if (pstmt1 != null)
				{
					pstmt1.close();
					pstmt1 = null;
				}
				if (pstmt2 != null)
				{
					pstmt2.close();
					pstmt2 = null;
				}
				/*if(conn != null)
				{
					conn.close();
					conn = null;
				}*/
			}
			catch(Exception e){}
		}
		result[0] = noArt;
		result[1] = shipperQty1;
		result[2] = integralQty1;
		return result;
	}
	// End addition - Gulzar - 23/01/08

	//add 12-07-10 getChangeSord method      Rambeer sharma
	private String getChangeSord(Document dom, Document dom1,String currentColumn, String xtraParams ,Connection conn)throws RemoteException, ITMException
	{
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		int ctr=0;
		String childNodeName = null;
		String columnValue = null;
		PreparedStatement pstmt = null, pstmt1 = null;
		ResultSet rs = null,rs1 = null;
		String mcode = "", mVal = "" ,   sql = "" ;
		String siteCodeDet = "",taxcl = "", taxch = "", taxen = "", mdiscount = "" ;
		String custItemCodeRef ="",itemCode =""; 
		String custCode="" ,custItemDesc = "";
		double rateStd = 0,rateClg = 0 ;
		double despatchedQty=0,orderQty = 0 ,pendingQty = 0 , minusQty = 0 ,balQty=0;
		String  mdescr1 = "", mdescr2 = "",  mdescr3 = "", mdescr4 = "", mstunit ="", mUnit = "";
		String mpack = "" ,mitemdesc = "" ,mloc = "" ,listType = "",applyPrice = "",priceVar = "", itemRef = "";
		String smNum = "",smNum1 ="" ,smNum2 = "" ,sNoArt="" ,sGrossWeight="" ,despId ="", sNoArticle ="" ;
		double mNum = 0, mNum1 = 0, mNum2 = 0 ,mnopack = 0 ,ordQty = 0, noArticle =0,grossWeight=0;
		double packQty = 0, itemRate =0,totRate =0 ,diffRate =0, discAmt = 0 ,effCost = 0;
		String mVal1 = "", saleOrder = "", lineNo = "",locCode = "" ,lotNo = "" ,lotSl ="" ,siteCode="";
		double noArt = 0, noArt1 = 0, noArt2 = 0 ,grossWt = 0,nettWt =0 ;
		double shipperQty = 0,acShipperQty =0,intQty = 0,acIntQty = 0,looseQty = 0 ,discPerc = 0;
		String str = "" ,itemCodeOrd ="",saleord ="",saleordLine="" ,lineNoSord="" ,sItemRate = "";
		double nettWeight = 0, sordExcRate = 0,sordQuantity =0; 
		double tareWeight = 0,qtyPerArt = 0,stcrate = 0, grossWeightArt = 0,tareWeightArt = 0, palletWt = 0;
		String packInstr = "",dimension = "",siteMfg ="";
		String trackShelfLife = "",stkOpt = "" ,explev ="" ,priceList ="" ,priceListClg ="" ,sDespDate="";
		String priceListParent ="",sConv="" ,nature="" ,packCode="" ,sDiscAmt="" ,sRateStd="";
		double pickRate = 0 ,conv =0 ,priceRateClg =0 ,pickRateClg =0 ,mNum3 =0 ,rate=0,sordRate=0;
		double rateStduom = 0 ,qty1=0 ,rateStduom1 = 0;
		String quantityStudom = "" ;
		double qtyStudom = 0;
		DecimalFormat df = new DecimalFormat("#########.###");
		Timestamp despDate =null ,expDate = null ,mfgDate=null,retestDate=null,plistdate=null,orderdate=null;
		int count = 0;
		String sExpDate ="" ,sMfgDate="" ,sRetestDate="";

		ArrayList arrList = new ArrayList();

		StringBuffer valueXmlString = new StringBuffer();


		DistCommon distCommon = new DistCommon();
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();

		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();

		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			if(currentColumn !=null || currentColumn.trim().length() > 0)
			{
				if(currentColumn.equalsIgnoreCase("line_no__sord"))
				{
					mcode = genericUtility.getColumnValue("line_no__sord",dom);
					valueXmlString.append("<line_no__sord isSrvCallOnChg='0' >").append("<![CDATA[").append(mcode == null ? " ": mcode).append("]]>").append("</line_no__sord>\r\n");				
					setNodeValue( dom, "line_no__sord", (mcode == null) ? " ": mcode );
					mVal = genericUtility.getColumnValue("sord_no",dom);
					System.out.println("line_no__sord CASE....:"+mcode +"..."+mVal);
					sql = "select site_code,tax_class, tax_chap, tax_env, discount, rate__stduom,rate__clg ,"
						+"cust_item__ref,item_code from sorddet where sale_order = ? and line_no = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,mVal);
					pstmt.setString(2,mcode);

					rs = pstmt.executeQuery();
					if(rs.next())
					{
						siteCodeDet = rs.getString("site_code");
						taxcl = rs.getString("tax_class");  
						taxch = rs.getString("tax_chap");
						taxen = rs.getString("tax_env");
						mdiscount = checkDoubleNull(rs.getString("discount"));
						rateStd = rs.getDouble("rate__stduom");
						rateClg = rs.getDouble("rate__clg");
						custItemCodeRef = rs.getString("cust_item__ref");
						itemCode = rs.getString("item_code");
					}

					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					//rateStd = df.parse(df.format(rateStd)).doubleValue(); 
					if(custItemCodeRef == null || custItemCodeRef.trim().length() ==0)  ///&&
					{
						custCode = genericUtility.getColumnValue("cust_code",dom1);
						sql = "select item_code__ref,descr from customeritem where cust_code = ? and  item_code  = ? ";

						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,custCode);
						pstmt.setString(2,itemCode);

						rs = pstmt.executeQuery();
						while (rs.next())
						{
							custItemCodeRef = rs.getString("item_code__ref");
							custItemDesc = rs.getString("descr");
						}

						valueXmlString.append("<cust_item__ref isSrvCallOnChg='0'>").append("<![CDATA[").append(custItemCodeRef == null?"":custItemCodeRef).append("]]>").append("</cust_item__ref>\r\n");
						setNodeValue( dom, "cust_item__ref", (custItemCodeRef == null) ? "" : custItemCodeRef );
						valueXmlString.append("<custitem_desc isSrvCallOnChg='0'>").append("<![CDATA[").append(custItemDesc == null?"":custItemDesc).append("]]>").append("</custitem_desc>\r\n");				
						setNodeValue( dom, "custitem_desc", (custItemDesc == null) ? "" : custItemDesc );

						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					else
					{
						custCode = genericUtility.getColumnValue("cust_code",dom1);
						sql = "select descr from customeritem where cust_code = ? and  item_code  = ? and  item_code__ref  = ?";

						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,custCode);
						pstmt.setString(2,itemCode);
						pstmt.setString(3,custItemCodeRef);

						rs = pstmt.executeQuery();
						if(rs.next())
						{
							custItemDesc = rs.getString("descr");
						}

						valueXmlString.append("<cust_item__ref isSrvCallOnChg='0'>").append("<![CDATA[").append(custItemCodeRef == null?"":custItemCodeRef).append("]]>").append("</cust_item__ref>\r\n");
						setNodeValue( dom, "cust_item__ref", (custItemCodeRef == null) ? "" : custItemCodeRef );
						valueXmlString.append("<custitem_desc isSrvCallOnChg='0'>").append("<![CDATA[").append(custItemDesc == null?"":custItemDesc).append("]]>").append("</custitem_desc>\r\n");				
						setNodeValue( dom, "custitem_desc", (custItemDesc == null) ? "" : custItemDesc );

						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}

					sql = "select sum(case when qty_desp is null then 0 else qty_desp end), "+
					"sum(case when quantity is null then 0 else quantity end) from sorditem " +
					"where	sale_order = ? and	line_no = ? and line_type != 'B'";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,mVal);
					pstmt.setString(2,mcode);

					rs = pstmt.executeQuery();
					if(rs.next())
					{
						despatchedQty = rs.getDouble(1);
						orderQty = rs.getDouble(2);
					}

					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					pendingQty = orderQty - despatchedQty;

					despId = genericUtility.getColumnValue("desp_id",dom1);
					minusQty = 0;

					sql = "select sum(quantity) from despatchdet where sord_no = ? and  desp_id  = ? and  line_no__sord = ?";

					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,mVal);
					pstmt.setString(2,despId);
					pstmt.setString(3,mcode);

					rs = pstmt.executeQuery();
					if(rs.next())
					{
						minusQty = rs.getDouble(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					balQty = pendingQty - minusQty;

					valueXmlString.append("<pending_qty isSrvCallOnChg='0'>").append("<![CDATA[").append(balQty).append("]]>").append("</pending_qty>\r\n");
					setNodeValue( dom, "pending_qty", balQty );
					sql = "Select item_code__ord, quantity, exp_lev, item_code, qty_alloc "
						+" from sorditem where sale_order = ? and line_no = ? and line_type = 'I'";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,mVal);
					pstmt.setString(2,mcode);

					rs = pstmt.executeQuery();
					if(rs.next())
					{
						itemCode = rs.getString("item_code__ord");
						mNum  = rs.getDouble("quantity");
						mdescr1 = rs.getString("exp_lev");
						itemCode = rs.getString("item_code");
						mNum1  = rs.getDouble("qty_alloc");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					sql = "Select unit__std, conv__qty_stduom, unit, pack_instr, (case when no_art is null then 0 else no_art end) "
						+" from sorddet where sale_order = ? and line_no = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,mVal);
					pstmt.setString(2,mcode);

					rs = pstmt.executeQuery();
					if(rs.next())
					{
						mstunit = rs.getString("unit__std");
						mNum2  = rs.getDouble("conv__qty_stduom");
						mUnit = rs.getString("unit");
						mpack = rs.getString("pack_instr");
						mnopack = rs.getDouble(5);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					sql = "Select descr from item where item_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,itemCode);

					rs = pstmt.executeQuery();
					if(rs.next())
					{
						mitemdesc = rs.getString("descr");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					ordQty = mNum;
					if(balQty != mNum)
					{
						mNum = balQty;
					}
					else
					{
						mNum = mNum;
					}
					if(mdescr3 == null || mdescr3.trim().length() ==0)
					{
						mdescr3 = "    ";
					}
					if(mdescr4 == null || mdescr4.trim().length() ==0)
					{
						mdescr4 = "    ";
					}

					valueXmlString.append("<item_code__ord isSrvCallOnChg='0'>").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code__ord>\r\n");
					setNodeValue( dom, "item_code__ord", (itemCode == null) ? "" : itemCode );
					valueXmlString.append("<quantity__ord isSrvCallOnChg='0'>").append("<![CDATA[").append(ordQty).append("]]>").append("</quantity__ord>\r\n");
					setNodeValue( dom, "quantity__ord", ordQty);
					valueXmlString.append("<site_code isSrvCallOnChg='0'>").append("<![CDATA[").append(siteCodeDet).append("]]>").append("</site_code>\r\n");
					setNodeValue( dom, "site_code", (siteCodeDet == null) ? "" : siteCodeDet );
					// commented by cpatil on 31/JAN/13 as per suggestion start
					//valueXmlString.append("<exp_lev isSrvCallOnChg='0'>").append("<![CDATA[").append(mdescr1).append("]]>").append("</exp_lev>\r\n");     
					//setNodeValue( dom, "exp_lev", mdescr1);
					// commented by cpatil on 31/JAN/13 as per suggestion end
					valueXmlString.append("<item_code isSrvCallOnChg='0'>").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code>\r\n");
					setNodeValue( dom, "item_code", (itemCode == null) ? "" : itemCode );
					valueXmlString.append("<quantity isSrvCallOnChg='0'>").append("<![CDATA[").append(mNum).append("]]>").append("</quantity>\r\n"); 
					setNodeValue( dom, "quantity", mNum);
					valueXmlString.append("<quantity_real isSrvCallOnChg='0'>").append("<![CDATA[").append(mNum).append("]]>").append("</quantity_real>\r\n");
					setNodeValue( dom, "quantity_real", mNum );
					mdescr3 = genericUtility.getColumnValue("lot_no",dom);
					valueXmlString.append("<lot_no isSrvCallOnChg='0'>").append("<![CDATA[").append(mdescr3).append("]]>").append("</lot_no>\r\n");
					setNodeValue( dom, "lot_no", (mdescr3 == null) ? "" : mdescr3 );
					mloc = genericUtility.getColumnValue("lot_sl",dom); 


					if(mdescr4 == null || mdescr4.trim().length() ==0)
					{
						//mdescr4 = "    ";///extra  
						mdescr4 = mloc;
						valueXmlString.append("<lot_sl isSrvCallOnChg='0'>").append("<![CDATA[").append(mdescr4).append("]]>").append("</lot_sl>\r\n");
						setNodeValue( dom, "lot_sl", (mdescr4 == null) ? "" : mdescr4 );
					}
					// start 06-08-2010
					else
					{
						valueXmlString.append("<lot_sl isSrvCallOnChg='0'>").append("<![CDATA[").append(mdescr4).append("]]>").append("</lot_sl>\r\n");
						setNodeValue( dom, "lot_sl", (mdescr4 == null) ? "" : mdescr4 );
					}
					//end 06-08-2010
					valueXmlString.append("<unit__std isSrvCallOnChg='0'>").append("<![CDATA[").append(mstunit).append("]]>").append("</unit__std>\r\n");
					setNodeValue( dom, "unit__std", (mstunit == null) ? "" : mstunit );
					valueXmlString.append("<conv__qty_stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(mNum2).append("]]>").append("</conv__qty_stduom>\r\n");
					setNodeValue( dom, "conv__qty_stduom", mNum2 );
					valueXmlString.append("<conv__rtuom_stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(mNum2).append("]]>").append("</conv__rtuom_stduom>\r\n");///13-08
					setNodeValue( dom, "conv__rtuom_stduom", (mNum2) );
					valueXmlString.append("<item_descr isSrvCallOnChg='0'>").append("<![CDATA[").append(mitemdesc).append("]]>").append("</item_descr>\r\n");
					setNodeValue( dom, "item_descr", (mitemdesc == null) ? " " : mitemdesc );
					valueXmlString.append("<unit isSrvCallOnChg='0'>").append("<![CDATA[").append(mUnit).append("]]>").append("</unit>\r\n");
					setNodeValue( dom, "unit", (mUnit == null) ? "" : mUnit );
					valueXmlString.append("<rate__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(rateStd).append("]]>").append("</rate__stduom>\r\n");
					setNodeValue( dom, "rate__stduom", rateStd );
					valueXmlString.append("<rate__clg isSrvCallOnChg='0'>").append("<![CDATA[").append(rateClg).append("]]>").append("</rate__clg>\r\n");
					setNodeValue( dom, "rate__clg", rateClg );
					valueXmlString.append("<discount isSrvCallOnChg='0'>").append("<![CDATA[").append(mdiscount).append("]]>").append("</discount>\r\n");
					setNodeValue( dom, "discount", (mdiscount == null) ? "" : mdiscount );
					arrList = distCommon.getConvQuantityFact(mUnit, mstunit, itemCode, mNum, mNum2 , conn); // arraylist

					mNum3 = Double.parseDouble(arrList.get(1).toString());
					//// chandni 7-02 

					valueXmlString.append("<quantity__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(mNum3).append("]]>").append("</quantity__stduom>\r\n");
					setNodeValue( dom, "quantity__stduom", mNum3);
					////

					//valueXmlString.append("<rate__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(mNum3).append("]]>").append("</rate__stduom>\r\n");
					//setNodeValue( dom, "rate__stduom", mNum3); /// 16-08
					sNoArticle = genericUtility.getColumnValue("no_art",dom);
					/*if (sNoArticle == null || "null".equals(sNoArticle))
					{
						sNoArticle = "1";
					}*/

					if(sNoArticle != null && sNoArticle.trim().length() > 0)
					{
						noArticle = Double.parseDouble(sNoArticle);
						// 20-01-11 - chandni shah
						if (noArticle == 0)
						{
							noArticle = 1;
						}
						if(noArticle > 0)
						{
							packQty = mNum3 / noArticle;
							//19-1-11 - Chandni Shah
							packQty = df.parse(df.format(packQty)).doubleValue();
							System.out.println("28/08/10 packQty 3 [" + packQty + "]");
							valueXmlString.append("<pack_qty isSrvCallOnChg='0'>").append("<![CDATA[").append(packQty).append("]]>").append("</pack_qty>\r\n");
							setNodeValue( dom, "pack_qty", packQty );
						}
					}

					sql = "select distinct list_type from pricelist "
						+"where price_list = (select price_list from sorder where sale_order = ?)";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,mVal);

					rs = pstmt.executeQuery();
					while(rs.next())
					{
						listType = rs.getString("list_type");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					sql = "select item_ref from sorditem where sale_order = ? "
						+"and line_no = ? and exp_lev = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,mVal);
					pstmt.setString(2,mcode);
					pstmt.setString(3,mdescr1);

					rs = pstmt.executeQuery();
					while(rs.next())
					{
						itemRef = rs.getString("item_ref");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					sql = "select apply_price,price_var from bom where bom_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,itemCode);

					rs = pstmt.executeQuery();
					while(rs.next())
					{	
						applyPrice = rs.getString("apply_price");
						priceVar = rs.getString("price_var");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if("L".equalsIgnoreCase(listType) && "P".equalsIgnoreCase(applyPrice))
					{
						sql = "select rate from pricelist where "
							+"price_list = (select price_list from sorder where sale_order = ? and item_code = ?)";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,mVal);
						pstmt.setString(2,itemCode);

						rs = pstmt.executeQuery();
						while(rs.next())
						{	
							itemRate =rs.getDouble("rate");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						sql = "select sum(rate) from pricelist where price_list = ( select price_list from sorder "
							+"where sale_order = ? and item_code in (select item_code from sorditem where "
							+"sale_order = ? and line_no= ? and line_type= 'I'))";

						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,mVal);
						pstmt.setString(2,mVal);
						pstmt.setString(3,mcode);

						rs = pstmt.executeQuery();
						while(rs.next())
						{	
							totRate = rs.getDouble(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						sql = "select rate__stduom from sorddet where sale_order = ?"
							+" and line_no = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,mVal);
						pstmt.setString(2,mcode);

						rs = pstmt.executeQuery();
						while(rs.next())
						{	
							rateStd = rs.getDouble("rate__stduom");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						diffRate = totRate - rateStd;

						if("A".equalsIgnoreCase(priceVar))
						{
							rateStd = itemRate - (diffRate * ( itemRate / totRate) );
							//rateStd = df.parse(df.format(rateStd)).doubleValue(); 
							discAmt = diffRate * ( itemRate / totRate);
							System.out.println("diffRate from getChangeSord if priceVar is A 4 ::::::::" + diffRate);
							System.out.println("itemRate from getChangeSord if priceVar is A 4 ::::::::" + itemRate);
							System.out.println("totRate from getChangeSord if priceVar is A 4 ::::::::" + totRate);
							System.out.println("discAmt from getChangeSord if priceVar is A 4 ::::::::" + discAmt);
							valueXmlString.append("<rate__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(rateStd).append("]]>").append("</rate__stduom>\r\n");
							setNodeValue( dom, "rate__stduom", rateStd );
							valueXmlString.append("<rate__clg isSrvCallOnChg='0'>").append("<![CDATA[").append(rateStd).append("]]>").append("</rate__clg>\r\n");
							setNodeValue( dom, "rate__clg", rateStd );
							valueXmlString.append("<conf_diff_amt isSrvCallOnChg='0'>").append("<![CDATA[").append(discAmt).append("]]>").append("</conf_diff_amt>\r\n");
							setNodeValue( dom, "conf_diff_amt", discAmt );
						}
						else if("D".equalsIgnoreCase(priceVar))
						{
							discAmt = diffRate * ( itemRate / totRate);
							System.out.println("diffRate from getChangeSord if priceVar is D 5 ::::::::" + diffRate);
							System.out.println("itemRate from getChangeSord if priceVar is D 5 ::::::::" + itemRate);
							System.out.println("totRate from getChangeSord if priceVar is D 5 ::::::::" + totRate);
							System.out.println("discAmt from getChangeSord if priceVar is D 5 ::::::::" + discAmt);
							//itemRate = df.parse(df.format(itemRate)).doubleValue(); 
							valueXmlString.append("<disc_amt isSrvCallOnChg='0'>").append("<![CDATA[").append(discAmt).append("]]>").append("</disc_amt>\r\n");
							setNodeValue( dom, "discount", (mdiscount == null) ? "" : mdiscount );
							valueXmlString.append("<rate__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(itemRate).append("]]>").append("</rate__stduom>\r\n");
							setNodeValue( dom, "rate__stduom", itemRate );
							valueXmlString.append("<rate__clg isSrvCallOnChg='0'>").append("<![CDATA[").append(itemRate).append("]]>").append("</rate__clg>\r\n");
							setNodeValue( dom, "rate__clg", itemRate );
							valueXmlString.append("<conf_diff_amt isSrvCallOnChg='0'>").append("<![CDATA[").append(0).append("]]>").append("</conf_diff_amt>\r\n");
							setNodeValue( dom, "conf_diff_amt", 0 );
						}
					}
					else if("L".equalsIgnoreCase(listType) && "E".equalsIgnoreCase(applyPrice))
					{
						sql = "select (case when eff_cost is null then 0 else eff_cost end) from bomdet "
							+"where bom_code = ? and item_ref = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,itemCode);
						pstmt.setString(2,itemRef);

						rs = pstmt.executeQuery();
						while(rs.next())
						{	
							effCost =rs.getDouble(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						//effCost = df.parse(df.format(effCost)).doubleValue(); 
						valueXmlString.append("<rate__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(effCost).append("]]>").append("</rate__stduom>\r\n");
						setNodeValue( dom, "rate__stduom", effCost );
						valueXmlString.append("<rate__clg isSrvCallOnChg='0'>").append("<![CDATA[").append(effCost).append("]]>").append("</rate__clg>\r\n");
						setNodeValue( dom, "rate__clg", effCost );
					}
					valueXmlString.append("<tax_class isSrvCallOnChg='0'>").append("<![CDATA[").append((taxcl == null) ? "" : taxcl).append("]]>").append("</tax_class>\r\n");
					setNodeValue( dom, "tax_class", (taxcl == null) ? "" : taxcl );
					valueXmlString.append("<tax_chap isSrvCallOnChg='0'>").append("<![CDATA[").append((taxch == null) ? "" : taxch).append("]]>").append("</tax_chap>\r\n");
					setNodeValue( dom, "tax_chap", (taxch == null) ? "" : taxch );
					valueXmlString.append("<tax_env isSrvCallOnChg='0'>").append("<![CDATA[").append((taxen == null) ? "" : taxen).append("]]>").append("</tax_env>\r\n");
					setNodeValue( dom, "tax_env", (taxen == null) ? "" : taxen );
					mloc = genericUtility.getColumnValue("lot_code",dom); //miss6 no use of mloc
					valueXmlString.append("<pack_instr isSrvCallOnChg='0'>").append("<![CDATA[").append(blanknull(mpack)).append("]]>").append("</pack_instr>\r\n");
					setNodeValue( dom, "pack_instr", (mpack == null) ? "" : mpack );
					valueXmlString.append("<no_art isSrvCallOnChg='0'>").append("<![CDATA[").append(mnopack).append("]]>").append("</no_art>\r\n");
					// 23/09/14 not required 
					//setNodeValue( dom, "no_art", mnopack );
					//valueXmlString.append(getChangeSord(dom , dom1 , "quantity", xtraParams , conn));
					//gbf_itemchanged_logic(as_form_no,"Quantity",as_editflag)//miss31  function

					sql = "select rate__std from sorddet where sale_order =? and line_no = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,mVal);
					pstmt.setString(2,mcode);

					rs = pstmt.executeQuery();
					while(rs.next())
					{	
						rate =rs.getDouble("rate__std");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if( rate > 0)
					{
						valueXmlString.append("<rate__std isSrvCallOnChg='0'>").append("<![CDATA[").append(rate).append("]]>").append("</rate__std>\r\n");
						setNodeValue( dom, "rate__std", rate );
					}
					//  valueXmlString.append(getChangeSord(dom , dom1 , "exp_lev", xtraParams , conn));    // commented by cpatil on 04/02/13
					//gbf_itemchanged_logic(as_form_no,"exp_lev",as_editflag)//miss32  function
					explev = genericUtility.getColumnValue("exp_lev",dom);
					saleOrder = genericUtility.getColumnValue("sord_no",dom);
					lineNoSord = genericUtility.getColumnValue("line_no__sord",dom);

					sql = "select nature from sorditem where sale_order =? and line_no = ? "
						+" and exp_lev = ? and line_type != 'B'";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,saleOrder);
					pstmt.setString(2,lineNoSord);
					pstmt.setString(3,explev);

					rs = pstmt.executeQuery();
					while(rs.next())
					{	
						nature =rs.getString("nature");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					//System.out.println("nature 6635 ="+nature);
					if("F".equalsIgnoreCase(nature))
					{
						valueXmlString.append("<rate__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(0).append("]]>").append("</rate__stduom>\r\n");
						setNodeValue( dom, "rate__stduom", 0 );
						valueXmlString.append("<line_type isSrvCallOnChg='0'>").append("<![CDATA[").append("F").append("]]>").append("</line_type>\r\n");
						setNodeValue( dom, "line_type", "F" );
					}
					//added by kunal on 17/06/13 for sample and bonus item 
					if("B".equalsIgnoreCase(nature))
					{
						valueXmlString.append("<rate__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(0).append("]]>").append("</rate__stduom>\r\n");
						setNodeValue( dom, "rate__stduom", 0 );
						valueXmlString.append("<line_type isSrvCallOnChg='0'>").append("<![CDATA[").append("B").append("]]>").append("</line_type>\r\n");
						setNodeValue( dom, "line_type", "B" );
					}
					if("S".equalsIgnoreCase(nature))
					{
						valueXmlString.append("<rate__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(0).append("]]>").append("</rate__stduom>\r\n");
						setNodeValue( dom, "rate__stduom", 0 );
						valueXmlString.append("<line_type isSrvCallOnChg='0'>").append("<![CDATA[").append("S").append("]]>").append("</line_type>\r\n");
						setNodeValue( dom, "line_type", "S" );
					}


				}
				//new column value 
				else if(currentColumn.equalsIgnoreCase("exp_lev"))
				{
					String mitemflg="";
					String lstaxclass="" ,lstaxchap="",lstaxenv="",lstaxchapsoitem="";
					mVal1 = genericUtility.getColumnValue("exp_lev",dom);
					mcode = genericUtility.getColumnValue("line_no__sord",dom);///genericUtility.
					mVal = genericUtility.getColumnValue("sord_no",dom);
					System.out.println("exp_lev CASE....:"+mVal1+"..."+mcode+"..."+mVal);

					sql = "Select site_code, unit__std, conv__qty_stduom, unit,item_flg,tax_class,tax_chap,tax_env from sorddet "
						+"where sale_order = ? and line_no = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,mVal);
					pstmt.setString(2,mcode);

					rs = pstmt.executeQuery();
					while(rs.next())
					{	
						mdescr2 = rs.getString("site_code");
						mstunit = rs.getString("unit__std");
						mNum2 = rs.getDouble("conv__qty_stduom");
						mUnit = rs.getString("unit");
						mitemflg = rs.getString("item_flg");
						lstaxclass = rs.getString("tax_class");
						lstaxchap = rs.getString("tax_chap");
						lstaxenv = rs.getString("tax_env");
						
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					//Added by Abhijit
					sql = "Select item_code__ord, quantity, item_code, qty_alloc, nature, tax_chap from sorditem "
						+" where sale_order = ? and line_no = ? and "
						+" exp_lev = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,mVal);
					pstmt.setString(2,mcode);
					//pstmt.setString(3,mdescr2); /// site_code = ? and
					pstmt.setString(3,mVal1);

					rs = pstmt.executeQuery();
					if(rs.next())
					{	
						itemCode = rs.getString("item_code__ord");
						mNum = rs.getDouble("quantity");
						mdescr1 = rs.getString("item_code");
						mNum1 = rs.getDouble("qty_alloc");
						nature=rs.getString("nature");
						lstaxchapsoitem = rs.getString("tax_chap");
						
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					System.out.println(".:nature" +nature);
					System.out.println(".:item_code" +mdescr1);
					System.out.println(".:lstaxchapsoitem" +lstaxchapsoitem);
					System.out.println(".:lstaxchap" +lstaxchap);
					if("B".equalsIgnoreCase(mitemflg))
					{
						System.out.println("Match Item Flag");
						lstaxchap = lstaxchapsoitem;
					}
					sql = "Select descr from item where item_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,mdescr1);

					rs = pstmt.executeQuery();
					if(rs.next())
					{	
						mitemdesc = rs.getString("descr");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<quantity isSrvCallOnChg='0'>").append("<![CDATA[").append(0).append("]]>").append("</quantity>\r\n");
					setNodeValue( dom, "quantity", 0 );
					sql = "Select sum(quantity - qty_desp) as totSum from sorditem "
						+" where sale_order = ? and line_no = ? and exp_lev = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,mVal);
					pstmt.setString(2,mcode);
					pstmt.setString(3,mVal1);

					rs =pstmt.executeQuery();
					if(rs.next())
					{	
						mNum = rs.getDouble(1);
						System.out.println("mNum on 5900  "+mNum);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println("::item_flg" +mitemflg);
					System.out.println("::nature" +nature);
					if( "B".equals(mitemflg) &&  "F".equals(nature) )
					{
						sql="select unit, unit__pur from item where item_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,mdescr1);
						rs = pstmt.executeQuery();
						if(rs.next())
						{	
							mUnit = rs.getString("unit");
							mstunit = rs.getString("unit__pur");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("::mUnit" +mUnit);
						System.out.println("::mstunit" +mstunit);

					}
					valueXmlString.append("<item_code__ord isSrvCallOnChg='0'>").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code__ord>\r\n");
					setNodeValue( dom, "item_code__ord", (itemCode == null) ? "" : itemCode );
					valueXmlString.append("<quantity__ord isSrvCallOnChg='0'>").append("<![CDATA[").append(mNum).append("]]>").append("</quantity__ord>\r\n");
					setNodeValue( dom, "quantity__ord", mNum );
					valueXmlString.append("<item_code isSrvCallOnChg='0'>").append("<![CDATA[").append(mdescr1).append("]]>").append("</item_code>\r\n");
					setNodeValue( dom, "item_code", (mdescr1 == null) ? "" : mdescr1 );
					valueXmlString.append("<quantity isSrvCallOnChg='0'>").append("<![CDATA[").append(mNum).append("]]>").append("</quantity>\r\n"); 
					setNodeValue( dom, "quantity",mNum);
					valueXmlString.append("<quantity_real isSrvCallOnChg='0'>").append("<![CDATA[").append(mNum).append("]]>").append("</quantity_real>\r\n"); 
					setNodeValue( dom, "quantity_real", mNum );
					valueXmlString.append("<unit__std isSrvCallOnChg='0'>").append("<![CDATA[").append(mstunit).append("]]>").append("</unit__std>\r\n");
					setNodeValue( dom, "unit__std", (mstunit == null) ? "" : mstunit );
					valueXmlString.append("<conv__qty_stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(mNum2).append("]]>").append("</conv__qty_stduom>\r\n");
					setNodeValue( dom, "conv__qty_stduom", mNum2 );
					valueXmlString.append("<conv__rtuom_stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(mNum2).append("]]>").append("</conv__rtuom_stduom>\r\n");///13-08
					setNodeValue( dom, "conv__rtuom_stduom", (mNum2) );
					valueXmlString.append("<item_descr isSrvCallOnChg='0'>").append("<![CDATA[").append(mitemdesc).append("]]>").append("</item_descr>\r\n");
					setNodeValue( dom, "item_descr", (mitemdesc == null) ? "" : mitemdesc );
					valueXmlString.append("<unit isSrvCallOnChg='0'>").append("<![CDATA[").append(mUnit).append("]]>").append("</unit>\r\n");
					setNodeValue( dom, "unit", (mUnit == null) ? "" : mUnit );
					arrList = distCommon.getConvQuantityFact(mUnit, mstunit, mdescr1, mNum, mNum2 , conn);

					System.out.println("arrList   "+arrList);
					mNum3 = Double.parseDouble(arrList.get(1).toString());
					valueXmlString.append("<quantity__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(mNum3).append("]]>").append("</quantity__stduom>\r\n");
					setNodeValue( dom, "quantity__stduom", mNum3 );
					//valueXmlString.append("<rate__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(mNum3).append("]]>").append("</rate__stduom>\r\n");
					//setNodeValue( dom, "rate__stduom", mNum3); /// 16-08
					sNoArticle = genericUtility.getColumnValue("no_art",dom);
					/*if(sNoArticle == null || "null".equals(sNoArticle) || "0".equals(sNoArticle))
					{
						sNoArticle = "1";
					}*/

					if(sNoArticle != null && sNoArticle.trim().length() > 0)
					{
						noArticle = Double.parseDouble(sNoArticle);
						// 20-01-11 - Chandni Shah
						if (noArticle == 0)
						{
							noArticle = 1;
						}
						if(noArticle > 0)
						{
							packQty = mNum3 / noArticle;
							// 19-1-11 - chandni Shah
							packQty = df.parse(df.format(packQty)).doubleValue();
							System.out.println("28/08/10 packQty 4 [" + packQty + "]");
							valueXmlString.append("<pack_qty isSrvCallOnChg='0'>").append("<![CDATA[").append(packQty).append("]]>").append("</pack_qty>\r\n");
							setNodeValue( dom, "pack_qty",packQty );
						}	
					}					
					sql = "Select sum(case when qty_desp is null then 0 else qty_desp end), "
						+"sum(case when quantity is null then 0 else quantity end)  from sorditem "
						+" where sale_order = ? and line_no = ? and exp_lev = ? and line_type != 'B'";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,mVal);
					pstmt.setString(2,mcode);
					pstmt.setString(3,mVal1);

					rs = pstmt.executeQuery();
					while(rs.next())
					{	
						despatchedQty = rs.getDouble(1);
						orderQty = rs.getDouble(2);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					pendingQty = orderQty - despatchedQty;
					despId = genericUtility.getColumnValue("desp_id",dom1);
					minusQty = 0;
					sql = "Select sum(quantity) from despatchdet where sord_no = ? "
						+"and desp_id = ? and line_no__sord = ? and exp_lev = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,mVal);
					pstmt.setString(2,despId);
					pstmt.setString(3,mcode);
					pstmt.setString(4,mVal1);

					rs = pstmt.executeQuery();
					while(rs.next())
					{	
						minusQty = rs.getDouble(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					/*if(minusQty == null)
					{
						minusQty = 0;
					}
					if(pendingQty == null)
					{
						pendingQty = 0;
					}*/// chg41
					pendingQty = pendingQty - minusQty;
					valueXmlString.append("<pending_qty isSrvCallOnChg='0'>").append("<![CDATA[").append(pendingQty).append("]]>").append("</pending_qty>\r\n");
					setNodeValue( dom, "pending_qty", pendingQty );
					sql = "select distinct list_type from pricelist "
						+"where price_list = (select price_list from sorder where sale_order = ?)";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,mVal);

					rs = pstmt.executeQuery();
					while(rs.next())
					{
						listType = rs.getString("list_type");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					sql = "select item_ref from sorditem where sale_order = ? "
						+"and line_no = ? and exp_lev = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,mVal);
					pstmt.setString(2,mcode);
					pstmt.setString(3,mdescr1);

					rs = pstmt.executeQuery();
					while(rs.next())
					{
						itemRef = rs.getString("item_ref");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					sql = "select apply_price , price_var from bom where bom_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,itemCode);

					rs = pstmt.executeQuery();
					while(rs.next())
					{	
						applyPrice = rs.getString("apply_price");
						priceVar = rs.getString("price_var");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if("L".equalsIgnoreCase(listType) && "P".equalsIgnoreCase(applyPrice))
					{
						sql = "select rate from pricelist where "
							+"price_list = (select price_list from sorder where sale_order = ? and item_code = ?)";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,mVal);
						pstmt.setString(2,mdescr2);

						rs = pstmt.executeQuery();
						while(rs.next())
						{	
							itemRate =rs.getDouble("rate");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						sql = "select sum(rate) from pricelist where price_list = ( select price_list from sorder "
							+"where sale_order = ? and item_code in (select item_code from sorditem where "
							+"sale_order = ? and line_no= ? and line_type= 'I'))";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,mVal);
						pstmt.setString(2,mVal);
						pstmt.setString(3,mcode);

						rs = pstmt.executeQuery();
						while(rs.next())
						{	
							totRate = rs.getDouble(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						sql = "select rate__stduom from sorddet where sale_order = ?"
							+" and line_no = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,mVal);
						pstmt.setString(2,mcode);

						rs = pstmt.executeQuery();
						while(rs.next())
						{	
							rateStd = rs.getDouble("rate__stduom");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						diffRate = totRate - rateStd;
						if("A".equalsIgnoreCase(priceVar))
						{

							//rateStd = itemRate - (diffRate * ( itemRate / totRate) ); // 18-08 change
							discAmt = diffRate * ( itemRate / totRate);
							System.out.println("diffRate from getChangeSord if priceVar is A 6 ::::::::" + diffRate);
							System.out.println("itemRate from getChangeSord if priceVar is A 6 ::::::::" + itemRate);
							System.out.println("totRate from getChangeSord if priceVar is A 6 ::::::::" + totRate);
							System.out.println("discAmt from getChangeSord if priceVar is A 6 ::::::::" + discAmt);
							//rateStd = df.parse(df.format(rateStd)).doubleValue(); 
							valueXmlString.append("<rate__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(rateStd).append("]]>").append("</rate__stduom>\r\n");
							setNodeValue( dom, "rate__stduom", rateStd);
							valueXmlString.append("<rate__clg isSrvCallOnChg='0'>").append("<![CDATA[").append(rateStd).append("]]>").append("</rate__clg>\r\n");
							setNodeValue( dom, "rate__clg", rateStd );
							valueXmlString.append("<conf_diff_amt isSrvCallOnChg='0'>").append("<![CDATA[").append(discAmt).append("]]>").append("</conf_diff_amt>\r\n");
							setNodeValue( dom, "conf_diff_amt", discAmt );
						}
						else if("D".equalsIgnoreCase(priceVar))
						{
							discAmt = diffRate * ( itemRate / totRate);
							System.out.println("diffRate from getChangeSord if priceVar is D 7  ::::::::" + diffRate);
							System.out.println("itemRate from getChangeSord if priceVar is D 7 ::::::::" + itemRate);
							System.out.println("totRate from getChangeSord if priceVar is D 7 ::::::::" + totRate);
							System.out.println("discAmt from getChangeSord if priceVar is D 7 ::::::::" + discAmt);
							//itemRate = df.parse(df.format(itemRate)).doubleValue(); 
							valueXmlString.append("<disc_amt isSrvCallOnChg='0'>").append("<![CDATA[").append(discAmt).append("]]>").append("</disc_amt>\r\n");
							setNodeValue( dom, "disc_amt", discAmt );
							valueXmlString.append("<rate__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(itemRate).append("]]>").append("</rate__stduom>\r\n");
							setNodeValue( dom, "rate__stduom", itemRate);
							valueXmlString.append("<rate__clg isSrvCallOnChg='0'>").append("<![CDATA[").append(itemRate).append("]]>").append("</rate__clg>\r\n");
							setNodeValue( dom, "rate__clg", itemRate );
							valueXmlString.append("<conf_diff_amt isSrvCallOnChg='0'>").append("<![CDATA[").append(0).append("]]>").append("</conf_diff_amt>\r\n");
							setNodeValue( dom, "conf_diff_amt", 0);
						}
					}
					else if("L".equalsIgnoreCase(listType) && "E".equalsIgnoreCase(applyPrice))
					{
						sql = "select (case when eff_cost is null then 0 else eff_cost end) from bomdet "
							+"where bom_code = ? and item_ref = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,itemCode);
						pstmt.setString(2,itemRef);

						rs = pstmt.executeQuery();
						while(rs.next())
						{	
							effCost =rs.getDouble(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						//effCost = df.parse(df.format(effCost)).doubleValue(); 
						valueXmlString.append("<rate__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(effCost).append("]]>").append("</rate__stduom>\r\n");
						setNodeValue( dom, "rate__stduom", effCost );
						valueXmlString.append("<rate__clg isSrvCallOnChg='0'>").append("<![CDATA[").append(effCost).append("]]>").append("</rate__clg>\r\n");
						setNodeValue( dom, "rate__clg", effCost );

					}

					sql = "select nature from sorditem where sale_order =? and line_no = ? "
						+" and exp_lev = ? and line_type != 'B'";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,mVal);
					pstmt.setString(2,mcode);
					pstmt.setString(3,mVal1);

					rs = pstmt.executeQuery();
					while(rs.next())
					{	
						nature =rs.getString("nature");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;


					//arrList = distCommon.getConvQuantityFact(mstunit,mUnit, mdescr2, itemRate, rateStd , conn);
					//rateStduom = Double.parseDouble(arrList.get(1).toString());
					//START change 18-08-10 extra code
					sql = "select rate__stduom from sorddet where sale_order = ?"
						+" and line_no = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,mVal);
					pstmt.setString(2,mcode);

					rs = pstmt.executeQuery();
					while(rs.next())
					{	
						rateStd = rs.getDouble("rate__stduom");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					// ed change
					rateStduom =rateStd; // 18-08 -10
					valueXmlString.append("<rate__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(rateStduom).append("]]>").append("</rate__stduom>\r\n");
					setNodeValue( dom, "rate__stduom", rateStduom );
					//System.out.println("nature 6635 ="+nature);
					if("F".equalsIgnoreCase(nature))
					{
						valueXmlString.append("<rate__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(0).append("]]>").append("</rate__stduom>\r\n");
						setNodeValue( dom, "rate__stduom", 0 );
						valueXmlString.append("<line_type isSrvCallOnChg='0'>").append("<![CDATA[").append("F").append("]]>").append("</line_type>\r\n");
						setNodeValue( dom, "line_type","F");
					}
					//added by kunal on 17/06/13 for bonus and sample item
					if("B".equalsIgnoreCase(nature))
					{
						valueXmlString.append("<rate__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(0).append("]]>").append("</rate__stduom>\r\n");
						setNodeValue( dom, "rate__stduom", 0 );
						valueXmlString.append("<line_type isSrvCallOnChg='0'>").append("<![CDATA[").append("B").append("]]>").append("</line_type>\r\n");
						setNodeValue( dom, "line_type", "B" );
					}
					if("S".equalsIgnoreCase(nature))
					{
						valueXmlString.append("<rate__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(0).append("]]>").append("</rate__stduom>\r\n");
						setNodeValue( dom, "rate__stduom", 0 );
						valueXmlString.append("<line_type isSrvCallOnChg='0'>").append("<![CDATA[").append("S").append("]]>").append("</line_type>\r\n");
						setNodeValue( dom, "line_type", "S" );
					}
					//gbf_set_rate_conversion() //miss13 function
					//Added by Abhijit Gaikwad on 09/07/17
					valueXmlString.append("<tax_class isSrvCallOnChg='0'>").append("<![CDATA[").append(lstaxclass).append("]]>").append("</tax_class>\r\n");
					setNodeValue( dom, "tax_class", lstaxclass );
					valueXmlString.append("<tax_chap isSrvCallOnChg='0'>").append("<![CDATA[").append(lstaxchap).append("]]>").append("</tax_chap>\r\n");
					setNodeValue( dom, "tax_chap", lstaxchap );
					valueXmlString.append("<tax_env isSrvCallOnChg='0'>").append("<![CDATA[").append(lstaxenv).append("]]>").append("</tax_env>\r\n");
					setNodeValue( dom, "tax_env", lstaxenv );
					//End 
					
					
				}

				else if(currentColumn.equalsIgnoreCase("quantity"))
				{
					String lsexplev="";
					double disccsorditem=0;
					String lsnature="",lslinetype="",disccsorditem1="";
					smNum = genericUtility.getColumnValue("quantity",dom);
					if(smNum != null && smNum.trim().length() > 0)
					{
						mNum = Double.parseDouble(smNum);
					}
					valueXmlString.append("<quantity_real isSrvCallOnChg='0'>").append("<![CDATA[").append(mNum).append("]]>").append("</quantity_real>\r\n");
					setNodeValue( dom, "quantity_real", mNum );
					mVal = genericUtility.getColumnValue("unit",dom);
					mVal1 = genericUtility.getColumnValue("unit__std",dom);
					itemCode = genericUtility.getColumnValue("item_code",dom);
					smNum1 = genericUtility.getColumnValue("conv__qty_stduom",dom);
					lsexplev = genericUtility.getColumnValue("exp_lev",dom);
					System.out.println("quantity case.. :"+mVal+"   "+mVal1+"   "+itemCode+"   "+mNum +"  "+smNum1);
					System.out.println("quantity case..lsexplev.. :"+lsexplev);

					//itemCode = genericUtility.getColumnValue("item_code",dom);
					siteCode = genericUtility.getColumnValue("site_code",dom);
					locCode = genericUtility.getColumnValue("loc_code",dom);
					lotNo = genericUtility.getColumnValue("lot_no",dom);
					lotSl = genericUtility.getColumnValue("lot_sl",dom);
					System.out.println("locCode :"+locCode+"lotNo :"+lotNo+" lotSl :"+lotSl);
					if(itemCode == null)
					{
						itemCode = "";
					}
					if(smNum1 != null && smNum1.trim().length() > 0)
					{
						mNum1 = Double.parseDouble(smNum1);
					}
					mNum2 = mNum1;
					if(mVal == null || mVal.trim().length() == 0)
					{
						sql = "select unit from item where item_code =?  ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,itemCode);

						rs = pstmt.executeQuery();
						while(rs.next())
						{	
							mVal =rs.getString("unit");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("unitStd.. :"+mVal+"   "+mVal1+"   "+itemCode+"   "+mNum);

						if(mVal == null)
						{
							mVal="";
						}
						if(mVal1 == null)
						{
							mVal1="";
						}
						arrList = distCommon.getConvQuantityFact(mVal, mVal1, itemCode, mNum, mNum1 , conn);

						mNum = Double.parseDouble(arrList.get(1).toString());
						valueXmlString.append("<unit isSrvCallOnChg='0'>").append("<![CDATA[").append(mVal).append("]]>").append("</unit>\r\n");
						setNodeValue( dom, "unit", (mVal == null) ? "" : mVal );
					}
					else
					{
						System.out.println("unitStd. :"+mVal+"   "+mVal1+"   "+itemCode+"   "+mNum);
						arrList = distCommon.getConvQuantityFact(mVal, mVal1, itemCode, mNum, mNum1 , conn);

						mNum = Double.parseDouble(arrList.get(1).toString());
					}

					if(mNum2 == 0)
					{
						valueXmlString.append("<conv__qty_stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(mNum1).append("]]>").append("</conv__qty_stduom>\r\n");
						setNodeValue( dom, "conv__qty_stduom", mNum1 );
						valueXmlString.append("<conv__rtuom_stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(mNum1).append("]]>").append("</conv__rtuom_stduom>\r\n");///13-08
						setNodeValue( dom, "conv__rtuom_stduom", (mNum1) );///13-08
					}

					/*valueXmlString.append("<quantity__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(mNum3).append("]]>").append("</quantity__stduom>\r\n");
					setNodeValue( dom, "quantity__stduom", mNum3 );
					System.out.println("quantity Stduom from getChangeSord from mNum3 " + mNum3);
					 */

					//valueXmlString.append("<rate__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(mNum).append("]]>").append("</rate__stduom>\r\n");
					//setNodeValue( dom, "rate__stduom", mNum); /// 16-08
					custCode = genericUtility.getColumnValue("cust_code",dom1);
					saleOrder = genericUtility.getColumnValue("sord_no",dom);
					lineNo = genericUtility.getColumnValue("line_no__sord",dom);
					String lsitemflg="";//Added by Abhijit
					sql = "select site_code ,pack_code,item_flg from sorddet where sale_order = ? and line_no = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,saleOrder);
					pstmt.setString(2,lineNo);

					rs = pstmt.executeQuery();
					while(rs.next())
					{	
						siteCodeDet = rs.getString("site_code");
						packCode = rs.getString("pack_code");
						lsitemflg= rs.getString("item_flg");
						
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println("item_flg["+lsitemflg+"]");
					//noArt = distCommon.getNoArt(siteCodeDet,custCode,itemCode,packCode,mNum,'B',acShipperQty,acIntQty ,conn);
					//System.out.println("manohar 15/10/10 noArt from distCommon [" + noArt + "]");
					////noArt = gf_get_no_art(ls_site_code_det,ls_cust_code,itemCode,ls_pack_code,mNum,'B',ac_shipper_qty,ac_int_qty) //miss15 function
					//valueXmlString.append("<no_art isSrvCallOnChg='0'>").append("<![CDATA[").append(noArt).append("]]>").append("</no_art>\r\n");
					//setNodeValue( dom, "no_art", noArt );
					if(noArt == 0)
					{
						//itemCode = genericUtility.getColumnValue("item_code",dom);
						//siteCode = genericUtility.getColumnValue("site_code",dom);
						//locCode = genericUtility.getColumnValue("loc_code",dom);
						//lotNo = genericUtility.getColumnValue("lot_no",dom);
						//lotSl = genericUtility.getColumnValue("lot_sl",dom);

						sql = "select (case when qty_per_art is null then 0 else qty_per_art end) "
							+" from stock where item_code = ? and site_code = ? "
							+" and loc_code  = ? and lot_no = ? and lot_sl 	= ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,itemCode);
						pstmt.setString(2,siteCode);
						pstmt.setString(3,locCode);
						pstmt.setString(4,lotNo);
						pstmt.setString(5,lotSl);

						rs = pstmt.executeQuery();
						while(rs.next())
						{	
							qtyPerArt = rs.getDouble(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null; 
						if(qtyPerArt > 0)
						{
							smNum = genericUtility.getColumnValue("quantity",dom);
							if(smNum !=null && smNum.trim().length() > 0)
							{
								mNum = Double.parseDouble(smNum);
							}
							valueXmlString.append("<no_art isSrvCallOnChg='0'>").append("<![CDATA[").append(Math.round(mNum / qtyPerArt)).append("]]>").append("</no_art>\r\n"); //chg43
							setNodeValue( dom, "no_art", Math.round(mNum / qtyPerArt) );
						}
					}

					sNoArticle = genericUtility.getColumnValue("no_art",dom);
					if(sNoArticle !=null && sNoArticle.trim().length() >0) 
					{
						noArticle = Double.parseDouble(sNoArticle);
						if (noArticle == 0)
						{
							noArticle = 1;
						}
						if(noArticle > 0)
						{
							packQty = mNum / noArticle;
							// 19-1-11 - Chandni Shah
							packQty = df.parse(df.format(packQty)).doubleValue();
							System.out.println("28/08/10 packQty 5 [" + packQty + "]");
							valueXmlString.append("<pack_qty isSrvCallOnChg='0'>").append("<![CDATA[").append(packQty).append("]]>").append("</pack_qty>\r\n");
							setNodeValue( dom, "pack_qty", packQty );
						}
					}
					shipperQty = acShipperQty; 
					intQty = acIntQty; 
					ArrayList list = new ArrayList();
					//change by kunal on 15/jan/14 change for  SCM issue tracker 169-N set correct Qty details 
					//noArt1 = distCommon.getNoArt(siteCodeDet,custCode,itemCode,packCode,mNum,'S',acShipperQty,acIntQty ,conn);
					list = distCommon.getNoArtAList(siteCodeDet,custCode,itemCode,packCode,mNum,'S',acShipperQty,acIntQty ,conn);
					System.out.println("kunal test 7252::"+list.toString());
					noArt1 = Double.parseDouble((String)list.get(0)) ;
					acShipperQty = Double.parseDouble((String)list.get(1)) ;
					acIntQty = Double.parseDouble((String)list.get(2)) ;

					balQty = mNum - (shipperQty * noArt1);
					//noArt2 = distCommon.getNoArt(siteCodeDet,custCode,itemCode,packCode,mNum,'S',acShipperQty,acIntQty ,conn);
					list = distCommon.getNoArtAList(siteCodeDet,custCode,itemCode,packCode,balQty,'I',acShipperQty,acIntQty ,conn);
					System.out.println("kunal test 7260::"+list.toString());
					noArt2 = Double.parseDouble((String)list.get(0)) ;
					//noArt2 = 1;
					acShipperQty = Double.parseDouble((String)list.get(1)) ;
					acIntQty = Double.parseDouble((String)list.get(2)) ;

					intQty = acIntQty;
					shipperQty = shipperQty * noArt1;
					intQty = intQty * noArt2;
					looseQty = mNum - (shipperQty + intQty );
					str = "Shipper Quantity = "+(Math.round(shipperQty*100.0)/100.0 )+ " Integral Quantity = "+(Math.round(intQty*100.0)/100.0)+  " Loose Quantity = "+(Math.round(looseQty*100.0)/100.0);//miss string() =round()
					valueXmlString.append("<qty_details isSrvCallOnChg='0'>").append("<![CDATA[").append(str).append("]]>").append("</qty_details>\r\n");
					setNodeValue( dom, "qty_details", (str == null ) ? "": str );
					if(packCode !=null && packCode.trim().length() > 0)
					{
						sql = "select gross_weight,nett_weight from packing where pack_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,packCode);

						rs = pstmt.executeQuery();
						while(rs.next())
						{	
							grossWt = rs.getDouble("gross_weight");
							nettWt = rs.getDouble("nett_weight");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						sNoArt = genericUtility.getColumnValue("no_art",dom);
						if(sNoArt != null && sNoArt.trim().length() > 0)
						{
							noArt = Double.parseDouble(sNoArt);
						}
						grossWt = grossWt * noArt;
						grossWt = df.parse(df.format(grossWt)).doubleValue(); 
						nettWt  = nettWt  * noArt;
						nettWt = df.parse(df.format(nettWt)).doubleValue(); 
						sGrossWeight = genericUtility.getColumnValue("gross_weight",dom);
						if(sGrossWeight != null && sGrossWeight.trim().length() > 0)
						{
							grossWeight = Double.parseDouble(sGrossWeight);
						}

						if(sGrossWeight == null || grossWeight == 0)
						{
							valueXmlString.append("<gross_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(grossWt).append("]]>").append("</gross_weight>\r\n");
							setNodeValue( dom, "gross_weight", grossWt);
							valueXmlString.append("<nett_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(nettWt).append("]]>").append("</nett_weight>\r\n");
							setNodeValue( dom, "nett_weight",nettWt);
							valueXmlString.append("<tare_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(grossWt - nettWt).append("]]>").append("</tare_weight>\r\n");
							setNodeValue( dom, "tare_weight", (grossWt - nettWt ));
						}
					}

					sDiscAmt = genericUtility.getColumnValue("disc_amt",dom);
					if(sDiscAmt != null && sDiscAmt.trim().length() > 0)
					{
						discAmt = Double.parseDouble(sDiscAmt);
					}

					mcode = genericUtility.getColumnValue("line_no__sord",dom);
					mVal = genericUtility.getColumnValue("sord_no",dom);
					smNum1 = genericUtility.getColumnValue("quantity__stduom",dom);//
					if(smNum1 != null && smNum1.trim().length() > 0)
					{
						mNum1 = Double.parseDouble(smNum1);
					}
					else
					{
						mNum1 = mNum3;
					}

					smNum2 = genericUtility.getColumnValue("rate__stduom",dom);//
					System.out.println("manohar 07/02/11 smNum1 [ " + smNum1 + "] smNum2 [" + smNum2 + "] mNum3 ["+ mNum3 +"]");
					if(smNum2 != null && smNum2.trim().length() > 0)
					{
						mNum2 = Double.parseDouble(smNum2);
					}
					else
					{
						mNum2 = 0;
					}


					itemCodeOrd = genericUtility.getColumnValue("item_code__ord",dom);
					applyPrice = null;

					sql = "select apply_price from bom where bom_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,itemCodeOrd);

					rs = pstmt.executeQuery();
					while(rs.next())
					{	
						applyPrice = rs.getString("apply_price");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if(applyPrice == null)
					{

						sql = "select discount from sorddet where sale_order = ? and line_no = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,mVal);
						pstmt.setString(2,mcode);

						rs = pstmt.executeQuery();
						while(rs.next())
						{	
							discPerc = rs.getDouble("discount");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						discAmt = (discPerc/100) * (mNum1 * mNum2);
						System.out.println("discPerc from getChangeSord if priceVar is D 9  ::::::::" + discPerc);
						System.out.println("mNum1 from getChangeSord if priceVar is D 9 ::::::::" + mNum1);
						System.out.println("mNum2 from getChangeSord if priceVar is D 9 ::::::::" + mNum2);
						System.out.println("discAmt from getChangeSord if priceVar is D 9 ::::::::" + discAmt);
						valueXmlString.append("<disc_amt isSrvCallOnChg='0'>").append("<![CDATA[").append(discAmt).append("]]>").append("</disc_amt>\r\n");
						setNodeValue( dom, "disc_amt", discAmt );
						
					}
		
					// Added by Abhijit  10/07/2017 , discount set from sorditem when schem is define
					if("B".equalsIgnoreCase(lsitemflg))
					{
						sql="select nature,line_type,discount from sorditem where sale_order= ? and line_no= ? and exp_lev=? and line_type <> 'B' ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,mVal);
						pstmt.setString(2,lineNo);
						pstmt.setString(3,lsexplev);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							lsnature=rs.getString("nature");
							lslinetype =rs.getString("line_type");
							disccsorditem1 = rs.getString("discount");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("disccsorditem1 ["+disccsorditem1 +"]");
						System.out.println("lsnature ["+lsnature +"]");
						if(disccsorditem1 != null && disccsorditem1.trim().length() > 0)
						{
							disccsorditem = Double.parseDouble(disccsorditem1);
						}
						else
						{
							disccsorditem = 0;
						}
						System.out.println("disccsorditem["+disccsorditem+"]");
						
						if(disccsorditem > 0 && "C".equalsIgnoreCase(lsnature))
						{
							discAmt=(disccsorditem/100) * (mNum1 * mNum2);
							System.out.println("disccsorditem from getChangeSord if lsitemflg is B ::::::::" + disccsorditem);
							System.out.println("mNum1 from getChangeSord if lsitemflg is B ::::::::" + mNum1);
							System.out.println("mNum2 from getChangeSord if lsitemflg is B ::::::::" + mNum2);
							System.out.println("discAmt from getChangeSord if lsitemflg is B::::::::" + discAmt);
							
							valueXmlString.append("<disc_amt isSrvCallOnChg='0'>").append("<![CDATA[").append(discAmt).append("]]>").append("</disc_amt>\r\n");
							setNodeValue( dom, "disc_amt", discAmt );
							valueXmlString.append("<discount isSrvCallOnChg='0'>").append("<![CDATA[").append(disccsorditem).append("]]>").append("</discount>\r\n");
							setNodeValue( dom, "discount", disccsorditem );
							
							
						}
					}
				}
				else if(currentColumn.equalsIgnoreCase("lot_no") || currentColumn.equalsIgnoreCase("lot_sl"))
				{
					//disc_amt
					String unitstd="";
					itemCode = genericUtility.getColumnValue("item_code",dom);
					siteCode = genericUtility.getColumnValue("site_code",dom);
					locCode = genericUtility.getColumnValue("loc_code",dom);
					lotNo = genericUtility.getColumnValue("lot_no",dom);
					lotSl = genericUtility.getColumnValue("lot_sl",dom);
					saleord = genericUtility.getColumnValue("sord_no",dom);
					saleordLine = genericUtility.getColumnValue("line_no__sord",dom);
					smNum = genericUtility.getColumnValue("quantity", dom);
					unitstd = genericUtility.getColumnValue("unit__std", dom);
					System.out.println("unit__std"+unitstd);
					System.out.println("lot_no AND lot_sl....:"+itemCode+"..."+siteCode+"..."+locCode+"..."+lotNo+"..."+lotSl+"..."+saleord+"..."+ saleordLine +"..QUANTITY"+smNum);
					if(itemCode == null)
					{
						itemCode="";
					}

					if(lotSl == null)
					{
						lotSl = "";
					}
					if(lotNo == null)
					{
						lotNo = "";
					}
					sql = "select (case when gross_weight is null then 0 else gross_weight end), "
						+"(case when net_weight is null then 0 else net_weight end), "
						+"(case when tare_weight is null then 0 else tare_weight end), "
						+"(case when qty_per_art is null then 1 else qty_per_art end), "
						+" pack_instr, dimension, "
						+"(case when rate is null then 0 else rate end), "
						+"(case when gross_wt_per_art is null then 0 else gross_wt_per_art end), "
						+"(case when tare_wt_per_art is null then 0 else tare_wt_per_art end), "
						+"exp_date, mfg_date, site_code__mfg, "
						+"(case when pallet_wt is null then 0 else pallet_wt end),retest_date, " 
						+" quantity "
						+" from stock where  item_code = ? and "
						+" site_code = ? and  loc_code  = ? and  "	
						+"lot_no = ? and  lot_sl 	= ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,itemCode);
					pstmt.setString(2,siteCode);
					pstmt.setString(3,locCode);
					pstmt.setString(4,lotNo);
					pstmt.setString(5,lotSl);

					rs = pstmt.executeQuery();
					if(rs.next())
					{	
						grossWeight = rs.getDouble(1);
						nettWeight = rs.getDouble(2);
						tareWeight = rs.getDouble(3);
						qtyPerArt = rs.getDouble(4);

						packInstr = rs.getString(5);
						dimension  = rs.getString(6);
						dimension = (dimension == null ? " ": dimension );
						stcrate  = rs.getDouble(7);
						grossWeightArt = rs.getDouble(8);

						tareWeightArt = rs.getDouble(9);

						expDate = rs.getTimestamp(10);
						mfgDate = rs.getTimestamp(11);
						// commented by cpatil on 31/JAN/13 as per suggestion start
						//siteMfg  = rs.getString(12);        
						siteMfg  = ( rs.getString(12) == null ?"":rs.getString(12));   
						System.out.println("@@@@ siteMfg:["+siteMfg+"]");
						packInstrStock = packInstr;
						System.out.println("@@@@ packInstrStock:["+packInstrStock+"]");
						// commented by cpatil on 31/JAN/13 as per suggestion modified end
						palletWt = rs.getDouble(13);

						retestDate = rs.getTimestamp(14);
						//qty1 = rs.getDouble(15);
						if(smNum != null && smNum.trim().length() > 0)
						{
							mNum = Double.parseDouble(smNum);
						}
						qty1 = mNum;

						System.out.println("quantity  :"+mNum);
						sNoArt = genericUtility.getColumnValue("no_art",dom);
						if(sNoArt != null && sNoArt.trim().length() > 0)
						{
							noArt = Double.parseDouble(sNoArt);
						}
						else
						{
							noArt = 1;
						}
						if(noArt == 0)
						{
							/*if(qtyPerArt > 0)
							{
								valueXmlString.append("<no_art isSrvCallOnChg='0'>").append("<![CDATA[").append(Math.round(mNum/qtyPerArt)).append("]]>").append("</no_art>\r\n");
								setNodeValue( dom, "no_art", (Math.round(qty1/qtyPerArt)));
							}*/
						}
						//qtyPerArt = 0;//
						if(qtyPerArt == 0)
						{
							qtyPerArt = 1;
						}

						grossWeight = (grossWeightArt/qtyPerArt) * qty1;
						grossWeight = df.parse(df.format(grossWeight)).doubleValue(); 
						tareWeight  = (tareWeightArt/qtyPerArt) * qty1;
						tareWeight = df.parse(df.format(tareWeight)).doubleValue(); 
						nettWeight  = grossWeight -  tareWeight	;
						nettWeight = df.parse(df.format(nettWeight)).doubleValue(); 

						System.out.println("28/10/10 manohar before qty1 [" + qty1 + "] qtyPerArt {" + qtyPerArt +"] no_art [" + noArt + "] grossWeight [" + grossWeight + "] tareWeight [" + tareWeight + "] nettWeight [" + nettWeight +"]"); 

						//comment by kunal on 15/jan/14 change for grossWeight,tareWeight,nettWeight calculation SCM issue tracker 169-N 
						/*
						noArt = qty1 / qtyPerArt;
						noArt = getReqDecimal(noArt, 0);
						grossWeight = (grossWeightArt * noArt);///qtyPerArt) * qty1;
						grossWeight = df.parse(df.format(grossWeight)).doubleValue(); 
						tareWeight  = (tareWeightArt * noArt); ///qtyPerArt) * qty1;
						tareWeight = df.parse(df.format(tareWeight)).doubleValue(); 
						nettWeight  = grossWeight -  tareWeight	;
						nettWeight = df.parse(df.format(nettWeight)).doubleValue();
						 */ 

						System.out.println("28/10/10 manohar after qty1 [" + qty1 + "] qtyPerArt {" + qtyPerArt +"] no_art [" + noArt + "] grossWeight [" + grossWeight + "] tareWeight [" + tareWeight + "] nettWeight [" + nettWeight +"]"); 
						sql = "select stk_opt   ,(case when track_shelf_life is null then 'N' else track_shelf_life end) "
							+" from item where item_code = ? ";
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1,itemCode);

						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{	
							stkOpt = rs1.getString(1); 
							trackShelfLife = rs1.getString(2);
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
						if(expDate != null)
						{
							sExpDate = sdf.format(expDate).toString();
						}
						else
						{
							sExpDate="";
						}
						if(mfgDate != null)
						{
							sMfgDate = sdf.format(mfgDate).toString();
						}
						else
						{
							sMfgDate="";
						}
						if(retestDate != null)
						{
							sRetestDate = sdf.format(retestDate).toString();
						}
						else
						{
							sRetestDate="";
						}

						if(!("0".equalsIgnoreCase(stkOpt)))
						{
							if("Y".equalsIgnoreCase(trackShelfLife) && expDate != null)
							{
								valueXmlString.append("<exp_date isSrvCallOnChg='0'>").append("<![CDATA[").append(sExpDate).append("]]>").append("</exp_date>\r\n");
								setNodeValue( dom, "exp_date", sExpDate );
							}
							valueXmlString.append("<mfg_date isSrvCallOnChg='0'>").append("<![CDATA[").append(sMfgDate).append("]]>").append("</mfg_date>\r\n");
							setNodeValue( dom, "mfg_date", (sMfgDate == null) ? "" : sMfgDate );
							valueXmlString.append("<site_code__mfg isSrvCallOnChg='0'>").append("<![CDATA[").append(siteMfg).append("]]>").append("</site_code__mfg>\r\n");
							setNodeValue( dom, "site_code__mfg", (siteMfg == null) ? "" : siteMfg );
							valueXmlString.append("<retest_date isSrvCallOnChg='0'>").append("<![CDATA[").append(sRetestDate).append("]]>").append("</retest_date>\r\n");
							setNodeValue( dom, "retest_date", (sRetestDate == null) ? "" : sRetestDate );
						}

						if(packInstr == null || packInstr.trim().length() == 0)
						{
							sql = "select pack_instr from sorddet where sale_order = ? and line_no = ?";
							pstmt1 = conn.prepareStatement(sql);
							pstmt1.setString(1,saleord);
							pstmt1.setString(2,saleordLine);

							rs1 = pstmt1.executeQuery();
							if(rs1.next())
							{	
								packInstr = rs1.getString("pack_instr"); 
							}
							rs1.close();
							rs1 = null;
							pstmt1.close();
							pstmt1 = null;
						}
						valueXmlString.append("<pallet_wt isSrvCallOnChg='0'>").append("<![CDATA[").append(palletWt).append("]]>").append("</pallet_wt>\r\n");
						setNodeValue( dom, "pallet_wt", palletWt );
						valueXmlString.append("<gross_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(grossWeight).append("]]>").append("</gross_weight>\r\n");
						setNodeValue( dom, "gross_weight", grossWeight );
						valueXmlString.append("<nett_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(nettWeight).append("]]>").append("</nett_weight>\r\n");
						setNodeValue( dom, "nett_weight", nettWeight );
						valueXmlString.append("<tare_weight isSrvCallOnChg='0'>").append("<![CDATA[").append(tareWeight).append("]]>").append("</tare_weight>\r\n");
						setNodeValue( dom, "tare_weight", tareWeight );
						System.out.println("@@@@@@@@ 55:::packInstrInvPackRcp:["+packInstrInvPackRcp+"]::::packInstr["+packInstr+"]");
						if( packInstrInvPackRcp == null || packInstrInvPackRcp.trim().length()==0 )
						{
							valueXmlString.append("<pack_instr isSrvCallOnChg='0'>").append("<![CDATA[").append(blanknull(packInstr)).append("]]>").append("</pack_instr>\r\n");
							setNodeValue( dom, "pack_instr", (packInstr == null) ? " " : packInstr );
						}
						else
						{
							valueXmlString.append("<pack_instr isSrvCallOnChg='0'>").append("<![CDATA[").append(blanknull(packInstrInvPackRcp)).append("]]>").append("</pack_instr>\r\n");
							setNodeValue( dom, "pack_instr", (packInstrInvPackRcp == null) ? " " : packInstrInvPackRcp );
						}
						valueXmlString.append("<dimension isSrvCallOnChg='0'>").append("<![CDATA[").append(blanknull(dimension)).append("]]>").append("</dimension>\r\n");
						setNodeValue( dom, "dimension", (dimension == null) ? " " : dimension );
						valueXmlString.append("<no_art isSrvCallOnChg='0'>").append("<![CDATA[").append(noArt).append("]]>").append("</no_art>\r\n");
						setNodeValue( dom, "no_art", (noArt));

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					sRateStd = genericUtility.getColumnValue("rate__stduom",dom);
					if(sRateStd != null && sRateStd.trim().length() > 0)
					{
						rateStd = Double.parseDouble(sRateStd);
					}
					saleord = genericUtility.getColumnValue("sord_no",dom);
					saleordLine = genericUtility.getColumnValue("line_no__sord",dom);

					sordRate =0;

					sql = "select  (case when rate__stduom is null then 0 else rate__stduom end),"
						+" (case when rate__clg is null then 0 else rate__clg end),quantity__stduom "
						+" from sorddet where sale_order = ? and line_no = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,saleord);
					pstmt.setString(2,saleordLine);

					rs = pstmt.executeQuery();
					if(rs.next())
					{	
						sordRate = rs.getDouble(1); 
						sordExcRate = rs.getDouble(2); 
						sordQuantity = rs.getDouble(3); 

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;	

					explev = genericUtility.getColumnValue("exp_lev",dom);
					saleOrder = genericUtility.getColumnValue("sord_no",dom);
					lineNoSord = genericUtility.getColumnValue("line_no__sord",dom);


					sql = "select nature from sorditem where sale_order =? and line_no = ? "
						+" and exp_lev = ? and line_type != 'B'";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,saleOrder);
					pstmt.setString(2,lineNoSord);
					pstmt.setString(3,explev);

					rs = pstmt.executeQuery();
					if(rs.next())
					{	
						nature =rs.getString("nature");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					String custcode="",plistdiscStr="";
					double plistdisc=0,discMerge=0;
					sql = "select price_list,price_list__clg,price_list__disc,cust_code,pl_date,order_date  from sorder where sale_order =?  ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,saleord);
					System.out.println("@@@sql is"+sql);
					rs = pstmt.executeQuery();
					if(rs.next())
					{	
						priceList = rs.getString("price_list");
						priceListClg = rs.getString("price_list__clg");
						plistdiscStr=rs.getString("price_list__disc");
					    custcode=rs.getString("cust_code");
					    plistdate = rs.getTimestamp("pl_date");
					    orderdate = rs.getTimestamp("order_date");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					sDespDate = genericUtility.getColumnValue("desp_date",dom1);

					if(sDespDate == null)
					{
						sDespDate ="";
					}

					mUnit = genericUtility.getColumnValue("unit",dom);
					mstunit = genericUtility.getColumnValue("unit_std",dom);
					sItemRate = genericUtility.getColumnValue("rate",dom);
					if(sItemRate != null)
					{
						itemRate = Double.parseDouble(sItemRate);
					}
					//start change extra
					sql = "select  rate__stduom	from sorddet where sale_order = ? and line_no = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,saleord);
					pstmt.setString(2,saleordLine);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						rateStd = rs.getDouble("rate__stduom");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					//end change 18-08-10


					//cas24
					if(mstunit == null)
					{
						mstunit="";
					}
					if(mUnit == null)
					{
						mUnit="";
					}
					//arrList = distCommon.getConvQuantityFact(mstunit,mUnit, siteCode, itemRate, qty1,  conn);

					//rateStduom = Double.parseDouble(arrList.get(1).toString());
					rateStduom = rateStd ; // 18-08 -10 
					valueXmlString.append("<rate__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(rateStduom).append("]]>").append("</rate__stduom>\r\n");
					setNodeValue( dom, "dimension", (dimension == null) ? " " : dimension );
					int Count=0;
					System.out.println("Nature is (ABHI)"+nature);
					if (nature == null) 
					{
					 nature = "C";
					}
					System.out.println("Sorder Rate is "+ sordRate);
					System.out.println("nature  is "+ nature);
					if (sordRate == 0 && nature.equalsIgnoreCase("C"))
					{
						System.out.println("Price List discount is " + plistdiscStr);
				
					if(plistdiscStr != null && plistdiscStr.trim().length() > 0)
					 {
						        System.out.println("DIscount COndition");
								plistdisc = getDiscount(plistdiscStr,orderdate,custcode,siteCode,itemCode,unitstd,discMerge,plistdate,sordQuantity,conn);
								System.out.println("Get discount is :"+ plistdisc);
								listType = distCommon.getPriceListType(plistdiscStr , conn);
								System.out.println("listType is:" +listType);
								if("M".equalsIgnoreCase(listType))
								{
								
									if(! "L".equalsIgnoreCase(listType))
									{
										System.out.println("List type L");
										pickRate= distCommon.pickRate(priceList,sDespDate,itemCode,lotNo,"D",sordQuantity, conn);
									}
									pickRate =calcRate(pickRate,plistdisc);
									System.out.println("mrate from calcRate function====="+pickRate);
								}
								else
								{
									System.out.println("DIscount Else Condition ");
									sql ="select count(*) from pricelist where price_list = ? and list_type = ?";
									pstmt = conn.prepareStatement(sql);
								    pstmt.setString(1,priceList);
								    pstmt.setString(2,"I");
								    rs = pstmt.executeQuery();
									if (rs.next()) 
									{
										Count = rs.getInt(1);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									System.out.println("Count in Else discount Condition"+ Count);
									//ibase3-webitm-dis5-12-1-01
									// if no record found in price list for I (Inventory) then continue with the current logic else check in Stock table
									if(Count == 0 ) 
									{
										System.out.println("Count is 0 then List type is:"+listType);
										if(! "L".equalsIgnoreCase(listType))
										{
											System.out.println("Condition True");
											          
											pickRate = distCommon.pickRate(priceList,sDespDate,itemCode,lotNo,"D",sordQuantity, conn);
											System.out.println("PicRate is in else discount condition"+pickRate);
										
										}
									}
									else
									{
										System.out.println("Count is not zero");

										String listTyp=   siteCode + "@" + locCode + "@" + lotNo;
										System.out.println("Listtype is "+listType);
										pickRate=distCommon.pickRate(priceList, sDespDate, itemCode, listTyp, "I", sordQuantity, conn);
										System.out.println("PicRate "+pickRate);
									}
								}
								sConv = genericUtility.getColumnValue("conv__rtuom_stduom",dom);
								if(sConv != null && sConv.trim().length() > 0)
								{
									conv = Double.parseDouble(sConv);
									valueXmlString.append("<conv__rtuom_stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(conv).append("]]>").append("</conv__rtuom_stduom>\r\n");///13-08
									setNodeValue( dom, "conv__rtuom_stduom", (conv) ); ///13-08
								}
								if(sConv ==null || conv == 0)
								{
									conv = 1;
								}
								System.out.println("pickRate"+pickRate);
								System.out.println("conv"+conv);
								rateStduom1 = pickRate * conv;
								System.out.println("pickRate * conv"+rateStduom1);
								valueXmlString.append("<rate__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(rateStduom1).append("]]>").append("</rate__stduom>\r\n");
								setNodeValue( dom, "rate__stduom", rateStduom1 );
								valueXmlString.append("<conv__rtuom_stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(conv).append("]]>").append("</conv__rtuom_stduom>\r\n");///13-08
								setNodeValue( dom, "conv__rtuom_stduom", (conv) );
									
						}	
					else
					{
						System.out.println("ELse Condition");
					if(sordRate == 0 )
					{
						if(priceList != null && priceList.trim().length() > 0)
						{
							sql = "select count(*) from pricelist where price_list = ? and list_type = 'I'";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,priceList);

							rs = pstmt.executeQuery();
							if(rs.next())
							{	
								count =rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if(count == 0)
							{
								listType = distCommon.getPriceListType(priceList , conn);

								if(listType == null || listType.trim().length() == 0)
								{
									sql = "select price_list__parent from pricelist where price_list =? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1,priceList);

									rs = pstmt.executeQuery();
									if(rs.next())
									{	
										priceListParent =rs.getString("price_list__parent");
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									listType = distCommon.getPriceListType(priceListParent , conn);

								}

								if("B".equalsIgnoreCase(listType))
								{
									//pickRate = distCommon.pickRateGSM(priceList,sDespDate,itemCode,lotNo,"B",sordQuantity , conn); //miss23 function
									pickRate = distCommon.pickRate(priceList,sDespDate,itemCode,lotNo,"B",sordQuantity, conn);
									System.out.println("pickRate :"+pickRate);
									valueXmlString.append("<rate__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(pickRate).append("]]>").append("</rate__stduom>\r\n");
									setNodeValue( dom, "rate__stduom", pickRate );
								}
								else 
								{
									pickRate = -1;
								}

							}
							else if(count > 0)
							{
								//pickRate = distCommon.pickRateGSM(priceList,sDespDate,itemCode,lotNo,"I",sordQuantity , conn); 
								pickRate = distCommon.pickRate(priceList,sDespDate,itemCode,lotNo,"I",sordQuantity, conn);
								System.out.println("count >0 pickRate :"+pickRate);
								valueXmlString.append("<rate__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(pickRate).append("]]>").append("</rate__stduom>\r\n");
								setNodeValue( dom, "rate__stduom", pickRate );
							}

						}

						sConv = genericUtility.getColumnValue("conv__rtuom_stduom",dom);
						if(sConv != null && sConv.trim().length() > 0)
						{
							conv = Double.parseDouble(sConv);
							valueXmlString.append("<conv__rtuom_stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(conv).append("]]>").append("</conv__rtuom_stduom>\r\n");///13-08
							setNodeValue( dom, "conv__rtuom_stduom", (conv) ); ///13-08
						}
						if(sConv ==null || conv == 0)
						{
							conv = 1;
						} // 13/04/12 manoharan
						rateStduom1 = pickRate * conv;
						//rateStduom1 = df.parse(df.format(rateStduom1)).doubleValue(); 
						valueXmlString.append("<rate__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(rateStduom1).append("]]>").append("</rate__stduom>\r\n");
						setNodeValue( dom, "rate__stduom", rateStduom1 );
						valueXmlString.append("<conv__rtuom_stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(conv).append("]]>").append("</conv__rtuom_stduom>\r\n");///13-08
						setNodeValue( dom, "conv__rtuom_stduom", (conv) );///13-08
						//} // 13/04/12 manoharan

					}
					}
					
					}

					pickRateClg = 0;

					if(priceListClg !=null && priceListClg.trim().length() > 0)
					{

						//pickRateClg = distCommon.pickRateGSM(priceListClg,sDespDate,itemCode,lotNo,"B",sordQuantity , conn);
						pickRateClg = distCommon.pickRate(priceListClg,sDespDate,itemCode,lotNo,"B",sordQuantity, conn);

						valueXmlString.append("<rate__clg isSrvCallOnChg='0'>").append("<![CDATA[").append(pickRateClg).append("]]>").append("</rate__clg>\r\n");
						setNodeValue( dom, "rate__clg", pickRateClg );
					}
					else
					{
						mcode = genericUtility.getColumnValue("line_no__sord",dom);
						mVal = genericUtility.getColumnValue("sord_no",dom);
						sql = "select rate__clg from sorddet where sale_order = ? and line_no = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,mVal);
						pstmt.setString(2,mcode);

						rs = pstmt.executeQuery();
						while(rs.next())
						{	
							pickRateClg =rs.getDouble("rate__clg");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (pickRateClg ==0) //pickRateClg == null ||
						{
							valueXmlString.append("<rate__clg isSrvCallOnChg='0'>").append("<![CDATA[").append(pickRate).append("]]>").append("</rate__clg>\r\n");
							setNodeValue( dom, "rate__clg", pickRate );
						}
						else
						{
							valueXmlString.append("<rate__clg isSrvCallOnChg='0'>").append("<![CDATA[").append(pickRateClg).append("]]>").append("</rate__clg>\r\n");
							setNodeValue( dom, "rate__clg", pickRateClg );
						}
					}
					//System.out.println("nature 6635 ="+nature);
					if("F".equalsIgnoreCase(nature))
					{
						valueXmlString.append("<rate__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(0).append("]]>").append("</rate__stduom>\r\n");
						setNodeValue( dom, "rate__stduom", 0 );
						valueXmlString.append("<line_type isSrvCallOnChg='0'>").append("<![CDATA[").append('F').append("]]>").append("</line_type>\r\n");
						setNodeValue( dom, "line_type", 'F' );
					}
					//valueXmlString.append(getChangeSord(dom , dom1 , "quantity", xtraParams , conn));  /// 17-08-10
					//added by kunal on 17/06/13 for bonus and sample item
					if("B".equalsIgnoreCase(nature))
					{
						valueXmlString.append("<rate__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(0).append("]]>").append("</rate__stduom>\r\n");
						setNodeValue( dom, "rate__stduom", 0 );
						valueXmlString.append("<line_type isSrvCallOnChg='0'>").append("<![CDATA[").append("B").append("]]>").append("</line_type>\r\n");
						setNodeValue( dom, "line_type", "B" );
					}
					if("S".equalsIgnoreCase(nature))
					{
						valueXmlString.append("<rate__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(0).append("]]>").append("</rate__stduom>\r\n");
						setNodeValue( dom, "rate__stduom", 0 );
						valueXmlString.append("<line_type isSrvCallOnChg='0'>").append("<![CDATA[").append("S").append("]]>").append("</line_type>\r\n");
						setNodeValue( dom, "line_type", "S" );
					}

					// }
				}


			}

			//valueXmlString.append("</Root>\r\n");	


		}//try end
		catch (SQLException sqx)
		{
			System.out.println("The SQLException occurs in getChangeSord for Default button :"+sqx);
			throw new ITMException(sqx);
		}
		catch (Exception e)
		{
			System.out.println("The Exception occure in getChangeSord  for Default button:"+e);
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
				if (rs1 != null)
				{
					rs1.close();
					rs1 = null;
				}
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if (pstmt1 != null)
				{
					pstmt1.close();
					pstmt1 = null;
				}				
			}
			catch(Exception e){}
		}
		/*finally
		{
			try
			{
				conn.close();
				conn = null;
			}
			catch (Exception e){}
		}*/
		System.out.println("Return getChangeSord currentColumn[" + currentColumn + "] valueXmlString [" + valueXmlString.toString() + "]");
		return valueXmlString.toString();
	}

	private static void setNodeValue( Document dom, String nodeName, String nodeVal ) throws Exception
	{
		Node tempNode = dom.getElementsByTagName( nodeName ).item(0);

		if( tempNode != null )
		{
			if( tempNode.getFirstChild() == null )
			{
				CDATASection cDataSection = dom.createCDATASection( nodeVal );
				tempNode.appendChild( cDataSection );
			}
			else
			{
				tempNode.getFirstChild().setNodeValue(nodeVal);
			}
		}
		tempNode = null;
	}	
	private static void setNodeValue( Document dom, String nodeName, double nodeVal ) throws Exception
	{
		setNodeValue( dom, nodeName, Double.toString( nodeVal ) );
	}
	private static void setNodeValue( Document dom, String nodeName, int nodeVal ) throws Exception
	{
		setNodeValue( dom, nodeName, Integer.toString( nodeVal ) );
	}

	public double getReqDecimal(double actVal, int prec)
	{
		String fmtStr = "############0";
		String strValue = null;
		if (prec > 0)
		{
			fmtStr = fmtStr + "." + "000000000".substring(0, prec);
		}
		DecimalFormat decFormat = new DecimalFormat(fmtStr);
		return Double.parseDouble(decFormat.format(actVal));
	}
	private String serializeDom(Node dom) throws Exception
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
			System.out.println("Exception : In : serializeDom :"+e);
			e.printStackTrace();

		}
		return retString;
	}
	public double calcRate(double rate,double plistDisc)
	{
		try
		{
			rate =  rate - (plistDisc * rate)/100;
			if( rate < 0 )
			{
				rate=0;
			}

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}


		return rate;
	}
	public double getDiscount(String plistDisc,Timestamp orderDate,String custCode,String siteCode,String itemCode,String unit,double discMerge,Timestamp plDate,double sordItmQty,Connection conn) throws SQLException, ITMException
	{
		String ls_listtype = "", itemSer = "",sql="";
		double lc_rate=0.0, lc_disc=0.0,rate=0.0,discPerc=0.0;
		PreparedStatement pstmt=null;
		ResultSet rs = null;
		try
		{
			if(plistDisc.trim().length() > 0)
			{

				sql = "    select case when rate is null then 0 else rate end as rate " +
						" from    pricelist where price_list    = ? and " +
						"    item_code     = ? and unit = ? " +
						" and    list_type IN  ('M','N') " +
						" and    case when min_qty is null then 0 else min_qty end     <=    ? " +
						" and    ((case when max_qty is null then 0 else max_qty end    >=    ? ) " +
						" OR  (case when max_qty is null then 0 else max_qty end    =0)) and eff_from <=    ?  " +
						" and    valid_upto >=    ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,plistDisc);
				pstmt.setString(2,itemCode);
				pstmt.setString(3,unit);
				pstmt.setDouble(4,sordItmQty);
				pstmt.setDouble(5,sordItmQty);
				pstmt.setTimestamp(6,plDate);
				pstmt.setTimestamp(7,plDate);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					rate = rs.getDouble("rate");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				System.out.println("Rate"+ rate);

			}
			System.out.println("List Type " +ls_listtype);
			System.out.println("Price LIst Discount " +plistDisc);
			System.out.println("Rate " +rate);
			System.out.println("itemCode " +itemCode);
			System.out.println("cust_code " +custCode);
			discPerc = rate;
			if("M".equalsIgnoreCase(ls_listtype) || plistDisc == null || plistDisc.trim().length() == 0 || rate == 0)
			{
				System.out.println("Pass All condition");
				
				sql = "select item_ser from item where item_code = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,itemCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					itemSer = rs.getString("item_ser");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				System.out.println("itemSer " +itemSer);
				sql = "select disc_perc from customer_series where cust_code = ? and item_ser = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,custCode);
				pstmt.setString(2,itemSer);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					discPerc = rs.getDouble("disc_perc");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				System.out.println("disc_perc " +discPerc);

				if(discPerc == 0)
				{
					System.out.println("Perc is"+discPerc);
					sql = "select disc_perc from site_customer where site_code = ? and cust_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,siteCode);
					pstmt.setString(2,custCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						discPerc = rs.getDouble("disc_perc");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
				if(discPerc == 0)
				{
					sql = "select disc_perc  from customer where cust_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,custCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						discPerc = rs.getDouble("disc_perc");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

				}
				if("M".equalsIgnoreCase(ls_listtype))
				{
					discMerge = discPerc;
					if(rate != 0)
					{
						discPerc = rate;    
					}
				}
				else
				{
					discMerge = 0;
				}


			}
			if(itemCode == null)
			{
				discPerc = 0;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage()); 
			throw new ITMException(e); 
		}finally
		{
			try
			{
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}				
			}
			catch(Exception e){}
		}


		return discPerc;
	}
	private String checkDoubleNull(String input) {
		if (input == null || input.trim().length() == 0 ) 
		{
		input = "0";
		}
		return input;
		}
	public Document buildDefaultDom() throws ITMException
	{
		Document dom=null;
		String xmlString="";
		try
		{
			xmlString="<DocumentRoot> <description>Datawindow Root</description> <group0> <description>Group0 description</description> <Header0> <description>Header0 members</description> <Detail2 dbID=\"\" domID=\"1\" objContext=\"2\" objName=\"despatch\"> <attribute pkNames=\"\" selected=\"N\" status=\"N\" updateFlag=\"A\" /> <desp_id><![CDATA[null]]></desp_id> <line_no/><sord_no isSrvCallOnChg=\"0\"/> <line_no__sord isSrvCallOnChg=\"0\"/> <exp_lev isSrvCallOnChg=\"0\"/> <item_code__ord isSrvCallOnChg=\"0\"/> <item_code isSrvCallOnChg=\"0\"/> <lot_no isSrvCallOnChg=\"0\"/> <lot_sl isSrvCallOnChg=\"0\"/> <quantity__ord isSrvCallOnChg=\"0\"/> <quantity isSrvCallOnChg=\"0\"/> <item_descr isSrvCallOnChg=\"0\"/> <loc_code isSrvCallOnChg=\"0\"/> <status/> <conv__qty_stduom isSrvCallOnChg=\"0\"/> <unit__std isSrvCallOnChg=\"0\"/> <unit isSrvCallOnChg=\"0\"/> <quantity__stduom isSrvCallOnChg=\"0\"/> <quantity_real isSrvCallOnChg=\"0\"/> <rate__stduom isSrvCallOnChg=\"0\"/> <tax_class isSrvCallOnChg=\"0\"/> <tax_chap isSrvCallOnChg=\"0\"/> <tax_env isSrvCallOnChg=\"0\"/> <discount isSrvCallOnChg=\"0\"/> <pack_instr isSrvCallOnChg=\"0\" /> <no_art isSrvCallOnChg=\"0\"/> <pending_qty isSrvCallOnChg=\"0\"/> <sorddet_rate /> <pack_qty isSrvCallOnChg=\"0\"/> <exp_date /> <site_code isSrvCallOnChg=\"0\"/> <mfg_date isSrvCallOnChg=\"0\"/> <chg_date /> <chg_user /> <chg_term /> <site_code__mfg isSrvCallOnChg=\"0\"/> <rate__clg isSrvCallOnChg=\"0\"/> <gross_weight isSrvCallOnChg=\"0\"/> <tare_weight isSrvCallOnChg=\"0\"/> <nett_weight isSrvCallOnChg=\"0\"/> <dimension isSrvCallOnChg=\"0\" /> <tax_amt/> <disc_amt isSrvCallOnChg=\"0\"/> <conf_diff_amt/> <rate__std /> <cost_rate /> <qty_details isSrvCallOnChg=\"0\"/> <line_type /> <tot_net_amt /> <conv__rtuom_stduom isSrvCallOnChg=\"0\"/> <pallet_wt isSrvCallOnChg=\"0\"/> <tran_id__invpack /> <cust_item__ref isSrvCallOnChg=\"0\"/> <custitem_desc isSrvCallOnChg=\"0\"/> <retest_date isSrvCallOnChg=\"0\" /> <part_no /> <disc_schem_billback_amt /> <disc_schem_offinv_amt /> <pallet_no /> </Detail2> </Header0> </group0> </DocumentRoot>";	
			dom = new  ibase.utility.E12GenericUtility().parseString(xmlString); 
			System.out.println("dom :"+dom);
			
		}
		catch(Exception e)
		{
			System.out.println(e);
			e.printStackTrace();
		}
		return dom;
		
	}



}