/********************************************************
	Title : SReturnAmdConf
	Date  : 04/03/2012
	Author: Kunal

 ********************************************************/
package ibase.webitm.ejb.dis;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.*;

import java.sql.*;
import ibase.webitm.utility.ITMException;
import javax.ejb.Stateless;
import java.util.Date;

@Stateless
public class SReturnAmdConf  extends ActionHandlerEJB implements SReturnAmdConfLocal,SReturnAmdConfRemote
{

	public String actionHandler()  throws RemoteException,ITMException
	{
		//System.out.println("item actionHandler() called.............");
		return "";
	}
	public String actionHandler(String actionType, String xmlString, String objContext, String xtraParams) throws RemoteException, ITMException
	{
		//System.out.println("item actionHandler(...) called............");
		String str = "";
		
		System.out.println("actionType---"+actionType);
		System.out.println("xmlString---"+xmlString);
		System.out.println("objContext---"+objContext);
		System.out.println("xtraParams---"+xtraParams);
		return str;
	}

	public String confirm(String tranId, String xtraParams, String forcedFlag) throws RemoteException,ITMException
	{
		System.out.println("SReturnAmd confirm called..............");
		
		String lrNo = "";
		String transMode = "";
		String lorryNo = "";
		String frtType = "";
		String refId = "";
		String confirmed = ""; 
		String sql = "";
		String sql1 = "";
		int status = 0;
		double frtAmt = 0.0;
		String tranCode="";
		Date lrDate = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		String errString = null;
		ResultSet rs = null;
		ResultSet rs1 = null;
		System.out.println("Confirm Action Called for Train Id =:::"+tranId);
		try
		{

			itmDBAccessEJB = new ITMDBAccessEJB();
			ConnDriver connDriver = null;
			connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			if( errString == null || errString.trim().length() == 0 )
			{

				sql1 = "select (case when confirmed is null then 'N' else confirmed end)AS confirmed from sreturn_amd where tran_id = ?";
				pstmt1 = conn.prepareStatement(sql1);
				pstmt1.setString(1,tranId);
				rs1 = pstmt1.executeQuery();
				while(rs1.next())
				{
					System.out.println("Tranc Check ");
					confirmed = rs1.getString("confirmed");
					System.out.println("confirmed ="+confirmed);
					if(confirmed.equals("Y"))
					{
						System.out.println("Not Update");
						errString = itmDBAccessEJB.getErrorString("","VTCONF8   ","","",conn);
					}
					else
					{
						System.out.println("Update");
						sql = "select (case when lr_no is null then ' ' else lr_no end)AS lr_no,lr_date,(case when trans_mode is null then ' ' else trans_mode end)AS trans_mode,(case when lorry_no is null then ' ' else lorry_no end)as lorry_no,(case when frt_type is null then ' 'else frt_type end)as frt_type,(case when frt_amt is null then 0 else frt_amt end)as frt_amt,(case when tran_code is null then ' ' else tran_code end) as tran_code,ref_id from sreturn_amd where tran_id = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,tranId);
						rs = pstmt.executeQuery();
						while(rs.next())
						{
							System.out.println("Inside REsult");
							lrNo = rs.getString("lr_no");
							lrDate = rs.getDate("lr_date");
							transMode = rs.getString("trans_mode");
							lorryNo = rs.getString("lorry_no");
							frtType = rs.getString("frt_type");
							frtAmt = rs.getDouble("frt_amt");
							tranCode = rs.getString("tran_code");//added by priyanka on 03/03/15 as per manoj sharma instructiona
							refId = rs.getString("ref_id");
							System.out.println("LrNO="+lrNo);
							System.out.println("LrDate="+lrDate);
							System.out.println("transMode="+transMode);
							System.out.println("lorryNO="+lorryNo);
							System.out.println("Frt Type="+frtType);
							System.out.println("Frt Amt="+frtAmt);
							System.out.println("Ref Id ="+refId);
							System.out.println("tranCode ="+tranCode);//added by priyanka on 03/03/15 as per manoj sharma instruction
						}
						pstmt.close();
						pstmt = null;
						rs.close();
						rs = null;
						//
						sql = "update SRETURN set LR_NO = ? ,LR_DATE = ? ,LORRY_NO= ? ,TRANS_MODE= ?,FRT_TYPE =?,FRT_AMT = ?,TRAN_CODE= ? WHERE TRAN_ID = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,lrNo);
						if(lrDate == null) 
						{
							pstmt.setDate(2,null);
						}
						else
						{
							pstmt.setDate(2,new java.sql.Date(lrDate.getTime()));
						}
						pstmt.setString(3,lorryNo);
						pstmt.setString(4, transMode);
						pstmt.setString(5, frtType);
						pstmt.setDouble(6, frtAmt);
						pstmt.setString(7, tranCode);//added by priyanka on 03/03/15 as per manoj sharma instruction
						pstmt.setString(8, refId);
						status =  pstmt.executeUpdate();
						System.out.println("nO OF ROWS UPADTE="+status);
						conn.commit();
						
						pstmt.close();
						pstmt = null;
						
						if(status == 1)
						{
							sql = "update sreturn_amd set confirmed = 'Y',conf_date = sysdate where tran_id = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,tranId);
							System.out.println("nO OF transaction conf is ="+pstmt.executeUpdate());
							//conn.commit();

							pstmt.close();
							pstmt = null;

							errString = itmDBAccessEJB.getErrorString("","VMSRETAMDC","","",conn);
						}
					}
				}
				pstmt1.close();
				pstmt1 = null;
				rs1.close();
				rs1 = null;
				
				
			}
		}
		catch( Exception e)
		{
			if(conn!=null)
			{
				try {
					conn.rollback();
				} catch (SQLException ex) {
					Logger.getLogger(SReturnAmdConf.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
			e.printStackTrace();
		}
		finally
		{
			if(conn!=null)
			{
				try {
					conn.commit();
					conn.close();
					conn = null;
				} catch (SQLException ex) {
					Logger.getLogger(SReturnAmdConf.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
		return errString;
	}

}