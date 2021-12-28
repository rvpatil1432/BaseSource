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

public class InsuranceAgentIC extends ValidatorEJB implements InsuranceAgentICRemote, InsuranceAgentICLocal {

    // comment by nasruddin 07-10-16
	// GenericUtility genericUtility = GenericUtility.getInstance();
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
				if (objContext != null && objContext.trim().length() > 0)
				{
					currentFormNo = Integer.parseInt(objContext);
				}
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
						if (childNodeName.equalsIgnoreCase("agent_code")) 
						{
							System.out.println(":::childNodeName" + childNodeName);
							String agentCode = genericUtility.getColumnValue("agent_code", dom);

							agentCode = agentCode == null ? "" : agentCode.trim();
							System.out.println("::: agentCode" + agentCode);
							if(agentCode.length() <= 0)
							{
								errString = itmdbAccessEJB.getErrorString("agent_code", "VTAGENTB", userId);
								break;
							}
							else
							{
								if(editFlag.equalsIgnoreCase("A"))
								{
									int count = 0;
									sql = "select count(*) as count from ins_agent where agent_code = ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, agentCode);
									rs = pstmt.executeQuery();

									if(rs.next())
									{
										count = rs.getInt("count");
									}
									// Changed By Nasruddin khan [16-SEP-16] START
									pstmt.close();
									pstmt = null;
									rs.close();
									rs = null;
									// Changed By Nasruddin khan [16-SEP-16] END
									if(count > 0){
										errString = itmdbAccessEJB.getErrorString("agent_code", "VDIAC", userId);
										break;
									}	
								}

							}
						}
						if (childNodeName.equalsIgnoreCase("count_code"))
						{
							System.out.println(":::childNodeName" + childNodeName);
							String countCode = genericUtility.getColumnValue("count_code", dom);
							/* Changed  By Nasruddin [16-SEP-16] START
							countCode = countCode == null ? "" : countCode.trim();
							System.out.println("::: countCode" + countCode);
							if(countCode.length() <= 0)
							{
								errString = itmdbAccessEJB.getErrorString("count_code", "VTCOUNTB", userId);
								break;
							}
							else  Changed  By Nasruddin [16-SEP-16] END*/
							if( countCode != null && countCode.trim().length() > 0)
							{
								int count = 0;
								sql = "select count(*) as count from country where count_code = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, countCode);
								rs = pstmt.executeQuery();

								if(rs.next()){
									count = rs.getInt("count");
								}
								if(count == 0)
								{
									errString = itmdbAccessEJB.getErrorString("count_code", "VTCONTCD1", userId);
									break;
								}	
							}	
						}
						if (childNodeName.equalsIgnoreCase("stan_code")) 
						{
							System.out.println(":::childNodeName" + childNodeName);
							String stanCode = genericUtility.getColumnValue("stan_code", dom);
							/* Changed By Nasruddin [16/SEP/16] START
							stanCode = stanCode == null ? "" : stanCode.trim();
							System.out.println("::: stanCode" + stanCode);
							if(stanCode.length() <= 0)
							{
								errString = itmdbAccessEJB.getErrorString("stan_code", "VTSTATB", userId);
								break;
							}
							else
							{
								int count = 0;
								sql = "select count(*) as count from station where stan_code = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, stanCode);
								rs = pstmt.executeQuery();

								if(rs.next()){
									count = rs.getInt("count");
								}
								if(count <= 0){
									errString = itmdbAccessEJB.getErrorString("stan_code", "VMSTANINVD", userId);
									break;
								}	
							}	
							 //Changed By Nasruddin [16/SEP/16] END */
							int count = 0;
							sql = "select count(*) as count from station where stan_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, stanCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count = rs.getInt("count");
							}
							if( stanCode != null && stanCode.trim().length() > 0)
							{
								if(count == 0)
								{
									errString = itmdbAccessEJB.getErrorString("stan_code", "VTSTAN1", userId);
									//errString = itmdbAccessEJB.getErrorString("stan_code", "VMSTANINVD", userId);
									break;
								}	
							}

						}
						if (childNodeName.equalsIgnoreCase("supp_code__ins")) 
						{
							System.out.println(":::childNodeName" + childNodeName);
							String suppCode = genericUtility.getColumnValue("supp_code__ins", dom);

							//Changed By Nasruddin [16-SEP-16] 
							if(suppCode != null && suppCode.trim().length() > 0)
							{
								int count = 0;
								sql = "select count(*) as count from insurance where supp_code__ins = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, suppCode);
								rs = pstmt.executeQuery();

								if(rs.next())
								{
									count = rs.getInt("count");
								}
								if(count <= 0)
								{
									errString = itmdbAccessEJB.getErrorString("supp_code__ins", "VTNINSSUPP", userId);
									break;
								}
							}
							/*else{
										GenValidate gV = new GenValidate();
										String result = gV.generalValidate(dom, "supp_code__ins", "w_insurance_agent");
										if(result.trim().length() <= 0 && result == ""){
											continue;
										}
									else{
										errString = itmdbAccessEJB.getErrorString("supp_code__ins", "VTNINSSUPP", userId);
									}
									}*/
						}	
					}
				}
					
			}catch (Exception e)
			{
				System.out.println(":::" + this.getClass().getSimpleName() + ":::" + e.getMessage());
				e.printStackTrace();
			}
			return errString;
	  }
	  public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn, String editFlag, 
				String xtraParams) throws RemoteException, ITMException {
			Document dom = null;
			Document dom1 = null;
			Document dom2 = null;
			String rtStr = "";
			//comment by nasruddin 07-10-16
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
			}
			return rtStr;
		}
	  public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag,
				String xtraParams) throws RemoteException, ITMException {
			
			Connection conn = null;
			String countCode = "", sql = "", countryDescr = "", stanCode = "", stationDescr = "";
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
						if (currentColumn.equalsIgnoreCase("count_code")){
							countCode = genericUtility.getColumnValue("count_code", dom);
							 System.out.println(":::: countCode" + countCode);
							 countCode = countCode == null ? "" : countCode.trim();	
							 try {
								 if(countCode.length() > 0){
										sql = "select descr from country where count_code = ? ";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, countCode);
										rs = pstmt.executeQuery();
									
									if(rs.next()){
										countryDescr = rs.getString("descr");
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
							 System.out.println("countCode called::::");
							 valueXmlString.append("<country_descr><![CDATA["+ countryDescr + "]]></country_descr>");
						}
						if (currentColumn.equalsIgnoreCase("stan_code")){
							stanCode = genericUtility.getColumnValue("stan_code", dom);
							 System.out.println(":::: countCode" + stanCode);
							 stanCode = stanCode == null ? "" : stanCode.trim();	
							 try {
								 if(stanCode.length() > 0){
										sql = "select descr from station where stan_code = ? ";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, stanCode);
										rs = pstmt.executeQuery();
									
									if(rs.next()){
										stationDescr = rs.getString("descr");
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
							 System.out.println("stanCode called::::");
							 valueXmlString.append("<station_descr><![CDATA["+ stationDescr + "]]></station_descr>");
						}
						System.out.println(":::::generated xml" + valueXmlString.toString());
						valueXmlString.append("</Detail1>\r\n");
                   }
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
