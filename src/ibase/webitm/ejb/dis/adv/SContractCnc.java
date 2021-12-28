/* 
	Developed by : Niraja
	Company : Base Information Management Pvt. Ltd
	Version : 1.0
	Date :08/11/2005
*/
package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import java.sql.*;
import javax.ejb.*;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.system.config.*;

import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class SContractCnc extends ActionHandlerEJB implements SContractCncLocal, SContractCncRemote
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

	public String actionHandler(String tranID, String xtraParams,String forcedFlag) throws RemoteException,ITMException
	{
		System.out.println("SContract is called");
		String  retString = null;
		try
		{
			retString = actionCancel(tranID,xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception :SContract :actionHandler:" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
	    return retString;
	}

	private String actionCancel(String contractNo ,String xtraParams)throws ITMException
	{
		Connection conn = null;
		Statement stmt = null;
		PreparedStatement pstmt =null;
		ResultSet rs = null;
		String sql = "";
		String updSql = "";
		String errCode = "";
		String errString = "";
		String conf = "";
		String status = null;
		double relQty = 0;
		int rows = 0;
		
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		ConnDriver connDriver = new ConnDriver();
		try
		{	
		   	//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
			sql = "SELECT CONFIRMED, STATUS FROM SCONTRACT WHERE CONTRACT_NO ='"+contractNo+"'";
			System.out.println("SContract:actionCancel:sql:"+sql);
			rs = stmt.executeQuery(sql);
			if(rs.next())
			{
				conf = rs.getString(1);
				status = rs.getString(2);
				// 
				if (status == null)
				{
					status = "";
				}
			}
			System.out.println("SContract:actionCancel:status:"+status+":conf:"+conf+":");
			//if sqlca.sqlcode <> 0 then
			if(status != null)
			{
				if(status.equalsIgnoreCase("X"))
				{
					errCode = "VTCANC2";
				}
				else
				{
					sql ="SELECT REL_QTY FROM SCONTRACTDET WHERE CONTRACT_NO ='"+contractNo+"' ORDER BY LINE_NO";
					System.out.println("SContract:actionCancel:sql:"+sql);
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						rs.beforeFirst();
						while(rs.next())
						{
							relQty = rs.getDouble(1);
							System.out.println("SContract:actionCancel:relQty:"+relQty+":");
							if(relQty != 0)
							{
								errCode = "VTCANC3";
								break;
							}
						}
					}
					else
					{
						errCode = "VTACTDTL";
					}
					if(errCode == null || errCode.trim().length()== 0)
					{
						updSql = "UPDATE SCONTRACT SET STATUS = 'X', STATUS_DATE = ?, STATUS_REMARKS = 'Cancelled' "+
								" WHERE CONTRACT_NO = ?";
						System.out.println("SContract:actionCancel:updSql:"+updSql);
						pstmt = conn.prepareStatement(updSql);
						pstmt.setDate(1,new java.sql.Date(System.currentTimeMillis()));
						pstmt.setString(2,contractNo);
						rows = pstmt.executeUpdate();
						System.out.println("rows:"+rows);
						if(rows > 0)
						{
							conn.commit();
							errCode = "VTCANC1";
							System.out.println("\n <==== SCONTRACT Updated Successfully ====>");
						}
					}
				}//else status != X
			}// status != null(End If)
			else
			{
				errCode ="VTMCONF20";
			}
			if (errCode != null  && errCode.trim().length() > 0)
			{
				System.out.println("SContract:errCode:"+errCode);
				errString = itmDBAccess.getErrorString("",errCode,"","",conn);
				System.out.println("errString:"+errString+":");
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :SContract :" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("Closing Connection.....");
				conn.close();
				conn = null;
			}catch(Exception e){}
		}
		return errString;
	}
}		