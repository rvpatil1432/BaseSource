package ibase.webitm.ejb.dis;

import java.sql.*;
import ibase.system.config.ConnDriver;
import ibase.webitm.utility.ITMException;
public class EmpFlightsaveBean 
{
	public String getXmlData(String flightCode,String tranId )throws ITMException
	{	
		Connection conn = null;
		ConnDriver connDriver = new ConnDriver(); 
		String sql="";
		int upd=0;
		PreparedStatement pstmt = null;
		System.out.println("@@@@@@@@@---------EmpFlightsaveBean EJB called...");
		try
		{	conn = connDriver.getConnectDB("DriverITM");
			//conn = getConnection();
			
			sql = " update emp_travel_info set flight_code = ?  where tran_id  =  ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, flightCode);
			pstmt.setString(2, tranId);
			upd =  pstmt.executeUpdate();
			pstmt.close();pstmt = null;			
			System.out.println(" status updated:: "+upd);
			if(upd > 0)
			{	conn.commit();
				System.out.println("  information submited::");
			}
				
			conn.close();conn = null;
		}
		catch(Exception e)
		{
			System.out.println("Exception :EmpFlightsaveBean ::" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}

		return "";
	}

}