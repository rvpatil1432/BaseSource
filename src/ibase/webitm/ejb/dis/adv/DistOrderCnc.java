/* 
	Developed by : Niraja
	Company : Base Information Management Pvt. Ltd
	Version : 1.0
	Date :15/11/2005
*/
package ibase.webitm.ejb.dis.adv;
import ibase.webitm.utility.*;
import java.rmi.RemoteException;
import java.sql.*;
import javax.ejb.*;

import java.util.*;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.system.config.*;

import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class DistOrderCnc extends ActionHandlerEJB implements DistOrderCncLocal, DistOrderCncRemote
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

	public String actionHandler(String tranID, String xtraParams, String forcedFlag) throws RemoteException,ITMException
	{
		System.out.println("Distorder is called");
		String  retString = null;
		try
		{
			retString = actionCancel(tranID,xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception :Distorder :actionHandler:" + e.getMessage() + ":");
			throw new ITMException(e);
		}
	    return retString;
	}

	private String actionCancel(String distOrder ,String xtraParams)throws ITMException
	{
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String sql = "";
		String updSql = "";
		String errCode = "";
		String errString = "";
		String status = "";
		double qtyShip = 0;
		double qtyRecd = 0;
		int rows = 0;
		int cnt = 0;
		int cnt1 = 0;
		
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		ConnDriver connDriver = new ConnDriver();
		
		try
		{	
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
			sql = "SELECT STATUS FROM DISTORDER	WHERE DIST_ORDER ='"+distOrder+"'";
			System.out.println("Distorder:actionCancel:DISTORDER:sql:"+sql);
			rs = stmt.executeQuery(sql) ;
			if(rs.next())
			{
				status = rs.getString(1);
			}
			System.out.println("Distorder:actionCancel:status:"+status+":");
			//if the order is already closed
			if(status != null && status.trim().length() > 0 && status.equalsIgnoreCase("C"))
			{
				errCode ="VTCANC5";
			}
			else if(status != null && status.trim().length() > 0 && status.equalsIgnoreCase("X"))
			{
				errCode ="VTCANC2";
			}
			//dont allow to cancel order if unconfirmed dist issue is exist
			else 
			{				
				sql="SELECT COUNT(*) FROM DISTORD_ISS WHERE DIST_ORDER ='"+distOrder+"'" + 
					" AND (CASE WHEN CONFIRMED IS NULL THEN 'N' ELSE CONFIRMED END) = 'N'";//Changed on 03/10/06 - jiten
				System.out.println("Distorder:actionCancel:DISTORDER:sql:"+sql);
				rs = stmt.executeQuery(sql) ;
				if(rs.next())
				{
					cnt =rs.getInt(1);
				}
				System.out.println("Distorder:actionCancel:cnt:"+cnt+":");
				if(cnt > 0)
				{
					errCode ="VTCANC6";
				}
				//distord_rcp
				sql="SELECT COUNT(*) FROM DISTORD_RCP WHERE DIST_ORDER ='"+distOrder+"'" + 
				    " AND (CASE WHEN CONFIRMED IS NULL THEN 'N' ELSE CONFIRMED END) = 'N'";//Changed on 26/05/16 - Abhijit 
			    System.out.println("Distorder:actionCancel:DISTORDER:sql:"+sql);
			    rs = stmt.executeQuery(sql) ;
			    if(rs.next())
			      {
				     cnt1 =rs.getInt(1);
			      }
			    System.out.println("cnt@@@@@@@@@@@@@@@@@@@@@@"+cnt1+":");
			    if(cnt1 > 0)
			     {
			    	System.out.println("COUNT is "+cnt1);
				   errCode ="VTCANC6";
			     }
				if(errCode == null || errCode.trim().length() == 0)
				{
					sql="SELECT SUM(QTY_SHIPPED), SUM(QTY_RECEIVED) FROM DISTORDER_DET WHERE DIST_ORDER ='"+distOrder+"'";
					rs = stmt.executeQuery(sql);
					if(rs.next())
					{
						qtyShip = rs.getDouble(1);
						qtyRecd = rs.getDouble(2);
					}
					System.out.println(":qtyShip:"+qtyShip+":qtyRecd:"+qtyRecd+":");
					//if isnull(lc_qty_ship) then lc_qty_ship = 0
					//if isnull(lc_qty_recd) then lc_qty_recd = 0
					if(qtyShip == 0 && qtyRecd == 0)
					{
						status = "X";
					}
					else
					{
						status = "C";
					}
					updSql ="UPDATE DISTORDER SET STATUS ='"+status+"'  WHERE DIST_ORDER ='"+distOrder+"'";
					System.out.println("Distorder:actionCancel:updSql:"+updSql);
					rows = stmt.executeUpdate(updSql);
					if(rows > 0)
					{
						conn.commit();
						errCode = "VTCANC1";
						
						System.out.println("\n <==== DISTORDER Updated Successfully ====>");
					}
				}
			}
			if (errCode != null  && errCode.trim().length() > 0)
			{
				System.out.println("Distorder:errCode:"+errCode);
				//errString = itmDBAccess.getErrorString("",errCode,"","",conn);	// commented by Abdul 06/08/2007 - as Prompt messages not showing to user			
			   errString = itmDBAccess.getErrorString("",errCode,"","",conn);   // added by  Abdul 06/08/2007
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :Distorder :" + e.getMessage() + ":");
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