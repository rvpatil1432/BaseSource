
package ibase.webitm.ejb.dis;

import ibase.scheduler.utility.interfaces.Schedule;
import ibase.system.config.*;
import ibase.utility.CommonConstants;
import ibase.utility.BaseException;

import ibase.webitm.ejb.dis.VerifyAll;
import ibase.webitm.utility.ITMException;

import java.io.*;
import java.util.*;
import java.sql.*;
import java.text.SimpleDateFormat;

import java.rmi.RemoteException;
import javax.ejb.*;

import org.w3c.dom.*;
import java.rmi.*;
import javax.naming.*;

public class VerifySchedular implements Schedule
{
	public String schedule(String name)throws Exception
	{
		Connection conn = null;
		Statement stmt = null;
		String result = "";
		String sql = "";
		try
		{
			
			VerifyAll verifyAll = null;
			try
			{
				verifyAll = new VerifyAll();
				System.out.println("AutoVerification.......");
			}
			catch(Exception e)
			{
				System.out.println("Exception []::"+e.getMessage());
				e.printStackTrace();
			}
			
			ConnDriver connDriver = new ConnDriver();
			
			sql = "SELECT TRAN_ID FROM CHARGE_BACK WHERE VERIFY_FLAG = 'N' OR VERIFY_FLAG IS null ";
			conn = connDriver.getConnectDB("DriverITM");
			//conn = getConnection();
			stmt = conn.createStatement();
			ResultSet  rs = stmt.executeQuery(sql);
			while(rs.next())
			{ 
				String tranId = "";
				tranId = rs.getString(1);
				result = verifyAll.verifyAll(tranId);
			}	
			stmt.close();
			stmt = null; 
			verifyAll = null;
		}
		catch(Exception ex)
		{
			System.out.println("Exception []::"+ sql +ex.getMessage());
			ex.printStackTrace();
			throw new ITMException(ex); //Added By Mukesh Chauhan on 06/08/19
		}	
		finally
		{
			try
			{
				if(conn != null)
				{					
					if(stmt != null)
					{
						stmt.close();
						stmt = null;
					}
					conn.close();
				}
			}
			catch (Exception e)
			{
				throw e;
			}
		}
		return "";
	}
	public  String schedulePriority(String paramString)
	{
		return "";
	}
	public String schedule(HashMap map)throws Exception
	{
		return schedule((String)map.get("PROCESS_NAME"));
	}	
}