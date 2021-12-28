/* 
	Developed by : Kasturi
	Company : Base Information Management Pvt. Ltd
	Version : 1.0
	Date : 27/10/2005
*/
package ibase.webitm.ejb.dis.adv;

import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.ejb.Stateless;

@Stateless // added for ejb3
public class ConsumeOrderCnc extends ActionHandlerEJB implements ConsumeOrderCncLocal, ConsumeOrderCncRemote
{
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
    public String actionHandler() throws RemoteException,ITMException
	{
		return "";
	}

	public String actionHandler(String tranID, String xtraParams, String forcedFlasg) throws RemoteException,ITMException
	{
		String  retString = null;
		try
		{
			retString = actionCancel(tranID,xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception :ConsumeOrdCanc :actionHandler:" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
	    return retString;
	}

	private String actionCancel(String consOrd ,String xtraParams)throws ITMException
	{
		Connection conn = null;
		PreparedStatement pstmt =null;
		ResultSet rs = null;
		String sql = "";
		String updSql = "";
		String errCode="";
		String errString="";
		String status = "";
		int rows=0;
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		ConnDriver connDriver = new ConnDriver();
		try
		{	
		   	//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			
			sql = "SELECT STATUS FROM CONSUME_ORD WHERE CONS_ORDER = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,consOrd);
			rs = pstmt.executeQuery();
			
			System.out.println("ConsumeOrdCanc:actionCancel:sql:"+sql);
			
			if(rs.next())
			{
				status = rs.getString("STATUS");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			System.out.println("consOrd:"+consOrd+":status:"+status+":");
			if(status != null && status.trim().length() > 0 )
			{
				if(status.equalsIgnoreCase("D"))
				{
					errCode ="VTCANC5";
				}
				else if(status.equalsIgnoreCase("C"))
				{
                  errCode ="VTCANCEL2";
				}
				else
			    {
					/* Added by Sandesh to check if any record is not confirmed for given consumption order
					*  In such a case system will show an error message and order cancellation won't proceed.
					*  Date - 20/MAY/2013
					*/
					 
					int confirmed = 0;
					
					sql ="SELECT count(*) FROM CONSUME_ISS WHERE CONS_ORDER = ? " +
							"and (case when confirmed is null then 'N' else confirmed  END)='N'";
					
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,consOrd);
					rs = pstmt.executeQuery();
					
					System.out.println("ConsumeOrdCanc:actionCancel:confirmchk sql:"+sql);

					if(rs.next()){
						
						confirmed = rs.getInt(1);
						System.out.println("CONFIRMED for cancellation : "+confirmed);
					}
					
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					if(confirmed>0){
						
						errCode="VTCANNTCON";
						
					}else{
					
	                    updSql="UPDATE CONSUME_ORD  SET STATUS = 'C',STATUS_DATE = ?"+
								" WHERE CONS_ORDER = ?";
						System.out.println("ConsumeOrdCanc:Cancel:updSql:"+updSql+":");
						pstmt = conn.prepareStatement(updSql);
						pstmt.setDate(1,new java.sql.Date(System.currentTimeMillis()));
						pstmt.setString(2,consOrd);
						rows = pstmt.executeUpdate();
						pstmt.close();
						pstmt = null;
						System.out.println("*ConsumeOrdCanc:rows:"+rows);
						if(rows <= 0)
						{
				         errCode="DS000NR";
						}
						else if(rows > 0)
						{
							conn.commit();
							errCode="VTCANC1";
							System.out.println("\n <==== Table Updated Successfully ====>");
						}
					}// end of cofirmed condition else	
				}//end of else
			}	//end of if
			if (errCode != null  && errCode.trim().length() > 0)
			{
				System.out.println("ConsOrdCanc:errCode:"+errCode);
				errString = itmDBAccess.getErrorString("",errCode,"","",conn);
				System.out.println("errString:"+errString+":");				
			}		  
		}//end try
		catch(Exception e)
		{
			System.out.println("Exception :ConseOrdCanc :" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("Closing Connection.....");
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if (conn != null)
				{
					conn.close();
					conn = null;
				}
			}catch(Exception e){}
		}
		return errString;
	}
}