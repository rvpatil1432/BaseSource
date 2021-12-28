/* 
	Developed by : Niraja
	Company : Base Information Management Pvt. Ltd
	Version : 1.0
	Date :01/12/2005 
*/

package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import java.util.*;
import java.lang.String;
import java.sql.*;
import org.w3c.dom.*;
import javax.ejb.*;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.utility.E12GenericUtility;
import ibase.system.config.*;
import ibase.utility.CommonConstants;
import ibase.webitm.ejb.dis.DistCommon;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class AssociateAct extends ActionHandlerEJB implements AssociateActLocal, AssociateActRemote
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

	public String actionHandler(String actionType,String xmlString1, String objContext, String xtraParams) throws RemoteException,ITMException
	{
		System.out.println("Associate called");
		Document dom1 = null;
		String  resString = null;
		try
		{
			ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility(); 
			if(xmlString1 != null && xmlString1.trim().length()!=0)
			{
				//System.out.println("XML String1 :"+xmlString1);
				dom1 = genericUtility.parseString(xmlString1); 
			}
			//System.out.println("actionType:"+actionType);
			if (actionType.equalsIgnoreCase("Process"))
			{
				resString = actionProcess(dom1,objContext,xtraParams);
			}
		
		}
	   	catch(Exception e)
		{
			//System.out.println("Exception :Associate :actionHandler(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		//System.out.println("returning from action["+actionType+"] actionHandler"+resString);
	    return (resString);
	}

	private String actionProcess(Document dom1, String objContext, String xtraParams) throws RemoteException , ITMException
	{
		Connection conn = null, connCP = null;
		PreparedStatement pstmt = null, pstmtCP = null, pstmtCP1 = null;
		PreparedStatement pstmt1 = null;
		//Statement stmt = null;
		ResultSet rs = null, rsCP = null, rsCP1 = null;
		ResultSet rs1 = null;
		ResultSet rsScd = null;
		String sql = "", sql1 = "", sql2 = "";
	   	String mopt ="", tranId ="", custCode ="";
		String fromDate ="", tranDate ="", toDate = "",detCnt = "0",dateFlag= "";
		String siteCodeCurr ="", siteCode ="", itemCode = "", itemDescr = "", unit = "";
		String locType ="", source ="", siteCodeSupp ="", itemSer ="", schemeCd ="", stateCd ="";
		String applyCustList = "", applyCust = "", noApplyCustList ="", noApplyCust ="", applicableOrdTypes ="";
		String prevscheme ="", schemeCode = "", slabOn ="", mnature ="", orderType ="", token ="";
		int mcnt=0,count =0,mchk = 0; 
		double orderQty = 0, clStk=0, qtyStk =0;
		double totQty =0, batchQty =0, freeQty =0, perc =0, mainStk =0, chkStk =0, avaiStk =0, balStk =0;
		int schcnt=0;
		boolean proceed ;
		String replRate = "";

		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
	
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		
		try
		{	
			ConnDriver connDriver = new ConnDriver();			
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver = null;
			connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			connCP = connDriver.getConnectDB("DriverCP");
			//connCP = connDriver.getConnectDB("DriverITM");
			//connCP = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END
			connDriver = null;
			//stmt = conn.createStatement();//Removed Resultset Sensitive Type // To be Updated in Server - Jiten - 
			//detCnt = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"detCnt"); 
			if(detCnt.equals("0"))
			{
				orderType = "F";//Added by Jiten 09/10/06 as it is in PB
				// orginal disparm INCL_FREITM_INASOSTK
				//sql= "SELECT VAR_VALUE FROM DISPARM WHERE PRD_CODE ='999999' AND VAR_NAME ='SOREPL_EXCLUDE_FREE_ITEMS'"; //variable anme changed by pankaj on 18/02/09
				//System.out.println("DISPARM:sql:"+sql);
				
				//rs = stmt.executeQuery(sql);
				//if(rs.next())
				//{
				//	mopt = rs.getString(1);
				//}
				//System.out.println("mopt:"+mopt+":");
				DistCommon disCommon = new DistCommon();
				mopt = disCommon.getDisparams("999999","SOREPL_EXCLUDE_FREE_ITEMS", conn);
				disCommon = null;
				if(!"C".equalsIgnoreCase(mopt))
				{
					tranId = genericUtility.getColumnValue("tran_id",dom1);
					dateFlag = genericUtility.getColumnValue( "date_flag", dom1 ); // added by pankaj on 18.01.09
					custCode = genericUtility.getColumnValue("cust_code",dom1);
					tranDate = genericUtility.getColumnValue("tran_date",dom1);
					fromDate = genericUtility.getColumnValue("from_date",dom1);
					toDate = genericUtility.getColumnValue("to_date",dom1);
					siteCodeCurr =  genericUtility.getColumnValue("site_code",dom1);
					//Added by Jiten 09/10/06 as added in PB
					replRate = genericUtility.getColumnValue("repl_rate",dom1);
					orderType = genericUtility.getColumnValue("order_type",dom1);
					//End
					
					//System.out.println("tranId:"+tranId+":custCode:"+custCode+":tranDate:"+tranDate+":fromDate"+fromDate+":toDate:"+toDate+":siteCodeCurr:"+siteCodeCurr);
					//System.out.println("Repl rate :"+replRate+" orderType :"+orderType);
					sql="SELECT SITE_CODE FROM CUSTOMER WHERE CUST_CODE = ?";
					//System.out.println("CUSTOMER:sql:"+sql);
					//rs = stmt.executeQuery(sql);
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,custCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
					  siteCode = rs.getString(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					//System.out.println("siteCode:"+siteCode+":");
					//Added by Jiten 09/10/06 As code changed in PB
					if(orderType == null || orderType.trim().length() == 0){
						orderType = "F";
					}
					//End
					
					if(mopt != null && mopt.trim().length()!= 0)
					{
						if(mopt.equalsIgnoreCase("I")) // changed by pankaj on 18/02/09 to Include free items
						{
							mnature = "%";
						}
						if(mopt.equalsIgnoreCase("E")) //changed by pankaj on 18/02/09 to exclude free items
						{	
							//mnature = "C"; // //Piyush Friday, May 05, 2006
							mnature = "C%";
						}
					}//mopt != null
					//System.out.println("mnature:"+mnature+":");
					//Added by Jiten 09/10/06 as added in PB & below sql changed
					if(replRate != null && replRate.trim().equalsIgnoreCase("1"))
					{
						orderType = orderType.trim()+"%"; 
					}
					else
					{
						orderType = "%";
					}
					
					System.out.println("dateFlag:::["+dateFlag+"]");
					/*sql =" SELECT B.ITEM_CODE ,SUM(B.QUANTITY - B.QTY_DESP),C.DESCR ,C.UNIT ,"+
						" C.LOC_TYPE FROM SORDITEM B , ITEM C ,SORDER A"+
						" WHERE A.SALE_ORDER = B.SALE_ORDER"+ 
						" AND A.ORDER_DATE >= ? "+
						" AND A.ORDER_DATE <= ? "+
						" AND  (A.STATUS IS NULL OR A.STATUS = 'P')"+
						" AND  B.SITE_CODE = '"+siteCode+"'"+
						" AND B.LINE_TYPE  <> 'B' "+
						" AND  (B.QUANTITY - QTY_DESP) > 0 "+
						" AND   B.NATURE LIKE '"+mnature+"' "+
						" AND 	B.ITEM_CODE   = C.ITEM_CODE"+
						" GROUP BY B.ITEM_CODE, C.DESCR, C.UNIT, C.LOC_TYPE ORDER BY DESCR"  ;*/
					// CommonConstants.setIBASEHOME();
					// if(CommonConstants.DB_NAME.equalsIgnoreCase("db2")){
						// sql = "SELECT ITEM_CODE, SUM(BAL_QTY) , DESCR , UNIT , "+ //LOC_TYPE , AC_RATE "+ // commented by pankaj on 18/01/09
						// "case when c.loc_type__parent is null then c.loc_type else c.loc_type__parent end loc_type," + // changed by pankaj as code changed in pb on 18/01/09
						// "FROM (SELECT B.ITEM_CODE ITEM_CODE, (B.QUANTITY - B.QTY_DESP) BAL_QTY , C.DESCR DESCR ,"+ 
						// "C.UNIT UNIT , C.LOC_TYPE LOC_TYPE,(CASE WHEN ? = '1' THEN D.RATE ELSE 0 END) AC_RATE "+
						// "FROM SORDITEM_CP B ,ITEM C ,SORDER A ,SORDDET D WHERE A.SALE_ORDER  = B.SALE_ORDER "+  // table name changed  to sorditem_cp by pankaj on 18/02/9
						// "AND B.ITEM_CODE = C.ITEM_CODE "+
						// "AND B.SALE_ORDER = D.SALE_ORDER "+
						// "AND B.LINE_NO = D.LINE_NO "+	  
						// "AND A.ORDER_DATE >= ? "+ 
						// "AND A.ORDER_DATE <= ? "+
						// "AND (A.STATUS IS NULL OR A.STATUS = 'P') "+
						// "AND B.SITE_CODE   = ? "+
						// "AND B.LINE_TYPE  <> 'B' "+
						// "AND (B.QUANTITY - QTY_DESP) > 0 "+
						// "AND B.NATURE LIKE ? "+
						// "AND (CASE WHEN A.ORDER_TYPE IS NULL THEN ' ' ELSE A.ORDER_TYPE END ) LIKE ? ) AS DD "+
						// "GROUP BY ITEM_CODE, DESCR, UNIT, LOC_TYPE , AC_RATE ORDER BY DESCR";
					// }else if(CommonConstants.DB_NAME.equalsIgnoreCase("oracle")){
						// sql = "SELECT ITEM_CODE, SUM(BAL_QTY) , DESCR , UNIT , "+ //LOC_TYPE , AC_RATE "+ // commented by pankaj on 18/01/09
						// "case when c.loc_type__parent is null then c.loc_type else c.loc_type__parent end loc_type," + // changed by pankaj as code changed in pb on 18/01/09
						// "FROM (SELECT B.ITEM_CODE ITEM_CODE, (B.QUANTITY - B.QTY_DESP) BAL_QTY , C.DESCR DESCR ,"+ 
						// "C.UNIT UNIT , C.LOC_TYPE LOC_TYPE,(CASE WHEN ? = '1' THEN D.RATE ELSE 0 END) AC_RATE "+
						// "FROM SORDITEM_CP B ,ITEM C ,SORDER A ,SORDDET D WHERE A.SALE_ORDER  = B.SALE_ORDER "+ // table name changed  to sorditem_cp by pankaj on 18/02/9
						// "AND B.ITEM_CODE = C.ITEM_CODE "+
						// "AND B.SALE_ORDER = D.SALE_ORDER "+
						// "AND B.LINE_NO = D.LINE_NO "+	  
						// "AND A.ORDER_DATE >= ? "+ 
						// "AND A.ORDER_DATE <= ? "+
						// "AND (A.STATUS IS NULL OR A.STATUS = 'P') "+
						// "AND B.SITE_CODE   = ? "+
						// "AND B.LINE_TYPE  <> 'B' "+
						// "AND (B.QUANTITY - QTY_DESP) > 0 "+
						// "AND B.NATURE LIKE ? "+
						// "AND (CASE WHEN A.ORDER_TYPE IS NULL THEN ' ' ELSE A.ORDER_TYPE END ) LIKE ? ) "+
						// "GROUP BY ITEM_CODE, DESCR, UNIT, LOC_TYPE , AC_RATE ORDER BY DESCR";
						////******* 15/04/09
						// query changed by pankaj 0n 18.01.09
						if( orderType == null || orderType.trim().length() == 0 )
						{
							 orderType = "F";
						}
						sql = "select 	item_code, sum(bal_qty), descr, unit, loc_type, ac_rate from ( ";
						sql = sql + " select b.item_code item_code, ";
						sql = sql + "(b.quantity - b.qty_desp) bal_qty, c.descr descr, c.unit unit, " ;
						sql = sql + " case when c.loc_type__parent is null then c.loc_type else c.loc_type__parent end loc_type, ";
						if((replRate != null && replRate.trim().length() > 0) && replRate.equals("1"))
						{
							sql = sql + " b.rate ac_rate ";
						}
						else
						{
							sql = sql + " (0) ac_rate ";
						}
						//sql = sql + "(case when '" + ls_repl_rate + "' = '1' then d.rate else 0 end) ac_rate ";
						// 20/03/09 manoharan seperate connection instead of synonym
						//sql = sql + " from sorditem_cp b, item c, sorder_cp a,	sorddet_cp d where ";
  						// 27/03/09 manoharan sorddet removed and function used
						//sql = sql + " from sorditem b, item c, sorder a,	sorddet d where ";
						sql = sql + " from sorditem b, item c ";
						sql = sql + " where b.item_code   = c.item_code ";
						if((dateFlag != null && dateFlag.trim().length() > 0) && dateFlag.equalsIgnoreCase("O") )
						{
							sql = sql + " and 	b.order_date >= ?"; 
							sql = sql + " and 	b.order_date <= ?"; 
						}
						else if((dateFlag != null && dateFlag.trim().length() > 0) && dateFlag.equalsIgnoreCase("D") )
						{
							sql = sql + " and 	b.dsp_date >= ?"; 
							sql = sql + " and 	b.dsp_date <= ?"; 
						}
						sql = sql + " and 	b.site_code   = ? ";
						sql = sql + " and  (case when b.order_type is null then ' ' else b.order_type end ) like '" + orderType + "'"; 
						sql = sql + " and  (b.status is null or b.status in( 'P','D')) ";
						sql = sql + " and   b.nature like ? ";
						sql = sql + " and 	b.line_type  <> 'B' ";
						sql = sql + " and  (b.quantity - qty_desp) > 0 ";
						sql = sql + " ) ";
						sql = sql + " group by descr, item_code,  unit, loc_type , ac_rate ";
						sql = sql + " order by descr" ;
					}
					//End change by pankaj
					// 20/03/09 manoharan seperate connection instead of synonym
					//pstmt = conn.prepareStatement(sql);
					pstmtCP = connCP.prepareStatement(sql);
					// end 20/03/09 manoharan seperate connection instead of synonym
					//System.out.println(":sql:"+sql);
					//System.out.println("Value form function:"+genericUtility.getValidDateTimeString(fromDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()));
					//System.out.println("2. From date :"+java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(fromDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
					//System.out.println("3. To Date :"+java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(toDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
					//System.out.println("1. replRate :"+replRate);
					//System.out.println("4. siteCode :"+siteCode);
					//System.out.println("5. mnature :"+mnature);
					//System.out.println("6. orderType :"+orderType);
					
					//pstmt.setString(1,replRate);//Added by Jiten 10/10/06 as sql changed in PB
					// 20/03/09 manoharan seperate connection instead of synonym
					//pstmt.setTimestamp(1,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(fromDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
					//pstmt.setTimestamp(2,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(toDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
					
					if((dateFlag != null && dateFlag.trim().length() > 0) && (dateFlag.equalsIgnoreCase("D") || dateFlag.equalsIgnoreCase("O") ) )
					{
						pstmtCP.setTimestamp(1,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(fromDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
						pstmtCP.setTimestamp(2,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(toDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
						pstmtCP.setString(3,siteCode);
						pstmtCP.setString(4,mnature);
					}
					else
					{
						pstmtCP.setString(1,siteCode);
						pstmtCP.setString(2,mnature);
					}
					////******* 15/04/09 end
					// end 20/03/09 manoharan seperate connection instead of synonym
					//pstmt.setString(4,siteCode);//Added by Jiten 09/10/06 as sql changed in PB
					//pstmt.setString(5,mnature);//Added by Jiten 09/10/06 as sql changed in PB
					//pstmt.setString(6,orderType);//Added by Jiten 09/10/06 as sql changed in PB   //  change by pankaj
					// 20/03/09 manoharan seperate connection instead of synonym
					//rs1 = pstmt.executeQuery();
					rsCP = pstmtCP.executeQuery();
					
					//while(rs1.next())
					while(rsCP.next())
					{
						//itemCode = rs1.getString(1);
						//orderQty = rs1.getDouble(2);
						//itemDescr = rs1.getString(3);
						//unit = rs1.getString(4);
						//locType = rs1.getString(5);
						itemCode = rsCP.getString(1);
						orderQty = rsCP.getDouble(2);
						itemDescr = rsCP.getString(3);
						unit = rsCP.getString(4);
						locType = rsCP.getString(5);
						//System.out.println("itemCode:"+itemCode+":orderQty:"+orderQty+":itemDescr:"+itemDescr+":unit:"+unit+":locType:"+locType+":"); 
						//if get_sqlcode() <> 0 then
						//if isnull(lc_order_qty) then lc_order_qty = 0
						sql=" SELECT SUM(A.QUANTITY - CASE WHEN A.ALLOC_QTY IS NULL THEN 0 ELSE A.ALLOC_QTY END )"+  
							" FROM STOCK A, INVSTAT  B "+ // table name changed to stock_cp by pankaj on 18/01/09, reverted to stock tableby manoharan 20/03/09
							" WHERE A.INV_STAT  = B.INV_STAT "+
							" AND A.ITEM_CODE = ? "	+
							" AND A.SITE_CODE = ? "	+
							" AND A.QUANTITY  > 0 " +
							" AND B.AVAILABLE = 'Y'" +
							" AND B.INV_STAT NOT IN (SELECT DISTINCT INV_STAT FROM INV_RESTR)";
						//System.out.println(":sql:"+sql);
						//rs = stmt.executeQuery(sql);
						pstmtCP1 = connCP.prepareStatement(sql);
						pstmtCP1.setString(1,itemCode);
						pstmtCP1.setString(2,siteCode);
						rsCP1 = pstmtCP1.executeQuery();
						if(rsCP1.next())
						{
							qtyStk = rsCP1.getDouble(1);
						}
						rsCP1.close();
						rsCP1 = null;
						pstmtCP1.close();
						pstmtCP1 = null;
						//System.out.println("qtyStk:"+qtyStk+":");
						//if isnull(lc_qty_stk) then lc_qty_stk = 0
						sql ="SELECT SUPP_SOUR,SITE_CODE__SUPP "+
							" FROM 	SITEITEM "+
							" WHERE SITE_CODE = ? " +
							" AND ITEM_CODE =? ";
						//System.out.println(":sql:"+sql);
						//rs = stmt.executeQuery(sql);
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,siteCode);
						pstmt.setString(2,itemCode);
						rs = pstmt.executeQuery();
						if (!rs.next())
						{
							source = "P";
							siteCodeSupp = siteCodeCurr;
						}
						else
						{
							source =rs.getString(1);	
							siteCodeSupp =rs.getString(2);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						//System.out.println("orderQty - qtyStk:"+(orderQty - qtyStk));
						//System.out.println("orderQty - qtyStk:"+((orderQty - qtyStk) > 0));
						//System.out.println("Condition siteCodeSupp :"+(siteCodeSupp!= null && siteCodeSupp.trim().equalsIgnoreCase(siteCodeCurr.trim())));
						//System.out.println("Condition source :"+(source != null && source.trim().equalsIgnoreCase("P")));
						//System.out.println("Condition:"+((orderQty - qtyStk > 0) && (source != null && source.trim().equalsIgnoreCase("P")) && (siteCodeSupp!= null && siteCodeSupp.trim().equalsIgnoreCase(siteCodeCurr.trim()))));
						if(((orderQty - qtyStk) > 0) && (source != null && source.trim().equalsIgnoreCase("P")) && (siteCodeSupp != null && siteCodeSupp.trim().equalsIgnoreCase(siteCodeCurr.trim())))
						{
							itemSer = "";
							itemSer = itmDBAccess.getItemSeries(itemCode, siteCodeCurr,tranDate,custCode,'C',null);
							//System.out.println("FromFunction :itemSer:"+itemSer+":");
							sql="SELECT COUNT(*) FROM CUSTOMER_SERIES "+
								" WHERE  CUST_CODE = ? "+ 
								" AND ITEM_SER  = ? ";
							//System.out.println(":sql:"+sql);
							//rs = stmt.executeQuery(sql);
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,custCode);
							pstmt.setString(2,itemSer);
							rs = pstmt.executeQuery();
							
							if(rs.next())
							{
							  mcnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							//System.out.println("mcnt:"+mcnt+":");
							if(mcnt > 0)
							{
								clStk = orderQty - qtyStk;
								count = 1;
								schemeCd = "";
								sql ="SELECT STATE_CODE FROM CUSTOMER WHERE CUST_CODE = ? ";					
								//System.out.println(":sql:"+sql);
								//rs = stmt.executeQuery(sql);
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,custCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									stateCd = rs.getString(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								//System.out.println("stateCd:"+stateCd+":");
								sql2 = " SELECT A.SCHEME_CODE FROM SCHEME_APPLICABILITY A,"+
										" SCHEME_APPLICABILITY_DET  B" +
										" WHERE A.SCHEME_CODE	= B.SCHEME_CODE" +
										" AND A.ITEM_CODE = ? "	+
										" AND A.APP_FROM <= ? "+
										" AND A.VALID_UPTO >= ?"+
										" AND (B.SITE_CODE = ? "+ 
										" OR B.STATE_CODE =  ? )";
								pstmt1 = conn.prepareStatement(sql2);
								//System.out.println(":sql2:"+sql2);
								pstmt1.setString(1,itemCode);
								pstmt1.setTimestamp(2,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(tranDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
								pstmt1.setTimestamp(3,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(tranDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
								pstmt1.setString(4,siteCodeCurr);
								pstmt1.setString(5,stateCd);
								rsScd = pstmt1.executeQuery();
								while(rsScd.next())
								{
									 schemeCd = rsScd.getString(1);
									 //System.out.println("schemeCd:"+schemeCd+":");
									 sql ="SELECT (CASE WHEN APPLY_CUST_LIST IS NULL THEN ' ' ELSE APPLY_CUST_LIST END),"+
											 "(CASE WHEN NOAPPLY_CUST_LIST IS NULL THEN ' ' ELSE NOAPPLY_CUST_LIST END),"+
											 "ORDER_TYPE FROM SCHEME_APPLICABILITY "+
											 "WHERE SCHEME_CODE =  ? ";
									 //System.out.println("sql:"+sql);
									// rs = stmt.executeQuery(sql);
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1,schemeCd);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										applyCustList =rs.getString(1);
										noApplyCustList =rs.getString(2);
										applicableOrdTypes =rs.getString(3);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									 //System.out.println("applyCustList:"+applyCustList+":noApplyCustList:"+noApplyCustList+":applicableOrdTypes:"+applicableOrdTypes);
								     if((orderType.trim().equalsIgnoreCase("NE")) && (applicableOrdTypes == null || applicableOrdTypes.trim().length()== 0))
									 {
										//Below lines commented as not required - jiten - 09/10/06
								     	//System.out.println("schemeCode:"+schemeCode+":");
										//if(schemeCode != null && schemeCode.trim().length()== 0)
										//{
											continue;
										//}
										//else
										//{
										//	break;
										//} 
									 }
									 else if(applicableOrdTypes != null && applicableOrdTypes.trim().length() > 0)
									 {
										proceed = false;
										StringTokenizer st = new StringTokenizer(applicableOrdTypes,",");
										while(st.hasMoreTokens())
										{
											token = st.nextToken();
											if(orderType.equalsIgnoreCase(token))
											{
												proceed = true;
												break;
											}
										}//st loop
										if(!proceed)
										{
											//Below lines commented as not required - Jiten - 09/10/06
											//if(schemeCode != null && schemeCode.trim().length()== 0)
											//{
												continue;
											//}
											// else
											//{
											//	break;
											//} 
										//}
									}
									prevscheme	= schemeCode; 
									schemeCode = schemeCd ;
									System.out.println("\n********* prevscheme:"+prevscheme+":schemeCode:"+schemeCode);
									if(applyCustList.trim().length() > 0)
									{
										//setnull(ls_scheme_code)
										schemeCode	="";
										//ls_cust_code = dw_header.getitemstring(1,"cust_code")
										StringTokenizer st1 = new StringTokenizer(applyCustList,",");
										while(st1.hasMoreTokens())
										{
											applyCust = (st1.nextToken()).trim();
											if(applyCust.equalsIgnoreCase(custCode.trim()))
											{
												schemeCode = schemeCd;
												break;
											}
										}//st1 loop
									}
									if((noApplyCustList.trim().length() > 0) && (schemeCode!= null))
									{
										//custCode = dw_header.getitemstring(1,"cust_code")
										if(noApplyCustList.trim().length() > 0)
										{
											StringTokenizer st2 = new StringTokenizer(noApplyCustList,",");
											while(st2.hasMoreTokens())
											{
											   noApplyCust = (st2.nextToken()).trim();
												if(noApplyCust.equalsIgnoreCase(custCode.trim()))
												{
													//setnull(ls_scheme_code)
													schemeCode	="";
													break;
												}
											}
										}
									}
									if(schemeCode != null)
									{
										schcnt ++;
									}
									else if(schcnt == 1)
									{
										schemeCode	= prevscheme;
									}
								  //System.out.println("\n===schemeCode:"+schemeCode+":");
								}//end loop if(rsScd.next()) Next Scheme Code
								if(schemeCode != null && schemeCode.trim().length()> 0)
								{
									//CHECKING SLAB ON IS NOT APPL or NOT
									sql="SELECT SLAB_ON FROM SCHEME_APPLICABILITY WHERE  SCHEME_CODE = ? ";
									//System.out.println("sql:"+sql);
									//rs = stmt.executeQuery(sql);
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1,schemeCd);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										slabOn = rs.getString(1);
									}
									//System.out.println("slabOn:"+slabOn+":");
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									if(slabOn.equalsIgnoreCase("N")) // scheme is applicable for current site/state
									//CALCULATION OF FREE QTY
									{
										sql="SELECT COUNT(ITEM_CODE) FROM BOMDET "+
											" WHERE BOM_CODE  = ? AND ITEM_CODE = ? ";
										//System.out.println("sql:"+sql);
										//rs = stmt.executeQuery(sql);
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1,schemeCd);
										pstmt.setString(2,itemCode);
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											count = rs.getInt(1);
										}
										//System.out.println("count:"+count+":");
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										if(count > 1) // scheme is on same item
										{
											sql=" SELECT SUM(QTY_PER)FROM BOMDET	"+
												" WHERE BOM_CODE  = ? AND ITEM_CODE = ? ";
											//System.out.println("sql:"+sql);
											//rs = stmt.executeQuery(sql);
											pstmt = conn.prepareStatement(sql);
											pstmt.setString(1,schemeCd);
											pstmt.setString(2,itemCode);
											rs = pstmt.executeQuery();
											if(rs.next())
											{
												totQty = rs.getDouble(1);
											}
											rs.close();
											rs = null;
											pstmt.close();
											pstmt = null;
											//System.out.println("totQty:"+totQty);
											sql="SELECT BATCH_QTY FROM BOM WHERE BOM_CODE = ? ";
											//System.out.println("sql:"+sql);
											//rs = stmt.executeQuery(sql);
											pstmt = conn.prepareStatement(sql);
											pstmt.setString(1,schemeCd);
											rs = pstmt.executeQuery();
											if(rs.next())
											{
												batchQty = rs.getDouble(1);
											}
											rs.close();
											rs = null;
											pstmt.close();
											pstmt = null;
											//System.out.println("batchQty:"+batchQty+":");
											sql=" SELECT SUM(QTY_PER) FROM BOMDET"+
												" WHERE BOM_CODE  = ? AND ITEM_CODE = ? "+
												 " AND 	NATURE = 'F'";
											//System.out.println("sql:"+sql);
											//rs = stmt.executeQuery(sql);
											pstmt = conn.prepareStatement(sql);
											pstmt.setString(1,schemeCd);
											pstmt.setString(2,itemCode);
											rs = pstmt.executeQuery();
											if(rs.next())
											{
												freeQty = rs.getDouble(1);
											}
											rs.close();
											rs = null;
											pstmt.close();
											pstmt = null;
											//System.out.println("freeQty:"+freeQty+":clStk:"+clStk);
											//clStk 	 = clStk - (clStk%(batchQty + freeQty)) ;
											clStk = clStk - (clStk % batchQty);//Above Commented and added by Jiten as code changed in PB - 09/10/06
											//System.out.println("clStk:"+clStk);
											if(clStk % batchQty > 0){//if condition added by Jiten 09/10/06 as code changed in PB
												perc 	 = (freeQty / totQty) * 100	;
												mainStk = clStk - (clStk * perc / 100);
												clStk 	 = mainStk;
											}
											//System.out.println("clStk:"+clStk+":perc:"+perc+":mainStk:"+mainStk+":clStk:"+clStk);
											sql = "SELECT SUM(A.QUANTITY - NVL(A.ALLOC_QTY,0)) "+
												 "FROM STOCK A,INVSTAT B "+ 
												 "WHERE A.INV_STAT  = B.INV_STAT "+
												 "AND A.ITEM_CODE =  ? " +
												 "AND A.SITE_CODE = ? "+
												 "AND A.QUANTITY  > 0"+
												 "AND B.AVAILABLE = 'Y'	"+
												 "AND B.INV_STAT NOT IN (SELECT DISTINCT INV_STAT FROM INV_RESTR)";
											//System.out.println("sql:"+sql);
											pstmt = conn.prepareStatement(sql);
											pstmt.setString(1,itemCode);
											pstmt.setString(2,siteCodeCurr);
											rs = pstmt.executeQuery();
											if(rs.next())
											{
												chkStk = rs.getDouble(1);
											}
											rs.close();
											rs = null;
											pstmt.close();
											pstmt = null;
											//System.out.println("chkStk:"+chkStk+":");
											//If the stock in our site(Chnl. Part. repl site) is >0  but less than the reqd qty
											if(chkStk < (orderQty - qtyStk) && chkStk > 0)
											{
												avaiStk = chkStk ;
												//System.out.println("avaiStk:"+avaiStk+":");
												//chkStk = chkStk -(chkStk%(batchQty + freeQty));
												chkStk = chkStk -(chkStk % batchQty);
												//System.out.println("chkStk:"+chkStk+":");
												if(chkStk % batchQty > 0){ // if condition added by Jiten - 09/10/06 
													clStk = chkStk - (chkStk * perc / 100);
												}
												//System.out.println("clStk:"+clStk+":");
												balStk = avaiStk - chkStk;
												//System.out.println("balStk:"+balStk+":");
												if(balStk > 0)
												{
													if(balStk < (batchQty + freeQty))
													{
														//		lc_bal_stk = lc_batch_qty
													}
													else
													{
														//balStk = balStk - ((balStk%batchQty + freeQty)) + (batchQty +freeQty);
														//balStk = balStk - (balStk * perc / 100);
														balStk = balStk - (balStk % batchQty);
														//System.out.println("IN ELSE :balStk:"+balStk+":");
													}
													if(clStk == 0)
													{
														clStk = (orderQty - qtyStk) - balStk ;
														if (clStk < (batchQty + freeQty)) 
														{
															clStk = batchQty;
														}
														else
														{
															//clStk = clStk - (clStk%(batchQty + freeQty)) + (batchQty + freeQty);
															//clStk = clStk - (clStk * perc / 100);
															clStk = clStk - (clStk % batchQty);
															//System.out.println("IN ELSE :clStk:"+clStk+":");
														}
													}
													count = 2;
												}
												else
												{
													count = 1 ;
												}
											}
											else
											{
												//if stk available in sun > ord qty
												balStk = orderQty - qtyStk;
												//System.out.println("1:balStk:"+balStk+":orderQty:"+orderQty+":qtyStk:"+qtyStk+":");
												//balStk = balStk - (balStk % (batchQty + freeQty)) + (batchQty + freeQty);
												balStk = balStk - (balStk % batchQty);
												//System.out.println("2:balStk:"+balStk+":");
												
												//balStk = balStk - (balStk * perc / 100);
												//System.out.println("3:balStk:"+balStk+":");
												clStk = balStk;
												count = 1;
												}
											} // if ll_count > 1
										}// if ls_slab_on = 'N'					 
									}//schemeCode != null
									
							}//while(rsScd.next())
							rsScd.close();
							rsScd = null;
							pstmt1.close();
							pstmt1 = null;
							// changed by pankaj on 19.02.09 to bring valuexml string out of while(rsScd.next()) loop
							System.out.println("ASSOCIATE count["+count+"]");
							
							for(int i = 0; i < count; i++)
							{
								if(i == 2)
								{
									clStk = balStk;
								}
								System.out.println("clStk:"+clStk+":");
								valueXmlString.append("<Detail>\r\n");
								valueXmlString.append("<item_code>").append("<![CDATA[").append(itemCode==null?"":itemCode).append("]]>").append("</item_code>\r\n");
								valueXmlString.append("<descr isSrvCallOnChg='0'>").append("<![CDATA[").append(itemDescr).append("]]>").append("</descr>\r\n");
								valueXmlString.append("<unit isSrvCallOnChg='0'>").append("<![CDATA[").append(unit).append("]]>").append("</unit>\r\n");
								valueXmlString.append("<loc_type isSrvCallOnChg='0'>").append("<![CDATA[").append(locType).append("]]>").append("</loc_type>\r\n");
								valueXmlString.append("<item_ser isSrvCallOnChg='0'>").append("<![CDATA[").append(itemSer).append("]]>").append("</item_ser>\r\n");
								valueXmlString.append("<cl_stock>").append("<![CDATA[").append(clStk).append("]]>").append("</cl_stock>\r\n");
								if (tranId != null)	     // if condition added in 23/05/06 - jiten
								{
									valueXmlString.append("<tran_id isSrvCallOnChg='0'>").append("<![CDATA[").append(tranId).append("]]>").append("</tran_id>\r\n");
								}
								//valueXmlString.append("<rate isSrvCallOnChg='0'>").append("<![CDATA[").append(rs1.getString(6) == null ? "":rs1.getString(6)).append("]]>").append("</rate>\r\n");
								valueXmlString.append("<rate>").append("<![CDATA[").append(rsCP.getString(6) == null ? "":rsCP.getString(6)).append("]]>").append("</rate>\r\n");
								
								valueXmlString.append("</Detail>\r\n");	
								System.out.println("valueXmlString generated>>>>>>>>>>>>>>>>>>> " + valueXmlString);
								System.out.println("mchk >>>>>>>>>>>>>>>>>>> " + mchk);
								mchk++;
							}//for(int i = 0; i < count; i++)
							// changed by pankaj on 19.02.09
						}//if(mcnt > 0)
					}//rs1 close()
				}//if(!mopt.equalsIgnoreCase("C"))
				rsCP.close();
				rsCP = null;
				pstmtCP.close();
				pstmtCP = null;
			}//detCnt
			if (mchk == 0)
			{
				System.out.println(itmDBAccess.getErrorString("","VTPROCESS1","","",conn));
			}
			valueXmlString.append("</Root>\r\n");
		}
		catch(SQLException e)
		{
			System.out.println("Exception : Associate : actionProcess " +e.getMessage());
			throw new ITMException(e);
		}
		catch(Exception e)
		{
			System.out.println("Exception : Associate : actionHandler :(Document dom)" +e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("Closing Connection.......");
				conn.close();
				conn = null;
				connCP.close();
				connCP = null;
			}catch(Exception e){}
		}
		System.out.println("valueXmlString.toString() "+valueXmlString.toString());
		return valueXmlString.toString();		
	}	
}