package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.ejb.Stateless;

@Stateless
public class ProofOfDeliveryReject extends ActionHandlerEJB implements ProofOfDeliveryRejectLocal,ProofOfDeliveryRejectRemote 
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	public String actionHandler(String tranId, String xtraParams, String forcedFlag) throws RemoteException,ITMException
	{
		System.out.println("--------------actionHandler method of ProofOfDeliveryReject ------------- : ");
		String retString = "";		
		try
		{						
				retString = reject(tranId,xtraParams,forcedFlag);			
		}
		catch(Exception e)
		{
			System.out.println("Exception :ProofOfDeliveryReject :actionHandler():" + e.getMessage() + ":");
			retString = genericUtility.createErrorString(e);
			e.printStackTrace();
		}
		
		return retString;
	}
	
	private String reject(String tranId,String xtraParams,String forcedFlag) throws ITMException 
	{
		System.out.println("---------Class : ProofOfDeliveryReject-->> reject method called-----------");		
		ResultSet rs=null;
		Connection conn=null;
		PreparedStatement pstmt=null;
		String errorString="",sql="",loginCode="";		
		String errString="";
		int updCnt=0;
		//GenericUtility genericUtility=new GenericUtility();
		try
		{
			ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
			ConnDriver connDriver = null;
			connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 13-06-2016 :START
			//conn = connDriver.getConnectDB("DriverValidator");
			conn = getConnection();
			//Changes and Commented By Bhushan on 13-06-2016 :END
			conn.setAutoCommit(false);			
			String wfStatus="",confirmed="",rejectionRemark="";
			sql = "select confirmed,wf_status from spl_sales_por_hdr WHERE tran_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs=pstmt.executeQuery();
			if( rs.next() )
			{
				confirmed =rs.getString(1)==null ? "N":rs.getString(1);
				wfStatus =rs.getString(2)==null ? "O":rs.getString(2);
			}	
			if( pstmt != null )
			{
				pstmt.close();
				pstmt = null;
			}
			if ( rs != null )
			{
				rs.close();
				rs = null;
			}
			System.out.println("Transaction Status------->>["+wfStatus+"]");
			if( wfStatus != null && wfStatus.equalsIgnoreCase("O") )
			{
				//Opened Transaction cannot be rejected 
				errString = itmDBAccess.getErrorString("", "VTOPTCRJ", "", "", conn);
				return errString;
			}	
			if( confirmed != null && confirmed.equalsIgnoreCase("Y") )
			{
				//Confirmed Transaction can not be rejected 
				errString = itmDBAccess.getErrorString("", "VTCONFCRJ", "", "", conn);
				return errString;
			}	
			if( wfStatus != null && wfStatus.equalsIgnoreCase("R") )
			{
				//Already rejected
				errString = itmDBAccess.getErrorString("", "VTALREJ", "", "", conn);
				return errString;
			}
			
			//Get Rejection remark
			//[START] Added By Sanket Girme [04-Aug-2015]
			
			sql="";
			rejectionRemark="";
			sql="SELECT REMARKS FROM SPL_SALES_POR_HDR WHERE TRAN_ID =?";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs=pstmt.executeQuery();			
			if (rs.next()){
				rejectionRemark = rs.getString(1);				
			}
			System.out.println("Rejection Remark == >"+rejectionRemark);
			rejectionRemark= rejectionRemark== null ? "" :rejectionRemark;
			if(rejectionRemark.equalsIgnoreCase("") || rejectionRemark==null || rejectionRemark.length()<=0)
			{
				//Rejection Remark Blank
				errString = itmDBAccess.getErrorString("", "VTREJNOTAB", "", "", conn);
				return errString;
			}
		    //[END] By Sanket Girme [04-Aug-2015]
			
			sql="";
			loginCode=genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode");
			
			sql="update spl_sales_por_hdr set wf_status = ?, emp_code__aprv = ? where tran_id = ? ";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, "R");
			pstmt.setString(2, loginCode);//E10808    loginCode 
			pstmt.setString(3, tranId);	
			updCnt=pstmt.executeUpdate();			
			if (updCnt > 0)
			{
				conn.commit();
				errString = itmDBAccess.getErrorString("", "VTREJSUCF", "", "", conn);
				System.out.println("--------Column wf_status of spl_sales_por_hdr table updated successfully------- ");
				return errString;
			}			
		}
		catch(SQLException se)
		{
			System.out.println("SQLException : class ProofOfDeliveryReject : ");			
			se.printStackTrace();
			try
			{
			  conn.rollback();
			}
			catch(Exception e){
				System.out.println("Exception : Occure during rollback........");
				e.printStackTrace();
				}
			throw new ITMException(se); //Added By Mukesh Chauhan on 05/08/19
		}
		catch(Exception e)
		{
			System.out.println("Exception : class ProofOfDeliveryReject : ");
			e.printStackTrace();
			try
			{
				conn.rollback();
			}
			catch(Exception se){
				System.out.println("Exception : Occure during rollback........");
				se.printStackTrace();
				}
		}
		
		finally
		{
		   try{
			    if (pstmt != null)
			    {
			    	pstmt.close();
					pstmt=null;
			    }
				if (rs !=null)
				{
					rs.close();
					rs=null;
				}
				if(conn!=null)
				{		
					conn.close();
					conn = null;
				}
				
		    }catch(Exception e)
			{
				System.out.println("inside Finally Exception ProofOfDeliveryReject class:reject() ");
				e.printStackTrace();
			}

		}
		return errorString;
	}
}
