package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.E12CreateBatchLoadEjb;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.sys.CreateRCPXML;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.ejb.Stateless;

import org.w3c.dom.*;


@Stateless
public class SRLContainerConfirm extends ActionHandlerEJB implements SRLContainerConfirmLocal, SRLContainerConfirmRemote
{
	E12GenericUtility genericUtility = new E12GenericUtility();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();

	public String confirm(String tranID,String xtraParams, String forcedFlag) throws RemoteException,ITMException
	{
		String retString = "";
		Connection conn = null;
		try
		{
			retString = this.confirm(tranID, xtraParams, forcedFlag, conn);
			System.out.println("retString:::::"+retString);
		}
		catch(Exception e)
		{
			System.out.println("Exception in [SRLContainerConfirm] confirm " + e.getMessage());
			throw new ITMException(e);
		}
		return retString;
	}

	public String confirm(String tranId, String xtraParams, String forcedFlag, Connection conn) throws RemoteException,ITMException
	{
		System.out.println(" ========= Inside SRLContainerConfirm confirm ============= ");
		System.out.println(" =========  tranId ============= "+tranId +"xtraParams ::::::::::::: " + xtraParams);
		String errString = "", sql = "", srlStatus = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null; 
		boolean isError = false;
		
		try
		{
			if(conn == null)
			{
				conn = getConnection();
			}
			
			sql = "select status from "
				+ "srl_container "
				+ "where serial_no = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				srlStatus = checkNullAndTrim(rs.getString("status"));
				System.out.println("Status of SRL Container" + srlStatus);
			}
			if(rs != null){rs.close();rs = null;}
			if(pstmt != null){pstmt.close();pstmt = null;}
			
			if("A".equalsIgnoreCase(srlStatus))   // --- A ==> CONFIRMED
			{
				errString = itmDBAccessEJB.getErrorString("", "VTDIST26", "", "", conn); //Transacation Already Confirmed  !
				isError = true;
				return errString;
			}
			else if("C".equalsIgnoreCase(srlStatus))    //--- C == > SPLITTED
			{
				errString = itmDBAccessEJB.getErrorString("", "VTCONFMSP ", "", "", conn);//Selected Transaction is already Split.You can not split record.
				isError = true;
				return errString;
			}
			else
			{
				sql = "update srl_container set status ='A' where serial_no=?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				int j = pstmt.executeUpdate();  System.out.println("j is == ["+j+"]");
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				
				if(j > 0)
				{
					errString = itmDBAccessEJB.getErrorString("", "CONFSUCCES", "", "", conn);//Selected Transaction Confirmed Successfully!!
					isError = false;
					return errString;
				}
			}
		}
		catch(Exception e)
		{
			try
			{
				isError = true;
				System.out.println("Exception "+e.getMessage());
				e.printStackTrace();			
				throw new ITMException(e);
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
				throw new ITMException(e1);
			}
		}
		finally
		{
			try
			{	
				System.out.println("isError in Finally SRLContainerConfirm="+isError);			
				if ( isError )
				{
					System.out.println("--------------rollback------------");
					conn.rollback();
				}
				else
				{
					System.out.println("----------commmit-----------");
					conn.commit(); 
				}
				
				if ( rs != null )
				{
					rs.close();
					rs = null;
				}
				if ( pstmt != null )
				{
					pstmt.close();
					pstmt = null;
				}
				if (conn != null)
				{
					conn.close();
					conn = null;
				}
				
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return errString;
	}

	private String checkNullAndTrim( String inputVal )
	{
		if ( inputVal == null )
		{
			inputVal = "";
		}
		else
		{
			inputVal = inputVal.trim();
		}
		return inputVal;
	}
}


