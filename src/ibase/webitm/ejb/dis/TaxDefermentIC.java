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

public class TaxDefermentIC extends ValidatorEJB implements TaxDefermentICRemote, TaxDefermentICLocal {


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
				System.out.println("wfValdata string :::::");
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
			String sql = "", siteCode = "", tranType = "", siteCodeFor = "", taxCode = "";
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
				System.out.println("in wfValdata doc :::::");
				switch (currentFormNo) {
				case 1:
					parentNodeList = dom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeLength = childNodeList.getLength();
					for (ctr = 0; ctr < childNodeLength; ctr++) {
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						if(childNodeName.equalsIgnoreCase("site_code"))
						{
							siteCode = genericUtility.getColumnValue("site_code", dom);
							//siteCode = childNode.getFirstChild().getNodeValue();
							siteCode = siteCode == null ? "" : siteCode.trim();
							System.out.println("site code :::" + siteCode);
							if(siteCode.isEmpty())
							{  
								errString = itmdbAccessEJB.getErrorString("site_code", "VMSITECD1", userId);
								return errString;
							}
							else
							{
								int count = 0;
								sql = "select count(*) as count from site where site_code = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, siteCode);
								rs = pstmt.executeQuery();
								if(rs.next()){
									count = rs.getInt("count");
								}
								if(count == 0)
								{
									errString = itmdbAccessEJB.getErrorString("site_code", "VMSITE1", userId);
									return errString;
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}
						/*Comment By Nasruddin 21-SEp-16 STart
						if(childNodeName.equalsIgnoreCase("tran_type"))
						{
							tranType = genericUtility.getColumnValue("tran_type", dom);
							//tranType = childNode.getFirstChild().getNodeValue();
							tranType = tranType == null ? "" : tranType.trim();
							System.out.println("tran type ::::" + tranType);
							if(tranType.isEmpty()){
								errString = itmdbAccessEJB.getErrorString("tran_type", "VTBLTRATYP", userId); 
								return errString;
							}else{
								int count = 0;
								sql = "select count(*) as count from gencodes where fld_name='PORD_TYPE' and fld_value = ? " ;
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, tranType);
								rs = pstmt.executeQuery();
								if(rs.next()){
									count = rs.getInt("count");
								}
								if(count == 0){
									errString = itmdbAccessEJB.getErrorString("tran_type", "VTINVTRTP", userId);
									return errString;
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
							if(editFlag.equalsIgnoreCase("A")){
								int cnt = 0;
								sql = "select count(*) as count from tax_deferment where site_code = ? and tran_type = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, siteCode);
								pstmt.setString(2, tranType);
								rs =pstmt.executeQuery();
								
								if(rs.next()){
									cnt = rs.getInt("count");
								}
								if(cnt > 0){
									errString = itmdbAccessEJB.getErrorString("tran_type", "VTINVSTTP", userId);
									return errString;
								}
							}
							
						}Comment By Nasruddin 21-SEp-16 END*/
						
						if(childNodeName.equalsIgnoreCase("site_code__for"))
						{
							siteCodeFor = genericUtility.getColumnValue("site_code__for", dom);
							//siteCodeFor = childNode.getFirstChild().getNodeValue();
							siteCodeFor = siteCodeFor == null ? "" : siteCodeFor.trim();
							System.out.println("site code for :::::" + siteCodeFor);
							if(siteCodeFor.isEmpty())
							{
								
								errString = itmdbAccessEJB.getErrorString("site_code__for", "VMSITECD1", userId);
								return errString;
							}
							else
							{
								int count = 0;
								sql = "select count(*) as count from site where site_code = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, siteCodeFor);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									count = rs.getInt("count");
								}
								if(count == 0)
								{
									errString = itmdbAccessEJB.getErrorString("site_code__for", "VMSITE1", userId);
									return errString;
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}
						if(childNodeName.equalsIgnoreCase("tax_code"))
						{
							taxCode = genericUtility.getColumnValue("tax_code", dom);
							//taxCode = childNode.getFirstChild().getNodeValue();
							taxCode = taxCode == null ? "" : taxCode.trim();
							System.out.println("tax code :::" + taxCode);
							if(taxCode.isEmpty())
							{
								errString = itmdbAccessEJB.getErrorString("tax_code", "VTTAXC", userId);
								return errString;
							}
						}
					}
				}
			}catch(Exception e){
				System.out.println(":::" + this.getClass().getSimpleName() + ":::" + e.getMessage());
				e.printStackTrace();
				throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
			}
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
			String siteCode = "", sql = "", siteDescr = "", siteCodeFor = "", siteForDescr = "" , tranType = "", tranDescr = "";
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
				System.out.println("itemchange document ::::");
				switch (currentFormNo) {
				case 1:
					valueXmlString.append("<Detail1>");
					System.out.println("currentColumn: " + currentColumn);
					if (currentColumn != null) {
						if (currentColumn.equalsIgnoreCase("site_code")){
							siteCode = genericUtility.getColumnValue("site_code", dom);
							 System.out.println(":::: siteCode" + siteCode);
							 siteCode = siteCode == null ? "" : siteCode.trim();	
							 try {
								
										sql = "select descr from site where site_code = ? ";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, siteCode);
										rs = pstmt.executeQuery();
									
									if(rs.next()){
										siteDescr = rs.getString("descr");
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								
							} catch (Exception e) {
								System.out.println("::" + this.getClass().getSimpleName() + "::::" + e.getMessage());
								e.printStackTrace();
							}
							 System.out.println("siteCode called::::");
							 valueXmlString.append("<site_descr><![CDATA["+ siteDescr + "]]></site_descr>");
						}
						
						if (currentColumn.equalsIgnoreCase("site_code__for")){
							siteCodeFor = genericUtility.getColumnValue("site_code__for", dom);
							 System.out.println(":::: siteCodeFor" + siteCodeFor);
							 siteCodeFor = siteCodeFor == null ? "" : siteCodeFor.trim();	
							 try {
										sql = "select descr from site where site_code = ? ";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, siteCodeFor);
										rs = pstmt.executeQuery();
									
									if(rs.next()){
										siteForDescr = rs.getString("descr");
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								
							} catch (Exception e) {
								System.out.println("::" + this.getClass().getSimpleName() + "::::" + e.getMessage());
								e.printStackTrace();
							}
							 System.out.println("siteCodeFor called::::");
							 valueXmlString.append("<site_descr__for><![CDATA["+ siteForDescr + "]]></site_descr__for>");
						}
						
						if (currentColumn.equalsIgnoreCase("tran_type")){
							tranType = genericUtility.getColumnValue("tran_type", dom);
							 System.out.println(":::: tranType" + tranType);
							 tranType = tranType == null ? "" : tranType.trim();	
							 try {
								 		sql = "select descr from gencodes where fld_name ='PORD_TYPE' and fld_value = ? ";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, tranType);
										rs = pstmt.executeQuery();
									
									if(rs.next()){
										tranDescr = rs.getString("descr");
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								
							} catch (Exception e) {
								System.out.println("::" + this.getClass().getSimpleName() + "::::" + e.getMessage());
								e.printStackTrace();
							}
							 System.out.println("tranType called::::");
							 valueXmlString.append("<descr><![CDATA["+ tranDescr + "]]></descr>");
						}
						
						if(currentColumn.equalsIgnoreCase("itm_defaultedit")){
							System.out.println(":::In itm_defaultedit :::");
							
							siteCode = genericUtility.getColumnValue("site_code", dom);
							 System.out.println(":::: siteCode edit" + siteCode);
							 siteCode = siteCode == null ? "" : siteCode.trim();
							 
							 tranType = genericUtility.getColumnValue("tran_type", dom);
							 System.out.println("::: tran type " + tranType);
							 tranType = tranType == null ? "" : tranType.trim();
							 
							 
							 System.out.println("itmdefault edit called :::");
							 valueXmlString.append("<site_code  protect = \"1\"><![CDATA["+ siteCode + "]]></site_code>");
							 valueXmlString.append("<tran_type  protect = \"1\"><![CDATA["+ tranType + "]]></tran_type>");
						}
						
						System.out.println(":::::generated xml" + valueXmlString.toString());
						valueXmlString.append("</Detail1>\r\n");
					}
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

