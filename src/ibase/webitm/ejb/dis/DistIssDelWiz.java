
package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.StringTokenizer;

import javax.ejb.Stateless;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Stateless
public class DistIssDelWiz extends ValidatorEJB implements DistIssDelWizLocal, DistIssDelWizRemote {

	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	String userId = null;
	String chgUser = null;
	String chgTerm = null;
	NumberFormat nf = null;
	boolean isError=false;



	public DistIssDelWiz() 
	{
		System.out.println("^^^^^^^ inside Distribution Issue Wizard ^^^^^^^");
	}

	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException {
		System.out.println("^^^^^^^ inside Distribution Issue Wizard >^^^^^^^");
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String errString = "";

		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = "";

		try {
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0) 
			{
				dom2 = parseString("<Root>" + xmlString2 + "</Root>");
			}
			if (objContext != null && Integer.parseInt(objContext) == 1) 
			{
				parentNodeList = dom2.getElementsByTagName("Header0");
				parentNode = parentNodeList.item(1);
				childNodeList = parentNode.getChildNodes();
				for (int x = 0; x < childNodeList.getLength(); x++) 
				{
					childNode = childNodeList.item(x);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equalsIgnoreCase("Detail1")) 
					{
						errString = wfValData(dom, dom1, dom2, "1", editFlag, xtraParams);
						if (errString != null && errString.trim().length() > 0)
							break;
					} else if (childNodeName.equalsIgnoreCase("Detail2")) 
					{
						errString = wfValData(dom, dom1, dom2, "2", editFlag, xtraParams);
						break;
					}
				}
			} else 
			{
				errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
			}
		} catch (Exception e) {
			System.out.println("Exception : Inside DocumentMaster wfValData Method ..> " + e.getMessage());
			throw new ITMException(e);
		}
		return (errString);
	}

	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException {
		System.out.println("^^^^^^^ inside Distribution Issue wfValData >^^^^^^^");
		//GenericUtility genericUtility;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		int ctr = 0, currentFormNo = 0, childNodeListLength = 0, cnt = 0;
		String childNodeName = null;
		String errString = "";
		String errCode = "";
		Connection conn = null;
		String userId = "";
		PreparedStatement pstmt = null ;
		ResultSet rs = null;
		String sql = "";

		String distOrderNo = "",confirmed="",tranId="",siteCode="",lotSl="";
		try {

			System.out.println("editFlag>>>>wf"+editFlag);
			System.out.println("xtraParams>>>wf"+xtraParams);


			ConnDriver connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			//genericUtility = GenericUtility.getInstance();
			if (objContext != null && objContext.trim().length() > 0) {
				currentFormNo = Integer.parseInt(objContext);
			}
			switch (currentFormNo) {
			case 1:
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();

				for (ctr = 0; ctr < childNodeListLength; ctr++) {


					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();

					if(childNodeName.equalsIgnoreCase("dist_order1"))
					{

						distOrderNo = genericUtility.getColumnValue("dist_order1",dom);

						if (distOrderNo == null || distOrderNo.trim().length() == 0)
						{
							errCode = "DISISSNULL";
							errString = getErrorString("dist_order1",errCode,userId);
							break;
						}
						else
						{

							sql = "select count(1) from distorder where dist_order = ?";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1,distOrderNo);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;
							if(cnt == 0)
							{
								errCode = "DISNOTEX";
								errString = getErrorString("dist_order1",errCode,userId);
								break;
							}/*else
							{
								sql = "select confirmed from distord_iss where tran_id = ?";
								pstmt=conn.prepareStatement(sql);
								pstmt.setString(1,distOrderNo);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									confirmed = rs.getString(1) == null ? "":rs.getString(1);
								}
								pstmt.close();
								rs.close();
								pstmt = null;
								rs = null;

								if(confirmed.equals("Y"))
								{
									errCode = "DISISSCO";
									errString = getErrorString("dist_order1",errCode,userId);
									break;
								}

							}*/

						}

					}
				}
				break;
			case 2:


				System.out.println("DOM>>>> Elements>>["+genericUtility.serializeDom(dom).toString()+"]");
				System.out.println("DOM1>> Elements>>["+genericUtility.serializeDom(dom1).toString()+"]");
				System.out.println("DOM2>> Elements>>["+genericUtility.serializeDom(dom2).toString()+"]");	

				parentNodeList = dom2.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				System.out.println("parentNode >>>{"+parentNode+"}");
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();


				for(ctr = 0; ctr < childNodeListLength; ctr++)
				{
					
				}
				break;
			case 3:

				parentNodeList = dom2.getElementsByTagName("Detail3");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				System.out.println("childNodeListLength+ ====>>>>>>> "+childNodeListLength);
				System.out.println("childNodeName+ ==== "+childNodeName);
				for(ctr = 0; ctr < childNodeListLength; ctr++)
				{


					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					System.out.println("value of child node : "+childNode);

					if(childNodeName.equalsIgnoreCase("lot_sl"))
					{
			/*			siteCode = genericUtility.getColumnValue("site_code",dom2);
						lotSl = genericUtility.getColumnValue("lot_sl",dom2);
						distOrderNo = genericUtility.getColumnValue("dist_order1",dom1);

						if (lotSl == null || lotSl.trim().length() == 0)
						{
							errCode = "DIDOLSNULL";
							errString = getErrorString("lot_sl",errCode,userId);
							break;
						}
						else
						{
							
							//add validation here for check lot sl present in detail table
							sql = "select count(1) from distord_issdet where lot_sl = ? and dist_order = ?";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1,lotSl);
							pstmt.setString(2,distOrderNo);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;
							if(cnt == 0)
							{
								errCode = "VTINVLOTSL";
								errString = getErrorString("lot_sl",errCode,userId);
								break;
							}
						}
*/
	
					}
					

					
				}
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();			
			errString = e.getMessage();
			try {
				conn.rollback();				
			} catch (Exception d) {
				d.printStackTrace();
			}
			throw new ITMException(e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
				conn = null;
			} catch (Exception d) {
				d.printStackTrace();
			}
		}
		return errString;
	}

	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException {
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try {
			System.out.println("currentColumn"+currentColumn);
			System.out.println("editFlag"+editFlag);
			System.out.println("xtraParams"+xtraParams);


			System.out.println("xmlString111>>"+xmlString);
			System.out.println("xmlString222>>"+xmlString1);
			System.out.println("xmlString333>>"+xmlString2);
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0) {
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
		} catch (Exception e) {
			System.out.println("Exception : [itemChanged(String,String)] :==>\n" + e.getMessage());
			throw new ITMException(e);
		}
		return valueXmlString;
	}


	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException {

		StringBuffer valueXmlString = null;
		int currentFormNo = 0;
		Connection conn = null;
		double squantity = 0.0;
		PreparedStatement pstmt = null,pstmt1=null;
		ResultSet rs = null ,rs1 = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		ConnDriver connDriver = new ConnDriver();
		SimpleDateFormat simpleDateFormat = null;
		//GenericUtility genutility = new GenericUtility();
		E12GenericUtility genutility= new  E12GenericUtility();
		String locCode="",lotNo="",itmdesc="",siteCode="",locdesc="";
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		DistCommon disCommon = new DistCommon();
		System.out.println("DOM111 Elements>>>********************************["+genericUtility.serializeDom(dom).toString()+"]");
		System.out.println("DOM222 Elements>>>********************************["+genericUtility.serializeDom(dom1).toString()+"]");
		System.out.println("DOM322 Elements>>>********************************["+genericUtility.serializeDom(dom2).toString()+"]");
		String distOrder = "",itemCode = "",sql="",unit="",packCode="",tranType ="";
		java.sql.Timestamp currDate = null;
		double grossWeight = 0,netWeight=0,tareWeight=0,noOfArt=0;
		SimpleDateFormat sdf = null;
		String currAppdate = "";
		String tranid ="";
		String rate = "";
		double amount = 0;
		StringBuffer detail2xml = new StringBuffer();
		String tranDate = null;
		
		PreparedStatement pstmt2 =null;
		ResultSet rs2 = null,rs3 = null;
		
		int count = 0;
		double minputQty = 0d, remQuantity = 0d, stockQty = 0d, integralQty = 0d;
		double grossPer = 0d,netPer = 0d,grossWt = 0d,tarePer = 0d,netWt = 0d,tareWt =0d, rateClgVal = 0d, rate2 = 0d;
		double disAmount = 0d, shipperQty = 0d,discount =0;
		int  minShelfLife = 0, noArt1 = 0;
		int mLineNoDist =0, lineNo = 0;
		double qtyConfirm =0,qtyShipped =0,lcQtyOrderAlt =0,lcFact =0;
		String siteCodeMfg = "", sundryCode = "";
		String priceList = "", tabValue = "", priceListClg = "", chkDate = "";
		String res = "", locCodeDamaged = "",availableYn ="";
		String checkIntegralQty = "", tranTypeParent ="";
		String rate1 = "";
		String active = "",errCode ="",sql2 ="",noArt ="",itemDescr="";
		String errString ="",siteCodeShip ="";
		String  lotSl ="",rateClg ="";
		java.util.Date chkDate1 = null;
		String prvDeptCode = null,deptCode ="";
		String locGroupJwiss="";
		String subSQL="", distOrderNo = "",unitAlt = "";

		try
		{   
			sdf=new SimpleDateFormat(genutility.getApplDateFormat());
			currDate = new java.sql.Timestamp(System.currentTimeMillis()) ;
			currAppdate = sdf.format(currDate);
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
			simpleDateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "userId");

			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");

			if (objContext != null && objContext.trim().length() > 0) {
				currentFormNo = Integer.parseInt(objContext);
			}
			System.out.println("FORM NO IS"+currentFormNo);
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			System.out.println("lot sl in begin from dom ...>>>>>>.."+genericUtility.getColumnValue("lot_sl",dom)+"lot sl from dom1 ...>>>>>>.."+genericUtility.getColumnValue("lot_sl",dom1)+"lot sl from dom2 ...>>>>>>.."+genericUtility.getColumnValue("lot_sl",dom2));
			switch (currentFormNo) {

			case 1 :
				break;

			case 2 : 
				System.out.println("DOM2 Elements["+genericUtility.serializeDom(dom2).toString()+"]");

			

				distOrderNo = genericUtility.getColumnValue("dist_order1", dom1);
				
					if(currentColumn.trim().equalsIgnoreCase("itm_default"))
					{
						
						
						sql =   "SELECT SITE_A.DESCR,SITE_B.DESCR,LOCATION.DESCR,DISTORD_ISS.TRAN_ID,DISTORD_ISS.TRAN_DATE,DISTORD_ISS.EFF_DATE,DISTORD_ISS.DIST_ORDER,DISTORD_ISS.SITE_CODE,DISTORD_ISS.SITE_CODE__DLV,"
								+"DISTORD_ISS.DIST_ROUTE,DISTORD_ISS.TRAN_CODE,DISTORD_ISS.LR_NO,DISTORD_ISS.LR_DATE,DISTORD_ISS.LORRY_NO,DISTORD_ISS.GROSS_WEIGHT,"
								+"DISTORD_ISS.TARE_WEIGHT,DISTORD_ISS.NET_WEIGHT,DISTORD_ISS.FRT_AMT,DISTORD_ISS.AMOUNT,DISTORD_ISS.TAX_AMT,DISTORD_ISS.NET_AMT,DISTORD_ISS.REMARKS,"
								+"DISTORD_ISS.FRT_TYPE,DISTORD_ISS.CHG_USER,DISTORD_ISS.CHG_TERM,DISTORD_ISS.CURR_CODE,DISTORD_ISS.CHG_DATE,"
								+"TRANSPORTER.TRAN_NAME,CURRENCY_A.DESCR,DISTORD_ISS.CONFIRMED,DISTORD_ISS.LOC_CODE__GIT,DISTORD_ISS.CONF_DATE,DISTORD_ISS.NO_ART,DISTORD_ISS.TRANS_MODE,"
								+"DISTORD_ISS.GP_NO,DISTORD_ISS.GP_DATE,DISTORD_ISS.CONF_PASSWD,DISTORD_ISS.ORDER_TYPE,DISTORD_ISS.GP_SER,DISTORD_ISS.REF_NO,DISTORD_ISS.REF_DATE,DISTORD_ISS.AVAILABLE_YN,"
								+"SITE_B.ADD1,SITE_B.ADD2,SITE_B.CITY,SITE_B.PIN,SITE_B.STATE_CODE,DISTORD_ISS.EXCH_RATE,DISTORD_ISS.TRAN_TYPE,DISTORD_ISS.EMP_CODE__APRV,DISTORD_ISS.DISCOUNT,DISTORD_ISS.PERMIT_NO,"
								+"DISTORD_ISS.SHIPMENT_ID,DISTORD_ISS.CURR_CODE__FRT,DISTORD_ISS.EXCH_RATE__FRT,CURRENCY_B.DESCR,DISTORD_ISS.RD_PERMIT_NO,DISTORD_ISS.DC_NO,DISTORD_ISS.TRAN_SER,DISTORD_ISS.PART_QTY,SPACE(100) "
								+"AS SUNDRY_DETAILS,SPACE(100) AS "
								+"SUNDRY_NAME,DISTORD_ISS.PROJ_CODE,SITE_B.TELE1,SITE_B.TELE2,SITE_B.TELE3,DISTORD_ISS.SITE_CODE__BIL,SITE_C.DESCR,SITE_C.ADD1,SITE_C.ADD2,SITE_C.CITY,SITE_C.PIN,SITE_C.STATE_CODE,"
								+"DISTORD_ISS.PALLET_WT,DISTORDER.AUTO_RECEIPT,DISTORD_ISS.CR_TERM,DISTORD_ISS.DLV_TERM,DISTORD_ISS.OUTSIDE_INSPECTION,DISTORD_ISS.LABEL_TYPE,DISTORD_ISS.ADD_USER,DISTORD_ISS.ADD_TERM "
								+"FROM DISTORD_ISS  DISTORD_ISS,SITE SITE_A,SITE SITE_B,LOCATION "
								+"LOCATION,TRANSPORTER  TRANSPORTER,CURRENCY CURRENCY_A,CURRENCY" 
								+" CURRENCY_B,SITE SITE_C,DISTORDER  DISTORDER WHERE ( "
								+"DISTORD_ISS.SITE_CODE = SITE_A.SITE_CODE ) AND ( "
								+"DISTORD_ISS.SITE_CODE__DLV = SITE_B.SITE_CODE ) AND ( "
								+"DISTORD_ISS.LOC_CODE__GIT = LOCATION.LOC_CODE ) AND ( "
								+"DISTORD_ISS.CURR_CODE = CURRENCY_A.CURR_CODE  ) AND ( "
								+"DISTORD_ISS.DIST_ORDER = DISTORDER.DIST_ORDER ) AND ( "
								+"DISTORD_ISS.TRAN_CODE=TRANSPORTER.TRAN_CODE(+)) AND ( "
								+"DISTORD_ISS.CURR_CODE__FRT=CURRENCY_B.CURR_CODE(+)) AND (" 
								+"DISTORD_ISS.SITE_CODE__BIL=SITE_C.SITE_CODE(+)) AND "
								+"DISTORD_ISS.DIST_ORDER    = '"+distOrderNo+"'";



						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if(rs.next())
						{

							valueXmlString.append("<Detail2  domID='1' objContext = '"+currentFormNo+"' selected=\"Y\">\r\n");
							valueXmlString.append("<attribute selected=\"Y\" updateFlag=\"E\" status=\"O\" pkNames=\"\"/>\r\n");

							valueXmlString.append("<tran_id><![CDATA["+(rs.getString("tran_id")==null?"":rs.getString("tran_id").trim())+"]]></tran_id>");

							simpleDateFormat=new SimpleDateFormat(genutility.getApplDateFormat());
							currDate = rs.getTimestamp("tran_date");
							if(currDate != null)
							{
								currAppdate = simpleDateFormat.format(currDate);
							}
							valueXmlString.append("<tran_date><![CDATA["+currAppdate+"]]></tran_date>");
							valueXmlString.append("<eff_date><![CDATA["+currAppdate+"]]></eff_date>");
							valueXmlString.append("<dist_order><![CDATA["+(rs.getString("dist_order")==null?"":rs.getString("dist_order").trim())+"]]></dist_order>");
							valueXmlString.append("<site_code><![CDATA["+(rs.getString("site_code")==null?"":rs.getString("site_code").trim())+"]]></site_code>");
							valueXmlString.append("<site_code__dlv><![CDATA["+(rs.getString("SITE_CODE__DLV")==null?"":rs.getString("SITE_CODE__DLV").trim())+"]]></site_code__dlv>");
							valueXmlString.append("<dist_route><![CDATA["+(rs.getString("DIST_ROUTE")==null?"":rs.getString("DIST_ROUTE"))+"]]></dist_route>");
							valueXmlString.append("<tran_code><![CDATA["+(rs.getString("tran_code")==null?"":rs.getString("tran_code"))+"]]></tran_code>");
							valueXmlString.append("<lr_no><![CDATA[]]></lr_no>");
							valueXmlString.append("<lr_date><![CDATA[]]></lr_date>");
							valueXmlString.append("<lorry_no><![CDATA[]]></lorry_no>");
							valueXmlString.append("<gross_weight><![CDATA["+(rs.getDouble("gross_weight"))+"]]></gross_weight>");
							valueXmlString.append("<tare_weight><![CDATA["+(rs.getDouble("tare_weight"))+"]]></tare_weight>");
							valueXmlString.append("<net_weight><![CDATA["+(rs.getDouble("net_weight"))+"]]></net_weight>");
							valueXmlString.append("<frt_amt><![CDATA[0]]></frt_amt>");
							valueXmlString.append("<amount><![CDATA[0]]></amount>");
							valueXmlString.append("<tax_amt><![CDATA[0]]></tax_amt>");
							valueXmlString.append("<net_amt><![CDATA[0]]></net_amt>");
							valueXmlString.append("<remarks><![CDATA["+(rs.getString("remarks")==null?"":rs.getString("remarks").trim())+"]]></remarks>");
							valueXmlString.append("<frt_type><![CDATA[T]]></frt_type>");
							valueXmlString.append("<chg_user><![CDATA["+(rs.getString("CHG_USER")==null?"":rs.getString("CHG_USER").trim())+"]]></chg_user>");
							valueXmlString.append("<chg_term><![CDATA["+(rs.getString("CHG_TERM")==null?"":rs.getString("CHG_TERM").trim())+"]]></chg_term>");
							valueXmlString.append("<curr_code><![CDATA["+(rs.getString("curr_code")==null?"":rs.getString("curr_code").trim())+"]]></curr_code>");
							valueXmlString.append("<chg_date><![CDATA["+currAppdate+"]]></chg_date>");
							valueXmlString.append("<site_descr><![CDATA["+(rs.getString(1)==null?"":rs.getString(1).trim())+"]]></site_descr>");
							valueXmlString.append("<site_to_descr><![CDATA["+(rs.getString(2)==null?"":rs.getString(2).trim())+"]]></site_to_descr>");
							valueXmlString.append("<location_descr><![CDATA["+(rs.getString(3)==null?"":rs.getString(3).trim())+"]]></location_descr>");
							valueXmlString.append("<tran_name><![CDATA["+(rs.getString("tran_name")==null?"":rs.getString("tran_name"))+"]]></tran_name>");
							valueXmlString.append("<currency_descr><![CDATA[]]></currency_descr>");
							valueXmlString.append("<confirmed><![CDATA[N]]></confirmed>");
							valueXmlString.append("<loc_code__git><![CDATA["+(rs.getString("loc_code__git")==null?"":rs.getString("loc_code__git"))+"]]></loc_code__git>");
							valueXmlString.append("<conf_date><![CDATA["+currAppdate+"]]></conf_date>");
							valueXmlString.append("<no_art><![CDATA[0]]></no_art>");
							valueXmlString.append("<trans_mode><![CDATA["+(rs.getString("TRANS_MODE")==null?"":rs.getString("TRANS_MODE").trim())+"]]></trans_mode>");
							valueXmlString.append("<gp_no><![CDATA[]]></gp_no>");
							valueXmlString.append("<gp_date/>");
							valueXmlString.append("<conf_passwd/>");
							valueXmlString.append("<order_type><![CDATA["+(rs.getString("ORDER_TYPE")==null?"":rs.getString("ORDER_TYPE").trim())+"]]></order_type>");
							valueXmlString.append("<gp_ser><![CDATA[I]]></gp_ser>");
							valueXmlString.append("<ref_no><![CDATA[]]></ref_no>");
							valueXmlString.append("<ref_date><![CDATA[]]></ref_date>");
							valueXmlString.append("<available_yn><![CDATA["+(rs.getString("available_yn")==null?"N":rs.getString("available_yn").trim())+"]]></available_yn>");
							valueXmlString.append("<site_add1><![CDATA["+(rs.getString("ADD1")==null?"":rs.getString("ADD1").trim())+"]]></site_add1>");
							valueXmlString.append("<site_add2><![CDATA["+(rs.getString("ADD2")==null?"":rs.getString("ADD2").trim())+"]]></site_add2>");
							valueXmlString.append("<site_city><![CDATA["+(rs.getString("CITY")==null?"":rs.getString("CITY").trim())+"]]></site_city>");
							valueXmlString.append("<site_pin><![CDATA["+(rs.getString("PIN")==null?"":rs.getString("PIN").trim())+"]]></site_pin>");
							valueXmlString.append("<site_state_code><![CDATA["+(rs.getString("STATE_CODE")==null?"":rs.getString("STATE_CODE").trim())+"]]></site_state_code>");
							valueXmlString.append("<exch_rate><![CDATA["+(rs.getDouble("EXCH_RATE"))+"  ]]></exch_rate>");
							valueXmlString.append("<tran_type><![CDATA["+(rs.getString("TRAN_TYPE")==null?"":rs.getString("TRAN_TYPE").trim())+"]]></tran_type>");
							valueXmlString.append("<emp_code__aprv><![CDATA[]]></emp_code__aprv>");
							valueXmlString.append("<discount><![CDATA[0]]></discount>");
							valueXmlString.append("<permit_no><![CDATA[]]></permit_no>");
							valueXmlString.append("<shipment_id><![CDATA[]]></shipment_id>");
							valueXmlString.append("<curr_code__frt><![CDATA[ ]]></curr_code__frt>");
							valueXmlString.append("<exch_rate__frt><![CDATA[]]></exch_rate__frt>");
							valueXmlString.append("<currency_descr__frt><![CDATA[]]></currency_descr__frt>");
							valueXmlString.append("<rd_permit_no><![CDATA[]]></rd_permit_no>");
							valueXmlString.append("<dc_no><![CDATA[]]></dc_no>");
							valueXmlString.append("<tran_ser><![CDATA[D-ISS ]]></tran_ser>");
							valueXmlString.append("<part_qty><![CDATA[A]]></part_qty>");
							valueXmlString.append("<sundry_details><![CDATA[]]></sundry_details>");
							valueXmlString.append("<sundry_name><![CDATA["+(rs.getString("SUNDRY_NAME")==null?"":rs.getString("SUNDRY_NAME"))+"]]></sundry_name>");
							valueXmlString.append("<proj_code><![CDATA["+(rs.getString("PROJ_CODE")==null?"":rs.getString("PROJ_CODE").trim())+"]]></proj_code>");
							valueXmlString.append("<site_tele1><![CDATA[]]></site_tele1>");
							valueXmlString.append("<site_tele2><![CDATA[]]></site_tele2>");
							valueXmlString.append("<site_tele3><![CDATA[]]></site_tele3>");
							valueXmlString.append("<site_code__bil><![CDATA[]]></site_code__bil>");
							valueXmlString.append("<site_descr_bill><![CDATA[]]></site_descr_bill>");
							valueXmlString.append("<site_add1_bill><![CDATA[]]></site_add1_bill>");
							valueXmlString.append("<site_add2_bill><![CDATA[]]></site_add2_bill>");
							valueXmlString.append("<site_city_bill><![CDATA[]]></site_city_bill>");
							valueXmlString.append("<site_pin_bill><![CDATA[]]></site_pin_bill>");
							valueXmlString.append("<site_state_code_bill><![CDATA[]]></site_state_code_bill>");
							valueXmlString.append("<pallet_wt><![CDATA[]]></pallet_wt>");
							valueXmlString.append("<auto_receipt><![CDATA[N]]></auto_receipt>");
							valueXmlString.append("<add_user><![CDATA["+(rs.getString("add_user")==null?"":rs.getString("add_user").trim())+"]]></add_user>");
							valueXmlString.append("<add_term><![CDATA["+(rs.getString("add_term")==null?"":rs.getString("add_term").trim())+"]]></add_term>");
							valueXmlString.append("<add_date>").append("<![CDATA[" + currAppdate + "]]>").append("</add_date>");
							valueXmlString.append("</Detail2>");
						}
						pstmt.close();
						rs.close();
						pstmt = null;
						rs = null;
					}
				
				

				break;
			case 3 : 
				if(currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					System.out.println("in itm default for>>> check itm_default");
					//System.out.println("FIND CHK ...>>>>>>.."+addUser+">>>>>>"+chgUser);
					distOrderNo = genericUtility.getColumnValue("dist_order1", dom2);
					lotSl = genericUtility.getColumnValue("lot_sl",dom2);
					System.out.println("lot sl from dom ...>>>>>>.."+genericUtility.getColumnValue("lot_sl",dom)+"lot sl from dom1 ...>>>>>>.."+genericUtility.getColumnValue("lot_sl",dom1)+"lot sl from dom2 ...>>>>>>.."+genericUtility.getColumnValue("lot_sl",dom2));
						System.out.println("in itm default for checkcccc itm_default");
						sql = 	 "SELECT item.descr,location.descr,"   
								+"distord_issdet.tran_id,"   
								+"distord_issdet.line_no,"   
								+"distord_issdet.dist_order,"   
								+"distord_issdet.line_no_dist_order,"  
								+"distord_issdet.item_code,"    
								+"distord_issdet.quantity,"    
								+"distord_issdet.unit,"    
								+"distord_issdet.tax_class,"    
								+"distord_issdet.tax_chap,"    
								+"distord_issdet.tax_env,"    
								+"distord_issdet.loc_code,"    
								+"distord_issdet.lot_no,"    
								+"distord_issdet.lot_sl,"    
								+"distord_issdet.pack_code,"    
								+"distord_issdet.rate,"    
								+"distord_issdet.amount,"    
								+"distord_issdet.tax_amt,"    
								+"distord_issdet.net_amt,"    
								+"distord_issdet.site_code__mfg,"    
								+"distord_issdet.mfg_date,"    
								+"distord_issdet.exp_date,"    
								+"distord_issdet.potency_perc,"    
								+"distord_issdet.no_art,"    
								+"distord_issdet.gross_weight,"    
								+"distord_issdet.tare_weight,"    
								+"distord_issdet.net_weight,"    
								+"distord_issdet.pack_instr,"    
								+"distord_issdet.dimension,"    
								+"distord_issdet.supp_code__mfg,"    
								+"distord_issdet.batch_no,"    
								+"distord_issdet.grade,"    
								+"distord_issdet.retest_date,"    
								+"distord_issdet.rate__clg,"    
								+"distord_issdet.discount,"    
								+"distord_issdet.disc_amt,"    
								+"distord_issdet.remarks,"    
								+"distord_issdet.cost_rate,"    
								+"space(300) as qty_details,"    
								+"distord_issdet.unit__alt,"    
								+"distord_issdet.conv__qty__alt,"    
								+"distord_issdet.qty_order__alt,"    
								+"distord_issdet.pallet_wt,"    
								+"distorder_det.reas_code,"    
								+"distord_issdet.rate__alt,"    
								+"distord_issdet.conv__rate_alt,"    
								+"distord_issdet.batch_size,"    
								+"distord_issdet.shelf_life_type "   
								+"FROM distord_issdet,"    
								+"item,"    
								+"location,"    
								+"distorder_det, " 
								+"distord_iss "   
								+"WHERE ( distord_issdet.item_code = item.item_code ) and "  
								+" ( distord_issdet.loc_code = location.loc_code ) and "  
								+" ( distord_issdet.dist_order = distorder_det.dist_order ) and "  
								+" ( distord_issdet.line_no_dist_order = distorder_det.line_no ) and " 
								+" ( distord_iss.tran_id = distord_issdet.tran_id ) and "  
								+" distord_issdet.dist_order = '"+distOrderNo+"'";

						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						while(rs.next())
						{
							valueXmlString.append("<Detail3 domID='" +rs.getString("line_no")+ "'  objContext = '"+currentFormNo+"' selected=\"N\">\r\n");
							valueXmlString.append("<attribute selected=\"N\" updateFlag=\"E\" status=\"O\" pkNames=\"\"/>\r\n");
							//valueXmlString.append("<tran_id><![CDATA["+tranid+"]]></tran_id>");
							valueXmlString.append("<tran_id>").append("<![CDATA[" + tranid + "]]>").append("</tran_id>");
							valueXmlString.append("<dist_order><![CDATA["+rs.getString("dist_order")+"]]></dist_order>");
							valueXmlString.append("<line_no_dist_order><![CDATA["+rs.getString("line_no_dist_order")+"]]></line_no_dist_order>");
							valueXmlString.append("<line_no><![CDATA["+rs.getString("line_no")+"]]></line_no>");
							valueXmlString.append("<item_code>").append("<![CDATA["+(rs.getString("item_code")==null?"":rs.getString("item_code").trim())+"]]>").append("</item_code>\r\n");
							valueXmlString.append("<item_descr>").append("<![CDATA["+(rs.getString(1)==null?"":rs.getString(1).trim())+"]]>").append("</item_descr>\r\n");
							valueXmlString.append("<location_descr>").append("<![CDATA["+(rs.getString(2)==null?"":rs.getString(2).trim())+"]]>").append("</location_descr>\r\n");
							valueXmlString.append("<unit>").append("<![CDATA["+(rs.getString("unit")==null?"":rs.getString("unit").trim())+"]]>").append("</unit>\r\n");
							valueXmlString.append("<unit__alt>").append("<![CDATA["+(rs.getString("unit__alt")==null?"":rs.getString("unit__alt").trim())+"]]>").append("</unit__alt>\r\n");
							valueXmlString.append("<conv__qty__alt>").append("<![CDATA["+rs.getDouble("conv__qty__alt")+"]]>").append("</conv__qty__alt>\r\n");
							valueXmlString.append("<qty_order__alt>").append("<![CDATA["+rs.getDouble("conv__qty__alt")+"]]>").append("</qty_order__alt>\r\n");
							valueXmlString.append("<loc_code>").append("<![CDATA["+(rs.getString("loc_code")==null?"":rs.getString("loc_code").trim())+"]]>").append("</loc_code>\r\n");
							valueXmlString.append("<rate>").append("<![CDATA["+rs.getDouble("rate")+"]]>").append("</rate>\r\n");
							valueXmlString.append("<rate__clg>").append("<![CDATA["+rs.getDouble("rate__clg")+"]]>").append("</rate__clg>\r\n"); //Commented - jiten - 05/04/06 -  as set in itemChange of lot_no
							valueXmlString.append("<quantity>").append("<![CDATA["+rs.getDouble("quantity")+"]]>").append("</quantity>\r\n");
							valueXmlString.append("<amount>").append("<![CDATA["+rs.getDouble("amount")+"]]>").append("</amount>\r\n");
							valueXmlString.append("<lot_sl>").append("<![CDATA["+(rs.getString("lot_sl")==null?"":rs.getString("lot_sl").trim())+"]]>").append("</lot_sl>\r\n");
							valueXmlString.append("<pack_code>").append("<![CDATA["+(rs.getString("pack_code")==null?"":rs.getString("pack_code").trim())+"]]>").append("</pack_code>\r\n");
							valueXmlString.append("<disc_amt>").append("<![CDATA["+rs.getDouble("disc_amt")+"]]>").append("</disc_amt>\r\n");
							valueXmlString.append("<tax_class>").append("<![CDATA["+(rs.getString("tax_class")==null?"":rs.getString("tax_class").trim())+"]]>").append("</tax_class>\r\n");
							valueXmlString.append("<tax_chap>").append("<![CDATA["+(rs.getString("tax_chap")==null?"":rs.getString("tax_chap").trim())+"]]>").append("</tax_chap>\r\n");
							valueXmlString.append("<tax_env>").append("<![CDATA["+(rs.getString("tax_env")==null?"":rs.getString("tax_env").trim())+"]]>").append("</tax_env>\r\n");
							valueXmlString.append("<gross_weight>").append("<![CDATA["+rs.getDouble("gross_weight")+"]]>").append("</gross_weight>\r\n");
							valueXmlString.append("<net_weight>").append("<![CDATA["+rs.getDouble("net_weight")+"]]>").append("</net_weight>\r\n");
							valueXmlString.append("<tare_weight>").append("<![CDATA["+rs.getDouble("tare_weight")+"]]>").append("</tare_weight>\r\n");
							valueXmlString.append("<pack_instr>").append("<![CDATA["+(rs.getString("pack_instr")==null?"":rs.getString("pack_instr").trim())+"]]>").append("</pack_instr>\r\n"); //Gulzar 24/03/07
							valueXmlString.append("<retest_date>").append("<![CDATA[").append((rs.getDate("retest_date") == null) ? "":sdf.format(rs.getDate("retest_date"))).append("]]>").append("</retest_date>\r\n");
							valueXmlString.append("<dimension>").append("<![CDATA[").append((rs.getString("dimension") == null) ? "":rs.getString("dimension")).append("]]>").append("</dimension>\r\n");
							valueXmlString.append("<supp_code__mfg>").append("<![CDATA[").append((rs.getString("supp_code__mfg") == null) ? "":rs.getString("supp_code__mfg")).append("]]>").append("</supp_code__mfg>\r\n"); //Gulzar 24/03/07
							valueXmlString.append("<site_code__mfg>").append("<![CDATA[").append((rs.getString("site_code__mfg") == null) ? "":rs.getString("site_code__mfg")).append("]]>").append("</site_code__mfg>\r\n");
							valueXmlString.append("<mfg_date>").append("<![CDATA[").append((rs.getDate("mfg_date") == null) ? "":sdf.format(rs.getDate("mfg_date"))).append("]]>").append("</mfg_date>\r\n");
							valueXmlString.append("<exp_date>").append("<![CDATA[").append((rs.getDate("exp_date") == null) ? "":sdf.format(rs.getDate("exp_date"))).append("]]>").append("</exp_date>\r\n");
							valueXmlString.append("<potency_perc>").append("<![CDATA[").append( ( (rs.getString("potency_perc") == null) ? "": rs.getString("potency_perc") ) ).append("]]>").append("</potency_perc>\r\n");
							valueXmlString.append("<no_art>").append("<![CDATA["+rs.getDouble("no_art")+"]]>").append("</no_art>\r\n");
							valueXmlString.append("<batch_no>").append("<![CDATA[").append( ( (rs.getString("batch_no") == null) ? "":rs.getString("batch_no") ) ).append("]]>").append("</batch_no>\r\n");
							valueXmlString.append("<grade>").append("<![CDATA[").append( ( (rs.getString("grade") == null) ? "": rs.getString("grade") ) ).append("]]>").append("</grade>\r\n");
							valueXmlString.append("<lot_no>").append("<![CDATA[").append(( (rs.getString("lot_no") == null) ? "": rs.getString("lot_no"))).append("]]>").append("</lot_no>\r\n");
							valueXmlString.append("</Detail3>");
						}
						pstmt.close();
						rs.close();
						pstmt = null;
						rs = null;
					
				}/*else if(currentColumn.trim().equalsIgnoreCase("itm_default_add"))
				{
					distOrderiss = genericUtility.getColumnValue("dist_order1", dom2);
					lotSl = genericUtility.getColumnValue("lot_sl", dom2);

					sql = "select line_no, dist_order from distord_issdet where tran_id = ? and lot_sl = ?";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1,distOrderiss);
					pstmt.setString(2,lotSl);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						lineNo = rs.getInt(1);
						distOrder = rs.getString(2);
					}
					pstmt.close();
					rs.close();
					pstmt = null;
					rs = null;

					lineNo++;
					System.out.println(">>>>>>>>>>>>>..dom"+distOrderiss+">><><"+lotSl+">>>>>>>>>>>>>>>>>"+genericUtility.getColumnValue("lot_sl", dom)+">>>>>>>1>>>>>>>>>>"+genericUtility.getColumnValue("lot_sl", dom1)+"");
					valueXmlString.append("<Detail3 domID='" + lineNo+ "'  objContext = '"+currentFormNo+"' selected=\"N\">\r\n");
					valueXmlString.append("<attribute  selected=\"N\" updateFlag=\"A\"  status=\"N\" pkNames=\"\"/>\r\n");
					valueXmlString.append("<tran_id><![CDATA["+distOrderiss+"]]></tran_id>");

					prvDeptCode = "NULL";
					sql =    "SELECT D.DIST_ORDER,D.LINE_NO AS LINE_NO,D.TRAN_ID__DEMAND,D.ITEM_CODE AS ITEM_CODE,D.QTY_ORDER AS QTY_ORDER,D.QTY_CONFIRM AS QTY_CONFIRM,"
							+"D.QTY_RECEIVED AS QTY_RECEIVED,D.QTY_SHIPPED AS QTY_SHIPPED,D.DUE_DATE AS DUE_DATE,D.TAX_CLASS AS TAX_CLASS,D.TAX_CHAP AS TAX_CHAP,D.TAX_ENV AS TAX_ENV,D.UNIT AS UNIT,ITEM.DESCR AS ITEM_DESCR,"
							+"D.SALE_ORDER AS SALE_ORDER,D.LINE_NO__SORD AS LINE_NO__SORD,D.RATE AS RATE,D.QTY_RETURN AS QTY_RETURN,D.RATE__CLG AS RATE__CLG,D.DISCOUNT AS DISCOUNT,D.REMARKS AS REMARKS,D.TOT_AMT AS TOT_AMT,D.TAX_AMT AS TAX_AMT,"
							+"D.NET_AMT AS NET_AMT,D.OVER_SHIP_PERC AS OVER_SHIP_PERC,SPACE(300) AS QTY_DETAILS,D.UNIT__ALT AS UNIT__ALT,D.CONV__QTY__ALT AS CONV__QTY__ALT,"
							+"D.QTY_ORDER__ALT AS QTY_ORDER__ALT,D.SHIP_DATE AS SHIP_DATE,D.PACK_INSTR AS PACK_INSTR ,"
							+"( CASE WHEN ITEM.DEPT_CODE__ISS IS NULL then ' ' else ITEM.DEPT_CODE__ISS END ) AS DEPT_CODE, "
							+"H.AVALIABLE_YN, H.TRAN_TYPE AS TRAN_TYPE, CASE WHEN H.LOC_GROUP__JWISS IS NULL THEN ' ' ELSE H.LOC_GROUP__JWISS END AS LOC_GROUP "
							+"FROM DISTORDER_DET  D,ITEM  ITEM, DISTORDER H "
							+"WHERE D.DIST_ORDER = H.DIST_ORDER "
							+"AND D.ITEM_CODE = ITEM.ITEM_CODE "
							+"AND H.DIST_ORDER    = '"+distOrder+"'";
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						//added by msalam on 180609 to get tran_type from distorder start
						tranType = rs.getString( "TRAN_TYPE" );
						//added by msalam on 180609 to get tran_type from distorder end 
						locGroupJwiss=rs.getString( "LOC_GROUP" );
						System.out.println("(locGroupJwiss.trim()).length()----"+(locGroupJwiss.trim()).length());
						if((locGroupJwiss.trim()).length()>0)
						{
							subSQL=" AND C.LOC_GROUP ='"+locGroupJwiss+"' ";
						}
						else
						{
							subSQL="";
						}
						// 28/05/09 manoharan available_yn added
						availableYn = rs.getString("AVALIABLE_YN");
						if( availableYn == null )
						{
							availableYn = "Y";
						}
						// end 28/05/09 manoharan available_yn added
						deptCode = rs.getString("DEPT_CODE");
						//System.out.println( "deptCode....................... " + deptCode );
						if("NULL".equalsIgnoreCase(prvDeptCode))
						{
							prvDeptCode = deptCode;
							//System.out.println( "prvDeptCode....................... " + prvDeptCode );
						}
						System.out.println("prvDeptCode----deptCode--"+prvDeptCode+"----"+deptCode);

						System.out.println( "match dept....................... " );
						valueXmlString.append("<dist_order><![CDATA["+distOrder+"]]></dist_order>");
						valueXmlString.append("<line_no_dist_order><![CDATA["+rs.getInt("LINE_NO")+"]]></line_no_dist_order>");

						mLineNoDist = rs.getInt("LINE_NO");
						unit = rs.getString("UNIT");
						unitAlt = rs.getString("UNIT__ALT");
						itemCode = rs.getString("ITEM_CODE");
						qtyConfirm = rs.getDouble("QTY_CONFIRM");
						qtyShipped = rs.getDouble("QTY_SHIPPED");
						discount =	rs.getDouble("DISCOUNT");
						remQuantity = qtyConfirm - qtyShipped;
						System.out.println("calling getDetails");
						valueXmlString.append(getDetails(siteCode,mLineNoDist,distOrder,tranType,conn));
						System.out.println("calling getDetails exit>>>>>");

						if (tranType != null && tranType.trim().length() > 0)
						{
							System.out.println("tranType != null && tranType.trim().length() > 0");
							sql = "SELECT CHECK_INTEGRAL_QTY, TRAN_TYPE__PARENT FROM DISTORDER_TYPE WHERE TRAN_TYPE = '"+tranType+"' ";

							pstmt1   = conn.prepareStatement(sql);
							//pstmt1.setString(1,tranType);
							rs1 = pstmt1.executeQuery();
							if (rs1.next())
							{
								//System.out.println( "CHECK_INTEGRAL_QTY :" + rs.getString( 1 ) );
								checkIntegralQty = rs1.getString( 1 );
								tranTypeParent = rs1.getString( 2 );
								//System.out.println( "tranTypeParent : " + tranTypePparent );
								if (checkIntegralQty == null || checkIntegralQty.trim().length() == 0)
								{
									checkIntegralQty = "Y";
								}
							}
							// added 18/06/09 manoharan
							rs1.close();
							rs1 = null;
							pstmt1.close();
							pstmt1 = null;
							// end added 18/06/09 manoharan
						}
						if (!tranType.equals(tranTypeParent))
						{
							System.out.println("!tranType.equals(tranTypeParent)");
						}
						else
						{
							System.out.println("!tranType.equals(tranTypeParent else)");
						}
						//availableYn ="Y"; // 28/05/09 manoharan commented taken from distorder table
						sql =  " SELECT (CASE WHEN ACTIVE IS NULL THEN 'Y' ELSE ACTIVE END) ACT, MIN_SHELF_LIFE, "
								+ " (CASE WHEN TRACK_SHELF_LIFE IS NULL THEN 'N' ELSE TRACK_SHELF_LIFE END) TRK_SHELF_LIFE, "
								+ " (CASE WHEN SUPP_SOUR IS NULL THEN 'M' ELSE SUPP_SOUR END) SUP_SOUR, DESCR "
								+ " FROM ITEM WHERE ITEM_CODE = '"+itemCode+"' ";
						pstmt1= conn.prepareStatement(sql);
						rs1 = pstmt1.executeQuery();
						if ( rs1.next() )
						{
							active = rs1.getString( 1 );
							minShelfLife = rs1.getInt( 2 );
							itemDescr = rs1.getString( 5 );
							if( active.equals("N") )
							{
								System.out.println("VTITEM4 error through");
								errCode = "VTITEM4";
								errString = itmDBAccessEJB.getErrorString("", errCode, "", "", conn);
								return errString;
							}
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null ;
						sql = " SELECT LOC_CODE__DAMAGED, SUNDRY_CODE, PRICE_LIST, PRICE_LIST__CLG, SITE_CODE__SHIP "
								+ " FROM DISTORDER WHERE DIST_ORDER = '"+distOrder+"' ";
						pstmt1= conn.prepareStatement(sql);
						rs1 = pstmt1.executeQuery();
						if (rs1.next())
						{
							siteCodeShip = rs1.getString("SITE_CODE__SHIP");

							locCodeDamaged = rs1.getString("LOC_CODE__DAMAGED");
							//System.out.println("locCodeDamaged :"+locCodeDamaged);
							sundryCode = rs1.getString( 2 );
							priceList = rs1.getString( 3 );
							//System.out.println("priceList :" + priceList);

							priceListClg = rs1.getString( 4 );
							//System.out.println( "priceListClg :" + priceListClg );

							if (locCodeDamaged == null)
							{
								locCodeDamaged = "";
							}
							if (locCodeDamaged != null && locCodeDamaged.trim().length() > 0)
							{
								StringTokenizer st = new StringTokenizer(locCodeDamaged,",");
								res = ""; //  28/05/09 manoharan
								while (st.hasMoreTokens())
								{
									res = res + "'" + st.nextToken() + "',";
								}
								res = res.substring(0,res.length()-1);
								//System.out.println("res ::" + res);
								locCodeDamaged = res;
								//System.out.println("locCodeDamaged After String Tockenized ::"+locCodeDamaged);
							}
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null ;
						boolean isRecordFound = false;



						sql = "SELECT A.LOT_NO, A.LOT_SL, A.QUANTITY, A.EXP_DATE, A.UNIT, A.ITEM_SER, "
								+"A.SITE_CODE__MFG, A.MFG_DATE, A.POTENCY_PERC, A.ALLOC_QTY, "
								+"A.PACK_CODE, A.LOC_CODE, A.BATCH_NO, A.GRADE , "
								+"A.GROSS_WEIGHT, A.TARE_WEIGHT, A.NET_WEIGHT, A.DIMENSION, A.RETEST_DATE, "
								+"A.SUPP_CODE__MFG, A.PACK_INSTR,A.RATE,C.DESCR "
								+"FROM STOCK A, INVSTAT B, LOCATION C "
								+"WHERE C.INV_STAT = B.INV_STAT "
								+"AND A.LOC_CODE = C.LOC_CODE "
								+"AND A.ITEM_CODE = '"+itemCode+"'  "
								+"AND A.SITE_CODE = '"+siteCodeShip+"'  "
								+"AND B.AVAILABLE = '"+availableYn+"'  "
								+"AND B.USABLE = '"+availableYn+"' "
								+"AND B.STAT_TYPE <> 'S' "
								+""+subSQL+""
								+" AND A.QUANTITY - A.ALLOC_QTY > 0 ";

						if( availableYn != null && availableYn.equals("Y") )
						{
							sql = sql + " AND NOT EXISTS (SELECT 1 FROM INV_RESTR I "
									+"WHERE I.INV_STAT = B.INV_STAT AND I.REF_SER = 'D-ISS') ";
						}
						if( locCodeDamaged != null && locCodeDamaged.trim().length() > 0 )
						{
							sql = sql + "AND A.LOC_CODE IN (" + locCodeDamaged + ")";
						}
						sql =  sql + " ORDER BY CASE WHEN A.EXP_DATE IS NULL THEN A.CREA_DATE ELSE A.EXP_DATE END,A.CREA_DATE,A.LOT_NO, A.LOT_SL ";
						pstmt1= conn.prepareStatement(sql);
						rs1 = pstmt1.executeQuery();

						System.out.println("Resetting detail2xml----"+detail2xml);
						System.out.println("ItemCode----"+itemCode);

						while (rs1.next())
							//changed by msalam on 180609 for stopping processing only 999 rows. end
						{
							//System.out.println( "inside while........................" );
							isRecordFound = true;
							lotNo = rs1.getString(1);
							lotSl = rs1.getString(2);
							packCode = rs1.getString(11);
							if (remQuantity == 0)
							{
								break;
							}
							// 11/09/09 manoharan if in stock there is invalid site_code__mfg then skip the item 
							siteCodeMfg = rs1.getString(7);
							if (siteCodeMfg != null && siteCodeMfg.trim().length() > 0)
							{
								sql2 = "SELECT COUNT(*) FROM SITE "
										+ "WHERE SITE_CODE = ?";
								pstmt2= conn.prepareStatement(sql2);
								pstmt2.setString(1,siteCodeMfg);
								rs2 = pstmt2.executeQuery();
								if (rs2.next())
								{
									count = rs2.getInt(1);
								}
								rs2.close();
								rs2 = null;
								pstmt2.close();
								pstmt2 = null;

								//System.out.println("count :" + count);
								if (count == 0)
								{
									continue;
								}

							} 
							// end 11/09/09 manoharan if in stock there is invalid site_code__mfg then skip the item

							stockQty = rs1.getDouble(3) - rs1.getDouble(10);
							//System.out.println("stockQty :" + stockQty);
							if (availableYn.equals("Y"))
							{
								if (minShelfLife > 0)
								{
									chkDate = calcExpiry(tranDate,minShelfLife); //calcExpiry function to be checked.
									//System.out.println("chkDate :" + chkDate);
									chkDate1 = sdf.parse(chkDate);
									java.sql.Date date1 = rs1.getDate(4);
									//System.out.println("date1 :" + date1);
									java.util.Date date2 = null;
									if(date1 != null)
									{
										date2 = new java.util.Date(date1.getTime());
										//System.out.println("chkDate1 :" + chkDate1);
										//System.out.println("date2 :" + date2);
										if((chkDate1.compareTo(date2) > 0))
										{
											continue;
										}
									}
								}
							}
							if (!checkIntegralQty.equals("N"))
							{
								//integralQty = getIntegralQty( siteCode, itemCode, lotNo, packCode,conn );
								integralQty = getIntegralQty( siteCode, itemCode, lotNo, packCode, checkIntegralQty );
								//System.out.println("integralQty :"+integralQty);
								if (integralQty <= 0)
								{
									errCode = "VINTGRLQTY";
									errString = itmDBAccessEJB.getErrorString( "", errCode, "", "", conn);
									//System.out.println("errString:" + errString + ":");
									return errString;
								}
							}
							if (stockQty >= remQuantity)
							{
								if (checkIntegralQty.equals("Y"))
								{
									remQuantity = remQuantity - (remQuantity % integralQty);
									//System.out.println("remQuantity :"+remQuantity);
								}
								minputQty = remQuantity;
								remQuantity = 0;
							}
							else if (stockQty < remQuantity)
							{
								if (checkIntegralQty.equals("Y"))
								{
									stockQty = stockQty - (stockQty % integralQty);
								}
								minputQty = stockQty;
								remQuantity = remQuantity - stockQty;
							}
							if (minputQty == 0)
							{
								continue;
							}
							rate1 = rate;
							if (rate1.equals(""))
							{
								rate1 = "0";
							}
							System.out.println("rate1----"+rate1+"---priceList----"+priceList);
							if (Double.parseDouble(rate1) == 0)
							{
								if( priceList != null && priceList.trim().length() > 0 )
								{
									sql2 = "SELECT COUNT(*) FROM PRICELIST "
											+ "WHERE PRICE_LIST = '"+priceList+"'  AND LIST_TYPE = 'I' ";
									pstmt2= conn.prepareStatement(sql2);
									rs2 = pstmt2.executeQuery();
									if (rs2.next())
									{
										count = rs2.getInt(1);
									}
									rs2.close();
									rs2 = null;
									pstmt2.close();
									pstmt2 = null;

									//System.out.println("count :" + count);
									if (count == 0)
									{
										rate2 = disCommon.pickRate(priceList, tranDate, itemCode, rs1.getString(1),"D",conn);
										//System.out.println("rate2 :" + rate2);
									}
									else
									{
										tabValue = siteCode + "~t" + rs1.getString(12) + "~t" + rs1.getString(1) + "~t";
										System.out.println("printing tabValue----"+tabValue);
										//System.out.println("tabValue :" + tabValue);
										rate2 = disCommon.pickRate(priceList, tranDate, itemCode, tabValue, "I",conn);
										//System.out.println("rate2 :" + rate2);
									}
								}
								rate1 = Double.toString(rate2);
							}
							rateClg = null;
							if (rateClg == null || rateClg.equals("") || Double.parseDouble(rateClg) == 0)
							{
								if (priceListClg != null && priceListClg.trim().length() > 0 )
								{
									rateClgVal = disCommon.pickRate(priceListClg, tranDate, itemCode, rs1.getString(1),"D",conn);
									//System.out.println("rateClgVal :"+rateClgVal);
								}
								if (rateClgVal <= 0)
								{
									rateClgVal = rate2;
									//System.out.println("rateClgVal :"+rateClgVal);
								}
								rateClg = Double.toString(rateClgVal);
							}
							if (Double.parseDouble(rs1.getString(3)) > 0)
							{
								grossPer    = rs1.getDouble(15) / rs1.getDouble(3);
								//System.out.println("grossPer :"+grossPer);
								netPer 	    = rs1.getDouble(17) 	/ rs1.getDouble(3);
								//System.out.println("netPer :"+netPer);
								tarePer 	= rs1.getDouble(16) / rs1.getDouble(3);
								//System.out.println("tarePer :"+tarePer);
								grossWt = minputQty * grossPer;
								//System.out.println("grossWt :"+grossWt);
								netWt   = minputQty * netPer;
								//System.out.println("netWt :"+netWt);
								tareWt  = minputQty * tarePer;
								//System.out.println("tareWt :"+tareWt);
							}
							disAmount = (amount * ( discount / 100));
							//if( sundryCode != null && sundryCode.trim().length() > 0 )
							//{
							noArt1 = 0;
							//noArt1 = disCommon.getNoArt(siteCode, sundryCode, itemCode, packCode,minputQty, 'B', shipperQty, integralQty,conn);
							noArt1 = getNoArt(siteCode, sundryCode, itemCode, packCode,minputQty, 'B', shipperQty, integralQty);
							//System.out.println("noArt1 :"+noArt1);
							noArt = "" + noArt1;
							System.out.println("%^%^%^%^%^%NoArt is&***&*&*&*&["+noArt+"]");
							//}
							//minputQty
							double shipperSize=0,shipQty=0,noArt11=0,remainder=0;
							double integralqty=0;
							double noArt12=0,acShipperQty=0,acIntegralQty=0;
							Statement stmt1 = conn.createStatement();
							sql ="select (case when shipper_size is null then 0 else shipper_size end) shipper_size"
									+" from item_lot_packsize where item_code = '"+itemCode+"'"
									+" and  '"+lotNo+"' >= lot_no__from "
									+" and  '"+lotNo+"'  <= lot_no__to ";
							System.out.println("sql :"+sql);
							rs3 = stmt1.executeQuery(sql);
							if (rs3.next())
							{
								shipperSize = rs3.getDouble(1);
							}
							System.out.println("shipperSize .............:"+shipperSize);	
							System.out.println("minputQty .............:"+minputQty);	
							if( shipperSize > 0)
							{
								shipQty = shipperSize;
								noArt11 = (minputQty - (minputQty % shipQty))/shipQty;
								System.out.println("noArt11 .............:"+noArt11);
								remainder = minputQty % shipQty;
								System.out.println("remainder .............:"+remainder);
								sql ="select ( case when integral_qty is null then 0 else integral_qty end) integral_qty"
										+" from customeritem where cust_code = '"+sundryCode+"' and item_code ='"+itemCode+"'";
								System.out.println("sql :"+sql);
								rs3 = stmt1.executeQuery(sql);
								if (rs3.next())
								{
									integralqty = rs3.getDouble(1);

								}
								System.out.println("integralqty .............:"+integralqty);
								if(integralqty ==0)
								{
									sql ="select  ( case when integral_qty is null then 0 else integral_qty end) integral_qty"
											+" from siteitem where site_code = '"+siteCode+"' and item_code ='"+itemCode+"'";
									System.out.println("sql :"+sql);
									rs3 = stmt1.executeQuery(sql);
									if (rs3.next())
									{
										integralqty = rs3.getDouble(1);

									}
									if(integralqty ==0)
									{
										sql ="select ( case when integral_qty is null then 0 else integral_qty end) integral_qty"
												+" from item where item_code ='"+itemCode+"'";
										System.out.println("sql :"+sql);
										rs3 = stmt1.executeQuery(sql);
										if (rs3.next())
										{
											integralqty = rs3.getDouble(1);
											//System.out.println("integralqty .............:"+integralqty);
										}
									}

								} 
								System.out.println("integralqty .............:"+integralqty);
								if(integralqty > 0)
								{
									noArt12 = (remainder -(remainder % integralqty))/integralqty;
									//System.out.println("noArt12 ....2.........:"+noArt12);
								}
								if(noArt12 > 0)
								{
									noArt12 =1;
									//System.out.println("noArt2 ....0.........:"+noArt12);
								}
								noArt1			= (int)(noArt11 + noArt12);
								noArt = "" + noArt1;
								System.out.println("noArt .............:"+noArt);
								acShipperQty	= shipQty;
								acIntegralQty	= integralqty;
							}
							if(shipperSize ==0)
							{
								noArt1 = getNoArt(siteCode, sundryCode, itemCode, packCode, minputQty, 'B', acShipperQty, acIntegralQty);
								noArt = "" + noArt1;
								//System.out.println("noArt .............:"+noArt);
							}

							lcFact =0;
							ArrayList QtyFactorList = new ArrayList();

							QtyFactorList =	disCommon.convQtyFactor(unitAlt, unit, itemCode, minputQty, lcFact,conn);
							lcQtyOrderAlt =	((Double)QtyFactorList.get(1)).doubleValue() ;
							lcFact 	=	((Double)QtyFactorList.get(0)).doubleValue() ;
							QtyFactorList = null;

							
							valueXmlString.append("<line_no><![CDATA["+(lineNo)+"]]></line_no>");
						//	valueXmlString.append("<item_descr>").append("<![CDATA[").append(itemDescr==null ? "" : itemDescr).append("]]>").append("</item_descr>\r\n");
						//	valueXmlString.append("<location_descr>").append("<![CDATA[").append(rs1.getString(23)).append("]]>").append("</location_descr>\r\n");
							valueXmlString.append("<unit>").append("<![CDATA[").append(rs1.getString(5)).append("]]>").append("</unit>\r\n");
							valueXmlString.append("<unit__alt>").append("<![CDATA[").append(unitAlt).append("]]>").append("</unit__alt>\r\n");
							valueXmlString.append("<conv__qty__alt>").append("<![CDATA[").append(lcFact).append("]]>").append("</conv__qty__alt>\r\n");
							valueXmlString.append("<qty_order__alt>").append("<![CDATA[").append(lcQtyOrderAlt).append("]]>").append("</qty_order__alt>\r\n");
							String tLocCode = null;
							tLocCode = rs1.getString(12);
						//	valueXmlString.append("<loc_code>").append("<![CDATA[").append( (tLocCode == null ? "" : tLocCode.trim()) ).append("]]>").append("</loc_code>\r\n");
							//commented for rajendra on 04/09/08 for pick up rate from stock
							valueXmlString.append("<rate>").append("<![CDATA[").append(rate1).append("]]>").append("</rate>\r\n");
							//xmldetail2stock.append("<rate>").append("<![CDATA[").append(ratefromStock).append("]]>").append("</rate>\r\n");
							valueXmlString.append("<rate__clg>").append("<![CDATA[").append(rateClg).append("]]>").append("</rate__clg>\r\n"); //Commented - jiten - 05/04/06 -  as set in itemChange of lot_no
						//	valueXmlString.append("<quantity>").append("<![CDATA[").append(minputQty).append("]]>").append("</quantity>\r\n");
							valueXmlString.append("<amount>").append("<![CDATA[").append(minputQty*Double.parseDouble(rate1)).append("]]>").append("</amount>\r\n");
							String tLotSl = null;
							tLotSl = rs1.getString(2);
						//	valueXmlString.append("<lot_sl>").append("<![CDATA[").append( ( tLotSl == null ? "    " : tLotSl) ).append("]]>").append("</lot_sl>\r\n");
							valueXmlString.append("<pack_code>").append("<![CDATA[").append((rs1.getString(11) == null) ? "":rs1.getString(11)).append("]]>").append("</pack_code>\r\n");
							valueXmlString.append("<disc_amt>").append("<![CDATA[").append(disAmount).append("]]>").append("</disc_amt>\r\n");
							//xmldetail2stock.append("<tax_class>").append("<![CDATA[").append( ( taxClass == null ? "": taxClass ) ).append("]]>").append("</tax_class>\r\n");
							//xmldetail2stock.append("<tax_chap>").append("<![CDATA[").append( ( taxChap == null ? "": taxChap ) ).append("]]>").append("</tax_chap>\r\n");
							//xmldetail2stock.append("<tax_env>").append("<![CDATA[").append( ( taxEnv == null ? "": taxEnv ) ).append("]]>").append("</tax_env>\r\n");
							grossWt = Double.parseDouble(getFormatedValue(grossWt,3));
							//System.out.println("[DistIssueActEJB] Gross Wt=============>"+grossWt);
						//	valueXmlString.append("<gross_weight>").append("<![CDATA[").append(grossWt).append("]]>").append("</gross_weight>\r\n");
							netWt = Double.parseDouble(getFormatedValue(netWt,3));
							//System.out.println("[DistIssueActEJB] Net Wt=============>"+netWt);
							valueXmlString.append("<net_weight>").append("<![CDATA[").append(netWt).append("]]>").append("</net_weight>\r\n");
							tareWt = Double.parseDouble(getFormatedValue(netWt,3));
							//System.out.println("[DistIssueActEJB] Tare Wt=============>"+tareWt);
							valueXmlString.append("<tare_weight>").append("<![CDATA[").append(tareWt).append("]]>").append("</tare_weight>\r\n");
							valueXmlString.append("<pack_instr>").append("<![CDATA[").append((rs1.getString(21) == null) ? "":rs1.getString(21)).append("]]>").append("</pack_instr>\r\n"); //Gulzar 24/03/07
							valueXmlString.append("<retest_date>").append("<![CDATA[").append((rs1.getDate(19) == null) ? "":sdf.format(rs1.getDate(19))).append("]]>").append("</retest_date>\r\n");
							valueXmlString.append("<dimension>").append("<![CDATA[").append((rs1.getString(18) == null) ? "":rs1.getString(18)).append("]]>").append("</dimension>\r\n");
							valueXmlString.append("<supp_code__mfg>").append("<![CDATA[").append((rs1.getString(20) == null) ? "":rs1.getString(20)).append("]]>").append("</supp_code__mfg>\r\n"); //Gulzar 24/03/07
							valueXmlString.append("<site_code__mfg>").append("<![CDATA[").append((rs1.getString(7) == null) ? "":rs1.getString(7)).append("]]>").append("</site_code__mfg>\r\n");
							valueXmlString.append("<mfg_date>").append("<![CDATA[").append((rs1.getDate(8) == null) ? "":sdf.format(rs1.getDate(8))).append("]]>").append("</mfg_date>\r\n");
							valueXmlString.append("<exp_date>").append("<![CDATA[").append((rs1.getDate(4) == null) ? "":sdf.format(rs1.getDate(4))).append("]]>").append("</exp_date>\r\n");
							valueXmlString.append("<potency_perc>").append("<![CDATA[").append( ( (rs1.getString(9) == null) ? "": rs1.getString(9) ) ).append("]]>").append("</potency_perc>\r\n");
							valueXmlString.append("<no_art>").append("<![CDATA[").append(noArt).append("]]>").append("</no_art>\r\n");
							valueXmlString.append("<batch_no>").append("<![CDATA[").append( ( (rs1.getString(13) == null) ? "":rs1.getString(13) ) ).append("]]>").append("</batch_no>\r\n");
							valueXmlString.append("<grade>").append("<![CDATA[").append( ( (rs1.getString(14) == null) ? "": rs1.getString(14) ) ).append("]]>").append("</grade>\r\n");
						//	valueXmlString.append("<lot_no>").append("<![CDATA[").append(( (rs1.getString(1) == null) ? "               ": rs1.getString(1))).append("]]>").append("</lot_no>\r\n");

								detail2stock = xmldetail2stock.toString();
							xmldetail2stock = null;

							valueXmlString.append(xmldetail2hdr.toString());
							valueXmlString.append(detail2stock);
							valueXmlString.append(xmldetail2ftr.toString());
							System.out.println("xmlString detail2......" + valueXmlString.toString());
							noArt1 = 0;
							grossWt = 0;
							tareWt = 0;
							netWt = 0;
							//cnt++;
							//System.out.println("The cnt :" + ++cnt);
						}//while end
						// added 18/06/09 manoharan
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
						sql = null;
						// end added 18/06/09 manoharan
						if ( isRecordFound == false )
						{

							//System.out.println("record not found.....................");
							//errCode = "VTDIST16";
							//errString = itmDBAccessEJB.getErrorString("",errCode,"","",conn);
							//return errString;
						}
						//}

					}
					pstmt.close();
					rs.close();
					pstmt = null;
					rs = null;
							
					 
					valueXmlString.append("</Detail3>");
				}*/
				else if(currentColumn.trim().equalsIgnoreCase("lot_sl"))
				{
					System.out.println(">>>>START>>>>");
					distOrderNo = genericUtility.getColumnValue("distord_issno", dom2);
					lotSl = genericUtility.getColumnValue("lot_sl",dom);
					siteCode = genericUtility.getColumnValue("site_code", dom2,"2");
					System.out.println("<<<sdsdsddS"+siteCode);

					String currDomStr = genericUtility.serializeDom(dom);
					if(lotSl != null && lotSl.trim().length() > 0)
					{
						//item_code,no_art,quantity,amount,net_amt,discount,tax_amt,gross_weight,tare_weight,net_weight
						sql = "select LOC_CODE,LOT_NO,ITEM_CODE,QUANTITY,GROSS_WEIGHT,NET_WEIGHT,TARE_WEIGHT,NO_ART,DIST_ORDER from distord_issdet where LOT_SL = ? and tran_id = ?";
						pstmt1=conn.prepareStatement(sql);
						pstmt1.setString(1,lotSl);
						pstmt1.setString(2,distOrderNo);

						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{
							locCode =  rs1.getString("loc_code")==null?"":rs1.getString("loc_code").trim();
							lotNo = rs1.getString("lot_no")==null?"":rs1.getString("lot_no").trim();
							itemCode = rs1.getString("item_code")==null?"":rs1.getString("item_code").trim();
							squantity = rs1.getDouble("quantity");
							grossWeight = rs1.getDouble("gross_weight");
							netWeight = rs1.getDouble("net_weight");
							tareWeight = rs1.getDouble("tare_weight");
							noOfArt = rs1.getDouble("no_art");
							distOrder = rs1.getString("DIST_ORDER");
							
                        }
						pstmt1.close();
						rs1.close();
						pstmt1 = null;
						rs1 = null;


						valueXmlString.append("<dist_order protect=\"0\">").append("<![CDATA[" + distOrder + "]]>").append("</dist_order>");
						setNodeValue( dom, "dist_order", getAbsString(""+distOrder)); 
						

						valueXmlString.append("<loc_code protect=\"0\">").append("<![CDATA[" + locCode + "]]>").append("</loc_code>");
						setNodeValue( dom, "loc_code", getAbsString(""+locCode)); 

						sql = "select descr from location where loc_code = ?";
						pstmt1=conn.prepareStatement(sql);
						pstmt1.setString(1,locCode);
						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{
							locdesc =  rs1.getString("descr")==null?"":rs1.getString("descr").trim();
						}
						pstmt1.close();
						rs1.close();
						pstmt1 = null;
						rs1 = null;



						valueXmlString.append("<location_descr protect=\"0\">").append("<![CDATA[" + locdesc + "]]>").append("</location_descr>");
						setNodeValue( dom, "location_descr", getAbsString(""+locdesc)); 

						valueXmlString.append("<lot_no protect=\"0\">").append("<![CDATA[" + lotNo + "]]>").append("</lot_no>");
						setNodeValue( dom, "lot_no", getAbsString(""+lotNo)); 

						sql = "select descr from item where item_code = ?";
						pstmt1=conn.prepareStatement(sql);
						pstmt1.setString(1,itemCode);
						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{
							itmdesc =  rs1.getString("descr")==null?"":rs1.getString("descr").trim();
						}
						pstmt1.close();
						rs1.close();
						pstmt1 = null;
						rs1 = null;


						valueXmlString.append("<item_descr protect=\"0\">").append("<![CDATA[" + itmdesc + "]]>").append("</item_descr>");
						setNodeValue( dom, "item_descr", getAbsString(""+itmdesc)); 

						valueXmlString.append("<item_code protect=\"0\">").append("<![CDATA[" + itemCode + "]]>").append("</item_code>");
						setNodeValue( dom, "item_code", getAbsString(""+itemCode)); 

						valueXmlString.append("<quantity protect=\"0\">").append("<![CDATA[" + squantity + "]]>").append("</quantity>");
						setNodeValue( dom, "quantity", getAbsString(""+squantity)); 

						valueXmlString.append("<gross_weight protect=\"0\">").append("<![CDATA[" + grossWeight + "]]>").append("</gross_weight>");
						setNodeValue( dom, "gross_weight", getAbsString(""+grossWeight)); 
						
						valueXmlString.append("<net_weight protect=\"0\">").append("<![CDATA[" + netWeight + "]]>").append("</net_weight>");
						setNodeValue( dom, "net_weight", getAbsString(""+netWeight)); 
						
						valueXmlString.append("<tare_weight protect=\"0\">").append("<![CDATA[" + tareWeight + "]]>").append("</tare_weight>");
						setNodeValue( dom, "tare_weight", getAbsString(""+tareWeight)); 
						
						valueXmlString.append("<no_art protect=\"0\">").append("<![CDATA[" + noOfArt + "]]>").append("</no_art>");
						setNodeValue( dom, "no_art", getAbsString(""+noOfArt)); 

						
						currDomStr = currDomStr.replace("</Detail3>", valueXmlString.toString() + "</Detail3>");
						System.out.println("after currDomStr[" + currDomStr + "]");
						valueXmlString.append(currDomStr);
					}
					else
					{
						valueXmlString.append("<dist_order protect=\"0\">").append("<![CDATA[]]>").append("</dist_order>");
						setNodeValue( dom, "dist_order", getAbsString("")); 
						

						
						valueXmlString.append("<loc_code protect=\"0\">").append("<![CDATA[]]>").append("</loc_code>");
						setNodeValue( dom, "loc_code", getAbsString("")); 

						valueXmlString.append("<location_descr protect=\"0\">").append("<![CDATA[]]>").append("</location_descr>");
						setNodeValue( dom, "location_descr", getAbsString("")); 

						valueXmlString.append("<lot_no protect=\"0\">").append("<![CDATA[]]>").append("</lot_no>");
						setNodeValue( dom, "lot_no", getAbsString("")); 

						valueXmlString.append("<item_descr protect=\"0\">").append("<![CDATA[]]>").append("</item_descr>");
						setNodeValue( dom, "item_descr", getAbsString("")); 

						valueXmlString.append("<item_code protect=\"0\">").append("<![CDATA[]]>").append("</item_code>");
						setNodeValue( dom, "item_code", getAbsString("")); 

						valueXmlString.append("<quantity protect=\"0\">").append("<![CDATA[0]]>").append("</quantity>");
						setNodeValue( dom, "quantity", getAbsString("0")); 

						valueXmlString.append("<gross_weight protect=\"0\">").append("<![CDATA[]]>").append("</gross_weight>");
						setNodeValue( dom, "gross_weight", getAbsString("")); 

						valueXmlString.append("<net_weight protect=\"0\">").append("<![CDATA[]]>").append("</net_weight>");
						setNodeValue( dom, "net_weight", getAbsString("")); 
						
						valueXmlString.append("<tare_weight protect=\"0\">").append("<![CDATA[]]>").append("</tare_weight>");
						setNodeValue( dom, "tare_weight", getAbsString("")); 
						
						valueXmlString.append("<no_art protect=\"0\">").append("<![CDATA[]]>").append("</no_art>");
						setNodeValue( dom, "no_art", getAbsString("")); 
						
						currDomStr = currDomStr.replace("</Detail3>", valueXmlString.toString() + "</Detail3>");
						System.out.println("after currDomStr[" + currDomStr + "]");
						valueXmlString.append(currDomStr);

					}


					System.out.println(">>>>END>>>>>>>>>");
				}


				break;
			}

			if(("lot_sl".equalsIgnoreCase(currentColumn)))
			{
				System.out.println("CHK VAL");
				String currDomStr = genericUtility.serializeDom(dom);
				System.out.println("currDomStr[" + currDomStr + "]");
				StringBuffer valueXmlStr = new StringBuffer(currDomStr);
				System.out.println("@@@@@@@@@@@ after serialize : valueXmlStr ["+valueXmlStr+"]");
				StringBuffer valueXmlString1 = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
				valueXmlString1.append(editFlag).append("</editFlag></header>");
				valueXmlString1.append(valueXmlStr);
				valueXmlString = valueXmlString1;
			}

			valueXmlString.append("</Root>"); 
		}
		catch(Exception e) 
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage()); 
			try {
				conn.rollback();				
			} catch (Exception d) {
				d.printStackTrace();
			}
			throw new ITMException(e); 
		}
		finally 
		{
			try
			{
				if(conn != null)
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
				}
				conn = null;
			}
			catch(Exception d)
			{
				d.printStackTrace(); 
			}
		}
		return valueXmlString.toString();
	}

	private String getDetails(String mSiteCode,int mLineNoDist,String mDistOrder,String lsTranType,Connection conn)throws RemoteException,ITMException
	{//this method will return xml data
		String mItemCode = null,mTaxClass=null,mTaxChap=null,mTaxEnv = null;
		String lsUnitAlt = null,lsUnit = null,lsPackInstr =null;
		double mQty= 0,lcQty= 0,mRate = 0,mDiscount =0;

		String sql = null,lsTranTypeParent ="";
		ResultSet rs = null,rs1 =null;
		PreparedStatement pstmt = null,pstmt1 =null;
		StringBuffer detail2hdr = new StringBuffer("");
		try
		{
			sql="select item_code,((case when qty_confirm is null then 0 else qty_confirm end) - "
					+" (case when qty_shipped is null then 0 else qty_shipped end))	as qty,"
					+" ((case when qty_shipped is null then 0 else qty_shipped end) - "
					+" (case when qty_return is null then 0 else qty_return end)) as lcqty,"
					+" tax_class,tax_chap,tax_env,case when rate is null then 0 else rate end as rate,"
					+" case when discount is null then 0 else discount end as discount,	"
					+" rate__clg  ,UNIT__ALT ,UNIT,CONV__QTY__ALT,pack_instr "
					+" from 	distorder_det "
					+" where dist_order = '"+mDistOrder+"'  "
					+" and 	line_no    = "+mLineNoDist+"" 
					+ " AND   CASE WHEN STATUS IS NULL THEN 'O' ELSE STATUS END<>'C' ";//Added by manoj dtd 24/12/2013 to exclude closed line"

			//System.out.println("[DistIssueItemChangeEJB] sql=>"+sql);

			pstmt   = conn.prepareStatement(sql);
			//	pstmt.setString(1,mDistOrder);
			//pstmt.setInt(2,mLineNoDist);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				mItemCode = rs.getString("item_code")==null?"":rs.getString("item_code");
				mQty = rs.getDouble("qty");
				lcQty = rs.getDouble("lcqty");
				mTaxClass = rs.getString("tax_class")==null?"":rs.getString("tax_class");
				mTaxChap = rs.getString("tax_chap")==null?"":rs.getString("tax_chap");
				mTaxEnv = rs.getString("tax_env")==null?"":rs.getString("tax_env");
				mRate = rs.getDouble("rate");
				mDiscount = rs.getDouble("discount");
				lsUnitAlt = rs.getString("UNIT__ALT")==null?"":rs.getString("UNIT__ALT");
				lsPackInstr = rs.getString("pack_instr")==null?"":rs.getString("pack_instr");

				if(lsUnitAlt.trim().length() == 0)
				{
					lsUnitAlt = lsUnit;
				}

			//	detail2hdr.append("<item_code><![CDATA["+mItemCode+"]]></item_code>");
				detail2hdr.append("<unit><![CDATA["+lsUnit+"]]></unit>");
				detail2hdr.append("<unit__alt><![CDATA["+lsUnitAlt+"]]></unit__alt>");
				detail2hdr.append("<pack_instr><![CDATA["+lsPackInstr+"]]></pack_instr>");


				sql=" select tran_type__parent "
						+" from	distorder_type where  tran_type = '"+lsTranType+"' ";
				//System.out.println("[DistIssueItemChangeEJB] sql=>"+sql);
				pstmt1= conn.prepareStatement(sql);
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{
					lsTranTypeParent = rs1.getString("tran_type__parent") == null ? "": rs1.getString("tran_type__parent").trim();
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;

				if(! lsTranTypeParent.equalsIgnoreCase(lsTranType.trim()))
				{
				//	detail2hdr.append("<quantity>"+lcQty+"</quantity>");
					detail2hdr.append("<qty_order__alt>"+lcQty+"</qty_order__alt>");
					lcQty = lcQty;
				}
				else
				{
				//	detail2hdr.append("<quantity>"+mQty+"</quantity>");
					detail2hdr.append("<qty_order__alt>"+mQty+"</qty_order__alt>");
					lcQty = mQty;
				}
				detail2hdr.append("<tax_class><![CDATA["+mTaxClass+"]]></tax_class>");
				detail2hdr.append("<tax_chap><![CDATA["+mTaxChap+"]]></tax_chap>");
				detail2hdr.append("<tax_env><![CDATA["+mTaxEnv+"]]></tax_env>");
				detail2hdr.append("<rate>"+mRate+"</rate>");
				detail2hdr.append("<discount>"+mDiscount+"</discount>");
				//System.out.println("[CreateDistIssue] xml return ==>"+detail2hdr.toString());
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		System.out.println("chandni inside detail::"+detail2hdr.toString());
		return detail2hdr.toString();
	}
	
	private static String getAbsString( String str )
	{
		return ( str == null || str.trim().length() == 0 || "null".equalsIgnoreCase( str.trim() ) ? "" : str.trim() );
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


	
	public String getFormatedValue(double actVal,int prec)throws RemoteException//This method is added by nisar on 11/23/2007
	{//this method is used to return double with appropriate precison
		NumberFormat numberFormat = NumberFormat.getIntegerInstance ();
		Double DoubleValue = new Double (actVal);
		numberFormat.setMaximumFractionDigits(prec);
		String strValue = numberFormat.format(DoubleValue);
		//System.out.println(strValue);
		strValue = strValue.replaceAll(",","");
		return strValue;
	}
	private String getCurrdateAppFormat() throws ITMException
	{
		String s = "";
		//GenericUtility genericUtility = GenericUtility.getInstance();
		try
		{
			java.util.Date date = null;
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			//System.out.println(genericUtility.getDBDateFormat());

			SimpleDateFormat simpledateformat = new SimpleDateFormat(genericUtility.getDBDateFormat());
			date = simpledateformat.parse(timestamp.toString());
			timestamp = Timestamp.valueOf(simpledateformat.format(date).toString() + " 00:00:00.0");
			s = (new SimpleDateFormat(genericUtility.getApplDateFormat())).format(timestamp).toString();
		}
		catch(Exception exception)
		{
			//System.out.println("Exception in [MPSOrder] getCurrdateAppFormat " + exception.getMessage());
			throw new ITMException(exception); //Added By Mukesh Chauhan on 07/08/19
		}
		return s;
	}
	
	private String calcExpiry(String tranDate, int months) throws ITMException
	{
		java.util.Date expDate = new java.util.Date();
		java.util.Date retDate = new java.util.Date();
		String retStrInDate = "";
		//System.out.println("tranDate :"+tranDate+"\nmonths :"+months);
		try
		{
			//GenericUtility genericUtility = GenericUtility.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			if (months > 0)
			{
				Calendar  cal = Calendar.getInstance();
				expDate = sdf.parse(tranDate);
				//System.out.println("expDate :"+expDate);
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
			//System.out.println("The Exception occurs in calcExpiry :"+e);
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		//System.out.println("retStrInDate :"+retStrInDate);
		return retStrInDate;
	}
	private double getIntegralQty(String siteCode, String itemCode, String lotNo, String packCode, String checkIntegralQty) throws ITMException
	{
		double integralQty = 0;
		String sql = "";
		ResultSet rs = null;
		Connection conn = null;
		ConnDriver connDriver = new ConnDriver();
		Statement stmt = null;
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			stmt = conn.createStatement();
			char type = checkIntegralQty.charAt(0);
			//System.out.println("type==>"+type);
			switch (type)
			{
			case 'S':
				sql ="SELECT CASE WHEN SHIPPER_SIZE IS NULL THEN 0 ELSE SHIPPER_SIZE END "
						+"FROM ITEM_LOT_PACKSIZE "
						+"WHERE ITEM_CODE = '"+itemCode+"' "
						+"AND LOT_NO__FROM <= '"+lotNo+"' "
						+"AND LOT_NO__TO   >= '"+lotNo+"' ";
				System.out.println("sql :"+sql);
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					integralQty = rs.getDouble(1);
					//System.out.println("integralQty :"+integralQty);
				}
				if (integralQty == 0)
				{
					sql = "SELECT CASE WHEN CAPACITY IS NULL THEN 0 ELSE CAPACITY END "
							+"FROM PACKING WHERE PACK_CODE = '"+packCode+"'";
					System.out.println("sql :"+sql);
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						integralQty = rs.getDouble(1);
						//System.out.println("integralQty :"+integralQty);
					}
					if (integralQty == 0)
					{
						sql = "SELECT REO_QTY FROM SITEITEM "
								+"WHERE SITE_CODE = '"+siteCode+"'"+" AND ITEM_CODE = '"+itemCode+"'";
						System.out.println("sql :"+sql);	
						rs = stmt.executeQuery(sql);
						if (rs.next())
						{
							integralQty = rs.getDouble(1);
							//System.out.println("integralQty :"+integralQty);
						}
						if (integralQty == 0)
						{
							sql = "SELECT REO_QTY FROM ITEM "
									+"WHERE ITEM_CODE = '"+itemCode+"'";
							System.out.println("sql :"+sql);	
							rs = stmt.executeQuery(sql);
							if (rs.next())
							{
								integralQty = rs.getDouble(1);
								//System.out.println("integralQty :"+integralQty);
							}
						}
					}
				}

				break;
			case 'P':
				sql = "SELECT CASE WHEN CAPACITY IS NULL THEN 0 ELSE CAPACITY END "
						+"FROM PACKING WHERE PACK_CODE = '"+packCode+"'";
				System.out.println("sql :"+sql);
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					integralQty = rs.getDouble(1);
					//System.out.println("integralQty :"+integralQty);
				}

				break;
			case 'I':
				sql = "SELECT INTEGRAL_QTY FROM SITEITEM "
						+"WHERE SITE_CODE = '"+siteCode+"'"+" AND ITEM_CODE = '"+itemCode+"'";
				System.out.println("sql :"+sql);	
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					integralQty = rs.getDouble(1);
					//System.out.println("integralQty :"+integralQty);
				}
				if (integralQty == 0)
				{
					sql = "SELECT INTEGRAL_QTY FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
					System.out.println("sql :"+sql);
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						integralQty = rs.getDouble(1);
						//System.out.println("integralQty :"+integralQty);
					}
				}

			}
			/* sql ="SELECT CASE WHEN SHIPPER_SIZE IS NULL THEN 0 ELSE SHIPPER_SIZE END "
				+"FROM ITEM_LOT_PACKSIZE "
				+"WHERE ITEM_CODE = '"+itemCode+"' "
				+"AND LOT_NO__FROM <= '"+lotNo+"' "
				+"AND LOT_NO__TO   >= '"+lotNo+"' ";
			System.out.println("sql :"+sql);
			rs = stmt.executeQuery(sql);
			if (rs.next())
			{
				integralQty = rs.getDouble(1);
				System.out.println("integralQty :"+integralQty);
			}
			if (integralQty == 0)
			{
				sql = "SELECT CASE WHEN CAPACITY IS NULL THEN 0 ELSE CAPACITY END "
					 +"FROM PACKING WHERE PACK_CODE = '"+packCode+"'";
				System.out.println("sql :"+sql);
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					integralQty = rs.getDouble(1);
					System.out.println("integralQty :"+integralQty);
				}
				if (integralQty == 0)
				{
					sql = "SELECT INTEGRAL_QTY FROM SITEITEM "
						 +"WHERE SITE_CODE = '"+siteCode+"'"+" AND ITEM_CODE = '"+itemCode+"'";
					System.out.println("sql :"+sql);	
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						integralQty = rs.getDouble(1);
						System.out.println("integralQty :"+integralQty);
					}
					if (integralQty == 0)
					{
						sql = "SELECT INTEGRAL_QTY FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
						System.out.println("sql :"+sql);
						rs = stmt.executeQuery(sql);
						if (rs.next())
						{
							 integralQty = rs.getDouble(1);
							System.out.println("integralQty :"+integralQty);
						}
					}
				}
			} */
			conn.close();	
		}
		catch(Exception e)
		{
			System.out.println("the exception occurs in getIntegralQty :"+e);
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		System.out.println("integralQty :"+integralQty);
		return integralQty;
	}

	private int getNoArt(String siteCode, String custCode, String itemCode, String packCode, double qty, char type, double shipperQty, double integralQty1) throws ITMException
	{
		String sql = "";
		ResultSet rs = null;
		Connection conn = null;
		ConnDriver connDriver = new ConnDriver();
		Statement stmt = null;
		double reoQty = 0d, capacity = 0d, integralQty = 0d, mod = 0d, noArt3 = 0d;
		double noArt = 0, noArt1 = 0, noArt2 = 0; 
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			stmt = conn.createStatement();
			System.out.println("type :"+type);
			switch (type)
			{
			case 'S':
				sql = "SELECT CASE WHEN CAPACITY IS NULL THEN 0 ELSE CAPACITY END " 
						+"FROM PACKING WHERE PACK_CODE = '"+packCode+"'";
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					capacity = rs.getDouble(1);
					//System.out.println("capacity :"+capacity);
				}
				else
				{
					capacity = 0;
				}
				sql = "SELECT REO_QTY FROM SITEITEM WHERE SITE_CODE = '"+siteCode+"' " 
						+"AND ITEM_CODE = '"+itemCode+"'";
				System.out.println("sql :"+sql);
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					reoQty = rs.getDouble(1);
					//System.out.println("reoQty :"+reoQty);
				}
				if( reoQty == 0 )
				{
					sql = "SELECT REO_QTY FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
					System.out.println("sql :"+sql);
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						reoQty = rs.getDouble(1);
						//System.out.println("reoQty :"+reoQty);
					}
				}
				if (reoQty == 0)
				{
					reoQty = 0;
				}
				if (capacity > 0)
				{
					shipperQty = capacity;
					//System.out.println("shipperQty :"+shipperQty);
				}
				else
				{
					shipperQty = reoQty;
					//System.out.println("shipperQty :"+shipperQty);
				}
				System.out.println("shipperQty :"+shipperQty);
				if (shipperQty > 0)
				{
					mod = qty%shipperQty;
					System.out.println("mod :"+mod);
					noArt = (qty - mod) / shipperQty;
				}
				//System.out.println("noArt :"+noArt);
				break;
			case 'I':
				sql = "SELECT INTEGRAL_QTY FROM CUSTOMERITEM "
						+"WHERE CUST_CODE = '"+custCode+"'"+" AND ITEM_CODE = '"+itemCode+"'";
				System.out.println("sql :"+sql);
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					integralQty = rs.getDouble(1);
					//System.out.println("integralQty :"+integralQty);
				}
				if (integralQty == 0)
				{
					sql = "SELECT INTEGRAL_QTY FROM SITEITEM "
							+"WHERE SITE_CODE = '"+siteCode+"'"+" AND ITEM_CODE = '"+itemCode+"'";
					System.out.println("sql :"+sql);
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						integralQty = rs.getDouble(1);
						//System.out.println("integralQty :"+integralQty);
					}
					if (integralQty == 0)
					{
						sql = "SELECT INTEGRAL_QTY FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
						System.out.println("sql :"+sql);
						rs = stmt.executeQuery(sql);
						if (rs.next())
						{
							integralQty = rs.getDouble(1);
							//System.out.println("integralQty :"+integralQty);
						}
					}
				}
				if (integralQty > 0)
				{
					mod = qty%integralQty;
					System.out.println("mod :"+mod);
					noArt = (qty - mod) / integralQty;
					//System.out.println("noArt :"+noArt);
				}
				break;
			case 'B' :
				sql = "SELECT CASE WHEN CAPACITY IS NULL THEN 0 ELSE CAPACITY END " 
						+"FROM PACKING WHERE PACK_CODE = '"+packCode+"'";
				System.out.println("sql :"+sql);
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					capacity = rs.getDouble(1);
					//System.out.println("capacity :"+capacity);
				}
				else
				{
					capacity = 0;
				}
				sql = "SELECT REO_QTY FROM SITEITEM WHERE SITE_CODE = '"+siteCode+"' " 
						+"AND ITEM_CODE = '"+itemCode+"'";
				System.out.println("sql :"+sql);
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					reoQty = rs.getDouble(1);
					//System.out.println("reoQty :"+reoQty);
				}
				if( reoQty == 0 )
				{
					sql = "SELECT REO_QTY FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
					System.out.println("sql :"+sql);
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						reoQty = rs.getDouble(1);
						//System.out.println("reoQty :"+reoQty);
					}
				}
				if (capacity > 0)
				{
					shipperQty = capacity;
					//System.out.println("shipperQty :"+shipperQty);
				}
				else
				{
					shipperQty = reoQty;
					//System.out.println("shipperQty :"+shipperQty);
				}
				if (shipperQty > 0)
				{
					mod = (qty % shipperQty);
					noArt1 = (qty - mod) / shipperQty;
				}
				sql = "SELECT INTEGRAL_QTY FROM CUSTOMERITEM "
						+"WHERE CUST_CODE = '"+custCode+"' "
						+"AND ITEM_CODE = '"+itemCode+"'";
				System.out.println("sql :"+sql);
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					integralQty = rs.getDouble(1);
					//System.out.println("integralQty :"+integralQty);
				}
				if (integralQty == 0)
				{
					sql ="SELECT INTEGRAL_QTY FROM SITEITEM "
							+"WHERE SITE_CODE = '"+siteCode+"' " 
							+"AND ITEM_CODE = '"+itemCode+"'";
					System.out.println("sql :"+sql);
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						integralQty = rs.getDouble(1);
						//System.out.println("integralQty :"+integralQty);
					}
					if(integralQty == 0)
					{
						sql = "SELECT INTEGRAL_QTY FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
						System.out.println("sql :"+sql);
						rs = stmt.executeQuery(sql);
						if (rs.next())
						{
							integralQty = rs.getDouble(1);
							//System.out.println("integralQty :"+integralQty);
						}
					}
				}
				double remainder1 = 0d;
				if (integralQty > 0)
				{
					remainder1 = mod % integralQty;
					System.out.println("remainder1 :"+remainder1);
					noArt3 =(mod - remainder1) / integralQty;
					noArt2 = (int)noArt3;
				}
				if (noArt2 > 0)
				{
					noArt2 = 1;
				}
				noArt  = noArt1 + noArt2;
				System.out.println("noArt :"+noArt);
			}
			conn.close();
			if (noArt == 0)
			{
				noArt = 0;
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception occures in getNoArt :"+e);
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		System.out.println("(int)noArt :"+(int)noArt);
		return (int)noArt;
	}
}
