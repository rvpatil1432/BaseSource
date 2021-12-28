package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.sys.UtilMethods;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

@Stateless

public class PriceListGenIC extends ValidatorEJB implements PriceListGenLocal, PriceListGenRemote 
 {
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	FinCommon finCommon = null;
	ValidatorEJB validator = null;
	Date effFrom = null;
	UtilMethods utilMethods = UtilMethods.getInstance();
	String dateStr = "";
	String errFldName = "";
	String errorType ="";
	
	@Override
	public String wfValData() throws RemoteException,ITMException
	{
		return "";
	}

	@Override
	public String itemChanged() throws RemoteException,ITMException
	{
		return "";
	}
	@Override
	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag,	String xtraParams) throws RemoteException, ITMException 
	{
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			System.out.println("Val xmlString :: " + xmlString);
			System.out.println("Val xmlString1 :: " + xmlString1);
			System.out.println("Val xmlString2 :: " + xmlString2);

			if (xmlString != null && xmlString.trim().length() > 0) {
				dom = parseString(xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0) {
				dom1 = parseString(xmlString1);
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0) {
				dom2 = parseString(xmlString2);
			}
			errString = wfValData(dom, dom1, dom2, objContext, editFlag,
					xtraParams);
		} catch (Exception e) {
			throw new ITMException(e);
		}
		return (errString);
	}
	@Override
	public String wfValData(Document dom, Document dom1, Document dom2,	String objContext, String editFlag, String xtraParams)
			throws RemoteException, ITMException 
			{
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		String errString = "";
		String errCode = "";
		String errorType = "";
		String userId = "";
		String sql = "";
		String itemCode = "";
		String refNo = "";
		String keyFlag = "";
		String manageType = "";
		String priceListParent = "";
		String priceList = "";
		String priceListTar = "";
		String calcMethod = "";
		String tranId = "";
		String sql1 = "";
		String active = "";
		Date validUpto = null;
		String validUptoStr = "";
		String effFromStr = "";
		String minRateStr = "";
		String maxRateStr = "";
		double maxRate=0;
		String rateStr = "";
		double rate = 0;
		double minRate = 0;
		String unit ="";
		int ctr = 0;
		int count = 0;
		int currentFormNo = 0;
		double minQty = 0;
		String minQtyStr = "";
		String maxQtyStr = "";
		double maxQty = 0;
		int childNodeListLength;
		StringBuffer valueXmlString = new StringBuffer();
		ArrayList<String> errList = new ArrayList();
		ArrayList<String> errFields = new ArrayList<String>();
		
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ConnDriver connDriver = new ConnDriver();
		StringBuffer errStringXml = new StringBuffer(
				"<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
		try {
			
			
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
					genericUtility.getApplDateFormat());
			SimpleDateFormat dbDateFormat = new SimpleDateFormat(genericUtility.getDBDateFormat());
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			if (objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			valueXmlString = new StringBuffer(
					"<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");
			switch (currentFormNo) {
			case 1:
				
				System.out.println("testing case 1 for validation ");
				parentNodeList = dom.getElementsByTagName("Detail1");
				valueXmlString.append("<Detail1>");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				
				childNodeListLength = childNodeList.getLength();
				for (ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					System.out.println("pricelist>>>><<<<");
					
					if (childNodeName.equalsIgnoreCase("price_list"))
					{
						priceList = genericUtility.getColumnValue("price_list",dom);
						if(priceList == null )
						{
							errCode ="VMPRLNULL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							break;
							
						}
						
						if (priceList != null && priceList.trim().length() > 0)
						{
							sql1 = "select count(*)  from pricelist_mst where price_list = ?";
							pstmt = conn.prepareStatement(sql1);
							pstmt.setString(1, priceList);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								count = rs.getInt(1);
								System.out.println("Count is " + count);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if (count == 0) {
								errCode = "VTPLIST";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
								
							}
							
							else
							{
						sql = "Select manage_type  from pricelist_mst where  price_list = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, priceList);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							manageType = checkNull(rs.getString(1));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (errCode == null || errCode.trim().length() == 0) 
						{
							if (manageType.equalsIgnoreCase("M"))
							{
								errCode = "VTMTYPE";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
							
						}
							}

					}
					}
					else if(childNodeName.equalsIgnoreCase("tran_date"))
					{
						dateStr =genericUtility.getColumnValue("tran_date", dom);
						if (dateStr == null) {
							errCode = "VTTRNDTNUL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							break;
						}
					}
					
					else if (childNodeName.equalsIgnoreCase("tran_id"))
					{
						tranId = genericUtility.getColumnValue("tran_id", dom);
						sql = "select key_flag  from transetup where tran_window = 'w_pricelist_tran' ";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							keyFlag = rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (keyFlag == null) {
							keyFlag = "M";
							tranId = genericUtility.getColumnValue("tran_id",
									dom);

						}
						if (keyFlag.equalsIgnoreCase("M")
								&& (tranId == null || tranId.trim().length() == 0)) {
							errCode = "VMCODNULL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							break;
						}
						

						else if (editFlag.equalsIgnoreCase("A"))
						{
							sql = "select count(*)  from pricelist_hdr where tran_id =  ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, tranId);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								count = rs.getInt(1);
							}
							if (count > 0) {
								errCode = "VMDUPL1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}

					}
					else if (childNodeName.equalsIgnoreCase("ref_no")
							|| childNodeName.equalsIgnoreCase("ref_no_old")) {
						refNo = genericUtility.getColumnValue(childNodeName,
								dom);
						if (refNo == null) {
							errCode = "VTREFNONUL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							break;
						}

					}
					
				}
				valueXmlString.append("</Detail1>");
				
				break;
			case 2:
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail2>");
				ctr =0;
				childNodeListLength = childNodeList.getLength();
				for (ctr = 0; ctr < childNodeListLength; ctr++) {
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					System.out.println("testing case 2 for validation ");
					if (childNodeName.equalsIgnoreCase("item_code")) {

						itemCode = genericUtility.getColumnValue("item_code",
								dom);
						if (itemCode == null || itemCode.trim().length() == 0) {
							errCode = "VTITMNL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							break;

						}
						sql = "select count(*) from   item where  item_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							count = rs.getInt(1);
						}
						if (count == 0) {
							errCode = "VMITEM_CD";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							break;
						} else {
							sql1 = "select active  from  item where item_code = ?";
							pstmt = conn.prepareStatement(sql1);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								active = rs.getString(1);
							}
							if (active.equalsIgnoreCase("N")) {
								errCode = "VTITEM4";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}

						}
					}
					else if (childNodeName.equalsIgnoreCase("unit")) {
						count =0;
						unit = genericUtility.getColumnValue("unit",
								dom);
						sql = "select count(*) from uom where unit = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,unit);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							count = rs.getInt(1);
						}
						if (count == 0) {
							errCode = "VTINVUNT02";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							break;
						}
					}
					/*else if (childNodeName.equalsIgnoreCase("min_qty")) {
						minQtyStr = genericUtility.getColumnValue("min_qty",
								dom);
						if(minQtyStr == null || minQtyStr.trim().length() == 0 )
						{
							errCode = "NULLQTY";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							break;
	
						}
	//					minQty = minQtyStr == null ? 0 : minQtyStr.trim().length() == 0 ? 0 : Integer.parseInt(minQtyStr.trim());  
						
					}*/
					else if (childNodeName.equalsIgnoreCase("max_qty")||(childNodeName.equalsIgnoreCase("min_qty")))
					{
						maxQtyStr = genericUtility.getColumnValue("max_qty",dom);
						minQtyStr = genericUtility.getColumnValue("min_qty",
								dom);
						if(maxQtyStr == null || minQtyStr.trim().length() == 0 || minQtyStr == null || minQtyStr.trim().length() == 0 )
						{
							errCode = "NULLQTY";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							break;
	
						}
	
						
						//maxQty 	= maxQtyStr == null ? 0 : maxQtyStr.trim().length() == 0 ? 0 : Integer.parseInt(maxQtyStr.trim());
						
						if((minQtyStr.trim().length() > 0||minQtyStr !=null )&&  (maxQtyStr.trim().length() >0 ||maxQtyStr != null ))
						{
							minQty = Double.parseDouble(minQtyStr.trim());
							maxQty = Double.parseDouble(maxQtyStr.trim());
						if (maxQty < minQty) {
							errCode = "VMMINQTY";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							break;
						}
						}
					}
					else if ((childNodeName.equalsIgnoreCase("min_rate"))||(childNodeName.equalsIgnoreCase("rate"))||(childNodeName.equalsIgnoreCase("max_rate"))) {
						minRateStr = genericUtility.getColumnValue("min_rate",
								dom);
						maxRateStr = genericUtility.getColumnValue("max_rate",
								dom);
						rateStr = genericUtility.getColumnValue("rate", dom);
						if((minRateStr.trim().length() > 0||minRateStr !=null )&&  (rateStr.trim().length() >0 ||rateStr != null )&&(maxRateStr.trim().length() > 0||maxRateStr !=null ))
						{
						minRate = Double.parseDouble(minRateStr);
						rate = Double.parseDouble(rateStr);
						maxRate = Double.parseDouble(maxRateStr);
						if(rate<0 || minRate<0 )
						{
							errCode = "VTSUBCRATE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							break;
						}
						if (rate < minRate) {
							errCode = "VMMINRATE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							break;
						}
						if (maxRate < minRate) {
							errCode = "VTRATE7";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							break;
						}
						}
					}
					else if (childNodeName.equalsIgnoreCase("valid_upto")) {
						System.out.println("validupto chidnode name" + childNodeName);
						validUptoStr = checkNull(genericUtility.getColumnValue(
								"valid_upto", dom));
						
						effFromStr = checkNull(genericUtility.getColumnValue("eff_from",
								dom));
						System.out.println("Valid upto" + validUptoStr);
						if((validUptoStr.trim().length() > 0 && validUptoStr!=null) && (effFromStr.trim().length()> 0 &&effFromStr!=null ))
						{
							System.out.println("Inside validupto condition");
						//stmtMaster.setTimestamp( 1, java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString( ldtTranDateFrom, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
						SimpleDateFormat sdf1=new SimpleDateFormat("dd/MM/yy");
						effFrom=sdf1.parse(effFromStr);
						
						validUpto=sdf1.parse(validUptoStr);
						if (validUpto.before(effFrom)) {
							errCode = "VTVALUPTOE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							break;
						}
						}

					}
					
				}
				valueXmlString.append("</Detail2>");
				break;

			}
			int errListSize = errList.size();
			int cnt = 0;
			if(errList != null && errListSize > 0)
			{
				for(cnt = 0; cnt < errListSize; cnt ++)
				{
					errCode = errList.get((int) cnt);
					System.out.println("errCode .........." + errCode);
					errString = getErrorString(errFldName, errCode, userId);
					errorType = errorType(conn , errCode);
					if(errString.length() > 0)
					{
						String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8, errString.indexOf("<trace>"));
						bifurErrString = bifurErrString + errString.substring(errString.indexOf("</trace>") + 8, errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						errString = "";
					}
					if(errorType.equalsIgnoreCase("E"))
					{
						break;
					}
				}
				errList.clear();
				errList = null;
				errFields.clear();
				errFields = null;
				errStringXml.append("</Errors> </Root> \r\n");
			}
			else
			{
				errStringXml = new StringBuffer("");
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println("Exception : " + e);
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(conn!=null)
				{
					if(rs != null )rs.close();
					
					rs = null;
					
					if(pstmt != null )pstmt.close();
					pstmt =null;
					conn.close();
				}
				conn = null;
			}catch(Exception d)
				{
				  d.printStackTrace();
				  throw new ITMException( d );
				}
			//System.out.println("[SOrderFormEJB] Connection is Closed");
		}
		errString = errStringXml.toString();
		return errString;
	}
	
	
	public String itemChanged(String xmlString, String xmlString1,
			String xmlString2, String objContext, String currentColumn,
			String editFlag, String xtraParams) throws RemoteException,
			ITMException {
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		System.out.println("xmlString:>>>>>>>>>>>>>>>>>>>>>>>>>>    " +xmlString);
		System.out.println("xmlString1:>>>>>>>>>>>>>>>>>>>>>>>>>>    " +xmlString1);
		System.out.println("xmlString2:>>>>>>>>>>>>>>>>>>>>>>>>>>    " +xmlString2);
		System.out.println("objContext:>>>>>>>>>>>>>>>>>>>>>>>>>>    " +objContext);
		System.out.println("currentColumn:>>>>>>>>>>>>>>>>>>>>>>>>>>    " +currentColumn);
		System.out.println("editFlag:>>>>>>>>>>>>>>>>>>>>>>>>>>    " +editFlag);
		System.out.println("xtraParams:>>>>>>>>>>>>>>>>>>>>>>>>>>    " +xtraParams);
		
		try
		{
			if(xmlString != null && xmlString.trim().length() > 0)
			{
				dom = parseString(xmlString);
			}
			System.out.println("dom : " + genericUtility.serializeDom(dom));
			if(xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom1 = parseString(xmlString1);
			}
			System.out.println("dom1" +genericUtility.serializeDom(dom1));
			
			if(xmlString2 != null && xmlString2.trim().length() > 0)
			{
				dom2 = parseString(xmlString2);
			}
			System.out.println("dom2" + genericUtility.serializeDom(dom2));
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
		} catch (Exception e) 
		{
			System.out.println("Exception : [PriceListGen][itemChanged( String, String )] :==>\n"+ e.getMessage());
		}
		return valueXmlString;

	}
	
	public String itemChanged(Document dom, Document dom1, Document dom2,
			String objContext, String currentColumn, String editFlag,
			String xtraParams) throws RemoteException, ITMException
			{
		StringBuffer valueXmlString = new StringBuffer();
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		Node parentNode1 = null;
		String descr = "";
		String itemCode = "";
		String childNodeName = null;
		String currCode = "";
		String defCrTerm = "";
		String calcMethod = "";
		String calcMethodDescr = "";
		String rateStr = "";
		String rateNo = "";
		double rate = 0;
		String columnValue = "";
		String priceList = "";
		String unit = "";
		String expr = "";
		String formula = "";
		String sql = "";
		int ctr = 0;
		String errString = "";
		String plist1 = "", plist2 = "", plist3 = "", plist4 = "", plist5 = "", plist6 = "", plist7 = "", plist8 = "", plist9 = "", plist10 = "", plist11 = "", plist12 = "";
		int currentFormNo = 0;
		double lineNo = 0;
		String liNoStr = "";
		String sql1 = "";
		String sql2 = "";
		String pdescr ="";
		String effFromStr = "";
		Connection conn = null;
		PreparedStatement pstmt = null, pstmt1 = null, pstmt2 = null;
		ResultSet rs = null, rs1 = null, rs2 = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		ConnDriver connDriver = new ConnDriver();
		int liNo = 0;
		try {
			System.out.println("dom : " + genericUtility.serializeDom(dom));
			System.out.println("dom1" +genericUtility.serializeDom(dom1));
			System.out.println("dom2" + genericUtility.serializeDom(dom2));
			System.out.println("objContext" + objContext);
			System.out.println("currentColumn"+currentColumn);
			System.out.println("editFlag" +editFlag);
			System.out.println("xtraParams" + xtraParams);
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
			System.out.println("editFlag@@ : ["+editFlag+"]");
			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
					genericUtility.getApplDateFormat());
			
			String currDate = simpleDateFormat.format(currentDate.getTime());
			if (objContext != null && objContext.trim().length() > 0) {
				currentFormNo = Integer.parseInt(objContext);
			}
			valueXmlString = new StringBuffer(
					"<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");

			switch (currentFormNo) 
			{
			case 1:
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0; 
				valueXmlString.append("<Detail1>");
				int childNodeListLength = childNodeList.getLength();
				do {
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equals(currentColumn))
					{
						if (childNode.getFirstChild() != null) 
						{
							columnValue = childNode.getFirstChild().getNodeValue().trim();
						}
					}
					ctr++;
				}
				while (ctr < childNodeListLength
						&& !childNodeName.equals(currentColumn));
				
				System.out.println("currentColumn[" + currentColumn + "]columnValue ==> '" + columnValue + "'");
				System.out.println("testing case 1 for item change");
				System.out.println("currentColumn"+currentColumn);
				if (currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					java.sql.Timestamp date1 =null ;
					date1 = new java.sql.Timestamp(System.currentTimeMillis()) ;
					dateStr = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(date1);
					System.out.println("inside item 1234566775578457878 ");
					valueXmlString.append("<tran_date>")
							.append("<![CDATA[" + dateStr + "]]>")
							.append("</tran_date>");
					System.out.println("CHILDNODENAME14153453412341524545454524534" + childNodeName);
					
				}
				else if (currentColumn.trim().equalsIgnoreCase("itm_defaultedit"))
				{
					Timestamp date2 = new java.sql.Timestamp(System.currentTimeMillis()) ;
					dateStr = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(date2);
					System.out.println("inside item 1234566775578457878 ");
					valueXmlString.append("<tran_date>")
							.append("<![CDATA[" + dateStr + "]]>")
							.append("</tran_date>");
					System.out.println("@@@@@@@@ itm_defaultedit called @@@@@@@@");
					System.out.println("Childnodename itmdefaultedit" + childNodeName );
					
					
				}
				/*if (currentColumn.trim().equalsIgnoreCase("price_list"))
				{
					System.out.println("currentColumn " + currentColumn );
					priceList = genericUtility.getColumnValue("price_list",
							dom);
					sql = "Select descr from pricelist_mst where  price_list = ?";
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					pstmt.setString(1, pdescr);

					if (rs.next())
					{
						priceList = rs.getString(1) == null ? "" : rs
								.getString(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<descr>")
							.append("<![CDATA[" + pdescr + "]]>")
							.append("</descr>");
				}*/
				else if (currentColumn.trim().equalsIgnoreCase("price_list"))
				{
					System.out.println("currentColumn1123512561564567346725672562788ASAAA" + currentColumn);
					priceList = genericUtility.getColumnValue("price_list",dom);
					sql = "Select descr from pricelist_mst where  price_list =?";
					System.out.println("PRICELIST" + priceList);
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,priceList );
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						pdescr = rs.getString(1) == null ? "" : rs
								.getString(1);
						
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println("PRICELIST DESCR" +pdescr );
					valueXmlString.append("<descr>")
							.append("<![CDATA[" + pdescr + "]]>")
							.append("</descr>");
				}
				
				valueXmlString.append("</Detail1>");
				break;
			case 2:
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0;
				
				valueXmlString.append("<Detail2>");
				childNodeListLength = childNodeList.getLength();

				do {
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equals(currentColumn.trim())) {
						if (childNode.getFirstChild() != null) {
							columnValue = childNode.getFirstChild()
									.getNodeValue();
						}
					}
					ctr++;
				} while (ctr < childNodeListLength
						&& !childNodeName.equals(currentColumn));
				// System.out.println("currentColumn..." + currentColumn);
				if (currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					priceList = genericUtility.getColumnValue("price_list", dom);
					sql = "Select plist_1,plist_2,plist_3,plist_4,plist_5,plist_6,plist_7,plist_8,plist_9,plist_10,plist_11,plist_12 from pricelist_mst where price_list = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, priceList);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						plist1 = rs.getString("plist_1");
						plist2 = rs.getString("plist_2");
						plist3 = rs.getString("plist_3");
						plist4 = rs.getString("plist_4");
						plist5 = rs.getString("plist_5");
						plist6 = rs.getString("plist_6");
						plist7 = rs.getString("plist_7");
						plist8 = rs.getString("plist_8");
						plist9 = rs.getString("plist_9");
						plist10 = rs.getString("plist_10");
						plist11 = rs.getString("plist_11");
						plist12 = rs.getString("plist_12");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<rate_1_t>")
							.append("<![CDATA[" + plist1 + "]]>")
							.append("</rate_1_t>");
					valueXmlString.append("<rate_2_t>")
							.append("<![CDATA[" + plist2 + "]]>")
							.append("</rate_2_t>");
					valueXmlString.append("<rate_3_t>")
							.append("<![CDATA[" + plist3 + "]]>")
							.append("</rate_3_t>");
					valueXmlString.append("<rate_4_t>")
							.append("<![CDATA[" + plist4 + "]]>")
							.append("</rate_4_t>");
					valueXmlString.append("<rate_5_t>")
							.append("<![CDATA[" + plist5 + "]]>")
							.append("</rate_5_t>");
					valueXmlString.append("<rate_6_t>")
							.append("<![CDATA[" + plist6 + "]]>")
							.append("</rate_6_t>");
					valueXmlString.append("<rate_7_t>")
							.append("<![CDATA[" + plist7 + "]]>")
							.append("</rate_7_t>");
					valueXmlString.append("<rate_8_t>")
							.append("<![CDATA[" + plist8 + "]]>")
							.append("</rate_8_t>");
					valueXmlString.append("<rate_9_t>")
							.append("<![CDATA[" + plist9 + "]]>")
							.append("</rate_9_t>");
					valueXmlString.append("<rate_10_t>")
							.append("<![CDATA[" + plist10 + "]]>")
							.append("</rate_10_t>");
					valueXmlString.append("<rate_11_t>")
							.append("<![CDATA[" + plist11 + "]]>")
							.append("</rate_11_t>");
					valueXmlString.append("<rate_12_t>")
							.append("<![CDATA[" + plist12 + "]]>")
							.append("</rate_12_t>");
					effFrom = new java.sql.Timestamp(System.currentTimeMillis()) ;
					effFrom = utilMethods.RelativeDate(effFrom, 1);
					effFromStr = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(effFrom);
					
					valueXmlString.append("<eff_from>")
							.append("<![CDATA[" + effFromStr + "]]>")
							.append("</eff_from>");
					
					// effFrom = new
					// SimpleDateFormat(genericUtility.getApplDateFormat()).format(effFrom);
					valueXmlString.append("<list_type protect = \"0\">")
							.append("<![CDATA[" + 0 + "]]>")
							.append("</list_type>");
					valueXmlString.append("<order_type protect = \"0\">")
							.append("<![CDATA[" + 0 + "]]>")
							.append("</order_type>");
				}
				else if (currentColumn.trim().equalsIgnoreCase("itm_default_edit")) 
				{
					/*if (currentColumn.equalsIgnoreCase("item_code")) 
					{
						itemCode = genericUtility.getColumnValue("item_code",dom);
						sql = "Select descr, unit from item where item_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							descr = checkNull(rs.getString(1));
							unit = checkNull(rs.getString(2));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						/*java.sql.Timestamp date1 =null ;
						date1 = new java.sql.Timestamp(System.currentTimeMillis()) ;
						effFromStr = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(date1);
						System.out.println("inside item 1234566775578457878 ");
						
						
						effFrom = new java.sql.Timestamp(System.currentTimeMillis()) ;
						
						effFromStr = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(effFrom);
						
						valueXmlString.append("<eff_from>")
								.append("<![CDATA[" + effFromStr + "]]>")
								.append("</eff_from>");
						valueXmlString.append("<descr>")
						.append("<![CDATA[" + descr + "]]>")
						.append("</descr>");
						valueXmlString.append("<unit>")
						.append("<![CDATA[" + unit + "]]>")
						.append("</unit>");
						
					}*/
					priceList = genericUtility.getColumnValue("price_list", dom);
					sql = "Select plist_1,plist_2,plist_3,plist_4,plist_5,plist_6,plist_7,plist_8,plist_9,plist_10,plist_11,plist_12 from pricelist_mst where price_list = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, priceList);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						plist1 = rs.getString("plist_1");
						plist2 = rs.getString("plist_2");
						plist3 = rs.getString("plist_3");
						plist4 = rs.getString("plist_4");
						plist5 = rs.getString("plist_5");
						plist6 = rs.getString("plist_6");
						plist7 = rs.getString("plist_7");
						plist8 = rs.getString("plist_8");
						plist9 = rs.getString("plist_9");
						plist10 = rs.getString("plist_10");
						plist11 = rs.getString("plist_11");
						plist12 = rs.getString("plist_12");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<rate_1_t>")
							.append("<![CDATA[" + plist1 + "]]>")
							.append("</rate_1_t>");
					valueXmlString.append("<rate_2_t>")
							.append("<![CDATA[" + plist2 + "]]>")
							.append("</rate_2_t>");
					valueXmlString.append("<rate_3_t>")
							.append("<![CDATA[" + plist3 + "]]>")
							.append("</rate_3_t>");
					valueXmlString.append("<rate_4_t>")
							.append("<![CDATA[" + plist4 + "]]>")
							.append("</rate_4_t>");
					valueXmlString.append("<rate_5_t>")
							.append("<![CDATA[" + plist5 + "]]>")
							.append("</rate_5_t>");
					valueXmlString.append("<rate_6_t>")
							.append("<![CDATA[" + plist6 + "]]>")
							.append("</rate_6_t>");
					valueXmlString.append("<rate_7_t>")
							.append("<![CDATA[" + plist7 + "]]>")
							.append("</rate_7_t>");
					valueXmlString.append("<rate_8_t>")
							.append("<![CDATA[" + plist8 + "]]>")
							.append("</rate_8_t>");
					valueXmlString.append("<rate_9_t>")
							.append("<![CDATA[" + plist9 + "]]>")
							.append("</rate_9_t>");
					valueXmlString.append("<rate_10_t>")
							.append("<![CDATA[" + plist10 + "]]>")
							.append("</rate_10_t>");
					valueXmlString.append("<rate_11_t>")
							.append("<![CDATA[" + plist11 + "]]>")
							.append("</rate_11_t>");
					valueXmlString.append("<rate_12_t>")
							.append("<![CDATA[" + plist12 + "]]>")
							.append("</rate_12_t>");
					
					effFrom = new java.sql.Timestamp(System.currentTimeMillis()) ;
					effFrom = utilMethods.RelativeDate(effFrom, 1);
					effFromStr = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(effFrom);
					
					valueXmlString.append("<eff_from>")
							.append("<![CDATA[" + effFromStr + "]]>")
							.append("</eff_from>");

					// effFrom = new
					// SimpleDateFormat(genericUtility.getApplDateFormat()).format(effFrom);
					valueXmlString.append("<list_type protect = \"0\">")
							.append("<![CDATA[" + 0 + "]]>")
							.append("</list_type>");
					valueXmlString.append("<order_type protect = \"0\">")
							.append("<![CDATA[" + 0 + "]]>")
							.append("</order_type>");
				
					

					HashMap<String, Integer> hashMap = new HashMap<String, Integer>();
					hashMap.put("rate", 1);
					hashMap.put("rate_1", 2);
					hashMap.put("rate_2", 3);
					hashMap.put("rate_3", 4);
					hashMap.put("rate_4", 5);
					hashMap.put("rate_5", 6);
					hashMap.put("rate_6", 7);
					hashMap.put("rate_7", 8);
					hashMap.put("rate_8", 9);
					hashMap.put("rate_9", 10);
					hashMap.put("rate_10", 11);
					hashMap.put("rate_11", 12);
					if (hashMap.containsKey("rate")) 
					{
						System.out.println("ratevalue:" + hashMap.get("rate"));
						valueXmlString.append("<min_rate>").append("<![CDATA[" + rate + "]]>").append("</min_rate>");
						valueXmlString.append("<max_rate>").append("<![CDATA[" + rate + "]]>").append("</max_rate>");
						
					} else if (hashMap.containsKey(childNodeName)) 
					{
						System.out.println("ratevalue:"	+ hashMap.get(childNodeName));

						for (int i = hashMap.get(childNodeName); i < hashMap.size(); i++)
						{
							sql = "DECLARE CUR_RATE_1 DYNAMIC CURSOR FOR SQLSA ";
							pstmt = conn.prepareStatement(sql);
							liNoStr = Integer.toString(i);
							rateStr = "rate" + liNoStr + "_formula";
							rateNo = "rate" + liNoStr;
							sql1 = "Select "
									+ rateStr
									+ " From	Pricelist_mst	Where  price_list = ?";
							pstmt1 = conn.prepareStatement(sql1);
							pstmt1.setString(1, priceList);
							rs1 = pstmt1.executeQuery();
							if (rs1.next()) 
							{
								rate = rs1.getInt(1);

							}
							rs1.close();
							rs1 = null;
							pstmt1.close();
							pstmt1 = null;
							sql2 = "PREPARE SQLSA FROM " + sql1
									+ "OPEN DYNAMIC CUR_RATE_1 using :"
									+ priceList + ";"
									+ "FETCH CUR_RATE_1; CLOSE CUR_RATE_1 ;";
							pstmt2 = conn.prepareStatement(sql2);
							rs2 = pstmt2.executeQuery();
							if (rs2.next())
							{
								formula = rs2.getString(1);
							}
							rs2.close();
							rs2 = null;
							pstmt2.close();
							pstmt2 = null;

						}
					}

				}
				if (currentColumn.equalsIgnoreCase("item_code")) 
				{
					itemCode = genericUtility.getColumnValue("item_code",dom);
					sql = "Select descr, unit from item where item_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCode);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						descr = checkNull(rs.getString(1));
						unit = checkNull(rs.getString(2));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					effFrom = new java.sql.Timestamp(System.currentTimeMillis()) ;
					effFrom = utilMethods.RelativeDate(effFrom, 1);
					effFromStr = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(effFrom);
					
					valueXmlString.append("<eff_from>").append("<![CDATA[" + effFromStr + "]]>").append("</eff_from>");
					valueXmlString.append("<item_descr>").append("<![CDATA[" + descr + "]]>").append("</item_descr>");
					valueXmlString.append("<unit>").append("<![CDATA[" + unit + "]]>").append("</unit>");
					
				}
				valueXmlString.append("</Detail2>");
				
				break;

			}
			valueXmlString.append("</Root>");	
		} catch (Exception e) 
		{
			System.out.println(e.getMessage());
			System.out.println("Exception : " + e);
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(conn!=null)
				{
					if(rs != null )rs.close();
					if(rs1 != null )rs1.close();
					if(rs2 != null )rs2.close();
					
					rs = null;
					rs1 = null;
					rs2 = null;
					
					if(pstmt != null )pstmt.close();
					if(pstmt1 != null )pstmt1.close();
					if(pstmt2 != null )pstmt2.close();
					pstmt =null;
					pstmt1 =null;
					pstmt2 =null;
					conn.close();
				}
				conn = null;
			}catch(Exception d)
				{
				  d.printStackTrace();
				  throw new ITMException( d );
				}
			
		}
		return valueXmlString.toString();
		
	}
	private String errorType(Connection conn , String errorCode)
	{
		String msgType = "";
		PreparedStatement pstmt = null ; 
		ResultSet rs = null;
		try
		{			
			String sql = " SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ? ";
			pstmt = conn.prepareStatement(sql);			
			pstmt.setString(1, errorCode);			
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				msgType = rs.getString("MSG_TYPE");
			}			
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
	private String checkNull(String input)
	{
		if (input == null)
		{
			input = "";
		}
		return input;
	}
}