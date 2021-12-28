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

public class PurchaseTermsIC extends ValidatorEJB implements PurchaseTermsICRemote, PurchaseTermsICLocal {

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
				throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
			}
			return rtStr;
		}
	  @Override
		public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams)
				throws RemoteException, ITMException {

		  String errString = "";
		  String sql = "", termCode = "", descr = "";
		  Connection conn = null;
		  String userId = "", keyFlag = "";
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
			  switch (currentFormNo) {
			  case 1:
				  parentNodeList = dom.getElementsByTagName("Detail1");
				  parentNode = parentNodeList.item(0);
				  childNodeList = parentNode.getChildNodes();
				  childNodeLength = childNodeList.getLength();
				  for (ctr = 0; ctr < childNodeLength; ctr++) 
				  {
					  childNode = childNodeList.item(ctr);
					  childNodeName = childNode.getNodeName();
					  if(childNodeName.equalsIgnoreCase("term_code"))
					  {
						  termCode= genericUtility.getColumnValue("term_code", dom);
						  termCode = termCode == null ? "" : termCode.trim();
						  System.out.println("termCode ::::" + termCode);

						  sql = "select key_flag from transetup where tran_window = 'w_pur_term' " ;
						  pstmt = conn.prepareStatement(sql);
						  rs = pstmt.executeQuery();
						  if(rs.next())
						  {
							  keyFlag = rs.getString("key_flag");
						  }

						  keyFlag = keyFlag == null ? "" : keyFlag.trim();

						  System.out.println("keyFlag:::" + keyFlag);
						  if(keyFlag.isEmpty()){
							  keyFlag = "M";
						  }
						  if(termCode.isEmpty() && keyFlag.equalsIgnoreCase("M"))
						  {
							  errString = itmdbAccessEJB.getErrorString("term_code", "VTTERMNULL", userId);
							  return errString;
						  }
						  else if(editFlag.equalsIgnoreCase("A"))
						  {
							  int count = 0;
							  sql = "select count(*) as count from pur_term where term_code = ?";
							  pstmt = conn.prepareStatement(sql);
							  pstmt.setString(1, termCode);
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
								  errString = itmdbAccessEJB.getErrorString("term_code", "VMDUPL1", userId);
								  return errString;
							  }
						  }
					  }
					  /* Comment By Nasruddin [20-SEP-16] START
						if(childNodeName.equalsIgnoreCase("descr"))
						{
							descr = genericUtility.getColumnValue("descr", dom);
							descr = descr == null ? "" : descr.trim();
							System.out.println("Description :::" + descr);
							if(descr.trim().isEmpty()){
								errString = itmdbAccessEJB.getErrorString("descr", "VMDESCR", userId);
								return errString;
							}
						}
						 Comment By Nasruddin [20-SEP-16] END */
				  }
			  }
		  }catch(Exception e)
		  {
			  System.out.println("::::" + this.getClass().getSimpleName() + ":::" + e.getMessage());
			  e.printStackTrace();
			  throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		  }
		  finally
		  {
			  try
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
				  if(conn != null)
				  {
					  conn.close();
					  conn = null;
				  }
			  }
			  catch(Exception e1){
				  System.out.println("::::" + this.getClass().getSimpleName() + ":::" + e1.getMessage());
				  e1.printStackTrace();	
			  }
		  }
		  return errString;
	  }
}
