package ibase.webitm.ejb.dis;

import ibase.utility.E12GenericUtility;

import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList; 

@Stateless
public class TaxVariables extends ValidatorEJB {

	E12GenericUtility genericUtility = new E12GenericUtility();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();

	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag,String xtraParams) 
			throws RemoteException, ITMException {
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try {

			if (xmlString != null && xmlString.trim().length() > 0) {
				dom = parseString(xmlString);
				System.out.println("In wfValData Current xmlString="+ xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0) {
				dom1 = parseString(xmlString1);
				System.out.println("In wfValData Header xmlString1="+ xmlString1);
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0) {
				dom2 = parseString(xmlString2);
				System.out.println("In wfValData All xmlString2=" + xmlString2);
			}
			errString = wfValData(dom, dom1, dom2, objContext, editFlag,xtraParams);
		}
		catch (Exception e) 
		{
			throw new ITMException(e);
		}
		return (errString);
	}

	public String wfValData(Document curDom, Document hdrDom, Document allDom,String objContext, String editFlag, String xtraParams)
			throws RemoteException, ITMException {
		System.out.println("####<<<<<<=======call mfg item wfValData  Method=======>>>>>>>#####");
		System.out.println(" editFlag validation===>>" + editFlag);
		String userId = "",errorType = "", var_name = "",errCode = "",errString = "", prd_code = "";
		String childNodeName = null;
		int currentFormNo = 0;
		int childNodeListLength;
		int ctr = 0;

		NodeList parentNodeList = null, childNodeList = null;
		Node parentNode = null, childNode = null;
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		int count = 0;
		StringBuffer errStringXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root><Errors>");

		try {
			conn = getConnection();
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			//String loginSiteCode = getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
			if (objContext != null && objContext.trim().length() > 0) 
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			System.out.println("Current Form No. :- " + currentFormNo);
			switch (currentFormNo) {
			case 1:
				System.out.println(" editFlag===>>" + editFlag);
				parentNodeList = curDom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();

				for (ctr = 0; ctr < childNodeListLength; ctr++) 
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if ("var_name".equalsIgnoreCase(childNodeName))
					{
						if ((childNode.getFirstChild() == null))
						{
							errList.add("VPBLKVRNM");
							errFields.add(childNodeName.toLowerCase());
							break;
						} 
						else
						{
							prd_code = checkNull(genericUtility.getColumnValue("prd_code", curDom));
							var_name = checkNull(genericUtility.getColumnValue("var_name", curDom));

							if (editFlag.equalsIgnoreCase("A"))
							{
								sql = "select count(*) as COUNT from taxparm where prd_code=? and var_name=?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, prd_code.trim());
								pstmt.setString(2, var_name.trim());
								rs = pstmt.executeQuery();

								if (rs.next()) 
								{
									count = rs.getInt("COUNT");
								}
								if (count > 0)
								{
									errList.add("VPINVVRNM");
									errFields.add(childNodeName.toLowerCase());
									break;
								}
								callPstRs(pstmt, rs);
							}
						}
					}
					if ("prd_code".equalsIgnoreCase(childNodeName)) 
					{
						if ((childNode.getFirstChild() == null)) 
						{
							errList.add("VPBLKPRCD");
							errFields.add(childNodeName.toLowerCase());
							break;
						}
					}
					if ("var_type".equalsIgnoreCase(childNodeName)) 
					{
						if ((childNode.getFirstChild() == null)) 
						{
							errList.add("VPBLKVRTP");
							errFields.add(childNodeName.toLowerCase());
							break;
						}
					}
				}
				break;
			}
			int errListSize = errList.size();
			int cnt = 0;
			String errFldName = null;
			if (errList != null && errListSize > 0) {
				for (cnt = 0; cnt < errListSize; cnt++) {
					errCode = (String) errList.get(cnt);
					errFldName = (String) errFields.get(cnt);
					System.out.println("errCode .........." + errCode);
					errString = getErrorString(errFldName, errCode, userId);
					errorType = errorType(conn, errCode);
					if (errString.length() > 0) {
						String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8,errString.indexOf("<trace>"));
						bifurErrString = bifurErrString+ errString.substring(errString.indexOf("</trace>") + 8,errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						System.out.println("errStringXml .........."+ errStringXml);
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
				errStringXml.append("</Errors></Root>\r\n");
			} else {
				errStringXml = new StringBuffer("");
			}
		}// end try
		catch (Exception e) {
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

	public void callPstRs(PreparedStatement pstmt, ResultSet rs) {
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

	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn,String editFlag, String xtraParams) 
			throws RemoteException,ITMException {
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
			System.out.println("xmlString1=" + xmlString);
			System.out.println("xmlString2=" + xmlString1);
			System.out.println("xmlString3=" + xmlString2);
			valueXmlString = itemChanged(dom, dom1, dom2, objContext,currentColumn, editFlag, xtraParams);
		} catch (Exception e)
		{
			System.out.println("Exception : [DistributionRoute][itemChanged( String, String )] :==>\n"+ e.getMessage());
		}
		return valueXmlString;
	}

	public String itemChanged(Document dom, Document dom1, Document dom2,String objContext, String currentColumn, String editFlag,String xtraParams)
			throws RemoteException, ITMException {
		System.out.println("####<<<<<<=======call item itemChanged Method=======>>>>>>>#####");
		System.out.println(" editFlag itemchanged===>>" + editFlag);
		StringBuffer valueXmlString = new StringBuffer();
		int currentFormNo = 0;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = getConnection();
			conn.setAutoCommit(false);
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");
			if (objContext != null && objContext.trim().length() > 0) 
			{
				currentFormNo = Integer.parseInt(objContext);
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			throw new ITMException(e);
		} 
		finally {
			try {
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
		System.out.println("ValueXmlString:::::" + valueXmlString.toString());
		return valueXmlString.toString();
	}

	private String checkNull(String input) {
		if (input == null) 
		{
			input = "";
		}
		return input;
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
			while (rs.next()) {
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

}
