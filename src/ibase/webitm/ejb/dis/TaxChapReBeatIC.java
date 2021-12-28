package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
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
public class TaxChapReBeatIC extends ValidatorEJB implements
		TaxChapReBeatICLocal, TaxChapReBeatICRemote {
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();

	public String wfValData() throws RemoteException, ITMException {
		return "";
	}

	public String itemChanged() throws RemoteException, ITMException {
		return "";
	}

	public String wfValData(String xmlString, String xmlString1,
			String xmlString2, String objContext, String editFlag,
			String xtraParams) throws RemoteException, ITMException {
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String errString = null;
		System.out.println("Validation Start..........");
		try {
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0) {
				dom2 = parseString(xmlString2);
			}
			errString = wfValData(dom, dom1, dom2, objContext, editFlag,
					xtraParams);
		} catch (Exception e) {
			System.out
					.println("Exception : TaxChapReBeatICEJB : wfValData(String xmlString) : ==>\n"
							+ e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
		}
		return (errString);
	}

	public String wfValData(Document dom, Document dom1, Document dom2,
			String objContext, String editFlag, String xtraParams)
			throws RemoteException, ITMException
			{
		String errString = " ";
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		String errCode = null;
		String userId = null;
		int cnt = 0;
		int ctr = 0;
		String itemCode = "";
		String countcode ="";
		int currentFormNo = 0;
		int childNodeListLength;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		String taxChap = "";
		ConnDriver connDriver = new ConnDriver();
		try {
			System.out.println("wfValData called");
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");

			if (objContext != null && objContext.trim().length() > 0) {
				currentFormNo = Integer.parseInt(objContext);
			}
			switch (currentFormNo) {
			case 1:
				System.out.println("VALIDATION FOR DETAIL [ 1 ]..........");
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				for (ctr = 0; ctr < childNodeListLength; ctr++) 
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					int seriCount = 0;
				     String col = genericUtility.getColumnValue("item_code", dom);
				     String col1 = genericUtility.getColumnValue("count_code", dom);
				     String col2 = genericUtility.getColumnValue("tax_chap",dom);
				     String Sql = "SELECT COUNT(1) AS COUNT FROM taxchap_rebate WHERE item_code=? AND count_code=?AND tax_chap=?";
			         pstmt = conn.prepareStatement(Sql);
			         pstmt.setString(1,col);
			         pstmt.setString(2,col1);
		             pstmt.setString(3,col2);
			         rs = pstmt.executeQuery();
				     System.out.println("Sql" + Sql);
				     if (rs.next()) 
				     {
				    	 seriCount = rs.getInt("COUNT");
				     }
				     System.out.println("COunt is"+seriCount);
				    if (!editFlag.equalsIgnoreCase("E")) 
				     {
				       if (seriCount > 0) 
				       {
					errString = getErrorString(" ", "VMDUPREC1", userId);				
					break;
				       }
				     }
				    if (childNodeName.equalsIgnoreCase("item_code")) 
					{
				    	itemCode = genericUtility.getColumnValue("item_code",dom);
				    	if(itemCode != null && itemCode.trim().length() > 0)
						{

							sql = "select count(1) from item where item_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;
							if (cnt == 0) {
								errCode = "STKVALITNE";
								errString = getErrorString("item_code",
										errCode, userId);
								break;
							}
						}
					} else if (childNodeName.equalsIgnoreCase("count_code")) 
					{

						countcode = genericUtility.getColumnValue("count_code", dom);
						if(countcode != null && countcode.trim().length() > 0)
						{
							sql = "select count(1) from country where count_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, countcode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;
							if (cnt == 0) {
								errCode = "WROCOUNTCD";
								errString = getErrorString("count_code",
										errCode, userId);
								break;
							}

						}
					}
					if (childNodeName.equalsIgnoreCase("tax_chap"))
					{
						taxChap = genericUtility.getColumnValue("tax_chap", dom);
						System.out.println("Tax chapter"+taxChap);
						if(taxChap != null && taxChap.trim().length() > 0)
						{
							sql = " SELECT COUNT(*) FROM taxchap WHERE tax_chap = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, taxChap.trim());
							rs = pstmt.executeQuery();
							cnt = 0;
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							if (cnt == 0) {
								errCode = "INTAXCHAP";
								errString = getErrorString("tax_chap", errCode,userId);
							}
							rs.close();
							rs = null;

							pstmt.close();
							pstmt = null;
						}
					}
				     System.out.println("Value of itemcode is:"+col);
				     System.out.println("Value of Country code is:"+col1);
				     System.out.println("Value of tax chapter is:"+col2);
				     if (col == null) 
				     {
				    	 errString = getErrorString(" ", "STKVALITCO", userId);
							break;
				     }
				     if (col1 == null) 
				     {
				    	 errString = getErrorString(" ", "STKVALCRCO", userId);
							break; 
				     }
				     if (col2 == null) 
				     {
				    	 errString = getErrorString(" ", "STKVALTXCP", userId);
						 break; 
				     }
				     if(col.trim().length() == 0  && col1.trim().length()==0 && col2.trim().length() == 0 )
				     {
				    	 System.out.println("column is space");
				    	 errString = getErrorString(" ", "VTMANFIELD", userId);				
							break; 
				     }
				}
				break;
				}
			

		} catch (Exception e) {
			System.out.println("Exception ::" + e);
			e.printStackTrace();
			errCode = "VALEXCEP";
			errString = getErrorString("", errCode, userId);
			throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
		} finally {
			try {
				if (conn != null) {
					if (pstmt != null) {
						pstmt.close();
						pstmt = null;
					}

					if (rs != null) {
						rs.close();
						rs = null;
					}
					conn.close();
				}
				conn = null;
			} catch (Exception d) {
				d.printStackTrace();
			}
			System.out.println(" < TaxChapReBeatICEJB > CONNECTION IS CLOSED");
		}
		System.out.println("ErrString ::" + errString);

		return errString;
	}// END OF VALIDATION

	@Override
	public String itemChanged(String xmlString, String xmlString1,
			String xmlString2, String objContext, String currentColumn,
			String editFlag, String xtraParams) throws RemoteException,
			ITMException {
		Document dom1 = null;
		Document dom = null;
		Document dom2 = null;
		String valueXmlString = "";
		//GenericUtility genericUtility = GenericUtility.getInstance();
		try {
			if (xmlString != null && xmlString.trim().length() != 0) {
				dom = genericUtility.parseString(xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() != 0) {
				dom1 = genericUtility.parseString(xmlString1);
			}
			if (xmlString2 != null && xmlString2.trim().length() != 0) {
				dom2 = genericUtility.parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext,
					currentColumn, editFlag, xtraParams);
		} catch (Exception e) {
			System.out
					.println("Exception :TaxChapReBeatIC:itemChanged(String,String,String,String,String,String):"
							+ e.getMessage() + ":");
			valueXmlString = genericUtility.createErrorString(e);
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
		}
		System.out.println("returning from TaxChapReBeatIC itemChanged");
		return (valueXmlString);
	}

	@Override
	public String itemChanged(Document dom, Document dom1, Document dom2,
			String objContext, String currentColumn, String editFlag,
			String xtraParams) throws RemoteException, ITMException {
		StringBuffer valueXmlString = new StringBuffer();

		Connection conn = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		String sql = "";
		int currentFormNo = 0;

		//GenericUtility genericUtility;
		String itemCode = "", descr = "";
		String countCode = "", itmdescr = "";
		try {
			//genericUtility = GenericUtility.getInstance();
			// siteCode = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");

			ConnDriver conndriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			conndriver = null;
			if (objContext != null && objContext.trim().length() > 0)
				currentFormNo = Integer.parseInt(objContext);

			currentColumn = currentColumn == null ? "" : currentColumn.trim();
			System.out.println("currentColumn : " + currentColumn);
			valueXmlString = new StringBuffer(
					"<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			System.out.println("current form no: " + currentFormNo);
			System.out.println("dom:::::::::: : "
					+ genericUtility.serializeDom(dom));
			System.out.println("dom11111111111111:::::::::: : "
					+ genericUtility.serializeDom(dom1));
			System.out.println("dom222222222222222:::::::::: : "
					+ genericUtility.serializeDom(dom2));

			switch (currentFormNo) {
			case 1:
				valueXmlString.append("<Detail1>");
				if ("itm_default".equalsIgnoreCase(currentColumn)) {
					System.out.println("itm_default : ");
					System.out.println("Content of xtraParams ..> "
							+ xtraParams);
					// tranDate = (sdf.format(timestamp).toString()).trim();
				}
				if (currentColumn.trim().equalsIgnoreCase("item_code")) {
					itemCode = genericUtility.getColumnValue("item_code", dom);
					sql = " select descr from item where item_code = ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, itemCode);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						itmdescr = (rs.getString("descr"));
					}
					System.out.println("Item Description" + itmdescr);
					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
					itmdescr = (itmdescr == null) ? "" : itmdescr.trim();
					valueXmlString.append("<item_descr >").append(
							"<![CDATA[" + itmdescr + "]]>").append(
							"</item_descr>");
				}

				else if (currentColumn.trim().equalsIgnoreCase("count_code")) {
					countCode = genericUtility
							.getColumnValue("count_code", dom);

					sql = " SELECT DESCR FROM COUNTRY WHERE COUNT_CODE= ? ";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, countCode);
					rs = pStmt.executeQuery();
					if (rs.next()) {
						descr = rs.getString("DESCR") == null ? "" : rs
								.getString("DESCR");
					}

					rs.close();
					rs = null;
					pStmt.close();
					pStmt = null;
					descr = (descr == null) ? "" : descr.trim();
					System.out.println("Country Description" + descr);
					valueXmlString.append("<country_descr>").append(descr)
							.append("</country_descr>\r\n");

				}
				valueXmlString.append("</Detail1 >");
				break;
			}
			valueXmlString.append("</Root>\r\n");
		} catch (Exception e) {
			System.out
					.println("Exception :TaxChapReBeatIC•••••••(Document,String):"
							+ e.getMessage() + ":");
			valueXmlString.delete(0, valueXmlString.length());
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
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
				System.out.println("Exception ::" + e);
				e.printStackTrace();
			}
		}
		System.out.println("\n***** ValueXmlString :" + valueXmlString
				+ ":*******");
		return valueXmlString.toString();
	}

}
