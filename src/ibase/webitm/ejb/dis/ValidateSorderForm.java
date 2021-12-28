/*
 * Title : ValidateSorderForm []
 * Date  : 11-JUN-2019
 * Author: AMOL SANT
 * */
package ibase.webitm.ejb.dis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.dis.DistCommon;
 
public class ValidateSorderForm extends ValidatorEJB
{
	E12GenericUtility genericUtility = new E12GenericUtility();
	public String validateNature(Connection conn, Document dom, Document dom1, Document dom2, String nature, String itemCode)
	//public String validateNature(Connection conn, Document dom, Document dom1, Document dom2)
	{
		System.out.println("#### ValidateSorderForm  ");
		//
		String siteCode = "",custCode = "",tranId ="", sql = "",errCode = "" ;
		//String nature= "" ,itemCode = "";
		
		Timestamp OrderDate = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		PreparedStatement pstmt1 = null;
		ResultSet rs1 = null;
		//
		double  unConfTotFreeQty = 0 , 	 rate1 = 0,qtyTot=0.0,mRate = 0.00,value=0.0,valueAmount=0.0,quantity=0.0;
		double  freeQty = 0.0 ,prvFreeQty = 0.0;
		String  currLineNo = "",ldtDateStr = "",lsPriceList = "",retlSchmRateBase="",lsUnit="",lsListType="",lsRefNo="";
		
		String browItemCode = "", lineNo = "" ;
		Timestamp  ldtPlDate = null,ldPlistDate = null;
		int llPlcount=0;
		DistCommon distCommon = new DistCommon();
		
		try 
		{
				SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
				//nature = checkNull(genericUtility.getColumnValue("nature", dom));
				//itemCode= checkNull(genericUtility.getColumnValue("item_code", dom));
				siteCode = checkNull(genericUtility.getColumnValue("site_code", dom1));
				custCode = checkNull(genericUtility.getColumnValue("cust_code", dom1));
				OrderDate = Timestamp.valueOf(
							genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
							genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
							+ " 00:00:00.0");
				
				tranId = checkNull(genericUtility.getColumnValue("tran_id", dom2));
				System.out.println(" $$$$ Nature " +nature+" $$$$ Item Code "+itemCode);
				
				if(tranId.trim().length() == 0)
				{
					tranId=" ";
				}
				
				freeQty=0.0;
				qtyTot = checkDoubleNull(genericUtility.getColumnValue("qty_1", dom));

				if ("V".equalsIgnoreCase(nature.trim()))
				{	
					retlSchmRateBase = checkNull(distCommon.getDisparams( "999999", "RETL_SCHM_RATE_BASE", conn ));	
					
					ldtDateStr = genericUtility.getColumnValue("order_date", dom1);
					
					ldPlistDate = OrderDate;
					
					if("M".equalsIgnoreCase(retlSchmRateBase))
					{
						lsPriceList = checkNull(distCommon.getDisparams( "999999", "MRP", conn ));
						lsUnit = checkNull(genericUtility.getColumnValue("unit", dom));
						
						lsListType = distCommon.getPriceListType(lsPriceList, conn);
						System.out.println("#### lsListType " + lsListType);
						sql = "select count(1)  as llPlcount from pricelist where price_list=?"
								+ " and item_code= ? and unit= ? and list_type=? and eff_from<=? and valid_upto  >=? and min_qty<=? and max_qty>= ?"
								+ " and (ref_no is not null)";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsPriceList);
						pstmt.setString(2, itemCode);
						pstmt.setString(3, lsUnit);
						pstmt.setString(4, lsListType);
						pstmt.setTimestamp(5, OrderDate);
						pstmt.setTimestamp(6, OrderDate);
						pstmt.setDouble(7, qtyTot);
						pstmt.setDouble(8, qtyTot);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							llPlcount = rs.getInt("llPlcount");
						}
						System.out.println("#### llPlcount " + llPlcount);
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (llPlcount >= 1)
						{
							sql = "select max(ref_no) as ref_no from pricelist where price_list  =? and item_code= ? and unit=? and list_type= ?"
									+ " and eff_from<=? and valid_upto>=? and min_qty<=? and max_qty>=? and (ref_no is not null)";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lsPriceList);
							pstmt.setString(2, itemCode);
							pstmt.setString(3, lsUnit);
							pstmt.setString(4, lsListType);
							pstmt.setTimestamp(5, OrderDate);
							pstmt.setTimestamp(6, OrderDate);
							pstmt.setDouble(7, qtyTot);
							pstmt.setDouble(8, qtyTot);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								lsRefNo = rs.getString("ref_no");
							}
							System.out.println("#### ref_no " + lsRefNo);
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							mRate = distCommon.pickRateRefnoWise(lsPriceList, ldtDateStr, itemCode, lsRefNo, "L", qtyTot,conn);
						}
						if (mRate <= 0) 
						{
							mRate = distCommon.pickRateRefnoWise(lsPriceList, ldtDateStr, itemCode, lsRefNo, "L", qtyTot,
									conn);
						}
							
					}
					else
					{
						
						sql = " SELECT PRICE_LIST FROM CUSTOMER WHERE CUST_CODE= ?  ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,custCode);
							
							rs1 = pstmt.executeQuery();

							if (rs1.next()) 
							{
								lsPriceList =checkNull( rs1.getString(1));
							}
							rs1.close();
							rs1 = null;
							pstmt.close();
							pstmt = null;
						
						if (lsPriceList != null || lsPriceList.trim().length() > 0)
						{
							mRate = distCommon.pickRate(lsPriceList, ldtDateStr, itemCode, "", "L", qtyTot, conn);
							System.out.print("#### mRate gbfICquantity " + mRate);
							System.out.print("#### mqty " + qtyTot);
						}
						
					}
					valueAmount= qtyTot * mRate;
					
					sql = " SELECT BALANCE_FREE_VALUE - USED_FREE_VALUE  FROM SCHEME_BALANCE  WHERE  BALANCE_FREE_VALUE - USED_FREE_VALUE > 0 "
						+ " AND EFF_FROM <= ? AND VALID_UPTO >=?  AND CUST_CODE = ?  AND ITEM_CODE= ?  ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setTimestamp(1, OrderDate);
					pstmt.setTimestamp(2, OrderDate);
					pstmt.setString(3,custCode);
					pstmt.setString(4,"X");
					rs1 = pstmt.executeQuery();

					if (rs1.next()) 
					{
						freeQty = rs1.getDouble(1);
					}
					System.out.println("#### freeQty " + freeQty);
					rs1.close();
					rs1 = null;
					pstmt.close();
					pstmt = null;
					if(freeQty > 0) 
					{
						sql = " SELECT PRICE_LIST FROM CUSTOMER WHERE CUST_CODE= ?  ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,custCode);
						
						rs1 = pstmt.executeQuery();

						if (rs1.next()) 
						{
							lsPriceList =checkNull( rs1.getString(1));
						}
						rs1.close();
						rs1 = null;
						pstmt.close();
						pstmt = null;
						
						sql = " select ( (case when qty_1 is null then 0 else qty_1 end) + (case when qty_2 is null then 0 else qty_2 end)  + (case when qty_3 is null then 0 else qty_3 end)  +(case when qty_4 is null then 0 else qty_4 end)  +(case when qty_5 is null then 0 else qty_5 end)  + (case when qty_6 is null then 0 else qty_6 end) )  ,"
								+ " a.order_date,b.unit " +								
										" from sordform a,sordformdet b	where a.TRAN_ID = b.TRAN_ID and a.site_code = ?	"
										+ " and a.cust_code = ?  and a.TRAN_ID <> ? and a.order_date between ? and ?"
										+ " and (case when a.status is null then 'N' else trim(a.status) end )= 'N'	and b.nature in ('V')";

								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, siteCode);
								pstmt1.setString(2, custCode);
								pstmt1.setString(3, tranId);
								pstmt1.setTimestamp(4, OrderDate);
								pstmt1.setTimestamp(5, OrderDate);
								rs1 = pstmt1.executeQuery();
								while (rs1.next()) {
								
									
									qtyTot = rs1.getDouble(1);
									OrderDate = rs1.getTimestamp(2);
									lsUnit = rs1.getString(3);
									
								    ldtDateStr = sdf.format(OrderDate);

									
									ldPlistDate = OrderDate;
									
									
									if("M".equalsIgnoreCase(retlSchmRateBase)) 
									{
										lsPriceList = checkNull(distCommon.getDisparams( "999999", "MRP", conn ));//ALMRP
										lsListType = distCommon.getPriceListType(lsPriceList, conn);//L

										sql = "select count(1)  as llPlcount from pricelist where price_list=?"
												+ " and item_code= ? and unit= ? and list_type=? and eff_from<=? and valid_upto  >=? and min_qty<=? and max_qty>= ?"
												+ " and (ref_no is not null)";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, lsPriceList);
										pstmt.setString(2, itemCode);
										pstmt.setString(3, lsUnit);
										pstmt.setString(4, lsListType);
										pstmt.setTimestamp(5, OrderDate);
										pstmt.setTimestamp(6, OrderDate);
										pstmt.setDouble(7, qtyTot);
										pstmt.setDouble(8, qtyTot);
										rs = pstmt.executeQuery();
										if (rs.next()) 
										{
											llPlcount = rs.getInt("llPlcount");
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;

										if (llPlcount >= 1) 
										{
											sql = "select max(ref_no)from pricelist where price_list  =? and item_code= ? and unit=? and list_type= ?"
													+ " and eff_from<=? and valid_upto>=? and min_qty<=? and max_qty>=? and (ref_no is not null)";
											pstmt = conn.prepareStatement(sql);
											pstmt.setString(1, lsPriceList);
											pstmt.setString(2, itemCode);
											pstmt.setString(3, lsUnit);
											pstmt.setString(4, lsListType);
											pstmt.setTimestamp(5, OrderDate);
											pstmt.setTimestamp(6, OrderDate);
											pstmt.setDouble(7, qtyTot);
											pstmt.setDouble(8, qtyTot);
											rs = pstmt.executeQuery();
											if (rs.next()) 
											{
												lsRefNo = rs.getString("ref_no");
											}
											rs.close();
											rs = null;
											pstmt.close();
											pstmt = null;

											mRate = distCommon.pickRateRefnoWise(lsPriceList, ldtDateStr, itemCode, lsRefNo, "L", qtyTot,
													conn);
										}
										if (mRate <= 0) {
											mRate = distCommon.pickRateRefnoWise(lsPriceList, ldtDateStr, itemCode, lsRefNo, "L", qtyTot,
													conn);
										}
									}
									else
									{
										
										if (lsPriceList != null || lsPriceList.trim().length() > 0)
										{
											mRate = distCommon.pickRate(lsPriceList, ldtDateStr, itemCode, "", "L", qtyTot, conn);
											System.out.print("mRate gbfICquantity++++++++" + mRate);
											System.out.print("mqty++++++++" + qtyTot);
										}
										
									}
									value = qtyTot * mRate;
									unConfTotFreeQty= unConfTotFreeQty + value;
									System.out.println("unConfTotFreeQty separte free" + lsPriceList +ldtPlDate+ qtyTot );
									
									
								}
								pstmt1.close();
								rs1.close();
								pstmt1 = null;
								rs1 = null;
								
								Node currDetail1 = null;
								prvFreeQty = 0;
								int count = 0;
								currLineNo = checkNull(genericUtility.getColumnValue("line_no", dom));
								NodeList detailList1 = dom2.getElementsByTagName("Detail2");
								
								
								int noOfDetails = detailList1.getLength();
								
								
								OrderDate = Timestamp.valueOf(
										genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom1),
												genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
												+ " 00:00:00.0");
							
								ldtDateStr = genericUtility.getColumnValue("order_date", dom1);

								ldPlistDate = OrderDate;
								for (int ctr1 = 0; ctr1 < noOfDetails; ctr1++)
								{

									currDetail1 = detailList1.item(ctr1);
									
									lineNo = checkNull(genericUtility.getColumnValueFromNode("line_no", currDetail1));
									nature = checkNull(genericUtility.getColumnValueFromNode("nature", currDetail1));
									browItemCode = checkNull(genericUtility.getColumnValueFromNode("item_code", currDetail1));
									quantity = checkDoubleNull(genericUtility.getColumnValueFromNode("totqty", currDetail1));
									lsUnit = checkNull(genericUtility.getColumnValueFromNode("unit", currDetail1));
									System.out.println("rate1: ====" + rate1 + "]lineNo" + lineNo + "quantity"+quantity);
									
									System.out.println("currLineNo: ====" + currLineNo + "]lineNo" + lineNo + "");
								
									if (!currLineNo.trim().equalsIgnoreCase(lineNo.trim()))
									{
										System.out.println("##### Inside if forSCHEME_BALANCE ");
											if (nature.equals("V"))
											{
												
												if("M".equalsIgnoreCase(retlSchmRateBase)) 
												{
													lsPriceList = checkNull(distCommon.getDisparams( "999999", "MRP", conn ));
													lsListType = distCommon.getPriceListType(lsPriceList, conn);
													
													System.out.println("#### lsPriceList ["+lsPriceList+" #### lsListType "+lsListType);
													
													sql = "select count(1)  as llPlcount from pricelist where price_list=?"
															+ " and item_code= ? and unit= ? and list_type=? and eff_from<=? and valid_upto  >=? and min_qty<=? and max_qty>= ?"
															+ " and (ref_no is not null)";
													pstmt = conn.prepareStatement(sql);
													pstmt.setString(1, lsPriceList);
													pstmt.setString(2, browItemCode);
													pstmt.setString(3, lsUnit);
													pstmt.setString(4, lsListType);
													pstmt.setTimestamp(5, OrderDate);
													pstmt.setTimestamp(6, OrderDate);
													pstmt.setDouble(7, quantity);
													pstmt.setDouble(8, quantity);
													rs = pstmt.executeQuery();
													if (rs.next())
													{
														llPlcount = rs.getInt("llPlcount");
													}
													System.out.println("##### llPlcount ");
													rs.close();
													rs = null;
													pstmt.close();
													pstmt = null;

													if (llPlcount >= 1)
													{
														sql = "select max(ref_no)from pricelist where price_list  =? and item_code= ? and unit=? and list_type= ?"
																+ " and eff_from<=? and valid_upto>=? and min_qty<=? and max_qty>=? and (ref_no is not null)";
														pstmt = conn.prepareStatement(sql);
														pstmt.setString(1, lsPriceList);
														pstmt.setString(2, browItemCode);
														pstmt.setString(3, lsUnit);
														pstmt.setString(4, lsListType);
														pstmt.setTimestamp(5, OrderDate);
														pstmt.setTimestamp(6, OrderDate);
														pstmt.setDouble(7, quantity);
														pstmt.setDouble(8, quantity);
														rs = pstmt.executeQuery();
													
														if (rs.next())
														{
															lsRefNo = rs.getString("ref_no");
														}
													
														rs.close();
														rs = null;
														pstmt.close();
														pstmt = null;

														mRate = distCommon.pickRateRefnoWise(lsPriceList, ldtDateStr, browItemCode, lsRefNo, "L", quantity,
																conn);
													}
													if (mRate <= 0)
													{
														mRate = distCommon.pickRateRefnoWise(lsPriceList, ldtDateStr, browItemCode, lsRefNo, "L", quantity,
																conn);
													}
												}
												else
												{
													
													if (lsPriceList != null || lsPriceList.trim().length() > 0)
													{
														mRate = distCommon.pickRate(lsPriceList, ldtDateStr, browItemCode, "", "L", quantity, conn);
														System.out.print("#### mRate gbfICquantity " + mRate);
														System.out.print("#### mqty " + quantity);
													}
													
												}
												value =quantity * mRate;
												prvFreeQty =prvFreeQty + value;
											}
											System.out.println("#### prvFreeQty insdie V [" + prvFreeQty+ "]");
										
									}	
								}	
								
								System.out.println("##### valueAmount "+valueAmount+"#### unConfTotFreeQty "+unConfTotFreeQty+"#### prvFreeQty "+prvFreeQty);
								System.out.println("##### freeQty "+freeQty);
								
								if ((valueAmount +unConfTotFreeQty + prvFreeQty ) > freeQty)
								{
									errCode = "VTFREEQTY1";
									System.out.println("##### Entered free quantity is greater than sCHEME_BALANCE quantity");
									return errCode;
									
								}
									
					}
					else
					{
						errCode = "VTQTYSCBAL";
						System.out.println("#### Entered free quantity is greater than sCHEME_BALANCE quantity");
						return errCode;
					}
					
				}
				if ("I".equalsIgnoreCase(nature.trim()))
				{
					System.out.println("#### I ");
					sql = " SELECT BALANCE_FREE_QTY - USED_FREE_QTY  FROM SCHEME_BALANCE  WHERE  BALANCE_FREE_QTY - USED_FREE_QTY > 0 "
							+ " AND EFF_FROM <= ? AND VALID_UPTO >=? AND CUST_CODE = ?  AND ITEM_CODE= ?  ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setTimestamp(1, OrderDate);
						pstmt.setTimestamp(2, OrderDate);
						pstmt.setString(3,custCode);
						pstmt.setString(4,itemCode);
						rs1 = pstmt.executeQuery();

						if (rs1.next()) 
						{
							freeQty = rs1.getDouble(1);
						}
						rs1.close();
						rs1 = null;
						pstmt.close();
						pstmt = null;
						if(freeQty > 0)
						{
							sql = " select sum(case when nature ='I' then  ( (case when qty_1 is null then 0 else qty_1 end) + (case when qty_2 is null then 0 else qty_2 end)  + (case when qty_3 is null then 0 else qty_3 end)  +(case when qty_4 is null then 0 else qty_4 end)  +(case when qty_5 is null then 0 else qty_5 end)  + (case when qty_6 is null then 0 else qty_6 end) ) else 0 end) as unconfirmFreeQty " +								
											" from sordform a,sordformdet b	where a.TRAN_ID = b.TRAN_ID and a.site_code = ?	"
											+ " and a.cust_code = ? and a.TRAN_ID <> ? and a.order_date between ? and ?"
											+ "	and b.item_code = ?"
											+ " and (case when a.status is null then 'N' else trim(a.status) end )= 'N'	and b.nature in ('I')";

									pstmt1 = conn.prepareStatement(sql);
									pstmt1.setString(1, siteCode);
									pstmt1.setString(2, custCode);
									pstmt1.setString(3, tranId);
									pstmt1.setTimestamp(4, OrderDate);
									pstmt1.setTimestamp(5, OrderDate);
									pstmt1.setString(6, itemCode);
									rs1 = pstmt1.executeQuery();
									if (rs1.next())
									{
									
										unConfTotFreeQty = rs1.getDouble("unconfirmFreeQty");
										System.out.println("unConfTotFreeQty separte free [" + unConfTotFreeQty+"]");
										
									}
									pstmt1.close();
									rs1.close();
									pstmt1 = null;
									rs1 = null;
									
									Node currDetail1 = null;
									prvFreeQty = 0;
									int count = 0;
									currLineNo = checkNull(genericUtility.getColumnValue("line_no", dom));
									NodeList detailList1 = dom2.getElementsByTagName("Detail2");
									
									int noOfDetails = detailList1.getLength();
									
									for (int ctr1 = 0; ctr1 < noOfDetails; ctr1++) {

										currDetail1 = detailList1.item(ctr1);
										
										lineNo = checkNull(genericUtility.getColumnValueFromNode("line_no", currDetail1));
										nature = checkNull(genericUtility.getColumnValueFromNode("nature", currDetail1));
										browItemCode = checkNull(genericUtility.getColumnValueFromNode("item_code", currDetail1));
										quantity = checkDoubleNull(genericUtility.getColumnValueFromNode("totqty", currDetail1));
									
										System.out.println("rate1: ====" + rate1 + "]lineNo" + lineNo + "quantity"+quantity);
										
										System.out.println("currLineNo: ====" + currLineNo + "]lineNo" + lineNo + "");
									
										if (!currLineNo.trim().equalsIgnoreCase(lineNo.trim())) {
											System.out.println("Insideif00000forSCHEME_BALANCE ");
												if (nature.equals("I") && (browItemCode.equalsIgnoreCase(itemCode))) {
													prvFreeQty = prvFreeQty + quantity;

												}
												System.out.println("prvFreeQty insdie V[" + prvFreeQty+ "]");
											
										}	
									}	
									System.out.println("##### qtyTot "+qtyTot+"#### unConfTotFreeQty "+unConfTotFreeQty+"#### prvFreeQty "+prvFreeQty);
									System.out.println("##### freeQty "+freeQty);
									if ((qtyTot +unConfTotFreeQty + prvFreeQty ) > freeQty) {
										errCode = "VTFREEQTY1";
										//errList.add(errCode);
										//errFields.add(childNodeName.toLowerCase());
										////errString = getErrorString( childNodeName, errCode, userId ); 
											
										System.out.println(
												"#### Entered free quantity is greater than sCHEME_BALANCE quantity");
										//break;
										 return errCode;
									}
				        }
						else
						{
							errCode = "VTQTYSCBAL";
							System.out.println("#### Entered free quantity is greater than SCHEME_BALANCE quantity");
							return errCode;
						}
			 }
			
		}
		catch(Exception e) 
		{
			System.out.println("Exception in PlaceOrdWiz :: wfValData() : Nature -> ["+e+"]");
		}
		return "";
	
	}
	private String checkNull(String input)	
	{
		if (input == null)
		{
			input="";
		}
		return input;
	}
	private double checkDoubleNull(String str) {
		if (str == null || str.trim().length() == 0) {
			return 0.0;
		} else {
			return Double.parseDouble(str);
		}
	}
}
