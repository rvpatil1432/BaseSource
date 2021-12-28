package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;

import java.text.SimpleDateFormat;
import java.util.*;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;

import java.text.DecimalFormat;
import java.text.NumberFormat;

//import org.apache.poi.hssf.record.formula.functions.Step;
//import org.apache.poi.util.SystemOutLogger;
//import org.apache.poi.hssf.record.formula.functions.Round;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.ejb.Stateless;
import javax.jcr.Value;

@Stateless
public class MinRateHistoryIC extends ValidatorEJB implements MinRateHistoryICLocal, MinRateHistoryICRemote {
	E12GenericUtility genericUtility = new E12GenericUtility();
	ibase.webitm.ejb.fin.FinCommon finCommon = new ibase.webitm.ejb.fin.FinCommon();
	
    	DistCommon distComm = new DistCommon();
	static String inventoryAcct = "N";

	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag,
			String xtraParams) throws RemoteException, ITMException {
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try {
			if (xmlString != null && xmlString.trim().length() > 0) {
				dom = parseString(xmlString);
				System.out.println("xmlString[" + xmlString + "]");
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0) {
				dom1 = parseString(xmlString1);
				System.out.println("xmlString1[" + xmlString1 + "]");
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0) {
				dom2 = parseString(xmlString2);
				System.out.println("xmlString2[" + xmlString2 + "]");
			}

			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
		} catch (Exception e) {
			System.out.println("Exception : [MinRateHistoryIC][wfValData( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e);
		}
		return (errString);
	}

	@SuppressWarnings({ "finally", "finally", "finally" })
	@Override
	public String wfValData(Document dom, Document dom1, Document dom2, String objCotext, String editFlag,
			String xtraParams) throws RemoteException, ITMException {

		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		String docKey = "", invoiceId = "";
		String schemeCode = "";
		int effCost = 0;
		Timestamp tranDate;
		SimpleDateFormat simpleDateFormat = null;
		String lineNo = null;
		String errCode = "";
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		int cc = 0, cnt = 0;
		String errorType = "", errString = "";
		String custCode = "", siteCode = "", lotNo = "";
		String itemCode = "",tranDateStr="";
		int ctr = 0, currentFormNo = 0;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql;
		String quantity = "", quantityAdj = "" ;
		double qty = 0, qtyAdj = 0; 
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> <Root> <Errors>");
	
		Calendar currentDate = Calendar.getInstance();
		SimpleDateFormat sdf;

		try {
			conn = getConnection();
			String userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			System.out.println("userId = " + userId);
			siteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			System.out.println("xtraParams = " + xtraParams);
			System.out.println("editFlag = " + editFlag);

			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			tranDateStr = sdf.format(currentDate.getTime());


			if (objCotext != null && objCotext.trim().length() > 0) 
			{
				currentFormNo = Integer.parseInt(objCotext);
			}

			switch (currentFormNo) 
			{
			case 1:
				System.out.println("VALIDATION FOR DETAIL [ 1 ]..........");
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				cc = childNodeList.getLength();
				for (ctr = 0; ctr < cc; ctr++) {
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();

					if (childNodeName.equalsIgnoreCase("doc_key")) 
					{
						docKey = checkNull(this.genericUtility.getColumnValue("doc_key", dom));
						System.out.println("docKey:" + docKey);
						if("A".equalsIgnoreCase(editFlag))
						{
							System.out.println("editFlag:" + editFlag);
							sql = "select count(*) from min_rate_history where doc_key= ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, docKey);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								cnt = rs.getInt(1);
							}
							rs.close();                                                                                                                       
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("cnt value is :[" + cnt + "]");

							if (cnt>0) 
							{
								errCode = "VTKNET";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					// Added By PriyankaC to check scheme code not null [Start].
					else if (childNodeName.equalsIgnoreCase("scheme_code")) 
					{
						schemeCode = checkNull(this.genericUtility.getColumnValue("scheme_code", dom));
						itemCode=checkNull(this.genericUtility.getColumnValue("item_code", dom));
						System.out.println("schemeCode : " +schemeCode); 
						if(schemeCode== null || schemeCode.trim().length() == 0)
						{
							errCode = "VMSCHCD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase()); 
						}
						// Added By PriyankaC to check scheme code not null [END]
						if(schemeCode!= null && schemeCode.trim().length() > 0 )
						{
							sql = "select count(*) from bom where bom_code = ? ";

							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, schemeCode);
							rs = pstmt.executeQuery();

							if (rs.next()) 
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(cnt==0)
							{
								System.out.println("itemCode Testing : " +itemCode);
								sql=  "select count(*) from item where item_code = ? "; 
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, schemeCode);
								rs=pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
							if (cnt == 0) 
							{
								errCode = "VTSCHNO1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					//Changed By PriyankaC to remove validation from invoice_id and line_no field [START].
				/*	else if (childNodeName.equalsIgnoreCase("invoice_id")) 
					{
						invoiceId = checkNull(genericUtility.getColumnValue("invoice_id", dom));
						System.out.println("INSIDE INVOICE ID[" + invoiceId + "]");
						if (invoiceId != null && invoiceId.trim().length() > 0) 
						{
							sql = "select count(*) from invoice where invoice_id = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, invoiceId);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if (cnt == 0)
							{
								errCode = "VTSRET1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
						else if (childNodeName.equalsIgnoreCase("line_no")) 
					{
						invoiceId = checkNull(genericUtility.getColumnValue("invoice_id", dom));
						lineNo = checkNull(genericUtility.getColumnValue("line_no", dom));

						if (invoiceId != null && invoiceId.trim().length() > 0) 
						{
							sql = "select count(*) from invdet where invoice_id = ? and line_no= ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, invoiceId);
							pstmt.setString(2, lineNo);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if (cnt == 0) 
							{
								errCode = "VTEMTLINO";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						System.out.println("Testing done");
					}*/
					//Changed By PriyankaC to remove validation from invoice_id and line_no field [END].
					else if (childNodeName.equalsIgnoreCase("cust_code")) 
					{
						System.out.println("Testing pending");
						custCode = checkNull(genericUtility.getColumnValue("cust_code", dom));
						if(custCode== null || custCode.trim().length() == 0)
						{
							errCode = "VTMSG";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase()); 
						}
						if (custCode != null && custCode.trim().length() > 0)
						{
							sql = "SELECT COUNT(*) FROM customer WHERE cust_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, custCode);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if (cnt == 0) 
							{
								errCode = "VTINVCUST";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					} 
					else if (childNodeName.equalsIgnoreCase("item_code"))
					{
						itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						if ( itemCode == null || itemCode.trim().length() == 0) 
						{
							errCode = "VTITMNUL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						} 
						if (itemCode != null & itemCode.trim().length() > 0)
						{
							System.out.println(" INSITE ITEM CODE VALIDATION ");
							sql = "SELECT count(*) from item  where item_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCode.trim());
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								cnt = rs.getInt(1);
							}
							System.out.println(" COUNT =====> [" + cnt + "]");
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if (cnt == 0)
							{
								errCode = "VTINVITM";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("site_code")) 
					{
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						if ( siteCode== null || siteCode.trim().length() == 0) 
						{
							errCode = "VTSITEEMT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						if (siteCode != null && siteCode.trim().length() > 0) 
						{
							System.out.println(" INSITE SITE CODE VALIDATION ");
							sql = " SELECT COUNT(1) FROM site WHERE site_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, siteCode.trim());
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if (cnt == 0) 
							{
								errCode = "VTIVSITE";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					//Added By PriyankaC  to check  adj_qty and  qty validation [start].
					else if (childNodeName.equalsIgnoreCase("quantity_adj")) 
					{
						quantityAdj = checkNull(genericUtility.getColumnValue("quantity_adj", dom));
						quantity    = checkNull(genericUtility.getColumnValue("quantity", dom));
						System.out.println("Value of quantity :" +quantityAdj +" [ qtyAdj ]" +quantity);

						
						if(quantity != null && quantity.trim().length() > 0)
						{
							qty= Double.parseDouble(quantity);
							System.out.println("Value of quantity :"+qty) ;
						}
						if(quantityAdj != null && quantityAdj.trim().length() > 0 )
						{
							qtyAdj= Double.parseDouble(quantityAdj);
							System.out.println("Value of quantity :"+qtyAdj) ;
						}
						System.out.println("Value of quantity FINAL :" +qty +" [ qtyAdj ]" +qtyAdj);
						if (qtyAdj > qty) 
						{
							errCode = "VTINADJQTY";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					//Added By PriyankaC to check  adj_qty and  qty validation [end].
				} 
				break ;
			}
			int errListSize = errList.size();
			cnt = 0;
			String errFldName = null;
			System.out.println("errListSize [" + errListSize + "] errFields size [" + errFields.size() + "]");
			if (errList != null && errListSize > 0) 
			{
				for (cnt = 0; cnt < errListSize; cnt++) 
				{
					errCode = errList.get(cnt);
					errFldName = errFields.get(cnt);
					System.out.println(" testing :errCode .:" + errCode);
					errString = getErrorString(errFldName, errCode, userId);
					errorType = errorType(conn, errCode);
					if (errString.length() > 0) 
					{
						String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8,
						errString.indexOf("<trace>"));
						bifurErrString = bifurErrString + errString.substring(errString.indexOf("</trace>") + 8,
						errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						errString = "";
					
					}
					if (errorType.equalsIgnoreCase("E")) 
					{
						break;
					}
				}
				errList.clear();
				errList = null;
				errFields.clear();
				errFields = null;
				errStringXml.append("</Errors> </Root> \r\n");
			} else 
			{
				errStringXml = new StringBuffer("");
			}
		} // end of try
		catch (Exception e) 
		{
			e.printStackTrace();
			errString = e.getMessage();
			throw new ITMException(e);
	    } 
		finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (conn != null) {
					conn.close();
					conn = null;
				}
			} 
			
		 	catch (Exception d) {
				d.printStackTrace();
				throw new ITMException(d);
			}
			errString = errStringXml.toString();
			System.out.println("testing : final errString : " + errString);
			return errString;
		}

	}// wf valdata method
		
	@Override
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext,
			String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException {
		Document dom = null;
		Document domhr = null;
		Document domAll = null;
		String retString = "";
		try {
			System.out.println("**************  Inside itemChanged method ****************");
			if (xmlString != null && xmlString.trim().length() > 0) {
				dom = genericUtility.parseString(xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0) {
				domhr = genericUtility.parseString(xmlString1);
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0) {
				domAll = genericUtility.parseString(xmlString2);
			}
			retString = itemChanged(dom, domhr, domAll, objContext, currentColumn, editFlag, xtraParams);
			System.out.println("retString::::::::::" + retString);
		} catch (Exception e) {
			System.out.println(":::" + getClass().getSimpleName() + "::" + e.getMessage());
			e.getMessage();
		}
		return retString;
	} // end of item change method

	@Override
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn,
			String editFlag, String xtraParams) throws RemoteException, ITMException {
		Connection conn = null;
		ConnDriver connDriver = new ConnDriver();
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		String sql, loginSite = "";
		String itemCode = "";
		int currentFormNo = 0, cnt = 0;
		StringBuffer valueXmlString = new StringBuffer();
		String columnValue = "";
		NodeList parentNodeList = null;
		Node parentNode = null;
		NodeList childNodeList = null;
		int childNodeListLength = 0, ctr = 0;
		Timestamp date = null, timestamp = null, today1 = null, mrefdate = null, mduedate = null, today = null,
				exprcpdate = null;
		String msite = "";
		String sitecode = "";
		String msdesc1 = "";
		String invoiceId = "";
		Timestamp trandate = null, sysdate = null;
		String lsValue, errString, mcode, chguser = null, emp;
		Timestamp tdate;
		String mdescr = "",descr="";
	    //DistCommon distComm = null;
		String disparmVal = null;
		String custCode = "";
		String loginSiteDescr = "";
		String childNodeName = "", reascode = "", reasdescr = "",siteCode="",lotNo="";
		Node childNode;
		Long lineno;
		Calendar currentDate = Calendar.getInstance();
		SimpleDateFormat sdf;
		String quantity = "";
		
		try {
			conn = getConnection();
			connDriver = null;
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			Date currentDateval = new Date();
			DistCommon distComm = new DistCommon();
			disparmVal = distComm.getDisparams("999999", "SALES_BANGLA", conn);
			System.out.println("getDisparams()....disparmVal..." + disparmVal);

			if (objContext != null && objContext.trim().length() > 0) {
				currentFormNo = Integer.parseInt(objContext);
			}
			System.out.println("[MinRateHistoryIc] [itemChanged] :currentFormNo ....." + currentFormNo);
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			switch (currentFormNo) {
			case 1:
				valueXmlString.append("<Detail1>");
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0;
				childNodeListLength = childNodeList.getLength();
				System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");
				
				if (currentColumn.trim().equals("itm_default")) 
				{
					loginSite = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
					sitecode = genericUtility.getColumnValue("site_code", dom);
					sql = " select descr from site where site_code = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, loginSite);
					rs = pStmt.executeQuery();

					if (rs.next()) 
					{
						loginSiteDescr = rs.getString("descr");
					}
					rs.close();
					pStmt.close();
					pStmt = null;
					rs = null;

					valueXmlString.append("<site_code protect =\"1\">").append("<![CDATA[" + loginSite.trim() + "]]>").append("</site_code>");
					valueXmlString.append("<site_descr>").append("<![CDATA[" + loginSiteDescr + "]]>").append("</site_descr>");

				}
				//Added By PriyankaC to make fields Not Editable [Start] 
				if (currentColumn.trim().equals("itm_defaultedit")) 
				{
					System.out.println("Inside itm_default");
					lotNo = genericUtility.getColumnValue("lot_no", dom);
					custCode = genericUtility.getColumnValue("cust_code", dom);
					siteCode = genericUtility.getColumnValue("site_code", dom);
					itemCode = genericUtility.getColumnValue("item_code", dom);
					quantity =	genericUtility.getColumnValue("quantity", dom);
					invoiceId=	genericUtility.getColumnValue("invoice_id", dom);
					
					valueXmlString.append("<lot_no protect =\"1\">").append("<![CDATA["+lotNo+"]]>").append("</lot_no>");
					valueXmlString.append("<cust_code protect =\"1\">").append("<![CDATA["+custCode+"]]>").append("</cust_code>");
					valueXmlString.append("<site_code protect =\"1\">").append("<![CDATA["+siteCode+"]]>").append("</site_code>");
					valueXmlString.append("<item_code protect =\"1\">").append("<![CDATA["+itemCode+"]]>").append("</item_code>");
					valueXmlString.append("<quantity protect =\"1\">").append("<![CDATA["+quantity+"]]>").append("</quantity>");
					valueXmlString.append("<invoice_id protect =\"1\">").append("<![CDATA["+invoiceId+"]]>").append("</invoice_id>");
				}
				//Added By PriyankaC to make fields Not Editable [END]
				 else if (currentColumn.trim().equals("invoice_id")) 
				 {
					sysdate = new Timestamp(currentDateval.getTime());
					invoiceId = genericUtility.getColumnValue("invoice_id", dom);
					System.out.println("invoiceId : " +invoiceId);
					sql = "select tran_date from invoice where invoice_id = ?";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, invoiceId);
					rs = pStmt.executeQuery();

					if (rs.next())
					{
						trandate = rs.getTimestamp("tran_date");
					}
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
					if (trandate != null) 
					{
						valueXmlString.append("<invoice_date >").append("<![CDATA[" + sdf.format(trandate) + "]]>").append("</invoice_date>");
					}
				}

				else if (currentColumn.trim().equalsIgnoreCase("site_code"))
				{
					msite = genericUtility.getColumnValue("site_code", dom);
					if (sitecode != null && sitecode.trim().length() > 0) 
					{
						sql = " select descr from site where site_code=?";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, msite);
						rs = pStmt.executeQuery();

						if (rs.next()) 
						{
							msdesc1 = rs.getString("descr");
						}
						rs.close();
						pStmt.close();
						pStmt = null;
						valueXmlString.append("<site_descr>").append("<![CDATA[" + msdesc1 + "]]>").append("</site_descr>");
					}
				} 
				else if (currentColumn.trim().equalsIgnoreCase("item_code")) 
				{
					itemCode = genericUtility.getColumnValue("item_code", dom);
					if(itemCode !=null && itemCode.trim().length()>0) 
					{
						sql="select descr from item where item_code= ?";
						pStmt=conn.prepareStatement(sql);
						pStmt.setString(1, itemCode);
						rs = pStmt.executeQuery();
						if(rs.next()) 
						{
							descr = rs.getString("descr");
						}
						rs.close();
						pStmt.close();
						pStmt= null;
						valueXmlString.append("<item_descr>").append("<![CDATA[" + descr + "]]>").append("</item_descr>");
					}	
				}

				else if (currentColumn.trim().equalsIgnoreCase("cust_code"))
				{
					custCode = genericUtility.getColumnValue("cust_code", dom);
					if (custCode != null && custCode.trim().length() > 0) 
					{
						sql = "select cust_name from customer where cust_code = ?";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, custCode);
						rs = pStmt.executeQuery();
						if (rs.next()) 
						{
							mdescr = rs.getString("cust_name");
						}
						rs.close();
						pStmt.close();
						pStmt = null;
						valueXmlString.append("<customer_cust_name>").append("<![CDATA[" + mdescr + "]]>").append("</customer_cust_name>");
					}
				}
				valueXmlString.append("</Detail1>");
				break;
			}// end of switch
			valueXmlString.append("</Root>");
		} // end of try
		catch (Exception e) {
			System.out.println("MinRateHistoryIC Exception ::" + e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pStmt != null) {
					pStmt.close();
					pStmt = null;
				}
				if (conn != null) {
					conn.close();
					conn = null;
				}
			} catch (Exception e) {
				System.out.println("Exception ::"+ e.getMessage());
				throw new ITMException(e);
			//	e.printStackTrace();
			}
		}
		System.out.println("valueXmlString @@@@@@@@@ [" + valueXmlString + "]");
		return valueXmlString.toString();

	} // end of item change method

	private String errorType(Connection conn, String errorCode) throws ITMException {
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
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		} catch (Exception ex) {
			System.out.println("Exception ::"+ ex.getMessage());
			ex.printStackTrace();
			throw new ITMException(ex);
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
				System.out.println("Exception ::"+ e.getMessage());
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return msgType;
	}
	
	private String checkNull(String input)
	{
		if (input == null || "null".equalsIgnoreCase(input) || "undefined".equalsIgnoreCase(input))
		{
			input= "";
		}
		return input.trim();
	}

} // Class
	// return null;
