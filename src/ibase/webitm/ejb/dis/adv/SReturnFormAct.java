
/*develop by ritesh on 27/06/13
 : Purpose - provision to Close/Cancel Sales Return Form.
 */

package ibase.webitm.ejb.dis.adv;
import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.ejb.Stateless;

@Stateless
public class SReturnFormAct extends ActionHandlerEJB implements SReturnFormActLocal,SReturnFormActRemote 
{
	static
	{
		System.out.println("-- SalesReturnFormClose called -- ");
	}
	public String actionHandler(String tranId, String xtraParams, String forcedFlag) throws RemoteException, ITMException
	{
		String returnString=null;

		System.out.println(".......tranId......."+tranId);
		System.out.println(".......xtraParams..."+xtraParams);
		System.out.println(".......forcedFlag..."+forcedFlag);
		if(tranId!=null && tranId.trim().length()>0)
		{
			returnString = closeCanSreturnForm(tranId,xtraParams,forcedFlag);
		}
		return returnString;
	}	
	public String closeCanSreturnForm(String tranId,String xtraParams,String forcedFlag) throws RemoteException,ITMException
	{
		System.out.println("confirm called........");
		String sql = "";
		String errString = "" ;
		String status = "";
		int noOfRec = 0,noOfCofRec = 0;
		int rowUpdate  =0;
		Connection conn = null;
		ConnDriver connDriver = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		System.out.println("tran id = "+tranId);
		try
		{
			itmDBAccessEJB = new ITMDBAccessEJB();
			connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver = null;
			conn.setAutoCommit(false);
			
			sql  = "select status from sreturn_form where tran_id = ?";
			pstmt  = conn.prepareStatement(sql);
			pstmt.setString(1,tranId);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				status = rs.getString(1);
			}
			if("C".equalsIgnoreCase(status.trim()))
			{
				System.out.println("--transaction already closed--");
				errString = itmDBAccessEJB.getErrorString("","VTTRNALCLS","","",conn);
			}
			else if("X".equalsIgnoreCase(status.trim()))
			{
				System.out.println("--transaction already cancelled--");
				errString = itmDBAccessEJB.getErrorString("","VTTRNALCNL","","",conn);
			}
			else
			{
				sql  = "select count(*) from sreturn where form_no = ?";
				pstmt  = conn.prepareStatement(sql);
				pstmt.setString(1,tranId);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					noOfRec = rs.getInt(1);
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
				if(noOfRec > 0)
				{
					sql  = "select count(1) from sreturn where form_no = ? and confirmed='Y'";
					pstmt  = conn.prepareStatement(sql);
					pstmt.setString(1,tranId);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						noOfCofRec =rs.getInt(1);
						
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;
					if(noOfRec == noOfCofRec)
					{
						sql =  "update sreturn_form set status = 'C' where tran_id = ?";
						pstmt  = conn.prepareStatement(sql);
						pstmt.setString(1,tranId);
						rowUpdate=  pstmt.executeUpdate();
						pstmt.close();pstmt = null;
						errString = itmDBAccessEJB.getErrorString("","VTTRNCLS","","",conn);
					}else
					{
						System.out.println("The Selected transaction can not close ");
						errString = itmDBAccessEJB.getErrorString("","VTTRNNTCL","","",conn);
					}
					
					
				}else
				{
					sql =  "update sreturn_form set status = 'X' where tran_id = ?";
					pstmt  = conn.prepareStatement(sql);
					pstmt.setString(1,tranId);
					rowUpdate=  pstmt.executeUpdate();
					pstmt.close();pstmt = null;
					errString = itmDBAccessEJB.getErrorString("","VTTRNCNCL","","",conn);
				}
			}
			
		} 
		catch( Exception e)
		{			
				try 
				{
					conn.rollback(); 
					System.out.println("Exception.. "+e.getMessage());
					e.printStackTrace();	
					errString=e.getMessage();
					throw new ITMException(e);

				} 
				catch (SQLException ex) 
				{
					ex.printStackTrace();
					errString=ex.getMessage();
					throw new ITMException(ex);
				}
			
		}
		finally
		{		
				try
				{
					if(rowUpdate > 0)
					{
						conn.commit();
						System.out.println("--transaction commited--"+rowUpdate);
							
					}else
					{
						conn.rollback();
						System.out.println("--transaction rollback--");
					}
					
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
					if(conn != null)
					{
						conn.close();
						conn = null;
					}
				}
				catch(Exception e)
				{
					System.out.println("Exception : "+e);
					e.printStackTrace();
					throw new ITMException(e);
				}
			}
		return errString;
	} //end of  method
}
