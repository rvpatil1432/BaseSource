/* Developed By Pavan Rane.
 * Date : 12-JUN-2019
 * Component Name : SalesReturnVerifyPasswordPrc
 */
package ibase.webitm.ejb.dis;

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ProcessEJB;
import ibase.webitm.ejb.dis.adv.PoRcpConf;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.ejb.Stateless;
import org.w3c.dom.Document;

/**
 * Session Bean implementation class EmpMaritalStatusConfPrc
 */
@Stateless

public class PoReceiptVerifyPasswordPrc extends ProcessEJB implements PoReceiptVerifyPasswordPrcRemote, PoReceiptVerifyPasswordPrcLocal 
{
	E12GenericUtility genericUtility = new E12GenericUtility();
	
	public String process() throws RemoteException, ITMException 
	{
		return "";
	}

	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException, ITMException 
	{		
		String retString = "";
		Document detailDom = null;
		Document headerDom = null;
		try {
			if (xmlString != null && xmlString.trim().length() != 0) 
			{
				headerDom = genericUtility.parseString(xmlString);
				System.out.println("headerDom" + headerDom);
			}
			if (xmlString2 != null && xmlString2.trim().length() != 0) 
			{
				detailDom = genericUtility.parseString(xmlString2);
				System.out.println("detailDom" + detailDom);
			}
			retString = process(headerDom, detailDom, windowName, xtraParams);
		} catch (Exception e) 
		{
			System.out.println("Exception :SalesReturnVerifyPasswordPrc :process(String):"+ e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return retString;
	}

	public String process(Document headerDom, Document detailDom, String windowName, String xtraParams)	throws RemoteException, ITMException {
		System.out.println("-----------Calling PoReceiptVerifyPasswordPrc----process(Document)----------");
		String returnString = "", tranId = "", sql = "", lsPasswd = "", lsConfirm = "", lsEntPasswd = "";
		String lsPwdVerified = "N";
		boolean isError = false;
		Connection conn = null;
		ITMDBAccessEJB itmDBAccess = null;
		PreparedStatement pstmt = null;		
		ResultSet rs = null;		
		try 
		{
			conn = getConnection();
			conn.setAutoCommit(false);
			itmDBAccess = new ITMDBAccessEJB();			
			tranId = genericUtility.getColumnValue("tran_id", detailDom);
			System.out.println("PO Receipt No: # "+tranId+"]");
			if (tranId == null || tranId.trim().length() <= 0) 
			{
				returnString = itmDBAccess.getErrorString("", "VTTRNNULL", "", "", conn);
				return returnString;
			} else 
			{
				sql = "select conf_passwd, confirmed from porcp where tran_id = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					lsPasswd = checkNull(rs.getString("conf_passwd"));
					lsConfirm = checkNull(rs.getString("confirmed"));					
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;

				System.out.println("@V@ PAVAN lsPasswd :- [" + lsPasswd + "]");
				System.out.println("@V@ PAVAN lsConfirm :- [" + lsConfirm + "]");
				if ("Y".equalsIgnoreCase(lsConfirm)) 
				{
					returnString = itmDBAccess.getErrorString("", "VTMCONF1", "", "", conn);
					return returnString;
				} else 
				{
					if (lsPasswd != null && lsPasswd.trim().length() > 0) 
					{
						lsEntPasswd = checkNull(genericUtility.getColumnValue("conf_passwd", detailDom));
						System.out.println("@V@ PAVAN lsEntPasswd :- [" + lsEntPasswd + "]");
						if (lsEntPasswd == null || lsEntPasswd.trim().length() <= 0 || !(lsEntPasswd.equals(lsPasswd))) 
						{
							returnString = itmDBAccess.getErrorString("", "VPINVDPSWD", "", "", conn);
							return returnString;
						} else 
						{
							if(lsEntPasswd.equals(lsPasswd))
							{
								lsPwdVerified = "Y";
							}
						}
					} /*else {
						lsPwdVerified = "Y";
					}*/
					if ("Y".equalsIgnoreCase(lsPwdVerified)) 
					{
						// conn.commit();												
						PoRcpConf poRcpConf = new PoRcpConf();
						returnString = poRcpConf.confirm(tranId, xtraParams, "", conn, false);							
						sql = "select confirmed from porcp where tran_id = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, tranId);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							lsConfirm  = checkNull(rs.getString("confirmed"));
						}
						rs.close();	rs = null;
						pstmt.close();pstmt = null;						
						
						if(!"Y".equalsIgnoreCase(lsConfirm))
						{
							conn.rollback();
							if(returnString == null  || returnString.trim().length() == 0)
							{
								returnString = itmDBAccess.getErrorString("", "DS000", "","",conn);
								isError = true;
							}
						}
						else
						{
							conn.commit();
							returnString = itmDBAccess.getErrorString("", "VTMCONF2", "","",conn);
						}							
					}
				} // }
			} // TRAN_ID NOT NULL

			if (returnString.length() > 0 && returnString.indexOf("VTCONF") > -1) {
				returnString = itmDBAccess.getErrorString("", "PRCSUCES", "", "", conn);
			} else {
				isError = true;
				System.out.println("PoReceiptVerifyPasswordPrc isError ::[" + isError + "]");
				// returnString = itmDBAccess.getErrorString("", "PRCFAIL", "", "", connection);
			}
		} catch (Exception e) {
			isError = true;
			System.out.println("Exception :PoReceiptVerifyPasswordPrc :process(Documents):"+ e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		} finally {
			System.out.println("Finally Closing Connection....");
			try {
				if (isError) {
					conn.rollback();
					System.out.println("PoReceiptVerifyPasswordPrc connection rollback");
				} else {
					conn.commit();
					System.out.println("PoReceiptVerifyPasswordPrc connection committed");
				}
				if (conn != null) {
					conn.close();
					conn = null;
				}
			} catch (Exception e) {
				returnString = e.getMessage();
				e.printStackTrace();
			}
		}
		System.out.println("errString...:: " + returnString);
		return returnString;
	}

	private String checkNull(String input) {
		if (input == null || ("null").equalsIgnoreCase(input.trim())) {
			input = "";
		} else {
			input = input.trim();
		}
		return input;
	}
}
