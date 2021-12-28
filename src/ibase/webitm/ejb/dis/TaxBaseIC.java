package ibase.webitm.ejb.dis;


import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import javax.ejb.Stateless;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



@Stateless

public class TaxBaseIC extends ValidatorEJB implements TaxBaseICRemote, TaxBaseICLocal {

	//changed by nasruddin 05-10-16
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility = new E12GenericUtility();
	ITMDBAccessEJB itmdbAccessEJB = new ITMDBAccessEJB();

	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams)
			throws RemoteException, ITMException {

		String rtStr = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try {
			System.out.println("::: xmlString" + xmlString);
			System.out.println("::: xmlString1" + xmlString1);
			System.out.println("::: xmlString2" + xmlString2);

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
			System.out.println(":::" + this.getClass().getSimpleName() + "::" + e.getMessage());
			e.getMessage();
			throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
		}
		return rtStr;
	}
	@Override
	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams)
			throws RemoteException, ITMException {

		String errString = "";
		String sql = "";
		Connection conn = null;
		String userId = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		NodeList parentNodeList = null, childNodeList = null;
		Node parentNode = null, childNode = null;
		int ctr = 0, childNodeLength = 0, currentFormNo = 0;
		String childNodeName = "";
		try {
			ConnDriver con = new ConnDriver();
			//Changes and Commented By Bhushan on 13-06-2016 :START
			//conn = con.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 13-06-2016 :END
			conn.setAutoCommit(false);
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, userId);
			if (objContext != null && objContext.trim().length() > 0) {
				currentFormNo = Integer.parseInt(objContext);
			}
			switch (currentFormNo) {
			case 1:
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeLength = childNodeList.getLength();
				for (ctr = 0; ctr < childNodeLength; ctr++) {
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equalsIgnoreCase("tax_base"))
					{
						System.out.println(":::childNodeName" + childNodeName);
						String taxBase = genericUtility.getColumnValue("tax_base", dom);
						//String taxBase = childNode.getFirstChild().getNodeValue();
						taxBase = taxBase == null ? "" : taxBase.trim();
						System.out.println("::: taxBase" + taxBase);
						if(taxBase.length() <= 0)
						{
							errString = itmdbAccessEJB.getErrorString("tax_base", "VMTAXB1", userId, "", conn);
							break;
						}
						/* Comment By Nasruddin Start 21-SEP-16 START 
						else
						{
							if(editFlag.equalsIgnoreCase("A"))
							{
								int count = 0;
								sql = "select count(*) as count from taxbase where tax_base = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, taxBase);
								rs = pstmt.executeQuery();

								if(rs.next()){
									count = rs.getInt("count");
								}
								if(count > 0){
									errString = itmdbAccessEJB.getErrorString("tax_base", "VTTAXBDUP", userId, "", conn);
									break;
								}	
							}

						}Comment By Nasruddin Start 21-SEP-16 END*/
					}
					if(childNodeName.equalsIgnoreCase("descr"))
					{
						String descr = genericUtility.getColumnValue("descr", dom);
						//String descr = childNode.getFirstChild().getNodeValue();
						descr = descr == null ? "" : descr.trim();
						System.out.println("::: descr" + descr);
						if(descr.length() <= 0)
						{
							errString = itmdbAccessEJB.getErrorString("descr", "VMDESCR", userId, "", conn);
							break;
						}
					}
					if(childNodeName.equalsIgnoreCase("apply_on"))
					{
						String applyOn = genericUtility.getColumnValue("apply_on", dom);
						//String applyOn = childNode.getFirstChild().getNodeValue();
						applyOn = applyOn == null ? "" : applyOn.trim();
						System.out.println("::: applyOn " + applyOn);
						if(applyOn.length() <= 0)
						{
							errString = itmdbAccessEJB.getErrorString("apply_on", "VMAPPON1", userId, "", conn);
							break;
						}
					}

					if(childNodeName.equalsIgnoreCase("apply_perc"))
					{
						String applyPerc = genericUtility.getColumnValue("apply_perc", dom);
						//String applyPerc = childNode.getFirstChild().getNodeValue();
						applyPerc = applyPerc == null ? "" : applyPerc.trim();
						System.out.println("::: applyPerc " + applyPerc);
						if(editFlag.equalsIgnoreCase("A") || editFlag.equalsIgnoreCase("E"))
						{
							if(applyPerc.length() <= 0)
							{
								errString = itmdbAccessEJB.getErrorString("apply_perc", "VMAPPLPERC", userId, "", conn);
								break;
							}
						}
					}
				}
				break;
			case 2:
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeLength = childNodeList.getLength();
				for (ctr = 0; ctr < childNodeLength; ctr++) {
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if(childNodeName.equalsIgnoreCase("tax_base"))
					{
						String tax = genericUtility.getColumnValue("tax_base", dom);
						//String tax = childNode.getFirstChild().getNodeValue();
						System.out.println(":::tax" + tax);
						tax = tax == null ? "" : tax.trim();
						if(tax.length() <= 0)
						{
							errString = itmdbAccessEJB.getErrorString("tax_base", "VMTAXB1", userId, "", conn); 
							break;
						}
					}
					if(childNodeName.equalsIgnoreCase("tax_code"))
					{
						String taxCode = genericUtility.getColumnValue("tax_code", dom);
						//String taxCode = childNode.getFirstChild().getNodeValue();
						taxCode = taxCode == null ? "" : taxCode.trim();
						/* Comment By Nasrrudn 21-SEP-16 START
						if(taxCode.length() <= 0)
						{
							errString = itmdbAccessEJB.getErrorString("tax_code", "VTTAXC", userId, "", conn);
							break;
						}
						//else Comment By Nasrrudn 21-SEP-16 END*/
						//{
						int count = 0;
						sql = "select count(*) as count from tax where tax_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, taxCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							count = rs.getInt("count");
						}
						if(count <= 0)
						{
							errString = itmdbAccessEJB.getErrorString("tax_code", "VTTAX1", userId, "", conn); 
							break;
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						//}
					}
					if(childNodeName.equalsIgnoreCase("seq_no"))
					{
						String seqNo = genericUtility.getColumnValue("seq_no", dom);
						//String seqNo = childNode.getFirstChild().getNodeValue();
						seqNo = seqNo == null ? "" : seqNo.trim();
						if(seqNo.length() <= 0)
						{
							errString = itmdbAccessEJB.getErrorString("seq_no", "VMSEQNO", userId, "", conn); 
							break;
						}
						if(!seqNo.matches("\\d+")){
							errString = itmdbAccessEJB.getErrorString("seq_no", "VTSEQINV", userId, "", conn);
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			System.out.println(":::" + this.getClass().getSimpleName() + ":::" + e.getMessage());
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
		}
		// Changed By Nasruddin 21-SEP-16 STart
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
				throw new ITMException(d);
			}

		}	// Changed By Nasruddin 21-SEP-16 STart
		return errString;
	}
	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn, String editFlag, 
			String xtraParams) throws RemoteException, ITMException {
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String rtStr = "";
		//changed by nasruddin 05-10-16
		//GenericUtility genericUtility = GenericUtility.getInstance();
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
			rtStr = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
		} catch (Exception e) {
			System.out.println(":::" + this.getClass().getSimpleName() + "::" + e.getMessage());
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
		}
		return rtStr;
	}
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag,
			String xtraParams) throws RemoteException, ITMException {

		Connection conn = null;
		String taxBase = "", taxCode = "", sql = "", descr = "", seqNo = "";
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
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			String loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			System.out.println("loginsitecode.....=" + loginSiteCode);
			if (objContext != null && objContext.trim().length() > 0) {
				currentFormNo = Integer.parseInt(objContext);
			}
			switch (currentFormNo) {
			case 1:
				valueXmlString.append("<Detail1>");
				System.out.println("currentColumn: " + currentColumn);
				if (currentColumn != null) {
					if (currentColumn.equalsIgnoreCase("itm_default")) {
						if (currentColumn.equalsIgnoreCase("tax_base")){
							taxBase = genericUtility.getColumnValue("tax_base", dom);
							System.out.println(":::: taxBase" + taxBase);
							taxBase = taxBase == null ? "" : taxBase.trim();	
						}

						System.out.println("itm_default called::::");
						valueXmlString.append("<tax_base><![CDATA["+ taxBase + "]]></tax_base>");
					}
					valueXmlString.append("</Detail1>\r\n");
					break;
				}
			case 2 :
				valueXmlString.append("<Detail2>");
				System.out.println("currentColumn: " + currentColumn);
				if (currentColumn != null) {
					if (currentColumn.equalsIgnoreCase("tax_code")){
						System.out.println("editFlag:::" + editFlag);

						taxCode = genericUtility.getColumnValue("tax_code", dom);
						System.out.println(":::: taxCode" + taxCode);
						taxCode = taxCode == null ? "" : taxCode.trim();	
						try {
							if(taxCode.length() > 0){
								sql = "select descr from tax where tax_code = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, taxCode);
								rs = pstmt.executeQuery();

								if(rs.next()){
									descr = rs.getString("descr");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						} catch (Exception e) {
							System.out.println("::" + this.getClass().getSimpleName() + "::::" + e.getMessage());
							e.printStackTrace();
						}
						System.out.println("tax_code called::::");
						valueXmlString.append("<tax_descr><![CDATA["+ descr + "]]></tax_descr>");	
					}
					if (currentColumn.equalsIgnoreCase("itm_defaultedit")){
						if(editFlag.equalsIgnoreCase("E")){
							taxCode = genericUtility.getColumnValue("tax_code", dom);
							System.out.println(":::: taxCode" + taxCode);
							taxCode = taxCode == null ? "" : taxCode.trim();

							System.out.println("tax_code called::::");
							valueXmlString.append("<tax_code  protect = \"1\"><![CDATA["+ taxCode + "]]></tax_code>");

						}
					} if (currentColumn.equalsIgnoreCase("itm_default")){

						taxCode = genericUtility.getColumnValue("tax_code", dom);
						System.out.println(":::: taxCode" + taxCode);
						taxCode = taxCode == null ? "" : taxCode.trim();

						System.out.println("tax_code called::::");
						valueXmlString.append("<tax_code  protect = \"0\"><![CDATA["+ taxCode + "]]></tax_code>");

					}
					/*if (currentColumn.equalsIgnoreCase("itm_defaultedit")){
						System.out.println("itmdefaultedit called::::");
						seqNo = genericUtility.getColumnValue("seq_no", dom);
						 System.out.println(":::: seqNo" + seqNo);
						 seqNo = seqNo == null ? "" : seqNo.trim();

						 valueXmlString.append("<seq_no  protect = \"1\"><![CDATA["+ seqNo + "]]></seq_no>");

						 taxCode = genericUtility.getColumnValue("tax_code", dom);
						 System.out.println(":::: taxCode" + taxCode);
						 taxCode = taxCode == null ? "" : taxCode.trim();

						 valueXmlString.append("<tax_code  protect = \"1\"><![CDATA["+ taxCode + "]]></tax_code>");
					}*/

					/*if (currentColumn.equalsIgnoreCase("itm_default")) {

					if (currentColumn.equalsIgnoreCase("tax_code")){
						taxCode = genericUtility.getColumnValue("tax_code", dom);
						 System.out.println(":::: taxCode" + taxCode);
						 taxCode = taxCode == null ? "" : taxCode.trim();	
						 try {
							 if(taxCode.length() > 0){
									sql = "select descr from tax where tax_code = ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, taxCode);
									rs = pstmt.executeQuery();

								if(rs.next()){
									descr = rs.getString("descr");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						} catch (Exception e) {
							System.out.println("::" + this.getClass().getSimpleName() + "::::" + e.getMessage());
							e.printStackTrace();
						}
						 System.out.println("tax_code called::::");
						 valueXmlString.append("<tax_descr><![CDATA["+ descr + "]]></tax_descr>");
						 valueXmlString.append("<tax_base><![CDATA["+ taxBase + "]]></tax_base>");
					}


				}*/
					/*else if (currentColumn.equalsIgnoreCase("itm_defaultedit")){
						if(currentColumn.equalsIgnoreCase("seq_no")){
							seqNo = genericUtility.getColumnValue("seq_no", dom);
							 System.out.println(":::: seqNo" + seqNo);
							 seqNo = seqNo == null ? "" : seqNo.trim();

						}
						 System.out.println("itmdefaultedit called::::");
						 valueXmlString.append("<seq_no  protect = \"1\"><![CDATA["+ seqNo + "]]></seq_no>");
						 valueXmlString.append("<tax_base><![CDATA["+ taxBase + "]]></tax_base>");
						 valueXmlString.append("<tax_code  protect = \"1\"><![CDATA["+ taxCode + "]]></tax_code>");
						 valueXmlString.append("<tax_descr><![CDATA["+ descr + "]]></tax_descr>");
					}*/
					System.out.println(":::::generated xml" + valueXmlString.toString());
					valueXmlString.append("</Detail2>\r\n");
					break;
				}
				/*System.out.println("::currentFormNo" + currentFormNo);
				parentNodeList = dom.getElementsByTagName("Detail1");
				System.out.println("::: in detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeLength = childNodeList.getLength();
				for (ctr = 0; ctr < childNodeLength; ctr++) {
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if(childNodeName.equalsIgnoreCase("tax_base")){
						 taxBase = genericUtility.getColumnValue("tax_base", dom);
						 System.out.println(":::: taxBase" + taxBase);
						 //taxBase = childNode.getFirstChild().getNodeValue();
						taxBase = taxBase == null ? "" : taxBase.trim();	
					}
					break;
				}
					case 2:
					System.out.println("::currentFormNo" + currentFormNo);
					parentNodeList = dom.getElementsByTagName("Detail2");
					System.out.println(":::in detail2");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeLength = childNodeList.getLength();
					for (ctr = 0; ctr < childNodeLength; ctr++) {
						childNode = childNodeList.item(ctr);
					if(childNodeName.equalsIgnoreCase("tax_code")){
						taxCode = genericUtility.getColumnValue("tax_code", dom);
						// taxCode = childNode.getFirstChild().getNodeValue();
						taxCode = taxCode == null ? "" : taxCode.trim();
						System.out.println("::: taxCode" + taxCode);
						if(taxCode.length() > 0){
							sql = "select descr from tax where tax_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, taxCode);
							rs = pstmt.executeQuery();

						if(rs.next()){
							descr = rs.getString("descr");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					}
				}
				valueXmlString.append("<Detail2>");
				System.out.println("currentColumn: " + currentColumn);
				if (currentColumn.equalsIgnoreCase("itm_default")) {
					System.out.println("itm_default called::::");
					valueXmlString.append("<tax_base><![CDATA["+ taxBase + "]]></tax_base>");
				}
				if (currentColumn.equalsIgnoreCase("tax_code")) {
					System.out.println("tax_code called::::");
					valueXmlString.append("<tax_descr><![CDATA["+ descr + "]]></tax_descr>");
				}
				System.out.print("*************** Generated XML ******************" + valueXmlString.toString());
				valueXmlString.append("</Detail2>\r\n");
				break;*/
			}

		}catch(Exception e){
			System.out.println("::" + this.getClass().getSimpleName() + "::::" + e.getMessage());
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
		}finally {
			try {
				if (conn != null)
					conn.close();
				conn = null;
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		valueXmlString.append("</Root>\r\n");
		System.out.println("ValueXmlString:::::" + valueXmlString.toString());
		return valueXmlString.toString();

	}
}
