/* 
	Developed by : Niraja
	Company : Base Information Management Pvt. Ltd
	Version : 1.0
	Date : 27/10/2005
*/
package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import java.sql.*;

import javax.ejb.*;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.system.config.*;

import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class EnquiryCls extends ActionHandlerEJB implements EnquiryClsLocal, EnquiryClsRemote
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

	public String actionHandler(String tranID, String xtraParams, String forcedFlag) throws RemoteException,ITMException
	{
		String  retString = null;
		try
		{
			retString = actionClose(tranID,xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception :EnquiryClose :actionHandler:" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
	    return retString;
	}

	private String actionClose(String enqNo ,String xtraParams)throws ITMException
	{
		Connection conn = null;
		Statement stmt = null;
		PreparedStatement pstmt =null;
		ResultSet rs = null;
		String sql = "";
		String updSql = "";
		String errCode="";
		String errString="";
		String status = "";
		int rows=0;
		int rowsHdr=0;
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		ConnDriver connDriver = new ConnDriver();
		try
		{	
		   	//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			stmt = conn.createStatement();
			sql = "SELECT STATUS FROM ENQ_HDR WHERE ENQ_NO ='"+enqNo+"'";
			System.out.println("EnquiryClose:actionClose:sql:"+sql);
			rs = stmt.executeQuery(sql);
			if(rs.next())
			{
				status = rs.getString("STATUS");
			}
			System.out.println("enqNo:"+enqNo+":status:"+status+":");
			if(status != null && status.trim().length() > 0 )
			{
				if(status.equalsIgnoreCase("C") || status.equalsIgnoreCase("R"))
				{
					errCode ="VTCLOSE1";
				}
				else 
				{
					updSql="UPDATE ENQ_DET SET STATUS = 'C',STATUS_DATE = ?"+
							" WHERE ENQ_NO = ?";
					System.out.println("EnquiryClose:ENQ_DET:updSql:"+updSql+":");
					pstmt = conn.prepareStatement(updSql);
					pstmt.setDate(1,new java.sql.Date(System.currentTimeMillis()));
					pstmt.setString(2,enqNo);
					rows = pstmt.executeUpdate();
					System.out.println("*EnquiryClose:rows:"+rows);
					if(rows > 0)
					{
						updSql = "UPDATE ENQ_HDR SET STATUS = 'C',STATUS_DATE = ? "+
		   						 "  WHERE ENQ_NO =?";
						System.out.println("EnquiryClose:ENQ_HDR:updSql:"+updSql+":");
						pstmt = conn.prepareStatement(updSql);
						pstmt.setDate(1,new java.sql.Date(System.currentTimeMillis()));
						pstmt.setString(2,enqNo);
						rowsHdr = pstmt.executeUpdate();
					}
					else
					{
					   errCode="VTRETDET";
					}
				}
			} //end if(status null)
			System.out.println("*EnquiryClose:rowsHdr:"+rowsHdr);
			if(rowsHdr > 0)
			{
				conn.commit();
				errCode = "VTCLOSED";
				System.out.println("\n <==== Table Updated Successfully ====>");
			}
			if (errCode != null  && errCode.trim().length() > 0)
			{
				System.out.println("EnquiryClose:errCode:"+errCode);
				errString = itmDBAccess.getErrorString("",errCode,"","",conn);
				System.out.println("errString:"+errString+":");
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :EnquiryClose :" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("Closing Connection.....");
				conn.close();
			}catch(Exception e){}
		}
		return errString;
	}
 }