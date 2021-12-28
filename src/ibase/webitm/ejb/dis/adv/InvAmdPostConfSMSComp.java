/**
 * This component is used in post_confirm event to send SMS to out station customer on invoice amendment confirmation.
 * @author Bhushan Lad on 15/JAN/15
 * 
 */

package ibase.webitm.ejb.dis.adv;

import ibase.system.config.ConnDriver;
import ibase.ejb.E12SMSComp;
import ibase.ejb.E12SMSCompLocal;
import ibase.system.config.AppConnectParm;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.DBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.naming.NamingException;

import org.w3c.dom.Document;

@javax.ejb.Stateless
public class InvAmdPostConfSMSComp extends ValidatorEJB implements InvAmdPostConfSMSCompRemote, InvAmdPostConfSMSCompLocal 
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	/** 
	 * This method is used to send SMS to out station customer after confirmation
	 * @param xmlString contains the transaction data
	 * @param formatCode formatCode is contains information like mobile no, sms text etc
	 * @param xtraParams contains user information like login code, login emp code etc
	 * @return retStr on success returns SEND_SUCCESS
	 */
	public String sendSMS(String xmlString, String formatCode, String xtraParams)throws RemoteException, ITMException 
	{

		String retStr = "SEND_SUCCESS ";
		ConnDriver mConnDriver = null;
		Connection mConnection = null;
		PreparedStatement pstmt = null;
		PreparedStatement pstmtCust = null;
		ResultSet mResultSet = null;
		int count = 0;
		
		Document dom = null;
		
		String XTRA_PARAMS_SEPARATOR = "~~";
		
		try
		{
			//GenericUtility genericUtility = GenericUtility.getInstance();
			
			dom =genericUtility.parseString(xmlString);
			
			mConnDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 13-06-2016 :START
			//mConnection = (Connection) mConnDriver.getConnectDB("Driver");
			mConnection = (Connection)getConnection();
			//Changes and Commented By Bhushan on 13-06-2016 :END
			mConnDriver = null;
			String invoiceID = genericUtility.getColumnValue("invoice_id",dom,"1");
			String status = genericUtility.getColumnValue("status",dom,"1");
			String custCode = genericUtility.getColumnValue("invoice_cust_code",dom,"1");
			
			System.out.println("invoiceID =["+invoiceID+"], status =["+status+"], custCode =["+custCode+"], formatCode =["+formatCode+"]");
			
			String invoicesql="SELECT COUNT(*) AS COUNT FROM INVOICE_AMENDMENT WHERE INVOICE_ID = ? AND (CASE WHEN CONFIRMED IS NULL THEN 'N' ELSE CONFIRMED END) ='Y'";

			pstmt = mConnection.prepareStatement(invoicesql);
			pstmt.setString(1, invoiceID);
			
			mResultSet=pstmt.executeQuery();

			if(mResultSet.next())
			{
				count = mResultSet.getInt("COUNT"); 
			}
			mResultSet.close(); mResultSet = null;
			
			System.out.println("COUNT =[" + count + "]");
			
			if(count > 0)
			{
				String locatedAt = "";
				String sql="SELECT LOCATED_AT FROM CUSTOMER WHERE CUST_CODE = ?";
				pstmtCust = mConnection.prepareStatement(sql);
				pstmtCust.setString(1, custCode);
				
				mResultSet = pstmtCust.executeQuery();
				if(mResultSet.next())
				{
					locatedAt = (mResultSet.getString("LOCATED_AT") == null) ?"":mResultSet.getString("LOCATED_AT");
				}
				mResultSet.close(); mResultSet = null;
				System.out.println("located_at =["+ locatedAt +"]");
				
				if("O".equalsIgnoreCase(locatedAt))
				{
					E12SMSComp e12sms= new E12SMSComp();
					
					xtraParams = "loginCode=SYSTEM" + XTRA_PARAMS_SEPARATOR + "termId=SYSTEM";
					
					//retStr = e12sms.sendSMS(xmlString, formatCode, xtraParams);
					DBAccessEJB dbAccess = new DBAccessEJB();
					UserInfoBean userInfo = dbAccess.createUserInfo("SYSTEM");							
					retStr = e12sms.sendSMS(xmlString, formatCode, xtraParams, userInfo);
					e12sms = null;
				}
			}
			
		}
		catch(Exception e)
		{
			System.out.println("Exception ::"+e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(mConnection != null)
				{					
					if(mResultSet != null)
					{
						mResultSet.close();
						mResultSet = null;
					}
					if(pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}
					if(pstmtCust != null)
					{
						pstmtCust.close();
						pstmtCust = null;
					}
					mConnection.close();
					mConnection = null;
				}
			}
			catch(Exception e1){}
		}
		System.out.println("retStr ::"+retStr);
		return retStr;	
	} 
}