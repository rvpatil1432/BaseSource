/********************************************************
	Title 	 : 	PContractPostSave[]
	Date  	 : 	25/JUN/18
	Developer:  Pankaj R.

 ********************************************************/
package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.*;
import ibase.webitm.utility.ITMException;
import ibase.utility.E12GenericUtility;
//import ibase.webitm.ejb.sys_UTL.CommonFunctions;
import java.rmi.RemoteException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.w3c.dom.*;


@javax.ejb.Stateless
public class PContractPostSave extends ValidatorEJB implements PContractPostSaveLocal, PContractPostSaveRemote
{
	E12GenericUtility genericUtility = new E12GenericUtility();
	ITMDBAccessEJB dbEjb = new ITMDBAccessEJB() ;
	@Override
	public String postSave() throws RemoteException, ITMException
	{
		return "";
	}

	@Override
	public String postSave(String xmlStringAll, String tranId, String editFlag, String xtraParams, Connection conn) throws RemoteException, ITMException
	{
		System.out.println("PContractPostSave.postSave()");
		String errString = "";
		Document dom = null;
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		//System.out.println("xmlStringAll :: "+xmlStringAll);
		System.out.println(" Tran id = "+tranId);
		dom = genericUtility.parseString(xmlStringAll);
		tranId = genericUtility.getColumnValue("contract_no", dom);
		String maxValue = "";
		try
		{
			String maxValueSql = "SELECT SUM(MAX_VALUE) AS MAX_VALUE FROM PCONTRACT_DET WHERE CONTRACT_NO = ? ";
			pstmt = conn.prepareStatement(maxValueSql);
			pstmt.setString(1, tranId);
			resultSet = pstmt.executeQuery();
			if(resultSet.next())
			{
				maxValue = E12GenericUtility.checkNull(resultSet.getString("MAX_VALUE"));
				if(maxValue.length() == 0)
				{
					maxValue = "0";
				}
			}
			resultSet.close(); resultSet = null;
			pstmt.close(); pstmt = null;
			
			String updateValue = "UPDATE PCONTRACT_HDR SET CONTRACT_AMT = ? WHERE CONTRACT_NO = ?";
			pstmt = conn.prepareStatement(updateValue);
			pstmt.setString(1, maxValue);
			pstmt.setString(2, tranId);
			int updateCnt = pstmt.executeUpdate();
			if(updateCnt > 0)
			{
				conn.commit();
			}
			pstmt.close(); pstmt = null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			errString = dbEjb.getErrorString("", "POSFAIL", "");
			throw new ITMException( e );
		}
		finally
		{
			if(pstmt != null)
			{
				try {
					if (conn != null) {
						conn.close();
					}
					conn = null;
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return errString;
	}
}
