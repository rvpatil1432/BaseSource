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
public class SchemeDefMaster extends ValidatorEJB implements
		SchemeDefMasterLocal, SchemeDefMasterRemote {
		//changed by nasruddin 05-10-16
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility = new E12GenericUtility();
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
			throws RemoteException, ITMException {
		System.out
				.println("####<<<<<<=======call mfg item wfValData  Method=======>>>>>>>#####");
		System.out.println(" editFlag validation===>>" + editFlag);
		String userId = "";
		String errorType = "";
		String childNodeName = null;
		String errCode = "";
		String errString = "", batch_qty = "", batch_value = "", min_qty = "";
		String bom_code = "", type = "", scheme_flag = "", min_batch_value = "";
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

		// StringBuffer errStringXml = new
		// StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
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
					if ("bom_code".equalsIgnoreCase(childNodeName))
					{
						bom_code = checkNull(genericUtility.getColumnValue("bom_code", curDom));
						type = checkNull(genericUtility.getColumnValue("type",curDom));
						if (!type.equalsIgnoreCase("C")) 
						{
							sql = "Select Count(*) as COUNT from scheme_applicability where scheme_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, bom_code.trim());
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								count = rs.getInt("COUNT");
							}

							callPstRs(pstmt, rs);
							if (count <= 0) 
							{
								errList.add("VMSCHEME1");
								errFields.add(childNodeName.toLowerCase());
								break;
							}
						}
						/*
						if (editFlag.equalsIgnoreCase("A")) 
						{
							count = 0;
							sql = "Select Count(*) as COUNT from BOM where bom_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, bom_code.trim());
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								count = rs.getInt("COUNT");
							}

							if (count >= 1) 
							{
								errList.add("VPINVBMCD");
								errFields.add(childNodeName.toLowerCase());
								break;
							}
						}
						 */
					}

					else if ("batch_qty".equalsIgnoreCase(childNodeName)) 
					{

						scheme_flag = checkNull(genericUtility.getColumnValue("scheme_flag", curDom));
						/*Comment By Nasruddin [20-SEP16] Start 
						 * if (scheme_flag.isEmpty())
						{
							scheme_flag = "Q";
						}Comment By Nasruddin [20-SEP16] END */
						if (!scheme_flag.equalsIgnoreCase("V")) 
						{
							batch_qty = checkNull(genericUtility.getColumnValue("batch_qty", curDom));
							Double batchQuantity;

							if (batch_qty.isEmpty()) 
							{
								batchQuantity = 0.0;
							} 
							else 
							{
								batchQuantity = Double.parseDouble(batch_qty);
							}

							if (batchQuantity == 0.0) 
							{
								errList.add("VTBQTY");
								errFields.add(childNodeName.toLowerCase());
								break;
							}
						}
					}

					else if ("batch_value".equalsIgnoreCase(childNodeName))
					{

						scheme_flag = checkNull(genericUtility.getColumnValue("scheme_flag", curDom));
						/* Comment By Nasruddin [20-SEP16] Start 
						if (scheme_flag.isEmpty())
						{
							scheme_flag = "Q";
						}
						Comment By Nasruddin [20-SEP16] END */
						System.out.println("scheme_flag in batch_value: "
								+ scheme_flag);

						if (scheme_flag.equalsIgnoreCase("V")) 
						{
							batch_value = checkNull(genericUtility.getColumnValue("batch_value", curDom));
							Double batchValue;

							if (batch_value.isEmpty())
							{
								batchValue = 0.0;
							} else
							{
								batchValue = Double.parseDouble(batch_value);
							}
							System.out.println("batchValue in batch_value: "
									+ batchValue);
							if (batchValue == 0.0) 
							{
								errList.add("VTBVALUE");
								errFields.add(childNodeName.toLowerCase());
								break;
							}
						}
					}
					else if ("min_qty".equalsIgnoreCase(childNodeName)) 
					{

						scheme_flag = checkNull(genericUtility.getColumnValue("scheme_flag", curDom));
						/* Comment By Nasruddin [20-SEP16] Start 
						if (scheme_flag.isEmpty()) 
						{
							scheme_flag = "Q";
						}
						Comment By Nasruddin [20-SEP16] END */
						if (!scheme_flag.equalsIgnoreCase("V"))
						{
							batch_qty = checkNull(genericUtility.getColumnValue("batch_qty", curDom));
							min_qty = checkNull(genericUtility.getColumnValue("min_qty", curDom));
							Double batchQuantity, minQuantity;
							System.out.println("batch_qty in min_qty: "	+ batch_qty);
							if (batch_qty.isEmpty()) 
							{
								batchQuantity = 0.0;
							}
							else
							{
								batchQuantity = Double.parseDouble(batch_qty);
							}
							System.out.println("min_qty in min_qty: " + min_qty);
							if (min_qty.isEmpty()) 
							{
								minQuantity = 0.0;
							} else
							{
								minQuantity = Double.parseDouble(min_qty);
							}
							System.out.println("batchQuantity: "
									+ batchQuantity + "batch_qty: " + batch_qty
									+ "in min_qty");
							if ((batchQuantity > 0) && !(batch_qty.isEmpty()))
							{
								if (minQuantity < batchQuantity)
								{
									errList.add("VTMINQTY");
									errFields.add(childNodeName.toLowerCase());
									break;
								}
							}
						}
					}
					else if ("min_batch_value".equalsIgnoreCase(childNodeName)) 
					{

						scheme_flag = checkNull(genericUtility.getColumnValue("scheme_flag", curDom));
						batch_value = checkNull(genericUtility.getColumnValue("batch_value", curDom));
						min_batch_value = checkNull(genericUtility.getColumnValue("min_batch_value", curDom));
						/* Comment By Nasruddin [20-SEP16] Start 
						if (scheme_flag.isEmpty())
						{
							scheme_flag = "Q";
						}
						Comment By Nasruddin [20-SEP16] END*/

						Double batchValue, minBatchValue;

						if (batch_value.isEmpty()) 
						{
							batchValue = 0.0;
						} 
						else 
						{
							batchValue = Double.parseDouble(batch_value);
						}

						if (min_batch_value.isEmpty()) 
						{
							minBatchValue = 0.0;
						}
						else
						{
							minBatchValue = Double.parseDouble(min_batch_value);
						}

						System.out.println("scheme_flag in min_batch_value: "+ scheme_flag);
						System.out.println("batchValue: " + batchValue
								+ " batch_value: " + batch_value
								+ " minBatchValue: " + minBatchValue);
						if (scheme_flag.equalsIgnoreCase("V"))
						{
							if ((batchValue > 0) && !(batch_value.isEmpty())) 
							{
								System.out.println("Inside (batchValue>0)");
								if (minBatchValue < batchValue) 
								{
									System.out
									.println("Inside minBatchValue<batchValue");
									errList.add("VTMINVALUE");
									errFields.add(childNodeName.toLowerCase());
									break;
								}
							}
						}

					}
				}
				break;

			case 2:

				parentNodeList = curDom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();

				String valid_upto = "",
				header_item_code = "",
				eff_from = "",
				app_min_qty = "",
				item_code = "",
				qty_per = "",
				app_max_qty = "";

				for (ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if ("valid_upto".equalsIgnoreCase(childNodeName)) 
					{

						valid_upto = checkNull(genericUtility.getColumnValue("valid_upto", curDom));
						eff_from = checkNull(genericUtility.getColumnValue("eff_from", curDom));

						SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());

						if (!(valid_upto.isEmpty()) || !(eff_from.isEmpty()))
						{
							Date validUpto = sdf.parse(valid_upto);
							Date effFrom = sdf.parse(eff_from);

							if (effFrom.after(validUpto)) 
							{
								errList.add("VMVAL_UPTO");
								errFields.add(childNodeName.toLowerCase());
								break;
							}
						}

					}

					if ("item_code".equalsIgnoreCase(childNodeName))
					{
						item_code = checkNull(genericUtility.getColumnValue("item_code", curDom));
						header_item_code = checkNull(genericUtility.getColumnValue("item_code", hdrDom));
						sql = "Select Count(*) as COUNT from item where item_code = ?";
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
							errList.add("VMITEM_CD");
							errFields.add(childNodeName.toLowerCase());
							break;
						}
						else
						{
							String active = "";
							sql = "select active as ACTIVE from item where item_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, item_code.trim());
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								active = rs.getString("ACTIVE");
							}
							callPstRs(pstmt, rs);
							if (!active.equalsIgnoreCase("Y"))
							{
								errList.add("VTITEM4");
								errFields.add(childNodeName.toLowerCase());
								break;
							} 
							else 
							{
								System.out.println("header_item_code: "	+ header_item_code);
								String item_ser_header = "", item_ser_detail = "", oth_series = "";

								if (!header_item_code.isEmpty()) 
								{
									sql = "select item_ser from ITEMSER_CHANGE where item_code = ? and valid_upto is null";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, header_item_code.trim());
									rs = pstmt.executeQuery();
									if (rs.next()) 
									{
										item_ser_header = rs.getString("item_ser");
									}
									// Changed By Nasruddin 20-SEP-16 START
									callPstRs(pstmt, rs);
									if(!item_ser_header.isEmpty())
									{
										sql = "select item_ser from Item where item_code = ? ";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, header_item_code.trim());
										rs = pstmt.executeQuery();
										if (rs.next()) 
										{
											item_ser_header = rs.getString("item_ser");
										}
										
										callPstRs(pstmt, rs);
									}
									// Changed By Nasruddin 20-SEP-16 END
									sql = "select item_ser from ITEMSER_CHANGE where item_code = ? and valid_upto is null";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, item_code.trim());
									rs = pstmt.executeQuery();
									if (rs.next()) 
									{
										item_ser_detail = rs.getString("item_ser");
									}
									// Changed By Nasruddin 20-SEP-16 START
									callPstRs(pstmt, rs);
									if(!item_ser_detail.isEmpty())
									{
										sql = "select item_ser from Item where item_code = ? ";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, item_code.trim());
										rs = pstmt.executeQuery();
										if (rs.next()) 
										{
											item_ser_header = rs.getString("item_ser");
										}
										
										callPstRs(pstmt, rs);
									}

									sql = "select oth_series from itemser where item_ser = ?";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, item_ser_header.trim());
									rs = pstmt.executeQuery();
									if (rs.next()) {
										oth_series = rs.getString("oth_series");
									}
									callPstRs(pstmt, rs);

									// Changed By Nasruddin 20-SEP-16 END
									if (oth_series.isEmpty()) 
									{
										oth_series = "N";
									}

									System.out.println("item_ser_header: "
											+ item_ser_header
											+ " item_ser_detail: "
											+ item_ser_detail + " oth_series: "
											+ oth_series);

									// if (!(item_ser_header.trim().equalsIgnoreCase(item_ser_detail))	&& (oth_series.equalsIgnoreCase("N")))
									if (!(item_ser_header.trim().equalsIgnoreCase(item_ser_detail.trim()))	&& (oth_series.equalsIgnoreCase("N")))  // condition changed by mahesh saggam on 08/aug/19
									{
										errList.add("VTITEM2");
										errFields.add(childNodeName.toLowerCase());
										break;
									}
								}
							}
						}

					}

					if ("qty_per".equalsIgnoreCase(childNodeName)) {

						qty_per = checkNull(genericUtility.getColumnValue("qty_per", curDom));
						scheme_flag = checkNull(genericUtility.getColumnValue("scheme_flag", hdrDom));

						if (!(scheme_flag.equalsIgnoreCase("V"))) 
						{
							int qtyPer = Integer.parseInt(qty_per);
							if ((qty_per.isEmpty()) || (qtyPer == 0))
							{
								errList.add("VTQTY");
								errFields.add(childNodeName.toLowerCase());
								break;
							}
						}

					}

					if ("app_max_qty".equalsIgnoreCase(childNodeName)) {

						app_max_qty = checkNull(genericUtility.getColumnValue("app_max_qty", curDom));
						app_min_qty = checkNull(genericUtility.getColumnValue("app_min_qty", curDom));
						scheme_flag = checkNull(genericUtility.getColumnValue("scheme_flag", hdrDom));

						int appMaxQty = Integer.parseInt(app_max_qty);
						int appMinQty = Integer.parseInt(app_min_qty);

						if (!(scheme_flag.equalsIgnoreCase("V"))) {
							if (appMaxQty < appMinQty) {
								errList.add("VMMINQTY");
								errFields.add(childNodeName.toLowerCase());
								break;
							}
						}
					}
				}

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
			catch(Exception e)
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
		catch(Exception e)
		{
			e.printStackTrace();
		}
		}
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
			
		}
		return valueXmlString;
	}

	public String itemChanged(Document dom, Document dom1, Document dom2,
			String objContext, String currentColumn, String editFlag,
			String xtraParams) throws RemoteException, ITMException {
		System.out
				.println("####<<<<<<=======call item itemChanged Method=======>>>>>>>#####");
		String childNodeName = null;
		String sql = "", descr = "", scheme_flag = "", nature = "", max_qty = "", min_batch_value = "";
		String bom_code = "", item_code = "", unit = "", batch_value = "", max_batch_value = "";
		System.out.println(" editFlag itemchanged===>>" + editFlag);

		StringBuffer valueXmlString = new StringBuffer();
		int ctr = 0, detailCount = 0;
		String detCnt = "", batch_qty = "";
		int currentFormNo = 0;
		int childNodeListLength = 0;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		//changed by nasruddin 05-10-16
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility = new E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();

		System.out.println("xtraParams" + xtraParams);
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

				if ("itm_default".equalsIgnoreCase(currentColumn)) {
					String min_qty = "";
					valueXmlString.append("<scheme_flag>")
							.append("<![CDATA[Q]]>")
							.append("</scheme_flag>\r\n");

					batch_qty = checkNull(genericUtility.getColumnValue(
							"batch_qty", dom));
					min_qty = checkNull(genericUtility.getColumnValue(
							"min_qty", dom));
					max_qty = checkNull(genericUtility.getColumnValue(
							"max_qty", dom));
					batch_value = checkNull(genericUtility.getColumnValue(
							"batch_value", dom));
					min_batch_value = checkNull(genericUtility.getColumnValue(
							"min_batch_value", dom));
					max_batch_value = checkNull(genericUtility.getColumnValue(
							"max_batch_value", dom));

					valueXmlString.append("<batch_qty protect ='0'>")
							.append("<![CDATA[" + batch_qty + "]]>")
							.append("</batch_qty>");

					valueXmlString.append("<min_qty protect ='0'>")
							.append("<![CDATA[" + min_qty + "]]>")
							.append("</min_qty>");

					valueXmlString.append("<max_qty protect ='0'>")
							.append("<![CDATA[" + max_qty + "]]>")
							.append("</max_qty>");

					valueXmlString.append("<batch_value protect ='1'>")
							.append("<![CDATA[" + batch_value + "]]>")
							.append("</batch_value>");

					valueXmlString.append("<min_batch_value protect ='1'>")
							.append("<![CDATA[" + min_batch_value + "]]>")
							.append("</min_batch_value>");

					valueXmlString.append("<max_batch_value protect ='1'>")
							.append("<![CDATA[" + max_batch_value + "]]>")
							.append("</max_batch_value>");

				} else if ("itm_defaultedit".equalsIgnoreCase(currentColumn)) {

					scheme_flag = checkNull(genericUtility.getColumnValue(
							"scheme_flag", dom));
					String min_qty = "";
					batch_qty = checkNull(genericUtility.getColumnValue(
							"batch_qty", dom));
					min_qty = checkNull(genericUtility.getColumnValue(
							"min_qty", dom));
					max_qty = checkNull(genericUtility.getColumnValue(
							"max_qty", dom));
					batch_value = checkNull(genericUtility.getColumnValue(
							"batch_value", dom));
					min_batch_value = checkNull(genericUtility.getColumnValue(
							"min_batch_value", dom));
					max_batch_value = checkNull(genericUtility.getColumnValue(
							"max_batch_value", dom));

					if (scheme_flag.equalsIgnoreCase("V")) {
						valueXmlString.append("<batch_qty protect ='1'>")
								.append("<![CDATA[" + batch_qty + "]]>")
								.append("</batch_qty>");

						valueXmlString.append("<min_qty protect ='1'>")
								.append("<![CDATA[" + batch_qty + "]]>")
								.append("</min_qty>");

						valueXmlString.append("<max_qty protect ='1'>")
								.append("<![CDATA[" + max_qty + "]]>")
								.append("</max_qty>");

						valueXmlString.append("<batch_value protect ='0'>")
								.append("<![CDATA[" + batch_value + "]]>")
								.append("</batch_value>");

						valueXmlString.append("<min_batch_value protect ='0'>")
								.append("<![CDATA[" + min_batch_value + "]]>")
								.append("</min_batch_value>");

						valueXmlString.append("<max_batch_value protect ='0'>")
								.append("<![CDATA[" + max_batch_value + "]]>")
								.append("</max_batch_value>");

					} else {
						valueXmlString.append("<batch_qty protect ='0'>")
								.append("<![CDATA[" + batch_qty + "]]>")
								.append("</batch_qty>");

						valueXmlString.append("<min_qty protect ='0'>")
								.append("<![CDATA[" + min_qty + "]]>")
								.append("</min_qty>");

						valueXmlString.append("<max_qty protect ='0'>")
								.append("<![CDATA[" + max_qty + "]]>")
								.append("</max_qty>");

						valueXmlString.append("<batch_value protect ='1'>")
								.append("<![CDATA[" + batch_value + "]]>")
								.append("</batch_value>");

						valueXmlString.append("<min_batch_value protect ='1'>")
								.append("<![CDATA[" + min_batch_value + "]]>")
								.append("</min_batch_value>");

						valueXmlString.append("<max_batch_value protect ='1'>")
								.append("<![CDATA[" + max_batch_value + "]]>")
								.append("</max_batch_value>");
					}

				}

				if (currentColumn.trim().equalsIgnoreCase("bom_code")) {
					System.out.println(" curr_code====>>>>");
					bom_code = checkNull(genericUtility.getColumnValue(
							"bom_code", dom));

					sql = "SELECT scheme_applicability.item_code,   \r\n"
							+ "         		item.descr,item.unit  \r\n"
							+ "    				FROM item,scheme_applicability  \r\n"
							+ "   				WHERE ( scheme_applicability.item_code = item.item_code ) and  \r\n"
							+ "         			( ( scheme_applicability.scheme_code = ? ) )";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, bom_code.trim());
					rs = pstmt.executeQuery();
					if (rs.next()) {
						item_code = rs.getString("item_code");
						descr = rs.getString("descr");
						unit = rs.getString("unit");
					}

					valueXmlString.append("<item_code>")
							.append("<![CDATA[" + item_code + "]]>")
							.append("</item_code>");

					valueXmlString.append("<unit>")
							.append("<![CDATA[" + unit + "]]>")
							.append("</unit>");

					valueXmlString.append("<item_descr>")
							.append("<![CDATA[" + descr + "]]>")
							.append("</item_descr>");

					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

				}

				if (currentColumn.trim().equalsIgnoreCase("scheme_flag")) {
					System.out.println(" scheme_flag====>>>>");
					scheme_flag = checkNull(genericUtility.getColumnValue(
							"scheme_flag", dom));
					String min_qty = "";
					batch_qty = checkNull(genericUtility.getColumnValue(
							"batch_qty", dom));
					min_qty = checkNull(genericUtility.getColumnValue(
							"min_qty", dom));
					max_qty = checkNull(genericUtility.getColumnValue(
							"max_qty", dom));
					batch_value = checkNull(genericUtility.getColumnValue(
							"batch_value", dom));
					min_batch_value = checkNull(genericUtility.getColumnValue(
							"min_batch_value", dom));
					max_batch_value = checkNull(genericUtility.getColumnValue(
							"max_batch_value", dom));

					if (scheme_flag.equalsIgnoreCase("V")) {
						valueXmlString.append("<batch_qty protect ='1'>")
								.append("<![CDATA[" + batch_qty + "]]>")
								.append("</batch_qty>");

						valueXmlString.append("<min_qty protect ='1'>")
								.append("<![CDATA[" + batch_qty + "]]>")
								.append("</min_qty>");

						valueXmlString.append("<max_qty protect ='1'>")
								.append("<![CDATA[" + max_qty + "]]>")
								.append("</max_qty>");

						valueXmlString.append("<batch_value protect ='0'>")
								.append("<![CDATA[" + batch_value + "]]>")
								.append("</batch_value>");

						valueXmlString.append("<min_batch_value protect ='0'>")
								.append("<![CDATA[" + min_batch_value + "]]>")
								.append("</min_batch_value>");

						valueXmlString.append("<max_batch_value protect ='0'>")
								.append("<![CDATA[" + max_batch_value + "]]>")
								.append("</max_batch_value>");

					} else {
						valueXmlString.append("<batch_qty protect ='0'>")
								.append("<![CDATA[" + batch_qty + "]]>")
								.append("</batch_qty>");

						valueXmlString.append("<min_qty protect ='0'>")
								.append("<![CDATA[" + min_qty + "]]>")
								.append("</min_qty>");

						valueXmlString.append("<max_qty protect ='0'>")
								.append("<![CDATA[" + max_qty + "]]>")
								.append("</max_qty>");

						valueXmlString.append("<batch_value protect ='1'>")
								.append("<![CDATA[" + batch_value + "]]>")
								.append("</batch_value>");

						valueXmlString.append("<min_batch_value protect ='1'>")
								.append("<![CDATA[" + min_batch_value + "]]>")
								.append("</min_batch_value>");

						valueXmlString.append("<max_batch_value protect ='1'>")
								.append("<![CDATA[" + max_batch_value + "]]>")
								.append("</max_batch_value>");
					}

				}
				valueXmlString.append("</Detail1>");
				break;

			case 2:
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail2>");
				childNodeListLength = childNodeList.getLength();
				do {
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					ctr++;
				} while (ctr < childNodeListLength
						&& !childNodeName.equals(currentColumn));
				System.out.println("currentColumn = " + currentColumn);

				if ("itm_defaultedit".equalsIgnoreCase(currentColumn)) {

					System.out.println("itm_defaultedit itemchange");
					
					nature = checkNull(genericUtility.getColumnValue("nature",
							dom));

					valueXmlString.append("<nature protect ='0'>")
							.append("<![CDATA[" + nature + "]]>")
							.append("</nature>");
					
					String qty_per = "", req_type = "", crit_item = "", min_qty = "", app_min_value = "", app_max_value = "", app_min_qty = "", app_max_qty = "";

					scheme_flag = checkNull(genericUtility.getColumnValue(
							"scheme_flag", dom1));

					app_max_qty = checkNull(genericUtility.getColumnValue(
							"app_max_qty", dom));
					app_min_qty = checkNull(genericUtility.getColumnValue(
							"app_min_qty", dom));
					item_code = checkNull(genericUtility.getColumnValue(
							"item_code", dom));
					qty_per = checkNull(genericUtility.getColumnValue(
							"qty_per", dom));
					req_type = checkNull(genericUtility.getColumnValue(
							"req_type", dom));
					qty_per = checkNull(genericUtility.getColumnValue(
							"qty_per", dom));
					crit_item = checkNull(genericUtility.getColumnValue(
							"crit_item", dom));
					min_qty = checkNull(genericUtility.getColumnValue(
							"min_qty", dom));
					app_min_value = checkNull(genericUtility.getColumnValue(
							"app_min_value", dom));
					app_max_value = checkNull(genericUtility.getColumnValue(
							"app_max_value", dom));

					System.out.println("scheme_flag: " + scheme_flag);
					System.out.println("nature: " + nature);

					if ((nature.equalsIgnoreCase("F"))
							|| (nature.equalsIgnoreCase("S"))
							|| (nature.equalsIgnoreCase("B"))) {

						System.out.println("Inside nature F,S or B");

						valueXmlString.append("<item_code protect ='0'>")
								.append("<![CDATA[" + item_code + "]]>")
								.append("</item_code>");

						valueXmlString.append("<qty_per protect ='0'>")
								.append("<![CDATA[" + qty_per + "]]>")
								.append("</qty_per>");

						valueXmlString.append("<req_type protect ='0'>")
								.append("<![CDATA[" + req_type + "]]>")
								.append("</req_type>");

						valueXmlString.append("<crit_item protect ='0'>")
								.append("<![CDATA[" + crit_item + "]]>")
								.append("</crit_item>");

						valueXmlString.append("<min_qty protect ='0'>")
								.append("<![CDATA[" + min_qty + "]]>")
								.append("</min_qty>");

						if (scheme_flag.equalsIgnoreCase("V")) {

							System.out.println("scheme flag V");

							valueXmlString
									.append("<app_min_value protect ='0'>")
									.append("<![CDATA[" + app_min_value + "]]>")
									.append("</app_min_value>");
							valueXmlString
									.append("<app_max_value protect ='0'>")
									.append("<![CDATA[" + app_max_value + "]]>")
									.append("</app_max_value>");
						} else {

							System.out.println("Scheme flag not V");

							valueXmlString.append("<app_min_qty protect ='0'>")
									.append("<![CDATA[" + app_min_qty + "]]>")
									.append("</app_min_qty>");
							valueXmlString.append("<app_max_qty protect ='0'>")
									.append("<![CDATA[" + app_max_qty + "]]>")
									.append("</app_max_qty>");
						}

						valueXmlString.append("<nature protect ='0'>")
								.append("<![CDATA[" + nature + "]]>")
								.append("</nature>");

					} else {
						System.out.println("Nature in else");

						valueXmlString.append("<req_type protect ='1'>")
								.append("<![CDATA[" + req_type + "]]>")
								.append("</req_type>");

						valueXmlString.append("<crit_item protect ='1'>")
								.append("<![CDATA[" + crit_item + "]]>")
								.append("</crit_item>");

						valueXmlString.append("<min_qty protect ='1'>")
								.append("<![CDATA[" + min_qty + "]]>")
								.append("</min_qty>");

						valueXmlString.append("<app_min_qty protect ='1'>")
								.append("<![CDATA[" + app_min_qty + "]]>")
								.append("</app_min_qty>");

						valueXmlString.append("<app_max_qty protect ='1'>")
								.append("<![CDATA[" + app_max_qty + "]]>")
								.append("</app_max_qty>");

						valueXmlString.append("<nature protect ='1'>")
								.append("<![CDATA[" + nature + "]]>")
								.append("</nature>");

						valueXmlString.append("<app_min_value protect ='1'>")
								.append("<![CDATA[" + app_min_value + "]]>")
								.append("</app_min_value>");

						valueXmlString.append("<app_max_value protect ='1'>")
								.append("<![CDATA[" + app_max_value + "]]>")
								.append("</app_max_value>");

					}
					
					
					
					
				}

				else if ("itm_default".equalsIgnoreCase(currentColumn)) {

					String qty_per = "", req_type = "", crit_item = "", min_qty = "", app_min_value = "", app_max_value = "", app_min_qty = "", app_max_qty = "", curr_code = "", value_per = "", min_value = "";

					nature = checkNull(genericUtility.getColumnValue("nature",
							dom));
					value_per = checkNull(genericUtility.getColumnValue(
							"value_per", dom));

					app_min_qty = checkNull(genericUtility.getColumnValue(
							"app_min_qty", dom));

					curr_code = checkNull(genericUtility.getColumnValue(
							"curr_code", dom));

					app_max_qty = checkNull(genericUtility.getColumnValue(
							"app_max_qty", dom));

					min_value = checkNull(genericUtility.getColumnValue(
							"min_value", dom));
					item_code = checkNull(genericUtility.getColumnValue(
							"item_code", dom));
					qty_per = checkNull(genericUtility.getColumnValue(
							"qty_per", dom));
					req_type = checkNull(genericUtility.getColumnValue(
							"req_type", dom));
					qty_per = checkNull(genericUtility.getColumnValue(
							"qty_per", dom));
					crit_item = checkNull(genericUtility.getColumnValue(
							"crit_item", dom));
					min_qty = checkNull(genericUtility.getColumnValue(
							"min_qty", dom));
					app_min_value = checkNull(genericUtility.getColumnValue(
							"app_min_value", dom));
					app_max_value = checkNull(genericUtility.getColumnValue(
							"app_max_value", dom));

					bom_code = checkNull(genericUtility.getColumnValue(
							"bom_code", dom));

					scheme_flag = checkNull(genericUtility.getColumnValue(
							"scheme_flag", dom1));

					detCnt = getValueFromXTRA_PARAMS(xtraParams, "detCnt");
					detailCount = Integer.parseInt(detCnt);

					if (detailCount <= 1) {
						valueXmlString.append("<nature>")
								.append("<![CDATA[C]]>").append("</nature>");
					} else {
						valueXmlString.append("<nature>")
								.append("<![CDATA[F]]>").append("</nature>");
					}

					if (scheme_flag.equalsIgnoreCase("")) {
						scheme_flag = "Q";
					}

					System.out.println("scheme_flag in itm_default: "
							+ scheme_flag);

					if (scheme_flag.equalsIgnoreCase("V")) {

						valueXmlString.append("<qty_per protect ='1'>")
								.append("<![CDATA[" + qty_per + "]]>")
								.append("</qty_per>");

						valueXmlString.append("<min_qty protect ='1'>")
								.append("<![CDATA[" + min_qty + "]]>")
								.append("</min_qty>");

						valueXmlString.append("<app_min_qty protect ='1'>")
								.append("<![CDATA[" + app_min_qty + "]]>")
								.append("</app_min_qty>");

						valueXmlString.append("<app_max_qty protect ='1'>")
								.append("<![CDATA[" + app_max_qty + "]]>")
								.append("</app_max_qty>");

						valueXmlString.append("<curr_code protect ='0'>")
								.append("<![CDATA[" + curr_code + "]]>")
								.append("</curr_code>");

						valueXmlString.append("<value_per protect ='0'>")
								.append("<![CDATA[" + value_per + "]]>")
								.append("</value_per>");

						valueXmlString.append("<min_value protect ='0'>")
								.append("<![CDATA[" + min_value + "]]>")
								.append("</min_value>");

						valueXmlString.append("<app_min_value protect ='0'>")
								.append("<![CDATA[" + app_min_value + "]]>")
								.append("</app_min_value>");

						valueXmlString.append("<app_max_value protect ='0'>")
								.append("<![CDATA[" + app_max_value + "]]>")
								.append("</app_max_value>");

					} else {

						valueXmlString.append("<qty_per protect ='0'>")
								.append("<![CDATA[" + qty_per + "]]>")
								.append("</qty_per>");

						valueXmlString.append("<min_qty protect ='0'>")
								.append("<![CDATA[" + min_qty + "]]>")
								.append("</min_qty>");

						valueXmlString.append("<app_min_qty protect ='0'>")
								.append("<![CDATA[" + qty_per + "]]>")
								.append("</app_min_qty>");

						valueXmlString.append("<curr_code protect ='1'>")
								.append("<![CDATA[" + curr_code + "]]>")
								.append("</curr_code>");

						valueXmlString.append("<app_max_value protect ='1'>")
								.append("<![CDATA[" + app_max_value + "]]>")
								.append("</app_max_value>");

						valueXmlString.append("<value_per protect ='1'>")
								.append("<![CDATA[" + value_per + "]]>")
								.append("</value_per>");

						valueXmlString.append("<min_value protect ='1'>")
								.append("<![CDATA[" + min_value + "]]>")
								.append("</min_value>");

						valueXmlString.append("<app_min_value protect ='1'>")
								.append("<![CDATA[" + app_min_value + "]]>")
								.append("</app_min_value>");

						valueXmlString.append("<app_max_value protect ='1'>")
								.append("<![CDATA[" + app_max_value + "]]>")
								.append("</app_max_value>");

					}

					valueXmlString.append("<bom_code>")
							.append("<![CDATA[" + bom_code + "]]>")
							.append("</bom_code>");
				}

				else if ("item_code".equalsIgnoreCase(currentColumn)) {

					item_code = checkNull(genericUtility.getColumnValue(
							"item_code", dom));
					descr = "";

					sql = "SELECT descr FROM item WHERE item_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, item_code.trim());
					rs = pstmt.executeQuery();
					if (rs.next()) {
						descr = rs.getString("descr");
					}
					valueXmlString.append("<item_descr>")
							.append("<![CDATA[" + descr + "]]>")
							.append("</item_descr>");

					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

				}

				else if ("nature".equalsIgnoreCase(currentColumn)) {

					String qty_per = "", req_type = "", crit_item = "", min_qty = "", app_min_value = "", app_max_value = "", app_min_qty = "", app_max_qty = "";

					scheme_flag = checkNull(genericUtility.getColumnValue(
							"scheme_flag", dom1));

					nature = checkNull(genericUtility.getColumnValue("nature",
							dom));

					app_max_qty = checkNull(genericUtility.getColumnValue(
							"app_max_qty", dom));
					app_min_qty = checkNull(genericUtility.getColumnValue(
							"app_min_qty", dom));
					item_code = checkNull(genericUtility.getColumnValue(
							"item_code", dom));
					qty_per = checkNull(genericUtility.getColumnValue(
							"qty_per", dom));
					req_type = checkNull(genericUtility.getColumnValue(
							"req_type", dom));
					qty_per = checkNull(genericUtility.getColumnValue(
							"qty_per", dom));
					crit_item = checkNull(genericUtility.getColumnValue(
							"crit_item", dom));
					min_qty = checkNull(genericUtility.getColumnValue(
							"min_qty", dom));
					app_min_value = checkNull(genericUtility.getColumnValue(
							"app_min_value", dom));
					app_max_value = checkNull(genericUtility.getColumnValue(
							"app_max_value", dom));

					System.out.println("scheme_flag: " + scheme_flag);
					System.out.println("nature: " + nature);

					if ((nature.equalsIgnoreCase("F"))
							|| (nature.equalsIgnoreCase("S"))
							|| (nature.equalsIgnoreCase("B"))) {

						System.out.println("Inside nature F,S or B");

						valueXmlString.append("<item_code protect ='0'>")
								.append("<![CDATA[" + item_code + "]]>")
								.append("</item_code>");

						valueXmlString.append("<qty_per protect ='0'>")
								.append("<![CDATA[" + qty_per + "]]>")
								.append("</qty_per>");

						valueXmlString.append("<req_type protect ='0'>")
								.append("<![CDATA[" + req_type + "]]>")
								.append("</req_type>");

						valueXmlString.append("<crit_item protect ='0'>")
								.append("<![CDATA[" + crit_item + "]]>")
								.append("</crit_item>");

						valueXmlString.append("<min_qty protect ='0'>")
								.append("<![CDATA[" + min_qty + "]]>")
								.append("</min_qty>");

						if (scheme_flag.equalsIgnoreCase("V")) {

							System.out.println("scheme flag V");

							valueXmlString
									.append("<app_min_value protect ='0'>")
									.append("<![CDATA[" + app_min_value + "]]>")
									.append("</app_min_value>");
							valueXmlString
									.append("<app_max_value protect ='0'>")
									.append("<![CDATA[" + app_max_value + "]]>")
									.append("</app_max_value>");
						} else {

							System.out.println("Scheme flag not V");

							valueXmlString.append("<app_min_qty protect ='0'>")
									.append("<![CDATA[" + app_min_qty + "]]>")
									.append("</app_min_qty>");
							valueXmlString.append("<app_max_qty protect ='0'>")
									.append("<![CDATA[" + app_max_qty + "]]>")
									.append("</app_max_qty>");
						}

						valueXmlString.append("<nature protect ='0'>")
								.append("<![CDATA[" + nature + "]]>")
								.append("</nature>");

					} else {
						System.out.println("Nature in else");

						valueXmlString.append("<req_type protect ='1'>")
								.append("<![CDATA[" + req_type + "]]>")
								.append("</req_type>");

						valueXmlString.append("<crit_item protect ='1'>")
								.append("<![CDATA[" + crit_item + "]]>")
								.append("</crit_item>");

						valueXmlString.append("<min_qty protect ='1'>")
								.append("<![CDATA[" + min_qty + "]]>")
								.append("</min_qty>");

						valueXmlString.append("<app_min_qty protect ='1'>")
								.append("<![CDATA[" + app_min_qty + "]]>")
								.append("</app_min_qty>");

						valueXmlString.append("<app_max_qty protect ='1'>")
								.append("<![CDATA[" + app_max_qty + "]]>")
								.append("</app_max_qty>");

						valueXmlString.append("<nature protect ='1'>")
								.append("<![CDATA[" + nature + "]]>")
								.append("</nature>");

						valueXmlString.append("<app_min_value protect ='1'>")
								.append("<![CDATA[" + app_min_value + "]]>")
								.append("</app_min_value>");

						valueXmlString.append("<app_max_value protect ='1'>")
								.append("<![CDATA[" + app_max_value + "]]>")
								.append("</app_max_value>");

					}
				}

				valueXmlString.append("</Detail2>");
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
		System.out.println("valueXmlString: " + valueXmlString);
		return valueXmlString.toString();
	}

	private String checkNull(String input) {
		if (input == null) {
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
			if (rs.next()) {
				msgType = rs.getString("MSG_TYPE");
			}
			callPstRs(pstmt, rs);
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

	public boolean isRecordExist(Connection conn, String tableName,
			String condition) throws ITMException {

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		boolean isExist = false;
		int recCount = 0;

		try {

			sql = " SELECT  COUNT(*)  FROM  " + tableName + "  " + condition;
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				recCount = rs.getInt(1);

			}
			callPstRs(pstmt, rs);
			if (recCount > 0) {
				isExist = true;

			} else {
				isExist = false;

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

		System.out.print("---------Final--isExist-------" + isExist);
		return isExist;
	}

}
