/*
 * Request Id=W19LSUN009
 * author: Mrunalini Sinkar
 * date 24-march-2020
 * 
 */
package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.w3c.dom.Document;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;


public class StpStatusChangePos extends ValidatorEJB 
{
	/**
	 * Default constructor.
	 */
	private E12GenericUtility genericUtility = new ibase.utility.E12GenericUtility();

	public StpStatusChangePos()
	{
		// TODO Auto-generated constructor stub
	}
	protected static String database = CommonConstants.DB_NAME;

	public String postSave() throws RemoteException, ITMException
	{
		return "";
	}
	
	public String postSave(String domString,String tranId, String editFlag, String xtraParams, Connection conn) throws RemoteException, ITMException
	{
		String resString="";
		Document dom = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		try
		{
			String loginCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			System.out.println("xmlStringAll::"+domString);
			dom = genericUtility.parseString(domString);
			String salesPers = checkNull(genericUtility.getColumnValue("sales_pers", dom, "1"));
			String stpStatus = checkNull(genericUtility.getColumnValue("stp_status", dom, "1"));
			String stpStatusReason = checkNull(genericUtility.getColumnValue("stp_status_reason", dom, "1"));
			sql="UPDATE SALES_PERS SET STP_STATUS=? ,STP_STATUS_REASON=? WHERE SALES_PERS=?";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, stpStatus);
			pstmt.setString(2, stpStatusReason);
			pstmt.setString(3, salesPers);
			int updateCnt= pstmt.executeUpdate();
			System.out.println("updateCnt is["+updateCnt+"]");
			

		}
		catch (Exception e)
		{
			System.out.println("Exception  :==>\n" + e.getMessage());
			throw new ITMException(e);
		} 
		finally
		{
			try
			{
				if (conn != null)
				{
					/*if (isError)
					{
						conn.rollback();
					}*/
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
				}
			}
			catch (Exception e)
			{
				System.out.println("Exception :==>\n" + e.getMessage());
				throw new ITMException(e);
			}
		}
		return resString;
	}
	private String checkNull(String input)
	{
		if (input == null || "null".equalsIgnoreCase(input))
		{
			input= "";
		}
		return input.trim();
	}
	
}