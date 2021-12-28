
/********************************************************
	Title : SOrderFormPostsaveEJB
	Date  : 19/03/2009
	Author: pankaj singh

********************************************************/

package ibase.webitm.ejb.dis;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.util.Date;
import javax.ejb.CreateException;
import java.text.SimpleDateFormat;
import java.sql.Timestamp;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.ejb.Stateless; 


@Stateless 
public class SRetPostSave extends ValidatorEJB implements SRetPostSaveLocal, SRetPostSaveRemote
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	
	public String postSave() throws RemoteException,ITMException
	{
		return "";
	}
	public String postSaveRec() throws RemoteException, ITMException
	{
		return "";
	}
	public String postSaveRec(String xmlString1, String domId, String objContext, String editFlag, String xtraParams, Connection conn) throws RemoteException, ITMException
	{
		return "";
	}

	
	public String postSave(String winName,String editFlag,String tranId,  String xtraParams,Connection conn) throws RemoteException,ITMException
	{
		String sql = "",errorString="",invoiceID="";
		//GenericUtility genericUtility = GenericUtility.getInstance();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ITMDBAccessEJB	itmDBAccessEJB = new ITMDBAccessEJB(); //added by nandkumar gadkari on 18/09/19
		double taxAmtHdr = 0.0, netAmtHdr = 0.0, effNetAmtHdr = 0.0,adjAmtHdr = 0.0,rate=0.0;
		
		try
		{
					
			sql = "update sreturndet set "
					+ "	net_amt = (quantity__stduom * rate__stduom) - ( (quantity__stduom * rate__stduom * case when discount is null then 0 else discount end ) / 100) + case when tax_amt is null then 0 else tax_amt end ,"
					+ " eff_net_amt = ( case when ret_rep_flag = 'R' then (quantity__stduom * rate__stduom) - ( (quantity__stduom * rate__stduom * case when discount is null then 0 else discount end ) / 100) + case when tax_amt is null then 0 else tax_amt end  else (-1 * ((quantity__stduom * rate__stduom) - ( (quantity__stduom * rate__stduom * case when discount is null then 0 else discount end ) / 100) + case when tax_amt is null then 0 else tax_amt end )) end )"
					+ " where  tran_id = ? ";
			pstmt= conn.prepareStatement(sql);
			//System.out.println("First sql ["+ sql + "] tranId [" + tranId + "]");
			pstmt.setString( 1, tranId );
			pstmt.executeUpdate();
			pstmt.close();
			pstmt = null;		

			sql = "select sum(net_amt), sum(tax_amt),sum(eff_net_amt) "
				+ " from sreturndet where tran_id = ? ";
			pstmt= conn.prepareStatement(sql);
			//System.out.println("Second sql ["+sql + "]");
			pstmt.setString( 1, tranId );
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				netAmtHdr = rs.getDouble(1);
				taxAmtHdr = rs.getDouble(2);
				effNetAmtHdr = rs.getDouble(3);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			sql = "select sum(adj_amt) "
				+ " from sreturn_inv where tran_id = ? ";
			pstmt= conn.prepareStatement(sql);
			//System.out.println("Third sql ["+sql + "]");
			pstmt.setString( 1, tranId );
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				adjAmtHdr = rs.getDouble(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			sql = " update sreturn set "
						+ " net_amt = ?, tax_amt = ?, eff_net_amt = ?, adj_amount = ? "
					+ " where  tran_id = ? " ;
			pstmt= conn.prepareStatement( sql );
			//System.out.println("Fourth sql ["+sql + "]");
			
			pstmt.setDouble( 1, netAmtHdr );
			pstmt.setDouble( 2, taxAmtHdr );
			pstmt.setDouble( 3, effNetAmtHdr );
			pstmt.setDouble( 4, adjAmtHdr );
			pstmt.setString( 5, tranId );
			
			pstmt.executeUpdate();
			pstmt.close();
			pstmt = null;
			
		
			//validation added by nandkumar gadkari on 18/09/19-----------start----------------
			sql = "select invoice_id , rate  "
					+ " from sreturndet where tran_id = ? ";
				pstmt= conn.prepareStatement(sql);
				pstmt.setString( 1, tranId );
				rs = pstmt.executeQuery();
				while (rs.next())
				{
					invoiceID = rs.getString(1);
					rate = rs.getDouble(2);
					
					if(rate < 0)
					{
						errorString=itmDBAccessEJB.getErrorString("","VTRATE2","","",conn);
						return errorString;
					}
					else
					{
						if( invoiceID == null || invoiceID.trim().length() == 0 )
						{
							
							if ( rate == 0 )
							{
								errorString=itmDBAccessEJB.getErrorString("","VTRATE3","","",conn);
								return errorString;
							}
							
						}
					}
				
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			
				
			
			//validation added by nandkumar gadkari on 18/09/19-----------end----------------
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(pstmt != null)pstmt.close();
				pstmt = null;
			}catch(Exception d)
			{
			  d.printStackTrace();
				throw new ITMException(d);
			}
		}
		//return "";commented and added by nandkumar gadkari on 18/09/19
		return	errorString;
	}
 }// END OF MAIN CLASS