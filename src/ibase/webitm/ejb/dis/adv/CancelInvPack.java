package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import java.util.*;
import java.io.*;
import java.util.Date;
import java.sql.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.ejb.*;
import javax.naming.InitialContext;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.utility.E12GenericUtility;
import ibase.ejb.*;
import ibase.system.config.*;
import java.text.SimpleDateFormat;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class CancelInvPack extends ActionHandlerEJB implements CancelInvPackLocal, CancelInvPackRemote
{
	ibase.utility.E12GenericUtility genericUtility= null;
	ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
	Connection conn = null;
	PreparedStatement pstmt = null;
	String errorString = null;

	/*public void ejbCreate() throws RemoteException, CreateException 
	{
		System.out.println("Create Method Called....");
		System.out.println("Create Method Called[CancelInvPack].................");
		ConnDriver connDriver = new ConnDriver();
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			
			connDriver = null;
		}
		catch(Exception e)
		{
			System.out.println("Exception in creating Connection");
		}
	}

	public void ejbRemove()
	{
	}

	public void ejbActivate() 
	{
	}

	public void ejbPassivate() 
	{
	}*/

    public String actionHandler() throws RemoteException,ITMException
	{
		System.out.println("actionHandler() Method Called....");
		return "";
	}
	public String actionHandler(String xmlString, String xtraParams, String objContext) throws RemoteException,ITMException
	{
		Document dom = null;
		String  retString = null;
		System.out.println("Xtra Params : " + xtraParams);
		genericUtility = new  ibase.utility.E12GenericUtility();
		try
		{	
			retString = actionCancel(xmlString, xtraParams);  
		}
	   	catch(Exception e)
		{
			System.out.println("Exception :Enquiry :actionHandler(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning from actionPickList actionHandler"+retString);
	    return (retString);
	}
	
	//by msalam on 05/07/07 for req-id :: DI78GIN021
	//First Issue the stock for previous location
	//Then Receive it at the issued location
	
	private String actionCancel(String tranId, String xtraParams) throws RemoteException , ITMException
	{
		String  sql = "",sql1 = "",sql2 = "",sql3 ="";
		String confirmed = "",status = "",itemCode = "";
		String siteCode = "",locCode = "",lotNo = "",lotSl = "";
		String errCode = "",errString = "";
		java.sql.Timestamp tranDate = null;
		
		PreparedStatement pstmt = null,pstmt1 = null;
		ResultSet rs = null;
		int count,upd = 0;
		boolean found = false;
		
		//by msalam on 05/07/07 for req-id :: DI78GIN021
		double quantity = 0.0;
		double allocQuantity = 0.0;
		double grossWt = 0.0;
		double tareWt = 0.0;
		double netWt = 0.0;
		String unit = null;
		String packCode = null;
		String packInstr = null;

		ibase.webitm.ejb.dis.StockUpdate updateStock = new  ibase.webitm.ejb.dis.StockUpdate();
		String cancelSql = null;
		HashMap cancelStockMap = null;
		//End by msalam on 05/07/07 for req-id :: DI78GIN021
		
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		try
		{
			ConnDriver connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver = null;
		    System.out.println("tranId ....:"+tranId);
		    sql = " SELECT CONFIRMED, STATUS FROM INV_PACK WHERE TRAN_ID = '"+tranId+"' ";
			pstmt = conn.prepareStatement(sql);
			System.out.println("sql ....:"+sql);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
			    confirmed = (rs.getString(1)==null?" ":rs.getString(1));
			    status = (rs.getString(2)==null ?" ":rs.getString(2));
			}
		    rs.close();
			pstmt.close();
			System.out.println("confirmed......"+confirmed);
		    System.out.println("status........."+status);
			if(confirmed.equalsIgnoreCase("Y") &&(!status.equalsIgnoreCase("X")))
			{
				sql1 = " SELECT STOCK.ITEM_CODE, "
				      +" STOCK.SITE_CODE, "
					  +" STOCK.LOC_CODE, "
					  +" STOCK.LOT_NO, "
					  +" STOCK.LOT_SL"
					  +" FROM STOCK,INV_PACK,INV_PACK_RCP "
					  +" WHERE INV_PACK.TRAN_ID = INV_PACK_RCP.TRAN_ID "
					  +" AND STOCK.ITEM_CODE = INV_PACK_RCP.ITEM_CODE "
					  +" AND STOCK.SITE_CODE = INV_PACK.SITE_CODE "
					  +" AND STOCK.LOC_CODE = INV_PACK_RCP.LOC_CODE "
					  +" AND STOCK.LOT_NO = INV_PACK_RCP.LOT_NO "
					  +" AND STOCK.LOT_SL = INV_PACK_RCP.LOT_SL "
					  +" AND STOCK.QUANTITY - STOCK.ALLOC_QTY > 0 "     
					  +" AND  INV_PACK.TRAN_ID = '"+tranId+"' "
					  +" AND STOCK.PACK_REF IS NOT NULL AND LENGTH(TRIM(STOCK.PACK_REF)) > 0";

				pstmt = conn.prepareStatement(sql1);
			    System.out.println("sql :"+sql1);
			    rs = pstmt.executeQuery();
				sql2 = "UPDATE STOCK SET PACK_REF = ? "
							   + " WHERE ITEM_CODE = ? "
							   + " AND SITE_CODE = ? "
							   + " AND LOC_CODE = ? "
							   + " AND LOT_NO = ? "
							   + " AND LOT_SL = ? " ;
				
				pstmt1 = conn.prepareStatement(sql2);
				
				while (rs.next())
			       {	found = true;
						itemCode = rs.getString(1);
						siteCode = rs.getString(2);
						locCode  = rs.getString(3);
						lotNo	 = rs.getString(4);
						lotSl	 = rs.getString(5);
						System.out.println("itemcode......"+itemCode);
						System.out.println("sitecode......"+siteCode);
						System.out.println("loccode......."+locCode);
						System.out.println("lotno........."+lotNo);
						System.out.println("lotsl........."+lotSl);
						pstmt1.setNull(1,java.sql.Types.VARCHAR);
						pstmt1.setString(2,itemCode);
						pstmt1.setString(3,siteCode);
						pstmt1.setString(4,locCode);
						pstmt1.setString(5,lotNo);
						pstmt1.setString(6,lotSl);
			            pstmt1.addBatch();
						System.out.println("sql......"+sql2);
				   }

				//end by msalam
				pstmt1.executeBatch();
				rs.close();
			    pstmt.close();
				pstmt1.close();
				sql3 = "UPDATE INV_PACK SET STATUS = ? "
					   +" WHERE TRAN_ID = ? " ;
				pstmt = conn.prepareStatement(sql3);
				pstmt.setString(1,"X");
				pstmt.setString(2,tranId);
				upd = pstmt.executeUpdate();
				if(upd == 1)
				{
				  System.out.println("Status updated by..."+upd);
				}
			    pstmt.close();
			    
			    //by msalam on 05/07/07 for req-id :: DI78GIN021
			    //Issue all the available items in the receipt
				cancelSql = " SELECT R.ITEM_CODE, H.SITE_CODE, R.LOC_CODE, R.LOT_NO, R.LOT_SL, H.TRAN_DATE ,R.QUANTITY, R.GROSS_WEIGHT, R.TARE_WEIGHT, R.NET_WEIGHT " +
							"	FROM INV_PACK H, INV_PACK_RCP R " +
							" where H.TRAN_ID = R.TRAN_ID  " +
							" 	and R.TRAN_ID = '" + tranId + "' ";

			    pstmt = conn.prepareStatement(cancelSql);
			    System.out.println("Cancel Sql :" + cancelSql);
			    rs = pstmt.executeQuery();
				
				while (rs.next())
				{
					itemCode = rs.getString(1);
					siteCode = rs.getString(2);
					locCode  = rs.getString(3);
					lotNo	 = rs.getString(4);
					lotSl	 = rs.getString(5);
					tranDate = rs.getTimestamp(6);
					quantity = rs.getDouble(7);
					grossWt = rs.getDouble(8);
					tareWt = rs.getDouble(9);
					netWt = rs.getDouble(10);

					System.out.println("itemcode......" + itemCode);
					System.out.println("sitecode......" + siteCode);
					System.out.println("loccode......." + locCode);
					System.out.println("lotno........." + lotNo);
					System.out.println("lotsl........." + lotSl);
					System.out.println("sql......" + sql2);
					
					cancelStockMap = new HashMap();
					
					cancelStockMap.put("item_code", itemCode);
					cancelStockMap.put("site_code", siteCode);
					cancelStockMap.put("loc_code", locCode);
					cancelStockMap.put("lot_no", lotNo);
					cancelStockMap.put("lot_sl", lotSl);
					cancelStockMap.put("qty_stduom", new Double(quantity));
					cancelStockMap.put("tran_ser", "I-PKI");
					
					cancelStockMap.put("net_weight", new Double(netWt));
					cancelStockMap.put("gross_weight", new Double(grossWt));
					cancelStockMap.put("tare_weight", new Double(tareWt));
					tranDate = getCurrdateAppFormat();
					cancelStockMap.put("tran_date", tranDate);

				
					cancelStockMap.put("tran_type", "I");
					updateStock.updateStock(cancelStockMap, xtraParams, conn);
					cancelStockMap = null;
				}
			
				rs.close();
			    pstmt.close();
			    
			    

			    //End of Issue all by msalam on 05/07/07 for req-id :: DI78GIN021
			    
			    //by msalam on 05/07/07 for req-id :: DI78GIN021
			    //Receipt all the available items
			    
				cancelSql = " SELECT HDR.SITE_CODE, DTL.ITEM_CODE, DTL.LOC_CODE, DTL.LOT_NO, DTL.LOT_SL, " +
							"		  DTL.QUANTITY, DTL.GROSS_WEIGHT, DTL.TARE_WEIGHT, DTL.NET_WEIGHT, DTL.UNIT, DTL.PACK_CODE, DTL.PACK_INSTR, HDR.TRAN_DATE " +
							"	FROM INV_PACK HDR, INV_PACK_ISS DTL " +
							" where HDR.TRAN_ID = DTL.TRAN_ID " +
							" 	AND HDR.TRAN_ID = '" + tranId + "' ";


			    pstmt = conn.prepareStatement(cancelSql);
			    System.out.println("Cancel Sql :" + cancelSql);
			    rs = pstmt.executeQuery();
				
				while (rs.next())
				{
					siteCode = rs.getString(1);
					itemCode = rs.getString(2);
					locCode  = rs.getString(3);
					lotNo	 = rs.getString(4);
					lotSl	 = rs.getString(5);
					quantity = rs.getDouble(6);
					grossWt = rs.getDouble(7);
					tareWt = rs.getDouble(8);
					netWt = rs.getDouble(9);
					unit = rs.getString(10);
					packCode = rs.getString(11);
					packInstr = rs.getString(12);
					tranDate = rs.getTimestamp(13);
					
					System.out.println("itemcode......"+itemCode);
					System.out.println("loccode......."+locCode);
					System.out.println("lotno........."+lotNo);
					System.out.println("lotsl........."+lotSl);
					
					System.out.println("siteCode......." + siteCode);
					System.out.println("quantity........." + quantity);
					System.out.println("tareWt........." + tareWt);
					System.out.println("grossWt........." + grossWt);
					System.out.println("netWt........." + netWt);

					System.out.println("sql......"+sql2);
					
					cancelStockMap = new HashMap();
					
					cancelStockMap.put("item_code", itemCode);
					cancelStockMap.put("loc_code", locCode);
					cancelStockMap.put("site_code", siteCode);
					cancelStockMap.put("lot_no", lotNo);
					cancelStockMap.put("lot_sl", lotSl);
					cancelStockMap.put("net_weight", new Double(netWt));
					cancelStockMap.put("gross_weight", new Double(grossWt));
					cancelStockMap.put("tare_weight", new Double(tareWt));
					cancelStockMap.put("quantity", new Double(quantity));
					cancelStockMap.put("qty_stduom", new Double(quantity));
					cancelStockMap.put("unit", unit);
					cancelStockMap.put("pack_code", packCode);
					cancelStockMap.put("pack_instr", packInstr);
					cancelStockMap.put("tran_ser", "I-PKR");
					//tranDate = new Timestamp(System.currentTimeMillis());
					tranDate = getCurrdateAppFormat();
					cancelStockMap.put("tran_date", tranDate);

					cancelStockMap.put("tran_type", "R");
					updateStock.updateStock(cancelStockMap, xtraParams, conn);
					cancelStockMap = null;
				   }
				
				rs.close();
			    pstmt.close();

			    //End of Receipt all by msalam on 05/07/07 for req-id :: DI78GIN021
			}
			else if(!confirmed.equalsIgnoreCase("Y"))  
			{
			   errCode = "NOCONFIRM";
			   System.out.println("errcode......"+errCode);
			   errString = itmDBAccess.getErrorString("",errCode,"","",conn);
			   return errString;
			}
			else if(status.equalsIgnoreCase("X"))  
			{
			   errCode = "STATUSX";
			   System.out.println("errcode......"+errCode);
			   errString = itmDBAccess.getErrorString("",errCode,"","",conn);
			   return errString;
			}
			if(!found)
			{
				errCode = "NOFOUND";
				System.out.println("errcode......"+errCode);
				errString = itmDBAccess.getErrorString("",errCode,"","",conn);
				return errString;
			}
            if(found)
			{ 
				errCode = "UNPACKSUCC";
				System.out.println("errcode......"+errCode);
				errString = itmDBAccess.getErrorString("",errCode,"","",conn);
            }
			
	    }
		catch (SQLException sqx)
		{
			System.out.println("The SQLException occurs in invPakc for CANCEL button :"+sqx);
			sqx.printStackTrace();
			errorString=sqx.getMessage();
			
		}
		catch (Exception e)
		{
			System.out.println("The Exception occure in InvPakc  for CANCEL button:"+e);
			e.printStackTrace();
			errorString=e.getMessage();
		}
		finally
		{
			try
			{
				if(errCode.equalsIgnoreCase("UNPACKSUCC"))
				{
					System.out.println("Connection Commited");
					conn.commit();
				}
				else      
				{
					System.out.println("Connection is rollback");
					conn.rollback();
				}
				pstmt.close();
				pstmt = null;
				conn.close();
			    conn = null;
			}
			catch (Exception e)
				{
				
					errString = e.getMessage();
					e.printStackTrace();
					return errString ;
			    }
		}
		return  errString;
	}
	
	//End by msalam on 05/07/07 for req-id :: DI78GIN021
	//signature changed by msalam
	//private String getCurrdateAppFormat()
	private java.sql.Timestamp getCurrdateAppFormat()
	{
		String currAppdate ="";
		java.sql.Timestamp currDate = null;
		try
		{
				Object date = null;
				currDate =new java.sql.Timestamp(System.currentTimeMillis()) ;
				System.out.println(genericUtility.getDBDateFormat());
				SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getDBDateFormat());
				date = sdf.parse(currDate.toString());
				currDate =	java.sql.Timestamp.valueOf(sdf.format(date).toString() + " 00:00:00.0");
				currAppdate = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(currDate).toString();
		}
		catch(Exception e)
		{
			System.out.println("Exception in :::calcFrsBal"+e.getMessage());
		}
		return currDate;
		//LINE COMMENTED BY MSALAM AS TIMESTAMP IS NEEDED
		//return (currAppdate);
	}

}
