package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import javax.ejb.Stateless;

import org.w3c.dom.Document;

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;

@Stateless
public class PoRcpRetPostSave extends ValidatorEJB implements PoRcpRetPostSaveLocal,PoRcpRetPostSaveRemote
{
	boolean isError = false;
	public String postSave(String xmlString,String tranId ,String editFlag, String xtraParams,Connection conn) throws RemoteException,ITMException
	{
		String retString = "";

		Document dom = null;
		E12GenericUtility genericUtility = null;
		try
		{
			System.out.println("tranId--["+tranId+"]");
			genericUtility = new E12GenericUtility();
			dom = genericUtility.parseString(xmlString);
			retString = postSave(dom,tranId,editFlag,xtraParams,conn);
			
		}
		catch(Exception e )
		{
			e.getMessage();
			e.printStackTrace();
			isError = true;
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				//Modified by Anjali R. on [04/02/2019][Post Save component will not commit the data][Start]
				/*if(!isError)
				{
					//conn.commit();
				}
				else
				{
					conn.rollback();
				}*/
				//Modified by Anjali R. on [04/02/2019][Post Save component will not commit the data][End]
			}
			catch(Exception e)
			{
				e.getMessage();
				e.printStackTrace();
				isError = true;
				throw new ITMException(e);
			}
		}

		return retString;
	}
	public String postSave(Document dom,String tranId,String editFlag,String xtraParams,Connection conn) throws ITMException
	{
		String retString = "";
		String chgUser = "";
		String chgTerm = "";
		String sql = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql1 = "";
		PreparedStatement pstmt1 = null;
		ResultSet rs1 = null;

		Timestamp currDate = null;
		String errString = "";
		double amount = 0.0;
		double taxAmt = 0.0;
		int count = 0;
		String tranSer = "";
		E12GenericUtility genericUtility = null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		SimpleDateFormat sdf = null;
		double frtAmt = 0.0,exchRateFrt = 0.0,insuranceAmt = 0.0,exchRateIns = 0.0,clearingCharges = 0.0,exchRateClr = 0.0,otherCharges = 0.0,exchRateOthch = 0.0;
		double totAddlCost = 0.0;
		try
		{
			genericUtility = new E12GenericUtility();
			itmDBAccessEJB = new ITMDBAccessEJB();
			chgUser =(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode"));
			chgTerm =(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "chgTerm"));
			
			tranId = genericUtility.getColumnValue("tran_id",dom);
			System.out.println("Tran id :- ["+tranId+"]");
			sql = "select tran_ser from porcp where tran_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				tranSer = rs.getString("tran_ser");
			}
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
			if("P-RCP".equalsIgnoreCase(tranSer))
			{
				double quantityStduom = 0.0,rateStduom = 0.0,discount = 0.0,taxAmtDetail = 0.0,netAmtDetail = 0.0,netAmtHdr = 0.0,detailQty = 0.0;
				double totQty = 0.0;
				double frtAmount = 0.0;
				double totDiscount = 0.0;
				double totTaxAmt = 0.0;
				String lineNo  = "";
				sql = "select quantity__stduom,rate__stduom,discount,tax_amt,line_no,quantity from porcpdet where tran_id = ? order by line_no";
				pstmt = conn.prepareStatement(sql);
				
				sql1 = "update porcpdet set net_amt = ? where tran_id = ? and line_no = ?";
				pstmt1 = conn.prepareStatement(sql1);
				
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				while(rs.next())
				{
					netAmtDetail = 0.0;
					detailQty = 0.0;
					discount = 0.0;
					taxAmtDetail = 0.0;
					quantityStduom = rs.getDouble("quantity__stduom");
					rateStduom = rs.getDouble("rate__stduom");
					discount = rs.getDouble("discount");
					taxAmtDetail = rs.getDouble("tax_amt");
					lineNo = rs.getString("line_no");
					detailQty = rs.getDouble("quantity");
					
					
					totQty = totQty + detailQty;
					netAmtDetail = (quantityStduom*rateStduom)-((quantityStduom*rateStduom*discount)/100)+taxAmtDetail;
					totDiscount = totDiscount + (((quantityStduom*rateStduom*discount)/100));
					totTaxAmt = totTaxAmt + taxAmtDetail;
					
					System.out.println("netAmtDetail --["+netAmtDetail+"]");
					netAmtHdr = netAmtHdr + netAmtDetail;
					pstmt1.setDouble(1, netAmtDetail);
					pstmt1.setString(2, tranId);
					pstmt1.setString(3, lineNo);
					count = pstmt1.executeUpdate();
					pstmt1.clearParameters();
					
				}
				System.out.println("netAmtHdr---["+netAmtHdr+"]");
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
				if(pstmt1 != null)
				{
					pstmt1.close();
					pstmt1 = null;
				}
				String frtType = "";
				double frtRate = 0.0;
				
				sql = "select FRT_AMT,EXCH_RATE__FRT,INSURANCE_AMT,EXCH_RATE__INS,CLEARING_CHARGES,EXCH_RATE__CLR,OTHER_CHARGES,EXCH_RATE__OTHCH,frt_rate ,frt_type from porcp where tran_id = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
						//frtAmt = rs.getDouble("FRT_AMT");
					
					frtAmount = rs.getDouble("FRT_AMT");//changes -By-Monika-14-may-19
					exchRateFrt =  rs.getDouble("EXCH_RATE__FRT");
					insuranceAmt =  rs.getDouble("INSURANCE_AMT");
					exchRateIns =  rs.getDouble("EXCH_RATE__INS");
				//commented vy monika-29-my-2019
					//clearingCharges =  rs.getDouble("EXCH_RATE__INS");
					//changes -By-Monika-29-may-19
					clearingCharges =  rs.getDouble("CLEARING_CHARGES");//end
					exchRateClr =  rs.getDouble("EXCH_RATE__CLR");
					otherCharges =  rs.getDouble("OTHER_CHARGES");
					exchRateOthch =  rs.getDouble("EXCH_RATE__OTHCH");
					frtType = rs.getString("frt_type");
					frtRate = rs.getDouble("frt_rate");
				}
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

				
				//changes made by-Monika-14-may-19
				
				if("Q".equalsIgnoreCase(frtType))
				{
				frtAmount = totQty * frtRate;

				}
				//frtAmount = totQty * frtRate *("Q".equalsIgnoreCase(frtType) ? 1 : 0);
				//end
				//changes by-monika-29-may-2019--
				System.out.println("test1:--"+(frtAmount*exchRateFrt)+"test2:--"+(insuranceAmt*exchRateIns)+"test3:--"+(clearingCharges*exchRateClr)+"test4:--"+(otherCharges*exchRateOthch));
				//end
				totAddlCost = (frtAmount*exchRateFrt)+(insuranceAmt*exchRateIns)+(clearingCharges*exchRateClr)+(otherCharges*exchRateOthch);
				System.out.println("totAddlCost--["+totAddlCost+"]");

				sql = "update porcp set frt_amt = ?,tax = ?,amount = ?,discount=?,total_additional_cost= ? where tran_id = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setDouble(1,frtAmount );
				pstmt.setDouble(2, totTaxAmt);
				pstmt.setDouble(3, netAmtHdr);
				pstmt.setDouble(4, totDiscount);
				pstmt.setDouble(5, totAddlCost);
				pstmt.setString(6, tranId);
				count = pstmt.executeUpdate();
				System.out.println("count--["+count+"]");
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
			}
			else if("P-RET".equalsIgnoreCase(tranSer))
			{
				double quantityStduom = 0.0 ,rateStduom = 0.0,discount = 0.0,taxAmountDetail = 0.0;
				String lineNo = "";
				double netAmtDetail = 0.0;
				double netAmtHdr = 0.0;
				double totTaxAmt = 0.0;
				double totDiscount = 0.0;
				sql = "select quantity__stduom,rate__stduom,discount,tax_amt,line_no from porcpdet where tran_id = ? order by line_no"; 	
				pstmt = conn.prepareStatement(sql);
				
				sql1 = "update porcpdet set net_amt = ? where tran_id = ? and line_no = ?";
				pstmt1 = conn.prepareStatement(sql1);
				
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				while(rs.next())
				{
					quantityStduom = 0.0 ;
					rateStduom = 0.0;
					discount = 0.0;
					taxAmountDetail = 0.0;
					netAmtDetail = 0.0;
					quantityStduom = rs.getDouble("quantity__stduom");
					rateStduom = rs.getDouble("rate__stduom");
					discount = rs.getDouble("discount");
					taxAmountDetail = rs.getDouble("tax_amt");
					lineNo = rs.getString("line_no");
					
					netAmtDetail =  (quantityStduom*rateStduom)-((quantityStduom*rateStduom*discount)/100)+taxAmountDetail;
					netAmtHdr = netAmtHdr + netAmtDetail;
					totTaxAmt = totTaxAmt + taxAmountDetail;
					totDiscount = totDiscount + discount;
					
					pstmt1.setDouble(1, netAmtDetail);
					pstmt1.setString(2, tranId);
					pstmt1.setString(3, lineNo);
					count = pstmt1.executeUpdate();
					pstmt1.clearParameters();
				}
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
				if(pstmt1 != null)
				{
					pstmt1.close();
					pstmt1 = null;
				}
				
				sql = "update porcp set tax = ?,amount = ?,discount = ?  where tran_id = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setDouble(1, totTaxAmt);
				pstmt.setDouble(2, netAmtHdr);
				pstmt.setDouble(3, totDiscount);
				pstmt.setString(4, tranId);
				count = pstmt.executeUpdate();
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
			}
		}
		catch(Exception e )
		{
			e.getMessage();
			e.printStackTrace();
			isError = true;
			throw new ITMException(e);
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
				if(pstmt1 != null)
				{
					pstmt1.close();
					pstmt1 = null;
				}
				
			}
			catch(Exception e)
			{
				e.getMessage();
				e.printStackTrace();
				isError = true;
				throw new ITMException(e);
			}
		}
		return retString;

	}
}
