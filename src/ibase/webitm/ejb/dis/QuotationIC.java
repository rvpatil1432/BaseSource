
package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javax.ejb.Stateless; 
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ibase.utility.E12GenericUtility;

import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.fin.FinCommon;

import ibase.webitm.utility.ITMException;

@Stateless

public class QuotationIC extends ValidatorEJB  {
	E12GenericUtility genericUtility = new E12GenericUtility();


	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag,
			String xtraParams) throws RemoteException, ITMException {
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
		} 
		catch (Exception e) 
		{
			throw new ITMException(e);
		}
		return (errString);
	}
	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag,
			String xtraParams) throws RemoteException, ITMException {

		String 	siteCode = ""; 
		String	status = "";
		String	itemSer="";
		String	itemCode="";
		String  intentNum=""; 
		String	confirm="";
		String lineNumEnq= "";
		String	lineNo = "" ;
		String	quotNumber = "";
		String 	unit="";
		Timestamp quotDate=null;
		Timestamp today = null;
		Timestamp validDate=null;
		int currentFormNo = 0;
		int childNodeListLength;
		int ctr = 0;
		int cnt = 0;
		String childNodeName1 = null;
		String childNodeName = null;
		String errString = "",enqNumber="";
		String taxClass="",taxChap="",taxEnv="";
		String errCode = "",quotDatestr="";
		String userId = "",loginSite="";
		String sql = "",suppCode="",sql1="";
		String errorType = "",quotReceived="";;
		String quatNumber="";
		String validDatestr="";
		String enqNumberbrow="", lineNumEnqbrow="", lineNobrow="";
		double quantitybrow=0,totalquantity=0;
		String validUptoStr="",quotNumberStr="";
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		int childNodeListLength1 = 0;
		NodeList parentNodeList1 = null;
		NodeList childNodeList1 = null;
		Node parentNode = null;
		Node parentNode1 =null; 
		Node childNode = null;
		Node childNode1 = null; 
		int ctr1=0;
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		double quantitySum=0 ;
		Connection conn = null;
		PreparedStatement pstmt = null, pstmt1 = null;
		ResultSet rs = null, rs1 = null;
		double quantity=0,quantity2 = 0,enqQuantity=0;
		FinCommon finCommon = new FinCommon();
		StringBuffer errStringXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root><Errors>");
		try 
		{
			conn = getConnection();
			userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			loginSite = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getDBDateFormat());
			today = java.sql.Timestamp.valueOf(sdf.format(new java.util.Date()).toString() + " 00:00:00.0");
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
				for (ctr = 0; ctr < childNodeListLength; ctr++) {
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equalsIgnoreCase("quot_date"))
					{
						quotDatestr =checkNullAndTrim (genericUtility.getColumnValue("quot_date", dom));
						siteCode = genericUtility.getColumnValue("site_code", dom);

						if (quotDatestr != null && quotDatestr.trim().length() > 0) 
						{
							quotDate = Timestamp.valueOf(genericUtility.getValidDateString(quotDatestr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");

							errString = finCommon.nfCheckPeriod("QUOT", quotDate,siteCode, conn);
							if (errString != null && errCode.trim().length() > 0)
							{
								errList.add(errString);
								errFields.add(childNodeName.toLowerCase());
							}
						}	 

					}
					else if (childNodeName.equalsIgnoreCase("enq_no")) 
					{

						enqNumber = checkNull(genericUtility.getColumnValue("enq_no", dom));


						if (enqNumber != null && enqNumber.trim().length() > 0)
						{
							sql=" select status, case when confirmed is null then 'N' else confirmed end as confirmed , valid_upto  " +
									" from enq_hdr  where enq_no = ? ";

							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, enqNumber);
							rs = pstmt.executeQuery();

							if (rs.next()) 
							{
								status = checkNull(rs.getString("status"));
								confirm = checkNull(rs.getString("confirmed"));
                                validDatestr =checkNull(rs.getString("valid_upto"));
                                validDate =rs.getTimestamp("valid_upto"); // 03-sep-2020 manoharan it is already timestamp not need to get in string and convert
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if (validDatestr != null && validDatestr.trim().length() > 0) {
                                // 03-sep-2020 manoharan trimestamp value assigned above
								//validDate = Timestamp.valueOf(genericUtility.getValidDateString(validDatestr, genericUtility.getApplDateFormat(),
								//		genericUtility.getDBDateFormat()) + " 00:00:00.0");

								if (status==null || status.trim().length()==0) 
								{
									status="C";
								} 
								else if ("C".equalsIgnoreCase(status) || "X".equalsIgnoreCase(status)) 
								{
									errCode = "VTENQ2";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}

								else if (!"Y".equalsIgnoreCase(confirm))
								{
									errCode = "VTENQNCONF";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}

								else if(validDate.before(today))
								{
									errCode = "VTVALDTERR";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
					}

					else if (childNodeName.equalsIgnoreCase("supp_code")) 
					{
						suppCode = checkNull(genericUtility.getColumnValue("supp_code", dom));
						enqNumber = checkNull(genericUtility.getColumnValue("enq_no", dom));
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom)); 
						quatNumber = checkNull(genericUtility.getColumnValue("quot_no", dom));
						if (suppCode == null || suppCode.trim().length()== 0)
						{
							errCode= "VTSUPCDNLL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						if (suppCode != null && suppCode.trim().length() > 0)
						{
							errCode = finCommon.isSupplier(siteCode, suppCode, "", conn);

						}
						if (errCode != null && errCode.trim().length() > 0)
						{
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						if (enqNumber != null && enqNumber.trim().length() > 0)
						{
							if (quatNumber == null ||  quatNumber.trim().length() == 0) 
							{
								sql=" select count(*) from pquot_hdr where	enq_no = ? and	 supp_code = ? "; 
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, enqNumber);
								pstmt.setString(2, suppCode);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									cnt=rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if( cnt == 0 )
								{
									sql = " select count(*)  from enq_supp where enq_no = ? and supp_code = ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1,enqNumber);
									pstmt.setString(2,suppCode);
									rs = pstmt.executeQuery();
									if (rs.next())
									{
										cnt = rs.getInt(1);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									if(cnt==0)
									{  
										errCode = "VTNOENQSUP";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								else
								{
									errCode = "VTALENQSUP";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}

							}

							else
							{
								
								sql=" select count(*) from pquot_hdr where	enq_no = ? and	 supp_code = ? "; 
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, enqNumber);
								pstmt.setString(2, suppCode);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									cnt=rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if(cnt==0)	

								{
									sql1 = " select count(*)  from enq_supp where enq_no = ? and supp_code = ? ";
									pstmt1 = conn.prepareStatement(sql1);
									pstmt1.setString(1,enqNumber);
									pstmt1.setString(2,suppCode);
									rs1 = pstmt1.executeQuery();
									if (rs1.next())
									{
										cnt = rs1.getInt(1);
									}
									rs1.close();
									rs1 = null;
									pstmt1.close();
									pstmt1 = null;

									if(cnt==0)
									{  
										errCode="VTNOENQSUP"; 
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}

									else
									{
										errCode = "VTALENQSUP";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
						}
					}


					else if (childNodeName.equalsIgnoreCase("item_ser")) 
					{
						itemSer = checkNull(genericUtility.getColumnValue("item_ser", dom));

						if (itemSer == null || itemSer.trim().length() ==0) 
						{
							errCode = "VTITEMSER5";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						if (itemSer != null && itemSer.trim().length() >0) 
						{
							sql = "select count(1) from item where item_ser = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemSer);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								cnt = rs.getInt(1);
							}

							pstmt.close();
							rs.close();
							pstmt = null;
							rs = null;
							if (cnt == 0) 
							{
								errCode = "VMITMSBC";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							}

						}
					}

					else if (childNodeName.equalsIgnoreCase("valid_upto")) 
					{

						quotNumberStr = checkNull(genericUtility.getColumnValue("quot_date",dom));
						validUptoStr = checkNull(genericUtility.getColumnValue("valid_upto", dom));						
						if(quotNumberStr != null && quotNumberStr.trim().length() > 0)
						{
							quotDate = Timestamp.valueOf(genericUtility.getValidDateString(quotDatestr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");

							if(validUptoStr != null && validUptoStr.trim().length() > 0)
							{
								validDate = Timestamp.valueOf(genericUtility.getValidDateString(validUptoStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");

								if(validDate==null || (quotDate.after(validDate)))
								{
									errCode = "VTVALUPTO";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}

							}
						}
					}

					else if (childNodeName.equalsIgnoreCase("quot_received")) 
					{
						quotReceived = checkNull(genericUtility.getColumnValue("quot_received",dom));
						quotNumber = checkNull(genericUtility.getColumnValue("quot_no", dom));	
						if(("E".equalsIgnoreCase(editFlag)) && ("N".equalsIgnoreCase(quotReceived)) )	
						{
							sql = " select count(*) from  pquot_det where quot_no = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, quotNumber);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if (cnt > 0) 
							{
								errCode = "VQTDETPSNT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						} 
					}
				} 
				break;

			case 2:

				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				for (ctr = 0; ctr < childNodeListLength; ctr++) {
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equalsIgnoreCase("enq_no")) {
						enqNumber = checkNull(genericUtility.getColumnValue("enq_no", dom));
						if (enqNumber != null && enqNumber.trim().length() > 0) 
						{
							sql = " select status from enq_hdr where enq_no = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, enqNumber);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								status=rs.getString("status");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (status == null || status.trim().length() == 0) 
							{
								status="C";

							}
							else if(("C".equalsIgnoreCase(status)) || ("X".equalsIgnoreCase(status)))
							{
								errCode = "VTENQ2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

						}
					} 

					else if (childNodeName.equalsIgnoreCase("quantity")) 
					{

						quantity = checkDoubleNull(genericUtility.getColumnValue("quantity", dom));

						intentNum =checkNull(genericUtility.getColumnValue("ind_no", dom));
						if ((intentNum != null && intentNum.trim().length() > 0) && quantity > 0 ) 
						{
							sql = " select (case when (quantity - ord_qty) is null then 0 else (quantity - ord_qty) end )  as quantity2 "+  
									" from indent  where ind_no= ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,intentNum);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								quantity2 = rs.getDouble(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if (quantity2 != quantity) 
							{
								errCode = "VTIDQTYDIF";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

							if (errCode == null || errCode.trim().length() == 0) 
							{
								lineNumEnq = checkNull(genericUtility.getColumnValue("line_no__enq", dom));
								lineNo = checkNull(genericUtility.getColumnValue("line_no", dom));
								quotNumber = checkNull(genericUtility.getColumnValue("quot_no", dom));

								if (quotNumber == null || quotNumber.trim().length() == 0) 
								{
									quotNumber = "@#@";
								}
								sql = "	select quantity  from enq_det where enq_no = ? and line_no = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, enqNumber);
								pstmt.setString(2, lineNo);
								rs = pstmt.executeQuery();
								if (rs.next()) 
								{
									enqQuantity = rs.getDouble(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								sql = "	select sum(quantity) from pquot_det " + 
										" where enq_no = ? and line_no__enq =  ? and QUOT_NO <> ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, enqNumber);
								pstmt.setString(2, lineNo);
								pstmt.setString(3, quatNumber);
								rs = pstmt.executeQuery();
								if (rs.next()) {
									quantitySum = rs.getDouble(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								parentNodeList1 = dom2.getElementsByTagName("Detail2");
								parentNode1 = parentNodeList1.item(0);
								childNodeList1 = parentNode1.getChildNodes();
								childNodeListLength1 = childNodeList1.getLength();
								for (ctr1 = 0; ctr1 < childNodeListLength1; ctr1++) 
								{
									childNode1 = childNodeList1.item(ctr1);
									childNodeName1 = childNode1.getNodeName();
									lineNumEnqbrow = checkNull(genericUtility.getColumnValue("line_no__enq", dom));
									quantitybrow =  checkDoubleNull(genericUtility.getColumnValue("quantity", dom));
									lineNobrow = checkNull(genericUtility.getColumnValue("line_no", dom)).trim();
									if((enqNumber.trim().equals(enqNumberbrow.trim())) && (lineNumEnq.trim().equals(lineNumEnqbrow.trim())) && (!(lineNo.trim().equals(lineNobrow.trim()))))  
									{

										totalquantity=totalquantity + quantitybrow ;
									}
								}
								if(quantity + quantitySum + totalquantity > enqQuantity)
								{
									errCode = "VTEXQUOT";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());

								}
							}
						}	
					}

					else if (childNodeName.equalsIgnoreCase("item_code")) 
					{
						quotReceived= checkNull(genericUtility.getColumnValue("quot_received", dom));
						if ((quotReceived== null || quotReceived.trim().length()==0) || (("Y").equalsIgnoreCase(quotReceived)))
						{
							itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
							enqNumber = checkNull(genericUtility.getColumnValue("enq_no", dom));
							siteCode = checkNull(genericUtility.getColumnValue("site_code", dom1));
							suppCode = checkNull(genericUtility.getColumnValue("supp_code", dom1));
							lineNo = checkNull(genericUtility.getColumnValue("line_no", dom));
							intentNum = checkNull(genericUtility.getColumnValue("ind_no", dom));
							quotNumber = checkNull(genericUtility.getColumnValue("quot_no", dom));
							errCode=this.isItem(quotReceived, itemCode,siteCode,conn);
							if ( errCode != null && errCode.trim().length() >0)
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else
							{
								if ( errCode == null || errCode.trim().length()==0)
								{
									sql = " select count(*)  from enq_det  where enq_no = ? and item_code = ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, enqNumber);
									pstmt.setString(2, itemCode);
									rs = pstmt.executeQuery();
									if (rs.next()) 
									{
										cnt = rs.getInt(1);
									}

									pstmt.close();
									rs.close();
									pstmt = null;
									rs = null;
									if (cnt == 0)
									{
										errCode = "VTNOITENQ";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}

								}
								else
								{
									sql=" select count(*) from enq_supp  where	enq_no = ? " +
											" and supp_code = ? "; 

									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, enqNumber);
									pstmt.setString(2, suppCode);
									rs = pstmt.executeQuery();
									if (rs.next()) 
									{
										cnt = rs.getInt(1);
									}

									pstmt.close();
									rs.close();
									pstmt = null;
									rs = null;
									if (cnt == 0)
									{
										errCode = "VTSUPNOTEN";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}	
						}
						else
						{
							errCode = "VQDTNOTALW";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}


					else if (childNodeName.equalsIgnoreCase("tax_chap")) 
					{
						taxChap = checkNull(genericUtility.getColumnValue("tax_chap", dom));

						if ( taxChap.trim().length() > 0) 
						{
							sql = " select count(*) from taxchap where  tax_chap = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, taxChap);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0) 
							{
								errCode = "VTTAXCHAP1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					else if (childNodeName.equalsIgnoreCase("tax_class")) 
					{
						taxClass = checkNull(genericUtility.getColumnValue("tax_class", dom));

						if ( taxClass.trim().length() > 0) 
						{
							sql = " select count(*) from taxclass where  tax_class = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, taxClass);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0) 
							{
								errCode = "VTTAXCLA1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					else if (childNodeName.equalsIgnoreCase("tax_env")) 
					{
						taxEnv = checkNull(genericUtility.getColumnValue("tax_env", dom));
						if ( taxEnv.trim().length() > 0) 
						{
							sql = "SELECT COUNT(*) FROM taxenv WHERE tax_env = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, taxEnv);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if (cnt == 0) {
								errCode = "VMTAENV1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					else if (childNodeName.equalsIgnoreCase("ind_no")) 
					{

						intentNum = genericUtility.getColumnValue("ind_no",dom);
						enqNumber = genericUtility.getColumnValue("enq_no",dom);
						if (enqNumber != null && enqNumber.trim().length() > 0)
						{
							if (intentNum != null && intentNum.trim().length() > 0) 
							{
								sql= "select count(*)  from enq_det	where enq_no = ? and ind_no = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,enqNumber);
								pstmt.setString(2,intentNum);
								rs = pstmt.executeQuery();
								if (rs.next()) 
								{
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if (cnt == 0) 
								{
									errCode = "VTINVENQ";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());

								}
							}
						}
					}

					else if (childNodeName.equalsIgnoreCase("unit")) 
					{

						unit = genericUtility.getColumnValue("unit",dom);
						sql = "select count(*) from uom where unit = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,unit);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							cnt = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (cnt == 0) 
						{
							errCode = "VTUNIT1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}

				}


				break;
			}
			int errListSize = errList.size();
			int count = 0;
			String errFldName = null;
			if (errList != null && errListSize > 0) {
				for (count = 0; count < errListSize; count++) {
					errCode = errList.get(count);
					errFldName = errFields.get(count);
					System.out.println(" testing :errCode .:" + errCode);
					errString = getErrorString(errFldName, errCode, userId);
					errorType = errorType(conn, errCode);
					if (errString.length() > 0) {
						String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8,
								errString.indexOf("<trace>"));
						bifurErrString = bifurErrString + errString.substring(errString.indexOf("</trace>") + 8,
								errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
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


		}
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
	public String itemChanged(String xmlString, String xmlString1,
			String xmlString2, String objContext, String currentColumn,
			String editFlag, String xtraParams) throws RemoteException,ITMException {
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
			valueXmlString = itemChanged(dom, dom1, dom2, objContext,currentColumn, editFlag, xtraParams);
		} catch (Exception e) {

			throw new ITMException(e);

		}
		return valueXmlString;
	}

	public String itemChanged(Document dom, Document dom1, Document dom2,
			String objContext, String currentColumn, String editFlag,
			String xtraParams) throws RemoteException, ITMException {

		System.out.println("###########ITEMCHANGE FOR CASE###################");
		String childNodeName = null;
		String sql = "";
		StringBuffer valueXmlString = new StringBuffer();
		int ctr = 0, cnt = 0;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int currentFormNo = 0;
		String	siteDescr = "",descr = "", itemSer="";
		//Timestamp timestamp = null;
		SimpleDateFormat sdf = null;
		String siteCode="";
		int pos = 0;
		String supplName="",quotNumber;
		String enqNumber="",suppCode="";
		String columnValue = "";
		int childNodeListLength=0;
		String  supplCode="" ;
		String  remarks="";
		String	intentNum="";
		String	payTerm="";
		String	brand="",unit="";
		String	packInst="";
		String	lineEnqNumber="";
		String  specificRef="",packSize="",itemCode=""; 
		String  intentNumber="";
		String 	freight="";
		String 	taxChap="",taxClass="",taxEnv="";	
		String  stationTo="",stationFr="",custCode="";
		Timestamp reqDate=null;
		String quotdate="";
		String  reStr="";
		String loginSiteCode="";
		String chguser="",currCode="",exchRate="",currCodeBase="";
		int  quantity = 0;
		double lineNumber=0;
		try {
			System.out.println("**********ITEMCHANGE FOR CASE*********************");
			conn = getConnection();
			DistCommon distComm = new DistCommon();
			if (objContext != null && objContext.trim().length() > 0) 
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			loginSiteCode = getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
			chguser = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			Timestamp sysDate = new Timestamp(System.currentTimeMillis());
			FinCommon finCommon = new FinCommon();
			System.out.println("loginSite[" + loginSiteCode + "][chguserhdr "+ chguser + "]");
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			System.out.println("**********ITEMCHANGE FOR CASE" + currentFormNo+ "**************");
			switch (currentFormNo) {
			case 1:
				valueXmlString.append("<Detail1>");
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0;
				childNodeListLength = childNodeList.getLength();

				if (currentColumn.trim().equalsIgnoreCase("itm_defaultedit")) 
				{

					suppCode=checkNull(genericUtility.getColumnValue("supp_code", dom));
					sql = " select supp_name  from supplier where supp_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, suppCode);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						supplName = rs.getString("supp_name");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if (supplName!= null && supplName.trim().length() > 0) 
					{
						valueXmlString.append("<supp_name protect =\"1\">").append("<![CDATA[" + supplName + "]]>").append("</supp_name>");
					}
					else
					{	
						valueXmlString.append("<supp_name protect =\"0\">").append("<![CDATA[" + supplName + "]]>").append("</supp_name>");
					}
				} 
				else if (currentColumn.trim().equalsIgnoreCase("itm_default")) 
				{

					valueXmlString.append("<site_code>").append("<![CDATA[" + loginSiteCode + "]]>").append("</site_code>");

					sql = " select descr from site where site_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, loginSiteCode);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						siteDescr = rs.getString(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<site_descr>").append("<![CDATA[" + siteDescr + "]]>").append("</site_descr>");
					valueXmlString.append("<quot_date>").append("<![CDATA[" + sdf.format(sysDate) + "]]>").append("</quot_date>");
					valueXmlString.append("<status_date>").append("<![CDATA[" + sdf.format(sysDate) + "]]>").append("</status_date>");
					valueXmlString.append("<status>").append("<![CDATA[U]]>").append("</status>");

				}

				else if (currentColumn.trim().equalsIgnoreCase("enq_no"))
				{
					enqNumber = checkNull(genericUtility.getColumnValue("enq_no", dom));

					if (enqNumber != null && enqNumber.trim().length() > 0) 
					{
						sql = " select count(*) from enq_supp where enq_no= ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, enqNumber);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if(cnt == 1)
						{
							sql = " select supp_code from enq_supp where enq_no= ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, enqNumber);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								suppCode = rs.getString(1);

							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							valueXmlString.append("<supp_code>").append("<![CDATA[" + suppCode + "]]>").append("</supp_code>");


							sql = " select supp_name from supplier where supp_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, suppCode);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								supplName = rs.getString("supp_name");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("descr@supp_name@" + descr);


							if (supplName != null && supplName.trim().length() > 0)  
							{
								valueXmlString.append("<supp_name protect =\"1\">").append("<![CDATA[" + supplName + "]]>").append("</supp_name>");

							}
							else
							{	
								valueXmlString.append("<supp_name protect =\"0\">").append("<![CDATA[" + supplName + "]]>").append("</supp_name>");
							}

						}
					}
					else

					{

						valueXmlString.append("<supp_code>").append("<![CDATA[" + " " + "]]>").append("</supp_code>");
						valueXmlString.append("<supp_name>").append("<![CDATA[" + " " + "]]>").append("</supp_name>");
					}
					sql = " select item_ser  from enq_hdr where enq_no = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, enqNumber);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						itemSer = checkNull(rs.getString(1));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<item_ser>").append("<![CDATA[" + itemSer + "]]>").append("</item_ser>");

					sql = " select descr from itemser where item_ser= ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemSer);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						descr = checkNull(rs.getString(1));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<itemser_descr>").append("<![CDATA[" + descr + "]]>").append("</itemser_descr>");

				} 
				else if (currentColumn.trim().equalsIgnoreCase("supp_code")) 
				{

					suppCode = checkNull(genericUtility.getColumnValue("supp_code", dom));
					sql = " select supp_name,curr_code from supplier where supp_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, suppCode);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						supplName = rs.getString(1);
						currCode=rs.getString(2);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if (supplName != null && supplName.trim().length() > 0) 
					{
						valueXmlString.append("<supp_name protect =\"1\">").append("<![CDATA[" + supplName + "]]>").append("</supp_name>");

					}
					else
					{	
						valueXmlString.append("<supp_name protect =\"0\">").append("<![CDATA[" + supplName + "]]>").append("</supp_name>");

					}
				} 

				else if (currentColumn.trim().equalsIgnoreCase("site_code")) 
				{
					siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
					sql = " Select descr from Site where Site_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteCode);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						descr = checkNull(rs.getString(1));

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<site_descr>").append("<![CDATA[" + descr + "]]>").append("</site_descr>");
				}

				else if (currentColumn.trim().equalsIgnoreCase("item_ser")) 
				{
					itemSer = checkNull(genericUtility.getColumnValue("item_ser", dom));

					sql = " Select descr from itemser where item_ser = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemSer);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						descr = checkNull(rs.getString(1));

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<itemser_descr>").append("<![CDATA[" + descr + "]]>").append("</itemser_descr>");
				}
				else if (currentColumn.trim().equalsIgnoreCase("curr_code")) 
				{

					currCode = genericUtility.getColumnValue("curr_code", dom);
					siteCode = genericUtility.getColumnValue("site_code", dom);
					quotdate = genericUtility.getColumnValue("quot_date", dom);

					if (currCode != null && currCode.trim().length() > 0)
					{
						exchRate = String.valueOf(finCommon.getDailyExchRateSellBuy(currCode, quotdate, siteCode, "", "S", conn));


						System.out.println("exchRate>>>>>>>"+exchRate);
					}
					sql = "select descr from currency where curr_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, currCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						descr = rs.getString("descr");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<currency_descr>").append("<![CDATA[" + descr + "]]>")
					.append("</currency_descr>");

					valueXmlString.append("<exch_rate>").append("<![CDATA[" + exchRate + "]]>")
					.append("</exch_rate>");
					setNodeValue(dom, "exch_rate", getAbsString(exchRate));


					if (currCode != null && currCode.trim().length() > 0) 
					{
						sql = "select a.curr_code from finent a, site b where b.fin_entity = a.fin_entity and b.site_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, siteCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							currCodeBase = rs.getString("curr_code");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;


						System.out.println("lsCurrcodeBase--["+currCodeBase+"]lsCurrCode--["+currCode+"]");
						if ((currCodeBase != null && currCodeBase.trim().length() > 0) && currCode.equalsIgnoreCase(currCodeBase)) {

							valueXmlString.append("<exch_rate protect = \"1\">")
							.append("<![CDATA[" + exchRate + "]]>").append("</exch_rate>");

							setNodeValue(dom, "exch_rate", getAbsString(exchRate));

						} else {
							valueXmlString.append("<exch_rate protect = \"0\">")
							.append("<![CDATA[" + exchRate + "]]>").append("</exch_rate>");

							setNodeValue(dom, "exch_rate", getAbsString(exchRate));

						}
					}
					reStr = itemChanged(dom, dom1, dom2, objContext, "exch_rate", editFlag, xtraParams);
					pos = reStr.indexOf("<Detail1>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail1>");
					reStr = reStr.substring(0, pos);
					valueXmlString.append(reStr);
				}


				valueXmlString.append("</Detail1>");
				break;

			case 2:
				valueXmlString.append("<supp_code protect =\"1\">").append("<![CDATA[" + suppCode + "]]>").append("</supp_code>");
				parentNodeList = dom.getElementsByTagName("Detail2");
				valueXmlString.append("<Detail2>");
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				parentNode = parentNodeList.item(0);
				ctr = 0;
				String lineNo="";

				childNodeListLength = childNodeList.getLength();
				System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");

				if (currentColumn.trim().equalsIgnoreCase("itm_default")) 
				{
					lineNo = checkNull(genericUtility.getColumnValue("line_no", dom));

					if (Integer.parseInt(lineNo) > 0) 
					{
						valueXmlString.append("<line_no>").append("<![CDATA[" + lineNo + "]]>").append("</line_no>");
					}
					quotNumber = checkNull(genericUtility.getColumnValue("quot_no", dom1));
					enqNumber = checkNull(genericUtility.getColumnValue("enq_no", dom1));
					valueXmlString.append("<quot_no>").append("<![CDATA[" + quotNumber + "]]>").append("</quot_no>");
					valueXmlString.append("<enq_no>").append("<![CDATA[" + enqNumber + "]]>").append("</enq_no>");
					setNodeValue(dom1, "enq_no", enqNumber);
					if ( enqNumber !=null && enqNumber.trim().length() > 0) 
					{

						reStr = itemChanged(dom, dom1,dom2, objContext, "enq_no", editFlag, xtraParams);

						pos = reStr.indexOf("<Detail2>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail2>");
						reStr = reStr.substring(0, pos);
						valueXmlString.append(reStr);
					}
				} 

				else if(currentColumn.trim().equalsIgnoreCase("enq_no"))
				{

					supplCode = checkNull(genericUtility.getColumnValue("supp_code", dom1));
					enqNumber=checkNull(genericUtility.getColumnValue("enq_no", dom));
					sql = " select item_code from enq_supp  where enq_no = ? and supp_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, enqNumber);
					pstmt.setString(2, supplCode);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						itemCode = checkNull(rs.getString(1));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
						sql = " select descr from item where item_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							descr = checkNull(rs.getString(1));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						valueXmlString.append("<item_code>").append("<![CDATA[" + itemCode + "]]>").append("</item_code>");
						valueXmlString.append("<item_descr>").append("<![CDATA[" + descr + "]]>").append("</item_descr>");
						
						if(itemCode != null && itemCode.trim().length() > 0)
						{
						sql = " select quantity ,remarks ,ind_no ,unit ,pay_term , brand ,spec_ref , pack_instr from enq_det " + 
								" where enq_no = ? and item_code = ?  ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, enqNumber);
						pstmt.setString(2, itemCode);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{ 
							quantity = rs.getInt("quantity");
							remarks = checkNull(rs.getString("remarks"));
							intentNumber= checkNull(rs.getString("ind_no"));
							unit=checkNull(rs.getString("unit"));
							payTerm = checkNull(rs.getString("pay_term"));
							brand=checkNull(rs.getString("brand"));
							specificRef=checkNull(rs.getString("spec_ref"));
							packInst=checkNull(rs.getString("pack_instr"));

						}

						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						valueXmlString.append("<quantity>").append("<![CDATA[" + quantity + "]]>").append("</quantity>");
						valueXmlString.append("<remarks>").append("<![CDATA[" + remarks + "]]>").append("</remarks>");
						valueXmlString.append("<ind_no>").append("<![CDATA[" + intentNum + "]]>").append("</ind_no>");
						valueXmlString.append("<unit>").append("<![CDATA[" + unit + "]]>").append("</unit>");
						valueXmlString.append("<pay_term>").append("<![CDATA[" + payTerm + "]]>").append("</pay_term>");
						valueXmlString.append("<brand>").append("<![CDATA[" + brand + "]]>").append("</brand>");
						valueXmlString.append("<spec_ref>").append("<![CDATA[" + specificRef + "]]>").append("</spec_ref>");
						valueXmlString.append("<pack_size>").append("<![CDATA[" + packInst + "]]>").append("</pack_size>");
					}
				}

				else if(currentColumn.trim().equalsIgnoreCase("item_code"))
				{
					System.out.println("item_code>>>>>>..");
					itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
					itemSer = checkNull(genericUtility.getColumnValue("item_ser", dom));
					siteCode = checkNull(genericUtility.getColumnValue("site_code", dom1));
					System.out.println("ls_tranid_ref@@"+itemCode);

					sql = " select descr,unit  from item where item_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCode);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						descr = checkNull(rs.getString(1));
						unit = checkNull(rs.getString(2));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<unit>").append("<![CDATA[" + unit + "]]>").append("</unit>");
					valueXmlString.append("<item_descr>").append("<![CDATA[" + descr + "]]>").append("</item_descr>");

					enqNumber = checkNull(genericUtility.getColumnValue("enq_no", dom));

					sql = " select quantity, remarks, ind_no, unit, pay_term ,brand ,spec_ref , pack_instr from enq_det " + 
							" where enq_no = ? and item_code = ?  ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,enqNumber);
					pstmt.setString(2,itemCode);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{ 

						quantity = rs.getInt(1);
						remarks = rs.getString(2);
						intentNum= rs.getString(3);
						unit=rs.getString(4);
						payTerm = rs.getString(5);
						brand=rs.getString(6);
						specificRef=rs.getString(7);
						packInst=rs.getString(8);

					}

					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<quantity>").append("<![CDATA[" + quantity + "]]>").append("</quantity>");
					valueXmlString.append("<remarks>").append("<![CDATA[" + remarks + "]]>").append("</remarks>");
					valueXmlString.append("<ind_no>").append("<![CDATA[" + intentNum + "]]>").append("</ind_no>");
					valueXmlString.append("<unit>").append("<![CDATA[" + unit + "]]>").append("</unit>");
					valueXmlString.append("<pay_term>").append("<![CDATA[" + payTerm + "]]>").append("</pay_term>");
					valueXmlString.append("<brand>").append("<![CDATA[" + brand + "]]>").append("</brand>");
					valueXmlString.append("<spec_ref>").append("<![CDATA[" + specificRef + "]]>").append("</spec_ref>");
					valueXmlString.append("<pack_size>").append("<![CDATA[" + packInst + "]]>").append("</pack_size>");

					taxEnv = checkNull(genericUtility.getColumnValue("tax_env", dom));
					taxClass = checkNull(genericUtility.getColumnValue("tax_class", dom));
					taxChap = checkNull(genericUtility.getColumnValue("tax_chap", dom));
					if (taxChap == null || taxChap.trim().length() == 0) 
					{
						taxChap = checkNull(distComm.getTaxChap(itemCode, itemSer, "S", supplCode, siteCode, conn));
						valueXmlString.append("<tax_chap>").append("<![CDATA[" + taxChap + "]]>").append("</tax_chap>");
						System.out.println("taxchapter>>>>>>>>>>."+taxChap);
					}


					if (taxClass == null || taxClass.trim().length() == 0) 
					{
						taxClass = checkNull(distComm.getTaxClass(itemSer, supplCode, "S", siteCode, conn));
						valueXmlString.append("<tax_class>").append("<![CDATA[" + taxClass + "]]>").append("</tax_class>");
						System.out.println("taxchapter>>>>>>>>>>."+taxChap);
					}



					if (taxEnv == null || taxEnv.trim().length() == 0) 
					{
						supplCode = checkNull(genericUtility.getColumnValue("supp_code", dom1));

						sql = " select tax_env  from supplieritem  where supp_code = ? and item_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, supplCode);
						pstmt.setString(2, itemCode);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							taxEnv = rs.getString("tax_env");

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (taxEnv != null && taxEnv.trim().length() > 0) 
						{
							valueXmlString.append("<tax_env>").append("<![CDATA[" + taxEnv + "]]>").append("</tax_env>");
							System.out.println("taxEnv>>>>>>>>>>."+taxEnv);
						} 
						else 
						{
							sql = " select tax_env  from supplier where supp_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, supplCode);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								taxEnv = rs.getString("tax_env");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (taxEnv != null && taxEnv.trim().length() > 0) 
							{
								valueXmlString.append("<tax_env>").append("<![CDATA[" + taxEnv + "]]>").append("</tax_env>");
							}
							else
							{
								sql = "select stan_code from site where site_code =?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, siteCode);
								rs = pstmt.executeQuery();
								if (rs.next()) 
								{
									stationFr = rs.getString(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								sql = "select stan_code from customer where cust_code =?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, custCode);
								rs = pstmt.executeQuery();
								if (rs.next()) 
								{
									stationTo = rs.getString(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								taxEnv = this.getTaxEnv(stationFr, stationTo, taxChap, taxClass, siteCode);							
								valueXmlString.append("<tax_env>").append("<![CDATA[" + distComm.getTaxEnv(stationFr,stationTo,taxChap, taxClass, siteCode, conn) + "]]>").append("</tax_env>");
							} 
						} 

					} 

				}	

				else if ((currentColumn.trim().equalsIgnoreCase("line_no__enq"))) 
				{

					lineEnqNumber = checkNull(genericUtility.getColumnValue("line_no__enq", dom));
					enqNumber = checkNull(genericUtility.getColumnValue("enq_no", dom));
					sql = " select item_code, quantity, unit, pay_term, freight ,ind_no, req_date, brand , spec_ref ," +
							" pack_instr  from enq_det where  enq_no =  ?  and  line_no =  ? ";

					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, enqNumber);
                    //pstmt.setString(2, lineNo);
                    pstmt.setString(2, lineEnqNumber);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						itemCode =checkNull(rs.getString("item_code"));
						quantity = rs.getInt("quantity");
						unit=checkNull(rs.getString("unit"));
						payTerm = checkNull(rs.getString("pay_term"));
						freight = checkNull(rs.getString("freight"));
						intentNum= checkNull(rs.getString("ind_no"));
						reqDate = rs.getTimestamp("req_date");
						brand=checkNull(rs.getString("brand"));
						specificRef=checkNull(rs.getString("spec_ref"));
						packInst=checkNull(rs.getString("pack_instr"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					sql = " select descr  from item where item_code =  ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCode);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						descr = checkNull(rs.getString(1));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<item_code>").append("<![CDATA[" + itemCode + "]]>").append("</item_code>");
					valueXmlString.append("<item_descr>").append("<![CDATA[" + descr + "]]>").append("</item_descr>");
					valueXmlString.append("<quantity>").append("<![CDATA[" + quantity + "]]>").append("</quantity>");
					valueXmlString.append("<remarks>").append("<![CDATA[" + remarks + "]]>").append("</remarks>");
					valueXmlString.append("<ind_no>").append("<![CDATA[" + intentNum + "]]>").append("</ind_no>");
					valueXmlString.append("<unit>").append("<![CDATA[" + unit + "]]>").append("</unit>");
					valueXmlString.append("<pay_term>").append("<![CDATA[" + payTerm + "]]>").append("</pay_term>");
					valueXmlString.append("<req_date>").append("<![CDATA[" + reqDate + "]]>").append("</req_date>");
					valueXmlString.append("<brand>").append("<![CDATA[" + brand + "]]>").append("</brand>");
					valueXmlString.append("<spec_ref>").append("<![CDATA[" + specificRef + "]]>").append("</spec_ref>");
					valueXmlString.append("<pack_size>").append("<![CDATA[" + packSize + "]]>").append("</pack_size>");

				}



				valueXmlString.append("</Detail2>");
				break;
			}
			valueXmlString.append("</Root>");

		}catch (Exception e) 
		{
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
			} catch (Exception d) 
			{
				d.printStackTrace();

				throw new ITMException(d);

			}
		}
		return valueXmlString.toString();
	}

	private String checkNull(String input) {
		if (input == null) {
			input = "";
		}
		return (input);
	}
	private String errorType(Connection conn , String errorCode)
	{
		String msgType = "";
		PreparedStatement pstmt = null ; 
		ResultSet rs = null;
		try
		{			
			String  sql = "SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ?";
			pstmt = conn.prepareStatement(sql);			
			pstmt.setString(1,errorCode);			
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				msgType = rs.getString("MSG_TYPE");
			}			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
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
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}		
		return msgType;
	}


	public String checkNullAndTrim( String inputVal )
	{
		if ( inputVal == null )
		{
			inputVal = "";
		}
		else
		{
			inputVal = inputVal.trim();
		}
		return inputVal;
	}
	private static String getAbsString(String str) {
		return (str == null || str.trim().length() == 0 || "null".equalsIgnoreCase(str.trim()) ? "" : str.trim());
	}

	private double checkDoubleNull(String str) {
		if (str == null || str.trim().length() == 0) {
			return 0.0;
		} else {
			return Double.parseDouble(str);
		}
	}


	private static void setNodeValue(Document dom, String nodeName, String nodeVal) throws Exception {
		Node tempNode = dom.getElementsByTagName(nodeName).item(0);

		if (tempNode != null) {
			if (tempNode.getFirstChild() == null) {
				CDATASection cDataSection = dom.createCDATASection(nodeVal);
				tempNode.appendChild(cDataSection);
			} else {
				tempNode.getFirstChild().setNodeValue(nodeVal);
			}
		}
		tempNode = null;
	}
}