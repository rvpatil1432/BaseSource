
/********************************************************
 Title : ItemIC
 Date  : 18/05/16
 Developer: Tajuddin Mahadi

 ********************************************************/

package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Stateless
public class ItemIC extends ValidatorEJB implements ItemICLocal, ItemICRemote {
	
	E12GenericUtility genericUtility = new E12GenericUtility();

	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams)
			throws RemoteException, ITMException {
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
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
			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
		} catch (Exception e) {
			throw new ITMException(e);
		}
		return errString;
	}

	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams)
			throws RemoteException, ITMException {

		String childNodeValue = "", childNodeName = "", errString = "",  errCode = "", spec_tol = "",
				item_code = "", qc_reqd_type = "", stk_opt = "",  qc_reqd = "", track_shelf_life = "", str_shelf_life= "",
				sql = "",  sql2 = "",  sql3 = "",  sql4 = "",  errorType = "", 
				userId = "", unit = "",  transer = "M-ITEM", stkOpt="", loginSiteCode= "";
		int count = 0, cnt = 0, purLeadTime = 0, mfgPerc = 0, mfgLeadTime = 0, cycle_count = 0, shelf_life = 0, dlv_prd_tol = 0, min_shelf_life = 0, noSalesMonth = 0,  
				qcLeadTime = 0, yieldPerc = 0, ctr = 0, childNodeListLength;
		java.util.Date today = null, validUpto = null;
		NodeList parentNodeList = null, childNodeList = null;
		Node parentNode = null, childNode = null;
		//Modified by Rohini T. on[28/11/2018][Validate Qc Cycle Time][Start]
		String qcTime = "";
		//Modified by Rohini T. on[28/11/2018][Validate Qc Cycle Time][End]
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		FinCommon finCommon = new FinCommon(); // Changed By Nasruddin 19-SEP-16
		DistCommon common = new DistCommon();// Changed By Nasruddin 19-SEP-16
		Connection conn = null;
		PreparedStatement pstmt = null, pstmt2 = null, pstmt3 = null, pstmt4 = null, pstmt5 = null;
		ResultSet rs = null, rs2 = null, rs3 = null, rs4 = null, rs5 = null;
		ConnDriver connDriver = new ConnDriver();
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
		try {
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			// Changed By Nasruddin 19-SEP-16 
			loginSiteCode = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			parentNodeList = dom.getElementsByTagName("Detail1");
			parentNode = parentNodeList.item(0);
			childNodeList = parentNode.getChildNodes();
			childNodeListLength = childNodeList.getLength();

			//TODO -- FOR LOOP START.
			for (ctr = 0; ctr < childNodeListLength; ctr++)
			{
				count = 0;
				childNode = childNodeList.item(ctr);
				childNodeName = childNode.getNodeName().toLowerCase().trim();

				//item_code
				if (childNodeName.equalsIgnoreCase("item_code"))
				{
					childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					sql = "select key_flag from transetup where tran_window = 'w_item'";
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					String key_flag = "";
					if(rs.next()) 
					{
						key_flag = rs.getString(1);
					}
					//Add by Ajay on 21/02/18:START
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					//End
					key_flag = key_flag == null ? "" : key_flag.trim();
					if(key_flag.equalsIgnoreCase("M") && (childNodeValue == null || childNodeValue.trim().length() == 0)) 
					{
						errCode = "VMITEMNULL";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					} 
					if (childNodeValue != null && childNodeValue.trim().length() > 0) 
					{
						if (editFlag.equalsIgnoreCase("A")) 
						{
							sql = "select count(*)  from item where item_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, childNodeValue);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								count = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if (count > 0) 
							{
								//errCode = "VTITMEXST";// Changed By Nasruddin 
								errCode = "VMPMKY";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
				}
				//descr
				else if(childNodeName.equalsIgnoreCase("descr"))
				{
					childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					if (childNodeValue == null || childNodeValue.trim().length() == 0) 
					{
						//errCode = "VTDESC1";
						errCode = "VMBDESCR";// Changed By Nasruddin 
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
				}
				//item_parnt Changed BY Nasruddin 19-SEP-16
				//else if(childNodeName.equalsIgnoreCase("item_parnt") || childNodeName.equalsIgnoreCase("item_code__al"))
				else if(childNodeName.equalsIgnoreCase("item_parnt") )
				{
					childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					/* Comment By Nasruddin [16-SEP-16] START
					if (childNodeValue == null || childNodeValue.trim().length() == 0)
					{
						errCode = "VMITEMNULL";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
					 Comment By Nasruddin [16-SEP-16] END */
					//Changed By Nasruddin khan [16-SEP-16]
					//if (childNodeValue != null && childNodeValue.trim().length() > 0) 
					item_code = checkNullAndTrim(genericUtility.getColumnValue("item_code", dom));
                    if( childNodeValue.trim().length() > 0 && (!(childNodeValue).equalsIgnoreCase(item_code)))
					{
						sql = "select count(*)  from item where item_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, childNodeValue);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							count = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (count == 0) {
							//errCode = "VMITEMCD1";
							errCode = "VTITEMP1"; // Changed By Nasruddin
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}
				//stk_opt
				else if(childNodeName.equalsIgnoreCase("stk_opt")) 
				{
					childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					qc_reqd_type = checkNullAndTrim(genericUtility.getColumnValue("qc_reqd_type", dom));
					if (childNodeValue == null || childNodeValue.trim().length() == 0)
					{
						errCode = "VMSTKOPT";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					} 
					else if (childNodeValue != null && childNodeValue.trim().length() > 0) 
					{
						if(childNodeValue.equalsIgnoreCase("1"))
						{
							if(!qc_reqd_type.equals("I"))
							{
								errCode = "VIWRSTKOPT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						else if(childNodeValue.equalsIgnoreCase("2"))
						{
							if(!qc_reqd_type.equals("L") && !qc_reqd_type.equals("S") && !qc_reqd_type.equals("U"))
							{
								errCode = "VIWRSTKOPT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
				}
				//unit
				else if(childNodeName.equalsIgnoreCase("unit"))
				{
					childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					item_code = checkNullAndTrim(genericUtility.getColumnValue("item_code", dom));
					/* Comment By Nasruddin [19-sep-16] StART
					 * if (childNodeValue == null || childNodeValue.trim().length() == 0) 
					{
						errCode = "VTUOM1";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}Comment By Nasruddin [19-sep-16] end*/
					
					// Changed By Nasruddin [19-sep-16] START
					sql = "SELECT COUNT(1) FROM UOM WHERE UNIT = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, childNodeValue);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						count = rs.getInt(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					if( count == 0 )
					{
						errCode = "VTUOM1";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
					else
					{
						//Modified by Anjali R. on[08/03/2018][To check unit is same when edit any transaction][Start]
						if(childNodeValue != null && childNodeValue.trim().length() > 0 && !"A".equalsIgnoreCase(editFlag) )
						//if(childNodeValue != null && childNodeValue.trim().length() > 0)
						{
							/*sql = "select count(1) from stock where item_code = ? and unit = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, item_code);
							pstmt.setString(2, childNodeValue);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								count = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if (count > 0)
							{
								errCode = "VTSTKITM ";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}*/
							String orgUnit = "";
							System.out.println("item_code ----["+item_code+"] childNodeValue---["+childNodeValue+"]");
							sql = "select unit from item where item_code = ?";
							
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, item_code);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								orgUnit = rs.getString("unit").trim();
								System.out.println("orgUnit----["+orgUnit+"]");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("orgUnit---------["+orgUnit+"]new unit--------["+childNodeValue+"]");
							if(orgUnit != null && orgUnit.trim().length() > 0 && !orgUnit.equalsIgnoreCase(childNodeValue))
							{
								System.out.println("--------------------- Inside VTSTKITM -----------------");
								errCode = "VTEXITEM";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						//Modified by Anjali R. on[08/03/2018][To check unit is same when edit any transaction][End]
					}
					// Changed By Nasruddin [19-sep-16] END
					/* Comment By Nasruddin [19-sep-16] StART
					if (childNodeValue != null && childNodeValue.trim().length() > 0) 
					{
						sql = "select count(*) from stock where item_code = ? and unit = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, item_code);
						pstmt.setString(2, childNodeValue);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							count = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (count > 0)
						{
							errCode = "VTSTKITM ";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}Comment By Nasruddin [19-sep-16] end*/
				}
				// Changed By Nasruddin khan [19-SEP-16] START
				//unit__pur/unit__sal
				/*else if(childNodeName.equalsIgnoreCase("unit__pur") || childNodeName.equalsIgnoreCase("unit__sal") || 						childNodeName.equalsIgnoreCase("unit__netwt") || childNodeName.equalsIgnoreCase("unit__dimn") || 
						childNodeName.equalsIgnoreCase("unit") || childNodeName.equalsIgnoreCase("unit__rate")) {*/
				// Changed By Nasruddin khan [19-SEP-16] END
				else if(childNodeName.equalsIgnoreCase("unit__pur") || childNodeName.equalsIgnoreCase("unit__sal") || childNodeName.equalsIgnoreCase("unit__rate"))
				{
					//Modified by Anjali R. on [19/12/2018][Start]
					//childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					childNodeValue = genericUtility.getColumnValue(childNodeName, dom);
					//Modified by Anjali R. on [19/12/2018][End]

					/* Comment By Nasruddin [19-SEP-16] START
					if (childNodeValue == null || childNodeValue.trim().length() == 0) 
					{
						errCode = "VTUOM1";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					} 
					Comment By Nasruddin [19-SEP-16] END */
					//Modified by Anjali R. on [19/12/2018][Start]
					if (childNodeValue != null && childNodeValue.length() > 0 )
						//Modified by Anjali R. on [19/12/2018][End]	
					{
						sql = "select count(*) from uom where unit = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, childNodeValue);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							count = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (count == 0) {
							errCode = "VMITEMDK ";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}

				//site_code__own/site_code
				else if(childNodeName.equalsIgnoreCase("site_code__own") || childNodeName.equalsIgnoreCase("site_code") || childNodeName.equalsIgnoreCase("site_code__ship"))
				{
					childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					/* Changed By Nasruddin [19-SEP-16] START
					if (childNodeValue == null || childNodeValue.trim().length() == 0)
					{
						errCode = "VTMNULSITE";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					} 

					else if (childNodeValue != null && childNodeValue.trim().length() > 0) 
					{
						sql = "select count(*) from site where site_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, childNodeValue);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							count = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (count == 0) 
						{
							errCode = "VMSITE1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}*/

					childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					if (childNodeValue != null && childNodeValue.trim().length() > 0 )
					{
						errCode = this.isSiteCode(childNodeValue, transer);
						System.out.println("SiteCode Error code is"+errCode);
						if (errCode != null && errCode.trim().length() > 0) 
						{
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					// Changed By Nasruddin [19-SEP-16] END 
				}
				//loc_code/loc_type__parent/loc_zone__pref/loc_code__recv
				else if(childNodeName.equalsIgnoreCase("loc_code") 
						/*childNodeName.equalsIgnoreCase("loc_type__parent") || childNodeName.equalsIgnoreCase("loc_zone__pref") || */
						/*childNodeName.equalsIgnoreCase("loc_code__recv") Comment By Nasruddin 19-SEP-16 */)
				{
					childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					stkOpt = checkNullAndTrim(genericUtility.getColumnValue("stk_opt", dom));

					//Changed By Nasruddin [19-SEP-16]
					//if (childNodeValue != null && childNodeValue.trim().length() > 0) {
						if(!stk_opt.equalsIgnoreCase("0"))
						{	
							sql = "select count(*) from location where loc_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, childNodeValue);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								count = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if (count == 0)
							{
								errCode = "INVLOCCODE ";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
				}
				//apr_code
				else if(childNodeName.equalsIgnoreCase("apr_code"))
				{
					//Modified by Anjali R. on[19/12/2018][Start]
					//childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					childNodeValue = genericUtility.getColumnValue(childNodeName, dom);
					//Modified by Anjali R. on[19/12/2018][End]

					// Changed By Nasruddin [19-SEP-16]
					//	stk_opt = checkNullAndTrim(genericUtility.getColumnValue("stk_opt", dom));
					
					//Modified by Anjali R. on[19/12/2018][Start]
					//if (childNodeValue != null && childNodeValue.trim().length() > 0)
					if (childNodeValue != null && childNodeValue.length() > 0 )
						//Modified by Anjali R. on[19/12/2018][End]	
					{
						//if(!stk_opt.equalsIgnoreCase("0")) 
						//{
							sql = "select count(*) from aprlev where apr_code= ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, childNodeValue);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								count = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if (count == 0) 
							{
								errCode = "VTAPR1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						//}
					}
				}
				//tax_chap Changed by Nasruddin [19-SEP-16]
				else if(childNodeName.equalsIgnoreCase("tax_chap") )
				//else if(childNodeName.equalsIgnoreCase("tax_chap") || childNodeName.equalsIgnoreCase("tax_chap__rep"))
				{
					//Modified by Anjali R. on [19/12/2018][Start]
					//childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					childNodeValue = genericUtility.getColumnValue(childNodeName, dom);
					//Modified by Anjali R. on [19/12/2018][End]

					/*if (childNodeValue == null || childNodeValue.trim().length() == 0) {
						errCode = "VMITEMCD1 ";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					} else */
					
					//Modified by Anjali R. on [19/12/2018][Start]
					//if (childNodeValue != null && childNodeValue.trim().length() > 0)
					if (childNodeValue != null && childNodeValue.length() > 0)
						//Modified by Anjali R. on [19/12/2018][End]
					{
						sql = "select count(*) from taxchap where tax_chap = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, childNodeValue);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							count = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (count == 0) 
						{
							errCode = "VTTCHAP1 ";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}
				//tax_class
				else if(childNodeName.equalsIgnoreCase("tax_class")) 
				{
					//Modified by Anjali R. on [19/12/2018][Start]
					//childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					childNodeValue = genericUtility.getColumnValue(childNodeName, dom);
					//Modified by Anjali R. on [19/12/2018][End]


					/*if (childNodeValue == null || childNodeValue.trim().length() == 0) {
						errCode = "VMITEMCD1 ";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					} else */
					//Modified by Anjali R. on [19/12/2018][Start]
					//if (childNodeValue != null && childNodeValue.trim().length() > 0)
					if (childNodeValue != null && childNodeValue.length() > 0 )
						//Modified by Anjali R. on [19/12/2018][End]	
					{
						sql = "select count(*) from taxclass where tax_class = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, childNodeValue);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							count = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (count == 0) {
							errCode = "VTTCLASS1 ";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}
				//unit__rate
				/* Comment By Nasruddin 19-SEP-16 START
				 * else if(childNodeName.equalsIgnoreCase("unit__rate")) 
				{
					childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					if (childNodeValue == null || childNodeValue.trim().length() == 0) 
					{
						errCode = "VTUOM1";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
					else if (childNodeValue != null && childNodeValue.trim().length() > 0) {
						sql = "select count(*) from uom where unit = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, childNodeValue);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							count = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (count == 0) {
							errCode = "VTUOM2 ";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				} Comment By Nasruddin 19-SEP-16 END*/
				//cycle_count
				else if(childNodeName.equalsIgnoreCase("cycle_count")) 
				{
					childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					try
					{
						cycle_count = Integer.parseInt(childNodeValue);
						if(cycle_count < 0) 
						{
							errCode = "VTCYC1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					} 
					catch (Exception e)
					{
						cycle_count = 0;
					}
				}
				//qc_cycle_time
				else if(childNodeName.equalsIgnoreCase("qc_cycle_time"))
				{
					childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					qc_reqd = checkNullAndTrim(genericUtility.getColumnValue("qc_reqd", dom));
					if(qc_reqd.equalsIgnoreCase("Y"))
					{
						if (childNodeValue == null || childNodeValue.trim().length() == 0) 
						{
							errCode = "VMQCREQD";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}
				//spec_reqd
				else if(childNodeName.equalsIgnoreCase("spec_reqd")) 
				{
					childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					qc_reqd = checkNullAndTrim(genericUtility.getColumnValue("qc_reqd", dom));
					if(qc_reqd.equalsIgnoreCase("Y"))
					{
						if (childNodeValue == null || childNodeValue.trim().length() == 0) 
						{
							errCode = "VMSPEC1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}
				//qc_reqd_type
				else if(childNodeName.equalsIgnoreCase("qc_reqd_type")) 
				{
					childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					qc_reqd = checkNullAndTrim(genericUtility.getColumnValue("qc_reqd", dom));
					
					if(qc_reqd.equalsIgnoreCase("Y"))
					{
						if (childNodeValue == null || childNodeValue.trim().length() == 0)
						{
							errCode = "VMQCTYPE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}
				//qc_reqd
				else if(childNodeName.equalsIgnoreCase("qc_reqd"))
				{
					childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					stk_opt = checkNullAndTrim(genericUtility.getColumnValue("stk_opt", dom));					
					//if(qc_reqd.equalsIgnoreCase("Y")) Pavan Rane 08jan20[commented as same condition used inside and qc_reqd variable not-initializes]
					//{
						if (childNodeValue == null || childNodeValue.trim().length() == 0) 
						{
							errCode = "VTQCREQERR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						} 
						else if (childNodeValue.equalsIgnoreCase("Y"))
						{
							//if(!stk_opt.equalsIgnoreCase("2")) Pavan Rane 08jan20[to validate QC Reqd is 'Y' then stk opt should not '0']
							if("0".equalsIgnoreCase(stk_opt))
							{
								errCode = "VMSTKOPT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					//}
				}
				//pack_code
				else if(childNodeName.equalsIgnoreCase("pack_code"))
				{
					childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					
					/*if (childNodeValue == null || childNodeValue.trim().length() == 0) {
						errCode = "VMITEMCD1";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					} else */
					
					//if (childNodeValue != null && childNodeValue.trim().length() > 0) 
					//{
						sql = "select count(*) from packing where pack_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, childNodeValue);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							count = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (count == 0) 
						{
							errCode = "VTPKCD1 ";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					//}
				}
				//shelf_life
				else if(childNodeName.equalsIgnoreCase("shelf_life")) 
				{
					//Changed by wasim on 04-07-2016 to remove check null trim
					//childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					childNodeValue = genericUtility.getColumnValue(childNodeName, dom);
					track_shelf_life = checkNullAndTrim(genericUtility.getColumnValue("track_shelf_life", dom));

					if(track_shelf_life.equalsIgnoreCase("Y")) 
					{
						try
						{
							shelf_life = Integer.parseInt(childNodeValue==null ? "0" : childNodeValue.trim());
							if(shelf_life <= 0)
							{
								errCode = "VSHELF";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						} 
						catch (Exception e)
						{
							cycle_count = 0;
						}
					}
				}
				//min_shelf_life
				else if(childNodeName.equalsIgnoreCase("min_shelf_life"))
				{
					//Changed by wasim on 04-07-2016 to remove check null trim
					//childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					track_shelf_life = checkNullAndTrim(genericUtility.getColumnValue("track_shelf_life", dom));
					//str_shelf_life = checkNullAndTrim(genericUtility.getColumnValue("shelf_life", dom));
					childNodeValue = genericUtility.getColumnValue(childNodeName, dom);
					str_shelf_life = genericUtility.getColumnValue("shelf_life", dom);
					
					shelf_life = Integer.parseInt(str_shelf_life == null ? "0" : str_shelf_life);
					min_shelf_life = Integer.parseInt(childNodeValue == null ? "0" : childNodeValue);
					
					if(track_shelf_life.equalsIgnoreCase("Y")) 
					{
						if(min_shelf_life <= 0) 
						{
							errCode = "VMIN_SHELF";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						if(shelf_life > 0)
						{
							if(min_shelf_life > shelf_life)
							{
								errCode = "VMINSHELF";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
				}
				//emp_code__pln/emp_code__pur/emp_code__iapr
				else if(childNodeName.equalsIgnoreCase("emp_code__pln") || childNodeName.equalsIgnoreCase("emp_code__pur") || childNodeName.equalsIgnoreCase("emp_code__iapr") || childNodeName.equalsIgnoreCase("emp_code__qcaprv")) 
				{
					//Modified by Anjali R. on [19/12/2018][Start]
					//childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					childNodeValue = genericUtility.getColumnValue(childNodeName, dom);
					//Modified by Anjali R. on [19/12/2018][End]

					//Modified by Anjali R. on [19/12/2018][Start]
					//if(childNodeValue.trim().length() > 0) 
					if(childNodeValue != null && childNodeValue.length() > 0) 
						//Modified by Anjali R. on [19/12/2018][End]
					{/*Changed By Nasruddin [19-SEP-16] START
						sql = "select count(*) from employee where emp_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, childNodeValue);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							count = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (count == 0)
						{
							errCode = "EMPCODENF";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					*/
					 errCode = finCommon.isEmployee(loginSiteCode, childNodeValue, transer, conn);
						if (errCode != null && errCode.trim().length() > 0) 
						{
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						// Changed By Nasruddin [19-SEP-16] END
					}
				}
				//role_code__indaprv/role_code__qcaprv
				else if(childNodeName.equalsIgnoreCase("role_code__indaprv") || childNodeName.equalsIgnoreCase("role_code__qcaprv"))
				{
					childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					if(childNodeValue != null && childNodeValue.trim().length() > 0) 
					{
						sql = "select count(*) from wf_role where role_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, childNodeValue);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							count = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (count == 0)
						{
							errCode = "VTINVDROLL ";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}
				//dlv_prd_tol_bef/dlv_prd_tol_aft
				else if(childNodeName.equalsIgnoreCase("dlv_prd_tol_bef") || childNodeName.equalsIgnoreCase("dlv_prd_tol_aft")) 
				{
					//Changed by wasim on 04-01-2017 to remove check null trim
					//childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					childNodeValue = genericUtility.getColumnValue(childNodeName, dom);
					spec_tol = checkNullAndTrim(genericUtility.getColumnValue("spec_tol", dom));
					//dlv_prd_tol = Integer.parseInt(childNodeValue);
					dlv_prd_tol = Integer.parseInt(childNodeValue == null ? "0" : childNodeValue);
					
					if(spec_tol.equalsIgnoreCase("Y")) 
					{
						if(dlv_prd_tol == 0)
						{
							errCode = "VMTOL01 ";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}
				//supp_code__pref
				else if(childNodeName.equalsIgnoreCase("supp_code__pref"))
				{
					childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					//Modified by Anjali R. on [19/12/2018][Start]
					//if(childNodeValue.trim().length() > 0)
					if(childNodeValue != null && childNodeValue.trim().length() > 0)
						//Modified by Anjali R. on [19/12/2018][End]	

					{ /*Changed By Nasruddin 19-SEP-16 Start
						sql = "select count(*) from supplier where supp_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, childNodeValue);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							count = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (count == 0) 
						{
							errCode = "INVSUPPCD";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					 */
						errCode = finCommon.isSupplier(loginSiteCode, childNodeValue, transer, conn);
						if (errCode != null && errCode.trim().length() > 0) 
						{
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						// Changed By Nasruddin 19-SEP-16 END
					}
				}
				//mfg_date_on Changed By Nasruddin 19-SEP-16
				else if(childNodeName.equalsIgnoreCase("mfg_date_on"))
				{
					childNodeValue = checkNull(genericUtility.getColumnValue(childNodeName, dom));
					if (childNodeValue == null || childNodeValue.trim().length() == 0)
					{
						errCode = "VTMFGDATE2";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					} 
				}
				//emp_code__qcaprv
				else if(childNodeName.equalsIgnoreCase("emp_code__qcaprv"))
				{
					childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					qc_reqd = checkNullAndTrim(genericUtility.getColumnValue("qc_reqd", dom));
					if(qc_reqd.equalsIgnoreCase("Y"))
					{/* Changed By Nasruddin 19-SEP-16 START
						sql = "select count(*), relieve_date from employee where emp_code = ? group by relieve_date";// and relieve_date > sysdate";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, childNodeValue);
						rs = pstmt.executeQuery();
						today = Calendar.getInstance().getTime();
						if (rs.next())
						{
							java.sql.Date rlvDate = rs.getDate(2);
							Date dtRelDate = new Date(rlvDate.getTime());
							if(dtRelDate.before(today) && !dtRelDate.equals(today))
							{
								errCode = "VMEMPRLVD";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						else 
						{
							errCode = "EMPCODENF";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					 */
						if(childNodeValue == null || childNodeValue.trim().length() == 0 )
						{
							errCode = "VERREMPL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							sql = "select count(*), relieve_date from employee where emp_code = ? group by relieve_date";// and relieve_date > sysdate";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, childNodeValue);
							rs = pstmt.executeQuery();
							today = Calendar.getInstance().getTime();
							if (rs.next())
							{
								java.sql.Date rlvDate = rs.getDate(2);
								Date dtRelDate = new Date(rlvDate.getTime());
								if(dtRelDate.before(today) && !dtRelDate.equals(today))
								{
									errCode = "VMEMPRLVD";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							else 
							{
								errCode = "EMPCODENF";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
				}
				//item_type
				else if(childNodeName.equalsIgnoreCase("item_type")) {
					childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					if (childNodeValue == null || childNodeValue.trim().length() == 0) {
						//Item_parent Cannot be left Blank.
						errCode = "VUITEM ";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					} else if (childNodeValue != null && childNodeValue.trim().length() > 0) {
						sql = "select count(*) from item_type where item_type = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, childNodeValue);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							count = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (count == 0) {
							//Item DOES NOT Exist in ITEM Master.
							errCode = "INVITYPE ";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}
				//item_ser Changed By Nasruddin 19-SEP-16
				//else if(childNodeName.equalsIgnoreCase("item_ser") || childNodeName.equalsIgnoreCase("item_ser__pref")) 
				else if(childNodeName.equalsIgnoreCase("item_ser"))
				{
					childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					//Added By PriyankaC on 04JAN18 [START]
					if(childNodeValue==null || childNodeValue.trim().length() == 0)
					{
						errCode = "VMITMSERBK ";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
					//Added By PriyankaC on 04JAN18 [END]
					
					/* Comment By Nasruddin 19-SEP-16 START
					if (childNodeValue == null || childNodeValue.trim().length() == 0) 
					{
						if(childNodeValue.equalsIgnoreCase("item_ser")) 
						{
							errCode = "VTITSER1 ";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					} 
					 Comment By Nasruddin 19-SEP-16 END */
					
					else if (childNodeValue != null && childNodeValue.trim().length() > 0)
					{
					
						sql = "select count(*)  from itemser where item_ser = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, childNodeValue);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							count = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						if (count <= 0) 
						{
							errCode = "VTITEMSER1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						} 
						else
						{
							sql = "select case when link_yn is null then 'Y' else link_yn end from itemser where item_ser = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, childNodeValue);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								String link_yn = rs.getString(1);
								if(link_yn.equalsIgnoreCase("N"))
								{
									errCode = "VMISER";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
				}
				//item_code__parent
				// COMMENT BY NASRUDDIN 19-sep-16 
				//else if(childNodeName.equalsIgnoreCase("item_code__parent") || childNodeName.equalsIgnoreCase("item_code__plan")) {
				else if(childNodeName.equalsIgnoreCase("item_code__parent"))
				{
				childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
				item_code = checkNullAndTrim(genericUtility.getColumnValue("item_code", dom));
				/* Changed by Nasruddin  [19-SEP-16] Start
					if (childNodeValue == null || childNodeValue.trim().length() == 0)
					{
						if(childNodeName.equalsIgnoreCase("item_code__parent"))
						{
							errCode = "VMITEMCD1 ";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					Comment by Nasruddin  [19-SEP-16] END */
					 //if (childNodeValue != null && childNodeValue.trim().length() > 0) 
				if (childNodeValue != null && childNodeValue.trim().length() > 0 && (!(childNodeValue).equalsIgnoreCase(item_code))) 
					 {
						sql = "select count(1)  from item where item_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, childNodeValue);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							count = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (count == 0) 
						{
							errCode = "VTSCHITEM1 ";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					// Changed by Nasruddin  [19-SEP-16] END
				}
				//grp_code 
				/* Comment By Nasruddin 19-SEP-16 START
				else if(childNodeName.equalsIgnoreCase("grp_code")) {
					childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					if (childNodeValue == null || childNodeValue.trim().length() == 0) {
						errCode = "VMGRPCD3";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					} else if (childNodeValue != null && childNodeValue.trim().length() > 0) {
						sql = "SELECT count(*) FROM GENCODES WHERE FLD_NAME='GRP_CODE'";
						pstmt = conn.prepareStatement(sql);

						rs = pstmt.executeQuery();
						if (rs.next()) {
							count = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (count == 0) {
							//Invalid Group Code
							errCode = "VTPGRPCD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}
				//TODO stab_pattern
				else if(childNodeName.equalsIgnoreCase("stab_pattern")) {
					childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					if (childNodeValue != null && childNodeValue.trim().length() > 0) {
						sql = "SELECT count(*) FROM GENCODES WHERE FLD_NAME='STAB_PATRN' and MOD_NAME='W_ITEM'";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							count = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (count == 0) {
							errCode = "VTSTBPTRIN";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}
				//TODO dept_code__iss
				else if(childNodeName.equalsIgnoreCase("dept_code__iss")) {
					childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					if (childNodeValue != null && childNodeValue.trim().length() > 0) {
						sql = "select count(*)  from department where dept_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, childNodeValue);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							count = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (count == 0) {
							errCode = "VMDEP1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}
			
				//count_code__own/count_code__mfg
				else if(childNodeName.equalsIgnoreCase("count_code__own") || childNodeName.equalsIgnoreCase("count_code__mfg")) {
					childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					if (childNodeValue != null && childNodeValue.trim().length() > 0) {
						sql = "select count(*) from country where count_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, childNodeValue);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							count = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (count == 0) {
							errCode = "VMCONT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}
				//sgrp_code
				else if(childNodeName.equalsIgnoreCase("sgrp_code")) {
					childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					if (childNodeValue == null || childNodeValue.trim().length() == 0) {
						errCode = "VMSBGRPCD";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					} else if (childNodeValue != null && childNodeValue.trim().length() > 0) {
						sql = "SELECT count(*) FROM GENCODES WHERE FLD_NAME='SGRP_CODE'";
						pstmt = conn.prepareStatement(sql);

						rs = pstmt.executeQuery();
						if (rs.next()) {
							count = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (count == 0) {
							errCode = "VTSGRPCD1 ";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}	 Comment By Nasruddin 19-SEP-16 END */
				//brand_code
				else if(childNodeName.equalsIgnoreCase("brand_code"))
				{
					childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
				/* Comment By Nasruddin 19-SEP-16 Start
					if (childNodeValue == null || childNodeValue.trim().length() == 0) 
					{
						errCode = "VMBRNCDNL";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
					Comment By Nasruddin 19-SEP-16 END */
					 if (childNodeValue != null && childNodeValue.trim().length() > 0) 
					 {
						//Modified by Anjali R. on [19/12/2018][wrong Where clause column for brand table][Start]
						//sql = "select count(*)  from brand where code = ?";
						sql = "select count(*)  from brand where brand_code = ?";
						//Modified by Anjali R. on [19/12/2018][wrong Where clause column for brand table][End]	 

						 pstmt = conn.prepareStatement(sql);
						 pstmt.setString(1, childNodeValue);
						 rs = pstmt.executeQuery();
						 if (rs.next()) {
							 count = rs.getInt(1);
						 }
						 rs.close();
						 rs = null;
						 pstmt.close();
						 pstmt = null;
						 if (count == 0)
						 {
							 //Invalid BRAND Code
							 errCode = "VTBRAND";
							 errList.add(errCode);
							 errFields.add(childNodeName.toLowerCase());
						 }
					 }
				}
				/*bom_code__std/bom_code Comment By Nasruddin 19-SEP-16 START 
				else if(childNodeName.equalsIgnoreCase("bom_code__std") || childNodeName.equalsIgnoreCase("bom_code")) {
					childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					/*if (childNodeValue == null || childNodeValue.trim().length() == 0) {
						//TODO BOM Code Costing cannot be Blank
						errCode = "VMITEMCD1 ";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					} else if (childNodeValue != null && childNodeValue.trim().length() > 0) {
						sql = "select count(*)  from bom where bom_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, childNodeValue);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							count = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (count == 0) {
							//Invalid BRAND Code
							errCode = "VTBOMINV";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				} Comment By Nasruddin 19-SEP-16 END  */
				//cust_code__mkt 
				else if (childNodeName.equalsIgnoreCase("cust_code__mkt")) 
				{
					childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					/*Comment By Nasruddin 19-SEP-16 START
					 * if (childNodeValue == null || childNodeValue.trim().length() == 0)
					{
						//TODO BOM Code Costing cannot be Blank
						errCode = "VMITEMCD1 ";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					} 
					else 
					/*Comment By Nasruddin 19-SEP-16 END */
					if (childNodeValue != null && childNodeValue.trim().length() > 0) 
					{
						String blkListed = "";
						sql = "select black_listed from customer where cust_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, childNodeValue);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							blkListed = rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (blkListed.equalsIgnoreCase("Y")) 
						{
							//Invalid BRAND Code
							errCode = "VTCSTBLK";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}
				//item_code__ndc/item_code__ndc11
				/*Comment By Nasruddin 19-SEP-16 STARt
				else if(childNodeName.equalsIgnoreCase("item_code__ndc") || childNodeName.equalsIgnoreCase("item_code__ndc11")) {
					childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					if (childNodeValue == null || childNodeValue.trim().length() == 0) {
						errCode = "VMITEMNULL ";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					} else if (childNodeValue != null && childNodeValue.trim().length() > 0) {
						sql = "select count(*)  from item where item_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, childNodeValue);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							count = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (count == 0) {
							errCode = "VMITEMCD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}cust_code__mkt Comment By Nasruddin 19-SEP-16 END*/
				//loc_code
				else if(childNodeName.equalsIgnoreCase("loc_code"))
				{
					childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					stk_opt = checkNullAndTrim(genericUtility.getColumnValue("stk_opt", dom));

					if(!stk_opt.equalsIgnoreCase("0")) 
					{
						if (childNodeValue != null && childNodeValue.trim().length() > 0) 
						{
							sql = "select count(*) from location where loc_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, childNodeValue);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								count = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if (count == 0) {
								errCode = "VTLOC1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
				}
				/*loc_type Comment bY Nasruddin 19-SEP-16 START
				else if(childNodeName.equalsIgnoreCase("loc_type")) {
					childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					if (childNodeValue == null || childNodeValue.trim().length() == 0) {
						//LOcation Type cannot be Blank
						errCode = "VMLOCTYP";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					} else if (childNodeValue != null && childNodeValue.trim().length() > 0) {
						sql = "SELECT count(*) FROM GENCODES WHERE FLD_NAME='LOC_TYPE'";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							count = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (count == 0) {
							//Invalid Location Type
							errCode = "VMLOCINV ";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}
				loc_type Comment bY Nasruddin 19-SEP-16 END*/
				//supp_sour
				else if(childNodeName.equalsIgnoreCase("supp_sour")) 
				{
					childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					if (childNodeValue == null || childNodeValue.trim().length() == 0) 
					{
						errCode = "VMSUPPSR";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
				}
				//price_list
				/* Comment By Nasruddin 19-SEP-16 START
				else if(childNodeName.equalsIgnoreCase("price_list")) 
				{
					childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					if (childNodeValue.trim().length() > 0) {
						sql = "select count(*) from pricelist where price_list = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, childNodeValue);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							count = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (count == 0) {
							errCode = "VMPLIST08 ";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}
				 Comment By Nasruddin 19-SEP-16 END */
				//order_opt
				else if(childNodeName.equalsIgnoreCase("order_opt")) 
				{
					childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					if (childNodeValue == null || childNodeValue.trim().length() == 0)
					{
						errCode = "VMORDOPT";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
				}
				// Changed By Nasruddin 19-SEP-16 Start
				else if(childNodeName.equalsIgnoreCase("auto_reqc")) 
				{
					childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					if (childNodeValue == null || childNodeValue.trim().length() == 0)
					{
						errCode = "VMAUTORT";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
					//Modified by Rohini T. on[28/11/2018][Validate Qc Cycle Time][Start]
					else
					{
						if("Y".equalsIgnoreCase(childNodeValue))
						{
							qcTime = checkNullAndTrim(genericUtility.getColumnValue("qc_cycle_time", dom));
							int qcRetestTime = 0;
							try 
							{
								qcRetestTime = Integer.parseInt(qcTime);
							}
							catch (Exception e) 
							{
								e.printStackTrace();
								qcRetestTime = 0;
							}	
							if (qcRetestTime <= 0)
							{
								errCode = "VMQCCYCLE";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
				}
				/*else if(childNodeName.equalsIgnoreCase("auto_reqc")) 
				{
					childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					if (childNodeValue == null || childNodeValue.trim().length() == 0)
					{
						errCode = "VMAUTORT";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
				}*/
				//Modified by Rohini T. on[28/11/2018][Validate Qc Cycle Time][End]
				else if(childNodeName.equalsIgnoreCase("no_sales_month")) 
				{
					//Changed by wasim on 04-01-2017 to remove checknulltrim
					//childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					//track_shelf_life = checkNullAndTrim(genericUtility.getColumnValue("min_shelf_life", dom));
					childNodeValue = genericUtility.getColumnValue(childNodeName, dom);
					track_shelf_life = genericUtility.getColumnValue("min_shelf_life", dom);
					
					min_shelf_life = Integer.parseInt(track_shelf_life == null ? "0" : track_shelf_life);
					
					if (childNodeValue != null )
					{
						noSalesMonth = Integer.parseInt(childNodeValue == null ? "0" : childNodeValue);
						if( noSalesMonth > min_shelf_life)
						{
							errCode = "VMCGTMSL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					
				}
				/*else if(childNodeName.equalsIgnoreCase("no_sales_month")) 
				{
					childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					track_shelf_life = checkNullAndTrim(genericUtility.getColumnValue("min_shelf_life", dom));
					min_shelf_life = Integer.parseInt(track_shelf_life == null ? "0" : track_shelf_life);
					if (childNodeValue != null )
					{
						noSalesMonth = Integer.parseInt(childNodeValue == null ? "0" : childNodeValue);
						if( noSalesMonth > min_shelf_life)
						{
							errCode = "VMCGTMSL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					
				}*/
				/* Changed By Nasruddin 19-SEP-16 Start
				  else if(childNodeName.equalsIgnoreCase("phy_attrib_1") || childNodeName.equalsIgnoreCase("phy_attrib_2") || 
						childNodeName.equalsIgnoreCase("phy_attrib_3") || childNodeName.equalsIgnoreCase("phy_attrib_4") ||
						childNodeName.equalsIgnoreCase("phy_attrib_5")) 
				{
					childNodeValue = checkNullAndTrim(genericUtility.getColumnValue(childNodeName, dom));
					track_shelf_life = checkNullAndTrim(genericUtility.getColumnValue("min_shelf_life", dom));
					min_shelf_life = Integer.parseInt(track_shelf_life == null ? "0" : track_shelf_life);
					if (childNodeValue != null )
					{
						noSalesMonth = Integer.parseInt(childNodeValue == null ? "0" : childNodeValue);
						if( noSalesMonth > min_shelf_life)
						{
							errCode = "VMCGTMSL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					
				}*/
				// // Changed By Nasruddin 19-SEP-16 END
			}
			
			//TODO -- FOR LOOP End.

			int errListSize = errList.size();
			cnt = 0;
			String errFldName = null;
			if (errList != null && errListSize > 0) {
				for (cnt = 0; cnt < errListSize; cnt++) {
					errCode = errList.get(cnt);
					errFldName = errFields.get(cnt);
					System.out.println("errCode .........." + errCode);
					errString = getErrorString(errFldName, errCode, userId);
					errorType = errorType(conn, errCode);
					if (errString.length() > 0) {
						String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8, errString.indexOf("<trace>"));
						bifurErrString = bifurErrString+ errString.substring(errString.indexOf("</trace>") + 8,errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
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
				errStringXml.append("</Errors> </Root> \r\n");
			} else {
				errStringXml = new StringBuffer("");
			}
		} catch (Exception e) {
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

	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams)
			throws RemoteException, ITMException {
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
			valueXmlString = itemChanged(dom, dom1, dom2, objContext,
					currentColumn, editFlag, xtraParams);
		} catch (Exception e) {
			System.out
					.println("Exception : [SiteItemIC][itemChanged( String, String )] :==>\n"
							+ e.getMessage());
		}
		return valueXmlString;
	}

	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams)
			throws RemoteException, ITMException {

		Connection conn = null;
		String aprCode = "", sql = "", descrAprlev = "", grpCode = "", descrItemSer = "", itemSer = "", itemType = "", phyAttribThree = "", phyAttribFour = "";
		String siteCode = "", descrSiteCode = "", siteCodeOwn = "", descrSiteCodeOwn = "", specTol = "", dlvPrdTolBef = "", dlvPrdTolAft = "";
		String taxChap = "", descrTaxChap = "", trackShelfLife = "", locCode = "", descrLocCode = "", apprSupp = "", noConsUnaprv = "", disapprvOnRej = "", noOfRej = "";
		String suppSour = "", mfgType = "", extraMfgPerc = "", itemCode  = "", unit = "", siteCodeShip ="", descrSiteCodeShip ="", saleOption = "", contractReq = "";
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
			// SimpleDateFormat sdf = new
			// SimpleDateFormat(genericUtility.getDispDateFormat());
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			String loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			System.out.println("loginsitecode.....=" + loginSiteCode);
			if (objContext != null && objContext.trim().length() > 0) {
				currentFormNo = Integer.parseInt(objContext);
			}
			switch (currentFormNo) 
			{
			case 1:
			{
				valueXmlString.append("<Detail1>");
				System.out.println("currentColumn detail 1::: " + currentColumn);
				if (currentColumn != null) {
					if (currentColumn.equalsIgnoreCase("apr_code")) 
					{
						aprCode = checkNullAndTrim(genericUtility.getColumnValue("apr_code", dom));
						System.out.println(":::: aprCode ::: " + aprCode);
						sql = "select descr from aprlev where apr_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, aprCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							descrAprlev = checkNullAndTrim(rs.getString("descr"));
						}
						//Add by Ajay on 21/02/18:START
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						//End
						System.out.println("descrAprlev:::: " + descrAprlev);
						valueXmlString.append("<aprlev_descr><![CDATA[" + descrAprlev + "]]></aprlev_descr>");
					}
					if (currentColumn.equalsIgnoreCase("grp_code"))
					{
						grpCode = checkNullAndTrim(genericUtility.getColumnValue("grp_code", dom));
						System.out.println("grpCode:::: " + grpCode);

						if (grpCode.length() == 0) 
						{
							valueXmlString.append("<sgrp_code><![CDATA[]]></sgrp_code>");
						}
					}
					if (currentColumn.equalsIgnoreCase("item_ser")) {
						itemSer = checkNullAndTrim(genericUtility.getColumnValue("item_ser", dom));
						System.out.println("::: itemSer" + itemSer);
						sql = "select descr from itemser where item_ser = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemSer);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							descrItemSer = checkNullAndTrim(rs.getString("descr"));
						}
						//Add by Ajay on 21/02/18:START
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						//End
						System.out.println("::: descritemSer" + descrItemSer);
						valueXmlString.append("<itemser_descr><![CDATA[" + descrItemSer + "]]></itemser_descr>");
					}
					if (currentColumn.equalsIgnoreCase("item_type")) 
					{
						itemType = checkNullAndTrim(genericUtility.getColumnValue("item_type", dom));
						System.out.println(":::itemtype " + itemType);
						setAttrbVal(itemType, conn, valueXmlString);
						phyAttribThree = checkNullAndTrim(genericUtility.getColumnValue("phy_attrib_3", dom));
						System.out.println("phyAttribThree:: " + phyAttribThree);
						phyAttribFour = checkNullAndTrim(genericUtility.getColumnValue("phy_attrib_4", dom));
						System.out.println("phyAttribFour:: " + phyAttribFour);
						String valueThree = setDescr(itemType, phyAttribThree, phyAttribFour, conn);
						valueXmlString.append("<descr><![CDATA[" + valueThree + "]]></descr>");
					}
					if (currentColumn.equalsIgnoreCase("itm_default")) {
						System.out.println("itm default called:::");
						valueXmlString.append("<canc_bo_mode><![CDATA[A]]></canc_bo_mode>");
						noConsUnaprv = checkNullAndTrim(genericUtility.getColumnValue("no_cons_unaprv", dom));
						disapprvOnRej = checkNullAndTrim(genericUtility.getColumnValue("disapprv_on_rej", dom));
						noOfRej = checkNullAndTrim(genericUtility.getColumnValue("no_of_rej", dom));
						extraMfgPerc = checkNullAndTrim(genericUtility.getColumnValue("extra_mfg_perc", dom));
						System.out.println("extraMfgPerc:: " + extraMfgPerc);
						System.out.println("noConsUnaprv:: " + noConsUnaprv);
						System.out.println("disapprvOnRej:: " + disapprvOnRej);
						System.out.println("noOfRej:: " + noOfRej);

						valueXmlString.append("<no_cons_unaprv  protect = \"1\"><![CDATA[" + noConsUnaprv + "]]></no_cons_unaprv>");
						valueXmlString.append("<disapprv_on_rej  protect = \"1\"><![CDATA[" + disapprvOnRej + "]]></disapprv_on_rej>");
						valueXmlString.append("<no_of_rej  protect = \"1\"><![CDATA[" + noOfRej + "]]></no_of_rej>");

						mfgType = checkNullAndTrim(genericUtility.getColumnValue("mfg_type", dom));
						suppSour = checkNullAndTrim(genericUtility.getColumnValue("supp_sour", dom));

						System.out.println("mfgType:: " + mfgType);
						System.out.println("suppSour:: " + suppSour);

						if ("D".equalsIgnoreCase(mfgType.trim()) && "M".equalsIgnoreCase(suppSour.trim())) {
							valueXmlString.append("<extra_mfg_perc protect = \"0\"><![CDATA[" + extraMfgPerc + "]]></extra_mfg_perc>");
						} else {
							valueXmlString.append("<extra_mfg_perc protect = \"1\"><![CDATA[0]]></extra_mfg_perc>");
						}
						valueXmlString.append("<spec_reqd><![CDATA[N]]></spec_reqd>");
						valueXmlString.append("<qc_reqd><![CDATA[N]]></qc_reqd>");
						valueXmlString.append("<auto_reqc><![CDATA[N]]></auto_reqc>");
						valueXmlString.append("<qc_reqd_type><![CDATA[L]]></qc_reqd_type>");
						valueXmlString.append("<shelf_life__type><![CDATA[E]]></shelf_life__type>");
					}
					if (currentColumn.equalsIgnoreCase("itm_defaultedit")) {
						apprSupp = checkNullAndTrim(genericUtility.getColumnValue("appr_supp", dom));
						noConsUnaprv = checkNullAndTrim(genericUtility.getColumnValue("no_cons_unaprv", dom));
						disapprvOnRej = checkNullAndTrim(genericUtility.getColumnValue("disapprv_on_rej", dom));
						noOfRej = checkNullAndTrim(genericUtility.getColumnValue("no_of_rej", dom));

						System.out.println("apprSupp:: " + apprSupp);
						System.out.println("noConsUnaprv:: " + noConsUnaprv);
						System.out.println("disapprvOnRej:: " + disapprvOnRej);
						System.out.println("noOfRej:: " + noOfRej);

						if ("Y".equalsIgnoreCase(apprSupp)) {
							valueXmlString.append("<no_cons_unaprv  protect = \"0\"><![CDATA[" + noConsUnaprv + "]]></no_cons_unaprv>");
							valueXmlString.append("<disapprv_on_rej  protect = \"0\"><![CDATA[" + disapprvOnRej + "]]></disapprv_on_rej>");

							if ("Y".equalsIgnoreCase(disapprvOnRej)) {
								valueXmlString.append("<no_of_rej  protect = \"0\"><![CDATA[" + noOfRej + "]]></no_of_rej>");
							} else {
								valueXmlString.append("<no_of_rej  protect = \"1\"><![CDATA[" + noOfRej + "]]></no_of_rej>");
							}
						} else {
							valueXmlString.append("<no_cons_unaprv  protect = \"1\"><![CDATA[" + noConsUnaprv + "]]></no_cons_unaprv>");
							valueXmlString.append("<disapprv_on_rej  protect = \"1\"><![CDATA[" + disapprvOnRej + "]]></disapprv_on_rej>");
							valueXmlString.append("<no_of_rej  protect = \"1\"><![CDATA[" + noOfRej + "]]></no_of_rej>");
						}
					}
					if (currentColumn.equalsIgnoreCase("appr_supp")) {
						apprSupp = checkNullAndTrim(genericUtility.getColumnValue("appr_supp", dom));
						noConsUnaprv = checkNullAndTrim(genericUtility.getColumnValue("no_cons_unaprv", dom));
						disapprvOnRej = checkNullAndTrim(genericUtility.getColumnValue("disapprv_on_rej", dom));

						if("Y".equalsIgnoreCase(apprSupp.trim())) {
							valueXmlString.append("<no_cons_unaprv  protect = \"0\"><![CDATA[" + noConsUnaprv + "]]></no_cons_unaprv>");
							valueXmlString.append("<disapprv_on_rej  protect = \"0\"><![CDATA[" + disapprvOnRej + "]]></disapprv_on_rej>");
						} else {
							valueXmlString.append("<no_cons_unaprv  protect = \"1\"><![CDATA[" + noConsUnaprv + "]]></no_cons_unaprv>");
							valueXmlString.append("<disapprv_on_rej  protect = \"1\"><![CDATA[" + disapprvOnRej + "]]></disapprv_on_rej>");
						}

					}
					if (currentColumn.equalsIgnoreCase("loc_code")) 
					{
						locCode = checkNullAndTrim(genericUtility.getColumnValue("loc_code", dom));
						System.out.println("locCode:: " + locCode);

						sql = "select descr from from location where loc_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, locCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							descrLocCode = checkNullAndTrim(rs.getString("descr"));
						}
						//Add by Ajay on 21/02/18:START
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						//End
						System.out.println("descrLocCode:: " + descrLocCode);

						valueXmlString.append("<location_descr><![CDATA[" + descrLocCode + "]]></location_descr>");
					}
					/* Comment By Nasruddin Start 23-SEP-16 Start
					 * if (currentColumn.equalsIgnoreCase("phy_attrib_3")) 
					{

						phyAttribThree = checkNullAndTrim(genericUtility.getColumnValue("phy_attrib_3", dom));

						System.out.println("phyAttribThree:: " + phyAttribThree);

						phyAttribFour = checkNullAndTrim(genericUtility.getColumnValue("phy_attrib_4", dom));

						System.out.println("phyAttribFour:: " + phyAttribFour);

						String valueThree = setDescr(itemType, phyAttribThree,phyAttribFour, conn);

						valueXmlString.append("<descr><![CDATA[" + valueThree + "]]></descr>");
					}
					if (currentColumn.equalsIgnoreCase("phy_attrib_4")) {

						phyAttribThree = checkNullAndTrim(genericUtility.getColumnValue("phy_attrib_3", dom));

						System.out.println("phyAttribThree:: " + phyAttribThree);

						phyAttribFour = checkNullAndTrim(genericUtility.getColumnValue("phy_attrib_4", dom));

						System.out.println("phyAttribFour:: " + phyAttribFour);

						String valueFour = setDescr(itemType, phyAttribThree,phyAttribFour, conn);

						valueXmlString.append("<descr><![CDATA[" + valueFour + "]]></descr>");
					}Comment By Nasruddin Start 23-SEP-16 END */
					if (currentColumn.equalsIgnoreCase("site_code"))
					{

						siteCode = checkNullAndTrim(genericUtility.getColumnValue("site_code", dom));

						System.out.println("siteCode:: " + siteCode);

						//Modified by Anjali R. on[08/03/2018][Seems written wrong query][Start]
						//sql = "select descr from from site where site_code = ?";
						sql = "select descr from site where site_code = ?";
						//Modified by Anjali R. on[08/03/2018][Seems written wrong query][End]
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, siteCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							descrSiteCode = checkNullAndTrim(rs.getString("descr"));
						}
						System.out.println("descrSiteCode:: " + descrSiteCode);

						valueXmlString.append("<site_b_site_descr><![CDATA[" + descrSiteCode + "]]></site_b_site_descr>");
					}

					if (currentColumn.equalsIgnoreCase("site_code__own")) {

						siteCodeOwn = checkNullAndTrim(genericUtility.getColumnValue("site_code__own", dom));

						System.out.println("siteCodeOwn:: " + siteCodeOwn);
						
						//Modified by Anjali R. on[08/03/2018][Seems written wrong query][Start]
						//sql = "select descr from from site where site_code = ?";
						sql = "select descr from site where site_code = ?";
						//Modified by Anjali R. on[08/03/2018][Seems written wrong query][End]
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, siteCodeOwn);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							descrSiteCodeOwn = checkNullAndTrim(rs.getString("descr"));
						}
												System.out.println("descrSiteCodeOwn:: " + descrSiteCodeOwn);

						valueXmlString.append("<site_descr><![CDATA[" + descrSiteCodeOwn + "]]></site_descr>");
					}

					if (currentColumn.equalsIgnoreCase("spec_tol")) {

						specTol = checkNullAndTrim(genericUtility.getColumnValue("spec_tol", dom));
						dlvPrdTolBef = checkNullAndTrim(genericUtility.getColumnValue("dlv_prd_tol_bef", dom));
						dlvPrdTolAft = checkNullAndTrim(genericUtility.getColumnValue("dlv_prd_tol_aft", dom));

						System.out.println("specTol:: " + specTol);

						if ("N".equalsIgnoreCase(specTol) || specTol.isEmpty()) {
							valueXmlString.append("<dlv_prd_tol_bef  protect = \"1\"><![CDATA[0]]></dlv_prd_tol_bef>");
							valueXmlString.append("<dlv_prd_tol_aft  protect = \"1\"><![CDATA[0]]></dlv_prd_tol_aft>");
						} else if ("Y".equalsIgnoreCase(specTol)) {
							valueXmlString.append("<dlv_prd_tol_bef  protect = \"0\"><![CDATA[" + dlvPrdTolBef + "]]></dlv_prd_tol_bef>");
							valueXmlString.append("<dlv_prd_tol_aft  protect = \"0\"><![CDATA[" + dlvPrdTolAft + "]]></dlv_prd_tol_aft>");
						}
					}
					if (currentColumn.equalsIgnoreCase("tax_chap")) {

						taxChap = checkNullAndTrim(genericUtility.getColumnValue("tax_chap", dom));

						System.out.println("taxChap:: " + taxChap);

						sql = "select descr from taxchap where tax_chap = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, taxChap);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							descrTaxChap = checkNullAndTrim(rs.getString("descr"));
						}
						
						valueXmlString.append("<taxchap_descr><![CDATA[" + descrTaxChap + "]]></taxchap_descr>");
					}
					if (currentColumn.equalsIgnoreCase("track_shelf_life")) {

						trackShelfLife = checkNullAndTrim(genericUtility.getColumnValue("track_shelf_life", dom));

						System.out.println("trackShelfLife:: " + trackShelfLife);

						if (trackShelfLife.isEmpty()|| "N".equalsIgnoreCase(trackShelfLife)) {

							valueXmlString.append("<min_shelf_life><![CDATA[0]]></min_shelf_life>");
						}
					}
					//added on 21/MAY/16 after cross checking on live server[START]

					if (currentColumn.equalsIgnoreCase("item_code")) {

						itemCode = checkNullAndTrim(genericUtility.getColumnValue("item_code", dom));
						System.out.println("itemCode:: " + itemCode);

						valueXmlString.append("<item_code__plan><![CDATA[" + itemCode + "]]></item_code__plan>");
					}

					if (currentColumn.equalsIgnoreCase("unit")) {
						unit = checkNullAndTrim(genericUtility.getColumnValue("unit", dom));
						System.out.println("Unit :: " + unit);

						valueXmlString.append("<unit__pur><![CDATA[" + unit + "]]></unit__pur>");
						valueXmlString.append("<unit__rate><![CDATA[" + unit + "]]></unit__rate>");
					}

					if (currentColumn.equalsIgnoreCase("site_code__ship")) {

						siteCodeShip = checkNullAndTrim(genericUtility.getColumnValue("site_code__ship", dom));

						System.out.println("siteCodeShip:: " + siteCodeShip);

						//Modified by Anjali R. on[08/03/2018][Seems written wrong query][Start]
						//sql = "select descr from from site where site_code = ?";
						sql = "select descr from site where site_code = ?";
						//Modified by Anjali R. on[08/03/2018][Seems written wrong query][End]
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, siteCodeShip);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							descrSiteCodeShip = checkNullAndTrim(rs.getString("descr"));
						}
						
						System.out.println("descrSiteCodeShip:: " + descrSiteCodeShip);

						valueXmlString.append("<site_descr_ship><![CDATA[" + descrSiteCodeShip + "]]></site_descr_ship>");
					}

					if(currentColumn.equalsIgnoreCase("sale_option")) {
						saleOption = checkNullAndTrim(genericUtility.getColumnValue("sale_option", dom));
						contractReq = checkNullAndTrim(genericUtility.getColumnValue("contract_req", dom));
						if("S".equalsIgnoreCase(saleOption)) {
							valueXmlString.append("<contract_req protect = '0'><![CDATA[" +  contractReq + "]]></contract_req>");
						} else {
							valueXmlString.append("<contract_req protect = '1'><![CDATA[" +  contractReq + "]]></contract_req>");
						}
					}
					//[END]
				}
			}
			}
		} 
		catch (Exception e) 
		{

			System.out.println(":::: " + this.getClass().getSimpleName()
					+ ":::" + e.getMessage());
			e.printStackTrace();
		}
		finally 
		{
			try 
			{
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
		valueXmlString.append("</Detail1>");      
		valueXmlString.append("</Root>");
		System.out.println("ValueXmlString:::::" + valueXmlString.toString());
		return valueXmlString.toString();
	}

	private String checkNullAndTrim(String value) {
		return value == null ? "" : value.trim();
	}
	
	private String checkNull(String value) {
		return value == null ? "" : value;
	}

	private int convertInt(String value) {
		return value.trim().length() == 0 ? 0 : Integer.parseInt(value);
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
			if (rs.next()) {
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
	
	private void setAttrbVal(String itemType, Connection conn, StringBuffer valueXmlString) {
		String sql = "";
		String phyAttrOne = "", phyAttrTwo = "", phyAttrThree = "", phyAttrFour = "", phyAttrFive = "", phyAttrSix = "", phyAttrSeven = "", phyAttrEight = "", phyAttrNine = "", phyAttrTen = "", phyAttrEleven = "";
		String phyAttrTwelve = "", phyAttrThirt = "", phyAttrFourt = "", phyAttrFift = "", phyAttrSixt = "", phyAttrSevent = "", phyAttrEighte = "", phyAttrNinet = "", phyAttrTwenty = "", phyAttrTwentyOne = "", phyAttrTwentyTwo = "", phyAttrTwentyThree = "", phyAttrTwentyFour = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			sql = "select PHY_ATTRIB_1, PHY_ATTRIB_2, PHY_ATTRIB_3, PHY_ATTRIB_4, PHY_ATTRIB_5, PHY_ATTRIB_6, PHY_ATTRIB_7, PHY_ATTRIB_8, PHY_ATTRIB_9,"
					+ "PHY_ATTRIB_10, PHY_ATTRIB_11, PHY_ATTRIB_12, PHY_ATTRIB_13, PHY_ATTRIB_14, PHY_ATTRIB_15, PHY_ATTRIB_16, PHY_ATTRIB_17, PHY_ATTRIB_18, "
					+ "PHY_ATTRIB_19, PHY_ATTRIB_20, PHY_ATTRIB_21, PHY_ATTRIB_22, PHY_ATTRIB_23, PHY_ATTRIB_24 from item_type where item_type = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, itemType);
			rs = pstmt.executeQuery();
			
			while (rs.next()) {
				phyAttrOne = checkNullAndTrim(rs.getString("PHY_ATTRIB_1"));
				phyAttrTwo = checkNullAndTrim(rs.getString("PHY_ATTRIB_2"));
				phyAttrThree = checkNullAndTrim(rs.getString("PHY_ATTRIB_3"));
				phyAttrFour = checkNullAndTrim(rs.getString("PHY_ATTRIB_4"));
				phyAttrFive = checkNullAndTrim(rs.getString("PHY_ATTRIB_5"));
				phyAttrSix = checkNullAndTrim(rs.getString("PHY_ATTRIB_6"));
				phyAttrSeven = checkNullAndTrim(rs.getString("PHY_ATTRIB_7"));
				phyAttrEight = checkNullAndTrim(rs.getString("PHY_ATTRIB_8"));
				phyAttrNine = checkNullAndTrim(rs.getString("PHY_ATTRIB_9"));
				phyAttrTen = checkNullAndTrim(rs.getString("PHY_ATTRIB_10"));
				phyAttrEleven = checkNullAndTrim(rs.getString("PHY_ATTRIB_11"));
				phyAttrTwelve = checkNullAndTrim(rs.getString("PHY_ATTRIB_12"));
				phyAttrThirt = checkNullAndTrim(rs.getString("PHY_ATTRIB_13"));
				phyAttrFourt = checkNullAndTrim(rs.getString("PHY_ATTRIB_14"));
				phyAttrFift = checkNullAndTrim(rs.getString("PHY_ATTRIB_15"));
				phyAttrSixt = checkNullAndTrim(rs.getString("PHY_ATTRIB_16"));
				phyAttrSevent = checkNullAndTrim(rs.getString("PHY_ATTRIB_17"));
				phyAttrEighte = checkNullAndTrim(rs.getString("PHY_ATTRIB_18"));
				phyAttrNinet = checkNullAndTrim(rs.getString("PHY_ATTRIB_19"));
				phyAttrTwenty = checkNullAndTrim(rs.getString("PHY_ATTRIB_20"));
				phyAttrTwentyOne = checkNullAndTrim(rs.getString("PHY_ATTRIB_21"));
				phyAttrTwentyTwo = checkNullAndTrim(rs.getString("PHY_ATTRIB_22"));
				phyAttrTwentyThree = checkNullAndTrim(rs.getString("PHY_ATTRIB_23"));
				phyAttrTwentyFour = checkNullAndTrim(rs.getString("PHY_ATTRIB_24"));

			}
			//Add by Ajay on 21/02/18:START
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			//End
			if ((phyAttrOne != null) && phyAttrOne.trim().length() > 0) {
				valueXmlString.append("<phy_attrib__lab1><![CDATA[" + phyAttrOne + ":" + "]]></phy_attrib__lab1>");
				valueXmlString.append("<phy_attrib_1  protect = \"0\"><![CDATA[" + phyAttrOne + "]]></phy_attrib_1>");
			} else {
				valueXmlString.append("<phy_attrib__lab1><![CDATA[Phy Attrib1 :]]></phy_attrib__lab1>");
				valueXmlString.append("<phy_attrib_1  protect = \"1\"><![CDATA[" + phyAttrOne + "]]></phy_attrib_1>");
			}

			if ((phyAttrTwo != null) && phyAttrTwo.trim().length() > 0) {
				valueXmlString.append("<phy_attrib__lab2><![CDATA[" + phyAttrTwo + ":" + "]]></phy_attrib__lab2>");
				valueXmlString.append("<phy_attrib_2  protect = \"0\"><![CDATA[" + phyAttrTwo + "]]></phy_attrib_2>");
			} else {
				valueXmlString.append("<phy_attrib__lab2><![CDATA[Phy Attrib2 :]]></phy_attrib__lab2>");
				valueXmlString.append("<phy_attrib_2  protect = \"1\"><![CDATA[" + phyAttrTwo + "]]></phy_attrib_2>");
			}
			if ((phyAttrThree != null) && phyAttrThree.trim().length() > 0) {
				valueXmlString.append("<phy_attrib__lab3><![CDATA[" + phyAttrThree + ":" + "]]></phy_attrib__lab3>");
				valueXmlString.append("<phy_attrib_3  protect = \"0\"><![CDATA[" + phyAttrThree + "]]></phy_attrib_3>");
			} else {
				valueXmlString.append("<phy_attrib__lab3><![CDATA[Phy Attrib3 :]]></phy_attrib__lab3>");
				valueXmlString.append("<phy_attrib_3  protect = \"1\"><![CDATA[" + phyAttrThree + "]]></phy_attrib_3>");
			}
			if ((phyAttrFour != null) && phyAttrFour.trim().length() > 0) {
				valueXmlString.append("<phy_attrib__lab4><![CDATA[" + phyAttrFour + ":" + "]]></phy_attrib__lab4>");
				valueXmlString.append("<phy_attrib_4  protect = \"0\"><![CDATA[" + phyAttrFour + "]]></phy_attrib_4>");
			} else {
				valueXmlString.append("<phy_attrib__lab4><![CDATA[Phy Attrib4 :]]></phy_attrib__lab4>");
				valueXmlString.append("<phy_attrib_4  protect = \"1\"><![CDATA[" + phyAttrFour + "]]></phy_attrib_4>");
			}
			if ((phyAttrFive != null) && phyAttrFive.trim().length() > 0) {
				valueXmlString.append("<phy_attrib__lab5><![CDATA[" + phyAttrFive + ":" + "]]></phy_attrib__lab5>");
				valueXmlString.append("<phy_attrib_5  protect = \"0\"><![CDATA[" + phyAttrFive + "]]></phy_attrib_5>");
			} else {
				valueXmlString.append("<phy_attrib__lab5><![CDATA[Phy Attrib5 :]]></phy_attrib__lab5>");
				valueXmlString.append("<phy_attrib_5  protect = \"1\"><![CDATA[" + phyAttrFive + "]]></phy_attrib_5>");
			}
			if ((phyAttrSix != null) && phyAttrSix.trim().length() > 0) {
				valueXmlString.append("<phy_attrib__lab6><![CDATA[" + phyAttrSix + ":" + "]]></phy_attrib__lab6>");
				valueXmlString.append("<phy_attrib_6  protect = \"0\"><![CDATA[" + phyAttrSix + "]]></phy_attrib_6>");
			} else {
				valueXmlString.append("<phy_attrib__lab6><![CDATA[Phy Attrib6 :]]></phy_attrib__lab6>");
				valueXmlString.append("<phy_attrib_6  protect = \"1\"><![CDATA[" + phyAttrSix + "]]></phy_attrib_6>");
			}
			if ((phyAttrSeven != null) && phyAttrSeven.trim().length() > 0) {
				valueXmlString.append("<phy_attrib__lab7><![CDATA[" + phyAttrSeven + ":" + "]]></phy_attrib__lab7>");
				valueXmlString.append("<phy_attrib_7  protect = \"0\"><![CDATA[" + phyAttrSeven + "]]></phy_attrib_7>");
			} else {
				valueXmlString.append("<phy_attrib__lab7><![CDATA[Phy Attrib7 :]]></phy_attrib__lab7>");
				valueXmlString.append("<phy_attrib_7  protect = \"1\"><![CDATA[" + phyAttrSeven + "]]></phy_attrib_7>");
			}
			if ((phyAttrEight != null) && phyAttrEight.trim().length() > 0) {
				valueXmlString.append("<phy_attrib__lab8><![CDATA[" + phyAttrEight + ":" + "]]></phy_attrib__lab8>");
				valueXmlString.append("<phy_attrib_8  protect = \"0\"><![CDATA[" + phyAttrEight + "]]></phy_attrib_8>");
			} else {
				valueXmlString.append("<phy_attrib__lab8><![CDATA[Phy Attrib8 :]]></phy_attrib__lab8>");
				valueXmlString.append("<phy_attrib_8  protect = \"1\"><![CDATA[" + phyAttrEight + "]]></phy_attrib_8>");
			}
			if ((phyAttrNine != null) && phyAttrNine.trim().length() > 0) {
				valueXmlString.append("<phy_attrib__lab9><![CDATA[" + phyAttrNine + ":" + "]]></phy_attrib__lab9>");
				valueXmlString.append("<phy_attrib_9  protect = \"0\"><![CDATA[" + phyAttrNine + "]]></phy_attrib_9>");
			} else {
				valueXmlString.append("<phy_attrib__lab9><![CDATA[Phy Attrib9 :]]></phy_attrib__lab9>");
				valueXmlString.append("<phy_attrib_9  protect = \"1\"><![CDATA[" + phyAttrNine + "]]></phy_attrib_9>");
			}
			if ((phyAttrTen != null) && phyAttrTen.trim().length() > 0) {
				valueXmlString.append("<phy_attrib__lab10><![CDATA[" + phyAttrTen + ":" + "]]></phy_attrib__lab10>");
				valueXmlString.append("<phy_attrib_10  protect = \"0\"><![CDATA[" + phyAttrTen + "]]></phy_attrib_10>");
			} else {
				valueXmlString.append("<phy_attrib__lab10><![CDATA[Phy Attrib10 :]]></phy_attrib__lab10>");
				valueXmlString.append("<phy_attrib_10  protect = \"1\"><![CDATA[" + phyAttrTen + "]]></phy_attrib_10>");
			}
			if ((phyAttrEleven != null) && phyAttrEleven.trim().length() > 0) {
				valueXmlString.append("<phy_attrib__lab11><![CDATA[" + phyAttrEleven + ":" + "]]></phy_attrib__lab11>");
				valueXmlString.append("<phy_attrib_11  protect = \"0\"><![CDATA[" + phyAttrEleven + "]]></phy_attrib_11>");
			} else {
				valueXmlString.append("<phy_attrib__lab11><![CDATA[Phy Attrib11 :]]></phy_attrib__lab11>");
				valueXmlString.append("<phy_attrib_11  protect = \"1\"><![CDATA[" + phyAttrEleven + "]]></phy_attrib_11>");
			}
			if ((phyAttrTwelve != null) && phyAttrTwelve.trim().length() > 0) {
				valueXmlString.append("<phy_attrib__lab12><![CDATA[" + phyAttrTwelve + ":" + "]]></phy_attrib__lab12>");
				valueXmlString.append("<phy_attrib_12  protect = \"0\"><![CDATA[" + phyAttrTwelve + "]]></phy_attrib_12>");
			} else {
				valueXmlString.append("<phy_attrib__lab12><![CDATA[Phy Attrib12 :]]></phy_attrib__lab12>");
				valueXmlString.append("<phy_attrib_12  protect = \"1\"><![CDATA[" + phyAttrTwelve + "]]></phy_attrib_12>");
			}
			if ((phyAttrThirt != null) && phyAttrThirt.trim().length() > 0) {
				valueXmlString.append("<phy_attrib__lab13><![CDATA[" + phyAttrThirt + ":" + "]]></phy_attrib__lab13>");
				valueXmlString.append("<phy_attrib_13  protect = \"0\"><![CDATA[" + phyAttrThirt + "]]></phy_attrib_13>");
			} else {
				valueXmlString.append("<phy_attrib__lab13><![CDATA[Phy Attrib13 :]]></phy_attrib__lab13>");
				valueXmlString.append("<phy_attrib_13  protect = \"1\"><![CDATA[" + phyAttrThirt + "]]></phy_attrib_13>");
			}
			if ((phyAttrFourt != null) && phyAttrFourt.trim().length() > 0) {
				valueXmlString.append("<phy_attrib__lab14><![CDATA[" + phyAttrFourt + ":" + "]]></phy_attrib__lab14>");
				valueXmlString.append("<phy_attrib_14  protect = \"0\"><![CDATA[" + phyAttrFourt + "]]></phy_attrib_14>");
			} else {
				valueXmlString.append("<phy_attrib__lab14><![CDATA[Phy Attrib14 :]]></phy_attrib__lab14>");
				valueXmlString.append("<phy_attrib_14  protect = \"1\"><![CDATA[" + phyAttrFour + "]]></phy_attrib_14>");
			}
			if ((phyAttrFift != null) && phyAttrFift.trim().length() > 0) {
				valueXmlString.append("<phy_attrib__lab15><![CDATA[" + phyAttrFift + ":" + "]]></phy_attrib__lab15>");
				valueXmlString.append("<phy_attrib_15  protect = \"0\"><![CDATA[" + phyAttrFift + "]]></phy_attrib_15>");
			} else {
				valueXmlString.append("<phy_attrib__lab15><![CDATA[Phy Attrib15 :]]></phy_attrib__lab15>");
				valueXmlString.append("<phy_attrib_15  protect = \"1\"><![CDATA[" + phyAttrFift + "]]></phy_attrib_15>");
			}
			if ((phyAttrSixt != null) && phyAttrSixt.trim().length() > 0) {
				valueXmlString.append("<phy_attrib__lab16><![CDATA[" + phyAttrSixt + ":" + "]]></phy_attrib__lab16>");
				valueXmlString.append("<phy_attrib_16  protect = \"0\"><![CDATA[" + phyAttrSixt + "]]></phy_attrib_16>");
			} else {
				valueXmlString.append("<phy_attrib__lab16><![CDATA[Phy Attrib16 :]]></phy_attrib__lab16>");
				valueXmlString.append("<phy_attrib_16  protect = \"1\"><![CDATA[" + phyAttrSixt + "]]></phy_attrib_16>");
			}
			if ((phyAttrSevent != null) && phyAttrSevent.trim().length() > 0) {
				valueXmlString.append("<phy_attrib__lab17><![CDATA[" + phyAttrSevent + ":" + "]]></phy_attrib__lab17>");
				valueXmlString.append("<phy_attrib_17  protect = \"0\"><![CDATA[" + phyAttrSevent + "]]></phy_attrib_17>");
			} else {
				valueXmlString.append("<phy_attrib__lab17><![CDATA[Phy Attrib17 :]]></phy_attrib__lab17>");
				valueXmlString.append("<phy_attrib_17  protect = \"1\"><![CDATA[" + phyAttrSevent + "]]></phy_attrib_17>");
			}
			if ((phyAttrEighte != null) && phyAttrEighte.trim().length() > 0) {
				valueXmlString.append("<phy_attrib__lab18><![CDATA[" + phyAttrEighte + ":" + "]]></phy_attrib__lab18>");
				valueXmlString.append("<phy_attrib_18  protect = \"0\"><![CDATA[" + phyAttrEighte + "]]></phy_attrib_18>");
			} else {
				valueXmlString.append("<phy_attrib__lab18><![CDATA[Phy Attrib18 :]]></phy_attrib__lab18>");
				valueXmlString.append("<phy_attrib_18  protect = \"1\"><![CDATA[" + phyAttrEighte + "]]></phy_attrib_18>");
			}
			if ((phyAttrNinet != null) && phyAttrNinet.trim().length() > 0) {
				valueXmlString.append("<phy_attrib__lab19><![CDATA[" + phyAttrNinet + ":" + "]]></phy_attrib__lab19>");
				valueXmlString.append("<phy_attrib_19  protect = \"0\"><![CDATA[" + phyAttrOne + "]]></phy_attrib_19>");
			} else {
				valueXmlString.append("<phy_attrib__lab19><![CDATA[Phy Attrib19 :]]></phy_attrib__lab19>");
				valueXmlString.append("<phy_attrib_19  protect = \"1\"><![CDATA[" + phyAttrNinet + "]]></phy_attrib_19>");
			}
			if ((phyAttrTwenty != null) && phyAttrTwenty.trim().length() > 0) {
				valueXmlString.append("<phy_attrib__lab20><![CDATA["+ phyAttrTwenty + ":" + "]]></phy_attrib__lab20>");
				valueXmlString.append("<phy_attrib_20  protect = \"0\"><![CDATA[" + phyAttrTwenty + "]]></phy_attrib_20>");
			} else {
				valueXmlString.append("<phy_attrib__lab20><![CDATA[Phy Attrib20 :]]></phy_attrib__lab20>");
				valueXmlString.append("<phy_attrib_20  protect = \"1\"><![CDATA[" + phyAttrTwenty + "]]></phy_attrib_20>");
			}
			if ((phyAttrTwentyOne != null) && phyAttrTwentyOne.trim().length() > 0) {
				valueXmlString.append("<phy_attrib__lab21><![CDATA[" + phyAttrTwentyOne + ":" + "]]></phy_attrib__lab21>");
				valueXmlString.append("<phy_attrib_21  protect = \"0\"><![CDATA[" + phyAttrTwentyOne + "]]></phy_attrib_21>");
			} else {
				valueXmlString.append("<phy_attrib__lab21><![CDATA[Phy Attrib21 :]]></phy_attrib__lab21>");
				valueXmlString.append("<phy_attrib_21  protect = \"1\"><![CDATA[" + phyAttrTwentyOne + "]]></phy_attrib_21>");
			}
			if ((phyAttrTwentyTwo != null) && phyAttrTwentyTwo.trim().length() > 0) {
				valueXmlString.append("<phy_attrib__lab22><![CDATA[" + phyAttrTwentyTwo + ":" + "]]></phy_attrib__lab22>");
				valueXmlString.append("<phy_attrib_22  protect = \"0\"><![CDATA[" + phyAttrTwentyTwo + "]]></phy_attrib_22>");
			} else {
				valueXmlString.append("<phy_attrib__lab22><![CDATA[Phy Attrib22 :]]></phy_attrib__lab22>");
				valueXmlString.append("<phy_attrib_22  protect = \"1\"><![CDATA[" + phyAttrTwentyTwo + "]]></phy_attrib_22>");
			}
			if ((phyAttrTwentyThree != null) && phyAttrTwentyThree.trim().length() > 0) {
				valueXmlString.append("<phy_attrib__lab23><![CDATA[" + phyAttrTwentyThree + ":" + "]]></phy_attrib__lab23>");
				valueXmlString.append("<phy_attrib_23  protect = \"0\"><![CDATA[" + phyAttrTwentyThree + "]]></phy_attrib_23>");
			} else {
				valueXmlString.append("<phy_attrib__lab23><![CDATA[Phy Attrib23 :]]></phy_attrib__lab23>");
				valueXmlString.append("<phy_attrib_23  protect = \"1\"><![CDATA[" + phyAttrTwentyThree + "]]></phy_attrib_23>");
			}
			if ((phyAttrTwentyFour != null) && phyAttrTwentyFour.trim().length() > 0) {
				valueXmlString.append("<phy_attrib__lab24><![CDATA[" + phyAttrTwentyFour + ":" + "]]></phy_attrib__lab24>");
				valueXmlString.append("<phy_attrib_24  protect = \"0\"><![CDATA[" + phyAttrTwentyFour + "]]></phy_attrib_24>");
			} else {
				valueXmlString.append("<phy_attrib__lab24><![CDATA[Phy Attrib24 :]]></phy_attrib__lab24>");
				valueXmlString.append("<phy_attrib_24  protect = \"1\"><![CDATA[" + phyAttrTwentyFour + "]]></phy_attrib_24>");
			}
		}
		
		catch (Exception e) {
			System.out.println("::" + this.getClass().getSimpleName() + "::::" + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				if (conn != null)
					conn.close();
				conn = null;
				
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

	}
	
	private String setDescr(String itemType, String phyAttribThree, String phyAttribFour, Connection conn) {
		String sql = "", lsItemType = "", descrAttr = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			if (itemType.length() > 0 && phyAttribThree.length() > 0 && phyAttribFour.length() > 0) {
				lsItemType = itemType.trim() + phyAttribThree.trim() + phyAttribFour.trim();
				System.out.println(":::lsItemType " + lsItemType);
			}
			sql = "select descr from gencodes where fld_name = 'CAS_NO' and mod_name = 'X' and rtrim(fld_value) = ?  and active = 'Y'";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, lsItemType);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				descrAttr = checkNullAndTrim(rs.getString("descr"));
			}
			//Add by Ajay on 21/02/18:START
			rs.close();
			pstmt.close();
            //End
			System.out.println("descrAttr::: " + descrAttr);
		} catch (Exception e) {
			System.out.println(":::" + this.getClass().getSimpleName() + ":::"
					+ e.getMessage());
			e.printStackTrace();
		}
		return descrAttr;
	}
}
