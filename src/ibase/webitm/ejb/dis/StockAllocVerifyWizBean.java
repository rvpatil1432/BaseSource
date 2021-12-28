/**
 * @author : PAVAN RANE
 * @Version : 1.0
 * Date : 01/07/18
 * Request Id: F18BSHL001
 */
package ibase.webitm.ejb.dis;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import javax.naming.*;

//import com.jcraft.jsch.UserInfo;

import ibase.system.config.ConnDriver;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.*;
import ibase.webitm.utility.*;

public class StockAllocVerifyWizBean extends ValidatorEJB {

	E12GenericUtility genericUtility = new E12GenericUtility();
	InitialContext ctx1 = null;
	private String objName = "";
	private String user_lang = "en";
	private String user_country = "US";

	public StockAllocVerifyWizBean() {
	}

	public StockAllocVerifyWizBean(String objName) throws ITMException {
		try {
			this.objName = objName;

		} catch (Exception e) {
			e.printStackTrace();
			throw new ITMException(e);
		}

	}

	public String previousForm(String formNo, String xmlData)
			throws ITMException {
		String retHtmlData = null;

		try {
			System.out.println("In Method : [previousForm]");

			System.out.println("xmlString : [" + xmlData + "]");

			String xslFileName = getXSLFileName(this.objName + formNo + "_wiz_"
					+ this.user_lang + "_" + this.user_country + "_" + "A"
					+ ".xsl");

			retHtmlData = (genericUtility).transformToString(xslFileName,
					xmlData, CommonConstants.APPLICATION_CONTEXT
							+ File.separator + "temp", "Output", ".html");

		} catch (Exception e) {
			e.printStackTrace();
			throw new ITMException(e);
		}

		return retHtmlData;
	}

	public String getAllocDetail(String paramData, UserInfoBean userInfo) throws ITMException {

		System.out.println("----------------:: StockAllocVerifyWizBean ::----------------");
		System.out.println("--------------------::[ getAllocDetail ]::----------------");
		String retHtmlData = null;
		Connection conn = null;
		String sql = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String itemCode = "";
		String siteCode = "";
		String locCode = "";
		String lotNo = "";
		String lotSl = "";
		String xmlData = "";
		int formNo = 1;
		ConnDriver connDriver = null;
		int row = 0;
		SimpleDateFormat sdf = null;
		try {
			System.out.println("inside bean--" + userInfo);
			String transDB = userInfo.getTransDB();
			System.out.println("get TransDB connection in ConsumpIssueConfWF : "+ transDB);
			connDriver = new ConnDriver();
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			if (transDB != null && transDB.trim().length() > 0) {
				conn = connDriver.getConnectDB(transDB);
			} else {
				conn = connDriver.getConnectDB("Driver");
			}
			System.out.println("insided bean conn:" + conn);
			System.out.println("In Method : [previousForm]");
			System.out.println("xmlString : [" + paramData + "]");
			StringBuffer xmlString = new StringBuffer("<DocumentRoot>\r\n<description>Datawindow Root</description>\r\n<group0>\r\n<description>Group0 description</description>\r\n<Header0>\r\n<description>Header0 members</description>\r\n");
			// {$site_code}~{$item_code}~{$lot_no}~{$loc_code}~{$lot_sl}
			String[] paramArr = paramData.split("~");
			siteCode = paramArr[0];
			itemCode = paramArr[1];
			lotNo = paramArr[2];
			locCode = paramArr[3];
			lotSl = paramArr[4];
			System.out.println(paramArr[0]+":"+paramArr[1]+":"+paramArr[2]+":"+paramArr[3]+":"+paramArr[4]);

			//sql = "SELECT ITEM_CODE, LOC_CODE, LOT_NO, LOT_SL,REF_ID, REF_LINE, REF_SER, TRAN_DATE, CHG_DATE, ALLOC_QTY"
			sql = "SELECT REF_ID, REF_LINE, REF_SER, TRAN_DATE, CHG_DATE, ALLOC_QTY"
					+ " FROM INVALLOC_TRACE"
					+ " WHERE SITE_CODE = ?"
					+ " AND ITEM_CODE = ?"
					+ " AND LOC_CODE = ?"
					+ " AND LOT_NO = ?"
					+ " AND LOT_SL = ?"
					+ " ORDER BY SITE_CODE, ITEM_CODE, LOC_CODE, LOT_NO, LOT_SL";
			System.out.println("GetTraceStock @> Sql::[" + sql + "]");
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, siteCode);
			pstmt.setString(2, itemCode);
			pstmt.setString(3, locCode);
			pstmt.setString(4, lotNo);
			pstmt.setString(5, lotSl);
			rs = pstmt.executeQuery();
			while (rs.next()) {	
								
				row++;
				xmlString.append("<Detail2 dbID='' domID='" + row+ "' selected='" + "Y" + "'>\r\n");
				xmlString.append("<attribute pkNames='' status='" + "N"	+ "' updateFlag='" + "A" + "' selected='" + "Y"	+ "' />\r\n");
				
				xmlString.append("<site_code><![CDATA[").append(siteCode).append("]]></site_code>");
				xmlString.append("<item_code><![CDATA[").append(itemCode).append("]]></item_code>");				
				xmlString.append("<loc_code><![CDATA[").append(locCode).append("]]></loc_code>");
				xmlString.append("<lot_no><![CDATA[").append(lotNo).append("]]></lot_no>");
				xmlString.append("<lot_sl><![CDATA[").append(lotSl).append("]]></lot_sl>");
				xmlString.append("<ref_id><![CDATA[").append(rs.getString("REF_ID")).append("]]></ref_id>");
				xmlString.append("<ref_line><![CDATA[").append(rs.getString("REF_LINE")).append("]]></ref_line>");
				xmlString.append("<ref_ser><![CDATA[").append(rs.getString("REF_SER")).append("]]></ref_ser>");
				xmlString.append("<alloc_qty_trc><![CDATA[").append(rs.getDouble("ALLOC_QTY")).append("]]></alloc_qty_trc>");
				xmlString.append("<tran_date><![CDATA[").append(sdf.format(rs.getTimestamp("TRAN_DATE"))).append("]]></tran_date>");
				xmlString.append("<chg_date><![CDATA[").append(sdf.format(rs.getTimestamp("CHG_DATE"))).append("]]></chg_date>");

				xmlString.append("</Detail2>\r\n");

			}
			pstmt.close();
			rs.close();
			pstmt = null;
			rs = null;

			xmlString.append("</Header0>\r\n");
			xmlString.append("</group0>\r\n");
			xmlString.append("</DocumentRoot>\r\n");
			System.out.println("Pavan R 2018[" + xmlString + "]End");
			String xslFileName = getXSLFileName("stock_alloc_verify_AllocDet"+ formNo + "_wiz_" + this.user_lang + "_"+ this.user_country + "_" + "A" + ".xsl");

			retHtmlData = (genericUtility).transformToString(xslFileName,xmlString.toString(), CommonConstants.APPLICATION_CONTEXT+ File.separator + "temp", "Output", ".html");

		} 
		catch (SQLException s) {
			System.out.println("StockAllocVerifyWizBean: getAllocDetail::" + s.getMessage());			
			s.printStackTrace();
			throw new ITMException(s);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ITMException(e);
		}finally{
			try
			{
				if (conn != null)
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
					conn.close();
					conn = null;
				}				
			} catch (Exception e)
			{	
				e.printStackTrace();
			}
		}

		return retHtmlData;
	}
	public String getPendingDetail(String paramData, UserInfoBean userInfo) throws ITMException {

		System.out.println("----------------:: StockAllocVerifyWizBean ::----------------");
		System.out.println("--------------------::[ getPendingDetail ]::----------------");
		String retHtmlData = null;
		Connection conn = null;
		String sql = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String itemCode = "";
		String siteCode = "";
		String locCode = "";
		String lotNo = "";
		String lotSl = "";
		String xmlData = "";
		int formNo = 2;
		ConnDriver connDriver = null;
		int row = 0;
		SimpleDateFormat sdf = null;
		try {
			System.out.println("inside bean--" + userInfo);
			String transDB = userInfo.getTransDB();
			System.out.println("get TransDB connection in ConsumpIssueConfWF : "+ transDB);
			connDriver = new ConnDriver();
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			if (transDB != null && transDB.trim().length() > 0) {
				conn = connDriver.getConnectDB(transDB);
			} else {
				conn = connDriver.getConnectDB("Driver");
			}
			System.out.println("insided bean conn:" + conn);
			System.out.println("In Method : [previousForm]");
			System.out.println("xmlString : [" + paramData + "]");
			StringBuffer xmlString = new StringBuffer("<DocumentRoot>\r\n<description>Datawindow Root</description>\r\n<group0>\r\n<description>Group0 description</description>\r\n<Header0>\r\n<description>Header0 members</description>\r\n");
			// {$site_code}~{$item_code}~{$lot_no}~{$loc_code}~{$lot_sl}
			String[] paramArr = paramData.split("~");
			siteCode = paramArr[0];
			itemCode = paramArr[1];
			lotNo = paramArr[2];
			locCode = paramArr[3];
			lotSl = paramArr[4];
			System.out.println("siteCode["+paramArr[0]+"]itemCode["+paramArr[1]+"]lotNo["+paramArr[2]+"]locCode["+paramArr[3]+"]lotSl["+paramArr[4]+"]");
			
			sql = "SELECT F.SITE_CODE, F.ITEM_CODE, F.LOT_NO, F.LOC_CODE, F.LOT_SL, F.ISS_QTY, TRAN_ID, TRAN_SER, EXP_LEV"
					+ " FROM"
					+ "("	
										// INV_PACK - INV_PACK_ISS
						+ " SELECT B.SITE_CODE, A.ITEM_CODE, A.LOT_NO, A.LOC_CODE, A.LOT_SL, A.QUANTITY AS ISS_QTY," 
						+ " B.TRAN_ID ||'@'||A.LINE_NO TRAN_ID, 'PDL' as TRAN_SER , '' as EXP_LEV"
						+ " FROM INV_PACK_ISS A, INV_PACK B"
						+ " wHERE A.TRAN_ID = B.TRAN_ID" 
						+ " AND B.SITE_CODE = ?"
						+ " AND A.ITEM_CODE = ?"
						+ " AND A.LOC_CODE = ?"
						+ " AND A.LOT_NO = ?"
						+ " AND A.LOT_SL = ?"
						+ " AND B.CONFIRMED = 'N'"
					
						+ " UNION ALL"	// WORKORDER_ISS				
						+ " SELECT D.SITE_CODE, A.ITEM_CODE, A.LOT_NO, A.LOC_CODE, A.LOT_SL, (A.QUANTITY+NVL(A.POTENCY_ADJ,0)) AS ISS_QTY," 
						+ " D.TRAN_ID||'@'||A.LINE_NO TRAN_ID, 'W-ISS' as TRAN_SER, '' as EXP_LEV"
						+ " FROM WORKORDER_ISSDET A, WORKORDER_ISS D"
						+ " Where A.TRAN_ID = D.TRAN_ID "
						+ " AND D.SITE_CODE = ?"
						+ " AND A.ITEM_CODE = ?" 
					    + " AND A.LOC_CODE = ?"
					    + " AND A.LOT_NO = ?"
					    + " AND A.LOT_SL = ?"
					    + " AND D.CONFIRMED = 'N'"
					    + " AND D.TRAN_TYPE <> 'R'"
					    + " AND A.QUANTITY + NVL(A.POTENCY_ADJ,0) > 0"
		
						+ " UNION ALL"	// INV_ALLOCATE
						+ " SELECT D.SITE_CODE, A.ITEM_CODE, A.LOT_NO, A.LOC_CODE, A.LOT_SL, (A.QUANTITY+NVL(A.POTENCY_ADJ,0)) AS ISS_QTY,"
						+ " D.TRAN_ID||'@'||A.LINE_NO TRAN_ID, 'W-RIN' as TRAN_SER, A.EXP_LEV"
						+ " FROM INV_ALLOC_DET A, INV_ALLOCATE D"
						+ " Where A.TRAN_ID = D.TRAN_ID"
						+ " AND D.SITE_CODE = ?"
						+ " AND A.ITEM_CODE = ?"
						+ " AND A.LOC_CODE = ?"
						+ " AND A.LOT_NO = ?"
						+ " AND A.LOT_SL = ?"
						+ " AND (NVL(A.DEALLOCATED,'N') <> 'Y'"
						+ " OR A.DEALLOCATED            IS NULL)"
						+ " AND NOT EXISTS"
						+ " (SELECT H.WORK_ORDER"
						+ " FROM WORKORDER_ISS H,      WORKORDER_ISSDET I"
						+ " WHERE I.TRAN_ID        = H.TRAN_ID"
						+ " AND H.WORK_ORDER       = D.WORK_ORDER"
						+ " AND H.TRAN_DATE       >= '01-apr-00'"
						+ " AND SITE_CODE          = ?"
						+ " AND NVL(H.STATUS,'O') <> 'X'"
						+ " AND A.SITE_CODE        = H.SITE_CODE"
						+ " AND A.EXP_LEV          = I.EXP_LEV"
						+ " AND A.ITEM_CODE        = I.ITEM_CODE"
						+ " AND A.LOC_CODE         = I.LOC_CODE"
						+ " AND A.LOT_NO           = I.LOT_NO"
						+ " AND A.LOT_SL           = I.LOT_SL)"
			
						+ " UNION ALL"	// CONSUME_ISS
						+ " SELECT D.SITE_CODE__ORD, A.ITEM_CODE, A.LOT_NO, A.LOC_CODE, A.LOT_SL, A.QUANTITY AS ISS_QTY," 
						+ " D.CONS_ISSUE||'@'||A.LINE_NO TRAN_ID, 'C-ISS' as TRAN_SER, '' as EXP_LEV"
						+ " FROM CONSUME_ISS_DET A,   CONSUME_ISS D"
						+ " WHERE D.CONS_ORDER   >' '"
						+ " AND D.CONS_ISSUE     = A.CONS_ISSUE"
						+ " AND D.CONFIRMED      = 'N'"
						+ " And D.Tran_Type      = 'I'"
						+ " AND D.SITE_CODE__ORD = ?"
						+ " AND A.ITEM_CODE = ?"
						+ " AND A.LOC_CODE = ?"
						+ " AND A.LOT_NO = ?"
						+ " AND A.LOT_SL = ?"
						
						+ " UNION ALL"  // DESPATCH
						+ " SELECT D.SITE_CODE, A.ITEM_CODE, A.LOT_NO, A.LOC_CODE, A.LOT_SL, A.QUANTITY AS ISS_QTY," 
						+ " D.DESP_ID||'@'||A.LINE_NO TRAN_ID, 'S-DSP ' as TRAN_SER, '' as EXP_LEV"
						+ " FROM DESPATCHDET A,  DESPATCH D"
						+ " WHERE D.DESP_ID  = A.DESP_ID"
						+ " And D.Desp_Date >= '01-jan-2000'"
						+ " And D.Site_Code = ?"
						+ " AND A.ITEM_CODE = ?"
						+ " AND A.LOC_CODE = ?"
						+ " AND A.LOT_NO = ?"
						+ " AND A.LOT_SL = ?"
						+ " AND D.CONFIRMED  = 'N'"
						  
						+ " UNION ALL"	// SORDALLOC
						+ " SELECT A.SITE_CODE, A.ITEM_CODE, A.LOT_NO, A.LOC_CODE, A.LOT_SL, SUM(A.QUANTITY__STDUOM) AS ISS_QTY," 
						+ " A.SALE_ORDER||'@'||A.LINE_NO as TRAN_ID, 'S-ORD' as TRAN_SER, A.EXP_LEV"
						+ " FROM SORDALLOC A"
						+ " WHERE A.SITE_CODE = ?"
						+ " AND A.ITEM_CODE = ?"
						+ " AND A.LOC_CODE = ?"
						+ " AND A.LOT_NO = ?"
						+ " AND A.LOT_SL = ?"
						+ " AND NOT EXISTS"
						+ " (SELECT H.SORD_NO"
						+ " FROM DESPATCH H, DESPATCHDET I"
						+ " WHERE I.DESP_ID = H.DESP_ID"
						+ " AND H.SORD_NO = A.SALE_ORDER"
						+ " And I.Line_No__Sord = A.Line_No"
						+ " And H.Site_Code = ?"
						+ " AND A.ITEM_CODE = ?"
					    + " AND A.LOC_CODE = ?"
					    + " AND A.LOT_NO = ?"
					    + " AND A.LOT_SL = ?"
					    + " AND NVL(H.CONFIRMED,'N') = 'N'"
					    + " AND A.EXP_LEV            = I.EXP_LEV"
					    + " AND A.ITEM_CODE          = I.ITEM_CODE"
					    + " AND A.LOC_CODE           = I.LOC_CODE"
					    + " AND A.LOT_NO             = I.LOT_NO"
					    + " AND A.LOT_SL             = I.LOT_SL)"
					    + " GROUP BY A.SITE_CODE, A.ITEM_CODE, A.LOT_NO, A.LOC_CODE, A.Lot_Sl,  A.SALE_ORDER||'@'||A.LINE_NO, A.EXP_LEV" 
			
						+ " UNION ALL"  // DISTORD_ISS
						+ " SELECT D.SITE_CODE, A.ITEM_CODE, A.LOT_NO, A.LOC_CODE, A.LOT_SL, A.QUANTITY AS ISS_QTY," 
						+ " D.TRAN_ID||'@'||A.LINE_NO TRAN_ID, 'D-ISS' as TRAN_SER, '' as EXP_LEV"
						+ " FROM DISTORD_ISSDET A, DISTORD_ISS D"
						+ " WHERE D.TRAN_ID = A.TRAN_ID"
						+ " AND D.CONFIRMED = 'N'"
						+ " And D.Site_Code = ?"
						+ " And A.Item_Code = ?"
						+ " AND A.LOC_CODE = ?"
						+ " AND A.LOT_NO = ?"
						+ " AND A.LOT_SL = ?"
						
						+ " UNION ALL"  // ADJ_ISSRCP
						+ " SELECT D.SITE_CODE, A.ITEM_CODE, A.LOT_NO, A.LOC_CODE, A.LOT_SL, A.QUANTITY AS ISS_QTY," 
						+ " D.TRAN_ID||'@'||A.LINE_NO TRAN_ID, 'ADJISS' as TRAN_SER, '' as EXP_LEV"
						+ " FROM ADJ_ISSRCPDET A, ADJ_ISSRCP D"
						+ " WHERE D.TRAN_ID = A.TRAN_ID"
						+ " AND D.Site_Code = ?"
						+ " And A.Item_Code = ?"
						+ " AND A.LOC_CODE = ?"
						+ " AND A.LOT_NO = ?"
						+ " AND A.LOT_SL = ?"
						+ " AND D.CONFIRMED = 'N'"
						+ " AND D.REF_SER   = 'ADJISS'"
						   
						+ " UNION ALL"  // WORDER_ISS_RND
						+ " SELECT D.SITE_CODE, A.ITEM_CODE, A.LOT_NO, A.LOC_CODE, A.LOT_SL, (A.QUANTITY+NVL(A.POTENCY_ADJ,0)) AS ISS_QTY," 
						+ " D.TRAN_ID||'@'||A.LINE_NO TRAN_ID, 'W-ISS' as TRAN_SER, '' as EXP_LEV"
						+ " FROM WORDER_ISSDET_RND A, WORDER_ISS_RND D"
						+ " WHERE A.TRAN_ID  = D.TRAN_ID"
						+ " AND D.CONFIRMED <> 'Y'"
						+ " AND A.QUANTITY   > 0"
						+ " AND D.Site_Code = ?"
						+ " AND A.ITEM_CODE = ?"
						+ " AND A.LOC_CODE = ?"
						+ " AND A.LOT_NO = ?"
						+ " AND A.LOT_SL = ?"
						  
						+ " UNION ALL"  //RECEIPT_BACKFLUSH
						+ " SELECT D.SITE_CODE, A.ITEM_CODE, A.LOT_NO, A.LOC_CODE, A.LOT_SL, A.QUANTITY AS ISS_QTY," 
						+ " D.TRAN_ID||'@'||A.LINE_NO TRAN_ID, 'R-BFS' as TRAN_SER, '' as EXP_LEV"
						+ " FROM RECEIPT_BACKFLUSH_DET A, RECEIPT_BACKFLUSH D"
						+ " WHERE D.TRAN_ID = A.TRAN_ID"
						+ " AND D.Site_Code = ?"
						+ " And A.Item_Code = ?"
						+ " AND A.LOC_CODE = ?"
						+ " AND A.LOT_NO = ?"
						+ " AND A.LOT_SL = ?"
						+ " AND D.CONFIRMED = 'N'"
						  
						+ " UNION ALL"  // STOCK_TRANSFER
						+ " SELECT D.SITE_CODE, A.ITEM_CODE, A.LOT_NO__FR AS LOT_NO, A.LOC_CODE__FR AS LOC_CODE, A.LOT_SL__FR AS LOT_SL, A.QUANTITY AS ISS_QTY," 
						+ " D.TRAN_ID||'@'||A.LINE_NO TRAN_ID, 'XFRX' as TRAN_SER, '' as EXP_LEV"
						+ " FROM STOCK_TRANSFER_DET A, STOCK_TRANSFER D"
						+ " WHERE D.TRAN_ID = A.TRAN_ID"
						+ " AND D.Site_Code = ?"
						+ " AND A.ITEM_CODE = ?"
						+ " AND A.LOC_CODE__FR = ?"
						+ " AND A.LOT_NO__FR = ?"
						+ " AND A.LOT_SL__FR = ?"
						+ " AND D.CONFIRMED = 'N'"
						 
						+ " UNION ALL"  // QC_SAMPLE
						+ " SELECT D.SITE_CODE, D.ITEM_CODE, A.LOT_NO, A.LOC_CODE, A.LOT_SL, A.QTY_SAMPLE AS ISS_QTY," 
						+ " D.TRAN_ID||'@'||A.LINE_NO TRAN_ID, 'S-ISS' as TRAN_SER, '' as EXP_LEV"
						+ " FROM QC_SAMPLE_STK A, QC_SAMPLE D"
						+ " WHERE D.TRAN_ID = A.TRAN_ID"
						+ " And D.Site_Code = ?"
						+ " And d.Item_Code = ?"
						+ " AND A.LOC_CODE = ?"
						+ " AND A.LOT_NO = ?"
						+ " AND A.LOT_SL = ?"
						+ " And D.Confirmed = 'N'"
						  
						+ " UNION ALL"  // PORCP
						+ " SELECT A.SITE_CODE, B.ITEM_CODE, B.LOT_NO, B.LOC_CODE, B.LOT_SL, B.QUANTITY AS ISS_QTY," 
						+ " A.TRAN_ID||'@'||B.LINE_NO TRAN_ID,'P-RET' as TRAN_SER, '' as EXP_LEV"
						+ " FROM PORCP A,PORCPDET B"
						+ " Where A.TRAN_ID = B.TRAN_ID"
						+ " AND A.SITE_CODE = ?"
						+ " And B.Item_Code = ?"
						+ " AND B.LOC_CODE = ?"
						+ " AND B.LOT_NO = ?"
						+ " AND B.LOT_SL = ?"
						+ " AND A.CONFIRMED = 'N'"
						+ " AND A.TRAN_SER  ='P-RET'"  
					+ " )F"
					+ " ORDER BY F.SITE_CODE, F.ITEM_CODE, F.LOT_NO, F.LOC_CODE, F.Lot_Sl";						 			
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, siteCode); 	//INV_PACK_ISS
			pstmt.setString(2, itemCode);
			pstmt.setString(3, locCode);
			pstmt.setString(4, lotNo);
			pstmt.setString(5, lotSl);  
			
			pstmt.setString(6, siteCode); 	//WORKORDER_ISS
			pstmt.setString(7, itemCode);
			pstmt.setString(8, locCode);
			pstmt.setString(9, lotNo);
			pstmt.setString(10, lotSl); 
			
			pstmt.setString(11, siteCode); 	//INV_ALLOCATE
			pstmt.setString(12, itemCode);
			pstmt.setString(13, locCode);
			pstmt.setString(14, lotNo);
			pstmt.setString(15, lotSl);
			pstmt.setString(16, siteCode);
			
			pstmt.setString(17, siteCode); 	//CONSUME_ISS
			pstmt.setString(18, itemCode);
			pstmt.setString(19, locCode);
			pstmt.setString(20, lotNo);
			pstmt.setString(21, lotSl);
			
			pstmt.setString(22, siteCode); 	//DESPATCH
			pstmt.setString(23, itemCode);
			pstmt.setString(24, locCode);
			pstmt.setString(25, lotNo);
			pstmt.setString(26, lotSl);
			
			pstmt.setString(27, siteCode); 	//SORDALLOC
			pstmt.setString(28, itemCode);
			pstmt.setString(29, locCode);
			pstmt.setString(30, lotNo);
			pstmt.setString(31, lotSl);
			pstmt.setString(32, siteCode); 	
			pstmt.setString(33, itemCode);
			pstmt.setString(34, locCode);
			pstmt.setString(35, lotNo);
			pstmt.setString(36, lotSl);
			
			pstmt.setString(37, siteCode); 	//DISTORD_ISS
			pstmt.setString(38, itemCode);
			pstmt.setString(39, locCode);
			pstmt.setString(40, lotNo);
			pstmt.setString(41, lotSl);
			
			pstmt.setString(42, siteCode); 	//ADJ_ISSRCP
			pstmt.setString(43, itemCode);
			pstmt.setString(44, locCode);
			pstmt.setString(45, lotNo);
			pstmt.setString(46, lotSl);
			
			pstmt.setString(47, siteCode); 	//WORDER_ISS_RND
			pstmt.setString(48, itemCode);
			pstmt.setString(49, locCode);
			pstmt.setString(50, lotNo);
			pstmt.setString(51, lotSl);
			
			pstmt.setString(52, siteCode); 	//RECEIPT_BACKFLUSH
			pstmt.setString(53, itemCode);
			pstmt.setString(54, locCode);
			pstmt.setString(55, lotNo);
			pstmt.setString(56, lotSl);
			
			pstmt.setString(57, siteCode); 	//STOCK_TRANSFER
			pstmt.setString(58, itemCode);
			pstmt.setString(59, locCode);
			pstmt.setString(60, lotNo);
			pstmt.setString(61, lotSl);
			
			pstmt.setString(62, siteCode); 	//QC_SAMPLE
			pstmt.setString(63, itemCode);
			pstmt.setString(64, locCode);
			pstmt.setString(65, lotNo);
			pstmt.setString(66, lotSl);
			
			pstmt.setString(67, siteCode); 	//PORCP
			pstmt.setString(68, itemCode);
			pstmt.setString(69, locCode);
			pstmt.setString(70, lotNo);
			pstmt.setString(71, lotSl);
			System.out.println("GetPendingStock @> Sql::[" + sql + "]");
			rs = pstmt.executeQuery();			
			while (rs.next()) {	
						
				row++;
				xmlString.append("<Detail3 dbID='' domID='" + row+ "' selected='" + "N" + "'>\r\n");
				xmlString.append("<attribute pkNames='' status='" + "N"	+ "' updateFlag='" + "A" + "' selected='" + "N"	+ "' />\r\n");
				
				xmlString.append("<site_code><![CDATA[").append(rs.getString("SITE_CODE")).append("]]></site_code>");
				xmlString.append("<item_code><![CDATA[").append(rs.getString("ITEM_CODE")).append("]]></item_code>");				
				xmlString.append("<lot_no><![CDATA[").append(checkNull(rs.getString("LOT_NO"))).append("]]></lot_no>");
				xmlString.append("<loc_code><![CDATA[").append(checkNull(rs.getString("LOC_CODE"))).append("]]></loc_code>");
				xmlString.append("<lot_sl><![CDATA[").append(checkNull(rs.getString("LOT_SL"))).append("]]></lot_sl>");
				xmlString.append("<iss_qty><![CDATA[").append(rs.getDouble("ISS_QTY")).append("]]></iss_qty>");
				xmlString.append("<tran_id><![CDATA[").append(rs.getString("TRAN_ID")).append("]]></tran_id>");
				xmlString.append("<tran_ser><![CDATA[").append(checkNull(rs.getString("TRAN_SER"))).append("]]></tran_ser>");
				xmlString.append("<exp_lev><![CDATA[").append(checkNull(rs.getString("EXP_LEV"))).append("]]></exp_lev>");
				
				xmlString.append("</Detail3>\r\n");
			}
			pstmt.close();
			rs.close();
			pstmt = null;
			rs = null;

			xmlString.append("</Header0>\r\n");
			xmlString.append("</group0>\r\n");
			xmlString.append("</DocumentRoot>\r\n");
			System.out.println("Pavan R 2018 Pending...[" + xmlString + "]End");
			String xslFileName = getXSLFileName("stock_alloc_verify_AllocDet"+ formNo + "_wiz_" + this.user_lang + "_"+ this.user_country + "_" + "A" + ".xsl");

			retHtmlData = (genericUtility).transformToString(xslFileName,xmlString.toString(), CommonConstants.APPLICATION_CONTEXT+ File.separator + "temp", "Output", ".html");
			System.out.println("retHtmlData["+retHtmlData+"]");
		} 
		catch (SQLException s) {
			System.out.println("StockAllocVerifyWizBean: getAllocDetail::" + s.getMessage());			
			s.printStackTrace();
			throw new ITMException(s);
		} catch (Exception e) {
			e.printStackTrace();
			e.printStackTrace();
			throw new ITMException(e);
		}finally{
			try
			{
				if (conn != null)
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
					conn.close();
					conn = null;
				}				
			} catch (Exception e)
			{	
				e.printStackTrace();
			}
		}

		return retHtmlData;
	}
	public static String checkNull(String input)
	{
		if (input == null || "null".equalsIgnoreCase(input) || "undefined".equalsIgnoreCase(input))
		{
			input= "";
		}
		return input.trim();
	}
	private String getXSLFileName(String xslFileName) throws ITMException {
		String retFileName = null;
		try {
			String defaultPath = null;
			if (CommonConstants.APPLICATION_CONTEXT != null) {
				defaultPath = CommonConstants.APPLICATION_CONTEXT+ CommonConstants.ITM_CONTEXT + File.separator;
			} else {
				defaultPath = ".." + File.separator + "webapps"	+ File.separator + "ibase" + File.separator	+ CommonConstants.ITM_CONTEXT + File.separator;
			}
			File xslPath = new File(defaultPath + File.separator + "xsl"+ File.separator + CommonConstants.THEME + File.separator+ "WIZARD" + File.separator + "Galaxy");
			if (!xslPath.exists()) {
				xslPath.mkdir();
			}
			System.out.println(" xslPath [" + xslPath + "] xslFileName ["+ xslFileName + "]");
			File xslFile = new File(xslPath, xslFileName);
			if (xslFile.exists()) {
				retFileName = xslFile.getAbsolutePath();
			} else {
				throw new ITMException(new Exception(retFileName+ " Wizard XSL file Not Found"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new ITMException(e);
		}
		return retFileName;
	}

}