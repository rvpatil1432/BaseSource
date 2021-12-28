
/*
 * Added By Dhiraj Chavan
 * Req#-D16BSUN009
 * DATE -30-JUNE-2016
 * 
 * */

package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.ejb.Stateless;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.utility.ITMException;

@Stateless
public class PoStatChgConf extends ActionHandlerEJB implements PoStatChgConfLocal,PoStatChgConfRemote
{

	E12GenericUtility genericUtility= new  E12GenericUtility();
	public String actionHandler() throws RemoteException, ITMException
	{
		return "";
	}


	
	 public String actionHandler(String tranId, String xtraParams, String forcedFlag)
			    throws RemoteException, ITMException
			  {
			    String returnString = null;

			    System.out.println(".......tranId......." + tranId);
			    System.out.println(".......xtraParams..." + xtraParams);
			    System.out.println(".......forcedFlag..." + forcedFlag);
			    if ((tranId != null) && (tranId.trim().length() > 0))
			    {
			      returnString = confirm(tranId, xtraParams, forcedFlag);
			      System.out.println("returnString@@dhiraj@@"+returnString);
			    }
			    return returnString;
			  }

	public String confirm(String tranId, String xtraParams, String forcedFlag) throws RemoteException, ITMException
	{
		System.out.println("POrderAmdConf confirm called..............");
		String confirmed = "";
		String sql = "",sql1="";
		// String sql1 = "";
		// int status = 0;
		double quantity = 0.0,ordQty=0.0,indQty=0.0,qty=0.0,totalQty=0.0,dlvQty=0.0;
		// Date lrDate = null;
		//Connection conn = null;
		PreparedStatement pstmt = null,pstmt1 = null;
		// PreparedStatement pstmt1 = null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		String errString = "", errCode = "",indNo="",errString1="";
		ResultSet rs = null,rs1 = null;
		// ResultSet rs1 = null;
		String purcOrder = "", poStatus = "",lineNo="", amdNo = "",statusReason="", workflowStatus = "",poHdrStatus="",indentNo="",status="";
		int recCnt = 0, cnt = 0,statusCnt=0;
		
		//ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		
		//purcOrder=genericUtility.getColumnValue("purc_order",dom);
		itmDBAccessEJB = new ITMDBAccessEJB();
		DistCommon distCommon = new DistCommon();
		Connection conn = null;
		try {
			
			
			ConnDriver connDriver = new ConnDriver();
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			conn.setAutoCommit(false);
			connDriver = null;
			} catch (Exception e) {
			System.out.println("Exception :POStatChgConf :ejbCreate :==>"
					+ e);
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
			}
		
		
		try
		{
			
			int updCnt=0;
			Timestamp sysDate1 = null;
			SimpleDateFormat sdf1 = null;
			String sysDateStr1 = "";
			//DistCommon distCommon = new DistCommon();
			Calendar currentDate = Calendar.getInstance();
			sdf1 = new SimpleDateFormat(
					genericUtility.getApplDateFormat());
			sysDateStr1 = sdf1.format(currentDate.getTime());
			System.out
					.println("after conf method Now the date is :=>  "
							+ sysDateStr1);
			sysDate1 = Timestamp.valueOf(genericUtility
					.getValidDateString(sysDateStr1,
							genericUtility.getApplDateFormat(),
							genericUtility.getDBDateFormat())
					+ " 00:00:00.0");
			String userId = genericUtility.getValueFromXTRA_PARAMS(
					xtraParams, "loginCode");
			
			double poDetQty=0,quantityStduom=0,orderQty=0,convIndOrdQty=0;
			String unit="",unitStd="",itemCode="";
			sql = " Select purc_order,confirmed,status_reason from porder_stat_chg 	Where tran_id=? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs=pstmt.executeQuery();
      	     	if(rs.next())
      	     	{
      	     		purcOrder=rs.getString(1);
      	     		confirmed=rs.getString(2);
      	     		statusReason=rs.getString(3);
      	     		
      	     	}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if(confirmed.equals("C")||confirmed.equalsIgnoreCase("C"))
			{
				
				errString = itmDBAccessEJB.getErrorString("", "VTRECONF", "","", conn);
				return errString;
			}
			
			sql = " Select count(*) from porcp 	Where purc_order=? and confirmed = 'N' and tran_ser='P-RCP' ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, purcOrder);
			rs=pstmt.executeQuery();
      	     	if(rs.next())
      	     	{
      	     		cnt=rs.getInt(1);
      	     		
      	     	}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if(cnt>0)
			{
				errCode = "VTPORCPCN";
				errCode = itmDBAccessEJB.getErrorString("", errCode, "","",conn);
				return errCode;
			}
			
			sql = " Select count(*) from porcp 	Where purc_order=? and confirmed = 'N' and tran_ser='P-RET' ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, purcOrder);
			rs=pstmt.executeQuery();
      	     	if(rs.next())
      	     	{
      	     		cnt=rs.getInt(1);
      	     		
      	     	}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if(cnt>0)
			{
				errCode = "VTPORETCL";
				errCode = itmDBAccessEJB.getErrorString("", errCode, "","",conn);
				return errCode;
			}
			
			
			
			
			sql = " Select status from porder 	Where purc_order=? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, purcOrder);
			rs=pstmt.executeQuery();
      	     	if(rs.next())
      	     	{
      	     		status=rs.getString(1);
      	     		
      	     	}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			if(status.equals("C"))
			{
				int count=0;
					System.out.println("@tranID@@"+purcOrder);
					sql1 = " select quantity,ind_no,dlv_qty,unit,unit__std,item_code,line_no from porddet where purc_order=? and status='C' and (dlv_qty<quantity) ";
					pstmt1 = conn.prepareStatement(sql1);
					pstmt1.setString(1, purcOrder);
					rs1 = pstmt1.executeQuery();
					while(rs1.next())
					{
						count++;
						System.out.println("whileCount@@"+count);
						quantity = rs1.getDouble(1);
						indNo = rs1.getString(2);
						dlvQty = rs1.getDouble(3);
						unit=rs1.getString(4);
						unitStd=rs1.getString(5);
						itemCode=rs1.getString(6);
						lineNo=rs1.getString(7);
						System.out.println("@quantity@@"+quantity);
						System.out.println("@indNo@@"+indNo);
						System.out.println("@dlvQty@@"+dlvQty);
						System.out.println("@unit@@"+unit);
						System.out.println("@unitStd@@"+unitStd);
						System.out.println("@itemCode@@"+itemCode);
						
						
						if(indNo!=null)
						{
						System.out.println("INDENT NUMBER FOUND"+indNo);
						sql = " Select ord_qty,quantity__stduom from indent 	Where ind_no=? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, indNo);
						rs=pstmt.executeQuery();
			      	     	if(rs.next())
			      	     	{
			      	     		orderQty=rs.getDouble(1);
			      	     		quantityStduom=rs.getDouble(2);
			      	     	}
			      	     	System.out.println("orderQty@@"+orderQty);
			      	     	System.out.println("quantityStduom@@"+quantityStduom);
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						poDetQty=quantity-dlvQty;
						System.out.println("[quantity-dlvQty][::"+poDetQty+"::]");
						//conversion function
						convIndOrdQty=distCommon.convQtyFactor(unit, unitStd, itemCode, poDetQty, conn)	;
						System.out.println("convIndOrdQty"+convIndOrdQty);
						double indOrdQty=orderQty+convIndOrdQty;
						System.out.println("indOrdQty@Partly@[orderQty+convIndOrdQty]"+indOrdQty);
						sql = " update porder set status='O' ,status_reason=?  where  purc_order=? ";
						pstmt = conn.prepareStatement(sql);
						
						pstmt.setString(1, statusReason);
						pstmt.setString(2, purcOrder);
						//pstmt.setString(2, purcOrder);
						updCnt=pstmt.executeUpdate();
			      	    System.out.println("porder updated@@@"+updCnt);
					//	rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						sql = " update porddet set status='O' where ind_no=? and purc_order=? and line_no=? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, indNo);
						pstmt.setString(2, purcOrder);
						pstmt.setString(3, lineNo);
					
						updCnt=pstmt.executeUpdate();
						System.out.println("porddet updated@@@"+updCnt); 	
						//rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						if(indOrdQty==quantityStduom)
						{
							System.out.println("(indOrdQty==quantityStduom)");
							sql=null;
							sql = " update indent set status='L' ,ord_qty="+indOrdQty+" ,status_date=? where ind_no=? ";
						}else
						{
							System.out.println("(indOrdQty!=quantityStduom)@NOT EQUAL");
							sql=null;
							sql = " update indent set status='O' ,ord_qty="+indOrdQty+" ,status_date=? where ind_no=? ";
						}
						
						pstmt = conn.prepareStatement(sql);
						pstmt.setTimestamp(1, sysDate1);
						pstmt.setString(2, indNo);
						
						updCnt=pstmt.executeUpdate();
			      	     System.out.println("Indent Updated"+updCnt);	
						//rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						sql=null;
						
						}
						else//without indent condition
						{
							
							System.out.println("without indent condition with status C");
							sql = " update porder set status='O' ,status_reason=?  where purc_order=? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, statusReason);
							pstmt.setString(2, purcOrder);
							//pstmt.setString(2, purcOrder);
							updCnt=pstmt.executeUpdate();
				      	    System.out.println("porder updCnt"+updCnt); 	
							//rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							sql = " update porddet set status='O' where  purc_order=? and line_no=? ";
							pstmt = conn.prepareStatement(sql);
							
							pstmt.setString(1, purcOrder);
							pstmt.setString(2, lineNo);
						
							updCnt=pstmt.executeUpdate();
							System.out.println("porddet updCnt"+updCnt);  	
							//rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
						}
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
				
				
			}else if(status.equals("O"))
			{
				System.out.println("comming into status O condition@@");
				int count=0;
				System.out.println("@tranID@@"+purcOrder);
				sql1 = " select quantity,ind_no,dlv_qty,unit,unit__std,item_code,line_no from porddet where purc_order=? and status='C' and (dlv_qty<quantity) ";
				pstmt1 = conn.prepareStatement(sql1);
				pstmt1.setString(1, purcOrder);
				rs1 = pstmt1.executeQuery();
				while(rs1.next())
				{
					count++;
					System.out.println("whileCount@@"+count);
					quantity = rs1.getDouble(1);
					indNo = rs1.getString(2);
					dlvQty = rs1.getDouble(3);
					unit=rs1.getString(4);
					unitStd=rs1.getString(5);
					itemCode=rs1.getString(6);
					lineNo=rs1.getString(7);
					System.out.println("@quantity@@"+quantity);
					System.out.println("@indNo@@"+indNo);
					System.out.println("@dlvQty@@"+dlvQty);
					System.out.println("@unit@@"+unit);
					System.out.println("@unitStd@@"+unitStd);
					System.out.println("@itemCode@@"+itemCode);
					
					
					if(indNo!=null)
					{
					System.out.println("INDENT NUMBER FOUND"+indNo);
					sql = " Select ord_qty,quantity__stduom from indent 	Where ind_no=? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, indNo);
					rs=pstmt.executeQuery();
		      	     	if(rs.next())
		      	     	{
		      	     		orderQty=rs.getDouble(1);
		      	     		quantityStduom=rs.getDouble(2);
		      	     	}
		      	     	System.out.println("orderQty@@"+orderQty);
		      	     	System.out.println("quantityStduom@@"+quantityStduom);
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					poDetQty=quantity-dlvQty;
					System.out.println("[quantity-dlvQty][::"+poDetQty+"::]");
					//conversion function
					convIndOrdQty=distCommon.convQtyFactor(unit, unitStd, itemCode, poDetQty, conn)	;
					System.out.println("convIndOrdQty"+convIndOrdQty);
					double indOrdQty=orderQty+convIndOrdQty;
					System.out.println("indOrdQty@Partly@[orderQty+convIndOrdQty]"+indOrdQty);
					sql = " update porder set status='O' ,status_reason=?  where  purc_order=? ";
					pstmt = conn.prepareStatement(sql);
					
					pstmt.setString(1, statusReason);
					pstmt.setString(2, purcOrder);
					//pstmt.setString(2, purcOrder);
					updCnt=pstmt.executeUpdate();
		      	    System.out.println("porder updated@@@"+updCnt);
				//	rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					sql = " update porddet set status='O' where ind_no=? and purc_order=? and line_no=? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, indNo);
					pstmt.setString(2, purcOrder);
					pstmt.setString(3, lineNo);
				
					updCnt=pstmt.executeUpdate();
					System.out.println("porddet updated@@@"+updCnt); 	
					//rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					if(indOrdQty==quantityStduom)
					{
						System.out.println("(indOrdQty==quantityStduom)");
						sql=null;
						sql = " update indent set status='L' ,ord_qty="+indOrdQty+" ,status_date=? where ind_no=? ";
					}else
					{
						System.out.println("(indOrdQty!=quantityStduom)@NOT EQUAL");
						sql=null;
						sql = " update indent set status='O' ,ord_qty="+indOrdQty+" ,status_date=? where ind_no=? ";
					}
					
					pstmt = conn.prepareStatement(sql);
					pstmt.setTimestamp(1, sysDate1);
					pstmt.setString(2, indNo);
					
					updCnt=pstmt.executeUpdate();
		      	     System.out.println("Indent Updated"+updCnt);	
					//rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					sql=null;
					
					}
					else//without indent condition
					{
						
						System.out.println("without indent condition with status C");
						sql = " update porder set status='O' ,status_reason=?  where purc_order=? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, statusReason);
						pstmt.setString(2, purcOrder);
						//pstmt.setString(2, purcOrder);
						updCnt=pstmt.executeUpdate();
			      	    System.out.println("porder updCnt"+updCnt); 	
						//rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						sql = " update porddet set status='O' where  purc_order=? and line_no=? ";
						pstmt = conn.prepareStatement(sql);
						
						pstmt.setString(1, purcOrder);
						pstmt.setString(2, lineNo);
					
						updCnt=pstmt.executeUpdate();
						System.out.println("porddet updCnt"+updCnt);  	
						//rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
					}
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
			
			
		
				
			}	
			else if(status.equals("X"))
			{
				System.out.println("Comming into status=X");
				
				

				sql = " Select count(1) from porddet Where status='C' and purc_order=? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, purcOrder);
				rs=pstmt.executeQuery();
	      	     	if(rs.next())
	      	     	{
	      	     		statusCnt=rs.getInt(1);
	      	     		
	      	     	}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				System.out.println("statusCnt@@@"+statusCnt);
				
			/*	if(statusCnt==0)
				{	

					sql=null;
					sql1 = " select quantity,ind_no,dlv_qty,unit,unit__std,item_code,line_no from porddet where purc_order=? and status='O' ";
				
				}else*/
				//{
					
					//sql=null;
					sql1 = " select quantity,ind_no,dlv_qty,unit,unit__std,item_code,line_no from porddet where purc_order=?  and (dlv_qty<quantity) ";
				
					
				//}
				
				pstmt1 = conn.prepareStatement(sql1);
				pstmt1.setString(1, purcOrder);
				rs1 = pstmt1.executeQuery();
				while(rs1.next())
				{
					quantity = rs1.getDouble(1);
					indNo = rs1.getString(2);
					dlvQty = rs1.getDouble(3);
					unit=rs1.getString(4);
					unitStd=rs1.getString(5);
					itemCode=rs1.getString(6);
					lineNo=rs1.getString(7);
					System.out.println("@quantity@@"+quantity);
					System.out.println("@indNo@@"+indNo);
					System.out.println("@dlvQty@@"+dlvQty);
					System.out.println("@unit@@"+unit);
					System.out.println("@unitStd@@"+unitStd);
					System.out.println("@itemCode@@"+itemCode);
					
					
					if(indNo!=null)
					{
					System.out.println("INDENT NUMBER FOUND"+indNo);
					sql = " Select ord_qty,quantity__stduom from indent 	Where ind_no=? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, indNo);
					rs=pstmt.executeQuery();
		      	     	if(rs.next())
		      	     	{
		      	     		orderQty=rs.getDouble(1);
		      	     		quantityStduom=rs.getDouble(2);
		      	     	}
		      	     	System.out.println("orderQty@@"+orderQty);
		      	     	System.out.println("quantityStduom@@"+quantityStduom);
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					poDetQty=quantity-dlvQty;
					System.out.println("[quantity-dlvQty][::"+poDetQty+"::]");
					//conversion function
					convIndOrdQty=distCommon.convQtyFactor(unit, unitStd, itemCode, poDetQty, conn)	;
					System.out.println("convIndOrdQty"+convIndOrdQty);
					double indOrdQty=orderQty+convIndOrdQty;
					System.out.println("indOrdQty@Partly@[orderQty+convIndOrdQty]"+indOrdQty);
					
					if(indOrdQty==quantityStduom)
					{
						System.out.println("(indOrdQty==quantityStduom)");
						sql=null;
						sql = " update indent set status='L' ,ord_qty="+indOrdQty+" ,status_date=? where ind_no=? ";
					}else
					{
						System.out.println("(indOrdQty!=quantityStduom)@NOT EQUAL");
						sql=null;
						sql = " update indent set status='O' ,ord_qty="+indOrdQty+" ,status_date=? where ind_no=? ";
					}
					
					pstmt = conn.prepareStatement(sql);
					pstmt.setTimestamp(1, sysDate1);
					pstmt.setString(2, indNo);
					
					updCnt=pstmt.executeUpdate();
		      	     System.out.println("Indent Updated"+updCnt);	
					//rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					sql=null;
					
					}
					
					}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
				sql = " update porder set status='O' ,status_reason=?  where  purc_order=? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, statusReason);
				pstmt.setString(2, purcOrder);
				//pstmt.setString(2, purcOrder);
				updCnt=pstmt.executeUpdate();
	      	    System.out.println("porder updated"+updCnt); 	
				//rs.close();
				//rs = null;
				pstmt.close();
				pstmt = null;
				//}
				
				
			}
			
			try {
				
				//Timestamp sysDate1 = null;
				
				System.out.println("TranID@"+tranId);
				sql = " update porder_stat_chg set CONFIRMED='C' ,conf_date=?  where  tran_id=? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, sysDate1);
				pstmt.setString(2, tranId);
				//pstmt.setString(2, purcOrder);
				updCnt=pstmt.executeUpdate();
	      	    System.out.println("porder_stat_chg updated"+updCnt); 	
				//rs.close();
				//rs = null;
				pstmt.close();
				pstmt = null;
				
				if(updCnt>0){
				System.out.println("Comming into Success messages");
				errString1 = itmDBAccessEJB.getErrorString(" ", "VTTRNCNF", userId,"",conn);
				return errString1;
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				if(errString1==null)
				{System.out.println("got errString exception"+e.getMessage());
					conn.rollback();
					conn.close();
				}
				
			}
			
		} catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Unable to Confirmed@@");
			throw new ITMException(e);
		}
		
		finally
		{
			try
			{
				System.out.println( ">>>>>>>>>>>>>In finaly errString:"+errString);
				if (errString != null && errString.trim().length() > 0 && !("d".equalsIgnoreCase(errString)))
				{
					
					conn.rollback();
					System.out.println("Transaction rollback... ");
					conn.close();
					conn = null;
				}
				else
				{
					System.out.println(" Confirmed Success@@");
					conn.commit(); // test
					System.out.println("@@@@ Transaction commit... ");
					conn.close();
					conn = null;
					
				}
			} 
			catch (Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		System.out.println("errString[" + errString + "]:::::::::::errCode[" + errCode + "]");
		return errString;
	}
	
}
