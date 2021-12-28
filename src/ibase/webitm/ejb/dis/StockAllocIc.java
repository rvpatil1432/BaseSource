

/********************************************************
	Title : StockAllocIc
	Date  : 02/12/2011
	Developer: Dipak Chattar

 ********************************************************/

package ibase.webitm.ejb.dis;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

import java.util.*;
import java.text.SimpleDateFormat;
import java.rmi.RemoteException;
import java.security.AllPermission;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.ejb.Stateless; // added for ejb3


@Stateless // added for ejb3

public class StockAllocIc extends ValidatorEJB implements StockAllocIcLocal, StockAllocIcRemote
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();

	//method for validation
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			System.out.println("Val xmlString :: " + xmlString);
			System.out.println("Val xmlString1 :: " + xmlString1);
			System.out.println("Val xmlString2 :: " + xmlString2);
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			dom2 = parseString(xmlString2);
			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			throw new ITMException(e);
		}
		return(errString);
	}

	//method for validation
	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		NodeList parentNodeList = null;
		NodeList childNodeList = null;	
		Node parentNode = null;
		Node childNode = null;
		String errorType = "";
		String childNodeName = null;
		String errString = "";
		String errCode = "";
		String userId = "";
		String sql = "";
		String siteCode = "";
		String tranDate = "";
		String saleOrder = "";
		String itemCode = "";
		String lotNo = "";
		String lotSl = "";
		String locCode = "";
		String quantity = "";
		String lineNoSord = "";
		String expLev = "",batchId="";
		//Changed By Rohan on 10/07/12 To add validation sale order.start
		String saleOrderHdr = "";
		String sqlSOrder = "";
		//Changed By Rohan on 10/07/12 To add validation sale order.end

		int ctr=0;
		int cnt = 0;
		int cnt1 = 0;
		double pendingQty = 0.0;
		int currentFormNo = 0;
		int childNodeListLength;

		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement pstmt = null ;
		ResultSet rs = null;
		String siteCodeShip="";       // added by cpatil on 27/06/13
		String activePickAllow="";
		
		ConnDriver connDriver = new ConnDriver();
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
		String stockOpt = "";//Added by wasim on 20-APR-17
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			if(objContext != null && objContext.trim().length() > 0)
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
				for(ctr = 0; ctr < childNodeListLength; ctr ++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if(childNodeName.equalsIgnoreCase("tran_date"))
					{    
						tranDate = genericUtility.getColumnValue("tran_date", dom);
						if(tranDate == null || tranDate.trim().length() == 0)
						{
							errCode = "VTTRAN1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase()); 
						}
					}
					else if(childNodeName.equalsIgnoreCase("site_code"))
					{ 
						siteCode = genericUtility.getColumnValue("site_code", dom);
						if(siteCode == null || (siteCode.length() == 0))
						{
							errList.add("VMSITECOD");
							errFields.add(childNodeName.toLowerCase());
						}
						if(siteCode != null && (siteCode.trim().length() > 0))
						{
							sql = " select count(*) from site where site_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,siteCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt =  rs.getInt(1);
								if(cnt == 0) 
								{
									errCode = "VMSITE1";
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
					else if(childNodeName.equalsIgnoreCase("sale_order"))
					{    
						saleOrder = genericUtility.getColumnValue("sale_order", dom);
						if(saleOrder == null || saleOrder.trim().length() == 0)
						{
							errCode = "VMSORD2";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
						}
						else
						{
							sql = " select count(*) from sorder where sale_order = ?  and confirmed='Y'  and  status= (case when status is null then 'P' else status end) ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,saleOrder);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt =  rs.getInt(1);
								if(cnt == 0) 
								{
									errCode = "VMSORD1";
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
					else if(childNodeName.equalsIgnoreCase("site_code__ship"))
					{ 
						siteCodeShip = genericUtility.getColumnValue("site_code__ship", dom);
						if(siteCodeShip == null || (siteCodeShip.length() == 0))
						{
							errList.add("VMSITECOD");
							errFields.add(childNodeName.toLowerCase());
						}
						if(siteCodeShip != null && (siteCodeShip.trim().length() > 0))
						{
							sql = " select count(*) from site where site_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,siteCodeShip);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt =  rs.getInt(1);
								if(cnt == 0) 
								{
									errCode = "VMSITE1";
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
					else if(childNodeName.equalsIgnoreCase("active_pick_allow"))
					{
						activePickAllow = genericUtility.getColumnValue("active_pick_allow", dom);
						System.out.println("activePickAllow----"+activePickAllow);
						if(activePickAllow==null || activePickAllow.length()==0 || ((!"Y".equalsIgnoreCase(activePickAllow)) && (!"N".equalsIgnoreCase(activePickAllow))))
						{
							errCode = "VTINVPICK";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
						}
					}
					// VALLABH KADAM validation for BATCH_ID if it exist in SORD_ALLOC
//					else if(childNodeName.equalsIgnoreCase("batch_id"))
//					{
//						batchId = genericUtility.getColumnValue("batch_id", dom);
//						System.out.println("Batch ID :- ----"+batchId);
//						
//						if(batchId!=null && batchId.trim().length()>0){
//							System.out.println("*****IN if VALIDATION *****");
//							sql="select count(*) as cnt from sord_alloc where batch_id=?";
//							pstmt = conn.prepareStatement(sql);
//							pstmt.setString(1,batchId);
//							rs = pstmt.executeQuery();
//							if(rs.next())
//							{
//								cnt=rs.getInt("cnt");
//							}
//							rs.close();
//							rs = null;
//							pstmt.close();
//							pstmt = null;
//							System.out.println("Batch id cnt at validation :- "+cnt);
//							if(cnt<=0)
//							{
//								errCode = "VTBCHIDINV";
//								errList.add(errCode);
//								errFields.add(childNodeName.toLowerCase());
//							}
//						}
//					}// VALLABH KADAM validation BATCH_ID END
				}			
				break;

			case 2 : 
				parentNodeList = dom1.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				//Changed By Rohan on 10/17/12 to add validation on sale order
				saleOrderHdr = genericUtility.getColumnValue("sale_order", dom1 );
				siteCodeShip = genericUtility.getColumnValue("site_code__ship", dom1);
				System.out.println("saleOrderHdr==*****==>"+saleOrderHdr+"::::siteCodeShip===>"+siteCodeShip);
				for(ctr = 0; ctr < childNodeListLength; ctr ++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();

					if(childNodeName.equalsIgnoreCase("sale_order"))
					{    
						saleOrder = genericUtility.getColumnValue("sale_order", dom);
						if(saleOrder == null || saleOrder.trim().length() == 0)
						{
							errCode = "VMSORD2";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
						}
						else
						{
							sql = " select count(*) from sorder where sale_order = ?  and confirmed='Y'  and  status= (case when status is null then 'P' else status end) ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,saleOrder);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt =  rs.getInt(1);
								if(cnt == 0) 
								{
									errCode = "VMSORD1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());	
								}									
							}
							rs.close(); rs = null;
							pstmt.close(); pstmt = null;
							//Changed by Rohan on 10/07/12 To addvalidation on hdr & Detail.start
							
							if(!(saleOrder.equalsIgnoreCase(saleOrderHdr)))
							{
								errCode = "VMSORDER";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());	
							}
							//Changed by Rohan on 10/07/12 To addvalidation on hdr & Detail.end
						}
					}
					else if(childNodeName.equalsIgnoreCase("site_code"))
					{ 
						siteCode = genericUtility.getColumnValue("site_code", dom);
						if(siteCode == null || (siteCode.length() == 0))
						{
							errList.add("VMSITECOD");
							errFields.add(childNodeName.toLowerCase());
						}
						if(siteCode != null && (siteCode.trim().length() > 0))
						{
							sql = " select count(*) from site where site_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,siteCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt =  rs.getInt(1);
								if(cnt == 0) 
								{
									errCode = "VMSITE1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());	
								}									
							}
							rs.close(); rs = null;
							pstmt.close(); pstmt = null;
						}
					}
					else if(childNodeName.equalsIgnoreCase("item_code"))
					{    
						itemCode = genericUtility.getColumnValue("item_code", dom);
						if(itemCode == null || itemCode.trim().length() == 0)
						{
							errCode = "VMITMC2";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
						}
						else
						{
							sql = " select count(*) from item where item_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,itemCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt =  rs.getInt(1);
								if(cnt == 0) 
								{
									errCode = "VMITMC1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());	
								}									
							}
							rs.close(); rs = null;
							pstmt.close(); pstmt = null;
						}
					}
					else if(childNodeName.equalsIgnoreCase("loc_code"))
					{    
						locCode = genericUtility.getColumnValue("loc_code", dom);
						if(locCode == null || locCode.trim().length() == 0)
						{
							errCode = "VTLOCN2";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
						}
						else
						{
							sql = " select count(*) from location where loc_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,locCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt =  rs.getInt(1);
								if(cnt == 0) 
								{
									errCode = "VTLOCN1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());	
								}									
							}
							rs.close(); rs = null;
							pstmt.close(); pstmt = null;
						}
					}
					else if(childNodeName.equalsIgnoreCase("line_no__sord"))
					{
						lineNoSord = genericUtility.getColumnValue("line_no__sord", dom);
						if(lineNoSord == null || lineNoSord.trim().length() == 0)
						{
							errCode = "VTSORDLINE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
						}
					}
					else if(childNodeName.equalsIgnoreCase("exp_lev"))
					{
						expLev = genericUtility.getColumnValue("exp_lev", dom);
						saleOrder = genericUtility.getColumnValue("sale_order", dom);
						lineNoSord = genericUtility.getColumnValue("line_no__sord", dom);
						if(expLev == null || expLev.trim().length() == 0)
						{
							errCode = "VTEXPL2";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
						}
						else
						{
							if(lineNoSord != null && lineNoSord.trim().length() > 0)
							{
								lineNoSord = "   " + lineNoSord;
								lineNoSord = lineNoSord.substring( lineNoSord.length()-3 );

								sql = " select count(*) from sorditem where sale_order = ? and line_no = ? and exp_lev = ? and line_type = 'I' ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1,saleOrder);
								pstmt.setString(2,lineNoSord);
								pstmt.setString(3,expLev);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt =  rs.getInt(1);
									if(cnt == 0) 
									{
										errCode = "VTEXPL1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());	
									}									
								}
								rs.close(); rs = null;
								pstmt.close(); pstmt = null;
							}
						}
					}
					else if(childNodeName.equalsIgnoreCase("lot_no"))
					{    
						lotNo = genericUtility.getColumnValue("lot_no", dom);
						
						//Changed by wasim on 20-APR-17 for cehcking stock opt [START]
						/*if(lotNo == null || lotNo.trim().length() == 0)
						{
							errCode = "VTLOTNO";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
						}*/
						itemCode = genericUtility.getColumnValue("item_code", dom);
						sql = "SELECT STK_OPT FROM ITEM WHERE ITEM_CODE = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							stockOpt = rs.getString(1) == null ? "" : rs.getString(1).trim();
						}
						if(rs!=null)
						{
							rs.close();rs = null;
						}
						if(pstmt!=null)
						{
							pstmt.close();pstmt = null;
						}
						
						if("2".equalsIgnoreCase(stockOpt))
						{
							if(lotNo == null || lotNo.trim().length() == 0)
							{
								errCode = "VTLOTNO";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());	
							}
						}
						//Changed by wasim on 20-APR-17 for cehcking stock opt [END]
					}
					else if(childNodeName.equalsIgnoreCase("lot_sl"))
					{    
						lotSl = genericUtility.getColumnValue("lot_sl", dom);

						//Changed by wasim on 20-APR-17 for cehcking stock opt [START]
						/*if(lotSl == null || lotSl.trim().length() == 0)
						{
							errCode = "VTLOTSL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
						}*/
						//Change by Rohan on 10/07/12 To check.start
						
						itemCode = genericUtility.getColumnValue("item_code", dom);
						sql = "SELECT STK_OPT FROM ITEM WHERE ITEM_CODE = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							stockOpt = rs.getString(1) == null ? "" : rs.getString(1).trim();
						}
						if(rs!=null)
						{
							rs.close();rs = null;
						}
						if(pstmt!=null)
						{
							pstmt.close();pstmt = null;
						}
						
						if("2".equalsIgnoreCase(stockOpt))
						{
							if(lotSl == null || lotSl.trim().length() == 0)
							{
								errCode = "VTLOTSL";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());	
							}
						}
						//Changed by wasim on 20-APR-17 for cehcking stock opt [END]
						
						itemCode = genericUtility.getColumnValue("item_code", dom);
						siteCode = genericUtility.getColumnValue("site_code", dom);
						locCode = genericUtility.getColumnValue("loc_code", dom);
						lotNo = genericUtility.getColumnValue("lot_no", dom);
						Map tempMap = new HashMap();
						Map currentDetailMap = new HashMap();
						ArrayList tempList = new ArrayList();
						
						tempMap.put("item_code", itemCode);
						tempMap.put("site_code", siteCode);
						tempMap.put("loc_code", locCode);
						tempMap.put("lot_no", lotNo);
						tempMap.put("lot_sl", lotSl);
						System.out.println(" temp  map "+tempMap);
						tempList.add(tempMap);
						currentDetailMap.put("Detail2", tempList);
						System.out.println(" current detail map "+currentDetailMap);
						/*
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
						*/	
						System.out.println("itemCode"+itemCode+"siteCode"+siteCode+"locCode"+locCode+"lotNo"+lotNo+"lotSl"+lotSl);
						
						sql = " select count(*) as CN1 from stock where item_code = ? and site_code = ? and loc_code = ? "
							+" and lot_no = ? and lot_sl = ? ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,itemCode);
						pstmt.setString(2,siteCodeShip);   //  replace siteCode by cpatil 27/06/13
						pstmt.setString(3,locCode);
						pstmt.setString(4,lotNo);
						pstmt.setString(5,lotSl);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt1 =  rs.getInt("CN1");
							if(cnt1 == 0) 
							{
								errCode = "VTNTFOUND";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());	
							}	
							//Changed by sumit on 21/08/12 checking for repeating detail start.
							else
							{								
								boolean isDupDetail = checkDetail(dom2, conn);								
								if(isDupDetail)
								 {									 
									errCode = "DUPDETAIL2";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());	
								 }								
							}
							//Changed by sumit on 21/08/12 checking for repeating detail end.
						}
						rs.close(); rs = null;
						pstmt.close(); pstmt = null;
						//Change by Rohan on 10/07/12 To check.end
					}
					else if(childNodeName.equalsIgnoreCase("quantity"))
					{
						String pendingQtyDom = "";
						String quantityDom = "";
						quantityDom = genericUtility.getColumnValue("quantity", dom);
						pendingQtyDom = genericUtility.getColumnValue("pending_qty", dom);
						
						saleOrder = genericUtility.getColumnValue("sale_order", dom);
						lineNoSord = genericUtility.getColumnValue("line_no__sord", dom);
						itemCode = genericUtility.getColumnValue("item_code", dom);
						siteCode = genericUtility.getColumnValue("site_code", dom);
						//siteCodeShip = genericUtility.getColumnValue("site_code", dom1);
						lotNo = genericUtility.getColumnValue("lot_no", dom);
						lotSl = genericUtility.getColumnValue("lot_sl", dom);
						System.out.println("saleOrder ["+saleOrder+"] lineNoSord["+lineNoSord+"] itemCode["+itemCode+"] siteCode["+siteCode+"] lotNo["+lotNo+"] lotSl["+lotSl+"]");						;
						if ( quantityDom == null || quantityDom.trim().length() == 0  )
						{
							quantityDom = "0";
						}
						else
						{
							sql = "SELECT (STOCK.QUANTITY - STOCK.ALLOC_QTY - (CASE WHEN STOCK.HOLD_QTY IS NULL THEN 0 ELSE STOCK.HOLD_QTY END)) AS QTY_AVAIL_ALLOC " +
									" FROM STOCK WHERE ITEM_CODE = ? AND SITE_CODE = ? AND LOC_CODE = ? AND LOT_NO = ? AND LOT_SL = ?";
							
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							pstmt.setString(2, siteCodeShip);     //  replace siteCode by cpatil 27/06/13
							pstmt.setString(3, locCode);
							pstmt.setString(4, lotNo);
							pstmt.setString(5, lotSl);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								double qtyAvilable = 0.0;
								qtyAvilable = rs.getDouble("QTY_AVAIL_ALLOC");	
								System.out.println("Calling for item code"+itemCode);
								if(Double.parseDouble(quantityDom) > qtyAvilable)
								{
									errCode = "VTSTOCK2";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								//Changed by Rohan on 13-06-13 for bug fixing of quantity not greater than pending quantity.start
								/*
								else if(validateAllDetailQty(saleOrder, lineNoSord, dom2, conn))
								{
									errCode = "INVQUANTY";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								*/
								//Changed by Rohan on 13-06-13 for bug fixing of quantity not greater than pending quantity.start
							}
							
						}
						System.out.println("quantityDom"+quantityDom+"pendingQtyDom"+pendingQtyDom);
						if ( pendingQtyDom == null || pendingQtyDom.trim().length() == 0  )
						{
							pendingQtyDom = "0";
						}
						if( Double.parseDouble(quantityDom) == 0 )
						{
							errCode = "VTQUANTITY";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else if(Double.parseDouble(quantityDom) > Double.parseDouble(pendingQtyDom) )
						{
							errCode = "VTSORDQTY";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}	
					}
				}
				break;

			}//end switch
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
					errorType =  errorType(conn , errCode);
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
		}//end try
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
	}//end of validation

	// method for item change
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		System.out.println("XML STRING======>----------------------------");
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try
		{   
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			System.out.println("XML STRING======>"+xmlString1);
			dom2 = parseString(xmlString2);
			System.out.println("XML STRING======555555555555555555555===>"+xmlString2);
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [JvVal][itemChanged( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		System.out.println("valueXmlStringvalueXmlStrin ST"+valueXmlString);
		return valueXmlString;
	}

	// method for item change
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		StringBuffer valueXmlString = new StringBuffer();
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;

		String childNodeName = null;
		String siteCode = "";
		String itemCode = "";
		String itemDescr = "";
		String dateNow = "";
		String quantity = "";
		String saleOrder = "";
		String lineNoSord = "";
		String expLev = "";
		String custCode = "";
		String custDescr = "";
		String chgUser = "";
		String chgTerm = "";
		String sql = "";
		String siteDescr = "";
		String locCode = "";
		String locDescr = "";
		
		String lotNo = "";//added by sumit on 19/12/12
		boolean isActives = false;
		String orderByStkStr = "";
		String resrvLoc  = "",casePickLoc = "", activePickLoc = "", deepStoreLoc = "", partialResrvLoc = "", singleLot = "";
		String sSQL = "",sSingleLotSql = "",lineNo = "";
		int minSelfLife = 0;
		int ctr = 0;
		int currentFormNo = 0;
		double pendingQty = 0.0,qtyAvail = 0.0;		
		//Changes by Sumit end  
		// added by cpatil on 25/06/13 start
		String siteCodeShip="",siteCodeShipDescr="",unit="", unitStd ="",columnValue="";  
		double orderQuantity = 0.0,convQtyStduom = 0.0,quantityStduom = 0.0;
		// added by cpatil on 25/06/13 end
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		ConnDriver connDriver = new ConnDriver();
		String activePickAllow="";
		try
		{ 
			System.out.println("@@@@@@@@@@@ itemchange method called for ---->>>>["+currentColumn+"]");
			DistCommon discommon = new DistCommon();	
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());

			if(objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}

			siteCode =(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode"));

			siteDescr = findValue(conn, "descr" ,"site", "site_code", siteCode);
			System.out.println("@@@@@@ 1 siteCode["+siteCode+"]::::::siteDescr["+siteDescr+"]");
			chgUser =(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode"));
			chgTerm =(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "chgTerm"));
			
			dateNow = simpleDateFormat.format(currentDate.getTime());

			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");
			switch(currentFormNo)
			{
			case 1 : 
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail1>");
				int childNodeListLength = childNodeList.getLength();
				do
				{   
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if(childNodeName.equals(currentColumn))
					{
						if(childNode.getFirstChild() != null)
						{
							columnValue = childNode.getFirstChild().getNodeValue().trim();  //added by cpatil
						}
					}
					ctr ++;
				}while(ctr < childNodeListLength  && ! childNodeName.equals(currentColumn));
				if(currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					System.out.println("@@@@@@  itm_default itemchange called");
					System.out.println("@@@@@@ 2 siteCode["+siteCode+"]::::::siteDescr["+siteDescr+"]");
					valueXmlString.append("<site_code>").append("<![CDATA[" +  siteCode + "]]>").append("</site_code>");
					valueXmlString.append("<site_descr>").append("<![CDATA[" +  siteDescr + "]]>").append("</site_descr>");

					valueXmlString.append("<tran_date>").append("<![CDATA[" + dateNow + "]]>").append("</tran_date>");
					valueXmlString.append("<add_date>").append("<![CDATA[" + dateNow + "]]>").append("</add_date>");
					valueXmlString.append("<add_user>").append("<![CDATA[" + chgUser + "]]>").append("</add_user>");
					valueXmlString.append("<add_term>").append("<![CDATA[" + chgTerm + "]]>").append("</add_term>");
					// added by cpatil start
					valueXmlString.append("<chg_date>").append("<![CDATA[" + dateNow + "]]>").append("</chg_date>");
					valueXmlString.append("<chg_user>").append("<![CDATA[" + chgUser + "]]>").append("</chg_user>");
					valueXmlString.append("<chg_term>").append("<![CDATA[" + chgTerm + "]]>").append("</chg_term>");
					// end
				}//end of if
				else if(currentColumn.trim().equalsIgnoreCase("itm_defaultedit"))
				{
					/*--Commented by gulzar
					siteCode = genericUtility.getColumnValue("site_code", dom);
					chgUser =(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode"));
					chgTerm =(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "chgTerm"));
					dateNow = simpleDateFormat.format(currentDate.getTime());
					valueXmlString.append("<site_code>").append("<![CDATA[" +  siteCode + "]]>").append("</site_code>");
					valueXmlString.append("<tran_date>").append("<![CDATA[" + dateNow + "]]>").append("</tran_date>");
					valueXmlString.append("<add_date protect = \"1\">").append("<![CDATA[" + dateNow + "]]>").append("</add_date>");
					valueXmlString.append("<add_user protect = \"1\">").append("<![CDATA[" + chgUser + "]]>").append("</add_user>");
					valueXmlString.append("<add_term protect = \"1\">").append("<![CDATA[" + chgTerm + "]]>").append("</add_term>");
					*/
					activePickAllow=genericUtility.getColumnValue("active_pick_allow", dom);
					activePickAllow=activePickAllow==null?"Y":activePickAllow;
					valueXmlString.append("<active_pick_allow protect = \"1\">").append("<![CDATA[" +  activePickAllow + "]]>").append("</active_pick_allow>");
				}
				else if(currentColumn.trim().equalsIgnoreCase("sale_order"))
				{
					System.out.println("@@@@@@  sale order itemchange called");
					saleOrder = genericUtility.getColumnValue("sale_order", dom);
					custCode = findValue(conn, "cust_code" ,"sorder", "sale_order", saleOrder);
					valueXmlString.append("<cust_code>").append("<![CDATA[" +  custCode + "]]>").append("</cust_code>");
					custDescr = findValue(conn, "cust_name" ,"customer", "cust_code", custCode);
					valueXmlString.append("<cust_name>").append("<![CDATA[" +  custDescr + "]]>").append("</cust_name>");
					// added by cpatil on 25/06/13 start
					siteCodeShip = findValue(conn, "site_code__ship" ,"sorder", "sale_order", saleOrder);
					siteCodeShipDescr = findValue(conn, "descr" ,"site", "site_code", siteCodeShip);
					System.out.println("@@@@@ siteCodeShip["+siteCodeShip+"]siteCodeShipDescr["+siteCodeShipDescr+"]");
					
					valueXmlString.append("<site_code__ship>").append("<![CDATA[" +  siteCodeShip + "]]>").append("</site_code__ship>");
					valueXmlString.append("<site_ship_descr>").append("<![CDATA[" +  siteCodeShipDescr + "]]>").append("</site_ship_descr>");
					// added by cpatil on 25/06/13 end
					sSQL = " SELECT   W.ACTIVE_PICK_ALLOW " +							
							" FROM SORDER S, CUSTOMER C, WAVE_TYPE W  WHERE SALE_ORDER = ? " +										
							" AND C.CUST_CODE = S.CUST_CODE__DLV " +
							" AND C.WAVE_TYPE = W.WAVE_TYPE ";
					pstmt=conn.prepareStatement(sSQL);
					pstmt.setString(1,saleOrder);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						activePickAllow=rs.getString(1);
					}
					rs.close();
					rs=null;
					pstmt.close();
					pstmt=null;
					activePickAllow=activePickAllow==null?"Y":activePickAllow;
					valueXmlString.append("<active_pick_allow>").append("<![CDATA[" +  activePickAllow + "]]>").append("</active_pick_allow>");
				}
				// added by cpatil on 25/06/13 start
				else if(currentColumn.trim().equalsIgnoreCase("site_code__ship"))
				{
					System.out.println("@@@@@@  site_code__ship itemchange called");
					siteCodeShip = genericUtility.getColumnValue("site_code__ship", dom);
					siteCodeShipDescr = findValue(conn, "descr" ,"site", "site_code", siteCodeShip);
					System.out.println("@@@@@ siteCodeShip["+siteCodeShip+"]siteCodeShipDescr["+siteCodeShipDescr+"]");
					valueXmlString.append("<site_ship_descr>").append("<![CDATA[" +  siteCodeShipDescr + "]]>").append("</site_ship_descr>");
				}
				// added by cpatil on 25/06/13 end
				valueXmlString.append("</Detail1>");
				break;       

			case 2 : 
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0;
				valueXmlString.append("<Detail2>");
				childNodeListLength = childNodeList.getLength();
				do
				{   
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if(childNodeName.equals(currentColumn))
					{
						if(childNode.getFirstChild() != null)
						{
						}
					}
					ctr ++;
				}while(ctr < childNodeListLength  && ! childNodeName.equals(currentColumn));
				siteCodeShip = genericUtility.getColumnValue("site_code__ship", dom1);
				System.out.println("siteCodeShip----"+siteCodeShip);
				if(currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					/*--commented and changes below by gulzar on 12/24/2011
					siteCode =(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode"));
					valueXmlString.append("<site_code>").append("<![CDATA[" +  siteCode + "]]>").append("</site_code>");
					*/
					saleOrder = genericUtility.getColumnValue("sale_order", dom1, "1");
					//siteCode = genericUtility.getColumnValue("site_code", dom1, "1");
					siteCode = genericUtility.getColumnValue("site_code__ship", dom1, "1");
					System.out.println("siteCode---"+siteCode);
					siteDescr = findValue(conn, "descr" ,"site", "site_code", siteCode);
					valueXmlString.append("<sale_order>").append("<![CDATA[" +  saleOrder + "]]>").append("</sale_order>");
					valueXmlString.append("<site_code protect = \"1\">").append("<![CDATA[" +  siteCode + "]]>").append("</site_code>");
					valueXmlString.append("<site_descr>").append("<![CDATA[" +  siteDescr + "]]>").append("</site_descr>");
					valueXmlString.append("<quantity>").append("<![CDATA[0]]>").append("</quantity>");

				}//end of if
				else if(currentColumn.trim().equalsIgnoreCase("itm_defaultedit"))
				{
					/*--commented by gulzar on 12/24/2011
					siteCode = genericUtility.getColumnValue("site_code", dom);
					itemCode = genericUtility.getColumnValue("item_code", dom);
					valueXmlString.append("<site_code>").append("<![CDATA[" +  siteCode + "]]>").append("</site_code>");
					itemDescr = findValue(conn, "descr" ,"item", "item_code", itemCode);
					valueXmlString.append("<item_descr>").append("<![CDATA[" +  itemDescr + "]]>").append("</item_descr>");
					*/

				}
				else if(currentColumn.trim().equalsIgnoreCase("item_code"))
				{
					itemCode = genericUtility.getColumnValue("item_code", dom);
					itemDescr = findValue(conn, "descr" ,"item", "item_code", itemCode);
					valueXmlString.append("<item_descr>").append("<![CDATA[" +  itemDescr + "]]>").append("</item_descr>");
				}
				else if(currentColumn.trim().equalsIgnoreCase("site_code"))
				{
					siteCode = genericUtility.getColumnValue("site_code", dom);
					siteDescr = findValue(conn, "descr" ,"site", "site_code", siteCode);
					valueXmlString.append("<site_descr>").append("<![CDATA[" +  siteDescr + "]]>").append("</site_descr>");
				}
				else if(currentColumn.trim().equalsIgnoreCase("loc_code"))
				{
					locCode = genericUtility.getColumnValue("loc_code", dom);
					locDescr = findValue(conn, "descr" ,"location", "loc_code", locCode);
					valueXmlString.append("<location_descr>").append("<![CDATA[" +  locDescr + "]]>").append("</location_descr>");
				}
				else if(currentColumn.trim().equalsIgnoreCase("sale_order") || currentColumn.trim().equalsIgnoreCase("line_no__sord") || currentColumn.trim().equalsIgnoreCase("exp_lev") )
				{
					saleOrder = checkNull(genericUtility.getColumnValue("sale_order", dom));
					lineNoSord = checkNull(genericUtility.getColumnValue("line_no__sord", dom));
					expLev = checkNull(genericUtility.getColumnValue("exp_lev", dom));
					activePickAllow=genericUtility.getColumnValue("active_pick_allow", dom1);
					activePickAllow=activePickAllow==null?"Y":activePickAllow;
					lineNoSord = "   " + lineNoSord.trim();
					lineNoSord = lineNoSord.substring( lineNoSord.length()-3 );
					
					if (lineNoSord.trim().length() > 0 )
					{
						valueXmlString.append("<line_no__sord>").append("<![CDATA[" +  lineNoSord + "]]>").append("</line_no__sord>");
					}
					//if (saleOrder.trim().length() > 0 && lineNoSord.trim().length() > 0 && expLev.trim().length() > 0 )
					if (saleOrder.trim().length() > 0 && lineNoSord.trim().length() > 0 )
					{
						/*sql = "SELECT ITEM_CODE,(CASE WHEN quantity IS NULL THEN 0 ELSE quantity END - CASE WHEN qty_alloc IS NULL THEN 0 ELSE qty_alloc END - CASE WHEN qty_desp IS NULL THEN 0 ELSE qty_desp END )"
								+ " FROM SORDITEM WHERE SALE_ORDER = ? AND LINE_NO = ? AND EXP_LEV = ? ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,saleOrder);
						pstmt.setString(2,lineNoSord);
						pstmt.setString(3,expLev);
						rs = pstmt.executeQuery();*/
						if(expLev.trim().length() > 0)
						{
							sql = "SELECT ITEM_CODE,((CASE WHEN quantity IS NULL THEN 0 ELSE quantity END)*CONV__QTY_STDQTY - CASE WHEN qty_alloc IS NULL THEN 0 ELSE qty_alloc END - (CASE WHEN qty_desp IS NULL THEN 0 ELSE qty_desp END)*CONV__QTY_STDQTY )"
									+ ", EXP_LEV FROM SORDITEM WHERE SALE_ORDER = ? AND LINE_NO = ? AND EXP_LEV = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,saleOrder);
							pstmt.setString(2,lineNoSord);
							pstmt.setString(3,expLev);
						}
						else
						{
							sql = "SELECT ITEM_CODE,((CASE WHEN quantity IS NULL THEN 0 ELSE quantity END)*CONV__QTY_STDQTY - CASE WHEN qty_alloc IS NULL THEN 0 ELSE qty_alloc END - (CASE WHEN qty_desp IS NULL THEN 0 ELSE qty_desp END)*CONV__QTY_STDQTY )"
									+ ", EXP_LEV FROM SORDITEM WHERE SALE_ORDER = ? AND LINE_NO = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,saleOrder);
							pstmt.setString(2,lineNoSord);
							
						}						
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							itemCode = rs.getString("ITEM_CODE");
							pendingQty  =  rs.getDouble(2);
							//Changed by sumit
							expLev = rs.getString("EXP_LEV");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						
						valueXmlString.append("<pending_qty>").append("<![CDATA[" +  pendingQty + "]]>").append("</pending_qty>");
						valueXmlString.append("<item_code>").append("<![CDATA[" +  itemCode + "]]>").append("</item_code>");
						itemDescr = findValue(conn, "descr" ,"item", "item_code", itemCode);
						valueXmlString.append("<item_descr>").append("<![CDATA[" +  itemDescr + "]]>").append("</item_descr>");
						valueXmlString.append("<exp_lev>").append("<![CDATA[" +  expLev + "]]>").append("</exp_lev>");
						
						//Changed by sumit on 07/01/13 start						
						Map stockDeatil = new HashMap();
						//stockDeatil = getStockDetail(siteCode, saleOrder, lineNoSord, conn, itemCode);//COMMENTED BY MANOJ DTD 27/06/2013 to get Stock etail for Site ode Ship
						
						stockDeatil = getStockDetail(siteCodeShip, saleOrder, lineNoSord, conn, itemCode,activePickAllow);
						/**
						 * stockDetail.put("qtyAvail", qtyAvail);
						 * stockDetail.put("lotNo", lotNo);
							stockDetail.put("lotSl", lotSl);
							stockDetail.put("locCode", locCode);
							stockDetail.put("locDescr", locDescr);
						 */
						System.out.println(" test "+stockDeatil.get("locCode"));
						valueXmlString.append("<quantity>").append("<![CDATA[" +  stockDeatil.get("qtyAvail") + "]]>").append("</quantity>");
						valueXmlString.append("<lot_no>").append("<![CDATA[" +  stockDeatil.get("lotNo") + "]]>").append("</lot_no>");
						valueXmlString.append("<lot_sl>").append("<![CDATA[" +  stockDeatil.get("lotSl") + "]]>").append("</lot_sl>");
						valueXmlString.append("<loc_code>").append("<![CDATA[" +  stockDeatil.get("locCode") + "]]>").append("</loc_code>");
						valueXmlString.append("<location_descr>").append("<![CDATA[" +  stockDeatil.get("locDescr") + "]]>").append("</location_descr>");
						//Changed by sumit on 07/01/13 end	
						
						// added by cpatil start
						
						sql = " select quantity,unit,unit__std,conv__qty_stduom,quantity__stduom FROM sorddet " +
							 	   " WHERE sale_order = ?  and line_no = ? ";
							
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,saleOrder);
						pstmt.setString(2,lineNoSord);
						
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							orderQuantity = rs.getDouble("quantity");
							unit = rs.getString("unit");
							unitStd = rs.getString("unit__std");
							convQtyStduom = rs.getDouble("conv__qty_stduom");
							quantityStduom = rs.getDouble("quantity__stduom");								
						}
						rs.close();
						rs = null;
						pstmt.close(); 
						pstmt = null;
						
						valueXmlString.append("<order_quantity>").append( orderQuantity ).append("</order_quantity>");
						valueXmlString.append("<unit>").append( unit ).append("</unit>");
						valueXmlString.append("<unit__std>").append( unitStd ).append("</unit__std>");
						valueXmlString.append("<conv__qty_stduom>").append( convQtyStduom ).append("</conv__qty_stduom>");
						valueXmlString.append("<quantity__stduom>").append( quantityStduom ).append("</quantity__stduom>");
						//Added by wasim on 14-04-2017 for setting dealloc_qty in XML string as it was giving error, can not insert null into sord_alloc_det
						valueXmlString.append("<dealloc_qty>").append(0.0).append("</dealloc_qty>");				
						
						// added by cpatil  end
					}
				}
				else if(currentColumn.trim().equalsIgnoreCase("lot_no"))
				{

					HashMap stockLot=new HashMap<String,Double>();
					HashMap<String,ArrayList<String>>stockLotDetail=new HashMap<String, ArrayList<String>>();   // ADDED BY RITESH ON 30/06/14
					lotNo = checkNull(genericUtility.getColumnValue("lot_no", dom));
					saleOrder = checkNull(genericUtility.getColumnValue("sale_order", dom));
					lineNoSord = checkNull(genericUtility.getColumnValue("line_no__sord", dom));
					itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
					System.out.println(" saleOrder ["+saleOrder+"] lot no ["+lotNo+"] itemCode["+itemCode+"]");
					activePickAllow=genericUtility.getColumnValue("active_pick_allow", dom1);
					
					siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));  // ADDED BY RITESH 
					System.out.println("siteCode::DETAIL"+siteCode);
					activePickAllow=activePickAllow==null?"Y":activePickAllow;
					
					String getDataSql =" SELECT SORDER.CUST_CODE, CUSTOMER.CUST_NAME,SORDDET.LINE_NO, "
						+"SORDER.SALE_ORDER,SORDER.DUE_DATE,SORDITEM.ITEM_CODE,"
						+"ITEM.DESCR,SORDITEM.QUANTITY,"
						+"((SORDITEM.QUANTITY*SORDITEM.CONV__QTY_STDQTY) - (SORDITEM.QTY_DESP*SORDITEM.CONV__QTY_STDQTY) - SORDITEM.QTY_ALLOC) PENDING_QUANTITY,"
						+"SORDITEM.QTY_ALLOC,SORDDET.PACK_INSTR,"
						+"SORDER.SITE_CODE,SORDITEM.EXP_LEV, "
						+"SORDER.PART_QTY AS PART_QTY, SORDER.SINGLE_LOT AS SINGLE_LOT, SORDITEM.MIN_SHELF_LIFE AS MIN_SHELF_LIFE "//Gulzar 5/13/2012
						//Chnged by Rohan 0n 22/06/12
						//Changed by sumit 20/08/12 getting column data start.
						//+",SORDER.ALLOC_FLAG AS ALLOC_FLAG "		   
						//+"FROM SORDDET,SORDER,SORDITEM,CUSTOMER,ITEM "
						+" ,SORDER.ALLOC_FLAG AS ALLOC_FLAG , WAVE_TYPE.MASTER_PACK_ALLOW , WAVE_TYPE.ACTIVE_PICK_ALLOW , WAVE_TYPE.STOCK_TO_DOCK_ALLOW "		   
						+"FROM SORDDET,SORDER,SORDITEM, ITEM, "
						
						//Changed by Manish 25/09/15 for mssql .
						+" CUSTOMER left outer join WAVE_TYPE WAVE_TYPE  on  ( CUSTOMER.WAVE_TYPE = WAVE_TYPE.WAVE_TYPE) "
						//Changed by Manish 25/09/15 for mssql end.

						//Changed by sumit 20/08/12 getting column data end.
						+"WHERE ( SORDER.SALE_ORDER = SORDDET.SALE_ORDER ) AND "
						+"( SORDITEM.SALE_ORDER = SORDER.SALE_ORDER ) AND "
						+"( SORDDET.LINE_NO = SORDITEM.LINE_NO ) AND "
						+"( SORDITEM.ITEM_CODE = ITEM.ITEM_CODE ) AND "
						+"( SORDER.CUST_CODE = CUSTOMER.CUST_CODE ) AND  "
						+"( SORDDET.SITE_CODE = SORDITEM.SITE_CODE ) AND  "
						//Changed by sumit on 20/08/12 join customer with wave_type
						//+"( CUSTOMER.WAVE_TYPE = WAVE_TYPE.WAVE_TYPE(+)) AND "
						//+" SORDER.SITE_CODE = ? AND "							   
						+" SORDER.SALE_ORDER = ? "
						+" AND SORDITEM.LINE_NO = ? "
						+"AND CASE WHEN SORDDET.STATUS IS NULL THEN 'P' ELSE SORDDET.STATUS end <> 'C' "
						+"AND CASE WHEN SORDER.STATUS IS NULL THEN 'P' ELSE SORDER.STATUS end = 'P' "
						//Changed by sumit on 12/09/12 considering hold_flag from sorddet 
						+" AND CASE WHEN SORDDET.HOLD_FLAG IS NULL THEN 'N' ELSE SORDDET.HOLD_FLAG end <> 'Y'"
						//Chnaged by Rohan on 11/07/12 revert Changes of Manual Stock Allocation
						//Changed by Rohan on 22/06/12
						//+"AND SORDITEM.QUANTITY - SORDITEM.QTY_DESP - SORDITEM.QTY_ALLOC > 0 "
						//+"AND (SORDITEM.QUANTITY - SORDITEM.QTY_DESP - SORDITEM.QTY_ALLOC > 0 OR SORDER.ALLOC_FLAG='M' )"
						//Changed by sumit on 13/08/12 as per manual stock allocation start.
						//+"AND SORDITEM.QUANTITY - SORDITEM.QTY_DESP - SORDITEM.QTY_ALLOC > 0 "
						+" AND (((SORDITEM.QUANTITY*SORDITEM.CONV__QTY_STDQTY) - (SORDITEM.QTY_DESP*SORDITEM.CONV__QTY_STDQTY) - SORDITEM.QTY_ALLOC > 0 ) " ; 
						
					//Changed by Manish on 25/09/2015 for ms sql server.
					String DB = CommonConstants.DB_NAME;
					if("mssql".equalsIgnoreCase(DB))
					{
						getDataSql	= getDataSql +" OR (dbo.FN_CHECK_MANUAL_STOCK_ALLOC(SORDER.SALE_ORDER, SORDDET.LINE_NO) = 0 ) )"	 
						+"AND SORDITEM.LINE_TYPE = 'I'";
					}
					else
					{
						getDataSql	= getDataSql +" OR (FN_CHECK_MANUAL_STOCK_ALLOC(SORDER.SALE_ORDER, SORDDET.LINE_NO) = 0 ) )"	 
						+"AND SORDITEM.LINE_TYPE = 'I'";
					}
					//Changed by Manish on 25/09/2015 for ms sql server.
										   

					 pstmt = conn.prepareStatement(getDataSql);
					 pstmt.setString(1, saleOrder);
					 pstmt.setString(2, lineNoSord);
					 rs = pstmt.executeQuery();
					 if( rs.next())
					 {
						 singleLot = rs.getString("SINGLE_LOT")==null?"N":rs.getString("SINGLE_LOT");
						 minSelfLife = rs.getInt("MIN_SHELF_LIFE");
						 pendingQty = rs.getInt("PENDING_QUANTITY");
					 }
					 System.out.println(" singleLot ["+singleLot+"] minSelfLife ["+minSelfLife+"] pendingQty ["+pendingQty+"]");
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
					 
					 resrvLoc  = discommon.getDisparams("999999","RESERV_LOCATION",conn);
			            
					 casePickLoc  = discommon.getDisparams("999999","CASE_PICK_INVSTAT",conn);
					 activePickLoc  = discommon.getDisparams("999999","ACTIVE_PICK_INVSTAT",conn);
					 deepStoreLoc = discommon.getDisparams("999999","DEEP_STORE_INVSTAT",conn);
					 partialResrvLoc = discommon.getDisparams("999999","PRSRV_INVSTAT",conn);			
					 System.out.println("CASE_PICK_INVSTAT"+casePickLoc+"ACTIVE_PICK_INVSTAT"+activePickLoc+"RESERV_LOCATION"+resrvLoc+"PARTIAL RESERVE LOC["+partialResrvLoc+"]");
					 if("N".equalsIgnoreCase(activePickAllow))
						{
							activePickLoc="";
							partialResrvLoc="";
						}
					 /*sSQL =	"SELECT STOCK.LOT_NO,STOCK.LOT_SL,"
							   +"STOCK.LOC_CODE, "
							   +"STOCK.UNIT,  "
							   +"(STOCK.QUANTITY - STOCK.ALLOC_QTY - (CASE WHEN STOCK.HOLD_QTY IS NULL THEN 0 ELSE STOCK.HOLD_QTY END)) AS QTY_AVAIL_ALLOC ,"
							   +"STOCK.GRADE,STOCK.EXP_DATE,STOCK.CONV__QTY_STDUOM,STOCK.QUANTITY, " 
							   //Changed By Pragyan
							   // +"STOCK.MFG_DATE,STOCK.SITE_CODE__MFG "
							   +"STOCK.MFG_DATE,STOCK.SITE_CODE__MFG, "
							   +"STOCK.NO_ART,INVSTAT.INV_STAT,ITEM.LOC_TYPE__PARENT,ITEM.LOC_TYPE,ITEM.LOC_ZONE__PREF "  //Changes done by chandni 08-06-12(instead of loc_type, loc_type__parent should be selected)
							   +"FROM STOCK,ITEM,LOCATION,INVSTAT " 
							   +"WHERE (ITEM.ITEM_CODE = STOCK.ITEM_CODE) "
							   +"AND (LOCATION.LOC_CODE = STOCK.LOC_CODE ) "
							   +"AND (LOCATION.INV_STAT = INVSTAT.INV_STAT) "
							   +"AND INVSTAT.AVAILABLE = 'Y' "
							   +"AND STOCK.ITEM_CODE = ? AND STOCK.SITE_CODE = ? "
							   +"AND (STOCK.QUANTITY - STOCK.ALLOC_QTY - CASE WHEN STOCK.HOLD_QTY IS NULL THEN 0 ELSE STOCK.HOLD_QTY END) > 0  " // 25/06/12 manoharan 
							   +"AND (MONTHS_BETWEEN(TO_DATE(STOCK.EXP_DATE),SYSDATE) > ? ) " ;
					 		   //+"AND NOT EXISTS (SELECT 1 FROM INV_RESTR I WHERE I.INV_STAT = INVSTAT.INV_STAT AND I.REF_SER = 'S-DSP' ) ";
*/					 
					 /*String sSingleLotSql = "SELECT SUM(STOCK.QUANTITY - STOCK.ALLOC_QTY - (CASE WHEN STOCK.HOLD_QTY IS NULL THEN 0 ELSE STOCK.HOLD_QTY END ) ), "
				 				+"SUM(STOCK.HOLD_QTY ) " 
				 				+"FROM STOCK,ITEM,LOCATION,INVSTAT " 
				 				+"WHERE (ITEM.ITEM_CODE = STOCK.ITEM_CODE) " 
				 				+"AND (LOCATION.LOC_CODE = STOCK.LOC_CODE ) " 
				 				+"AND (LOCATION.INV_STAT = INVSTAT.INV_STAT) " 
				 				+"AND INVSTAT.AVAILABLE = ? " 
				 				+"AND STOCK.ITEM_CODE = ? " 
				 				+"AND STOCK.SITE_CODE = ? "  
				 				+"AND STOCK.LOT_NO IN (SELECT LOT_NO FROM STOCK WHERE ITEM_CODE =? AND SITE_CODE =? "
				 				+"GROUP BY LOT_NO HAVING SUM(QUANTITY - ALLOC_QTY - (CASE WHEN STOCK.HOLD_QTY IS NULL THEN 0 ELSE STOCK.HOLD_QTY END) ) >= ?) "
				 				+"AND (MONTHS_BETWEEN(TO_DATE(STOCK.EXP_DATE),SYSDATE) > ? ) ";*/
					 sSingleLotSql = "SELECT STOCK.LOT_NO,STOCK.LOT_SL,"
							+"STOCK.LOC_CODE, "
							+"STOCK.UNIT,  "
							+"(STOCK.QUANTITY - STOCK.ALLOC_QTY - (CASE WHEN STOCK.HOLD_QTY IS NULL THEN 0 ELSE STOCK.HOLD_QTY END)) AS QTY_AVAIL_ALLOC ,"
							+"STOCK.GRADE,STOCK.EXP_DATE,STOCK.CONV__QTY_STDUOM,STOCK.QUANTITY, " 					   
							+"STOCK.MFG_DATE,STOCK.SITE_CODE__MFG, "
							+"STOCK.NO_ART,INVSTAT.INV_STAT,ITEM.LOC_TYPE__PARENT,ITEM.LOC_TYPE,ITEM.LOC_ZONE__PREF,LOCATION.DESCR " 
							+"FROM STOCK,ITEM,LOCATION,INVSTAT " 
							+"WHERE (ITEM.ITEM_CODE = STOCK.ITEM_CODE) "
							+"AND (LOCATION.LOC_CODE = STOCK.LOC_CODE ) "
							+"AND (LOCATION.INV_STAT = INVSTAT.INV_STAT) "
							+"AND INVSTAT.AVAILABLE = 'Y' "
							+"AND STOCK.ITEM_CODE = ? AND STOCK.SITE_CODE = ? "
							+"AND (STOCK.QUANTITY - STOCK.ALLOC_QTY - (CASE WHEN STOCK.HOLD_QTY IS NULL THEN 0 ELSE STOCK.HOLD_QTY END) > 0) "
							+"AND STOCK.LOT_NO IN (SELECT LOT_NO FROM STOCK WHERE ITEM_CODE =? AND SITE_CODE =? AND LOT_NO = ? "
							+"GROUP BY LOT_NO HAVING SUM(QUANTITY - ALLOC_QTY - (CASE WHEN STOCK.HOLD_QTY IS NULL THEN 0 ELSE STOCK.HOLD_QTY END) ) > 0) " ;
						//	+"AND (MONTHS_BETWEEN(TO_DATE(STOCK.EXP_DATE),SYSDATE) > ? ) ";
						//+"AND NOT EXISTS (SELECT 1 FROM INV_RESTR I WHERE I.INV_STAT = INVSTAT.INV_STAT AND I.REF_SER = 'S-DSP' ) ";

						
						//changed by Manish for ms sql server query on 04/09/2015
						if("mssql".equalsIgnoreCase(DB))
						{
							sSingleLotSql =  sSingleLotSql + " AND (DATEDIFF(mm,GETDATE(),CONVERT(DATETIME, STOCK.EXP_DATE)) > ?) ";	
						}
						
						else
						{
							sSingleLotSql =  sSingleLotSql + "AND (MONTHS_BETWEEN(TO_DATE(STOCK.EXP_DATE),SYSDATE) > ? ) ";
						}
						//changed by Manish for ms sql server query on 04/09/2015
						
				
					 HashMap itemVolMap = getItemVoumeMap(itemCode, "", conn);
					 double packSize = (Double)itemVolMap.get("PACK_SIZE");
					 double itemWeight = (Double)itemVolMap.get("ITEM_WEIGHT");
					 System.out.println("itemWeight =["+itemWeight+"] packSize ["+packSize+"]");
					 if((pendingQty % packSize) > 0)
					{
						isActives = true;
						orderByStkStr = "AND LOCATION.INV_STAT IN(?,?,?,?,?) ORDER BY CASE WHEN STOCK.EXP_DATE IS NULL THEN STOCK.CREA_DATE ELSE STOCK.EXP_DATE END,INVSTAT.ALLOC_PREF, STOCK.LOC_CODE ";
					}
					else
					{
						isActives = false; 		//CHANGE BY RITESH ON 30/APR/14
//						orderByStkStr = "AND LOCATION.INV_STAT IN(?,?,?,?) ORDER BY CASE WHEN STOCK.EXP_DATE IS NULL THEN STOCK.CREA_DATE ELSE STOCK.EXP_DATE END,INVSTAT.ALLOC_PREF, STOCK.LOC_CODE ";
						orderByStkStr = "AND LOCATION.INV_STAT IN(?,?,?,?) ORDER BY CASE WHEN STOCK.EXP_DATE IS NULL THEN STOCK.CREA_DATE ELSE STOCK.EXP_DATE END,INVSTAT.ALLOC_PREF_CASE, STOCK.LOC_CODE ";
					}
					 
					/*if ( singleLot.trim().length() > 0 && singleLot.trim().equalsIgnoreCase("Y") )
						{*/
							//sSingleLotSql = sSingleLotSql + orderByStkStr;
							/*pstmt = conn.prepareStatement(sSingleLotSql + orderByStkStr);
							pstmt.setString(1,"Y");
							pstmt.setString(2,itemCode);
							pstmt.setString(3,siteCode);
							pstmt.setString(4,lotNo);
							//pstmt.setString(5,siteCode);
							pstmt.setDouble(5,pendingQty);
							pstmt.setInt(6,minSelfLife);
							pstmt.setString(7,resrvLoc);
							pstmt.setString(8,casePickLoc);
							pstmt.setString(9,activePickLoc);
							pstmt.setString(10,deepStoreLoc);
							if(isActives)
							{
								pstmt.setString(11,partialResrvLoc);
							}*/
							
							pstmt = conn.prepareStatement(sSingleLotSql + orderByStkStr);
							pstmt.setString(1,itemCode);
							pstmt.setString(2,siteCodeShip);
							pstmt.setString(3,itemCode);
							pstmt.setString(4,siteCodeShip);
							pstmt.setString(5,lotNo);
							//pstmt.setDouble(6,pendingQty);
							pstmt.setInt(6,minSelfLife);
							pstmt.setString(7,resrvLoc);
							pstmt.setString(8,casePickLoc);
							pstmt.setString(9,activePickLoc);
							pstmt.setString(10,deepStoreLoc);
							
							if(isActives)
							{
								pstmt.setString(11,partialResrvLoc);
							}
						rs = pstmt.executeQuery();	
						String lotSl = "";
						locCode = "";
						locDescr = "";
						System.out.println("==singleLot=="+singleLot);												// ADDED BY RITESH ON 30/06/14 START
						if("N".equalsIgnoreCase(singleLot.trim()))
						{
							if( rs.next())
							{
								qtyAvail = rs.getDouble("QTY_AVAIL_ALLOC");  //quantity
								lotSl = rs.getString("LOT_SL");
								locCode = rs.getString("LOC_CODE");
								locDescr = rs.getString("DESCR");
							}
							if(pendingQty < qtyAvail)
							{
								qtyAvail = pendingQty;
							}
						}
						if("Y".equalsIgnoreCase(singleLot.trim()))
						{
							while(rs.next())
							{
								lotNo=rs.getString("LOT_NO");
								lotNo = rs.getString("LOT_NO");
								lotSl = rs.getString("LOT_SL");
								locCode = rs.getString("LOC_CODE");
								qtyAvail = rs.getDouble("QTY_AVAIL_ALLOC");
								locDescr = rs.getString("DESCR");
								if(stockLot.containsKey(lotNo))
								{
									stockLot.put(lotNo, ((Double)stockLot.get(lotNo)+qtyAvail));
									ArrayList<String> stockList=new ArrayList<String>();
									stockList=stockLotDetail.get(lotNo);
									stockList.add(lotNo+"@"+lotSl+"@"+locCode+"@"+locDescr+"@"+qtyAvail);
									stockLotDetail.put(lotNo, stockList);
								}
								else
								{	
									stockLot.put(lotNo,qtyAvail);
									ArrayList<String> stockList=new ArrayList<String>();
									//stockList=stockLotDetail.get(lotNo);
									System.out.println(lotNo+"@"+lotSl+"@"+locCode+"@"+locDescr+"@"+qtyAvail);
									stockList.add(lotNo+"@"+lotSl+"@"+locCode+"@"+locDescr+"@"+qtyAvail);
									stockLotDetail.put(lotNo, stockList);
								}
							}
							rs.close(); rs = null;
							pstmt.close(); pstmt = null;
							String lotDetail="";
							
							Set<String> lotKey=stockLot.keySet();
							Iterator<String> lot=lotKey.iterator();
							while(lot.hasNext())
							{
								String lotKeyVal=lot.next();
								double qty=Double.parseDouble(""+stockLot.get(lotKeyVal));
								System.out.println("qty---"+qty+"---pendingQty---"+pendingQty);
								if(qty>=pendingQty)
								{
									lotDetail=stockLotDetail.get(lotKeyVal).get(0);
									break;
								}
							}
//							Enumeration enu = Collections.enumeration(lotKey);
//							while(enu.hasMoreElements())
//							{
//								String lotKeyVal=(String)enu.nextElement();
//								double qty=Double.parseDouble(""+stockLot.get(lotKeyVal));
//								System.out.println("qty--1-"+qty+"---pendingQty--1-"+pendingQty);
//								if(qty>=pendingQty)
//								{
//									lotDetail=stockLotDetail.get(lotKeyVal).get(0);
//									break;
//								}
//							}
							lotNo="";
							lotSl="";
							locCode="";
							locDescr="";
							qtyAvail=0;
							if(lotDetail.length()>0)
							{
								String[] lotArray=lotDetail.split("@");
								lotNo=lotArray[0];
								lotSl=lotArray[1];
								locCode=lotArray[2];
								locDescr=lotArray[3];
								qtyAvail=Double.parseDouble(lotArray[4]);
								//qtyAvail=pendingQty;	
							}
						}																 // ADDED BY RITESH ON 30/06/14 END for issue no . 209
						valueXmlString.append("<quantity>").append("<![CDATA[" +  qtyAvail + "]]>").append("</quantity>");
						valueXmlString.append("<lot_sl>").append("<![CDATA[" +  lotSl + "]]>").append("</lot_sl>");
						valueXmlString.append("<loc_code>").append("<![CDATA[" +  locCode + "]]>").append("</loc_code>");
						valueXmlString.append("<location_descr>").append("<![CDATA[" +  locDescr + "]]>").append("</location_descr>");
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
				else if(currentColumn.trim().equalsIgnoreCase("quantity"))
				{
					String quantityset = checkNull(genericUtility.getColumnValue("quantity", dom));
					System.out.println("Set Quantity ["+quantityset+"]");
					valueXmlString.append("<quantity>").append("<![CDATA[" +  quantityset + "]]>").append("</quantity>");
				}
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
					if(pstmt != null)
						pstmt.close();
					if(rs != null)
						rs.close();
					rs = null;
					pstmt = null;
					conn.close();
					conn = null;
				}
			}
			catch(Exception d)
			{
				d.printStackTrace();
			}			
		}
		return valueXmlString.toString();
	}	 
	private String findValue(Connection conn, String columnName ,String tableName, String columnName2, String value) throws  ITMException, RemoteException
	{
		PreparedStatement pstmt = null ;
		ResultSet rs = null ; 
		String sql = "";
		String findValue = "";
		try
		{			
			sql = "SELECT " + columnName + " from " + tableName + " where " + columnName2 +"= ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,value);
			rs = pstmt.executeQuery();
			if(rs.next())
			{					
				findValue = rs.getString(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;	        
		}
		catch(Exception e)
		{
			System.out.println("Exception in findValue ");
			e.printStackTrace();
			throw new ITMException(e); 
		}
		System.out.println("returning String from findValue " + findValue);
		return findValue;
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
			while(rs.next())
			{
				msgType = rs.getString("MSG_TYPE");
			}			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new ITMException(ex); //Added By Mukesh Chauhan on 05/08/19
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
	private String checkNull( String input )
	{
		if (input == null )
		{
			input = "";
		}
		return input;
	}
	//Changed by sumit on 21/08/12 checking duplicate deatil start
	private boolean checkDetail(Document allDom, Connection conn) throws ITMException
	{
		String itemCode = "", siteCode ="", locCode = "", lotSl = "", lotNo ="";
		NodeList parentNodeList = null, childNodeList  = null;
		Node parentNode = null, childNode = null;
	    int parentNodeListLength = 0;
	    String childNodeName = "";
		String domID = "";
		Map detailMap = new HashMap();		
		boolean isDupDetail = false;
		String updateFlag = "";// added by sumit on 07/01/13
		try {
			parentNodeList = allDom.getElementsByTagName("Detail2");
			parentNodeListLength = parentNodeList.getLength();
			for (int row = 0; row < parentNodeList.getLength(); row++) 
			{
				parentNode = parentNodeList.item(row);
				childNodeList = parentNode.getChildNodes();
				
				System.out.println("************* first For loop *************["+ parentNode.getNodeName() + "]");
				for (int col = 0; col < childNodeList.getLength(); col++) {
					childNode = childNodeList.item(col);
					domID = parentNode.getAttributes().getNamedItem("domID").getNodeValue();
					
					if (childNode.getNodeType() == childNode.ELEMENT_NODE) 
					{
						childNodeName = childNode.getNodeName();
						//domID = parentNode.getAttributes().getNamedItem("domID").getNodeValue();
						//System.out.println("=======--======== ["+domID+"]");
						//System.out.println("childNodeNamechildNodeName ** ["+childNodeName+"]");
						if ( "attribute".equalsIgnoreCase( childNodeName ) )
						{
							//System.out.println(" natribute ");						
							updateFlag = childNode.getAttributes().getNamedItem("updateFlag").getNodeValue();
							System.out.println(" updateFlag suit ["+updateFlag+"]");
						}
						if( !"D".equalsIgnoreCase(updateFlag))
						{
							if ("item_code".equalsIgnoreCase(childNodeName)) 
							{
								itemCode = childNode.getFirstChild().getNodeValue().trim();							
								
							}
							else if ("site_code".equalsIgnoreCase(childNodeName)) 
							{
								siteCode = childNode.getFirstChild().getNodeValue().trim();							
							}
							else if ("loc_code".equalsIgnoreCase(childNodeName)) 
							{
								locCode = childNode.getFirstChild().getNodeValue().trim();							
							}
							else if ("lot_no".equalsIgnoreCase(childNodeName)) 
							{
								lotNo = childNode.getFirstChild().getNodeValue().trim();							
							}
							else if ("lot_sl".equalsIgnoreCase(childNodeName)) 
							{
								lotSl = childNode.getFirstChild().getNodeValue().trim();
								/*arryList.add(childNode.getFirstChild().getNodeValue().trim());
								tempMap.put("lot_sl", childNode.getFirstChild().getNodeValue().trim());*/
							}
						}
					}							
				}
				/*tempList.add(tempMap);
				System.out.println(" detail arrayaList  "+arryList);*/
				if(detailMap.containsKey(itemCode+siteCode+locCode+lotNo+lotSl))
				{
					isDupDetail = true;
				}
				else
				{
					detailMap.put(itemCode+siteCode+locCode+lotNo+lotSl, "Detail2:"+domID);
				}
				System.out.println(" detailMap in checkdetail "+detailMap);	
				System.out.println(" isDupDetail "+isDupDetail);							
			}	
		} 
		catch (Exception e) 
		{
			System.out.println( "Exception : StockAllocIc:wfValData:checkDetail : " + e.getMessage() );
			throw new ITMException(e);
		}
		return isDupDetail;
	}
	//Changed by sumit on 21/08/12 checking duplicate deatil end.

	private HashMap getItemVoumeMap(String itemCode,String lotNo,Connection con)throws Exception
	{
		double packSize = 0,itemSize = 0,lotSize = 0;
		PreparedStatement pstmt = null;
		String sql="";
		ResultSet rs = null;
		double itmLen = 0,itmWidth = 0,itmHeight = 0,itemWeight = 0,lotLen = 0 ,lotHeight = 0,lotWidth = 0,lotWeight = 0;
		HashMap dataVolumeMap = new HashMap();
		try {
			
			sql = "SELECT I.LENGTH ITEM_LEN,I.WIDTH ITEM_WID,I.HEIGHT ITEM_HEIGHT,I.GROSS_WEIGHT ITEM_WEIGHT,"
				  +" L.LENGTH LITEM_LEN,L.WIDTH LITEM_WID,L.HEIGHT LITEM_HEIGHT,L.SHIPPER_SIZE SHIPSIZE,L.GROSS_WEIGHT LOT_WEIGHT FROM"
				  +" ITEM I,ITEM_LOT_PACKSIZE L"
				  +" WHERE I.ITEM_CODE = L.ITEM_CODE"
				  +" AND L.LOT_NO__FROM <= ? AND L.LOT_NO__TO >= ?"
				  +" AND  I.ITEM_CODE = ?";
				  
			
			pstmt = con.prepareStatement(sql);
			if(lotNo != null && lotNo.length() > 0)
			{
				pstmt.setString(1, lotNo);
				pstmt.setString(2, lotNo);
			}
			else
			{
				pstmt.setString(1, "00");
				pstmt.setString(2, "ZZ");
			}
			pstmt.setString(3, itemCode);
			rs = pstmt.executeQuery();
			
			if(rs.next())
			{
				itmLen = rs.getDouble("ITEM_LEN");
				itmWidth = rs.getDouble("ITEM_WID");
				itmHeight = rs.getDouble("ITEM_HEIGHT");
				itemWeight = rs.getDouble("ITEM_WEIGHT");
				lotLen = rs.getDouble("LITEM_LEN");
				lotWidth = rs.getDouble("LITEM_WID");
				lotHeight = rs.getDouble("LITEM_HEIGHT");				
				packSize = rs.getDouble("SHIPSIZE");				
				lotWeight = rs.getDouble("LOT_WEIGHT");				
			}
			//packSize = (lotHeight * lotWidth * lotLen)/(itmLen * itmWidth * itmHeight);
			/*itemSize = Math.floor(itmLen * itmWidth * itmHeight);
			lotSize = Math.floor((lotHeight * lotWidth * lotLen));*/
			itemSize = itmLen * itmWidth * itmHeight;
			lotSize = lotHeight * lotWidth * lotLen;
			
			dataVolumeMap.put("PACK_SIZE", packSize);
			dataVolumeMap.put("ITEM_SIZE", itemSize);
			dataVolumeMap.put("LOT_SIZE", lotSize);
			dataVolumeMap.put("ITEM_WEIGHT", itemWeight);
			dataVolumeMap.put("PACK_WEIGHT", lotWeight);
			
			if(pstmt != null)
			{
				pstmt.close();
				pstmt = null;
			}
			if(rs != null)
			{
				rs.close();
				rs = null;
			}
		} catch (Exception e) {
			// TODO: handle exception
			throw e;
		}
		finally
		{
			if(pstmt != null)
			{
				pstmt.close();
				pstmt = null;
			}
			if(rs != null)
			{
				rs.close();
				rs = null;
			}
		}
		return dataVolumeMap;
	}
	//Changed by Rohan on 13-06-13 for bug fixing of quantity not greater than pending quantity.start
	//Changed by sumit on 21/12/12 quantity checking in all present deatil start. 
	/*
	private boolean validateAllDetailQty(String saleOrder, String lineNoSord, Document allDom, Connection conn) throws ITMException
	{
		double quantity = 0.0, qtyavil = 0.0;
		boolean result = false;
		NodeList parentNodeList = null, childNodeList  = null;
		Node parentNode = null, childNode = null;
	    int parentNodeListLength = 0;
	    String childNodeName = "";
		String sql = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String updateFlag = "";//added by sumit on 07/01/13
		
		try {
			parentNodeList = allDom.getElementsByTagName("Detail2");
			parentNodeListLength = parentNodeList.getLength();
			for (int row = 0; row < parentNodeList.getLength(); row++) 
			{
				parentNode = parentNodeList.item(row);
				childNodeList = parentNode.getChildNodes();
				
				System.out.println("*****SUMIT******** first For loop *************["+ parentNode.getNodeName() + "]");
				for (int col = 0; col < childNodeList.getLength(); col++) 
				{
					childNode = childNodeList.item(col);					
					if (childNode.getNodeType() == childNode.ELEMENT_NODE) 
					{
						childNodeName = childNode.getNodeName();
						System.out.println("childNodeNamechildNodeName ** ["+childNodeName+"]"+"saleOrder"+saleOrder+"lineNoSord"+lineNoSord);
						if ( "attribute".equalsIgnoreCase( childNodeName ) )
						{
							//System.out.println(" natribute ");						
							updateFlag = childNode.getAttributes().getNamedItem("updateFlag").getNodeValue();
							System.out.println(" updateFlag suit ["+updateFlag+"]");
							
						}
						if( !"D".equalsIgnoreCase(updateFlag))
						{
							if ("quantity".equalsIgnoreCase(childNodeName)) 
							{
								//arryList.add(childNode.getFirstChild().getNodeValue().trim());
								System.out.println("Quantity before"+quantity);
								quantity = quantity + Double.parseDouble(childNode.getFirstChild().getNodeValue());		
								System.out.println("Quantity Afetre"+quantity);
								
							}
						}
					}
				}
			}
			
			sql = "SELECT (QUANTITY - QTY_ALLOC) AS AVILQTY FROM SORDITEM WHERE SALE_ORDER = ? AND LINE_NO = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, saleOrder);
			pstmt.setString(2, lineNoSord);
			rs = pstmt.executeQuery();
			if( rs.next())
			{
				qtyavil = rs.getDouble("AVILQTY");
			}
			rs.close();rs = null;
			pstmt.close();
			pstmt=null;
			System.out.println(" quantity ["+quantity+"] qtyavil["+qtyavil+"]");
			if(quantity > qtyavil )
			{
				result = true;
			}
			
		} 
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		
		return result;
	}
	//Changed by sumit on 21/12/12 quantity checking in all present deatil end.
	 * 
	 */
	//Changed by Rohan on 13-06-13 for bug fixing of quantity not greater than pending quantity.end
	//Changed by sumit on 07/03/13 getting value after sale order and line_no_sord start
	private Map getStockDetail(String siteCode, String saleOrder, String lineNoSord, Connection conn, String itemCode,String activePickAllow) throws ITMException
	{
		String singleLot = "";
		String resrvLoc  = "";
		String casePickLoc  = "", activePickLoc  = "", deepStoreLoc = "", partialResrvLoc = "";
		String locCode = "";
		String locDescr = "";
		String lotNo = "";
		String sSingleLotSql = "";
		String orderByStkStr = "";
		int minSelfLife = 0, pendingQty = 0;
		double qtyAvail = 0.0;
		boolean isActives = false;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Map stockDetail = new HashMap();
		DistCommon discommon = new DistCommon();	
		
		TreeMap stockLot=new TreeMap<String,Double>();
		TreeMap<String,ArrayList<String>>stockLotDetail=new TreeMap<String, ArrayList<String>>();
		System.out.println("getStockDetail::called::040714");
		try
		{
			String getDataSql =" SELECT SORDER.CUST_CODE, CUSTOMER.CUST_NAME,SORDDET.LINE_NO, "
				+"SORDER.SALE_ORDER,SORDER.DUE_DATE,SORDITEM.ITEM_CODE,"
				+"ITEM.DESCR,SORDITEM.QUANTITY,"
				+"((SORDITEM.QUANTITY*SORDITEM.CONV__QTY_STDQTY) - (SORDITEM.QTY_DESP*SORDITEM.CONV__QTY_STDQTY) - SORDITEM.QTY_ALLOC) PENDING_QUANTITY,"
				+"SORDITEM.QTY_ALLOC,SORDDET.PACK_INSTR,"
				+"SORDER.SITE_CODE,SORDITEM.EXP_LEV, "
				+"SORDER.PART_QTY AS PART_QTY, SORDER.SINGLE_LOT AS SINGLE_LOT, SORDITEM.MIN_SHELF_LIFE AS MIN_SHELF_LIFE "//Gulzar 5/13/2012
				+" ,SORDER.ALLOC_FLAG AS ALLOC_FLAG , WAVE_TYPE.MASTER_PACK_ALLOW , WAVE_TYPE.ACTIVE_PICK_ALLOW , WAVE_TYPE.STOCK_TO_DOCK_ALLOW "		   
				+"FROM SORDDET,SORDER,SORDITEM, ITEM , "

				//Changed by Manish 25/09/15 for mssql .
				+" CUSTOMER left outer join WAVE_TYPE WAVE_TYPE  on  ( CUSTOMER.WAVE_TYPE = WAVE_TYPE.WAVE_TYPE) "
				//Changed by Manish 25/09/15 for mssql end.

				//Changed by sumit 20/08/12 getting column data end.
				+"WHERE ( SORDER.SALE_ORDER = SORDDET.SALE_ORDER ) AND "
				+"( SORDITEM.SALE_ORDER = SORDER.SALE_ORDER ) AND "
				+"( SORDDET.LINE_NO = SORDITEM.LINE_NO ) AND "
				+"( SORDITEM.ITEM_CODE = ITEM.ITEM_CODE ) AND "
				+"( SORDER.CUST_CODE = CUSTOMER.CUST_CODE ) AND  "
				+"( SORDDET.SITE_CODE = SORDITEM.SITE_CODE ) AND  "
				//Changed by sumit on 20/08/12 join customer with wave_type
				//+"( CUSTOMER.WAVE_TYPE = WAVE_TYPE.WAVE_TYPE(+)) AND "
				//+" SORDER.SITE_CODE = ? AND "							   
				+" SORDER.SALE_ORDER = ? "
				+" AND SORDITEM.LINE_NO = ? "
				+"AND CASE WHEN SORDDET.STATUS IS NULL THEN 'P' ELSE SORDDET.STATUS end <> 'C' "
				+"AND CASE WHEN SORDER.STATUS IS NULL THEN 'P' ELSE SORDER.STATUS end = 'P' "
				//Changed by sumit on 12/09/12 considering hold_flag from sorddet 
				+" AND CASE WHEN SORDDET.HOLD_FLAG IS NULL THEN 'N' ELSE SORDDET.HOLD_FLAG end <> 'Y'"
				//Chnaged by Rohan on 11/07/12 revert Changes of Manual Stock Allocation
				//Changed by Rohan on 22/06/12
				//+"AND SORDITEM.QUANTITY - SORDITEM.QTY_DESP - SORDITEM.QTY_ALLOC > 0 "
				//+"AND (SORDITEM.QUANTITY - SORDITEM.QTY_DESP - SORDITEM.QTY_ALLOC > 0 OR SORDER.ALLOC_FLAG='M' )"
				//Changed by sumit on 13/08/12 as per manual stock allocation start.
				//+"AND SORDITEM.QUANTITY - SORDITEM.QTY_DESP - SORDITEM.QTY_ALLOC > 0 "
				+" AND (((SORDITEM.QUANTITY*SORDITEM.CONV__QTY_STDQTY) - (SORDITEM.QTY_DESP*SORDITEM.CONV__QTY_STDQTY) - SORDITEM.QTY_ALLOC > 0 ) "  ;

			//Changed by Manish on 25/09/2015 for ms sql server.
			String DB = CommonConstants.DB_NAME;
			if("mssql".equalsIgnoreCase(DB))
			{
				getDataSql	= getDataSql+" OR (dbo.FN_CHECK_MANUAL_STOCK_ALLOC(SORDER.SALE_ORDER, SORDDET.LINE_NO) = 0 ) )"	 
				+"AND SORDITEM.LINE_TYPE = 'I'";
			}
			else
			{
				getDataSql	= getDataSql+" OR (FN_CHECK_MANUAL_STOCK_ALLOC(SORDER.SALE_ORDER, SORDDET.LINE_NO) = 0 ) )"	 
				+"AND SORDITEM.LINE_TYPE = 'I'";					   
			}
			//Changed by Manish on 25/09/2015 for ms sql server.
			
			 pstmt = conn.prepareStatement(getDataSql);
			 pstmt.setString(1, saleOrder);
			 pstmt.setString(2, lineNoSord);
			 rs = pstmt.executeQuery();
			 if( rs.next())
			 {
				 singleLot = rs.getString("SINGLE_LOT")==null?"N":rs.getString("SINGLE_LOT");
				 minSelfLife = rs.getInt("MIN_SHELF_LIFE");
				 pendingQty = rs.getInt("PENDING_QUANTITY");
			 }
			 System.out.println(" singleLot ["+singleLot+"] minSelfLife ["+minSelfLife+"] pendingQty ["+pendingQty+"]");
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
			 
			 resrvLoc  = discommon.getDisparams("999999","RESERV_LOCATION",conn);
	            
			 casePickLoc  = discommon.getDisparams("999999","CASE_PICK_INVSTAT",conn);
			 activePickLoc  = discommon.getDisparams("999999","ACTIVE_PICK_INVSTAT",conn);
			 deepStoreLoc = discommon.getDisparams("999999","DEEP_STORE_INVSTAT",conn);
			 partialResrvLoc = discommon.getDisparams("999999","PRSRV_INVSTAT",conn);			
			 System.out.println("CASE_PICK_INVSTAT"+casePickLoc+"ACTIVE_PICK_INVSTAT"+activePickLoc+"RESERV_LOCATION"+resrvLoc+"PARTIAL RESERVE LOC["+partialResrvLoc+"]");
			if("N".equalsIgnoreCase(activePickAllow))
			{
				activePickLoc="";
				partialResrvLoc="";
			}
			sSingleLotSql = "SELECT STOCK.LOT_NO,STOCK.LOT_SL,"
				+"STOCK.LOC_CODE, "
				+"STOCK.UNIT,  "
				+"(STOCK.QUANTITY - STOCK.ALLOC_QTY - (CASE WHEN STOCK.HOLD_QTY IS NULL THEN 0 ELSE STOCK.HOLD_QTY END)) AS QTY_AVAIL_ALLOC ,"
				+"STOCK.GRADE,STOCK.EXP_DATE,STOCK.CONV__QTY_STDUOM,STOCK.QUANTITY, " 					   
				+"STOCK.MFG_DATE,STOCK.SITE_CODE__MFG, "
				+"STOCK.NO_ART,INVSTAT.INV_STAT,ITEM.LOC_TYPE__PARENT,ITEM.LOC_TYPE,ITEM.LOC_ZONE__PREF,LOCATION.DESCR " 
				+"FROM STOCK,ITEM,LOCATION,INVSTAT " 
				+"WHERE (ITEM.ITEM_CODE = STOCK.ITEM_CODE) "
				+"AND (LOCATION.LOC_CODE = STOCK.LOC_CODE ) "
				+"AND (LOCATION.INV_STAT = INVSTAT.INV_STAT) "
				+"AND INVSTAT.AVAILABLE = 'Y' "
				+"AND STOCK.ITEM_CODE = ? AND STOCK.SITE_CODE = ? "
				+"AND (STOCK.QUANTITY - STOCK.ALLOC_QTY - (CASE WHEN STOCK.HOLD_QTY IS NULL THEN 0 ELSE STOCK.HOLD_QTY END) > 0) "
				//+"AND STOCK.LOT_NO IN (SELECT LOT_NO FROM STOCK WHERE ITEM_CODE =? AND SITE_CODE =? AND LOT_NO = ? "
				//+"GROUP BY LOT_NO HAVING SUM(QUANTITY - ALLOC_QTY - (CASE WHEN STOCK.HOLD_QTY IS NULL THEN 0 ELSE STOCK.HOLD_QTY END) ) > 0) "
				+"AND STOCK.LOT_NO IN (SELECT LOT_NO FROM STOCK WHERE ITEM_CODE =? AND SITE_CODE =? "
				+"GROUP BY LOT_NO HAVING SUM(QUANTITY - ALLOC_QTY - (CASE WHEN STOCK.HOLD_QTY IS NULL THEN 0 ELSE STOCK.HOLD_QTY END) ) > 0) " ;
			//	+"AND (MONTHS_BETWEEN(TO_DATE(STOCK.EXP_DATE),SYSDATE) > ? ) ";
			//+"AND NOT EXISTS (SELECT 1 FROM INV_RESTR I WHERE I.INV_STAT = INVSTAT.INV_STAT AND I.REF_SER = 'S-DSP' ) ";

			
			//changed by Manish for ms sql server query on 04/09/2015
			if("mssql".equalsIgnoreCase(DB))
			{
				sSingleLotSql =  sSingleLotSql + " AND (DATEDIFF(mm,GETDATE(),CONVERT(DATETIME, STOCK.EXP_DATE)) > ?) ";	
			}
			else
			{
				sSingleLotSql =  sSingleLotSql + "AND (MONTHS_BETWEEN(TO_DATE(STOCK.EXP_DATE),SYSDATE) > ? ) ";
			}
			//changed by Manish for ms sql server query on 04/09/2015
			
			
			 HashMap itemVolMap = getItemVoumeMap(itemCode, "", conn);
			 double packSize = (Double)itemVolMap.get("PACK_SIZE");
			 double itemWeight = (Double)itemVolMap.get("ITEM_WEIGHT");
			 System.out.println("itemWeight =["+itemWeight+"] packSize ["+packSize+"]");
			 if((pendingQty % packSize) > 0)
			 {
				isActives = true;
				orderByStkStr = "AND LOCATION.INV_STAT IN(?,?,?,?,?) ORDER BY CASE WHEN STOCK.EXP_DATE IS NULL THEN STOCK.CREA_DATE ELSE STOCK.EXP_DATE END,INVSTAT.ALLOC_PREF, STOCK.LOC_CODE ";
			 }
			 else
			 {
				isActives = false;		//CHANGE BY RITESH ON 30/APR/14
//				orderByStkStr = "AND LOCATION.INV_STAT IN(?,?,?,?) ORDER BY CASE WHEN STOCK.EXP_DATE IS NULL THEN STOCK.CREA_DATE ELSE STOCK.EXP_DATE END,INVSTAT.ALLOC_PREF, STOCK.LOC_CODE ";
				orderByStkStr = "AND LOCATION.INV_STAT IN(?,?,?,?) ORDER BY CASE WHEN STOCK.EXP_DATE IS NULL THEN STOCK.CREA_DATE ELSE STOCK.EXP_DATE END,INVSTAT.ALLOC_PREF_CASE, STOCK.LOC_CODE ";
			 }
			
			pstmt = conn.prepareStatement(sSingleLotSql + orderByStkStr);
			pstmt.setString(1,itemCode);
			pstmt.setString(2,siteCode);
			pstmt.setString(3,itemCode);
			pstmt.setString(4,siteCode);
			//pstmt.setString(5,lotNo);
			//pstmt.setDouble(6,pendingQty);
			pstmt.setInt(5,minSelfLife);
			pstmt.setString(6,resrvLoc);
			pstmt.setString(7,casePickLoc);
			pstmt.setString(8,activePickLoc);
			pstmt.setString(9,deepStoreLoc);
			
			if(isActives)
			{
				pstmt.setString(10,partialResrvLoc);
			}
			rs = pstmt.executeQuery();	
			String lotSl = "";
			locCode = "";
			locDescr = "";
			if("N".equalsIgnoreCase(singleLot))
			{
				if( rs.next())
				{
					qtyAvail = rs.getDouble("QTY_AVAIL_ALLOC");  //quantity
					lotNo = rs.getString("LOT_NO");
					lotSl = rs.getString("LOT_SL");
					locCode = rs.getString("LOC_CODE");
					locDescr = rs.getString("DESCR");
				}			
				rs.close(); rs = null;
				pstmt.close(); pstmt = null;
				if(pendingQty < qtyAvail)
				{
					qtyAvail = pendingQty;
				}
			}
			else
			{
				while(rs.next())
				{
					lotNo=rs.getString("LOT_NO");
					lotNo = rs.getString("LOT_NO");
					lotSl = rs.getString("LOT_SL");
					locCode = rs.getString("LOC_CODE");
					qtyAvail = rs.getDouble("QTY_AVAIL_ALLOC");
					locDescr = rs.getString("DESCR");
					System.out.println("lot_no ::"+lotNo +"== qtyAvail ::"+qtyAvail);
					if(stockLot.containsKey(lotNo))
					{
						stockLot.put(lotNo, ((Double)stockLot.get(lotNo)+qtyAvail));
						ArrayList<String> stockList=new ArrayList<String>();
						stockList=stockLotDetail.get(lotNo);
						stockList.add(lotNo+"@"+lotSl+"@"+locCode+"@"+locDescr+"@"+qtyAvail);
						stockLotDetail.put(lotNo, stockList);
					}
					else
					{	
						stockLot.put(lotNo,qtyAvail);
						ArrayList<String> stockList=new ArrayList<String>();
						//stockList=stockLotDetail.get(lotNo);
						System.out.println(lotNo+"@"+lotSl+"@"+locCode+"@"+locDescr+"@"+qtyAvail);
						stockList.add(lotNo+"@"+lotSl+"@"+locCode+"@"+locDescr+"@"+qtyAvail);
						stockLotDetail.put(lotNo, stockList);
					}
				}
				stockLotDetail.descendingMap();
				stockLot.descendingMap();
				rs.close(); rs = null;
				pstmt.close(); pstmt = null;
				String lotDetail="";
				Set<String> lotKey=stockLot.keySet();
				System.out.println("lotKey::"+lotKey);

				Iterator<String> lot=lotKey.iterator();
				while(lot.hasNext())
				{
					String lotKeyVal=lot.next();
					System.out.println("lotKeyVal::"+lotKeyVal);
					double qty=Double.parseDouble(""+stockLot.get(lotKeyVal));
					System.out.println("qty---1"+qty+"---pendingQty---1"+pendingQty);
					if(qty>=pendingQty)
					{
						lotDetail=stockLotDetail.get(lotKeyVal).get(0);
						break;
					}
				}
//				Enumeration enu = Collections.enumeration(lotKey);
//				while(enu.hasMoreElements())
//				{
//					String lotKeyVal=(String)enu.nextElement();
//					double qty=Double.parseDouble(""+stockLot.get(lotKeyVal));
//					System.out.println("qty--1-"+qty+"---pendingQty--1-"+pendingQty);
//					if(qty>=pendingQty)
//					{
//						lotDetail=stockLotDetail.get(lotKeyVal).get(0);
//						break;
//					}
//				}
				lotNo="";
				lotSl="";
				locCode="";
				locDescr="";
				qtyAvail=0;
				if(lotDetail.length()>0)
				{
					String[] lotArray=lotDetail.split("@");
					lotNo=lotArray[0];
					lotSl=lotArray[1];
					locCode=lotArray[2];
					locDescr=lotArray[3];
					qtyAvail=Double.parseDouble(lotArray[4]);
					//qtyAvail=pendingQty;	
				}
			}
			System.out.println(" @@@ 30-06-14");
			stockDetail.put("qtyAvail", qtyAvail);
			stockDetail.put("lotNo", lotNo);
			stockDetail.put("lotSl", lotSl);
			stockDetail.put("locCode", locCode);
			stockDetail.put("locDescr", locDescr);			
			//System.out.println(" stock detail :->"+stockDetail);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(rs != null)
				{
					rs.close(); rs = null;
				}
				if( pstmt != null)
				{
					pstmt.close(); pstmt = null;
				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				throw new ITMException(ex);
			}
		}
		return stockDetail;
	}
private  String getErrorString(String str ) {
	if(str == null)
	{
		str ="";
	}
	return str;
	// TODO Auto-generated method stub
}
}	

