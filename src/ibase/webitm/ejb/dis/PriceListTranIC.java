package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.BaseLogger;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import javax.ejb.Stateless;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


@Stateless
public class PriceListTranIC extends ValidatorEJB implements
		PriceListTranICRemote, PriceListTranICLocal {

	// GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility = new E12GenericUtility();
//	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	

	@Override
	public String wfValData(String xmlString, String xmlString1,
			String xmlString2, String objContext, String editFlag,
			String xtraParams) throws RemoteException, ITMException {
		String rtStr = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try {
			//System.out.println("::: xmlString" + xmlString);
			//System.out.println("::: xmlString1" + xmlString1);
			//System.out.println("::: xmlString2" + xmlString2);
			BaseLogger.log("9", null, null, "::: xmlString" + xmlString);
			BaseLogger.log("9", null, null, "::: xmlString1" + xmlString1);
			BaseLogger.log("9", null, null, "::: xmlString2" + xmlString2);
			if (xmlString != null && xmlString.trim().length() > 0) {
				dom = genericUtility.parseString(xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0) {
				dom1 = genericUtility.parseString(xmlString1);
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0) {
				dom2 = genericUtility.parseString(xmlString2);
			}
			rtStr = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
		} catch (Exception e) {
			//System.out.println(":::" + this.getClass().getSimpleName() + "::"	+ e.getMessage());
			BaseLogger.log("0", null, null,"Exception in PriceListTranIC:: wfValData:: " + e.getMessage());
			e.getMessage();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return rtStr;
	}

	public String wfValData(Document dom, Document dom1, Document dom2,
			String objContext, String editFlag, String xtraParams)
			throws RemoteException, ITMException {
		System.out.println("inside wfValData....."+editFlag+"....."+xtraParams);
		String errString = "";
		String sql = "", priceList = "", manageType = "", refNo = "", refOld = "", itemCode = "", active = "";
		Connection conn = null;
		String userId = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		NodeList parentNodeList = null, childNodeList = null;
		Node parentNode = null, childNode = null;
		int ctr = 0, childNodeLength = 0, currentFormNo = 0, count = 0;
		Date validUptoDt = null, effDate = null;
		String childNodeName = "",  minQty = "", maxQty = "", maxRate = "", minRate = "", validUpto = "", effFrom = "", rate = "";
		String modName = "w_pricelist_tran", tranId = "", keyFlag = "", taxCode = "";
		//Pavan Rane 20may19[to change validation by arralist instead of direct return]
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		String errCode = "";
		//Pavan Rane end
		try {
			ConnDriver con = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = con.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END
			conn.setAutoCommit(false);
			SimpleDateFormat sdf = new SimpleDateFormat(
					genericUtility.getDispDateFormat());

			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, userId);
			if (objContext != null && objContext.trim().length() > 0) {
				currentFormNo = Integer.parseInt(objContext);
			}
			switch (currentFormNo) 
			{
				case 1: 
				{
					parentNodeList = dom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeLength = childNodeList.getLength();
					for (ctr = 0; ctr < childNodeLength; ctr++) 
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						if (childNodeName.equalsIgnoreCase("price_list"))
						{
							System.out.println(":::childNodeName" + childNodeName);
							priceList = genericUtility.getColumnValue("price_list",dom);
							priceList = priceList == null ? "" : priceList.trim();
							//System.out.println(":::price list:: " + priceList);
							BaseLogger.log("3", null, null, ":::price list:: " + priceList);
							sql = "select count(*) as count from pricelist_mst where price_list = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, priceList);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								count = rs.getInt("count");
							}
							close(pstmt, rs);
							//System.out.println("::: count:::" + count);
							 BaseLogger.log("3", null, null, "::: count:::" + count);
							if (count <= 0)
							{  
								//errString = getErrorString("price_list", "VTPLIST", userId); 
								//return errString;							
								errList.add("VTPLIST");
								errFields.add(childNodeName.toLowerCase());
							}
							//close(pstmt, rs);
	
							sql = "select manage_type from pricelist_mst where price_list = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, priceList);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								manageType = rs.getString("manage_type");
							}
							close(pstmt, rs);
							manageType = manageType == null ? "" : manageType.trim();
	
							//System.out.println(":: manage type::" + manageType);
							BaseLogger.log("3", null, null,":: manage type::" + manageType); 
							if (!manageType.equalsIgnoreCase("M") || manageType.equalsIgnoreCase(""))
							{
								//errString = getErrorString("price_list", "VTMTYPE", userId);
								//return errString;
								errList.add("VTMTYPE");
								errFields.add(childNodeName.toLowerCase());
							}
							//close(pstmt, rs);
						}
						// Changed By Nasruddin [20-SEP-16] START
						if (childNodeName.equalsIgnoreCase("tran_id")) 
						{
							//System.out.println(":::childNodeName" + childNodeName);
							BaseLogger.log("3", null, null,":::childNodeName" + childNodeName);
							tranId = E12GenericUtility.checkNull(genericUtility.getColumnValue("tran_id", dom));
							//sql = "SELECT KEY_FLAG FROM TRANSETUP WHERE TRAN_WINDOW =''";
							sql = "SELECT KEY_FLAG FROM TRANSETUP WHERE TRAN_WINDOW = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, modName);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								keyFlag = rs.getString("KEY_FLAG");
							}
							close(pstmt, rs);
							keyFlag = keyFlag == null ?"M": keyFlag.trim();
							System.out.println("keyFlag::["+keyFlag+"] tranId::["+tranId+"]editFlag::["+editFlag+"]");
							if( keyFlag == "M" && (tranId == null || tranId.trim().length() == 0 ))
							{
								//errString = getErrorString("tran_id", "VMCODNULL", userId);
								//return errString;
								errList.add("VMCODNULL");
								errFields.add(childNodeName.toLowerCase());
							}
							else
							{
								if("A".equals(editFlag))
								{
									count = 0;
									sql = "SELECT COUNT(1)  FROM PRICELIST_HDR WHERE TRAN_ID = ?";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, tranId);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										count = rs.getInt(1);
									}
									close(pstmt, rs);
									if(count > 0)
									{
										//errString = getErrorString("tran_id", "VMDUPL1", userId);
										//return errString;
										errList.add("VMDUPL1");
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
	
						}
						// Changed By Nasruddin [20-SEP-16] END
						if (childNodeName.equalsIgnoreCase("ref_no")) 
						{
							//System.out.println(":::childNodeName" + childNodeName);
							BaseLogger.log("3", null, null,":::childNodeName" + childNodeName);
							//refNo = chkNull(genericUtility.getColumnValue("ref_no", dom));
							refNo = E12GenericUtility.checkNull(genericUtility.getColumnValue("ref_no", dom));
							System.out.println("ref_no::["+refNo+"]");
							if(refNo.length() == 0)
							{
								//errString = getErrorString("ref_no", "VTCIRCUNL", userId);
								//return errString;
								errList.add("VTCIRCUNL");
								errFields.add(childNodeName.toLowerCase());
							}
							/*refNo = refNo == null ? "" : refNo.trim();
							System.out.println(":::ref No:: " + refNo);
	
							if (refNo.isEmpty()) {
								errString = getErrorString("ref_no", "VTCIRCUNL", userId);
								return errString;
							}*/
						}
	
						if (childNodeName.equalsIgnoreCase("ref_no_old")) {
							//System.out.println(":::childNodeName" + childNodeName);
							BaseLogger.log("3", null, null,":::childNodeName" + childNodeName);
							refOld = E12GenericUtility.checkNull(genericUtility.getColumnValue("ref_no_old",dom));
							refNo = E12GenericUtility.checkNull(genericUtility.getColumnValue("ref_no", dom));
							
							if(refOld.length() == 0)
							{
								//errString = getErrorString("ref_no_old", "VTCIRCUNL", userId);
								//return errString;
								errList.add("VTCIRCUNL");
								errFields.add(childNodeName.toLowerCase());
							}
							//Pavan R 6dec19 start [to validate ref no should not be same with old ref no]
							if(refOld.equals(refNo))
							{
								errList.add("VTDUPREFN1");
								errFields.add(childNodeName.toLowerCase());
							}
							//Pavan R 6dec19 end [to validate ref no should not be same with old ref no]
							/*refOld = refOld == null ? "" : refOld.trim();
							System.out.println(":::ref No old:: " + refOld);
	
							if (refOld.isEmpty()) {
								errString = getErrorString("ref_no_old", "VTCIRCOUNL", userId);
								return errString;
							}*/
						}
					}
					break;
				}
	
				case 2: 
				{
					parentNodeList = dom.getElementsByTagName("Detail2");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeLength = childNodeList.getLength();
					for (ctr = 0; ctr < childNodeLength; ctr++)
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						if (childNodeName.equalsIgnoreCase("item_code"))
						{
							//System.out.println(":::childNodeName" + childNodeName);
							BaseLogger.log("3", null, null,":::childNodeName" + childNodeName);
							itemCode = genericUtility.getColumnValue("item_code", dom);
							itemCode = itemCode == null ? "" : itemCode.trim();
							//System.out.println(":::item code:: " + itemCode);
							BaseLogger.log("3", null, null,":::item code:: " + itemCode);
							sql = "select count(*) as count from item where item_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								count = rs.getInt("count");
							}
							//System.out.println("::count ::: " + count);
							close(pstmt, rs);
							BaseLogger.log("3", null, null,"::count ::: " + count);
							if (count <= 0) {
								//errString = getErrorString("item_code", "VMITEM_CD", userId);
								//return errString;
								errList.add("VMITEM_CD");
								errFields.add(childNodeName.toLowerCase());
							}
	
							sql = "select active from item where item_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								active = rs.getString("active");
							}
							close(pstmt, rs);
							if (active.equalsIgnoreCase("N"))
							{
								//errString = getErrorString("item_code", "VTITEM4", userId);
								//return errString;
								errList.add("VTITEM4");
								errFields.add(childNodeName.toLowerCase());
							}
						}
	
						if (childNodeName.equalsIgnoreCase("min_qty"))
						{
							System.out.println(":::childNodeName" + childNodeName);
							minQty = genericUtility.getColumnValue("min_qty", dom);
							minQty = minQty == null ? "" : minQty.trim();
							System.out.println(":::min quantity:: " + minQty);
	
							Double min = Double.parseDouble(minQty);
							System.out.println("double min quantity::::" + min);
	
							maxQty = genericUtility.getColumnValue("max_qty", dom);
							maxQty = maxQty == null ? "" : maxQty.trim();
							System.out.println("::: max quantity:: " + maxQty);
	
							Double max = Double.parseDouble(maxQty);
							System.out.println("double max quantity::::" + max);
	
							if (min > max) 
							{
								//errString = getErrorString("min_qty", "VMMINQTY", userId);
								//return errString;
								errList.add("VMMINQTY");
								errFields.add(childNodeName.toLowerCase());
							}
						}
	
						if (childNodeName.equalsIgnoreCase("min_rate")) 
						{
							System.out.println(":::childNodeName" + childNodeName);
							minRate = genericUtility.getColumnValue("min_rate", dom);
							minRate = minRate == null ? "" : minRate.trim();
							System.out.println(":::min rate:: " + minRate);
	
							Double min = Double.parseDouble(minRate);
							System.out.println("double min rate ::::" + min);
	
							maxRate = genericUtility.getColumnValue("rate", dom);
							maxRate = maxRate == null ? "" : maxRate.trim();
							System.out.println(":::max rate:: " + maxRate);
	
							Double max = Double.parseDouble(maxRate);
							System.out.println("double max rate::::" + max);
	
							if (min > max)
							{
								//errString = getErrorString("min_rate", "VMMINRATE", userId);
								//return errString;
								errList.add("VMMINRATE");
								errFields.add(childNodeName.toLowerCase());
							}
						}
	
						if (childNodeName.equalsIgnoreCase("valid_upto")) 
						{
							System.out.println(":::childNodeName" + childNodeName);
							validUpto = genericUtility.getColumnValue("valid_upto", dom);
							validUpto = validUpto == null ? "" : validUpto.trim();
							System.out.println(":::valid upto:: " + validUpto);
	
							validUptoDt = new java.sql.Date(sdf.parse(validUpto).getTime());
							System.out.println("date valid upto::: " + validUptoDt);
	
							effFrom = genericUtility.getColumnValue("eff_from", dom);
							effFrom = effFrom == null ? "" : effFrom.trim();
							System.out.println(":::effective from:: " + effFrom);
	
							effDate = new java.sql.Date(sdf.parse(effFrom).getTime());
							System.out.println("date effective from::: " + effDate);
	
							if (validUptoDt.before(effDate) && !validUptoDt.equals(effDate)) {
								//errString = getErrorString("valid_upto", "VTVALUPTOE", userId);
								//return errString;
								errList.add("VTVALUPTOE");
								errFields.add(childNodeName.toLowerCase());
							}
						}
	
						if (childNodeName.equalsIgnoreCase("rate")) 
						{
							System.out.println(":::childNodeName" + childNodeName);
							rate = genericUtility.getColumnValue("rate", dom);
							rate = rate == null ? "" : rate.trim();
							System.out.println(":::rate:: " + rate);
	
							Double rateDouble = Double.parseDouble(rate);
							System.out.println("::double rate:: " + rateDouble);
	
							if (rateDouble <= 0) 
							{
								//errString = getErrorString("rate", "VTRATE2", userId);
								//return errString;
								errList.add("VTRATE2");
								errFields.add(childNodeName.toLowerCase());
							}
						}
						//Changed By Nasruddin 20-SEP-16 START
						if(childNodeName.equalsIgnoreCase("tax_code"))
						{
							taxCode = genericUtility.getColumnValue("tax_code", dom);
							count = 0;
							sql = "SELECT COUNT(1) FROM TAX WHERE TAX_CODE = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, taxCode);
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								count = rs.getInt(1);
							}
							close(pstmt, rs);
							if( count == 0)
							{
								//errString = getErrorString("tax_code", "VTTAX1", userId);
								//return errString;
								errList.add("VTTAX1");
								errFields.add(childNodeName.toLowerCase());
							}
	
						}
						//Changed By Nasruddin 20-SEP-16 END
					}
				}
			}//switch
			//Pavan Rane 20may19[to change validation by arralist instead of direct return]
			System.out.println("errList::"+errList.toString()+"] >>>>>> size>>"+errList.size());
			int errListSize = errList.size();
			int cntErr = 0;
			String errFldName = "", errorType = "";			
			System.out.println("errListSize ..........[" + errListSize + "]");
			if(errList != null && errListSize > 0)
			{
				for(cntErr = 0; cntErr < errListSize; cntErr ++)
				{
					errCode = errList.get(cntErr);
					errFldName = errFields.get(cntErr);					
					System.out.println("errCode ..........[" + errCode + "]");
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
			errString = errStringXml.toString();
			//Pavan Rane end
		} 
		catch (Exception e) {
			//System.out.println(":::: " + this.getClass().getSimpleName() + ":::" + e.getMessage());
			BaseLogger.log("0", null, null,"Exception in PriceListTranIC:: wfValData:: " + this.getClass().getSimpleName() + "::"	+ e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		} finally {
			try {

				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}

				if (conn != null) {
					conn.close();
					conn = null;
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		System.out.println("wfValData...:::" + genericUtility.parseString(errString));
		return errString;
	}

	private String chkNull(String columnValue) {
		if( columnValue == null)
		{
			columnValue = "";
		}
		else
		{
			
		}
		return columnValue.trim();
	}
	//Pavan Rane 20may19[to get error type]
	private String errorType(Connection conn, String errorCode) throws ITMException
	{
		String msgType = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			String sql = "SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, errorCode);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				msgType = rs.getString("MSG_TYPE");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new ITMException(ex);
		} finally
		{
			try
			{
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
			} catch (Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return msgType;
	}
	//Pavan Rane end
	private void close(PreparedStatement pstmt, ResultSet rs) {
		try {
			if (pstmt != null) {
				pstmt.close();
				pstmt = null;
			}
			if (rs != null) {
				rs.close();
				rs = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public String itemChanged(String xmlString, String xmlString1,
			String xmlString2, String objContext, String currentColumn,
			String editFlag, String xtraParams) throws RemoteException,
			ITMException {
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String rtStr = "";
		//changed by nasruddin 07-10-16
	//	GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility = new E12GenericUtility();
		System.out.println("In Itemchange String:::");

		try {
			if (xmlString != null && xmlString.trim().length() > 0) {
				dom = genericUtility.parseString(xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0) {
				dom1 = genericUtility.parseString(xmlString1);
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0) {
				dom2 = genericUtility.parseString(xmlString2);
			}
			rtStr = itemChanged(dom, dom1, dom2, objContext, currentColumn,
					editFlag, xtraParams);
		} catch (Exception e) {
			System.out.println(":::" + this.getClass().getSimpleName() + "::"
					+ e.getMessage());
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return rtStr;
	}

	public String itemChanged(Document dom, Document dom1, Document dom2,
			String objContext, String currentColumn, String editFlag,
			String xtraParams) throws RemoteException, ITMException {

		Connection conn = null;
		String priceList = "", itemCode = "", sql = "", descr = "", unit = "", descrItem = "";
		String rate = "", effDate = "", tranID = "";
		String lotNoFrom = "";
		String lotNoTo = "";		
		Timestamp effData = null;
		Timestamp validUpto = null;
		
		java.sql.Date validUptodt = null;
		 java.sql.Date effFromdt = null;

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		StringBuffer valueXmlString = new StringBuffer();
		int currentFormNo = 0;

		try {
			ConnDriver connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getDispDateFormat());
			valueXmlString = new StringBuffer(
					"<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			String loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(
					xtraParams, "loginSiteCode");
			System.out.println("loginsitecode.....=" + loginSiteCode);
			if (objContext != null && objContext.trim().length() > 0) {
				currentFormNo = Integer.parseInt(objContext);
			}
			switch (currentFormNo) {
			case 1:
				valueXmlString.append("<Detail1>");
				System.out
						.println("currentColumn detail 1::: " + currentColumn);
				if (currentColumn != null) {
					if (currentColumn.equalsIgnoreCase("itm_default")) {

						java.util.Date currentDate = null;
						Calendar cal = Calendar.getInstance();
						currentDate = cal.getTime();

						String currentDateNew = sdf.format(currentDate);

						System.out.println("itm_default called in form 1::::");
						valueXmlString.append("<tran_date><![CDATA["
								+ currentDateNew + "]]></tran_date>");
					}
					if (currentColumn.equalsIgnoreCase("price_list")) {
						System.out.println("price list called in form 1::::");

						priceList = genericUtility.getColumnValue("price_list",
								dom);
						priceList = priceList == null ? "" : priceList.trim();

						System.out.println("::: price list in itemchange::"
								+ priceList);

						sql = "select descr from pricelist_mst where price_list = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, priceList);
						rs = pstmt.executeQuery();

						if (rs.next()) {
							descr = rs.getString("descr");
						}
						close(pstmt, rs);
						valueXmlString.append("<descr><![CDATA[" + descr
								+ "]]></descr>");
					}
					valueXmlString.append("</Detail1>\r\n");
					System.out.println(":::::generated xml"
							+ valueXmlString.toString());
					break;
				}

			case 2:
				valueXmlString.append("<Detail2>");
				System.out
						.println("currentColumn detail 2::: " + currentColumn);
				if (currentColumn != null) {
					if (currentColumn.equalsIgnoreCase("itm_default")) {
						System.out.println("itm_default called in form 2::::");
						Calendar cal = Calendar.getInstance();
						cal.add(Calendar.DATE, 1);
						Date tomorrow = cal.getTime();

						System.out.println("tomorrow::" + tomorrow);

						effDate = sdf.format(tomorrow);

						System.out.println("effective date::" + effDate);

						valueXmlString.append("<eff_from><![CDATA[" + effDate
								+ "]]></eff_from>");
					}
					
					if (currentColumn.equalsIgnoreCase("itm_defaultedit")) {
						System.out.println("itm_default called in form 2::::");
						/*Calendar cal = Calendar.getInstance();
						cal.add(Calendar.DATE, 1);
						Date tomorrow = cal.getTime();

						System.out.println("tomorrow::" + tomorrow);

						effDate = sdf.format(tomorrow);

						System.out.println("effective date::" + effDate);

						valueXmlString.append("<eff_from><![CDATA[" + effDate + "]]></eff_from>");
*/						
						
						tranID = genericUtility.getColumnValue("tran_id", dom2);
						
						tranID = tranID == null ? "" : tranID.trim();
						
						System.out.println("::: tran id :: " + tranID);
						
						sql = "select valid_upto, eff_from from pricelist_det where tran_id = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, tranID);
						rs = pstmt.executeQuery();
						if(rs.next()){
							validUptodt = rs.getDate("valid_upto");
							effFromdt = rs.getDate("eff_from");
						}
						close(pstmt, rs);
						System.out.println("validUptodt::: " + validUptodt);
						
						System.out.println("effective from dt::: " + effFromdt);
						
						String date = sdf.format(validUptodt);
						
						String date1 = sdf.format(effFromdt);
						
						System.out.println("date::: " + date);
						
						System.out.println("date1::: " + date1);
						
						valueXmlString.append("<valid_upto><![CDATA[" + date + "]]></valid_upto>");
						
						valueXmlString.append("<eff_from><![CDATA[" + date1 + "]]></eff_from>");
						
					}
					if (currentColumn.equalsIgnoreCase("item_code")) {

						itemCode = genericUtility.getColumnValue("item_code",
								dom);
						itemCode = itemCode == null ? "" : itemCode.trim();
						System.out.println(":::itemcode in itemchange "
								+ itemCode);

						sql = "select descr, unit from item where item_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							descrItem = rs.getString("descr");
							unit = rs.getString("unit");
						}
						close(pstmt, rs);
						valueXmlString.append("<item_descr><![CDATA["
								+ descrItem + "]]></item_descr>");
						valueXmlString.append("<unit><![CDATA[" + unit
								+ "]]></unit>");
					}
					if (currentColumn.equalsIgnoreCase("rate")) {

						rate = genericUtility.getColumnValue("rate", dom);
						rate = rate == null ? "" : rate.trim();
						System.out.println(":::rate in itemchange " + rate);

						valueXmlString.append("<min_rate><![CDATA[" + rate
								+ "]]></min_rate>");
						valueXmlString.append("<max_rate><![CDATA[" + rate
								+ "]]></max_rate>");
					}
					/*Pavan R 4feb19 [to set eff_from from mfg_date of min lot entered in lot_no_from field.
					And valid_upto from exp_date of max lot no entered in Lot_no_to field]*/ 
					if (currentColumn.equalsIgnoreCase("lot_no__from"))
					{
						lotNoFrom = genericUtility.getColumnValue("lot_no__from", dom);						
						itemCode = genericUtility.getColumnValue("item_code", dom);
						BaseLogger.log("3", null, null, "lot_no__from ["+lotNoFrom+"] itemCode ["+itemCode+"]");
						if(!("0".equalsIgnoreCase(lotNoFrom) || "00".equalsIgnoreCase(lotNoFrom)))
						{
							sql = "select mfg_date from item_lot_info where item_code = ? and lot_no = ? "; 
							pstmt = conn.prepareStatement(sql);						
							pstmt.setString(1, itemCode);
							pstmt.setString(2, lotNoFrom);						
							rs = pstmt.executeQuery();
							if (rs.next()) {
								effData = rs.getTimestamp("mfg_date");							
							}
							close(pstmt, rs);
							BaseLogger.log("3", null, null, "lot_no__from >>>> eff_from ["+effData+"]");
							if(effData != null)
							{
								valueXmlString.append("<eff_from><![CDATA[" + sdf.format(effData) + "]]></eff_from>");
							}else
							{
								valueXmlString.append("<eff_from><![CDATA[]]></eff_from>");
							}
						}
					}
					if (currentColumn.equalsIgnoreCase("lot_no__to")) 
					{
						lotNoTo = genericUtility.getColumnValue("lot_no__to", dom);										
						itemCode = genericUtility.getColumnValue("item_code", dom);
						BaseLogger.log("3", null, null, "lot_no__to ["+lotNoTo+"] itemCode ["+itemCode+"]");
						if(!("z".equalsIgnoreCase(lotNoFrom) || "zz".equalsIgnoreCase(lotNoFrom) || "Z".equalsIgnoreCase(lotNoFrom) || "ZZ".equalsIgnoreCase(lotNoFrom)))
						{
							sql = "select exp_date from item_lot_info where item_code = ? and lot_no = ? "; 
							pstmt = conn.prepareStatement(sql);						
							pstmt.setString(1, itemCode);
							pstmt.setString(2, lotNoTo);						
							rs = pstmt.executeQuery();
							if (rs.next()) {
								validUpto = rs.getTimestamp("exp_date");							
							}
							close(pstmt, rs);
							BaseLogger.log("3", null, null, "lot_no__from >>>> valid_upto ["+validUpto+"]");
							if(validUpto != null)
							{
								valueXmlString.append("<valid_upto><![CDATA[" + sdf.format(validUpto) + "]]></valid_upto>");
							}else
							{
								valueXmlString.append("<valid_upto><![CDATA[]]></valid_upto>");
							}
						}						
					}/*Pavan R 4feb19 end*/
					valueXmlString.append("</Detail2>\r\n");
					System.out.println(":::::generated xml"+ valueXmlString.toString());
					break;
				}
			}
		} catch (Exception e) {
			System.out.println(":::: " + this.getClass().getSimpleName()
					+ ":::" + e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		} finally {
			try {

				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}

				if (conn != null) {
					conn.close();
					conn = null;
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		valueXmlString.append("</Root>\r\n");
		System.out.println("ValueXmlString:::::" + valueXmlString.toString());
		return valueXmlString.toString();
	}
	
}