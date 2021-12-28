package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import javax.ejb.Stateless; 
import javax.ejb.Stateless;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.fin.FinCommon;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

import java.text.SimpleDateFormat;
import java.util.*;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.ejb.Stateless; 

@Stateless
public class BillofQuantityAct extends ActionHandlerEJB
implements BillofQuantityActLocal, BillofQuantityActRemote
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	//GenericUtility genericUtility = GenericUtility.getInstance();
	String act_type = null;

	@Override
	public String actionHandler()
			throws RemoteException, ITMException
			{
		return "";
			}

	@Override
	public String actionHandler(String actionType, String xmlString, String xmlString1, String objContext, String xtraParams)
			throws RemoteException, ITMException
			{
		Document dom = null;
		Document dom1 = null;
		String retString = null;
		try
		{
			if ((xmlString != null) && (xmlString.trim().length() != 0))
			{
				System.out.println("XML String ===================>:" + xmlString);
				dom = this.genericUtility.parseString(xmlString);
				System.out.println("dom :" + dom);
			}
			if ((xmlString1 != null) && (xmlString1.trim().length() != 0))
			{
				System.out.println("@@@@@XML String1 ===================>:" + xmlString1);
				dom1 = this.genericUtility.parseString(xmlString1);
			}

			System.out.println("actionType:" + actionType + ":");

			if (actionType.equalsIgnoreCase("ITEMList"))
			{
				retString = getItemListDetails(dom, dom1, objContext, xtraParams);
			}

		}
		catch (Exception e)
		{
			System.out.println("Exception :ModelHandler :actionHandler(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning String from ModelHandler : actionHandler" + retString);
		return retString;
			}

	@Override
	public String actionHandlerTransform(String actionType, String xmlString, String xmlString1, String objContext, String xtraParams, String selDataStr) throws RemoteException, ITMException
	{
		System.out.println("[PorderActEJB] actionHandlerTransform is calling.............");
		Document dom = null;
		Document dom1 = null;
		Document selDataDom = null;

		String retString = null;
		try
		{
			E12GenericUtility genericUtility= new  E12GenericUtility();
			if ((xmlString != null) && (xmlString.trim().length() != 0))
			{
				//dom = GenericUtility.getInstance().parseString(xmlString);
				dom = genericUtility.parseString(xmlString);
			}
			if ((xmlString1 != null) && (xmlString1.trim().length() != 0))
			{
				//dom1 = GenericUtility.getInstance().parseString(xmlString1);
				dom1 = genericUtility.parseString(xmlString1);
			}
			if ((selDataStr != null) && (selDataStr.length() > 0))
			{
				//selDataDom = GenericUtility.getInstance().parseString(selDataStr);
				selDataDom = genericUtility.parseString(selDataStr);
			}

			System.out.println("actionType:" + actionType + ":");

			if (actionType.equalsIgnoreCase("ITEMList"))
			{
				retString = itemListTransform(dom, dom1, objContext, xtraParams, selDataDom);
			}
		}
		catch (Exception e)
		{
			System.out.println("Exception :PorderActEJB :actionHandlerTransform(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning String from PorderActEJB : actionHandlerTransform" + retString);
		return retString;
	}

	private String getItemListDetails(Document dom, Document dom1, String objContext, String xtraParams)
			throws RemoteException, ITMException
			{
		System.out.println("getItemListDetails is callled..");
		String itemCode = ""; String itemSer = ""; String unit = ""; String sql = ""; String sql1 = ""; String unitStd = ""; String unitRate = ""; String convRateStduom = ""; String packCode = ""; String remarks = ""; String amount = ""; String siteCode = ""; String indentNo = "";
		String packInstr = ""; String specInstr = ""; String splInstr = ""; String hremarks = ""; String currCode = ""; String exchRate = ""; String projCode = ""; String hstatus = ""; String amountBc = ""; String suppCodePref = ""; String hunit = ""; String hamount = ""; String splInstrh = ""; String hsplInstr = "";
		String hRate = ""; String tranId = ""; String hItemCode = ""; String htranType = ""; String hindentNo = ""; String hunitStd = ""; String hpackCode = ""; String hpackInstr = ""; String netAmountBc = ""; String hunitRate = ""; String hspecInstr = "",hsitecode = "",hitemSer = "",ditemCode = "",dItemCode = "",confirm = "", errString = "" ;
		String loginSite = "",itemCodeI = "", boqId = "";
		String empCodeQcaprv = "";
		String descr = "";
		String locCode = "";
		String empFname = "";
		String empMname = "";
		String empLname = "";
		String taxClass = "";
		String taxChap = "";
		String taxEnv = "";
		String discountType = "";
		String bomCode = "";
		String contractNo = "";
		String priceList = "",acctCode = "",cctrCode = "";
		java.util.Date reqDate = null , dlvDate = null; 
		String siteCodeDlv="";
		StringBuffer XmlString = new StringBuffer();
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		PreparedStatement pstmt = null; PreparedStatement pstmt1 = null;
		ResultSet rs = null , rs1 = null;
		Connection conn = null;

		double quantity = 0.0D , discount = 0.0D; double hquantity = 0.0D; double rate = 0.0D; double hrate = 0.0D; double quantityStduom = 0.0D; double convQtyStduom = 0.0D;
		double hquantityStduom = 0.0D; double hconvQtyStduom = 0.0D; double hnetAmount = 0.0D; double hnetAmountBc = 0.0D; double hconvRateStduom = 0.0D; double hnoArt = 0.0D; double noArt = 0.0D; double rateStduom = 0.0D,hrateStduom = 0.0;
		ConnDriver connDriver = new ConnDriver();

		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility= new  E12GenericUtility();
																																																																																																																																																																																																																																																																																																																																																																																	
		try
		{
			loginSite = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			System.out.println("loginSite---->>>["+loginSite+"]------>>>>>");
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			itemCode = genericUtility.getColumnValue("item_code", dom);
			boqId = genericUtility.getColumnValue("tran_id__boq", dom1);
			contractNo = genericUtility.getColumnValue("contract_no", dom1);

			siteCodeDlv = genericUtility.getColumnValue("site_code__dlv", dom1);    // SITE_CODE__DLV
			System.out.println("@@@@@@@@@siteCodeDlv["+siteCodeDlv+"]");
			System.out.println("item code = " + itemCode+"indent no -->>"+indentNo+"boq id-->> "+boqId+"     contractNo-->>"+contractNo);
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			//changes for temp test 
			sql = "select confirmed from boqhdr where tran_id = ?";
			pstmt = conn.prepareStatement(sql);
			//dItemCode
			pstmt.setString(1, boqId);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				confirm = checkNull(rs.getString("confirmed"));
				System.out.println("confirm --->>>["+confirm+"]");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			//end of changes 
			//END OF ADDITION ON 05/03/2013
			if("Y".equalsIgnoreCase(confirm))
			{
				System.out.println("SQL START DETAIL-->>");
				sql = "select (case when d.item_code is null then ' ' else d.item_code end) as item_code," +
						"(case when d.item_ser is null then ' ' else d.item_ser end) as item_ser," +
						"(case when d.tran_id is null then ' ' else d.tran_id end) as tran_id," +
						"(case when d.quantity is null then 0 else d.quantity end) as quantity," +
						"(case when d.unit is null then ' ' else d.unit end) as unit," +
						"(case when d.quantity__stduom is null then 0 else d.quantity__stduom end) as quantity__stduom," +
						"(case when d.conv__qty_stduom is null then 0 else d.conv__qty_stduom end) as conv__qty_stduom," +
						"(case when d.unit__std is null then ' ' else d.unit__std end) as unit__std," +
						"(case when d.rate is null then 0 else d.rate end) as rate ," +
						"(case when d.unit__rate is null then ' ' else d.unit__rate end) as unit__rate, " +
						"(case when d.conv__rate_stduom is null then 0 else d.conv__rate_stduom end) as conv__rate_stduom ," +
						"(case when d.no_art is null then 0 else d.no_art end) as no_art," +
						"(case when d.pack_code is null then ' ' else d.pack_code end) as pack_code," +
						"(case when d.remarks is null then ' ' else d.remarks end) as remarks," +
						"(case when d.pack_instr is null then ' ' else d.pack_instr end) as pack_instr," +
						"(case when d.spec_instr is null then ' ' else d.spec_instr end) as spec_instr," +
						"(case when d.spl_instr is null then ' ' else d.spl_instr end) as spl_instr," +

						"(case when d.tax_class is null then '' else d.tax_class end) as tax_class," +
						"(case when d.tax_chap is null then '' else d.tax_chap end) as tax_chap," +
						"(case when d.tax_env is null then '' else d.tax_env end) as tax_env," +
						" d.req_date as req_date," +

						"(case when h.item_code is null then ' ' else h.item_code end) as hitemcode , " +
						"(case when h.site_code is null then ' ' else h.site_code end) as hsitecode , " +
						//selecting proj code from hder boq
						"(case when h.proj_code is null then ' ' else h.proj_code end) as proj_code , " +
						//	" '' as hindentno , " +
						"(case when d.rate__stduom is null then 0 else d.rate__stduom end) as rate__stduom," +
						"(case when d.amount is null then 0 else d.amount end) as amount ,d.dlv_date, d.acct_code__dr ,d.cctr_code__dr " +
						"from boqdet d,boqhdr h where d.tran_id = h.tran_id  and h.tran_id = ? and h.site_code = ?";

				System.out.println("Detail data sql [PorderActEJB] sql==>" + sql);
				pstmt = conn.prepareStatement(sql);
				//dItemCode
				pstmt.setString(1,boqId);
				//changes by cpandey according to new changes on purchase order on 04/01/13
				pstmt.setString(2,loginSite);
				//end of changes 
				rs = pstmt.executeQuery();
				while (rs.next())
				{
					ditemCode = checkNull(rs.getString("item_code"));
					System.out.println("Deatail ITEM CODE --->>[" + itemCode + "]");
					itemSer = checkNull(rs.getString("item_ser"));
					tranId = checkNull(rs.getString("tran_id"));
					System.out.println("tranId--[" + tranId + "]>>");
					quantity = rs.getDouble("quantity");
					System.out.println("quantity --->>[" + quantity + "]");
					unit = checkNull(rs.getString("unit"));
					quantityStduom = rs.getDouble("quantity__stduom");
					rate = rs.getDouble("rate");
					convQtyStduom = rs.getDouble("conv__qty_stduom");
					unitStd = checkNull(rs.getString("unit__std"));
					unitRate = checkNull(rs.getString("unit__rate"));
					convRateStduom = checkNull(rs.getString("conv__rate_stduom"));
					noArt = rs.getDouble("no_art");
					packCode = checkNull(rs.getString("pack_code"));
					remarks = checkNull(rs.getString("remarks"));
					packInstr = checkNull(rs.getString("pack_instr"));
					specInstr = checkNull(rs.getString("spec_instr"));
					splInstr = checkNull(rs.getString("spl_instr"));

					taxClass = checkNull(rs.getString("tax_class"));
					taxChap = checkNull(rs.getString("tax_chap"));
					taxEnv = checkNull(rs.getString("tax_env"));
					reqDate = rs.getDate("req_date");
					System.out.println("@@@@@ 1 taxClass["+taxClass+"]::taxChap["+taxChap+"]:::taxEnv["+taxEnv+"]");
					amount = checkNull(rs.getString("amount"));
					hItemCode = checkNull(rs.getString("hitemcode"));//
					System.out.println("hItemCode-->>["+hItemCode+"]");
					siteCode = siteCodeDlv;
//					siteCode = checkNull(rs.getString("hsitecode"));        // commented by cpatil 24-09-13
					System.out.println("siteCode-->>["+siteCode+"]");
					//hindentNo = checkNull(rs.getString("hindentno"));
					//System.out.println("indentNo-->>["+indentNo+"]");
					projCode = checkNull(rs.getString("proj_code"));
					System.out.println("proj_code-->>["+projCode+"]");
					rateStduom = rs.getDouble("rate__stduom");
					System.out.println("te Stduom --[" + rateStduom + "]>>");

					dlvDate = rs.getDate("dlv_date");
					acctCode = checkNull(rs.getString("acct_code__dr"));
					cctrCode = checkNull(rs.getString("cctr_code__dr"));

					sql = "select descr from item where item_code = ?  ";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, ditemCode);
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						descr = rs1.getString("descr");
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;

					//Added by akhilesh to show other fields that is set in porder details itm change of item_code  




					System.out.println("ditemCode-->>["+ditemCode+"]");
					System.out.println("siteCode-->>["+siteCode+"]");

					XmlString =   itmCodeItmChanges(dom,dom1,ditemCode,contractNo,siteCode,quantity,unitStd,rate,acctCode,cctrCode,conn);
					System.out.println("valueXmlString:::"+XmlString);
					valueXmlString.append(XmlString); // 26-nov-2019 manoharan

					valueXmlString.append("<Detail>\r\n");
					valueXmlString.append("<item_code>").append("<![CDATA[").append(ditemCode).append("]]>").append("</item_code>\r\n");

					valueXmlString.append("<item_descr>").append("<![CDATA[").append(descr).append("]]>").append("</item_descr>\r\n");

					valueXmlString.append("<item_ser>").append("<![CDATA[").append(itemSer).append("]]>").append("</item_ser>\r\n");
					valueXmlString.append("<rate>").append("<![CDATA[").append(rate).append("]]>").append("</rate>\r\n");
					valueXmlString.append("<quantity>").append("<![CDATA[").append(quantity).append("]]>").append("</quantity>\r\n");
					valueXmlString.append("<unit>").append("<![CDATA[").append(unit).append("]]>").append("</unit>\r\n");
					valueXmlString.append("<amount>").append("<![CDATA[").append(amount).append("]]>").append("</amount>\r\n");
					valueXmlString.append("<no_art>").append("<![CDATA[").append(noArt).append("]]>").append("</no_art>\r\n");
					valueXmlString.append("<pack_code>").append("<![CDATA[").append(packCode).append("]]>").append("</pack_code>\r\n");
					valueXmlString.append("<pack_instr>").append("<![CDATA[").append(packInstr).append("]]>").append("</pack_instr>\r\n");

					valueXmlString.append("<specific_instr>").append("<![CDATA[").append(specInstr).append("]]>").append("</specific_instr>\r\n");
					valueXmlString.append("<special_instr>").append("<![CDATA[").append(splInstr).append("]]>").append("</special_instr>\r\n");

					valueXmlString.append("<site_code>").append("<![CDATA[").append(siteCode).append("]]>").append("</site_code>\r\n");
					valueXmlString.append("<unit__rate>").append("<![CDATA[").append(unitRate).append("]]>").append("</unit__rate>\r\n");
					valueXmlString.append("<conv__qty_stduom>").append("<![CDATA[").append(convQtyStduom).append("]]>").append("</conv__qty_stduom>\r\n");
					valueXmlString.append("<conv__rate_stduom>").append("<![CDATA[").append(convRateStduom).append("]]>").append("</conv__rate_stduom>\r\n");
					valueXmlString.append("<unit__std>").append("<![CDATA[").append(unitStd).append("]]>").append("</unit__std>\r\n");
					valueXmlString.append("<quantity__stduom>").append("<![CDATA[").append(quantityStduom).append("]]>").append("</quantity__stduom>\r\n");
					valueXmlString.append("<rate__stduom>").append("<![CDATA[").append(rateStduom).append("]]>").append("</rate__stduom>\r\n");
					System.out.println("ITEM CODE FROM INDENT  ["+itemCodeI+"] AND BILL OF QUANITIY ["+dItemCode+"] ");
					valueXmlString.append("<ind_no>").append("<![CDATA["+" "+"]]>").append("</ind_no>\r\n");
					valueXmlString.append("<proj_code>").append("<![CDATA[").append(projCode).append("]]>").append("</proj_code>\r\n");
					valueXmlString.append("<remarks>").append("<![CDATA[").append(remarks).append("]]>").append("</remarks>\r\n");

					valueXmlString.append("<tax_class>").append("<![CDATA[").append(taxClass).append("]]>").append("</tax_class>\r\n");
					valueXmlString.append("<tax_chap>").append("<![CDATA[").append(taxChap).append("]]>").append("</tax_chap>\r\n");
					valueXmlString.append("<tax_env>").append("<![CDATA[").append(taxEnv).append("]]>").append("</tax_env>\r\n");
					valueXmlString.append("<tax_amt>").append("<![CDATA[0]]>").append("</tax_amt>\r\n");
					valueXmlString.append("<discount>").append("<![CDATA[0]]>").append("</discount>\r\n");
					if(reqDate == null)
					{
						valueXmlString.append("<req_date>").append("<![CDATA[").append(sdf.format(new Date())).append("]]>").append("</req_date>\r\n");
					}
					else
					{
						valueXmlString.append("<req_date>").append("<![CDATA[").append(sdf.format(reqDate)).append("]]>").append("</req_date>\r\n");
					}

					if(dlvDate == null)
					{
						valueXmlString.append("<dlv_date>").append("<![CDATA[").append(sdf.format(new Date())).append("]]>").append("</dlv_date>\r\n");
					}
					else
					{
						valueXmlString.append("<dlv_date>").append("<![CDATA[").append(sdf.format(dlvDate)).append("]]>").append("</dlv_date>\r\n");
					}
					System.out.println("value of valueXmlString 1"+valueXmlString);

					//valueXmlString.append(XmlString);// = itmCodeItmChanges(dom, dom1, dItemCode, contractNo, hsitecode);	

					System.out.println("value of valueXmlString 2 "+valueXmlString);

					valueXmlString.append("</Detail>\r\n");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;



				//				sql1 = "select " +
				//						"(case when h.item_code is null then ' ' else h.item_code end) as item_code ," +
				//						"(case when h.quantity is null then 0 else h.quantity end) as quantity," +
				//						"(case when h.item_ser is null then ' ' else h.item_ser end ) as item_ser ," +
				//						"(case when h.site_code is null then ' ' else h.site_code end) as site_code," +
				//						"(case when h.indent_no is null then ' ' else h.indent_no end ) as indent_no ," +
				//						"(case when h.remarks is null then ' ' else h.remarks end) as remarks," +
				//						"(case when h.curr_code is null then ' ' else h.curr_code end) as curr_code," +
				//						"(case when h.exch_rate is null then 0 else h.exch_rate end) as exch_rate," +
				//						"(case when h.proj_code is null then ' ' else h.proj_code end) as proj_code," +
				//						"(case when h.status is null then ' ' else h.status end) as status," +
				//						"(case when h.amount__bc is null then 0 else h.amount__bc end) as amount__bc," +
				//						"(case when h.supp_code__pref is null then ' ' else h.supp_code__pref end) as supp_code__pref," +
				//						"(case when h.unit is null then ' ' else h.unit end) as unit," +
				//						"(case when h.amount is null then 0 else h.amount end) as amount,  h.tran_date as tran_date, " +
				//						"(case when h.tran_type is null then ' 'else h.tran_type end) as tran_type, " +
				//						//"(case when h.indent_no is null then ' ' else h.indent_no end) as indent_no, " +
				//						"(case when h.quantity__stduom is null then 0 else h.quantity__stduom end) as quantity__stduom, " +
				//						"(case when h.conv__qty_stduom is null then 0 else h.conv__qty_stduom end) as conv__qty_stduom, " +
				//						"(case when h.unit__std is null then ' ' else h.unit__std end) as unit__std, " +
				//						"(case when h.rate is null then 0 else h.rate end) as rate, " +
				//						"(case when h.net_amount is null then 0 else h.net_amount end) as net_amount, " +
				//						"(case when h.pack_code is null then ' ' else h.pack_code end) as pack_code, " +
				//						"(case when h.pack_instr is null then ' ' else h.pack_instr end) as pack_instr, " +
				//						"(case when h.spl_instr is null then ' ' else h.spl_instr end) as spl_instr, " +
				//						"(case when h.net_amount__bc is null then 0 else h.net_amount__bc end) as net_amount__bc, " +
				//						"(case when h.unit__rate is null then ' ' else h.unit__rate end) as unit__rate, " +
				//						"(case when h.conv__rate_stduom is null then 0 else h.conv__rate_stduom end) as conv__rate_stduom," +
				//						"(case when h.spec_instr is null then ' ' else h.spec_instr end) as spec_instr, " +
				//						"(case when h.no_art is null then 0 else h.no_art end) as no_art," +
				//						//added project code to set on purchase order 
				//						"(case when h.proj_code is null then ' ' else h.proj_code end) as proj_code , " +
				//						"(case when h.rate__stduom is null then 0 else h.rate__stduom end) as rate__stduom from boqhdr h,item itm where itm.item_code = h.item_code and h.indent_no = ? and h.site_code = ?";
				//
				//				System.out.println("[Header Data for PorderActEJB] sql==>" + sql);
				//				pstmt1 = conn.prepareStatement(sql1);
				//				//
				//				pstmt1.setString(1,indentNo);
				//				//changes by cpandey according to new changes on purchase order on 04/01/13 
				//				pstmt1.setString(2,loginSite);
				//				//end oc changes 
				//				rs = pstmt1.executeQuery();
				//				while (rs.next())
				//				{
				//					hItemCode = rs.getString("item_code");
				//					hitemSer = checkNull(rs.getString("item_ser"));
				//					hquantity = rs.getDouble("quantity");
				//					hremarks = checkNull(rs.getString("remarks"));
				//					currCode = checkNull(rs.getString("curr_code"));
				//					siteCode = checkNull(rs.getString("site_code"));
				//					exchRate = checkNull(rs.getString("exch_rate"));
				//					projCode = checkNull(rs.getString("proj_code"));
				//					hstatus = checkNull(rs.getString("status"));
				//					amountBc = checkNull(rs.getString("amount__bc"));
				//					suppCodePref = checkNull(rs.getString("supp_code__pref"));
				//					hunit = checkNull(rs.getString("unit"));
				//					hamount = checkNull(rs.getString("amount"));
				//					htranType = checkNull(rs.getString("tran_type"));
				//					//hindentNo = checkNull(rs.getString("indent_no"));
				//					hquantityStduom = rs.getDouble("quantity__stduom");
				//					hconvQtyStduom = rs.getDouble("conv__qty_stduom");
				//					hunitStd = checkNull(rs.getString("unit__std"));
				//					hrate = rs.getDouble("rate");
				//					hnetAmount = rs.getDouble("net_amount");
				//					hpackCode = checkNull(rs.getString("pack_code"));
				//					hpackInstr = checkNull(rs.getString("pack_instr"));
				//					hsplInstr = checkNull(rs.getString("spl_instr"));
				//					hspecInstr = checkNull(rs.getString("spec_instr"));
				//					hnoArt = rs.getDouble("no_art");
				//					hnetAmountBc = rs.getDouble("net_amount__bc");
				//					hunitRate = checkNull(rs.getString("unit__rate"));
				//					hconvRateStduom = rs.getDouble("conv__rate_stduom");
				//					projCode = checkNull(rs.getString("proj_code"));
				//					hrateStduom = rs.getDouble("rate__stduom");
				//
				//					valueXmlString.append("<Detail>\r\n");
				//					valueXmlString.append("<item_code>").append("<![CDATA[").append(hItemCode).append("]]>").append("</item_code>\r\n");
				//					valueXmlString.append("<item_ser>").append("<![CDATA[").append(hitemSer).append("]]>").append("</item_ser>\r\n");
				//					valueXmlString.append("<rate>").append("<![CDATA[").append(hrate).append("]]>").append("</rate>\r\n");
				//					valueXmlString.append("<quantity>").append("<![CDATA[").append(hquantity).append("]]>").append("</quantity>\r\n");
				//					valueXmlString.append("<unit>").append("<![CDATA[").append(hunit).append("]]>").append("</unit>\r\n");
				//					valueXmlString.append("<amount>").append("<![CDATA[").append(hamount).append("]]>").append("</amount>\r\n");
				//					valueXmlString.append("<no_art>").append("<![CDATA[").append(hnoArt).append("]]>").append("</no_art>\r\n");
				//					valueXmlString.append("<pack_code>").append("<![CDATA[").append(hpackCode).append("]]>").append("</pack_code>\r\n");
				//					valueXmlString.append("<pack_instr>").append("<![CDATA[").append(hpackInstr).append("]]>").append("</pack_instr>\r\n");
				//					valueXmlString.append("<spec_instr>").append("<![CDATA[").append(hspecInstr).append("]]>").append("</spec_instr>\r\n");
				//					valueXmlString.append("<spl_instr>").append("<![CDATA[").append(hsplInstr).append("]]>").append("</spl_instr>\r\n");
				//					valueXmlString.append("<site_code>").append("<![CDATA[").append(siteCode).append("]]>").append("</site_code>\r\n");
				//					valueXmlString.append("<unit__rate>").append("<![CDATA[").append(hunitRate).append("]]>").append("</unit__rate>\r\n");
				//					valueXmlString.append("<conv__qty_stduom>").append("<![CDATA[").append(hconvQtyStduom).append("]]>").append("</conv__qty_stduom>\r\n");
				//					valueXmlString.append("<conv__rate_stduom>").append("<![CDATA[").append(hconvRateStduom).append("]]>").append("</conv__rate_stduom>\r\n");
				//					valueXmlString.append("<unit__std>").append("<![CDATA[").append(hunitStd).append("]]>").append("</unit__std>\r\n");
				//					valueXmlString.append("<quantity__stduom>").append("<![CDATA[").append(hquantityStduom).append("]]>").append("</quantity__stduom>\r\n");
				//					valueXmlString.append("<rate__stduom>").append("<![CDATA[").append(hrateStduom).append("]]>").append("</rate__stduom>\r\n");
				//					System.out.println("remove comment from according to item change -----06/02/2013----->>>>>>>. ");
				//					System.out.println("item code from indent ["+itemCodeI+"]and item code from bill of .["+dItemCode+"] ");
				//					//valueXmlString.append("<ind_no>").append("<![CDATA[").append(hindentNo).append("]]>").append("</ind_no>\r\n");
				//					//added project code to set on purchase order  
				//					valueXmlString.append("<proj_code>").append("<![CDATA[").append(projCode).append("]]>").append("</proj_code>\r\n");
				//					valueXmlString.append("<remarks>").append("<![CDATA[").append(remarks).append("]]>").append("</remarks>\r\n");
				//					valueXmlString.append("</Detail>\r\n");
				//	}
				//				rs.close();
				//				rs = null;
				//				pstmt1.close();
				//				pstmt1 = null;


				valueXmlString.append("</Root>\r\n");
			}
			else 
			{
				errString = itmDBAccessEJB.getErrorString("","UNCONFBIL","","",conn);
				return errString;
			}

			System.out.println("value of valueXmlString 3 "+valueXmlString);
			System.out.println("Header data from boqhdr ITEM CODE -->>" + itemCode);
			System.out.println("Header data from boqhdr-->>");
		}
		catch (Exception e)
		{
			System.out.println("Exception : PorderActEJB : actionHandler :(Document dom)" + e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("Closing Connection.....");
				conn.close();
				conn = null;
			} catch (Exception localException1) {
			}
		}
		return genericUtility.serializeDom(genericUtility.parseString(valueXmlString.toString()));
			} 

	private StringBuffer itmCodeItmChanges(Document dom, Document dom1,String ditemCode, String contractNo,String siteCode,double quantity,String  unitStd,double rate,String  boqAcctCode,String  boqCctrCode, Connection conn) throws ITMException 
	{
		System.out.println("itmCodeItmChanges called....");
		String empLname = " ";
		//String taxClass = "";
		//String taxChap = "";
		String sql  = "";
		String locCodeAprv = "";
		String locCodeInsp = "";
		String qcreqd = "";
		String empCodeQcaprv = " ";
		//String descr = "";
		String locCode = "";
		String empFname = " ";
		String empMname = " ";
		//String taxEnv = "";
		//String discountType = "";
		//String bomCode = "";
		String priceListClg = "";
		String priceList = "";
		String ordDate = "";
		String suppCode = "";
		String itemSer = "";
		String pordType = "";
		//String cctrCode = "";
		//String acctCode = "";
		String itemCodeRef = "";
		//String indentNo = "";
		//String suppCodeMnfr = " ";
		//String qcReqd = "" ;
		//String costctr = "";
		//String CctrLoccode = "";
		//String acctCr = "" ;
		//String cctrCr = "" ;
		String acctCr = "            " ;
		String cctrCr = "      " ;
		String invacct = "";
		String invacctQc = "";
		String acctDr = "            " ;
		String cctrDr = "      " ;
		String Cr[] = null;
		String Dr[] = null;
		//String  unitStd = "";
		String projectCode = "", acctDet = "";

		//SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yy");
		double rateClg = 0.0;
		//double discount = 0.0;
		double stdrate = 0.0 ;
		double actualCost = 0.0;
		double stdCode = 0.0 ;
		//double quantity = 0.0 ;
		//double rate = 0.0 ;
		FinCommon finCommon = new FinCommon();
		DistCommon disCommon = new DistCommon();
		StringBuffer XmlString = new StringBuffer();
		//ConnDriver connDriver = new ConnDriver();
		PreparedStatement pstmt = null; 
		ResultSet rs = null;
		//Connection conn = null;
		try 
		{
			//conn = connDriver.getConnectDB("DriverITM");

			sql = "Select loc_code, emp_code__qcaprv ,item_ser from item where item_code = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,ditemCode);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				locCode = checkNull(rs.getString(1));
				empCodeQcaprv = checkNull(rs.getString("emp_code__qcaprv"));
				itemSer = checkNull(rs.getString("item_ser"));
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			sql = "select loc_code__aprv,loc_code__insp from siteitem where site_code = ? and item_code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,siteCode);
			pstmt.setString(2,ditemCode);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				locCodeAprv = checkNull(rs.getString("loc_code__aprv"));
				locCodeInsp = checkNull(rs.getString("loc_code__insp"));	
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			qcreqd = qcReqd(siteCode,ditemCode,conn);

			if(qcreqd.equalsIgnoreCase("Y") && locCodeInsp != null && locCodeInsp.trim().length() > 0)
			{
				XmlString.append("<loc_code>").append("<![CDATA[").append(locCodeInsp).append("]]>").append("</loc_code>\r\n");
			}
			else if(qcreqd.equalsIgnoreCase("N") && locCodeAprv != null && locCodeAprv.trim().length() > 0)
			{
				XmlString.append("<loc_code>").append("<![CDATA[").append(locCodeAprv).append("]]>").append("</loc_code>\r\n");
			}
			else
			{
				XmlString.append("<loc_code>").append("<![CDATA[").append(locCode).append("]]>").append("</loc_code>\r\n");
			}


			sql = "select emp_fname,emp_mname,emp_lname from employee where emp_code = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,empCodeQcaprv);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				empFname = rs.getString("emp_fname") == null ?" ":rs.getString("emp_fname");
				empMname = rs.getString("emp_mname") == null ?" ":rs.getString("emp_mname");
				empLname = rs.getString("emp_lname") == null ?" ":rs.getString("emp_lname");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			XmlString.append("<emp_fname>").append("<![CDATA[").append(empFname).append("]]>").append("</emp_fname>\r\n");	
			XmlString.append("<emp_mname>").append("<![CDATA[").append(empMname).append("]]>").append("</emp_mname>\r\n");	
			XmlString.append("<emp_lname>").append("<![CDATA[").append(empLname).append("]]>").append("</emp_lname>\r\n");

			ordDate =genericUtility.getColumnValue("ord_date", dom1);
			priceList = disCommon.getDisparams("999999", "STD_PO_PL", conn);

			if(priceList == null || priceList.trim().length() == 0 || priceList.equals("NULLFOUND"))
			{
				priceList = genericUtility.getColumnValue("price_list", dom1);
				//unitStd =genericUtility.getColumnValue("unit__std", dom);
				sql = " select rate from pricelist where price_list = ? and item_code = ? and unit= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,priceList);
				pstmt.setString(2,ditemCode);
				pstmt.setString(3,unitStd);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					stdrate = rs.getDouble(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}
			else
			{
				stdrate = disCommon.pickRate(priceList,ordDate,ditemCode,"","L", conn);
			}

			XmlString.append("<std_rate>").append("<![CDATA[").append(stdrate).append("]]>").append("</std_rate>\r\n");
			if(empCodeQcaprv == null || empCodeQcaprv.trim().length() == 0)
			{
				empCodeQcaprv = " ";
			}


			XmlString.append("<emp_code__qcaprv>").append("<![CDATA[").append(empCodeQcaprv).append("]]>").append("</emp_code__qcaprv>\r\n");


			//if isnull(ls_pricelist) or ls_pricelist = 'NULLFOUND' then
			//ls_pricelist = dw_header.getitemString(1,&quot;price_list&quot;)
			//unitStd = dw_detedit[ii_currformno].getitemString(1,&quot;unit__std&quot;)


			/*unitStd =genericUtility.getColumnValue("unit__std", dom1);
			sql = " select rate from pricelist where price_list = ? and item_code = ? and unit= ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,priceList);
			pstmt.setString(2,ditemCode);
			pstmt.setString(3,unitStd);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				stdrate = rs.getDouble(1);
			}
			else
			{

				stdrate = disCommon.pickRate(priceList,ordDate,ditemCode,"","L", conn);

			}

			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			XmlString.append("<std_rate>").append("<![CDATA[").append(stdrate).append("]]>").append("</std_rate>\r\n");*/


			//itemSer = genericUtility.getColumnValue("item_ser", dom1);

			System.out.println("boqAcctCode="+boqAcctCode+"boqCctrCode="+boqCctrCode);

			pordType = genericUtility.getColumnValue("pord_type", dom1); 
			System.out.println("pordType::"+pordType);
			if(boqAcctCode != null && boqAcctCode.trim().length() > 0)
			//if(boqAcctCode != null )	
			{
				acctDr = boqAcctCode;
				cctrDr = boqCctrCode;
			}
			if(acctDr == null || acctDr.trim().length() == 0)
			//if(acctDr == null )
			{
				acctDet = 	finCommon.getAcctDetrTtype(ditemCode, itemSer, "IN", pordType, conn);
				System.out.println("cctrCode::"+acctDet);
				if(acctDet != null)
				{
					Dr = acctDet.split(",");
					System.out.println(Dr.toString()+"   "+Dr.length);
					if(Dr.length > 0)
					{
						acctDr = Dr[0];
					}
					if(Dr.length > 1)//change done by kunal on 13/04/13 for set cctr code
					{
						cctrDr = Dr[1];
					}
					else
					{
						cctrDr = "      ";
					}
					System.out.println("acctCode::"+acctDr); 
				}
			}
			//XmlString.append("<acct_code__dr>").append("<![CDATA[").append(acctDr).append("]]>").append("</acct_code__dr>\r\n");
			//XmlString.append("<cctr_code__dr>").append("<![CDATA[").append(cctrDr).append("]]>").append("</cctr_code__dr>\r\n");


			/*pordType = genericUtility.getColumnValue("ord_date", dom1);
			mcctr = nvo_dis_obj.gbf_acct_detr_ttype(mcode,ls_itemser,"IN", pordType);
			macct = f_get_token(mcctr,'~t');
			 */

			//contractNo IS NULL FOR BOQ
			/*System.out.println("contractNo="+contractNo); 
			if(contractNo != null && contractNo.trim().length() >  0)
			{
				sql = "select loc_code discount_type,discount,bom_code from pcontract_det where contract_no = ? and item_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,contractNo);
				pstmt.setString(2,ditemCode);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					locCode = checkNull(rs.getString("loc_code"));
					discountType = checkNull(rs.getString("discount_type"));
					discount = rs.getDouble("discount");
					bomCode = checkNull(rs.getString("bom_code"));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

			}
			XmlString.append("<loc_code>").append("<![CDATA[").append(locCode).append("]]>").append("</loc_code>\r\n");
			XmlString.append("<discount_type>").append("<![CDATA[").append(discountType).append("]]>").append("</discount_type>\r\n");
			XmlString.append("<discount>").append("<![CDATA[").append(discount).append("]]>").append("</discount>\r\n");
			XmlString.append("<bom_code>").append("<![CDATA[").append(bomCode).append("]]>").append("</bom_code>\r\n");
			 */
			//XmlString.append("<contract_detail>").append("<![CDATA[").append("").append("]]>").append("</contract_detail>\r\n");



			/*ordDate =genericUtility.getColumnValue("ord_date", dom1);
			priceList = disCommon.getDisparams("999999", "STD_PO_PL", conn);

			rate = disCommon.pickRate(priceList, ordDate, ditemCode, "", "L", conn);

			XmlString.append("<rate__clg>").append("<![CDATA[").append(rate).append("]]>").append("</rate__clg>\r\n");



			if(priceList == null || priceList.equals("NULLFOUND"))
			{
				priceList =genericUtility.getColumnValue("price_list", dom1);
			}


			//if isnull(ls_pricelist) or ls_pricelist = 'NULLFOUND' then
			//ls_pricelist = dw_header.getitemString(1,&quot;price_list&quot;)
			//unitStd = dw_detedit[ii_currformno].getitemString(1,&quot;unit__std&quot;)


			unitStd =genericUtility.getColumnValue("unit__std", dom1);
			sql = " select rate from pricelist where price_list = ? and item_code = ? and unit= ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,priceList);
			pstmt.setString(2,ditemCode);
			pstmt.setString(3,unitStd);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				stdrate = rs.getDouble(1);
			}
			else
			{

				stdrate = disCommon.pickRate(priceList,ordDate,ditemCode,"","L", conn);

			}

			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			XmlString.append("<stdrate>").append("<![CDATA[").append(stdrate).append("]]>").append("</stdrate>\r\n");
			 */



			ordDate =genericUtility.getColumnValue("ord_date", dom1);
			priceListClg = genericUtility.getColumnValue("price_list__clg", dom1);

			/*rate = Double.parseDouble(genericUtility.getColumnValue("rate", dom1));
			if(rate == -1)
			{
				rateClg = disCommon.pickRate(priceListClg , ordDate, ditemCode, "", "L", conn);
				XmlString.append("<rate__clg>").append("<![CDATA[").append(rateClg).append("]]>").append("</rate__clg>\r\n");
			}*/


			// get the existing cr a/c  
			//indentNo =genericUtility.getColumnValue("ind_no", dom);
			//cctrCr = genericUtility.getColumnValue("cctr_code__cr", dom);
			//acctCr = genericUtility.getColumnValue("acct_code__cr", dom);
			projectCode = genericUtility.getColumnValue("proj_code", dom1);
			suppCode = genericUtility.getColumnValue("supp_code", dom1);

			/*if(indentNo != null && indentNo.trim().length() > 0) //indentNo IS BLANK
			{
				sql = "Select acct_code, cctr_code from indent where ind_no = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,indentNo);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					acctDr = checkNull(rs.getString(1));
					cctrDr = checkNull(rs.getString(2));

				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}*/

			if(acctDr == null || acctDr.trim().length() == 0)
			//if(acctDr == null || acctDr.length() > 10)
			{
				if(contractNo != null && contractNo.trim().length() > 0)
				{
					sql = "select acct_code__dr,cctr_code__dr,acct_code__cr,cctr_code__cr" +
							"from pcontract_det where contract_no = ? and item_code = ?" ;

					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,contractNo);
					pstmt.setString(2,ditemCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						acctDr = rs.getString(1) == null ? "            ":rs.getString(1);
						cctrDr = rs.getString(2) == null ? "       ":rs.getString(2);
						acctCr = rs.getString(3) == null ? "            ":rs.getString(3);
						cctrCr = rs.getString(4) == null ? "       ":rs.getString(4);

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
				if(acctDr == null || acctDr.trim().length() == 0)
				{
					if(projectCode != null && projectCode.trim().length() > 0)
					{
						sql = "select acct_code,cctr_code from project where proj_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,projectCode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							acctDr = rs.getString(1) == null ?"            ":rs.getString(1);
							cctrDr = rs.getString(2) == null ?"      ":rs.getString(2);

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}

				}
				if(acctDr == null || acctDr.trim().length() == 0)
				{

					acctDet = finCommon.getAcctDetrTtype(ditemCode, itemSer, "IN", pordType, conn);
					System.out.println("acctDet=="+acctDet);
					Dr = acctDet.split(",");
					System.out.println(Dr.toString()+"   "+Dr.length);
					if(Dr.length > 0)
					{
						acctDr = Dr[0];
					}
					if(Dr.length > 1)
					{
						cctrDr = Dr[1];
					}
					else
					{
						cctrDr = "      ";
					}
				}

			}

			//invacct =  disCommon.getDisparams("999999", "INV_ACCT_PORCP", conn);
			invacct = finCommon.getFinparams("999999", "INV_ACCT_PORCP",conn);
			if(invacct == null || invacct.equalsIgnoreCase("NULLFOUND") || invacct.trim().length() == 0)
			{
				invacct= "N";
			}

			//invacctQc =  disCommon.getDisparams("999999", "INV_ACCT_QCORDER", conn);
			invacctQc = finCommon.getFinparams("999999", "INV_ACCT_QCORDER",conn);
			if(invacctQc == null || invacctQc.equals("NULLFOUND") || invacctQc.trim().length() == 0)
			{
				invacctQc= "N";
			}

			System.out.println("invacct="+invacct+"   invacctQc="+invacctQc+"   "+acctCr);

			if(acctCr == null || acctCr.trim().length() == 0)
			{
				if(invacct.equalsIgnoreCase("Y") && ! invacctQc.equalsIgnoreCase("Y") )
				{
					acctDet = finCommon.getAcctDetrTtype(ditemCode, itemSer, "PORCP", pordType, conn);
					System.out.println("cctrCr for PORCP="+acctDet);
					Cr = acctDet.split(",");
					if(Cr.length > 0)
					{
						acctCr = Cr[0];
					}
					else
					{
						acctCr = "            ";
					}
					if(Cr.length > 1)
					{
						cctrCr = Cr[1];
					}
					else
					{
						cctrCr = "      ";
					}

				}
				else
				{
					acctDet = finCommon.getAcctDetrTtype(ditemCode, itemSer, "PO", pordType, conn);
					System.out.println("cctrCr FOR PO ="+acctDet);
					Cr = acctDet.split(",");
					if(Cr.length > 0)
					{
						acctCr =  Cr[0];
					}
					else
					{
						acctCr = "            ";
					}
					if(Cr.length > 1)
					{
						cctrCr =  Cr[1];
					}
					else
					{
						cctrCr = "      ";
					}
					if(acctCr == null || acctCr.trim().length() == 0)
					{
						sql= " select acct_code__ap , cctr_code__ap from supplier where supp_code = ? " ;
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,suppCode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							acctCr = rs.getString(1) == null ?"            ":rs.getString(1);
							cctrCr = rs.getString(2) == null ?"      ":rs.getString(2);
						}
					}
				}
			}

			/*if (acctDr == null || acctDr.trim().length() == 0 )
			{
				acctDr = " ";
			}
			if (cctrDr == null || cctrDr.trim().length() == 0 )
			{
				cctrDr = " ";
			}
			if (acctCr == null || acctCr.trim().length() == 0 )
			{
				acctCr = " ";
			}
			if (cctrCr == null || cctrCr.trim().length() == 0 )
			{
				cctrCr = " ";
			}*/

			XmlString.append("<acct_code__dr>").append("<![CDATA[").append(acctDr).append("]]>").append("</acct_code__dr>\r\n");
			XmlString.append("<cctr_code__dr>").append("<![CDATA[").append(cctrDr).append("]]>").append("</cctr_code__dr>\r\n");
			XmlString.append("<acct_code__cr>").append("<![CDATA[").append(acctCr).append("]]>").append("</acct_code__cr>\r\n");
			XmlString.append("<cctr_code__cr>").append("<![CDATA[").append(cctrCr).append("]]>").append("</cctr_code__cr>\r\n");


			//cctr_code__dr and cctr_code__cr is allways Blank 
			/*costctr =genericUtility.getColumnValue("cctr_code__dr", dom1);
			if(costctr == null || costctr.trim().length() == 0)
			{
				costctr = checkNull(genericUtility.getColumnValue("cctr_code__cr", dom1));
			}  		
			sql = "select (case when qc_reqd is null then 'N' else qc_reqd end) " +
					"from item  where item_code = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,ditemCode);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				qcReqd = checkNull(rs.getString(1));

			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			if(qcReqd.equals("Y"))
			{
				CctrLoccode = costctr.trim()+"Q";
				XmlString.append("<loc_code>").append("<![CDATA[").append(CctrLoccode).append("]]>").append("</loc_code>\r\n");
			}
			else
			{
				CctrLoccode = costctr;
				XmlString.append("<loc_code>").append("<![CDATA[").append(CctrLoccode).append("]]>").append("</loc_code>\r\n");
			}*/


			//priceListItemCheck(ditemCode, dom,conn);
			//itemCodeRef =genericUtility.getColumnValue("item_code", dom);
			//mdescr = gf_get_desc_specs(mcode)
			// dw_detedit[ii_currformno].setitem(1,&quot;spl_instr&quot;,mdescr)
			//PB 526
			priceListClg =genericUtility.getColumnValue("price_list__clg", dom1);
			System.out.println("priceListClg="+priceListClg);

			rateClg = disCommon.pickRate(priceListClg , ordDate, ditemCode, "", "L", conn);
			System.out.println("rateClg="+rateClg);
			if(rateClg == -1 )
			{
				int cnt = 0;
				String priceListParent = "";
				priceList =genericUtility.getColumnValue("price_list", dom1);
				sql = "select count(*)  from pricelist where price_list = ? and item_code = ? and (list_type = 'F' or list_type = 'B') " ;
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,priceList);
				pstmt.setString(2,ditemCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					cnt = rs.getInt(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				if(cnt > 0)
				{
					//XmlString.append("<rate__clg>").append("<![CDATA[").append(0).append("]]>").append("</rate__clg>\r\n");
					rateClg = 1;
				}
				else
				{
					sql= "select (case when price_list__parent is null then '' else price_list__parent end )" +
							" from pricelist where price_list = ? and list_type = 'B'";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,priceList);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						priceListParent = rs.getString(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if(priceListParent != null && priceListParent.trim().length() > 0)
					{
						sql = "select count(*)  from pricelist where price_list = ? and item_code = ? and (list_type = 'F' or list_type = 'B'); " ;
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,priceListParent);
						pstmt.setString(2,ditemCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						//XmlString.append("<rate__clg>").append("<![CDATA[").append(0).append("]]>").append("</rate__clg>\r\n");
						if(cnt > 0)
						{
							rateClg = 1;
						}
					}
				}
			}
			else
			{
				System.out.println("rateClg="+rateClg);
				if(rateClg == 0 || rateClg == -1)
				{
					rateClg = rate;
				}
			}
			XmlString.append("<rate__clg>").append("<![CDATA[").append(rateClg).append("]]>").append("</rate__clg>\r\n");

			//not req. bez ind_no blank
			/*sql = " select supp_code__mnfr  from indent where ind_no = ? and item_code= ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,indentNo);
			pstmt.setString(2,ditemCode);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				suppCodeMnfr = rs.getString(1) == null?" ":rs.getString(1);
			}
			XmlString.append("<supp_code__mnfr>").append("<![CDATA[").append(suppCodeMnfr).append("]]>").append("</supp_code__mnfr>\r\n");*/

			sql = "select item_code__ref from supplieritem  where supp_code = ? and item_code = ?" ;
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,suppCode);
			pstmt.setString(2,ditemCode);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				itemCodeRef = rs.getString(1) == null ?" ":rs.getString(1) ;
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			XmlString.append("<supp_item__ref>").append("<![CDATA[").append(itemCodeRef).append("]]>").append("</supp_item__ref>\r\n");

			//quantity =  Double.parseDouble(genericUtility.getColumnValue("quantity", dom1));

			System.out.println("quantity="+quantity+"   stdrate="+stdrate);
			stdCode = quantity * stdrate;
			actualCost = Double.parseDouble( genericUtility.getColumnValue("actual_cost", dom)== null ?"0":genericUtility.getColumnValue("actual_cost", dom));		

			XmlString.append("<std_cost>").append("<![CDATA[").append(stdCode).append("]]>").append("</std_cost>\r\n");
			XmlString.append("<varience>").append("<![CDATA[").append(stdCode - actualCost).append("]]>").append("</varience>\r\n");


		}
		catch (Exception e)
		{
			System.out.println("Exception : BillofQuantityAct : actionHandler() " + e.getMessage());
			throw new ITMException(e);
		}


		System.out.println("itmCodeItmChanges xmlString="+XmlString);
		return XmlString;
	}



	private String qcReqd(String siteCode, String ditemCode,Connection conn) throws ITMException {
		String qcReqd = "";
		String sql = "";
		//ConnDriver connDriver = new ConnDriver();
		PreparedStatement pstmt = null; 
		ResultSet rs = null;
		//Connection conn = null;
		try 
		{


			//conn = connDriver.getConnectDB("DriverITM");

			sql = "select case when qc_reqd is null then 'N' else qc_reqd end " +
					"from siteitem where item_code = ? and site_code = ?" ;
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,ditemCode);
			pstmt.setString(2,siteCode);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				qcReqd = rs.getString(1);
			}
			rs.close();
			rs = null ;
			pstmt.close();
			pstmt = null ;

			if(qcReqd == null || qcReqd.trim().length() == 0)
			{
				sql = "select case when qc_reqd is null then 'N' else qc_reqd end " +
						"from item where item_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,ditemCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					qcReqd = rs.getString(1);
				}
				rs.close();
				rs = null ;
				pstmt.close();
				pstmt = null ;


			}
		}catch (Exception e)
		{
			System.out.println("Exception : PorderActEJB : actionHandler :(Document dom)" + e.getMessage());
			//throw new ITMException(e);
			throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
		}

		return qcReqd;
	}

	private String itemListTransform(Document dom, Document dom1, String objContext, String xtraParams, Document selDataDom) throws ITMException
	{
		System.out.println("[PorderActEJB] gpListTransform is calling.............");
		Connection conn = null;
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		Node currDetail = null; Node currDetail1 = null;
		int count = 0;
		double stkQty = 0.0D;
		ConnDriver connDriver = new ConnDriver();
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility= new  E12GenericUtility();
		String objName = "";
		String discountType = "",bomCode = "";
		String acctCodeCr = null,acctCodeDr = null,cctrCodeCr = null,cctrCodeDr = null;
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 

			NodeList detailList = selDataDom.getElementsByTagName("Detail");
			//Node parentNode = parentNodeList.item(0);
			int noOfDetails = detailList.getLength();
			for (int ctr = 0; ctr < noOfDetails; ctr++)
			{
				currDetail = detailList.item(ctr);
			}
			if (noOfDetails > 0)
			{
				for (int ctr = 0; ctr < noOfDetails; ctr++)
				{
					//isSrvCallOnChg='0'
					valueXmlString.append("<Detail>");
					currDetail1 = detailList.item(ctr);
					valueXmlString.append("<item_ser>").append("<![CDATA[").append(genericUtility.getColumnValueFromNode("item_ser", currDetail1)).append("]]>").append("</item_ser>\r\n");
					//set item desc. on 24/04/13
					valueXmlString.append("<item_descr>").append("<![CDATA[").append(genericUtility.getColumnValueFromNode("item_descr", currDetail1)).append("]]>").append("</item_descr>\r\n");
					
					valueXmlString.append("<item_code isSrvCallOnChg='0'>").append("<![CDATA[").append(genericUtility.getColumnValueFromNode("item_code", currDetail1)).append("]]>").append("</item_code>\r\n");
					valueXmlString.append("<quantity isSrvCallOnChg='0'>").append("<![CDATA[").append(genericUtility.getColumnValueFromNode("quantity", currDetail1)).append("]]>").append("</quantity>\r\n");
					valueXmlString.append("<unit isSrvCallOnChg='0'>").append("<![CDATA[").append(genericUtility.getColumnValueFromNode("unit", currDetail1)).append("]]>").append("</unit>\r\n");
					valueXmlString.append("<rate isSrvCallOnChg='0'>").append("<![CDATA[").append(genericUtility.getColumnValueFromNode("rate", currDetail1)).append("]]>").append("</rate>\r\n");
					valueXmlString.append("<amount isSrvCallOnChg='0'>").append("<![CDATA[").append(genericUtility.getColumnValueFromNode("amount", currDetail1)).append("]]>").append("</amount>\r\n");
					valueXmlString.append("<no_art isSrvCallOnChg='0'>").append("<![CDATA[").append(genericUtility.getColumnValueFromNode("no_art", currDetail1)).append("]]>").append("</no_art>\r\n");
					valueXmlString.append("<pack_code isSrvCallOnChg='0'>").append("<![CDATA[").append(genericUtility.getColumnValueFromNode("pack_code", currDetail1)).append("]]>").append("</pack_code>\r\n");
					valueXmlString.append("<pack_instr isSrvCallOnChg='0'>").append("<![CDATA[").append(checkNull(genericUtility.getColumnValueFromNode("pack_instr", currDetail1))).append("]]>").append("</pack_instr>\r\n");
					valueXmlString.append("<specific_instr isSrvCallOnChg='0'>").append("<![CDATA[").append(checkNull(genericUtility.getColumnValueFromNode("specific_instr", currDetail1))).append("]]>").append("</specific_instr>\r\n");
					valueXmlString.append("<special_instr isSrvCallOnChg='0'>").append("<![CDATA[").append(checkNull(genericUtility.getColumnValueFromNode("special_instr", currDetail1))).append("]]>").append("</special_instr>\r\n");

					valueXmlString.append("<site_code isSrvCallOnChg='0'>").append("<![CDATA[").append(genericUtility.getColumnValueFromNode("site_code", currDetail1)).append("]]>").append("</site_code>\r\n");
					valueXmlString.append("<unit__rate isSrvCallOnChg='0'>").append("<![CDATA[").append(genericUtility.getColumnValueFromNode("unit__rate", currDetail1)).append("]]>").append("</unit__rate>\r\n");
					//commented according to manoj sir on 27/02/13

					//valueXmlString.append("<ind_no>").append("<![CDATA[").append(genericUtility.getColumnValueFromNode("ind_no", currDetail1).trim()).append("]]>").append("</ind_no>\r\n");
					valueXmlString.append("<remarks protect=\"0\" isSrvCallOnChg='0'>").append("<![CDATA[").append(genericUtility.getColumnValueFromNode("remarks", currDetail1)).append("]]>").append("</remarks>\r\n");
					valueXmlString.append("<unit__std isSrvCallOnChg='0'>").append("<![CDATA[").append(genericUtility.getColumnValueFromNode("unit__std", currDetail1)).append("]]>").append("</unit__std>\r\n");

					valueXmlString.append("<unit__std isSrvCallOnChg='0'>").append("<![CDATA[").append(genericUtility.getColumnValueFromNode("unit__std", currDetail1)).append("]]>").append("</unit__std>\r\n");
					valueXmlString.append("<conv__qty_stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(genericUtility.getColumnValueFromNode("conv__qty_stduom", currDetail1)).append("]]>").append("</conv__qty_stduom>\r\n");
					valueXmlString.append("<conv__rtuom_stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(genericUtility.getColumnValueFromNode("conv__rate_stduom", currDetail1)).append("]]>").append("</conv__rtuom_stduom>\r\n");
					valueXmlString.append("<quantity__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(genericUtility.getColumnValueFromNode("quantity__stduom", currDetail1)).append("]]>").append("</quantity__stduom>\r\n");
					//added project code for item change on purchase order  
					valueXmlString.append("<proj_code isSrvCallOnChg='0'>").append("<![CDATA[").append(genericUtility.getColumnValueFromNode("proj_code", currDetail1)).append("]]>").append("</proj_code>\r\n");
					valueXmlString.append("<rate__stduom isSrvCallOnChg='0'>").append("<![CDATA[").append(genericUtility.getColumnValueFromNode("rate__stduom", currDetail1)).append("]]>").append("</rate__stduom>\r\n");
					
					//add by akhilesh on 01/apr/2013 to set the field on item code item changes disable 	
					valueXmlString.append("<tax_class  protect=\"0\" isSrvCallOnChg='0'>").append("<![CDATA[").append(checkNull(genericUtility.getColumnValueFromNode("tax_class", currDetail1)).trim()).append("]]>").append("</tax_class>\r\n");	
					valueXmlString.append("<tax_chap  protect=\"0\" isSrvCallOnChg='0'>").append("<![CDATA[").append(checkNull(genericUtility.getColumnValueFromNode("tax_chap", currDetail1)).trim()).append("]]>").append("</tax_chap>\r\n");
					valueXmlString.append("<tax_env  protect=\"0\" isSrvCallOnChg='0'>").append("<![CDATA[").append(checkNull(genericUtility.getColumnValueFromNode("tax_env", currDetail1)).trim()).append("]]>").append("</tax_env>\r\n");

					System.out.println("@@@@@ 3 taxClass["+ checkNull(genericUtility.getColumnValueFromNode("tax_class", currDetail1)).trim()+"]::taxChap["+checkNull(genericUtility.getColumnValueFromNode("tax_chap", currDetail1)).trim()+"]:::taxEnv["+checkNull(genericUtility.getColumnValueFromNode("tax_env", currDetail1)).trim()+"]");
					
					discountType = genericUtility.getColumnValueFromNode("discount_type", currDetail1);
					if(discountType == null || discountType.trim().equalsIgnoreCase("null") )
					{
						discountType = "";
					}
					bomCode = genericUtility.getColumnValueFromNode("bom_code", currDetail1) ;
					if(bomCode == null || bomCode.trim().equalsIgnoreCase("null") )
					{
						bomCode = "";
					}

					valueXmlString.append("<discount_type isSrvCallOnChg='0'>").append("<![CDATA[").append(discountType).append("]]>").append("</discount_type>\r\n");	
					valueXmlString.append("<discount isSrvCallOnChg='0'>").append("<![CDATA[").append(genericUtility.getColumnValueFromNode("discount", currDetail1)).append("]]>").append("</discount>\r\n");
					valueXmlString.append("<bom_code isSrvCallOnChg='0'>").append("<![CDATA[").append(bomCode).append("]]>").append("</bom_code>\r\n");			
					valueXmlString.append("<contract_detail isSrvCallOnChg='0'>").append("<![CDATA[").append(genericUtility.getColumnValueFromNode("contract_detail", currDetail1)).append("]]>").append("</contract_detail>\r\n");
					acctCodeDr = genericUtility.getColumnValueFromNode("acct_code__dr", currDetail1);
					System.out.println("acctCodeDr="+acctCodeDr);
					if(acctCodeDr != null && acctCodeDr.length() > 10)
					{
						valueXmlString.append("<acct_code__dr isSrvCallOnChg='0'>").append("<![CDATA[").append(checkNull(acctCodeDr).trim()).append("]]>").append("</acct_code__dr>\r\n");
					}
					else
					{
						valueXmlString.append("<acct_code__dr isSrvCallOnChg='0'>").append("<![CDATA[").append(acctCodeDr).append("]]>").append("</acct_code__dr>\r\n");
					}
					cctrCodeDr = genericUtility.getColumnValueFromNode("cctr_code__dr", currDetail1);
					System.out.println("cctrCodeDr="+cctrCodeDr);
					if (cctrCodeDr != null && cctrCodeDr.length() > 4) 
					{
						valueXmlString.append("<cctr_code__dr isSrvCallOnChg='0'>").append("<![CDATA[").append(cctrCodeDr.trim()).append("]]>").append("</cctr_code__dr>\r\n");
					}
					else
					{
						valueXmlString.append("<cctr_code__dr isSrvCallOnChg='0'>").append("<![CDATA[").append(cctrCodeDr).append("]]>").append("</cctr_code__dr>\r\n");
					}
					
					acctCodeCr = genericUtility.getColumnValueFromNode("acct_code__cr", currDetail1);
					if(acctCodeCr != null && acctCodeCr.length() > 10)
					{
						valueXmlString.append("<acct_code__cr> isSrvCallOnChg='0'").append("<![CDATA[").append(acctCodeCr.trim()).append("]]>").append("</acct_code__cr>\r\n");
					}
					else
					{
						valueXmlString.append("<acct_code__cr isSrvCallOnChg='0'>").append("<![CDATA[").append(acctCodeCr).append("]]>").append("</acct_code__cr>\r\n");
					}
					cctrCodeCr = genericUtility.getColumnValueFromNode("cctr_code__cr", currDetail1);
					if (cctrCodeCr != null && cctrCodeCr.length() > 4) 
					{
						valueXmlString.append("<cctr_code__cr isSrvCallOnChg='0'>").append("<![CDATA[").append( cctrCodeCr.trim()).append("]]>").append("</cctr_code__cr>\r\n");
					}
					else
					{
						valueXmlString.append("<cctr_code__cr isSrvCallOnChg='0'>").append("<![CDATA[").append( cctrCodeCr).append("]]>").append("</cctr_code__cr>\r\n");
					}
					
					//valueXmlString.append("<cctr_code__cr>").append("<![CDATA[").append( checkNull( genericUtility.getColumnValueFromNode("cctr_code__cr", currDetail1)).trim()).append("]]>").append("</cctr_code__cr>\r\n");
					//valueXmlString.append("<acct_code__cr>").append("<![CDATA[").append(checkNull( genericUtility.getColumnValueFromNode("acct_code__cr", currDetail1)).trim()).append("]]>").append("</acct_code__cr>\r\n");
					//valueXmlString.append("<cctr_code__dr>").append("<![CDATA[").append(checkNull( genericUtility.getColumnValueFromNode("cctr_code__dr", currDetail1)).trim()).append("]]>").append("</cctr_code__dr>\r\n");
					//valueXmlString.append("<acct_code__dr>").append("<![CDATA[").append(checkNull( genericUtility.getColumnValueFromNode("acct_code__dr", currDetail1)).trim()).append("]]>").append("</acct_code__dr>\r\n");
					valueXmlString.append("<loc_code isSrvCallOnChg='0'>").append("<![CDATA[").append(genericUtility.getColumnValueFromNode("loc_code", currDetail1)).append("]]>").append("</loc_code>\r\n");
					valueXmlString.append("<rate__clg isSrvCallOnChg='0'>").append("<![CDATA[").append(genericUtility.getColumnValueFromNode("rate__clg", currDetail1)).append("]]>").append("</rate__clg>\r\n");
					valueXmlString.append("<supp_item__ref isSrvCallOnChg='0'>").append("<![CDATA[").append( checkNull(genericUtility.getColumnValueFromNode("supp_item__ref", currDetail1)).trim() ).append("]]>").append("</supp_item__ref>\r\n");
					valueXmlString.append("<supp_code__mnfr isSrvCallOnChg='0'>").append("<![CDATA[").append( checkNull(genericUtility.getColumnValueFromNode("supp_code__mnfr", currDetail1)).trim()).append("]]>").append("</supp_code__mnfr>\r\n");
					valueXmlString.append("<std_cost isSrvCallOnChg='0'>").append("<![CDATA[").append(genericUtility.getColumnValueFromNode("std_cost", currDetail1)).append("]]>").append("</std_cost>\r\n");
					valueXmlString.append("<varience>").append("<![CDATA[").append(genericUtility.getColumnValueFromNode("varience", currDetail1)).append("]]>").append("</varience>\r\n");


					valueXmlString.append("<emp_code__qcaprv isSrvCallOnChg='0'>").append("<![CDATA[").append( checkNull(genericUtility.getColumnValueFromNode("emp_code__qcaprv", currDetail1)).trim()).append("]]>").append("</emp_code__qcaprv>\r\n");
					valueXmlString.append("<emp_fname isSrvCallOnChg='0'>").append("<![CDATA[").append( checkNull(genericUtility.getColumnValueFromNode("emp_fname", currDetail1)).trim()).append("]]>").append("</emp_fname>\r\n");
					valueXmlString.append("<emp_mname isSrvCallOnChg='0'>").append("<![CDATA[").append( checkNull(genericUtility.getColumnValueFromNode("emp_mname", currDetail1)).trim()).append("]]>").append("</emp_mname>\r\n");
					valueXmlString.append("<emp_lname isSrvCallOnChg='0'>").append("<![CDATA[").append( checkNull(genericUtility.getColumnValueFromNode("emp_lname", currDetail1)).trim()).append("]]>").append("</emp_lname>\r\n");

					//added by kunal on 05/04/13 set request date and dlv date
					valueXmlString.append("<req_date isSrvCallOnChg='0'>").append("<![CDATA[").append(genericUtility.getColumnValueFromNode("req_date", currDetail1)).append("]]>").append("</req_date>\r\n");
					valueXmlString.append("<dlv_date isSrvCallOnChg='0'>").append("<![CDATA[").append(genericUtility.getColumnValueFromNode("dlv_date", currDetail1)).append("]]>").append("</dlv_date>\r\n");

					valueXmlString.append("</Detail>");
				}
			}//rate__stduom
			valueXmlString.append("</Root>");
		}
		catch (ITMException itme)
		{
			throw itme;
		}
		catch (Exception e)
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
			} catch (Exception localException1) {
			}
		}
		System.out.println("[PorderActEJB] gpListTransform() : valueXmlString from :" + valueXmlString.toString());
		return valueXmlString.toString();
	}

	

	private String getObjName(Node node) throws Exception {
		String objName = null;
		NodeList nodeList = null;
		Node detaulNode = null;
		Node detailNode = null;
		nodeList = node.getChildNodes();
		NamedNodeMap attrMap = node.getAttributes();
		objName = attrMap.getNamedItem("objName").getNodeValue();

		return "w_" + objName;
	}

	private String checkNull(String input)
	{
		if (input == null)
		{
			input = "";
		}
		return input;
	}

	private void priceListItemCheck(String itemCode, Document dom ,Connection conn) throws ITMException
	{
		String PriceListParent = "";
		String priceList = "";
		String sql = "";
		int cnt= 0;
		StringBuffer XmlString = new StringBuffer();
		//ConnDriver connDriver = new ConnDriver();
		PreparedStatement pstmt = null;
		//PreparedStatement pstmt1 = null;
		ResultSet rs = null;
		//Connection conn = null;

		try
		{
			priceList =genericUtility.getColumnValue("price_list", dom);

			//conn = connDriver.getConnectDB("DriverITM");

			sql = "select count(*)  from pricelist where price_list = ? and item_code = ? and (list_type = 'F' or list_type = 'B'); " ;
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,priceList);
			pstmt.setString(2,itemCode);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				cnt = rs.getInt(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			if(cnt > 0)
			{
				XmlString.append("<rate__clg>").append("<![CDATA[").append(0).append("]]>").append("</rate__clg>\r\n");
			}
			else
			{
				sql= "select (case when price_list__parent is null then '' else price_list__parent end )" +
						" from pricelist where price_list = ? and list_type = 'B'";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,priceList);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					PriceListParent = rs.getString(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				if(PriceListParent != null && PriceListParent.trim().length() > 0)
				{
					sql = "select count(*)  from pricelist where price_list = ? and item_code = ? and (list_type = 'F' or list_type = 'B'); " ;
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,PriceListParent);
					pstmt.setString(2,itemCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						cnt = rs.getInt(1);
					}
					XmlString.append("<rate__clg>").append("<![CDATA[").append(0).append("]]>").append("</rate__clg>\r\n");
				}
			}
		}catch (Exception e) {
			// TODO: handle exception
			throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
		}

	}

}