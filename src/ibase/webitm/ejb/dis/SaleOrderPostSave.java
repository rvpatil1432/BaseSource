package ibase.webitm.ejb.dis;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.utility.TransIDGenerator;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import javax.ejb.Stateless;

@Stateless
public class SaleOrderPostSave extends ValidatorEJB implements SaleOrderPostSaveLocal,SaleOrderPostSaveRemote {
	E12GenericUtility genericUtility= new  E12GenericUtility();
	DistCommon distCommon = new DistCommon();//added by nandkumar gadkari on 11/02/19
	public String postSave(String xmlString,String tranId,String editFlag, String xtraParams,Connection conn) throws RemoteException,ITMException
	{

		System.out.println("------------ postSave method called-----------------SaleOrderPostSave : ");
		System.out.println("tranId111--->>["+tranId+"]");
		System.out.println("xml String--->>["+xmlString+"]");
		Document dom = null;
		String errString="";

		try
		{
			if (xmlString != null && xmlString.trim().length() > 0)
			{
				dom = parseString(xmlString);				
				errString = postSave(dom,tranId,xtraParams,conn);
			}

		}
		catch(Exception e)
		{
			System.out.println("Exception : SaleOrderPostSave.java : postSave : ==>\n"+e.getMessage());
			throw new ITMException(e);
		}		
		return errString;
	}	
	public String postSave(Document dom,String tranId,String xtraParams,Connection conn)
	{
		System.out.println("in SaleOrderPostSave postSave tran_id---->>["+tranId+"]");
		ResultSet rs=null,rs1=null;
		PreparedStatement pstmt=null,pstmt1=null;
		String sql="",sql1="",errorString="";
		double quantityStduom = 0,rateStduom=0,discount=0,taxAmt=0,netAmt=0,taxAmtH=0,totAmtH=0,
		ordAmtH=0,ordValueH=0,ordValue=0,totOrdValueH=0,
		billBackAmt=0,offInvAmt=0,netTotAmtDet=0,netTotAmtHdr=0,ordBillBackAmt=0,ordOffInvAmt=0,lineBillBackAmt=0,lineOffInvAmt=0;
		int count=0,lineNo=0;
		String freeBalOrd="",nature="";//added by nandkumar gadkari on 11/02/19
		double freeBalValue=0.0,totFreeBalValue=0.0;//added by nandkumar gadkari on 11/02/19
		try
		{
			//GenericUtility genericUtility = GenericUtility.getInstance();	
			ITMDBAccessEJB	itmDBAccessEJB = new ITMDBAccessEJB();
			tranId = genericUtility.getColumnValue("sale_order",dom);
			//added by nandkumar gadkari on 11/02/19------------------------start--------------------------------
			freeBalValue = checkDoubleNull(genericUtility.getColumnValue("free_bal_value",dom));
			freeBalOrd = distCommon.getDisparams( "999999", "FREE_BAL_ORD", conn );
			if(freeBalOrd==null || freeBalOrd.trim().length() ==0 || freeBalOrd.equalsIgnoreCase("NULLFOUND"))
			{
				freeBalOrd="N";
			}
			else
			{
				freeBalOrd=freeBalOrd.trim();
			}
			if("Y".equalsIgnoreCase(freeBalOrd) &&  freeBalValue == 0)
			{
				System.out.println("in schemeFreeQtyAdd postSave ----");
				errorString=  schemeFreeQtyAdd(dom,tranId,xtraParams,conn);
				errorString="";
				
			}
			if("P".equalsIgnoreCase(freeBalOrd) &&  freeBalValue == 0)
			{
				System.out.println("in PointBaseschemeFreeQtyAdd postSave ----");
				errorString=  pointBaseScheme(dom,tranId,xtraParams,conn);
				//errorString="";
				if(errorString!=null && errorString.trim().length() > 0)
				{
					return errorString;
				}
				
			}
			//added by nandkumar gadkari on 11/02/19------------------------end--------------------------------
			System.out.println("sale_order--->>["+tranId+"]");
			sql="select quantity__stduom,rate__stduom,discount,tax_amt,line_no,ord_value,"
				+ "billback_amt,offinv_amt, "		//VALLABH KADAM [20/JUL/15] find billback_amt,offinv_amt
				+ "nature " //added by nandkumar gadkari on 11/02/19
				+ "from sorddet where sale_order = ?";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs=pstmt.executeQuery();
			while(rs.next())
			{
				quantityStduom=rs.getDouble(1);
				rateStduom=rs.getDouble(2);
				discount=rs.getDouble(3);
				taxAmt=rs.getDouble(4);
				lineNo=rs.getInt(5);
				ordValue=rs.getDouble(6);
				billBackAmt=rs.getDouble(7); 	//VALLABH KADAM [20/JUL/15] find billback_amt
				offInvAmt=rs.getDouble(8);		//VALLABH KADAM [20/JUL/15] find offinv_amt
				nature =rs.getString(9);//added by nandkumar gadkari on 11/02/19
				System.out.println("quantityStduom-->["+quantityStduom+"]");
				System.out.println("rateStduom-->["+rateStduom+"]taxAmt-->["+taxAmt+"]");
				System.out.println("discount-->["+discount+"] lineNo-->["+lineNo+"]");

				netAmt=(quantityStduom*rateStduom)-((quantityStduom*rateStduom*discount)/100)+taxAmt;
				System.out.println("NetAmt--->>["+netAmt+"]");

				/**
				 * VALLABH KADAM [20/JUL/15]
				 * Calculate net_tot_amt
				 * for every detail
				 * */
				netTotAmtDet=netAmt-billBackAmt-offInvAmt;
				System.out.println("Nte Total Amt Det:- ["+netTotAmtDet+"]");

				sql1="update sorddet set net_amt = ?,net_tot_amt=? where sale_order = ? and line_no = ?";
				pstmt1=conn.prepareStatement(sql1);
				pstmt1.setDouble(1, netAmt);
				pstmt1.setDouble(2, netTotAmtDet); //VALLABH KADAM [20/JUL/15]set net_tot_amt for every detail
				pstmt1.setString(3, tranId);
				pstmt1.setInt(4, lineNo);
				pstmt1.executeUpdate();
				if (pstmt1 != null){
					pstmt1.close();
					pstmt1=null;
				}

				taxAmtH=taxAmtH + taxAmt;
				totAmtH=totAmtH + netAmt;

				totOrdValueH=totOrdValueH + ordValue;	
				//condition added by nandkuar gadkari on 11/02/19------------start-------------
				if("I".equalsIgnoreCase(nature) || "V".equalsIgnoreCase(nature) || "P".equalsIgnoreCase(nature))// P TYPE ADDED BY NANDKUMAR GADKARI 
				{
					totFreeBalValue= totFreeBalValue + ordValue;
				}
				//condition added by nandkuar gadkari on 11/02/19------------end -------------
			} //end while
			if (pstmt != null)
			{
				pstmt.close();
				pstmt=null;
			}
			if (rs !=null)
			{
				rs.close();
				rs=null;
			}
			ordAmtH=totAmtH - taxAmtH;
			System.out.println("pre count---->>["+count+"]");
			System.out.println("taxAmtH-->["+taxAmtH+"] totAmtH-->["+totAmtH+"]");
			System.out.println("ordAmtH-->["+ordAmtH+"] totOrdValueH-->["+totOrdValueH+"]");
			System.out.println("totFreeBalValue---->>["+totFreeBalValue+"]");
			/**
			 * VALLABH KADAM [20/JUL/15]
			 * find ord_billback_amt, ord_offinv_amt, line_billback_amt, line_offinv_amt
			 * to calculate net_tot_amt for header
			 * */
			sql1="SELECT ORD_BILLBACK_AMT,ORD_OFFINV_AMT, LINE_BILLBACK_AMT,LINE_OFFINV_AMT FROM SORDER WHERE SALE_ORDER=?";
			pstmt1=conn.prepareStatement(sql1);
			pstmt1.setString(1, tranId);
			rs1=pstmt1.executeQuery();
			if(rs1.next())
			{
				ordBillBackAmt=rs1.getDouble("ORD_BILLBACK_AMT");
				ordOffInvAmt=rs1.getDouble("ORD_OFFINV_AMT");
				lineBillBackAmt=rs1.getDouble("LINE_BILLBACK_AMT");
				lineOffInvAmt=rs1.getDouble("LINE_OFFINV_AMT");
			}
			pstmt1.close();
			pstmt1=null;
			rs1.close();
			rs1=null;

			netTotAmtHdr=totAmtH-ordBillBackAmt-ordOffInvAmt-lineBillBackAmt-lineOffInvAmt;
			System.out.println("Net Tot Amt Hdr :- ["+netTotAmtHdr+"]");


			sql="update sorder set tax_amt = ? ,tot_amt= ? ,ord_amt = ?,tot_ord_value= ?,net_tot_amt=? ,free_bal_value= ? where sale_order = ?";// free_bal_value column added by nandkumar gadkari 
			pstmt=conn.prepareStatement(sql);
			pstmt.setDouble(1, taxAmtH);
			pstmt.setDouble(2, totAmtH);
			pstmt.setDouble(3, ordAmtH);
			pstmt.setDouble(4, totOrdValueH);	
			pstmt.setDouble(5, netTotAmtHdr);		// VALLABH KADAM [20/JUL/15] net_tot_amt for header	
			pstmt.setDouble(6, totFreeBalValue);// free_bal_value- nandkumar gadkari
			pstmt.setString(7, tranId);
			count=pstmt.executeUpdate();
			System.out.println("post count---->>["+count+"]");
			//Manoj dtd 18/03/2016 Removed Commit and Rollback condition
			if(count==0)
			{
				errorString=itmDBAccessEJB.getErrorString("","VTNORUPDT","","",conn);
				System.out.println("errorString :"+errorString);
			}
			// Changed by Manish for Maximum open cursor on 29/03/16 [start]
			if(pstmt != null)
			{
				pstmt.close();
				pstmt = null;
			}
			// Changed by Manish for Maximum open cursor on 29/03/16 [end]		
		}
		catch(Exception e)
		{
			System.out.println("Exception : SaleOrderPostSave -->["+e.getMessage()+"]");
			e.printStackTrace();
			try
			{
				conn.rollback();
			}
			catch(Exception e1)
			{
				System.out.println("Exception while rollbacking transaction....");
				e1.printStackTrace();
			}
		}
		// Changed by Manish for Maximum open cursor on 29/03/16 [start]
		finally
		{
			try
			{
				if (pstmt != null)
				{
					pstmt.close();
					pstmt=null;
				}
				if (rs !=null)
				{
					rs.close();
					rs=null;
				}
				if (pstmt1 != null)
				{
					pstmt1.close();
					pstmt1=null;
				}
				if (rs1 !=null)
				{
					rs1.close();
					rs1=null;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		// Changed by Manish for Maximum open cursor on 29/03/16 [end]
		return errorString;
	}
	//added by nandkumar gadkari on 11/02/19------------------------start--------------------------------
	private String schemeFreeQtyAdd(Document dom,String tranId,String xtraParams,Connection conn) throws ITMException 
	{
		ResultSet rs=null,rs1=null,rs2=null;
		PreparedStatement pstmt=null,pstmt1=null,pstmt2=null;
		String sql="",sql1="",errorString="";
		double quantityStduom = 0,rateStduom=0,discount=0,taxAmt=0,netAmt=0,taxAmtH=0,totAmtH=0,
		ordAmtH=0,ordValueH=0,ordValue=0,totOrdValueH=0,
		billBackAmt=0,offInvAmt=0,netTotAmtDet=0,netTotAmtHdr=0,ordBillBackAmt=0,ordOffInvAmt=0,lineBillBackAmt=0,totOrdValue=0,freeOrdvalue=0;
		int count=0,lineNo=0;
		String freeBalOrd="",freeBalOrdPerStr="",itemCodeOrd="",custCode="",siteCode="",priceList="",lsListType="",unit="",lsRefNo="",orderDateStr="";//added by nandkumar gadkari on 06/02/19
		double freeBalOrdPer=0.0,freeBalValue=0.0,schemeBalFreeQty=0.0,unConfTotFreeQty=0.0,freeValue=0.0,freeQtyToadd=0.0,convRtuomStduom=0.0,convQtyStduom=0.0,rateClg=0.0;//added by nandkumar gadkari on 06/02/19
		Timestamp orderDate=null,currDate = null;
		int llPlcount=0,minShelfLife=0,lineNos=0,cnt=0;
		String itemFlag="",lineNoSord="",chgUser="",chgTerm="",taxChap="",taxEnv="",taxClass="",unitRate="",itemDesc="",status="",unitStd="",packCode="",itemCode="";
		String itemSer="",itemSerProm="",locType="",holdflag="",nature="",unitSal="",mType="";
		String custCodeEnd="";   //added by manish mhatre on 08-nov-2019
		boolean schemeUsedInDetail=false;
		try {
			chgUser = (genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode"));		
			chgTerm = (genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId"));
			currDate = getCurrtDate();
			SimpleDateFormat sdfAppl = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String currDateStr = sdfAppl.format(currDate);
			currDate = getCurrtDate();
			custCode = genericUtility.getColumnValue("cust_code",dom);
			orderDate = Timestamp.valueOf(
					genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom),
							genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
							+ " 00:00:00.0");
			orderDateStr = genericUtility.getColumnValue("order_date", dom);
			siteCode = (genericUtility.getColumnValue("site_code", dom));
			
			//added by manish mhatre on 8-nov-2019
			//start manish
			sql="select cust_code__end from sorder where sale_order = ? ";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				custCodeEnd=checkNull((rs.getString(1)));
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt= null;
			//end manish
			
			sql="select item_code__ord,ord_value,unit,quantity__stduom,ITEM_FLG,TAX_CLASS,TAX_CHAP,TAX_ENV,STATUS,ITEM_DESCR,UNIT__RATE,"
					+ "CONV__QTY_STDUOM,CONV__RTUOM_STDUOM,UNIT__STD,PACK_CODE,ITEM_CODE,MIN_SHELF_LIFE,ITEM_SER,RATE__CLG,ITEM_SER__PROM,LOC_TYPE,HOLD_FLAG,"
					+ "NATURE " 
					+ "from sorddet where sale_order = ?";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs=pstmt.executeQuery();
				while(rs.next())
				{
					schemeBalFreeQty=0.0;
					itemCodeOrd=rs.getString(1);
					ordValue=rs.getDouble(2);
					quantityStduom=rs.getDouble(4);
					itemFlag=rs.getString(5);
					taxChap=rs.getString("TAX_CHAP");
					taxEnv=rs.getString("TAX_ENV");
					taxClass=rs.getString("TAX_CLASS");
					status=rs.getString("STATUS");
					convQtyStduom=rs.getDouble("CONV__QTY_STDUOM");
					convRtuomStduom=rs.getDouble("CONV__RTUOM_STDUOM");
					minShelfLife=rs.getInt("MIN_SHELF_LIFE");
					rateClg=rs.getDouble("RATE__CLG");
					holdflag=rs.getString("HOLD_FLAG");
					nature=rs.getString("NATURE");
					System.out.println("itemCodeOrd-->["+itemCodeOrd+"]");
					if("I".equalsIgnoreCase(nature) || "V".equalsIgnoreCase(nature))
					{
						schemeUsedInDetail=true;
					}
					totOrdValue= totOrdValue + ordValue;
				}
				if (rs !=null)
				{
					rs.close();
					rs=null;
				}	
				if (pstmt != null)
				{
					pstmt.close();
					pstmt=null;
				}
				
				
				if(!schemeUsedInDetail)
				{
				
					priceList = (distCommon.getDisparams( "999999", "MRP", conn ));
					if(priceList ==null || priceList.trim().length() ==0 || priceList.equalsIgnoreCase("NULLFOUND"))
					{
						priceList = (genericUtility.getColumnValue("price_list", dom));
					}
					lsListType = distCommon.getPriceListType(priceList, conn);
					freeBalOrdPerStr = distCommon.getDisparams( "999999", "FREE_BAL_ORD_PER", conn );
						if(freeBalOrdPerStr==null || freeBalOrdPerStr.trim().length() ==0 || freeBalOrdPerStr.equalsIgnoreCase("NULLFOUND"))
						{
								freeBalOrdPer=50.0;
						}
						else
						{
								freeBalOrdPer=Double.parseDouble(freeBalOrdPerStr);
						}
						// caluclation of quantity to be add
						freeValue= (totOrdValue * freeBalOrdPer)/100;
					
						//freeQtyToadd=freeValue/rateStduom;
						System.out.println("freeValue  " + freeValue);
						System.out.println("totOrdValue" + totOrdValue);
						System.out.println("freeBalOrdPer  " + freeBalOrdPer);
						System.out.println("totOrdValue  " + totOrdValue);
						sql = " SELECT BALANCE_FREE_QTY - USED_FREE_QTY ,ITEM_CODE FROM SCHEME_BALANCE  WHERE  BALANCE_FREE_QTY - USED_FREE_QTY > 0 "
									+ " AND EFF_FROM <= ? AND VALID_UPTO >=? AND CUST_CODE = ?  ";
							pstmt2 = conn.prepareStatement(sql);
							pstmt2.setTimestamp(1, orderDate);
							pstmt2.setTimestamp(2, orderDate);
							pstmt2.setString(3,custCode);
							//pstmt2.setString(4,itemCodeOrd);
							rs2 = pstmt2.executeQuery();
			
							while (rs2.next()) 
							{
								schemeBalFreeQty = rs2.getDouble(1);
								itemCodeOrd = rs2.getString(2);
								
								if(schemeBalFreeQty > 0 && freeValue > 0)
								{
									
									sql = " select sum(case when nature ='I' then quantity else 0 end) as unconfirmFreeQty " +								
													" from sorder a,sorddet b	where a.sale_order = b.sale_order and a.site_code = ?	"
													+ " and a.cust_code = ? and a.order_date between ? and ?"
													+ "	and b.item_code__ord = ?"
													+ " and (case when a.confirmed is null then 'N' else a.confirmed end )= 'N'	and b.nature in ('I')";
			
											pstmt1 = conn.prepareStatement(sql);
											pstmt1.setString(1, siteCode);
											pstmt1.setString(2, custCode);
											pstmt1.setTimestamp(3, orderDate);
											pstmt1.setTimestamp(4, orderDate);
											pstmt1.setString(5, itemCodeOrd);
											rs1 = pstmt1.executeQuery();
											if (rs1.next()) {
											
												unConfTotFreeQty = rs1.getDouble("unconfirmFreeQty");
												System.out.println("unConfTotFreeQty separte free" + unConfTotFreeQty);
												
											}
											
											rs1.close();
											rs1 = null;
											pstmt1.close();
											pstmt1 = null;
											
											
											if ((unConfTotFreeQty) < schemeBalFreeQty) 
											{
												schemeBalFreeQty=schemeBalFreeQty-unConfTotFreeQty;
												
												sql="select descr , loc_type ,pack_code,unit,unit__rate,unit__sal,item_stru from item where  item_code =?";
												pstmt=conn.prepareStatement(sql);
												pstmt.setString(1, itemCodeOrd);
												rs=pstmt.executeQuery();
												if(rs.next())
												{
													itemDesc = rs.getString("descr");
													locType = rs.getString("loc_type");
													packCode = rs.getString("pack_code");
													unit = rs.getString("unit");
													unitRate = rs.getString("unit__rate");
													unitSal = rs.getString("unit__sal");
													mType = rs.getString("item_stru");
												}
												if (rs !=null)
												{
													rs.close();
													rs=null;
												}
												if (pstmt != null)
												{
													pstmt.close();
													pstmt=null;
												}
												if (unitSal == null || unitSal.trim().length() == 0) {
													unitSal = unit;
												}
												
												sql = "select count(1)  as llPlcount from pricelist where price_list=?"
														+ " and item_code= ? and unit= ? and list_type=? and eff_from<=? and valid_upto  >=? and min_qty<=? and max_qty>= ?"
														+ " and (ref_no is not null)";
												pstmt1 = conn.prepareStatement(sql);
												pstmt1.setString(1, priceList);
												pstmt1.setString(2, itemCodeOrd);
												pstmt1.setString(3, unitSal);
												pstmt1.setString(4, lsListType);
												pstmt1.setTimestamp(5, orderDate);
												pstmt1.setTimestamp(6, orderDate);
												pstmt1.setDouble(7, quantityStduom);
												pstmt1.setDouble(8, quantityStduom);
												rs1 = pstmt1.executeQuery();
												if (rs1.next()) {
													llPlcount = rs1.getInt("llPlcount");
												}
												rs1.close();
												rs1 = null;
												pstmt1.close();
												pstmt1 = null;
								
												if (llPlcount >= 1) {
													sql = "select max(ref_no)from pricelist where price_list  =? and item_code= ? and unit=? and list_type= ?"
															+ " and eff_from<=? and valid_upto>=? and min_qty<=? and max_qty>=? and (ref_no is not null)";
													pstmt1 = conn.prepareStatement(sql);
													pstmt1.setString(1, priceList);
													pstmt1.setString(2, itemCodeOrd);
													pstmt1.setString(3, unitSal);
													pstmt1.setString(4, lsListType);
													pstmt1.setTimestamp(5, orderDate);
													pstmt1.setTimestamp(6, orderDate);
													pstmt1.setDouble(7, quantityStduom);
													pstmt1.setDouble(8, quantityStduom);
													rs1 = pstmt1.executeQuery();
													if (rs1.next()) {
														lsRefNo = rs1.getString(1);
													}
													rs1.close();
													rs1 = null;
													pstmt1.close();
													pstmt1 = null;
								
													rateStduom = distCommon.pickRateRefnoWise(priceList, orderDateStr, itemCodeOrd, lsRefNo, lsListType, quantityStduom,
															conn);
												}
												if (rateStduom <= 0) {
													rateStduom = distCommon.pickRate(priceList, orderDateStr, itemCodeOrd, "", "L", quantityStduom, conn);
												}
												if (rateStduom > 0)
												{
													// caluclation of quantity to be add
													freeOrdvalue =	schemeBalFreeQty * rateStduom;
													
													//
													System.out.println("freeOrdvalue  " + freeOrdvalue);
													System.out.println("schemeBalFreeQty" + schemeBalFreeQty);
													System.out.println("rateStduom  " + rateStduom);	
													System.out.println("freeValue Remaning  " + freeValue);
													
													sql="select trim(max(cast(line_no as number))) from  sorddet where sale_order = ?";
														pstmt=conn.prepareStatement(sql);
														pstmt.setString(1, tranId);
														rs=pstmt.executeQuery();
														if(rs.next())
														{
															lineNos=rs.getInt(1);
														}
														if (rs !=null)
														{
															rs.close();
															rs=null;
														}
														if (pstmt != null)
														{
															pstmt.close();
															pstmt=null;
														}
														
														lineNos ++;
														lineNoSord= Integer.toString(lineNos);
														
														
														if ("F".equalsIgnoreCase(mType)) {
															itemFlag="B";
														} else {
															itemFlag="I";
														}
														itemSer = distCommon.getItemSer(itemCodeOrd, siteCode, orderDate, custCode, "C", conn);
														
													
														itemSerProm = distCommon.getItemSer(itemCodeOrd, siteCode, orderDate, custCode, "O",
																	conn);
														

														sql = "select count(*) as cnt from customer_series where cust_code = ? and item_ser =?";
														pstmt = conn.prepareStatement(sql);
														pstmt.setString(1, custCode);
														pstmt.setString(2, itemSerProm);
														rs = pstmt.executeQuery();
														if (rs.next()) {
															cnt = rs.getInt("cnt");
														}
														rs.close();
														rs = null;
														pstmt.close();
														pstmt = null;
														System.out.println("cnt=========[" + cnt + "]");
														if (cnt == 0) {
															sql = "select item_ser from item_credit_perc where item_code = ?"
																	+ " and item_ser in ( select item_ser from customer_series where cust_code = ?"
																	+ " and item_ser  = item_credit_perc.item_ser)";
															pstmt = conn.prepareStatement(sql);
															pstmt.setString(1, itemCodeOrd);
															pstmt.setString(2, custCode);
															rs = pstmt.executeQuery();
															if (rs.next()) {
																itemSerProm = rs.getString("item_ser");
															}
															rs.close();
															rs = null;
															pstmt.close();
															pstmt = null;

															if (itemSerProm != null && itemSerProm.trim().length() > 0) {
																
																sql = "select item_ser__inv from customer_series where cust_code = ? and item_ser =?";
																pstmt = conn.prepareStatement(sql);
																pstmt.setString(1, custCode);
																pstmt.setString(2, itemSerProm);
																rs = pstmt.executeQuery();
																if (rs.next()) {
																	itemSer = rs.getString("item_ser__inv");
																}
																rs.close();
																rs = null;
																pstmt.close();
																pstmt = null;
																
															}
														}
														sql="insert into SORDDET (SALE_ORDER,LINE_NO,SITE_CODE,ITEM_FLG,QUANTITY,UNIT,DSP_DATE,RATE,DISCOUNT,TAX_AMT,TAX_CLASS,"//11
																+ "TAX_CHAP,TAX_ENV,NET_AMT,STATUS,CHG_DATE,CHG_USER,CHG_TERM,ITEM_DESCR,UNIT__RATE,CONV__QTY_STDUOM,"//21
																+ "CONV__RTUOM_STDUOM,UNIT__STD,QUANTITY__STDUOM,RATE__STDUOM,NO_ART,PACK_CODE,ITEM_CODE,MIN_SHELF_LIFE,ITEM_SER,"//30
																+ "RATE__CLG,ORD_VALUE,ITEM_SER__PROM,"//33
																+ "ITEM_CODE__ORD,OVER_SHIP_PERC,COMM_PERC_1,COMM_PERC_2,COMM_PERC_3,"//38
																+ "SALES_PERS_COMM_1,SALES_PERS_COMM_2,SALES_PERS_COMM_3,RATE__STD,"//42
																+ "LOC_TYPE,QUANTITY__FC,NATURE,"//45
																+ "BILLBACK_AMT,OFFINV_AMT,HOLD_FLAG,NET_TOT_AMT,SCH_ATTR,CUST_CODE__END) " //50  //cust code end added by manish mhatre on 08-nov-2019
																+ "values(?,?,?,?,?,?,?,?,?,?,"
																+ "?,?,?,?,?,?,?,?,?,?,"
																+"?,?,?,?,?,?,?,?,?,?,"
																+"?,?,?,?,?,?,?,?,?,?,"
																+"?,?,?,?,?,?,?,?,?,?,?)"; //50
														pstmt1 = conn.prepareStatement(sql);
														pstmt1.setString(1, tranId);
														lineNoSord = "   " + lineNoSord;
														lineNoSord = lineNoSord.substring(lineNoSord.length()-3);
														pstmt1.setString(2, lineNoSord);
														pstmt1.setString(3, siteCode);
														pstmt1.setString(4, itemFlag);
														//pstmt1.setDouble(5, schemeBalFreeQty);
														pstmt1.setString(6, unitSal);
														pstmt1.setTimestamp(7, currDate); //dsp_date
														pstmt1.setDouble(8, 0);
														pstmt1.setDouble(9, 0);
														pstmt1.setDouble(10, 0);
														pstmt1.setString(11, taxClass);
														pstmt1.setString(12, taxChap);
														pstmt1.setString(13, taxEnv);
														pstmt1.setDouble(14, 0);
														pstmt1.setString(15, status);
														pstmt1.setTimestamp(16, currDate); //chgDate
														pstmt1.setString(17, chgUser);
														pstmt1.setString(18, chgTerm); 
														pstmt1.setString(19, itemDesc);
														pstmt1.setString(20, unitRate);
						
														pstmt1.setDouble(21, convQtyStduom);
														pstmt1.setDouble(22, convRtuomStduom);
														pstmt1.setString(23, unit);
														//pstmt1.setDouble(24, schemeBalFreeQty);
		
														pstmt1.setDouble(25, 0);
		
														pstmt1.setDouble(26,0);//NO_ART
														pstmt1.setString(27,packCode);
														pstmt1.setString(28,itemCodeOrd);//
														pstmt1.setInt(29,minShelfLife);//
														pstmt1.setString(30,itemSer);//
														pstmt1.setDouble(31,rateClg);//
		
													//	ordValue=schemeBalFreeQty * rateStduom;
													//	System.out.println("ordValue--->>"+ordValue +" ("+schemeBalFreeQty +" * "+rateStduom+")");									
													//	pstmt1.setDouble(32,ordValue);//ord_value
														pstmt1.setString(33,itemSerProm);
														pstmt1.setString(34,itemCodeOrd);
														pstmt1.setDouble(35,0);
														pstmt1.setDouble(36,0);//
														pstmt1.setDouble(37,0);
														pstmt1.setDouble(38,0);
														pstmt1.setDouble(39,0);
														pstmt1.setDouble(40,0);
														pstmt1.setDouble(41,0);
														pstmt1.setDouble(42,0);//RATE__STD
														pstmt1.setString(43,locType);
													//	pstmt1.setDouble(44,schemeBalFreeQty);
		
														pstmt1.setString(45,"I");
														pstmt1.setDouble(46,0);
		
														pstmt1.setDouble(47,0);
														pstmt1.setString(48,holdflag);
														pstmt1.setDouble(49,0);
														pstmt1.setString(50,"N");	
														pstmt1.setString(51,custCodeEnd);  //added by manish mhatre on 08-nov-2019
														
														if(freeOrdvalue < freeValue)
														{
															pstmt1.setDouble(5, schemeBalFreeQty);
															pstmt1.setDouble(24, schemeBalFreeQty);
															ordValue=schemeBalFreeQty * rateStduom;
															System.out.println("ordValue--->>"+ordValue +" ("+schemeBalFreeQty +" * "+rateStduom+")");									
															pstmt1.setDouble(32,ordValue);//ord_value
															pstmt1.setDouble(44,schemeBalFreeQty);
														}
														else
														{
															freeQtyToadd=freeValue/rateStduom;// if freeValue is less than  freeOrdvalue
															System.out.println("freeQtyToadd befor" + freeQtyToadd);
															freeQtyToadd=integralPartQty(freeQtyToadd);
															System.out.println("freeQtyToadd after" + freeQtyToadd);
															pstmt1.setDouble(5, freeQtyToadd);
															pstmt1.setDouble(24, freeQtyToadd);
															ordValue=freeQtyToadd * rateStduom;
															System.out.println("ordValue--->>"+ordValue +" ("+freeQtyToadd +" * "+rateStduom+")");									
															pstmt1.setDouble(32,ordValue);//ord_value
															pstmt1.setDouble(44,freeQtyToadd);
														}
														
														
														
														int rows=pstmt1.executeUpdate();
														System.out.println("rows in detail ----["+rows+"]");
														if(pstmt1 != null)
														{
															pstmt1.close();
															pstmt1 = null;
														}
														if(rows > 0)
														{
															errorString="Success";
															freeValue= freeValue - freeOrdvalue;
															
														}			
														
													
												}
											}
											
						        }
								
							}
							rs2.close();
							rs2 = null;
							pstmt2.close();
							pstmt2 = null;
				}	
					
				
			
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new ITMException(ex);
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
				throw new ITMException(e);
			}
		}
		return errorString;
	}
	//added by nandkumar gadkari on 29/05/19-------------------start-------------------- 
	private String pointBaseScheme(Document dom,String tranId,String xtraParams,Connection conn) throws ITMException 
	{
		ResultSet rs=null,rs1=null,rs2=null;
		PreparedStatement pstmt=null,pstmt1=null,pstmt2=null,pStmt=null;
		String sql="",sql1="",errorString="";
		double quantityStduom = 0,rateStduom=0,discount=0,taxAmt=0,netAmt=0,taxAmtH=0,totAmtH=0,ordValue=0;
		int count=0,lineNo=0;
		String freeBalOrd="",freeBalOrdPerStr="",itemCodeOrd="",custCode="",siteCode="",priceList="",lsListType="",unit="",lsRefNo="",orderDateStr="";//added by nandkumar gadkari on 06/02/19
		double freeBalOrdPer=0.0,freeBalValue=0.0,schemeBalFreeQty=0.0,unConfTotFreeQty=0.0,freeValue=0.0,freeQtyToadd=0.0,convRtuomStduom=0.0,convQtyStduom=0.0,rateClg=0.0;//added by nandkumar gadkari on 06/02/19
		Timestamp orderDate=null,currDate = null,validUpto=null,appFrom=null;
		int llPlcount=0,minShelfLife=0,lineNos=0,cnt=0,updatedCount=0,schCnt=0;
		String itemFlag="",lineNoSord="",chgUser="",chgTerm="",taxChap="",taxEnv="",taxClass="",unitRate="",itemDesc="",status="",unitStd="",packCode="",itemCode="";
		String itemSer="",itemSerProm="",locType="",holdflag="",nature="",unitSal="",mType="";
		String schemeCode="",countCodeDlv="",stateCodeDlv="",schemeCode1="",SordlineNo="",schemeStkChk="";
		String custCodeEnd="";   //added by manish mhatre on 08-nov-2019
		double offerPoints=0,totalpoints=0,freePoints=0,reqPoints=0,availQty=0,unConfFreeQty=0,unconfreqPoints=0,unConfTotFreePoints=0,quantity=0;
		boolean schemeUsedInDetail=false;
		try {
			chgUser = (genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode"));		
			chgTerm = (genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId"));
			currDate = getCurrtDate();
			SimpleDateFormat sdfAppl = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String currDateStr = sdfAppl.format(currDate);
			currDate = getCurrtDate();
			custCode = genericUtility.getColumnValue("cust_code",dom);
			orderDate = Timestamp.valueOf(
					genericUtility.getValidDateString(genericUtility.getColumnValue("order_date", dom),
							genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat())
							+ " 00:00:00.0");
			orderDateStr = genericUtility.getColumnValue("order_date", dom);
			siteCode = (genericUtility.getColumnValue("site_code", dom));
			countCodeDlv = (genericUtility.getColumnValue("count_code__dlv", dom));
			stateCodeDlv = (genericUtility.getColumnValue("state_code__dlv", dom));
			priceList = (genericUtility.getColumnValue("price_list", dom));
			
			lsListType = distCommon.getPriceListType(priceList, conn);
			//added by manish mhatre on 8-nov-2019
			//start manish
			sql="select cust_code__end from sorder where sale_order = ? ";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs=pstmt.executeQuery();
			while(rs.next())
			{
				custCodeEnd=checkNull(rs.getString(1));
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt= null;
			//end manish
			sql="select item_code__ord,ord_value,unit,quantity__stduom,ITEM_FLG,TAX_CLASS,TAX_CHAP,TAX_ENV,STATUS,ITEM_DESCR,UNIT__RATE,"
					+ "CONV__QTY_STDUOM,CONV__RTUOM_STDUOM,UNIT__STD,PACK_CODE,ITEM_CODE,MIN_SHELF_LIFE,ITEM_SER,RATE__CLG,ITEM_SER__PROM,LOC_TYPE,HOLD_FLAG,"
					+ "NATURE,LINE_NO  " 
					+ "from sorddet where sale_order = ?";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs=pstmt.executeQuery();
				while(rs.next())
				{
					schemeBalFreeQty=0.0;
					itemCodeOrd=rs.getString(1);
					ordValue=rs.getDouble(2);
					quantityStduom=rs.getDouble(4);
					itemFlag=rs.getString(5);
					taxChap=rs.getString("TAX_CHAP");
					taxEnv=rs.getString("TAX_ENV");
					taxClass=rs.getString("TAX_CLASS");
					status=rs.getString("STATUS");
					convQtyStduom=rs.getDouble("CONV__QTY_STDUOM");
					convRtuomStduom=rs.getDouble("CONV__RTUOM_STDUOM");
					minShelfLife=rs.getInt("MIN_SHELF_LIFE");
					rateClg=rs.getDouble("RATE__CLG");
					holdflag=rs.getString("HOLD_FLAG");
					nature=rs.getString("NATURE");
					SordlineNo=rs.getString("LINE_NO");
					System.out.println("itemCodeOrd-->["+itemCodeOrd+"]");
					if("P".equalsIgnoreCase(nature) )
					{
						schemeUsedInDetail=true;
						errorString=  pointSchemeValidation(custCode,orderDate,tranId,siteCode,stateCodeDlv,countCodeDlv,quantityStduom,itemCodeOrd,SordlineNo,conn);
						if(errorString !=null && errorString.trim().length()> 0)
						{
							return errorString;
						}
					}
					if("C".equalsIgnoreCase(nature) )
					{
						cnt= 0;
						sql = "select a.scheme_code from scheme_applicability a,scheme_applicability_det  b "
								+ " where a.scheme_code= b.scheme_code  and a.app_from <= ? and a.valid_upto>= ? "
								+ " and (b.site_code= ? or b.state_code = ?  or b.count_code= ?) and PROD_SCH = ?";
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setTimestamp(1, orderDate);
						pstmt1.setTimestamp(2, orderDate);
						pstmt1.setString(3, siteCode);
						pstmt1.setString(4, stateCodeDlv);
						pstmt1.setString(5, countCodeDlv);
						pstmt1.setString(6, "Y");
						rs1 = pstmt1.executeQuery();
						while (rs1.next()) {
							schemeCode1 = rs1.getString("scheme_code");
						
							if(schemeCode1 !=null && schemeCode1.trim().length() > 0)
							{
							sql = "select count (*) as cnt from SCH_PUR_ITEMS  where SCHEME_CODE =? and item_code=?";
									pstmt2 = conn.prepareStatement(sql);
									pstmt2.setString(1, schemeCode1);
									pstmt2.setString(2, itemCodeOrd);
									rs2 = pstmt2.executeQuery();
									if (rs2.next()) 
									{
										cnt = rs2.getInt("cnt");
									}
									rs2.close();
									rs2 = null;
									pstmt2.close();
									pstmt2 = null;
										
									if(cnt==0)
									{
										continue;
									}
									else {
										schCnt++;
										schemeCode=schemeCode1;
									}
																
									
							}		
									
							}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1= null;
						
						
					}
					
				}
				if (rs !=null)
				{
					rs.close();
					rs=null;
				}	
				if (pstmt != null)
				{
					pstmt.close();
					pstmt=null;
				}
				
				if(!schemeUsedInDetail)
				{
				
					if(schemeCode !=null && schemeCode.trim().length() > 0 && schCnt > 0)
					{
					
					sql = " SELECT BALANCE_FREE_VALUE - USED_FREE_VALUE  FROM SCHEME_BALANCE  WHERE  BALANCE_FREE_VALUE - USED_FREE_VALUE > 0 "
							+ "  AND CUST_CODE = ?  AND ITEM_CODE= ?  AND EFF_FROM <= ? AND VALID_UPTO >=? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,custCode);
						pstmt.setString(2,"X");
						pstmt.setTimestamp(3, orderDate);
						pstmt.setTimestamp(4, orderDate);
						rs = pstmt.executeQuery();

						if (rs.next()) 
						{
							freePoints = rs.getDouble(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if(freePoints > 0)
						{
							sql = " select b.item_code__ord ,b.quantity " +								
									" from sorder a,sorddet b	where a.sale_order = b.sale_order and a.site_code = ?	"
									+ " and a.cust_code = ?  and a.order_date between ? and ?"
									+ " and (case when a.confirmed is null then 'N' else a.confirmed end )= 'N'	and b.nature in ('P')";
	
							pstmt1 = conn.prepareStatement(sql);
							pstmt1.setString(1, siteCode);
							pstmt1.setString(2, custCode);
							pstmt1.setTimestamp(3, orderDate);
							pstmt1.setTimestamp(4, orderDate);
							
							rs1 = pstmt1.executeQuery();
						while (rs1.next()) {
							itemCodeOrd=rs1.getString(1);
								unConfFreeQty = rs1.getDouble(2);
								System.out.println("unConfFreeQty" + unConfFreeQty);
								sql = "select count (*) as cnt from SCH_OFFER_ITEMS  where SCHEME_CODE =? and item_code=?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, schemeCode);
								pstmt.setString(2, itemCodeOrd);
								rs = pstmt.executeQuery();
								if (rs.next()) 
								{
									cnt = rs.getInt("cnt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
									
								if(cnt > 0)
								{
									sql = "select required_points from SCH_OFFER_ITEMS  where SCHEME_CODE =? and item_code=?";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, schemeCode);
									pstmt.setString(2, itemCodeOrd);
									rs = pstmt.executeQuery();
									if (rs.next()) 
									{
										unconfreqPoints = rs.getDouble(1);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									
									unConfTotFreePoints = unConfTotFreePoints + integralPartQty(unConfFreeQty  * unconfreqPoints);
								
								}
								
							}
							pstmt1.close();
							rs1.close();
							pstmt1 = null;
							rs1 = null;
							freePoints= freePoints - unConfTotFreePoints;
							//added by nandkumar gadkari on 09/09/19
							schemeStkChk = distCommon.getDisparams( "999999", "SCHEME_STOCK_CHECK", conn );
							if(schemeStkChk==null || schemeStkChk.trim().length() ==0 || schemeStkChk.equalsIgnoreCase("NULLFOUND"))
							{
								schemeStkChk="Y";
							}
							
								sql = "select item_code, required_points from SCH_OFFER_ITEMS  where SCHEME_CODE =? ";
								pstmt2 = conn.prepareStatement(sql);
								pstmt2.setString(1, schemeCode);
								rs2 = pstmt2.executeQuery();
								while (rs2.next()) 
								{
									
									if(freePoints > 0)
									{
										
										itemCodeOrd = rs2.getString(1);
										reqPoints = rs2.getDouble(2);
										//if condition added by nandkumar gadkari on 09/09/19
										
										if("Y".equalsIgnoreCase(schemeStkChk.trim()))
										{	
											sql="SELECT SUM(a.QUANTITY - a.ALLOC_QTY - CASE WHEN a.HOLD_QTY IS NULL THEN 0 ELSE a.HOLD_QTY END ) AVAIL_QTY "
											+" FROM STOCK A, "
											+"LOCATION B, "
											+"INVSTAT C "
											+"WHERE A.LOC_CODE = B.LOC_CODE "
											+"AND B.INV_STAT = C.INV_STAT "
											+"AND A.ITEM_CODE = ? "
											+"AND A.SITE_CODE = ? "
											+"AND C.AVAILABLE = 'Y' "
											+"AND C.STAT_TYPE = 'M' ";
											pstmt = conn.prepareStatement(sql);
											pstmt.setString(1, itemCodeOrd);
											pstmt.setString(2, siteCode);
											
											rs = pstmt.executeQuery();
											if (rs.next()) 
											{
												availQty = rs.getDouble(1);
											}
											rs.close();
											rs = null;
											pstmt.close();
											pstmt = null;
											
											if(availQty <= 0)
											{
												continue;
											}
										}
										quantity=integralPartQty(freePoints/reqPoints); 
										if(quantity <= 0)
										{
											continue;
										}
				
										sql="select descr , loc_type ,pack_code,unit,unit__rate,unit__sal,item_stru from item where  item_code =?";
										pstmt=conn.prepareStatement(sql);
										pstmt.setString(1, itemCodeOrd);
										rs=pstmt.executeQuery();
										if(rs.next())
										{
											itemDesc = rs.getString("descr");
											locType = rs.getString("loc_type");
											packCode = rs.getString("pack_code");
											unit = rs.getString("unit");
											unitRate = rs.getString("unit__rate");
											unitSal = rs.getString("unit__sal");
											mType = rs.getString("item_stru");
										}
										if (rs !=null)
										{
											rs.close();
											rs=null;
										}
										if (pstmt != null)
										{
											pstmt.close();
											pstmt=null;
										}
										if (unitSal == null || unitSal.trim().length() == 0) {
											unitSal = unit;
										}
										
										sql = "select count(1)  as llPlcount from pricelist where price_list=?"
												+ " and item_code= ? and unit= ? and list_type=? and eff_from<=? and valid_upto  >=? and min_qty<=? and max_qty>= ?"
												+ " and (ref_no is not null)";
										pstmt1 = conn.prepareStatement(sql);
										pstmt1.setString(1, priceList);
										pstmt1.setString(2, itemCodeOrd);
										pstmt1.setString(3, unitSal);
										pstmt1.setString(4, lsListType);
										pstmt1.setTimestamp(5, orderDate);
										pstmt1.setTimestamp(6, orderDate);
										pstmt1.setDouble(7, quantityStduom);
										pstmt1.setDouble(8, quantityStduom);
										rs1 = pstmt1.executeQuery();
										if (rs1.next()) {
											llPlcount = rs1.getInt("llPlcount");
										}
										rs1.close();
										rs1 = null;
										pstmt1.close();
										pstmt1 = null;
						
										if (llPlcount >= 1) {
											sql = "select max(ref_no)from pricelist where price_list  =? and item_code= ? and unit=? and list_type= ?"
													+ " and eff_from<=? and valid_upto>=? and min_qty<=? and max_qty>=? and (ref_no is not null)";
											pstmt1 = conn.prepareStatement(sql);
											pstmt1.setString(1, priceList);
											pstmt1.setString(2, itemCodeOrd);
											pstmt1.setString(3, unitSal);
											pstmt1.setString(4, lsListType);
											pstmt1.setTimestamp(5, orderDate);
											pstmt1.setTimestamp(6, orderDate);
											pstmt1.setDouble(7, quantityStduom);
											pstmt1.setDouble(8, quantityStduom);
											rs1 = pstmt1.executeQuery();
											if (rs1.next()) {
												lsRefNo = rs1.getString(1);
											}
											rs1.close();
											rs1 = null;
											pstmt1.close();
											pstmt1 = null;
						
											rateStduom = distCommon.pickRateRefnoWise(priceList, orderDateStr, itemCodeOrd, lsRefNo, lsListType, quantityStduom,
													conn);
										}
										if (rateStduom <= 0) {
											rateStduom = distCommon.pickRate(priceList, orderDateStr, itemCodeOrd, "", "L", quantityStduom, conn);
										}
										
											System.out.println("freePoints  " + freePoints);
											System.out.println("quantity" + quantity);
											System.out.println("rateStduom  " + rateStduom);	
											
											
											sql="select trim(max(cast(line_no as number))) from  sorddet where sale_order = ?";
												pstmt=conn.prepareStatement(sql);
												pstmt.setString(1, tranId);
												rs=pstmt.executeQuery();
												if(rs.next())
												{
													lineNos=rs.getInt(1);
												}
												if (rs !=null)
												{
													rs.close();
													rs=null;
												}
												if (pstmt != null)
												{
													pstmt.close();
													pstmt=null;
												}
												
												lineNos ++;
												lineNoSord= Integer.toString(lineNos);
												
												
												if ("F".equalsIgnoreCase(mType)) {
													itemFlag="B";
												} else {
													itemFlag="I";
												}
												itemSer = distCommon.getItemSer(itemCodeOrd, siteCode, orderDate, custCode, "C", conn);
												
											
												itemSerProm = distCommon.getItemSer(itemCodeOrd, siteCode, orderDate, custCode, "O",
															conn);
												

												sql = "select count(*) as cnt from customer_series where cust_code = ? and item_ser =?";
												pstmt = conn.prepareStatement(sql);
												pstmt.setString(1, custCode);
												pstmt.setString(2, itemSerProm);
												rs = pstmt.executeQuery();
												if (rs.next()) {
													cnt = rs.getInt("cnt");
												}
												rs.close();
												rs = null;
												pstmt.close();
												pstmt = null;
												System.out.println("cnt=========[" + cnt + "]");
												if (cnt == 0) {
													sql = "select item_ser from item_credit_perc where item_code = ?"
															+ " and item_ser in ( select item_ser from customer_series where cust_code = ?"
															+ " and item_ser  = item_credit_perc.item_ser)";
													pstmt = conn.prepareStatement(sql);
													pstmt.setString(1, itemCodeOrd);
													pstmt.setString(2, custCode);
													rs = pstmt.executeQuery();
													if (rs.next()) {
														itemSerProm = rs.getString("item_ser");
													}
													rs.close();
													rs = null;
													pstmt.close();
													pstmt = null;

													if (itemSerProm != null && itemSerProm.trim().length() > 0) {
														
														sql = "select item_ser__inv from customer_series where cust_code = ? and item_ser =?";
														pstmt = conn.prepareStatement(sql);
														pstmt.setString(1, custCode);
														pstmt.setString(2, itemSerProm);
														rs = pstmt.executeQuery();
														if (rs.next()) {
															itemSer = rs.getString("item_ser__inv");
														}
														rs.close();
														rs = null;
														pstmt.close();
														pstmt = null;
														
													}
												}
												sql="insert into SORDDET (SALE_ORDER,LINE_NO,SITE_CODE,ITEM_FLG,QUANTITY,UNIT,DSP_DATE,RATE,DISCOUNT,TAX_AMT,TAX_CLASS,"//11
														+ "TAX_CHAP,TAX_ENV,NET_AMT,STATUS,CHG_DATE,CHG_USER,CHG_TERM,ITEM_DESCR,UNIT__RATE,CONV__QTY_STDUOM,"//21
														+ "CONV__RTUOM_STDUOM,UNIT__STD,QUANTITY__STDUOM,RATE__STDUOM,NO_ART,PACK_CODE,ITEM_CODE,MIN_SHELF_LIFE,ITEM_SER,"//30
														+ "RATE__CLG,ORD_VALUE,ITEM_SER__PROM,"//33
														+ "ITEM_CODE__ORD,OVER_SHIP_PERC,COMM_PERC_1,COMM_PERC_2,COMM_PERC_3,"//38
														+ "SALES_PERS_COMM_1,SALES_PERS_COMM_2,SALES_PERS_COMM_3,RATE__STD,"//42
														+ "LOC_TYPE,QUANTITY__FC,NATURE,"//45
														+ "BILLBACK_AMT,OFFINV_AMT,HOLD_FLAG,NET_TOT_AMT,SCH_ATTR,CUST_CODE__END) " //50   //cust_code__end added by manish mhatre on 08-nov-2019
														+ "values(?,?,?,?,?,?,?,?,?,?,"
														+ "?,?,?,?,?,?,?,?,?,?,"
														+"?,?,?,?,?,?,?,?,?,?,"
														+"?,?,?,?,?,?,?,?,?,?,"
														+"?,?,?,?,?,?,?,?,?,?,?)"; //50
												pstmt1 = conn.prepareStatement(sql);
												pstmt1.setString(1, tranId);
												lineNoSord = "   " + lineNoSord;
												lineNoSord = lineNoSord.substring(lineNoSord.length()-3);
												pstmt1.setString(2, lineNoSord);
												pstmt1.setString(3, siteCode);
												pstmt1.setString(4, itemFlag);
												//pstmt1.setDouble(5, schemeBalFreeQty);
												pstmt1.setString(6, unitSal);
												pstmt1.setTimestamp(7, currDate); //dsp_date
												pstmt1.setDouble(8, 0);
												pstmt1.setDouble(9, 0);
												pstmt1.setDouble(10, 0);
												pstmt1.setString(11, taxClass);
												pstmt1.setString(12, taxChap);
												pstmt1.setString(13, taxEnv);
												pstmt1.setDouble(14, 0);
												pstmt1.setString(15, status);
												pstmt1.setTimestamp(16, currDate); //chgDate
												pstmt1.setString(17, chgUser);
												pstmt1.setString(18, chgTerm); 
												pstmt1.setString(19, itemDesc);
												pstmt1.setString(20, unitRate);
				
												pstmt1.setDouble(21, convQtyStduom);
												pstmt1.setDouble(22, convRtuomStduom);
												pstmt1.setString(23, unit);
												//pstmt1.setDouble(24, schemeBalFreeQty);

												pstmt1.setDouble(25, 0);

												pstmt1.setDouble(26,0);//NO_ART
												pstmt1.setString(27,packCode);
												pstmt1.setString(28,schemeCode);//
												pstmt1.setInt(29,minShelfLife);//
												pstmt1.setString(30,itemSer);//
												pstmt1.setDouble(31,rateClg);//

											//	ordValue=schemeBalFreeQty * rateStduom;
											//	System.out.println("ordValue--->>"+ordValue +" ("+schemeBalFreeQty +" * "+rateStduom+")");									
											//	pstmt1.setDouble(32,ordValue);//ord_value
												pstmt1.setString(33,itemSerProm);
												pstmt1.setString(34,itemCodeOrd);
												pstmt1.setDouble(35,0);
												pstmt1.setDouble(36,0);//
												pstmt1.setDouble(37,0);
												pstmt1.setDouble(38,0);
												pstmt1.setDouble(39,0);
												pstmt1.setDouble(40,0);
												pstmt1.setDouble(41,0);
												pstmt1.setDouble(42,0);//RATE__STD
												pstmt1.setString(43,locType);
											//	pstmt1.setDouble(44,schemeBalFreeQty);

												pstmt1.setString(45,"P");
												pstmt1.setDouble(46,0);

												pstmt1.setDouble(47,0);
												pstmt1.setString(48,holdflag);
												pstmt1.setDouble(49,0);
												pstmt1.setString(50,"N");	
												pstmt1.setString(51, custCodeEnd);      //added by manish mhatre on 08-nov-2019
												
												
												
												if("Y".equalsIgnoreCase(schemeStkChk.trim()))//if condition added by nandkumar gadkari on 09/09/19
												{
													if(quantity < availQty)
													{
														pstmt1.setDouble(5, quantity);
														pstmt1.setDouble(24, quantity);
														ordValue=quantity * rateStduom;
														System.out.println("ordValue--->>"+ordValue +" ("+quantity +" * "+rateStduom+")");									
														pstmt1.setDouble(32,ordValue);//ord_value
														pstmt1.setDouble(44,quantity);
													}
													else
													{
														
														pstmt1.setDouble(5, availQty);
														pstmt1.setDouble(24, availQty);
														ordValue=availQty * rateStduom;
														System.out.println("ordValue--->>"+ordValue +" ("+availQty +" * "+rateStduom+")");									
														pstmt1.setDouble(32,ordValue);//ord_value
														pstmt1.setDouble(44,availQty);
													}
												}
												else
												{
													pstmt1.setDouble(5, quantity);
													pstmt1.setDouble(24, quantity);
													ordValue=quantity * rateStduom;
													System.out.println("ordValue--->>"+ordValue +" ("+quantity +" * "+rateStduom+")");									
													pstmt1.setDouble(32,ordValue);//ord_value
													pstmt1.setDouble(44,quantity);
												}
												
												
												int rows=pstmt1.executeUpdate();
												System.out.println("rows in detail ----["+rows+"]");
												if(pstmt1 != null)
												{
													pstmt1.close();
													pstmt1 = null;
												}
												if(rows > 0)
												{
													/*errorString="Success";*/
													if("Y".equalsIgnoreCase(schemeStkChk.trim()))//if else condition added by nandkumar gadkari on 09/09/19
													{
														if(quantity < availQty)
														{
															
															freePoints=freePoints - (quantity * reqPoints);
														}
														else
														{
															freePoints=freePoints - (availQty * reqPoints);
														}
													}
													else
													{
														freePoints=freePoints - (quantity * reqPoints);
													}
													
												}			
											
									}
										
								}
								rs2.close();
								rs2 = null;
								pstmt2.close();
								pstmt2 = null;
						
						}
					
				}		
				}
						
					
			
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new ITMException(ex);
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
				throw new ITMException(e);
			}
		}
		return errorString;
	}
	private String pointSchemeValidation(String custCode,Timestamp orderDate,String saleOrder,String siteCode,String stateCodeDlv,String countCodeDlv,double quantityStduom,String itemCodeOrd,String currLineNo,Connection conn) throws ITMException 
	{
		int cnt1=0,cnt=0;
		double reqPoints=0,freePoints=0,unconfreqPoints=0,unConfFreeQty=0,unConfTotFreePoints=0,prvFreePoints=0,currentPoints=0,prvFreeQty=0,quantity=0;
		ResultSet rs=null,rs1=null,rs2=null;
		PreparedStatement pstmt=null,pstmt1=null,pstmt2=null,pStmt=null;
		String sql="",sql1="",errorString="",schemeCode="",schemeCode1="",itemCode="";
		String  lineNo = "", browItemCode = "",nature="";
		ITMDBAccessEJB	itmDBAccessEJB = new ITMDBAccessEJB();

		try {
			
				sql = "select a.scheme_code from scheme_applicability a,scheme_applicability_det  b where a.scheme_code= b.scheme_code  and a.app_from <= ? and a.valid_upto>= ? and (b.site_code= ? or b.state_code = ?  or b.count_code= ?) and PROD_SCH = ?";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setTimestamp(1, orderDate);
				pstmt1.setTimestamp(2, orderDate);
				pstmt1.setString(3, siteCode);
				pstmt1.setString(4, stateCodeDlv);
				pstmt1.setString(5, countCodeDlv);
				pstmt1.setString(6, "Y");
				rs1 = pstmt1.executeQuery();
				while (rs1.next()) {
					schemeCode1 = rs1.getString("scheme_code");
				
					if(schemeCode1 !=null && schemeCode1.trim().length() > 0)
					{
					sql = "select count (*) as cnt from SCH_OFFER_ITEMS  where SCHEME_CODE =? and item_code=?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, schemeCode1);
							pstmt.setString(2, itemCodeOrd);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
								
							if(cnt==0)
							{
								continue;
							}
							if (cnt > 1 )
							{
								
								errorString=itmDBAccessEJB.getErrorString("","VTITEM10","","",conn);
								return errorString;
							}
							if(cnt== 1)
							{
								cnt1++;
								schemeCode=schemeCode1;
							}
							
							
					}		
							
					}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1= null;
					if (cnt1 == 0) {
						errorString=itmDBAccessEJB.getErrorString("","VTINFEEQTY","","",conn);
						System.out.println("invalid free quantity for this item code ");
					}
					if(schemeCode !=null && schemeCode.trim().length() > 0)
					{
					sql = "select required_points from SCH_OFFER_ITEMS  where SCHEME_CODE =? and item_code=?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, schemeCode);
					pstmt.setString(2, itemCodeOrd);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						reqPoints = rs.getDouble(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					if(reqPoints > 0)
					{
						currentPoints = quantityStduom * reqPoints;
					}
					
					sql = " SELECT BALANCE_FREE_VALUE - USED_FREE_VALUE  FROM SCHEME_BALANCE  WHERE  BALANCE_FREE_VALUE - USED_FREE_VALUE > 0 "
							+ "  AND CUST_CODE = ?  AND ITEM_CODE= ?   AND EFF_FROM <= ? AND VALID_UPTO >=?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,custCode);
						pstmt.setString(2,"X");
						pstmt.setTimestamp(3, orderDate);
						pstmt.setTimestamp(4, orderDate);
						rs = pstmt.executeQuery();

						if (rs.next()) 
						{
							freePoints = rs.getDouble(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					if(freePoints > 0)
						{
								sql = " select b.item_code__ord ,b.quantity " +								
									" from sorder a,sorddet b	where a.sale_order = b.sale_order and a.site_code = ?	"
									+ " and a.cust_code = ? and a.sale_order <> ? and a.order_date between ? and ?"
									+ " and (case when a.confirmed is null then 'N' else a.confirmed end )= 'N'	and b.nature in ('P')";

							pstmt1 = conn.prepareStatement(sql);
							pstmt1.setString(1, siteCode);
							pstmt1.setString(2, custCode);
							pstmt1.setString(3, saleOrder);
							pstmt1.setTimestamp(4, orderDate);
							pstmt1.setTimestamp(5, orderDate);
							
							rs1 = pstmt1.executeQuery();
						while (rs1.next()) {
								itemCode=rs1.getString(1);
								unConfFreeQty = rs1.getDouble(2);
								System.out.println("unConfFreeQty" + unConfFreeQty);
								sql = "select count (*) as cnt from SCH_OFFER_ITEMS  where SCHEME_CODE =? and item_code=?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, schemeCode);
								pstmt.setString(2, itemCode);
								rs = pstmt.executeQuery();
								if (rs.next()) 
								{
									cnt = rs.getInt("cnt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
									
								if(cnt > 0)
								{
									sql = "select required_points from SCH_OFFER_ITEMS  where SCHEME_CODE =? and item_code=?";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, schemeCode);
									pstmt.setString(2, itemCode);
									rs = pstmt.executeQuery();
									if (rs.next()) 
									{
										unconfreqPoints = rs.getDouble(1);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									
								
										unConfTotFreePoints = unConfTotFreePoints + unConfFreeQty * unconfreqPoints;
									
								}
								
							}
							pstmt1.close();
							rs1.close();
							pstmt1 = null;
							rs1 = null;
							System.out.println("unConfTotFreePoints" + unConfTotFreePoints+ "]");
							//---------------------------
							
							prvFreeQty = 0;
							int count = 0;
							sql="select item_code__ord,quantity__stduom,NATURE,LINE_NO  " 
									+ "from sorddet where sale_order = ?";
							pstmt1 = conn.prepareStatement(sql);
							pstmt1.setString(1, saleOrder);
														
							rs1 = pstmt1.executeQuery();
						while (rs1.next()) {
							browItemCode=rs1.getString(1);
							quantity = rs1.getDouble(2);
							nature=rs1.getString(3);
							lineNo=rs1.getString(4);

							
							
							System.out.println("lineNo" + lineNo + "quantity"+quantity);
							
							System.out.println("currLineNo: ====" + currLineNo + "]lineNo" + lineNo + "");
						
							if (!currLineNo.trim().equalsIgnoreCase(lineNo.trim())) {
								System.out.println("Insideif00000forSCHEME_BALANCE ");
									if (nature.equals("P")) {
										
										sql = "select count (*) as cnt from SCH_OFFER_ITEMS  where SCHEME_CODE =? and item_code=?";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, schemeCode);
										pstmt.setString(2, browItemCode);
										rs = pstmt.executeQuery();
										if (rs.next()) 
										{
											cnt = rs.getInt("cnt");
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
											
										if(cnt > 0)
										{
											sql = "select required_points from SCH_OFFER_ITEMS  where SCHEME_CODE =? and item_code=?";
											pstmt = conn.prepareStatement(sql);
											pstmt.setString(1, schemeCode);
											pstmt.setString(2, browItemCode);
											rs = pstmt.executeQuery();
											if (rs.next()) 
											{
												unconfreqPoints = rs.getDouble(1);
											}
											rs.close();
											rs = null;
											pstmt.close();
											pstmt = null;
											
										
												prvFreePoints = prvFreePoints + quantity * unconfreqPoints;
											
										}
										

									}
									System.out.println("prvFreePoints insdie P[" + prvFreePoints+ "]");
								
							}	
								
							}
							pstmt1.close();
							rs1.close();
							pstmt1 = null;
							rs1 = null;
							
							
							if ((currentPoints +unConfTotFreePoints + prvFreePoints ) > freePoints) {
								
								errorString=itmDBAccessEJB.getErrorString("","VTFREEQTY1","","",conn);
								return errorString;
								
							}
						}	
					else
					{
						errorString=itmDBAccessEJB.getErrorString("","VTQTYSCBAL","","",conn);
						return errorString;
						
					}
					}
	} catch (Exception ex) {
		ex.printStackTrace();
		throw new ITMException(ex);
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
			throw new ITMException(e);
		}
	}
	return errorString;
	}
	private java.sql.Timestamp getCurrtDate() throws RemoteException,ITMException 
	{
		String currAppdate = "";
		java.sql.Timestamp currDate = null;
		try 
		{
			Object date = null;
			currDate = new java.sql.Timestamp(System.currentTimeMillis());
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(genericUtility.getDBDateFormat());
			date = sdf.parse(currDate.toString());
			currDate = java.sql.Timestamp.valueOf(sdf.format(date).toString()+ " 00:00:00.0");

		} 
		catch (Exception e) 
		{
			throw new ITMException(e);
		}
		return (currDate);
	}
	private double checkDoubleNull(String str) {
		if (str == null || str.trim().length() == 0) {
			return 0.0;
		} else {
			return Double.parseDouble(str);
		}
	}
	//added by manish mhatre on 08-nov-2019
	//start manish
	private String checkNull(String input)
	{
		if (input == null || "null".equals(input))
		{
			input = "";
		}
		return input;
	}
	//end manish
	private double integralPartQty(double value) {
		double fractionalPart = value % 1;
		double integralPart = value - fractionalPart;
		System.out.println(integralPart +" integralPart     "+ fractionalPart);
		return integralPart;
	}
	//added by nandkumar gadkari on 11/02/19------------------------end--------------------------------
}
