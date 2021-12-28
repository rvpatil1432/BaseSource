package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.ejb.Stateless;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Stateless
public class LocationMaster extends ValidatorEJB implements
		LocationMasterLocal, LocationMasterrRemote {
	//changed by nasruddin 07-10-16
	E12GenericUtility genericUtility = new E12GenericUtility();
	//GenericUtility genericUtility = GenericUtility.getInstance();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();

	public String wfValData(String xmlString, String xmlString1,
			String xmlString2, String objContext, String editFlag,
			String xtraParams) throws RemoteException, ITMException {
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try {

			if (xmlString != null && xmlString.trim().length() > 0) {
				dom = parseString(xmlString);
				System.out.println("In wfValData Current xmlString="
						+ xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0) {
				dom1 = parseString(xmlString1);
				System.out.println("In wfValData Header xmlString1="
						+ xmlString1);
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0) {
				dom2 = parseString(xmlString2);
				System.out.println("In wfValData All xmlString2=" + xmlString2);
			}
			errString = wfValData(dom, dom1, dom2, objContext, editFlag,
					xtraParams);
		} catch (Exception e) {
			throw new ITMException(e);
		}
		return (errString);
	}

	public String wfValData(Document curDom, Document hdrDom, Document allDom,
			String objContext, String editFlag, String xtraParams)
					throws RemoteException, ITMException 
					{
		System.out.println("####<<<<<<=======call mfg item wfValData  Method=======>>>>>>>#####");
		System.out.println(" editFlag validation===>>" + editFlag);
		String userId = "";
		String childNodeName = null;
		String errorType = "", loc_code = "", item_code = "", site_code__own = "";
		String errCode = "", key_flag = "", site_code = "", inv_stat = "";
		String errString = "", facility_code = "", valid_upto = "", eff_from = "";
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		int currentFormNo = 0;
		int childNodeListLength;
		int ctr = 0;

		boolean flag = false;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;

		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ConnDriver connDriver = new ConnDriver();
		String sql = "";
		int count = 0;

		StringBuffer errStringXml = new StringBuffer(
				"<?xml version=\"1.0\"?>\r\n<Root><Errors>");

		try {
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			String loginSiteCode = getValueFromXTRA_PARAMS(xtraParams,
					"loginSiteCode");
			if (objContext != null && objContext.trim().length() > 0) {
				currentFormNo = Integer.parseInt(objContext);
			}

			System.out.println("Current Form No. :- " + currentFormNo);

			switch (currentFormNo)
			{
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
					if ("loc_code".equalsIgnoreCase(childNodeName)) 
					{
						//if ((childNode.getFirstChild() == null)) {

						sql = "select key_flag from transetup where tran_window = 'w_location'";

						loc_code = checkNull(genericUtility.getColumnValue("loc_code", curDom));
					     // Comment By Nasruddin [20-SEP-16] 
						//	facility_code = checkNull(genericUtility.getColumnValue("facility_code", curDom));
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							key_flag = rs.getString("key_flag");
						}
						//Changed By Nasruddin [20-sep-16] 
						key_flag = key_flag == null?"M":key_flag.trim();
						if (((key_flag.isEmpty()) || key_flag.equalsIgnoreCase("M")) && (loc_code.isEmpty())) 
						{
							errList.add("VMCODNULL");
							errFields.add(childNodeName.toLowerCase());
							break;
						} 
						else if (key_flag.equalsIgnoreCase("A")) 
						{
                             // Changed By Nasruddin 20-SEP-16 sql
							//sql = "select count(*) as COUNT from location where loc_code = ? and facility_code=?";
							sql = "select count(1) as COUNT from location where loc_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, loc_code.trim());
						//	pstmt.setString(2, facility_code.trim());
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								count = rs.getInt("COUNT");
							}
							if (count > 0)
							{
								errList.add("VMDUPL1");
								errFields.add(childNodeName.toLowerCase());
								break;
							}
						}

						callPstRs(pstmt, rs);
						//}
					}

					else if ("site_code".equalsIgnoreCase(childNodeName)) 
					{
						/*Comment By Nasruddin [20-SEP-16]
						 * if (!(childNode.getFirstChild() == null))
						{
                          */
						site_code = checkNull(genericUtility.getColumnValue("site_code", curDom));
                        if(site_code != null && site_code.trim().length() > 0)
                        {
							sql = "select count(*) as COUNT from site where site_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, site_code.trim());
							rs = pstmt.executeQuery();

							if (rs.next()) 
							{
								count = rs.getInt("COUNT");
							}
							callPstRs(pstmt, rs);
							if (count <= 0) 
							{
								errList.add("VTSITE1");
								errFields.add(childNodeName.toLowerCase());
								break;
							}
						
						}
					}

					else if ("item_code".equalsIgnoreCase(childNodeName)) {
						//Changed By Nasruddin 20-SEP-16 
						//if (!(childNode.getFirstChild() == null))
						item_code = checkNull(genericUtility.getColumnValue("item_code", curDom));
						if(item_code != null && item_code.trim().length() >0)
						{

							sql = "select count(*) as COUNT from item where item_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, item_code.trim());
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								count = rs.getInt("COUNT");
							}
							callPstRs(pstmt, rs);
							if (count <= 0) 
							{
								errList.add("VTITEM1");
								errFields.add(childNodeName.toLowerCase());
								break;
							}
						}
					}
                  
					else if ("inv_stat".equalsIgnoreCase(childNodeName)) 
					{
						

							sql = "select count(*) as COUNT from invstat where inv_stat = ?";

							inv_stat = checkNull(genericUtility.getColumnValue(
									"inv_stat", curDom));
							pstmt = conn.prepareStatement(sql);

							pstmt.setString(1, inv_stat.trim());

							rs = pstmt.executeQuery();

							if (rs.next()) {
								count = rs.getInt("COUNT");
							}

							if (count <= 0) {
								errList.add("VTISTAT1");
								errFields.add(childNodeName.toLowerCase());
								break;
							}
							callPstRs(pstmt, rs);
					}
                 
					else if ("site_code__own".equalsIgnoreCase(childNodeName)) 
					{
						// Changed by Nasruddin [20-SEP-16]
						//if (!(childNode.getFirstChild() == null))
						site_code__own = checkNull(genericUtility.getColumnValue("site_code__own", curDom));
						if(site_code__own != null && site_code__own.trim().length() > 0)
						{
							sql = "select count(*) as COUNT from site where site_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, site_code__own.trim());
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								count = rs.getInt("COUNT");
							}
							callPstRs(pstmt, rs);
							if (count <= 0) 
							{
								errList.add("VMSITE1");
								errFields.add(childNodeName.toLowerCase());
								break;
							}
						}
					}

					else if ("facility_code".equalsIgnoreCase(childNodeName)) 
					{
						// Changed By Nasruddin [20-SEP-16]
						//if (!(childNode.getFirstChild() == null)) 
						facility_code = checkNull(genericUtility.getColumnValue("facility_code", curDom));
						if( facility_code != null && facility_code.trim().length() > 0)
						{
							sql = "select count(*) as COUNT from facility where facility_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, facility_code.trim());
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								count = rs.getInt("COUNT");
							}
							callPstRs(pstmt, rs);
							if (count <= 0) 
							{
								errList.add("VMFACI1");
								errFields.add(childNodeName.toLowerCase());
								break;
							}
						}
					}

					else if ("valid_upto".equalsIgnoreCase(childNodeName))
					{
						//Changed By Nasruddin [20-SEP-16]
						//if (!(childNode.getFirstChild() == null)) 
						valid_upto = checkNull(genericUtility.getColumnValue("valid_upto", curDom));
						eff_from = checkNull(genericUtility.getColumnValue("eff_from", curDom));
						if(valid_upto != null && valid_upto.trim().length() > 0 && eff_from != null && eff_from.trim().length() > 0)
						{
							SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
							System.out.println("valid_upto: " + valid_upto	+ " eff_from: " + eff_from);
							Date validUpto = sdf.parse(valid_upto);
							Date effFrom = sdf.parse(eff_from);

							System.out.println("validUpto: " + validUpto+ " effFrom: " + effFrom);

							if (effFrom.after(validUpto))
							{
								errList.add("VFRTODATE");
								errFields.add(childNodeName.toLowerCase());
								break;
							}
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
					// String errMsg = hashMap.get(errCode)!=null ?
					// hashMap.get(errCode).toString():"";
					// System.out.println("errMsg .........."+errMsg);
					errString = getErrorString(errFldName, errCode, userId);
					errorType = errorType(conn, errCode);
					if (errString.length() > 0) {
						String bifurErrString = errString.substring(
								errString.indexOf("<Errors>") + 8,
								errString.indexOf("<trace>"));
						// bifurErrString
						// =bifurErrString;//+"<trace>"+errMsg+"</trace>";
						bifurErrString = bifurErrString
								+ errString.substring(
										errString.indexOf("</trace>") + 8,
										errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						System.out.println("errStringXml .........."
								+ errStringXml);
						errString = "";
					}
					if (errorType.equalsIgnoreCase("E")) {
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
			errString = errStringXml.toString();
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

	public String cctrVal(String cctrCode, String acctCode, Connection conn) throws ITMException {
		String sql = "", active = "", errString = "CONFIRMED";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int count = 0;
		String varValue = "";
		sql = "select var_value from finparm where prd_code = '999999' and var_name = 'CCTR_CHECK'";

		try {
			pstmt = conn.prepareStatement(sql);

			rs = pstmt.executeQuery();

			if (rs.next()) {
				varValue = rs.getString("var_value");
			}
			//Added by sarita on 13NOV2017
			callPstRs(pstmt,rs);

			if (varValue.equalsIgnoreCase("Y")) {
				if (!cctrCode.isEmpty()) {
					sql = "select count(*) as COUNT from costctr where cctr_code = ?";

					pstmt = conn.prepareStatement(sql);

					pstmt.setString(1, cctrCode.trim());

					rs = pstmt.executeQuery();

					if (rs.next()) {
						count = rs.getInt("COUNT");
					}
					//Added by sarita on 13NOV2017
					callPstRs(pstmt,rs);
					
					if (count <= 0) {
						errString = "VMCCTR1";
						return errString;
					}
				}

				sql = "select count(*) as COUNT from accounts_cctr where acct_code = ? and cctr_code = ?";

				pstmt = conn.prepareStatement(sql);

				pstmt.setString(1, acctCode.trim());
				pstmt.setString(2, cctrCode.trim());

				rs = pstmt.executeQuery();

				if (rs.next()) {
					count = rs.getInt("COUNT");
				}
				//Added by sarita on 13NOV2017
				callPstRs(pstmt,rs);
				
				System.out.println("count: " + count);

				if (count <= 0) {
					errString = "VMCCTR2";
					return errString;
				}

			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
		}
		//Added by sarita on 13NOV2017
		finally
		{
			try
			{
				if (pstmt != null) 
				{
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) 
				{
					rs.close();
					rs = null;
				}
			}
			catch(Exception  e)
			{
				e.printStackTrace();
			}			
		}
		return errString;

	}

	public String acctVal(String loginSiteCode, String acctCode, Connection conn) throws ITMException {
		String sql = "", active = "", errString = "CONFIRMED";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int count = 0;
		String varValue = "";
		sql = "select var_value from finparm where prd_code = '999999' and var_name = 'SITE_SPECIFIC_ACCT'";

		try {
			pstmt = conn.prepareStatement(sql);

			rs = pstmt.executeQuery();

			if (rs.next()) {
				varValue = rs.getString("var_value");
			}
            //Added by sarita on 13NOV2017
			callPstRs(pstmt,rs);
			
			sql = " select count(*) as COUNT,ACTIVE from accounts where acct_code=? group by active";

			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, acctCode.trim());

			rs = pstmt.executeQuery();

			if (rs.next()) {
				count = rs.getInt("COUNT");
				active = rs.getString("ACTIVE");
			}
			//Added by sarita on 13NOV2017
			callPstRs(pstmt,rs);
			
			if (count <= 0) {
				errString = "VMACCT1";
				return errString;
			} else {
				if (!active.equalsIgnoreCase("Y")) {
					errString = "VMACCTA";
					return errString;
				} else {
					if (varValue.trim().equalsIgnoreCase("Y")) {

						sql = "select count(*) as COUNT from site_account where site_code = ? and acct_code = ?";

						pstmt = conn.prepareStatement(sql);

						pstmt.setString(1, loginSiteCode.trim());
						pstmt.setString(2, acctCode.trim());

						rs = pstmt.executeQuery();

						if (rs.next()) {
							count = rs.getInt("COUNT");
						}
						//Added by sarita on 13NOV2017
						callPstRs(pstmt,rs);
						
						if (count <= 0) {
							errString = "VMACCT3";
							return errString;
						}
					}
				}
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
		}
		//Added by sarita on 13NOV2017
		finally
		{
			try
			{
				if (pstmt != null) 
				{
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) 
				{
					rs.close();
					rs = null;
				}
			}
			catch(Exception  e)
			{
				e.printStackTrace();
			}		}
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

	public String itemChanged(String xmlString, String xmlString1,
			String xmlString2, String objContext, String currentColumn,
			String editFlag, String xtraParams) throws RemoteException,
			ITMException {
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

			valueXmlString = itemChanged(dom, dom1, dom2, objContext,
					currentColumn, editFlag, xtraParams);
		} catch (Exception e) {
			System.out
					.println("Exception : [DistributionRoute][itemChanged( String, String )] :==>\n"
							+ e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
		}
		return valueXmlString;
	}

	public String itemChanged(Document dom, Document dom1, Document dom2,
			String objContext, String currentColumn, String editFlag,
			String xtraParams) throws RemoteException, ITMException {
		System.out
				.println("####<<<<<<=======call item itemChanged Method=======>>>>>>>#####");
		String childNodeName = null;
		String sql = "", zone_descr = "", invstat_descr = "", one_item = "";
		String zone_code = "", inv_stat = "", item_code = "", temp = "";
		System.out.println(" editFlag itemchanged===>>" + editFlag);

		StringBuffer valueXmlString = new StringBuffer();
		int ctr = 0;
		int currentFormNo = 0;
		int childNodeListLength = 0;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		//changed by nasruddin 07-10-16
		E12GenericUtility genericUtility = new E12GenericUtility();
	//	GenericUtility genericUtility = GenericUtility.getInstance();
		ConnDriver connDriver = new ConnDriver();
		try {

			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
			if (objContext != null && objContext.trim().length() > 0) {
				currentFormNo = Integer.parseInt(objContext);
			}

			valueXmlString = new StringBuffer(
					"<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");
			switch (currentFormNo) {
			case 1:
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail1>");
				childNodeListLength = childNodeList.getLength();
				do {
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					ctr++;
				} while (ctr < childNodeListLength
						&& !childNodeName.equals(currentColumn));
				System.out.println("currentColumn = " + currentColumn);

				if ("zone_code".equalsIgnoreCase(currentColumn)) {
					zone_code = checkNull(genericUtility.getColumnValue(
							"zone_code", dom));

					sql = "select descr from zone where zone_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, zone_code.trim());
					rs = pstmt.executeQuery();
					if (rs.next()) {
						zone_descr = rs.getString("descr");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<zone_descr>")
							.append("<![CDATA[" + zone_descr + "]]>")
							.append("</zone_descr>\r\n");

				} else if ("inv_stat".equalsIgnoreCase(currentColumn)) {

					inv_stat = checkNull(genericUtility.getColumnValue(
							"inv_stat", dom));

					sql = "select descr from invstat where inv_stat = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, inv_stat.trim());
					rs = pstmt.executeQuery();
					if (rs.next()) {
						invstat_descr = rs.getString("descr");
					}

					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<invstat_descr>")
							.append("<![CDATA[" + invstat_descr + "]]>")
							.append("</invstat_descr>\r\n");

				}

				if (currentColumn.trim().equalsIgnoreCase("item_code")) {
					item_code = checkNull(genericUtility.getColumnValue(
							"item_code", dom));

					System.out.println("item_code: " + item_code);
					if (!item_code.isEmpty()) {
						valueXmlString.append("<one_item>")
								.append("<![CDATA[Y]]>").append("</one_item>");
					} else {
						valueXmlString.append("<one_item>")
								.append("<![CDATA[N]]>").append("</one_item>");
					}
				}

				if (currentColumn.trim().equalsIgnoreCase("one_item")) {
					one_item = checkNull(genericUtility.getColumnValue(
							"one_item", dom));

					System.out.println("one_item: " + one_item);
					if ((one_item.isEmpty())
							|| (one_item.equalsIgnoreCase("N"))) {
						valueXmlString.append("<item_code>")
								.append("<![CDATA[]]>")
								.append("</item_code>\r\n");
					}

				}  if (currentColumn.trim().equalsIgnoreCase("temp")) {
					temp = checkNull(genericUtility.getColumnValue("temp", dom));

					System.out.println("temp: " + temp);

					if ((temp.isEmpty()) || (temp.equalsIgnoreCase("N"))) {

						valueXmlString.append("<eff_from>")
								.append("<![CDATA[]]>")
								.append("</eff_from>\r\n");

						valueXmlString.append("<valid_upto>")
								.append("<![CDATA[]]>")
								.append("</valid_upto>\r\n");

					}
				}

				valueXmlString.append("</Detail1>");
				break;

			}
			valueXmlString.append("</Root>");

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
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
			}
		}
		return valueXmlString.toString();
	}

	private String checkNull(String input) {
		if (input == null)
		{
			input = "";
		}
		return input;
	}

	private String errorType(Connection conn, String errorCode) throws ITMException {
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
			throw new ITMException(ex); //Added By Mukesh Chauhan on 06/08/19
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
