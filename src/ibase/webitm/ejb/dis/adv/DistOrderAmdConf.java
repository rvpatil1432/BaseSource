/**
 For DB2 database condition added in sql statement - by ritesh on 03/dec/13 for supreme.
 */
package ibase.webitm.ejb.dis.adv;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.rmi.RemoteException;
import javax.ejb.SessionContext;
import javax.ejb.CreateException;
//import javax.ejb.SessionBean;// commented for ejb3
import ibase.system.config.ConnDriver;
import ibase.webitm.utility.ITMException;
import ibase.webitm.utility.TransIDGenerator;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.utility.*;
import ibase.webitm.ejb.ActionHandlerEJB;
import org.w3c.dom.Document;
import ibase.system.config.*;
import ibase.utility.CommonConstants;
import java.util.HashMap;
import ibase.webitm.ejb.sys.UtilMethods;
import ibase.webitm.ejb.mfg.InvDemSuppTraceBean;
import java.util.Properties;

import ibase.webitm.ejb.sys.GenerateEDI;
//import ibase.webitm.ejb.sys.GenerateEDIHome;//commented for ejb3
import ibase.webitm.ejb.sys.GenerateEDILocal;//added for ejb3
import javax.ejb.Stateless;//added for ejb3

@Stateless//added for ejb3
public class DistOrderAmdConf extends ActionHandlerEJB implements DistOrderAmdConfLocal , DistOrderAmdConfRemote //SessionBean //commented for ejb3
{
    /*public void ejbCreate() throws RemoteException,CreateException{
    }
    public void ejbRemove(){

    }
    public void ejbActivate(){

    }
    public void ejbPassivate(){

    }
    public void setSessionContext(SessionContext se){

    }*/

    public String confirm(String tranId,String xtraParams, String forcedFlag) throws RemoteException,ITMException
    {
        Connection conn = null;
        PreparedStatement pstmt = null, pstmt1 = null;
        ResultSet rs = null;
		ResultSet rs1 = null;
        String sql = "";
        ConnDriver connDriver = null;
		String loginEmpCode = null;
        ibase.utility.E12GenericUtility genericUtility= null;
		Document dom = null;
		String errCode = null;
		int count=0;
		ITMDBAccessEJB itmDBAccessEJB = null;
		String errString = null, ediOption = "0";
		int upd = 0;
		/**Added by Pavan Rane 24dec19 start[to update demand/supply in summary table(RunMRP process) related changes]*/
		Timestamp shipDate = null;
		int leadTime = 0;
		String siteCodeShip = "", siteCodeDlv = "";
		SimpleDateFormat sdf1 = null;
		Timestamp chgDate = null;
		/**Added by Pavan Rane 24dec19 end[to update demand/supply in summary table(RunMRP process) related changes]*/
        try
		{
            itmDBAccessEJB = new ITMDBAccessEJB();
			genericUtility = new ibase.utility.E12GenericUtility();
            connDriver = new ConnDriver();
            //Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
            conn.setAutoCommit(false);
            sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
            chgDate = java.sql.Timestamp.valueOf(sdf1.format(new java.util.Date()).toString() + " 00:00:00.0");
            InvDemSuppTraceBean invDemSupTrcBean = new InvDemSuppTraceBean();
		    HashMap demandSupplyMap = new HashMap();
			loginEmpCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			//check if there is record in detail
			int countDet = 0;
			if( errString == null || errString.trim().length() == 0 )
			{
				sql = " select count( 1 ) cnt from distordamd where amd_no = ? AND CONFIRMED = 'Y' ";
				if ("db2".equalsIgnoreCase(CommonConstants.DB_NAME ) ||  "mysql".equalsIgnoreCase(CommonConstants.DB_NAME ))
				{
					sql = sql + " for update ";
				}System.out.println("executing Sql "+sql);
				pstmt = conn.prepareStatement( sql );
				pstmt.setString(1,tranId.trim());
				rs = pstmt.executeQuery();
				
				if( rs.next() )
				{
					countDet = rs.getInt( "cnt" );
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;				
								
				if( countDet > 0 )
				{
					errString = itmDBAccessEJB.getErrorString("","VTCONFMD","","",conn);
				}
			}
			if( errString == null || errString.trim().length() == 0 )
			{
				String	distOrder = null ;
				String	purcOrder = null ;
				String	remark = null ;
				String	remarks1 = null ;
				String	remarks2 = null ;
				String	transMode = null ;
				String	totAmt = null ;
				String	netAmt = null ;
				String	taxAmt = null ;
											
				sql = " select dist_order, "
					+"	purc_order, "
					+"	tot_amt, "
					+"	net_amt, "
					+"	tax_amt, "
					+"	remarks, "
					+"	remarks1, "
					+"	remarks2, "
					+"	trans_mode "
					+"	from "
					+"	distordamd "
					+"	where "
					+"	amd_no = ? ";
				if ("db2".equalsIgnoreCase(CommonConstants.DB_NAME ) ||  "mysql".equalsIgnoreCase(CommonConstants.DB_NAME ))
				{
					sql = sql + " for update ";
				}System.out.println("executing Sql "+sql);
					pstmt = conn.prepareStatement( sql );
					pstmt.setString(1,tranId.trim());
					rs = pstmt.executeQuery();
					if( rs.next() )
					{
						distOrder = rs.getString( "dist_order" );
						purcOrder = rs.getString( "purc_order" ) == null ? "" : rs.getString( "purc_order" );
						remark = rs.getString( "remarks" ) == null ? "" : rs.getString( "remarks" );
						remarks1 = rs.getString( "remarks1" ) == null ? "" : rs.getString( "remarks1" );
						remarks2 = rs.getString( "remarks2" ) == null ? "" : rs.getString( "remarks2" );
						transMode = rs.getString( "trans_mode" ) == null ? "" : rs.getString( "trans_mode" );
						netAmt = rs.getString( "net_amt" ) == null ? "" : rs.getString( "net_amt" );
						taxAmt = rs.getString( "tax_amt" ) == null ? "" : rs.getString( "tax_amt" );
						totAmt = rs.getString( "tot_amt" ) == null ? "" : rs.getString( "tot_amt" );
					}
					rs.close();
					rs = null;	
					pstmt.close();
					pstmt = null;	
					
					sql = " select count( 1 ) cnt from distorder where dist_order = ? AND CONFIRMED = 'Y' ";
					if ("db2".equalsIgnoreCase(CommonConstants.DB_NAME ) ||  "mysql".equalsIgnoreCase(CommonConstants.DB_NAME ))
					{
						sql = sql + " for update ";
					}System.out.println("executing Sql "+sql);
					pstmt = conn.prepareStatement( sql );
					pstmt.setString(1,distOrder.trim());
					rs = pstmt.executeQuery();
					
					if( rs.next() )
					{
						countDet = rs.getInt( "cnt" );
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;				
										
					if( countDet == 0 )
					{
						errString = itmDBAccessEJB.getErrorString("","NVCONFIRM","","",conn);
					}
					else
					{
						sql = " update distorder set "
							+"		purc_order = ?, "
							+"		remarks1 = ?, "
							+"		remarks = ?, "
							+"		remarks2 = ?, "
							+"		net_amt = ?, "
							+"		tot_amt = ?, "
							+"		tax_amt = ?, "
							+"		trans_mode = ?, "
							+"		emp_code__aprv = ? "
							+"	where dist_order = ? ";
						
						pstmt = conn.prepareStatement( sql );
						
						pstmt.setString(1,purcOrder.trim());
						pstmt.setString(2,remarks1.trim());
						pstmt.setString(3,remark.trim());
						pstmt.setString(4,remarks2.trim());
						pstmt.setString(5,netAmt.trim());
						pstmt.setString(6,totAmt.trim());
						pstmt.setString(7,taxAmt.trim());
						pstmt.setString(8,transMode.trim());
						pstmt.setString(9,loginEmpCode.trim());
						pstmt.setString(10,distOrder.trim());
											
						pstmt.executeUpdate();
						pstmt.close();
						pstmt = null;

						String distOrderamd = null;
						String lineNoDistord = null;
						String qtyOrder = null;
						String rate = null;
						String rateClg = null;
						String remarks = null;
						String packInstr = null;
						String taxClass = null;
						String taxChap = null;
						String taxEnv = null;
						String itemCode = null;
						String custSpecNo = null;
						String	totAmtDet = null ;
						String	netAmtDet = null ;
						String	taxAmtDet = null ;
						String  status = null;   // added by ritesh on 20/dec/13 for DI3HSUP004
						String siteCode = "";
						Timestamp dueDate = null;
						double qtyOrderOld = 0.0;
						
						
						sql = " select hdr.dist_order, dtl.line_no_distord, dtl.qty_order, dtl.rate, "
							+"	dtl.rate__clg,dtl.remarks, dtl.pack_instr,dtl.cust_spec__no, "
							+"	dtl.tot_amt, dtl.net_amt,dtl.tax_amt, "
							+"	dtl.tax_class, dtl.tax_chap, dtl.tax_env "
							+"  ,dtl.status "      // added by ritesh on 20/dec/13 FOR DI3HSUP004
							/**Modified by Pavan Rane 24dec19 start[fetched extra columns to update with demand/supply in summary table(RunMRP process) related changes]*/
							+", hdr.site_code, dtl.item_code, case when dtl.due_date is null then dtl.due_date_o else dtl.due_date end as due_date"
							+", case when hdr.site_code__ship is null then hdr.site_code__ship_o else hdr.site_code__ship end as site_code__ship"
							+", case when hdr.site_code__dlv is null then hdr.site_code__dlv_o else hdr.site_code__dlv end as site_code__dlv"
							+", case when dtl.qty_order_o  is null then 0 else dtl.qty_order_o  end as qty_order_o "
							/**Modified by Pavan Rane 24dec19 end[fetched extra columns to update with demand/supply in summary table(RunMRP process) related changes]*/
							+"	from distordamd hdr, distordamd_det dtl "
							+"	where  hdr.amd_no = dtl.amd_no "
							+"	and hdr.amd_no = ?"
							+"	and dtl.line_no_distord is not null "
							+"	and ( hdr.dist_order, dtl.line_no_distord ) " 
							+"	in( select dist_order, line_no "
							+"	from distorder_det dist "
							+"	where trim( dist.dist_order) = trim(hdr.dist_order) )";
						/*if ("db2".equalsIgnoreCase(CommonConstants.DB_NAME ) ||  "mysql".equalsIgnoreCase(CommonConstants.DB_NAME ))
						{
							sql = sql + " for update ";
						}*/
						System.out.println("executing Sql "+sql);
						pstmt = conn.prepareStatement( sql );
						pstmt.setString(1,tranId.trim());
						rs = pstmt.executeQuery();
						while( rs.next() )
						{
							distOrderamd = rs.getString( "dist_order" );
							lineNoDistord = rs.getString( "line_no_distord" ) == null ? "" : rs.getString( "line_no_distord" );
							qtyOrder = rs.getString( "qty_order" ) == null ?  "" : rs.getString( "qty_order" );
							rate = rs.getString( "rate" ) == null ? "" : rs.getString( "rate" );
							rateClg = rs.getString( "rate__clg" ) == null ? "" : rs.getString( "rate__clg" );
							remarks = rs.getString( "remarks" ) == null ? "" : rs.getString( "remarks" );
							packInstr = rs.getString( "pack_instr" ) == null ? "" : rs.getString( "pack_instr" );
							taxClass = rs.getString( "tax_class" ) == null ?  "" : rs.getString( "tax_class" );
							taxChap = rs.getString( "tax_chap" ) == null ?  "" : rs.getString( "tax_chap" );
							//taxEnv = rs.getString( "tax_env" ) == null ?  "" : rs.getString( "tax_env" );
							taxEnv = rs.getString( "tax_env" );
							netAmtDet = rs.getString( "net_amt" ) == null ? "" : rs.getString( "net_amt" );
							taxAmtDet = rs.getString( "tax_amt" ) == null ? "" : rs.getString( "tax_amt" );
							totAmtDet = rs.getString( "tot_amt" ) == null ? "" : rs.getString( "tot_amt" );
							custSpecNo = rs.getString( "cust_spec__no" ) == null ?  "" : rs.getString( "cust_spec__no" );
							status = rs.getString( "status" ) == null ?  "" : rs.getString( "status" ); // added by ritesh on 20/dec/13 FOR DI3HSUP004
							/**Modified by Pavan Rane 24dec19 start[fetched extra columns to update with demand/supply in summary table(RunMRP process) related changes]*/
							itemCode = rs.getString("item_code") == null ?  "" : rs.getString("item_code");
							siteCode = rs.getString("site_code") == null ?  "" : rs.getString("site_code");
							dueDate = rs.getTimestamp("due_date");							
							siteCodeShip = rs.getString("site_code__ship") == null ?  "" : rs.getString("site_code__ship");
							siteCodeDlv = rs.getString("site_code__dlv") == null ?  "" : rs.getString("site_code__dlv");
							qtyOrderOld = rs.getDouble("qty_order_o");
							/**Modified by Pavan Rane 24dec19 end[fetched extra columns to update with demand/supply in summary table(RunMRP process) related changes]*/
							sql = "update distorder_det set dist_order = ?, "
								+"	line_no = ?, "
								+"	qty_order = ?, " 
								+"	rate = ?, " 
								+"	rate__clg = ?, " 
								+"	remarks = ?, " 
								+"	pack_instr = ?, " 
								+"	tax_class = ?, " 
								+"	tax_chap = ?, " 
								+"	tax_env = ?, " 
								+"	net_amt = ?, "
								+"	tot_amt = ?, "
								+"	tax_amt = ?, "		
								+"	cust_spec__no = ?, "
								+"  qty_confirm = ? "
								+"  ,status = ? "   // added by ritesh on 20/dec/13 FOR DI3HSUP004
								+"	where dist_order = ? "
								+"	and line_no = ? "	;
							/**Modified by Pavan Rane 24dec19 start[changed same perpared statement refenrece as pstmt to pstmt1 and closed ]*/
							pstmt1 = conn.prepareStatement( sql );
							pstmt1.setString(1,distOrderamd.trim());
							pstmt1.setString(2,lineNoDistord.trim());
							pstmt1.setString(3,qtyOrder.trim());
							pstmt1.setString(4,rate.trim());
							pstmt1.setString(5,rateClg.trim());
							pstmt1.setString(6,remarks.trim());
							pstmt1.setString(7,packInstr.trim());
							pstmt1.setString(8,taxClass.trim());
							pstmt1.setString(9,taxChap.trim());
							pstmt1.setString(10,taxEnv);
							pstmt1.setString(11,netAmtDet.trim());
							pstmt1.setString(12,totAmtDet.trim());
							pstmt1.setString(13,taxAmtDet.trim());
							pstmt1.setString(14,custSpecNo.trim());
							pstmt1.setString(15,qtyOrder.trim());
							pstmt1.setString(16,status.trim());  // added by ritesh on 20/dec/13 FOR DI3HSUP004
							pstmt1.setString(17,distOrderamd.trim());
							pstmt1.setString(18,lineNoDistord.trim());
							int updCnt = pstmt1.executeUpdate();
							pstmt1.close();
							pstmt1 = null;
							/**Modified by Pavan Rane 24dec19 end[changed same perpared statement refenrece as pstmt to pstmt1 and closed ]*/
							/**Added by Pavan Rane 24dec19 start[to update demand/supply in summary table(RunMRP process) related changes]*/
							if(updCnt > 0)
							{
							    demandSupplyMap.put("site_code", siteCodeShip);
								demandSupplyMap.put("item_code", itemCode);		
								demandSupplyMap.put("ref_ser", "D-ORDI");
								demandSupplyMap.put("ref_id", distOrderamd);
								demandSupplyMap.put("ref_line", lineNoDistord);
								demandSupplyMap.put("due_date", dueDate);		
								demandSupplyMap.put("demand_qty", Double.parseDouble(qtyOrder)-qtyOrderOld);
								demandSupplyMap.put("supply_qty", 0.0);
								demandSupplyMap.put("change_type", "C");
								demandSupplyMap.put("chg_process", "T");
								demandSupplyMap.put("chg_user", genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
							    demandSupplyMap.put("chg_term", genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId"));	
							    errString = invDemSupTrcBean.updateDemandSupply(demandSupplyMap, conn);
							    demandSupplyMap.clear();
							    System.out.println("errString D-ORDI ["+errString+"]");
							    if(errString != null && errString.trim().length() > 0)
							    {
							    	System.out.println("errString["+errString+"]");
					                return errString;
							    }							    							   
							    demandSupplyMap.put("site_code", siteCodeDlv);
								demandSupplyMap.put("item_code", itemCode);		
								demandSupplyMap.put("ref_ser", "D-ORDR");
								demandSupplyMap.put("ref_id", distOrderamd);
								demandSupplyMap.put("ref_line", lineNoDistord);
								demandSupplyMap.put("due_date", dueDate);		
								demandSupplyMap.put("demand_qty", 0.0);
								demandSupplyMap.put("supply_qty", Double.parseDouble(qtyOrder)-qtyOrderOld);
								demandSupplyMap.put("change_type", "C");
								demandSupplyMap.put("chg_process", "T");
								demandSupplyMap.put("chg_user", genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
							    demandSupplyMap.put("chg_term", genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId"));	
							    errString = invDemSupTrcBean.updateDemandSupply(demandSupplyMap, conn);
							    demandSupplyMap.clear();
							    System.out.println("errString D-ORD ["+errString+"]");
							    if(errString != null && errString.trim().length() > 0)
							    {
							    	System.out.println("errString["+errString+"]");
					                return errString;
							    }							  
							}	
							/**Modified by Pavan Rane 24dec19 end[to update demand/supply in summary table(RunMRP process) related changes]*/						
						}	
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;				
						
						sql = " select hdr.dist_order, dtl.line_no_distord, dtl.qty_order, dtl.rate, dtl.item_code , "
							+"	dtl.rate__clg,dtl.remarks, dtl.pack_instr, dtl.tax_class, dtl.tax_chap, dtl.tax_env "
							/**Modified by Pavan Rane 24dec19 start[fetched extra columns to update with demand/supply in summary table(RunMRP process) related changes]*/	
							+", case when hdr.ship_date_o is null then (case when dtl.ship_date is null then dtl.due_date_o else dtl.ship_date end) else hdr.ship_date_o end as ship_date "							
							+ ", case when hdr.site_code__ship is null then hdr.site_code__ship_o else hdr.site_code__ship end as site_code__ship"
							+ ", case when hdr.site_code__dlv is null then hdr.site_code__dlv_o else hdr.site_code__dlv end as site_code__dlv"							
							/**Modified by Pavan Rane 24dec19 end[fetched extra columns to update with demand/supply in summary table(RunMRP process) related changes]*/
							+"	from distordamd hdr, distordamd_det dtl "
							+"	where  hdr.amd_no = dtl.amd_no "
							+"	and hdr.amd_no = ? "
							+"	and dtl.line_no_distord is null "
							+"	and ( hdr.dist_order ) " 
							+"	in( select dist_order "
							+"	from distorder_det dist "
							+"	where trim( dist.dist_order) = trim(hdr.dist_order) )";
					/*	if ("db2".equalsIgnoreCase(CommonConstants.DB_NAME ) ||  "mysql".equalsIgnoreCase(CommonConstants.DB_NAME ))
						{
							sql = sql + " for update ";
						}
						*/
						System.out.println("executing Sql "+sql);
						pstmt = conn.prepareStatement( sql );
						pstmt.setString(1,tranId.trim());
						rs = pstmt.executeQuery();
						
						while( rs.next() )
						{
							distOrder = rs.getString( "dist_order" );
							itemCode = rs.getString( "item_code" );
							qtyOrder = rs.getString( "qty_order" ) == null ? "" :  rs.getString( "qty_order" );
							rate = rs.getString( "rate" ) == null ? "" : rs.getString( "rate" );
							rateClg = rs.getString( "rate__clg" ) == null ? "" : rs.getString( "rate__clg" );
							remarks = rs.getString( "remarks" ) == null ? "" : rs.getString( "remarks" );
							packInstr = rs.getString( "pack_instr" ) == null ? "" : rs.getString( "pack_instr" );
							taxClass = rs.getString( "tax_class" ) == null ? "" : rs.getString( "tax_class" );
							taxChap = rs.getString( "tax_chap" ) == null ? "" : rs.getString( "tax_chap" );
							//taxEnv = rs.getString( "tax_env" ) == null ? "" : rs.getString( "tax_env" );
							taxEnv = rs.getString( "tax_env" );
							/**Modified by Pavan Rane 24dec19 start[fetched extra columns to calculate due date and to update with demand/supply in summary table(RunMRP process) related changes]*/
							shipDate = rs.getTimestamp("ship_date");
							siteCodeShip = rs.getString("site_code__ship") == null ? "" : rs.getString("site_code__ship");
							siteCodeDlv = rs.getString("site_code__dlv") == null ? "" : rs.getString("site_code__dlv");
							leadTime = 0;
							String sql2 = "select case when max(case when pur_lead_time is null then 0 else pur_lead_time end) is null then 0  "
										+ " else max(case when pur_lead_time is null then 0 else pur_lead_time end) end "
										+ " from siteitem where site_code = ? ";
            				pstmt1 = conn.prepareStatement(sql2);
            				pstmt1.setString(1, siteCodeDlv);
            				rs1 = pstmt1.executeQuery();
            				if(rs1.next())
            				{
            					leadTime = rs1.getInt(1);
            				}
            				rs1.close();
            				rs1 = null;
            				pstmt1.close();
            				pstmt1 = null;
            				
            				UtilMethods util = new UtilMethods();            				
            				dueDate =  util.RelativeDate(shipDate, leadTime);
							/**Modified by Pavan Rane 24dec19 end[fetched extra columns to calculate due date and to update with demand/supply in summary table(RunMRP process) related changes]*/
							sql = " select (max(line_no) + 1) line_no from distorder_det where dist_order = ? ";
							if ("db2".equalsIgnoreCase(CommonConstants.DB_NAME ) ||  "mysql".equalsIgnoreCase(CommonConstants.DB_NAME ))
							{
								sql = sql + " for update ";
							}System.out.println("executing Sql "+sql);
							/**Modified by Pavan Rane 24dec19 start[to update demand/supply in summary table(RunMRP process) related changes]
							Changed the same preparedstatement used inside loop*/
							/*pstmt = conn.prepareStatement( sql );
							pstmt.setString(1,distOrder.trim());
							rs1 = pstmt.executeQuery();*/
							pstmt1 = conn.prepareStatement( sql );
							pstmt1.setString(1,distOrder.trim());
							rs1 = pstmt1.executeQuery();
							String lineNo = null;
							if ( rs1.next() )
							{
								lineNo = rs1.getString( "line_no" );
							}
							rs1.close();
            				rs1 = null;
            				pstmt1.close();
            				pstmt1 = null;
							sql = "insert into distorder_det ( dist_order,line_no,item_code,qty_order,rate,rate_clg,remarks,pack_instr,tax_class,tax_chap,tax_env,qty_confirm, due_date ) "
								+"	values ( ?,?,?,?,?,?,?,?,?,?,?,?,?) ";
							//Pavan Rane start [to add DEMAND/SUPPLY in INV_DEM_SUPP for MRP]pstmt changed to pstmt1 
							pstmt1 = conn.prepareStatement( sql );
							pstmt1.setString(1,distOrder.trim());
							pstmt1.setInt(2,Integer.parseInt(lineNo.trim()));
							pstmt1.setString(3,itemCode.trim());
							pstmt1.setString(4,qtyOrder.trim());
							pstmt1.setString(5,rate.trim());
							pstmt1.setString(6,rateClg.trim());
							pstmt1.setString(7,remarks.trim());
							pstmt1.setString(8,packInstr.trim());
							pstmt1.setString(9,taxClass.trim());
							pstmt1.setString(10,taxChap.trim());
							pstmt1.setString(11,taxEnv);
							pstmt1.setString(12,qtyOrder.trim());
							pstmt1.setTimestamp(13,dueDate); //Pavan Rane [to add DEMAND/SUPPLY in INV_DEM_SUPP for MRP]
							int updCnt = pstmt1.executeUpdate();
							//rs1.close();
							//rs1 = null;
							pstmt1.close();
            				pstmt1 = null;
							if(updCnt > 0)
							{
								demandSupplyMap.put("site_code", siteCodeShip);
								demandSupplyMap.put("item_code", itemCode);		
								demandSupplyMap.put("ref_ser", "D-ORDI");
								demandSupplyMap.put("ref_id", distOrder);
								demandSupplyMap.put("ref_line", lineNo);
								demandSupplyMap.put("due_date", chgDate);		
								demandSupplyMap.put("demand_qty", Double.parseDouble(qtyOrder));
								demandSupplyMap.put("supply_qty", 0.0);
								demandSupplyMap.put("change_type", "A");
								demandSupplyMap.put("chg_process", "T");
								demandSupplyMap.put("chg_user", genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
							    demandSupplyMap.put("chg_term", genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId"));	
							    errString = invDemSupTrcBean.updateDemandSupply(demandSupplyMap, conn);
							    //demandSupplyMap.clear();
							    System.out.println("errString D-ORDI ["+errString+"]");
							    if(errString != null && errString.trim().length() > 0)
							    {
							    	System.out.println("errString ["+errString+"]");
					                return errString;
							    }
							    demandSupplyMap.put("site_code", siteCodeShip);
								demandSupplyMap.put("item_code", itemCode);		
								demandSupplyMap.put("ref_ser", "D-ORDR");
								demandSupplyMap.put("ref_id", distOrder);
								demandSupplyMap.put("ref_line", lineNo);
								demandSupplyMap.put("due_date", dueDate);		
								demandSupplyMap.put("demand_qty", 0.0);
								demandSupplyMap.put("supply_qty", Double.parseDouble(qtyOrder));
								demandSupplyMap.put("change_type", "A");
								demandSupplyMap.put("chg_process", "T");
								demandSupplyMap.put("chg_user", genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
							    demandSupplyMap.put("chg_term", genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId"));	
							    errString = invDemSupTrcBean.updateDemandSupply(demandSupplyMap, conn);
							    //demandSupplyMap.clear();
							    System.out.println("errString D-ORDR ["+errString+"]");
							    if(errString != null && errString.trim().length() > 0)
							    {
							    	System.out.println("errString ["+errString+"]");
					                return errString;
							    }							    							   
							}
							/**Modified by Pavan Rane 24dec19 end[to update demand/supply in summary table(RunMRP process) related changes]*/ 
						}
						rs.close();
						rs = null;		
						pstmt.close();
						pstmt = null;				
																		
						sql = "update distordamd set conf_date = ?,confirmed = 'Y' where amd_no = ?";
						pstmt = conn.prepareStatement( sql );
					    pstmt.setTimestamp( 1, new java.sql.Timestamp( System.currentTimeMillis() ) );
						pstmt.setString(2,tranId.trim());
						pstmt.executeUpdate();
						pstmt.close();
						pstmt = null;				
					}
			}
			//call edi generation
			if( errString == null || errString.trim().length() == 0 )
			{
				// 28/05/14 manoharan
				sql = " select edi_option from transetup where tran_window = 'w_distordamd' ";
				pstmt = conn.prepareStatement( sql );
				rs = pstmt.executeQuery();
				
				if ( rs.next() )
				{
					ediOption = rs.getString(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if (ediOption != null && ("1".equals(ediOption) || "2".equals(ediOption)) )
				{
					errCode = null;
					GenerateEDI genEDIRemote = new GenerateEDI();
					//GenerateEDI genEDIRemote = (GenerateEDI)genEDIHome.create();//commented for ejb3
					
					//errCode = genEDIRemote.genEDI( "w_distordamd", tranId, "E", xtraParams, conn );
					//Method changed by manoj dtd 18/12/2013 as per no. of arguments used
					errCode = genEDIRemote.genEDI( "w_distordamd", tranId,  xtraParams, conn );
					if( "SUCCESS".equalsIgnoreCase( errCode ) )
					{
						errCode = null;
					}
					genEDIRemote = null;
				}
				
			}
			if( errCode != null && errCode.trim().length() > 0 )
			{
				errString = itmDBAccessEJB.getErrorString("", errCode, "", "", conn );
			}
			//end calling edi generation
			
			if( errString != null && errString.trim().length() > 0 )
			{
				
				conn.rollback();
				return errString;
			}
			else
			{
				conn.commit();
				errString = itmDBAccessEJB.getErrorString("","VTCNFSUCC","");
			}
		}
		catch(ITMException ie)
		{
			try
			{
				conn.rollback();
			}
			catch(Exception t)
			{
				t.printStackTrace();
				throw new ITMException (t);
			}
			ie.printStackTrace();
			errString = itmDBAccessEJB.getErrorString("","VTDESNCONF","","",conn);
			return errString;
		}
		catch(Exception e)
		{
			try
			{
				conn.rollback();
			}
			catch(Exception t)
			{
				t.printStackTrace();
				throw new ITMException ( t );
			}
			e.printStackTrace();
			errString = itmDBAccessEJB.getErrorString("","VTDESNCONF","","",conn);
			return errString;
		}
		finally
		{
			try{
				if( pstmt != null )
				{
					pstmt.close();
				}
				pstmt = null;
				if(conn != null)
				{
					conn.close();
				}
				conn = null;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new ITMException ( e );
			}
		}
		return errString;
	}
}