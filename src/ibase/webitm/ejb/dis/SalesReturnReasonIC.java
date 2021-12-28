package ibase.webitm.ejb.dis;


import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Stateless
public class SalesReturnReasonIC extends ValidatorEJB implements SalesReturnReasonICRemote, SalesReturnReasonICLocal {

	
	E12GenericUtility genericUtility =  new E12GenericUtility();
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
			}
			return rtStr;
		}
	  @Override
		public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams)
				throws RemoteException, ITMException {

			String errString = "";
			String sql = "", reasonCode = "", descr = "", restrDays = "";
			Connection conn = null;
			String userId = "";
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			NodeList parentNodeList = null, childNodeList = null;
			Node parentNode = null, childNode = null;
			int ctr = 0, childNodeLength = 0, currentFormNo = 0;
			String childNodeName = "";
			try {
				ConnDriver connDriver = new ConnDriver();
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
				conn.setAutoCommit(false);
				userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, userId);
				if (objContext != null && objContext.trim().length() > 0) {
					currentFormNo = Integer.parseInt(objContext);
				}
				System.out.println("in wfValdata doc :::::");
				switch (currentFormNo) 
				{
				case 1:
					parentNodeList = dom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeLength = childNodeList.getLength();
					for (ctr = 0; ctr < childNodeLength; ctr++) 
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						/* Comment By Nasruddin  21-SEP-16 START
						if(childNodeName.equalsIgnoreCase("reason_code"))
						{
							reasonCode = genericUtility.getColumnValue("reason_code", dom);
							reasonCode = reasonCode == null ? "" : reasonCode.trim();
							System.out.println("reasonCode ::::" + reasonCode);
							if(reasonCode.isEmpty())
							{
								errString = itmdbAccessEJB.getErrorString("reason_code", "VTREABL", userId);
								return errString;
							}
							else if(editFlag.equalsIgnoreCase("A"))
							{
								int count = 0;
								sql = "select count(*) as count from SRETURN_REASON where reason_code = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, reasonCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									count = rs.getInt("count");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(count > 0)
								{
									errString = itmdbAccessEJB.getErrorString("reason_code", "VTREAINV", userId);
									return errString;
								}
							}
						}
						if(childNodeName.equalsIgnoreCase("reason_descr"))
						{
							descr = genericUtility.getColumnValue("reason_descr", dom);
							descr = descr == null ? "" : descr.trim();
							System.out.println("Description :::" + descr);
							if(descr.trim().isEmpty())
							{
								errString = itmdbAccessEJB.getErrorString("reason_descr", "VMDESCR", userId);
								return errString;
							}
						}
						Comment By Nasruddin  21-SEP-16 END */
						if(childNodeName.equalsIgnoreCase("restr_days"))
						{
							restrDays = genericUtility.getColumnValue("restr_days", dom);
							restrDays = restrDays == null ? "" : restrDays.trim();
							System.out.println("Restr Days" + restrDays);
							if(restrDays.trim().isEmpty())
							{
								errString = itmdbAccessEJB.getErrorString("restr_days", "VTRESTBL", userId);
								return errString;
							}
						}

					}
				}
			}catch(Exception e){
				System.out.println("::::" + this.getClass().getSimpleName() + ":::" + e.getMessage());
				e.printStackTrace();
			}finally{
				try{
					if(rs != null){
						rs.close();
						rs = null;
					}
					if(pstmt != null){
						pstmt.close();
						pstmt = null;
					}
					if(conn != null){
						conn.close();
						conn = null;
					}
				}catch(Exception e1){
					System.out.println("::::" + this.getClass().getSimpleName() + ":::" + e1.getMessage());
					e1.printStackTrace();	
				}
			}
			return errString;
			}
	@Override
	public String wfValData(String arg0, String arg1, String arg2, String arg3,
			String arg4, String arg5, String arg6) throws RemoteException,
			ITMException
	{
		// TODO Auto-generated method stub
		return null;
	}
}
