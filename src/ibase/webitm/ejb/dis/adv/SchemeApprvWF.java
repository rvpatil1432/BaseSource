package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

//import com.ibm.db2.jcc.b.co;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.dis.GenericWorkflowClass;
import ibase.webitm.utility.ITMException;

public class SchemeApprvWF extends ActionHandlerEJB 
{

	String responseFlag = "";
	public String confirm(String tranID, String xtraParams, String forcedFlag) throws RemoteException,ITMException
	{

		System.out.println("SchemeConf.confirm() :: ");
		String retString = "";
		Connection conn = null;
		try
		{
			retString = this.confirm(tranID, xtraParams, forcedFlag, conn);
		}
		catch(Exception e)
		{
			System.out.println("SchemeConf Exception.confirm() : "+e);
			throw new ITMException(e);
		}
		return retString;

	}

	public String confirm(String tranID,String xtraParams, String forcedFlag,String userInfoStr,String responseFlag) throws RemoteException,ITMException
	{
		System.out.println("SchemeConf.confirm() userInfo responseFlag : "+responseFlag);
		String retString = "";
		String appResult="", userId = "";
		Connection conn = null;
		ConnDriver connDriver = new ConnDriver();
		PreparedStatement pStmt = null;
		ValidatorEJB validatorEJB = new ValidatorEJB();
		try
		{
			userId = validatorEJB.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			UserInfoBean userInfo = new UserInfoBean(userInfoStr);
			String transDB = userInfo.getTransDB();
			System.out.println("#### TransDB connection in : "+transDB);

			if (transDB != null && transDB.trim().length() > 0)
			{
				conn = connDriver.getConnectDB(transDB);
			}
			else
			{
				conn = connDriver.getConnectDB("DriverITM");
			}
			conn.setAutoCommit(false);
			connDriver = null;
			System.out.println("Response flag:"+responseFlag);
			/*retString = this.confirm(tranID, xtraParams, forcedFlag, conn, responseFlag);*/
			String updateResponseFlag="UPDATE SCHEME_APPRV SET  APRV_STATUS= ?, EMP_CODE_APPRV = ? WHERE TRAN_ID = ?";
			pStmt=conn.prepareStatement(updateResponseFlag);
			pStmt.setString(1, responseFlag);
			pStmt.setString(2, userId);
			pStmt.setString(3, tranID);
			int i=pStmt.executeUpdate();
			System.out.println("Uodated  : "+i);
			pStmt.close();
			if(i>0)
			{
				conn.commit();
			}
			if(responseFlag.equalsIgnoreCase("A"))
			{
				SchemeConf sc=new SchemeConf();
				sc.confirm(tranID, xtraParams, forcedFlag,conn);
			}
          
			System.out.println("appresult:"+appResult);
			//return retString;	
		}
		catch(Exception e)
		{
			System.out.println("Exception in [IndentReqConf] confirm " + e.getMessage());
			throw new ITMException(e);
		}
		//return retString;
		return "1";
	}

	/*public String confirm(String tranID, String xtraParams, String forcedFlag, Connection conn, String responseFlag) throws RemoteException,ITMException
	{
		this.responseFlag = responseFlag;
		return confirm(tranID, xtraParams, forcedFlag, conn);
	}*/

	public String confirm(String tranID, String xtraParams, String forcedFlag, Connection conn) throws RemoteException,ITMException
	{

		System.out.println("================ SchemeApprvWF : confirm ================");
		String userId = "";
		String errString = "";
		ValidatorEJB validatorEJB = null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		String sql = "";
		String confirmed = "",retString = "", apprvStatus= "", wf_status = "";
		String[] authencate = new String[2];
		authencate[0] = "";
		authencate[1] = "";
		boolean isError = false;

		try 
		{
			itmDBAccessEJB = new ITMDBAccessEJB();
			validatorEJB = new ValidatorEJB();
			if(conn == null)
			{
				conn = getConnection();
				conn.setAutoCommit(false);
			}
			userId = validatorEJB.getValueFromXTRA_PARAMS(xtraParams, "loginCode");

			System.out.println("tran_id ..> " + tranID+" userId: "+userId+" xtraParams: "+xtraParams+" forcedFlag: "+forcedFlag);

			sql = "SELECT CONFIRMED, APRV_STATUS FROM SCHEME_APPRV WHERE TRAN_ID = ? ";
			pStmt = conn.prepareStatement(sql);
			pStmt.setString(1, tranID);
			rs = pStmt.executeQuery();

			if (rs.next()) 
			{
				confirmed  = E12GenericUtility.checkNull(rs.getString("CONFIRMED"));
				apprvStatus = E12GenericUtility.checkNull(rs.getString("APRV_STATUS"));
			}
			System.out.println("confirmed : "+confirmed);
			rs.close();
			pStmt.close();

			if(!"Y".equalsIgnoreCase(confirmed) || confirmed.length() <= 0)
			{
				GenericWorkflowClass wfGenericClass = new GenericWorkflowClass();
				retString = wfGenericClass.invokeWorkflow(conn, tranID, xtraParams, "w_scheme_apprv", "scheme_apprv");
				System.out.println("return String : "+retString);
				errString = new ITMDBAccessEJB().getErrorString("", "VTSUCORDER", "","",conn);
			}
			else
			{
				errString = new ITMDBAccessEJB().getErrorString("", "VTMWFALCNF", "","",conn);
			}
		} 
		catch (Exception e) 
		{
			isError = true;
			System.out.println("SchemeApprvWF.confirm() : "+e);
			e.printStackTrace();
			//errString = itmDBAccessEJB.getErrorString("", "VFMWRKFLWF", userId,"",conn);
			throw new ITMException(e);
		} 
		finally 
		{
			if (conn != null) 
			{
				try 
				{
					if (isError) 
					{
						conn.rollback();
						System.out.println("SchemeApprvWF: confirm: Connection Rollback");
					} 
					else 
					{
						conn.commit();
						System.out.println("SchemeApprvWF: confirm: Connection Commit");
					}
					conn.close();
					conn = null;
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
		}
		return errString;

	}
	private String checkNull(String str) 
	{
		if(str == null)
		{
			str = "";
		}
		else
		{
			str = str.trim();
		}
		return str;
	}

	private void closePstmtRs(PreparedStatement pStmt, ResultSet rs) 
	{
		if (pStmt != null) 
		{
			try 
			{
				pStmt.close();
			} catch (SQLException localSQLException1) {
			}
			pStmt = null;
		}
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			rs = null;
		}


	}

}
