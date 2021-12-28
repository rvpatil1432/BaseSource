//
/*******************************************    
Title : StockTransferPos
    Date  : 29/11/11
    Author: Chitranjan Pandey

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
import ibase.utility.E12GenericUtility;
//Changed by Rohan on 06-08-13 for importing stock transfer confirmation
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
public class StockTransferWizPos extends ValidatorEJB implements StockTransferWizPosLocal, StockTransferWizPosRemote
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	public String postSave()throws RemoteException,ITMException
	{
		return "";
	}
	public String postSave(String domString, String tranId,String editFlag, String xtraParams, Connection conn ) throws RemoteException,ITMException
	{
		String forcedFlag = "false" ;
		String errString = "";
		String sql = "";

		int totStockTranDet = 0;

		boolean toCommit = false;
		PreparedStatement pstmt = null;
        ResultSet rs = null;
        StockTransferConf stockTranConf = new StockTransferConf();

		try
		{
			conn.setAutoCommit(false);
			System.out.println( "Total Tran Id ["+tranId+"]");

			sql = "SELECT COUNT(*) AS COUNT FROM STOCK_TRANSFER_DET WHERE TRAN_ID = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString( 1, tranId );
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				totStockTranDet = rs.getInt("COUNT");
			}
			rs.close(); rs = null;
			pstmt.close(); pstmt = null;

			System.out.println("Total Transfer detail-->["+totStockTranDet+"]");
			//checking data available in stock_transfer_det table
			if( totStockTranDet > 0 )
			{
				System.out.println("Calling Confirm method...............");
				errString = stockTranConf.confirm( tranId, xtraParams, forcedFlag, conn, toCommit);
			}
			//Changes by Dadaso pawar on 11/02/15 [Start]
			System.out.println("errString After stockTranConf--------->>["+errString);
			if (errString != null && errString.indexOf("CONFSUCC") > -1)
			{
				errString = "";
			}
			if ( errString != null && errString.length() > 0 )
			{
				throw new Exception(errString);
			}
			//Changes by Dadaso pawar on 11/02/15 [End]
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


