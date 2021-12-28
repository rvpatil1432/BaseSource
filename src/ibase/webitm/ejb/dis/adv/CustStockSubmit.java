/********************************************************
	Title : CustStockOrder[D16HVHB005]
	Date  : 07/12/16
 ********************************************************/
package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;

import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.*;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.dis.DistDiscount;
import ibase.webitm.ejb.sys.UtilMethods;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import ibase.utility.E12GenericUtility;
import ibase.utility.EMail;
import ibase.webitm.utility.ITMException;

import javax.ejb.Stateless;


@Stateless
public class CustStockSubmit extends ActionHandlerEJB implements CustStockSubmitLocal, CustStockSubmitRemote
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	FinCommon finCommon = new FinCommon();
	DistCommon distCommon= new DistCommon();
	ValidatorEJB validatorEJB = new ValidatorEJB();
	UtilMethods utilMethods = new UtilMethods();
	DistDiscount distDiscount = new DistDiscount();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	
	public String submit(String tranId, String xtraParams, String forcedFlag)throws RemoteException, ITMException
	{
		System.out.println(">>>>>>>>>>>>>>>>>>CustStockSubmit called>>>>>>>>>>>>>>>>>>>");
		String sql = "" ;
		Connection conn = null;
		PreparedStatement pstmt = null ;
		String errString = null;
		ResultSet rs = null;
		String confirm = "", custCode = "",emailId = "",name = "",msgText= "",subText = "", mailRetStr = "",status= "";
		int updCnt = 0;
		Date tranDate = null;
		try
		{

			//ConnDriver connDriver  = new ConnDriver();
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			conn.setAutoCommit(false);
			SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getApplDateFormat());
			SimpleDateFormat sdf2 = new SimpleDateFormat(genericUtility.getDispDateFormat());
			
			System.out.println("xtraParams"+xtraParams);
			String userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			System.out.println("--login code--"+userId);

			sql = " select confirmed,cust_code,tran_date,status from cust_stock where tran_id = ?";
			pstmt = conn.prepareStatement( sql );
			pstmt.setString( 1, tranId.trim());
			rs = pstmt.executeQuery();
			if( rs.next() )
			{
				confirm = rs.getString("confirmed");
				custCode = rs.getString("cust_code");
				tranDate = rs.getDate("tran_date");
				status = rs.getString("status");
			}
			pstmt.close();
			pstmt = null;				
			rs.close();
			rs = null;
			
			String tDate = sdf1.format(tranDate);
			String tDate2 = sdf2.format(tranDate);
			
			System.out.println("tDate["+tDate+"]tDate2["+tDate2+"]");
			
			if( "Y".equalsIgnoreCase(confirm))
			{
				errString = itmDBAccessEJB.getErrorString("","VTCONFMDSM","","",conn);
			}
			else if( "S".equalsIgnoreCase(status))
			{
				errString = itmDBAccessEJB.getErrorString("","VTSUBM1AD","","",conn);
			}
			else
			{
				
				sql = " select sh_name,email_addr from customer where cust_code = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, custCode);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					name = checkNull(rs.getString("sh_name"));
					emailId = checkNull(rs.getString("email_addr"));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
				Calendar cal = Calendar.getInstance();
				SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
				String displayDate = df.format(cal.getTime());
				System.out.println("fmt: " + displayDate);
				
				System.out.println("@V@ Format Code :-  emailAddr["+emailId+"]reportTo["+custCode+"]name["+name+"]");

				subText = "Auto stock Replenishment generation["+tranId+"]" ;
				
				msgText = "Dear " + name +",\n\n "+ 
						"\t\t Kindly check,verify and approve the Replenishment Quantity for tran_id "+tranId +" generated on "+tDate+".\n"+
						"\n\n"+ "Thanks and Regards,\n"+ userId ;
				
				System.out.println("msgText loop" + msgText);
				
				String mailDomStr = "<ROOT>"+
						"<MAIL><EMAIL_TYPE>page</EMAIL_TYPE><ENTITY_CODE>Base</ENTITY_CODE>"+
						"<ENTITY_TYPE>"+"E"+"</ENTITY_TYPE><SUBJECT>"+subText+"</SUBJECT>"+
						"<BODY_TEXT>"+msgText+"</BODY_TEXT><TO_ADD>"+emailId+"</TO_ADD><CC_ADD></CC_ADD>"+
						"</MAIL></ROOT>";
				
				EMail email = new EMail();
				
				if(emailId != null && emailId.trim().length() > 0)
				{
					 mailRetStr = email.sendMail(mailDomStr, "ITM"); 
				}
				
				System.out.println("@V@ Mail return String  :- ["+mailRetStr+"]");
				
				if("S".equalsIgnoreCase(mailRetStr))
				{
					sql = "update cust_stock set status = 'S' where tran_id = ? and status <> 'S'";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, tranId);	
					updCnt = pstmt.executeUpdate();
					pstmt.close();pstmt = null;
					System.out.println("updCnt status>>"+updCnt);									
					errString = itmDBAccessEJB.getErrorString("","VTSUBM1","","",conn);
				}
			}
			
			
		} catch (Exception e)
		{
			if(conn!=null)
			{
				try 
				{
					conn.rollback();
				} 
				catch (SQLException ex) 
				{

					e.printStackTrace();
					throw new ITMException(e);
				}
			}
			e.printStackTrace();
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
				if (errString.contains("VTSUBM1"))
				{
					conn.commit(); 
					System.out.println("@@@@ Transaction commit... ");
					conn.close();
					conn = null;
				}
			} catch (Exception e)
			{
				System.out.println("Exception : " + e);
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		System.out.println("Last errString ["+errString+"]");
		return errString;

	}
	
	
	private String checkNull(String input)	
	{
		if (input == null)
		{
			input="";
		}
		return input.trim();
	}
	
	
	
}