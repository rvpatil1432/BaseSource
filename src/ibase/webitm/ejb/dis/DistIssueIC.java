package ibase.webitm.ejb.dis;

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

import javax.ejb.Stateless;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Session Bean implementation class DistIssueIC
 */
@Stateless
public class DistIssueIC extends ValidatorEJB {
	E12GenericUtility genericUtility = new E12GenericUtility();
	FinCommon finCommon = new FinCommon();
	DistCommon distCommon = new DistCommon();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();	
	
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		//Commented by Jaffar S. as suggested by Piyush Sir because it takes 4-6 second on each item change [Start- 15-11-18]
		/*System.out.println("xmlString............."+xmlString+"\n\n");
		System.out.println("xmlString1............"+xmlString1+"\n\n");
		System.out.println("xmlString2............"+xmlString2+"\n\n");*/
		//Commented by Jaffar S. as suggested by Piyush Sir because it takes 4-6 second on each item change [End- 15-11-18]
		
		try
		{   
			if(xmlString != null && xmlString.trim().length() > 0)
			{
				dom = parseString(xmlString);
			}
			if(xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom1 = parseString(xmlString1);
			}
			if(xmlString2 != null && xmlString2.trim().length() > 0)
			{
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [DistIssueIC][itemChanged( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e);
		}
		return valueXmlString;
	}


	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{		
		System.out.println(" ---- INSIDE DIST ISSUE ITEMCHANGHED ------");				
		String childNodeName = null;
		String sql = "";
		StringBuffer valueXmlString = new StringBuffer();
		int ctr = 0;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null ;	
		int currentFormNo =0;
		String columnValue="";
		int cnt=0;
		String reStr="";
		String lsLoginSite = "";
		String finEntity = "";
		String mCurrCode = "";
		String mCurr = "";
		String varValue = "";
		String mloc = "";
		String mCode = "";			
		String msiteCode = "";
		String msite = "";
		String tranSer = "D-ISS";
		String msiteToDescr = "";
		String msiteTo = "";
		String lsSiteCode = "";
		String mSiteCodeMfg = "";
		String lsSiteCodeBil = "";
		String siteToTel1 = ""; 
		String siteToTel2 = "";
		String siteToTel3 = "";
		String locCode="";
		String mLocCodeGit="";
		String lotNo="";
		String loc="";
		String lotSl="";
		String priceListClg = "";
		String priceListParent = "";
		String priceList = "";
		String listType = "";
		String mdistRoute = ""; 		
		String saleOrder = "";
		String tranCode = "";
		String tranName = "";
		String tranType = "";
		String currCode = "";
		String mCurrDescr = "";
		String currCode1 = "";
		String orderType = "";
		String mDistOrder = "";
		String distOrderType = "";
		String distOrder = "";
		String distIss = "";
		String gpSer = ""; 
		String addr1 = "";
		String addr2 = "";
		String city = "";
		String pinCode = "";
		String stateCode = "";
		String sundryDetails = "";
		String sundryType = "";
		String sundryCode = "";
		String sundryName = "";
		String siteFinEntity = "";
		String finentCurrCode = "";
		String remarks = "";
		String avaliableYn1 = "";		
		String avail = "";
		String shipmentId = ""; 
		String lorryNo = "";
		String lrNo = "";
		String lrNoDesp = "";
		String lorryNoDesp = "";
		String stateFrom = "";
		String stateTo = "";
		String tranCodeDesp = "";
		String tranTypeParent = "";
		String unitAlt = "";
		String unit = "";
		String itemCode = "";
		String descr = "";
		String transMode = "";	
		String unitAlt1 = "";
		String packInstr1 = "";
		String mPackCode = "";
		String packCode = "";
		String packInstr = "";
		String mDescr = "";
		String taxClass = "";
		String taxChap = "";
		String taxEnv = "";
		String batchNo = "";
		String grade = "";
		String mKeyVal = "";
		String suppCodeMfg = ""; 
		String dimension = "";
		String str = "";
		String stkOpt = "";
		String projCode = "";
		String reasCode = "";
		String crTerm = "";
		String dlvTerm = "";
		String outins = "";
		String labelType = "";
		String issueDateStr = "";
		String lrDateDesp="";
		String mTranDateStr = "";
		String lcQtyStr = "";	
		String lineNoDist = "";
		
		Timestamp mTranDate = null;
		Timestamp lrDate = null; 
		Timestamp mfgDate = null;
		Timestamp expDate = null;
		Timestamp retestDate = null;
		Timestamp orderDate = null; 
		
		int pos = 0;
		double exchRateF = 0d;
		double exchRate = 0d;
		
		double netWeight = 0d;
		double grossWeight = 0d;
		double tareWeight = 0d;	
		double fact = 0d;
		double qtyQrderAlt = 0d;
		double lcQty = 0d;
		double shipperQty = 0d;
		double integralQty = 0.0;
		double shipQty = 0.0;
		double intQty = 0.0;
		double looseQty = 0.0;
		double balQty = 0.0;
		double qtyIss = 0.0;
		double qtyBrow = 0.0;
		double ConfShipQty = 0.0;
		double ShipRetQty = 0.0;
		double convQtyAlt = 0.0;
		double mRateClg = 0.0;
		double shipperSize = 0.0;
		double lcIntegralQty = 0.0;
		double mRate = 0.0;
		double discount = 0.0;
		double RateClg = 0.0;
		double rateAlt = 0.0;
		double costRate = 0.0;
		double remainder = 0.0;
		double mod = 0.0d;
		double potencyPerc = 0.0d;
		
		double stockGrossWeight = 0d; 
		double stockNetWeight = 0d; 
		double stockTareWeight = 0d; 	
		double grossWeightArt = 0d;
		double tareWeightArt = 0d;
		double qtyPerArt = 0d;	
		double palletWt = 0d;
		double stockQty = 0d;
		
		int mCount = 0;
		int noArticle = 0;
		int noArticle1=0;
		int noArticle2=0;
		double noArt1 = 0d;
		double noArt2 = 0d;
		double mtaxamt = 0d;	
		double mQty =0d;
		double mtotamt = 0d;
		double mdisamt = 0d;
		double mnetAmt = 0d;
		double tmpAmt = 0d;
		double tmpAmt1 = 0d;
		try
		{
			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String sysDate = sdf.format(currentDate.getTime());		
			conn = getConnection();
			conn.setAutoCommit(false);	
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext.trim());
			}
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");	
			System.out.println("**********ITEMCHANGE FOR CASE"+currentFormNo+"**************["+xtraParams+"]");
			switch(currentFormNo)
			{
			case 1 :
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0; 
				valueXmlString.append("<Detail1>");
				int childNodeListLength = childNodeList.getLength();
				do
				{ 
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if(childNodeName.equals(currentColumn))
					{
						if (childNode.getFirstChild()!= null)
						{
							columnValue = childNode.getFirstChild().getNodeValue().trim();
						}
					}
					ctr++;
				}
				while(ctr < childNodeListLength  && !childNodeName.equals(currentColumn));
				System.out.println("Pavan>>>[" + currentColumn + "] ==> '" + columnValue + "'");
				if(currentColumn.trim().equalsIgnoreCase("itm_defaultedit"))
				{								
					mKeyVal = checkNull(genericUtility.getColumnValue("tran_id", dom));
					sql = "select count(*) from distord_issdet where tran_id = ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,mKeyVal);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						cnt =  rs.getInt(1);					
					}
					rs.close();
					rs = null;
					pstmt.close(); 
					pstmt = null;
					distOrder = checkNull(genericUtility.getColumnValue("dist_order", dom));
					avaliableYn1 = checkNull(genericUtility.getColumnValue("available_yn", dom));
					if( cnt > 0 )
					{
						valueXmlString.append("<dist_order protect = \"1\">").append("<![CDATA[" +  distOrder +"]]>").append("</dist_order>");
						valueXmlString.append("<available_yn protect = \"1\">").append("<![CDATA[" + avaliableYn1 + "]]>").append("</available_yn>");										
					}
					else
					{
						valueXmlString.append("<dist_order protect = \"0\">").append("<![CDATA[" +  distOrder +"]]>").append("</dist_order>");
						valueXmlString.append("<available_yn protect = \"0\">").append("<![CDATA[" + avaliableYn1 + "]]>").append("</available_yn>");										
					}
					
				}else if(currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					lsLoginSite = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
					/** SELECTING SITE DESCRIPTION FROM SITE*/
					sql = "select descr, fin_entity from site where site_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsLoginSite);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						msite = checkNull(rs.getString("descr"));
						finEntity = checkNull(rs.getString("fin_entity"));
					}				
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;					
					/**selecting currency code from fin entity*/			
					sql = "select curr_code from finent where fin_entity = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, finEntity);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						mCurrCode = checkNull(rs.getString("curr_code"));					
					}
					rs.close();
					rs = null;
					pstmt.close(); 
					pstmt = null;
					
					/**selecting currency descr from currency*/				
					sql = "select descr from currency where curr_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mCurrCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						mCurr = checkNull(rs.getString("descr"));					
					}
					rs.close();
					rs = null;
					pstmt.close(); 
					pstmt = null;
									
					/**selecting transit loc code from disparm*/
					varValue = distCommon.getDisparams("999999", "TRANSIT_LOC", conn);	
					System.out.println("TRANSIT_LOC ["+varValue+"]");
					/**selecting description from location master*/				
					sql = "select descr from location where loc_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, varValue);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						mloc = checkNull(rs.getString("descr"));					
					}
					rs.close();
					rs = null;
					pstmt.close(); 
					pstmt = null;
					
					valueXmlString.append("<site_code>").append("<![CDATA[" + lsLoginSite + "]]>").append("</site_code>");
					setNodeValue(dom, "site_code", getAbsString(lsLoginSite));				
					valueXmlString.append("<site_descr>").append("<![CDATA[" + msite + "]]>").append("</site_descr>");
					setNodeValue(dom, "site_descr", getAbsString(msite));				
					valueXmlString.append("<loc_code__git>").append("<![CDATA[" + varValue + "]]>").append("</loc_code__git>");
					setNodeValue(dom, "loc_code__git", getAbsString(varValue));				
					valueXmlString.append("<location_descr>").append("<![CDATA[" + mloc + "]]>").append("</location_descr>");
					setNodeValue(dom, "location_descr", getAbsString(mloc));				
					valueXmlString.append("<curr_code>").append("<![CDATA[" + mCurrCode + "]]>").append("</curr_code>");
					setNodeValue(dom, "curr_code", getAbsString(mCurrCode));				
					valueXmlString.append("<currency_descr>").append("<![CDATA[" + mCurr + "]]>").append("</currency_descr>");
					setNodeValue(dom, "currency_descr", getAbsString(mCurr));				
					valueXmlString.append("<tran_date>").append("<![CDATA[" + sysDate + "]]>").append("</tran_date>");
					setNodeValue(dom, "tran_date", getAbsString(sysDate));				
					valueXmlString.append("<eff_date>").append("<![CDATA[" + sysDate + "]]>").append("</eff_date>");
					setNodeValue(dom, "eff_date", getAbsString(sysDate));				
					valueXmlString.append("<tran_ser>").append("<![CDATA[" + tranSer + "]]>").append("</tran_ser>");								
					setNodeValue(dom, "tran_ser", getAbsString(tranSer));				
					//valueXmlString.append("<dist_order protect = \"0\">").append("<![CDATA[]]>").append("</dist_order>");
					//setNodeValue(dom, "dist_order", getAbsString());
					valueXmlString.append("<sundry_details>").append("<![CDATA[" + " " + "]]>").append("</sundry_details>");
					setNodeValue(dom, "sundry_details", getAbsString(" "));
					valueXmlString.append("<gp_date>").append("<![CDATA[" + sysDate + "]]>").append("</gp_date>");
					setNodeValue(dom, "gp_date", getAbsString(sysDate));
				
				}
				else if(currentColumn.trim().equalsIgnoreCase("dist_order"))
				{				
					mCode = checkNull(genericUtility.getColumnValue("dist_order", dom));
							//Added site_code__bil in following select to set it from distorder into dist issue,								
					sql = "select dist_route, site_code__dlv, loc_code__git, sale_order, order_date, order_type,"
						+ " tran_type, curr_code, exch_rate, sundry_type, sundry_code, remarks, avaliable_yn, proj_code," 
						+ " trans_mode, site_code__bil, cr_term, dlv_term, label_type, outside_inspection "
						+ " from DISTORDER where dist_order = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						mdistRoute = checkNull(rs.getString("dist_route"));
						msiteTo = checkNull(rs.getString("site_code__dlv"));
						mLocCodeGit = checkNull(rs.getString("loc_code__git"));
						saleOrder = checkNull(rs.getString("sale_order"));
						orderDate = rs.getTimestamp("order_date");
						distOrderType = checkNull(rs.getString("order_type"));
						tranType = checkNull(rs.getString("tran_type"));
						currCode1 = checkNull(rs.getString("curr_code"));
						exchRate = rs.getDouble("exch_rate");
						sundryType = checkNull(rs.getString("sundry_type"));
						sundryCode = checkNull(rs.getString("sundry_code"));
						remarks = checkNull(rs.getString("remarks"));
						avaliableYn1 = checkNull(rs.getString("avaliable_yn"));
						projCode = checkNull(rs.getString("proj_code"));
						transMode = checkNull(rs.getString("trans_mode"));
						lsSiteCodeBil = checkNull(rs.getString("site_code__bil"));
						crTerm = checkNull(rs.getString("cr_term"));
						dlvTerm = checkNull(rs.getString("dlv_term"));
						labelType = checkNull(rs.getString("label_type"));
						outins = checkNull(rs.getString("outside_inspection"));
						
					}
					rs.close();
					rs = null;
					pstmt.close(); 
					pstmt = null;
					 
					valueXmlString.append("<proj_code>").append("<![CDATA[" + projCode + "]]>").append("</proj_code>");
					setNodeValue(dom, "proj_code", getAbsString(projCode));
					valueXmlString.append("<trans_mode>").append("<![CDATA[" + transMode + "]]>").append("</trans_mode>");
					setNodeValue(dom, "trans_mode", getAbsString(transMode));
					valueXmlString.append("<site_code__bil>").append("<![CDATA[" + lsSiteCodeBil + "]]>").append("</site_code__bil>");								
					setNodeValue(dom, "site_code__bil", getAbsString(lsSiteCodeBil));
							
					//gbf_itemchanged_logic(as_form_no,"site_code__bil",as_editflag)//To set site descr and address.
					reStr = itemChanged(dom, dom1, dom2, objContext, "site_code__bil", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail1>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail1>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);							
					if ("C".equals(sundryType))
					{				
						sql="select cust_name from customer where cust_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, sundryCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							sundryName = checkNull(rs.getString("cust_name"));
						}
						rs.close();
						rs = null;
						pstmt.close(); 
						pstmt = null;
						sundryDetails = sundryCode +"  "+sundryName;				
						valueXmlString.append("<sundry_name>").append("<![CDATA[" + "Customer : " + "]]>").append("</sundry_name>");
						valueXmlString.append("<sundry_details>").append("<![CDATA[" + sundryDetails + "]]>").append("</sundry_details>");
						//setNodeValue(dom, "sundry_name", getAbsString("Customer : "));
						setNodeValue(dom, "sundry_details", getAbsString(sundryDetails));
					}					 
					else if("S".equals(sundryType))
					{
						sql="select supp_name from supplier where supp_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, sundryCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							sundryName = checkNull(rs.getString("supp_name"));
						}
						rs.close();
						rs = null;
						pstmt.close(); 
						pstmt = null;							 				
						sundryDetails = sundryCode +"  "+sundryName;				
						valueXmlString.append("<sundry_name>").append("<![CDATA[" + "Supplier : " + "]]>").append("</sundry_name>");
						valueXmlString.append("<sundry_details>").append("<![CDATA[" + sundryDetails + "]]>").append("</sundry_details>");
						setNodeValue(dom, "sundry_details", getAbsString(sundryDetails));
					}
					if(sundryCode == null || sundryCode.trim().length() == 0)
					{
						valueXmlString.append("<sundry_details>").append("<![CDATA[" + " " + "]]>").append("</sundry_details>");				
					}
					
					sql="select descr from site where site_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, msiteTo);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						msiteToDescr = checkNull(rs.getString("descr"));
					}
					rs.close();
					rs = null;
					pstmt.close(); 
					pstmt = null;
					valueXmlString.append("<dist_route>").append("<![CDATA[" + mdistRoute + "]]>").append("</dist_route>");
					valueXmlString.append("<site_code__dlv>").append("<![CDATA[" + msiteTo + "]]>").append("</site_code__dlv>");
					setNodeValue(dom, "site_code__dlv", getAbsString(msiteTo));
					//gbf_itemchanged_logic(as_form_no,"site_code__dlv",as_editflag)
					reStr = itemChanged(dom, dom1, dom2, objContext, "site_code__dlv", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail1>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail1>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);
					
					valueXmlString.append("<site_to_descr>").append("<![CDATA[" + msiteToDescr + "]]>").append("</site_to_descr>");
					valueXmlString.append("<loc_code__git>").append("<![CDATA[" + mLocCodeGit + "]]>").append("</loc_code__git>");
					valueXmlString.append("<tran_type>").append("<![CDATA[" + tranType + "]]>").append("</tran_type>");
					setNodeValue(dom, "site_to_descr", getAbsString(msiteToDescr));
					setNodeValue(dom, "loc_code__git", getAbsString(mLocCodeGit));
					setNodeValue(dom, "tran_type", getAbsString(tranType));
					//gbf_itemchanged_logic(as_form_no,"tran_type",as_editflag)
					reStr = itemChanged(dom, dom1, dom2, objContext, "tran_type", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail1>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail1>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);
					
					valueXmlString.append("<curr_code>").append("<![CDATA[" + currCode1 + "]]>").append("</curr_code>");
					setNodeValue(dom, "curr_code", getAbsString(currCode1));
					
					reStr = itemChanged(dom, dom1, dom2, objContext, "curr_code", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail1>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail1>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);
					
					lsSiteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
					 /** - If curr_code = finent.curr_code then make exch_rate editable.*/						
					sql="select fin_entity from site where site_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsSiteCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						siteFinEntity = checkNull(rs.getString("fin_entity"));
					}
					rs.close();
					rs = null;
					pstmt.close(); 
					pstmt = null;
					
					sql="select curr_code from finent where fin_entity = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteFinEntity);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						finentCurrCode = checkNull(rs.getString("curr_code"));
					}
					rs.close();
					rs = null;
					pstmt.close(); 
					pstmt = null;
													
					if(!(currCode1.trim().equals(finentCurrCode.trim())))									
					{	
						valueXmlString.append("<exch_rate protect = \"0\">").append("<![CDATA[]]>").append("</exch_rate>");											
					}else
					{
						valueXmlString.append("<exch_rate protect = \"1\">").append("<![CDATA[]]>").append("</exch_rate>");
						
					}															
					issueDateStr = checkNull(genericUtility.getColumnValue("tran_date", dom));
										
					exchRateF = finCommon.getDailyExchRateSellBuy(currCode1, finentCurrCode, lsSiteCode, issueDateStr, "S", conn);			
					if(exchRateF == 0)
					{						
						valueXmlString.append("<exch_rate protect = \"1\">").append("<![CDATA[" + exchRate + "]]>").append("</exch_rate>");
						setNodeValue(dom, "exch_rate", getAbsString(String.valueOf(exchRate)));
						
					}else{						
						valueXmlString.append("<exch_rate protect = \"1\">").append("<![CDATA[" + exchRateF + "]]>").append("</exch_rate>");
						setNodeValue(dom, "exch_rate", getAbsString(String.valueOf(exchRateF)));
					}				
					
					sql = "select descr from location where loc_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mLocCodeGit);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						mloc = checkNull(rs.getString("descr"));
					}
					rs.close();
					rs = null;
					pstmt.close(); 
					pstmt = null;
					valueXmlString.append("<location_descr>").append("<![CDATA[" + mloc + "]]>").append("</location_descr>");
					valueXmlString.append("<order_type>").append("<![CDATA[" + distOrderType + "]]>").append("</order_type>");				
					setNodeValue(dom, "location_descr", getAbsString(mloc));
					setNodeValue(dom, "order_type", getAbsString(distOrderType));
					
					sql = "select gp_ser from distorder_type where tran_type = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, tranType);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						gpSer = checkNull(rs.getString("gp_ser"));
					}
					rs.close();
					rs = null;
					pstmt.close(); 
					pstmt = null;
						
					/*if(gpSer == null || gpSer.trim().length() == 0)
					{	
						if(tranType.trim().length() > 0)
						{gpSer = tranType.substring(0,1);}//--left(trim(tranType),1)
					}*/
					
					valueXmlString.append("<gp_ser>").append("<![CDATA[" + gpSer + "]]>").append("</gp_ser>");
					valueXmlString.append("<remarks>").append("<![CDATA[" + remarks + "]]>").append("</remarks>");				
					valueXmlString.append("<available_yn protect = \"1\">").append("<![CDATA[" + avaliableYn1 + "]]>").append("</available_yn>");				
					setNodeValue(dom, "gp_ser", getAbsString(gpSer));
					setNodeValue(dom, "remarks", getAbsString(remarks));
					setNodeValue(dom, "available_yn", getAbsString(avaliableYn1));
									
					valueXmlString.append("<cr_term>").append("<![CDATA[" + crTerm + "]]>").append("</cr_term>");
					valueXmlString.append("<dlv_term>").append("<![CDATA[" + dlvTerm + "]]>").append("</dlv_term>");
					valueXmlString.append("<label_type>").append("<![CDATA[" + labelType + "]]>").append("</label_type>");
					valueXmlString.append("<outside_inspection>").append("<![CDATA[" + outins + "]]>").append("</outside_inspection>");
					setNodeValue(dom, "cr_term", getAbsString(crTerm));
					setNodeValue(dom, "dlv_term", getAbsString(dlvTerm));
					setNodeValue(dom, "label_type", getAbsString(labelType));
					setNodeValue(dom, "outside_inspection", getAbsString(outins));
				}
				else if(currentColumn.trim().equalsIgnoreCase("tran_date"))
				{
					issueDateStr = checkNull(genericUtility.getColumnValue("tran_date", dom));
					valueXmlString.append("<eff_date>").append("<![CDATA[" + issueDateStr + "]]>").append("</eff_date>");
					setNodeValue(dom, "eff_date", getAbsString(issueDateStr));
				}
				/*else if(currentColumn.trim().equalsIgnoreCase("tran_type"))
				{
					commented
				}*/
				else if(currentColumn.trim().equalsIgnoreCase("site_code"))
				{
					msiteCode = checkNull(genericUtility.getColumnValue("site_code", dom));				
					sql = "select descr from site where site_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, msiteCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						msite = checkNull(rs.getString("descr"));
					}
					rs.close();
					rs = null;
					pstmt.close(); 
					pstmt = null;				
					valueXmlString.append("<site_descr>").append("<![CDATA[" + msite + "]]>").append("</site_descr>");
					
					/**If curr_code = finent.curr_code then make exch_rate editable.*/					
					currCode = checkNull(genericUtility.getColumnValue("curr_code", dom));
					sql = "select fin_entity from site where site_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, msiteCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						siteFinEntity = checkNull(rs.getString("fin_entity"));
					}
					rs.close();
					rs = null;
					pstmt.close(); 
					pstmt = null;
					sql = "select curr_code from finent where fin_entity = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteFinEntity);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						finentCurrCode = checkNull(rs.getString("curr_code"));
					}
					rs.close();
					rs = null;
					pstmt.close(); 
					pstmt = null;
					
					if(!(currCode.trim().equals(finentCurrCode.trim())))									
					{		
						//valueXmlString.append("<exch_rate protect = \"0\">").append("<![CDATA[]]>").append("</exch_rate>");
						valueXmlString.append("<exch_rate protect = \"0\">").append ("</exch_rate>");
					}else
					{						
						//valueXmlString.append("<exch_rate protect = \"1\">").append("<![CDATA[]]>").append("</exch_rate>");
						valueXmlString.append("<exch_rate protect = \"1\">").append ("</exch_rate>");
					}				
				}
				else if(currentColumn.trim().equalsIgnoreCase("site_code__dlv"))
				{
					msiteCode = checkNull(genericUtility.getColumnValue("site_code__dlv", dom)); 
					lsSiteCode = checkNull(genericUtility.getColumnValue("site_code", dom)); 
					/** for setting telephone nos of delivery site(to_site)*/
					sql ="select tele1, tele2, tele3 from site where site_code = ?"; 
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, msiteCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						siteToTel1 = checkNull(rs.getString("tele1"));
						siteToTel2 = checkNull(rs.getString("tele2"));
						siteToTel3 = checkNull(rs.getString("tele3"));
					}
					rs.close();
					rs = null;
					pstmt.close(); 
					pstmt = null;
					valueXmlString.append("<site_tele1>").append("<![CDATA[" + siteToTel1 + "]]>").append("</site_tele1>");
					valueXmlString.append("<site_tele2>").append("<![CDATA[" + siteToTel2 + "]]>").append("</site_tele2>");
					valueXmlString.append("<site_tele3>").append("<![CDATA[" + siteToTel3 + "]]>").append("</site_tele3>");
					setNodeValue(dom, "site_tele1", getAbsString(siteToTel1));
					setNodeValue(dom, "site_tele2", getAbsString(siteToTel2));
					setNodeValue(dom, "site_tele3", getAbsString(siteToTel3));
					
					sql="select a.state_code as stat_fr,b.state_code as stat_to from site a , site b " 
						+ " where a.site_code = ? and b.site_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, msiteCode);
					pstmt.setString(2, lsSiteCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						stateFrom = checkNull(rs.getString("stat_fr"));
						stateTo = checkNull(rs.getString("stat_to"));					
					}
					rs.close();
					rs = null;
					pstmt.close(); 
					pstmt = null;								
					if (stateFrom.trim().equals(stateTo.trim()))
					{
						valueXmlString.append("<rd_permit_no protect = \"1\">").append("<![CDATA["+ "" +"]]>").append("</rd_permit_no>");					
					}
					else
					{
						valueXmlString.append("<rd_permit_no protect = \"0\">").append("<![CDATA["+ "" +"]]>").append("</rd_permit_no>");					
					}
					
					sql = "select descr,"
						+ " case when add1 is null then '' else add1 end as add1,"
						+ " case when add2 is null then '' else add2 end as add2,"	
						+ " case when city is null then '' else city end as city,"	
						+ " case when pin is null then '' else pin end as pin,	"
						+ " case when state_code is null then '' else state_code end as state_code"				
						+ " from site where site_code = ? ";				
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, msiteCode);				
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						msite = checkNull(rs.getString("descr"));
						addr1 = checkNull(rs.getString("add1"));
						addr2 = checkNull(rs.getString("add2"));
						city = checkNull(rs.getString("city"));	
						pinCode = checkNull(rs.getString("pin"));	
						stateCode = checkNull(rs.getString("state_code"));						
					}
					rs.close();
					rs = null;
					pstmt.close(); 
					pstmt = null;						
					valueXmlString.append("<site_to_descr>").append("<![CDATA[" + msite + "]]>").append("</site_to_descr>");
					setNodeValue(dom, "site_to_descr", getAbsString(msite));
					valueXmlString.append("<site_add1>").append("<![CDATA[" + addr1 + "]]>").append("</site_add1>");
					setNodeValue(dom, "site_add1", getAbsString(addr1));
					valueXmlString.append("<site_add2>").append("<![CDATA[" + addr2 + "]]>").append("</site_add2>");
					setNodeValue(dom, "site_add2", getAbsString(addr2));
					valueXmlString.append("<site_city>").append("<![CDATA[" + city + "]]>").append("</site_city>");
					setNodeValue(dom, "site_city", getAbsString(city));
					valueXmlString.append("<site_pin>").append("<![CDATA[" + pinCode + "]]>").append("</site_pin>");
					setNodeValue(dom, "site_pin", getAbsString(pinCode));
					valueXmlString.append("<site_state_code>").append("<![CDATA[" + stateCode + "]]>").append("</site_state_code>");				
					setNodeValue(dom, "site_state_code", getAbsString(stateCode));				
				}
				else if(currentColumn.trim().equalsIgnoreCase("tran_code"))
				{
					tranCode = checkNull(genericUtility.getColumnValue("tran_code", dom));
					lsSiteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
					
					sql ="select tran_name, curr_code from transporter where tran_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, tranCode);				
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						tranName = checkNull(rs.getString("tran_name"));
						mCurrCode = checkNull(rs.getString("curr_code"));											
					}
					rs.close();
					rs = null;
					pstmt.close(); 
					pstmt = null;			
					valueXmlString.append("<tran_name>").append("<![CDATA[" + tranName + "]]>").append("</tran_name>"); 																							
					setNodeValue(dom, "tran_name", getAbsString(tranName));
					if (mCurrCode == null || mCurrCode.trim().length() == 0)
					{		
						sql = "select fin_entity from site where site_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsSiteCode);				
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							siteFinEntity = checkNull(rs.getString("fin_entity"));																
						}
						rs.close();
						rs = null;
						pstmt.close(); 
						pstmt = null;	
						sql = "select curr_code from finent where fin_entity = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, siteFinEntity);				
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							mCurrCode = checkNull(rs.getString("curr_code"));																		
						}
						rs.close();
						rs = null;
						pstmt.close(); 
						pstmt = null;	
						valueXmlString.append("<curr_code__frt>").append("<![CDATA[" + mCurrCode + "]]>").append("</curr_code__frt>");
						setNodeValue(dom, "curr_code__frt", getAbsString(mCurrCode));
						
						reStr = itemChanged(dom, dom1, dom2, objContext, "curr_code__frt", editFlag, xtraParams);
						pos = reStr.indexOf("<Detail1>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail1>");
						reStr = reStr.substring(0, pos);
						valueXmlString.append(reStr);																	
					}								
				}
				else if(currentColumn.trim().equalsIgnoreCase("loc_code__git"))
				{				
					locCode = checkNull(genericUtility.getColumnValue("loc_code__git", dom)); 
					sql = "select descr from location where loc_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, locCode);				
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						mloc = checkNull(rs.getString("descr"));																		
					}
					rs.close();
					rs = null;
					pstmt.close(); 
					pstmt = null;	
					valueXmlString.append("<location_descr>").append("<![CDATA[" + mloc + "]]>").append("</location_descr>");
					
				}
				else if(currentColumn.trim().equalsIgnoreCase("curr_code"))
				{
					mCurrCode = checkNull(genericUtility.getColumnValue("curr_code", dom));				
					sql = "select descr from currency where curr_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mCurrCode);	
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						mCurrDescr = checkNull(rs.getString("descr"));																		
					}
					rs.close();
					rs = null;
					pstmt.close(); 
					pstmt = null;	
					valueXmlString.append("<currency_descr>").append("<![CDATA[" + mCurrDescr + "]]>").append("</currency_descr>");
					setNodeValue(dom, "currency_descr", getAbsString(mCurrDescr));
					
					/**If curr_code = finent.curr_code then make exch_rate editable.*/
					issueDateStr = checkNull(genericUtility.getColumnValue("tran_date", dom));								
					lsSiteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
					currCode = checkNull(genericUtility.getColumnValue("curr_code", dom));
					sql = "select fin_entity from site where site_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsSiteCode);	
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						siteFinEntity = checkNull(rs.getString("fin_entity"));																		
					}
					rs.close();
					rs = null;
					pstmt.close(); 
					pstmt = null;
							
					sql = "select curr_code from finent where fin_entity = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteFinEntity);	
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						finentCurrCode = checkNull(rs.getString("curr_code"));																		
					}
					rs.close();
					rs = null;
					pstmt.close(); 
					pstmt = null;		
									
					if(!(currCode.trim().equals(finentCurrCode.trim())))									
					{
						exchRate = finCommon.getDailyExchRateSellBuy(mCurrCode, finentCurrCode, lsSiteCode, issueDateStr, "S", conn);
						valueXmlString.append("<exch_rate protect = \"0\">").append("<![CDATA[" + String.valueOf(exchRate) + "]]>").append("</exch_rate>");
						setNodeValue(dom, "exch_rate", getAbsString(String.valueOf(exchRate)));
						
					}else
					{					
						valueXmlString.append("<exch_rate protect = \"1\">").append("<![CDATA[" + "1" + "]]>").append("</exch_rate>");
						setNodeValue(dom, "exch_rate", getAbsString("1"));
					}	
				}else if(currentColumn.trim().equalsIgnoreCase("curr_code__frt"))
				{
					mCurrCode = checkNull(genericUtility.getColumnValue("curr_code__frt", dom));
					issueDateStr = checkNull(genericUtility.getColumnValue("tran_date", dom));
					lsSiteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
					currCode = checkNull(genericUtility.getColumnValue("curr_code", dom));
					lsLoginSite = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
					
					sql = "select descr from currency where curr_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mCurrCode);	
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						mCurrDescr = checkNull(rs.getString("descr"));																		
					}
					rs.close();
					rs = null;
					pstmt.close(); 
					pstmt = null;													
					valueXmlString.append("<currency_descr__frt>").append("<![CDATA[" + mCurrDescr + "]]>").append("</currency_descr__frt>");
					setNodeValue(dom, "currency_descr__frt", getAbsString(mCurrDescr));
												
					exchRate = finCommon.getDailyExchRateSellBuy(mCurrCode, "", lsLoginSite, issueDateStr, "S", conn);								
					//valueXmlString.append("<exch_rate__frt>").append("<![CDATA[" + exchRate + "]]>").append("</exch_rate__frt>");
					//setNodeValue(dom, "exch_rate__frt", getAbsString(String.valueOf(exchRate)));
									
					/**If curr_code = finent.curr_code then make exch_rate editable.	*/				
					sql = "select fin_entity from site where site_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsSiteCode);	
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						siteFinEntity = checkNull(rs.getString("fin_entity"));																		
					}
					rs.close();
					rs = null;
					pstmt.close(); 
					pstmt = null;	
					
					sql = "select curr_code from finent where fin_entity = ?";				
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteFinEntity);	
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						finentCurrCode = checkNull(rs.getString("curr_code"));																		
					}
					rs.close();
					rs = null;
					pstmt.close(); 
					pstmt = null;	
					if(!(currCode.trim().equals(finentCurrCode.trim())))									
					{					
						//valueXmlString.append("<exch_rate__frt protect = \"0\">").append("<![CDATA[]]>").append("</exch_rate__frt>");
						valueXmlString.append("<exch_rate__frt protect = \"0\">").append("<![CDATA[" + exchRate + "]]>").append("</exch_rate__frt>");
						setNodeValue(dom, "exch_rate__frt", getAbsString(String.valueOf(exchRate)));
						
						
					}else
					{					
						//valueXmlString.append("<exch_rate__frt protect = \"1\">").append("<![CDATA[]]>").append("</exch_rate__frt>");
						valueXmlString.append("<exch_rate__frt protect = \"1\">").append("<![CDATA[" + exchRate + "]]>").append("</exch_rate__frt>");
						setNodeValue(dom, "exch_rate__frt", getAbsString(String.valueOf(exchRate)));
						
					}			
				}
				else if(currentColumn.trim().equalsIgnoreCase("order_type"))
				{
					orderType = checkNull(genericUtility.getColumnValue("order_type", dom));	
					sql = "select gp_ser from distorder_type where tran_type = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, orderType);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						gpSer = checkNull(rs.getString("gp_ser"));
					}
					rs.close();
					rs = null;
					pstmt.close(); 
					pstmt = null;
					/*if(gpSer == null || gpSer.trim().length() == 0)	
					{			
						//gpSer = left(trim(orderType),1)			
						gpSer =	orderType.substring(0,1);
					}*/
					valueXmlString.append("<gp_ser>").append("<![CDATA[" + gpSer + "]]>").append("</gp_ser>");
					setNodeValue(dom, "gp_ser", getAbsString(gpSer));
				}
				else if(currentColumn.trim().equalsIgnoreCase("shipment_id"))
				{
					shipmentId = checkNull(genericUtility.getColumnValue("shipment_id", dom));
					
					sql = "select lorry_no, lr_no, lr_date, gross_weight, tare_weight, tran_code"
						+ " from shipment where shipment_id = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, shipmentId);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						lorryNo = checkNull(rs.getString("lorry_no"));
						lrNo = checkNull(rs.getString("lr_no"));
						lrDate = rs.getTimestamp("lr_date");
						grossWeight = rs.getDouble("gross_weight");
						tareWeight= rs.getDouble("tare_weight");
						tranCode= checkNull(rs.getString("tran_code"));
					}		
					
					lorryNoDesp = checkNull(genericUtility.getColumnValue("lorry_no", dom));
					lrNoDesp = checkNull(genericUtility.getColumnValue("lr_no", dom));
					lrDateDesp = genericUtility.getColumnValue("lr_date", dom);
					tranCodeDesp = checkNull(genericUtility.getColumnValue("tran_code", dom));
					
					netWeight = grossWeight - tareWeight;
					valueXmlString.append("<gross_weight>").append("<![CDATA[" + grossWeight + "]]>").append("</gross_weight>");
					valueXmlString.append("<tare_weight>").append("<![CDATA[" + tareWeight + "]]>").append("</tare_weight>");
					valueXmlString.append("<net_weight>").append("<![CDATA[" + netWeight + "]]>").append("</net_weight>");
					setNodeValue(dom, "gross_weight", getAbsString(String.valueOf(grossWeight)));
					setNodeValue(dom, "tare_weight", getAbsString(String.valueOf(grossWeight)));
					setNodeValue(dom, "net_weight", getAbsString(String.valueOf(grossWeight)));
					if(lorryNoDesp == null || lorryNoDesp.trim().length() == 0)
					{
						valueXmlString.append("<lorry_no>").append("<![CDATA[" + lorryNo + "]]>").append("</lorry_no>");
						setNodeValue(dom, "lorry_no", getAbsString(lorryNo));
					}
					if(lorryNoDesp == null || lorryNoDesp.trim().length() == 0)
					{									
						valueXmlString.append("<lr_no>").append("<![CDATA[" + lrNo + "]]>").append("</lr_no>");
						setNodeValue(dom, "lr_no", getAbsString(lrNo));
					}
					if(lrDateDesp == null)
					{							
						if(lrDate!=  null)
						{					
							valueXmlString.append("<lr_date>").append("<![CDATA[" + sdf.format(lrDate) + "]]>").append("</lr_date>");
							setNodeValue(dom, "lr_date", getAbsString(sdf.format(lrDate)));
						}
						
					}
					if(tranCodeDesp == null || tranCodeDesp.trim().length() == 0)
					{					
						sql = "select tran_name from transporter where tran_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, tranCode);
						rs= pstmt.executeQuery();
						if(rs.next())
						{
							tranName = checkNull(rs.getString("tran_name"));
						}
						if (tranName == null || tranName.trim().length() == 0)
						{
							tranName = "";												
						}
						valueXmlString.append("<tran_code>").append("<![CDATA[" + tranCode + "]]>").append("</tran_code>");
						valueXmlString.append("<tran_name>").append("<![CDATA[" + tranName + "]]>").append("</tran_name>");
						setNodeValue(dom, "tran_code", getAbsString(tranCode));
						setNodeValue(dom, "tran_name", getAbsString(tranName));
					}
							
				}else if(currentColumn.trim().equalsIgnoreCase("site_code__bil"))
				{
					
					lsSiteCodeBil = genericUtility.getColumnValue("site_code__bil", dom);
					sql = " select descr," 
						+ " case when add1 is null then '' else add1 end as add1," 
						+ "	case when add2 is null then '' else add2 end as add2,"
						+ " case when city is null then '' else city end as city," 
						+ " case when pin is null then '' else pin end as pin," 
						+ " case when state_code is null then '' else state_code end as state_code"				
						+ " from site "
						+ " where site_code = ?";				
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsSiteCodeBil);				
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						descr = checkNull(rs.getString("descr"));
						addr1 = checkNull(rs.getString("add1"));
						addr2 = checkNull(rs.getString("add2"));
						city = checkNull(rs.getString("city"));	
						pinCode = checkNull(rs.getString("pin"));	
						stateCode = checkNull(rs.getString("state_code"));						
					}
					rs.close();
					rs = null;
					pstmt.close(); 
					pstmt = null;		
					
					valueXmlString.append("<site_descr_bill>").append("<![CDATA[" + descr + "]]>").append("</site_descr_bill>");
					valueXmlString.append("<site_add1_bill>").append("<![CDATA[" + addr1 + "]]>").append("</site_add1_bill>");
					valueXmlString.append("<site_add2_bill>").append("<![CDATA[" + addr2 + "]]>").append("</site_add2_bill>");
					valueXmlString.append("<site_city_bill>").append("<![CDATA[" + city + "]]>").append("</site_city_bill>");
					valueXmlString.append("<site_pin_bill>").append("<![CDATA[" + pinCode + "]]>").append("</site_pin_bill>");
					valueXmlString.append("<site_state_code_bill>").append("<![CDATA[" + stateCode + "]]>").append("</site_state_code_bill>");
					setNodeValue(dom, "site_descr_bill", getAbsString(descr));
					setNodeValue(dom, "site_add1_bill", getAbsString(addr1));
					setNodeValue(dom, "site_add2_bill", getAbsString(addr2));
					setNodeValue(dom, "site_city_bill", getAbsString(city));
					setNodeValue(dom, "site_pin_bill", getAbsString(pinCode));
					setNodeValue(dom, "site_state_code_bill", getAbsString(stateCode));
					
				}
	
				valueXmlString.append("</Detail1>");
				break;
			case 2 :
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail2>");
				childNodeListLength = childNodeList.getLength();
				do
				{   
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					ctr ++;
				}while(ctr < childNodeListLength  && ! childNodeName.equals(currentColumn));
				System.out.println("Pavan Detail[" + currentColumn + "] ==> '" + columnValue + "'");
				if(currentColumn.trim().equalsIgnoreCase("itm_defaultedit"))
				{
					
					//dw_detedit[ii_currformno].setitem(1,"qty_details",'')				
					distOrder = genericUtility.getColumnValue("dist_order", dom);
					
					lcQtyStr =genericUtility.getColumnValue("quantity", dom);
					if(lcQtyStr != null && lcQtyStr.trim().length() > 0)
					{				
						lcQty = Double.parseDouble(lcQtyStr);
					}
					itemCode =genericUtility.getColumnValue("item_code", dom);
					packCode =genericUtility.getColumnValue("pack_code", dom);
					msiteCode =genericUtility.getColumnValue("site_code", dom1);
					
					
					 sql = "select sundry_code from distorder where dist_order = ?";
					 pstmt = conn.prepareStatement(sql);
					 pstmt.setString(1, distOrder);
					 rs = pstmt.executeQuery();
					 if(rs.next())
					 {
						 sundryCode = checkNull(rs.getString("sundry_code"));					 
					 }
					
					 noArticle = distCommon.getNoArt(msiteCode, sundryCode, itemCode, packCode, lcQty, 'B', shipperQty, integralQty, conn);
					 shipQty = shipperQty;
					 intQty = integralQty;
					 
					 noArticle1 = distCommon.getNoArt(msiteCode, sundryCode, itemCode, packCode, lcQty, 'S', shipperQty, integralQty, conn);
					 balQty = lcQty - (shipQty * noArticle1);				 		
					 
					 noArticle2 = distCommon.getNoArt(msiteCode, sundryCode, itemCode, packCode, balQty, 'I', shipperQty, integralQty, conn);
					 intQty = integralQty;
					 shipQty = shipQty * noArticle1;
					 intQty = intQty * noArticle2;
					 looseQty = lcQty - (shipQty + intQty );
	 
					 
					//str = "Shipper Quantity = "+string(shipQty,"0.00") + "   Integral Quantity = "+string(intQty,"0.00")+  "   Loose Quantity = "+string(looseQty,"0.00")				
					str = "Shipper Quantity = "+ shipQty +"   Integral Quantity = "+intQty+  "   Loose Quantity = "+looseQty;
					valueXmlString.append("<qty_details>").append("<![CDATA[" + str + "]]>").append("</qty_details>");
					setNodeValue(dom, "qty_details", getAbsString(str));					
					
					valueXmlString.append(gbfRateProtect(dom, dom1, dom2, editFlag, xtraParams, objContext, conn));																										
					
				}else if(currentColumn.trim().equalsIgnoreCase("itm_default"))
				{							
					tranType = checkNull(genericUtility.getColumnValue("tran_type", dom1));
					distOrder = checkNull(genericUtility.getColumnValue("dist_order", dom1));
					valueXmlString.append(gbfRateProtect(dom, dom1, dom2, editFlag, xtraParams, objContext, conn));
					sql = "select tran_type__parent from distorder_type where tran_type = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, tranType);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						tranTypeParent = checkNull(rs.getString("tran_type__parent"));
					}
					pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;
					if(!tranTypeParent.trim().equals(tranType.trim()))
					{
						/**if tranType = 'CR' or tranType = 'LR' then*/
						sql = "select loc_code__cons from distorder_type where tran_type = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, tranType);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							//Changed by Pavan R26dec19 start[bug fixed]
							//varValue = rs.getString("tran_type__parent");
							varValue = checkNull(rs.getString("loc_code__cons"));
							//Changed by Pavan R26dec19 end[bug fixed]
						}
						pstmt.close();
						pstmt = null;
						rs.close();
						rs = null;					
					}
					valueXmlString.append("<dist_order>").append("<![CDATA[" + distOrder + "]]>").append("</dist_order>");
					setNodeValue(dom, "dist_order", getAbsString(distOrder));
					
				}
				else if(currentColumn.trim().equalsIgnoreCase("line_no_dist_order") )//|| currentColumn.trim().equalsIgnoreCase("dist_order"))
				{	ConfShipQty = 0;
					System.out.println("Inside line_no_dist_order ~~ dist_order ["+currentColumn+"]dom["+genericUtility.serializeDom(dom));
					lineNoDist = checkNull(genericUtility.getColumnValue("line_no_dist_order", dom));
					mDistOrder = checkNull(genericUtility.getColumnValue("dist_order", dom));
					tranType = checkNull(genericUtility.getColumnValue("tran_type", dom1)); //from header
					System.out.println("lineNoDist["+lineNoDist+"]mDistOrder["+mDistOrder+"]tranType["+tranType+"]");					
					sql = "select item_code,"  
					+ " ((case when qty_confirm is null then 0 else qty_confirm end) - " 
					+ " (case when qty_shipped is null then 0 else qty_shipped end)) as conf_ship_qty,"	
					+ " ((case when qty_shipped is null then 0 else qty_shipped end) - " 
					+ " (case when qty_return is null then 0 else qty_return end)) as ship_ret_qty,	"
					+ " tax_class, tax_chap, tax_env,"
					+ " case when rate is null then 0 else rate end as rate,"
					+ " case when discount is null then 0 else discount end as discount,"	
			    	+ " rate__clg, UNIT__ALT, UNIT,CONV__QTY__ALT,"
			    	+ " pack_instr, reas_code "
					+ " from distorder_det"
			    	+ " where dist_order = ?" 
			    	+ " and line_no = ?";				
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mDistOrder);
					pstmt.setString(2, lineNoDist);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						itemCode = checkNull(rs.getString("item_code"));
						ConfShipQty = rs.getDouble("conf_ship_qty");
						ShipRetQty = rs.getDouble("ship_ret_qty");
						taxClass = checkNull(rs.getString("tax_class"));
						taxChap = checkNull(rs.getString("tax_chap"));
						taxEnv = checkNull(rs.getString("tax_env"));
						mRate = rs.getDouble("rate");
						discount = rs.getDouble("discount");
						RateClg = rs.getDouble("rate__clg");
						unitAlt1 = checkNull(rs.getString("UNIT__ALT"));
						unit = checkNull(rs.getString("UNIT"));
						convQtyAlt = rs.getDouble("CONV__QTY__ALT");
						packInstr = checkNull(rs.getString("pack_instr"));
						reasCode = checkNull(rs.getString("reas_code"));
					}
					pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;	
					System.out.println("itemCode["+itemCode+"]["+ConfShipQty+"]["+ConfShipQty+"]["+taxClass+"]["+taxChap+"]["+taxEnv+"]["+mRate+"]["+unitAlt1+"]");	  				
					if (unitAlt1 == null || unitAlt1.trim().length() == 0)
					{
						unitAlt1 = unit;
						convQtyAlt = 1;
					}															
					distIss = genericUtility.getColumnValue("tran_id", dom1);//from header
					if (distIss == null || distIss.trim().length()== 0)
					{ 
						distIss = "@@";
					}					
					valueXmlString.append("<quantity>").append("<![CDATA[" + "0" + "]]>").append("</quantity>");
					setNodeValue(dom, "quantity", getAbsString("0"));
							
					sql = "select sum(case when det.quantity is null then 0 else det.quantity end) as qty_iss from "
						+ " distord_issdet det, distord_iss hdr"
						+ " where det.tran_id  = hdr.tran_id"
						+ " and	hdr.tran_id <> ?"
						+ " and	(case when hdr.confirmed is null then 'N' else hdr.confirmed end) = 'N'"
						+ " and	det.dist_order = ?"
						+ " and	det.line_no_dist_order = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, distIss);
					pstmt.setString(2, mDistOrder);
					pstmt.setString(3, lineNoDist);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
	
						qtyIss = rs.getDouble("qty_iss") ;				
					}
					pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;		
					System.out.println("1316@@ qtyIss["+qtyIss+"]");
					if(String.valueOf(qtyIss) == null)
					{
						qtyIss = 0;
					}
					
					qtyBrow = 0;
					/*for mCount = 1 to dw_detbrow[ii_currformno].RowCount()
						if dw_detbrow[ii_currformno].GetItemNumber(mCount,"line_no_dist_order") = lineNoDist & 		and trim(dw_detbrow[ii_currformno].GetItemString(mCount,"dist_order")) = trim(mDistOrder) then  //Added by 
							qtyBrow = dw_detbrow[ii_currformno].GetItemDecimal(mCount,"quantity")
							if isnull(qtyBrow) then qtyBrow = 0
							qtyIss = qtyIss + qtyBrow
						end if
					next*/
					//if(lineNoDist!=  null && lineNoDist.trim().length()>0)
					//{
					System.out.println("1334::::ConfShipQty["+ConfShipQty+"]");					
					//System.out.println("DOM::["+genericUtility.serializeDom(dom)+"\nDOM1::["+genericUtility.serializeDom(dom1)+"\nDOM2::["+genericUtility.serializeDom(dom2)+"]");
					int parentNodeListLengthTemp = 0;
					NodeList parentNodeListTemp = null;				
					Node parentNodeTemp = null;
					parentNodeListTemp = dom2.getElementsByTagName("Detail2");
					parentNodeListLengthTemp = parentNodeListTemp.getLength();					
					for (int selectedRow = 0; selectedRow < parentNodeListLengthTemp; selectedRow++)
					{
						parentNodeTemp = parentNodeListTemp.item(selectedRow);
						String lineNoDistOrd = checkNull(genericUtility.getColumnValueFromNode("line_no_dist_order", parentNodeTemp));
						String distOrd = checkNull(genericUtility.getColumnValueFromNode("dist_order", parentNodeTemp));
						System.out.println("1348 check::["+lineNoDistOrd+"="+lineNoDist+"]["+distOrd+"="+mDistOrder+"]");
						if(lineNoDistOrd.trim().equals(lineNoDist.trim()) && distOrd.trim().equals(mDistOrder.trim()) && lineNoDist.trim().length() > 0)		
						{		
							qtyBrow = getDoubleValue(genericUtility.getColumnValueFromNode("quantity", parentNodeTemp));
							System.out.println("inside loop qtyBrow["+qtyBrow+"] qtyIss["+qtyIss+"]  selectedRow["+selectedRow+"]");
							qtyIss = qtyIss + qtyBrow;						
						}
					}
					ConfShipQty = ConfShipQty - qtyIss;
					System.out.println("after ConfShipQty["+ConfShipQty+"]");					
					valueXmlString.append("<item_code>").append("<![CDATA[" + itemCode + "]]>").append("</item_code>");
					setNodeValue(dom, "item_code", getAbsString(itemCode));
					
					reStr = itemChanged(dom, dom1, dom2, objContext, "item_code", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail2>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail2>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);
					
					valueXmlString.append("<unit>").append("<![CDATA[" + unit + "]]>").append("</unit>");
					setNodeValue(dom, "unit", getAbsString(unit));
					valueXmlString.append("<unit__alt>").append("<![CDATA[" + unitAlt1 + "]]>").append("</unit__alt>");
					setNodeValue(dom, "unit__alt", getAbsString(unitAlt1));
					valueXmlString.append("<pack_instr>").append("<![CDATA[" + packInstr + "]]>").append("</pack_instr>");
					setNodeValue(dom, "pack_instr", getAbsString(packInstr));
					valueXmlString.append("<reas_code>").append("<![CDATA[" + reasCode + "]]>").append("</reas_code>");
					setNodeValue(dom, "reas_code", getAbsString(reasCode));
					
					sql = "Select pack_code from item where item_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCode);				
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						mPackCode = checkNull(rs.getString("pack_code"));
					}
					pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;	
					
					valueXmlString.append("<pack_code>").append("<![CDATA[" + mPackCode + "]]>").append("</pack_code>");
					setNodeValue(dom, "pack_code", getAbsString(str));
					
					sql = "select tran_type__parent from distorder_type where tran_type = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, tranType);				
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						tranTypeParent = checkNull(rs.getString("tran_type__parent"));
					}
					pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;	
					
					if (!tranTypeParent.trim().equals(tranType.trim()))
					{	
						valueXmlString.append("<quantity>").append("<![CDATA[" + ShipRetQty + "]]>").append("</quantity>");
						setNodeValue(dom, "quantity", getAbsString(String.valueOf(ShipRetQty)));
						valueXmlString.append("<qty_order__alt>").append("<![CDATA[" + ShipRetQty + "]]>").append("</qty_order__alt>");
						setNodeValue(dom, "qty_order__alt", getAbsString(String.valueOf(ShipRetQty)));
					}					
					else
					{
						valueXmlString.append("<quantity>").append("<![CDATA[" + ConfShipQty + "]]>").append("</quantity>");
						setNodeValue(dom, "quantity", getAbsString(String.valueOf(ConfShipQty)));
						valueXmlString.append("<qty_order__alt>").append("<![CDATA[" + ConfShipQty + "]]>").append("</qty_order__alt>");
						setNodeValue(dom, "qty_order__alt", getAbsString(String.valueOf(ConfShipQty)));										
					}
					//gbf_itemchanged_logic(as_form_no,"quantity",as_editflag)
					reStr = itemChanged(dom, dom1, dom2, objContext, "quantity", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail2>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail2>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);
						
					valueXmlString.append("<tax_class>").append("<![CDATA[" + taxClass + "]]>").append("</tax_class>");
					setNodeValue(dom, "tax_class", getAbsString(taxClass));
					valueXmlString.append("<tax_chap>").append("<![CDATA[" + taxChap + "]]>").append("</tax_chap>");
					setNodeValue(dom, "tax_chap", getAbsString(taxChap));
					valueXmlString.append("<tax_env>").append("<![CDATA[" + taxEnv + "]]>").append("</tax_env>");
					setNodeValue(dom, "tax_env", getAbsString(taxEnv));
					
					/**---price rate set from master and if not get then set from dist order,and protect tochange from iser/////////////////////*/				
					//gbf_itemchg_modifier_ds(dw_detedit[ii_currformno],"rate__alt","protect","1")
				
					valueXmlString.append("<rate protect = \"1\">").append("<![CDATA[" + mRate + "]]>").append("</rate>");
					setNodeValue(dom, "rate", getAbsString(String.valueOf(mRate)));
					
					//gbf_itemchanged_logic(as_form_no,"rate",as_editflag)		//Added Ruchira 27/03/2k6
					reStr = itemChanged(dom, dom1, dom2, objContext, "rate", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail2>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail2>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);
					
					valueXmlString.append("<rate__clg protect = \"1\">").append("<![CDATA[" + RateClg + "]]>").append("</rate__clg>");
					setNodeValue(dom, "rate__clg", getAbsString(String.valueOf(RateClg)));				
					valueXmlString.append("<discount>").append("<![CDATA[" + discount + "]]>").append("</discount>");
					setNodeValue(dom, "discount", getAbsString(String.valueOf(discount)));
				}
				else if(currentColumn.trim().equalsIgnoreCase("quantity"))
				{
					ShipRetQty = Double.parseDouble(genericUtility.getColumnValue("quantity", dom));
					itemCode   = checkNull(genericUtility.getColumnValue("item_code", dom));
					msiteCode  = checkNull(genericUtility.getColumnValue("site_code", dom1));
					packCode   = checkNull(genericUtility.getColumnValue("pack_code", dom));			
					unitAlt	   = checkNull(genericUtility.getColumnValue("unit__alt", dom));
					unit	   = checkNull(genericUtility.getColumnValue("unit", dom));
					fact = 0;								
					ArrayList ratestduomArr = null;
					ratestduomArr = distCommon.getConvQuantityFact(unitAlt, unit, itemCode, ShipRetQty, fact, conn);
					System.out.println("ratestduomArr 2967["+ratestduomArr+"]");
					if(fact > 0 )
					{
						qtyQrderAlt = ShipRetQty / fact;
					}
					
					valueXmlString.append("<conv__qty__alt>").append("<![CDATA[" + ratestduomArr.get(0).toString() + "]]>").append("</conv__qty__alt>");
					setNodeValue(dom, "conv__qty__alt", getAbsString(String.valueOf(ratestduomArr.get(0).toString())));
					
					valueXmlString.append("<qty_order__alt>").append("<![CDATA[" + ratestduomArr.get(1).toString() + "]]>").append("</qty_order__alt>");
					setNodeValue(dom, "qty_order__alt", getAbsString(ratestduomArr.get(1).toString()));								
										
					distOrder = checkNull(genericUtility.getColumnValue("dist_order", dom));
										
					sql = "select sundry_code from distorder where dist_order = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, distOrder);
					rs  = pstmt.executeQuery();				
					if(rs.next())
					{
						sundryCode = checkNull(rs.getString("sundry_code"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					lotNo  = checkNull(genericUtility.getColumnValue("lot_no", dom));
					
					sql = "select (case when shipper_size is null then 0 else shipper_size end) as shipper_size"
						+ " from item_lot_packsize where item_code = ?"
						+ " and ? >= lot_no__from" 
						+ " and ? <= lot_no__to";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCode);
					pstmt.setString(2, lotNo);
					pstmt.setString(3, lotNo);
					rs = pstmt.executeQuery();				
					if(rs.next())
					{
						shipperSize = rs.getDouble("shipper_size");
					}
					else
					{
						shipQty = 0;
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
													
					if (shipperSize > 0) 
					{
						shipQty = shipperSize;					
						mod = ShipRetQty % shipQty; /**noArt1 = (ShipRetQty - mod(ShipRetQty,shipQty))/shipQty;*/					
						noArt1 = ((ShipRetQty - mod)/shipQty);														
						remainder = (ShipRetQty % shipQty);	   /**lc_remainder = mod(ShipRetQty,shipQty);*/				
						lcIntegralQty = distCommon.getIntegralQty(sundryCode, itemCode, msiteCode, conn);
						System.out.println("lcIntegralQty from discommon::["+lcIntegralQty+"]");
						/*
						sql = "select ( case when integral_qty is null then 0 else integral_qty end) as integral_qty "
						 + "from customeritem where cust_code = ? and item_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, sundryCode);
						pstmt.setString(2, itemCode);
						pstmt.executeQuery();				
						if(rs.next())
						{
							lcIntegralQty = rs.getDouble("integral_qty");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if(lcIntegralQty == 0)///if sqlca.sqlcode = 100 or lcIntegralQty = 0 then
						{
							sql = "select ( case when integral_qty is null then 0 else integral_qty end) as integral_qty "
							+ " from siteitem where site_code = ? and item_code = ?";  
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, msiteCode);
							pstmt.setString(2, itemCode);
							pstmt.executeQuery();				
							if(rs.next())
							{
								lcIntegralQty = rs.getDouble("integral_qty");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							if(lcIntegralQty == 0)//if sqlca.sqlcode = 100 or lcIntegralQty = 0 then
							{
								sql = " select ( case when integral_qty is null then 0 else integral_qty end) as integral_qty"
										+ "from item where item_code = :";						
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, itemCode);
								pstmt.executeQuery();				
								if(rs.next())
								{
									lcIntegralQty = rs.getDouble("integral_qty");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}*/						
						if( lcIntegralQty > 0 ) 		
						{							
							mod =(remainder % lcIntegralQty);
							noArt2 = (remainder - mod)/lcIntegralQty;
						}
						if( noArt2 > 0 )
						{
							noArt2 = 1;
						}									
						noArticle = (int)(noArt1 + noArt2);
						shipperQty = shipQty;
						integralQty = lcIntegralQty;
									
						if (shipQty == 0 )
						{					
							noArticle = distCommon.getNoArt(msiteCode, sundryCode, itemCode, packCode, ShipRetQty, 'B', shipperQty, integralQty, conn);
						}
						
						valueXmlString.append("<no_art>").append("<![CDATA[" + noArticle + "]]>").append("</no_art>");
						setNodeValue(dom, "no_art", getAbsString(String.valueOf(noArticle)));
						shipQty = shipperQty;
						intQty = integralQty;
					}		
					if ( shipQty == 0 )
					{						
						noArticle1 = distCommon.getNoArt(msiteCode, sundryCode, itemCode, packCode, ShipRetQty, 'S', shipperQty, integralQty, conn);
					}
					else
					{ 
						noArticle1 =  (int)noArt1;
					}
					balQty = ShipRetQty - (shipQty * noArticle1);
											
					noArticle2 = distCommon.getNoArt(msiteCode, sundryCode, itemCode, packCode, balQty, 'I', shipperQty, integralQty, conn);
					intQty = integralQty;
					shipQty = shipQty * noArticle1;
					intQty = intQty * noArticle2;
					looseQty = ShipRetQty - (shipQty + intQty );
	
					str = "Shipper Quantity = "+shipQty+ "   Integral Quantity = "+intQty+  "   Loose Quantity = "+looseQty;
					valueXmlString.append("<qty_details>").append("<![CDATA[" + str + "]]>").append("</qty_details>");
					setNodeValue(dom, "qty_details", getAbsString(String.valueOf(str)));
									
					reStr = itemChanged(dom, dom1, dom2, objContext, "rate", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail2>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail2>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);
					ratestduomArr = null;					
				}
				else if(currentColumn.trim().equalsIgnoreCase("item_code"))
				{
					mCode = checkNull(genericUtility.getColumnValue("item_code", dom));				
					sql = "Select descr, stk_opt from item where item_code = ?";			
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mCode);
					rs = pstmt.executeQuery();				
					if(rs.next())
					{
						mDescr = checkNull(rs.getString("descr"));
						stkOpt = checkNull(rs.getString("stk_opt"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
									
					if ("1".equals(stkOpt))
					{											
						valueXmlString.append("<lot_no>").append("<![CDATA[" + "               " + "]]>").append("</lot_no>");
						setNodeValue(dom, "lot_no", getAbsString(String.valueOf("               ")));
						valueXmlString.append("<lot_sl>").append("<![CDATA[" + "     " + "]]>").append("</lot_sl>");
						setNodeValue(dom, "lot_sl", getAbsString(String.valueOf("     ")));
					}
					valueXmlString.append("<item_descr>").append("<![CDATA[" + mDescr + "]]>").append("</item_descr>");
					setNodeValue(dom, "item_descr", getAbsString(mDescr));							
				}
				else if(currentColumn.trim().equalsIgnoreCase("loc_code"))
				{
					locCode = checkNull(genericUtility.getColumnValue("loc_code", dom));
					sql = "select descr from location where loc_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, locCode);
					rs = pstmt.executeQuery();				
					if(rs.next())
					{
						loc = checkNull(rs.getString("descr"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;								
					valueXmlString.append("<location_descr>").append("<![CDATA[" + loc + "]]>").append("</location_descr>");
					setNodeValue(dom, "location_descr", getAbsString(loc));			
				}
				else if(currentColumn.trim().equalsIgnoreCase("lot_sl") || currentColumn.trim().equalsIgnoreCase("lot_no"))
				{
					msiteCode  = checkNull(genericUtility.getColumnValue("site_code", dom1));
					itemCode   = checkNull(genericUtility.getColumnValue("item_code", dom));
					locCode    = checkNull(genericUtility.getColumnValue("loc_code", dom));
					lotNo 	   = checkNull(genericUtility.getColumnValue("lot_no", dom));
					lotSl 	   = checkNull(genericUtility.getColumnValue("lot_sl", dom));
					ShipRetQty =  Double.parseDouble(genericUtility.getColumnValue("quantity", dom));
					avail 	   =  checkNull(genericUtility.getColumnValue("available_yn", dom1));
					
					if( lotNo == null || lotNo.trim().length() == 0 )
					{	 lotNo = "               ";
					}
					if( lotSl == null || lotSl.trim().length() == 0 )
					{	 lotSl = "     ";				
					}
					sql = "SELECT COUNT(a.item_code) as count" 
							+ " FROM stock a, invstat b" 
							+ " WHERE a.inv_stat = b.inv_stat" 
							+ " AND a.item_code  = ?" 
							+ " AND a.site_code  = ?" 
							+ " AND a.loc_code   = ?" 
							+ " AND a.lot_no     = ?" 
							+ " AND a.lot_sl     = ?" 
							+ " AND b.available  = ?" 
							+ " AND b.usable     = ?"; 
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCode);
					pstmt.setString(2, msiteCode);
					pstmt.setString(3, locCode);
					pstmt.setString(4, lotNo);
					pstmt.setString(5, lotSl);
					pstmt.setString(6, avail);
					pstmt.setString(7, avail);
					rs = pstmt.executeQuery();				
					if(rs.next())
					{
						mCount = rs.getInt("count");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;					
					packInstr = "";
					
					sql = " SELECT a.site_code__mfg,"
							+ " a.mfg_date,  a.exp_date,  a.pack_code,  a.potency_perc," 
							+ " a.batch_no,  a.grade,  a.dimension,  a.supp_code__mfg," 
							+ " a.retest_date,  a.quantity,  a.gross_weight,  a.tare_weight,"
							+ " a.net_weight,  a.pack_instr,  a.pallet_wt,  a.rate,"
							+ " a.gross_wt_per_art,  a.tare_wt_per_art,  a.qty_per_art"
							+ " FROM stock a,   invstat b"
							+ " WHERE a.inv_stat = b.inv_stat"
							+ " AND a.item_code  = ? "
							+ " and a.site_code  = ? "
							+ " AND a.loc_code   = ? "
							+ " AND a.lot_no     = ? "
							+ " and a.lot_sl     = ? "
							//+ " AND b.available  = 'Y'"; commented by nandkumar gadkari on 11/09/19
							+ " AND b.available  = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCode);
					pstmt.setString(2, msiteCode);
					pstmt.setString(3, locCode);
					pstmt.setString(4, lotNo);
					pstmt.setString(5, lotSl);
					pstmt.setString(6, avail); //added by nandkumar gadkari on 11/09/19
					rs = pstmt.executeQuery();				
					while(rs.next())
					{
						mSiteCodeMfg = checkNull(rs.getString("site_code__mfg"));
						mfgDate		 = rs.getTimestamp("mfg_date");
						expDate 	 = rs.getTimestamp("exp_date");
						mPackCode 	 = checkNull(rs.getString("pack_code"));
						potencyPerc  = rs.getDouble("potency_perc");
						batchNo 	 = checkNull(rs.getString("batch_no"));
						grade		 = checkNull(rs.getString("grade"));
						dimension    = checkDouble(rs.getString("dimension"));
						suppCodeMfg  = checkNull(rs.getString("supp_code__mfg"));
						retestDate   = rs.getTimestamp("retest_date");
						stockQty     = rs.getDouble("quantity");
						stockGrossWeight = rs.getDouble("gross_weight");
						stockTareWeight = rs.getDouble("tare_weight");
						stockNetWeight = rs.getDouble("net_weight");
						packInstr 	 = checkNull(rs.getString("pack_instr"));
						palletWt	 = rs.getDouble("pallet_wt");
						costRate     = rs.getDouble("rate");
						grossWeightArt = rs.getDouble("gross_wt_per_art");
						tareWeightArt= rs.getDouble("tare_wt_per_art");
						qtyPerArt    = rs.getDouble("qty_per_art");						  					   				
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if (mPackCode == null || mCount == 0 )
					{
						sql = "Select pack_code from item where item_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							mPackCode = checkNull(rs.getString("pack_code"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					} 
	
					if (mCount > 0 )
					{
						valueXmlString.append("<cost_rate>").append("<![CDATA[" + costRate + "]]>").append("</cost_rate>");
						setNodeValue(dom, "cost_rate", getAbsString(String.valueOf(costRate)));					
						valueXmlString.append("<site_code__mfg>").append("<![CDATA[" + mSiteCodeMfg + "]]>").append("</site_code__mfg>");
						setNodeValue(dom, "site_code__mfg", getAbsString(mSiteCodeMfg));
						if(mfgDate != null)	{
							valueXmlString.append("<mfg_date>").append("<![CDATA[" + sdf.format(mfgDate) + "]]>").append("</mfg_date>");
							setNodeValue(dom, "mfg_date", getAbsString(sdf.format(mfgDate)));
						}else{
							valueXmlString.append("<mfg_date>").append("<![CDATA[]]>").append("</mfg_date>");
						}
						if(expDate != null){							
						valueXmlString.append("<exp_date>").append("<![CDATA[" + sdf.format(expDate) + "]]>").append("</exp_date>");
						setNodeValue(dom, "exp_date", getAbsString(sdf.format(expDate)));					
						}else{
							valueXmlString.append("<exp_date>").append("<![CDATA[]]>").append("</exp_date>");
						}
						valueXmlString.append("<pack_code>").append("<![CDATA[" + mPackCode + "]]>").append("</pack_code>");
						setNodeValue(dom, "pack_code", getAbsString(mPackCode));					
						valueXmlString.append("<potency_perc>").append("<![CDATA[" + potencyPerc + "]]>").append("</potency_perc>");
						setNodeValue(dom, "potency_perc", getAbsString(String.valueOf(potencyPerc)));					
						valueXmlString.append("<batch_no>").append("<![CDATA[" + batchNo + "]]>").append("</batch_no>");
						setNodeValue(dom, "batch_no", getAbsString(batchNo));					
						valueXmlString.append("<grade>").append("<![CDATA[" +grade  + "]]>").append("</grade>");
						setNodeValue(dom, "grade", getAbsString(grade));					
						if(retestDate!= null){
						valueXmlString.append("<retest_date>").append("<![CDATA[" + sdf.format(retestDate) + "]]>").append("</retest_date>");
						setNodeValue(dom, "retest_date", getAbsString(sdf.format(retestDate)));					
						}else{
							valueXmlString.append("<retest_date>").append("<![CDATA[]]>").append("</retest_date>");
						}
						valueXmlString.append("<dimension>").append("<![CDATA[" + dimension + "]]>").append("</dimension>");
						setNodeValue(dom, "dimension", getAbsString(dimension));					
						valueXmlString.append("<supp_code__mfg>").append("<![CDATA[" + suppCodeMfg + "]]>").append("</supp_code__mfg>");
						setNodeValue(dom, "supp_code__mfg", getAbsString(suppCodeMfg));					
						valueXmlString.append("<pallet_wt>").append("<![CDATA[" + palletWt + "]]>").append("</pallet_wt>");
						setNodeValue(dom, "pallet_wt", getAbsString(String.valueOf(palletWt)));
													
						ShipRetQty = getDoubleValue(genericUtility.getColumnValue("quantity", dom));
					
						if (qtyPerArt == 0)
						{
							qtyPerArt = 1;
						}
			
						if(stockQty > 0)					
						{
							grossWeight = (grossWeightArt/qtyPerArt) * ShipRetQty;
							tareWeight  = (tareWeightArt/qtyPerArt) * ShipRetQty;
							netWeight  = grossWeight -  tareWeight;
							
							valueXmlString.append("<gross_weight>").append("<![CDATA[" + grossWeight + "]]>").append("</gross_weight>");
							setNodeValue(dom, "gross_weight", getAbsString(String.valueOf(grossWeight)));						
							valueXmlString.append("<net_weight>").append("<![CDATA[" + netWeight + "]]>").append("</net_weight>");
							setNodeValue(dom, "net_weight", getAbsString(String.valueOf(netWeight)));						
							valueXmlString.append("<tare_weight>").append("<![CDATA[" + tareWeight + "]]>").append("</tare_weight>");
							setNodeValue(dom, "tare_weight", getAbsString(String.valueOf(tareWeight)));						
						}
	
						mRate = getDoubleValue(genericUtility.getColumnValue("rate", dom));
					}//end of mcount
					else
					{									
						valueXmlString.append("<cost_rate>").append("<![CDATA[" + costRate + "]]>").append("</cost_rate>");
						setNodeValue(dom, "cost_rate", getAbsString(String.valueOf(costRate)));					
						valueXmlString.append("<site_code__mfg>").append("<![CDATA[]]>").append("</site_code__mfg>");					
						valueXmlString.append("<mfg_date>").append("<![CDATA[]]>").append("</mfg_date>");						
						valueXmlString.append("<exp_date>").append("<![CDATA[]]>").append("</exp_date>");
						if (mPackCode == null || mPackCode.trim().length() == 0 )
						{	
							mPackCode = ""; //mNull;
						}
						valueXmlString.append("<pack_code>").append("<![CDATA[" + mPackCode + "]]>").append("</pack_code>");
						setNodeValue(dom, "pack_code", getAbsString(mPackCode));
						valueXmlString.append("<potency_perc>").append("<![CDATA[]]>").append("</potency_perc>");
						valueXmlString.append("<batch_no>").append("<![CDATA[]]>").append("</batch_no>");											
						valueXmlString.append("<grade>").append("<![CDATA[]]>").append("</grade>");
					}
					packInstr1 = checkNull(genericUtility.getColumnValue("pack_instr", dom)); 
					if(packInstr1 == null ||packInstr1.trim().length() == 0)
					{
						valueXmlString.append("<pack_instr>").append("<![CDATA["  + packInstr + "]]>").append("</pack_instr>");
						setNodeValue(dom, "pack_instr", getAbsString(packInstr));									
					}
					reStr = itemChanged(dom, dom1, dom2, objContext, "quantity", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail2>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail2>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);
												
					mDistOrder  = checkNull(genericUtility.getColumnValue("dist_order", dom));
					lineNoDist = checkNull(genericUtility.getColumnValue("line_no_dist_order" , dom));					
					sql = "select rate, unit from distorder_det where dist_order = ? and line_no = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mDistOrder);
					pstmt.setString(2, lineNoDist);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						mRate = rs.getDouble("rate");
						unit = checkNull(rs.getString("unit"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println("mRate from distorder_det["+mRate+"]");
					if( mRate == 0)
					{
						mDistOrder = checkNull(genericUtility.getColumnValue("dist_order", dom1)); // from header
						mTranDateStr  = genericUtility.getColumnValue("tran_date" ,dom1);// from header
						msiteCode  = checkNull(genericUtility.getColumnValue("site_code",dom1));// from header
						itemCode  = checkNull(genericUtility.getColumnValue("item_code", dom));
						lotNo	   = checkNull(genericUtility.getColumnValue("lot_no", dom));
						locCode	= checkNull(genericUtility.getColumnValue("loc_code",dom));
						lotSl 		=  checkNull(genericUtility.getColumnValue("lot_sl",dom));
						if(mTranDateStr != null)
						{						
							mTranDate = Timestamp.valueOf(genericUtility.getValidDateString(mTranDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
						}
						sql = "select price_list from distorder where dist_order = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mDistOrder);					
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							priceList = checkNull(rs.getString("price_list"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;						
						//MODIFIED PRICE LIST LOGIC FOR PICKING RATE FROM INVENTORY IF THE 
						//PRICE LIST IS OF INVENTORY TYPE........MANOJ ......11/09/02
						//CHECK WHETHER PRICE LIST IS OF TYPE I OR OTHER THAN I
						if (priceList == null || priceList.trim().length() == 0)
						{
							//	mRate = 0  //price rate set from master and if not get then set from dist order,and protect to change
							//dw_detedit[ii_currformno].setitem(1,"rate",mRate)														
							valueXmlString.append("<rate>").append("<![CDATA[" + mRate + "]]>").append("</rate>");
							setNodeValue(dom, "rate", getAbsString(String.valueOf(mRate)));						
						}
						else
						{
							sql = "select count(*) as count from pricelist where  price_list = ? and list_type  = 'I'";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, priceList);					
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								mCount = rs.getInt("count");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							if (mCount == 0 ) // PRICE LIST DEFINED IS NOT OF TYPE I
							{
								System.out.println("1935 @@ mCount::["+mCount+"]"); 
								listType =  distCommon.getPriceListType(priceList, conn);
								if(!"L".equals(listType))
								{		
									if (priceList != null &&  priceList.trim().length() > 0) 
									{
										//mRate = i_nvo_gbf_func.gbf_pick_rate(priceList,mTranDate,itemCode,lotNo,'D',ShipRetQty)
										mRate = distCommon.pickRate(priceList, mTranDateStr, itemCode, lotNo, "D", ShipRetQty, conn);
										System.out.println("Zero Rate from distorder so PickRate priceList=["+mRate+"]");
									}
									if (mRate == -1) 									
									{
										sql = "select price_list__parent from pricelist	where price_list = ?";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, priceList);					
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											priceListParent = rs.getString("price_list__parent");
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										if(priceListParent == null || priceListParent.trim().length() == 0 )
										{																			
										}
										else
										{												
											sql = "select list_type from pricelist " 
												+ " where price_list = ?  and item_code = ? " 
												+ " and eff_from <= ? and valid_upto >= ? ";																
											pstmt = conn.prepareStatement(sql);
											pstmt.setString(1, priceListParent);
											pstmt.setString(2, itemCode);
											pstmt.setTimestamp(3, mTranDate);
											pstmt.setTimestamp(4, mTranDate);
											rs = pstmt.executeQuery();
											while(rs.next())
											{
												listType = rs.getString("list_type");
											}
											rs.close();
											rs = null;
											pstmt.close();
											pstmt = null;			
											//mRate = i_nvo_gbf_func.gbf_pick_rate(priceListParent,mTranDate,itemCode,lotNo,'D',ShipRetQty)
											mRate = distCommon.pickRate(priceListParent, mTranDateStr, itemCode, lotNo, "D", ShipRetQty, conn);
											System.out.println("Zero Rate from distorder so PickRate PriceListParent=["+mRate+"]listType["+listType+"]");
											if("B".equals(listType) && mRate == -1)
											{
												mRate = 0;
												
											}
										}
									}
								}																	
							}
							else
							{ // PRICE LIST DEFINED IS OF I TYPE SO PICK RATE FROM INVENTORY								
								if (lotSl == null || lotSl.trim().length() == 0 )
								{
									lotSl = " ";
								}							
								if (priceList == null  || priceList.trim().length() == 0)
								{
								}
								else
								{
									//Changed By Pavan R 13apr2020 start [changes in sql and added if condition]
									//mRate = i_nvo_gbf_func.gbf_pick_rate(priceList,mTranDate,itemCode,msiteCode+'~t'+locCode+'~t'+lotNo+'~t'+lotSl,'I',ShipRetQty) //Added on 07/04/2k5 by Ruchira
									//mRate = distCommon.pickRate(priceList, mTranDateStr, itemCode, msiteCode+"~t"+locCode+"~t"+lotNo+"~t"+lotSl, "I", ShipRetQty, conn);
									//System.out.println("2016 Rate from pickRate::["+mRate+"]");
									sql ="select list_type from pricelist_mst where price_list = ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, priceList);					
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										listType = rs.getString("list_type");
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									if("I".equals(listType))
									{
										mRate = distCommon.pickRate(priceList, mTranDateStr, itemCode, msiteCode+"~t"+locCode+"~t"+lotNo+"~t"+lotSl, "I", ShipRetQty, conn);
										System.out.println("2016 Rate from pickRate::["+mRate+"]");
									}
									//Changed By Pavan R 13apr2020 start [changes in sql and added if condition]
								}	
							}	
						}	
						if (priceList != null && priceList.trim().length() > 0 )
						{
							valueXmlString.append("<rate>").append("<![CDATA[" + mRate + "]]>").append("</rate>");
							setNodeValue(dom, "rate", getAbsString(String.valueOf(mRate)));
							
							//gbf_itemchanged_logic(as_form_no,"rate",as_editflag);
							reStr = itemChanged(dom, dom1, dom2, objContext, "rate", editFlag, xtraParams);
							pos = reStr.indexOf("<Detail2>");
							reStr = reStr.substring(pos + 9);
							pos = reStr.indexOf("</Detail2>");
							reStr = reStr.substring(0, pos);
							valueXmlString.append(reStr);
						}																	
						//rate__clg from picelist price_list__clg
						mDistOrder = checkNull(genericUtility.getColumnValue("dist_order", dom));
						sql = "select price_list__clg from distorder where  dist_order = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mDistOrder);											
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							priceListClg = checkNull(rs.getString("price_list__clg"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						// PRICE LIST CLG DEFINED IS OF I TYPE SO PICK RATE FROM INVENTORY
						if (priceListClg != null && priceListClg.trim().length() > 0)
						{
							//Changed By Pavan R 13apr2020 start [changes in sql and if condition]
							//sql ="select count(*) as count from pricelist where price_list = ? and list_type = 'I'";
							sql ="select list_type from pricelist_mst where price_list = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, priceListClg);					
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								listType = rs.getString("list_type");//mCount = rs.getInt("count");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if("I".equals(listType))//if (mCount > 0) 
							{ //Changed By Pavan R 13apr2020 end [changes in sql and if condition]
								if(lotSl == null || lotSl.trim().length() == 0 )
								{	
									lotSl = " ";
								}						
								//mRateClg = i_nvo_gbf_func.gbf_pick_rate(priceListClg,mTranDate,itemCode,msiteCode+'~t'+locCode+'~t'+lotNo+'~t'+lotSl,'I',ShipRetQty) ;
								mRateClg = distCommon.pickRate(priceListClg, mTranDateStr, itemCode, msiteCode+"~t"+locCode+"~t"+lotNo+"~t"+lotSl, "I", ShipRetQty, conn);
								System.out.println("2072 Rate fro pickRate::["+mRateClg+"]");
							}
							else
							{
								//Changed By Pavan R 13apr2020 start [to get and pass list type from getPriceListType() instead if hard code]
								//mRateClg = distCommon.pickRate(priceListClg, mTranDateStr, itemCode, lotNo, "I", ShipRetQty, conn);
								listType = distCommon.getPriceListType(priceListClg, conn);
								mRateClg = distCommon.pickRate(priceListClg, mTranDateStr, itemCode, lotNo, listType, ShipRetQty, conn);
								//Changed By Pavan R 13apr2020 end [to get and pass list type from getPriceListType() instead if hard code]
								System.out.println("2077 Rate fro pickRate::["+mRateClg+"]");
							}						
							valueXmlString.append("<rate__clg>").append("<![CDATA[" + mRateClg + "]]>").append("</rate__clg>");
							setNodeValue(dom, "rate__clg", getAbsString(String.valueOf(mRateClg)));					
						}
						else
						{
							lineNoDist = checkNull(genericUtility.getColumnValue("line_no_dist_order", dom));
							mDistOrder = checkNull(genericUtility.getColumnValue("dist_order", dom));
							
							sql = "select rate__clg From distorder_det Where dist_order	= ?	and	line_no	= ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, mDistOrder);
							pstmt.setString(2, lineNoDist);									
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								mRateClg = rs.getDouble("rate__clg");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if (mRateClg == 0 ) 
							{
								//Added if condition to change the rate entered by user only if price list is defined for the item.
								if (priceList != null && priceList.trim().length() > 0)
								{
									valueXmlString.append("<rate__clg>").append("<![CDATA[" + mRate + "]]>").append("</rate__clg>");
									setNodeValue(dom, "rate__clg", getAbsString(String.valueOf(mRate)));
								}
							}
							else
							{						
								valueXmlString.append("<rate__clg>").append("<![CDATA[" + mRateClg + "]]>").append("</rate__clg>");
								setNodeValue(dom, "rate__clg", getAbsString(String.valueOf(mRateClg)));
							}
						}
					}//rate==0
					//gbf_itemchanged_logic(as_form_no,"",as_editflag)
					reStr = itemChanged(dom, dom1, dom2, objContext, "rate", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail2>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail2>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);
						
				}else if(currentColumn.trim().equalsIgnoreCase("gross_weight") || currentColumn.trim().equalsIgnoreCase("net_weight"))
				{	
					grossWeight = getDoubleValue(genericUtility.getColumnValue("gross_weight",dom));
					netWeight   = getDoubleValue(genericUtility.getColumnValue("net_weight",dom));
					tareWeight  = grossWeight - netWeight;				
					
					System.out.println("Gross_Weigh values :: grossWeight["+grossWeight+"] netWeight["+netWeight+"] tareWeight["+tareWeight+"]" );				
					valueXmlString.append("<tare_weight>").append("<![CDATA[" + tareWeight + "]]>").append("</tare_weight>");
					setNodeValue(dom, "tare_weight", getAbsString(String.valueOf(tareWeight)));							
					
				}else if(currentColumn.trim().equalsIgnoreCase("tare_weight"))
				{
					grossWeight = getDoubleValue(genericUtility.getColumnValue("gross_weight",dom));
					tareWeight  = getDoubleValue(genericUtility.getColumnValue("tare_weight",dom));
					netWeight 	= grossWeight - tareWeight;				
					
					System.out.println("Tare_Weight values :: grossWeight["+grossWeight+"] netWeight["+netWeight+"] tareWeight["+tareWeight+"]" );				
					valueXmlString.append("<net_weight>").append("<![CDATA[" + netWeight + "]]>").append("</net_weight>");
					setNodeValue(dom, "net_weight", getAbsString(String.valueOf(netWeight)));															
				}
				else if(currentColumn.trim().equalsIgnoreCase("unit__alt") || currentColumn.trim().equalsIgnoreCase("qty_order__alt"))
				{
					qtyQrderAlt = getDoubleValue(genericUtility.getColumnValue("qty_order__alt", dom));
					unitAlt		= checkNull(genericUtility.getColumnValue("unit__alt", dom));
					unit		= checkNull(genericUtility.getColumnValue("unit", dom));
					itemCode	= checkNull(genericUtility.getColumnValue("item_code", dom));
					fact = 0;
					System.out.println("Unit__Alt::Qty_Order__Alt values :: qtyQrderAlt["+qtyQrderAlt+"] unitAlt["+unitAlt+"] unit["+unit+"] itemCode["+itemCode+"]" );
					//quantity = gf_conv_qty_fact(unitAlt, unit, itemCode, qtyQrderAlt, fact)
					ArrayList ratestduomArr = null;
					ratestduomArr = distCommon.getConvQuantityFact(unitAlt, unit, itemCode, qtyQrderAlt, fact, conn);				
					System.out.println("Unit__Alt::Qty_Order__Alt values ::ratestduomArr["+ratestduomArr+"]");
					
					valueXmlString.append("<conv__qty__alt>").append("<![CDATA[" + ratestduomArr.get(0).toString() + "]]>").append("</conv__qty__alt>");
					setNodeValue(dom, "conv__qty__alt", getAbsString(String.valueOf(ratestduomArr.get(0).toString())));				
					valueXmlString.append("<quantity>").append("<![CDATA[" + ratestduomArr.get(1) + "]]>").append("</quantity>");
					setNodeValue(dom, "quantity", getAbsString(String.valueOf(ratestduomArr.get(1).toString())));
					ratestduomArr = null;
					
				}else if(currentColumn.trim().equalsIgnoreCase("rate"))
				{
					System.out.println("Inside Rate itmChange["+genericUtility.getColumnValue("rate",dom)+"]");
					mQty    = getDoubleValue(genericUtility.getColumnValue("quantity", dom));
					mRate   = getDoubleValue(genericUtility.getColumnValue("rate",dom));
					mtaxamt = getDoubleValue(genericUtility.getColumnValue("tax_amt",dom));
					mdisamt = getDoubleValue(genericUtility.getColumnValue("disc_amt", dom));
					fact    = getDoubleValue(genericUtility.getColumnValue("conv__qty__alt",dom));
					mtotamt = (mQty * mRate);
					mnetAmt = mtotamt + mtaxamt - mdisamt;
					System.out.println("Rate >> mQty["+mQty+"] mRate["+mRate+"] mtaxamt["+mtaxamt+"] mdisamt["+mdisamt+"] fact["+fact+"] mtotamt["+mtotamt+"] mnet_amt["+mnetAmt+"]");																			
					
					valueXmlString.append("<amount>").append("<![CDATA[" + mtotamt + "]]>").append("</amount>");
					setNodeValue(dom, "amount", getAbsString(String.valueOf(mtotamt)));				
					valueXmlString.append("<net_amt>").append("<![CDATA[" + mnetAmt + "]]>").append("</net_amt>");
					setNodeValue(dom, "net_amt", getAbsString(String.valueOf(mnetAmt)));
					
					mRateClg = getDoubleValue(genericUtility.getColumnValue("rate__clg", dom));			
					if (mRateClg == 0 )
					{	
						valueXmlString.append("<rate__clg>").append("<![CDATA[" + mRate + "]]>").append("</rate__clg>");
						setNodeValue(dom, "rate__clg", getAbsString(String.valueOf(mRate)));					
					}
					tmpAmt1 = mRate * fact;
					System.out.println("3679::tmpAmt1["+tmpAmt1+"]");
					valueXmlString.append("<rate__alt>").append("<![CDATA[" + tmpAmt1 + "]]>").append("</rate__alt>");
					setNodeValue(dom, "rate__alt", getAbsString(String.valueOf(tmpAmt1)));											
					if (fact > 0)
					{	tmpAmt = 1/fact;					
						valueXmlString.append("<conv__rate_alt>").append("<![CDATA[" + tmpAmt + "]]>").append("</conv__rate_alt>");
						setNodeValue(dom, "conv__rate_alt", getAbsString(String.valueOf(tmpAmt)));
						tmpAmt = 0;
					}
				}else if(currentColumn.trim().equalsIgnoreCase("rate__alt"))
				{					
					qtyQrderAlt = getDoubleValue(genericUtility.getColumnValue("qty_order__alt", dom));
					rateAlt   	= getDoubleValue(genericUtility.getColumnValue("rate__alt", dom));
					fact		= 0;
					mtaxamt 	= getDoubleValue(genericUtility.getColumnValue("tax_amt", dom));
					mdisamt 	= getDoubleValue(genericUtility.getColumnValue("disc_amt", dom));
					unitAlt		= checkNull(genericUtility.getColumnValue("unit__alt", dom));
					unit		= checkNull(genericUtility.getColumnValue("unit", dom));
					itemCode	= checkNull(genericUtility.getColumnValue("item_code", dom));
					mQty    	= getDoubleValue(genericUtility.getColumnValue("quantity", dom));
									
					//convRateAlt = gf_conv_qty_fact(unit, unitAlt , itemCode, qtyQrderAlt, fact)
					ArrayList ratestduomArr = null;
					ratestduomArr = distCommon.getConvQuantityFact(unit, unitAlt, itemCode, qtyQrderAlt, fact, conn);					
					mRate = rateAlt * fact;
					mtotamt  = qtyQrderAlt * rateAlt;
					mnetAmt = mtotamt + mtaxamt - mdisamt;
					//if(ratestduomArr.size() > 1)
					//{
					//	valueXmlString.append("<conv__rate_alt>").append("<![CDATA[" + String.valueOf(ratestduomArr.get(0)) + "]]>").append("</conv__rate_alt>");
					//}else{
						valueXmlString.append("<conv__rate_alt>").append("<![CDATA[" + ratestduomArr.get(0).toString() + "]]>").append("</conv__rate_alt>");
					//}
					setNodeValue(dom, "conv__rate_alt", getAbsString(String.valueOf(ratestduomArr.get(0).toString())));									
					valueXmlString.append("<rate>").append("<![CDATA[" + mRate + "]]>").append("</rate>");
					setNodeValue(dom, "rate", getAbsString(String.valueOf(mRate)));					
					valueXmlString.append("<amount>").append("<![CDATA[" + mtotamt + "]]>").append("</amount>");				
					setNodeValue(dom, "amount", getAbsString(String.valueOf(mtotamt)));													
					valueXmlString.append("<net_amt>").append("<![CDATA[" + mnetAmt + "]]>").append("</net_amt>");
					setNodeValue(dom, "net_amt", getAbsString(String.valueOf(mnetAmt)));					
					mRateClg = getDoubleValue(genericUtility.getColumnValue("rate__clg", dom));
					if(mRateClg == 0)
					{					
						valueXmlString.append("<rate__clg>").append("<![CDATA[" + mRate + "]]>").append("</rate__clg>");
						setNodeValue(dom, "rate__clg", getAbsString(String.valueOf(mRate)));					
					}
					ratestduomArr = null;
													
				}
				valueXmlString.append("</Detail2>");
				break;
			}	
			valueXmlString.append("</Root>");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage());
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
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String errString = "";
		try
		{
			if(xmlString != null && xmlString.trim().length() > 0)
			   {
				  dom =  genericUtility.parseString(xmlString);
			   }
			   if(xmlString1 != null && xmlString1.trim().length() > 0)
			   {
				   dom1 = genericUtility.parseString(xmlString1);
			   }
			   if(xmlString2 != null && xmlString2.trim().length() > 0)
			   {
				   dom2 = genericUtility.parseString(xmlString2);
			   }
			   errString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams);
			   System.out.println("retString:::"+errString);
		}
		catch(Exception e)
		{	
			errString = genericUtility.createErrorString(e);
			e.printStackTrace();
			throw new ITMException(e);
		}
		return(errString);
	}
	
	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		int ctr=0;
		int childNodeListLength;
		int currentFormNo = 0;
		int cnt = 0;
		NodeList parentNodeList = null;
		NodeList parentNodeListTemp = null;
		NodeList childNodeList = null;	
		Node parentNode = null;
		Node parentNodeTemp = null;
		Node childNode = null;//Added By Mukesh Chauhan on 02/01/2020 START
		NodeList detail2List = null;
		NodeList childDetilList=null;
		Node detailNode = null;
		Node chidDetailNode= null;
		String itemCodeDet="" ,locCodeDet="",lotSlDet="",lotNoDet="",updateFlag = "";
		int lineNoCur=0,lineValueInt=0;//Added By Mukesh Chauhan on 02/01/2020 END
		String siteCode = "";
		String userId = "";
		String sql="";
		String errCode="";
		String errorType = "";
		String childNodeName = null;
		String errString = "";
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement pstmt = null ;
		ResultSet rs = null;
		String existFlag = "",projCodeOrd="",projCode="",siteCodeBil="",locCode="",tranDate="",availableYN="",chkDate="",expDate="",lotNo="",transitLoc="";
		String distOrder = "",tranType="",distIss="",confirm="",status="",tranTypeParent="",tranTypeOrd="",tranTypeOrderParent="",tranTypeIssueParent="";
		String site_code="",loginSiteCode="",mVal="",tranCode="",currCode="",shipmentId="",rdPermitNo="",siteDlv="",stateFrom="",stateTo="",permitReqd="",stanCd="";
		String invStatAvailable="",invStat="",siteCodeMfg="",suppSour="",itemCode="",mfgDate="",track="",packCode="",checkIntegralQty="",lotSl="",stkOpt="";
		java.util.Date chkDate1 = null, expDate1 = null;
		String tranDateStr="";
		int mminShelfLife=0,parentNodeListLength = 0;
		String	lineNoDOrd="",lineNo="";
		double rate=0.0,quantity=0.0,totQty=0.0,balQty=0.0,overShipPerc=0.0,qtyBrow=0.0,integralQty=0.0,modQty=0.0,stkQty=0.0,oldQty=0.0,qtyOrderAlt=0.0,rateAlt=0.0,batchSize=0.0,mBatchSize=0.0;
	    Timestamp TranDate=null;
		boolean stkExpFlag = false;
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>"); 
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			conn = getConnection();
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			if (objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			
			switch (currentFormNo)
			{
				
				case 1:

					parentNodeList = dom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
					for(ctr = 0; ctr < childNodeListLength; ctr ++)
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						if("dist_order".equalsIgnoreCase(childNodeName))
						{
							distOrder = checkNullAndTrim(genericUtility.getColumnValue("dist_order", dom)); 
							tranType = checkNullAndTrim(genericUtility.getColumnValue("tran_type", dom)); 
							distIss = checkNullAndTrim(genericUtility.getColumnValue("tran_id", dom)); 
							
								sql = "SELECT COUNT(*) FROM DISTORDER WHERE DIST_ORDER = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, distOrder);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									cnt = rs.getInt(1);
									
								}
								pstmt.close(); pstmt = null;
								rs.close(); rs = null;
								if (cnt == 0)
								{
									errCode = "VTDIST2";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								else
								{
									sql = "select confirmed, status from distorder where dist_order = ?";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, distOrder);
									rs = pstmt.executeQuery();
									if (rs.next())
									{
										confirm = rs.getString("confirmed");
										status = rs.getString("status");
										
									}
									pstmt.close(); pstmt = null;
									rs.close(); rs = null;
									if(confirm != null  && !"Y".equalsIgnoreCase(confirm))
									{
										errCode = "VTDIST3";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									else
									{
										if(status != null  && !"P".equalsIgnoreCase(status))
										{	
											sql = "select tran_type__parent from distorder_type where tran_type =  ?";
											pstmt = conn.prepareStatement(sql);
											pstmt.setString(1, tranType);
											rs = pstmt.executeQuery();
											if (rs.next())
											{
												tranTypeParent = checkNullAndTrim(rs.getString("tran_type__parent"));
												
												
											}
											pstmt.close(); pstmt = null;
											rs.close(); rs = null;
											if(tranTypeParent.equalsIgnoreCase(tranType))
											{
												errCode = "VTDIST20";
												errList.add(errCode);
												errFields.add(childNodeName.toLowerCase());
											}
										}
									}
									
								}

							
							
							sql = "Select count(*) From	 distord_iss Where  dist_order = ? and tran_id <> ? and confirmed = 'N'";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, distOrder);
							pstmt.setString(2, distIss);
							rs= pstmt.executeQuery();
							if (rs.next())
							{
								if (rs.getInt(1) > 0)
								{
									errCode = "VTINVDO1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							if(pstmt != null)
							{
								pstmt.close(); 
								pstmt = null;
							}
							if(rs != null)
							{
								rs.close();
								rs = null;
							}
							
							
						}// end of dist_order if block 
						else if(childNodeName.equalsIgnoreCase("tran_type"))
						{    
							distOrder = checkNullAndTrim(genericUtility.getColumnValue("dist_order", dom)); 
							tranType = checkNullAndTrim(genericUtility.getColumnValue("tran_type", dom)); 
							sql = "select count(*) from distorder_type where tran_type = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, tranType);
							rs= pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt(1);
								
							}
							pstmt.close(); pstmt = null;
							rs.close(); rs = null;
							if (cnt == 0)
							{
								errCode = "VMIDOT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							sql = "select tran_type from distorder where dist_order = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, distOrder);
							rs= pstmt.executeQuery();
							if (rs.next())
							{
								tranTypeOrd = rs.getString("tran_type");
								
							}
							pstmt.close(); pstmt = null;
							rs.close(); rs = null;
							sql = "select tran_type__parent from distorder_type where tran_type = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, tranTypeOrd);
							rs= pstmt.executeQuery();
							if (rs.next())
							{
								tranTypeOrderParent = rs.getString("tran_type__parent");
								
							}
							pstmt.close(); pstmt = null;
							rs.close(); rs = null;
							sql = "select tran_type__parent from distorder_type where tran_type = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, tranType);
							rs= pstmt.executeQuery();
							if (rs.next())
							{
								tranTypeIssueParent = rs.getString("tran_type__parent");
								
							}
							pstmt.close(); pstmt = null;
							rs.close(); rs = null;
							
							if(!tranTypeOrderParent.equalsIgnoreCase(tranTypeIssueParent))
							{
								errCode = "VTCRET";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}// end of if block
						else if (childNodeName.equalsIgnoreCase("tran_date"))
						{
							tranDateStr = checkNullAndTrim(genericUtility.getColumnValue("tran_date", dom));
							siteCode = checkNullAndTrim(genericUtility.getColumnValue("site_code", dom));
							System.out.println("@@@@ Tran Date[" + tranDateStr + "]");
							TranDate = Timestamp.valueOf(genericUtility.getValidDateString(tranDateStr.toString(),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");
							System.out.println("Trandate is"+TranDate);
							errCode = finCommon.nfCheckPeriod("DIS", TranDate,siteCode, conn);
							System.out.println("Errorcode in TranDate"+errCode);
							if (errCode != null && errCode.trim().length() > 0)
							  {
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
							  }
						}
						else if(childNodeName.equalsIgnoreCase("site_code"))
						{    
							siteCode = genericUtility.getColumnValue("site_code", dom);
							System.out.println("siteCode: " + siteCode);
							
								if (siteCode != null && siteCode.trim().length() > 0) {
									if (!(isExist(conn, "site", "site_code", siteCode))) {
										errCode = "VTSITE1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
						}// end of if block for site_code
						
						
						
						else if("dist_route".equalsIgnoreCase(childNodeName))
						{
							mVal = checkNullAndTrim(genericUtility.getColumnValue("dist_route", dom)); 
							if (mVal != null && mVal.trim().length() > 0) {
								
								sql = "select count(*) from distroute where dist_route  = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, mVal);
								rs= pstmt.executeQuery();
								if (rs.next())
								{
									cnt = rs.getInt(1);
									
								}
								pstmt.close(); pstmt = null;
								rs.close(); rs = null;
								if (cnt == 0)
								{
									errCode = "VTDISTRT1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							
							
						}// end of if block 
						else if ("site_code__dlv".equalsIgnoreCase(childNodeName)) 
						{
							mVal = checkNullAndTrim(genericUtility.getColumnValue("site_code__dlv", dom)); 
							siteCode = checkNullAndTrim(genericUtility.getColumnValue("site_code", dom)); 
							distOrder = checkNullAndTrim(genericUtility.getColumnValue("dist_order", dom)); 
								
								sql = "select count(*) from site where site_code  = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, mVal);
								rs= pstmt.executeQuery();
								if (rs.next())
								{
									cnt = rs.getInt(1);
									
								}
								pstmt.close(); pstmt = null;
								rs.close(); rs = null;
								if (cnt == 0)
								{
									errCode = "VTSITE1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								else
								{
									errCode = gbfDistOrderSite( distOrder, siteCode, mVal, conn );
									if (errCode != null && errCode.trim().length() > 0)
									  {
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
									  }
								}
							
						}
						else if ("tran_code".equalsIgnoreCase(childNodeName)) 
						{
							tranCode = checkNullAndTrim(genericUtility.getColumnValue("tran_code", dom)); 
							if (tranCode != null && tranCode.trim().length() > 0) {								
								sql = "select count(*) from transporter where tran_code  = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, tranCode);
								rs= pstmt.executeQuery();
								if (rs.next())
								{
									cnt = rs.getInt(1);
									
								}
								pstmt.close(); pstmt = null;
								rs.close(); rs = null;
								if (cnt == 0)
								{
									errCode = "VTTRANCD1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								
							}
						}
						else if ("loc_code__git".equalsIgnoreCase(childNodeName)) 
						{
							mVal = checkNullAndTrim(genericUtility.getColumnValue("loc_code__git", dom)); 
														
								sql = "select count(*) from location where loc_code  = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, mVal);
								rs= pstmt.executeQuery();
								if (rs.next())
								{
									cnt = rs.getInt(1);
									
								}
								pstmt.close(); pstmt = null;
								rs.close(); rs = null;
								if (cnt == 0)
								{
									errCode = "VTLOC1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								
						}
						else if ("curr_code".equalsIgnoreCase(childNodeName)) 
						{
							currCode = checkNullAndTrim(genericUtility.getColumnValue("curr_code", dom)); 						
								sql = "select count(*) from currency where curr_code  = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, currCode);
								rs= pstmt.executeQuery();
								if (rs.next())
								{
									cnt = rs.getInt(1);
									
								}
								pstmt.close(); pstmt = null;
								rs.close(); rs = null;
								if (cnt == 0)
								{
									errCode = "VTCURRCD1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
						}
						else if ("shipment_id".equalsIgnoreCase(childNodeName)) 
						{
							shipmentId = checkNullAndTrim(genericUtility.getColumnValue("shipment_id", dom)); 
							if (shipmentId != null && shipmentId.trim().length() > 0) {								
								sql = "select count(1) from   shipment where  shipment_id  = ? and    confirmed = 'N' ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, shipmentId);
								rs= pstmt.executeQuery();
								if (rs.next())
								{
									cnt = rs.getInt(1);
									
								}
								pstmt.close(); pstmt = null;
								rs.close(); rs = null;
								if (cnt == 0)
								{
									errCode = "VTSHPID";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								
							}
						}
						else if ("rd_permit_no".equalsIgnoreCase(childNodeName)) 
						{
							rdPermitNo =checkNullAndTrim(genericUtility.getColumnValue("rd_permit_no", dom));
							siteCode = checkNullAndTrim(genericUtility.getColumnValue("site_code", dom)); 
							siteDlv = checkNullAndTrim(genericUtility.getColumnValue("site_code__dlv", dom));
							distIss = checkNullAndTrim(genericUtility.getColumnValue("tran_id", dom)); 
								sql = "select a.state_code,b.state_code from site a , site b where a.site_code = ? and b.site_code = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, siteCode);
								pstmt.setString(2, siteDlv);
								rs= pstmt.executeQuery();
								if (rs.next())
								{
									stateFrom = checkNullAndTrim(rs.getString(1));
									stateTo = checkNullAndTrim(rs.getString(2));
									
								}
								pstmt.close(); pstmt = null;
								rs.close(); rs = null;
								if (!stateFrom.equalsIgnoreCase(stateTo))
								{
									sql = "select stan_code from site where site_code = ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, siteDlv);
									rs= pstmt.executeQuery();
									if (rs.next())
									{
										stanCd = checkNullAndTrim(rs.getString("stan_code"));
										
										
									}
									pstmt.close(); pstmt = null;
									rs.close(); rs = null;
									sql = "select rd_permit_reqd from station where stan_code = ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, stanCd);
									rs= pstmt.executeQuery();
									if (rs.next())
									{
										permitReqd = checkNullAndTrim(rs.getString(1));
										
									}
									pstmt.close(); pstmt = null;
									rs.close(); rs = null;
									if ("Y".equalsIgnoreCase(permitReqd))
									{
										if(rdPermitNo.length()< 1)
										{
											errCode = "VTRDPT";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
										else
										{
											sql = "select count(*) from roadpermit where rd_permit_no = ? and status = 'O' and stan_code__to = ? ";
											pstmt = conn.prepareStatement(sql);
											pstmt.setString(1, rdPermitNo);
											pstmt.setString(2, stanCd);
											rs= pstmt.executeQuery();
											if (rs.next())
											{
												cnt = rs.getInt(1);
												
											}
											pstmt.close(); pstmt = null;
											rs.close(); rs = null;
											if (cnt == 0)
											{
												errCode = "VTRDPT1";
												errList.add(errCode);
												errFields.add(childNodeName.toLowerCase());
											}
											else
											{
												sql = "select count(*) from distord_iss where rd_permit_no = ? and site_code__dlv = ? and tran_id <> ? ";
												pstmt = conn.prepareStatement(sql);
												pstmt.setString(1, rdPermitNo);
												pstmt.setString(2, siteDlv);
												pstmt.setString(3, distIss);
												rs= pstmt.executeQuery();
												if (rs.next())
												{
													cnt = rs.getInt(1);
													
												}
												pstmt.close(); pstmt = null;
												rs.close(); rs = null;
												if (cnt > 0)
												{
													errCode = "VTRDPT2";
													errList.add(errCode);
													errFields.add(childNodeName.toLowerCase());
												}
											}
										}
										
									}
								}
								else if(rdPermitNo.length()> 0)
								{
									errCode = "VTRDPT1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								
						}
						else if ("proj_code".equalsIgnoreCase(childNodeName)) 
						{
							projCode = checkNullAndTrim(genericUtility.getColumnValue("proj_code", dom)); 
							if (projCode != null && projCode.trim().length() > 0) {		
								distOrder = checkNullAndTrim(genericUtility.getColumnValue("dist_order", dom)); 
								sql = "select case when proj_code is null then ' ' else proj_code end from distorder where dist_order = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, distOrder);
								rs= pstmt.executeQuery();
								if (rs.next())
								{
									projCodeOrd =checkNullAndTrim(rs.getString(1));
									
								}
								pstmt.close(); pstmt = null;
								rs.close(); rs = null;
								if (projCodeOrd.trim().length() > 0) {		
									if (!projCodeOrd.equalsIgnoreCase(projCode))
									{
										errCode = "VTPROMISMT";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								sql = "select (case when proj_status is null then ' ' else proj_status end) from project where proj_code = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, projCode);
								rs= pstmt.executeQuery();
								if (rs.next())
								{
									status =checkNullAndTrim(rs.getString(1));
									
								}
								pstmt.close(); pstmt = null;
								rs.close(); rs = null;
								/*elseif get_sqlcode() = 100 then
										errcode = "VMPROJCDX"*/
								if (!"A".equalsIgnoreCase(status))
								{
									errCode = "VTPROJ2";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
						else if ("site_code__bil".equalsIgnoreCase(childNodeName)) 
						{
							siteCodeBil = checkNullAndTrim(genericUtility.getColumnValue("site_code__bil", dom)); 	
							if (siteCodeBil != null && siteCodeBil.trim().length() > 0) {	
								sql = "select count(*) from site where site_code = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, siteCodeBil);
								rs= pstmt.executeQuery();
								if (rs.next())
								{
									cnt = rs.getInt(1);
									
								}
								pstmt.close(); pstmt = null;
								rs.close(); rs = null;
								if (cnt == 0)
								{
									errCode = "VTBILSITE";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
						    }
						}
						
					}
				break;
				case 2:
					parentNodeList = dom.getElementsByTagName("Detail2");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
					System.out.println("@@@@@@@@@@@@childNodeListLength["+ childNodeListLength + "]");
					for (ctr = 0; ctr < childNodeListLength; ctr++) 
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						if("dist_order".equalsIgnoreCase(childNodeName))
						{ 
							distOrder = checkNullAndTrim(genericUtility.getColumnValue("dist_order", dom)); 
							distIss = checkNullAndTrim(genericUtility.getColumnValue("tran_id", dom)); 
							tranType = checkNullAndTrim(genericUtility.getColumnValue("tran_type", dom1)); 
							sql = "select tran_type__parent from distorder_type where tran_type =  ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, tranType);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								tranTypeParent = checkNullAndTrim(rs.getString("tran_type__parent"));
								
								
							}
							pstmt.close(); pstmt = null;
							rs.close(); rs = null;
							if(!tranTypeParent.equalsIgnoreCase(tranType))
							{
								sql = "SELECT COUNT(*) FROM DISTORDER WHERE DIST_ORDER = ? AND CONFIRMED = 'Y' ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, distOrder);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									cnt = rs.getInt(1);
									
								}
								pstmt.close(); pstmt = null;
								rs.close(); rs = null;
								
							}
							else
							{
								sql = "SELECT COUNT(*) FROM DISTORDER WHERE DIST_ORDER = ? AND CONFIRMED = 'Y'  AND STATUS = 'P' ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, distOrder);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									cnt = rs.getInt(1);
									
								}
								pstmt.close(); pstmt = null;
								rs.close(); rs = null;
							}
							if (cnt == 0)
							{
								errCode = "VTDIST2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else
							{
								siteCode = checkNullAndTrim(genericUtility.getColumnValue("site_code", dom1)); 
								siteDlv = checkNullAndTrim(genericUtility.getColumnValue("site_code__dlv", dom1));
								errCode = gbfDistOrderSite( distOrder, siteCode, siteDlv, conn );
								if (errCode != null && errCode.trim().length() > 0)
								  {
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
								  }
							}
							tranType = checkNullAndTrim(genericUtility.getColumnValue("tran_type", dom1)); 
							sql = "select tran_type from distorder where dist_order =  ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, distOrder);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								tranTypeOrd = checkNullAndTrim(rs.getString("tran_type"));
								
								
							}
							pstmt.close(); pstmt = null;
							rs.close(); rs = null;
							if(!tranTypeOrd.equalsIgnoreCase(tranType))
							{
								errCode = "VTIDOTT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							sql = "Select count(*) From	 distord_iss Where  dist_order = ? and tran_id <> ? and confirmed = 'N'";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, distOrder);
							pstmt.setString(2, distIss);
							rs= pstmt.executeQuery();
							if (rs.next())
							{
								if (rs.getInt(1) > 0)
								{
									errCode = "VTINVDO1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							if(pstmt != null)
							{
								pstmt.close(); 
								pstmt = null;
							}
							if(rs != null)
							{
								rs.close();
								rs = null;
							}
							
					}// end of if block 
					else if("item_code".equalsIgnoreCase(childNodeName))
						{
							mVal = checkNullAndTrim(genericUtility.getColumnValue("item_code", dom)); 
							locCode = checkNullAndTrim(genericUtility.getColumnValue("loc_code", dom)); 
							siteCode = checkNullAndTrim(genericUtility.getColumnValue("site_code", dom1)); 
							tranDate = checkNullAndTrim(genericUtility.getColumnValue("tran_date", dom1)); 
							String transer =null;
							transer = "D-ISS";
							errCode = itmDBAccessEJB.isItem( siteCode, mVal, transer, conn );
							if (errCode != null && errCode.trim().length() > 0)
							{
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
						    }
							sql = "select (case when min_shelf_life is null then 0 else min_shelf_life end) from item where item_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, mVal);
							rs= pstmt.executeQuery();
							if (rs.next())
							{
								mminShelfLife =rs.getInt(1);
								
							}
							pstmt.close(); pstmt = null;
							rs.close(); rs = null;
							availableYN = checkNullAndTrim(genericUtility.getColumnValue("available_yn", dom1)); 
							String varValue = "",stkExpLoc = "";
							
							varValue = itmDBAccessEJB.getEnvDis("999999","NEAREXP_LOC",conn);
							if(varValue != null && varValue.trim().length() > 0 && !(varValue.equalsIgnoreCase("NULLFOUND")))
							{
								stkExpLoc = varValue;
							}
							stkExpFlag = false;
							String token = "";
							StringTokenizer stToken = new StringTokenizer(stkExpLoc,",");
							while(stToken.hasMoreTokens())
							{
								token = stToken.nextToken();
								if(locCode.equalsIgnoreCase(token))
								{
									stkExpFlag = true;
									break;
								}
							}
							if(stkExpFlag == false){
								if("Y".equalsIgnoreCase(availableYN))
								{
									if(mminShelfLife > 0)
									{
										chkDate = calcExpiry(tranDate,mminShelfLife);
										chkDate1 = sdf.parse(chkDate);
										expDate = genericUtility.getColumnValue("exp_date", dom); 
										if(expDate != null)
										{
											expDate1 = sdf.parse(expDate);
												System.out.println("expDate1 :"+expDate1);
											if (chkDate1.compareTo(expDate1) > 0)
												{
													errCode = "VTSHELF01";
													errList.add(errCode);
													errFields.add(childNodeName.toLowerCase());
												}
											}	
										
									}
								}
							}
							
							lotNo = checkNullAndTrim(genericUtility.getColumnValue("lot_no", dom)); 
							/*li_mth = Month(date(mtrandate))
									if li_mth >=1 and li_mth <=3 then
										ls_nxt_yr = '01-04-' + string(year(date(mtrandate)))					
									else
										ls_nxt_yr = '01-04-' + string(year(date(mtrandate))+1)
									end if
									ldt_nxt_yr = Datetime(Date(ls_nxt_yr))
									select count(*) into :cnt from cenvat 
									 where site_code = :msite_code  and item_code = :mVal and tran_date >= :ldt_nxt_yr 
									 and   lot_no = :mlot_no and tran_type = 'C';
									if get_sqlcode() < 0 then
										errcode = 'DS000'+TRIM(STRING(SQLCA.SQLDBCODE)) + TRIM(SQLCA.SQLERRTEXT)
									end if
									if cnt > 0 then
										errcode = "VTCENITM"
									end if */ //pb code not migrated   
						}// end of if block 
						
						else if("rate".equalsIgnoreCase(childNodeName))
						{
							rate = checkDoubleNull(genericUtility.getColumnValue("rate", dom)); 
							if(rate < 0)
							{
								errCode = "VTRATE1";  
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}// end of if block 
						else if("rate__clg".equalsIgnoreCase(childNodeName))
						{
							rate = checkDoubleNull(genericUtility.getColumnValue("rate__clg", dom)); 
							if(rate < 0)
							{
								errCode = "VTRATE1";  
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}// end of if block 
						else if("loc_code".equalsIgnoreCase(childNodeName))
						{
							locCode = checkNullAndTrim(genericUtility.getColumnValue("loc_code", dom)); 
							sql = "SELECT COUNT(*) from location where loc_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, locCode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt(1);
								
							}
							pstmt.close(); pstmt = null;
							rs.close(); rs = null;
						
							if (cnt == 0)
							{
								errCode = "VTLOC1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else
							{
								transitLoc = checkNullAndTrim(genericUtility.getColumnValue("loc_code__git", dom1)); 
								if(transitLoc.equalsIgnoreCase(locCode))
								{
									errCode = "VTTRANSIT";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								else
								{
									availableYN = checkNullAndTrim(genericUtility.getColumnValue("available_yn", dom1)); 
									sql = "select inv_stat from location where loc_code =  ?";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, locCode);
									rs = pstmt.executeQuery();
									if (rs.next())
									{
										invStat = checkNullAndTrim(rs.getString("inv_stat"));
										
										
									}
									pstmt.close(); pstmt = null;
									rs.close(); rs = null;
									sql = "select available from invstat where inv_stat =  ?";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, invStat);
									rs = pstmt.executeQuery();
									if (rs.next())
									{
										invStatAvailable = checkNullAndTrim(rs.getString("available"));
										
										
									}
									pstmt.close(); pstmt = null;
									rs.close(); rs = null;
									if(!invStatAvailable.equalsIgnoreCase(availableYN))
									{
										errCode = "VTAVAIL";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
							
						}// end of if block 
						else if("tax_class".equalsIgnoreCase(childNodeName))
						{
							mVal = checkNullAndTrim(genericUtility.getColumnValue("tax_class", dom)); 
							if (mVal != null && mVal.trim().length() > 0)
							{
								sql = "Select Count(*) from taxclass where tax_class = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, mVal);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									cnt = rs.getInt(1);
									
								}
								pstmt.close(); pstmt = null;
								rs.close(); rs = null;
							
								if (cnt == 0)
								{
									errCode = "VTTCLASS1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
						    }
						}// end of if block
						else if("tax_chap".equalsIgnoreCase(childNodeName))
						{
							mVal = checkNullAndTrim(genericUtility.getColumnValue("tax_chap", dom)); 
							if (mVal != null && mVal.trim().length() > 0)
							{
								sql = "Select Count(*) from taxchap where tax_chap = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, mVal);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									cnt = rs.getInt(1);
									
								}
								pstmt.close(); pstmt = null;
								rs.close(); rs = null;
							
								if (cnt == 0)
								{
									errCode = "VTTCHAP1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
						    }
						}// end of if block
						else if("tax_env".equalsIgnoreCase(childNodeName))
						{
							//mVal = checkNullAndTrim(genericUtility.getColumnValue("tax_env", dom)); 
							mVal = checkNullAndTrim(distCommon.getParentColumnValue("tax_env", dom, "2"));
							System.out.println("DOIusse 2 tax_env["+mVal+"]");
							if (mVal != null && mVal.trim().length() > 0)
							{
								sql = "Select Count(*) from taxenv where tax_env = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, mVal);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									cnt = rs.getInt(1);
									
								}
								pstmt.close(); pstmt = null;
								rs.close(); rs = null;
							
								if (cnt == 0)
								{
									errCode = "VTTENV1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								else
								{
									tranDate = checkNullAndTrim(genericUtility.getColumnValue("tran_date", dom1)); 		
									if( tranDate != null && tranDate.trim().length() > 0 )
									{
										TranDate = Timestamp.valueOf(genericUtility.getValidDateString(tranDate, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");		
									}
									//Pavan R 17sept19 start[to validate tax environment]
									//errCode = finCommon.checkTaxEnvStatus(mVal, TranDate, conn);
									errCode = distCommon.getCheckTaxEnvStatus(mVal, TranDate, "D", conn);
									System.out.println("#### checkTaxEnvStatus::errCode["+errCode+"]");
									//Pavan R 17sept19 end[to validate tax environment]
									if (errCode != null && errCode.trim().length() > 0)
									{								
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}	
								}
						    }
						}// end of if block
						else if("lot_no".equalsIgnoreCase(childNodeName))//Changed by Mukesh Chauhan on 28/12/19 START
						{
					
							lineNo = checkNull(genericUtility.getColumnValue("line_no", dom));//Added By Mukesh Chauhan on 02/01/2020 START
							itemCode = checkNullAndTrim(genericUtility.getColumnValue("item_code", dom));
							locCode = checkNullAndTrim(genericUtility.getColumnValue("loc_code", dom));
							lotSl = checkNullAndTrim(genericUtility.getColumnValue("lot_sl", dom));
							lotNo = checkNullAndTrim(genericUtility.getColumnValue("lot_no", dom));
							detail2List = dom2.getElementsByTagName("Detail2");//Added By Mukesh Chauhan on 02/01/2020 END
							// 30-05-2020 [Changes done to avoid error in case of stock opt other then 2 and to allow space].Start
							/*
							lotNo = checkNullAndTrim(genericUtility.getColumnValue("lot_no", dom)); 
							if (lotNo == null || lotNo.trim().length() == 0)
							{
							*/
							lotNo = checkNull(genericUtility.getColumnValue("lot_no", dom)); 
							sql = "select stk_opt from item where item_code =  ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								stkOpt = checkNullAndTrim(rs.getString("stk_opt"));
							}
							pstmt.close(); pstmt = null;
							rs.close(); rs = null;
							if (lotNo == null || ("2".equalsIgnoreCase(stkOpt) && lotNo.trim().length() == 0))
								//Added by Poonam B[30-05-2020][Start][To remove syntax error]
							{
								//Added by Poonam B[30-05-2020][End][To remove syntax error]
							// 30-05-2020 [Changes done to avoid error in case of stock opt other then 2 and to allow space].End
									errCode = "VTDIST9";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
						    }
							// 30-05-2020 [Changes done to avoid error in case of stock opt other then 2 and to allow space].Start
							if (!("2".equalsIgnoreCase(stkOpt)))
							{
							// 30-05-2020 [Changes done to avoid error in case of stock opt other then 2 and to allow space].End
								if(lineNo!=null && lineNo.trim().length()>0) //Added By Mukesh Chauhan on 02/01/2020 for not  to allow duplicate stock START
								{
									lineNoCur=Integer.parseInt(lineNo.trim());
								}
								for(int selectAll = 0; selectAll < detail2List.getLength(); selectAll++) {
									  detailNode = detail2List.item(selectAll);
									  childDetilList = detailNode.getChildNodes();
									for (int childDet = 0; childDet < childDetilList.getLength(); childDet++)
									{
										chidDetailNode = childDetilList.item(childDet);
										System.out.println("current child node>>>>>>>>>> " + chidDetailNode.getNodeName());

										
										
										if (chidDetailNode.getNodeName().equalsIgnoreCase("line_no")) 
										{
											System.out.println("line node found >>>>>" + chidDetailNode.getNodeName());
											if (chidDetailNode.getFirstChild() != null) 
											{
												String lineValue = chidDetailNode.getFirstChild().getNodeValue();
												if (lineValue != null && lineValue.trim().length() > 0) 
												{
													lineValueInt = Integer.parseInt(lineValue.trim());
												}
											}
										}

										
										
										if (chidDetailNode.getNodeName().equalsIgnoreCase("attribute")) 
										{
											System.out.println("operation node found >>>>>" + chidDetailNode.getNodeName());
											updateFlag = chidDetailNode.getAttributes().getNamedItem("updateFlag")
													.getNodeValue();

										}
										
										if (chidDetailNode.getNodeName().equalsIgnoreCase("item_code")) 
										{
											if (chidDetailNode.getFirstChild() != null) 
											{
												itemCodeDet =checkNullAndTrim(chidDetailNode.getFirstChild().getNodeValue());
											}
										}
										
										if (chidDetailNode.getNodeName().equalsIgnoreCase("loc_code")) 
										{
											if (chidDetailNode.getFirstChild() != null) 
											{
												locCodeDet = checkNullAndTrim(chidDetailNode.getFirstChild().getNodeValue());
											}
										}
										
										if (chidDetailNode.getNodeName().equalsIgnoreCase("lot_sl")) 
										{
											if (chidDetailNode.getFirstChild() != null) 
											{
												lotSlDet = checkNullAndTrim(chidDetailNode.getFirstChild().getNodeValue());
												
												if (lineNoCur != lineValueInt && !updateFlag.equalsIgnoreCase("D")	&& itemCodeDet.equalsIgnoreCase(itemCode) 
														&& locCodeDet.equalsIgnoreCase(locCode) && lotSlDet.equalsIgnoreCase(lotSl) && lotNoDet.equalsIgnoreCase(lotNo))
												{
													errCode = "VTDUPSTOCK ";
													errList.add(errCode);
													errFields.add(childNodeName.toLowerCase());
												}	
											}
											
										}
										if (chidDetailNode.getNodeName().equalsIgnoreCase("lot_no")) 
										{
											if (chidDetailNode.getFirstChild() != null) 
											{
												lotNoDet = checkNullAndTrim(chidDetailNode.getFirstChild().getNodeValue());
											}
										}
										
									}//Added By Mukesh Chauhan on 02/01/2020  END
								}
							}
						}// end of if block
						//Changed by Mukesh Chauhan on 28/12/19 END
						else if("site_code__mfg".equalsIgnoreCase(childNodeName))
						{
							siteCodeMfg = checkNullAndTrim(genericUtility.getColumnValue("site_code__mfg", dom)); 
							itemCode = checkNullAndTrim(genericUtility.getColumnValue("item_code", dom)); 
							sql = "select (case when supp_sour is null  then 'M' else supp_sour end ) from item where item_code  =  ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								suppSour = checkNullAndTrim(rs.getString(1));
								
								
							}
							pstmt.close(); pstmt = null;
							rs.close(); rs = null;
							if("M".equalsIgnoreCase(suppSour))
							{
								if (siteCodeMfg == null || siteCodeMfg.trim().length() == 0)
								{
									errCode = "VTSITEMFG1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}// end of if block
						else if("mfg_date".equalsIgnoreCase(childNodeName))
						{
							mfgDate = genericUtility.getColumnValue("mfg_date", dom); 
							itemCode = checkNullAndTrim(genericUtility.getColumnValue("item_code", dom)); 
							sql = "select (case when track_shelf_life is null then 'N' else track_shelf_life end ) from item where item_code  =  ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								track = checkNullAndTrim(rs.getString(1));
								
								
							}
							pstmt.close(); pstmt = null;
							rs.close(); rs = null;
							if("Y".equalsIgnoreCase(track))
							{
								if (mfgDate == null || mfgDate.trim().length() == 0)
								{
									errCode = "VTMFGDATE3";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}// end of if block
						else if("exp_date".equalsIgnoreCase(childNodeName))
						{
							expDate = genericUtility.getColumnValue("exp_date", dom); 
							itemCode = checkNullAndTrim(genericUtility.getColumnValue("item_code", dom)); 
							sql = "select (case when track_shelf_life is null then 'N' else track_shelf_life end ) from item where item_code  =  ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								track = checkNullAndTrim(rs.getString(1));
								
							}
							pstmt.close(); pstmt = null;
							rs.close(); rs = null;
							if("Y".equalsIgnoreCase(track))
							{
								if (expDate == null || expDate.trim().length() == 0)
								{
									errCode = "VTEXPDATE1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}// end of if block
						else if("quantity".equalsIgnoreCase(childNodeName))
						{
							distOrder = checkNullAndTrim(genericUtility.getColumnValue("dist_order", dom)); 
							lineNoDOrd = checkNullAndTrim(genericUtility.getColumnValue("line_no_dist_order", dom)); 
							quantity = checkDoubleNull(genericUtility.getColumnValue("quantity", dom)); 
							distIss = checkNullAndTrim(genericUtility.getColumnValue("tran_id", dom));
							if (distIss == null || distIss.trim().length() == 0)
							{
								distIss="@@";
							}
							lineNo = checkNullAndTrim(genericUtility.getColumnValue("line_no", dom)); 
							tranType = checkNullAndTrim(genericUtility.getColumnValue("tran_type", dom1)); 
							siteCode = checkNullAndTrim(genericUtility.getColumnValue("site_code", dom1)); 
							packCode = checkNullAndTrim(genericUtility.getColumnValue("pack_code", dom));
							lotNo = checkNullAndTrim(genericUtility.getColumnValue("lot_no", dom)); 
							
							sql = "select tran_type__parent from distorder_type where tran_type =  ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, tranType);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								tranTypeParent = checkNullAndTrim(rs.getString("tran_type__parent"));
								
								
							}
							pstmt.close(); pstmt = null;
							rs.close(); rs = null;
							sql = "select qty_confirm , over_ship_perc from   distorder_det where  dist_order =  ? and    line_no    = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, distOrder);
							pstmt.setString(2, lineNoDOrd);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								balQty = checkDoubleNull(rs.getString("qty_confirm"));
								overShipPerc = checkDoubleNull(rs.getString("over_ship_perc"));
								
							}
							pstmt.close(); pstmt = null;
							rs.close(); rs = null;
							sql = " select sum(case when quantity is null then 0 else quantity end) from distord_issdet "
								+ " where dist_order = ? and line_no_dist_order = ? and tran_id <> ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, distOrder);
							pstmt.setString(2, lineNoDOrd);
							pstmt.setString(3, distIss);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								totQty = checkDoubleNull(rs.getString(1));
							
							}
							pstmt.close(); pstmt = null;
							rs.close(); rs = null;
							
							totQty = totQty + quantity;
							
							qtyBrow=0;
							parentNodeListTemp = dom2.getElementsByTagName("Detail2");
							parentNodeListLength = parentNodeListTemp.getLength();
							System.out.println("ParentNodeListLength ..........."+parentNodeListLength);
							for (int selectedRow = 0; selectedRow < parentNodeListLength; selectedRow++)
							{
								parentNodeTemp = parentNodeListTemp.item(selectedRow);
								String mlineNoDOrd=checkNullAndTrim(genericUtility.getColumnValueFromNode("line_no_dist_order", parentNodeTemp));
								String mdistOrder=checkNullAndTrim(genericUtility.getColumnValueFromNode("dist_order", parentNodeTemp));
								String mlineNo=checkNullAndTrim(genericUtility.getColumnValueFromNode("line_no", parentNodeTemp));
								
								if(lineNoDOrd.equalsIgnoreCase(mlineNoDOrd) &&	
										mdistOrder.equalsIgnoreCase(distOrder) && 
										!mlineNo.equalsIgnoreCase(lineNo)   )
								{
									qtyBrow=checkDoubleNull(genericUtility.getColumnValueFromNode("quantity", parentNodeTemp));
									totQty = totQty + qtyBrow;
								}
								
							}
							
							if(totQty > (balQty + ((balQty * overShipPerc)/100)) && tranTypeParent.equalsIgnoreCase(tranType) )
							{
								errCode = "VTDIST19";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							
							itemCode = checkNullAndTrim(genericUtility.getColumnValue("item_code", dom)); 
							sql = "Select Count(1) from  item where  item_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt(1);
								
							}
							pstmt.close(); pstmt = null;
							rs.close(); rs = null;
						
							if (cnt > 0)
							{
								sql = "Select case when Check_integral_qty is null then 'N' else Check_integral_qty end From distorder_type Where tran_type = ?  ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, tranType);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									checkIntegralQty = rs.getString(1);
									
								}
								pstmt.close(); pstmt = null;
								rs.close(); rs = null;
							}
							if(!"N".equalsIgnoreCase(checkIntegralQty))
							{
								integralQty = getIntegralQty( siteCode, itemCode, lotNo, packCode, checkIntegralQty,conn);
								if( integralQty > 0 )
								{
									sql = "Select mod(?,?) as mmOdQty from dual";
									pstmt = conn.prepareStatement(sql);
									pstmt.setDouble(1, quantity);
									pstmt.setDouble(2, integralQty);
									rs = pstmt.executeQuery();
									if (rs.next()) {
										modQty = rs.getDouble("mmOdQty");
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									
									if( modQty > 0 )
									{
										errCode = "VTINTQTY";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									
								}//end if
							}
							if (errCode == null || errCode.trim().length() == 0)
							{
								if(!tranTypeParent.equalsIgnoreCase(tranType))
								{
									sql = "select sum(quantity) from distord_issdet where dist_order =  ? and line_no_dist_order = ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, distOrder);
									pstmt.setString(2, lineNoDOrd);
									rs = pstmt.executeQuery();
									if (rs.next())
									{

										totQty = rs.getDouble(1);
									}
									pstmt.close(); pstmt = null;
									rs.close(); rs = null;
									
									if(quantity > totQty)
									{
										errCode = "VTIQTY";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
							if (errCode == null || errCode.trim().length() == 0)
							{
								lotSl = checkNullAndTrim(genericUtility.getColumnValue("lot_sl", dom)); 
								locCode = checkNullAndTrim(genericUtility.getColumnValue("loc_code", dom)); 
								availableYN = checkNullAndTrim(genericUtility.getColumnValue("available_yn", dom1)); 
								sql = "select stk_opt from item where item_code =  ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, itemCode);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									stkOpt = checkNullAndTrim(rs.getString("stk_opt"));
									
									
								}
								pstmt.close(); pstmt = null;
								rs.close(); rs = null;
								if(!"0".equalsIgnoreCase(stkOpt))
								{
									//Modified by Sana S on 29/05/20 [start][Changes done to resolve issue reported by Gaurav in CAS as stock is maintained item wise]
									//if(stkOpt == "1")
									System.out.println("stkOpt is Value:::"+stkOpt);
									if("1".equals(stkOpt))// modified by Sana S on 29/05/20
									{
										System.out.println("Condition is true");
										lotNo = " ";
										lotSl = " ";
										
									}
									System.out.println("lotNo is ::"+lotNo);
									System.out.println("lotSl is ::"+lotSl);
									//Modified by Sana S on 29/05/20 [end][Changes done to resolve issue reported by Gaurav in CAS as stock is maintained item wise]
									if("Y".equalsIgnoreCase(availableYN))
									{
										sql = "select a.quantity - a.alloc_qty from stock a, invstat b, location c "
												+ " where a.loc_code =c.loc_code "
												+ " and b.inv_stat = c.inv_stat "
												+ " and a.item_code = ?	"
												+ " and a.site_code = ?"
												+ " and a.loc_code = ? "
												+ " and a.lot_no = ? "
												+ " and a.lot_sl = ? "
												+ " and b.available = ? "
												+ " and not exists (select 1 from inv_restr i where i.inv_stat = b.inv_stat and i.ref_ser = 'D-ISS') ";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, itemCode);
										pstmt.setString(2, siteCode);
										pstmt.setString(3, locCode);
										pstmt.setString(4, lotNo);
										pstmt.setString(5, lotSl);
										pstmt.setString(6, availableYN);
										rs = pstmt.executeQuery();
										if (rs.next())
										{
											stkQty = rs.getDouble(1);
											
										}
										pstmt.close(); pstmt = null;
										rs.close(); rs = null;
									}
									else
									{
										sql = "select a.quantity - a.alloc_qty from stock a, invstat b, location c "
												+ " where a.loc_code =c.loc_code "
												+ " and b.inv_stat = c.inv_stat "
												+ " and a.item_code = ?	"
												+ " and a.site_code = ?"
												+ " and a.loc_code = ? "
												+ " and a.lot_no = ? "
												+ " and a.lot_sl = ? "
												+ " and b.available = ? ";
											
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, itemCode);
										pstmt.setString(2, siteCode);
										pstmt.setString(3, locCode);
										pstmt.setString(4, lotNo);
										pstmt.setString(5, lotSl);
										pstmt.setString(6, availableYN);
										rs = pstmt.executeQuery();
										if (rs.next())
										{
											stkQty = rs.getDouble(1);
											
										}
										pstmt.close(); pstmt = null;
										rs.close(); rs = null;
									}
									sql = " select sum(case when quantity is null then 0 else quantity end) from distord_issdet "
										+ " where dist_order = ? and line_no_dist_order = ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, distOrder);
									pstmt.setString(2, lineNoDOrd);
									rs = pstmt.executeQuery();
									if (rs.next())
									{
										oldQty = rs.getDouble(1);
										
									}
									pstmt.close(); pstmt = null;
									rs.close(); rs = null;
									
									stkQty = stkQty + oldQty;
									
									System.out.println("stkQty is ::"+stkQty);
									System.out.println("quantity is ::"+quantity);
									
									if(stkQty < quantity)
									{
										errCode = "VTSTK5";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
						}// end of if block
						else if("qty_order__alt".equalsIgnoreCase(childNodeName))
						{
							qtyOrderAlt = checkDoubleNull(genericUtility.getColumnValue("qty_order__alt", dom)); 
							quantity = checkDoubleNull(genericUtility.getColumnValue("quantity", dom)); 
							rate = checkDoubleNull(genericUtility.getColumnValue("rate", dom)); 
							rateAlt = checkDoubleNull(genericUtility.getColumnValue("rate__alt", dom)); 
							if(rateAlt != 0)
							{
								if((quantity * rate) !=(qtyOrderAlt * rateAlt))
								{
									errCode = "VTALTQTY";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
								
								
						}// end of if block
						else if("batch_size".equalsIgnoreCase(childNodeName))
						{
							batchSize = checkDoubleNull(genericUtility.getColumnValue("batch_size", dom)); 
							itemCode = checkNullAndTrim(genericUtility.getColumnValue("item_code", dom)); 
							siteCode = checkNullAndTrim(genericUtility.getColumnValue("site_code", dom1)); 
							tranDate = checkNullAndTrim(genericUtility.getColumnValue("tran_date", dom1)); 	
							if( tranDate != null && tranDate.trim().length() > 0 )
							{
								TranDate = Timestamp.valueOf(genericUtility.getValidDateString(tranDate, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");		
							}
							sql = "Select Count(*) from batchsize_aprv where item_code = ? "
									+ " and 	site_code__mfg = ?"
									+ " and 	eff_from   	  <= ? "
									+ " and 	valid_upto 	  >= ?"
									+ " and 	confirmed 	= 'Y' ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							pstmt.setString(2, siteCode);
							pstmt.setTimestamp(3, TranDate);
							pstmt.setTimestamp(4, TranDate);
							
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt(1);
								
							}
							pstmt.close(); pstmt = null;
							rs.close(); rs = null;
						
							if (cnt > 0 && batchSize == 0)
							{
								errCode = "VMBATCHBLK";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							if ( batchSize > 0)
							{
								sql = "select batch_size from batchsize_aprv where item_code = ? "
										+ " and 	site_code__mfg = ?"
										+ " and 	eff_from   	  <= ? "
										+ " and 	valid_upto 	  >= ?"
										+ " and 	confirmed 	= 'Y' ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, itemCode);
								pstmt.setString(2, siteCode);
								pstmt.setTimestamp(3, TranDate);
								pstmt.setTimestamp(4, TranDate);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									mBatchSize = rs.getDouble(1);
									
								}
								pstmt.close(); pstmt = null;
								rs.close(); rs = null;
								if ( batchSize > mBatchSize)
								{
									errCode = "VTBTHSIZE";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}// end of if block
						
					}//end for loop
					break;
				
			}// end of switch statement 			

			int errListSize = errList.size();
			cnt = 0;
			String errFldName = null;
			if ( errList != null && errListSize > 0 )
			{
				for (cnt = 0; cnt < errListSize; cnt++ )
				{
					errCode = errList.get(cnt);
					errFldName = errFields.get(cnt);
					System.out.println("errCode .........." + errCode);
					errString = itmDBAccessEJB.getErrorString("", errCode, userId, "", conn);
					errorType =  errorType( conn, errCode );
					if ( errString.length() > 0)
					{
						String bifurErrString = errString.substring( errString.indexOf("<Errors>") + 8, errString.indexOf("<trace>"));
						bifurErrString =bifurErrString+errString.substring( errString.indexOf("</trace>") + 8, errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						System.out.println("errStringXml .........."+errStringXml);
						errString = "";
					}
					if ( errorType.equalsIgnoreCase("E"))
					{
						break;
					}
				}
				errList.clear();
				errList = null;
				errFields.clear();
				errFields = null;

				errStringXml.append("</Errors></Root>\r\n");
			}
			else
			{
				errStringXml = new StringBuffer( "" );
			}	
			errString = errStringXml.toString();

		}// End of try
		catch(Exception e)
		{
			e.printStackTrace();
			errString = e.getMessage();
			throw new ITMException(e);
		}
		finally
		{
			try
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
				if(conn != null)
				{
					conn.close();
					conn = null;
				}
				
			} 
			catch(Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		errString = errStringXml.toString();
		System.out.println("testing : final errString : " + errString);
		return errString;
	}//end of validation
	private String checkNull(String input) 
	{
		if(input == null)
		{
			input = "";
		}
		return input;
	}
	private double getDoubleValue(String input) throws Exception
	{
		double value = 0.0;
		try
		{
			if(input == null)
			{
				value = 0.0;
			}
			else
			{
				if(input.trim().length() > 0)
				{	
					try 
			        { 
			            // checking valid integer using parseInt() method 
						value = Double.valueOf(checkNull(input)); 
			        }  
			        catch (NumberFormatException es)  
			        { 
			        	es.printStackTrace();
			            return 0.0; 
			        }
					
				}
			}
			System.out.println("retutn value["+value+"]");
		}
		catch(Exception e)
		{
			System.out.println("DistIssue.getDoubleValue()["+e.getMessage()+"]");
			throw e;
		}
		return value;
	}
	private StringBuffer gbfRateProtect(Document dom, Document dom1, Document dom2,
			String editFlag, String xtraParams, String objContext, Connection conn)
	{
		String sql = "";
		String distOrd = "";
		String priceList = ""; 
		String priceListClg = "";
		String listType = "";
		String unit = "";
		String itemCode = "";
		String unitAlt = "";
		String rateClgStr ="";
		//dec{4}
		double rate = 0.0; 
		double rateClg = 0.0;
		//double convRateAlt = 0.0;
		double rateAlt = 0.0;
		//dec{3} 
		double qty = 0.0;
		double amount = 0.0;
		double netAmt = 0.0;
		double qtyOrderAlt = 0.0;
		double taxAmt = 0.0;
		double disAmt = 0.0;
		double totAmt = 0.0;
		//dec{7} 
		double fact = 0.0;
		PreparedStatement pstmt = null;
		ResultSet rs= null;	
		StringBuffer retString = new StringBuffer();
		try {
			
			distOrd = checkNull(genericUtility.getColumnValue("dist_order",dom1));
			
			sql = "Select price_list, price_list__clg From distorder Where dist_order = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, distOrd);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				priceList = rs.getString("price_list");
				priceListClg = rs.getString("price_list__clg");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;			
			
			listType = distCommon.getPriceListType(priceList, conn);			
			if(priceList.trim().length() > 0 && "B".equals(listType)) 
			{
				retString.append("<rate protect = \"1\">").append("</rate>");
			}else
			{
				retString.append("<rate protect = \"0\">").append("</rate>");
			}	
			listType = distCommon.getPriceListType(priceListClg, conn);
			if(priceListClg.trim().length() > 0 && "B".equals(listType))
			{			
				retString.append("<rate__clg protect = \"1\">").append("</rate__clg>");
			}else 
			{			
				retString.append("<rate__clg protect = \"0\">").append("</rate__clg>");
			}				
			qtyOrderAlt = Double.parseDouble(genericUtility.getColumnValue("qty_order__alt",dom));
			rateAlt   	= Double.parseDouble(genericUtility.getColumnValue("rate__alt",dom));
			fact        = 0;
			taxAmt 		= Double.parseDouble(genericUtility.getColumnValue("tax_amt",dom));
			disAmt 		= Double.parseDouble(genericUtility.getColumnValue("disc_amt",dom));
			unitAlt		= checkNull(genericUtility.getColumnValue("unit__alt",dom));
			unit		= checkNull(genericUtility.getColumnValue("unit",dom));
			itemCode	= checkNull(genericUtility.getColumnValue("item_code",dom));
			qty    		= Double.parseDouble(genericUtility.getColumnValue("quantity",dom));
			rate 		= Double.parseDouble(genericUtility.getColumnValue( "rate",dom));
			
			if (!unitAlt.equals(unit))
			{
				//convRateAlt = gf_conv_qty_fact(unit, unitAlt , itemCode, qtyOrderAlt, fact)
				ArrayList alist = null;
				alist = distCommon.getConvQuantityFact(unit, unitAlt, itemCode, qtyOrderAlt, fact, conn);				
				rate = rateAlt * fact;
				if(alist.size() > 1)
				{
					retString.append("<conv__rate_alt>").append("<![CDATA[" + String.valueOf(alist.get(0)) + "]]>").append("</conv__rate_alt>");
				}else{
					retString.append("<conv__rate_alt>").append("<![CDATA[" + fact + "]]>").append("</conv__rate_alt>");
				}
				alist = null;
			} 
			else 
			{
				fact = 1;
			}
			totAmt  = qtyOrderAlt * rateAlt;
			netAmt = totAmt + taxAmt - disAmt;
			
			retString.append("<rate>").append("<![CDATA[" + rate + "]]>").append("</rate>");
			retString.append("<amount>").append("<![CDATA[" + totAmt + "]]>").append("</amount>");
			retString.append("<net_amt>").append("<![CDATA[" + netAmt + "]]>").append("</net_amt>");

				
			rateClgStr = genericUtility.getColumnValue( "rate__clg",dom);			
			if(rateClgStr == null || rateClgStr.trim().equals("0") || rateClgStr.trim().length() == 0)
			{				
				retString.append("<rate__clg>").append("<![CDATA[" + rate + "]]>").append("</rate__clg>");								
			}
			else// else condition added by nandkumar gadkari on 07/05/2020
			{
				rateClg=Double.parseDouble(rateClgStr);
				retString.append("<rate__clg>").append("<![CDATA[" + rateClg + "]]>").append("</rate__clg>");
			}
		
		} catch (ITMException e) {			
			e.printStackTrace();
		} catch (SQLException sQl) {
			sQl.printStackTrace();
		} catch (Exception e) {			
			e.printStackTrace();
		}		
		return retString;
	} 

	private static void setNodeValue(Document dom, String nodeName, String nodeVal) throws Exception {
		Node tempNode = dom.getElementsByTagName(nodeName).item(0);

		if (tempNode != null) {
			if (tempNode.getFirstChild() == null) {
				CDATASection cDataSection = dom.createCDATASection(nodeVal);
				tempNode.appendChild(cDataSection);
			} else {
				tempNode.getFirstChild().setNodeValue(nodeVal);
			}
		}
		tempNode = null;
	}
	private static String getAbsString(String str) {
		return (str == null || str.trim().length() == 0 || "null".equalsIgnoreCase(str.trim()) ? "" : str.trim());
	}
	
	private String checkDouble(String input)	
	{
		if (input == null || input.trim().length() == 0)
		{
			input="0";
		}
		return input;
	}
	//Validation --------
	private String isExist(String table, String field, String value, Connection conn) throws SQLException

	{
		String sql = "", retStr = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int cnt = 0;

		sql = " SELECT COUNT(1) FROM " + table + " WHERE " + field + " = ? ";
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, value);
		rs = pstmt.executeQuery();
		if (rs.next())
		{
			cnt = rs.getInt(1);
		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;
		if (cnt > 0)
		{
			retStr = "TRUE";
		}
		if (cnt == 0)
		{
			retStr = "FALSE";
		}
		System.out.println("@@@@ isexist[" + value + "]:::[" + retStr + "]:::[" + cnt + "]");
		return retStr;
	}
	private double checkDoubleNull(String str) {
		if (str == null || str.trim().length() == 0) {
			return 0.0;
		} else {
			return Double.parseDouble(str);
		}
	}
	private int checkIntNull(String str) {
		if (str == null || str.trim().length() == 0) {
			return 0;
		} else {
			return Integer.parseInt(str);
		}
	}
	
	
	private String errorType(Connection conn , String errorCode)
	{
		String msgType = "";
		PreparedStatement pstmt = null ; 
		ResultSet rs = null;
		try
		{			
			String  sql = "SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ?";
			pstmt = conn.prepareStatement(sql);			
			pstmt.setString(1,errorCode);			
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				msgType = rs.getString("MSG_TYPE");
			}	
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}		
		finally
		{
			try
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
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}		
		return msgType;
	}
	public String checkNullAndTrim( String inputVal )
	{
		if ( inputVal == null )
		{
			inputVal = "";
		}
		else
		{
			inputVal = inputVal.trim();
		}
		return inputVal;
	}

	private boolean isExist(Connection conn, String tableName, String columnName, String value)
			throws ITMException, RemoteException {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		boolean status = false;
		try {
			sql = "SELECT count(*) from " + tableName + " where " + columnName + "  = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, value);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				if (rs.getBoolean(1)) {
					status = true;
				}
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		} catch (Exception e) {
			System.out.println("Exception in isExist ");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning String from isExist ");
		return status;
	}
	private String gbfDistOrderSite( String asDistOrder, String asSiteShip, String asSiteDlv, Connection conn ) throws Exception
	{
		String lsErrcode = null, lsSiteCodeShip = null, lsSiteCodeDlv = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		sql = "select site_code__ship ls_site_code__ship, site_code__dlv ls_site_code__dlv from distorder "
			+" where dist_order = '" + asDistOrder + "'";

		pstmt = conn.prepareStatement( sql );
		rs = pstmt.executeQuery();
		if( rs.next() )
		{
			lsSiteCodeShip = rs.getString( "ls_site_code__ship" );
			lsSiteCodeDlv = rs.getString( "ls_site_code__dlv" );
		}

		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;

		if( !( lsSiteCodeShip.trim().equalsIgnoreCase( asSiteShip.trim() ) ) || !( lsSiteCodeDlv.trim().equalsIgnoreCase( asSiteDlv.trim() ) ) )
		{
		  lsErrcode = "VTDIST10";
		}
		return lsErrcode;
	}
	private String calcExpiry(String tranDate, int months)
	{

		java.util.Date expDate = new java.util.Date();
		java.util.Date retDate = new java.util.Date();
		String retStrInDate = "";
		System.out.println("tranDate :"+tranDate+"\nmonths :"+months);
		try
		{
			//GenericUtility genericUtility = GenericUtility.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			if (months > 0)
			{
				Calendar  cal = Calendar.getInstance();
				expDate = sdf.parse(tranDate);
				System.out.println("expDate :"+expDate);
				cal.setTime(expDate);
				cal.add(Calendar.MONTH,months);
				
				cal.add(Calendar.MONTH,1);
				cal.set(Calendar.DATE,0);
				
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
	private double getIntegralQty(String siteCode, String itemCode, String lotNo, String packCode, String checkIntegralQty, Connection conn)
	{
		double integralQty = 0;
		String sql = "";
		ResultSet rs = null;
		//Connection conn = null;
		//ConnDriver connDriver = new ConnDriver();
		PreparedStatement pstmt = null;
		
		try
		{
			//conn = connDriver.getConnectDB("DriverITM");
	
			
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
					pstmt = conn.prepareStatement( sql );
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						integralQty = rs.getDouble(1);
						//System.out.println("integralQty :"+integralQty);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if (integralQty == 0)
					{
						sql = "SELECT CASE WHEN CAPACITY IS NULL THEN 0 ELSE CAPACITY END "
							 +"FROM PACKING WHERE PACK_CODE = '"+packCode+"'";
						System.out.println("sql :"+sql);
						pstmt = conn.prepareStatement( sql );
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							integralQty = rs.getDouble(1);
							//System.out.println("integralQty :"+integralQty);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (integralQty == 0)
						{
							sql = "SELECT REO_QTY FROM SITEITEM "
								 +"WHERE SITE_CODE = '"+siteCode+"'"+" AND ITEM_CODE = '"+itemCode+"'";
							System.out.println("sql :"+sql);	
							pstmt = conn.prepareStatement( sql );
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								integralQty = rs.getDouble(1);
								//System.out.println("integralQty :"+integralQty);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if (integralQty == 0)
							{
								sql = "SELECT REO_QTY FROM ITEM "
									 +"WHERE ITEM_CODE = '"+itemCode+"'";
								System.out.println("sql :"+sql);	
								pstmt = conn.prepareStatement( sql );
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									integralQty = rs.getDouble(1);
									//System.out.println("integralQty :"+integralQty);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}
					}
					
				break;
				case 'P':
					sql = "SELECT CASE WHEN CAPACITY IS NULL THEN 0 ELSE CAPACITY END "
							 +"FROM PACKING WHERE PACK_CODE = '"+packCode+"'";
					System.out.println("sql :"+sql);
					pstmt = conn.prepareStatement( sql );
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						integralQty = rs.getDouble(1);
						//System.out.println("integralQty :"+integralQty);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				break;
				case 'I':
					sql = "SELECT INTEGRAL_QTY FROM SITEITEM "
								 +"WHERE SITE_CODE = '"+siteCode+"'"+" AND ITEM_CODE = '"+itemCode+"'";
					System.out.println("sql :"+sql);	
					pstmt = conn.prepareStatement( sql );
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						integralQty = rs.getDouble(1);
						//System.out.println("integralQty :"+integralQty);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if (integralQty == 0)
					{
						sql = "SELECT INTEGRAL_QTY FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
						System.out.println("sql :"+sql);
						pstmt = conn.prepareStatement( sql );
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							integralQty = rs.getDouble(1);
							//System.out.println("integralQty :"+integralQty);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					
			}
			
			//conn.close();	
		}
		catch(Exception e)
		{
			System.out.println("the exception occurs in getIntegralQty :"+e);
		}
		System.out.println("integralQty :"+integralQty);
		return integralQty;
	}
}
