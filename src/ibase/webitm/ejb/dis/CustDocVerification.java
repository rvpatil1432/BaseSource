/********************************************************
	Title :  CustDocVerification
	Date  : 01/06/21
	Author: Anagha Rane
 ********************************************************/
package ibase.webitm.ejb.dis;

import ibase.utility.E12GenericUtility;
import ibase.utility.CommonConstants;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ITMDBAccessLocal;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ActionHandlerEJB;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.ejb.Stateless;

@Stateless
public class CustDocVerification extends ActionHandlerEJB implements CustDocVerificationLocal,CustDocVerificationRemote
{
	E12GenericUtility genericUtility = new E12GenericUtility();
	public String actionHandler(String tranId, String xtraParams, String forcedFlag) throws RemoteException,ITMException
	{
		System.out.println("--------------verify method of CustDocVerification ------------- : ");
		String retString = "";		
		try
		{						
			retString = verify(tranId,xtraParams,forcedFlag);
		}
		catch(Exception e)
		{
			System.out.println("Exception :CustDocVerification :confirm():" + e.getMessage() + ":");
			retString = genericUtility.createErrorString(e);
			e.printStackTrace();
		}
		return retString;
	}
	
	private String verify(String tranId,String xtraParams,String forcedFlag) 
	{
		System.out.println("---------Class : CustDocVerification-->> Verify method called-----------");   
		ResultSet rs=null;
		Connection conn=null;
		PreparedStatement pstmt=null;
		String errString="", sql="", custStatus="", custCode = "";
		int updCnt=0;
        //ITMDBAccessLocal itmdbAccess=new ITMDBAccessEJB();
        ITMDBAccessEJB itmdbAccess  = new ITMDBAccessEJB();
        Timestamp sysDate = null;
        
		try
		{		
            boolean isTranLocked = false;
			conn = getConnection();
			conn.setAutoCommit(false);
			            
            SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());

			Calendar currentDate = Calendar.getInstance();

			String sysDateStr = sdf.format(currentDate.getTime());
			System.out.println("Now the date is :=>  " + sysDateStr + "]");
			sysDate = Timestamp.valueOf(genericUtility.getValidDateString(sysDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");

            if ("db2".equalsIgnoreCase(CommonConstants.DB_NAME ))
			    {
				    sql =" select cust_code, cust_status  from customer where cust_code = ? for update ";
			    }
			    else if ( "mssql".equalsIgnoreCase(CommonConstants.DB_NAME ))
			    {
				    sql =" select cust_code, cust_status from customer (updlock) where cust_code = ? " ;
			    }
			    else
			    {
				    sql =" select cust_code, cust_status from customer where cust_code = ? for update nowait " ;
			    }

			    pstmt = conn.prepareStatement(sql);
			    pstmt.setString(1,tranId);
                rs = pstmt.executeQuery();
                if (rs.next())
				{
                    custCode = rs.getString("cust_code");
					custStatus = rs.getString("cust_status");
					isTranLocked = true;
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
                
                if(!isTranLocked)
				{
					System.out.println("Not able to lock the record");
					errString = itmdbAccess.getErrorString("", "VTLCKERR", "","",conn);
					return errString;
				}

            System.out.println("CustCode:: "+tranId);
			sql = "select cust_status from customer WHERE cust_code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if( rs.next() )
			{
				custStatus=rs.getString(1)==null ? "C" : rs.getString(1);
			}	
			if( pstmt != null )
			{
				pstmt.close();
				pstmt = null;
			}
			if ( rs != null )
			{
				rs.close();
				rs = null;
            }
            if( custStatus != null && custStatus.equalsIgnoreCase("P") )
			{
			    //deactivate transaction can not deactivate again
			    System.out.println("Customer Status is Provisional Allotment");
				errString = itmdbAccess.getErrorString("", "VTVARALR", "", "", conn);
				return errString;
			}else{
                
			    String loginCode=genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			    System.out.println("Login Code ------->> "+loginCode);
			    sql="update customer set cust_status = ?, status_date = ? where cust_code = ?";
			    pstmt=conn.prepareStatement(sql);
                pstmt.setString(1, "P");
                pstmt.setTimestamp(2,sysDate);
			    pstmt.setString(3, tranId);
			    updCnt=pstmt.executeUpdate();
			    pstmt.close();
			    pstmt = null;
			    System.out.println("Update count is"+ updCnt);
			    if (updCnt > 0)
			    {	
				    System.out.println("successfully added status");
				    errString = itmdbAccess.getErrorString("", "VTCHBSUC", "", "", conn);
				    conn.commit();
			    }else
			    {
				    System.out.println("Rollback called..........");
				    conn.rollback();
				    return errString;
			    }
            }
		}
		catch(SQLException se)
		{
			System.out.println("SQLException : class CustDocVerification : ");
			se.printStackTrace();
			try
			{
			  conn.rollback();
			}
            catch(Exception e)
            {
                System.out.println("Exception : Occure during rollback........");
                e.printStackTrace();
			}
		}
		catch(Exception e)
		{
			try
			{
				conn.rollback();
			}
			catch (Exception e1)
			{
			}
			e.printStackTrace();
			System.out.println("Exception Class [CustDocVerification]::"+e.getMessage());
			
		}
		finally
		{
			try
			{
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;					
				}	
				if (conn!=null)
				{
					conn.close();
					conn=null;
				}
							
			}
			catch(Exception e)
			{
				System.out.println(e.getMessage());
				e.printStackTrace();				
			}
		}
		System.out.println("[CustDocVerification]errstring :"+errString);
		return errString;
    }
}