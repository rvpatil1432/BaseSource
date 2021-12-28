package ibase.webitm.ejb.dis.adv;

import java.util.*;
import java.sql.*;
import java.rmi.RemoteException;
import org.w3c.dom.*;
import ibase.webitm.ejb.*;
import ibase.webitm.utility.ITMException;
import javax.ejb.Stateless;

@Stateless
// added for ejb3
public class StockTransferAct extends ActionHandlerEJB implements
		StockTransferActLocal, StockTransferActRemote {
	public String actionHandler() throws RemoteException, ITMException {
		return "";
	}

	public String actionHandler(String actionType, String xmlString,
			String xmlString1, String objContext, String xtraParams)
			throws RemoteException, ITMException {
		Document dom = null;
		Document dom1 = null;
		String retString = null;

		ibase.utility.E12GenericUtility genericUtility = new ibase.utility.E12GenericUtility();
		try {
			if (xmlString != null && xmlString.trim().length() != 0) {
				System.out.println("XML String :" + xmlString);
				dom = genericUtility.parseString(xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() != 0) {
				System.out.println("XML String1 :" + xmlString1);
				dom1 = genericUtility.parseString(xmlString1);
			}

			System.out.println("actionType:" + actionType + ":");

			if (actionType.equalsIgnoreCase("All Items")) {

				retString = actionAllItems(dom, dom1, xtraParams, actionType);
			}

		}
		
		catch (Exception e) 
		{
			System.out.println("Exception :StockTransferAct :actionHandler(String xmlString):"+ e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning String from StockTransferAct : actionHandler"+ retString);
		return retString;
	}

	private String actionAllItems(Document dom, Document dom1,String xtraParams, String actionType) throws RemoteException,ITMException 
	{
		System.out.println("xmlString from StockTransferActEJB :" + dom+ " \n xmlString1 :" + dom1);
		String refIdFor="",refSerFor = "", lineNo="",acctCodeDr="",cctrCodeDr="",locCodeTo="",trantype="";
		//int lineCntr=0;
		ResultSet rs = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		String siteCodeReq = "", sql = "";
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		String detailCnt = "";
		String itemCode = "";
		String availableYn = "";
		int detCnt = 0;
		double remainingQty = 0d;

		ibase.utility.E12GenericUtility genericUtility = new ibase.utility.E12GenericUtility();
		System.out.println("Action Default Entry time :: ");

		try 
		{		
			detailCnt = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "detCnt");

			if (detailCnt != null) 
			{
				detCnt = Integer.parseInt(detailCnt);
				System.out.println("detCnt...........:: " + detCnt);
			}

			if (dom == null || detCnt > 1) 
			{
				valueXmlString.append("</Root>\r\n");
				return valueXmlString.toString();
			}
			conn = getConnection();

			refSerFor = genericUtility.getColumnValue("ref_ser__for", dom1);
			refSerFor = refSerFor.trim();
			System.out.println("Reference series::::::::["+refSerFor+"]");
			
			locCodeTo = genericUtility.checkNull(genericUtility.getColumnValue("loc_code__to",dom));
			System.out.println("Location code::::::::["+locCodeTo+"]");
			
			refIdFor = genericUtility.getColumnValue("ref_id__for", dom1);
			System.out.println("Reference id::::::::["+refIdFor+"]"); 
			
			//Added by mayur on 17-July-2018--[start]
			trantype = genericUtility.getColumnValue("tran_type", dom1);
			System.out.println("Tran Type::::::::["+trantype+"]");
			//Added by mayur on 17-July-2018--[end]

			if(refIdFor != null && refIdFor.trim().length() >  0)
			{
				System.out.println("Inside reference id for");	
			//Consumption order
			if (("C-ORD").trim().equalsIgnoreCase(refSerFor))
			{
				
				sql = "select hdr.available_yn," 
						+ "hdr.site_code__req,"
						+ "det.line_no," 
						+ "det.item_code," 
						+ "hdr.loc_code,"
						+ "det.quantity,"
						+ "req_typ.udf_str1, " 
						+ "req_typ.udf_str2 "
						+ " from consume_ord hdr left outer join ser_req_typ req_typ on hdr.order_type = req_typ.req_type,consume_ord_det det"
						+ " where det.cons_order = hdr.cons_order "
						+ " and hdr.cons_order = ?";

				System.out.println("SQL ::" + sql);
				pstmt = conn.prepareStatement( sql );
				pstmt.setString(1,refIdFor);
				rs = pstmt.executeQuery();

				while (rs.next()) {
					availableYn = rs.getString("available_yn");
					siteCodeReq =  rs.getString("site_code__req");
					lineNo = rs.getString("line_no");
					itemCode = rs.getString("item_code");
					remainingQty = rs.getDouble("quantity");
					acctCodeDr = rs.getString("udf_str1");
					cctrCodeDr = rs.getString("udf_str2");
					//locCode = rs.getString("loc_code");

					System.out.println(":::CONSUMPTION ORDER DETAILS:::");		
					System.out.println("AvailableYn ::["+availableYn+"]" );
					System.out.println("itemCode ::["+itemCode+"]");
					System.out.println("quantity taken as remainingQty ::["+remainingQty+"]");
					System.out.println(":::SER_REQ_TYP DETAILS:::");	
					System.out.println("acctCodeDr ::["+acctCodeDr+"]");
					System.out.println("cctrCodeDr ::["+cctrCodeDr+"]");
					//System.out.println("Location code ::" + locCode);

					valueXmlString.append( StockDetails(trantype,itemCode,locCodeTo,acctCodeDr,cctrCodeDr,siteCodeReq, availableYn, remainingQty, conn ) );
					
				}//end of while loop
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;		
		
			} //end of if for consume order
			else if (("S-REQ").trim().equalsIgnoreCase(refSerFor))
			{

				sql="select hdr.site_code, " 
						 //Changes by mayur on 09-May-2018----start
						//Add engineer code from service order to be set as default loc code to
						+"hdr.emp_code__assign,"  
					    //Changes by mayur on 09-May-2018----end
						+"det.line_no, det.item_code, " 
						+"det.quantity, hdr.req_type, " 
						+"req_typ.udf_str1, req_typ.udf_str2 "
						+ "from ser_request hdr left outer join ser_req_typ req_typ on hdr.req_type = req_typ.req_type, ser_req_item det "
						+ "where hdr.req_id = det.req_id and hdr.req_id = ?";

				System.out.println("SQL ::" + sql);
				pstmt = conn.prepareStatement( sql );
				pstmt.setString(1,refIdFor);
				rs = pstmt.executeQuery();

				while (rs.next()) {
					siteCodeReq =  rs.getString("site_code");
					locCodeTo = rs.getString("emp_code__assign");  //get the engineer code 
					lineNo =   rs.getString("line_no");
					itemCode = rs.getString("item_code");
					remainingQty = rs.getDouble("quantity");
					acctCodeDr = rs.getString("udf_str1");
					cctrCodeDr = rs.getString("udf_str2");

					System.out.println(":::SER_REQUEST TABLE DETAIL:::");		
					System.out.println("siteCodeReq::["+siteCodeReq+"]");
					System.out.println("locCodeTo as engineer code::["+locCodeTo+"]");
					
					System.out.println(":::SER_REQ_ITEM TABLE DETAIL:::");
					System.out.println("lineNo ::["+lineNo+"]");
					System.out.println("itemCode ::["+itemCode+"]");
					
					System.out.println("quantity taken as remainingQty ::["+remainingQty+"]");
					System.out.println(":::SER_REQ_TYP DETAILS:::");	
					System.out.println("acctCodeDr ::["+acctCodeDr+"]");
					System.out.println("cctrCodeDr ::["+cctrCodeDr+"]");
									
					valueXmlString.append( StockDetails( trantype,itemCode,locCodeTo,acctCodeDr,cctrCodeDr,siteCodeReq, "Y", remainingQty, conn ) );
					
				}//end of while 
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
					
			}//end of else if for ser request
			valueXmlString.append("</Root>\r\n");
			}//end of refIdFor
	}//end of try block
		catch (SQLException sqx) 
		{
			System.out.println("The sqlException occured in StockTransferAct for Default button:"+ sqx);
			throw new ITMException(sqx);
		} 
		catch (Exception e) 
		{
			System.out.println("The Exception occured in StockTransferAct for Default button:"+ e);
			throw new ITMException(e);
		} 
		finally 
		{
			try 
			{
				if( rs != null )  
					rs.close();
				    rs = null;
				if( pstmt != null )  
					pstmt.close();
				    pstmt = null;
				if( conn != null )
					conn.close();
				    conn = null;	
			} 
			catch (Exception e)
			{
				 e.printStackTrace(); 
			}
		} //end of finally block

		System.out.println("valueXmlString return from StockTransferAct[actionAllItems] :"+ valueXmlString.toString());
		return valueXmlString.toString();

	} // End of actionAllItems method
	
	 private String StockDetails(String trantype,String itemCode,String locCodeTo,String acctCodeDr,String cctrCodeDr,String siteCodeReq,String availableYn,double remainingQty,Connection conn) throws ITMException
	 {
		 HashMap<String, Double> hm = new HashMap<String, Double>();
		 String lotNum="",lotSerial="", accountInv="",costCenInv="",remarks="",locCodeFr="",udfStr1="";
		 double noArt = 0;
		 double hmQty = 0d;
		 double inputQty = 0d;
		 PreparedStatement pstmt1 = null,  pstmt2 = null;
		 ResultSet rs1 = null, rs2 = null;
		 String sqlDscr = "", itemDscr = "", locFrDscr = "", locToDscr="";
		 int lineCntr=0;
		 
		 StringBuilder valueXmlString = new StringBuilder();

		 try
		 {
			//Added by mayur on 18-July-2018--[start]
			 String sql1 = "SELECT GEN.UDF_STR1 FROM GENCODES GEN"
						+" WHERE GEN.FLD_NAME = 'TRAN_TYPE'"
				 		+" AND GEN.MOD_NAME = 'W_STOCK_TRANSFER' "
						+" AND GEN.FLD_VALUE =  ? ";
				 System.out.println("sql :" + sql1);
				 pstmt1 = conn.prepareStatement( sql1 );	
				 pstmt1.setString(1,trantype);											
				 rs1 = pstmt1.executeQuery();
				 
				 if (rs1.next())
				 {
					udfStr1 = rs1.getString( "UDF_STR1" );
					System.out.println("udfStr1["+udfStr1+"]");
				 }
				 
				 rs1.close();
				 rs1 = null;
				 pstmt1.close();
				 pstmt1 = null;					 
		        //Added by mayur on 18-July-2018--[end]			

				 String sql2 = "SELECT STOCK.ITEM_SER, "
						 + "STOCK.ITEM_CODE,"
						 + "STOCK.UNIT, "
						 + "STOCK.LOC_CODE,"
						 + "STOCK.LOT_NO,"
						 + "STOCK.LOT_SL,"
						 + "STOCK.QUANTITY - STOCK.ALLOC_QTY AS BAL_QUANTITY, "
						 + "STOCK.SITE_CODE, "
						 + "STOCK.NO_ART, "
						 + "(STOCK.QUANTITY - CASE WHEN STOCK.ALLOC_QTY IS NULL THEN 0 ELSE STOCK.ALLOC_QTY END -CASE WHEN STOCK.HOLD_QTY IS NULL THEN 0 ELSE STOCK.HOLD_QTY END) ,"
						 + "STOCK.EXP_DATE, " 
						 + "STOCK.RETEST_DATE, "
						 + "STOCK.DIMENSION, " 
						 + "STOCK.ACCT_CODE__INV, " 
						 + "STOCK.CCTR_CODE__INV, " 					
						 + "STOCK.RATE "
						 + "FROM STOCK,INVSTAT,LOCATION"
						 + " WHERE LOCATION.INV_STAT = INVSTAT.INV_STAT" //Changes by mayur on 20-July-2018
						 + " AND STOCK.LOC_CODE = LOCATION.LOC_CODE"     //Changes by mayur on 20-July-2018
						 + " AND STOCK.ITEM_CODE = ? "   
						 + " AND STOCK.SITE_CODE = ? "     
						 + " AND STOCK.QUANTITY - STOCK.ALLOC_QTY > 0"
						 + " AND INVSTAT.AVAILABLE = ? ";
				         //Added by mayur on 18-July-2018--[start]
				         if(udfStr1 != null && udfStr1.trim().length() > 0)
				         {
				           sql2= sql2 + " AND LOCATION.LOC_GROUP =  '"+udfStr1+"' "; 
				           System.out.println("sql2 :" + sql2);			
				         }
				         //Added by mayur on 18-July-2018--[end]
				         
				 pstmt1 = conn.prepareStatement( sql2 );
				 pstmt1.setString(1,itemCode);				
				 pstmt1.setString(2,siteCodeReq);
				 pstmt1.setString(3,availableYn);	
				 rs1 = pstmt1.executeQuery();


			 System.out.println(":::STOCK DETAILS:::");		
			 System.out.println("itemCode ::["+itemCode+"]");			
			 System.out.println("siteCodeReq ::["+siteCodeReq+"]");		
			 System.out.println("availableYn ::["+availableYn+"]");

			 while (rs1.next()) {
				 lineCntr++;
				 locCodeFr = rs1.getString( "LOC_CODE" );
				 lotNum = rs1.getString( "LOT_NO" );
				 lotSerial = rs1.getString("LOT_SL");
				// remainingQty = rs1.getDouble( "BAL_QUANTITY" );
				 noArt = rs1.getDouble( "NO_ART" );
				 accountInv = rs1.getString( "ACCT_CODE__INV" );
				 costCenInv = rs1.getString( "CCTR_CODE__INV" );

				 
				 System.out.println("locCodeFr :["+locCodeFr+"]");
				 System.out.println("lotNum :["+lotNum+"]");
				 System.out.println("lotSerial :["+lotSerial+"]");				
				 System.out.println("noArt :["+noArt+"]");
				 System.out.println("accountInv :["+accountInv+"]");
				 System.out.println("costCenInv :["+costCenInv+"]");

				 String mapKey = itemCode + "~" + siteCodeReq + "~"+ locCodeTo + "~" + lotNum + "~" + lotSerial;
				 System.out.println("mapKey-->"+mapKey);
				 
				 if (!hm.containsKey(mapKey)) 
				 {
					 hm.put(mapKey, new Double(rs1.getDouble(10)));
				 }

				 hmQty = Double.parseDouble((hm.get(mapKey)).toString());
				 System.out.println("hmQty is ["+hmQty+"]");
				 
				 if (remainingQty == 0) {
					 break;
				 }

				 else if (hmQty >= remainingQty) {
					 System.out.println("@@Inside hmQty >= remainingQty["+hmQty+">="+remainingQty+"]");
					 inputQty = remainingQty;
					 System.out.println("inputQty :["+inputQty+"]");
					 remainingQty = 0;
					 hm.put(mapKey, new Double(hmQty - inputQty));

					 System.out.println("hmQty - remainingQty :"+ (hmQty - inputQty));
					 System.out.println("hm if [hmQty >= remainingQty] :["+ hm+"]");
				 }

				 else if (hmQty < remainingQty) {
					 System.out.println("@@Inside hmQty < remainingQty["+hmQty+"<"+remainingQty+"]");
					 inputQty = hmQty;
					 System.out.println("inputQty :["+inputQty+"]");

					 remainingQty = remainingQty - inputQty;
					 System.out.println("remainingQty :["+remainingQty+"-"+inputQty+"]");
					 System.out.println("remainingQty :["+remainingQty+"]");
					 hm.put(mapKey, new Double(0));
				 }
				 System.out.println("Hashmap :" + hm);
				//changes by mayur on 06-June-2018 ---end
				//Added and below appended by Pavan R on 13aug18 start [to set descr of item and locFr and locTo]
				 sqlDscr = "SELECT DESCR FROM ITEM WHERE  ITEM_CODE  = ? ";
					pstmt2 = conn.prepareStatement( sqlDscr );
					pstmt2.setString( 1, itemCode );						
					rs2 = pstmt2.executeQuery();	
					if( rs2.next() )
					{
						itemDscr = rs2.getString("DESCR");						
					}
					rs2.close();rs2 = null;
					pstmt2.close();pstmt2 = null;
					
					if(locCodeFr != null && locCodeFr.trim().length() > 0)
					{
						sqlDscr = "SELECT DESCR FROM LOCATION WHERE LOC_CODE  = ? ";				
						pstmt2 = conn.prepareStatement( sqlDscr );
						pstmt2.setString( 1, locCodeFr );						
						rs2 = pstmt2.executeQuery();	
						if( rs2.next() )
						{
							locFrDscr = rs2.getString("DESCR");						
						}
						rs2.close();rs2 = null;
						pstmt2.close();pstmt2 = null;
					}
					if(locCodeTo != null && locCodeTo.trim().length() > 0)
					{					
						sqlDscr = "SELECT DESCR FROM LOCATION WHERE LOC_CODE  = ? ";
						pstmt2 = conn.prepareStatement( sqlDscr );
						pstmt2.setString( 1, locCodeTo );						
						rs2 = pstmt2.executeQuery();	
						if( rs2.next() )
						{
							locToDscr = rs2.getString("DESCR");						
						}
						rs2.close();rs2 = null;
						pstmt2.close();pstmt2 = null;
					}	
				System.out.println("Pavan R Description:: Item["+itemDscr+"]LocFr["+locFrDscr+"]LocTo["+locToDscr+"]");	
				//Added and below appended by Pavan R End
				 valueXmlString.append("<Detail>\r\n");	
				 //Commented by Varsha V on 31-08-18 for GTPL issue 623[Getting error in adding multiple records in detail]
				 //valueXmlString.append("<line_no isSrvCallOnChg=\"0\">").append("<![CDATA[").append(lineCntr).append("]]>").append("</line_no>\r\n");
				 valueXmlString.append("<item_code isSrvCallOnChg=\"0\">").append("<![CDATA[").append(itemCode).append("]]>").append("</item_code>\r\n");
				 valueXmlString.append("<item_descr isSrvCallOnChg=\"0\">").append("<![CDATA[").append(itemDscr).append("]]>").append("</item_descr>\r\n");
				 valueXmlString.append("<loc_code__to isSrvCallOnChg=\"0\">").append("<![CDATA[").append( locCodeTo.trim() ).append("]]>").append("</loc_code__to>\r\n");
				 valueXmlString.append("<loc_descr__to isSrvCallOnChg=\"0\">").append("<![CDATA[").append(locToDscr).append("]]>").append("</loc_descr__to>\r\n");
				 valueXmlString.append("<loc_code__fr isSrvCallOnChg=\"0\">").append("<![CDATA[").append( locCodeFr.trim() ).append("]]>").append("</loc_code__fr>\r\n");
				 valueXmlString.append("<location_descr__fr isSrvCallOnChg=\"0\">").append("<![CDATA[").append(locFrDscr).append("]]>").append("</location_descr__fr>\r\n");
				 valueXmlString.append("<lot_no__to isSrvCallOnChg=\"0\">").append("<![CDATA[").append(lotNum).append("]]>").append("</lot_no__to>\r\n");
				 valueXmlString.append("<lot_no__fr isSrvCallOnChg=\"0\">").append("<![CDATA[").append(lotNum).append("]]>").append("</lot_no__fr>\r\n");				 
				 valueXmlString.append("<lot_sl__to isSrvCallOnChg=\"0\">").append("<![CDATA[").append(lotSerial).append("]]>").append("</lot_sl__to>\r\n");
				 valueXmlString.append("<lot_sl__fr isSrvCallOnChg=\"0\">").append("<![CDATA[").append(lotSerial).append("]]>").append("</lot_sl__fr>\r\n");				 
				 valueXmlString.append("<no_art isSrvCallOnChg=\"0\">").append("<![CDATA[").append(noArt).append("]]>").append("</no_art>\r\n");
				 valueXmlString.append("<quantity isSrvCallOnChg=\"0\">").append("<![CDATA[").append(inputQty).append("]]>").append("</quantity>\r\n");		
				 valueXmlString.append("<acct_code__cr isSrvCallOnChg=\"0\">").append("<![CDATA[").append(accountInv).append("]]>").append("</acct_code__cr>\r\n");					
				 valueXmlString.append("<cctr_code__cr isSrvCallOnChg=\"0\">").append("<![CDATA[").append(costCenInv).append("]]>").append("</cctr_code__cr>\r\n");	
				 valueXmlString.append("<acct_code__dr isSrvCallOnChg=\"0\">").append("<![CDATA[").append((acctCodeDr== null) ?"":acctCodeDr).append("]]>").append("</acct_code__dr>\r\n");
				 valueXmlString.append("<cctr_code__dr isSrvCallOnChg=\"0\">").append("<![CDATA[").append((cctrCodeDr== null) ?"":cctrCodeDr).append("]]>").append("</cctr_code__dr>\r\n");					
				 valueXmlString.append("<remarks isSrvCallOnChg=\"0\">").append("<![CDATA[").append(remarks).append("]]>").append("</remarks>\r\n");					
				 valueXmlString.append("</Detail>\r\n");	
				 //changes by mayur on 06-June-2018 ---end
				 
			 } // end of while loop
			 rs1.close();
			 rs1 = null;
			 pstmt1.close();
			 pstmt1 = null;

		 } //end of try block
		 catch (SQLException sqx) 
		 {
			 System.out.println("The sqlException occured in StockTransferAct for Default button:"+ sqx);
			 throw new ITMException(sqx);
		 } 
		 catch (Exception e) 
		 {
			 System.out.println("The Exception occured in StockTransferAct for Default button:"+ e);
			 throw new ITMException(e);
		 } 
		 finally
		 {

			 try 
			 {

				 if( rs1 != null )  
					 rs1.close();
				 rs1 = null;	
				 if( pstmt1!= null )     
					 pstmt1.close();
				 pstmt1 = null;		

			 } 
			 catch (Exception e)
			 {
				 e.printStackTrace(); 
			 }

		 }//end of finally

		 return valueXmlString.toString();
	 } //End of stock details 
}
