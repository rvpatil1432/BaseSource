package ibase.webitm.ejb.dis.adv;
import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import org.w3c.dom.*;

import javax.ejb.*;
import javax.naming.InitialContext;

import ibase.system.config.*;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.utility.E12GenericUtility;
import javax.ejb.Stateless;//added for ejb3
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.dis.DistCommon;

@Stateless

//public class GenStkHelpAct extends ActionHandlerEJB implements SessionBean

public class GenStkHelpAct extends ActionHandlerEJB implements GenStkHelpActLocal,GenStkHelpActRemote
{	
	/*
	public void ejbCreate() throws RemoteException, CreateException {}
	public void ejbRemove(){}
	public void ejbActivate(){}
	public void ejbPassivate(){}
	 */
	public String actionHandler() throws RemoteException,ITMException{return "";}	


	public String actionHandler(String actionType, String xmlString, String xmlString1, String objContext, String xtraParams) throws RemoteException,ITMException
	{
		Connection conn=null;
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		Document dom = null;
		Document dom1 = null;
		String  retString = null;
		try
		{
			System.out.println("XML String1:::"+xmlString1);
			System.out.println("XML String:::"+xmlString);
			System.out.println("Default function executing in progress...................");
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				System.out.println("XML String:::"+xmlString);
				dom = new  ibase.utility.E12GenericUtility().parseString(xmlString); 
				System.out.println("dom :"+dom);
			}
			if(xmlString1 != null && xmlString1.trim().length()!=0)
			{
				System.out.println("XML String1:::"+xmlString1);
				dom1 = new  ibase.utility.E12GenericUtility().parseString(xmlString1);
				System.out.println("dom1 :"+dom1);
			}
			System.out.println("actionType:::["+actionType+"]");

			if (actionType.equalsIgnoreCase("Stock") || actionType.equalsIgnoreCase("PrRtStk"))
			{
				// 28/01/11 manoharan unused connection commented
				/*try
					{
						if(conn==null)
						{
							System.out.println("Getting Connection From Database......");
							ConnDriver connDriver = new ConnDriver();
							//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
							conn.setAutoCommit(false);
							System.out.println("Connection Established......");
							DatabaseMetaData dbmd = conn.getMetaData();
							System.out.println("DriverName["+dbmd.getDriverName() );
							System.out.println("DriverURI["+dbmd.getURL()  );
							System.out.println("DriverUSER["+dbmd.getUserName());
							//System.out.println("ApplDateFormat["+genericUtility. ());
							System.out.println("DBDateFormat["+genericUtility.getDBDateFormat() );
							connDriver = null;
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}*/
				// end 28/01/11 manoharan 
				retString = actionDefault(dom, dom1, objContext, xtraParams);
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :Dispatch :actionHandler(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning String from AdjIssStkHELPEJB : actionHandler"+retString);
		return retString;
	}
	// 28/01/11 manoharan transform added
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

			if (actionType.equalsIgnoreCase("Stock") || actionType.equalsIgnoreCase("PrRtStk"))
			{
				retString = stockTransform(dom, dom1, objContext, xtraParams, selDataDom);
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
	//end  28/01/11 manoharan transform added
	private String actionDefault(Document dom, Document dom1, String objContext, String xtraParams) throws RemoteException, ITMException
	{
		System.out.println("Calling the Default function.........");
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		Connection conn = null;		
		StringBuffer valueXmlString = null;
		valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");	
		int currentFormNo = 0;
		String sql1 = "";
		String qcTableNo = "";
		String tableDescr = "" ;
		Timestamp exprDate = null;
		Timestamp chgDate = null;
		String chgUser = "";
		String chgTerm = "";
		int lineNo = 0 ;
		String specCode = "";
		String expctedRes = "";
		String sqlSpec = "";
		String descr = "";
		String tranId = "";
		String benefitType = "";
		String sql = "";
		int cnt = 0;
		String str = "";
		String despCurrCode = "";
		String keyString = "";
		String alType = "";
		String ls_advlic_line_no = "";
		String ls_pocldet_line_no = "";
		String unit = "";
		String itemCode = "";
		String currCode = "";
		String exchRate = "";
		double exchRateDec = 0.0;
		String quantity  = "";
		double quantityDec = 0.00;
		String value ="";
		double valueDec =0.00;
		String qtyImp = "";
		double qtyImpDec = 0.00;
		String amtImp = "";
		double amtImpDec = 0.00;
		String qty = "";
		double qtyDec =0.00;
		String amt = "";
		double amtDec=0.0;
		double  baseValue = 0.00;
		String merrcode = "";
		PreparedStatement pstmt =null,pstmt1=null;
		ResultSet rs = null,rs1=null;
		String fileNo = "";
		String advCurrCode = "";
		String ls_adv_lic = "";
		//String qty = "";
		String allocqty = "";
		double avl =0.00;
		String ls_allowtax="";
		String mtaxclass="",mtaxchap="",mtaxenv="",tranidrcp="",purcOrder="",poLinenorcp="",retopt="",linenoOrd="";
		try
		{	
			DistCommon disscommon = new DistCommon();

			String detCnt = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"detCnt");
			System.out.println("detCnt ...... :"+detCnt);
			String siteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
			System.out.println("siteCode ...... :"+siteCode);
			if(conn==null)
			{
				ConnDriver connDriver = new ConnDriver();
				//Changes and Commented By Bhushan on 09-06-2016 :START
				//conn = connDriver.getConnectDB("DriverITM");
				conn = getConnection();
				//Changes and Commented By Bhushan on 09-06-2016 :END 
				connDriver=null;
			}
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			System.out.println("[] :currentFormNo ....." +currentFormNo);
			if (dom != null)
			{
				itemCode = genericUtility.getColumnValue("item_code",dom);
				itemCode = itemCode == null ? "" :itemCode.trim();

				//added by monika-23 august 2019
				purcOrder= genericUtility.getColumnValue("purc_order",dom1);
				//	poLineno= genericUtility.getColumnValue("line_no",dom);
				retopt =genericUtility.getColumnValue("ret_opt", dom1);//end
				tranidrcp=genericUtility.getColumnValue("tran_id__rcp",dom);//added by monika 21 oct 2019.
				poLinenorcp= chkNull(genericUtility.getColumnValue("line_no__rcp",dom));	
				linenoOrd= genericUtility.getColumnValue("line_no__ord",dom);//end
				poLinenorcp = "   " + poLinenorcp;
				poLinenorcp = poLinenorcp.substring(poLinenorcp.length() - 3,poLinenorcp.length());
				linenoOrd = "   " + linenoOrd;
				linenoOrd = linenoOrd.substring(linenoOrd.length() - 3,linenoOrd.length());
				System.out.println(" from det line no rcp ["+poLinenorcp.substring(poLinenorcp.length() - 3,poLinenorcp.length()));
				
				System.out.println("SUSTRING VALUE ["+linenoOrd.substring(linenoOrd.length() - 3,linenoOrd.length())+"[POLINE---["+poLinenorcp+"]");
				System.out.println(" from det line no rcp ["+poLinenorcp+" from det line no ord ["+linenoOrd);
			}
			System.out.println(" in if condition line no rcp ["+poLinenorcp);

			sql = " SELECT  stock.site_code , site.descr , "   
					+" stock.item_code as item_code,item.descr as item_descr , "    
					+" stock.loc_code , stock.lot_no , " 
					+" stock.lot_sl , location.inv_stat , "    
					+" stock.retest_date , stock.exp_date , "    
					+" stock.quantity qty,  stock.alloc_qty alloc_qty, "  
					+" invstat.available , stock.unit , "  
					+" stock.potency_perc , stock.batch_no ,"  
					+" stock.dimension ,  stock.supp_code__mfg "   
					+" from  invstat ,  item , location ,    site , stock "
					+" where ( location.inv_stat = invstat.inv_stat ) and     "
					+" ( stock.loc_code = location.loc_code ) and      "
					+" ( stock.item_code = item.item_code ) and  "  
					+" ( site.site_code = stock.site_code ) and  "   
					+" ( ( stock.item_code = '"+itemCode+"' ) and  "      
					+" ( stock.site_code = '"+siteCode+"' ) ) and "     
					+" ( invstat.stat_type <> 'S' ) And "       
					+" ( stock.quantity - case when stock.alloc_qty is null then 0 else stock.alloc_qty end  > 0 ) "
					+" ORDER BY stock.site_code,  "
					+" stock.item_code,	   stock.loc_code, "
					+ "stock.lot_no, " 
					+" stock.lot_sl";
			pstmt = conn.prepareStatement(sql);	
			rs = pstmt.executeQuery();
			while(rs.next())
				//if(rs.next())
			{
				//addedd by monika-23 aug 2019- to add tax chap tax env and tax class
				//String isnull=null;

				if (retopt.equalsIgnoreCase("C")) 
				{
					ls_allowtax = "Y";

				} else {

					ls_allowtax = disscommon.getDisparams("999999",
							"CALC_TAX_ON_REPLACE", conn);
				}
				if (ls_allowtax.equalsIgnoreCase("Y"))
				{

					if(poLinenorcp != null && poLinenorcp.trim().length() > 0)
					{
						sql = "select tax_class,tax_chap,tax_env "
								+ " from porcpdet "
								+ " where tran_id = ? and line_no= ? ";

						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1,tranidrcp);//
						pstmt1.setString(2, poLinenorcp);
						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{
							mtaxclass =rs1.getString("tax_class");
							mtaxchap =rs1.getString("tax_chap");
							mtaxenv = rs1.getString("tax_env");
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;

						System.out.println(" in if condition tran_id rcp["+tranidrcp+"] line no rcp ["+poLinenorcp+" ] tax class["+mtaxclass+"] tax chap["+mtaxchap+"] tax env["+mtaxenv);
					}
					else {

						sql = "select tax_class,tax_chap,tax_env "
								+ " from porddet "
								+ " where purc_order = ? and line_no=?";

						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1,purcOrder);//
						pstmt1.setString(2, linenoOrd);
						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{
							mtaxclass =rs1.getString("tax_class");
							mtaxchap =rs1.getString("tax_chap");
							mtaxenv = rs1.getString("tax_env");
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;

						System.out.println(" in else condition purc_ord rcp["+purcOrder+"] line no ord ["+linenoOrd+" ] tax class["+mtaxclass+"] tax chap["+mtaxchap+"] tax env["+mtaxenv);

					}
				}

				qty = rs.getString(11)== null?"0.00":rs.getString(11);						
				allocqty = rs.getString(12)== null?"0.00":rs.getString(12);						
				avl = Double.parseDouble(qty)-Double.parseDouble(allocqty) ;
				valueXmlString.append("<Detail>\r\n");
				valueXmlString.append("<item_code>").append("<![CDATA[").append(rs.getString("item_code")==null ?"":rs.getString("item_code")).append("]]>").append("</item_code>\r\n");
				valueXmlString.append("<loc_code>").append("<![CDATA[").append(rs.getString(5)==null ?"":rs.getString(5)).append("]]>").append("</loc_code>\r\n");
				valueXmlString.append("<lot_no>").append("<![CDATA[").append(rs.getString(6)==null ?"":rs.getString(6)).append("]]>").append("</lot_no>\r\n");
				valueXmlString.append("<lot_sl>").append("<![CDATA[").append(rs.getString(7)==null ?"":rs.getString(7)).append("]]>").append("</lot_sl>\r\n");
				valueXmlString.append("<available>").append("<![CDATA[").append(rs.getString(13)==null ?"":rs.getString(13)).append("]]>").append("</available>\r\n");
				valueXmlString.append("<unit>").append("<![CDATA[").append(rs.getString(14)==null ?"":rs.getString(14)).append("]]>").append("</unit>\r\n");
				valueXmlString.append("<quantity>").append("<![CDATA[").append(rs.getString(11)==null ?"":rs.getString(11)).append("]]>").append("</quantity>\r\n");						
				valueXmlString.append("<alloc_qty>").append("<![CDATA[").append(rs.getString(12)==null ?"":rs.getString(12)).append("]]>").append("</alloc_qty>\r\n");
				valueXmlString.append("<net_available>").append("<![CDATA[").append(avl).append("]]>").append("</net_available>\r\n");
				//valueXmlString.append("<Inv St>").append("<![CDATA[").append(rs.getString(8)==null ?"":rs.getString(8)).append("]]>").append("</inv_stat>\r\n");
				valueXmlString.append("<retest_date>").append("<![CDATA[").append(rs.getString(9)==null ?"":genericUtility.getValidDateString(rs.getString(9),genericUtility.getDBDateFormat(),genericUtility.getApplDateFormat())).append("]]>").append("</retest_date>\r\n");
				valueXmlString.append("<exp_date>").append("<![CDATA[").append(rs.getString(10)==null ?"":genericUtility.getValidDateString(rs.getString(10),genericUtility.getDBDateFormat(),genericUtility.getApplDateFormat())).append("]]>").append("</exp_date>\r\n");
				valueXmlString.append("<potency>").append("<![CDATA[").append(rs.getString(15)==null ?"":rs.getString(15)).append("]]>").append("</potency>\r\n");
				valueXmlString.append("<supp_code__mfg>").append("<![CDATA[").append(rs.getString(18)==null ?"":rs.getString(18)).append("]]>").append("</supp_code__mfg>\r\n");
				valueXmlString.append("<batch_no>").append("<![CDATA[").append(rs.getString(16)==null ?"":rs.getString(16)).append("]]>").append("</batch_no>\r\n");
				valueXmlString.append("<dimension>").append("<![CDATA[").append(rs.getString(17)==null ?"":rs.getString(17)).append("]]>").append("</dimension>\r\n");
				//addedd by monika-23 aug 2019- to add tax chap tax env and tax class
				valueXmlString.append("<tax_class>").append("<![CDATA[").append(mtaxclass).append("]]>").append("</tax_class>\r\n");
				valueXmlString.append("<tax_chap>").append("<![CDATA[").append(mtaxchap).append("]]>").append("</tax_chap>\r\n");
				valueXmlString.append("<tax_env>").append("<![CDATA[").append(mtaxenv).append("]]>").append("</tax_env>");	
				//end
				valueXmlString.append("</Detail>\r\n");					
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;			



			valueXmlString.append("</Root>\r\n");					
		}//tryBase1 greyd			
		catch(SQLException sqx)
		{
			System.out.println("The Exception caught from AdjIssStkHELPEJB :"+sqx);
			throw new ITMException(sqx);
		}
		catch(Exception e)
		{
			System.out.println("The Exception caught from AdjIssStkHELPEJB :"+e);
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				conn.close();
				conn = null;

			}catch(Exception e){}
		}
		return valueXmlString.toString();
	}		
	private String stockTransform(Document dom,Document dom1, String objContext, String xtraParams, Document selDataDom)throws ITMException
	{
		String tranId = "",siteCode = "",itemCode = "",locCode = "",lotNo = "",lotSl = "";
		String quantity = "",sundryType = "",sundryCode = "",grossRate = "",grade = "",noArt = "";
		String amount = "",dimension = "",grossWt = "",tareWt = "",netWt = "";
		String acctCodeCr = "",acctCodeDr = "",cctrCodeCr = "",cctrCodeDr = "", userId ="", packCode = "";
		String issCriteria = "",stkQtyStr = "",sql = "",errCode = "", errString = "";
		String selLocCode = "",selLotNo = "",selLotSl = "",selUnit = "",selQuantity = "",selStkRate = "0",selLocCodeTo="";
		double stkQty = 0,ordQuantity = 0,selQty = 0, inputQty = 0,integralQty = 0;
		NodeList detailList = null;
		int detailListLength = 0, noART = 0;
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		ConnDriver connDriver = new ConnDriver();
		ITMDBAccessEJB itmDbAccess = new ITMDBAccessEJB();   //Added by Alka on 31/05/2007
		Connection conn = null;
		Statement stmt = null;
		PreparedStatement pstmt =null;
		ResultSet rs = null;
		boolean lbDefault = false;
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();

		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			stmt = conn.createStatement();
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
			//BatchNo = new  ibase.utility.E12GenericUtility().getColumnValue("batch_no",dom);
			selLocCodeTo = genericUtility.getColumnValue("loc_code__to",dom);
			System.out.println("@@@@@ selLocCodeTO from dom "+selLocCodeTo);

			if (quantity == null || "null".equals(quantity) )
			{
				quantity = "0";
			}
			//Added by Alka 31/05/07 -- Entering Required Quantity in the Screen is necessary
			// 20/03/12 manoharan if quantity not entered transfer all
			if (Double.parseDouble(quantity) == 0)
			{
				lbDefault = true;
				//errCode = "VTQUAN";
				//errString = itmDbAccess.getErrorString("quantity", errCode, userId, "", conn);
				//return errString;
			}
			// end Added by Alka 31/05/07

			sql = "SELECT ISS_CRITERIA FROM ITEM WHERE ITEM_CODE =  '"+itemCode+"'";
			rs = stmt.executeQuery(sql);
			if (rs.next())
			{
				issCriteria = rs.getString("ISS_CRITERIA");
			}

			// 20/03/12 manoharan
			detailList = selDataDom.getElementsByTagName("Detail");
			detailListLength = detailList.getLength();
			if (lbDefault == false)
			{
				for (int i = 0;i < detailListLength;i++ )
				{
					stkQtyStr = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("net_available",detailList.item(i));
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
			}
			if (lbDefault == false)
			{
				ordQuantity = Double.parseDouble(quantity);
			}
			// end 20/03/12 manoharan
			System.out.println("issCriteria ::"+issCriteria);
			System.out.println("manohar 20/03/12 ordQuantity  before ["+ordQuantity + "] detailListLength [" + detailListLength + "]");

			for (int ctr = 0;ctr < detailListLength;ctr++)
			{
				selLocCode = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("loc_code",detailList.item(ctr));
				// added by cpatil on 6/11/12 start
				//if(ctr == 0)
				//{
				//selLocCodeTo  = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("loc_code",detailList.item(ctr));

				//}
				// added by cpatil on 6/11/12 end
				//System.out.println("@@@@@ ctr["+ctr+"]:::::::selLocCodeTo :["+selLocCodeTo+"]");
				selLotNo = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("lot_no",detailList.item(ctr));
				selLotSl = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("lot_sl",detailList.item(ctr));
				selUnit = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("unit",detailList.item(ctr));
				selQuantity = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("net_available",detailList.item(ctr));
				//	BatchNo = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("batch_no",detailList.item(ctr));

				// 20/03/12 manoharan
				if (lbDefault == true)
				{
					ordQuantity = Double.parseDouble(selQuantity);
				}
				System.out.println("manohar 20/03/12 ordQuantity  after ["+ordQuantity + "]");
				// 20/03/12 manoharan
				//	selStkRate = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("stock_rate",detailList.item(ctr)); //stock-rate is wrongly placed Gulzar - 10/05/07
				//selStkRate = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("rate",detailList.item(ctr)); //stock-rate is changed to rate Gulzar - 10/05/07
				//if (selStkRate == null)
				//{
				//	selStkRate = "0";
				//}
				valueXmlString.append("<Detail>");
				valueXmlString.append("<item_code isSrvCallOnChg='1'>").append(itemCode).append("</item_code>");
				if (selUnit != null && selUnit.trim().length() > 0)
				{
					valueXmlString.append("<unit>").append(selUnit).append("</unit>");
				}
				if (selLocCode != null && selLocCode.trim().length() > 0)
				{
					valueXmlString.append("<loc_code__fr isSrvCallOnChg='1'>").append(selLocCode).append("</loc_code__fr>");
					//valueXmlString.append("<loc_code__to isSrvCallOnChg='0'>").append(selLocCodeTo).append("</loc_code__to>");  //commented by cpatil on 6/11/12
				}
				// added by cpatil on 6-11-12 start
				if (selLocCodeTo != null && selLocCodeTo.trim().length() > 0)
				{
					valueXmlString.append("<loc_code__to isSrvCallOnChg='1'>").append(selLocCodeTo).append("</loc_code__to>");
				}
				// added by cpatil on 6-11-12 end
				if (selLotNo != null && selLotNo.trim().length() > 0)
				{
					valueXmlString.append("<lot_no__fr isSrvCallOnChg='1'>").append(selLotNo).append("</lot_no__fr>");
					valueXmlString.append("<lot_no__to isSrvCallOnChg='0'>").append(selLotNo).append("</lot_no__to>");
				}
				if (selLotSl != null && selLotSl.trim().length() > 0)
				{
					valueXmlString.append("<lot_sl__fr isSrvCallOnChg='1'>").append(selLotSl).append("</lot_sl__fr>");
					valueXmlString.append("<lot_sl__to isSrvCallOnChg='0'>").append(selLotSl).append("</lot_sl__to>");
				}
				//BatchNo=checknull(BatchNo);
				//System.out.println("batch_no1111"+ BatchNo);
				valueXmlString.append("<amount>").append(amount).append("</amount>");
				//	valueXmlString.append("<batch_no>").append(BatchNo).append("</batch_no>");
				//System.out.println("batch_no"+ BatchNo);
				selQty = Double.parseDouble(selQuantity);
				System.out.println("selQty ::"+selQty);
				if (selQty < ordQuantity)
				{
					valueXmlString.append("<quantity>").append(selQty).append("</quantity>");
					ordQuantity = ordQuantity - selQty;
					inputQty = selQty;
				}
				else
				{
					if (issCriteria != null && issCriteria.equalsIgnoreCase("W"))
					{
						valueXmlString.append("<quantity>").append(selQty).append("</quantity>");
						inputQty = selQty;
					}
					else
					{
						valueXmlString.append("<quantity>").append(ordQuantity).append("</quantity>");
						inputQty = ordQuantity;
						ordQuantity = 0; 
					}	
				}
				// 18/02/12 manoharan
				siteCode = new  ibase.utility.E12GenericUtility().getColumnValue("site_code",dom1);
				itemCode = new  ibase.utility.E12GenericUtility().getColumnValue("item_code",dom);
				sql = "Select qty_per_art,pack_code From stock  "
						+ " WHERE  ITEM_CODE =  ? "
						+ " and  site_CODE =  ? "
						+ " and  loc_CODE =  ? "
						+ " and  lot_no =  ? "
						+ " and  lot_sl =  ? " ;

				pstmt = conn.prepareStatement(sql);	
				pstmt.setString(1,itemCode);
				pstmt.setString(2,siteCode);
				pstmt.setString(3,selLocCode);
				pstmt.setString(4,selLotNo);
				pstmt.setString(5,selLotSl);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					integralQty = rs.getDouble("qty_per_art");
					packCode = rs.getString("pack_code");
				}
				rs.close();
				rs =  null;
				pstmt.close();
				pstmt = null;
				if (integralQty > 0 )
				{

					System.out.println("First inputQty [" + inputQty + "] integralQty [" + integralQty + "] (inputQty % integralQty) [" + (inputQty % integralQty) + "]");
					if( (inputQty % integralQty) > 0 )
					{
						noART = (int) ((inputQty / integralQty) + 1);
						System.out.println("noART 1 [" + noART + "]");
					}
					else
					{
						noART = (int) ((inputQty / integralQty));
						System.out.println("noART 2 [" + noART + "]");
					}

					valueXmlString.append("<no_art isSrvCallOnChg='0'>").append(noART).append("</no_art>");
				}
				else
				{
					if (packCode != null && !"null".equals(packCode) &&  packCode.trim().length() > 0 )
					{
						sql = "select capacity from packing "
								+ " where pack_code = ? ";
						pstmt = conn.prepareStatement(sql);	
						pstmt.setString(1,packCode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							integralQty = rs.getDouble("capacity");
						}
						rs.close();
						rs =  null;
						pstmt.close();
						pstmt = null;
					}
					if (integralQty == 0)
					{
						// 03/02/11 manoharan no_art to be set as per PB logic
						sql = "SELECT integral_qty FROM siteITEM "
								+ " WHERE site_code = ? "
								+ " and ITEM_CODE =  ? ";

						pstmt = conn.prepareStatement(sql);	
						pstmt.setString(1,siteCode);
						pstmt.setString(2,itemCode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							integralQty = rs.getDouble("integral_qty");
						}
						rs.close();
						rs =  null;
						pstmt.close();
						pstmt = null;
						if (integralQty == 0)
						{
							sql = "SELECT integral_qty FROM ITEM "
									+ " WHERE ITEM_CODE =  ? ";

							pstmt = conn.prepareStatement(sql);	
							pstmt.setString(1,itemCode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								integralQty = rs.getDouble("integral_qty");
							}
							rs.close();
							rs =  null;
							pstmt.close();
							pstmt = null;
						}
					}
					if (integralQty > 0) 
					{
						System.out.println("Second inputQty [" + inputQty + "] integralQty [" + integralQty + "] (inputQty % integralQty) [" + (inputQty % integralQty) + "]");
						if( (inputQty % integralQty) > 0 )
						{
							noART = (int) ((inputQty / integralQty) + 1);
							System.out.println("noART 3 [" + noART + "]");
						}
						else
						{
							noART = (int) ((inputQty / integralQty));
							System.out.println("noART 4 [" + noART + "]");
						}
						valueXmlString.append("<no_art isSrvCallOnChg='0'>").append(noART).append("</no_art>");
					}
					// end 03/02/11 manoharan
				} // end 18/02/12 manoharan

				valueXmlString.append("</Detail>");
				//if (ordQuantity == 0 )
				if (ordQuantity == 0 && lbDefault == false)
				{
					break;
				}
			}
			valueXmlString.append("</Root>\r\n");				
		}
		catch (Exception e)
		{
			System.out.println("Exception GenStkHelpAct "+e.getMessage());
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
			catch (Exception se){}
		}
		return valueXmlString.toString();
	}
	/*private String checknull(String string)
	{
		if (string == null)
		{
			string = "";
		}
		return string;
	}*/
	private String chkNull(String input) {
		if (input == null || "null".equalsIgnoreCase(input)) {
			input = "";
		}
		return input;
	}



}//end of Ejb
