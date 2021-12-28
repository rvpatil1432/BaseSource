package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.util.*;
import java.util.Date;
import java.text.DateFormat;
import java.sql.*;
import javax.ejb.*;
import javax.naming.InitialContext;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.text.SimpleDateFormat;
import ibase.webitm.ejb.*;
import ibase.webitm.utility.*;
import ibase.system.config.*;
import ibase.webitm.utility.ITMException;
//import ibase.webitm.utility.GenericUtility;
//import oracle.jdbc.driver.OraclePreparedStatement;

import javax.ejb.Stateless; // added for ejb3
@Stateless // added for ejb3

public class CustStockPos extends ValidatorEJB //implements SessionBean
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
		System.out.println("ejbPassivate() method calling........");		
	}*/
	
	public String postSave()throws RemoteException,ITMException
	{
		return "";
	}
		
	//public String postSave(String winName,String editFlag,String tranId, String xtraParams, Connection conn) throws RemoteException,ITMException
	public String postSave(String tranId, String editFlag, String xtraParams, Connection conn) throws RemoteException,ITMException
	
	{
		String retString = "";
		boolean isError = false;
		PreparedStatement pStmt = null;		
		String sql="";
		boolean isLocalConn = false;
		try
		{
			if(conn == null)
			{
				System.out.println("connection is null");
				ConnDriver connDriver = null;
				connDriver = new ConnDriver();
				//Changes and Commented By Bhushan on 13-06-2016 :START
				//conn = connDriver.getConnectDB("DriverValidator");
				conn = getConnection();
				//Changes and Commented By Bhushan on 13-06-2016 :END
				isLocalConn = true;
				conn.setAutoCommit( false );
				connDriver = null;
			}
			int totOpValue =	0;
			int totPurValue =	0;
			int totSalesValue =	0;
			int totClValue =	0;

			sql ="SELECT SUM(OP_VALUE) AS OP_VALUE, SUM(PUR_VALUE) AS PUR_VALUE, SUM(SALES_VALUE) AS SALES_VALUE, SUM(CL_VALUE) AS CL_VALUE FROM CUST_STOCK_DET WHERE TRAN_ID = ? ";
			System.out.println("sql [" + sql + "]");
			pStmt = conn.prepareStatement( sql );
			pStmt.setString( 1, tranId);
			ResultSet rs = pStmt.executeQuery();			
			while( rs.next())
			{
				totOpValue =	rs.getInt( "OP_VALUE" );          
				totPurValue =	rs.getInt( "PUR_VALUE" );        
				totSalesValue =	rs.getInt( "SALES_VALUE" );    
                totClValue =	rs.getInt( "CL_VALUE" );         
			}
			if (pStmt != null)
			{
				pStmt.close(); 
				pStmt = null;
			}

			sql ="UPDATE CUST_STOCK SET TOT_OP_VALUE = ?, TOT_PUR_VALUE = ?, TOT_SALES_VALUE = ?, TOT_CL_VALUE = ?  WHERE TRAN_ID = ? "; 
			System.out.println("sql [" + sql + "]");
			System.out.println( " totOpValue [" + totOpValue + "]");
			System.out.println( " totPurValue [" + totPurValue + "]");
			System.out.println( " totSalesValue [" + totSalesValue + "]");
			System.out.println( " totClValue [" + totClValue + "]");

			pStmt = conn.prepareStatement( sql );
			pStmt.setInt( 1, totOpValue);
			pStmt.setInt( 2, totPurValue);
			pStmt.setInt( 3, totSalesValue);
			pStmt.setInt( 4, totClValue);
			pStmt.setString( 5, tranId);
			int updateCnt = pStmt.executeUpdate();
			System.out.println( "updateCnt [" + updateCnt + "]" );
		}
		catch(Exception e)
		{
			System.out.println("Exception  :==>\n"+e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				//conn.commit();
				System.out.println("isError ["+isError+"]");
				System.out.println("isLocalConn ["+isLocalConn+"]");
				if( conn != null )
				{

					if( isError )
					{
						conn.rollback();
					}
					if(pStmt != null)
					{
						pStmt.close();
						pStmt = null;
					}
					if ( isLocalConn )
					{
						if ( ! isError )
						{
							conn.commit();
						}
						conn.close();
						conn = null;
					}
					
				}
			}
			catch(Exception e)
			{
				System.out.println("Exception :==>\n"+e.getMessage());
				throw new ITMException(e);
			}
		}
		System.out.println("Return string :"+retString);
		return retString;
	}
}

	


