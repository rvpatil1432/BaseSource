/********************************************************
	Title : AdjIssueRcpConf[D16ASUN021]
	Date  : 09/05/16
	Developer: Chandrashekar

 ********************************************************/
package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;


import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.*;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.dis.StockUpdate;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.fin.InvAcct;
import ibase.webitm.ejb.sys.CreateRCPXML;

import java.sql.*;
import java.text.SimpleDateFormat;

import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.utility.ITMException;
import javax.ejb.Stateless;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

@Stateless
public class AdjIssueRcpConf extends ActionHandlerEJB implements AdjIssueRcpConfLocal, AdjIssueRcpConfRemote
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	FinCommon finCommon = new FinCommon();
	DistCommon disCommon= new DistCommon();
	
	//Added Method By Sarita on 19MARCH2018 [start]
	public String confirm(String tranId, String xtraParams, String forcedFlag)throws RemoteException, ITMException
	{
		System.out.println("Inside Confirm Method of Connection!!!!!!!!");
		String errString = "";
		boolean isError = false;
		Connection conn = null;
		try
		{
			ConnDriver connDriver = null;
			connDriver = new ConnDriver();
			conn = getConnection();		
			conn.setAutoCommit(false);
			errString = confirm(tranId, xtraParams,forcedFlag,conn);		
			System.out.println("errString[confirm] is ["+errString+"]");
			if(errString != null && !(errString.indexOf("VTMCONF2") > -1))
			{
	            isError = true;
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception [AdjIssueRcpConf]"+e);
			e.printStackTrace();
			isError = true;
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(isError)
				{
					conn.rollback();
					System.out.println("SRLContainerSplit connection rollback");
				}
				else
				{
					conn.commit();
					System.out.println("SRLContainerSplit connection committed");
				}
				if(conn != null)
				{
					conn.close();
					conn = null;
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
	//Added Method By Sarita on 19MARCH2018 [start]
	public String confirm(String tranId, String xtraParams, String forcedFlag,Connection conn)throws RemoteException, ITMException
	{
		System.out.println(">>>>>>>>>>>>>>>>>>AdjIssueRcpConf and receipt Conf confirm called>>>>>>>>>>>>>>>>>>>");
		String confirmed = "",runMode="";
		String sql = "";
		//Connection conn = null;
		PreparedStatement pstmt = null;
	    String errString = null;
	    String refSer = "",winName= "";
		ResultSet rs = null;
	    int cnt = 0;
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		Timestamp sysDate = null;
		E12GenericUtility genericUtility= new  E12GenericUtility();
		try 
		{
			//Commented by sarita on 19MARCH2018 [start]
			/*ConnDriver connDriver = null;
			connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);*/
			//Commented by sarita on 19MARCH2018 [end]
			runMode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "runMode");
			System.out.println("runMode["+runMode+"]");
			if (tranId != null && tranId.trim().length() > 0) 
			{
				sql = "	select confirmed,ref_ser from  adj_issrcp where tran_id = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					confirmed = rs.getString("confirmed");
					refSer = rs.getString("ref_ser");
				}
				System.out.println("confirmed>>>>>>>>"+confirmed);
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if("ADJISS".equalsIgnoreCase(refSer))
				{
					winName="w_adj_iss";
				}else if("ADJRCP".equalsIgnoreCase(refSer))
				{
					winName="w_adj_rcp";
				}
				 if ("Y".equalsIgnoreCase(confirmed) && "I".equalsIgnoreCase(runMode))
				{
					errString = itmDBAccessLocal.getErrorString("", "VTAJISS3", "","",conn);
					return errString;

				}else
				{
					sql = "	select count(*) from  adj_issrcpdet where tran_id = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, tranId);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						cnt = rs.getInt(1);
					}
					System.out.println("confirmed>>>>>>>>cnt is "+cnt);
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					if(cnt>0)
					{
						errString = retrieveAdjissrcp(tranId, winName,xtraParams,conn);
						System.out.println("retrieveAdjissrcp>>>"+errString);
						if (errString == null || errString.trim().length() == 0) 
						{
							//Commented by sarita on 19MARCH2018
							//conn.commit();
							errString = itmDBAccessLocal.getErrorString("", "VTMCONF2", "","",conn);
						}
						else
						{
							conn.rollback();
						}
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
				//Commented by sarita on 19MARCH2018[start]
			  /*if(conn != null)
				{
					conn.close();
					conn = null;
				}*/
				//Commented by sarita on 19MARCH2018[end]
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
	
	private String retrieveAdjissrcp(String tranId, String winName,String xtraParams, Connection conn) throws ITMException
    {
	    String errCode ="";
	    String sql = "";
	    String confirmed = "", tranIdStr = "",empCode = "",loginCode = "",siteCode = "";
	    String ledgPostConf = "",gsRunMode  = " ",loginEmpCode = "",chgTerm ="",errString = "";
	    String ediOption = "",dataStr = "",retString = "";
	    int cnt = 0,updCnt = 0;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;
	    boolean isTranExist =false;
	    ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
	    ITMDBAccessEJB itmDBAccessEJB = null;
	    Timestamp sysDate = null;
	    try
		{
	    	itmDBAccessEJB = new ITMDBAccessEJB();
	    	Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String sysDateStr = sdf.format(currentDate.getTime());
			System.out.println("Now the date is :=>  " + sysDateStr);
			sysDate = Timestamp.valueOf(genericUtility.getValidDateString(sysDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			loginEmpCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode");
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"chgTerm");
			String DB = CommonConstants.DB_NAME;
			System.out.println("DB  ==========>>>>>"+DB);
			if("mssql".equalsIgnoreCase(DB))
			{
				sql = "	select (case when  confirmed is null then 'N' else confirmed end) as confirmed from  adj_issrcp where tran_id = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					confirmed = rs.getString("confirmed");
				}
				System.out.println("confirmed>>>>>>>>"+confirmed);
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
			}else if("db2".equalsIgnoreCase(DB) || "mysql".equalsIgnoreCase(DB) )
			{
				sql = "	select tran_id from  adj_issrcp where tran_id = ?  for update ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					tranIdStr = rs.getString("tran_id");
					isTranExist = true;
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(!isTranExist)
				{
					errCode = itmDBAccessLocal.getErrorString("", "VTLCKERR", "","",conn);
					return errCode;
				}
				
				sql = "	select (case when confirmed is null then 'N' else confirmed end) as confirmed from  adj_issrcp where tran_id = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranIdStr);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					confirmed = rs.getString("confirmed");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
			}else
			{
				sql = "	select (case when confirmed is null then 'N' else confirmed end) as confirmed from  adj_issrcp where tran_id = ? for update nowait ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					confirmed = rs.getString("confirmed");
					isTranExist = true;
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(!isTranExist)
				{
					errCode = itmDBAccessLocal.getErrorString("", "VTLCKERR", "","",conn);
					return errCode;
				}
			}
			loginCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			sql = "select emp_code from users where code = ?  ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, loginCode);
			rs = pstmt.executeQuery();
			if (rs.next()) 
			{
				empCode = rs.getString("emp_code");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			sql = "select site_code  from adj_issrcp where tran_id  = ?  ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if (rs.next()) 
			{
				siteCode = rs.getString("site_code");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			sql = "select count(*)  from adj_issrcpdet a, item b 	where a.item_code = b.item_code " +
					"	and a.tran_id = ?  and (case when  b.qc_reqd is null then 'N' else b.qc_reqd end = 'Y') 	" +
					"	and (case when  b.stk_opt is null then '0' else b.stk_opt end) <> '2'  "; 
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if (rs.next()) 
			{
				cnt = rs.getInt(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if(cnt > 0)
			{
				errCode = itmDBAccessLocal.getErrorString("", "VTSTKOPT", "","",conn);
				return errCode;
			}
			sql = "select (case when ledg_post_conf is null then 'N' else ledg_post_conf end) as ledg_post_conf " +
					" from transetup 	where lower(tran_window) = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, winName);
			rs = pstmt.executeQuery();
			if (rs.next()) 
			{
				 ledgPostConf = rs.getString("ledg_post_conf");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if ( "Y".equalsIgnoreCase(ledgPostConf) && !"B".equalsIgnoreCase(gsRunMode))
			{
				sql = " update adj_issrcp set emp_code__aprv = ?,chg_term = ?, tran_date = ?  where tran_id = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, loginEmpCode);
				pstmt.setString(2, chgTerm);
				pstmt.setTimestamp(3, sysDate);
				pstmt.setString(4, tranId);
				pstmt.executeUpdate();
				pstmt.close();
				pstmt = null;
			}else
			{
				sql = " update adj_issrcp set emp_code__aprv = ?,chg_term = ?  where tran_id = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, loginEmpCode);
				pstmt.setString(2, chgTerm);
				pstmt.setString(3, tranId);
				pstmt.executeUpdate();
				pstmt.close();
				pstmt = null;
			}
			errCode =  confirmAdjissrcp(tranId,xtraParams,conn);
			System.out.println("confirmAdjissrcp errCode>>>>>["+errCode+"]");
			if (errCode == null || errCode.trim().length() == 0)
			{
				sql = " update adj_issrcp set confirmed = 'Y', conf_date = ?  where tran_id = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, sysDate);
				pstmt.setString(2, tranId);
				updCnt =pstmt.executeUpdate();
				pstmt.close();
				pstmt = null;
				if (!(updCnt == 1))
				{
					errCode = itmDBAccessLocal.getErrorString("", "VTAJISS2", "","",conn);
					return errCode;
				}
			}
			if(errCode.trim().length()>0)
			{
				conn.rollback();
			}else
			{
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
				System.out.println("@@@@@@@@@@@@@@@ ediOption  called next.............."+ediOption);
				ediOption = ediOption != null?ediOption:"0";
				int ediOpt = Integer.parseInt(ediOption);
				if(ediOpt > 0)
				{
					CreateRCPXML createRCPXML = new CreateRCPXML(winName, "tran_id");
					dataStr = createRCPXML.getTranXML(tranId, conn);
					System.out.println("dataStr =[ " + dataStr + "]");
					Document ediDataDom = genericUtility.parseString(dataStr);

					E12CreateBatchLoadEjb e12CreateBatchLoad = new E12CreateBatchLoadEjb();
					retString = e12CreateBatchLoad.createBatchLoad(ediDataDom, winName, ""+ediOpt , xtraParams, conn);
					createRCPXML = null;
					e12CreateBatchLoad = null;

					if (retString != null && "SUCCESS".equals(retString))
					{
						System.out.println("retString from batchload = [" + retString + "]");
					}
				}
				
				//Commented as per manoharan sir suggestion
				/*if ("2".equals(ediOption)) 
				{
					CreateRCPXML createRCPXML = new CreateRCPXML(winName,"tran_id");
					dataStr = createRCPXML.getTranXML(tranId, conn);
					System.out.println("dataStr =[ " + dataStr + "]");
					Document ediDataDom = genericUtility.parseString(dataStr);

					E12CreateBatchLoadEjb e12CreateBatchLoad = new E12CreateBatchLoadEjb();
					retString = e12CreateBatchLoad.createBatchLoad(ediDataDom,winName, "2", xtraParams, conn);
					createRCPXML = null;
					e12CreateBatchLoad = null;

					if (retString != null && "SUCCESS".equalsIgnoreCase(retString)) 
					{
						System.out.println("retString from batchload = [" + retString + "]");
					}
				}

				else 
				{
					CreateRCPXML createRCPXML = new CreateRCPXML(winName,"tran_id");
					dataStr = createRCPXML.getTranXML(tranId, conn);
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
				}*/
				System.out.println("@@@@@@@@@@@@@@@ ediOption  called end..............");
				
			}
			/*if (errCode != null && errCode.trim().length() > 0)
			{
				conn.rollback();
			} else
			{
				conn.commit();
			}*/
			
		}  // end try
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			try
            {
				System.out.println("@@@@@@@connection roll back@@@@");
	            conn.rollback();
            } catch (SQLException e1)
            {
	            // TODO Auto-generated catch block
	            e1.printStackTrace();
            }
			throw new ITMException(e);
		}
		return errCode;
    }

	private String  confirmAdjissrcp(String tranId,String xtraParams, Connection conn) throws ITMException
	{
		PreparedStatement pstmt = null, pstmt1 = null;
		ResultSet rs = null,rs1=null;
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		ITMDBAccessEJB itmDBAccessEJB = null;
		String sql = "", sql1="",siteCode = "",refSer = "", reasCode = "";
		String lineNo = "", itemCode = "" ,unit = "", locCode = "",lotNo = "",lotSl = "",acctCodeCr = "";
		String acctCodeDr = "",cctrCodeCr = "",cctrCodeDr ="",grade ="",packCode = "",siteCodeMfg = "",suppCodeMfg = "";
		String unitAlt = "",convQtyStduom = "",itemSer = "",invStat = "",batchNo = "";
		String errCode = "",dimension = "",lineNoPost="";
		int itemCnt = 0,hdrCnt =0;
		double quantity =0.0,rate =0.0,grossWeight=0.0,tareWeight=0.0,netWeight=0.0,noArt=0.0,grossRate = 0.0,potencyPerc =0.0;
		double stkBefQty =0.0,stkAfterQty = 0.0;
		Timestamp mfgDate = null,expDate = null,retestDate = null;
		Timestamp sysDate = null,tranDate = null;
		HashMap stockUpd = new HashMap();
		try
		{
			StockUpdate stockUpdate = new StockUpdate();
			sql = "Select tran_date, site_code,ref_ser, reas_code From adj_issrcp Where tran_id = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				hdrCnt++;
				tranDate = rs.getTimestamp("tran_date");
				siteCode = checkNull(rs.getString("site_code"));
				refSer = rs.getString("ref_ser");
				reasCode = rs.getString("reas_code");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			if(hdrCnt==0)
			{
				errCode = itmDBAccessLocal.getErrorString("", "VTAJISS2", "","",conn);
				return errCode;
			}
			sql = "Select line_no,item_code, quantity, 	rate,unit, loc_code	, lot_no, 	lot_sl," +
					"acct_code__cr	, acct_code__dr	, cctr_code__cr, 	cctr_code__dr	, gross_weight	," +
					" tare_weight, net_weight,	grade, no_art, gross_rate, dimension,potency_perc," +
					"mfg_date, exp_date, pack_code,site_code__mfg ,unit__alt, conv__qty_stduom, batch_no" +
					",supp_code__mfg,retest_date From  adj_issrcpdet Where tran_id = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				lineNo = checkNull(rs.getString("line_no")).trim();
				itemCode = checkNull(rs.getString("item_code"));
				quantity = rs.getDouble("quantity");
				rate = rs.getDouble("rate");
				unit = rs.getString("unit");
				locCode = checkNull(rs.getString("loc_code"));
				lotNo = checkNull(rs.getString("lot_no"));
				lotSl = checkNull(rs.getString("lot_sl"));
				acctCodeCr = rs.getString("acct_code__cr");
				acctCodeDr = rs.getString("acct_code__dr");
				cctrCodeCr = rs.getString("cctr_code__cr");
				cctrCodeDr = rs.getString("cctr_code__dr");
				grossWeight = rs.getDouble("gross_weight");
				tareWeight = rs.getDouble("tare_weight");
				netWeight = rs.getDouble("net_weight");
				grade = rs.getString("grade");
				noArt = rs.getDouble("no_art");
				grossRate = rs.getDouble("gross_rate");
				dimension = rs.getString("dimension");
				potencyPerc = rs.getDouble("potency_perc");
				mfgDate = rs.getTimestamp("mfg_date");
				expDate = rs.getTimestamp("exp_date");
				packCode = rs.getString("pack_code");
				siteCodeMfg = rs.getString("site_code__mfg");
				unitAlt = rs.getString("unit__alt");
				convQtyStduom = rs.getString("conv__qty_stduom");
				batchNo = rs.getString("batch_no");
				suppCodeMfg=rs.getString("supp_code__mfg");
				retestDate = rs.getTimestamp("retest_date");
				lineNoPost=lineNo;
				
				sql1 = "select (case when quantity is null then 0 else quantity end) as befqty  from 	 stock where  item_code=? " +
						" and	 site_code =? and	loc_code  =? and lot_no  =? and lot_sl  =? ";
				pstmt1 = conn.prepareStatement(sql1);
				pstmt1.setString(1, itemCode);
				pstmt1.setString(2, siteCode);
				pstmt1.setString(3, locCode);
				pstmt1.setString(4, lotNo);
				pstmt1.setString(5, lotSl);
				rs1 = pstmt1.executeQuery();
				if (rs1.next())
				{
					stkBefQty = rs1.getDouble("befqty");
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
				
				stkAfterQty = stkBefQty-quantity;
				sql1 = "select item_ser from 	 siteitem where  item_code = ? and	 site_code = ? ";
				pstmt1 = conn.prepareStatement(sql1);
				pstmt1.setString(1, itemCode);
				pstmt1.setString(2, siteCode);
				rs1 = pstmt1.executeQuery();
				if (rs1.next())
				{
					itemSer = rs1.getString("item_ser");
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
				if(itemSer == null || itemSer.trim().length() ==0)
				{
					sql1 = "select item_ser,unit from 	 item where  item_code = ?  ";
					pstmt1 = conn.prepareStatement(sql1);
					pstmt1.setString(1, itemCode);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						itemCnt++;
						itemSer = rs1.getString("item_ser");
						unit = rs1.getString("unit");
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					if(itemCnt==0)
					{
						rs.close(); //Pavan [closed expected opening cursor and pstmt].
						rs = null;
						pstmt.close();
						pstmt = null;
						errCode = itmDBAccessLocal.getErrorString("", "VMITEM1", "","",conn);
						return errCode;
					}
				}
				sql1 = "select inv_stat from 	 location where  loc_code =?  ";
				pstmt1 = conn.prepareStatement(sql1);
				pstmt1.setString(1, locCode);
				rs1 = pstmt1.executeQuery();
				if (rs1.next())
				{
					invStat = rs1.getString("inv_stat");
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
				
				stockUpd.put("item_code",itemCode);
				stockUpd.put("site_code",siteCode);
				stockUpd.put("loc_code",locCode);
				stockUpd.put("lot_no",lotNo);
				stockUpd.put("lot_sl",lotSl);
				stockUpd.put("quantity",quantity);
				stockUpd.put("unit",unit);
				if("ADJISS".equalsIgnoreCase(refSer))
				{
					stockUpd.put("tran_type","ID");
					stockUpd.put("acct_code_inv",acctCodeCr);
					stockUpd.put("cctr_code_inv",cctrCodeCr);
				}else
				{
					stockUpd.put("tran_type","R");
					stockUpd.put("acct_code_inv",acctCodeDr);
					stockUpd.put("cctr_code_inv",cctrCodeDr);
				}
					
				stockUpd.put("tran_date",tranDate);
				stockUpd.put("tran_ser",refSer);
				stockUpd.put("tran_id",tranId);
				stockUpd.put("acct_code__cr",acctCodeCr);
				stockUpd.put("acct_code__dr",acctCodeDr);
				stockUpd.put("cctr_code__cr",cctrCodeCr);
				stockUpd.put("cctr_code__dr",cctrCodeDr);
				lineNo= "    "+lineNo;
				lineNo = lineNo.substring(lineNo.length()-4, lineNo.length());
				stockUpd.put("line_no",lineNo);
				stockUpd.put("qty_stduom",quantity);
				stockUpd.put("rate",rate);
				stockUpd.put("no_art",noArt);
				stockUpd.put("inv_stat",invStat);
				stockUpd.put("item_ser",itemSer);
				stockUpd.put("grade",grade);
				stockUpd.put("gross_rate",grossRate);
				stockUpd.put("dimension",dimension);
				stockUpd.put("reas_code",reasCode);
				stockUpd.put("potency_perc",potencyPerc);
				stockUpd.put("mfg_date",mfgDate);
				stockUpd.put("exp_date",expDate);
				stockUpd.put("pack_code",packCode);
				//stockUpd.put("supp_code__mfg",siteCodeMfg);
				//Modified by Anjali R. on [15/05/2019][Start]
				//stockUpd.put("gross_weight",grossRate);
				stockUpd.put("gross_weight",grossWeight);
				//Modified by Anjali R. on [15/05/2019][End]
				stockUpd.put("tare_weight",tareWeight);
				stockUpd.put("net_weight",netWeight);
				stockUpd.put("unit__alt",unitAlt);
				stockUpd.put("conv__qty_stduom",convQtyStduom);
				if("ADJRCP".equalsIgnoreCase(refSer))
				{
					stockUpd.put("actual_rate",rate);
				}
				if(batchNo != null && batchNo.trim().length() >0)
				{
					stockUpd.put("batch_no",batchNo);
				}
				if(siteCodeMfg != null && siteCodeMfg.trim().length() >0)
				{
					stockUpd.put("site_code__mfg",siteCodeMfg);
				}
				if(suppCodeMfg != null && suppCodeMfg.trim().length() >0)
				{
					stockUpd.put("supp_code__mfg",suppCodeMfg);
				}
				if(retestDate != null)
				{
					stockUpd.put("retest_date",retestDate);
				}
				errCode	= stockUpdate.updateStock(stockUpd,xtraParams, conn);
				if(errCode !=null && errCode.trim().length() >0)  
				{
					rs.close(); //Pavan [closed expected opening cursor and pstmt].
					rs = null;
					pstmt.close();
					pstmt = null;
					return errCode;
				}
				errCode	=invAdjPost(tranId, rate, refSer, lineNoPost,conn);
				if(errCode !=null && errCode.trim().length() >0)  
				{
					rs.close(); //Pavan [closed expected opening cursor and pstmt].
					rs = null;
					pstmt.close();
					pstmt = null;
					return errCode;
				}
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
		} // end try
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			try
			{
				System.out.println("@@@@@@@connection roll back@@@@");
				conn.rollback();
			} catch (SQLException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			throw new ITMException(e);
		}
		return errCode;
	}

	private String invAdjPost(String tranId, double rate, String refSer, String lineNo, Connection conn) throws ITMException
    {
		PreparedStatement pstmt1 = null;
		PreparedStatement pstmt = null;
		ResultSet rs1 = null ;
		ResultSet rs = null ;
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		InvAcct invAcctObj= new InvAcct();
		
		String invParam="",rateOpt="",sql="",invOnline="",errCode = "",itemCode = "",sql1= "",invAcct = "",costPrice = "",adjPost = "";
		String siteCode ="",acctCodeDr ="",cctrCodeDr ="",acctCodeCr= "",cctrCodeCr= "",tranType= "",siteType = "",finEntity = "";
		String currCode = "",priceListCost = "",tranDateStr ="",refId ="",remarks = "",analysis = "";
		String analysis1 = "",analysis2 ="",analysis3="";
		int intExit =0,lineNoInt =0,siteeCnt =0,curFinCnt =0;
		double quantity =0.0,costRate =0.0,rateDet=0.0,amount = 0;
		Timestamp tranDate = null,effDate=null;
		HashMap gltraceMap = new HashMap();
		try
		{
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());

			if(refSer.equals("ADJISS"))
			{
				invParam="INV_ACCT_AISS";
			}
			else if(refSer.equals("ADJRCP"))
			{
				invParam="INV_ACCT_ARCP";
			}
			else if(refSer.equals("SCRRCP"))
			{
				invParam="INV_ACCT_ARCP";
			}

			invOnline = checkNull(finCommon.getFinparams("999999",invParam, conn));
			if("NULLFOUND".equalsIgnoreCase(invOnline))
			{
				errCode = itmDBAccessLocal.getErrorString("", "VTFINPARM", " not defined under Financial Variables","",conn);
				return errCode;
			}
			
			ArrayList<String> al = new ArrayList<String>();
			al.add("S");
			al.add("Y");
			al.add("N");
			if(!al.contains(invOnline))
			{
				errCode = itmDBAccessLocal.getErrorString("", "VTFINPARM1", "value should be Y or N or S under Financial Variables","",conn);
				return errCode;
			}
			if(invOnline == null || invOnline.trim().length() ==0 )
			{
				invOnline = "N";
			}
			if("S".equalsIgnoreCase(invOnline))
			{
				sql = "Select item_code From adj_issrcpdet where tran_id = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				while (rs.next())
				{
					itemCode = rs.getString("item_code");
					
					sql1 = "Select (case when b.inv_acct is null then 'N' else b.inv_acct end) as inv_acct From   Item a,ItemSer b " +
							" Where a.item_ser 	= b.item_ser And   a.item_code 	= ?";
					pstmt1 = conn.prepareStatement(sql1);
					pstmt1.setString(1, itemCode);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						invAcct = rs1.getString("inv_acct");
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					
					if(!"Y".equalsIgnoreCase(invAcct))
					{
						intExit =1;
						break;
					}
					
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}
			costPrice = checkNull(finCommon.getFinparams("999999","STD_COST_PRICE", conn));
			adjPost = checkNull(finCommon.getFinparams("999999","ADJ_ISS_RCP_POST", conn));
			if("NULLFOUND".equalsIgnoreCase(adjPost))
			{
				errCode = itmDBAccessLocal.getErrorString("", "VTFINPARM", " Variabe ADJ_ISS_RCP_POST not defined under Distribution Variable.","",conn);
				return errCode;
			}
			if("S".equalsIgnoreCase(adjPost) && "NULLFOUND".equalsIgnoreCase(costPrice) )
			{
				errCode = itmDBAccessLocal.getErrorString("", "VTFINPARM", " Variable STD_COST_PRICE Cost Price List not defined.","",conn);
				return errCode;
			}
			
			if("Y".equalsIgnoreCase(invOnline) || ("S".equalsIgnoreCase(invOnline) && intExit !=1))
			{
				if (lineNo != null && lineNo.trim().length() > 0)
				{
					try
					{
						lineNoInt= Integer.parseInt(lineNo);
					} catch (NumberFormatException n)
					{
						lineNoInt = 0;
					}
				}else
					
				{
					lineNoInt=0;
				}
				
				sql = "select  b.item_code,a.tran_date, a.eff_date,a.ref_ser,a.site_code,b.quantity,b.acct_code__dr," +
						" b.cctr_code__dr, b.acct_code__cr,b.cctr_code__cr, a.tran_id, b.item_code ,a.tran_type,b.rate from " +
						" adj_issrcp a, adj_issrcpdet b where   a.tran_id =  b.tran_id and 	  a.tran_id = ? and 	  b.line_no = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				pstmt.setInt(2, lineNoInt);
				rs = pstmt.executeQuery();
				while (rs.next())
				{
					tranDate = rs.getTimestamp("tran_date");
					effDate = rs.getTimestamp("eff_date");
					refSer = rs.getString("ref_ser");
					siteCode = rs.getString("site_code");
					quantity = rs.getDouble("quantity");
					acctCodeDr = rs.getString("acct_code__dr");
					cctrCodeDr = rs.getString("cctr_code__dr");
					acctCodeCr = rs.getString("acct_code__cr");
					cctrCodeCr = rs.getString("cctr_code__cr");
					refId = rs.getString("tran_id");
					tranType = rs.getString("tran_type");
					rateDet = rs.getDouble("rate");
					itemCode = rs.getString("item_code");
					/*if(tranDateStr != null && tranDateStr.trim().length() >0)
					{
						tranDate = Timestamp.valueOf(genericUtility.getValidDateString(tranDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
					}*/
					
					tranDateStr = simpleDateFormat.format(tranDate);
					System.out.println("tranDateStr@@@@@@@@@"+tranDateStr);
					sql1 = "select site_type from site where site_code = ? ";
					pstmt1 = conn.prepareStatement(sql1);
					pstmt1.setString(1, siteCode);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						siteType = rs1.getString("site_type");
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					if(refSer == null || refSer.trim().length() ==0 )
					{
						rs.close(); //Pavan [closed expected opening cursor and pstmt].
						rs = null;
						pstmt.close();
						pstmt = null;
						errCode = itmDBAccessLocal.getErrorString("", "VTREFID", "","",conn);
						return errCode;
					}
					
					sql1 = "select fin_entity from site where site_code = ? ";
					pstmt1 = conn.prepareStatement(sql1);
					pstmt1.setString(1, siteCode);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						siteeCnt++;
						finEntity = rs1.getString("fin_entity");
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					if(siteeCnt ==0)
					{
						rs.close(); //Pavan [closed expected opening cursor and pstmt].
						rs = null;
						pstmt.close();
						pstmt = null;
						errCode = itmDBAccessLocal.getErrorString("", "VTSITECD1", "","",conn);
						return errCode;
					}
					if(finEntity == null || finEntity.trim().length() ==0 )
					{
						rs.close(); //Pavan [closed expected opening cursor and pstmt].
						rs = null;
						pstmt.close();
						pstmt = null;
						errCode = itmDBAccessLocal.getErrorString("", "VMFINENT", "","",conn);
						return errCode;
					}
					sql1 = "select curr_code  from finent where fin_entity = ? ";
					pstmt1 = conn.prepareStatement(sql1);
					pstmt1.setString(1, finEntity);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						curFinCnt++;
						currCode = rs1.getString("curr_code");
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					if(curFinCnt==0)
					{
						rs.close(); //Pavan [closed expected opening cursor and pstmt].
						rs = null;
						pstmt.close();
						pstmt = null;
						errCode = itmDBAccessLocal.getErrorString("", "VTFINENT1", "","",conn);
						return errCode;
					}
					if(currCode == null || currCode.trim().length() ==0 )
					{
						rs.close(); //Pavan [closed expected opening cursor and pstmt].
						rs = null;
						pstmt.close();
						pstmt = null;
						errCode = itmDBAccessLocal.getErrorString("", "VTCURFIN", "","",conn);
						return errCode;
					}
					if("S".equalsIgnoreCase(adjPost))
					{
						sql1 = "select b.price_list__cost from item a, itemser b where a.item_ser = b.item_ser and a.item_code = ? ";
						pstmt1 = conn.prepareStatement(sql1);
						pstmt1.setString(1, itemCode);
						rs1 = pstmt1.executeQuery();
						if (rs1.next())
						{
							priceListCost = rs1.getString("price_list__cost");
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
						if(priceListCost != null && priceListCost.trim().length()>0)
						{
							costPrice=priceListCost;
						}
						costRate=disCommon.pickRate(costPrice, tranDateStr, itemCode, "", "L", conn);
						if(costRate == -1)
						{
							costRate=rateDet;
						}
						if(costRate==0)
						{
							rs.close(); //Pavan [closed expected opening cursor and pstmt].
							rs = null;
							pstmt.close();
							pstmt = null;
							errCode = itmDBAccessLocal.getErrorString("", "VTITCOST", "","",conn);
							return errCode;
						}
						rate=costRate;
					}
					
					if(rate == 0)
					{
						rs.close(); //Pavan [closed expected opening cursor and pstmt].
						rs = null;
						pstmt.close();
						pstmt = null;
						errCode = itmDBAccessLocal.getErrorString("", "VTSTKRATE", "","",conn);
						return errCode;
					}
					amount=quantity*rate;
					System.out.println("AMOUNT>>>>"+amount);
					if(amount !=0)
					{
						gltraceMap.put("tran_date",tranDate);
						gltraceMap.put("eff_date",effDate);
						gltraceMap.put("fin_entity",finEntity);
						gltraceMap.put("site_code",siteCode);
						gltraceMap.put("sundry_type","O");
						gltraceMap.put("sundry_code",null);
						gltraceMap.put("acct_code",acctCodeCr);
						gltraceMap.put("cctr_code",cctrCodeCr);
						gltraceMap.put("emp_code",null);
						gltraceMap.put("anal_code",null);
						gltraceMap.put("curr_code",currCode);
						gltraceMap.put("exch_rate",1);
						
						if(amount>0)
						{
							gltraceMap.put("dr_amt",0);
							gltraceMap.put("cr_amt",amount);
						}else
						{
							gltraceMap.put("dr_amt",0-amount);
							gltraceMap.put("cr_amt",0);
							
						}
						
						gltraceMap.put("ref_type","D");
						gltraceMap.put("ref_ser",refSer);
						gltraceMap.put("ref_id",refId);
						gltraceMap.put("remarks",remarks);
						analysis = invAcctObj.AcctAnalysisType(itemCode, refSer, tranType, "CR", conn);
						if(analysis.trim().length() >0)
						{
							String[] arrStr =analysis.split("@");
							if(arrStr.length>0)
							{
								analysis1  =arrStr[0];
							}
							if(arrStr.length>1)
							{
								analysis2  =arrStr[1];
							}
							if(arrStr.length>2)
							{
								analysis3  =arrStr[2];
							}
						}
						
						gltraceMap.put("analysis1",analysis1);
						gltraceMap.put("analysis2",analysis2);
						gltraceMap.put("analysis3",analysis3);
						errCode = finCommon.glTraceUpdate(gltraceMap, conn);
						
						if(errCode.trim().length()>0)
						{
							return errCode;
						}
						gltraceMap.put("tran_date",tranDate);
						gltraceMap.put("eff_date",effDate);
						gltraceMap.put("fin_entity",finEntity);
						gltraceMap.put("site_code",siteCode);
						gltraceMap.put("sundry_type","O");
						gltraceMap.put("sundry_code",null);
						gltraceMap.put("acct_code",acctCodeDr);
						gltraceMap.put("cctr_code",cctrCodeDr);
						gltraceMap.put("emp_code",null);
						gltraceMap.put("anal_code",null);
						gltraceMap.put("curr_code",currCode);
						gltraceMap.put("exch_rate",1);
						if(amount>0)
						{
							gltraceMap.put("dr_amt",amount);
							gltraceMap.put("cr_amt",0);
						}else
						{
							gltraceMap.put("dr_amt",0);
							gltraceMap.put("cr_amt",0-amount);
							
						}gltraceMap.put("ref_type","D");
						gltraceMap.put("ref_ser",refSer);
						gltraceMap.put("ref_id",refId);
						gltraceMap.put("remarks",remarks);
						analysis = invAcctObj.AcctAnalysisType(itemCode, refSer, tranType, "DR", conn);
						if(analysis.trim().length() >0)
						{
							String[] arrStr =analysis.split("@");
							if(arrStr.length>0)
							{
								analysis1  =arrStr[0];
							}
							if(arrStr.length>1)
							{
								analysis2  =arrStr[1];
							}
							if(arrStr.length>2)
							{
								analysis3  =arrStr[2];
							}
						}
						
						gltraceMap.put("analysis1",analysis1);
						gltraceMap.put("analysis2",analysis2);
						gltraceMap.put("analysis3",analysis3);
						errCode = finCommon.glTraceUpdate(gltraceMap, conn);
						
						if(errCode.trim().length()>0)
						{
							rs.close(); //Pavan [closed expected opening cursor and pstmt].
							rs = null;
							pstmt.close();
							pstmt = null;
							return errCode;
						}
						
						errCode = finCommon.checkGlTranDrCr(refSer, refId, conn);
						System.out.println("checkGlTranDrCr error code>>>>"+errCode);
					}
					
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
			}
			
		} // end try
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			try
			{
				System.out.println("@@@@@@@connection roll back@@@@");
				conn.rollback();
			} catch (SQLException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			throw new ITMException(e);
		}
		
		
		return errCode;
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

}