package ibase.webitm.ejb.dis.adv;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ejb.Stateless;

import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.dis.PostOrderProcess;
import ibase.webitm.ejb.dis.SOrderForm;
import ibase.webitm.ejb.dis.SOrderFormPost;
import ibase.webitm.utility.ITMException;;

@Stateless
public class SalesOrderPost extends ActionHandlerEJB implements SalesOrderPostLocal, SalesOrderPostRemote
{
	E12GenericUtility genericUtility = new E12GenericUtility();
	
	public String actionHandler(String tranId, String xtraParams, String forcedFlag) throws RemoteException, ITMException
	{
		String returnString = "";
		Connection conn = null;
		try 
		{
			System.out.println(".......tranId......."+tranId);
			System.out.println(".......xtraParams..."+xtraParams);
			System.out.println(".......forcedFlag..."+forcedFlag);
			if(tranId!=null && tranId.trim().length() > 0 )
			{
				conn = getConnection();
				returnString = actionHandler(tranId, xtraParams, forcedFlag, conn);
			}
		}
		catch(Exception e)
		{
			try
			{
				conn.rollback();
			}
			catch(Exception e1)
			{
				e1.printStackTrace();
			}
			
			System.out.println("Exception SalesOrderPost actionHandler[Without Connection]: "+e);
			e.printStackTrace();	
			throw new ITMException(e);
		}
		finally
		{		
			try
			{
				conn.commit();
				if(conn != null)
				{
					conn.close();
					conn = null;
				}
			}
			catch(Exception e)
			{
				System.out.println("Exception : "+e);
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		System.out.println("returnString in SalesOrderPost actionHandler[Without Connection]: "+returnString);
		return returnString;
	}
	
	public String actionHandler(String tranId, String xtraParams, String forcedFlag, Connection conn) throws RemoteException, ITMException
	{
		String returnString = "", sql = null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String confirmed  = "", status = "";
		String userId = "";
		UserInfoBean userInfo = null;
		SOrderFormPost sorderPost = new SOrderFormPost();
		genericUtility = new E12GenericUtility();
		try
		{
			itmDBAccessEJB = new ITMDBAccessEJB();
			userInfo = getUserInfo();
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");			
			sql = "SELECT confirmed, status FROM sorder WHERE sale_order = ?";
			
			pstmt  = conn.prepareStatement(sql);
			pstmt.setString(1,tranId);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				confirmed = checkNull(rs.getString("confirmed"));
				status = checkNull(rs.getString("status"));
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
			System.out.println("confirmed:::["+confirmed+"] status:::["+status+"]");
			if("C".equalsIgnoreCase(status.trim()))
			{
				System.out.println("--transaction already closed--");
				returnString = itmDBAccessEJB.getErrorString("","VTSORDPT02","","",conn);
				return returnString;
			}
			else if("X".equalsIgnoreCase(status.trim()))
			{
				System.out.println("--transaction already cancelled--");
				returnString = itmDBAccessEJB.getErrorString("","VTSORDPT03","","",conn);
				return returnString;
			}
			if(!("Y".equalsIgnoreCase(confirmed)))
			{
				System.out.println("--transaction not confirmed--");
				returnString = itmDBAccessEJB.getErrorString("","VTSORDPT01","","",conn);
				return returnString;
			}
			
			returnString = sorderPost.postOrder(tranId, xtraParams, userInfo, conn);
			// 23-apr-2021 manoharan no need to change the message returned from post order
			/*if(returnString != null && returnString.trim().length() > 0 && returnString.indexOf("VTPOST03") > -1)
			{
				returnString = itmDBAccessEJB.getErrorString("","VTSORDPT04","","",conn);
			}*/
		}
		catch( Exception e)
		{	
			System.out.println("Exception SalesOrderPost actionHandler[With Connection]: "+e);
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
				sorderPost = null;
			}
			catch(Exception e)
			{
				System.out.println("Exception in SalesOrderPost.. "+e.getMessage());
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		System.out.println("returnString in SalesOrderPost actionHandler[With Connection]: "+returnString);
		return returnString;
	}
		
	public String checkNull(String inputStr) throws ITMException
	{
		String retString = "";
		if(inputStr == null || inputStr.trim().length() == 0)
		{
			retString = "";
		}
		else
		{
			retString = inputStr;
		}
		return retString;
	}
}