package ibase.webitm.ejb.dis;

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.XML2DBEJB;
import ibase.webitm.utility.GenerateXmlFromDB;
import ibase.webitm.utility.ITMException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GenericWorkflowClass {
	public String invokeWorkflow(Connection conn, String tranId, String xtraParams, String winName, String objName) throws ITMException {
		E12GenericUtility genericUtility = new E12GenericUtility();
		Document domAll = null;
		NodeList nodeList = null;
		Node node = null;
		Element nodeElement = null;
		String sql = "";
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		String retString = "";
		String wrkflwInit = "";
		String refSer = "";
		String nodeName = "";
		try {
			XML2DBEJB xml2dbObj = new XML2DBEJB();
			GenerateXmlFromDB generateXmlFromDB = new GenerateXmlFromDB();
			String retXml = generateXmlFromDB.getXMLData(winName, tranId, conn, true, true);
			retXml = retXml.replace("<Root>", "");
			retXml = retXml.replace("</Root>", "");
			if ((retXml != null) && (retXml.trim().length() > 0)) {
				domAll = genericUtility.parseString(retXml);
			}
			nodeList = domAll.getElementsByTagName("Detail1");
			node = nodeList.item(0);
			if (node != null) {
				objName = node.getAttributes().getNamedItem("objName").getNodeValue();
				nodeList = node.getChildNodes();
				int nodeListLength = nodeList.getLength();
				for (int i = 0; i < nodeListLength; i++) {
					node = nodeList.item(i);
					if (node != null) {
						nodeName = node.getNodeName();
					}

					if ("wf_status".equalsIgnoreCase(nodeName)) {
						if (node.getFirstChild() != null) {
							node.getFirstChild().setNodeValue("S");
						} else {
							nodeElement = (Element) node;
							nodeElement.appendChild(domAll.createCDATASection("S"));
						}
					}
				}
			}
			nodeList = domAll.getElementsByTagName("DocumentRoot");
			node = nodeList.item(0);

			sql = "SELECT WRKFLW_INIT,REF_SER FROM TRANSETUP WHERE TRAN_WINDOW = 'w_" + objName + "'";
			pStmt = conn.prepareStatement(sql);
			rs = pStmt.executeQuery();
			if (rs.next()) {
				wrkflwInit = rs.getString("WRKFLW_INIT") == null ? "" : rs.getString("WRKFLW_INIT");
				refSer = rs.getString("REF_SER") == null ? "" : rs.getString("REF_SER");
			}
			if (rs != null) {
				rs.close();
				rs = null;
			}
			if (pStmt != null) {
				pStmt.close();
				pStmt = null;
			}

			String entityCodeInit = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			retString = xml2dbObj.invokeWorkflowExternal(domAll, entityCodeInit, wrkflwInit, objName, refSer, tranId, conn);
			System.out.println("retString From xml2dbObj.invokeWorkflowExternal --->" + retString);
		} catch (Exception ex) {
			ex.printStackTrace();
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pStmt != null) {
					pStmt.close();
					pStmt = null;
				}
			} catch (SQLException sqlEx) {
				System.out.println("Exception in Finally " + sqlEx.getMessage());
				sqlEx.printStackTrace();
			}
			throw new ITMException(ex); //Added By Mukesh Chauhan on 07/08/19
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
			} catch (SQLException sqlEx) {
				System.out.println("Exception in Finally " + sqlEx.getMessage());
				sqlEx.printStackTrace();
			}
		}
		return "success";
	}
}