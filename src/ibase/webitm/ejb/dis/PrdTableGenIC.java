/********************************************************
Title 	 : PrdTableGenIC [D15JSUN001]
Date  	 : 22/DEC/15
Developer: Priyanks S
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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Stateless
public class PrdTableGenIC extends ValidatorEJB implements PrdTableGenICLocal,PrdTableGenICRemote {
	E12GenericUtility genericUtility = new E12GenericUtility();
	FinCommon finCommon = null;
	ValidatorEJB valEjb = null;

	// method for validation
	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag,String xtraParams) throws RemoteException, ITMException 
	{
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try 
		{
			System.out.println("Val xmlStrling :: " + xmlString);
			System.out.println("Val xmlString1 :: " + xmlString1);
			System.out.println("Val xmlString2 :: " + xmlString2);
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			dom2 = parseString(xmlString2);
			errString = wfValData(dom, dom1, dom2, objContext, editFlag,xtraParams);
		}
		catch (Exception e) 
		{
			throw new ITMException(e);
		}
		return (errString);
	}

	public String wfValData(Document dom, Document dom1, Document dom2,String objContext, String editFlag, String xtraParams)throws RemoteException, ITMException 
	{
		String childNodeName = null;
		String errString = "";
		String errCode = "";
		String userId = "";
		String sql = "";
		String errorType = "";
		int cnt = 0;
		int ctr = 0;
		int childNodeListLength = 0;
		int parentNodeListLength = 0;
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
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
		int currentFormNo = 0;
		boolean getValue = false;
		boolean getPeriodValue = false;
		String prdCode = "", cntCode = "", frDate = "", toDate = "", addDate = "", chgDate = "";
		String prdTblNo = "", prdClose = "";
		String entryStartDtStr = "", entryEndDtStr = "";
		Timestamp fromDate = null, tooDate = null, entryStartDt = null, entryEndDt = null;
		String divisionList = "", frmDateList = "", toDateList = "", toDateListNull = "", frmDateListNull = "", toDateListInvalid = "", frmDateListInvalid = "";
		String entryStartDtListNull = "", entryStartDtListInvalid = "", entryEndDtListInvalid = "", entryEndDtListNull = "", entryStartDtList = "";
		String ToDateEntryStrtList = "";
		boolean validateFlag = false;
		String retString = "", begPart = "", endPart = "", mainStr = "";
		try 
		{
			System.out.println("@@@@@@@@ wfvaldata called");
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			connDriver = null;
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			valEjb = new ValidatorEJB();
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
				childNodeListLength = childNodeList.getLength();
				ArrayList<String> filter = new ArrayList<String>();
				filter.add(0, "prd_code");
				filter.add(1, "count_code");
				filter.add(2, "fr_date");
				filter.add(3, "to_date");
				filter.add(4, "entry_start_dt");
				filter.add(5, "entry_end_dt");
				filter.add(6, "prd_closed");
				for (int fld = 0; fld < filter.size(); fld++) 
				{
					childNodeName = (String) filter.get(fld);
					if (childNodeName.equalsIgnoreCase("prd_code")) 
					{
						prdCode = this.genericUtility.getColumnValue("prd_code", dom);
						System.out.println("prdCode :" + prdCode);
						if (prdCode == null || prdCode.trim().length() == 0) 
						{
							System.out.println("Error : period code should not be blank!!!");
							errCode = "VTPRD14";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else if (prdCode != null && prdCode.trim().length() > 0) 
						{
							sql = "select count(*) from period where code=? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, prdCode.trim());
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								cnt = rs.getInt(1);
							}
							if (cnt == 0) 
							{
								errCode = "VMPRD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					else if (childNodeName.equalsIgnoreCase("count_code")) 
					{
						cntCode = this.genericUtility.getColumnValue("count_code", dom);
						System.out.println("count_code :" + cntCode);
						if (cntCode == null || cntCode.trim().length() == 0) 
						{
							System.out.println("Error : country code should not be blank!!!");
							errCode = "VMCOUNTCD";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else if (cntCode != null && cntCode.trim().length() > 0) 
						{
							sql = "select count(*) from country where count_code=? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, cntCode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								cnt = rs.getInt(1);
							}
							if (cnt == 0) {
								errCode = "VTCONTCD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						System.out.println("dIVISION vALIDATION");
						sql = "select count(1) from period_appl where ref_code like '%" + cntCode + "%'";
						pstmt = conn.prepareStatement(sql);
						// pstmt.setString(1,cntCode);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							cnt = rs.getInt(1);
						}
						if (cnt == 0) {
							errCode = "VTDIV1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					else if (childNodeName.equalsIgnoreCase("fr_date"))
					{
						frDate = genericUtility.getColumnValue("fr_date", dom);
						toDate = genericUtility.getColumnValue("to_date", dom);
						System.out.println(">>>>>>>>>>>frDate====" + frDate);
						System.out.println(">>>>>>>>>>>toDate====" + toDate);
						if (frDate == null || frDate.trim().length() == 0) 
						{
							errCode = "VTFRDTNULL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							break;
						}
						else 
						{
							if (isDateValid(frDate) == false) 
							{
								System.out.println(" from Date Invalid");
								errCode = "VTFRDT2";//
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							if ((frDate != null && frDate.trim().length() > 0) && (toDate != null && toDate.trim().length() > 0) && (isDateValid(frDate) == true)) {
								fromDate = Timestamp.valueOf(genericUtility.getValidDateString(frDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+ " 00:00:00.0");
								tooDate = Timestamp.valueOf(genericUtility.getValidDateString(toDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+ " 00:00:00.0");
								if (fromDate.compareTo(tooDate) > 0) 
								{
									errCode = "VTFRDATE";// **************
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									break;
								}
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("to_date")) {
						frDate = genericUtility.getColumnValue("fr_date", dom);
						toDate = genericUtility.getColumnValue("to_date", dom);
						System.out.println(">>>>>>>>>>>frDate====" + frDate);
						System.out.println(">>>>>>>>>>>toDate====" + toDate);
						if (toDate == null || toDate.trim().length() == 0) 
						{
							errCode = "VTTODTNULL";//
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							break;
						} else 
						{
							if (isDateValid(toDate) == false) 
							{
								System.out.println(" ToDate Invalid");
								errCode = "VTFRDT2";//
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

							if ((toDate != null && toDate.trim().length() > 0) && (frDate != null && frDate.trim().length() > 0) && (isDateValid(toDate) == true)) 
							{
								fromDate = Timestamp.valueOf(genericUtility.getValidDateString(frDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+ " 00:00:00.0");
								tooDate = Timestamp.valueOf(genericUtility.getValidDateString(toDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+ " 00:00:00.0");
								if (tooDate.compareTo(fromDate) < 0) {
									errCode = "VTTODATE";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									break;
								}
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("entry_start_dt")) 
					{
						entryStartDtStr = genericUtility.getColumnValue("entry_start_dt", dom);
						toDate = genericUtility.getColumnValue("to_date", dom);
						if (entryStartDtStr == null || entryStartDtStr.trim().length() == 0) 
						{
							errCode = "VTSTDTNULL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							break;
						}
						else 
						{
							if (isDateValid(entryStartDtStr) == false) 
							{
								System.out.println(" entryStartDtStr Invalid");
								errCode = "INVSTDT1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
							if ((toDate != null && toDate.trim().length() > 0) && (isDateValid(toDate) == true)) 
							{
								entryStartDt = Timestamp.valueOf(genericUtility.getValidDateString(entryStartDtStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+ " 00:00:00.0");
								tooDate = Timestamp.valueOf(genericUtility.getValidDateString(toDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+ " 00:00:00.0");
								if (entryStartDt.compareTo(tooDate) <= 0) 
								{
									System.out.println(" entryStartDt is less than To date");
									errCode = "INVSTDT3";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									break;
								}
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("entry_end_dt")) 
					{
						entryEndDtStr = genericUtility.getColumnValue("entry_end_dt", dom);
						entryStartDtStr = genericUtility.getColumnValue("entry_start_dt", dom);
						if (entryEndDtStr == null || entryEndDtStr.trim().length() == 0) 
						{
							errCode = "VTENDTNULL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							break;
						}
						else 
						{
							if (isDateValid(entryEndDtStr) == false) 
							{
								System.out.println(" entryEndDtStr Invalid");
								errCode = "INVENDDT1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
							else 
							{
								entryStartDt = Timestamp.valueOf(genericUtility.getValidDateString(entryStartDtStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+ " 00:00:00.0");
								entryEndDt = Timestamp.valueOf(genericUtility.getValidDateString(entryEndDtStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+ " 00:00:00.0");
								if (entryEndDt.compareTo(entryStartDt) < 0) 
								{
									System.out.println(" entryEndDtStr is less than start date");
									errCode = "INVENDDT2";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									break;
								}
							}
						}
					}
				}
				break;
			case 2:
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNodeListLength = parentNodeList.getLength();
				System.out.println("parentNodeListLength Detail3:::::::::"
						+ parentNodeListLength);
				for (int selectedRow = 0; selectedRow < parentNodeListLength; selectedRow++) {
					parentNode = parentNodeList.item(selectedRow);
					// parentNode = parentNodeList.item(0);
					if (parentNode != null) 
					{
						childNodeList = parentNode.getChildNodes();
						childNodeListLength = childNodeList.getLength();
					}
					System.out.println("@@@@@@@@@@@@childNodeListLength["+ childNodeListLength + "]");
					validateFlag = false;
					for (ctr = 0; ctr < childNodeListLength; ctr++) 
					{
						if (childNodeListLength > 0 && ctr == (childNodeListLength - 1)) 
						{
							validateFlag = true;
						}
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						System.out.println("childNodeName====" + childNodeName);
						if ("add_date".equalsIgnoreCase(childNode.getNodeName())) 
						{
							if (childNodeList.item(ctr).getFirstChild() != null) 
							{
								addDate = childNodeList.item(ctr).getFirstChild().getNodeValue();
							}
							else 
							{
								addDate = "";
							}
							System.out.println("addDate from detail===="+ addDate);						
							}
						if ("chg_date".equalsIgnoreCase(childNode.getNodeName())) 
						{
							if (childNodeList.item(ctr).getFirstChild() != null) 
							{
								chgDate = childNodeList.item(ctr).getFirstChild().getNodeValue();
							}
							else 
							{
								chgDate = "";
							}
							System.out.println("chgDate from detail===="+ chgDate);
						}
						if ("fr_date".equalsIgnoreCase(childNode.getNodeName())) 
						{
							if (childNodeList.item(ctr).getFirstChild() != null) 
							{
								frDate = childNodeList.item(ctr).getFirstChild().getNodeValue();
							}
							else 
							{
								frDate = "";
							}
							System.out.println("frDate from detail===="+ frDate);
						}
						if ("prd_closed".equalsIgnoreCase(childNode.getNodeName())) 
						{
							if (childNodeList.item(ctr).getFirstChild() != null) 
							{
								prdClose = childNodeList.item(ctr).getFirstChild().getNodeValue();
							}
							else 
							{
								prdClose = "";
							}
							System.out.println("prdClose from detail===="+ prdClose);
						}
						if ("prd_code".equalsIgnoreCase(childNode.getNodeName())) 
						{
							if (childNodeList.item(ctr).getFirstChild() != null) 
							{
								prdCode = childNodeList.item(ctr).getFirstChild().getNodeValue();
							}
							else 
							{
								prdCode = "";
							}
							System.out.println("prdCode from detail===="+ prdCode);
						}
						if ("prd_tblno".equalsIgnoreCase(childNode.getNodeName())) 
						{
							if (childNodeList.item(ctr).getFirstChild() != null) 
							{
								prdTblNo = childNodeList.item(ctr).getFirstChild().getNodeValue();
							}
							else 
							{
								prdTblNo = "";
							}
							System.out.println("prdTblNo from detail===="+ prdTblNo);
						}
						if ("to_date".equalsIgnoreCase(childNode.getNodeName())) 
						{
							if (childNodeList.item(ctr).getFirstChild() != null) 
							{
								toDate = childNodeList.item(ctr).getFirstChild().getNodeValue();
							}
							else 
							{
								toDate = "";
							}
							System.out.println("toDate from detail===="+ toDate);
						}
						if ("entry_start_dt".equalsIgnoreCase(childNode.getNodeName())) 
						{
							if (childNodeList.item(ctr).getFirstChild() != null) 
							{
								entryStartDtStr = childNodeList.item(ctr).getFirstChild().getNodeValue();
							}
							else 
							{
								entryStartDtStr = "";
							}
							System.out.println("entryStartDtStr from detail===="+ entryStartDtStr);
						}
						if ("entry_end_dt".equalsIgnoreCase(childNode.getNodeName())) 
						{
							if (childNodeList.item(ctr).getFirstChild() != null) 
							{
								entryEndDtStr = childNodeList.item(ctr).getFirstChild().getNodeValue();
							}
							else 
							{
								entryEndDtStr = "";
							}
							System.out.println("entryEndDtStr from detail===="+ entryEndDtStr);
						}
						System.out.println("Period code[" + prdCode+ "]-Period table Code[" + prdTblNo+ "]-from date[" + frDate + "]To Date["+ toDate + "]period closed[" + prdClose+ "]@@@");
						System.out.println("Add Date=====" + addDate);
						System.out.println("validateFlag===" + validateFlag);
						if ((validateFlag) && (prdTblNo != null && prdTblNo.trim().length() > 0)) 
						{
							System.out.println("To date Validation!!!!!!");
							System.out.println(">>>>>>>>>>>frDate====" + frDate);
							System.out.println(">>>>>>>>>>>toDate====" + toDate);
							if (frDate == null || frDate.trim().length() == 0) 
							{
								System.out.println("frmDate Null validation==");
								frmDateListNull = frmDateListNull + " " + prdTblNo + ",";
								System.out.println("frmDateListNull===="+ frmDateListNull);
								errCode = "VTFRDTNULL";//
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else 
							{
								if (isDateValid(frDate) == false) 
								{
									System.out.println(" From Date Invalid");
									frmDateListInvalid = frmDateListInvalid + " " + prdTblNo + ",";
									System.out.println("frmDateListInvalid===="+ frmDateListInvalid);
									errCode = "VTFRDT2";//
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							if (toDate == null || toDate.trim().length() == 0) 
							{
								toDateListNull = toDateListNull + " "+ prdTblNo + ",";
								System.out.println("toDateListNull===="+ toDateListNull);
								errCode = "VTTODTNULL";//
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else 
							{
								if (isDateValid(toDate) == false) 
								{
									System.out.println(" From Date Invalid");
									toDateListInvalid = toDateListInvalid + " " + prdTblNo + ",";
									System.out.println("toDateListInvalid===="+ toDateListInvalid);
									errCode = "VTTODT2";//
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								if ((toDate != null && toDate.trim().length() > 0) && (frDate != null && frDate.trim().length() > 0) && (isDateValid(frDate) == true) && (isDateValid(toDate) == true)) 
								{
									fromDate = Timestamp.valueOf(genericUtility.getValidDateString(frDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+ " 00:00:00.0");
									tooDate = Timestamp.valueOf(genericUtility.getValidDateString(toDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+ " 00:00:00.0");
									if (tooDate.compareTo(fromDate) < 0) 
									{
										// toDateList
										toDateList = toDateList + " "+ prdTblNo + ",";
										System.out.println("toDateList===="+ toDateList);
										errCode = "VTTODATE";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
							if (entryStartDtStr == null || entryStartDtStr.trim().length() == 0) 
							{
								entryStartDtListNull = entryStartDtListNull+ " " + prdTblNo + ",";
								System.out.println("entryStartDtListNull===="+ entryStartDtListNull);
								errCode = "VTSTDTNULL";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else 
							{
								if (isDateValid(entryStartDtStr) == false) 
								{
									entryStartDtListInvalid = entryStartDtListInvalid+ " " + prdTblNo + ",";
									System.out.println("entryStartDtListInvalid===="+ entryStartDtListInvalid);
									errCode = "INVSTDT1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								if ((toDate != null && toDate.trim().length() > 0) && (isDateValid(toDate) == true)) 
								{
									entryStartDt = Timestamp.valueOf(genericUtility.getValidDateString(entryStartDtStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+ " 00:00:00.0");
									tooDate = Timestamp.valueOf(genericUtility.getValidDateString(toDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+ " 00:00:00.0");
									System.out.println("entryStartDt>>"+ entryStartDt);
									System.out.println("tooDate>>" + tooDate);
									if (entryStartDt.compareTo(tooDate) <= 0) 
									{
										ToDateEntryStrtList = ToDateEntryStrtList+ " " + prdTblNo + ",";
										System.out.println("ToDateEntryStrtList===="+ ToDateEntryStrtList);
										errCode = "INVSTDT3";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
							if (entryEndDtStr == null || entryEndDtStr.trim().length() == 0) 
							{
								entryEndDtListNull = entryEndDtListNull + " " + prdTblNo + ",";
								System.out.println("entryEndDtListNull===="+ entryEndDtListNull);
								errCode = "VTENDTNULL";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else 
							{
								if (isDateValid(entryEndDtStr) == false) 
								{
									entryEndDtListInvalid = entryEndDtListInvalid + " " + prdTblNo + ",";
									System.out.println("entryEndDtListInvalid===="+ entryEndDtListInvalid);
									errCode = "INVENDDT1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								else 
								{
									if ((entryStartDtStr != null && entryStartDtStr.trim().length() > 0) && (isDateValid(entryStartDtStr) == true)) 
									{
										entryStartDt = Timestamp.valueOf(genericUtility.getValidDateString(entryStartDtStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+ " 00:00:00.0");
										entryEndDt = Timestamp.valueOf(genericUtility.getValidDateString(entryEndDtStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+ " 00:00:00.0");
										if (entryEndDt.compareTo(entryStartDt) < 0) 
										{
											System.out.println(" entryEndDtStr is less than start date");
											entryStartDtList = entryStartDtList+ " " + prdTblNo + ",";
											System.out.println("entryStartDtList===="+ entryStartDtList);
											errCode = "INVENDDT2";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
									}
								}
							}
							System.out.println("Value inside (getDetailValues=====) Period code["+ prdCode+ "]-Period table Code["+ prdTblNo+ "]-from date["+ frDate+ "]To Date["+ toDate+ "]period closed["+ prdClose+ "]@@@");
							getValue = getDetailValues(prdCode, prdTblNo,frDate, toDate, prdClose, conn);
							System.out.println("getValue from getDetailValues==== "+ getValue);
							if (getValue) 
							{
								divisionList = divisionList + " " + prdTblNo+ ",";
								System.out.println("divisionList===="+ divisionList);
								/*
								 * if(!divisionLst.contains(prdTblNo)) {
								 * System.out
								 * .println("divisionLst item>>>>>>>"+prdTblNo);
								 * divisionLst.add(prdTblNo.trim()); }
								 */
								errCode = "VTDIVCLOS";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							System.out.println("===============================================================================");
							System.out.println("Validation of From Date####");
							System.out.println("Value inside (getPeriodValue=====) Period code["+ prdCode+ "]-Period table Code["+ prdTblNo+ "]-from date["+ frDate+ "]To Date["+ toDate+ "]period closed["+ prdClose+ "]@@@");
							getPeriodValue = getPeriod(prdCode, prdTblNo,frDate, toDate, prdClose, addDate, conn);
							System.out.println("getValue from getDetailValues==== "+ getPeriodValue);
							if (getPeriodValue) 
							{
								frmDateList = frmDateList + " " + prdTblNo+ ",";
								System.out.println("frmDateList===="+ frmDateList);
								errCode = "VTFRMPRV";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							// added
						}
					} // end for
				}// end of selected row
				break; // case 2 end// case 1 end
			}
			int errListSize = errList.size();
			cnt = 0;
			String errFldName = "";
			System.out.println("@@@@@errListSize[" + errListSize + "]");
			if (errList != null && errListSize > 0) {
				for (cnt = 0; cnt < errListSize; cnt++) {
					errCode = errList.get(cnt);
					errFldName = errFields.get(cnt);
					System.out.println("errCode .........." + errCode+"errFldName:::"+errFldName+"userId::::"+userId);
					errString = getErrorString(errFldName, errCode, userId);
					System.out.println("errString====" + errString);
					errorType = errorType(conn, errCode);
					System.out.println("errorType====" + errorType);
					/*
					 * if (errString.length() > 0) { String bifurErrString =
					 * errString.substring(errString.indexOf("<Errors>") + 8,
					 * errString.indexOf("<trace>")); bifurErrString =
					 * bifurErrString +
					 * errString.substring(errString.indexOf("</trace>") + 8,
					 * errString.indexOf("</Errors>"));
					 * errStringXml.append(bifurErrString); // errString = ""; }
					 */
					// Start Added by chandrashekar on 16-feb-2016
					if (errCode.equals("INVSTDT3")) {
						if (ToDateEntryStrtList != null && ToDateEntryStrtList.trim().length() > 0) {
							ToDateEntryStrtList = ToDateEntryStrtList.substring(0,ToDateEntryStrtList.length() - 1);
						}
						System.out.println("@@@@@@@ ToDateEntryStrtList["+ ToDateEntryStrtList + "]");
						retString = errString;
						begPart = retString.substring(0,errString.indexOf("]]></description>"));
						endPart = retString.substring(errString.indexOf("]]></description>"),errString.length());
						mainStr = begPart + ToDateEntryStrtList + endPart;
						errString = mainStr;
					}
					if (errCode.equals("VTSTDTNULL"))
					{
						if (entryStartDtListNull != null && entryStartDtListNull.trim().length() > 0) 
						{
							entryStartDtListNull = entryStartDtListNull.substring(0,entryStartDtListNull.length() - 1);
						}
						System.out.println("@@@@@@@ entryStartDtListNull["+ entryStartDtListNull + "]");
						retString = errString;
						begPart = retString.substring(0,errString.indexOf("]]></description>"));
						endPart = retString.substring(errString.indexOf("]]></description>"),errString.length());
						mainStr = begPart + entryStartDtListNull + endPart;
						errString = mainStr;
					}
					if (errCode.equals("INVSTDT1")) 
					{
						if (entryStartDtListInvalid != null && entryStartDtListInvalid.trim().length() > 0) 
						{
							entryStartDtListInvalid = entryStartDtListInvalid.substring(0,entryStartDtListInvalid.length() - 1);
						}
						System.out.println("@@@@@@@ entryStartDtListInvalid["+ entryStartDtListInvalid + "]");
						retString = errString;
						begPart = retString.substring(0,errString.indexOf("]]></description>"));
						endPart = retString.substring(errString.indexOf("]]></description>"),errString.length());
						mainStr = begPart + entryStartDtListInvalid + endPart;
						errString = mainStr;
					}
					if (errCode.equals("VTENDTNULL")) 
					{
						if (entryEndDtListNull != null && entryEndDtListNull.trim().length() > 0) 
						{
							entryEndDtListNull = entryEndDtListNull.substring(0, entryEndDtListNull.length() - 1);
						}
						System.out.println("@@@@@@@ entryEndDtListNull["+ entryEndDtListNull + "]");
						retString = errString;
						begPart = retString.substring(0,errString.indexOf("]]></description>"));
						endPart = retString.substring(errString.indexOf("]]></description>"),errString.length());
						mainStr = begPart + entryEndDtListNull + endPart;
						errString = mainStr;
					}
					if (errCode.equals("INVENDDT1")) 
					{
						if (entryEndDtListInvalid != null && entryEndDtListInvalid.trim().length() > 0) 
						{
							entryEndDtListInvalid = entryEndDtListInvalid.substring(0,entryEndDtListInvalid.length() - 1);
						}
						System.out.println("@@@@@@@ entryEndDtListInvalid["+ entryEndDtListInvalid + "]");
						retString = errString;
						begPart = retString.substring(0,errString.indexOf("]]></description>"));
						endPart = retString.substring(errString.indexOf("]]></description>"),errString.length());
						mainStr = begPart + entryEndDtListInvalid + endPart;
						errString = mainStr;
					}
					if (errCode.equals("INVENDDT2")) 
					{
						if (entryStartDtList != null && entryStartDtList.trim().length() > 0) 
						{
							entryStartDtList = entryStartDtList.substring(0,entryStartDtList.length() - 1);
						}
						System.out.println("@@@@@@@ entryStartDtListNull["+ entryStartDtList + "]");
						retString = errString;
						begPart = retString.substring(0,errString.indexOf("]]></description>"));
						endPart = retString.substring(errString.indexOf("]]></description>"),errString.length());
						mainStr = begPart + entryStartDtList + endPart;
						errString = mainStr;
					}
					// End Added by chandrashekar on 16-feb-2016
					if (errCode.equals("VTDIVCLOS")) 
					{
						/*
						 * retString=errString;
						 * System.out.println("retString===="+retString); //
						 * retString =
						 * itmDBAccessEJB.getErrorString("","VTNTCONF"
						 * ,"","",conn); begPart = retString.substring( 0,
						 * retString.indexOf("<trace>") + 7 );
						 * System.out.println("begPart===="+begPart); endPart =
						 * retString.substring( retString.indexOf("</trace>"));
						 * System.out.println("endPart===="+endPart); mainStr =
						 * begPart + divisionList + endPart; errString =
						 * mainStr; System.out.println("mainStr-----"+mainStr);
						 * errStringXml.append(errString);
						 * System.out.println("errStringXml===="+errStringXml);
						 * errString = "";
						 */
						if (divisionList != null && divisionList.trim().length() > 0) 
						{
							divisionList = divisionList.substring(0,divisionList.length() - 1);
						}
						System.out.println("@@@@@@@ divisionList["+ divisionList + "]");
						retString = errString;
						System.out.println("retString====" + retString);
						// retString =
						// itmDBAccessEJB.getErrorString("","VTNTCONF","","",conn);
						begPart = retString.substring(0,errString.indexOf("]]></description>"));
						System.out.println("begPart====" + begPart);
						endPart = retString.substring(errString.indexOf("]]></description>"),errString.length());
						System.out.println("endPart====" + endPart);
						mainStr = begPart + divisionList + endPart;
						errString = mainStr;
						System.out.println("mainStr-----" + mainStr);
						// errStringXml.append(errString);
						System.out.println("errStringXml====" + errStringXml);
						// errString = "";
					}
					if (errCode.equals("VTFRMPRV")) 
					{
						/*
						 * retString=errString;
						 * System.out.println("retString===="+retString); //
						 * retString =
						 * itmDBAccessEJB.getErrorString("","VTNTCONF"
						 * ,"","",conn); begPart = retString.substring( 0,
						 * retString.indexOf("<trace>") + 7 );
						 * System.out.println("begPart===="+begPart); endPart =
						 * retString.substring( retString.indexOf("</trace>"));
						 * System.out.println("endPart===="+endPart); mainStr =
						 * begPart + divisionList + endPart; errString =
						 * mainStr; System.out.println("mainStr-----"+mainStr);
						 * 
						 * errStringXml.append(errString);
						 * System.out.println("errStringXml===="+errStringXml);
						 * errString = "";
						 */
						if (frmDateList != null && frmDateList.trim().length() > 0) 
						{
							frmDateList = frmDateList.substring(0,frmDateList.length() - 1);
						}
						System.out.println("@@@@@@@ frmDateList[" + frmDateList + "]");
						retString = errString;
						System.out.println("retString====" + retString);
						// retString =
						// itmDBAccessEJB.getErrorString("","VTNTCONF","","",conn);
						begPart = retString.substring(0,errString.indexOf("]]></description>"));
						System.out.println("begPart====" + begPart);
						endPart = retString.substring(errString.indexOf("]]></description>"),errString.length());
						System.out.println("endPart====" + endPart);
						mainStr = begPart + frmDateList + endPart;
						errString = mainStr;
						System.out.println("mainStr-----" + mainStr);
						// errStringXml.append(errString);
						System.out.println("errStringXml====" + errStringXml);
						// errString = "";
					}
					// New To date validation
					if (errCode.equals("VTTODATE")) 
					{
						/*
						 * retString=errString;
						 * System.out.println("retString===="+retString); //
						 * retString =
						 * itmDBAccessEJB.getErrorString("","VTNTCONF"
						 * ,"","",conn); begPart = retString.substring( 0,
						 * retString.indexOf("<trace>") + 7 );
						 * System.out.println("begPart===="+begPart); endPart =
						 * retString.substring( retString.indexOf("</trace>"));
						 * System.out.println("endPart===="+endPart); mainStr =
						 * begPart + divisionList + endPart; errString =
						 * mainStr; System.out.println("mainStr-----"+mainStr);
						 * errStringXml.append(errString);
						 * System.out.println("errStringXml===="+errStringXml);
						 * errString = "";
						 */
						if (toDateList != null && toDateList.trim().length() > 0) 
						{
							toDateList = toDateList.substring(0,toDateList.length() - 1);
						}
						System.out.println("@@@@@@@ toDateList[" + toDateList+ "]");
						retString = errString;
						System.out.println("retString====" + retString);
						// retString =
						// itmDBAccessEJB.getErrorString("","VTNTCONF","","",conn);
						begPart = retString.substring(0,errString.indexOf("]]></description>"));
						System.out.println("begPart====" + begPart);
						endPart = retString.substring(errString.indexOf("]]></description>"),errString.length());
						System.out.println("endPart====" + endPart);
						mainStr = begPart + toDateList + endPart;
						errString = mainStr;
						System.out.println("mainStr-----" + mainStr);
						// errStringXml.append(errString);
						System.out.println("errStringXml====" + errStringXml);
						// errString = "";
					}
					// to date null
					if (errCode.equals("VTTODTNULL")) 
					{
						/*
						 * retString=errString;
						 * System.out.println("retString===="+retString); //
						 * retString =
						 * itmDBAccessEJB.getErrorString("","VTNTCONF"
						 * ,"","",conn); begPart = retString.substring( 0,
						 * retString.indexOf("<trace>") + 7 );
						 * System.out.println("begPart===="+begPart); endPart =
						 * retString.substring( retString.indexOf("</trace>"));
						 * System.out.println("endPart===="+endPart); mainStr =
						 * begPart + divisionList + endPart; errString =
						 * mainStr; System.out.println("mainStr-----"+mainStr);
						 * errStringXml.append(errString);
						 * System.out.println("errStringXml===="+errStringXml);
						 * errString = "";
						 */
						if (toDateListNull != null && toDateListNull.trim().length() > 0) 
						{
							toDateListNull = toDateListNull.substring(0,toDateListNull.length() - 1);
						}
						System.out.println("@@@@@@@ toDateListNull["+ toDateListNull + "]");
						retString = errString;
						System.out.println("retString====" + retString);
						// retString =
						// itmDBAccessEJB.getErrorString("","VTNTCONF","","",conn);
						begPart = retString.substring(0,errString.indexOf("]]></description>"));
						System.out.println("begPart====" + begPart);
						endPart = retString.substring(errString.indexOf("]]></description>"),errString.length());
						System.out.println("endPart====" + endPart);
						mainStr = begPart + toDateListNull + endPart;
						errString = mainStr;
						System.out.println("mainStr-----" + mainStr);
						// errStringXml.append(errString);
						System.out.println("errStringXml====" + errStringXml);
						// errString = "";
					}
					// Null from Date
					if (errCode.equals("VTFRDTNULL")) 
					{
						/*
						 * retString=errString;
						 * System.out.println("retString===="+retString); //
						 * retString =
						 * itmDBAccessEJB.getErrorString("","VTNTCONF"
						 * ,"","",conn); begPart = retString.substring( 0,
						 * retString.indexOf("<trace>") + 7 );
						 * System.out.println("begPart===="+begPart); endPart =
						 * retString.substring( retString.indexOf("</trace>"));
						 * System.out.println("endPart===="+endPart); mainStr =
						 * begPart + divisionList + endPart; errString =
						 * mainStr; System.out.println("mainStr-----"+mainStr);
						 * errStringXml.append(errString);
						 * System.out.println("errStringXml===="+errStringXml);
						 * errString = "";
						 */
						if (frmDateListNull != null && frmDateListNull.trim().length() > 0) 
						{
							frmDateListNull = frmDateListNull.substring(0,frmDateListNull.length() - 1);
						}
						System.out.println("@@@@@@@ frmDateListNull["+ frmDateListNull + "]");
						retString = errString;
						System.out.println("retString====" + retString);
						// retString =
						// itmDBAccessEJB.getErrorString("","VTNTCONF","","",conn);
						begPart = retString.substring(0,errString.indexOf("]]></description>"));
						System.out.println("begPart====" + begPart);
						endPart = retString.substring(errString.indexOf("]]></description>"),errString.length());
						System.out.println("endPart====" + endPart);
						mainStr = begPart + frmDateListNull + endPart;
						errString = mainStr;
						System.out.println("mainStr-----" + mainStr);
						// errStringXml.append(errString);
						System.out.println("errStringXml====" + errStringXml);
						// errString = "";
					}
					// VTFRDT2
					if (errCode.equals("VTFRDT2")) 
					{
						/*
						 * retString=errString;
						 * System.out.println("retString===="+retString); //
						 * retString =
						 * itmDBAccessEJB.getErrorString("","VTNTCONF"
						 * ,"","",conn); begPart = retString.substring( 0,
						 * retString.indexOf("<trace>") + 7 );
						 * System.out.println("begPart===="+begPart); endPart =
						 * retString.substring( retString.indexOf("</trace>"));
						 * System.out.println("endPart===="+endPart); mainStr =
						 * begPart + divisionList + endPart; errString =
						 * mainStr; System.out.println("mainStr-----"+mainStr);
						 * errStringXml.append(errString);
						 * System.out.println("errStringXml===="+errStringXml);
						 * errString = "";
						 */
						if (frmDateListInvalid != null && frmDateListInvalid.trim().length() > 0) 
						{
							frmDateListInvalid = frmDateListInvalid.substring(0, frmDateListInvalid.length() - 1);
						}
						System.out.println("@@@@@@@ frmDateListInvalid["+ frmDateListInvalid + "]");
						retString = errString;
						System.out.println("retString====" + retString);
						// retString =
						// itmDBAccessEJB.getErrorString("","VTNTCONF","","",conn);
						begPart = retString.substring(0,errString.indexOf("]]></description>"));
						System.out.println("begPart====" + begPart);
						endPart = retString.substring(errString.indexOf("]]></description>"),errString.length());
						System.out.println("endPart====" + endPart);
						mainStr = begPart + frmDateListInvalid + endPart;
						errString = mainStr;
						System.out.println("mainStr-----" + mainStr);
						// errStringXml.append(errString);
						System.out.println("errStringXml====" + errStringXml);
						// errString = "";
					}
					// VTTODT2
					if (errCode.equals("VTTODT2")) 
					{
						/*
						 * retString=errString;
						 * System.out.println("retString===="+retString); //
						 * retString =
						 * itmDBAccessEJB.getErrorString("","VTNTCONF"
						 * ,"","",conn); begPart = retString.substring( 0,
						 * retString.indexOf("<trace>") + 7 );
						 * System.out.println("begPart===="+begPart); endPart =
						 * retString.substring( retString.indexOf("</trace>"));
						 * System.out.println("endPart===="+endPart); mainStr =
						 * begPart + divisionList + endPart; errString =
						 * mainStr; System.out.println("mainStr-----"+mainStr);
						 * errStringXml.append(errString);
						 * System.out.println("errStringXml===="+errStringXml);
						 * errString = "";
						 */
						if (toDateListInvalid != null && toDateListInvalid.trim().length() > 0) 
						{
							toDateListInvalid = toDateListInvalid.substring(0,toDateListInvalid.length() - 1);
						}
						System.out.println("@@@@@@@ toDateListInvalid["+ toDateListInvalid + "]");
						retString = errString;
						System.out.println("retString====" + retString);
						// retString =
						// itmDBAccessEJB.getErrorString("","VTNTCONF","","",conn);
						begPart = retString.substring(0,errString.indexOf("]]></description>"));
						System.out.println("begPart====" + begPart);
						endPart = retString.substring(errString.indexOf("]]></description>"),errString.length());
						System.out.println("endPart====" + endPart);
						mainStr = begPart + toDateListInvalid + endPart;
						errString = mainStr;
						System.out.println("mainStr-----" + mainStr);
						// errStringXml.append(errString);
						System.out.println("errStringXml====" + errStringXml);
						// errString = "";
					}
					if (errString.length() > 0) 
					{
						String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8,errString.indexOf("<trace>"));
						System.out.println("bifurErrString>>>>>>>>>>"+ bifurErrString);
						bifurErrString = bifurErrString+ errString.substring(errString.indexOf("</trace>") + 8,errString.indexOf("</Errors>"));
						System.out.println("bifurErrString@@@@@@@@@@@@"+ bifurErrString);
						errStringXml.append(bifurErrString);
						System.out.println("errStringXml@@@@@@@"+ errStringXml.toString());
						System.out.println("errStringXml$$$$$$$$$$"+ errStringXml.toString());
						errString = "";
					}
					if (errorType.equalsIgnoreCase("E")) 
					{
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

	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn,String editFlag, String xtraParams) throws RemoteException,ITMException 
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		System.out.println("xmlString............." + xmlString);
		System.out.println("xmlString1............" + xmlString1);
		System.out.println("xmlString2............" + xmlString2);
		try 
		{
			if (xmlString != null && xmlString.trim().length() > 0) 
			{
				dom = parseString(xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0) {
				dom1 = parseString(xmlString1);
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0) {
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext,currentColumn, editFlag, xtraParams);
		} catch (Exception e) {
			System.out.println("Exception : [EcollectionIC][itemChanged( String, String )] :==>\n"+ e.getMessage());
			throw new ITMException(e);
		}
		return valueXmlString;
	}
//TODO
	public String itemChanged(Document dom, Document dom1, Document dom2,String objContext, String currentColumn, String editFlag,String xtraParams) throws RemoteException, ITMException 
	{
		String sql = "";
		StringBuffer valueXmlString = new StringBuffer();
		Connection conn = null;
		PreparedStatement pstmt = null, pstmt1 = null;
		ResultSet rs = null, rs1 = null;
		E12GenericUtility genericUtility = new E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();
		int currentFormNo = 0;
		//String  userId = "", termId = "";
		String loginSiteCode="";
		Timestamp timestamp = null;
		String prdCode = "", cntCode = "", frDate = "", toDate = "", prdClose = "", siteCode = "",countDescr = "";
		String refCode = "";
		int prdCnt = 0, cnt = 0;
		String currDate = "";
		Date fromDateVal = null, toDateVal = null, entryStartDtVal = null, entryEndDtVal = null;
		String periodClosed = "", entryStartDt = "", entryEndDt = "";
		String entryStartDtStr = "", entryEndDtStr = "",singleRefCode="",finalRefCode="",cntryCodeSeries="";
		java.util.Date enStrtDt=null,frDateFrmPrd=null,toDateFrmPrd=null;
		StringBuffer abc =new StringBuffer();
		
		try 
		{
			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String sysDate = sdf.format(currentDate.getTime());
			timestamp = new Timestamp(System.currentTimeMillis());
			currDate = (sdf.format(timestamp).toString()).trim();
			System.out.println("Current Date>>>>>>>>>" + currDate);
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			conn.setAutoCommit(false);
			connDriver = null;
			if (objContext != null && objContext.trim().length() > 0) 
			{
				currentFormNo = Integer.parseInt(objContext.trim());
			}
			loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
			System.out.println("Now the date is :=>  " + sysDate);
			DistCommon distCommon = new DistCommon();
			String obj_name = getObjName(dom, objContext);
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n<Header>\r\n<editFlag>");
			valueXmlString.append(editFlag).append("</editFlag>\r\n</Header>\r\n");
			System.out.println("**********ITEMCHANGE FOR CASE" + currentFormNo+ "**************");
			switch (currentFormNo) 
			{
			case 1:
				if (currentColumn.trim().equalsIgnoreCase("itm_default")) 
				{
					String conSql="SELECT C.COUNT_CODE AS CNT_CODE, C.DESCR  AS CNT_DESCR FROM COUNTRY C JOIN STATE S ON S.COUNT_CODE = C.COUNT_CODE AND S.STATE_CODE IN (SELECT T.STATE_CODE FROM SITE T WHERE T.SITE_CODE='"+loginSiteCode+"')";
					pstmt = conn.prepareStatement(conSql);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						cntCode=rs.getString("CNT_CODE");
					}
					else
					{
						cntCode=distCommon.getDisparams("999999", "PRD_TBL_GEN_CN", conn);
					}
					if(pstmt!=null || rs!=null)
					{
						pstmt.close();pstmt=null;rs.close();rs=null;
					}
					cntCode=cntCode==null ? "" : cntCode.trim();
					System.out.println("loginSiteCode:::"+loginSiteCode+"cntCode:::"+cntCode+"obj_name::"+obj_name);
					//added by saurabh to set default item change[Start|07/10/16] 
					if (cntCode != null && cntCode.trim().length() > 0)
					{
						sql = "select descr from country where count_code= ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, cntCode);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							countDescr = rs.getString("descr");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					System.out.println("case 1: itm_default called ");
					valueXmlString.append("<Detail1 domID='1' objContext='1'>");
					// valueXmlString.append("<Detail1 domID='1' objContext=\"1\" selected=\"N\">\r\n");
					// valueXmlString.append("<attribute  selected=\"N\" updateFlag=\"A\"  status=\"N\" pkNames=\"\" />\r\n");
					valueXmlString.append("<count_code>").append("<![CDATA[" +cntCode+ "]]>").append("</count_code>");
					valueXmlString.append("<count_descr>").append("<![CDATA[" + countDescr + "]]>").append("</count_descr>\r\n");
					valueXmlString.append("<prd_code>").append("").append("</prd_code>");
					valueXmlString.append("<fr_date>").append("").append("</fr_date>");
					valueXmlString.append("<to_date>").append("").append("</to_date>");
					valueXmlString.append("<entry_start_dt>").append("").append("</entry_start_dt>");
					valueXmlString.append("<entry_end_dt>").append("").append("</entry_end_dt>");
					valueXmlString.append("<prd_closed>").append("<![CDATA[N]]>").append("</prd_closed>");
					valueXmlString.append("</Detail1>");
				}
				//added by saurabh to set item change on Period code[Start|07/10/16]
				else if (currentColumn.trim().equalsIgnoreCase("prd_code")) 
				{
					System.out.println("Period code itemchange ");
					prdCode = checkNull(genericUtility.getColumnValue("prd_code", dom));
					System.out.println("Period Code=====" + prdCode);
					if (prdCode != null && prdCode.trim().length() > 0)
					{
						sql = "select fr_date,to_date from period where code=?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, prdCode);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							frDateFrmPrd=rs.getDate("fr_date");
							toDateFrmPrd =rs.getDate("to_date");
							entryStartDt=sdf.format(toDateFrmPrd.getTime() + 1000 * 60 * 60 * 24);
						}
						if(frDateFrmPrd!=null && toDateFrmPrd!=null)
						{
							frDate=sdf.format(frDateFrmPrd);
							toDate=sdf.format(toDateFrmPrd);
							
						}
						System.out.println("frDate:::"+frDate+"toDate:::"+toDate+"entryStartDt:::"+entryStartDt);
						rs.close();rs = null;pstmt.close();pstmt = null;
						
						System.out.println("Country description====="+ countDescr);
						valueXmlString.append("<Detail1 domID='1' objContext='1'>");
						valueXmlString.append("<prd_code>").append("<![CDATA[" + prdCode + "]]>").append("</prd_code>");
						valueXmlString.append("<fr_date>").append("<![CDATA[" + frDate + "]]>").append("</fr_date>");
						valueXmlString.append("<to_date>").append("<![CDATA[" + toDate + "]]>").append("</to_date>");
						valueXmlString.append("<entry_start_dt>").append("<![CDATA[" + entryStartDt + "]]>").append("</entry_start_dt>");
						valueXmlString.append("</Detail1>");
					}
					else 
					{
						valueXmlString.append("<Detail1 domID='1' objContext='1'>");
						valueXmlString.append("<prd_code>").append("<![CDATA[" + prdCode + "]]>").append("</prd_code>");
						valueXmlString.append("<fr_date>").append("").append("</fr_date>");
						valueXmlString.append("<to_date>").append("").append("</to_date>");
						valueXmlString.append("<entry_start_dt>").append("").append("</entry_start_dt>");
						valueXmlString.append("</Detail1>");
					}
				}
				else if (currentColumn.trim().equalsIgnoreCase("to_date")) 
				{
					prdCode = checkNull(genericUtility.getColumnValue("prd_code", dom));
					frDate = checkNull(genericUtility.getColumnValue("fr_date", dom));
					toDate = checkNull(genericUtility.getColumnValue("to_date", dom));
					entryEndDt = checkNull(genericUtility.getColumnValue("entry_end_dt", dom));
					if(toDate!=null && toDate.trim().length()>0)
					{
						enStrtDt=sdf.parse(toDate);
						entryStartDt=sdf.format(enStrtDt.getTime() + 1000 * 60 * 60 * 24);
						valueXmlString.append("<Detail1 domID='1' objContext='1'>");
						valueXmlString.append("<prd_code>").append("<![CDATA[" + prdCode + "]]>").append("</prd_code>");
						valueXmlString.append("<fr_date>").append("<![CDATA[" + frDate + "]]>").append("</fr_date>");
						valueXmlString.append("<to_date>").append("<![CDATA[" + toDate + "]]>").append("</to_date>");
						valueXmlString.append("<entry_start_dt>").append("<![CDATA[" + entryStartDt + "]]>").append("</entry_start_dt>");
						valueXmlString.append("<entry_end_dt>").append("<![CDATA[" + entryEndDt + "]]>").append("</entry_end_dt>");
						valueXmlString.append("</Detail1>");
					}
					else
					{
						valueXmlString.append("<Detail1 domID='1' objContext='1'>");
						valueXmlString.append("<prd_code>").append("<![CDATA[" + prdCode + "]]>").append("</prd_code>");
						valueXmlString.append("<fr_date>").append("<![CDATA[" + frDate + "]]>").append("</fr_date>");
						valueXmlString.append("<to_date>").append("<![CDATA[" + toDate + "]]>").append("</to_date>");
						valueXmlString.append("<entry_start_dt>").append("").append("</entry_start_dt>");
						valueXmlString.append("<entry_end_dt>").append("<![CDATA[" + entryEndDt + "]]>").append("</entry_end_dt>");
						valueXmlString.append("</Detail1>");
					}
				}
				//added by saurabh to set item change on Period code[End|07/10/16]
				else if (currentColumn.trim().equalsIgnoreCase("count_code")) 
				{
					System.out.println("Country code itemchange ");
					prdCode = checkNull(genericUtility.getColumnValue("prd_code", dom));
					frDate = checkNull(genericUtility.getColumnValue("fr_date", dom));
					toDate = checkNull(genericUtility.getColumnValue("to_date", dom));
					entryStartDt = checkNull(genericUtility.getColumnValue("entry_start_dt", dom));
					entryEndDt = checkNull(genericUtility.getColumnValue("entry_end_dt", dom));
					cntCode = checkNull(genericUtility.getColumnValue("count_code", dom));
					System.out.println("Country Code=====" + cntCode);
					if (cntCode != null && cntCode.trim().length() > 0) 
					{
						sql = "select descr from country where count_code= ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, cntCode);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							countDescr = rs.getString("descr");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("Country description====="+ countDescr);
						valueXmlString.append("<Detail1 domID='1' objContext='1'>");
						valueXmlString.append("<count_code>").append("<![CDATA[" + cntCode + "]]>").append("</count_code>\r\n");
						valueXmlString.append("<count_descr>").append("<![CDATA[" + countDescr + "]]>").append("</count_descr>\r\n");
						valueXmlString.append("<prd_code>").append("<![CDATA[" + prdCode + "]]>").append("</prd_code>");
						valueXmlString.append("<fr_date>").append("<![CDATA[" + frDate + "]]>").append("</fr_date>");
						valueXmlString.append("<to_date>").append("<![CDATA[" + toDate + "]]>").append("</to_date>");
						valueXmlString.append("<entry_start_dt>").append("<![CDATA[" + entryStartDt + "]]>").append("</entry_start_dt>");
						valueXmlString.append("<entry_end_dt>").append("<![CDATA[" + entryEndDt + "]]>").append("</entry_end_dt>");
						valueXmlString.append("</Detail1>");
					} else {
						valueXmlString.append("<Detail1 domID='1' objContext='1'>");
						valueXmlString.append("<count_code>").append("<![CDATA[]]>").append("</count_code>\r\n");
						valueXmlString.append("<count_descr>").append("<![CDATA[]]>").append("</count_descr>\r\n");
						valueXmlString.append("<prd_code>").append("<![CDATA[" + prdCode + "]]>").append("</prd_code>");
						valueXmlString.append("<fr_date>").append("<![CDATA[" + frDate + "]]>").append("</fr_date>");
						valueXmlString.append("<to_date>").append("<![CDATA[" + toDate + "]]>").append("</to_date>");
						valueXmlString.append("<entry_start_dt>").append("<![CDATA[" + entryStartDt + "]]>").append("</entry_start_dt>");
						valueXmlString.append("<entry_end_dt>").append("<![CDATA[" + entryEndDt + "]]>").append("</entry_end_dt>");
						valueXmlString.append("</Detail1>");
					}
				}
				break;
			case 2:
				if (currentColumn.trim().equals("itm_default")) 
				{
					System.out.println("itm_default called for case 2");
					prdCode = checkNull(genericUtility.getColumnValue("prd_code", dom1));
					cntCode = checkNull(genericUtility.getColumnValue("count_code", dom1).trim());
					frDate = checkNull(genericUtility.getColumnValue("fr_date",dom1));
					toDate = checkNull(genericUtility.getColumnValue("to_date",dom1));
					toDate = checkNull(genericUtility.getColumnValue("to_date",dom1));
					entryStartDt = checkNull(genericUtility.getColumnValue("entry_start_dt", dom1));
					entryEndDt = checkNull(genericUtility.getColumnValue("entry_end_dt", dom1));
					prdClose = checkNull(genericUtility.getColumnValue("prd_closed", dom1));
					System.out.println("prdCode :" + prdCode);
					System.out.println("cntCode :" + cntCode);
					System.out.println("frDate :" + frDate);
					System.out.println("toDate :" + toDate);
					System.out.println("prdClose :" + prdClose);
					//Added and commented by saurabh
					//sql = "select site_code,ref_code,prd_tblno from period_appl where ref_code like '%" + cntCode + "%'";
					cntryCodeSeries=distCommon.getDisparams("999999", "CAL_CRIT_ITEMSER", conn);
					if(cntryCodeSeries!=null && cntryCodeSeries.trim().length()>0)
					{
						ArrayList<String> aList= new ArrayList<String>(Arrays.asList(cntryCodeSeries.split(",")));
						for(int i=0,cntRef=0;i<aList.size();i++)
						{
							String ser= (String) aList.get(i);
							System.out.println("ser::::::"+ser);
							if(ser.trim().length()>0)
							{
								singleRefCode="'"+cntCode+"_"+ser.trim()+"'";
						    	abc.append(cntRef > 1? singleRefCode.trim():singleRefCode.trim()+",");
						    	cntRef++;
							}
						}
						 finalRefCode=abc.substring(0, abc.lastIndexOf(","));
					}
					System.out.println("finalRefCode:::::"+finalRefCode);
					if("PRD_TABLE_GEN".equalsIgnoreCase(obj_name.toUpperCase()) && (cntryCodeSeries!=null && cntryCodeSeries.trim().length()>0))
					{
						//sql = "select site_code,ref_code,prd_tblno from period_appl where ref_code like '%" + cntCode + "%' and ref_code not in ("+finalRefCode+")";
						//Added by saurabh For CHC menu option in domestic division's[050717|Start]
						//sql = "select distinct ref_code from period_appl where ref_code like '%" + cntCode + "%' and ref_code not in ("+finalRefCode+")";
						sql = "select distinct ref_code from period_appl where ref_code like '%" + cntCode + "%' ";
						//Added by saurabh For CHC menu option in domestic division's[050717|end]
					}
					else if("PRD_TABLE_GEN_OTH".equalsIgnoreCase(obj_name.toUpperCase()) && (cntryCodeSeries!=null && cntryCodeSeries.trim().length()>0))
					{
						//sql = "select site_code,ref_code,prd_tblno from period_appl where ref_code like '%" + cntCode + "%' and ref_code in ("+finalRefCode+")";
						sql = "select distinct ref_code from period_appl where ref_code like '%" + cntCode + "%' and ref_code in ("+finalRefCode+")";
					}
					else if(cntryCodeSeries==null || cntryCodeSeries.trim().length()==0)
					{
						//sql = "select site_code,ref_code,prd_tblno from period_appl where ref_code like '%" + cntCode + "%'";
						sql = "select distinct ref_code from period_appl where ref_code like '%" + cntCode + "%'";
					}
					
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					while (rs.next()) 
					{
						prdCnt++;
						//siteCode = rs.getString("site_code");
						refCode = rs.getString("ref_code");
						//prdTblno = rs.getString("prd_tblno");
						//System.out.println("siteCode===" + siteCode);
						System.out.println("refCode===" + refCode);
						//System.out.println("siteCode===" + siteCode);
						sql = "select count(1) from period_tbl where prd_code= ? and prd_tblno= ?";
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1, prdCode);
						pstmt1.setString(2, refCode);
						rs1 = pstmt1.executeQuery();
						if (rs1.next()) {
							cnt = rs1.getInt(1);
							System.out.println("CNT===" + cnt);
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
						if (cnt == 0) {
							System.out.println("Count is 0");
							valueXmlString.append("<Detail2 domID='" + prdCnt+ "' objContext=\"2\" selected=\"N\">\r\n");
							valueXmlString.append("<attribute  selected=\"Y\" updateFlag=\"A\"  status=\"N\" pkNames=\"\" />\r\n");
							valueXmlString.append("<prd_code>").append("<![CDATA[" + prdCode + "]]>").append("</prd_code>");
							valueXmlString.append("<prd_tblno>").append("<![CDATA[" + refCode + "]]>").append("</prd_tblno>");
							valueXmlString.append("<fr_date>").append("<![CDATA[" + frDate + "]]>").append("</fr_date>");
							valueXmlString.append("<to_date>").append("<![CDATA[" + toDate + "]]>").append("</to_date>");
							valueXmlString.append("<prd_closed>").append("<![CDATA[" + prdClose + "]]>").append("</prd_closed>");
							valueXmlString.append("<add_date>").append("<![CDATA[" + currDate + "]]>").append("</add_date>");
							valueXmlString.append("<chg_date>").append("<![CDATA[" + currDate + "]]>").append("</chg_date>");
							valueXmlString.append("<entry_start_dt>").append("<![CDATA[" + entryStartDt + "]]>").append("</entry_start_dt>");
							valueXmlString.append("<entry_end_dt>").append("<![CDATA[" + entryEndDt + "]]>").append("</entry_end_dt>");
							valueXmlString.append("</Detail2>");
						}
						else 
						{
							System.out.println("Count greater than 0");
							sql = "select fr_date,to_date,prd_closed,entry_start_dt,entry_end_dt from period_tbl where prd_code= ?  and prd_tblno=?";
							pstmt1 = conn.prepareStatement(sql);
							pstmt1.setString(1, prdCode);
							pstmt1.setString(2, refCode);
							rs1 = pstmt1.executeQuery();
							if (rs1.next()) {
								fromDateVal = rs1.getDate("fr_date");
								toDateVal = rs1.getDate("to_date");
								periodClosed = rs1.getString("prd_closed");
								entryStartDtVal = rs1.getDate("entry_start_dt");
								entryEndDtVal = rs1.getDate("entry_end_dt");
							}
							rs1.close();
							rs1 = null;
							pstmt1.close();
							pstmt1 = null;
							System.out.println("periodClosed===" + periodClosed);
							System.out.println("fromDateVal===" + fromDateVal);
							System.out.println("toDateVal===" + toDateVal);
							String fromDateStr = sdf.format(fromDateVal);
							String toDateStr = sdf.format(toDateVal);
							System.out.println("String format fromDateStr"+ fromDateStr);
							System.out.println("String format toDateStr"+ toDateStr);
							if (entryStartDtVal != null) 
							{
								entryStartDtStr = sdf.format(entryStartDtVal);
							}
							if (entryEndDtVal != null) 
							{
								entryEndDtStr = sdf.format(entryEndDtVal);
							}
							valueXmlString.append("<Detail2 domID='" + prdCnt+ "' objContext=\"2\" selected=\"N\">\r\n");
							valueXmlString.append("<attribute  selected=\"Y\" updateFlag=\"E\"  status=\"N\" pkNames=\"\" />\r\n");
							valueXmlString.append("<prd_code>").append("<![CDATA[" + prdCode + "]]>").append("</prd_code>");
							valueXmlString.append("<prd_tblno>").append("<![CDATA[" + refCode + "]]>").append("</prd_tblno>");
							valueXmlString.append("<fr_date>").append("<![CDATA[" + fromDateStr + "]]>").append("</fr_date>");
							valueXmlString.append("<to_date>").append("<![CDATA[" + toDateStr + "]]>").append("</to_date>");
							valueXmlString.append("<prd_closed>").append("<![CDATA[" + periodClosed + "]]>").append("</prd_closed>");
							valueXmlString.append("<add_date>").append("<![CDATA[" + currDate + "]]>").append("</add_date>");
							valueXmlString.append("<chg_date>").append("<![CDATA[" + currDate + "]]>").append("</chg_date>");
							valueXmlString.append("<entry_start_dt>").append("<![CDATA[" + entryStartDtStr+ "]]>").append("</entry_start_dt>");
							valueXmlString.append("<entry_end_dt>").append("<![CDATA[" + entryEndDtStr + "]]>").append("</entry_end_dt>");
							valueXmlString.append("</Detail2>");
						}
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
				else if (currentColumn.trim().equals("fr_date")) 
				{
					frDate = checkNull(genericUtility.getColumnValue("fr_date",dom));
					System.out.println("@@@@@@@@@@ fr_date called for case 2 frDate["+ frDate + "]");
					valueXmlString.append("<Detail2  objContext=\"2\" selected=\"Y\">\r\n");
					valueXmlString.append("<fr_date>").append("<![CDATA[" + frDate + "]]>").append("</fr_date>");
					valueXmlString.append("</Detail2>");
				}
				System.out.println("valueXmlString from case 2 :"+ valueXmlString);
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
			throw new ITMException(ex); //Added By Mukesh Chauhan on 05/08/19
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
	}// end of errorType

	private String checkNull(String input) {
		if (input == null) {
			input = "";
		}
		return input;
	}// end of checkNull

	public boolean getDetailValues(String prdCode, String prdTblNo,String frDate, String toDate, String prdClose, Connection conn)throws ITMException 
	{
		System.out.println("Enter in getDetailValues ");
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		Timestamp currDate = null;
		String sql = "";
		String errString = "";
		int cnt = 0;
		boolean recordExist = false;
		ibase.utility.E12GenericUtility genericUtility = null;
		genericUtility = new ibase.utility.E12GenericUtility();
		try 
		{
			currDate = new Timestamp(System.currentTimeMillis());
			System.out.println("TimeStamp>>>>>>>>>>" + currDate);
			System.out.println("Period code[" + prdCode+ "]-Period table Code[" + prdTblNo + "]-from date["+ frDate + "]To Date[" + toDate + "]period closed["+ prdClose + "]@@@");
			if ((prdCode != null && prdCode.trim().length() > 0) && (prdTblNo != null && prdTblNo.trim().length() > 0)) 
			{
				System.out.println("Not Nullll222222222");
				sql = "select count(1) from period_tbl where prd_code <> ? and prd_tblno= ? and prd_closed='N' and ?='N'";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, prdCode);
				pstmt.setString(2, prdTblNo);
				pstmt.setString(3, prdClose);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					cnt = rs.getInt(1);
					System.out.println("CNT===" + cnt);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if (cnt > 0) 
				{
					recordExist = true;
				}
			}
		} catch (Exception e) {
			System.out.println("Exception ::" + e.getMessage());
			errString = genericUtility.createErrorString(e);
			e.printStackTrace();
			throw new ITMException(e);
		} finally {
			try {
				System.out.println(">>>>>In finally errString:" + errString);
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
			} catch (Exception e) {
				System.out.println("Exception : " + e);
				e.printStackTrace();
				throw new ITMException(e);
			}
		}

		System.out.println("recordExist==============" + recordExist);
		return recordExist;

	}

	public boolean getPeriod(String prdCode, String prdTblNo, String frDate,String toDate, String prdClose, String addDate, Connection conn)throws ITMException 
	{
		System.out.println("Enter in getPeriod ");
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		Timestamp currDate = null;
		String sql = "";
		String errString = "";
		int cnt = 0;
		boolean periodStaus = false;
		Timestamp frmTimeStamp = null;
		ibase.utility.E12GenericUtility genericUtility = null;
		genericUtility = new ibase.utility.E12GenericUtility();
		try {
			currDate = new Timestamp(System.currentTimeMillis());
			System.out.println("TimeStamp>>>>>>>>>>" + currDate);
			System.out.println("Period code[" + prdCode+ "]-Period table Code[" + prdTblNo + "]-from date["+ frDate + "]To Date[" + toDate + "]period closed["+ prdClose + "]Add Date[" + addDate + "]@@@");
			if (frDate != null && frDate.trim().length() > 0 && (isDateValid(frDate) == true)) 
			{
				frmTimeStamp = Timestamp.valueOf(genericUtility.getValidDateString(frDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+ " 00:00:00.0");
				System.out.println("Date to be set frmTimeStamp@@@@@@@==="+ frmTimeStamp);
			}
			if ((prdCode != null && prdCode.trim().length() > 0) && (prdTblNo != null && prdTblNo.trim().length() > 0)) 
			{
				System.out.println("Not Nullll111111111");
				sql = "select count(1) from period_tbl where ? between fr_date and to_date and prd_code!= ? and prd_tblno= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, frmTimeStamp);
				pstmt.setString(2, prdCode);
				pstmt.setString(3, prdTblNo);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					cnt = rs.getInt(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				System.out.println("Cnt=====" + cnt);
				if (cnt > 0) {
					periodStaus = true;
				}
				System.out.println("periodStaus===" + periodStaus);
			}
		}// end today
		catch (Exception e) {
			System.out.println("Exception ::" + e.getMessage());
			errString = genericUtility.createErrorString(e);
			e.printStackTrace();
			throw new ITMException(e);
		} finally {
			try {
				System.out.println(">>>>>In finally errString:" + errString);
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
			} catch (Exception e) {
				System.out.println("Exception : " + e);
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		System.out.println("periodStaus*************=============="+ periodStaus);
		return periodStaus;
	}

	public static boolean isDateValid(String date) 
	{
		System.out.println("isDateValid!!!!!!!!!!");
		SimpleDateFormat sdf = null;
		ibase.utility.E12GenericUtility genericUtility = null;
		genericUtility = new ibase.utility.E12GenericUtility();
		try {

			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			sdf.setLenient(false);
			sdf.parse(date);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	private String getObjName(Document dom2,String objContext)
	{	
		Node elementName = null;
		NodeList elementList = null;
		String objName = "";
		elementList = dom2.getElementsByTagName("Detail"+objContext);
		elementName = elementList.item(0);
		if (elementName!=null && ("Detail"+objContext).equalsIgnoreCase(elementName.getNodeName()))
		{
			NamedNodeMap etlAttributes = elementName.getAttributes();
			if (etlAttributes!=null)
			{
				if (etlAttributes.getNamedItem("objName")!=null)
				{
					objName = etlAttributes.getNamedItem("objName").getNodeValue();
				}
			}
		}
		return objName;
	}
}
