
/********************************************************
	Title : Paking List
	Date  : 27/03/2012
	Developer: Navanath Nawale

 ********************************************************/
package ibase.webitm.ejb.dis;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.*;
import java.sql.*;
import ibase.webitm.utility.ITMException;
import javax.ejb.Stateless;
/*import org.jboss.xb.binding.sunday.unmarshalling.impl.runtime.RtAttributeHandler;
import javax.xml.rpc.ParameterMode;
 */
@Stateless
public class InvPackPrc extends ActionHandlerEJB implements InvPackPrcLocal,InvPackPrcRemote
{
	E12GenericUtility genericUtility = new E12GenericUtility();
	public String actionHandler(String tranId, String xtraParams, String forcedFlag) throws RemoteException, ITMException
	{
		String returnString=null;

		System.out.println("tranId----"+tranId);
		System.out.println("forcedFlag----"+forcedFlag);
		if(tranId!=null && tranId.trim().length() > 0)
		{
			returnString=StockUpdate(tranId,xtraParams,forcedFlag);
		}
		return returnString;
	}

	public String StockUpdate(String tranId, String xtraParams, String forcedFlag) throws ITMException 
	{
		System.out.println("In method AppScheme"    +xtraParams);
		String sql = "";
		String sql1 = "";
		String tranId1 = "";
		String siteCode = "";
		String itemCode = "";
		String lotNo = "";
		String lotSl = "";
		String locCode = "";
		String confirm = "";
		Connection conn = null;
		PreparedStatement pstmt1 = null;
		PreparedStatement pstmt = null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		String errString = null;
		ResultSet rs1 = null;
		int cnt=0;
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
			sql = "select confirmed from inv_pack where tran_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,tranId);
			rs1 = pstmt.executeQuery();
			while(rs1.next())
			{
				confirm = rs1.getString(1);
			}
			pstmt.close();
			pstmt = null;
			rs1.close();
			rs1 = null;
			if("Y".equalsIgnoreCase(confirm))
			{
				sql = "SELECT A.TRAN_ID,SITE_CODE,ITEM_CODE,LOT_NO,LOT_SL,LOC_CODE FROM INV_PACK A,INV_PACK_RCP B"+
						" WHERE A.TRAN_ID=B.TRAN_ID AND A.TRAN_ID= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,tranId);
				rs1 = pstmt.executeQuery();
				while(rs1.next())
				{
					tranId1 = rs1.getString(1);
					siteCode = rs1.getString(2);
					itemCode = rs1.getString(3);
					lotNo = rs1.getString(4);
					lotSl = rs1.getString(5);
					locCode = rs1.getString(6);
					sql1 = "update stock set pack_ref = null where item_code = ? and site_code = ? and loc_code =? and lot_no = ? and lot_sl = ? ";
					pstmt1 = conn.prepareStatement(sql1);
					pstmt1.setString(1,itemCode);
					pstmt1.setString(2,siteCode);
					pstmt1.setString(3,locCode);
					pstmt1.setString(4,lotNo);
					pstmt1.setString(5,lotSl);
					pstmt1.executeUpdate();
					errString = itmDBAccessEJB.getErrorString("","PACKUPD","","",conn);
					System.out.println("Tran_Id  is :" + tranId1);
					System.out.println("SiteCode  is :" + siteCode);
					System.out.println("ItemCode   is :" + itemCode);
					System.out.println("LotNo  is :" + lotNo);
					pstmt1.close();
					pstmt1 = null;
				}
				pstmt.close();
				pstmt = null;
				rs1.close();
				rs1 = null;
				System.out.println("Tran_id"+tranId);
				sql1 = "update inv_pack set status = ? where tran_id = ?";
				pstmt1 = conn.prepareStatement(sql1);
				pstmt1.setString(1,"X");
				pstmt1.setString(2,tranId);
				cnt=pstmt1.executeUpdate();
				System.out.println("@@@@@@@@@ Update Invpackprc:[" + cnt + "]");
				pstmt1.close();
				pstmt1 = null;
				
			}
			else
			{
				errString = itmDBAccessEJB.getErrorString("","PACKNOT","","",conn);
			}

		}
		catch( Exception e)
		{
			if(conn!=null)
			{
				try {
					conn.rollback();
				} catch (SQLException ex) {
					Logger.getLogger(InvPackPrc.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
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
					Logger.getLogger(InvPackPrc.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
		return errString;

	}

}
