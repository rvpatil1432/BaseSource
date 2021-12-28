/********************************************************
	Title : PorderConf[W16CKAT004]
	Date  : 22/06/16
	Developer: Poonam Gole

 ********************************************************/
package ibase.webitm.ejb.dis.adv;

import ibase.system.config.AppConnectParm;
import ibase.system.config.ConnDriver;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.E12CreateBatchLoadEjb;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.MasterStatefulLocal;

import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.mfg.InvDemSuppTraceBean;
import ibase.webitm.ejb.sys.CreateRCPXML;
import ibase.webitm.ejb.sys.UtilMethods;
import ibase.webitm.utility.ITMException;
import ibase.webitm.utility.TransIDGenerator;

import java.io.File;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.naming.InitialContext;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

@Stateless
public class PorderConf extends ActionHandlerEJB implements PorderConfLocal, PorderConfRemote
{

	E12GenericUtility genericUtility= new  E12GenericUtility();
	FinCommon finCommon = new FinCommon();
	DistCommon distCommon= new DistCommon();
	ValidatorEJB validatorEJB = new ValidatorEJB();
	UtilMethods utilMethods = new UtilMethods();
	Connection connCP = null;
	private boolean ordFlag = false;
	private boolean isWorkflow = false;
	public String confirm(String pOrder, String xtraParams, String forcedFlag)throws RemoteException, ITMException
	{

		String retString = "";
		Connection conn = null;
		try
		{
			retString = this.confirm(pOrder, xtraParams, forcedFlag, conn);
			System.out.println("retString:::::"+retString);
		}
		catch(Exception e)
		{
			System.out.println("Exception in [IndentReqConf] confirm " + e.getMessage());
			throw new ITMException(e);
		}
		return retString;

	}
	public String confirm(String pOrder, String xtraParams, String forcedFlag ,Connection conn)throws RemoteException, ITMException
	{
		System.out.println(">>>>>>>>>>>>>>>>>>PorderConf CONFIRM called>>>>>>>>>>>>>>>>>>>");
		String sql = "",sql1 = "";
		PreparedStatement pstmt = null,pstmt1=null;
		String errString = null, userid = "";
		String confirm = "",status= "" ,indNo = "",reason = "" ,pordType = "",ediOption = "",userIdOp="",
				projCode = "",suppCode= "" , pordSite = "";
		String loginEmpCode="";
		double totordqty = 0.0 ,amount = 0.0 ,hnetAmt = 0.0,hordAmt = 0.0,ordQty=0.0,indQty=0.0;
		ResultSet rs = null,rs1=null;
		int cnt = 0,count=0;
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		boolean isLocalConnection = false; // 28-feb-2020 manoharan to take care off local commit
		try 
		{
			//ConnDriver connDriver = null;
			//connDriver = new ConnDriver();
			//conn = connDriver.getConnectDB("DriverITM");


			//Changes and Commented By Bhushan on 09-06-2016 :START
            //conn = connDriver.getConnectDB("DriverITM");
            System.out.println("PorderConf CONFIRM called xtraParams[" + xtraParams+ "]");
			if(conn == null) 
			{
				conn = getConnection();
				conn.setAutoCommit(false);
				isLocalConnection = true;
			}
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			System.out.println("PorderConf CONFIRM called xtraParams[" + xtraParams+ "]");

			loginEmpCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");

			if(loginEmpCode == null || loginEmpCode.trim().length() == 0)
			{
				userid = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
				pstmt = conn.prepareStatement("select emp_code from users where code = ?");
				pstmt.setString(1, userid);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					loginEmpCode = checkNull(rs.getString("emp_code"));
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;


			}
			if(loginEmpCode == null || loginEmpCode.trim().length() == 0)
			{
				errString = itmDBAccessLocal.getErrorString("", "EMPAPRV", "","",conn);
				return errString;
			}

			sql = "SELECT count(*) as cnt FROM porddet WHERE purc_order =? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, pOrder);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				cnt = rs.getInt(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			if(cnt <= 0)
			{
				errString = itmDBAccessLocal.getErrorString("", "DS000NR", "","",conn);
				return errString;
			}

			sql = " SELECT PORDDET.PURC_ORDER,PORDDET.LINE_NO,PORDDET.ITEM_CODE,PORDDET.IND_NO,PORDDET.QUANTITY,INDENT.QUANTITY,"+   
					" INDENT.ORD_QTY,PORDDET.OP_REASON,PORDDET.USER_ID__OP,PORDDET.QUANTITY__STDUOM,INDENT.QUANTITY__STDUOM  "+
					" FROM PORDDET, INDENT  " +
					" WHERE ( PORDDET.IND_NO = INDENT.IND_NO (+)) AND  "+
					" ( ( PORDDET.PURC_ORDER = ?) AND ( PORDDET.IND_NO IS NOT NULL ) ) " + 
					" ORDER BY PORDDET.PURC_ORDER ASC,PORDDET.IND_NO ASC, PORDDET.LINE_NO ASC  ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, pOrder);
			rs = pstmt.executeQuery();
			//Changes by Nandkumar Gadkari on 24/04/18----------------------Start-------------
			while(rs.next())
			{	
				count++;
				indNo = checkNull(rs.getString(4));
				//Commented and added by Varsha V on 16-11-18 to change indent quantity
				//indQty = rs.getDouble(10);
				indQty = rs.getDouble(11);
				//Ended by Varsha V on 16-11-18 to change indent quantity
				ordQty = rs.getDouble(7);
				reason = rs.getString(8);
				userIdOp = rs.getString(9);
				pstmt1 = conn.prepareStatement("select edi_option from transetup where tran_window = 'w_porder'");
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{
					ediOption = rs1.getString(1);
				}
				rs1.close();rs1 = null;
				pstmt1.close();pstmt1 = null;

				sql1 = " select case when (sum(case when porddet.quantity__stduom is null then 0 else porddet.quantity__stduom end ))" +
						" is null then 0 else (sum(case when porddet.quantity__stduom is null then 0 else porddet.quantity__stduom end ))" +
						" end  as qty from porddet where ( porddet.ind_no = ?) and ( porddet.purc_order = ?)";
				pstmt1 = conn.prepareStatement(sql1);
				pstmt1.setString(1, indNo);
				pstmt1.setString(2, pOrder);
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{
					totordqty = rs1.getDouble(1);
				}
				rs1.close();rs1 = null;
				pstmt1.close();pstmt1 = null;
				System.out.println("totordqty>>>>"+totordqty);

				if ((totordqty + ordQty) > indQty && (( reason== null) ||  reason.trim().length() == 0))
				{
					continue;
				}
				else
				{
					count--;
				}

			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;
			if(count > 0 ) {
				errString = itmDBAccessLocal.getErrorString("", "VTPORESN1", "","",conn);
				return errString;

			}
			//Changes by Nandkumar Gadkari on 24/04/18----------------------end-------------			
            //Commented by monika to at pord_type on 04 aug 2020
			//sql= "select tot_amt, ord_amt from porder where purc_order= ? ";    // added By Vrushabh on 11-3-2020 for displaying total amount and order amount from purchase order start
            sql= "select tot_amt, ord_amt,pord_type from porder where purc_order= ? ";    // added By Monika salla on 04-08-2020 to get Pordtype

            pstmt1 = conn.prepareStatement(sql);
			pstmt1.setString(1, pOrder);
			rs1 = pstmt1.executeQuery();
			if(rs1.next())
			{
				hnetAmt = rs1.getDouble(1);
                hordAmt=  rs1.getDouble(2); 
                pordType=rs1.getString("pord_type");
			}
			rs1.close();rs1 = null;
			pstmt1.close();pstmt1 = null;
            System.out.println("purc Order amount is" +pordType+" INSIDE THE DISTORDER CREATION " );  //ADDED BY MONIKA SALLA    
			System.out.println("Total amount is" +  hnetAmt );
			System.out.println("Order amount is" +  hordAmt );   // added By Vrushabh on 11-3-2020 for displaying total amount and order amount from purchase order End
			if(errString == null || errString.trim().length() == 0 )
			{
				errString = gbfRetrieveOrder(pOrder,pordType ,ediOption,projCode,amount,hnetAmt ,hordAmt,suppCode ,pordSite ,xtraParams,conn);
			}

			System.out.println("errString  in gbfRetrieveOrder"+ errString);
			if (errString != null && errString.trim().length() > 0)
			{
				conn.rollback();
				return errString;
			} 
			else 
			{
				conn.commit();

				if(errString == null || errString.trim().length() == 0) 
				{
					System.out.println("Inside gbfPorderAdvance....."+errString);
					errString = gbfPorderAdvance(pOrder,0,"PO",xtraParams,conn); // xtraParams ADDED BY NANDKUMAR GADKARI ON 11/03/19
				}
				System.out.println(" gbfPorderAdvance errString....."+errString);
				if (errString != null && errString.trim().length() > 0)
				{
					conn.rollback();
					return errString;
				} 
			}


		}
		catch (Exception e) 
		{
			if(conn!=null)
			{
				try {
					conn.rollback();
				} catch (SQLException ex) {

					e.printStackTrace();
					throw new ITMException(e);
				}
			}
			e.printStackTrace();
			throw new ITMException(e);
		} 
		finally
		{
			try
			{
				System.out.println( ">>>>>>>>>>>>>In finaly errString:"+errString);
				if (errString != null && errString.trim().length() > 0 ) //&& !("d".equalsIgnoreCase(errString)))
				{
					if (isLocalConnection)// 28-feb-2020 manoharan to consider local connection (isLocalConnection)
					{
                        conn.rollback();
                        conn.close();//added by monika on 4 08 2020-if connection null no need to close it outside.
					    conn = null;
					}
					System.out.println("Transaction rollback... ");
					//conn.close();
					//conn = null;

					if(connCP!= null)
					{
						connCP.rollback();
						System.out.println("Transaction rollback... ");
						connCP.close();
						connCP = null;
					}
				}
				else
				{	
					if (isLocalConnection)// 28-feb-2020 manoharan to consider local connection (isLocalConnection)
					{
						conn.commit();
                        errString = itmDBAccessLocal.getErrorString("", "VTCNFSUCC", "","",conn);
                        System.out.println("@@@@ Transaction commit... ");//added by monika on 4 august 2020 to resolve connection issue.
					    conn.close();
					    conn = null;
					}
					/*System.out.println("@@@@ Transaction commit... ");//commented by monika to resolve commection issue.
					conn.close();
					conn = null;*/

					if(connCP!= null)
					{
						connCP.commit();
						System.out.println("T@@@@ Transactiock... ");
						connCP.close();
						connCP = null;
					}
				}
			} 
			catch (Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		System.out.println("errString[" + errString +"]");
		if(isWorkflow)//Added by AMOL
		{
			return "1";	
		}
		else
		{
			return errString;
		}
	}
	//Added by AMOL S on 21-11-18 For Workflow Execution [START]
	public String confirm(String pOrder, String xtraParams, String forcedFlag,String userInfoStr)throws RemoteException, ITMException
	{
		isWorkflow = true;
		System.out.println("#### Calling Through Workflow PorderConf :: confirm ...");
		String retString = "";
		Connection conn = null;
		ConnDriver connDriver = new ConnDriver();
		try
		{
			UserInfoBean userInfo = new UserInfoBean(userInfoStr);
			String transDB = userInfo.getTransDB();
			System.out.println("#### TransDB connection in : "+transDB);

			if (transDB != null && transDB.trim().length() > 0)
			{
				conn = connDriver.getConnectDB(transDB);
			}
			else
			{
				conn = connDriver.getConnectDB("DriverITM");
			}
			conn.setAutoCommit(false);
			connDriver = null;

			retString = this.confirm(pOrder, xtraParams, forcedFlag, conn);
			isWorkflow = false;
			System.out.println("retString:::::"+retString);
		}
		catch(Exception e)
		{
			System.out.println("Exception in [IndentReqConf] confirm " + e.getMessage());
			retString = "ERROR";
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println( ">>>>>>>>>>>>>In finaly errString:"+retString);
				if (retString != null && retString.trim().length() > 0 && !("d".equalsIgnoreCase(retString)))
				{
					// 28-feb-2020 manoharan to consider local connection (isLocalConnection)
					conn.rollback();
					System.out.println("Transaction rollback... ");
					conn.close();
					conn = null;
				}
				else
				{	
					conn.commit();
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
		return retString;

	}
	// Added by AMOL [END]
	private String gbfRetrieveOrder(String pOrder,String pordType ,String ediOption ,String projCode ,double amount ,double hnetAmt ,double hordAmt ,String suppCode ,String pordSite,String xtraParams, Connection conn) throws ITMException 
	{
		System.out.println("Inside gbfRetrieveOrder.............");
		String sql = "" ,sql1 = "",confirmed = "",validateProjCost = "",subcontractType = "",dataStr= "",retString = "",
				channelPartner = "", disLink = "", sordSite = "" ,jobWorkType ="" ;
		PreparedStatement pstmt = null,pstmt1=null;
		String errString ="";
		ResultSet rs = null , rs1 = null;
		int cnt=0,updCnt=0 ,cnt1=0 ,saleOrderCnt = 0;
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		double approxCost=0.0;
		double ordPer = 0.0 ,netPer = 0.0 ,fixed = 0.0;
		String loginEmpCode="";
		java.sql.Timestamp currDate = null;
		currDate = new java.sql.Timestamp(System.currentTimeMillis()) ;
		boolean isError = false ;
		//added by nandkumar gadkari on 08/03/19
		String indnoHdr = "",indNo = "",unitStd = "", enqNo = "", userid = "";
		PreparedStatement pstmt2=null;
		double totAmtProj=0.0,oqty = 0.0 ,quantity = 0.0 , ordqty = 0.0 ,totOrdQty = 0.0;
		Timestamp reqDate = null;
		String itemCode = "";
		String siteCode = "";
		try 
		{
			loginEmpCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			InvDemSuppTraceBean invDemSupTrcBean = new InvDemSuppTraceBean();
			HashMap demandSupplyMap = new HashMap();
			if(loginEmpCode == null || loginEmpCode.trim().length() == 0)
			{
				userid = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
				pstmt = conn.prepareStatement("select emp_code from users where code = ?");
				pstmt.setString(1, userid);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					loginEmpCode = checkNull(rs.getString("emp_code"));
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
			}
			System.out.println("pordType>>"+pordType +"confirmed"+confirmed);
			sql = "Select count(distinct acct_code__cr)  ,count(distinct cctr_code__cr) from porddet where purc_order = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, pOrder);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				cnt = rs.getInt(1);
				cnt1 = rs.getInt(2);
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;

			if(cnt > 1)
			{
				errString = itmDBAccessLocal.getErrorString("", "VTACCTCODE", "","",conn);
				return errString;
			}
			if(cnt1 > 1)
			{
				errString = itmDBAccessLocal.getErrorString("", "VTCCTRCODE", "","",conn);
				return errString;
			}

			validateProjCost = distCommon.getDisparams("999999","VALIDATE_PROJ_COST", conn);
			System.out.println("validateProjCost>>"+validateProjCost);
			if("Y".equalsIgnoreCase(validateProjCost))
			{
				if(projCode != null && projCode.trim().length() > 0)
				{
					sql = "select approx_cost from project where proj_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, projCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						approxCost = rs.getDouble(1);
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;

					if(amount > approxCost)
					{
						errString = itmDBAccessLocal.getErrorString("", "VTPROJCOST", "","",conn);
						return errString;
					}
				}

				sql = "select a.proj_code, b.approx_cost, sum(a.tot_amt* c.exch_rate)  from porder c,porddet a, project b" +
						" where c.purc_order = a.purc_order and a.proj_code = b.proj_code and a.purc_order = ? " +
						" and a.proj_code is not null group by a.proj_code,b.approx_cost";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, pOrder);
				rs = pstmt.executeQuery();
				while(rs.next())
				{
					projCode = rs.getString(1);
					approxCost = rs.getDouble(2);
					amount = rs.getDouble(3);

					if(amount > approxCost)
					{
						errString = itmDBAccessLocal.getErrorString("", "VTPROJCOST", "","",conn);
						return errString;
					}
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
			}

			if(! "P".equalsIgnoreCase(pordType))
			{
				errString = gbfConfirmOrder(pOrder,approxCost,xtraParams,conn);

				if(errString != null && errString.trim().length() > 0)
				{
					return errString ;
				}

			}
			/**Modified by Pavan Rane 24dec19 start[to update demand/supply in summary table(RunMRP process) related changes]*/
			sql = "select site_code, line_no, item_code, quantity__stduom, dlv_date from porddet where purc_order = ?" ;
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, pOrder);
			rs = pstmt.executeQuery();
			while(rs.next())
			{		
				demandSupplyMap.put("site_code", rs.getString("site_code"));
				demandSupplyMap.put("item_code", rs.getString("item_code"));		
				demandSupplyMap.put("ref_ser", "P-ORD");
				demandSupplyMap.put("ref_id", pOrder);
				demandSupplyMap.put("ref_line", rs.getString("line_no"));
				demandSupplyMap.put("due_date", rs.getTimestamp("dlv_date"));		
				demandSupplyMap.put("demand_qty", 0.0);
				demandSupplyMap.put("supply_qty", rs.getDouble("quantity__stduom"));
				demandSupplyMap.put("change_type", "A");
				demandSupplyMap.put("chg_process", "T");
				demandSupplyMap.put("chg_user", genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
				demandSupplyMap.put("chg_term", genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId"));			    
				errString = invDemSupTrcBean.updateDemandSupply(demandSupplyMap, conn);
				demandSupplyMap.clear();
				if(errString != null && errString.trim().length() > 0)
				{
					System.out.println("errString["+errString+"]");
					return errString;
				}
			}
			/**Modified by Pavan Rane 24dec19 end[to update demand/supply in summary table(RunMRP process) related changes]*/
			if(errString == null || errString.trim().length() == 0)
			{
				sql = " update porder set confirmed = 'Y',conf_date = ?,emp_code__aprv = ? ,status = 'O',status_date = ? " +
						" where purc_order = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, currDate);
				pstmt.setString(2, loginEmpCode);
				pstmt.setTimestamp(3, currDate);
				pstmt.setString(4, pOrder);
				updCnt= pstmt.executeUpdate();
				pstmt.close();
				pstmt = null;
				System.out.println("updCnt11"+updCnt);
				if(updCnt != 1)
				{
					errString = itmDBAccessLocal.getErrorString("", "VTPOUPD", "","",conn);
					return errString;
				}
				/*if(updCnt == 1) // 
				{
					conn.commit(); 
				}*/
			}
			System.out.println("pordType>>["+pordType + "]errString" +errString);
			if((!"P".equalsIgnoreCase(pordType)) && (errString == null || errString.trim().length() == 0))
			{
				System.out.println("INSIDE IF");

				sql = " select case when channel_partner is null then 'N' else channel_partner end , dis_link, site_code__ch" +
						" from site_supplier where site_code = ? and supp_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, pordSite);
				pstmt.setString(2, suppCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					channelPartner = checkNull(rs.getString(1));
					disLink = checkNull(rs.getString(2));
					sordSite =checkNull(rs.getString(3));

					if(channelPartner == null || channelPartner.trim().length() == 0)
					{
						sql1 = "select case when channel_partner is null then 'N' else channel_partner end, dis_link, site_code " +
								"from supplier where supp_code =  ?";
						pstmt1 = conn.prepareStatement(sql1);
						pstmt1.setString(1, suppCode);
						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{
							channelPartner = checkNull(rs1.getString(1));
							disLink = checkNull(rs1.getString(2));
							sordSite = checkNull(rs1.getString(3));
						}
						rs1.close();rs1 = null;
						pstmt1.close();pstmt1 = null;
					}

					System.out.println("channelPartner["+channelPartner+"]disLink["+disLink +"]sordSite["+sordSite + "]");
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
				//System.out.println("Inside11 connCP");
				if("Y".equalsIgnoreCase(channelPartner) && ("A".equalsIgnoreCase(disLink)  || "S".equalsIgnoreCase(disLink) || "C".equalsIgnoreCase(disLink)))
				{
					sql = " SELECT COUNT (*) FROM SORDER WHERE cust_pord = ?  and status <> 'X' and sale_order <> '0000000000'";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, pOrder);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						saleOrderCnt = rs.getInt(1);
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;

					System.out.println("Inside connCP"+saleOrderCnt);

					if(saleOrderCnt == 0)
					{
						connCP = chaneParnerExist(pOrder,disLink,channelPartner,xtraParams,conn);
						//System.out.println("connCP"+connCP);
						if(connCP != null)
						{
							System.out.println("INSIDE CONNCP CONNECTION");
							errString = gbfCreateSordPord(pOrder,xtraParams,conn,connCP);
						}
						else
						{
							System.out.println("INSIDE CONN CONNECTION");
							errString = gbfCreateSordPord(pOrder,xtraParams,conn,conn); 
						}

						if (errString.indexOf("Success") > -1)
						{
							System.out.println("@@@@@@3: Successb "+errString);
							errString = "";
						}
						else
						{
							System.out.println("@@@@@@3: Successbfd "+errString);
							sql = " update porder set confirmed = 'N',conf_date = ?,emp_code__aprv = ? ,status = 'O',status_date = ? " +
									" where purc_order = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setTimestamp(1, null);
							pstmt.setString(2, null);
							pstmt.setTimestamp(3, null);
							pstmt.setString(4, pOrder);
							updCnt= pstmt.executeUpdate();
							pstmt.close();
							pstmt = null;
							System.out.println("updCntupdCntupdCnt"+updCnt);
							if(updCnt == 1)
							{
								//added by nandkumar gadkari --------start---08/03/19
								sql = "select ind_no,item_code,quantity__stduom, unit__std from porddet where purc_order = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, pOrder);
								rs = pstmt.executeQuery();
								while(rs.next())
								{
									indNo = checkNull(rs.getString("ind_no"));
									//itemCode = rs.getString("item_code");
									oqty = rs.getDouble("quantity__stduom");
									unitStd= rs.getString("unit__std");

									if(indNo != null && indNo.trim().length() > 0)
									{

										/**Modified by Pavan Rane 24dec19 start[fetching extra columns to update with demand/supply in summary table(RunMRP process) related changes]*/
										//sql1 = "select quantity__stduom, ord_qty,  from indent where ind_no = ?  ";
										sql1 = "select quantity__stduom, ord_qty, req_date, item_code, site_code from indent where ind_no = ?  ";
										/**Modified by Pavan Rane 24dec19 end[fetching extra columns to update with demand/supply in summary table(RunMRP process) related changes]*/							    		
										pstmt1 = conn.prepareStatement(sql1);
										pstmt1.setString(1, indNo);
										rs1 = pstmt1.executeQuery();
										if(rs1.next())
										{
											cnt = 0;
											quantity = rs1.getDouble("quantity__stduom") ;
											ordqty = rs1.getDouble("ord_qty") ;
											/**Modified by Pavan Rane 24dec19 start[fetching extra columns to update with demand/supply in summary table(RunMRP process) related changes]*/
											reqDate = rs1.getTimestamp("req_date");
											itemCode = rs1.getString("item_code");
											siteCode = rs1.getString("site_code");
											/**Modified by Pavan Rane 24dec19 end[fetching extra columns to update with demand/supply in summary table(RunMRP process) related changes]*/
											cnt++;
										}
										rs1.close();rs1 = null;
										pstmt1.close();pstmt1 = null;

										totOrdQty = ordqty - oqty ;
										if(cnt > 0)
										{
											System.out.println("totOrdQty>>"+totOrdQty  + "ordqty" +ordqty+"oqty"+oqty);
											if(quantity > totOrdQty)
											{
												sql1 = "update indent set status = 'O',status_date = ?,ord_qty = ?, unit__ord = ? where ind_no = ?";
												pstmt1 = conn.prepareStatement(sql1);
												pstmt1.setTimestamp(1, currDate);
												pstmt1.setDouble(2, totOrdQty);
												pstmt1.setString(3, unitStd);
												pstmt1.setString(4, indNo);
												pstmt1.executeUpdate();
												pstmt1.close();
												pstmt1 = null;
											}
											else
											{
												sql1 = "update indent set status = 'A',status_date = ?, ord_qty = ?, unit__ord = ? where ind_no = ?";
												pstmt1 = conn.prepareStatement(sql1);
												pstmt1.setTimestamp(1, currDate);
												pstmt1.setDouble(2, totOrdQty);
												pstmt1.setString(3, unitStd);
												pstmt1.setString(4, indNo);
												pstmt1.executeUpdate();
												pstmt1.close();
												pstmt1 = null;
											}
											/**Added by Pavan Rane 24dec19 start[to update with demand/supply in summary table(RunMRP process) related changes]*/
											demandSupplyMap.put("site_code", siteCode);
											demandSupplyMap.put("item_code", itemCode);		
											demandSupplyMap.put("ref_ser", "IND");
											demandSupplyMap.put("ref_id", indNo);
											demandSupplyMap.put("ref_line", "NA");
											demandSupplyMap.put("due_date", reqDate);		
											demandSupplyMap.put("demand_qty", 0.0);
											demandSupplyMap.put("supply_qty", totOrdQty *(-1));
											demandSupplyMap.put("change_type", "C");
											demandSupplyMap.put("chg_process", "T");
											demandSupplyMap.put("chg_user", genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
											demandSupplyMap.put("chg_term", genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId"));										    
											errString = invDemSupTrcBean.updateDemandSupply(demandSupplyMap, conn);
											demandSupplyMap.clear();
											if(errString != null && errString.trim().length() > 0)
											{
												System.out.println("errString["+errString+"]");
												return errString;
											}										    
											/**Added by Pavan Rane 24dec19 end[to update with demand/supply in summary table(RunMRP process) related changes]*/
										}		

									}

								}
								rs.close();rs = null;
								pstmt.close();pstmt = null;
								//added by nandkumar gadkari------end----08/03/19
								// 28-feb-2020 manoharan not to commit locally
								//conn.commit(); 
								//isError = true;
							}
						}
					}
				} 



				if(errString == null || errString.trim().length() == 0 )
				{

					System.out.println("errString OUTSIDE FUNCTION"+errString);
					jobWorkType = distCommon.getDisparams("999999","JOBWORK_TYPE",conn);
					if(jobWorkType == null )
					{
						jobWorkType = "";
					}
					subcontractType = distCommon.getDisparams("999999","SUBCONTRACT_TYPE",conn);
					if(subcontractType == null )
					{
						subcontractType = "";
					}

					System.out.println("jobWorkType["+jobWorkType+"]subcontractType"+subcontractType+"]");
					//commented  by monika  salla on 30 july 2020 to create DO on PO button from projet esstmation baseline item.
					//	if("Y".equalsIgnoreCase(channelPartner) && (jobWorkType.trim().equalsIgnoreCase(pordType.trim())  || subcontractType.trim().equalsIgnoreCase(pordType.trim())))
					if( (jobWorkType.trim().equalsIgnoreCase(pordType.trim())  || subcontractType.trim().equalsIgnoreCase(pordType.trim())))
					{
						System.out.println("gbfCreateDistOrd INSIDE FUNCTION"+errString);
						errString = gbfCreateDistOrd(pOrder, xtraParams ,conn);
						System.out.println("errString distOrd"+errString);
						if (errString.indexOf("Success") > -1)
						{
							System.out.println("@@@@@@3: Successb "+errString);
							errString = "";
						}
						else
						{
							System.out.println("@@@@@@3: Successbfd "+errString);
							sql = " update porder set confirmed = 'N',conf_date = ?,emp_code__aprv = ? ,status = 'O',status_date = ? " +
									" where purc_order = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setTimestamp(1, null);
							pstmt.setString(2, null);
							pstmt.setTimestamp(3, null);
							pstmt.setString(4, pOrder);
							updCnt= pstmt.executeUpdate();
							pstmt.close();
							pstmt = null;
							System.out.println("updCntupdCntupdCnt"+updCnt);
							if(updCnt == 1)
							{
								//added by nandkumar gadkari --------start---08/03/19
								sql = "select ind_no,item_code,quantity__stduom, unit__std from porddet where purc_order = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, pOrder);
								rs = pstmt.executeQuery();
								while(rs.next())
								{
									indNo = checkNull(rs.getString("ind_no"));
									//itemCode = rs.getString("item_code");
									oqty = rs.getDouble("quantity__stduom");
									unitStd= rs.getString("unit__std");

									if(indNo != null && indNo.trim().length() > 0)
									{
										/**Modified by Pavan Rane 24dec19 start[fetching extra columns to update with demand/supply in summary table(RunMRP process) related changes]*/
										//sql1 = "select quantity__stduom, ord_qty   from indent where ind_no = ?  ";
										sql1 = "select quantity__stduom, ord_qty, req_date, item_code, site_code from indent where ind_no = ?  ";
										/**Modified by Pavan Rane 24dec19 end[fetching extra columns to update with demand/supply in summary table(RunMRP process) related changes]*/
										pstmt1 = conn.prepareStatement(sql1);
										pstmt1.setString(1, indNo);
										rs1 = pstmt1.executeQuery();
										if(rs1.next())
										{
											cnt = 0;
											quantity = rs1.getDouble("quantity__stduom") ;
											ordqty = rs1.getDouble("ord_qty") ;
											/**Modified by Pavan Rane 24dec19 start[fetching extra columns to update with demand/supply in summary table(RunMRP process) related changes]*/
											reqDate = rs1.getTimestamp("req_date");
											itemCode = rs1.getString("item_code");
											siteCode = rs1.getString("site_code");
											/**Modified by Pavan Rane 24dec19 end[fetching extra columns to update with demand/supply in summary table(RunMRP process) related changes]*/
											cnt++;
										}
										rs1.close();rs1 = null;
										pstmt1.close();pstmt1 = null;

										totOrdQty = ordqty - oqty ;
										if(cnt > 0)
										{
											System.out.println("totOrdQty>>"+totOrdQty  + "ordqty" +ordqty+"oqty"+oqty);
											if(quantity > totOrdQty)
											{
												sql1 = "update indent set status = 'O',status_date = ?,ord_qty = ?, unit__ord = ? where ind_no = ?";
												pstmt1 = conn.prepareStatement(sql1);
												pstmt1.setTimestamp(1, currDate);
												pstmt1.setDouble(2, totOrdQty);
												pstmt1.setString(3, unitStd);
												pstmt1.setString(4, indNo);
												pstmt1.executeUpdate();
												pstmt1.close();
												pstmt1 = null;
											}
											else
											{
												sql1 = "update indent set status = 'A',status_date = ?, ord_qty = ?, unit__ord = ? where ind_no = ?";
												pstmt1 = conn.prepareStatement(sql1);
												pstmt1.setTimestamp(1, currDate);
												pstmt1.setDouble(2, totOrdQty);
												pstmt1.setString(3, unitStd);
												pstmt1.setString(4, indNo);
												pstmt1.executeUpdate();
												pstmt1.close();
												pstmt1 = null;
											}
											/**Added by Pavan Rane 24dec19 start[to update with demand/supply in summary table(RunMRP process) related changes]*/
											demandSupplyMap.put("site_code", siteCode);
											demandSupplyMap.put("item_code", itemCode);		
											demandSupplyMap.put("ref_ser", "IND");
											demandSupplyMap.put("ref_id", indNo);
											demandSupplyMap.put("ref_line", "NA");
											demandSupplyMap.put("due_date", reqDate);		
											demandSupplyMap.put("demand_qty", 0.0);
											demandSupplyMap.put("supply_qty", totOrdQty *(-1));
											demandSupplyMap.put("change_type", "C");
											demandSupplyMap.put("chg_process", "T");
											demandSupplyMap.put("chg_user", genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
											demandSupplyMap.put("chg_term", genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId"));	
											errString = invDemSupTrcBean.updateDemandSupply(demandSupplyMap, conn);
											demandSupplyMap.clear();
											if(errString != null && errString.trim().length() > 0)
											{
												System.out.println("errString["+errString+"]");
												return errString;
											}
											/**Added by Pavan Rane 24dec19 end[to update with demand/supply in summary table(RunMRP process) related changes]*/
										}		

									}

								}
								rs.close();rs = null;
								pstmt.close();pstmt = null;
								//added by nandkumar gadkari------end----08/03/19
								// 28-feb-2020 manoharan not to commit locally
								//	conn.commit(); 
								//	isError = true;
							}
						}
					}
				}

			}

			System.out.println("errString>>>"+errString);
			System.out.println("ediOption"+ediOption);
			ediOption = ediOption != null?ediOption:"0";
			if (ediOption == null || ediOption.trim().length() == 0 || "null".equals(ediOption))
			{
				ediOption = "0";
			}
			int ediOpt = Integer.parseInt(ediOption);
			System.out.println("ediOpt"+ediOpt  +"ediOption"+ediOption);
			if(ediOpt > 0)
			{
				CreateRCPXML createRCPXML = new CreateRCPXML("w_porder", "purc_order");
				dataStr = createRCPXML.getTranXML(pOrder, conn);
				System.out.println("dataStr =[ " + dataStr + "]");
				Document ediDataDom = genericUtility.parseString(dataStr);
				System.out.println("ediDataDom =[ " + ediDataDom + "]");
				E12CreateBatchLoadEjb e12CreateBatchLoad = new E12CreateBatchLoadEjb();
				retString = e12CreateBatchLoad.createBatchLoad(ediDataDom, "w_porder",ediOption, xtraParams, conn);
				System.out.println("e12CreateBatchLoad from batchload = [" + retString + "]");
				createRCPXML = null;
				e12CreateBatchLoad = null;
				if (retString != null && "SUCCESS".equals(retString))
				{
					System.out.println("retString from batchload = [" + retString + "]");
				}
			}

			if(errString == null || errString.trim().length() == 0 )
			{
				sql = "select count(1) from pord_pay_term where purc_order = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, pOrder);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					cnt =rs.getInt(1);
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;

				if(cnt > 0 )
				{
					sql1 = " select sum(case when amt_type = '01' then rel_amt else 0 end),sum(case when amt_type = '02' then rel_amt else 0 end)," +
							" sum(case when amt_type = '03' then rel_amt else 0 end) " +
							" from   pord_pay_term where purc_order = ? ";
					pstmt1 = conn.prepareStatement(sql1);
					pstmt1.setString(1, pOrder);
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						ordPer = rs1.getDouble(1);
						netPer = rs1.getDouble(2);
						fixed  = rs1.getDouble(3);
					}
					rs1.close();rs1 = null;
					pstmt1.close();pstmt1 = null;

					System.out.println("hordAmt>>["+hordAmt + "]hnetAmt>>["+hnetAmt + "[fixed" +fixed + "]");

					ordPer = (hordAmt * (ordPer/100));
					netPer = (hnetAmt * (netPer/100));

					System.out.println("ordPer>>["+ordPer + "[netPer>>["+netPer +"](ordPer + netPer + fixed)["+(ordPer + netPer + fixed)+"]");

					if((hordAmt < ordPer) || (hnetAmt < netPer) || ((ordPer + netPer + fixed) > hnetAmt) )
					{
						errString = itmDBAccessLocal.getErrorString("", "VTINVAMT", "","",conn);
						return errString;
					}
					System.out.println("isError>>"+isError);
					// 28-feb-2020 manoharan voucher validation fails
					/*if((errString == null || errString.trim().length() == 0) && isError == false)
					{
						System.out.println("Inside gbfPorderAdvance....."+errString);
						errString = gbfPorderAdvance(pOrder,0,"PO",xtraParams,conn); // xtraParams ADDED BY NANDKUMAR GADKARI ON 11/03/19
					}
					System.out.println(" gbfPorderAdvance errString....."+errString);
					 */
					if (errString != null && errString.trim().length() > 0)
					{
						conn.rollback();
						sql = " update porder set confirmed = 'N',conf_date = ?,emp_code__aprv = ? ,status = 'O',status_date = ? " +
								" where purc_order = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setTimestamp(1, null);
						pstmt.setString(2, null);
						pstmt.setTimestamp(3, null);
						pstmt.setString(4, pOrder);
						updCnt= pstmt.executeUpdate();
						pstmt.close();
						pstmt = null;
						System.out.println("updCntupdCntupdCnt"+updCnt);
						if(updCnt == 1)
						{
							//added by nandkumar gadkari --------start---08/03/19
							sql = "select ind_no,item_code,quantity__stduom, unit__std from porddet where purc_order = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, pOrder);
							rs = pstmt.executeQuery();
							while(rs.next())
							{
								indNo = checkNull(rs.getString("ind_no"));
								//itemCode = rs.getString("item_code");
								oqty = rs.getDouble("quantity__stduom");
								unitStd= rs.getString("unit__std");

								if(indNo != null && indNo.trim().length() > 0)
								{

									/**Modified by Pavan Rane 24dec19 start[fetching extra columns to update with demand/supply in summary table(RunMRP process) related changes]*/
									//sql1 = "select quantity__stduom, ord_qty   from indent where ind_no = ?  ";
									sql1 = "select quantity__stduom, ord_qty, req_date, item_code, site_code from indent where ind_no = ?  ";
									/**Modified by Pavan Rane 24dec19 end[fetching extra columns to update with demand/supply in summary table(RunMRP process) related changes]*/
									pstmt1 = conn.prepareStatement(sql1);
									pstmt1.setString(1, indNo);
									rs1 = pstmt1.executeQuery();
									if(rs1.next())
									{
										cnt = 0;
										quantity = rs1.getDouble("quantity__stduom") ;
										ordqty = rs1.getDouble("ord_qty") ;
										/**Modified by Pavan Rane 24dec19 start[fetching extra columns to update with demand/supply in summary table(RunMRP process) related changes]*/
										reqDate = rs1.getTimestamp("req_date") ;
										itemCode = rs1.getString("item_code") ;
										siteCode = rs1.getString("site_code") ;
										/**Modified by Pavan Rane 24dec19 end[fetching extra columns to update with demand/supply in summary table(RunMRP process) related changes]*/
										cnt++;
									}
									rs1.close();rs1 = null;
									pstmt1.close();pstmt1 = null;

									totOrdQty = ordqty - oqty ;
									if(cnt > 0)
									{
										System.out.println("totOrdQty>>"+totOrdQty  + "ordqty" +ordqty+"oqty"+oqty);
										if(quantity > totOrdQty)
										{
											sql1 = "update indent set status = 'O',status_date = ?,ord_qty = ?, unit__ord = ? where ind_no = ?";
											pstmt1 = conn.prepareStatement(sql1);
											pstmt1.setTimestamp(1, currDate);
											pstmt1.setDouble(2, totOrdQty);
											pstmt1.setString(3, unitStd);
											pstmt1.setString(4, indNo);
											pstmt1.executeUpdate();
											pstmt1.close();
											pstmt1 = null;
										}
										else
										{
											sql1 = "update indent set status = 'A',status_date = ?, ord_qty = ?, unit__ord = ? where ind_no = ?";
											pstmt1 = conn.prepareStatement(sql1);
											pstmt1.setTimestamp(1, currDate);
											pstmt1.setDouble(2, totOrdQty);
											pstmt1.setString(3, unitStd);
											pstmt1.setString(4, indNo);
											pstmt1.executeUpdate();
											pstmt1.close();
											pstmt1 = null;
										}
										/**Added by Pavan Rane 24dec19 start[to update with demand/supply in summary table(RunMRP process) related changes]*/
										demandSupplyMap.put("site_code", siteCode);
										demandSupplyMap.put("item_code", itemCode);		
										demandSupplyMap.put("ref_ser", "IND");
										demandSupplyMap.put("ref_id", indNo);
										demandSupplyMap.put("ref_line", "NA");
										demandSupplyMap.put("due_date", reqDate);		
										demandSupplyMap.put("demand_qty", 0.0);
										demandSupplyMap.put("supply_qty", totOrdQty *(-1));
										demandSupplyMap.put("change_type", "C");
										demandSupplyMap.put("chg_process", "T");
										demandSupplyMap.put("chg_user", genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
										demandSupplyMap.put("chg_term", genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId"));	
										errString = invDemSupTrcBean.updateDemandSupply(demandSupplyMap, conn);
										demandSupplyMap.clear();
										if(errString != null && errString.trim().length() > 0)
										{
											System.out.println("errString["+errString+"]");
											return errString;
										}
										/**Added by Pavan Rane 24dec19 end[to update with demand/supply in summary table(RunMRP process) related changes]*/
									}		

								}

							}
							rs.close();rs = null;
							pstmt.close();pstmt = null;
							//added by nandkumar gadkari------end----08/03/19

							//conn.commit();  // 28-feb-2020 manoharan not to commit in all methods
							//isError = true;
						}
					} 
				}
			}

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
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if(pstmt1 != null)
				{
					pstmt1.close();
					pstmt1 = null;					
				}
				if(rs != null)
				{
					rs.close();
					rs = null;
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
	private String gbfConfirmOrder(String pOrder,double approxCost,String xtraParams, Connection conn) throws ITMException 
	{
		System.out.println("Inside gbfConfirmOrder............."+approxCost);
		String sql = "",sql2 = "",indnoHdr = "",boqId = "",taskCode = "",pordType = "",indNo = "",sql1 = "",typeAllowProjbudgtList="",proj = "" ,
				unitStd = "", enqNo = "",itemCd = "",termCode = "",termCode1 = "" ,termTable = "";
		PreparedStatement pstmt = null,pstmt1=null,pstmt2=null;
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		String errString = null;
		ResultSet rs = null,rs1 = null;
		ordFlag = false;
		int cnt=0 ,count = 0 ;
		double poAmt=0.0,currentPoamt=0.0,porcpAmt =0.0,pretAmt=0.0,totAmtProj=0.0,exceedAmt=0.0 ,oqty = 0.0 ,
				quantity = 0.0 , ordqty = 0.0 ,totOrdQty = 0.0;
		java.sql.Timestamp currDate = null;
		currDate = new java.sql.Timestamp(System.currentTimeMillis()) ;
		Timestamp reqDate = null;
		String itemCode = "";
		String sitecode = "";
		try
		{
			InvDemSuppTraceBean invDemSupTrcBean = new InvDemSuppTraceBean();
			HashMap demandSupplyMap = new HashMap();
			sql = "select count(*)  from porddet where purc_order = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, pOrder);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				cnt = rs.getInt(1); 
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;

			if(cnt == 0)
			{
				errString = itmDBAccessLocal.getErrorString("","VTNCONFT","","",conn);
				return errString;
			}

			sql = "select ind_no , tran_id__boq,task_code,ord_date , pord_type from porder where purc_order = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, pOrder);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				indnoHdr = rs.getString("ind_no");
				boqId = rs.getString("tran_id__boq");
				taskCode = rs.getString("task_code");
				//ordDate = rs.getTimestamp("ord_date");
				pordType = rs.getString("pord_type");
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;

			if("WO".equalsIgnoreCase(pordType))
			{
				if(cnt > 0)
				{
					sql = "select distinct ind_no,item_code from porddet where purc_order =  ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, pOrder);
					rs = pstmt.executeQuery();
					while(rs.next())
					{
						indNo = rs.getString(1);
						itemCd = rs.getString("item_code");
						System.out.println("WO indNo"+indNo + "itemCd"+itemCd);

						sql1 = "update proj_est_bsl_item set purc_order = ?  where ind_no = ? and task_code= ? and item_code= ?";
						pstmt1 = conn.prepareStatement(sql1);
						pstmt1.setString(1, pOrder);
						pstmt1.setString(2, indNo);
						pstmt1.setString(3, taskCode);
						pstmt1.setString(4, itemCd);
						pstmt1.executeUpdate();
						pstmt1.close();
						pstmt1 = null;

						sql1 = "update PROJ_ACT_MILESTONE set purc_order = ?  where ind_no = ? and task_code= ? ";
						pstmt1 = conn.prepareStatement(sql1);
						pstmt1.setString(1, pOrder);
						pstmt1.setString(2, indNo);
						pstmt1.setString(3, taskCode);
						pstmt1.executeUpdate();
						pstmt1.close();
						pstmt1 = null;

					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;
				}

			}
			if(errString == null || errString.trim().length() == 0)
			{
				typeAllowProjbudgtList = distCommon.getDisparams("999999", "TYPE_ALLOW_PROJBUDGET", conn);
				System.out.println("typeAllowProjbudgtList["+typeAllowProjbudgtList+"]");
				if("NULLFOUND".equalsIgnoreCase(typeAllowProjbudgtList))
				{
					typeAllowProjbudgtList = "";
					ordFlag=false ;
				}
				String typeAllowProjbudgtArray[]=typeAllowProjbudgtList.split(",");

				for(int i=0;i< typeAllowProjbudgtArray.length ; i++)
				{
					System.out.println("typeAllowProjbudgtArray"+typeAllowProjbudgtArray[i]);
					if(pordType.equalsIgnoreCase(typeAllowProjbudgtArray[i]))
					{
						ordFlag=true ;
					}
				}
				System.out.println("ordFlag == true "+ (ordFlag == true) );
				if(ordFlag == true )
				{
					sql = " select a.proj_code,sum(a.tot_amt * b.exch_rate) from porddet a , porder b where a.purc_order = ? " +
							" and a.purc_order = b.purc_order group by a.proj_code ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, pOrder);
					rs = pstmt.executeQuery();
					while(rs.next())
					{
						proj = checkNull(rs.getString(1));
						currentPoamt = rs.getDouble(2);	
						System.out.println("proj["+proj+"]"+"currentPoamt"+currentPoamt);

						sql1 = " select approx_cost from project where proj_code =  ?" ;
						pstmt1 = conn.prepareStatement(sql1);
						pstmt1.setString(1, proj);
						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{
							approxCost = rs1.getDouble(1);
						}
						rs1.close();rs1 = null;
						pstmt1.close();pstmt1 = null;		    	


						System.out.println("approxCost["+approxCost+"]");

						sql1 = " select sum(a.tot_amt * b.exch_rate ) from porddet a, porder b where a.purc_order = b.purc_order " +
								" and b.confirmed = 'Y' and a.proj_code = ? and b.purc_order <> ? and b.status <> 'X' and a.status <> 'C'";
						pstmt1 = conn.prepareStatement(sql1);
						pstmt1.setString(1, proj);
						pstmt1.setString(2, pOrder);
						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{
							poAmt = rs1.getDouble(1);
						}
						rs1.close();rs1 = null;
						pstmt1.close();pstmt1 = null;

						System.out.println("poAmt["+poAmt+"]");
						sql1 = "select sum(a.net_amt * b.exch_rate) from porcpdet a, porcp b ,porddet c where ( a.purc_order = c.purc_order ) and (a.tran_id = b.tran_id )" +
								"and 	a.line_no__ord = c.line_no and b.confirmed = 'Y' and c.proj_code = ?  and" +
								" a.purc_order <> ? and b.status <> 'X'	and c.status = 'C' and b.tran_ser = 'P-RCP' ";
						pstmt1 = conn.prepareStatement(sql1);
						pstmt1.setString(1, proj);
						pstmt1.setString(2, pOrder);
						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{
							porcpAmt = rs1.getDouble(1);
						}
						rs1.close();rs1 = null;
						pstmt1.close();pstmt1 = null;

						System.out.println("porcpAmt["+porcpAmt+"]");
						sql1 = "select sum(a.net_amt * b.exch_rate ) from porcpdet a, porcp b ,porddet c where ( a.purc_order = c.purc_order ) " +
								"and (a.tran_id = b.tran_id ) and b.confirmed = 'Y' and c.proj_code = ? and b.status <> 'X' " +
								"and c.status = 'C'	and b.tran_ser = 'P-RET' ";
						pstmt1 = conn.prepareStatement(sql1);
						pstmt1.setString(1, proj);
						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{
							pretAmt = rs1.getDouble(1);
						}
						rs1.close();rs1 = null;
						pstmt1.close();pstmt1 = null;

						System.out.println("pretAmt["+pretAmt+"]");

						poAmt = poAmt + porcpAmt - pretAmt ;
						totAmtProj =  poAmt + currentPoamt ; 
						exceedAmt = totAmtProj - approxCost ;

						System.out.println("poAmt LA["+poAmt+"]totAmtProj["+totAmtProj +"]exceedAmt "+exceedAmt+"]");

						if (totAmtProj > approxCost )
						{
							errString = itmDBAccessLocal.getErrorString("","VTPROJCNF","","",conn);
							return errString;
						}
					}
				}
			}
			System.out.println("indnoHdr" +indnoHdr + "boqId" + boqId );
			if(indnoHdr != null && indnoHdr.trim().length() > 0 && boqId != null && boqId.trim().length() > 0)
			{
				sql1 = "update indent set status = 'L',status_date = ? where ind_no = ?";
				pstmt1 = conn.prepareStatement(sql1);
				pstmt1.setTimestamp(1, currDate);
				pstmt1.setString(2, indNo);
				pstmt1.executeUpdate();
				pstmt1.close();
				pstmt1 = null;
			}

			sql = "select ind_no,item_code,quantity__stduom, unit__std from porddet where purc_order = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, pOrder);
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				indNo = checkNull(rs.getString("ind_no"));
				//itemCode = rs.getString("item_code");
				oqty = rs.getDouble("quantity__stduom");
				unitStd= rs.getString("unit__std");

				if(indNo != null && indNo.trim().length() > 0)
				{
					sql1 = "select enq_no  from enq_det where ind_no = ?  ";
					pstmt1 = conn.prepareStatement(sql1);
					pstmt1.setString(1, indNo);
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						enqNo = rs1.getString("enq_no");

						if(enqNo != null && enqNo.trim().length() > 0)
						{
							sql2 = "update enq_hdr set status = 'C' where enq_no = ?";
							pstmt2 = conn.prepareStatement(sql2);
							pstmt2.setString(1, enqNo);
							pstmt2.executeUpdate();
							pstmt2.close();
							pstmt2 = null;
						}
					}
					rs1.close();rs1 = null;
					pstmt1.close();pstmt1 = null;

					/**Modified by Pavan Rane 24dec19 start[fetching extra columns to update with demand/supply in summary table(RunMRP process) related changes]*/
					//sql1 = "select quantity__stduom, ord_qty   from indent where ind_no = ?  ";
					sql1 = "select quantity__stduom, ord_qty, req_date, item_code, site_code from indent where ind_no = ?  ";
					/**Modified by Pavan Rane 24dec19 end[fetching extra columns to update with demand/supply in summary table(RunMRP process) related changes]*/
					pstmt1 = conn.prepareStatement(sql1);
					pstmt1.setString(1, indNo);
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						cnt = 0;
						quantity = rs1.getDouble("quantity__stduom") ;
						ordqty = rs1.getDouble("ord_qty") ;
						/**Modified by Pavan Rane 24dec19 start[fetching extra columns to update with demand/supply in summary table(RunMRP process) related changes]*/
						reqDate = rs1.getTimestamp("req_date") ;
						itemCode = rs1.getString("item_code") ;
						sitecode = rs1.getString("site_code");
						/**Modified by Pavan Rane 24dec19 end[fetching extra columns to update with demand/supply in summary table(RunMRP process) related changes]*/
						cnt++;
					}
					rs1.close();rs1 = null;
					pstmt1.close();pstmt1 = null;

					System.out.println("cnt"+cnt);
					if(cnt == 0)
					{
						errString = itmDBAccessLocal.getErrorString("","VTINDENT1","","",conn);
						return errString;
					}

					totOrdQty = ordqty + oqty ;
					System.out.println("totOrdQty>>"+totOrdQty  + "ordqty" +ordqty+"oqty"+oqty);
					if(quantity > totOrdQty)
					{
						sql1 = "update indent set status = 'O',status_date = ?,ord_qty = ?, unit__ord = ? where ind_no = ?";
						pstmt1 = conn.prepareStatement(sql1);
						pstmt1.setTimestamp(1, currDate);
						pstmt1.setDouble(2, totOrdQty);
						pstmt1.setString(3, unitStd);
						pstmt1.setString(4, indNo);
						pstmt1.executeUpdate();
						pstmt1.close();
						pstmt1 = null;
					}
					else
					{
						sql1 = "update indent set status = 'L',status_date = ?, ord_qty = ?, unit__ord = ? where ind_no = ?";
						pstmt1 = conn.prepareStatement(sql1);
						pstmt1.setTimestamp(1, currDate);
						pstmt1.setDouble(2, totOrdQty);
						pstmt1.setString(3, unitStd);
						pstmt1.setString(4, indNo);
						pstmt1.executeUpdate();
						pstmt1.close();
						pstmt1 = null;
					}
					/**Added by Pavan Rane 24dec19 start[to update with demand/supply in summary table(RunMRP process) related changes]*/
					demandSupplyMap.put("site_code", sitecode);
					demandSupplyMap.put("item_code", itemCode);		
					demandSupplyMap.put("ref_ser", "IND");
					demandSupplyMap.put("ref_id", indNo);
					demandSupplyMap.put("ref_line", "NA");
					demandSupplyMap.put("due_date", reqDate);		
					demandSupplyMap.put("demand_qty", 0.0);
					demandSupplyMap.put("supply_qty", totOrdQty *(-1));
					demandSupplyMap.put("change_type", "C");
					demandSupplyMap.put("chg_process", "T");
					demandSupplyMap.put("chg_user", genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
					demandSupplyMap.put("chg_term", genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId"));	
					errString = invDemSupTrcBean.updateDemandSupply(demandSupplyMap, conn);
					demandSupplyMap.clear();
					if(errString != null && errString.trim().length() > 0)
					{
						System.out.println("errString["+errString+"]");
						return errString;
					}
					/**Added by Pavan Rane 24dec19 end[to update with demand/supply in summary table(RunMRP process) related changes]*/		

				}

			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;

			sql = "select term_table from porder where purc_order = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, pOrder);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				termTable = rs.getString("term_table");
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;

			ArrayList<String> termCodeList = new ArrayList();

			sql1="SELECT PURC_ORDER,LINE_NO,TERM_CODE,DESCR,PRINT_OPT FROM PORD_TERM WHERE PURC_ORDER = ? ORDER BY LINE_NO ASC";//Change By Mukesh Chauhan on 09/01/20
			pstmt1 = conn.prepareStatement(sql1);
			pstmt1.setString(1, pOrder);
			rs1 = pstmt1.executeQuery();
			while(rs1.next())
			{
				termCode1 = checkNull(rs1.getString(3));
				termCodeList.add(termCode1);//Change By Mukesh Chauhan on 09/01/2020

			}
			System.out.println("termCodeList>>"+termCodeList);
			rs1.close();rs1 = null;
			pstmt1.close();pstmt1 = null;


			sql = "select term_code from pur_term_table where term_table = ? and mandatory = 'Y'";//Change By Mukesh Chauhan on 09/01/2020
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, termTable);
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				termCode = checkNull(rs.getString(1));//Change By Mukesh Chauhan on 09/01/2020

				System.out.println("termCode1["+termCode1+"]termCode["+termCode+ "]");
				if(termCodeList.contains(termCode))
				{
					count++;
				}
				if(count == 0)
				{
					errString = itmDBAccessLocal.getErrorString("","VTTERMFND","","",conn);
					return errString;
				}
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;

			if(errString == null || errString.trim().length() == 0)
			{
				System.out.println("inside confirm");
				errString = gbfUpdateBudgetPorder(pOrder,conn);
			}

			if (errString != null && errString.trim().length() > 0)
			{
				conn.rollback();
			} 


		}
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new ITMException(e);
		} 
		finally
		{
			try
			{
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if(pstmt1 != null)
				{
					pstmt1.close();
					pstmt1 = null;					
				}
				if(rs != null)
				{
					rs.close();
					rs = null;
				}
				if(rs1 != null)
				{
					rs1.close();
					rs1 = null;
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
	private String checkNull(String str)
	{
		if(str == null)
		{
			return "";
		}
		else
		{
			return str ;
		}

	}
	// ....start here...add by KailasG on 16-aug-2019[getting error while confirm the despatch]
	private String checkNullTrim(String input)
	{
		if (input == null || "null".equalsIgnoreCase(input) )
		{
			input= "";
		}
		return input.trim();
	}
	// ....end  here...add by KailasG on 16-aug-2019[getting error while confirm the despatch]

	private String gbfUpdateBudgetPorder(String pOrder, Connection conn) throws ITMException 
	{
		System.out.println("Inside gbfUpdateBudgetPorder.............");
		String errString = "",activeBudget= "",sql = "",indNo = "",pordSite =  "",projCode = "",acctCode	= "",cctrCode = "",sql1 = "",
				deptCode ="",analCode , siteCode= "",tranType = "";
		PreparedStatement pstmt = null,pstmt1=null;
		double purQty = 0.0 , totAmt = 0.0 ,exchRate = 0.0,indRate = 0.0 ,indAmt = 0.0 ,amount = 0.0;
		ResultSet rs= null ,rs1 = null;
		int cnt1 = 0;
		Timestamp ordDate = null ,tranDate = null;
		ArrayList commtList = new ArrayList();
		SimpleDateFormat sdf = null;
		StringBuffer budgetXml = null;
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();

		try
		{
			System.out.println("pOrder"+pOrder);
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			budgetXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n<header>\r\n</header>");


			activeBudget = finCommon.getFinparams("999999", "ACTIVE_BUDGET", conn);
			System.out.println("activeBudget>>"+activeBudget);
			if("NULLFOUND".equalsIgnoreCase(activeBudget))
			{
				errString = itmDBAccessLocal.getErrorString("","VTFINPARM","","",conn);
				return errString;
			}
			if(activeBudget == null )
			{
				activeBudget = "N";
			}

			if("N".equalsIgnoreCase(activeBudget ))
			{
				errString = "";
			}
			System.out.println("888888>>"+activeBudget);

			sql = " select porddet.ind_no,porddet.site_code,porder.proj_code, acct_code__dr,cctr_code__dr,rate,quantity,ord_date,porddet.dept_code , porddet.tot_amt	,porder.purc_order,	" +
					" line_no ,exch_rate,  porder.anal_code from porder,porddet where porder.purc_order = porddet.purc_order " +
					" and porddet.purc_order = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, pOrder);
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				System.out.println("888888 while>>"+activeBudget);
				indNo =checkNull( rs.getString(1));
				pordSite =  checkNull( rs.getString(2));	
				projCode =checkNull(  rs.getString(3));	
				acctCode	= checkNull( rs.getString(4));
				cctrCode = checkNull( rs.getString(5));
				//purRate= rs.getDouble(6);
				purQty = rs.getDouble(7);
				ordDate = rs.getTimestamp(8);	
				deptCode = checkNull( rs.getString(9));
				totAmt = rs.getDouble(10);	
				//purcOrder = checkNull( rs.getString(11));	
				//lineNo = checkNull( rs.getString(12));
				exchRate = rs.getDouble(13); 
				analCode = checkNull( rs.getString(14));
				System.out.println("indNo>>["+indNo+ "]");

				if(indNo != null && indNo.trim().length() > 0 )
				{
					cnt1 = 0;
					sql1 = "select 	purc_rate from   indent where ind_no = ?";
					pstmt1 = conn.prepareStatement(sql1);
					pstmt1.setString(1, indNo);
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						indRate = rs1.getDouble("purc_rate");
						cnt1++;
					}

					rs1.close();rs1 = null;
					pstmt1.close();pstmt1 = null;

					if(cnt1 == 0)
					{
						errString = itmDBAccessLocal.getErrorString("","VTNOIND","","",conn);
						return errString;
					}
					indAmt = purQty * indRate  ; 		
				}
				amount = totAmt  * exchRate ;
				System.out.println("amount>>>>>>>>>>>"+amount + "totAmt" +totAmt + "purQty"+purQty +"indRate" +indRate + "exchRate"+exchRate);
				HashMap pOrderMap = new HashMap();
				pOrderMap.put("site_code", pordSite);
				pOrderMap.put("proj_code", projCode);
				pOrderMap.put("tran_date", ordDate);
				pOrderMap.put("acct_code", acctCode);
				pOrderMap.put("cctr_code", cctrCode);
				pOrderMap.put("dept_code", deptCode);
				pOrderMap.put("upd_type", "R");
				pOrderMap.put("tran_type", "PORD");
				pOrderMap.put("amount", amount);
				pOrderMap.put("ind_amount", indAmt);
				pOrderMap.put("anal_code", analCode);

				System.out.println("pOrderMap.size() "+pOrderMap.size() + "pOrderMap" +pOrderMap);
				commtList.add(pOrderMap);
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;

			for(int cnt = 0 ;commtList.size() > cnt ;cnt++) 
			{
				System.out.println("inddfgsdgsdfg");
				HashMap commMap = new HashMap();

				commMap = (HashMap) commtList.get(cnt);

				System.out.println(commMap);
				siteCode 	= (String) commMap.get("site_code");
				projCode	= (String) commMap.get("proj_code");
				tranDate		= (Timestamp) commMap.get("tran_date");
				acctCode		= (String) commMap.get("acct_code");
				cctrCode		= (String) commMap.get("cctr_code");
				deptCode	= (String)   commMap.get("dept_code");
				//updType		= (String)   commMap.get("upd_type") ;
				tranType	= (String) commMap.get("tran_type");
				amount	= (Double) commMap.get("amount");
				//indAmount		= (Double) commMap.get("ind_amount");
				analCode	= (String) commMap.get("anal_code");

				budgetXml.append("<Detail1>");
				budgetXml.append("<site_code><![CDATA["+siteCode+"]]></site_code>");
				budgetXml.append("<proj_code><![CDATA["+projCode+"]]></proj_code>");
				budgetXml.append("<tran_date><![CDATA["+sdf.format(tranDate)+"]]></tran_date>");
				budgetXml.append("<acct_code><![CDATA["+acctCode+"]]></acct_code>");
				budgetXml.append("<cctr_code><![CDATA["+cctrCode+"]]></cctr_code>");
				budgetXml.append("<dept_code><![CDATA["+deptCode+"]]></dept_code>");
				budgetXml.append("<upd_type><![CDATA[R]]></upd_type>");
				budgetXml.append("<tran_type><![CDATA["+tranType+"]]></tran_type>");
				budgetXml.append("<amount><![CDATA["+amount+"]]></amount>");
				budgetXml.append("<anal_code><![CDATA["+analCode+"]]></anal_code>");
				budgetXml.append("</Detail1>");
			}
			budgetXml.append("</Root>");

			System.out.print("Budget Xml String to update Budget :: "+budgetXml.toString());

			System.out.println("errString>>>>>>>>>>>{"+errString+"}" + errString.trim().length());
			if(errString == null || errString.trim().length() == 0)
			{
				System.out.println("errString11>>>>>>>>>>>"+errString);
				errString = finCommon.updateBudget(budgetXml.toString(),"",conn);
				System.out.println("rrString1122222>>>>>>>>>>>"+errString);

				if(errString != null && errString.trim().length() > 0)
				{
					conn.rollback();
					return errString ;
				}
			}

		}
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new ITMException(e);
		} 
		finally
		{
			try
			{
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}

				if(rs != null)
				{
					rs.close();
					rs = null;
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

	private String gbfCreateDistOrd(String pOrder, String xtraParams ,Connection conn) throws ITMException 
	{
		System.out.println("Inside gbfCreateDistOrd.............");
		String errString = "",suppCode= "",sql = "",pordSite =  "",orderType = "",locgroupJwiss	= "",channelPartner = "",sql1 = "",
				suppSite = "",keyStr = "", siteCode= "",distOrder = "",currCode = "",
				autoConfirm = "" ,priceList = "", locCodeGit = "",locCodeGitbf = "", autoReciept ="" ,locCodeCons= "" ,remarks ="",bomXml ="",// = "",
                bom= "",itemCode = "" ,itemCodeDet = "",unitSal = "",unit = "",xmlString="" ,qtyDetStr = "" ;
                String xmlStringBom="";
		
		PreparedStatement pstmt = null,pstmt1=null ,pstmtPo = null;
		double refQty = 0.0 ,fact = 0.0 ,qtyDet= 0.0;
		ResultSet rs= null ,rs1 = null ,rsPo = null;
		int cnt1 = 0,lineNo = 0 ,lineNoref = 0,xmlStringFrmBom = 0;
		Document domBom = null;
		NodeList detlList = null;
		Timestamp pordDate = null;
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		StringBuffer xmlBuff = null;
		ArrayList convQuantityFactArryList = new ArrayList();	
        String userId = "";
        String suppLoctn="";
		try
		{
			java.sql.Timestamp tranDate = null;
			tranDate = new java.sql.Timestamp(System.currentTimeMillis()) ;
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode"); 
			System.out.println("--login code---1299--"+userId);

			sql = " select supp_code, site_code__dlv, item_ser, ord_date , pord_type,loc_group__jwiss   " +
					" from porder	where purc_order = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, pOrder);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				suppCode = checkNull(rs.getString("supp_code"));
				pordSite = checkNull(rs.getString("site_code__dlv"));
				//itemSer  = checkNull(rs.getString("item_ser"));
				pordDate = rs.getTimestamp("ord_date");
				orderType= checkNull(rs.getString("pord_type"));
				locgroupJwiss = checkNull(rs.getString("loc_group__jwiss"));
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;


			sql = " select channel_partner,  site_code__ch from site_supplier	where site_code = ? and supp_code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, pordSite);
			pstmt.setString(2, suppCode);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				channelPartner = checkNull(rs.getString("channel_partner"));
				//disLink = checkNull(rs.getString("dis_link"));
				suppSite  = checkNull(rs.getString("site_code__ch"));
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;

			System.out.println("channelPartner>>>"+channelPartner);
			if(channelPartner == null || channelPartner.trim().length() == 0)
			{

				sql = " select case when channel_partner is null then 'N' else channel_partner end,site_code,supp_locn " +
						" from supplier	where supp_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, suppCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					channelPartner = checkNull(rs.getString(1));
                    suppSite  = rs.getString("site_code");
                    suppLoctn=rs.getString("supp_locn");
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
			}

            System.out.println("channelPartner14"+channelPartner);
			System.out.println("channelPartner>>"+channelPartner + "suppSite>>"+suppSite);
			System.out.println("suppSite dist"+suppSite + "pordSite" +pordSite + "order_date"+ pordDate);
			orderType = distCommon.getDisparams("999999", "DISORDTYP_SUBCTR", conn);
			System.out.println("orderType>>"+orderType);
			if(orderType == null || "NULLFOUND".equalsIgnoreCase(orderType))
			{
				errString = "Not Defined" + "Environmental Variabe DISORDTYP_SUBCTR is not defined";
				return errString;
			}

			if(!"Y".equalsIgnoreCase(channelPartner))
			{
				//commented by monika salla 2020
				//errString = "";
				//added by monika  salla on 30 july 2020 to create DO on PO button from projet esstmation baseline item.
				System.out.println("gbfCreateDistOrdNonCP INSIDE FUNCTION"+errString);
				errString = gbfCreateDistOrdNonCP(pOrder, xtraParams, conn);
				System.out.println("errString distOrd"+errString);
				return errString;
				
			}//end

			sql = "select key_string from transetup where upper(tran_window) = 'W_DIST_ORDER'" ;
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				keyStr = checkNull(rs.getString("key_string"));

			}
			else
			{
				sql = "select key_string from transetup where upper(tran_window) = 'GENERAL'";
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();	
				if(rs.next())
				{
					keyStr = checkNull(rs.getString(1));
				}
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;

			xmlBuff = new StringBuffer();

			System.out.println("--XML CREATION --" + tranDate + "tranDate" +sdf.format(tranDate).toString() );

			xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
			xmlBuff.append("<DocumentRoot>");
			xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
			xmlBuff.append("<group0>");
			xmlBuff.append("<description>").append("Group0 description").append("</description>");
			xmlBuff.append("<Header0>");
			xmlBuff.append("<objName><![CDATA[").append("dist_order").append("]]></objName>");  
			xmlBuff.append("<pageContext><![CDATA[").append("1").append("]]></pageContext>");
			xmlBuff.append("<objContext><![CDATA[").append("1").append("]]></objContext>");
			xmlBuff.append("<editFlag><![CDATA[").append("A").append("]]></editFlag>");
			xmlBuff.append("<focusedColumn><![CDATA[").append("").append("]]></focusedColumn>");
			xmlBuff.append("<action><![CDATA[").append("SAVE").append("]]></action>");
			xmlBuff.append("<elementName><![CDATA[").append("").append("]]></elementName>");
			xmlBuff.append("<keyValue><![CDATA[").append("1").append("]]></keyValue>");
			xmlBuff.append("<taxKeyValue><![CDATA[").append("").append("]]></taxKeyValue>");
			xmlBuff.append("<saveLevel><![CDATA[").append("1").append("]]></saveLevel>");
			xmlBuff.append("<forcedSave><![CDATA[").append(true).append("]]></forcedSave>");
			xmlBuff.append("<taxInFocus><![CDATA[").append(false).append("]]></taxInFocus>");
			xmlBuff.append("<description>").append("Header0 members").append("</description>");	
			xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"dist_order\" objContext=\"1\">");  
			xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
			xmlBuff.append("<dist_order/>");
			xmlBuff.append("<order_type><![CDATA["+ orderType +"]]></order_type>");
			xmlBuff.append("<order_date><![CDATA["+ sdf.format(tranDate).toString() +"]]></order_date>");
			xmlBuff.append("<site_code><![CDATA["+ pordSite +"]]></site_code>");
			xmlBuff.append("<site_code__ship><![CDATA["+ pordSite   +"]]></site_code__ship>");
			xmlBuff.append("<site_code__dlv><![CDATA["+suppSite +"]]></site_code__dlv>");
			xmlBuff.append("<ship_date><![CDATA["+ sdf.format(tranDate).toString()   +"]]></ship_date>");

			if(locgroupJwiss != null && locgroupJwiss.trim().length()>0)
			{
				xmlBuff.append("<loc_group__jwiss><![CDATA["+ locgroupJwiss  +"]]></loc_group__jwiss>");
			}
			else
			{
				sql = "select loc_group__jwiss from distorder_type where tran_type = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, orderType);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					locgroupJwiss = checkNull(rs.getString("loc_group__jwiss"));
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
				xmlBuff.append("<loc_group__jwiss><![CDATA["+ locgroupJwiss  +"]]></loc_group__jwiss>");
			}

			sql = "select a.curr_code from finent a, site b where a.fin_entity = b.fin_entity and b.site_code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, pordSite);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				currCode = checkNull(rs.getString(1));
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;

			xmlBuff.append("<curr_code><![CDATA["+ currCode  +"]]></curr_code>");
			xmlBuff.append("<exch_rate><![CDATA["+ 1  +"]]></exch_rate>");

			autoConfirm = distCommon.getDisparams("999999", "SUBCTR_DORD_CONF", conn);
			if("".equalsIgnoreCase(autoConfirm) || autoConfirm == null)
			{
				autoConfirm = "Y";
			}
			System.out.println("autoConfirm>>"+autoConfirm + "autoConfirm.contains" + autoConfirm.contains("N"));	
			if(!autoConfirm.contains("Y") && !autoConfirm.contains("N"))
			{
				errString = itmDBAccessLocal.getErrorString("", "VTDISPARM", "","",conn);
				return errString;
			}
			xmlBuff.append("<confirmed><![CDATA["+"N"+"]]></confirmed>");
			xmlBuff.append("<conf_date><![CDATA["+sdf.format(tranDate).toString()+"]]></conf_date>");
			xmlBuff.append("<tran_type><![CDATA["+orderType+"]]></tran_type>");
			xmlBuff.append("<purc_order><![CDATA["+pOrder+"]]></purc_order>");

			priceList = distCommon.getDisparams("999999", "PRICELIST_DIS_SUBCTR", conn);
			System.out.println("priceList>>"+priceList);	
			if(priceList != null && !"NULLFOUND".equalsIgnoreCase(priceList))
			{

				xmlBuff.append("<price_list><![CDATA["+priceList+"]]></price_list>");

			}

			sql = "select loc_code__git,loc_code__gitbf,loc_code__cons,auto_receipt from distorder_type where tran_type = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, orderType);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				cnt1 = 0;
				locCodeGit = checkNull(rs.getString("loc_code__git"));
				locCodeGitbf = checkNull(rs.getString("loc_code__gitbf"));
				locCodeCons = checkNull(rs.getString("loc_code__cons"));
				autoReciept = checkNull(rs.getString("auto_receipt"));
				cnt1++;
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;

			if(cnt1 == 0)
			{
				errString = itmDBAccessLocal.getErrorString("", "VTDORDTY", "","",conn);
				return errString;
			}
			xmlBuff.append("<loc_code__git><![CDATA["+locCodeGit+"]]></loc_code__git>");
			xmlBuff.append("<loc_code__gitbf><![CDATA["+locCodeGitbf+"]]></loc_code__gitbf>");
			xmlBuff.append("<loc_code__cons><![CDATA["+locCodeCons+"]]></loc_code__cons>");
			xmlBuff.append("<auto_receipt><![CDATA["+autoReciept+"]]></auto_receipt>");
			xmlBuff.append("<status><![CDATA["+"P"+"]]></status>");
			remarks = "Auto created from PO No. : " + pOrder + " Dated " + pordDate ;
			xmlBuff.append("<remarks><![CDATA["+ remarks +"]]></remarks>");
			xmlBuff.append("<purc_order><![CDATA["+ pOrder +"]]></purc_order>");
			xmlBuff.append("</Detail1>");


			sql = "SELECT PORDDET.PURC_ORDER,   PORDDET.LINE_NO,   PORDDET.SITE_CODE,   PORDDET.IND_NO,   PORDDET.ITEM_CODE,   PORDDET.QUANTITY,   PORDDET.UNIT,  PORDDET.RATE,  " + 
					"PORDDET.DISCOUNT,   PORDDET.REQ_DATE,   PORDDET.STATUS,   PORDDET.STATUS_DATE,   PORDDET.LOC_CODE,   PORDDET.TAX_CLASS,   PORDDET.TAX_CHAP,   PORDDET.TAX_ENV,   " +   
					"PORDDET.REMARKS,  PORDDET.WORK_ORDER,   PORDDET.TAX_AMT,   PORDDET.TOT_AMT,   PORDDET.DLV_DATE,   PORDDET.DLV_QTY,   PORDDET.UNIT__RATE,   PORDDET.CONV__QTY_STDUOM,    " +  
					"PORDDET.CONV__RTUOM_STDUOM,   PORDDET.UNIT__STD,   PORDDET.QUANTITY__STDUOM,   PORDDET.RATE__STDUOM,   PORDDET.PACK_CODE,   PORDDET.NO_ART,   PORDDET.PACK_INSTR,     " + 
					"PORDDET.ACCT_CODE__DR,   PORDDET.CCTR_CODE__DR,   PORDDET.ACCT_CODE__CR,   PORDDET.CCTR_CODE__CR,   ITEM_A.DESCR,   PORDDET.DISCOUNT_TYPE,   PORDDET.SUPP_CODE__MNFR,  " +    
					"PORDDET.ORDER_OPT,   PORDDET.BOM_CODE,   PORDDET.LINE_NO__SORD,   PORDDET.RATE__CLG,   '' contract_detail,   PORDDET.OP_REASON,   PORDDET.USER_ID__OP,   PORDDET.SPECIFIC_INSTR,   " +   
					"PORDDET.SPECIAL_INSTR,   PORDDET.EMP_CODE__QCAPRV,   EMPLOYEE.EMP_FNAME,   PORDDET.PROJ_CODE,   PORDDET.ITEM_CODE__MFG,   ITEM_B.DESCR,   PORDDET.SPEC_REF,   " +   
					"SPECIFICATION.DESCR,   PORDDET.OPERATION,   PORDDET.STD_RATE,EMPLOYEE.EMP_MNAME,   EMPLOYEE.EMP_LNAME,     PORDER.EXCH_RATE,   PORDDET.DEPT_CODE,     " + 
					"PORDDET.BENEFIT_TYPE,   PORDDET.LICENCE_NO,   PORDDET.ACCT_CODE__PROV_DR,   PORDDET.CCTR_CODE__PROV_DR,   PORDDET.ACCT_CODE__PROV_CR,   PORDDET.CCTR_CODE__PROV_CR,   " +   
					"PORDDET.SPEC_METADATA,   PORDDET.SPEC_DIMENSION,   PORDDET.SUPP_ITEM__REF,   PORDDET.QUANTITY__FC,   PORDDET.PRD_CODE__RFC,   PORDDET.FORM_NO,   PORDDET.DUTY_PAID,   " +   
					"(porddet.quantity * porddet.std_rate ) as std_cost,    " + 
					"PORDDET.ANAL_CODE,   ANALYSIS.DESCR,   PORDDET.ACCT_CODE__AP_ADV,   PORDDET.CCTR_CODE__AP_ADV,   PORDDET.LOT_NO__PASSIGN,   PORDDET.EXP_DATE__PASSIGN   " + 
					"FROM PORDDET,   ITEM ITEM_A,   EMPLOYEE,  ITEM ITEM_B,   SPECIFICATION,   PORDER,   ANALYSIS    " + 
					"WHERE ( porddet.emp_code__qcaprv = employee.emp_code (+)) and    " + 
					"( porddet.item_code__mfg = item_b.item_code (+)) and    " + 
					"( porddet.spec_ref = specification.spec_ref (+)) and    " + 
					"( porddet.anal_code = analysis.anal_code (+)) and    " + 
					"( PORDDET.ITEM_CODE = ITEM_A.ITEM_CODE ) and    " + 
					"( PORDER.PURC_ORDER = PORDDET.PURC_ORDER ) AND    " + 
					"( ( PORDDET.PURC_ORDER = ?) )     " + 
					"ORDER BY PORDDET.LINE_NO ASC   ";
			pstmtPo = conn.prepareStatement(sql);
			pstmtPo.setString(1, pOrder);
			rsPo = pstmtPo.executeQuery();
			lineNo = 1;
			while(rsPo.next())
			{
				System.out.println("INSIDE WHILE LOOP........");
				bom = rsPo.getString("BOM_CODE");
				itemCode = checkNull(rsPo.getString("ITEM_CODE"));
				//site = rs.getString("SITE_CODE");
				refQty = rsPo.getDouble("QUANTITY");
				lineNoref = rsPo.getInt("LINE_NO");
				System.out.println("bom"+bom + "itemCode"+itemCode );
				bomXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Root><Detail1>";
				bomXml = bomXml + "<site_code>" + pordSite + "</site_code>\r\n";
				bomXml = bomXml + "<item_code>" +itemCode + "</item_code>\r\n";
				bomXml = bomXml + "<quantity>" + refQty + "</quantity>\r\n";
				bomXml = bomXml + "<bom_code>" + bom + "</bom_code>\r\n";
				bomXml = bomXml + "<exp_lev>" + 1 + "</exp_lev>\r\n";
				bomXml = bomXml + "<work_order>" + "XYZ" + "</work_order>\r\n";
				bomXml = bomXml + "</Detail1>\r\n</Root>";
				System.out.println("bomXml is =" + bomXml);

                //xmlStringFrmBom = explodeBom(bomXml,bom,"1.","B","XYZ",conn);
                bomXml = explodeBomStr(bomXml,bom,"1.","B","XYZ",conn);//added by monika salla on 7 august 2020
				//xmlStringFrmBom = bomXml.toString(); 
                System.out.println("xmlStringBom--->"+xmlStringBom +"bomXml.size"+bomXml.length());
                

                //if (xmlStringFrmBom == -1)
                if(xmlStringBom==null ||xmlStringBom.trim().length()==0 ) 
				{
					errString = itmDBAccessLocal.getErrorString("", "Failed to explode Bill of Material", "","",conn);
					return errString;
				}
				domBom = genericUtility.parseString(bomXml);
				detlList = domBom.getElementsByTagName("Detail1");		
				System.out.println("detlList>>>["+detlList + "]["+detlList.getLength() + "]");
				if (detlList != null)
				{

					System.out.println("INSIDE LOOP...");
					for(int cntr = 0; cntr < detlList.getLength(); cntr++)
					{
						System.out.println("INSIDE LOOP 11...");

						System.out.println(" in for Loop=");
						itemCodeDet = genericUtility.getColumnValueFromNode("item_code", detlList.item(cntr));

						qtyDetStr = genericUtility.getColumnValueFromNode("quantity", detlList.item(cntr));
						if(qtyDetStr != null && qtyDetStr.trim().length() > 0)
						{
							qtyDet = Double.parseDouble(qtyDetStr);
							System.out.println("qtyDet"+qtyDet);
						}
						System.out.println("qtyDet"+qtyDet +"qtyDetStr"+qtyDetStr);
						xmlBuff.append("<Detail2 dbID='' domID=\""+lineNo +"\" objName=\"dist_order\" objContext=\"2\">"); 
						xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" updateFlag=\"A\" status=\"N\" />");
						xmlBuff.append("<line_no><![CDATA["+lineNo +"]]></line_no>");
						xmlBuff.append("<item_code><![CDATA["+itemCodeDet +"]]></item_code>");
						xmlBuff.append("<qty_order><![CDATA["+qtyDet +"]]></qty_order>");
						xmlBuff.append("<qty_confirm><![CDATA["+qtyDet +"]]></qty_confirm>");
						xmlBuff.append("<qty_shipped><![CDATA["+ 0 +"]]></qty_shipped>");
						xmlBuff.append("<qty_received><![CDATA["+0 +"]]></qty_received>");
						xmlBuff.append("<qty_return><![CDATA["+0 +"]]></qty_return>");
						xmlBuff.append("<rate><![CDATA["+0 +"]]></rate>");


						sql1 = "select unit,unit__sal from item where item_code = ?  ";
						pstmt1 = conn.prepareStatement(sql1);
						pstmt1.setString(1, itemCodeDet);
						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{
							unit = rs1.getString("unit");
							unitSal = rs1.getString("unit__sal");
						}
						rs1.close();rs1 = null;
						pstmt1.close();pstmt1 = null;

						if(unitSal == null || unitSal.trim().length() == 0)
						{
							unitSal = unit ;
						}
						xmlBuff.append("<unit><![CDATA["+unit +"]]></unit>");
						xmlBuff.append("<unit__alt><![CDATA["+unitSal +"]]></unit__alt>");

						fact = 0 ;

						convQuantityFactArryList = distCommon.getConvQuantityFact(unitSal,unit,itemCodeDet,qtyDet,fact,conn);
						System.out.println("convQuantityFactArryList"+convQuantityFactArryList);		
						xmlBuff.append("<conv__qty__alt><![CDATA["+fact +"]]></conv__qty__alt>");
						xmlBuff.append("<qty_order__alt><![CDATA["+convQuantityFactArryList.get(0) +"]]></qty_order__alt>");
						xmlBuff.append("<line_no__pord><![CDATA["+lineNoref +"]]></line_no__pord>");

						System.out.println("...............distOrder111"+distOrder);
						xmlBuff.append("<dist_order/>");
						xmlBuff.append("</Detail2>");
						lineNo++ ;
					}
				}
			}
			rsPo.close();rsPo = null;
			pstmtPo.close();pstmtPo = null;

			xmlBuff.append("</Header0>");
			xmlBuff.append("</group0>");
			xmlBuff.append("</DocumentRoot>");
			xmlString = xmlBuff.toString();
			System.out.println("@@@@@2: xmlString:"+xmlBuff.toString());
			System.out.println("...............just before savdata distorder()");
			siteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			System.out.println("== site code =="+siteCode);
			System.out.println("userId@1642["+userId+"]");
			errString = saveData(siteCode,xmlString,userId, conn);
			System.out.println("@@@@@2: retString:"+errString);
			System.out.println("--retString finished--");
			if (errString.indexOf("Success") > -1)
			{
				System.out.println("@@@@@@3: Success"+errString);
				//conn.commit();
				//errString = "";
			}
			else
			{
				System.out.println("[SuccessSuccess" + errString + "]");	
				conn.rollback();
				return errString;
			}

		}
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new ITMException(e);
		} 
		finally
		{
			try
			{
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if(pstmt1 != null)
				{
					pstmt1.close();
					pstmt1 = null;					
				}
				if(rs != null)
				{
					rs.close();
					rs = null;
				}
				if(rs1 != null)
				{
					rs1.close();
					rs1 = null;
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
	private String saveData(String siteCode,String xmlString,String userId, Connection conn) throws ITMException
	{
		System.out.println("saving data...........");
		InitialContext ctx = null;
		String retString = null;
		MasterStatefulLocal masterStateful = null; // for ejb3
		try
		{
			AppConnectParm appConnect = new AppConnectParm();
			ctx = new InitialContext(appConnect.getProperty());
			masterStateful = (MasterStatefulLocal)ctx.lookup("ibase/MasterStatefulEJB/local");
			System.out.println("-----------masterStateful------- " + masterStateful);
			String [] authencate = new String[2];
			//Changes By Ajay on 25-12-2017:START
			authencate[0] = userId;
			//Changes  By Ajay on 25-12-2017:END
			authencate[1] = "";
			System.out.println("xmlString to masterstateful [" + xmlString + "]");
			retString = masterStateful.processRequest(authencate, siteCode, true, xmlString,true,conn);
            System.out.println("--retString - -"+retString);
            
           //masterStatefulLocal = (MasterStatefulLocal)ctx.lookup("ibase/MasterStatefulEJB/local"); 
			//System.out.println("masterstatement process request....qqq:: "+masterStatefulLocal);
		    //retString = masterStatefulLocal.processRequest( authencate, siteCode, true, xmlString.toString(),false,conn);
			
            
            
		}
		catch(ITMException itme)
		{
			System.out.println("ITMException :CreateDistOrder :saveData :==>");
			throw itme;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception :CreateDistOrder :saveData :==>");
			throw new ITMException(e);
		}
		return retString;
	}

	private int explodeBom(String xmlString, String bom ,String explevel,String asType,String alLine,Connection conn) throws Exception 
	{
		System.out.println("Inside explodeBom...........");
		String errString = "",siteCode= "",sql = "",sql1 = "",sql2 = "" ,itemCode = "",sQuantity =  "",
				bomCode = "",newBomItem = "" ,newBom= "",itemSch= "",mfgLeadTimeStr = "",qcLeadTimeStr = "",dueDateStr ="",
				tranSer ="", mexptype="" ,tranId = "";
		String itemCodeBomDet = "";
		String unit = "";
		String reqType = "";
		String itemRef = "";
		String suppSour = "";
		String suppSourItem = "";
		String lineTypeInner = "";
		String sDueDate = "";
		String isDueDate = "";
		String critItem= "";
		String exp_lev = "";
		String xml_exp_lev ="";
		String workOrder = "";
		int cnt1 = 0,operation = 0,rowNo = 0,liRet = 0;
		//StringBuffer valueXmlString = new StringBuffer("<Root>");
		SimpleDateFormat simpleDateFormat = null;
		double perQty = 0,intQty =0,reoQty =0;
		double minQty = 0;
		double quantity = 0;
		double quantityUpdate = 0;
		double batchQty = 0;
		double 	 mfgLeadTime =0;
		double 	 qcLeadTime =0;
		double 	 leadTime =0;
		double purLeadTime  = 0;
		//boolean lbExplode = false;
		int  ctr = 0;
		int iLeadTime = 0;
		String tempXML = null;
		java.sql.Timestamp dueDate =  null;
		//java.sql.Timestamp idueDate =  null;
		Document dom;
		int pevoper = 0;
		PreparedStatement pstmt = null,pstmt1=null ,pstmt2=null;
		ResultSet rs= null ,rs1 = null ,rs2 = null;
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		try
		{
			dom = genericUtility.parseString(xmlString);
			siteCode = checkNull(genericUtility.getColumnValue("site_code",dom));
			itemCode = checkNull(genericUtility.getColumnValue("item_code",dom));
			sQuantity = checkNull(genericUtility.getColumnValue("quantity",dom));
			bomCode = checkNull(genericUtility.getColumnValue("bom_code",dom));
			quantity = Double.parseDouble(sQuantity);
			bomCode = checkNull(genericUtility.getColumnValue("bom_code",dom));
			suppSour = checkNull(genericUtility.getColumnValue("supp_sour",dom));
			mfgLeadTimeStr = genericUtility.getColumnValue("mfg_lead_time",dom); 
			if(mfgLeadTimeStr != null && mfgLeadTimeStr.trim().length()> 0 )
			{
				mfgLeadTime = Double.parseDouble(mfgLeadTimeStr);
			}
			qcLeadTimeStr =  genericUtility.getColumnValue("qc_lead_time",dom);
			if(qcLeadTimeStr != null && qcLeadTimeStr.trim().length()> 0 )
			{
				qcLeadTime = Double.parseDouble(qcLeadTimeStr);
			}
			System.out.println("mfgLeadTimeStr"+mfgLeadTimeStr+"qcLeadTimeStr"+qcLeadTimeStr);
			sDueDate = checkNull(genericUtility.getColumnValue("due_date",dom));

			System.out.println("Due Date..........["+sDueDate+"]");

			if(sDueDate != null && sDueDate.trim().length() > 0)
			{	
				dueDateStr = genericUtility.getValidDateString(sDueDate, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
				System.out.println("dueDateStr Date.........."+dueDateStr);
				dueDate = java.sql.Timestamp.valueOf(dueDateStr + " 00:00:00");
				leadTime = mfgLeadTime + qcLeadTime;
				iLeadTime = (int)leadTime;
				System.out.println("Due Date Before.........."+dueDate +"leadTime>>"+leadTime +"mfgLeadTime"+mfgLeadTime + "qcLeadTime"+qcLeadTime) ;
				dueDate = utilMethods.RelativeDate(dueDate, iLeadTime * -1);
				System.out.println("Due Date After..........."+dueDate) ;
				simpleDateFormat = new SimpleDateFormat(genericUtility.getDBDateFormat());
				sDueDate = simpleDateFormat.format(dueDate);

			}
			System.out.println("dueDatedueDate Date.........."+dueDate);


			sql2 = "select a.item_code,a.item_ref from bomdet a , siteitem b 	 " + 
					" where a.bom_code  = ?  and   a.item_code = b.item_code "	+ 
					" and   b.site_code = ?  and nvl(b.active,'Y') = 'Y' "  + 
					" order by a.operation, a.item_ref, a.item_code ";
			pstmt2 = conn.prepareStatement(sql2);
			pstmt2.setString(1,bomCode);
			pstmt2.setString(2,siteCode);
			rs2 = pstmt2.executeQuery();
			System.out.println("Exploding " + siteCode + " " + itemCode + "  " + bomCode +" for " + sQuantity );
			rowNo = 1;
			while (rs2.next())
			{

				itemCodeBomDet = checkNull(rs2.getString(1));
				itemRef = checkNull(rs2.getString(2));

				//lbExplode = false ;
				System.out.println("itemCodeBomDet>>>"+itemCodeBomDet +"itemRef"+itemRef);
				if("B".equalsIgnoreCase(asType))
				{
					sql1 = " select bom.unit			, bom.batch_qty	, bomdet.item_ref, bomdet.qty_per	, bomdet.req_type	, bomdet.min_qty	, 	" +
							" bomdet.operation , bomdet.crit_item from   bom, bomdet " +
							" where  bomdet.bom_code  = ?  and  	 bomdet.item_code = ? " +
							" and (bomdet.item_ref  = ? or bomdet.item_ref is null )  " +
							" and bom.bom_code 	   = bomdet.bom_code ";
					pstmt1 = conn.prepareStatement(sql1);
					pstmt1.setString(1, bomCode);
					pstmt1.setString(2, itemCodeBomDet);
					pstmt1.setString(3, itemRef);
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						unit = checkNull(rs1.getString(1));
						batchQty =	rs1.getDouble(2);	
						itemRef	=	checkNull(rs1.getString(3));
						perQty	=	rs1.getDouble(4);
						reqType	=	checkNull(rs1.getString(5));
						minQty	=	rs1.getDouble(6);
						operation	= rs1.getInt(7);
						//crIt =	rs1.getString(8);
					}
					rs1.close();rs1 = null;
					pstmt1.close();pstmt1 = null;
					System.out.println("operation>>>"+operation );

					if(pevoper != operation)
					{
						ctr =1;
						pevoper = operation;
						System.out.println("pevoper........." +pevoper);
					}

					if(exp_lev == null)
					{
						xml_exp_lev = operation +"."+ctr+".";
					}else
					{
						xml_exp_lev = xml_exp_lev +"."+ctr+".";
					}
					ctr++;
					System.out.println("xml_exp_lev :" + xml_exp_lev);

					System.out.println("perQty :" + perQty +"batchQty :"+batchQty + "quantity : "+quantity);
					if (reqType.equals("S"))
					{
						quantityUpdate = (quantity / batchQty) * perQty;
					}
					else if (reqType.equals("P"))
					{
						quantityUpdate = (perQty / batchQty) * quantity;
					}
					else if (reqType.equals("F"))
					{
						quantityUpdate = perQty;
					}
					System.out.println("Qty :" + quantity + " Batch Qty :" + batchQty + " Item Ref : " + itemRef + " Qty Per " + perQty + " Req Type " + reqType + " Qty Update :" + quantityUpdate);

					// quantityUpdate = itmDBAccessEJB.getRndamt(quantityUpdate, round, roundTo);

					if (quantityUpdate < minQty)
					{
						quantityUpdate = minQty;
					}
				}
				else
				{
					quantityUpdate = quantity;

					sql = "select unit from item where item_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCodeBomDet);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						unit = checkNull(rs.getString("unit"));
					}
					else
					{
						sql = "select item.unit from from  item , bom where bom.item_code = item.item_code and   bom.bom_code  = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCodeBomDet);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							unit = checkNull(rs.getString(1));
						}
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;
				}

				sql = "select item_ser, supp_sour , bom_code from  item where item_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, itemCodeBomDet);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					//itemSer = rs.getString("item_ser");
					suppSourItem = checkNull(rs.getString("supp_sour"));
					newBomItem = checkNull(rs.getString("bom_code"));
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;

				sql =" select supp_sour, bom_code ,qc_lead_time ,mfg_lead_time,pur_lead_time,integral_qty,reo_qty ,master_sch " +
						" from  siteitem where site_code = ? and   item_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, siteCode);
				pstmt.setString(2, itemCodeBomDet);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					suppSour = checkNull(rs.getString("supp_sour"));
					newBom = checkNull(rs.getString("bom_code"));
					qcLeadTime = rs.getDouble("qc_lead_time");
					mfgLeadTime = rs.getDouble("mfg_lead_time");
					purLeadTime = rs.getDouble("pur_lead_time");
					intQty = rs.getDouble("integral_qty");
					reoQty = rs.getDouble("reo_qty");
					itemSch = checkNull(rs.getString("master_sch"));

				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;

				if( suppSour == null || suppSour.trim().length() == 0 )
				{
					suppSour = suppSourItem ;
				}
				if("M".equalsIgnoreCase(suppSour))
				{
					tranSer = "W-ORD";
				}
				else if("D".equalsIgnoreCase(suppSour))
				{
					tranSer = "D-DEM";
				}
				else if( "P".equalsIgnoreCase(suppSour) || "S".equalsIgnoreCase(suppSour))
				{
					tranSer = "IND";
				}

				if( newBom == null || newBom.trim().length() == 0 )
				{
					newBom = newBomItem ;
				}

				sql = "select count(*) from  item where item_code  = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, itemCodeBomDet);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					cnt1  = rs.getInt(1);
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;

				if(cnt1 == 0)
				{
					sql = "select count(*) from  bom where bom_code  = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCodeBomDet);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						cnt1  = rs.getInt(1);
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;

					if(cnt1 != 0)
					{
						mexptype = "B" ;
						//lbExplode = true ;
					}
				}
				else
				{
					sql = "select item_stru from item where item_code  = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCodeBomDet);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						mexptype  = checkNull(rs.getString(1));
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;
					System.out.println("mexptype>>"+mexptype);
					if(mexptype == null || !"F".equalsIgnoreCase(mexptype))
					{
						mexptype = "I" ;
					}
				}
				System.out.println("intQty>>"+intQty+ "reoQty"+reoQty);
				if(intQty == 0 || Double.toString(intQty) == null)
				{
					intQty = 1;
				}
				if(reoQty == 0 || Double.toString(reoQty) == null)
				{
					reoQty = 1;
				}

				/*valueXmlString.append("<Detail>");
				valueXmlString.append("<work_order>").append("<![CDATA[").append(workOrder).append("]]>").append("</work_order>");
				valueXmlString.append("<site_code>").append("<![CDATA[").append(siteCode).append("]]>").append("</site_code>");
				valueXmlString.append("<bom_code>").append("<![CDATA[").append(bomCode).append("]]>").append("</bom_code>");
				valueXmlString.append("<item_code>").append("<![CDATA[").append(itemCodeBomDet).append("]]>").append("</item_code>");
				valueXmlString.append("<item_Ref>").append("<![CDATA[").append(itemRef).append("]]>").append("</item_Ref>");
				valueXmlString.append("<quantity_Ref>").append("<![CDATA[").append(sQuantity).append("]]>").append("</quantity_Ref>");	
				valueXmlString.append("<quantity>").append("<![CDATA[").append(quantityUpdate).append("]]>").append("</quantity>");	  
				simpleDateFormat = new SimpleDateFormat(genericUtility.getDBDateFormat());
				//isDueDate = simpleDateFormat.format(idueDate);
				valueXmlString.append("<due_date>").append("<![CDATA[").append(isDueDate).append("]]>").append("</due_date>");		
				valueXmlString.append("<exp_lev>").append("<![CDATA[").append(xml_exp_lev).append("]]>").append("</exp_lev>");		  
				valueXmlString.append("<operation>").append("<![CDATA[").append(operation).append("]]>").append("</operation>");	 
				valueXmlString.append("<crit_Item>").append("<![CDATA[").append(critItem).append("]]>").append("</crit_Item>");		 
				valueXmlString.append("<line_type>").append("<![CDATA[").append(lineTypeInner).append("]]>").append("</line_type>");
				valueXmlString.append("<unit>").append("<![CDATA[").append(unit).append("]]>").append("</unit>");
				valueXmlString.append("<batch_qty>").append("<![CDATA[").append(batchQty).append("]]>").append("</batch_qty>");
				valueXmlString.append("<qty_per>").append("<![CDATA[").append(perQty).append("]]>").append("</qty_per>");
				valueXmlString.append("<req_type>").append("<![CDATA[").append(reqType).append("]]>").append("</req_type>");
				valueXmlString.append("</Detail>");*/

				System.out.println("MAID DETAil....");
				tranId = "SYSF" + rowNo ;
				System.out.println("tranId ...."+tranId);
				simpleDateFormat = new SimpleDateFormat(genericUtility.getDBDateFormat());
				//sDueDate = simpleDateFormat.format(dueDate);
				StringBuffer tempXMLString = new StringBuffer("<Root>");
				tempXMLString.append("<DetailBom>");
				tempXMLString.append("<site_code>").append("<![CDATA[").append(siteCode).append("]]>").append("</site_code>");
				tempXMLString.append("<tran_id>").append("<![CDATA[").append(tranId).append("]]>").append("</tran_id>");  
				tempXMLString.append("<tran_ser>").append("<![CDATA[").append(tranSer).append("]]>").append("</tran_ser>");
				tempXMLString.append("<item_code>").append("<![CDATA[").append(itemCodeBomDet).append("]]>").append("</item_code>");
				tempXMLString.append("<tran_date>").append("<![CDATA[").append(dueDate).append("]]>").append("</tran_date>");
				tempXMLString.append("<quantity>").append("<![CDATA[").append(quantityUpdate).append("]]>").append("</quantity>");
				tempXMLString.append("<supply>").append("<![CDATA[").append(0).append("]]>").append("</supply>");
				tempXMLString.append("<demand>").append("<![CDATA[").append(quantityUpdate).append("]]>").append("</demand>");
				tempXMLString.append("<item_stru>").append("<![CDATA[").append(mexptype).append("]]>").append("</item_stru>");
				tempXMLString.append("<supp_sour>").append("<![CDATA[").append(suppSour).append("]]>").append("</supp_sour>");
				tempXMLString.append("<demand>").append("<![CDATA[").append(quantityUpdate).append("]]>").append("</demand>");
				tempXMLString.append("<bom_code>").append("<![CDATA[").append(newBom).append("]]>").append("</bom_code>");		 
				tempXMLString.append("<status>").append("<![CDATA[").append("N").append("]]>").append("</status>");	 
				tempXMLString.append("<crt_ord>").append("<![CDATA[").append("N").append("]]>").append("</crt_ord>");		 
				tempXMLString.append("<line_type>").append("<![CDATA[").append(lineTypeInner).append("]]>").append("</line_type>");
				tempXMLString.append("<due_date>").append("<![CDATA[").append(isDueDate).append("]]>").append("</due_date>");
				tempXMLString.append("<operation>").append("<![CDATA[").append(operation).append("]]>").append("</operation>");	   
				tempXMLString.append("<exp_lev>").append("<![CDATA[").append(xml_exp_lev).append("]]>").append("</exp_lev>");			 
				tempXMLString.append("<crit_Item>").append("<![CDATA[").append(critItem).append("]]>").append("</crit_Item>");			
				tempXMLString.append("<mfg_lead_time>").append("<![CDATA[").append(mfgLeadTime).append("]]>").append("</mfg_lead_time>");
				tempXMLString.append("<qc_lead_time>").append("<![CDATA[").append(qcLeadTime).append("]]>").append("</qc_lead_time>");
				tempXMLString.append("<pur_lead_time>").append("<![CDATA[").append(purLeadTime).append("]]>").append("</pur_lead_time>");
				tempXMLString.append("<integral_qty>").append("<![CDATA[").append(intQty).append("]]>").append("</integral_qty>");
				tempXMLString.append("<reo_qty>").append("<![CDATA[").append(reoQty).append("]]>").append("</reo_qty>");
				tempXMLString.append("<item_sch>").append("<![CDATA[").append(itemSch).append("]]>").append("</item_sch>");
				tempXMLString.append("</DetailBom>");
				tempXMLString.append("</Root>");
				rowNo++;
				System.out.println("tempXMLString>>"+tempXMLString.toString() +"itemCodeBomDet"+itemCodeBomDet);
				liRet = explodeBom(tempXMLString.toString(),itemCodeBomDet,"1","B","XYZ",conn); 
				if(liRet == -1)
				{
					errString = itmDBAccessLocal.getErrorString("", "VTEXPBOM", "","",conn);
				}
				/*if ( errString != null && errString.trim().length() > 0 )
				{
					System.out.println("Error While Exploding Bom Further:..."+errString);
					//return errString;
					errString = errString.replaceAll("<Root>","");
					errString = errString.replaceAll("</Root>","");
					valueXmlString.append(errString);
					errString = "";
				}*/
			}
			rs2.close();rs2 = null;
			pstmt2.close();pstmt2 = null;
			//valueXmlString.append("</Root>");

		}
		catch(SQLException se)
		{
			System.out.println("SQLException : ExplodeBom :explodeBom()" +se.getMessage());
			se.printStackTrace();
			errString = se.getMessage();
			throw new ITMException(se);

		}
		catch(Exception e)
		{
			System.out.println("Exception :ExplodeBom :explodeBom():" + e.getMessage() + ":");
			errString = genericUtility.createErrorString(e);
			e.printStackTrace();
			errString = e.getMessage();
			throw new ITMException(e);


		}
		finally
		{
			try{
				if(rs != null)
				{
					rs.close();
					rs = null;
				}

				if (pstmt != null) 
				{ 
					pstmt.close();
					pstmt = null; 
				}

				/*if(conn != null)
			{
				conn.close();
				conn = null;
			}*/
			}catch(Exception e)
			{
				System.out.println("Exception :conf ::" + e.getMessage() + ":");
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		System.out.println("errString.toString()>>"+errString);

		if(errString != null && errString.trim().length() > 0)
		{
			return -1;
		}
		else
		{
			return 1;
		}
    }
  
 //added by monika salla on  7 august 2020     
    public String explodeBomStr(String xmlString, String bom ,String explevel,String asType,String alLine,Connection conn) throws Exception 
	{
		System.out.println("Inside explodeBom String...........");
		String errString = "",siteCode= "",sql = "",sql1 = "",sql2 = "" ,itemCode = "",sQuantity =  "",
				bomCode = "",newBomItem = "" ,newBom= "",itemSch= "",mfgLeadTimeStr = "",qcLeadTimeStr = "",dueDateStr ="",
				tranSer ="", mexptype="" ,tranId = "";
		String itemCodeBomDet = "";
		String unit = "";
		String reqType = "";
		String itemRef = "";
		String suppSour = "";
		String suppSourItem = "";
		String lineTypeInner = "";
		String sDueDate = "";
		String isDueDate = "";
		String critItem= "";
		String exp_lev = "";
		String xml_exp_lev ="";
		String workOrder = "";
		int cnt1 = 0,operation = 0,rowNo = 0,liRet = 0;
		StringBuffer valueXmlString = new StringBuffer("<Root>");
		SimpleDateFormat simpleDateFormat = null;
		double perQty = 0,intQty =0,reoQty =0;
		double minQty = 0;
		double quantity = 0;
		double quantityUpdate = 0;
		double batchQty = 0;
		double 	 mfgLeadTime =0;
		double 	 qcLeadTime =0;
		double 	 leadTime =0;
		double purLeadTime  = 0;
		//boolean lbExplode = false;
		int  ctr = 0;
		int iLeadTime = 0;
		String tempXML = null;
		java.sql.Timestamp dueDate =  null;
		//java.sql.Timestamp idueDate =  null;
		Document dom;
		int pevoper = 0;
		PreparedStatement pstmt = null,pstmt1=null ,pstmt2=null;
		ResultSet rs= null ,rs1 = null ,rs2 = null;
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		try
		{
			dom = genericUtility.parseString(xmlString);
			siteCode = checkNull(genericUtility.getColumnValue("site_code",dom));
			itemCode = checkNull(genericUtility.getColumnValue("item_code",dom));
			sQuantity = checkNull(genericUtility.getColumnValue("quantity",dom));
			bomCode = checkNull(genericUtility.getColumnValue("bom_code",dom));
			quantity = Double.parseDouble(sQuantity);
			

			sql2 = "select a.item_code,a.item_ref , b.batch_qty,  a.qty_per	, a.req_type	, a.min_qty from bomdet a  , bom b 	 " + 
					" where a.bom_code = b.bom_code and a.bom_code  = ? " ;
			pstmt2 = conn.prepareStatement(sql2);
			pstmt2.setString(1,bomCode);
			rs2 = pstmt2.executeQuery();
			System.out.println("Exploding " + siteCode + " " + itemCode + "  " + bomCode +" for " + sQuantity );
			rowNo = 1;
			while (rs2.next())
			{

				itemCodeBomDet = rs2.getString(1);
				itemRef = rs2.getString(2);
				batchQty =	rs2.getDouble(3);	
				perQty	=	rs2.getDouble(4);
				reqType	=rs2.getString(5);
				minQty	=	rs2.getDouble(6);
				if (reqType.equals("S"))
				{
					quantityUpdate = (quantity / batchQty) * perQty;
				}
				else if (reqType.equals("P"))
				{
					quantityUpdate = (perQty / batchQty) * quantity;
				}
				else if (reqType.equals("F"))
				{
					quantityUpdate = perQty;
				}
				System.out.println("Qty :" + quantity + " Batch Qty :" + batchQty + " Item Ref : " + itemRef + " Qty Per " + perQty + " Req Type " + reqType + " Qty Update :" + quantityUpdate);

				if (quantityUpdate < minQty)
				{
					quantityUpdate = minQty;
				}
				valueXmlString.append("<Detail1>");
				valueXmlString.append("<item_code>").append("<![CDATA[").append(itemCodeBomDet).append("]]>").append("</item_code>");
				valueXmlString.append("<quantity>").append("<![CDATA[").append(quantityUpdate).append("]]>").append("</quantity>");
				
				valueXmlString.append("</Detail1>");
			}
			rs2.close();rs2 = null;
			pstmt2.close();pstmt2 = null;
			valueXmlString.append("</Root>");

		}
		catch(SQLException se)
		{
			System.out.println("SQLException : ExplodeBom :explodeBom()" +se.getMessage());
			se.printStackTrace();
			errString = se.getMessage();
			throw new ITMException(se);

		}
		catch(Exception e)
		{
			System.out.println("Exception :ExplodeBom :explodeBom():" + e.getMessage() + ":");
			errString = genericUtility.createErrorString(e);
			e.printStackTrace();
			errString = e.getMessage();
			throw new ITMException(e);


		}
		finally
		{
			try{
				if(rs != null)
				{
					rs.close();
					rs = null;
				}

				if (pstmt != null) 
				{ 
					pstmt.close();
					pstmt = null; 
				}

				/*if(conn != null)
			{
				conn.close();
				conn = null;
			}*/
			}catch(Exception e)
			{
				System.out.println("Exception :conf ::" + e.getMessage() + ":");
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		System.out.println("errString.toString()>>"+errString);

			return valueXmlString.toString();
	}
	//end


	private Connection chaneParnerExist(String despId,String disLink,String channelPartner,String xtraParams,Connection conn) throws ITMException
	{
		System.out.println("Inside chaneParnerExist...........");
		String purIntegrate="";
		Connection connCP = null;
		ConnDriver connDriver = new ConnDriver();
		DistCommon distCommon = new DistCommon();
		try
		{
			purIntegrate=distCommon.getDisparams("999999", "PUR_INTEGRATED", conn);
			if (("A".equalsIgnoreCase(disLink)|| "S".equalsIgnoreCase(disLink) || "C".equalsIgnoreCase(disLink) ) && "Y".equalsIgnoreCase(purIntegrate))
			{
				String dirPath="";
				if ( CommonConstants.APPLICATION_CONTEXT != null )
				{
					dirPath = CommonConstants.APPLICATION_CONTEXT + CommonConstants.SETTINGS;
					System.out.println("dirPath1>>>>"+dirPath);
				}
				else
				{
					dirPath = CommonConstants.JBOSSHOME + File.separator + "server" + File.separator + "default" + File.separator + "deploy" + File.separator + "ibase.ear" + File.separator + "ibase.war" + File.separator + CommonConstants.SETTINGS;
					System.out.println("dirPath2>>>>>>"+dirPath);
				}
				File xmlFile = new File( dirPath + File.separator + "DriverITMCP" + ".xml" );
				System.out.println("xmlFile>>>>>"+xmlFile);
				if(xmlFile.exists())
				{
					//if(connCP !=null)
					{
						System.out.println("file exist new connection is creating");
						//changes by sarita on 28DEC2017
						/*connCP = connDriver.getConnectDB("DriverITMCP");*/
						connCP = getConnection();
						return connCP;
					}
				}
			}


		}catch(Exception e)
		{
			System.out.println("Exception :conf ::" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return connCP;
	}
	private String gbfCreateSordPord(String pOrder  ,String xtraParams,Connection conn ,Connection connCp ) throws ITMException 
	{
		System.out.println("Inside gbfCreateSordPord...........");
		String errString = "",sql = "",sql1 = "",suppCode= "",pdlvSite = "",	itemSer = "",orderType = "",priceList = "",pricelistClg = "",
				currCode= "" ,keyStr ="" ,custCode="" ,countCode = "", remarks= "",
				priceListDiscount = "",isVAr = "" ,custCodeBil = "" ,				taxClass = "" ,				taxChap = "" ,crTerm = "" ,salesPers1 = "" ,salesPers2 = "" ,addr1 = "" ,addr2 = "" ,
				city = "" ,pin = "" ,stanCode = "" , tranCode = "" , addr3 = "" ,salesPers = "", commPerc= "" ,commPercon = "",currcodeComm = "",
				bankCode = "" ,	transMode = "" , rcpMode = "" ,stateCode = "" ,tele1 = "" ,				tele2 = "" ,dlvto = "",custCodeend = "", saleOrderend="",
				tele3 = "" ,fax = "" , marketReg = "" ,dlvTerm = "",frtTerm ="" , currCodefrt = "" ,acctCodesal = "" , cctrCodesal = "",
				saleOrder="", remarks2= "" ,remarks3 ="", itemCode = "",taxEnv = "",prdCodeRfc = "", unit = "", linenopord = "",
				linenosord= "",specificinstr = "",ordType = "",jobWorkType= "",subcontractType = "",lsTaxreq = "",
				itemSerDet= "",itemDescr= "",unitRate = "" ,lsType = "",packCode = "" ,packinstr = "",udfStr1 = "" ,udfStr2 = "", 
				rateStduom= "" , preAssignLot = "",lotNo = "" ,xmlString = "",userId = "" ,termId = "",empCode = "",salesPersMain = "",
				crTermMain = "", channelPartner ="", disLink ="", sordSite ="";


		ArrayList rateStduomArryList = new ArrayList();
		Date pordDate= null ,tranDate = null;
		PreparedStatement pstmt = null,pstmt1=null;
		int cnt = 0,lineNo = 0;
		String lineNoStr="";
		double  exchRateComm = 0.0 ,exchRatefrt = 0.0 ,exchRate = 0.0,rate = 0.0 ,discount= 0.0 , 
				rateClg= 0.0 ,quantityFc = 0.0 ,quantity = 0.0 , rateConv= 0.0 , rateDiff= 0.0 ,taxAmt = 0.0 , totAmt = 0.0 ,
				totAmtHdr = 0.0,taxAmtHdr = 0.0,ordAmtHdr = 0.0;
		ResultSet rs= null ,rs1 = null;
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		StringBuffer xmlBuff = null  ;
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
			tranDate =  java.sql.Timestamp.valueOf(sdf1.format(new java.util.Date()).toString() + " 00:00:00.0");
			System.out.println("==tranDate=="+tranDate );

			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");  System.out.println("--login code--"+userId);
			termId =  genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId"); System.out.println("--term id--"+termId);
			sql = "SELECT EMP_CODE FROM USERS WHERE CODE = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			rs = pstmt.executeQuery();
			if ( rs.next() )
			{
				empCode = rs.getString("EMP_CODE")== null ?"":rs.getString("EMP_CODE");
			}
			pstmt.close();
			pstmt = null;					
			rs.close();
			rs = null;

			System.out.println("empCode>>>>"+empCode);
			sql = " select supp_code, site_code__dlv, item_ser, ord_date , pord_type, price_list, price_list__clg , curr_code " +
					" from porder	where purc_order = ?";
			pstmt = connCp.prepareStatement(sql);
			pstmt.setString(1,pOrder);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				suppCode = checkNull(rs.getString("supp_code"));
				pdlvSite = checkNull(rs.getString("site_code__dlv"));
				itemSer = checkNull(rs.getString("item_ser"));
				pordDate = rs.getDate("ord_date");
				orderType = checkNull(rs.getString("pord_type"));
				priceList = checkNull(rs.getString("price_list"));
				pricelistClg = checkNull(rs.getString("price_list__clg"));
				currCode = checkNull(rs.getString("curr_code"));
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;

			System.out.println("pordDate"+pordDate);
			sql = "select channel_partner, dis_link, site_code__ch from site_supplier where site_code = ? and supp_code = ?";
			pstmt = connCp.prepareStatement(sql);
			pstmt.setString(1,pdlvSite);
			pstmt.setString(2,suppCode);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				channelPartner = checkNull(rs.getString("channel_partner"));
				disLink = checkNull(rs.getString("dis_link"));
				sordSite = checkNull(rs.getString("site_code__ch"));
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;

			if(channelPartner == null || channelPartner.trim().length() == 0)
			{
				sql1 = "select case when channel_partner is null then 'N' else channel_partner end, dis_link, site_code" +
						" from supplier	where supp_code = ?";
				pstmt1 = connCp.prepareStatement(sql1);
				pstmt1.setString(1,suppCode);
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{
					channelPartner = checkNull(rs1.getString(1));
					disLink = checkNull(rs1.getString("dis_link"));
					sordSite = checkNull(rs1.getString("site_code"));
				}
				rs1.close();rs1 = null;
				pstmt1.close();pstmt1 = null;

			}

			System.out.println("sordSite......."+sordSite);
			if(!channelPartner.equalsIgnoreCase("Y") || (!"A".equalsIgnoreCase(disLink ) && !"S".equalsIgnoreCase(disLink ) && !"C".equalsIgnoreCase(disLink )))
			{
				errString = "";
			}

			sql = "select key_string from transetup where upper(tran_window) = 'W_SORDER' " ;
			pstmt = connCp.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				keyStr = checkNull(rs.getString("key_string"));

			}
			else
			{
				sql1 = "select key_string from transetup where upper(tran_window) = 'GENERAL' ";
				pstmt1 = connCp.prepareStatement(sql1);
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{
					keyStr = checkNull(rs1.getString("key_string"));
				}
				rs1.close();rs1 = null;
				pstmt1.close();pstmt1 = null;
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;


			sql = "select count(*) from site_customer where site_code__ch = ?  and site_code = ? and channel_partner = 'Y'";
			pstmt = connCp.prepareStatement(sql);
			pstmt.setString(1,pdlvSite);
			pstmt.setString(2,sordSite);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				cnt = rs.getInt(1);
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;

			if(cnt == 0)
			{

				sql = "select count(*) from customer where site_code = ? and case when channel_partner is null then 'N' else channel_partner end = 'Y'" ;
				pstmt = connCp.prepareStatement(sql);
				pstmt.setString(1,pdlvSite);
				rs = pstmt.executeQuery();		
				if(rs.next())
				{
					cnt = rs.getInt(1);
				}

				if(cnt > 1)
				{
					errString = itmDBAccessLocal.getErrorString("", "ERRORVTCPC", "","",conn);
					return errString;
				}
				else if(cnt == 0)
				{
					errString = itmDBAccessLocal.getErrorString("", "VTCUSTCD4", "","",conn);
					return errString;
				}
				else if(cnt == 1)
				{
					sql1 = "select cust_code from customer where site_code = ? and channel_partner = 'Y'" ;
					pstmt1 = connCp.prepareStatement(sql1);
					pstmt1.setString(1,pdlvSite);
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						custCode = rs1.getString("cust_code");
					}
					rs1.close();rs1 = null;
					pstmt1.close();pstmt1 = null;
				}
			}
			else if(cnt == 1)
			{
				sql = "select cust_code from site_customer where site_code__ch = ? and site_code = ?  and channel_partner = 'Y'";
				pstmt = connCp.prepareStatement(sql);
				pstmt.setString(1,pdlvSite);
				pstmt.setString(2,sordSite);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					custCode = checkNull(rs.getString("cust_code"));
				}
			}
			if(rs != null) 
			{
				rs.close();rs = null;

			}
			if(pstmt != null) 
			{
				pstmt.close();pstmt = null;
			}

			sql = " select cust_code__bil,cust_name,tax_class,tax_chap,cr_term,sales_pers__1,sales_pers,sales_pers__2,addr1, addr2, city, pin," +
					" count_code,stan_code, 	tran_code, addr3, bank_code, trans_mode, rcp_mode," +
					" state_code,tele1,tele2,	tele3,fax, market_reg from customer where cust_code = ? ";
			pstmt = connCp.prepareStatement(sql);
			pstmt.setString(1,custCode);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				custCodeBil = checkNull(rs.getString("cust_code__bil"));
				dlvto = checkNull(rs.getString("cust_name"));
				taxClass = checkNull(rs.getString("tax_class"));
				taxChap = checkNull(rs.getString("tax_chap"));
				crTermMain = checkNull(rs.getString("cr_term"));
				salesPersMain = checkNull(rs.getString("sales_pers"));
				salesPers1 = checkNull(rs.getString("sales_pers__1"));
				salesPers2 = checkNull(rs.getString("sales_pers__2"));
				addr1 = checkNull(rs.getString("addr1"));
				addr2 = checkNull(rs.getString("addr2"));
				city = checkNull(rs.getString("city"));
				pin = checkNull(rs.getString("pin"));
				countCode = checkNull(rs.getString("count_code")); 
				stanCode = checkNull(rs.getString("stan_code")); 
				tranCode = checkNull(rs.getString("tran_code")); 
				addr3 = checkNull(rs.getString("addr3"));
				bankCode = checkNull(rs.getString("bank_code")); 
				transMode = checkNull(rs.getString("trans_mode")); 
				rcpMode = checkNull(rs.getString("rcp_mode")); 
				stateCode = checkNull(rs.getString("state_code"));
				tele1 = checkNull(rs.getString("tele1"));
				tele2 = checkNull(rs.getString("tele2"));
				tele3 = checkNull(rs.getString("tele3"));
				fax = checkNull(rs.getString("fax")); 
				marketReg = rs.getString("market_reg");
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;

			System.out.println("tranDate >>>"+tranDate +"SDF FORMat"+sdf.format(tranDate));
			xmlBuff = new StringBuffer();
			System.out.println("--XML CREATION --");
			xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
			xmlBuff.append("<DocumentRoot>");
			xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
			xmlBuff.append("<group0>");
			xmlBuff.append("<description>").append("Group0 description").append("</description>");
			xmlBuff.append("<Header0>");
			xmlBuff.append("<objName><![CDATA[").append("sorder").append("]]></objName>");  
			xmlBuff.append("<pageContext><![CDATA[").append("1").append("]]></pageContext>");
			xmlBuff.append("<objContext><![CDATA[").append("1").append("]]></objContext>");
			xmlBuff.append("<editFlag><![CDATA[").append("A").append("]]></editFlag>");
			xmlBuff.append("<focusedColumn><![CDATA[").append("").append("]]></focusedColumn>");
			xmlBuff.append("<action><![CDATA[").append("SAVE").append("]]></action>");
			xmlBuff.append("<elementName><![CDATA[").append("").append("]]></elementName>");
			xmlBuff.append("<keyValue><![CDATA[").append("1").append("]]></keyValue>");
			xmlBuff.append("<taxKeyValue><![CDATA[").append("").append("]]></taxKeyValue>");
			xmlBuff.append("<saveLevel><![CDATA[").append("1").append("]]></saveLevel>");
			xmlBuff.append("<forcedSave><![CDATA[").append(true).append("]]></forcedSave>");
			xmlBuff.append("<taxInFocus><![CDATA[").append(false).append("]]></taxInFocus>");
			xmlBuff.append("<description>").append("Header0 members").append("</description>");	

			xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"sorder\" objContext=\"1\">");  
			xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
			xmlBuff.append("<order_type><![CDATA["+ checkNull(orderType )+"]]></order_type>");
			xmlBuff.append("<sale_order/>");
			xmlBuff.append("<order_date><![CDATA["+ sdf.format(tranDate) +"]]></order_date>");
			xmlBuff.append("<site_code><![CDATA["+ checkNull(sordSite) +"]]></site_code>");
			xmlBuff.append("<site_code__ship><![CDATA["+ checkNull(sordSite)   +"]]></site_code__ship>");
			xmlBuff.append("<item_ser><![CDATA["+itemSer +"]]></item_ser>");
			xmlBuff.append("<cust_code><![CDATA["+ checkNull(custCode) +"]]></cust_code>");
			xmlBuff.append("<cust_code__dlv><![CDATA["+ checkNull(custCode )+"]]></cust_code__dlv>");
			xmlBuff.append("<cust_code__bil><![CDATA["+ checkNull(custCodeBil) +"]]></cust_code__bil>");
			xmlBuff.append("<cust_pord><![CDATA["+ pOrder +"]]></cust_pord>");
			xmlBuff.append("<tax_class><![CDATA["+ taxClass +"]]></tax_class>");
			xmlBuff.append("<tax_chap><![CDATA["+ taxChap +"]]></tax_chap>");
			xmlBuff.append("<cr_term><![CDATA["+ crTermMain +"]]></cr_term>");
			xmlBuff.append("<curr_code><![CDATA["+ currCode +"]]></curr_code>");
			xmlBuff.append("<dlv_add1><![CDATA["+ addr1 +"]]></dlv_add1>");
			xmlBuff.append("<dlv_add2><![CDATA["+ addr2 +"]]></dlv_add2>");
			xmlBuff.append("<dlv_city><![CDATA["+ checkNull(city )+"]]></dlv_city>");
			xmlBuff.append("<count_code__dlv><![CDATA["+ checkNull(countCode) +"]]></count_code__dlv>");
			xmlBuff.append("<dlv_pin><![CDATA["+ pin +"]]></dlv_pin>");
			xmlBuff.append("<stan_code><![CDATA["+ stanCode +"]]></stan_code>");
			xmlBuff.append("<tran_code><![CDATA["+ tranCode +"]]></tran_code>");
			xmlBuff.append("<sales_pers__2><![CDATA["+ salesPers2 +"]]></sales_pers__2>");
			xmlBuff.append("<sales_pers__1><![CDATA["+ salesPers1 +"]]></sales_pers__1>");
			xmlBuff.append("<trans_mode><![CDATA["+ transMode +"]]></trans_mode>");
			xmlBuff.append("<rcp_mode><![CDATA["+ checkNull(rcpMode) +"]]></rcp_mode>");
			xmlBuff.append("<bank_code><![CDATA["+ checkNull(bankCode )+"]]></bank_code>");
			xmlBuff.append("<state_code__dlv><![CDATA["+ stateCode +"]]></state_code__dlv>");
			xmlBuff.append("<dlv_add3><![CDATA["+ addr3 +"]]></dlv_add3>");
			xmlBuff.append("<curr_code__ins><![CDATA["+ currCode +"]]></curr_code__ins>");
			xmlBuff.append("<tel1__dlv><![CDATA["+ tele1 +"]]></tel1__dlv>");
			xmlBuff.append("<tel2__dlv><![CDATA["+ tele2 +"]]></tel2__dlv>");
			xmlBuff.append("<tel3__dlv><![CDATA["+ tele3 +"]]></tel3__dlv>");
			xmlBuff.append("<fax__dlv><![CDATA["+ fax +"]]></fax__dlv>");
			xmlBuff.append("<market_reg><![CDATA["+ checkNull(marketReg) +"]]></market_reg>");
			xmlBuff.append("<pl_date><![CDATA["+sdf.format(tranDate) +"]]></pl_date>");
			xmlBuff.append("<due_date><![CDATA["+ sdf.format(tranDate)+"]]></due_date>");
			xmlBuff.append("<status_date><![CDATA["+ sdf.format(tranDate) +"]]></status_date>");
			xmlBuff.append("<tax_date><![CDATA["+ sdf.format(tranDate) +"]]></tax_date>");
			xmlBuff.append("<prom_date><![CDATA["+ sdf.format(tranDate) +"]]></prom_date>");
			xmlBuff.append("<pord_date><![CDATA["+ sdf.format(pordDate).toString() +"]]></pord_date>");
			xmlBuff.append("<emp_code__ord><![CDATA["+checkNull( empCode.trim()) +"]]></emp_code__ord>");

			exchRate = finCommon.getDailyExchRateSellBuy(currCode, "", sordSite, sdf.format(tranDate).toString(), "S", conn);
			//exchRate = gf_get_daily_exch_rate_sell_buy(ls_currcode, '', ls_sordsite, ldt_trandate, 'S')
			xmlBuff.append("<exch_rate><![CDATA["+ exchRate +"]]></exch_rate>");
			xmlBuff.append("<exch_rate__ins><![CDATA["+ exchRate +"]]></exch_rate__ins>");
			xmlBuff.append("<part_qty><![CDATA["+ "Y" +"]]></part_qty>");
			xmlBuff.append("<label_type><![CDATA["+ "N" +"]]></label_type>");
			remarks = "Auto created from PO No. : " + pOrder + " Dated  " + pordDate ;
			xmlBuff.append("<remarks><![CDATA["+checkNull( remarks) +"]]></remarks>");
			xmlBuff.append("<dlv_to><![CDATA["+checkNull( dlvto )+"]]></dlv_to>");

			if(priceList == null || priceList.trim().length() == 0)
			{
				priceList = gbfPricelistSite(sordSite, custCode, conn);
			}
			xmlBuff.append("<price_list><![CDATA["+ checkNull(priceList) +"]]></price_list>");

			priceListDiscount = gbfPriceListDiscount(sordSite, custCode, conn);
			xmlBuff.append("<price_list__disc><![CDATA["+ priceListDiscount +"]]></price_list__disc>");

			if(pricelistClg == null || pricelistClg.trim().length() == 0)
			{
				sql = "select price_list__clg from site_customer where  cust_code = ? and    site_code =  ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,custCode );
				pstmt.setString(2,sordSite );
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					pricelistClg = checkNull(rs.getString("price_list__clg"));
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;

				if(pricelistClg == null || pricelistClg.trim().length() == 0)
				{
					sql = "select price_list__clg from customer where  cust_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,custCode );
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						pricelistClg = checkNull(rs.getString("price_list__clg"));
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;
				}

				if(pricelistClg == null || pricelistClg.trim().length() == 0)
				{
					isVAr = distCommon.getDisparams("999999", "PRICE_LIST__CLG_YN", conn);
					if("NULLFOUND".equalsIgnoreCase(isVAr))
					{
						isVAr = "N";
					}
					if("Y".equalsIgnoreCase(isVAr))
					{
						pricelistClg = distCommon.getDisparams("999999", "PRICE_LIST__CLG", conn);
						if(pricelistClg != null && pricelistClg.trim().length() > 0)
						{
							xmlBuff.append("<price_list__clg><![CDATA["+ checkNull(pricelistClg) +"]]></price_list__clg>");
						}
					}
				}
				else
				{
					xmlBuff.append("<price_list__clg><![CDATA["+ checkNull(pricelistClg) +"]]></price_list__clg>");
				}
			}
			else
			{
				xmlBuff.append("<price_list__clg><![CDATA["+ checkNull(pricelistClg) +"]]></price_list__clg>");
			}

			sql = "select cr_term, sales_pers from customer_series where cust_code = ? and item_ser = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,custCode );
			pstmt.setString(2,itemSer );
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				crTerm = checkNull(rs.getString("cr_term"));
				salesPers = checkNull(rs.getString("sales_pers"));
			}
			else
			{
				crTerm = crTermMain;
				salesPers = salesPersMain;
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;

			if(salesPers == null || salesPers.trim().length() == 0)
			{
				salesPers = salesPersMain;
			}

			sql = "select comm_perc, comm_perc__on, curr_code from sales_pers where sales_pers  = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,salesPers );
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				commPerc = checkNull(rs.getString("comm_perc"));
				commPercon = checkNull(rs.getString("comm_perc__on"));
				currcodeComm = checkNull(rs.getString("curr_code"));

				xmlBuff.append("<sales_pers><![CDATA["+ checkNull(salesPers) +"]]></sales_pers>");
				xmlBuff.append("<comm_perc><![CDATA["+ checkNull(commPerc) +"]]></comm_perc>");
				xmlBuff.append("<comm_perc__on><![CDATA["+ checkNull(commPercon) +"]]></comm_perc__on>");
				xmlBuff.append("<curr_code__comm><![CDATA["+ checkNull(currcodeComm) +"]]></curr_code__comm>");

				exchRateComm =finCommon.getDailyExchRateSellBuy(currcodeComm, "", sordSite, sdf.format(tranDate).toString(), "S", conn);
				xmlBuff.append("<exch_rate__comm><![CDATA["+ exchRateComm +"]]></exch_rate__comm>");
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;

			sql = "select dlv_term from   customer_series  where cust_code = ? and    item_ser = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,custCode );
			pstmt.setString(2, itemSer );
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				dlvTerm = checkNull(rs.getString("dlv_term"));
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;

			if(dlvTerm == null || dlvTerm.trim().length() == 0)
			{
				sql = "select dlv_term from   customer  where cust_code = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,custCode );
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					dlvTerm = checkNull(rs.getString("dlv_term"));
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
			}

			if(dlvTerm == null || dlvTerm.trim().length() == 0)
			{
				dlvTerm = "NA";
			}
			xmlBuff.append("<dlv_term><![CDATA["+ checkNull(dlvTerm) +"]]></dlv_term>");


			sql = " select case when frt_term is null then 'B' else frt_term end , curr_code  from transporter" +
					" where tran_code = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,tranCode );
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				frtTerm = checkNull(rs.getString(1));
				currCodefrt = checkNull(rs.getString("curr_code"));

				xmlBuff.append("<curr_code__frt><![CDATA["+ checkNull(currCodefrt) +"]]></curr_code__frt>");
				xmlBuff.append("<frt_term><![CDATA["+ checkNull(frtTerm) +"]]></frt_term>");
				exchRatefrt =  finCommon.getDailyExchRateSellBuy(currCodefrt, "", sordSite, sdf.format(tranDate).toString(), "S", conn);
				xmlBuff.append("<exch_rate__frt><![CDATA["+ exchRatefrt +"]]></exch_rate__frt>");
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;


			sql = "select comm_perc, comm_perc__on, curr_code from sales_pers where sales_pers  = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,salesPers1 );
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				commPerc = checkNull(rs.getString("comm_perc"));
				commPercon = checkNull(rs.getString("comm_perc__on"));
				currcodeComm = checkNull(rs.getString("curr_code"));

				xmlBuff.append("<comm_perc_1><![CDATA["+ checkNull(commPerc) +"]]></comm_perc_1>");
				xmlBuff.append("<comm_perc_on_1><![CDATA["+ checkNull(commPercon) +"]]></comm_perc_on_1>");
				xmlBuff.append("<curr_code__comm_1><![CDATA["+ checkNull(currcodeComm) +"]]></curr_code__comm_1>");

				exchRateComm =finCommon.getDailyExchRateSellBuy(currcodeComm, "", sordSite, sdf.format(tranDate).toString(), "S", conn);
				xmlBuff.append("<exch_rate__comm_1><![CDATA["+ exchRateComm +"]]></exch_rate__comm_1>");
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;

			sql = "select comm_perc, comm_perc__on, curr_code from sales_pers where sales_pers  = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,salesPers2 );
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				commPerc = checkNull(rs.getString("comm_perc"));
				commPercon = checkNull(rs.getString("comm_perc__on"));
				currcodeComm = checkNull(rs.getString("curr_code"));

				xmlBuff.append("<comm_perc_2><![CDATA["+ checkNull(commPerc) +"]]></comm_perc_2>");
				xmlBuff.append("<comm_perc_on_2><![CDATA["+checkNull( commPercon) +"]]></comm_perc_on_2>");
				xmlBuff.append("<curr_code__comm_2><![CDATA["+ checkNull(currcodeComm) +"]]></curr_code__comm_2>");

				exchRateComm =finCommon.getDailyExchRateSellBuy(currcodeComm, "", sordSite, sdf.format(tranDate).toString(), "S", conn);
				xmlBuff.append("<exch_rate__comm_2><![CDATA["+ exchRateComm +"]]></exch_rate__comm_2>");
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;

			sql = "select acct_code__sal, cctr_code__sal from itemser where item_ser = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,itemSer );
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				acctCodesal = checkNull(rs.getString("acct_code__sal"));
				cctrCodesal = checkNull(rs.getString("cctr_code__sal"));

				xmlBuff.append("<acct_code__sal><![CDATA["+ checkNull(acctCodesal) +"]]></acct_code__sal>");
				xmlBuff.append("<cctr_code__sal><![CDATA["+ checkNull(cctrCodesal) +"]]></cctr_code__sal>");
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;

			sql = "select remarks, cust_code__end, sale_order__end, sale_order from porder where purc_order  = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,pOrder );
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				remarks = checkNull(rs.getString("remarks"));
				custCodeend = checkNull(rs.getString("cust_code__end"));
				saleOrderend = checkNull(rs.getString("sale_order__end"));
				saleOrder = checkNull(rs.getString("sale_order"));
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;

			if(saleOrderend == null || saleOrderend.trim().length() == 0)
			{
				if(saleOrder!= null && saleOrder.trim().length() > 0)
				{
					saleOrderend = saleOrder ;
				}
			}

			if(custCodeend == null || custCodeend.trim().length() == 0)
			{
				if(saleOrderend!= null && saleOrderend.trim().length() > 0)
				{
					sql = "select cust_code from sorder where sale_order = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,saleOrderend );
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						custCodeend = checkNull(rs.getString("cust_code"));
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;
				}
			}
			saleOrder = "";
			if(remarks == null)
			{
				remarks = "" ;
			}
			if(remarks.trim().length() > 100)
			{
				remarks2 = remarks.substring(1, 100) ;
				remarks3 = remarks.substring(101, remarks.length()) ;
				System.out.println("remarks>>>"+remarks +"remarks2 >>"+checkNull(remarks2)+ "remarks3 >> "+remarks3);
				xmlBuff.append("<remarks2><![CDATA["+ checkNull(remarks2) +"]]></remarks2>");
				xmlBuff.append("<remarks3><![CDATA["+ checkNull(remarks3) +"]]></remarks3>");
			}
			else
			{
				xmlBuff.append("<remarks2><![CDATA["+ checkNull(remarks )+"]]></remarks2>");
			}
			xmlBuff.append("<sale_order__end><![CDATA["+ checkNull(saleOrderend)+"]]></sale_order__end>");
			xmlBuff.append("<cust_code__end><![CDATA["+ checkNull(custCodeend)+"]]></cust_code__end>");
			//xmlBuff.append("<tot_amt><![CDATA["+totAmtHdr +"]]></tot_amt>");
			//xmlBuff.append("<ord_amt><![CDATA["+ordAmtHdr +"]]></ord_amt>");
			//xmlBuff.append("<tax_amt><![CDATA["+taxAmtHdr +"]]></tax_amt>");
			//xmlBuff.append("</Detail1>");


			sql1 = "select item_code, quantity__stduom, unit__std, rate__stduom, discount, line_no, line_no__sord, specific_instr," +
					" rate__clg, quantity__fc    , prd_code__rfc	,lot_no__passign 	,		tax_chap , tax_class , tax_env " +
					"from porddet where purc_order = ?";
			pstmt1 = conn.prepareStatement(sql1);
			pstmt1.setString(1,pOrder );
			rs1 = pstmt1.executeQuery();
			StringBuffer xmlBuffDet = new StringBuffer();
			//lineNo = 1;
			while(rs1.next())
			{


				itemCode = checkNull(rs1.getString("item_code"));
				quantity = rs1.getDouble("quantity__stduom");
				unit = checkNull(rs1.getString("unit__std"));
				rate = rs1.getDouble("rate__stduom");
				discount = rs1.getDouble("discount");
				linenopord = checkNull(rs1.getString("line_no"));
				linenosord = checkNull(rs1.getString("line_no__sord"));
				specificinstr = checkNull(rs1.getString("specific_instr"));
				rateClg = rs1.getDouble("rate__clg");
				quantityFc = rs1.getDouble("quantity__fc");
				prdCodeRfc = checkNull(rs1.getString("prd_code__rfc"));
				//lotNopassign = rs1.getString("lot_no__passign");
				taxChap = checkNull(rs1.getString("tax_chap"));
				taxClass = checkNull(rs1.getString("tax_class"));
				taxEnv = checkNull(rs1.getString("tax_env"));

				lineNo=lineNo+1;
				lineNoStr="";
				lineNoStr = "   "+lineNo;
				lineNoStr = lineNoStr.substring(lineNoStr.length()-3,lineNoStr.length());
				System.out.println("lineNo["+lineNo+"]");
				System.out.println("lineNoStr["+lineNoStr+"]");
				System.out.println("itemCode"+itemCode+"quantity"+quantity+"unit"+unit+"rate"+rate+"discount"+discount+
						"linenopord"+linenopord+"linenosord"+linenosord+"specificinstr"+specificinstr+"rateClg"+rateClg
						+"taxChap"+taxChap+"taxClass"+taxClass+"taxEnv"+taxEnv);
				xmlBuffDet.append("<Detail2 dbID='' domID=\""+lineNo +"\" objName=\"sorder\" objContext=\"2\">"); 
				xmlBuffDet.append("<attribute pkNames=\"\" selected=\"N\" updateFlag=\"A\" status=\"N\" />");
				xmlBuffDet.append("<sale_order/>");
				xmlBuffDet.append("<line_no><![CDATA["+lineNoStr +"]]></line_no>");
				xmlBuffDet.append("<specific_instr><![CDATA["+specificinstr +"]]></specific_instr>");
				xmlBuffDet.append("<item_code__ord><![CDATA["+itemCode +"]]></item_code__ord>");
				xmlBuffDet.append("<item_code><![CDATA["+itemCode +"]]></item_code>");
				xmlBuffDet.append("<quantity><![CDATA["+quantity +"]]></quantity>");
				xmlBuffDet.append("<unit><![CDATA["+ unit +"]]></unit>");
				xmlBuffDet.append("<rate><![CDATA["+rate +"]]></rate>");
				xmlBuffDet.append("<conv__qty_stduom><![CDATA["+1 +"]]></conv__qty_stduom>");
				xmlBuffDet.append("<unit__std><![CDATA["+unit +"]]></unit__std>");
				xmlBuffDet.append("<quantity__stduom><![CDATA["+quantity +"]]></quantity__stduom>");
				xmlBuffDet.append("<status><![CDATA["+"O" +"]]></status>");
				xmlBuffDet.append("<status_date><![CDATA["+sdf.format(tranDate) +"]]></status_date>");
				xmlBuffDet.append("<dsp_date><![CDATA["+sdf.format(tranDate) +"]]></dsp_date>");
				xmlBuffDet.append("<site_code><![CDATA["+sordSite +"]]></site_code>");
				xmlBuffDet.append("<quantity__fc><![CDATA["+quantityFc +"]]></quantity__fc>");
				xmlBuffDet.append("<prd_code__rfc><![CDATA["+prdCodeRfc +"]]></prd_code__rfc>");

				/*sql = "select stan_code from site where site_code = ?";
				pstmt= conn.prepareStatement(sql);
				pstmt.setString(1,sordSite );
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					//frStation = rs.getString("stan_code");
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;

				sql = "select pord_type from porder where purc_order = ?";
				pstmt= conn.prepareStatement(sql);
				pstmt.setString(1,pOrder );
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					//ordType = rs.getString("pord_type");
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;*/

				//ls_jobworktype = gf_getenv_dis('999999',"JOBWORK_TYPE")
				jobWorkType = distCommon.getDisparams("999999","JOBWORK_TYPE",conn);
				if(jobWorkType == null )
				{
					jobWorkType = "";
				}
				subcontractType = distCommon.getDisparams("999999","SUBCONTRACT_TYPE",conn);
				if(subcontractType == null )
				{
					subcontractType = "";
				}
				lsTaxreq = "Y" ;



				if(jobWorkType.trim().equalsIgnoreCase(ordType.trim()))
				{
					lsTaxreq = distCommon.getDisparams("999999","JOBWORK_TAX",conn);
					if(lsTaxreq == null)
					{
						lsTaxreq = "N";
					}

				}
				if(subcontractType.trim().equalsIgnoreCase(ordType.trim()))
				{
					lsTaxreq = distCommon.getDisparams("999999","SUBCONTRACT_TAX",conn);
					if(lsTaxreq == null)
					{
						lsTaxreq = "N";
					}

				}
				if("Y".equalsIgnoreCase(lsTaxreq))
				{
					xmlBuffDet.append("<tax_chap><![CDATA["+taxChap +"]]></tax_chap>");
					xmlBuffDet.append("<tax_class><![CDATA["+taxClass +"]]></tax_class>");
					xmlBuffDet.append("<tax_env><![CDATA["+taxEnv +"]]></tax_env>");
				}
				Timestamp  currDate = java.sql.Timestamp.valueOf(sdf1.format(tranDate).toString() + " 00:00:00.0");

				System.out.println("currDatecurrDate"+currDate);
				itemSerDet = distCommon.getItemSer(itemCode, sordSite, currDate, custCode, "C", conn);
				errString = distCommon.getToken(itemSerDet, ",");
				xmlBuffDet.append("<item_ser><![CDATA["+itemSerDet +"]]></item_ser>");

				sql = "select descr, unit__rate, item_stru, pack_code,pack_instr from item where item_code = ?";
				pstmt= conn.prepareStatement(sql);
				pstmt.setString(1,itemCode );
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					itemDescr = rs.getString("descr");
					unitRate = checkNull(rs.getString("unit__rate"));
					lsType = rs.getString("item_stru");
					packCode = checkNullTrim(rs.getString("pack_code"));//Modify by kailasG on 16 aug 2019 [getting error while confirm the despatch]
					packinstr = checkNullTrim(rs.getString("pack_instr"));//Modify by kailasG on 16 aug 2019 [getting error while confirm the despatch]
					System.out.println("descr:::["+itemDescr+"]"+"unit rate:::["+unitRate+"]"+"item_stru:::["+lsType+"]"+"packCode:::["+packCode+"]"+"pack instr:::["+packinstr+"]");

					xmlBuffDet.append("<item_descr><![CDATA["+itemDescr +"]]></item_descr>");
					xmlBuffDet.append("<unit__rate><![CDATA["+unitRate +"]]></unit__rate>");
					xmlBuffDet.append("<pack_code><![CDATA["+packCode +"]]></pack_code>");
					xmlBuffDet.append("<pack_instr><![CDATA["+packinstr +"]]></pack_instr>");

					if("F".equalsIgnoreCase(lsType))
					{
						xmlBuffDet.append("<item_flg><![CDATA["+"B" +"]]></item_flg>");
					}
					else
					{
						xmlBuffDet.append("<item_flg><![CDATA["+"I" +"]]></item_flg>");
					}

				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;

				if(!unit.trim().equalsIgnoreCase(unitRate.trim()))
				{
					//lc_ratestduom = gf_conv_qty_fact(ls_unit, ls_unitrate, ls_itemcode, lc_rate, lc_rateconv)
					rateStduomArryList = distCommon.getConvQuantityFact(unit,unitRate,itemCode,rate,rateConv,conn);
					System.out.println("rateStduomArryList>>>"+rateStduomArryList.get(0));
					rateStduom = (String)rateStduomArryList.get(0);

					xmlBuffDet.append("<conv__rtuom_stduom><![CDATA["+rateConv +"]]></conv__rtuom_stduom>");
				}
				else
				{
					rateStduom = Double.toString(rate );
					xmlBuffDet.append("<conv__rtuom_stduom><![CDATA["+1 +"]]></conv__rtuom_stduom>");
				}
				xmlBuffDet.append("<rate__stduom><![CDATA["+rateStduom +"]]></rate__stduom>");
				xmlBuffDet.append("<order_type><![CDATA["+orderType +"]]></order_type>");

				if(Double.toString(rateClg) == null || rateClg <= 0)
				{
					sql = " select (case when udf_str1 is null then ''  else udf_str1 end)," +
							" (case when udf_str2 is null then '' else udf_str2 end) " +
							" from gencodes where fld_name = 'ORDER_TYPE'  and  mod_name = 'W_SORDER' and  fld_value = ?";
					pstmt= conn.prepareStatement(sql);
					pstmt.setString(1,orderType );
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						udfStr1 = checkNull(rs.getString(1));
						udfStr2 = checkNull(rs.getString(2));

					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;


					if(udfStr1.length() > 0 && Double.parseDouble(udfStr1.trim()) > 0)
					{
						rateDiff = (100 - Double.parseDouble(udfStr1.trim())) / 100 ;
						rateClg = Double.parseDouble(rateStduom) * rateDiff ;
					}
					else if(udfStr2.trim().length() > 0)
					{
						rateClg = distCommon.pickRate(udfStr2,sdf.format( tranDate).toString(), itemCode, "", "L", quantity,conn);
					}
					else
					{

						if(pricelistClg.length() > 0)
						{
							if(("B").equalsIgnoreCase(distCommon.getPriceListType(pricelistClg, conn)))
							{
								rateClg = 0 ;
							}
							else
							{
								rateClg = distCommon.pickRate(pricelistClg,sdf.format( tranDate).toString(), itemCode ,"", "L", quantity,conn);
							}
						}

					}
					if(Double.toString(rateClg) == null || rateClg < 0 )
					{
						rateClg = 0;
					}

				}
				xmlBuffDet.append("<rate__clg><![CDATA["+rateClg +"]]></rate__clg>");
				xmlBuffDet.append("<discount><![CDATA["+discount +"]]></discount>");

				if(taxAmt == -999999999)
				{
					errString = itmDBAccessLocal.getErrorString("", "Error in Calculating Tax", "","",conn);
					return errString;
				}

				totAmt = (quantity * Double.parseDouble(rateStduom ));
				totAmt = totAmt - ((totAmt * discount)/100) + taxAmt ;
				totAmtHdr += totAmt ;
				taxAmtHdr += taxAmt ;
				ordAmtHdr += (totAmt - taxAmt) ;

				xmlBuffDet.append("<tax_amt><![CDATA["+taxAmt +"]]></tax_amt>");
				xmlBuffDet.append("<net_amt><![CDATA["+taxAmt +"]]></net_amt>");
				xmlBuffDet.append("<status><![CDATA["+"N" +"]]></status>");
				xmlBuffDet.append("</Detail2>");

				//lineNo ++;

				System.out.println("totAmtHdr>>[" +totAmtHdr+"]taxAmtHdr[>>"+taxAmtHdr+"]ordAmtHdr>>["+ordAmtHdr+"]totAmt["+totAmt+"]taxAmt["+taxAmt);

				StringBuffer xmlBuffkeyGen = new StringBuffer();
				xmlBuffkeyGen.append("<Detail1 dbID='' domID=\"1\" objName=\"porder\" objContext=\"1\">"); 
				xmlBuffkeyGen.append("<attribute pkNames=\"\" selected=\"N\" updateFlag=\"A\" status=\"N\" />");
				xmlBuffkeyGen.append("<site_code><![CDATA["+sordSite +"]]></site_code>");
				xmlBuffkeyGen.append("<line_no><![CDATA["+linenopord +"]]></line_no>");
				xmlBuffkeyGen.append("<item_code><![CDATA["+itemCode +"]]></item_code>");
				xmlBuffkeyGen.append("</Detail1>");

				sql = "select PRE_ASSIGN_LOT from pordertype where order_type = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, orderType);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					//Added CheckNull By PriankaC on 25JAN18.[START]
					//preAssignLot = rs.getString("PRE_ASSIGN_LOT");
					preAssignLot = checkNull(rs.getString("PRE_ASSIGN_LOT"));
					//Added CheckNull By PriankaC on 25JAN18.[END]
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;

				if("A".equalsIgnoreCase(preAssignLot.trim()))
				{
					sql = "select key_string from transetup where upper(tran_window) = 'PURC_LOT_NO' " ;
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						keyStr =checkNull( rs.getString("key_string"));

					}
					else
					{
						sql = "select key_string from transetup where upper(tran_window) = 'GENERAL' ";
						pstmt = conn.prepareStatement(sql);
						rs = pstmt1.executeQuery();
						if(rs.next())
						{
							keyStr = checkNull(rs.getString("key_string"));
						}
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;

					//lotNo  = gf_gen_key_nvo(lds_keygen, 'PURLOT', "lot_no__passign", keystr);

					TransIDGenerator tg = new TransIDGenerator(xmlBuffkeyGen.toString(), "BASE", CommonConstants.DB_NAME);
					lotNo = tg.generateTranSeqID("PURLOT", "lot_no__passign", keyStr, conn);
					System.out.println("lotNo :"+lotNo);

					sql = "SELECT ? from dual";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lotNo);
					rs = pstmt1.executeQuery();
					if(rs.next())
					{
						lotNo = checkNull(rs.getString(1));
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;

					if("ERROR".equalsIgnoreCase(lotNo))
					{
						errString = itmDBAccessLocal.getErrorString("", "VTLOTNO", "","",conn);
						return errString;
					}

					sql = "update porddet  set lot_no__passign = ? where purc_order = ?  and line_no = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lotNo);	
					pstmt.setString(2, pOrder);	
					pstmt.setString(3, linenopord);	
					pstmt.executeUpdate();

					pstmt.close();pstmt = null;

				}

				sql = "update porddet  set line_no__sord = ? where purc_order = ?  and line_no = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, linenosord);	
				pstmt.setString(2, pOrder);	
				pstmt.setString(3, linenopord);	
				pstmt.executeUpdate();

				pstmt.close();pstmt = null;

			}
			rs1.close();rs1 = null;
			pstmt1.close();pstmt1 = null;

			System.out.println("xmlBuffDet>>>"+xmlBuffDet);

			xmlBuff.append("<tot_amt><![CDATA["+totAmtHdr +"]]></tot_amt>");
			xmlBuff.append("<ord_amt><![CDATA["+ordAmtHdr +"]]></ord_amt>");
			xmlBuff.append("<tax_amt><![CDATA["+taxAmtHdr +"]]></tax_amt>");
			xmlBuff.append("</Detail1>");

			xmlBuff.append(xmlBuffDet);
			xmlBuff.append("</Header0>");
			xmlBuff.append("</group0>");
			xmlBuff.append("</DocumentRoot>");
			xmlString = xmlBuff.toString();
			System.out.println("@@@@@2: xmlString:"+xmlBuff.toString());
			System.out.println("...............just before savdata()");
			errString = saveData(sordSite,xmlString,userId,conn);
			System.out.println("@@@@@2: retString:"+errString);
			System.out.println("--retString finished--");
			if (errString.indexOf("Success") > -1)
			{
				System.out.println("@@@@@@3: Success"+errString);
			}
			else
			{
				System.out.println("@@@@@@3: inside rollback "+errString);
				conn.rollback();
				System.out.println("[" + errString + "]");	
				return errString;
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new ITMException(e);
		} 
		finally
		{
			try
			{
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if(pstmt1 != null)
				{
					pstmt1.close();
					pstmt1 = null;					
				}
				if(rs != null)
				{
					rs.close();
					rs = null;
				}
				if(rs1 != null)
				{
					rs1.close();
					rs1 = null;
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

	private String gbfPricelistSite(String sordSite ,String custCode ,Connection conn) throws ITMException 
	{
		System.out.println("Inside gbfPricelistSite..........."+sordSite);
		String priceList = "" ,sql= "";
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		try 
		{

			sql = "select price_list from site_customer where cust_code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,custCode);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				priceList = checkNull(rs.getString("price_list"));
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;

			if(priceList == null || priceList.trim().length() == 0)
			{
				sql = "select price_list from customer  where cust_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,custCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					priceList = checkNull(rs.getString("price_list"));
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
			}
		} 
		catch (SQLException e)
		{
			System.out.println("Exception :conf ::" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}		
		return priceList; 
	}
	private String gbfPorderAdvance(String tranId ,double adAdvPerc ,String asFlag ,String xtraParams,Connection conn) throws Exception // xtraParams ADDED BY NANDKUMAR GADKARI ON 11/03/19
	{

		System.out.println("Inside gbfPorderAdvance...........");
		String sql= "" , poId = "", supp= "", curr= "",sql1 = "", crTerm= "",proj= "",site= "", itemSer = "",lsType= "",provPo = "",
				errString= "" , siteCodeDlv = "",siteCodeOrd = "" ,finDlv = "",finOrd = "" ,lsLink = "",sqlNew = "" ,
				bankCode = "", finEnt = "" , lsCode = "" ,relAgnst = "" ,advType= "" ,sqlInsert = "" ,taxClass = "" ,
				taxChap= "" ,taxEnv = "" ,siteCodeAdv = "",payMode = "", acctAp = "" ,acctApAdv = "",cctrAp = "" , cctrApAdv= "",
				invAcct= "" , acctCodeCr = "", cctrCodeCr = "" ,acctApItem = "",cctrApItem = "" , keyStr = "",vouchId = "",remarks = "", rndStr= "",
				rndOff = "", rndTo= "",userId=""; //, = "",, = "", = "",, = "", = "",, = "", = "",, = "", = "", ;
		double amount = 0.0, ordAmt = 0.0 ,totalPoamt = 0.0 ,exch = 0.0 ,taxAmt = 0.0,advPerc= 0.0,netAmtBase= 0.0,
				netAmt = 0.0 ,vouchAmt= 0.0 ,lcTax = 0.0 ;
		int lineNo = 1,liRelAfter = 0 ,liRndTo = 0,cnt=0;
		boolean edilink = false;
		Timestamp tranDt = null,mileStoneDt = null ;
		PreparedStatement pstmt = null ,pstmt1 = null,pstmtNew = null ,pstmtInsert = null ;
		ResultSet rs = null ,rs1 = null ,rsNew = null ;
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		java.sql.Timestamp currDate = null;
		currDate = new java.sql.Timestamp(System.currentTimeMillis()) ;
		//Added by sarita on 30APR2018 to set current Date with Time [start]
		java.sql.Timestamp currentDateTime = null;
		currentDateTime = new java.sql.Timestamp(System.currentTimeMillis());
		System.out.println("currentDateTime >>> ["+currentDateTime+"]");
		//Added by sarita on 30APR2018 to set current Date with Time[end]  
		try 
		{
			//Modified by mayur on 27-04-18--start
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getDBDateFormat()); 
			currDate = java.sql.Timestamp.valueOf(sdf.format(currDate) + " 00:00:00.000");
			tranDt = currDate;
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			//Modified by mayur on 27-04-18--end 
			System.out.println("tranId..."+tranId + "adAdvPerc>>>"+adAdvPerc + "asFlag" + asFlag);

            if(asFlag.equalsIgnoreCase("PO") || asFlag.equalsIgnoreCase("POPRO") )
			{
                //added by monika salla on 11 feb 21 --to avoid if their is  no row still system is trying to create advance against PO .
                sql = "select count(1) from pord_pay_term where purc_order = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					cnt =rs.getInt(1);
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
                if(cnt==0)
                {
                    return errString;
                }

                //end
				sqlNew = " Select porder.ord_date, porder.supp_code , porder.tot_amt, porder.ord_amt	," +
						" porder.curr_code 		, porder.exch_rate , porder.cr_term		, porder.proj_code," +
						" porder.site_code__bill ,porder.item_ser, case when porder.vouch_adv_amt is null then 0 else porder.vouch_adv_amt end," +
						" porder.tot_amt, porder.advance,porder.pord_type, porder.provi_tran_id " +
						" from porder where purc_order = ? And 	confirmed  = 'Y'";
				pstmtNew = conn.prepareStatement(sqlNew);
				pstmtNew.setString(1, tranId);
				rsNew = pstmtNew.executeQuery();
				if(rsNew.next())
				{
					tranDt = rsNew.getTimestamp(1);
					supp = checkNull(rsNew.getString(2));
					amount = rsNew.getDouble(3);
					ordAmt = rsNew.getDouble(4);
					curr   = checkNull(rsNew.getString(5));
					exch	 = rsNew.getDouble(6);
					crTerm = checkNull(rsNew.getString(7));
					proj = checkNull(rsNew.getString(8));
					site   = checkNull(rsNew.getString(9));
					itemSer= checkNull(rsNew.getString(10));
					// vouchAdvAmt = rsNew.getDouble(11);
					totalPoamt = rsNew.getDouble(12);
					//advance	= rsNew.getDouble(13);
					lsType = checkNull(rsNew.getString(14));
					provPo = checkNull(rsNew.getString(15));
				} 
				else
				{
					errString = itmDBAccessLocal.getErrorString("", "VTPORD3", "","",conn);
					return errString;
				}

				poId = tranId ;

				if(!"POPRO".equalsIgnoreCase(asFlag))
				{
					if(errString == null ||errString.trim().length() == 0)
					{
						sql1 = "select SITE_CODE__DLV,SITE_CODE__ORD from porder where  PURC_ORDER = ? And confirmed  = 'Y' ";
						pstmt1 = conn.prepareStatement(sql1);
						pstmt1.setString(1, poId);
						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{
							siteCodeDlv = checkNull(rs1.getString(1));
							siteCodeOrd = checkNull(rs1.getString(2));

							if(!siteCodeDlv.trim().equalsIgnoreCase(siteCodeOrd.trim()))
							{  
								sql = "select FIN_ENTITY from site where  site_code = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,siteCodeDlv);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									finDlv = checkNull(rs.getString("FIN_ENTITY"));
								}
								rs.close();rs = null;
								pstmt.close();pstmt = null ;

								sql = "select FIN_ENTITY from site where site_code = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,siteCodeOrd);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									finOrd = checkNull(rs.getString("FIN_ENTITY"));
								}
								rs.close();rs = null;
								pstmt.close();pstmt = null ;

								sql = " select link_type from ibca_pay_ctrl where  site_code__from = ? and site_code__to = ? " +
										" and fin_entity__from = ? and fin_entity__to   = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,siteCodeOrd);
								pstmt.setString(2,siteCodeDlv);
								pstmt.setString(3,finOrd);
								pstmt.setString(4,finDlv);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									lsLink = checkNull(rs.getString("link_type"));
								}
								else
								{
									errString = itmDBAccessLocal.getErrorString("", "VTNIBCA", "","",conn);
									return errString;
								}
								rs.close();rs = null;
								pstmt.close();pstmt = null ;

								if ("N".equalsIgnoreCase(lsLink) || "E".equalsIgnoreCase(lsLink) ) 
								{
									edilink = true ;
								}
							}
						}
						rs1.close();rs1 = null;
						pstmt1.close();pstmt1 = null ;
					}
					else
					{
						asFlag = "PO";
					}
				}
			}
			else if(asFlag.equalsIgnoreCase("PR") || asFlag.equalsIgnoreCase("QC") )
			{

				sqlNew = " Select porder.ord_date , porder.supp_code , case when porcp.amount is null then 0 else porcp.amount end  ," +
						" case when porcp.amount is null then 0 else porcp.amount end - case when porcp.tax is null then 0 else porcp.tax end," +
						" porder.curr_code , porder.exch_rate , porder.cr_term, porder.proj_code, " +
						" porder.site_code__bill , porder.item_ser  , case when porder.vouch_adv_amt is null then 0 else porder.vouch_adv_amt end ,  " +
						" porder.tot_amt , porder.advance, porder.pord_type, porder.provi_tran_id," +
						" porder.purc_order, (case when porcp.tax is null then 0 else porcp.tax end), porder.SITE_CODE__ORD From   porder , porcp" +
						" Where  porder.purc_order = porcp.purc_order 	And    porcp.tran_id 	 = ? And	 porder.confirmed  = 'Y'";
				pstmtNew = conn.prepareStatement(sqlNew);
				pstmtNew.setString(1, tranId);
				rsNew = pstmtNew.executeQuery();
				if(rsNew.next())
				{
					tranDt = rsNew.getTimestamp(1);
					supp = checkNull(rsNew.getString(2));
					amount = rsNew.getDouble(3);
					ordAmt = rsNew.getDouble(4);
					curr   = checkNull(rsNew.getString(5));
					exch	 = rsNew.getDouble(6);
					crTerm = checkNull(rsNew.getString(7));
					proj = checkNull(rsNew.getString(8));
					site   = checkNull(rsNew.getString(9));
					itemSer= checkNull(rsNew.getString(10));
					//vouchAdvAmt = rsNew.getDouble(11);
					totalPoamt = rsNew.getDouble(12);
					//advance	= rsNew.getDouble(13);
					lsType = checkNull(rsNew.getString(14));
					provPo = checkNull(rsNew.getString(15));
					poId   = checkNull(rsNew.getString(16));
					taxAmt  = rsNew.getDouble(7);
					siteCodeOrd  = checkNull(rsNew.getString(18));
				}


			}
			if(rsNew == null)
			{
				errString = itmDBAccessLocal.getErrorString("", "VTPORD3", "","",conn);
				return errString;
			}
			rsNew.close();rsNew = null;
			pstmtNew.close();pstmtNew = null ;

			sql = "select bank_code , fin_entity from site where site_code = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,site);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				bankCode = checkNull(rs.getString("bank_code"));
				finEnt = checkNull(rs.getString("fin_entity"));
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null ;

			if("PO".equalsIgnoreCase(asFlag) || "POPRO".equalsIgnoreCase(asFlag))
			{
				lsCode = "01";
			}
			else if("PR".equalsIgnoreCase(asFlag))
			{
				lsCode = "02";
			}
			else if("QC".equalsIgnoreCase(asFlag))
			{
				lsCode = "03";
			}
			System.out.println("asFlag "+asFlag);

			if("PR".equalsIgnoreCase(asFlag))
			{
				sqlNew = " select line_no,rel_agnst,amt_type, rel_amt, rel_after,retention_perc,tax_class,tax_chap,tax_env, site_code__adv "+
						" from pord_pay_term where  purc_order = ? " ;
				pstmtNew = conn.prepareStatement(sqlNew);
				pstmtNew.setString(1, poId);
				rsNew = pstmtNew.executeQuery();
				while(rsNew.next())
				{
					lineNo = rsNew.getInt("line_no");
					relAgnst = checkNull(rsNew.getString("rel_agnst"));
					advType = checkNull(rsNew.getString("amt_type"));
					adAdvPerc = (double) rsNew.getDouble("rel_amt");
					liRelAfter = rsNew.getInt("rel_after");
					// lcRetPerc = rsNew.getString("retention_perc");
					taxClass = checkNull(rsNew.getString("tax_class"));
					taxChap = checkNull(rsNew.getString("tax_chap"));
					taxEnv = checkNull(rsNew.getString("tax_env"));
					siteCodeAdv = checkNull(rsNew.getString("site_code__adv"));

					if(siteCodeAdv.length() > 0)
					{
						if(!siteCodeAdv.equalsIgnoreCase(siteCodeOrd))
						{
							continue ;
						}
						else
						{
							if(edilink == true)
							{
								continue ;
							}
						}
					}

					if("04".equalsIgnoreCase(relAgnst) || (!lsCode.equalsIgnoreCase(relAgnst)))
					{
						continue ;
					}

					if("01".equalsIgnoreCase(advType))
					{
						advType = "B";
					}
					else if("02".equalsIgnoreCase(advType))
					{
						advType = "P";
					}
					else if("03".equalsIgnoreCase(advType))
					{
						advType = "F";
					}
					else if("04".equalsIgnoreCase(advType))
					{
						advType = "T";
					}

					if(tranId.equalsIgnoreCase(provPo))
					{
						errString= itmDBAccessLocal.getErrorString("", "VTPURPROV1", "","",conn);
						return errString;
					}

					if(errString != null && errString.trim().length() > 0)
					{
						return errString;
					}

					sql = " select pay_mode,acct_code__ap ,cctr_code__ap ,acct_code__ap_adv, cctr_code__ap_adv" +
							" from  supplier where supp_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,supp);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						payMode = checkNull(rs.getString("pay_mode"));
						acctAp = checkNull(rs.getString("acct_code__ap"));
						cctrAp = checkNull(rs.getString("cctr_code__ap"));
						acctApAdv = checkNull(rs.getString("acct_code__ap_adv"));
						cctrApAdv = checkNull(rs.getString("cctr_code__ap_adv"));
					}
					else
					{
						errString= itmDBAccessLocal.getErrorString("", "VMSUPP1", "","",conn);
						return errString;
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null ;


					invAcct = finCommon.getFinparams("999999", "INV_ACCT_PORCP", conn);
					if(invAcct == null || "".equalsIgnoreCase(invAcct) || invAcct.trim().length() == 0)
					{
						invAcct = "N";
					}

					sql = "select ACCT_CODE__AP_ADV,CCTR_CODE__AP_ADV,acct_code__cr,cctr_code__cr from  porddet where purc_order = ? " ;
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, tranId);
					rs = pstmt.executeQuery();
					while(rs.next())
					{
						//acctApAdv = checkNull(rs.getString("ACCT_CODE__AP_ADV")); // manoharan 03-aug-18 as per pitambari trigger
						//cctrApAdv = checkNull(rs.getString("CCTR_CODE__AP_ADV")); // manoharan 03-aug-18 as per pitambari trigger
						acctCodeCr = checkNull(rs.getString("acct_code__cr"));
						cctrCodeCr = checkNull(rs.getString("cctr_code__cr")); 
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null ;


					if(errString.trim().length() == 0)
					{
						if(acctAp == null || acctAp.trim().length() == 0)
						{
							cctrApItem = finCommon.getAcctDetrTtype("", itemSer, "PO", lsType, conn);
							//  gbf_acct_detr_ttype("",ls_itemser,'PO', ls_type);
							acctApItem = distCommon.getToken(cctrApItem, ",");

							if(acctApItem == null )
							{
								errString= itmDBAccessLocal.getErrorString("", "VTSUPPAC", "","",conn);
								return errString;
							}
							acctAp = acctApItem ;
						}

						if(cctrAp == null || cctrAp.trim().length() == 0)
						{
							cctrAp = cctrApItem;
						}
						if(acctCodeCr == null || acctCodeCr.trim().length() == 0)
						{
							acctCodeCr = acctAp;
						}
						if(cctrApAdv == null || cctrApAdv.trim().length() == 0)
						{
							sql = "select cctr_code__cr from  porddet where purc_order = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, tranId);
							rs = pstmt.executeQuery();
							while(rs.next())
							{
								cctrApAdv = rs.getString("cctr_code__cr");
							}
							rs.close();rs = null;
							pstmt.close();pstmt = null ;

						}
						if(acctCodeCr == null || acctCodeCr.trim().length() == 0)
						{
							acctCodeCr = acctAp;
						}

						if(cctrAp == null || cctrAp.trim().length() == 0)
						{
							cctrAp = cctrApAdv ; 
						}

						if(errString.trim().length() == 0)
						{
							//changed by mayur on 23-APR-2018
							//sql = "select key_string from transetup where upper(tran_window) = 'w_voucher' " ;
							sql = "select key_string from transetup where upper(tran_window) = 'W_VOUCHER' " ;
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								keyStr = checkNull(rs.getString("key_string"));

							}
							else
							{
								sql = "select key_string from transetup where upper(tran_window) = 'GENERAL' ";
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									keyStr = checkNull(rs.getString("key_string"));
								}
							}
							rs.close();rs = null;
							pstmt.close();pstmt = null;

							//COMMENTED BY NANDKUMAR GADKARI ON 11/03/19 
							/*StringBuffer xmlBuffkeyGen = new StringBuffer();
							xmlBuffkeyGen.append("<Detail1 dbID='' domID=\"1\" objName=\"voucher\" objContext=\"1\">"); 
							xmlBuffkeyGen.append("<attribute pkNames=\"\" selected=\"N\" updateFlag=\"A\" status=\"N\" />");
							xmlBuffkeyGen.append("<site_code><![CDATA["+site +"]]></site_code>");
							//changes done by mayur on 24-04-18--start
							//xmlBuffkeyGen.append("<tran_date><![CDATA["+currDate+"]]></tran_date>");
							xmlBuffkeyGen.append("<tran_date><![CDATA["+sdf.format(currDate) +"]]></tran_date>");
							//changes done by mayur on 24-04-18--end
							xmlBuffkeyGen.append("<vouch_type><![CDATA["+"A" +"]]></vouch_type>");
							xmlBuffkeyGen.append("</Detail1>");

							TransIDGenerator tg = new TransIDGenerator(xmlBuffkeyGen.toString(), "BASE", CommonConstants.DB_NAME);
							System.out.println("vouchId :"+vouchId);*/

							if("ERROR".equalsIgnoreCase(vouchId))
							{
								errString= itmDBAccessLocal.getErrorString("", "TRANIDERR", "","",conn);
								return errString;
							}
							System.out.println("vouchId adAdvPerc:"+adAdvPerc);
							if("P".equalsIgnoreCase(advType))
							{
								advPerc = amount * (adAdvPerc/100) ;
							}
							else if("F".equalsIgnoreCase(advType))
							{
								advPerc = adAdvPerc ;
							}
							else if("B".equalsIgnoreCase(advType))
							{
								advPerc = ordAmt * (adAdvPerc/100)  ;
							}
							else if("T".equalsIgnoreCase(advType))
							{
								advPerc = (ordAmt * (adAdvPerc/100)) + taxAmt ;
							}
							netAmtBase = advPerc * exch ;
							netAmt = advPerc ;

							System.out.println("vouchId netAmtBase:"+netAmtBase+"exch"+exch +"netAmt"+netAmt+"advPerc"
									+advPerc+"totalPoamt"+totalPoamt+"vouchAmt"+vouchAmt);

							sql = "select sum(adv_amt) from voucher where tran_id Between '0' and 'Z' and	vouch_type = 'A' and 	purc_order =  ?";	
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, tranId);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								vouchAmt  = rs.getDouble(1);

								if(totalPoamt < (vouchAmt + advPerc))
								{
									errString= itmDBAccessLocal.getErrorString("", "VTPURCADV", "","",conn);
									return errString;
								}
							}
							rs.close();rs = null;
							pstmt.close();pstmt = null;

							remarks =  "PO:" + tranId  +"Date:"+ sdf.format(currDate.toString())+" Adv. Amt:"+ Double.toString(advPerc);

							if(errString.trim().length() == 0)
							{
								if(liRelAfter == 0)
								{
									mileStoneDt = currDate ;
								}
								else
								{
									mileStoneDt = utilMethods.RelativeDate(currDate, liRelAfter);
								}

								remarks = "PO:" + tranId  +"Date:"+ sdf.format(currDate.toString())+" Adv. Amt:"+ Double.toString(amount);
								rndStr = "VOUCH-RND";
								rndOff = finCommon.getFinparams("999999", rndStr, conn);

								if(!"NULLFOUND".equalsIgnoreCase(rndOff))
								{
									rndOff = rndOff.trim();
									rndStr = "VOUCH" + "-RNDTO" ;	
									rndTo = finCommon.getFinparams("999999", rndStr, conn);
									if(!"NULLFOUND".equalsIgnoreCase(rndTo))
									{
										liRndTo = Integer.parseInt(rndTo.trim()) ;
									}
								}

								//COMMENTED BY NANDKUMAR GADKARI ON 11/03/19 
								/* sqlInsert = "Insert into voucher( tran_id	,tran_date,eff_date,supp_code,bill_no,bill_date,purc_order,curr_code,exch_rate," +
										"acct_code,cctr_code, bank_code	,auto_pay,adv_amt,cr_term,due_date	,chg_date,chg_user,chg_term,site_code," +
										"fin_entity, bill_amt,tax_amt,tot_amt,tax_date,vouch_type, proj_code, confirmed,paid,conf_date, pay_mode, " +
										"net_amt, net_amt__bc,diff_amt__exch ,supp_bill_amt ,acct_code__adv ,cctr_code__adv,tax_chap,  tax_class, " +
										"tax_env ,remarks,tran_mode ,rnd_off,	rnd_to) " +
										"values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
							pstmtInsert = conn.prepareStatement(sqlInsert);
							pstmtInsert.setString(1, vouchId);
							pstmtInsert.setTimestamp(2, currDate); 	
							//pstmtInsert.setTimestamp(3, tranDt); done changes by sarita to set currDate in[eff_date] : 30APR2018
							pstmtInsert.setTimestamp(3, currDate);
							pstmtInsert.setString(4,supp);
							pstmtInsert.setString(5, "");
							//pstmtInsert.setTimestamp(6,tranDt);done changes by sarita to set currDate in[bill_date] : 30APR2018
							pstmtInsert.setTimestamp(6,currDate);
							pstmtInsert.setString(7,tranId);
							pstmtInsert.setString(8, curr);
							pstmtInsert.setDouble(9, exch);
							pstmtInsert.setString(10, acctAp); // acctApAdv); // manoharan 03-aug-18 as per pitambari trigger
							pstmtInsert.setString(11, cctrAp); // cctrApAdv); // manoharan 03-aug-18 as per pitambari trigger
							pstmtInsert.setString(12, bankCode);
							pstmtInsert.setString(13, "Y");
							pstmtInsert.setDouble(14, advPerc);
							pstmtInsert.setString(15, crTerm);
							pstmtInsert.setTimestamp(16,mileStoneDt);
							//pstmtInsert.setTimestamp(17,currDate);//done changes by sarita to set current Date with time[chg_date] : 30APR2018
							pstmtInsert.setTimestamp(17,currentDateTime);
							pstmtInsert.setString(18, "BASE"); //chg USer
							pstmtInsert.setString(19, "BASE"); // LoginCOde
							pstmtInsert.setString(20, site);
							pstmtInsert.setString(21, finEnt);
							pstmtInsert.setDouble(22, 0);
							pstmtInsert.setDouble(23, 0);
							pstmtInsert.setDouble(24, 0);
							//pstmtInsert.setTimestamp(25, tranDt); done changes by sarita to set currDate in[tax_date] : 30APR2018
							pstmtInsert.setTimestamp(25, currDate);
							pstmtInsert.setString(26, "A");
							pstmtInsert.setString(27, proj);
							pstmtInsert.setString(28, "N");
							pstmtInsert.setString(29, null);
							//pstmtInsert.setTimestamp(30, currDate);done changes by sarita to set current Date with time[conf_date] : 30APR2018
							pstmtInsert.setTimestamp(30, currentDateTime);
							pstmtInsert.setString(31, payMode);
							pstmtInsert.setDouble(32, netAmt);
							pstmtInsert.setDouble(33, netAmtBase);
							pstmtInsert.setString(34, null);
							pstmtInsert.setDouble(35, netAmt);
							pstmtInsert.setString(36, acctApAdv);//acctCodeCr); // manoharan 03-aug-18 as per pitambari trigger
							pstmtInsert.setString(37, cctrApAdv);//cctrCodeCr); // manoharan 03-aug-18 as per pitambari trigger
							pstmtInsert.setString(38, taxChap);            
							pstmtInsert.setString(39, taxClass);
							pstmtInsert.setString(40, taxEnv);
							pstmtInsert.setString(41, remarks);
							pstmtInsert.setString(42, "A");
							pstmtInsert.setString(43, rndOff);
							pstmtInsert.setInt(44, liRndTo);        
							pstmtInsert.executeUpdate();
							pstmtInsert.close();
							pstmtInsert = null;


							sqlInsert = "insert into vouchdet( tran_id	, line_no	, acct_code	, cctr_code	, amount,emp_code, anal_code , tax_amt	, apply_tax) " +
										"values(?,?,?,?,?,?,?,?,?)";
							pstmtInsert = conn.prepareStatement(sqlInsert);
							pstmtInsert.setString(1, vouchId);
							pstmtInsert.setInt(2, 1); 	
							pstmtInsert.setString(3, acctApAdv); //acctCodeCr); // manoharan 03-aug-18 as per pitambari trigger
							pstmtInsert.setString(4, cctrApAdv); //cctrCodeCr); // manoharan 03-aug-18 as per pitambari trigger
							pstmtInsert.setDouble(5,advPerc);
							pstmtInsert.setString(6,null);
							pstmtInsert.setString(7,null);
							pstmtInsert.setDouble(8, lcTax);
							pstmtInsert.setDouble(9, 0);
							pstmtInsert.executeUpdate();
							pstmtInsert.close();
							pstmtInsert = null;


							sql1 = " update voucher set tax_amt  = '"+lcTax +"'," +
								   " tot_amt = tot_amt + '"+lcTax +"' , " +
								   " net_amt = net_amt + '"+lcTax +"' ," +
								   " net_amt__bc = (net_amt + '"+lcTax +"') * exch_rate " +
								   " where tran_id = ?";
					    	pstmt1 = conn.prepareStatement(sql1);
							pstmt1.setString(1, vouchId);
							pstmt1.executeUpdate();
							pstmt1.close();
							pstmt1 = null;*/

								//								ADDED BY NANDKUMAR GADKARI ON 11/03/19------------START---------------------------------------------
								StringBuffer xmlBuff = new StringBuffer();
								String xmlString = null;



								xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
								xmlBuff.append("<DocumentRoot>");
								xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
								xmlBuff.append("<group0>");
								xmlBuff.append("<description>").append("Group0 description").append("</description>");
								xmlBuff.append("<Header0>");
								xmlBuff.append("<objName><![CDATA[").append("voucher").append("]]></objName>");
								xmlBuff.append("<pageContext><![CDATA[").append("1").append("]]></pageContext>");
								xmlBuff.append("<objContext><![CDATA[").append("1").append("]]></objContext>");
								xmlBuff.append("<editFlag><![CDATA[").append("A").append("]]></editFlag>");
								xmlBuff.append("<focusedColumn><![CDATA[").append("").append("]]></focusedColumn>");
								xmlBuff.append("<action><![CDATA[").append("SAVE").append("]]></action>");
								xmlBuff.append("<elementName><![CDATA[").append("").append("]]></elementName>");
								xmlBuff.append("<keyValue><![CDATA[").append("1").append("]]></keyValue>");
								xmlBuff.append("<taxKeyValue><![CDATA[").append("").append("]]></taxKeyValue>");
								xmlBuff.append("<saveLevel><![CDATA[").append("1").append("]]></saveLevel>");
								xmlBuff.append("<forcedSave><![CDATA[").append(true).append("]]></forcedSave>");
								xmlBuff.append("<taxInFocus><![CDATA[").append(false).append("]]></taxInFocus>");
								xmlBuff.append("<description>").append("Header0 members").append("</description>");

								xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"voucher\" objContext=\"1\">");
								xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
								xmlBuff.append("<tran_id/>");

								xmlBuff.append("<tran_date><![CDATA[" + sdf.format(currDate) + "]]></tran_date>");
								xmlBuff.append("<eff_date><![CDATA[" + sdf.format(currDate) + "]]></eff_date>");

								xmlBuff.append("<supp_code><![CDATA[" + supp + "]]></supp_code>");
								xmlBuff.append("<bill_date><![CDATA[" +sdf.format(currDate) + "]]></bill_date>");

								xmlBuff.append("<purc_order><![CDATA[" + tranId + "]]></purc_order>");
								xmlBuff.append("<curr_code><![CDATA[" + curr + "]]></curr_code>");
								xmlBuff.append("<exch_rate><![CDATA[" + exch + "]]></exch_rate>");
								xmlBuff.append("<acct_code><![CDATA[" + acctAp + "]]></acct_code>");
								xmlBuff.append("<cctr_code><![CDATA[" + cctrAp + "]]></cctr_code>");
								xmlBuff.append("<bank_code><![CDATA[" + bankCode + "]]></bank_code>");
								xmlBuff.append("<auto_pay><![CDATA[" + "Y" + "]]></auto_pay>");
								xmlBuff.append("<adv_amt><![CDATA["+advPerc+"]]></adv_amt>");
								xmlBuff.append("<cr_term><![CDATA[" + (crTerm==null?"":crTerm) + "]]></cr_term>");
								if ( mileStoneDt != null )
								{
									xmlBuff.append("<due_date><![CDATA[" + sdf.format(mileStoneDt) + "]]></due_date>");
								}
								xmlBuff.append("<site_code><![CDATA[" + site + "]]></site_code>");
								xmlBuff.append("<fin_entity><![CDATA[" + finEnt + "]]></fin_entity>");
								if ( currDate != null )
								{
									xmlBuff.append("<tax_date><![CDATA[" + sdf.format(currDate) + "]]></tax_date>");
								}
								xmlBuff.append("<vouch_type><![CDATA[A]]></vouch_type>");
								xmlBuff.append("<proj_code><![CDATA[" + proj + "]]></proj_code>");
								xmlBuff.append("<confirmed><![CDATA[N]]></confirmed>");
								xmlBuff.append("<pay_mode><![CDATA[" + (payMode==null?"":payMode) + "]]></pay_mode>");
								xmlBuff.append("<rnd_off><![CDATA[" + rndOff + "]]></rnd_off>");
								xmlBuff.append("<rnd_to><![CDATA[" + liRndTo + "]]></rnd_to>");
								xmlBuff.append("<tran_mode><![CDATA[A]]></tran_mode>");
								xmlBuff.append("<acct_code__adv><![CDATA[" + acctApAdv + "]]></acct_code__adv>");
								xmlBuff.append("<cctr_code__adv><![CDATA[" + cctrApAdv + "]]></cctr_code__adv>");

								xmlBuff.append("<bill_amt><![CDATA[0]]></bill_amt>");
								xmlBuff.append("<tax_amt><![CDATA[0]]></tax_amt>");
								xmlBuff.append("<tot_amt><![CDATA[0]]></tot_amt>");
								xmlBuff.append("<net_amt><![CDATA[" + netAmt + "]]></net_amt>");
								xmlBuff.append("<net_amt__bc><![CDATA[" + netAmtBase + "]]></net_amt__bc>");
								xmlBuff.append("<diff_amt__exch><![CDATA[0]]></diff_amt__exch>");
								xmlBuff.append("<supp_bill_amt><![CDATA[" + netAmt + "]]></supp_bill_amt>");

								xmlBuff.append("<tax_chap><![CDATA[" +taxChap  + "]]></tax_chap>");
								xmlBuff.append("<tax_class><![CDATA[" + taxClass + "]]></tax_class>");
								xmlBuff.append("<tax_env><![CDATA[" + taxEnv + "]]></tax_env>");
								xmlBuff.append("<remarks><![CDATA[" +  remarks + "]]></remarks>");
								xmlBuff.append("<chg_user><![CDATA[" + "BASE"  + "]]></chg_user>");
								xmlBuff.append("<chg_term><![CDATA[" +  "BASE" + "]]></chg_term>");
								xmlBuff.append("<chg_date><![CDATA[" + sdf.format(currentDateTime)  + "]]></chg_date>");

								xmlBuff.append("</Detail1>");

								xmlBuff.append("<Detail4 dbID='' domID=\'1\' objName=\"voucher\" objContext=\"2\">");

								xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" updateFlag=\"A\" status=\"N\" />");
								xmlBuff.append("<tran_id/>");
								xmlBuff.append("<line_no>"+(1)+"</line_no>");
								xmlBuff.append("<acct_code><![CDATA[" + acctApAdv + "]]></acct_code>");
								xmlBuff.append("<cctr_code><![CDATA[" + cctrApAdv + "]]></cctr_code>");
								xmlBuff.append("<exch_rate><![CDATA[" + exch + "]]></exch_rate>");
								xmlBuff.append("<amount><![CDATA[" + advPerc + "]]></amount>");
								xmlBuff.append("<tax_chap><![CDATA[" +taxChap  + "]]></tax_chap>");
								xmlBuff.append("<tax_class><![CDATA[" + taxClass + "]]></tax_class>");
								xmlBuff.append("<tax_env><![CDATA[" + taxEnv + "]]></tax_env>");
								xmlBuff.append("<apply_tax><![CDATA[" + 0 + "]]></apply_tax>");

								xmlBuff.append("</Detail4>");

								xmlBuff.append("</Header0>");
								xmlBuff.append("</group0>");
								xmlBuff.append("</DocumentRoot>");
								System.out.println("advxmlBuff.toString() ["+xmlBuff.toString() + "]");
								userId = validatorEJB.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
								System.out.println("userId" + userId);
								errString = saveData(site,xmlBuff.toString(),userId, conn);
								System.out.println("@@@@@2: retString:"+errString);
								System.out.println("--retString finished--");
								if (errString.indexOf("Success") > -1)
								{
									System.out.println("@@@@@@3: Success"+errString);
									errString = "";
								}
								//	ADDED BY NANDKUMAR GADKARI ON 11/03/19------------END---------------------------------------------

							}
						}
					}

					if(errString == null || errString.trim().length() == 0)
					{
						if("PORD".equalsIgnoreCase(asFlag) || "PO".equalsIgnoreCase(asFlag))
						{
							sql = "Update pord_pay_term set vouch_created = 'Y' where  purc_order = ? and  line_no  = ? " ;
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, tranId);
							pstmt.setInt(2, lineNo);
							pstmt.executeUpdate();
							pstmt.close();
							pstmt = null;
						}
					}

				}
				rsNew.close();rsNew = null;
				pstmtNew.close();pstmtNew = null; 
			}
			else
			{
				sqlNew = "select line_no,rel_agnst,amt_type, rel_amt, rel_after,retention_perc,tax_class,tax_chap,tax_env, site_code__adv from  pord_pay_term " +
						"where  purc_order = ? and case when vouch_created is null then 'N' else vouch_created end = 'N'";
				pstmtNew = conn.prepareStatement(sqlNew);
				pstmtNew.setString(1, poId);
				rsNew = pstmtNew.executeQuery();
				while(rsNew.next())
				{
					lineNo = rsNew.getInt("line_no");
					relAgnst = checkNull(rsNew.getString("rel_agnst"));
					advType = checkNull(rsNew.getString("amt_type"));
					adAdvPerc = (double) rsNew.getDouble("rel_amt");
					liRelAfter = rsNew.getInt("rel_after");
					// lcRetPerc = rsNew.getString("retention_perc");
					taxClass = checkNull(rsNew.getString("tax_class"));
					taxChap = checkNull(rsNew.getString("tax_chap"));
					taxEnv = checkNull(rsNew.getString("tax_env"));
					siteCodeAdv = checkNull(rsNew.getString("site_code__adv"));

					if(siteCodeAdv.length() > 0)
					{
						if(!siteCodeAdv.equalsIgnoreCase(siteCodeOrd))
						{
							continue ;
						}
						else
						{
							if(edilink == true)
							{
								continue ;
							}
						}
					}

					if("04".equalsIgnoreCase(relAgnst) || (!lsCode.equalsIgnoreCase(relAgnst)))
					{
						continue ;
					}
					System.out.println("advType....."+advType);
					if("01".equalsIgnoreCase(advType))
					{
						advType = "B";
					}
					else if("02".equalsIgnoreCase(advType))
					{
						advType = "P";
					}
					else if("03".equalsIgnoreCase(advType))
					{
						advType = "F";
					}
					else if("04".equalsIgnoreCase(advType))
					{
						advType = "T";
					}

					if(tranId.equalsIgnoreCase(provPo))
					{
						errString= itmDBAccessLocal.getErrorString("", "VTPURPROV1", "","",conn);
						return errString;
					}

					if(errString != null && errString.trim().length() > 0)
					{
						return errString;
					}

					sql = " select pay_mode,acct_code__ap ,cctr_code__ap ,acct_code__ap_adv, cctr_code__ap_adv" +
							" from  supplier where supp_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,supp);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						payMode = checkNull(rs.getString("pay_mode"));
						acctAp = checkNull(rs.getString("acct_code__ap"));
						cctrAp = checkNull(rs.getString("cctr_code__ap"));
						acctApAdv = checkNull(rs.getString("acct_code__ap_adv"));
						cctrApAdv = checkNull(rs.getString("cctr_code__ap_adv"));
					}
					else
					{
						errString= itmDBAccessLocal.getErrorString("", "VMSUPP1", "","",conn);
						return errString;
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null ;


					invAcct = finCommon.getFinparams("999999", "INV_ACCT_PORCP", conn);
					if(invAcct == null || "".equalsIgnoreCase(invAcct) || invAcct.trim().length() == 0)
					{
						invAcct = "N";
					}


					sql = "select ACCT_CODE__AP_ADV,CCTR_CODE__AP_ADV,acct_code__cr,cctr_code__cr from  porddet where purc_order = ? " ;
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, tranId);
					rs = pstmt.executeQuery();
					while(rs.next())
					{
						//acctApAdv =checkNull( rs.getString("ACCT_CODE__AP_ADV"));  // manoharan 03-aug-18 as per pitambari trigger
						//cctrApAdv = checkNull(rs.getString("CCTR_CODE__AP_ADV"));  // manoharan 03-aug-18 as per pitambari trigger
						acctCodeCr =checkNull( rs.getString("acct_code__cr"));
						cctrCodeCr = checkNull(rs.getString("cctr_code__cr")); 
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null ;

					if(errString.trim().length() == 0)
					{
						if(acctAp == null || acctAp.trim().length() == 0)
						{
							cctrApItem = finCommon.getAcctDetrTtype("", itemSer, "PO", lsType, conn);
							//  gbf_acct_detr_ttype("",ls_itemser,'PO', ls_type);
							acctApItem = distCommon.getToken(cctrApItem, ",");

							if(acctApItem == null )
							{
								errString= itmDBAccessLocal.getErrorString("", "VTSUPPAC", "","",conn);
								return errString;
							}
							acctAp = acctApItem ;
						}

						if(cctrAp == null || cctrAp.trim().length() == 0)
						{
							cctrAp = cctrApItem;
						}
						if(acctCodeCr == null || acctCodeCr.trim().length() == 0)
						{
							acctCodeCr = acctAp;
						}
						if(cctrApAdv == null || cctrApAdv.trim().length() == 0)
						{
							sql = "select cctr_code__cr from  porddet where purc_order = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, tranId);
							rs = pstmt.executeQuery();
							while(rs.next())
							{
								cctrApAdv = rs.getString("cctr_code__cr");
							}
							rs.close();rs = null;
							pstmt.close();pstmt = null ;

						}


						if(cctrAp == null || cctrAp.trim().length() == 0)
						{
							cctrAp = cctrApAdv ; 
						}

						if(errString.trim().length() == 0)
						{
							//changed by mayur on 23-APR-2018
							//sql = "select key_string from transetup where upper(tran_window) = 'w_voucher' " ;
							sql = "select key_string from transetup where upper(tran_window) = 'W_VOUCHER' " ;
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								keyStr = checkNull(rs.getString("key_string"));

							}
							else
							{
								sql = "select key_string from transetup where upper(tran_window) = 'GENERAL' ";
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									keyStr = checkNull(rs.getString("key_string"));
								}
							}
							rs.close();rs = null;
							pstmt.close();pstmt = null;

							//COMMENTED BY NANDKUMAR GADKARI ON 11/03/19 

							/*StringBuffer xmlBuffkeyGen = new  StringBuffer();
							xmlBuffkeyGen.append("<Detail1 dbID='' domID=\"1\" objName=\"voucher\" objContext=\"1\">"); 
							xmlBuffkeyGen.append("<attribute pkNames=\"\" selected=\"N\" updateFlag=\"A\" status=\"N\" />");
							xmlBuffkeyGen.append("<site_code><![CDATA["+site +"]]></site_code>");
							//changes done by mayur on 24-04-18--start
							//xmlBuffkeyGen.append("<tran_date><![CDATA["+currDate+"]]></tran_date>");
							xmlBuffkeyGen.append("<tran_date><![CDATA["+sdf.format(currDate) +"]]></tran_date>");
							//changes done by mayur on 24-04-18--end
							xmlBuffkeyGen.append("<vouch_type><![CDATA["+"A" +"]]></vouch_type>");
							xmlBuffkeyGen.append("</Detail1>");

							System.out.println("xmlBuffkeyGen"+xmlBuffkeyGen.toString());

							TransIDGenerator tg = new TransIDGenerator(xmlBuffkeyGen.toString(), "BASE", CommonConstants.DB_NAME);
							vouchId = tg.generateTranSeqID("VOUCH", "tran_id", keyStr, conn);
							System.out.println("vouchId :"+vouchId);*/

							if("ERROR".equalsIgnoreCase(vouchId))
							{
								errString= itmDBAccessLocal.getErrorString("", "TRANIDERR", "","",conn);
								return errString;
							}

							if("P".equalsIgnoreCase(advType))
							{
								advPerc = amount * (adAdvPerc/100) ;
							}
							else if("F".equalsIgnoreCase(advType))
							{
								advPerc = adAdvPerc ;
							}
							else if("B".equalsIgnoreCase(advType))
							{
								advPerc = ordAmt * (adAdvPerc/100)  ;
							}
							else if("T".equalsIgnoreCase(advType))
							{
								advPerc = (ordAmt * (adAdvPerc/100)) + taxAmt ;
							}
							netAmtBase = advPerc * exch ;
							netAmt = advPerc ;

							sql = "select sum(adv_amt) from voucher where tran_id Between '0' and 'Z' and	vouch_type = 'A' and 	purc_order =  ?";	
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, tranId);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								vouchAmt  = rs.getDouble(1);

								if(totalPoamt < (vouchAmt + advPerc))
								{
									errString= itmDBAccessLocal.getErrorString("", "VTPURCADV", "","",conn);
									return errString;
								}
							}
							rs.close();rs = null;
							pstmt.close();pstmt = null;

							remarks =  "PO:" + tranId  +"Date:"+  sdf.format(currDate) +" Adv. Amt:"+ Double.toString(advPerc);
							System.out.println("remarks"+remarks +"liRelAfter ["+liRelAfter + "]" + "currDate [" +sdf.format(currDate) + "]");
							if(errString.trim().length() == 0)
							{
								if(liRelAfter == 0)
								{
									mileStoneDt = currDate ;
								}
								else
								{
									mileStoneDt = utilMethods.RelativeDate(currDate, liRelAfter);
								}

								remarks = "PO:" + tranId  +"Date:"+ sdf.format(currDate) +" Adv. Amt:"+ Double.toString(amount);
								rndStr = "VOUCH-RND";
								rndOff = finCommon.getFinparams("999999", rndStr, conn);
								System.out.println("rndOff"+rndOff + rndStr);
								if(!"NULLFOUND".equalsIgnoreCase(rndOff))
								{
									rndOff = rndOff.trim();
									rndStr = "VOUCH" + "-RNDTO" ;	
									rndTo = finCommon.getFinparams("999999", rndStr, conn);
									if(!"NULLFOUND".equalsIgnoreCase(rndTo))
									{
										liRndTo = Integer.parseInt(rndTo.trim()) ;
									}
								}
								System.out.println("liRndTo"+liRndTo );

								//commented by nandkumar gadkari on 11/03/19
								/*sqlInsert = " Insert into voucher( tran_id	,tran_date,eff_date,supp_code,bill_no,bill_date,purc_order,curr_code,exch_rate," +
									" acct_code,cctr_code, bank_code	,auto_pay,adv_amt,cr_term,due_date	,chg_date,chg_user,chg_term,site_code," +
									" fin_entity, bill_amt,tax_amt,tot_amt,tax_date,vouch_type, proj_code, confirmed,paid,conf_date, pay_mode, " +
									" net_amt, net_amt__bc,diff_amt__exch ,supp_bill_amt ,acct_code__adv ,cctr_code__adv,tax_chap,  tax_class, " +
									" tax_env ,remarks,tran_mode ,rnd_off,	rnd_to) " +
									" values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
						pstmtInsert = conn.prepareStatement(sqlInsert);
						pstmtInsert.setString(1, vouchId);
						pstmtInsert.setTimestamp(2, currDate); 	
						//pstmtInsert.setTimestamp(3, tranDt); done changes by sarita to set currDate in[eff_date] : 30APR2018
						pstmtInsert.setTimestamp(3, currDate);
						pstmtInsert.setString(4,supp);
						pstmtInsert.setString(5, "");
						//pstmtInsert.setTimestamp(6,tranDt);done changes by sarita to set currDate in[bill_date] : 30APR2018
						pstmtInsert.setTimestamp(6,currDate);
						pstmtInsert.setString(7,tranId);
						pstmtInsert.setString(8, curr);
						pstmtInsert.setDouble(9, exch);
						pstmtInsert.setString(10, acctAp); // acctApAdv); // manoharan 03-aug-18 as per pitambari trigger
						pstmtInsert.setString(11, cctrAp); // cctrApAdv); // manoharan 03-aug-18 as per pitambari trigger
						pstmtInsert.setString(12, bankCode);
						pstmtInsert.setString(13, "Y");
						pstmtInsert.setDouble(14, advPerc);
						pstmtInsert.setString(15, crTerm);
						pstmtInsert.setTimestamp(16,mileStoneDt);
						//pstmtInsert.setTimestamp(17,currDate);//done changes by sarita to set current Date with time[chg_date] : 30APR2018
						pstmtInsert.setTimestamp(17,currentDateTime);
						pstmtInsert.setString(18, "BASE"); //chg USer
						pstmtInsert.setString(19, "BASE"); // LoginCOde
						pstmtInsert.setString(20, site);
						pstmtInsert.setString(21, finEnt);
						pstmtInsert.setDouble(22, 0);
						pstmtInsert.setDouble(23, 0);
						pstmtInsert.setDouble(24, 0);
						//pstmtInsert.setTimestamp(25, tranDt); done changes by sarita to set currDate in[tax_date] : 30APR2018
						pstmtInsert.setTimestamp(25, currDate);
						pstmtInsert.setString(26, "A");
						pstmtInsert.setString(27, proj);
						pstmtInsert.setString(28, "N");
						pstmtInsert.setString(29, null);
						//pstmtInsert.setTimestamp(30, currDate);done changes by sarita to set current Date with time[conf_date] : 30APR2018
						pstmtInsert.setTimestamp(30, currentDateTime);
						pstmtInsert.setString(31, payMode);
						pstmtInsert.setDouble(32, netAmt);
						pstmtInsert.setDouble(33, netAmtBase);
						pstmtInsert.setString(34, null);
						pstmtInsert.setDouble(35, netAmt);
						pstmtInsert.setString(36, acctApAdv);//acctCodeCr); // manoharan 03-aug-18 as per pitambari trigger
						pstmtInsert.setString(37, cctrApAdv);//cctrCodeCr); // manoharan 03-aug-18 as per pitambari trigger
						pstmtInsert.setString(38, taxChap);            
						pstmtInsert.setString(39, taxClass);
						pstmtInsert.setString(40, taxEnv);
						pstmtInsert.setString(41, remarks);
						pstmtInsert.setString(42, "A");
						pstmtInsert.setString(43, rndOff);
						pstmtInsert.setInt(44, liRndTo);        
						pstmtInsert.executeUpdate();
						pstmtInsert.close();
						pstmtInsert = null;


						sqlInsert = "insert into vouchdet( tran_id	, line_no	, acct_code	, cctr_code	, amount,emp_code	, anal_code , tax_amt	, apply_tax) " +
								"values(?,?,?,?,?,?,?,?,?)";
						pstmtInsert = conn.prepareStatement(sqlInsert);
						pstmtInsert.setString(1, vouchId);
						pstmtInsert.setInt(2, 1); 	
						pstmtInsert.setString(3, acctApAdv); //acctCodeCr); // manoharan 03-aug-18 as per pitambari trigger
						pstmtInsert.setString(4, cctrApAdv); //cctrCodeCr); // manoharan 03-aug-18 as per pitambari trigger
						pstmtInsert.setDouble(5,advPerc);
						pstmtInsert.setString(6,null);
						pstmtInsert.setString(7,null);
						pstmtInsert.setDouble(8, lcTax);
						pstmtInsert.setDouble(9, 0);
						pstmtInsert.executeUpdate();
						pstmtInsert.close();
						pstmtInsert = null;


						sql1 = " update voucher set tax_amt  = '"+lcTax +"'," +
							   " tot_amt = tot_amt + '"+lcTax +"' , " +
							   " net_amt = net_amt + '"+lcTax +"' ," +
							   " net_amt__bc = (net_amt + '"+lcTax +"') * exch_rate " +
							   " where tran_id = ? ";
				    	pstmt1 = conn.prepareStatement(sql1);
						pstmt1.setString(1, vouchId);
						pstmt1.executeUpdate();
						pstmt1.close();
						pstmt1 = null;*/

								//	ADDED BY NANDKUMAR GADKARI ON 11/03/19------------START---------------------------------------------
								StringBuffer xmlBuff = new StringBuffer();
								String xmlString = null;



								xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
								xmlBuff.append("<DocumentRoot>");
								xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
								xmlBuff.append("<group0>");
								xmlBuff.append("<description>").append("Group0 description").append("</description>");
								xmlBuff.append("<Header0>");
								xmlBuff.append("<objName><![CDATA[").append("voucher").append("]]></objName>");
								xmlBuff.append("<pageContext><![CDATA[").append("1").append("]]></pageContext>");
								xmlBuff.append("<objContext><![CDATA[").append("1").append("]]></objContext>");
								xmlBuff.append("<editFlag><![CDATA[").append("A").append("]]></editFlag>");
								xmlBuff.append("<focusedColumn><![CDATA[").append("").append("]]></focusedColumn>");
								xmlBuff.append("<action><![CDATA[").append("SAVE").append("]]></action>");
								xmlBuff.append("<elementName><![CDATA[").append("").append("]]></elementName>");
								xmlBuff.append("<keyValue><![CDATA[").append("1").append("]]></keyValue>");
								xmlBuff.append("<taxKeyValue><![CDATA[").append("").append("]]></taxKeyValue>");
								xmlBuff.append("<saveLevel><![CDATA[").append("1").append("]]></saveLevel>");
								xmlBuff.append("<forcedSave><![CDATA[").append(true).append("]]></forcedSave>");
								xmlBuff.append("<taxInFocus><![CDATA[").append(false).append("]]></taxInFocus>");
								xmlBuff.append("<description>").append("Header0 members").append("</description>");

								xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"voucher\" objContext=\"1\">");
								xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
								xmlBuff.append("<tran_id/>");

								xmlBuff.append("<tran_date><![CDATA[" + sdf.format(currDate) + "]]></tran_date>");
								xmlBuff.append("<eff_date><![CDATA[" + sdf.format(currDate) + "]]></eff_date>");

								xmlBuff.append("<supp_code><![CDATA[" + supp + "]]></supp_code>");
								xmlBuff.append("<bill_date><![CDATA[" +sdf.format(currDate) + "]]></bill_date>");

								xmlBuff.append("<purc_order><![CDATA[" + tranId + "]]></purc_order>");
								xmlBuff.append("<curr_code><![CDATA[" + curr + "]]></curr_code>");
								xmlBuff.append("<exch_rate><![CDATA[" + exch + "]]></exch_rate>");
								xmlBuff.append("<acct_code><![CDATA[" + acctAp + "]]></acct_code>");
								xmlBuff.append("<cctr_code><![CDATA[" + cctrAp + "]]></cctr_code>");
								xmlBuff.append("<bank_code><![CDATA[" + bankCode + "]]></bank_code>");
								xmlBuff.append("<auto_pay><![CDATA[" + "Y" + "]]></auto_pay>");
								xmlBuff.append("<adv_amt><![CDATA["+advPerc+"]]></adv_amt>");
								xmlBuff.append("<cr_term><![CDATA[" + (crTerm==null?"":crTerm) + "]]></cr_term>");
								if ( mileStoneDt != null )
								{
									xmlBuff.append("<due_date><![CDATA[" + sdf.format(mileStoneDt) + "]]></due_date>");
								}
								xmlBuff.append("<site_code><![CDATA[" + site + "]]></site_code>");
								xmlBuff.append("<fin_entity><![CDATA[" + finEnt + "]]></fin_entity>");
								if ( currDate != null )
								{
									xmlBuff.append("<tax_date><![CDATA[" + sdf.format(currDate) + "]]></tax_date>");
								}
								xmlBuff.append("<vouch_type><![CDATA[A]]></vouch_type>");
								xmlBuff.append("<proj_code><![CDATA[" + proj + "]]></proj_code>");
								xmlBuff.append("<confirmed><![CDATA[N]]></confirmed>");
								xmlBuff.append("<pay_mode><![CDATA[" + (payMode==null?"":payMode) + "]]></pay_mode>");
								xmlBuff.append("<rnd_off><![CDATA[" + rndOff + "]]></rnd_off>");
								xmlBuff.append("<rnd_to><![CDATA[" + liRndTo + "]]></rnd_to>");
								xmlBuff.append("<tran_mode><![CDATA[A]]></tran_mode>");
								xmlBuff.append("<acct_code__adv><![CDATA[" + acctApAdv + "]]></acct_code__adv>");
								xmlBuff.append("<cctr_code__adv><![CDATA[" + cctrApAdv + "]]></cctr_code__adv>");

								xmlBuff.append("<bill_amt><![CDATA[0]]></bill_amt>");
								xmlBuff.append("<tax_amt><![CDATA[0]]></tax_amt>");
								xmlBuff.append("<tot_amt><![CDATA[0]]></tot_amt>");
								xmlBuff.append("<net_amt><![CDATA[" + netAmt + "]]></net_amt>");
								xmlBuff.append("<net_amt__bc><![CDATA[" + netAmtBase + "]]></net_amt__bc>");
								xmlBuff.append("<diff_amt__exch><![CDATA[0]]></diff_amt__exch>");
								xmlBuff.append("<supp_bill_amt><![CDATA[" + netAmt + "]]></supp_bill_amt>");

								xmlBuff.append("<tax_chap><![CDATA[" +taxChap  + "]]></tax_chap>");
								xmlBuff.append("<tax_class><![CDATA[" + taxClass + "]]></tax_class>");
								xmlBuff.append("<tax_env><![CDATA[" + taxEnv + "]]></tax_env>");
								xmlBuff.append("<remarks><![CDATA[" +  remarks + "]]></remarks>");
								xmlBuff.append("<chg_user><![CDATA[" + "BASE"  + "]]></chg_user>");
								xmlBuff.append("<chg_term><![CDATA[" +  "BASE" + "]]></chg_term>");
								xmlBuff.append("<chg_date><![CDATA[" + sdf.format(currentDateTime)  + "]]></chg_date>");

								xmlBuff.append("</Detail1>");

								xmlBuff.append("<Detail4 dbID='' domID=\'1\' objName=\"voucher\" objContext=\"2\">");

								xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" updateFlag=\"A\" status=\"N\" />");
								xmlBuff.append("<tran_id/>");
								xmlBuff.append("<line_no>"+(1)+"</line_no>");
								xmlBuff.append("<acct_code><![CDATA[" + acctApAdv + "]]></acct_code>");
								xmlBuff.append("<cctr_code><![CDATA[" + cctrApAdv + "]]></cctr_code>");
								xmlBuff.append("<exch_rate><![CDATA[" + exch + "]]></exch_rate>");
								xmlBuff.append("<amount><![CDATA[" + advPerc + "]]></amount>");
								xmlBuff.append("<tax_chap><![CDATA[" +taxChap  + "]]></tax_chap>");
								xmlBuff.append("<tax_class><![CDATA[" + taxClass + "]]></tax_class>");
								xmlBuff.append("<tax_env><![CDATA[" + taxEnv + "]]></tax_env>");
								xmlBuff.append("<apply_tax><![CDATA[" + 0 + "]]></apply_tax>");

								xmlBuff.append("</Detail4>");

								xmlBuff.append("</Header0>");
								xmlBuff.append("</group0>");
								xmlBuff.append("</DocumentRoot>");
								System.out.println("advxmlBuff.toString() ["+xmlBuff.toString() + "]");
								userId = validatorEJB.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
								System.out.println("userId" + userId);
								errString = saveData(site,xmlBuff.toString(),userId, conn);
								System.out.println("@@@@@2: retString:"+errString);
								System.out.println("--retString finished--");
								if (errString.indexOf("Success") > -1)
								{
									System.out.println("@@@@@@3: Success"+errString);
									errString = "";
								}
								//	ADDED BY NANDKUMAR GADKARI ON 11/03/19------------END---------------------------------------------
							}
						}
					}
					if(errString == null || errString.trim().length() == 0)
					{
						if("PORD".equalsIgnoreCase(asFlag) || "PO".equalsIgnoreCase(asFlag))
						{
							sql = "Update pord_pay_term set vouch_created = 'Y' where  purc_order = ? and  line_no  = ? " ;
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, tranId);
							pstmt.setInt(2, lineNo);
							pstmt.executeUpdate();
							pstmt.close();
							pstmt = null;
						}
					}
				}
				rsNew.close();rsNew = null;
				pstmtNew.close();pstmtNew = null;
			}

			System.out.println("errString IN ADVANCE"+errString);	
		}
		catch (SQLException e) 
		{
			System.out.println("Exception :conf ::" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);

		}
		finally
		{
			try
			{
				if(errString != null && errString.trim().length() > 0)
				{
					conn.rollback();
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
	private String gbfPriceListDiscount(String sordSite ,String custCode ,Connection conn) throws ITMException 
	{
		System.out.println("Inside gbfPriceListDiscount............");

		String plistDisc = "",sql= "";
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		try 
		{
			System.out.println("sordSite..."+sordSite + "custCode>>>"+custCode );
			sql = "select price_list__disc from site_customer  where cust_code = ? and site_code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,custCode);
			pstmt.setString(2,sordSite);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				plistDisc = checkNull(rs.getString("price_list__disc"));
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;


			if(plistDisc == null || plistDisc.trim().length() == 0)
			{
				sql = "select price_list__disc from customer  where cust_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,custCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					plistDisc = checkNull(rs.getString("price_list__disc"));
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
			}
		}
		catch (SQLException e) 
		{
			System.out.println("Exception :conf ::" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return plistDisc;		
	}
	
	///--new method for dist order for not channel partner condition----for realestate//addedd by monika salla on 30 july 2020
	private String gbfCreateDistOrdNonCP(String pOrder, String xtraParams ,Connection conn) throws ITMException 
	{
		System.out.println("Inside gbfCreateDistOrd for non channel partner contion.............");

		String errString = "";
		String suppCode= "";
		String sql = "";
		String pordSite =  "";
		String orderType = "";
		String locgroupJwiss	= "";
		String channelPartner = "";
		String sql1 = "";
		String suppSite = "";
		String keyStr = "";
		String siteCode= "";
		String distOrder = "";
		String currCode = "";
		String autoConfirm = "" ;
		String priceList = "";
		String locCodeGit = "";
		String locCodeGitbf = "";
		String autoReciept ="" ;
		String locCodeCons= "" ;
		String remarks ="";
		String bomXml ="";
		String	bom= "";
		String itemCode = "" ;
		String itemCodeDet = "";
		String unitSal = "";
		String unit = ""; 
		String xmlString="" ;
		String qtyDetStr = "" ;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1=null ;
		PreparedStatement pstmtPo = null;
		double refQty = 0.0 ;
		double  fact = 0.0 ;
		double  qtyDet= 0.0;
		ResultSet rs= null ;
		ResultSet rs1 = null ;
		ResultSet rsPo = null;
		int cnt1 = 0;
		int  lineNo = 0;
		int  lineNoref = 0;
		int  xmlStringFrmBom = 0;
		int  supplySiteCount =0;
		Document domBom = null;
		NodeList detlList = null;
		Timestamp pordDate = null;
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		StringBuffer xmlBuff = null;
		ArrayList convQuantityFactArryList = new ArrayList();	
		String userId = "";
        String suppSource="";
        String suppLoctn="";
        String xmlStringBom="", lineNoOrd = "";
		try
		{
			java.sql.Timestamp tranDate = null;
			tranDate = new java.sql.Timestamp(System.currentTimeMillis()) ;
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode"); 
			System.out.println("--login code---1299--"+userId);

			sql = " select supp_code, site_code__dlv, ord_date , pord_type,loc_group__jwiss   " +
					" from porder	where purc_order = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, pOrder);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				suppCode = checkNull(rs.getString("supp_code"));
				pordSite = checkNull(rs.getString("site_code__dlv"));
				pordDate = rs.getTimestamp("ord_date");
				orderType= checkNull(rs.getString("pord_type"));
				locgroupJwiss = checkNull(rs.getString("loc_group__jwiss"));
			}

			rs.close();rs = null;
			pstmt.close();pstmt = null;

			sql = " select channel_partner, site_code__ch from site_supplier	where site_code = ? and supp_code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, pordSite);
			pstmt.setString(2, suppCode);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				channelPartner = checkNull(rs.getString("channel_partner"));
				suppSite  = checkNull(rs.getString("site_code__ch"));
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;


			System.out.println("channelPartner>>>"+channelPartner);
			if(channelPartner == null || channelPartner.trim().length() == 0)
			{

				sql = " select case when channel_partner is null then 'N' else channel_partner end,site_code,supp_locn " +
						" from supplier	where supp_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, suppCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					channelPartner = checkNull(rs.getString(1));
                    suppSite  = rs.getString("site_code");
                    suppLoctn=rs.getString("supp_locn");
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
            }
            
            sql = "select supp_locn from supplier	where supp_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, suppCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
                    suppLoctn=rs.getString("supp_locn");
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;



			System.out.println("channelPartner>>"+channelPartner + "suppSite>>"+suppSite+" suppLoctnion--->"+suppLoctn);
			System.out.println("suppSite dist"+suppSite + "pordSite" +pordSite + "order_date"+ pordDate);
			orderType = distCommon.getDisparams("999999", "DISORDTYP_SUBCTR", conn);
			System.out.println("orderType>>"+orderType);
			if(orderType == null || "NULLFOUND".equalsIgnoreCase(orderType))
			{
				errString = "Not Defined" + "Environmental Variabe DISORDTYP_SUBCTR is not defined";
				return errString;
			}

			// in case of channel partner just return
			if("Y".equalsIgnoreCase(channelPartner))
			{
				errString = "";
				System.out.println("Inside channel partner contion.............");

				return errString;
			}
			else
			{
				System.out.println("Inside  non channel partner contion.............");

				//check whether  DO created for delivery to PO site  from some other supply site
				sql = "select  count(1) from siteitem si, bomdet b, porddet p   "
						+ " where b.bom_code=p.bom_code "
						+ " and  si.site_code = p.site_code "
						+ " and si.item_code = b.item_code "
						+ " and   p.purc_order = ? "
						+ " and p.bom_code is not null "
						+ " and si.supp_sour = 'D' ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,pOrder);
				rs = pstmt.executeQuery();	
				if(rs.next())
				{
					supplySiteCount = rs.getInt(1);
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
				System.out.println("supplysiteCOunt............."+supplySiteCount);

				if (supplySiteCount == 0 )
				{
					// there is no such item to be supplied from other site so return
					return "";
				}
			}
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// populate the list for each supply site
			HashMap SupplySiteMap = new HashMap();
			ArrayList suppSiteItems = null;
			HashMap itemMap = null;

			sql = "SELECT PORDDET.PURC_ORDER,   PORDDET.LINE_NO,      PORDDET.IND_NO,   PORDDET.ITEM_CODE,   PORDDET.QUANTITY,   PORDDET.UNIT,  PORDDET.RATE,  " + 
					"PORDDET.DISCOUNT,   PORDDET.REQ_DATE,   PORDDET.STATUS,   PORDDET.STATUS_DATE,   PORDDET.LOC_CODE,   PORDDET.TAX_CLASS,   PORDDET.TAX_CHAP,   PORDDET.TAX_ENV,   " +   
					"PORDDET.REMARKS,  PORDDET.WORK_ORDER,   PORDDET.TAX_AMT,   PORDDET.TOT_AMT,   PORDDET.DLV_DATE,   PORDDET.DLV_QTY,   PORDDET.UNIT__RATE,   PORDDET.CONV__QTY_STDUOM,    " +  
					"PORDDET.CONV__RTUOM_STDUOM,   PORDDET.UNIT__STD,   PORDDET.QUANTITY__STDUOM,   PORDDET.RATE__STDUOM,   PORDDET.PACK_CODE,   PORDDET.NO_ART,   PORDDET.PACK_INSTR,     " + 
					"PORDDET.ACCT_CODE__DR,   PORDDET.CCTR_CODE__DR,   PORDDET.ACCT_CODE__CR,   PORDDET.CCTR_CODE__CR,   ITEM_A.DESCR,   PORDDET.DISCOUNT_TYPE,   PORDDET.SUPP_CODE__MNFR,  " +    
					"PORDDET.ORDER_OPT,   PORDDET.BOM_CODE,   PORDDET.LINE_NO__SORD,   PORDDET.RATE__CLG,   '' contract_detail,   PORDDET.OP_REASON,   PORDDET.USER_ID__OP,   PORDDET.SPECIFIC_INSTR,   " +   
					"PORDDET.SPECIAL_INSTR,   PORDDET.EMP_CODE__QCAPRV,   EMPLOYEE.EMP_FNAME,   PORDDET.PROJ_CODE,   PORDDET.ITEM_CODE__MFG,   ITEM_B.DESCR,   PORDDET.SPEC_REF,   " +   
					"SPECIFICATION.DESCR,   PORDDET.OPERATION,   PORDDET.STD_RATE,EMPLOYEE.EMP_MNAME,   EMPLOYEE.EMP_LNAME,     PORDER.EXCH_RATE,   PORDDET.DEPT_CODE,     " + 
					"PORDDET.BENEFIT_TYPE,   PORDDET.LICENCE_NO,   PORDDET.ACCT_CODE__PROV_DR,   PORDDET.CCTR_CODE__PROV_DR,   PORDDET.ACCT_CODE__PROV_CR,   PORDDET.CCTR_CODE__PROV_CR,   " +   
					"PORDDET.SPEC_METADATA,   PORDDET.SPEC_DIMENSION,   PORDDET.SUPP_ITEM__REF,   PORDDET.QUANTITY__FC,   PORDDET.PRD_CODE__RFC,   PORDDET.FORM_NO,   PORDDET.DUTY_PAID,   " +   
					"(porddet.quantity * porddet.std_rate ) as std_cost,    " + 
					"PORDDET.ANAL_CODE,   ANALYSIS.DESCR,   PORDDET.ACCT_CODE__AP_ADV,   PORDDET.CCTR_CODE__AP_ADV,   PORDDET.LOT_NO__PASSIGN,   PORDDET.EXP_DATE__PASSIGN   " + 
					"FROM PORDDET,   ITEM ITEM_A,   EMPLOYEE,  ITEM ITEM_B,   SPECIFICATION,   PORDER,   ANALYSIS    " + 
					"WHERE ( porddet.emp_code__qcaprv = employee.emp_code (+)) and    " + 
					"( porddet.item_code__mfg = item_b.item_code (+)) and    " + 
					"( porddet.spec_ref = specification.spec_ref (+)) and    " + 
					"( porddet.anal_code = analysis.anal_code (+)) and    " + 
					"( PORDDET.ITEM_CODE = ITEM_A.ITEM_CODE ) and    " + 
					"( PORDER.PURC_ORDER = PORDDET.PURC_ORDER ) AND    " + 
					"( ( PORDDET.PURC_ORDER = ?) )     " + 
					"ORDER BY PORDDET.LINE_NO ASC   ";
			pstmtPo = conn.prepareStatement(sql);
			pstmtPo.setString(1, pOrder);
			rsPo = pstmtPo.executeQuery();
			lineNo = 1;
			while(rsPo.next())
			{

				System.out.println("INSIDE WHILE LOOP........");
				bom = rsPo.getString("BOM_CODE");
				itemCode = checkNull(rsPo.getString("ITEM_CODE"));
				refQty = rsPo.getDouble("QUANTITY");
				lineNoref = rsPo.getInt("LINE_NO");
				lineNoOrd = rsPo.getString("LINE_NO"); // 29-apr-2021 manoharan 
				System.out.println("bom"+bom + "itemCode"+itemCode );
				bomXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Root><Detail1>";
				bomXml = bomXml + "<site_code>" + pordSite + "</site_code>\r\n";
				bomXml = bomXml + "<item_code>" +itemCode + "</item_code>\r\n";
				bomXml = bomXml + "<quantity>" + refQty + "</quantity>\r\n";
				bomXml = bomXml + "<bom_code>" + bom + "</bom_code>\r\n";
				bomXml = bomXml + "<exp_lev>" + 1 + "</exp_lev>\r\n";
				bomXml = bomXml + "<work_order>" + "XYZ" + "</work_order>\r\n";
				bomXml = bomXml + "</Detail1>\r\n</Root>";

				System.out.println("bomXml is =" + bomXml);

                //xmlStringFrmBom = explodeBom(bomXml,bom,"1.","B","XYZ",conn);
                bomXml = explodeBomStr(bomXml,bom,"1.","B","XYZ",conn);//added by monika salla on 7 august 20
				//xmlStringFrmBom = bomXml.toString(); 
				System.out.println("xmlStringFrmBom"+xmlStringFrmBom +"bomXml.size"+bomXml.length());

				if (xmlStringFrmBom == -1)
				{
					errString = itmDBAccessLocal.getErrorString("", "Failed to explode Bill of Material", "","",conn);
					return errString;
				}
				domBom = genericUtility.parseString(bomXml);
				detlList = domBom.getElementsByTagName("Detail1");		
				System.out.println("detlList>>>["+detlList + "]["+detlList.getLength() + "]");
				if (detlList != null)
				{

					System.out.println("INSIDE LOOP...");
					for(int cntr = 0; cntr < detlList.getLength(); cntr++)
					{

						itemCodeDet = genericUtility.getColumnValueFromNode("item_code", detlList.item(cntr));
						System.out.println("itemcode detail--"+itemCodeDet);
						// check whether item to be supplied from another site 
						sql = " select supp_sour,site_code__supp from siteitem where item_code = ? and site_code = ?  ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCodeDet);
						pstmt.setString(2, pordSite);

						rs = pstmt.executeQuery();
						if(rs.next())
						{
							suppSource =rs.getString("supp_sour");
							//disLink = checkNull(rs.getString("dis_link"));
							suppSite  = rs.getString("site_code__supp");
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
						System.out.println("suppSOurce--["+suppSource+"] site_ code _supp[--"+suppSite);

						if ("D".equalsIgnoreCase(suppSource))
						{
							
							qtyDetStr = genericUtility.getColumnValueFromNode("quantity", detlList.item(cntr));
							System.out.println("qtyDetStr--["+qtyDetStr);
                            
							if(qtyDetStr != null && qtyDetStr.trim().length() > 0)
							{
								qtyDet = Double.parseDouble(qtyDetStr);
                            }
                            // 29-apr-2021 manoharan to consider SUPP_SOURCE of table  proj_const_item_det
							// check the suupp_source for item itemCodeDet if Y then only add for dit order creation
							sql = "select pcd.SUPP_SOURCE " // "select pcd.SUPP_SOURCE, b.PROJ_CODE,b.WING_ID,  b.PHASE_CODE,b.CLSTR_CODE, b.BLDG_CODE,bi.TASK_CODE,bi.ACTIVITY_CODE ,bi.QUANTITY "
							    + " from proj_est_baseline b, proj_est_bsl_item bi, " 
							    + " proj_const_item pci, proj_const_item_det pcd "
							    + " where b.tran_id = bi.tran_id "
							    + " and pci.tran_id = pcd.tran_id "
							    + " and b.VERSION_NO = 0 "
							    + " and pci.proj_code = b.proj_code " 
							    + " and pci.WING_ID = b.WING_ID  "
							    + " and bi.FLOOR_ID >= pci.FLOOR_ID_FR "
							    + " and bi.FLOOR_ID <= pci.FLOOR_ID_TO "
							    + " and pci.PHASE_CODE = b.PHASE_CODE "
							    + " and pci.CLSTR_CODE = b.CLSTR_CODE "
							    + " and pci.BLDG_CODE  = b.BLDG_CODE "
							    + " and bi.ACTIVITY_CODE = pci.ACTIVITY_CODE " 
								+ " and pcd.item_code = ? "
								+ " and bi.purc_order = ? "
								+ " and trim(bi.line_no__ord) = trim(?) ";

							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCodeDet);
							pstmt.setString(2, pOrder);
							pstmt.setString(3, lineNoOrd);

							rs = pstmt.executeQuery();
							if (rs.next())
							{
								suppSource =rs.getString("SUPP_SOURCE");
							}
							rs.close();rs = null;
							pstmt.close();pstmt = null;
							System.out.println("29-apr-2021 manoharan suppSource before [" +suppSource + "]");
							if(suppSource == null || "null".equals(suppSource))
							{
								suppSource = "Y";
							}
							System.out.println("29-apr-2021 manoharan suppSource after [" +suppSource + "]");
							if("N".equals(suppSource))
							{
								System.out.println("29-apr-2021 manoharan suppSource after [" +suppSource + "] akipping item [" + itemCodeDet + "]");
								continue; //  skip the item from dist order detail
							}
							// end 29-apr-2021 manoharan to consider SUPP_SOURCE of table  proj_const_item_det
							
							if(SupplySiteMap.containsKey(suppSite))
							{
								suppSiteItems = (ArrayList) SupplySiteMap.get(suppSite);
							}
							else
							{
								suppSiteItems = new ArrayList();
							}
							// create item map to store item code and quantity
							itemMap = new HashMap();
							itemMap.put("item_code",itemCodeDet);
							itemMap.put("quantity",qtyDet);

							suppSiteItems.add(itemMap);

							SupplySiteMap.put(suppSite,suppSiteItems);
						}//closing of supply site
					}
				}
			}

			rsPo.close();rsPo = null;
			pstmtPo.close();pstmtPo = null;
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			sql = "select key_string from transetup where upper(tran_window) = 'W_DIST_ORDER'" ;
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				keyStr = checkNull(rs.getString("key_string"));
			}
			else
			{
				sql = "select key_string from transetup where upper(tran_window) = 'GENERAL'";
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();	
				if(rs.next())
				{
					keyStr = checkNull(rs.getString(1));
				}
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;

			///////////// for each supplysite create DO separately
			Set setItem = SupplySiteMap.entrySet();
			suppSiteItems =  null;
			Iterator itrItem = setItem.iterator();
			while(itrItem.hasNext())
			{
				Map.Entry itemMapEntry = (Map.Entry)itrItem.next();
				suppSite = (String)itemMapEntry.getKey();
				suppSiteItems = (ArrayList)SupplySiteMap.get(suppSite);

				xmlBuff = null;
				xmlBuff = new StringBuffer();
				System.out.println("--XML CREATION --" + tranDate + "tranDate" +sdf.format(tranDate).toString() );
				xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
				xmlBuff.append("<DocumentRoot>");
				xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
				xmlBuff.append("<group0>");
				xmlBuff.append("<description>").append("Group0 description").append("</description>");
				xmlBuff.append("<Header0>");
				xmlBuff.append("<objName><![CDATA[").append("dist_order").append("]]></objName>");  
				xmlBuff.append("<pageContext><![CDATA[").append("1").append("]]></pageContext>");
				xmlBuff.append("<objContext><![CDATA[").append("1").append("]]></objContext>");
				xmlBuff.append("<editFlag><![CDATA[").append("A").append("]]></editFlag>");
				xmlBuff.append("<focusedColumn><![CDATA[").append("").append("]]></focusedColumn>");
				xmlBuff.append("<action><![CDATA[").append("SAVE").append("]]></action>");
				xmlBuff.append("<elementName><![CDATA[").append("").append("]]></elementName>");
				xmlBuff.append("<keyValue><![CDATA[").append("1").append("]]></keyValue>");
				xmlBuff.append("<taxKeyValue><![CDATA[").append("").append("]]></taxKeyValue>");
				xmlBuff.append("<saveLevel><![CDATA[").append("1").append("]]></saveLevel>");
				xmlBuff.append("<forcedSave><![CDATA[").append(true).append("]]></forcedSave>");
				xmlBuff.append("<taxInFocus><![CDATA[").append(false).append("]]></taxInFocus>");
				xmlBuff.append("<description>").append("Header0 members").append("</description>");	
				xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"dist_order\" objContext=\"1\">");  
				xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
				xmlBuff.append("<dist_order/>");
				xmlBuff.append("<order_type><![CDATA["+ orderType +"]]></order_type>");
				xmlBuff.append("<order_date><![CDATA["+ sdf.format(tranDate).toString() +"]]></order_date>");
				xmlBuff.append("<site_code><![CDATA["+ pordSite +"]]></site_code>");
				xmlBuff.append("<site_code__ship><![CDATA["+ suppSite   +"]]></site_code__ship>");
				xmlBuff.append("<site_code__dlv><![CDATA["+pordSite +"]]></site_code__dlv>");
				xmlBuff.append("<ship_date><![CDATA["+ sdf.format(tranDate).toString()   +"]]></ship_date>");

				if(locgroupJwiss != null && locgroupJwiss.trim().length()>0)
				{
					xmlBuff.append("<loc_group__jwiss><![CDATA["+ locgroupJwiss  +"]]></loc_group__jwiss>");
				}
				else
				{
					sql = "select loc_group__jwiss from distorder_type where tran_type = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, orderType);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						locgroupJwiss = checkNull(rs.getString("loc_group__jwiss"));
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;
					xmlBuff.append("<loc_group__jwiss><![CDATA["+ locgroupJwiss  +"]]></loc_group__jwiss>");
				}

				sql = "select a.curr_code from finent a, site b where a.fin_entity = b.fin_entity and b.site_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, pordSite);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					currCode = checkNull(rs.getString(1));
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;

				xmlBuff.append("<curr_code><![CDATA["+ currCode  +"]]></curr_code>");
				xmlBuff.append("<exch_rate><![CDATA["+ 1  +"]]></exch_rate>");
				autoConfirm = distCommon.getDisparams("999999", "SUBCTR_DORD_CONF", conn);
				if("".equalsIgnoreCase(autoConfirm) || autoConfirm == null)
				{
					autoConfirm = "Y";
				}
				System.out.println("autoConfirm>>"+autoConfirm + "autoConfirm.contains" + autoConfirm.contains("N"));	
				if(!autoConfirm.contains("Y") && !autoConfirm.contains("N"))
				{
					errString = itmDBAccessLocal.getErrorString("", "VTDISPARM", "","",conn);
					return errString;
				}
				xmlBuff.append("<confirmed><![CDATA["+"N"+"]]></confirmed>");
				xmlBuff.append("<conf_date><![CDATA["+sdf.format(tranDate).toString()+"]]></conf_date>");
				xmlBuff.append("<tran_type><![CDATA["+orderType+"]]></tran_type>");
				xmlBuff.append("<purc_order><![CDATA["+pOrder+"]]></purc_order>");
				priceList = distCommon.getDisparams("999999", "PRICELIST_DIS_SUBCTR", conn);
				System.out.println("priceList>>"+priceList+"supp location--->"+ suppLoctn);	
				if(priceList != null && !"NULLFOUND".equalsIgnoreCase(priceList))
				{
					xmlBuff.append("<price_list><![CDATA["+priceList+"]]></price_list>");
				}

				sql = "select loc_code__git,loc_code__gitbf,loc_code__cons,auto_receipt from distorder_type where tran_type = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, orderType);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					cnt1 = 0;
					locCodeGit = checkNull(rs.getString("loc_code__git"));
					locCodeGitbf = checkNull(rs.getString("loc_code__gitbf"));
					locCodeCons = checkNull(rs.getString("loc_code__cons"));
					autoReciept = checkNull(rs.getString("auto_receipt"));
					cnt1++;
				}

				rs.close();rs = null;
				pstmt.close();pstmt = null;

				if(cnt1 == 0)
				{
					errString = itmDBAccessLocal.getErrorString("", "VTDORDTY", "","",conn);
					return errString;
				}
				xmlBuff.append("<loc_code__git><![CDATA["+locCodeGit+"]]></loc_code__git>");
				xmlBuff.append("<loc_code__gitbf><![CDATA["+locCodeGitbf+"]]></loc_code__gitbf>");
                //xmlBuff.append("<loc_code__cons><![CDATA["+locCodeCons+"]]></loc_code__cons>");//added by monika salla on 7 august 2020 to set the value from supplier
                xmlBuff.append("<loc_code__cons><![CDATA["+suppLoctn+"]]></loc_code__cons>"); 
				xmlBuff.append("<auto_receipt><![CDATA["+autoReciept+"]]></auto_receipt>");
				xmlBuff.append("<status><![CDATA["+"P"+"]]></status>");
				remarks = "Auto created from PO No. : " + pOrder + " Dated " + pordDate ;
				xmlBuff.append("<remarks><![CDATA["+ remarks +"]]></remarks>");
				xmlBuff.append("<purc_order><![CDATA["+ pOrder +"]]></purc_order>");
				xmlBuff.append("</Detail1>");

				//////////////////////// detail for each list	
                for (int i =0; i < suppSiteItems.size(); i++)
				{
                    System.out.println("itemCodeDet--["+itemCodeDet+"] qtyDet[--"+qtyDetStr);
                    //added by monika to populate BOM Detail MAP
                    HashMap curMap = (HashMap)suppSiteItems.get(i);
                    itemCodeDet = (String) curMap.get("item_code");
                    qtyDet= (Double) curMap.get("quantity");

                
                    System.out.println("Map itemCodeDet--["+itemCodeDet+"] qtyDet[--"+qtyDetStr);
                    
					xmlBuff.append("<Detail2 dbID='' domID=\""+lineNo +"\" objName=\"dist_order\" objContext=\"2\">"); 
					xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" updateFlag=\"A\" status=\"N\" />");
					xmlBuff.append("<line_no><![CDATA["+lineNo +"]]></line_no>");
					xmlBuff.append("<item_code><![CDATA["+itemCodeDet +"]]></item_code>");
					xmlBuff.append("<qty_order><![CDATA["+qtyDet +"]]></qty_order>");
					xmlBuff.append("<qty_confirm><![CDATA["+qtyDet +"]]></qty_confirm>");
					xmlBuff.append("<qty_shipped><![CDATA["+ 0 +"]]></qty_shipped>");
					xmlBuff.append("<qty_received><![CDATA["+0 +"]]></qty_received>");
					xmlBuff.append("<qty_return><![CDATA["+0 +"]]></qty_return>");
					xmlBuff.append("<rate><![CDATA["+0 +"]]></rate>");

					sql1 = "select unit,unit__sal from item where item_code = ?  ";
					pstmt1 = conn.prepareStatement(sql1);
					pstmt1.setString(1, itemCodeDet);
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						unit = rs1.getString("unit");
						unitSal = rs1.getString("unit__sal");
					}
					rs1.close();rs1 = null;
					pstmt1.close();pstmt1 = null;
					if(unitSal == null || unitSal.trim().length() == 0)
					{
						unitSal = unit ;
					}
					xmlBuff.append("<unit><![CDATA["+unit +"]]></unit>");
					xmlBuff.append("<unit__alt><![CDATA["+unitSal +"]]></unit__alt>");
					fact = 0 ;
					convQuantityFactArryList = distCommon.getConvQuantityFact(unitSal,unit,itemCodeDet,qtyDet,fact,conn);
					System.out.println("convQuantityFactArryList"+convQuantityFactArryList);		
					xmlBuff.append("<conv__qty__alt><![CDATA["+fact +"]]></conv__qty__alt>");
					xmlBuff.append("<qty_order__alt><![CDATA["+convQuantityFactArryList.get(0) +"]]></qty_order__alt>");
					xmlBuff.append("<line_no__pord><![CDATA["+lineNoref +"]]></line_no__pord>");

					System.out.println("...............distOrder"+distOrder);
					xmlBuff.append("<dist_order/>");
					xmlBuff.append("</Detail2>");
					lineNo++ ;
				}
				//	pura loop coooooomplete hone ke baad arraylist mein save data ko call hona chahiye
				xmlBuff.append("</Header0>");
				xmlBuff.append("</group0>");
				xmlBuff.append("</DocumentRoot>");
				xmlString = xmlBuff.toString();
				System.out.println("@@@@@2: xmlString:"+xmlBuff.toString());
				System.out.println("...............just before savdata distorder()");
				siteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
				System.out.println("== site code =="+siteCode);
				System.out.println("userId@1642["+userId+"]");
				errString = saveData(siteCode,xmlString,userId, conn);
				System.out.println("@@@@@2: retString:"+errString);
				System.out.println("--retString finished--");
				if (errString.indexOf("Success") > -1)
				{
					System.out.println("@@@@@@3: Success"+errString);
					//conn.commit();
					//errString = "";
				}
				else
				{
					System.out.println("[SuccessSuccess" + errString + "]");	
					conn.rollback();
					return errString;
				}
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new ITMException(e);
		} 
		finally
		{
			try
			{
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if(pstmt1 != null)
				{
					pstmt1.close();
					pstmt1 = null;					
				}
				if(rs != null)
				{
					rs.close();
					rs = null;
				}
				if(rs1 != null)
				{
					rs1.close();
					rs1 = null;
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