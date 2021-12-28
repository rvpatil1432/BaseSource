/********************************************************
	Title : StationAmdConf
	Date  : 03/05/12
	Developer: Kunal Mandhre

 ********************************************************/
package ibase.webitm.ejb.dis;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
//import ibase.webitm.utility.GenericUtility;
import org.w3c.dom.Document;
import ibase.webitm.ejb.*;
import java.sql.*;
import ibase.webitm.utility.ITMException;
import javax.ejb.Stateless;
import ibase.webitm.ejb.sys.CreateRCPXML;
import java.util.Date;
@Stateless
public class StationAmdConf  extends ActionHandlerEJB implements StationAmdConfLocal,StationAmdConfRemote
{
	public String confirm(String tranId, String xtraParams, String forcedFlag) throws RemoteException,ITMException
	{
		//changed by nasruddin 05-10-16
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility = new E12GenericUtility();
		System.out.println("StationAmdConf confirm called..............");
		String lrNo = "";
		String transMode = "";
		String lorryNo = "";
		String frtType = "";
		String refId = "";
		String confirmed = ""; 
		String tranCode = "";
		String shipmentId = "";
		String remarks = "";
		String roadPermitNo = "";
		String gpNo = "";
		String sql = "";
		String sql1 = "";
		String ediOption = "";
		String dataStr = "";
		int status = 0;
		double frtAmt = 0.0;
		double grossWeight = 0.0;
		double noArt = 0.0;
		Date lrDate = null;
		Date gpDate = null; 
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

				sql1 = "select (case when confirmed is null then 'N' else confirmed end)AS confirmed from stn_amd where tran_id = ?";
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
						errString = itmDBAccessEJB.getErrorString("","VTCONF8","","",conn);
					}
					else
					{
						System.out.println("Update");

						sql = "select (case when lr_no is null then ' ' else lr_no end)AS lr_no,lr_date,"
								+"(case when trans_mode is null then ' ' else trans_mode end)AS trans_mode,"
								+"(case when lorry_no is null then ' ' else lorry_no end)as lorry_no,"
								+"(case when frt_type is null then ' 'else frt_type end)as frt_type,"
								+"(case when frt_amt is null then 0 else frt_amt end)as frt_amt,"
								+"(case when tran_code is null then ' ' else tran_code end)as tran_code,"
								+"(case when shipment_id is null then ' ' else shipment_id end)as shipment_id,"
								+"(case when remarks is null then ' ' else remarks end)as remarks,"
								+"(case when rd_permit_no is null then ' ' else rd_permit_no end)as rd_permit_no,"
								+"(case when gp_no is null then ' ' else gp_no end)as gp_no,"
								+"(case when gross_weight is null then 0 else gross_weight end)as gross_weight,"
								+"(case when no_art is null then 0 else no_art end)as no_art,"
								+"ref_id ,gp_date from stn_amd where tran_id = ?";
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
							refId = rs.getString("ref_id");
							tranCode = rs.getString("tran_code");
							shipmentId = rs.getString("shipment_id");
							remarks = rs.getString("remarks");
							roadPermitNo = rs.getString("rd_permit_no");
							gpNo = rs.getString("gp_no");
							grossWeight = rs.getDouble("gross_weight");
							noArt = rs.getDouble("no_art");
							gpDate = rs.getDate("gp_date");

							System.out.println("LrNO="+lrNo);
							System.out.println("LrDate="+lrDate);
							System.out.println("transMode="+transMode);
							System.out.println("lorryNO="+lorryNo);
							System.out.println("Frt Type="+frtType);
							System.out.println("Frt Amt="+frtAmt);
							System.out.println("Ref Id ="+refId);
						}
						pstmt.close();
						pstmt = null;
						rs.close();
						rs = null;

						//
						sql = "update distord_iss  set LR_NO = ? ,LR_DATE = ? ,LORRY_NO= ? ,TRANS_MODE= ?,FRT_TYPE =?,FRT_AMT = ?,TRAN_CODE = ?,"
								+"SHIPMENT_ID = ?, NO_ART = ?,REMARKS = ?,GROSS_WEIGHT = ?,RD_PERMIT_NO = ?,GP_NO = ?,GP_DATE = ?"
								+"  WHERE TRAN_ID = ?";
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
						pstmt.setString(7, tranCode);
						pstmt.setString(8, shipmentId);
						pstmt.setDouble(9, noArt);
						pstmt.setString(10, remarks);
						pstmt.setDouble(11, grossWeight);
						pstmt.setString(12, roadPermitNo);
						pstmt.setString(13, gpNo);
						pstmt.setDate(14,new java.sql.Date(gpDate.getTime()));
						pstmt.setString(15, refId);
						status =  pstmt.executeUpdate();
						System.out.println("nO OF ROWS UPADTE="+status);
						conn.commit();

						pstmt.close();
						pstmt = null;

						if(status != 0)
						{
							sql = "update stn_amd set confirmed = 'Y',conf_date = ? "
								  +" where tran_id   = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setDate(1,new java.sql.Date(new java.util.Date().getTime()) );
							pstmt.setString(2,tranId);
							System.out.println("nO OF transaction conf is ="+pstmt.executeUpdate());
							pstmt.close();
							pstmt = null;
							
							sql = "SELECT EDI_OPTION FROM TRANSETUP WHERE TRAN_WINDOW = 'w_inv_amendment' ";
				            pstmt = conn.prepareStatement(sql);
				            rs = pstmt.executeQuery();
				            if ( rs.next() )
				            {
				                ediOption = rs.getString("EDI_OPTION");
				                if(ediOption == null)
			                	{
			                		ediOption = "";
			                	}
				            }
				            rs.close();
				            rs = null;
				            pstmt.close();
				            pstmt = null;

				            if ( "1".equals(ediOption.trim()) )
				            {
				                CreateRCPXML createRCPXML = new CreateRCPXML("w_stn_amd","tran_id");
				                dataStr = createRCPXML.getTranXML( tranId, conn );
				                System.out.println( "dataStr =[ "+ dataStr + "]" );
				                Document ediDataDom = genericUtility.parseString(dataStr);
				                E12CreateBatchLoadEjb e12CreateBatchLoad = new E12CreateBatchLoadEjb();
				                String retString = e12CreateBatchLoad.createBatchLoad(ediDataDom, "w_stn_amd", "0", xtraParams, conn );
				                createRCPXML = null;
				                e12CreateBatchLoad = null;
				                if( retString != null && "SUCCESS".equals(retString) )
				                {
				                    System.out.println("retString from batchload = 	["+retString+"]");
				                }
				            }
							errString = itmDBAccessEJB.getErrorString("","VTDESPCONF","","",conn);
						}
						else
						{
							errString = itmDBAccessEJB.getErrorString("","VTCONFER","","",conn);
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
					Logger.getLogger(StationAmdConf.class.getName()).log(Level.SEVERE, null, ex);
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
					Logger.getLogger(StationAmdConf.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
		return errString;
	}

}