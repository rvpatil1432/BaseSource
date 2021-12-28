package ibase.webitm.ejb.dis;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.dis.InvAllocTraceBean;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import org.w3c.dom.Document;

import javax.ejb.Stateless;


@Stateless 
public class DissIssuePosSave extends ValidatorEJB implements   DissIssuePosSaveLocal,DissIssuePosSaveRemote // SessionBean
{	

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
		String sql = "",shipmentId = "";
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility= new  E12GenericUtility();
		PreparedStatement pstmt = null;
		ResultSet rs = null;		
		double amount = 0,netAmount=0,discAmount=0,taxAmount=0,noArt = 0;	//net_amt =amount+tax_amt-disc_amt
		double grossWeight = 0 ,tareWeight = 0 , netWeight = 0;
		boolean isLocalConn = false;
		try
		{
			
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
			
			
			//conn.setAutoCommit(false);	// commented on 7-Oct-2014 as not needed for framework conn Sandesh 
			
			sql = "update distord_issdet  set "
					+"	amount = quantity * rate , "
					+"	disc_amt = quantity * rate * discount/100 ,  " 
					+ "	net_amt = quantity * rate  + tax_amt - (quantity * rate * discount/100 )  "
					+ " where  tran_id = ? ";
			pstmt= conn.prepareStatement(sql);
			//System.out.println("First sql ["+ sql + "] tranId [" + tranId + "]");
			pstmt.setString( 1, tranId );
			pstmt.executeUpdate();
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

		}
		catch(Exception e)
		{
			// rollback code Sandesh 7-Oct-2014
			try {
				conn.rollback();				
			} catch (Exception d) {
				d.printStackTrace();
			}
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(isLocalConn){
					conn.commit();
					conn.close();
					conn = null;
				}
				if(pstmt != null){
					pstmt.close();
					pstmt = null;
				}
				
			}catch(Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		return "";
	}

	
	public String postSaveCall(String xmlString,String editFlag,String tranId,  String xtraParams,Connection conn) throws RemoteException,ITMException
	{


		System.out.println("----------postSaveWiz method ......................."+tranId);
		System.out.println("editFlag------>>["+editFlag+"]");		
		System.out.println("xmlString------>>["+xmlString+"]");
		String sql = "",saleOrder = "",retString="",currAppdate="",lineNoS="",siteCode="",itemCode="",
				locCode="",lotNo="",lotSl="",quantityS="",chgUser="",chgTerm="";
		int lineNo = 0,cnt=0;
		Document dom = null;
		double grossWeight = 0,tareWeight = 0,netWeight = 0,noAart = 0,offinvAmt = 0,billbackAmt = 0,quantity=0 ;
		double quantityStduom = 0 ,rateStduom = 0,offinvAmtDet = 0,taxAmtDet = 0 ,discount = 0,totAmt = 0,amount = 0;  
		//GenericUtility genericUtility = GenericUtility.getInstance();
		PreparedStatement pstmt = null,pstmt1 = null;
		ResultSet rs = null,rs1 = null;
		boolean isError=false;
		java.sql.Timestamp currDate = null;
		SimpleDateFormat sdf = null;
		HashMap allocQtyMap=new HashMap();
		InvAllocTraceBean allocTraceBean=new InvAllocTraceBean();
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility= new  E12GenericUtility();
		
		String shipmentId = "";
		
		
		boolean isLocalConn = false;
		double netAmount=0,discAmount=0,taxAmount=0,noArt = 0;	//net_amt =amount+tax_amt-disc_amt
		double allqty = 0;
		
		try
		{
			
            conn = null;
			
          
			
			if(conn == null){
				
				ConnDriver connDriver = new ConnDriver();
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
				connDriver= null;
				isLocalConn = true;
				
			}
			
			conn.setAutoCommit(false);

			
			try
			{
				if(xmlString != null && xmlString.trim().length()!=0)
				{
					dom = genericUtility.parseString(xmlString); 
					System.out.println("xmlString d" + xmlString);
				}
				
				tranId = genericUtility.getColumnValue("tran_id",dom);

				System.out.println("------------ WorkOrdRcpPostSave postSave method called-----------------tranId from dom: "+ tranId);
				
				
					System.out.println("dom not null.........");
					//tranId = GenericUtility.getInstance().getColumnValue("tran_id",dom);
					lineNoS =genericUtility.getColumnValue("line_no",dom); 
					 siteCode= genericUtility.getColumnValue("site_code",dom);				
					 itemCode= genericUtility.getColumnValue("item_code",dom);
					 
					 locCode= genericUtility.getColumnValue("loc_code",dom);
					lotNo= genericUtility.getColumnValue("lot_no",dom);
					lotSl= genericUtility.getColumnValue("lot_sl",dom);
					quantityS= genericUtility.getColumnValue("quantity",dom);
					
					chgUser= genericUtility.getColumnValue("chg_user",dom);
					chgTerm= genericUtility.getColumnValue("chg_term",dom);
					
					System.out.println("tranId----->>["+tranId+"]");
					System.out.println("lineNoS----->>["+lineNoS+"]");
					System.out.println("siteCode----->>["+siteCode+"]");
					System.out.println("itemCode----->>["+itemCode+"]");
					System.out.println("locCode----->>["+locCode+"]");
					System.out.println("lotSl----->>["+lotSl+"]");
					System.out.println("quantity----->>["+quantity+"]");
					
					System.out.println("chgUser----->>["+chgUser+"]");
					System.out.println("chgTerm----->>["+chgTerm+"]");
					
				

			}
			catch(Exception e)
			{
				System.out.println("Exception : dississpossave : postSave : ==>\n"+e.getMessage());
				throw new ITMException(e);
			}
			
			
			sql = "update distord_issdet  set "
					+"	amount = quantity * rate , "
					+"	disc_amt = quantity * rate * discount/100 ,  " 
					+ "	net_amt = quantity * rate  + tax_amt - (quantity * rate * discount/100 )  "
					+ " where  tran_id = ? ";
			pstmt= conn.prepareStatement(sql);
			//System.out.println("First sql ["+ sql + "] tranId [" + tranId + "]");
			pstmt.setString( 1, tranId );
			pstmt.executeUpdate();
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

			
			
/*			
			sql = "select alloc_qty from stock where site_code = ? AND LOT_SL = ? AND QUANTITY > 0";
			pstmt= conn.prepareStatement(sql);
			pstmt.setString( 1, siteCode );
			pstmt.setString( 2, lotSl );
			
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				allqty = rs.getDouble(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			
			
			quantity  = Double.parseDouble(quantityS);
		
			
			
			if(allqty < quantity)
			{
				//Allocating stock 
				quantityS = quantityS == null ? "0":quantityS.trim();
				allocQtyMap.put("tran_date", currAppdate);
				allocQtyMap.put("ref_ser", "WI-ISS");
				allocQtyMap.put("ref_id", tranId);
				allocQtyMap.put("ref_line",lineNoS);
				allocQtyMap.put("site_code",siteCode);
				allocQtyMap.put("item_code",itemCode);
				//hashMap.put("loc_code",locCode[iRow]);
				allocQtyMap.put("loc_code",locCode);
				allocQtyMap.put("lot_no",lotNo);
				allocQtyMap.put("lot_sl",lotSl);
				allocQtyMap.put("alloc_qty",Double.parseDouble(quantityS));
				allocQtyMap.put("chg_user",chgUser);
				allocQtyMap.put("chg_term",chgTerm);
				allocQtyMap.put("chg_win","W_DISTISSWIZ");
				
				System.out.println("called allocTraceBean.updateInvallocTrace(allocQtyMap, conn);");
				retString=allocTraceBean.updateInvallocTrace(allocQtyMap, conn);
			}
			*/
			
			
			
			
		}
		catch(Exception e)
		{
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
			throw new ITMException(e);
			
		}
		finally
		{
			try
			{
				if(isLocalConn){
					if(conn != null)
					{
						conn.commit();
						conn.close();	
						conn = null;
					}
					
				}
				
				if(pstmt != null)pstmt.close();
				pstmt = null;
			}catch(Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		return "";
	
	
	}
}// end of DissIssuePosSaveEJB class 
