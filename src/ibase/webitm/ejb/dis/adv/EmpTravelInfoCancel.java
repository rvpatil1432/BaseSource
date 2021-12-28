/**
 DEVELOPED BY CHANDRASHEKAR ON 14/05/14 
 PURPOSE: W14BSUN003 (StarClub Employee details.)
 */
package ibase.webitm.ejb.dis.adv;

import ibase.system.config.ConnDriver;
import ibase.utility.EMail;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;
import org.w3c.dom.*;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.ejb.Stateless;

@Stateless 
public class EmpTravelInfoCancel extends ActionHandlerEJB implements EmpTravelInfoCancelRemote, EmpTravelInfoCancelLocal
{
	public String confirm(String tranID,String xtraParams, String forcedFlag) throws RemoteException,ITMException
	{

		String retString = "";
		Connection conn = null;
		boolean isConn= false;
		try
		{
			retString = this.confirm(tranID, xtraParams, forcedFlag, conn, isConn);
			System.out.println("retString:::::"+retString);
		}
		catch(Exception e)
		{
			System.out.println("Exception in [EmpTravelInfoCancel] confirm " + e.getMessage());
			throw new ITMException(e);
		}
		return retString;
	}
	public String confirm( String tranId, String xtraParams, String forcedFlag, Connection conn, boolean connStatus ) throws RemoteException,ITMException
	{
		String retString = "";
		String sql = "",sql1="";
		String userType="",formatCode="";
		String travelCode="",division="",travelCodeEmail="",nsmCodeEmail="";
		int upd=0;
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		ibase.utility.E12GenericUtility genericUtility= null;
		ITMDBAccessEJB itmDBAccess = null;
		StringBuffer commInfo = new StringBuffer();
		java.util.Date  tranDate= null;
		String objName="",winName="",refSer="",tranIdCol="",eventCode="",empCode="",empFirstName="",empLastName="",empMiddleName="",formatType="",nsmCode="";
		try
		{
			genericUtility = new  ibase.utility.E12GenericUtility();
			itmDBAccess = new ITMDBAccessEJB();
			ConnDriver connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver = null;
			conn.setAutoCommit(false);
			connStatus = true;	
			
			sql1 = " update emp_travel_info set status = 'X'  where tran_id  =  ? ";
			pstmt = conn.prepareStatement(sql1);
			pstmt.setString(1, tranId);
			upd =  pstmt.executeUpdate();
			pstmt.close();pstmt = null;
			System.out.println(" status updated:: "+upd);
			if(upd > 0)
			{				
				objName = "emp_travel_info";
				winName = "w_emp_travel_info";
				eventCode="act_cancel";
				sql="select comm_format from system_events where obj_name = '"+objName+"' "
						+   " and event_code = '"+eventCode+"' ";
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						formatCode = rs.getString("comm_format");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				sql="select ref_ser, tran_id_col from transetup where tran_window = '"+winName+"' ";
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						refSer = rs.getString("ref_ser");
						tranIdCol = rs.getString("tran_id_col");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if(refSer == null ) refSer ="   ";
					if(tranIdCol == null ) tranIdCol ="   ";
				sql = " select emp_code,emp_fname,emp_lname,emp_mname,tran_date,item_ser from emp_travel_info where tran_id = ? ";
					pstmt = conn.prepareStatement(sql);			
					pstmt.setString(1, tranId);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						empCode = rs.getString("emp_code");
						empFirstName = rs.getString("emp_fname");
						empLastName = rs.getString("emp_lname");
						empMiddleName = rs.getString("emp_mname");	
						tranDate = rs.getTimestamp("tran_date");
						division = rs.getString("item_ser");
						System.out.println("division["+division+"]");
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;
				sql = " select travel_code,nsm_code from nsm_email where division = ? ";
					pstmt = conn.prepareStatement(sql);			
					pstmt.setString(1, division);
					rs = pstmt.executeQuery();
					if(rs.next())
					{					
						travelCode = rs.getString("travel_code");
						nsmCode = rs.getString("nsm_code");
						
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;
				if(travelCode != null && travelCode.trim().length() > 0)
				{	
					sql = " select email_id_off from employee where emp_code=? ";
					pstmt = conn.prepareStatement(sql);			
					pstmt.setString(1, travelCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{					
						travelCodeEmail = rs.getString("email_id_off");												
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;	
					System.out.println("travelCodeEmail["+travelCodeEmail+"]");
					
				}
				if(nsmCode != null && nsmCode.trim().length() > 0)
				{
					sql = " select email_id_off from employee where emp_code=? ";
					pstmt = conn.prepareStatement(sql);			
					pstmt.setString(1, nsmCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{					
						nsmCodeEmail = rs.getString("email_id_off");												
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;	
					System.out.println("nsmCodeEmail["+nsmCodeEmail+"]");
				}
				commInfo.append("<ROOT>");
				commInfo.append("<MAIL><EMAIL_TYPE>page</EMAIL_TYPE><ENTITY_CODE>BASE</ENTITY_CODE>");
				commInfo.append("<ENTITY_TYPE>"+userType+"</ENTITY_TYPE>");
				commInfo.append("<TO_ADD></TO_ADD>");
				//commInfo.append("<cc_to>"+travelCodeEmail+","+nsmCodeEmail+"</cc_to>");
				commInfo.append("<BCC_ADD></BCC_ADD>");
				commInfo.append("<FORMAT_CODE>"+formatCode+"</FORMAT_CODE>");							
				commInfo.append("<ATTACHMENT><BODY></BODY><LOCATION></LOCATION></ATTACHMENT>");
				commInfo.append("</MAIL>");
				commInfo.append("<XML_DATA><ROOT><Detail1><TRAN_ID>"+tranId+"</TRAN_ID><TRAN_DATE>"+tranDate+"</TRAN_DATE><emp_code>"+empCode+"</emp_code>");
				commInfo.append("<cc_to>"+travelCodeEmail+","+nsmCodeEmail+"</cc_to>");
				commInfo.append("<EMP_NAME>"+empFirstName+empLastName+"</EMP_NAME></Detail1></ROOT></XML_DATA>");
				commInfo.append("</ROOT>");	
				EMail email = new EMail();
				email.sendMail(commInfo.toString(), "ITM"); 
				
				retString = "submited";
				System.out.println("  information submited::");
			}			
		}
		catch( Exception e )
		{
			try
			{
				conn.rollback();
				retString = e.getMessage();
				e.printStackTrace();
			}
			catch (Exception e1)
			{
				System.out.println("Exception : "+e);e.printStackTrace();
			}
			throw new ITMException(e);
		}
		finally
		{	
			if(retString != null && retString.indexOf("submited") > -1)
			{
				try {
					conn.commit();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				retString = itmDBAccess.getErrorString("","VTCANCEL5","","",conn);
			}			
			else
			{
				retString = itmDBAccess.getErrorString("","VTERRCAN","","",conn);
			}
			try{

				if(rs != null)
				{
					rs.close();rs = null;
				}
				if(pstmt != null)
				{
					pstmt.close();pstmt = null;
				}
				if(conn != null)
				{
					conn.close();
					conn = null;	
				}
			}
			catch(Exception e)
			{System.out.println("Exception : "+e);e.printStackTrace();}

		}		
		return retString ;
	}
	
}
