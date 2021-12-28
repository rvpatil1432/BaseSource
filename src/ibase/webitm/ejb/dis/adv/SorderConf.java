/********************************************************
	Title : SorderConf[D16BSUN008]
	Date  : 24/05/16
	Developer: Chandrashekar

 ********************************************************/
package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.*;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.dis.PostOrdCreditChk;
import ibase.webitm.ejb.dis.PostOrderProcess;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.sys.CreateRCPXML;
import ibase.webitm.ejb.sys.UtilMethods;
import java.sql.*;
import java.text.SimpleDateFormat;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;
import javax.ejb.Stateless;
import org.w3c.dom.Document;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

@Stateless
public class SorderConf extends ActionHandlerEJB implements SorderConfLocal, SorderConfRemote
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	FinCommon finCommon = new FinCommon();
	DistCommon disCommon= new DistCommon();
	UtilMethods utilMethod = new UtilMethods();
	public String confirm(String saleOrder, String xtraParams, String forcedFlag)throws RemoteException, ITMException
	{
		System.out.println(">>>>>>>>>>>>>>>>>>SorderConf called>>>>>>>>>>>>>>>>>>>");
		String confirmed = "";
		String sql = "";
		Connection conn = null;
		PreparedStatement pstmt = null,pstmt1=null;
		String errString = null;
		String refSer = "",winName= "",custCode="",itemSer="",siteCode="",status="",stopBusiness="",automps="",custCodeBil="";
		String errCode="",totalAmtStr="";
		ResultSet rs = null,rs1=null;
		double totAmt=0.0,totalAmt=0.0;
		java.sql.Date orderDate = null;
		Timestamp dueDate=null;
		int cnt = 0,cnt1 = 0, updCnt1=0;
		int retCtr=0;
		Boolean isSaleOrder=false;
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		Timestamp sysDate = null;
		E12GenericUtility genericUtility= new  E12GenericUtility();
		PostOrderProcess postPrc= new PostOrderProcess();
		ValidatorEJB valEJB=new ValidatorEJB();
		PostOrdCreditChk postcrdchk= new PostOrdCreditChk();
		ArrayList CreditCheckList= new ArrayList();
		try 
		{
			ConnDriver connDriver = null;
			//connDriver = new ConnDriver();
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			conn.setAutoCommit(false);

			if (saleOrder != null && saleOrder.trim().length() > 0) 
			{
				//Changes and Commented By Ajay on 22-12-2017:START
				//sql = "	select cust_code , item_ser , site_code ,	due_date	, tot_amt, confirmed , " +
				//		" status, order_date   from sorder where sale_order = ? ";
				/*sql = "select cust_code ,cust_code__bil, item_ser , site_code ,	due_date	, tot_amt, confirmed , " +
						"(CASE WHEN status IS NULL THEN 'P' ELSE status END) as status , order_date from sorder where sale_order = ?";
				//Changes and Commented By Ajay on 22-12-2017:END
*/				//Changed By PriyankaC on 19June2019 [Start]
				sql =  " select cust_code ,cust_code__bil, item_ser , site_code ,	due_date	, (case when sorder.tot_amt = 0 then sorder.tot_ord_value else sorder.tot_amt  end  ) as tot_amt, confirmed ," 
					+  "(CASE WHEN status IS NULL THEN 'P' ELSE status END) as status , order_date from sorder where sale_order = ? " ;

				//Changed By PriyankaC [END].
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, saleOrder);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					isSaleOrder=true;
					custCode = rs.getString("cust_code");
					//Added by Kunal to get cust_code__bil
					custCodeBil = rs.getString("cust_code__bil");
					itemSer = rs.getString("item_ser");
					siteCode = rs.getString("site_code");
					dueDate = rs.getTimestamp("due_date");
					totalAmtStr = rs.getString("tot_amt");
					confirmed = rs.getString("confirmed");
					status = rs.getString("status");
					orderDate=rs.getDate("order_date");
				}
				System.out.println("***totalAmtStr VAlues after sql execution["+totalAmtStr);
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(!isSaleOrder)
				{
					errString = itmDBAccessLocal.getErrorString("", "VTMCONF20", "","",conn);
					return errString;
				}else if(!"P".equalsIgnoreCase(status))
				{
					errString = itmDBAccessLocal.getErrorString("", "VTSOSTAT", "Sales order is not in pending status","",conn);
					return errString;

				}else if ("Y".equalsIgnoreCase(confirmed))
				{
					errString = itmDBAccessLocal.getErrorString("", "VTSCONF1", "","",conn);
					return errString;

				}else
				{
					sql = "select stop_business from customer where cust_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, custCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						stopBusiness = rs.getString("stop_business");
					}
					System.out.println("confirmed>>>>>>>>" + confirmed);
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if("Y".equalsIgnoreCase(stopBusiness))
					{
						errString = itmDBAccessLocal.getErrorString("", "VTICCW", "Sales Order " + saleOrder + " Not Confirmed,","",conn);
						return errString;
					}
					//Changes and Commented By Ajay on 20-12-2017 :START
					//errCode = valEJB.nfCheckPeriod("SAL", orderDate, siteCode);
					errCode=finCommon.nfCheckPeriod("SAL", orderDate, siteCode, conn);
					//Changes and Commented By Ajay on 20-12-2017 :END
					if(errCode.trim().length() > 0)
					{
						errString = itmDBAccessLocal.getErrorString("", errCode, "","",conn);
						return errString;
					}

					sql = "SELECT count(*) as cnt FROM sorddet WHERE sale_order =? AND quantity__stduom ='0'";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, saleOrder);
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						cnt = rs1.getInt(1);
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;

					// The Sale order should confirm even if there are no
					// records in the detail
					if(cnt == 0)
					{
						sql = "SELECT count(*) AS cnt1 FROM sorddet WHERE sale_order= ? AND quantity='0'";
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1, saleOrder);
						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{
							cnt1 = rs1.getInt(1);
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;

						if(cnt1 > 0)
						{
							errString = itmDBAccessLocal.getErrorString("", "'VTSCONF2''", "","",conn);
							return errString;
						}

						HashMap CreditCheckMap = new HashMap();
						CreditCheckMap.put("as_cust_code_bil", custCodeBil );
						//added by kunal on 5/11/2018 to add cust_code__bill in Map
						CreditCheckMap.put("as_cust_code_sold_to", custCode);
						CreditCheckMap.put("as_item_ser", itemSer);
						//CreditCheckMap.put("ad_net_amt", totalAmt);
						CreditCheckMap.put("ad_net_amt", totalAmtStr);
						CreditCheckMap.put("as_sorder", saleOrder);
						CreditCheckMap.put("adt_tran_date", dueDate);
						CreditCheckMap.put("as_site_code", siteCode);
						CreditCheckMap.put("as_apply_time", "S");
						CreditCheckMap.put("as_despid", "");
						System.out.println("Credit check custCodeBill["+custCodeBil+"]");
						CreditCheckList = postcrdchk.CreditCheck(CreditCheckMap, conn);
						//Pavan Rane 27aug19 start[to display error message to front end]
						if(CreditCheckList.size() > 0 && CreditCheckList.contains("Error"))
						{
							conn.rollback();
							errString = CreditCheckList.get(CreditCheckList.indexOf("Error")+1).toString();
							return errString;
						}	
						//if(CreditCheckList.size() > 0)
						else if(CreditCheckList.size() > 0)//Pavan Rane 27aug19 end
						{						
							conn.rollback();
							retCtr = postPrc.writeBusinessLogicCheck(CreditCheckList, siteCode, "S", conn);

							System.out.println("@@@@@@@@@ insert retCtr[" + retCtr + "]errStringList.size()[" + CreditCheckList.size() + "]");
							if(retCtr > 0)
							{
								System.out.println("@@@@@@@@@ errorlist and inserted record missmatch........");
								conn.commit();
							}
							errString = itmDBAccessLocal.getErrorString("", "VTWBLGCCHK", "","",conn);
							System.out.println("@@@@@@@@@@ writeBusinessLogicCheck errString[" + errString + "]");
							return errString;
						}
						errString = retriveSaleOrder(saleOrder, xtraParams, conn);
						System.out.println("retriveSaleOrder error code>>>>>" + errString);
						if(errString !=null && errString.trim().length() > 0)
						{
							System.out.println(">>>transaction not confirmaed");
						} else
						{
							String mainStr="";
							System.out.println(">>>Successful transaction confirmaed");
							errString = itmDBAccessLocal.getErrorString("", "VTCNFSUCC", "","",conn);
							System.out.println("errString@@@>>>"+errString);
							String begPart = errString.substring( 0, errString.indexOf("<trace>") + 7 );
							String endPart = errString.substring( errString.indexOf("</trace>"));
							mainStr="Sales order ";
							mainStr=begPart+mainStr+saleOrder+" is confirmed . ";
							if(mainStr.trim().length()==0)
							{
								mainStr = begPart;
							}
							mainStr = mainStr +  endPart;	
							errString = mainStr;
						}
					}
					//Modified by Anjali R. on [30/11/2018][Start]
					else
					{
						errString = itmDBAccessLocal.getErrorString("","VTSCONF3","","",conn);
						return errString;
					}
					//Modified by Anjali R. on [30/11/2018][End]
				}
			}
		} catch (Exception e) 
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
				if(errString != null && errString.trim().length() > 0)
				{
					System.out.println("--going to commit tranaction--");
					if(errString.indexOf("VTCNFSUCC") > -1)
					{
						conn.commit();
						System.out.println("--transaction commited--");
					}
					else
					{
						conn.rollback();
						System.out.println("--transaction rollback--");
					}
				}
				if(conn != null)
				{
					conn.close();
					conn = null;
				}
				if(rs != null) 
				{
					rs.close();
					rs = null;
				}
				if(pstmt != null) 
				{
					pstmt.close();
					pstmt = null;
				}
			}
			catch(Exception e)
			{
				System.out.println("Exception : "+e);e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return errString;
	}
	//Pavan R on 24sept18 start [to confirm sorderAmd on business override logic]
	public String confirmSorder(String saleOrder,String temp, String xtraParams, Connection conn) throws ITMException 
	{
		System.out.println("----------inside sorder confirmSorder........................");
		String sql = "";
		String errString = "";
		Boolean isSaleOrder=false;
		PreparedStatement pstmt = null;
		ITMDBAccessEJB itmDBAcces = null;
		ResultSet rs = null;
		int cnt = 0;
		java.sql.Date orderDate = null;
		String custCode="";		
		String siteCode="";
		String confirmed="";
		String status="";
		String stopBusiness="";
		String errCode="";
		try 
		{
			itmDBAcces = new ITMDBAccessEJB();
			if (saleOrder != null && saleOrder.trim().length() > 0) 
			{
				sql = "select cust_code, site_code, confirmed, (CASE WHEN status IS NULL THEN 'P' ELSE status END) as status, order_date "
					  + " from sorder where sale_order = ?";				
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, saleOrder);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					isSaleOrder=true;
					custCode = rs.getString("cust_code");
					//itemSer = rs.getString("item_ser");
					siteCode = rs.getString("site_code");
					//dueDate = rs.getTimestamp("due_date");
					//totalAmtStr = rs.getString("tot_amt");
					confirmed = rs.getString("confirmed");
					status = rs.getString("status");
					orderDate=rs.getDate("order_date");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(!isSaleOrder)
				{
					errString = itmDBAcces.getErrorString("", "VTMCONF20", "","",conn);
					return errString;
				}else if(!"P".equalsIgnoreCase(status))
				{
					errString = itmDBAcces.getErrorString("", "VTSOSTAT", "Sales order is not in pending status","",conn);
					return errString;

				}else if ("Y".equalsIgnoreCase(confirmed))
				{
					errString = itmDBAcces.getErrorString("", "VTSCONF1", "","",conn);
					return errString;

				}else
				{					
					sql = "select stop_business from customer where cust_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, custCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						stopBusiness = rs.getString("stop_business");
					}
					System.out.println("confirmed>>>>>>>>[" + confirmed + "]  stopBusiness>>>>>[" + stopBusiness+"]");
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if("Y".equalsIgnoreCase(stopBusiness))
					{
						errString = itmDBAcces.getErrorString("", "VTICCW", "Sales Order " + saleOrder + " Not Confirmed,","",conn);
						return errString;
					}					
					errCode=finCommon.nfCheckPeriod("SAL", orderDate, siteCode, conn);
					System.out.println("nfCheckPeriod  errCode["+errCode+"]");
					if(errCode.trim().length() > 0)
					{
						errString = itmDBAcces.getErrorString("", errCode, "","",conn);
						return errString;
					}

					sql = "SELECT count(*) as cnt FROM sorddet WHERE sale_order =? AND quantity__stduom ='0'";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, saleOrder);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						cnt = rs.getInt(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if(cnt == 0)
					{
						errString = retriveSaleOrder(saleOrder, xtraParams, conn);
						System.out.println("retriveSaleOrder error code>>>>>" + errString);
					}
					if(errString !=null && errString.trim().length() > 0)
					{
						System.out.println(">>>>transaction not confirmaed");
					} else
					{
						errString = itmDBAcces.getErrorString("", "VTCNFSUCC", "","",conn);
					}
				}
			}
		}catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		return errString;
	}
	//Pavan R on 24sept18 end

	private String retriveSaleOrder(String saleOrder,String xtraParams, Connection conn) throws ITMException 
	{
		String sql = "",errorCode = "";
		PreparedStatement pstmt = null,pstmt1=null;
		String errString ="";
		ResultSet rs = null,rs1=null;
		int cnt=0,updCnt=0;
		String sordLineNo="";
		String custCode="",siteCode="",channelPartner="",disLink="",ediOption="";
		String winName="w_sorder",dataStr="",retString="";
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		double ordAmount=0.0,exchRate=0.0,commPerc1=0.0,commPerc2=0.0,commPerc3=0.0,totalCommAmt=0.0,spComm1=0.0,spComm2=0.0,spComm3=0.0;
		double totalCommSp1=0.0,totalCommSp2=0.0,totalCommSp3=0.0,commAmtOc=0.0,minShelfLife=0.0,minShelfPerc=0.0,maxLife=0.0,shelfLife=0.0,tempLife=0.0,maxShelfLife=0.0,qtyOrd=0.0;
		double totalComm=0.0;
		String loginEmpCode="";
		Timestamp sysDate = null;
		HashMap commissionMap = null;
		DistCommon distCommom = new DistCommon();
		//start implement
		try 
		{
			loginEmpCode =(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode"));
			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String sysDateStr = sdf.format(currentDate.getTime());
			sysDate = Timestamp.valueOf(genericUtility.getValidDateString(sysDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");


			sql = "SELECT site_code,due_date,confirmed,quot_no,contract_no,tot_amt,ord_amt,"
					+ "(CASE WHEN comm_perc__on IS NULL THEN '' ELSE comm_perc__on END) AS comm_perc__on,"
					+ "(CASE WHEN comm_perc_on_1 IS NULL THEN '' ELSE comm_perc_on_1 END) AS comm_perc_on_1,"
					+ "(CASE WHEN comm_perc_on_2 IS NULL THEN '' ELSE comm_perc_on_2 END) AS comm_perc_on_2,"
					+ "(CASE WHEN comm_perc IS NULL THEN 0 ELSE comm_perc END) AS comm_perc,"
					+ "(CASE WHEN comm_perc_1 IS NULL THEN 0 ELSE comm_perc_1 END) AS comm_perc_1,"
					+ "(CASE WHEN comm_perc_2 IS NULL THEN 0 ELSE comm_perc_2 END) AS comm_perc_2,exch_rate,order_type,"
					+ "price_list__disc,proj_code,cust_code FROM  sorder WHERE sale_order = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, saleOrder);
			rs = pstmt.executeQuery();
			if (rs.next()) 
			{
				siteCode = rs.getString("site_code");
				custCode = rs.getString("cust_code");
				ordAmount = rs.getDouble("ord_amt");
				exchRate = rs.getDouble("exch_rate");
				commPerc1 = rs.getDouble("comm_perc");
				commPerc2 = rs.getDouble("comm_perc_1");
				commPerc3 = rs.getDouble("comm_perc_2");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			ordAmount = ordAmount * exchRate;
			//get count......
			sql ="SELECT count(1) AS cnt FROM sorddet WHERE sale_order = ?";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, saleOrder);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				cnt=rs.getInt(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if(cnt==0)
			{
				errString = itmDBAccessLocal.getErrorString("","VTNODET1","","",conn);//removed single quotes from msg_no by nandkumar gadkari on 2/02/19
				return errString;
			}

			if(commPerc1 > 0 || commPerc2 > 0 || commPerc3 > 0)
			{
				commissionMap = calcCommission(saleOrder,"", conn);
				errString= checkNull((String)commissionMap.get("errorStr"));
				if(errString.trim().length() > 0)
				{
					return errString;
				}
				totalCommSp1= checkDouble((Double)commissionMap.get("sp1Comm"));
				totalCommSp2= checkDouble((Double)commissionMap.get("sp2Comm"));
				totalCommSp3= checkDouble((Double)commissionMap.get("sp3Comm"));
				totalCommAmt = checkDouble((Double)commissionMap.get("netComm"));

				if(exchRate==0)
				{
					exchRate=1;
				}
				//check if total commision exceeds Order Amount. 
				if(totalCommAmt > ordAmount)
				{
					errString = itmDBAccessLocal.getErrorString("","VTCOMMERR","","",conn);//removed single quotes from msg_no by nandkumar gadkari on 2/02/19
					return errString;
				}
			}
			else
			{
				sql ="SELECT sale_order, line_no FROM sorddet WHERE sale_order = ? ORDER BY line_no";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1, saleOrder);
				rs = pstmt.executeQuery();
				while(rs.next())
				{
					saleOrder=rs.getString("sale_order");
					sordLineNo=rs.getString("line_no");
					//calculate Commission for current Line no
					commissionMap = calcCommission(saleOrder,sordLineNo, conn);
					//store each sales person's comm amt in separate variables.
					errString= checkNull((String)commissionMap.get("errorStr"));
					if(errString.trim().length() > 0)
					{
						rs.close();	rs = null;//23feb19[to close the cursor and pstmt while returning string]
						pstmt.close();pstmt = null;
						return errString;
					}
					spComm1 = checkDouble((Double)commissionMap.get("sp1Comm"));
					spComm2 = checkDouble((Double)commissionMap.get("sp2Comm"));
					spComm3 = checkDouble((Double)commissionMap.get("sp3Comm"));
					totalCommAmt = totalCommAmt +checkDouble((Double)commissionMap.get("netComm"));
					totalCommSp1= totalCommSp1 + spComm1;
					totalCommSp2= totalCommSp2 + spComm2;
					totalCommSp3= totalCommSp3 + spComm3;

					if(exchRate==0)
					{
						exchRate=1;
					}

					//check if total commision exceeds Order Amount. 
					if(totalCommAmt > ordAmount)
					{
						rs.close();	rs = null;//23feb19[to close the cursor and pstmt while returning string]
						pstmt.close();pstmt = null;
						errString = itmDBAccessLocal.getErrorString("","VTCOMMERR","","",conn);//removed single quotes from msg_no by nandkumar gadkari on 2/02/19
						return errString;
					}

					//Update sorddet
					sql="UPDATE sorddet SET sales_pers_comm_1 = ?,sales_pers_comm_2 = ?,sales_pers_comm_3 = ? WHERE sale_order = ? AND line_no = ?";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setDouble(1, spComm1);
					pstmt1.setDouble(2, spComm2);
					pstmt1.setDouble(3, spComm3);
					pstmt1.setString(4, saleOrder);
					pstmt1.setString(5, sordLineNo);
					updCnt= pstmt1.executeUpdate();
					pstmt1.close();
					pstmt1 = null;
					if(updCnt <= 0)
					{
						rs.close();	rs = null;//23feb19[to close the cursor and pstmt while returning string]
						pstmt.close();pstmt = null;
						errString = itmDBAccessLocal.getErrorString("","VTNCONFT","","",conn);//removed single quotes from msg_no by nandkumar gadkari on 2/02/19
						return errString;
					}
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}

			//update sorder..
			updCnt=0;
			sql="UPDATE sorder SET	sales_pers_comm_1 = ?, sales_pers_comm_2 = ?, sales_pers_comm_3 = ? WHERE sale_order = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setDouble(1, totalCommSp1);
			pstmt.setDouble(2, totalCommSp2);
			pstmt.setDouble(3, totalCommSp3);
			pstmt.setString(4, saleOrder);
			updCnt= pstmt.executeUpdate();
			System.out.println(">>>>Update sorder totalsp1Comm:"+updCnt);
			pstmt.close();
			pstmt = null;

			if(updCnt > 0)
			{
				//update sorder...
				updCnt=0;
				commAmtOc=totalCommAmt / exchRate;
				sql="UPDATE sorder SET	comm_amt__oc = ? WHERE sale_order = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setDouble(1, commAmtOc);
				pstmt.setString(2, saleOrder);
				updCnt= pstmt.executeUpdate();
				System.out.println(">>>>Update sorder comm_amt__oc:"+updCnt);
				pstmt.close();
				pstmt = null;
			}

			if(updCnt > 0)
			{
				//update sorder set updCnt
				updCnt=0;
				sql="UPDATE sorder SET confirmed = 'Y',conf_date = ?,comm_amt = ?,emp_code__aprv = ? WHERE sale_order = ?";
				pstmt = conn.prepareStatement(sql); 
				pstmt.setTimestamp(1, sysDate);
				pstmt.setDouble(2, totalCommAmt);
				pstmt.setString(3, loginEmpCode);
				pstmt.setString(4, saleOrder);
				updCnt= pstmt.executeUpdate();
				System.out.println(">>>>Update sorder confirmed status:"+updCnt);
				pstmt.close();
				pstmt = null;
			}
			if(updCnt > 0)
			{

				///// added by arun pal 12-OCT-2017
				System.out.println("channel  Partner configuration ");
				errString=confirmSaleOrder(saleOrder,xtraParams,conn); 
				System.out.println("confirmSaleOrder error code >>>"+errString);
				System.out.println(" @@@@@@@@@@@@@@@@@@@---------channel  Partner configuration ");
				//if(errString ==null || errString.trim().length() == 0)
				{				
					System.out.println("confirmSaleOrder error code >>>"+errString);
					if(errString ==null || errString.trim().length() == 0)
					{
						sql ="select channel_partner, dis_link from  site_customer where cust_code = ? and   site_code = ?";
						pstmt=conn.prepareStatement(sql);
						System.out.println("@@@@@@ sql "+sql );

						pstmt.setString(1, custCode);
						pstmt.setString(2, siteCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							channelPartner=rs.getString("channel_partner");
							disLink=rs.getString("dis_link");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						System.out.println("@@@@@@ channel_partner"+channelPartner);
						System.out.println("@@@@@@@ dis_link"+disLink);

						if(channelPartner== null || channelPartner.trim().length()==0)
						{
							sql ="select channel_partner, dis_link from  customer where cust_code = ? ";
							System.out.println("@@@@@@ sql "+sql );

							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1, custCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								channelPartner=rs.getString("channel_partner");
								disLink=rs.getString("dis_link");
								System.out.println("@@@@@@ channel partner "+channelPartner );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}

						System.out.println("@@@@@@ channel partner "+channelPartner );
						///added arun
						if("Y".equalsIgnoreCase(channelPartner) )
						{

							System.out.println("@@@@@@@ channel partner in "+channelPartner);

							String pItemCode="",plineNo="",pItemFlg="",pUnit="",pUnitStd="",pItemCodeOrd="",pNature="",plineNoContr="",pDspDate="",acctCodeCr=null,acctCodeDr=null,cctrCodeCr=null;
							String ptaxEnv="" , pStatus="",locCode="",ppackCode="",pspecRef="",cctrCodeDr=null,ptaxClass="",ptaxChap="";
							double pQuantityStduom=0,pConvQtyStduom=0,pRate=0,taxAmt=0,ptotAmt=0,pDiscount=0,pnoArt=0,prateClg=0,prateStd=0,ptaxAmt=0;
							String custItemRef="";
							Timestamp pDspDate1=null, pstatusDate=null,reqDate=null,dlvDate=null;
							int lineNoSo=0;
							String lineNoPo="",purcOrder="";
							//Added By Pavan R on 29/NOV/17 Start
							String itemCode = "";
							String pordMaxLineNo="";
							int poLineNo = 0;
							//End
							sql ="select cust_pord from sorder where sale_order = ? ";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1, saleOrder);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								purcOrder=rs.getString("cust_pord");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							
							System.out.println("@@@@ cust purchase order "+purcOrder);

							if(purcOrder !=null && purcOrder.trim().length() > 0)
							{
								sql="select line_no, item_code FROM sorddet WHERE sale_order =? order by line_no";
								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1,saleOrder );	
								rs1 = pstmt1.executeQuery();
								while (rs1.next())
								{
									lineNoSo=rs1.getInt(1);
									itemCode=rs1.getString(2);
									System.out.println("itemCode=["+itemCode+"]");
									System.out.println("@@@@ lineNoSo"+lineNoSo);

									sql="select line_no,SITE_CODE,LOC_CODE,REQ_DATE ,DLV_DATE,STATUS,ACCT_CODE__DR,ACCT_CODE__CR,CCTR_CODE__CR,CCTR_CODE__DR from  porddet where purc_order=? and line_no=? ";
									//sql1="select line_no from  porddet where purc_order=? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1,purcOrder );	
									pstmt.setInt(2,lineNoSo );	
									rs = pstmt.executeQuery();
									if(rs.next())
									{  
										System.out.println("sql"+sql);
										lineNoPo=rs.getString(1);
										siteCode=rs.getString(2);
										//taxAmt=rs.getDouble(3);
										//totAmt=rs.getDouble(3);
										//taxEnv=rs.getString(3);
										locCode=rs.getString(3);
										reqDate=rs.getTimestamp(4);
										dlvDate=rs.getTimestamp(5);
										pStatus=rs.getString(6);
										acctCodeDr=rs.getString(7);
										acctCodeCr=rs.getString(8);
										cctrCodeCr=rs.getString(9);
										cctrCodeDr=rs.getString(10);
										System.out.println("@@@@@@@@ lineNoPo"+lineNoPo);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									//System.out.println("lineNoPo.trim().length()"+lineNoPo.trim().length());
									if(lineNoPo!=null && lineNoPo.trim().length()>0)
									{
										System.out.println("Not null");
										lineNoPo="";
										//Added By Pavan R on 30/NOV/17 Start
										
										sql="select line_no,SITE_CODE,LOC_CODE,REQ_DATE ,DLV_DATE,STATUS,ACCT_CODE__DR,ACCT_CODE__CR,CCTR_CODE__CR,CCTR_CODE__DR from  porddet where purc_order=? and item_code=? ";
										//sql1="select line_no from  porddet where purc_order=? ";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1,purcOrder );	
										pstmt.setString(2, itemCode );	
										rs = pstmt.executeQuery();
										if(rs.next())
										{  
											System.out.println("sql"+sql);
											lineNoPo=rs.getString(1);
											siteCode=rs.getString(2);
											//taxAmt=rs.getDouble(3);
											//totAmt=rs.getDouble(3);
											//taxEnv=rs.getString(3);
											locCode=rs.getString(3);
											reqDate=rs.getTimestamp(4);
											dlvDate=rs.getTimestamp(5);
											pStatus=rs.getString(6);
											acctCodeDr=rs.getString(7);
											acctCodeCr=rs.getString(8);
											cctrCodeCr=rs.getString(9);
											cctrCodeDr=rs.getString(10);


											System.out.println("@@@@@@@@ lineNoPo"+lineNoPo);
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										//Added By Pavan R on 30/NOV/17 End
										
										
									}
									if(lineNoPo==null || lineNoPo.trim().length()==0)
									{
										System.out.println("646:: Not null");
										lineNoPo="";
										
										sql="SELECT ACCT_CODE__CR,ACCT_CODE__DR,CCTR_CODE__CR,CCTR_CODE__DR FROM PORDDET WHERE PURC_ORDER = ? ";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, purcOrder);
										rs = pstmt.executeQuery();
										while(rs.next()) 
										{
											acctCodeCr=rs.getString(1);
											acctCodeDr=rs.getString(2);
											cctrCodeCr=rs.getString(3);
											cctrCodeDr=rs.getString(4);
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										
										sql="SELECT MAX(LINE_NO) as maxPoDetLine FROM PORDDET WHERE PURC_ORDER = ? ";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, purcOrder);
										rs = pstmt.executeQuery();
										while(rs.next())         
										{
											pordMaxLineNo =rs.getString(1).trim();
										}
										System.out.println("pordMaxLineNo::["+pordMaxLineNo+"]");
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										
										System.out.println("acctandcctrCR:"+acctCodeCr+":"+cctrCodeCr);
										
										sql="SELECT sale_order, line_no, site_code, item_code, item_code__ord, item_flg, " +
												"quantity, unit, line_no__contr, unit__std, quantity__stduom, dsp_date, rate, " +
												"min_shelf_life, max_shelf_life, cust_item__ref, nature, conv__qty_stduom,NET_AMT," +
												"STATUS_DATE,DISCOUNT,NO_ART,PACK_CODE,RATE__CLG,SPEC_REF,RATE__STD,TAX_AMT,TAX_CLASS ,TAX_CHAP,TAX_ENV FROM" +
												" sorddet WHERE sale_order = ? and line_no=?";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, saleOrder);
										pstmt.setInt(2, lineNoSo);
										rs = pstmt.executeQuery();
										while(rs.next()) 
										{
											pItemCode = rs.getString("item_code");
											plineNo= rs.getString("line_no");
											//siteCode= rs.getString("site_code");
											pItemFlg = rs.getString("item_flg");
											pUnit = rs.getString("unit");
											pUnitStd = rs.getString("unit__std");
											pItemCodeOrd = rs.getString("item_code__ord");
											minShelfLife= rs.getDouble("min_shelf_life"); 
											maxShelfLife= rs.getDouble("max_shelf_life");
											custItemRef = rs.getString("cust_item__ref");
											pNature= rs.getString("nature");
											plineNoContr= rs.getString("line_no__contr");
											qtyOrd= rs.getDouble("quantity");
											pQuantityStduom= rs.getDouble("quantity__stduom");
											pConvQtyStduom= rs.getDouble("conv__qty_stduom");
											pRate= rs.getDouble("rate");
											pDspDate1= rs.getTimestamp("dsp_date");
											ptotAmt= rs.getDouble("NET_AMT");
											//pStatus= rs.getString("STATUS");
											pstatusDate= rs.getTimestamp("STATUS_DATE");
											pDiscount= rs.getDouble("DISCOUNT");
											pnoArt= rs.getDouble("NO_ART");
											ppackCode=rs.getString("PACK_CODE");
											prateClg= rs.getDouble("RATE__CLG");
											pspecRef=rs.getString("SPEC_REF");
											prateStd= rs.getDouble("RATE__STD");
											ptaxAmt= rs.getDouble("TAX_AMT");
											ptaxClass=rs.getString("TAX_CLASS");
											ptaxChap=rs.getString("TAX_CHAP");
											ptaxEnv=rs.getString("TAX_ENV");
										
											
											System.out.println("sql"+sql);



										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										/////	
									System.out.println("before:"+poLineNo+">["+pordMaxLineNo+"]");
									poLineNo = Integer.parseInt(pordMaxLineNo.trim());
									System.out.println("poLineNo---1---["+poLineNo+"]");
									poLineNo = poLineNo + 1;
									//System.out.println("poLineNo---2---"+poLineNo);
									pordMaxLineNo = pordMaxLineNo.valueOf(poLineNo);
									pordMaxLineNo = "    " + pordMaxLineNo;
									pordMaxLineNo = pordMaxLineNo.substring(pordMaxLineNo.length() - 3,	pordMaxLineNo.length());
									//System.out.println("pordMaxLineNo---*---"+pordMaxLineNo);
									System.out.println("after:"+poLineNo+">["+pordMaxLineNo+"]");
										sql="Insert into porddet(PURC_ORDER,LINE_NO,SITE_CODE,ITEM_CODE,QUANTITY,UNIT,"+
												"RATE,CONV__QTY_STDUOM,CONV__RTUOM_STDUOM,UNIT__STD,QUANTITY__STDUOM,ACCT_CODE__DR,ACCT_CODE__CR,"+
												"CCTR_CODE__CR,TOT_AMT,STATUS,DISCOUNT,LOC_CODE,REQ_DATE ,DLV_DATE,NO_ART," +
												"PACK_CODE,RATE__CLG,SPEC_REF,STD_RATE,CCTR_CODE__DR,TAX_CLASS ,TAX_CHAP,TAX_ENV,TAX_AMT)"+ 
												"values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
										// System.out.println("@@@@ inserted in"+sql);// where purc_order=? and line_no=? ";	
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, purcOrder);
										//pstmt.setInt(2, lineNoSo);
										pstmt.setString(2, pordMaxLineNo);
										pstmt.setString(3, siteCode);
										pstmt.setString(4,pItemCode );
										pstmt.setDouble(5, qtyOrd);
										pstmt.setString(6, pUnit);
										pstmt.setDouble(7, pRate);
										pstmt.setDouble(8,pConvQtyStduom );
										pstmt.setDouble(9, pConvQtyStduom);
										pstmt.setString(10, pUnitStd);
										pstmt.setDouble(11, pQuantityStduom);
										pstmt.setString(12, acctCodeDr);
										pstmt.setString(13, acctCodeCr);
										pstmt.setString(14, cctrCodeCr);
										//pstmt.setDouble(15, taxAmt);
										//pstmt.setDouble(16, totAmt);
										//pstmt.setString(16, ptaxEnv);
										pstmt.setDouble(15, ptotAmt);
										pstmt.setString(16, pStatus);
										pstmt.setDouble(17, pDiscount);
										pstmt.setString(18, locCode);
										pstmt.setTimestamp(19, reqDate);
										pstmt.setTimestamp(20, dlvDate);
										pstmt.setDouble(21, pnoArt);
										pstmt.setString(22, ppackCode);
										pstmt.setDouble(23, prateClg);
										pstmt.setString(24, pspecRef);
										pstmt.setDouble(25, prateStd);
										pstmt.setString(26, cctrCodeDr);
										pstmt.setString(27, ptaxClass);
										pstmt.setString(28, ptaxChap);
										pstmt.setString(29, ptaxEnv);
										pstmt.setDouble(30, ptaxAmt);
										//TAX_AMT


										rs = pstmt.executeQuery(); 
										//System.out.println("@@@ lineNoPo"+lineNoPo);
										//lineNoPo="";
									}//end if 
								}
								rs1.close();
								rs1 = null;
								pstmt1.close();
								pstmt1 = null;
							}

						}//end else if
						///ended by arun pal 12-OCT-2017

						/*errString=confirmSaleOrder(saleOrder,xtraParams,conn); */
						System.out.println("confirmSaleOrder error code >>>"+errString);

						sql = "select edi_option from transetup where tran_window = ?  ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, winName);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							ediOption = rs.getString("edi_option");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if("Y".equalsIgnoreCase(channelPartner) && "E".equalsIgnoreCase(disLink))
						{
							if ("2".equals(ediOption)) 
							{
								CreateRCPXML createRCPXML = new CreateRCPXML(winName,"sale_order");
								dataStr = createRCPXML.getTranXML(saleOrder, conn);
								System.out.println("dataStr =[ " + dataStr + "]");
								Document ediDataDom = genericUtility.parseString(dataStr);

								E12CreateBatchLoadEjb e12CreateBatchLoad = new E12CreateBatchLoadEjb();
								retString = e12CreateBatchLoad.createBatchLoad(ediDataDom,winName, "2", xtraParams, conn);
								createRCPXML = null;
								e12CreateBatchLoad = null;

								if (retString != null && "SUCCESS".equalsIgnoreCase(retString)) 
								{
									System.out.println("retString from edi 2 batchload = [" + retString + "]");
								}
							}

							else 
							{
								CreateRCPXML createRCPXML = new CreateRCPXML(winName,"sale_order");
								dataStr = createRCPXML.getTranXML(saleOrder, conn);
								System.out.println("dataStr =[ " + dataStr + "]");
								Document ediDataDom = genericUtility.parseString(dataStr);

								E12CreateBatchLoadEjb e12CreateBatchLoad = new E12CreateBatchLoadEjb();
								retString = e12CreateBatchLoad.createBatchLoad(ediDataDom,winName, ediOption, xtraParams, conn);
								createRCPXML = null;
								e12CreateBatchLoad = null;

								if (retString != null && "SUCCESS".equalsIgnoreCase(retString)) 
								{
									System.out.println("retString from batchload = ["+ retString + "]");
								}
							}
						}
					}
					else
					{
						return errString;
					}
				}
				//Commented by rupali on 11/05/2021 to original message on front end as required by user
				/* 
				else
				{
					errString = itmDBAccessLocal.getErrorString("","VTNCONFT","","",conn);//removed single quotes from msg_no by nandkumar gadkari on 2/02/19
					return errString;
				}
				*/
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
	private String confirmSaleOrder(String saleOrder,String xtraParams, Connection conn) throws ITMException 
	{
		String sql = "",errorCode = "";
		PreparedStatement pstmt = null,pstmt1=null;
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		String errString = null;
		ResultSet rs = null,rs1 = null;
		int cnt=0,updCnt=0;
		String consumeFc="",ordType="",custCode="",itemCode="",itemSer="",priceListDisc="",disVarValue="",lineNoContr="",contractNo="",quotNo="",itemCodeOrd="";
		String lineNo="",siteCode="",itemFlg="",unit="",unitStd="",custItemRef="",lineType="";
		String nature="",explev = "1.",expLev="", schAttr="";
		double ordAmount=0.0,minShelfLife=0.0,minShelfPerc=0.0,maxLife=0.0,shelfLife=0.0,tempLife=0.0,maxShelfLife=0.0,qtyOrd=0.0;
		double quantityStduom=0.0,rate=0.0,convQtyStduom=0.0;
		Timestamp dspDate=null;
		String loginEmpCode="",projCode="";
		String taxChap=null,taxChapFr=null,taxClass=null,siteCodeCr=null;
		Timestamp sysDate = null,dueDate=null,orderDate=null;
		DistCommon distCommom = new DistCommon();
		String schemeCode = "";
		try
		{
			//Get consume_fc from Sale Order header.
			sql="SELECT consume_fc, cust_code, item_ser, price_list__disc, proj_code,due_date, " +
					" order_date, order_type ,site_code__ship FROM sorder WHERE sale_order = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, saleOrder);
			rs = pstmt.executeQuery();
			if (rs.next()) 
			{
				consumeFc=rs.getString("consume_fc");
				custCode=rs.getString("cust_code");
				itemSer=rs.getString("item_ser");
				priceListDisc=rs.getString("price_list__disc");
				//ordAmount=rs.getDouble("ord_amt");
				ordType=rs.getString("order_type");
				projCode=rs.getString("proj_code");
				dueDate=rs.getTimestamp("due_date");
				orderDate=rs.getTimestamp("order_date");
				siteCodeCr=rs.getString("site_code__ship");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			String lsDisPobOrdtypeList = disCommon.getDisparams("999999", "POB_ORD_TYPE", conn);
			System.out.println("===lsDisPobOrdtypeList[" + lsDisPobOrdtypeList + "]");

			boolean lbOrdFlag = false;
			System.out.println("===lsOrderType[" + lsDisPobOrdtypeList + "]");
			System.out.println("===lsDisPobOrdtypeList.length[" + lsDisPobOrdtypeList.length() + "]");
			if (lsDisPobOrdtypeList != null && lsDisPobOrdtypeList.trim().length() > 0) 
			{
				String lsDisPobOrdtypeListArr[] = lsDisPobOrdtypeList.split(",");
				ArrayList<String> disPobOrdtypeList = new ArrayList<String>(Arrays.asList(lsDisPobOrdtypeListArr));
				System.out.println("disPobOrdtypeList[" + disPobOrdtypeList + "]");
				for( String pobOrdType : disPobOrdtypeList )
				{
					if(checkNull(pobOrdType).equalsIgnoreCase(ordType)) 
					{
						lbOrdFlag = true;
					}
				}
			}

			// get from sorddet // Nandkumar Gadkari --Start--- column added sch_attr --------- and set it into sorditem table------12-01-18---
			sql="SELECT sale_order, line_no, site_code, item_code, item_code__ord, item_flg, " +
					"quantity, unit, line_no__contr, unit__std, quantity__stduom, dsp_date, rate, " +
					"min_shelf_life, max_shelf_life, cust_item__ref, nature, conv__qty_stduom,tax_chap , sch_attr, scheme_code FROM" +
					" sorddet WHERE sale_order = ? ORDER BY line_no";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, saleOrder);
			rs = pstmt.executeQuery();
			while(rs.next()) 
			{
				itemCode= rs.getString("item_code");
				lineNo= rs.getString("line_no");
				siteCode= rs.getString("site_code");
				itemFlg= rs.getString("item_flg");
				unit= rs.getString("unit");
				unitStd= rs.getString("unit__std");
				itemCodeOrd= rs.getString("item_code__ord");
				minShelfLife= rs.getDouble("min_shelf_life"); 
				maxShelfLife= rs.getDouble("max_shelf_life");
				custItemRef= rs.getString("cust_item__ref");
				nature= rs.getString("nature");
				lineNoContr= rs.getString("line_no__contr");
				qtyOrd= rs.getDouble("quantity");
				quantityStduom= rs.getDouble("quantity__stduom");
				convQtyStduom= rs.getDouble("conv__qty_stduom");
				rate= rs.getDouble("rate");
				dspDate= rs.getTimestamp("dsp_date");
				taxChap= rs.getString("tax_chap");
				schAttr= rs.getString("sch_attr");
				schemeCode = rs.getString("scheme_code");
				lineType = itemFlg;
				
				//added by rupali on 04/05/2021 to validate tax chapter [start]
				sql="SELECT * FROM ITEM WHERE ITEM_CODE = ? AND TAX_CHAP = ?";
				pstmt1= conn.prepareStatement(sql);
				pstmt1.setString(1, itemCode);
				pstmt1.setString(2, taxChap);
				rs1 = pstmt1.executeQuery();
				if (!rs1.next()) 
				{
					System.out.println("Tax chapter is not present in item master");
					errString = itmDBAccessLocal.getErrorString("","INVTAXCHAP","","",conn);
					return errString;
				}
				rs1.close();
				rs1= null;
				pstmt1.close();
				pstmt1= null;
				//added by rupali on 04/05/2021 to validate tax chapter [end]
				
				if(lbOrdFlag && schemeCode != null && schemeCode.trim().length() > 0)
				{
					lineType = "B";
				}

				if(minShelfLife==0)
				{
					//get min_shelf_life from sordertype
					sql="SELECT min_shelf_life FROM sordertype WHERE order_type = ?";
					pstmt1= conn.prepareStatement(sql);
					pstmt1.setString(1, ordType);
					rs1 = pstmt1.executeQuery();
					if (rs1.next()) 
					{
						minShelfLife = rs1.getDouble("min_shelf_life");
					}
					rs1.close();
					rs1= null;
					pstmt1.close();
					pstmt1= null;
				}
				if(minShelfLife==0)
				{
					//get min_shelf_life from customeritem
					minShelfLife=0;
					sql="SELECT min_shelf_life FROM customeritem WHERE cust_code = ? AND item_code = ?";
					pstmt1=conn.prepareStatement(sql);
					pstmt1.setString(1, custCode);
					pstmt1.setString(2, itemCode);
					rs1 = pstmt1.executeQuery();
					if (rs1.next()) 
					{
						minShelfLife = rs1.getDouble("min_shelf_life");
					}
					rs1.close();
					rs1= null;
					pstmt1.close();
					pstmt1= null;

					if(minShelfLife==0)
					{
						//get min_shelf_perc from customer series...
						sql="SELECT (CASE WHEN min_shelf_perc IS NULL THEN 0 ELSE min_shelf_perc END) AS min_shelf_perc " +
								" FROM customer_series WHERE cust_code = ? AND item_ser = ?";
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1, custCode);
						pstmt1.setString(2, itemSer);
						rs1 = pstmt1.executeQuery();
						if (rs1.next()) 
						{
							minShelfPerc = rs1.getDouble("min_shelf_perc"); 
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;

						if(minShelfPerc==0)
						{
							minShelfLife = 0;
							maxLife = 0;
						}
						else
						{
							//get shelf_life from item...
							sql="SELECT (CASE WHEN shelf_life IS NULL THEN 0 ELSE shelf_life END) AS shelf_life " +
									" FROM item WHERE item_code = ?";
							pstmt1 = conn.prepareStatement(sql);
							pstmt1.setString(1, itemCode);
							rs1 = pstmt1.executeQuery();
							if (rs1.next()) 
							{
								shelfLife = rs1.getDouble("shelf_life"); 
							}
							rs1.close();
							rs1 = null;
							pstmt1.close();
							pstmt1 = null;

							if(shelfLife > 0)
							{
								//minShelfLife = round((minShelfPerc/100) * shelfLife,0);
								minShelfLife = Math.round((minShelfPerc/100) * shelfLife);
								maxLife = shelfLife;
							}
							else
							{
								minShelfLife = 0;
								maxLife = 0;
							}
						}
					}
					// Pick up min_shelf_perc from customer series .
					if(minShelfLife==0)
					{
						sql="SELECT min_shelf_life FROM customer WHERE cust_code = ?";
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1, custCode);
						rs1 = pstmt1.executeQuery();
						if (rs1.next()) 
						{
							minShelfLife = rs1.getDouble("min_shelf_life");
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;

					}
					if(minShelfLife==0)
					{
						//Changed by Manish on 09/09/16 for removing extra semicolon[start]
						//sql="SELECT min_shelf_life FROM item WHERE item_code = ?;";
						sql="SELECT min_shelf_life FROM item WHERE item_code = ?";
						//Changed by Manish on 09/09/16 for removing extra semicolon[end]
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1, itemCode);
						rs1 = pstmt1.executeQuery();
						if (rs1.next()) 
						{
							minShelfLife = rs1.getDouble("min_shelf_life");
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;

					}
					if(priceListDisc!=null && priceListDisc.trim().length() > 0) 
					{
						sql="SELECT order_type FROM sorder WHERE sale_order = ?";
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1, saleOrder);
						rs1 = pstmt1.executeQuery();
						if (rs1.next()) 
						{
							ordType = rs1.getString("order_type");
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
						if("NE".equals(ordType.trim()))
						{
							sql="SELECT (CASE WHEN no_sales_month IS NULL THEN 0 ELSE no_sales_month END) AS no_sales_month " +
									" FROM item WHERE item_code = ?";
							pstmt1 = conn.prepareStatement(sql);
							pstmt1.setString(1, itemCode);
							rs1 = pstmt1.executeQuery();
							if (rs1.next()) 
							{
								maxLife = rs1.getDouble("no_sales_month");
							}
							rs1.close();
							rs1 = null;
							pstmt1.close();
							pstmt1 = null;
							if(maxLife==0)
							{
								//call getDisparams() .....
								disVarValue = checkNull(distCommom.getDisparams("999999","NEAR_EXP_SHELF_LIFE", conn));
								System.out.println(">>>>>disVarValue:"+disVarValue);
								if("NULLFOUND".equalsIgnoreCase(disVarValue) || disVarValue== null || disVarValue.trim().length()==0)
								{
									disVarValue="0";
								}
								maxLife=Double.parseDouble(disVarValue);
							}
							// Interchange values of min shelf life with max shelf life
							tempLife=maxLife;
							maxLife=minShelfLife;
							minShelfLife=tempLife;
						}
						else
						{
							maxLife=0;
						}
					}

				}
				if(maxShelfLife > 0)
				{
					maxLife = maxShelfLife;
				}
				
				//Changed by Kunal on 4/01/18 for adding tax chapter in sorditem[Start]
				
				sql="insert into sorditem "
						+ " (sale_order, line_no, site_code, item_code__ord, item_code__ref," 
						+ " item_code,item_flag,line_type,unit__ord,unit__ref,unit,qty_ord,qty_ref,"
						+ "quantity,exp_lev, min_shelf_life,max_shelf_life,consume_fc,due_date," +
						"cust_item__ref,order_date,order_type,dsp_date,rate,status,nature," +
						"conv__qty_stdqty,cust_code__dlv, " 
						//Changed by Manish on 12/09/16 for allocated quantity and despatch quantity[start]
						+" qty_alloc, qty_desp ,tax_chap ,sch_attr )"
						//Changed by Manish on 12/09/16 for allocated quantity and despatch quantity[end]
						+ " values "
						+ "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				pstmt1=conn.prepareStatement(sql);
				pstmt1.setString(1,saleOrder);
				pstmt1.setString(2,lineNo);
				pstmt1.setString(3,siteCode);
				// CONDITON ADDED BY NANDKUMAR GADKARI ON 03/06/19
				if("P".equalsIgnoreCase(nature))
				{
					pstmt1.setString(4,itemCodeOrd);
					pstmt1.setString(5,itemCodeOrd);
					pstmt1.setString(6,itemCodeOrd);
				}
				else {
					pstmt1.setString(4,itemCode);
					pstmt1.setString(5,itemCode);
					pstmt1.setString(6,itemCode);
				}
				
				
				pstmt1.setString(7,itemFlg);
				//pstmt1.setString(8,itemFlg);
				pstmt1.setString(8,lineType);
				pstmt1.setString(9,unit);
				pstmt1.setString(10,unit);
				pstmt1.setString(11,unit);
				pstmt1.setDouble(12,qtyOrd);
				pstmt1.setDouble(13,qtyOrd);
				pstmt1.setDouble(14,qtyOrd);
				pstmt1.setString(15,explev);
				pstmt1.setDouble(16,minShelfLife);
				pstmt1.setDouble(17,maxShelfLife);
				pstmt1.setString(18,consumeFc);
				pstmt1.setTimestamp(19,dueDate);
				pstmt1.setString(20,custItemRef);
				pstmt1.setTimestamp(21,orderDate);
				pstmt1.setString(22,ordType);
				pstmt1.setTimestamp(23,dspDate);
				pstmt1.setDouble(24,rate);
				pstmt1.setString(25,"P");
				pstmt1.setString(26,nature);
				pstmt1.setDouble(27,convQtyStduom);
				pstmt1.setString(28,custCode);
				//Changed by Manish on 12/09/16 for allocated quantity and despatch quantity[start]
				pstmt1.setString(29,"0");
				pstmt1.setString(30,"0");
				//Changed by Manish on 12/09/16 for allocated quantity and despatch quantity[end]
				pstmt1.setString(31,taxChap);
				pstmt1.setString(32,schAttr);	
				//Nandkumar Gadkari --end--- column added sch_attr --------- and set it into sorditem table------12-01-18---
				pstmt1.executeUpdate();
				pstmt1.close();
				pstmt1=null;


				if(contractNo!=null && lineNoContr!=null && contractNo.trim().length() > 0 && lineNoContr.trim().length() > 0)
				{
					sql="UPDATE scontractdet SET rel_qty= rel_qty + ?,rel_date = ? WHERE contract_no = ? AND line_no = ?";
					pstmt1 = conn.prepareStatement(sql); 
					pstmt1.setDouble(1, qtyOrd);
					pstmt1.setTimestamp(2, sysDate);
					pstmt1.setString(3, contractNo);
					pstmt1.setString(4, lineNoContr);
					updCnt= pstmt1.executeUpdate();
					System.out.println(">>>>Update scontractdet:"+updCnt);
					pstmt1.close();
					pstmt1 = null;
					if (updCnt < 0) 
					{	
						errString = itmDBAccessLocal.getErrorString("","VTNCONFT","","",conn);
						return errString;
					}
				}
				//confirmation rel_qty,bal_qty and rel_date update in sales_quotdet
				sql="SELECT quot_no FROM sorder WHERE sale_order = ?";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, saleOrder);
				rs1 = pstmt1.executeQuery();
				if (rs1.next()) 
				{
					quotNo = rs1.getString("quot_no");
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
				if(quotNo!=null && quotNo.trim().length()!= 0)
				{
					updCnt=0;
					sql="UPDATE sales_quotdet SET rel_qty =(CASE WHEN rel_qty IS NULL THEN 0 ELSE rel_qty END)  + ?,rel_date = ?," +
							" bal_qty =(CASE WHEN bal_qty IS NULL THEN 0 ELSE bal_qty END) - ? WHERE quot_no = ?" +
							" AND item_code = ?";
					pstmt1 = conn.prepareStatement(sql); 
					pstmt1.setDouble(1, qtyOrd);
					pstmt1.setTimestamp(2, sysDate);
					pstmt1.setDouble(3, qtyOrd);
					pstmt1.setString(4, quotNo);
					pstmt1.setString(5, itemCodeOrd);
					updCnt= pstmt1.executeUpdate();
					System.out.println(">>>>Update scontractdet:"+updCnt);
					pstmt1.close();
					pstmt1 = null;
					if (updCnt < 0) 
					{	
						rs.close();//23feb19[to close the cursor and pstmt while returning string]
						rs = null;
						pstmt.close();
						pstmt = null;
						errString = itmDBAccessLocal.getErrorString("","VTNCONFT","","",conn);
						return errString;
					}
				}
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			int result = 0;
			sql="SELECT line_type,item_code,exp_lev,line_no FROM sorditem WHERE sale_order = ?";
			pstmt1 = conn.prepareStatement(sql);
			pstmt1.setString(1, saleOrder);
			rs1 = pstmt1.executeQuery();
			while (rs1.next()) 
			{
				lineType = checkNull(rs1.getString("line_type")).trim();
				itemCode = checkNull(rs1.getString("item_code")).trim();
				expLev = checkNull(rs1.getString("exp_lev")).trim();
				lineNo = rs1.getString("line_no");
				//added by rupali on 20/04/2021 to check scheme code and call explode pob details if scheme is present [start]
				String orderType = "";
				sql = "select so.order_type, det.scheme_code from sorder so, sorddet det where so.sale_order = det.sale_order "+
						" and so.sale_order = ? and det.item_code = ?"; 
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1, saleOrder);
				pstmt.setString(2, itemCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					orderType = checkNull(rs.getString("order_type")).trim();
					schemeCode = checkNull(rs.getString("scheme_code")).trim();
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				System.out.println("orderType::::["+orderType+"] schemeCode:::["+schemeCode+"]");
				if(lbOrdFlag && schemeCode.length() > 0)
				{
					result = explodePobDs(saleOrder,itemCode,expLev,lineNo,lineType,conn);
					if(result !=1)
					{
						rs1.close();//23feb19[to close the cursor and pstmt while returning string]
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
						//added by rupali on 05/05/2021 to validate tax chapter [start]
						if(result == -1)
						{
							errString = itmDBAccessLocal.getErrorString("","INVTAXCHAP","","",conn);
							return errString;
						}
						//added by rupali on 05/05/2021 to validate tax chapter [end]
						else
						{
							errString = itmDBAccessLocal.getErrorString("","DS000","","",conn);
							return errString;
						}
					}
				}
				//added by rupali on 20/04/2021 to check scheme code and call explode pob details if scheme is present [end]
				else if(!"I".equalsIgnoreCase(lineType))
				{
					result = explodeBomDs(saleOrder,itemCode,expLev,lineNo,lineType,conn);
					if(result !=1)
					{
						rs1.close();//23feb19[to close the cursor and pstmt while returning string]
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
						//added by rupali on 05/05/2021 to validate tax chapter [start]
						if(result == -1)
						{
							errString = itmDBAccessLocal.getErrorString("","INVTAXCHAP","","",conn);
							return errString;
						}
						//added by rupali on 05/05/2021 to validate tax chapter [end]
						else
						{
							errString = itmDBAccessLocal.getErrorString("","DS000","","",conn);
							return errString;
						}
					}
				}
			}
			rs1.close();
			rs1 = null;
			pstmt1.close();
			pstmt1 = null;

			// Calling of new function which uses sales order credit terms data.
			cnt=0;
			sql ="SELECT count(*) AS cnt FROM sord_cr_terms WHERE sale_order = ? AND rel_agnst ='02'";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, saleOrder);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				cnt=rs.getInt(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if(cnt > 0)
			{
				//errString=InvoicememoHdr("S-ORD",projCode,dueDate,saleOrder,saleOrder,xtraParams,conn);//no need as per manoharan sir  suggession
			}
			if(quotNo!=null && quotNo.trim().length()!= 0)
			{
				updCnt=0;
				sql="UPDATE sales_quot SET status = 'O', status_date = ? WHERE quot_no = ?";
				pstmt = conn.prepareStatement(sql); 
				pstmt.setTimestamp(1, sysDate);
				pstmt.setString(2, quotNo);
				updCnt= pstmt.executeUpdate();
				System.out.println(">>>>Update sales_quot:"+updCnt);
				pstmt.close();
				pstmt = null;
				if (updCnt < 0) 
				{	
					errString = itmDBAccessLocal.getErrorString("","VTNCONFT","","",conn);
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
	private double checkDouble(Double double1)
	{
		if (double1 == null) 
		{
			double1 = 0.0;
		}
		return double1;

	}
	public HashMap calcCommission(String saleOrder,String lineNo ,Connection conn) throws ITMException
	{
		String sql="" ,currCode = "",baseCurrency = "",errorCode="",errString="";
		String commPercOn="",currCodeComm="",commPercOn1="",currCodeComm1="",commPercOn2="",currCodeComm2="",dlvTerm="";
		String commHdr = "N",commPercOnDet1="",commPercOnDet2="",commPercOnDet3="",itemCode="",siteCode="",finEntity="",insReqd="",frtReqd="";
		String custCode="",itemSer="",salesPers="",salesPers1="",salesPers2="",priceListDate="";
		double exchRate=0,commPerc=0,exchRateComm=0,commPerc1=0,exchRateComm1=0,commPerc2=0,exchRateComm2=0,commPercDet1=0,commPercDet2=0,commPercDet3=0;
		double taxAmt=0,netAmt=0,qtyStduom=0,rateStduom=0,frtAmt=0,exchFrtRate=0,insAmt=0,exchInsRate=0,fobAmt=0,netComm=0,ordPrice=0;
		double commPerUnit=0,commBl1=0,commBl2=0,commBl3=0,commPerUnit1=0.0,commPerUnit2=0.0,commPerUnit3=0.0,qtyComm=0.0,qtyComm1=0.0,qtyComm2=0.0,qtyComm3=0.0;
		double sp1Comm=0.0,sp2Comm=0.0,sp3Comm=0.0,totalCommBl=0.0,commAmt=0.0,commAmt1=0.0,commAmt2=0.0,commAmt3=0.0;
		double baseAmtComm=0.0,baseAmtComm1=0.0,baseAmtComm2=0.0,baseAmtComm3=0.0,assessAmt=0.0,asesAmtComm=0.0,asesAmtComm1=0.0,asesAmtComm2=0.0,asesAmtComm3=0.0;
		double salesAmt=0.0,taxAmtComm=0.0,taxAmtComm1=0.0,taxAmtComm2=0.0,taxAmtComm3=0.0,fobComm=0.0,fobComm1=0.0,fobComm2=0.0,fobComm3=0.0;
		double fobQtyComm=0.0,fobQtyComm1=0.0,fobQtyComm2=0.0,fobQtyComm3=0.0,amtQtyComm=0.0,amtQtyComm1=0.0,amtQtyComm2=0.0,amtQtyComm3=0.0;
		double taxAmtHdr=0.0,taxAmtDet=0.0,netAmtHdr=0.0,netAmtDet=0.0;
		Date plDate=null;
		PreparedStatement pstmt = null ;
		ResultSet rs = null;
		HashMap commissionMap = null;
		HashMap commPercMap = null;
		HashMap commPercSalesMap=null;
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		try
		{
			commissionMap = new HashMap();
			sql="select site_code from sorder where sale_order = ?";		
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,saleOrder);	
			rs = pstmt.executeQuery();
			if(rs.next())
			{  
				siteCode =  rs.getString(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			sql="SELECT fin_entity FROM site WHERE site_code = ?";		
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,siteCode);	
			rs = pstmt.executeQuery();
			if(rs.next())
			{  
				finEntity =  rs.getString(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			sql="SELECT curr_code FROM finent WHERE fin_entity= ?";		
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,finEntity);	
			rs = pstmt.executeQuery();
			if(rs.next())
			{  
				baseCurrency =  rs.getString(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			//Get data from sale order...
			sql ="SELECT dlv_term,curr_code,exch_rate,exch_rate__frt,exch_rate__ins," +
					"ins_amt,frt_amt,comm_perc,comm_perc__on,curr_code__comm," +
					"exch_rate__comm,comm_perc_1,comm_perc_on_1,curr_code__comm_1," +
					"exch_rate__comm_1,comm_perc_2,comm_perc_on_2,curr_code__comm_2," +
					"exch_rate__comm_2,pl_date,cust_code,item_ser,sales_pers," +
					"sales_pers__1,sales_pers__2,tot_amt, tax_amt FROM sorder WHERE sale_order =?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,saleOrder);	
			rs = pstmt.executeQuery();
			if(rs.next())
			{  
				dlvTerm = rs.getString("dlv_term");
				currCode = rs.getString("curr_code");
				exchRate = rs.getDouble("exch_rate");
				exchFrtRate =rs.getDouble("exch_rate__frt");
				exchInsRate =rs.getDouble("exch_rate__ins");
				insAmt = rs.getDouble("ins_amt");
				frtAmt = rs.getDouble("frt_amt");

				commPerc = rs.getDouble("comm_perc");
				commPercOn =  rs.getString("comm_perc__on");
				currCodeComm =  rs.getString("curr_code__comm");
				exchRateComm = rs.getDouble("exch_rate__comm");

				commPerc1 = rs.getDouble("comm_perc_1");
				commPercOn1 =  rs.getString("comm_perc_on_1");
				currCodeComm1 =  rs.getString("curr_code__comm_1");
				exchRateComm1 = rs.getDouble("exch_rate__comm_1");

				commPerc2 = rs.getDouble("comm_perc_2");
				commPercOn2 =  rs.getString("comm_perc_on_2");
				commPercOn2 =  rs.getString("comm_perc_on_2");
				currCodeComm2 =  rs.getString("curr_code__comm_2");
				exchRateComm2 = rs.getDouble("exch_rate__comm_2");


				priceListDate = rs.getString("pl_date");
				custCode= rs.getString("cust_code");
				itemSer= rs.getString("item_ser");
				salesPers= rs.getString("sales_pers");
				salesPers1= rs.getString("sales_pers__1");
				salesPers2= rs.getString("sales_pers__2");
				taxAmtHdr         =rs.getDouble("tax_amt");
				netAmtHdr        =rs.getDouble("tot_amt");

			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			commHdr = "N";
			baseCurrency=currCode;
			if( (commPerc > 0) || (commPerc1 > 0) || (commPerc2 > 0))
			{
				commHdr = "Y";
			}
			if("N".equalsIgnoreCase(commHdr) && lineNo.trim().length() > 0)
			{

				sql="SELECT comm_perc_on_1, comm_perc_on_2, comm_perc_on_3, comm_perc_1, comm_perc_2, comm_perc_3, " +
						"tax_amt,net_amt FROM sorddet WHERE sale_order = ? AND line_no = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,saleOrder);
				pstmt.setString(2,lineNo.trim());	
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					commPercOnDet1 =rs.getString("comm_perc_on_1");
					commPercOnDet2 =rs.getString("comm_perc_on_2");
					commPercOnDet3 =rs.getString("comm_perc_on_3");
					commPercDet1   =rs.getDouble("comm_perc_1");
					commPercDet2   =rs.getDouble("comm_perc_2");
					commPercDet3   =rs.getDouble("comm_perc_3");
					taxAmtDet         =rs.getDouble("tax_amt");
					netAmtDet         =rs.getDouble("net_amt");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				if(commPercOnDet1 != null && commPercOnDet1.trim().length() > 0)
				{
					commPercOn = commPercOnDet1;
				}
				if(commPercOnDet2 != null && commPercOnDet2.trim().length() > 0)
				{
					commPercOn1 = commPercOnDet2;
				}
				if(commPercOnDet3 != null && commPercOnDet3.trim().length() > 0)
				{
					commPercOn2 = commPercOnDet3;
				}
				if(commPercDet1 > 0)
				{
					commPerc = commPercDet1;
				}
				if(commPercDet2 > 0)
				{
					commPerc1 = commPercDet2;
				}
				if(commPercDet3 > 0)
				{
					commPerc2 = commPercDet3;
				}
				System.out.println(">>>>>> in detail commPercOn:"+commPercOn+"   "+commPercOn1+"     "+commPercOn2);
				System.out.println(">>>>>> in detail commPerc:"+commPerc+"   "+commPerc1+"     "+commPerc2);
			}
			if("Y".equalsIgnoreCase(commHdr))
			{
				netAmt =  netAmtHdr;
				taxAmt =  taxAmtHdr;

				/*sql="SELECT tot_amt, tax_amt FROM sorder WHERE sale_order = ?";		
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,saleOrder);	
				rs = pstmt.executeQuery();
				if(rs.next())
				{  
					netAmt =  rs.getDouble(1);
					taxAmt =  rs.getDouble(2);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;*/
			}
			else
			{
				netAmt =  netAmtDet;
				taxAmt =  taxAmtDet;

				/*sql="SELECT net_amt, tax_amt FROM sorddet WHERE sale_order= ? AND line_no= ?";		
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, saleOrder);	
				pstmt.setString(2, lineNo);	
				rs = pstmt.executeQuery();
				if(rs.next())
				{  
					netAmt =  rs.getDouble(1);
					taxAmt =  rs.getDouble(2);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;*/
			}
			//
			sql="SELECT ins_reqd,frt_reqd FROM delivery_term WHERE dlv_term = ?";		
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, dlvTerm);	
			rs = pstmt.executeQuery();
			if(rs.next())
			{  
				insReqd = rs.getString("ins_reqd");
				frtReqd = rs.getString("frt_reqd");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			netAmt =netAmt * exchRate;
			taxAmt =taxAmt * exchRate;
			frtAmt =frtAmt * exchFrtRate;
			insAmt =insAmt * exchInsRate;

			if("Y".equalsIgnoreCase(insReqd) && "Y".equalsIgnoreCase(frtReqd)) //CIF
			{
				fobAmt = netAmt - frtAmt - insAmt;
			}
			else if("N".equalsIgnoreCase(insReqd) && "Y".equalsIgnoreCase(frtReqd))   //C&F
			{
				fobAmt = netAmt - frtAmt;
			}
			else if("Y".equalsIgnoreCase(insReqd) && "N".equalsIgnoreCase(frtReqd)) //CIP
			{
				fobAmt = netAmt -  insAmt;
			}
			else if("N".equalsIgnoreCase(insReqd) && "N".equalsIgnoreCase(frtReqd)) //FOB
			{
				fobAmt = netAmt;
			}
			fobAmt = fobAmt - taxAmt;


			if(lineNo.trim().length() > 0)
			{
				System.out.println(">>>>>>qty for detail");
				sql="SELECT quantity__stduom, rate__stduom, item_code FROM sorddet WHERE sale_order = ? AND line_no = ?";		
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,saleOrder);	
				pstmt.setString(2,lineNo);	
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					qtyStduom =  rs.getDouble(1);
					rateStduom =  rs.getDouble(2);
					itemCode = rs.getString(3);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if("B".equalsIgnoreCase(commPercOn) || "B".equalsIgnoreCase(commPercOn1) || "B".equalsIgnoreCase(commPercOn2))
				{
					netComm=0;
					ordPrice= rateStduom;
					//Calling function for picking base comm perc for business logic 
					commPercMap=commPercBase(custCode,itemCode,itemSer,ordPrice,priceListDate,conn);
					errString= checkNull((String) commPercMap.get("errorStr"));
					commPerUnit= checkDouble((Double)commPercMap.get("commPerc"));
					if(errString.trim().length() > 0)
					{
						commissionMap.put("errorStr", errString);
						return commissionMap;
						//goto exit_now	
					}
					if(commPerUnit == 0 && commPerc == 0 && commPerc1 == 0 && commPerc2 == 0)
					{
						errString = itmDBAccessLocal.getErrorString("","VTNOPERC","","",conn);
						netComm=0;
						commissionMap.put("errorStr", errString);
						return commissionMap;

					}
				}
				//Option: Business Logic 1 	
				if("B".equalsIgnoreCase(commPercOn) && commPerc==0)
				{
					//commision % not entered
					if(!baseCurrency.equalsIgnoreCase(currCodeComm))
					{
						commBl1= qtyStduom * commPerUnit * exchRateComm;
					}
					else
					{
						commBl1= qtyStduom * commPerUnit * exchRate;

					}
				}
				else if("B".equalsIgnoreCase(commPercOn) && commPerc > 0)
				{
					//commission % entered
					if(!baseCurrency.equalsIgnoreCase(currCodeComm))
					{
						commBl1= qtyStduom * commPerc * exchRateComm;
					}
					else
					{
						commBl1= qtyStduom * commPerc * exchRate;

					}

				}
				//Option: Business Logic 2
				if("B".equalsIgnoreCase(commPercOn1) && commPerc1==0)
				{
					//commision % not entered
					if(!baseCurrency.equalsIgnoreCase(currCodeComm1))
					{
						commBl2= qtyStduom * commPerUnit * exchRateComm1;
					}
					else
					{
						commBl2= qtyStduom * commPerUnit * exchRate;
					}
				}
				else if("B".equalsIgnoreCase(commPercOn1) && commPerc1 > 0)
				{
					//commission % entered
					if(!baseCurrency.equalsIgnoreCase(currCodeComm1))
					{
						commBl2= qtyStduom * commPerc1 * exchRateComm1;
					}
					else
					{
						commBl2= qtyStduom * commPerc1 * exchRate;
					}
				}
				//Option: Business Logic 3
				if("B".equalsIgnoreCase(commPercOn2) && commPerc2==0)
				{
					//commision % not entered
					if(!baseCurrency.equalsIgnoreCase(currCodeComm2))
					{
						commBl3= qtyStduom * commPerUnit * exchRateComm2;
					}
					else
					{
						commBl3= qtyStduom * commPerUnit * exchRate;
					}
				}
				else if("B".equalsIgnoreCase(commPercOn2) && commPerc2 > 0)
				{
					//commission % entered
					if(!baseCurrency.equalsIgnoreCase(currCodeComm2))
					{
						commBl3= qtyStduom * commPerc2 * exchRateComm2;
					}
					else
					{
						commBl3= qtyStduom * commPerc2 * exchRate;
					}
				}

				//Business Logic Sales Person 
				netComm=0;
				ordPrice= rateStduom;
				if("S".equalsIgnoreCase(commPercOn) || "S".equalsIgnoreCase(commPercOn1) || "S".equalsIgnoreCase(commPercOn2))
				{
					if("S".equalsIgnoreCase(commPercOn))
					{
						commPercSalesMap=commPercSalesPers(salesPers,ordPrice,priceListDate,itemCode,conn);
						errString= checkNull((String) commPercSalesMap.get("errorStr"));
						if(errString.trim().length() > 0)
						{
							commissionMap.put("errorStr", errString);
							return commissionMap;
							//goto exit_now	
						}
						commPerUnit1= checkDouble((Double)commPercSalesMap.get("commPercSales"));
					}
					if("S".equalsIgnoreCase(commPercOn1))
					{
						commPercSalesMap= commPercSalesPers(salesPers1,ordPrice,priceListDate,itemCode,conn);
						errString= checkNull((String) commPercSalesMap.get("errorStr"));
						if(errString.trim().length() > 0)
						{
							commissionMap.put("errorStr", errString);
							return commissionMap;
							//goto exit_now	
						}
						commPerUnit2= checkDouble((Double)commPercSalesMap.get("commPercSales"));
					}
					if("S".equalsIgnoreCase(commPercOn2))
					{
						commPercSalesMap= commPercSalesPers(salesPers2,ordPrice,priceListDate,itemCode,conn);
						errString= checkNull((String) commPercSalesMap.get("errorStr"));
						if(errString.trim().length() > 0)
						{
							commissionMap.put("errorStr", errString);
							return commissionMap;
							//goto exit_now	
						}
						commPerUnit3= checkDouble((Double)commPercSalesMap.get("commPercSales"));
					}
					if(commPerUnit1 == 0 && commPerUnit2 == 0 && commPerUnit3 == 0 && commPerc==0 && commPerc1==0 && commPerc2==0)
					{
						errString = itmDBAccessLocal.getErrorString("","VTNOPERC","","",conn);
						netComm=0;
						commissionMap.put("errorStr", errString);
						return commissionMap;
					}
				}
				//Option: 1
				if("S".equalsIgnoreCase(commPercOn) && commPerc==0)
				{
					if(!baseCurrency.equalsIgnoreCase(currCodeComm))
					{
						commBl1= qtyStduom * commPerUnit1 * exchRateComm;
					}
					else
					{
						commBl1= qtyStduom * commPerUnit1 * exchRate;
					}
				}
				else if("S".equalsIgnoreCase(commPercOn) && commPerc > 0)
				{
					if(!baseCurrency.equalsIgnoreCase(currCodeComm))
					{
						commBl1= qtyStduom * commPerc * exchRateComm;
					}
					else
					{
						commBl1= qtyStduom * commPerc * exchRate;
					}
				}
				//Option: 2
				if("S".equalsIgnoreCase(commPercOn1) && commPerc1==0)
				{
					if(!baseCurrency.equalsIgnoreCase(currCodeComm1))
					{
						commBl2= qtyStduom * commPerUnit2 * exchRateComm1;
					}
					else
					{
						commBl2= qtyStduom * commPerUnit2 * exchRate;
					}
				}
				else if("S".equalsIgnoreCase(commPercOn1) && commPerc1 > 0)
				{
					if(!baseCurrency.equalsIgnoreCase(currCodeComm1))
					{
						commBl2= qtyStduom * commPerc1 * exchRateComm1;
					}
					else
					{
						commBl2= qtyStduom * commPerc1 * exchRate;
					}
				}
				//Option: 3
				if("S".equalsIgnoreCase(commPercOn2) && commPerc2==0)
				{
					if(!baseCurrency.equalsIgnoreCase(currCodeComm2))
					{
						commBl3= qtyStduom * commPerUnit3 * exchRateComm2;
					}
					else
					{
						commBl3= qtyStduom * commPerUnit3 * exchRate;
					}
				}
				else if("S".equalsIgnoreCase(commPercOn2) && commPerc2 > 0)
				{
					//commission % entered
					if(!baseCurrency.equalsIgnoreCase(currCodeComm2))
					{
						commBl3= qtyStduom * commPerc2 * exchRateComm2;
					}
					else
					{
						commBl3= qtyStduom * commPerc2 * exchRate;
					}
				} 
				//end.. Option : Business Logic Sales Person

				if("Q".equalsIgnoreCase(commPercOn)) //commPercOn for Q
				{
					//comm calc in base currency
					if(!baseCurrency.equalsIgnoreCase(currCodeComm))
					{
						qtyComm1= qtyStduom * commPerc * exchRateComm;
					}
					else
					{
						qtyComm1= qtyStduom * commPerc * exchRate;
					}
				}
				if("Q".equalsIgnoreCase(commPercOn1)) //commPercOn1 for Q
				{
					//comm calc in base currency
					if(!baseCurrency.equalsIgnoreCase(currCodeComm1))
					{
						qtyComm2= qtyStduom * commPerc1 * exchRateComm1;
					}
					else
					{
						qtyComm2= qtyStduom * commPerc1 * exchRate;
					}
				}
				if("Q".equalsIgnoreCase(commPercOn2)) //commPercOn2 for Q
				{
					//comm calc in base currency
					if(!baseCurrency.equalsIgnoreCase(currCodeComm2))
					{
						qtyComm3= qtyStduom * commPerc2 * exchRateComm2;
					}
					else
					{
						qtyComm3= qtyStduom * commPerc2 * exchRate;
					}
				}
				qtyComm=qtyComm + qtyComm1 + qtyComm2 + qtyComm3 + commBl1 + commBl2 + commBl3;

				sp1Comm = sp1Comm + commBl1 + qtyComm1;
				sp2Comm = sp2Comm + commBl2 + qtyComm2;
				sp3Comm = sp3Comm + commBl3 + qtyComm3;

				qtyComm1=0;
				qtyComm2=0;
				qtyComm3=0;

				qtyStduom=0;
				commBl1=0;
				commBl2=0;
				commBl3=0;
			}

			if("B".equalsIgnoreCase(commPercOn) || "B".equalsIgnoreCase(commPercOn1) || "B".equalsIgnoreCase(commPercOn2) )
			{
				totalCommBl= qtyComm;
			}
			//To calculate commission on AMOUNT
			if("A".equalsIgnoreCase(commPercOn))
			{
				commAmt1= (netAmt *  commPerc) / 100;
			}
			if("A".equalsIgnoreCase(commPercOn1))
			{
				commAmt2= (netAmt *  commPerc1) / 100;
			}
			if("A".equalsIgnoreCase(commPercOn2))
			{
				commAmt3= (netAmt *  commPerc2) / 100;
			}
			commAmt= commAmt1 + commAmt2 + commAmt3;

			sp1Comm= sp1Comm + commAmt1;
			sp2Comm= sp2Comm + commAmt2;
			sp3Comm= sp3Comm + commAmt3;

			//if line no is not available 

			if(lineNo.trim().length() == 0)
			{
				System.out.println(">>>>>");
				sql="SELECT SUM(quantity__stduom) AS quantity__stduom FROM sorddet WHERE sale_order = ?";		
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,saleOrder);	
				rs = pstmt.executeQuery();
				if(rs.next())
				{  
					qtyStduom =  rs.getDouble(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				//To calculate commission on Quantity...

				if("Q".equalsIgnoreCase(commPercOn))
				{
					if(!baseCurrency.equalsIgnoreCase(currCodeComm))
					{
						qtyComm1 = qtyStduom *  commPerc * exchRateComm;
					}
					else
					{
						qtyComm1 = qtyStduom *  commPerc * exchRate;
					}
				}
				if("Q".equalsIgnoreCase(commPercOn1))
				{
					if(!baseCurrency.equalsIgnoreCase(currCodeComm1))
					{
						qtyComm2 = qtyStduom *  commPerc1 * exchRateComm1;
					}
					else
					{
						qtyComm2 = qtyStduom *  commPerc1 * exchRate;
					}
				}
				if("Q".equalsIgnoreCase(commPercOn2))
				{
					if(!baseCurrency.equalsIgnoreCase(currCodeComm2))
					{
						qtyComm3 = qtyStduom *  commPerc2 * exchRateComm2;
					}
					else
					{
						qtyComm3 = qtyStduom *  commPerc2 * exchRate;
					}
				}

				qtyComm = qtyComm + qtyComm1 + qtyComm2 + qtyComm3 + commBl1 + commBl2 + commBl3;
				sp1Comm= sp1Comm + commBl1 + qtyComm1;
				sp2Comm= sp2Comm + commBl2 + qtyComm2 ;
				sp3Comm= sp3Comm + commBl3 + qtyComm3 ;
				qtyStduom = 0;
				//System.out.println("If line is o =" +commissionQty+"   "+salesPersComm+"    "+salesPersComm1+" 
			}
			//To calculate commission on BASE AMOUNT
			if("E".equalsIgnoreCase(commPercOn))
			{
				baseAmtComm1= (( netAmt - taxAmt) * commPerc) / 100;
			}
			if("E".equalsIgnoreCase(commPercOn1))
			{
				baseAmtComm2= (( netAmt - taxAmt) * commPerc1) / 100;
			}
			if("E".equalsIgnoreCase(commPercOn2))
			{
				baseAmtComm3= (( netAmt - taxAmt) * commPerc2) / 100;
			}

			baseAmtComm = baseAmtComm1 + baseAmtComm2 + baseAmtComm3;

			sp1Comm = sp1Comm + baseAmtComm1;
			sp2Comm = sp2Comm + baseAmtComm2;
			sp3Comm = sp3Comm + baseAmtComm3;

			//To calculate commission on ASSESSABLE AMOUNT
			if("M".equalsIgnoreCase(commPercOn) || "M".equalsIgnoreCase(commPercOn1) || "M".equalsIgnoreCase(commPercOn2) )
			{
				sql="SELECT ddf_get_tax_detail('S-ORD',sale_order,line_no,'EXC_TAX_CODE','A') AS asses_amt  FROM  sorddet WHERE sale_order = ? AND line_no = ?";		
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,saleOrder);
				pstmt.setString(2,lineNo);	
				rs = pstmt.executeQuery();
				if(rs.next())
				{  
					assessAmt = rs.getDouble("asses_amt");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}
			if("M".equalsIgnoreCase(commPercOn))
			{
				asesAmtComm1= (assessAmt * commPerc) / 100;
				asesAmtComm1= asesAmtComm1 * exchRate;
			}
			if("M".equalsIgnoreCase(commPercOn1))
			{
				asesAmtComm2= (assessAmt * commPerc1) / 100;
				asesAmtComm2= asesAmtComm2 * exchRate;
			}
			if("M".equalsIgnoreCase(commPercOn2))
			{
				asesAmtComm3= (assessAmt * commPerc2) / 100;
				asesAmtComm3= asesAmtComm3 * exchRate;
			}
			asesAmtComm= asesAmtComm1 + asesAmtComm2 + asesAmtComm3;
			sp1Comm = sp1Comm + asesAmtComm1;
			sp2Comm = sp2Comm + asesAmtComm2;
			sp3Comm = sp3Comm + asesAmtComm3;

			//To calculate commission on Taxable Amount
			if("T".equalsIgnoreCase(commPercOn) || "T".equalsIgnoreCase(commPercOn1) || "T".equalsIgnoreCase(commPercOn2))
			{
				sql="SELECT ddf_get_tax_detail('S-ORD',sale_order,line_no,'SALE_TAX_CODE','A') as sales_amt FROM  sorddet WHERE sale_order = ? AND line_no = ?";		
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,saleOrder);
				pstmt.setString(2,lineNo);	
				rs = pstmt.executeQuery();
				if(rs.next())
				{  
					salesAmt = rs.getDouble("sales_amt");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}
			if("T".equalsIgnoreCase(commPercOn))
			{
				taxAmtComm1= (salesAmt * commPerc) / 100;
				taxAmtComm1= taxAmtComm1 * exchRate;
			}
			if("T".equalsIgnoreCase(commPercOn1))
			{
				taxAmtComm2= (salesAmt * commPerc1) / 100;
				taxAmtComm2= taxAmtComm2 * exchRate;
			}
			if("T".equalsIgnoreCase(commPercOn2))
			{
				taxAmtComm3= (salesAmt * commPerc2) / 100;
				taxAmtComm3= taxAmtComm3 * exchRate;
			}

			taxAmtComm = taxAmtComm1 + taxAmtComm2 + taxAmtComm3;
			sp1Comm =  sp1Comm + taxAmtComm1;
			sp2Comm =  sp2Comm + taxAmtComm2;
			sp3Comm =  sp3Comm + taxAmtComm3;

			//To calculate commission on FOB
			if("F".equalsIgnoreCase(commPercOn))
			{
				fobComm1= (fobAmt * commPerc) / 100;
			}
			if("F".equalsIgnoreCase(commPercOn1))
			{
				fobComm2= (fobAmt * commPerc1) / 100;
			}
			if("F".equalsIgnoreCase(commPercOn2))
			{
				fobComm3= (fobAmt * commPerc2) / 100;
			}

			fobComm= fobComm1 + fobComm2 + fobComm3;

			sp1Comm =  sp1Comm + fobComm1;
			sp2Comm =  sp2Comm + fobComm2;
			sp3Comm =  sp3Comm + fobComm3;

			//To calculate commission on FOB LESS QUANTITY
			if("Y".equalsIgnoreCase(commPercOn))
			{
				fobQtyComm1= ((fobAmt - qtyComm) * commPerc) / 100;
			}
			if("Y".equalsIgnoreCase(commPercOn))
			{
				fobQtyComm2= ((fobAmt - qtyComm) * commPerc1) / 100;
			}
			if("Y".equalsIgnoreCase(commPercOn))
			{
				fobQtyComm3= ((fobAmt - qtyComm) * commPerc2) / 100;
			}
			fobQtyComm= fobQtyComm1 + fobQtyComm2 + fobQtyComm3;
			sp1Comm =  sp1Comm + fobQtyComm1;
			sp2Comm =  sp2Comm + fobQtyComm2;
			sp3Comm =  sp3Comm + fobQtyComm3;

			//To calculate commission on AMOUNT LESS QUANTITY

			if("Z".equalsIgnoreCase(commPercOn))
			{
				amtQtyComm1= ((netAmt - qtyComm) * commPerc) / 100;
			}
			if("Z".equalsIgnoreCase(commPercOn))
			{
				amtQtyComm2= ((netAmt - qtyComm) * commPerc1) / 100;
			}
			if("Z".equalsIgnoreCase(commPercOn))
			{
				amtQtyComm3= ((netAmt - qtyComm) * commPerc2) / 100;
			}
			amtQtyComm= amtQtyComm1 + amtQtyComm2 + amtQtyComm3;
			sp1Comm =  sp1Comm + amtQtyComm1;
			sp2Comm =  sp2Comm + amtQtyComm2;
			sp3Comm =  sp3Comm + amtQtyComm3;

			netComm = commAmt + qtyComm + fobComm + fobQtyComm + amtQtyComm + baseAmtComm + asesAmtComm + taxAmtComm ;

			if(netComm < 0)
			{
				netComm=0;
			}
			if(exchRateComm > 0)
			{
				sp1Comm =  sp1Comm / exchRateComm;

			}
			if(exchRateComm1 > 0)
			{
				sp2Comm =  sp2Comm / exchRateComm1;
			}
			if(exchRateComm2 > 0)
			{
				sp3Comm =  sp2Comm / exchRateComm2;
			}
			commissionMap.put("errorStr", errString);
			commissionMap.put("sp1Comm", sp1Comm);
			commissionMap.put("sp2Comm", sp2Comm);
			commissionMap.put("sp2Comm", sp3Comm);
			commissionMap.put("netComm", netComm);
			System.out.println("commissionMap:::"+commissionMap.toString());
		}
		catch(Exception e)
		{
			System.out.println("Exception ::calcCommission:"+e);
			throw new ITMException(e);
		}
		return commissionMap;
	}
	private HashMap commPercBase(String custCode, String itemCode,String itemSer, double ordPrice, String priceListDate, Connection conn) throws ITMException 
	{
		//This function calculates the commission perc required for Business Logic
		//First check comm perc from customeritem then from customer series then
		//from customer then pick price list code from disparm and based on price 
		//list pick the rate for case 'L'....
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		String sql="",defaultPriceList="",errString="";
		double commPerc=0.0,priceListVal=0.0;
		PreparedStatement pstmt = null ;
		ResultSet rs = null;
		DistCommon distCommon= new DistCommon(); 
		HashMap commPercMap = null;	

		try
		{
			commPercMap = new HashMap();
			sql="SELECT comm_perc__base FROM customeritem WHERE cust_code = ? AND item_code = ?";		
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,custCode);
			pstmt.setString(2,itemCode);	
			rs = pstmt.executeQuery();
			if(rs.next())
			{  
				commPerc = rs.getDouble(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if(commPerc == 0)
			{
				sql="SELECT comm_perc__base FROM customer_series WHERE cust_code = ? AND item_ser = ?";		
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, custCode);
				pstmt.setString(2, itemSer);	
				rs = pstmt.executeQuery();
				if(rs.next())
				{  
					commPerc = rs.getDouble(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(commPerc == 0)
				{
					sql="SELECT comm_perc__base FROM customer WHERE cust_code = ?";		
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, custCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{  
						commPerc = rs.getDouble(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if(commPerc == 0)
					{
						defaultPriceList=distCommon.getDisparams("999999", "BASE_PRICE_LIST", conn);
						System.out.println(">>>>>>>>>>>>>>>defaultPriceList:"+defaultPriceList);
						if("NULLFOUND".equalsIgnoreCase(defaultPriceList) || defaultPriceList == null || defaultPriceList.trim().length()==0)
						{
							errString = itmDBAccessLocal.getErrorString("","VTNOPL","Missing DISPARM Environment Variable: BASE_PRICE_LIST","",conn);
							commPercMap.put("errorStr", errString);
							return commPercMap;
						}
						else
						{
							priceListVal=distCommon.pickRate(defaultPriceList, priceListDate, itemCode, "", "L", conn);
							commPerc= ordPrice - priceListVal;
						}
					}
					else if(commPerc > 0)
					{
						defaultPriceList=distCommon.getDisparams("999999", "DEFAULT_PRICE_LIST", conn);
						System.out.println(">>>>>>>>>>>>>>>defaultPriceList:"+defaultPriceList);
						if("NULLFOUND".equalsIgnoreCase(defaultPriceList) || defaultPriceList == null || defaultPriceList.trim().length()==0)
						{
							errString = itmDBAccessLocal.getErrorString("","VTNOPL","","",conn);
							commPercMap.put("errorStr", errString);
							return commPercMap;
						}
						else
						{
							priceListVal=distCommon.pickRate(defaultPriceList, priceListDate, itemCode, "", "L", conn);
							commPerc = (priceListVal * commPerc) / 100;
							commPerc = ordPrice - commPerc;
						}
					}
				} 
				else if(commPerc > 0)
				{
					defaultPriceList=distCommon.getDisparams("999999", "DEFAULT_PRICE_LIST", conn);
					System.out.println(">>>>>>>>>>>>>>>defaultPriceList:"+defaultPriceList);
					if(defaultPriceList.equals("NULLFOUND") || defaultPriceList==null || defaultPriceList.trim().length()==0 )
					{
						// = 'VTNOPL' + "~t" + " Missing DISPARM Environment Variable: BASE_PRICE_LIST ";
						errString = itmDBAccessLocal.getErrorString("","VTNOPL","","",conn);
						commPercMap.put("errorStr", errString);
						return commPercMap;
					}
					else
					{
						priceListVal=distCommon.pickRate(defaultPriceList, priceListDate, itemCode, "", "L", conn);
						commPerc = (priceListVal * commPerc) / 100;
						commPerc = ordPrice - commPerc;
					}
				}
			}
			else if(commPerc > 0)
			{
				defaultPriceList=distCommon.getDisparams("999999", "DEFAULT_PRICE_LIST", conn);
				System.out.println(">>>>>>>>>>>>>>>defaultPriceList:"+defaultPriceList);
				if(defaultPriceList.equals("NULLFOUND") || defaultPriceList == null || defaultPriceList.trim().length()==0 )
				{
					errString = itmDBAccessLocal.getErrorString("","VTNOPL","","",conn);
					commPercMap.put("errorStr", errString);
					return commPercMap;
				}
				else
				{
					priceListVal=distCommon.pickRate(defaultPriceList, priceListDate, itemCode, "", "L", conn);
					commPerc = (priceListVal * commPerc) / 100;
					commPerc = ordPrice - commPerc;
					commPercMap.put("commPerc", commPerc);
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :"+e);
			throw new ITMException(e);
		}
		//need to return error code..
		return commPercMap;
	}
	private HashMap commPercSalesPers(String salesPers, double ordPrice,String priceListDate, String itemCode, Connection conn) throws ITMException
	{
		String sql="",salesPersCode="",priceList ="",errString="";
		double priceListVal=0.0,commPerc=0.0;
		PreparedStatement pstmt = null ;
		ResultSet rs = null;
		DistCommon distCommon= new DistCommon(); 
		HashMap commPercSalesMap = null;	
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		//New Function Added, Used By Business Logic For Sales Person Commission Type...
		try
		{
			commPercSalesMap =new HashMap();
			sql="SELECT price_list FROM sales_pers WHERE sales_pers = ?";		
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,salesPers);
			rs = pstmt.executeQuery();
			if(rs.next())
			{  
				priceList = rs.getString(1) == null ?"":rs.getString(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if(priceList.trim().length()==0)
			{
				errString = itmDBAccessLocal.getErrorString("","VTNOPL","","",conn);
				commPercSalesMap.put("errorStr", errString);
			}
			else
			{
				priceListVal=distCommon.pickRate(priceList, priceListDate, itemCode, "", "L", conn);
				commPerc= ordPrice - priceListVal;
				commPercSalesMap.put("commPercSales", commPerc);
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :"+e);
			throw new ITMException(e);
		}
		//need to return error code....
		return commPercSalesMap;
	}
	public int explodeBomDs(String saleOrder, String itemCode, String expLev, String lineNo, String lineType,Connection conn) throws ITMException
	{
		String sql="",sql1="",sql2="",mapplicable = "Y",mitemCode="" ,mitemRef="";
		String itemRef="";
		String siteCode="",unit="",itemCodeOrd="",itemFlag="",unitOrd="",itemSer="",siteCodeCr="",taxChapsord="";
		String munit="",mreqtype="",mnature="",mexptype="",merrstr="",itemStru="";
		String custCode="",orderType="",nearExpShelfLife="",round="",roundToStr="";
		String mlevel = "";
		String consumeFc = "", custCodeDlv = ""; // variable declared by mahesh saggam on 09/07/2019
		int incrmlevel=0;
		int mcnt=0,insertCnt=0;
		double mbatqty=0.0,mqtyper=0.0,mminqty=0.0,aminqty=0.0,amaxqty=0.0;
		double quantity=0.0,qtyOrd=0.0,rate=0.0,mqty=0.0, convQtyStd = 0; // convQtyStd variable added by mahesh saggam on 09/07/2019
		double minShelfLife=0.0,maxLife=0.0,temp=0.0,roundTo=0.0;
		Timestamp orderDate=null,dspDate=null, dueDate = null; // dueDate variable added by Mahesh Saggam on 09/07/2019
		int returnValue=0;
		PreparedStatement pstmt = null ,pstmt1=null,pstmt2=null;
		ResultSet rs = null,rs1=null,rs2=null;
		String taxChap=null;
		double mdiscPerc=0.0;// variable declared by nandkumar gadkari on 28/02/19
		DistCommon distCommon = new DistCommon();
		try
		{
			if("B".equalsIgnoreCase(lineType))
			{
				//sql="select item_code , item_ref from bomdet where bom_code = ? order by bom_code";	
                sql="select item_code , item_ref from bomdet where bom_code = ? order by line_no";	
			}else if("F".equalsIgnoreCase(lineType))
			{
				sql="select item_code, item_type from item where item_parnt  = ? and item_code <> item_parnt";	
			}
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,itemCode);
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				mitemCode = rs.getString(1) == null ? "" : rs.getString(1);
				mitemRef = rs.getString(2) == null ? "" : rs.getString(2);
				
				

				sql1 = "select site_code,unit,quantity,item_code__ord,item_flag,unit__ord, " +
						" qty_ord,order_date,dsp_date ,rate, "
						+ "due_date, conv__qty_stdqty, consume_fc, cust_code__dlv " // Added by Mahesh Saggam on 09/07/2019
						+ " from sorditem where sale_order=? and exp_lev=? and line_no=? ";
				pstmt1 = conn.prepareStatement(sql1);
				pstmt1.setString(1, saleOrder);
				pstmt1.setString(2, expLev);
				pstmt1.setString(3, lineNo);
				rs1 = pstmt1.executeQuery();
				while (rs1.next())
				{
					siteCode = rs1.getString("site_code");
					unit = rs1.getString("unit");
					quantity = rs1.getDouble("quantity");
					itemCodeOrd = rs1.getString("item_code__ord");
					itemFlag = rs1.getString("item_flag");
					unitOrd = rs1.getString("unit__ord");
					qtyOrd = rs1.getDouble("qty_ord");
					orderDate = rs1.getTimestamp("order_date");
					dspDate = rs1.getTimestamp("dsp_date");
					rate = rs1.getDouble("rate");
					// added by mahesh saggam on 09/07/2019 start
					dueDate = rs1.getTimestamp("due_date");
					convQtyStd = rs1.getDouble("conv__qty_stdqty");
					consumeFc = rs1.getString("consume_fc");
					custCodeDlv = rs1.getString("cust_code__dlv");
					// added by mahesh saggam on 09/07/2019 end

					if("B".equalsIgnoreCase(lineType))
					{
						sql2 = "select bom.unit, bom.batch_qty, bomdet.item_ref, bomdet.qty_per, bomdet.req_type, bomdet.min_qty, " +
								"bomdet.app_min_qty, bomdet.app_max_qty, bomdet.nature ,bomdet.disc_perc from bom, bomdet where bomdet.bom_code = ? and " +
								" bomdet.item_code = ? and bomdet.item_ref  = ? and bom.bom_code = bomdet.bom_code";//bomdet.disc_perc column added by nandkumar gadkari on 28/02/19 
						pstmt2 = conn.prepareStatement(sql2);
						pstmt2.setString(1, itemCode);
						pstmt2.setString(2, mitemCode);
						pstmt2.setString(3, mitemRef);

						rs2 = pstmt2.executeQuery();
						if (rs2.next())
						{
							munit = rs2.getString("unit");
							mbatqty = rs2.getDouble("batch_qty");
							mqtyper = rs2.getDouble("qty_per");
							mreqtype = rs2.getString("req_type");
							mminqty = rs2.getDouble("min_qty");
							aminqty = rs2.getDouble("app_min_qty");
							amaxqty = rs2.getDouble("app_max_qty");
							mnature = rs2.getString("nature");
							mdiscPerc = rs2.getDouble("disc_perc");//disc_perc column added by nandkumar gadkari on 28/02/19 
							System.out.println("aminqty"+aminqty+" "+"amaxqty"+amaxqty+" "+"disc_perc"+mdiscPerc);
							System.out.println("mreqtype"+mreqtype);
						}
						rs2.close();
						rs2 = null;
						pstmt2.close();
						pstmt2 = null;
						if(aminqty ==0 && amaxqty==0)
						{
							mapplicable = "Y";
						}else
						{
							if(quantity>aminqty && quantity<=amaxqty)
							{
								mapplicable = "Y";
							}else
							{
								mapplicable = "N";
							}
						}
						if("S".equalsIgnoreCase(mreqtype))
						{

							//Changed By PriyankaC on 3Sep2019 to round the division.
							mqty=geRndamt((quantity/mbatqty),"P",1)*mqtyper;
							//mqty=(quantity/mbatqty)*mqtyper;
							//mqty = geRndamt(mqty,"P",1); // Added By PriyankaC on 30Aug2019.
							System.out.println("mqty S:::"+mqty);
						}else if("P".equalsIgnoreCase(mreqtype))
						{
							mqty=(mqtyper/mbatqty)*quantity;
							System.out.println("mqty P:::"+mqty);
						}else if("F".equalsIgnoreCase(mreqtype))
						{
							mqty=mqtyper;
							System.out.println("mqty F:::"+mqty);
						}
						if(mqty<mminqty)
						{
							mminqty=mqty;
							System.out.println("mqty<mminqty:::::"+mqty);
						}
					}

					else
					{
						System.out.println("mqty S:::"+mqty);
						mqty=quantity;

						sql2="select unit from item where item_code = ?";
						pstmt2 = conn.prepareStatement(sql2);
						pstmt2.setString(1, mitemCode);
						rs2 = pstmt2.executeQuery();
						if (rs2.next())
						{
							munit = rs2.getString("unit");
						}
						rs2.close();
						rs2 = null;
						pstmt2.close();
						pstmt2 = null;
					}
					sql2="select count(*) from item where item_code = ?";
					pstmt2 = conn.prepareStatement(sql2);
					pstmt2.setString(1, mitemCode);
					rs2 = pstmt2.executeQuery();
					if (rs2.next())
					{
						mcnt=rs2.getInt(1);
					}
					rs2.close();
					rs2 = null;
					pstmt2.close();
					pstmt2 = null;
					if(mcnt==0)
					{
						sql2="select count(*) from bom where bom_code = ?";
						pstmt2 = conn.prepareStatement(sql2);
						pstmt2.setString(1, mitemCode);
						rs2 = pstmt2.executeQuery();
						if (rs2.next())
						{
							mcnt=rs2.getInt(1);
						}
						rs2.close();
						rs2 = null;
						pstmt2.close();
						pstmt2 = null;
						if(mcnt !=0)
						{
							mexptype="B";
						}else
						{
							merrstr = "VTITEM1";
						}
					}
					else
					{
						sql2="select item_stru from item where item_code = ?";
						pstmt2 = conn.prepareStatement(sql2);
						pstmt2.setString(1, mitemCode);
						rs2 = pstmt2.executeQuery();
						if (rs2.next())
						{
							mexptype = rs2.getString("item_stru");
							System.out.println("mexptype"+mexptype);
						}
						rs2.close();
						rs2 = null;
						pstmt2.close();
						pstmt2 = null;
						if(!"F".equalsIgnoreCase(mexptype))
						{
							mexptype="I";
						}
					}
					//PICKING MIN SHELF LIFE OF ITEM AND INSERTING IN SORDITEM IF MIN LIFE
					sql2="select min_shelf_life from customeritem where cust_code =  ? and item_code = ?";
					pstmt2 = conn.prepareStatement(sql2);
					pstmt2.setString(1, custCode);
					pstmt2.setString(2, mitemCode);
					rs2 = pstmt2.executeQuery();
					if (rs2.next())
					{
						minShelfLife = rs2.getDouble("min_shelf_life");
					}
					rs2.close();
					rs2 = null;
					pstmt2.close();
					pstmt2 = null;
					if(minShelfLife==0)
					{
						sql2="select min_shelf_life from item where item_code = ?";
						pstmt2 = conn.prepareStatement(sql2);
						pstmt2.setString(1, mitemCode);
						rs2 = pstmt2.executeQuery();
						if (rs2.next())
						{
							minShelfLife = rs2.getDouble("min_shelf_life");
						}
						rs2.close();
						rs2 = null;
						pstmt2.close();
						pstmt2 = null;
					}

					sql2="select order_type  from sorder where sale_order = ?";
					pstmt2 = conn.prepareStatement(sql2);
					pstmt2.setString(1, saleOrder);
					rs2 = pstmt2.executeQuery();
					if (rs2.next())
					{
						orderType = rs2.getString("order_type");
					}
					rs2.close();
					rs2 = null;
					pstmt2.close();
					pstmt2 = null;
					if("NE".equalsIgnoreCase(orderType))
					{
						sql2="select (case when no_sales_month is null then 0 else no_sales_month end)  as ll_max_life " +
								"from item where item_code = ?";
						pstmt2 = conn.prepareStatement(sql2);
						pstmt2.setString(1, mitemCode);
						rs2 = pstmt2.executeQuery();
						if (rs2.next())
						{
							maxLife = rs2.getDouble("ll_max_life");
						}
						rs2.close();
						rs2 = null;
						pstmt2.close();
						pstmt2 = null;
						if(maxLife==0)
						{
							nearExpShelfLife = distCommon.getDisparams("999999", "NEAR_EXP_SHELF_LIFE", conn);
							try {
								maxLife = Long.parseLong(nearExpShelfLife);
							} catch (NumberFormatException nfe) {
								System.out.println("NumberFormatException: " + nfe.getMessage());
							}
						}
						temp=maxLife;
						maxLife=minShelfLife;
						minShelfLife=temp;
					}
					if("Y".equalsIgnoreCase(mapplicable) || "C".equalsIgnoreCase(mnature))
					{
						sql2="select unit from item where item_code = ?";
						pstmt2 = conn.prepareStatement(sql2);
						pstmt2.setString(1, mitemCode);
						rs2 = pstmt2.executeQuery();
						if (rs2.next())
						{
							munit = rs2.getString("unit");
						}
						rs2.close();
						rs2 = null;
						pstmt2.close();
						pstmt2 = null;

						sql2="select round, round_to from uom  where unit = ?";
						pstmt2 = conn.prepareStatement(sql2);
						pstmt2.setString(1, munit);
						rs2 = pstmt2.executeQuery();
						if (rs2.next())
						{
							round = rs2.getString("round");
							roundToStr = rs2.getString("round_to");
						}
						rs2.close();
						rs2 = null;
						pstmt2.close();
						pstmt2 = null;
						if(round ==null || round.trim().length()==0)
						{
							round="N";
						}
						if(roundToStr ==null || roundToStr.trim().length()==0)
						{
							roundTo=0.001;
						}else
						{
							roundTo=Double.parseDouble(roundToStr);
						}
						mqty=geRndamt(mqty,round,roundTo);
						
						
						sql="Select tax_chap from sorditem where exp_lev= ? and sale_order= ? and line_no=? ";
						pstmt2=conn.prepareStatement(sql);
						pstmt2.setString(1,expLev);
						pstmt2.setString(2,saleOrder);
						pstmt2.setString(3,lineNo);
						rs2=pstmt2.executeQuery();
						if(rs2.next())
						{
							taxChapsord=rs2.getString(1);
						}
						pstmt2.close();
						pstmt2=null;
						rs2.close();
						rs2=null;
						
						
						
						sql="Select item_ser,site_code__ship,cust_code from sorder where sale_order= ?";
						pstmt2=conn.prepareStatement(sql);
						pstmt2.setString(1,saleOrder);
						rs2=pstmt2.executeQuery();
						if(rs2.next())
						{
							itemSer=rs2.getString(1);
							siteCodeCr=rs2.getString(2);
							custCode=rs2.getString(2);
						}
						
						
						pstmt2.close();
						pstmt2=null;
						rs2.close();
						rs2=null;
						
						// commented by rupali on 04/05/2021 to call getTaxChap method in all cases as suggested by SM sir
						/*if("C".equalsIgnoreCase(mnature))
						{
							taxChap=taxChapsord;
						}
						else*/
						{
							taxChap = distCommon.getTaxChap(mitemCode, itemSer, "C", custCode, siteCodeCr,conn);
							System.out.println("taxChapter["+taxChap+"]");
							
						}
						
						//added by rupali on 05/05/2021 to validate tax chapter [start]
						sql="SELECT * FROM ITEM WHERE ITEM_CODE = ? AND TAX_CHAP = ?";
						pstmt2= conn.prepareStatement(sql);
						pstmt2.setString(1, mitemCode);
						pstmt2.setString(2, taxChap);
						rs2 = pstmt2.executeQuery();
						if (!rs2.next()) 
						{
							System.out.println("inside explodeBomDs tax chap not validates");
							returnValue = -1;
							return returnValue;
						}
						rs2.close();
						rs2= null;
						pstmt2.close();
						pstmt2= null;
						//added by rupali on 05/05/2021 to validate tax chapter [end]
						
						incrmlevel++;
						mlevel=String.valueOf(incrmlevel)+".";
						sql="insert into sorditem "
								+ " (exp_lev, sale_order, line_no, site_code, item_code__ord," 
								+ " item_flag,unit__ord,qty_ord,item_code__ref,unit__ref,qty_ref,item_code,item_ref,"
								+ "quantity,unit, line_type,min_shelf_life,max_shelf_life,nature," +
								"order_date,order_type,dsp_date,rate , "
								//Changed by Manish on 12/09/16 for allocated quantity and despatch quantity[start]
								+" qty_alloc, qty_desp,tax_chap ,discount, "//discount column added by nandkumar gadkari on 28/02/19
								//Changed by Manish on 12/09/16 for allocated quantity and despatch quantity[start]
								+ "due_date, conv__qty_stdqty, consume_fc, cust_code__dlv )" // columns on this line added by Mahesh Saggam on 09/07/2019
								+ " values "
								+ "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, ?,?,?,?)";
						pstmt2=conn.prepareStatement(sql);
						pstmt2.setString(1,expLev+mlevel);
						pstmt2.setString(2,saleOrder);
						pstmt2.setString(3,lineNo);
						pstmt2.setString(4,siteCode);
						pstmt2.setString(5,itemCodeOrd);
						pstmt2.setString(6,itemFlag);
						pstmt2.setString(7,unitOrd);
						pstmt2.setDouble(8,qtyOrd);
						pstmt2.setString(9,itemCode);
						pstmt2.setString(10,unit);
						pstmt2.setDouble(11,quantity);
						pstmt2.setString(12,mitemCode);
						pstmt2.setString(13,mitemRef);
						pstmt2.setDouble(14,mqty);
						pstmt2.setString(15,munit);
						pstmt2.setString(16,mexptype);
						pstmt2.setDouble(17,minShelfLife);
						pstmt2.setDouble(18,maxLife);
						pstmt2.setString(19,mnature);
						pstmt2.setTimestamp(20,orderDate);
						pstmt2.setString(21,orderType);
						pstmt2.setTimestamp(22,dspDate);
						pstmt2.setDouble(23,rate);
						//Changed by Manish on 12/09/16 for allocated quantity and despatch quantity[start]
						pstmt2.setString(24,"0");
						pstmt2.setString(25,"0");
						//Changed by Manish on 12/09/16 for allocated quantity and despatch quantity[end]
						pstmt2.setString(26,taxChap);
						pstmt2.setDouble(27,mdiscPerc);//mdiscPerc column set by nandkumar gadkari on 28/02/19 
						// columns set by mahesh saggam on 09/07/2019 start
						pstmt2.setTimestamp(28, dueDate);
						pstmt2.setDouble(29, convQtyStd);
						pstmt2.setString(30, consumeFc);
						pstmt2.setString(31, custCodeDlv);
						// columns set by mahesh saggam on 09/07/2019 end
						insertCnt= pstmt2.executeUpdate();
						pstmt2.close();
						pstmt2=null;

					}
				}//20feb19[to close the cursor and pstmt while returning string]
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
			}
			/*rs1.close();
			rs1 = null;
			pstmt1.close();
			pstmt1 = null;*/


			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("insertCnt>>>>"+insertCnt);
			returnValue=insertCnt;
			System.out.println("returnValue>>>>"+returnValue);
		}catch(Exception e)
		{
			System.out.println("Exception :"+e);
			throw new ITMException(e);
		}
		return returnValue;
	}
	private double geRndamt(double netAmt, String round, double roundTo)
	{
		// TODO Auto-generated method stub
		double rndAmt=0.0,multiply=1,unrAmt=0;
		try
		{
			if(netAmt<0)
			{
				netAmt=abs(netAmt);
			}else if(netAmt==0)
			{
				return netAmt;
			}else if("N".equalsIgnoreCase(round))
			{
				return netAmt;
			}else if(roundTo==0)
			{
				return netAmt;
			}
			if("X".equalsIgnoreCase(round))
			{
				if(netAmt%roundTo>0)
				{
					rndAmt=netAmt-(netAmt%roundTo)+roundTo;
				}else
				{
					rndAmt=netAmt;
				}
			}else if("P".equalsIgnoreCase(round))
			{
				rndAmt=netAmt-(netAmt%roundTo);
			}else if("R".equalsIgnoreCase(round))
			{
				if(netAmt%roundTo<roundTo/2)
				{
					rndAmt=netAmt-(netAmt%roundTo);
				}else
				{
					rndAmt=netAmt-(netAmt%roundTo)+roundTo;
				}
			}else
			{
				rndAmt=netAmt;
			}

		} catch (Exception e)
		{
			// TODO: handle exception
		}

		return rndAmt;
	}
	private double abs(double netAmt)
	{
		// TODO Auto-generated method stub
		return 0;
	}
	
	//Method added by Varsha V to check business logic on SorderFormSplit as per instructed by KB Sir on 08-03-19.--START
	public String confSordWithBOChk(String saleOrder, String xtraParams, String forcedFlag,Connection conn)throws RemoteException, ITMException
	{
		System.out.println(">>>>>>>>>>>>>>>>>>SorderConf confSordWithBOChk called>>>>>>>>>>>>>>>>>>>");
		String confirmed = "";
		String sql = "";
		PreparedStatement pstmt = null,pstmt1=null;
		String errString = null;
		String custCode="",itemSer="",siteCode="",status="",stopBusiness="",custCodeBil="";
		String errCode="",totalAmtStr="";
		ResultSet rs = null,rs1=null;
		java.sql.Date orderDate = null;
		Timestamp dueDate=null;
		int cnt = 0,cnt1 = 0, retCtr=0;
		Boolean isSaleOrder=false;
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		PostOrderProcess postPrc= new PostOrderProcess();
		PostOrdCreditChk postcrdchk= new PostOrdCreditChk();
		ArrayList CreditCheckList= new ArrayList();
		try 
		{
			if (saleOrder != null && saleOrder.trim().length() > 0) 
			{
				sql = "select cust_code ,cust_code__bil, item_ser , site_code ,	due_date	, tot_amt, confirmed , " +
						"(CASE WHEN status IS NULL THEN 'P' ELSE status END) as status , order_date from sorder where sale_order = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, saleOrder);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					isSaleOrder=true;
					custCode = rs.getString("cust_code");
					custCodeBil = rs.getString("cust_code__bil");
					itemSer = rs.getString("item_ser");
					siteCode = rs.getString("site_code");
					dueDate = rs.getTimestamp("due_date");
					totalAmtStr = rs.getString("tot_amt");
					confirmed = rs.getString("confirmed");
					status = rs.getString("status");
					orderDate=rs.getDate("order_date");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(!isSaleOrder)
				{
					errString = itmDBAccessLocal.getErrorString("", "VTMCONF20", "","",conn);
					return errString;
				}else if(!"P".equalsIgnoreCase(status))
				{
					errString = itmDBAccessLocal.getErrorString("", "VTSOSTAT", "Sales order is not in pending status","",conn);
					return errString;

				}else if ("Y".equalsIgnoreCase(confirmed))
				{
					errString = itmDBAccessLocal.getErrorString("", "VTSCONF1", "","",conn);
					return errString;

				}else
				{
					sql = "select stop_business from customer where cust_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, custCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						stopBusiness = rs.getString("stop_business");
					}
					System.out.println("confirmed>>>>>>>>" + confirmed);
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if("Y".equalsIgnoreCase(stopBusiness))
					{
						errString = itmDBAccessLocal.getErrorString("", "VTICCW", "Sales Order " + saleOrder + " Not Confirmed,","",conn);
						return errString;
					}
					errCode=finCommon.nfCheckPeriod("SAL", orderDate, siteCode, conn);
					if(errCode.trim().length() > 0)
					{
						errString = itmDBAccessLocal.getErrorString("", errCode, "","",conn);
						return errString;
					}

					sql = "SELECT count(*) as cnt FROM sorddet WHERE sale_order =? AND quantity__stduom ='0'";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, saleOrder);
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						cnt = rs1.getInt(1);
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;

					if(cnt == 0)
					{
						sql = "SELECT count(*) AS cnt1 FROM sorddet WHERE sale_order= ? AND quantity='0'";
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1, saleOrder);
						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{
							cnt1 = rs1.getInt(1);
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;

						if(cnt1 > 0)
						{
							errString = itmDBAccessLocal.getErrorString("", "'VTSCONF2''", "","",conn);
							return errString;
						}

						HashMap CreditCheckMap = new HashMap();
						//Added By PriyankaC on 04June2019 [Start]
						/*CreditCheckMap.put("as_cust_code_bil", custCode);
						CreditCheckMap.put("as_cust_code_sold_to", custCodeBil);*/
						CreditCheckMap.put("as_cust_code_bil",custCodeBil);
						CreditCheckMap.put("as_cust_code_sold_to",custCode);
						//Added By PriyankaC on 04June2019[END]
						CreditCheckMap.put("as_item_ser", itemSer);
						CreditCheckMap.put("ad_net_amt", totalAmtStr);
						CreditCheckMap.put("as_sorder", saleOrder);
						CreditCheckMap.put("adt_tran_date", dueDate);
						CreditCheckMap.put("as_site_code", siteCode);
						CreditCheckMap.put("as_apply_time", "S");
						CreditCheckMap.put("as_despid", "");
						System.out.println("Credit check custCodeBill["+custCodeBil+"]");
						CreditCheckList = postcrdchk.CreditCheck(CreditCheckMap, conn);
						//Pavan Rane 27aug19 start[to display error message to front end]
						if(CreditCheckList.size() > 0 && CreditCheckList.contains("Error"))
						{
							conn.rollback();
							errString = CreditCheckList.get(CreditCheckList.indexOf("Error")+1).toString();
							return errString;
						}	
						//if(CreditCheckList.size() > 0)
						else if(CreditCheckList.size() > 0)//Pavan Rane 27aug19 end
						{												
							conn.rollback();
							retCtr = postPrc.writeBusinessLogicCheck(CreditCheckList, siteCode, "S", conn);

							System.out.println("@@@@@@@@@ insert retCtr[" + retCtr + "]errStringList.size()[" + CreditCheckList.size() + "]");
							if(retCtr > 0)
							{
								System.out.println("@@@@@@@@@ errorlist and inserted record missmatch........");
								//conn.commit();
							}
							errString = itmDBAccessLocal.getErrorString("", "VTWBLGCCHK", "","",conn);
							System.out.println("@@@@@@@@@@ writeBusinessLogicCheck errString[" + errString + "]");
							return errString;
						}
						errString = retriveSaleOrder(saleOrder, xtraParams, conn);
						System.out.println("retriveSaleOrder error code>>>>>" + errString);
						if(errString !=null && errString.trim().length() > 0)
						{
							System.out.println(">>>transaction not confirmaed");
						} else
						{
							String mainStr="";
							System.out.println(">>>Successful transaction confirmaed");
							errString = itmDBAccessLocal.getErrorString("", "VTCNFSUCC", "","",conn);
							System.out.println("errString@@@>>>"+errString);
							String begPart = errString.substring( 0, errString.indexOf("<trace>") + 7 );
							String endPart = errString.substring( errString.indexOf("</trace>"));
							mainStr="Sales order ";
							mainStr=begPart+mainStr+saleOrder+" is confirmed . ";
							if(mainStr.trim().length()==0)
							{
								mainStr = begPart;
							}
							mainStr = mainStr +  endPart;	
							errString = mainStr;
						}
					}
					else
					{
						errString = itmDBAccessLocal.getErrorString("","VTSCONF3","","",conn);
						return errString;
					}
				}
			}
		} catch (Exception e) 
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
				if(errString != null && errString.trim().length() > 0)
				{
					System.out.println("--going to commit tranaction--");
					if(errString.indexOf("VTCNFSUCC") > -1)
					{
						//conn.commit();
						System.out.println("--transaction commited--");
					}
					else
					{
						conn.rollback();
						System.out.println("--transaction rollback--");
					}
				}
				if(rs != null) 
				{
					rs.close();
					rs = null;
				}
				if(pstmt != null) 
				{
					pstmt.close();
					pstmt = null;
				}
			}
			catch(Exception e)
			{
				System.out.println("Exception : "+e);e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return errString;
	}
	//Method added by Varsha V to check business logic on SorderFormSplit as per instructed by KB Sir on 08-03-19--END
	//added by rupali on 20/04/2021 to explode pob details [start]
	public int explodePobDs(String saleOrder, String itemCode, String expLev, String lineNo, String lineType,Connection conn) throws ITMException
	{
		String sql="",sql1="",sql2="",mapplicable = "Y",mitemCode="" ,mitemRef="";
		String itemRef="";
		String siteCode="",unit="",itemCodeOrd="",itemFlag="",unitOrd="",itemSer="",siteCodeCr="",taxChapsord="";
		String munit="",mreqtype="",mnature="",mexptype="",merrstr="",itemStru="";
		String custCode="",orderType="",nearExpShelfLife="",round="",roundToStr="";
		String mlevel = "";
		String consumeFc = "", custCodeDlv = ""; // variable declared by mahesh saggam on 09/07/2019
		int incrmlevel=0;
		int mcnt=0,insertCnt=0;
		double mbatqty=0.0,mqtyper=0.0,mminqty=0.0,aminqty=0.0,amaxqty=0.0;
		double quantity=0.0,qtyOrd=0.0,rate=0.0,mqty=0.0, convQtyStd = 0; // convQtyStd variable added by mahesh saggam on 09/07/2019
		double minShelfLife=0.0,maxLife=0.0,temp=0.0,roundTo=0.0;
		Timestamp orderDate=null,dspDate=null, dueDate = null; // dueDate variable added by Mahesh Saggam on 09/07/2019
		int returnValue=0;
		PreparedStatement pstmt = null ,pstmt1=null,pstmt2=null;
		ResultSet rs = null,rs1=null,rs2=null;
		String taxChap=null;
		double mdiscPerc=0.0;// variable declared by nandkumar gadkari on 28/02/19
		DistCommon distCommon = new DistCommon();
		String quantityStr = "", freeQtyStr = "";
		double chageableQty = 0, freeQty = 0;
		try
		{
			sql1 = "select site_code,unit,quantity,item_code__ord,item_flag,unit__ord, " +
					" qty_ord,order_date,dsp_date ,rate, "
					+ "due_date, conv__qty_stdqty, consume_fc, cust_code__dlv " // Added by Mahesh Saggam on 09/07/2019
					+ " from sorditem where sale_order=? and exp_lev=? and line_no=? ";
			pstmt1 = conn.prepareStatement(sql1);
			pstmt1.setString(1, saleOrder);
			pstmt1.setString(2, expLev);
			pstmt1.setString(3, lineNo);
			rs1 = pstmt1.executeQuery();
			while (rs1.next())
			{
				siteCode = rs1.getString("site_code");
				unit = rs1.getString("unit");
				quantity = rs1.getDouble("quantity");
				itemCodeOrd = rs1.getString("item_code__ord");
				itemFlag = rs1.getString("item_flag");
				unitOrd = rs1.getString("unit__ord");
				qtyOrd = rs1.getDouble("qty_ord");
				orderDate = rs1.getTimestamp("order_date");
				dspDate = rs1.getTimestamp("dsp_date");
				rate = rs1.getDouble("rate");
				// added by mahesh saggam on 09/07/2019 start
				dueDate = rs1.getTimestamp("due_date");
				convQtyStd = rs1.getDouble("conv__qty_stdqty");
				consumeFc = rs1.getString("consume_fc");
				custCodeDlv = rs1.getString("cust_code__dlv");
				// added by mahesh saggam on 09/07/2019 end

				sql = "select * from pob_det where tran_id = (select tran_id__porcp from sorder where sale_order = ?) and item_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,saleOrder);
				pstmt.setString(2,itemCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					mitemCode = rs.getString("item_code") == null ? "" : rs.getString("item_code");
					mitemRef = mitemCode;
					mdiscPerc = rs.getDouble("discount");
					quantityStr = rs.getString("quantity") == null ? "0" : rs.getString("quantity");
					freeQtyStr = rs.getString("free_qty") == null ? " " : rs.getString("free_qty");
					if(quantityStr != null && quantityStr.trim().length()>0)
					{
						chageableQty=Double.parseDouble(quantityStr);
					}
					if(freeQtyStr != null && freeQtyStr.trim().length()>0)
					{
						freeQty=Double.parseDouble(freeQtyStr);
					}
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				sql2="select unit from item where item_code = ?";
				pstmt2 = conn.prepareStatement(sql2);
				pstmt2.setString(1, mitemCode);
				rs2 = pstmt2.executeQuery();
				if (rs2.next())
				{
					munit = rs2.getString("unit");
				}
				rs2.close();
				rs2 = null;
				pstmt2.close();
				pstmt2 = null;

				sql2="select item_stru from item where item_code = ?";
				pstmt2 = conn.prepareStatement(sql2);
				pstmt2.setString(1, mitemCode);
				rs2 = pstmt2.executeQuery();
				if (rs2.next())
				{
					mexptype = rs2.getString("item_stru");
					System.out.println("mexptype"+mexptype);
				}
				rs2.close();
				rs2 = null;
				pstmt2.close();
				pstmt2 = null;
				if(!"F".equalsIgnoreCase(mexptype))
				{
					mexptype="I";
				}
				//PICKING MIN SHELF LIFE OF ITEM AND INSERTING IN SORDITEM IF MIN LIFE
				sql2="select min_shelf_life from customeritem where cust_code =  ? and item_code = ?";
				pstmt2 = conn.prepareStatement(sql2);
				pstmt2.setString(1, custCode);
				pstmt2.setString(2, mitemCode);
				rs2 = pstmt2.executeQuery();
				if (rs2.next())
				{
					minShelfLife = rs2.getDouble("min_shelf_life");
				}
				rs2.close();
				rs2 = null;
				pstmt2.close();
				pstmt2 = null;
				if(minShelfLife==0)
				{
					sql2="select min_shelf_life from item where item_code = ?";
					pstmt2 = conn.prepareStatement(sql2);
					pstmt2.setString(1, mitemCode);
					rs2 = pstmt2.executeQuery();
					if (rs2.next())
					{
						minShelfLife = rs2.getDouble("min_shelf_life");
					}
					rs2.close();
					rs2 = null;
					pstmt2.close();
					pstmt2 = null;
				}

				//if("Y".equalsIgnoreCase(mapplicable) || "C".equalsIgnoreCase(mnature))
				{
					sql="Select tax_chap from sorditem where exp_lev= ? and sale_order= ? and line_no=? ";
					pstmt2=conn.prepareStatement(sql);
					pstmt2.setString(1,expLev);
					pstmt2.setString(2,saleOrder);
					pstmt2.setString(3,lineNo);
					rs2=pstmt2.executeQuery();
					if(rs2.next())
					{
						taxChapsord=rs2.getString(1);
					}
					pstmt2.close();
					pstmt2=null;
					rs2.close();
					rs2=null;

					sql="Select item_ser,site_code__ship,cust_code from sorder where sale_order= ?";
					pstmt2=conn.prepareStatement(sql);
					pstmt2.setString(1,saleOrder);
					rs2=pstmt2.executeQuery();
					if(rs2.next())
					{
						itemSer=rs2.getString(1);
						siteCodeCr=rs2.getString(2);
						custCode=rs2.getString(2);
					}

					pstmt2.close();
					pstmt2=null;
					rs2.close();
					rs2=null;

					for(int i = 1; i <= 2; i++)
					{
						if(i == 1)
						{
							if(chageableQty>0)
							{
								mnature = "C";
								quantity = chageableQty;
							}
							else
							{
								continue;
							}
						}
						
						if(i == 2)
						{
							if(freeQty>0)
							{
								mnature = "F";
								quantity = freeQty;
							}
							else
							{
								continue;
							}
						}
						
						mqty=quantity;
						System.out.println("mqty S:::"+mqty);
						
						sql2="select round, round_to from uom  where unit = ?";
						pstmt2 = conn.prepareStatement(sql2);
						pstmt2.setString(1, munit);
						rs2 = pstmt2.executeQuery();
						if (rs2.next())
						{
							round = rs2.getString("round");
							roundToStr = rs2.getString("round_to");
						}
						rs2.close();
						rs2 = null;
						pstmt2.close();
						pstmt2 = null;
						if(round ==null || round.trim().length()==0)
						{
							round="N";
						}
						if(roundToStr ==null || roundToStr.trim().length()==0)
						{
							roundTo=0.001;
						}else
						{
							roundTo=Double.parseDouble(roundToStr);
						}
						mqty=geRndamt(mqty,round,roundTo);
						
						// commented by rupali on 04/05/2021 to call getTaxChap method in all cases as suggested by SM sir
						/*if("C".equalsIgnoreCase(mnature))
						{
							taxChap=taxChapsord;
						}
						else
						*/
						{
							taxChap = distCommon.getTaxChap(mitemCode, itemSer, "C", custCode, siteCodeCr,conn);
							System.out.println("taxChaper["+taxChap+"]");
						}
						
						//added by rupali on 05/05/2021 to validate tax chapter [start]
						sql="SELECT * FROM ITEM WHERE ITEM_CODE = ? AND TAX_CHAP = ?";
						pstmt2= conn.prepareStatement(sql);
						pstmt2.setString(1, mitemCode);
						pstmt2.setString(2, taxChap);
						rs2 = pstmt2.executeQuery();
						if (!rs2.next()) 
						{
							System.out.println("inside explodePobDs tax chap not validates");
							returnValue = -1;
							return returnValue;
						}
						rs2.close();
						rs2= null;
						pstmt2.close();
						pstmt2= null;
						//added by rupali on 05/05/2021 to validate tax chapter [end]
						
						incrmlevel++;
						mlevel=String.valueOf(incrmlevel)+".";
						sql="insert into sorditem "
								+ " (exp_lev, sale_order, line_no, site_code, item_code__ord," 
								+ " item_flag,unit__ord,qty_ord,item_code__ref,unit__ref,qty_ref,item_code,item_ref,"
								+ "quantity,unit, line_type,min_shelf_life,max_shelf_life,nature," +
								"order_date,order_type,dsp_date,rate , "
								//Changed by Manish on 12/09/16 for allocated quantity and despatch quantity[start]
								+" qty_alloc, qty_desp,tax_chap ,discount, "//discount column added by nandkumar gadkari on 28/02/19
								//Changed by Manish on 12/09/16 for allocated quantity and despatch quantity[start]
								+ "due_date, conv__qty_stdqty, consume_fc, cust_code__dlv )" // columns on this line added by Mahesh Saggam on 09/07/2019
								+ " values "
								+ "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, ?,?,?,?)";
						pstmt2=conn.prepareStatement(sql);
						pstmt2.setString(1,expLev+mlevel);
						pstmt2.setString(2,saleOrder);
						pstmt2.setString(3,lineNo);
						pstmt2.setString(4,siteCode);
						pstmt2.setString(5,itemCodeOrd);
						pstmt2.setString(6,itemFlag);
						pstmt2.setString(7,unitOrd);
						pstmt2.setDouble(8,qtyOrd);
						pstmt2.setString(9,itemCode);
						pstmt2.setString(10,unit);
						pstmt2.setDouble(11,quantity);
						pstmt2.setString(12,mitemCode);
						pstmt2.setString(13,mitemRef);
						pstmt2.setDouble(14,mqty);
						pstmt2.setString(15,munit);
						pstmt2.setString(16,mexptype);
						pstmt2.setDouble(17,minShelfLife);
						pstmt2.setDouble(18,maxLife);
						pstmt2.setString(19,mnature);
						pstmt2.setTimestamp(20,orderDate);
						pstmt2.setString(21,orderType);
						pstmt2.setTimestamp(22,dspDate);
						pstmt2.setDouble(23,rate);
						//Changed by Manish on 12/09/16 for allocated quantity and despatch quantity[start]
						pstmt2.setString(24,"0");
						pstmt2.setString(25,"0");
						//Changed by Manish on 12/09/16 for allocated quantity and despatch quantity[end]
						pstmt2.setString(26,taxChap);
						pstmt2.setDouble(27,mdiscPerc);//mdiscPerc column set by nandkumar gadkari on 28/02/19 
						// columns set by mahesh saggam on 09/07/2019 start
						pstmt2.setTimestamp(28, dueDate);
						pstmt2.setDouble(29, convQtyStd);
						pstmt2.setString(30, consumeFc);
						pstmt2.setString(31, custCodeDlv);
						// columns set by mahesh saggam on 09/07/2019 end
						insertCnt= pstmt2.executeUpdate();
						pstmt2.close();
						pstmt2=null;
					}
				}
			}//20feb19[to close the cursor and pstmt while returning string]
			rs1.close();
			rs1 = null;
			pstmt1.close();
			pstmt1 = null;

			System.out.println("insertCnt>>>>"+insertCnt);
			returnValue=insertCnt;
			System.out.println("returnValue>>>>"+returnValue);
		}
		catch(Exception e)
		{
			System.out.println("Exception :"+e);
			throw new ITMException(e);
		}
		return returnValue;
	}
	//added by rupali on 20/04/2021 to explode pob details [end]
}