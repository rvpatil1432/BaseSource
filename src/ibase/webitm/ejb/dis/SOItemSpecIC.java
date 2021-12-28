
/********************************************************
 Title : SOItemSpecIC
 Date  : 14/01/21
 Developer: Rohini Telang

 ********************************************************/

package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.sql.Timestamp;
@Stateless
public class SOItemSpecIC extends ValidatorEJB implements SOItemSpecICLocal, SOItemSpecICRemote {
	//Added by Rohini Telang on [25/01/2021][Start]
/*	public SOItemSpecIC(UserInfoBean userInfoBean)
	{
		setUserInfo(userInfoBean);
	}*/
	//Added by Rohini Telang on [25/01/2021][End]
	E12GenericUtility genericUtility = new E12GenericUtility();

	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams)
			throws RemoteException, ITMException {
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try {
			if (xmlString != null && xmlString.trim().length() > 0) {
				dom = parseString(xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0) {
				dom1 = parseString(xmlString1);
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0) {
				dom2 = parseString(xmlString2);
			}
			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
		} catch (Exception e) {
			throw new ITMException(e);
		}
		return errString;
	}

	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams)
			throws RemoteException, ITMException {

		String childNodeValue = "", childNodeName = "", errString = "",  errCode = "", item_code = "",sql = "", errorType = "", loginSiteCode= "",userId="";
		String saleOrder = "";
		String lineNo = "";
		String soLineNo = "";
		String itemCode = "";
		String lineNoSord = "";
		String tranDate = "";
		String tranId = "";
		String keyFlag = "";
		double quantity = 0;
		String siteCode = "";
		double qty = 0;
		StringBuffer valueXmlString = new StringBuffer();
		int count = 0, cnt = 0,ctr = 0, childNodeListLength;
		java.util.Date today = null, validUpto = null;
		NodeList parentNodeList = null, childNodeList = null;
		Node parentNode = null, childNode = null;
		int currentFormNo = 0;
		String qcTime = "";
		String currAppdateStr = "";
		int cntv = 0;
		int cntD = 0;//Added by Rohini Telang on 20-01-2021
		Timestamp currentDate = null,tranDt =  null;
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
		try {
			conn = getConnection();
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			loginSiteCode = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			java.sql.Timestamp currDate = null;
			currDate = new java.sql.Timestamp(System.currentTimeMillis()) ;
			currAppdateStr = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(currDate).toString();
			currentDate = Timestamp.valueOf(genericUtility.getValidDateString(currAppdateStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())
					+ " 00:00:00.0");
			System.out.println("####### currAppdate : "+currentDate);
			if (objContext != null && objContext.trim().length() > 0) {
				currentFormNo = Integer.parseInt(objContext);
				System.out.println("currentFormNo :" + currentFormNo);
			}
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");
			switch (currentFormNo) {
			case 1:
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail1>");
				childNodeListLength = childNodeList.getLength();
			//TODO -- FOR LOOP START.
			for (ctr = 0; ctr < childNodeListLength; ctr++)
			{
				count = 0;
				childNode = childNodeList.item(ctr);
				childNodeName = childNode.getNodeName().toLowerCase().trim();
				if(childNodeName.equalsIgnoreCase("tran_id"))
				{
					
					    sql = "select key_flag from transetup where tran_window = 'w_sorditem_spec'";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							keyFlag = rs.getString(1)==null ? "M" : rs.getString(1).trim();
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("keyFlag....."+keyFlag);  
						tranId = genericUtility.getColumnValue("tran_id", dom);
						System.out.println("tranId....."+tranId);  
						
						if(keyFlag.equalsIgnoreCase("M"))
						{
						
							if(tranId == null || tranId.trim().length() == 0 )
							{
								System.out.println("tranId null validatioon fire");
								errCode="VSTRANCD1 ";//The Transaction ID cannot be empty.						
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
							else
							{
								System.out.println("editFlag...."+editFlag);
								if(editFlag.equals("A"))
								{
									sql = "select count(*) from sorditem_spec where tran_id = ?";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, tranId);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										cntv = rs.getInt(1);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									System.out.println("cntv....."+cntv); 
									 if(cntv > 0)
									 {
										 System.out.println("trancode already exist validatioon fire");
										    errCode = "VSDUPL1";//Transaction ID entered already exists in master. Please enter different Transaction ID.
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
											break;
									 }
							  }
						 }
					}

				}
				if (childNodeName.equalsIgnoreCase("tran_date"))
				{
					tranDate = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					if (tranDate == null || tranDate.trim().length() == 0) 
					{
						errCode = "VMTDNULL";//Transaction date cannot be blank
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
					tranDt = Timestamp.valueOf(genericUtility.getValidDateString(tranDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())
							+ " 00:00:00.0");
					System.out.println("editFlag....@@@@@"+editFlag);//Added by Rohini T on 20-01-2021
					if(editFlag.equals("A"))
					{
						if(tranDt.before(currentDate))
						{
							errCode = "VSTRDTIV";//Transaction Date can not be less than Todays Date
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							break;
						}
					}
				}
				else if(childNodeName.equalsIgnoreCase("site_code"))
				{
					siteCode = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					if (siteCode == null || siteCode.trim().length() == 0) 
					{
						errCode = "VSTDNULL";// Site Code can not be blank
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}else {
						if (siteCode != null && siteCode.trim().length() > 0) {
							if (!(isExist(conn, "site", "site_code", siteCode))) {
								errCode = "VMSITE1";//The site code entered does not exist in the site master
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
				}
				else if(childNodeName.equalsIgnoreCase("sale_order"))
				{
					saleOrder = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					if (saleOrder == null || saleOrder.trim().length() == 0) 
					{
						errCode = "VSODNULL";// sale order can not be blank
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
					sql = "SELECT COUNT(1) FROM sorder WHERE sale_order =? and Status not in('C','X')";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, saleOrder);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						cnt = rs.getInt(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if (cnt == 0) {
						errList.add("VSODNVLL");//Please Enter valid Sale Order.
						errFields.add(childNodeName.toLowerCase());
					}
				}
				else if(childNodeName.equalsIgnoreCase("line_no__sord"))
				{
					saleOrder = checkNull(genericUtility.getColumnValue("sale_order", dom));
					soLineNo = checkNullAndTrim(genericUtility.getColumnValue("line_no__sord", dom));//Added by  Rohini T on [20-01-2021]
					System.out.println("saleOrder...."+saleOrder+"soLineNo...."+soLineNo);
					if (soLineNo == null || soLineNo.trim().length() == 0) 
					{
						errCode = "VSOLNULL";// Sale Order Line Number can not be blank
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
					//Added by  Rohini T on [20-01-2021]
					if (soLineNo == null) 
					{
						soLineNo = "   ";
					}
					else 
					{
						soLineNo = "    " + soLineNo;
						soLineNo = soLineNo.substring(soLineNo.length() - 3,soLineNo.length());
						System.out.println("soLineNo #####= ["+soLineNo+"]");
						sql = "select COUNT(*) from sorddet where sale_order=? and line_no=?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, saleOrder);
						pstmt.setString(2, soLineNo);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							cnt = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("cnt...."+cnt);
						if (cnt == 0) {
							errList.add("VSOLNEXM");//Line number not exists against sale order
							errFields.add(childNodeName.toLowerCase());
						}
						//Added by  Rohini T on [20-01-2021][Start]
						tranId = checkNull(genericUtility.getColumnValue("tran_id", dom));
						if(tranId==null || tranId.trim().length()==0)
						{
							tranId="@@@@@@";
						}
						sql = "select count(*) from sorditem_spec where sale_order =? and line_no__sord=? and tran_id <> ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, saleOrder);
						pstmt.setString(2, soLineNo);
						pstmt.setString(3, tranId);
						rs= pstmt.executeQuery();
						if(rs.next())
						{
							cntD=rs.getInt(1);
						}							
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (cntD > 0) 
						{
							errCode = "VDUPNALL";//Duplicate Transaction. Transaction is existing for specified sale order and Line Number.
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}	
					//Added by  Rohini T on [20-01-2021][End]
				}
				else if(childNodeName.equalsIgnoreCase("quantity"))
				{
					//quantity = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					quantity = Double.parseDouble( genericUtility.getColumnValue(childNodeName, dom)== null ?"0":genericUtility.getColumnValue(childNodeName, dom));
					saleOrder = checkNull(genericUtility.getColumnValue("sale_order", dom));
					lineNoSord= checkNullAndTrim(genericUtility.getColumnValue("line_no__sord", dom));//Added by  Rohini T on [20-01-2021]
					System.out.println("quantity...."+quantity+"saleOrder..."+saleOrder+"lineNoSord...."+lineNoSord);
					if (quantity <= 0 || quantity == 0) 
					{
						errCode = "VQANNULL";//Quantity cannot be negative or zero
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
					//Added by  Rohini T on [20-01-2021]
					if (lineNoSord == null) 
					{
						lineNoSord = "   ";
					}
					else 
					{
						lineNoSord = "    " + lineNoSord;
						lineNoSord = lineNoSord.substring(lineNoSord.length() - 3,lineNoSord.length());
						System.out.println("lineNoSord #####= ["+lineNoSord+"]");
						sql = "select quantity from sorddet where sale_order=? and line_no=?";
						pstmt = conn.prepareStatement( sql );
						pstmt.setString( 1, saleOrder );
						pstmt.setString( 2, lineNoSord );
						rs = pstmt.executeQuery();	
						if( rs.next() )
						{
							qty = rs.getDouble("quantity");
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
					}
					if(quantity != qty) 
					{
						errCode = "VSOQANVL";//Sale Order Quantity not valid
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
				}
				else if(childNodeName.equalsIgnoreCase("item_code"))
				{
					itemCode = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					if (itemCode == null || itemCode.trim().length() == 0) 
					{
						errCode = "VITMNULL";// Item Code can not be blank
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
				}
			}//TODO -- FOR LOOP End.
			valueXmlString.append("</Detail1>");
			break;
			case 2:
				System.out.println("---------------in detail2 validation------------------------");
				System.out.println("dom@@@@------->>" + genericUtility.serializeDom(dom));
				System.out.println("dom@@@@111------->>" + genericUtility.serializeDom(dom1));
				System.out.println("dom@@@@222------->>" + genericUtility.serializeDom(dom2));
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail2>");
				childNodeListLength = childNodeList.getLength();
				for (ctr = 0; ctr < childNodeListLength; ctr++) {
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equalsIgnoreCase("item_code"))
					{
						itemCode = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
						if (itemCode == null || itemCode.trim().length() == 0) 
						{
							errCode = "VITMNULL";// Item Code can not be blank
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					if (childNodeName.equalsIgnoreCase("quantity"))
					{
						quantity = Double.parseDouble( genericUtility.getColumnValue(childNodeName, dom)== null ?"0":genericUtility.getColumnValue(childNodeName, dom));
						System.out.println("quantity....@@@"+quantity);
						if (quantity <= 0 || quantity == 0) 
						{
							errCode = "VQANNULL";//Quantity cannot be negative or zero
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}
				valueXmlString.append("</Detail2>");
				break;
			}	
			int errListSize = errList.size();
			cnt = 0;
			String errFldName = null;
			if (errList != null && errListSize > 0) {
				for (cnt = 0; cnt < errListSize; cnt++) {
					errCode = errList.get(cnt);
					errFldName = errFields.get(cnt);
					System.out.println("errCode .........." + errCode);
					errString = getErrorString(errFldName, errCode, userId);
					errorType = errorType(conn, errCode);
					if (errString.length() > 0) {
						String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8, errString.indexOf("<trace>"));
						bifurErrString = bifurErrString+ errString.substring(errString.indexOf("</trace>") + 8,errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						errString = "";
					}
					if (errorType.equalsIgnoreCase("E")) {
						break;
					}
				}
				errList.clear();
				errList = null;
				errFields.clear();
				errFields = null;
				errStringXml.append("</Errors> </Root> \r\n");
			} else {
				errStringXml = new StringBuffer("");
			}
		} catch (Exception e) {
			e.printStackTrace();
			errString = e.getMessage();
			throw new ITMException(e);
		} finally {
			try {
				if (conn != null) {
					if (rs != null) {
						rs.close();
						rs = null;
					}
					if (pstmt != null) {
						pstmt.close();
						pstmt = null;
					}
					conn.close();
				}
				conn = null;
			} catch (Exception d) {
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		errString = errStringXml.toString();
		return errString;
	}

	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams)
			throws RemoteException, ITMException {
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try {
			if (xmlString != null && xmlString.trim().length() > 0) {
				dom = parseString(xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0) {
				dom1 = parseString(xmlString1);
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0) {
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext,
					currentColumn, editFlag, xtraParams);
		} catch (Exception e) {
			System.out
					.println("Exception : [SOItemSpecIC][itemChanged( String, String )] :==>\n"
							+ e.getMessage());
		}
		return valueXmlString;
	}

	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams)
			throws RemoteException, ITMException {

		Connection conn = null;
		String siteCode = "";
		String sql ="";
		String siteDescr = "";
		String salesOrder = "";
		String custCode = "";
		String lineNo  = "";
		String custName = "";
		String itemCodeDescr = "";
		String tranId = "";
		String itemCode ="";
		String itemType = "";
		String lineNoSord =  "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		StringBuffer valueXmlString = new StringBuffer();
		int currentFormNo = 0;
		int cnt = 0;//Added by  Rohini T on [20-01-2021]
		double quantity = 0;
		E12GenericUtility genericUtility = new E12GenericUtility();
		StringBuffer XmlString = new StringBuffer();
		StringBuffer xmlString = new StringBuffer();//Added by Rohini Telang on [20-01-2021]
		try {
			conn = getConnection();
			conn.setAutoCommit(false);
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			String loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			System.out.println("loginsitecode.....=" + loginSiteCode);
			DateFormat dateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String dbDateFormat = genericUtility.getDBDateFormat();
			String applDateFormat = genericUtility.getApplDateFormat();
			if (objContext != null && objContext.trim().length() > 0) {
				currentFormNo = Integer.parseInt(objContext);
			}
			switch (currentFormNo) 
			{
			case 1:
			{
				System.out.println("---------------in detail1 validation------------------------");
				System.out.println("dom@@@@------->>" + genericUtility.serializeDom(dom));
				System.out.println("dom@@@@111------->>" + genericUtility.serializeDom(dom1));
				System.out.println("dom@@@@222------->>" + genericUtility.serializeDom(dom2));
				System.out.println("Called.. case 1");
				valueXmlString.append("<Detail1>");
				System.out.println("currentColumn detail 1::: " + currentColumn);
				if (currentColumn != null) {
					if( currentColumn.trim().equalsIgnoreCase( "itm_default" ) )							
					{	
						java.util.Date currDate = new java.util.Date();
						SimpleDateFormat sdf = new SimpleDateFormat(applDateFormat);
						String currDateStr = sdf.format(currDate);
						String loginCode = checkNull(genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginCode" ));
						String chgTerm = checkNull(genericUtility.getValueFromXTRA_PARAMS( xtraParams, "chgTerm" ));
						siteCode = checkNull(genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginSiteCode" ));

						System.out.println("siteCode = ["+siteCode+"]");
						System.out.println("currDateStr..."+currDateStr+"loginCode....."+loginCode+"chgTerm...."+chgTerm);

						
						valueXmlString.append( "<tran_date><![CDATA[" ).append( currDateStr ).append( "]]></tran_date>\r\n" );
						valueXmlString.append( "<site_code><![CDATA[" ).append(  checkNull ( siteCode)).append( "]]></site_code>\r\n" );

						sql = "SELECT DESCR FROM SITE WHERE SITE_CODE = ?";
						pstmt = conn.prepareStatement( sql );
						pstmt.setString( 1, siteCode );						
						rs = pstmt.executeQuery();	
						if( rs.next() )
						{
							siteDescr = rs.getString("DESCR");
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;

						valueXmlString.append( "<site_descr><![CDATA[" ).append( checkNull( siteDescr )).append( "]]></site_descr>\r\n" );
						valueXmlString.append( "<chg_user><![CDATA[" ).append( loginCode ).append( "]]></chg_user>\r\n" );
						valueXmlString.append( "<chg_date><![CDATA[" ).append( currDateStr ).append( "]]></chg_date>\r\n" );
						valueXmlString.append( "<chg_term><![CDATA[" ).append( chgTerm ).append( "]]></chg_term>\r\n" );
					}
					else if( currentColumn.trim().equalsIgnoreCase( "itm_defaultedit" ) )							
					{
						//Added by  Rohini T on [20-01-2021][Start]
						tranId = genericUtility.getColumnValue("tran_id", dom2);
						lineNo = genericUtility.getColumnValue("line_no", dom2);
						salesOrder = checkNull(genericUtility.getColumnValue("sale_order", dom));
						lineNoSord = checkNull(genericUtility.getColumnValue("line_no__sord", dom));
						custCode = checkNull(genericUtility.getColumnValue("cust_code", dom));
						itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						System.out.println("tranId...@"+tranId+"lineNo.....@"+lineNo);
						System.out.println("salesOrder...@"+salesOrder+"lineNoSord.....@"+lineNoSord+"siteCode....."+siteCode);
						sql = "select count(*) as CNT from sorditem_spec_det where tran_id=? and line_no=?"; 
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, tranId);
						pstmt.setString(2, lineNo);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							cnt = rs.getInt("CNT");
						}
						pstmt.close();
						pstmt = null;
						rs.close();
						rs = null;
						if (cnt > 0)
						{
							valueXmlString.append("<sale_order  protect = \"1\"><![CDATA[" + salesOrder + "]]></sale_order>");
							valueXmlString.append("<line_no__sord  protect = \"1\"><![CDATA[" + lineNoSord + "]]></line_no__sord>");
						}
						sql = "select cust_name from customer where cust_code=?";
						pstmt = conn.prepareStatement( sql );
						pstmt.setString( 1, custCode );						
						rs = pstmt.executeQuery();	
						if( rs.next() )
						{
							custName = rs.getString("cust_name");
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						sql = "select descr from item where item_code=?";
						pstmt = conn.prepareStatement( sql );
						pstmt.setString( 1, itemCode );						
						rs = pstmt.executeQuery();	
						if( rs.next() )
						{
							itemCodeDescr = rs.getString("descr");
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						System.out.println("itemCodeDescr = ["+itemCodeDescr+"]");
						valueXmlString.append( "<cust_name><![CDATA[" ).append( checkNull(custName )).append( "]]></cust_name>\r\n" );
						valueXmlString.append( "<item_descr><![CDATA[" ).append( checkNull(itemCodeDescr )).append( "]]></item_descr>\r\n" );
						//Added by  Rohini T on [20-01-2021][End]
					}
					else if( currentColumn.trim().equalsIgnoreCase( "sale_order" ) )							
					{
						salesOrder = checkNull(genericUtility.getColumnValue("sale_order", dom));
						System.out.println("salesOrder..."+salesOrder);
						sql = "select s.cust_code,sd.line_no,sd.quantity,sd.item_code from sorder s,sorddet sd where s.sale_order = sd.sale_order and s.sale_order= ?";
						pstmt = conn.prepareStatement( sql );
						pstmt.setString( 1, salesOrder );						
						rs = pstmt.executeQuery();	
						if( rs.next() )
						{
							custCode = rs.getString(1);
							lineNo = rs.getString(2);
							quantity = rs.getDouble(3);
							itemCode = rs.getString(4);
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						System.out.println("custCode..."+custCode+"lineNo....."+lineNo+"quantity...."+quantity+"itemCode..."+itemCode);
						sql = "select cust_name from customer where cust_code=?";
						pstmt = conn.prepareStatement( sql );
						pstmt.setString( 1, custCode );						
						rs = pstmt.executeQuery();	
						if( rs.next() )
						{
							custName = rs.getString("cust_name");
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						/*System.out.println("custName = ["+custName+"]");//Commented by Rohini T on 20/01/2021
						sql = "select descr from item where item_code=?";
						pstmt = conn.prepareStatement( sql );
						pstmt.setString( 1, itemCode );						
						rs = pstmt.executeQuery();	
						if( rs.next() )
						{
							itemCodeDescr = rs.getString("descr");
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						System.out.println("itemCodeDescr = ["+itemCodeDescr+"]");*/
						//valueXmlString.append( "<line_no__sord><![CDATA[" ).append( checkNull( lineNo )).append( "]]></line_no__sord>\r\n" );//Commented by Rohini T on 20/01/2021
						valueXmlString.append( "<cust_code><![CDATA[" ).append( custCode ).append( "]]></cust_code>\r\n" );
						valueXmlString.append( "<cust_name><![CDATA[" ).append( custName ).append( "]]></cust_name>\r\n" );
						//valueXmlString.append( "<item_code><![CDATA[" ).append( itemCode ).append( "]]></item_code>\r\n" );//Commented by Rohini T on 20/01/2021
						//valueXmlString.append( "<item_descr><![CDATA[" ).append( itemCodeDescr ).append( "]]></item_descr>\r\n" );//Commented by Rohini T on 20/01/2021
						//valueXmlString.append( "<quantity><![CDATA[" ).append( quantity ).append( "]]></quantity>\r\n" );//Commented by Rohini T on 20/01/2021
					}
					else if( currentColumn.trim().equalsIgnoreCase( "line_no__sord" ) )							
					{
						salesOrder = checkNull(genericUtility.getColumnValue("sale_order", dom));
						lineNoSord = checkNull(genericUtility.getColumnValue("line_no__sord", dom));
						System.out.println("salesOrder = ["+salesOrder+"]");
						System.out.println("lineNoSord = ["+lineNoSord+"]");
						//Added by  Rohini T on [20-01-2021]
						if (lineNoSord == null) 
						{
							lineNoSord = "   ";
						}
						else 
						{
							lineNoSord = "    " + lineNoSord;
							lineNoSord = lineNoSord.substring(lineNoSord.length() - 3,lineNoSord.length());
							System.out.println("lineNoSord #####= ["+lineNoSord+"]");
							sql = "select item_code,quantity from sorddet where sale_order=? and line_no=?";
							pstmt = conn.prepareStatement( sql );
							pstmt.setString( 1, salesOrder );		
							pstmt.setString( 2, lineNoSord );	
							rs = pstmt.executeQuery();	
							if( rs.next() )
							{
								itemCode = rs.getString(1);
								quantity = rs.getDouble(2);
							
							}
							rs.close();rs = null;
							pstmt.close();pstmt = null;
						}
						//Added by  Rohini T on [20-01-2021][Start]
						valueXmlString.append("<line_no__sord>").append("<![CDATA[" + lineNoSord + "]]>").append("</line_no__sord>");//Added by  Rohini T on [29-01-2021]
						sql = "select descr from item where item_code=?";
						pstmt = conn.prepareStatement( sql );
						pstmt.setString( 1, itemCode );						
						rs = pstmt.executeQuery();	
						if( rs.next() )
						{
							itemCodeDescr = rs.getString("descr");
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						System.out.println("itemCodeDescr = ["+itemCodeDescr+"]");
						//Added by  Rohini T on [20-01-2021][End]
						System.out.println("itemCode...."+itemCode+"quantity...."+quantity);
						valueXmlString.append( "<item_code><![CDATA[" ).append( itemCode ).append( "]]></item_code>\r\n" );
						valueXmlString.append( "<quantity><![CDATA[" ).append( quantity ).append( "]]></quantity>\r\n" );
						valueXmlString.append( "<item_descr><![CDATA[" ).append( itemCodeDescr ).append( "]]></item_descr>\r\n" );//Added by  Rohini T on [20-01-2021]
					}
					//Added by  Rohini T on [20-01-2021][Start]
					else if( currentColumn.trim().equalsIgnoreCase( "site_code" ) )							
					{
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						sql = "SELECT DESCR FROM SITE WHERE SITE_CODE = ?";
						pstmt = conn.prepareStatement( sql );
						pstmt.setString( 1, siteCode );						
						rs = pstmt.executeQuery();	
						if( rs.next() )
						{
							siteDescr = rs.getString("DESCR");
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;

						valueXmlString.append( "<site_descr><![CDATA[" ).append( checkNull( siteDescr )).append( "]]></site_descr>\r\n" );
					}
					//Added by  Rohini T on [20-01-2021][End]
				}
				valueXmlString.append("</Detail1>/r/n");
				break;
			}
			case 2:
			{
				System.out.println("---------------in detail2 validation------------------------");
				System.out.println("dom@@@@------->>" + genericUtility.serializeDom(dom));
				System.out.println("dom@@@@111------->>" + genericUtility.serializeDom(dom1));
				System.out.println("dom@@@@222------->>" + genericUtility.serializeDom(dom2));
				System.out.println("Called.. case 2");
				
				valueXmlString.append("<Detail2 selected ='N'>\r\n");
				if( currentColumn.trim().equalsIgnoreCase( "itm_default" ) )							
				{
					tranId = genericUtility.getColumnValue("tran_id", dom1);
					lineNo = genericUtility.getColumnValue("line_no", dom1);
					itemCode = genericUtility.getColumnValue("item_code", dom1);
					valueXmlString.append( "<item_code><![CDATA[" ).append( itemCode ).append( "]]></item_code>\r\n" );
					sql = "SELECT ITEM_TYPE FROM ITEM WHERE  ITEM_CODE  = ? ";
					pstmt = conn.prepareStatement( sql );
					pstmt.setString( 1, itemCode );						
					rs = pstmt.executeQuery();	
					if( rs.next() )
					{
						itemType = rs.getString("ITEM_TYPE");
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;
					XmlString =	setAttrbVal(itemType,itemCode,editFlag, conn);//Added by Rohini Telang on [20-01-2021]
					System.out.println("valueXmlString:::"+XmlString);
					valueXmlString.append(XmlString);
				}
				if( currentColumn.trim().equalsIgnoreCase( "itm_defaultedit" ) )							
				{
					tranId = genericUtility.getColumnValue("tran_id", dom1);
					lineNo = genericUtility.getColumnValue("line_no", dom1);
					itemCode = genericUtility.getColumnValue("item_code", dom1);
					sql = "SELECT ITEM_TYPE FROM ITEM WHERE  ITEM_CODE  = ? ";
					pstmt = conn.prepareStatement( sql );
					pstmt.setString( 1, itemCode );						
					rs = pstmt.executeQuery();	
					if( rs.next() )
					{
						itemType = rs.getString("ITEM_TYPE");
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;
					XmlString =	setAttrbVal(itemType,itemCode,editFlag, conn);//Added by Rohini Telang on [20-01-2021]
					System.out.println("valueXmlString:::"+XmlString);
					valueXmlString.append(XmlString);
				}
				else if( currentColumn.trim().equalsIgnoreCase( "item_code" ) )							
				{
					itemCode = genericUtility.getColumnValue("item_code", dom);
					sql = "SELECT ITEM_TYPE FROM ITEM WHERE  ITEM_CODE  = ? ";
					pstmt = conn.prepareStatement( sql );
					pstmt.setString( 1, itemCode );						
					rs = pstmt.executeQuery();	
					if( rs.next() )
					{
						itemType = rs.getString("ITEM_TYPE");
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;
					XmlString =	setAttrbVal(itemType,itemCode,editFlag, conn);
					System.out.println("valueXmlString:::"+XmlString);
					valueXmlString.append(XmlString);
				}
				valueXmlString.append("</Detail2>/r/n");
				break;				  		
			}
			}
		} 
		catch (Exception e) 
		{

			System.out.println(":::: " + this.getClass().getSimpleName()
					+ ":::" + e.getMessage());
			e.printStackTrace();
		}
		finally 
		{
			try 
			{
				if (pstmt != null)
				{
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
		valueXmlString.append("</Root>");
		System.out.println("ValueXmlString:::::" + valueXmlString.toString());
		return valueXmlString.toString();
	}

	private String checkNullAndTrim(String value) {
		return value == null ? "" : value.trim();
	}
	
	private String checkNull(String value) {
		return value == null ? "" : value;
	}

	private int convertInt(String value) {
		return value.trim().length() == 0 ? 0 : Integer.parseInt(value);
	}

	private String errorType(Connection conn, String errorCode) {
		String msgType = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, errorCode);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				msgType = rs.getString("MSG_TYPE");
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return msgType;
	}
	//private StringBuffer setAttrbVal(String itemType,String itemCode, Connection conn) {//Changes done by Rohini T on 22/01/2021
	private StringBuffer setAttrbVal(String itemType,String itemCode,String editFlag, Connection conn) {
		String sql = "";
		String phyAttrOne = "", phyAttrTwo = "", phyAttrThree = "", phyAttrFour = "", phyAttrFive = "", phyAttrSix = "";
		String phyAttrValOne = "", phyAttrValTwo = "", phyAttrValThree = "", phyAttrValFour = "", phyAttrValFive = "", phyAttrValSix = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		StringBuffer XmlString = new StringBuffer();
		try {
			sql = "select PHY_ATTRIB_1, PHY_ATTRIB_2, PHY_ATTRIB_3, PHY_ATTRIB_4, PHY_ATTRIB_5, PHY_ATTRIB_6 from item_type where item_type = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, itemType);
			rs = pstmt.executeQuery();
			
			while (rs.next()) {
				phyAttrOne = checkNullAndTrim(rs.getString("PHY_ATTRIB_1"));
				phyAttrTwo = checkNullAndTrim(rs.getString("PHY_ATTRIB_2"));
				phyAttrThree = checkNullAndTrim(rs.getString("PHY_ATTRIB_3"));
				phyAttrFour = checkNullAndTrim(rs.getString("PHY_ATTRIB_4"));
				phyAttrFive = checkNullAndTrim(rs.getString("PHY_ATTRIB_5"));
				phyAttrSix = checkNullAndTrim(rs.getString("PHY_ATTRIB_6"));
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			sql = "select PHY_ATTRIB_1, PHY_ATTRIB_2, PHY_ATTRIB_3, PHY_ATTRIB_4, PHY_ATTRIB_5, PHY_ATTRIB_6 from item where item_code=?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, itemCode);
			rs = pstmt.executeQuery();
			
			while (rs.next()) {
				phyAttrValOne = checkNullAndTrim(rs.getString("PHY_ATTRIB_1"));
				phyAttrValTwo = checkNullAndTrim(rs.getString("PHY_ATTRIB_2"));
				phyAttrValThree = checkNullAndTrim(rs.getString("PHY_ATTRIB_3"));
				phyAttrValFour = checkNullAndTrim(rs.getString("PHY_ATTRIB_4"));
				phyAttrValFive = checkNullAndTrim(rs.getString("PHY_ATTRIB_5"));
				phyAttrValSix = checkNullAndTrim(rs.getString("PHY_ATTRIB_6"));
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if ((phyAttrOne != null) && phyAttrOne.trim().length() > 0) {
				XmlString.append("<phy_attrib__lab1><![CDATA[" + phyAttrOne + ":" + "]]></phy_attrib__lab1>");
			} else {
				XmlString.append("<phy_attrib__lab1><![CDATA[Phy Attrib1 :]]></phy_attrib__lab1>");
			}

			if ((phyAttrTwo != null) && phyAttrTwo.trim().length() > 0) {
				XmlString.append("<phy_attrib__lab2><![CDATA[" + phyAttrTwo + ":" + "]]></phy_attrib__lab2>");
			} else {
				XmlString.append("<phy_attrib__lab2><![CDATA[Phy Attrib2 :]]></phy_attrib__lab2>");
			}
			if ((phyAttrThree != null) && phyAttrThree.trim().length() > 0) {
				XmlString.append("<phy_attrib__lab3><![CDATA[" + phyAttrThree + ":" + "]]></phy_attrib__lab3>");
			} else {
				XmlString.append("<phy_attrib__lab3><![CDATA[Phy Attrib3 :]]></phy_attrib__lab3>");
			}
			if ((phyAttrFour != null) && phyAttrFour.trim().length() > 0) {
				XmlString.append("<phy_attrib__lab4><![CDATA[" + phyAttrFour + ":" + "]]></phy_attrib__lab4>");
			} else {
				XmlString.append("<phy_attrib__lab4><![CDATA[Phy Attrib4 :]]></phy_attrib__lab4>");
			}
			if ((phyAttrFive != null) && phyAttrFive.trim().length() > 0) {
				XmlString.append("<phy_attrib__lab5><![CDATA[" + phyAttrFive + ":" + "]]></phy_attrib__lab5>");
			} else {
				XmlString.append("<phy_attrib__lab5><![CDATA[Phy Attrib5 :]]></phy_attrib__lab5>");
			}
			if ((phyAttrSix != null) && phyAttrSix.trim().length() > 0) {
				XmlString.append("<phy_attrib__lab6><![CDATA[" + phyAttrSix + ":" + "]]></phy_attrib__lab6>");
			} else {
				XmlString.append("<phy_attrib__lab6><![CDATA[Phy Attrib6 :]]></phy_attrib__lab6>");
			}
			System.out.println("setAttrbVal editFlag="+editFlag);//Changes done by Rohini T on 22/01/2021
			if(!editFlag.equals("E"))
			{
				if ((phyAttrValOne != null) && phyAttrValOne.trim().length() > 0) {
					XmlString.append("<phy_attrib_1><![CDATA[" + phyAttrValOne + "]]></phy_attrib_1>");
				} else {
					XmlString.append( "<phy_attrib_1 protect = '0'><![CDATA[" ).append( "" ).append( "]]></phy_attrib_1>\r\n" );
				}
				if ((phyAttrValTwo != null) && phyAttrValTwo.trim().length() > 0) {
					XmlString.append("<phy_attrib_2><![CDATA[" + phyAttrValTwo + "]]></phy_attrib_2>");
				} else {
					XmlString.append( "<phy_attrib_2 protect = '0'><![CDATA[" ).append( "" ).append( "]]></phy_attrib_2>\r\n" );
				}
				if ((phyAttrValThree != null) && phyAttrValThree.trim().length() > 0) {
					XmlString.append("<phy_attrib_3><![CDATA[" + phyAttrValThree + "]]></phy_attrib_3>");
				} else {
					XmlString.append( "<phy_attrib_3 protect = '0'><![CDATA[" ).append( "" ).append( "]]></phy_attrib_3>\r\n" );
				}
				if ((phyAttrValFour != null) && phyAttrValFour.trim().length() > 0) {
					XmlString.append("<phy_attrib_4><![CDATA[" + phyAttrValFour + "]]></phy_attrib_4>");
				} else {
					XmlString.append( "<phy_attrib_4 protect = '0'><![CDATA[" ).append( "" ).append( "]]></phy_attrib_4>\r\n" );
				}
				if ((phyAttrValFive != null) && phyAttrValFive.trim().length() > 0) {
					XmlString.append("<phy_attrib_5><![CDATA[" + phyAttrValFive + "]]></phy_attrib_5>");
				} else {
					XmlString.append( "<phy_attrib_5 protect = '0'><![CDATA[" ).append( "" ).append( "]]></phy_attrib_5>\r\n" );
				}
				if ((phyAttrValSix != null) && phyAttrValSix.trim().length() > 0) {
					XmlString.append("<phy_attrib_6><![CDATA[" + phyAttrValSix + "]]></phy_attrib_6>");
				} else {
					XmlString.append( "<phy_attrib_6 protect = '0'><![CDATA[" ).append( "" ).append( "]]></phy_attrib_6>\r\n" );
				}
			}
		}
		catch (Exception e) {
			System.out.println("::" + this.getClass().getSimpleName() + "::::" + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				if (conn != null)
					conn.close();
				conn = null;
				
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		System.out.println("setAttrbVal xmlString="+XmlString);
		return XmlString;
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
	//Added by Rohini Telang on [25/01/2021][Start]
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ArrayList getSOItemListDetails(String saleOrder ,String lineNoSord,UserInfoBean userInfo) throws ITMException 
	{
		System.out.println("saleOrder : " +saleOrder +"lineNoSord " +lineNoSord);
		String sql = "";
		String tranId = "";
		String itemCode = "";
		String remarks = "";
		String itemCodeDescr = "";
		String phyAttrValOne = "", phyAttrValTwo = "", phyAttrValThree = "", phyAttrValFour = "", phyAttrValFive = "", phyAttrValSix = "";
		Connection conn = null;
		PreparedStatement pstmt = null,pstmt1= null;
		ResultSet rs = null,rs1= null;
		double lineNo = 0,quantity=0;
		int cnt=0;
		HashMap itemMap = null;
		HashMap SOItemListMap = new HashMap();
		//ArrayList<SOItemSpecBean> SOItemList = new ArrayList();
		ArrayList SOItemList = new ArrayList();
		try 
		{
			if (conn == null)
			{
				setUserInfo(userInfo);
				conn = getConnection();
			}
			if (lineNoSord == null) 
			{
				lineNoSord = "   ";
			}
			else 
			{
				lineNoSord = "    " + lineNoSord;
				lineNoSord = lineNoSord.substring(lineNoSord.length() - 3,lineNoSord.length());
				System.out.println("lineNoSord #####== ["+lineNoSord+"]");
				sql = "select item_code,tran_id from sorditem_spec where sale_order=? and line_no__sord=? ";
				System.out.println("sql......."+sql);
				pstmt = conn.prepareStatement( sql );
				pstmt.setString( 1, saleOrder);		
				pstmt.setString( 2, lineNoSord);	
				System.out.println("lineNoSord @@@=======>>["+lineNoSord+"]");
				rs = pstmt.executeQuery();	
				if( rs.next() )
				{
					itemCode = checkNull(rs.getString(1));
					tranId = checkNull(rs.getString(2));

				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
			}
			System.out.println("itemCode : " +itemCode +"tranId " +tranId);
			sql = "select descr from item where item_code=?";
			pstmt = conn.prepareStatement( sql );
			pstmt.setString( 1, itemCode );						
			rs = pstmt.executeQuery();	
			if( rs.next() )
			{
				itemCodeDescr = rs.getString("descr");
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;
			System.out.println("itemCodeDescr = ["+itemCodeDescr+"]");
			sql = "select LINE_NO,ITEM_CODE,PHY_ATTRIB_1,PHY_ATTRIB_2,PHY_ATTRIB_3,PHY_ATTRIB_4,PHY_ATTRIB_5,PHY_ATTRIB_6,QUANTITY,REMARKS from sorditem_spec_det where  tran_id=? order by line_no";
			pstmt = conn.prepareStatement( sql );		
			pstmt.setString( 1, tranId );	
			rs = pstmt.executeQuery();	
			cnt=0;
			while( rs.next() )
			{
				lineNo = rs.getDouble(1);
				itemCode = checkNull(rs.getString(2));
				phyAttrValOne = checkNullAndTrim(rs.getString(3));
				phyAttrValTwo = checkNullAndTrim(rs.getString(4));
				phyAttrValThree = checkNullAndTrim(rs.getString(5));
				phyAttrValFour = checkNullAndTrim(rs.getString(6));
				phyAttrValFive = checkNullAndTrim(rs.getString(7));
				phyAttrValSix = checkNullAndTrim(rs.getString(8));
				quantity = rs.getDouble(9);
				remarks = checkNull(rs.getString(10));
				cnt++;
				/*
				SOItemSpecBean itemSpecBean=new SOItemSpecBean();
				itemSpecBean.setLineNo(lineNo);
				itemSpecBean.setItemCode(itemCode);
				itemSpecBean.setItemDesc(itemCodeDescr);
				itemSpecBean.setPhyAttrValOne(phyAttrValOne);
				itemSpecBean.setPhyAttrValTwo(phyAttrValTwo);
				itemSpecBean.setPhyAttrValThree(phyAttrValThree);
				itemSpecBean.setPhyAttrValFour(phyAttrValFour);
				itemSpecBean.setPhyAttrValFive(phyAttrValFive);
				itemSpecBean.setPhyAttrValSix(phyAttrValSix);
				itemSpecBean.setQuantity(quantity);
				itemSpecBean.setRemarks(remarks);
				SOItemList.add(itemSpecBean);*/
				
				
				itemMap = new HashMap();
				itemMap.put("line_no", String.valueOf(lineNo));
				itemMap.put("item_code",itemCode);
				itemMap.put("descr",itemCodeDescr);
				itemMap.put("PHY_ATTRIB_1",phyAttrValOne);
				itemMap.put("PHY_ATTRIB_2",phyAttrValTwo);
				itemMap.put("PHY_ATTRIB_3",phyAttrValThree);
				itemMap.put("PHY_ATTRIB_4",phyAttrValFour);
				itemMap.put("PHY_ATTRIB_5",phyAttrValFive);
				itemMap.put("PHY_ATTRIB_6",phyAttrValSix);
				itemMap.put("QUANTITY",String.valueOf(quantity));
				itemMap.put("REMARKS",remarks);
				System.out.println("itemMap @@@=======>>["+itemMap+"]");
				SOItemList.add(itemMap);
				System.out.println("SOItemList @@@=======>>>>["+SOItemList+"]");
				System.out.println("cnt @@@=======>>["+cnt+"]");
				
				//SOItemListMap.put(itemCode,SOItemList);
				//System.out.println("SOItemListMap @@@=======>>["+SOItemListMap+"]");
				//Set setItem = SOItemListMap.entrySet(); 
				//Iterator itrItem = setItem.iterator();
				//System.out.println("setItem @@@=======>>["+setItem+"] itrItem......"+itrItem);
				//while(itrItem.hasNext())
				//{
					//Map.Entry itemMapEntry = (Map.Entry)itrItem.next();
					//itemCode = (String)itemMapEntry.getKey();
				//	SOItemList = (ArrayList)SOItemListMap.get(itemCode);
				//}
				System.out.println("SOItemList @@@@=======>>["+SOItemList.size()+"]");
				/*SOItemList.add(String.valueOf(lineNo));
				SOItemList.add(itemCode);
				SOItemList.add(itemCodeDescr);
				SOItemList.add(phyAttrValOne);
				SOItemList.add(phyAttrValTwo);
				SOItemList.add(phyAttrValThree);
				SOItemList.add(phyAttrValFour);
				SOItemList.add(phyAttrValFive);
				SOItemList.add(phyAttrValSix);
				SOItemList.add(String.valueOf(quantity));
				SOItemList.add(remarks);*/
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;
			
		}
		catch (Exception e) 
		{
			System.out.println("SOItemSpecIC.getSOItemListDetails()["+e.getMessage()+"]");
			e.printStackTrace();
			throw new ITMException(e);
		} 
		finally 
		{
			try 
			{
				if (rs != null) 
				{					
					rs.close();
					rs = null;
				}
				if (pstmt != null ) 
				{					
					pstmt.close();
					pstmt = null;
				}
				if (conn != null ) 
				{					
					conn.close();
					conn = null;
				}
			} 
			catch (Exception e) 
			{
				System.out.println("SOItemSpecIC.getSOItemListDetails()");
				e.printStackTrace();
			}
		}
		System.out.println("cnt =======>>["+cnt+"]");
		System.out.println("getSOItemListDetails =======>>["+SOItemList+"]");
		return SOItemList;
	}
	public ArrayList getSOItemLabelListDetails(String saleOrder ,String lineNoSord,UserInfoBean userInfo) throws ITMException 
	{
		String sql = "";
		String tranId = "";
		String itemCode = "";
		String itemType = "";
		String phyAttrOne = "", phyAttrTwo = "", phyAttrThree = "", phyAttrFour = "", phyAttrFive = "", phyAttrSix = "";
		ArrayList SOItemLabelList = new ArrayList();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try 
		{
			if (conn == null)
			{
				setUserInfo(userInfo);
				conn = getConnection();
			}
			if (lineNoSord == null) 
			{
				lineNoSord = "   ";
			}
			else 
			{
				lineNoSord = "    " + lineNoSord;
				lineNoSord = lineNoSord.substring(lineNoSord.length() - 3,lineNoSord.length());
				System.out.println("lineNoSord #####= ["+lineNoSord+"]");
				sql = "select item_code,tran_id from sorditem_spec where sale_order=? and line_no__sord=? ";
				System.out.println("sql......."+sql);
				pstmt = conn.prepareStatement( sql );
				pstmt.setString( 1, saleOrder);		
				pstmt.setString( 2, lineNoSord);	
				System.out.println("lineNoSord ;;; : " +lineNoSord);
				rs = pstmt.executeQuery();	
				if( rs.next() )
				{
					itemCode = checkNull(rs.getString(1));
					tranId = checkNull(rs.getString(2));

				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
			}
			System.out.println("itemCode : " +itemCode +"tranId " +tranId);
			sql = "SELECT ITEM_TYPE FROM ITEM WHERE  ITEM_CODE  = ? ";
			pstmt = conn.prepareStatement( sql );
			pstmt.setString( 1, itemCode );						
			rs = pstmt.executeQuery();	
			if( rs.next() )
			{
				itemType = rs.getString("ITEM_TYPE");
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;
			sql = "select PHY_ATTRIB_1, PHY_ATTRIB_2, PHY_ATTRIB_3, PHY_ATTRIB_4, PHY_ATTRIB_5, PHY_ATTRIB_6 from item_type where item_type = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, itemType);
			rs = pstmt.executeQuery();

			if(rs.next()) {
				phyAttrOne = rs.getString("PHY_ATTRIB_1");
				phyAttrTwo = rs.getString("PHY_ATTRIB_2");
				phyAttrThree = rs.getString("PHY_ATTRIB_3");
				phyAttrFour = rs.getString("PHY_ATTRIB_4");
				phyAttrFive = rs.getString("PHY_ATTRIB_5");
				phyAttrSix = rs.getString("PHY_ATTRIB_6");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			SOItemLabelList.add(phyAttrOne);
			SOItemLabelList.add(phyAttrTwo);
			SOItemLabelList.add(phyAttrThree);
			SOItemLabelList.add(phyAttrFour);
			SOItemLabelList.add(phyAttrFive);
			SOItemLabelList.add(phyAttrSix);
           /*
			SOItemLabelList.add(phyAttrOne == null ?"Phy Attrib1" :phyAttrOne);
			SOItemLabelList.add(phyAttrTwo == null ?"Phy Attrib2" :phyAttrTwo);
			SOItemLabelList.add(phyAttrThree == null ?"Phy Attrib3" :phyAttrThree);
			SOItemLabelList.add(phyAttrFour == null ?"Phy Attrib4" :phyAttrFour);
			SOItemLabelList.add(phyAttrFive == null ?"Phy Attrib5" :phyAttrFive);
			SOItemLabelList.add(phyAttrSix == null ?"Phy Attrib6" :phyAttrSix);*/
		}
		catch (Exception e) 
		{
			System.out.println("SOItemSpecIC.getSOItemListDetails()["+e.getMessage()+"]");
			e.printStackTrace();
			throw new ITMException(e);
		} 
		finally 
		{
			try 
			{
				if (rs != null) 
				{					
					rs.close();
					rs = null;
				}
				if (pstmt != null ) 
				{					
					pstmt.close();
					pstmt = null;
				}
				if (conn != null ) 
				{					
					conn.close();
					conn = null;
				}
			} 
			catch (Exception e) 
			{
				System.out.println("SOItemSpecIC.getSOItemListDetails()");
				e.printStackTrace();
			}
		}
		System.out.println("getSOItemlABELListDetails =======>>["+SOItemLabelList+"]");
		return SOItemLabelList;
		
	}
	//Added by Rohini Telang on [25/01/2021][End]
}

