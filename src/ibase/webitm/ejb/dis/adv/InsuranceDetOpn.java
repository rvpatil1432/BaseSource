/*	
		Developed by	: Hatim Laxmidhar
		Started On		: 31/12/2005
		Purpose  			: This  will set the STATUS of INSURANCE_DET to 'O' 
										and adds UTILISED_PREMIUM and UTILISED_VALUE into INSURANCE table.
		Window				: w_insruance_det
*/

package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import java.sql.*;
import javax.ejb.*;
import java.util.StringTokenizer;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.system.config.*;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class InsuranceDetOpn extends ActionHandlerEJB implements InsuranceDetOpnLocal, InsuranceDetOpnRemote
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
	public String actionHandler(String tranId, String xtraParams, String forcedFlag) throws RemoteException,ITMException
	{

		String  retString = null;
		try
		{
			retString = actionOpen(tranId, xtraParams, forcedFlag);
		}
		catch(Exception e)
		{
			System.out.println("Exception :InsuranceDetOpn :actionHandler :" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return retString;
	}

	public String actionOpen(String tranId, String xtraParams, String forcedFlag) throws RemoteException,ITMException, SQLException
	{
		String status = "", sql="";
		String tranIdIns = "", refId = "", refSer = "", certNo = "",retString = "";
		double utilVal = 0d, utilPremium = 0d;
		StringTokenizer stz = null;
		Connection conn=  null;
		Statement stmt = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ConnDriver connDriver = new ConnDriver();
		int cnt = 0;
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			stz = new StringTokenizer(tranId, ":");
			if (stz.hasMoreTokens())
			{
				refId = stz.nextToken();
			}
			if (stz.hasMoreTokens())
			{
				refSer = stz.nextToken();
			}
			if (stz.hasMoreTokens())
			{
				tranIdIns = stz.nextToken();
			}
			if (stz.hasMoreTokens())
			{
				certNo = stz.nextToken();
			}

			System.out.println("REF_ID: " + refId);
			System.out.println("REF_SER: " + refSer);
			System.out.println("TRAN_ID__INS: " + tranIdIns);
			System.out.println("CERT_NO: " + certNo);

			sql = "SELECT PREMIUM_VALUE, INS_VALUE, STATUS FROM INSURANCE_DET " +
						"WHERE REF_ID				= '" + refId		 + "'" +
						"AND	 REF_SER			= '" + refSer		 + "'" +	
						"AND	 TRAN_ID__INS = '" + tranIdIns + "'" +
						"AND	 CERT_NO			= '" + certNo		 + "'" ;

			System.out.println(sql);
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			if (rs.next())
			{
				utilPremium = rs.getDouble(1);
				utilVal = rs.getDouble(2);
				status = rs.getString(3);

				System.out.println("premium_value : " + utilPremium);
				System.out.println("ins_value : " + utilVal);
				System.out.println("status : " + status);

				if (status.trim().equalsIgnoreCase("X"))
				{
					sql = "UPDATE INSURANCE_DET SET STATUS = 'O' " +
								"WHERE REF_ID				= ?" +
								"AND	 REF_SER			= ?" +	
								"AND	 TRAN_ID__INS = ?" +
								"AND	 CERT_NO			= ?" ;
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,refId);
					pstmt.setString(2,refSer);
					pstmt.setString(3,tranIdIns);
					pstmt.setString(4,certNo);
					cnt = pstmt.executeUpdate();
					
					if (cnt > 0)
					{
						sql = "UPDATE INSURANCE " +
									"SET UTILISED_PREMIUM = UTILISED_PREMIUM + ? ," +
									"UTILISED_VALUE = UTILISED_VALUE + ? " +
									"WHERE TRAN_ID = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setDouble(1,utilPremium);
						pstmt.setDouble(2,utilVal);
						pstmt.setString(3,tranIdIns);
						cnt = pstmt.executeUpdate();
						if (cnt > 0)
						{
							conn.commit();
							System.out.println("Update Successfull!");
							retString = itmDBAccess.getErrorString("","VTSUCC","","",conn);
						}
						else 
						{
							conn.rollback();
							System.out.println("Update failed!");
							retString = itmDBAccess.getErrorString("","UNSCC","","",conn);
						}
					}
				}
			}
		}
		catch (SQLException sqx)
		{
			conn.rollback();
			System.out.println("The sqlException occure in InsuranceDetOpn :"+sqx);
			throw new ITMException(sqx);
		}
		catch (Exception e)
		{
			System.out.println("The Exception occure in InsuranceDetOpn :"+e);
			throw new ITMException(e);
		}
		finally
		{
			try
			{	
				if (stmt!=null)
				{
					stmt.close();
					stmt=null;
				}
				if (pstmt!=null)
				{
					pstmt.close();
					pstmt=null;
				}
				if (conn!=null)
				{
					conn.close();
					conn = null;
				}
			}
			catch (Exception e){}
		}
		System.out.println("returning String from InsuranceDetOpn :actionOpen :"+retString);
		return retString;
	}
}