/********************************************************
	Title : AdjIssueIC[D16ASUN021]
	Date  : 29/04/16
	Developer: Chandrashekar

 ********************************************************/
package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;

import java.text.SimpleDateFormat;
import java.util.*;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import ibase.utility.E12GenericUtility;
import java.text.DecimalFormat;
import java.text.NumberFormat;

//import org.apache.poi.hssf.record.formula.functions.Round;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.ejb.Stateless; 
@Stateless 

public class AdjIssueIC extends ValidatorEJB implements AdjIssueICLocal, AdjIssueICRemote
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	ibase.webitm.ejb.fin.FinCommon finCommon = new ibase.webitm.ejb.fin.FinCommon();
	ibase.webitm.ejb.dis.DistCommon disCommon = new ibase.webitm.ejb.dis.DistCommon();
	static String inventoryAcct ="N";
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			if (xmlString != null && xmlString.trim().length() > 0 )
			{
				dom = parseString(xmlString);
				System.out.println("xmlString["+xmlString+"]");
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0 )
			{
				dom1 = parseString(xmlString1);
				System.out.println("xmlString1["+xmlString1+"]");
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0 )
			{
				dom2 = parseString(xmlString2);
				System.out.println("xmlString2["+xmlString2+"]");				
			}

			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [AdjIssueIC][wfValData( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e);
		}
		return(errString);
	}
	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{  
		String childNodeName = null;
		String errString = "";
		String errCode = "";
		String userId = "";
		String sql = "";
		String errorType = "";
		String deptCode="",bankCode="",lineNo="",locCode="", itemCode = "",itemSer= "",sundryCode = "",sundryType = "";
		String refIdFor="",refSerFor = "",lotNo = "",lotSl = "",quantityStr ="",tranId= "";
		String acctCodeDr = "",active = "",acctCodeCr = "",cctrCodeCr = "", cctrCodeDr = "";
		String priceList = "",noArtStr = "";
		int cnt = 0;
		int ctr=0, siteItmCnt =0,itmCnt = 0,noArt=0;
		int childNodeListLength;
		double rcpAmt=0,rcpAmtNew=0,rcpAmtStatus=0.0;
		double  isInvAmt=0.0,chqAmt=0.0,quantity = 0.0;
		double oldQty  =0.0,stkQty = 0.0;
		double rate =0.0,grossWeight =0.0,netWeight =0.0,tarWeight =0.0;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;	
		Node parentNode = null;
		Node childNode = null;
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement pstmt = null ;
		ResultSet rs = null;
		ConnDriver connDriver = new ConnDriver();
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
		int currentFormNo =0;
		String siteCode="",rcpAmtStr="",refSer="",refNo="",prdCode="";
		java.util.Date tranDate = null;
		SimpleDateFormat dateFormat2 = null;
		String chkDec="";   //added by manish mhatre on 02-dec-2019
		String unit="";     //added by manish mhatre on 02-dec-2019
		try
		{	dateFormat2 = new SimpleDateFormat(genericUtility.getApplDateFormat());
		System.out.println("@@@@@@@@ wfvaldata called");
		//Changes and Commented By Bhushan on 09-06-2016 :START
		//conn = connDriver.getConnectDB("DriverITM");
		conn = getConnection();
		//Changes and Commented By Bhushan on 09-06-2016 :END 
		connDriver = null;
		userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
		//	inventoryAcct = disCommon.getDisparams("999999", "INVENTORY_ACCT", conn);   //commented by manish mhatre 
		inventoryAcct = finCommon.getFinparams("999999", "INVENTORY_ACCT", conn); //added by manish mhatre on 9-jan-2019

		if(objContext != null && objContext.trim().length()>0)
		{
			currentFormNo = Integer.parseInt(objContext);
		}		
		switch(currentFormNo)
		{
		case 1 :
			parentNodeList = dom.getElementsByTagName("Detail1");
			parentNode = parentNodeList.item(0);
			childNodeList = parentNode.getChildNodes();
			childNodeListLength = childNodeList.getLength();

			for(ctr = 0; ctr < childNodeListLength; ctr++)
			{
				childNode = childNodeList.item(ctr);
				childNodeName = childNode.getNodeName();
				if (childNodeName.equalsIgnoreCase("tran_date"))
				{
					siteCode = this.genericUtility.getColumnValue("site_code", dom);
					tranDate = dateFormat2.parse(genericUtility.getColumnValue("tran_date",dom));
					if(siteCode == null || siteCode.trim().length()==0)
					{
						errCode = "NULLSITE";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}else
					{
						cnt=0;
						sql = "select count(*) from site where site_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, siteCode);
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
							errCode = "VMSITE1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}else
						{
							//Changes and Commented By Ajay on 20-12-2017:START
							//errCode = nfCheckPeriod("SAL",tranDate,siteCode);
							errCode=finCommon.nfCheckPeriod("SAL", tranDate, siteCode, conn);
							//Changes and Commented By Ajay on 20-12-2017:END
							if(errCode != null && errCode.trim().length() > 0)
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}



				}
				else if (childNodeName.equalsIgnoreCase("site_code"))
				{
					siteCode = this.genericUtility.getColumnValue("site_code", dom);
					cnt=0;
					sql = "select count(*) from site where site_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteCode);
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
						errCode = "VMSITE1";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
				}else if (childNodeName.equalsIgnoreCase("dept_code"))
				{
					deptCode = this.genericUtility.getColumnValue("dept_code", dom);
					if(deptCode != null && deptCode.trim().length() > 0 )
					{	cnt=0;
					sql = "select count(*) from department where dept_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, deptCode);
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
						errCode = "VTDEPT1";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
					}
				}else if (childNodeName.equalsIgnoreCase("item_ser"))
				{
					itemSer = this.genericUtility.getColumnValue("item_ser", dom);
					if(itemSer == null || itemSer.trim().length() ==0)
					{
						errCode = "VMITSER";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
					else
					{
						cnt=0;
						sql = "select count(*) from itemser where item_ser = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemSer);
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
							errCode = "VTITEMSER1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}else if (childNodeName.equalsIgnoreCase("price_list"))
				{
					priceList = this.genericUtility.getColumnValue("price_list", dom);
					if (priceList != null && priceList.trim().length()>0)
					{
						cnt=0;
						sql = "select count(*) from pricelist where price_list = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, priceList);
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
							errCode = "VTPLIST1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}

			}// end for
			break;  // case 1 end
		case 2:
			parentNodeList = dom.getElementsByTagName("Detail2");
			parentNode = parentNodeList.item(0);
			childNodeList = parentNode.getChildNodes();
			childNodeListLength = childNodeList.getLength();
			System.out.println("@@@@@@@@@@@@childNodeListLength["+childNodeListLength+"]");
			for(ctr = 0; ctr < childNodeListLength; ctr++)
			{
				childNode = childNodeList.item(ctr);
				childNodeName = childNode.getNodeName();
				System.out.println("childNodeName>>>"+ctr+">>>"+childNodeName);
				if (childNodeName.equalsIgnoreCase("loc_code"))
				{		cnt=0;
				locCode = this.genericUtility.getColumnValue("loc_code", dom);
				if(locCode == null || locCode.trim().length() ==0)
				{
					errCode = "VTLOC11";
					errList.add(errCode);
					errFields.add(childNodeName.toLowerCase());
				}else
				{
					sql = "select count(*) from location where loc_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, locCode);
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
						errCode = "VMLOC1";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}

				}
				}else if (childNodeName.equalsIgnoreCase("item_code"))
				{
					itemCode = this.genericUtility.getColumnValue("item_code", dom);
					siteCode = this.genericUtility.getColumnValue("site_code", dom1);
					itemSer = this.genericUtility.getColumnValue("item_ser", dom1);
					if(itemCode ==null || itemCode.trim().length() ==0)
					{
						errCode = "STKVALITCO";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
					else
					{
						cnt=0;
						sql = "select count(*) from item where item_code= ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
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
							errCode = "VTITEM1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						sql = "select count(*) from siteitem where item_code= ? and item_ser= ? and site_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						pstmt.setString(2, itemSer);
						pstmt.setString(3, siteCode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							siteItmCnt = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (siteItmCnt == 0)
						{
							sql = "select count(*) from item where item_code= ? and 	 item_ser = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							pstmt.setString(2, itemSer);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								itmCnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (itmCnt == 0)
							{
								errCode = "INVSITEITM";
								//errCode = "VTITEM1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

				}else if(childNodeName.equalsIgnoreCase("sundry_code"))
				{
					sundryCode = this.genericUtility.getColumnValue("sundry_code", dom);
					System.out.println("sundryCode>>>>"+sundryCode);
					if(sundryCode != null && sundryCode.trim().length() > 0 )
					{
						siteCode = this.genericUtility.getColumnValue("site_code", dom1);
						sundryType = this.genericUtility.getColumnValue("sundry_type", dom);
						System.out.println("siteCode>>>>"+siteCode);
						System.out.println("sundryType>>>>"+sundryType);
						errCode = finCommon.isSundryCode(siteCode, sundryType, sundryCode, refSer, conn);
						if(errCode != null && errCode.trim().length() > 0)
						{
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}else if(childNodeName.equalsIgnoreCase("ref_id__for") || childNodeName.equalsIgnoreCase("ref_ser__for") )
				{
					refIdFor = this.genericUtility.getColumnValue("ref_id__for", dom);
					refSerFor = this.genericUtility.getColumnValue("ref_ser__for", dom);
					itemCode = this.genericUtility.getColumnValue("item_code", dom);
					siteCode = this.genericUtility.getColumnValue("site_code", dom1);
					lotNo = this.genericUtility.getColumnValue("lot_no", dom);
					lotSl = this.genericUtility.getColumnValue("lot_sl", dom);
					locCode = this.genericUtility.getColumnValue("loc_code", dom);
					if((refSerFor.trim().length()>0 && (refIdFor == null || refIdFor.trim().length() == 0)) ||
							(refIdFor.trim().length()>0 &&  (refSerFor == null || refSerFor.trim().length() == 0)))
					{
						errCode = "VTINV001";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
					else
					{
						if(refSerFor.trim().length()>0 && refIdFor.trim().length() >0 )
						{	cnt=0;
						sql = "select count(*) from invtrace	where site_code = ? " +
								"and item_code = ?	and loc_code = ? and lot_no = ?  " +
								"and lot_sl = ? and ref_ser__for = ?   and ref_id__for = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, siteCode);
						pstmt.setString(2, itemCode);
						pstmt.setString(3, locCode);
						pstmt.setString(4, lotNo);
						pstmt.setString(5, lotSl);
						pstmt.setString(6, refSerFor);
						pstmt.setString(7, refIdFor);
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
							errCode = "VTINV002";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						}
					}
				}else if(childNodeName.equalsIgnoreCase("lot_no"))
				{
					lotNo = this.genericUtility.getColumnValue("lot_no", dom);
					if(lotNo == null)
					{
						errCode = "VTLOTEMPTY";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}	
				}else if(childNodeName.equalsIgnoreCase("lot_sl"))
				{
					lotSl = this.genericUtility.getColumnValue("lot_sl", dom);
					if(lotSl == null)
					{
						errCode = "VMLOTSL1";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
				}else if(childNodeName.equalsIgnoreCase("quantity"))
				{
					itemCode = this.genericUtility.getColumnValue("item_code", dom);
					siteCode = this.genericUtility.getColumnValue("site_code", dom1);
					lotNo = this.genericUtility.getColumnValue("lot_no", dom);
					lotSl = this.genericUtility.getColumnValue("lot_sl", dom);
					locCode = this.genericUtility.getColumnValue("loc_code", dom);
					tranId = this.genericUtility.getColumnValue("tran_id", dom);
					lineNo = this.genericUtility.getColumnValue("line_no", dom);
					quantityStr = this.genericUtility.getColumnValue("quantity", dom);
					unit=  this.genericUtility.getColumnValue("unit", dom);
					System.out.println("tranId>>>>"+tranId+"]");
					if(tranId == null || tranId.trim().length() ==0 || "null".equalsIgnoreCase(tranId))
					{
						tranId="@@";
					}
					if(quantityStr != null && quantityStr.trim().length() > 0)
					{
						try
						{
							quantity = Double.parseDouble(quantityStr);
						} catch (NumberFormatException n)
						{
							quantity = 0;
						}
					}
					System.out.println("quantityStr>>>>>"+quantityStr+"]");
					System.out.println("quantity>>>>>"+quantity+"]");
					if(quantity <= 0)
					{
						errCode = "VTINVQTY03";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}else
					{
						sql = "select sum(case when quantity is null then 0 else quantity end) as lc_old_qty from 	adj_issrcpdet " +
								" where tran_id = ? and   line_no = ? and item_code = ? and lot_no = ? and lot_sl = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, tranId);
						pstmt.setString(2, lineNo);
						pstmt.setString(3, itemCode);
						pstmt.setString(4, lotNo);
						pstmt.setString(5, lotSl);

						rs = pstmt.executeQuery();
						if (rs.next())
						{
							oldQty = rs.getDouble("lc_old_qty");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						sql = "select (quantity - (case when alloc_qty is null then 0 else alloc_qty end) - " +
								"(case when hold_qty is null then 0 else hold_qty end)) as lc_stkqty	from stock " +
								"where item_code = ? and site_code = ? and loc_code = ? " +
								"and lot_no = ?  and lot_sl = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						pstmt.setString(2, siteCode);
						pstmt.setString(3, locCode);
						pstmt.setString(4, lotNo);
						pstmt.setString(5, lotSl);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							stkQty = rs.getDouble("lc_stkqty");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						System.out.println("quantity>>>>"+quantity);
						System.out.println("oldQty>>>>"+oldQty);
						System.out.println("stkQty>>>>"+stkQty);
						if(quantity-oldQty > stkQty)
						{
							errCode = "VXSTK2";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}

						//added by manish mhatre on 02-dec-2019
						//start manish
						else
						{
							errCode=disCommon.checkDecimal(quantity, unit, conn);
							if(errCode!=null && errCode.trim().length()>0)
							{
								errCode = "VTUOMDEC3";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}   //end manish	
					}
				}else if(childNodeName.equalsIgnoreCase("acct_code__dr"))
				{
					acctCodeDr = this.genericUtility.getColumnValue("acct_code__dr", dom);
					if("Y".equalsIgnoreCase(inventoryAcct))
					{
						cnt=0;
						sql = "select count(*) from  accounts where acct_code  = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, acctCodeDr);
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
							sql = "select active from  accounts where acct_code  = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, acctCodeDr);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								active = rs.getString("active");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(!"Y".equalsIgnoreCase(active))
							{
								errCode = "VMACCTA";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}else
						{
							errCode = "VMACCTR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}else if(childNodeName.equalsIgnoreCase("acct_code__cr"))
				{
					acctCodeCr = this.genericUtility.getColumnValue("acct_code__cr", dom);
					if("Y".equalsIgnoreCase(inventoryAcct))
					{
						cnt=0;
						sql = "select count(*) from  accounts where acct_code  = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, acctCodeCr);
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
							sql = "select active from  accounts where acct_code  = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, acctCodeCr);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								active = rs.getString("active");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(!"Y".equalsIgnoreCase(active))
							{
								errCode = "VMACCTA";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}else
						{
							errCode = "VMACCTR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}else if(childNodeName.equalsIgnoreCase("cctr_code__dr"))
				{
					cctrCodeDr = this.genericUtility.getColumnValue("cctr_code__dr", dom);
					if("Y".equalsIgnoreCase(inventoryAcct))
					{
						//added by manish mhatre on 3-jan-2020
						errCode = finCommon.isCctrCode(acctCodeDr, cctrCodeDr, " ", conn);
						if (errCode != null && errCode.trim().length() > 0) {
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}//end manish
						/*		cnt=0;
									sql = "select count(*)  from costctr where cctr_code  = ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, cctrCodeDr);
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
										errCode = "VMCCTR";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}*/
					}
				}else if(childNodeName.equalsIgnoreCase("cctr_code__cr"))
				{
					cctrCodeCr = this.genericUtility.getColumnValue("cctr_code__cr", dom);
					if("Y".equalsIgnoreCase(inventoryAcct))
					{

						//added by manish mhatre on 3-jan-2020
						errCode = finCommon.isCctrCode(acctCodeCr, cctrCodeCr, " ", conn);
						if (errCode != null && errCode.trim().length() > 0) {
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}//end manish
						/*cnt=0;
									sql = "select count(*)  from costctr where cctr_code  = ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, cctrCodeCr);
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
										errCode = "VMCCTR";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}*/
					}
				}else if(childNodeName.equalsIgnoreCase("rate"))
				{
					rate = Double.parseDouble(this.genericUtility.getColumnValue("rate", dom));
					System.out.println("rate>>>>"+rate+"]");
					if(rate <= 0)
					{
						errCode = "VTRATE2";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
				}else if(childNodeName.equalsIgnoreCase("gross_weight"))
				{
					grossWeight = Double.parseDouble(this.genericUtility.getColumnValue("gross_weight", dom));
					System.out.println("grossWeight>>>>"+grossWeight+"]");
					if(grossWeight < 0)
					{
						errCode = "VTGRSWTNEG";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
				}else if(childNodeName.equalsIgnoreCase("net_weight"))
				{
					netWeight = Double.parseDouble(this.genericUtility.getColumnValue("net_weight", dom));
					grossWeight = Double.parseDouble(this.genericUtility.getColumnValue("gross_weight", dom));
					System.out.println("grossWeight>>>>"+grossWeight+"]");
					System.out.println("netWeight>>>>"+netWeight+"]");
					if(netWeight < 0)
					{
						errCode = "VTNETWTNEG";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}else if(netWeight > grossWeight)
					{
						errCode = "VTGRNET";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
				}else if(childNodeName.equalsIgnoreCase("tare_weight"))
				{
					tarWeight = Double.parseDouble(this.genericUtility.getColumnValue("tare_weight", dom));
					System.out.println("tarWeight>>>>"+tarWeight+"]");
					if(tarWeight < 0)
					{
						errCode = "VTTARWTNEG";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
				}else if(childNodeName.equalsIgnoreCase("no_art"))
				{
					noArtStr = this.genericUtility.getColumnValue("no_art", dom);
					if(noArtStr != null && noArtStr.trim().length() > 0)
					{
						try
						{
							noArt = Integer.parseInt(noArtStr);
						} catch (NumberFormatException n)
						{
							noArt = 0;
						}
					}else
					{
						noArt = 0;
					}
					if(noArt < 0)
					{
						errCode = "VTNARTNEG";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
				}
			} // end for		
			
			//Modified by Pratiksha A. on [16/03/2021][Start]
			System.out.println("869>>>>>currLineNo LINE NO:" + lineNo+ " lotNo ["+lotNo+" ]locCode ["+locCode+" ]itemCode ["+itemCode+" ]lotSlDet ["+lotSl+"]");
			lotNo = genericUtility.getColumnValue("lot_no", dom);
			lotSl = genericUtility.getColumnValue("lot_sl", dom);
			locCode = genericUtility.getColumnValue("loc_code", dom);
			itemCode = genericUtility.getColumnValue("item_code", dom);
			lineNo =genericUtility.getColumnValue("line_no", dom);
			Node currDetail1 = null;
			NodeList detailList1 = dom2.getElementsByTagName("Detail2");
			int noOfDetails = detailList1.getLength();
			String lotNoDet ="",locCodeDet ="",itemCodeDet ="",lotSlDet ="",lineNoDet;

			for (int ctr1 = 0; ctr1 < noOfDetails; ctr1++)
			{
				currDetail1 = detailList1.item(ctr1);				
				lineNoDet =genericUtility.getColumnValueFromNode("line_no", currDetail1);
				lotNoDet =genericUtility.getColumnValueFromNode("lot_no", currDetail1);
				locCodeDet =genericUtility.getColumnValueFromNode("loc_code", currDetail1);
				itemCodeDet =genericUtility.getColumnValueFromNode("item_code", currDetail1);
				lotSlDet =genericUtility.getColumnValueFromNode("lot_sl", currDetail1);				
				System.out.println("currDetail1 LINE NO:" + currDetail1+ " lotNoDet ["+lotNoDet+" ]locCodeDet ["+locCodeDet+" ]itemCodeDet ["+itemCodeDet+" ]lotSlDet ["+lotSlDet+"]");	
				if (!lineNo.trim().equalsIgnoreCase(lineNoDet.trim())) 
				{
                    System.out.println("inside currDetail1 LINE NO:"+lineNo +""+lineNoDet);
					if(lotNoDet.equalsIgnoreCase(lotNo) && locCodeDet.equalsIgnoreCase(locCode) && itemCodeDet.equalsIgnoreCase(itemCode) && lotSlDet.equalsIgnoreCase(lotSl) )
					{
						System.out.println("849>>>>>>>>");
						errCode = "VTDUPREC1";
						errList.add(errCode);
						errFields.add("item_code"); 					
					}					
				}	                     
			}//--end of for duplicate	
			//Modified by Pratiksha A. on [16/03/2021][End]

			break;  // case 2 end		
		}
	
		int errListSize = errList.size();
		cnt = 0;
		String errFldName = null;
		if(errList != null && errListSize > 0)
		{
			for(cnt = 0; cnt < errListSize; cnt ++)
			{
				errCode = errList.get(cnt);
				errFldName = errFields.get(cnt);
				System.out.println("errCode .........." + errCode);
				errString = getErrorString(errFldName, errCode, userId);
				errorType = errorType(conn , errCode);
				if(errString.length() > 0)
				{
					String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8, errString.indexOf("<trace>"));
					bifurErrString = bifurErrString + errString.substring(errString.indexOf("</trace>") + 8, errString.indexOf("</Errors>"));
					errStringXml.append(bifurErrString);
					errString = "";
				}
				if(errorType.equalsIgnoreCase("E"))
				{
					break;
				}
			}
			errList.clear();
			errList = null;
			errFields.clear();
			errFields = null;
			errStringXml.append("</Errors> </Root> \r\n");
		}
		else
		{
			errStringXml = new StringBuffer("");
		}		        
		}
		catch(Exception e)
		{
			e.printStackTrace();
			errString = e.getMessage();
			throw new ITMException(e);
		}
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
		errString = errStringXml.toString();
		return errString;
	}

	// method for item change
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		System.out.println("xmlString............."+xmlString);
		System.out.println("xmlString1............"+xmlString1);
		System.out.println("xmlString2............"+xmlString2);
		try
		{   
			if(xmlString != null && xmlString.trim().length() > 0)
			{
				dom = parseString(xmlString);
			}
			if(xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom1 = parseString(xmlString1);
			}
			if(xmlString2 != null && xmlString2.trim().length() > 0)
			{
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [AdjIssueIC][itemChanged( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e);
		}
		return valueXmlString;
	}


	// method for item change
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		String sql = "";
		StringBuffer valueXmlString = new StringBuffer();
		int ctr = 0;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		E12GenericUtility genericUtility= new  E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();

		int currentFormNo =0, childNodeListLength=0;
		String columnValue="",hdrTranId = "",itemCode = "";
		String tranDateStr = "", loginSiteDesc= "", siteCode = "",loginSite= "", deptCode = "", deptDesc = "";
		String stkOpt = "",itemDescr = "",unit = "",locCode = "",lotNo = "",lotSl = "",locDescr = "",quantityStr="",rateStr = "";
		String grossWeightStr = "",tareWeightStr ="",packCode = "";
		String custCode = "",reStr = "",expdateStr="",effDateStr ="",mfgDateStr ="",expDateStr="",netWeightStr="";
		int pos=0;
		double amount= 0.0,quantity =0.0,rate =0.0,netWeight =0.0,grossWeight =0.0,tareWeight=0.0,stkQuantity =0.0;
		double shipperQty = 0.0 ,integralQty = 0.0,qtyPerArt =0.0,grossper=0.0,tareper=0.0,netper=0.0;
		long noArt =0;
		double noOfArt = 0d, shipperSize = 0d, mGrossWeight = 0d, mNetWeight = 0d, mGrosswt = 0d, mNetWt = 0d, mTareWt = 0d;
		try
		{
			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String sysDate = sdf.format(currentDate.getTime());		
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;

			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext.trim());
			}

			System.out.println("Now the date is :=>  " + sysDate);

			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");

			System.out.println("**********ITEMCHANGE FOR CASE"+currentFormNo+"**************");
			switch(currentFormNo)
			{
			case 1 :
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0; 
				childNodeListLength = childNodeList.getLength();
				System.out.println("currentColumn[" + currentColumn + "]columnValue ==> '" + columnValue + "'");
				valueXmlString.append("<Detail1>");
				if ( currentColumn.trim().equalsIgnoreCase( "itm_default" ))
				{
					loginSite = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSite");
					sql = "SELECT DESCR FROM SITE WHERE SITE_CODE = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, loginSite);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						loginSiteDesc = rs.getString("DESCR");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<tran_date><![CDATA[").append(sysDate).append("]]></tran_date>\r\n");
					valueXmlString.append("<ref_ser protect='1'><![CDATA[").append("ADJISS").append("]]></ref_ser>\r\n");
					valueXmlString.append("<eff_date><![CDATA[").append(sysDate).append("]]></eff_date>\r\n");
					valueXmlString.append("<site_code><![CDATA[").append(loginSite).append("]]></site_code>\r\n");
					valueXmlString.append("<site_descr><![CDATA[").append(loginSiteDesc).append("]]></site_descr>\r\n");
					valueXmlString.append("<order_id protect='0'><![CDATA[").append("").append("]]></order_id>\r\n");
					valueXmlString.append("<remarks protect='0'><![CDATA[").append("").append("]]></remarks>\r\n");
				}else if(currentColumn.trim().equalsIgnoreCase( "itm_defaultedit" ) )
				{
					tranDateStr = checkNull(genericUtility.getColumnValue("tran_date",dom));
					effDateStr = checkNull(genericUtility.getColumnValue("eff_date",dom));
					//valueXmlString.append("<tran_date protect='1'><![CDATA[").append(tranDateStr).append("]]></tran_date>\r\n");
					valueXmlString.append("<tran_date protect='0'><![CDATA[").append(tranDateStr).append("]]></tran_date>\r\n");  //added by manish mhatre on 25-03-20[unprotect tran_date]
					valueXmlString.append("<eff_date protect='1'><![CDATA[").append(effDateStr).append("]]></eff_date>\r\n");
				}else if(currentColumn.trim().equalsIgnoreCase( "site_code" ) )
				{
					siteCode = checkNull(genericUtility.getColumnValue("site_code",dom));
					sql = "SELECT DESCR FROM SITE WHERE SITE_CODE = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						loginSiteDesc = rs.getString("DESCR");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<site_descr><![CDATA[").append(loginSiteDesc).append("]]></site_descr>\r\n");

				}else if( currentColumn.trim().equalsIgnoreCase( "tran_date" ) )
				{
					tranDateStr = checkNull(genericUtility.getColumnValue("tran_date",dom));
					valueXmlString.append("<eff_date><![CDATA[").append(tranDateStr).append("]]></eff_date>\r\n");
				}else if( currentColumn.trim().equalsIgnoreCase( "dept_code" ) )
				{
					deptCode = checkNull(genericUtility.getColumnValue("dept_code",dom));
					sql = "SELECT DESCR FROM DEPARTMENT WHERE DEPT_CODE = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, deptCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						deptDesc = rs.getString("DESCR");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<department_descr><![CDATA[").append(deptDesc).append("]]></department_descr>\r\n");
				}
				valueXmlString.append("</Detail1>");
				break;
			case 2 :
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0; 
				valueXmlString.append("<Detail2>");
				childNodeListLength = childNodeList.getLength();
				System.out.println("currentColumn[" + currentColumn + "] ==> '" + columnValue + "'");
				if ( currentColumn.trim().equalsIgnoreCase("itm_default") )
				{	

					hdrTranId = this.genericUtility.getColumnValue("tran_id", dom1);
					mfgDateStr = checkNull(genericUtility.getColumnValue("mfg_date",dom));
					expDateStr = checkNull(genericUtility.getColumnValue("exp_date",dom));
					netWeightStr = checkNull(genericUtility.getColumnValue("net_weight",dom));
					valueXmlString.append("<tran_id><![CDATA[").append(hdrTranId).append("]]></tran_id>\r\n");
					valueXmlString.append("<mfg_date protect='1'><![CDATA[").append(mfgDateStr).append("]]></mfg_date>\r\n");
					valueXmlString.append("<exp_date protect='1'><![CDATA[").append(expDateStr).append("]]></exp_date>\r\n");
					//valueXmlString.append("<net_weight protect='1'><![CDATA[").append(netWeightStr).append("]]></net_weight>\r\n");

				}else if( currentColumn.trim().equalsIgnoreCase( "itm_defaultedit" ) )
				{
					mfgDateStr = checkNull(genericUtility.getColumnValue("mfg_date",dom));
					expDateStr = checkNull(genericUtility.getColumnValue("exp_date",dom));
					netWeightStr = checkNull(genericUtility.getColumnValue("net_weight",dom));
					valueXmlString.append("<mfg_date protect='1'><![CDATA[").append(mfgDateStr).append("]]></mfg_date>\r\n");
					valueXmlString.append("<exp_date protect='1'><![CDATA[").append(expDateStr).append("]]></exp_date>\r\n");
					//	valueXmlString.append("<net_weight protect='1'><![CDATA[").append(netWeightStr).append("]]></net_weight>\r\n");
				}else if( currentColumn.trim().equalsIgnoreCase( "item_code" ) )
				{
					siteCode = this.genericUtility.getColumnValue("site_code", dom1);
					tranDateStr = this.genericUtility.getColumnValue("tran_date", dom1);
					itemCode = checkNull(genericUtility.getColumnValue("item_code",dom));

					sql = "SELECT DESCR, UNIT,STK_OPT FROM ITEM WHERE ITEM_CODE = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						itemDescr = checkNull(rs.getString("descr"));
						unit = checkNull(rs.getString("unit"));
						stkOpt = checkNull(rs.getString("stk_opt"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if("1".equalsIgnoreCase(stkOpt))
					{
						valueXmlString.append("<lot_no><![CDATA[").append("").append("]]></lot_no>\r\n");
						valueXmlString.append("<lot_sl><![CDATA[").append("").append("]]></lot_sl>\r\n");
					}
					locCode = checkNull(genericUtility.getColumnValue("loc_code",dom));
					lotNo = checkNull(genericUtility.getColumnValue("lot_no",dom));
					lotSl = checkNull(genericUtility.getColumnValue("lot_sl",dom));

					valueXmlString.append("<item_descr><![CDATA[").append(itemDescr).append("]]></item_descr>\r\n");
					valueXmlString.append("<unit><![CDATA[").append(unit).append("]]></unit>\r\n");
					gbfValAcct(itemCode,siteCode,locCode,lotNo,lotSl,conn,valueXmlString,dom1);
				}else if( currentColumn.trim().equalsIgnoreCase( "loc_code" ) )
				{
					siteCode = this.genericUtility.getColumnValue("site_code", dom1);
					itemCode = checkNull(genericUtility.getColumnValue("item_code",dom));
					locCode = checkNull(genericUtility.getColumnValue("loc_code",dom));
					lotNo = checkNull(genericUtility.getColumnValue("lot_no",dom));
					lotSl = checkNull(genericUtility.getColumnValue("lot_sl",dom));


					sql = "SELECT DESCR FROM LOCATION  WHERE LOC_CODE = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, locCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						locDescr = checkNull(rs.getString("descr"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<location_descr><![CDATA[").append(locDescr).append("]]></location_descr>\r\n");
					gbfValAcct(itemCode,siteCode,locCode,lotNo,lotSl,conn,valueXmlString,dom1);
				}else if( currentColumn.trim().equalsIgnoreCase( "quantity" ) ||  currentColumn.trim().equalsIgnoreCase( "rate" ))
				{
					quantityStr = checkDouble(genericUtility.getColumnValue("quantity",dom));
					rateStr = checkDouble(genericUtility.getColumnValue("rate",dom));
					amount=Double.parseDouble(quantityStr)*Double.parseDouble(rateStr);
					valueXmlString.append("<amount><![CDATA[").append(amount).append("]]></amount>\r\n");
				}else if( currentColumn.trim().equalsIgnoreCase( "lot_no" ) ||  currentColumn.trim().equalsIgnoreCase( "lot_sl" ))
				{
					siteCode = this.genericUtility.getColumnValue("site_code", dom1);
					itemCode = checkNull(genericUtility.getColumnValue("item_code",dom));
					locCode = checkNull(genericUtility.getColumnValue("loc_code",dom));
					lotNo = checkNull(genericUtility.getColumnValue("lot_no",dom));
					lotSl = checkNull(genericUtility.getColumnValue("lot_sl",dom));
					rateStr = checkDouble(genericUtility.getColumnValue("rate",dom));
					quantityStr = checkNull(genericUtility.getColumnValue("quantity",dom));
					if(quantityStr.trim().length()>0)
					{
						quantity =Double.parseDouble(quantityStr);
					}
					gbfValAcct(itemCode,siteCode,locCode,lotNo,lotSl,conn,valueXmlString,dom1);

					packCode = checkNull(genericUtility.getColumnValue("pack_code",dom));
					//System.out.println("PavanDOM[  "+genericUtility.serializeDom(dom)+"]\n\n\n");				
					//Pavan R on may19[no of art to consider from item_lot_packsize, item and if not found then packing master]										
					sql = "select (case when shipper_size is null then 0 else shipper_size end) as shipper_size, "
							+ "(case when gross_weight is null then 0 else gross_weight end) as lc_gross_weigth, "
							+ "(case when net_weight is null then 0 else net_weight end) as lc_net_weight "
							+ "from item_lot_packsize where item_code = ? "
							+ "and ? >= lot_no__from and ? <= lot_no__to";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCode);
					pstmt.setString(2, lotNo);
					pstmt.setString(3, lotNo);
					rs = pstmt.executeQuery();				
					if(rs.next())
					{					
						shipperSize = rs.getDouble("shipper_size");							
						mGrossWeight= rs.getDouble("lc_gross_weigth");
						mNetWeight= rs.getDouble("lc_net_weight");					
						System.out.println("shipperSize["+shipperSize+"] mGrossWeight["+mGrossWeight+"] mNetWeight["+mNetWeight+"]");					
						if (shipperSize > 0) 
						{		
							double mod = quantity/shipperSize;
							noOfArt = getRndamt(mod , "X", 1);
							System.out.println("noOfArt["+noOfArt+"]");
							valueXmlString.append("<no_art><![CDATA[").append(noOfArt).append("]]></no_art>\r\n");
							if(noOfArt > 0)
							{							
								mGrosswt = ((mGrossWeight/shipperSize) * quantity); 						
								mNetWt = ((mNetWeight/shipperSize) * quantity);
								//System.out.println("mGrosswt [ "+mGrosswt+" ]");
								if(mGrossWeight > 0)
								{
									valueXmlString.append("<gross_weight ><![CDATA[").append(getRequiredDecimals(mGrosswt,3)).append("]]></gross_weight>\r\n");
									valueXmlString.append("<net_weight ><![CDATA[").append(getRequiredDecimals(mNetWt,3)).append("]]></net_weight>\r\n");										
									mTareWt=mGrosswt - mNetWt;
									System.out.println("mGrossWeight > 0 mTareWt [ "+mTareWt+" ]");
									valueXmlString.append("<tare_weight ><![CDATA[").append(getRequiredDecimals(mTareWt, 3)).append("]]></tare_weight>\r\n");
								}									
							} //if(noOfArt > 0)																
						}//if(shipperSize > 0)  						
					}						
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println("shipperSize["+shipperSize+"]");
					if(shipperSize == 0)
					{
						//End - Pavan R on may19

						noArt=disCommon.getNoArt(siteCode,custCode,itemCode,packCode,quantity,'B',shipperQty,integralQty,conn);
						System.out.println("noArt>>>>"+noArt);
						valueXmlString.append("<no_art><![CDATA[").append(noArt).append("]]></no_art>\r\n");
						if(noArt ==0)
						{
							sql = "SELECT (CASE WHEN QTY_PER_ART IS NULL THEN 0 ELSE QTY_PER_ART END)  as QTY_PER_ART FROM STOCK WHERE " +
									"	ITEM_CODE = ? AND SITE_CODE = ? AND		LOC_CODE  = ? AND " 
									+ " LOT_NO	 = ?  AND		LOT_SL = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							pstmt.setString(2, siteCode);
							pstmt.setString(3, locCode);
							pstmt.setString(4, lotNo);
							pstmt.setString(5, lotSl);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								qtyPerArt = rs.getDouble("QTY_PER_ART");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("quantity>>>"+quantity);
							System.out.println("qtyPerArt>>>"+qtyPerArt);
							if(qtyPerArt >0)
							{
								valueXmlString.append("<no_art><![CDATA[").append(Math.round(quantity/qtyPerArt)).append("]]></no_art>\r\n");
							}

						}

						sql = "select a.quantity as	quantity,	a.gross_weight	as gross_weight,	a.tare_weight	as tare_weight,	" +
								" a.net_weight as net_weight from 	stock a  where a.item_code = ? " +
								" and	a.site_code = ? and	a.loc_code  =? and	a.lot_no	 	= ? and	a.lot_sl	 	= ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						pstmt.setString(2, siteCode);
						pstmt.setString(3, locCode);
						pstmt.setString(4, lotNo);
						pstmt.setString(5, lotSl);
						rs = pstmt.executeQuery();
						while(rs.next())
						{
							stkQuantity = rs.getDouble("quantity");
							grossWeight = rs.getDouble("gross_weight");
							tareWeight = rs.getDouble("tare_weight");
							netWeight = rs.getDouble("net_weight");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (stkQuantity>0)
						{
							grossper=grossWeight/stkQuantity;
							tareper=tareWeight/stkQuantity;
							netper=netWeight/stkQuantity;
							grossWeight=grossper*quantity;
							tareWeight=tareper*quantity;
							netWeight =netper*quantity;
						}
						valueXmlString.append("<gross_weight><![CDATA[").append(grossWeight).append("]]></gross_weight>\r\n");
						valueXmlString.append("<net_weight><![CDATA[").append(netWeight).append("]]></net_weight>\r\n");
						valueXmlString.append("<tare_weight><![CDATA[").append(tareWeight).append("]]></tare_weight>\r\n");
					}
					//rate item change
					setNodeValue( dom, "rate", rateStr );
					reStr=itemChanged(dom,dom1, dom2, objContext,"rate",editFlag,xtraParams);
					pos = reStr.indexOf("<Detail2>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail2>");
					reStr = reStr.substring(0,pos);
					valueXmlString.append(reStr);
				}
				else if( currentColumn.trim().equalsIgnoreCase( "gross_weight" ) ||  currentColumn.trim().equalsIgnoreCase( "tare_weight" ))
				{
					grossWeightStr = checkDouble(genericUtility.getColumnValue("gross_weight",dom));
					tareWeightStr = checkDouble(genericUtility.getColumnValue("tare_weight",dom));
					netWeight=Double.parseDouble(grossWeightStr)-Double.parseDouble(tareWeightStr);
					valueXmlString.append("<net_weight ><![CDATA[").append(netWeight).append("]]></net_weight>\r\n");
				}

//				Modified By Aniket C. On [29th-APR-2021] Calculate Quantity According To Dimension And No_Art[Start]
				else if(currentColumn.trim().equalsIgnoreCase( "no_art" ) || currentColumn.trim().equalsIgnoreCase( "dimension" ) )
				{
					System.out.println("Inside no_art block or dimension block");
					String dimension="",noArtStr="";
					double  noArticles = 0.0d;
					
					itemCode= checkNull(genericUtility.getColumnValue("item_code", dom));
					dimension=checkNull(genericUtility.getColumnValue("dimension", dom));
					noArtStr= checkNull(genericUtility.getColumnValue("no_art", dom));

					System.out.println("item code>>"+itemCode+"\ndimension>>"+dimension+"\nno of articles>>"+noArtStr);

					if (dimension != null && dimension.trim().length() > 0) {
						sql = "SELECT UNIT FROM ITEM WHERE ITEM_CODE = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							unit = rs.getString("UNIT");
						}
						if (pstmt != null) {
							pstmt.close();
							pstmt = null;
						}
						if (rs != null) {
							rs.close();
							rs = null;
						}
						System.out.println("unit>>" + unit);

						if (noArtStr != null && noArtStr.trim().length() > 0) {
							noArticles = Double.parseDouble(noArtStr);
						} else {
							noArticles = 1;
						}
						System.out.println("dimension>>" + dimension + "\n no of articles>>" + noArticles);

						if ("CFT".equalsIgnoreCase(unit) || "SQM".equalsIgnoreCase(unit)) {

							quantity = disCommon.getQuantity(dimension, noArticles, unit, conn);

							System.out.println("quantity in dimension block>>" + quantity);
							valueXmlString.append("<quantity>").append("<![CDATA[" + quantity + "]]>").append("</quantity>");
							setNodeValue(dom, "quantity", getAbsString(String.valueOf(quantity)));
								reStr = itemChanged(dom, dom1, dom2, objContext, "quantity", editFlag, xtraParams);
								System.out.println("quantity after itemchanged");
								pos = reStr.indexOf("<Detail2>");
								reStr = reStr.substring(pos + 9);
								pos = reStr.indexOf("</Detail2>");
								reStr = reStr.substring(0, pos);
								valueXmlString.append(reStr);								
						}
					}
				}
//				Modified By Aniket C. On [29th-APR-2021] Calculate Quantity According To Dimension And No_Art[END]
				
				valueXmlString.append("</Detail2>");
				break;	
			}	
			valueXmlString.append("</Root>");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage());
			throw new ITMException(e);
		}
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
			}			
		}
		return valueXmlString.toString();
	}

	private Object Round(double d)
	{
		// TODO Auto-generated method stub
		return null;
	}
	private void gbfValAcct(String itemCode, String siteCode, String locCode, String lotNo, String lotSl, Connection conn, StringBuffer valueXmlString, Document dom1) throws ITMException
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		String sql = "";
		String acctCodeInv = "",cctrCodeInv = "",grade = "",dimension = "";
		String unitAlt = "",convQtyStduom = "",potencyPerc = "",siteCodeMfg ="",packCode= "";
		String itemSer = "",tranType = "", CctrArray = "", acctCodeAr = "",cctrCodeAr = "";
		double rate=0.0,noArt=0.0,grossRate=0.0;
		Timestamp mfgDate = null,expDate = null,retestDate=null;
		SimpleDateFormat sdf;
		try
		{
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			if(lotSl == null || lotSl.trim().length() ==0)
			{

				sql = "SELECT	ACCT_CODE__INV,CCTR_CODE__INV,RATE,GROSS_RATE, GRADE,DIMENSION,	NO_ART,	UNIT__ALT	," 
						+ " CONV__QTY_STDUOM,	POTENCY_PERC,MFG_DATE,EXP_DATE,SITE_CODE__MFG,	PACK_CODE,RETEST_DATE	FROM"  
						+ " STOCK  WHERE 	ITEM_CODE = ? AND SITE_CODE = ? AND		LOC_CODE  = ? AND " 
						+ " LOT_NO	 = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, itemCode);
				pstmt.setString(2, siteCode);
				pstmt.setString(3, locCode);
				pstmt.setString(4, lotNo);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					acctCodeInv = rs.getString("ACCT_CODE__INV");
					cctrCodeInv = rs.getString("CCTR_CODE__INV");
					rate = rs.getDouble("RATE");
					grossRate = rs.getDouble("GROSS_RATE");
					grade = rs.getString("GRADE");
					dimension = rs.getString("DIMENSION");
					noArt = rs.getDouble("NO_ART");
					unitAlt = checkNull(rs.getString("UNIT__ALT"));
					convQtyStduom = rs.getString("CONV__QTY_STDUOM");
					potencyPerc = rs.getString("POTENCY_PERC");
					mfgDate = rs.getTimestamp("MFG_DATE");
					expDate = rs.getTimestamp("EXP_DATE");
					siteCodeMfg = rs.getString("SITE_CODE__MFG");
					packCode = rs.getString("PACK_CODE");
					retestDate = rs.getTimestamp("RETEST_DATE");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}
			else
			{
				sql = "SELECT	ACCT_CODE__INV,CCTR_CODE__INV,RATE,GROSS_RATE, GRADE,DIMENSION,	NO_ART,	UNIT__ALT	," 
						+ " CONV__QTY_STDUOM,	POTENCY_PERC,MFG_DATE,EXP_DATE,SITE_CODE__MFG,	PACK_CODE,RETEST_DATE	FROM"  
						+ " STOCK  WHERE 	ITEM_CODE = ? AND SITE_CODE = ? AND		LOC_CODE  = ? AND " 
						+ " LOT_NO	 = ?  AND		LOT_SL = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, itemCode);
				pstmt.setString(2, siteCode);
				pstmt.setString(3, locCode);
				pstmt.setString(4, lotNo);
				pstmt.setString(5, lotSl);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					acctCodeInv = rs.getString("ACCT_CODE__INV");
					cctrCodeInv = rs.getString("CCTR_CODE__INV");
					rate = rs.getDouble("RATE");
					grossRate = rs.getDouble("GROSS_RATE");
					grade = rs.getString("GRADE");
					dimension = rs.getString("DIMENSION");
					noArt = rs.getDouble("NO_ART");
					unitAlt = checkNull(rs.getString("UNIT__ALT"));
					convQtyStduom = rs.getString("CONV__QTY_STDUOM");
					potencyPerc = rs.getString("POTENCY_PERC");
					mfgDate = rs.getTimestamp("MFG_DATE");
					expDate = rs.getTimestamp("EXP_DATE");
					siteCodeMfg = rs.getString("SITE_CODE__MFG");
					packCode = rs.getString("PACK_CODE");
					retestDate = rs.getTimestamp("RETEST_DATE");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}

			sql = "SELECT ITEM_SER FROM ITEM WHERE ITEM_CODE = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, itemCode);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				itemSer = rs.getString("ITEM_SER");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;


			tranType = this.genericUtility.getColumnValue("tran_type", dom1);
			CctrArray = finCommon.getAcctDetrTtype(itemCode,itemSer,"STKINV", tranType,conn);
			System.out.println("hdrCctrArray>>>>"+CctrArray);
			if (CctrArray != null && CctrArray.trim().length() > 0)
			{
				if(CctrArray.trim().length() >0)
				{
					String[] arrStr =CctrArray.split(",");
					if(arrStr.length>0)
					{
						acctCodeAr =arrStr[0];
					}
					if(arrStr.length>1)
					{
						cctrCodeAr =arrStr[1];
					}
				}
			}
			valueXmlString.append("<rate><![CDATA[").append(rate).append("]]></rate>\r\n");
			valueXmlString.append("<dimension><![CDATA[").append(dimension).append("]]></dimension>\r\n");
			valueXmlString.append("<no_art><![CDATA[").append(noArt).append("]]></no_art>\r\n");
			valueXmlString.append("<conv__qty_stduom><![CDATA[").append(convQtyStduom).append("]]></conv__qty_stduom>\r\n");
			valueXmlString.append("<unit__alt><![CDATA[").append(unitAlt).append("]]></unit__alt>\r\n");
			valueXmlString.append("<potency_perc><![CDATA[").append(potencyPerc).append("]]></potency_perc>\r\n");
			if(mfgDate != null)
			{
				valueXmlString.append("<mfg_date>").append("<![CDATA["+sdf.format(mfgDate).toString()+"]]>").append("</mfg_date>");
			}else
			{
				valueXmlString.append("<mfg_date><![CDATA[").append("").append("]]></mfg_date>\r\n");
			}
			if(expDate != null)
			{
				valueXmlString.append("<exp_date>").append("<![CDATA["+sdf.format(expDate).toString()+"]]>").append("</exp_date>");
			}else
			{
				valueXmlString.append("<exp_date><![CDATA[").append("").append("]]></exp_date>\r\n");
			}
			valueXmlString.append("<site_code__mfg><![CDATA[").append(siteCodeMfg).append("]]></site_code__mfg>\r\n");
			valueXmlString.append("<pack_code><![CDATA[").append(packCode).append("]]></pack_code>\r\n");
			valueXmlString.append("<acct_code__cr><![CDATA[").append(acctCodeInv).append("]]></acct_code__cr>\r\n");
			valueXmlString.append("<cctr_code__cr><![CDATA[").append(cctrCodeInv).append("]]></cctr_code__cr>\r\n");
			valueXmlString.append("<acct_code__dr><![CDATA[").append(acctCodeAr).append("]]></acct_code__dr>\r\n");
			valueXmlString.append("<cctr_code__dr><![CDATA[").append(cctrCodeAr).append("]]></cctr_code__dr>\r\n");
			valueXmlString.append("<grade><![CDATA[").append(grade).append("]]></grade>\r\n");
			valueXmlString.append("<gross_rate><![CDATA[").append(grossRate).append("]]></gross_rate>\r\n");
			if(retestDate != null)
			{
				valueXmlString.append("<retest_date>").append("<![CDATA["+sdf.format(retestDate).toString()+"]]>").append("</retest_date>");
			}else
			{
				valueXmlString.append("<retest_date><![CDATA[").append("").append("]]></retest_date>\r\n");
			}

		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new ITMException(ex);
		}


	}
	private String checkNull(String input) 
	{
		if(input == null)
		{
			input = "";
		}
		return input;
	}
	public String getRequiredDecimal(double actVal, int prec)
	{
		String fmtStr = "############0";
		if (prec > 0)
		{
			fmtStr = fmtStr + "." + "000000000".substring(0, prec);
		}
		DecimalFormat decFormat = new DecimalFormat(fmtStr);
		return decFormat.format(actVal);
	}
	public static java.util.Date relativeDate(java.util.Date date, int days) 
	{
		java.util.Date calculatedDate = null;
		if (date != null) 
		{
			final GregorianCalendar calendar = new GregorianCalendar();
			calendar.setTime(date);
			calendar.add(Calendar.DATE,days);
			calculatedDate = new java.util.Date(calendar.getTime().getTime());
		}		
		return calculatedDate;
	}
	private String errorType(Connection conn , String errorCode) throws ITMException
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
			if(rs.next())
			{
				msgType = rs.getString("MSG_TYPE");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new ITMException(ex);
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
				throw new ITMException(e);
			}
		}		
		return msgType;
	}

	private String checkDouble(String input)	
	{
		if (input == null || input.trim().length() == 0)
		{
			input="0";
		}
		return input;
	}
	private static void setNodeValue( Document dom, String nodeName, String nodeVal ) throws Exception
	{
		Node tempNode = dom.getElementsByTagName( nodeName ).item(0);

		if( tempNode != null )
		{
			if( tempNode.getFirstChild() == null )
			{
				CDATASection cDataSection = dom.createCDATASection( nodeVal );
				tempNode.appendChild( cDataSection );
			}
			else
			{
				tempNode.getFirstChild().setNodeValue(nodeVal);
			}
		}
		tempNode = null;
	}
	public double getRndamt(double newQty, String round, double roundTo) {
		System.out.println(newQty+"~~~"+round+"~~~"+roundTo);
		double lcMultiply = 1;
		try {
			round = round.toUpperCase();
			if (newQty < 0) {
				lcMultiply = -1;
				newQty = Math.abs(newQty);
			} else if (newQty == 0) {
				return newQty;
			} else if (round.trim().equals("N")) {
				return newQty;
			} else if (roundTo == 0) {
				return newQty;
			}
			if (round.trim().equals("X")) {

				if(newQty == (newQty - (newQty % roundTo)))
				{
					return newQty;
				}
				else 
				{
					newQty = ((newQty - (newQty % roundTo)) + roundTo);
				}				
			}
			if (round.trim().equals("P")) {
				newQty = (newQty - (newQty % roundTo));
			}
			if (round.trim().equals("R")) {
				if ((newQty % roundTo) < (roundTo / 2)) {
					newQty = (newQty - (newQty % roundTo));
				} else {
					newQty = (newQty - (newQty % roundTo) + roundTo);
				}
			}
			System.out.println("newQty[" + newQty + "]");
			System.out.println("lcMultiply[" + lcMultiply + "]");
			newQty = newQty * lcMultiply;
			System.out.println("newQty * lcMultiply[" + newQty + "]");
			return newQty;
		} catch (Exception e) {
			System.out.println("Exception :Conversion Qty ::" + e.getMessage()
			+ ":");

		}
		if (roundTo == 1) {
			newQty = getRequiredDecimals(newQty, 0);
		} else if (roundTo == .1) {
			newQty = getRequiredDecimals(newQty, 1);
		} else if (roundTo == .01) {
			newQty = getRequiredDecimals(newQty, 2);
		} else if (roundTo == .001) {
			newQty = getRequiredDecimals(newQty, 3);
		} else if (roundTo == .0001) {
			newQty = getRequiredDecimals(newQty, 4);
		}
		return newQty;
	}
	public double getRequiredDecimals(double actVal, int prec) {
		NumberFormat numberFormat = NumberFormat.getIntegerInstance();
		Double DoubleValue = new Double(actVal);
		numberFormat.setMaximumFractionDigits(3);
		String strValue = numberFormat.format(DoubleValue);
		strValue = strValue.replaceAll(",", "");
		double reqVal = Double.parseDouble(strValue);
		return reqVal;
	}
	
//	Modified By Aniket C. On [28th-APR-2021] Add Method For Calculate Quantity According To Dimension And No_Art[Start]
	private static String getAbsString(String str) {
		return (str == null || str.trim().length() == 0 || "null".equalsIgnoreCase(str.trim()) ? "" : str.trim());
	}
//	Modified By Aniket C. On [28th-APR-2021] Add Method For Calculate Quantity According To Dimension And No_Art[END]
}	