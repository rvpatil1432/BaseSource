/********************************************************
	Title 	 : 	GRNTransfWizPos[D14HFRA001]
	Date  	 : 	10/11/14
	Developer:  Chandrashekar

********************************************************/
package ibase.webitm.ejb.dis;

import ibase.system.config.*;

import ibase.webitm.ejb.*;
import ibase.webitm.ejb.dis.InvAllocTraceBean;
import ibase.webitm.ejb.dis.StockUpdate;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.utility.TransIDGenerator;
import ibase.utility.CommonConstants;
import ibase.webitm.ejb.dis.adv.StockTransferConf;

import java.io.*;
import java.rmi.RemoteException;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.w3c.dom.*;

import javax.ejb.*;
import javax.naming.InitialContext;
@javax.ejb.Stateless
public class GRNTransfWizPos extends ValidatorEJB implements GRNTransfWizPosLocal, GRNTransfWizPosRemote
{
	public String postSave()throws RemoteException,ITMException
	{
		return "";
	}
	public String postSave(String domString, String tranId,String editFlag, String xtraParams, Connection conn ) throws RemoteException,ITMException
	{
		String forcedFlag = "false" ;
		String errString = "";
		String sql = "";
		String updateSql="";
		String locCode = "",itemCode = "",lotNo="",siteCode="",lotSl="";
		int updCnt=0;
		String lotNoFrom="",lotSlFrom="",locCodeTo="",locCodeFrom="",sql1="";

		boolean toCommit = false;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
        ResultSet rs = null;
        ResultSet rs1 = null;
        StockTransferConf stockTranConf = new StockTransferConf();

		try
		{
			conn.setAutoCommit(false);
			System.out.println("Tran Id [" + tranId + "]");
			System.out.println("Calling Confirm method...............");
			errString = stockTranConf.confirm(tranId, xtraParams, forcedFlag, conn, toCommit);
			System.out.println("transfer done::");
			System.out.println("errString:@@@@:[" + errString + "]");
			//Changed by manoj dtd 01/04/2015 set errString blank if Stock transfer confirmed successfully
			if(errString != null && errString.indexOf("CONFSUCC")>-1)
			{
				errString="";
			}
			if (errString == null || errString.trim().length() == 0)
			{
				sql = "select item_code,lot_no__fr,lot_sl__fr,loc_code__fr,loc_code__to from stock_transfer_det where tran_id = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				while (rs.next())
				{
					itemCode = rs.getString("item_code");
					lotNoFrom = rs.getString("lot_no__fr");
					lotSlFrom = rs.getString("lot_sl__fr");
					locCodeTo = rs.getString("loc_code__to");
					locCodeFrom = rs.getString("loc_code__fr");
					
					sql1 = "select loc_code from qc_order where item_code = ? and lot_no = ?";
					pstmt1 = conn.prepareStatement(sql1);
					pstmt1.setString(1, itemCode);
					pstmt1.setString(2, lotNoFrom);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						locCode = rs1.getString("loc_code");
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					System.out.println("locCode:["+locCode+"]");
					if(locCode != null)
					{
						updateSql = "update qc_order set loc_code = null " + "where item_code = ? and lot_no = ? ";
						pstmt1 = conn.prepareStatement(updateSql);
						pstmt1.setString(1, itemCode);
						pstmt1.setString(2, lotNoFrom);
						updCnt = pstmt1.executeUpdate();
						System.out.println("updCnt::[" + updCnt + "]");
						pstmt1.close();
						pstmt1 = null;
					}
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception "+e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if( rs != null )
				{
					rs.close();
					rs = null;
				}
				if( pstmt != null )
				{
					pstmt.close();
					pstmt = null;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return errString;
	}
}


