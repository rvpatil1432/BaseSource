/* Name of Developer : Nisar S. Khatib */
package ibase.webitm.ejb.dis;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import org.xml.sax.InputSource;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.Date;
import java.sql.*;
import java.text.NumberFormat;
import java.io.*;
import org.omg.CORBA.ORB;
import org.w3c.dom.*;
import java.util.Properties;
import javax.xml.parsers.*;
import javax.ejb.*;
import javax.naming.InitialContext;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.webitm.utility.*;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.ejb.*;
import ibase.system.config.*;
import java.text.SimpleDateFormat;
import java.util.*;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.dis.adv.DistIssueConfirmAct;

public class CreateDistIssue
{
	CreateDistOrder distOrder = new CreateDistOrder();
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility = new E12GenericUtility();
	String currCode = null;
	String isAutoConfirm = null;
	
	public CreateDistIssue()
	{
	}
	 //added createDistIssue part by ManojSharma for adding logic for clubbing dist_order Req.Id DI1ISUN005
	public String createDistIssue(String siteCode,ArrayList clubList,String xtraParams, Connection conn)throws RemoteException,ITMException
	{
		StringBuffer xmlBuff = new StringBuffer();

		String xmldetail1 =null;
		StringBuffer detail2xml = new StringBuffer();
		StringBuffer xmldetail2ftr = new StringBuffer();
		xmldetail2ftr.append("</Detail2>");
		String xmlString="";
		String distOrderNo = null;
		String sql = null;
		String tranDate = null;
		String locCode = null;
		String retString = "";
		PreparedStatement pstmt = null,pstmt1 = null,pstmt2 =null;
		ResultSet rs = null,rs1 =null,rs2 = null,rs3 = null;
		Statement stmt1 = null;
		//Connection conn = null;
		String lsPackCode = null,unitAlt = null,unit = null,mLotNo = null;
		int count = 0;
		int linenoCtr = 0;
		String 	tranType ="";
		double mod = 0d, minputQty = 0d, remQuantity = 0d, stockQty = 0d, integralQty = 0d;
		double grossPer = 0d,netPer = 0d,tarePer = 0d, grossWt = 0d, netWt = 0d, tareWt =0d, rateClgVal = 0d, rate2 = 0d;
		double disAmount = 0d, amount = 0d, shipperQty = 0d,discount =0;
		int  minShelfLife = 0, noArt1 = 0, cnt = 0;
		int mLineNoDist =0;
		double qtyConfirm =0,qtyShipped =0,lcQtyOrderAlt =0,lcFact =0,stkRate=0;
		String suppSour = "", trackShelfLife = "", siteCodeMfg = "", sundryCode = "", potencyPerc = "";
		String priceList = "", tabValue = "", priceListClg = "", chkDate = "", disCountPer = "";
		String tranTypePparent = null,rate ="",lsTranTypeParent ="";
		String qtyOrdAlt = "",convQtyAlt = "";
		String res = "", locCodeDamaged = "",itemCode ="",availableYn ="";
		String checkIntegralQty = "", rate1 = "",tranTypeParent ="",quantity = "";
		String active = "", itemDescr = "",errCode ="",sql2 ="",noArt ="";
		String detail2stock ="",errString ="",taxChap ="",siteCodeShip ="";
		String lotNo ="",lotSl ="",packCode ="",rateClg ="",taxEnv ="",taxClass ="";
		java.util.Date expDate = null,mfgDate = null, chkDate1 = null;
		boolean isDetFound = false;
		String prvDeptCode = null,deptCode ="";
		StringBuffer xmldetail2stock = null;
		int MAXLINESALLOWED = 0;
		int countDtl=0;
		String locGroupJwiss="";
		String subSQL="";
		String rateClgStr="",rateFmDistOrd="",issCriteria="";
		
		try
		{


			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			if (conn == null)
			{
				ConnDriver connDriver = new ConnDriver();
				conn = connDriver.getConnectDB("DriverITM");
				connDriver = null;
				conn.setAutoCommit( false ); //  added 18/06/09 manoharan
			}
			ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
			DistCommon disCommon = new DistCommon();
			/*ByteArrayInputStream bais = new ByteArrayInputStream(xmlString.getBytes());
			Document dom = (DocumentBuilderFactory.newInstance()).newDocumentBuilder().parse(bais);
			NodeList parent = dom.getElementsByTagName("TranID");
			distOrderNo = parent.item(0).getFirstChild().getNodeValue();*/
			tranDate = getCurrdateAppFormat() ;
			System.out.println("[CreateDistIssue]------------Distribution Issue is created for Dist Order------------->"+distOrderNo);

			isAutoConfirm = disCommon.getDisparams( "999999", "AUTO_DIST_ISS_CONFIRM", conn );
			
			if( isAutoConfirm != null && isAutoConfirm.trim().length() > 0 )
			{
				isAutoConfirm = isAutoConfirm.substring( 0, 1 );
			}
			else 
			{
				isAutoConfirm = "N";
			}
			//msalam on 230609 as per kb
			String maxLinesStr = disCommon.getDisparams( "999999", "MAX_LINES_DIST_ISS", conn );
			if (maxLinesStr == null || maxLinesStr.trim().length() == 0 || maxLinesStr.equals("NULLFOUND"))
			{
				maxLinesStr = "600";
			}
			MAXLINESALLOWED = Integer.parseInt( maxLinesStr.trim() );
			maxLinesStr = null;
			//msalam on 230609 as per kb end
			/*
			sql="SELECT D.DIST_ORDER AS DIST_ORDER,D.ORDER_DATE AS ORDER_DATE,D.SITE_CODE__SHIP AS SITE_CODE__SHIP,D.SITE_CODE__DLV AS SITE_CODE__DLV,D.SHIP_DATE AS SHIP_DATE,D.DUE_DATE AS DUE_DATE,D.REMARKS AS REMARKS,"
			+" D.DIST_ROUTE AS DIST_ROUTE,D.PRICE_LIST AS PRICE_LIST,D.CONFIRMED AS CONFIRMED,D.CHG_USER AS CHG_USER,D.CHG_TERM AS CHG_TERM,D.TARGET_WGT AS TARGET_WGT,D.TARGET_VOL AS TARGET_VOL,D.LOC_CODE__GIT AS LOC_CODE__GIT,"
			+" D.CHG_DATE AS CHG_DATE,SITE_A.DESCR AS SITEA_DESCR,SITE_B.DESCR AS SITEB_DESCR,LOCATION.DESCR AS LOCATION_DESCR,D.CONF_DATE AS CONF_DATE,D.SITE_CODE AS SITE_CODE,D.STATUS AS STATUS,D.SALE_ORDER AS SALE_ORDER,"
			+" D.REMARKS1 AS REMARK1,D.REMARKS2 AS REMARK2,TRIM(D.ORDER_TYPE) AS ORDER_TYPE,SITE_A.ADD1 AS SITEA_ADD1,SITE_A.ADD2 AS SITEA_ADD2,SITE_A.CITY AS SITEA_CITY,SITE_A.PIN AS SITEA_PIN,SITE_A.STATE_CODE AS SITEA_STATE_CODE,"
			+" SITE_B.ADD1 AS SITEB_ADD1,SITE_B.ADD2 AS SITEB_ADD2,SITE_B.CITY AS SITEB_CITY,SITE_B.PIN AS SITEB_PIN,SITE_B.STATE_CODE AS SITEB_STATE_CODE,D.LOC_CODE__CONS AS LOC_CODE__CONS,D.SUNDRY_TYPE AS SUNDRY_TYPE,"
			+" D.SUNDRY_CODE AS SUNDRY_CODE,D.AUTO_RECEIPT AS AUTO_RECEIPT,D.TRAN_TYPE AS TRAN_TYPE,D.CURR_CODE AS CURR_CODE,D.EXCH_RATE AS EXCH_RATE,D.SALES_PERS AS SALES_PERS,SALES_PERS.SP_NAME AS SP_NAME,"
			+" D.LOC_CODE__GITBF AS LOC_CODE__GITBF,D.CUST_CODE__DLV AS CUST_CODE__DLV,D.DLV_TO AS DLV_TO,D.DLV_ADD1 AS DLV_ADD1,D.DLV_ADD2 AS DLV_ADD2,D.DLV_ADD3 AS DLV_ADD3,D.DLV_CITY AS DLV_CITY,"
			+" D.STATE_CODE__DLV AS STATE_CODE__DLV,D.COUNT_CODE__DLV AS COUNT_CODE__DLV,D.DLV_PIN AS DLV_PIN,D.STAN_CODE AS STAN_CODE,D.TEL1__DLV AS TEL1__DLV,D.TEL2__DLV AS TEL2__DLV,D.TEL3__DLV AS TEL3__DLV,"
			+" D.FAX__DLV AS FAX__DLV,D.AVALIABLE_YN AS AVALIABLE_YN,D.PURC_ORDER AS PURC_ORDER,D.TOT_AMT AS TOT_AMT,D.TAX_AMT AS TAX_AMT,D.NET_AMT AS NET_AMT,D.TRAN_SER AS TRAN_SER,"
			+" D.PRICE_LIST__CLG AS PRICE_LIST__CLG,SPACE(25) AS LOC,FN_SUNDRY_NAME(D.SUNDRY_TYPE,D.SUNDRY_CODE,'N') AS SUNDRY_NAME,"
			+" D.PROJ_CODE AS PROJ_CODE,SITE_C.DESCR AS SITEC_DESCR,D.POLICY_NO AS POLICY_NO,D.LOC_CODE__DAMAGED AS LOC_CODE__DAMAGED,D.SITE_CODE__BIL AS SITE_CODE__BIL,SITE_D.DESCR AS SITED_DESCR,SITE_D.ADD1 AS SITED_ADD1,"
			+" SITE_D.ADD2 AS SITED_ADD2,SITE_D.CITY AS SITED_CITY,SITE_D.PIN SITED_PIN ,SITE_D.STATE_CODE AS SITED_STATE_CODE,D.TRANS_MODE AS TRANS_MODE"
			+"  FROM DISTORDER  D,SITE SITE_A,SITE SITE_B,LOCATION  LOCATION,SALES_PERS  SALES_PERS,SITE SITE_C,SITE SITE_D "
			+"  WHERE ( D.SITE_CODE__SHIP      = SITE_A.SITE_CODE  ) AND "
			+" ( D.SITE_CODE__DLV      = SITE_B.SITE_CODE (+)  ) AND "
			+" ( D.LOC_CODE__GIT      = LOCATION.LOC_CODE (+)  ) AND "
			+" ( D.SITE_CODE      = SITE_C.SITE_CODE (+)  ) AND "
			+" ( D.SALES_PERS=SALES_PERS.SALES_PERS(+)) AND "
			+" ( D.SITE_CODE__BIL=SITE_D.SITE_CODE(+)) "
			+"  AND ( ( D.DIST_ORDER    = '"+distOrderNo+"' ) ) ";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				this.currCode = rs.getString("CURR_CODE");
				locCode = rs.getString("LOC_CODE__GIT");
				tranType = rs.getString("TRAN_TYPE");
				siteCodeShip = rs.getString("SITE_CODE__SHIP");
				xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
				xmlBuff.append("<DocumentRoot>");
				xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
				xmlBuff.append("<group0>");
				xmlBuff.append("<description>").append("Group0 description").append("</description>");
				xmlBuff.append("<Header0>");
				xmlBuff.append("<objName><![CDATA[").append("dist_issue").append("]]></objName>");
				xmlBuff.append("<pageContext><![CDATA[").append("1").append("]]></pageContext>");
				xmlBuff.append("<objContext><![CDATA[").append("1").append("]]></objContext>");
				xmlBuff.append("<editFlag><![CDATA[").append("A").append("]]></editFlag>");
				xmlBuff.append("<focusedColumn><![CDATA[").append("").append("]]></focusedColumn>");
				xmlBuff.append("<action><![CDATA[").append("SAVE").append("]]></action>");
				xmlBuff.append("<elementName><![CDATA[").append("").append("]]></elementName>");
				xmlBuff.append("<keyValue><![CDATA[").append("1").append("]]></keyValue>");
				xmlBuff.append("<taxKeyValue><![CDATA[").append("").append("]]></taxKeyValue>");
				xmlBuff.append("<saveLevel><![CDATA[").append("1").append("]]></saveLevel>");
				xmlBuff.append("<forcedSave><![CDATA[").append(true).append("]]></forcedSave>");
				xmlBuff.append("<taxInFocus><![CDATA[").append(false).append("]]></taxInFocus>");
				xmlBuff.append("<description>").append("Header0 members").append("</description>");

				xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"dist_issue\" objContext=\"1\">");
				xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
				xmlBuff.append("<tran_id/>");
				//tranDate = sdf.format(new Timestamp(System.currentTimeMillis()));
				xmlBuff.append("<tran_date><![CDATA["+tranDate+"]]></tran_date>");
				xmlBuff.append("<eff_date><![CDATA["+tranDate+"]]></eff_date>");
				xmlBuff.append("<dist_order><![CDATA["+distOrderNo+"]]></dist_order>");
				//System.out.println("SITE_CODE__SHIP------------>"+rs.getString("SITE_CODE__SHIP"));
				xmlBuff.append("<site_code><![CDATA["+(rs.getString("SITE_CODE__SHIP")==null?"":rs.getString("SITE_CODE__SHIP").trim())+"]]></site_code>");
				xmlBuff.append("<site_code__dlv><![CDATA["+(rs.getString("SITE_CODE__DLV")==null?"":rs.getString("SITE_CODE__DLV").trim())+"]]></site_code__dlv>");
				xmlBuff.append("<dist_route><![CDATA["+(rs.getString("DIST_ROUTE")==null?"":rs.getString("DIST_ROUTE"))+"]]></dist_route>");
				xmlBuff.append("<tran_code><![CDATA[]]></tran_code>");
				xmlBuff.append("<lr_no><![CDATA[]]></lr_no>");
				xmlBuff.append("<lr_date><![CDATA[]]></lr_date>");
				xmlBuff.append("<lorry_no><![CDATA[]]></lorry_no>");
				xmlBuff.append("<gross_weight><![CDATA[0]]></gross_weight>");
				xmlBuff.append("<tare_weight><![CDATA[0]]></tare_weight>");
				xmlBuff.append("<net_weight><![CDATA[0]]></net_weight>");
				xmlBuff.append("<frt_amt><![CDATA[0]]></frt_amt>");
				xmlBuff.append("<amount><![CDATA[0]]></amount>");
				xmlBuff.append("<tax_amt><![CDATA[0]]></tax_amt>");
				xmlBuff.append("<net_amt><![CDATA[0]]></net_amt>");
				xmlBuff.append("<remarks><![CDATA[]]></remarks>");
				xmlBuff.append("<frt_type><![CDATA[T]]></frt_type>");
				xmlBuff.append("<chg_user><![CDATA["+(rs.getString("CHG_USER")==null?"":rs.getString("CHG_USER").trim())+"]]></chg_user>");
				xmlBuff.append("<chg_term><![CDATA["+(rs.getString("CHG_TERM")==null?"":rs.getString("CHG_TERM").trim())+"]]></chg_term>");
				xmlBuff.append("<curr_code><![CDATA[" + this.currCode + "]]></curr_code>");
				xmlBuff.append("<chg_date><![CDATA["+tranDate+"]]></chg_date>");
				xmlBuff.append("<site_descr><![CDATA["+(rs.getString("SITEA_DESCR")==null?"":rs.getString("SITEA_DESCR").trim())+"]]></site_descr>");
				xmlBuff.append("<site_to_descr><![CDATA["+(rs.getString("SITEB_DESCR")==null?"":rs.getString("SITEB_DESCR").trim())+"]]></site_to_descr>");
				xmlBuff.append("<location_descr><![CDATA["+(rs.getString("LOCATION_DESCR")==null?"":rs.getString("LOCATION_DESCR").trim())+"]]></location_descr>");
				xmlBuff.append("<tran_name><![CDATA[]]></tran_name>");
				xmlBuff.append("<currency_descr><![CDATA[]]></currency_descr>");
				xmlBuff.append("<confirmed><![CDATA[N]]></confirmed>");
				xmlBuff.append("<loc_code__git><![CDATA["+(rs.getString("LOC_CODE__GITBF")==null?"":rs.getString("LOC_CODE__GITBF"))+"]]></loc_code__git>");
				xmlBuff.append("<conf_date><![CDATA["+sdf.format(rs.getTimestamp("CONF_DATE"))+"]]></conf_date>");
				xmlBuff.append("<no_art><![CDATA[0]]></no_art>");
				xmlBuff.append("<trans_mode><![CDATA["+(rs.getString("TRANS_MODE")==null?"":rs.getString("TRANS_MODE").trim())+"]]></trans_mode>");
				xmlBuff.append("<gp_no><![CDATA[]]></gp_no>");
				xmlBuff.append("<gp_date/>");
				xmlBuff.append("<conf_passwd/>");

				xmlBuff.append("<order_type><![CDATA["+(rs.getString("ORDER_TYPE")==null?"":rs.getString("ORDER_TYPE").trim())+"]]></order_type>");
				xmlBuff.append("<gp_ser><![CDATA[I]]></gp_ser>");
				xmlBuff.append("<ref_no><![CDATA[]]></ref_no>");
				xmlBuff.append("<ref_date><![CDATA[]]></ref_date>");
				//xmlBuff.append("<available_yn><![CDATA[Y]]></available_yn>");
				xmlBuff.append("<available_yn><![CDATA["+(rs.getString("AVALIABLE_YN")==null?"N":rs.getString("AVALIABLE_YN").trim())+"]]></available_yn>");
				xmlBuff.append("<site_add1><![CDATA["+(rs.getString("SITEA_ADD1")==null?"":rs.getString("SITEA_ADD1").trim())+"]]></site_add1>");
				xmlBuff.append("<site_add2><![CDATA["+(rs.getString("SITEA_ADD2")==null?"":rs.getString("SITEA_ADD2").trim())+"]]></site_add2>");
				xmlBuff.append("<site_city><![CDATA["+(rs.getString("SITEA_CITY")==null?"":rs.getString("SITEA_CITY").trim())+"]]></site_city>");
				xmlBuff.append("<site_pin><![CDATA["+(rs.getString("SITEA_PIN")==null?"":rs.getString("SITEA_PIN").trim())+"]]></site_pin>");
				xmlBuff.append("<site_state_code><![CDATA["+(rs.getString("SITEA_STATE_CODE")==null?"":rs.getString("SITEA_STATE_CODE").trim())+"]]></site_state_code>");
				xmlBuff.append("<exch_rate><![CDATA["+(rs.getDouble("EXCH_RATE"))+"  ]]></exch_rate>");
				xmlBuff.append("<tran_type><![CDATA["+(rs.getString("TRAN_TYPE")==null?"":rs.getString("TRAN_TYPE").trim())+"]]></tran_type>");
				xmlBuff.append("<emp_code__aprv><![CDATA[]]></emp_code__aprv>");
				xmlBuff.append("<discount><![CDATA[0]]></discount>");
				xmlBuff.append("<permit_no><![CDATA[]]></permit_no>");
				xmlBuff.append("<shipment_id><![CDATA[]]></shipment_id>");
				xmlBuff.append("<curr_code__frt><![CDATA[" + this.currCode + "]]></curr_code__frt>");
				xmlBuff.append("<exch_rate__frt><![CDATA[]]></exch_rate__frt>");
				xmlBuff.append("<currency_descr__frt><![CDATA[]]></currency_descr__frt>");
				xmlBuff.append("<rd_permit_no><![CDATA[]]></rd_permit_no>");
				xmlBuff.append("<dc_no><![CDATA[]]></dc_no>");
				xmlBuff.append("<tran_ser><![CDATA[D-ISS ]]></tran_ser>");
				xmlBuff.append("<part_qty><![CDATA[A]]></part_qty>");
				xmlBuff.append("<sundry_details><![CDATA[]]></sundry_details>");
				xmlBuff.append("<sundry_name><![CDATA["+(rs.getString("SUNDRY_NAME")==null?"":rs.getString("SUNDRY_NAME"))+"]]></sundry_name>");
				xmlBuff.append("<proj_code><![CDATA["+(rs.getString("PROJ_CODE")==null?"":rs.getString("PROJ_CODE").trim())+"]]></proj_code>");
				xmlBuff.append("<site_tele1><![CDATA[]]></site_tele1>");
				xmlBuff.append("<site_tele2><![CDATA[]]></site_tele2>");
				xmlBuff.append("<site_tele3><![CDATA[]]></site_tele3>");
				xmlBuff.append("<site_code__bil><![CDATA[]]></site_code__bil>");
				xmlBuff.append("<site_descr_bill><![CDATA[]]></site_descr_bill>");
				xmlBuff.append("<site_add1_bill><![CDATA[]]></site_add1_bill>");
				xmlBuff.append("<site_add2_bill><![CDATA[]]></site_add2_bill>");
				xmlBuff.append("<site_city_bill><![CDATA[]]></site_city_bill>");
				xmlBuff.append("<site_pin_bill><![CDATA[]]></site_pin_bill>");
				xmlBuff.append("<site_state_code_bill><![CDATA[]]></site_state_code_bill>");
				xmlBuff.append("<pallet_wt><![CDATA[]]></pallet_wt>");
				xmlBuff.append("<auto_receipt><![CDATA[N]]></auto_receipt>");
				xmlBuff.append("</Detail1>");
			}//end of if(rs.next())
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;   */
			prvDeptCode = "NULL";
			detail2xml=new StringBuffer();
			countDtl=0;
			for(int ctr=0;ctr<clubList.size();ctr++)
			{
				DistOrderClubBean distOrdBean=(DistOrderClubBean) clubList.get(ctr);
				distOrderNo = distOrdBean.getDistOrdNo();
				siteCode=distOrdBean.getSiteCode();
				//added by msalam on 180609 to get tran_type from distorder start
				//tranType = rs.getString( "TRAN_TYPE" );
				tranType = distOrdBean.getTranType();
				//added by msalam on 180609 to get tran_type from distorder end 
				
				// 28/05/09 manoharan available_yn added
				//availableYn = rs.getString("AVALIABLE_YN");
				availableYn = distOrdBean.getAvailableYn();
				if( availableYn == null )
				{
					availableYn = "Y";
				}
				// end 28/05/09 manoharan available_yn added
				//deptCode = rs.getString("DEPT_CODE");
				deptCode=distOrdBean.getDeptCode();  
				if(prvDeptCode.equals("NULL"))
				{
					prvDeptCode = deptCode;
				}
				System.out.println( "prvDeptCode:" +prvDeptCode+"deptCode:"+deptCode );
				/*Commented by Manoj dtd 09/07/2013 not required as only header is created
				 if( ! prvDeptCode.equalsIgnoreCase(deptCode))
				{
					System.out.println( "different....................... " );
					prvDeptCode = deptCode;
					xmldetail1 = createHeader(siteCode, distOrderNo, xtraParams,  conn);
					xmlBuff   = null;
					xmlBuff = new StringBuffer();
					xmlBuff.append(xmldetail1.toString());
					xmlBuff.append(detail2xml.toString());
					xmlBuff.append("</Header0>");
					xmlBuff.append("</group0>");
					xmlBuff.append("</DocumentRoot>");
					xmlString = xmlBuff.toString();
					//System.out.println("xmlString final......" + xmlString);
					retString = distOrder.saveData(siteCode,xmlString,conn);
					if (retString.indexOf("Success") > -1)
					{
						int  d =retString.indexOf("<TranID>");
						int f =	 retString.indexOf("</TranID>");
						String TranIDDistIssue = retString.substring(d+8,f);
						//System.out.println("TranIDDistIssue......... "+TranIDDistIssue);
						//System.out.println("Distribution Issue Generated Successfully");
						linenoCtr = 0; //  23/06/09 added by manoharan 
					}
					else
					{
						//System.out.println("Distribution Issue not Generated ");
						//msalam on 220609 to show actual error coming form master stateful
						//retString="ERROR";
						//end msalam on 220609
					}
				}*/
				//if( prvDeptCode.equalsIgnoreCase( deptCode ) )
				//{
					System.out.println( "match dept....................... " );
					StringBuffer xmldetail2hdr = new StringBuffer();
							
					
					/* //xmldetail2hdr.append("<Detail2 dbID=\"\" domID=\"1\" objName=\"dist_issue\" objContext=\"2\">");
					//object name changed by msalam on 18/06/09 as per manoharan sir. start
					//from dist_issue to dist_issue_prc
					xmldetail2hdr.append("<Detail2 dbID=\"\" domID=\"1\" objName=\"dist_issue_prc\" objContext=\"2\">");
					//object name changed by msalam on 18/06/09 as per manoharan sir. end */
					
					//obj_name again changed to dist_issue on 03/02/10 as instructed by Manoharan sir.---START
//					xmldetail2hdr.append("<Detail2 dbID=\"\" domID=\"1\" objName=\"dist_issue_dummy\" objContext=\"2\">");
					//xmldetail2hdr.append("<Detail2 dbID=\"\" domID=\"1\" objName=\"dist_issue\" objContext=\"2\">");
					

					//mLineNoDist = rs.getInt("LINE_NO");
					 mLineNoDist=distOrdBean.getLineNo();
					//unit = rs.getString("UNIT");
					  unit=distOrdBean.getUnit();
					//unitAlt = rs.getString("UNIT__ALT");
					unitAlt=distOrdBean.getUnitAlt();
					//itemCode = rs.getString("ITEM_CODE");
					itemCode=distOrdBean.getItemCode();
					//qtyConfirm = rs.getDouble("QTY_CONFIRM");
					qtyConfirm=distOrdBean.getQtyConfirm();
					//qtyShipped = rs.getDouble("QTY_SHIPPED");
					qtyShipped=distOrdBean.getQtyShipped();
					//discount =	rs.getDouble("DISCOUNT");
					discount =distOrdBean.getDiscount();
					remQuantity = qtyConfirm - qtyShipped;
					rateClgStr=distOrdBean.getRateClgFmDistOrd();
					rateFmDistOrd=distOrdBean.getRateFmDistOrd();
					System.out.println(" rateClg received from dist_ordre det "+rateClgStr); 
					System.out.println(" rate received from dist_ordre det "+rateFmDistOrd); 
					

					//coding for stock allocation
					if (tranType != null && tranType.trim().length() > 0)
					{
						sql = "SELECT CHECK_INTEGRAL_QTY, TRAN_TYPE__PARENT FROM DISTORDER_TYPE WHERE TRAN_TYPE = '"+tranType+"' ";
						//System.out.println("sql :"+sql);
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
						quantity = qtyShipped + "";
						qtyOrdAlt = qtyShipped + "";
					}
					else
					{
						quantity = qtyConfirm + "";
						qtyOrdAlt = qtyConfirm + "";
					}
					//availableYn ="Y"; // 28/05/09 manoharan commented taken from distorder table
					sql =  " SELECT (CASE WHEN ACTIVE IS NULL THEN 'Y' ELSE ACTIVE END) ACT, MIN_SHELF_LIFE, "
						 + " (CASE WHEN TRACK_SHELF_LIFE IS NULL THEN 'N' ELSE TRACK_SHELF_LIFE END) TRK_SHELF_LIFE, "
						 + " (CASE WHEN SUPP_SOUR IS NULL THEN 'M' ELSE SUPP_SOUR END) SUP_SOUR, DESCR,ISS_CRITERIA  "
						 + " FROM ITEM WHERE ITEM_CODE = '"+itemCode+"' ";
					pstmt1= conn.prepareStatement(sql);
					rs1 = pstmt1.executeQuery();
					if ( rs1.next() )
					{
						active = rs1.getString( 1 );
						minShelfLife = rs1.getInt( 2 );
						trackShelfLife = rs1.getString( 3 );
						suppSour = rs1.getString( 4 );
						itemDescr = rs1.getString( 5 );
						//Added By NANDKUMAR GADKARI 02/08/ 18[START][iss_criteria in item master is W. then system should not allow to issue partial quantity.]
						issCriteria = rs1.getString( 6 );
						System.out.println("issCriteria  :[" + issCriteria+"]");
						if( active.equals("N") )
						{
							errCode = "VTITEM4";
							errString = itmDBAccessEJB.getErrorString("", errCode, "", "", conn);
							return errString;
						}
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null ;
					sql = " SELECT LOC_CODE__DAMAGED, SUNDRY_CODE, PRICE_LIST, PRICE_LIST__CLG, SITE_CODE__SHIP, CASE WHEN LOC_GROUP__JWISS IS NULL THEN ' ' ELSE LOC_GROUP__JWISS END LOC_GROUP__JWISS  "
						+ " FROM DISTORDER WHERE DIST_ORDER = '"+distOrderNo+"' ";
					pstmt1= conn.prepareStatement(sql);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						siteCodeShip = rs1.getString("SITE_CODE__SHIP");
						locGroupJwiss= rs1.getString("LOC_GROUP__JWISS");
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
						if(locGroupJwiss!=null && locGroupJwiss.trim().length()>0)
						{
							subSQL=" AND C.LOC_GROUP ='"+locGroupJwiss+"' ";
						}
						else
						{
							subSQL=" ";
						}
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null ;
					boolean isRecordFound = false;
					
					/* by alam as redundent in above query
					sql="SELECT SITE_CODE__SHIP FROM  DISTORDER where dist_order ='"+distOrderNo+"' ";
					pstmt1= conn.prepareStatement(sql);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						siteCodeShip = rs1.getString("SITE_CODE__SHIP");
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null ;
					*/
					//System.out.println( "siteCodeShip........................"+siteCodeShip );


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
					//System.out.println("remQuantity :" + remQuantity);
					//System.out.println( "sql........................"+sql );
					//changed by msalam on 180609 for stopping processing only 999 rows. start
					//while (rs1.next())
					//changed for load balancing as per k bhangar
					System.out.println("Resetting detail2xml----"+detail2xml);
					System.out.println("ItemCode----"+itemCode);
					boolean ispListnotDf=true;
					while (rs1.next() && linenoCtr < MAXLINESALLOWED )
					//changed by msalam on 180609 for stopping processing only 999 rows. end
					{
						//System.out.println( "inside while........................" );
						isRecordFound = true;
						lotNo = rs1.getString(1);
						lotSl = rs1.getString(2);
						packCode = rs1.getString(11);
						stkRate = rs1.getDouble(22);
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
						System.out.println("stockQty before issCriteria[" + stockQty + "] remQuantity :[" + remQuantity+"]");
						//Added By NANDKUMAR GADKARI  02/08/18 [START][iss_criteria in item master is W. then system should not allow to issue partial quantity.]
						System.out.println("--1--issCriteria stockQty[" + stockQty + "] remQuantity[" + remQuantity+"] minputQty["+minputQty+"]");
						if (issCriteria != null && ("W").equalsIgnoreCase(issCriteria))
						{/*
							
							if (stockQty >= remQuantity)
							{	
								minputQty= remQuantity;
								remQuantity = 0;
								
							}
							else
							{	
								minputQty = stockQty;
								remQuantity = remQuantity - stockQty;
							}	
							System.out.println("--3--issCriteria stockQty[" + stockQty + "] remQuantity[" + remQuantity+"] minputQty["+minputQty+"]");
						*/

							
							minputQty = stockQty;	
							if(minputQty > remQuantity)
							{
								System.out.println("--2--issCriteria stockQty[" + stockQty + "] remQuantity[" + remQuantity+"] minputQty["+minputQty+"]");
								continue;
							}
							if (stockQty >= remQuantity)
							{
								remQuantity = 0;
							}
							else
							{
								remQuantity = remQuantity - stockQty;
							}	
							System.out.println("--3--issCriteria stockQty[" + stockQty + "] remQuantity[" + remQuantity+"] minputQty["+minputQty+"]");
							
						}
						else 
						{	
							//Added by NANDKUMAR GADKARI  02/08/18 --------------------------------- [END]-----------------------
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
						}
						System.out.println("--4--issCriteria stockQty[" + stockQty + "] remQuantity[" + remQuantity+"] minputQty["+minputQty+"]");
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
						/*if (Double.parseDouble(rate1) == 0)
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
										//Changes Done by manoj dtd 04062013 replace / with ~.
										tabValue = siteCode + "~t" + rs1.getString(12) + "~t" + rs1.getString(1) + "~t";
										System.out.println("printing tabValue----"+tabValue);
										//System.out.println("tabValue :" + tabValue);
										rate2 = disCommon.pickRate(priceList, tranDate, itemCode, tabValue, "I",conn);
										//System.out.println("rate2 :" + rate2);
									}
								}
								rate1 = Double.toString(rate2);
						}*/
						if( priceList != null && priceList.trim().length() > 0 )
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
								/*	rate2 = disCommon.pickRate(priceList, tranDate, itemCode, tabValue, "I",conn);*/ //commented by Nandkumar Gadkari on 26/07/18
									rate2=stkRate; //added by by Nandkumar Gadkari on 26/07/18
									System.out.println("rate2 stkRate :" + rate2);
								}
							}
							
							if(rate2<=0)
							{
								ispListnotDf=false;
							}
							else
							{
								rateFmDistOrd=rate2+"";
							}
							//rate1 = Double.toString(rate2);
						}
						else
						{
							ispListnotDf=true;	
						}
						//Condition added by manoj dtd 13/10/2014 to exclude items for which rate in not defined
						if(ispListnotDf)
						{
							StringBuffer xmldetail2hdr1=new StringBuffer();
							xmldetail2hdr1.append("<Detail2 dbID=\"\" domID=\"1\" objName=\"dist_order_post\" objContext=\"2\">"); //change done by kunal on 31/01/13
							xmldetail2hdr1.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
							xmldetail2hdr1.append("<tran_id/>");
							xmldetail2hdr1.append("<dist_order><![CDATA["+distOrderNo+"]]></dist_order>");
							xmldetail2hdr1.append("<line_no_dist_order><![CDATA["+distOrdBean.getLineNo()+"]]></line_no_dist_order>");
							xmldetail2hdr1.append(getDetails(siteCode,mLineNoDist,distOrderNo,tranType,conn));
							if (priceListClg != null && priceListClg.trim().length() > 0 )
							{
								rateClgVal = disCommon.pickRate(priceListClg, tranDate, itemCode, rs1.getString(1),"D",conn);
								if (rateClgVal <= 0)
								{
									rateClgVal = Double.parseDouble(rateFmDistOrd);
									//System.out.println("rateClgVal :"+rateClgVal);
								}
							}
							else
							{
								rateClgVal=Double.parseDouble(rateClgStr);
							}
								
								//rateClg = Double.toString(rateClgVal);
							
	//						commented by ritesh on 17/sep/2014 as per instruct by manoj start
	//						rateClg = null;
	//						if (rateClg == null || rateClg.equals("") || Double.parseDouble(rateClg) == 0)
	//						{
	//							if (priceListClg != null && priceListClg.trim().length() > 0 )
	//							{
	//								rateClgVal = disCommon.pickRate(priceListClg, tranDate, itemCode, rs1.getString(1),"D",conn);
	//								//System.out.println("rateClgVal :"+rateClgVal);
	//							}
	//							if (rateClgVal <= 0)
	//							{
	//								rateClgVal = rate2;
	//								//System.out.println("rateClgVal :"+rateClgVal);
	//							}
	//							rateClg = Double.toString(rateClgVal);
	//						}
	//						commented by ritesh end
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
							stmt1 = conn.createStatement();
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
							
							xmldetail2stock = new StringBuffer();
							
							isDetFound =true;
							linenoCtr =	linenoCtr + 1;
							countDtl++;//Added by manoj 15/01/2013 not to call if detail not found
							xmldetail2stock.append("<line_no><![CDATA["+(linenoCtr)+"]]></line_no>");
							xmldetail2stock.append("<item_descr>").append("<![CDATA[").append(itemDescr==null ? "" : itemDescr).append("]]>").append("</item_descr>\r\n");
							xmldetail2stock.append("<location_descr>").append("<![CDATA[").append(rs1.getString(23)).append("]]>").append("</location_descr>\r\n");
							xmldetail2stock.append("<unit>").append("<![CDATA[").append(rs1.getString(5)).append("]]>").append("</unit>\r\n");
							xmldetail2stock.append("<unit__alt>").append("<![CDATA[").append(unitAlt).append("]]>").append("</unit__alt>\r\n");
							xmldetail2stock.append("<conv__qty__alt>").append("<![CDATA[").append(lcFact).append("]]>").append("</conv__qty__alt>\r\n");
							xmldetail2stock.append("<qty_order__alt>").append("<![CDATA[").append(lcQtyOrderAlt).append("]]>").append("</qty_order__alt>\r\n");
							String tLocCode = null;
							tLocCode = rs1.getString(12);
							xmldetail2stock.append("<loc_code>").append("<![CDATA[").append( (tLocCode == null ? "" : tLocCode.trim()) ).append("]]>").append("</loc_code>\r\n");
							//commented for rajendra on 04/09/08 for pick up rate from stock
	//						xmldetail2stock.append("<rate>").append("<![CDATA[").append(rate1).append("]]>").append("</rate>\r\n");
							//xmldetail2stock.append("<rate>").append("<![CDATA[").append(ratefromStock).append("]]>").append("</rate>\r\n");
	//						xmldetail2stock.append("<rate__clg>").append("<![CDATA[").append(rateClg).append("]]>").append("</rate__clg>\r\n"); //Commented - jiten - 05/04/06 -  as set in itemChange of lot_no
							
							xmldetail2stock.append("<rate>").append("<![CDATA[").append(rateFmDistOrd).append("]]>").append("</rate>\r\n");  // ADDED BY RITESH  ON 18/SEP/2014
	
							xmldetail2stock.append("<rate__clg>").append("<![CDATA[").append(rateClgVal).append("]]>").append("</rate__clg>\r\n");  // ADDED BY RITESH  ON 17/SEP/2014
	
							xmldetail2stock.append("<quantity>").append("<![CDATA[").append(minputQty).append("]]>").append("</quantity>\r\n");
							xmldetail2stock.append("<amount>").append("<![CDATA[").append(minputQty*Double.parseDouble(rate1)).append("]]>").append("</amount>\r\n");
							String tLotSl = null;
							tLotSl = rs1.getString(2);
							xmldetail2stock.append("<lot_sl>").append("<![CDATA[").append( ( tLotSl == null ? "   " : tLotSl) ).append("]]>").append("</lot_sl>\r\n");
							xmldetail2stock.append("<pack_code>").append("<![CDATA[").append((rs1.getString(11) == null) ? "":rs1.getString(11)).append("]]>").append("</pack_code>\r\n");
							xmldetail2stock.append("<disc_amt>").append("<![CDATA[").append(disAmount).append("]]>").append("</disc_amt>\r\n");
							//xmldetail2stock.append("<tax_class>").append("<![CDATA[").append( ( taxClass == null ? "": taxClass ) ).append("]]>").append("</tax_class>\r\n");
							//xmldetail2stock.append("<tax_chap>").append("<![CDATA[").append( ( taxChap == null ? "": taxChap ) ).append("]]>").append("</tax_chap>\r\n");
							//xmldetail2stock.append("<tax_env>").append("<![CDATA[").append( ( taxEnv == null ? "": taxEnv ) ).append("]]>").append("</tax_env>\r\n");
							grossWt = Double.parseDouble(getFormatedValue(grossWt,3));
							//System.out.println("[DistIssueActEJB] Gross Wt=============>"+grossWt);
							xmldetail2stock.append("<gross_weight>").append("<![CDATA[").append(grossWt).append("]]>").append("</gross_weight>\r\n");
							netWt = Double.parseDouble(getFormatedValue(netWt,3));
							//System.out.println("[DistIssueActEJB] Net Wt=============>"+netWt);
							xmldetail2stock.append("<net_weight>").append("<![CDATA[").append(netWt).append("]]>").append("</net_weight>\r\n");
							tareWt = Double.parseDouble(getFormatedValue(netWt,3));
							//System.out.println("[DistIssueActEJB] Tare Wt=============>"+tareWt);
							xmldetail2stock.append("<tare_weight>").append("<![CDATA[").append(tareWt).append("]]>").append("</tare_weight>\r\n");
							xmldetail2stock.append("<pack_instr>").append("<![CDATA[").append((rs1.getString(21) == null) ? "":rs1.getString(21)).append("]]>").append("</pack_instr>\r\n"); //Gulzar 24/03/07
							xmldetail2stock.append("<retest_date>").append("<![CDATA[").append((rs1.getDate(19) == null) ? "":sdf.format(rs1.getDate(19))).append("]]>").append("</retest_date>\r\n");
							xmldetail2stock.append("<dimension>").append("<![CDATA[").append((rs1.getString(18) == null) ? "":rs1.getString(18)).append("]]>").append("</dimension>\r\n");
							xmldetail2stock.append("<supp_code__mfg>").append("<![CDATA[").append((rs1.getString(20) == null) ? "":rs1.getString(20)).append("]]>").append("</supp_code__mfg>\r\n"); //Gulzar 24/03/07
							xmldetail2stock.append("<site_code__mfg>").append("<![CDATA[").append((rs1.getString(7) == null) ? "":rs1.getString(7)).append("]]>").append("</site_code__mfg>\r\n");
							xmldetail2stock.append("<mfg_date>").append("<![CDATA[").append((rs1.getDate(8) == null) ? "":sdf.format(rs1.getDate(8))).append("]]>").append("</mfg_date>\r\n");
							xmldetail2stock.append("<exp_date>").append("<![CDATA[").append((rs1.getDate(4) == null) ? "":sdf.format(rs1.getDate(4))).append("]]>").append("</exp_date>\r\n");
							xmldetail2stock.append("<potency_perc>").append("<![CDATA[").append( ( (rs1.getString(9) == null) ? "": rs1.getString(9) ) ).append("]]>").append("</potency_perc>\r\n");
							xmldetail2stock.append("<no_art>").append("<![CDATA[").append(noArt).append("]]>").append("</no_art>\r\n");
							xmldetail2stock.append("<batch_no>").append("<![CDATA[").append( ( (rs1.getString(13) == null) ? "":rs1.getString(13) ) ).append("]]>").append("</batch_no>\r\n");
							xmldetail2stock.append("<grade>").append("<![CDATA[").append( ( (rs1.getString(14) == null) ? "": rs1.getString(14) ) ).append("]]>").append("</grade>\r\n");
							xmldetail2stock.append("<lot_no>").append("<![CDATA[").append(( (rs1.getString(1) == null) ? "               " : rs1.getString(1) )).append("]]>").append("</lot_no>\r\n");
							detail2stock = xmldetail2stock.toString();
							xmldetail2stock = null;
	
							detail2xml.append(xmldetail2hdr1.toString());
							detail2xml.append(detail2stock);
							detail2xml.append(xmldetail2ftr.toString());
						}
						//System.out.println("xmlString detail2......" + detail2xml.toString());
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

			}//end of for 
			xmlBuff = null;
			xmlBuff = new StringBuffer();
			xmldetail1 = createHeader(siteCode, distOrderNo, xtraParams,  conn);
			xmlBuff.append(xmldetail1.toString());
			xmlBuff.append(detail2xml.toString());
			xmlBuff.append("</Header0>");
			xmlBuff.append("</group0>");
			xmlBuff.append("</DocumentRoot>");
			xmlString = xmlBuff.toString();
			System.out.println("sitecode is equal::"+siteCode);
			System.out.println("xmlString is equals:::"+xmlString);
			
			
			/* System.out.println("xmlString before serialization====>"+xmlString);
			Document detailDom = genericUtility.parseString(xmlString);
			System.out.println("detailDom.getFirstChild()=========>"+detailDom.getFirstChild());
			xmlString = serializeDom(detailDom.getFirstChild());
			System.out.println("xmlString after serialization......" + xmlString); */
			
			//System.out.println("xmlString......" + xmlString);
			System.out.println("Number of Records in Detail---"+countDtl);
			//if(countDtl>0)//Added by manoj 15/01/2013 not to call if detail not found
			if(xmlString.contains("Detail2"))
			{
				retString = distOrder.saveData(siteCode,xmlString,xtraParams,conn);//changes by Nandkumar Gadkari on 04/07/18 
	
				if (retString.indexOf("Success") > -1)
				{
					int  d =retString.indexOf("<TranID>");
					int f =	 retString.indexOf("</TranID>");
					String TranIDDistIssue = retString.substring(d+8,f);
					//System.out.println("TranIDDistIssue......... "+TranIDDistIssue);
					//System.out.println("Distribution Issue Generated Successfully");

					//added by kunal on 31/01/13 for cross update as per manoj instruction

					/*
					System.out.println("TranIDDistIssue = "+TranIDDistIssue);
					if(TranIDDistIssue != null && TranIDDistIssue.trim().length() > 0)
					{
						double amountDet = 0 ,discAmtDet = 0,netAmtDet = 0,taxAmtDet = 0;
						double quantityDet = 0,rateDet = 0 ,discountDet = 0;
						double discAmtHdr = 0,netAmtHdr = 0,taxAmtHdr = 0 ,amountHdr = 0,noArtHdr = 0, grossWtHdr = 0,tareWtHdr = 0,netWtHdr = 0 ;
						int lineNoDet = 0;
						String shipmentId = "";
						
						sql2 = " select shipment_id  from distord_iss  where tran_id = ?  ";
						pstmt2= conn.prepareStatement(sql2);
						pstmt2.setString(1, TranIDDistIssue);
						rs2 = pstmt2.executeQuery();
						if (rs2.next())
						{
							shipmentId = rs2.getString(1) == null? " ":rs2.getString(1);
						}
						rs2.close();
						rs2 = null;
						pstmt2.close();
						pstmt2 = null;
						
						/*
						sql2 = " select  quantity,rate ,discount, line_no , tax_amt from distord_issdet where tran_id = ?  order by line_no ";
						pstmt2= conn.prepareStatement(sql2);
						pstmt2.setString(1, TranIDDistIssue);
						rs2 = pstmt2.executeQuery();
						while (rs2.next())
						{
							quantityDet = rs2.getDouble("quantity");
							rateDet = rs2.getDouble("rate");
							discountDet = rs2.getDouble("discount");
							lineNoDet = rs2.getInt("line_no");
							taxAmtDet = rs2.getDouble("tax_amt");

							amountDet = quantityDet * rateDet ;
							discAmtDet = amountDet * (discountDet/100);
							netAmtDet = amount + taxAmtDet - discAmtDet ;

							sql = " update distord_issdet set amount = ? ,disc_amt = ? ,net_amt = ?  where tran_id = ?  and  line_no = ?   ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setDouble(1,amountDet );
							pstmt.setDouble(2,discAmtDet );
							pstmt.setDouble(3,netAmtDet );
							pstmt.setString(4,TranIDDistIssue );
							pstmt.setInt(5,lineNoDet );
							cnt = pstmt.executeUpdate();
							pstmt.close();
							pstmt = null;

							if( cnt > 0)
							{
								System.out.println("distord_issdet update successfully ["+cnt+"]");
							}

						}
						rs2.close();
						rs2 = null;
						pstmt2.close();
						pstmt2 = null;
						
						sql = "update distord_issdet  set "
								+"	amount = quantity * rate , "
								+"	disc_amt = quantity * rate * discount/100 ,  " 
								+ "	net_amt = quantity * rate  + tax_amt - (quantity * rate * discount/100 )  "
								+ " where  tran_id = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,TranIDDistIssue );
						cnt = pstmt.executeUpdate();
						pstmt.close();
						pstmt = null;

						if( cnt > 0)
						{
							System.out.println("distord_issdet update successfully ["+cnt+"]");
						}

						sql2 = " select sum(tax_amt),sum(amount),sum(net_amt),sum(disc_amt),sum( no_art)  ,sum(gross_weight),sum(tare_weight),sum(net_weight ) from distord_issdet where  tran_id = ?  ";
						pstmt2= conn.prepareStatement(sql2);
						pstmt2.setString(1, TranIDDistIssue);
						rs2 = pstmt2.executeQuery();
						if (rs2.next())
						{
							taxAmtHdr = rs2.getDouble(1);
							amountHdr = rs2.getDouble(2);
							netAmtHdr = rs2.getDouble(3);
							discAmtHdr = rs2.getDouble(4);
							noArtHdr = rs2.getDouble(5);
							grossWtHdr = rs2.getDouble(6);
							tareWtHdr = rs2.getDouble(7);
							netWtHdr = rs2.getDouble(8);
						}
						rs2.close();
						rs2 = null;
						pstmt2.close();
						pstmt2 = null;
						if(shipmentId != null && shipmentId.trim().length() > 0)
						{
							grossWtHdr = tareWtHdr = netWtHdr = 0;
						}

						sql = " update distord_iss set tax_amt = ? , amount = ? ,net_amt = ? ,discount = ? ,no_art = ? ,gross_weight = ? ,tare_weight = ? ,net_weight = ?  where tran_id = ?   ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setDouble(1,taxAmtHdr );
						pstmt.setDouble(2,amountHdr);
						pstmt.setDouble(3,netAmtHdr);
						pstmt.setDouble(4,discAmtHdr );
						pstmt.setDouble(5,noArtHdr);
						pstmt.setDouble(6,grossWtHdr);
						pstmt.setDouble(7,tareWtHdr);
						pstmt.setDouble(8,netWtHdr);
						pstmt.setString(9,TranIDDistIssue );
						cnt = pstmt.executeUpdate();
						pstmt.close();
						pstmt = null;
						if( cnt > 0)
						{
							System.out.println("distord_iss update successfully ["+cnt+"]");
						}
					}
					*/
					//added by kunal on 31/01/13 for cross update as per manoj instruction  end 



						if( isAutoConfirm != null 
									&& isAutoConfirm.trim().length() > 0 
									&& isAutoConfirm.equalsIgnoreCase( "Y" )  )
						{						
							DistIssueConfirmAct confActEjb = new DistIssueConfirmAct();
		
							retString = confActEjb.actionConfirm( TranIDDistIssue, xtraParams, "true", conn );
							System.out.println("After confri :: " + retString );
							confActEjb = null;
						} 
					
				}
				else
				{
					//System.out.println("Distribution Issue not Generated ");
					//msalam on 220609 for showing actual message coming from saveData
					//retString="ERROR";
					//end msalam on 220609
				}
			}
			else
			{
				//retString="NODETAILEXIST"+distOrderNo;
			}
			/*if(!isfound)
			{
				retString="PARTIALISS"+distOrderNo;
			}*/
		}
		catch(Exception e)
		{
			e.printStackTrace();
			//System.out.println("returning in createdistiss ::  "+retString);
			retString="ERROR";
			throw new ITMException(e);
		}
		//System.out.println("retString..   ....... "+retString);
		return retString;
	}
	public String createDistIssue(String siteCode,String xmlString,String xtraParams, Connection conn)throws RemoteException,ITMException
	{
		StringBuffer xmlBuff = new StringBuffer();

		String xmldetail1 =null;
		StringBuffer detail2xml = new StringBuffer();
		StringBuffer xmldetail2ftr = new StringBuffer();
		xmldetail2ftr.append("</Detail2>");
		String distOrderNo = null;
		String sql = null;
		String tranDate = null;
		String locCode = null;
		String retString = "";
		PreparedStatement pstmt = null,pstmt1 = null,pstmt2 =null;
		ResultSet rs = null,rs1 =null,rs2 = null,rs3 = null;
		Statement stmt1 = null;
		//Connection conn = null;
		String lsPackCode = null,unitAlt = null,unit = null,mLotNo = null;
		int count = 0;
		int linenoCtr = 0;
		String 	tranType ="";
		double mod = 0d, minputQty = 0d, remQuantity = 0d, stockQty = 0d, integralQty = 0d;
		double grossPer = 0d,netPer = 0d,tarePer = 0d, grossWt = 0d, netWt = 0d, tareWt =0d, rateClgVal = 0d, rate2 = 0d;
		double disAmount = 0d, amount = 0d, shipperQty = 0d,discount =0;
		int  minShelfLife = 0, noArt1 = 0, cnt = 0;
		int mLineNoDist =0;
		double qtyConfirm =0,qtyShipped =0,lcQtyOrderAlt =0,lcFact =0;
		String suppSour = "", trackShelfLife = "", siteCodeMfg = "", sundryCode = "", potencyPerc = "";
		String priceList = "", tabValue = "", priceListClg = "", chkDate = "", disCountPer = "";
		String tranTypePparent = null,rate ="",lsTranTypeParent ="";
		String qtyOrdAlt = "",convQtyAlt = "";
		String res = "", locCodeDamaged = "",itemCode ="",availableYn ="";
		String checkIntegralQty = "", rate1 = "",tranTypeParent ="",quantity = "";
		String active = "", itemDescr = "",errCode ="",sql2 ="",noArt ="";
		String detail2stock ="",errString ="",taxChap ="",siteCodeShip ="";
		String lotNo ="",lotSl ="",packCode ="",rateClg ="",taxEnv ="",taxClass ="";
		java.util.Date expDate = null,mfgDate = null, chkDate1 = null;
		boolean isDetFound = false;
		String prvDeptCode = null,deptCode ="",ratefromStock ="";
		StringBuffer xmldetail2stock = null;
		int MAXLINESALLOWED = 0;
		int countDtl=0;
		String locGroupJwiss="";
		String subSQL="";
		String rateFmDistOrd = "";
		try
		{
			
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			if (conn == null)
			{
				ConnDriver connDriver = new ConnDriver();
				conn = connDriver.getConnectDB("DriverITM");
				connDriver = null;
				conn.setAutoCommit( false ); //  added 18/06/09 manoharan
			}
			ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
			DistCommon disCommon = new DistCommon();
			ByteArrayInputStream bais = new ByteArrayInputStream(xmlString.getBytes());
			Document dom = (DocumentBuilderFactory.newInstance()).newDocumentBuilder().parse(bais);
			NodeList parent = dom.getElementsByTagName("TranID");
			distOrderNo = parent.item(0).getFirstChild().getNodeValue();
			tranDate = getCurrdateAppFormat() ;
			//System.out.println("[CreateDistIssue]------------Distribution Issue is created for Dist Order------------->"+distOrderNo);

			isAutoConfirm = disCommon.getDisparams( "999999", "AUTO_DIST_ISS_CONFIRM", conn );
			
			if( isAutoConfirm != null && isAutoConfirm.trim().length() > 0 )
			{
				isAutoConfirm = isAutoConfirm.substring( 0, 1 );
			}
			else 
			{
				isAutoConfirm = "N";
			}
			//msalam on 230609 as per kb
			String maxLinesStr = disCommon.getDisparams( "999999", "MAX_LINES_DIST_ISS", conn );
			if (maxLinesStr == null || maxLinesStr.trim().length() == 0 || maxLinesStr.equals("NULLFOUND"))
			{
				maxLinesStr = "600";
			}
			MAXLINESALLOWED = Integer.parseInt( maxLinesStr.trim() );
			maxLinesStr = null;
			//msalam on 230609 as per kb end
/*
			sql="SELECT D.DIST_ORDER AS DIST_ORDER,D.ORDER_DATE AS ORDER_DATE,D.SITE_CODE__SHIP AS SITE_CODE__SHIP,D.SITE_CODE__DLV AS SITE_CODE__DLV,D.SHIP_DATE AS SHIP_DATE,D.DUE_DATE AS DUE_DATE,D.REMARKS AS REMARKS,"
			+" D.DIST_ROUTE AS DIST_ROUTE,D.PRICE_LIST AS PRICE_LIST,D.CONFIRMED AS CONFIRMED,D.CHG_USER AS CHG_USER,D.CHG_TERM AS CHG_TERM,D.TARGET_WGT AS TARGET_WGT,D.TARGET_VOL AS TARGET_VOL,D.LOC_CODE__GIT AS LOC_CODE__GIT,"
			+" D.CHG_DATE AS CHG_DATE,SITE_A.DESCR AS SITEA_DESCR,SITE_B.DESCR AS SITEB_DESCR,LOCATION.DESCR AS LOCATION_DESCR,D.CONF_DATE AS CONF_DATE,D.SITE_CODE AS SITE_CODE,D.STATUS AS STATUS,D.SALE_ORDER AS SALE_ORDER,"
			+" D.REMARKS1 AS REMARK1,D.REMARKS2 AS REMARK2,TRIM(D.ORDER_TYPE) AS ORDER_TYPE,SITE_A.ADD1 AS SITEA_ADD1,SITE_A.ADD2 AS SITEA_ADD2,SITE_A.CITY AS SITEA_CITY,SITE_A.PIN AS SITEA_PIN,SITE_A.STATE_CODE AS SITEA_STATE_CODE,"
			+" SITE_B.ADD1 AS SITEB_ADD1,SITE_B.ADD2 AS SITEB_ADD2,SITE_B.CITY AS SITEB_CITY,SITE_B.PIN AS SITEB_PIN,SITE_B.STATE_CODE AS SITEB_STATE_CODE,D.LOC_CODE__CONS AS LOC_CODE__CONS,D.SUNDRY_TYPE AS SUNDRY_TYPE,"
			+" D.SUNDRY_CODE AS SUNDRY_CODE,D.AUTO_RECEIPT AS AUTO_RECEIPT,D.TRAN_TYPE AS TRAN_TYPE,D.CURR_CODE AS CURR_CODE,D.EXCH_RATE AS EXCH_RATE,D.SALES_PERS AS SALES_PERS,SALES_PERS.SP_NAME AS SP_NAME,"
			+" D.LOC_CODE__GITBF AS LOC_CODE__GITBF,D.CUST_CODE__DLV AS CUST_CODE__DLV,D.DLV_TO AS DLV_TO,D.DLV_ADD1 AS DLV_ADD1,D.DLV_ADD2 AS DLV_ADD2,D.DLV_ADD3 AS DLV_ADD3,D.DLV_CITY AS DLV_CITY,"
			+" D.STATE_CODE__DLV AS STATE_CODE__DLV,D.COUNT_CODE__DLV AS COUNT_CODE__DLV,D.DLV_PIN AS DLV_PIN,D.STAN_CODE AS STAN_CODE,D.TEL1__DLV AS TEL1__DLV,D.TEL2__DLV AS TEL2__DLV,D.TEL3__DLV AS TEL3__DLV,"
			+" D.FAX__DLV AS FAX__DLV,D.AVALIABLE_YN AS AVALIABLE_YN,D.PURC_ORDER AS PURC_ORDER,D.TOT_AMT AS TOT_AMT,D.TAX_AMT AS TAX_AMT,D.NET_AMT AS NET_AMT,D.TRAN_SER AS TRAN_SER,"
			+" D.PRICE_LIST__CLG AS PRICE_LIST__CLG,SPACE(25) AS LOC,FN_SUNDRY_NAME(D.SUNDRY_TYPE,D.SUNDRY_CODE,'N') AS SUNDRY_NAME,"
			+" D.PROJ_CODE AS PROJ_CODE,SITE_C.DESCR AS SITEC_DESCR,D.POLICY_NO AS POLICY_NO,D.LOC_CODE__DAMAGED AS LOC_CODE__DAMAGED,D.SITE_CODE__BIL AS SITE_CODE__BIL,SITE_D.DESCR AS SITED_DESCR,SITE_D.ADD1 AS SITED_ADD1,"
			+" SITE_D.ADD2 AS SITED_ADD2,SITE_D.CITY AS SITED_CITY,SITE_D.PIN SITED_PIN ,SITE_D.STATE_CODE AS SITED_STATE_CODE,D.TRANS_MODE AS TRANS_MODE"
			+"  FROM DISTORDER  D,SITE SITE_A,SITE SITE_B,LOCATION  LOCATION,SALES_PERS  SALES_PERS,SITE SITE_C,SITE SITE_D "
			+"  WHERE ( D.SITE_CODE__SHIP      = SITE_A.SITE_CODE  ) AND "
			+" ( D.SITE_CODE__DLV      = SITE_B.SITE_CODE (+)  ) AND "
			+" ( D.LOC_CODE__GIT      = LOCATION.LOC_CODE (+)  ) AND "
			+" ( D.SITE_CODE      = SITE_C.SITE_CODE (+)  ) AND "
			+" ( D.SALES_PERS=SALES_PERS.SALES_PERS(+)) AND "
			+" ( D.SITE_CODE__BIL=SITE_D.SITE_CODE(+)) "
			+"  AND ( ( D.DIST_ORDER    = '"+distOrderNo+"' ) ) ";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				this.currCode = rs.getString("CURR_CODE");
				locCode = rs.getString("LOC_CODE__GIT");
				tranType = rs.getString("TRAN_TYPE");
				siteCodeShip = rs.getString("SITE_CODE__SHIP");
				xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
				xmlBuff.append("<DocumentRoot>");
				xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
				xmlBuff.append("<group0>");
				xmlBuff.append("<description>").append("Group0 description").append("</description>");
				xmlBuff.append("<Header0>");
				xmlBuff.append("<objName><![CDATA[").append("dist_issue").append("]]></objName>");
				xmlBuff.append("<pageContext><![CDATA[").append("1").append("]]></pageContext>");
				xmlBuff.append("<objContext><![CDATA[").append("1").append("]]></objContext>");
				xmlBuff.append("<editFlag><![CDATA[").append("A").append("]]></editFlag>");
				xmlBuff.append("<focusedColumn><![CDATA[").append("").append("]]></focusedColumn>");
				xmlBuff.append("<action><![CDATA[").append("SAVE").append("]]></action>");
				xmlBuff.append("<elementName><![CDATA[").append("").append("]]></elementName>");
				xmlBuff.append("<keyValue><![CDATA[").append("1").append("]]></keyValue>");
				xmlBuff.append("<taxKeyValue><![CDATA[").append("").append("]]></taxKeyValue>");
				xmlBuff.append("<saveLevel><![CDATA[").append("1").append("]]></saveLevel>");
				xmlBuff.append("<forcedSave><![CDATA[").append(true).append("]]></forcedSave>");
				xmlBuff.append("<taxInFocus><![CDATA[").append(false).append("]]></taxInFocus>");
				xmlBuff.append("<description>").append("Header0 members").append("</description>");

				xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"dist_issue\" objContext=\"1\">");
				xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
				xmlBuff.append("<tran_id/>");
				//tranDate = sdf.format(new Timestamp(System.currentTimeMillis()));
				xmlBuff.append("<tran_date><![CDATA["+tranDate+"]]></tran_date>");
				xmlBuff.append("<eff_date><![CDATA["+tranDate+"]]></eff_date>");
				xmlBuff.append("<dist_order><![CDATA["+distOrderNo+"]]></dist_order>");
				//System.out.println("SITE_CODE__SHIP------------>"+rs.getString("SITE_CODE__SHIP"));
				xmlBuff.append("<site_code><![CDATA["+(rs.getString("SITE_CODE__SHIP")==null?"":rs.getString("SITE_CODE__SHIP").trim())+"]]></site_code>");
				xmlBuff.append("<site_code__dlv><![CDATA["+(rs.getString("SITE_CODE__DLV")==null?"":rs.getString("SITE_CODE__DLV").trim())+"]]></site_code__dlv>");
				xmlBuff.append("<dist_route><![CDATA["+(rs.getString("DIST_ROUTE")==null?"":rs.getString("DIST_ROUTE"))+"]]></dist_route>");
				xmlBuff.append("<tran_code><![CDATA[]]></tran_code>");
				xmlBuff.append("<lr_no><![CDATA[]]></lr_no>");
				xmlBuff.append("<lr_date><![CDATA[]]></lr_date>");
				xmlBuff.append("<lorry_no><![CDATA[]]></lorry_no>");
				xmlBuff.append("<gross_weight><![CDATA[0]]></gross_weight>");
				xmlBuff.append("<tare_weight><![CDATA[0]]></tare_weight>");
				xmlBuff.append("<net_weight><![CDATA[0]]></net_weight>");
				xmlBuff.append("<frt_amt><![CDATA[0]]></frt_amt>");
				xmlBuff.append("<amount><![CDATA[0]]></amount>");
				xmlBuff.append("<tax_amt><![CDATA[0]]></tax_amt>");
				xmlBuff.append("<net_amt><![CDATA[0]]></net_amt>");
				xmlBuff.append("<remarks><![CDATA[]]></remarks>");
				xmlBuff.append("<frt_type><![CDATA[T]]></frt_type>");
				xmlBuff.append("<chg_user><![CDATA["+(rs.getString("CHG_USER")==null?"":rs.getString("CHG_USER").trim())+"]]></chg_user>");
				xmlBuff.append("<chg_term><![CDATA["+(rs.getString("CHG_TERM")==null?"":rs.getString("CHG_TERM").trim())+"]]></chg_term>");
				xmlBuff.append("<curr_code><![CDATA[" + this.currCode + "]]></curr_code>");
				xmlBuff.append("<chg_date><![CDATA["+tranDate+"]]></chg_date>");
				xmlBuff.append("<site_descr><![CDATA["+(rs.getString("SITEA_DESCR")==null?"":rs.getString("SITEA_DESCR").trim())+"]]></site_descr>");
				xmlBuff.append("<site_to_descr><![CDATA["+(rs.getString("SITEB_DESCR")==null?"":rs.getString("SITEB_DESCR").trim())+"]]></site_to_descr>");
				xmlBuff.append("<location_descr><![CDATA["+(rs.getString("LOCATION_DESCR")==null?"":rs.getString("LOCATION_DESCR").trim())+"]]></location_descr>");
				xmlBuff.append("<tran_name><![CDATA[]]></tran_name>");
				xmlBuff.append("<currency_descr><![CDATA[]]></currency_descr>");
				xmlBuff.append("<confirmed><![CDATA[N]]></confirmed>");
				xmlBuff.append("<loc_code__git><![CDATA["+(rs.getString("LOC_CODE__GITBF")==null?"":rs.getString("LOC_CODE__GITBF"))+"]]></loc_code__git>");
				xmlBuff.append("<conf_date><![CDATA["+sdf.format(rs.getTimestamp("CONF_DATE"))+"]]></conf_date>");
				xmlBuff.append("<no_art><![CDATA[0]]></no_art>");
				xmlBuff.append("<trans_mode><![CDATA["+(rs.getString("TRANS_MODE")==null?"":rs.getString("TRANS_MODE").trim())+"]]></trans_mode>");
				xmlBuff.append("<gp_no><![CDATA[]]></gp_no>");
				xmlBuff.append("<gp_date/>");
				xmlBuff.append("<conf_passwd/>");

				xmlBuff.append("<order_type><![CDATA["+(rs.getString("ORDER_TYPE")==null?"":rs.getString("ORDER_TYPE").trim())+"]]></order_type>");
				xmlBuff.append("<gp_ser><![CDATA[I]]></gp_ser>");
				xmlBuff.append("<ref_no><![CDATA[]]></ref_no>");
				xmlBuff.append("<ref_date><![CDATA[]]></ref_date>");
				//xmlBuff.append("<available_yn><![CDATA[Y]]></available_yn>");
				xmlBuff.append("<available_yn><![CDATA["+(rs.getString("AVALIABLE_YN")==null?"N":rs.getString("AVALIABLE_YN").trim())+"]]></available_yn>");
				xmlBuff.append("<site_add1><![CDATA["+(rs.getString("SITEA_ADD1")==null?"":rs.getString("SITEA_ADD1").trim())+"]]></site_add1>");
				xmlBuff.append("<site_add2><![CDATA["+(rs.getString("SITEA_ADD2")==null?"":rs.getString("SITEA_ADD2").trim())+"]]></site_add2>");
				xmlBuff.append("<site_city><![CDATA["+(rs.getString("SITEA_CITY")==null?"":rs.getString("SITEA_CITY").trim())+"]]></site_city>");
				xmlBuff.append("<site_pin><![CDATA["+(rs.getString("SITEA_PIN")==null?"":rs.getString("SITEA_PIN").trim())+"]]></site_pin>");
				xmlBuff.append("<site_state_code><![CDATA["+(rs.getString("SITEA_STATE_CODE")==null?"":rs.getString("SITEA_STATE_CODE").trim())+"]]></site_state_code>");
				xmlBuff.append("<exch_rate><![CDATA["+(rs.getDouble("EXCH_RATE"))+"  ]]></exch_rate>");
				xmlBuff.append("<tran_type><![CDATA["+(rs.getString("TRAN_TYPE")==null?"":rs.getString("TRAN_TYPE").trim())+"]]></tran_type>");
				xmlBuff.append("<emp_code__aprv><![CDATA[]]></emp_code__aprv>");
				xmlBuff.append("<discount><![CDATA[0]]></discount>");
				xmlBuff.append("<permit_no><![CDATA[]]></permit_no>");
				xmlBuff.append("<shipment_id><![CDATA[]]></shipment_id>");
				xmlBuff.append("<curr_code__frt><![CDATA[" + this.currCode + "]]></curr_code__frt>");
				xmlBuff.append("<exch_rate__frt><![CDATA[]]></exch_rate__frt>");
				xmlBuff.append("<currency_descr__frt><![CDATA[]]></currency_descr__frt>");
				xmlBuff.append("<rd_permit_no><![CDATA[]]></rd_permit_no>");
				xmlBuff.append("<dc_no><![CDATA[]]></dc_no>");
				xmlBuff.append("<tran_ser><![CDATA[D-ISS ]]></tran_ser>");
				xmlBuff.append("<part_qty><![CDATA[A]]></part_qty>");
				xmlBuff.append("<sundry_details><![CDATA[]]></sundry_details>");
				xmlBuff.append("<sundry_name><![CDATA["+(rs.getString("SUNDRY_NAME")==null?"":rs.getString("SUNDRY_NAME"))+"]]></sundry_name>");
				xmlBuff.append("<proj_code><![CDATA["+(rs.getString("PROJ_CODE")==null?"":rs.getString("PROJ_CODE").trim())+"]]></proj_code>");
				xmlBuff.append("<site_tele1><![CDATA[]]></site_tele1>");
				xmlBuff.append("<site_tele2><![CDATA[]]></site_tele2>");
				xmlBuff.append("<site_tele3><![CDATA[]]></site_tele3>");
				xmlBuff.append("<site_code__bil><![CDATA[]]></site_code__bil>");
				xmlBuff.append("<site_descr_bill><![CDATA[]]></site_descr_bill>");
				xmlBuff.append("<site_add1_bill><![CDATA[]]></site_add1_bill>");
				xmlBuff.append("<site_add2_bill><![CDATA[]]></site_add2_bill>");
				xmlBuff.append("<site_city_bill><![CDATA[]]></site_city_bill>");
				xmlBuff.append("<site_pin_bill><![CDATA[]]></site_pin_bill>");
				xmlBuff.append("<site_state_code_bill><![CDATA[]]></site_state_code_bill>");
				xmlBuff.append("<pallet_wt><![CDATA[]]></pallet_wt>");
				xmlBuff.append("<auto_receipt><![CDATA[N]]></auto_receipt>");
				xmlBuff.append("</Detail1>");
			}//end of if(rs.next())
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;   */

			prvDeptCode = "NULL";
			sql="SELECT D.DIST_ORDER,D.LINE_NO AS LINE_NO,D.TRAN_ID__DEMAND,D.ITEM_CODE AS ITEM_CODE,D.QTY_ORDER AS QTY_ORDER,D.QTY_CONFIRM AS QTY_CONFIRM,"
			+" D.QTY_RECEIVED AS QTY_RECEIVED,D.QTY_SHIPPED AS QTY_SHIPPED,D.DUE_DATE AS DUE_DATE,D.TAX_CLASS AS TAX_CLASS,D.TAX_CHAP AS TAX_CHAP,D.TAX_ENV AS TAX_ENV,D.UNIT AS UNIT,ITEM.DESCR AS ITEM_DESCR,"
			+" D.SALE_ORDER AS SALE_ORDER,D.LINE_NO__SORD AS LINE_NO__SORD,D.RATE AS RATE,D.QTY_RETURN AS QTY_RETURN,D.RATE__CLG AS RATE__CLG,D.DISCOUNT AS DISCOUNT,D.REMARKS AS REMARKS,D.TOT_AMT AS TOT_AMT,D.TAX_AMT AS TAX_AMT,"
			+" D.NET_AMT AS NET_AMT,D.OVER_SHIP_PERC AS OVER_SHIP_PERC,SPACE(300) AS QTY_DETAILS,D.UNIT__ALT AS UNIT__ALT,D.CONV__QTY__ALT AS CONV__QTY__ALT,"
			+" D.QTY_ORDER__ALT AS QTY_ORDER__ALT,D.SHIP_DATE AS SHIP_DATE,D.PACK_INSTR AS PACK_INSTR ,"
			+" ( CASE WHEN ITEM.DEPT_CODE__ISS IS NULL then ' ' else ITEM.DEPT_CODE__ISS END ) AS DEPT_CODE, "
			+" H.AVALIABLE_YN, H.TRAN_TYPE AS TRAN_TYPE, CASE WHEN H.LOC_GROUP__JWISS IS NULL THEN ' ' ELSE H.LOC_GROUP__JWISS END AS LOC_GROUP "
			+" FROM DISTORDER_DET  D,ITEM  ITEM, DISTORDER H "
			+" WHERE D.DIST_ORDER = H.DIST_ORDER "
			+" AND D.ITEM_CODE = ITEM.ITEM_CODE "
			+" AND H.DIST_ORDER    = '"+distOrderNo+"'"
			+ " AND   CASE WHEN D.STATUS IS NULL THEN 'O' ELSE D.STATUS END<>'C' "//Added by manoj dtd 24/12/2013 to exclude closed lines
			+" ORDER BY item.dept_code__iss ASC,"   //added by rajendra on 02/09/08
			+" D.LINE_NO ASC ";
			//System.out.println( "sql....................... " + sql );
			pstmt= conn.prepareStatement( sql );
			rs = pstmt.executeQuery();
			count = 0;
			detail2xml=new StringBuffer();
			countDtl=0;
			locGroupJwiss="";
			while( rs.next() )
			{
				rateClg = Double.toString(rs.getDouble( "RATE__CLG" ));  // ADDED BY RITESH  ON 17/SEP/2014
				rateFmDistOrd = Double.toString(rs.getDouble( "RATE" ));  // ADDED BY RITESH  ON 18/SEP/2014
				System.out.println(" rateClg received from dist order iss det"+rateClg);
				System.out.println(" rate received from dist order iss det"+rateFmDistOrd);
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
				if(prvDeptCode.equals("NULL"))
				{
					prvDeptCode = deptCode;
					//System.out.println( "prvDeptCode....................... " + prvDeptCode );
				}
				System.out.println("prvDeptCode----deptCode--"+prvDeptCode+"----"+deptCode);
				/*Commented by manoj dtd 09/07/2013 not required as only header is created
				if( ! prvDeptCode.equalsIgnoreCase(deptCode))
				{
					System.out.println( "different....................... " );
					prvDeptCode = deptCode;
					xmldetail1 = createHeader(siteCode, distOrderNo, xtraParams,  conn);
					xmlBuff   = null;
					xmlBuff = new StringBuffer();
					xmlBuff.append(xmldetail1.toString());
					xmlBuff.append(detail2xml.toString());
					xmlBuff.append("</Header0>");
					xmlBuff.append("</group0>");
					xmlBuff.append("</DocumentRoot>");
					xmlString = xmlBuff.toString();
					//System.out.println("xmlString final......" + xmlString);
					retString = distOrder.saveData(siteCode,xmlString,conn);
					if (retString.indexOf("Success") > -1)
					{
						int  d =retString.indexOf("<TranID>");
						int f =	 retString.indexOf("</TranID>");
						String TranIDDistIssue = retString.substring(d+8,f);
						//System.out.println("TranIDDistIssue......... "+TranIDDistIssue);
						//System.out.println("Distribution Issue Generated Successfully");
						linenoCtr = 0; //  23/06/09 added by manoharan 
					}
					else
					{
						//System.out.println("Distribution Issue not Generated ");
						//msalam on 220609 to show actual error coming form master stateful
						//retString="ERROR";
						//end msalam on 220609
					}
				}*/
				//if( prvDeptCode.equalsIgnoreCase( deptCode ) )
				//{
					System.out.println( "match dept....................... " );
					//StringBuffer xmldetail2hdr = new StringBuffer();
					
					
					
					
					mLineNoDist = rs.getInt("LINE_NO");
					unit = rs.getString("UNIT");
					unitAlt = rs.getString("UNIT__ALT");
					itemCode = rs.getString("ITEM_CODE");
					qtyConfirm = rs.getDouble("QTY_CONFIRM");
					qtyShipped = rs.getDouble("QTY_SHIPPED");
					discount =	rs.getDouble("DISCOUNT");
					remQuantity = qtyConfirm - qtyShipped;
					/* //xmldetail2hdr.append("<Detail2 dbID=\"\" domID=\"1\" objName=\"dist_issue\" objContext=\"2\">");
					//object name changed by msalam on 18/06/09 as per manoharan sir. start
					//from dist_issue to dist_issue_prc
					xmldetail2hdr.append("<Detail2 dbID=\"\" domID=\"1\" objName=\"dist_issue_prc\" objContext=\"2\">");
					//object name changed by msalam on 18/06/09 as per manoharan sir. end */
					
					//obj_name again changed to dist_issue on 03/02/10 as instructed by Manoharan sir.---START
//					xmldetail2hdr.append("<Detail2 dbID=\"\" domID=\"1\" objName=\"dist_issue_dummy\" objContext=\"2\">");
					//xmldetail2hdr.append("<Detail2 dbID=\"\" domID=\"1\" objName=\"dist_issue\" objContext=\"2\">");
					


					//coding for stock allocation
					if (tranType != null && tranType.trim().length() > 0)
					{
						sql = "SELECT CHECK_INTEGRAL_QTY, TRAN_TYPE__PARENT FROM DISTORDER_TYPE WHERE TRAN_TYPE = '"+tranType+"' ";
						//System.out.println("sql :"+sql);
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
						quantity = qtyShipped + "";
						qtyOrdAlt = qtyShipped + "";
					}
					else
					{
						quantity = qtyConfirm + "";
						qtyOrdAlt = qtyConfirm + "";
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
						trackShelfLife = rs1.getString( 3 );
						suppSour = rs1.getString( 4 );
						itemDescr = rs1.getString( 5 );
						if( active.equals("N") )
						{
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
						+ " FROM DISTORDER WHERE DIST_ORDER = '"+distOrderNo+"' ";
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
					
					/* by alam as redundent in above query
					sql="SELECT SITE_CODE__SHIP FROM  DISTORDER where dist_order ='"+distOrderNo+"' ";
					pstmt1= conn.prepareStatement(sql);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						siteCodeShip = rs1.getString("SITE_CODE__SHIP");
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null ;
					*/
					//System.out.println( "siteCodeShip........................"+siteCodeShip );


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
					//System.out.println("remQuantity :" + remQuantity);
					//System.out.println( "sql........................"+sql );
					//changed by msalam on 180609 for stopping processing only 999 rows. start
					//while (rs1.next())
					//changed for load balancing as per k bhangar
					//detail2xml=new StringBuffer();
					System.out.println("Resetting detail2xml----"+detail2xml);
					System.out.println("ItemCode----"+itemCode);
					boolean ispListnotDf=true;
					while (rs1.next() && linenoCtr < MAXLINESALLOWED )
					//changed by msalam on 180609 for stopping processing only 999 rows. end
					{
						//System.out.println( "inside while........................" );
						isRecordFound = true;
						lotNo = rs1.getString(1);
						lotSl = rs1.getString(2);
						packCode = rs1.getString(11);
						ratefromStock = rs1.getString(22);
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
							integralQty = getIntegralQty( siteCode, itemCode, lotNo, packCode, checkIntegralQty);
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
						/*if (Double.parseDouble(rate1) == 0)
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
						}*/
						if( priceList != null && priceList.trim().length() > 0 )
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
							
							if(rate2<=0)
							{
								ispListnotDf=false;
							}
							else
							{
								rateFmDistOrd=rate2+"";
							}
							//rate1 = Double.toString(rate2);
						}
						else
						{
							ispListnotDf=true;	
						}
						//Condition added by manoj dtd 13/10/2014 to exclude items for which rate in not defined
						if(ispListnotDf)
						{
							StringBuffer xmldetail2hdr1 = new StringBuffer();
							xmldetail2hdr1.append("<Detail2 dbID=\"\" domID=\"1\" objName=\"dist_order_post\" objContext=\"2\">");
							//obj_name again changed to dist_issue on 03/02/10 as instructed by Manoharan sir.---END
							
							xmldetail2hdr1.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
							xmldetail2hdr1.append("<tran_id/>");
							//xmldetail2hdr.append("<line_no><![CDATA["+(count)+"]]></line_no>");
							xmldetail2hdr1.append("<dist_order><![CDATA["+distOrderNo+"]]></dist_order>");
							xmldetail2hdr1.append("<line_no_dist_order><![CDATA["+rs.getInt("LINE_NO")+"]]></line_no_dist_order>");
	
							
							xmldetail2hdr1.append(getDetails(siteCode,mLineNoDist,distOrderNo,tranType,conn));
						
							
						
						if (priceListClg != null && priceListClg.trim().length() > 0 )
						{
							rateClgVal = disCommon.pickRate(priceListClg, tranDate, itemCode, rs1.getString(1),"D",conn);
							System.out.println("rateClgVal----"+rateClgVal);
							if (rateClgVal <= 0)
							{
								rateClgVal = Double.parseDouble(rateFmDistOrd);
								//System.out.println("rateClgVal :"+rateClgVal);
							}
							
						}
						else
						{
							rateClgVal=Double.parseDouble(rateClg);
						}
						//commented by ritesh on 17/SEP/2014 as per instruction by manoj start
//						rateClg = null;
//						if (rateClg == null || rateClg.equals("") || Double.parseDouble(rateClg) == 0)
//						{
//							if (priceListClg != null && priceListClg.trim().length() > 0 )
//							{
//								rateClgVal = disCommon.pickRate(priceListClg, tranDate, itemCode, rs1.getString(1),"D",conn);
//								//System.out.println("rateClgVal :"+rateClgVal);
//							}
//							if (rateClgVal <= 0)
//							{
//								rateClgVal = rate2;
//								//System.out.println("rateClgVal :"+rateClgVal);
//							}
//							rateClg = Double.toString(rateClgVal);
//						}
						//commented by ritesh  end
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
						stmt1 = conn.createStatement();
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
						
						xmldetail2stock = new StringBuffer();
						
						isDetFound =true;
						linenoCtr =	linenoCtr + 1;
						countDtl++;//Added by manoj 15/01/2013 not to call if detail not found
						xmldetail2stock.append("<line_no><![CDATA["+(linenoCtr)+"]]></line_no>");
						xmldetail2stock.append("<item_descr>").append("<![CDATA[").append(itemDescr==null ? "" : itemDescr).append("]]>").append("</item_descr>\r\n");
						xmldetail2stock.append("<location_descr>").append("<![CDATA[").append(rs1.getString(23)).append("]]>").append("</location_descr>\r\n");
						xmldetail2stock.append("<unit>").append("<![CDATA[").append(rs1.getString(5)).append("]]>").append("</unit>\r\n");
						xmldetail2stock.append("<unit__alt>").append("<![CDATA[").append(unitAlt).append("]]>").append("</unit__alt>\r\n");
						xmldetail2stock.append("<conv__qty__alt>").append("<![CDATA[").append(lcFact).append("]]>").append("</conv__qty__alt>\r\n");
						xmldetail2stock.append("<qty_order__alt>").append("<![CDATA[").append(lcQtyOrderAlt).append("]]>").append("</qty_order__alt>\r\n");
						String tLocCode = null;
						tLocCode = rs1.getString(12);
						xmldetail2stock.append("<loc_code>").append("<![CDATA[").append( (tLocCode == null ? "" : tLocCode.trim()) ).append("]]>").append("</loc_code>\r\n");
						//commented for rajendra on 04/09/08 for pick up rate from stock
//						xmldetail2stock.append("<rate>").append("<![CDATA[").append(rate1).append("]]>").append("</rate>\r\n");
//						xmldetail2stock.append("<rate>").append("<![CDATA[").append(ratefromStock).append("]]>").append("</rate>\r\n");
						xmldetail2stock.append("<rate>").append("<![CDATA[").append(rateFmDistOrd).append("]]>").append("</rate>\r\n"); // CHANGED BY RITESH  ON 18/SEP/2014
						xmldetail2stock.append("<rate__clg>").append("<![CDATA[").append(rateClgVal).append("]]>").append("</rate__clg>\r\n");  // CHANGED BY RITESH  ON 17/SEP/2014
						xmldetail2stock.append("<quantity>").append("<![CDATA[").append(minputQty).append("]]>").append("</quantity>\r\n");
						xmldetail2stock.append("<amount>").append("<![CDATA[").append(minputQty*Double.parseDouble(rate1)).append("]]>").append("</amount>\r\n");
						String tLotSl = null;
						tLotSl = rs1.getString(2);
						xmldetail2stock.append("<lot_sl>").append("<![CDATA[").append( ( tLotSl == null ? "    " : tLotSl) ).append("]]>").append("</lot_sl>\r\n");
						xmldetail2stock.append("<pack_code>").append("<![CDATA[").append((rs1.getString(11) == null) ? "":rs1.getString(11)).append("]]>").append("</pack_code>\r\n");
						xmldetail2stock.append("<disc_amt>").append("<![CDATA[").append(disAmount).append("]]>").append("</disc_amt>\r\n");
						//xmldetail2stock.append("<tax_class>").append("<![CDATA[").append( ( taxClass == null ? "": taxClass ) ).append("]]>").append("</tax_class>\r\n");
						//xmldetail2stock.append("<tax_chap>").append("<![CDATA[").append( ( taxChap == null ? "": taxChap ) ).append("]]>").append("</tax_chap>\r\n");
						//xmldetail2stock.append("<tax_env>").append("<![CDATA[").append( ( taxEnv == null ? "": taxEnv ) ).append("]]>").append("</tax_env>\r\n");
						grossWt = Double.parseDouble(getFormatedValue(grossWt,3));
						//System.out.println("[DistIssueActEJB] Gross Wt=============>"+grossWt);
						xmldetail2stock.append("<gross_weight>").append("<![CDATA[").append(grossWt).append("]]>").append("</gross_weight>\r\n");
						netWt = Double.parseDouble(getFormatedValue(netWt,3));
						//System.out.println("[DistIssueActEJB] Net Wt=============>"+netWt);
						xmldetail2stock.append("<net_weight>").append("<![CDATA[").append(netWt).append("]]>").append("</net_weight>\r\n");
						tareWt = Double.parseDouble(getFormatedValue(netWt,3));
						//System.out.println("[DistIssueActEJB] Tare Wt=============>"+tareWt);
						xmldetail2stock.append("<tare_weight>").append("<![CDATA[").append(tareWt).append("]]>").append("</tare_weight>\r\n");
						xmldetail2stock.append("<pack_instr>").append("<![CDATA[").append((rs1.getString(21) == null) ? "":rs1.getString(21)).append("]]>").append("</pack_instr>\r\n"); //Gulzar 24/03/07
						xmldetail2stock.append("<retest_date>").append("<![CDATA[").append((rs1.getDate(19) == null) ? "":sdf.format(rs1.getDate(19))).append("]]>").append("</retest_date>\r\n");
						xmldetail2stock.append("<dimension>").append("<![CDATA[").append((rs1.getString(18) == null) ? "":rs1.getString(18)).append("]]>").append("</dimension>\r\n");
						xmldetail2stock.append("<supp_code__mfg>").append("<![CDATA[").append((rs1.getString(20) == null) ? "":rs1.getString(20)).append("]]>").append("</supp_code__mfg>\r\n"); //Gulzar 24/03/07
						xmldetail2stock.append("<site_code__mfg>").append("<![CDATA[").append((rs1.getString(7) == null) ? "":rs1.getString(7)).append("]]>").append("</site_code__mfg>\r\n");
						xmldetail2stock.append("<mfg_date>").append("<![CDATA[").append((rs1.getDate(8) == null) ? "":sdf.format(rs1.getDate(8))).append("]]>").append("</mfg_date>\r\n");
						xmldetail2stock.append("<exp_date>").append("<![CDATA[").append((rs1.getDate(4) == null) ? "":sdf.format(rs1.getDate(4))).append("]]>").append("</exp_date>\r\n");
						xmldetail2stock.append("<potency_perc>").append("<![CDATA[").append( ( (rs1.getString(9) == null) ? "": rs1.getString(9) ) ).append("]]>").append("</potency_perc>\r\n");
						xmldetail2stock.append("<no_art>").append("<![CDATA[").append(noArt).append("]]>").append("</no_art>\r\n");
						xmldetail2stock.append("<batch_no>").append("<![CDATA[").append( ( (rs1.getString(13) == null) ? "":rs1.getString(13) ) ).append("]]>").append("</batch_no>\r\n");
						xmldetail2stock.append("<grade>").append("<![CDATA[").append( ( (rs1.getString(14) == null) ? "": rs1.getString(14) ) ).append("]]>").append("</grade>\r\n");
						xmldetail2stock.append("<lot_no>").append("<![CDATA[").append(( (rs1.getString(1) == null) ? "               ": rs1.getString(1))).append("]]>").append("</lot_no>\r\n");
						detail2stock = xmldetail2stock.toString();
						xmldetail2stock = null;

						detail2xml.append(xmldetail2hdr1.toString());
						detail2xml.append(detail2stock);
						detail2xml.append(xmldetail2ftr.toString());
					}
						System.out.println("xmlString detail2......" + detail2xml.toString());
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

			}//end of while(rs.next())  
			xmlBuff = null;
			xmlBuff = new StringBuffer();
			xmldetail1 = createHeader(siteCode, distOrderNo, xtraParams,  conn);
			xmlBuff.append(xmldetail1.toString());
			xmlBuff.append(detail2xml.toString());
			xmlBuff.append("</Header0>");
			xmlBuff.append("</group0>");
			xmlBuff.append("</DocumentRoot>");
			xmlString = xmlBuff.toString();
			System.out.println("xmlString------"+xmlString);
			/* System.out.println("xmlString before serialization====>"+xmlString);
			Document detailDom = genericUtility.parseString(xmlString);
			System.out.println("detailDom.getFirstChild()=========>"+detailDom.getFirstChild());
			xmlString = serializeDom(detailDom.getFirstChild());
			System.out.println("xmlString after serialization......" + xmlString); */
			
			//System.out.println("xmlString......" + xmlString);
			System.out.println("Number of Records in Detail---"+countDtl);
			//if(countDtl>0)//Added by manoj 15/01/2013 not to call if detail not found
			System.out.println("xmlString.contains(Detail2)----"+xmlString.contains("Detail2"));
			if(xmlString.contains("Detail2"))
			{
				retString = distOrder.saveData(siteCode,xmlString,xtraParams,conn);//changes by Nandkumar Gadkari on 04/07/18 
	
				if (retString.indexOf("Success") > -1)
				{
					int  d =retString.indexOf("<TranID>");
					int f =	 retString.indexOf("</TranID>");
					String TranIDDistIssue = retString.substring(d+8,f);
					//System.out.println("TranIDDistIssue......... "+TranIDDistIssue);
					//System.out.println("Distribution Issue Generated Successfully");
					
					
					//added by kunal on 31/01/13 for cross update as per manoj instruction

					/*
					System.out.println("TranIDDistIssue = "+TranIDDistIssue);
					if(TranIDDistIssue != null && TranIDDistIssue.trim().length() > 0)
					{
						double amountDet = 0 ,discAmtDet = 0,netAmtDet = 0,taxAmtDet = 0;
						double quantityDet = 0,rateDet = 0 ,discountDet = 0;
						double discAmtHdr = 0,netAmtHdr = 0,taxAmtHdr = 0 ,amountHdr = 0,noArtHdr = 0, grossWtHdr = 0,tareWtHdr = 0,netWtHdr = 0 ;
						int lineNoDet = 0;
						String shipmentId = "";
						
						sql2 = " select shipment_id  from distord_iss  where tran_id = ?  ";
						pstmt2= conn.prepareStatement(sql2);
						pstmt2.setString(1, TranIDDistIssue);
						rs2 = pstmt2.executeQuery();
						if (rs2.next())
						{
							shipmentId = rs2.getString(1) == null? " ":rs2.getString(1);
						}
						rs2.close();
						rs2 = null;
						pstmt2.close();
						pstmt2 = null;
						
						/*
						sql2 = " select  quantity,rate ,discount, line_no , tax_amt from distord_issdet where tran_id = ?  order by line_no ";
						pstmt2= conn.prepareStatement(sql2);
						pstmt2.setString(1, TranIDDistIssue);
						rs2 = pstmt2.executeQuery();
						while (rs2.next())
						{
							quantityDet = rs2.getDouble("quantity");
							rateDet = rs2.getDouble("rate");
							discountDet = rs2.getDouble("discount");
							lineNoDet = rs2.getInt("line_no");
							taxAmtDet = rs2.getDouble("tax_amt");

							amountDet = quantityDet * rateDet ;
							discAmtDet = amountDet * (discountDet/100);
							netAmtDet = amount + taxAmtDet - discAmtDet ;

							sql = " update distord_issdet set amount = ? ,disc_amt = ? ,net_amt = ?  where tran_id = ?  and  line_no = ?   ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setDouble(1,amountDet );
							pstmt.setDouble(2,discAmtDet );
							pstmt.setDouble(3,netAmtDet );
							pstmt.setString(4,TranIDDistIssue );
							pstmt.setInt(5,lineNoDet );
							cnt = pstmt.executeUpdate();
							pstmt.close();
							pstmt = null;

							if( cnt > 0)
							{
								System.out.println("distord_issdet update successfully ["+cnt+"]");
							}

						}
						rs2.close();
						rs2 = null;
						pstmt2.close();
						pstmt2 = null;
						
						sql = "update distord_issdet  set "
								+"	amount = quantity * rate , "
								+"	disc_amt = quantity * rate * discount/100 ,  " 
								+ "	net_amt = quantity * rate  + tax_amt - (quantity * rate * discount/100 )  "
								+ " where  tran_id = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,TranIDDistIssue );
						cnt = pstmt.executeUpdate();
						pstmt.close();
						pstmt = null;

						if( cnt > 0)
						{
							System.out.println("distord_issdet update successfully ["+cnt+"]");
						}

						sql2 = " select sum(tax_amt),sum(amount),sum(net_amt),sum(disc_amt),sum( no_art)  ,sum(gross_weight),sum(tare_weight),sum(net_weight ) from distord_issdet where  tran_id = ?  ";
						pstmt2= conn.prepareStatement(sql2);
						pstmt2.setString(1, TranIDDistIssue);
						rs2 = pstmt2.executeQuery();
						if (rs2.next())
						{
							taxAmtHdr = rs2.getDouble(1);
							amountHdr = rs2.getDouble(2);
							netAmtHdr = rs2.getDouble(3);
							discAmtHdr = rs2.getDouble(4);
							noArtHdr = rs2.getDouble(5);
							grossWtHdr = rs2.getDouble(6);
							tareWtHdr = rs2.getDouble(7);
							netWtHdr = rs2.getDouble(8);
						}
						rs2.close();
						rs2 = null;
						pstmt2.close();
						pstmt2 = null;
						if(shipmentId != null && shipmentId.trim().length() > 0)
						{
							grossWtHdr = tareWtHdr = netWtHdr = 0;
						}

						sql = " update distord_iss set tax_amt = ? , amount = ? ,net_amt = ? ,discount = ? ,no_art = ? ,gross_weight = ? ,tare_weight = ? ,net_weight = ?  where tran_id = ?   ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setDouble(1,taxAmtHdr );
						pstmt.setDouble(2,amountHdr);
						pstmt.setDouble(3,netAmtHdr);
						pstmt.setDouble(4,discAmtHdr );
						pstmt.setDouble(5,noArtHdr);
						pstmt.setDouble(6,grossWtHdr);
						pstmt.setDouble(7,tareWtHdr);
						pstmt.setDouble(8,netWtHdr);
						pstmt.setString(9,TranIDDistIssue );
						cnt = pstmt.executeUpdate();
						pstmt.close();
						pstmt = null;
						if( cnt > 0)
						{
							System.out.println("distord_iss update successfully ["+cnt+"]");
						}
					}
					//added by kunal on 31/01/13 for cross update as per manoj instruction  end 
					*/
						if( isAutoConfirm != null 
									&& isAutoConfirm.trim().length() > 0 
									&& isAutoConfirm.equalsIgnoreCase( "Y" )  )
						{						
							DistIssueConfirmAct confActEjb = new DistIssueConfirmAct();
		
							retString = confActEjb.actionConfirm( TranIDDistIssue, xtraParams, "true", conn );
							System.out.println("After confri :: " + retString );
							confActEjb = null;
						} 
					
				}
				else
				{
					//System.out.println("Distribution Issue not Generated ");
					//msalam on 220609 for showing actual message coming from saveData
					//retString="ERROR";
					//end msalam on 220609
				}
			}
			else
			{
				
				//retString="NODETAILEXIST"+distOrderNo;
			}
			/*if(!isfound)
			{
				retString="PARTIALISS"+distOrderNo;
			}*/
		}
		catch(Exception e)
		{
			e.printStackTrace();
			//System.out.println("returning in createdistiss ::  "+retString);
			retString="ERROR";
			throw new ITMException(e);
		}
		//System.out.println("retString..   ....... "+retString);
		return retString;
	}
	
	/* private String serializeDom(Node dom) throws Exception
    {
        String retString = null;
        try
        {
            System.out.println("******* Serializing detailDom *****");
			ByteArrayOutputStream out = new ByteArrayOutputStream();
            Transformer serializer = TransformerFactory.newInstance().newTransformer();
            serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,"yes");
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
    } */
	
	public String createHeader(String siteCode,String distOrderNo,String xtraParams, Connection conn)throws RemoteException,ITMException
	{
		/*StringBuffer xmlBuff = new StringBuffer();
		StringBuffer detail2xml = new StringBuffer();
		StringBuffer xmldetail2ftr = new StringBuffer();
		xmldetail2ftr.append("</Detail2>"); */
		StringBuffer xmlBuffHeader = new StringBuffer();
	//	String distOrderNo = null;
		String sql = null;
		String tranDate = null;
		String locCode = null;
		String retString = "";
		PreparedStatement pstmt = null,pstmt1 = null,pstmt2 =null;
		ResultSet rs = null,rs1 =null,rs2 = null;
		//Connection conn = null;
		String lsPackCode = null,unitAlt = null,unit = null,mLotNo = null;
		int count = 0;
		int linenoCtr = 0;
		String 	tranType ="";
		double mod = 0d, minputQty = 0d, remQuantity = 0d, stockQty = 0d, integralQty = 0d;
		double grossPer = 0d,netPer = 0d,tarePer = 0d, grossWt = 0d, netWt = 0d, tareWt =0d, rateClgVal = 0d, rate2 = 0d;
		double disAmount = 0d, amount = 0d, shipperQty = 0d,discount =0;
		int  minShelfLife = 0, noArt1 = 0, cnt = 0;
		int mLineNoDist =0;
		double qtyConfirm =0,qtyShipped =0,lcQtyOrderAlt =0,lcFact =0;
		String suppSour = "", trackShelfLife = "", siteCodeMfg = "", sundryCode = "", potencyPerc = "";
		String priceList = "", tabValue = "", priceListClg = "", chkDate = "", disCountPer = "";
		String tranTypePparent = null,rate ="",lsTranTypeParent ="";
		String qtyOrdAlt = "",convQtyAlt = "";
		String res = "", locCodeDamaged = "",itemCode ="",availableYn ="";
		String checkIntegralQty = "", rate1 = "",tranTypeParent ="",quantity = "";
		String active = "", itemDescr = "",errCode ="",sql2 ="",noArt ="";
		String detail2stock ="",errString ="",taxChap ="",siteCodeShip ="";
		String lotNo ="",lotSl ="",packCode ="",rateClg ="",taxEnv ="",taxClass ="";
		java.util.Date expDate = null,mfgDate = null, chkDate1 = null;
		boolean isDetFound = false;
		
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			if (conn == null)
			{
				ConnDriver connDriver = new ConnDriver();
				conn = connDriver.getConnectDB("DriverITM");
				connDriver = null;
				conn.setAutoCommit( false ); //  added 18/06/09 manoharan
			}
			//System.out.println( "xtraParams :: " + xtraParams ); 
			String userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			String chgUser = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginCode" );
			String chgTerm = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "chgTerm" );

		/*	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
			DistCommon disCommon =new DistCommon();
			ByteArrayInputStream bais = new ByteArrayInputStream(xmlString.getBytes());
			Document dom = (DocumentBuilderFactory.newInstance()).newDocumentBuilder().parse(bais);
			NodeList parent = dom.getElementsByTagName("TranID");
			distOrderNo = parent.item(0).getFirstChild().getNodeValue(); */
			tranDate = getCurrdateAppFormat() ;
			//System.out.println("[CreateDistIssue]------------Distribution Issue is created for Dist Order------------->"+distOrderNo);

			sql="SELECT D.DIST_ORDER AS DIST_ORDER,D.ORDER_DATE AS ORDER_DATE,D.SITE_CODE__SHIP AS SITE_CODE__SHIP,D.SITE_CODE__DLV AS SITE_CODE__DLV,D.SHIP_DATE AS SHIP_DATE,D.DUE_DATE AS DUE_DATE,D.REMARKS AS REMARKS,"
			+" D.DIST_ROUTE AS DIST_ROUTE,D.PRICE_LIST AS PRICE_LIST,D.CONFIRMED AS CONFIRMED,D.CHG_USER AS CHG_USER,D.CHG_TERM AS CHG_TERM,D.TARGET_WGT AS TARGET_WGT,D.TARGET_VOL AS TARGET_VOL,D.LOC_CODE__GIT AS LOC_CODE__GIT,"
			+" D.CHG_DATE AS CHG_DATE,SITE_A.DESCR AS SITEA_DESCR,SITE_B.DESCR AS SITEB_DESCR,LOCATION.DESCR AS LOCATION_DESCR,D.CONF_DATE AS CONF_DATE,D.SITE_CODE AS SITE_CODE,D.STATUS AS STATUS,D.SALE_ORDER AS SALE_ORDER,"
			+" D.REMARKS1 AS REMARK1,D.REMARKS2 AS REMARK2,TRIM(D.ORDER_TYPE) AS ORDER_TYPE,SITE_A.ADD1 AS SITEA_ADD1,SITE_A.ADD2 AS SITEA_ADD2,SITE_A.CITY AS SITEA_CITY,SITE_A.PIN AS SITEA_PIN,SITE_A.STATE_CODE AS SITEA_STATE_CODE,"
			+" SITE_B.ADD1 AS SITEB_ADD1,SITE_B.ADD2 AS SITEB_ADD2,SITE_B.CITY AS SITEB_CITY,SITE_B.PIN AS SITEB_PIN,SITE_B.STATE_CODE AS SITEB_STATE_CODE,D.LOC_CODE__CONS AS LOC_CODE__CONS,D.SUNDRY_TYPE AS SUNDRY_TYPE,"
			+" D.SUNDRY_CODE AS SUNDRY_CODE,D.AUTO_RECEIPT AS AUTO_RECEIPT,D.TRAN_TYPE AS TRAN_TYPE,D.CURR_CODE AS CURR_CODE,D.EXCH_RATE AS EXCH_RATE,D.SALES_PERS AS SALES_PERS,SALES_PERS.SP_NAME AS SP_NAME,"
			+" D.LOC_CODE__GITBF AS LOC_CODE__GITBF,D.CUST_CODE__DLV AS CUST_CODE__DLV,D.DLV_TO AS DLV_TO,D.DLV_ADD1 AS DLV_ADD1,D.DLV_ADD2 AS DLV_ADD2,D.DLV_ADD3 AS DLV_ADD3,D.DLV_CITY AS DLV_CITY,"
			+" D.STATE_CODE__DLV AS STATE_CODE__DLV,D.COUNT_CODE__DLV AS COUNT_CODE__DLV,D.DLV_PIN AS DLV_PIN,D.STAN_CODE AS STAN_CODE,D.TEL1__DLV AS TEL1__DLV,D.TEL2__DLV AS TEL2__DLV,D.TEL3__DLV AS TEL3__DLV,"
			+" D.FAX__DLV AS FAX__DLV,D.AVALIABLE_YN AS AVALIABLE_YN,D.PURC_ORDER AS PURC_ORDER,D.TOT_AMT AS TOT_AMT,D.TAX_AMT AS TAX_AMT,D.NET_AMT AS NET_AMT,D.TRAN_SER AS TRAN_SER,"
			+" D.PRICE_LIST__CLG AS PRICE_LIST__CLG,SPACE(25) AS LOC,FN_SUNDRY_NAME(D.SUNDRY_TYPE,D.SUNDRY_CODE,'N') AS SUNDRY_NAME,"
			+" D.PROJ_CODE AS PROJ_CODE,SITE_C.DESCR AS SITEC_DESCR,D.POLICY_NO AS POLICY_NO,D.LOC_CODE__DAMAGED AS LOC_CODE__DAMAGED,D.SITE_CODE__BIL AS SITE_CODE__BIL,SITE_D.DESCR AS SITED_DESCR,SITE_D.ADD1 AS SITED_ADD1,"
			+" SITE_D.ADD2 AS SITED_ADD2,SITE_D.CITY AS SITED_CITY,SITE_D.PIN SITED_PIN ,SITE_D.STATE_CODE AS SITED_STATE_CODE,D.TRANS_MODE AS TRANS_MODE"
			+"  FROM DISTORDER  D,SITE SITE_A,SITE SITE_B,LOCATION  LOCATION,SALES_PERS  SALES_PERS,SITE SITE_C,SITE SITE_D "
			+"  WHERE ( D.SITE_CODE__SHIP      = SITE_A.SITE_CODE  ) AND "
			+" ( D.SITE_CODE__DLV      = SITE_B.SITE_CODE (+)  ) AND "
			+" ( D.LOC_CODE__GIT      = LOCATION.LOC_CODE (+)  ) AND "
			+" ( D.SITE_CODE      = SITE_C.SITE_CODE (+)  ) AND "
			+" ( D.SALES_PERS=SALES_PERS.SALES_PERS(+)) AND "
			+" ( D.SITE_CODE__BIL=SITE_D.SITE_CODE(+)) "
			+"  AND ( ( D.DIST_ORDER    = '"+distOrderNo+"' ) ) ";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				this.currCode = rs.getString("CURR_CODE");
				locCode = rs.getString("LOC_CODE__GIT");
				tranType = rs.getString("TRAN_TYPE");
				siteCodeShip = rs.getString("SITE_CODE__SHIP");
				xmlBuffHeader.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
				xmlBuffHeader.append("<DocumentRoot>");
				xmlBuffHeader.append("<description>").append("Datawindow Root").append("</description>");
				xmlBuffHeader.append("<group0>");
				xmlBuffHeader.append("<description>").append("Group0 description").append("</description>");
				xmlBuffHeader.append("<Header0>");
				
				
				
				/* //obj_name changed by msalam on 18/06/09 as per instruction from Manoharan sir. start
				// from dist_issue to dist_issue_prc
				//xmlBuffHeader.append("<objName><![CDATA[").append("dist_issue").append("]]></objName>");
				xmlBuffHeader.append("<objName><![CDATA[").append("dist_issue_prc").append("]]></objName>");
				//obj_name changed by msalam on 18/06/09 as per instruction from Manoharan sir. end */
				
				
				
				
				//obj_name again changed to dist_issue on 03/02/10 as instructed by Manoharan sir.---START
//				xmlBuffHeader.append("<objName><![CDATA[").append("dist_issue_dummy").append("]]></objName>");
				//xmlBuffHeader.append("<objName><![CDATA[").append("dist_issue").append("]]></objName>");
				xmlBuffHeader.append("<objName><![CDATA[").append("dist_order_post").append("]]></objName>");//change done by kunal on 31/01/13
				//obj_name again changed to dist_issue on 03/02/10 as instructed by Manoharan sir.---END
				
				
				xmlBuffHeader.append("<pageContext><![CDATA[").append("1").append("]]></pageContext>");
				xmlBuffHeader.append("<objContext><![CDATA[").append("1").append("]]></objContext>");
				xmlBuffHeader.append("<editFlag><![CDATA[").append("A").append("]]></editFlag>");
				xmlBuffHeader.append("<focusedColumn><![CDATA[").append("").append("]]></focusedColumn>");
				xmlBuffHeader.append("<action><![CDATA[").append("SAVE").append("]]></action>");
				xmlBuffHeader.append("<elementName><![CDATA[").append("").append("]]></elementName>");
				xmlBuffHeader.append("<keyValue><![CDATA[").append("1").append("]]></keyValue>");
				xmlBuffHeader.append("<taxKeyValue><![CDATA[").append("").append("]]></taxKeyValue>");
				xmlBuffHeader.append("<saveLevel><![CDATA[").append("1").append("]]></saveLevel>");
				xmlBuffHeader.append("<forcedSave><![CDATA[").append(true).append("]]></forcedSave>");
				xmlBuffHeader.append("<taxInFocus><![CDATA[").append(false).append("]]></taxInFocus>");
				xmlBuffHeader.append("<description>").append("Header0 members").append("</description>");

				
				
				
				
				/* //obj_name changed by msalam on 18/06/09 as per instruction from Manoharan sir. start
				//xmlBuffHeader.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"dist_issue\" objContext=\"1\">");
				xmlBuffHeader.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"dist_issue_prc\" objContext=\"1\">");
				//obj_name changed by msalam on 18/06/09 as per instruction from Manoharan sir. end */
				
				//obj_name again changed to dist_issue on 03/02/10 as instructed by Manoharan sir.---START
				//xmlBuffHeader.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"dist_issue\" objContext=\"1\">");
				//obj_name again changed to dist_issue on 03/02/10 as instructed by Manoharan sir.---END

				// 27/04/10 manoharan  changed to bypass validation
//				xmlBuffHeader.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"dist_issue_dummy\" objContext=\"1\">");
				//xmlBuffHeader.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"dist_issue\" objContext=\"1\">");
				xmlBuffHeader.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"dist_order_post\" objContext=\"1\">"); //change done by kunal 31/01/13
				// 27/04/10 manoharan  changed to bypass validation
				
				
				xmlBuffHeader.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
				xmlBuffHeader.append("<tran_id/>");
				//tranDate = sdf.format(new Timestamp(System.currentTimeMillis()));
				xmlBuffHeader.append("<tran_date><![CDATA["+tranDate+"]]></tran_date>");
				xmlBuffHeader.append("<eff_date><![CDATA["+tranDate+"]]></eff_date>");
				xmlBuffHeader.append("<dist_order><![CDATA["+distOrderNo+"]]></dist_order>");
				//System.out.println("SITE_CODE__SHIP------------>"+rs.getString("SITE_CODE__SHIP"));
				xmlBuffHeader.append("<site_code><![CDATA["+(rs.getString("SITE_CODE__SHIP")==null?"":rs.getString("SITE_CODE__SHIP").trim())+"]]></site_code>");
				xmlBuffHeader.append("<site_code__dlv><![CDATA["+(rs.getString("SITE_CODE__DLV")==null?"":rs.getString("SITE_CODE__DLV").trim())+"]]></site_code__dlv>");
				xmlBuffHeader.append("<dist_route><![CDATA["+(rs.getString("DIST_ROUTE")==null?"":rs.getString("DIST_ROUTE"))+"]]></dist_route>");
				xmlBuffHeader.append("<tran_code><![CDATA[]]></tran_code>");
				xmlBuffHeader.append("<lr_no><![CDATA[]]></lr_no>");
				xmlBuffHeader.append("<lr_date><![CDATA[]]></lr_date>");
				xmlBuffHeader.append("<lorry_no><![CDATA[]]></lorry_no>");
				xmlBuffHeader.append("<gross_weight><![CDATA[0]]></gross_weight>");
				xmlBuffHeader.append("<tare_weight><![CDATA[0]]></tare_weight>");
				xmlBuffHeader.append("<net_weight><![CDATA[0]]></net_weight>");
				xmlBuffHeader.append("<frt_amt><![CDATA[0]]></frt_amt>");
				xmlBuffHeader.append("<amount><![CDATA[0]]></amount>");
				xmlBuffHeader.append("<tax_amt><![CDATA[0]]></tax_amt>");
				xmlBuffHeader.append("<net_amt><![CDATA[0]]></net_amt>");
				xmlBuffHeader.append("<remarks><![CDATA[]]></remarks>");
				xmlBuffHeader.append("<frt_type><![CDATA[T]]></frt_type>");
				
				//xmlBuffHeader.append("<chg_user><![CDATA["+(rs.getString("CHG_USER")==null?"":rs.getString("CHG_USER").trim())+"]]></chg_user>");
				xmlBuffHeader.append("<chg_user><![CDATA["+ userId +"]]></chg_user>");
				//xmlBuffHeader.append("<chg_term><![CDATA["+(rs.getString("CHG_TERM")==null?"":rs.getString("CHG_TERM").trim())+"]]></chg_term>");
				xmlBuffHeader.append("<chg_term><![CDATA["+ chgTerm +"]]></chg_term>");
				xmlBuffHeader.append("<curr_code><![CDATA[" + this.currCode + "]]></curr_code>");
				xmlBuffHeader.append("<chg_date><![CDATA["+tranDate+"]]></chg_date>");
				xmlBuffHeader.append("<site_descr><![CDATA["+(rs.getString("SITEA_DESCR")==null?"":rs.getString("SITEA_DESCR").trim())+"]]></site_descr>");
				xmlBuffHeader.append("<site_to_descr><![CDATA["+(rs.getString("SITEB_DESCR")==null?"":rs.getString("SITEB_DESCR").trim())+"]]></site_to_descr>");
				xmlBuffHeader.append("<location_descr><![CDATA["+(rs.getString("LOCATION_DESCR")==null?"":rs.getString("LOCATION_DESCR").trim())+"]]></location_descr>");
				xmlBuffHeader.append("<tran_name><![CDATA[]]></tran_name>");
				xmlBuffHeader.append("<currency_descr><![CDATA[]]></currency_descr>");
				xmlBuffHeader.append("<confirmed><![CDATA[" + isAutoConfirm + "]]></confirmed>");
				xmlBuffHeader.append("<loc_code__git><![CDATA["+(rs.getString("LOC_CODE__GITBF")==null?"":rs.getString("LOC_CODE__GITBF"))+"]]></loc_code__git>");
				xmlBuffHeader.append("<conf_date><![CDATA["+sdf.format(rs.getTimestamp("CONF_DATE"))+"]]></conf_date>");
				xmlBuffHeader.append("<no_art><![CDATA[0]]></no_art>");
				xmlBuffHeader.append("<trans_mode><![CDATA["+(rs.getString("TRANS_MODE")==null?"":rs.getString("TRANS_MODE").trim())+"]]></trans_mode>");
				xmlBuffHeader.append("<gp_no><![CDATA[]]></gp_no>");
				xmlBuffHeader.append("<gp_date/>");
				xmlBuffHeader.append("<conf_passwd/>");

				xmlBuffHeader.append("<order_type><![CDATA["+(rs.getString("ORDER_TYPE")==null?"":rs.getString("ORDER_TYPE").trim())+"]]></order_type>");
				xmlBuffHeader.append("<gp_ser><![CDATA[I]]></gp_ser>");
				xmlBuffHeader.append("<ref_no><![CDATA[]]></ref_no>");
				xmlBuffHeader.append("<ref_date><![CDATA[]]></ref_date>");
				//xmlBuffHeader.append("<available_yn><![CDATA[Y]]></available_yn>");
				xmlBuffHeader.append("<available_yn><![CDATA["+(rs.getString("AVALIABLE_YN")==null?"N":rs.getString("AVALIABLE_YN").trim())+"]]></available_yn>");
				xmlBuffHeader.append("<site_add1><![CDATA["+(rs.getString("SITEA_ADD1")==null?"":rs.getString("SITEA_ADD1").trim())+"]]></site_add1>");
				xmlBuffHeader.append("<site_add2><![CDATA["+(rs.getString("SITEA_ADD2")==null?"":rs.getString("SITEA_ADD2").trim())+"]]></site_add2>");
				xmlBuffHeader.append("<site_city><![CDATA["+(rs.getString("SITEA_CITY")==null?"":rs.getString("SITEA_CITY").trim())+"]]></site_city>");
				xmlBuffHeader.append("<site_pin><![CDATA["+(rs.getString("SITEA_PIN")==null?"":rs.getString("SITEA_PIN").trim())+"]]></site_pin>");
				xmlBuffHeader.append("<site_state_code><![CDATA["+(rs.getString("SITEA_STATE_CODE")==null?"":rs.getString("SITEA_STATE_CODE").trim())+"]]></site_state_code>");
				xmlBuffHeader.append("<exch_rate><![CDATA["+(rs.getDouble("EXCH_RATE"))+"  ]]></exch_rate>");
				xmlBuffHeader.append("<tran_type><![CDATA["+(rs.getString("TRAN_TYPE")==null?"":rs.getString("TRAN_TYPE").trim())+"]]></tran_type>");
				xmlBuffHeader.append("<emp_code__aprv><![CDATA[]]></emp_code__aprv>");
				xmlBuffHeader.append("<discount><![CDATA[0]]></discount>");
				xmlBuffHeader.append("<permit_no><![CDATA[]]></permit_no>");
				xmlBuffHeader.append("<shipment_id><![CDATA[]]></shipment_id>");
				xmlBuffHeader.append("<curr_code__frt><![CDATA[" + this.currCode + "]]></curr_code__frt>");
				xmlBuffHeader.append("<exch_rate__frt><![CDATA[]]></exch_rate__frt>");
				xmlBuffHeader.append("<currency_descr__frt><![CDATA[]]></currency_descr__frt>");
				xmlBuffHeader.append("<rd_permit_no><![CDATA[]]></rd_permit_no>");
				xmlBuffHeader.append("<dc_no><![CDATA[]]></dc_no>");
				xmlBuffHeader.append("<tran_ser><![CDATA[D-ISS ]]></tran_ser>");
				xmlBuffHeader.append("<part_qty><![CDATA[A]]></part_qty>");
				xmlBuffHeader.append("<sundry_details><![CDATA[]]></sundry_details>");
				xmlBuffHeader.append("<sundry_name><![CDATA["+(rs.getString("SUNDRY_NAME")==null?"":rs.getString("SUNDRY_NAME"))+"]]></sundry_name>");
				xmlBuffHeader.append("<proj_code><![CDATA["+(rs.getString("PROJ_CODE")==null?"":rs.getString("PROJ_CODE").trim())+"]]></proj_code>");
				xmlBuffHeader.append("<site_tele1><![CDATA[]]></site_tele1>");
				xmlBuffHeader.append("<site_tele2><![CDATA[]]></site_tele2>");
				xmlBuffHeader.append("<site_tele3><![CDATA[]]></site_tele3>");
				xmlBuffHeader.append("<site_code__bil><![CDATA[]]></site_code__bil>");
				xmlBuffHeader.append("<site_descr_bill><![CDATA[]]></site_descr_bill>");
				xmlBuffHeader.append("<site_add1_bill><![CDATA[]]></site_add1_bill>");
				xmlBuffHeader.append("<site_add2_bill><![CDATA[]]></site_add2_bill>");
				xmlBuffHeader.append("<site_city_bill><![CDATA[]]></site_city_bill>");
				xmlBuffHeader.append("<site_pin_bill><![CDATA[]]></site_pin_bill>");
				xmlBuffHeader.append("<site_state_code_bill><![CDATA[]]></site_state_code_bill>");
				xmlBuffHeader.append("<pallet_wt><![CDATA[]]></pallet_wt>");
				xmlBuffHeader.append("<auto_receipt><![CDATA[N]]></auto_receipt>");
				xmlBuffHeader.append("</Detail1>");
			}//end of if(rs.next())
			//added 18/06/09 manoharan
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			//end added 18/06/09 manoharan
			retString = xmlBuffHeader.toString();
			System.out.println("Chandni retString is::"+retString);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			retString="ERROR";
			throw new ITMException(e);
		}
		//System.out.println("retString..   header....... "+retString);
		return retString;
	}
	 private String getCurrdateAppFormat()
    {
        String s = "";
        //GenericUtility genericUtility = GenericUtility.getInstance();
        E12GenericUtility genericUtility = new E12GenericUtility();
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
        }
        return s;
    }
	private String getDetails(String mSiteCode,int mLineNoDist,String mDistOrder,String lsTranType,Connection conn)throws RemoteException,ITMException
	{//this method will return xml data
		String mItemCode = null,mTaxClass=null,mTaxChap=null,mTaxEnv = null;
		String lsUnitAlt = null,lsUnit = null,lsPackInstr =null;
		String iItemCode ="",mPackCode=null,unit ="";
		double mQty= 0,lcQty= 0,mRate = 0,mDiscount =0,lcRateClg=0,lcConvQtyAlt=0;

		String sql = null,lsTranTypeParent ="";
		ResultSet rs = null,rs1 =null;
		PreparedStatement pstmt = null,pstmt1 =null;
		StringBuffer detail2hdr = new StringBuffer("");
		String mLotNo = null;
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
				lcRateClg = rs.getDouble("rate__clg");
				lsUnitAlt = rs.getString("UNIT__ALT")==null?"":rs.getString("UNIT__ALT");
				unit = rs.getString("UNIT")==null?"":rs.getString("UNIT");
				lcConvQtyAlt = rs.getDouble("CONV__QTY__ALT");
				lsPackInstr = rs.getString("pack_instr")==null?"":rs.getString("pack_instr");

				if(lsUnitAlt.trim().length() == 0)
				{
					lsUnitAlt = lsUnit;
					lcConvQtyAlt = 1;
				}
				
				detail2hdr.append("<item_code><![CDATA["+mItemCode+"]]></item_code>");
				detail2hdr.append("<unit><![CDATA["+lsUnit+"]]></unit>");
				detail2hdr.append("<unit__alt><![CDATA["+lsUnitAlt+"]]></unit__alt>");
				detail2hdr.append("<pack_instr><![CDATA["+lsPackInstr+"]]></pack_instr>");

				/* sql="Select pack_code from item where item_code = '"+mItemCode+"'";
				
				pstmt1= conn.prepareStatement(sql);
				//pstmt1.setString(1,mItemCode);
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{
					mPackCode = rs1.getString("pack_code")==null?"":rs1.getString("pack_code");
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;

				detail2hdr.append("<pack_code><![CDATA["+mPackCode+"]]></pack_code>"); */

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
					detail2hdr.append("<quantity>"+lcQty+"</quantity>");
					detail2hdr.append("<qty_order__alt>"+lcQty+"</qty_order__alt>");
					lcQty = lcQty;
				}
				else
				{
					detail2hdr.append("<quantity>"+mQty+"</quantity>");
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
		}
		System.out.println("chandni inside detail::"+detail2hdr.toString());
		return detail2hdr.toString();
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
	/* private double getIntegralQty(String siteCode, String itemCode, String lotNo, String packCode,Connection conn)
	{
		double integralQty = 0;
		String sql = "";
		ResultSet rs = null;
		Statement stmt = null;
		try
		{
			//conn = connDriver.getConnectDB("DriverITM");
			stmt = conn.createStatement();
			sql ="SELECT CASE WHEN SHIPPER_SIZE IS NULL THEN 0 ELSE SHIPPER_SIZE END "
				+"FROM ITEM_LOT_PACKSIZE "
				+"WHERE ITEM_CODE = '"+itemCode+"' "
				+"AND LOT_NO__FROM <= '"+lotNo+"' "
				+"AND LOT_NO__TO   >= '"+lotNo+"' ";
			//System.out.println("sql :"+sql);
			rs = stmt.executeQuery(sql);
			if (rs.next())
			{
				integralQty = rs.getDouble(1);
				//System.out.println("integralQty :"+integralQty);
			}
			// added 18/06/09 manoharan
			rs.close();
			rs = null;
			//end added 18/06/09 manoharan
			if (integralQty == 0)
			{
				sql = "SELECT CASE WHEN CAPACITY IS NULL THEN 0 ELSE CAPACITY END "
					 +"FROM PACKING WHERE PACK_CODE = '"+packCode+"'";
				//System.out.println("sql :"+sql);
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					integralQty = rs.getDouble(1);
					//System.out.println("integralQty :"+integralQty);
				}
				//added 18/06/09 manoharan
				rs.close();
				rs = null;
				//end added 18/06/09 manoharan
				if (integralQty == 0)
				{
					sql = "SELECT INTEGRAL_QTY FROM SITEITEM "
						 +"WHERE SITE_CODE = '"+siteCode+"'"+" AND ITEM_CODE = '"+itemCode+"'";
					//System.out.println("sql :"+sql);
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						integralQty = rs.getDouble(1);
						//System.out.println("integralQty :"+integralQty);
					}
					//added 18/06/09 manoharan
					rs.close();
					rs = null;
					//end added 18/06/09 manoharan
					if (integralQty == 0)
					{
						sql = "SELECT INTEGRAL_QTY FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
						//System.out.println("sql :"+sql);
						rs = stmt.executeQuery(sql);
						if (rs.next())
						{
							 integralQty = rs.getDouble(1);
							//System.out.println("integralQty :"+integralQty);
						}
						//added 18/06/09 manoharan
						rs.close();
						rs = null;
						//end added 18/06/09 manoharan
					}
				}
			}
			// added 18/06/09 manoharan
			stmt.close();
			stmt = null;
			// end added 18/06/09 manoharan
		}
		catch(Exception e)
		{
			//System.out.println("the exception occurs in getIntegralQty :"+e);
		}
		//System.out.println("integralQty :"+integralQty);
		return integralQty;
	} */
	private double getIntegralQty(String siteCode, String itemCode, String lotNo, String packCode, String checkIntegralQty)
	{
		double integralQty = 0;
		String sql = "";
		ResultSet rs = null;
		Connection conn = null;
		ConnDriver connDriver = new ConnDriver();
		Statement stmt = null;
		try
		{
			conn = connDriver.getConnectDB("DriverITM");
	
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
		}
		System.out.println("integralQty :"+integralQty);
		return integralQty;
	}
	
	private String calcExpiry(String tranDate, int months)
	{
		java.util.Date expDate = new java.util.Date();
		java.util.Date retDate = new java.util.Date();
		String retStrInDate = "";
		//System.out.println("tranDate :"+tranDate+"\nmonths :"+months);
		try
		{
			//GenericUtility genericUtility = GenericUtility.getInstance();
			E12GenericUtility genericUtility = new E12GenericUtility();
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
		}
		//System.out.println("retStrInDate :"+retStrInDate);
		return retStrInDate;
	}
	
	private int getNoArt(String siteCode, String custCode, String itemCode, String packCode, double qty, char type, double shipperQty, double integralQty1)
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
				conn = connDriver.getConnectDB("DriverITM");
			
			
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
		}
		System.out.println("(int)noArt :"+(int)noArt);
		return (int)noArt;
	}
}
