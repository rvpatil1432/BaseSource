/*
	This  is meant for confirming the Sales Order Attribute.
	After Generating Sales Order Attribute(SordAtt) 
	Sales Order Atrribute Confirmation is called.
	It generates Item Code for new physical Attribute entered in Sales Order Attribuite Form.
	Then it creates a Sales Order & confirms the Sales Order.
	Lastly it confirms the Sales Order Attribute.
	This  is invoked from ActionHandlerService 
	
	Developed by :Jiten
	Company : Base Information Management Pvt. Ltd
	Version : 1.0
	Date : 06-Aug-05
*/ 
package ibase.webitm.ejb.dis.adv;

import ibase.webitm.utility.*;
import ibase.utility.CommonConstants;

import ibase.webitm.ejb.*;
import ibase.system.config.*;

import java.rmi.RemoteException;
import java.text.*;
import java.util.*;
//import java.math.*;
import java.sql.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.ejb.*;
import javax.naming.InitialContext;
import java.io.*;
//import java.io.File;
//import java.sql.*;



//added temporarily
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
//import org.xml.sax.InputSource;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
//import javax.xml.transform.stream.StreamSource;
//import javax.xml.transform.stream.StreamResult;
//import javax.xml.transform.TransformerException;
//import javax.xml.transform.TransformerConfigurationException;
//End addding
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class SordAttConf extends ActionHandlerEJB implements SordAttConfLocal, SordAttConfRemote
{
	String blankTaxString = null;
	String taxAmt = "0.0";
	//Hashtable orderDetail = new Hashtable();
	Hashtable[] orderDetail = null; //Added 20/07/06 at Supreme
	
	double totAmtHdr = 0d,taxAmtHdr = 0d,ordAmtHdr = 0d;
	
	/*public void ejbCreate() throws RemoteException, CreateException 
	{
	}
	public void ejbRemove()
	{
	}
	public void ejbActivate() 
	{
	}
	public void ejbPassivate() 
	{
	}*/

	/*
	1. This method is called by ConfirmSordformAttService for the Sorder Attribute confirmation of a tranID.
	2. It returns 'Success' if receipt is confirmed or 'Failure' if failed.
	*/
	public String confirm(String tranID,String xtraParams, String forcedFlag) throws RemoteException,ITMException
	{
		String result = "";
		try
		{
			System.out.println("Confirming SalesOrder Attribute....");
			result =  confirmSordAtt(tranID,xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception ::"+e.getMessage());
			throw new ITMException(e);
		}
		System.out.println("Returning Result ::"+result);
		return result;
	}
	private	String confirmSordAtt(String tranID,String xtraParams) throws RemoteException,ITMException
	{
		Connection conn = null;
		Statement stmt = null;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt2 = null;
		ResultSet rs = null;
		String sql = "";
		String format = "";
		String retString = "";
		String userId = "BASE";
		String saleOrder ="";
		String keyString = "";
		String keyCol = "";
		String tranSer = "";
		String itemCode = "";
		String XMLString = "";
		ArrayList arrLineNo = new ArrayList();
		ArrayList arrItemType = new ArrayList();
		ArrayList arrItemFlag = new ArrayList();
		ArrayList arrUnit = new ArrayList();
		ArrayList arrUnitRate = new ArrayList();
		ArrayList arrUnitStd = new ArrayList();
		ArrayList arrLocType = new ArrayList();
		ArrayList arrPhyAttrib1 = new ArrayList();
		ArrayList arrPhyAttrib2 = new ArrayList();
		ArrayList arrPhyAttrib3 = new ArrayList();
		ArrayList arrPhyAttrib4 = new ArrayList();
		ArrayList arrPhyAttrib5 = new ArrayList();
		ArrayList arrPhyAttrib6 = new ArrayList();
		ArrayList arrPhyAttrib7 = new ArrayList();
		ArrayList arrPhyAttrib8 = new ArrayList();
		ArrayList arrPhyAttrib9 = new ArrayList();
		ArrayList arrPhyAttrib10 = new ArrayList();
		ArrayList arrPhyAttrib11 = new ArrayList();
		ArrayList arrPhyAttrib12 = new ArrayList();
		ArrayList arrPhyAttrib13 = new ArrayList();
		ArrayList arrPhyAttrib14 = new ArrayList();
		ArrayList arrPhyAttrib15 = new ArrayList();
		ArrayList arrPhyAttrib16 = new ArrayList();
		ArrayList arrPhyAttrib17 = new ArrayList();
		ArrayList arrPhyAttrib18 = new ArrayList();
		ArrayList arrPhyAttrib19 = new ArrayList();
		ArrayList arrPhyAttrib20 = new ArrayList();
		ArrayList arrPhyAttrib21 = new ArrayList();
		ArrayList arrPhyAttrib22 = new ArrayList();
		Hashtable hashPhyAttrib = new Hashtable();
		
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		ConnDriver connDriver = new ConnDriver();
		try
		{
			format= genericUtility.getApplDateFormat();
		}
		catch(Exception e)
		{						  
			System.out.println("Exception :[ConfirmSordAtt]While Getting Date Format"+e.getMessage());		
		}
		java.util.Date DateX = new java.util.Date();
		java.text.SimpleDateFormat dtf= new SimpleDateFormat(format);
		
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);

			System.out.println("Confirm Sorder Attribute Called.....");
			if (dupConf(tranID,conn))
			{
				retString =	itmDBAccess.getErrorString("","VTWORCONF","","",conn);
			}
			else if (checkForDetailEntry("SORDFORM_ATT_DET","TRAN_ID",tranID,conn))
			{
				retString =	itmDBAccess.getErrorString("","VTNOREC2","","",conn);
			}
			else 
			{
				String itemSer = "";
				String siteCode = "";
				String custCode = "";
				String currCode = "";
				String exchRate = "";
				String transMode = "";
				java.util.Date ordDate = new java.util.Date();

				sql = "SELECT ORD_DATE,SITE_CODE,ITEM_SER,CUST_CODE,CURR_CODE,EXCH_RATE,TRANS_MODE FROM SORDFORM_ATT WHERE TRAN_ID ='"+tranID+"'";	 
				try
				{
					stmt = conn.createStatement();
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						ordDate = rs.getDate("ORD_DATE");
						siteCode = rs.getString("SITE_CODE");
						itemSer = rs.getString("ITEM_SER");
						custCode = rs.getString("CUST_CODE"); 
						currCode = rs.getString("CURR_CODE");
						exchRate = rs.getString("EXCH_RATE");
						transMode = rs.getString("TRANS_MODE");
					}
					
					//Get Details 
					sql = "SELECT LINE_NO,ITEM_TYPE,ITEM_FLAG,UNIT,LOC_TYPE,PHY_ATTRIB_1,PHY_ATTRIB_2,PHY_ATTRIB_3,PHY_ATTRIB_4,PHY_ATTRIB_5,PHY_ATTRIB_6,PHY_ATTRIB_7,PHY_ATTRIB_8,PHY_ATTRIB_9,PHY_ATTRIB_10,PHY_ATTRIB_11, "+
						  "PHY_ATTRIB_12,PHY_ATTRIB_13,PHY_ATTRIB_14,PHY_ATTRIB_15,PHY_ATTRIB_16,PHY_ATTRIB_17,PHY_ATTRIB_18,PHY_ATTRIB_19,PHY_ATTRIB_20,PHY_ATTRIB_21,PHY_ATTRIB_22,UNIT__RATE,UNIT__STD "+
						  "FROM SORDFORM_ATT_DET WHERE TRAN_ID = '"+tranID+"'";
								
					rs = stmt.executeQuery(sql);
					while (rs.next())
					{
						arrLineNo.add(rs.getString("LINE_NO"));
						arrItemType.add(rs.getString("ITEM_TYPE"));
						arrItemFlag.add(rs.getString("ITEM_FLAG"));
						arrUnit.add(rs.getString("UNIT"));
						arrLocType.add(rs.getString("LOC_TYPE"));
						arrPhyAttrib1.add(rs.getString("PHY_ATTRIB_1"));
						arrPhyAttrib2.add(rs.getString("PHY_ATTRIB_2"));
						arrPhyAttrib3.add(rs.getString("PHY_ATTRIB_3"));
						arrPhyAttrib4.add(rs.getString("PHY_ATTRIB_4"));
						arrPhyAttrib5.add(rs.getString("PHY_ATTRIB_5"));
						arrPhyAttrib6.add(rs.getString("PHY_ATTRIB_6"));
						arrPhyAttrib7.add(rs.getString("PHY_ATTRIB_7"));
						arrPhyAttrib8.add(rs.getString("PHY_ATTRIB_8"));
						arrPhyAttrib9.add(rs.getString("PHY_ATTRIB_9"));
						arrPhyAttrib10.add(rs.getString("PHY_ATTRIB_10"));
						arrPhyAttrib11.add(rs.getString("PHY_ATTRIB_11"));
						arrPhyAttrib12.add(rs.getString("PHY_ATTRIB_12"));
						arrPhyAttrib13.add(rs.getString("PHY_ATTRIB_13"));
						arrPhyAttrib14.add(rs.getString("PHY_ATTRIB_14"));
						arrPhyAttrib15.add(rs.getString("PHY_ATTRIB_15"));
						arrPhyAttrib16.add(rs.getString("PHY_ATTRIB_16"));
						arrPhyAttrib17.add(rs.getString("PHY_ATTRIB_17"));
						arrPhyAttrib18.add(rs.getString("PHY_ATTRIB_18"));
						arrPhyAttrib19.add(rs.getString("PHY_ATTRIB_19"));
						arrPhyAttrib20.add(rs.getString("PHY_ATTRIB_20"));
						arrPhyAttrib21.add(rs.getString("PHY_ATTRIB_21"));
						arrPhyAttrib22.add(rs.getString("PHY_ATTRIB_22"));
						arrUnitRate.add(rs.getString("UNIT__RATE"));
						arrUnitStd.add(rs.getString("UNIT__STD"));
					}
					rs.close();
					stmt.close();
					hashPhyAttrib.put("1",arrUnit);
					hashPhyAttrib.put("2",arrLocType);
					hashPhyAttrib.put("3",arrPhyAttrib1);
					hashPhyAttrib.put("4",arrPhyAttrib2);
					hashPhyAttrib.put("5",arrPhyAttrib3);
					hashPhyAttrib.put("6",arrPhyAttrib4);
					hashPhyAttrib.put("7",arrPhyAttrib5);
					hashPhyAttrib.put("8",arrPhyAttrib6);
					hashPhyAttrib.put("9",arrPhyAttrib7);
					hashPhyAttrib.put("10",arrPhyAttrib8);
					hashPhyAttrib.put("11",arrPhyAttrib9);
					hashPhyAttrib.put("12",arrPhyAttrib10);
					hashPhyAttrib.put("13",arrPhyAttrib11);
					hashPhyAttrib.put("14",arrPhyAttrib12);
					hashPhyAttrib.put("15",arrPhyAttrib13);
					hashPhyAttrib.put("16",arrPhyAttrib14);
					hashPhyAttrib.put("17",arrPhyAttrib15);
					hashPhyAttrib.put("18",arrPhyAttrib16);
					hashPhyAttrib.put("19",arrPhyAttrib17);
					hashPhyAttrib.put("20",arrPhyAttrib18);
					hashPhyAttrib.put("21",arrPhyAttrib19);
					hashPhyAttrib.put("22",arrPhyAttrib20);
					hashPhyAttrib.put("23",arrPhyAttrib21);
					hashPhyAttrib.put("24",arrPhyAttrib22);
					hashPhyAttrib.put("25",arrUnitRate);
					hashPhyAttrib.put("26",arrUnitStd);

				}
				catch(Exception e)
				{
				   System.out.println("Exception Get Header & Detail Data ::"+e);
				   retString =	itmDBAccess.getErrorString("","VTCREDIT01","","",conn);
				   e.printStackTrace();
				}

				System.out.println("arrItemType.size() "+arrItemType.size());
				orderDetail = new Hashtable[arrItemType.size()];//Added 20/07/06 at Supreme 

				for (int i=0;i < arrItemType.size();i++ )
				{
					orderDetail[i] = new Hashtable();
				}
				//End adding 20/07/06
				
				for (int i=0; i < arrItemType.size(); i++ ) // item Code Generation for New Physical Attribute
				{
					String itemFlag = "";
					String xmlString = "";
					int phyAttNo = i;


					itemFlag = 	arrItemFlag.get(i).toString();
					//if (itemFlag != null && itemFlag.trim().equalsIgnoreCase("N")) //generateItemCode() for new Attribute
					if (itemFlag != null && itemFlag.trim().length() > 0)// itemFlag E or N in any case it will go for generate item code.
					{
						String itemType = arrItemType.get(i).toString();
						String lineNo = arrLineNo.get(i).toString();
						retString = generateItemCode(itemType,tranID,lineNo,phyAttNo,itemSer,siteCode,hashPhyAttrib,xtraParams,conn);
					}
				}
				//generateSalesOrder();
				if (retString == null || retString.trim().length() == 0)
				{
					int i = 0;
					XMLString = "<?xml version=\"1.0\"?>\r\n<Root>\r\n<header>"+
										"\r\n</header><Detail1><item_ser>"+itemSer+"</item_ser>"+
										"\r\n<site_code>"+siteCode+"</site_code>\r\n<order_date>"+dtf.format(ordDate)+"</order_date></Detail1></Root>";
									
					String ls_orderType = "";
					java.sql.Date ls_orderDate = new java.sql.Date(System.currentTimeMillis());
					String ls_custCode = "";
					String ls_custPord = "";
					java.sql.Date ls_pordDate = new java.sql.Date(System.currentTimeMillis()); 
					java.sql.Date ldt_taxDate = new java.sql.Date(System.currentTimeMillis()); //ADDED BY SHARON ON 29-DEC-2005
					String ls_priceList = "";
					java.sql.Date ls_plDate = new java.sql.Date(System.currentTimeMillis()); 
					String ls_currCode = "";
					String ls_exchRate = "";
					double ls_ordAmt = 0.00;
					double ls_totAmt = 0.00;
					double ls_taxAmt = 0.00;
					String ls_tranCode = "";
					String ls_transMode = "";
					String ls_priceListClg = "";

					sql = "SELECT KEY_STRING,TRAN_ID_COL,REF_SER FROM TRANSETUP "
									+"WHERE TRAN_WINDOW ='w_sorder'";
					System.out.println("Executing SQL :: GET DATA FROM TRANSETUP FOR W_SORDER ::"+sql);
					try //Generate Saleorder
					{	
						stmt = conn.createStatement();
						rs = stmt.executeQuery(sql);
											
						if (rs.next())
						{
							keyString = rs.getString(1);
							keyCol = rs.getString(2);
							tranSer = rs.getString(3);							
						}
					
						CommonConstants.setIBASEHOME();
						TransIDGenerator tg = new TransIDGenerator(XMLString, userId, CommonConstants.DB_NAME); //FOR DB2
						//TransIDGenerator tg = new TransIDGenerator(XMLString, userId, "");
						saleOrder = tg.generateTranSeqID(tranSer, keyCol, keyString, conn);

						System.out.println("Generated Sales Order ::"+saleOrder);
						
						generateSaleOrder(tranID,saleOrder,xtraParams,conn);
						
						ArrayList ls_lineNo = new ArrayList();
						ArrayList ls_itemCode = new ArrayList();
						ArrayList ls_itemFlag = new ArrayList();
						ArrayList ls_Quantity = new ArrayList();
						ArrayList ls_unit = new ArrayList();
						ArrayList ls_rate = new ArrayList();
						ArrayList ls_discount = new ArrayList();
						ArrayList ls_unitRate = new ArrayList();
						ArrayList ls_convQtyStd = new ArrayList();
						ArrayList ls_convRtuStd = new ArrayList();
						ArrayList ls_unitStd = new ArrayList();
						ArrayList ls_quanStd = new ArrayList();
						ArrayList ls_rateStd = new ArrayList();
						ArrayList ls_noArt = new ArrayList();
						ArrayList ls_clgRate = new ArrayList();
						ArrayList ls_loctype = new ArrayList();
						ArrayList ls_taxClass = new ArrayList();
						ArrayList ls_taxChap = new ArrayList();
						ArrayList ls_taxEnv = new ArrayList();
						String taxClass = "",taxChap = "",taxEnv = "";

						//SHARON 29-DEC-2005
						//ADDED ORDER TYPE IN THE SQL SELECT
						sql = "SELECT LINE_NO,ITEM_CODE,ITEM_FLAG,QUANTITY,UNIT,RATE,DISCOUNT,UNIT__RATE,CONV__QTY_STDUOM,CONV__RTUOM_STDUOM,"+
							"UNIT__STD,QUANTITY__STDUOM,RATE__STDUOM,NO_ART,RATE__CLG,LOC_TYPE,TAX_CLASS,TAX_CHAP,TAX_ENV "+
							"FROM SORDFORM_ATT_DET WHERE TRAN_ID ='"+tranID+"'";
						
						System.out.println("Executing SQL :: Getting Detail Data ::"+sql);
						rs = stmt.executeQuery(sql);
						System.out.println("Query Executed\n");
						while (rs.next())
						{
							ls_lineNo.add(rs.getString("LINE_NO"));
							ls_itemCode.add(rs.getString("ITEM_CODE"));
							ls_itemFlag.add(rs.getString("ITEM_FLAG"));
							ls_Quantity.add(rs.getString("QUANTITY"));
							ls_unit.add(rs.getString("UNIT"));
							ls_rate.add(rs.getString("RATE"));
							ls_discount.add(rs.getString("DISCOUNT"));
							ls_unitRate.add(rs.getString("UNIT__RATE"));
							ls_convQtyStd.add(rs.getString("CONV__QTY_STDUOM"));
							ls_convRtuStd.add(rs.getString("CONV__RTUOM_STDUOM"));
							ls_unitStd.add(rs.getString("UNIT__STD"));
							ls_quanStd.add(rs.getString("QUANTITY__STDUOM"));
							ls_rateStd.add(rs.getString("RATE__STDUOM"));
							ls_noArt.add(rs.getString("NO_ART"));
							ls_clgRate.add(new Double(rs.getDouble("RATE__CLG")));
							ls_loctype.add(rs.getString("LOC_TYPE"));
							ls_taxClass.add(rs.getString("TAX_CLASS"));
							ls_taxChap.add(rs.getString("TAX_CHAP"));
							ls_taxEnv.add(rs.getString("TAX_ENV"));
						}
										
						System.out.println("Item Types :: "+arrItemType);

						String line_no = "1";
						int row = 1;
						totAmtHdr = 0;
						taxAmtHdr = 0;
						ordAmtHdr = 0;
						String chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
						String chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId");
						//item_code__ord,TAX_ENV,tax_class,tax_chap added by sharon on 19-Dec-2005
						sql = "INSERT INTO SORDDET(SALE_ORDER,LINE_NO,SITE_CODE,QUANTITY,UNIT,RATE,DISCOUNT,"+
							  "UNIT__RATE,CONV__QTY_STDUOM,CONV__RTUOM_STDUOM,UNIT__STD,QUANTITY__STDUOM,RATE__STDUOM,"+
							  "ITEM_CODE,ITEM_SER,ITEM_FLG,CHG_DATE,CHG_USER,CHG_TERM,ITEM_CODE__ORD,TAX_CLASS,TAX_CHAP,TAX_ENV,TAX_AMT,NET_AMT,ITEM_DESCR,RATE__CLG,LINE_NO__SFORM,SORDFORM_NO) "+
								" VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,'I',?,'"+chgUser+"','"+chgTerm+"',?,?,?,?,?,?,?,?,?,?)";
						pstmt = conn.prepareStatement(sql);
						
						System.out.println(" No Of Details ::"+ls_itemCode.size());
						String descr = "";
						ArrayList actRatio = new ArrayList();
						int lratio,rratio,counter;
						//for (int j=0;j < ls_itemCode.size() ; j++ )
						String genKey = "",tempSql = "";
						for (int j=0;j < arrItemType.size() ; j++ )
						{
							System.out.println("For Item Code ::"+ls_itemCode.get(j));
							System.out.println("Insert Sql :: "+sql);
							Set orderSet = orderDetail[j].keySet();//Added 20/07/06 at Supreme							

							Iterator iter = orderSet.iterator();
														
							//Added on 30/06/06 - jiten
							String thrColName = "",unitNetWgt = "",unitRate ="",rateOpt = "";
							sql = "SELECT UNIT__NETWT FROM ITEM WHERE ITEM_CODE = '"+ls_itemCode.get(j).toString()+"'";
							rs = stmt.executeQuery(sql);
							if (rs.next())
							{
								unitNetWgt = rs.getString("UNIT__NETWT");	
							}
							unitRate = ls_unitRate.get(j).toString();
							sql = "SELECT RATE_OPT FROM ITEM_TYPE WHERE ITEM_TYPE = '"+arrItemType.get(j).toString()+"'";
							rs = stmt.executeQuery(sql);
							if (rs.next())
							{
								rateOpt = rs.getString("RATE_OPT");	
							}
							if (unitNetWgt != null && unitRate != null && unitNetWgt.equalsIgnoreCase(unitRate) && rateOpt != null && rateOpt.equalsIgnoreCase("1"))
							{
								thrColName = getAttributeColumn("Theoritical Wgt",arrItemType.get(j).toString(),conn);			
								sql = "SELECT "+thrColName+" FROM SORDFORM_ATT_DET WHERE TRAN_ID = '"+tranID+"' AND LINE_NO = '"+ls_lineNo.get(j).toString()+"'";
								
								System.out.println("SQL :: "+sql);
								System.out.println("Th Wgt Column :: "+thrColName);

								rs = stmt.executeQuery(sql);
								if (rs.next())
								{
									String convRate = rs.getString(1);
									if (convRate != null)
									{
										ls_convRtuStd.set(j,convRate);
										ls_rateStd.set(j,Double.toString((Double.parseDouble(convRate) * Double.parseDouble(ls_rate.get(j).toString()))));
									}
								}
							}
							//End Adding on 30/06/06
							while (iter.hasNext())
							{
								System.out.println("Inserting Into Sorddet");
								String newItemCode = iter.next().toString();
								String quantity = orderDetail[j].get(newItemCode).toString();

								pstmt.setString(1,saleOrder);
								System.out.println("1 "+saleOrder);
								pstmt.setString(2,("    "+line_no).substring(("    "+line_no).length()-3));	 //Jiten 17-12-05
								System.out.println("2 "+("    "+line_no).substring(("    "+line_no).length()-3));
								pstmt.setString(3,siteCode);
								System.out.println("3 "+siteCode);
								double quan = Double.parseDouble(quantity);
								
								pstmt.setString(4,quantity);
								System.out.println("4 "+quantity);
								pstmt.setString(5,ls_unit.get(j).toString());
								System.out.println("5 "+ls_unit.get(j).toString());
								pstmt.setString(6,ls_rate.get(j).toString());
								System.out.println("6 "+ls_rate.get(j).toString());
								pstmt.setDouble(7,Double.parseDouble(ls_discount.get(j).toString()));
								System.out.println("7 "+Double.parseDouble(ls_discount.get(j).toString()));
								pstmt.setString(8,ls_unitRate.get(j).toString());
								System.out.println("8 "+ls_unitRate.get(j).toString());
								pstmt.setString(9,ls_convQtyStd.get(j).toString());
								System.out.println("9 "+ls_convQtyStd.get(j).toString());
								pstmt.setString(10,ls_convRtuStd.get(j).toString());
								System.out.println("10 "+ls_convRtuStd.get(j).toString());
								pstmt.setString(11,ls_unitStd.get(j).toString());
								System.out.println("11 "+ls_unitStd.get(j).toString());
								pstmt.setString(12,ls_quanStd.get(j).toString());
								System.out.println("12 "+ls_quanStd.get(j).toString());
								pstmt.setString(13,ls_rateStd.get(j).toString());
								System.out.println("13 "+ls_rateStd.get(j).toString());
								//pstmt.setString(14,ls_noArt.get(j).toString());
								//System.out.println("Pos 14 :"+ls_noArt.get(j).toString());
								
								pstmt.setString(14,newItemCode);
								System.out.println("14 "+newItemCode);
								pstmt.setString(15,itemSer);
								System.out.println("15 "+itemSer);
								//pstmt.setString(16,ls_clgRate.get(j).toString());
								//pstmt.setString(17,ls_loctype.get(j).toString());
								
								pstmt.setTimestamp(16,new java.sql.Timestamp(System.currentTimeMillis()));
								System.out.println("16 "+new java.sql.Timestamp(System.currentTimeMillis()));
								pstmt.setString(17,newItemCode);
								System.out.println("17 "+newItemCode);
								String taxOrdDate = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(ls_orderDate);
								 
								//Calculate Tax	 for each Sorder Detail Entry
								//handleTax(tranId,lineNo,tranDate,quantity,rate,"INR",siteCode,(ctr++),taxClass,taxChap,taxEnv);
								
								if (ls_taxClass.get(j) == null)
								{
									taxClass = "";
								}
								else
								{
									taxClass = ls_taxClass.get(j).toString();
								}
								if (ls_taxChap.get(j) == null)
								{
									taxChap = "";							
								}
								else
								{
									taxChap = ls_taxChap.get(j).toString();
								}
								if (ls_taxEnv.get(j) == null)
								{
									taxEnv = "";
								}
								else
								{
									taxEnv = ls_taxEnv.get(j).toString().trim();
								}
								pstmt.setString(18,taxClass);
								System.out.println("18 "+taxClass);
								pstmt.setString(19,taxChap);
								System.out.println("19 "+taxChap);
								if (taxEnv.length() > 0 )
								{
									pstmt.setString(20,taxEnv);
									System.out.println("20 "+taxEnv);
								}
								else
								{
									pstmt.setNull(20,java.sql.Types.VARCHAR);
									System.out.println("20 "+java.sql.Types.VARCHAR);
								}
							
								//Added by Jiten temporarily 18/02/06
								System.out.println("taxClass :: "+taxClass);
								System.out.println("taxChap :: "+taxChap);
								System.out.println("taxEnv :: '"+taxEnv+"'");
								taxAmt = "0.0";
								handleTax(saleOrder,row,taxOrdDate,quan,Double.parseDouble(ls_rate.get(j).toString()),"INR",siteCode,row,taxClass,taxChap,taxEnv,ls_clgRate.get(j).toString(),ls_discount.get(j).toString(),ls_lineNo.get(j).toString(),tranID,conn);
								pstmt.setDouble(21,Double.parseDouble(taxAmt));
								System.out.println("21 "+Double.parseDouble(taxAmt));
								double netAmt = 0.0,disc = 0.0;
								disc = Double.parseDouble(ls_discount.get(j).toString());
								netAmt = Double.parseDouble(ls_quanStd.get(j).toString())* Double.parseDouble(ls_rateStd.get(j).toString());
								netAmt = netAmt - ((netAmt * disc)/100) + Double.parseDouble(taxAmt);
								totAmtHdr = totAmtHdr + netAmt;
								taxAmtHdr = taxAmtHdr + Double.parseDouble(taxAmt);
								pstmt.setDouble(22,netAmt);
								System.out.println("22 "+netAmt);
								sql = "SELECT ITEM_DESCR FROM SORDFORM_ATT_DET WHERE TRAN_ID = '"+tranID+"' AND LINE_NO = '"+ls_lineNo.get(j).toString()+"'";
								System.out.println("SQL :: "+sql);
								rs = stmt.executeQuery(sql);
								if (rs.next())
								{
									descr = rs.getString("ITEM_DESCR");

								}
								if (descr == null || descr.trim().length() == 0)
								{
									sql = "SELECT DESCR FROM ITEM WHERE ITEM_CODE = '"+newItemCode+"'";
									System.out.println("SQL :: "+sql);
									rs = stmt.executeQuery(sql);
									if (rs.next())
									{
										descr = rs.getString("DESCR");
									}						
								} 
								
								pstmt.setString(23,descr);
								System.out.println("23 "+descr);
								String clRate =  ((ls_clgRate.get(j).toString() == null) ? "":ls_clgRate.get(j).toString());
								pstmt.setString(24,clRate);
								System.out.println("24 "+clRate);
								pstmt.setString(25,ls_lineNo.get(j).toString());
								System.out.println("25 "+ls_lineNo.get(j).toString());
								pstmt.setString(26,tranID);
								System.out.println("26 "+tranID);
								//End TaxCalculate
								row = row+1;
								line_no = Integer.toString(row);
								pstmt.executeUpdate();
								descr = "";
								pstmt.clearParameters();
								System.out.println("Query Executed..");
							}
							orderSet.clear();
							orderDetail[j].clear();
						}

						ordAmtHdr = totAmtHdr - taxAmtHdr;
						stmt.close();
						pstmt.close();
						rs.close();					
					}				
					catch(Exception e)
					{
						conn.rollback();
						System.out.println("Exception :: "+e);
						e.printStackTrace();
						retString =	itmDBAccess.getErrorString("","VTCREDIT01","","",conn);//Not Confirmed
					} //End // Sale Order Generated

					retString = confirmSalesOrder(saleOrder,tranID,conn);
					try
					{
						if (retString.equalsIgnoreCase("Not Confirmed"))
						{
							conn.rollback();
							retString =	itmDBAccess.getErrorString("","VTCREDIT01","","",conn);//Not Confirmed
						}
						else if (retString.equalsIgnoreCase("Confirmed"))
						{
							conn.commit();
							retString =	itmDBAccess.getErrorString("","VTMCONF2","","",conn);//Confirmed
						}
					}
					catch(SQLException se)
					{
					}
				}
			}			
		}
		catch(Exception e) 
		{
			try
			{
				if (retString.equalsIgnoreCase("Not Confirmed"))
				{
					conn.rollback();
					retString =	itmDBAccess.getErrorString("","VTCREDIT01","","",conn);//Not Confirmed
				}
				conn.rollback();
			}
			catch(SQLException se){}
			
			System.out.println("Exception :: [ConfirmSordAtt][confirmSordAtt]"+e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			try
			{
				orderDetail = null;//Added 20/07/06 at Supreme
				System.out.println("Closing Connection......");
				conn.close();
				conn = null;
			}catch(SQLException se){}
		}
		System.out.println("retString ::"+retString);
		return retString;
	}
		
	//Confirm SaleOrder
	private String confirmSalesOrder(String sorderNo,String sordAttNo,Connection conn)  
	{
		String retString = "Not Confirmed";
		Statement stmt = null;
		Statement stmt1 = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ResultSet rs1 = null;
		String sql = "",lc_consume_fc = "N",ls_cust_code = "",ls_item_ser = "",ls_plist_disc = "",ls_project_code = "";
		java.util.Date DateX = new java.util.Date();
		java.util.Date ld_due_date = new java.util.Date();
		//java.util.Date ld_dsp_date = new java.util.Date();
		String ld_dsp_date = "";
		String format = "";
		StringBuffer xmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");//
		String msaleorder = "",mlineno = "",msitecode = "",mitemcode_ord = "",mitemflag = "",munit_ord = "",mlinenocontr = "";
		String munit_std = "",ls_sales_order_type = "",ls_varvalue = "",mexplev = "1.";
		double mqty_ord = 0.00,lc_rate = 0.00,mqty_std = 0.00;
		long mmin_life,ll_max_life_det = 0,lc_min_life_perc,ll_shelf_life = 0,ll_max_life = 0,ll_temp;
		Document dom = null,dom1 = null;
		String mparm1 = "",mparm2 = "",mparm3 = "",mparm4 = "",childNodeName = "";
		String update = "";
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		NodeList detailNodes = null;
		NodeList childNodes = null;
		Node child = null;
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();
		try
		{
			format = genericUtility.getApplDateFormat();
		}
		catch(Exception e) 
		{
			System.out.println("confirmSalesOrder :: confirmSalesOrder ::"+e);
		}
		java.text.SimpleDateFormat dtf = new java.text.SimpleDateFormat(format);
		System.out.println("Confirming SaleOrder :: "+sorderNo);
		try
		{
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
			stmt1 = conn.createStatement(); 
			sql = "SELECT CONSUME_FC,CUST_CODE,ITEM_SER,PRICE_LIST__DISC,PROJ_CODE,DUE_DATE FROM SORDER WHERE SALE_ORDER = '"+sorderNo+"'";
			System.out.println("SQL ::"+sql);
			rs = stmt.executeQuery(sql);
			if (rs.next())
			{
				lc_consume_fc = rs.getString("CONSUME_FC");
				if(lc_consume_fc == null){
					lc_consume_fc = "N";
				}
				ls_cust_code = rs.getString("CUST_CODE");
				ls_item_ser = rs.getString("ITEM_SER");
				ls_plist_disc = rs.getString("PRICE_LIST__DISC");
				ls_project_code = rs.getString("PROJ_CODE");
				ld_due_date = rs.getDate("DUE_DATE");
			}
			try
			{
				sql = "SELECT SALE_ORDER,LINE_NO,SITE_CODE,ITEM_CODE,ITEM_FLG,QUANTITY,UNIT,LINE_NO__CONTR,"+
					  "UNIT__STD,QUANTITY__STDUOM,DSP_DATE,RATE,MIN_SHELF_LIFE, MAX_SHELF_LIFE FROM SORDDET "+
					  "WHERE SALE_ORDER = '"+sorderNo+"' ORDER BY LINE_NO";
				System.out.println("SQL :: "+sql);
				rs = stmt.executeQuery(sql);
				if (!rs.next())
				{
					return "Not Confirmed";
				}
				rs.beforeFirst();
				while (rs.next())
				{
					System.out.println("In While Loop");
					msaleorder = rs.getString("SALE_ORDER");
					mlineno = rs.getString("LINE_NO");
					msitecode = rs.getString("SITE_CODE");
					mitemcode_ord = rs.getString("ITEM_CODE");
					mitemflag = rs.getString("ITEM_FLG");
					mqty_ord = rs.getDouble("QUANTITY");
					munit_ord = rs.getString("UNIT");
					mlinenocontr = rs.getString("LINE_NO__CONTR");
					munit_std = rs.getString("UNIT__STD");
					mqty_std = rs.getDouble("QUANTITY__STDUOM");
					//ld_dsp_date = rs.getDate("DSP_DATE");
					ld_dsp_date = rs.getString("DSP_DATE");
					lc_rate = rs.getDouble("RATE");
					mmin_life = rs.getLong("MIN_SHELF_LIFE");
					ll_max_life_det = rs.getLong("MAX_SHELF_LIFE");
					if (mmin_life == 0)
					{
						//mmin_life = 0;						
						sql = "SELECT MIN_SHELF_LIFE FROM CUSTOMERITEM WHERE CUST_CODE = '"+ls_cust_code+"' AND ITEM_CODE = '"+mitemcode_ord+"'";
						System.out.println("Executing SQL ::"+sql);
						rs1 = stmt1.executeQuery(sql);
						if (rs1.next())
						{
							mmin_life = rs1.getLong("MIN_SHELF_LIFE");
						}
						if (mmin_life == 0)
						{
							sql = "SELECT CASE WHEN MIN_SHELF_PERC IS NULL THEN 0 ELSE MIN_SHELF_PERC END FROM CUSTOMER_SERIES WHERE CUST_CODE = '"+ls_cust_code+"'	AND ITEM_SER = '"+ls_item_ser+"'";
							System.out.println("Executing SQL ::"+sql);
							rs1 = stmt1.executeQuery(sql);
							if (rs1.next())
							{
								lc_min_life_perc = rs1.getLong(1);
								if (lc_min_life_perc == 0)
								{
									mmin_life = 0;
									ll_max_life = 0;
								}
								else
								{
									sql = "SELECT (CASE WHEN SHELF_LIFE IS NULL THEN 0 ELSE SHELF_LIFE END ) FROM ITEM WHERE ITEM_CODE = '"+mitemcode_ord+"'";
									System.out.println("Executing Query ::"+sql);
									rs1 = stmt1.executeQuery(sql);
									if (rs1.next())
									{
										ll_shelf_life = rs1.getLong("SHELF_LIFE");
									}
									if (ll_shelf_life > 0)
									{
										mmin_life = Math.round((lc_min_life_perc/100) * ll_shelf_life);
										ll_max_life = ll_shelf_life;
									}
									else
									{
										mmin_life = 0;
										ll_max_life = 0;
									}								
								}
							}
							else
							{
								mmin_life = 0;
								ll_max_life = 0;
							}
							
						}
						if (mmin_life == 0)
						{
							sql = "SELECT MIN_SHELF_LIFE FROM ITEM WHERE ITEM_CODE = '"+mitemcode_ord+"'";
							System.out.println("Executing SQL ::"+sql);
							rs1 = stmt1.executeQuery(sql);
							if (rs1.next())
							{
								mmin_life = rs1.getLong("MIN_SHELF_LIFE");
							}
						}
						if (ls_plist_disc != null && ls_plist_disc.trim().length() > 0)
						{
							sql = "SELECT ORDER_TYPE FROM SORDER WHERE SALE_ORDER = '"+sorderNo+"'";
							System.out.println("Executing SQL ::"+sql);
							rs1 = stmt1.executeQuery(sql);
							if (rs1.next())
							{
								ls_sales_order_type = rs1.getString("ORDER_TYPE");
							}
							if (ls_sales_order_type != null && ls_sales_order_type.trim().equals("NE"))
							{
								sql = "SELECT (CASE WHEN NO_SALES_MONTH IS NULL THEN 0 ELSE NO_SALES_MONTH END) FROM ITEM WHERE ITEM_CODE = '"+mitemcode_ord+"'";
								System.out.println("Executing SQL ::"+sql);
								rs1 = stmt1.executeQuery(sql);
								if (rs1.next())
								{
									ll_max_life = rs1.getLong("NO_SALES_MONTH");
								}
								if (ll_max_life == 0)
								{
									ls_varvalue = itmDBAccess.getEnvDis("999999","NEAR_EXP_SHELF_LIFE",conn);
									if (ls_varvalue.equals("NULLFOUND"))
									{
										ls_varvalue = "0";
									}
									ll_max_life = Long.parseLong(ls_varvalue);
								}
								ll_temp = ll_max_life;
								ll_max_life = mmin_life;
								mmin_life = ll_temp;
							}
							else
							{

							}
						}
					}	
					//insert into datastore
					if (ll_max_life_det  > 0)
					{
						ll_max_life = ll_max_life_det;
					}
					xmlString.append("<Detail>");
					xmlString.append("<sale_order>").append(msaleorder).append("</sale_order>");
					xmlString.append("<line_no>").append(("      "+mlineno).substring(("      "+mlineno).length()- 3)).append("</line_no>");//Jiten 17-12-05
					xmlString.append("<site_code>").append(msitecode).append("</site_code>");
					xmlString.append("<item_code__ord>").append(mitemcode_ord).append("</item_code__ord>");
					xmlString.append("<item_code__ref>").append(mitemcode_ord).append("</item_code__ref>");
					xmlString.append("<item_code>").append(mitemcode_ord).append("</item_code>");
					xmlString.append("<item_flag>").append(mitemflag).append("</item_flag>");
					xmlString.append("<line_type>").append(mitemflag).append("</line_type>");
					xmlString.append("<unit__ord>").append(munit_ord).append("</unit__ord>");
					xmlString.append("<unit__ref>").append(munit_ord).append("</unit__ref>");
					xmlString.append("<unit>").append(munit_ord).append("</unit>");
					xmlString.append("<qty_ord>").append(mqty_ord).append("</qty_ord>");
					xmlString.append("<qty_ref>").append(mqty_ord).append("</qty_ref>");
					xmlString.append("<quantity>").append(mqty_ord).append("</quantity>");
					xmlString.append("<exp_lev>").append(mexplev).append("</exp_lev>");
					xmlString.append("<min_shelf_life>").append(mmin_life).append("</min_shelf_life>");
					xmlString.append("<max_shelf_life>").append(ll_max_life).append("</max_shelf_life>");
					xmlString.append("<consume_fc>").append((lc_consume_fc == null || lc_consume_fc.trim().length() == 0 ) ? "N":lc_consume_fc).append("</consume_fc>");
					xmlString.append("<due_date>").append( (ld_dsp_date == null) ? " ":ld_dsp_date).append("</due_date>");
					
					xmlString.append("</Detail>\r\n");

				}//While End
				xmlString.append("</Root>");
			}			//Try Block End
			catch(SQLException se)
			{
				retString = "Not Confirmed";
				System.out.println("SQL Exception ::"+se.getMessage());
				se.printStackTrace();
			}
			System.out.println("xmlString ::"+xmlString.toString());
			dom = new  ibase.utility.E12GenericUtility().parseString(xmlString.toString());
			detailNodes = dom.getElementsByTagName("Detail");
			//int cnt = detailNodes.getLength();
			for (int cnt = 0;cnt < detailNodes.getLength(); cnt++ )
			{
				child = detailNodes.item(cnt);
				childNodes = child.getChildNodes();
				for (int i = 0;i < childNodes.getLength() ;i++ )
				{
					childNodeName = childNodes.item(i).getNodeName();
					if (childNodeName.equalsIgnoreCase("line_type"))
					{
						mparm3 = childNodes.item(i).getFirstChild().getNodeValue();
						break;
					}					
				}
				if (!mparm3.equals("I"))
				{
					getExplodeBomDs(dom,child,conn); // 	Check Scheme for item				
				}
			} //For End
			int upd = insertFromDomToDB(dom,conn); //Insert Into SordItem table
			
			if (upd > 0)
			{
				//set confirm col of sorder attribute to 'Y'   
				//conn.commit();			
				update = "UPDATE SORDER SET CONFIRMED = 'Y',SORDATT_NO = ? ,CONF_DATE = ?,ORD_AMT = ?, TAX_AMT = ?, TOT_AMT = ? WHERE SALE_ORDER = ? ";
				System.out.println("Update Sorder ::"+update);
				pstmt = conn.prepareStatement(update);
				pstmt.setString(1,sordAttNo);
				System.out.println("Update Sorder Att ::"+sordAttNo);
				pstmt.setTimestamp(2,new java.sql.Timestamp(System.currentTimeMillis()));
				pstmt.setDouble(3,ordAmtHdr);
				pstmt.setDouble(4,taxAmtHdr);
				pstmt.setDouble(5,totAmtHdr);
				pstmt.setString(6,sorderNo);
				System.out.println("Update Sorder :"+sorderNo);
				totAmtHdr = 0;
				taxAmtHdr = 0;
				ordAmtHdr = 0;
				upd = pstmt.executeUpdate();			  
				System.out.println("Upd :"+upd);
				
				pstmt.close();
				pstmt = null;
				if (upd > 0)
				{
					update = "UPDATE SORDFORM_ATT SET CONFIRMED = 'Y',CONF_DATE = ? WHERE TRAN_ID = ?";
					System.out.println("Update :"+update);
					pstmt = conn.prepareStatement(update);
					pstmt.setTimestamp(1,new java.sql.Timestamp(System.currentTimeMillis()));
					pstmt.setString(2,sordAttNo);
					upd = pstmt.executeUpdate();
					if (upd > 0)
					{
						retString = "Confirmed";			
					}
				}
			}
			else
			{
				retString = "Not Confirmed";
			}
		}
		catch(Exception e)
		{
			try{
			conn.rollback();
			}catch(Exception ie) {}
			retString = "Not Confirmed";
			System.out.println("Exception ::"+e.getMessage());
			e.printStackTrace();
		}
		return retString; 
	}
	private int insertFromDomToDB(Document dom,Connection conn)
	{
		Statement stmt = null;
		ResultSet rs = null;
		ResultSetMetaData rsmdt = null;
		PreparedStatement pstmt = null;
		
		NodeList childNodes = null,detailNodes = null;
		Node detail = null,child = null;
		StringBuffer insertsql = new StringBuffer();
		StringBuffer fieldName = new StringBuffer();
		StringBuffer fieldValue = new StringBuffer();
		StringBuffer parameter = new StringBuffer();
		String sql  = "";
		String tagValue = "";
		Hashtable colType = new Hashtable(); 
		int upd = 0;
		try
		{
			stmt = conn.createStatement();
			detailNodes = dom.getElementsByTagName("Detail");
			
			for (int i=0;i < detailNodes.getLength(); i++) // No of Records
			{
				insertsql.append("INSERT INTO SORDITEM (");
				detail = detailNodes.item(i);//Get one Record
				childNodes = detail.getChildNodes();
				System.out.println("childNodes.getLength() ::"+childNodes.getLength());
				int n =  childNodes.getLength();
				for (int j = 0;j < n;j++ )
				{
					fieldName.append(childNodes.item(j).getNodeName()+",");
					sql = "SELECT "+childNodes.item(j).getNodeName()+" FROM SORDITEM";
					rs = stmt.executeQuery(sql);
					rsmdt = rs.getMetaData();
					colType.put(rsmdt.getColumnName(1),rsmdt.getColumnTypeName(1));
					parameter.append("?,");					
				}
				fieldName.deleteCharAt(fieldName.length()-1);
				parameter.deleteCharAt(parameter.length()-1);//Added latter
				String temp = fieldName +") VALUES ("+parameter+ ")";
				insertsql.append(temp);
				System.out.println("Insert SQL :: "+insertsql.toString());
				pstmt = conn.prepareStatement(insertsql.toString());

				for (int k=0;k < n; k++ )
				{
					String type = (String)colType.get(childNodes.item(k).getNodeName().toUpperCase());					
					//tagValue = childNodes.item(k).getFirstChild().getNodeValue().trim();
					tagValue = childNodes.item(k).getFirstChild().getNodeValue();
					System.out.println("childNodes.item(k) "+childNodes.item(k));
					System.out.println("childNodes.item(k) "+tagValue);
					if ((type.toUpperCase().indexOf("STRING") != -1) || (type.toUpperCase().indexOf("CHAR") != -1) || (type.toUpperCase().indexOf("VARCHAR") != -1))
					{
						if (tagValue == null || tagValue.length() <=0)
						{
							pstmt.setNull(k+1,java.sql.Types.VARCHAR);
						}
						else
						{
							pstmt.setString(k+1,tagValue);
						}
					}
					else if((type.toUpperCase().indexOf("DECIMAL") != -1) || (type.toUpperCase().indexOf("DOUBLE") != -1) || (type.toUpperCase().indexOf("NUMBER") != -1) || (type.toUpperCase().indexOf("LONG") != -1) || (type.toUpperCase().indexOf("SMALLINT") != -1))
					{
						if (tagValue == null || tagValue.trim().length() <=0)
						{
							pstmt.setNull(k+1,java.sql.Types.DOUBLE);
						}
						else
						{
							pstmt.setDouble(k+1,Double.parseDouble(tagValue));
						}						
					}
					else if((type.toUpperCase().indexOf("DATE") != -1) || (type.toUpperCase().indexOf("DATETIME") != -1) ||  (type.toUpperCase().indexOf("TIMESTAMP") != -1))
					{
						ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
						if (tagValue == null || tagValue.trim().length() <=0)
						{
							pstmt.setNull(k+1,java.sql.Types.TIMESTAMP);
						}
						else
						{
							pstmt.setTimestamp(k+1,java.sql.Timestamp.valueOf(genericUtility.getValidDateString(tagValue,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00"));						
						}						
					}
				}				
				upd = pstmt.executeUpdate();
				fieldName = fieldName.delete(0,fieldName.length());
				fieldValue = fieldValue.delete(0,fieldValue.length());
				insertsql = insertsql.delete(0,insertsql.length());
				parameter = parameter.delete(0,parameter.length());
			}
		}
		catch(Exception e)
		{
			try{
			conn.rollback(); }catch(Exception ie){}
			System.out.println("Exception  ::"+e.getMessage());
			e.printStackTrace();
		}
		return upd;
	}	
	//Returns true if the transaction is already confirmed. 
	private boolean dupConf(String tarnID,Connection conn) throws ITMException
	{
		Statement stmt = null;
		ResultSet rs = null;
		String sql="";
		boolean flag = false; 
		try
		{
			sql = " SELECT COUNT(*) FROM SORDFORM_ATT"+
						" WHERE TRAN_ID = '"+tarnID+"' AND CONFIRMED = 'Y' ";
						
			System.out.println("[ConfirmSordAtt:] [dupConf] EXECUTE SELECT: "+sql);
			
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			if(rs.next())
			{
				if(rs.getInt(1)!= 0)
				{
					flag = true;
				}
			}
			rs.close();
			stmt.close();
		}
		catch(SQLException e)
		{
			System.out.println("Exception : ConfirmSordAtt [dupConf]:==>\n"+e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}

		return flag;
	}
	//The method check for Detail entry for the corresponding colVal
	private boolean checkForDetailEntry(String table,String field,String colVal,Connection conn)
	{
		Statement stmt = null;
		ResultSet rs = null;
		String sql ="";
		boolean flag = false;
		try
		{
			sql = "SELECT COUNT(*) FROM "+table+" WHERE "+field+" = '"+colVal+"'";
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			if (rs.next())
			{
				if (rs.getInt(1)==0)
				{
					flag = true;
				}
			}
			rs.close();
			stmt.close();
		}
		catch(Exception e)
		{
			System.out.println("Exception :: [ConfirmSordAtt][checkForDetailEntry]"+e);
		}
		return flag;
	}
			//Added by Jiten - Pass the Dom and the Record for which the Free item is to checked
	public void getExplodeBomDs(Document dom,Node child,Connection conn) throws RemoteException,ITMException
	{
		String toexplode = "",explevel = "",mtype = "",mline = "",msorder = "",msite = "",refunit = "";
		String orditem = "",ordflg = "",ordunit = "",sqlStr = "",mcust_code = "",mlevel = "",refitem = "",mexptype = "";	
		String ls_order_type = "",ls_max_life = "",ls_round = "",mitem = "",mitemref = "",mitem_ref = "",mapplicable = "",munit = "";
		String mreqtype = "",mnature = "";
		double ordqty = 0.00,refqty = 0.00,mbatqty = 0.00,mqty = 0.00,mminqty = 0.00,aminqty = 0.00;
		double mmin_life = 0.00,lc_round_to = 0.00,amaxqty = 0.00,mqtyper = 0.00;
		long ll_max_life = 0,ll_temp = 0;
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();

		StringBuffer xmlString = new StringBuffer("<?xml version = \"1.0\"?>\r\n");
		Statement stmt = null;
		Statement stmt1 = null;
		String sql = "";
		ResultSet rs = null;
		ResultSet rs1 = null;

		int cnt = 0;
		Document dom1 = null;
		NodeList childNodes = null;
		Node child1 = null,child2 = null;
		String childNodeName = "";
 		//boolean connectionState = false;
		ConnDriver connDriver = new ConnDriver();
		try
		{
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
			stmt1 = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
			
			childNodes = child.getChildNodes();
			for (int i =0;i < childNodes.getLength() ; i++) // Getting the the Value of the Node
			{
				childNodeName = childNodes.item(i).getNodeName();
				if (childNodeName.equals("item_code"))
				{
					toexplode = childNodes.item(i).getFirstChild().getNodeValue();
				}
				else if (childNodeName.equals("exp_lev"))
				{
					explevel = childNodes.item(i).getFirstChild().getNodeValue(); 
				}
				 else if (childNodeName.equals("line_type"))
				 {
					 mtype = childNodes.item(i).getFirstChild().getNodeValue();  
				 }
				 else if (childNodeName.equals("line_no"))
				 {
					 mline = childNodes.item(i).getFirstChild().getNodeValue(); 
				 }
				 else if (childNodeName.equals("sale_order"))
				 {
					msorder = childNodes.item(i).getFirstChild().getNodeValue();
				 }
				 else if (childNodeName.equals("site_code"))
				 {
					msite = childNodes.item(i).getFirstChild().getNodeValue();
				 }
				 else if (childNodeName.equals("unit"))
				 {
					refunit = childNodes.item(i).getFirstChild().getNodeValue();
				 }
				 else if (childNodeName.equals("quantity"))
				 {
					 refqty = Double.parseDouble(childNodes.item(i).getFirstChild().getNodeValue());
				 }
				 else if (childNodeName.equals("item_code__ord"))
				 {
					 orditem = childNodes.item(i).getFirstChild().getNodeValue(); 
				 }
				 else if (childNodeName.equals("item_flag"))
				 {
					ordflg = childNodes.item(i).getFirstChild().getNodeValue(); 
				 }
				 else if (childNodeName.equals("unit__ord"))
				 {
					ordunit = childNodes.item(i).getFirstChild().getNodeValue();
				 }
				 else if (childNodeName.equals("qty_ord"))
				 {
					ordqty = Double.parseDouble(childNodes.item(i).getFirstChild().getNodeValue());
				 }
			}//Getting the Value of the Node //End For
			if (mtype.equals("B"))	// Bill Of Material
			{
				sqlStr = "SELECT ITEM_CODE, ITEM_REF FROM BOMDET WHERE BOM_CODE = '"+toexplode+"'";
				System.out.println("Executing SQL ::"+sqlStr);
				rs1 = stmt1.executeQuery(sqlStr);
			}
			else if (mtype.equals("F"))	 // Configured Item
			{
				sqlStr = "SELECT ITEM_CODE, ITEM_TYPE FROM ITEM WHERE ITEM_PARNT = '"+toexplode+"' AND ITEM_CODE != ITEM_PARNT";
				System.out.println("Executing SQL ::"+sqlStr);
				rs1 = stmt1.executeQuery(sqlStr);
			}
			// PICKING CUSTOMER CODE TO SEARCH FOR MIN SHELF LIFE FOR THAT
			// CUSTOMER & ITEM CODE
			sql = "SELECT CUST_CODE FROM SORDER WHERE SALE_ORDER = '"+msorder+"'";
			System.out.println("Executing SQL ::"+sql);
			rs = stmt.executeQuery(sql);
			if (rs.next())
			{
				mcust_code = rs.getString(1);				
			}
			if (!rs1.next())
			{
				return;
			}
			mlevel = "1";
			refitem = toexplode;
			rs1.beforeFirst();
			while (rs1.next())
			{
				mitem = rs1.getString(1);
				mitem_ref = rs1.getString(2);
				mapplicable = "Y";
				// Get unit and appropriate quantity
				if (mtype.equals("B")) // BOM
				{
					sql = "SELECT BOM.UNIT, BOM.BATCH_QTY, BOMDET.ITEM_REF, BOMDET.QTY_PER, BOMDET.REQ_TYPE,"+
						  "BOMDET.MIN_QTY, BOMDET.APP_MIN_QTY, BOMDET.APP_MAX_QTY, BOMDET.NATURE FROM BOM, BOMDET "+
						  "WHERE BOMDET.BOM_CODE = '"+toexplode+"' AND BOMDET.ITEM_CODE = '"+mitem+"' AND BOMDET.ITEM_REF "+
						  "= '"+mitem_ref+"' AND BOM.BOM_CODE = BOMDET.BOM_CODE";
					System.out.println("Executing SQL ::"+sql);
					rs = stmt.executeQuery(sql);
					if (!rs.next())
					{
						return;
					}
					rs.beforeFirst();
					if (rs.next())
					{
						munit = rs.getString(1);
						mbatqty = rs.getDouble(2);
						mitemref = rs.getString(3);
						mqtyper = rs.getDouble(4);
						mreqtype = rs.getString(5);
						mminqty = rs.getDouble(6);
						aminqty = rs.getDouble(7);
						amaxqty = rs.getDouble(8);
						mnature = rs.getString(9);
					}
					if (aminqty == 0 && amaxqty == 0)
					{
						mapplicable = "Y";
					}
					else
					{
						if (refqty >= aminqty && refqty <= amaxqty)
						{
							mapplicable = "Y";
						}
						else
						{
							mapplicable = "N";
						}
					}
					// Calculate qty based on requirement type
					if	(mreqtype.equals("S"))	// Slab
					{
						//mqty = truncate(refqty / mbatqty,0) * qtyPer; // To be Checked 
						mqty = (refqty / mbatqty) * mqtyper;
					}
					else if (mreqtype.equals("P"))	// Proportionate
					{
						mqty = ( mqtyper / mbatqty ) * refqty;
					}
					else if (mreqtype.equals("F"))	// Fixed
					{
						mqty = mqtyper; 
					}
					if (mqty < mminqty)
					{
						mqty = mminqty;
					}
				}
				else
				{
					mqty = refqty;
					sql = "SELECT UNIT FROM ITEM WHERE ITEM_CODE = '"+mitem+"'";
					rs = stmt.executeQuery(sql);
					if (!rs.next())
					{
						return;
					}
					rs.beforeFirst();
					if (rs.next())
					{
						munit = rs.getString(1);
					}
				}
				// get type of item F/B/I
				sql = "SELECT COUNT(*) AS COUNT FROM ITEM WHERE ITEM_CODE='"+mitem+"'";
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					cnt = rs.getInt("COUNT");
				}
				if (cnt == 0)	// Search in BOM
				{
					sql = "SELECT COUNT(*) AS COUNT FROM BOM WHERE BOM_CODE='"+mitem+"'";
					rs = stmt.executeQuery(sql);
					if (!rs.next())
					{
						return;
					}
					rs.beforeFirst();
					if (rs.next())
					{
						cnt = rs.getInt("COUNT");
					}
					if (cnt != 0)
					{
						mexptype = "B";
					}
					else
					{
						return;
					}
				}
				else
				{
					sql = "SELECT ITEM_STRU FROM ITEM WHERE ITEM_CODE = '"+mitem+"'";
					rs = stmt.executeQuery(sql);
					if (!rs.next())
					{
						return;
					}
					rs.beforeFirst();
					if (rs.next())
					{
						mexptype = rs.getString(1);
					}
					if (!mexptype.equals("F"))
					{
						mexptype = "I";
					}
				}
				//********************************************************************
				//PICKING MIN SHELF LIFE OF ITEM AND INSERTING IN SORDITEM IF MIN LIFE
				// IS > 0 1st FROM CUSTOMERITEM THEN IF NULL/0 THEN FROM ITEM TABLE
				sql = "SELECT MIN_SHELF_LIFE FROM CUSTOMERITEM WHERE CUST_CODE = '"+mcust_code+"' AND ITEM_CODE = '"+mitem+"'";
				rs = stmt.executeQuery(sql);

				if (rs.next())
				{
					mmin_life = rs.getDouble(1);
				}
				if (mmin_life == 0)
				{
					sql = "SELECT MIN_SHELF_LIFE FROM ITEM WHERE ITEM_CODE = '"+mitem+"'";
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						mmin_life = rs.getDouble(1);
					}
				}
				sql = "SELECT ORDER_TYPE FROM SORDER WHERE SALE_ORDER = '"+msorder+"'";
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					ls_order_type = rs.getString("ORDER_TYPE");
				}
				if (ls_order_type.trim().equals("NE"))
				{
					sql = "SELECT (CASE WHEN NO_SALES_MONTH IS NULL THEN 0 ELSE NO_SALES_MONTH END) FROM ITEM WHERE ITEM_CODE = '"+mitem+"'";
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						ll_max_life = rs.getLong(1);
					}
					if (ll_max_life == 0)
					{
						sql = "SELECT VAR_VALUE FROM DISPARM WHERE PRD_CODE = '999999' AND VAR_NAME = 'NEAR_EXP_SHELF_LIFE'";
						rs = stmt.executeQuery(sql);
						if (rs.next())
						{
							ls_max_life = rs.getString(1);
						}
						// Changed by Sneha on 12-09-2016, for Closing the Open Cursor [Start]
						if ( stmt != null )
						{
							stmt.close();
							stmt = null;
						}
						if ( rs != null )
						{
							rs.close();
							rs = null;
						}
						// Changed by Sneha on 12-09-2016, for Closing the Open Cursor [End]
						
						ll_max_life = Long.parseLong(ls_max_life);
					}
					ll_temp = ll_max_life;
					ll_max_life = (long)mmin_life;
					mmin_life = ll_temp;
				}
				if (mapplicable.equals("Y") || mnature.equals("C"))
				{
					sql = "SELECT UNIT FROM ITEM WHERE ITEM_CODE = '"+mitem+"'";
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						munit = rs.getString(1);
					}
					//PICKING ROUND, ROUND_TO	FROM UOM TO ROUND OFF THE QTY
					sql = "SELECT ROUND, ROUND_TO FROM UOM WHERE UNIT = '"+munit+"'";
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						ls_round = rs.getString("ROUND");
						lc_round_to = rs.getDouble("ROUND_TO");
					}
					if (ls_round == null || ls_round.trim().length() == 0)
					{
						ls_round = "N";
					}
					if (lc_round_to == 0)
					{
						lc_round_to = 0.001;
					}
					mqty = itmDBAccess.getRndamt(mqty, ls_round.charAt(0), lc_round_to);
					xmlString.append("<Detail>");
					xmlString.append("<exp_lev>").append(explevel+mlevel).append("</exp_lev>");
					xmlString.append("<sale_order>").append(msorder).append("</sale_order>");
					xmlString.append("<line_no>").append(("      "+mline).substring(("      "+mline).length()-3)).append("</line_no>");//Jiten 17-12-05
					xmlString.append("<site_code>").append(msite).append("</site_code>");
					xmlString.append("<item_code__ord>").append(orditem).append("</item_code__ord>");
					xmlString.append("<item_flag>").append(ordflg).append("</item_flag>");
					xmlString.append("<unit__ord>").append(ordunit).append("</unit__ord>");
					xmlString.append("<qty_ord>").append(ordqty).append("</qty_ord>");
					xmlString.append("<item_code__ref>").append(refitem).append("</item_code__ref>");
					xmlString.append("<unit__ref>").append(refunit).append("</unit__ref>");
					xmlString.append("<qty_ref>").append(refqty).append("</qty_ref>");
					xmlString.append("<item_code>").append(mitem).append("</item_code>");
					xmlString.append("<item_ref>").append(mitemref).append("</item_ref>");
					xmlString.append("<quantity>").append(mqty).append("</quantity>");
					xmlString.append("<unit>").append(munit).append("</unit>");
					xmlString.append("<line_type>").append(mexptype).append("</line_type>");
					xmlString.append("<min_shelf_life>").append(mmin_life).append("</min_shelf_life>");
					xmlString.append("<max_shelf_life>").append(ll_max_life).append("</max_shelf_life>");
					xmlString.append("<nature>").append(mnature).append("</nature>");
					xmlString.append("</Detail>\r\n");
					mlevel = Integer.toString(Integer.parseInt(mlevel)+1)+ ".";
				}
			}//While End
			//added Record To dom
			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			OutputStreamWriter errorWriter = new OutputStreamWriter(System.err, "UTF-8");
			ByteArrayInputStream baos = new ByteArrayInputStream(xmlString.toString().getBytes());
			dom1 = db.parse(baos);
			childNodes = dom1.getElementsByTagName("Detail");
			for (int j=0;j < childNodes.getLength();j++ )
			{
				child1 =  childNodes.item(j);
				child2 = dom.importNode(child1,true); //Importing Node from different Dom
				dom.getElementsByTagName("Root").item(0).appendChild(child2); //Appending to Dom
			}
			//End 
		}
		catch(SQLException ie)
		{
			System.out.println("Exception: ITMDBAccess: pickRate: type D: ==>"+ie);
			ie.printStackTrace();
			return;
		}
		catch(Exception e)
		{
			System.out.println("Exception: ITMDBAccess: priceListDiscount:" + e.getMessage() + ":");
			e.printStackTrace();
			return;
		}
	return;
	}

	//Added for Tax Calculation
	private void handleTax(String tranId,int lineNo,String tranDate,double quantity,double rate, String currCode,String siteCode,int ctr,String taxClass,String taxChap,String taxEnv,String rateClg,String discount,String sordAttlineNo,String sordAttNo,Connection conn)throws Exception
	{
		StringBuffer valueXmlString =null;
		try
		{
			//xml String in the foll. format
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>");
			valueXmlString.append("<Root>");
			valueXmlString.append("<Detail3 dbID='' domID='"+ctr+"' objName='sorder' objContext='3'>");
			valueXmlString.append("<attribute pkNames='' status='N' updateFlag='A' selected='N' />");
			valueXmlString.append("<tran_id>").append(tranId).append("</tran_id>");
			valueXmlString.append("<sordform_no>").append(sordAttNo).append("</sordform_no>");//Added on 21/07/06 at Supreme
			valueXmlString.append("<line_no__sform>").append(sordAttlineNo).append("</line_no__sform>");//Added on 21/07/06 at Supreme
			valueXmlString.append("<line_no>").append(lineNo).append("</line_no>");
			valueXmlString.append("<tax_date>").append(tranDate).append("</tax_date>");
			valueXmlString.append("<rate>").append(rate).append("</rate>");
			valueXmlString.append("<rate__clg>").append(rateClg).append("</rate__clg>");
			valueXmlString.append("<tax_class>").append(taxClass).append("</tax_class>");
			valueXmlString.append("<tax_chap>").append(taxChap).append("</tax_chap>");
			valueXmlString.append("<tax_env>").append(taxEnv).append("</tax_env>");
			valueXmlString.append("<tax_amt>").append("0").append("</tax_amt>");
			valueXmlString.append("<discount>").append(discount).append("</discount>");
			valueXmlString.append("<quantity>").append(quantity).append("</quantity>");	
			valueXmlString.append("<Taxes/>");
			valueXmlString.append("</Detail3>");
			valueXmlString.append("</Root>");
			
			Document itemDoc = new  ibase.utility.E12GenericUtility().parseString(valueXmlString.toString());
			Node currRecordNode = itemDoc.getElementsByTagName("Detail3").item(0);

			TaxCalculation taxCal = new TaxCalculation();
			
			appendOrReplaceTaxesNode(currRecordNode);
			
			NodeList currRecordChildList = currRecordNode.getChildNodes();
			int childListLength = currRecordChildList.getLength();
			Node currTaxNode = null;
			for (int i = 0; i < childListLength; i++)
			{
				if (currRecordChildList.item(i).getNodeName().equalsIgnoreCase("Taxes"))
				{
					currTaxNode = currRecordChildList.item(i);
				}
			}
			//taxCal.setUpdatedTaxDom(currRecordNode);
			taxCal.setUpdatedTaxDom(currTaxNode);
			taxCal.setTaxDom(currTaxNode);
			taxCal.setDataNode(currRecordNode);
			//taxCal.taxCalc("S-ORD", (tranId+":"+lineNo),tranDate,"rate__stduom", "quantity__stduom", currCode,siteCode);
			taxCal.taxCalc("S-ORD", tranId,tranDate,"rate__stduom", "quantity__stduom", currCode,siteCode,"2","1");//added form no. 2 ** vishakha


			System.out.println("CurrentTaxNode ::"+serializeDom(currTaxNode));
			System.out.println("CurrentRecordNode ::"+serializeDom(currRecordNode));
			//To get the calculated tax - Jiten 21/02/06
			taxAmt = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("tax_amt",currRecordNode);
			//
			currRecordChildList = currRecordNode.getChildNodes();
			for (int i = 0; i < childListLength; i++)
			{
				if (currRecordChildList.item(i).getNodeName().equalsIgnoreCase("Taxes"))
				{
					currTaxNode = currRecordChildList.item(i);					
					saveData(currTaxNode,conn);
				}
			}
		}
		catch(Exception e)
		{
			throw new ITMException(e);
		}
	}
	private void appendOrReplaceTaxesNode(Node currRecordNode)throws ITMException 
	{
		boolean found = false;
		MasterDataStatefulLocal masterDataStateful = null;
		try
		{

			if(	this.blankTaxString == null)
			{
				AppConnectParm appConnect = new AppConnectParm();
				InitialContext initialContext = new InitialContext(appConnect.getProperty());
				//MasterDataStatefulHome masterDataStatefulHome = (MasterDataStatefulHome)initialContext.lookup("MasterDataStateful");
				 masterDataStateful = (MasterDataStatefulLocal)initialContext.lookup("ibase/MasterDataStatefulEJB/local");
				//MasterDataStateful masterDataStateful = masterDataStatefulHome.create();
				this.blankTaxString = masterDataStateful.getBlankTaxDomForAdd("2");//previous it was Blank now Passed "2"**Vishakha
			}
			NodeList dataNodeChildList = currRecordNode.getChildNodes();
			int dataNodeChildListLen = dataNodeChildList.getLength();
			for (int i=0; i < dataNodeChildListLen; i++)
			{
				if (dataNodeChildList.item(i) != null)
				{
					if (dataNodeChildList.item(i).getNodeName().equalsIgnoreCase("Taxes") )
					{
						currRecordNode.replaceChild(currRecordNode.getOwnerDocument().importNode(new  ibase.utility.E12GenericUtility().parseString(blankTaxString).getFirstChild(), true),dataNodeChildList.item(i));
						found = true;
						break;
					}
				}
			}
			if (!found)
			{
				currRecordNode.appendChild(currRecordNode.getOwnerDocument().importNode(new  ibase.utility.E12GenericUtility().parseString(blankTaxString).getFirstChild(), true));
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :: removeTaxNodeInDetail :==>\n"+e);
			throw new ITMException(e);
		}
	}

	private String serializeDom(Node dom)throws ITMException
	{
		String retString = null;
		try
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Transformer serializer = TransformerFactory.newInstance().newTransformer();
			serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			serializer.transform(new DOMSource(dom), new javax.xml.transform.stream.StreamResult(out));
			retString = out.toString();
			out.flush();
			out.close();
			out = null;	
		}
		catch (Exception e)
		{
			System.out.println("Exception : MasterStateful : serializeDom :"+e);
			throw new ITMException(e);
		}
		return retString;
	}

	private void saveData(Node currNode,Connection conn) throws Exception
	{
		ConnDriver connDriver = new ConnDriver();
		try
		{
			StringBuffer fieldNameBuff = new StringBuffer();
			StringBuffer fieldValueBuff = new StringBuffer();
			StringBuffer insertQueryBuff = new StringBuffer();
			Statement stmt =null;
			String insertSql =null;

			int q =0,noOfField = 0,nodeLength = 0;
			
			stmt = conn.createStatement();
					
			Node currChildNode = null;
			NodeList currNodeList = currNode.getChildNodes();
			nodeLength = currNodeList.getLength();
			for (int i = 0;i < nodeLength ; i++ )
			{
				Node taxNode = currNodeList.item(i);
				NodeList taxfield = taxNode.getChildNodes();
				noOfField = taxfield.getLength();
				for (int j = 0;j < noOfField ; j++ )
				{																							 
					String fieldName = 	taxfield.item(j).getNodeName();
					if (!fieldName.equalsIgnoreCase("attribute") && !fieldName.equalsIgnoreCase("tax_descr") && !fieldName.equalsIgnoreCase("cc_editopt"))
					{
						if (taxfield.item(j).getFirstChild() != null && taxfield.item(j).getFirstChild().getNodeValue() != null && taxfield.item(j).getFirstChild().getNodeValue().trim().length() > 0)
						{
							fieldNameBuff.append(fieldName).append(",");
							if (fieldName.equalsIgnoreCase("TAX_AMT__TCURR") || fieldName.equalsIgnoreCase("EXCH_RATE") || fieldName.equalsIgnoreCase("EXCH_RATE_TRAN") || fieldName.equalsIgnoreCase("ROUND_TO") || fieldName.equalsIgnoreCase("RECO_PERC") || fieldName.equalsIgnoreCase("RECO_AMOUNT") || fieldName.equalsIgnoreCase("TAXABLE_AMT") || fieldName.equalsIgnoreCase("TAX_PERC") || fieldName.equalsIgnoreCase("TAX_AMT"))
							{
								fieldValueBuff.append(taxfield.item(j).getFirstChild().getNodeValue().trim()).append(",");
							}
							else
							{
								fieldValueBuff.append("'").append(taxfield.item(j).getFirstChild().getNodeValue().trim()).append("'").append(",");
							}
						}																		
					}
				}
				fieldNameBuff.deleteCharAt((fieldNameBuff.length() - 1));
				fieldValueBuff.deleteCharAt((fieldValueBuff.length() - 1));
				String tempString = "INSERT INTO TAXTRAN (" +fieldNameBuff+") VALUES ("+fieldValueBuff+")";
				//insertQueryBuff.append("INSERT INTO TAXTRAN (").append(fieldNameBuff).append(") VALUES (").append(fieldValueBuff).append(")");
				insertQueryBuff.append(tempString);
				fieldNameBuff.delete(0,fieldNameBuff.length());
				fieldValueBuff.delete(0,fieldValueBuff.length());
				System.out.println("insertQueryBuff ::"+insertQueryBuff.toString());
				stmt.addBatch(insertQueryBuff.toString());				
				insertQueryBuff.delete(0,insertQueryBuff.length());
			}
			stmt.executeBatch();			
		}
		catch(Exception e)
		{
			throw e;
		}
	}

	//The method generates the item code depending on item type, key gen,generate attribute
	//check if item code exist for current attributes then it will not generate the item code.
	private String generateItemCode(String itemType,String tranID,String lineNo,int phyAttNo,String itemSer,String siteCode,Hashtable hashPhyAttrib,String xtraParams,Connection conn) throws Exception
	{

		PreparedStatement pstmt = null;
		Statement stmt = null;
		ResultSet rs = null;
		String 	sql = "",XMLString="";
		String keyString = "",keyCol = "",tranSer = "";
		String keyGen = "",genAttrib = "",genAttribMap = "",rateOpt = "";
		String itemCode = "",newItemCode,userId = "BASE";
		String chgUser = "",chgTerm = ""; 
		double thWeight = 0;
		String thWeightCol = "";
		String unit = "",unitRate = "",unitStd = "";
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB(); 
	   	String retString = "";	
		try
		{
			chgUser = new  ibase.utility.E12GenericUtility().getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			chgTerm = new  ibase.utility.E12GenericUtility().getValueFromXTRA_PARAMS(xtraParams,"termId");
			ArrayList arrUnit = (ArrayList)hashPhyAttrib.get("1"); 
			ArrayList arrLocType = (ArrayList)hashPhyAttrib.get("2"); 
			ArrayList arrPhyAttrib1 = (ArrayList)hashPhyAttrib.get("3"); 
			ArrayList arrPhyAttrib2 = (ArrayList)hashPhyAttrib.get("4"); 
			ArrayList arrPhyAttrib3 = (ArrayList)hashPhyAttrib.get("5"); 
			ArrayList arrPhyAttrib4 = (ArrayList)hashPhyAttrib.get("6"); 
			ArrayList arrPhyAttrib5 = (ArrayList)hashPhyAttrib.get("7"); 
			ArrayList arrPhyAttrib6 = (ArrayList)hashPhyAttrib.get("8"); 
			ArrayList arrPhyAttrib7 = (ArrayList)hashPhyAttrib.get("9"); 
			ArrayList arrPhyAttrib8 = (ArrayList)hashPhyAttrib.get("10"); 
			ArrayList arrPhyAttrib9 = (ArrayList)hashPhyAttrib.get("11"); 
			ArrayList arrPhyAttrib10 = (ArrayList)hashPhyAttrib.get("12"); 
			ArrayList arrPhyAttrib11 = (ArrayList)hashPhyAttrib.get("13"); 
			ArrayList arrPhyAttrib12 = (ArrayList)hashPhyAttrib.get("14"); 
			ArrayList arrPhyAttrib13 = (ArrayList)hashPhyAttrib.get("15"); 
			ArrayList arrPhyAttrib14 = (ArrayList)hashPhyAttrib.get("16"); 
			ArrayList arrPhyAttrib15 = (ArrayList)hashPhyAttrib.get("17"); 
			ArrayList arrPhyAttrib16 = (ArrayList)hashPhyAttrib.get("18"); 
			ArrayList arrPhyAttrib17 = (ArrayList)hashPhyAttrib.get("19"); 
			ArrayList arrPhyAttrib18 = (ArrayList)hashPhyAttrib.get("20"); 
			ArrayList arrPhyAttrib19 = (ArrayList)hashPhyAttrib.get("21"); 
			ArrayList arrPhyAttrib20 = (ArrayList)hashPhyAttrib.get("22"); 
			ArrayList arrPhyAttrib21 = (ArrayList)hashPhyAttrib.get("23"); 
			ArrayList arrPhyAttrib22 = (ArrayList)hashPhyAttrib.get("24");
			
			sql = " SELECT KEY_STRING,TRAN_ID_COL,REF_SER FROM TRANSETUP "
					+"WHERE TRAN_WINDOW ='w_item' ";
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
									
			if (rs.next())
			{
				keyString = rs.getString(1);
				keyCol = rs.getString(2);
				tranSer = rs.getString(3);
			}
			rs.close();
			stmt.close();
		 
			XMLString = "<?xml version=\"1.0\"?>\r\n<Root>\r\n<header>"+
						"\r\n</header><Detail1><tran_id>"+tranID+"</tran_id><item_ser>"+itemSer+"</item_ser>"+
						"\r\n<item_type>"+itemType+"</item_type>"+
						"</Detail1></Root>";

			CommonConstants.setIBASEHOME();
		
			sql = "SELECT KEY_GEN,GEN_ATTRIB,GEN_ATTRIB_MAP,RATE_OPT,UNIT,UNIT__RATE,UNIT__STD FROM ITEM_TYPE WHERE ITEM_TYPE = '"+itemType+"'";
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			if (rs.next())
			{
				keyGen = rs.getString("KEY_GEN");			
				genAttrib = rs.getString("GEN_ATTRIB");			
				genAttribMap = rs.getString("GEN_ATTRIB_MAP");			
				rateOpt = rs.getString("RATE_OPT");			
				unit = rs.getString("UNIT");
				unitRate = rs.getString("UNIT__RATE");
				unitStd = rs.getString("UNIT__STD");
			}
			rs.close();
			stmt.close();
		
			int key = Integer.parseInt(keyGen);
			ArrayList genAttNo = new ArrayList();
			ArrayList genAttMapValue = new ArrayList();
			
			genAttNo = new  ibase.utility.E12GenericUtility().getTokenList(genAttrib,",");
			genAttMapValue = new  ibase.utility.E12GenericUtility().getTokenList(genAttribMap,";");
			thWeightCol = getAttributeColumn("Theoritical Wgt",itemType,conn);
			if (thWeightCol != null && thWeightCol.trim().length() > 0)
			{
				stmt = conn.createStatement();
				sql = "SELECT "+thWeightCol+" FROM SORDFORM_ATT_DET WHERE TRAN_ID = '"+tranID+"' AND LINE_NO = '"+lineNo+"'";
				System.out.println("SQL :: "+sql);
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					thWeight = rs.getDouble(1);	
				}
				stmt.close();
				rs.close();
			}
			System.out.println("Thweight Col : " + thWeightCol + "Thweight Value : " + thWeight);
			boolean flag = true;
			for (int k=1;k <= key;k++) 
			{
				stmt = conn.createStatement();
		   		String colourValue = "",colourName = "";
				if (k == 1)
				{
					ArrayList phyColNo = new  ibase.utility.E12GenericUtility().getTokenList(genAttMapValue.get(0).toString(),"=");
					
					System.out.println("phyColNo :: "+phyColNo.get(0).toString());
					System.out.println("phyColNo :: "+phyColNo.get(1).toString());

					sql = "SELECT PHY_ATTRIB_"+phyColNo.get(1).toString()+" FROM SORDFORM_ATT_DET WHERE TRAN_ID = '"+tranID+"' AND LINE_NO = '"+lineNo+"'";
					System.out.println("SQL :: "+sql);
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						colourValue = rs.getString(1);
						sql = "SELECT PHY_ATTRIB_23 FROM ITEM_TYPE WHERE ITEM_TYPE = '"+itemType+"'";
						System.out.println("SQL :: "+sql);
						rs = stmt.executeQuery(sql);
						if (rs.next())
						{
							colourName = rs.getString(1);//23
						}
						
					}
				}
				if (k == 2)
				{
					ArrayList phyCol = new  ibase.utility.E12GenericUtility().getTokenList(genAttMapValue.get(1).toString(),",");
					ArrayList phyColNo = new  ibase.utility.E12GenericUtility().getTokenList(phyCol.get(0).toString(),"=");
					ArrayList phyColNo2 = new  ibase.utility.E12GenericUtility().getTokenList(phyCol.get(1).toString(),"=");

					sql = "SELECT PHY_ATTRIB_"+phyColNo.get(1).toString()+" FROM SORDFORM_ATT_DET WHERE TRAN_ID = '"+tranID+"' AND LINE_NO = '"+lineNo+"'";
					System.out.println("SQL :: "+sql);
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						colourValue = rs.getString(1);
						sql = "SELECT PHY_ATTRIB_"+phyColNo2.get(1).toString()+" FROM ITEM_TYPE WHERE ITEM_TYPE = '"+itemType+"'";
						System.out.println("SQL :: "+sql);
						rs = stmt.executeQuery(sql);
						if (rs.next())
						{
							colourName = rs.getString(1);//23=24
						}
					}
				}
				if (k == 3)
				{
					ArrayList phyCol = new  ibase.utility.E12GenericUtility().getTokenList(genAttMapValue.get(2).toString(),",");
					ArrayList phyColNo = new  ibase.utility.E12GenericUtility().getTokenList(phyCol.get(0).toString(),"=");
					ArrayList phyColNo2 = new  ibase.utility.E12GenericUtility().getTokenList(phyCol.get(1).toString(),"=");
					
					sql = "SELECT PHY_ATTRIB_"+phyColNo.get(1).toString()+" FROM SORDFORM_ATT_DET WHERE TRAN_ID = '"+tranID+"' AND LINE_NO = '"+lineNo+"'";
					System.out.println("SQL :: "+sql);
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						colourValue = rs.getString(1);
						sql = "SELECT PHY_ATTRIB_"+phyColNo2.get(1).toString()+" FROM SORDFORM_ATT_DET WHERE TRAN_ID = '"+tranID+"' AND LINE_NO = '"+lineNo+"'";
						System.out.println("SQL :: "+sql);
						rs = stmt.executeQuery(sql);
						if (rs.next())
						{
							colourName = rs.getString(1);//23=18
						}
					}
				}
				stmt.close();
				rs.close();
				if (colourValue != null && colourValue.trim().length() > 0 && Integer.parseInt(colourValue) > 0)	// if quantity for specific color is specified then generate the item code
				{
					sql = "INSERT INTO ITEM(ITEM_CODE,DESCR,ITEM_SER,ITEM_TYPE,UNIT,STK_OPT,PHY_ATTRIB_1,"+   
						  "PHY_ATTRIB_2,PHY_ATTRIB_3,PHY_ATTRIB_4,PHY_ATTRIB_5,PHY_ATTRIB_6,PHY_ATTRIB_7,PHY_ATTRIB_8,"+
						  "PHY_ATTRIB_9,PHY_ATTRIB_10,PHY_ATTRIB_11,PHY_ATTRIB_12,PHY_ATTRIB_13,PHY_ATTRIB_14,PHY_ATTRIB_15, "+
						  "PHY_ATTRIB_16,PHY_ATTRIB_17,PHY_ATTRIB_18,PHY_ATTRIB_19,PHY_ATTRIB_20,PHY_ATTRIB_21,PHY_ATTRIB_22, "+
						  "ACTIVE,CHG_DATE,CHG_USER,CHG_TERM,SITE_CODE,UNIT__SAL,UNIT__RATE,UNIT__NETWT,MFG_TYPE) "+
						  "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
					System.out.println("Inserting Into Item Sql :: "+sql);
				
					StringBuffer sql2 = new StringBuffer();
					sql2.append("SELECT ITEM_CODE FROM ITEM WHERE ITEM_TYPE = '"+itemType+"' ");

					pstmt = conn.prepareStatement(sql);
					
					pstmt.setString(3,itemSer);
					pstmt.setString(4,itemType);
					pstmt.setString(5,unitStd);
					pstmt.setString(6,"2");
				
					if (arrPhyAttrib1.get(phyAttNo) == null || !(genAttNo.contains("1")))
					{
						if (thWeightCol.equalsIgnoreCase("phy_attrib_1"))
						{
							pstmt.setDouble(7,thWeight);							
						}
						else
						{
							pstmt.setNull(7,java.sql.Types.VARCHAR);							
							if(genAttNo.contains("1"))
							{
								sql2.append("AND PHY_ATTRIB_1 IS NULL ");
							}
						}
					}
					else
					{
						pstmt.setString(7,arrPhyAttrib1.get(phyAttNo).toString());
						sql2.append("AND PHY_ATTRIB_1 = '"+arrPhyAttrib1.get(phyAttNo).toString()+"' ");											
					}
				
					if (arrPhyAttrib2.get(phyAttNo) == null || !(genAttNo.contains("2")))
					{
						if (thWeightCol.equalsIgnoreCase("phy_attrib_2"))
						{
							pstmt.setDouble(8,thWeight);
						}
						else
						{
							pstmt.setNull(8,java.sql.Types.VARCHAR);							
							if (genAttNo.contains("2"))
							{
								sql2.append("AND PHY_ATTRIB_2 IS NULL ");
							}
						}
					}
					else
					{
						pstmt.setString(8,arrPhyAttrib2.get(phyAttNo).toString());
						sql2.append("AND PHY_ATTRIB_2 = '"+arrPhyAttrib2.get(phyAttNo).toString()+"' ");
					}
					if (arrPhyAttrib3.get(phyAttNo) == null || !(genAttNo.contains("3")))
					{
						if (thWeightCol.equalsIgnoreCase("phy_attrib_3"))
						{
							pstmt.setDouble(9,thWeight);
						}
						else
						{
							pstmt.setNull(9,java.sql.Types.VARCHAR);							
							if (genAttNo.contains("3"))
							{
								sql2.append("AND PHY_ATTRIB_3 IS NULL ");
							}
						}
					}
					else
					{
						pstmt.setString(9,arrPhyAttrib3.get(phyAttNo).toString());
						sql2.append("AND PHY_ATTRIB_3 = '"+arrPhyAttrib3.get(phyAttNo).toString()+"' ");
					}
					if (arrPhyAttrib4.get(phyAttNo) == null || !(genAttNo.contains("4")))
					{
						if (thWeightCol.equalsIgnoreCase("phy_attrib_4"))
						{
							pstmt.setDouble(10,thWeight);
						}
						else
						{
							pstmt.setNull(10,java.sql.Types.VARCHAR);							
							if (genAttNo.contains("4"))
							{
								sql2.append("AND PHY_ATTRIB_4 IS NULL ");
							}
						}
					}
					else
					{
						pstmt.setString(10,arrPhyAttrib4.get(phyAttNo).toString());
						sql2.append("AND PHY_ATTRIB_4 = '"+arrPhyAttrib4.get(phyAttNo).toString()+"' ");
					}
					if (arrPhyAttrib5.get(phyAttNo) == null || !(genAttNo.contains("5")))
					{
						if (thWeightCol.equalsIgnoreCase("phy_attrib_5"))
						{
							pstmt.setDouble(11,thWeight);
						}
						else
						{
							pstmt.setNull(11,java.sql.Types.VARCHAR);							
							if (genAttNo.contains("5"))
							{
								sql2.append("AND PHY_ATTRIB_5 IS NULL ");
							}
						}
					}
					else
					{
						pstmt.setString(11,arrPhyAttrib5.get(phyAttNo).toString());
						sql2.append("AND PHY_ATTRIB_5 = '"+arrPhyAttrib5.get(phyAttNo).toString()+"' ");
					}
					if (arrPhyAttrib6.get(phyAttNo) == null || !(genAttNo.contains("6")))
					{
						if (thWeightCol.equalsIgnoreCase("phy_attrib_6"))
						{
							pstmt.setDouble(12,thWeight);
						}
						else
						{
							pstmt.setNull(12,java.sql.Types.VARCHAR);							
							if (genAttNo.contains("6"))
							{
								sql2.append("AND PHY_ATTRIB_6 IS NULL ");
							}
						}
					}
					else
					{
						pstmt.setString(12,arrPhyAttrib6.get(phyAttNo).toString());
						sql2.append("AND PHY_ATTRIB_6 = '"+arrPhyAttrib6.get(phyAttNo).toString()+"' ");
					}
					if (arrPhyAttrib7.get(phyAttNo) == null || !(genAttNo.contains("7")))
					{
						if (thWeightCol.equalsIgnoreCase("phy_attrib_7"))
						{
							pstmt.setDouble(13,thWeight);
						}
						else
						{
							pstmt.setNull(13,java.sql.Types.VARCHAR);							
							if (genAttNo.contains("7"))
							{
								sql2.append("AND PHY_ATTRIB_7 IS NULL ");
							}
						}
					}
					else
					{
						pstmt.setString(13,arrPhyAttrib7.get(phyAttNo).toString());
						sql2.append("AND PHY_ATTRIB_7 = '"+arrPhyAttrib7.get(phyAttNo).toString()+"' ");
					}
					if (arrPhyAttrib8.get(phyAttNo) == null || !(genAttNo.contains("8")))
					{
						if (thWeightCol.equalsIgnoreCase("phy_attrib_8"))
						{
							pstmt.setDouble(14,thWeight);
						}
						else
						{
							pstmt.setNull(14,java.sql.Types.VARCHAR);					
							if (genAttNo.contains("8"))
							{
								sql2.append("AND PHY_ATTRIB_8 IS NULL ");
							}
						}
					}
					else
					{
						pstmt.setString(14,arrPhyAttrib8.get(phyAttNo).toString());
						sql2.append("AND PHY_ATTRIB_8 = '"+arrPhyAttrib8.get(phyAttNo).toString()+"' ");
					}
					if (arrPhyAttrib9.get(phyAttNo) == null || !(genAttNo.contains("9")))
					{
						if (thWeightCol.equalsIgnoreCase("phy_attrib_9"))
						{
							pstmt.setDouble(15,thWeight);
						}
						else
						{
							pstmt.setNull(15,java.sql.Types.VARCHAR);							
							if (genAttNo.contains("9"))
							{
								sql2.append("AND PHY_ATTRIB_9 IS NULL ");
							}
						}
					}
					else
					{
						pstmt.setString(15,arrPhyAttrib9.get(phyAttNo).toString());
						sql2.append("AND PHY_ATTRIB_9 = '"+arrPhyAttrib9.get(phyAttNo).toString()+"' ");
					}
					if (arrPhyAttrib10.get(phyAttNo) == null || !(genAttNo.contains("10")))
					{
						if (thWeightCol.equalsIgnoreCase("phy_attrib_10"))
						{
							pstmt.setDouble(16,thWeight);
						}
						else
						{
							pstmt.setNull(16,java.sql.Types.VARCHAR);						
							if (genAttNo.contains("10"))
							{
								sql2.append("AND PHY_ATTRIB_10 IS NULL ");
							}
						}
					}
					else
					{
						pstmt.setString(16,arrPhyAttrib10.get(phyAttNo).toString());
						sql2.append("AND PHY_ATTRIB_10 = '"+arrPhyAttrib10.get(phyAttNo).toString()+"' ");
					}
					if (arrPhyAttrib11.get(phyAttNo) == null) // phy attribute 11 is for Total Qty
					{
						if (thWeightCol.equalsIgnoreCase("phy_attrib_11"))
						{
							pstmt.setDouble(17,thWeight);
						}
						else
						{
							pstmt.setNull(17,java.sql.Types.VARCHAR);							
							sql2.append("AND PHY_ATTRIB_11 IS NULL ");
						}
					}
					else
					{
						pstmt.setString(17,colourName);
						sql2.append("AND PHY_ATTRIB_11 = '"+colourName+"' ");						
					}
					if (genAttNo.contains("12") || !genAttNo.contains("12"))
					{																	  						
						if (thWeightCol.equalsIgnoreCase("phy_attrib_12"))
						{
							pstmt.setDouble(18,thWeight);
						}
						else
						{
							pstmt.setNull(18,java.sql.Types.VARCHAR);
						}
						
					}
					if (genAttNo.contains("13") || !genAttNo.contains("13"))
					{
						if (thWeightCol.equalsIgnoreCase("phy_attrib_13"))
						{
							pstmt.setDouble(19,thWeight);
						}
						else
						{
							pstmt.setNull(19,java.sql.Types.VARCHAR);
						}						
					}
					if (genAttNo.contains("14")|| !(genAttNo.contains("14")))
					{
						if (thWeightCol.equalsIgnoreCase("phy_attrib_14"))
						{
							pstmt.setDouble(20,thWeight);
						}
						else
						{
							pstmt.setNull(20,java.sql.Types.VARCHAR);
						}						
					}
					if (arrPhyAttrib15.get(phyAttNo) == null || !(genAttNo.contains("15")))
					{
						if (thWeightCol.equalsIgnoreCase("phy_attrib_15"))
						{
							pstmt.setDouble(21,thWeight);
						}
						else
						{
							pstmt.setNull(21,java.sql.Types.VARCHAR);
							if (genAttNo.contains("15"))
							{
								sql2.append("AND PHY_ATTRIB_15 IS NULL ");
							}
						}
					}
					else
					{
						pstmt.setString(21,arrPhyAttrib15.get(phyAttNo).toString());
						sql2.append("AND PHY_ATTRIB_15 = '"+arrPhyAttrib15.get(phyAttNo).toString()+"' ");
					}
					if (arrPhyAttrib16.get(phyAttNo) == null || !(genAttNo.contains("16")))
					{
						if (thWeightCol.equalsIgnoreCase("phy_attrib_16"))
						{
							pstmt.setDouble(22,thWeight);
						}
						else
						{
							pstmt.setNull(22,java.sql.Types.VARCHAR);
							if (genAttNo.contains("16"))
							{
								sql2.append("AND PHY_ATTRIB_16 IS NULL ");
							}
						}
					}
					else
					{
						pstmt.setString(22,arrPhyAttrib16.get(phyAttNo).toString());
						sql2.append("AND PHY_ATTRIB_16 = '"+arrPhyAttrib16.get(phyAttNo).toString()+"' ");
					}
					if (arrPhyAttrib17.get(phyAttNo) == null || !(genAttNo.contains("17")))
					{
						if (thWeightCol.equalsIgnoreCase("phy_attrib_17"))
						{
							pstmt.setDouble(23,thWeight);
						}
						else
						{
							pstmt.setNull(23,java.sql.Types.VARCHAR);
							if (genAttNo.contains("17"))
							{
								sql2.append("AND PHY_ATTRIB_17 IS NULL ");
							}
						}
					}
					else
					{
						pstmt.setString(23,arrPhyAttrib17.get(phyAttNo).toString());
						sql2.append("AND PHY_ATTRIB_17 = '"+arrPhyAttrib17.get(phyAttNo).toString()+"' ");
					}
					if (arrPhyAttrib18.get(phyAttNo) == null || !(genAttNo.contains("18")))
					{
						if (thWeightCol.equalsIgnoreCase("phy_attrib_18"))
						{
							pstmt.setDouble(24,thWeight);
						}
						else
						{
							pstmt.setNull(24,java.sql.Types.VARCHAR);
							if (genAttNo.contains("18"))
							{
								sql2.append("AND PHY_ATTRIB_18 IS NULL ");
							}
						}
					}
					else
					{
						pstmt.setString(24,arrPhyAttrib18.get(phyAttNo).toString());
						sql2.append("AND PHY_ATTRIB_18 = '"+arrPhyAttrib18.get(phyAttNo).toString()+"' ");
					}
					if (arrPhyAttrib19.get(phyAttNo) == null || !(genAttNo.contains("19")))
					{
						if (thWeightCol.equalsIgnoreCase("phy_attrib_19"))
						{
							pstmt.setDouble(25,thWeight);
						}
						else
						{
							pstmt.setNull(25,java.sql.Types.VARCHAR);						
							if (genAttNo.contains("19"))
							{
								sql2.append("AND PHY_ATTRIB_19 IS NULL ");
							}
						}
					}
					else
					{
						pstmt.setString(25,arrPhyAttrib19.get(phyAttNo).toString());
						sql2.append("AND PHY_ATTRIB_19 = '"+arrPhyAttrib19.get(phyAttNo).toString()+"' ");
					}
					if (arrPhyAttrib20.get(phyAttNo) == null || !(genAttNo.contains("20")))
					{
						if (thWeightCol.equalsIgnoreCase("phy_attrib_20"))
						{
							pstmt.setDouble(26,thWeight);
						}
						else
						{
							pstmt.setNull(26,java.sql.Types.VARCHAR);						
							if (genAttNo.contains("20"))
							{
								sql2.append("AND PHY_ATTRIB_20 IS NULL ");
							}
						}
					}
					else
					{
						pstmt.setString(26,arrPhyAttrib20.get(phyAttNo).toString());
						sql2.append("AND PHY_ATTRIB_20 = '"+arrPhyAttrib20.get(phyAttNo).toString()+"' ");
					}
					if (arrPhyAttrib21.get(phyAttNo) == null || !(genAttNo.contains("21")))
					{
						if (thWeightCol.equalsIgnoreCase("phy_attrib_21"))
						{
							pstmt.setDouble(27,thWeight);
						}
						else
						{
							pstmt.setNull(27,java.sql.Types.VARCHAR);						
							if (genAttNo.contains("21"))
							{
								sql2.append("AND PHY_ATTRIB_21 IS NULL ");
							}
						}
					}
					else
					{
						pstmt.setString(27,arrPhyAttrib21.get(phyAttNo).toString());
						sql2.append("AND PHY_ATTRIB_21 = '"+arrPhyAttrib21.get(phyAttNo).toString()+"' ");
					}
					if (arrPhyAttrib22.get(phyAttNo) == null || !(genAttNo.contains("22")))
					{
						if (thWeightCol.equalsIgnoreCase("phy_attrib_22"))
						{
							pstmt.setDouble(28,thWeight);
						}
						else
						{
							pstmt.setNull(28,java.sql.Types.VARCHAR);							
							if (genAttNo.contains("22"))
							{
								sql2.append("AND PHY_ATTRIB_22 IS NULL ");
							}
						}
					}
					else
					{
						pstmt.setString(28,arrPhyAttrib22.get(phyAttNo).toString());
						sql2.append("AND PHY_ATTRIB_22 = '"+arrPhyAttrib22.get(phyAttNo).toString()+"' ");
					}

					pstmt.setString(29,"Y");
					pstmt.setTimestamp(30,new java.sql.Timestamp(System.currentTimeMillis()));
					pstmt.setString(31,chgUser);
					pstmt.setString(32,chgTerm);
					pstmt.setString(33,siteCode);
					pstmt.setString(34,unit);
					pstmt.setString(35,unitRate);
					pstmt.setString(36,unitRate);
					pstmt.setString(37,"D");
					
					try
					{
						int upd;
						String itemCode2 = "";
						stmt = conn.createStatement();
						System.out.println("SQL "+sql2.toString());
						rs = stmt.executeQuery(sql2.toString());
						if (rs.next())
						{
							itemCode2 = rs.getString("ITEM_CODE");
						}

						System.out.println("itemCode2 :: "+itemCode2);

						if (itemCode2 != null && itemCode2.trim().length() > 0)
						{
							newItemCode = itemCode2;
							itemCode = itemCode2;
						}
						else
						{
							String itemDescr = getItemDescription(tranID,lineNo,itemSer,itemType,conn);
							
							TransIDGenerator tg = new TransIDGenerator(XMLString, userId, CommonConstants.DB_NAME);
							itemCode = tg.generateTranSeqID(tranSer, keyCol, keyString, conn);
				
							System.out.println("Item Code Generated :: "+itemCode);
							System.out.println("Generated Description :: "+itemDescr);

							pstmt.setString(1,itemCode);
							pstmt.setString(2,itemDescr);
							
							upd = pstmt.executeUpdate();//Item Entry
							if (upd > 0)
							{
								sql = "INSERT INTO SITEITEM (SITE_CODE,ITEM_CODE,MIN_QTY,MAX_QTY,"+
									"REO_QTY,REO_LEV,PUR_LEAD_TIME,CHG_DATE,CHG_USER,CHG_TERM,AUTO_REQC) "+
									"VALUES(?,?,?,?,?,?,?,?,?,?,?)";
								
								System.out.println("SQL ::"+sql);
								PreparedStatement pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1,siteCode);
								pstmt1.setString(2,itemCode);
								pstmt1.setInt(3,0);
								pstmt1.setInt(4,0);
								pstmt1.setInt(5,0);
								pstmt1.setInt(6,0);
								pstmt1.setInt(7,0);
								pstmt1.setTimestamp(8,new java.sql.Timestamp(System.currentTimeMillis()));
								pstmt1.setString(9,chgUser);
								pstmt1.setString(10,chgTerm);
								pstmt1.setString(11,"Y");							
								
								upd = pstmt1.executeUpdate();
								pstmt1.clearParameters();
							}
							upd = 0;
							tg = null;
						}
						orderDetail[phyAttNo].put(itemCode,colourValue);
						System.out.println("Query Executed ::");
					}
					catch(Exception e)
					{
						conn.rollback();
						System.out.println("Exception Inserting Into Item ::"+e);
						retString =	itmDBAccess.getErrorString("","VTCREDIT01","","",conn);
						e.printStackTrace();
						return retString;
					}
					if (flag && itemCode != null && itemCode.trim().length() > 0)
					{
						stmt = conn.createStatement();
						sql = "UPDATE SORDFORM_ATT_DET SET ITEM_CODE = '"+itemCode+"' WHERE TRAN_ID = '"+tranID+"' AND LINE_NO = '"+lineNo+"'";
						System.out.println("Update SQL - "+sql);
						stmt.executeUpdate(sql);
						stmt.close();
						flag = false;
					}
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("Error In Generating Key ::==> "+e);
			e.printStackTrace();
			retString =	itmDBAccess.getErrorString("","VTCREDIT01","","",conn);
			return retString;
		}
		finally
		{
			try
			{
				if (stmt != null)
				{
					rs.close();
					stmt.close();
				}
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
			}
			catch(Exception e){}
		}
		return retString;
	}


	private String getItemDescription(String tranID,String lineNo,String itemSer,String itemType,Connection conn) throws Exception
	{
		Statement stmt = null;
		ResultSet rs = null;
		String columnName = "GSM"; 
		StringBuffer sql = new StringBuffer("SELECT ");
		String gsmCol,cutYN,lenCol,unitLenCol,widthCol,unitWidthCol,heightCol,unitHeightCol,centerHeightCol,unitCHCol,color;
		
		String itemDescr = "";
		try
		{
			gsmCol = getAttributeColumn("GSM",itemType,conn);
			if (gsmCol == null || gsmCol.trim().length() == 0)
			{
				gsmCol = getAttributeColumn("Micron",itemType,conn);
				if (gsmCol != null && gsmCol.trim().length() > 0)
				{
					columnName = "Micron"; 
				}
			}
			cutYN = getAttributeColumn("Cut/Fin",itemType,conn);
			lenCol = getAttributeColumn("Length",itemType,conn);
			unitLenCol = getAttributeColumn("Unit (L)",itemType,conn);
			widthCol = getAttributeColumn("Width",itemType,conn);
			unitWidthCol = getAttributeColumn("Unit (W)",itemType,conn);
			heightCol = getAttributeColumn("Height",itemType,conn);
			unitHeightCol = getAttributeColumn("Unit (H)",itemType,conn);
			centerHeightCol = getAttributeColumn("Center Height",itemType,conn);
			unitCHCol = getAttributeColumn("Unit (CH)",itemType,conn);
			
			//color = getAttributeColumn("GSM",itemType,conn);

			if (gsmCol != null && gsmCol.trim().length() > 0)
			{
				sql.append(gsmCol+","); 
			}
			if (cutYN != null && cutYN.trim().length() > 0)
			{
				sql.append(cutYN+","); 
			}
			if (lenCol != null && lenCol.trim().length() > 0)
			{
				sql.append(lenCol+","); 
			}
			if (unitLenCol != null && unitLenCol.trim().length() > 0)
			{
				sql.append(unitLenCol+","); 
			}
			if (widthCol != null && widthCol.trim().length() > 0)
			{
				sql.append(widthCol+","); 
			}
			if (unitWidthCol != null && unitWidthCol.trim().length() > 0)
			{
				sql.append(unitWidthCol+","); 
			}
			if (heightCol != null && heightCol.trim().length() > 0)
			{
				sql.append(heightCol+","); 
			}
			if (unitHeightCol != null && unitHeightCol.trim().length() > 0)
			{
				sql.append(unitHeightCol+","); 
			}
			if (centerHeightCol != null && centerHeightCol.trim().length() > 0)
			{
				sql.append(centerHeightCol+","); 
			}
			if (centerHeightCol != null && centerHeightCol.trim().length() > 0)
			{
				sql.append(centerHeightCol); 
			}

			sql.append(" FROM SORDFORM_ATT_DET WHERE TRAN_ID = '"+tranID+"' AND LINE_NO = '"+lineNo+"'");
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql.toString());

			if (rs.next())
			{
				itemDescr = itemDescr + itemSer.substring(0,3)+" ";
				itemDescr = itemDescr + ((rs.getString(1) == null) ? "":rs.getString(1))+" "+columnName;
				String cut = rs.getString(2);
				if (cut != null && cut.trim().length() > 0)
				{
					if (cut.equalsIgnoreCase("C"))
					{
						itemDescr = itemDescr + " Cut Size";
					}
					else if (cut.equalsIgnoreCase("F"))
					{
						itemDescr = itemDescr + " Finish Size";
					}
				}
				itemDescr = itemDescr +" Length "+((rs.getString(3) == null) ? "":rs.getString(3)) + " ";
				itemDescr = itemDescr + ((rs.getString(4) == null) ? "":rs.getString(4)) + " ";
				itemDescr = itemDescr +"Width "+((rs.getString(5) == null) ? "":rs.getString(5)) + " ";
				itemDescr = itemDescr + ((rs.getString(6) == null) ? "":rs.getString(6)) + " ";
				itemDescr = itemDescr +"Height "+((rs.getString(7) == null) ? "":rs.getString(7)) + " ";
				itemDescr = itemDescr +((rs.getString(8) == null) ? "":rs.getString(8)) + " ";
				itemDescr = itemDescr +"CH "+((rs.getString(9) == null) ? "":rs.getString(9)) + " ";
				itemDescr = itemDescr + ((rs.getString(10 ) == null) ? "":rs.getString(10));
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception in getItemDescription "+e.getMessage());
			e.printStackTrace();
			throw(e);
		}
		return itemDescr;
	}

	private String getAttributeColumn(String attributeName,String itemTYpe,Connection conn)
	{
		Statement stmt = null; 
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		String columnName = "",columnValue = "";
		int noOfColumn;
		HashMap hashAttrib = new HashMap();
		try
		{
			
			stmt = conn.createStatement();
			String sql = "SELECT PHY_ATTRIB_1,PHY_ATTRIB_2,PHY_ATTRIB_3,PHY_ATTRIB_4,PHY_ATTRIB_5, "+
				"PHY_ATTRIB_6,PHY_ATTRIB_7,PHY_ATTRIB_8,PHY_ATTRIB_9,PHY_ATTRIB_10,PHY_ATTRIB_11, "+
				"PHY_ATTRIB_12,PHY_ATTRIB_13,PHY_ATTRIB_14,PHY_ATTRIB_15,PHY_ATTRIB_16,PHY_ATTRIB_17, "+
				"PHY_ATTRIB_18,PHY_ATTRIB_19,PHY_ATTRIB_20,PHY_ATTRIB_21,PHY_ATTRIB_22 "+
				"FROM ITEM_TYPE WHERE ITEM_TYPE = '"+itemTYpe+"'";
			rs = stmt.executeQuery(sql);	
			rsmd = rs.getMetaData();
			if (rs.next())
			{
				noOfColumn = rsmd.getColumnCount();
				for (int i=1;i<=noOfColumn ;i++ )
				{
					columnValue = rs.getString(i);
					if (columnValue != null && columnValue.trim().equalsIgnoreCase(attributeName))
					{
						columnName = rsmd.getColumnName(i);		
					}
				}
			}
			System.out.println("Column Name :: "+columnName);
		}
		catch (Exception e)
		{
			System.out.println("Exception in attributeColumn :: "+e.getMessage());
			e.printStackTrace();
		}
		return columnName;	
	}

	private String generateSaleOrder(String tranID,String saleOrder,String xtraParams,Connection conn)
	{										
		PreparedStatement pstmt = null;
		Statement stmt = null;
		ResultSet rs = null;
		String sql,preparesql;

		String chgUser,chgTerm;

		java.util.Date ordDate = null;
		try
		{	
			
			stmt = conn.createStatement();
			chgUser = new  ibase.utility.E12GenericUtility().getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			chgTerm = new  ibase.utility.E12GenericUtility().getValueFromXTRA_PARAMS(xtraParams,"termId");
			sql = "SELECT ORD_DATE,ORDER_TYPE,SITE_CODE,CUST_CODE,ITEM_SER,CUST_PORD,PORD_DATE,PRICE_LIST,"+
				"PL_DATE,CURR_CODE,EXCH_RATE,REMARKS,DUE_DATE,STATUS,STATUS_DATE,"+
				"STATUS_REMARKS,TRAN_CODE,TRANS_MODE,CHG_DATE,CHG_USER,CHG_TERM,"+
				"PRICE_LIST__CLG,TAX_DATE,CUST_CODE__DLV,CUST_CODE__BIL,TAX_OPT,SALES_PERS,COMM_PERC,TAX_CLASS,"+
				"TAX_CHAP,TAX_ENV,CR_TERM,QUOT_NO,PROM_DATE,DLV_ADD1,DLV_ADD2,DLV_ADD3,DLV_CITY,STATE_CODE__DLV,"+
				"COUNT_CODE__DLV,DLV_PIN,STAN_CODE,PART_QTY,CONSUME_FC,PROJ_CODE,DLV_TERM,FRT_AMT,CURR_CODE__FRT,"+
				"EXCH_RATE__FRT,ALLOC_FLAG,FRT_TERM,CONTRACT_NO,EMP_CODE__ORD,INV_AMT,ADV_PERC,DIST_ROUTE,COMM_PERC__ON,"+
				"COMM_AMT,CURR_CODE__COMM,SALES_PERS__1,COMM_PERC_1,COMM_PERC_ON_1,CURR_CODE__COMM_1,SALES_PERS__2,"+
				"COMM_PERC_2,COMM_PERC_ON_2,CURR_CODE__COMM_2,RCP_MODE,BANK_CODE,ORDER_MODE,UDF__STR1,UDF__STR2,UDF__NUM1,"+
				"UDF__NUM2,UDF__DATE1,OFFSHORE_INVOICE,LABEL_TYPE,OUTSIDE_INSPECTION,REMARKS2,REMARKS3,STAN_CODE__INIT,"+
				"CURR_CODE__INS,EXCH_RATE__INS,INS_AMT,DLV_TO,ACCT_CODE__SAL,CCTR_CODE__SAL,TEL1__DLV,TEL2__DLV,TEL3__DLV,"+
				"FAX__DLV,EXCH_RATE__COMM,EXCH_RATE__COMM_1,EXCH_RATE__COMM_2,PRICE_LIST__DISC,MARKET_REG,HAZARD_YN,SN_CODE,"+
				"SALES_PERS_COMM_1,SALES_PERS_COMM_2,SALES_PERS_COMM_3,TOT_ORD_VALUE,MAX_ORDER_VALUE,COMM_AMT__OC,LOC_GROUP,"+
				"FIN_SCHEME,SITE_CODE__SHIP,CHEQUE_DATE,CHEQUE_NO,FOB_VALUE,PORD_MODE,TERR_CODE,CHQ_NAME,CHQ_AMOUNT,REV__TRAN,"+
				"PARENT__TRAN_ID,CUST_CODE__END,SALE_ORDER__END,ORDER_DB FROM SORDFORM_ATT WHERE TRAN_ID = '"+tranID+"'";
			rs = stmt.executeQuery(sql);
			if (rs.next())
			{
				preparesql = "INSERT INTO SORDER (SALE_ORDER,ORDER_DATE,ORDER_TYPE,SITE_CODE,CUST_CODE,ITEM_SER,"+
					"CUST_PORD,PORD_DATE,PRICE_LIST,PL_DATE,CURR_CODE,EXCH_RATE,REMARKS,DUE_DATE,STATUS,"+
					"STATUS_DATE,STATUS_REMARKS,TRAN_CODE,TRANS_MODE,CHG_DATE,CHG_USER,CHG_TERM,PRICE_LIST__CLG,"+
					"TAX_DATE,CUST_CODE__DLV,CUST_CODE__BIL,TAX_OPT,SALES_PERS,COMM_PERC,TAX_CLASS,"+
					"TAX_CHAP,TAX_ENV,CR_TERM,QUOT_NO,PROM_DATE,DLV_ADD1,DLV_ADD2,DLV_ADD3,DLV_CITY,STATE_CODE__DLV,"+
					"COUNT_CODE__DLV,DLV_PIN,STAN_CODE,PART_QTY,CONSUME_FC,PROJ_CODE,DLV_TERM,FRT_AMT,CURR_CODE__FRT,"+
					"EXCH_RATE__FRT,ALLOC_FLAG,FRT_TERM,CONTRACT_NO,EMP_CODE__ORD,INV_AMT,ADV_PERC,DIST_ROUTE,COMM_PERC__ON,"+
					"COMM_AMT,CURR_CODE__COMM,SALES_PERS__1,COMM_PERC_1,COMM_PERC_ON_1,CURR_CODE__COMM_1,SALES_PERS__2,"+
					"COMM_PERC_2,COMM_PERC_ON_2,CURR_CODE__COMM_2,RCP_MODE,BANK_CODE,ORDER_MODE,UDF__STR1,UDF__STR2,UDF__NUM1,"+
					"UDF__NUM2,UDF__DATE1,OFFSHORE_INVOICE,LABEL_TYPE,OUTSIDE_INSPECTION,REMARKS2,REMARKS3,STAN_CODE__INIT,"+
					"CURR_CODE__INS,EXCH_RATE__INS,INS_AMT,DLV_TO,ACCT_CODE__SAL,CCTR_CODE__SAL,TEL1__DLV,TEL2__DLV,TEL3__DLV,"+
					"FAX__DLV,EXCH_RATE__COMM,EXCH_RATE__COMM_1,EXCH_RATE__COMM_2,PRICE_LIST__DISC,MARKET_REG,HAZARD_YN,SN_CODE,"+
					"SALES_PERS_COMM_1,SALES_PERS_COMM_2,SALES_PERS_COMM_3,TOT_ORD_VALUE,MAX_ORDER_VALUE,COMM_AMT__OC,LOC_GROUP,"+
					"FIN_SCHEME,SITE_CODE__SHIP,CHEQUE_DATE,CHEQUE_NO,FOB_VALUE,PORD_MODE,TERR_CODE,CHQ_NAME,CHQ_AMOUNT,REV__TRAN,"+
					"PARENT__TRAN_ID,CUST_CODE__END,SALE_ORDER__END,ORDER_DB) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"+
					"?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"+
					"?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				
				System.out.println("Insert SQL "+preparesql);
				pstmt = conn.prepareStatement(preparesql);	
				

				pstmt.setString(1,saleOrder);

				java.util.Date tempDate = rs.getDate("ORD_DATE");
				SimpleDateFormat sdf = new SimpleDateFormat(new  ibase.utility.E12GenericUtility().getApplDateFormat());
				String tDate = new  ibase.utility.E12GenericUtility().getValidDateTimeString(sdf.format(tempDate),new  ibase.utility.E12GenericUtility().getApplDateFormat(),new  ibase.utility.E12GenericUtility().getDBDateFormat());
				pstmt.setTimestamp(2,java.sql.Timestamp.valueOf(tDate));
				System.out.println("ORDER DATE "+java.sql.Timestamp.valueOf(tDate));
				pstmt.setString(3,rs.getString("ORDER_TYPE"));
				pstmt.setString(4,rs.getString("SITE_CODE"));
				System.out.println("SITE CODE "+rs.getString("SITE_CODE"));
				pstmt.setString(5,rs.getString("CUST_CODE"));
				System.out.println("CUST CODE "+rs.getString("CUST_CODE"));
				pstmt.setString(6,rs.getString("ITEM_SER"));
				System.out.println("ITEM SER "+rs.getString("ITEM_SER"));
				pstmt.setString(7,rs.getString("CUST_PORD"));
				tempDate = rs.getDate("PORD_DATE");
				if (tempDate != null)
				{
					tDate = new  ibase.utility.E12GenericUtility().getValidDateTimeString(sdf.format(tempDate),new  ibase.utility.E12GenericUtility().getApplDateFormat(),new  ibase.utility.E12GenericUtility().getDBDateFormat());
					pstmt.setTimestamp(8,java.sql.Timestamp.valueOf(tDate));
				}
				else
				{
					pstmt.setNull(8,java.sql.Types.TIMESTAMP);
				}
				pstmt.setString(9,rs.getString("PRICE_LIST"));
				tempDate = rs.getDate("PL_DATE");
				tDate = new  ibase.utility.E12GenericUtility().getValidDateTimeString(sdf.format(tempDate),new  ibase.utility.E12GenericUtility().getApplDateFormat(),new  ibase.utility.E12GenericUtility().getDBDateFormat());
				pstmt.setTimestamp(10,java.sql.Timestamp.valueOf(tDate));
				pstmt.setString(11,rs.getString("CURR_CODE"));
				System.out.println("CURR CODE "+rs.getString("CURR_CODE"));
				pstmt.setString(12,rs.getString("EXCH_RATE"));
				System.out.println("EXCH RATE "+rs.getString("EXCH_RATE"));
				pstmt.setString(13,rs.getString("REMARKS"));
				tempDate = rs.getDate("DUE_DATE");
				tDate = new  ibase.utility.E12GenericUtility().getValidDateTimeString(sdf.format(tempDate),new  ibase.utility.E12GenericUtility().getApplDateFormat(),new  ibase.utility.E12GenericUtility().getDBDateFormat());
				pstmt.setTimestamp(14,java.sql.Timestamp.valueOf(tDate));
				pstmt.setString(15,rs.getString("STATUS"));
				tempDate = rs.getDate("STATUS_DATE");
				tDate = new  ibase.utility.E12GenericUtility().getValidDateTimeString(sdf.format(tempDate),new  ibase.utility.E12GenericUtility().getApplDateFormat(),new  ibase.utility.E12GenericUtility().getDBDateFormat());
				pstmt.setTimestamp(16,java.sql.Timestamp.valueOf(tDate));
				pstmt.setString(17,rs.getString("STATUS_REMARKS"));
				pstmt.setString(18,rs.getString("TRAN_CODE"));
				pstmt.setString(19,rs.getString("TRANS_MODE"));
				System.out.println("TRANS MODE "+rs.getString("TRANS_MODE"));
				pstmt.setTimestamp(20,new java.sql.Timestamp(System.currentTimeMillis()));
				pstmt.setString(21,chgUser);
				pstmt.setString(22,chgTerm); 
				System.out.println("Chg Date "+new java.sql.Timestamp(System.currentTimeMillis()));
				System.out.println("chg User "+chgUser);
				System.out.println("chg Term "+chgTerm);
				pstmt.setString(23,rs.getString("PRICE_LIST__CLG"));
				tempDate = rs.getDate("TAX_DATE");
				tDate = new  ibase.utility.E12GenericUtility().getValidDateTimeString(sdf.format(tempDate),new  ibase.utility.E12GenericUtility().getApplDateFormat(),new  ibase.utility.E12GenericUtility().getDBDateFormat());
				pstmt.setTimestamp(24,java.sql.Timestamp.valueOf(tDate));
				pstmt.setString(25,rs.getString("CUST_CODE__DLV"));
				System.out.println("CUST CODE DLV "+rs.getString("CUST_CODE__DLV"));
				pstmt.setString(26,rs.getString("CUST_CODE__BIL"));
				System.out.println("CUST CODE BIL "+rs.getString("CUST_CODE__BIL"));
				pstmt.setString(27,rs.getString("TAX_OPT")); 
				pstmt.setString(28,rs.getString("SALES_PERS"));
				pstmt.setString(29,rs.getString("COMM_PERC"));
				pstmt.setString(30,rs.getString("TAX_CLASS"));
				pstmt.setString(31,rs.getString("TAX_CHAP"));
				pstmt.setString(32,rs.getString("TAX_ENV"));
				pstmt.setString(33,rs.getString("CR_TERM"));
				System.out.println("CR TERM "+rs.getString("CR_TERM"));
				pstmt.setString(34,rs.getString("QUOT_NO"));
				tempDate = rs.getDate("PROM_DATE");
				if (tempDate != null)
				{
					tDate = new  ibase.utility.E12GenericUtility().getValidDateTimeString(sdf.format(tempDate),new  ibase.utility.E12GenericUtility().getApplDateFormat(),new  ibase.utility.E12GenericUtility().getDBDateFormat());
					pstmt.setTimestamp(35,java.sql.Timestamp.valueOf(tDate));
				}
				else
				{
					pstmt.setNull(35,java.sql.Types.TIMESTAMP);
				}
				pstmt.setString(36,rs.getString("DLV_ADD1"));
				pstmt.setString(37,rs.getString("DLV_ADD2"));
				pstmt.setString(38,rs.getString("DLV_ADD3"));
				pstmt.setString(39,rs.getString("DLV_CITY"));
				pstmt.setString(40,rs.getString("STATE_CODE__DLV"));
				pstmt.setString(41,rs.getString("COUNT_CODE__DLV"));
				pstmt.setString(42,rs.getString("DLV_PIN"));
				pstmt.setString(43,rs.getString("STAN_CODE"));
				pstmt.setString(44,rs.getString("PART_QTY"));
				System.out.println("rs.getString(CONSUME_FC) "+rs.getString("CONSUME_FC"));
				pstmt.setString(45,(rs.getString("CONSUME_FC") == null || rs.getString("CONSUME_FC").trim().length() == 0)? "N":rs.getString("CONSUME_FC"));
				pstmt.setString(46,rs.getString("PROJ_CODE"));
				pstmt.setString(47,rs.getString("DLV_TERM"));
				System.out.println("DLV TERM "+rs.getString("DLV_TERM"));
				pstmt.setDouble(48,rs.getDouble("FRT_AMT"));
				pstmt.setString(49,rs.getString("CURR_CODE__FRT"));
				pstmt.setString(50,rs.getString("EXCH_RATE__FRT"));
				pstmt.setString(51,rs.getString("ALLOC_FLAG"));
				pstmt.setString(52,rs.getString("FRT_TERM"));
				pstmt.setString(53,rs.getString("CONTRACT_NO"));
				pstmt.setString(54,rs.getString("EMP_CODE__ORD"));
				pstmt.setString(55,rs.getString("INV_AMT"));
				pstmt.setString(56,rs.getString("ADV_PERC"));
				pstmt.setString(57,rs.getString("DIST_ROUTE"));
				pstmt.setString(58,rs.getString("COMM_PERC__ON"));
				pstmt.setString(59,rs.getString("COMM_AMT"));
				pstmt.setString(60,rs.getString("CURR_CODE__COMM"));
				pstmt.setString(61,rs.getString("SALES_PERS__1"));

				pstmt.setString(62,rs.getString("COMM_PERC_1"));
				pstmt.setString(63,rs.getString("COMM_PERC_ON_1"));
				pstmt.setString(64,rs.getString("CURR_CODE__COMM_1"));
				pstmt.setString(65,rs.getString("SALES_PERS__2"));
				pstmt.setString(66,rs.getString("COMM_PERC_2"));
				pstmt.setString(67,rs.getString("COMM_PERC_ON_2"));
				pstmt.setString(68,rs.getString("CURR_CODE__COMM_2"));
				pstmt.setString(69,rs.getString("RCP_MODE"));
				pstmt.setString(70,rs.getString("BANK_CODE"));
				pstmt.setString(71,rs.getString("ORDER_MODE")); // To be checked
				pstmt.setString(72,rs.getString("UDF__STR1"));
				pstmt.setString(73,rs.getString("UDF__STR2"));
				pstmt.setString(74,rs.getString("UDF__NUM1"));
				pstmt.setString(75,rs.getString("UDF__NUM2"));
				tempDate = rs.getDate("UDF__DATE1");
				if (tempDate != null)
				{
					tDate = new  ibase.utility.E12GenericUtility().getValidDateTimeString(sdf.format(tempDate),new  ibase.utility.E12GenericUtility().getApplDateFormat(),new  ibase.utility.E12GenericUtility().getDBDateFormat());
					pstmt.setTimestamp(76,java.sql.Timestamp.valueOf(tDate));
				}
				else
				{
					pstmt.setNull(76,java.sql.Types.TIMESTAMP);
				}
				pstmt.setString(77,rs.getString("OFFSHORE_INVOICE"));
				pstmt.setString(78,rs.getString("LABEL_TYPE"));
				pstmt.setString(79,rs.getString("OUTSIDE_INSPECTION"));
				pstmt.setString(80,rs.getString("REMARKS2"));
				pstmt.setString(81,rs.getString("REMARKS3"));
				pstmt.setString(82,rs.getString("STAN_CODE__INIT"));
				pstmt.setString(83,rs.getString("CURR_CODE__INS"));
				pstmt.setString(84,rs.getString("EXCH_RATE__INS"));
				pstmt.setString(85,rs.getString("INS_AMT"));

				pstmt.setString(86,rs.getString("DLV_TO"));
				pstmt.setString(87,rs.getString("ACCT_CODE__SAL"));
				pstmt.setString(88,rs.getString("CCTR_CODE__SAL"));
				pstmt.setString(89,rs.getString("TEL1__DLV"));
				pstmt.setString(90,rs.getString("TEL2__DLV"));
				pstmt.setString(91,rs.getString("TEL3__DLV"));
				pstmt.setString(92,rs.getString("FAX__DLV"));
				pstmt.setString(93,rs.getString("EXCH_RATE__COMM"));
				pstmt.setString(94,rs.getString("EXCH_RATE__COMM_1"));
				pstmt.setString(95,rs.getString("EXCH_RATE__COMM_2"));
				pstmt.setString(96,rs.getString("PRICE_LIST__DISC"));
				pstmt.setString(97,rs.getString("MARKET_REG"));
				pstmt.setString(98,rs.getString("HAZARD_YN"));
				pstmt.setString(99,rs.getString("SN_CODE"));

				pstmt.setString(100,rs.getString("SALES_PERS_COMM_1"));
				pstmt.setString(101,rs.getString("SALES_PERS_COMM_2"));
				pstmt.setString(102,rs.getString("SALES_PERS_COMM_3"));
				pstmt.setString(103,rs.getString("TOT_ORD_VALUE"));
				pstmt.setString(104,rs.getString("MAX_ORDER_VALUE"));
				pstmt.setString(105,rs.getString("COMM_AMT__OC"));
				pstmt.setString(106,rs.getString("LOC_GROUP"));
				pstmt.setString(107,rs.getString("FIN_SCHEME"));
				pstmt.setString(108,rs.getString("SITE_CODE__SHIP"));
				tempDate = rs.getDate("CHEQUE_DATE");
				if (tempDate != null)
				{
					tDate = new  ibase.utility.E12GenericUtility().getValidDateTimeString(sdf.format(tempDate),new  ibase.utility.E12GenericUtility().getApplDateFormat(),new  ibase.utility.E12GenericUtility().getDBDateFormat());
					pstmt.setTimestamp(109,java.sql.Timestamp.valueOf(tDate));
				}
				else
				{
					pstmt.setNull(109,java.sql.Types.TIMESTAMP);
				}
				pstmt.setString(110,rs.getString("CHEQUE_NO"));
				pstmt.setString(111,rs.getString("FOB_VALUE"));
				pstmt.setString(112,rs.getString("PORD_MODE"));
				pstmt.setString(113,rs.getString("TERR_CODE"));
				pstmt.setString(114,rs.getString("CHQ_NAME"));
				pstmt.setString(115,rs.getString("CHQ_AMOUNT"));
				pstmt.setString(116,rs.getString("REV__TRAN"));
				pstmt.setString(117,rs.getString("PARENT__TRAN_ID"));
				pstmt.setString(118,rs.getString("CUST_CODE__END"));
				pstmt.setString(119,rs.getString("SALE_ORDER__END"));
				pstmt.setString(120,rs.getString("ORDER_DB"));

				pstmt.executeUpdate();

			}
		}
		catch(Exception e)
		{
			System.out.println("Exception in generateSaleOrder :: "+e.getMessage());
			e.printStackTrace();
		} 
		finally
		{
			try
			{
				stmt.close();
				pstmt.close();
			}
			catch(Exception e){}
		}
		return "";
	}
}