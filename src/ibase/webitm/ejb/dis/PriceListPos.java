package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
//import ibase.webitm.utility.GenericUtility;
import java.sql.*;

import javax.ejb.*;
import ibase.webitm.ejb.*;

import org.w3c.dom.*;
import ibase.webitm.ejb.*;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;

import java.util.*;
import java.text.SimpleDateFormat;
import ibase.webitm.utility.*;
//import ibase.webitm.ejb.MasterStateful;
//import ibase.webitm.ejb.MasterStatefulHome;
import ibase.system.config.ConnDriver;
import java.text.DecimalFormat;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class PriceListPos extends ValidatorEJB implements PriceListPosLocal, PriceListPosRemote  //added for ejb3
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	DecimalFormat df = new DecimalFormat( "##.00" );
	ibase.webitm.ejb.sys.UtilMethods utilMethods = ibase.webitm.ejb.sys.UtilMethods.getInstance();
	
    public String postSaveRec() throws RemoteException,ITMException
	{
		return "";
	} 
    public String postSaveRec(String xmlString1,String domId,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{
		String returnVal = "";
		try
		{
			if (xmlString1 != null && xmlString1.trim().length() > 0)
			{
				System.out.println("@@@@@@@@ xmlString1["+xmlString1+"]xtraParams["+xtraParams+"]");
				returnVal = generatePriceList( xmlString1, xtraParams, conn );
			}		
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		return returnVal;
	}
	private String generatePriceList( String xmlData, String xtraParams, Connection conn ) throws RemoteException,ITMException
	{
		String retString = "";
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		Document dom = null;
		String childNodeName = null;
		Node childNode = null;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		int childNodeListLength;
		PreparedStatement pstmt = null;
		PreparedStatement dtlpstmt = null;
		PreparedStatement insPriceListPStmt = null;
		PreparedStatement upDtPlPStmt = null;
		PreparedStatement chkPlpstmt = null;
		PreparedStatement dateOverpstmt = null;
		PreparedStatement pstmtSql = null;
		ArrayList PricelistGen = new ArrayList();
		HashMap PList=null;
		int ctr = 0;
		String sql = "";
		ResultSet rs = null;
		ResultSet dtlrs = null;
		ResultSet chkPlrs = null;
		String userId ="";
		String termId ="";
		String update_flag = null;
		userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
		termId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId");
		
		
		String dbName = "";
		//String retString = "";
		String errCode="", priceList="",confirmed="",empcode="", itemCode="", lotNoForm="";
		String lotNoTo ="", rateType="", chgref="", listType="", PriceListParent="",PriceList="";
		String orderType="", manageType="", plist1="",plist2="",plist3="",plist4="",plist5="",plist6="",plist7="",plist8="";
		String plist9="",plist10="", plist11="",plist12="",unit="", refNoold="",refNO="",data="",calc="",Str="",chgRefNo="";
		String calcmeth="",plisttar="";
		java.sql.Timestamp tranDate = null,confDate=null, efffrom=null,validup= null;
		double rate=0.0;
		double minrate=0.0;
		double maxrate=0.0;
		double rate1=0.0;
		double rate2=0.0;
		double rate3=0.0;
		double rate4=0.0;
		double rate5=0.0;
		double rate6=0.0;
		double rate7=0.0;
		double rate8=0.0;
		double rate9=0.0;
		double rate10=0.0;
		double rate11=0.0;
		double rate12=0.0;
		double minqty=0.0;
		double maxqty=0.0;
		int lineno=0;
		String loginEmpCode = "";
		
		
		
		String insPriceList = "INSERT into pricelist(  PRICE_LIST, ITEM_CODE, UNIT, LIST_TYPE, SLAB_NO, "                
					+"	EFF_FROM, VALID_UPTO, LOT_NO__FROM, LOT_NO__TO, "
					+" MIN_QTY, MAX_QTY, RATE, RATE_TYPE, MIN_RATE, "               
					+" CHG_DATE, CHG_USER, CHG_TERM, MAX_RATE, ORDER_TYPE, "             
					+" PRICE_LIST__PARENT, CHG_REF_NO, CALC_BASIS, REF_NO, REF_NO_OLD) "
					+" values ( ?, ?, ?, ?, ?, "
					+"			?, ?, ?, ?, ?, "
					+"			?, ?, ?, ?, ?, "
					+"			?, ?, ?, ?, ?, "
					+"			?, ?, ?, ? ) ";
		
		try
		{
			System.out.println("Post Save EJB calied....");
			System.out.println("Term Id is==>...."+termId);
			conn.setAutoCommit(false);
			dom = genericUtility.parseString( xmlData );
			parentNodeList = dom.getElementsByTagName("Detail1");
			parentNode = parentNodeList.item(0);
			childNodeList = parentNode.getChildNodes();
			childNodeListLength = childNodeList.getLength();
			for(ctr = 0; ctr < childNodeListLength; ctr++)
			{
				childNode = childNodeList.item(ctr);
				childNodeName = childNode.getNodeName();
				if ( childNodeName.equalsIgnoreCase( "attribute" ) )
				{
					update_flag = childNode.getAttributes().getNamedItem("updateFlag").getNodeValue();
				}
			}
			
			if( update_flag.equals("A") )
			{
				String pricelistMrp = genericUtility.getColumnValue( "price_list", dom );
				String pricelistItemCode = genericUtility.getColumnValue( "item_code", dom );
				String pricelistSlabNo = genericUtility.getColumnValue( "slab_no", dom );
				String pricelistUnit = genericUtility.getColumnValue( "unit", dom );
				String pricelistListype = genericUtility.getColumnValue( "list_type", dom );
				String pricelistEffRom = genericUtility.getColumnValue( "eff_from", dom );
				String pricelistValidUpto = genericUtility.getColumnValue( "valid_upto", dom );
				String pricelistLotNoF = genericUtility.getColumnValue( "lot_no__from", dom );
				String pricelistLotNoTo = genericUtility.getColumnValue( "lot_no__to", dom );
				String pricelistMinQty = genericUtility.getColumnValue( "min_qty", dom );
				String pricelistMaxQty = genericUtility.getColumnValue( "max_qty", dom );
				String pricelistRateype = genericUtility.getColumnValue( "rate_type", dom );
				String pricelistRate = genericUtility.getColumnValue( "rate", dom );
				String pricelistMinRate = genericUtility.getColumnValue( "min_rate", dom );
				String pricelistMaxRate = genericUtility.getColumnValue( "max_rate", dom );
				String pricelistOrderType = genericUtility.getColumnValue( "order_type", dom );
				String pricelistParent = genericUtility.getColumnValue( "price_list__parent", dom );
				String pricelistRefNo = genericUtility.getColumnValue( "ref_no", dom );
				String pricelistCalcBasis = genericUtility.getColumnValue( "calc_basis", dom );
				String pricelistChgRefNo = genericUtility.getColumnValue( "chg_ref_no", dom );
				String pricelistRefNOld = genericUtility.getColumnValue( "ref_no_old", dom );
				String pricelistChgUser = genericUtility.getColumnValue( "chg_user", dom );
				String pricelistChgTerm = genericUtility.getColumnValue( "chg_term", dom );
				
				String pricelstGen = "";
				double pricelstGencst = 0.0;
				double pricelstGenChrgRet = 0.0;
				double pricelstGenChrgBill = 0.0;
				double pricelstGenMargRet = 0.0;
				int updtCnt = 0;
				int plCount = 0;
				boolean flag = true;
				
				
				double minQty = pricelistMinQty == null ? 0.0 : Double.parseDouble(pricelistMinQty);
				double maxQty = pricelistMaxQty == null ? 9999999.0 : Double.parseDouble(pricelistMaxQty);
				double minRate = pricelistMinRate == null ? 0.0 : Double.parseDouble(pricelistMinRate);
				double maxRate = pricelistMaxRate == null ? 0.0 : Double.parseDouble(pricelistMaxRate);
				double pricelistRateDoub = pricelistRate == null ? 0.0 : Double.parseDouble(pricelistRate);
				int slabNo = pricelistSlabNo == null ? 0 : Integer.parseInt(pricelistSlabNo);
				
				java.sql.Timestamp effFrom = Timestamp.valueOf(genericUtility.getValidDateString(pricelistEffRom, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
				java.sql.Timestamp validUpto = Timestamp.valueOf(genericUtility.getValidDateString(pricelistValidUpto, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
				
				// logic merge by cpatil on 12/06/12 start
				
				PList = new HashMap();
				PList.put("price_list",pricelistMrp.trim());
				PList.put("item_code",pricelistItemCode.trim());
				PList.put("unit",pricelistUnit.trim().trim());
				PList.put("list_type",pricelistListype.trim());
				PList.put("slab_no",slabNo);
				PList.put("eff_from",effFrom);
				PList.put("valid_upto",validUpto);
				PList.put("lot_no__from",pricelistLotNoF.trim());
				PList.put("lot_no__to",pricelistLotNoTo.trim());
				PList.put("min_qty",minQty);
				PList.put("max_qty",maxQty);
				PList.put("rate",pricelistRateDoub);
				PList.put("rate_type",pricelistRateype);
				PList.put("min_rate",minRate);
				PList.put("max_rate",maxRate);
				PList.put("order_type",pricelistOrderType);
				PList.put("chg_ref_no",pricelistChgRefNo);
				PList.put("price_list__parent",pricelistParent);
				PList.put("calc_basis",pricelistCalcBasis);
				PList.put("ref_no",pricelistRefNo);
				PList.put("ref_no_old",pricelistRefNOld);
				
				System.out.println("lot_no__from@@"+PList.get("lot_no__from"));
				System.out.println("lot_no__to@@"+PList.get("lot_no__to"));
				System.out.println("item_code@@"+PList.get("item_code"));
				System.out.println("item_code@@"+itemCode);
				System.out.println("price_list@@"+PList.get("price_list"));
				System.out.println("price_list@@"+PriceList);
				
				
				
				PricelistGen.add(PList);
				System.out.println("@@@@@@@@@@@@@Price list@@@@@@@@@@@@@@@@"+PricelistGen);
				System.out.println("@@@@@@@@@@@@PList@@@@@@@@@@@@@"+PList);
				
				
				//PricelistGen.add(PList);
				//System.out.println("@@@@@@@@@@@@@Price list@@@@@@@@@@@@@@@@"+PricelistGen);
				
				 String str1=AuroExpPricelist(PList, PricelistGen, conn, xtraParams);
					System.out.println("function@@"+str1);
				
				sql = "SELECT count( 1 ) from pricelist where "
			            + " PRICE_LIST = ? "
						+ " AND ITEM_CODE = ? "
	                  //  +" AND LIST_TYPE = ? "
						+ " AND ( ? between EFF_FROM AND VALID_UPTO) " 
					//	+ " and  slab_no not in ( ? ) and unit not in ( ? ) and list_type not in ( ? ) ";
						+ " and  EFF_FROM not in ( ? ) " ;
					//+ "and VALID_UPTO not in ( ? ) ";    // added by cpatil 15/06/15
					chkPlpstmt = conn.prepareStatement( sql );
					chkPlpstmt.setString( 1, pricelistMrp.trim() );   // cpatil  pricelstGen
					chkPlpstmt.setString( 2, pricelistItemCode.trim() );
					//chkPlpstmt.setString( 3, pricelistListype.trim() );
					chkPlpstmt.setTimestamp (3, effFrom );
					chkPlpstmt.setTimestamp (4, effFrom );
				//	chkPlpstmt.setTimestamp (5, validUpto );
					
					chkPlrs = chkPlpstmt.executeQuery();
					if(chkPlrs.next())
					{
						plCount = chkPlrs.getInt( 1 );
					}
					chkPlrs.close();
					chkPlrs = null;
					chkPlpstmt.close(); //23feb19[pstmt closed and nulled on 12feb19]
					chkPlpstmt = null;
					System.out.println("@@@@@@ plCount["+plCount+"]");
					if(plCount > 0)
					{/*
						String str=insertPricelist(PList, PricelistGen, conn, xtraParams);
						System.out.println("function@@"+str);
						*/
						/*sql = "update pricelist set "
					     +" valid_upto = ? "
						 +" where price_list = ? "
						 +" AND item_code = ? "
						 //+" AND LIST_TYPE = ?"
						+ " AND ( ? between EFF_FROM AND VALID_UPTO) "
						+ " and  EFF_FROM not in ( ? ) " ;
						//+"and VALID_UPTO not in ( ? ) ";  // added by cpatil 15/06/15
						
						dateOverpstmt = conn.prepareStatement( sql );
					
						String effFromAppl = genericUtility.getValidDateString(effFrom.toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat());
						Calendar calender = Calendar.getInstance();
						java.text.SimpleDateFormat sdtf = new java.text.SimpleDateFormat( genericUtility.getApplDateFormat() );
						java.util.Date effFromCal = sdtf.parse( effFromAppl );
						calender.setTime( effFromCal );
						calender.add( calender.DATE, -1 );
						java.text.SimpleDateFormat  sdf =  new java.text.SimpleDateFormat( genericUtility.getDBDateFormat() );
						java.sql.Date effectFrom = java.sql.Date.valueOf( sdf.format( calender.getTime() ) );
						Timestamp effectFromTS = Timestamp.valueOf(genericUtility.getValidDateString(effectFrom.toString(), genericUtility.getDBDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");					
						
						dateOverpstmt.setTimestamp (1, effectFromTS );
						dateOverpstmt.setString( 2, pricelistMrp );    // cpatil  pricelstGen
						dateOverpstmt.setString( 3, pricelistItemCode );
						//dateOverpstmt.setString( 4, pricelistListype );
						dateOverpstmt.setTimestamp (4, effFrom );
						dateOverpstmt.setTimestamp (5, effFrom );
					//	dateOverpstmt.setTimestamp (6, validUpto );
						
						updtCnt = dateOverpstmt.executeUpdate();
					
						dateOverpstmt.close();
						dateOverpstmt = null;
						
						sql = "update pricelist set "
					     +" valid_upto = ? "
						 +" where price_list = ? "
						 +" AND item_code = ? "
						 +" AND ( ? between EFF_FROM AND VALID_UPTO) "
						 +" AND slab_no <> ? ";
						
						dateOverpstmt = conn.prepareStatement( sql );
						 
						dateOverpstmt.setTimestamp (1, effectFromTS );
						dateOverpstmt.setString( 2, pricelistMrp );
						dateOverpstmt.setString( 3, pricelistItemCode );
						//dateOverpstmt.setString( 4, pricelistListype );
						dateOverpstmt.setTimestamp (4, effFrom );
						dateOverpstmt.setInt (5, slabNo );
						
						updtCnt = dateOverpstmt.executeUpdate();
						
					*/
					
					}
				
				// end by cpatil   // added by cpatil 15/06/15
			//	if( plCount < 0 )  // added for testing 
			//	{	
				sql = "SELECT tran_id, price_list "
					 +" FROM pricelist_gen_hdr "
					 +" WHERE price_list__mrp = ? ";
			
				System.out.println( "Select Sql :: " + sql );
				pstmt = conn.prepareStatement( sql );
				pstmt.setString( 1, pricelistMrp );
				
				System.out.println( "Select Sql :: " + sql );
				rs = pstmt.executeQuery();
				while(rs.next())
				{
					String hdrTranId = rs.getString("tran_id");
					pricelstGen = rs.getString("price_list");
					
					sql = "SELECT cst_chrg, vat_chrg__retailer, "
					     +" vat_chrg_billing, margin_retailer "
					     +" FROM pricelist_gen_det where tran_id = ? ";
						 
					System.out.println( "Select Sql :: " + sql );
					dtlpstmt = conn.prepareStatement( sql );
					dtlpstmt.setString( 1, hdrTranId );
					System.out.println( "Select Sql :: " + sql );
					dtlrs = dtlpstmt.executeQuery();
					if( dtlrs.next() )
					{
						pricelstGencst = dtlrs.getDouble("cst_chrg");
						pricelstGenChrgRet = dtlrs.getDouble("vat_chrg__retailer");
						pricelstGenChrgBill = dtlrs.getDouble("vat_chrg_billing");
						pricelstGenMargRet = dtlrs.getDouble("margin_retailer");
					}
					dtlrs.close();//23feb19[rs closed and nulled on 12feb19]
					dtlrs = null;
					dtlpstmt.close();
					dtlpstmt = null;
				
					double rateCalc = calculateRate( pricelistRateDoub, pricelstGencst, pricelstGenChrgRet, pricelstGenChrgBill, pricelstGenMargRet );
					
					sql = "SELECT count( 1 ) from pricelist where "
			            +" PRICE_LIST = ? "
						+" AND ITEM_CODE = ? "
	                  //  +" AND LIST_TYPE = ? "
						+" AND ( ? between EFF_FROM AND VALID_UPTO) ";
					chkPlpstmt = conn.prepareStatement( sql );
					chkPlpstmt.setString( 1, pricelstGen.trim() );
					chkPlpstmt.setString( 2, pricelistItemCode.trim() );
					//chkPlpstmt.setString( 3, pricelistListype.trim() );
					chkPlpstmt.setTimestamp (3, effFrom );
					
					chkPlrs = chkPlpstmt.executeQuery();
					if(chkPlrs.next())
					{
						plCount = chkPlrs.getInt( 1 );
					}
					chkPlrs.close();
					chkPlrs = null;
					chkPlpstmt.close();//23feb19[rs closed and nulled on 12feb19]
					chkPlpstmt = null;
					if(plCount > 0)
					{
						
						String effFromAppl = genericUtility.getValidDateString(effFrom.toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat());
						Calendar calender = Calendar.getInstance();
						java.text.SimpleDateFormat sdtf = new java.text.SimpleDateFormat( genericUtility.getApplDateFormat() );
						java.util.Date effFromCal = sdtf.parse( effFromAppl );
						calender.setTime( effFromCal );
						calender.add( calender.DATE, -1 );
						java.text.SimpleDateFormat  sdf =  new java.text.SimpleDateFormat( genericUtility.getDBDateFormat() );
						java.sql.Date effectFrom = java.sql.Date.valueOf( sdf.format( calender.getTime() ) );
						Timestamp effectFromTS = Timestamp.valueOf(genericUtility.getValidDateString(effectFrom.toString(), genericUtility.getDBDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");					
						
						/*String str=insertPricelist( PList, PricelistGen, conn, xtraParams);
						System.out.println("function_test@@"+str);
						*/
						/*  // commented by cpatil beacause code shifted above i.e. before pricelist_gen_hdr sql 
						 
						 sql = "update pricelist set "
					     +" valid_upto = ? "
						 +" where price_list = ? "
						 +" AND item_code = ? "
						 //+" AND LIST_TYPE = ?"
						 +" AND ( ? between EFF_FROM AND VALID_UPTO) ";
						 
						 dateOverpstmt = conn.prepareStatement( sql );
					
						String effFromAppl = genericUtility.getValidDateString(effFrom.toString(), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat());
						Calendar calender = Calendar.getInstance();
						java.text.SimpleDateFormat sdtf = new java.text.SimpleDateFormat( genericUtility.getApplDateFormat() );
						java.util.Date effFromCal = sdtf.parse( effFromAppl );
						calender.setTime( effFromCal );
						calender.add( calender.DATE, -1 );
						java.text.SimpleDateFormat  sdf =  new java.text.SimpleDateFormat( genericUtility.getDBDateFormat() );
						java.sql.Date effectFrom = java.sql.Date.valueOf( sdf.format( calender.getTime() ) );
						Timestamp effectFromTS = Timestamp.valueOf(genericUtility.getValidDateString(effectFrom.toString(), genericUtility.getDBDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");					
						
						dateOverpstmt.setTimestamp (1, effectFromTS );
						dateOverpstmt.setString( 2, pricelstGen );
						dateOverpstmt.setString( 3, pricelistItemCode );
						//dateOverpstmt.setString( 4, pricelistListype );
						dateOverpstmt.setTimestamp (4, effFrom );
						
						updtCnt = dateOverpstmt.executeUpdate();
					
						dateOverpstmt.close();
						dateOverpstmt = null;
						
						*/
						sql = "update pricelist set "
					     +" valid_upto = ? "
						 +" where price_list = ? "
						 +" AND item_code = ? "
						 +" AND ( ? between EFF_FROM AND VALID_UPTO) "
						 +" AND slab_no <> ? ";
						
						dateOverpstmt = conn.prepareStatement( sql );
						 
						dateOverpstmt.setTimestamp (1, effectFromTS );
						dateOverpstmt.setString( 2, pricelistMrp );
						dateOverpstmt.setString( 3, pricelistItemCode );
						//dateOverpstmt.setString( 4, pricelistListype );
						dateOverpstmt.setTimestamp (4, effFrom );
						dateOverpstmt.setInt (5, slabNo );
						
						updtCnt = dateOverpstmt.executeUpdate();
						dateOverpstmt.close();//23feb19[rs closed and nulled on 12feb19]
						dateOverpstmt = null;
					}
					
					int slabNoUpdt = 1;
					String calcSlab = "SELECT max(SLAB_NO)+1 from PRICELIST where "
									 +" PRICE_LIST = ? "
									 +" AND ITEM_CODE = ? ";
									 //+" AND LIST_TYPE = ? ";
					chkPlpstmt = conn.prepareStatement( calcSlab );
					chkPlpstmt.setString( 1, pricelstGen);
					chkPlpstmt.setString(2, pricelistItemCode);
					
					chkPlrs = chkPlpstmt.executeQuery();
					if(chkPlrs.next())
					{
						slabNoUpdt = chkPlrs.getInt( 1 );
					}
					if( slabNoUpdt == 0 )
					{
						slabNoUpdt = 1;
					}
					
					chkPlrs.close();
					chkPlpstmt.close();
					chkPlrs = null;
					chkPlpstmt = null;
					
					insPriceListPStmt = conn.prepareStatement( insPriceList );
					System.out.println( "Select Sql :: " + insPriceListPStmt );
					
					insPriceListPStmt.setString(1, pricelstGen.trim());
					insPriceListPStmt.setString(2, pricelistItemCode.trim());
					insPriceListPStmt.setString(3, pricelistUnit.trim());
					insPriceListPStmt.setString(4, pricelistListype.trim());
					insPriceListPStmt.setInt(5,  slabNoUpdt);
					insPriceListPStmt.setTimestamp(6, effFrom);
					insPriceListPStmt.setTimestamp(7, validUpto);
					insPriceListPStmt.setString(8, pricelistLotNoF != null ? pricelistLotNoF.trim() : "");
					insPriceListPStmt.setString(9, pricelistLotNoTo != null ? pricelistLotNoTo.trim() : "");
					insPriceListPStmt.setDouble(10, minQty);
					insPriceListPStmt.setDouble(11, maxQty);
					insPriceListPStmt.setDouble(12, rate);
					insPriceListPStmt.setString(13, pricelistRateype != null ? pricelistRateype.trim() : "");
					insPriceListPStmt.setDouble(14, minRate);
					insPriceListPStmt.setTimestamp(15,getCurrdateAppFormat() );
					insPriceListPStmt.setString(16, userId != null ? userId.trim() : "");
					insPriceListPStmt.setString(17, termId != null ? termId.trim() : "");
					insPriceListPStmt.setDouble(18, maxRate);
					insPriceListPStmt.setString(19, pricelistOrderType != null ? pricelistOrderType.trim() : "");
					insPriceListPStmt.setString(20, pricelistParent != null ? pricelistParent.trim() : "");
					insPriceListPStmt.setString(21, pricelistChgRefNo != null ? pricelistChgRefNo.trim() : "");
					insPriceListPStmt.setString(22, pricelistCalcBasis != null ? pricelistCalcBasis.trim() : "");
					insPriceListPStmt.setString(23, pricelistRefNo != null ? pricelistRefNo.trim() : "");
					insPriceListPStmt.setString(24, pricelistRefNOld != null ? pricelistRefNOld.trim() : "");
					
					updtCnt = insPriceListPStmt.executeUpdate();
					System.out.println("updtCnt@@@"+updtCnt);
					flag = false;
					insPriceListPStmt.close();
					insPriceListPStmt = null;
					
					/* String upDtPl = "Update pricelist set eff_from = ?, valid_upto = ? where price_list = ? ";
					upDtPlPStmt = conn.prepareStatement( upDtPl );
					upDtPlPStmt.setTimestamp( 1,effFrom );
					upDtPlPStmt.setTimestamp( 2,validUpto );
					upDtPlPStmt.setString( 3, pricelstGen );
					int updt = upDtPlPStmt.executeUpdate();
					upDtPlPStmt.close();
					upDtPlPStmt = null; */
					//retString = "VTSUCCSS";
					retString = "VTSUCCSS";
				}
				pstmt.close();
				pstmt = null;
			
		//	} // added for testing	
				retString = "VTSUCCSS";
			}
			//retString = "VTSUCCSS";
		}
		catch (Exception e)
		{
			try
			{
				System.out.println("@@@@@@@@ conn.rollback().........");
				conn.rollback();
				e.printStackTrace();
			}
			catch(Exception e1)
			{
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if ( pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if ( insPriceListPStmt != null)
				{
					insPriceListPStmt.close();
					insPriceListPStmt = null;
				}
				if(retString.equals("VTSUCCSS"))
				{
					System.out.println("@@@@@@@@ conn.commit().........");
					retString = itmDBAccessEJB.getErrorString("",retString,userId,"",conn);
					conn.commit();
				}
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}
		}
		//return retString;
		return "";
	}
	
	public double  calculateRate( double rate,double cstChrg, double vatChrgRetailer, double vatChrgBilling, double marginRetailer )
	{
		double disneyBasicPrice = 0.0;
		double totBasicPriceWithVatDisInv = 0.0;
		double vatNotional = 0.0;
		double finalRate = 0.0;
		
		try
		{
			//Calculation by skale start
			
			double vatDisBillg = vatChrgBilling;
			double cstDisBillg = cstChrg;
			double mrpRate = rate;
			double basicMargin = marginRetailer;
			double retBasicMargin = ( mrpRate * ( basicMargin / 100) );
			double mrpRetailerMargin = mrpRate - retBasicMargin ;
			double vatRetailerBillg = vatChrgRetailer;
			
			if( vatDisBillg != 0)
			{
				totBasicPriceWithVatDisInv = mrpRetailerMargin ;
				double vatChargedByDis = totBasicPriceWithVatDisInv * ((vatDisBillg / 100) / ( 1 + ( vatDisBillg / 100 ) ));
				disneyBasicPrice = totBasicPriceWithVatDisInv - vatChargedByDis ;
			}
			else
			{
				vatNotional = mrpRetailerMargin * ( (vatRetailerBillg/100) / ( 1 + (vatRetailerBillg/100) ));
				double cstChargedByDis = ( mrpRetailerMargin - vatNotional ) * ( (cstDisBillg/100) / ( 1 + (cstDisBillg/100) ));
				disneyBasicPrice = mrpRetailerMargin - cstChargedByDis - vatNotional ;
				totBasicPriceWithVatDisInv = disneyBasicPrice + cstChargedByDis;
			}
			finalRate = Double.parseDouble(df.format(disneyBasicPrice));
			//Calculation by skale end
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return finalRate;
	}
	private Timestamp getCurrdateAppFormat()
    {
        String s = "";	
		 Timestamp timestamp = null;		
       // GenericUtility genericUtility = GenericUtility.getInstance();
        try
        {
            java.util.Date date = null;
            timestamp = new Timestamp(System.currentTimeMillis());
            
            SimpleDateFormat simpledateformat = new SimpleDateFormat(genericUtility.getDBDateFormat());
            date = simpledateformat.parse(timestamp.toString());
            timestamp = Timestamp.valueOf(simpledateformat.format(date).toString() + " 00:00:00.0");
		}
        catch(Exception exception)
        {
            System.out.println("Exception in  getCurrdateAppFormat " + exception.getMessage());
        }
        return timestamp;
    }
	
	private String AuroExpPricelist(HashMap PList,ArrayList PricelistGen, Connection conn, String xtraParams) throws RemoteException, ITMException
	{
	
		PreparedStatement pstmtSql = null,pstmtInsert= null;
		ResultSet rs = null;
		String dbName = "";
		String sql = "";
		String retString = "", LotNoFrom1="" ;
		String errCode="",AutoExpire="",LotFrBrow="",LotNoToBrow="",LotNoFrom="",OldLotNo="",Set="",NewStr="",edioption="";
		//char OrigStr="";
		int DayDiff=0;
		java.sql.Timestamp ValidUpTo=null,EffDate=null;
		int LineNo=0,RowCount=0,Count=0,OldLen=0;
		int New=0;
		double SlabNo=0.0;
		double NewLen=0.0,slabno=0.0,cnt=0.0;
		double MinQtyBrow=0.0;
		double MaxQtyBrow=0.0;
		double minrate=0.0;
		double rate1=0.0;
		double minqty=0.0,maxqty=0.0;
		String ratetype="";
		String refNo1="";
		String refNoold1="";
		String chgref1="";
		double maxrate1=0.0;
		String PriceListParent1="",orderType1="";
		int Ctr=0,countup=0,Len=0,Len1=0,sizeCnt=0;
		java.sql.Timestamp ChgDate=null,efffromdt=null,validupto=null;
		java.sql.Timestamp chgDate = null,efffrom=null,today=null;
		String lotnofr="",lotnoto="",currentlotnofr="",currentlotnoto="",nextlotnofr="",LotNoTo="",currentlotnofrtemp="",currentlotnototemp="";
		//String LotNoFrom="",LotNoTo="";
		boolean recordfound;
		//HashMap PlGen=null;
		String plist="",itemcode="",unit="",listtype="";
		String plist1="",refNoold="",OrigStr="";
		int cnt1=0,UpdCnt=0,recCnt=0;
		String LotNoFroma[] = null, LotNoToa[]=null;
		char left1=0,left2=0,left3 = 0,left4=0;
		int updCnt = 0;
		char left=0;
		String mid1="",mid2="",mid3="",mid4="";
		String mid="";
		java.text.SimpleDateFormat sdf = null;
		String lotnofrom="",lotnoto1="",userId="",termId="";
		userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
		termId = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "termId" );
		try
		{
			DistCommon distCommon = new DistCommon();
			ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
			dbName = CommonConstants.DB_NAME;
			System.out.println("Insert Price List@@@@@@@@@@@");
		AutoExpire=distCommon.getDisparams("999999","AUTO_EXPIRE_PL", conn);
		System.out.println("Autoexpire Value is:"+AutoExpire);
		
		if (AutoExpire == null)
		{
			AutoExpire = "";
		}
		else if(AutoExpire.equalsIgnoreCase("Y"))
		{
			System.out.println("Insert Autoexpire@@@@@@@@@");
			//PList=new HashMap();
			plist = (String)PList.get("price_list");
			System.out.println("Price list value is"+plist);
			itemcode = (String)PList.get("item_code");
			System.out.println("item code in price list"+itemcode);
			unit = (String)PList.get("unit");
			listtype = (String)PList.get("list_type");
			//rate1=(Double)PList.get("rate");
			//ratetype=(String)PList.get("rate_type");
			//minrate=(Double)PList.get("min_rate");
			//maxrate1=(Double)PList.get("max_rate");
			
			PricelistGen.add(PList);
			System.out.println("List size is"+PricelistGen);
			lotnofrom=(String)PList.get("lot_no__from");
			System.out.println("lot no from "+lotnofrom);
			lotnoto1=(String)PList.get("lot_no__to");
			System.out.println("lot no to "+lotnoto1);
			efffrom=(java.sql.Timestamp)PList.get("eff_from");
			System.out.println("List Sizeeeeeee"+PricelistGen.size());
			minqty=(Double)PList.get("min_qty");
			System.out.println("min qty in list"+minqty);
			maxqty=(Double)PList.get("max_qty");
			rate1=(Double)PList.get("rate");
			ratetype=(String)PList.get("rate_type");
			minrate=(Double)PList.get("min_rate");
			maxrate1=(Double)PList.get("max_rate");
			refNo1=(String)PList.get("ref_No");
			refNoold1=(String)PList.get("ref_No_old");
			PriceListParent1=(String)PList.get("Price_List__Parent");
			orderType1=(String)PList.get("order_Type");
			chgref1=(String)PList.get("chg_ref_no");

			System.out.println("plist@@"+plist);
			
			
					//SELECT COUNT(1) FROM pricelist WHERE PRICE_LIST=? AND ITEM_CODE=?;
			
			sql="SELECT COUNT(1) FROM pricelist WHERE PRICE_LIST=? AND ITEM_CODE=?";
			pstmtSql = conn.prepareStatement(sql);
			pstmtSql.setString(1,plist);
			pstmtSql.setString(2,itemcode);
			
			rs = pstmtSql.executeQuery();
			while(rs.next())
			{
				recCnt=rs.getInt(1);
			}
			System.out.println("recCnt@@"+recCnt);
			pstmtSql.close();
			pstmtSql=null;
			rs.close();
			rs=null;
			sql=null;
			if(recCnt==0)
			{
				sql="insert into pricelist (price_list,item_code,unit,list_type,slab_no,eff_from,valid_upto,lot_no__from,lot_no__to,min_qty,max_qty,rate,rate_type,min_rate,chg_date,chg_user,chg_term,max_rate,order_type,price_list__parent,	chg_ref_no,	ref_no,ref_no_old  )" +
							"VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
					pstmtInsert = conn.prepareStatement(sql);
					pstmtInsert.setString(1, plist);
					pstmtInsert.setString(2, itemcode);
					pstmtInsert.setString(3, unit);
					pstmtInsert.setString(4, listtype);
					pstmtInsert.setDouble(5, (Double)PList.get("slab_no"));//maxrate1=(Double)PList.get("slab_no");
					pstmtInsert.setTimestamp(6, efffrom);
					pstmtInsert.setTimestamp(7, ValidUpTo);
					pstmtInsert.setString(8,lotnofrom);//lotnofr)
					pstmtInsert.setString(9, lotnoto1);//lotnoto)
					pstmtInsert.setDouble(10, minqty);
					pstmtInsert.setDouble(11, maxqty);
					pstmtInsert.setDouble(12, rate1);
					pstmtInsert.setString(13, ratetype);
					pstmtInsert.setDouble(14, minrate);
					chgDate = new java.sql.Timestamp(System.currentTimeMillis());
					pstmtInsert.setTimestamp(15, chgDate);
					pstmtInsert.setString(16, userId);
					pstmtInsert.setString(17, termId);
					pstmtInsert.setDouble(18, maxrate1);
					pstmtInsert.setString(19, orderType1);
					pstmtInsert.setString(20, PriceListParent1);
					pstmtInsert.setString(21, chgref1);
					pstmtInsert.setString(22, refNo1);
					pstmtInsert.setString(23, refNoold1);
				    UpdCnt = pstmtInsert.executeUpdate();
				    System.out.println("NewRecord@@Inserted"+UpdCnt);
					pstmtInsert.close();
				pstmtInsert = null;			
				
			}
			
			
			
			sql="select count(1) from pricelist where price_list=? and item_code=? and unit=? and list_type=? and  EFF_FROM not in ( ? ) ";
			pstmtSql = conn.prepareStatement(sql);
			pstmtSql.setString(1,plist);
			pstmtSql.setString(2,itemcode);
			pstmtSql.setString(3,unit);
			pstmtSql.setString(4,listtype);
			pstmtSql.setTimestamp(5, efffrom);
			rs = pstmtSql.executeQuery();
			while(rs.next())
			{
				sizeCnt=rs.getInt(1);
			}
			System.out.println("sizeCnt@@"+sizeCnt);
			pstmtSql.close();
			pstmtSql=null;
			rs.close();
			rs=null;
			sql=null;
			
			sql="select valid_upto,eff_from,slab_no,lot_no__from,lot_no__to,min_qty,max_qty from pricelist where price_list=? and item_code=? and unit=? and list_type=? and  EFF_FROM not in ( ? ) ";
			pstmtSql = conn.prepareStatement(sql);
			pstmtSql.setString(1,plist);
			pstmtSql.setString(2,itemcode);
			pstmtSql.setString(3,unit);
			pstmtSql.setString(4,listtype);
			pstmtSql.setTimestamp(5, efffrom);
			rs = pstmtSql.executeQuery();
			for (Ctr =0; Ctr < sizeCnt; Ctr++)
			{
				
				System.out.println("Insert For loooopppp");
				//PList=new HashMap();
				//PList = (HashMap)PricelistGen.get(Ctr);
				if(rs.next())
				{
				ValidUpTo=rs.getTimestamp("valid_upto");
				System.out.println("Valid upto in price list"+ValidUpTo);
				EffDate=rs.getTimestamp("eff_from");
				SlabNo=rs.getDouble("slab_no");
				System.out.println("slabbbb_no1111111111111111111111"+SlabNo);
				LotFrBrow=rs.getString("lot_no__from");
				System.out.println("Lots Number from brow window"+LotFrBrow);
				LotNoToBrow=rs.getString("lot_no__to");
				System.out.println("Lots Number from brow window"+LotNoToBrow);
				MinQtyBrow=rs.getDouble("min_qty");
				MaxQtyBrow=rs.getDouble("max_qty");
				System.out.println("Minimum value is@@@@@@@@@"+MinQtyBrow);
				}
				System.out.println("check if conditionn");
				DayDiff=(int)utilMethods.DaysAfter(ValidUpTo,efffrom);
				System.out.println("check"+DayDiff);
			/*	if(DayDiff<=0 ) 
			     { 
					System.out.println("In if condition@@@@@");
					*/
				System.out.println("lotnofrom@@"+lotnofrom+"LotFrBrow@@"+LotFrBrow);
				System.out.println("lotnoto1@@"+lotnoto1+"LotNoToBrow@@"+LotNoToBrow);
				System.out.println("MinQtyBrow@@"+MinQtyBrow+"minqty@@"+minqty);
				System.out.println("MaxQtyBrow@@"+MaxQtyBrow+"maxqty@@"+maxqty);
				/*
				 * if((DayDiff<=0 )&& (LotFrBrow.equals( lotnofrom)|| lotnofrom.equalsIgnoreCase(LotFrBrow)) && (LotNoToBrow.equals(lotnoto1)|| LotNoToBrow.equalsIgnoreCase(lotnoto1)  ) && (MinQtyBrow==minqty)&& (MaxQtyBrow==maxqty)) 
				     {
				 */
				
					if((DayDiff<=0 )) 
				     { 
						System.out.println("DayDiff@indaycondition@@"+DayDiff);
						if((LotFrBrow.trim().equals( lotnofrom.trim())|| lotnofrom.trim().equalsIgnoreCase(LotFrBrow.trim())))
						{
							System.out.println("iNcONDITION@@lotnofrom@@"+lotnofrom+"LotFrBrow@@"+LotFrBrow);
							if((LotNoToBrow.trim().equals(lotnoto1.trim())|| LotNoToBrow.trim().equalsIgnoreCase(lotnoto1.trim())  ))
							{
								System.out.println("iNcONDITION@@lotnofrom@@"+lotnofrom+"LotFrBrow@@"+LotFrBrow);
								if((MinQtyBrow==minqty))
								{
									System.out.println("iNcONDITION@@MinQtyBrow@@"+MinQtyBrow+"minqty@@"+minqty);
									if((MaxQtyBrow==maxqty))
									{
										
										System.out.println("iNcONDITION@@MaxQtyBrow@@"+MaxQtyBrow+"maxqty@@"+maxqty);
										
										
										//System.out.println("In if condition@@@@@");
										System.out.println("In if condition ######");
									ValidUpTo=utilMethods.RelativeDate((java.sql.Timestamp)PList.get("eff_from"), -1 );
									System.out.println("ValidUpTo@@@@@"+ValidUpTo);
									if( dbName.equalsIgnoreCase("db2"))
									{
										validupto=(ValidUpTo);
										System.out.println("Valid upto date is:"+ValidUpTo);
										System.out.println("Converted Valid Upto Date is:"+validupto);
										sql="update pricelist set  valid_upto = ? , chg_user =?,chg_term = ?,chg_date = ?  where price_list =? and item_code = ? and unit = ? and list_type = ? and slab_no = ? and  EFF_FROM not in ( ? ) ";
										pstmtSql=conn.prepareStatement(sql);
										pstmtSql.setTimestamp(1, validupto);
										
										pstmtSql.setString(2, userId);
      								    pstmtSql.setString(3, termId);
      								    pstmtSql.setTimestamp(4, chgDate);
										
										pstmtSql.setString(5, plist);
										pstmtSql.setString(6, itemcode);
										pstmtSql.setString(7, unit);
										pstmtSql.setString(8, listtype);
										pstmtSql.setDouble(9, SlabNo);
										pstmtSql.setTimestamp(10, efffrom);
										int upcnt1=pstmtSql.executeUpdate();	
										System.out.println("No. of rows updated------>>"+upcnt1);
										if(pstmtSql !=null)
										{
											pstmtSql.close();
											pstmtSql=null;
										}
									}
									else 
									{
										sql="update pricelist set  valid_upto = ?  , chg_user =?,chg_term = ?,chg_date = ?  where price_list =? and item_code = ? and unit = ? and list_type = ? and slab_no = ? and  EFF_FROM not in ( ? ) ";
										pstmtSql=conn.prepareStatement(sql);
										pstmtSql.setTimestamp(1, ValidUpTo);
										
										pstmtSql.setString(2, userId);
	      								pstmtSql.setString(3, termId);
	      								pstmtSql.setTimestamp(4, chgDate);
										
										pstmtSql.setString(5, plist);
										pstmtSql.setString(6, itemcode);
										pstmtSql.setString(7, unit);
										pstmtSql.setString(8, listtype);
										pstmtSql.setDouble(9, SlabNo);
										pstmtSql.setTimestamp(10, efffrom);
										int upcnt1=pstmtSql.executeUpdate();	
										System.out.println("No. of rows updated------>>"+upcnt1);
										if(pstmtSql !=null)
										{
											pstmtSql.close();
											pstmtSql=null;
										}
										
									}
										
										
										
									}
									
								}
							}
							
						}
						
						
						
						
						
						
					
				 }
			/*}*/
					
				
			}/// for end
			
		}// end autoexpire if
		///////////////
		// astr_PL.list_type = 'B'
	      listtype = (String)PList.get("list_type");
	      System.out.println("check price listttttttttttttttt type"+listtype);
		  if(listtype.equalsIgnoreCase("B"))
		   {
			LotNoFrom=(String)PList.get("lot_no__from");
			System.out.println("@@@@@lot number from["+LotNoFrom+"]");
			sql="select count(1) from pricelist where price_list = ? and item_code = ? and unit = ? and list_type = ? and lot_no__from <= ? and lot_no__to >= ? and min_qty <= ? and max_qty >= ?";
			pstmtSql=conn.prepareStatement(sql);
			pstmtSql.setString(1,plist);
			pstmtSql.setString(2,itemcode);
			pstmtSql.setString(3,unit);
			pstmtSql.setString(4,listtype);
			pstmtSql.setString(5,lotnofrom);
			pstmtSql.setString(6,lotnoto1);
			pstmtSql.setDouble(7,minqty);
			pstmtSql.setDouble(8,maxqty);
    		rs=pstmtSql.executeQuery();
    		if(rs.next())
    		{
    			Count=rs.getInt(1);
    		}
    		System.out.println("llCount-------->>["+Count+"]");
    		if(rs != null)
			{
				rs.close();
				rs=null;
			}
			if(pstmtSql !=null)
			{
				pstmtSql.close();
				pstmtSql=null;
			}
			
    		if(Count > 0)
    		{
    			
    			System.out.println("LotNoFrom length["+LotNoFrom.length()+"]");
    			String s2=LotNoFrom.trim();
    		    System.out.println("Lotnumber.trim() length["+s2.length()+"]");
    		    left=0;
    		    char right= s2.charAt(s2.length()-1);
    		    int diff3=0;
    		    String original="", result = "",result2="";
    		    int length = LotNoFrom.length();
    			
    			
    			/*System.out.println("insert in count");
    			Len=LotNoFrom.trim().length();
    			Ctr=1;
    			String s2=LotNoFrom.trim();
    			//String chekStr=LotNoFrom.trim();
*/    			
    			System.out.println("s2 ["+s2+"]");
    			
    			
    			//char right= s2.charAt(s2.length()-1);
    			
    			System.out.println("right s2 ["+right+"]");
    			//Pavan Rane 23aug19[to set allow alphbet at right side]....start
    			if(!isNumber(right))
    			{
    				LotNoFrom ="";
    				for ( int i = 0;  i <= s2.length()-2 ; i++ )
    				{    						 
    					 LotNoFrom = LotNoFrom + s2.charAt(i); 
    					 System.out.println("LotNoFrom......["+LotNoFrom+"]");    						
    				}     				
    				//LotNoFrom = LotNoFrom+(int)right ;
    				int asciiChar = (int)right;		
    				System.out.println("--asciiChar--::["+asciiChar+"]");
    				char prevChar = (char) (asciiChar-1);
    				System.out.println("--Char--::["+asciiChar+"]");
    				LotNoFrom = LotNoFrom + String.valueOf(prevChar);    				
    				System.out.println("1028 if char LotNoFrom["+LotNoFrom+"]");
    			}else 
    			{
    				//Pavan Rane 23aug19[to set allow alphbet at right side]....end
    			cnt1 = 0;  //Pavan Rane 20may19[to initialize count] 
    			 for ( int i = length - 1 ; i >= 0 ; i-- )
    			    {
    			        Character character  = LotNoFrom.charAt(i);
    			        System.out.println("Char ["+character+"]");

    			        boolean flag = isNumber(character);

    			        if(!flag)
    			        {
    			            result = result + LotNoFrom.charAt(i);
    			            System.out.println("result.....["+result+"]");
    			            break;
    			        }
    			        System.out.println("result["+result+"]");
    			        cnt1++;
    			        System.out.println("Count is["+cnt1+"]");
    			    }
    			    System.out.println("Result of entered string is: ["+result+"]");
    			    System.out.println("Count is:::["+cnt1+"]");
    			    int testOrginal=0;
    			    int sub=0;
    			    testOrginal=length-cnt1;

    			    System.out.println("testOrginal["+testOrginal+"]");
    			    String testSub1=LotNoFrom.substring(0,testOrginal);
    			    System.out.println("testSub1::-["+testSub1+"]");
    			    int testSub=testSub1.length();
    			    System.out.println("testSub["+testSub+"]");
    			    String testSub2=LotNoFrom.substring(s2.length()-cnt1);
    			    System.out.println("testSub2::-["+testSub2+"]");
    			    sub=Integer.parseInt(testSub2);
    			    System.out.println("sub::["+testSub2+"]");
    			    int u2=testSub2.length();
    			    System.out.println("u2::["+u2+"]");
    			    int OrgSub=sub-1;
    			    System.out.println("OrgSub::["+OrgSub+"]");
    			    String v1=String.valueOf(OrgSub);
    			    System.out.println("v1::["+v1+"]");
    			    int v2=v1.length();
    			    System.out.println("v2::["+v2+"]");
    			    //System.out.println("V2:"+v2);
    			    int z=u2-v2;
    			    System.out.println("z:["+z+"]");
    			    if(z!=0)
    			    {
    			        for(int s3=0;s3<z;s3++)
    			        {
    			            System.out.println("For loop if ");
    			            NewStr=NewStr.concat("0");
    			            System.out.println("@@@@@@@@@New String:::::::::::["+NewStr.length()+"]");
    			        }
    			    }
    			    int q=NewStr.length();
    			    String a2=Integer.toString(OrgSub);
    			    System.out.println("NewStr["+NewStr+"]");
    			    LotNoFrom = testSub1.trim()+NewStr.trim()+a2 ;
    			    System.out.println("LotNoFrom1@@@@LotNoFrom@@@@:["+LotNoFrom+"]");
    			    
    			}
    			    
    			    
    			    sql="update pricelist set lot_no__to = ? , chg_user =?,chg_term = ?,chg_date = ? where price_list = ? and item_code = ? and unit = ? and list_type = ? and lot_no__from <= ? and lot_no__to >= ? and min_qty <= ? and max_qty >= ? and  EFF_FROM not in ( ? ) ";
    				pstmtSql=conn.prepareStatement(sql);
					pstmtSql.setString(1,LotNoFrom);
					
					pstmtSql.setString(2, userId);
					pstmtSql.setString(3, termId);
					pstmtSql.setTimestamp(4, chgDate);
					
					pstmtSql.setString(5, plist);
					pstmtSql.setString(6, itemcode);
					pstmtSql.setString(7, unit);
					pstmtSql.setString(8,listtype);
					pstmtSql.setString(9, lotnofrom);
					pstmtSql.setString(10, lotnoto1);//NEW UPDATE//OLD-lotnofrom
					pstmtSql.setDouble(11, minqty);
					pstmtSql.setDouble(12, maxqty);
					pstmtSql.setTimestamp(13, efffrom);
					int upcnt1=pstmtSql.executeUpdate();	
					System.out.println("No. of rows updated@@@@@@@@------>>["+upcnt1+"]");
					if(pstmtSql !=null)
					{
						pstmtSql.close();
						pstmtSql=null;
					}
    			    
    			    

    		}
		}
		//to expire the pricelist in case of revise circular of old circular.
		//change the valid upto date of price before the eff from date and batch no less then the 
		//plist1 = (String)PList.get("price_list");
		refNoold=(String)PList.get("ref_no_old");
		System.out.println("Reference Number old is:["+refNoold+"]");
		 if(listtype.equalsIgnoreCase("B"))
		{
			LotNoFrom=(String)PList.get("lot_no__from");
			System.out.println("@@@@@lot number from["+LotNoFrom+"]");
			if(refNoold !=null && refNoold.trim().length() > 0)
			{
				sql="select count(*) from pricelist  where price_list= ? and item_code = ? and unit = ?  and lot_no__from >= ? and list_type = ?   and min_qty >= ? and max_qty <= ? and ref_no  = ?";
				pstmtSql=conn.prepareStatement(sql);
				pstmtSql.setString(1, plist);
				pstmtSql.setString(2, itemcode);
				pstmtSql.setString(3, unit);
				pstmtSql.setString(4, lotnofrom.trim());
				pstmtSql.setString(5, listtype);
				pstmtSql.setDouble(6, minqty);
				pstmtSql.setDouble(7, maxqty);
				pstmtSql.setString(8, refNoold);
				rs = pstmtSql.executeQuery();
				System.out.println("COUNT"+countup);
				if (rs.next())
				{
					countup = rs.getInt(1);
				}
				
				rs.close();
				rs = null;
				pstmtSql.close();
				pstmtSql = null;
				
			}
			else
			{
				sql="select count(*) from pricelist  where price_list= ? and item_code = ? and unit = ?  and lot_no__from >= ? and list_type = ?   and min_qty >= ? and max_qty <= ? and ref_no is null ";
				pstmtSql=conn.prepareStatement(sql);
				pstmtSql.setString(1, plist);
				pstmtSql.setString(2, itemcode);
				pstmtSql.setString(3, unit);
				pstmtSql.setString(4, lotnofrom.trim());
				pstmtSql.setString(5, listtype);
				pstmtSql.setDouble(6, minqty);
				pstmtSql.setDouble(7, maxqty);
				//pstmtSql.setString(8, refNoold);
				rs = pstmtSql.executeQuery();
				System.out.println("COUNT"+countup);
				if (rs.next())
				{
					countup = rs.getInt(1);
				}
				
				rs.close();
				rs = null;
				pstmtSql.close();
				pstmtSql = null;
			}
			if(countup>0)
			{
				if(refNoold !=null && refNoold.trim().length() > 0)
				{
					sql="select slab_no,eff_from ,lot_no__from  from  pricelist where price_list = ? and item_code = ? and unit = ? and lot_no__from >= ? and list_type = ? and min_qty >= ? and max_qty <= ? and ref_no  = ?";
					pstmtSql.setString(1, plist);
					pstmtSql.setString(2, itemcode);
					pstmtSql.setString(3, unit);
					pstmtSql.setString(4, lotnofrom.trim());
					pstmtSql.setString(5, listtype);
					pstmtSql.setDouble(6, minqty);
					pstmtSql.setDouble(7, maxqty);
					pstmtSql.setString(8, refNoold);
					
					rs = pstmtSql.executeQuery();
					if (rs.next())
					{
						
						slabno=rs.getDouble(1);
						efffromdt=rs.getTimestamp(2);
						LotNoFrom=rs.getString(3);
						System.out.println("LotNoFrom@@"+LotNoFrom);
							}
					rs.close();
					rs = null;
					pstmtSql.close();
					pstmtSql = null;
					}
				else
				{
					sql="select slab_no,eff_from ,lot_no__from  from  pricelist where price_list = ? and item_code = ? and unit = ? and lot_no__from >= ? and list_type = ? and min_qty >= ? and max_qty <= ? and ref_no is null";
					pstmtSql=conn.prepareStatement(sql);
					
					pstmtSql.setString(1, plist);
					pstmtSql.setString(2, itemcode);
					pstmtSql.setString(3, unit);
					pstmtSql.setString(4, lotnofrom.trim());
					pstmtSql.setString(5, listtype);
					pstmtSql.setDouble(6, minqty);
					pstmtSql.setDouble(7, maxqty);
					//pstmtSql.setString(8, refNoold);
					
					rs = pstmtSql.executeQuery();
					if (rs.next())
					{
						slabno=rs.getDouble(1);
						efffromdt=rs.getTimestamp(2);
						LotNoFrom=rs.getString(3);
						System.out.println("LotNoFrom@@"+LotNoFrom);
						//refNoold=rs.getString(8);
					}
					rs.close();
					rs = null;
					pstmtSql.close();
					pstmtSql = null;
					}
				
				LotNoFrom=LotNoFrom.trim();
			ValidUpTo=utilMethods.RelativeDate((efffromdt), -1 );
			System.out.println("insert in count");
			System.out.println("insert in count");
			/*Len=LotNoFrom.trim().length();
			Ctr=1;
			*/
			System.out.println("@@LotNoFrom@@@@@["+LotNoFrom+"]");
			System.out.println("@@LotNoFrom.length()["+LotNoFrom.length()+"]");
			String s2=LotNoFrom.trim();
		    System.out.println("Lotnumber.trim().length["+s2.length()+"]");
		    left=0;
		    char right= s2.charAt(LotNoFrom.length()-1);
		    int diff3=0,cntt=0;
		    String original="", result = "",result2="";
		    int length = LotNoFrom.trim().length();
			    			
			System.out.println("s22222222222222["+s2.trim()+"]");
			
			
			//char right= s2.charAt(s2.length()-1);
			//Pavan Rane 23aug19[to set allow alphbet at right side]....start
			if(!isNumber(right))
			{
				LotNoFrom ="";
				for ( int i = 0;  i <= s2.length()-2 ; i++ )
				{					
					 LotNoFrom = LotNoFrom + s2.charAt(i); 
					 System.out.println("LotNoFrom......["+LotNoFrom+"]");						
				} 				
				//LotNoFrom = LotNoFrom+(int)right ;
				int asciiChar = (int)right;		
				System.out.println("--asciiChar--::["+asciiChar+"]");
				char prevChar = (char) (asciiChar-1);
				System.out.println("--Char--::["+asciiChar+"]");
				LotNoFrom = LotNoFrom + String.valueOf(prevChar);				
				System.out.println("1028 if char LotNoFrom["+LotNoFrom+"]");
			}else 
			{
			//Pavan Rane 23aug19[to set allow alphbet at right side]....end			
			System.out.println("right s22222222222222["+right+"]");
			cntt = 0; //Pavan Rane 20may19[to initialize count]
			 for ( int i = length - 1 ; i >= 0 ; i-- )
			    {
			        Character character  = LotNoFrom.charAt(i);
			        System.out.println("Char"+character);

			        boolean flag = isNumber(character);

			        if(!flag)
			        {
			            result = result + LotNoFrom.charAt(i);
			            break;
			        }
			        System.out.println("result["+result+"]");
			        cntt++;
			        System.out.println("Count is["+cntt+"]");
			    }
			    System.out.println("Result of entered string is: ["+result+"]");
			    System.out.println("Count is:::["+cntt+"]");
			    int testOrginal=0;
			    int sub=0;
			    testOrginal=length-cntt;

			    System.out.println("testOrginal["+testOrginal+"]");
			    String testSub1=LotNoFrom.substring(0,testOrginal)==null?"":LotNoFrom.substring(0,testOrginal).trim();
			    System.out.println("testSub1::-["+testSub1+"]");
			    int testSub=testSub1.length();
			    System.out.println("testSub["+testSub+"]");
			    String testSub21=LotNoFrom.substring(LotNoFrom.length()-cntt);
			    System.out.println("testSub2::-["+testSub21+"]");
			    sub=Integer.parseInt(testSub21);
			    System.out.println("SUB@@@["+sub+"]");
			    int u2=testSub21.length();
			    System.out.println("u2["+u2+"]");
			    int OrgSub=sub-1;
			    String v1=String.valueOf(OrgSub);
			    int v2=v1.length();
			    System.out.println("V2:["+v2+"]");
			    int z=u2-v2;
			    System.out.println("z:["+z+"]");
			    if(z!=0)
			    {
			        for(int s3=0;s3<z;s3++)
			        {
			            System.out.println("For loop if ");
			            NewStr=NewStr.concat("0");
			            System.out.println("@@@@@@@@@New String:::::::::::"+NewStr.length()+"]");
			        }
			    }
			    int q=NewStr.length();
			    String a2=Integer.toString(OrgSub);
			    System.out.println("NewStr"+NewStr+"]");
			    LotNoFrom = testSub1.trim()+NewStr.trim()+a2 ;
			    System.out.println("LotNoFrom1@@@@LotNoFrom@@@@:"+LotNoFrom+"]");
			    
			}
		
				if( dbName.equalsIgnoreCase("db2"))
				{
					validupto=(ValidUpTo);
					if(refNoold !=null && refNoold.trim().length() > 0)
					{
						sql="update pricelist set valid_upto = ? ,lot_no__to = ? , chg_user =?,chg_term = ?,chg_date = ? where price_list = ? and item_code = ? and unit =? and list_type = ? and min_qty >= ? and max_qty <= ? and slab_no = ? and ref_no  =? and  EFF_FROM not in ( ? ) ";
						pstmtSql=conn.prepareStatement(sql);
						pstmtSql.setTimestamp(1, validupto);
						pstmtSql.setString(2, LotNoFrom);
						
						pstmtSql.setString(3, userId);
						pstmtSql.setString(4, termId);
						pstmtSql.setTimestamp(5, chgDate);
						
						pstmtSql.setString(6, plist);
						pstmtSql.setString(7, itemcode);
						pstmtSql.setString(8, unit);
						pstmtSql.setString(9, listtype);
						pstmtSql.setDouble(10, minqty);
						pstmtSql.setDouble(11, maxqty);
						pstmtSql.setDouble(12, SlabNo);
						pstmtSql.setString(13, refNoold);
						pstmtSql.setTimestamp(14, efffrom);
						int upcnt1=pstmtSql.executeUpdate();	
						System.out.println("No. of rows updated11111111111------>>"+upcnt1);
						if(pstmtSql !=null)
						{
							pstmtSql.close();
							pstmtSql=null;
						}
					}
					else
					{
						sql="update pricelist set valid_upto = ? , lot_no__to = ? , chg_user =?,chg_term = ?,chg_date = ? where price_list = ? and item_code = ? and unit =? and list_type = ? and min_qty >= ? and max_qty <= ? and slab_no = ? and ref_no  is null and  EFF_FROM not in ( ? ) ";
						pstmtSql=conn.prepareStatement(sql);
						pstmtSql.setTimestamp(1, ValidUpTo);
						pstmtSql.setString(2, LotNoFrom);
						
						pstmtSql.setString(3, userId);
						pstmtSql.setString(4, termId);
						pstmtSql.setTimestamp(5, chgDate);
						
						pstmtSql.setString(6, plist);
						pstmtSql.setString(7, itemcode);
						pstmtSql.setString(8, unit);
						pstmtSql.setString(9, listtype);
						pstmtSql.setDouble(10, minqty);
						pstmtSql.setDouble(11, maxqty);
						pstmtSql.setDouble(12, SlabNo);
						pstmtSql.setTimestamp(13, efffrom);
						int upcnt1=pstmtSql.executeUpdate();	
						System.out.println("No. of rows updated11111111111------>>"+upcnt1);
						if(pstmtSql !=null)
						{
							pstmtSql.close();
							pstmtSql=null;
						}
					}
					
				}
				else
				{
					if(refNoold !=null && refNoold.trim().length() > 0)
					{
						sql="update pricelist set valid_upto = ? , lot_no__to = ? , chg_user =?,chg_term = ?,chg_date = ?  where price_list = ? and item_code = ? and unit =? and list_type = ? and min_qty >= ? and max_qty <= ? and slab_no = ? and ref_no  =? and  EFF_FROM not in ( ? ) ";
						pstmtSql=conn.prepareStatement(sql);
						pstmtSql.setTimestamp(1,ValidUpTo);
						pstmtSql.setString(2, LotNoFrom);
						
						
						pstmtSql.setString(3, userId);
						pstmtSql.setString(4, termId);
						pstmtSql.setTimestamp(5, chgDate);
						
						pstmtSql.setString(6, plist);
						pstmtSql.setString(7, itemcode);
						pstmtSql.setString(8, unit);
						pstmtSql.setString(9, listtype);
						pstmtSql.setDouble(10, minqty);
						pstmtSql.setDouble(11, maxqty);
						pstmtSql.setDouble(12, SlabNo);
						pstmtSql.setString(13, refNoold);
						pstmtSql.setTimestamp(14,efffrom);
						int upcnt1=pstmtSql.executeUpdate();	
						System.out.println("No. of rows updated11111111111------>>"+upcnt1);
						if(pstmtSql !=null)
						{
							pstmtSql.close();
							pstmtSql=null;
						}
					}
					else
					{
						sql="update pricelist set valid_upto = ? ,lot_no__to = ? , chg_user =?,chg_term = ?,chg_date = ?  where price_list = ? and item_code = ? and unit =? and list_type = ? and min_qty >= ? and max_qty <= ? and slab_no = ? and ref_no  is null  and  EFF_FROM not in ( ? ) ";
						pstmtSql=conn.prepareStatement(sql);
						pstmtSql.setTimestamp(1, ValidUpTo);
						pstmtSql.setString(2, LotNoFrom);
						
						pstmtSql.setString(3, userId);
						pstmtSql.setString(4, termId);
						pstmtSql.setTimestamp(5, chgDate);
						
						pstmtSql.setString(6, plist);
						pstmtSql.setString(7, itemcode);
						pstmtSql.setString(8, unit);
						pstmtSql.setString(9, listtype);
						pstmtSql.setDouble(10, minqty);
						pstmtSql.setDouble(11, maxqty);
						pstmtSql.setDouble(12, SlabNo);
						pstmtSql.setTimestamp(13,efffrom);
						int upcnt1=pstmtSql.executeUpdate();	
						System.out.println("No. of rows updated11111111111------>>"+upcnt1);
						if(pstmtSql !=null)
						{
							pstmtSql.close();
							pstmtSql=null;
						}
					}
				}
			}
			
			}
         
			
		//split the batch if the entered batch no is already existing in the master pricelist
		                 recordfound = false;
		                if(listtype.equalsIgnoreCase("B"))
		                {
		                	LotNoFrom=(String)PList.get("lot_no__from");
		        			System.out.println("@@@@@lot number from2222222222222"+LotNoFrom);
		        			sql="select count(1) from pricelist where price_list = ? and item_code = ? and unit = ? and list_type = ? and lot_no__from <= ? and lot_no__to >= ? and min_qty <= ? and max_qty >= ?";
		        			pstmtSql=conn.prepareStatement(sql);
		        			pstmtSql.setString(1,plist);
		        			pstmtSql.setString(2,itemcode);
		        			pstmtSql.setString(3,unit);
		        			pstmtSql.setString(4,listtype);
		        			pstmtSql.setString(5,LotFrBrow);
		        			pstmtSql.setString(6,LotNoToBrow);
		        			pstmtSql.setDouble(7,minqty);
		        			pstmtSql.setDouble(8,maxqty);
		            		rs=pstmtSql.executeQuery();
		            		if(rs.next())
		            		{
		            			cnt=rs.getInt(1);
		            		}
		            		System.out.println("llCount22222222222-------->>["+cnt+"]");
		            		if(rs != null)
		        			{
		        				rs.close();
		        				rs=null;
		        			}
		        			if(pstmtSql !=null)
		        			{
		        				pstmtSql.close();
		        				pstmtSql=null;
		        			}
		        			
		            		if(cnt > 0)
		            		{
		            			LotNoFrom=(String)PList.get("lot_no__from");
		            		    LotNoTo=(String)PList.get("lot_no__to");
		            		    nextlotnofr=LotNoFrom;
		            		    sql="select lot_no__from, lot_no__to from pricelist where price_list = ? and item_code = ? and unit = ? and list_type = ? and lot_no__from <= ? and lot_no__to >= ? and min_qty <= ? and max_qty >= ?  order by lot_no__from";
		            		    pstmtSql=conn.prepareStatement(sql);
			        			pstmtSql.setString(1,plist);
			        			pstmtSql.setString(2,itemcode);
			        			pstmtSql.setString(3,unit);
			        			pstmtSql.setString(4,listtype);
			        			pstmtSql.setString(5,lotnofrom);
			        			pstmtSql.setString(6,LotNoToBrow);
			        			pstmtSql.setDouble(7,minqty);
			        			pstmtSql.setDouble(8,maxqty);
			            		rs=pstmtSql.executeQuery();
			            		while(rs.next())
			            		{
			            			currentlotnofr=rs.getString(1);
			            			currentlotnoto=rs.getString(2);
			            		}
			            		rs.close(); rs = null;
			            		pstmtSql.close();
			            		pstmtSql = null;//[pstmt and rs closed and nulled on 23feb19]
			            		recordfound = true;
			            		System.out.println("Recode Found@@@@@@@@@@@@@"+recordfound);
			            		currentlotnofrtemp = currentlotnofr;
			        			currentlotnototemp= currentlotnoto;
			        			/*
			        			Len=currentlotnofr.trim().length();
			        			Ctr =1;
			        			OrigStr = "";
			        			Set ="";
			        			NewStr ="";
			        			New = 0;
			        			
			        			String s2=LotNoFrom.trim();
			        			System.out.println("@@@@@x"+s2);
			        			String y=currentlotnofr.trim();
			        			System.out.println("@@@@@x"+y);
			        			if(s2.length()<y.length())
			        			{
			        				char right3= y.charAt(y.length()-1);
			        				if(Character.isDigit(right3) )
			        				{
			        					//do
			        					//{
			        	    				if(Character.isDigit(right3))
			        	    				{
			        	    				left=y.charAt(0);
			        	    				System.out.println("left :::::::::::"+left);
			        	    				if(left =='0')
			        	    				{
			        	    				if(y.length()>2   )
			        	    				{
			        	    				mid=y.substring(2);
			        	    				System.out.println("middle ::::::::::::"+mid);
			        	    				Set=Set + '0';
			        	    				}
			        	    				}
			        	    				else
			        	    				{
			        	    				OldLen = currentlotnofr.trim().length();
			        	    				System.out.println("OldLen@@@@@@@"+OldLen);
			        	    				New = OldLen - 1;
			        	    				System.out.println("New@@@@@@@"+New);
			        	    				//String new1=Integer.toString(New);
			        	    				System.out.println("NewLen@@@@@@@"+NewLen);
			        	    				if(OldLen != New);
			        	    				{int diff3=(int) (OldLen - New);
			        	    				System.out.println("Old Length"+OldLen);
			        	    				System.out.println("New"+New);
			        	    				System.out.println("Diff"+diff3);
			        	    				if(s2.length() <= 2  )
			        	    				{
			        	    				if( Character.isDigit(left ))
			        	    				{
			        	    				    for(int s3=0;s3<diff3;s3++)
			        	    				    {
			        	    				    System.out.println("For loop if ");
			        	    				    NewStr=NewStr.concat("0");
			        	    				    System.out.println("@@@@@@@@@New String:::::::::::"+NewStr.length());
			        	    				    }
			        	    				}
			        	    				}
			        	    				else
			        	    				{
			        	    				    for(int s3=0;s3<diff3;s3++)
			        	    				    {
			        	    				    System.out.println("For loop else condition");
			        	    				    NewStr=NewStr.concat("0");
			        	    				    System.out.println("@@@@@@@@@New String:::::::::::"+NewStr.length());
			        	    				    }
			        	    				}}	
			        	    				}
			        	    				}
			        	    				else
			        	    				{
			        	    				OrigStr = Character.toString(left);
			        	    				   Set = Set + OrigStr;
			        	    				LotNoFrom = ( mid+1);

			        	    				}
			        	    				       Ctr++;
			        	    				//}while(Ctr <= Len);
			        				//	lotnoto = Set + trim(ls_NewStr) +string(ll_New)
			        					//ls_lot_no__fr = ls_next_lot_no_fr
			        					if(New==0)
			        					{
			        					LotNoFrom = Set +NewStr.trim();
			        					}
			        					else
			        					{
			        					   int length = LotNoFrom.length();
			        					   String result = "";
			        					   for (int i = 0; i < length; i++)
			        					   {
			        					       Character character = LotNoFrom.charAt(i);
			        					       if (Character.isDigit(character))
			        					       {
			        					           result += character;
			        					       }
			        					   }
			        					   int u=Integer.parseInt(result);
			        					System.out.println("u"+u);
			        					int v=u-1;
			        					System.out.println("v"+v);
			        					String Conv=String.valueOf(v);
			        					int o=Conv.length();
			        					System.out.println("O:"+o);
			        					int p=NewStr.length();
			        					System.out.println("p:"+p);
			        					int toat=o+p;
			        					String a2=Integer.toString(v);
			        					String e=s2.substring(0, s2.length()-toat);
			        					System.out.println("e"+e);
			        					System.out.println("NewStr"+NewStr);
			        					LotNoFrom = e.trim()+NewStr.trim()+a2 ;
			        					System.out.println("LotNoFrom1@@@@@@@@:"+LotNoFrom);
			        					}
		                         }
			        		
			        			
			                   }
			        			
			        			
                                        Len1=currentlotnoto.trim().length();
			        					Ctr =1;
					        			OrigStr = "";
					        			Set ="";
					        			NewStr ="";
					        			New = 0;
					        			String p=LotNoFrom.trim();
					        			String q=currentlotnofr.trim();
					        			if(p.length()<q.length())
					        			{
					        			char right4= y.charAt(y.length()-1);
					        			if(Character.isDigit(right4) )
					        			{
					        		//	do
					        			//{
					        			if(Character.isDigit(right4) )
					        			 {
					        			            left4 =y.charAt(0);
					        					    if(left4=='0')
					        					     {
					        					    	if(y.length()>2)
					        					    	{
					        						   mid4= y.substring(2);
					        						   Set=Set + '0';
					        					    	}
					        					     }
					        					   else
					        					     {
					        						   OldLen = currentlotnoto.trim().length();
					        						   New = OldLen - 1;
					        						   String new1=Integer.toString(New);
					        						   NewLen = new1.trim().length();
					        						   
					        						   OldLen = currentlotnoto.trim().length();
					        						   System.out.println("OldLen@@@@@@@"+OldLen);
					        						   New = OldLen - 1;
					        						   System.out.println("New@@@@@@@"+New);
					        						   //String new1=Integer.toString(New);
					        						   System.out.println("NewLen@@@@@@@"+NewLen);
					        						   
					        						   
					        						   if(OldLen != NewLen);
					        						     {int diff3=(int) (OldLen - New);
					        						     System.out.println("Old Length"+OldLen);
					        						     System.out.println("New"+New);
					        						     System.out.println("Diff"+diff3);
					        						     if(y.length() <= 2  )
					        						     {
					        						     if( Character.isDigit(left ))
					        						     {
					        						         for(int s3=0;s3<diff3;s3++)
					        						         {
					        						         System.out.println("For loop if ");
					        						         NewStr=NewStr.concat("0");
					        						         System.out.println("@@@@@@@@@New String:::::::::::"+NewStr.length());
					        						         }
					        						     }
					        						     }
					        						     else
					        						     {
					        						         for(int s3=0;s3<diff3;s3++)
					        						         {
					        						         System.out.println("For loop else condition");
					        						         NewStr=NewStr.concat("0");
					        						         System.out.println("@@@@@@@@@New String:::::::::::"+NewStr.length());
					        						         }
					        						     }}	
					        						
					        					     }
					        		     }
					        					  else
					        					  {     OrigStr = Character.toString(left4);
					        						    Set = Set + OrigStr;
					        							LotNoFrom = (mid4+ 1); 
					        					  }
					        					  Ctr++;
					        					//  }while(Ctr <= Len1);
					        				//	lotnoto = Set + trim(ls_NewStr) +string(ll_New)
					        					//ls_lot_no__fr = ls_next_lot_no_fr
					        					String a2=Integer.toString(New);
					        					lotnofr = Set + NewStr.trim() +a2.trim();
					        					lotnofr = nextlotnofr;
					        			
					        			if(New==0)
					    				{
					        				currentlotnoto = Set +NewStr.trim();
					    				}
					    				else
					    				{
					    				   int length = currentlotnoto.length();
					    				   String result = "";
					    				   for (int i = 0; i < length; i++)
					    				   {
					    				       Character character = currentlotnoto.charAt(i);
					    				       if (Character.isDigit(character))
					    				       {
					    				           result += character;
					    				       }
					    				   }
					    				   int u=Integer.parseInt(result);
					    				System.out.println("u"+u);
					    				int v=u-1;
					    				System.out.println("v"+v);
					    				String Conv=String.valueOf(v);
					    				int o=Conv.length();
					    				System.out.println("O:"+o);
					    				int p1=NewStr.length();
					    				System.out.println("p:"+p1);
					    				int toat=o+p1;
					    				String a2=Integer.toString(v);
					    				String e=y.substring(0, y.length()-toat);
					    				System.out.println("e"+e);
					    				System.out.println("NewStr"+NewStr);
					    				String currentlotnoto1 = e.trim()+NewStr.trim()+a2 ;
					    				System.out.println("LotNoFrom1@@@@@@@@:"+currentlotnoto1);
					    				}
					        			
					        			
					        			
				                          }
					                      }*/
					        			       /* int temp=0;
		                                       if(currentlotnofrtemp != LotNoFrom)
		                                       {
                                                    temp =Integer.parseInt(LotNoFrom);
                                                    int temp1=Integer.parseInt(LotNoTo);
		                                    	    nextlotnofr=LotNoFroma[temp]+1;
		                                    	    lotnoto=LotNoToa[temp1]+1;
		                                       }*/
		                                       //int tempnew=0;
		                                    /*   if(currentlotnototemp <= LotNoTo)
		                                       {
		                                    	   tempnew =Integer.parseInt(LotNoFrom);
                                                   int temp2=Integer.parseInt(LotNoTo);
		                                    	    nextlotnofr=LotNoFroma[temp]+1;
		                                    	    lotnoto=LotNoToa[temp2]+1;
		                                       }*/
		                               /*        for(int i=1;i<temp;i++)
		                                       {
		                                    	lotnofr = LotNoFroma[i];
		                                    	lotnoto = LotNoToa[i];  
		                                       sql="select  max(slab_no)  from pricelist where price_list = ? AND item_code = ? ";
		                                       pstmtSql = conn.prepareStatement(sql);
		                                       pstmtSql.setString(1, plist);
		                                       pstmtSql.setString(2, itemcode);
		                                       rs = pstmtSql.executeQuery();
		                       				   if ( rs.next() )
		                       				   {
		                       					LineNo = rs.getInt(1);
		                       				    System.out.println("line no@@@@@@@"+LineNo);
		                       				   }
		                       				    rs.close();
		                       				    rs = null;
		                       				    pstmtSql.close();
		                       				    pstmtSql = null;
		                       					if( LineNo == 0)
		                       					{
		                       						System.out.println("Insert line no");
		                       						LineNo++;
		                       						sql="insert into pricelist (price_list,	item_code,unit,list_type,slab_no,eff_from,valid_upto,lot_no__from,lot_no__to,min_qty,max_qty,rate,rate_type,min_rate,chg_date,chg_user,chg_term,max_rate,order_type,	price_list__parent,	chg_ref_no,	ref_no,ref_no_old  )" +
		                       								"VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		                       						pstmtInsert = conn.prepareStatement(sql);
		                       						pstmtInsert.setString(1, plist);
		                       						pstmtInsert.setString(2, itemcode);
		                       						pstmtInsert.setString(3, unit);
		                       						pstmtInsert.setString(4, listtype);
		                       						pstmtInsert.setDouble(5, LineNo);
		                       						pstmtInsert.setTimestamp(6, efffrom);
		                       						pstmtInsert.setTimestamp(7, ValidUpTo);
		                       						pstmtInsert.setString(8,lotnofrom);
		                       						pstmtInsert.setString(9, lotnoto1);
		                       						pstmtInsert.setDouble(10, minqty);
		                       						pstmtInsert.setDouble(11, maxqty);
		                       						pstmtInsert.setDouble(12, rate1);
		                       						pstmtInsert.setString(13, ratetype);
		                       						pstmtInsert.setDouble(14, minrate);
		                       						chgDate = new java.sql.Timestamp(System.currentTimeMillis());
		                       						pstmtInsert.setTimestamp(15, chgDate);
		                       						pstmtInsert.setString(16, userId);
		                       						pstmtInsert.setString(17, termId);
		                       						pstmtInsert.setDouble(18, maxrate1);
		                       						pstmtInsert.setString(19, orderType1);
		                       						pstmtInsert.setString(20, PriceListParent1);
		                       						pstmtInsert.setString(21, chgref1);
		                       						pstmtInsert.setString(22, refNo1);
		                       						pstmtInsert.setString(23, refNoold1);
		                       					    UpdCnt = pstmtInsert.executeUpdate();
		                       						pstmtInsert.close();
		            								pstmtInsert = null;			
		                       						
		                       						
		                       					}
		                       				    
		                                        }*/
		            		                  }
		                                    }
		                                           /* if(LineNo == 0)
		                                            {
		                                            	  sql="select  max(slab_no)  from pricelist where price_list = ? AND item_code = ? ";
		   		                                       pstmtSql = conn.prepareStatement(sql);
		   		                                       pstmtSql.setString(1, plist);
		   		                                       pstmtSql.setString(2, itemcode);
		   		                                       rs = pstmtSql.executeQuery();
		   		                       				   if ( rs.next() )
		   		                       				   {
		   		                       					LineNo = rs.getInt(1);
		   		                       				   }
		   		                       				    rs.close();
		   		                       				    rs = null;
		   		                       				    pstmtSql.close();
		   		                       				    pstmtSql = null;
		                                            }
		                                            System.out
															.println("LINE NO@@@"+LineNo);
		                                             System.out
															.println("Record found@@@@@@@@@@@@@"+recordfound);
		                                             today=new Timestamp(System.currentTimeMillis());
		                                             ChgDate = today;
		                                             if(recordfound == false)
		                                             {
		                                            	 LineNo++;
		                                            	 sql="insert into pricelist (price_list,item_code,unit,list_type,slab_no,eff_from,valid_upto,lot_no__from,lot_no__to,min_qty,max_qty,rate,rate_type,min_rate,chg_date,chg_user,chg_term,max_rate,order_type,price_list__parent,	chg_ref_no,	ref_no,ref_no_old  )" +
		                       								"VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		                       						pstmtInsert = conn.prepareStatement(sql);
		                       						pstmtInsert.setString(1, plist);
		                       						pstmtInsert.setString(2, itemcode);
		                       						pstmtInsert.setString(3, unit);
		                       						pstmtInsert.setString(4, listtype);
		                       						pstmtInsert.setDouble(5, slabno);
		                       						pstmtInsert.setTimestamp(6, EffDate);
		                       						pstmtInsert.setTimestamp(7, ValidUpTo);
		                       						pstmtInsert.setString(8,lotnofrom);//lotnofr)
		                       						pstmtInsert.setString(9, lotnoto1);//lotnoto)
		                       						pstmtInsert.setDouble(10, minqty);
		                       						pstmtInsert.setDouble(11, maxqty);
		                       						pstmtInsert.setDouble(12, rate1);
		                       						pstmtInsert.setString(13, ratetype);
		                       						pstmtInsert.setDouble(14, minrate);
		                       						chgDate = new java.sql.Timestamp(System.currentTimeMillis());
		                       						pstmtInsert.setTimestamp(15, chgDate);
		                       						pstmtInsert.setString(16, userId);
		                       						pstmtInsert.setString(17, termId);
		                       						pstmtInsert.setDouble(18, maxrate1);
		                       						pstmtInsert.setString(19, orderType1);
		                       						pstmtInsert.setString(20, PriceListParent1);
		                       						pstmtInsert.setString(21, chgref1);
		                       						pstmtInsert.setString(22, refNo1);
		                       						pstmtInsert.setString(23, refNoold1);
		                       					    UpdCnt = pstmtInsert.executeUpdate();
		                       						pstmtInsert.close();
		            								pstmtInsert = null;			
		                                            	 
		                                             }*/
		                                        /*    
		                                            System.out.println("Insert completed"); 
		                                           chgDate = new java.sql.Timestamp(System.currentTimeMillis());
		                                           String sql1="update pricelist set chg_user =?,chg_term = ?,chg_date = ? where price_list =? and  item_code=? and unit = ? and list_type =?";
		                                           pstmtSql = conn.prepareStatement(sql1);
		        								   chgDate = new java.sql.Timestamp(System.currentTimeMillis());
		        								   pstmtSql.setString(1, userId);
		        								   pstmtSql.setString(2, termId);
		        								   pstmtSql.setTimestamp(3, chgDate);
		        								   pstmtSql.setString(4,plist);
		        								   pstmtSql.setString(5,itemcode);
		        								   pstmtSql.setString(6, unit);
		        								   pstmtSql.setString(7, listtype);
		        								   updCnt = pstmtSql.executeUpdate();
		        								   pstmtSql.close();
		            									   pstmtSql = null;	*/
		                                            
		}//try end
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			System.out.println("Exception : "+e);
			e.printStackTrace();
			throw new ITMException(e);
		}finally {
			try {							
				if(rs != null)
				{
					rs.close();
					rs = null;	
				}
				if(pstmtSql != null)
				{
					pstmtSql.close();
					pstmtSql = null;
				}
				if(pstmtInsert != null)
				{
					pstmtInsert.close();
					pstmtInsert = null;
				}
			}catch(Exception e)
			{				
			}
		}
		
		 return retString;
	}
	
	
	private static boolean isNumber(Character character) 
	{
	    boolean flag= false;
	    try
	    {

	        double num = Double.parseDouble(""+character);
	        flag =  true;
	        return flag;
	    }
	    catch( Exception e)
	    {
	        return flag;
	    }

	}
	
}
