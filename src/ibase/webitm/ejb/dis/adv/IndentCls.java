/* 
		Developed by : Niraja
		Company : Base Information Management Pvt. Ltd
		Version : 1.0
		Date : 25/10/2005
*/
package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;

import javax.ejb.*;

import ibase.webitm.ejb.*;
import ibase.system.config.*;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class IndentCls extends ActionHandlerEJB implements IndentClsLocal, IndentClsRemote
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
	public String actionHandler(String tranID, String xtraParams, String forcedFlag) throws RemoteException,ITMException
	{
		String  retString = null;
		try
		{
			retString = actionClose(tranID,xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception :Indent :actionHandler:" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning from Indent actionHandler"+retString);
		return retString;
	}
	private String actionClose(String indNo,  String xtraParams)throws ITMException
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
		String statusDet = "";
		String userId = "";
		String empCode = "";
		String enqNo = "";
		boolean updFlag = true;
		boolean enqDetFlag = true;
		ArrayList indNoDet = new ArrayList();
		ArrayList enqDetStatus = new ArrayList();
		int rows1=0;
		int rows2=0;
		int rows3=0;
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();
		try
		{	
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			stmt = conn.createStatement();
			sql= "SELECT STATUS FROM INDENT WHERE IND_NO = '"+indNo+"'";
			System.out.println("Indent:actionClose:Status:sql:"+sql);
			rs = stmt.executeQuery(sql);
			if(rs.next())
			{
				status=rs.getString("STATUS");
			}
			if(status.equalsIgnoreCase("L")|| status.equalsIgnoreCase("C"))
			{
				errCode ="VTINDALAP";
			}
			else
			{
				userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"user_id");
				System.out.println("userId:"+userId);
				sql="SELECT EMP_CODE FROM USERS WHERE CODE ='"+userId+"'";
				System.out.println("\n Indent:actionClose:USERS:sql:"+sql);
				rs = stmt.executeQuery(sql);
				if(rs.next())
				{
					empCode =rs.getString("EMP_CODE");
				}
				System.out.println("empCode:"+empCode);
				rs.close();
				updSql ="UPDATE INDENT SET STATUS = 'C',STATUS_DATE = ?,CHG_USER = ?,CHG_DATE = ? "+
						" WHERE IND_NO = '"+indNo+"'";
				System.out.println("\n Indent:actionClose:INDENT:updSql:"+updSql);
				pstmt = conn.prepareStatement(updSql);
				pstmt.setDate(1,new java.sql.Date(System.currentTimeMillis()));
				pstmt.setString(2,empCode);
				pstmt.setDate(3,new java.sql.Date(System.currentTimeMillis()));
				rows1 = pstmt.executeUpdate();
				System.out.println("Rows1 Updated :INDENT:"+rows1);
				if(rows1 > 0)
				{
					conn.commit();
					errCode = "VTSUCC";
					System.out.println("\n <==== INDENT Updated Successfully ====>");

					// Updation Of ENQ_DET
					sql ="SELECT ENQ_DET.ENQ_NO FROM ENQ_DET WHERE ENQ_DET.IND_NO ='"+indNo+"'";
					System.out.println("Indent:actionClose:Status:ENQ_DET:sql:"+sql);
					rs = stmt.executeQuery(sql);
					if(rs.next())
					{
						enqNo =rs.getString("ENQ_NO");
					}
					System.out.println("enqNo :"+enqNo);
					rs.close();
					sql="SELECT ENQ_DET.IND_NO FROM ENQ_DET WHERE ENQ_DET.ENQ_NO ='"+enqNo+"'";
					System.out.println("Indent:actionClose:Status:ENQ_DET:sql:"+sql);
					rs = stmt.executeQuery(sql);
					while(rs.next())
					{
						indNoDet.add(rs.getString("IND_NO"));
					}
					System.out.println("indNoDet:"+indNoDet);
					for(int cnt = 0;cnt < indNoDet.size();cnt++)
					{
						sql="SELECT STATUS FROM INDENT WHERE IND_NO ='"+indNoDet.get(cnt).toString()+"'";
						System.out.println("Indent:actionClose:Status:INDENT:sql:"+sql);
						rs = stmt.executeQuery(sql);
						if(rs.next())
						{
							statusDet = rs.getString("STATUS");
						}
						if(statusDet.equalsIgnoreCase("C"))
						{
							updSql = "UPDATE ENQ_DET SET STATUS = 'C'  "+
							      " WHERE ENQ_DET.ENQ_NO ='"+enqNo+"'" +
								  " AND ENQ_DET.IND_NO ='"+indNoDet.get(cnt).toString()+"'" ;
							System.out.println("Indent:actionClose:Status:ENQ_DET:updSql:"+updSql);
							rows2 = stmt.executeUpdate(updSql) ;
							System.out.println("Rows2 Updated :ENQ_DET:"+rows2);
						}
						if(rows2 > 0)
						{
							conn.commit();
							System.out.println("\n <==== ENQ_DET Updated Successfully ====>");
						}
						else
						{
							enqDetFlag= false;
							break;
						}
					}
					// Updation Of ENQ_HDR
					if(enqDetFlag == true)
					{
						sql="SELECT ENQ_DET.STATUS FROM ENQ_DET WHERE ENQ_DET.ENQ_NO ='"+enqNo+"'";
						System.out.println("Indent:actionClose:Status:ENQ_DET:sql:"+sql);
						rs = stmt.executeQuery(sql);
						while(rs.next())
						{
							enqDetStatus.add(rs.getString("STATUS"));
						}
						System.out.println("enqDetStatus:"+enqDetStatus+":");
						for(int rnt = 0;rnt< enqDetStatus.size();rnt++)
						{
							String tempEnqDetStatus = enqDetStatus.get(rnt).toString();
							if(!tempEnqDetStatus.equalsIgnoreCase("C"))
							{
								updFlag = false;
							}
						}
						if(updFlag == true)
						{
							updSql ="UPDATE ENQ_HDR SET STATUS ='C' WHERE ENQ_HDR.ENQ_NO = '"+enqNo+"'";
							System.out.println("Indent:actionClose:Status:ENQ_DET:updSql:"+updSql);
							rows3 = stmt.executeUpdate(updSql) ;
							System.out.println("Rows3 Updated :ENQ_HDR:"+rows3);
						}
						if(rows3 > 0)
						{
							conn.commit();
							System.out.println("\n <==== ENQ_HDR Updated Successfully ====>");
						}
					}//end enqDetFlag
				}//end rows1				
			}//end else
			if (errCode != null  && errCode.trim().length() > 0)
			{
				System.out.println("Indent:errCode:"+errCode);
				errString = itmDBAccess.getErrorString("",errCode,"","",conn);
				System.out.println("errString:"+errString+":");
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :Indent :" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("Connection Closed......");
				conn.close();
			}catch(Exception e){}
		}
		return errString;
	}
}

