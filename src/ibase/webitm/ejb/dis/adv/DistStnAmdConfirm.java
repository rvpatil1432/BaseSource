package ibase.webitm.ejb.dis.adv;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import javax.ejb.Stateless;

@Stateless
public class DistStnAmdConfirm extends ActionHandlerEJB implements DistStnAmdConfirmRemote,
DistStnAmdConfirmLocal {

	E12GenericUtility genericUtility = new E12GenericUtility();
	String errorString = null;
	public String actionHandler() throws RemoteException,ITMException
	{
		System.out.println("actionHandler() Method Called....");
		return "";
	}
	
	public String confirm(String tranID, String xtraParams, String forcedFlag) throws RemoteException,ITMException
		{
		String  retString = null;
		System.out.println("Xtra Params : " + xtraParams);
		System.out.println("tranID>>>>"+tranID);
		try
		{
			retString = actionconfirm(tranID, xtraParams,forcedFlag);
		}
	   	catch(Exception e)
		{
			System.out.println("Exception :actionHandler(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning from  actionHandler"+retString);
	    return (retString);
	}
	public String actionconfirm(String tranID, String xtraParams, String forcedFlag) throws RemoteException, ITMException {
		Connection conn = null;
		PreparedStatement pstmt= null,pstmt1=null,pstmt2=null,pstmt3=null;
		
		ResultSet rs = null;
		String sql = "";
		
		ConnDriver connDriver = null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		String errString = null;
		String confirmed = "";
		int cnt = 0;
		String refId="",lrNo="",tranDate="",transMode="",shipId="",frtType="",reperNo="",remarks="",gpNo="",siteCode="",lorryNo="";
		double frtAmt=0.0,noArt=0.0,grossWt=0.0;
		Timestamp gpDate = null,lrDate = null;
		String userId ="",loginEmpCode="",tranId="";
		String returnString = null;
		int count=0;
		userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
		loginEmpCode = GenericUtility.getInstance().getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
		
		try {
			System.out.println("helloconfirm*****ProjAmdConf2******************");
			itmDBAccessEJB = new ITMDBAccessEJB();
			connDriver = new ConnDriver();
			conn = getConnection();
			conn.setAutoCommit(false);
			
			sql = "SELECT count(1) from stn_amd "
					  +" where tran_id = ? and confirmed='Y' ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranID);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				cnt = rs.getInt( 1 );
				System.out.println("count>>>>>>>>>>"+cnt);
			}
			
			if(cnt == 0)
			{
				sql = " Update stn_amd set confirmed = 'Y', conf_date = ? where tran_id = ? " ;
					System.out.println("sql....."+sql);
					pstmt3 = conn.prepareStatement(sql);
					pstmt3.setTimestamp(1,getCurrdateAppFormat() );
					pstmt3.setString(2,tranID);
					pstmt3.executeUpdate();
					pstmt3.close(); 
					pstmt3=null;
					System.out.println("confirm updated -------->>>>>>>>>updCount :"+cnt);
				
				sql = "select ref_id, lr_no, lr_date,tran_code, trans_mode, lorry_no, shipment_id, frt_type, frt_amt, "
					+ "no_art, remarks, gross_weight, rd_permit_no, gp_no, gp_date, site_code from stn_amd "
					+ "where tran_id= ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranID);
				rs = pstmt.executeQuery();

				if (rs.next()) {
					refId = rs.getString("ref_id");
					lrNo = rs.getString("lr_no");
					lrDate = rs.getTimestamp("lr_date");
					tranDate = rs.getString("tran_code");
					transMode = rs.getString("trans_mode");
					lorryNo = rs.getString("lorry_no");
					shipId = rs.getString("shipment_id");
					frtType = rs.getString("frt_type");
					frtAmt = rs.getDouble("frt_amt");
					noArt = rs.getDouble("no_art");
					remarks = rs.getString("remarks");
					grossWt = rs.getDouble("gross_weight");
					reperNo = rs.getString("rd_permit_no");
					gpNo = rs.getString("gp_no");						
					gpDate = rs.getTimestamp("gp_date");
					siteCode = rs.getString("site_code");
					
					
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;
				  
				  sql ="update distord_iss set lr_no = ?,lr_date = ?, tran_code = ?, trans_mode = ?, lorry_no = ?, "
				  +"shipment_id = ?, frt_type = ?, frt_amt = ?, no_art = ?, remarks = ?, gross_weight = ?, "
				  + "rd_permit_no = ?, gp_no = ?, gp_date = ? where tran_id = ? "; 
				  pstmt1 = conn.prepareStatement(sql); 
				  pstmt1.setString(1, lrNo);
				  pstmt1.setTimestamp(2, lrDate);
				  pstmt1.setString(3, tranDate);
				  pstmt1.setString(4, transMode);
				  pstmt1.setString(5, lorryNo);
				  pstmt1.setString(6, shipId);
				  pstmt1.setString(7, frtType);
				  pstmt1.setDouble(8, frtAmt);
				  pstmt1.setDouble(9, noArt);
				  pstmt1.setString(10, remarks);
				  pstmt1.setDouble(11, grossWt);
				  pstmt1.setString(12, reperNo);
				  pstmt1.setString(13, gpNo);
				  pstmt1.setTimestamp(14, gpDate);
				  pstmt1.setString(15, refId);
				  pstmt1.executeUpdate(); 
				  pstmt1.close();
				  pstmt1 = null;
				
				
				  String tranIdiss=""; 
				  sql ="select tran_id from distord_rcp where tran_id__iss = ?"; 
				  pstmt2 =conn.prepareStatement(sql); 
				  pstmt2.setString(1, refId); 
				  rs=pstmt2.executeQuery(); 
				  if(rs.next()) 
				  { 
					  tranIdiss =rs.getString("tran_id");
				      System.out.println("tran id>>>>>>>>>>"+tranIdiss);
				  
				  } 
				  pstmt2.close(); 
				  pstmt2 = null; 
				  rs.close(); 
				  rs = null;
				  
				  
				  sql ="update distord_rcp set lr_no = ?, lr_date = ?, tran_code = ?, trans_mode = ?, lorry_no = ?, frt_type = ?, "
				  +"frt_amt = ?, no_art = ?, remarks = ?, gross_weight = ?, gp_no = ?, gp_date = ? where tran_id = ?";
				  pstmt2 = conn.prepareStatement(sql); 
				  pstmt2.setString(1, lrNo);
				  pstmt2.setTimestamp(2, lrDate);
				  pstmt2.setString(3,tranDate); 
				  pstmt2.setString(4, transMode);
				  pstmt2.setString(5,lorryNo); 
				  pstmt2.setString(6, frtType); 
				  pstmt2.setDouble(7,frtAmt); 
				  pstmt2.setDouble(8, noArt); 
				  pstmt2.setString(9, remarks); 
				  pstmt2.setDouble(10,grossWt); 
				  pstmt2.setString(11, gpNo); 
				  pstmt2.setTimestamp(12, gpDate);
				  pstmt2.setString(13,tranIdiss); 
				  pstmt2.executeUpdate(); 
				  pstmt2.close(); 
				  pstmt2 =null;
				  
				  returnString = itmDBAccessEJB.getErrorString("","CONFSUCC",userId,"",conn);
				  return returnString;
			}
			else
			{
				returnString = itmDBAccessEJB.getErrorString("","VTMCONF1",userId,"",conn);
			}
			if ( rs != null )
			{
				rs.close();
				rs = null;
			}
		} catch (Exception e) {
			try {
				conn.rollback();
			} catch (Exception t) {
			}
			e.printStackTrace();
			errString = itmDBAccessEJB.getErrorString("", "VTDESNCONF", "", "", conn);
			throw new ITMException(e);
		} finally {
			try
			{
				conn.commit();
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if(pstmt1 != null)
				{
					pstmt1.close();
					pstmt1 = null;
				}
				if(pstmt2 != null)
				{
					pstmt2.close();
					pstmt2 = null;
				}
				if(pstmt3 != null)
				{
					pstmt3.close();
					pstmt3 = null;
				}
				if(conn != null)
				{
					conn.close();
					conn = null;
				}
			}
			catch(Exception e)
			{
				System.out.println("Exception DiscountConfirmEJB....... :\n"+e.getMessage());
			}
		}
		return errString;
	}
	private Timestamp getCurrdateAppFormat()
    {
		Timestamp timestamp = null;		
        try
        {
            java.util.Date date = null;
            timestamp = new Timestamp(System.currentTimeMillis());
            
			SimpleDateFormat simpledateformat = new SimpleDateFormat(genericUtility.getDBDateFormat());
            date = simpledateformat.parse(timestamp.toString());
            timestamp = Timestamp.valueOf(simpledateformat.format(date).toString() + " 00:00:00.0");
		}
        catch(Exception exception)
        {
            System.out.println("Exception in  getCurrdateAppFormat " + exception.getMessage());
        }
        return timestamp;
    }
	
}