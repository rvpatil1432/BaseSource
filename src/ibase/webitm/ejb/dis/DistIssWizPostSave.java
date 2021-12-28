package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.w3c.dom.Document;

@javax.ejb.Stateless
public class DistIssWizPostSave extends ValidatorEJB implements DistIssWizPostSaveLocal,DistIssWizPostSaveRemote
{
	public String postSave(String xmlString,String editFlag,String tranId,  String xtraParams,Connection conn) throws RemoteException,ITMException
	{

		System.out.println(">>>>>>>>>>>>>>CONNECTION"+conn);
		System.out.println("------------  postSave method called-----------------tranId : "+ tranId);		
		Document dom = null;
		String errString="";

		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility= new  E12GenericUtility();



		

		try
		{
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				dom = genericUtility.parseString(xmlString); 
				System.out.println("xmlString d" + xmlString);
			}
			
			tranId = genericUtility.getColumnValue("tran_id",dom);

			System.out.println("------------  postSave method called-----------------tranId from dom: "+ tranId);
			
			
			errString = postSave(dom,tranId,editFlag,xtraParams,conn);


		}
		catch(Exception e)
		{
			System.out.println("Exception :  : postSave : ==>\n"+e.getMessage());
			throw new ITMException(e);
		}		
		return errString;
	}

	public String postSave(Document dom,String tranId,String editflag,String xtraParams,Connection conn)
	{
		String sql = "",shipmentId = "";
		//GenericUtility genericUtility = GenericUtility.getInstance();
		PreparedStatement pstmt = null,pstmt1 = null;
		String lotsl = "",siteCode="";
		boolean isLocalConn = false;
		double quantity = 0,allQty=0,holdQty=0;
		ResultSet rs = null,rs1 = null;		
		double amount = 0,netAmount=0,discAmount=0,taxAmount=0,noArt = 0;	//net_amt =amount+tax_amt-disc_amt
		double grossWeight = 0 ,tareWeight = 0 , netWeight = 0;
		try
		{
			
			E12GenericUtility genericUtility= new  E12GenericUtility();	
			conn = null;
			
			if(conn == null){
				
				ConnDriver connDriver = new ConnDriver();
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
				connDriver= null;
				isLocalConn = true;
				conn.setAutoCommit(false);
			}

			siteCode = genericUtility.getColumnValue("site_code",dom);
			
			sql = "select lot_sl from distord_issdet where tran_id = ? ";
			pstmt= conn.prepareStatement(sql);
			//System.out.println("Second sql ["+sql + "]");
			pstmt.setString( 1, tranId );
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				lotsl = rs.getString(1) == null ? "":rs.getString(1);
				
				sql = "select quantity from distord_issdet where tran_id = ? and lot_sl = ?";
				pstmt1= conn.prepareStatement(sql);
				//System.out.println("Second sql ["+sql + "]");
				pstmt1.setString( 1, tranId );
				pstmt1.setString( 2, lotsl );
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{
					quantity = rs1.getDouble("quantity");
					
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
				
				
				sql = "select ALLOC_QTY,HOLD_QTY from stock WHERE site_code = ? AND LOT_SL = ? AND QUANTITY > 0";
				pstmt1= conn.prepareStatement(sql);
				//System.out.println("Second sql ["+sql + "]");
				pstmt1.setString( 1, siteCode);
				pstmt1.setString( 2, lotsl);
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{
					allQty = rs1.getDouble(1);
					holdQty = rs1.getDouble(2);
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
				
				if(allQty > 0)
				{
					quantity = allQty + quantity;
					quantity = quantity - holdQty;
				}
				
				sql = "UPDATE stock SET ALLOC_QTY = ? WHERE site_code = ? AND LOT_SL = ? AND QUANTITY > 0";
				pstmt1= conn.prepareStatement(sql);
				//System.out.println("First sql ["+ sql + "] tranId [" + tranId + "]");
				pstmt1.setDouble( 1, quantity);
				pstmt1.setString( 2, siteCode);
				pstmt1.setString( 3, lotsl);
				pstmt1.executeUpdate();
				//conn.commit();
				pstmt1.close();
				pstmt1 = null;		
				
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			

			sql = "update distord_issdet  set "
					+"	amount = quantity * rate , "
					+"	disc_amt = quantity * rate * discount/100 ,  " 
					+ "	net_amt = quantity * rate  + tax_amt - (quantity * rate * discount/100 )  "
					+ " where  tran_id = ? ";
			pstmt= conn.prepareStatement(sql);
			//System.out.println("First sql ["+ sql + "] tranId [" + tranId + "]");
			pstmt.setString( 1, tranId );
			pstmt.executeUpdate();
			//conn.commit();
			pstmt.close();
			pstmt = null;		

			sql = "select sum(amount), sum(net_amt),sum(disc_amt),sum(tax_amt),sum(no_art) , sum(gross_weight) ,sum (tare_weight) , sum(net_weight )  "
					+ " from distord_issdet where tran_id = ? ";
			pstmt= conn.prepareStatement(sql);
			//System.out.println("Second sql ["+sql + "]");
			pstmt.setString( 1, tranId );
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				amount = rs.getDouble(1);
				netAmount = rs.getDouble(2);
				discAmount = rs.getDouble(3);
				taxAmount = rs.getDouble(4);
				noArt = rs.getDouble(5);
				//added by kunal on 06/AUG/13
				grossWeight = rs.getDouble(6);
				tareWeight = rs.getDouble(7);
				netWeight = rs.getDouble(8);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			//added by kunal on 06/AUG/13 
			sql = "select shipment_id  from  distord_iss where tran_id = ? ";
			pstmt= conn.prepareStatement(sql);
			pstmt.setString( 1, tranId );
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				shipmentId = rs.getString(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			if(shipmentId != null && shipmentId.trim().length() > 0)
			{
				grossWeight = 0;
				tareWeight = 0;
				netWeight = 0;
			}


			sql = " update distord_iss  set "
					+ " amount = ?, net_amt = ?, discount = ? , tax_amt = ? , no_art = ? ,gross_weight = ? , tare_weight = ? , net_weight = ? "
					+ "  where  tran_id = ? " ;
			pstmt= conn.prepareStatement( sql );
			//System.out.println("Fourth sql ["+sql + "]");

			pstmt.setDouble( 1, amount );
			pstmt.setDouble( 2, netAmount );
			pstmt.setDouble( 3, discAmount );
			pstmt.setDouble( 4, taxAmount );
			pstmt.setDouble( 5, noArt );
			pstmt.setDouble( 6, grossWeight );////added by kunal on 06/AUG/13
			pstmt.setDouble( 7, tareWeight );//added by kunal on 06/AUG/13
			pstmt.setDouble( 8, netWeight );//added by kunal on 06/AUG/13
			pstmt.setString( 9, tranId );			
			pstmt.executeUpdate();
			pstmt.close();
			pstmt = null;
			
			//conn.commit();

		}
		catch(Exception e)
		{
			
			try {
				System.out.println(">>>>>>>>>>>>In catch Before rollback>>>");
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				
			}
			
			e.printStackTrace();
			try {
				throw new ITMException(e);
			} catch (ITMException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}
		finally
		{
			try
			{
				if(rs!=null){
					rs.close();
					rs = null;
				}
			
				if(pstmt != null){
					pstmt.close();
					pstmt = null;
				}
				
				if(isLocalConn){
					if(conn != null)
					{
						conn.commit();
						conn.close();	
						conn = null;
					}
					
				}
			}catch(Exception d)
			{
				d.printStackTrace();
				try {
					throw new ITMException(d);
				} catch (ITMException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return "";
	}

}
