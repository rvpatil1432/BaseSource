/********************************************************
	Title 	 : DistRcpExShClose
	Date  	 : 13/MAY/15
	Developer: Pankaj R.
 ********************************************************/

package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import java.sql.*;

import ibase.utility.E12GenericUtility;
import ibase.utility.EMail;
import ibase.webitm.ejb.*;
import ibase.system.config.*;

import javax.ejb.Stateless;
import ibase.webitm.ejb.ITMDBAccessEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

@Stateless
public class DistRcpExShClose extends ActionHandlerEJB implements DistRcpExShCloseLocal, DistRcpExShCloseRemote // SessionBean
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	public String close(String tranId, String xtraParams, String forcedFlag)throws RemoteException, ITMException 
	{
		System.out.println("Inside Close Method");
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "", sql1 = "";
		ConnDriver connDriver = null;
		String loginEmpCode = "";
		String status = "",confirmed="", empCode = "",siteCode="";
		int updateCount=0;
		double shortageAmtHdr=0.0,qtyRcp=0.0,qtyActual=0.0;
		ITMDBAccessEJB itmDBAccess = null;
		ibase.utility.E12GenericUtility genericUtility = null;
		genericUtility = new ibase.utility.E12GenericUtility();
		itmDBAccess = new ITMDBAccessEJB();
		// ConnDriver connDriver = new ConnDriver();
		connDriver = null;
		// connStatus = true;
		String objName = "", winName = "", eventCode = "";
		String errString = "";
		ValidatorEJB validatorEJB = null;

		ITMDBAccessEJB itmDBAccessEJB = null;
		try
		{
			itmDBAccessEJB = new ITMDBAccessEJB();
			validatorEJB = new ValidatorEJB();
			connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver = null;
			conn.setAutoCommit(false);
			
			sql = "select status,confirmed,site_code,shortage_amt from distrcp_exsh_hdr where tran_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				confirmed=rs.getString("confirmed");
				status = rs.getString("status");
				siteCode = rs.getString("site_code");
				shortageAmtHdr=rs.getDouble("shortage_amt");
			}
			System.out.println("Confirm>>>>>>>"+confirmed);
			System.out.println("Status@@@@@@@@" + status);
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			if(confirmed!=null && confirmed.equalsIgnoreCase("N"))
			{
				System.out.println("Transaction is not submitted.Please submit it before close");
				errString = itmDBAccessEJB.getErrorString("", "VTNSUBCLO", "","", conn);
				return errString;
			}
			if (status != null && status.equalsIgnoreCase("C"))
			{
				System.out.println("The Selected transaction is already Closed");
				errString = itmDBAccessEJB.getErrorString("", "VTMCLOSE1", "","", conn);
				return errString;
			} 
			else /* (status != null && status.equalsIgnoreCase("O") */
			{
				sql = "select status,qty_rcp,qty_actual from distrcp_exsh_det where tran_id = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				while (rs.next())
				{
					status = rs.getString("status");
					qtyRcp=Double.valueOf(rs.getString("qty_rcp"));
					qtyActual=Double.valueOf(rs.getString("qty_actual"));
					System.out.println("Status : " + status);
					if ("O".equalsIgnoreCase(status) && (qtyRcp > qtyActual) ) //Manoj dtd 21/05/2015
					{
						errString = itmDBAccessEJB.getErrorString("","VTMCLOSE2", "", "", conn);
						return errString;
					}
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;
				sql = "update distrcp_exsh_hdr set status = 'C', status_date = ? where tran_id = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setDate(1, new java.sql.Date(new java.util.Date().getTime()));
				pstmt.setString(2, tranId);
				updateCount = pstmt.executeUpdate();
				System.out.println("no of row update = " + updateCount);
				System.out.println("Status Date"+ new java.sql.Date(new java.util.Date().getTime()));
				System.out.println("tranId@@@@@@@@@" + tranId);
				pstmt.close();
				pstmt = null;
    
				/*if (updateCount > 0)//code comment by sagar on 19/05/15 
				{
					errString = itmDBAccessEJB.getErrorString("", "VTEXCL001","", "", conn);//comment added by sagar on 19/05/15
				}*/
				
				//code added by sagar on 13/05/15, Start..
				if(updateCount > 0) 
				{
					System.out.println(">>In Close If update successfully then send intimation mail with report>>>");
					DistRcpExShConf distRcpConf= new DistRcpExShConf();
					errString=distRcpConf.sendMailReport(tranId, siteCode, shortageAmtHdr, xtraParams, conn);
					//errString= sendMailReport(tranId,siteCode,shortageAmtHdr,xtraParams,conn);
					System.out.println(">>>>In DistRcpExShClose after sendMailReport() errString:"+errString);
				}
				System.out.println(">>>>>>>Check errString:"+errString);
				if((errString != null) &&  errString.indexOf("REPORTSUCC") > -1)
				{
					errString = itmDBAccessEJB.getErrorString("","VTEXCL001","","",conn);
				}
				else
				{
					errString = itmDBAccessEJB.getErrorString("","VTFAILCLOS","","",conn);
				}
				//code added by sagar on 13/05/15, End.
			}
		}
		catch (Exception e) 
		{
			System.out.println("Exception ::" + e.getMessage());
			errString = genericUtility.createErrorString(e);
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally 
		{
			try
			{
				if (errString != null && errString.trim().length() > 0)
				{
					if (errString.indexOf("VTEXCL001") > -1) 
					{
						conn.commit(); 
					} 
					else 
					{
						conn.rollback();
					}
				}
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (pstmt != null) 
				{
					pstmt.close();
					pstmt = null;
				}
				conn.close();
			}
			catch (Exception e)
			{
				System.out.println("Exception : " + e);
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return errString;
	}// end of close method
}// end of class
