/********************************************************
	Title : SOrderAmdConf[D16EBAS005]
	Date  : 08/08/16
	Developer: Bhushan Lad
	
 ********************************************************/
package ibase.webitm.ejb.dis.adv;

import ibase.system.config.AppConnectParm;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.E12CreateBatchLoadEjb;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.MasterStatefulLocal;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.dis.PostOrdCreditChk;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.sys.CreateRCPXML;
import ibase.webitm.ejb.sys.UtilMethods;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ejb.Stateless;
import javax.naming.InitialContext;

import org.w3c.dom.Document;



@Stateless
public class SOrderAmdConf extends ActionHandlerEJB implements SOrderAmdConfLocal, SOrderAmdConfRemote
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	FinCommon finCommon = new FinCommon();
	DistCommon distCommon= new DistCommon();
	ValidatorEJB validatorEJB = new ValidatorEJB();
	UtilMethods utilMethods = new UtilMethods();
	String loginSite = null;
	
	public String confirm(String amdNo, String xtraParams, String forcedFlag)throws RemoteException, ITMException
	{
		System.out.println("SOrderAmdConf CONFIRM called>>>>>>>>>>>>>>>>>>>");
		String sql = "",updtsql = "";
		Connection conn = null;
		PreparedStatement pstmt = null,pstmt1=null;
	    String errString = null;
	    String confirm = "",status= "" ,indNo = "",reason = "" ,pordType = "",saleOrd = "",
	    		projCode = "",suppCode= "" , pordSite = "" , custCode ="", itemSer = "", 
	    		totAmt = "", ordAmt= "" , aprvSta = "", tranId = "" , siteCode = "" , lsCrPolicy = "" , editOpn = "",custCodeBil="";
	    String loginEmpCode="", dataStr = "", retString = "";
	    double totordqty = 0.0 ,amount = 0.0 ,hnetAmt = 0.0,hordAmt = 0.0;
		ResultSet rs = null,rs1=null;
	    int cnt = 0 , cntt = 0, checkAmt = 0;
	    Timestamp dueDate=null , sysDate = null;
	    boolean isSaleOrder=false, confFlag = false, isError = false;
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		PostOrdCreditChk postcrdchk= new PostOrdCreditChk();
		ArrayList errStringList= new ArrayList();
		ArrayList retArrayList = new ArrayList();
		
		try 
		{
			ConnDriver connDriver = null;
			connDriver = new ConnDriver();
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			conn.setAutoCommit(false);
			
			loginEmpCode = validatorEJB.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			loginSite = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getDBDateFormat());
			//String currDate = sdf.format(new java.util.Date());
			sysDate = new java.sql.Timestamp(System.currentTimeMillis()) ;
			
			sql = "SELECT SALE_ORDER,CONFIRMED, STATUS FROM SORDAMD WHERE AMD_NO = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, amdNo);
			rs = pstmt.executeQuery();
			if (rs.next()) 
			{
				isSaleOrder=true;
				saleOrd = checkNull(rs.getString("SALE_ORDER"));
				confirm = checkNull(rs.getString("CONFIRMED"));
				status = checkNull(rs.getString("STATUS"));
				
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;
			
			if(!isSaleOrder)
			{
				errString = itmDBAccessLocal.getErrorString("", "VTMCONF20", "","",conn);
				return errString;
			}
			else if("X".equalsIgnoreCase(status) && status.trim().length() > 0 )
			{
				errString = itmDBAccessLocal.getErrorString("", "VTSTATUS3", "","",conn);
				return errString;

			}
			else if ("Y".equalsIgnoreCase(confirm) && confirm.trim().length() > 0)
			{
				errString = itmDBAccessLocal.getErrorString("", "VTDIST26", "","",conn);
				return errString;

			}
			else
			{
				//Start.......
				sql = "SELECT DUE_DATE,CUST_CODE__BIL, CUST_CODE, ITEM_SER,  TOT_AMT, SITE_CODE FROM SORDER WHERE  SALE_ORDER = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, saleOrd);
				rs= pstmt.executeQuery();
				 if(rs.next())
				 {
					 dueDate = rs.getTimestamp("DUE_DATE");
					 custCode = checkNull(rs.getString("CUST_CODE"));
					 custCodeBil = checkNull(rs.getString("CUST_CODE__BIL"));
					 //Added by kunal on 5/11/2018 to add cust_code__bill value
					 itemSer = checkNull(rs.getString("ITEM_SER"));
					 totAmt = checkNull(rs.getString("TOT_AMT"));
					 siteCode = checkNull(rs.getString("SITE_CODE"));
					 
				 }
				rs.close();rs = null;
				pstmt.close();pstmt = null;
				
				sql = "SELECT CASE WHEN ORD_AMT IS NULL THEN 0 ELSE ORD_AMT END AS ORD_AMT FROM	 SORDAMD WHERE AMD_NO = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, amdNo);
				rs= pstmt.executeQuery();
				if(rs.next())
				{
					ordAmt = checkNull(rs.getString("ORD_AMT"));
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
				
				
				sql = "SELECT COUNT(*) FROM BUSINESS_LOGIC_CHECK WHERE  SALE_ORDER = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, saleOrd);
				rs= pstmt.executeQuery();
				if(rs.next())
				{
					cnt = rs.getInt(1);
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
				
				if(cnt > 0)
				{
					sql = "SELECT COUNT(*) FROM BUSINESS_LOGIC_CHECK WHERE  SALE_ORDER = ? AND APRV_STAT = 'M'";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, saleOrd);
					rs= pstmt.executeQuery();
					if(rs.next())
					{
						cntt = rs.getInt(1);
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;
					
					if(cntt > 0)
					{
						try
						{
							sql ="UPDATE BUSINESS_LOGIC_CHECK SET APRV_STAT = 'O' WHERE  SALE_ORDER = ? "; 
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, saleOrd);
							int updtCnt = pstmt.executeUpdate();
							pstmt.close();pstmt = null;
							
						}
						catch (SQLException e)
						{
							e.printStackTrace();
							throw new ITMException(e);
						}
						
					}
					else
					{
						sql = "SELECT APRV_STAT  ,TRAN_ID  FROM   BUSINESS_LOGIC_CHECK WHERE SALE_ORDER = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, saleOrd);
						rs= pstmt.executeQuery();
						while(rs.next())
						{
							aprvSta = checkNull(rs.getString("APRV_STAT"));
							tranId = checkNull(rs.getString("TRAN_ID"));
							
							if("O".equalsIgnoreCase(aprvSta))
							{
								try
								{
									updtsql = "UPDATE BUSINESS_LOGIC_CHECK SET APRV_STAT = 'F' , AMD_NO = ?  WHERE TRAN_ID = ? ";
									pstmt1 = conn.prepareStatement(updtsql);
									pstmt1.setString(1, amdNo);
									pstmt1.setString(2, tranId);
									int updtCnt = pstmt1.executeUpdate();
									pstmt1.close();pstmt1 = null;
								}
								catch (SQLException e)
								{
									e.printStackTrace();
									throw new ITMException(e);
								}
							}
							
						}
						rs.close();rs = null;
						pstmt.close();pstmt = null;
					}
				}
				//errString = gbf_credit_check(ls_cust_code, ls_item_ser, lc_tot_amt_bc, '', ls_sales_order, ld_due_date, ls_site_code, 'S','',ls_check[])
				HashMap CreditCheckMap = new HashMap();
				//Changed By PriyankaC on 04June2019 to set correct custCodeSold and Bill to value [Start].
				/*CreditCheckMap.put("as_cust_code_bil",custCode);
				//added by kunal on 12/11/2018 to add custCodeBil in map for credit check
				CreditCheckMap.put("as_cust_code_sold_to", custCodeBil);*/
				CreditCheckMap.put("as_cust_code_bil",custCodeBil);
				CreditCheckMap.put("as_cust_code_sold_to",custCode);
				//Changed By PriyankaC on 04June2019 to set correct custCodeSold and Bill to value [END].
				CreditCheckMap.put("as_item_ser", itemSer);
				CreditCheckMap.put("ad_net_amt", totAmt);
				CreditCheckMap.put("as_sorder", saleOrd);
				CreditCheckMap.put("adt_tran_date", dueDate);
				CreditCheckMap.put("as_site_code", siteCode);
				CreditCheckMap.put("as_apply_time", "S");
				CreditCheckMap.put("as_despid", "");
				System.out.println("CreditCheckMap:::["+CreditCheckMap+"]");
				
				errStringList = postcrdchk.CreditCheck(CreditCheckMap, conn);
				if(errStringList.size() > 0)
				{
					conn.rollback();
					errString = itmDBAccessLocal.getErrorString("", "VTWBLGCCHK", "","",conn);
					System.out.println("@@@@@@@@@@ CreditCheck errString["+errString+"]");
					return errString;
					
				}
				else
				{
					// nvo_dist_sales.gbf_credit_check_update(ls_sales_order,ls_cr_policy,lc_tot_amt_bc,'S',lc_check_amt,'C',ls_cr_status)
					double adNetAmt = Double.parseDouble(totAmt);
					retArrayList =  postcrdchk.credit_check_update(saleOrd,lsCrPolicy,adNetAmt,"S",checkAmt,"C","",conn);
				
				}
			 
				
				if( errString == null || errString.trim().length() == 0 )
				{
					//nvo_dist_ord.gbf_sordamd_confirm(ls_amd_no, ls_sales_order, '*')
					errString =  sordamdConfirm(amdNo, saleOrd, "",xtraParams,conn);
					System.out.println("@@@@@ sordamdConfirm errString::::["+errString+"]");
					if(errString.indexOf("Success") > -1 || errString.trim().length() == 0 )
					{
						System.out.println("Sales Order Amd is Confirmed..........");
						errString = itmDBAccessLocal.getErrorString("", "VTCNFSUCC", "","",conn);
						confFlag = true;
					}
					else
					{	
						System.out.println("Sales Order Amd is not Confirmed..........");
						return errString;
					}
				}
				System.out.println("confFlag:: ["+confFlag+"]");
				if(confFlag)
				{
					//System.out.println("confFlag:: ["+confFlag+"]");
					updtsql = "UPDATE SORDAMD	SET CONFIRMED = 'Y' , CONF_DATE = ? , EMP_CODE__APRV = ?  " +
							"WHERE AMD_NO = ?	AND (CASE WHEN CONFIRMED IS NULL THEN 'N' ELSE CONFIRMED END) <> 'Y' ";
					
					pstmt = conn.prepareStatement(updtsql);
					pstmt.setTimestamp(1, sysDate);
					pstmt.setString(2, loginEmpCode);
					pstmt.setString(3, amdNo);
					
					int updtCnt = pstmt.executeUpdate();
					System.out.println("Update SORDAMD:::::::["+updtCnt+"]");
					pstmt.close();pstmt = null;
				}
				
				sql = "SELECT EDI_OPTION FROM TRANSETUP WHERE TRAN_WINDOW = 'w_sordamd'";
				
				pstmt = conn.prepareStatement(sql);
				rs= pstmt.executeQuery();
				if(rs.next())
				{
					editOpn = rs.getString("EDI_OPTION");
				}
				System.out.println("editOpn ::::["+editOpn+"]");
				rs.close();rs = null;
				pstmt.close();pstmt = null;
					 
				int ediOpt = checkInt(editOpn);
				if(ediOpt > 0)
				{
					CreateRCPXML createRCPXML = new CreateRCPXML("w_sordamd", "tran_id");
					dataStr = createRCPXML.getTranXML(amdNo, conn);
					System.out.println("dataStr =[ " + dataStr + "]");
					Document ediDataDom = genericUtility.parseString(dataStr);
	
					E12CreateBatchLoadEjb e12CreateBatchLoad = new E12CreateBatchLoadEjb();
					retString = e12CreateBatchLoad.createBatchLoad(ediDataDom, "w_sordamd", ""+ediOpt , xtraParams, conn);
					createRCPXML = null;
					e12CreateBatchLoad = null;
	
					if (retString != null && "SUCCESS".equals(retString))
					{
						System.out.println("retString from batchload = [" + retString + "]");
					}
				}
			}
			
		}
		catch (Exception e) 
		{
			if(conn!=null)
			{
				try 
				{	
					System.out.println("@@@@  Transaction rollback... ");
					isError = true;
					conn.rollback();
					
				} 
				catch (SQLException ex) 
				{
					e.printStackTrace();
					throw new ITMException(e);
				}
			}
			System.out.println("Exception in SOrderAmdConf confirm()::::["+e.getMessage()+"]");
			e.printStackTrace();
			throw new ITMException(e);
		} 
		finally
		{
			try
			{
				System.out.println(">>>>>In SOrderAmdConf finaly errString::::::["+errString+"]");
				if(confFlag && !isError)
				{
					conn.commit();
					System.out.println("@@@@ SOrderAmdConf Transaction commit... ");
					conn.close();
					conn = null;
					
				}
				else
				{
					conn.rollback();
					System.out.println("@@@@ SOrderAmdConf Transaction rollback... ");
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
		
		return errString;
	}
	//Pavan R on 24sept18 start [to confirm sorderAmd on business override logic]
	public String confirmSorderAmd(String saleOrderAmd, String saleOrder,String temp, String xtraParams, Connection conn) throws ITMException
	{
		System.out.println("-------------inside sorderAmd confirmSorderAmd........................");				
		String updtsql = "";
		String dataStr = "";
		String editOpn = "";
		String errString = "";
		String retString = "";
		String loginEmpCode="";		
		ResultSet rs = null;
		boolean confFlag = false;
		Timestamp sysDate = null;
		PreparedStatement pstmt = null;		
		ITMDBAccessEJB itmDBAccess = null;
		try 
		{
			itmDBAccess = new ITMDBAccessEJB();
			sysDate = new java.sql.Timestamp(System.currentTimeMillis()) ;
			loginEmpCode = validatorEJB.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			
			errString = sordamdConfirm(saleOrderAmd,saleOrder, "", xtraParams, conn);
			System.out.println("@@@@@ sordamdConfirm errString::::["+errString+"]");
			if(errString.indexOf("Success") > -1 || errString.trim().length() == 0 )
			{				
				errString = itmDBAccess.getErrorString("", "VTCNFSUCC", "","",conn);
				confFlag = true;
			}
			else
			{	
				System.out.println("Sales Order Amd is not Confirmed..........");
				return errString;
			}			
			if(confFlag)
			{
				updtsql = "UPDATE SORDAMD	SET CONFIRMED = 'Y' , CONF_DATE = ? , EMP_CODE__APRV = ?  " +
						"WHERE AMD_NO = ?	AND (CASE WHEN CONFIRMED IS NULL THEN 'N' ELSE CONFIRMED END) <> 'Y' ";
				
				pstmt = conn.prepareStatement(updtsql);
				pstmt.setTimestamp(1, sysDate);
				pstmt.setString(2, loginEmpCode);
				pstmt.setString(3, saleOrderAmd);
				
				int updtCnt = pstmt.executeUpdate();
				System.out.println("Update SORDAMD:::::::["+updtCnt+"]");
				pstmt.close();pstmt = null;
			}
			
			updtsql = "SELECT EDI_OPTION FROM TRANSETUP WHERE TRAN_WINDOW = 'w_sordamd'";
			
			pstmt = conn.prepareStatement(updtsql);
			rs= pstmt.executeQuery();
			if(rs.next())
			{
				editOpn = rs.getString("EDI_OPTION");
			}
			System.out.println("editOpn ::::["+editOpn+"]");
			rs.close();rs = null;
			pstmt.close();pstmt = null;
				 
			int ediOpt = checkInt(editOpn);
			if(ediOpt > 0)
			{
				CreateRCPXML createRCPXML = new CreateRCPXML("w_sordamd", "tran_id");
				dataStr = createRCPXML.getTranXML(saleOrderAmd, conn);
				System.out.println("dataStr =[ " + dataStr + "]");
				Document ediDataDom = genericUtility.parseString(dataStr);

				E12CreateBatchLoadEjb e12CreateBatchLoad = new E12CreateBatchLoadEjb();
				retString = e12CreateBatchLoad.createBatchLoad(ediDataDom, "w_sordamd", ""+ediOpt , xtraParams, conn);
				createRCPXML = null;
				e12CreateBatchLoad = null;

				if (retString != null && "SUCCESS".equals(retString))
				{
					System.out.println("retString from batchload = [" + retString + "]");
				}
			}
			
		}catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally{
			try
			{
				//System.out.println(">>>>>In SOrderAmdConf confirmSorderAmd() finally errString::::::["+errString+"]");
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
			catch (Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return errString;
	}
	//Pavan R on 24sept18 end
	public String sordamdConfirm(String amdNo,String saleOrd,String temp,String xtraParams ,Connection conn) throws ITMException 
	{
		System.out.println("In side sordamdConfirm()......");
		String sql = "",errorCode = "", updtSql="", updtSql1="", lnSql = "", insrtSql = "", detSql = "";
		PreparedStatement pstmt = null,pstln=null;
	    String errString ="";
		ResultSet rs = null, rsln = null;
		int cnt=0,updCnt=0, lineCnt=0, plCnt = 0 , llCnt = 0 ,lcdetCnt = 0, ll_Cnt = 0, min_life = 0, min_lifePer = 0, max_life = 0 , shelf_life = 0, qtyDesp = 0 ,despQty = 0;
		String  chgUsr = "",chgTrm ="" ;
		String custCode="",siteCode="",channelPartner="",disLink="",ediOption="",custCodeDlvAmd ="",custCodeBil = "",custPord = "", commPerc = "",partQty = "",
				taxClass = "",taxEnv = "",taxChap="", crTerm="", dlvadd1="", dlvadd2="", dlvadd3 ="" , dlvCity = "",cntCodeDlv ="", dlvPin = "",stanCode = "",
				tranCode= "", advPerc ="",disRoute = "",currCodeFrt = "", exchRateFrt = "", currCodeIns = "", exchRateIns = "",transMode ="",  frtTerm ="",  
				dlvTerm = "",priceList ="", priceListDisc="", udfStr1 = "", remarks = "" , remarks2 = "" , remarks3 = "" , stanCodeInit ="",acctCodeSal ="",
				cctrCodeSal = "", custCodeDlvSord ="", dlvTo ="" , custName = "" , consumeFc = "", itemSer = "", prcListDisc = "", ordType = "", prcList = "", 
				taxClassdt = "" , taxChapdt = "", taxEnvdt = "", pacKIns = "", packCode= "" , lineNoSo = "", unit ="", unitStd = "", itemCode = "", itemFlg = "",
				lineNo = "", itemCodeOrd = "", nature="", lsCnt = "",  prcListType = "" , rfNo = "", prcListParnt = "", itemDesc = "", schDesc = "" , 
				quotNo = "", itemDesr = "" , itemCodeOrdt ="" ,custSpNo ="" , custSpNoO = "", untRate = "";
		
		String winName="w_sorder",dataStr="",retString="";
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		double ordAmt=0.0,taxAmt=0.0,totAmt=0.0,frtAmt=0.0,insAmt=0.0,ordAmtDet=0.0,taxAmtDet=0.0,spComm2=0.0,spComm3=0.0;
		double totalComm=0.0, totsAmt = 0.0, qty = 0.0, qtyO = 0.0, rate = 0.0, disCnt = 0.0, taxAmtdt = 0.0 , netAmt = 0.0, qtyStd = 0.0, rateStd = 0.0, 
				noArt = 0.0 , rateClg = 0.0, convQtyStd = 0.0,  convRtStd = 0.0, minShLif = 0.0, minShLifO = 0.0, 
				//custSpNo = 0.0, custSpNoO = 0.0, 
				maxShLifO = 0.0, maxShLif = 0.0 ,maxRate = 0.0, ordValue = 0.0;
		String loginEmpCode="", amdDateStr = "";
		Timestamp sysDate = null, dueDate = null, dspDate = null;
		Timestamp amdDate =null, taxDate = null, pordDate = null, plDate = null, udfDate1 =null;
		HashMap commissionMap = null;
		ArrayList sordLineNo = new ArrayList();
		DistCommon distCommom = new DistCommon();
		double billBackAmt=0,offInvAmt=0,netTotAmtDet=0,netTotAmtHdr=0,ordBillBackAmt=0,ordOffInvAmt=0,lineBillBackAmt=0,lineOffInvAmt=0; //// added by nandkumar gadkari on 09/07/19
		//start implement
		try 
        {
			System.out.println("amdNo:::["+amdNo+"] saleOrd:::["+saleOrd+"]");
			
			chgUsr = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			chgTrm = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "chgTerm");
			
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getDBDateFormat());
			String currDate = sdf.format(new java.util.Date());
			sysDate = new java.sql.Timestamp(System.currentTimeMillis()) ;
			System.out.println("sysDate:::["+sysDate+"]");
			
			sql = "SELECT	AMD_DATE, CUST_CODE__DLV, CUST_CODE__BIL, CUST_PORD,COMM_PERC, TAX_CLASS, TAX_DATE, TAX_CHAP, TAX_ENV," +
					"CR_TERM, DLV_ADD1, DLV_ADD2, DLV_ADD3, DLV_CITY,COUNT_CODE__DLV, DLV_PIN, STAN_CODE, PART_QTY,TRAN_CODE, ORD_AMT, " +
					"TAX_AMT, TOT_AMT, PORD_DATE,ADV_PERC, DIST_ROUTE, CURR_CODE__FRT, EXCH_RATE__FRT,FRT_AMT, CURR_CODE__INS, EXCH_RATE__INS, " +
					"INS_AMT,TRANS_MODE, FRT_TERM, DLV_TERM,PRICE_LIST, PL_DATE, PRICE_LIST__DISC, UDF__STR1, REMARKS, REMARKS2, REMARKS3," +
					"STAN_CODE__INIT, 	UDF__DATE1, ACCT_CODE__SAL , CCTR_CODE__SAL,SALE_ORDER " +
					"FROM	SORDAMD  WHERE	AMD_NO =  ?  " ;//+ 09-dec-2020 manoharan terminated here  only primary key is required
					//"AND SALE_ORDER = ? "; //+ 09-dec-2020 manoharan commented here  only primary key is required
			
			pstmt= conn.prepareStatement(sql);
			pstmt.setString(1, amdNo);
			//pstmt.setString(2, saleOrd); //+ 09-dec-2020 manoharan commented here  only primary key is required
			rs= pstmt.executeQuery();
			//while(rs.next()) //+ 09-dec-2020 manoharan commented here  only primary key and one row
			if(rs.next())//+ 09-dec-2020 manoharan added here  only primary key and one row
			{
				amdDate = rs.getTimestamp("AMD_DATE");
				amdDateStr = rs.getString("AMD_DATE");
				custCodeDlvAmd = checkNull(rs.getString("CUST_CODE__DLV"));
				custCodeBil = checkNull(rs.getString("CUST_CODE__BIL"));
				custPord = checkNull(rs.getString("CUST_PORD"));
				commPerc = checkNull(rs.getString("COMM_PERC"));
				taxClass = checkNull(rs.getString("TAX_CLASS"));
				taxDate = rs.getTimestamp("TAX_DATE");
				taxChap = checkNull(rs.getString("TAX_CHAP"));
				taxEnv = checkNull(rs.getString("TAX_ENV"));
				crTerm = checkNull(rs.getString("CR_TERM"));
				dlvadd1 = checkNull(rs.getString("DLV_ADD1"));
				dlvadd2 = checkNull(rs.getString("DLV_ADD2"));
				dlvadd3 = checkNull(rs.getString("DLV_ADD3"));
				dlvCity = checkNull(rs.getString("DLV_CITY"));
				cntCodeDlv = checkNull(rs.getString("COUNT_CODE__DLV"));
				dlvPin = checkNull(rs.getString("DLV_PIN"));
				stanCode = checkNull(rs.getString("STAN_CODE"));
				partQty = checkNull(rs.getString("PART_QTY"));
				tranCode = checkNull(rs.getString("TRAN_CODE"));
				ordAmt = rs.getDouble("ORD_AMT");
				taxAmt = rs.getDouble("TAX_AMT");
				totAmt = rs.getDouble("TOT_AMT");
				pordDate = rs.getTimestamp("PORD_DATE");
				advPerc = checkNull(rs.getString("ADV_PERC"));
				disRoute = checkNull(rs.getString("DIST_ROUTE"));
				currCodeFrt = checkNull(rs.getString("CURR_CODE__FRT"));
				exchRateFrt = checkNull(rs.getString("EXCH_RATE__FRT"));
				frtAmt = rs.getDouble("FRT_AMT");
				currCodeIns = checkNull(rs.getString("CURR_CODE__INS"));
				exchRateIns = checkNull(rs.getString("EXCH_RATE__INS"));
				insAmt = rs.getDouble("INS_AMT");
				transMode = checkNull(rs.getString("TRANS_MODE"));
				frtTerm = checkNull(rs.getString("FRT_TERM"));
				dlvTerm = checkNull(rs.getString("DLV_TERM"));
				priceList = checkNull(rs.getString("PRICE_LIST"));
				plDate = rs.getTimestamp("PL_DATE");
				priceListDisc = checkNull(rs.getString("PRICE_LIST__DISC"));
				udfStr1 = checkNull(rs.getString("UDF__STR1"));
				remarks = checkNull(rs.getString("REMARKS"));
				remarks2 = checkNull(rs.getString("REMARKS2"));
				remarks3 = checkNull(rs.getString("REMARKS3"));
				stanCodeInit = checkNull(rs.getString("STAN_CODE__INIT"));
				udfDate1 = rs.getTimestamp("UDF__DATE1");
				acctCodeSal = checkNull(rs.getString("ACCT_CODE__SAL"));
				cctrCodeSal = checkNull(rs.getString("CCTR_CODE__SAL"));
				
				saleOrd = checkNull(rs.getString("SALE_ORDER")); // 09-dec-2020 manoharan added here to get from current amendment
				
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;
			
			//amdDateStr = sdf.format(amdDate);
			System.out.println("amdDateStr:::["+amdDateStr+"]");
			amdDateStr = genericUtility.getValidDateString(amdDateStr, genericUtility.getDBDateFormat(),genericUtility.getApplDateFormat());
			System.out.println("amdDateStr:::==["+amdDateStr+"]");
			
			sql= "SELECT	SUM(CASE WHEN NET_AMT_O IS NULL THEN 0 ELSE NET_AMT_O END) -  SUM(CASE WHEN TAX_AMT_O IS NULL THEN 0 ELSE TAX_AMT_O END) AS ORD_AMT_DET, " +
					" SUM(CASE WHEN TAX_AMT_O IS NULL THEN 0 ELSE TAX_AMT_O END) AS TAX_AMT_DET " +
					" FROM	SORDAMDDET	WHERE	AMD_NO =  ? ";//	GROUP BY AMD_NO "; // 09-dec-20 manoharan onle primary key so no grouping
					//"FROM	SORDAMDDET	WHERE	AMD_NO =  ? 	GROUP BY AMD_NO ";
			
			pstmt= conn.prepareStatement(sql);
			pstmt.setString(1, amdNo);
			rs= pstmt.executeQuery();
			if(rs.next())
			{
				ordAmtDet = rs.getDouble("ORD_AMT_DET");
				taxAmtDet = rs.getDouble("TAX_AMT_DET");
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;
			
			sql = "SELECT CUST_CODE__DLV , DLV_TO, DUE_DATE, CONSUME_FC, CUST_CODE, ITEM_SER, PRICE_LIST__DISC,  " +
					"ORDER_TYPE, PRICE_LIST, TOT_AMT, "
					+ "ORD_BILLBACK_AMT,ORD_OFFINV_AMT, LINE_BILLBACK_AMT,LINE_OFFINV_AMT  "// added by nandkumar gadkari on 09/07/19
					+ "FROM SORDER WHERE  SALE_ORDER = ? ";
			pstmt= conn.prepareStatement(sql);
			pstmt.setString(1, saleOrd);
			rs= pstmt.executeQuery();
			if(rs.next())
			{
				custCodeDlvSord = checkNull(rs.getString("CUST_CODE__DLV"));
				dlvTo = checkNull(rs.getString("DLV_TO"));
				
				dueDate = rs.getTimestamp("DUE_DATE");
				consumeFc = checkNull(rs.getString("CONSUME_FC"));
				custCode = checkNull(rs.getString("CUST_CODE"));
				itemSer = checkNull(rs.getString("ITEM_SER"));
				prcListDisc = checkNull(rs.getString("PRICE_LIST__DISC"));
				ordType = checkNull(rs.getString("ORDER_TYPE"));
				prcList = checkNull(rs.getString("PRICE_LIST"));
				totsAmt = rs.getDouble("TOT_AMT");
				//// added by nandkumar gadkari on 09/07/19-------start-----------
				ordBillBackAmt=rs.getDouble("ORD_BILLBACK_AMT");
				ordOffInvAmt=rs.getDouble("ORD_OFFINV_AMT");
				lineBillBackAmt=rs.getDouble("LINE_BILLBACK_AMT");
				lineOffInvAmt=rs.getDouble("LINE_OFFINV_AMT");
				netTotAmtHdr=totAmt-ordBillBackAmt-ordOffInvAmt-lineBillBackAmt-lineOffInvAmt;
				//// added by nandkumar gadkari on 09/07/19-----------end--------------
				
				
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;
			
			if(!custCodeDlvSord.equalsIgnoreCase(custCodeDlvAmd))
			{
				sql = "SELECT CUST_NAME  FROM CUSTOMER WHERE CUST_CODE = ? ";
				pstmt= conn.prepareStatement(sql);
				pstmt.setString(1, custCodeDlvAmd);
				rs= pstmt.executeQuery();
				if(rs.next())
				{
					dlvTo = checkNull(rs.getString("CUST_NAME"));
				}
				rs.close();rs = null;
				pstmt.close();pstmt = null;
			}
			
			updtSql = "UPDATE SORDER  SET CUST_CODE__DLV = ? ,CUST_CODE__BIL = ? , CUST_PORD = ? , " +
					"COMM_PERC = ? ,TAX_CLASS = ?,TAX_CHAP = ?,	TAX_ENV = ?, " +
					"CR_TERM = ?, DLV_ADD1 = ?,	DLV_ADD2 = ?,	DLV_ADD3 = ?, " +
					"DLV_CITY = ?, 	DLV_PIN	= ?,COUNT_CODE__DLV = ?, STAN_CODE = ?,	" +
					"PART_QTY = ?,	TRAN_CODE = ?, ORD_AMT = ORD_AMT -  ? + ? ,	" +
					"TAX_AMT = TAX_AMT - ?  + ? , TOT_AMT = TOT_AMT - ( ? + ? ) + ?, " +
					"PORD_DATE = ?, ADV_PERC = ?, DIST_ROUTE = ?, AMD_NO__LAST = ?,	" +
					"CURR_CODE__FRT = ?, EXCH_RATE__FRT = ?, FRT_AMT = ?, CURR_CODE__INS = ?, " +
					"EXCH_RATE__INS	= ?, INS_AMT = ?, TRANS_MODE = ?, FRT_TERM = ?,	" +
					"DLV_TERM = ?, UDF__STR1 = ?, REMARKS = ?,REMARKS2 = ?, " +
					"REMARKS3 = ?,STAN_CODE__INIT= ?,DLV_TO	= ?, UDF__DATE1	= ?	, " +
					"ACCT_CODE__SAL = ? , CCTR_CODE__SAL = ?  "
					+ ", NET_TOT_AMT= ? " + // added by nandkumar gadkari on 09/07/19
					"WHERE SALE_ORDER = ?  ";
			
			
			pstmt = conn.prepareStatement(updtSql);
			pstmt.setString(1, custCodeDlvAmd);
			pstmt.setString(2, custCodeBil);
			pstmt.setString(3, custPord);
			pstmt.setString(4, commPerc);
			pstmt.setString(5, taxClass);
			pstmt.setString(6, taxChap);
			pstmt.setString(7, taxEnv);
			pstmt.setString(8, crTerm);
			pstmt.setString(9, dlvadd1);
			pstmt.setString(10, dlvadd2);
			pstmt.setString(11, dlvadd3);
			pstmt.setString(12, dlvCity);
			pstmt.setString(13, dlvPin);
			pstmt.setString(14, cntCodeDlv);
			pstmt.setString(15, stanCode);
			pstmt.setString(16, partQty);
			pstmt.setString(17, tranCode);
			pstmt.setDouble(18, ordAmtDet);
			pstmt.setDouble(19, ordAmt);
			pstmt.setDouble(20, taxAmtDet);
			pstmt.setDouble(21, taxAmt);
			pstmt.setDouble(22, ordAmtDet);
			pstmt.setDouble(23, taxAmtDet);
			pstmt.setDouble(24, totAmt);
			pstmt.setTimestamp(25, pordDate);
			pstmt.setString(26, advPerc);
			pstmt.setString(27, disRoute);
			pstmt.setString(28, amdNo);
			pstmt.setString(29, currCodeFrt);
			pstmt.setString(30, exchRateFrt);
			pstmt.setDouble(31, frtAmt);
			pstmt.setString(32, currCodeIns);
			pstmt.setString(33, exchRateIns);
			pstmt.setDouble(34, insAmt);
			pstmt.setString(35, transMode);
			pstmt.setString(36, frtTerm);
			pstmt.setString(37, dlvTerm);
			pstmt.setString(38, udfStr1);
			pstmt.setString(39, remarks);
			pstmt.setString(40, remarks2);
			pstmt.setString(41, remarks3);
			pstmt.setString(42, stanCodeInit);
			pstmt.setString(43, dlvTo);
			pstmt.setTimestamp(44, udfDate1);
			pstmt.setString(45, acctCodeSal);
			pstmt.setString(46, cctrCodeSal);
			pstmt.setDouble(47, netTotAmtHdr);// added by nandkumar gadkari on 09/07/19
			pstmt.setString(48, saleOrd);
			
			int updtCnt = pstmt.executeUpdate();
			System.out.println("updtCnt SORDER :::["+updtCnt+"]");
			pstmt.close();pstmt = null;
			
			
			System.out.println("SORDAMDDET Detail parts start...........");
			
			sql = "SELECT SITE_CODE, QUANTITY, QUANTITY_O, RATE, DISCOUNT,  TAX_AMT, TAX_CLASS,TAX_CHAP, TAX_ENV, NET_AMT,  QUANTITY__STDUOM, " +
					"RATE__STDUOM, PACK_INSTR, NO_ART, PACK_CODE, DSP_DATE, LINE_NO__SORD, RATE__CLG, UNIT, CONV__QTY_STDUOM, UNIT__RATE, " +
					"CONV__RTUOM_STDUOM, UNIT__STD, ITEM_CODE, ITEM_FLG, LINE_NO, ITEM_CODE__ORD , MIN_SHELF_LIFE,MIN_SHELF_LIFE_O , " +
					"CUST_SPEC__NO_O, CUST_SPEC__NO,NATURE , MAX_SHELF_LIFE,MAX_SHELF_LIFE_O  " +
					"FROM SORDAMDDET " +
					"WHERE AMD_NO = ? ORDER BY LINE_NO ";
			
			pstmt= conn.prepareStatement(sql);
			pstmt.setString(1, amdNo);
			rs= pstmt.executeQuery();
			
			while(rs.next())
			{
				siteCode = checkNull(rs.getString("SITE_CODE"));
				qty = rs.getDouble("QUANTITY");
				qtyO = rs.getDouble("QUANTITY_O");
				rate = rs.getDouble("RATE");
				disCnt = rs.getDouble("DISCOUNT");
				taxAmtdt = rs.getDouble("TAX_AMT");
				taxClassdt = checkNull(rs.getString("TAX_CLASS"));
				taxChapdt = checkNull(rs.getString("TAX_CHAP"));
				taxEnvdt = checkNull(rs.getString("TAX_ENV"));
				netAmt = rs.getDouble("NET_AMT");
				qtyStd = rs.getDouble("QUANTITY__STDUOM");
				rateStd = rs.getDouble("RATE__STDUOM");
				pacKIns = checkNull(rs.getString("PACK_INSTR"));
				noArt = rs.getDouble("NO_ART");
				packCode = checkNull(rs.getString("PACK_CODE"));
				dspDate = rs.getTimestamp("DSP_DATE");
				lineNoSo = rs.getString("LINE_NO__SORD");
				rateClg = rs.getDouble("RATE__CLG");
				unit = checkNull(rs.getString("UNIT"));
				convQtyStd = rs.getDouble("CONV__QTY_STDUOM");
				untRate = checkNull(rs.getString("UNIT__RATE"));
				convRtStd = rs.getDouble("CONV__RTUOM_STDUOM");
				unitStd = checkNull(rs.getString("UNIT__STD"));
				itemCode = checkNull(rs.getString("ITEM_CODE"));
				itemFlg = checkNull(rs.getString("ITEM_FLG"));
				lineNo = rs.getString("LINE_NO");
				itemCodeOrd = checkNull(rs.getString("ITEM_CODE__ORD"));
				minShLif = rs.getDouble("MIN_SHELF_LIFE");
				minShLifO = rs.getDouble("MIN_SHELF_LIFE_O");
				custSpNoO = checkNull(rs.getString("CUST_SPEC__NO_O"));
				custSpNo = checkNull(rs.getString("CUST_SPEC__NO"));
				nature = checkNull(rs.getString("NATURE"));
				maxShLif = rs.getDouble("MAX_SHELF_LIFE");
				maxShLifO = rs.getDouble("MAX_SHELF_LIFE_O");
				llCnt = 0;//added by Pavan Rane20sep19[to reset value on amend of old sord line with new line].
				if(lineNoSo == null || lineNoSo.trim().length() == 0 )
				{
					//lnSql = "SELECT MAX(LINE_NO) AS LS_CNT FROM SORDDET WHERE SALE_ORDER = ? ";
					lnSql = "SELECT TRIM(MAX(CAST(LINE_NO as number))) AS LS_CNT FROM SORDDET WHERE SALE_ORDER = ? ";//Changed by Jaffar S. for getting exact max of line no
					pstln= conn.prepareStatement(lnSql);
					pstln.setString(1, saleOrd);
					rsln= pstln.executeQuery();
					if(rsln.next())
					{
						lsCnt = rsln.getString("LS_CNT");
					}
					rsln.close();rsln = null;
					pstln.close();pstln = null;
					
					//cnt = long(trim(ls_cnt)) + 1
					lineCnt = Integer.parseInt(lsCnt) + 1;
					lineNoSo = Integer.toString(lineCnt);
					System.out.println("lineNoSo:::>>>["+lineNoSo+"]");
					updtSql = "UPDATE SORDAMDDET SET LINE_NO__SORD = ?	" +
							" WHERE	AMD_NO = ? AND LINE_NO	= ? ";
							//"WHERE	AMD_NO = ? AND SALE_ORDER =  ? AND LINE_NO	= ? ";
					System.out.println("##lineNoSo after["+lineNoSo+"]");
					lineNoSo = getLineNewNo(lineNoSo);
					System.out.println("##lineNoSo before["+lineNoSo+"]");
					pstln= conn.prepareStatement(updtSql);
					pstln.setString(1, lineNoSo);
					pstln.setString(2, amdNo);
					pstln.setString(3, lineNo);
					//pstln.setString(3, saleOrd);
					//pstln.setString(4, lineNo);
					
					int uptln= pstln.executeUpdate();
					System.out.println("uptln::::::["+uptln+"]");
					pstln.close();pstln = null;
				}
				else
				{
					System.out.println("lineNoSo:::==["+lineNoSo+"]");
					lineNoSo = getLineNewNo(lineNoSo);
					System.out.println("lineNoSo:::=>>["+lineNoSo+"]");
					
					sql = "SELECT COUNT(1) AS LLCNT	 FROM SORDDET	" +
							"WHERE SALE_ORDER = ? AND LINE_NO = ?";
					
					pstln= conn.prepareStatement(sql);
					pstln.setString(1, saleOrd);
					pstln.setString(2, lineNoSo);
					rsln= pstln.executeQuery();
					if(rsln.next())
					{
						llCnt = rsln.getInt("LLCNT");
					}
					System.out.println("SORDDET-llCnt:::==["+llCnt+"]");
					rsln.close();rsln = null;
					pstln.close();pstln = null;
					
				}
				
				if(prcList != null && prcList.trim().length() > 0)
				{
					System.out.println("prcList::::::["+prcList+"]");
					System.out.println("itemCodeOrd::::::["+itemCodeOrd+"]");
					//ls_list_type = gbf_get_pricelist_type(ls_price_list)
					prcListType = distCommom.getPriceListType(prcList, conn);
					
					//lc_max_rate = lnvo_discount.gbf_pick_rate_refno_wise(ls_price_list,ldt_amd_date,ls_item_code__ord,ls_ref_no,'L',lc_quantity)
					// ADDED BY NANDKUMAR GADKARI ON 09/07/19------------------------------------START-----------------------------------------
					sql = "select count(1)  as llPlcount from pricelist where price_list=?"
							+ " and item_code= ? and unit= ? and list_type=? and eff_from<=? and valid_upto  >=? and min_qty<=? and max_qty>= ?"
							+ " and (ref_no is not null)";
					pstln = conn.prepareStatement(sql);
					pstln.setString(1, prcList);
					pstln.setString(2, itemCodeOrd);
					pstln.setString(3, unit);
					pstln.setString(4, prcListType);
					pstln.setTimestamp(5, amdDate);
					pstln.setTimestamp(6, amdDate);
					pstln.setDouble(7, qty);
					pstln.setDouble(8, qty);
					rsln = pstln.executeQuery();
					if (rsln.next()) {
						cnt = rsln.getInt("llPlcount");
					}
					rsln.close();
					rsln = null;
					pstln.close();
					pstln = null;

					if (cnt >= 1) {
						sql = "select max(ref_no)from pricelist where price_list  =? and item_code= ? and unit=? and list_type= ?"
								+ " and eff_from<=? and valid_upto>=? and min_qty<=? and max_qty>=? and (ref_no is not null)";
						pstln = conn.prepareStatement(sql);
						pstln.setString(1, prcList);
						pstln.setString(2, itemCodeOrd);
						pstln.setString(3, unit);
						pstln.setString(4, prcListType);
						pstln.setTimestamp(5, amdDate);
						pstln.setTimestamp(6, amdDate);
						pstln.setDouble(7, qty);
						pstln.setDouble(8, qty);
						rsln = pstln.executeQuery();
						if (rsln.next()) {
							rfNo = rsln.getString(1);
						}
						rsln.close();
						rsln = null;
						pstln.close();
						pstln = null;

						maxRate = distCommon.pickRateRefnoWise(prcList, amdDateStr, itemCodeOrd, rfNo, prcListType, qty,conn);
					}
					if (maxRate <= 0) {
						maxRate = distCommom.pickRateRefnoWise( prcList, amdDateStr, itemCodeOrd, rfNo,"L", qty, conn);

					}
				
				
				if (maxRate < 0) {
					maxRate = 0;
				}
				// ADDED BY NANDKUMAR GADKARI ON 09/07/19------------------------------------END-----------------------------------------
					System.out.println("maxRate::::::["+maxRate+"]");
					
					ordValue = qty * maxRate;
					System.out.println("ordValue:::["+ordValue+"]");
				}
				else
				{
					//lc_ord_value = lc_quantity * lc_rate
					ordValue = qty * rate;
					System.out.println("ordValue::::::=["+ordValue+"]");
				}
				
				
				sql = "SELECT DESCR AS ITEM_DESC FROM ITEM WHERE ITEM_CODE = ?";
				
				pstln= conn.prepareStatement(sql);
				pstln.setString(1, itemCode);
				rsln= pstln.executeQuery();
				if(rsln.next())
				{
					itemDesc = checkNull(rsln.getString("ITEM_DESC"));
				}
				System.out.println("itemCode::::::=["+itemCode+"]");
				System.out.println("itemDesc::::::=["+itemDesc+"]");
				rsln.close();rsln = null;
				pstln.close();pstln = null;
				
				if("B".equalsIgnoreCase(itemFlg))
				{
					sql = "SELECT  DESCR AS SCHEME_DESC FROM BOM WHERE BOM_CODE = ? ";
					pstln= conn.prepareStatement(sql);
					pstln.setString(1, itemCode);
					rsln= pstln.executeQuery();
					if(rsln.next())
					{
						schDesc = checkNull(rsln.getString("SCHEME_DESC"));
					}
					rsln.close();rsln = null;
					pstln.close();pstln = null;
					
					itemDesc = itemDesc + " " + schDesc ;
					System.out.println("itemDesc::::::=["+itemDesc+"]");
				}
				
				sql ="SELECT QUOT_NO AS MQUOTNO FROM SORDER WHERE SALE_ORDER = ? ";
				
				pstln= conn.prepareStatement(sql);
				pstln.setString(1, saleOrd);
				rsln= pstln.executeQuery();
				if(rsln.next())
				{
					quotNo = checkNull(rsln.getString("MQUOTNO"));
				}
				System.out.println("quotNo::::::=["+quotNo+"]");
				rsln.close();rsln = null;
				pstln.close();pstln = null;
				System.out.println("llCnt::::::===["+llCnt+"]");
				if(llCnt > 0)
				{
					
					sql = "SELECT ITEM_CODE__ORD,ITEM_DESCR,billback_amt,offinv_amt FROM SORDDET WHERE SALE_ORDER = ? AND LINE_NO = ? ";//billback_amt,offinv_amt added by nandkumar gadkari on 09/07/19
					
					pstln= conn.prepareStatement(sql);
					pstln.setString(1, saleOrd);
					pstln.setString(2, lineNoSo);
					rsln= pstln.executeQuery();
					if(rsln.next())
					{
						itemCodeOrdt = checkNull(rsln.getString("ITEM_CODE__ORD"));
						itemDesr = checkNull(rsln.getString("ITEM_DESCR"));
						billBackAmt=rsln.getDouble(3); 
						offInvAmt=rsln.getDouble(4);
					}
					rsln.close();rsln = null;
					pstln.close();pstln = null;
					
					
					if(itemCodeOrdt != null && itemCodeOrdt .trim().length() > 0 && itemCodeOrdt.equalsIgnoreCase(itemCodeOrd) )
					{
						
						sql = "SELECT COUNT(*)  FROM SALES_LCDET WHERE SALE_ORDER =  ? AND LINE_NO = ? ";
						
						pstln= conn.prepareStatement(sql);
						pstln.setString(1, saleOrd);
						pstln.setString(2, lineNoSo);
						rsln= pstln.executeQuery();
						if(rsln.next())
						{
							lcdetCnt = rsln.getInt(1);
						}
						rsln.close();rsln = null;
						pstln.close();pstln = null;
						
						if(lcdetCnt >0)
						{
							itemDesc = itemDesr;
						}
					}
					
					if(quotNo != null && quotNo.trim().length() > 0)
					{
						updtSql = "UPDATE SALES_QUOTDET	SET  REL_QTY = (CASE WHEN REL_QTY IS NULL THEN 0 ELSE REL_QTY END) - ? + ? , " +
								"REL_DATE = ? ,	BAL_QTY = (CASE WHEN BAL_QTY IS NULL THEN 0 ELSE BAL_QTY END)  + ? - ?  	" +
								"WHERE QUOT_NO = ?	AND 	 ITEM_CODE = ? ";
						
						pstln= conn.prepareStatement(updtSql);
						pstln.setDouble(1, qtyO);
						pstln.setDouble(2, qty);
						pstln.setTimestamp(3, sysDate);
						pstln.setDouble(4, qtyO);
						pstln.setDouble(5, qty);
						pstln.setString(6, quotNo);
						pstln.setString(7, itemCode);
						int updtQu = pstln.executeUpdate();
						System.out.println("updtQu SALES_QUOTDET :::["+updtQu+"]");
						pstln.close();pstln = null;
					}
					netTotAmtDet=netAmt-billBackAmt-offInvAmt;//added by nandkumar gadkari on 09/07/19
					updtSql = "UPDATE SORDDET SET SITE_CODE = ? , QUANTITY = ? , RATE = ? , DISCOUNT = ? , " +
							"TAX_AMT = ? , TAX_CLASS = ? , TAX_CHAP	= ? , TAX_ENV = ? , NET_AMT = ? , " +
							"QUANTITY__STDUOM	= ? , RATE__STDUOM = ? , PACK_INSTR = ? , NO_ART = ? , " +
							"PACK_CODE	= ? , STATUS = 'Y', DSP_DATE = ? , STATUS_DATE = ? , RATE__CLG = ? , " +
							"UNIT = ? , UNIT__RATE = ? ,  CONV__QTY_STDUOM = ? ,  CONV__RTUOM_STDUOM = ? , " +
							"UNIT__STD	= ? , ORD_VALUE	= ? , ITEM_CODE__ORD = ? , ITEM_CODE = ? , " +
							"ITEM_DESCR	= ?  , MIN_SHELF_LIFE = ? , CUST_SPEC__NO = ? , NATURE  = ? , " +
							"MAX_SHELF_LIFE	 = ?  "
							+ ", NET_TOT_AMT =? " + //added by nandkumar gadkari on 09/07/19
							"WHERE SALE_ORDER	= ? 	" +
							"AND LINE_NO	 = ?   ";
					
					pstln= conn.prepareStatement(updtSql);
					pstln.setString(1, siteCode);
					pstln.setDouble(2, qty);
					pstln.setDouble(3, rate);
					pstln.setDouble(4, disCnt);
					pstln.setDouble(5, taxAmtdt);
					pstln.setString(6, taxClassdt);
					pstln.setString(7, taxChapdt);
					pstln.setString(8, taxEnvdt);
					pstln.setDouble(9, netAmt);
					pstln.setDouble(10, qtyStd);
					pstln.setDouble(11, rateStd);
					pstln.setString(12, pacKIns);
					pstln.setDouble(13, noArt);
					pstln.setString(14, packCode);
					pstln.setTimestamp(15, dspDate);
					pstln.setTimestamp(16, amdDate);
					pstln.setDouble(17, rateClg);
					pstln.setString(18, unit);
					pstln.setString(19, untRate);
					pstln.setDouble(20, convQtyStd);
					pstln.setDouble(21, convRtStd);
					pstln.setString(22, unitStd);
					pstln.setDouble(23, ordValue);
					pstln.setString(24, itemCodeOrd);
					pstln.setString(25, itemCode);
					pstln.setString(26, itemDesc);
					pstln.setDouble(27, minShLif);
					pstln.setString(28, custSpNo);
					pstln.setString(29, nature);
					pstln.setDouble(30, maxShLif);
					pstln.setDouble(31, netTotAmtDet);// added by nandkumar gadkari on 09/07/19
					pstln.setString(32, saleOrd);
					pstln.setString(33, lineNoSo);
					
					int updtSodt = pstln.executeUpdate();
					System.out.println("updtSodt SORDDET:::["+updtSodt+"]");
					pstln.close();pstln = null;
					
				}
				else
				{
					insrtSql ="INSERT INTO SORDDET ( SALE_ORDER,  LINE_NO,  SITE_CODE,  ITEM_FLG,  QUANTITY,  UNIT,  DSP_DATE,  " +
							"RATE,  DISCOUNT,  TAX_AMT, TAX_CLASS,  TAX_CHAP,  TAX_ENV,  NET_AMT,  REMARKS,  STATUS,  STATUS_DATE, " +
							"CHG_DATE,  CHG_USER,  CHG_TERM,   ITEM_DESCR,  UNIT__RATE,  CONV__QTY_STDUOM,  CONV__RTUOM_STDUOM, " +
							"UNIT__STD, QUANTITY__STDUOM, RATE__STDUOM, NO_ART, PACK_CODE,  LINE_NO__CONTR, SPEC_REF,  ITEM_CODE,  " +
							"ITEM_CODE__ORD,  PACK_QTY,  MIN_SHELF_LIFE,  ITEM_SER,  PACK_INSTR,  FREE_GOODS,  RATE__CLG,  MFG_CODE,  " +
							"CONTRACT_NO,  SPECIFICATION_ID,  SPEC_ID,  ORD_VALUE,  ITEM_SER__PROM,  SPECIFIC_INSTR,  DOWN_PAYMENT,  " +
							"DOWN_PAYMENT_INT, INST_AMOUNT, INST_INT_AMOUNT, NO_OF_INST, FREQUENCY, CUST_SPEC__NO, NATURE , MAX_SHELF_LIFE )  " +
							"VALUES ( ?,?,?,?,?  , ?,?,?,?,? , ?,?,?,?,? , ?,?,?,?,? , ?,?,?,?,? , ?,?,?,?,? , ?,?,?,?,?   ," +
							" ?,?,?,?,? , ?,?,?,?,? ,?,?,?,?,? , ?,?,?,?,? ) ";
					
					pstln= conn.prepareStatement(insrtSql);
					
					pstln.setString(1, saleOrd);
					pstln.setString(2, lineNoSo);
					pstln.setString(3, siteCode);
					pstln.setString(4, itemFlg);
					pstln.setDouble(5, qty);
					pstln.setString(6, unit);
					pstln.setTimestamp(7, dspDate);
					pstln.setDouble(8, rate);
					pstln.setDouble(9, disCnt);
					pstln.setDouble(10, taxAmtdt);
					pstln.setString(11, taxClassdt);
					pstln.setString(12, taxChapdt);
					pstln.setString(13, taxEnvdt);
					pstln.setDouble(14, netAmt);
					pstln.setString(15, "");
					pstln.setString(16, "Y");
					pstln.setTimestamp(17, amdDate);
					pstln.setTimestamp(18, sysDate);
					pstln.setString(19, chgUsr);
					pstln.setString(20, chgTrm);
					pstln.setString(21, itemDesc);
					pstln.setString(22, untRate);
					pstln.setDouble(23, convQtyStd);
					pstln.setDouble(24, convRtStd);
					pstln.setString(25, unitStd);
					pstln.setDouble(26, qtyStd);
					pstln.setDouble(27, rateStd);
					pstln.setDouble(28, noArt);
					pstln.setString(29, packCode);
					pstln.setString(30, "");
					pstln.setString(31, "");
					pstln.setString(32, itemCode);
					pstln.setString(33, itemCodeOrd);
					pstln.setDouble(34, 0);
					pstln.setDouble(35, minShLif);
					pstln.setString(36, itemSer);
					pstln.setString(37, pacKIns);
					pstln.setString(38, "");
					pstln.setDouble(39, rateClg);
					pstln.setString(40, "");
					pstln.setString(41, "");
					pstln.setString(42, "");
					pstln.setString(43, "");
					pstln.setDouble(44, ordValue);
					pstln.setString(45, itemSer);
					pstln.setString(46, "");
					pstln.setDouble(47, 0);
					pstln.setDouble(48, 0);
					pstln.setDouble(49, 0);
					pstln.setDouble(50, 0);
					pstln.setDouble(51, 0);
					pstln.setString(52, "");
					pstln.setString(53, custSpNo);
					pstln.setString(54, nature);
					pstln.setDouble(55, maxShLif);
					
					int insrtCnt = pstln.executeUpdate();
					System.out.println("insrtCnt -SORDDET:::["+insrtCnt+"]");
					pstln.close();pstln = null;
					
					if(quotNo != null && quotNo.trim().length() > 0)
					{
						updtSql = "UPDATE SALES_QUOTDET	SET  REL_QTY = (CASE WHEN REL_QTY IS NULL THEN 0 ELSE REL_QTY END) + ? , " +
								"REL_DATE = ? ,	BAL_QTY = (CASE WHEN BAL_QTY IS NULL THEN 0 ELSE BAL_QTY END)  - ?  	" +
								"WHERE QUOT_NO = ?	AND 	 ITEM_CODE = ? ";
						
						pstln= conn.prepareStatement(updtSql);
						pstln.setDouble(1, qty);
						pstln.setTimestamp(2, sysDate);
						pstln.setDouble(3, qty);
						pstln.setString(4, quotNo);
						pstln.setString(5, itemCode);
						
						int updtQu = pstln.executeUpdate();
						System.out.println("updtQu SALES_QUOTDET:::["+updtQu+"]");
						pstln.close();pstln = null;
					}
					
				}//llCnt close
				
				//ls_errcode = gbf_taxproc_conf(as_amd_no, ls_line_no, as_sale_order, ls_line_no__ord)
				errString = taxprocConf(amdNo, lineNo, saleOrd, lineNoSo, conn);
				System.out.println("taxprocConf ::errString:: ["+errString+"]");
				if(errString != null && errString.trim().length() > 0)
				{
					return errString;
				}
				
				sql = "SELECT COUNT(*) AS LL_CNT FROM SORDITEM	" +
						"WHERE SALE_ORDER = ? AND LINE_NO = ?";
				
				pstln= conn.prepareStatement(sql);
				pstln.setString(1, saleOrd);
				pstln.setString(2, lineNoSo);
				rsln= pstln.executeQuery();
				if(rsln.next())
				{
					ll_Cnt = rsln.getInt("LL_CNT");
				}
				System.out.println("ll_Cnt:::==["+ll_Cnt+"]");
				rsln.close();rsln = null;
				pstln.close();pstln = null;
				
				if(itemFlg == null || itemFlg.trim().length() == 0)
				{
					sql = "SELECT ITEM_FLG FROM SORDDET	" +
							"WHERE SALE_ORDER = ? AND LINE_NO = ?";
					pstln= conn.prepareStatement(sql);
					pstln.setString(1, saleOrd);
					pstln.setString(2, lineNoSo);
					rsln= pstln.executeQuery();
					if(rsln.next())
					{
						itemFlg = rsln.getString("ITEM_FLG");
					}
					System.out.println("itemFlg:::==["+itemFlg+"]");
					rsln.close();rsln = null;
					pstln.close();pstln = null;
				}
				System.out.println("itemFlg::["+itemFlg+"]");
				if("I".equalsIgnoreCase(itemFlg))
				{
					if(ll_Cnt == 0 )
					{
						
						insrtSql = "INSERT INTO SORDITEM ( SALE_ORDER, LINE_NO, SITE_CODE, ITEM_CODE__ORD, ITEM_FLAG, " +
								"UNIT__ORD, QTY_ORD, EXP_LEV, ITEM_CODE__REF, UNIT__REF, QTY_REF, ITEM_CODE, ITEM_REF, QUANTITY, " +
								"UNIT, QTY_ALLOC, DATE_ALLOC, QTY_DESP, DATE_DESP, STATUS, STATUS_DATE, LINE_TYPE, CONV__QTY_STDQTY, " +
								"MIN_SHELF_LIFE, NATURE, REAS_CODE, DUE_DATE, CONSUME_FC, REAS_DETAIL, MAX_SHELF_LIFE ) " +
								"VALUES ( ?,?,?,?,? , ?,?,?,?,? , ?,?,?,?,? , ?,?,?,?,? , ?,?,?,?,? , ?,?,?,?,? ) ";
						
						pstln= conn.prepareStatement(insrtSql);
						
						pstln.setString(1, saleOrd);
						pstln.setString(2, lineNoSo);
						pstln.setString(3, siteCode);
						pstln.setString(4, itemCode);
						pstln.setString(5, itemFlg);
						pstln.setString(6, unit);
						pstln.setDouble(7, qty);
						pstln.setDouble(8, 1.0);
						pstln.setString(9, itemCode);
						pstln.setString(10, unit);
						pstln.setDouble(11, qty);
						pstln.setString(12, itemCodeOrd);
						pstln.setString(13, "");
						pstln.setDouble(14, qty);
						pstln.setString(15, unitStd);
						pstln.setDouble(16, 0);
						pstln.setString(17, "");
						pstln.setDouble(18, 0);
						pstln.setString(19, "");
						pstln.setString(20, "");
						pstln.setString(21, "");
						pstln.setString(22, itemFlg);
						pstln.setDouble(23, convQtyStd);
						pstln.setDouble(24, minShLif);
						pstln.setString(25, "");
						pstln.setString(26, "");
						pstln.setTimestamp(27, dueDate);
						pstln.setString(28, consumeFc);
						pstln.setString(29, "");
						pstln.setDouble(30, maxShLif);
						
						int insrtCnt = pstln.executeUpdate();
						System.out.println("insrtCnt-SORDITEM :::["+insrtCnt+"]");
						pstln.close();pstln = null;
						
					}
					else
					{
						sql = "SELECT QTY_DESP  FROM SORDITEM " +
								"WHERE SALE_ORDER = ? AND LINE_NO = ? AND EXP_LEV = '1.'";
						
						pstln= conn.prepareStatement(sql);
						pstln.setString(1, saleOrd);
						pstln.setString(2, lineNoSo);
						rsln= pstln.executeQuery();
						if(rsln.next())
						{
							qtyDesp = rsln.getInt("QTY_DESP");
						}
						System.out.println("qtyDesp:::==["+qtyDesp+"]");
						rsln.close();rsln = null;
						pstln.close();pstln = null;
						
						
						if(qty >= qtyDesp )
						{
							updtSql = "UPDATE SORDITEM	SET QTY_ORD	= ? , QUANTITY = ? , SITE_CODE = ? , " +
									"MIN_SHELF_LIFE = ? , MAX_SHELF_LIFE = ?   " +
									"WHERE SALE_ORDER = ?	AND LINE_NO	= ?	AND EXP_LEV	= '1.'";
							
							pstln= conn.prepareStatement(updtSql);
							pstln.setDouble(1, qty);
							pstln.setDouble(2, qty);
							pstln.setString(3, siteCode);
							pstln.setDouble(4, minShLif);
							pstln.setDouble(5, maxShLif);
							pstln.setString(6, saleOrd);
							pstln.setString(7, lineNoSo);
							
							int updtQu = pstln.executeUpdate();
							System.out.println("updtQu- SORDITEM:::["+updtQu+"]");
							pstln.close();pstln = null;
							
						}
						else
						{
							errString = itmDBAccessLocal.getErrorString("", "VTSOAMD2", "","",conn);
							System.out.println("@@@@@@@@@@ errString["+errString+"]");
							return errString;
						}
					}
				}//itemFlg
				else
				{
					sql = "SELECT SUM(QTY_DESP) AS SUM_QTY_DESP FROM SORDITEM " +
							"WHERE SALE_ORDER = ? AND LINE_NO = ? AND EXP_LEV = '1.'";
					
					pstln= conn.prepareStatement(sql);
					pstln.setString(1, saleOrd);
					pstln.setString(2, lineNoSo);
					rsln= pstln.executeQuery();
					if(rsln.next())
					{
						despQty = rsln.getInt("SUM_QTY_DESP");
					}
					System.out.println("despQty:::==["+despQty+"]");
					rsln.close();rsln = null;
					pstln.close();pstln = null;
					
					if(despQty > 0)
					{
						errString = itmDBAccessLocal.getErrorString("", "VTSOAMD3", "","",conn);
						System.out.println("@@@@@@@@@@ errString["+errString+"]");
						return errString;
					}
					else
					{	
						detSql = "DELETE FROM SORDITEM  WHERE SALE_ORDER = ? AND LINE_NO = ? ";
						
						pstln= conn.prepareStatement(detSql);
						pstln.setString(1, saleOrd);
						pstln.setString(2, lineNoSo);
						
						int updt = pstln.executeUpdate();
						System.out.println("detSql- SORDITEM:::["+updt+"]");
						pstln.close();pstln = null;
						
						// if not gbf_sorditem_explode(as_sale_order,ls_so_ln) then 
						//modified by rupali on 11/05/2021 to display original message on front end as required by user
						String errCode = sorditemExplode (saleOrd, lineNoSo, conn);
						//if(!sorditemExplode (saleOrd, lineNoSo, conn))
						if(errCode != null && errCode.trim().length() > 0)
						{
							if(errCode.equals("INVTAXCHAP"))
							{
								errString = itmDBAccessLocal.getErrorString("", "INVTAXCHAP", "","",conn);
							}
							else
							{
								errString = itmDBAccessLocal.getErrorString("", "VTSOAMD1", "","",conn);
							}
							System.out.println("@@@@@@@@@@ errString["+errString+"]");
							return errString;
						}
						
						
					}//despQty
				}//itemFlg
				System.out.println("custSpNo::["+custSpNo+"]  custSpNoO::["+custSpNoO+"]");
				if(!custSpNoO.equalsIgnoreCase(custSpNo))
				{
					//ls_sordno[upperbound(ls_sordno[]) + 1] = ls_so_ln	
					sordLineNo.add(lineNoSo);
				}
				
			}// while close-SORDAMDDET
			rs.close();rs = null;
			pstmt.close();pstmt = null;
			System.out.println("sordLineNo:::["+sordLineNo+"]");
			System.out.println("*******errString::::["+errString+"]");
			
			if (errString != null && errString.trim().length() > 0)
			{
				return errString;
			} 
			
			updtSql = "UPDATE SORDER	SET TOT_ORD_VALUE = ( SELECT (CASE WHEN SUM(ORD_VALUE) IS NULL THEN 0 ELSE SUM(ORD_VALUE) END )	" +
					"FROM SORDDET WHERE SALE_ORDER = ? ) " +
					"WHERE SALE_ORDER = ? ";
			
			pstmt = conn.prepareStatement(updtSql);
			pstmt.setString(1, saleOrd);
			pstmt.setString(2, saleOrd);
			
			int updtSord = pstmt.executeUpdate();
			System.out.println("updtSord SORDER:::====["+updtSord+"]");
			pstmt.close();pstmt = null;
			
			
			sql = "SELECT COUNT(1) AS LL_CNT FROM DISTORDER  WHERE SALE_ORDER = ? ";
			
			pstmt= conn.prepareStatement(sql);
			pstmt.setString(1, saleOrd);
			rs= pstmt.executeQuery();
			if(rs.next())
			{
				ll_Cnt = rs.getInt("LL_CNT");
			}
			System.out.println("ll_Cnt:::====["+ll_Cnt+"]");
			rs.close();rs = null;
			pstmt.close();pstmt = null;
			
			if(ll_Cnt > 0)
			{
				if(sordLineNo.size()  > 0)
				{
					System.out.println("sordLineNo:::==["+sordLineNo+"]");
					//ls_errcode = gbf_dist_orderamd(as_amd_no,as_sale_order,ls_sordno[])
					errString = distOrderamd(amdNo,saleOrd, sordLineNo,chgUsr, conn);
				}
			}
			
		}
		catch(Exception e)
		{
			System.out.println("Exception in sordamdConfirm()::::: ["+e.getMessage()+"]");
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
		System.out.println("errString in sordamdConfirm():::>>>>>> ["+errString+"]");
		return errString;
	}
	
	
	public String taxprocConf(String amdNo, String lineNo,String saleOrd ,String lineNoOrd , Connection conn) throws RemoteException, ITMException
	{
		PreparedStatement pstmt = null, pstmtln = null;
		ResultSet rs = null, rsln = null;
		int cnt = 0 , liCnt = 0;
		String retString = "", sql = "", sqlln = "", purcOrder = "",  errCode = "";
		String lineNoTax = "", taxCode = "", taxBase = "", taxPerc = "", chgStat = "", taxSet = "", effect = "", acctCodeReco = "";
		String cctrCodeReco = "", recoPerc = "", acctCode = "", cctrCode = "", rateType = "", round = "", roundTo = "", taxForm = "",excedAmt1="";
		String acctCodeApAdv = "", cctrCodeApAdv = "";
		String taxChap = "", taxEnv = "", taxClass = "", packCode = "", packInstr = "", acctCodeDr = "", cctrCodeDr = "";		
		String loginEmpCode = "", ediOption = "", dataStr = "" , chgUser = "", chgTerm = "", posted = "" , payTax = ""; 
		String formNo="";  //formNo variable declared by manish mhatre on 10/6/2019
		double taxableAmt = 0, taxAmt = 0, recoAmont = 0, poHdrTot = 0;
		Timestamp sysDate = null ,chgDate = null, taxFormDate = null;

		try
		{
			System.out.println("taxprocConf called..............");

			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String sysDateStr = sdf.format(currentDate.getTime());
			System.out.println("Now the date  [" + sysDateStr + "]");
			sysDate = Timestamp.valueOf(genericUtility.getValidDateString(sysDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");

			if(lineNoOrd.trim().length() > 0)
			{
				sql = " DELETE FROM TAXTRAN WHERE TRAN_CODE = 'S-ORO' AND TRAN_ID = ? AND LINE_NO = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, saleOrd);
				pstmt.setString(2, lineNoOrd);
				pstmt.executeUpdate();
				pstmt.close();
				pstmt = null;
				
				
				sql ="SELECT COUNT(TRAN_ID) AS LI_CNT FROM TAXTRAN  " +
						"WHERE  TRAN_CODE = 'S-ORD'  AND   TRAN_ID = ?  AND   LINE_NO = ? ";
				
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, saleOrd);
				pstmt.setString(2, lineNoOrd);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					liCnt = rs.getInt("LI_CNT");
				}
				System.out.println("liCnt:::["+liCnt+"]");
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
				if(liCnt > 0)
				{
					/*sql = "SELECT TRAN_CODE , TRAN_ID , LINE_NO , LINE_NO__TAX , TAX_CODE , TAX_CLASS , TAX_CHAP , TAX_BASE , TAX_ENV , TAXABLE_AMT , " +
							"TAX_PERC , TAX_AMT , CHG_STAT , TAX_SET , EFFECT , ACCT_CODE__RECO , CCTR_CODE__RECO, RECO_PERC , RECO_AMOUNT, ACCT_CODE , " +
							"CCTR_CODE , RATE_TYPE , ROUND , ROUND_TO , TAX_FORM , CHG_DATE , CHG_USER , CHG_TERM , POSTED , TAX_FORM_DATE , PAY_TAX  " +
							"FROM TAXTRAN " +
							"WHERE TRAN_CODE = 'S-ORD' AND TRAN_ID = ?  AND LINE_NO = ? ";*/
					
					sql = "SELECT TRAN_CODE , TRAN_ID , LINE_NO , LINE_NO__TAX , TAX_CODE , TAX_CLASS , TAX_CHAP , TAX_BASE , TAX_ENV , TAXABLE_AMT , " +
							"TAX_PERC , TAX_AMT , CHG_STAT , TAX_SET , EFFECT , ACCT_CODE__RECO , CCTR_CODE__RECO, RECO_PERC , RECO_AMOUNT, ACCT_CODE , " +
							"CCTR_CODE , RATE_TYPE , ROUND , ROUND_TO , TAX_FORM , CHG_DATE , CHG_USER , CHG_TERM , POSTED , TAX_FORM_DATE , PAY_TAX , FORM_NO  " +         //form_no added by manish mhatre on 26/6/2019
							"FROM TAXTRAN " +
							"WHERE TRAN_CODE = 'S-ORD' AND TRAN_ID = ?  AND LINE_NO = ? ";
					
					
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, saleOrd);
					pstmt.setString(2, lineNoOrd);
					rs = pstmt.executeQuery();
					while (rs.next())
					{
						lineNoTax = rs.getString("LINE_NO__TAX");
						taxCode = rs.getString("TAX_CODE");
						taxClass = rs.getString("TAX_CLASS");
						taxChap = rs.getString("TAX_CHAP");
						taxBase = rs.getString("TAX_BASE");
						taxEnv = rs.getString("TAX_ENV");
						taxableAmt = rs.getDouble("TAXABLE_AMT");
						taxPerc = rs.getString("TAX_PERC");
						taxAmt = rs.getDouble("TAX_AMT");
						chgStat = rs.getString("CHG_STAT");
						taxSet = rs.getString("TAX_SET");
						effect = rs.getString("EFFECT");
						acctCodeReco = rs.getString("ACCT_CODE__RECO");
						cctrCodeReco = rs.getString("CCTR_CODE__RECO");
						recoPerc = rs.getString("RECO_PERC");
						recoAmont = rs.getDouble("RECO_AMOUNT");
						acctCode = rs.getString("ACCT_CODE");
						cctrCode = rs.getString("CCTR_CODE");
						rateType = rs.getString("RATE_TYPE");
						round = rs.getString("ROUND");
						roundTo = rs.getString("ROUND_TO");
						taxForm = rs.getString("TAX_FORM");
						chgDate = rs.getTimestamp("CHG_DATE");
						chgUser = rs.getString("CHG_USER");
						chgTerm = rs.getString("CHG_TERM");
						posted = rs.getString("POSTED");
						taxFormDate = rs.getTimestamp("TAX_FORM_DATE");
						payTax = rs.getString("PAY_TAX");
						formNo=rs.getString("FORM_NO");     //added by manish mhatre on 26/6/2019
						
						/*sqlln = "INSERT INTO TAXTRAN (TRAN_CODE , TRAN_ID , LINE_NO , LINE_NO__TAX , TAX_CODE , TAX_CLASS , TAX_CHAP , TAX_BASE , " +
								"TAX_ENV , TAXABLE_AMT , TAX_PERC , TAX_AMT , CHG_STAT , TAX_SET , EFFECT , ACCT_CODE__RECO , CCTR_CODE__RECO, " +
								"RECO_PERC , RECO_AMOUNT, ACCT_CODE , CCTR_CODE , RATE_TYPE , ROUND , ROUND_TO , TAX_FORM , CHG_DATE , " +
								"CHG_USER , CHG_TERM , POSTED , TAX_FORM_DATE , PAY_TAX )  " +
								"VALUES ( ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";*/
						
						sqlln = "INSERT INTO TAXTRAN (TRAN_CODE , TRAN_ID , LINE_NO , LINE_NO__TAX , TAX_CODE , TAX_CLASS , TAX_CHAP , TAX_BASE , " +
								"TAX_ENV , TAXABLE_AMT , TAX_PERC , TAX_AMT , CHG_STAT , TAX_SET , EFFECT , ACCT_CODE__RECO , CCTR_CODE__RECO, " +
								"RECO_PERC , RECO_AMOUNT, ACCT_CODE , CCTR_CODE , RATE_TYPE , ROUND , ROUND_TO , TAX_FORM , CHG_DATE , " +
								"CHG_USER , CHG_TERM , POSTED , TAX_FORM_DATE , PAY_TAX , FORM_NO  )  " +            //FORM_NO added by manish mhatre on 26/6/2019
								"VALUES ( ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
						
						pstmtln = conn.prepareStatement(sqlln);
						pstmtln.setString(1, "S-ORO");
						pstmtln.setString(2, saleOrd);
						pstmtln.setString(3, lineNoOrd); 
						pstmtln.setString(4, lineNoTax);
						pstmtln.setString(5, taxCode);
						pstmtln.setString(6, taxClass);
						pstmtln.setString(7, taxChap);
						pstmtln.setString(8, taxBase);
						pstmtln.setString(9, taxEnv);
						pstmtln.setDouble(10, taxableAmt);
						pstmtln.setString(11, taxPerc);
						pstmtln.setDouble(12, taxAmt);
						pstmtln.setString(13, chgStat);
						pstmtln.setString(14, taxSet);
						pstmtln.setString(15, effect);
						pstmtln.setString(16, acctCodeReco);
						pstmtln.setString(17, cctrCodeReco);
						pstmtln.setString(18, recoPerc);
						pstmtln.setDouble(19, recoAmont);
						pstmtln.setString(20, acctCode);
						pstmtln.setString(21, cctrCode);
						pstmtln.setString(22, rateType);
						pstmtln.setString(23, round);
						pstmtln.setString(24, roundTo);
						pstmtln.setString(25, taxForm);
						pstmtln.setTimestamp(26, chgDate);
						pstmtln.setString(27, chgUser);
						pstmtln.setString(28, chgTerm);
						pstmtln.setString(29, posted);
						pstmtln.setTimestamp(30, taxFormDate);
						pstmtln.setString(31, payTax);
						pstmtln.setString(32, formNo);      //added by manish mhatre on 26/6/2019
						
						cnt = pstmtln.executeUpdate();
						pstmtln.close();
						pstmtln = null;
					
					}
					
					System.out.println("tax____Amt:"+taxAmt);
					System.out.println("form____no:"+formNo);
					System.out.println("insert cnt:::["+cnt+"]");
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					sql = " DELETE FROM TAXTRAN WHERE TRAN_CODE = 'S-ORD' AND TRAN_ID = ? AND LINE_NO = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, saleOrd);
					pstmt.setString(2, lineNoOrd);
					pstmt.executeUpdate();
					pstmt.close();
					pstmt = null;
				}//liCnt
			}//lineNoOrd
			
			//*************
			
			// 25-nov-2020 manoharan line_no should have leading spaces
			lineNo = "    " + lineNo;
			lineNo = lineNo.substring(lineNo.length() - 3,lineNo.length());
			int iLineNo = Integer.parseInt(lineNo.trim());
			// end 25-nov-2020 manoharan line_no should have leading spaces
			sql ="SELECT COUNT(TRAN_ID) AS LI_CNT FROM TAXTRAN  " +
					"WHERE  TRAN_CODE = 'S-AMD'  AND   TRAN_ID = ?  AND   LINE_NO = ? ";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, amdNo);
			pstmt.setString(2, lineNo);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				liCnt = rs.getInt("LI_CNT");
			}
			System.out.println("liCnt::=["+liCnt+"]");
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			if(liCnt > 0)
			{
				
				if(lineNoOrd.trim().length() == 0)
				{
					sql = "SELECT LINE_NO__SORD FROM  SORDAMDDET WHERE AMD_NO = ?  AND  LINE_NO = ? ";
					
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, amdNo);
					//pstmt.setString(2, lineNo); // // 25-nov-2020 manoharan line_no should have leading spaces
					pstmt.setInt(2, iLineNo); //  25-nov-2020 manoharan line_no having leading spaces so converted integer as per data type of SORDAMDDET
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						lineNoOrd = rs.getString("LINE_NO__SORD");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					
				}
				
				/*sql = "SELECT TRAN_CODE , TRAN_ID , LINE_NO , LINE_NO__TAX , TAX_CODE , TAX_CLASS , TAX_CHAP , TAX_BASE , TAX_ENV , TAXABLE_AMT , " +
						"TAX_PERC , TAX_AMT , CHG_STAT , TAX_SET , EFFECT , ACCT_CODE__RECO , CCTR_CODE__RECO, RECO_PERC , RECO_AMOUNT, ACCT_CODE , " +
						"CCTR_CODE , RATE_TYPE , ROUND , ROUND_TO , TAX_FORM , CHG_DATE , CHG_USER , CHG_TERM , POSTED , TAX_FORM_DATE , PAY_TAX " +      
						"FROM TAXTRAN " +
						"WHERE TRAN_CODE = 'S-AMD' AND TRAN_ID = ?  AND LINE_NO = ? ";*/  //commented by manish mhatre on 11/6/2019
				sql = "SELECT TRAN_CODE , TRAN_ID , LINE_NO , LINE_NO__TAX , TAX_CODE , TAX_CLASS , TAX_CHAP , TAX_BASE , TAX_ENV , TAXABLE_AMT , " +
						"TAX_PERC , TAX_AMT , CHG_STAT , TAX_SET , EFFECT , ACCT_CODE__RECO , CCTR_CODE__RECO, RECO_PERC , RECO_AMOUNT, ACCT_CODE , " +
						"CCTR_CODE , RATE_TYPE , ROUND , ROUND_TO , TAX_FORM , CHG_DATE , CHG_USER , CHG_TERM , POSTED , TAX_FORM_DATE , PAY_TAX , FORM_NO " +      //form_no added by manish mhatre on 11/6/2019
						" FROM TAXTRAN " +
						" WHERE TRAN_CODE = 'S-AMD' AND TRAN_ID = ?  AND LINE_NO = ? ";

				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, amdNo);
				pstmt.setString(2, lineNo);
				rs = pstmt.executeQuery();
				while (rs.next())
				{
					lineNoTax = rs.getString("LINE_NO__TAX");
					taxCode = rs.getString("TAX_CODE");
					taxClass = rs.getString("TAX_CLASS");
					taxChap = rs.getString("TAX_CHAP");
					taxBase = rs.getString("TAX_BASE");
					taxEnv = rs.getString("TAX_ENV");
					taxableAmt = rs.getDouble("TAXABLE_AMT");
					taxPerc = rs.getString("TAX_PERC");
					taxAmt = rs.getDouble("TAX_AMT");
					chgStat = rs.getString("CHG_STAT");
					taxSet = rs.getString("TAX_SET");
					effect = rs.getString("EFFECT");
					acctCodeReco = rs.getString("ACCT_CODE__RECO");
					cctrCodeReco = rs.getString("CCTR_CODE__RECO");
					recoPerc = rs.getString("RECO_PERC");
					recoAmont = rs.getDouble("RECO_AMOUNT");
					acctCode = rs.getString("ACCT_CODE");
					cctrCode = rs.getString("CCTR_CODE");
					rateType = rs.getString("RATE_TYPE");
					round = rs.getString("ROUND");
					roundTo = rs.getString("ROUND_TO");
					taxForm = rs.getString("TAX_FORM");
					chgDate = rs.getTimestamp("CHG_DATE");
					chgUser = rs.getString("CHG_USER");
					chgTerm = rs.getString("CHG_TERM");
					posted = rs.getString("POSTED");
					taxFormDate = rs.getTimestamp("TAX_FORM_DATE");
					payTax = rs.getString("PAY_TAX");
					formNo=rs.getString("FORM_NO");//added by manish mhatre on 11/6/2019
					
					
					System.out.println("Form no:"+formNo);
					
					
					/*sqlln = "INSERT INTO TAXTRAN (TRAN_CODE , TRAN_ID , LINE_NO , LINE_NO__TAX , TAX_CODE , TAX_CLASS , TAX_CHAP , TAX_BASE , " +
							"TAX_ENV , TAXABLE_AMT , TAX_PERC , TAX_AMT , CHG_STAT , TAX_SET , EFFECT , ACCT_CODE__RECO , CCTR_CODE__RECO, " +
							"RECO_PERC , RECO_AMOUNT, ACCT_CODE , CCTR_CODE , RATE_TYPE , ROUND , ROUND_TO , TAX_FORM , CHG_DATE , " +
							"CHG_USER , CHG_TERM , POSTED , TAX_FORM_DATE , PAY_TAX )  " +                  
							"VALUES ( ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";  */  //commented by manish mhatre on 11/6/2019
					
					
					sqlln = "INSERT INTO TAXTRAN (TRAN_CODE , TRAN_ID , LINE_NO , LINE_NO__TAX , TAX_CODE , TAX_CLASS , TAX_CHAP , TAX_BASE , " +
							"TAX_ENV , TAXABLE_AMT , TAX_PERC , TAX_AMT , CHG_STAT , TAX_SET , EFFECT , ACCT_CODE__RECO , CCTR_CODE__RECO, " +
							"RECO_PERC , RECO_AMOUNT, ACCT_CODE , CCTR_CODE , RATE_TYPE , ROUND , ROUND_TO , TAX_FORM , CHG_DATE , " +
							"CHG_USER , CHG_TERM , POSTED , TAX_FORM_DATE , PAY_TAX , FORM_NO )  " +                   //form_no added by manish mhatre on 11/6/2019
							"VALUES ( ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";  
					
					pstmtln = conn.prepareStatement(sqlln);
					pstmtln.setString(1, "S-ORD");
					pstmtln.setString(2, saleOrd);
					pstmtln.setString(3, lineNoOrd); 
					pstmtln.setString(4, lineNoTax);
					pstmtln.setString(5, taxCode);
					pstmtln.setString(6, taxClass);
					pstmtln.setString(7, taxChap);
					pstmtln.setString(8, taxBase);
					pstmtln.setString(9, taxEnv);
					pstmtln.setDouble(10, taxableAmt);
					pstmtln.setString(11, taxPerc);
					pstmtln.setDouble(12, taxAmt);
					pstmtln.setString(13, chgStat);
					pstmtln.setString(14, taxSet);
					pstmtln.setString(15, effect);
					pstmtln.setString(16, acctCodeReco);
					pstmtln.setString(17, cctrCodeReco);
					pstmtln.setString(18, recoPerc);
					pstmtln.setDouble(19, recoAmont);
					pstmtln.setString(20, acctCode);
					pstmtln.setString(21, cctrCode);
					pstmtln.setString(22, rateType);
					pstmtln.setString(23, round);
					pstmtln.setString(24, roundTo);
					pstmtln.setString(25, taxForm);
					pstmtln.setTimestamp(26, chgDate);
					pstmtln.setString(27, chgUser);
					pstmtln.setString(28, chgTerm);
					pstmtln.setString(29, posted);
					pstmtln.setTimestamp(30, taxFormDate);
					pstmtln.setString(31, payTax);
					pstmtln.setString(32, formNo);      //added by manish mhatre on 11/6/2019

					cnt = pstmtln.executeUpdate();
					pstmtln.close();
					pstmtln = null;
				
				}
				System.out.println("insert cnt:::=["+cnt+"]");
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
			}//liCnt
			
			
		} 
		catch (Exception e)
		{
			System.out.println("Exception :taxprocConf() ::" + e.getMessage() + ":");
			errCode = e.getMessage();
			e.printStackTrace();
			throw new ITMException(e);
		}

		return errCode;
	}

	//public boolean  sorditemExplode(String saleOrd ,String lineNoOrd , Connection conn) throws  ITMException
	public String  sorditemExplode(String saleOrd ,String lineNoOrd , Connection conn) throws  ITMException //changed return type by rupali on 11/05/2021 to display original message on front end as required by user
	{
		PreparedStatement pstmt = null, pstmtln = null;
		ResultSet rs = null, rsln = null;
		int cnt = 0 , rtnRate = 0;
		String retString = "", sql = "", sqlln = "", itemCode = "",  errCode = "", siteCode = "" ,expLev = "1.";
			
		String loginEmpCode = "", itemCodeOrd = "", qtyStd = "" , unitStd = "", lineNoCtr = "", unit = "" , itemFlag = "" , nature = "";
		double taxableAmt = 0, taxAmt = 0, recoAmont = 0, poHdrTot = 0 , qty = 0 , rate = 0, minLife = 0;
		Timestamp sysDate = null ,chgDate = null, taxFormDate = null;
		SorderConf sordConf = new SorderConf();
		try
		{
			System.out.println("sorditemExplode called..............");
			
			sql = "SELECT SALE_ORDER, LINE_NO, SITE_CODE, ITEM_CODE, ITEM_FLG,  QUANTITY, UNIT, LINE_NO__CONTR, UNIT__STD, " +
					"QUANTITY__STDUOM, ITEM_CODE__ORD,NATURE,RATE " +
					"FROM SORDDET  " +
					"WHERE SALE_ORDER =  ?   AND LINE_NO = ?  ";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, saleOrd);
			pstmt.setString(2, lineNoOrd);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				siteCode = checkNull(rs.getString("SITE_CODE"));
				itemCode = checkNull(rs.getString("ITEM_CODE"));
				itemFlag = checkNull(rs.getString("ITEM_FLG"));
				qty = rs.getDouble("QUANTITY");
				unit = checkNull(rs.getString("UNIT"));
				lineNoCtr = checkNull(rs.getString("LINE_NO__CONTR"));
				unitStd = checkNull(rs.getString("UNIT__STD"));
				qtyStd = checkNull(rs.getString("QUANTITY__STDUOM"));
				itemCodeOrd = checkNull(rs.getString("ITEM_CODE__ORD"));
				nature = checkNull(rs.getString("NATURE"));
				rate = rs.getDouble("RATE");
				
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			HashMap itemMap = new HashMap();
			itemMap.put("sale_order", saleOrd);
			itemMap.put("line_no", lineNoOrd);
			itemMap.put("site_code", siteCode);
			itemMap.put("item_code__ord", itemCode);
			itemMap.put("item_code__ref", itemCode);
			itemMap.put("line_type", itemFlag);
			itemMap.put("item_code", itemCode);
			itemMap.put("item_flag", itemFlag);
			itemMap.put("unit__ord", unit);
			itemMap.put("unit__ref", unit);
			itemMap.put("unit", unit);
			itemMap.put("nature", nature);
			itemMap.put("exp_lev", expLev);
			itemMap.put("rate", rate);
			itemMap.put("qty_ord", qty);
			itemMap.put("qty_ref", qty);
			itemMap.put("quantity", qty);
			itemMap.put("min_shelf_life", minLife);
			
			System.out.println("itemMap:::["+itemMap+"]");
			
			if(!"I".equalsIgnoreCase(itemFlag))
			{
				rtnRate = sordConf.explodeBomDs(saleOrd, itemCodeOrd, expLev, lineNoOrd, itemFlag, conn);
				
				if(rtnRate != 1)
				{
					//added by rupali on 05/05/2021 to validate tax chapter [start]
					if(rtnRate == -1)
					{
						errCode = "INVTAXCHAP";
						System.out.println("inside sorditemExplode tax chap not validated:::["+errCode+"]");
					}
					//added by rupali on 05/05/2021 to validate tax chapter [end]
					else
					{
						errCode = "VTSUCC";
					}
				}
			}
			//commented by rupali on 11/05/2021 to display original message on front end as required by user
			/*
			if(errCode.trim().length() !=  0 )
			{
				return false;
			}
			*/
		} 
		catch (Exception e)
		{
			System.out.println("Exception :sorditemExplode() ::" + e.getMessage() + ":");
			//errCode = e.getMessage();
			e.printStackTrace();
			throw new ITMException(e);
		}

		//return true;
		return errCode; //modified by rupali on 11/05/2021 to display original message on front end as required by user
	}
	
	
	public String distOrderamd(String amdNo,String saleOrd ,ArrayList sordLineNo ,String chgUsr, Connection conn) throws  ITMException
	{
		PreparedStatement pstmt = null, pstmtln = null;
		ResultSet rs = null, rsln = null;
		int cnt = 0 , sordCnt = 0, lineNo = 1;
		String retString = "", sql = "", sqlln = "", lineNoOrd = "",  errCode = "", disOrd= "" , disOrdStr = "" , disOrdListStr = "" , tranType = "",
				sunType ="",remarks = "", siteCode= "", purcOrd = "", remarks1 = "", remarks2 = "", siteCodeSh = "",siteCodedlv = "",tranMode = "", policyno = "",
				custCodedlv = "",dlvTo = "", dlvAdd1 = "", dlvAdd2 = "", dlvAdd3 = "", stanCode = "", stanCodedlv = "",dlvPin = "",tele1Dlv = "" ,tele2Dlv ="", tele3Dlv = "",
				locCodeGit = "" , locCodeCons = "",  locCodeDamg = "", prcList = "", prcListClg = "",siteCodeBil = "", currCode = "", projCode = "", salePers = "",
				autoRecpt = "", avlYn= "", targetWgt = "",targetVol = "",custOrdNo = "" , sunCode= "" , dlvCity = "", countCodedlv = "", remarksN = "",  lineNO = "", 
				saleOrdet = "", lineNoSord = "", tranIdemag = "", itemCode = "", unitAlt = "", remaksDet = "",  packInst = "", reasCode = "" , unit= "",taxClass = "",
				taxChp = "", taxEnv = "", custSpecNo = "" , xmlString = "", errString = "" ;
	
		double taxableAmt = 0, exchRate = 0,totAmt = 0, taxAmt = 0, netAmt = 0 , qtyOrdAlt = 0,conQtyAlt = 0,qtyOrd = 0, rate = 0, qtyShip = 0, qtyReceiv = 0,
				qtyConf = 0 , qtyReturn = 0,rateClg = 0,ovrShiPer = 0, disCnt = 0 ;
		Timestamp sysDate = null ,chgDate = null, ordDate = null ,shipDate = null, dueDate = null;
		
		StringBuffer disOrdLineBuff = new StringBuffer();
		StringBuffer xmlBuff = null;
		HashMap distOrdMap = new HashMap(); 
		ArrayList lineNoSordList = null;
		ArrayList lineNoSordListIn = null;
		
		try
		{
			System.out.println("distOrderamd called..............");
			
			java.sql.Timestamp tranDate = null;
			tranDate = new java.sql.Timestamp(System.currentTimeMillis()) ;
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			sordCnt = sordLineNo.size();
			System.out.println("sordCnt::["+sordCnt+"]");
			for(int i= 0; i<sordCnt ; i++)
			{
				lineNoOrd = sordLineNo.get(i).toString();
				System.out.println("lineNoOrd::["+lineNoOrd+"]");
				
				sql = "SELECT DIST_ORDER  FROM DISTORDER_DET WHERE  SALE_ORDER 	= ?  AND TRIM(LINE_NO__SORD) = ? ";
				
				pstmt= conn.prepareStatement(sql);
				pstmt.setString(1, saleOrd);
				pstmt.setString(2,lineNoOrd.trim());
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					disOrd = checkNull(rs.getString("DIST_ORDER"));
					
				}
				System.out.println("disOrd::["+disOrd+"]");
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
				if(disOrd.trim().length() > 0)
				{
					System.out.println("disOrd::=["+disOrd+"]");
					if(distOrdMap != null && distOrdMap.size() > 0)
					{
						if(distOrdMap.containsKey(disOrd))
						{
							System.out.println("containsKey:>>:::"+disOrd);
							
							lineNoSordListIn = (ArrayList) distOrdMap.get(disOrd);
							System.out.println("lineNoSordListIn:>>:::"+lineNoSordListIn);
							
							if(!lineNoSordListIn.contains(lineNoOrd))
							{
								lineNoSordListIn.add(lineNoOrd);
							}
							System.out.println("lineNoSordListIn:>>:::"+lineNoSordListIn);
							distOrdMap.put(disOrd, lineNoSordListIn);
							
							System.out.println("distOrdMap::>>"+distOrdMap);
						}
						else
						{
							lineNoSordList = new ArrayList();
							lineNoSordList.add(lineNoOrd);
							System.out.println("lineNoSordList::,,,,,,"+lineNoSordList);
							distOrdMap.put(disOrd, lineNoSordList);
							System.out.println("distOrdMap::,,,,,,"+distOrdMap);
						}
						
					}
					else
					{
						lineNoSordList = new ArrayList();
						lineNoSordList.add(lineNoOrd);
						System.out.println("lineNoSordList::''''''"+lineNoSordList);
						distOrdMap.put(disOrd, lineNoSordList);
						System.out.println("distOrdMap::''''''''"+distOrdMap);
						
					}
				}
				disOrd = "";
				
			}
			
			System.out.println("distOrdMap::::=== "+distOrdMap+"");
			
			
			Iterator<Entry> iterator = distOrdMap.entrySet().iterator();
			while (iterator.hasNext()) 
			{
				Map.Entry<String,ArrayList> entry = (Map.Entry<String,ArrayList>) iterator.next();
				
				System.out.println("Key :[" + entry.getKey() + "]  Value :[" + entry.getValue()+"]");
				
				disOrdStr = entry.getKey();
				ArrayList  lineNoSORD = entry.getValue();
				
				//disOrdStr = checkNull(rs.getString("DIST_ORDER"));
				System.out.println("disOrdStr::====["+disOrdStr+"]");
				
				for(int i = 0 ; i< lineNoSORD.size(); i++ )
				{
					if( disOrdLineBuff.length() == 0)
					{
						disOrdLineBuff.append("'").append( lineNoSORD.get(i) ).append("'");
					} 
					else
					{
						disOrdLineBuff.append(",'").append( lineNoSORD.get(i) ).append("'");
					}
					
				}
				System.out.println("disOrdLineBuff::====["+disOrdLineBuff+"]");
				
				sql = "SELECT TRAN_TYPE, ORDER_DATE, SUNDRY_TYPE, SUNDRY_CODE , REMARKS , SITE_CODE, PURC_ORDER, REMARKS1 , REMARKS2, " +
						"SITE_CODE__SHIP, SITE_CODE__DLV, TRANS_MODE, SHIP_DATE , DUE_DATE, POLICY_NO, CUST_CODE__DLV, DLV_TO, DLV_ADD1, " +
						"DLV_ADD2, DLV_ADD3, DLV_CITY ,STAN_CODE, STATE_CODE__DLV , COUNT_CODE__DLV, DLV_PIN, TEL1__DLV, TEL2__DLV, TEL3__DLV, LOC_CODE__GIT," +
						"LOC_CODE__CONS, LOC_CODE__DAMAGED , PRICE_LIST, PRICE_LIST__CLG, SITE_CODE__BIL , CURR_CODE, EXCH_RATE , TOT_AMT," +
						"TAX_AMT,NET_AMT, PROJ_CODE, SALES_PERS ,AUTO_RECEIPT, AVALIABLE_YN, TARGET_WGT, TARGET_VOL , CUST_ORDER__NO " +
						"FROM DISTORDER WHERE DIST_ORDER = ? ";
				
				pstmtln= conn.prepareStatement(sql);
				pstmtln.setString(1, disOrdStr);
				rsln = pstmtln.executeQuery();
				if (rsln.next())
				{
					tranType = checkNull(rsln.getString("TRAN_TYPE"));
					ordDate  = rsln.getTimestamp("ORDER_DATE");
					sunType = checkNull(rsln.getString("SUNDRY_TYPE"));
					sunCode = checkNull(rsln.getString("SUNDRY_CODE"));
					remarks = checkNull(rsln.getString("REMARKS"));
					siteCode = checkNull(rsln.getString("SITE_CODE"));
					purcOrd = checkNull(rsln.getString("PURC_ORDER"));
					remarks1 = checkNull(rsln.getString("REMARKS1"));
					remarks2 = checkNull(rsln.getString("REMARKS2"));
					siteCodeSh = checkNull(rsln.getString("SITE_CODE__SHIP"));
					siteCodedlv = checkNull(rsln.getString("SITE_CODE__DLV"));
					tranMode = checkNull(rsln.getString("TRANS_MODE"));
					shipDate  = rsln.getTimestamp("SHIP_DATE");
					dueDate  = rsln.getTimestamp("DUE_DATE");
					policyno = checkNull(rsln.getString("POLICY_NO"));
					custCodedlv = checkNull(rsln.getString("CUST_CODE__DLV"));
					dlvTo = checkNull(rsln.getString("DLV_TO"));
					dlvAdd1 = checkNull(rsln.getString("DLV_ADD1"));
					dlvAdd2 = checkNull(rsln.getString("DLV_ADD2"));
					dlvAdd3 = checkNull(rsln.getString("DLV_ADD3"));
					dlvAdd3 = checkNull(rsln.getString("DLV_ADD3"));
					dlvCity = checkNull(rsln.getString("DLV_CITY"));
					stanCode = checkNull(rsln.getString("STAN_CODE"));
					stanCodedlv = checkNull(rsln.getString("STATE_CODE__DLV"));
					countCodedlv = checkNull(rsln.getString("COUNT_CODE__DLV"));
					
					dlvPin = checkNull(rsln.getString("DLV_PIN"));
					tele1Dlv = checkNull(rsln.getString("TEL1__DLV"));
					tele2Dlv = checkNull(rsln.getString("TEL2__DLV"));
					tele3Dlv = checkNull(rsln.getString("TEL3__DLV"));
					locCodeGit = checkNull(rsln.getString("LOC_CODE__GIT"));
					locCodeCons = checkNull(rsln.getString("LOC_CODE__CONS"));
					locCodeDamg = checkNull(rsln.getString("LOC_CODE__DAMAGED"));
					prcList = checkNull(rsln.getString("PRICE_LIST"));
					prcListClg = checkNull(rsln.getString("PRICE_LIST__CLG"));
					siteCodeBil = checkNull(rsln.getString("SITE_CODE__BIL"));
					currCode = checkNull(rsln.getString("CURR_CODE"));
					exchRate = rsln.getDouble("EXCH_RATE");
					totAmt = rsln.getDouble("TOT_AMT");
					taxAmt = rsln.getDouble("TAX_AMT");
					netAmt = rsln.getDouble("NET_AMT");
					projCode = checkNull(rsln.getString("PROJ_CODE"));
					salePers = checkNull(rsln.getString("SALES_PERS"));
					autoRecpt = checkNull(rsln.getString("AUTO_RECEIPT"));
					avlYn = checkNull(rsln.getString("AVALIABLE_YN"));
					targetWgt = checkNull(rsln.getString("TARGET_WGT"));
					targetVol = checkNull(rsln.getString("TARGET_VOL"));
					custOrdNo = checkNull(rsln.getString("CUST_ORDER__NO"));
					
				}
				rsln.close();
				rsln = null;
				pstmtln.close();
				pstmtln = null;
				
				
				xmlBuff = new StringBuffer();
				
				xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
				xmlBuff.append("<DocumentRoot>");
				xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
				xmlBuff.append("<group0>");
				xmlBuff.append("<description>").append("Group0 description").append("</description>");
				xmlBuff.append("<Header0>");
				xmlBuff.append("<objName><![CDATA[").append("distordamd").append("]]></objName>");  
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
				
				//Detail1....Setting data to DistOrder Amendment from Distorder	
				xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"distordamd\" objContext=\"1\">");  
				xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
				xmlBuff.append("<amd_no><![CDATA[]]></amd_no>");
				xmlBuff.append("<amd_date><![CDATA["+ sdf.format(tranDate).toString() +"]]></amd_date>");
				xmlBuff.append("<site_code><![CDATA["+ siteCode +"]]></site_code>");
				xmlBuff.append("<dist_order><![CDATA["+ disOrdStr +"]]></dist_order>");
				xmlBuff.append("<tran_type_o><![CDATA["+ tranType +"]]></tran_type_o>");
				if(ordDate == null)
				{
					xmlBuff.append("<order_date><![CDATA[]]></order_date>");
					xmlBuff.append("<order_date_o><![CDATA[]]></order_date_o>");
				}
				else
				{
					xmlBuff.append("<order_date><![CDATA["+sdf.format(ordDate).toString()  +"]]></order_date>");
					xmlBuff.append("<order_date_o><![CDATA["+sdf.format(ordDate).toString()  +"]]></order_date_o>");
				}
				xmlBuff.append("<sundry_type_o><![CDATA["+sunType +"]]></sundry_type_o>");
				xmlBuff.append("<sundry_code_o><![CDATA["+sunCode +"]]></sundry_code_o>");
				
				remarksN = "Distribution order generated form Sale order -:"+saleOrd;
				xmlBuff.append("<remarks><![CDATA["+remarksN +"]]></remarks>");
				xmlBuff.append("<remarks_o><![CDATA["+remarks +"]]></remarks_o>");
				xmlBuff.append("<remarks1><![CDATA["+remarks1 +"]]></remarks1>");
				xmlBuff.append("<remarks1_o><![CDATA["+remarks1 +"]]></remarks1_o>");
				xmlBuff.append("<remarks2><![CDATA["+remarks2 +"]]></remarks2>");
				xmlBuff.append("<remarks2_o><![CDATA["+remarks2 +"]]></remarks2_o>");
				xmlBuff.append("<purc_order><![CDATA["+purcOrd +"]]></purc_order>");
				xmlBuff.append("<purc_order_o><![CDATA["+purcOrd +"]]></purc_order_o>");
				xmlBuff.append("<confirmed><![CDATA["+"N" +"]]></confirmed>");
				xmlBuff.append("<purc_order_o><![CDATA["+purcOrd +"]]></purc_order_o>");
				xmlBuff.append("<site_code__ship><![CDATA["+ siteCodeSh   +"]]></site_code__ship>");
				xmlBuff.append("<site_code__ship_o><![CDATA["+ siteCodeSh   +"]]></site_code__ship_o>");
				xmlBuff.append("<site_code__dlv_o><![CDATA["+ siteCodedlv   +"]]></site_code__dlv_o>");
				xmlBuff.append("<trans_mode><![CDATA["+ tranMode   +"]]></trans_mode>");
				xmlBuff.append("<trans_mode_o><![CDATA["+ tranMode   +"]]></trans_mode_o>");
				
				if(shipDate == null)
				{
					xmlBuff.append("<ship_date_o><![CDATA[]]></ship_date_o>");
				}
				else
				{
					xmlBuff.append("<ship_date_o><![CDATA["+ sdf.format(shipDate).toString()   +"]]></ship_date_o>");
				}
				if(dueDate == null)
				{
					xmlBuff.append("<due_date_o><![CDATA[]]></due_date_o>");
				}
				else
				{
					xmlBuff.append("<due_date_o><![CDATA["+ sdf.format(dueDate).toString()   +"]]></due_date_o>");
				}
				
				xmlBuff.append("<policy_no_o><![CDATA["+policyno +"]]></policy_no_o>");
				xmlBuff.append("<cust_code__dlv_o><![CDATA["+custCodedlv +"]]></cust_code__dlv_o>");
				xmlBuff.append("<dlv_to_o><![CDATA["+dlvTo +"]]></dlv_to_o>");
				xmlBuff.append("<dlv_add1_o><![CDATA["+dlvAdd1 +"]]></dlv_add1_o>");
				xmlBuff.append("<dlv_add2_o><![CDATA["+dlvAdd2 +"]]></dlv_add2_o>");
				xmlBuff.append("<dlv_add3_o><![CDATA["+dlvAdd3 +"]]></dlv_add3_o>");
				xmlBuff.append("<dlv_city_o><![CDATA["+dlvCity +"]]></dlv_city_o>");
				xmlBuff.append("<stan_code_o><![CDATA["+stanCode +"]]></stan_code_o>");
				xmlBuff.append("<state_code__dlv_o><![CDATA["+stanCodedlv +"]]></state_code__dlv_o>");
				xmlBuff.append("<count_code__dlv_o><![CDATA["+countCodedlv +"]]></count_code__dlv_o>");
				xmlBuff.append("<dlv_pin_o><![CDATA["+dlvPin +"]]></dlv_pin_o>");
				xmlBuff.append("<tel1__dlv_o><![CDATA["+tele1Dlv +"]]></tel1__dlv_o>");
				xmlBuff.append("<tel2__dlv_o><![CDATA["+tele2Dlv +"]]></tel2__dlv_o>");
				xmlBuff.append("<tel3__dlv_o><![CDATA["+tele3Dlv +"]]></tel3__dlv_o>");
				xmlBuff.append("<loc_code__git_o><![CDATA["+locCodeGit +"]]></loc_code__git_o>");
				xmlBuff.append("<loc_code__cons_o><![CDATA["+locCodeCons +"]]></loc_code__cons_o>");
				xmlBuff.append("<loc_code__damaged_o><![CDATA["+locCodeDamg +"]]></loc_code__damaged_o>");
				xmlBuff.append("<price_list_o><![CDATA["+prcList +"]]></price_list_o>");
				xmlBuff.append("<price_list__clg_o><![CDATA["+prcListClg+"]]></price_list__clg_o>");
				xmlBuff.append("<site_code__bil_o><![CDATA["+siteCodeBil+"]]></site_code__bil_o>");
				xmlBuff.append("<curr_code_o><![CDATA["+currCode+"]]></curr_code_o>");
				xmlBuff.append("<tot_amt_o><![CDATA["+totAmt+"]]></tot_amt_o>");
				xmlBuff.append("<tax_amt_o><![CDATA["+taxAmt+"]]></tax_amt_o>");
				xmlBuff.append("<net_amt_o><![CDATA["+netAmt+"]]></net_amt_o>");
				xmlBuff.append("<proj_code_o><![CDATA["+projCode+"]]></proj_code_o>");
				xmlBuff.append("<sales_pers_o><![CDATA["+salePers+"]]></sales_pers_o>");
				xmlBuff.append("<auto_receipt_o><![CDATA["+autoRecpt+"]]></auto_receipt_o>");
				xmlBuff.append("<avaliable_yn_o><![CDATA["+avlYn+"]]></avaliable_yn_o>");
				xmlBuff.append("<target_wgt_o><![CDATA["+targetWgt+"]]></target_wgt_o>");
				xmlBuff.append("<target_vol_o><![CDATA["+targetVol+"]]></target_vol_o>");
				xmlBuff.append("</Detail1>");
				
				//Detail2....Setting data to DistOrder Amendment det from Distorder_det
				
				sql = "SELECT LINE_NO , DUE_DATE, SALE_ORDER , LINE_NO__SORD, TRAN_ID__DEMAND, SHIP_DATE , ITEM_CODE , QTY_ORDER__ALT, " +
						"UNIT__ALT, CONV__QTY__ALT, QTY_ORDER , RATE, REMARKS, PACK_INSTR, REAS_CODE , UNIT, QTY_SHIPPED , QTY_RECEIVED, " +
						"QTY_CONFIRM, QTY_RETURN, RATE__CLG, OVER_SHIP_PERC, TAX_CLASS, TAX_CHAP, TAX_ENV, TOT_AMT, TAX_AMT, NET_AMT, " +
						"DISCOUNT, CUST_SPEC__NO " +
						"FROM DISTORDER_DET WHERE DIST_ORDER = ? AND  LINE_NO__SORD  IN ("+disOrdLineBuff.toString()+") ";
				
				pstmtln= conn.prepareStatement(sql);
				pstmtln.setString(1, disOrdStr);
				rsln = pstmtln.executeQuery();
				while (rsln.next())
				{
					lineNO = (rsln.getString("LINE_NO"));
					dueDate  = rsln.getTimestamp("DUE_DATE");
					saleOrdet = checkNull(rsln.getString("SALE_ORDER"));
					lineNoSord = (rsln.getString("LINE_NO__SORD"));
					tranIdemag = checkNull(rsln.getString("TRAN_ID__DEMAND"));
					shipDate  = rsln.getTimestamp("SHIP_DATE");
					itemCode = checkNull(rsln.getString("ITEM_CODE"));
					qtyOrdAlt = rsln.getDouble("QTY_ORDER__ALT");
					unitAlt = checkNull(rsln.getString("UNIT__ALT"));
					conQtyAlt = rsln.getDouble("CONV__QTY__ALT");
					qtyOrd = rsln.getDouble("QTY_ORDER");
					rate = rsln.getDouble("RATE");
					remaksDet = checkNull(rsln.getString("REMARKS"));
					packInst = checkNull(rsln.getString("PACK_INSTR"));
					reasCode = checkNull(rsln.getString("REAS_CODE"));
					unit = checkNull(rsln.getString("UNIT"));
					qtyShip = rsln.getDouble("QTY_SHIPPED");
					qtyReceiv = rsln.getDouble("QTY_RECEIVED");
					qtyConf = rsln.getDouble("QTY_CONFIRM");
					qtyReturn = rsln.getDouble("QTY_RETURN");
					rateClg = rsln.getDouble("RATE__CLG");
					ovrShiPer = rsln.getDouble("OVER_SHIP_PERC");
					taxClass = checkNull(rsln.getString("TAX_CLASS"));
					taxChp = checkNull(rsln.getString("TAX_CHAP"));
					taxEnv = checkNull(rsln.getString("TAX_ENV"));
					totAmt = rsln.getDouble("TOT_AMT");
					taxAmt = rsln.getDouble("TAX_AMT");
					netAmt = rsln.getDouble("NET_AMT");
					disCnt = rsln.getDouble("DISCOUNT");
					custSpecNo = checkNull(rsln.getString("CUST_SPEC__NO"));
					
					
					xmlBuff.append("<Detail2 dbID='' domID=\""+lineNo +"\" objName=\"distordamd\" objContext=\"2\">"); 
					xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" updateFlag=\"A\" status=\"N\" />");
					xmlBuff.append("<amd_no><![CDATA[]]></amd_no>");
					xmlBuff.append("<line_no><![CDATA["+lineNo +"]]></line_no>");
					xmlBuff.append("<dist_order><![CDATA["+disOrdStr +"]]></dist_order>");
					xmlBuff.append("<line_no_distord><![CDATA["+lineNO +"]]></line_no_distord>");
					if(dueDate == null)
					{
						xmlBuff.append("<due_date_o><![CDATA[]]></due_date_o>");
					}
					else
					{
						xmlBuff.append("<due_date_o><![CDATA["+ sdf.format(dueDate).toString()   +"]]></due_date_o>");
					}
					xmlBuff.append("<sale_order><![CDATA["+saleOrdet +"]]></sale_order>");
					xmlBuff.append("<line_no__sord_o><![CDATA["+lineNoSord +"]]></line_no__sord_o>");
					if(shipDate == null)
					{
						xmlBuff.append("<ship_date_o><![CDATA[]]></ship_date_o>");
					}
					else
					{
						xmlBuff.append("<ship_date_o><![CDATA["+ sdf.format(shipDate).toString()   +"]]></ship_date_o>");
					}
					xmlBuff.append("<item_code><![CDATA["+itemCode +"]]></item_code>");
					xmlBuff.append("<qty_order__alt_o><![CDATA["+qtyOrdAlt +"]]></qty_order__alt_o>");
					xmlBuff.append("<unit__alt_o><![CDATA["+unitAlt +"]]></unit__alt_o>");
					xmlBuff.append("<conv__qty__alt_o><![CDATA["+conQtyAlt +"]]></conv__qty__alt_o>");
					xmlBuff.append("<qty_order_o><![CDATA["+qtyOrd +"]]></qty_order_o>");
					xmlBuff.append("<qty_order><![CDATA["+qtyOrd +"]]></qty_order>");
					xmlBuff.append("<rate_o><![CDATA["+rate +"]]></rate_o>");
					xmlBuff.append("<rate><![CDATA["+rate +"]]></rate>");
					xmlBuff.append("<remarks_o><![CDATA["+remaksDet +"]]></remarks_o>");
					xmlBuff.append("<remarks><![CDATA["+remaksDet +"]]></remarks>");
					xmlBuff.append("<pack_instr_o><![CDATA["+packInst +"]]></pack_instr_o>");
					xmlBuff.append("<pack_instr><![CDATA["+packInst +"]]></pack_instr>");
					xmlBuff.append("<reas_code_o><![CDATA["+reasCode +"]]></reas_code_o>");
					xmlBuff.append("<unit_o><![CDATA["+unit +"]]></unit_o>");
					
					xmlBuff.append("<qty_confirm_o><![CDATA["+qtyConf +"]]></qty_confirm_o>");
					xmlBuff.append("<qty_shipped_o><![CDATA["+qtyShip +"]]></qty_shipped_o>");
					xmlBuff.append("<qty_received_o><![CDATA["+qtyReceiv +"]]></qty_received_o>");
					xmlBuff.append("<qty_return_o><![CDATA["+qtyReturn +"]]></qty_return_o>");
					xmlBuff.append("<rate__clg_o><![CDATA["+rateClg+"]]></rate__clg_o>");
					xmlBuff.append("<over_ship_perc_o><![CDATA["+ovrShiPer+"]]></over_ship_perc_o>");
					
					xmlBuff.append("<tax_class_o><![CDATA["+taxClass+"]]></tax_class_o>");
					xmlBuff.append("<tax_class><![CDATA["+taxClass+"]]></tax_class>");
					xmlBuff.append("<tax_chap_o><![CDATA["+taxChp+"]]></tax_chap_o>");
					xmlBuff.append("<tax_chap><![CDATA["+taxChp+"]]></tax_chap>"); 
					xmlBuff.append("<tax_env_o><![CDATA["+taxEnv+"]]></tax_env_o>"); 
					xmlBuff.append("<tax_env><![CDATA["+taxEnv+"]]></tax_env>"); 
					
					xmlBuff.append("<tot_amt_o><![CDATA["+totAmt+"]]></tot_amt_o>");
					xmlBuff.append("<tax_amt_o><![CDATA["+taxAmt+"]]></tax_amt_o>");
					xmlBuff.append("<net_amt_o><![CDATA["+netAmt+"]]></net_amt_o>");
					xmlBuff.append("<discount_o><![CDATA["+disCnt+"]]></discount_o>");
					xmlBuff.append("<cust_spec__no_o><![CDATA["+custSpecNo+"]]></cust_spec__no_o>");
					xmlBuff.append("<cust_spec__no><![CDATA["+custSpecNo+"]]></cust_spec__no>");
					
					xmlBuff.append("</Detail2>");
					lineNo++;
				
				}//Inner while
				rsln.close();
				rsln = null;
				pstmtln.close();
				pstmtln = null;
				
				xmlBuff.append("</Header0>");
				xmlBuff.append("</group0>");
				xmlBuff.append("</DocumentRoot>");
				xmlString = xmlBuff.toString();
				System.out.println("@@@@@:::: xmlString:"+xmlBuff.toString());
				//siteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
				siteCode = loginSite;
				System.out.println("site code =="+siteCode);
				errString = saveData(siteCode,xmlString,chgUsr,conn);
				System.out.println("@@@@@: retString:"+errString);
				System.out.println("--retString finished--");
				if (errString.indexOf("Success") > -1)
				{
					System.out.println("@@@@@@: Success"+errString);
					//conn.commit();
					//errString = "";
				}
				else
				{
					System.out.println("@@@@@@ UnSuccess" + errString + "]");	
					conn.rollback();
					return errString;
				}
			}
		/*		
			}//while
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;*/
			
			
		} 
		catch (Exception e)
		{
			System.out.println("Exception :distOrderamd() ::" + e.getMessage() + ":");
			errCode = e.getMessage();
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
				if(pstmtln != null)
				{
					pstmtln.close();
					pstmtln = null;					
				}
				if(rs != null)
				{
					rs.close();
					rs = null;
				}
				if(rsln != null)
				{
					rsln.close();
					rsln = null;
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
	
	private String saveData(String siteCode,String xmlString,String chgUsr,Connection conn) throws ITMException
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
			authencate[0] = chgUsr;
			authencate[1] = "";
			//System.out.println("xmlString to masterstateful [" + xmlString + "]");
			retString = masterStateful.processRequest(authencate, siteCode, true, xmlString,true,conn);
			System.out.println("--retString --"+retString);
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
	
	private String checkNull(String str)
	{
		if(str == null)
		{
			return "";
		}
		else
		{
			return str.trim() ;
		}
	}
	private int checkInt(String str)
	{
		if(str == null || str == "")
		{
			return 0;
		}
		else
		{
			return Integer.parseInt(str.trim()) ;
		}
	}
	
	private String getLineNewNo(String lineNo)
	{
		lineNo = lineNo.trim();
		System.out.println("lineNo::["+lineNo+"]");
		String lenStr = "  " + lineNo ;
		System.out.println("lenStr::["+lenStr+"]");
		String lineNoNew = lenStr.substring(lenStr.length() - 3, lenStr.length());
	
		System.out.println("lineNonew::["+lineNoNew+"]");
		return lineNoNew;
	}
	
}