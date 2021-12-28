package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.BaseLogger;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.fin.FinCommonInvAcct;
import ibase.webitm.ejb.fin.InvAcct;
import ibase.webitm.ejb.fin.adv.MiscValConf;
import ibase.webitm.ejb.mfg.InvDemSuppTraceBean;
import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.utility.TransIDGenerator;
import ibase.webitm.ejb.fin.MiscDrCrRcpConf;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import ibase.webitm.ejb.dis.adv.DissIssuePosConf;
import javax.ejb.Stateless;

import com.itextpdf.text.log.SysoCounter;
//import javax.ejb.SessionBean;
// added for ejb3

@Stateless // added for ejb3
//public class DistOrderRcpConf extends ActionHandlerEJB //implements SessionBean
public class DistOrderRcpConf extends ActionHandlerEJB implements DistOrderRcpConfLocal, DistOrderRcpConfRemote
{

	DistCommon distCommon =new DistCommon();
	CommonConstants commonConstants = new CommonConstants();
	E12GenericUtility genericUtility = new E12GenericUtility();
	//Modified by Rohini Telang on 03/03/2021[Start]
	FileOutputStream fos1 = null;
	java.util.Date startDate = new java.util.Date(System.currentTimeMillis());
	Calendar calendar = Calendar.getInstance();
	String startDateStr = null;
	ArrayList<Log> erroLogSordItme=new ArrayList<Log>();
	String logMsg = "";
	String strToWrite="",strToWriteHead="";
	//Modified by Rohini Telang on 03/03/2021[End]
	/*public void ejbCreate() throws RemoteException,CreateException{
    }
    public void ejbRemove(){

    }
    public void ejbActivate(){

    }
    public void ejbPassivate(){

    }
    public void setSessionContext(SessionContext se)
	{
    }*/
	//Changed by wasim on 18-07-2016 to add confirm method [START]
	public String confirm(String tranID, String xtraParams, String forcedFlag) throws RemoteException,ITMException
	{

		String  retString = null;
		Connection conn = null;

		try
		{
			System.out.println("connection in distrcpconf 111::::"+conn);	
			retString = confirm(tranID, xtraParams, forcedFlag, conn );
			System.out.println("retString1::::"+retString);
		}

		catch(Exception e)
		{
			System.out.println("Exception :DistIssueConfirmActEJB :actionHandler :" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}

		return retString;
	}
	//Changed by wasim on 18-07-2016 to add confirm method [END]

	public String actionHandler(String tranID, String xtraParams, String forcedFlag) throws RemoteException,ITMException
	{

		String  retString = null;
		boolean isConn = false;
		Connection conn = null;

		try
		{
			System.out.println("connection in distrcpconf 222::::"+conn);	

			retString = confirm(tranID, xtraParams, forcedFlag, conn);
			System.out.println("retString2::::"+retString);
		}

		catch(Exception e)
		{
			System.out.println("Exception :DistIssueConfirmActEJB :actionHandler :" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}

		return retString;
	}
	//public String actionConfirm(String tranID,String xtraParams, String forcedFlag) throws Exception,ITMException
	public String confirm(String tranID,String xtraParams, String forcedFlag,Connection conn) throws RemoteException,ITMException
	{
		String errString = null;
		//Connection conn = null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		boolean isError = false;
		boolean connStatus = false;
		try
		{
			itmDBAccessEJB = new ITMDBAccessEJB();
			System.out.println("Inside conn---"+conn);
			System.out.println("connection in distrcpconf 333::::"+conn);	

			if (conn == null)
			{
				ConnDriver connDriver = new ConnDriver();
				//conn = connDriver.getConnectDB("DriverITM");
				conn = getConnection();
				conn.setAutoCommit(false);
				connDriver = null;
				connStatus = true;
			}

			System.out.println("connection in distrcpconf 444::::"+conn);	
			errString = actionConfirm(tranID,xtraParams, forcedFlag,conn);
			System.out.println("errString errString::::["+errString+"]");

			//if(errString !=null && errString.indexOf("Error") != -1 && errString.indexOf("VTCONF") == -1)
			if(errString !=null && errString.indexOf("Error") != -1 && (errString.indexOf("CONFSUCCES") == -1 || errString.indexOf("VTCONF") > -1 || errString.indexOf("SEND_SUCCESS") > -1))//chages for gst recovery on 5 april 21 by monika salla to show msg on confirmation of verify password
			{
				//Modified by Rohini T on 17/03/2021[Start]
				//System.out.println("Inside VTCONF");
				System.out.println("Inside CONFSUCCES");
				strToWrite = createPostLog("","","","","",0,errString);
				fos1.write(strToWrite.getBytes());
				//Modified by Rohini T on 17/03/2021[End]	
				isError = true;
			}
		}
		catch(Exception ie)
		{
			isError = true;
			System.out.println("ITMException : "+ie);
			try{conn.rollback();}catch(Exception t){}
			ie.printStackTrace();
			errString = itmDBAccessEJB.getErrorString("","VTDESNCONF","","",conn);
			//Modified by Rohini T on [01/03/2021][Start]
			if(errString != null && errString.trim().length() > 0)
			{
				strToWrite = createPostLog("","","","","",0,errString);
				try {
					fos1.write(strToWrite.getBytes());
				} catch (IOException e) {
					e.printStackTrace();
				}
				isError = true;
				return errString;
			}
			//Modified by Rohini T on [01/03/2021][End]
			System.out.println("Returnng String From DistOrderRcpConfEJB :"+errString);
		}
		finally
		{
			System.out.println("isError in Finally"+isError);
			try
			{
				System.out.println("connection in distrcpconf 444::::"+connStatus);	
				if(connStatus)
				{	
					System.out.println("connection in distrcpconf 555::::["+conn+" ] isError ["+isError);	
					if (conn != null && !isError)
					{
						System.out.println("Transaction Commit::::");
						conn.commit();
					}
					else
					{
						System.out.println("Transaction Rollback::::");
						conn.rollback();
					}
					conn.close();
					conn = null;
				}

				/*   if (conn != null) 
				{
					//if (connStatus)
					if (isError && connStatus)
					{
						System.out.println("actionDistIssueConf Local connection rolledback");
						conn.rollback();
					} 
					else if (!isError && connStatus)
					{
						System.out.println("actionDistIssueConf Local connection committed");
						conn.commit();
						//retString = itmDBAccessEJB.getErrorString("", "VMCPSUCC  ", "", "", conn);
					}
					if (!connStatus)
					{
						System.out.println("actionDistIssueConf not Local connection so not commit or rollback");
					}
				}*/
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
		return errString;
	}
	public String actionConfirm(String tranID,String xtraParams, String forcedFlag,Connection conn) throws Exception,ITMException
	{
		//return actionConfirm(tranID,xtraParams, forcedFlag,"N", conn);
		System.out.println("connection in distrcpconf 666::::["+conn+" ] ");
		return actionConfirm(tranID, xtraParams, forcedFlag, "Y", conn);// Changed by Anagha
	}
	public String actionConfirm(String tranID,String xtraParams, String forcedFlag,String verifyPassword, Connection conn) throws Exception,ITMException
	{
		//Connection conn = null;
		PreparedStatement pstmt = null,pstmt1 = null;
		ResultSet rs = null,rs1 = null;
		PreparedStatement pstmtUpd = null;
		String sql = "",qcLockReqd = "",status = "",loginSiteCode = "",tranIdHoldTrace = "";
		String tranIdIss = "",siteCode = "",locCodeGit = "",siteCodeShip = "";
		String qcReqd = "",gpNo = "",orderType = "",tranType = "";
		String 	gengpno ="",errCode ="";
		String distOrder ="",itemCode ="",unit ="",locCode = "", packCode = "";
		String lotNo ="",lotSl = "",siteCodeMfg = "", packInstr ="",batchNo = "",grade ="";
		String dimension ="",suppCodeMfg = "",itemSer = "",grpCode ="";
		String acctCodeIss ="",	cctrCodeIss ="",acctDetrDistTtype = "",tranTypeParent ="";
		double quantity =0,rate =0,potencyPerc = 0,grossWeight =0,netWeight =0,holdQtyNew = 0;
		double tareWeight =0,netAmt =0;
		Timestamp mfgDate =null,expDate =null;
		String locCodeIss ="",invStat ="",acctCodeInv ="",unitAlt ="",quarLock = "";
		String cctrCodeInv ="",acctCodeOh ="",cctrCodeOh ="",acctDetrTtype ="",lockCode = "", holdLock = "";
		//Modified By Umakanta Das on 17-OCT-2016[]Start
		double batchSizeApprv = 0, batchSize = 0;
		String  keyString = "", remarks = "", userId = "", termId = "", jobWorkType = "", result = "", quarLockCode="";
		String siteCodeDRcp = "",  itemCodeDRcp = "", locCodeDRcp = "", lotNoDRcp = "", lotSlDRcp = "";
		double holdQty = 0;
		String bondTaxGroup = "", bondTaxArray[]=null,EOU = "";
		java.sql.Timestamp chgDate = null;
		java.util.Date date = null;
		int lineNoInv = 0, updCnt = 0, cnt = 0,countXfr = 0;
		//Modified By Umakanta Das on 17-OCT-2016[]Start
		double convQtyStduom =0,recoAmount =0;
		Timestamp retestDate =null;
		long noArt =0;
		double grossRate =0,palletWt =0;

		String errString = "";
		String errorString = "";// Added By Anagha R 
		boolean lbQcLock = false;
		Timestamp tranDate = null;
		HashMap stkUpdMap = null;
		String tranDateStr ="";
		int upd =0;
		//lineNo =0,lineNoDistOrder =0;
		String lineNo ="";
		int lineNoDistOrder = 0;
		ITMDBAccessEJB itmDBAccessEJB = null;
		InvHoldGen invHoldGen = new InvHoldGen();//Modified By Umakanta Das On 17-OCT-2016
		ArrayList arrayList =new ArrayList();
		List<HashMap<String, String>> holdQtyList = new ArrayList<HashMap<String, String>>();// Modified By Umakanta Das On 17-OCT-2016
		ArrayList<HashMap<String, String>> stockList = new ArrayList<HashMap<String, String>>();// Modified By Umakanta Das On 17-OCT-2016
		HashMap  stockQtyMap = new HashMap();// Modified By Umakanta Das on 17-OCT-2016
		FinCommon finCommon = null;
		//GenericUtility genericUtility = null;
		ActionHandlerEJB itmDBAccessLocal =null;
		FinCommonInvAcct finCommonInvAcct = null;
		int count=0;
		//added by saurabh
		int bondTaxAmt=0,bondValue=0,bondNo=0,bankGuarantee=0,bondValueFinal=0;
		String holdTranId="", lockCodeNew = "";
		List<HashMap<String, String>> stockIssList = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> stock_iss =new HashMap<String, String>();
		//added by saurabh
		Double holdQtyIss = 0.0;
		String siteCodeIss = "";
		String itemCodeIss ="";
		String locCodeIss1 = "";
		String lotNoIss ="";
		String lotSlIss ="";
		String msgType= "";
		int llCnt=0;
		/**Added by Pavan Rane 24dec19 start[to update demand/supply in summary table(RunMRP process) related changes]*/
		Timestamp dueDateOrd = null;
		String siteCodeDlv = "";
		/**Added by Pavan Rane 24dec19 start[to update demand/supply in summary table(RunMRP process) related changes]*/
		String confirmed="",confPasswd="",lsPwdVerified="",sql1="",lsLedgPostConf="",loginEmpCode="",saleOrder="",lineNoSord="",itemCodeOrd="",expLev="";
		double ldQty=0.00,ldBalQty=0.00,qtytoballocated=0.00,qtyAlloc=0.00,lcAllocatedqty=0.00;
		boolean lbAllocated=false,isError=false;

		String recoverCsaGst = "";// Added by Anagha
		String recoverTranType="";//added by monika salla on 31 dec 20
		String siteType = "";
		String creatInvOth = "";
		String creatInvOthlist = "";
		String otherSite = "";
		boolean connStatus = false;
		String createLog=null,logFileInit="";
		FinCommon fcom = new FinCommon();
		try{        	


			System.out.println("in dr confirm::::");
			System.out.println("connection in distrcpconf 666::::["+conn+" ] verify password ["+verifyPassword);
			finCommon = new FinCommon();
			// finCommonInvAcct = new FinCommonInvAcct();

			itmDBAccessEJB = new ITMDBAccessEJB();
			//genericUtility = GenericUtility.getInstance();
			E12GenericUtility genericUtility = new E12GenericUtility();
			System.out.println("@V@ xtraParams :- ["+xtraParams+"]");
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			termId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");

			loginEmpCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			System.out.println("@V@ loginEmpCode :- ["+loginEmpCode+"]");
			InvDemSuppTraceBean invDemSupTrcBean = new InvDemSuppTraceBean();
			HashMap demandSupplyMap = new HashMap();
			//Added by sarita to get Current Date with time[00:00:00.0] on 12 JUN 18 [START]
			SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
			java.util.Date currentDate = new java.util.Date();
			Timestamp newsysDate = java.sql.Timestamp.valueOf(sdf1.format(currentDate) + " 00:00:00.0");
			System.out.println("newsysDate is : ["+newsysDate+"]");
			//Added by sarita to get Current Date with time[00:00:00.0] on 12 JUN 18 [END]

			stkUpdMap = new HashMap();
			System.out.println("Getting Connection["+conn+"]");
			System.out.println("connection in distrcpconf 777::::["+conn+" ] ");

			if (conn == null)
			{
				/*	ConnDriver connDriver = new ConnDriver();
	            conn = connDriver.getConnectDB("DriverITM");
				//conn = getConnection();
	            conn.setAutoCommit(false);
                connDriver = null;*/

				conn = getConnection();
				conn.setAutoCommit(false);
				connStatus = true;
			}
			//Modified by Rohini Telang on 03/03/2021[Start]
			System.out.println("connection in distrcpconf 888::::["+conn+" ] connStatus ["+connStatus);
			createLog = itmDBAccessEJB.getEnvDis("999999", "CREATE_POST_LOG_FILE", conn);
			if( "NULLFOUND".equalsIgnoreCase(createLog) )
			{
				createLog ="N";
			}
			if("Y".equalsIgnoreCase(createLog) )
			{

				logFileInit=intializingLog("distrcp_conf",tranID);
				strToWrite="";
				strToWrite=strToWrite + strToWriteHead;
			}
			//Modified by Rohini Telang on 03/03/2021[End]

			// VALLABH KADAM gbf_post_logic validations [09/NOV/2017] START

			System.out.println("@V@ First half");
			sql="select count(*) as llCnt from distord_rcpdet where tran_id = ?";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, tranID);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				llCnt=rs.getInt("llCnt");
			}
			rs.close();rs=null;
			pstmt.close();pstmt=null;
			System.out.println("@V@ llCnt 270:- ["+llCnt+"]");
			if(llCnt==0)
			{				
				errString = itmDBAccessEJB.getErrorString("","VTNODET1","BASE","",conn);
				//Modified by Rohini T on [01/03/2021][Start]
				if(errString != null && errString.trim().length() > 0)
				{
					logMsg = "Record not found in Detail.";
					strToWrite = createPostLog(lineNo,itemCode,locCode,lotNo,lotSl,quantity,logMsg);
					fos1.write(strToWrite.getBytes());
					isError = true;
					return errString;
				}
				//Modified by Rohini T on [01/03/2021][End]
			}

			if("mssql".equalsIgnoreCase(commonConstants.DB_NAME))
			{
				sql="select confirmed, conf_passwd, site_code, qc_reqd from distord_rcp (updlock) where tran_id = ?";
			}
			else if("db2".equalsIgnoreCase(commonConstants.DB_NAME) || "mysql".equalsIgnoreCase(commonConstants.DB_NAME))
			{
				sql="select confirmed, conf_passwd, site_code, qc_reqd from distord_rcp where tran_id = ? for update";
			}
			else
			{
				sql="select confirmed, conf_passwd, site_code, (case when qc_reqd is null then 'N' else qc_reqd end) as qc_reqd"
						+ " from distord_rcp where tran_id = ? for update nowait";
				//				sql="select confirmed, conf_passwd, site_code, (case when qc_reqd is null then 'N' else qc_reqd end) as qc_reqd"
				//						+ " from distord_rcp where tran_id = ?";
			}
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, tranID);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				confirmed=checkNullAndTrim(rs.getString("confirmed"));
				confPasswd=checkNullAndTrim(rs.getString("conf_passwd"));
				siteCode=checkNullAndTrim(rs.getString("site_code"));
				qcReqd=checkNullAndTrim(rs.getString("qc_reqd"));
			}
			else  
			{  				
				rs.close();
				pstmt.close();				
				errString = itmDBAccessEJB.getErrorString("","VTLCKERR","","",conn);
				//Modified by Rohini T on [01/03/2021][Start]
				if(errString != null && errString.trim().length() > 0)
				{
					logMsg = "System is not able to lock the record, please try after sometime";
					strToWrite = createPostLog(lineNo,itemCode,locCode,lotNo,lotSl,quantity,logMsg);
					fos1.write(strToWrite.getBytes());
					isError = true;
					return errString;
				}
				//Modified by Rohini T on [01/03/2021][End]
			}

			rs.close();rs=null;
			pstmt.close();pstmt=null;
			System.out.println("@V@ confirmed 300:- ["+confirmed+"]");
			System.out.println("@V@ confPasswd 301:- ["+confPasswd+"]");
			System.out.println("@V@ siteCode 302:- ["+siteCode+"]");
			System.out.println("@V@ qcReqd 303:- ["+qcReqd+"]");
			qcReqd=qcReqd==null || qcReqd.trim().length()==0?"N":qcReqd.trim();

			if("Y".equalsIgnoreCase(confirmed))
			{
				errString = itmDBAccessEJB.getErrorString("","VTDIST15","BASE","",conn);
				//Modified by Rohini T on [01/03/2021][Start]
				if(errString != null && errString.trim().length() > 0)
				{
					logMsg = "Distribution Receipt already confirmed";
					strToWrite = createPostLog(lineNo,itemCode,locCode,lotNo,lotSl,quantity,logMsg);
					fos1.write(strToWrite.getBytes());
					isError = true;
					return errString;
				}
				//Modified by Rohini T on [01/03/2021][End]
			}

			if(confPasswd!=null && confPasswd.trim().length()>0 && "N".equalsIgnoreCase(verifyPassword))
			{
				/**UnComment By Onkar Rane On 20/03/2018 for Password Validation**/


				errString = itmDBAccessEJB.getErrorString("","VTPASS1","BASE","",conn);
				//Modified by Rohini T on [01/03/2021][Start]

				if(errString != null && errString.trim().length() > 0)
				{
					logMsg = "Invalid Password entered";
					strToWrite = createPostLog(lineNo,itemCode,locCode,lotNo,lotSl,quantity,logMsg);
					fos1.write(strToWrite.getBytes());
					isError = true;
					return errString;
				}
				//Modified by Rohini T on [01/03/2021][End]


				/**Closed...**/
				//				lsPwdVerified = distCommon.getDisparams("999999","passwordVerified",conn);
				//lsPwdVerified = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "passwordVerified");
				//System.out.println("@V@ lsPwdVerified 316:- ["+lsPwdVerified+"]");
				// VALLABH KADAM [20/NOV/2017] not required after source review START
				//				if(lsPwdVerified==null || lsPwdVerified.trim().length()==0 || !"Y".equalsIgnoreCase(lsPwdVerified))
				//				{
				//					errString = itmDBAccessEJB.getErrorString("","VTPASS1","BASE","",conn);
				//					return errString;
				//				}
				// VALLABH KADAM [20/NOV/2017] not required after source review END
			}

			sql="select dist_order,item_code,sum(quantity) as ldQty from distord_rcpdet where  tran_id = ? group by dist_order,item_code";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, tranID);
			rs=pstmt.executeQuery();
			while(rs.next())
			{			
				distOrder = checkNullAndTrim(rs.getString("dist_order"));
				itemCode= checkNullAndTrim(rs.getString("item_code"));
				ldQty=rs.getDouble("ldQty");

				System.out.println("@V@ distOrder 334:- ["+distOrder+"]");
				System.out.println("@V@ itemCode 335:- ["+itemCode+"]");
				System.out.println("@V@ ldQty 336:- ["+ldQty+"]");

				sql1="select sum( (qty_confirm + (qty_confirm * (case when OVER_SHIP_PERC is null then 1 when OVER_SHIP_PERC = 0 then 1 else OVER_SHIP_PERC end ) / 100 ) ) - qty_received) as ldBalQty"
						+ " from distorder,distorder_det"
						+ " where distorder.dist_order = distorder_det.dist_order"
						+ " and   distorder_det.dist_order = ?"
						+ " and   distorder_det.item_code = ?"
						+ " and   distorder.confirmed = 'Y'";
				pstmt1=conn.prepareStatement(sql1);
				pstmt1.setString(1, distOrder);
				pstmt1.setString(2, itemCode);
				rs1=pstmt1.executeQuery();
				if(rs1.next())
				{
					ldBalQty=rs1.getDouble("ldBalQty");  
					System.out.println("@V@ ldBalQty 351:- ["+ldBalQty+"]");
					if(ldQty > ldBalQty)
					{
						errString = itmDBAccessEJB.getErrorString("","VTQTYRC","BASE","",conn);
						//Modified by Rohini T on [01/03/2021][Start]
						if(errString != null && errString.trim().length() > 0)
						{
							logMsg = "The received quantity exceeds order quantity";
							strToWrite = createPostLog(lineNo,itemCode,locCode,lotNo,lotSl,quantity,logMsg);
							fos1.write(strToWrite.getBytes());
							isError = true;
							return errString;
						}
						//Modified by Rohini T on [01/03/2021][End]
					}
				}
				rs1.close();rs1=null;
				pstmt1.close();pstmt1=null;
			}
			rs.close();rs=null;
			pstmt.close();pstmt=null;

			sql="select count(1) as llQcreqdCtr from  distord_rcpdet a, item b where a.item_code = b.item_code and   a.tran_id   = ? and   b.qc_reqd   = 'Y'";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, tranID);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				llCnt=rs.getInt("llQcreqdCtr");
			}
			rs.close();rs=null;
			pstmt.close();pstmt=null;
			System.out.println("@V@ llCnt 374:- ["+llCnt+"]");
			if(!"Y".equalsIgnoreCase(qcReqd) && llCnt>0)
			{
				//Commented by Varsha V and added below code for checking msgType
				//errString = itmDBAccessEJB.getErrorString("","VTQCREQD","BASE","",conn);
				// errString;
				msgType = checkNull(errorType(conn,"VTQCREQD"));

				if(msgType.trim().length()==0 || msgType.trim().equalsIgnoreCase("E"))
				{
					errString = itmDBAccessEJB.getErrorString("","VTQCREQD","BASE","",conn);
					//Modified by Rohini T on [01/03/2021][Start]
					if(errString != null && errString.trim().length() > 0)
					{
						logMsg = "QC reqd is Yes for the Item and in the header QC reqd is  No.";
						strToWrite = createPostLog(lineNo,itemCode,locCode,lotNo,lotSl,quantity,logMsg);
						fos1.write(strToWrite.getBytes());
						isError = true;
						return errString;
					}
					//Modified by Rohini T on [01/03/2021][End]
				}
				//Ended by Varsha V and added below code for checking msgType
			}			
			sql="select	(case when ledg_post_conf is null then 'N' else ledg_post_conf end) as lsLedgPostConf from transetup where tran_window = 'w_dist_receipt'";
			pstmt=conn.prepareStatement(sql);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				lsLedgPostConf=checkNullAndTrim(rs.getString("lsLedgPostConf"));
			}
			rs.close();rs=null;
			pstmt.close();pstmt=null;
			System.out.println("@V@ lsLedgPostConf 390:- ["+lsLedgPostConf+"]");
			if("Y".equalsIgnoreCase(lsLedgPostConf))
			{
				//commented and added by sarita to set tran_date as current date with time[00:00:00.0] on 12 JUN 18 [START]
				//sql="update distord_rcp set tran_date = SYSDATE where tran_id =?";
				sql="update distord_rcp set tran_date = ? where tran_id =?";		
				pstmt=conn.prepareStatement(sql);
				pstmt.setTimestamp(1, newsysDate);
				pstmt.setString(2, tranID);				
				//pstmt.setString(1, tranID);
				//commented and added by sarita to set tran_date as current date with time[00:00:00.0] on 12 JUN 18 [END]
				llCnt=pstmt.executeUpdate();
				pstmt.close();pstmt=null;
				System.out.println("@V@ Update cnt :- ["+llCnt+"]");
			}

			if(loginEmpCode==null || loginEmpCode.trim().length()==0)
			{
				errString = itmDBAccessEJB.getErrorString("","EMPAPRV","BASE","",conn);
				//Modified by Rohini T on [01/03/2021][Start]
				if(errString != null && errString.trim().length() > 0)
				{
					logMsg = "The user must have a valid employee code in order to approve any data";
					strToWrite = createPostLog(lineNo,itemCode,locCode,lotNo,lotSl,quantity,logMsg);
					fos1.write(strToWrite.getBytes());
					isError = true;
					return errString;
				}
				//Modified by Rohini T on [01/03/2021][End]
			}
			else
			{
				sql="update distord_rcp set emp_code__aprv = ? where tran_id =?";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1, loginEmpCode);
				pstmt.setString(2, tranID);
				llCnt=pstmt.executeUpdate();
				pstmt.close();pstmt=null;
				System.out.println("@V@ Update cnt :- ["+llCnt+"]");
			}

			// VALLABH KADAM gbf_post_logic validations [09/NOV/2017] END

			SimpleDateFormat sdf = new  SimpleDateFormat(genericUtility.getApplDateFormat());
			sql = " select	 tran_id__iss,	site_code, loc_code__git, tran_date, "
					+" site_code__ship, ( case when qc_reqd is null then 'N' else qc_reqd end ) qc_reqd, "
					+" gp_no	, order_type  ,tran_type "
					+" from distord_rcp  where tran_id = ?";
			//System.out.println("SQL : "+sql);
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,tranID);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				tranIdIss = rs.getString("tran_id__iss");
				siteCode = rs.getString("site_code");
				locCodeGit = rs.getString("loc_code__git");
				System.out.println("manohar locCodeGit : "+locCodeGit);
				tranDate = rs.getTimestamp("tran_date");
				siteCodeShip = rs.getString("site_code__ship");
				qcReqd = rs.getString("qc_reqd");
				gpNo = rs.getString("gp_no");
				orderType = rs.getString("order_type");
				tranType = rs.getString("tran_type");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			count = 0;

			gengpno  = distCommon.getDisparams("999999","STN_RCP_GPNO",conn);
			if("Y".equalsIgnoreCase(gengpno.trim()))
			{
				if(gpNo == null || gpNo.trim().length() ==0 )
				{
					tranDateStr = sdf.format(tranDate);
					String xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
					xmlValues = xmlValues + "<Header></Header>";
					xmlValues = xmlValues + "<Detail1>";
					xmlValues = xmlValues +	"<tran_id></tran_id>";
					xmlValues = xmlValues + "<site_code>"+siteCode.trim()+"</site_code>";
					xmlValues = xmlValues +  "<tran_date>"+tranDateStr.trim()+"</tran_date>";
					xmlValues = xmlValues +  "<tran_type>"+orderType.trim()+"</tran_type>";
					xmlValues = xmlValues + "</Detail1></Root>";
					gpNo = generateTranId("stn_rcp_gpno",xmlValues,conn);//function to generate NEW transaction id
					System.out.println("gpNo......"+gpNo);
					if(gpNo == null || gpNo.trim().length() ==0)
					{
						errCode = "VTTRANID";
						System.out.println("errcode......"+errCode);
						errString = itmDBAccessEJB.getErrorString("","VTTRANID","BASE","",conn);
						//Modified by Rohini T on [01/03/2021][Start]
						if(errString != null && errString.trim().length() > 0)
						{
							logMsg = "Selected transaction not exists";
							strToWrite = createPostLog(lineNo,itemCode,locCode,lotNo,lotSl,quantity,logMsg);
							fos1.write(strToWrite.getBytes());
							isError = true;
							return errString;
						}
						//Modified by Rohini T on [01/03/2021][End]
					}
					else
					{
						sql =" update distord_rcp  set 	gp_no = ? where tran_id = ? " ;
						//System.out.println("sql-------------"+sql);
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,gpNo);
						pstmt.setString(2,tranID);
						upd = pstmt.executeUpdate();
						if(upd == 1)
						{
							System.out.println("distord_rcp update-------------"+upd);
						}
						rs.close();
						pstmt.close();
						pstmt=null;
						rs=null;
					}
				}
			}
			//Modified By Umakanta Das on 17-OCT-2016[]Start
			sql = "Select (case when eou is null then 'N' else eou end) eou From site Where site_code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, siteCode);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				EOU = rs.getString("eou");
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;


			jobWorkType = distCommon.getDisparams("999999", "JOBWORK_TYPE", conn);
			if (jobWorkType == null)
			{
				jobWorkType = "";
			}

			if (! "".equalsIgnoreCase( jobWorkType ) )
			{
				bondTaxGroup = distCommon.getDisparams( "999999", "B17_BOND_TAX_GROUP", conn );
				if ( bondTaxGroup != null && bondTaxGroup.trim().length() > 0 )
				{
					bondTaxArray = bondTaxGroup.split(",");
					String orderNoTemp = "";
					bondTaxGroup = "";
					for ( int ctr = 0; ctr < bondTaxArray.length; ctr++ )
					{
						orderNoTemp = bondTaxArray[ctr];
						bondTaxGroup = bondTaxGroup + "'".concat(orderNoTemp).concat("',");
					}
				}
			}
			/*uomRound = distCommon.getDisparams("999999", "UOM_ROUND", conn);
			if ("NULLFOUND".equals(uomRound))
			{
				errString = itmDBAccessLocal.getErrorString("", "VTUOMVARPARM", "");
				return errString;
			}

			if ("Y".equals(EOU))
			{
				bondTaxGroup = distCommon.getDisparams("999999", "B17_BOND_TAX_GROUP", conn);
				if (bondTaxGroup != null && bondTaxGroup.trim().length() > 0)
				{
					bondTaxArray = bondTaxGroup.split(",");
					String orderNoTemp = "";
					bondTaxGroup = "";
					for (int ctr = 0; ctr < bondTaxArray.length; ctr++)
					{
						orderNoTemp = bondTaxArray[ctr];
						bondTaxGroup = bondTaxGroup + "'".concat(orderNoTemp).concat("',");
					}
				}
			}*/

			//Modified By Umakanta Das on 17-OCT-2016[]End

			sql ="select	line_no	,	dist_order	,line_no_dist_order,item_code, "
					+" quantity	,	unit	, 	loc_code	, 	pack_code	, "
					+" rate	, 	lot_no	, 	lot_sl		, 	mfg_date	, "
					+" exp_date	, 	site_code__mfg	, 	potency_perc	, 	gross_weight, "
					//Modified By Umakanta Das on 17-OCT-2016
					//+" net_weight,	tare_weight	, 	pack_instr	, 	batch_no	,"
					+" net_weight,	tare_weight	, 	pack_instr	, 	batch_no	, batch_size ,"
					+" grade	, 	dimension	, 	supp_code__mfg 	,	net_amt, no_art "
					+ " from 	distord_rcpdet where tran_id = ? order by line_no ";
			//System.out.println("sql-------------"+sql);
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,tranID);
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				HashMap<String,String>  holdQtyMap = new HashMap();// Modified By Umakanta Das on 17-OCT-2016
				lineNo=rs.getString("line_no");
				distOrder=rs.getString("dist_order");
				lineNoDistOrder=rs.getInt("line_no_dist_order");
				itemCode=rs.getString("item_code");
				quantity=rs.getDouble("quantity");
				unit=rs.getString("unit");
				locCode=rs.getString("loc_code");
				packCode=rs.getString("pack_code");
				rate=rs.getDouble("rate");
				lotNo=rs.getString("lot_no");
				lotSl=rs.getString("lot_sl");
				mfgDate=rs.getTimestamp("mfg_date");
				expDate=rs.getTimestamp("exp_date");
				siteCodeMfg=rs.getString("site_code__mfg");
				potencyPerc=rs.getDouble("potency_perc");
				grossWeight=rs.getDouble("gross_weight");
				netWeight=rs.getDouble("net_weight");
				tareWeight=rs.getDouble("tare_weight");
				packInstr=rs.getString("pack_instr");
				batchNo=rs.getString("batch_no");
				batchSize = rs.getDouble("batch_size");
				grade=rs.getString("grade");
				dimension=rs.getString("dimension");
				suppCodeMfg=rs.getString("supp_code__mfg");
				netAmt=rs.getDouble("net_amt");
				noArt=rs.getLong("no_art");

				// Modified By umakanta Das on 17-OCT-2016[To check QC Required N]Start
				if ("N".equals(qcReqd))
				{
					if (batchSize > 0)//Need to Ask Manoj 15=OCT-2016
					{
						sql = "select batch_size from batchsize_aprv where item_code	= ? and site_code__mfg	= ? and eff_from <= ?  and valid_upto >= ? and confirmed = 'Y' ";
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1, itemCode);
						//pstmt1.setString(2, siteCodeMfg);//Commented by Manoj dtd 19/10/2016 sitecode to be set instead of siteCodeMfg
						pstmt1.setString(2, siteCode);
						pstmt1.setTimestamp(3, tranDate);
						pstmt1.setTimestamp(4, tranDate);
						rs1 = pstmt1.executeQuery();
						if (rs1.next())
						{
							batchSizeApprv = rs1.getDouble("batch_size");
						}

						pstmt1.close();
						pstmt1 = null;
						rs1.close();
						rs1 = null;

						if (batchSize > batchSizeApprv)
						{
							sql = "select key_string from transetup where upper(tran_window) = 'W_INV_HOLD'";
							pstmt1 = conn.prepareStatement(sql);
							rs1 = pstmt1.executeQuery();
							if (rs1.next())
							{
								keyString = rs1.getString("key_string");
								pstmt1.close();
								pstmt1 = null;
								rs1.close();
								rs1 = null;
							} 
							else
							{
								pstmt1.close();
								pstmt1 = null;
								rs1.close();
								rs1 = null;
								sql = "select key_string from transetup  where upper(tran_window) = 'GENERAL'";
								pstmt1 = conn.prepareStatement(sql);
								rs1 = pstmt1.executeQuery();
								if (rs1.next())
								{
									keyString = rs1.getString("key_string");
								}
								pstmt1.close();
								pstmt1 = null;
								rs1.close();
								rs1 = null;
							}
							// tranDate = getCurrdateAppFormat();
							String xmlValues = "", tempTranId = "";
							xmlValues = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
							xmlValues = xmlValues + "<Header></Header>\r\n";
							xmlValues = xmlValues + "<Detail1>\r\n";
							xmlValues = xmlValues + "<tran_id></tran_id>\r\n";
							xmlValues = xmlValues + "<site_code>" + siteCode + "</site_code>\r\n";
							xmlValues = xmlValues + "<tran_date>" + getCurrdateAppFormat() + "</tran_date>\r\n";
							xmlValues = xmlValues + "</Detail1>\r\n</Root>";
							System.out.println("xmlValues  :[" + xmlValues + "]");
							TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", CommonConstants.DB_NAME);
							tempTranId = tg.generateTranSeqID("INVHOL", "tran_id", keyString, conn);

							System.out.println("tempTranId [" + tempTranId + "]conn ["+conn);

							sql = " insert into inv_hold (tran_id, tran_date, site_code, remarks,confirmed, chg_user, chg_date, chg_term) " 
									+ " values	( ?, ?, ?, ?, 'N', ?, ?, ? ) ";

							remarks = "Auto generated from Purchase Receipt	:" + tranID;
							chgDate = new java.sql.Timestamp( System.currentTimeMillis() );
							//java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(genericUtility.getDBDateFormat());
							date = sdf.parse(chgDate.toString());
							chgDate = java.sql.Timestamp.valueOf(sdf.format(date).toString() + " 00:00:00.0");

							pstmtUpd = conn.prepareStatement(sql);
							pstmtUpd.setString(1, tempTranId);
							pstmtUpd.setTimestamp(2, chgDate);
							chgDate = new java.sql.Timestamp( System.currentTimeMillis() );
							pstmtUpd.setString(3, siteCode);
							pstmtUpd.setString(4, remarks);
							pstmtUpd.setString(5, userId);
							pstmtUpd.setTimestamp(6, chgDate);
							pstmtUpd.setString(7, termId);

							updCnt = pstmtUpd.executeUpdate();
							pstmtUpd.close();
							pstmtUpd = null;

							lineNoInv++;

							sql = "insert into inv_hold_det (tran_id, line_no, item_code, site_code,loc_code, lot_no, lot_sl, remarks,	hold_status) "
									+ " values	(?, ?, ?,?,?,?,?,?,	'H') ";

							pstmtUpd = conn.prepareStatement(sql);
							pstmtUpd.setString(1, tempTranId);
							pstmtUpd.setInt(2, lineNoInv);
							pstmtUpd.setString(3, itemCode);
							pstmtUpd.setString(4, siteCode);
							pstmtUpd.setString(5, locCode);
							pstmtUpd.setString(6, lotNo);
							pstmtUpd.setString(7, lotSl);
							pstmtUpd.setString(8, remarks);

							updCnt = pstmtUpd.executeUpdate();
							pstmtUpd.close();
							pstmtUpd = null;
						} // batchSize > approved batch size
					} // batchSize > 0
				} 
				// Modified By umakanta Das on 17-OCT-2016[To check QC Required N]Start
				itemSer = getItemSer(itemCode,siteCode, tranDate,conn);
				sql ="select inv_stat from location where loc_code = ? "	;
				//System.out.println("sql-------------"+sql);

				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1,locCodeGit);
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{
					invStat=rs1.getString("inv_stat");
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
				if(tranIdIss != null &&	tranIdIss.trim().length() > 0)
				{
					sql ="select loc_code from distord_issdet where tran_id = ? "
							+" and  	item_code = ? "
							+" and  	lot_no = ? "
							+" and  	lot_sl = ? " ;
					//System.out.println("sql-------------"+sql);

					pstmt1 = conn.prepareStatement(sql);
					//pstmt1.setString(1,tranID);// commented by azhar as tran id of issue should be used
					pstmt1.setString(1,tranIdIss);
					pstmt1.setString(2,itemCode);
					pstmt1.setString(3,lotNo);
					pstmt1.setString(4, lotSl);
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						locCodeIss=rs1.getString("loc_code");
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
				}
				else
				{
					locCodeIss = locCode;
				}
				sql ="select 	acct_code__inv	,	cctr_code__inv		, acct_code__oh	,	cctr_code__oh	, "
						+" unit__alt		, 	conv__qty_stduom	, retest_date"
						+" from 	stock a, invstat b "
						+" where a.inv_stat  = b.inv_stat "
						+" and 	a.item_code = ? "
						+" and	a.loc_code      = ? "
						+" and 	a.lot_no    = ? "
						+" and 	a.lot_sl    = ? "	  ;
				//System.out.println("sql-------------"+sql);
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1,itemCode);
				pstmt1.setString(2,locCodeIss);
				pstmt1.setString(3,lotNo);
				pstmt1.setString(4,lotSl);
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{
					acctCodeInv = rs1.getString("acct_code__inv");
					cctrCodeInv = rs1.getString("cctr_code__inv");
					acctCodeOh  = rs1.getString("acct_code__oh");
					cctrCodeOh  = rs1.getString("cctr_code__oh");
					unitAlt      = rs1.getString("unit__alt");
					convQtyStduom = rs1.getDouble("conv__qty_stduom");
					retestDate    = rs1.getTimestamp("retest_date");
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
				if(unitAlt == null || unitAlt.trim().length() < 0 )
				{
					unitAlt = unit;
				}
				if(convQtyStduom < 1)
				{
					convQtyStduom = 1;
				}
				lineNo = "   "+lineNo.trim() ;
				lineNo = lineNo.substring(lineNo.length()-3);
				sql ="select case when sum(reco_amount) is null then 0 else sum(reco_amount) end "
						+" from   taxtran  where  tran_code = 'D-RCP' and    tran_id   = ? "
						+" and  line_no = ? ";
				//System.out.println("sql-------------"+sql);
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1,tranID);
				pstmt1.setString(2,lineNo);
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{
					recoAmount=rs1.getDouble(1);

				}
				rs1.close();
				pstmt1.close();
				if((netAmt - recoAmount) > 0 && quantity > 0)
				{
					rate = ( (netAmt - recoAmount) / quantity  );
				}
				if(netAmt > 0 && quantity > 0)
				{
					grossRate = netAmt / quantity ;
				}
				sql ="select grp_code from item where item_code = ? " ;
				//System.out.println("sql-------------"+sql);
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1,itemCode);
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{
					grpCode=rs1.getString("grp_code");
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
				acctDetrDistTtype = finCommon.getAcctDetrDistTtype(siteCodeShip,siteCode,itemSer,grpCode,itemCode,"D-ISS", tranType,conn) ;
				if (acctDetrDistTtype != null && acctDetrDistTtype.trim().length() > 0)
				{
					acctCodeIss = acctDetrDistTtype.substring(0,acctDetrDistTtype.indexOf(","));
					cctrCodeIss = acctDetrDistTtype.substring(acctDetrDistTtype.indexOf(",")+1);
				}
				System.out.println("acctCodeIss.....D-ISS........" + acctCodeIss);
				System.out.println("cctrCodeIss.....D-ISS........" + cctrCodeIss);
				if(acctCodeInv ==null || acctCodeInv.trim().length() ==0 )
				{
					acctDetrDistTtype = finCommon.getAcctDetrDistTtype(siteCodeShip,siteCode,itemSer,grpCode,itemCode,"D-INV", tranType,conn) ;
					if (acctDetrDistTtype != null && acctDetrDistTtype.trim().length() > 0)
					{
						acctCodeInv = acctDetrDistTtype.substring(0,acctDetrDistTtype.indexOf(","));
						cctrCodeInv = acctDetrDistTtype.substring(acctDetrDistTtype.indexOf(",")+1);
					}
					System.out.println("acctCodeInv.....D-INV........" + acctCodeInv);
					System.out.println("cctrCodeInv.....D-INV........" + cctrCodeInv);
				}
				if(acctCodeInv ==null || acctCodeInv.trim().length() ==0 )
				{
					acctDetrTtype = finCommon.getAcctDetrTtype(itemCode,itemSer,"STKINV", tranType,conn) ;
					if (acctDetrTtype != null && acctDetrTtype.trim().length() > 0)
					{
						acctCodeInv = acctDetrTtype.substring(0,acctDetrTtype.indexOf(","));
						cctrCodeInv = acctDetrTtype.substring(acctDetrTtype.indexOf(",")+1);
					}
					System.out.println("acctCodeInv.....STKINV........" + acctCodeInv);
					System.out.println("cctrCodeInv.....STKINV........" + cctrCodeInv);
				}
				if(acctCodeInv ==null || acctCodeInv.trim().length() ==0 )
				{
					acctCodeInv =" ";
					cctrCodeInv = " ";
				}
				//added by azhar [31/JAN/2017][START]
				quarLock = distCommon.getDisparams("999999", "QUARNTINE_LOCKCODE", conn);//TODO migrate from pb
				System.out.println("quarantine lock code befor trantype I::" + quarLock);

				sql = "select hold_qty from stock where item_code = ? and site_code = ? and loc_code = ?" +
						"and lot_no  = ? and lot_sl = ?";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, itemCode);
				pstmt1.setString(2, siteCode);
				pstmt1.setString(3, locCodeGit);
				pstmt1.setString(4, lotNo);
				pstmt1.setString(5, lotSl);
				rs1 = pstmt1.executeQuery();
				if(rs1.next()){
					holdQtyNew = rs1.getDouble("hold_qty");
				}
				closeResources(pstmt1, rs1);

				System.out.println("new hold qty::" + holdQtyNew);
				if(holdQtyNew > 0){

					sql = "select a.lock_code ,a.tran_id from inv_hold a, inv_hold_det b , inv_hold_rel_trace c where a.tran_id = b.tran_id and" +
							" b.tran_id = c.ref_no and c.item_code  = ? and ( c.site_code = ? or c.site_code is null ) " +
							"and ( c.loc_code  = ? or c.loc_code is null ) and c.lot_no = ? and" +
							" ( c.lot_sl = ? or c.lot_sl is null ) and b.lot_no = ? and (b.lot_sl = ? or b.lot_sl is null ) " +
							"and (b.line_no_sl = 0 or b.line_no_sl is null) and a.confirmed='Y' and b.hold_status ='H'";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, itemCode);
					pstmt1.setString(2, siteCode);
					pstmt1.setString(3, locCodeGit);
					pstmt1.setString(4, lotNo);
					pstmt1.setString(5, lotSl);
					pstmt1.setString(6, lotNo);
					pstmt1.setString(7, lotSl);
					rs1 = pstmt1.executeQuery();
					while(rs1.next()){

						lockCode = checkNullAndTrim(rs1.getString("lock_code"));
						System.out.println("lock code::" + lockCode);

						if(lockCode.length()>0 && quarLock.equalsIgnoreCase(lockCode)){
							holdLock = "Y";
						}else{
							holdLock  = "N";
						}
					}
				}
				closeResources(pstmt1, rs1);
				//added by azhar [31/JAN/2017][END]
				stkUpdMap.put("hold_lock", holdLock);//added by azhar[31/JAN/2017]
				stkUpdMap.put("site_code",siteCode);
				stkUpdMap.put("item_code",itemCode);
				stkUpdMap.put("sorderno",distOrder);
				stkUpdMap.put("loc_code",locCodeGit);
				stkUpdMap.put("lot_no",lotNo);
				stkUpdMap.put("lot_sl",lotSl);
				stkUpdMap.put("unit",unit);
				stkUpdMap.put("quantity",Double.toString(quantity));
				stkUpdMap.put("qty_stduom",Double.toString(quantity));
				stkUpdMap.put("tran_type","I");
				stkUpdMap.put("tran_date",tranDate);
				stkUpdMap.put("tran_ser","D-RCP");
				stkUpdMap.put("tran_id",tranID);
				stkUpdMap.put("line_no",lineNo);
				System.out.println("manohar locCodeGit 1 : "+locCodeGit);
				stkUpdMap.put("locationcode",locCodeGit);
				stkUpdMap.put("rate",Double.toString(rate));
				stkUpdMap.put("gross_rate",Double.toString(grossRate));
				stkUpdMap.put("gross_weight",Double.toString(grossWeight));
				stkUpdMap.put("net_weight",Double.toString(netWeight));
				stkUpdMap.put("tare_weight",Double.toString(tareWeight));
				stkUpdMap.put("exp_date",expDate);
				stkUpdMap.put("item_ser",itemSer);
				//added by azhar[START][29-APR-2017]
				siteCodeMfg = siteCodeMfg == null ? "" : siteCodeMfg;
				stkUpdMap.put("site_code__mfg",siteCodeMfg); 
				stkUpdMap.put("unit__alt",unitAlt);
				//added by azhar[END][29-APR-2017]
				stkUpdMap.put("pack_code",packCode);
				stkUpdMap.put("pack_instr",packInstr);
				stkUpdMap.put("potency_perc",Double.toString(potencyPerc));
				stkUpdMap.put("inv_stat",invStat);
				stkUpdMap.put("batch_no",batchNo);
				stkUpdMap.put("dimension",dimension);
				//stkUpdMap.put("supp_code__mfg",siteCodeMfg); // commented by azhar[28-APR-2017] supp code mfg should be set instead of site code mfg
				stkUpdMap.put("supp_code__mfg",checkNullAndTrim(suppCodeMfg)); // changed by azhar[28-APR-2017] supp code mfg should be set instead of site code mfg
				stkUpdMap.put("mfg_date",mfgDate);
				stkUpdMap.put("acct_code_inv",acctCodeInv);
				stkUpdMap.put("cctr_code_inv",cctrCodeInv);
				stkUpdMap.put("rate_oh",Double.toString(0));
				stkUpdMap.put("acct_code_oh",acctCodeOh);
				stkUpdMap.put("cctr_code_oh",cctrCodeOh);
				stkUpdMap.put("no_art",Double.toString(noArt));
				stkUpdMap.put("conv__qty_stduom",Double.toString(convQtyStduom));
				stkUpdMap.put("retest_date",retestDate);
				stkUpdMap.put("grade",grade);
				stkUpdMap.put("acctcodecr",acctCodeIss);
				stkUpdMap.put("cctrcodecr",cctrCodeIss);
				StockUpdate stkUpd =  new StockUpdate();
				// Uncommented By Umakanta Das on 17-OCT-2016[ For stock update ]Start
				errString = stkUpd.updateStock(stkUpdMap,xtraParams,conn);
				if (errString != null && errString.trim().length() > 0 )
				{
					strToWrite = createPostLog(lineNo,itemCode,locCode,lotNo,lotSl,quantity,errString);//Modified by Rohini T on[28/02/2021]
					fos1.write(strToWrite.getBytes());
					isError = true;
					System.out.println("Returning Result "+errString);
					return errString;
				}

				// Uncommented By Umakanta Das on 17-OCT-2016[ For stock update ]End
				stkUpd =  null;
				stkUpdMap.clear();
				//Added By umakanta on 17-OCT-2016[ To hold Stock information ]Start
				sql ="Select hold_qty  From stock Where item_code = ? And site_code =? And loc_code = ? "
						+ "	And lot_no = ? And lot_sl = ? "; 
				//System.out.println("sql-------------"+sql);
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString( 1, itemCode );
				pstmt1.setString( 2, siteCode );
				pstmt1.setString( 3, locCodeGit );
				pstmt1.setString( 4, lotNo );
				pstmt1.setString( 5, lotSl );
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{
					holdQty = rs1.getDouble("hold_qty");
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;				

				//if ( holdQty > 0 ) // commented by azhar[31/JAN/2017]

				if ( holdQty > 0 && holdLock.equalsIgnoreCase("Y")) //added by azhar[31/JAN/2017]
				{
					holdQtyMap.put( "hold_qty", Double.toString(holdQty) );
					holdQtyMap.put( "site_code", siteCodeShip );
					holdQtyMap.put( "item_code", itemCode );
					holdQtyMap.put( "loc_code", locCodeGit );
					holdQtyMap.put( "lot_no", lotNo );
					holdQtyMap.put( "lot_sl", lotSl );
					holdQtyList.add( holdQtyMap );

					sql ="update stock set hold_qty = case when hold_qty is null then 0 else hold_qty end - ? Where item_code = ? And site_code =? And loc_code = ? "
							+ "	And lot_no = ? And lot_sl = ? "; 
					//System.out.println("sql-------------"+sql);
					pstmt1 = conn.prepareStatement(sql);
					//pstmt1.setDouble( 1, holdQty );//commented by azhar[31/JAN/2017]
					pstmt1.setDouble( 1, quantity); //added by azhar[31/JAN/2017]
					pstmt1.setString( 2, itemCode );
					pstmt1.setString( 3, siteCode );
					pstmt1.setString( 4, locCodeGit );
					pstmt1.setString( 5, lotNo );
					pstmt1.setString( 6, lotSl );
					upd = pstmt1.executeUpdate();
					pstmt1.close();
					pstmt1 = null;
				}
				//added by saurabh-18/10/16[Start]
				if("Y".equalsIgnoreCase(EOU) && ((jobWorkType.trim()).equalsIgnoreCase(orderType.trim())) && (bondTaxGroup!=null && bondTaxGroup.trim().length()>0))
				{
					bondTaxAmt=0;//Need to ask-saurabh
					sql = "select Sum(case when tax_amt is null then 0 else tax_amt end) as bond_tax_amt " + 
							"from   taxtran Where  tran_code = 'D-RCP' " + 
							" and tran_id = '" + tranID + "' " + 
							" and line_no = '" + lineNo + "' " + 
							" and tax_code IN ( select tax_code from tax where tax_group in ( " + bondTaxGroup + ")) ";
					pstmt1 = conn.prepareStatement(sql);
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						bondTaxAmt = rs1.getInt("bond_tax_amt");
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					if(bondTaxAmt!=0)
					{
						bondValue=0;
						sql= "select bond_no, case when bond_value is null then 0 else bond_value end as bond_value, " +
								"case when bank_guarantee is null then 0 else bank_guarantee end as bank_guarantee " +
								"from b17_bond where site_code = ? and ? >= eff_from and ? <= valid_upto " +
								"and case when confirmed is null then 'N' else confirmed end = 'Y' and bond_type = 'B' ";
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString( 1, siteCode );
						pstmt1.setTimestamp(2, tranDate);
						pstmt1.setTimestamp(3, tranDate);
						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{
							bondNo = rs1.getInt("bond_no");
							bondValue = rs1.getInt("bond_value");
							bankGuarantee = rs1.getInt("bank_guarantee");
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
						//if( bankGuarantee < (bondNo + bondTaxAmt) ) //commented by azhar[31/JAN/2017]
						if( bankGuarantee < (bondValue + bondTaxAmt) ) //added by azhar[31/JAN/2017]
						{
							//TODO
							//errString = itmDBAccessLocal.getErrorString("", "VTB17ERR2", ""); // commented by azhar[31/JAN/2017]
							errString = itmDBAccessEJB.getErrorString("", "VTB17ERR2", "");  //added by azhar[31/JAN/2017]
							//Modified by Rohini T on [01/03/2021][Start]
							if(errString != null && errString.trim().length() > 0)
							{
								logMsg = "Bank Guarantee is less than the Bond Value";
								strToWrite = createPostLog(lineNo,itemCode,locCode,lotNo,lotSl,quantity,logMsg);
								fos1.write(strToWrite.getBytes());
								isError = true;
								return errString;
							}
							//Modified by Rohini T on [01/03/2021][End]
						}
						else
						{
							bondValueFinal = bondValue + bondTaxAmt;
							sql="update b17_bond set bond_value = "+bondValueFinal+" where bond_no = "+bondNo+" ";
							pstmt1 = conn.prepareStatement(sql);
							upd = pstmt1.executeUpdate();
							System.out.println("b17_bond upd::::::"+upd);
							pstmt1.close();
							pstmt1 = null;
						}


					}
				}
				//added by saurabh-18/10/16[end]
				//Added By umakanta on 17-OCT-2016[ To hold Stock information ]Start
			}
			rs.close();
			pstmt.close();
			stkUpdMap.clear();
			if("Y".equalsIgnoreCase(qcReqd))
			{
				errString = createQCOrder(tranID,siteCode,xtraParams,conn);
				//Modified by Rohini T on [28/02/2021][Start]
				if(errString != null && errString.trim().length() > 0)
				{
					strToWrite = createPostLog(lineNo,itemCode,locCode,lotNo,lotSl,quantity,errString);
					fos1.write(strToWrite.getBytes());
					isError = true;
					return errString;
				}
				//Modified by Rohini T on [28/02/2021][End]
			}

			/////////////////////////////////////////
			if(errString ==null || errString.indexOf("Error") == -1)
			{
				sql ="select 	line_no	,	dist_order	,line_no_dist_order,item_code, "
						+" quantity	,	unit, loc_code	, pack_code	, "
						+" rate	, 	lot_no	, 	lot_sl		, 	mfg_date, "
						+" exp_date	, site_code__mfg	, 	potency_perc, gross_weight, "
						+" net_weight,tare_weight	, pack_instr,batch_no, "
						+" grade	, 	dimension	, 	supp_code__mfg 	,	net_amt, no_art ,pallet_wt"
						+ " from 	distord_rcpdet where tran_id = ? order by line_no" ;
				//System.out.println("sql-------------"+sql);
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,tranID);
				rs = pstmt.executeQuery();
				while(rs.next())
				{
					lineNo=rs.getString("line_no");
					distOrder=rs.getString("dist_order");
					lineNoDistOrder=rs.getInt("line_no_dist_order");
					itemCode=rs.getString("item_code");
					quantity=rs.getDouble("quantity");
					unit=rs.getString("unit");
					locCode=rs.getString("loc_code");
					packCode=rs.getString("pack_code");
					rate=rs.getDouble("rate");
					lotNo=rs.getString("lot_no");
					lotSl=rs.getString("lot_sl");
					mfgDate=rs.getTimestamp("mfg_date");
					expDate=rs.getTimestamp("exp_date");
					siteCodeMfg=rs.getString("site_code__mfg");
					potencyPerc=rs.getDouble("potency_perc");
					grossWeight=rs.getDouble("gross_weight");
					netWeight=rs.getDouble("net_weight");
					tareWeight=rs.getDouble("tare_weight");
					packInstr=rs.getString("pack_instr");
					batchNo=rs.getString("batch_no");
					grade=rs.getString("grade");
					dimension=rs.getString("dimension");
					suppCodeMfg=rs.getString("supp_code__mfg");
					netAmt=rs.getDouble("net_amt");
					noArt=rs.getLong("no_art");
					palletWt=rs.getDouble("pallet_wt");

					itemSer = getItemSer(itemCode,siteCode, tranDate,conn);
					sql ="select inv_stat from location where loc_code = ? " ;
					//System.out.println("sql-------------"+sql);

					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1,locCodeGit);
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						invStat=rs1.getString("inv_stat");
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					if(tranIdIss != null &&	tranIdIss.trim().length() > 0)
					{
						sql ="select loc_code,lot_no from distord_issdet where tran_id = ? "
								+" and  	item_code = ? "
								//+" and  	lot_no = ? " // commented by azhar[19-May-2017] as lot no changes in rcp_det if gen_lot_no is Y
								+" and  	line_no = ? " // changed by azhar[19-May-2017] lot no of issue to be taken line wise
								+" and  	lot_sl = ? " ;
						//System.out.println("sql-------------"+sql);

						pstmt1 = conn.prepareStatement(sql);
						//pstmt1.setString(1,tranID); //commented by azhar[19-May-2017][Tran id of distribution issue should be used]
						pstmt1.setString(1,tranIdIss);
						pstmt1.setString(2,itemCode);
						//pstmt1.setString(3,lotNo);
						pstmt1.setString(3,lineNo); // changed by azhar[19-May-2017] lot no of issue to be taken line wise
						pstmt1.setString(4,lotSl);
						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{
							locCodeIss=rs1.getString("loc_code");
							lotNoIss = rs1.getString("lot_no");
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
					}
					else
					{
						locCodeIss = locCode;
					}
					sql ="select acct_code__inv	,	cctr_code__inv		, acct_code__oh	,	cctr_code__oh	, "
							+" unit__alt		, 	conv__qty_stduom	, retest_date"
							+" from  stock a "
							+" where a.item_code = ? "
							+" and	a.loc_code = ? "
							+" and a.lot_no = ? "
							+" and a.lot_sl = ? " ;
					//System.out.println("sql-------------"+sql);
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1,itemCode);
					//pstmt1.setString(2,locCodeIss);//commented by azhar[19-May-2017] as location should be of receipt as per pb component
					pstmt1.setString(2,locCode);
					//pstmt1.setString(3,lotNo); // commented by azhar[19-May-2017] as lot no changes in rcp_det if gen_lot_no is Y
					pstmt1.setString(3,lotNoIss);
					pstmt1.setString(4,lotSl);
					rs1 = pstmt1.executeQuery();

					if(rs1.next())
					{
						acctCodeInv = rs1.getString("acct_code__inv");
						cctrCodeInv = rs1.getString("cctr_code__inv");
						acctCodeOh  = rs1.getString("acct_code__oh");
						cctrCodeOh  = rs1.getString("cctr_code__oh");
						unitAlt      = rs1.getString("unit__alt");
						convQtyStduom = rs1.getDouble("conv__qty_stduom");
						retestDate    = rs1.getTimestamp("retest_date");
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					if(unitAlt == null|| unitAlt.trim().length() < 0 )
					{
						unitAlt = unit;
					}
					if(convQtyStduom < 1)
					{
						convQtyStduom = 1;
					}
					lineNo = "   "+lineNo.trim() ;
					lineNo = lineNo.substring(lineNo.length()-3);
					sql ="select case when sum(reco_amount) is null then 0 else sum(reco_amount) end "
							+" from   taxtran  where  tran_code = 'D-RCP' and    tran_id   = ? "
							+" and line_no = ? ";
					//System.out.println("sql-------------"+sql);
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1,tranID);
					pstmt1.setString(2,lineNo);
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						recoAmount=rs1.getDouble(1);

					}
					rs1.close();
					pstmt1.close();
					if((netAmt - recoAmount) > 0 && quantity > 0)
					{
						rate = ( (netAmt - recoAmount) / quantity  ) ;
					}
					if(netAmt > 0 && quantity > 0)
					{
						grossRate = netAmt / quantity ;
					}
					sql ="select grp_code from item where item_code = ? " ;
					//System.out.println("sql-------------"+sql);
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1,itemCode);
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						grpCode=rs1.getString("grp_code");
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;

					acctDetrDistTtype = finCommon.getAcctDetrDistTtype(siteCodeShip,siteCode,itemSer,grpCode,itemCode,"D-ISS", tranType,conn) ;
					if (acctDetrDistTtype != null && acctDetrDistTtype.trim().length() > 0)
					{
						acctCodeIss = acctDetrDistTtype.substring(0,acctDetrDistTtype.indexOf(","));
						cctrCodeIss = acctDetrDistTtype.substring(acctDetrDistTtype.indexOf(",")+1);
					}
					System.out.println("acctCodeIss.....D-ISS........" + acctCodeIss);
					System.out.println("cctrCodeIss.....D-ISS........" + cctrCodeIss);
					if(acctCodeInv ==null || acctCodeInv.trim().length() ==0 )
					{
						acctDetrDistTtype = finCommon.getAcctDetrDistTtype(siteCodeShip,siteCode,itemSer,grpCode,itemCode,"D-INV", tranType,conn) ;
						if (acctDetrDistTtype != null && acctDetrDistTtype.trim().length() > 0)
						{
							acctCodeInv = acctDetrDistTtype.substring(0,acctDetrDistTtype.indexOf(","));
							cctrCodeInv = acctDetrDistTtype.substring(acctDetrDistTtype.indexOf(",")+1);
						}
						System.out.println("acctCodeInv.....D-INV........" + acctCodeInv);
						System.out.println("cctrCodeInv.....D-INV........" + cctrCodeInv);
					}
					if(acctCodeInv ==null || acctCodeInv.trim().length() ==0 )
					{
						acctDetrTtype = finCommon.getAcctDetrTtype(itemCode,itemSer,"STKINV", tranType,conn) ;
						if (acctDetrTtype != null && acctDetrTtype.trim().length() > 0)
						{
							acctCodeInv = acctDetrTtype.substring(0,acctDetrTtype.indexOf(","));
							cctrCodeInv = acctDetrTtype.substring(acctDetrTtype.indexOf(",")+1);
						}
						System.out.println("acctCodeInv.....STKINV........" + acctCodeInv);
						System.out.println("cctrCodeInv.....STKINV........" + cctrCodeInv);
					}
					if(acctCodeInv ==null || acctCodeInv.trim().length() ==0 )
					{
						acctCodeInv =" ";
						cctrCodeInv = " ";
					}

					stkUpdMap.put("site_code",siteCode);
					stkUpdMap.put("item_code",itemCode);
					stkUpdMap.put("sorderno",distOrder);
					stkUpdMap.put("loc_code",locCode);
					stkUpdMap.put("lot_no",lotNo);
					stkUpdMap.put("lot_sl",lotSl);
					stkUpdMap.put("unit",unit);
					stkUpdMap.put("quantity",Double.toString(quantity));
					stkUpdMap.put("qty_stduom",Double.toString(quantity));
					stkUpdMap.put("tran_type","R");
					stkUpdMap.put("tran_date",tranDate);
					stkUpdMap.put("tran_ser","D-RCP");
					stkUpdMap.put("tran_id",tranID);
					stkUpdMap.put("line_no",lineNo);
					stkUpdMap.put("locationcode",locCodeGit);
					stkUpdMap.put("rate",Double.toString(rate));
					stkUpdMap.put("gross_rate",Double.toString(grossRate));
					stkUpdMap.put("gross_weight",Double.toString(grossWeight));
					stkUpdMap.put("net_weight",Double.toString(netWeight));
					stkUpdMap.put("tare_weight",Double.toString(tareWeight));
					stkUpdMap.put("pallet_wt",Double.toString(palletWt));
					stkUpdMap.put("exp_date",expDate);
					stkUpdMap.put("item_ser",itemSer);
					//added by azhar[START][29-APR-2017]
					siteCodeMfg = siteCodeMfg == null ? "" : siteCodeMfg;
					stkUpdMap.put("site_code__mfg",siteCodeMfg); 
					stkUpdMap.put("unit__alt",unitAlt);
					//added by azhar[END][29-APR-2017]
					stkUpdMap.put("pack_code",packCode);
					stkUpdMap.put("pack_instr",packInstr);
					stkUpdMap.put("potency_perc",Double.toString(potencyPerc));
					stkUpdMap.put("inv_stat",invStat);
					stkUpdMap.put("batch_no",batchNo);
					stkUpdMap.put("dimension",dimension);
					//stkUpdMap.put("supp_code__mfg",siteCodeMfg); // commented by azhar[28-APR-2017] supp code mfg should be set instead of site code mfg
					stkUpdMap.put("supp_code__mfg",checkNullAndTrim(suppCodeMfg)); // changed by azhar[28-APR-2017] supp code mfg should be set instead of site code mfg
					stkUpdMap.put("mfg_date",mfgDate);
					stkUpdMap.put("acct_code_inv",acctCodeInv);
					stkUpdMap.put("cctr_code_inv",cctrCodeInv);
					stkUpdMap.put("rate_oh",Double.toString(0));
					stkUpdMap.put("acct_code_oh",acctCodeOh);
					stkUpdMap.put("cctr_code_oh",cctrCodeOh);
					stkUpdMap.put("no_art",Double.toString(noArt));
					stkUpdMap.put("conv__qty_stduom",Double.toString(convQtyStduom));
					stkUpdMap.put("retest_date",retestDate);
					stkUpdMap.put("grade",grade);
					stkUpdMap.put("acctcodecr",acctCodeIss);
					stkUpdMap.put("cctrcodecr",cctrCodeIss);
					// Modified  By Umakanta Das on 17-OCT-2016[]Start
					stkUpdMap.put( "hold_lock","N" );

					if ( holdQtyList != null && holdQtyList.size() > 0 )
					{
						for ( HashMap<String, String> resultMap : holdQtyList ) 
						{

							holdQtyIss = Double.parseDouble( (String) resultMap.get("hold_qty") );
							siteCodeIss = (String) resultMap.get("site_code");
							itemCodeIss = (String) resultMap.get("item_code");
							locCodeIss1 = (String) resultMap.get("loc_code");
							lotNoIss = (String) resultMap.get("lot_no");
							lotSlIss = (String) resultMap.get("lot_sl");
							if ( itemCodeIss.equals( itemCode ) &&  lotNoIss.equals( lotNo ) ) 
							{
								if ( holdQtyIss > 0 )
								{
									stkUpdMap.put("hold_qty", Double.toString( quantity ) );
									// Doubt Need to Discuss with Manoj.
									//sql =" Select a.tran_id as holdTranId From inv_hold a, inv_hold_det b , inv_hold_rel_trace c " //commented by azhar[31/JAN/2017]
									sql =" select a.tran_id as holdTranId,a.lock_code as lock_code From inv_hold a, inv_hold_det b , inv_hold_rel_trace c " //added by azhar[31/JAN/2017]
											+ " Where a.tran_id = b.tran_id "
											+ " And b.tran_id = c.ref_no "
											+ " And c.item_code  = ? "
											+ " And (c.site_code = ? OR c.site_code is null ) "
											+ " And c.loc_code  = ? "
											+ " And c.lot_no    = ? "
											+ " And c.lot_sl    = ? "
											+ " And b.lot_no    = ? "
											+ " And (b.line_no_sl = 0 or b.line_no_sl is null) "
											+ " And a.confirmed='Y' "
											+ " And b.hold_status ='H' " ;
									//System.out.println("sql-------------"+sql);
									pstmt1 = conn.prepareStatement(sql);
									pstmt1.setString(1,itemCodeIss);

									//commented by azhar[31/JAN/2017][START]
									//pstmt1.setString(2,siteCodeIss);//Changed by manoj dtd 19/10/2016 to use siteCodeIss instead of siteCode
									//commented by azhar[31/JAN/2017][END]

									pstmt1.setString(2,siteCode);//added by azhar[31/JAN/2017][START]

									pstmt1.setString(3,locCodeIss1);
									pstmt1.setString(4,lotNoIss);
									pstmt1.setString(5,lotSlIss);
									pstmt1.setString(6,lotNoIss);
									rs1 = pstmt1.executeQuery();
									if(rs1.next())
									{
										holdTranId= checkNullAndTrim(rs1.getString("holdTranId"));
										lockCodeNew =  checkNullAndTrim(rs1.getString("lock_code"));//added by azhar[31/JAN/2017]
									}
									closeResources(pstmt1, rs1);
									// Doubt Need to Discuss with Manoj.
									//Added by saurabh 18/10/16[Start]
									/* Commented by Manoj dtd 19/10/2016 as per PB component
									sql=" update inv_hold_det set loc_code = ?, site_code = ? Where tran_id = ? " +
		                    				"And item_code = ? And lot_no = ? And lot_sl = ? " +
		                    				"And loc_code = ? And hold_status ='H' ";
		                    		pstmt1 = conn.prepareStatement(sql);
		                    		pstmt1.setString( 1, locCode );
		                    		pstmt1.setString( 2, siteCode );
		                    		pstmt1.setString( 3, holdTranId );
		                    		pstmt1.setString( 4, itemCodeIss );
		                    		pstmt1.setString( 5, lotNoIss );
		                    		pstmt1.setString( 6, lotSlIss );
		                    		pstmt1.setString( 7, locCodeIss1 );
		                    		upd = pstmt1.executeUpdate();
		                    		pstmt1.close();
		                    		pstmt1 = null;*/
									//Added by saurabh 18/10/16[end]

									//commented by azhar[31/JAN/2017][START]
									/*sql ="update inv_hold_rel_trace set loc_code = ?, site_code = ? Where ref_no = ? "
		                    				+ " And site_code   = ? " 
		                    				+ " And item_code   = ? "
		                    				+ " And lot_no      = ? "
		                    				+ " And lot_sl      = ? "
		                    				+ " And loc_code    = ? "
		                    				+ " And hold_qty    > 0 " ;

		                    		pstmt1 = conn.prepareStatement(sql);
		                    		pstmt1.setString( 1, locCode );
		                    		pstmt1.setString( 2, siteCode );
		                    		pstmt1.setString( 3, holdTranId );//Need to Discuss with Manoj
		                    		pstmt1.setString( 4, siteCodeIss );
		                    		pstmt1.setString( 5, itemCodeIss );
		                    		pstmt1.setString( 6, lotNoIss );
		                    		pstmt1.setString( 7, lotSlIss );
		                    		pstmt1.setString( 8, locCodeIss1 );
		                    		upd = pstmt1.executeUpdate();
		                    		pstmt1.close();
		                    		pstmt1 = null;*/

									//commented by azhar[31/JAN/2017][END]

									//added by azhar[31/JAN/2017][START]
									sql = "update inv_hold_rel_trace set hold_qty = hold_qty - ? where ref_no = ? " +
											"and site_code = ? and item_code = ? and lot_no = ?" +
											" and lot_sl = ? and loc_code = ? and hold_qty > 0 ";

									pstmt1 = conn.prepareStatement(sql);
									pstmt1.setDouble( 1, quantity);
									pstmt1.setString( 2, holdTranId );
									pstmt1.setString( 3, siteCode );
									pstmt1.setString( 4, itemCodeIss);
									pstmt1.setString( 5, lotNoIss );
									pstmt1.setString( 6, lotSlIss );
									pstmt1.setString( 7, locCodeIss1 );
									upd = pstmt1.executeUpdate();
									pstmt1.close();
									pstmt1 = null;
									//added by azhar [31/JAN/2017][END]

									stkUpdMap.put( "hold_lock","Y" );
								}
								else
								{
									stkUpdMap.put( "hold_qty", 0 );
								}
							}
						}        
					}
					// Modified By Umakanta Das on 17-OCT-2016[]End
					StockUpdate stkUpd =  new StockUpdate();
					errString = stkUpd.updateStock(stkUpdMap,xtraParams,conn);
					System.out.println("Returning Result "+errString);
					if (errString != null && errString.trim().length() > 0 && errString.indexOf("Error") != -1 )
					{
						strToWrite = createPostLog(lineNo,itemCode,locCode,lotNo,lotSl,quantity,errString);//Modified by Rohini T on[28/02/2021]
						fos1.write(strToWrite.getBytes());
						isError = true;
						return errString;
					}

					//added by azhar[31/JAN/2017][START]

					int countHold = 0,updHold = 0;
					loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "site_code");
					System.out.println("login site ocde:::" + loginSiteCode);
					if(stkUpdMap.get("hold_lock").toString().equalsIgnoreCase("Y")){
						System.out.println("inside hold lock loop::");
						sql = "select count (*) as count from inv_hold a, inv_hold_det b , inv_hold_rel_trace c where a.tran_id = b.tran_id and " +
								"b.tran_id = c.ref_no and c.item_code  = ? and c.site_code = ? and c.loc_code = ? and c.lot_no = ? and c.lot_sl = ?" +
								" and (b.line_no_sl = 0 or b.line_no_sl is null) and a.confirmed='Y' and b.hold_status ='H' and a.lock_code =?";
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1, itemCode);
						pstmt1.setString(2, siteCode);
						pstmt1.setString(3, locCode);
						pstmt1.setString(4, lotNo);
						pstmt1.setString(5, lotSl);
						pstmt1.setString(6, lockCodeNew);
						rs1= pstmt1.executeQuery();
						if(rs1.next()){
							countHold = rs1.getInt("count");
						}
						System.out.println("count hold ::" + countHold);
						closeResources(pstmt1, rs1);

						if(countHold > 0){
							sql = "update inv_hold_rel_trace set hold_qty = hold_qty + ? where ref_no = ? and item_code = ? and site_code = ? and loc_code = ? " +
									"and lot_no = ? and lot_sl = ?";
							pstmt1 = conn.prepareStatement(sql);
							pstmt1.setDouble(1, quantity);
							pstmt1.setString(2, holdTranId);
							pstmt1.setString(3, itemCode);
							pstmt1.setString(4, siteCode);
							pstmt1.setString(5, locCode);
							pstmt1.setString(6, lotNo);
							pstmt1.setString(7, lotSl);
							updHold = pstmt1.executeUpdate();
						}else{
							String xmlValues = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
							xmlValues = xmlValues + "<Header></Header>";
							xmlValues = xmlValues + "<Detail1>";
							xmlValues = xmlValues + "<tran_id></tran_id>";
							xmlValues = xmlValues + "<site_code>"+loginSiteCode+"</site_code>";
							xmlValues = xmlValues + "<tran_date>"+getCurrdateAppFormat()+"</tran_date>";
							xmlValues = xmlValues + "</Detail1></Root>";
							tranIdHoldTrace = generateTranId("w_inv_hold_rel_trace",xmlValues,conn);//function to generate NEW transaction id
							System.out.println("tranIdHoldTrace......"+tranIdHoldTrace);
							if(tranIdHoldTrace == null || tranIdHoldTrace.trim().length() ==0)
							{
								errCode = "VTTRANID";
								System.out.println("errcode......"+errCode);
								errString = itmDBAccessEJB.getErrorString("","VTTRANID","BASE","",conn);
								//Modified by Rohini T on [01/03/2021][Start]
								if(errString != null && errString.trim().length() > 0)
								{
									logMsg = "Selected transaction not exists";
									strToWrite = createPostLog(lineNo,itemCode,locCode,lotNo,lotSl,quantity,logMsg);
									fos1.write(strToWrite.getBytes());
									isError = true;
									return errString;
								}
								//Modified by Rohini T on [01/03/2021][End]
							}else{
								sql = "insert into inv_hold_rel_trace(tran_id,item_code,site_code,loc_code,lot_no,lot_sl,ref_no,hold_qty,lock_code)" +
										"values(?,?,?,?,?,?,?,?,?)";
								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, tranIdHoldTrace);
								pstmt1.setString(2, itemCode);
								pstmt1.setString(3, siteCode);
								pstmt1.setString(4, locCode);
								pstmt1.setString(5, lotNo);
								pstmt1.setString(6, lotSl);
								pstmt1.setString(7, holdTranId);
								pstmt1.setDouble(8, quantity);
								pstmt1.setString(9, lockCodeNew);
								pstmt1.executeUpdate();
							}
						}	
					}
					closeResources(pstmt1, rs1);
					//added by azhar[31/JAN/2017][END]

					stkUpd =  null;
					stkUpdMap.clear();
					sql ="select tran_type__parent from distorder_type where tran_type = ? ";
					//System.out.println("sql-------------"+sql);
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1,tranType);
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						tranTypeParent=rs1.getString("tran_type__parent");
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					if(tranType.trim().equalsIgnoreCase(tranTypeParent))
					{
						sql ="update distorder_det set qty_received = case when qty_received is null then 0 else qty_received end + '"+quantity+"' "
								+" where  dist_order = ? "
								+" and    line_no = ? ";
						//System.out.println("sql-------------"+sql);
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1,distOrder);
						pstmt1.setInt(2,lineNoDistOrder);
						upd = pstmt1.executeUpdate();
						pstmt1.close();
						pstmt1 = null;

						/**Added by Pavan Rane 24dec19 start[to update demand/supply in summary table(RunMRP process) related changes]*/
						sql = "select b.due_date, a.site_code__dlv from distorder a, distorder_det b"
								+ " where a.dist_order = b.dist_order"
								+ " and a.dist_order = ?"
								+ " and b.line_no = ? ";
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1,distOrder);
						pstmt1.setInt(2,lineNoDistOrder);
						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{
							dueDateOrd = rs1.getTimestamp("due_date"); // to populate due_date and site_code
							siteCodeDlv = rs1.getString("site_code__dlv");
						}
						pstmt1.close();
						pstmt1 = null;

						demandSupplyMap.put("site_code", siteCodeDlv);
						demandSupplyMap.put("item_code", itemCode);		
						demandSupplyMap.put("ref_ser", "D-ORDR");
						demandSupplyMap.put("ref_id", distOrder);
						demandSupplyMap.put("ref_line", ""+lineNoDistOrder);
						demandSupplyMap.put("due_date", dueDateOrd);		
						demandSupplyMap.put("demand_qty", 0.0);
						demandSupplyMap.put("supply_qty", quantity *(-1));
						demandSupplyMap.put("change_type", "C");
						demandSupplyMap.put("chg_process", "T");
						demandSupplyMap.put("chg_user", genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
						demandSupplyMap.put("chg_term", genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId"));	
						errString = invDemSupTrcBean.updateDemandSupply(demandSupplyMap, conn);
						if(errString != null && errString.trim().length() > 0)
						{
							strToWrite = createPostLog(lineNo,itemCode,locCode,lotNo,lotSl,quantity,errString);
							fos1.write(strToWrite.getBytes());
							isError = true;//Modified by Rohini T on [28/02/2021]
							System.out.println("errString["+errString+"]");
							return errString;
						}
						/**Added by Pavan Rane 24dec19 end[to update demand/supply in summary table(RunMRP process) related changes]*/
					}
				}
				rs.close();
				pstmt.close();
				stkUpdMap.clear();
				//Modified By Umakanta on 17-OCT-2016[ To get QUARNTINE_LOCKCODE and generateHoldTrans ]Start
				System.out.println("qcReqd:::::"+qcReqd+"holdQty:::"+holdQty);
				if ( "Y".equalsIgnoreCase(qcReqd) && holdQty > 0 )
				{
					quarLockCode = checkNullAndTrim(distCommon.getDisparams("999999", "QUARNTINE_LOCKCODE", conn));
					System.out.println("quarLockCode:::::::"+quarLockCode);
					//if( quarLockCode.length() > 0 ) //commented by azhar[31/JAN/2017]
					if( quarLockCode.length() > 0 && !quarLockCode.equalsIgnoreCase("NULLFOUND")) // added by azhar[31/JAN/2017]
					{
						sql ="select count(*) as count from distord_rcpdet where tran_id = ? ";
						//System.out.println("sql-------------"+sql);
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString( 1, tranID);
						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{
							cnt = rs1.getInt("count");
						}
						System.out.println("cnt::::"+cnt);
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
						if ( cnt > 0 )
						{
							sql ="select a.site_code, b.item_code, b.loc_code, b.lot_no, b.lot_sl from distord_rcp a , distord_rcpdet b "
									+ " where a.tran_id = ? and a.tran_id = b.tran_id ";
							//System.out.println("sql-------------"+sql);
							pstmt1 = conn.prepareStatement(sql);
							pstmt1.setString( 1, tranID );
							rs1 = pstmt1.executeQuery();
							while( rs1.next() )
							{
								siteCodeDRcp = rs1.getString("site_code");
								itemCodeDRcp = rs1.getString("item_code");
								locCodeDRcp = rs1.getString("loc_code");
								lotNoDRcp = rs1.getString("lot_no");
								lotSlDRcp = rs1.getString("lot_sl");

								//added by azhar[31/JAN/2017][START]
								sql = "select qc_lock_reqd from qc_xfr_ctrl where item_code = ? and site_code = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,itemCode);
								pstmt.setString(2,siteCode);
								rs = pstmt.executeQuery();
								if(rs.next()){
									qcLockReqd = checkNullAndTrim(rs.getString("qc_lock_reqd"));
								}

								closeResources(pstmt, rs);

								if(qcLockReqd.equalsIgnoreCase("Y")) {

									sql = "select count(*) as count from qc_xfr_ctl_det where site_code = ? and item_code = ? and" +
											" (? >= lot_no__from and ? <= lot_no__to ) and (? >= from_date and ? <= to_date  ) ";

									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1,siteCode);
									pstmt.setString(2,itemCode);
									pstmt.setString(3, lotNo);
									pstmt.setString(4, lotNo);
									pstmt.setTimestamp(5, tranDate);
									pstmt.setTimestamp(6, tranDate);
									rs = pstmt.executeQuery();
									if(rs.next()){
										countXfr = rs.getInt("count");
									}

									closeResources(pstmt, rs);

								}
								if(countXfr > 0){
									lbQcLock = true;
									stockQtyMap.put( "hold_qty", holdQty );
									stockQtyMap.put( "site_code", siteCodeDRcp );
									stockQtyMap.put( "item_code", itemCodeDRcp );
									stockQtyMap.put( "loc_code", locCodeDRcp );
									stockQtyMap.put( "lot_no", lotNoDRcp );
									stockQtyMap.put( "lot_sl", lotSlDRcp );
									stockList.add( stockQtyMap );
								}
								//added by azhar[31/JAN/2017][END]


								//commented by azhar[31/JAN/2017][START]

								/*stockQtyMap.put( "hold_qty", holdQty );
								stockQtyMap.put( "site_code", siteCodeDRcp );
								stockQtyMap.put( "item_code", itemCodeDRcp );
								stockQtyMap.put( "loc_code", locCodeDRcp );
								stockQtyMap.put( "lot_no", lotNoDRcp );
								stockQtyMap.put( "lot_sl", lotSlDRcp );
								stockList.add( stockQtyMap );*/

								//commented by azhar[31/JAN/2017][END]
							}
							closeResources(pstmt1, rs1);							
							//errString = invHoldGen.generateHoldTrans(quarLockCode, tranID, "P-RCP", siteCode, stockList, xtraParams, conn); //commented by azhar[31/JAN/2017]

							//added by azhar[31/JAN/2017][START]
							if(lbQcLock){
								errString = invHoldGen.generateHoldTrans(quarLockCode, tranID, "D-RCP", siteCode, stockList, xtraParams, conn);
								//Modified by Rohini T on [28/02/2021][Start]
								if(errString != null && errString.trim().length() > 0)
								{
									strToWrite = createPostLog(lineNo,itemCode,locCode,lotNo,lotSl,quantity,errString);
									fos1.write(strToWrite.getBytes());
									isError = true;
									return errString;
								}
								//Modified by Rohini T on [28/02/2021][End]
							}
							//added by azhar[31/JAN/2017][END]
						}
					}
				}
				//Modified By Umakanta on 17-OCT-2016[]End
			}
			//Added by Nandkumar Gadkari  on 04-SEP-2018[START]----------------

			if(errString ==null || errString.indexOf("Error") == -1)
			{
				InvAcct invacct =  new InvAcct();
				errString = invacct.acctDisRcptPost(tranID,conn);	
				//Modified by Rohini T on [28/02/2021][Start]
				if(errString != null && errString.trim().length() > 0)
				{
					strToWrite = createPostLog(lineNo,itemCode,locCode,lotNo,lotSl,quantity,errString);
					fos1.write(strToWrite.getBytes());
					isError = true;
					return errString;
				}
				//Modified by Rohini T on [28/02/2021][End]
				System.out.println("Returning Result@@@ "+errString);
			}			
			//Added by Nandkumar Gadkari  on 04-SEP-2018[END]-----------------
			if(errString !=null && errString.indexOf("Error") != -1)
			{
				strToWrite = createPostLog(lineNo,itemCode,locCode,lotNo,lotSl,quantity,errString);//Modified by Rohini T on [28/02/2021]
				fos1.write(strToWrite.getBytes());
				isError = true;
				System.out.println("Returning Result.... "+errString);
				conn.rollback();
				return errString;
			}

			System.out.println("errString : "+errString);
			String empCodeAprv = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode");
			System.out.println("empCodeAprv ................ "+empCodeAprv);
			sql = "update distord_rcp set confirmed = 'Y',conf_date = ? ,emp_code__aprv = ? where tran_id = ? ";
			//System.out.println("SQL : "+sql);
			pstmt = conn.prepareStatement(sql);
			pstmt.setTimestamp(1,new Timestamp(System.currentTimeMillis()));
			pstmt.setString(2,empCodeAprv);
			pstmt.setString(3,tranID);
			upd =0;
			upd = pstmt.executeUpdate();
			pstmt.close();
			pstmt = null;
			System.out.println("distord_rcp Confirm update count :: "+upd);
			int countDet = 0;
			if(upd > 0)
			{
				//added by azhar[31/JAN/17][START]
				sql = "select count(*) as count from distorder_det where dist_order = ? and (qty_confirm > qty_shipped or qty_confirm > qty_received)";
				pstmt= conn.prepareStatement(sql);
				pstmt.setString(1, distOrder);
				rs = pstmt.executeQuery();
				if(rs.next()){
					countDet = rs.getInt("count");
				}
				closeResources(pstmt, rs);//[pstmt and rs closed and nulled on 23feb19]
				if(countDet > 0){
					status = "P";
				}else{
					status = "C";
				}

				sql = "update distorder set status = ? where dist_order = ? ";

				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,status);
				pstmt.setString(2,distOrder);
				upd =0;
				upd = pstmt.executeUpdate();
				pstmt.close();
				pstmt = null;
				System.out.println("distorder status update :: "+upd);

				//added by azhar[31/JAN/17][END]

				//commented by azhar[31/JAN/2017][START]

				/*sql = "update distorder set status = 'C' where dist_order = ? ";
				//System.out.println("SQL : "+sql);
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,distOrder);
				upd =0;
				upd = pstmt.executeUpdate();
				pstmt.close();
				pstmt = null;
				System.out.println("distorder status update :: "+upd);*/

				//commented by azhar[31/JAN/2017][END]

				//errString = itmDBAccessEJB.getErrorString("","VTDIST30","");
				//System.out.println("distord_rcp Confirmed .."+errString);

			}


			//added by azhar[31/JAN/2017][START]
			sql = "select count(*) as count from distorder_alloc where dist_order = '"+distOrder+"'";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next()){
				count = rs.getInt("count");
			}
			closeResources(pstmt, rs);//[pstmt and rs closed and nulled on 23feb19]
			if(count > 0){
				errString = allocateStockWorkOrder(distOrder,xtraParams,tranID,conn);
				//Modified by Rohini T on [28/02/2021][Start]
				if(errString != null && errString.trim().length() > 0)
				{
					strToWrite = createPostLog(lineNo,itemCode,locCode,lotNo,lotSl,quantity,errString);
					fos1.write(strToWrite.getBytes());
					isError = true;
					return errString;
				}
				//Modified by Rohini T on [28/02/2021][End]
				System.out.println("Error String after errString"+errString);
			}
			//added by azhar[31/JAN/2017][END]

			//Changed by wasim on 20-11-2015 for work order allocation for AWMS [START]
			//commented by azhar[31/JAN/2017][START]
			/*errString = allocateStockWorkOrder(distOrder,xtraParams,tranID,conn);
			 System.out.println("Error String after errString"+errString);*/
			//commented by azhar[31/JAN/2017][END]

			/*if(errString !=null && errString.indexOf("Error") != -1)
			 {
	              conn.rollback();
	              return errString;
			 }

			 else
			 {
				 errString = itmDBAccessEJB.getErrorString("","VTCONF","","",conn);
			 }*/
			//Changed by wasim on 20-11-2015 for work order allocation for AWMS [START]


			// VALLABH KADAM [20/NOV/2017] not required after source review START
			//			
			//			System.out.println("@V@ In second half");
			//			sql="select sale_order from distorder a, distord_rcp b where a.dist_order = b.dist_order and b.tran_id = ?";
			//			pstmt=conn.prepareStatement(sql);
			//			pstmt.setString(1, tranID);
			//			rs=pstmt.executeQuery();
			//			if(rs.next())
			//			{
			//				saleOrder=rs.getString("sale_order");
			//			}
			//			rs.close();rs=null;
			//			pstmt.close();pstmt=null;
			//			System.out.println("@V@ saleOrder 1804:- ["+saleOrder+"]");
			//			if(saleOrder!=null && saleOrder.trim().length()>0)
			//			{
			//				/**
			//				 * Commented as per suggested [17/NOV/2017] START
			//				 * */
			////				errString = gbfStockAllocate(tranID,conn,userId,termId);
			////				if(errString!=null && errString.trim().length()>0)
			////				{
			////					return errString;
			////				}
			//				/**
			//				 * Commented as per suggested [17/NOV/2017] END
			//				 * */
			//				
			//				sql="select o.LINE_NO__SORD, r.quantity, r.item_code from distord_rcpdet r, distorder_det o"
			//						+ " where o.dist_order = r.dist_order and o.line_no = r.LINE_NO_DIST_ORDER"
			//						+ " and o.SALE_ORDER = ? and o.LINE_NO__SORD is not null and r.tran_id = ?";
			//				pstmt=conn.prepareStatement(sql);
			//				pstmt.setString(1, saleOrder);
			//				pstmt.setString(2, tranID);
			//				rs=pstmt.executeQuery();
			//				while(rs.next())
			//				{
			//					lineNoSord=rs.getString("LINE_NO__SORD");
			//					qtytoballocated=rs.getDouble("quantity");
			//					itemCode=rs.getString("item_code");
			//					
			//					System.out.println("@V@ lineNoSord 1826:- ["+lineNoSord+"]");
			//					System.out.println("@V@ saleOrder 1827:- ["+1804+"]");
			//					System.out.println("@V@ saleOrder 1828:- ["+1804+"]");
			//					
			//					sql1="select item_code__ord,exp_lev,QTY_ALLOC from sorditem where sale_order = ? and line_no = ? and line_type = 'I'";
			//					pstmt1=conn.prepareStatement(sql1);
			//					pstmt1.setString(1, saleOrder);
			//					pstmt1.setString(2, lineNoSord);
			//					rs1=pstmt1.executeQuery();
			//					if(rs1.next())
			//					{
			//						itemCodeOrd=rs1.getString("item_code__ord");
			//						expLev=rs1.getString("exp_lev");
			//						qtyAlloc=rs1.getDouble("QTY_ALLOC");
			//					}
			//					rs1.close();rs1=null;
			//					pstmt1.close();pstmt1=null;
			//					
			//					if(qtyAlloc < qtytoballocated)
			//					{
			//						lbAllocated = false;
			//						break;
			//					}
			//						
			//					sql1="select sum( QTY_ALLOC) lc_allocatedqty from sordalloc"
			//							+ " where sale_order = ? and line_no = ? and exp_lev = ? and item_code__ord = ? and item_code = ?";
			//					pstmt1=conn.prepareStatement(sql1);
			//					pstmt1.setString(1, saleOrder);
			//					pstmt1.setString(2, lineNoSord);
			//					pstmt1.setString(3, expLev);
			//					pstmt1.setString(4, itemCodeOrd);
			//					rs1=pstmt1.executeQuery();
			//					if(rs1.next())
			//					{
			//						lcAllocatedqty=rs1.getDouble("lc_allocatedqty");
			//					}
			//					rs1.close();rs1=null;
			//					pstmt1.close();pstmt1=null;
			//					
			//					if(lcAllocatedqty < qtytoballocated)
			//					{
			//						lbAllocated = false;
			//						break;
			//					}
			//				} // END WHILE LOOP
			//				rs.close();rs=null;
			//				pstmt.close();pstmt=null;
			//				
			//				if(!lbAllocated)
			//				{
			//					errString = itmDBAccessEJB.getErrorString("","VTNOTALLOC","BASE","",conn);
			//					return errString;
			//				}	
			//			}// SORDER IF END
			//			
			//			
			//			
			// VALLABH KADAM [20/NOV/2017] not required after source review END
			if((errString !=null && errString.indexOf("Error") != -1) || (errString != null && errString.trim().length() > 0))
			{
				System.out.println("errString@@@::::"+errString);
				conn.rollback();
				strToWrite = createPostLog(lineNo,itemCode,locCode,lotNo,lotSl,quantity,errString);//Modified by Rohini T on 17/03/2021
				fos1.write(strToWrite.getBytes());
				return errString;
			}
			else
			{
				errString = itmDBAccessEJB.getErrorString("","VTCONF","","",conn);
				System.out.println("errString$$$::::"+errString);
				//Modified by Rohini T on [23/03/2021][Start]
				if(errString != null && errString.trim().length() > 0)
				{
					strToWrite = createPostLog(lineNo,itemCode,locCode,lotNo,lotSl,quantity,errString);
					fos1.write(strToWrite.getBytes());
				}
				//Modified by Rohini T on [23/03/2021][End]
			}

			// Added by Anagha Rane 02-04-2020 Serdia Customization
			//added by Monika salla on 31 dec 2020 

			//need to add tran type on basis of tran type recovery will be done DISTORDER_TYPE--table
			sql = "select RECOVER_GST,RECOVER_TRANTYPE from DISTORDER_TYPE where tran_type = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranType);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				recoverCsaGst = rs.getString("RECOVER_GST");
				recoverTranType = rs.getString("RECOVER_TRANTYPE");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			//recoverCsaGst = fincommon.getFinparams("999999", "RECOVER_CSA_GST", conn);
			System.out.println("recoverCsaGst--11["+recoverCsaGst+" ]recoverTranType ["+recoverTranType);
			//end
			//recoverCsaGst = fcom.getFinparams("999999", "RECOVER_CSA_GST", conn);
			//System.out.println("recoverCsaGst: " + recoverCsaGst);

			if ("NULLFOUND".equalsIgnoreCase(recoverCsaGst) || recoverCsaGst == null || recoverCsaGst == "") {
				recoverCsaGst = "N";
			}

			if ("Y".equalsIgnoreCase(recoverCsaGst))
			{
				sql = "select site_code from distord_rcp where tran_id = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranID);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					siteCode = checkNullAndTrim(rs.getString(1));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				sql = " select site_type from site where site_code = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, siteCode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					siteType = checkNullAndTrim(rs.getString(1));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				/*creatInvOthlist = fcom.getFinparams("999999", "ALOW_INV_OTH_SITE", conn);
					System.out.println("creatInvOthlist: " + creatInvOthlist);

					if ("NULLFOUND".equalsIgnoreCase(creatInvOthlist) || creatInvOthlist == null) {
						creatInvOthlist = "";
					}

                    if (creatInvOthlist.trim().length() > 0)
                     {
						String[] arrStr = creatInvOthlist.split(",");
                        for (int j = 0; j < arrStr.length; j++)
                         {
							creatInvOth = arrStr[j];
							System.out.println("creatInvOth>>>>>>>>" + creatInvOth);
                            if (siteType.equalsIgnoreCase(creatInvOth.trim())) 
                            {
								otherSite = fcom.getFinparams("999999", "INVOICE_OTHER_SITE", conn);
								System.out.println("otherSite: " + otherSite);
								if (!"NULLFOUND".equalsIgnoreCase(creatInvOthlist) && creatInvOthlist != null
                                        && creatInvOthlist.trim().length() > 0) 
                                {

									//errString = generateMiscVoucher(tranID, otherSite, xtraParams, conn);
                                    //errorString = generateMiscVoucher(tranID, otherSite, xtraParams, conn);
                                    errorString = generateMiscVoucher(tranID, otherSite,recoverTranType, xtraParams, conn);//added by monika salla on 31 dec 2020
									System.out.println("errString 1962: " + errorString);

									if( errorString != null && errorString.trim().length() > 0 )
									{
										//Added by Anagha R on 23/06/2020 for Serdia - VTCUSTCD4 pop up coming on confirmaiton of Distribution receipt
										if(errorString.indexOf("CONFSUCCES") > -1)
										{
											errorString = "";
										}
										else
										{
											if(connStatus) {
												conn.rollback();
												return errorString;
											}
										}
										System.out.println("retString3::::"+errorString);
										//Added by Anagha R on 23/06/2020 for Serdia - VTCUSTCD4 pop up coming on confirmaiton of Distribution receipt
										return errString;
									}
								}
							}
						}
                    }*/
				//commented by monika salla on 31 dec 20
				//added by monika salla  on 23 03 21

				// ' DN' = 'Raised  GST DN to Customer'--- value is "CG"---
				//will be used in distribution Order type for transferring material from Serdia to CFA . In this case DN will be generated towards the Customer in Issuing site (Receivables) . In  Receiving site CN will be created towards the Supplier  (Payables). 
				DissIssuePosSave disisspossave=new DissIssuePosSave();        
				if("CG".equalsIgnoreCase(recoverTranType.trim()))  
				{   
					System.out.println("recovertrantype CG--->>"+ recoverTranType +" CONNECTIONV["+ conn);
					errString = generateMiscVoucher(tranID, otherSite,recoverTranType, xtraParams, conn);//added by monika salla on 31 dec 2020

					if( errString != null && errString.trim().length() > 0 )
					{
						System.out.println("recovertrantype CG retun from voucher1234--->>"+ errString);

						if(errString.indexOf("CONFSUCCES") > -1 || errString.indexOf("SEND_SUCCESS")> -1 || errString.indexOf("VTCONF") > -1)
						{
							errString = "";
							//errString = itmDBAccessEJB.getErrorString("","CONFSUCCES","","",conn);
							errString = itmDBAccessEJB.getErrorString("", "CONFSUCCES", "", "", conn);
							System.out.println("is local.123............"+connStatus+" ]errString---["+errString);
							if(connStatus)
							{
								conn.commit();
								System.out.println("is local.123............ comiiting");
							}
							System.out.println("Committing.............."+errString);
							return errString;

						}
						else
						{
							isError = true;
							System.out.println("connStatus::::"+connStatus);
							if(connStatus)
							{
								conn.rollback();
								System.out.println("is local.123............rollbacking");
								//return errString;
							}
							System.out.println("retString3123::::"+errString);
							return errString;
						}
						//						
						//System.out.println("errorString miscdrcrrcp CG--->>"+ errString);
					}
				}
				//' DN' = ' Reverse GST DN' --- value is --"RG"---
				// will be used in distribution Order type for transferring material from CFA to Serdia . In this case DN will be generated towards the Supplier in Issuing site (Payable) . In Receiving site CN will be created towards  the Customer (Receivables) .  
				else if("RG".equalsIgnoreCase(recoverTranType.trim()))
				{
					System.out.println("recovertrantype RG--->>"+ recoverTranType);

					// errorString = disisspossave.genMiscDrCrRcp(tranID,recoverTranType, otherSite, xtraParams, conn); 
					System.out.println("is errorstring confsuccess ............"+errString+" ] constatus ---["+connStatus+" ]connection ["+conn);


					errString = genMiscDrCrRcp(tranID,recoverTranType, otherSite, xtraParams, conn); 

					System.out.println("is errorstring rg before confsuccess ............"+errString+" ] constatus ---["+connStatus);
					if(errString!= null && errString.trim().length() > 0)
					{

						System.out.println("is errString inside if of confsuccess............"+errString+" ]constatus"+connStatus);

						if(errString.indexOf("CONFSUCCES") > -1 ||errString.indexOf("SEND_SUCCESS") > -1 || errString.indexOf("VTCONF") > -1)
						{

							strToWrite = createPostLog(lineNo,itemCode,locCode,lotNo,lotSl,quantity,errString);//Modified by Rohini T on 17/03/2021
							fos1.write(strToWrite.getBytes());

							errString = "";
							errString = itmDBAccessEJB.getErrorString("", "CONFSUCCES", "", "", conn);
							if(connStatus)
							{
								conn.commit();
								System.out.println("is local.123 RG............COMMITING");
							}
							//errString=itmDBAccessEJB.getErrorString("","CONFSUCCES","","",conn);
							System.out.println("is errString inside if of confsuccess  2345............"+errString+" ]constatus"+connStatus);
							return errString;
						}
						else
						{
							System.out.println("is errString inside if of confsuccess  2345............"+errString+" ]constatus"+connStatus);

							isError = true;
							if(connStatus) 
							{
								conn.rollback();
								System.out.println("is local.123 RG............ROLLBACKING");

							}
							return errString;
						}
						//						System.out.println("errString345::::"+errString);
						//Added by Anagha R on 23/06/2020 for Serdia - VTCUSTCD4 pop up coming on confirmaiton of Distribution receipt
						//return retString
						///System.out.println("Committing.............."+conn);
						//conn.commit();
						//conn.close();
						//conn=null;
						//						System.out.println("Committing  errorstring 111.............. ["+errString+" ]conn  ["+conn);
						//return errString;

						//System.out.println("retString3::::"+errString);
						//Added by Anagha R on 23/06/2020 for Serdia - VTCUSTCD4 pop up coming on confirmaiton of Distribution receipt
						//return errString;
					}

					System.out.println("errorString miscdrcrrcp RG--->>"+ errString);
				}//end
				else
				{
					errString="";
				}
				System.out.println("errString 1962123: " + errorString+"tranID......"+tranID+"itemCode...."+itemCode+"lineNo....."+lineNo+" ]Constatus ["+connStatus);
				/*if( errString != null && errString.trim().length() > 0 )
									{
										strToWrite = createPostLog(lineNo,itemCode,locCode,lotNo,lotSl,quantity,errString);//Modified by Rohini T on 17/03/2021
										fos1.write(strToWrite.getBytes());
										//Added by Anagha R on 23/06/2020 for Serdia - VTCUSTCD4 pop up coming on confirmaiton of Distribution receipt
										/*if(errString.indexOf("CONFSUCCES") > -1)
										{
											errString = "";
										}
                                        else   
										{
                                            isError = true;
                                            if(connStatus)
                                            {
												conn.rollback();
												return errString;
											}
                                        }*/

				/*System.out.println("retString3::::"+errString);
										//Added by Anagha R on 23/06/2020 for Serdia - VTCUSTCD4 pop up coming on confirmaiton of Distribution receipt
                                        //return errString;
                                        return errString;//added buy monika salla
									}*/

				// Added by Anagha Rane

			}
		}catch(ITMException ie)
		{
			System.out.println("ITMException : "+ie);
			try{
				if(conn!=null && connStatus)
				{
					System.out.println("Exception isLocal Rollbacking..............");
					conn.rollback();
				}

				System.out.println("rollback distorderrcp : ");
				conn.rollback();
			}catch(Exception t){}
			ie.printStackTrace();
			errString = itmDBAccessEJB.getErrorString("","VTDESNCONF","","",conn);
			System.out.println("Returnng String From DistOrderRcpConfEJB :"+errString);
			if(errString != null && errString.trim().length() > 0)//Modified by Rohini T on 16/03/2021
			{
				strToWrite = strToWrite +createPostLog(lineNo,itemCode,locCode,lotNo,lotSl,quantity,errString);
				fos1.write(strToWrite.getBytes());
				isError=true;
				return errString;
			}
		}catch(Exception e){
			System.out.println("Exception in Confirm [DistOrderRcpConfEJB]"+e);
			try{

				System.out.println("rollback distorderrcp 12345: ");
				if(conn!=null && connStatus)
				{
					System.out.println("Exception isLocal Rollbacking..............");
					conn.rollback();
				}

				// conn.rollback();
			}
			catch(Exception t){

			}
			e.printStackTrace();
			errString = itmDBAccessEJB.getErrorString("","VTDESNCONF","","",conn);
			if(errString != null && errString.trim().length() > 0)//Modified by Rohini T on 16/03/2021
			{
				strToWrite = strToWrite +createPostLog(lineNo,itemCode,locCode,lotNo,lotSl,quantity,errString);
				fos1.write(strToWrite.getBytes());
				System.out.println("Returnng String From DistOrderRcpConfEJB :"+errString);
				isError=true;
				return errString;
			}
		}
		finally
		{
			try{
				stkUpdMap = null;
				if(rs != null){rs.close();rs = null;}
				if(rs1 != null){rs1.close();rs1 = null;}
				/*if(conn != null)
				{
					conn = null;
				}*/
				if(pstmt != null)
				{
					pstmt.close();
					pstmt=null;
				}
				if(pstmt1 != null)
				{
					pstmt1.close();
					pstmt1=null;
				}
				if(pstmtUpd != null)
				{
					pstmtUpd.close();
					pstmtUpd=null;
				}
				if(conn!=null)
				{

					if(isError)
					{
						System.out.println("is CONNECTION ............ROLLBACKING");
						conn.rollback();
						//                		conn.close();
						//                		conn=null;
					}
					else
					{
						System.out.println("is CONNECTION  ............COMMITING");
						conn.commit();
						//                		conn.close();
						//                		conn=null;
					}
					/*if(conn!=null && connStatus)
					{
						System.out.println(" finally Rollbacking..............");
						//conn.rollback();
						conn.close();
						conn=null;
					}*/
					if (isError && connStatus)
					{
						System.out.println("actionDistIssueConf Local connection rolledback");
						conn.rollback();
					} 
					else if (!isError && connStatus)
					{
						System.out.println("actionDistIssueConf Local connection committed");
						conn.commit();
						//retString = itmDBAccessEJB.getErrorString("", "VMCPSUCC  ", "", "", conn);
					}
					if (!connStatus)
					{
						System.out.println("actionDistIssueConf not Local connection so not commit or rollback"+connStatus);
					}
				}

			}
			catch(Exception e)
			{
				System.out.println("errString @@@: "+errString);
				if(errString != null && errString.trim().length() > 0)//Modified by Rohini T on 16/03/2021
				{
					strToWrite = strToWrite +createPostLog(lineNo,itemCode,locCode,lotNo,lotSl,quantity,errString);//Modified by Rohini T on 16/03/2021
					fos1.write(strToWrite.getBytes());
					System.out.println("Exception @@@@: "+e);
					e.printStackTrace();
				}


			}}
			System.out.println("Returnng String From DistOrderRcpConfEJB :"+errString);
			return errString;
			//return errorString
		}
		/**
		 * Commented as per suggested [17/NOV/2017] START
		 * */
		//	private String gbfStockAllocate(String tranID, Connection conn,String userId,String termId) throws ITMException 
		//	{
		//		// TODO Auto-generated method stub
		//		String errStr="",sql="",sql1="";
		//		PreparedStatement pstmt=null,pstmt1=null;
		//		ResultSet rs=null,rs1=null;
		//		int liLineNo=0,lcLineDorder=0,lsLineSord=0,llSoCnt=0,insrtCnt=0;
		//		String lsDistOrd="",lsItemcode="",lsLotno="",lsLotsl="",lsLocCode="",lsPackInstr="",lsDimension="",lsSaleorder="";
		//		String lsItemOrd="",lsExpLev="",lsItemref="",lsSitecode="",munit="",mstatus="",lsGrade="",lsSitecodeMfg="",munitStd="";
		//		double lcQuantityAlloc=0.00,ldGrosswt=0.00,ldTarewt=0.00,ldNetwt=0.00,mquantity=0.00,mconvQty=0.00;
		//		Timestamp mexpDate=null,mmfgDate=null;
		//		
		//		HashMap lstrAllocateHashMap=new HashMap();
		//		
		//		try {
		//			sql="select line_no,dist_order,item_code,lot_no,lot_sl,loc_code,quantity,line_no_dist_order,pack_instr,gross_weight,tare_weight,"
		//					+ "net_weight,dimension from distord_rcpdet where tran_id=?";
		//			pstmt=conn.prepareStatement(sql);
		//			pstmt.setString(1, tranID);
		//			rs=pstmt.executeQuery();
		//			while(rs.next())
		//			{				
		//				liLineNo=rs.getInt("line_no");
		//				lsDistOrd=rs.getString("dist_order");
		//				lsItemcode=rs.getString("item_code");
		//				lsLotno=rs.getString("lot_no");
		//				lsLotsl=rs.getString("lot_sl");
		//				lsLocCode=rs.getString("loc_code");
		//				lcQuantityAlloc=rs.getDouble("quantity");
		//				lcLineDorder=rs.getInt("line_no_dist_order");
		//				lsPackInstr=rs.getString("pack_instr");
		//				ldGrosswt=rs.getDouble("gross_weight");
		//				ldTarewt=rs.getDouble("tare_weight");
		//				ldNetwt=rs.getDouble("net_weight");
		//				lsDimension=rs.getString("dimension");
		//				
		//				sql1="select sale_order from distorder where dist_order =?";
		//				pstmt1=conn.prepareStatement(sql1);
		//				pstmt1.setString(1, lsDistOrd);
		//				rs1=pstmt1.executeQuery();
		//				if(rs1.next())
		//				{
		//					lsSaleorder=rs1.getString("sale_order");
		//				}
		//				rs1.close();rs1=null;
		//				pstmt1.close();pstmt1=null;
		//				
		//				sql1="select line_no__sord from distorder_det where dist_order = ? and line_no = ?";
		//				pstmt1=conn.prepareStatement(sql1);
		//				pstmt1.setString(1, lsDistOrd);
		//				pstmt1.setInt(2, lcLineDorder);
		//				rs1=pstmt1.executeQuery();
		//				if(rs1.next())
		//				{
		//					lsLineSord=rs1.getInt("line_no__sord");
		//				}
		//				rs1.close();rs1=null;
		//				pstmt1.close();pstmt1=null;
		//				
		//				sql1="select item_code__ord,exp_lev,item_ref,site_code,quantity - qty_desp as mquantity, unit,status"
		//						+ " from sorditem where sale_order = ? and line_no = ? and line_type = 'I'";
		//				pstmt1=conn.prepareStatement(sql1);
		//				pstmt1.setString(1, lsSaleorder);
		//				pstmt1.setInt(2, lsLineSord);
		//				rs1=pstmt1.executeQuery();
		//				if(rs1.next())
		//				{
		//					lsItemOrd=rs1.getString("item_code__ord");
		//					lsExpLev=rs1.getString("exp_lev");
		//					lsItemref=rs1.getString("item_ref");
		//					lsSitecode=rs1.getString("site_code");
		//					mquantity=rs1.getDouble("mquantity");
		//					munit=rs1.getString("unit");
		//					mstatus=rs1.getString("status");
		//				}
		//				rs1.close();rs1=null;
		//				pstmt1.close();pstmt1=null;
		//				
		//				sql1="select grade,exp_date,mfg_date,site_code__mfg from stock"
		//						+ " where item_code = ? and  site_code = ? and  loc_code = ? and lot_no = ? and lot_sl = ?";
		//				pstmt1=conn.prepareStatement(sql1);
		//				pstmt1.setString(1, lsSaleorder);
		//				pstmt1.setInt(2, lsLineSord);
		//				rs1=pstmt1.executeQuery();
		//				if(rs1.next())
		//				{
		//					lsGrade=rs1.getString("grade");
		//					mexpDate=rs1.getTimestamp("exp_date");
		//					mmfgDate=rs1.getTimestamp("mfg_date");
		//					lsSitecodeMfg=rs1.getString("site_code__mfg");
		//				}
		//				rs1.close();rs1=null;
		//				pstmt1.close();pstmt1=null;
		//				
		//				sql1="select conv__qty_stduom,unit__std from sorddet where sale_order = ? and line_no = ?";
		//				pstmt1=conn.prepareStatement(sql1);
		//				pstmt1.setString(1, lsSaleorder);
		//				pstmt1.setInt(2, lsLineSord);
		//				rs1=pstmt1.executeQuery();
		//				if(rs1.next())
		//				{
		//					mconvQty=rs1.getDouble("conv__qty_stduom");
		//					munitStd=rs1.getString("unit__std");
		//				}
		//				rs1.close();rs1=null;
		//				pstmt1.close();pstmt1=null;
		//				
		//				sql1="select count(*) as ll_so_cnt from sordalloc"
		//						+ " where sale_order = ? and line_no = "
		//						+ " and exp_lev = ? and item_code__ord = ?"
		//						+ " and item_code = ? and lot_no = ?"
		//						+ " and lot_sl = ? and loc_code = ?";
		//				pstmt1=conn.prepareStatement(sql1);
		//				pstmt1.setString(1, lsSaleorder);
		//				pstmt1.setInt(2, lsLineSord);
		//				pstmt1.setString(3, lsExpLev);
		//				pstmt1.setString(4, lsItemOrd);
		//				pstmt1.setString(5, lsItemcode);
		//				pstmt1.setString(6, lsLotno);
		//				pstmt1.setString(7, lsLotsl);
		//				pstmt1.setString(8, lsLocCode);
		//				rs1=pstmt1.executeQuery();
		//				if(rs1.next())
		//				{
		//					llSoCnt=rs1.getInt("ll_so_cnt");
		//				}
		//				rs1.close();rs1=null;
		//				pstmt1.close();pstmt1=null;
		//				
		//				if(llSoCnt>0)
		//				{
		//					sql="insert into sordalloc (sale_order,line_no,exp_lev,item_code__ord,item_code,lot_no,"
		//							+ " lot_sl,loc_code,item_ref,quantity,unit,qty_alloc,date_alloc,"
		//							+ " status,item_grade,exp_date,alloc_mode,site_code,conv__qty_stduom,"
		//							+ " unit__std,quantity__stduom,mfg_date,site_code__mfg,ref_id__alloc,ref_line__no)"
		//							+ " values(?,?,?,?,?,?,?,?,?,?,?,'SYSDATE',?,?,?,?,'A',?,?,?,? * ? ,?,?,?,?)";
		//					pstmt=conn.prepareStatement(sql);
		//					pstmt.setString(1, lsSaleorder);
		//					pstmt.setInt(2, lsLineSord);
		//					pstmt.setString(3, lsExpLev);
		//					pstmt.setString(4, lsItemOrd);
		//					pstmt.setString(5, lsItemcode);
		//					pstmt.setString(6, lsLotno);
		//					pstmt.setString(7, lsLotsl);
		//					pstmt.setString(8, lsLocCode);
		//					pstmt.setString(9, lsItemref);
		//					pstmt.setDouble(10, mquantity);
		//					pstmt.setString(11, munit);
		//					pstmt.setDouble(12, lcQuantityAlloc);
		//					pstmt.setString(13, mstatus);
		//					pstmt.setString(14, lsGrade);
		//					pstmt.setTimestamp(15, mexpDate);
		//					pstmt.setString(16, lsSitecode);
		//					pstmt.setDouble(17, mconvQty);
		//					pstmt.setString(18, munitStd);
		//					pstmt.setDouble(19, lcQuantityAlloc);
		//					pstmt.setDouble(20, mconvQty);
		//					pstmt.setTimestamp(21, mmfgDate);
		//					pstmt.setString(22, lsSitecodeMfg);
		//					pstmt.setString(23, tranID);
		//					pstmt.setInt(24, liLineNo);
		//					insrtCnt=pstmt.executeUpdate();
		//					pstmt.close();pstmt=null;
		//					
		//					System.out.println("@V@ insrtCnt 2076:- ["+insrtCnt+"]");
		//				}
		//				else
		//				{
		//					sql="update sordalloc set qty_alloc = qty_alloc + ?, quantity__stduom = (qty_alloc + ?) * ?, status = 'D',"
		//							+ " ref_id__alloc = ?,ref_line__no  = ? where sale_order = ? and line_no = ?"
		//							+ " and exp_lev = ? and item_code__ord = ? and item_code = ? and lot_no = ? and lot_sl = ? and loc_code = ?";
		//					pstmt=conn.prepareStatement(sql);
		//					pstmt.setDouble(1, lcQuantityAlloc);
		//					pstmt.setDouble(2, lcQuantityAlloc);
		//					pstmt.setDouble(3, mconvQty);
		//					pstmt.setString(4, tranID);
		//					pstmt.setInt(5, liLineNo);
		//					pstmt.setString(6, lsSaleorder);
		//					pstmt.setInt(7, lsLineSord);
		//					pstmt.setString(8, lsExpLev);
		//					pstmt.setString(9, lsItemOrd);
		//					pstmt.setString(10, lsItemcode);
		//					pstmt.setString(11, lsLotno);
		//					pstmt.setString(12, lsLotsl);
		//					pstmt.setString(13, lsLocCode);
		//					insrtCnt=pstmt.executeUpdate();
		//					pstmt.close();pstmt=null;
		//					
		//					System.out.println("@V@ update count 2100 :- ["+insrtCnt+"]");
		//				}
		//				
		//				sql="update sorder set alloc_flag = 'Y' where sale_order =?";
		//				pstmt=conn.prepareStatement(sql);
		//				pstmt.setString(1, lsSaleorder);
		//				insrtCnt=pstmt.executeUpdate();
		//				pstmt.close();pstmt=null;
		//				
		//				System.out.println("@V@ update count 2104 :- ["+insrtCnt+"]");
		//				
		//				sql="update sorditem set qty_alloc = qty_alloc + ? where sale_order = ? and line_no = ? and exp_lev =?";
		//				pstmt=conn.prepareStatement(sql);
		//				pstmt.setDouble(1, lcQuantityAlloc);
		//				pstmt.setString(2, lsSaleorder);
		//				pstmt.setInt(3, lsLineSord);
		//				pstmt.setString(4, lsExpLev);
		//				insrtCnt=pstmt.executeUpdate();
		//				pstmt.close();pstmt=null;
		//				
		//				System.out.println("@V@ update count 2120 :- ["+insrtCnt+"]");
		//				
		//				sql="update stock set gross_weight = ?, tare_weight = ?, net_weight = ?, pack_instr = ?,"
		//						+ " dimension  = ? where item_code = ? and site_code = ?"
		//						+ " and loc_code  = ? and lot_no    = ? and lot_sl    =?";
		//				pstmt=conn.prepareStatement(sql);
		//				pstmt.setDouble(1, ldGrosswt);
		//				pstmt.setDouble(2, ldTarewt);
		//				pstmt.setDouble(3, ldNetwt);
		//				pstmt.setString(4, lsPackInstr);
		//				pstmt.setString(5, lsDimension);
		//				pstmt.setString(6, lsItemcode);
		//				pstmt.setString(7, lsSitecode);
		//				pstmt.setString(8, lsLocCode);
		//				pstmt.setString(9, lsLotno);
		//				pstmt.setString(10, lsLotsl);
		//				insrtCnt=pstmt.executeUpdate();
		//				pstmt.close();pstmt=null;
		//				
		//				System.out.println("@V@ update count 2139 :- ["+insrtCnt+"]");
		//				
		//				lstrAllocateHashMap.put("ref_ser", "D-RCP");
		//				lstrAllocateHashMap.put("ref_id", tranID);
		//				lstrAllocateHashMap.put("ref_line", liLineNo);
		//				lstrAllocateHashMap.put("site_code", lsSitecode);
		//				lstrAllocateHashMap.put("item_code", lsItemcode);
		//				lstrAllocateHashMap.put("loc_code", lsLocCode);
		//				lstrAllocateHashMap.put("lot_no", lsLotno);
		//				lstrAllocateHashMap.put("lot_sl", lsLotsl);
		//				lstrAllocateHashMap.put("alloc_qty", lcQuantityAlloc);
		//				lstrAllocateHashMap.put("lot_sl", lsLotsl);
		//				lstrAllocateHashMap.put("chg_user", userId);
		//				lstrAllocateHashMap.put("chg_term", termId);
		//				lstrAllocateHashMap.put("chg_win", "W_DIST_RECEIPT");
		//				
		//				
		//				InvAllocTraceBean invBean = new InvAllocTraceBean(); 
		//				errStr = invBean.updateInvallocTrace(lstrAllocateHashMap,conn);
		////				errStr = gbfUpdAllocTrace(lstrAllocateHashMap,conn,userId);
		//				
		//			}
		//			rs.close();rs=null;
		//			pstmt.close();pstmt=null;
		//		} 
		//		catch (SQLException e) 
		//		{
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//			throw new ITMException(e);
		//		} catch (Exception e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//			throw new ITMException(e);
		//		}
		//		finally
		//		{
		//			try {
		//				if(pstmt!=null)
		//				{
		//					pstmt.close();
		//					pstmt=null;
		//				}
		//				if(pstmt1!=null)
		//				{
		//					pstmt1.close();
		//					pstmt1=null;
		//				}
		//				if(rs!=null)
		//				{
		//					rs.close();
		//					rs=null;
		//				}
		//				if(rs1!=null)
		//				{
		//					rs1.close();
		//					rs1=null;
		//				}
		//			} catch (SQLException e) {
		//				// TODO Auto-generated catch block
		//				e.printStackTrace();
		//			}
		//		}
		//		return errStr;
		//	}
		/**
		 * Commented as per suggested [17/NOV/2017] START
		 * */
		//	private String gbfUpdAllocTrace(HashMap<String, String> lstrAllocateHashMap, Connection conn,String userId) throws ITMException 
		//	{
		//		// TODO Auto-generated method stub
		//		String errStr="",sql="",lsStkOpt="",lsKeystr="",sql1="",lsTRanId="",holdLock="N";
		//		PreparedStatement pstmt=null,pstmt1=null;
		//		ResultSet rs=null,rs1=null;
		//		int llCnt=0,insrtCnt=0;
		//		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		//		double lcTempallocqty=0.00;
		//		
		//		try {
		//			sql="select stk_opt from item where item_code =?";
		//			pstmt=conn.prepareStatement(sql);
		//			pstmt.setString(1, lstrAllocateHashMap.get("item_code"));
		//			rs=pstmt.executeQuery();
		//			if(rs.next())
		//			{
		//				lsStkOpt=rs.getString("stk_opt");
		//			}
		//			rs.close();rs=null;
		//			pstmt.close();pstmt=null;
		//			
		//			sql="select key_string from transetup where upper(tran_window) = 'T_ALLOCTRACE'";
		//			pstmt=conn.prepareStatement(sql);
		//			rs=pstmt.executeQuery();
		//			if(rs.next())
		//			{
		//				lsKeystr=rs.getString("key_string");
		//				
		//				if(lsKeystr==null || lsKeystr.trim().length()==0)
		//				{
		//					sql1="select key_string from transetup where upper(tran_window) = 'GENERAL'";
		//					pstmt1=conn.prepareStatement(sql1);
		//					rs1=pstmt1.executeQuery();
		//					if(rs1.next())
		//					{
		//						lsKeystr=rs.getString("key_string");
		//					}
		//					rs1.close();rs1=null;
		//					pstmt1.close();pstmt1=null;
		//				}
		//			}
		//			rs.close();rs=null;
		//			pstmt.close();pstmt=null;
		//			
		//			
		//			String xmlValues = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
		//			xmlValues = xmlValues + "<Header></Header>";
		//			xmlValues = xmlValues + "<Detail1>";
		//			xmlValues = xmlValues + "<tran_id></tran_id>";
		//			xmlValues = xmlValues + "<site_code>"+lstrAllocateHashMap.get("site_code")+"</site_code>";
		//			xmlValues = xmlValues + "<tran_date>"+getCurrdateAppFormat()+"</tran_date>";
		//			xmlValues = xmlValues + "</Detail1></Root>";
		//			
		//			lsTRanId=generateTranIdForAllocTrace("T_ALLOCTRACE",xmlValues,userId,conn);
		//			
		//			if("N".equalsIgnoreCase(holdLock) && !"QC-ORD".equalsIgnoreCase(lstrAllocateHashMap.get("ref_ser").toString()))
		//			{
		//				sql="Select count(1) as ll_count From inv_hold a, inv_hold_det b"
		//						+ " Where a.tran_id = b.tran_id And b.item_code  = ?"
		//						+ " And (b.site_code = ? or b.site_code is null )"
		//						+ " And (b.loc_code  = ?  or b.loc_code is null )"
		//						+ " And (b.lot_no    = ?  or b.lot_no is null )"
		//						+ " And (b.lot_sl    = ?   or b.lot_sl is null )"
		//						+ " And a.confirmed='Y' And b.hold_status ='H'";
		//				pstmt=conn.prepareStatement(sql);
		//				pstmt.setString(1, lstrAllocateHashMap.get("item_code"));
		//				pstmt.setString(2, lstrAllocateHashMap.get("site_code"));
		//				pstmt.setString(3, lstrAllocateHashMap.get("loc_code"));
		//				pstmt.setString(4, lstrAllocateHashMap.get("lot_no"));
		//				pstmt.setString(5, lstrAllocateHashMap.get("lot_sl"));
		//				rs=pstmt.executeQuery();
		//				if(rs.next())
		//				{
		//					llCnt=rs.getInt("ll_count");
		//				}
		//				rs.close();rs=null;
		//				pstmt.close();pstmt=null;
		//				
		//				if(llCnt>0)
		//				{
		//					errStr=itmDBAccessEJB.getErrorString("","VTSTKHOLD","","",conn);
		//				}
		//			}
		//			if("db2".equalsIgnoreCase(commonConstants.DB_NAME) || "mysql".equalsIgnoreCase(commonConstants.DB_NAME))
		//			{
		//				sql="select alloc_qty  from stock where item_code = ?"
		//						+ " and site_code = ? and loc_code = ?"
		//						+ " and lot_no = ? and lot_sl = ? for update";
		//			}
		//			else if("mssql".equalsIgnoreCase(commonConstants.DB_NAME))
		//			{
		//				sql="select alloc_qty from stock (updlock)"
		//						+ " where item_code = ? and site_code = ?"
		//						+ " and loc_code = ? and lot_no = ? and lot_sl = ?";
		//			}
		//			else
		//			{
		//				sql="select  alloc_qty from stock where item_code = ?"
		//						+ " and site_code = ? and loc_code = ?"
		//						+ " and lot_no = ? and lot_sl = ? for update nowait";
		//			}
		//			pstmt=conn.prepareStatement(sql);
		//			pstmt.setString(1, lstrAllocateHashMap.get("item_code"));
		//			pstmt.setString(2, lstrAllocateHashMap.get("site_code"));
		//			pstmt.setString(3, lstrAllocateHashMap.get("loc_code"));
		//			pstmt.setString(4, lstrAllocateHashMap.get("lot_no"));
		//			pstmt.setString(5, lstrAllocateHashMap.get("lot_sl"));
		//			rs=pstmt.executeQuery();
		//			if(rs.next())
		//			{
		//				lcTempallocqty=rs.getDouble("alloc_qty");
		//			}
		//			rs.close();rs=null;
		//			pstmt.close();pstmt=null;
		//			
		//			sql="Insert into invalloc_trace (tran_id, tran_date, ref_ser, ref_id, ref_line, item_code, site_code, loc_code, lot_no, lot_sl,"
		//					+ " alloc_qty, chg_win, chg_user, chg_term, chg_date )"
		//					+ " values (?,SYSDATE,?,?,?,?,?,?,?,?,?,?,?,?,SYSDATE)";
		//			pstmt=conn.prepareStatement(sql);
		//			pstmt.setString(1, lsTRanId);
		//			pstmt.setString(2, lstrAllocateHashMap.get("ref_ser"));
		//			pstmt.setString(3, lstrAllocateHashMap.get("ref_id"));
		//			pstmt.setString(4, lstrAllocateHashMap.get("ref_line"));
		//			pstmt.setString(5, lstrAllocateHashMap.get("item_code"));
		//			pstmt.setString(6, lstrAllocateHashMap.get("site_code"));
		//			pstmt.setString(7, lstrAllocateHashMap.get("loc_code"));
		//			pstmt.setString(8, lstrAllocateHashMap.get("lot_no"));
		//			pstmt.setString(9, lstrAllocateHashMap.get("lot_sl"));
		//			pstmt.setString(10, lstrAllocateHashMap.get("alloc_qty"));
		//			pstmt.setString(11, lstrAllocateHashMap.get("chg_win"));
		//			pstmt.setString(12, lstrAllocateHashMap.get("chg_user"));
		//			pstmt.setString(13, lstrAllocateHashMap.get("chg_term"));
		//			insrtCnt=pstmt.executeUpdate();
		//			pstmt.close();pstmt=null;
		//			
		//			System.out.println("@V@ Insert count 2306 :- ["+insrtCnt+"]");
		//			
		//			sql="update stock set alloc_qty = (case when alloc_qty is null then 0 else alloc_qty end) + ?"
		//					+ " where item_code = ? and site_code = ? and loc_code = ? and lot_no = ? and lot_sl = ?";
		//			pstmt=conn.prepareStatement(sql);
		//			pstmt.setString(1, lstrAllocateHashMap.get("alloc_qty"));
		//			pstmt.setString(2, lstrAllocateHashMap.get("item_code"));
		//			pstmt.setString(3, lstrAllocateHashMap.get("site_code"));
		//			pstmt.setString(4, lstrAllocateHashMap.get("loc_code"));
		//			pstmt.setString(5, lstrAllocateHashMap.get("lot_no"));
		//			pstmt.setString(6, lstrAllocateHashMap.get("lot_sl"));
		//			insrtCnt=pstmt.executeUpdate();
		//			pstmt.close();pstmt=null;
		//			
		//			System.out.println("@V@ update count 2324:- ["+insrtCnt+"]");
		//			
		//		} 
		//		catch (SQLException e) 
		//		{
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		} catch (ITMException e) 
		//		{
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		} catch (RemoteException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
		//		finally
		//		{
		//			try {
		//				if(pstmt!=null)
		//				{
		//					pstmt.close();
		//					pstmt=null;
		//				}
		//				if(pstmt1!=null)
		//				{
		//					pstmt1.close();
		//					pstmt1=null;
		//				}
		//				if(rs!=null)
		//				{
		//					rs.close();
		//					rs=null;
		//				}
		//				if(rs1!=null)
		//				{
		//					rs1.close();
		//					rs1=null;
		//				}
		//			} catch (SQLException e) {
		//				// TODO Auto-generated catch block
		//				e.printStackTrace();
		//				throw new ITMException(e);
		//			}
		//		}
		//		
		//		return errStr;
		//	}
		/**
		 * VALLABH KADAM [13/NOV/2017]
		 * To generate new tran id for Alloc trace
		 * */
		private static String generateTranIdForAllocTrace(String tranWindow, String xmlValues,String loginCode, Connection conn) throws ITMException {
			Statement stmt = null;
			ResultSet rs = null;
			String sql = "", batchId = "", str3 = "";
			try {
				sql = "SELECT KEY_STRING, TRAN_ID_COL, REF_SER FROM TRANSETUP WHERE TRAN_WINDOW = '"+ tranWindow +"'";
				System.out.println("sql for generatTranId [" + sql + "]");
				stmt = conn.createStatement();
				rs = stmt.executeQuery(sql);

				System.out.println("keyString :" + rs.toString());
				String refSer = "", keyString = "", tranIdCol = "";
				if (rs.next()) {
					keyString = rs.getString(1);
					tranIdCol = rs.getString(2);
					refSer = rs.getString(3);
				}

				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (stmt != null) {
					stmt.close();
					stmt = null;
				}
				//			String strXml = "";
				//			strXml = "<?xml version=\"1.0\" encoding='" + CommonConstants.ENCODING + "'?>\r\n<Root>";
				//			strXml = strXml + "<Header></Header>";
				//			strXml = strXml + "<Detail1>";
				//			strXml = strXml + "<tran_id/>";
				//			strXml = strXml + "<tran_date>" + tranDate + "</tran_date>";
				//			strXml = strXml + "<site_code>" + siteCode + "</site_code>";
				//			strXml = strXml + "</Detail1></Root>";

				TransIDGenerator localTransIDGenerator = new TransIDGenerator(xmlValues, loginCode, CommonConstants.DB_NAME);
				batchId = localTransIDGenerator.generateTranSeqID(refSer, tranIdCol, keyString, conn);
			} catch (Exception e) {
				System.out.println("Exception ::" + e.getMessage() + ":");
				e.printStackTrace();
				throw new ITMException(e);
			} finally {
				try {
					if (rs != null) {
						rs.close();
						rs = null;
					}
					if (stmt != null) {
						stmt.close();
						stmt = null;
					}
				} catch (SQLException localSQLException3) {
					System.out.println("Connection not Closed....");
				}
			}
			System.out.println("@V@ New Generated batchId...." + batchId);
			return batchId;
		}
		private String getItemSer(String itemCode,String siteCode,Timestamp tranDate,Connection conn) throws ITMException
		{
			String itemSer ="",sql ="";
			PreparedStatement pstmt =null;
			ResultSet rs =null;
			try
			{
				sql ="select item_ser from siteitem where site_code = ? and  "
						+" item_code = ?  ";
				//System.out.println("sql-------------"+sql);
				pstmt =   conn.prepareStatement(sql);
				pstmt.setString(1,siteCode);
				pstmt.setString(2,itemCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					itemSer=rs.getString("item_ser");
				}
				rs.close();
				pstmt.close();
				if(itemSer == null || itemSer.trim().length() == 0)
				{
					sql ="select item_ser from itemser_change where  "
							+" item_code = ? and "
							+"	eff_date <= ? and (	valid_upto > = ? or valid_upto is null )	";
					//System.out.println("sql-------------"+sql);
					pstmt =   conn.prepareStatement(sql);
					pstmt.setString(1,itemCode);
					pstmt.setTimestamp(2,tranDate);
					pstmt.setTimestamp(3,tranDate);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						itemSer=rs.getString("item_ser");
					}
					rs.close();
					pstmt.close();
				}
				if(itemSer == null || itemSer.trim().length() == 0)
				{
					sql ="select item_ser__old from itemser_change where  "
							+" item_code = ? and "
							+"	eff_date <= ? and (	valid_upto > =? or valid_upto is null )	"
							+" and eff_date = ( select min(eff_date) from itemser_change where item_code = ? )";
					//System.out.println("sql-------------"+sql);
					pstmt =   conn.prepareStatement(sql);
					pstmt.setString(1,itemCode);
					pstmt.setTimestamp(2,tranDate);
					pstmt.setTimestamp(3,tranDate);
					pstmt.setString(4,itemCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						itemSer=rs.getString("item_ser");
					}
					rs.close();
					pstmt.close();
				}
				if(itemSer == null || itemSer.trim().length() == 0)
				{
					sql ="select item_ser from item where  "
							+" item_code = ? ";
					//System.out.println("sql-------------"+sql);
					pstmt =   conn.prepareStatement(sql);
					pstmt.setString(1,itemCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						itemSer=rs.getString("item_ser");
					}
					rs.close();
					pstmt.close();
				}
			}
			catch(Exception e)
			{
				System.out.println("Exception ::" +sql+ e.getMessage() + ":");
				e.printStackTrace();
				throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
			}

			return itemSer;

		}
		private String generateTranId(String windowName,String xmlValues,Connection conn) throws ITMException
		{
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			String sql = "",errCode ="",errString ="";
			String tranId = null;
			String newKeystring = "";
			String srType = "RS";
			boolean found =false;
			boolean isError = false;
			ITMDBAccessEJB itmDBAccessEJB = null;
			try
			{
				itmDBAccessEJB = new ITMDBAccessEJB();
				sql = "SELECT KEY_STRING, TRAN_ID_COL, REF_SER FROM TRANSETUP WHERE UPPER(TRAN_WINDOW)=UPPER(?)";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,windowName);
				rs = pstmt.executeQuery();
				System.out.println("keyString :"+rs.toString());
				String tranSer1 = "";
				String keyString = "";
				String keyCol = "";
				if (rs.next())
				{
					found =true;
					keyString = rs.getString(1);
					keyCol = rs.getString(2);
					tranSer1 = rs.getString(3);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(!found)
				{
					sql ="SELECT key_string,TRAN_ID_COL, REF_SER from transetup where tran_window = 'GENERAL' ";
					pstmt	=  conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						keyString = rs.getString(1);
						keyCol = rs.getString(2);
						tranSer1 = rs.getString(3);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null ;
				}
				if(keyString ==null || keyString.trim().length() ==0)
				{
					errCode = "VTSEQ";
					System.out.println("errcode......"+errCode);
					errString = itmDBAccessEJB.getErrorString("","VTSEQ","BASE","",conn);
					//Modified by Rohini T on [01/03/2021][Start]
					if(errString != null && errString.trim().length() > 0)
					{
						logMsg = "The logic for the autogeneration of transaction numbers for this option is not defined";
						strToWrite = createPostLog("","","","","",0,logMsg);
						fos1.write(strToWrite.getBytes());
						isError = true;
						return errString;
					}
					//Modified by Rohini T on [01/03/2021][End]

				}
				System.out.println("keyString=>"+keyString);
				System.out.println("keyCol=>"+keyCol);
				System.out.println("tranSer1"+tranSer1);

				System.out.println("xmlValues  :["+xmlValues+"]");

				TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", CommonConstants.DB_NAME);
				tranId = tg.generateTranSeqID(tranSer1, keyCol, keyString, conn);

				System.out.println(" new tranId :"+tranId);
				if(rs!=null)
				{
					rs.close();
				}
				if(pstmt!=null)
				{
					pstmt.close();
				}
			}
			catch(SQLException ex)
			{
				System.out.println("Exception ::" +sql+ ex.getMessage() + ":");
				ex.printStackTrace();
				tranId=null;
				throw new ITMException(ex); //Added By Mukesh Chauhan on 07/08/19
			}
			catch(Exception e)
			{
				System.out.println("Exception ::" + e.getMessage() + ":");
				e.printStackTrace();
				tranId=null;
				throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
			}
			return tranId;
		}//generateTranTd()
		private String createQCOrder(String tranId,String SiteCode,String xtraParams,Connection conn) throws ITMException
		{
			PreparedStatement pstmt = null,pstmt1 = null,pstmt2 = null;
			ResultSet rs = null,rs1 = null,rs2 = null;
			Timestamp expDate = null,mfgDate = null,dueDate =null, retestDate = null;;
			String itemCode ="",lotSl = "",	unit = "",locCode ="",lotNo = "",batchNo = "",suppCodeMfg = "",generateLotNo = "",lotNoOld = "",batchNoOld = "";
			String qcReqd ="",itemSer = "",sql ="",errString ="",userId ="",errCode = "",suppCode = "";
			String qcNo ="",locCodeAprv ="",locCodeRej ="",empCode ="",sqlInsert ="",siteCodeShip = "";
			HashMap qcUpdMap = null	;
			double quantity =0,qtySample =0,qcLeadTime =0;
			double qcleadtimeSiteitem =0,qcCycleTime =0,qcLeadTimeItem =0;
			int ctr = 0,upd =0,count = 0;
			String geneLotNo = "";
			String generateLot= "";
			String lineNo = "";
			ITMDBAccessEJB itmDBAccessEJB = null;
			String tranSer = "D-RCP";
			String termId="";
			boolean isError = false;
			//GenericUtility genericUtility = null;
			try
			{
				System.out.println("@V@ xtraParams 1850:- ["+xtraParams+"]");

				itmDBAccessEJB = new ITMDBAccessEJB();
				//genericUtility = GenericUtility.getInstance();
				E12GenericUtility genericUtility = new E12GenericUtility();

				//added by azhar[31/JAN/2017][START]
				sql = "select site_code__ship from distord_rcp where tran_id = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				if(rs.next()){
					siteCodeShip = checkNullAndTrim(rs.getString("site_code__ship"));
				}
				System.out.println("sitecode ship::" + siteCodeShip);

				closeResources(pstmt, rs);

				sql = "select supp_code from site where site_code = '" +siteCodeShip+"'";
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if(rs.next()){
					suppCode = checkNullAndTrim(rs.getString("supp_code"));
				}
				System.out.println("supp code::" + suppCode);

				closeResources(pstmt, rs);

				//added by azhar[31/JAN/2017][END]

				//commented by azhar[31/JAN/2017][START]
				/*sql = "select  item_code, sum(quantity) ,count(1), max(lot_sl),  max(unit), loc_code, "
				 +" (CASE		WHEN lot_no IS NULL THEN ' '		ELSE lot_no 	END ) as lot_no, "
				 +" min(exp_date),(CASE 	WHEN batch_no IS NULL THEN ' '	ELSE batch_no 	End ) as batch_no,  "
				 +" Min(line_no), min(mfg_date) from distord_rcpdet where  tran_id = ? "
				 +" Group by loc_code, item_code, lot_no, batch_no"
				 +" Order by loc_code, item_code, lot_no, batch_no";*/

				//commented by azhar[31/JAN/2017][END]

				//added by azhar[31/JAN/2017][START]
				sql = "select item_code,sum(quantity) ,count(1), max(lot_sl), max(unit),loc_code," +
						"(CASE WHEN lot_no IS NULL THEN ' '	ELSE lot_no END ) as lot_no," +
						" min(exp_date)," +
						"(CASE 	WHEN batch_no IS NULL THEN ' ' ELSE batch_no End) as batch_no," +
						" Min(line_no), min(mfg_date) , min(retest_date), supp_code__mfg"
						+ " from distord_rcpdet where  tran_id = ?" +
						" Group by loc_code, item_code, lot_no, batch_no , supp_code__mfg" +
						" Order by loc_code, item_code, lot_no, batch_no , supp_code__mfg";

				//added by azhar[31/JAN/2017][END]

				//System.out.println("sql..........."+sql);
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,tranId);
				rs = pstmt.executeQuery();
				while (rs.next())
				{
					itemCode = rs.getString(1);
					quantity = rs.getDouble(2);
					ctr = rs.getInt(3);
					lotSl = rs.getString(4);
					unit = rs.getString(5);
					locCode = rs.getString(6);
					lotNo = rs.getString(7);
					expDate = rs.getTimestamp(8);
					batchNo = rs.getString(9);
					lineNo = rs.getString(10);
					mfgDate = rs.getTimestamp(11);
					retestDate = rs.getTimestamp(12);  // added by azhar[31/JAN/2017]
					suppCodeMfg = checkNullAndTrim(rs.getString("supp_code__mfg")); // added by azhar[31/JAN/2017]

					lotNoOld = lotNo; //added by azhar[02/MAR/2017]
					batchNoOld = batchNo; //added by azhar[02/MAR/2017]
					System.out.println("lot no old::" + lotNoOld+":;batch no old["+batchNoOld+"]");



					if(ctr > 0)
					{
						lotSl ="";
					}

					//added by azhar[31/JAN/2017][START]
					sql = "select  QTY_SAMPLE, QC_REQD ,PROC_MTH ,spec_ref from siteitem where site_code = ? and item_code = ?" ;
					pstmt1 =  conn.prepareStatement(sql);
					pstmt1.setString(1,SiteCode);
					pstmt1.setString(2,itemCode);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						qtySample	 = rs1.getDouble("QTY_SAMPLE");

					}else{
						sql = "select  qty_sample,qc_reqd,proc_mth from item where item_code = ?";
						pstmt2 =  conn.prepareStatement(sql);
						pstmt2.setString(1,itemCode);
						rs2 = pstmt1.executeQuery();
						if (rs2.next())
						{
							qtySample	 = rs2.getDouble("QTY_SAMPLE");
							//qcReqd = rs1.getString("QC_REQD");
						}

					}
					closeResources(pstmt1,rs1);
					closeResources(pstmt2,rs2);



					if(suppCodeMfg.length() == 0)
					{
						sql = "select count(*) as count from item_lot_info where item_code = ? and lot_no = ?";
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1, itemCode);
						pstmt1.setString(2, lotNo);
						rs1 = pstmt1.executeQuery();
						if(rs1.next()){
							//						count = rs.getInt("count");
							count = rs1.getInt("count");
						}
						closeResources(pstmt1, rs1);
						if(count > 0){

							sql = "select supp_code__mfg,supp_code from item_lot_info where item_code = ? and lot_no = ?";
							pstmt1 = conn.prepareStatement(sql);
							pstmt1.setString(1, itemCode);
							pstmt1.setString(2, lotNo);
							rs1 = pstmt1.executeQuery();
							if(rs1.next()){
								suppCodeMfg = checkNullAndTrim(rs1.getString("supp_code__mfg"));
								System.out.println("supp code mfg:::" + suppCodeMfg);
								//Modified By Umakanta Das on 09-OCT-2017[To get Suppcode]Start
								suppCode = checkNullAndTrim(rs1.getString("supp_code"));
								//Modified By Umakanta Das on 09-OCT-2017[To get Suppcode]Start
							}

							closeResources(pstmt1, rs1);

							if(suppCodeMfg.length() == 0){
								sql = "select supp_code__mfg from stock where item_code = ? and site_code = ? and loc_code  = ? and lot_no =? and lot_sl = ?";	
								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, itemCode);
								pstmt1.setString(2, SiteCode);
								pstmt1.setString(3, locCode);
								pstmt1.setString(4, lotNo);
								pstmt1.setString(5, lotSl);
								rs1 = pstmt1.executeQuery();
								if(rs1.next()){
									suppCodeMfg = checkNullAndTrim(rs1.getString("supp_code__mfg"));
									System.out.println("supp code mfg from stock in if condition:::" + suppCodeMfg);
								}
							}
							closeResources(pstmt1, rs1);
						}else{

							sql = "select supp_code__mfg from stock where item_code = ? and site_code = ? and loc_code  = ? and lot_no =? and lot_sl = ?";	
							pstmt1 = conn.prepareStatement(sql);
							pstmt1.setString(1, itemCode);
							pstmt1.setString(2, SiteCode);
							pstmt1.setString(3, locCode);
							pstmt1.setString(4, lotNo);
							pstmt1.setString(5, lotSl);
							rs1 = pstmt1.executeQuery();
							if(rs1.next()){
								suppCodeMfg = checkNullAndTrim(rs1.getString("supp_code__mfg"));
								System.out.println("supp code mfg from stock in if condition:::" + suppCodeMfg);
							}
						}
						closeResources(pstmt1, rs1);
					}
					System.out.println("@V@ suppCodeMfg :- 2017 ["+suppCodeMfg+"]");
					System.out.println("@V@ mfgDate :- 2019 ["+mfgDate+"]");

					//added by azhar[31/JAN/2017][END]


					//commented by azhar[31/JAN/2017][START]

					/*sql = "select  QTY_SAMPLE, QC_REQD  "
				 +" from siteitem "
				 +" where site_code = ? "
				 +" and  item_code  = ? "  ;
				pstmt1 =  conn.prepareStatement(sql);
				pstmt1.setString(1,SiteCode);
				pstmt1.setString(2,itemCode);
				rs1 = pstmt1.executeQuery();
				if (rs1.next())
				{
					  qtySample	 = rs1.getDouble("QTY_SAMPLE");
					  //qcReqd = rs1.getString("QC_REQD");
				}
				else
				{
					sql = "select  QTY_SAMPLE, QC_REQD  "
					 +" from item "
					 +" Where item_code  = ? " ;
					pstmt1 =  conn.prepareStatement(sql);
					pstmt1.setString(1,itemCode);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						  qtySample	 = rs1.getDouble("QTY_SAMPLE");
						  //qcReqd = rs1.getString("QC_REQD");
					}
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;*/

					//commented by azhar[31/JAN/2017][END]
					if(batchNo == null || batchNo.trim().length() ==0)
					{
						batchNo = lotNo;
					}

					//changed by chaitali on 30-05-2019 [Start]
					sql= "select qc_reqd from siteitem where site_code=? and item_code =?";
					pstmt1=conn.prepareStatement(sql);
					pstmt1.setString(1, SiteCode);
					pstmt1.setString(2,itemCode);
					rs1=pstmt1.executeQuery();
					if(rs1.next())
					{
						qcReqd = rs1.getString("qc_reqd");
					}
					closeResources(pstmt1, rs1);

					if(qcReqd==null || qcReqd.trim().length()==0)
					{
						sql = "select qc_reqd from distorder_type where tran_type in " +
								"(select tran_type from distorder where dist_order in(select dist_order from distord_rcp where tran_id = ?))"; //TODO
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1, tranId);
						rs1 = pstmt1.executeQuery();
						if(rs1.next()){
							qcReqd = rs1.getString("qc_reqd");
						}
						qcReqd = qcReqd == null ? "N" : qcReqd.trim();

						System.out.println("qcReqd ........"+qcReqd);
					}
					closeResources(pstmt1, rs1);

					//changed by chaitali on 30-05-2019 [End]

					if("Y".equalsIgnoreCase(qcReqd))
					{
						lineNo = "   "+lineNo.trim() ;
						lineNo = lineNo.substring(lineNo.length()-3);

						Calendar rightNow = Calendar.getInstance();
						java.util.Date today =new java.util.Date();
						System.out.println("today..........."+today);
						rightNow.setTime(today);
						/*sql = "select case when qc_lead_time is null then 0 else qc_lead_time end "
					 +" from item "
					 +" where  item_code  = ? " ;
					pstmt1 =  conn.prepareStatement(sql);
					pstmt1.setString(1,itemCode);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						  qcLeadTime = rs1.getDouble(1);
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
						 */

						sql = "select qc_cycle_time, qc_lead_time "
								+" from item "
								+" where  item_code  = ? " ;
						pstmt1 =  conn.prepareStatement(sql);
						pstmt1.setString(1,itemCode);
						rs1 = pstmt1.executeQuery();
						if (rs1.next())
						{
							qcCycleTime	 = rs1.getDouble("qc_cycle_time");
							qcLeadTimeItem	 = rs1.getDouble("qc_lead_time");
							qcLeadTime = qcLeadTimeItem;
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;


						rightNow.add(Calendar.DATE,(int)qcLeadTime);
						dueDate = new Timestamp(rightNow.getTimeInMillis());

						sql = "select qc_lead_time "
								+" from siteitem "
								+" where  item_code  = ? "
								+" and site_code = ? " ;
						pstmt1 =  conn.prepareStatement(sql);
						pstmt1.setString(1,itemCode);
						pstmt1.setString(2,SiteCode);
						rs1 = pstmt1.executeQuery();
						if (rs1.next())
						{
							qcleadtimeSiteitem	 = rs1.getDouble("qc_lead_time");
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;

						if(qcleadtimeSiteitem == 0)
						{
							qcLeadTime = qcLeadTimeItem;
						}
						else
						{
							qcLeadTime = qcleadtimeSiteitem;
						}
						today =new java.util.Date();
						System.out.println("today..........."+today);
						rightNow.add(Calendar.DATE,(int)qcLeadTime);
						dueDate = new Timestamp(rightNow.getTimeInMillis())	;

						sql = "select  item_ser,loc_code__aprv,loc_code__rej  "
								+" from siteitem "
								+" where site_code = ? "
								+" and  item_code  = ? " ;
						pstmt1 =  conn.prepareStatement(sql);
						pstmt1.setString(1,SiteCode);
						pstmt1.setString(2,itemCode);
						rs1 = pstmt1.executeQuery();
						if (rs1.next())
						{
							itemSer	 = rs1.getString("item_ser");
							locCodeAprv	 = rs1.getString("loc_code__aprv") == null ? "":rs1.getString("loc_code__aprv").trim();
							locCodeRej	 = rs1.getString("loc_code__rej") == null ? "" :rs1.getString("loc_code__rej").trim();
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
						if(itemSer == null || itemSer.trim().length() ==0)
						{
							sql = "select  item_ser  "
									+" from item "
									+" Where item_code  = ? "	;
							pstmt1 =  conn.prepareStatement(sql);
							pstmt1.setString(1,itemCode);
							rs1 = pstmt1.executeQuery();
							if (rs1.next())
							{
								itemSer	 = rs1.getString("item_ser");
							}
							rs1.close();
							rs1 = null;
							pstmt1.close();
							pstmt1 = null;
						}


						// VALLABH KADAM [06/OCT/2017] START
						//					userId = genericUtility.getValueFromXTRA_PARAMS("loginCode",xtraParams);
						userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
						termId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");
						// VALLABH KADAM [06/OCT/2017] END

						System.out.println("@V@ User id :- ["+userId+"]");

						String xmlValues = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
						xmlValues = xmlValues + "<Header></Header>";
						xmlValues = xmlValues + "<Detail1>";
						xmlValues = xmlValues + "<tran_id></tran_id>";
						xmlValues = xmlValues + "<site_code>"+SiteCode+"</site_code>";
						xmlValues = xmlValues + "<qorder_type>"+ "I" + "</qorder_type>";
						xmlValues = xmlValues + "<qorder_date>"+getCurrdateAppFormat()+"</qorder_date>";
						xmlValues = xmlValues + "<lot_no>"+lotNo+"</lot_no>";
						xmlValues = xmlValues + "<item_ser>"+itemSer+"</item_ser>";
						xmlValues = xmlValues + "</Detail1></Root>";
						qcNo = generateTranId("w_qcorder_new",xmlValues,conn);//function to generate NEW transaction id
						System.out.println("qcNo......"+qcNo);
						if(qcNo == null || qcNo.trim().length() ==0)
						{
							errCode = "VTTRANID";
							System.out.println("errcode......"+errCode);
							errString = itmDBAccessEJB.getErrorString("","VTTRANID","BASE","",conn);
							//Modified by Rohini T on [01/03/2021][Start]
							if(errString != null && errString.trim().length() > 0)
							{
								logMsg = "Selected transaction not exists";
								strToWrite = createPostLog(lineNo,itemCode,locCode,lotNo,lotSl,quantity,logMsg);
								fos1.write(strToWrite.getBytes());
								isError = true;
								return errString;
							}
							//Modified by Rohini T on [01/03/2021][End]
						}
						/*sql = "select loc_code__aprv,loc_code__rej "
						 +" from siteitem "
						 +" where site_code = '"+SiteCode+"' "
						 +" and  item_code  ='"+itemCode+"' " ;
					pstmt1 =  conn.prepareStatement(sql);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						  locCodeAprv	 = rs1.getString("loc_code__aprv") == null ? "":rs1.getString("loc_code__aprv").trim();
						  locCodeRej	 = rs1.getString("loc_code__rej") == null ? "" :rs1.getString("loc_code__rej").trim();
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
						 */
						if(lotNo == null ||lotNo.trim().length() == 0 )
						{
							lotNo = qcNo;
						}
						if("W-RCP".equalsIgnoreCase(tranSer)){
							sql = "update workorder_receipt set lot_no = ? where tran_id = ?";
							pstmt1 = conn.prepareStatement(sql);
							pstmt1.setString(1, lotNo);
							pstmt1.setString(2, tranId);
							pstmt1.executeUpdate();
						}
						if(pstmt1 != null){
							pstmt1.close();
							pstmt1 = null;
						}

						//added by azhar[14/FEB/2017][START]

						sql = "select generate_lot_no from siteitem where site_code = ? and item_code = ?";
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1, SiteCode);
						pstmt1.setString(2, itemCode);
						rs1 = pstmt1.executeQuery();
						if(rs1.next()){
							geneLotNo = checkNullAndTrim(rs1.getString("generate_lot_no"));
						}
						System.out.println("generate lot no::" + geneLotNo);
						if(geneLotNo.length() == 0){

							geneLotNo = "1";
						}

						generateLot = distCommon.getDisparams("999999","GENERATE_LOT_NO_AUTO",conn);
						System.out.println("value from disparm:::" + generateLot);

						if("D-RCP".equalsIgnoreCase(tranSer)){

							if(("Y".equalsIgnoreCase(generateLot) || "M".equalsIgnoreCase(generateLot)) && "1".equalsIgnoreCase(geneLotNo)){
								lotNo = qcNo;
							}
						}

						//added by azhar[14/FEB/2017][END]
						sql = "select emp_code  from users where code = '"+userId+"' "	;
						pstmt1 =  conn.prepareStatement(sql);
						rs1 = pstmt1.executeQuery();
						if (rs1.next())
						{
							empCode	 = rs1.getString("emp_code") == null ? "" :rs1.getString("emp_code").trim();
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;

						System.out.println("@V@ empCode :- ["+empCode+"]");

						sqlInsert ="insert into qc_order (qorder_no,qorder_type,qorder_date,site_code,item_code, " +
								"route_code,quantity,qty_passed,qty_rejected," +
								"start_date,due_date,rel_date,porcp_no,porcp_line_no, " +
								"lot_no,lot_sl,chg_date,chg_user,chg_term,loc_code," +
								"qty_sample,status, unit,qc_create_type, batch_no,expiry_date," +
								"loc_code__aprv,loc_code__rej,unit__sample,lot_no__new,retest_date,emp_code,item_code__new,mfg_date,supp_code__mfg, supp_code ) " +
								"values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, ?)";
						pstmt1 = conn.prepareStatement(sqlInsert);
						pstmt1.setString(1,qcNo);
						pstmt1.setString(2,"I");
						pstmt1.setTimestamp(3,new Timestamp(System.currentTimeMillis()));
						pstmt1.setString(4,SiteCode);
						pstmt1.setString(5,itemCode);
						pstmt1.setString(6," ");
						pstmt1.setDouble(7,quantity);
						pstmt1.setDouble(8,qtySample);
						pstmt1.setDouble(9,0);
						pstmt1.setTimestamp(10,new Timestamp(System.currentTimeMillis()));
						pstmt1.setTimestamp(11,dueDate);
						pstmt1.setTimestamp(12,new Timestamp(System.currentTimeMillis()));
						pstmt1.setString(13,tranId);
						pstmt1.setString(14,lineNo);
						pstmt1.setString(15,lotNo);
						pstmt1.setString(16,lotSl);
						pstmt1.setTimestamp(17,new Timestamp(System.currentTimeMillis()));
						pstmt1.setString(18,userId);

						// VALLABH KADAM [06/OCT/2017] START
						//					pstmt1.setString(19,userId);
						pstmt1.setString(19,termId);
						// VALLABH KADAM [06/OCT/2017] END

						pstmt1.setString(20,locCode);
						pstmt1.setDouble(21,qtySample);
						pstmt1.setString(22,"U");
						pstmt1.setString(23,unit);
						pstmt1.setString(24,"A");
						pstmt1.setString(25,batchNo);
						if(expDate == null){
							pstmt1.setNull(26,Types.TIMESTAMP);
						}else{
							pstmt1.setTimestamp(26,expDate);
						}
						pstmt1.setString(27,locCodeAprv);
						pstmt1.setString(28,locCodeRej);
						pstmt1.setString(29,unit);
						pstmt1.setString(30,lotNo);
						pstmt1.setNull(31,Types.TIMESTAMP);
						pstmt1.setString(32,empCode);
						pstmt1.setString(33,itemCode);

						// VALLABH KADAM [06/OCT/2017] START
						pstmt1.setTimestamp(34, mfgDate);
						pstmt1.setString(35, suppCodeMfg);
						// VALLABH KADAM [06/OCT/2017] END
						//Modified By Umakanta Das on 09-OCT-2017[ To set suppiler code in insert statemrnt]
						pstmt1.setString( 36, suppCode );

						int intCount = pstmt1.executeUpdate();
						System.out.println("Inserting Qc_order :"+intCount);
						pstmt1.clearParameters();
						sql = "update distord_rcpdet set lot_no = ?, batch_no = ?"
								+" where tran_id = ? and loc_code = ? "
								+" and item_code = ? "
								+" and CASE  WHEN  LOT_NO IS NULL  THEN ' ' ELSE LOT_NO END  = ? "
								+" and CASE  WHEN  BATCH_NO IS NULL  THEN ' ' ELSE BATCH_NO END  = ?";
						pstmt1 =  conn.prepareStatement(sql);
						pstmt1.setString(1,lotNo);
						pstmt1.setString(2,batchNo);
						pstmt1.setString(3,tranId);
						pstmt1.setString(4,locCode);
						pstmt1.setString(5,itemCode);
						pstmt1.setString(6,lotNoOld);
						pstmt1.setString(7,batchNoOld);

						upd = pstmt1.executeUpdate();
						pstmt1.close();
						pstmt1 = null;
					}
				} //end while
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

			}
			catch(Exception e)
			{
				errString = "Error";
				System.out.println("Exception ........"+e);
				e.printStackTrace();
				throw new ITMException(e);
			}
			return  errString;
		}
		private String getCurrdateAppFormat() throws ITMException
		{
			String s = "";
			//GenericUtility genericUtility = GenericUtility.getInstance();
			E12GenericUtility genericUtility = new E12GenericUtility();
			try
			{
				java.util.Date date = null;
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				System.out.println(genericUtility.getDBDateFormat());

				SimpleDateFormat simpledateformat = new SimpleDateFormat(genericUtility.getDBDateFormat());
				date = simpledateformat.parse(timestamp.toString());
				timestamp = Timestamp.valueOf(simpledateformat.format(date).toString() + " 00:00:00.0");
				s = (new SimpleDateFormat(genericUtility.getApplDateFormat())).format(timestamp).toString();
			}
			catch(Exception exception)
			{
				System.out.println("Exception in  getCurrdateAppFormat " + exception.getMessage());
				throw new ITMException(exception); //Added By Mukesh Chauhan on 07/08/19
			}
			return s;
		}

		//Changed by wasim on 20-11-2015 for work order allocation for AWMS [START]
		public String allocateStockWorkOrder(String distOrder,String xtraParams,String tranID,Connection conn) throws Exception
		{
			String saleOrder=" ", siteCode=" ", lotNo=" ", locCode="", lotSl="",sqlStock = "",expLev = "";
			double runningSupply=0,qtyReqd=0;
			String tranId="",refId="",refLineNo="",tranIdCrossDock = "",unit="",errString="",workOrder = "",sqlUpdate = "",siteCodeAlloc = "";
			double quantity=0,qtyAllocated=0,totAllocQty=0,allocQty=0, potAdj = 0;
			String sql = "",itemCode = "",remarks = "",chgTerm = "",chgUser = "",previousItem = "",refWorkOrder = "",locCodeTo = "",acctCodeInv = "",
					cctrCodeInv = "",noArt = "",siteCodeShip = "",itemCodeWorder = "", siteCodeRcp = "";
			PreparedStatement pstmt = null,pstmtStock = null,pstmtUpdate = null,pstmtInsert = null,pstmt1 = null;
			ResultSet rs= null,rsStock = null,rs1 = null;
			java.util.Date currentDate = new java.util.Date();
			java.sql.Date date = new java.sql.Date(currentDate.getTime());
			GenericUtility genericUtility = null;
			int lineNo = 0,detCnt = 0;
			String refNo = "", rcpTranId = "",siteCodeLogin = "",status = "";
			InvAllocTraceBean invAllocTrace = null;
			boolean isError = false;
			Timestamp currDate = null;
			ArrayList worderList = new ArrayList();
			HashMap hm = null;
			try
			{
				System.out.println("@@Inside allocateStockWorkOrder");

				currDate = getCurrtDate();

				genericUtility = GenericUtility.getInstance();
				chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode");
				siteCodeLogin = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSite");

				String tranType = "AUT";


				sql = "SELECT DISTINCT H.REF_NO,h.site_code FROM CROSS_DOCK_ALLOC H, cross_dock_alloc_det d WHERE h.tran_id = d.tran_id and D.ORDER_NO = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, distOrder);
				rs= pstmt.executeQuery();
				if(rs.next()){
					refNo = rs.getString("REF_NO");
					siteCode = rs.getString("site_code");
				}
				refNo = refNo == null ? "":refNo.trim();
				siteCode = siteCode == null ? "":siteCode.trim();

				//hm.put("SITE_CODE", siteCode);

				sql = "select work_order from workorder where work_order = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, refNo);
				rs= pstmt.executeQuery();
				if(rs.next())
				{	
					System.out.println("is workorder:::");
					//Changed by wasim on 30-06-2016 to update sql to consider  SITE_CODE, SITE_CODE__SHIP 
					sql = "SELECT DISTINCT H.REF_NO,W.ITEM_CODE FROM CROSS_DOCK_ALLOC H,CROSS_DOCK_ALLOC_DET D,WORKORDER W WHERE"
							+ " H.TRAN_ID = D.TRAN_ID AND H.REF_NO = W.WORK_ORDER AND D.ORDER_NO = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,distOrder);
					rs = pstmt.executeQuery();
					while(rs.next())
					{
						workOrder = checkNullAndTrim(rs.getString("REF_NO"));

						itemCodeWorder = checkNullAndTrim(rs.getString("ITEM_CODE"));
						hm = new HashMap();
						hm.put("SITE_CODE", siteCode);
						hm.put("WORK_ORDER", workOrder);
						hm.put("ITEM_CODE", itemCodeWorder);

						worderList.add(hm);
					}
				}else{
					System.out.println("is campaign no:::");
					sql = "select work_order,item_code from workorder where campgn_no = ?";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, refNo);
					rs1= pstmt1.executeQuery();
					while(rs1.next()){

						workOrder = "";itemCodeWorder = "";
						workOrder = checkNullAndTrim(rs1.getString("work_order"));
						itemCodeWorder = checkNullAndTrim(rs1.getString("ITEM_CODE"));

						System.out.println("work order::::" + workOrder);
						hm = new HashMap();
						hm.put("SITE_CODE", siteCode);
						hm.put("WORK_ORDER", workOrder);
						hm.put("ITEM_CODE", itemCodeWorder);

						System.out.println(":::hash map:::" + hm.toString());
						worderList.add(hm);
						System.out.println(":::: array list"+worderList.toString());
					}

				}

				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				if(rs1 != null){rs1.close();
				rs1 = null;}
				if(pstmt != null){

					pstmt1.close();
					pstmt1 = null;	
				}
				System.out.println("firmplam changes arraylist:::" + worderList.toString());
				Iterator itr = worderList.iterator();
				System.out.println("Array List size="+worderList.size());

				if(worderList.size() > 0)
				{	
					while(itr.hasNext())
					{  
						HashMap detail = new HashMap();
						detail = (HashMap) itr.next();

						workOrder = (String) detail.get("WORK_ORDER");
						siteCode = (String) detail.get("SITE_CODE");
						itemCodeWorder = (String) detail.get("ITEM_CODE");

						sql = "select status from workorder where work_order = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, workOrder);
						rs = pstmt.executeQuery();
						while(rs.next()){
							status = rs.getString("status");
						}
						System.out.println("workorder status::" + status + "for workorder::" +workOrder);

						if("F".equalsIgnoreCase(status) || "R".equalsIgnoreCase(status)){


							sql = " SELECT MAX(TRAN_ID) AS TRAN_ID ,max(site_code) as site_code FROM INV_ALLOCATE WHERE WORK_ORDER = ? "
									+ " AND TRAN_TYPE = 'AUT' AND CONFIRMED = 'Y' ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, workOrder);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								tranId = checkNullAndTrim(rs.getString("TRAN_ID"));
								siteCodeAlloc = checkNullAndTrim(rs.getString("site_code"));
							}
							rs.close();rs = null;
							pstmt.close();pstmt = null;

							System.out.println("Max Tran ID ["+tranId+"]");
							System.out.println("site_code ["+siteCodeAlloc+"]");

							if(tranId.length() == 0)//Insert into header if tran id not found
							{
								String xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
								xmlValues = xmlValues + "<Header></Header>";
								xmlValues = xmlValues + "<Detail1>";
								xmlValues = xmlValues +	"<tran_id></tran_id>";
								xmlValues = xmlValues + "<site_code>"+siteCode.trim()+"</site_code>";
								xmlValues = xmlValues +  "<tran_date>"+getCurrdateAppFormat()+"</tran_date>";
								xmlValues = xmlValues +  "<tran_type></tran_type>";
								xmlValues = xmlValues + "</Detail1></Root>";

								tranId = generateTranId( "w_inv_allocate",xmlValues, conn );

								sql = " INSERT INTO INV_ALLOCATE ( TRAN_ID,TRAN_DATE,TRAN_TYPE,WORK_ORDER,SITE_CODE,ITEM_CODE," +
										" CHG_DATE,CHG_USER,CHG_TERM,REMARKS,CONFIRMED,CONF_DATE,STATUS,STATUS_DATE )" +
										" VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
								pstmtInsert = conn.prepareStatement(sql);
								pstmtInsert.setString(1,tranId);
								pstmtInsert.setTimestamp(2, currDate);  
								pstmtInsert.setString(3, tranType);                                                                                                                                                                                                     
								pstmtInsert.setString(4, workOrder); 
								pstmtInsert.setString(5, siteCode);                                                                                                                                                                          
								pstmtInsert.setString(6, itemCodeWorder);                                                                                                                                                                           
								pstmtInsert.setTimestamp(7, currDate);                                                                                                                                                                      
								pstmtInsert.setString(8, chgUser);                                                                                                                                                                       
								pstmtInsert.setString(9, chgTerm );                                                                                                                                                    
								pstmtInsert.setString(10, remarks);                                                                                                                                                                
								pstmtInsert.setString(11, "Y");                                                                                                                                                                         
								pstmtInsert.setTimestamp(12, currDate);                                                                                                                                                                 
								pstmtInsert.setString(13, "O");                                                                                                                                                         
								pstmtInsert.setTimestamp(14, currDate); //it should be timestamp

								int hdrCnt = pstmtInsert.executeUpdate();
								pstmtInsert.close();pstmtInsert = null;
							}	
							else
							{
								sql = " SELECT MAX(LINE_NO) AS LINE_NO FROM INV_ALLOCATE H,INV_ALLOC_DET D "
										+"	WHERE H.TRAN_ID = D.TRAN_ID AND H.TRAN_ID = ? AND H.TRAN_TYPE = 'AUT' AND H.CONFIRMED = 'Y' ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, tranId);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									lineNo = rs.getInt("LINE_NO");
									//siteCode = rs.getString("site_code");
								}
								rs.close();rs = null;
								pstmt.close();pstmt = null;

								int upd = 0;
								sql = "update inv_allocate set site_code = ? where tran_id = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, siteCode);
								pstmt.setString(2, tranId);
								upd = pstmt.executeUpdate();
								if(upd > 0){
									System.out.println("site code updated successfully::" + upd);
								}
							}



							System.out.println("Max Line No ["+lineNo+"]");




							/*sql = "select d.tran_id from distord_rcp d , distord_rcpdet dt where d.tran_id = dt.tran_id and d.dist_order = ? and ";
						pstmt= conn.prepareStatement(sql);
						pstmt.setString(1, distOrder);
						rs = pstmt.executeQuery();
						while(rs.next()){
							rcpTranId = rs.getString("tran_id");

						rcpTranId = rcpTranId == null ? "" : rcpTranId.trim();
						System.out.println("rcp tran id:::" + rcpTranId);*/

							//rcp site code = cross doc det site code
							//sqlStock = " SELECT H.REF_NO,D.EXP_LEV,D.ITEM_CODE,RCP.SITE_CODE,RCPDET.LOC_CODE,D.LOT_NO,D.LOT_SL,D.QUANTITY,D.REQ_QTY"
							sqlStock = " SELECT H.REF_NO,D.EXP_LEV,D.ITEM_CODE,RCPDET.LOC_CODE, D.LOT_NO, D.LOT_SL," +
									//"RCPDET.LOT_NO," + //changed by azhar
									//" RCPDET.LOT_SL" +//changed by azhar
									"D.QUANTITY,D.REQ_QTY, D.POTENCY_ADJ,D.SITE_CODE"
									+" FROM CROSS_DOCK_ALLOC H, CROSS_DOCK_ALLOC_DET D, DISTORD_RCP RCP,DISTORD_RCPDET RCPDET"
									+" WHERE H.TRAN_ID = D.TRAN_ID"
									+" AND RCP.TRAN_ID = RCPDET.TRAN_ID"
									+" AND RCP.SITE_CODE = H.SITE_CODE"
									+" AND RCPDET.LINE_NO_DIST_ORDER = D.LINE_NO__ORD " //changed by azhar
									//+" AND RCPDET.LINE_NO = D.LINE_NO__ORD "
									+" AND D.ORDER_NO = RCPDET.DIST_ORDER"
									+" AND H.REF_NO = ? and rcp.tran_id = ? and d.order_ref = ? " +
									" and d.item_code=RCPDET.item_code and d.lot_no=RCPDET.lot_no " +
									" and d.lot_sl=RCPDET.lot_sl ";
							pstmtStock = conn.prepareStatement(sqlStock);
							pstmtStock.setString(1,refNo);
							pstmtStock.setString(2,tranID);
							pstmtStock.setString(3, workOrder);
							rsStock = pstmtStock.executeQuery();
							while(rsStock.next())
							{
								expLev = rsStock.getString("EXP_LEV");
								itemCode = rsStock.getString("ITEM_CODE");
								siteCodeRcp = rsStock.getString("SITE_CODE");//rcp site code
								locCode = rsStock.getString("LOC_CODE");//rcp loc code
								lotNo = rsStock.getString("LOT_NO");
								lotSl = rsStock.getString("LOT_SL");
								qtyReqd = rsStock.getDouble("REQ_QTY");
								potAdj = rsStock.getDouble("POTENCY_ADJ");
								//quantity = rsStock.getDouble("QUANTITY");

								sql = "select quantity from stock where item_code = ? and site_code = ? and loc_code = ? and lot_no = ? and lot_sl = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, itemCode);
								pstmt.setString(2, siteCode);
								pstmt.setString(3, locCode);
								pstmt.setString(4, lotNo);
								pstmt.setString(5, lotSl);
								rs = pstmt.executeQuery();
								while(rs.next()){
									quantity = rs.getDouble("quantity");
								}
								System.out.println("quantity to be set:::" + quantity);
								sql = " INSERT INTO INV_ALLOC_DET ( TRAN_ID,LINE_NO,SITE_CODE,ITEM_CODE,LOT_NO,LOT_SL,QUANTITY,REMARKS,REAS_CODE," +//9
										" LOC_CODE,EXP_LEV,POTENCY_ADJ,ALLOC_QTY )" +//16
										" VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?) ";
								pstmtInsert = conn.prepareStatement(sql);

								lineNo++;
								pstmtInsert.setString(1,tranId);
								pstmtInsert.setInt(2,lineNo);                                                                                                                                                                                
								//pstmtInsert.setString(3, siteCodeAlloc);
								//pstmtInsert.setString(3, siteCodeRcp);
								pstmtInsert.setString(3, siteCode);
								pstmtInsert.setString(4, itemCode );                                                                                                                                                                              
								pstmtInsert.setString(5, lotNo );                                                                                                                                                                       
								pstmtInsert.setString(6, lotSl );                                                                                                                                                                        
								pstmtInsert.setDouble(7, quantity);   //need to ask with sir                                                                                                                                                                           
								pstmtInsert.setString(8, remarks );                                                                                                                                                                            
								pstmtInsert.setString(9, "" );                                                                                                                                                                            
								pstmtInsert.setString(10, locCode);                                                                                                                                                                    
								pstmtInsert.setString(11, expLev);                                                                                                                                                                           
								pstmtInsert.setDouble(12, potAdj);                                                                                                                                                                     
								//pstmtInsert.setDouble(13, 0 );                                                                                                                                                                       
								//pstmtInsert.setDouble(14, 0 );                                                                                                                                                                    
								pstmtInsert.setDouble(13, qtyReqd );                                                                                                                                                                
								//pstmtInsert.setString(16, "N" ); 

								detCnt = pstmtInsert.executeUpdate();
								pstmtInsert.close();
								pstmtInsert = null;	

								System.out.println("Data inserted in inv_alloc_det="+detCnt); 

								String refSer = "W-RIN" ; 
								HashMap hashMap = null;
								hashMap = new HashMap();
								hashMap.put("tran_date",date);
								hashMap.put("ref_ser",refSer);
								hashMap.put("ref_id",tranId); 
								hashMap.put("ref_line",""+lineNo);					
								hashMap.put("site_code",siteCode);
								hashMap.put("item_code",itemCode);
								hashMap.put("loc_code",locCode);
								hashMap.put("lot_no",lotNo);
								hashMap.put("lot_sl",lotSl);
								hashMap.put("alloc_qty",qtyReqd);
								hashMap.put("chg_user",chgUser);
								hashMap.put("chg_term",chgTerm);
								hashMap.put("chg_date", date );
								invAllocTrace = new InvAllocTraceBean();

								errString = invAllocTrace.updateInvallocTrace(hashMap, conn);
								System.out.println("updateInvallocTrace  1....."+errString);
								if ( errString != null && errString.trim().length() > 0 )
								{
									strToWrite = createPostLog(Integer.toString(lineNo),itemCode,locCode,lotNo,lotSl,quantity,errString);//Modified by Rohini T on[28/02/2021]
									fos1.write(strToWrite.getBytes());
									isError = true;
									System.out.println("Rollbacking connection:");
									conn.rollback();
									return errString;
								}
								else
								{
									System.out.println("Inventory allocated successfully");
								}
							}
						}
					}
				}//End while iterator	
				if(detCnt == 0){
					errString = "Insert failed::"; 
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new Exception (e);
			}
			finally
			{
				try
				{
					if(pstmt != null){pstmt.close();pstmt=null;}
					if(pstmtInsert != null){pstmtInsert.close();pstmtInsert=null;}
					if(pstmtStock != null){pstmtStock.close();pstmtStock=null;}
					if(rs != null){rs.close();rs=null;}
					if(rsStock != null){rsStock.close();rsStock=null;}
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
			return errString;
		}
		private String checkNullAndTrim( String inputVal )
		{
			if ( inputVal == null )
			{
				inputVal = "";
			}
			else
			{
				inputVal = inputVal.trim();
			}
			return inputVal;
		}
		private java.sql.Timestamp getCurrtDate() throws RemoteException,ITMException
		{
			String currAppdate = "";
			java.sql.Timestamp currDate = null;
			GenericUtility genericUtility = GenericUtility.getInstance();
			try
			{
				Object date = null;
				currDate = new java.sql.Timestamp(System.currentTimeMillis());
				java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(genericUtility.getDBDateFormat());
				date = sdf.parse(currDate.toString());
				currDate = java.sql.Timestamp.valueOf(sdf.format(date).toString()+ " 00:00:00.0");

			}
			catch (Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
			return (currDate);
		}
		//Changed by wasim on 20-11-2015 for work order allocation for AWMS [END]


		private void closeResources(PreparedStatement pstmt, ResultSet rs ){
			try {
				if(rs != null){
					rs.close();
					rs = null;
				}
				if(pstmt != null){
					pstmt.close();
					pstmt = null;
				}
			} catch (Exception e) {
				System.out.println("Exception::" + e.getMessage());
			}	
		}

		//Added by varsha v to get error type
		private String errorType(Connection conn, String errorCode) throws ITMException
		{
			String msgType = "";
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try
			{
				String sql = "SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, errorCode);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					msgType = rs.getString("MSG_TYPE");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			} 
			catch (Exception ex)
			{
				ex.printStackTrace();
				throw new ITMException(ex);
			} 
			finally
			{
				try
				{
					if (rs != null)
					{
						rs.close();
						rs = null;
					}
					if (pstmt != null)
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
			return msgType;
		}
		private String checkNull( String inputVal )
		{
			if ( inputVal == null )
			{
				inputVal = "";
			}
			else
			{
				inputVal = inputVal;
			}
			return inputVal;
		}
		//Ended by varsha v to get error type

		// Added by Anagha Rane (To generate misc. voucher) 02-04-2020 Serdia
		// Customization
		//private String generateMiscVoucher(String tranID, String otherSite, String xtraParams, Connection conn)//commented by monika salla on 31 dec 20
		//throws ITMException {
		private String generateMiscVoucher(String tranID,String recoverTranType, String otherSite, String xtraParams, Connection conn)//added by monika salla for recovertrantype on 31 dec 20
				throws ITMException {
			String currCode = "", confirmed = "", siteCode = "", tranType = "", sreturnNo = "", tranIdNew = "";
			String itemCode = "", taxChap = "", taxClass = "", taxEnv = "";
			String transMode = "", chgUser = "", chgTerm = "", errString = "", sql = "", sql1 = "";
			String distOrder = "", tranIdIss = "", siteCodeShip = "", confPasswd = "", orderType = "", qcReqd = "",
					issueRef = "";
			String locCodeGit = "", frtType = "", chgUsr = "", empCodeAprv = "", lotNo = "", cctrCode = "",
					cctrCodeDet = "", tranSer = "", tranWin = "";
			String analysis1 = "", analysis2 = "", analysis3 = "", unit = "", lotSl = "", acctCode = "";
			String lineNoDistOrder = "", locCode = "", packCode = "", packInstr = "", dimension = "", grade = "";
			String suppCode = "", acctCodeAp = "", cctrCodeAp = "", acctCodeApAdv = "", cctrCodeApAdv = "", stanCode = "",
					suppType = "", payMode = "", crTerm = "", suppName = "", lrNo = "";
			Timestamp chgDate = null, tranDate = null, effDate = null, confDate = null, gpDate = null, retestDate = null,
					lrDate = null, currDate = null;
			double amount = 0, taxAmt = 0, netAmt = 0, rateClg = 0, exchRate = 0, rate = 0, calAmount = 0;
			double discount = 0, costRate = 0;
			int count = 0;
			String  lineNo = ""; 
			int grossWeight = 0, tareWeight = 0, netWeight = 0, volume = 0, frtAmt = 0, noArt = 0, quantity = 0;
			int lineNoSret = 0, lineNoInvtrace = 0, lineNoRcpinv = 0, i = 0, actualQty = 0, palletWt = 0;
			String startFrom = "", billDate = "", finEntity = "";
			int crDays = 0;
			Timestamp dueDate = null;
			double totAmtDet = 0.0, totBillAmtDet = 0.0, totNetAmtDet = 0.0;

			PreparedStatement pstmt = null;
			PreparedStatement pstmt1 = null;
			PreparedStatement pstmt2 = null;
			ResultSet rs = null;
			ResultSet rs1 = null;
			ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
			boolean isError = false;
			FinCommon fcom = new FinCommon();
			try {
				currDate = new java.sql.Timestamp(System.currentTimeMillis());
				SimpleDateFormat sdfAppl = new SimpleDateFormat(genericUtility.getApplDateFormat());
				String currDateStr = sdfAppl.format(currDate);

				chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
				chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");
				chgDate = new java.sql.Timestamp(new java.util.Date().getTime());

				sql = "select tran_date,eff_date,dist_order,tran_id__iss,site_code,site_code__ship,gross_weight,"
						+ "tare_weight,net_weight,volume,frt_amt,amount,tax_amt,net_amt,loc_code__git,frt_type,"
						+ "chg_user,chg_term,curr_code,chg_date,confirmed,conf_date,no_art,trans_mode,conf_passwd,"
						+ "order_type,tran_type,exch_rate,emp_code__aprv,qc_reqd,gp_date,issue_ref,lr_no,lr_date "
						+ "from distord_rcp where tran_id = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranID);

				rs = pstmt.executeQuery();
				while (rs.next()) {
					tranDate = rs.getTimestamp("tran_date");
					effDate = rs.getTimestamp("eff_date");
					distOrder = rs.getString("dist_order");
					tranIdIss = rs.getString("tran_id__iss");
					siteCode = rs.getString("site_code");
					siteCodeShip = rs.getString("site_code__ship");
					grossWeight = rs.getInt("gross_weight");
					tareWeight = rs.getInt("tare_weight");
					netWeight = rs.getInt("net_weight");
					volume = rs.getInt("volume");
					frtAmt = rs.getInt("frt_amt");
					amount = rs.getDouble("amount");
					System.out.println("Amount selected from distord_rcp: " + amount);
					taxAmt = rs.getInt("tax_amt");
					netAmt = rs.getInt("net_amt");
					locCodeGit = rs.getString("loc_code__git");
					frtType = rs.getString("frt_type");
					chgUsr = rs.getString("chg_user");
					chgTerm = rs.getString("chg_term");
					currCode = rs.getString("curr_code");
					chgDate = rs.getTimestamp("chg_date");
					confirmed = rs.getString("confirmed");
					confDate = rs.getTimestamp("conf_date");
					noArt = rs.getInt("no_art");
					transMode = rs.getString("trans_mode");
					confPasswd = rs.getString("conf_passwd");
					orderType = rs.getString("order_type");
					tranType = rs.getString("tran_type");
					exchRate = rs.getDouble("exch_rate");
					empCodeAprv = rs.getString("emp_code__aprv");
					qcReqd = rs.getString("qc_reqd");
					gpDate = rs.getTimestamp("gp_date");
					issueRef = rs.getString("issue_ref");
					lrNo = rs.getString("lr_no");
					lrDate = rs.getTimestamp("lr_date");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				String xmlValues = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
				xmlValues = xmlValues + "<Header></Header>";
				xmlValues = xmlValues + "<Detail1>";
				xmlValues = xmlValues + "<tran_id>" + "</tran_id>";
				xmlValues = xmlValues + "<site_code>" + siteCode.trim() + "</site_code>";
				xmlValues = xmlValues + "<tran_date>" + getCurrdateAppFormat() + "</tran_date>";
				//Commented by monika salla on 31 dec 20
				//xmlValues = xmlValues + "<tran_type>" + tranType + "</tran_type>";
				//added by monika salla on 31 dec 2020--to set tran type from distoreder type
				xmlValues = xmlValues + "<tran_type>" + recoverTranType + "</tran_type>";
				//end

				xmlValues = xmlValues + "<vouch_type> E </vouch_type>";
				xmlValues = xmlValues + "</Detail1></Root>";

				//Commented by Anagha R on 2/9/2020 for Serdia: GST Recovery change START
				//tranIdNew = generateTranId("w_misc_voucher", xmlValues, conn);
				//System.out.println("tranIdNew: " + tranIdNew);
				//Commented by Anagha R on 2/9/2020 for Serdia: GST Recovery change END

				System.out.println("tranType:--["+tranType+"] recoverTranType---["+recoverTranType+" ] site code---["+siteCode+" ]site code ship ["+siteCodeShip);

				
				sql = "select count(*) from site_supplier where site_code__ch = ? and site_code = ? and channel_partner = 'Y' ";
				pstmt = conn.prepareStatement(sql);
				//pstmt.setString(1, siteCode);
				//pstmt.setString(2, siteCodeShip);//changed for removing excecuting  channel partener condition
				pstmt.setString(1, siteCodeShip);
				pstmt.setString(2, siteCode);//changed for removing excecuting  channel partener condition ON 8 TH APRIL 2021
				rs = pstmt.executeQuery();
				if (rs.next()) {
					count = rs.getInt(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				if (count == 0) {
					sql = "select count(*) from supplier where site_code = ? and case when channel_partner is null then 'N' else channel_partner end = 'Y'";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, siteCode);
					//pstmt.setString(1, siteCodeShip);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						count = rs.getInt(1);
					}

					if (count > 1) {
						errString = itmDBAccessLocal.getErrorString("", "ERRORVTCPC", "", "", conn);// Error code need to
						// change
						//Modified by Rohini T on [01/03/2021][Start]
						if(errString != null && errString.trim().length() > 0)
						{
							logMsg = "More Than One Channel Partner ";
							strToWrite = createPostLog(lineNo,itemCode,locCode,lotNo,lotSl,quantity,logMsg);
							fos1.write(strToWrite.getBytes());
							isError = true;
							return errString;
						}
						//Modified by Rohini T on [01/03/2021][End]
					} else if (count == 0) {
						errString = itmDBAccessLocal.getErrorString("", "VTCUSTCD4", "", "", conn);// Error code need to
						// change
						//Modified by Rohini T on [01/03/2021][Start]
						if(errString != null && errString.trim().length() > 0)
						{
							logMsg = "Customer is not a channel partner";
							strToWrite = createPostLog(lineNo,itemCode,locCode,lotNo,lotSl,quantity,logMsg);
							fos1.write(strToWrite.getBytes());
							isError = true;
							return errString;
						}
						//Modified by Rohini T on [01/03/2021][End]
					} else if (count == 1) {
						sql1 = "select supp_code from supplier where site_code = ? and channel_partner = 'Y'";
						pstmt1 = conn.prepareStatement(sql1);
						pstmt1.setString(1, siteCodeShip);
						rs = pstmt1.executeQuery();
						if (rs.next()) {
							suppCode = rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt1.close();
						pstmt1 = null;
					}
				}

				else if (count == 1) {
					sql = "select supp_code from site_supplier where site_code__ch = ? and site_code = ?  and channel_partner = 'Y'";
					pstmt = conn.prepareStatement(sql);
					//pstmt.setString(1, siteCode);
					//pstmt.setString(2, siteCodeShip);
					pstmt.setString(1, siteCodeShip);
					pstmt.setString(2, siteCode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						suppCode = rs.getString(1);
					}
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}

				sql = "select acct_code__ap,cctr_code__ap,acct_code__ap_adv,cctr_code__ap_adv,stan_code,supp_type, pay_mode, cr_term, supp_name from supplier where supp_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, suppCode);

				rs = pstmt.executeQuery();
				while (rs.next()) {
					acctCodeAp = rs.getString("acct_code__ap");
					cctrCodeAp = rs.getString("cctr_code__ap");
					acctCodeApAdv = rs.getString("acct_code__ap_adv");
					cctrCodeApAdv = rs.getString("cctr_code__ap_adv");
					stanCode = rs.getString("stan_code");
					suppType = rs.getString("supp_type");
					payMode = rs.getString("pay_mode");
					crTerm = rs.getString("cr_term");
					suppName = rs.getString("supp_name");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				// misc_voucher
				//Added by Anagha R on 09/09/2020 to insert details multiple times and get updated amount for Serdia: GST Recovery change START

				billDate = currDateStr;
				crDays = Integer.parseInt(findValue(conn, "cr_days", "crterm", "cr_term", crTerm));
				startFrom = findValue(conn, "start_from", "crterm", "cr_term", crTerm);
				dueDate = this.getDueDate(conn, sdfAppl, startFrom, sdfAppl.format(tranDate), sdfAppl.format(effDate),
						billDate, crDays);

				// finentity
				sql = "select fin_entity from site where site_code= ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, siteCode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					finEntity = rs.getString("fin_entity");
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;

				sql = "insert into misc_voucher(tran_id,tran_date,tran_type,eff_date,curr_code,exch_rate,"
						+ "site_code,tax_amt,confirmed,conf_date,chg_date,chg_user,chg_term,remarks,mvouch_gen_tran_id,net_amt,tran_mode,gp_date,"
						+ "sundry_code,bill_no,bill_date,acct_code__ap,cctr_code__ap,stan_code,"
						+ "tax_class,tax_chap,tax_env,pay_mode,cr_term,vouch_type,auto_pay,adv_amt,due_date,"
						+ "fin_entity,bill_amt,tot_amt,sundry_type,sundry_type__pay,sundry_code__pay,acct_code__pay,net_amt__bc)"
						+ "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";

				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranID);
				pstmt.setTimestamp(2, tranDate);
				//commented by monika salla on 31 dec 20  to add recover tran type from distorder_type master
				//pstmt.setString(3, tranType);
				pstmt.setString(3, recoverTranType);
				//end 
				pstmt.setTimestamp(4, effDate);
				pstmt.setString(5, currCode);
				pstmt.setDouble(6, exchRate);
				pstmt.setString(7, siteCode);
				pstmt.setDouble(8, 0.0);
				pstmt.setString(9, "N");
				pstmt.setTimestamp(10, null);
				pstmt.setTimestamp(11, chgDate);
				pstmt.setString(12, chgUser);
				pstmt.setString(13, chgTerm);
				pstmt.setString(14, "GST payable CSA site against D-RCP -:" + tranID);
				pstmt.setString(15, tranID);
				pstmt.setDouble(16, calAmount);
				pstmt.setString(17, transMode);
				pstmt.setTimestamp(18, gpDate);
				pstmt.setString(19, suppCode);
				pstmt.setString(20, lrNo);
				pstmt.setTimestamp(21, lrDate);
				pstmt.setString(22, acctCodeAp);
				pstmt.setString(23, cctrCodeAp);
				pstmt.setString(24, stanCode);
				pstmt.setString(25, taxClass);
				pstmt.setString(26, taxChap);
				pstmt.setString(27, taxEnv);
				pstmt.setString(28, payMode);
				pstmt.setString(29, crTerm);
				pstmt.setString(30, "E");
				pstmt.setString(31, "Y");
				pstmt.setDouble(32, 0.0);
				pstmt.setTimestamp(33, confDate);
				pstmt.setString(34, finEntity);
				pstmt.setDouble(35, calAmount);
				pstmt.setDouble(36, calAmount);
				pstmt.setString(37, "S");
				pstmt.setString(38, "S");
				pstmt.setString(39, suppCode);
				pstmt.setString(40, acctCodeAp);
				pstmt.setDouble(41, calAmount * exchRate);

				int updateCountHeader = pstmt.executeUpdate();
				System.out.println("no of header row update = " + updateCountHeader);

				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				//Added by Anagha R on 09/09/2020 to insert details multiple times and get updated amount for Serdia: GST Recovery change START

				//Changed by Anagha R on 1-Jun-2020 for Serdia Customization Release11052020
				//sql = "select sum(tr.tax_amt) from taxtran tr, tax t where t.tax_code = tr.tax_code and t.tax_type in ('G','H','I') and tr.tran_code = 'D-RCP' and tr.tran_id= ?";
				sql = "select TX.ACCT_CODE__RECO,TX.CCTR_CODE__RECO,SUM(TX.TAX_AMT) from taxtran TX, tax T where TX.TAX_CODE=T.TAX_CODE"+
						" AND T.TAX_TYPE IN ('I','G','H','J')AND TX.TRAN_ID=? AND TX.TAX_AMT <>0 AND tran_code='D-RCP'"+
						" GROUP BY TX.ACCT_CODE__RECO,TX.CCTR_CODE__RECO";
				//Changed by Anagha R on 1-Jun-2020 for Serdia Customization Release11052020

				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranID);
				rs = pstmt.executeQuery();
				//if (rs.next()) { //Commented by Anagha R on 09/Sep/2020 for Serdia: GST Recovery change
				int rowCnt = 0;
				while (rs.next()) { //Added by Anagha R on 09/Sep/2020 for Serdia: GST Recovery change    
					//calAmount = rs.getInt(1);
					acctCode = rs.getString(1);//Changed by Anagha R on 1-Jun-2020 for Serdia Customization Release11052020
					cctrCode = rs.getString(2);//Changed by Anagha R on 1-Jun-2020 for Serdia Customization Release11052020
					//commented by monika to get double value on 9 june 2020
					//calAmount = rs.getInt(3);//Changed by Anagha R on 1-Jun-2020 for Serdia Customization Release11052020
					calAmount = rs.getDouble(3);//end					
					System.out.println("AcctCode: "+acctCode);
					System.out.println("CCTRCode: "+cctrCode);
					System.out.println("Amount calculated: " + calAmount);

					//Added by Anagha R on 09/09/2020 to insert details multiple times and get updated amount for Serdia: GST Recovery change START
					rowCnt++; 
					if ("NULLFOUND".equalsIgnoreCase(cctrCode) || cctrCode == null || cctrCode == ""
							|| cctrCode.length() == 0) {
						cctrCode = cctrCodeAp;
					}

					sql = "insert into misc_vouchdet (tran_id,line_no,acct_code,cctr_code,item_code,amount,tax_amt,tax_class, "
							+ "	tax_chap,tax_env,rate_clg,net_amount,bill_amt) Values (?,?,?,?,?,?,?,?,?,?,?,?,?) ";

					pstmt2 = conn.prepareStatement(sql);
					pstmt2.setString(1, tranID);
					pstmt2.setInt(2, rowCnt);
					pstmt2.setString(3, acctCode);
					pstmt2.setString(4, cctrCode);
					pstmt2.setString(5, itemCode);
					pstmt2.setDouble(6, calAmount);
					pstmt2.setDouble(7, 0.0);
					pstmt2.setString(8, taxClass);
					pstmt2.setString(9, taxChap);
					pstmt2.setString(10, taxEnv);
					pstmt2.setDouble(11, rateClg);
					pstmt2.setDouble(12, calAmount * exchRate);
					pstmt2.setDouble(13, calAmount);

					int updateCount = pstmt2.executeUpdate();
					System.out.println("no of row update = " + updateCount);

					pstmt2.close();
					pstmt2= null;

					totAmtDet=totAmtDet+Math.abs(calAmount);
					totNetAmtDet=totNetAmtDet+Math.abs(calAmount);
					totBillAmtDet=totBillAmtDet+Math.abs(calAmount);
					//Added by Anagha R on 09/09/2020 to insert details multiple times and get updated amount for Serdia: GST Recovery change END

				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				//Commented by Anagha R on 08/Sep/2020 for Serdia: GST Recovery change START
				/*
				billDate = currDateStr;
				crDays = Integer.parseInt(findValue(conn, "cr_days", "crterm", "cr_term", crTerm));
				startFrom = findValue(conn, "start_from", "crterm", "cr_term", crTerm);
				dueDate = this.getDueDate(conn, sdfAppl, startFrom, sdfAppl.format(tranDate), sdfAppl.format(effDate),
						billDate, crDays);

				// finentity
				sql = "select fin_entity from site where site_code= ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, siteCode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					finEntity = rs.getString("fin_entity");
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;

				sql = "insert into misc_voucher(tran_id,tran_date,tran_type,eff_date,curr_code,exch_rate,"
						+ "site_code,tax_amt,confirmed,conf_date,chg_date,chg_user,chg_term,remarks,mvouch_gen_tran_id,net_amt,tran_mode,gp_date,"
						+ "sundry_code,bill_no,bill_date,acct_code__ap,cctr_code__ap,stan_code,"
						+ "tax_class,tax_chap,tax_env,pay_mode,cr_term,vouch_type,auto_pay,adv_amt,due_date,"
						+ "fin_entity,bill_amt,tot_amt,sundry_type,sundry_type__pay,sundry_code__pay,acct_code__pay,net_amt__bc)"
						+ "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";

				pstmt = conn.prepareStatement(sql);
                //pstmt.setString(1, tranIdNew); //Commented by Anagha R on 2/9/2020 for Serdia: GST Recovery change
                pstmt.setString(1, tranID); // Added by Anagha R on 2/9/2020 for Serdia: GST Recovery change
				pstmt.setTimestamp(2, tranDate);
				pstmt.setString(3, tranType);
				pstmt.setTimestamp(4, effDate);
				pstmt.setString(5, currCode);
				pstmt.setDouble(6, exchRate);
				pstmt.setString(7, siteCode);
				pstmt.setDouble(8, 0.0);
				pstmt.setString(9, "N");
				pstmt.setTimestamp(10, null);
				pstmt.setTimestamp(11, chgDate);
				pstmt.setString(12, chgUser);
				pstmt.setString(13, chgTerm);
				pstmt.setString(14, "GST payable against D-RCP " + tranID);
				pstmt.setString(15, tranID);
				pstmt.setDouble(16, calAmount);
				pstmt.setString(17, transMode);
				pstmt.setTimestamp(18, gpDate);
				pstmt.setString(19, suppCode);
				pstmt.setString(20, lrNo);
				pstmt.setTimestamp(21, lrDate);
				pstmt.setString(22, acctCodeAp);
				pstmt.setString(23, cctrCodeAp);
				pstmt.setString(24, stanCode);
				pstmt.setString(25, taxClass);
				pstmt.setString(26, taxChap);
				pstmt.setString(27, taxEnv);
				pstmt.setString(28, payMode);
				pstmt.setString(29, crTerm);
				pstmt.setString(30, "E");
				pstmt.setString(31, "Y");
				pstmt.setDouble(32, 0.0);
				pstmt.setTimestamp(33, confDate);
				pstmt.setString(34, finEntity);
				pstmt.setDouble(35, calAmount);
				pstmt.setDouble(36, calAmount);
				pstmt.setString(37, "S");
				pstmt.setString(38, "S");
				pstmt.setString(39, suppCode);
				pstmt.setString(40, acctCodeAp);
				pstmt.setDouble(41, calAmount * exchRate);

				int updateCountHeader = pstmt.executeUpdate();
				System.out.println("no of header row update = " + updateCountHeader);

				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
                }*/
				//Commented by Anagha R on 08/Sep/2020 for Serdia: GST Recovery change END                

				//Commented  by Anagha R on 1-Jun-2020 for Serdia Customization Release11052020

				//acctCode = fcom.getFinparams("999999", "GST_RECO_ACCT_DET", conn);
				//System.out.println("acctCode: " + acctCode);

				//cctrCode = fcom.getFinparams("999999", "GST_RECO_CCTR_DET", conn);
				//System.out.println("cctrCode: " + cctrCode);

				//Commented by Anagha R on 1-Jun-2020 for Serdia Customization Release11052020

				//Commented by Anagha R on 09/09/2020 for Serdia: GST Recovery change START            
				/*if ("NULLFOUND".equalsIgnoreCase(cctrCode) || cctrCode == null || cctrCode == ""
						|| cctrCode.length() == 0) {
					cctrCode = cctrCodeAp;
				}

				sql = "insert into misc_vouchdet (tran_id,line_no,acct_code,cctr_code,item_code,amount,tax_amt,tax_class, "
						+ "	tax_chap,tax_env,rate_clg,net_amount,bill_amt) Values (?,?,?,?,?,?,?,?,?,?,?,?,?) ";

				pstmt = conn.prepareStatement(sql);
                //pstmt.setString(1, tranIdNew); //Commented by Anagha R on 2/9/2020 for Serdia: GST Recovery change
                pstmt.setString(1, tranID); //Added by Anagha R on 2/9/2020 for Serdia: GST Recovery change
				pstmt.setInt(2, lineNo);
				pstmt.setString(3, acctCode);
				pstmt.setString(4, cctrCode);
				pstmt.setString(5, itemCode);
				pstmt.setDouble(6, calAmount);
				pstmt.setDouble(7, 0.0);
				pstmt.setString(8, taxClass);
				pstmt.setString(9, taxChap);
				pstmt.setString(10, taxEnv);
				pstmt.setDouble(11, rateClg);
				pstmt.setDouble(12, calAmount * exchRate);
				pstmt.setDouble(13, calAmount);

				int updateCount = pstmt.executeUpdate();
				System.out.println("no of row update = " + updateCount);

				pstmt.close();
				pstmt = null;
				 */
				//Commented by Anagha R on 09/09/2020 for Serdia: GST Recovery change END                                
				//Added by Anagha R on 09/09/2020 to update totAmt in miscVoucher for Serdia: GST Recovery change START
				sql = "update misc_voucher set net_amt = ?, bill_amt = ?, tot_amt = ? where tran_id = ?";
				pstmt = conn.prepareStatement(sql);

				pstmt.setDouble(1, totAmtDet);
				pstmt.setDouble(2, totBillAmtDet);
				pstmt.setDouble(3, totNetAmtDet);
				pstmt.setString(4, tranID);
				int updCnt = pstmt.executeUpdate();

				if(pstmt!=null)
				{
					pstmt.close();
					pstmt = null;
				}
				System.out.println("update amount Successfully:"+updCnt);
				//Added by Anagha R on 09/09/2020 to update totAmt in miscVoucher for Serdia: GST Recovery change END

				//if (updateCount > 0) {
				MiscValConf MiscValConfObj=new MiscValConf(); //VTSUCC1
				//errString=MiscValConfObj.confirm(tranIdNew, xtraParams, "",conn); //Commented by Anagha R on 2/9/2020 for Serdia: GST Recovery change
				errString=MiscValConfObj.confirm(tranID, xtraParams, "",conn); //Added by Anagha R on 2/9/2020 for Serdia: GST Recovery change
				//Modified by Rohini T on [28/02/2021][Start]
				if(errString != null && errString.trim().length() > 0)
				{
					strToWrite = createPostLog(lineNo,itemCode,locCode,lotNo,lotSl,quantity,errString);
					fos1.write(strToWrite.getBytes());
					isError = true;
					return errString;
				}

				if(errString != null && (errString.indexOf("CONFSUCCES") != -1 || errString.indexOf("VTCONF") > -1 || errString.indexOf("SEND_SUCCESS") > -1))
				{
					errString = "";
				}
				//Modified by Rohini T on [28/02/2021][End]
				System.out.println("errString:misc val conf: "+errString);
				//MiscValConfObj=null;

				/*
				 * if(errString.indexOf("CONFSUCCES") > -1) { errString = ""; }
				 */

				//}

				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
			}

			catch (Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
			finally {
				try {
					if (rs != null) {
						rs.close();
						rs = null;
					}
					if (pstmt != null) {
						pstmt.close();
						pstmt = null;
					}
					//added by monika salla on 3 april 2021
					/*if (conn != null && !conn.isClosed())
					{
						conn.close();
						conn = null;

					}*///COMMENTED 0N 7TH APRIL
					System.out.println("Transaction commited 444.............from MiscValConf");


				} catch (Exception e) {
					e.printStackTrace();
					throw new ITMException(e);
				}
			}
			return errString;
		}

		private String findValue(Connection conn, String columnName, String tableName, String columnName2, String value)
				throws ITMException, RemoteException {
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			String sql = "";
			String findValue = "";
			try {
				sql = "SELECT " + columnName + " from " + tableName + " where " + columnName2 + "= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, value);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					findValue = rs.getString(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			} catch (Exception e) {
				System.out.println("Exception in findValue ");
				e.printStackTrace();
				throw new ITMException(e);
			}
			System.out.println("returning String from findValue " + findValue);
			return findValue;
		}

		private java.sql.Timestamp getDueDate(Connection conn, SimpleDateFormat simpleDateFormat, String startFrom,
				String tranDate, String effDate, String billDate, int crDays) throws ITMException {
			Calendar cal = Calendar.getInstance();
			Timestamp dueDate = null;
			try {

				/*
				 * System.out.println("In getDueDate startFrom [" + startFrom + "] tranDate [" +
				 * tranDate + "] effDate [" + effDate + "] billDate [" + billDate + "] crDays ["
				 * + crDays + "]");
				 	System.out.println("In getDueDate  tranDate [" + simpleDateFormat.parse(tranDate) + "]");*/
				if ("R".equalsIgnoreCase(startFrom)) {
					cal.setTime((simpleDateFormat.parse(tranDate)));
					cal.getTime();
					cal.add(Calendar.DATE, crDays);
					dueDate = (Timestamp) cal.getTime();
				} else if ("D".equalsIgnoreCase(startFrom)) {
					cal.setTime((simpleDateFormat.parse(effDate)));
					cal.getTime();
					cal.add(Calendar.DATE, crDays);
					dueDate = (Timestamp) cal.getTime();
				} else if ("Q".equalsIgnoreCase(startFrom)) {
					cal.setTime((simpleDateFormat.parse(effDate)));
					cal.getTime();
					cal.add(Calendar.DATE, crDays);
					dueDate = (Timestamp) cal.getTime();
				} else if ("B".equalsIgnoreCase(startFrom)) {
					cal.setTime((simpleDateFormat.parse(billDate)));
					cal.getTime();
					cal.add(Calendar.DATE, crDays);
					dueDate = (Timestamp) cal.getTime();
				}
				System.out.println("In getDueDate [" + startFrom + "] dueDate [" + simpleDateFormat.format(dueDate) + "]");

			} catch (ParseException e) {
				System.out.println("Exception in  date [duedate]" + e);
				e.printStackTrace();
				throw new ITMException(e);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			System.out.println("Returning Due date [duedate] from function" + dueDate);
			return dueDate;
		}

		// Added by Anagha Rane (To generate misc. voucher) 02-04-2020 Serdia
		// Customization End
		//Modified by Rohini Telang on 03/03/2021[Start]
		private String intializingLog(String fileName,String tranID) throws ITMException
		{
			String log="";
			String strToWrite = "";
			String currTime = null;
			try
			{
				//System.out.println(">>>In intializingLog() fileName:"+fileName);
				SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
				try
				{
					System.out.println("In intializingLog method");
					currTime = sdf1.format(new Timestamp(System.currentTimeMillis())).toString();
					currTime = currTime.replaceAll("-","");
					calendar.setTime(new java.sql.Timestamp(System.currentTimeMillis()));
					//fileName = fileName+currTime+calendar.get(Calendar.HOUR)+""+calendar.get(Calendar.MINUTE)+".csv";
					//Pavan R on 15mar19[to change the log file name] start
					//fileName= saleOrder.trim().toLowerCase()+ "_post.log";
					fileName= "distrcp_conf"+tranID.trim()+ ".log";
					System.out.println("fileName: :"+fileName);
					//fos1 = new FileOutputStream(CommonConstants.JBOSSHOME + File.separator +"EDI"+File.separator+fileName);
					//Added by Anagha R on 08/12/2020 for Sales order posting START
					String filePath = CommonConstants.JBOSSHOME + File.separator +"EDI";
					System.out.println("filePath..."+filePath);
					try 
					{
						File file = new java.io.File(filePath);
						System.out.println("file..."+file);
						if (!file.exists()) 
						{
							file.mkdir();
						} 
						filePath = filePath + File.separator +fileName;
						System.out.println("filePath...$$$$"+filePath);
					} 
					catch (Exception e) 
					{
						System.out.println("exception occured while creating file...");
						e.printStackTrace();
					}
					fos1 = new FileOutputStream(filePath);
					System.out.println("fos1...11"+fos1);
					//Added by Anagha R on 08/12/2020 for Sales order posting END

					//logFile="c:\\appl\\itm26\\" + fromSaleOrder.trim().toLowerCase()+ "_post.log";
					//strToWrite="\"TRANID\",\"START TIME\",\"END TIME\",\"STATUS\"\r\n";
					//fos1.write(strToWrite.getBytes());
					log ="IntializingLog_Successesfull";
				}
				catch(Exception e)
				{
					System.out.println(e.getMessage());
					e.printStackTrace();
					throw new ITMException(e);
				}
				startDate = new java.util.Date(System.currentTimeMillis());
				calendar.setTime(new java.sql.Timestamp(System.currentTimeMillis()));
				startDateStr = sdf1.format(startDate)+" "+calendar.get(Calendar.HOUR)+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND);
				fos1.write(("Distribution Receipt Confirmation started at: " + startDateStr +"\r\n~").getBytes());
			}
			catch(Exception e)
			{
				System.out.println("Exception []::"+e.getMessage());
				log="IntializingLog_Failed";
				e.printStackTrace();


				System.out.println(e.getMessage());
				e.printStackTrace();
				throw new ITMException(e);


			}
			return log;
		}
		public String createPostLog(String lineNo, String itemCode, String locCode, String lotNo,String lotSl, double quantity,String logMsg)
		{
			String retString = "";				
			retString = "#" + lineNo + "~" + itemCode + "~" + locCode + "~" + lotNo + "~" + lotSl + "~" + quantity + "~" + logMsg;		
			return retString;
		}

		//Modified by Rohini Telang on 03/03/2021[End]

		//added by monika salla on 23 march 2021
		private String genMiscDrCrRcp(String tranId,String recoverTranType, String siteCodeAs, String xtraParams, Connection conn)
				throws ITMException {


			String currCode = "", confirmed = "", siteCode = "", tranType = "", sreturnNo = "", tranIdNew = "";
			String itemCode = "", taxChap = "", taxClass = "", taxEnv = "";
			String transMode = "", chgUser = "", chgTerm = "", errString = "", sql = "", sql1 = "";
			String distOrder = "", tranIdIss = "", siteCodeShip = "", confPasswd = "", orderType = "", qcReqd = "",
					issueRef = "";
			String locCodeGit = "", frtType = "", chgUsr = "", empCodeAprv = "", lotNo = "", cctrCode = "",
					cctrCodeDet = "", tranSer = "", tranWin = "";
			String analysis1 = "", analysis2 = "", analysis3 = "", unit = "", lotSl = "", acctCode = "";
			String lineNoDistOrder = "", locCode = "", packCode = "", packInstr = "", dimension = "", grade = "";
			String suppCode = "", acctCodeAp = "", cctrCodeAp = "", acctCodeApAdv = "", cctrCodeApAdv = "", stanCode = "",
					suppType = "", payMode = "", crTerm = "", suppName = "", lrNo = "";
			Timestamp chgDate = null, tranDate = null, effDate = null, confDate = null, gpDate = null, retestDate = null,
					lrDate = null, currDate = null;
			double amount = 0, taxAmt = 0, netAmt = 0, rateClg = 0, exchRate = 0, rate = 0, calAmount = 0;
			double discount = 0, costRate = 0;
			int count = 0;
			String  lineNo = ""; 
			int grossWeight = 0, tareWeight = 0, netWeight = 0, volume = 0, frtAmt = 0, noArt = 0, quantity = 0;
			int lineNoSret = 0, lineNoInvtrace = 0, lineNoRcpinv = 0, i = 0, actualQty = 0, palletWt = 0;
			String startFrom = "", billDate = "", finEntity = "";
			int crDays = 0;
			Timestamp dueDate = null;
			double totAmtDet = 0.0, totBillAmtDet = 0.0, totNetAmtDet = 0.0;

			PreparedStatement pstmt = null;
			PreparedStatement pstmt1 = null;
			PreparedStatement pstmt2 = null;
			ResultSet rs = null;
			ResultSet rs1 = null;
			ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
			boolean isError = false;
			String custCode="";
			FinCommon fcom = new FinCommon();
			String acctCodeaAr="",cctrCodeAr="",custType="",rcpMode="",custName="";
			String  retString = "";
			String mUnit="";
			try {
				currDate = new java.sql.Timestamp(System.currentTimeMillis());
				SimpleDateFormat sdfAppl = new SimpleDateFormat(genericUtility.getApplDateFormat());
				String currDateStr = sdfAppl.format(currDate);

				chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
				chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");
				chgDate = new java.sql.Timestamp(new java.util.Date().getTime());

				sql = "select tran_date,eff_date,dist_order,tran_id__iss,site_code,site_code__ship,gross_weight,"
						+ "tare_weight,net_weight,volume,frt_amt,amount,tax_amt,net_amt,loc_code__git,frt_type,"
						+ "chg_user,chg_term,curr_code,chg_date,confirmed,conf_date,no_art,trans_mode,conf_passwd,"
						+ "order_type,tran_type,exch_rate,emp_code__aprv,qc_reqd,gp_date,issue_ref,lr_no,lr_date "
						+ "from distord_rcp where tran_id = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);

				rs = pstmt.executeQuery();
				while (rs.next()) {
					tranDate = rs.getTimestamp("tran_date");
					effDate = rs.getTimestamp("eff_date");
					distOrder = rs.getString("dist_order");
					tranIdIss = rs.getString("tran_id__iss");
					siteCode = rs.getString("site_code");
					siteCodeShip = rs.getString("site_code__ship");
					grossWeight = rs.getInt("gross_weight");
					tareWeight = rs.getInt("tare_weight");
					netWeight = rs.getInt("net_weight");
					volume = rs.getInt("volume");
					frtAmt = rs.getInt("frt_amt");
					amount = rs.getDouble("amount");
					System.out.println("Amount selected from distord_rcp: " + amount);
					taxAmt = rs.getInt("tax_amt");
					netAmt = rs.getInt("net_amt");
					locCodeGit = rs.getString("loc_code__git");
					frtType = rs.getString("frt_type");
					chgUsr = rs.getString("chg_user");
					chgTerm = rs.getString("chg_term");
					currCode = rs.getString("curr_code");
					chgDate = rs.getTimestamp("chg_date");
					confirmed = rs.getString("confirmed");
					confDate = rs.getTimestamp("conf_date");
					noArt = rs.getInt("no_art");
					transMode = rs.getString("trans_mode");
					confPasswd = rs.getString("conf_passwd");
					orderType = rs.getString("order_type");
					tranType = rs.getString("tran_type");
					exchRate = rs.getDouble("exch_rate");
					empCodeAprv = rs.getString("emp_code__aprv");
					qcReqd = rs.getString("qc_reqd");
					gpDate = rs.getTimestamp("gp_date");
					issueRef = rs.getString("issue_ref");
					lrNo = rs.getString("lr_no");
					lrDate = rs.getTimestamp("lr_date");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				String siteCodeDlv="";

				sql = "select site_code__dlv from distorder  where dist_order= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, distOrder);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					siteCodeDlv = rs.getString("site_code__dlv");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				String xmlValues = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
				xmlValues = xmlValues + "<Header></Header>";
				xmlValues = xmlValues + "<Detail1>";
				xmlValues = xmlValues + "<tran_id>" + "</tran_id>";
				xmlValues = xmlValues + "<site_code>" + siteCode.trim() + "</site_code>";
				xmlValues = xmlValues + "<tran_date>" + getCurrdateAppFormat() + "</tran_date>";
				xmlValues = xmlValues + "<tran_type>" + recoverTranType + "</tran_type>";
				xmlValues = xmlValues + "<vouch_type> E </vouch_type>";
				xmlValues = xmlValues + "</Detail1></Root>";


				//ADDED BY MONIKA FOR CREDIT NOTE GENERATION
				String ls_ret_opt="",ls_drcr_flag="",ls_tranwin="",objName="",keystr="",keyCol="";
				boolean lb_flag;
				if ("D".equalsIgnoreCase(ls_ret_opt))
				{
					ls_drcr_flag = "D";		
					tranSer = "MDRCRD";
					ls_tranwin = "W_MISC_DRCR_RCP_DR";
					objName = "misc_drcr_rcp_dr";
				}
				else
				{
					ls_drcr_flag = "C";			
					tranSer = "MDRCRC";
					ls_tranwin = "W_MISC_DRCR_RCP_CR";
					objName = "misc_drcr_rcp_cr";

				}

				lb_flag = false;
				sql = "select KEY_STRING, TRAN_ID_COL, REF_SER from transetup where upper(tran_window) = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,ls_tranwin );
				rs = pstmt.executeQuery();
				if(rs.next())
				{	
					keystr = checkNullAndTrim(rs.getString("KEY_STRING"));
					keyCol = rs.getString("TRAN_ID_COL");
					tranSer = rs.getString("REF_SER");
				}

				if (pstmt != null )
				{
					pstmt.close();
					pstmt = null;
				}				
				if (rs != null )
				{
					rs.close();
					rs = null;
				}			

				if(keystr == null || keystr.length() == 0)
				{
					sql = "select KEY_STRING, TRAN_ID_COL, REF_SER from transetup where tran_window = 'GENERAL' ";
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if(rs.next())
					{	
						keystr = checkNullAndTrim(rs.getString("KEY_STRING"));
						keyCol = rs.getString("TRAN_ID_COL");
						tranSer = rs.getString("REF_SER");
					}

					if (pstmt != null )
					{
						pstmt.close();
						pstmt = null;
					}				
					if (rs != null )
					{
						rs.close();
						rs = null;
					}			
				}
				//System.out.println("ls_tran_id ["+ls_tran_id+"]");

				////////////////////////////////

				// data from site_customer
				System.out.println(" MISCDRCR RCP tranType:--["+tranType+"] recoverTranType---["+recoverTranType+" ]site code dlv-->["+siteCodeDlv+" ] site code---["+siteCode+" ]site code ship ["+siteCodeShip);


				sql = "select count(*) from site_customer where site_code__ch = ? and site_code = ? and channel_partner = 'Y' ";
				pstmt = conn.prepareStatement(sql);

				pstmt.setString(1, siteCodeShip);
				pstmt.setString(2, siteCode);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					count = rs.getInt(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				System.out.println("tranType:--["+tranType+"] recoverTranType---["+recoverTranType+" ] site code---["+siteCode);

				// data from customer
				if (count == 0) {
					sql = "select count(*) from customer where site_code = ? and case when channel_partner is null then 'N' else channel_partner end = 'Y'";
					pstmt = conn.prepareStatement(sql);
					//pstmt.setString(1, siteCodeAs);
					// pstmt.setString(1, siteCodeDlv);
					pstmt.setString(1, siteCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						count = rs.getInt(1);
					}

					if (count > 1) {
						retString = itmDBAccessLocal.getErrorString("", "ERRORVTCPC", "", "", conn);
						return retString;
					} else if (count == 0) {
						retString = itmDBAccessLocal.getErrorString("", "VTCUSTCD4", "", "", conn);
						return retString;
					}
					// cust_code from customer
					else if (count == 1)
					{
						sql1 = "select cust_code from customer where site_code = ? and channel_partner = 'Y'";
						pstmt1 = conn.prepareStatement(sql1);
						//pstmt1.setString(1, siteCodeAs);
						//pstmt1.setString(1, siteCodeDlv); 
						pstmt1.setString(1, siteCode); 
						rs1 = pstmt1.executeQuery();
						if (rs1.next())
						{
							custCode = rs1.getString("cust_code");
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
					}
				}

				// cust_code from site_customer
				else if (count == 1) 
				{
					sql = "select cust_code from site_customer where site_code__ch = ? and site_code = ?  and channel_partner = 'Y'";
					pstmt = conn.prepareStatement(sql);
					//pstmt.setString(1, siteCodeAs);
					//pstmt.setString(2, siteCodeDlv);
					//pstmt.setString(1, siteCodeDlv);
					pstmt.setString(1, siteCodeShip);//ADDED BY MONIKA SALLA ON 8 APRIL 2021 TO SET PROPER CUSTOMER
					pstmt.setString(2, siteCode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						custCode = rs.getString("cust_code");
					}
				}
				if (rs != null) {
					rs.close();
					rs = null;

				}
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				
				// finentity
				sql = "select fin_entity from site where site_code= ? ";
				pstmt = conn.prepareStatement(sql);
				//pstmt.setString(1, siteCodeAs);
				pstmt.setString(1, siteCodeDlv);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					finEntity = rs.getString("fin_entity");
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;

				sql = "select acct_code__ar,cctr_code__ar,acct_code__adv,cctr_code__adv,stan_code,cust_type, rcp_mode, cr_term, cust_name from customer where cust_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, custCode);

				rs = pstmt.executeQuery();
				if (rs.next()) {
					acctCodeaAr = rs.getString("acct_code__ar");
					cctrCodeAr = rs.getString("cctr_code__ar");
					acctCodeApAdv = rs.getString("acct_code__adv");
					cctrCodeApAdv = rs.getString("cctr_code__adv");
					stanCode = rs.getString("stan_code");
					custType = rs.getString("cust_type");
					rcpMode = rs.getString("rcp_mode");
					crTerm = rs.getString("cr_term");
					custName = rs.getString("cust_name");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				// MISC_DRCR_RCP
				//Added by Anagha R on 08/Sep/2020 for Serdia: GST Recovery change START
				amount = 0.0;
				//END

				sql = "insert into misc_drcr_rcp(tran_id,tran_ser,tran_date,eff_date,fin_entity,site_code,sundry_type,sundry_code,acct_code,"
						+ " cctr_code,amount,curr_code,exch_rate,confirmed,chg_user,chg_date,chg_term,"
						+ " due_date,tran_type,emp_code__aprv,drcr_flag,remarks,parent__tran_id) "
						+ "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);//added by monika on 2 sept 2020 -to set tran id same same dist issue
				//pstmt.setString(2, "MDRCRD");
				//BY MONIKA SALLA credit note creation on dist order on gst recovery 27 march 2021---
				pstmt.setString(2, "MDRCRC");
				pstmt.setTimestamp(3, tranDate);
				pstmt.setTimestamp(4, effDate);
				pstmt.setString(5, finEntity);
				//pstmt.setString(6, siteCodeAs);
				// pstmt.setString(6, siteCodeDlv);//added by monika salla 27 march 2021 for gst recovery
				//CSA site to Serdia HO trf from S0003 to S0001
				pstmt.setString(6, siteCode);//end
				pstmt.setString(7, "C");
				pstmt.setString(8, custCode);
				pstmt.setString(9, acctCodeaAr);
				pstmt.setString(10, cctrCodeAr);
				pstmt.setDouble(11, calAmount);
				pstmt.setString(12, currCode);
				pstmt.setDouble(13, exchRate);
				pstmt.setString(14, "N");
				pstmt.setString(15, chgUser);
				pstmt.setTimestamp(16, tranDate);
				pstmt.setString(17, chgTerm);
				pstmt.setTimestamp(18, tranDate);
				pstmt.setString(19, recoverTranType);
				//pstmt.setString(19, tranType);
				pstmt.setString(20, null);
				//pstmt.setString(21, "D");
				pstmt.setString(21, "C");
				pstmt.setString(22, "GST Receivable at HO against D-RCP -:" + tranId);
				pstmt.setString(23, tranId);//added by monika salla to get tran id in creation of misc debit credit

				int count1 = pstmt.executeUpdate();
				System.out.println("inserted dATA -->"+count1+"account code___["+acctCodeaAr+"] cctrcode ["+cctrCodeAr);
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				sql = "select TX.ACCT_CODE__RECO,TX.CCTR_CODE__RECO,SUM(TX.TAX_AMT) from taxtran TX, tax T where TX.TAX_CODE=T.TAX_CODE"+
						" AND T.TAX_TYPE IN ('I','G','H','J')AND TX.TRAN_ID=? AND TX.TAX_AMT <>0 AND tran_code='D-RCP'"+
						" GROUP BY TX.ACCT_CODE__RECO,TX.CCTR_CODE__RECO";
				//Changed by Anagha R on 1-Jun-2020 for Serdia Customization Release11052020

				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				//if (rs.next()) { //Commented by Anagha R on 09/Sep/2020 for Serdia: GST Recovery change
				int rowCnt = 0;
				while (rs.next())
				{ //Added by Anagha R on 09/Sep/2020 for Serdia: GST Recovery change    
					//calAmount = rs.getInt(1);
					acctCode = rs.getString(1);//Changed by Anagha R on 1-Jun-2020 for Serdia Customization Release11052020
					cctrCode = rs.getString(2);//Changed by Anagha R on 1-Jun-2020 for Serdia Customization Release11052020
					calAmount = rs.getDouble(3);//end					
					System.out.println("AcctCode: "+acctCode);
					System.out.println("CCTRCode: "+cctrCode);
					System.out.println("Amount calculated: " + calAmount);
					//Added by Anagha R on 09/09/2020 to insert details multiple times and get updated amount for Serdia: GST Recovery change START

					rowCnt++;     
					if ("NULLFOUND".equalsIgnoreCase(cctrCode) || cctrCode == null || cctrCode == "" || cctrCode.length() == 0) 
					{
						cctrCode = cctrCodeAp;
					}

					sql = "insert into misc_drcr_rdet (tran_id,line_no,acct_code,cctr_code,amount,quantity,rate,unit,net_amt) "
							+ "	Values (?,?,?,?,?,?,?,?,?)";
					pstmt2 = conn.prepareStatement(sql);
					pstmt2.setString(1, tranId);
					pstmt2.setInt(2, rowCnt);
					pstmt2.setString(3, acctCode);
					pstmt2.setString(4, cctrCode);
					pstmt2.setDouble(5, calAmount);
					pstmt2.setDouble(6, 1);
					pstmt2.setDouble(7, rate);
					pstmt2.setString(8, mUnit);
					pstmt2.setDouble(9, calAmount);//added by monika salla on 12 jan 21
					count = pstmt2.executeUpdate();
					if(pstmt2!=null)
					{
						pstmt2.close();
						pstmt2 = null;
					}

					totAmtDet=totAmtDet+Math.abs(calAmount);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				sql = "update misc_drcr_rcp set amount = ? where tran_id = ?";
				pstmt = conn.prepareStatement(sql);

				pstmt.setDouble(1, totAmtDet);
				pstmt.setString(2, tranId);
				int updCnt = pstmt.executeUpdate();

				if(pstmt!=null)
				{
					pstmt.close();
					pstmt = null;
				}
				System.out.println("update amount Successfully1234:"+updCnt);

				MiscDrCrRcpConf confDebitNote = new MiscDrCrRcpConf();
				//retString = confDebitNote.confirm(tranIdNew, xtraParams, "", conn);
				retString = confDebitNote.confirm(tranId, xtraParams, "", conn);//added by monika to set tran id as dist issue on -2 sept 2020
				System.out.println("After DrCrRcpConf---->[" + retString + "]");
				if(retString != null && (retString.indexOf("CONFSUCCES") != -1|| errString.indexOf("VTCONF") > -1 || errString.indexOf("SEND_SUCCESS") > -1))
				{ 
					
					retString ="SEND_SUCCESS";
					System.out.println("out DrCrRcpConf---->[" + retString + "]");
					//retString="";
					System.out.println("out DrCrRcpConf-12345--->[" + retString + "]");
				}
			}

			catch (SQLException e) {
				System.out.println("Exception :DISSPOSCONF: actionVoucher " + e.getMessage());
				throw new ITMException(e);
			} catch (Exception e) {
				System.out.println("Exception :DISSPOSCONF : actionHandler " + e.getMessage());
				throw new ITMException(e);
			} finally {

				try {
					if (pstmt != null) {
						pstmt.close();
						pstmt = null;
					}
					if (pstmt1 != null) {
						pstmt1.close();
						pstmt1 = null;
					}

					if (rs != null) {
						rs.close();
						rs = null;
					}
					if (rs1 != null) {
						rs1.close();
						rs1 = null;
					}
					/*if (conn != null && !conn.isClosed()) {
						conn.close();
						conn = null;

					}*///COMMENTED ON 7TH APRIL
					System.out.println("Transaction commited 555.............from MiscDRCRConf");

				} catch (Exception e) {
					e.printStackTrace();
					throw new ITMException(e);
				}

			}
			System.out.println("valueXmlString.toString() " + retString.toString());
			return retString;
		}

		//end

	}
