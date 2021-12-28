package ibase.webitm.ejb.dis;


import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

public class TaxEnvIC extends ValidatorEJB implements TaxEnvICRemote, TaxEnvICLocal {

	//changed by nasruddin 05-10-16
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility = new E12GenericUtility();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();

	@Override
	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag,String xtraParams)
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
	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams)
			throws RemoteException, ITMException {

		String errString = "";
		String sql = "", sql1 = "", sql2 = "";
		Connection conn = null;
		String userId = "";
		String acctCode = "", acctCodeRev = "";
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
					if (childNodeName.equalsIgnoreCase("tax_env"))
					{
						System.out.println(":::childNodeName" + childNodeName);
						String taxEnv = genericUtility.getColumnValue("tax_env", dom);
						taxEnv = taxEnv == null ? "" : taxEnv.trim();
						System.out.println("::: taxEnv" + taxEnv);
						if(taxEnv.length() <= 0)
						{
							errString = itmDBAccessEJB.getErrorString("tax_env", "VMTAXENV", userId,"",conn);
							break;
						}
						/* Comment By Nasruddin 21-sep16 Start
						 * else
					{
						if(editFlag.equalsIgnoreCase("A")){
						int count = 0;
						sql = "select count(*) as count from taxenv where tax_env = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, taxEnv);
						rs = pstmt.executeQuery();
						if(rs.next()){
							count = rs.getInt("count");
						}
						if(count > 0){
							errString = itmDBAccessEJB.getErrorString("tax_env", "VTDUPREC", userId,"",conn); 
							break;
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						}
					}Comment By Nasruddin 21-sep16 End*/
					}
					if(childNodeName.equalsIgnoreCase("descr"))
					{
						String descr = genericUtility.getColumnValue("descr", dom);
						descr = descr == null ? "" : descr.trim();
						System.out.println("::: descr" + descr);
						if(descr.length() <= 0)
						{
							errString = itmDBAccessEJB.getErrorString("descr", "VMDESCR", userId,"",conn);
							break;
						}
					}
					if(childNodeName.equalsIgnoreCase("site_code"))
					{
						String siteCode = genericUtility.getColumnValue("site_code", dom);
						siteCode = siteCode == null ? "" : siteCode.trim();
						if(siteCode.length() > 0)
						{
							int count = 0;
							sql = "select count(*) as count from site where site_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, siteCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count = rs.getInt("count");
							}
							if(count <= 0){
								errString = itmDBAccessEJB.getErrorString("site_code", "VMSITE1", userId,"",conn); 
								break;
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					if(childNodeName.equalsIgnoreCase("ref_ser"))
					{
						String refSer = genericUtility.getColumnValue("ref_ser", dom);
						refSer = refSer == null ? "" : refSer.trim();
						if(refSer.length() > 0)
						{
							int count = 0;
							sql = "select count(*) as count from refser where ref_ser = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, refSer);
							rs = pstmt.executeQuery();
							if(rs.next()){
								count = rs.getInt("count");
							}
							if(count <= 0){
								errString = itmDBAccessEJB.getErrorString("ref_ser", "VTREFSER5", userId,"",conn); 
								break;
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}

				}
				break;
			case 2:
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeLength = childNodeList.getLength();
				for (ctr = 0; ctr < childNodeLength; ctr++) {
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if(childNodeName.equalsIgnoreCase("stan_code__fr"))
					{
						String stanCodeFor = genericUtility.getColumnValue("stan_code__fr", dom);
						stanCodeFor = stanCodeFor == null ? "" : stanCodeFor.trim();
						/*
						 * Comment By Nasruddin 21-SEp-16 Start
					if(stanCodeFor.length() <= 0)
					{
						errString = itmDBAccessEJB.getErrorString("stan_code__fr", "VTSTAN2", userId,"",conn);
						break;
					}
					 Comment By Nasruddin 21-SEp-16 END*/
						//Changed By Nasruddin 21-SEP-16
						if(stanCodeFor.length() > 0)
						{
							int count = 0;
							sql = "select count(*) as count from station where stan_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, stanCodeFor);
							rs = pstmt.executeQuery();
							if(rs.next()){
								count = rs.getInt("count");
							}
							if(count <= 0){
								errString = itmDBAccessEJB.getErrorString("stan_code__fr", "VMSTANINVD", userId,"",conn); 
								break;
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}

					if(childNodeName.equalsIgnoreCase("stan_code__to"))
					{
						String stanCodeTo = genericUtility.getColumnValue("stan_code__to", dom);
						//String stanCodeTo = childNode.getFirstChild().getNodeValue();
						stanCodeTo = stanCodeTo == null ? "" : stanCodeTo.trim();
						System.out.println("::: stanCodeTo " + stanCodeTo);
						/*
						 * Comment By Nasruddin 21-SEp-16 Start
					if(stanCodeTo.length() <= 0){
						errString = itmDBAccessEJB.getErrorString("stan_code__to", "VTSTAN3", userId,"",conn);
						break;
					}
					 Comment By Nasruddin 21-SEp-16 END*/
						//Changed By Nasruddin 21-SEP-16
						if(stanCodeTo.length() > 0)
						{
							int count = 0;
							sql1 = "select count(*) as count from station where stan_code = ?";
							pstmt = conn.prepareStatement(sql1);
							pstmt.setString(1, stanCodeTo);
							rs = pstmt.executeQuery();
							if(rs.next()){
								count = rs.getInt("count");
							}
							if(count <= 0){
								errString = itmDBAccessEJB.getErrorString("stan_code__to", "VTSTANTOT", userId,"",conn); 
								break;
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					if(childNodeName.equalsIgnoreCase("tax_class"))
					{
						String taxClass = genericUtility.getColumnValue("tax_class", dom);
						taxClass = taxClass == null ? "" : taxClass.trim();
						int count = 0;
						//Changed By Nasruddin 21-SEP-16
						if(taxClass.length() > 0)
						{
							sql = "select count(*) as count from taxclass where tax_class = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, taxClass);
							rs = pstmt.executeQuery();
							if(rs.next()){
								count = rs.getInt("count");
							}
							if(count <= 0)
							{
								errString = itmDBAccessEJB.getErrorString("tax_class", "VTTCLASS1", userId,"",conn); 
								break;
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					if(childNodeName.equalsIgnoreCase("tax_chap"))
					{

						String taxChap = genericUtility.getColumnValue("tax_chap", dom);
						taxChap = taxChap == null ? "" : taxChap.trim();
						int count = 0;
						//Changed By Nasruddin 21-SEP-16
						if(taxChap.length() > 0)
						{
							sql = "select count(*) as count from taxchap where tax_chap = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, taxChap);
							rs = pstmt.executeQuery();
							if(rs.next()){
								count = rs.getInt("count");
							}
							if(count <= 0){
								errString = itmDBAccessEJB.getErrorString("tax_chap", "VTTCHAP1", userId,"",conn); 
								break;
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					//Changed By Nasruddin 21-SEP-16 Start
					if(childNodeName.equalsIgnoreCase("state_code__to"))
					{
						String stateCodeTo = genericUtility.getColumnValue("state_code__to", dom);
						stateCodeTo = stateCodeTo == null ? "" : stateCodeTo.trim();
						System.out.println("::: stateCodeTo " + stateCodeTo);

						if(stateCodeTo.length() > 0)
						{
							int count = 0;
							sql1 = "select count(*) as count from state where state_code = ?";
							pstmt = conn.prepareStatement(sql1);
							pstmt.setString(1, stateCodeTo);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count = rs.getInt("count");
							}
							if(count == 0)
							{
								errString = itmDBAccessEJB.getErrorString("state_code__to", "VMSTATCD2", userId,"",conn); 
								break;
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					if(childNodeName.equalsIgnoreCase("state_code__fr"))
					{
						String stateCodeFrom = genericUtility.getColumnValue("state_code__fr", dom);
						stateCodeFrom = stateCodeFrom == null ? "" : stateCodeFrom.trim();
						System.out.println("::: stateCodeTo " + stateCodeFrom);

						if(stateCodeFrom.length() > 0)
						{
							int count = 0;
							sql1 = "select count(*) as count from state where state_code = ?";
							pstmt = conn.prepareStatement(sql1);
							pstmt.setString(1, stateCodeFrom);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count = rs.getInt("count");
							}
							if(count == 0)
							{
								errString = itmDBAccessEJB.getErrorString("state_code__fr", "VMSTATCD2", userId,"",conn); 
								break;
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					if (childNodeName.equalsIgnoreCase("tax_env"))
					{
						System.out.println(":::childNodeName" + childNodeName);
						String taxEnv = genericUtility.getColumnValue("tax_env", dom);
						if( taxEnv == null || taxEnv.trim().length() > 0)
						{
							errString = itmDBAccessEJB.getErrorString("ref_ser", "VTREFSER5", userId,"",conn); 
							break;
						}
					}
					//Changed By Nasruddin 21-SEP-16 END
				}
				break;
			case 3:
				parentNodeList = dom.getElementsByTagName("Detail3");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeLength = childNodeList.getLength();
				for (ctr = 0; ctr < childNodeLength; ctr++) {
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					//Changed By Nasruddin 21-SEP-16 Start
					if (childNodeName.equalsIgnoreCase("tax_env"))
					{
						System.out.println(":::childNodeName" + childNodeName);
						String taxEnv = genericUtility.getColumnValue("tax_env", dom);
						if( taxEnv == null || taxEnv.trim().length() > 0)
						{
							errString = itmDBAccessEJB.getErrorString("ref_ser", "VTREFSER5", userId,"",conn); 
							break;
						}
					}
					//Changed By Nasruddin 21-SEP-16 END
					if(childNodeName.equalsIgnoreCase("ref_ser"))
					{
						String refSer1 = genericUtility.getColumnValue("ref_ser", dom);
						//String refSer = childNode.getFirstChild().getNodeValue();
						refSer1 = refSer1 == null ? "" : refSer1.trim();
						System.out.println("::: refSer1" + refSer1);

						int count = 0;
						//Changed By Nasruddin 21-SEP-16 
						if(refSer1.length() > 0)
						{
							sql2 = "select count(*) as count from refser where ref_ser = ?";
							pstmt = conn.prepareStatement(sql2);
							pstmt.setString(1, refSer1);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count = rs.getInt("count");
							}
							if(count <= 0)
							{
								errString = itmDBAccessEJB.getErrorString("ref_ser", "VTREFSER5", userId,"",conn); 
								break;
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					if(childNodeName.equalsIgnoreCase("seq_no"))
					{
						String seqNo = genericUtility.getColumnValue("seq_no", dom);
						seqNo = seqNo == null ? "" : seqNo.trim();
						if(seqNo.length() <= 0)
						{
							errString = itmDBAccessEJB.getErrorString("seq_no", "VMSEQNO", userId,"",conn); 
							break;
						}
						if(!seqNo.matches("\\d+"))
						{
							errString = itmDBAccessEJB.getErrorString("seq_no", "VTSEQINV", userId,"",conn);
						}
					}
					if(childNodeName.equalsIgnoreCase("tax_code"))
					{
						String taxCode = genericUtility.getColumnValue("tax_code", dom);
						taxCode = taxCode == null ? "" : taxCode.trim();
						/*	Comment By Nasrruddin Start 21-sep-16
				   if(taxCode.length() <= 0){
						errString = itmDBAccessEJB.getErrorString("tax_code", "VTTAXC", userId,"",conn);
						break;
					}
					else 
					{Comment By Nasrruddin 21-SEP-16 End*/
						int count = 0;
						sql = "select count(*) as count from tax where tax_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, taxCode);
						rs = pstmt.executeQuery();
						if(rs.next()){
							count = rs.getInt("count");
						}
						if(count <= 0){
							errString = itmDBAccessEJB.getErrorString("tax_code", "VTTAX1", userId,"",conn); 
							break;
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						//}
					}
					if(childNodeName.equalsIgnoreCase("acct_code"))
					{
						acctCode = genericUtility.getColumnValue("acct_code", dom);
						acctCode = acctCode == null ? "" : acctCode.trim();
						/*if(acctCode.length() <= 0){
						errString = itmDBAccessEJB.getErrorString("acct_code", "", userId,"",conn);//TODO
						break;
					}*/
						if(acctCode.length() > 0){
							String returnVal = acctVal("", acctCode, xtraParams, conn);
							if(!returnVal.equalsIgnoreCase("CONFIRMED")){
								errString = itmDBAccessEJB.getErrorString("acct_code", returnVal, userId,"",conn);
								break;
							}
						}
					}
					if(childNodeName.equalsIgnoreCase("acct_code__revr")){
						acctCodeRev = genericUtility.getColumnValue("acct_code__revr", dom);
						acctCodeRev = acctCodeRev == null ? "" : acctCodeRev.trim();
						/*if(acctCodeRev.length() <= 0){
						errString = itmDBAccessEJB.getErrorString("acct_code__revr", "", userId,"",conn); //TODO
						break;
					}*/
						if(acctCodeRev.length() > 0){
							String returnVal1 = acctVal("", acctCodeRev, xtraParams, conn);
							if(!returnVal1.equalsIgnoreCase("CONFIRMED")){
								errString = itmDBAccessEJB.getErrorString("acct_code__revr", returnVal1, userId,"",conn);
								break;
							}
						}
					}
					if(childNodeName.equalsIgnoreCase("cctr_code")){
						String cctrCode = genericUtility.getColumnValue("cctr_code", dom);
						cctrCode = cctrCode == null ? "" : cctrCode.trim();
						/*if(cctrCode.length() <= 0){
						errString = itmDBAccessEJB.getErrorString("cctr_code", "", userId,"",conn);//TODO
						break;
					}*/
						if(cctrCode.length() > 0){
							String result = cctrVal(cctrCode, acctCode, xtraParams, conn);
							if(!result.equalsIgnoreCase("CONFIRMED")){
								errString = itmDBAccessEJB.getErrorString("", result, userId,"",conn);
								break;
							}
						}
					}
					if(childNodeName.equalsIgnoreCase("cctr_code__revr")){
						String cctrCodeRev = genericUtility.getColumnValue("cctr_code__revr", dom);
						cctrCodeRev = cctrCodeRev == null ? "" : cctrCodeRev.trim();
						/*if(cctrCodeRev.length() <= 0){
						errString = itmDBAccessEJB.getErrorString("cctr_code__revr", "", userId,"",conn);//TODO
						break;
					}*/
						if(cctrCodeRev.length() > 0){
							String result1 = cctrVal(cctrCodeRev, acctCodeRev, xtraParams, conn);
							if(!result1.equalsIgnoreCase("CONFIRMED")){
								errString = itmDBAccessEJB.getErrorString("", result1, userId,"",conn);
								break;
							}
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			System.out.println(":::" + this.getClass().getSimpleName() + ":::" + e.getMessage());
			e.printStackTrace();
		}
		// Changed By Nasruddin 21-SEP-16 STart
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
		// Changed By Nasruddin 21-SEP-16 END
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
		String taxEnv = "", stanCodeFrom = "", sql = "", stateCodeFrom = "", taxCode = "", descr = "", stanCodeTo = "";
		String stateCodeTo = "" , taxClass = "", taxChap = "", seqNo = "";
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
					if (currentColumn.equalsIgnoreCase("tax_env")){
						taxEnv = genericUtility.getColumnValue("tax_env", dom);
						System.out.println(":::: taxEnv" + taxEnv);
						taxEnv = taxEnv == null ? "" : taxEnv.trim();	
					}
					if (currentColumn.equalsIgnoreCase("itm_default")) {
						System.out.println("itm_default called::::");
						valueXmlString.append("<tax_env><![CDATA["+ taxEnv + "]]></tax_env>");
					}
					valueXmlString.append("</Detail1>\r\n");
					break;
				}
			case 2 :
				valueXmlString.append("<Detail2>");
				System.out.println("currentColumn: " + currentColumn);
				if (currentColumn != null) {
					if (currentColumn.equalsIgnoreCase("stan_code__fr")){
						stanCodeFrom = genericUtility.getColumnValue("stan_code__fr", dom);
						System.out.println(":::: stanCodeFrom" + stanCodeFrom);
						stanCodeFrom = stanCodeFrom == null ? "" : stanCodeFrom.trim();	
						try {
							if(stanCodeFrom.length() > 0){
								sql = "select state_code from station where stan_code = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, stanCodeFrom);
								rs = pstmt.executeQuery();

								if(rs.next()){
									stateCodeFrom = rs.getString("state_code");
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
						System.out.println("stanCodeFrom called::::");
						valueXmlString.append("<state_code__fr><![CDATA["+ stateCodeFrom + "]]></state_code__fr>");
					}

					if (currentColumn.equalsIgnoreCase("stan_code__to")){
						stanCodeTo = genericUtility.getColumnValue("stan_code__to", dom);
						System.out.println(":::: stanCodeTo" + stanCodeTo);
						stanCodeTo = stanCodeTo == null ? "" : stanCodeTo.trim();	
						try {
							if(stanCodeTo.length() > 0){
								sql = "select state_code from station where stan_code = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, stanCodeTo);
								rs = pstmt.executeQuery();

								if(rs.next()){
									stateCodeTo = rs.getString("state_code");
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
						System.out.println("stanCodeTo called::::");
						valueXmlString.append("<state_code__to><![CDATA["+ stateCodeTo + "]]></state_code__to>");
					}
					/*					if(currentColumn.equalsIgnoreCase("itm_defaultedit")){
						System.out.println(":::In itm_defaultedit :::");

						stanCodeFrom = genericUtility.getColumnValue("stan_code__fr", dom);
						 System.out.println(":::: stanCodeFrom" + stanCodeFrom);
						 stanCodeFrom = stanCodeFrom == null ? "" : stanCodeFrom.trim();	

						 stanCodeTo = genericUtility.getColumnValue("stan_code__to", dom);
						 System.out.println(":::: stanCodeTo" + stanCodeTo);
						 stanCodeTo = stanCodeTo == null ? "" : stanCodeTo.trim();

						 stateCodeFrom = genericUtility.getColumnValue("state_code__fr", dom);
						 System.out.println(":::: stateCodeFrom" + stateCodeFrom);
						 stateCodeFrom = stateCodeFrom == null ? "" : stateCodeFrom.trim();

						 stateCodeTo = genericUtility.getColumnValue("state_code__to", dom);
						 System.out.println(":::: stateCodeTo" + stateCodeTo);
						 stateCodeTo = stateCodeTo == null ? "" : stateCodeTo.trim();

						 taxClass = genericUtility.getColumnValue("tax_class", dom);
						 System.out.println(":::: taxClass" + taxClass);
						 taxClass = taxClass == null ? "" : taxClass.trim();

						 taxChap = genericUtility.getColumnValue("tax_chap", dom);
						 System.out.println(":::: taxChap" + taxChap);
						 taxChap = taxChap == null ? "" : taxChap.trim();

						 valueXmlString.append("<stan_code__fr  protect = \"1\"><![CDATA["+ stanCodeFrom + "]]></stan_code__fr>");
						 valueXmlString.append("<stan_code__to  protect = \"1\"><![CDATA["+ stanCodeTo + "]]></stan_code__to>");
						 valueXmlString.append("<state_code__fr  protect = \"1\"><![CDATA["+ stateCodeFrom + "]]></state_code__fr>");
						 valueXmlString.append("<state_code__to  protect = \"1\"><![CDATA["+ stateCodeTo + "]]></state_code__to>");
						 valueXmlString.append("<tax_class  protect = \"1\"><![CDATA["+ taxClass + "]]></tax_class>");
						 valueXmlString.append("<tax_chap  protect = \"1\"><![CDATA["+ taxChap + "]]></tax_chap>");

					}
					if(currentColumn.equalsIgnoreCase("itm_default")){

						stanCodeFrom = genericUtility.getColumnValue("stan_code__fr", dom);
						 System.out.println(":::: stanCodeFrom" + stanCodeFrom);
						 stanCodeFrom = stanCodeFrom == null ? "" : stanCodeFrom.trim();	

						 stanCodeTo = genericUtility.getColumnValue("stan_code__to", dom);
						 System.out.println(":::: stanCodeTo" + stanCodeTo);
						 stanCodeTo = stanCodeTo == null ? "" : stanCodeTo.trim();

						 stateCodeFrom = genericUtility.getColumnValue("state_code__fr", dom);
						 System.out.println(":::: stateCodeFrom" + stateCodeFrom);
						 stateCodeFrom = stateCodeFrom == null ? "" : stateCodeFrom.trim();

						 stateCodeTo = genericUtility.getColumnValue("state_code__to", dom);
						 System.out.println(":::: stateCodeTo" + stateCodeTo);
						 stateCodeTo = stateCodeTo == null ? "" : stateCodeTo.trim();

						 taxClass = genericUtility.getColumnValue("tax_class", dom);
						 System.out.println(":::: taxClass" + taxClass);
						 taxClass = taxClass == null ? "" : taxClass.trim();

						 taxChap = genericUtility.getColumnValue("tax_chap", dom);
						 System.out.println(":::: taxChap" + taxChap);
						 taxChap = taxChap == null ? "" : taxChap.trim();

						 valueXmlString.append("<stan_code__fr  protect = \"0\"><![CDATA["+ stanCodeFrom + "]]></stan_code__fr>");
						 valueXmlString.append("<stan_code__to  protect = \"0\"><![CDATA["+ stanCodeTo + "]]></stan_code__to>");
						 valueXmlString.append("<state_code__fr  protect = \"0\"><![CDATA["+ stateCodeFrom + "]]></state_code__fr>");
						 valueXmlString.append("<state_code__to  protect = \"0\"><![CDATA["+ stateCodeTo + "]]></state_code__to>");
						 valueXmlString.append("<tax_class  protect = \"0\"><![CDATA["+ taxClass + "]]></tax_class>");
						 valueXmlString.append("<tax_chap  protect = \"0\"><![CDATA["+ taxChap + "]]></tax_chap>");
					}*/

					System.out.println(":::::generated xml" + valueXmlString.toString());
					valueXmlString.append("</Detail2>\r\n");
					break;
				}
			case 3 :
				valueXmlString.append("<Detail3>");
				System.out.println("currentColumn: " + currentColumn);
				if (currentColumn != null) {
					if (currentColumn.equalsIgnoreCase("tax_code")){
						taxCode = genericUtility.getColumnValue("tax_code", dom);
						System.out.println(":::: taxCode" + taxCode);
						taxCode = taxCode == null ? "" : taxCode.trim();	
						try {
							if(taxCode.length() > 0){
								sql = "select descr from tax where tax_code = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, taxCode);
								rs = pstmt.executeQuery();

								if(rs.next()){
									descr = rs.getString("descr");
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
						System.out.println("tax_code called::::");
						valueXmlString.append("<tax_descr><![CDATA["+ descr + "]]></tax_descr>");
					}
					/*				if (currentColumn.equalsIgnoreCase("itm_defaultedit")){

				taxCode = genericUtility.getColumnValue("tax_code", dom);
				 System.out.println(":::: taxCode" + taxCode);
				 taxCode = taxCode == null ? "" : taxCode.trim();

				 System.out.println("for tax_code ::::");
				valueXmlString.append("<tax_code  protect = \"1\"><![CDATA["+ taxCode + "]]></tax_code>");

				seqNo = genericUtility.getColumnValue("seq_no", dom);
				 System.out.println(":::: seqNo" + seqNo);
				 seqNo = seqNo == null ? "" : seqNo.trim();

				 System.out.println("for seq_no::::");
				 valueXmlString.append("<seq_no  protect = \"1\"><![CDATA["+ seqNo + "]]></seq_no>");
		} if (currentColumn.equalsIgnoreCase("itm_default")){

		taxCode = genericUtility.getColumnValue("tax_code", dom);
		 System.out.println(":::: taxCode" + taxCode);
		 taxCode = taxCode == null ? "" : taxCode.trim();

		 System.out.println("tax_code called::::");
		valueXmlString.append("<tax_code  protect = \"0\"><![CDATA["+ taxCode + "]]></tax_code>");

		seqNo = genericUtility.getColumnValue("seq_no", dom);
		 System.out.println(":::: seqNo" + seqNo);
		 seqNo = seqNo == null ? "" : seqNo.trim();

		 System.out.println("for seq_no::::");
		 valueXmlString.append("<seq_no  protect = \"0\"><![CDATA["+ seqNo + "]]></seq_no>");
}*/

					System.out.println(":::::generated xml" + valueXmlString.toString());
					valueXmlString.append("</Detail3>\r\n");
					break;
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

	public String acctVal(String siteCode, String acctCode, String xtraParams, Connection conn) throws ITMException, RemoteException {
		String sql = "", active = "", errString = "CONFIRMED", userId = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int count = 0;
		try{
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, userId);
			String varValue = "";
			sql = "select var_value from finparm where prd_code = '999999' and var_name = 'SITE_SPECIFIC_ACCT'";
			pstmt = conn.prepareStatement(sql);

			rs = pstmt.executeQuery();

			if (rs.next()) {
				varValue = rs.getString("var_value");
			}
            //Added by sarita on 13NOV2017
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			sql = " select count(*) as COUNT,ACTIVE from accounts where acct_code=? group by active";

			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, acctCode.trim());

			rs = pstmt.executeQuery();

			if (rs.next()) {
				count = rs.getInt("COUNT");
				active = rs.getString("ACTIVE");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			if (count <= 0) {
				errString = "VMACCT1";
				return errString;
			} /*else {
				if (!active.equalsIgnoreCase("Y")) {
					errString =  "VMACCTA";
					return errString;
				} else {
					if (varValue.trim().equalsIgnoreCase("Y")) {

						sql = "select count(*) as COUNT from site_account where site_code = ? and acct_code = ?";

						pstmt = conn.prepareStatement(sql);

						pstmt.setString(1, siteCode.trim());
						pstmt.setString(2, acctCode.trim());

						rs = pstmt.executeQuery();

						if (rs.next()) {
							count = rs.getInt("COUNT");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (count <= 0) {
							errString =  "VMACCT3";
							return errString;
						}
					}
				}
			}*/
		} catch (SQLException e) {
			System.out.println("::" + this.getClass().getSimpleName() + "::::" + e.getMessage());
			e.printStackTrace();
		}
		//Added by sarita on 13NOV2017[start]
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
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
		}		
		//Added by sarita on 13NOV2017[end]
		return errString;
	}

	public String cctrVal(String cctrCode, String acctCode, String xtraParams,
			Connection conn) throws ITMException, RemoteException {
		String sql = "", errString = "CONFIRMED", userId = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int count = 0;
		String varValue = "";
		try {

			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, userId);
			sql = "select var_value from finparm where prd_code = '999999' and var_name = 'CCTR_CHECK'";
			pstmt = conn.prepareStatement(sql);

			rs = pstmt.executeQuery();

			if (rs.next()) {
				varValue = rs.getString("var_value");
			}
			//Added by sarita on 13NOV2017
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
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
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
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
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
				System.out.println("count:::" + count);

				if (count <= 0) {
					errString = "VMCCTR2";
					return errString;
				}
			}
		} catch (SQLException e) {
			System.out.println("::" + this.getClass().getSimpleName() + "::::"
					+ e.getMessage());
			e.printStackTrace();
		}
		//Added by sarita on 13NOV2017[start]
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
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
	    }		
	   //Added by sarita on 13NOV2017[end]
		return errString;

	}
}
