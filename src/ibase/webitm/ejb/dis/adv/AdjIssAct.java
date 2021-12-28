/* 
	Developed by : Niraja
	Company : Base Information Management Pvt. Ltd
	Version : 1.0
	Date :18/11/2005
*/

package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import java.lang.String;
import java.sql.*;
import org.w3c.dom.*;
import javax.ejb.*;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.utility.E12GenericUtility;
import ibase.system.config.*;
import ibase.webitm.ejb.ITMDBAccessEJB;
import java.text.SimpleDateFormat;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class AdjIssAct extends ActionHandlerEJB implements AdjIssActLocal, AdjIssActRemote
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

	public String actionHandler(String actionType, String xmlString,String xmlString1, String objContext, String xtraParams) throws RemoteException,ITMException
	{
		System.out.println("AdjIss called-----675123");
		Document dom = null;
		Document dom1 = null;
		String  resString = null;
		try
		{
			ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility(); 
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
			System.out.println("actionType:"+actionType);
			if (actionType.equalsIgnoreCase("Stock"))
			{
				resString = actionStock(dom,dom1,objContext,xtraParams);
			}
			if (actionType.equalsIgnoreCase("Damage"))
			{
				resString = actionDamage(dom,dom1,objContext,xtraParams);
			}
		}
	   	catch(Exception e)
		{
			System.out.println("Exception :AdjIss :actionHandler(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning from action[Stock] actionHandler"+resString);
	    return (resString);
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
			if (actionType.equalsIgnoreCase("Damage"))
			{
				retString = damageTransform(dom, dom1, objContext, xtraParams, selDataDom);
			}
		}
	   	catch(Exception e)
		{
			System.out.println("Exception :AdjIssAct :actionHandlerTransform(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning String from AdjIssAct : actionHandlerTransform"+retString);
	    return retString;
	}

	private String actionStock(Document dom,Document dom1, String objContext, String xtraParams) throws RemoteException , ITMException
	{
		Connection conn = null;
		//Statement stmt = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		//ResultSet rs1 = null;
		String sql = "";
		String errCode = "";
		String siteCode = "";
		String itemCode = "";
		String quantity = "";
		String locCode = "";
		String lotNo = "";
		String itemSer = "";
		String unit = "";
		String lotSl = "";
		java.sql.Date expDate = null;
		java.sql.Date retestDate = null;
		String dimension = "";
		String stkRetestDate = "";
		String stkExpDate = "";
		double rate = 0;
		double grossWeight=0.0,tareWeight=0.0,netWeight=0.0,noArt=0.0;
		int row = 0;
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		if (dom == null)
		{
			valueXmlString.append("</Root>\r\n");
			return valueXmlString.toString();
		}
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		//ConnDriver connDriver = new ConnDriver();
		try
		{		
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
		//	stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE); //Gulzar 18/03/07 - As Not working in DB2 and gives Exception like - sensitive cursor cannot be defined for the specified select statement
			//stmt = conn.createStatement(); //Gulzar 18/03/07 
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			
			siteCode = genericUtility.getColumnValue("site_code",dom1);
			itemCode = genericUtility.getColumnValue("item_code",dom);
			quantity = genericUtility.getColumnValue("quantity",dom);
			locCode = genericUtility.getColumnValue("loc_code",dom);
			lotNo = genericUtility.getColumnValue("lot_no",dom);
			
			System.out.println("FRom DOM:siteCode:"+siteCode+":itemCode:"+itemCode+":quantity:"+quantity+":");
			System.out.println("FRom DOM:lotNo:"+lotNo+":locCode:"+locCode+":");
			
			//sql=" SELECT COUNT(A.ITEM_CODE) FROM STOCK A, INVSTAT B "+
			sql=" SELECT COUNT(*) FROM STOCK A, INVSTAT B "+
				" WHERE A.INV_STAT  = B.INV_STAT AND A.ITEM_CODE = ? " +//'"+itemCode+"'"+
				" AND A.SITE_CODE = ? "	+//'"+siteCode+"'"+
				" AND (CASE WHEN ((CASE WHEN A.QUANTITY IS NULL THEN 0 ELSE A.QUANTITY END) - (CASE WHEN A.ALLOC_QTY IS NULL THEN 0 ELSE A.ALLOC_QTY END)) "+
				" IS NULL THEN 0 ELSE ((CASE WHEN A.QUANTITY IS NULL THEN 0 ELSE A.QUANTITY END) - (CASE WHEN A.ALLOC_QTY IS NULL THEN 0 ELSE A.ALLOC_QTY END)) END )  > 0 ";
			//System.out.println("sql:"+sql);
			//rs = stmt.executeQuery(sql);
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, itemCode);
			pstmt.setString(2, siteCode);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				row = rs.getInt(1);
			}
			rs.close(); rs = null;
			pstmt.close(); pstmt = null;
			System.out.println("row:"+row);
			if(row > 0)
			{
				//errCode = "VTSTOCK1";
				if(lotNo == null)
				{
					lotNo = "";
				}
				if(locCode == null)
				{
					locCode = "";
				}
				if(quantity == null)
				{
					quantity = "0";
				}
				sql = "SELECT STOCK.ITEM_SER,STOCK.ITEM_CODE, " + 
						" STOCK.UNIT,STOCK.LOC_CODE,STOCK.LOT_NO, " + 
						" STOCK.LOT_SL,STOCK.QUANTITY - STOCK.ALLOC_QTY AS QUANTITY, " + 
						" STOCK.SITE_CODE,STOCK.EXP_DATE,STOCK.RETEST_DATE, " +
						" STOCK.DIMENSION,STOCK.RATE,STOCK.GROSS_WEIGHT,STOCK.TARE_WEIGHT,STOCK.NET_WEIGHT,STOCK.NO_ART "+
						" FROM STOCK,INVSTAT " + 
						" WHERE ( STOCK.INV_STAT = INVSTAT.INV_STAT ) AND " + 
						"( STOCK.ITEM_CODE = '" +itemCode + "' ) AND " + 
						"( STOCK.SITE_CODE = '" +siteCode + "' ) AND " + 
						"((CASE WHEN INVSTAT.STAT_TYPE IS NULL THEN ' ' ELSE INVSTAT.STAT_TYPE END) <> 'S') AND ";
			
				if(locCode.trim().length() > 0)
				{
					sql = sql + " ( STOCK.LOC_CODE = '"+locCode+"' ) AND ";
				}
				if(lotNo.trim().length() > 0)
				{
					sql = sql + " ( STOCK.LOT_NO = '" +lotNo + "' ) AND " ;
				}
				sql = sql + "( STOCK.QUANTITY - STOCK.ALLOC_QTY > 0 ) " + 
					 " ORDER BY (CASE WHEN STOCK.EXP_DATE IS NULL THEN STOCK.CREA_DATE ELSE STOCK.EXP_DATE end) ASC, STOCK.LOT_NO ASC, " + 
					 " STOCK.LOT_SL ASC ";
				//System.out.println("AdjIss:actionStock:sql:"+sql);
				//rs = stmt.executeQuery(sql);
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				while(rs.next())
				{
					itemSer = rs.getString(1);
					itemCode = rs.getString(2);
					unit = rs.getString(3);
					locCode = rs.getString(4);
					lotNo =( rs.getString(5) ==null ?"" :rs.getString(5));
					lotSl = rs.getString(6);
					quantity = rs.getString(7);
					siteCode = rs.getString(8);
					expDate = rs.getDate(9) ;
					retestDate = rs.getDate(10);
					dimension =( rs.getString(11) ==null ?" ": rs.getString(11));
					rate = rs.getDouble(12);
					grossWeight = rs.getDouble(13);
					tareWeight = rs.getDouble(14);
					netWeight = rs.getDouble(15);
					noArt = rs.getDouble(16);
					
					System.out.println("lotNo......................."+lotNo);
					valueXmlString.append("<Detail>\r\n");
					//added by rajendra on 19/06/07
					if(lotNo.trim().length() ==0)
					{
						lotNo =" ";
					}
					if(locCode.trim().length() ==0)
					{
						locCode =" ";
					}
					if(lotSl.trim().length() ==0)
					{
						lotSl =" ";
					}
					if(unit.trim().length() ==0)
					{
						unit =" ";
					}
					if(siteCode.trim().length() ==0)
					{
						siteCode =" ";
					}
					if(itemSer.trim().length() ==0)
					{
						siteCode =" ";
					}
					valueXmlString.append("<item_ser>").append("<![CDATA[").append(itemSer == null?"":itemSer.trim()).append("]]>").append("</item_ser>\r\n");
					valueXmlString.append("<item_code>").append("<![CDATA[").append(itemCode == null?"":itemCode.trim()).append("]]>").append("</item_code>\r\n");
					valueXmlString.append("<unit>").append("<![CDATA[").append(unit == null?"":unit.trim()).append("]]>").append("</unit>\r\n");
					valueXmlString.append("<loc_code>").append("<![CDATA[").append(locCode == null?"":locCode.trim()).append("]]>").append("</loc_code>\r\n");
					valueXmlString.append("<lot_no>").append("<![CDATA[").append(lotNo ==null ?"               ":lotNo).append("]]>").append("</lot_no>\r\n");
					valueXmlString.append("<lot_sl>").append("<![CDATA[").append(lotSl ==null ?"               ":lotSl).append("]]>").append("</lot_sl>\r\n");
					valueXmlString.append("<quantity>").append("<![CDATA[").append(quantity).append("]]>").append("</quantity>\r\n");
					valueXmlString.append("<site_code>").append("<![CDATA[").append(siteCode).append("]]>").append("</site_code>\r\n");
					
					if(expDate != null)
					{
						stkExpDate = sdf.format(expDate);
					}
					System.out.println("stkExpDate....:"+stkExpDate);
					if(stkExpDate.trim().length() ==0)
					{
					  stkExpDate =" ";
					}
					if(retestDate != null)					 //added by rajendra on 19/06/07
					{
						stkRetestDate = sdf.format(retestDate);
					}
					System.out.println("stkRetestDate....:"+stkRetestDate);
					
					if(stkRetestDate.trim().length() ==0)
					{
					  stkRetestDate =" ";
					}
					if(dimension.trim().length() ==0)
					{
						dimension =" ";
					}
					valueXmlString.append("<rate>").append("<![CDATA[").append(rate).append("]]>").append("</rate>\r\n");
					valueXmlString.append("<exp_date>").append("<![CDATA[").append(stkExpDate).append("]]>").append("</exp_date>\r\n");
					valueXmlString.append("<retest_date>").append("<![CDATA[").append(stkRetestDate).append("]]>").append("</retest_date>\r\n");
					valueXmlString.append("<dimension>").append("<![CDATA[").append(dimension).append("]]>").append("</dimension>\r\n");
					
					valueXmlString.append("<gross_weight>").append("<![CDATA[").append(grossWeight).append("]]>").append("</gross_weight>\r\n");
					valueXmlString.append("<tare_weight>").append("<![CDATA[").append(tareWeight).append("]]>").append("</tare_weight>\r\n");
					valueXmlString.append("<net_weight>").append("<![CDATA[").append(netWeight).append("]]>").append("</net_weight>\r\n");
					valueXmlString.append("<no_art>").append("<![CDATA[").append(noArt).append("]]>").append("</no_art>\r\n");
					
					valueXmlString.append("</Detail>\r\n");
				}//end while
				rs.close(); rs = null;
				pstmt.close(); pstmt = null;
			}
			valueXmlString.append("</Root>\r\n");
		}
		catch(SQLException e)
		{
			System.out.println("Exception : AdjIss : actionStock " +e.getMessage());
			throw new ITMException(e);
		}
		catch(Exception e)
		{
			System.out.println("Exception : AdjIss : actionHandler :(Document dom)" +e.getMessage());
			throw new ITMException(e);
		}
		finally
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
		System.out.println("valueXmlString.toString() "+valueXmlString.toString());
		return valueXmlString.toString();		
	}

	private String stockTransform(Document dom,Document dom1, String objContext, String xtraParams, Document selDataDom)throws ITMException
	{
		String tranId = "",siteCode = "",itemCode = "",locCode = "",lotNo = "",lotSl = "";
		String quantity = "",sundryType = "",sundryCode = "",grossRate = "",grade = "",noArt = "";
		String amount = "",dimension = "",grossWt = "",tareWt = "",netWt = "";
		String acctCodeCr = "",acctCodeDr = "",cctrCodeCr = "",cctrCodeDr = "", userId ="";
		String issCriteria = "",stkQtyStr = "",sql = "",errCode = "", errString = "";
		String selLocCode = "",selLotNo = "",selLotSl = "",selUnit = "",selQuantity = "",selStkRate = "0";
		String selGrossWeight="0",selTareWeight="0",selNetWeight="0",selNoArt="0";
		double stkQty = 0,ordQuantity = 0,selQty = 0;
		NodeList detailList = null;
		int detailListLength = 0;
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		ConnDriver connDriver = new ConnDriver();
		ITMDBAccessEJB itmDbAccess = new ITMDBAccessEJB();   //Added by Alka on 31/05/2007
		Connection conn = null;
		//Statement stmt = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			//stmt = conn.createStatement();
			userId = new  ibase.utility.E12GenericUtility().getValueFromXTRA_PARAMS(xtraParams, "loginCode");//Added by Alka on 31/05/2007

			tranId = new  ibase.utility.E12GenericUtility().getColumnValue("tran_id",dom1);
			siteCode = new  ibase.utility.E12GenericUtility().getColumnValue("site_code",dom1);
			itemCode = new  ibase.utility.E12GenericUtility().getColumnValue("item_code",dom);
			locCode = new  ibase.utility.E12GenericUtility().getColumnValue("loc_code",dom);
			lotNo = new  ibase.utility.E12GenericUtility().getColumnValue("lot_no",dom);
			lotSl = new  ibase.utility.E12GenericUtility().getColumnValue("lot_sl",dom);
			quantity = new  ibase.utility.E12GenericUtility().getColumnValue("quantity",dom);
			sundryType = new  ibase.utility.E12GenericUtility().getColumnValue("sundry_type",dom);
			sundryCode = new  ibase.utility.E12GenericUtility().getColumnValue("sundry_code",dom);
			grossRate = new  ibase.utility.E12GenericUtility().getColumnValue("gross_rate",dom);
			grade = new  ibase.utility.E12GenericUtility().getColumnValue("grade",dom);
			noArt = new  ibase.utility.E12GenericUtility().getColumnValue("no_art",dom);
			amount = new  ibase.utility.E12GenericUtility().getColumnValue("amount",dom);
			dimension = new  ibase.utility.E12GenericUtility().getColumnValue("dimension",dom);
			dimension =(dimension == null ? "":dimension);		 //add by rajendra
			grossWt = new  ibase.utility.E12GenericUtility().getColumnValue("gross_weight",dom);
			tareWt = new  ibase.utility.E12GenericUtility().getColumnValue("tare_weight",dom);
			netWt = new  ibase.utility.E12GenericUtility().getColumnValue("net_weight",dom);
			acctCodeCr = new  ibase.utility.E12GenericUtility().getColumnValue("acct_code__cr",dom);
			acctCodeDr = new  ibase.utility.E12GenericUtility().getColumnValue("acct_code__dr",dom);
			cctrCodeCr = new  ibase.utility.E12GenericUtility().getColumnValue("cctr_code__cr",dom);
			cctrCodeDr = new  ibase.utility.E12GenericUtility().getColumnValue("cctr_code__dr",dom);
			
			System.out.println("grossWt>>["+grossWt+"]tareWt>>["+tareWt+"]netWt>>["+netWt+"]noArt>>["+noArt+"]");
			//Added by Alka 31/05/07 -- Entering Required Quantity in the Screen is necessary
			if (Double.parseDouble(quantity) == 0)
			{
				errCode = "VTQUAN";
				errString = itmDbAccess.getErrorString("quantity", errCode, userId, "", conn);
				return errString;
			}
			// end Added by Alka 31/05/07

			sql = "SELECT ISS_CRITERIA FROM ITEM WHERE ITEM_CODE = ?"; //'"+itemCode+"'";
			//rs = stmt.executeQuery(sql);
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, itemCode);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				issCriteria = rs.getString("ISS_CRITERIA");
			}
			rs.close(); rs = null;
			pstmt.close(); pstmt = null;
			detailList = selDataDom.getElementsByTagName("Detail");
			detailListLength = detailList.getLength();
			for (int i = 0;i < detailListLength;i++ )
			{
				stkQtyStr = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("quantity",detailList.item(i));
				if (stkQtyStr != null)
				{
					stkQty = stkQty + Double.parseDouble(stkQtyStr);
				}				
			}
			if (stkQty < Double.parseDouble(quantity))
			{
				errCode = "VTSTOCK1";
				errString = itmDbAccess.getErrorString("quantity", errCode, userId, "", conn); //Added by Alka 31/05/07
				return errString;  //Added by Alka 31/05/07
			}
			ordQuantity = Double.parseDouble(quantity);
			
			System.out.println("issCriteria ::"+issCriteria);
			System.out.println("ordQuantity ::"+ordQuantity);

			for (int ctr = 0;ctr < detailListLength;ctr++)
			{
				selLocCode = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("loc_code",detailList.item(ctr));
				selLotNo = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("lot_no",detailList.item(ctr));
				selLotSl = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("lot_sl",detailList.item(ctr));
				selUnit = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("unit",detailList.item(ctr));
				selQuantity = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("quantity",detailList.item(ctr));
			//	selStkRate = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("stock_rate",detailList.item(ctr)); //stock-rate is wrongly placed Gulzar - 10/05/07
				selStkRate = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("rate",detailList.item(ctr)); //stock-rate is changed to rate Gulzar - 10/05/07
				
				selGrossWeight = checkDouble(new  ibase.utility.E12GenericUtility().getColumnValueFromNode("gross_weight",detailList.item(ctr)));
				selTareWeight = checkDouble(new  ibase.utility.E12GenericUtility().getColumnValueFromNode("tare_weight",detailList.item(ctr)));
				selNetWeight = checkDouble(new  ibase.utility.E12GenericUtility().getColumnValueFromNode("net_weight",detailList.item(ctr)));
				selNoArt = checkDouble(new  ibase.utility.E12GenericUtility().getColumnValueFromNode("no_art",detailList.item(ctr)));
				System.out.println("selGrossWeight>>["+selGrossWeight+"]selTareWeight>>["+selTareWeight+"]selNetWeight>>["+selNetWeight+"]selNoArt>>["+selNoArt+"]");
				if (selStkRate == null)
				{
					selStkRate = "0";
				}
				valueXmlString.append("<Detail>");
				valueXmlString.append("<item_code isSrvCallOnChg='1'>").append(itemCode).append("</item_code>");
				if (selUnit != null && selUnit.trim().length() > 0)
				{
					valueXmlString.append("<unit>").append(selUnit).append("</unit>");
				}
				if (selLocCode != null && selLocCode.trim().length() > 0)
				{
					valueXmlString.append("<loc_code isSrvCallOnChg='1'>").append(selLocCode).append("</loc_code>");
				}
				//Commented by mayur on 30-July-2018---start
				//if (selLotNo != null && selLotNo.trim().length() > 0)
				//{
					valueXmlString.append("<lot_no isSrvCallOnChg='1'>").append(selLotNo).append("</lot_no>");
				//}
				//if (selLotSl != null && selLotSl.trim().length() > 0)
				//{
					valueXmlString.append("<lot_sl isSrvCallOnChg='1'>").append(selLotSl).append("</lot_sl>");
				//}
					//Commented by mayur on 30-July-2018---end
				if (sundryType != null && sundryType.trim().length() > 0)
				{
					valueXmlString.append("<sundry_type>").append(sundryType).append("</sundry_type>");
				}

				//valueXmlString.append("<sundry_code>").append(sundryCode).append("</sundry_code>"); //Gulzar 28/03/07 - Checking fo null values
				valueXmlString.append("<sundry_code>").append((sundryCode == null) ? "":sundryCode).append("</sundry_code>"); //Gulzar 28/03/07 - Checking fo null values
				valueXmlString.append("<rate isSrvCallOnChg='1'>").append(selStkRate).append("</rate>");
				//valueXmlString.append("<grade>").append(grade).append("</grade>"); //Gulzar 28/03/07 - gives IMPORTERR if grade == null and has no itemchange in nvo thus it replaces the grade value by null
				valueXmlString.append("<amount>").append(amount).append("</amount>");
				selQty = Double.parseDouble(selQuantity);
				System.out.println("selQty ::"+selQty);
				if (selQty < ordQuantity)
				{
					valueXmlString.append("<quantity>").append(selQty).append("</quantity>");
					ordQuantity = ordQuantity - selQty;
				}
				else
				{
					if (issCriteria != null && issCriteria.equalsIgnoreCase("W"))
					{
						valueXmlString.append("<quantity>").append(selQty).append("</quantity>");
						valueXmlString.append("<amount>").append(selQty * Double.parseDouble(selStkRate)).append("</amount>");
					}
					else
					{
/*					changed on 31/05/2007 by Alka as ordQuantity should always be entered and 
 * 					this condition will never be reached , hence commented
						if (ordQuantity == 0)
						{
							ordQuantity = selQty;
						}
					change by Alka ended 31/05/07
*/
						valueXmlString.append("<quantity>").append(ordQuantity).append("</quantity>");
						valueXmlString.append("<amount>").append(ordQuantity * Double.parseDouble(selStkRate)).append("</amount>");
						ordQuantity = 0;  // added by Alka on 31/05/2007 to exit the loop
					}	
				}
				//valueXmlString.append("<amount>").append(ordQuantity * Double.parseDouble(selStkRate)).append("</amount>");change by chandrashekar on 20-may-2016
				if (noArt != null && noArt.trim().length() > 0 && Double.parseDouble(noArt)>0)
				{
					valueXmlString.append("<no_art>").append(noArt).append("</no_art>");
				}
				else
				{
					valueXmlString.append("<no_art>").append(selNoArt).append("</no_art>");
				}
				if (grossWt != null && grossWt.trim().length() > 0 && Double.parseDouble(grossWt)>0)
				{
					valueXmlString.append("<gross_weight>").append(grossWt).append("</gross_weight>");
				}else
				{
					valueXmlString.append("<gross_weight>").append(selGrossWeight).append("</gross_weight>");
				}
				if (tareWt != null && tareWt.trim().length() > 0 &&  Double.parseDouble(tareWt)>0)
				{
					valueXmlString.append("<tare_weight>").append(tareWt).append("</tare_weight>");
				}else
				{
					valueXmlString.append("<tare_weight>").append(selTareWeight).append("</tare_weight>");
				}
				if (netWt != null && netWt.trim().length() > 0 &&  Double.parseDouble(netWt)>0)
				{
						valueXmlString.append("<net_weight>").append(netWt).append("</net_weight>");
				}else
				{
					valueXmlString.append("<net_weight>").append(selNetWeight).append("</net_weight>");
				}
				valueXmlString.append("</Detail>");
				if (ordQuantity == 0)
				{
					break;
				}
			}
			valueXmlString.append("</Root>\r\n");				
		}
		catch (Exception e)
		{
			System.out.println("Exception AdjIssAct "+e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(rs != null)
				{
					rs.close(); rs = null;
				}
				if(pstmt != null)
				{
					pstmt.close(); pstmt = null;
				}
				if(conn != null)
				{
					conn.close();
					conn = null;
				}
			}
			catch (Exception se){}
		}
		return valueXmlString.toString();
	}


		private String actionDamage(Document dom,Document dom1, String objContext, String xtraParams) throws RemoteException , ITMException
	{
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		String siteCode = "";
		String locDescr = "";
		String locCode = "";
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		if (dom == null)
		{
			valueXmlString.append("</Root>\r\n");
			return valueXmlString.toString();
		}
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		//ConnDriver connDriver = new ConnDriver();
		try
		{		
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			siteCode = genericUtility.getColumnValue("site_code",dom1);
			
			System.out.println("From DOM:siteCode:"+siteCode);
			
				sql = " SELECT LOC_CODE, DESCR FROM LOCATION";
				
				//System.out.println("AdjIssDamage:actionStock:sql:"+sql);
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				while(rs.next())
				{
					locCode = rs.getString(1);
					locDescr = rs.getString(2);
					valueXmlString.append("<Detail>\r\n");
					valueXmlString.append("<loc_code>").append("<![CDATA[").append(locCode.trim()).append("]]>").append("</loc_code>\r\n");
					valueXmlString.append("<loc_descr>").append("<![CDATA[").append(locDescr.trim()).append("]]>").append("</loc_descr>\r\n");
					valueXmlString.append("</Detail>\r\n");
				}//end while
				rs.close(); rs = null;
				pstmt.close(); pstmt = null;
			valueXmlString.append("</Root>\r\n");
		}
		catch(SQLException e)
		{
			System.out.println("Exception : AdjIssDamage : actionStock " +e.getMessage());
			throw new ITMException(e);
		}
		catch(Exception e)
		{
			System.out.println("Exception : AdjIssDamage : actionHandler :(Document dom)" +e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try{
				if(rs != null) {
					rs.close(); 
					rs = null;	
				}
				if(pstmt != null) {
					pstmt.close(); 
					pstmt = null;	
				}
				if(conn != null) {
					conn.close();
					conn = null;	
				}
				
			}catch(Exception e){}
		}
		System.out.println("valueXmlString.toString() "+valueXmlString.toString());
		return valueXmlString.toString();		
	}

	private String damageTransform(Document dom,Document dom1, String objContext, String xtraParams, Document selDataDom)throws ITMException
	{
		String siteCode = "",sql = "", selLocCode = "", selLocDescr = "", udfStr = "", reasCode = null;
		NodeList detailList = null;
		int detailListLength = 0;
		String stkMfgDate = null, stkExpDate = null, acctCodeDr = null, cctrCodeDr = null;
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		ConnDriver connDriver = new ConnDriver();
		Connection conn = null;
		PreparedStatement pstmt = null, pstmt1 = null;
		ResultSet rs = null, rs1 = null;
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			
			siteCode = genericUtility.getColumnValue("site_code",dom1);
			reasCode = genericUtility.getColumnValue("reas_code",dom1);
			detailList = selDataDom.getElementsByTagName("Detail");
			detailListLength = detailList.getLength();

			for (int ctr = 0;ctr < detailListLength;ctr++)
			{
				selLocCode = genericUtility.getColumnValueFromNode("loc_code",detailList.item(ctr));
				selLocDescr = genericUtility.getColumnValueFromNode("loc_descr",detailList.item(ctr));
				
				sql = " SELECT A.ITEM_CODE, B.DESCR, A.UNIT, A.LOT_NO,  A.LOT_SL,"
					+ " (CASE WHEN A.QUANTITY IS NULL THEN 0 ELSE A.QUANTITY END) - "
					+ " (CASE WHEN A.ALLOC_QTY IS NULL THEN 0 ELSE A.ALLOC_QTY END) AS QUANTITY,  "
					+ " A.MFG_DATE,A.EXP_DATE, A.DIMENSION,A.RATE "
					+ " FROM STOCK A, ITEM B  WHERE "
					+ " (A.ITEM_CODE = B.ITEM_CODE) AND (A.SITE_CODE = ?) AND (A.LOC_CODE = ?) AND "
					+ " ((CASE WHEN A.QUANTITY IS NULL THEN 0 ELSE A.QUANTITY END) - "
					+ " (CASE WHEN A.ALLOC_QTY IS NULL THEN 0 ELSE A.ALLOC_QTY END) > 0) " 
					+ " ORDER BY (CASE WHEN A.EXP_DATE IS NULL THEN A.CREA_DATE ELSE A.EXP_DATE END) ASC, "
					+ " A.LOT_NO ASC, A.LOT_SL ASC";
				//System.out.println("sql ::: "+ sql);
				System.out.println("Setting siteCode ::: "+ siteCode +" -- selLocCode :: " + selLocCode);
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, siteCode);
				pstmt.setString(2, selLocCode);
				rs = pstmt.executeQuery();
				while (rs.next())
				{
					if(rs.getDate(7) != null)
					{
						stkMfgDate = sdf.format(rs.getDate(7));
					}
					if(rs.getDate(8) != null)
					{
						stkExpDate = sdf.format(rs.getDate(8));
					}
					valueXmlString.append("<Detail>");
					valueXmlString.append("<item_code>").append(rs.getString(1)).append("</item_code>");
					valueXmlString.append("<item_descr>").append(rs.getString(2)).append("</item_descr>");
					valueXmlString.append("<unit>").append(rs.getString(3)).append("</unit>");
					valueXmlString.append("<loc_code>").append(selLocCode).append("</loc_code>");
					valueXmlString.append("<loc_descr>").append(selLocDescr).append("</loc_descr>");
					valueXmlString.append("<lot_no>").append(rs.getString(4)).append("</lot_no>");
					valueXmlString.append("<lot_sl>").append(rs.getString(5)).append("</lot_sl>");
					valueXmlString.append("<quantity>").append(rs.getString(6)).append("</quantity>");
					valueXmlString.append("<mfg_date>").append(stkMfgDate==null?"":stkMfgDate).append("</mfg_date>");
					valueXmlString.append("<exp_date>").append(stkExpDate==null?"":stkExpDate).append("</exp_date>");
					valueXmlString.append("<dimension>").append(rs.getString(9)==null?"":rs.getString(9)).append("</dimension>");
					valueXmlString.append("<rate>").append(rs.getString(10)).append("</rate>");
					
					sql = "SELECT UDF_STR1 FROM GENCODES WHERE FLD_NAME = 'REAS_CODE' AND (MOD_NAME = 'W_ADJ_ISS' OR MOD_NAME = 'X')"
						+ " AND RTRIM(FLD_VALUE) = ?";
					pstmt1 = conn.prepareStatement(sql);
					//System.out.println("sql ::: " + sql);
					System.out.println("reasCode ::: "+ reasCode);
					pstmt1.setString(1, reasCode);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						udfStr = rs1.getString(1);
					}
					else
					{
						udfStr = null;
						acctCodeDr = "";
						cctrCodeDr = "";
					}
					rs1.close(); rs1 = null;
					pstmt1.close(); pstmt1 = null;
					if (udfStr != null  && udfStr.trim().length() > 0)
					{
						acctCodeDr = udfStr.substring(0,udfStr.indexOf(","));
						cctrCodeDr = udfStr.substring(udfStr.indexOf(",")+1);
					}
					valueXmlString.append("<acct_code__dr>").append(acctCodeDr).append("</acct_code__dr>");
					valueXmlString.append("<cctr_code__dr>").append(cctrCodeDr).append("</cctr_code__dr>");
					
					valueXmlString.append("</Detail>");
				}
				rs.close(); rs = null;
				pstmt.close(); pstmt = null;
			}
			valueXmlString.append("</Root>\r\n");				
		}
		catch (Exception e)
		{
			System.out.println("Exception AdjIssAct "+e.getMessage());
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
				if(pstmt != null) {
					pstmt.close(); pstmt = null;
				}
				if(rs1 != null) {
					rs1.close(); rs1 = null;
				}
				if(pstmt1 != null) {
					pstmt1.close(); pstmt1 = null;
				}
				if(conn != null) {
					conn.close();
					conn = null;
				}
			}
			catch (Exception se){}
		}
		return valueXmlString.toString();
	}
	private String checkDouble(String input)	
	{
		if (input == null || input.trim().length() == 0)
		{
			input="0";
		}
		return input;
	}

}