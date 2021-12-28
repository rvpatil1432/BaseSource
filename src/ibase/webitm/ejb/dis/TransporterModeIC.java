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

public class TransporterModeIC extends ValidatorEJB implements TransporterModeICRemote, TransporterModeICLocal {

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
			}
			return rtStr;
		}
	  @Override
		public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams)
				throws RemoteException, ITMException {

		  String errString = "";
		  String sql = "", tranCode = "", tranType = "", transMode = "";
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
				  for (ctr = 0; ctr < childNodeLength; ctr++)
				  {
					  childNode = childNodeList.item(ctr);
					  childNodeName = childNode.getNodeName();
					  if(childNodeName.equalsIgnoreCase("tran_code"))
					  {
						  tranCode = genericUtility.getColumnValue("tran_code", dom);
						  tranCode = tranCode == null ? "" : tranCode.trim();
						  System.out.println("transporter code ::::" + tranCode);
						  if(tranCode.isEmpty())
						  {
							  errString = itmdbAccessEJB.getErrorString("tran_code", "VMCODNULL", userId);
							  return errString;
						  }
						  else
						  {
							  int count = 0;
							  sql = "select count(*) as count from transporter where tran_code = ?";
							  pstmt = conn.prepareStatement(sql);
							  pstmt.setString(1, tranCode);
							  rs = pstmt.executeQuery();
							  if(rs.next())
							  {
								  count = rs.getInt("count");
							  }
							  rs.close();
							  rs = null;
							  pstmt.close();
							  pstmt = null;
							  if(count == 0)
							  {
								  errString = itmdbAccessEJB.getErrorString("tran_code", "VTTRANCD1", userId);
								  return errString;
							  }
						  }	
					  }
					  if(childNodeName.equalsIgnoreCase("trans_mode"))
					  {
						  transMode = genericUtility.getColumnValue("trans_mode", dom);
						  transMode = transMode == null ? "" : transMode.trim();
						  System.out.println("mode of transport :::" + transMode);
						  if(transMode.isEmpty())
						  {
							  errString = itmdbAccessEJB.getErrorString("trans_mode", "VMCODNULL", userId);
							  return errString;
						  }
						  else
						  {
							  int count = 0;
							  //Changed By Nasruddin 21-sep-16 start
							  if("A".equals(editFlag))
							  {

								  sql = "select count(*) as count from transporter_mode where tran_code = ? and trans_mode = ?";
								  pstmt = conn.prepareStatement(sql);
								  pstmt.setString(1, tranCode);
								  pstmt.setString(2, transMode);
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
									  errString = itmdbAccessEJB.getErrorString("", "VMDUPL1", userId);
									  return errString;
								  }

							  }
						  }
					  }
				  }
			  }
		  }
		  catch(Exception e)
		  {
			  System.out.println("::::" + this.getClass().getSimpleName() + ":::" + e.getMessage());
			  e.printStackTrace();
		  }
		  // Changed by Nasruddin 21-sep-16 Start
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
			}
		  // Changed by Nasruddin 21-sep-16 End
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
			}
			return rtStr;
		}
	  public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag,
				String xtraParams) throws RemoteException, ITMException {
			
			Connection conn = null;
			String sql = "", trCode = "", transName = "";
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
						if(currentColumn.equalsIgnoreCase("tran_code")){
							trCode = genericUtility.getColumnValue("tran_code", dom);
							trCode = trCode == null ? "" : trCode.trim();
							System.out.println(":::tran code ::" + trCode);
							
							try {
								
								sql = "select tran_name from transporter where tran_code = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, trCode);
								rs = pstmt.executeQuery();
							
							if(rs.next()){
								transName = rs.getString("tran_name");
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
					 valueXmlString.append("<tran_name><![CDATA["+ transName + "]]></tran_name>");
						}
						
						/*if(currentColumn.equalsIgnoreCase("itm_default")){
							System.out.println("itm_default called::::");
							valueXmlString.append("<trans_mode><![CDATA[R]]></trans_mode>");						
							}*/
					}
					System.out.println(":::::generated xml" + valueXmlString.toString());
					valueXmlString.append("</Detail1>\r\n");
				}
			}catch(Exception e){
				System.out.println("::" + this.getClass().getSimpleName() + "::::" + e.getMessage());
				e.printStackTrace();
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
