package ibase.webitm.ejb.dis;


import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Stateless
public class ProjectAmdIC extends ValidatorEJB implements ProjectAmdICRemote, ProjectAmdICLocal {

	E12GenericUtility genericUtility= new  E12GenericUtility();
	public ProjectAmdIC() {
	}

	public String wfValData() throws RemoteException, ITMException {
		return "";
	}

	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException {
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String errString = "";
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
			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
		} catch (Exception e) {
			System.out.println("Exception: "+ this.getClass().getSimpleName() +" : wfValData(String) : " + e.getMessage());
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return errString;
	}

	@Override
	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException {
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String errString = "";
		String userId = null,getDate= "";
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		String childNodeName = null;
		int ctr = 0, childNodeLength = 0, currentFormNo = 0;
		Connection conn = null;
		Date startDate = null;
		
		try {
			ConnDriver con = new ConnDriver();
			//Changes and Commented By Bhushan on 13-06-2016 :START
			  //conn = con.getConnectDB("DriverITM");
			  conn = getConnection();
			  //Changes and Commented By Bhushan on 13-06-2016 :END
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginsitecode");
			//GenericUtility genericUtility = GenericUtility.getInstance();
		    DateFormat dateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat(),Locale.ENGLISH);

			String projCode = genericUtility.getColumnValue("proj_code", dom);
			getDate = " select start_date from project where proj_code=?";
			pstmt = conn.prepareStatement(getDate);
			pstmt.setString(1, projCode);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				startDate = rs.getDate("start_date");

			}
			callPstRs(pstmt, rs);
			
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
					if (childNodeName.equalsIgnoreCase("end_date")) {
						
						if (childNode.getFirstChild() == null) {
							errString = getErrorString("end_date", "POAMNDED", userId);
							break;

						}else
						{
							String asOnDateStr=  genericUtility.getColumnValue("end_date", dom);
							java.util.Date asOnDate  =dateFormat.parse(asOnDateStr);
							if(startDate.after(asOnDate))
						
							{
								System.out.println("Error Msg");
								errString = getErrorString("end_date", "VTPRJAMD4", userId);
								break;

							}
							
						}
					
					}
					else if (childNodeName.equalsIgnoreCase("ext_end_date")) {
						
						

						if (childNode.getFirstChild() == null) {
							errString = getErrorString("ext_end_date", "VTPRJAMD2", userId);
							break;
						}else
						{
							String extEndDateStr =  genericUtility.getColumnValue("ext_end_date", dom);
							java.util.Date extEndDate  =dateFormat.parse(extEndDateStr);
							if(startDate.after(extEndDate))
							{
								System.out.println("Error Msg");
								errString = getErrorString("ext_end_date", "VTPRJAMD3", userId);
								break;
							}
						}
					
					}
					else if (childNodeName.equalsIgnoreCase("amd_reason")) {
						String amdReason = genericUtility.getColumnValue("amd_reason", dom);
						System.out.println("amdReason::"+amdReason);
						if (childNode.getFirstChild() == null || amdReason.trim().length() <=0) {
							errString = getErrorString("amd_reason", "VTPRJAMD1", userId);
							break;
						}
					}
					else if (childNodeName.equalsIgnoreCase("amd_date")) {
						if (childNode.getFirstChild() == null) {
							errString = getErrorString("amd_date", "POAMNDEE", userId);
							break;
						}
					}
					else if (childNodeName.equalsIgnoreCase("proj_code")) {
						if (childNode.getFirstChild() == null || projCode.trim().length() <=0) {
							errString = getErrorString("proj_code", "POAMNDEF", userId);
							break;
						}
						else{
							int count = 0;
							String pcode = genericUtility.getColumnValue(
									"proj_code", dom);

							System.out.println("proj_code invalid");

							String sql = " select count(1) as count from project where proj_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, pcode);
							rs = pstmt.executeQuery();

							if (rs.next()) {
								count = rs.getInt("count");
							}

							if (rs != null) {
								rs.close();
								rs = null;
							}
							if (pstmt != null) {
								pstmt.close();
								pstmt = null;
							}

							if (count < 1) {

								System.out.println("proj_code error message");

								errString = getErrorString("proj_code",
										"POAMNDEH", userId);
								break;
							}
						}
						}
					}
				break;
				}
				
			}
		 catch (Exception e) {
			//System.out.println("Exception" + e.getMessage());
			e.printStackTrace();
			errString = e.getMessage();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
				conn = null;
			} catch (Exception d) {
				d.printStackTrace();
			}

		}
		System.out.println("ErrorString" + errString);
		return errString;
	}

	public String itemChanged() throws RemoteException, ITMException {
		return "";
	}

	@Override
	public String itemChanged(String xmlString, String xmlString1,
			String xmlString2, String objContext, String currentColumn,
			String editFlag, String xtraParams) throws RemoteException,
			ITMException {
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String ValuexmlString = "";
		//GenericUtility genericUtility = GenericUtility.getInstance();
		System.out.println("itemChanged ProjAmd String");

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
			ValuexmlString = itemChanged(dom, dom1, dom2, objContext,
					currentColumn, editFlag, xtraParams);
		} catch (Exception e) {
			System.out
					.println("Exception:ProjAmdIC:[itemChanged(String,String)]::==>\n"
							+ e.getMessage());
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return ValuexmlString;
	}

	@Override
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException {
		System.out.println("itemChanged ProjAmd Doc");
		Connection conn = null;
		PreparedStatement pst = null;
		SimpleDateFormat sdf = null;
		String currentDate = "";
		//String N= "";
		int currentFormNo = 0;
		ResultSet rs = null;
		String projcode = "";
		String sql = "";
		String query = "";
		String userId = "";
		//GenericUtility genericUtility = null;
		StringBuffer valueXmlString;
		System.out.println("dom2==>" + dom2);
		try {
			//genericUtility = new GenericUtility();
			valueXmlString = new StringBuffer();
			sdf = new SimpleDateFormat(genericUtility.getDispDateFormat());
			Date date = Calendar.getInstance().getTime();
			currentDate = sdf.format(date);
			ConnDriver connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			String loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			String loginCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			String siteCode = "",projType = "";
			
			System.out.println("loginsitecode.....=" + loginSiteCode);
			if (objContext != null && objContext.trim().length() > 0) {
				currentFormNo = Integer.parseInt(objContext);
			}
			switch (currentFormNo) {
			case 1:
				valueXmlString.append("<Detail1>");
				System.out.println("currentColumn: " + currentColumn);
				if (currentColumn != null) {
					if (currentColumn.equalsIgnoreCase("proj_code")) {
						System.out.println("In itemChange proj_code");
						String projCode = "";
						projCode = genericUtility.getColumnValue("proj_code", dom2);
						String projDescr = "";
						Date projendDate = null,extEnddate = null,startDate= null;
						String projapproxCost = "";
						
						try {
							sql = " select descr, end_date, approx_cost, site_code,ext_end_date ,proj_type from project where proj_code=?";
							pst = conn.prepareStatement(sql);
							pst.setString(1, projCode);
							rs = pst.executeQuery();
							if (rs.next()) {
								projDescr = rs.getString("descr")==null?"":rs.getString("descr");
								projendDate = rs.getDate("end_date");
								projapproxCost = rs.getString("approx_cost")==null?"0": rs.getString("approx_cost");
								siteCode = rs.getString("site_code")==null?"": rs.getString("site_code");
								extEnddate = rs.getDate("ext_end_date");
								projType = rs.getString("proj_type")==null?"":rs.getString("proj_type");
							}
							callPstRs(pst, rs);
								valueXmlString.append("<proj_type>").append("<![CDATA[" + projType + "]]>").append("</proj_type>");

								valueXmlString.append("<descr>").append("<![CDATA[" + projDescr + "]]>").append("</descr>");
								if(projendDate != null) {
									valueXmlString.append("<end_date_o>").append("<![CDATA[" + sdf.format(projendDate) + "]]>").append("</end_date_o>");
									valueXmlString.append("<end_date>").append("<![CDATA[" + sdf.format(projendDate) + "]]>").append("</end_date>");

								}
								if(extEnddate != null) {
									valueXmlString.append("<ext_end_date_o>").append("<![CDATA[" + sdf.format(extEnddate) + "]]>").append("</ext_end_date_o>");
									valueXmlString.append("<ext_end_date>").append("<![CDATA[" + sdf.format(extEnddate) + "]]>").append("</ext_end_date>");

								}
										
								valueXmlString.append("<approx_cost_o>").append("<![CDATA[" + projapproxCost + "]]>").append("</approx_cost_o>");
								valueXmlString.append("<approx_cost>").append("<![CDATA[" + projapproxCost + "]]>").append("</approx_cost>");

								if(siteCode.length() > 0){
									valueXmlString.append("<site_code>").append("<![CDATA[" +  siteCode + "]]>").append("</site_code>");
								} else {
									valueXmlString.append("<site_code>").append("<![CDATA[" +  loginSiteCode + "]]>").append("</site_code>");
								}
						
						} catch (Exception e) {
									System.out.println("		if (rs.next()) {");
									e.printStackTrace();
						}
						
					}
					if (currentColumn.equalsIgnoreCase("itm_default")) {
						System.out.println("Azhar ::: ITMDEFAULT Called ::: Tajuddin");
//						valueXmlString.append("<appr_by>").append("<![CDATA[" + loginCode + "]]>").append("</appr_by>");
						valueXmlString.append("<amd_date>").append("<![CDATA[" + currentDate + "]]>").append("</amd_date>");
						//valueXmlString.append("<end_date>").append("<![CDATA[" + currentDate + "]]>").append("</end_date>");
//						valueXmlString.append("<appr_date>").append("<![CDATA[" + currentDate + "]]>").append("</appr_date>");
						valueXmlString.append("<site_code>").append("<![CDATA[" + loginSiteCode + "]]>").append("</site_code>");
						valueXmlString.append("<confirmed><![CDATA[N]]></confirmed>");
					}

					System.out.print("*************** Generated XML ******************" + valueXmlString.toString());
					valueXmlString.append("</Detail1>\r\n");

					break;

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			throw new ITMException(e);
		} finally {
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

}
