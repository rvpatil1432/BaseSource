package ibase.webitm.ejb.dis;

import ibase.webitm.utility.*;
import ibase.utility.*;
import ibase.webitm.ejb.*;
import ibase.system.config.*;

import java.rmi.RemoteException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.*;
import java.util.*;
import java.math.*;
import java.sql.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.ejb.*;
import javax.naming.InitialContext;
import java.io.*;
import java.io.File;
import java.sql.*;
import ibase.webitm.utility.TransIDGenerator;//TID
import ibase.utility.CommonConstants;//TID

import java.lang.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import org.xml.sax.InputSource;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for 3
public class VerifyAll implements VerifyAllLocal, VerifyAllRemote
{
	SessionContext cSessionContext;
	String userId = "";
	
	//ibase.webitm.utility.GenericUtility genericUtility = ibase.webitm.utility.GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
	ConnDriver connDriver = new ConnDriver();
	CommonConstants commonConstants;

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

	public void setSessionContext(SessionContext mSessionContext)
	{
		this.cSessionContext = mSessionContext;
	}
	
	public String verifyAll(String tranId)
	{
		String result = "";
		try
		{
			System.out.println("Tran Id .ejb..ejb.."+tranId);
			result = verify(tranId);
		}
		catch(Exception e)
		{
			System.out.println("Exception while AutoVerification "+e);
		}
		return result;
	}
	public String verify(String cbtranId) throws RemoteException
	{
		String errString = "";
		String errCode = "";
		String descr = "";
		Document dom = null;
		String vFlag = "";
		Connection conn = null;
		Statement stmt = null;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		PreparedStatement pstmt2 = null;
		ResultSet rsdet = null;
		ResultSet rs = null;
		StringBuffer retString = new StringBuffer();
		retString.append("");
		StringBuffer xmlString = new StringBuffer();
		java.sql.Date tranDate = null;
		String sql = "";
		String sqlInstr = "";
		String sqldet = ""; 
		String loginSite = "";
		String tranId = "";
		String siteCode = "";
		String custCode = "";
		String custCodeCrdt = "";
		String intVendorNo	 = "";
		String priceList = "";
		String currCode = "";
		String porderNo = "";
		String contractNo = "";
		String itemCode = "";
		String buyPrdCode = "";
		String lineCtr = "";
		String custCodeEnd = "";
		double exchRate = 0.0;
		double amount = 0.0;
		double netAmt = 0.0;
		double rateCtr = 0.0;
		double dbcountRate = 0.0;
		double dbsellRate = 0.0;
		double ordQty = 0.0;
		double oldQty = 0.0;
		double usedQty = 0.0;
		double scQty = 0.0;
		double retPQtyR = 0.0;
		double retPQtyP = 0.0;
		double custQty = 0.0;
		double netSell = 0.0;
		double chargeBakQty = 0.0;
		double invQty = 0.0;
		double rateSell = 0.0;
		int lineNo = 0;
		int cnt = 0;;
		int count = 0;
		
		try
		{
			conn = connDriver.getConnectDB("DriverITM");
			//conn = getConnection();
		}
		catch(Exception ex)
		{
			System.out.println("[Exception :][02]"+ex);
		}
					
		try
		{ 
			sql = "SELECT CASE WHEN SUM(AMOUNT) IS NULL THEN 0 ELSE SUM(AMOUNT) END, "
				+ "CASE WHEN SUM(NET_AMT) IS NULL THEN 0 ELSE SUM(NET_AMT) END FROM CHARGE_BACK_DET WHERE TRAN_ID = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,cbtranId.trim());
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				amount = rs.getDouble(1);
				netAmt = rs.getDouble(2);
				System.out.println("amount.ejb.."+amount);
			}
			rs.close();
			pstmt.close();
		}
		catch(SQLException se)
		{
			System.out.println("SQLException [01]:[VerifyAllEJB][Excuting Query Failed]" + sql +se.getMessage());
		}
		catch(Exception ex)
		{
			System.out.println("Exception [02]::"+ex.getMessage());
			ex.printStackTrace();
		}	
		try
		{
			sql = "UPDATE CHARGE_BACK SET AMOUNT = ? ,NET_AMT = ? WHERE TRAN_ID = ? ";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setDouble(1,amount);
			pstmt.setDouble(2,netAmt);
			pstmt.setString(3,cbtranId.trim());
			System.out.println("Charge Back TranId.ejb.."+cbtranId);
			int i = pstmt.executeUpdate();
			System.out.println("NO OF RECORDS UPDATED IN CHARGE_BACK WITH AMOUNT "+amount+"IS .ejb..ejb..ejb."+i);
			pstmt.close();
		}
		catch(SQLException se)
		{
			System.out.println("SQLException []:[Insert Query Failed]" + sql +se.getMessage());
		}
		catch(Exception ex)
		{
			System.out.println("Exception [Insert Query Failed]::"+ex.getMessage());
			ex.printStackTrace();
		}
		try
		{
			sql = "SELECT SITE_CODE,CUST_CODE,CUST_CODE__CREDIT,PRICE_LIST,CURR_CODE,EXCH_RATE,PORDER_NO,INT_VENDOR_NO,CHG_USER,TRAN_DATE "
				+ "FROM CHARGE_BACK WHERE TRAN_ID = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,cbtranId.trim());
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				siteCode = rs.getString(1);
				custCode = rs.getString(2);	
				custCodeCrdt = rs.getString(3);
				priceList = rs.getString(4);
				currCode = rs.getString(5);
				exchRate = rs.getDouble(6);
				porderNo = rs.getString(7);
				intVendorNo = rs.getString(8);
				userId = rs.getString(9);
				tranDate = rs.getDate(10);
				if(siteCode == null)
				{
					siteCode = "";
				}
				System.out.println(".ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb.");
				System.out.println("VALUES FROM CHARGE BACK HEADER.ejb..ejb..ejb..ejb..ejb.");
				System.out.println("Site Code.ejb..ejb..ejb.."+siteCode);
				if(custCode == null)
				{
					custCode = "";
				}
				System.out.println("Cust Code.ejb..ejb.."+custCode);
				if(custCodeCrdt == null)
				{
					custCodeCrdt = "";
				}
				System.out.println("Cust Code Crdt.ejb..ejb.."+custCodeCrdt);
				if(priceList == null)
				{
					priceList = "";
				}
				System.out.println("Price List.ejb..ejb.."+priceList);
				if(currCode == null)
				{
					currCode = "";
				}
				System.out.println("Curr Code.ejb..ejb.."+currCode);
				System.out.println("Exch Rate.ejb..ejb.."+exchRate);
				if(porderNo == null)
				{
					porderNo = "";
				}
				System.out.println("Porder No.ejb..ejb.."+porderNo);
				if(intVendorNo == null)
				{
					intVendorNo = "";
				}
				System.out.println("Int Vendor No.ejb..ejb.."+intVendorNo);
				System.out.println("User.ejb..ejb.."+userId);
				System.out.println("TranDate.ejb..ejb.."+tranDate);
			}
			rs.close();
			pstmt.close();
		}//try
		catch(SQLException se)
		{
			System.out.println("SQLException [While getting data from ChargeBack table]:[][Excuting Query Failed]" + sql +se.getMessage());
		}	
		try
		{ 
			sql = "SELECT COUNT(*) AS COUNT FROM SITE WHERE SITE_CODE = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,siteCode.trim());
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				cnt = rs.getInt("COUNT");
			}
			rs.close();
			pstmt.close();
			if (cnt == 0)
			{
				retString.append("Invalid Site. Define the Site in Site Master Table.ejb..ejb.").append(siteCode).append("          \n");	
			}
		}//try
		catch(SQLException se)
		{
			System.out.println("SQLException [04]:[ChargeBackVarifyEJB][Excuting Query Failed]" + sql +se.getMessage());
		}
		catch(Exception ex)
		{
			System.out.println("Exception [05]::"+ex.getMessage());
			ex.printStackTrace();
		}	
		try
		{
			cnt = 0;
			sql = "SELECT COUNT(*) AS COUNT FROM CUSTOMER WHERE CUST_CODE = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,custCode.trim());
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				cnt = rs.getInt("COUNT");
			}
			rs.close();
			pstmt.close();
			if (cnt == 0)
			{
				retString.append("    \n").append("Invalid Customer.ejb..ejb.").append(custCode).append("          \n");
			}//if
		}//try
		catch(SQLException se)
		{
			System.out.println("SQLException [07]:[VerifyAllEJB][Excuting Query Failed]" + sql +se.getMessage());
		}
		catch(Exception ex)
		{
			System.out.println("Exception [08]::"+ex.getMessage());
			ex.printStackTrace();
		}	
		try
		{
			cnt = 0;
			sql = "SELECT COUNT(*) AS COUNT FROM CUSTOMER WHERE CUST_CODE = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,custCodeCrdt.trim());
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				cnt = rs.getInt("COUNT");
			}
			rs.close();
			pstmt.close();
			if (cnt == 0)
			{
				retString.append("Invalid Bill Customer.ejb..ejb.").append(custCodeCrdt).append("          \n");
			}//if
		}//try
		catch(SQLException se)
		{
			System.out.println("SQLException [10]:[ChargeBackVarifyEJB][Excuting Query Failed]" + sql +se.getMessage());
		}
		catch(Exception ex)
		{
			System.out.println("Exception [11]::"+ex.getMessage());
			ex.printStackTrace();
		}	
		if(priceList.length() == 0)
		{
			retString.append("No Price List Found in the transaction.ejb..ejb.").append("          \n");
		}//if
		else
		{
			try
			{
				cnt = 0;
				sql = "SELECT COUNT(*) AS COUNT FROM PRICELIST WHERE PRICE_LIST = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,priceList.trim());
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					cnt = rs.getInt("COUNT");
				}
				rs.close();
				pstmt.close();
				if (cnt == 0)
				{
					retString.append("Invalid Price List.ejb..ejb.").append(priceList).append("          \n");
				}//if
			}//try
			catch(SQLException se)
			{
				System.out.println("SQLException [14]:[VerifyAllEJB][Excuting Query Failed]" + sql +se.getMessage());
			}
			catch(Exception ex)
			{
				System.out.println("Exception [15]::"+ex.getMessage());
				ex.printStackTrace();
			}
		}//else	
		if(currCode.equals(""))
		{
			retString.append("No Currency Found in the transaction.ejb..ejb.").append("          \n");
		}
		else
		{
			try
			{
				cnt = 0;
				sql = "SELECT COUNT(*) AS COUNT FROM CURRENCY WHERE CURR_CODE = '"+currCode.trim()+"' ";
				stmt = conn.createStatement();
				rs = stmt.executeQuery(sql);
				if(rs.next())
				{
					cnt = rs.getInt("COUNT");
					System.out.println("COUNT.ejb.."+cnt);
				}
				rs.close();
				stmt.close();
				if (cnt == 0)
				{
					retString.append("Invalid Currency Code.ejb..ejb.").append(currCode).append("          \n");
				}//if
			}//try
			catch(SQLException se)
			{
				System.out.println("SQLException [18]:[VerifyAllEJB][Excuting Query Failed]" + sql +se.getMessage());
			}
			catch(Exception ex)
			{
				System.out.println("Exception [19]::"+ex.getMessage());
				ex.printStackTrace();
			}
		}//else	
		if(exchRate == 0)
		{
			retString.append("Exchange Rate Cannot Be Zero.ejb..ejb.").append("          \n");
		}
		if(tranId == null)
		{
			tranId = "@@@";
		}
		if(porderNo != null && porderNo.length() > 0)
		{
			try
			{
				cnt = 0;
				sql = "SELECT COUNT(1) AS COUNT FROM CHARGE_BACK WHERE TRAN_ID != ? AND PORDER_NO = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,cbtranId.trim());
				pstmt.setString(2,porderNo.trim());
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					cnt = rs.getInt("COUNT");
				}
				rs.close();
				pstmt.close();
				if(cnt > 0)
				{
					retString.append("Duplicate purche order.ejb..ejb.").append(porderNo).append("          \n");
				}
			}//try
			catch(SQLException se)
			{
				System.out.println("SQLException [22]:[VerifyAllEJB][Excuting Query Failed]" + sql +se.getMessage());
			}
			catch(Exception ex)
			{
				System.out.println("Exception [Extra.ejb..ejb.]::"+ex.getMessage());
				ex.printStackTrace();
			}
		}//if	
		try
		{
			int counter = 1;
			sqldet = "SELECT LINE_NO,CONTRACT_NO,ITEM_CODE,BUYERS_PROD_CODE,LINE_NO__CONTR,RATE__CONTR,RATE__SELL,QUANTITY,CUST_CODE__END "
				   + "FROM CHARGE_BACK_DET WHERE TRAN_ID = ? ";
			pstmt = conn.prepareStatement(sqldet);
			pstmt.setString(1,cbtranId.trim());
			rsdet = pstmt.executeQuery();
			while(rsdet.next())
			{
				lineNo = rsdet.getInt(1);
				contractNo = rsdet.getString(2);
				itemCode = rsdet.getString(3);
				buyPrdCode = rsdet.getString(4);
				lineCtr = rsdet.getString(5);
				rateCtr = rsdet.getDouble(6);
				rateSell = rsdet.getDouble(7);
				ordQty = rsdet.getDouble(8);
				custCodeEnd = rsdet.getString(9);
				
				System.out.println(".ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb.");
				System.out.println("VALUES FROM CHARGE BACK DETAIL.ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..ejb..>"+counter);
				System.out.println("LineNo.ejb..ejb."+lineNo);
				if(contractNo == null)
				{
					contractNo = "";
				}
				System.out.println("contractNo.ejb..ejb."+contractNo);
				if(itemCode == null)
				{
					itemCode = "";
				}
				System.out.println("itemCode.ejb..ejb."+itemCode);
				if(buyPrdCode == null)
				{
					buyPrdCode = "";
				}
				System.out.println("buyPrdCode.ejb..ejb."+buyPrdCode);
				if(lineCtr == null)
				{
					lineCtr = "";
				}
				System.out.println("lineCtr.ejb..ejb."+lineCtr);
				System.out.println("rateCtr.ejb..ejb."+rateCtr);
				System.out.println("rateSell.ejb..ejb."+rateSell);
				System.out.println("ordQty.ejb..ejb."+ordQty);
				if(custCodeEnd == null)
				{
					custCodeEnd = "";
				}
				System.out.println("custCodeEnd.ejb..ejb."+custCodeEnd);
							
				if(itemCode == null )
				{
					itemCode ="";
				}
				if(itemCode.length() == 0)
				{
					retString.append("Item Not Found.ejb..ejb..").append(itemCode).append("            \n");
				}						
				if(contractNo != null && contractNo.length() > 0)
				{
					try
					{
						cnt = 0;
						sql = "SELECT COUNT(*) AS COUNT FROM SCONTRACTDET,SCONTRACT "
							+ "WHERE SCONTRACTDET.CONTRACT_NO = SCONTRACT.CONTRACT_NO "
							+ "AND SCONTRACT.CONTRACT_NO = ? AND SCONTRACT.CONFIRMED = 'Y' "
							+ "AND LINE_NO = TO_NUMBER( ? )";
						pstmt2 = conn.prepareStatement(sql);
						pstmt2.setString(1,contractNo.trim());
						pstmt2.setString(2,lineCtr.trim());
						rs = pstmt2.executeQuery();
						System.out.println("Contract No.  is .ejb..ejb..ejb.."+contractNo);
						if(rs.next())
						{
							cnt = rs.getInt("COUNT");
						}
						rs.close();
						pstmt2.close();
						if (cnt == 0)
						{
							retString.append("No Contract Details found for Contract NO .ejb..ejb..").append(contractNo).append("          \n");
						}//if
					}//try
					catch(SQLException se)
					{
						System.out.println("SQLException [25]:[VerifyAllEJB][Excuting Query Failed]" + sql +se.getMessage());
					}
					catch(Exception ex)
					{
						System.out.println("Exception [26]::"+ex.getMessage());
						ex.printStackTrace();
					}
					try
					{
						sql = "SELECT CASE WHEN CONV__RTUOM_STDUOM IS NULL THEN 0 ELSE CONV__RTUOM_STDUOM END FROM SCONTRACTDET "
							+ "WHERE SCONTRACTDET.CONTRACT_NO = '"+contractNo.trim()+"' AND LINE_NO = TO_NUMBER('"+lineCtr.trim()+"') AND ITEM_CODE = '"+itemCode.trim()+"' "; 
						stmt = conn.createStatement();
						rs = stmt.executeQuery(sql);
						if(rs.next())
						{
							dbcountRate = rs.getDouble(1);
							System.out.println("Data base contract rate is.ejb..ejb..ejb.."+dbcountRate);
						}
						rs.close();
						stmt.close();
						if(dbcountRate != rateCtr)
						{
							try
							{
								String aprState = "";
								count = 0;
								sql = "SELECT APRV_STAT FROM BUSINESS_LOGIC_CHECK WHERE SALE_ORDER = ? AND CR_POLICY = 'CT0' ";
								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1,cbtranId.trim());
								rs = pstmt1.executeQuery();
								if(rs.next())
								{
									aprState = rs.getString(1);
									System.out.println("aprState.ejb.."+aprState);
								}
								rs.close();
								pstmt1.close();
								if(aprState.equalsIgnoreCase("A"))
								{
									System.out.println("The contract rate has been approved.ejb..ejb..ejb.");
								}
								else
								{
									tranId = generateTranId("w_charge_back",conn);
									retString.append("Contract Rate Mismatch !!!!!! Rate In Contract.ejb..ejb..").append(dbcountRate).append("  Rate in transaction.ejb..ejb..ejb..ejb.").append(rateCtr).append("          \n");
									errCode = "CT0";
									descr = "Contract Rate Mismatch. Rate In Contract = "+dbcountRate+" , Rate in transaction = "+rateCtr;
									try
									{
										sqlInstr = "INSERT INTO BUSINESS_LOGIC_CHECK (TRAN_ID, TRAN_TYPE, SALE_ORDER, CR_POLICY, DESCR,APRV_STAT ) "
												 + "VALUES ( ?,'C',?,?,?,'N' )";
										pstmt1 = conn.prepareStatement(sqlInstr);
										pstmt1.setString(1,tranId.trim());
										pstmt1.setString(2,cbtranId.trim());
										pstmt1.setString(3,errCode.trim());
										pstmt1.setString(4,descr.trim());
										count = pstmt1.executeUpdate();	
										System.out.println("No. of records inserted with error code for contract rate "+rateCtr+" is :"+count);
										pstmt1.close();
									}
									catch(SQLException se)
									{
										System.out.println("SQLException [27]:[VerifyAllEJB][Excuting Query Failed]" + sqlInstr +se.getMessage());
									}	
								}//else
							}//try after if				
							catch(SQLException se)
							{
								System.out.println("SQLException [27]:[VerifyAllEJB][Excuting Query Failed]" + sql +se.getMessage());
							}
						}//if
					}//try
					catch(SQLException se)
					{
						System.out.println("SQLException [28]:[VerifyAllEJB][Excuting Query Failed]" + sql +se.getMessage());
					}
					catch(Exception ex)
					{
						System.out.println("Exception [29]::"+ex.getMessage());
						ex.printStackTrace();
					}	
				}//contract No if
				try
				{
					Object date = null;
					String stranDate = tranDate.toString();
					SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getDBDateFormat());
					date = sdf.parse(stranDate);
					SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getApplDateFormat());
					stranDate = sdf1.format(date).toString();
					
					sql = "SELECT CASE WHEN RATE IS NULL THEN 0 ELSE RATE END FROM PRICELIST WHERE PRICE_LIST = '"+priceList.trim()+"' AND ITEM_CODE = '"+itemCode.trim()+"' " 
                        + "AND to_date('"+stranDate+"','"+genericUtility.getApplDateFormat()+"') BETWEEN  EFF_FROM AND VALID_UPTO ";
					stmt = conn.createStatement();
					rs = stmt.executeQuery(sql);
					
					//System.out.println("Sale Rate sql.ejb.."+sql);
					if(rs.next())
					{
						dbsellRate = rs.getDouble(1);
						System.out.println("Data base sell rate is.ejb..ejb..ejb.."+dbsellRate);
					}
					rs.close();
					stmt.close();
					if(dbsellRate != rateSell)
					{
						try
						{
							String aprState = "";
							count = 0;
							sql = "SELECT APRV_STAT FROM BUSINESS_LOGIC_CHECK WHERE SALE_ORDER = ? AND CR_POLICY = 'RS0' ";
							pstmt1 = conn.prepareStatement(sql);
							pstmt1.setString(1,cbtranId.trim());
							rs = pstmt1.executeQuery();
							if(rs.next())
							{
								aprState = rs.getString(1);
								System.out.println("aprState.ejb.."+aprState);
							}
							rs.close();
							pstmt1.close();
							if(aprState.equalsIgnoreCase("A"))
							{
								System.out.println("The Sale rate has been approved.ejb..ejb..ejb.");
							}
							else
							{
								tranId = generateTranId("w_charge_back",conn);
								retString.append("Sale Rate Mismatch. Rate In Price List.ejb..ejb.").append(dbsellRate).append("  Rate in Transaction.ejb..ejb..").append(rateSell).append("          \n");
								errCode = "RS0";
								descr = "Sale Rate Mismatch.Rate In Price List = "+dbsellRate+" ,Rate in transaction = "+rateSell;
								try
								{
									sqlInstr = "INSERT INTO BUSINESS_LOGIC_CHECK (TRAN_ID, TRAN_TYPE, SALE_ORDER, CR_POLICY, DESCR,APRV_STAT ) "
											 + "VALUES ( ?,'C',?,?,?,'N' )";
									pstmt1 = conn.prepareStatement(sqlInstr);
									pstmt1.setString(1,tranId.trim());
									pstmt1.setString(2,cbtranId.trim());
									pstmt1.setString(3,errCode.trim());
									pstmt1.setString(4,descr.trim());
									count = pstmt1.executeUpdate();	
									System.out.println("No. of records inserted with error code for sale rate "+rateSell+" is :"+count);
									pstmt1.close();
								}
								catch(SQLException se)
								{
									System.out.println("SQLException [30]:[VerifyAllEJB][Excuting Query Failed]" + sqlInstr +se.getMessage());
								}
							}//else
						}//select try
						catch(SQLException se)
						{
							System.out.println("SQLException [30]:[VerifyAllEJB][Excuting Query Failed]" + sql +se.getMessage());
						}
					}//if
				}//try
				catch(SQLException se)
				{
					System.out.println("SQLException [31]:[VerifyAllEJB][Excuting Query Failed]" + sql +se.getMessage());
				}
				catch(Exception ex)
				{
					System.out.println("Exception [32]::"+ex.getMessage());
					ex.printStackTrace();
				}
				if(ordQty <= 0)
				{
					retString.append("Quantity Ordered is Blank or Negative.ejb..ejb..").append(ordQty).append("          \n");
				}//if
				else if(lineCtr.length() > 0)
				{
					lineCtr = "    "+lineCtr;
					int len = lineCtr.length();
					//System.out.println("Length of string is ----"+len);
					lineCtr = lineCtr.substring(len-3,len);
					System.out.println("New line counter.ejb..ejb.."+lineCtr);
					if(lineNo > 0)
					{
						oldQty = ordQty;
						System.out.println("Old quantity.ejb..ejb.."+oldQty);
					}
					else
					{
						oldQty = 0;
						System.out.println("Old quantity.ejb..ejb.."+oldQty);
					}
					try
					{
						sql = "SELECT CASE WHEN SUM(C2.QUANTITY) IS NULL THEN 0 ELSE SUM(C2.QUANTITY) END FROM CHARGE_BACK C1,CHARGE_BACK_DET C2 "
							+ "WHERE C1.TRAN_ID = C2.TRAN_ID AND C2.CONTRACT_NO = ? "
							+ "AND C2.LINE_NO__CONTR = ? AND C1.CONFIRMED = 'Y' ";
						pstmt2 = conn.prepareStatement(sql);
						pstmt2.setString(1,contractNo.trim());
						pstmt2.setString(2,lineCtr.trim());
						rs = pstmt2.executeQuery();
						if (rs.next())
						{
							chargeBakQty = rs.getDouble(1);
							System.out.println("Charge back qty is.ejb..ejb..ejb.."+chargeBakQty);
						}
						rs.close();
						pstmt2.close();
						usedQty = ((chargeBakQty - oldQty) + ordQty );
					}//try
					catch(SQLException se)
					{ 
						System.out.println("SQLException [34]:[VerifyAllEJB][Excuting Query Failed]" + sql +se.getMessage());
					}
					catch(Exception ex)
					{
						System.out.println("Exception [35]::"+ex.getMessage());
						ex.printStackTrace();
					}
					try
					{
						sql = "SELECT SUM(B.QUANTITY) FROM SCONTRACT A , SCONTRACTDET B "
							+ "WHERE A.CONTRACT_NO = B.CONTRACT_NO AND B.CONTRACT_NO = ? " 
							+ "AND B.LINE_NO = TO_NUMBER( ? )";
						pstmt2 = conn.prepareStatement(sql);
						pstmt2.setString(1,contractNo.trim());
						pstmt2.setString(2,lineCtr.trim());
						rs = pstmt2.executeQuery();
						if (rs.next())
						{
							scQty = rs.getDouble(1);
							System.out.println("Quantity from Scontract is .ejb..ejb..ejb.."+scQty);
						}
						rs.close();
						pstmt2.close();
					}
					catch(SQLException se)
					{
						System.out.println("SQLException [36]:[VerifyAllEJB][Excuting Query Failed]" + sql +se.getMessage());
					}
					catch(Exception ex)
					{
						System.out.println("Exception [37]::"+ex.getMessage());
						ex.printStackTrace();
					}
					if(usedQty > scQty)
					{
						retString.append("ChargeBack Qty.ejb..ejb..").append(scQty).append("  Exceeds the Contract Qty.ejb..ejb..ejb..").append(usedQty).append("          \n");
					}//if
					else
					{
						try
						{
							sql = "SELECT CASE WHEN SUM(SRETURNDET.PHYSICAL_QTY) = 0 THEN SUM(SRETURNDET.QUANTITY) ELSE SUM(SRETURNDET.PHYSICAL_QTY) END "
								+ "FROM SRETURNDET,SRETURN WHERE SRETURN.TRAN_ID = SRETURNDET.TRAN_ID AND SRETURN.CUST_CODE = ? "
								+ "AND SRETURNDET.ITEM_CODE = ? AND SRETURNDET.RET_REP_FLAG = 'R' AND SRETURN.CONFIRMED = 'Y' ";
							pstmt2 = conn.prepareStatement(sql);
							pstmt2.setString(1,custCode.trim());
							pstmt2.setString(2,itemCode.trim());
							rs = pstmt2.executeQuery();
							
							//System.out.println("SQL for Physical quantity with R .ejb.."+sql);
							if (rs.next())
							{
								retPQtyR = rs.getDouble(1);
								System.out.println("Physical quantity for R is .ejb..ejb..ejb.."+retPQtyR);
							}
							rs.close();
							pstmt2.close();
						}
						catch(SQLException se)
						{
							System.out.println("SQLException [39]:[VerifyAllEJB][Excuting Query Failed]" + sql +se.getMessage());
						}
						catch(Exception ex)
						{
							System.out.println("Exception [40]::"+ex.getMessage());
							ex.printStackTrace();
						}
						try
						{
							sql = "SELECT CASE WHEN SUM(SRETURNDET.PHYSICAL_QTY) = 0 THEN SUM(SRETURNDET.QUANTITY) ELSE SUM(SRETURNDET.PHYSICAL_QTY) END "
								+ "FROM SRETURNDET,SRETURN WHERE SRETURN.TRAN_ID = SRETURNDET.TRAN_ID AND SRETURN.CUST_CODE = ? "
								+ "AND SRETURNDET.ITEM_CODE = ? AND SRETURNDET.RET_REP_FLAG = 'P' AND SRETURN.CONFIRMED = 'Y' ";
							pstmt2 = conn.prepareStatement(sql);
							pstmt2.setString(1,custCode.trim());
							pstmt2.setString(2,itemCode.trim());
							rs = pstmt2.executeQuery();
							//System.out.println("SQL for Physical quantity with P .ejb.."+sql);
							if (rs.next())
							{
								retPQtyP = rs.getDouble(1);
								System.out.println("Physical quantity for P is .ejb..ejb..ejb.."+retPQtyP);
							}
							rs.close();
							pstmt2.close();
						}
						catch(SQLException se)
						{
							System.out.println("SQLException [41]:[VerifyAllEJB][Excuting Query Failed]" + sql +se.getMessage());
						}
						catch(Exception ex)
						{
							System.out.println("Exception [42]::"+ex.getMessage());
							ex.printStackTrace();
						}
						try
						{
							
							sql = "SELECT CASE WHEN SUM(C2.QUANTITY) IS NULL THEN 0 ELSE SUM(C2.QUANTITY) END FROM CHARGE_BACK C1,CHARGE_BACK_DET C2 "
				 				+ "WHERE C1.TRAN_ID = C2.TRAN_ID AND C2.ITEM_CODE = ? "
				 				+ "AND C1.CUST_CODE = ? AND C1.CONFIRMED = 'Y' ";
							pstmt2 = conn.prepareStatement(sql);
							pstmt2.setString(1,itemCode.trim());
							pstmt2.setString(2,custCode.trim());
							rs = pstmt2.executeQuery();
							//System.out.println("SQL for quantity from charge back .ejb.."+sql);
							if (rs.next())
							{
								custQty = rs.getDouble(1);
								System.out.println("Quantity  is .ejb..ejb..ejb.."+custQty);
							}
							rs.close();
							pstmt2.close();
						}
						catch(SQLException se)
						{
							System.out.println("SQLException [43]:[VerifyAllEJB][Excuting Query Failed]" + sql +se.getMessage());
						}
						catch(Exception ex)
						{
							System.out.println("Exception [44]::"+ex.getMessage());
							ex.printStackTrace();
						}
						try
						{
							
							sql = "SELECT CASE WHEN SUM(INVOICE_TRACE.QUANTITY__STDUOM) IS NULL THEN 0 ELSE SUM(INVOICE_TRACE.QUANTITY__STDUOM) END FROM INVOICE_TRACE,INVOICE "
								+ "WHERE INVOICE_TRACE.INVOICE_ID = INVOICE.INVOICE_ID AND INVOICE_TRACE.ITEM_CODE = ? "
								+ "AND INVOICE.CUST_CODE__BIL = ? AND INVOICE.CONFIRMED = 'Y' ";
							pstmt2 = conn.prepareStatement(sql);
							pstmt2.setString(1,itemCode.trim());
							pstmt2.setString(2,custCodeCrdt.trim());
							rs = pstmt2.executeQuery();
							//System.out.println("SQL for quantity from Invoice Trace .ejb.."+sql);
							if (rs.next())
							{
								invQty = rs.getDouble(1);
								System.out.println("INV Quantity  is .ejb..ejb..ejb.."+invQty);
							}
							rs.close();
							pstmt2.close();
						}
						catch(SQLException se)
						{
							System.out.println("SQLException [45]:[VerifyAllEJB][Excuting Query Failed]" + sql +se.getMessage());
						}
						catch(Exception ex)
						{
							System.out.println("Exception [46]::"+ex.getMessage());
							ex.printStackTrace();
						}
						
						usedQty = ((custQty - oldQty) + ordQty);
						netSell = (((invQty - custQty) - retPQtyR) + retPQtyP);
						System.out.println("Used quantity .ejb.."+usedQty);
						System.out.println("Net sell quantity .ejb.."+netSell);
						System.out.println("Ordered quantity .ejb.."+ordQty);	
						
						if(ordQty > netSell)
						{
							retString.append("Quantity Mismatch. Net Sale Qty .ejb..ejb..").append(netSell).append("  Transaction Qty.ejb..ejb..ejb.").append(ordQty).append("          \n");
						}//if
					}//used Qty else
				}//lineCtr else	
				else
				{
					if(custCodeEnd == null)
					{
						retString.append("No Sales Contract defined For the Customer.ejb..ejb..").append(custCodeEnd).append("          \n");
					}//if
				}//else
				++counter;
			}//while
			rsdet.close();
			pstmt.close();
			if(retString.toString().trim().equals(""))
			{
				vFlag = "Y";
			}
			else
			{
				vFlag = "N";
			}
			try
			{
				sql = "UPDATE CHARGE_BACK SET VERIFY_FLAG = ? WHERE TRAN_ID = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,vFlag.trim());
				pstmt.setString(2,cbtranId.trim());
				int updCnt = pstmt.executeUpdate();	
				System.out.println("No of CHARGE_BACK records updated is .ejb."+updCnt);
				
				xmlString.append( "<?xml version='1.0' encoding='UTF-8'?>\n");
				xmlString.append("<Detail>");
				xmlString.append("<tran_id><![CDATA[").append(cbtranId.trim()).append("]]></tran_id>");
				xmlString.append("<verify_flag><![CDATA[").append(vFlag.trim()).append("]]></verify_flag>");
				xmlString.append("</Detail>");		
			}
			catch(SQLException se)
			{
				System.out.println("SQLException []:[VerifyAllEJB][Excuting Query Failed]" + sql + se.getMessage());
			}
			catch(Exception e)
			{
				System.out.println("Exception ::"+e);
				e.printStackTrace();
			}
		}//most outer try
		catch(SQLException se)
		{
			System.out.println("SQLException []:[VerifyAllEJB][Excuting Query Failed]" + sqldet + se.getMessage());
		}
		catch(Exception e)
		{
			System.out.println("Exception ::"+e);
			e.printStackTrace();
		}
		finally
		{
			try
			{
				System.out.println("Closing Connection.ejb..ejb..");
				conn.close();
				conn = null;
			}
			catch(Exception se){}
		}
		java.io.File objFile = new java.io.File (CommonConstants.JBOSSHOME+File.separator+"EDI");
		if(!(objFile.exists()))
		{
			objFile.mkdir();
		}
		try
		{
			FileOutputStream	fos = new FileOutputStream(CommonConstants.JBOSSHOME + File.separator +"EDI"+File.separator+ cbtranId+".txt");
			byte convertStringToByte[] = retString.toString().getBytes();
			fos.write(convertStringToByte);
			fos.close();
		}
		catch(Exception e)
		{
			System.out.println("Exception ::"+e);
			e.printStackTrace();
		}
		System.out.println("Result of verification.ejb..ejb..ejb..ejb..ejb."+retString.toString());
		try
		{
			FileOutputStream	fos = new FileOutputStream(CommonConstants.JBOSSHOME + File.separator +"EDI"+File.separator+ cbtranId+".xml");
			byte convertStringToByte[] = xmlString.toString().getBytes();
			fos.write(convertStringToByte);
			fos.close();
		}
		catch(Exception e)
		{
			System.out.println("Exception ::"+e);
			e.printStackTrace();
		}
		return xmlString.toString();
	}//verify

	private String  generateTranId(String winName,Connection conn1)
	{
		Statement lstmt = null;
		ResultSet lrs = null;
		String keyStringQuery = null;
		String tranId = null;
		try
		{ 	
			keyStringQuery = "SELECT KEY_STRING, TRAN_ID_COL, REF_SER FROM TRANSETUP WHERE TRAN_WINDOW = '"+ winName +"'";
		    lstmt = conn1.createStatement();
			lrs = lstmt.executeQuery(keyStringQuery);
			String tranSer = "";
			String keyString = "";
			String keyCol = "";
			if (lrs.next())
			{
				keyString = lrs.getString(1);
				keyCol = lrs.getString(2);
				tranSer = lrs.getString(3);				
			}
			// Changed by Sneha on 01-09-2016, for Closing the Open Cursor [Start]
			if ( lstmt != null )
			{
				lstmt.close();
				lstmt = null;
			}
			if ( lrs != null )
			{
				lrs.close();
				lrs = null;
			}
			// Changed by Sneha on 01-09-2016, for Closing the Open Cursor [End]
			
			String xmlValues = "";
			xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
			xmlValues = xmlValues + "<Header></Header>";
			xmlValues = xmlValues + "<Detail1>";
			xmlValues = xmlValues +"</Detail1></Root>";
			TransIDGenerator tg = new TransIDGenerator(xmlValues,userId, commonConstants.DB_NAME);
			tranId = tg.generateTranSeqID(tranSer, keyCol, keyString, conn1);
		}
		catch(SQLException se)
		{				
			System.out.println("SQLException :Generating id[failed] : " + "\n" +se.getMessage());
		}
		catch(Exception ex)
		{
			System.out.println("Exception8:Generating id [failed]:" + "\n" +ex.getMessage());
		}
		finally
		{
			if (lstmt != null)
			{
			
				lstmt = null;
			}
		}
		return tranId;
	}//trnId
}
