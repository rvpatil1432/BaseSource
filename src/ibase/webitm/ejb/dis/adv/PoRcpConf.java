/**
 * PURPOSE : Confirmation of Purchase receipt
 * AUTHOR :   
 */

package ibase.webitm.ejb.dis.adv;

import ibase.utility.EMail;
import ibase.utility.BaseLogger;
import ibase.utility.CommonConstants;
import ibase.webitm.utility.*;
import ibase.webitm.ejb.*;
import ibase.webitm.ejb.mfg.ExplodeBom;
import ibase.webitm.ejb.mfg.InvDemSuppTraceBean;
import ibase.webitm.ejb.mfg.adv.RcpBackflushConfirm;
import ibase.webitm.ejb.sys.*;
import ibase.system.config.*;
import ibase.webitm.utility.ITMException;

import java.text.*;

import ibase.webitm.ejb.fin.*;
import ibase.webitm.ejb.fin.AssetInstall;
import ibase.webitm.utility.TransIDGenerator;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.dis.*;
import ibase.webitm.ejb.dis.adv.PorderConf;

import java.rmi.RemoteException;
import java.sql.*;
import java.util.*;

import javax.ejb.*;
import javax.naming.InitialContext;
import javax.ejb.Stateless;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
//import org.drools.runtime.pipeline.SmooksTransformerProvider;
import org.w3c.dom.*;

import javax.xml.rpc.ParameterMode;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.parsers.*;

import java.text.SimpleDateFormat;

@Stateless
public class PoRcpConf extends ActionHandlerEJB implements PoRcpConfLocal, PoRcpConfRemote
{
	/**
	 * The public method is used for confirming the purchase receipt transaction
	 * Returns confirmation message on successfull confirm otherwise returns
	 * error message
	 * 
	 * @param tranId
	 *            is the transaction id to be confirmed
	 * @param xtraParams
	 *            contais additional information such as
	 *            loginEmpCode,loginCode,chgTerm etc
	 * @param forcedFlag
	 *            (true or false)
	 */
	String userId = "", termId = "", lckGroup = "";
	ibase.utility.E12GenericUtility genericUtility = new ibase.utility.E12GenericUtility();
	CommonConstants commonConstants = new CommonConstants();
    DistCommon distCommon = new DistCommon();
    UtilMethods utl = new UtilMethods();

	// overloaded method added to call the confirm method from postsave
	// component - 25/11/11 - Gulzar
	public String confirm(String tranID, String xtraParams, String forcedFlag) throws RemoteException, ITMException
	{
		String retString = "";
		boolean isConn = false;
		Connection conn = null;

		//Modified by Anjali R. on[24/10/2018][Start]
		String sql = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String confirmed = ""; 
		ITMDBAccessEJB itmDBAccessLocal = null;
		//Modified by Anjali R. on[24/10/2018][End]

		try
		{
			//Modified by Anjali R. on[24/10/2018][Start]
			conn = getConnection();
			itmDBAccessLocal = new ITMDBAccessEJB();
			//Modified by Anjali R. on[24/10/2018][End]

			retString = confirm(tranID, xtraParams, forcedFlag, conn, isConn);

			//Modified by Anjali R. on[24/10/2018][Start]
			sql = "select confirmed from porcp where tran_id = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranID);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				confirmed  = checkNull(rs.getString("confirmed"));
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

			if(!"Y".equalsIgnoreCase(confirmed))
			{
				conn.rollback();
				//Modified by Anjali R. on [01/11/2018][Start]
				//if(retString != null  && retString.trim().length() > 0)
				if(retString == null  || retString.trim().length() == 0)
					//Modified by Anjali R. on [01/11/2018][End]
				{
					retString = itmDBAccessLocal.getErrorString("", "DS000", "","",conn);
				}
			}
			else
			{
				conn.commit();
				retString = itmDBAccessLocal.getErrorString("", "VTMCONF2", "","",conn);
			}

			/*if (retString != null && retString.length() > 0  )
			{
				//throw new Exception("Exception while calling confirm for tran  Id:[" + tranID + "]");
			}*/
			//Modified by Anjali R. on[24/10/2018][End]
		} catch (Exception exception)
		{
			System.out.println("Exception in [InvHoldConfEJB] getCurrdateAppFormat " + exception.getMessage());
			//Added by Anjali R.  on[4/10/2018][Start]

			try
			{
				conn.rollback();
			}
			catch(Exception e)
			{
				System.out.println("Exception--["+e.getMessage()+"]");
				e.printStackTrace();
				throw new ITMException(e);
			}
			exception.printStackTrace();
			throw new ITMException(exception);

		}
		finally
		{
			try
			{
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
				if(conn != null)
				{
					conn.close();
					conn = null;
				}
			}
			catch(Exception e)
			{
				System.out.println("Exception in finally--["+e.getMessage()+"]");
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		//Added by Anjali R.  on[4/10/2018][End]
		return retString;
	}

	// Commented and new parameters(conn, connStatus) to function - 25/11/11 -
	// Gulzar
	/**
	 * The public method is used for confirming the purchase receipt transaction
	 * Returns confirmation message on successfull confirm otherwise returns
	 * error message
	 * 
	 * @param tranId
	 *            is the transaction id to be confirmed
	 * @param xtraParams
	 *            contais additional information such as
	 *            loginEmpCode,loginCode,chgTerm etc
	 * @param forcedFlag
	 *            (true or false)
	 * @param Connection
	 *            (true or false) - Connection to database
	 * @param connStatus
	 *            (true or false) - This status indicate whether to commit the
	 *            trasaction or not (true - commit, false - no commit)
	 */
	// public String confirm(String tranId, String xtraParams, String
	// forcedFlag) throws RemoteException, ITMException
	public String confirm(String tranId, String xtraParams, String forcedFlag, Connection conn, boolean connStatus) throws RemoteException, ITMException
	{

		// Connection conn = null; //Gulzar - 25/11/11
		PreparedStatement pstmtSql = null;
		ResultSet rs = null;

		String retString = "";
		String sql = "";
		String conf = "";
		String siteRcp = "";
		String invserialNo ="",itemCode ="" ,sItem = "";
		HashSet<String> sh = new HashSet<String>();
		try
		{
			if (conn == null)// changed by Gulzar - 25/11/11
			{
				ConnDriver connDriver = null;
				connDriver = new ConnDriver();
				//Changes and Commented By Bhushan on 09-06-2016 :START
				//conn = connDriver.getConnectDB("DriverITM");
				conn = getConnection();
				//Changes and Commented By Bhushan on 09-06-2016 :END 
				conn.setAutoCommit(false);
				connDriver = null;
				connStatus = true;
			}

			ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			termId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");
			if (userId == null || userId.trim().length() == 0)
			{
				userId = "SYSTEM";
			}
			if (termId == null || termId.trim().length() == 0)
			{
				termId = "SYSTEM";
			}

			sql = "SELECT CONFIRMED, SITE_CODE FROM PORCP WHERE TRAN_ID= ?";
			pstmtSql = conn.prepareStatement(sql);
			pstmtSql.setString(1, tranId);
			rs = pstmtSql.executeQuery();
			if (rs.next())
			{
				conf = checkNull(rs.getString("CONFIRMED"));
				siteRcp = checkNull(rs.getString("SITE_CODE"));
			}

			if (pstmtSql != null)
			{
				pstmtSql.close();
				pstmtSql = null;
			}
			if (rs != null)
			{
				rs.close();
				rs = null;
			}
			if (conf.equalsIgnoreCase("Y"))
			{
				retString = itmDBAccessLocal.getErrorString("", "VTCONF1", "","",conn);
				return retString;
			} else
			{

				/*//Added By PriyankaC 05/03/2018.[START]
				System.out.println("tran_id : " +tranId);
				sql ="SELECT ITEM_CODE FROM PORCPDET WHERE TRAN_ID= ? ";
				pstmtSql = conn.prepareStatement(sql);
				pstmtSql.setString(1, tranId);
				rs = pstmtSql.executeQuery();
				while (rs.next())
				{
					 itemCode = checkNull(rs.getString("ITEM_CODE"));				
					 sh.add(itemCode);
				}
				System.out.println("itemCode :" +itemCode +"Hashset" +sh);
				if (pstmtSql != null)
				{
					pstmtSql.close();
					pstmtSql = null;
				}
				if (rs != null)
				{
					rs.close();
					rs = null;
				}

				if(sh.isEmpty() == false)
				{
					Iterator itr = sh.iterator();
					while(itr.hasNext())
					{
						sItem = sItem + "'"+(String) itr.next() + "'"+",";				
					}
				}
				if(sItem != null && sItem.length() != 0)
	            {
					sItem = sItem.substring(0,sItem.length()-1);
	            }
	            else
	            {
	            	sItem = "''";
	            }
				System.out.println("sItem : " +sItem);
				sql ="SELECT INV_SRNO_REQD FROM ITEM WHERE ITEM_CODE in ("+sItem+") ";
				pstmtSql = conn.prepareStatement(sql);
			//	pstmtSql.setString(1, itemCode);
				rs = pstmtSql.executeQuery();
				while (rs.next())
				{
					 invserialNo = checkNull(rs.getString("INV_SRNO_REQD"));
					 System.out.println("invserialNo : "+invserialNo);
					 if ("Y".equalsIgnoreCase(invserialNo))
						{ 
							retString = itmDBAccessLocal.getErrorString("", "VTINVSRNRQ", "","",conn);
							return retString;
						}
				}
				if (pstmtSql != null)

				{
					pstmtSql.close();
					pstmtSql = null;
				}
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				//Added By PriyankaC on 05/03/2018.. [END]
				 */				

				retString = retrieveReceipt(tranId, 1, conn, xtraParams);
				if (retString == null || retString.trim().length() == 0)
				{
					// //////////////////// EDI creation
					String ediOption = "";
					String dataStr = "";
					sql = "SELECT EDI_OPTION FROM TRANSETUP WHERE TRAN_WINDOW = 'w_porcp' ";
					pstmtSql = conn.prepareStatement(sql);
					rs = pstmtSql.executeQuery();
					if (rs.next())
					{
						ediOption = checkNull(rs.getString("EDI_OPTION"));
					}
					rs.close();
					rs = null;
					pstmtSql.close();
					pstmtSql = null;

					if ("1".equals(ediOption.trim()))
					{
						CreateRCPXML createRCPXML = new CreateRCPXML("w_porcp", "tran_id");
						dataStr = createRCPXML.getTranXML(tranId, conn);
						System.out.println("dataStr =[ " + dataStr + "]");
						Document ediDataDom = genericUtility.parseString(dataStr);

						E12CreateBatchLoadEjb e12CreateBatchLoad = new E12CreateBatchLoadEjb();
						retString = e12CreateBatchLoad.createBatchLoad(ediDataDom, "w_porcp", "0", xtraParams, conn);
						createRCPXML = null;
						e12CreateBatchLoad = null;

						if (retString != null && "SUCCESS".equals(retString))
						{
							System.out.println("retString from batchload = [" + retString + "]");
						}
					}
					// ///////////////////

					if (connStatus)// Condition added - 25/11/11 - Gulzar as
						// confirm method is called from post save
						// component
					{
						conn.commit();
						retString = itmDBAccessLocal.getErrorString("", "VTMCONF2", "","",conn);
					}
				} else
				{
					conn.rollback();
				}
			}
		} catch (Exception e)
		{
			try
			{
				conn.rollback();
			} catch (Exception e1)
			{
				//Modified by Anjali R. on[25/10/2018][Start]
				e1.printStackTrace();
				System.out.println("Exception ::" + e.getMessage());
				throw new ITMException(e1);
				//Modified by Anjali R. on[25/10/2018][End]
			}
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			throw new ITMException(e);
		} finally
		{
			try
			{
				if (pstmtSql != null)
				{
					pstmtSql.close();
					pstmtSql = null;
				}
				// if( conn != null && ! conn.isClosed() ) // Gulzar - 25/11/11
				if (conn != null && !conn.isClosed() && connStatus) // Gulzar -
					// 25/11/11
				{
					conn.close();
					conn = null;
				}
			} catch (Exception e)
			{
				System.out.println(e.getMessage());
				e.printStackTrace();//Modified by Anjali R. on[25/10/2018]
				throw new ITMException(e);
			}
		}
		System.out.println("Returning Result ::" + retString);
		return retString;
	}

	public String retrieveReceipt(String tranId, int a, Connection conn, String xtraParams) throws RemoteException, ITMException
	{
		PreparedStatement pstmtSql = null, pstmt2 = null, pstmt3 = null, pstmt4 = null, pstmt5 = null,pstmt6 = null;
		PreparedStatement pstmtUpd = null;
		ResultSet rs = null, rs2 = null, rs3 = null, rs4 = null, rs5 = null, rs6 = null;

		String sql = "";
		String dbName = "";
		String retString = "";
		String purcOrder = "";
		String purcOrderDet = "";
		String porcpTranType = "";
		String confirmed = "";
		String qcReqd = "";
		String jobWorkType = "";
		String subContractType = "";
		String pordType = "";
		String autoBkFlush = "";
		String empCode = "";
		String siteCode = "";
		String tranSer = "";
		String value = "";
		String lineNoOrd = "";
		String itemCode = "";
		String nullPo = "";
		String policyNo = "";
		String pervPolicy = "";
		String ledgPostConf = "";
		String runMode = "";
		String warning = "";
		String cwipTranType = "";
		String tranType = "";
		String saleOrder = "";
		String err = "";
		String errcode = "";
		String payTrmLineNo = "",voucher_error="";

		double qtyRcp = 0.0;
		double ordQty = 0.0;
		double dlvQty = 0.0;
		double totRcp = 0.0;
		double qtyTol = 0.0;

		long detCnt = 0;
		long noStk = 0;
		long cnt = 0;
		long pcnt = 0;
		long qcCnt = 0;
		long Cnt1 = 0, cnt2 = 0, cnt3 = 0, cnt4 = 0;
		boolean testFlag=false;

		//added by monika salla on 15 sept 2020
		String confirmPoReceipt="",tranIdProjEst="",lineNoPorcpdet="",lineNoProjEst="";
		double totquantity=0.0;//end

		int updCnt;

		java.sql.Timestamp today = null;
		java.sql.Timestamp confDate = null;
		java.util.Date date = null;

		try
		{
			DistCommon distCommon = new DistCommon();
			ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
			dbName = CommonConstants.DB_NAME;
			runMode = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "runMode"));
			if (runMode.length() == 0)
			{
				runMode = "I";
			}
			// 10-oct-2019 manoharan to update GIT stock based on channel_partner flag in header
			if (dbName.equalsIgnoreCase("db2"))
			{
				sql = "SELECT TRAN_ID,(CASE WHEN CONFIRMED IS NULL THEN 'N' ELSE CONFIRMED END), (CASE WHEN QC_REQD IS NULL THEN 'N' ELSE		   QC_REQD END), PURC_ORDER, TRAN_TYPE FROM PORCP WHERE TRAN_ID = ? FOR UPDATE ";
			} else if (dbName.equalsIgnoreCase("mssql"))
			{
				sql = "SELECT TRAN_ID,(CASE WHEN CONFIRMED IS NULL THEN 'N' ELSE CONFIRMED END), (CASE WHEN QC_REQD IS NULL THEN 'N' ELSE		   QC_REQD END), PURC_ORDER , TRAN_TYPE FROM PORCP (UPDLOCK) WHERE TRAN_ID = ?";
			} else
			{
				sql = "SELECT TRAN_ID,(CASE WHEN CONFIRMED IS NULL THEN 'N' ELSE CONFIRMED END), (CASE WHEN QC_REQD IS NULL THEN 'N' ELSE		   QC_REQD END), PURC_ORDER , TRAN_TYPE FROM PORCP WHERE TRAN_ID = ? FOR UPDATE NOWAIT";
			}
			try
			{
				pstmtSql = conn.prepareStatement(sql);
				pstmtSql.setString(1, tranId);
				rs = pstmtSql.executeQuery();
				if (rs.next())
				{
					tranId = checkNull(rs.getString("TRAN_ID"));
					purcOrder = checkNull(rs.getString("PURC_ORDER"));
					porcpTranType = checkNull(rs.getString("TRAN_TYPE"));
					pstmtSql.close();
					pstmtSql = null;
					rs.close();
					rs = null;
				} else
				{
					pstmtSql.close();
					pstmtSql = null;
					rs.close();
					rs = null;
					retString = itmDBAccessLocal.getErrorString("", "VTLCKERR", "","",conn);
					return retString;
				}
			} catch (Exception e)
			{
				retString = itmDBAccessLocal.getErrorString("", "VTLCKERR", "","",conn);
				e.printStackTrace();//Modified by Anjali R. on [25/10/2018]
				throw new ITMException(e);
			}

			jobWorkType = distCommon.getDisparams("999999", "JOBWORK_TYPE", conn);
			if (jobWorkType == null)
			{
				jobWorkType = "";
			}
			subContractType = distCommon.getDisparams("999999", "SUBCONTRACT_TYPE", conn);
			if (subContractType == null)
			{
				subContractType = "";
			}
			//END PC.......
			sql = "SELECT PORD_TYPE FROM PORDER WHERE PURC_ORDER = ?";
			pstmtSql = conn.prepareStatement(sql);
			pstmtSql.setString(1, purcOrder);
			rs = pstmtSql.executeQuery();
			if (rs.next())
			{
				pordType = checkNull(rs.getString("PORD_TYPE"));
			}
			pstmtSql.close();
			pstmtSql = null;
			rs.close();
			rs = null;

			// 26/10/11 manoharan enabled backflush
			if ((pordType.equalsIgnoreCase(jobWorkType.trim()) || pordType.equalsIgnoreCase(subContractType.trim())) && purcOrder.length() > 0)
			{
				autoBkFlush = distCommon.getDisparams("999999", "AUTO_RCP_BKFLUSH", conn);
			} else
			{
				autoBkFlush = "N";
			}

			sql = "SELECT COUNT(1) AS COUNT FROM PORCPDET WHERE TRAN_ID = ?";
			pstmtSql = conn.prepareStatement(sql);
			pstmtSql.setString(1, tranId);
			rs = pstmtSql.executeQuery();
			if (rs.next())
			{
				detCnt = rs.getLong("COUNT");
			}

			if (pstmtSql != null)
			{
				pstmtSql.close();
				pstmtSql = null;
			}
			if (rs != null)
			{
				rs.close();
				rs = null;
			}

			sql = "SELECT COUNT(1) AS COUNT FROM PORCPDET WHERE TRAN_ID = ? AND CASE WHEN EFFECT_STOCK IS NULL THEN 'Y' ELSE  EFFECT_STOCK END  = 'N'";
			pstmtSql = conn.prepareStatement(sql);
			pstmtSql.setString(1, tranId);
			rs = pstmtSql.executeQuery();
			if (rs.next())
			{
				noStk = rs.getLong("COUNT");
			}
			if (pstmtSql != null)
			{
				pstmtSql.close();
				pstmtSql = null;
			}
			if (rs != null)
			{
				rs.close();
				rs = null;
			}

			// to be implemented later
			//if (autoBkFlush.trim().equals("Y") && detCnt > noStk)//COMMENTED BY MONIKA SALLA ON 28 SEPT 2020
			if (autoBkFlush.trim().equals("Y") && detCnt >= noStk)//ADDED EQUAL TOH CONDITION TO CAL AUTO BACKFLUSH METHOD
			{
				System.out.println("INSIDE AUTOBACFLUH 111222");
				retString = autoBackflush(tranId, conn, xtraParams);
				System.out.println(" retString-----"+retString);
				if (retString != null && retString.trim().length() > 0 && retString.indexOf("VTSUCC1") == -1)
				{
					return retString;
				}

				/*
				 * if ( retString != null && retString.trim().length() > 0) {
				 * return retString; }
				 */
			}
			retString = chkUpdAddlCost(tranId, conn);

			if (retString != null && retString.trim().length() > 0)
			{
				return retString;
			}

			retString = checkReplVal(tranId, conn);

			if (retString != null && retString.trim().length() > 0)
			{
				return retString;
			}

			sql = "SELECT EMP_CODE FROM USERS WHERE CODE = ? ";
			pstmtSql = conn.prepareStatement(sql);
			pstmtSql.setString(1, userId);
			rs = pstmtSql.executeQuery();
			if (rs.next())
			{
				empCode = checkNull(rs.getString("EMP_CODE"));
			}
			pstmtSql.close();
			pstmtSql = null;
			rs.close();
			rs = null;

			sql = "SELECT SITE_CODE, TRAN_SER FROM PORCP WHERE TRAN_ID = ? ";
			pstmtSql = conn.prepareStatement(sql);
			pstmtSql.setString(1, tranId);
			rs = pstmtSql.executeQuery();
			if (rs.next())
			{
				siteCode = checkNull(rs.getString("SITE_CODE"));
				tranSer = checkNull(rs.getString("TRAN_SER"));
			}
			pstmtSql.close();
			pstmtSql = null;
			rs.close();
			rs = null;

			value = distCommon.getDisparams("999999", "RCP_UOM_VARIANCE", conn);
			if (value == null)
			{
				retString = itmDBAccessLocal.getErrorString("", "VTUOMVARPARM", "","",conn);
				return retString;
			}
			if (value == null)
			{
				value = "0";
			}
			double val = 0.0;
			try
			{
				val = Double.parseDouble(value);
			} catch (Exception e)
			{
				//Added by Anjali R.  on[4/10/2018][Start]
				System.out.println("Exception--["+e.getMessage()+"]");
				e.printStackTrace();
				throw new ITMException(e);
				//Added by Anjali R.  on[4/10/2018][End]
			}

			sql = "SELECT COUNT(*) AS COUNT FROM PORCPDET WHERE TRAN_ID = ? AND ABS( ((CASE WHEN QUANTITY__STDUOM IS NULL THEN 0 ELSE		   QUANTITY__STDUOM END) * (CASE WHEN RATE__STDUOM IS NULL THEN 0 ELSE RATE__STDUOM END) - (CASE WHEN QUANTITY IS NULL		   THEN 0 ELSE QUANTITY END) * (CASE WHEN RATE IS NULL THEN 0 ELSE RATE END)) ) > ? ";
			pstmtSql = conn.prepareStatement(sql);
			pstmtSql.setString(1, tranId);
			pstmtSql.setDouble(2, val);
			rs = pstmtSql.executeQuery();
			if (rs.next())
			{
				cnt = rs.getLong("COUNT");
			}
			if (cnt > 0)
			{
				pstmtSql.close();
				pstmtSql = null;
				rs.close();
				rs = null;
				retString = itmDBAccessLocal.getErrorString("", "VTCONV", "","",conn);
				return retString;
			} else
			{
				pstmtSql.close();
				pstmtSql = null;
				rs.close();
				rs = null;
			}
			sql = "SELECT COUNT(*) AS COUNT FROM PORCPDET A, ITEM B WHERE A.ITEM_CODE = B.ITEM_CODE AND A.TRAN_ID = ? AND (CASE WHEN		   B.QC_REQD IS NULL THEN 'N' ELSE B.QC_REQD END) = 'Y' AND (CASE WHEN B.STK_OPT IS NULL THEN '0' ELSE B.STK_OPT END)		   <> '2' ";
			pstmtSql = conn.prepareStatement(sql);
			pstmtSql.setString(1, tranId);
			rs = pstmtSql.executeQuery();
			if (rs.next())
			{
				cnt = rs.getLong("COUNT");
			}
			if (cnt > 0)
			{
				pstmtSql.close();
				pstmtSql = null;
				rs.close();
				rs = null;
				retString = itmDBAccessLocal.getErrorString("", "VTSTKOPT", "","",conn);
				return retString;
			} else
			{
				pstmtSql.close();
				pstmtSql = null;
				rs.close();
				rs = null;
			}

			sql = "SELECT PURC_ORDER,LINE_NO__ORD, ITEM_CODE, SUM(QUANTITY) QUANTITY FROM PORCPDET " + " WHERE TRAN_ID = ? GROUP BY PURC_ORDER,LINE_NO__ORD, ITEM_CODE ";
			//pstmtSql = conn.prepareStatement(sql);
			//pstmtSql.setString(1, tranId);
			//rs = pstmtSql.executeQuery();
			pstmt2 = conn.prepareStatement(sql);//changed to pstmt2
			pstmt2.setString(1, tranId);
			rs = pstmt2.executeQuery();

			nullPo = distCommon.getDisparams("999999", "RCP_WO_PO", conn);

			if (purcOrder != null && nullPo != "Y")
			{
				while (rs.next())
				{
					purcOrder = checkNull(rs.getString("PURC_ORDER"));
					lineNoOrd = checkNull(rs.getString("LINE_NO__ORD"));
					itemCode = checkNull(rs.getString("ITEM_CODE"));
					qtyRcp = rs.getDouble("QUANTITY");

					lineNoOrd = "   " + lineNoOrd;
					lineNoOrd = lineNoOrd.substring(lineNoOrd.length() - 3);

					ResultSet rs1 = null;
					sql = "SELECT QUANTITY , (CASE WHEN DLV_QTY IS NULL THEN 0 ELSE DLV_QTY END) DLV_QTY FROM PORDDET WHERE PURC_ORDER = ? AND		   LINE_NO   = ? ";
					pstmtSql = conn.prepareStatement(sql);
					pstmtSql.setString(1, purcOrder);
					pstmtSql.setString(2, lineNoOrd);
					rs1 = pstmtSql.executeQuery();
					if (rs1.next())
					{
						ordQty = rs1.getDouble("QUANTITY");
						dlvQty = rs1.getDouble("DLV_QTY");
					}
					pstmtSql.close();
					pstmtSql = null;
					rs1.close();
					rs1 = null;

					totRcp = qtyRcp + dlvQty;

					if (totRcp > ordQty)
					{
						sql = "SELECT (CASE WHEN QTY_TOL_PERC IS NULL THEN 0 ELSE QTY_TOL_PERC END) QTY_TOL_PERC FROM ITEM WHERE ITEM_CODE = ? ";
						pstmtSql = conn.prepareStatement(sql);
						pstmtSql.setString(1, itemCode);
						rs1 = pstmtSql.executeQuery();
						if (rs1.next())
						{
							qtyTol = rs1.getDouble("QTY_TOL_PERC");
						}
						System.out.println("Qty Tol :- ["+qtyTol+"]");
						pstmtSql.close();
						pstmtSql = null;
						rs1.close();
						rs1 = null;

						System.out.println("(((totRcp - ordQty) / ordQty) * 100) :- ["+(((totRcp - ordQty) / ordQty) * 100)+"]");

						if ((((totRcp - ordQty) / ordQty) * 100) > qtyTol)
						{
							retString = itmDBAccessLocal.getErrorString("", "VTPOQTY2", "","",conn);
						}
					}
				}
				if (pstmtSql != null)
				{
					pstmtSql.close();
					pstmtSql = null;
				}
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
			}
			//Added by sarita on 15NOV2017 for open cursor issue [start]
			if(pstmt2 != null)
			{
				pstmt2.close();
				pstmt2 = null;
			}
			if (rs != null)
			{
				rs.close();
				rs = null;
			}
			//Added by sarita on 15NOV2017 for open cursor issue [end]

			if ((retString != null) && (retString.trim().length() > 0))
			{
				return retString;
			}

			sql = "SELECT COUNT(DISTINCT PURC_ORDER) AS COUNT FROM PORCPDET WHERE TRAN_ID = ? ";
			pstmtSql = conn.prepareStatement(sql);
			pstmtSql.setString(1, tranId);
			rs = pstmtSql.executeQuery();
			if (rs.next())
			{
				cnt = rs.getLong("COUNT");
			}
			pstmtSql.close();
			pstmtSql = null;
			rs.close();
			rs = null;
			if (cnt > 1)
			{
				sql = "SELECT COUNT(1) AS COUNT FROM PORDER WHERE PURC_ORDER IN (SELECT DISTINCT PURC_ORDER FROM PORCPDET " + " WHERE TRAN_ID = ?) AND POLICY_NO IS NOT NULL ";
				pstmtSql = conn.prepareStatement(sql);
				pstmtSql.setString(1, tranId);
				rs = pstmtSql.executeQuery();
				if (rs.next())
				{
					pcnt = rs.getLong("COUNT");
				}
				pstmtSql.close();
				pstmtSql = null;
				rs.close();
				rs = null;

				if (pcnt > 0)
				{
					sql = "SELECT DISTINCT PURC_ORDER FROM PORCPDET WHERE TRAN_ID = ? ";
					pstmtSql = conn.prepareStatement(sql);
					pstmtSql.setString(1, tranId);
					rs = pstmtSql.executeQuery();

					while (rs.next())
					{
						purcOrderDet = rs.getString("PURC_ORDER");

						ResultSet rs1 = null;
						sql = "SELECT POLICY_NO FROM PORDER WHERE PURC_ORDER = ? ";
						pstmtSql = conn.prepareStatement(sql);
						pstmtSql.setString(1, purcOrderDet);
						rs1 = pstmtSql.executeQuery();
						if (rs1.next())
						{
							policyNo = checkNull(rs1.getString("POLICY_NO"));
						}
						pstmtSql.close();
						pstmtSql = null;
						rs1.close();
						rs1 = null;
						if (policyNo == null)
						{
							policyNo = "";
						}
						if (pervPolicy == null || pervPolicy.trim().length() == 0)
						{
							pervPolicy = policyNo;
						} else if (pervPolicy.trim() != policyNo.trim())
						{
							retString = itmDBAccessLocal.getErrorString("", "VTPONOSAME", "","",conn);
						}
					}
					if (pstmtSql != null)
					{
						pstmtSql.close();
						pstmtSql = null;
					}
					if (rs != null)
					{
						rs.close();
						rs = null;
					}
				}
				if (retString != null && retString.trim().length() > 0)
				{
					return retString;
				}

			}

			sql = "SELECT EMP_CODE FROM USERS WHERE CODE = ? ";
			pstmtSql = conn.prepareStatement(sql);
			pstmtSql.setString(1, userId);
			rs = pstmtSql.executeQuery();
			if (rs.next())
			{
				empCode = checkNull(rs.getString("EMP_CODE"));
			}
			pstmtSql.close();
			pstmtSql = null;
			rs.close();
			rs = null;

			// Changed by wasim on 02-07-2015 to UPPER TRAN_WINDOW [START]
			// sql =
			// "SELECT (CASE WHEN LEDG_POST_CONF IS NULL THEN 'N' ELSE LEDG_POST_CONF END ) LEDG_POST_CONF FROM TRANSETUP WHERE LOWER(TRAN_WINDOW) = 'W_PORCP' ";
			sql = "SELECT (CASE WHEN LEDG_POST_CONF IS NULL THEN 'N' ELSE LEDG_POST_CONF END ) LEDG_POST_CONF FROM TRANSETUP WHERE UPPER(TRAN_WINDOW) = 'W_PORCP' ";
			// Changed by wasim on 02-07-2015 to UPPER TRAN_WINDOW [END]
			pstmtSql = conn.prepareStatement(sql);
			rs = pstmtSql.executeQuery();
			if (rs.next())
			{
				ledgPostConf = checkNull(rs.getString("LEDG_POST_CONF"));
			}
			pstmtSql.close();
			pstmtSql = null;
			rs.close();
			rs = null;
			// Changed by wasim on 02-07-2015 [START]
			System.out.println("LEDG_POST_CONF=" + ledgPostConf);
			// Changed by wasim on 02-07-2015 [END]

			if (ledgPostConf == null)
			{
				ledgPostConf = "N";
			}
			if (ledgPostConf.equalsIgnoreCase("Y") && !runMode.equalsIgnoreCase("B"))
			{
				today = new java.sql.Timestamp(System.currentTimeMillis());
				java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(genericUtility.getDBDateFormat());
				date = sdf.parse(today.toString());
				today = java.sql.Timestamp.valueOf(sdf.format(date).toString() + " 00:00:00.0");

				sql = "UPDATE PORCP SET EMP_CODE__APRV = ?, CHG_TERM = ?, TRAN_DATE = ? WHERE TRAN_ID = ?";
				pstmtUpd = conn.prepareStatement(sql);
				pstmtUpd.setString(1, empCode);
				pstmtUpd.setString(2, termId);
				pstmtUpd.setTimestamp(3, today);
				pstmtUpd.setString(4, tranId);
				updCnt = pstmtUpd.executeUpdate();
				if (updCnt != 1)
				{
					retString = itmDBAccessLocal.getErrorString("", "DS000NR", "","",conn);// Added
					// by
					// chandrashekar
					// on
					// 17-sep-2014
				}
				pstmtUpd.close();
				pstmtUpd = null;
			} else
			{
				sql = "UPDATE PORCP SET EMP_CODE__APRV = ?, CHG_TERM = ? WHERE TRAN_ID = ?";
				pstmtUpd = conn.prepareStatement(sql);
				pstmtUpd.setString(1, empCode);
				pstmtUpd.setString(2, termId);
				pstmtUpd.setString(3, tranId);
				updCnt = pstmtUpd.executeUpdate();
				if (updCnt != 1)
				{
					retString = itmDBAccessLocal.getErrorString("", "DS000NR", "","",conn);// Added
					// by
					// chandrashekar
					// on
					// 17-sep-2014
				}
				pstmtUpd.close();
				pstmtUpd = null;
			}

			// 26/10/11 manoharan create QC order
			qcCnt = 0;
			// not required for Taro to be done later
			if (detCnt > noStk)
			{
				//Changed by Jagruti Shinde Request id:[W16CSUN009]
				//retString = createQc(tranId, siteCode, conn);
				retString = createQc(tranId, siteCode, conn,xtraParams);
				if ((retString != null) && (retString.trim().length() > 0))
				{
					// break;
					return retString;
				}
				sql = "SELECT COUNT(1) AS COUNT FROM QC_ORDER WHERE PORCP_NO = ? AND STATUS = 'U' ";
				pstmtSql = conn.prepareStatement(sql);
				pstmtSql.setString(1, tranId);
				rs = pstmtSql.executeQuery();
				if (rs.next())
				{
					qcCnt = rs.getLong("COUNT");
				}
				if (pstmtSql != null)
				{
					pstmtSql.close();
					pstmtSql = null;
				}
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
			}

			// end 26/10/11 manoharan create QC order
			retString = confirm(tranId, 1, warning, conn, xtraParams);

			if ((retString != null) && (retString.trim().length() > 0))
			{
				conn.rollback();
				return retString;
			}
			// not required for Taro to be done later
			if (qcCnt == 0)
			{

				System.out.println("Enter @@@@@@@@@@@");
				pordType = pordType.trim();
				if (pordType.equalsIgnoreCase("Q") || pordType.equalsIgnoreCase("H"))
				{
					tranType = distCommon.getDisparams("999999", "ASSET_PORCP_TRAN_TYPE", conn);
					cwipTranType = distCommon.getDisparams("999999", "ASSET_PORCP_CWIP_TRAN_TYPE", conn);

					if (cwipTranType == null || cwipTranType.trim().length() == 0)
					{
						tranType = "";
					}
					if (tranType == null || tranType.trim().length() == 0)
					{
						tranType = "";
					}

					if (cwipTranType.trim().length() == 0 && tranType.trim().length() == 0)
					{
						retString = itmDBAccessLocal.getErrorString("", "VMRCPPARM", "");
					} else
					{
						System.out.println(" porcpTranType is " + porcpTranType.trim() + "tranType is = " + tranType.trim());
						if (!porcpTranType.trim().equalsIgnoreCase(tranType.trim()))
						{
							if (!porcpTranType.trim().equalsIgnoreCase(cwipTranType))
							{
								retString = itmDBAccessLocal.getErrorString("", "VTRCPTYPE1", "","",conn);

							}
						} else
						{
							retString = createAssetInstall(tranId, "", conn, xtraParams);
						}
					}
				}
				if (retString != null && retString.trim().length() > 0)
				{
					return retString;
				}
			}


			if (retString == null || retString.trim().length() == 0)
			{
				System.out.println(">>>>>>>>>>>>>>>Lock group for updation:" + lckGroup);
				confDate = new java.sql.Timestamp(System.currentTimeMillis());
				sql = "UPDATE PORCP SET CONFIRMED = 'Y',CONF_DATE = ?, LOCK_GROUP=? WHERE TRAN_ID = ? "; // add
				// LOCK_GROUP
				// by
				// Sagar
				// M.
				// 03/MAy/14
				pstmtUpd = conn.prepareStatement(sql);
				pstmtUpd.setTimestamp(1, confDate);
				pstmtUpd.setString(2, lckGroup);
				pstmtUpd.setString(3, tranId);
				updCnt = pstmtUpd.executeUpdate();
				pstmtUpd.close();
				pstmtUpd = null;
				//added by monika on 11 sept 2020 to update qty complete coloumnin projectestimation screen.

				//to find confirm po-receipt
				sql = "select confirmed from porcp where tran_id = ?";
				pstmt2 = conn.prepareStatement(sql);
				pstmt2.setString(1,tranId);
				rs = pstmt2.executeQuery();
				if( rs.next() )
				{	
					confirmPoReceipt = rs.getString("confirmed");
				}
				rs.close();
				rs = null;
				pstmt2.close(); 
				pstmt2 = null;
				System.out.println("poreceipt conffffirm"+confirmPoReceipt);

				//to find line_no__ord and purc_order from poreceipt det to bind in proj_est_bsl_item
				sql = "select purc_order,line_no__ord from porcpdet where tran_id = ? ";
				System.out.println("SQL::" + sql);
				pstmt6 = conn.prepareStatement(sql);
				pstmt6.setString(1, tranId);
				rs6 = pstmt6.executeQuery();
				if (rs6.next()) 
				{
					purcOrder = rs6.getString("purc_order") == null ? "" : rs6.getString("purc_order");
					lineNoPorcpdet = rs6.getString("line_no__ord") == null ? "" : rs6.getString("line_no__ord");

					//Added by monika salla to avoid space on 18 sept 2020
					lineNoPorcpdet = "   " + lineNoPorcpdet;
					lineNoPorcpdet = lineNoPorcpdet.substring(lineNoPorcpdet.length() - 3);//end
					System.out.println("@@lineNoPorddet in porder@@" + lineNoPorcpdet);
				}
				rs6.close();
				rs6 = null;
				pstmt6.close();
				pstmt6 = null;

				if("Y".equalsIgnoreCase(confirmPoReceipt.trim()))
				{
					sql = "select tran_id,line_no from proj_est_bsl_item where purc_order = ? and  line_no__ord = ?";

					pstmt2 = conn.prepareStatement(sql);
					pstmt2.setString(1, purcOrder);
					pstmt2.setString(2, lineNoPorcpdet);
					rs = pstmt2.executeQuery();
					if( rs.next() )
					{	
						tranIdProjEst = rs.getString("tran_id");
						lineNoProjEst= rs.getString("line_no");
					}
					System.out.println("project baseline item count "+tranIdProjEst+" line noproject estimation "+lineNoProjEst);
					rs.close();
					rs = null;
					pstmt2.close();
					pstmt2 = null;

					if(tranIdProjEst!=null && tranIdProjEst.trim().length()>0) 
					{
						//tot find total quantity
						sql = "select sum(quantity)from porcpdet where purc_order = ? and line_no__ord=?";
						pstmt2 = conn.prepareStatement(sql);
						pstmt2.setString(1, purcOrder);
						pstmt2.setString(2, lineNoPorcpdet);
						rs = pstmt2.executeQuery();
						if( rs.next() )
						{	
							//commented by monika salla on 7 sept -error while confrim poreceipt for cas
							//totquantity = rs.getDouble("quantity");
							totquantity = rs.getDouble(1);//end
						}
						rs.close();
						rs = null;
						pstmt2.close();
						pstmt2 = null;
						System.out.println("po receipt quantity--"+totquantity+" purc _order--"+purcOrder+"line_no--- "+lineNoPorcpdet);

						//update qty in proj_est_bsl_item
						sql = "update  proj_est_bsl_item set qty_complete=qty_complete+?  where  tran_id = ?  and purc_order=? and line_no__ord=?";
						pstmtUpd = conn.prepareStatement(sql);
						pstmtUpd.setDouble(1, totquantity);
						pstmtUpd.setString(2,  tranIdProjEst);
						pstmtUpd.setString(3,  purcOrder);
						pstmtUpd.setString(4,  lineNoPorcpdet);
						updCnt = pstmtUpd.executeUpdate();
						pstmtUpd.close(); pstmtUpd = null;
						System.out.println("updated records in proj_est_bsl_item 11: "+updCnt);
					}
				}

				//end--monika

				if (updCnt != 1)
				{
					retString = itmDBAccessLocal.getErrorString("", "VTPORCP2", "","",conn);
				}
			}

			System.out.println("RetString>>>>>>>>>>>>>>>>>>>" + retString);
			if (retString == null || retString.trim().length() == 0)
			{
				System.out.println("RetString>>>>>>>" + retString);
				InvAcct invAcct = new InvAcct();
				retString = invAcct.acctPoRcpt(tranId, conn);
				System.out.println("RetString1>>>>>>>" + retString);


				if(retString != null && "CREATE-VOUCHER".equals(retString.trim()))
				{
					/**
					 * VALLABH KADAM [03/JUL/15] Req Id:- [D15DSUN004] Before
					 * Check record exist in table 'PORD_PAY_TERM' for
					 * current purchase order and REL_AGNST = '05',06.
					 * */
					sql = "SELECT COUNT(*) AS CNT FROM PORD_PAY_TERM WHERE PURC_ORDER=? AND REL_AGNST IN ('05','06')";
					pstmt3 = conn.prepareStatement(sql);
					pstmt3.setString(1, purcOrder);
					rs3 = pstmt3.executeQuery();
					if (rs3.next())
					{
						cnt3 = rs3.getLong("CNT");
					}
					System.out.println("@V@ Line No count from pord_pay_term :- ["+cnt3+"]");
					if (cnt3 > 0)
					{							
						/**
						 * From table 'PORD_PAY_TERM'
						 * select all line_no
						 * for current purchase_order and Rel_agnst ='05','06'
						 * */
						sql = "SELECT LINE_NO FROM PORD_PAY_TERM WHERE PURC_ORDER=? AND REL_AGNST IN ('05','06')";
						pstmt4 = conn.prepareStatement(sql);
						pstmt4.setString(1, purcOrder);
						rs4 = pstmt4.executeQuery();
						while (rs4.next())
						{
							System.out.println("@V@ Line No :- ["+rs4.getString("LINE_NO")+"]");
							/**
							 * For every line_no of purchase order
							 * check task_status count
							 * from table 'PUR_MILSTN'
							 * */
							sql = "SELECT COUNT(*) AS CNT FROM PUR_MILSTN WHERE PURC_ORDER=? AND LINE_NO__ORD=? AND TASK_STATUS ='C'";
							pstmt5 = conn.prepareStatement(sql);
							pstmt5.setString(1, purcOrder);
							pstmt5.setString(2, rs4.getString("LINE_NO"));
							rs5 = pstmt5.executeQuery();
							if (rs5.next())
							{
								cnt4=rs5.getInt("CNT");
							}								
							pstmt5.close();
							pstmt5 = null;
							rs5.close();
							rs5 = null;
							if(cnt4<=0)
							{
								testFlag=true;
								break;
							}						
						}
						if (testFlag)
						{
							System.out.println("@V@ Mileston Are pending for purchase order :- [" + purcOrder + "]");
							voucher_error="DONOT-CREATE-VOUCHER";
							retString="";
						}

						pstmt4.close();
						pstmt4 = null;
						rs4.close();
						rs4 = null;
					}				
					pstmt3.close();
					pstmt3 = null;
					rs3.close();
					rs3 = null;

					/**
					 * VALLABH KADAM [03/JUL/15] Req Id:- [D15DSUN004]
					 * 
					 * END
					 * */
				}

				System.out.println("@V@ Before Voucher Error :- ["+voucher_error+"]");

				if (retString != null && "CREATE-VOUCHER".equals(retString.trim()) && !("DONOT-CREATE-VOUCHER".equalsIgnoreCase(voucher_error)))
				{
					CreatePoRcpVoucher createVouc = new CreatePoRcpVoucher();
					retString = createVouc.createPoRcpVoucher(tranId,
							xtraParams, conn);
					System.out.println("RetString2>>>>>>>" +retString );
					//Change by Manish on 28/04/16 [start]
					// modified by Rupesh Pawar [17/10/2017][Start]
					/*if (retString != null || "Success".equalsIgnoreCase(retString))
					{
						retString = " ";
					}*/
					/*if(retString == null || "Success".equalsIgnoreCase(retString) || retString.trim().length() == 0 )
					{
						retString = " ";
					}*/

					//modified by kunal [8/2/2018] start
					if(retString == null || retString.indexOf("Success") > -1 || retString.trim().length() == 0 )
					{
						retString = " ";
					}
					//modified by kunal [8/2/2018] end
					//modified by Rupesh Pawar [17/10/2017][End]
					//Change by Manish on 28/04/16 [End]
				} 
				else if (retString != null && retString.trim().length() > 0 
						&& (!("DONOT-CREATE-VOUCHER".equalsIgnoreCase(voucher_error))) && voucher_error.trim().length()==0)
				{
					//changed By Nasruddin start 21/11/16
					//etString = itmDBAccessLocal.getErrorString("", retString, "","",conn);
					System.out.println("RetString3>>>>>>>" + retString);
					//Added by sarita on 9JAN2018
					//if (retString.indexOf("ERROR") != -1 || retString.indexOf("error") != -1 )
					if(retString.toUpperCase().indexOf("ERROR") == -1)
					{
						retString = itmDBAccessLocal.getErrorString("", retString, "","",conn);
						System.out.println("RetString3>>>>>>>" + retString);
					}
					//changed By Nasruddin End 21/11/16
				}

				else if ((errcode == null || errcode.trim().length() == 0) && (err == null || err.trim().length() == 0) 
						&& (retString == null || retString.trim().length() == 0)
						&& (!("DONOT-CREATE-VOUCHER".equalsIgnoreCase(voucher_error)) && voucher_error.trim().length()==0))
				{
					System.out.println("Voucher Created1");
					sql = "select count(1)  AS count1 from pord_pay_term where  purc_order = ? and  rel_agnst = '02'";
					pstmtSql = conn.prepareStatement(sql);

					pstmtSql.setString(1, purcOrder);
					rs = pstmtSql.executeQuery();
					if (rs.next())
					{
						Cnt1 = rs.getLong("count1");
					}

					if (Cnt1 > 0)
					{
						CreatePoVoucherAdvance createVouc11 = new CreatePoVoucherAdvance();
						System.out.println("Voucher Created2");
						errcode = createVouc11.createPoVoucherAdv(tranId, xtraParams, conn, "PR", 0, today);
						// (String tranId,String xtraParams,Connection
						// conn,String as_flag,double ad_advperc,Date day)

					}
					rs.close();
					rs = null;
					pstmtSql.close();
					pstmtSql = null;

				}
				//Added by sarita on 8JAN2018 [start]
				//else if((retString != null && retString.trim().length() > 0) && (retString.indexOf("ERROR") != -1 || retString.indexOf("error") != -1))
				//Added and replace by sarita on 9JAN2018
				else if((retString != null && retString.trim().length() > 0) && (retString.toUpperCase().indexOf("ERROR") == -1))
				{
					retString = itmDBAccessLocal.getErrorString("", retString, "","",conn);
					System.out.println("RetString for porcp>>>>>>>" + retString);
				}
				//Added by sarita on 8JAN2018 [end]
			}
			/*
			 * // not required for Taro to be done later sql =
			 * "SELECT SALE_ORDER FROM PORDER A, PORCP B WHERE A.PURC_ORDER = B.PURC_ORDER AND B.TRAN_ID = ? "
			 * ; pstmtSql = conn.prepareStatement(sql); pstmtSql.setString(1,
			 * tranId); rs = pstmtSql.executeQuery(); if ( rs.next() ) {
			 * saleOrder = checkNull(rs.getString("SALE_ORDER")); } if( pstmtSql
			 * != null) { pstmtSql.close(); pstmtSql = null; } if( rs != null) {
			 * rs.close(); rs = null; } if ( detCnt > noStk) { if (
			 * saleOrder.trim().length() > 0 && retString.trim().length() == 0 )
			 * { retString = stockAllocatePur( tranId, "P", conn); } }
			 */
		} catch (Exception e)
		{
			try
			{
				conn.rollback();
			} catch (Exception e1)
			{
				//Added by Anjali R.  on[4/10/2018][Start]
				e1.printStackTrace();
				throw new ITMException(e1);
				//Added by Anjali R.  on[4/10/2018][End]
			}
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			throw new ITMException(e);
		} finally
		{
			lckGroup = "";
			System.out.println(">>>>>>>>>>>>>>>>>Finaly lckGroup vaalue:" + lckGroup);
			try
			{
				if (pstmtSql != null)
				{
					pstmtSql.close();
					pstmtSql = null;
				}
				if (pstmtUpd != null)
				{
					pstmtUpd.close();
					pstmtUpd = null;
				}
			} catch (Exception e)
			{
				System.out.println(e.getMessage());
				e.printStackTrace();//Modified by Anjali R. on[25/10/2018]
				throw new ITMException(e);
			}
		}
		System.out.println("@V@ Returning Result ::" + retString);
		return retString;
	}

	private String confirm(String tranId, int commit, String warning, Connection conn, String xtraParams) throws RemoteException, ITMException
	{

		double lcholdQty = 0;
		String prdCode = "";
		String siteCode = "";
		String varValue = "";
		String varName = "";
		PreparedStatement pstmt = null, pstmt1 = null, pstmt2 = null, pstmt3 = null;
		PreparedStatement pstmtUpd = null;
		String retString = "";
		PreparedStatement pstmtItemLotPack = null;
		ResultSet rs = null, rs1 = null, rs2 = null, rs3 = null;
		ResultSet rsItemLotPack = null;
		String sql = "";
		java.sql.Timestamp tranDate = null;
		String siteRcp = "", tranSer = "", suppCode = "", currCode = "", invacctRate = "", errString = "";
		String bondTaxGroup = "", bondTaxArray[], EOU = "", channelPartner = "", disLink = "";

		double exchRate = 0, quantity = 0, discount = 0, rate = 0, netAmount = 0, taxAmount = 0, stdQuantity = 0, batchSize = 0;
		double potencyPerc = 0, grossWeight = 0, tareWeight = 0, netWeight = 0, convQtyStdUom = 0, exShtQty = 0;
		double noArt = 0, receiptQty = 0, stdRate = 0;

		String lineNo = "0", itemCode = "", purcOrder = "", unit = "", locCode = "", stdUom = "", pordLine = "", cancelBo = "";
		String exShtLoc = "", formNo = "", effectStock = "", mfgItemCode = "", invStat = "", itemSer = "", shelfLifeType = "";
		String lotNo = "", lotSl = "", acctCodeCR = "", acctCodeDR = "", cctCodeCR = "", cctCodeDR = "", packCode = "";
		String suppCodeMnfr = "", siteCodeMfg = "", grade = "", batchNo = "", dutyPaid = "", uomRound = "";
		java.sql.Timestamp mfgDate = null, retestDate = null, expiryDate = null;

		double ordQuantity = 0, dlvQuantity = 0, batchSizeApprv = 0, effRate = 0, grossRate = 0;
		double shipperSize = 0d, grossWt = 0d, netWt = 0d, tareWt = 0d;

		String ordStatus = "", siteCodeDet = "", xmlValues = "", keyString = "", remarks = "";
		String cctrCodeInv = "", acctCodeInv = "";
		int lineNoInv = 0;
		int count1 = 0, count2 = 0, count3 = 0;
		java.sql.Timestamp chgDate = null;
		FinCommon finCommon = null;
		HashMap stkUpdMap = null;
		String formStatus = "", bondNo = "";
		java.util.Date date = null;
		// CommonConstants commonConstants = CommonConstants.getInstance();
		int formLineNo = 0, updCnt = 0, count = 0;
		double ct3Quantity = 0, qtyUsed = 0, bondTaxAmount = 0, bondValue = 0, bankGuarantee = 0, exShtQtystd = 0;

		ArrayList suppLockList = new ArrayList(), qtyAr = null;
		ArrayList itemLockList = null, tempList = null;
		HashMap lockCodeWiseMap = new HashMap(), tempMap = null;
		String lockGroup = "", lockCode = "", tempLockGroup = "", tempLockCode = "", qcReqd = "N", tempTranId = "";
		String locCodeGit = "";
		String stockOption = "", qcReq = ""; //Added By Priyanka on 16MAY2019
		String gitUpdate = "N";// 10-oct-2019 manoharan to update GIT stock based on channel_partner flag in header
		//Manish Mhatre 16oct19 start [to de-allocate free qty customer stock]
		String custCodeEnd = "", sorderNo = "", tranIdSordAlloc = "", dimension;
		PreparedStatement pstmtHdr = null, pstmtDet = null;		
		InvAllocTraceBean invAllocTrace = null;
		HashMap strAllocate = null;
		//Manish Mhatre 16oct19 end [to de-allocate free qty customer stock]
		Timestamp dlvDate = null;
		try
		{
			//Manish Mhatre 16oct19 start [to de-allocate free qty customer stock]
			SimpleDateFormat sdfApp = new SimpleDateFormat(genericUtility.getApplDateFormat());
			ArrayList<HashMap<String, String>> sordAllocStkList = new ArrayList<HashMap<String, String>>();
			HashMap<String, String>  sordAllocStk = null;
			//Manish Mhatre 16oct19 end [to de-allocate free qty customer stock]
			DistCommon distCommon = new DistCommon();
			ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();			
			InvDemSuppTraceBean invDemSupTrcBean = new InvDemSuppTraceBean();
			HashMap demandSupplyMap = new HashMap();		  
			//sql = "Select tran_date, site_code,tran_ser,supp_code,post_type	, curr_code , exch_rate, channel_partner  From porcp Where tran_id = ? ";
			sql = "Select tran_date, site_code,tran_ser,supp_code,post_type	, curr_code , exch_rate, channel_partner From porcp Where tran_id = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				tranDate = rs.getTimestamp("tran_date");
				siteRcp = rs.getString("site_code");
				tranSer = rs.getString("tran_ser");
				suppCode = rs.getString("supp_code");
				invacctRate = rs.getString("post_type");
				currCode = rs.getString("post_type");
				exchRate = rs.getDouble("exch_rate");
				gitUpdate = rs.getString("channel_partner");				
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;
			// 10-oct-2019 manoharan to update GIT stock based on channel_partner flag in header
			System.out.println("confirm porcp....gitUpdate before["+gitUpdate + "]");

			if (gitUpdate == null || gitUpdate.trim().length() == 0)
			{
				gitUpdate = "N";
			}
			//Manish Mhatre 16oct19 start [to de-allocate free qty customer stock]
			String userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			String chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");
			String loginEmp = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			sql = "INSERT INTO SORD_ALLOC (TRAN_ID,TRAN_DATE,CUST_CODE,SITE_CODE,CHG_DATE,CHG_TERM,CHG_USER,ADD_DATE,ADD_TERM,ADD_USER,EMP_CODE__APRV,"
					+ " CONFIRMED,CONF_DATE,ACTIVE_PICK_ALLOW,SITE_CODE__SHIP,ALLOC_SOURCE,ALLOC_FLAG,EDI_STAT) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			pstmtHdr = conn.prepareStatement(sql);
			sql = "Insert into SORD_ALLOC_DET (TRAN_ID,LINE_NO,ITEM_CODE,LOC_CODE,LOT_NO,LOT_SL,QUANTITY,DEALLOC_QTY,SITE_CODE,PENDING_QTY)values (?,?,?,?,?,?,?,?,?,?)";
			pstmtDet = conn.prepareStatement(sql);
			/*						
			if ("Y".equalsIgnoreCase(gitUpdate) && (custCodeEnd != null && custCodeEnd.trim().length() > 0))
			{
				//tranIdSordAlloc = generateTranId("w_sord_alloc", siteCode, sdfApp.format(tranDate), conn);													
				BaseLogger.log("2", null, null, "SordAlloc Tran ID:["+tranIdSordAlloc+"] custCodeEnd["+custCodeEnd+"]sorderNo["+sorderNo+"]");
				if(custCodeEnd != null && custCodeEnd.trim().length() > 0)
				{
					tranIdSordAlloc = generateTranId("w_sord_alloc", siteCode, sdfApp.format(tranDate), conn);
					//String chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
					String userId =(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode"));
					String chgTerm =(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId"));

					sql = "INSERT INTO SORD_ALLOC (TRAN_ID,TRAN_DATE,CUST_CODE,SITE_CODE,CHG_DATE,CHG_TERM,CHG_USER,ADD_DATE,ADD_TERM,ADD_USER,EMP_CODE__APRV,"
							+ " CONFIRMED,CONF_DATE,ACTIVE_PICK_ALLOW,SITE_CODE__SHIP,ALLOC_SOURCE,ALLOC_FLAG,EDI_STAT) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
					pstmtHdr = conn.prepareStatement(sql);
					pstmtHdr.setString(1, tranIdSordAlloc);
					pstmtHdr.setTimestamp(2, tranDate);
					pstmtHdr.setString(3, custCodeEnd);
					pstmtHdr.setString(4, siteRcp);
					pstmtHdr.setTimestamp(5, tranDate);						
					pstmtHdr.setString(6, chgTerm);
					pstmtHdr.setString(7, userId);
					pstmtHdr.setTimestamp(8, tranDate);						
					pstmtHdr.setString(9, chgTerm);
					pstmtHdr.setString(10, userId);						
					pstmtHdr.setString(11, userId);
					pstmtHdr.setString(12, "Y");
					pstmtHdr.setTimestamp(13, tranDate);						
					pstmtHdr.setString(14, "Y");
					pstmtHdr.setString(15, siteRcp);						
					pstmtHdr.setString(16, "M");
					pstmtHdr.setString(17, "A");
					pstmtHdr.setString(18, "N");
					pstmtHdr.executeUpdate();
					pstmtHdr.clearParameters();
					pstmtHdr.close();
					pstmtHdr = null;					
				}								
			}//end if
			//Manish Mhatre 16oct19 end [to de-allocate free qty customer stock]
			 */			System.out.println("confirm porcp....gitUpdate after["+gitUpdate + "]");
			 sql = "Select (case when eou is null then 'N' else eou end) eou From site Where site_code = ?";
			 pstmt = conn.prepareStatement(sql);
			 pstmt.setString(1, siteRcp);
			 rs = pstmt.executeQuery();
			 if (rs.next())
			 {
				 EOU = rs.getString("eou");
			 }
			 pstmt.close();
			 pstmt = null;
			 rs.close();
			 rs = null;

			 uomRound = distCommon.getDisparams("999999", "UOM_ROUND", conn);
			 if ("NULLFOUND".equals(uomRound))
			 {
				 errString = itmDBAccessLocal.getErrorString("", "VTUOMVARPARM", "","",conn);
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

			 }
			 sql = "Select channel_partner, dis_link From site_supplier " + " Where  site_code = ? " + " And supp_code = ? ";
			 pstmt = conn.prepareStatement(sql);
			 pstmt.setString(1, siteRcp);
			 pstmt.setString(2, suppCode);
			 rs = pstmt.executeQuery();
			 if (rs.next())
			 {
				 channelPartner = rs.getString("channel_partner");
				 disLink = rs.getString("dis_link");
				 pstmt.close();
				 pstmt = null;
				 rs.close();
				 rs = null;
			 } else
			 {
				 pstmt.close();
				 pstmt = null;
				 rs.close();
				 rs = null;
				 sql = "Select channel_partner, dis_link From supplier " + " Where  supp_code = ? ";
				 pstmt = conn.prepareStatement(sql);
				 pstmt.setString(1, suppCode);
				 rs = pstmt.executeQuery();
				 if (rs.next())
				 {
					 channelPartner = rs.getString("channel_partner");
					 disLink = rs.getString("dis_link");
				 }
				 pstmt.close();
				 pstmt = null;
				 rs.close();
				 rs = null;
			 }

			 // 1. get the lock group of supplier
			 // 2. get the lock_code under all the lock_group and store them in a
			 // list
			 // 3. At the detail level check supplieritem for lock_group if
			 // specified
			 // add the item to all the lock_code in the lock_group
			 // 4. If the above is not is not specified add the item to all
			 // lock_code for supplier
			 // ///////////////////////////////////////////////

			 // populate the supplier lock list

			 /*
			  * System.out.println(
			  * ">>>>>>>>>>>>>>>>>>>Before add to lckGroup in supplier:"
			  * +lckGroup); //comment added by sagar on 24/07/14 sql =
			  * "select lock_group From supplier " + " where  supp_code = ? " ;
			  * pstmt = conn.prepareStatement(sql); pstmt.setString(1, suppCode);
			  * rs = pstmt.executeQuery(); if ( rs.next() ) { lockGroup =
			  * rs.getString("lock_group");
			  * System.out.println(">>>>>>>>lockGroup in supplier:"+lockGroup); }
			  * pstmt.close(); pstmt = null; rs.close(); rs = null; if (lockGroup
			  * != null && lockGroup.trim().length() > 0 ) { sql =
			  * "select lock_code from lock_group " + " where  lock_group = ? " ;
			  * pstmt = conn.prepareStatement(sql); pstmt.setString(1,
			  * lockGroup); rs = pstmt.executeQuery(); while ( rs.next() ) {
			  * lockCode = rs.getString("lock_code"); suppLockList.add(lockCode);
			  * } pstmt.close(); pstmt = null; rs.close(); rs = null; }
			  */
			 //Manish Mhatre 16oct19 start [to de-allocate free qty customer stock]
			 //int lineNoDet = 0; 
			 HashMap<String, HashMap> custLineItem = new HashMap<String, HashMap>();
			 //Manish Mhatre 16oct19 end [to de-allocate free qty customer stock]
			 // ///////////////////////////////////////////////
			 sql = "SELECT SHIPPER_SIZE, GROSS_WEIGHT, NET_WEIGHT FROM ITEM_LOT_PACKSIZE WHERE ITEM_CODE = ? AND LOT_NO__FROM <= ? AND LOT_NO__TO >= ? ";
			 pstmtItemLotPack = conn.prepareStatement(sql);

			 //sql = "select line_no, item_code, purc_order, quantity, " + " unit, rate__stduom, discount, tax_amt, net_amt, " + " loc_code, line_no__ord, canc_bo, lot_no, lot_sl, " + " unit__std, quantity__stduom, acct_code__cr, acct_code__dr, cctr_code__cr, cctr_code__dr, " + " mfg_date, pack_code, potency_perc, expiry_date, " + " gross_weight, tare_weight, net_weight, supp_code__mnfr, " + " site_code__mfg, grade, conv__qty_stduom, " + " (case when excess_short_qty is null then 0 else excess_short_qty end) as excess_short_qty, " + " loc_code__excess_short	, (case when realised_qty is null then 0 else realised_qty end) as realised_qty, " + " batch_no, no_art, item_code__mfg, std_rate, effect_stock, form_no, retest_date, " + " duty_paid, batch_size, shelf_life__type " + " from porcpdet Where tran_id = ? ";

			 //Commented by Anagha R on 17/09/2020 for DIMENSION to be  captured and updated in stock, a new format to be defined to accept length, width and height
			 /*sql = "select line_no, item_code, purc_order, quantity, " + " unit, rate__stduom, discount, tax_amt, net_amt, " + " loc_code, line_no__ord, canc_bo, lot_no, lot_sl, " + " unit__std, quantity__stduom, acct_code__cr, acct_code__dr, cctr_code__cr, cctr_code__dr, " + " mfg_date, pack_code, potency_perc, expiry_date, " + " gross_weight, tare_weight, net_weight, supp_code__mnfr, " + " site_code__mfg, grade, conv__qty_stduom, " + " (case when excess_short_qty is null then 0 else excess_short_qty end) as excess_short_qty, " + " loc_code__excess_short	, (case when realised_qty is null then 0 else realised_qty end) as realised_qty, " + " batch_no, no_art, item_code__mfg, std_rate, effect_stock, form_no, retest_date, " + " duty_paid, batch_size, shelf_life__type, cust_code__end " + " from porcpdet Where tran_id = ? ";*/

			 //Changed by Anagha R on 17/09/2020 for DIMENSION to be  captured and updated in stock, a new format to be defined to accept length, width and height
			 sql = "select line_no, item_code, purc_order, quantity, " + " unit, rate__stduom, discount, tax_amt, net_amt, " + " loc_code, line_no__ord, canc_bo, lot_no, lot_sl, " + " unit__std, quantity__stduom, acct_code__cr, acct_code__dr, cctr_code__cr, cctr_code__dr, " + " mfg_date, pack_code, potency_perc, expiry_date, " + " gross_weight, tare_weight, net_weight, supp_code__mnfr, " + " site_code__mfg, grade, conv__qty_stduom, " + " (case when excess_short_qty is null then 0 else excess_short_qty end) as excess_short_qty, " + " loc_code__excess_short	, (case when realised_qty is null then 0 else realised_qty end) as realised_qty, " + " batch_no, no_art, item_code__mfg, std_rate, effect_stock, form_no, retest_date, " + " duty_paid, batch_size, shelf_life__type, cust_code__end, dimension " + " from porcpdet Where tran_id = ? ";

			 pstmt = conn.prepareStatement(sql);
			 pstmt.setString(1, tranId);

			 rs = pstmt.executeQuery();

			 while (rs.next())
			 {
				 lineNo = rs.getString("line_no");
				 itemCode = rs.getString("item_code");
				 purcOrder = rs.getString("purc_order");
				 quantity = rs.getDouble("quantity");
				 unit = rs.getString("unit");
				 rate = rs.getDouble("rate__stduom");
				 discount = rs.getDouble("discount");
				 taxAmount = rs.getDouble("tax_amt");
				 netAmount = rs.getDouble("net_amt");
				 locCode = rs.getString("loc_code");
				 pordLine = rs.getString("line_no__ord");
				 cancelBo = rs.getString("canc_bo");
				 lotNo = rs.getString("lot_no");
				 lotSl = rs.getString("lot_sl");
				 stdUom = rs.getString("unit__std");
				 stdQuantity = rs.getDouble("quantity__stduom");
				 acctCodeCR = rs.getString("acct_code__cr");
				 acctCodeDR = rs.getString("acct_code__dr");
				 cctCodeCR = rs.getString("cctr_code__cr");
				 cctCodeDR = rs.getString("cctr_code__dr");
				 mfgDate = rs.getTimestamp("mfg_date");
				 packCode = rs.getString("pack_code");
				 potencyPerc = rs.getDouble("potency_perc");
				 expiryDate = rs.getTimestamp("expiry_date");
				 grossWeight = rs.getDouble("gross_weight");
				 tareWeight = rs.getDouble("tare_weight");
				 netWeight = rs.getDouble("net_weight");
				 suppCodeMnfr = rs.getString("supp_code__mnfr");
				 siteCodeMfg = rs.getString("site_code__mfg");
				 grade = rs.getString("grade");
				 convQtyStdUom = rs.getDouble("conv__qty_stduom");
				 exShtQty = rs.getDouble("excess_short_qty");
				 exShtLoc = rs.getString("loc_code__excess_short");
				 receiptQty = rs.getDouble("realised_qty");
				 batchNo = rs.getString("batch_no");
				 noArt = rs.getDouble("no_art");
				 custCodeEnd = checkNull(rs.getString("cust_code__end")); 
				 dimension = rs.getString("dimension"); //Added by Anagha R on 17/09/2020 for DIMENSION to be  captured and updated in stock, a new format to be defined to accept length, width and height
				 if (noArt == 0)
				 {
					 pstmtItemLotPack.setString(1, itemCode);
					 pstmtItemLotPack.setString(2, lotNo);
					 pstmtItemLotPack.setString(3, lotNo);
					 rsItemLotPack = pstmtItemLotPack.executeQuery();
					 if (rsItemLotPack.next())
					 {
						 shipperSize = rsItemLotPack.getDouble("SHIPPER_SIZE");
						 grossWeight = rsItemLotPack.getDouble("GROSS_WEIGHT");
						 netWeight = rsItemLotPack.getDouble("NET_WEIGHT");
						 tareWeight = grossWeight - netWeight;
						 if (shipperSize > 0)
						 {
							 noArt = (int) (quantity / shipperSize);
						 }
					 }
					 rsItemLotPack.close();
					 rsItemLotPack = null;					
					 pstmtItemLotPack.clearParameters();
					 //Commented by Varsha V on 31-05-18 because wrote in wrong place as it is made out of while loop
					 //Added by sarita on 15NOV2017 for open cursor issue[start]
					 //pstmtItemLotPack.close();
					 //pstmtItemLotPack = null;
					 //Added by sarita on 15NOV2017 for open cursor issue[end]
					 //Ended Comment by Varsha V on 31-05-18 because wrote in wrong place as it is made out of while loop
				 }
				 mfgItemCode = rs.getString("item_code__mfg");
				 stdRate = rs.getDouble("std_rate");
				 // 06/11/13 manoharan as per PB code assigned receipt rate in
				 // case std_rate is not available
				 if (stdRate == -1)
				 {
					 stdRate = rate;
				 }
				 // end 06/11/13 manoharan as per PB code assigned receipt rate
				 // in case std_rate is not available
				 effectStock = rs.getString("effect_stock");
				 formNo = rs.getString("form_no");
				 retestDate = rs.getTimestamp("retest_date");
				 dutyPaid = rs.getString("duty_paid");
				 batchSize = rs.getDouble("batch_size");
				 shelfLifeType = rs.getString("shelf_life__type");
				 // qcReqd = rs.getString("qc_reqd");//Gulzar this column is not
				 // present in porcpdet
				 // /////////////////////////////////////////////////////////////////////////////
				 // maintain the lock list in the map as per supplier or
				 // supplieritem definition
				 // suppLockList
				 // itemLockList
				 // lockCodeWiseMap
				 tempLockGroup = null;

				 /*
				  * sql = "select lock_group " + " from supplieritem " +
				  * " Where supp_code = ? And  item_code = ?"; pstmt1 =
				  * conn.prepareStatement(sql); pstmt1.setString(1, suppCode);
				  * pstmt1.setString(2, itemCode); rs1 = pstmt1.executeQuery();
				  * if ( rs1.next() ) { tempLockGroup =
				  * rs1.getString("lock_group");
				  * System.out.println(">>>>>>>>>>Lock Group in supplieritem:"
				  * +tempLockGroup); } pstmt1.close(); pstmt1 = null;
				  * rs1.close(); rs1 = null; //Added by Sagar M. on 06/05/14
				  * start if(tempLockGroup != null &&
				  * tempLockGroup.trim().length() > 0) { lckGroup=tempLockGroup;
				  * System
				  * .out.println(">>>>>>>>Add to lckGroup for supplieritem:"
				  * +lckGroup); } else if(lockGroup != null &&
				  * lockGroup.trim().length() > 0) { lckGroup=lockGroup;
				  * System.out
				  * .println(">>>>>>>>Add to lckGroup for supplier:"+lckGroup); }
				  * //Added by Sagar M. on 06/05/14 end
				  */// comment by sagar on 24/07/14
				 // added by sagar on 24/07/14

				 //Added By PriyankaC on 16May2019.[Start]
				 System.out .println("Value of lotNo in Detail: "+lotNo );
				 if(lotNo == null || lotNo.trim().length() == 0)
				 {
					 sql = "select stk_opt, qc_reqd from item where item_code = ? ";
					 pstmt1 = conn.prepareStatement(sql);
					 pstmt1.setString(1, itemCode);
					 rs1 = pstmt1.executeQuery();
					 if (rs1.next())
					 {

						 stockOption = rs1.getString("stk_opt");
						 qcReq = rs1.getString("qc_reqd");
					 }
					 System.out .println("Stock and Qc value : "+stockOption +" : "+qcReq);
					 if("2".equalsIgnoreCase(stockOption) && "N".equalsIgnoreCase(qcReq)  )
					 {
						 System.out .println("Stock and Qc value in : "+stockOption);
						 errString = itmDBAccessLocal.getErrorString("", "VMTOLOTNO", "","",conn);
						 break;
					 }
					 pstmt1.close();
					 pstmt1 = null;
					 rs1.close();
					 rs1 = null;
				 }
				 //Added By PriyankaC on 16MAY2019 [END]
				 lockGroup = "";
				 sql = "select lock_group from itemmnfr where item_code= ? ";
				 pstmt1 = conn.prepareStatement(sql);
				 pstmt1.setString(1, itemCode);
				 rs1 = pstmt1.executeQuery();
				 if (rs1.next())
				 {
					 lockGroup = rs1.getString("lock_group");
				 }
				 pstmt1.close();
				 pstmt1 = null;
				 rs1.close();
				 rs1 = null;
				 if (lockGroup == null || lockGroup.trim().length() == 0)
				 {
					 sql = "select lock_group from site_supplier where site_code= ? and supp_code= ? ";
					 pstmt1 = conn.prepareStatement(sql);
					 pstmt1.setString(1, siteRcp);
					 pstmt1.setString(2, suppCode);
					 rs1 = pstmt1.executeQuery();
					 if (rs1.next())
					 {
						 lockGroup = rs1.getString("lock_group");
					 }
					 pstmt1.close();
					 pstmt1 = null;
					 rs1.close();
					 rs1 = null;
					 if (lockGroup == null || lockGroup.trim().length() == 0)
					 {
						 sql = "select lock_group from supplier where supp_code= ?";
						 pstmt1 = conn.prepareStatement(sql);
						 pstmt1.setString(1, suppCode);
						 rs1 = pstmt1.executeQuery();
						 if (rs1.next())
						 {
							 lockGroup = rs1.getString("lock_group");
						 }
						 pstmt1.close();
						 pstmt1 = null;
						 rs1.close();
						 rs1 = null;
						 if (lockGroup == null || lockGroup.trim().length() == 0)
						 {
							 sql = "select lock_group from siteitem where site_code= ? and item_code= ?";
							 pstmt1 = conn.prepareStatement(sql);
							 pstmt1.setString(1, siteRcp);
							 pstmt1.setString(2, itemCode);
							 rs1 = pstmt1.executeQuery();
							 if (rs1.next())
							 {
								 lockGroup = rs1.getString("lock_group");
							 }
							 pstmt1.close();
							 pstmt1 = null;
							 rs1.close();
							 rs1 = null;
						 }
					 }
				 }
				 itemLockList = null;
				 System.out.println(">>>>>>>>>>>lockGroup:" + lockGroup);
				 if (lockGroup != null && lockGroup.trim().length() > 0)
				 {
					 itemLockList = new ArrayList();
					 sql = "select lock_code from lock_group " + " where  lock_group = ? ";
					 pstmt1 = conn.prepareStatement(sql);
					 pstmt1.setString(1, lockGroup);
					 rs1 = pstmt1.executeQuery();
					 while (rs1.next())
					 {
						 lockCode = rs1.getString("lock_code");
						 System.out.println(">>>>>>>>>>>>>found lockCode:" + lockCode);
						 itemLockList.add(lockCode);
					 }
					 pstmt1.close();
					 pstmt1 = null;
					 rs1.close();
					 rs1 = null;
				 }
				 /*
				  * else if(suppLockList.size() > 0) { itemLockList =
				  * suppLockList; }
				  */// comment added by sagar on 24/07/14
				 if (itemLockList != null && itemLockList.size() > 0)
				 {
					 for (int ctr = 0; ctr < itemLockList.size(); ctr++)
					 {
						 tempLockCode = (String) itemLockList.get(ctr);
						 // find whether the lockCode is already in the HashMap
						 if (lockCodeWiseMap.containsKey(tempLockCode))
						 {
							 tempList = (ArrayList) lockCodeWiseMap.get(tempLockCode);
						 } else
						 {
							 tempList = new ArrayList();
						 }
						 // populate the map with the stock keys
						 tempMap = new HashMap();

						 tempMap.put("site_code", siteRcp);
						 tempMap.put("item_code", itemCode);
						 tempMap.put("loc_code", locCode);
						 tempMap.put("lot_no", lotNo);
						 tempMap.put("lot_sl", lotSl);

						 tempList.add(tempMap);

						 if (lockCodeWiseMap.containsKey(tempLockCode))
						 {
							 lockCodeWiseMap.put(tempLockCode, tempList);
						 } else
						 {
							 lockCodeWiseMap.put(tempLockCode, tempList);
						 }
					 }
				 }
				 // update lock_group in porcpdet table, added by sagar on
				 // 25/07/14
				 if (lockGroup != null && lockGroup.trim().length() > 0)
				 {
					 System.out.println(">>>>>>>>Updating Porcpdet table>>>>>>>>>>>>>>>>>");
					 sql = "update porcpdet set lock_group= ? where tran_id = ? and line_no = ? ";
					 pstmt1 = conn.prepareStatement(sql);
					 pstmt1.setString(1, lockGroup);
					 pstmt1.setString(2, tranId);
					 pstmt1.setString(3, lineNo);
					 updCnt = pstmt1.executeUpdate();

					 pstmt1.close();
					 pstmt1 = null;
				 }
				 // /////////////////////////////////////////////////////////////////////////////
				 if (purcOrder != null)
				 {
					 sql = "select quantity, dlv_qty, status, site_code, dlv_date " + " from porddet " + " Where Purc_order = ? And  line_no    = ?";
					 pstmt1 = conn.prepareStatement(sql);
					 pstmt1.setString(1, purcOrder);
					 pstmt1.setString(2, pordLine);
					 rs1 = pstmt1.executeQuery();
					 if (rs1.next())
					 {
						 ordQuantity = rs1.getDouble("quantity");
						 dlvQuantity = rs1.getDouble("dlv_qty");
						 ordStatus = rs1.getString("status");
						 siteCodeDet = rs1.getString("site_code");
						 /**Modified by Pavan Rane 24dec19 [fetching dlv_date to update demand/supply in summary table(RunMRP process) related changes]*/
						 dlvDate = rs1.getTimestamp("dlv_date"); 
					 }
					 pstmt1.close();
					 pstmt1 = null;
					 rs1.close();
					 rs1 = null;


				 }
				 if (effectStock == null)
				 {
					 effectStock = "Y";
				 }
				 if ("Y".equals(effectStock))
				 {
					 if ("N".equals(qcReqd))
					 {
						 if (batchSize > 0)
						 {
							 sql = "select batch_size from batchsize_aprv" + " where item_code	= ? " + " and site_code__mfg	= ? " + " and eff_from <= ? " + " and valid_upto >= ? " + " and confirmed = 'Y' ";
							 pstmt1 = conn.prepareStatement(sql);
							 pstmt1.setString(1, itemCode);
							 pstmt1.setString(2, siteRcp);
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
								 } else
								 {
									 pstmt1.close();
									 pstmt1 = null;
									 rs1.close();
									 rs1 = null;
									 sql = "select key_string from transetup " + " where upper(tran_window) = 'GENERAL'";
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
								 xmlValues = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
								 xmlValues = xmlValues + "<Header></Header>\r\n";
								 xmlValues = xmlValues + "<Detail1>\r\n";
								 xmlValues = xmlValues + "<tran_id></tran_id>\r\n";
								 xmlValues = xmlValues + "<site_code>" + siteRcp + "</site_code>\r\n";
								 xmlValues = xmlValues + "<tran_date>" + getCurrdateAppFormat() + "</tran_date>\r\n";
								 xmlValues = xmlValues + "</Detail1>\r\n</Root>";
								 System.out.println("xmlValues  :[" + xmlValues + "]");
								 TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", CommonConstants.DB_NAME);
								 tempTranId = tg.generateTranSeqID("INVHOL", "tran_id", keyString, conn);

								 System.out.println("tempTranId [" + tempTranId + "]");

								 sql = "insert into inv_hold (tran_id, tran_date, site_code, remarks,confirmed, chg_user, chg_date, chg_term) " + " values	(?,?,?,?, 'N',?,?,?) ";

								 remarks = "Auto generated from Purchase Receipt	:" + tranId;
								 chgDate = new java.sql.Timestamp(System.currentTimeMillis());
								 java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(genericUtility.getDBDateFormat());
								 date = sdf.parse(chgDate.toString());
								 chgDate = java.sql.Timestamp.valueOf(sdf.format(date).toString() + " 00:00:00.0");

								 pstmtUpd = conn.prepareStatement(sql);
								 pstmtUpd.setString(1, tempTranId);
								 pstmtUpd.setTimestamp(2, chgDate);
								 chgDate = new java.sql.Timestamp(System.currentTimeMillis());
								 pstmtUpd.setString(3, siteRcp);
								 pstmtUpd.setString(4, remarks);
								 pstmtUpd.setString(5, this.userId);
								 pstmtUpd.setTimestamp(6, chgDate);
								 pstmtUpd.setString(7, this.termId);

								 updCnt = pstmtUpd.executeUpdate();
								 pstmtUpd.close();
								 pstmtUpd = null;

								 lineNoInv++;

								 sql = "insert into inv_hold_det (tran_id, line_no, item_code, site_code,loc_code, lot_no, lot_sl, remarks,	hold_status) " + " values	(?, ?, ?,?,?,?,?,?,	'H') ";

								 pstmtUpd = conn.prepareStatement(sql);
								 pstmtUpd.setString(1, tempTranId);
								 pstmtUpd.setInt(2, lineNoInv);
								 pstmtUpd.setString(3, itemCode);
								 pstmtUpd.setString(4, siteRcp);
								 pstmtUpd.setString(5, locCode);
								 pstmtUpd.setString(6, lotNo);
								 pstmtUpd.setString(7, lotSl);
								 pstmtUpd.setString(8, remarks);

								 updCnt = pstmtUpd.executeUpdate();
								 pstmtUpd.close();
								 pstmtUpd = null;
							 } // batchSize > approved batch size
						 } // batchSize > 0
					 } // qc_reqd == N
					 sql = "select inv_stat from location where loc_code = ? ";
					 pstmt1 = conn.prepareStatement(sql);
					 pstmt1.setString(1, locCode);
					 rs1 = pstmt1.executeQuery();
					 if (rs1.next())
					 {
						 invStat = rs1.getString("inv_stat");
					 }
					 pstmt1.close();
					 pstmt1 = null;
					 rs1.close();
					 rs1 = null;
					 sql = "select item_ser from item where item_code = ? ";
					 pstmt1 = conn.prepareStatement(sql);
					 pstmt1.setString(1, itemCode);
					 rs1 = pstmt1.executeQuery();
					 if (rs1.next())
					 {
						 itemSer = rs1.getString("item_ser");
					 }
					 pstmt1.close();
					 pstmt1 = null;
					 rs1.close();
					 rs1 = null;

					 finCommon = new FinCommon();
					 cctrCodeInv = finCommon.getFromAcctDetr(itemCode, itemSer, "STKINV", conn);

					 String tokens[] = cctrCodeInv.split(",");
					 if (tokens != null && tokens.length >= 2)
					 {
						 acctCodeInv = tokens[0];
						 cctrCodeInv = tokens[1];
					 }

					 // Calculating effective rate
					 // 22/01/01 manoharan argument gross_net added to the
					 // function
					 // get the net rate
					 effRate = calcEffRate(lineNo, stdQuantity, rate, taxAmount, tranId, "N", conn);
					 // ////////////////////////////////////////////////////////////////////////////////
					 // manoharan 29/03/02 effective rate should be more than 0
					 // added and rate > 0 by Manoj on 10/5/02 as per Kandarp
					 // since when sorder was posted
					 // for free item and if porcp is created then eff rate and
					 // rate are 0 so to bypass that
					 // check validation has been modified
					 if (effRate <= 0 && rate > 0)
					 {

						 errString = itmDBAccessLocal.getErrorString("", "VTRATE", "","",conn);

						 String begPart = errString.substring(0, errString.indexOf("<message>") + 9);
						 String begDesc = errString.substring(0, errString.indexOf("<description>") + 13);
						 String endDesc = errString.substring(errString.indexOf("</description>"));

						 String mainStr = begPart + "Effective rate calculated is [" + effRate + "] for Line No / Item [" + lineNo + " / " + itemCode + "] should be more than 0 " + "</message><description>";
						 mainStr = mainStr + "Please Check rate  " + endDesc;
						 errString = mainStr;
						 break;
					 }
					 // ////////////// 29/03/02
					 // ///////////////////////////////////////////////////////
					 // get the gross_rate
					 grossRate = calcEffRate(lineNo, stdQuantity, rate, taxAmount, tranId, "G", conn);

					 stkUpdMap = new HashMap();

					 if (receiptQty == 0)
					 {
						 exShtQty = 0;
					 }
					 stkUpdMap.put("quantity", Double.toString(quantity + exShtQty));
					 if (exShtQty != 0)
					 {
						 // Sharon 12-Sep-2003
						 /*
						  * if ( "Q".equals(uomRound) || "B".equals(uomRound)) {
						  * stdQuantity = distCommon.convQtyFactor(unit,stdUom,
						  * itemCode, quantity + exShtQty, conn); } else {
						  * stdQuantity = distCommon.convQtyFactor(unit,stdUom,
						  * itemCode, quantity + exShtQty, conn); }
						  */
						 // End Sharon
						 qtyAr = distCommon.getConvQuantityFact(unit, stdUom, itemCode, quantity + exShtQty, convQtyStdUom, conn);
						 convQtyStdUom = Double.parseDouble(qtyAr.get(0).toString());
						 stdQuantity = Double.parseDouble(qtyAr.get(1).toString());

						 // stdQuantity = gf_conv_qty_fact(unit,stdUom, itemCode,
						 // quantity + exShtQty, convQtyStdUom)

						 // added by jasmina DI89ALL024-17/07/09
						 // Script- if realised_qty is less than qty, than rate
						 // shd ne netamt/receiptQty(i.e net_amt/quantity +
						 // exShtQty)

						 effRate = calcEffRate(lineNo, receiptQty, rate, taxAmount, tranId, "N", conn);
						 // ////////////////////////////////////////////////////////////////////////////////
						 // manoharan 29/03/02 effective rate should be more than
						 // 0
						 // added and rate > 0 by Manoj on 10/5/02 as per Kandarp
						 // since when sorder was posted
						 // for free item and if porcp is created then eff rate
						 // and rate are 0 so to bypass that
						 // check validation has been modified
						 if (effRate <= 0 && rate > 0)
						 {
							 errString = itmDBAccessLocal.getErrorString("", "VTRATE", "","",conn);

							 String begPart = errString.substring(0, errString.indexOf("<message>") + 9);
							 String begDesc = errString.substring(0, errString.indexOf("<description>") + 13);
							 String endDesc = errString.substring(errString.indexOf("</description>"));

							 String mainStr = begPart + "Effective rate calculated is [" + effRate + "] for Line No / Item [" + lineNo + " / " + itemCode + "] should be more than 0 " + "</message><description>";
							 mainStr = mainStr + "Please Check rate  " + endDesc;
							 errString = mainStr;
							 break;
						 }
						 grossRate = calcEffRate(lineNo, receiptQty, rate, taxAmount, tranId, "G", conn);
					 }

					 stkUpdMap.put("gross_rate", Double.toString(grossRate));
					 stkUpdMap.put("qty_stduom", Double.toString(stdQuantity));
					 stkUpdMap.put("item_code", itemCode);
					 stkUpdMap.put("no_art", Double.toString(noArt));
					 stkUpdMap.put("site_code", siteRcp);
					 stkUpdMap.put("loc_code", locCode);

					 System.out.println("Lot No=[" + lotNo+"]");

					 stkUpdMap.put("lot_no", lotNo);
					 stkUpdMap.put("lot_sl", lotSl);
					 stkUpdMap.put("unit", unit);
					 stkUpdMap.put("unit__alt", unit);
					 stkUpdMap.put("tran_type", "R");
					 stkUpdMap.put("tran_date", tranDate);
					 stkUpdMap.put("tran_ser", tranSer);
					 stkUpdMap.put("tran_id", tranId);
					 stkUpdMap.put("acct_code__dr", acctCodeDR);
					 stkUpdMap.put("cctr_code__dr", cctCodeDR);
					 stkUpdMap.put("acct_code__cr", acctCodeCR);
					 stkUpdMap.put("cctr_code__ccr", cctCodeCR);
					 stkUpdMap.put("acct_code_inv", acctCodeDR);
					 stkUpdMap.put("cctr_code_inv", cctCodeDR);
					 stkUpdMap.put("line_no", lineNo);
					 if ("A".equals(invacctRate))
					 {
						 stkUpdMap.put("rate", Double.toString(effRate));
					 } else if ("S".equals(invacctRate))
					 {
						 stkUpdMap.put("rate", Double.toString(stdRate));
					 }
					 stkUpdMap.put("actual_rate", Double.toString(effRate));
					 stkUpdMap.put("site_code__mfg", siteCodeMfg);
					 stkUpdMap.put("supp_code__mfg", suppCodeMnfr);
					 stkUpdMap.put("potency_perc", Double.toString(potencyPerc));
					 stkUpdMap.put("pack_code", packCode);
					 stkUpdMap.put("mfg_date", mfgDate);
					 stkUpdMap.put("exp_date", expiryDate);
					 stkUpdMap.put("inv_stat", invStat);
					 // change done by Kunal on 09/10/12 start as per Manoj
					 // Sharma instructions
					 /*
					  * stkUpdMap.put("gross_weight",Double.toString(grossWeight
					  * * noArt));
					  * stkUpdMap.put("tare_weight",Double.toString(tareWeight *
					  * noArt));
					  * stkUpdMap.put("net_weight",Double.toString(netWeight *
					  * noArt));
					  */
					 stkUpdMap.put("gross_weight", Double.toString(grossWeight));
					 stkUpdMap.put("tare_weight", Double.toString(tareWeight));
					 stkUpdMap.put("net_weight", Double.toString(netWeight));
					 // change done by Kunal on 09/10/12 end

					 stkUpdMap.put("retest_date", retestDate);
					 stkUpdMap.put("grade", grade);
					 stkUpdMap.put("conv__qty_stduom", Double.toString(convQtyStdUom));
					 stkUpdMap.put("dimension", dimension); ////Added by Anagha R on 17/09/2020 for DIMENSION to be  captured and updated in stock, a new format to be defined to accept length, width and height
					 // stkUpdMap.put("pack_instr",packInstr);
					 stkUpdMap.put("batch_no", batchNo);
					 stkUpdMap.put("batch_size", Double.toString(batchSize));
					 stkUpdMap.put("shelf_life_type", shelfLifeType);
					 StockUpdate stkUpd = new StockUpdate();
					 errString = stkUpd.updateStock(stkUpdMap, xtraParams, conn);
					 stkUpd = null;
					 // stkUpdMap.clear(); //seems not required as the values are
					 // replaced properly in map
					 if (errString != null && errString.indexOf("Error") != -1)
					 {
						 break;
					 }

					 System.out.println("Going in git");

					 sql = "Select channel_partner, dis_link From site_supplier " + " Where  site_code = ? " + " And supp_code = ? ";
					 pstmt2 = conn.prepareStatement(sql);
					 pstmt2.setString(1, siteRcp);
					 pstmt2.setString(2, suppCode);
					 rs2 = pstmt2.executeQuery();
					 if (rs2.next())
					 {
						 channelPartner = checkNull(rs2.getString("channel_partner"));
						 disLink = checkNull(rs2.getString("dis_link"));
						 pstmt2.close();
						 pstmt2 = null;
						 rs2.close();
						 rs2 = null;
					 } else
					 {
						 pstmt2.close();
						 pstmt2 = null;
						 rs2.close();
						 rs2 = null;
						 sql = "Select channel_partner, dis_link From supplier " + " Where  supp_code = ? ";
						 pstmt2 = conn.prepareStatement(sql);
						 pstmt2.setString(1, suppCode);
						 rs2 = pstmt2.executeQuery();
						 if (rs2.next())
						 {
							 channelPartner = checkNull(rs2.getString("channel_partner"));
							 disLink = checkNull(rs2.getString("dis_link"));
						 }
						 pstmt2.close();
						 pstmt2 = null;
						 rs2.close();
						 rs2 = null;
					 }

					 System.out.println("Channel Partner " + channelPartner);
					 System.out.println("DisLink " + disLink);
					 // 01-Mar-2019 in case dis_link A also to be updated
					 //if (channelPartner.equalsIgnoreCase("Y") && (disLink.equalsIgnoreCase("E") || disLink.equalsIgnoreCase("A")))
					 // 10-oct-2019 manoharan to update GIT stock based on channel_partner flag in header
					 //if (channelPartner.equalsIgnoreCase("Y") && (disLink.equalsIgnoreCase("E") || disLink.equalsIgnoreCase("A")))
					 //if ("Y".equalsIgnoreCase(gitUpdate) && (disLink.equalsIgnoreCase("E") || disLink.equalsIgnoreCase("A")))
					 if ("Y".equalsIgnoreCase(gitUpdate) )
					 {
						 //Manish Mhatre 16oct19 start [to de-allocate free qty customer stock]
						 //if( custCodeEnd != null && custCodeEnd.trim().length()> 0 && rate == 0 )
						 if( custCodeEnd != null && custCodeEnd.trim().length()> 0 )
						 {						
							 if ("Y".equalsIgnoreCase(gitUpdate) && (custCodeEnd != null && custCodeEnd.trim().length() > 0))
							 {
								 BaseLogger.log("2", null, null, "SordAlloc Tran ID:[" + tranIdSordAlloc+ "] custCodeEnd[" + custCodeEnd + "]sorderNo[" + sorderNo + "]");
								 int lineNoSord = 1;
								 if (custCodeEnd != null && custCodeEnd.trim().length() > 0) 
								 {								
									 if (!custLineItem.containsKey(custCodeEnd)) 
									 {
										 tranIdSordAlloc = generateTranId("w_sord_alloc", siteCode,sdfApp.format(tranDate), conn);
										 HashMap detailItmMap = new HashMap();
										 detailItmMap.put("tran_id", tranIdSordAlloc);
										 detailItmMap.put("line_no", lineNoSord);
										 custLineItem.put(custCodeEnd, detailItmMap);

										 /*sql = "INSERT INTO SORD_ALLOC (TRAN_ID,TRAN_DATE,CUST_CODE,SITE_CODE,CHG_DATE,CHG_TERM,CHG_USER,ADD_DATE,ADD_TERM,ADD_USER,EMP_CODE__APRV,"
												+ " CONFIRMED,CONF_DATE,ACTIVE_PICK_ALLOW,SITE_CODE__SHIP,ALLOC_SOURCE,ALLOC_FLAG,EDI_STAT) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
										pstmtHdr = conn.prepareStatement(sql);*/
										 pstmtHdr.setString(1, tranIdSordAlloc);
										 pstmtHdr.setTimestamp(2, tranDate);
										 pstmtHdr.setString(3, custCodeEnd);
										 pstmtHdr.setString(4, siteRcp);
										 pstmtHdr.setTimestamp(5, tranDate);
										 pstmtHdr.setString(6, chgTerm);
										 pstmtHdr.setString(7, userId);
										 pstmtHdr.setTimestamp(8, tranDate);
										 pstmtHdr.setString(9, chgTerm);
										 pstmtHdr.setString(10, userId);
										 pstmtHdr.setString(11, loginEmp);
										 pstmtHdr.setString(12, "Y");
										 pstmtHdr.setTimestamp(13, tranDate);
										 pstmtHdr.setString(14, "Y");
										 pstmtHdr.setString(15, siteRcp);
										 pstmtHdr.setString(16, "M");
										 pstmtHdr.setString(17, "A");
										 pstmtHdr.setString(18, "N");
										 pstmtHdr.executeUpdate();
										 pstmtHdr.clearParameters();
										 /*pstmtHdr.close();
										pstmtHdr = null;*/

										 /*sql = "Insert into SORD_ALLOC_DET (TRAN_ID,LINE_NO,ITEM_CODE,LOC_CODE,LOT_NO,LOT_SL,QUANTITY,DEALLOC_QTY,SITE_CODE,PENDING_QTY)values (?,?,?,?,?,?,?,?,?,?)";
										pstmtDet = conn.prepareStatement(sql);*/
										 pstmtDet.setString(1, tranIdSordAlloc);
										 pstmtDet.setInt(2, lineNoSord);
										 pstmtDet.setString(3, itemCode);
										 pstmtDet.setString(4, locCode);
										 pstmtDet.setString(5, lotNo);
										 pstmtDet.setString(6, lotSl);
										 pstmtDet.setDouble(7, stdQuantity);
										 pstmtDet.setDouble(8, 0.0);
										 pstmtDet.setString(9, siteRcp);
										 pstmtDet.setDouble(10, 0.0);
										 int detCnt = pstmtDet.executeUpdate();
										 pstmtDet.clearParameters();
										 /*pstmtDet.close();
										pstmtDet = null;*/
										 if (detCnt > 0) {
											 strAllocate = new HashMap();
											 strAllocate.put("tran_date", tranDate);
											 strAllocate.put("ref_ser", "S-ALC");
											 strAllocate.put("ref_id", tranIdSordAlloc);
											 strAllocate.put("ref_line", "" + lineNoSord);
											 strAllocate.put("site_code", siteRcp);
											 strAllocate.put("item_code", itemCode);
											 strAllocate.put("loc_code", locCode);
											 strAllocate.put("lot_no", lotNo);
											 strAllocate.put("lot_sl", lotSl);
											 strAllocate.put("alloc_qty", new Double(stdQuantity));
											 strAllocate.put("chg_user", new ibase.utility.E12GenericUtility()
											 .getValueFromXTRA_PARAMS(xtraParams, "loginCode"));
											 strAllocate.put("chg_term", new ibase.utility.E12GenericUtility()
											 .getValueFromXTRA_PARAMS(xtraParams, "termId"));
											 strAllocate.put("chg_win", "W_SORDALLOC");
											 invAllocTrace = new InvAllocTraceBean();
											 errString = invAllocTrace.updateInvallocTrace(strAllocate, conn);
											 System.out.println("errString for Customer::: " + errString);
											 if (errString != null && errString.trim().length() > 0) {
												 break;
											 }
											 strAllocate = null;
											 // end if(detCnt > 0)
										 }

									 } else 
									 {
										 HashMap detailItmMap = custLineItem.get(custCodeEnd);
										 int line = Integer.parseInt(detailItmMap.get("line_no").toString()) + 1;
										 tranIdSordAlloc = detailItmMap.get("tran_id").toString();
										 detailItmMap.put("line_no", line);
										 custLineItem.put(custCodeEnd, detailItmMap);

										 BaseLogger.log("2", null, null, "Allocation details:" + line + ">>" + itemCode
												 + ">>" + locCode + ">>" + lotNo + ">>" + lotSl + ">>" + convQtyStdUom);
										 /*sql = "Insert into SORD_ALLOC_DET (TRAN_ID,LINE_NO,ITEM_CODE,LOC_CODE,LOT_NO,LOT_SL,QUANTITY,DEALLOC_QTY,SITE_CODE,PENDING_QTY)values (?,?,?,?,?,?,?,?,?,?)";
										pstmtDet = conn.prepareStatement(sql);*/
										 pstmtDet.setString(1, tranIdSordAlloc);
										 pstmtDet.setInt(2, line);
										 pstmtDet.setString(3, itemCode);
										 pstmtDet.setString(4, locCode);
										 pstmtDet.setString(5, lotNo);
										 pstmtDet.setString(6, lotSl);
										 pstmtDet.setDouble(7, stdQuantity);
										 pstmtDet.setDouble(8, 0.0);
										 pstmtDet.setString(9, siteRcp);
										 pstmtDet.setDouble(10, 0.0);
										 int detCnt = pstmtDet.executeUpdate();
										 pstmtDet.clearParameters();
										 /*pstmtDet.close();
										pstmtDet = null;*/
										 if (detCnt > 0) {
											 strAllocate = new HashMap();
											 strAllocate.put("tran_date", tranDate);
											 strAllocate.put("ref_ser", "S-ALC");
											 strAllocate.put("ref_id", tranIdSordAlloc);
											 strAllocate.put("ref_line", "" + line);
											 strAllocate.put("site_code", siteRcp);
											 strAllocate.put("item_code", itemCode);
											 strAllocate.put("loc_code", locCode);
											 strAllocate.put("lot_no", lotNo);
											 strAllocate.put("lot_sl", lotSl);
											 strAllocate.put("alloc_qty", new Double(stdQuantity));
											 strAllocate.put("chg_user", new ibase.utility.E12GenericUtility()
											 .getValueFromXTRA_PARAMS(xtraParams, "loginCode"));
											 strAllocate.put("chg_term", new ibase.utility.E12GenericUtility()
											 .getValueFromXTRA_PARAMS(xtraParams, "termId"));
											 strAllocate.put("chg_win", "W_SORDALLOC");
											 invAllocTrace = new InvAllocTraceBean();
											 errString = invAllocTrace.updateInvallocTrace(strAllocate, conn);
											 System.out.println("errString for Customer::: " + errString);
											 if (errString != null && errString.trim().length() > 0) {
												 break;
											 }
											 strAllocate = null;
											 // end if(detCnt > 0)
										 }										
									 }
								 }
							 } // end if
						 }
						 //Manish Mhatre 16oct19 end [to de-allocate free qty customer stock]																			

						 System.out.println("Entered in channel Partner loop");

						 sql = "select var_value" + " from disparm  Where prd_code='999999'" + " And   var_name='TRANSIT_LOC'";

						 pstmt2 = conn.prepareStatement(sql);
						 rs2 = pstmt2.executeQuery();
						 if (rs2.next())
						 {

							 locCodeGit = checkNull(rs2.getString("var_value"));

							 System.out.println("VarValue" + locCodeGit);
						 }

						 pstmt2.close();
						 pstmt2 = null;
						 rs2.close();
						 rs2 = null;

						 sql = "Select inv_stat from location" + " Where loc_code = ?";

						 pstmt2 = conn.prepareStatement(sql);

						 pstmt2.setString(1, locCodeGit);
						 rs2 = pstmt2.executeQuery();
						 if (rs2.next())
						 {

							 invStat = checkNull(rs2.getString("inv_stat"));
							 System.out.print("invstat" + invStat);
						 }
						 pstmt2.close();
						 pstmt2 = null;
						 rs2.close();
						 rs2 = null;

						 stkUpdMap.put("inv_stat", (invStat));
						 stkUpdMap.put("loc_code", (locCodeGit));
						 stkUpdMap.put("tran_type", "I");
						 stkUpd = new StockUpdate();
						 errString = stkUpd.updateStock(stkUpdMap, xtraParams, conn);
						 stkUpd = null;

						 sql = "Select hold_qty From stock Where item_code= ?" + "and site_code = ? and loc_code = ? and " + "lot_no =? and lot_sl = ?";

						 pstmt2 = conn.prepareStatement(sql);
						 pstmt2.setString(1, itemCode);
						 pstmt2.setString(2, siteRcp);
						 pstmt2.setString(3, locCodeGit);
						 pstmt2.setString(4, lotNo);
						 pstmt2.setString(5, lotSl);
						 rs2 = pstmt2.executeQuery();
						 if (rs2.next())
						 {
							 System.out.print("ItemCode" + itemCode);
							 System.out.print("SiteCode" + siteRcp);
							 lcholdQty = (rs2.getDouble("hold_qty"));
						 }
						 pstmt2.close();
						 pstmt2 = null;
						 rs2.close();
						 rs2 = null;

						 if (lcholdQty > 0)
						 {

							 sql = "update stock set  hold_qty =(case when hold_qty is null " + "then 0 else hold_qty end)-? " + " Where item_code  =?" + "And site_code    = ?" + " And loc_code = ? " + "And lot_no = ? And lot_sl= ?";

							 pstmtUpd = conn.prepareStatement(sql);
							 pstmtUpd.setDouble(1, lcholdQty);
							 pstmtUpd.setString(2, itemCode);
							 pstmtUpd.setString(3, siteRcp);
							 pstmtUpd.setString(4, locCodeGit);
							 pstmtUpd.setString(5, lotNo);
							 pstmtUpd.setString(6, lotSl);
							 updCnt = pstmtUpd.executeUpdate();
							 System.out.print("HoldQty Updated" + lcholdQty);

							 pstmtUpd.close();
							 pstmtUpd = null;

						 }						
					 }
					 // stkUpdMap = null;

					 /*
					  * // not required for taro to be developed later if
					  * channelPartner = 'Y' and disLink='E' then Select
					  * var_value Into :ls_loc_code_git From disparm Where
					  * prd_code='999999' And var_name='TRANSIT_LOC'; if
					  * get_sqlcode() < 0 then ls_errcode ='DS000'
					  * +string(sqlca.sqldbcode) exit end if
					  * 
					  * Select inv_stat Into :ls_tran_stat From location Where
					  * loc_code = :ls_loc_code_git; if get_sqlcode() < 0 then
					  * ls_errcode = 'DS000'+string(sqlca.sqldbcode) exit end if
					  * s_update.inv_stat = ls_tran_stat s_update.locationcode =
					  * ls_loc_code_git s_update.trantype = 'I' ls_errcode =
					  * lnvo_stock.gbf_update_stock(s_update) if len(ls_errcode)
					  * > 0 then exit end if // end Kalpesh
					  */
					 if (exShtQty != 0)
					 {
						 // Sharon 12-Sep-2003
						 if ("Q".equals(uomRound) || "B".equals(uomRound))
						 {
							 exShtQtystd = distCommon.convQtyFactor(unit, stdUom, itemCode, exShtQty, conn);
						 } else
						 {
							 exShtQtystd = distCommon.convQtyFactor(unit, stdUom, itemCode, exShtQty, conn);
						 }
						 stkUpdMap.put("loc_code", exShtLoc);
						 stkUpdMap.put("qty_stduom", Double.toString(-1 * exShtQty));
						 stkUpdMap.put("qty_stduom", Double.toString(-1 * exShtQtystd));

						 sql = "select inv_stat from location where loc_code =  ? ";
						 pstmt1 = conn.prepareStatement(sql);
						 pstmt1.setString(1, exShtLoc);
						 rs1 = pstmt1.executeQuery();
						 if (rs1.next())
						 {
							 invStat = rs1.getString("inv_stat");
						 }
						 pstmt1.close();
						 pstmt1 = null;
						 rs1.close();
						 rs1 = null;
						 stkUpdMap.put("inv_stat", invStat);
						 stkUpdMap.put("gross_weight", Double.toString(Math.abs(grossWeight / (quantity + exShtQty) * exShtQty)));
						 stkUpdMap.put("tare_weight", Double.toString(Math.abs(tareWeight / (quantity + exShtQty) * exShtQty)));
						 stkUpdMap.put("net_weight", Double.toString(Math.abs(netWeight / (quantity + exShtQty) * exShtQty)));

						 stkUpd = new StockUpdate();
						 errString = stkUpd.updateStock(stkUpdMap, xtraParams, conn);
						 stkUpd = null;
						 stkUpdMap.clear();
					 }
				 } // effectStock

				 // ////////////////////////////////////////////////////////////////
				 // If puchase order is closed then don't insert in any of the
				 // tables
				 // if ordStatus <> 'C' then

				 if (purcOrder != null)
				 {
					 if ("Y".equals(cancelBo))
					 {
						 sql = "Update porddet set status = 'C', status_date = ?, dlv_qty = (case when dlv_qty is null then 0 else dlv_qty end) + ? " + "	Where purc_order = ? and line_no = ? ";
					 } else
					 {
						 sql = "Update porddet set  status_date = ?, dlv_qty = (case when dlv_qty is null then 0 else dlv_qty end) + ? " + "	Where purc_order = ? and line_no = ? ";

					 }
					 chgDate = new java.sql.Timestamp(System.currentTimeMillis());

					 pstmtUpd = conn.prepareStatement(sql);
					 pstmtUpd.setTimestamp(1, chgDate);
					 pstmtUpd.setDouble(2, quantity);
					 pstmtUpd.setString(3, purcOrder);
					 pstmtUpd.setString(4, pordLine);
					 updCnt = pstmtUpd.executeUpdate();
					 if (updCnt != 1)
					 {
						 errString = itmDBAccessLocal.getErrorString("", "DS000NR", "","",conn);// Added
						 // by
						 // chandrashekar
						 // on
						 // 17-sep-2014
					 }
					 pstmtUpd.close();
					 pstmtUpd = null;
					 /**Modified by Pavan Rane 24dec19 start[to update demand/supply in summary table(RunMRP process) related changes]*/
					 demandSupplyMap.put("site_code", siteCodeDet);
					 demandSupplyMap.put("item_code", itemCode);		
					 demandSupplyMap.put("ref_ser", "P-ORD");
					 demandSupplyMap.put("ref_id", purcOrder);
					 demandSupplyMap.put("ref_line", pordLine);
					 demandSupplyMap.put("due_date",dlvDate );		
					 demandSupplyMap.put("demand_qty", 0.0);
					 demandSupplyMap.put("supply_qty", quantity *(-1));//quantity_stuom - dlv_qty
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
					 /**Modified by Pavan Rane 24dec19 end[to update demand/supply in summary table(RunMRP process) related changes]*/
				 }
				 // Changed By Pragyan on 25/02/15 to fix bug as suggested by
				 // Manohran sir
				 // 05/05/01 manoharan if all the PO Details are closed
				 // then close the PO Header also
				 // if(errString != null && errString.indexOf("Error") == -1 &&
				 // purcOrder != null )
				 if (errString == null || errString.trim().length() == 0 || errString.toUpperCase().indexOf("ERROR") == -1)
				 {
					 System.out.println("Bug fix check as on date 25FEB15 purcOrder [" + purcOrder + "");

					 if (purcOrder != null && purcOrder.length() > 0)
					 {
						 sql = "select count(1) from porddet where purc_order =  ? And (case when status is null then ' ' else status end) <> 'C' ";
						 pstmt1 = conn.prepareStatement(sql);
						 pstmt1.setString(1, purcOrder);
						 System.out.print("in count loop");
						 rs1 = pstmt1.executeQuery();
						 if (rs1.next())
						 {
							 count = rs1.getInt(1);
						 }
						 System.out.println("Count for relagainst in pordet>>>>>>>>>>> " + count);
						 pstmt1.close();
						 pstmt1 = null;
						 rs1.close();
						 rs1 = null;

						 if (count == 0)
						 {
							 boolean pocomp = true;
							 String sql1 = "select count(*) from PORD_PAY_TERM Where purc_order= ?and ( rel_agnst ='05' or rel_agnst ='06')";
							 pstmt1 = conn.prepareStatement(sql1);
							 pstmt1.setString(1, purcOrder);
							 System.out.print("in count loop");
							 rs1 = pstmt1.executeQuery();
							 if (rs1.next())
							 {
								 count1 = rs1.getInt(1);
							 }
							 pstmt1.close();
							 pstmt1 = null;
							 rs1.close();
							 rs1 = null;
							 System.out.println("Count for relagainst in pord_pay_term>>>>>>>>>>> " + count1);
							 if (count1 > 0)
							 {
								 String sql2 = "Select Count(*) From Pord_Pay_Term,Pur_Milstn " + "where Pord_Pay_Term.Line_No = pur_milstn.Line_No__Ord " + "And Pord_Pay_Term.Purc_Order = Pur_Milstn.Purc_Order " + "and Pord_Pay_Term.Purc_Order = ?";
								 pstmt2 = conn.prepareStatement(sql2);
								 pstmt2.setString(1, purcOrder);
								 System.out.print("in count for pordpayterm and purcmilestone exist>>>>>>>>>>>>>>");
								 rs2 = pstmt2.executeQuery();
								 while (rs2.next())
								 {
									 count2 = rs2.getInt(1);

								 }
								 System.out.print("in count for pordpayterm and purcmilestone line exist>>>>>>>>>>>>>>>>>>>>> " + count2);
								 pstmt2.close();
								 pstmt2 = null;
								 rs2.close();
								 rs2 = null;

								 if (count2 == 0)
								 {
									 pocomp = false;

								 } else if (count2 > 0)
								 {
									 String sql3 = "Select Count(*) From Pord_Pay_Term,Pur_Milstn " + "where Pord_Pay_Term.Line_No = pur_milstn.Line_No__Ord " + "And Pord_Pay_Term.Purc_Order = Pur_Milstn.Purc_Order " + "and Pur_Milstn.Task_Status ! = 'C'" + "and Pord_Pay_Term.Purc_Order = ?";
									 pstmt3 = conn.prepareStatement(sql3);
									 pstmt3.setString(1, purcOrder);
									 System.out.print("in count for pordpayterm and purcmilestone line>>>>>>>>>>>>>>");
									 rs3 = pstmt3.executeQuery();
									 while (rs3.next())
									 {
										 count3 = rs3.getInt(1);

									 }
									 System.out.print("in count for pordpayterm and purcmilestone line>>>>>>>>>>>>>>>>>>>>> " + count3);
									 pstmt3.close();
									 pstmt3 = null;
									 rs3.close();
									 rs3 = null;

								 }
								 if (count3 > 0)
								 {
									 pocomp = false;
								 }
							 }
							 if (pocomp == true)
							 {
								 sql = "update porder Set status = 'C' Where purc_order = ?";
								 pstmtUpd = conn.prepareStatement(sql);
								 pstmtUpd.setString(1, purcOrder);
								 updCnt = pstmtUpd.executeUpdate();
								 System.out.println("in update");
								 if (updCnt != 1)
								 {
									 errString = itmDBAccessLocal.getErrorString("", "DS000NR", "","",conn);// Added
									 // by
									 // chandrashekar
									 // on
									 // 17-sep-2014
								 }
								 pstmtUpd.close();
								 pstmtUpd = null;
							 }
						 }
					 }// if(purcOrder != null && purcOrder.length() > 0)
				 }

				 // Added by Jasmina-28/11/08- EO89SUN002,
				 // site is eou than update the qty used in ct3form table.
				 System.out.println("outside update");
				 if ("Y".equals(EOU) && "N".equals(dutyPaid))
				 {

					 sql = "select a.status as status , b.line_no as line_no, (case when b.quantity is null then 0 else b.quantity end) quantity, " + " (case when b.qty_used is null then 0 else b.qty_used end) as qty_used " + " from ct3form_hdr a , ct3form_det b " + " where a.form_no = b.form_no " + "	and a.form_no = ? " + "	and b.purc_order = ? " + "	and b.line_no__ord = ? " + "	and a.site_code = ? " + "	and b.supp_code = ? " + "	and b.item_code = ? " + "	and ? >= a.eff_from " + "	and ? <= a.valid_upto " + "	and case when a.confirmed is null then 'N' else a.confirmed end = 'Y' ";

					 pstmt1 = conn.prepareStatement(sql);
					 pstmt1.setString(1, formNo);
					 pstmt1.setString(2, purcOrder);
					 pstmt1.setString(3, pordLine);
					 pstmt1.setString(4, siteRcp);
					 pstmt1.setString(5, suppCode);
					 pstmt1.setString(6, itemCode);
					 pstmt1.setTimestamp(7, tranDate);
					 rs1 = pstmt1.executeQuery();
					 if (rs1.next())
					 {
						 formStatus = rs1.getString(1);
						 formLineNo = rs1.getInt(1);
						 ct3Quantity = rs1.getInt(1);
						 qtyUsed = rs1.getInt(1);
					 } else
					 {
						 pstmt1.close();
						 pstmt1 = null;
						 rs1.close();
						 rs1 = null;
						 errString = itmDBAccessLocal.getErrorString("", "VTCT3FORM1", "","",conn);
						 break;
					 }
					 pstmt1.close();
					 pstmt1 = null;
					 rs1.close();
					 rs1 = null;

					 if (!"O".equals(formStatus))
					 {
						 errString = itmDBAccessLocal.getErrorString("", "VTCT3FORM2", "","",conn);// CT3
						 // Form
						 // has
						 // been
						 // closed/cancelled/expired.
						 break;
					 } else if (ct3Quantity - qtyUsed < quantity)
					 {
						 errString = itmDBAccessLocal.getErrorString("", "VTCT3QTY", "","",conn);// Quantity
						 // exceeds
						 // the
						 // balance
						 // quantity
						 // of
						 // CT3
						 // Form
						 break;
					 } else
					 {
						 sql = "update ct3form_det set qty_used = ?  + ? " + " where form_no =  ? and line_no = ?";
						 pstmtUpd = conn.prepareStatement(sql);
						 pstmtUpd.setDouble(1, qtyUsed);
						 pstmtUpd.setDouble(2, quantity);
						 pstmtUpd.setString(3, formNo);
						 pstmtUpd.setInt(4, formLineNo);
						 updCnt = pstmtUpd.executeUpdate();
						 if (updCnt != 1)
						 {
							 errString = itmDBAccessLocal.getErrorString("", "DS000NR", "","",conn);// Added
							 // by
							 // chandrashekar
							 // on
							 // 17-sep-2014
						 }
						 pstmtUpd.close();
						 pstmtUpd = null;
					 }

					 // for Updating Bond Value, bond value shd be debited
					 if (bondTaxGroup != null && bondTaxGroup.trim().length() > 0)
					 {
						 bondTaxAmount = 0;
						 sql = "select Sum(case when tax_amt is null then 0 else tax_amt end) as tax_amount " + " from taxtran Where  tran_code = 'P-RCP' " + " and tran_id = ? " + " and line_no = ? " + " and tax_code IN ( select tax_code from tax where tax_group in ( " + bondTaxGroup + ")) ";

						 pstmt1 = conn.prepareStatement(sql);
						 pstmt1.setString(1, tranId);
						 pstmt1.setString(2, lineNo);
						 rs1 = pstmt1.executeQuery();
						 if (rs1.next())
						 {
							 bondTaxAmount = rs1.getDouble(1);
						 }
						 pstmt1.close();
						 pstmt1 = null;
						 rs1.close();
						 rs1 = null;

						 if (bondTaxAmount != 0)
						 {
							 bondValue = 0;
							 sql = "select bond_no, (case when bond_value is null then 0 else bond_value end) as bond_value, " + " (case when bank_guarantee is null then 0 else bank_guarantee end) as bank_guarantee " + " from b17_bond " + " where site_code = ? " + " and ? >= eff_from " + " and ? <= valid_upto " + " and case when confirmed is null then 'N' else confirmed end = 'Y' " + " and bond_type = 'B' ";

							 pstmt1 = conn.prepareStatement(sql);
							 pstmt1.setString(1, siteRcp);
							 pstmt1.setString(2, lineNo);
							 pstmt1.setTimestamp(3, tranDate);
							 pstmt1.setTimestamp(4, tranDate);
							 rs1 = pstmt1.executeQuery();
							 if (rs1.next())
							 {
								 bondNo = rs1.getString("bond_no");
								 bondValue = rs1.getDouble("bond_value");
								 bankGuarantee = rs1.getDouble("bank_guarantee");
							 } else
							 {
								 pstmt1.close();
								 pstmt1 = null;
								 rs1.close();
								 rs1 = null;
								 errString = itmDBAccessLocal.getErrorString("", "VTB17ERR1", "","",conn); // Active
								 // B17
								 // Bond
								 // Not
								 // Found
								 // for
								 // the
								 // Site
								 break;
							 }
							 pstmt1.close();
							 pstmt1 = null;
							 rs1.close();
							 rs1 = null;

							 if (bankGuarantee < (bondValue - bondTaxAmount) || (bondValue - bondTaxAmount) < 0)
							 {
								 errString = itmDBAccessLocal.getErrorString("", "VTB17ERR2", "","",conn); // Insufficient
								 // Bond
								 // Balance
								 break;
							 } else
							 {
								 sql = "update b17_bond set bond_value =  ?  + ? " + " where bond_no = ? ";
								 pstmtUpd = conn.prepareStatement(sql);
								 pstmtUpd.setDouble(1, bondValue);
								 pstmtUpd.setDouble(2, bondTaxAmount);
								 pstmtUpd.setString(3, bondNo);
								 updCnt = pstmtUpd.executeUpdate();
								 if (updCnt != 1)
								 {
									 errString = itmDBAccessLocal.getErrorString("", "DS000NR", "","",conn);// Added
									 // by
									 // chandrashekar
									 // on
									 // 17-sep-2014
								 }
								 pstmtUpd.close();
								 pstmtUpd = null;
							 }
						 }
					 } // bondTaxGroup
				 } // EOU and !dutyPaid
				 // Added end by Jasmina-28/11/08- EO89SUN002
			 } // end of details
			 //Added by Varsha V on 31-05-18 for cursor issue
			 if(pstmtItemLotPack != null)
			 {
				 pstmtItemLotPack.close();
				 pstmtItemLotPack = null;
			 }
			 //Manish Mhatre 16oct19 start [to de-allocate free qty customer stock]
			 if(pstmtHdr != null) {
				 pstmtHdr.close();
				 pstmtHdr = null;
			 }
			 if(pstmtDet != null)
			 {
				 pstmtDet.close();
				 pstmtDet = null;
			 }
			 if(!custLineItem.isEmpty())
			 {
				 custLineItem.clear();
			 }
			 //Manish Mhatre 16oct19 end [to de-allocate free qty customer stock]
			 //Ended by Varsha V on 31-05-18 for cursor issue
			 pstmt.close();
			 pstmt = null;
			 rs.close();
			 rs = null;

			 if (errString == null || errString.trim().length() == 0)
			 {

				 Set setItem = lockCodeWiseMap.entrySet();
				 tempList = null;
				 Iterator itrItem = setItem.iterator();
				 InvHoldGen invHoldGen = new InvHoldGen();
				 while (itrItem.hasNext())
				 {
					 Map.Entry itemMapEntry = (Map.Entry) itrItem.next();
					 lockCode = (String) itemMapEntry.getKey();
					 System.out.println(">>>>>>>>>Generate inv hold lockCode:" + lockCode);
					 tempList = (ArrayList) lockCodeWiseMap.get(lockCode);
					 System.out.println("tempList on confirm:::"+tempList);
					 errString = invHoldGen.generateHoldTrans(lockCode, tranId, "P-RCP", siteRcp, tempList, xtraParams, conn);
					 if (errString != null && errString.trim().length() > 0)
					 {
						 break;
					 }
				 }

			 }
			 // ////////////////////////////////////////////////////////////////////////////////////////
			 /*
			  * // not required for taro to be developed later // 30-06-2005
			  * manoharan error checking added if len(trim(ls_errcode)) > 0 then
			  * goto errfound end if // end 30-06-2005 manoharan
			  * //////////////////////////////////////////////// // 14/02/03
			  * manoharan this allocation need to be done // only if necessary //
			  * 29-06-2005 manoharan not to allocate for some clients
			  * ls_autoallocord = gf_getenv_dis('999999',"AUTO_ALLOC_ORD") if
			  * ls_autoallocord = "NULLFOUND" then ls_autoallocord = 'N' end if
			  * 
			  * if gs_run_mode = 'I' and ls_autoallocord = 'Y' then //Req
			  * No.MP-03-0014 Select key_string Into :ls_keystr from transetup
			  * Where upper(tran_window) = 'W_INV_ALLOCATE'; if get_sqlcode() =
			  * 100 then Select key_string Into :ls_keystr From transetup where
			  * upper(tran_window) = 'GENERAL'; if get_sqlcode() = 100 then
			  * ls_errcode = 'DS000' + string(sqlca.sqldbcode) goto errfound end
			  * if end if // MODIFIED BY RADHAKRISHNAN 0N 14-01-04 // MERGED BY
			  * TWO CURSORS DECLARE CURITEMCODEMFG CURSOR FOR SELECT DISTINCT
			  * A.ITEM_CODE__MFG , E.WORK_ORDER FROM PORCPDET A, ITEM B, PORCP C
			  * , PORDER D , PORDDET E WHERE A.ITEM_CODE = B.ITEM_CODE AND
			  * E.ITEM_CODE = B.ITEM_CODE AND A.PURC_ORDER = D.PURC_ORDER And
			  * D.PURC_ORDER = E.PURC_ORDER and e.line_no = a.line_no__ord AND
			  * A.TRAN_ID = C.TRAN_ID AND C.TRAN_ID= :as_tranid //AND
			  * DECODE(C.QC_REQD,'N',C.QC_REQD,B.QC_REQD) = 'N' and (case when
			  * C.QC_REQD = 'N' then C.QC_REQD else B.QC_REQD end) = 'N' // to
			  * avoid decode ** kiran 20/05/05 AND ((E.WORK_ORDER IS NOT NULL AND
			  * LENGTH(LTRIM(RTRIM(E.WORK_ORDER))) > 0) OR (A.ITEM_CODE__MFG IS
			  * NOT NULL AND LENGTH(LTRIM(RTRIM(A.ITEM_CODE__MFG))) > 0));
			  * 
			  * OPEN CURITEMCODEMFG; FETCH CURITEMCODEMFG INTO
			  * :itemCode_code_mfg,:ls_workorder; DO WHILE Sqlca.SqlCode = 0
			  * Declare cur_inv_rcpdet Cursor For Select a.line_no, a.item_code,
			  * a.quantity,a.loc_code, a.lot_no, a.lot_sl, (case when
			  * a.excess_short_qty is null then 0 else a.excess_short_qty end),
			  * a.item_code__mfg, a.unit, a.unit__std, a.conv__qty_stduom, (case
			  * when a.realised_qty is null then 0 else a.realised_qty end) From
			  * porcpdet a, item b , porcp c ,porder d , porddet e Where
			  * b.item_code = e.item_code and b.item_code = a.item_code And
			  * D.purc_order = a.purc_order and d.purc_order = e.purc_order and
			  * e.line_no = a.line_no__ord And a.tran_id = c.tran_id And
			  * a.tran_id = :as_tranid //And
			  * decode(c.qc_reqd,'N',c.qc_reqd,b.qc_reqd) = 'N' And (c.qc_reqd =
			  * 'N') or (b.qc_reqd = 'N') // to avoid decode ** kiran 20/05/05
			  * And (a.item_code__mfg = :itemCode_code_mfg or e.work_order =
			  * :ls_workorder);
			  * 
			  * Open cur_inv_rcpdet; Fetch cur_inv_rcpdet Into :lineNo,
			  * :itemCode, :quantity,:locCode, :lotNo, :lotSl,:exShtQty,
			  * :mfgItemCode, :unit, :stdUom, :convQtyStdUom, :receiptQty;
			  * 
			  * ll_newrow = lds_alloc_hdr.InsertRow(0)
			  * lds_alloc_hdr.SetItem(ll_newrow
			  * ,"tran_date",datetime(today(),time("00:00:00")))
			  * lds_alloc_hdr.SetItem(ll_newrow,"tran_type",'GIM') // GIM
			  * //02Dec-2004 //sharon
			  * lds_alloc_hdr.SetItem(ll_newrow,"site_code",siteRcp) // added by
			  * radhakrishnan 0n 10-01-04 if isnull(mfgItemCode) or
			  * len(trim(mfgItemCode)) = 0 then Select Item_Code Into
			  * :ls_WorkItem From WorkOrder Where Work_Order = :ls_workorder;
			  * lds_alloc_hdr.SetItem(ll_newrow,"item_code",ls_WorkItem) else
			  * lds_alloc_hdr.SetItem(ll_newrow,"item_code",mfgItemCode) end if
			  * // end here 10-01-04
			  * lds_alloc_hdr.SetItem(ll_newrow,"remarks","Auto Reserve " +
			  * as_tranid)
			  * lds_alloc_hdr.setitem(ll_newrow,"chg_date",datetime(today
			  * (),now())) lds_alloc_hdr.setitem(ll_newrow,"chg_user",userid)
			  * lds_alloc_hdr.setitem(ll_newrow,"chg_term",termid)
			  * lds_alloc_hdr.setitem
			  * (ll_newrow,"conf_date",datetime(today(),now()))
			  * lds_alloc_hdr.setitem(ll_newrow,"confirmed",'Y')
			  * lds_alloc_hdr.setitem(ll_newrow,"status",'O')
			  * lds_alloc_hdr.setitem
			  * (ll_newrow,"status_date",datetime(today(),now()))
			  * lds_alloc_hdr.SetItem(ll_newrow,"work_order",ls_workorder) //
			  * Assign the tran_id in the header if isnull(ls_tranid) or
			  * len(trim(ls_tranid)) = 0 then lb_insert = true // Generate INV
			  * Tran_Id ls_tranid = gf_gen_key_nvo(lds_alloc_hdr, "W-RIN",
			  * "tran_id", ls_keystr) if ls_tranid = 'ERROR' then ls_errcode =
			  * 'VTTRANID' goto errfound end if end if
			  * lds_alloc_hdr.SetItem(ll_newrow,"tran_id",ls_tranid)
			  * 
			  * Do While Sqlca.SqlCode = 0
			  * 
			  * if isnull(receiptQty) or receiptQty = 0 then exShtQty = 0 end if
			  * if isnull(lotNo) or len(trim(lotNo)) = 0 then lotNo = space(15)
			  * end if if isnull(lotSl) or len(trim(lotSl)) = 0 then lotSl =
			  * space(5) end if ld_quantity = quantity + exShtQty
			  * s_update.quantity = quantity + exShtQty If ls_value = 'Q' Or
			  * ls_value = 'B' Then stdQuantity = gf_conv_qty_fact1(unit,stdUom,
			  * itemCode, quantity + exShtQty, convQtyStdUom,'Y') Else
			  * stdQuantity = gf_conv_qty_fact1(unit,stdUom, itemCode, quantity +
			  * exShtQty, convQtyStdUom,'N') End If
			  * 
			  * ll_allocrow = lds_alloc_det.insertrow(0)
			  * lds_alloc_det.SetItem(ll_allocrow,"tran_id",ls_tranid)
			  * lds_alloc_det.SetItem(ll_allocrow,"line_no",ll_allocrow)
			  * lds_alloc_det.SetItem(ll_allocrow,"site_code",siteRcp)
			  * lds_alloc_det.SetItem(ll_allocrow,"item_code",itemCode)
			  * lds_alloc_det.SetItem(ll_allocrow,"loc_code",locCode)
			  * lds_alloc_det.SetItem(ll_allocrow,"lot_no",lotNo)
			  * lds_alloc_det.SetItem(ll_allocrow,"lot_sl",lotSl)
			  * lds_alloc_det.SetItem(ll_allocrow,"quantity",stdQuantity)
			  * lds_alloc_det.SetItem(ll_allocrow,"alloc_qty",quantity)
			  * lds_alloc_det.SetItem(ll_allocrow,"potency_adj",0)
			  * lds_alloc_det.SetItem(ll_allocrow,"dealloc_qty",0)
			  * lds_alloc_det.SetItem(ll_allocrow,"issue_qty",0)
			  * //lds_alloc_det.SetItem(ll_allocrow,"exp_lev",ls_explev)
			  * //lds_alloc_det.SetItem(ll_allocrow,"remarks",ls_remarks)
			  * //lds_alloc_det.SetItem(ll_allocrow,"reas_code",ls_reascode)
			  * 
			  * Fetch cur_inv_rcpdet Into :lineNo, :itemCode, :quantity,:locCode,
			  * :lotNo, :lotSl,:exShtQty, :mfgItemCode, :unit, :stdUom,
			  * :convQtyStdUom, :receiptQty; Loop Close cur_inv_rcpdet;
			  * 
			  * if lds_alloc_det.rowcount() > 0 then if lb_insert then if
			  * lds_alloc_hdr.Update() = 1 then if lds_alloc_det.Update() = 1
			  * then else ls_errcode = "DS000" + trim(string(sqlca.sqldbcode)) +
			  * "~t" + sqlca.sqlerrtext goto errfound end if else ls_errcode =
			  * "DS000" + trim(string(sqlca.sqldbcode)) + "~t" + sqlca.sqlerrtext
			  * goto errfound end if elseif lds_alloc_det.rowcount() > 0 then if
			  * lds_alloc_det.Update() = 1 then else ls_errcode = "DS000" +
			  * trim(string(sqlca.sqldbcode)) + "~t" + sqlca.sqlerrtext goto
			  * errfound end if end if if len(trim(ls_errcode)) = 0 and not
			  * isnull(ls_errcode) then ls_errcode =
			  * lnvo_alloc.gbf_confirm_inv_allocate(ls_tranid,ai_commit) end if
			  * end if
			  * 
			  * FETCH NEXT CURITEMCODEMFG INTO :itemCode_code_mfg,:ls_workorder;
			  * LOOP CLOSE CURITEMCODEMFG; end if //gs_run_mode = 'I' //
			  * 30-06-2005 manoharan error checking if len(trim(ls_errcode)) > 0
			  * then goto errfound end if // end 30-06-2005 manoharan
			  * 
			  * // Changes for Accounting Effect ls_errcode =
			  * lnvo_invacct.gbf_acct_po_rcpt(as_tranid,tranSer) if gs_run_mode =
			  * 'I' then // 24-02-2005 manoharan not required during loading
			  * update porcp set confirmed = 'Y' where tran_id = :as_tranid; if
			  * sqlca.sqlcode < 0 then ls_errcode = 'DS000' +
			  * trim(string(sqlca.SqlDbCode)) elseif sqlca.sqlnrows <> 1 then
			  * ls_errcode = 'VTPORCP2' end if ls_err =
			  * lnvo_vouch.gbf_check_accept_criteria(as_tranid) if ls_errcode =
			  * 'CREATE-VOUCHER' and trim(ls_err) <> "DONOTCREATE" then
			  * ls_errcode = "" // Commented by Brijesh Mishra on 20-10-04 due to
			  * bad argument // ls_errstr =
			  * lnvo_vouch.gbf_porcp_vouch_retrieve(as_tranid, as_tranid, 1)
			  * ls_errstr = lnvo_vouch.gbf_porcp_vouch_retrieve(as_tranid,
			  * as_tranid, 1, as_warning) if isnull(ls_errstr) then ls_errstr =
			  * "" ls_msgstr = "" if pos(ls_errstr,'~t') > 0 then ls_msgstr =
			  * ls_errstr ls_errcode = f_get_token(ls_msgstr,'~t') if ls_errcode
			  * = 'VTREJRET' then ls_errcode = "" else ls_errcode = ls_errstr end
			  * if else ls_errcode = ls_errstr end if // 30-06-2005 manoharan
			  * open of w_msg box removed this warning is not necessary elseif
			  * ls_errcode = 'CREATE-VOUCHER' and trim(ls_err) = "DONOTCREATE"
			  * then ls_errcode = '' elseIf (IsNull(ls_errcode) Or
			  * Len(Trim(ls_errcode)) = 0) And (IsNull(ls_err) Or
			  * Len(Trim(ls_err)) = 0) Then select count(1) into :ll_cnt from
			  * pord_pay_term where purc_order = :purcOrder and rel_agnst = '02';
			  * 
			  * if get_sqlcode() < 0 then ls_errcode = 'DS000' +
			  * trim(string(sqlca.sqldbcode)) populateerror(9999,'populateerror')
			  * ls_errcode = gf_error_location(ls_errcode) Return ls_errcode
			  * elseif ll_cnt > 0 then //function name changed by Sharon on
			  * 22-Jan-2003 // added by kashinath //ls_errcode =
			  * lnvo_vouch.gbf_porcp_advance(as_tranid,' ',' ',0,' ') ls_errcode
			  * = lnvo_pord.gbf_porder_advance(as_tranid,0,'PR') // end of added
			  * by kashinath End If end if
			  * 
			  * //Added by Brijesh Soni on 11-03-2006 //Update Insurance Amount.
			  * in Insurance Master if isnull(ls_errcode) or
			  * len(trim(ls_errcode)) = 0 then //Global structure s_ins_parm is
			  * used for storing insurance parameters. s_ins_parm lstr_ins
			  * 
			  * select policy_no into :ls_policy_no from porder where purc_order
			  * = :purcOrder ; if get_sqlcode() < 0 then ls_errcode = 'DS000' +
			  * trim(string(sqlca.sqldbcode)) goto errfound end if
			  * 
			  * if isnull(ls_errcode) or len(trim(ls_errcode)) = 0 then select
			  * tran_id , agent_code into :ls_tran_id , :ls_agent_code from
			  * insurance where policy_no = :ls_policy_no ; if get_sqlcode() < 0
			  * then ls_errcode = 'DS000' + trim(string(sqlca.sqldbcode)) goto
			  * errfound end if end if if isnull(ls_errcode) or
			  * len(trim(ls_errcode)) = 0 then if len(trim(ls_tran_id)) > 0 then
			  * lstr_ins.tran_id__ins = ls_tran_id lstr_ins.doc_no = as_tranid
			  * lstr_ins.doc_date = tranDate lstr_ins.ref_ser = 'P-RCP'
			  * lstr_ins.doc_value = ld_net_amt lstr_ins.curr_code = currCode
			  * 
			  * if isnull(lstr_ins.curr_code) then ls_errcode = "VTCURRCD1" end
			  * if
			  * 
			  * lstr_ins.exch_rate = exchRate if isnull(lstr_ins.exch_rate) then
			  * ls_errcode = "VTEXCH1" end if lstr_ins.doc_type = 'I'
			  * //Certificate and Policy number are same lstr_ins.cert_no =
			  * ls_policy_no lstr_ins.bulk = 'Y' select count(*) into :ll_cnt
			  * from insurance_det where tran_id__ins = :lstr_ins.tran_id__ins
			  * and ref_ser = 'P-RCP' and ref_id = :lstr_ins.doc_no and cert_no =
			  * :lstr_ins.cert_no; if ll_cnt > 0 then ls_errcode = 'VTINSNF1'
			  * else ls_errcode = gf_ins_upd(lstr_ins) end if end if end if end
			  * if //End by Brijesh Soni end if
			  * //////////////////////////////////////////////////////////////
			  */


		} // end try
		catch (Exception e)
		{
			try
			{
				conn.rollback();
			} catch (Exception e1)
			{
				//Added by Anjali R.  on[4/10/2018][Start]
				e1.printStackTrace();
				throw new ITMException(e1);
				//Added by Anjali R.  on[4/10/2018][End]
			}
			e.printStackTrace();
			throw new ITMException(e);
		} finally
		{
			try
			{
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (rs1 != null)
				{
					rs1.close();
					rs1 = null;
				}
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if (pstmt1 != null)
				{
					pstmt1.close();
					pstmt1 = null;
				}
				if (pstmtUpd != null)
				{
					pstmtUpd.close();
					pstmtUpd = null;
				}
			} catch (Exception e)
			{
				System.out.println(e.getMessage());
				e.printStackTrace();//Modified by Anjali R. on [25/10/2018]
				throw new ITMException(e);
			}
		}
		return errString;
	}

	private double calcEffRate(String lineNo, double stdQuantity, double acrate, double taxAmount, String tranId, String grossNet, Connection conn) throws RemoteException, ITMException
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		double rate = 0, recoAmount = 0, additionalCost = 0, netAmount = 0, exchRate = 0;
		try
		{

			sql = "select (case when exch_rate is null then 0 else exch_rate end ) exch_rate from porcp " + " where tran_id = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				exchRate = rs.getDouble(1);
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;

			if (exchRate == 0)
			{
				exchRate = 1;
			}

			sql = "select (case when sum(case when reco_amount is null then 0 else reco_amount end) is null then 0 else sum(case when reco_amount is null then 0 else reco_amount end) end) from taxtran " + " where tran_code = 'P-RCP' and tran_id = ? and line_no = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			pstmt.setString(2, lineNo);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				recoAmount = rs.getDouble(1);
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;

			sql = "select (case when additional_cost is null then 0 else additional_cost end), " + "(case when net_amt is null then 0 else net_amt end ) " + " from porcpdet where tran_id = ? and line_no = ?";

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			pstmt.setString(2, lineNo);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				additionalCost = rs.getDouble(1);
				netAmount = rs.getDouble(2);
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;

			// 22/01/01 manoharan gross rate calculation added
			if ("N".equals(grossNet))
			{
				rate = (((netAmount - recoAmount) * exchRate) + additionalCost) / stdQuantity;
			} else
			{
				rate = ((netAmount * exchRate) + additionalCost) / stdQuantity;
			}
		} catch (Exception e)
		{
			e.printStackTrace();//Modified by Anjali R. on [25/10/2018]
			throw new ITMException(e);
		}

		return rate;
	}

	private String getCurrdateAppFormat() throws RemoteException, ITMException
	{
		String currAppdate = "";
		java.sql.Timestamp currDate = null;
		try
		{
			Object date = null;
			currDate = new java.sql.Timestamp(System.currentTimeMillis());
			System.out.println(genericUtility.getDBDateFormat());
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(genericUtility.getDBDateFormat());
			date = sdf.parse(currDate.toString());
			currDate = java.sql.Timestamp.valueOf(sdf.format(date).toString() + " 00:00:00.0");
			currAppdate = new java.text.SimpleDateFormat(genericUtility.getApplDateFormat()).format(currDate).toString();
		} catch (Exception e)
		{
			e.printStackTrace();//Modified by Anjali R. on [25/10/2018]
			throw new ITMException(e);
		}
		return (currAppdate);
	}

	private java.sql.Timestamp getCurrtDate() throws RemoteException, ITMException
	{
		String currAppdate = "";
		java.sql.Timestamp currDate = null;
		try
		{
			Object date = null;
			currDate = new java.sql.Timestamp(System.currentTimeMillis());
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(genericUtility.getDBDateFormat());
			date = sdf.parse(currDate.toString());
			currDate = java.sql.Timestamp.valueOf(sdf.format(date).toString() + " 00:00:00.0");

		} catch (Exception e)
		{
			e.printStackTrace();//Modified by Anjali R. on[25/10/2018]
			throw new ITMException(e);
		}
		return (currDate);
	}

	public String checkReplVal(String tranId, Connection conn) throws RemoteException, ITMException
	{
		PreparedStatement pstmtSql = null;
		ResultSet rs = null;

		String ret = "";
		String rcpType = "";
		String retString = "";
		String sql = "";

		double amount = 0.0;
		double retAmount = 0.0;
		double diffAmount = 0.0;
		String parmValue = "0";

		try
		{

			ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();

			sql = "SELECT TRAN_ID__REF ,RECIEPT_TYPE, AMOUNT FROM PORCP WHERE TRAN_ID = ? AND TRAN_SER = 'P-RCP'";
			pstmtSql = conn.prepareStatement(sql);
			pstmtSql.setString(1, tranId);
			rs = pstmtSql.executeQuery();
			if (rs.next())
			{
				ret = checkNull(rs.getString("TRAN_ID__REF"));
				rcpType = checkNull(rs.getString("RECIEPT_TYPE"));
				amount = rs.getDouble("AMOUNT");
			}
			if (pstmtSql != null)
			{
				pstmtSql.close();
				pstmtSql = null;
			}
			if (rs != null)
			{
				rs.close();
				rs = null;
			}

			if (rcpType.equalsIgnoreCase("R"))
			{
				sql = "SELECT AMOUNT FROM PORCP WHERE TRAN_ID = ? AND TRAN_SER = 'P-RET'";
				pstmtSql = conn.prepareStatement(sql);
				pstmtSql.setString(1, ret);
				rs = pstmtSql.executeQuery();
				if (rs.next())
				{
					retAmount = rs.getDouble("AMOUNT");
				}
				if (pstmtSql != null)
				{
					pstmtSql.close();
					pstmtSql = null;
				}
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				DistCommon distCommon = new DistCommon();
				parmValue = distCommon.getDisparams("999999", "DIFF_RCP_RET_AMT", conn);
				if (parmValue == null || "NULLFOUND".equals(parmValue))
				{
					diffAmount = Double.parseDouble(parmValue);
				}
				if ((amount - retAmount) > diffAmount)
				{
					retString = itmDBAccessLocal.getErrorString("", "VTOVRDIFF", "","",conn);
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			throw new ITMException(e);
		} finally
		{
			try
			{
				if (pstmtSql != null)
				{
					pstmtSql.close();
					pstmtSql = null;
				}
			} catch (Exception e)
			{
				System.out.println(e.getMessage());
				e.printStackTrace();//Modified by Anjali R. on [25/10/2018]
				throw new ITMException(e);
			}
		}
		System.out.println("Returning Result ::" + retString);
		return retString;
	}

	public String chkUpdAddlCost(String tranId, Connection conn) throws RemoteException, ITMException
	{
		PreparedStatement pstmtSql = null;
		PreparedStatement pstmtUpd = null;
		ResultSet rs = null;

		String sql = "";
		String retString = "";
		String lineNo = "";

		double hdrCost = 0.0;
		double detCost = 0.0;
		double sumCost = 0.0;
		double sumValue = 0.0;
		double value = 0.0;
		double qty = 0.0;
		double rate = 0.0;

		int updCnt;

		long row = 0;

		try
		{
			ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();

			sql = "SELECT (CASE WHEN TOTAL_ADDITIONAL_COST IS NULL THEN 0 ELSE TOTAL_ADDITIONAL_COST END) AS TOTAL_ADDITIONAL_COST FROM PORCP WHERE TRAN_ID = ? ";
			pstmtSql = conn.prepareStatement(sql);
			pstmtSql.setString(1, tranId);
			rs = pstmtSql.executeQuery();
			if (rs.next())
			{
				hdrCost = rs.getDouble("TOTAL_ADDITIONAL_COST");
			} else
			{
				retString = itmDBAccessLocal.getErrorString("", "VTPORCP2", "","",conn);
			}
			if (pstmtSql != null)
			{
				pstmtSql.close();
				pstmtSql = null;
			}
			if (rs != null)
			{
				rs.close();
				rs = null;
			}

			if (hdrCost != 0)
			{
				sql = "SELECT (CASE WHEN SUM(CASE WHEN ADDITIONAL_COST IS NULL THEN 0 ELSE ADDITIONAL_COST END) IS NULL THEN 0 ELSE		   SUM(CASE WHEN ADDITIONAL_COST IS NULL THEN 0 ELSE ADDITIONAL_COST END) END), ( CASE WHEN SUM( (CASE WHEN			   QUANTITY__STDUOM IS NULL THEN 0 ELSE QUANTITY__STDUOM END) * (CASE WHEN RATE__STDUOM IS NULL THEN 0 ELSE			   RATE__STDUOM END) ) IS NULL THEN 0 ELSE SUM( (CASE WHEN QUANTITY__STDUOM IS NULL THEN 0 ELSE QUANTITY__STDUOM		   END) * (CASE WHEN RATE__STDUOM IS NULL THEN 0 ELSE RATE__STDUOM END) ) END) FROM PORCPDET WHERE TRAN_ID = ?";
				pstmtSql = conn.prepareStatement(sql);
				pstmtSql.setString(1, tranId);
				rs = pstmtSql.executeQuery();
				if (rs.next())
				{
					sumCost = rs.getDouble(1);
					sumValue = rs.getDouble(2);
				}
				if (pstmtSql != null)
				{
					pstmtSql.close();
					pstmtSql = null;
				}
				if (rs != null)
				{
					rs.close();
					rs = null;
				}

				if (sumCost != 0 && hdrCost != sumCost)
				{
					retString = itmDBAccessLocal.getErrorString("", "VTADDLCST", "","",conn);
				}

				if (sumCost == 0)
				{
					sql = "SELECT PORCPDET.LINE_NO, PORCPDET.QUANTITY__STDUOM, PORCPDET.RATE__STDUOM FROM PORCPDET, ITEM ITEM_A,		   ITEM ITEM_B, ACCOUNTS, SPECIFICATION, PORCP WHERE ( PORCPDET.ITEM_CODE__MFG = ITEM_B.ITEM_CODE (+)) AND           ( PORCPDET.ACCT_CODE__DR = ACCOUNTS.ACCT_CODE (+)) AND  ( PORCPDET.SPEC_REF = SPECIFICATION.SPEC_REF (+))		   AND           ( PORCPDET.ITEM_CODE = ITEM_A.ITEM_CODE ) AND  ( PORCPDET.TRAN_ID = PORCP.TRAN_ID ) AND  ( (        PORCPDET.TRAN_ID = ? ) ) ORDER BY PORCPDET.LINE_NO ASC ";
					pstmtSql = conn.prepareStatement(sql);
					pstmtSql.setString(1, tranId);
					rs = pstmtSql.executeQuery();
					while (rs.next())
					{
						lineNo = checkNull(rs.getString("LINE_NO"));
						qty = rs.getDouble("QUANTITY__STDUOM");
						rate = rs.getDouble("RATE__STDUOM");
						value = qty * rate;
						detCost = hdrCost / sumValue * value;
						sql = "UPDATE PORCPDET SET ADDITIONAL_COST = ? WHERE TRAN_ID = ? AND LINE_NO = ? ";
						pstmtUpd = conn.prepareStatement(sql);
						pstmtUpd.setDouble(1, detCost);
						pstmtUpd.setString(2, tranId);
						// pstmtUpd.setString(3, lineNo);
						pstmtUpd.setInt(3, Integer.parseInt(lineNo.trim()));// change
						// done
						// by
						// kunal
						// on
						// 6/11/12
						updCnt = pstmtUpd.executeUpdate();
						if (updCnt != 1)
						{
							retString = itmDBAccessLocal.getErrorString("", "UPDCNTNOT1", "","",conn);
						}
						if (pstmtUpd != null)
						{
							pstmtUpd.close();
							pstmtUpd = null;
						}
					}
					if (pstmtSql != null)
					{
						pstmtSql.close();
						pstmtSql = null;
					}
					if (rs != null)
					{
						rs.close();
						rs = null;
					}
				}
			}
		} catch (Exception e)

		{
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			throw new ITMException(e);
		} finally
		{
			try
			{
				if (pstmtSql != null)
				{
					pstmtSql.close();
					pstmtSql = null;
				}
				if (pstmtUpd != null)
				{
					pstmtUpd.close();
					pstmtUpd = null;
				}
			} catch (Exception e)
			{
				System.out.println(e.getMessage());
				e.printStackTrace();//Modified by Anjali R. on [25/10/2015]
				throw new ITMException(e);
			}
		}
		System.out.println("Returning Result ::" + retString);
		return retString;
	}

	//Changed by Jagruti Shinde Req id:[W16CSUN009]
	//public String createQc(String tranId, String siteCode, Connection conn) throws RemoteException, ITMException
	public String createQc(String tranId, String siteCode, Connection conn, String xtraParams) throws RemoteException, ITMException
	{
		PreparedStatement pstmtSql = null;
		PreparedStatement pstmtUpd = null;
		PreparedStatement pstmtInsert = null, pstmt1 = null, pstmt2 = null;
		ResultSet rs = null;
		ResultSet rs1 = null;
		ResultSet rs2 = null;

		String sql = "";
		String retString = "";
		String itemCode = "";
		String qcType = "";
		String lot = "";
		String lotNoRcp = "";
		String lotNoRcpOld = "";//Pavan Rane 16jul19		
		String lotSl = "";
		String key = "";
		String win = "w_qcorder_new";
		String qcNo = "";
		String lotRcp = "";
		String locCode = "";
		String lineNo = "";
		// String tranId = "";
		String unit = "";
		String batchNo = "";
		String aprv = "";
		String rej = "";
		String qcReqd = "";
		String lotNo = "";
		String genLotAuto = "";
		String rcpLine = "";
		String itemSer = "";
		String qcReqdSite = "";
		String confTran = "";
		String unitPur = "";
		String spec = "";
		String purcOrder = "";
		String lineNoOrd = "";
		String empCodeQcaprv = "";
		String purcorder = "";
		String pordType = "";
		String jobWorkType = "";
		String subContractType = "";
		String genLotSubctr = "";
		String value = "";
		String nullPo = "";
		String lotNoManualSite = "";
		String sitecode = "";
		String siteString = "";
		String emp = "";
		String procMth = "";
		String poRcpDetSpec = "";
		String dbName = "", useSuppLot = "", qcOrdType = "",qcLockDsp= "",lineNoSl="" ,lineNoP="";//Added by Jagruti Shinde Req id:[W16CSUN009]
		String suppCode = "", suppCodeMnfr = "";// added by Kunal on 8/11/12
		String apprLocLogic = "", apprLocUdf = "", qcLotLocCode = "",apprLocUdfInput="";// added by
		// chandrashekar
		// on
		// 07-01-15
		double qty = 0.0;
		double qtySample = 0.0, qtySampleSiteItem = 0;
		double passedQty = 0.0;
		double qcLeadTime = 0.0;
		double convQty = 0.0;
		double quantity = 0.0, excessShortQty = 0.0, quantityStduom = 0.0; // added
		// by
		// Kunal
		// on
		// 5/11/12
		double netWeight = 0.0, noArt = 0.0;// added by chandrashekar 0n
		// 31-12-2014
		java.sql.Timestamp expiryDate = null;
		java.sql.Timestamp qcDueDate = null;
		java.sql.Timestamp retestDt = null;
		java.sql.Timestamp tranDate = null;
		java.sql.Timestamp confDate = null;// new Date();
		java.sql.Timestamp mfgDate = null;
		java.sql.Timestamp today = null;// new Date();

		ArrayList qtyAr = null;

		int updCnt = 0, updCnt1 = 0;
		SimpleDateFormat dtFormat = null;
		String xmlValues = "",lotNoUpdated="";
		java.util.Date date = null;
		//Changed by Jagruti Shinde Req id:[W16CSUN009]
		ArrayList qcRejectedList ;
		InvHoldGen invHoldGen = new InvHoldGen();

		try
		{

			ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
			DistCommon distCommon = new DistCommon();
			ibase.utility.E12GenericUtility genericUtility = new ibase.utility.E12GenericUtility();
			dtFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());
			dbName = CommonConstants.DB_NAME;
			today = new java.sql.Timestamp(System.currentTimeMillis());
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(genericUtility.getDBDateFormat());
			date = sdf.parse(today.toString());
			today = java.sql.Timestamp.valueOf(sdf.format(date).toString() + " 00:00:00.0");

			SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
			java.util.Date currentDate = new java.util.Date();
			Timestamp newsysDate = java.sql.Timestamp.valueOf(sdf1.format(currentDate) + " 00:00:00.0");

			nullPo = distCommon.getDisparams("999999", "RCP_WO_PO", conn);
			//Changed by Jagruti Shinde Req id:[W16CSUN009]
			qcLockDsp = checkNullAndTrim(distCommon.getDisparams("999999", "QUARNTINE_LOCKCODE", conn));

			sql = "SELECT KEY_STRING FROM TRANSETUP WHERE TRAN_WINDOW = ? ";
			pstmtSql = conn.prepareStatement(sql);
			pstmtSql.setString(1, win);
			rs = pstmtSql.executeQuery();
			if (rs.next())
			{
				key = checkNull(rs.getString("KEY_STRING"));
			} else
			{
				pstmtSql.close();
				pstmtSql = null;
				rs.close();
				rs = null;
				sql = "SELECT KEY_STRING FROM TRANSETUP WHERE  TRAN_WINDOW = ?";
				pstmtSql = conn.prepareStatement(sql);
				pstmtSql.setString(1, "GENERAL");
				rs = pstmtSql.executeQuery();
				if (rs.next())
				{
					key = checkNull(rs.getString("KEY_STRING"));
				}
				pstmtSql.close();
				pstmtSql = null;
				rs.close();
				rs = null;
			}
			pstmtSql.close();
			pstmtSql = null;
			rs.close();
			rs = null;
			if (key == null || key.trim().length() == 0)
			{
				return itmDBAccessLocal.getErrorString("", "NOKEYSTRING", "","",conn);
			}
			sql = "SELECT (CASE WHEN QC_REQD IS NULL THEN 'Y' ELSE QC_REQD END) AS QC_REQD, PURC_ORDER, TRAN_DATE,SUPP_CODE FROM PORCP WHERE TRAN_ID = ?";
			pstmtSql = conn.prepareStatement(sql);
			pstmtSql.setString(1, tranId);
			rs = pstmtSql.executeQuery();
			if (rs.next())
			{
				qcReqd = checkNull(rs.getString("QC_REQD"));
				purcorder = checkNull(rs.getString("PURC_ORDER"));
				tranDate = rs.getTimestamp("TRAN_DATE");
				suppCode = checkNull(rs.getString("SUPP_CODE")); // added by
				// Kunal on
				// 8/11/12
			}
			pstmtSql.close();
			pstmtSql = null;
			rs.close();
			rs = null;
			genLotAuto = distCommon.getDisparams("999999", "GENERATE_LOT_NO_AUTO", conn);

			value = distCommon.getDisparams("999999", "UOM_ROUND", conn);
			if (value == null)
			{
				retString = itmDBAccessLocal.getErrorString("", "VTUOMVARPARM", "","",conn);
				return retString;
				// break;
			}

			confTran = distCommon.getDisparams("999999", "RCP_TRAN_CONF_DATE", conn);
			if (confTran == null)
			{
				retString = itmDBAccessLocal.getErrorString("", "VTDISPARM", "","",conn);
				return retString;
				// break;
			}

			if (!"TRAN".equalsIgnoreCase(confTran) && !"CONF".equalsIgnoreCase(confTran))
			{
				retString = itmDBAccessLocal.getErrorString("", "VTDISPARM", "","",conn);
				return retString;
				// break;
			}
			if ("CONF".equalsIgnoreCase(confTran))
			{
				tranDate = getCurrtDate(); // confDate;
			}

			if (purcorder != null && !"Y".equals(nullPo))
			{
				sql = "SELECT PORD_TYPE FROM PORDER WHERE PURC_ORDER = ?";
				pstmtSql = conn.prepareStatement(sql);
				pstmtSql.setString(1, purcorder);
				rs = pstmtSql.executeQuery();
				if (rs.next())
				{
					pordType = checkNull(rs.getString("PORD_TYPE"));
				}
				rs.close();
				rs = null;
				pstmtSql.close();
				pstmtSql = null;
			}

			jobWorkType = distCommon.getDisparams("999999", "JOBWORK_TYPE", conn);
			//Changed by Jagruti Shinde Req id:[W16CSUN009]
			//if (jobWorkType == null )
			if (jobWorkType == null || "NULLFOUND".equalsIgnoreCase(jobWorkType) )
			{
				jobWorkType = "";
			}

			subContractType = distCommon.getDisparams("999999", "SUBCONTRACT_TYPE", conn);
			//Changed by Jagruti Shinde Req id:[W16CSUN009]
			//if (subContractType == null)
			if (subContractType == null || "NULLFOUND".equalsIgnoreCase(subContractType))
			{
				subContractType = "";
			}

			genLotSubctr = distCommon.getDisparams("999999", "GEN_LOT_SUBCTR", conn);
			//Changed by Jagruti Shinde Req id:[W16CSUN009]
			//if (genLotSubctr == null)
			if (genLotSubctr == null || "NULLFOUND".equalsIgnoreCase(genLotSubctr))
			{
				genLotSubctr = "";
			}

			sql = "SELECT  EMP_CODE FROM USERS WHERE CODE = ?";
			pstmtSql = conn.prepareStatement(sql);
			pstmtSql.setString(1, userId);
			rs = pstmtSql.executeQuery();
			if (rs.next())
			{
				emp = checkNull(rs.getString("EMP_CODE"));
			}
			rs.close();
			rs = null;
			pstmtSql.close();
			pstmtSql = null;

			// added by sagar on 10/07/14 start
			sql = "SELECT (CASE WHEN USE_SUPPLIER_LOT IS NULL THEN 'N' ELSE USE_SUPPLIER_LOT END) AS USE_SUPPLIER_LOT FROM SUPPLIER WHERE SUPP_CODE= ?";
			pstmtSql = conn.prepareStatement(sql);
			pstmtSql.setString(1, suppCode);
			rs = pstmtSql.executeQuery();
			if (rs.next())
			{
				useSuppLot = rs.getString("USE_SUPPLIER_LOT");
			}
			rs.close();
			rs = null;
			pstmtSql.close();
			pstmtSql = null;
			System.out.println(">>>>>>>>>>>>>useSuppLot:" + useSuppLot);
			if ("Y".equals(useSuppLot))
			{
				qcOrdType = "A";
			} else
			{
				qcOrdType = "I";
			}
			System.out.println(">>>>>>qcOrdType:" + qcOrdType);
			System.out.println(">>>>>>qcReqd:" + qcReqd);
			// added by sagar on 10/07/14 end

			if ("Y".equalsIgnoreCase(qcReqd))
			{
				sql = "SELECT ITEM_CODE, MIN(LINE_NO) AS LINE_NO FROM   PORCPDET WHERE  PORCPDET.TRAN_ID = ? GROUP BY ITEM_CODE "; // ORDER
				// BY
				// MIN(LINE_NO)";
				pstmtSql = conn.prepareStatement(sql);
				pstmtSql.setString(1, tranId);
				rs = pstmtSql.executeQuery();
				while (rs.next())
				{
					itemCode = checkNull(rs.getString("ITEM_CODE"));
					lineNo = checkNull(rs.getString("LINE_NO"));

					sql = "SELECT (CASE WHEN QC_LEAD_TIME IS NULL THEN 0 ELSE QC_LEAD_TIME END) AS QC_LEAD_TIME FROM ITEM WHERE ITEM_CODE = ?";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, itemCode);
					rs1 = pstmt1.executeQuery();

					if (rs1.next())
					{
						qcLeadTime = rs1.getDouble("QC_LEAD_TIME");
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;

					// ldt_qcduedate =
					// datetime(relativedate(date(ldt_tran_date),lc_qcleadtime))

					sql = "SELECT SITEITEM.ITEM_SER , SITEITEM.QC_REQD, SITEITEM.PROC_MTH,ITEM.QC_REQD_TYPE, (CASE WHEN SITEITEM.QTY_SAMPLE IS NULL THEN 0 ELSE SITEITEM.QTY_SAMPLE END) AS QTY_SAMPLE  FROM SITEITEM , ITEM WHERE ITEM.ITEM_CODE = SITEITEM.ITEM_CODE AND SITEITEM.ITEM_CODE = ? AND SITEITEM.SITE_CODE = ? ";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, itemCode);
					pstmt1.setString(2, siteCode);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						itemSer = checkNull(rs1.getString("ITEM_SER"));
						qcReqdSite = checkNull(rs1.getString("QC_REQD"));
						procMth = checkNull(rs1.getString("PROC_MTH"));
						qcType = checkNull(rs1.getString("QC_REQD_TYPE")); 
						// by
						// Kunal
						// Mandhre
						// on
						// 29/10/12
						qtySampleSiteItem = rs1.getDouble("QTY_SAMPLE");
					} else
					{
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
						//Added By PriyankaC on 25JAN2018..[START]
						//	sql = "SELECT QC_REQD_TYPE, (CASE WHEN QTY_SAMPLE IS NULL THEN 0 ELSE QTY_SAMPLE END) AS QTY_SAMPLE, PROC_MTH  FROM ITEM WHERE ITEM_CODE = ?";
						sql = "SELECT QC_REQD_TYPE, (CASE WHEN QTY_SAMPLE IS NULL THEN 0 ELSE QTY_SAMPLE END) AS QTY_SAMPLE, PROC_MTH ,QC_REQD FROM ITEM WHERE ITEM_CODE = ?"; 
						//Changed By PriyankaC on 25JAN2018..[END]
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1, itemCode);
						rs1 = pstmt1.executeQuery();
						if (rs1.next())
						{
							qcType = checkNull(rs1.getString("QC_REQD_TYPE"));
							qtySample = rs1.getDouble("QTY_SAMPLE");
							procMth = checkNull(rs1.getString("PROC_MTH"));
							qcReqdSite = checkNull(rs1.getString("QC_REQD")); //Added By PriyankaC on 25JAN2018.. 

						}
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					if (qtySampleSiteItem > 0)
					{
						qtySample = qtySampleSiteItem;
					}
					if (itemSer == null || itemSer.trim().length() == 0)
					{
						sql = "SELECT ITEM_SER FROM ITEM WHERE ITEM_CODE = ?";
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1, itemCode);
						rs1 = pstmt1.executeQuery();
						if (rs1.next())
						{
							itemSer = checkNull(rs1.getString("ITEM_SER"));
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
					}

					if (procMth != null && procMth.trim().length() > 0)
					{
						qtySample = 0.0;
					}

					if (qcType == null || qcType.trim().length() == 0) // change
						// done
						// by
						// Kunal
						// on
						// 26/10/12
						// add
						// trim()
						// cond.
					{
						qcType = "S";
					}
					System.out.println(">>>>>>>qcType:= " + qcType);
					System.out.println(">>>qcReqdSite: = " + qcReqdSite);

					if ("S".equalsIgnoreCase(qcType) && "Y".equalsIgnoreCase(qcReqdSite))
					{
						System.out.println(">>>>>>>In S And Y <<<<<<<<<<=" + qcReqdSite);
						// change done by Kunal on 26/10/12 rename column as
						// CONV__QTY_STDUOM,SPEC_REF,BATCH_NO
						sql = "SELECT LINE_NO, LOC_CODE	, LOT_NO, LOT_SL,  (CASE WHEN QUANTITY IS NULL THEN 0 ELSE QUANTITY END + CASE		   WHEN EXCESS_SHORT_QTY IS NULL THEN 0 ELSE EXCESS_SHORT_QTY END) , " + " (CASE WHEN CONV__QTY_STDUOM IS NULL         THEN 1 ELSE CONV__QTY_STDUOM END) as CONV__QTY_STDUOM ," + " UNIT__STD, (CASE WHEN BATCH_NO IS NULL THEN ' ' ELSE BATCH_NO END) as BATCH_NO	       , EXPIRY_DATE , PURC_ORDER, LINE_NO__ORD ,  " + " UNIT, (CASE WHEN SPEC_REF IS NULL THEN ' ' ELSE SPEC_REF          END ) as SPEC_REF, MFG_DATE, " + "  ( CASE		   WHEN EXCESS_SHORT_QTY IS NULL THEN 0 ELSE EXCESS_SHORT_QTY END) as EXCESS_SHORT_QTY , ( CASE		   WHEN quantity__stduom IS NULL THEN 0 ELSE quantity__stduom END) as quantity__stduom,supp_code__mnfr " // added
								// by
								// kunal
								// on
								// 5/11/12
								+ " FROM  PORCPDET WHERE TRAN_ID   = ? AND   ITEM_CODE = ? ORDER BY LINE_NO ASC";
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1, tranId);
						pstmt1.setString(2, itemCode);
						rs1 = pstmt1.executeQuery();
						while (rs1.next())
						{
							lineNo = checkNull(rs1.getString("LINE_NO"));
							locCode = checkNull(rs1.getString("LOC_CODE"));
							lotNoRcp = (rs1.getString("LOT_NO"));
							lotSl = (rs1.getString("LOT_SL"));
							qty = rs1.getDouble(5);
							convQty = rs1.getDouble("CONV__QTY_STDUOM");
							unit = checkNull(rs1.getString("UNIT__STD"));
							batchNo = checkNull(rs1.getString("BATCH_NO"));
							expiryDate = rs1.getTimestamp("EXPIRY_DATE");
							purcOrder = checkNull(rs1.getString("PURC_ORDER"));
							lineNoOrd = checkNull(rs1.getString("LINE_NO__ORD"));
							unitPur = (rs1.getString("UNIT"));
							spec = (rs1.getString("SPEC_REF"));
							mfgDate = rs1.getTimestamp("MFG_DATE");
							// added by kunal on 5/11/12 start as per Pravin
							// Sali Sir instruction
							excessShortQty = rs1.getDouble("EXCESS_SHORT_QTY"); // added
							// by
							// Kunal
							// on
							// 5/11/12
							quantityStduom = rs1.getDouble("quantity__stduom"); // added
							// by
							// Kunal
							// on
							// 5/11/12
							System.out.println("excessShortQt =" + excessShortQty);
							System.out.println("quantityStduom =" + quantityStduom);
							//Commented by Ajay on 28/05/18:START
							/*if (excessShortQty == 0)
							{
								System.out.println("get qty from quantityStduom");
								qty = quantityStduom;
							}
							// added by kunal on 5/11/12 end as per Pravin Sali
							// Sir instruction
							suppCodeMnfr = checkNull(rs1.getString("supp_code__mnfr"));// added
							// by
							// Kunal
							// on
							// 8/11/12
							System.out.println("suppCodeMnfr = " + suppCodeMnfr);

							// if ( "Q".equalsIgnoreCase(value) ||
							// "B".equalsIgnoreCase(value) )
							// {
							// qty = gf_conv_qty_fact1(ls_unitpur, ls_unit,
							// ls_itemcode, lc_Qty, lc_convqty,'Y')

							System.out.println("before qty [" + qty + "]");*/
							//Commented by Ajay on 28/05/18:END


							//Add by Ajay on 28/05/18:START
							if (excessShortQty == 0)
							{
								System.out.println("get qty from quantityStduom");
								qty = quantityStduom;
							}
							//added by saurabh missed condition part from nvo source as per discussion with Manoj S.[03/AUG/17|Start]
							else
							{
								if ( "Q".equalsIgnoreCase(value) || "B".equalsIgnoreCase(value))
								{
									qtyAr = distCommon.getConvQuantityFact(unitPur, unit, itemCode, qty, convQty, conn);
								}
								convQty = Double.parseDouble(qtyAr.get(0).toString());
								qty = Double.parseDouble(qtyAr.get(1).toString());
								System.out.println(" (excessShortQty>0) qty [" + qty + "]");
							}
							System.out.println(" After excessShortQty check qty [" + qty + "]");
							//added by saurabh missed condition part from nvo source as per discussion with Manoj S.[03/AUG/17|End]
							//Add by Ajay on 28/05/18:END

							qtyAr = distCommon.getConvQuantityFact(unitPur, unit, itemCode, qty, convQty, conn);
							convQty = Double.parseDouble(qtyAr.get(0).toString());
							qty = Double.parseDouble(qtyAr.get(1).toString());
							System.out.println("after qty [" + qty + "]");
							// }
							// else
							// {
							// //qty = gf_conv_qty_fact1(ls_unitpur, ls_unit,
							// ls_itemcode, lc_Qty, lc_convqty,'N')
							// qty = distCommon.getConvQuantityFact(unitPur,
							// unit, itemCode, qty, convQty, conn);
							// }

							if (purcOrder != null && !nullPo.equalsIgnoreCase("Y"))
							{
								sql = "SELECT EMP_CODE__QCAPRV FROM PORDDET WHERE  PURC_ORDER = ? AND LINE_NO = ? ";
								pstmt2 = conn.prepareStatement(sql);
								pstmt2.setString(1, purcOrder);
								pstmt2.setString(2, lineNo);
								rs2 = pstmt2.executeQuery();
								if (rs2.next())
								{
									empCodeQcaprv = checkNull(rs2.getString("EMP_CODE__QCAPRV"));
								}
								pstmt2.close();
								pstmt2 = null;
								rs2.close();
								rs2 = null;
							}

							if (spec == null || spec.trim().length() == 0)
							{
								sql = "SELECT CASE WHEN SPEC_REF IS NULL THEN ' ' ELSE SPEC_REF END FROM SITEITEM WHERE SITE_CODE = ?		   AND    ITEM_CODE = ? ";
								pstmt2 = conn.prepareStatement(sql);
								pstmt2.setString(1, siteCode);
								pstmt2.setString(2, itemCode);
								rs2 = pstmt2.executeQuery();
								if (rs2.next())
								{
									spec = checkNull(rs2.getString(1));
								}
								pstmt2.close();
								pstmt2 = null;
								rs2.close();
								rs2 = null;
							}

							xmlValues = "";
							xmlValues = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
							xmlValues = xmlValues + "<Header></Header>";
							xmlValues = xmlValues + "<Detail1>";
							xmlValues = xmlValues + "<qorder_no></qorder_no>";
							xmlValues = xmlValues + "<site_code>" + siteCode + "</site_code>";
							xmlValues = xmlValues + "<qorder_date>" + getCurrdateAppFormat() + "</qorder_date>";
							// xmlValues = xmlValues +
							// "<qorder_type>I</qorder_type>";
							xmlValues = xmlValues + "<qorder_type>" + qcOrdType + "</qorder_type>";// added
							// by
							// sagar
							// on
							// 10/07/14
							xmlValues = xmlValues + "<lot_no>" + lotNoRcp + "</lot_no>";
							xmlValues = xmlValues + "<item_ser>" + itemSer + "</item_ser>";
							xmlValues = xmlValues + "<porcp_no>" + tranId + "</porcp_no>";
							xmlValues = xmlValues + "<porcp_line_no>" + lineNo + "</porcp_line_no>";
							xmlValues = xmlValues + "</Detail1></Root>";
							System.out.println("xmlValues  :[" + xmlValues + "]");
							TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", commonConstants.DB_NAME);
							qcNo = tg.generateTranSeqID("QC-ORD", "qorder_no", key, conn);
							System.out.println("qcNo1=" + qcNo);

							if ("ERROR".equals(qcNo))
							{
								retString = itmDBAccessLocal.getErrorString("", "VTTRANID", "","",conn);
								break;
							}

							if (retString.trim().length() > 0)
							{
								break;
							} else
							{
								sql = "SELECT LOC_CODE__APRV, LOC_CODE__REJ FROM SITEITEM WHERE SITE_CODE = ? AND ITEM_CODE = ? ";
								pstmt2 = conn.prepareStatement(sql);
								pstmt2.setString(1, siteCode);
								pstmt2.setString(2, itemCode);
								rs2 = pstmt2.executeQuery();
								if (rs2.next())
								{
									aprv = checkNull(rs2.getString("LOC_CODE__APRV"));
									rej = checkNull(rs2.getString("LOC_CODE__REJ"));
								}
								pstmt2.close();
								pstmt2 = null;
								rs2.close();
								rs2 = null;

								if (!"Y".equals(useSuppLot)) // Condition added
									// by sagar on
									// 11/07/14
								{
									System.out.println(">>>>>>>in S & Y if useSuppLot is not Y:" + useSuppLot);
									if ("Y".equalsIgnoreCase(genLotAuto) || "M".equalsIgnoreCase(genLotAuto))
									{

										if ((!pordType.equalsIgnoreCase(jobWorkType) && !pordType.equalsIgnoreCase(subContractType)))
										{
											lotNoRcp = qcNo;
										} else
										{
											if (genLotSubctr.equalsIgnoreCase("Y"))
											{
												lotNoRcp = qcNo;
											}
										}
									}
								}

								passedQty = qty - qtySample;

								if (dbName.equalsIgnoreCase("db2"))
								{
									if (itemCode.length() == 0)
									{
										itemCode = null;
									}
									if (siteCode.length() == 0)
									{
										siteCode = null;
									}
									if (locCode.length() == 0)
									{
										locCode = null;
									}
									if (aprv.length() == 0)
									{
										aprv = null;
									}
									if (rej.length() == 0)
									{
										rej = null;
									}
									if (empCodeQcaprv.length() == 0)
									{
										empCodeQcaprv = null;
									}
									if (unit.length() == 0)
									{
										unit = null;
									}
									if (qcNo.length() == 0)
									{
										qcNo = null;
									}
								}

								sql = "INSERT INTO QC_ORDER ( QORDER_NO, QORDER_TYPE, QORDER_DATE, SITE_CODE, " + " ITEM_CODE, ROUTE_CODE, QUANTITY, QTY_PASSED, QTY_REJECTED, START_DATE, " + " DUE_DATE, REL_DATE, PORCP_NO, PORCP_LINE_NO, LOT_NO, LOT_SL, CHG_DATE, " + " CHG_USER, CHG_TERM, LOC_CODE, QTY_SAMPLE, STATUS, UNIT, QC_CREATE_TYPE, " + " BATCH_NO, EXPIRY_DATE, LOC_CODE__APRV, LOC_CODE__REJ, UNIT__SAMPLE, " + " LOT_NO__NEW, RETEST_DATE, EMP_CODE__QCAPRV, SPEC_REF, ITEM_CODE__NEW, " + " MFG_DATE, EMP_CODE ,SUPP_CODE,SUPP_CODE__MFG) " + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " + " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?)";
								pstmtInsert = conn.prepareStatement(sql);
								pstmtInsert.setString(1, qcNo);
								// pstmtInsert.setString(2, "I");
								pstmtInsert.setString(2, qcOrdType);// added by
								// sagar on
								// 10/07/14
								pstmtInsert.setTimestamp(3, tranDate);
								pstmtInsert.setString(4, siteCode);
								pstmtInsert.setString(5, itemCode);
								pstmtInsert.setString(6, null);
								pstmtInsert.setDouble(7, qty);
								pstmtInsert.setDouble(8, passedQty);
								pstmtInsert.setDouble(9, 0);
								pstmtInsert.setTimestamp(10, tranDate);
								pstmtInsert.setTimestamp(11, qcDueDate);
								pstmtInsert.setTimestamp(12, tranDate);
								pstmtInsert.setString(13, tranId);
								pstmtInsert.setString(14, lineNo);
								pstmtInsert.setString(15, lotNoRcp);
								pstmtInsert.setString(16, lotSl);
								pstmtInsert.setTimestamp(17, tranDate);
								pstmtInsert.setString(18, userId);
								pstmtInsert.setString(19, termId);
								pstmtInsert.setString(20, locCode);
								pstmtInsert.setDouble(21, qtySample);
								pstmtInsert.setString(22, "U");
								pstmtInsert.setString(23, unit);
								pstmtInsert.setString(24, "A");
								pstmtInsert.setString(25, batchNo);
								pstmtInsert.setTimestamp(26, expiryDate);
								pstmtInsert.setString(27, aprv);
								pstmtInsert.setString(28, rej);
								pstmtInsert.setString(29, unit);
								pstmtInsert.setString(30, lotNoRcp);
								pstmtInsert.setTimestamp(31, retestDt);
								pstmtInsert.setString(32, empCodeQcaprv);
								pstmtInsert.setString(33, spec);
								pstmtInsert.setString(34, itemCode);
								pstmtInsert.setTimestamp(35, mfgDate);
								pstmtInsert.setString(36, emp);
								pstmtInsert.setString(37, suppCode); // added by
								// Kunal
								// on
								// 8/11/12
								// as
								// per
								// Pravin
								// Sali
								// intruction
								pstmtInsert.setString(38, suppCodeMnfr);// added
								// by
								// Kunal
								// on
								// 8/11/12
								// as
								// per
								// Pravin
								// Sali
								// intruction
								updCnt = pstmtInsert.executeUpdate();
								pstmtInsert.close();
								pstmtInsert = null;

								if ("Y".equalsIgnoreCase(genLotAuto) || "M".equalsIgnoreCase(genLotAuto))
								{
									if (!pordType.equals(jobWorkType) && !pordType.equals(subContractType))
									{
										// retString =
										// gbf_updlotnowithqcno(as_tranid,ls_itemcode,ls_lineno,ls_lotno_rcp,ls_qcno)

										//Changed by Jagruti Shinde Request id:[W16CSUN009][Start]
										//Updated to add Autogenerated Lot No in porcpdet
										updateLotNo(lotNoRcp,tranId, itemCode, locCode, lineNo,  conn) ;
										//Changed by Jagruti Shinde Request id:[W16CSUN009][End]
										if (retString != null && retString.trim().length() > 0)
										{
											break;
										}
									} else
									{
										if ("Y".equals(genLotSubctr))
										{
											lotNoRcp = qcNo;
											// retString =
											// gbf_updlotnowithqcno(as_tranid,ls_itemcode,ls_lineno,ls_lotno_rcp,ls_qcno)

											//Changed by Jagruti Shinde Request id:[W16CSUN009][Start]
											//Updated to add Autogenerated Lot No in porcpdet
											updateLotNo(lotNoRcp,tranId, itemCode, locCode, lineNo,  conn) ;
											//Changed by Jagruti Shinde Request id:[W16CSUN009][End]
											if (retString != null && retString.trim().length() > 0)
											{
												break;
											}
										}
									}
								}
								qcRejectedList = new ArrayList();
								//Modified by Anjali R. on [17/01/2019][As per KB sir suggested,QC_ORDER_LOTS entry should be inserted only in sampling for lot sl type(S)][Start]
								int rowcount = InsertQcOrderLots(qcNo, tranId, itemCode, unit, lotNoRcp, aprv, batchNo, spec, today, mfgDate, expiryDate, conn);
								//Modified by Anjali R. on [17/01/2019][As per KB sir suggested,QC_ORDER_LOTS entry should be inserted only in sampling for lot sl type(S)][End]

								//Changed by Jagruti Shinde Request id:[W16CSUN009][Start]
								if(qcLockDsp.length() != 0 &&  !qcLockDsp.equalsIgnoreCase("")) 
								{
									HashMap hashMapQc = new HashMap();
									hashMapQc.put("site_code", siteCode);
									hashMapQc.put("item_code", itemCode);
									hashMapQc.put("lot_no", lotNoRcp);
									hashMapQc.put("lot_sl", lotSl);
									hashMapQc.put("loc_code", locCode);
									hashMapQc.put("line_no", lineNo);
									qcRejectedList.add(hashMapQc);
									System.out.println("qcRejectedList for qcType S && qcReqdSite Y::" + qcRejectedList);
									System.out.println("qcRejectedList size for qcType S && qcReqdSite Y::" + qcRejectedList.size());
									retString = invHoldGen.generateHoldTrans(qcLockDsp, tranId, "P-RCP", siteCode, qcRejectedList, xtraParams, conn);
									qcRejectedList.clear();
								}
								//Changed by Jagruti Shinde Request id:[W16CSUN009][End]
							}
							// ls_qord_no[upperbound(ls_qord_no[]) + 1] =
							// ls_qcno;
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;
						if (retString != null && retString.trim().length() > 0)
						{
							break;
						}
					} else if ("L".equalsIgnoreCase(qcType) && "Y".equalsIgnoreCase(qcReqdSite))
					{

						System.out.println(">>>>>>>In L And Y <<<<<<<<<<=" + qcReqdSite);
						sql = "SELECT (CASE WHEN BATCH_NO IS NULL THEN ' ' ELSE BATCH_NO END) AS BATCH_NO, " + " LOC_CODE, UNIT__STD, " + " CASE WHEN (SUM(CASE WHEN (((CASE WHEN QUANTITY IS NULL THEN 0 ELSE QUANTITY END) + " + " (CASE WHEN EXCESS_SHORT_QTY IS NULL THEN 0 ELSE EXCESS_SHORT_QTY END)) * " + " (CASE WHEN CONV__QTY_STDUOM IS NULL THEN 1 ELSE CONV__QTY_STDUOM END)) IS NULL " + " THEN 0 ELSE (((CASE WHEN QUANTITY IS NULL THEN 0 ELSE QUANTITY END) + " + " ( CASE WHEN EXCESS_SHORT_QTY IS NULL THEN 0 ELSE EXCESS_SHORT_QTY END)) * " // bug
								// fixing
								// done
								// by
								// kunal
								// on
								// 29/10/12
								// add
								// bracket
								+ " (CASE WHEN CONV__QTY_STDUOM IS NULL THEN 1 ELSE CONV__QTY_STDUOM END)) END)) IS NULL THEN 0" + " ELSE (SUM(CASE WHEN (((CASE WHEN QUANTITY IS NULL THEN 0 ELSE QUANTITY END) + " + " (CASE WHEN EXCESS_SHORT_QTY IS NULL THEN 0 ELSE EXCESS_SHORT_QTY END)) * " + " (CASE WHEN CONV__QTY_STDUOM IS NULL THEN 1 ELSE CONV__QTY_STDUOM END)) IS NULL THEN " + " 0 ELSE ( ((CASE WHEN QUANTITY IS NULL THEN 0 ELSE QUANTITY END) + " // bug
								// fixing
								// done
								// by
								// kunal
								// on
								// 29/10/12
								// add
								// bracket
								+ " (CASE WHEN EXCESS_SHORT_QTY IS NULL THEN 0 ELSE EXCESS_SHORT_QTY END)) * " + " (CASE WHEN CONV__QTY_STDUOM IS NULL THEN 1 ELSE CONV__QTY_STDUOM END))END)) END, " + " MIN(LINE_NO), MFG_DATE ,LOT_NO, " + " ( CASE WHEN SPEC_REF IS NULL THEN ' ' ELSE SPEC_REF END), " + " sum(case when excess_short_qty is null then 0 else excess_short_qty end) as excess_short_qty ,sum((case when quantity__stduom is null then 0 else quantity__stduom end)) as quantity__stduom,supp_code__mnfr " // added
								// by
								// kunal
								// on
								// 5/11/12
								+ " , sum(case when no_art is null then 0 else no_art end) as no_art," // stary
								// added
								// by
								// chandrashekar
								// on
								// 06-01-2015
								+ " sum(case when net_weight is null then 0 else net_weight end) as net_weight"// stary
								// added
								// by
								// chandrashekar
								// on
								// 06-01-2015
								+ " ,expiry_date " + " FROM PORCPDET  WHERE TRAN_ID = ? " + " AND ITEM_CODE = ? " + " GROUP BY (CASE WHEN BATCH_NO IS NULL THEN ' ' ELSE BATCH_NO END), " + " LOC_CODE, UNIT__STD, (CASE WHEN SPEC_REF IS NULL THEN ' ' ELSE SPEC_REF END), " + " MFG_DATE ,expiry_date,LOT_NO,supp_code__mnfr  " + " ORDER BY MIN(LINE_NO) ASC  ";
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1, tranId);
						pstmt1.setString(2, itemCode);
						rs1 = pstmt1.executeQuery();
						while (rs1.next())
						{							
							lotNoRcpOld = "               "; //Pavan Rane 16jul19
							batchNo = rs1.getString(1);
							locCode = rs1.getString(2);
							unit = rs1.getString(3);
							qty = rs1.getDouble(4);
							lineNo = checkNull(rs1.getString(5));

							spec = rs1.getString(8);
							mfgDate = rs1.getTimestamp(6);
							lotNoRcp = rs1.getString(7);
							lotNoRcpOld = lotNoRcp;  //Pavan Rane 16jul19
							// added by kunal on 5/11/12 start as per Pravin
							// Sali Sir instruction
							excessShortQty = rs1.getDouble("excess_short_qty"); // added
							// by
							// Kunal
							// on
							// 5/11/12
							quantityStduom = rs1.getDouble("quantity__stduom"); // added
							// by
							// Kunal
							// on
							// 5/11/12
							expiryDate = rs1.getTimestamp("expiry_date");
							System.out.println("excessShortQt =" + excessShortQty);
							System.out.println("quantityStduom =" + quantityStduom);
							if (excessShortQty == 0)
							{
								System.out.println("qty get from qtystduom");
								qty = quantityStduom;
							}
							// added by kunal on 5/11/12 end as per Pravin Sali
							// Sir instruction
							suppCodeMnfr = rs1.getString("supp_code__mnfr");// added
							// by
							// Kunal
							// on
							// 8/11/12
							System.out.println("suppCodeMnfr = " + suppCodeMnfr);							
							if(suppCodeMnfr == null || suppCodeMnfr.trim().length()==0 || "null".equals(suppCodeMnfr))
							{
								suppCodeMnfr= " ";
								System.out.println("suppCodeMnfr---2 = " + suppCodeMnfr);
							}							
							//Changed by Jagruti Shinde Req id:[W16CSUN009]
							//sql="SELECT PURC_ORDER, LINE_NO__ORD"
							sql = "SELECT PURC_ORDER, LINE_NO__ORD , LOT_SL ,LINE_NO "
									// +
									// ", EXPIRY_DATE	, LOT_SL , LINE_NO, MFG_DATE"//Changed
									// by manoj td 21/01/2015
									+ " FROM PORCPDET WHERE TRAN_ID = ? AND ITEM_CODE = ? AND LOC_CODE = ? AND (CASE WHEN BATCH_NO IS NULL THEN ' ' ELSE               BATCH_NO END) = ?";
							pstmt2 = conn.prepareStatement(sql);
							pstmt2.setString(1, tranId);
							pstmt2.setString(2, itemCode);
							pstmt2.setString(3, locCode);
							pstmt2.setString(4, batchNo);
							rs2 = pstmt2.executeQuery();
							if (rs2.next())
							{
								purcOrder = checkNull(rs2.getString("PURC_ORDER"));
								lineNoOrd = checkNull(rs2.getString("LINE_NO__ORD"));
								// expiryDate = rs2.getTimestamp("EXPIRY_DATE");
								//Changed by Jagruti Shinde Req id:[W16CSUN009]
								lotSl = checkNull(rs2.getString("LOT_SL"));
								lineNoP = checkNull(rs2.getString("LINE_NO"));
								// rcpLine =
								// checkNull(rs2.getString("LINE_NO"));
								// mfgDate = rs2.getTimestamp("MFG_DATE");
							}
							pstmt2.close();
							pstmt2 = null;
							rs2.close();
							rs2 = null;

							sql = "SELECT EMP_CODE__QCAPRV FROM PORDDET WHERE PURC_ORDER = ? AND LINE_NO = ?";
							pstmt2 = conn.prepareStatement(sql);
							pstmt2.setString(1, purcOrder);
							pstmt2.setString(2, lineNoOrd);
							rs2 = pstmt2.executeQuery();
							if (rs2.next())
							{
								empCodeQcaprv = checkNull(rs2.getString("EMP_CODE__QCAPRV"));
							}
							pstmt2.close();
							pstmt2 = null;
							rs2.close();
							rs2 = null;

							poRcpDetSpec = spec;

							xmlValues = "";
							xmlValues = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
							xmlValues = xmlValues + "<Header></Header>";
							xmlValues = xmlValues + "<Detail1>";
							xmlValues = xmlValues + "<qorder_no></qorder_no>";
							xmlValues = xmlValues + "<site_code>" + siteCode + "</site_code>";
							xmlValues = xmlValues + "<qorder_date>" + getCurrdateAppFormat() + "</qorder_date>";
							// xmlValues = xmlValues +
							// "<qorder_type>I</qorder_type>";
							xmlValues = xmlValues + "<qorder_type>" + qcOrdType + "</qorder_type>";// added
							// by
							// sagar
							// on
							// 10/07/14
							xmlValues = xmlValues + "<lot_no>" + lotNoRcp + "</lot_no>";
							xmlValues = xmlValues + "<item_ser>" + itemSer + "</item_ser>";
							xmlValues = xmlValues + "<porcp_no>" + tranId + "</porcp_no>";
							// xmlValues = xmlValues + "<porcp_line_no>" +
							// rcpLine + "</porcp_line_no>";
							xmlValues = xmlValues + "</Detail1></Root>";
							System.out.println("xmlValues  :[" + xmlValues + "]");
							TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", commonConstants.DB_NAME);
							qcNo = tg.generateTranSeqID("QC-ORD", "qorder_no", key, conn);
							System.out.println("qcNo2=" + qcNo);

							if ("ERROR".equals(qcNo))
							{
								retString = itmDBAccessLocal.getErrorString("", "VTTRANID", "","",conn);
								break;
							}

							if (retString.trim().length() > 0)
							{
								break;
							} else
							{
								sql = "SELECT LOC_CODE__APRV, LOC_CODE__REJ FROM SITEITEM WHERE  SITE_CODE = ?  AND  ITEM_CODE = ? ";
								pstmt2 = conn.prepareStatement(sql);
								pstmt2.setString(1, siteCode);
								pstmt2.setString(2, itemCode);
								rs2 = pstmt2.executeQuery();
								if (rs2.next())
								{
									aprv = checkNull(rs2.getString("LOC_CODE__APRV"));
									rej = checkNull(rs2.getString("LOC_CODE__REJ"));
								}
								pstmt2.close();
								pstmt2 = null;
								rs2.close();
								rs2 = null;

								if (!"Y".equals(useSuppLot)) // Condition added
									// by sagar on
									// 11/07/14
								{
									System.out.println(">>>>>>>in L & Y if useSuppLot is not Y:" + useSuppLot);
									if ("Y".equalsIgnoreCase(genLotAuto) || "M".equalsIgnoreCase(genLotAuto))
									{
										// if ( (pordType != jobWorkType) && (
										// pordType != subContractType) )
										// changes done by priyanka on 15/11/14
										if ((!pordType.equals(jobWorkType) && !pordType.equals(subContractType)))
										{
											lotNoRcp = qcNo;
										} else
										{
											if (genLotSubctr.equalsIgnoreCase("Y"))
											{
												lotNoRcp = qcNo;
											}
										}
									}
								}
								passedQty = qty - qtySample;

								if (spec == null || spec.length() == 0)
								{
									sql = "SELECT (CASE WHEN SPEC_REF IS NULL THEN ' ' ELSE SPEC_REF END) AS SPEC_REF FROM SITEITEM WHERE			   SITE_CODE = ? AND    ITEM_CODE = ? ";
									pstmt2 = conn.prepareStatement(sql);
									pstmt2.setString(1, siteCode);
									pstmt2.setString(2, itemCode);
									rs2 = pstmt2.executeQuery();
									if (rs2.next())
									{
										spec = checkNull(rs2.getString(1));
									}
									pstmt2.close();
									pstmt2 = null;
									rs2.close();
									rs2 = null;
								}

								if (dbName.equalsIgnoreCase("db2"))
								{
									if (itemCode.length() == 0)
									{
										itemCode = null;
									}
									if (siteCode.trim().length() == 0)
									{
										siteCode = null;
									}
									if (locCode.length() == 0)
									{
										locCode = null;
									}
									if (aprv.length() == 0)
									{
										aprv = null;
									}
									if (rej.length() == 0)
									{
										rej = null;
									}
									if (empCodeQcaprv.length() == 0)
									{
										empCodeQcaprv = null;
									}
									if (unit.length() == 0)
									{
										unit = null;
									}
									if (qcNo.length() == 0)
									{
										qcNo = null;
									}
								}

								sql = "INSERT INTO QC_ORDER( QORDER_NO, QORDER_TYPE, QORDER_DATE, SITE_CODE ,ITEM_CODE, ROUTE_CODE,		   QUANTITY, QTY_PASSED ," + " QTY_REJECTED	, START_DATE , DUE_DATE	, PORCP_NO ,  LOT_NO,	LOT_SL , CHG_DATE ,	CHG_USER, CHG_TERM	," + " LOC_CODE,	QTY_SAMPLE, STATUS,	UNIT, QC_CREATE_TYPE ,  BATCH_NO, EXPIRY_DATE, " + " LOC_CODE__APRV , LOC_CODE__REJ , UNIT__SAMPLE, LOT_NO__NEW ,	RETEST_DATE  ,	EMP_CODE__QCAPRV, " + " SPEC_REF, ITEM_CODE__NEW , MFG_DATE, EMP_CODE,SUPP_CODE,SUPP_CODE__MFG ) " + " VALUES(  ?,?,?,?,?,?,?,? ,?	," // bug
										// fixing
										// done
										// by
										// kunal
										// on
										// 29/10/12
										+ " ?,?,?,?,?,?,?,?,?,?,?,?,?,?," + " ?,?,?,?,?,?,?,?,?,?,?,?,? )";
								pstmtInsert = conn.prepareStatement(sql);
								pstmtInsert.setString(1, qcNo);
								// pstmtInsert.setString(2, "I");
								pstmtInsert.setString(2, qcOrdType); // added by
								// sagar
								// on
								// 10/07/14
								pstmtInsert.setTimestamp(3, tranDate);
								pstmtInsert.setString(4, siteCode);
								pstmtInsert.setString(5, itemCode);
								pstmtInsert.setString(6, null);

								qty = getUnroundDecimal(qty, 3);

								pstmtInsert.setDouble(7, qty);
								double qtypass = qty - qtySample;
								// pstmtInsert.setDouble(8, qty - qtySample);
								pstmtInsert.setDouble(8, qtypass);
								pstmtInsert.setDouble(9, 0);
								pstmtInsert.setTimestamp(10, tranDate);
								pstmtInsert.setTimestamp(11, qcDueDate);
								pstmtInsert.setString(12, tranId);
								// pstmtInsert.setString(13, rcpLine);
								pstmtInsert.setString(13, lotNoRcp);
								// pstmtInsert.setString(15, lotSl);
								pstmtInsert.setString(14, null);// start added
								// by
								// chandrashekar
								// on 12-12-14
								pstmtInsert.setTimestamp(15, tranDate);
								pstmtInsert.setString(16, userId);
								pstmtInsert.setString(17, termId);
								pstmtInsert.setString(18, locCode);
								pstmtInsert.setDouble(19, qtySample);
								pstmtInsert.setString(20, "U");
								pstmtInsert.setString(21, unit);
								pstmtInsert.setString(22, "A");
								pstmtInsert.setString(23, batchNo);
								pstmtInsert.setTimestamp(24, expiryDate);
								pstmtInsert.setString(25, aprv);
								pstmtInsert.setString(26, rej);
								pstmtInsert.setString(27, unit);
								pstmtInsert.setString(28, lotNoRcp);
								pstmtInsert.setTimestamp(29, retestDt);
								pstmtInsert.setString(30, empCodeQcaprv);
								pstmtInsert.setString(31, spec);
								pstmtInsert.setString(32, itemCode);
								pstmtInsert.setTimestamp(33, mfgDate);
								pstmtInsert.setString(34, emp);
								pstmtInsert.setString(35, suppCode); // added by
								// Kunal
								// on
								// 8/11/12
								// as
								// per
								// Pravin
								// Sali
								// intruction
								pstmtInsert.setString(36, suppCodeMnfr);// added
								// by
								// Kunal
								// on
								// 8/11/12
								// as
								// per
								// Pravin
								// Sali
								// intruction
								updCnt = pstmtInsert.executeUpdate();
								pstmtInsert.close();
								pstmtInsert = null;
								// start added by chandrashekar on 08-01-2015
								noArt = rs1.getDouble("no_art");
								netWeight = rs1.getDouble("net_weight");

								apprLocLogic = distCommon.getDisparams("999999", "APR_LOC_LOGIC", conn);
								apprLocUdf = distCommon.getDisparams("999999", "APR_LOC_UDF", conn);
								apprLocUdfInput= distCommon.getDisparams("999999", "APR_LOC_UDF_INPUTS", conn);

								System.out.println("apprLocLogic:::[" + apprLocLogic + "]apprLocUdf[" + apprLocUdf + "]apprLocUdfINPUT["+apprLocUdfInput+"]");
								if (apprLocLogic.equalsIgnoreCase("NULLFOUND") || apprLocLogic.trim().length() == 0)
								{
									retString = itmDBAccessLocal.getErrorString("", "VTLOCLOG", "","",conn);
								}
								if ("U".equalsIgnoreCase(apprLocLogic))
								{
									if (apprLocUdf.equalsIgnoreCase("NULLFOUND") || apprLocUdf.trim().length() == 0)
									{
										retString = itmDBAccessLocal.getErrorString("", "VTLOCUDF", "","",conn);
									}
								}
								if ("I".equalsIgnoreCase(apprLocLogic))
								{
									sql = "select loc_code from item where item_code = ? ";
									pstmt2 = conn.prepareStatement(sql);
									pstmt2.setString(1, itemCode);
									rs2 = pstmt2.executeQuery();
									if (rs2.next())
									{
										qcLotLocCode = checkNull(rs2.getString("loc_code"));
									}
									pstmt2.close();
									pstmt2 = null;
									rs2.close();
									rs2 = null;
								} else if ("S".equalsIgnoreCase(apprLocLogic))
								{
									sql = "select loc_code__aprv from siteitem where item_code = ?  and site_code = ? ";
									pstmt2 = conn.prepareStatement(sql);
									pstmt2.setString(1, itemCode);
									pstmt2.setString(2, siteCode);
									rs2 = pstmt2.executeQuery();
									if (rs2.next())
									{
										qcLotLocCode = checkNull(rs2.getString("loc_code__aprv"));
									}
									pstmt2.close();
									pstmt2 = null;
									rs2.close();
									rs2 = null;

								} 

								else if (("U".equalsIgnoreCase(apprLocLogic)) && apprLocUdf != null && apprLocUdf.trim().length() > 0)
								{
									System.out.println("ApprLocationUdfinput Before"+ apprLocUdfInput);

									if(apprLocUdfInput != null && apprLocUdfInput.trim().length() > 0 && !(apprLocUdfInput.equalsIgnoreCase("NULLFOUND")))
									{
										System.out.println("ApprLocationUdfinput"+ apprLocUdfInput);
										String lotNoIssueOut = "";
										String lotNoIssueOut1 = "";
										String arrStr[] = apprLocUdfInput.split(",");
										int len =arrStr.length;
										for(int i =0;i<len;i++)
										{
											System.out.println("arrStr[i]"+arrStr[i]);
											if(arrStr[i].equals("LOC_CODE__ISS"))
											{
												lotNoIssueOut1=locCode;
												System.out.println("Orignal Value of lOcation Code is"+locCode);
												System.out.println("Value of lOcation Code is"+lotNoIssueOut1);
											}
											if(arrStr[i].equals("SITE_CODE"))
											{
												lotNoIssueOut1=siteCode;
												System.out.println("Orignal Value of Site Code is"+siteCode);
												System.out.println("Value of Site Code is"+lotNoIssueOut1);
											}
											if(arrStr[i].equals("ITEM_CODE"))
											{
												lotNoIssueOut1=itemCode;
												System.out.println("Orignal Value of itemCOde Code is"+itemCode);
												System.out.println("Value of itemCOde Code is"+lotNoIssueOut1);
											}
											lotNoIssueOut=lotNoIssueOut + "'"+lotNoIssueOut1+"',";
										}
										lotNoIssueOut =lotNoIssueOut.substring(0,lotNoIssueOut.length()-1);
										System.out.println("Value is------>" + lotNoIssueOut);
										sql = "select " + apprLocUdf + "("+lotNoIssueOut + ") from dual";
										pstmt2 = conn.prepareStatement(sql);
										rs2 = pstmt2.executeQuery();
										if (rs2.next())
										{
											qcLotLocCode = rs2.getString(1);
										}
										pstmt2.close();
										pstmt2 = null;
										rs2.close();
										rs2 = null;
										//	}
										System.out.println("qcLotLocCode"+qcLotLocCode);
									}
									else
									{

										sql = "select " + apprLocUdf + "('" + locCode + "') from dual";
										pstmt2 = conn.prepareStatement(sql);
										rs2 = pstmt2.executeQuery();
										if (rs2.next())
										{
											qcLotLocCode = rs2.getString(1);
										}
										pstmt2.close();
										pstmt2 = null;
										rs2.close();
										rs2 = null;
										System.out.println("qcLotLocCode Else ############"+qcLotLocCode);
									}
								}


								// int
								// rowcount=InsertQcOrderLots(qcNo,tranId,itemCode,unit,lotNoRcp,qcLotLocCode,conn);

								/*
								 * sql =
								 * " Insert into qc_order_lots (QC_ORDER,LINE_NO,ITEM_CODE,LOT_NO,LOT_SL,LOC_CODE,QUANTITY,"
								 * +
								 * "UNIT,SAMPLE_QTY,LOCTYPE,LOC_CODE__ISSUE,QORDER_NO,NO_ART,NET_WEIGHT) "
								 * + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
								 * pstmtInsert = conn.prepareStatement(sql);
								 * pstmtInsert.setString(1, qcNo);
								 * pstmtInsert.setString(2, rcpLine);
								 * pstmtInsert.setString(3, itemCode);
								 * pstmtInsert.setString(4, lotNoRcp);
								 * pstmtInsert.setString(5, lotSl);
								 * //pstmtInsert.setString(6, locCode);
								 * pstmtInsert.setString(6, qcLotLocCode); qty =
								 * getUnroundDecimal(qty,3);
								 * pstmtInsert.setDouble(7, qty);
								 * pstmtInsert.setString(8, unit);
								 * pstmtInsert.setDouble(9, qtySample);
								 * pstmtInsert.setString(10, "A");
								 * pstmtInsert.setString(11, locCode);
								 * pstmtInsert.setString(12, qcNo);
								 * pstmtInsert.setDouble(13, noArt);
								 * pstmtInsert.setDouble(14, netWeight); updCnt1
								 * = pstmtInsert.executeUpdate();
								 * pstmtInsert.close(); pstmtInsert = null;
								 */

								System.out.println("qc_order_lots insert count" + updCnt1);
								// end added by chandrashekar on 08-01-2015

								spec = poRcpDetSpec;

								if ("Y".equalsIgnoreCase(genLotAuto) || "M".equalsIgnoreCase(genLotAuto))
								{
									if (mfgDate == null)
									{
										// mfgDate = today;
										mfgDate = newsysDate;
									}
									//Added By PriyankaC on 20july2019 as per discuss with SM sir.
									if (lotNoRcpOld == null)
									{

										lotNoRcpOld = "           ";
									}
									//Added By PriyankaC on 20July2019 as per discuss with SM sir


									if (!pordType.equals(jobWorkType) && !pordType.equals(subContractType))
									{

										System.out.println("Enter if 11111111111");
										// sql =
										// "UPDATE PORCPDET SET 	LOT_NO = ?, LOT_SL = ? WHERE TRAN_ID = ? AND ITEM_CODE = ? AND		   LOC_CODE  = ? AND (CASE WHEN BATCH_NO IS NULL THEN ' ' ELSE BATCH_NO END) = ? AND (CASE		   WHEN SPEC_REF IS NULL THEN ' ' ELSE SPEC_REF END)	= ? AND (CASE WHEN MFG_DATE IS NULL		   THEN ? ELSE MFG_DATE END) = ? ";
										//Pavan R on 25jun19 start [grouping done in where on updating lot_no in porcpdet as per same as get data]
										//sql = "UPDATE PORCPDET SET 	LOT_NO = ? WHERE TRAN_ID = ? AND ITEM_CODE = ? AND		   LOC_CODE  = ? AND (CASE WHEN BATCH_NO IS NULL THEN ' ' ELSE BATCH_NO END) = ? AND (CASE		   WHEN SPEC_REF IS NULL THEN ' ' ELSE SPEC_REF END)	= ? AND (CASE WHEN MFG_DATE IS NULL		   THEN ? ELSE MFG_DATE END) = ? ";
										sql = "UPDATE PORCPDET SET LOT_NO = ? WHERE TRAN_ID = ? AND ITEM_CODE = ? AND (CASE WHEN LOT_NO IS NULL THEN '               ' ELSE LOT_NO END) = ? AND (CASE WHEN BATCH_NO IS NULL THEN ' ' ELSE BATCH_NO END) = ? AND LOC_CODE  = ? AND UNIT__STD = ? AND (CASE WHEN SPEC_REF IS NULL THEN ' ' ELSE SPEC_REF END) = ? AND (CASE WHEN MFG_DATE IS NULL THEN ? ELSE MFG_DATE END) = ? AND EXPIRY_DATE = ? AND (CASE WHEN SUPP_CODE__MNFR IS NULL THEN ' ' ELSE SUPP_CODE__MNFR END) = ? ";
										//Pavan R on 25jun19 end
										pstmtUpd = conn.prepareStatement(sql);
										pstmtUpd.setString(1, lotNoRcp);
										// pstmtUpd.setString(2,
										// lotSl);//commented by chandrashekar
										// on 12-12-14
										pstmtUpd.setString(2, tranId);
										pstmtUpd.setString(3, itemCode);										
										pstmtUpd.setString(4, lotNoRcpOld); //Pavan Rane 16jul19[to update record based on lotwise group in porcpdet]																				
										/*pstmtUpd.setString(4, locCode);
										pstmtUpd.setString(5, batchNo);
										pstmtUpd.setString(6, spec);
										pstmtUpd.setTimestamp(7, today);
										// pstmtUpd.setTimestamp(7, newsysDate);
										pstmtUpd.setTimestamp(8, mfgDate);*/
										//Pavan R on 25jun19 start [grouping done in where on updating lot_no in porcpdet as per same as get data]
										pstmtUpd.setString(5, batchNo);
										pstmtUpd.setString(6, locCode);
										pstmtUpd.setString(7, unit);
										pstmtUpd.setString(8, spec);
										pstmtUpd.setTimestamp(9, today);
										pstmtUpd.setTimestamp(10, mfgDate);
										pstmtUpd.setTimestamp(11, expiryDate);
										pstmtUpd.setString(12, suppCodeMnfr);
										//Pavan R on 25jun19 end
										updCnt = pstmtUpd.executeUpdate();
										// if ( updCnt != 1 )
										System.out.println("Porcpdet:1:updCnt["+updCnt+"]");
										if (updCnt == 0)// Condition Added by
											// Sagar on 20/OCT/14
										{
											retString = itmDBAccessLocal.getErrorString("", "DS000NR", "","",conn);// Added
											// by
											// chandrashekar
											// on
											// 17-sep-2014
										}
										pstmtUpd.close();
										pstmtUpd = null;
									} else
									{
										System.out.println("Enter else 11111111111");
										if (genLotSubctr.equalsIgnoreCase("Y"))
										{
											System.out.println("Enter else  if 222222222");
											// sql =
											// "UPDATE PORCPDET SET   LOT_NO = ?, LOT_SL = ? WHERE TRAN_ID = ? AND ITEM_CODE = ? AND		   LOC_CODE  = ? AND (CASE WHEN BATCH_NO IS NULL THEN ' ' ELSE BATCH_NO END) = ? AND		   (CASE WHEN SPEC_REF IS NULL THEN ' ' ELSE SPEC_REF END)	= ?	 AND (CASE WHEN			   MFG_DATE IS NULL THEN ? ELSE MFG_DATE END) 	= ?";
											//Pavan R on 25jun19 start [grouping done in where on updating lot_no in porcpdet as per same as get data]
											//sql = "UPDATE PORCPDET SET   LOT_NO = ? WHERE TRAN_ID = ? AND ITEM_CODE = ? AND		   LOC_CODE  = ? AND (CASE WHEN BATCH_NO IS NULL THEN ' ' ELSE BATCH_NO END) = ? AND		   (CASE WHEN SPEC_REF IS NULL THEN ' ' ELSE SPEC_REF END)	= ?	 AND (CASE WHEN	 MFG_DATE IS NULL THEN ? ELSE MFG_DATE END) 	= ? AND (CASE WHEN	 EXPIRY_DATE IS NULL THEN ? ELSE EXPIRY_DATE END) 	= ? ";
											sql = "UPDATE PORCPDET SET LOT_NO = ? WHERE TRAN_ID = ? AND ITEM_CODE = ? AND (CASE WHEN LOT_NO IS NULL THEN '               ' ELSE LOT_NO END) = ? AND (CASE WHEN BATCH_NO IS NULL THEN ' ' ELSE BATCH_NO END) = ? AND LOC_CODE  = ? AND UNIT__STD = ? AND (CASE WHEN SPEC_REF IS NULL THEN ' ' ELSE SPEC_REF END) = ? AND (CASE WHEN MFG_DATE IS NULL THEN ? ELSE MFG_DATE END) = ? AND EXPIRY_DATE = ? AND (CASE WHEN SUPP_CODE__MNFR IS NULL THEN ' ' ELSE SUPP_CODE__MNFR END) = ? ";
											//Pavan R on 25jun19 end
											pstmtUpd = conn.prepareStatement(sql);
											pstmtUpd.setString(1, lotNoRcp);
											// pstmtUpd.setString(2,
											// lotSl);//commented by
											// chandrashekar on 12-12-14
											pstmtUpd.setString(2, tranId);
											pstmtUpd.setString(3, itemCode);
											pstmtUpd.setString(4, lotNoRcpOld);										
											/*pstmtUpd.setString(4, locCode);
											pstmtUpd.setString(5, batchNo);
											pstmtUpd.setString(6, spec);
											pstmtUpd.setTimestamp(7, today);
											// pstmtUpd.setTimestamp(7,
											// newsysDate);
											pstmtUpd.setTimestamp(8, mfgDate);
											pstmtUpd.setTimestamp(9, today);
											pstmtUpd.setTimestamp(10, expiryDate);*/
											//Pavan R on 25jun19 start [grouping done in where on updating lot_no in porcpdet as per same as get data]
											pstmtUpd.setString(5, batchNo);
											pstmtUpd.setString(6, locCode);	
											pstmtUpd.setString(7, unit);
											pstmtUpd.setString(8, spec);
											pstmtUpd.setTimestamp(9, today);
											// pstmtUpd.setTimestamp(7,
											// newsysDate);
											pstmtUpd.setTimestamp(10, mfgDate);
											pstmtUpd.setTimestamp(11, today);
											pstmtUpd.setTimestamp(12, expiryDate);
											pstmtUpd.setString(13, suppCodeMnfr);
											//Pavan R on 25jun19 end
											updCnt = pstmtUpd.executeUpdate();
											System.out.println("Porcpdet:2:updCnt["+updCnt+"]");
											// if ( updCnt != 1 )
											if (updCnt == 0)// Condition Added
												// by Sagar on
												// 20/OCT/14
											{
												retString = itmDBAccessLocal.getErrorString("", "DS000NR", "","",conn);// Added
												// by
												// chandrashekar
												// on
												// 17-sep-2014
											}
											pstmtUpd.close();
											pstmtUpd = null;
										}
									}
								}
								// added by priyanka
								//Commented and added by Varsha V on 12-11-18 to set loc_code__aprv of qc_order in qc_order_lots
								//int rowcount = InsertQcOrderLots(qcNo, tranId, itemCode, unit, lotNoRcp, qcLotLocCode, batchNo, spec, today, mfgDate, expiryDate, conn);

								//Modified by Anjali R. on [17/01/2019][As per KB sir suggested,QC_ORDER_LOTS entry should be inserted only in sampling for lot sl type][Start]
								//int rowcount = InsertQcOrderLots(qcNo, tranId, itemCode, unit, lotNoRcp, aprv, batchNo, spec, today, mfgDate, expiryDate, conn);
								//Modified by Anjali R. on [17/01/2019][As per KB sir suggested,QC_ORDER_LOTS entry should be inserted only in sampling for lot sl type][End]

								qcRejectedList = new ArrayList();
								//Changed by Jagruti Shinde Request id:[W16CSUN009][Start]
								if(qcLockDsp.length() != 0 &&  !qcLockDsp.equalsIgnoreCase(""))
								{

									HashMap hashMapQc = new HashMap();
									hashMapQc.put("site_code", siteCode);
									hashMapQc.put("item_code", itemCode);
									hashMapQc.put("lot_no", lotNoRcp);
									hashMapQc.put("lot_sl", lotSl);
									hashMapQc.put("loc_code", locCode);
									hashMapQc.put("line_no", lineNoP);
									qcRejectedList.add(hashMapQc);
									System.out.println("qcRejectedList for qcType L && qcReqdSite Y::" + qcRejectedList);
									System.out.println("qcRejectedList size for qcType L && qcReqdSite Y::" + qcRejectedList.size());
									retString = invHoldGen.generateHoldTrans(qcLockDsp, tranId, "P-RCP", siteCode, qcRejectedList, xtraParams, conn);
									qcRejectedList.clear();
								}
								//Changed by Jagruti Shinde Request id:[W16CSUN009][End]

							}
							// ls_qord_no[upperbound(ls_qord_no[]) + 1] =
							// ls_qcno;
						}
						pstmt1.close();
						pstmt1 = null;
						rs1.close();
						rs1 = null;
						if (retString != null && retString.trim().length() > 0)
						{
							break;
						}
					} else if ("I".equalsIgnoreCase(qcType) && "Y".equalsIgnoreCase(qcReqdSite))
					{
						System.out.println(">>>>>>>In I And Y <<<<<<<<<<=" + qcReqdSite);
						sql = "SELECT LOC_CODE , (CASE WHEN SPEC_REF IS NULL THEN ' ' ELSE SPEC_REF END) AS SPEC_REF, CASE WHEN (SUM(CASE WHEN		   (((CASE WHEN QUANTITY IS NULL THEN 0 ELSE QUANTITY END) + (CASE WHEN EXCESS_SHORT_QTY IS NULL THEN 0		   ELSE EXCESS_SHORT_QTY END)) * (CASE WHEN CONV__QTY_STDUOM IS NULL THEN 1 ELSE CONV__QTY_STDUOM END)) IS		   NULL THEN 0 ELSE (((CASE WHEN QUANTITY IS NULL THEN 0 ELSE QUANTITY END) + (CASE WHEN EXCESS_SHORT_QTY		   IS NULL THEN 0 ELSE EXCESS_SHORT_QTY END)) * (CASE WHEN CONV__QTY_STDUOM IS NULL THEN 1 ELSE				   CONV__QTY_STDUOM END)) END)) IS NULL THEN 0 ELSE (SUM(CASE WHEN (((CASE WHEN QUANTITY IS NULL THEN 0		   ELSE QUANTITY END) + (CASE WHEN EXCESS_SHORT_QTY IS NULL THEN 0 ELSE EXCESS_SHORT_QTY END)) * (CASE WHEN		   CONV__QTY_STDUOM IS NULL THEN 1 ELSE CONV__QTY_STDUOM END)) IS NULL THEN 0 ELSE (((CASE WHEN QUANTITY IS		   NULL THEN 0 ELSE QUANTITY END) + (CASE WHEN EXCESS_SHORT_QTY IS NULL THEN 0 ELSE EXCESS_SHORT_QTY END))		   * (CASE WHEN CONV__QTY_STDUOM IS NULL THEN 1 ELSE CONV__QTY_STDUOM END)) END)) END ,sum(case when excess_short_qty is null then 0 else excess_short_qty end) as excess_short_qty ,sum((case when quantity__stduom is null then 0 else quantity__stduom end)) as quantity__stduom ,supp_code__mnfr FROM PORCPDET WHERE		   TRAN_ID = ? AND ITEM_CODE = ? GROUP BY LOC_CODE , (CASE WHEN SPEC_REF IS NULL THEN ' ' ELSE SPEC_REF		   END),supp_code__mnfr ";
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1, tranId);
						pstmt1.setString(2, itemCode);
						rs1 = pstmt1.executeQuery();
						while (rs1.next())
						{
							locCode = checkNull(rs1.getString("LOC_CODE"));
							spec = checkNull(rs1.getString("SPEC_REF"));
							qty = rs1.getDouble(3);
							// added by kunal on 5/11/12 start as per Pravin
							// Sali Sir instruction
							excessShortQty = rs1.getDouble("excess_short_qty"); // added
							// by
							// Kunal
							// on
							// 5/11/12
							quantityStduom = rs1.getDouble("quantity__stduom"); // added
							// by
							// Kunal
							// on
							// 5/11/12
							if (excessShortQty == 0)
							{
								System.out.println("qty get from qty stduom");
								qty = quantityStduom;
							}
							// added by kunal on 5/11/12 end as per Pravin Sali
							// Sir instruction
							suppCodeMnfr = checkNull(rs1.getString("supp_code__mnfr"));// added
							// by
							// Kunal
							// on
							// 8/11/12
							System.out.println("suppCodeMnfr = " + suppCodeMnfr);
							// tranDate = getCurrdateAppFormat();
							xmlValues = "";
							xmlValues = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
							xmlValues = xmlValues + "<Header></Header>";
							xmlValues = xmlValues + "<Detail1>";
							xmlValues = xmlValues + "<qorder_no></qorder_no>";
							xmlValues = xmlValues + "<site_code>" + siteCode + "</site_code>";
							xmlValues = xmlValues + "<qorder_date>" + getCurrdateAppFormat() + "</qorder_date>";
							// xmlValues = xmlValues +
							// "<qorder_type>I</qorder_type>";
							xmlValues = xmlValues + "<qorder_type>" + qcOrdType + "</qorder_type>";// added
							// by
							// sagar
							// on
							// 10/07/14
							xmlValues = xmlValues + "<item_ser>" + itemSer + "</item_ser>";
							xmlValues = xmlValues + "<porcp_no>" + tranId + "</porcp_no>";
							xmlValues = xmlValues + "</Detail1></Root>";
							System.out.println("xmlValues  :[" + xmlValues + "]");
							TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", commonConstants.DB_NAME);
							qcNo = tg.generateTranSeqID("QC-ORD", "qorder_no", key, conn);
							System.out.println("qcNo3=" + qcNo);

							if ("ERROR".equals(qcNo))
							{
								retString = itmDBAccessLocal.getErrorString("", "VTTRANID", "","",conn);
								break;
							}
							if (retString.trim().length() == 0)
							{
								sql = "SELECT LOC_CODE__APRV , LOC_CODE__REJ FROM SITEITEM WHERE SITE_CODE = ? AND ITEM_CODE = ? ";
								pstmt2 = conn.prepareStatement(sql);
								pstmt2.setString(1, siteCode);
								pstmt2.setString(2, itemCode);
								rs2 = pstmt2.executeQuery();
								if (rs2.next())
								{
									aprv = checkNull(rs2.getString("LOC_CODE__APRV"));
									rej = checkNull(rs2.getString("LOC_CODE__REJ"));
								}
								rs2.close();
								rs2 = null;
								pstmt2.close();
								pstmt2 = null;

								sql = "SELECT UNIT FROM ITEM WHERE ITEM_CODE = ?";
								pstmt2 = conn.prepareStatement(sql);
								pstmt2.setString(1, itemCode);
								rs2 = pstmt2.executeQuery();
								if (rs2.next())
								{
									unit = checkNull(rs2.getString("UNIT"));
								}
								rs2.close();
								rs2 = null;
								pstmt2.close();
								pstmt2 = null;

								lineNo = null;
								// ls_lotno_rcp = space(15)
								// ls_lotsl = space(5)
								batchNo = null;
								expiryDate = null;
								retestDt = null;

								if (spec == null || spec.length() == 0)
								{
									sql = "SELECT (CASE WHEN SPEC_REF IS NULL THEN ' ' ELSE SPEC_REF END) AS SPEC_REF FROM   SITEITEM WHERE			  SITE_CODE = ? AND  ITEM_CODE = ? ";
									pstmt2 = conn.prepareStatement(sql);
									pstmt2.setString(1, siteCode);
									pstmt2.setString(2, itemCode);
									rs2 = pstmt2.executeQuery();
									if (rs2.next())
									{
										spec = checkNull(rs2.getString("SPEC_REF"));
									}
									rs2.close();
									rs2 = null;
									pstmt2.close();
									pstmt2 = null;
								}

								if (dbName.equalsIgnoreCase("db2"))
								{
									if (itemCode.length() == 0)
									{
										itemCode = null;
									}
									if (siteCode.length() == 0)
									{
										siteCode = null;
									}
									if (locCode.length() == 0)
									{
										locCode = null;
									}
									if (aprv.length() == 0)
									{
										aprv = null;
									}
									if (rej.length() == 0)
									{
										rej = null;
									}
									if (empCodeQcaprv.length() == 0)
									{
										empCodeQcaprv = null;
									}
									if (unit.length() == 0)
									{
										unit = null;
									}
									if (qcNo.length() == 0)
									{
										qcNo = null;
									}
								}

								sql = "INSERT INTO QC_ORDER (QORDER_NO,	QORDER_TYPE	, QORDER_DATE ,	SITE_CODE ,	ITEM_CODE, ROUTE_CODE		   , QUANTITY	, QTY_PASSED , QTY_REJECTED, START_DATE	, DUE_DATE,	REL_DATE, PORCP_NO	,			   PORCP_LINE_NO , LOT_NO	, LOT_SL , CHG_DATE	, CHG_USER , CHG_TERM ,	LOC_CODE , QTY_SAMPLE ,	         STATUS,	UNIT, QC_CREATE_TYPE , BATCH_NO	, EXPIRY_DATE , LOC_CODE__APRV , LOC_CODE__REJ ,             UNIT__SAMPLE ,	LOT_NO__NEW	, RETEST_DATE ,	SPEC_REF , ITEM_CODE__NEW ,	EMP_CODE,SUPP_CODE,SUPP_CODE__MFG) VALUES ( ?         ,?, ? , ? , ?, ? , ?, ? , ?, ? , ? , ? , ?	, ? , ? , ? , ? , ? , ?	, ? , ? , ? , ? , ? , ? ,        ? , ? , ? , ? , ? , ? , ? ,	? , ?,?,?)";
								pstmtInsert = conn.prepareStatement(sql);
								pstmtInsert.setString(1, qcNo);
								// pstmtInsert.setString(2, "I");
								pstmtInsert.setString(2, qcOrdType);// added by
								// sagar on
								// 10/07/14
								pstmtInsert.setTimestamp(3, tranDate);
								pstmtInsert.setString(4, siteCode);
								pstmtInsert.setString(5, itemCode);
								pstmtInsert.setString(6, null);
								qty = getUnroundDecimal(qty, 3);
								pstmtInsert.setDouble(7, qty);
								pstmtInsert.setDouble(8, qty - qtySample);
								pstmtInsert.setDouble(9, 0);
								pstmtInsert.setTimestamp(10, tranDate);
								pstmtInsert.setTimestamp(11, qcDueDate);
								pstmtInsert.setTimestamp(12, tranDate);
								pstmtInsert.setString(13, tranId);
								pstmtInsert.setString(14, lineNo);
								pstmtInsert.setString(15, lotNoRcp);
								pstmtInsert.setString(16, lotSl);
								pstmtInsert.setTimestamp(17, tranDate);
								pstmtInsert.setString(18, userId);
								pstmtInsert.setString(19, termId);
								pstmtInsert.setString(20, locCode);
								pstmtInsert.setDouble(21, qtySample);
								pstmtInsert.setString(22, "U");
								pstmtInsert.setString(23, unit);
								pstmtInsert.setString(24, "A");
								pstmtInsert.setString(25, batchNo);
								pstmtInsert.setTimestamp(26, expiryDate);
								pstmtInsert.setString(27, aprv);
								pstmtInsert.setString(28, rej);
								pstmtInsert.setString(29, unit);
								pstmtInsert.setString(30, lotNoRcp);
								pstmtInsert.setTimestamp(31, retestDt);
								pstmtInsert.setString(32, spec);
								pstmtInsert.setString(33, itemCode);
								pstmtInsert.setString(34, emp);
								pstmtInsert.setString(35, suppCode); // added by
								// Kunal
								// on
								// 8/11/12
								// as
								// per
								// Pravin
								// Sali
								// intruction
								pstmtInsert.setString(36, suppCodeMnfr);// added
								// by
								// Kunal
								// on
								// 8/11/12
								// as
								// per
								// Pravin
								// Sali
								// intruction
								updCnt = pstmtInsert.executeUpdate();
								pstmtInsert.close();
								pstmtInsert = null;
							}

							// ls_qord_no[upperbound(ls_qord_no[]) + 1] =
							// ls_qcno;
						}
						pstmt1.close();
						pstmt1 = null;
						rs1.close();
						rs1 = null;
						if (retString != null && retString.trim().length() > 0)
						{
							break;
						}
					}

				}// end of main while
				rs.close();
				rs = null;
				pstmtSql.close();
				pstmtSql = null;
				if (retString != null && retString.trim().length() > 0)
				{
					return retString;
				}
			} else if ("N".equalsIgnoreCase(qcReqd))
			{
				genLotAuto = distCommon.getDisparams("999999", "GENERATE_LOT_NO_AUTO", conn);
				// lotNoManualSite = distCommon.getDisparams("999999" ,
				// "LOT_NO_MANUAL_SITE", conn); //Gulzar - 25/11/11

				if (genLotAuto == null)
				{
					genLotAuto = "N";
				}
				genLotAuto = genLotAuto.trim();

				if ("Y".equalsIgnoreCase(genLotAuto))
				{
					lotNoManualSite = distCommon.getDisparams("999999", "LOT_NO_MANUAL_SITE", conn); // Gulzar
					// -
					// 25/11/11
					siteString = lotNoManualSite;
					// 10/09/13 manoharan in case there is no comma
					String siteArray[];
					siteArray = lotNoManualSite.split(",");
					String siteTemp = "";
					boolean manualLot = false;
					for (int ctr = 0; ctr < siteArray.length; ctr++)
					{
						siteTemp = siteArray[ctr];
						if (siteTemp.trim() == siteCode.trim())
						{
							manualLot = true;
						}
					}

					/*
					 * while ( siteString.trim().length() > 0 ) { sitecode =
					 * distCommon.getToken( siteString , ","); if (
					 * sitecode.trim() == siteCode.trim() ) { break; } }
					 */
					if (!manualLot)
					{
						sql = "SELECT DISTINCT A.ITEM_CODE, (CASE WHEN A.BATCH_NO IS NULL THEN ' ' ELSE A.BATCH_NO END) AS BATCH_NO, A.LOC_CODE FROM		   PORCPDET A, ITEM B  WHERE A.ITEM_CODE = B.ITEM_CODE AND TRAN_ID = ? AND (CASE WHEN B.STK_OPT IS NULL THEN		   '0' ELSE B.STK_OPT END) = '2' AND (A.LOT_NO IS NULL OR TRIM(A.LOT_NO) IS NULL OR LENGTH(TRIM(A.LOT_NO)) = 0		   ) ";
						pstmtSql = conn.prepareStatement(sql);
						pstmtSql.setString(1, tranId);
						rs = pstmtSql.executeQuery();

						while (rs.next())
						{
							itemCode = checkNull(rs.getString("ITEM_CODE"));
							batchNo = checkNull(rs.getString("BATCH_NO"));
							locCode = checkNull(rs.getString("LOC_CODE"));

							// tranDate = getCurrdateAppFormat();
							xmlValues = "";
							xmlValues = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
							xmlValues = xmlValues + "<Header></Header>";
							xmlValues = xmlValues + "<Detail1>";
							xmlValues = xmlValues + "<qorder_no></qorder_no>";
							xmlValues = xmlValues + "<site_code>" + siteCode + "</site_code>";
							xmlValues = xmlValues + "<qorder_date>" + getCurrdateAppFormat() + "</qorder_date>";
							// xmlValues = xmlValues +
							// "<qorder_type>I</qorder_type>";
							xmlValues = xmlValues + "<qorder_type>" + qcOrdType + "</qorder_type>";// added
							// by
							// sagar
							// on
							// 10/07/14
							xmlValues = xmlValues + "<item_ser>" + itemSer + "</item_ser>";
							xmlValues = xmlValues + "<porcp_no>" + tranId + "</porcp_no>";
							xmlValues = xmlValues + "<porcp_line_no>" + lineNo + "</porcp_line_no>";
							xmlValues = xmlValues + "</Detail1></Root>";
							System.out.println("xmlValues  :[" + xmlValues + "]");
							TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", commonConstants.DB_NAME);
							qcNo = tg.generateTranSeqID("QC-ORD", "qorder_no", key, conn);
							System.out.println("qcNo4=" + qcNo);

							if ("ERROR".equals(qcNo))
							{
								retString = itmDBAccessLocal.getErrorString("", "VTTRANID", "","",conn);
								break;
							}

							sql = "UPDATE PORCPDET SET LOT_NO = ? WHERE TRAN_ID = ? AND ITEM_CODE = ? AND (CASE WHEN BATCH_NO IS NULL THEN		   ' ' ELSE BATCH_NO END) = ? AND LOC_CODE = ? AND (LOT_NO IS NULL OR LENGTH(TRIM(LOT_NO)) = 0)";
							pstmtUpd = conn.prepareStatement(sql);
							if ("Y".equals(useSuppLot)) // condition added by
								// sagar on 18/07/14
							{
								pstmtUpd.setString(1, lotNoRcp);
							} else
							{
								pstmtUpd.setString(1, qcNo);
							}
							pstmtUpd.setString(2, tranId);
							pstmtUpd.setString(3, itemCode);
							pstmtUpd.setString(4, batchNo);
							pstmtUpd.setString(5, locCode);
							updCnt = pstmtUpd.executeUpdate();
							// if ( updCnt != 1 )
							if (updCnt == 0)// Condition Added by Sagar on
								// 20/OCT/14
							{
								retString = itmDBAccessLocal.getErrorString("", "DS000NR", "","",conn);// Added
								// by
								// chandrashekar
								// on
								// 17-sep-2014
							}
							if (pstmtUpd != null)
							{
								pstmtUpd.close();
								pstmtUpd = null;
							}
						}
						pstmtSql.close();
						pstmtSql = null;
						rs.close();
						rs = null;
					}
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			throw new ITMException(e);
		} finally
		{
			try
			{
				System.out.println("Finally block, closing resultset and prepared statement variables..............");
				if (pstmtSql != null)
				{
					pstmtSql.close();
					pstmtSql = null;
				}
				if (pstmtUpd != null)
				{
					pstmtUpd.close();
					pstmtUpd = null;
				}
				if (pstmt1 != null)
				{
					pstmt1.close();
					pstmt1 = null;
				}
				if (pstmt2 != null)
				{
					pstmt2.close();
					pstmt2 = null;
				}
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (rs1 != null)
				{
					rs1.close();
					rs1 = null;
				}
				if (rs2 != null)
				{
					rs2.close();
					rs2 = null;
				}
			} catch (Exception e)
			{
				System.out.println(e.getMessage());
				e.printStackTrace();//Modified by Anjali R. on [25/10/2018]
				throw new ITMException(e);
			}
		}
		System.out.println("Returning Result ::" + retString);
		return retString;
	}

	public String autoBackflush(String tranId, Connection conn, String xtraParams) throws RemoteException, ITMException
	{

		String purcOrder = "", retString = "", suppCode = "", pordSite = "", ordType = "";
		String channelPartner = "", disLink = "", sordSite = "", distOrder = "", distRcp = "";
		String itemSer = "", itemCode = "", bomCode = "", unit = "", itemCodeDet = "", unitDet = "";
		String trNoFld = "", keyStr = "", keyStrFrDepatch = "", exit = "", locHdr = "", saleOrder = "", custCode = "";
		String custCodeDlv = "", tranCode = "", stanCode = "", dlv1 = "", dlv2 = "", dlvCity = "", dlvPin = "";
		String dlvCnt = "", currCode = "", currCodeFrt = "", stanCodeInit = "", orderType = "", transMode = "";
		String remarks = "", remarks2 = "", remarks3 = "", currCodeIns = "", dlv3 = "", gpSer = "";
		String itemDes = "", lineNoSord = "", siteCodeDet = "", taxCl = "", taxCh = "", taxEn = "";
		String unitStd = "", packIns = "", stUnit = "", itemCodeOrd = "", expLev = "", jobWorkType = "", subContractType = "";
		String linePoRcpDet = "", lineNo = "", invStat = "", allocated = "", acctCodeDr = "", cctrCodeDr = "";
		String sordType = "", grade = "", acctCodeCr = "", cctrCodeCr = "", status = "", lot = "";
		String distIssTid = "", cctrGl = "", acctGl = "", purcOrderTemp = "", lnoPoRcpDet = "", lnoOrdTemp = "";
		String itemCodeTemp = "", bomCodeTemp = "", unitTemp = "", locDetNoBom = "", func = "";
		String totFunc = "", lot_Sl = "", sql = "", itemStru = "", convrReqd = "", today = "";
		String tranSer = "", generatedTranId = "", xmlValues = "", bomXml = "", xmlStringFrmBom = "", tranType = "";
		String lotNo = "";
		PreparedStatement pstmt = null, pstmt1 = null, pstmt2 = null, pstmtRbHdr = null, pstmtRbDet = null, pstmtDspHdr = null, pstmtDspDtl = null, pstmtUpd = null;
		ResultSet rs = null, rs1 = null, rs2 = null;

		// ArrayList<String> purcOrderDet=new ArrayList<String>(),lineNoOrd=new
		// ArrayList<String>(),itemCodeRcp=new
		// ArrayList<String>(),bomCodeOrd=new ArrayList<String>();
		ArrayList<String> itemCodeRcpNoBom = new ArrayList<String>(), unitOrdNoBom = new ArrayList<String>();
		// unitOrd=new ArrayList<String>(),,lnnoPoRcpDet=new
		// ArrayList<String>();
		// ArrayList<String> locCode=new ArrayList<String>() ;//, lotSl=new
		// ArrayList<String>();
		String distIss = "";
		// ArrayList<Double> rate=new ArrayList<Double>();
		// quantityPoRdDet=new ArrayList<Double>(), quantityPoRcpDet=new
		// ArrayList<Double>(),stkQty=new ArrayList<Double>(),
		ArrayList convRateList = null, convQtyList = null, qtyStduomList;

		HashMap<Integer, String> lotNoMap = new HashMap<Integer, String>();
		HashMap<Integer, Double> stkQtyMap = new HashMap<Integer, Double>();
		HashMap<Integer, String> lotSlMap = new HashMap<Integer, String>();
		HashMap<Integer, String> locCodeMap = new HashMap<Integer, String>();
		HashMap<Integer, Double> rateMap = new HashMap<Integer, Double>();
		NodeList detlList = null;
		Document dom = null;
		HashMap hashMap = new HashMap();
		boolean forceFlag = true;
		boolean isbackflush=false;

		double quantity = 0.0, qtyDet = 0.0, updQty = 0.0, amountHdr = 0.0, conQtyStd = 0.0;
		double qtyDesp = 0.0, frtAmt = 0.0, exchRateIns = 0.0, insAmt = 0.0, discountDes = 0.0, despatchedQty = 0.0;
		double orderQty = 0.0, pendingQty = 0.0, minusQty = 0.0, balQty = 0.0, noArt = 0.0;
		double qtyAlloc = 0.0, quantityStduom = 0.0, amtHdrDesp = 0.0, totalAdditionalCost = 0.0, additionalCost = 0.0;
		double qtyChk = 0.0, grWgh = 0.0, netWgh = 0.0, tareWgh = 0.0, hGrWgh = 0.0, hNetWgh = 0.0;
		double hTareWgh = 0.0, taxAmt = 0.0, chkQty = 0.0, quantityNoBom = 0.0, amountHdrNoBom = 0.0, quantityPoRdTemp = 0.0;
		double quantityPoRcpTemp = 0.0, convQty = 0.0, rateStd = 0.0, rateClg = 0.0, diffRate = 0.0, totRate = 0.0;
		double itemRate = 0.0, rateNoBom = 0.0, convRate = 0.0, exchRate = 0.0, exchRateFrt = 0.0, conv = 0.0;

		int ctr = 0, row = 0, rtn = 0, setRow = 0, count = 0, currRowNoBom = 0;
		int iRow = 0, currRow = 0, hdrRow = 0, updCnt = 0, rcpBackFlushDet = 0;
		Timestamp ordDate = null, mfgDt = null, expDt = null, expDate = null;
		boolean noBom = true;
        TransIDGenerator tg = null;
        String qtyDetStr="";//added by monika salla on 12 oct 20
		try
		{
			ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
			DistCommon distCommon = new DistCommon();
			FinCommon finCommon = new FinCommon();
			ExplodeBom bom = new ExplodeBom();
			PorderConf poConf=new PorderConf();//added by monika to call explode bom from porderconf
			ibase.utility.E12GenericUtility genericUtility = new ibase.utility.E12GenericUtility();
			RcpBackflushConfirm rcpBackflushConf = new RcpBackflushConfirm();
			DateFormat dtFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());
			DateFormat dtFormatDb = new SimpleDateFormat(genericUtility.getDBDateFormat());
			today = dtFormat.format(new java.util.Date());

			String todaysDate = dtFormatDb.format(new java.util.Date());

			// DateFormat dateFormat= new
			// SimpleDateFormat("yyyy-mm-dd HH:mm:ss.SSSSSS");

			InvAllocTraceBean allocTraceBean = new InvAllocTraceBean();
			// ShipmentConf shipmentConf=new ShipmentConf();

			jobWorkType = distCommon.getDisparams("999999", "JOBWORK_TYPE", conn);

			if (jobWorkType == null || jobWorkType.trim().length() > 0)
			{
				jobWorkType = ""; 

			}

			subContractType = distCommon.getDisparams("999999", "SUBCONTRACT_TYPE", conn);

			if (subContractType == null || subContractType.trim().length() > 0)
			{
				subContractType = "";
			}

			//commented by monika salla on 6 oct 2020
			//convrReqd = distCommon.getDisparams("999999", "RBKFSH_CONV_REQD", conn);
			/*if (convrReqd == null || convrReqd.trim().length() > 0)
			{
				convrReqd = "Y";
			}*/

			tranSer = "R-BFS";
			trNoFld = "tranId";

			sql = "SELECT KEY_STRING FROM TRANSETUP WHERE UPPER(TRAN_WINDOW) = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, "W_RECEIPT_BACKFLUSH");
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				keyStr = checkNull(rs.getString("KEY_STRING"));
			} else
			{
				sql = "SELECT KEY_STRING FROM TRANSETUP WHERE TRAN_WINDOW = ?";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, "GENERAL");
				rs1 = pstmt.executeQuery();
				if (rs1.next())
				{
					keyStr = checkNull(rs1.getString("KEY_STRING"));
				}
				pstmt1.close();
				pstmt1 = null;
				rs1.close();
				rs1 = null;
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;

			sql = "SELECT KEY_STRING FROM TRANSETUP WHERE UPPER(TRAN_WINDOW) = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, "W_DESPATCH");
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				keyStrFrDepatch = checkNull(rs.getString("KEY_STRING"));
			} else
			{
				sql = "SELECT KEY_STRING FROM TRANSETUP WHERE TRAN_WINDOW = ?";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, "GENERAL");
				rs1 = pstmt.executeQuery();
				if (rs1.next())
				{
					keyStrFrDepatch = checkNull(rs1.getString("KEY_STRING"));
				}
				pstmt1.close();
				pstmt1 = null;
				rs1.close();
				rs1 = null;
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;

			sql = "SELECT PURC_ORDER FROM PORCP WHERE TRAN_ID = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				purcOrder = checkNull(rs.getString("PURC_ORDER"));
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;

			sql = "SELECT SUPP_CODE, SITE_CODE__DLV, PORD_TYPE FROM  PORDER WHERE PURC_ORDER = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, purcOrder);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				suppCode = checkNull(rs.getString("SUPP_CODE"));
				pordSite = checkNull(rs.getString("SITE_CODE__DLV"));
				ordType = checkNull(rs.getString("PORD_TYPE"));
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;

			if (!("P").equalsIgnoreCase(orderType))
			{
				sql = "SELECT (CASE WHEN CHANNEL_PARTNER IS NULL THEN 'N' ELSE CHANNEL_PARTNER END) AS CHANNEL_PARTNER, DIS_LINK, SITE_CODE__CH FROM SITE_SUPPLIER WHERE SITE_CODE = ? AND SUPP_CODE = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, pordSite);
				pstmt.setString(2, suppCode);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					channelPartner = checkNull(rs.getString("CHANNEL_PARTNER"));
					disLink = checkNull(rs.getString("DIS_LINK"));
					sordSite = checkNull(rs.getString("SITE_CODE__CH"));
				}

				/*
				 * added by sachin on 08-aug-13 check length of channel partner
				 * string
				 */
				if (channelPartner == null || channelPartner.trim().length() == 0)
				{
					sql = "SELECT (CASE WHEN CHANNEL_PARTNER IS NULL THEN 'N' ELSE CHANNEL_PARTNER END) AS CHANNEL_PARTNER, DIS_LINK, SITE_CODE FROM  SUPPLIER WHERE SUPP_CODE = ? ";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, suppCode);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						channelPartner = checkNull(rs1.getString("CHANNEL_PARTNER"));
						disLink = checkNull(rs1.getString("DIS_LINK"));
						sordSite = checkNull(rs1.getString("SITE_CODE"));
					}
					pstmt1.close();
					pstmt1 = null;
					rs1.close();
					rs1 = null;
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;

			}
			int supplySiteCount=0;	
			Map<String, HashMap<String, String>> outerHashMap = new HashMap<String, HashMap<String, String>>();


			if(("Y").equalsIgnoreCase(channelPartner) && ("A").equalsIgnoreCase(disLink))
			{
				isbackflush=true;
			}//Added  by monika salla on 29 sept 2020 as it is not get dist_iss tran_id
			// change done by kunal on 12/june/14 not consider cancel dist
			// order
			sql = "SELECT DIST_ORDER FROM  DISTORDER WHERE PURC_ORDER IN (SELECT DISTINCT PURC_ORDER FROM PORCPDET WHERE TRAN_ID =?) AND CONFIRMED = 'Y'  and case when status is null then 'N' else status end <> 'X' ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				distOrder = checkNull(rs.getString("DIST_ORDER"));

				sql = "SELECT TRAN_ID FROM DISTORD_ISS WHERE  DIST_ORDER = ? AND CONFIRMED = 'Y' ";

				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, distOrder);
				rs1 = pstmt1.executeQuery();
				if (rs1.next())
				{
					distIss = checkNullFrLotSl(rs1.getString("TRAN_ID"));
				} else
				{
					pstmt1.close();
					pstmt1 = null;
					rs1.close();
					rs1 = null;
					retString = itmDBAccessLocal.getErrorString("", "VTDIST13", "","",conn);
					return retString;
				}
				pstmt1.close();
				pstmt1 = null;
				rs1.close();
				rs1 = null;

				sql = "SELECT TRAN_ID FROM DISTORD_RCP WHERE DIST_ORDER = ? AND TRAN_ID__ISS = ? AND CONFIRMED = 'Y' ";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, distOrder);
				pstmt1.setString(2, distIss.toString());// distIss[row]
				rs1 = pstmt1.executeQuery();
				if (rs1.next())
				{
					distRcp = checkNull(rs1.getString("TRAN_ID"));
				} else
				{
					pstmt1.close();
					pstmt1 = null;
					rs1.close();
					rs1 = null;
					retString = itmDBAccessLocal.getErrorString("", "VTPORCP", "","",conn);
					return retString;
				}

				pstmt1.close();
				pstmt1 = null;
				rs1.close();
				rs1 = null;
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;
			if (distOrder == null || distOrder.trim().length() == 0) // added
				// by
				// kunal
				// on
				// 09/july/14
			{
				retString = itmDBAccessLocal.getErrorString("", "VTPORCP01", "","",conn);
				return retString;
			}

			func = distCommon.getDisparams("999999", "TRANSFER_LOTSL", conn);
			if (("NULLFOUND").equalsIgnoreCase(func))
			{
				retString = itmDBAccessLocal.getErrorString("", "VTFUNCERR", "","",conn);
				return retString;
			}

			sql = "SELECT ITEM_SER FROM PORDER WHERE PURC_ORDER = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, purcOrder);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				itemSer = checkNull(rs.getString("ITEM_SER"));
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;
			//added by monika salla
			//}  
			//added by monika salla on 12 august 2020 to check receipt backflush for non cjhannel partner condition
			//else // commented by manoharan during review
			//{ // commented by manoharan during review
			if(!isbackflush)
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
				pstmt.setString(1,purcOrder);
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
				isbackflush=true;
			}
			//}  // commented by manoharan during review
			//CREATING BACKFLUSH---
			if(isbackflush)
			{
				sql = "INSERT INTO RECEIPT_BACKFLUSH( TRAN_ID,TRAN_DATE, SITE_CODE, ITEM_CODE, BOM_CODE, QUANTITY, UNIT, BACKFLUSH_TYPE,LOT_SL, QC_REQD, LOC_CODE, CHG_DATE, MFG_DATE, EXP_DATE, CHG_USER, CHG_TERM, ACCT_CODE__CONV_GL, CCTR_CODE__CONV_GL,LOT_NO, AMOUNT, NET_AMT, INV_VALUE, TAX_AMT, DISCOUNT,ORDER_NO,REF_NO) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				pstmtRbHdr = conn.prepareStatement(sql);
				sql = "INSERT INTO RECEIPT_BACKFLUSH_DET(TRAN_ID, LINE_NO, ITEM_CODE, SITE_CODE, QUANTITY, UNIT,UNIT__DOC, LOC_CODE, LOT_NO, LOT_SL, TAX_AMT, DISCOUNT, RATE, AMOUNT, NET_AMT, CONV_QTY_DOC, QTY_DOC, RATE_DOC)	 VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				pstmtRbDet = conn.prepareStatement(sql);

				sql = "SELECT PORCPDET.PURC_ORDER, PORCPDET.LINE_NO__ORD , PORCPDET.ITEM_CODE ,	PORCPDET.QUANTITY, PORDDET.QUANTITY	,PORDDET.BOM_CODE , PORCPDET.UNIT, PORCPDET.LINE_NO,PORCPDET.LOT_SL,PORCPDET.LOC_CODE FROM PORCPDET, PORDDET WHERE PORCPDET.TRAN_ID = ? AND PORCPDET.PURC_ORDER = PORDDET.PURC_ORDER  AND PORCPDET.LINE_NO__ORD = PORDDET.LINE_NO AND  PORDDET.BOM_CODE IS NOT NULL";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();

				//Map<String, HashMap<String, String>> outerHashMap = new HashMap<String, HashMap<String, String>>();

				while (rs.next())
				{
					HashMap<String, String> innerHashMap = new HashMap<String, String>();

					purcOrderTemp = checkNull(rs.getString("PURC_ORDER"));
					lnoOrdTemp = checkNull(rs.getString("LINE_NO__ORD"));
					itemCodeTemp = checkNull(rs.getString("ITEM_CODE"));
					quantityPoRcpTemp = rs.getDouble("QUANTITY");
					quantityPoRdTemp = rs.getDouble("QUANTITY");
					bomCodeTemp = checkNull(rs.getString("BOM_CODE"));
					unitTemp = checkNull(rs.getString("UNIT"));
					lnoPoRcpDet = checkNull(rs.getString("LINE_NO"));
					System.out.println("before trim lnoPoRcpDet----" + lnoPoRcpDet);
					if (lnoPoRcpDet.length() > 0)// Added By Manoj dtd
						// 02/10/2013 to trim
						// line_no
					{
						lnoPoRcpDet = lnoPoRcpDet.trim();
					}
					System.out.println(" after trim lnoPoRcpDet----" + lnoPoRcpDet);
					// int lnoPoRcpDetInInt = Integer.parseInt(lnoPoRcpDet);
					/* purcOrderDet[lnoPoRcpDetInInt] = purcOrderTemp; */
					// purcOrderDet.add(lnoPoRcpDetInInt,purcOrderTemp);
					// lineNoOrd.add(lnoPoRcpDetInInt,lnoOrdTemp);
					// itemCodeRcp.add(lnoPoRcpDetInInt,itemCodeTemp);
					// quantityPoRdDet.add(lnoPoRcpDetInInt,quantityPoRdTemp);
					// quantityPoRcpDet.add(lnoPoRcpDetInInt,quantityPoRcpTemp);
					// bomCodeOrd.add(lnoPoRcpDetInInt,bomCodeTemp);
					// unitOrd.add(lnoPoRcpDetInInt,unitTemp);
					// lnnoPoRcpDet.add(lnoPoRcpDetInInt,lnoPoRcpDet);

					/* added by swati on 11 sep 2013 */
					innerHashMap.put("purcOrderDet", purcOrderTemp);
					innerHashMap.put("lineNoOrd", lnoOrdTemp);
					innerHashMap.put("itemCodeRcp", itemCodeTemp);
					innerHashMap.put("quantityPoRdDet", String.valueOf(quantityPoRdTemp));
					innerHashMap.put("quantityPoRcpDet", String.valueOf(quantityPoRcpTemp));
					innerHashMap.put("bomCodeOrd", bomCodeTemp);
					innerHashMap.put("unitOrd", unitTemp);
					innerHashMap.put("lnnoPoRcpDet", lnoPoRcpDet);
					innerHashMap.put("poRcpdetlotSL", rs.getString("LOT_SL"));// Added
					// by
					// manoj
					// dtd
					// 25/09/2013
					// to
					// set
					// lotSL
					// from
					// porcpdet
					innerHashMap.put("poRcpdetlocCode", rs.getString("LOC_CODE"));// Added
					// by
					// manoj
					// dtd
					// 27/10/2014
					// to
					// set
					// locCode
					// from
					// porcpdet


					outerHashMap.put(lnoPoRcpDet, innerHashMap);
					System.out.println("testporcp11  outerhashmap" + outerHashMap.get(lnoPoRcpDet));


				}

				Iterator it = outerHashMap.entrySet().iterator();
				ArrayList<String> rcbTranIdList = new ArrayList<String>();

				while (it.hasNext())
				{

					rcpBackFlushDet = 0;
					Map.Entry entry = (Map.Entry) it.next();

					/* ended by swati on 11 sep 2013 */
					System.out.println("outerHashMap.get(entry.getKey())--[" + outerHashMap.get(entry.getKey()) + "]");
					currRow = 0;
					hdrRow = 0;
					sql = "SELECT MFG_DATE, EXPIRY_DATE , GROSS_WEIGHT, NET_WEIGHT , TARE_WEIGHT FROM PORCPDET WHERE TRAN_ID 	= ?  AND ITEM_CODE = ? ";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, tranId);
					// pstmt1.setString(2,itemCodeRcp.get(lnoPoRcpDetInInt));
					// //replaced by swati
					pstmt1.setString(2, ((outerHashMap.get(entry.getKey())).get("itemCodeRcp"))); // innerHashMap.get("itemCodeRcp")
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						mfgDt = rs1.getTimestamp("MFG_DATE");
						expDt = rs1.getTimestamp("EXPIRY_DATE");
						grWgh = rs1.getDouble("GROSS_WEIGHT");
						netWgh = rs1.getDouble("NET_WEIGHT");
						tareWgh = rs1.getDouble("TARE_WEIGHT");
					}
					pstmt1.close();
					pstmt1 = null;
					rs1.close();
					rs1 = null;

					sql = "SELECT TRAN_TYPE FROM PORCP WHERE TRAN_ID = ?";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, tranId);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						tranType = rs1.getString("tran_type");
					}
					pstmt1.close();
					pstmt1 = null;
					rs1.close();
					rs1 = null;

					sql = "SELECT LOC_CODE__APRV FROM  SITEITEM WHERE ITEM_CODE = ? AND SITE_CODE = ? ";
					pstmt1 = conn.prepareStatement(sql);
					// pstmt1.setString(1,
					// itemCodeRcp.get(lnoPoRcpDetInInt));//replaced by swati
					pstmt1.setString(1, ((outerHashMap.get(entry.getKey())).get("itemCodeRcp")));
					pstmt1.setString(2, sordSite);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						locHdr = checkNull(rs1.getString("LOC_CODE__APRV"));
					} else
					{
						sql = "SELECT LOC_CODE FROM ITEM WHERE  ITEM_CODE = ?";
						pstmt2 = conn.prepareStatement(sql);
						// pstmt2.setString(1,itemCodeRcp.get(lnoPoRcpDetInInt));
						pstmt2.setString(1, ((outerHashMap.get(entry.getKey())).get("itemCodeRcp")));
						rs2 = pstmt2.executeQuery();
						if (rs2.next())
						{
							locHdr = checkNull(rs2.getString("LOC_CODE"));
						} else
						{
							pstmt2.close();
							pstmt2 = null;
							rs2.close();
							rs2 = null;
							retString = itmDBAccessLocal.getErrorString("", "VMLOC4", "","",conn);
							return retString;
						}
						pstmt2.close();
						pstmt2 = null;
						rs2.close();
						rs2 = null;
					}
					pstmt1.close();
					pstmt1 = null;
					rs1.close();
					rs1 = null;

					cctrGl = finCommon.getFromAcctDetr(((outerHashMap.get(entry.getKey())).get("itemCodeRcp")), itemSer, "CONVGL", conn);

					if (cctrGl != null)
					{
						String[] Dr = cctrGl.split(",");
						System.out.println(Dr.toString() + "   " + Dr.length);
						if (Dr.length > 0)
						{
							acctGl = Dr[0];
						}
						if (Dr.length > 1)
						{
							cctrGl = Dr[1];
						} else
						{
							cctrGl = "";
						}
					}

					if (acctGl != null && acctGl.trim().length() == 0)// change
						// done
						// by
						// kunal
						// on
						// 21/may/14
						// handle
						// null
					{
						acctGl = null;
					}
					if (cctrGl != null && cctrGl.trim().length() == 0)
					{
						cctrGl = null;
					}
					System.out.println("acctCode::" + acctGl);
					System.out.println("cctr Code::" + cctrGl);
					// added by kunal on 21/may/14
					if (acctGl == null || acctGl.trim().length() == 0)
					{
						retString = itmDBAccessLocal.getErrorString("", "VMACCTBK", "","",conn);
						return retString;
					}
					xmlValues = "";
					xmlValues = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
					xmlValues = xmlValues + "<Header></Header>\r\n";
					xmlValues = xmlValues + "<Detail1>\r\n";
					xmlValues = xmlValues + "<tran_id></tran_id>\r\n";
					//added by monika to check site_code on channel partner condition
					if (("Y").equalsIgnoreCase(channelPartner))
					{
						xmlValues = xmlValues + "<site_code>" + sordSite + "</site_code>\r\n";
					}
					else
					{
						xmlValues = xmlValues + "<site_code>" + pordSite + "</site_code>\r\n";

					}//end
					xmlValues = xmlValues + "<tran_date>" + getCurrdateAppFormat() + "</tran_date>\r\n";
					xmlValues = xmlValues + "</Detail1>\r\n</Root>";
					System.out.println("xmlValues  :[" + xmlValues + "]");
					tg = new TransIDGenerator(xmlValues, "BASE", CommonConstants.DB_NAME);

					generatedTranId = tg.generateTranSeqID("R-BFS", "tran_id", keyStr, conn);

					if (generatedTranId.equalsIgnoreCase("ERROR"))
					{
						retString = itmDBAccessLocal.getErrorString("", "VTTRANID", "","",conn);
						return retString;
					}

					System.out.println("generatedTranId is :" + generatedTranId);
					System.out.println("outer hash key:" + Integer.parseInt(entry.getKey().toString()));

					pstmtRbHdr.setString(1, generatedTranId);
					pstmtRbHdr.setTimestamp(2, this.getCurrtDate());
					if (("Y").equalsIgnoreCase(channelPartner))
					{
						pstmtRbHdr.setString(3, sordSite);
					}
					else
					{
						pstmtRbHdr.setString(3, pordSite);
					}
					// commented by swati
					// pstmtRbHdr.setString(4,itemCodeRcp.get(lnoPoRcpDetInInt));
					// pstmtRbHdr.setString(5,
					// bomCodeOrd.get(lnoPoRcpDetInInt));
					// pstmtRbHdr.setDouble(6,
					// quantityPoRcpDet.get(lnoPoRcpDetInInt));
					// pstmtRbHdr.setString(7, unitOrd.get(lnoPoRcpDetInInt));
					pstmtRbHdr.setString(4, ((outerHashMap.get(entry.getKey())).get("itemCodeRcp")));
					pstmtRbHdr.setString(5, ((outerHashMap.get(entry.getKey())).get("bomCodeOrd")));
					pstmtRbHdr.setDouble(6, Double.parseDouble(((outerHashMap.get(entry.getKey())).get("quantityPoRcpDet"))));
					pstmtRbHdr.setString(7, ((outerHashMap.get(entry.getKey())).get("unitOrd")));
					pstmtRbHdr.setString(8, "J");
					// pstmtRbHdr.setString(9, "1S");//Commented by manoj dtd
					// 25/09/2013 to set lotSL from porcpdet
					System.out.println("Print lotSL----" + ((outerHashMap.get(entry.getKey())).get("poRcpdetlotSL")));
					pstmtRbHdr.setString(9, ((outerHashMap.get(entry.getKey())).get("poRcpdetlotSL")));
					pstmtRbHdr.setString(10, "N");
					// pstmtRbHdr.setString(11, locHdr);
					pstmtRbHdr.setString(11, ((outerHashMap.get(entry.getKey())).get("poRcpdetlocCode")));
					pstmtRbHdr.setTimestamp(12, this.getCurrtDate());
					pstmtRbHdr.setTimestamp(13, mfgDt);
					pstmtRbHdr.setString(15, userId);
					pstmtRbHdr.setString(16, termId);
					pstmtRbHdr.setString(17, acctGl);
					pstmtRbHdr.setString(18, cctrGl);

					sql = "select lot_no__passign,exp_date__passign FROM porddet where purc_order = ? and line_no = ? ";// manoj
					// dtd
					// 03/10/2013
					// Removed
					// trim
					// from
					// line_no
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, purcOrderTemp);
					pstmt1.setString(2, lnoOrdTemp);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						lotNo = checkNullFrLotSl(rs1.getString("lot_no__passign"));
						expDate = rs1.getTimestamp("exp_date__passign");
					}
					System.out.println("lot_no__passign is= " + lotNo + "and  exp_date__passign is =" + expDate);
					pstmt1.close();
					pstmt1 = null;
					rs1.close();
					rs1 = null;

					if (expDate != null)
					{
						pstmtRbHdr.setTimestamp(14, expDate);
					} else
					{
						pstmtRbHdr.setTimestamp(14, expDt);
					}

					if (lotNo != null && lotNo.trim().length() > 0)
					{
						pstmtRbHdr.setString(19, lotNo);

						lotNoMap.put(Integer.parseInt(entry.getKey().toString()), lotNo);
						pstmtRbHdr.setString(19, lotNoMap.get(Integer.parseInt(entry.getKey().toString())));
						System.out.println("Lot No is = [" + lotNo + "]  and key is " + Integer.parseInt(entry.getKey().toString()));

					} else
					{
						String generatedLotNo = tg.generateTranSeqID("QC-ORD", "lot_no", keyStr, conn);
						if (("ERROR").equalsIgnoreCase(generatedLotNo))
						{
							retString = itmDBAccessLocal.getErrorString("", "VTTRANID", "","",conn);
							return retString;
						}
						lotNoMap.put(Integer.parseInt(entry.getKey().toString()), generatedLotNo);
						pstmtRbHdr.setString(19, lotNoMap.get(Integer.parseInt(entry.getKey().toString())));

					}

					double dmfgLeadTime = 0;
					double dqcLeadTime = 0;

					sql = "SELECT PUR_LEAD_TIME, QC_LEAD_TIME, MFG_LEAD_TIME, mfg_lead_basis, batch_size_lead " + "FROM SITEITEM " + "WHERE SITE_CODE = ? " + "AND ITEM_CODE = ? ";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, sordSite);
					pstmt1.setString(2, (outerHashMap.get(entry.getKey())).get("itemCodeRcp"));
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{

						dqcLeadTime = rs1.getDouble(2);
						dmfgLeadTime = rs1.getDouble(3);
						pstmt1.close();
						rs1.close();
					} else
					{

						sql = "SELECT PUR_LEAD_TIME, QC_LEAD_TIME, MFG_LEAD " + "FROM ITEM " + "WHERE ITEM_CODE = ? ";
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1, (outerHashMap.get(entry.getKey())).get("itemCodeRcp"));
						rs1 = pstmt1.executeQuery();
						if (rs1.next())
						{
							// dpurLeadTime = rs1.getDouble(1);
							dqcLeadTime = rs1.getDouble(2);
							dmfgLeadTime = rs1.getDouble(3);
						}
						pstmt1.close();
						rs1.close();
					}

					pstmt1 = null;
					rs1 = null;

					// commented by swati
					// bomXml = bomXml +
					// "<bom_code>"+bomCodeOrd.get(lnoPoRcpDetInInt)+"</bom_code>\r\n";
					// bomXml = bomXml +
					// "<quantity>"+quantityPoRcpDet.get(lnoPoRcpDetInInt)+"</quantity>\r\n";

					System.out.println("Todays Date is =" + todaysDate);
					//commented by monika to get xml from Detail
					bomXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Root><Detail1>";

					//added by monika to check site_code on channel partner condition
					if (("Y").equalsIgnoreCase(channelPartner))
					{
						bomXml = bomXml + "<site_code>" + sordSite + "</site_code>\r\n";
					}
					else
					{
						bomXml = bomXml + "<site_code>" + pordSite + "</site_code>\r\n";
					}//end

					bomXml = bomXml + "<item_code>" + (outerHashMap.get(entry.getKey())).get("itemCodeRcp") + "</item_code>\r\n";
					bomXml = bomXml + "<line_type>B</line_type>\r\n";
					bomXml = bomXml + "<quantity>" + Double.parseDouble(((outerHashMap.get(entry.getKey())).get("quantityPoRcpDet"))) + "</quantity>\r\n";
					bomXml = bomXml + "<bom_code>" + ((outerHashMap.get(entry.getKey())).get("bomCodeOrd")) + "</bom_code>\r\n";
					bomXml = bomXml + "<exp_lev>" + 1 + "</exp_lev>\r\n";
					bomXml = bomXml + "<mfg_lead_time>" + dmfgLeadTime + "</mfg_lead_time>\r\n";
					bomXml = bomXml + "<due_date>" + todaysDate + "</due_date>\r\n";
					bomXml = bomXml + "<qc_lead_time>" + dqcLeadTime + "</qc_lead_time>\r\n";

					// bomXml = bomXml + "<work_order>" + "XYZ" +
					// "</work_order>\r\n";

					bomXml = bomXml + "</Detail1>\r\n</Root>";
					System.out.println("bomXml is =" + bomXml);

					//xmlStringFrmBom = bom.explodeBom(bomXml);
					//commented by monika salla on 12 august 2020  will explode multiple level which is not required
					// xmlStringFrmBom = bom.explodeBom(bomXml,conn);
					xmlStringFrmBom =poConf.explodeBomStr(bomXml,bomCodeTemp,"1.","B","XYZ",conn);

					System.out.println("xmlStringFrmBom is =" + xmlStringFrmBom);

					if (xmlStringFrmBom == null || xmlStringFrmBom.trim().length() == 0)
					{
						retString = itmDBAccessLocal.getErrorString("", "Failed to explode Bill of Material", "","",conn);
						return retString;
					}

					dom = genericUtility.parseString(xmlStringFrmBom);
					//detlList = dom.getElementsByTagName("Detail");
					//commented by Monika salla to get the data form explodeBomStr method from detail 1
					detlList = dom.getElementsByTagName("Detail1");//end
					System.out.println("detlList1 is =" + detlList.getLength());
					System.out.println("testporcp12  detsil list" +detlList);


					if (detlList != null)
					{
						for (int cntr = 0; cntr < detlList.getLength(); cntr++)
						{
							System.out.println(" in for Loop=");
                            itemCodeDet = genericUtility.getColumnValueFromNode("item_code", detlList.item(cntr));
                            //commented  by monika salla 0n 12 oct 2020 to getReqDecString upto 3 digit
                            qtyDet = Double.parseDouble(genericUtility.getColumnValueFromNode("quantity", detlList.item(cntr)));
                            System.out.println(" qtyDetStr in strig="+qtyDet);
                            qtyDet=doublevalue(utl.getReqDecString((qtyDet),3));
                             System.out.println(" qtyDetStr in double...="+qtyDet);
							chkQty = 0;
							setRow = 1;
							distIssTid = distIss;

							sql = "SELECT " + func + "('" + distIssTid + "','ABC') " + " FROM DUAL";

							pstmt1 = conn.prepareStatement(sql);
							rs1 = pstmt1.executeQuery();
							if (rs1.next())
							{
								lot_Sl = checkNullFrLotSl(rs1.getString(1));
							}
							pstmt1.close();
							pstmt1 = null;
							rs1.close();
							rs1 = null;

							if ("ERROR".equals(lot_Sl))
							{
								retString = itmDBAccessLocal.getErrorString("", "VTFUNCERR", "","",conn);
								return retString;
							}

							sql = " SELECT (CASE WHEN STOCK.QUANTITY IS NULL THEN 0 ELSE STOCK.QUANTITY END) -" + "(CASE WHEN STOCK.ALLOC_QTY IS NULL THEN 0 ELSE STOCK.ALLOC_QTY END) - " + "(CASE WHEN STOCK.HOLD_QTY IS NULL THEN 0 ELSE STOCK.HOLD_QTY END), " + "STOCK.UNIT, STOCK.LOC_CODE, STOCK.LOT_NO, STOCK.LOT_SL,STOCK.RATE " + "FROM STOCK,LOCATION,INVSTAT, DISTORD_ISSDET,distorder,porder WHERE STOCK.LOC_CODE = LOCATION.LOC_CODE  AND " + "((CASE WHEN STOCK.QUANTITY IS NULL THEN 0 ELSE STOCK.QUANTITY END) -" + "(CASE WHEN  STOCK.ALLOC_QTY IS NULL THEN 0 ELSE STOCK.ALLOC_QTY END) - " + "(CASE WHEN STOCK.HOLD_QTY IS NULL THEN 0 ELSE STOCK.HOLD_QTY END)) > 0 AND " + "LOCATION.INV_STAT = INVSTAT.INV_STAT AND  STOCK.SITE_CODE = ? AND " + "STOCK.ITEM_CODE = ? AND STOCK.LOT_NO = DISTORD_ISSDET.LOT_NO ";

							if (lot_Sl.equalsIgnoreCase(distIss))
							{
								sql = sql + " and stock.lot_sl= '" + distIss + "'";
							} else
							{
								sql = sql + " and stock.lot_sl = distord_issdet.lot_sl ";
							}
							//added by monika to check condition for channel partner
							if(("Y").equalsIgnoreCase(channelPartner))
							{ 
								sql = sql + "AND DISTORD_ISSDET.TRAN_ID = '" + distIssTid + "' " + " AND (CASE WHEN STOCK.ALLOC_QTY IS NULL THEN 0 ELSE STOCK.ALLOC_QTY END) >= 0  " + "AND INVSTAT.AVAILABLE = 'Y' ORDER BY  STOCK.PARTIAL_USED, " + "(CASE WHEN STOCK.EXP_DATE IS NULL THEN STOCK.CREA_DATE ELSE STOCK.EXP_DATE	 END), " + "STOCK.CREA_DATE, STOCK.LOT_NO, STOCK.LOT_SL";

								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, sordSite);
								pstmt1.setString(2, itemCodeDet);
							}
							else
							{
								//sql = sql + "AND DISTORD_ISSDET.TRAN_ID = '" + distIssTid + "' " + "distorder.dist_order=DISTORD_ISSDET.dist_order and porder.purc_order=distorder.purc_order and distorder.purc_order=?  AND (CASE WHEN STOCK.ALLOC_QTY IS NULL THEN 0 ELSE STOCK.ALLOC_QTY END) >= 0  " + "AND INVSTAT.AVAILABLE = 'Y' ORDER BY  STOCK.PARTIAL_USED, " + "(CASE WHEN STOCK.EXP_DATE IS NULL THEN STOCK.CREA_DATE ELSE STOCK.EXP_DATE	 END), " + "STOCK.CREA_DATE, STOCK.LOT_NO, STOCK.LOT_SL";
								//changes done by monika salla on 5 oct 2020 as sql query was not proper.
								sql = sql + "AND DISTORD_ISSDET.TRAN_ID = '" + distIssTid + "' " + " AND distorder.dist_order=DISTORD_ISSDET.dist_order and porder.purc_order=distorder.purc_order and distorder.purc_order=?  AND (CASE WHEN STOCK.ALLOC_QTY IS NULL THEN 0 ELSE STOCK.ALLOC_QTY END) >= 0  " + "AND INVSTAT.AVAILABLE = 'Y' ORDER BY  STOCK.PARTIAL_USED, " + "(CASE WHEN STOCK.EXP_DATE IS NULL THEN STOCK.CREA_DATE ELSE STOCK.EXP_DATE	 END), " + "STOCK.CREA_DATE, STOCK.LOT_NO, STOCK.LOT_SL";

								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, pordSite);
								pstmt1.setString(2, itemCodeDet);
								pstmt1.setString(3, purcOrder);
							}//end
							rs1 = pstmt1.executeQuery();
							while (rs1.next())
							{
								stkQtyMap.put(setRow, rs1.getDouble(1));
								unitDet = checkNull(rs1.getString("UNIT"));

								// locCode[setRow] =
								// checkNull(rs1.getString("LOC_CODE"));
								// lotNo[setRow] =
								// checkNull(rs1.getString("LOT_NO"));
								// lotSl[setRow] =
								// checkNull(rs1.getString("LOT_SL"));

								locCodeMap.put(setRow, checkNull(rs1.getString("LOC_CODE")));
								lotNoMap.put(setRow, checkNullFrLotSl(rs1.getString("LOT_NO")));
								lotSlMap.put(setRow, checkNullFrLotSl(rs1.getString("LOT_SL")));
								rateMap.put(setRow, rs1.getDouble("RATE"));
								chkQty = chkQty + stkQtyMap.get(setRow);
								setRow = setRow + 1;
							}
							pstmt1.close();
							pstmt1 = null;
							rs1.close();
							rs1 = null;
							System.out.println("Issued stock is not sufficientto get other available stock qtyDet11 [" + qtyDet + "] chkQty [" + chkQty + "]");
							if (qtyDet > chkQty || chkQty == 0)
							{
								sql = "SELECT (CASE WHEN STOCK.QUANTITY IS NULL THEN 0 ELSE STOCK.QUANTITY END) - (CASE WHEN STOCK.ALLOC_QTY IS NULL THEN 0 ELSE STOCK.ALLOC_QTY END) - (CASE WHEN STOCK.HOLD_QTY IS NULL THEN 0 ELSE STOCK.HOLD_QTY END), STOCK.UNIT, STOCK.LOC_CODE, STOCK.LOT_NO, STOCK.LOT_SL, STOCK.RATE FROM STOCK,LOCATION,INVSTAT WHERE STOCK.LOC_CODE = LOCATION.LOC_CODE AND ((CASE WHEN STOCK.QUANTITY IS NULL THEN 0 ELSE STOCK.QUANTITY END) - (CASE WHEN STOCK.ALLOC_QTY IS NULL THEN 0 ELSE STOCK.ALLOC_QTY END)-(CASE WHEN STOCK.HOLD_QTY  IS NULL THEN 0 ELSE STOCK.HOLD_QTY  END))  > 0 AND LOCATION.INV_STAT = INVSTAT.INV_STAT AND STOCK.SITE_CODE = ? AND STOCK.ITEM_CODE = ? AND (CASE WHEN STOCK.ALLOC_QTY IS NULL THEN 0 ELSE STOCK.ALLOC_QTY END) >= 0 AND INVSTAT.AVAILABLE = ? AND STOCK.LOT_NO NOT IN (SELECT LOT_NO FROM DISTORD_ISSDET WHERE DISTORD_ISSDET.TRAN_ID = ? AND  DISTORD_ISSDET.ITEM_CODE = ? ) ORDER BY STOCK.PARTIAL_USED, (CASE WHEN	STOCK.EXP_DATE IS NULL THEN STOCK.CREA_DATE ELSE STOCK.EXP_DATE END),STOCK.CREA_DATE, STOCK.LOT_NO, STOCK.LOT_SL ";
								pstmt1 = conn.prepareStatement(sql);

								if(("Y").equalsIgnoreCase(channelPartner))
								{ 
									pstmt1.setString(1, sordSite);
								}
								else
								{
									pstmt1.setString(1, pordSite);
								}
								pstmt1.setString(2, itemCodeDet);
								pstmt1.setString(3, "Y");
								pstmt1.setString(4, distIssTid);
								pstmt1.setString(5, itemCodeDet);

								rs1 = pstmt1.executeQuery();
								while (rs1.next())
								{ 
									stkQtyMap.put(setRow, rs1.getDouble(1));
									unitDet = checkNull(rs1.getString("UNIT"));
									// locCode[setRow] =
									// checkNull(rs1.getString("LOC_CODE"));
									// lotNo[setRow] =
									// checkNull(rs1.getString("LOT_NO"));
									// lotSl[setRow] =
									// checkNull(rs1.getString("LOT_SL"));

									locCodeMap.put(setRow, checkNull(rs1.getString("LOC_CODE")));
									lotNoMap.put(setRow, checkNullFrLotSl(rs1.getString("LOT_NO")));
									lotSlMap.put(setRow, checkNullFrLotSl(rs1.getString("LOT_SL")));

									rateMap.put(setRow, rs1.getDouble("RATE"));
									setRow = setRow + 1;
								}
								//System.out.println("testporcp13 set row--- ["+stkQtyMap.get(setRow, rs1.getDouble(1)));

								pstmt1.close();
								pstmt1 = null;
								rs1.close();
								rs1 = null;
							}


							exit = "";
							iRow = 0;
							System.out.println("set row--- ["+setRow+"iRow ["+iRow);

							for (iRow = 1; iRow <= setRow - 1; iRow++)
							{
								System.out.println("set row--- ["+setRow+"iRow ["+iRow+" ]11stkQtyMap.get(iRow) ["+stkQtyMap.get(iRow));
								currRow = currRow + 1;
								if (stkQtyMap.get(iRow) == qtyDet)
								{
									System.out.println("11stkQtyMap.get(iRow) ["+stkQtyMap.get(iRow)+"qtyDet--["+qtyDet);
									updQty = qtyDet;
									exit = "exit";
								} else if (stkQtyMap.get(iRow) > qtyDet)
								{
									System.out.println("12stkQtyMap.get(iRow) ["+stkQtyMap.get(iRow)+"qtyDet--["+qtyDet);

									updQty = qtyDet;
									exit = "exit";
								} else if (stkQtyMap.get(iRow) < qtyDet)
								{
									System.out.println("13stkQtyMap.get(iRow) ["+stkQtyMap.get(iRow)+"qtyDet--["+qtyDet);

									updQty = stkQtyMap.get(iRow);
									qtyDet = qtyDet - stkQtyMap.get(iRow);
									System.out.println("qtyDet - stkQtyMap.get(iRow)---"+qtyDet );

									exit = "exit";//added by monika salla on 8 oct 2020
								}
								//commented by monika salla  on 6 oct 2020 for unit conversion
								/*if (convrReqd.equalsIgnoreCase("Y"))
								{
									conv = 0;

									// convQtyList=distCommon.convQtyFactor(unitDet,unitOrd.get(lnoPoRcpDetInInt),
									// itemCodeDet, updQty, conv, conn);
									convQtyList = distCommon.convQtyFactor(unitDet, ((outerHashMap.get(entry.getKey())).get("unitOrd")), itemCodeDet, updQty, conv, conn);

									conv = Double.parseDouble(convQtyList.get(0).toString());
									convQty = Double.parseDouble(convQtyList.get(1).toString());
									convRateList = distCommon.convQtyFactor(unitDet, ((outerHashMap.get(entry.getKey())).get("unitOrd")), itemCodeDet, rateMap.get(iRow), conv, conn);
									conv = Double.parseDouble(convRateList.get(0).toString());
									convRate = Double.parseDouble(convRateList.get(1).toString());

									amountHdr = amountHdr + (convQty * convRate);

									if (-999999999 == convQty)
									{
										retString = itmDBAccessLocal.getErrorString("", "VMUCNV1", "","",conn);
										return retString;
									}
								} else
								{*/
								amountHdr = amountHdr + (updQty * rateMap.get(iRow));
								//	}
								pstmtRbDet.setString(1, generatedTranId);
								pstmtRbDet.setString(2, "" + currRow);
								pstmtRbDet.setString(3, itemCodeDet);
								if(("Y").equalsIgnoreCase(channelPartner))
								{ 
									pstmtRbDet.setString(4, sordSite);
								}
								else
								{
									pstmtRbDet.setString(4, pordSite);
								}
								pstmtRbDet.setDouble(5, updQty);
								pstmtRbDet.setString(6, unitDet);
								/*
								 * pstmtRbDet.setString(7,
								 * unitOrd[lnoPoRcpDetInInt]);
								 */
								pstmtRbDet.setString(7, ((outerHashMap.get(entry.getKey())).get("unitOrd")));
								// pstmtRbDet.setString(8, locCode[iRow] );
								pstmtRbDet.setString(8, locCodeMap.get(iRow));
								pstmtRbDet.setString(9, lotNoMap.get(iRow));
								pstmtRbDet.setString(10, lotSlMap.get(iRow));
								pstmtRbDet.setDouble(11, 0);
								pstmtRbDet.setDouble(12, 0);
								pstmtRbDet.setDouble(13, rateMap.get(iRow));
								pstmtRbDet.setDouble(14, updQty * rateMap.get(iRow));
								pstmtRbDet.setDouble(15, updQty * rateMap.get(iRow));
								pstmtRbDet.setDouble(16, conv);
								pstmtRbDet.setDouble(17, convQty);
								pstmtRbDet.setDouble(18, convRate);
								rcpBackFlushDet++;
								pstmtRbDet.addBatch();
								pstmtRbDet.clearParameters();

								// * Allocationg Stock*\\
								hashMap.put("tran_date", this.getCurrtDate());
								hashMap.put("ref_ser", "R-BFS");
								hashMap.put("ref_id", generatedTranId);
								hashMap.put("ref_line", lineNo);
								//added by monika to set site_code on channel partner contion
								if(("Y").equalsIgnoreCase(channelPartner))
								{ 
									hashMap.put("site_code", sordSite);
								}
								else
								{
									hashMap.put("site_code",pordSite);
								}

								hashMap.put("item_code", itemCodeDet);
								// hashMap.put("loc_code",locCode[iRow]);
								hashMap.put("loc_code", locCodeMap.get(iRow));
								hashMap.put("lot_no", lotNoMap.get(iRow));
								hashMap.put("lot_sl", lotSlMap.get(iRow));
								hashMap.put("alloc_qty", updQty);
								hashMap.put("chg_user", this.userId);
								hashMap.put("chg_term", this.termId);
								hashMap.put("chg_win", "W_RECEIPT_BACKFLUSH");

								sql = "SELECT ITEM_STRU FROM ITEM WHERE ITEM_CODE = ?";
								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, itemCodeDet);
								rs1 = pstmt1.executeQuery();
								if (rs1.next())
								{
									itemStru = checkNull(rs1.getString("ITEM_STRU"));
								}
								pstmt1.close();
								pstmt1 = null;
								rs1.close();
								rs1 = null;
								if (!itemStru.equalsIgnoreCase("R"))
								{
									retString = allocTraceBean.updateInvallocTrace(hashMap, conn);
									hashMap.clear();
									if (retString != null && retString.trim().length() > 0)
									{
										retString = itmDBAccessLocal.getErrorString("", retString, "","",conn);
										return retString;
									}
								}
								if (exit.equalsIgnoreCase("exit"))
								{
									break;
								}
							}

							if (noBom)
							{
								amountHdrNoBom = 0.0;
								sql = "SELECT PORCPDET.PURC_ORDER,PORCPDET.LINE_NO__ORD, PORCPDET.ITEM_CODE,PORCPDET.QUANTITY,PORDDET.QUANTITY , PORDDET.BOM_CODE, PORCPDET.UNIT , PORCPDET.LINE_NO FROM PORCPDET,PORDDET WHERE PORCPDET.TRAN_ID = ? AND PORCPDET.PURC_ORDER = PORDDET.PURC_ORDER AND PORCPDET.LINE_NO__ORD = PORDDET.LINE_NO AND	PORDDET.BOM_CODE IS NULL";
								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, tranId);
								rs1 = pstmt1.executeQuery();
								while (rs1.next())
								{
									HashMap<String, String> innerHashMap = new HashMap<String, String>();
									currRow = currRow + 1;
									purcOrderTemp = checkNull(rs1.getString("PURC_ORDER"));
									lnoOrdTemp = checkNull(rs1.getString("LINE_NO__ORD"));
									itemCodeTemp = checkNull(rs1.getString("ITEM_CODE"));
									quantityPoRcpTemp = rs1.getDouble("QUANTITY");
									quantityPoRdTemp = rs1.getDouble("QUANTITY");
									bomCodeTemp = checkNull(rs1.getString("BOM_CODE"));
									unitTemp = checkNull(rs1.getString("UNIT"));
									lnoPoRcpDet = checkNull(rs1.getString("LINE_NO"));

									// lnoPoRcpDetInInt =
									// Integer.parseInt(lnoPoRcpDet);
									/*
									 * purcOrderDet[lnoPoRcpDetInInt] =
									 * purcOrderTemp;
									 */
									// commented by swati
									// purcOrderDet.add(lnoPoRcpDetInInt,
									// purcOrderTemp);
									// lineNoOrd.add(lnoPoRcpDetInInt,
									// lnoOrdTemp) ;
									// itemCodeRcp.add(lnoPoRcpDetInInt,
									// itemCodeTemp);
									// bomCodeOrd.add(lnoPoRcpDetInInt,
									// bomCodeTemp);
									// unitOrd.add(lnoPoRcpDetInInt, unitTemp);
									// lnnoPoRcpDet.add(lnoPoRcpDetInInt,
									// lnoPoRcpDet);
									// quantityPoRcpDet.add(lnoPoRcpDetInInt,
									// quantityPoRcpTemp);
									// quantityPoRdDet.add(lnoPoRcpDetInInt,
									// quantityPoRdTemp);

									innerHashMap.put("purcOrderDet", purcOrderTemp);
									innerHashMap.put("lineNoOrd", lnoOrdTemp);
									innerHashMap.put("itemCodeRcp", itemCodeTemp);
									innerHashMap.put("quantityPoRdDet", String.valueOf(quantityPoRdTemp));
									innerHashMap.put("quantityPoRcpDet", String.valueOf(quantityPoRcpTemp));
									innerHashMap.put("bomCodeOrd", bomCodeTemp);
									innerHashMap.put("unitOrd", unitTemp);
									innerHashMap.put("lnnoPoRcpDet", lnoPoRcpDet);

									outerHashMap.put(lnoPoRcpDet, innerHashMap);

									sql = "SELECT MFG_DATE,	EXPIRY_DATE, GROSS_WEIGHT, NET_WEIGHT, TARE_WEIGHT FROM  PORCPDET WHERE	 TRAN_ID = ? AND ITEM_CODE = ? ";
									pstmt2 = conn.prepareStatement(sql);
									pstmt2.setString(1, tranId);
									// pstmt2.setString(2,itemCodeRcp.get(lnoPoRcpDetInInt));
									// //replaced by swati
									pstmt2.setString(2, ((outerHashMap.get(lnoPoRcpDet))).get("itemCodeRcp"));
									rs2 = pstmt2.executeQuery();
									if (rs2.next())
									{
										mfgDt = rs2.getTimestamp("MFG_DATE");
										expDt = rs2.getTimestamp("EXPIRY_DATE");
										grWgh = rs2.getDouble("GROSS_WEIGHT");
										netWgh = rs2.getDouble("NET_WEIGHT");
										tareWgh = rs2.getDouble("TARE_WEIGHT");
									}
									pstmt2.close();
									pstmt2 = null;
									rs2.close();
									rs2 = null;
									lotNoMap.put(Integer.parseInt(lnoPoRcpDet), "");
									sql = "SELECT COST_RATE FROM ITEM WHERE ITEM_CODE = ? ";
									pstmt2 = conn.prepareStatement(sql);
									// pstmt2.setString(1,itemCodeRcp.get(lnoPoRcpDetInInt));
									// //replaced by swati
									pstmt2.setString(1, ((outerHashMap.get(lnoPoRcpDet))).get("itemCodeRcp"));
									rs2 = pstmt2.executeQuery();
									if (rs2.next())
									{
										rateNoBom = rs2.getDouble("COST_RATE");
									}
									pstmt2.close();
									pstmt2 = null;
									rs2.close();
									rs2 = null;

									// quantityNoBom = 0 -
									// quantityPoRcpDet.get(lnoPoRcpDetInInt);//
									// replaced by swati
									quantityNoBom = 0 - Double.parseDouble(((outerHashMap.get(lnoPoRcpDet))).get("quantityPoRcpDet"));
									pstmtRbDet.setString(1, generatedTranId);
									pstmtRbDet.setString(2, "" + currRow);
									// pstmtRbDet.setString(3,
									// itemCodeRcp.get(lnoPoRcpDetInInt)); //
									// replaced by swati
									pstmtRbDet.setString(3, ((outerHashMap.get(lnoPoRcpDet))).get("itemCodeRcp"));
									//added by monika to set site_code on channel partner contion
									if(("Y").equalsIgnoreCase(channelPartner))
									{
										pstmtRbDet.setString(4, sordSite);
									}
									else
									{
										pstmtRbDet.setString(4, pordSite);
									}
									pstmtRbDet.setDouble(5, quantityNoBom);
									// pstmtRbDet.setString(6,
									// unitOrd.get(lnoPoRcpDetInInt));//
									// replaced by swati
									pstmtRbDet.setString(6, ((outerHashMap.get(lnoPoRcpDet))).get("unitOrd"));
									// pstmtRbDet.setString(7,
									// unitOrd.get(lnoPoRcpDetInInt));//
									// replaced by swati
									pstmtRbDet.setString(7, ((outerHashMap.get(lnoPoRcpDet))).get("unitOrd"));
									pstmtRbDet.setString(8, locHdr);
									// pstmtRbDet.setString(9,
									// lotNo.get(lnoPoRcpDetInInt));// replaced
									// by swati
									pstmtRbDet.setString(9, lotNoMap.get(Integer.parseInt(lnoPoRcpDet)));
									pstmtRbDet.setString(10, "1S");
									pstmtRbDet.setString(11, "0");
									pstmtRbDet.setString(12, "0");
									pstmtRbDet.setDouble(13, rateNoBom);
									pstmtRbDet.setDouble(14, quantityNoBom * rateNoBom);
									pstmtRbDet.setDouble(15, quantityNoBom * rateNoBom);
									pstmtRbDet.setDouble(16, conv);
									pstmtRbDet.setDouble(17, convQty);
									pstmtRbDet.setDouble(18, convRate);
									rcpBackFlushDet++;
									pstmtRbDet.addBatch();
									pstmtRbDet.clearParameters();
									amountHdrNoBom = amountHdrNoBom + (quantityNoBom * rateNoBom);
								}
								rs1.close();
								rs1 = null;
								pstmt1.close();
								pstmt1 = null;
								noBom = false;
							}
						}
						if (retString != null && retString.trim().length() > 0)
						{
							break;
						}
					}
					amountHdr = amountHdr + amountHdrNoBom;
					pstmtRbHdr.setDouble(20, amountHdr);
					pstmtRbHdr.setDouble(21, amountHdr);
					pstmtRbHdr.setDouble(22, amountHdr);
					pstmtRbHdr.setDouble(23, 0.0);
					pstmtRbHdr.setDouble(24, 0.0);
					pstmtRbHdr.setString(25, ((outerHashMap.get(entry.getKey())).get("purcOrderDet")));// Added
					// by
					// Manoj
					// dtd
					// 25/09/2013
					// to
					// store
					// porder
					// no.
					pstmtRbHdr.setString(26, tranId);
					pstmtRbHdr.addBatch();
					pstmtRbHdr.clearParameters();
					amountHdr = 0;
					amountHdrNoBom = 0;

					System.out.println("RECEIPT BACK FLUSH DET COUNT::" + rcpBackFlushDet);
					if (rcpBackFlushDet == 0) // ADDED BY KUNAL ON 21/MAY/14
					{
						retString = itmDBAccessLocal.getErrorString("", "VTBACKFREC", "","",conn);
						return retString;
					} else
					{
						rcbTranIdList.add(generatedTranId);
					}

				} // end of main while
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				pstmtRbHdr.executeBatch();
				pstmtRbHdr.clearBatch();
				pstmtRbDet.executeBatch();
				pstmtRbDet.clearBatch();
				// conn.commit();

				// * Confirmation Logic Come Here RECEIPT_BACKFLUSH

				// added by kunal on 20/MAY/14
				if (rcbTranIdList.size() > 0)
				{
					for (int backflushId = 0; backflushId < rcbTranIdList.size(); backflushId++)
					{
						generatedTranId = (String) rcbTranIdList.get(backflushId);
						System.out.println("generatedTranId--[" + generatedTranId + "]");
						retString = rcpBackflushConf.confirmRcpBackflush(generatedTranId, xtraParams, "", conn, false);// change
						// on
						// 22/may/14
						// handle
						// connection
						System.out.println("retString 4953::" + retString);
					}
				}

				// added by kunal on 20/MAY/14 END
				// retString=this.confirmWebService("receipt_backflush",
				// generatedTranId, xtraParams,"",conn);
				if (retString != null && retString.trim().length() > 0 && retString.indexOf("VTCONFIRM") == -1)
				{
					System.out.println("error in confirmRcpBackflush..");
					return retString;
				}

			}//end receipt backflush if condition  //added by monika salla
			// * Confirmation Logic Come Here

			/*---***  Despatch Start ***---*/
			//added by monika salla on 9 oct 2020  when channel partner ony to create despatch 
			if(("Y").equalsIgnoreCase(channelPartner))
			{

				tranSer = "S-DSP";
				trNoFld = "despId";
				currRow = 0;
				bomCode = "";
				generatedTranId = "";

				sql = "SELECT KEY_STRING FROM TRANSETUP WHERE UPPER(TRAN_WINDOW) = 'W_DESPATCH'  ";
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					keyStr = checkNull(rs.getString("KEY_STRING"));
				} else
				{
					sql = "SELECT KEY_STRING FROM TRANSETUP  WHERE TRAN_WINDOW ='GENERAL' ";
					pstmt1 = conn.prepareStatement(sql);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						keyStr = checkNull(rs.getString("KEY_STRING"));
					}
					pstmt1.close();
					pstmt1 = null;
					rs1.close();
					rs1 = null;
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;

				sql = "SELECT SALE_ORDER FROM SORDER WHERE trim(CUST_PORD) = ? AND CONFIRMED ='Y'";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, purcOrder);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					saleOrder = checkNull(rs.getString("SALE_ORDER"));
				} else
				{
					pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;
					retString = itmDBAccessLocal.getErrorString("", "no rec in sale ord", "","",conn);
					return retString;
				}

				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;
				if (saleOrder == null)
				{
					retString = itmDBAccessLocal.getErrorString("", "no rec in sale ord", "","",conn);
					return retString;
				}

				sql = "SELECT ORDER_DATE , CUST_CODE , CUST_CODE__DLV ,	TRAN_CODE , STAN_CODE ,	DLV_ADD1, DLV_ADD2 , DLV_CITY , DLV_PIN, COUNT_CODE__DLV ,	CURR_CODE ,	EXCH_RATE , STAN_CODE__INIT	, ORDER_TYPE, TRANS_MODE ,CURR_CODE__FRT,EXCH_RATE__FRT, FRT_AMT, CURR_CODE__INS,	EXCH_RATE__INS, INS_AMT, REMARKS, REMARKS2, REMARKS3, DLV_ADD3 FROM SORDER WHERE SALE_ORDER = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, saleOrder);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					ordDate = rs.getTimestamp("ORDER_DATE");
					custCode = checkNull(rs.getString("CUST_CODE"));
					custCodeDlv = checkNull(rs.getString("CUST_CODE__DLV"));
					tranCode = checkNull(rs.getString("TRAN_CODE"));
					stanCode = checkNull(rs.getString("STAN_CODE"));
					dlv1 = checkNull(rs.getString("DLV_ADD1"));
					dlv2 = checkNull(rs.getString("DLV_ADD2"));
					dlvCity = checkNull(rs.getString("DLV_CITY"));
					dlvPin = checkNull(rs.getString("DLV_PIN"));
					dlvCnt = checkNull(rs.getString("COUNT_CODE__DLV"));
					currCode = checkNull(rs.getString("CURR_CODE"));
					exchRate = rs.getDouble("EXCH_RATE");
					stanCodeInit = checkNull(rs.getString("STAN_CODE__INIT"));
					orderType = checkNull(rs.getString("ORDER_TYPE"));
					transMode = checkNull(rs.getString("TRANS_MODE"));
					currCodeFrt = checkNull(rs.getString("CURR_CODE__FRT"));
					exchRateFrt = rs.getDouble("EXCH_RATE__FRT");
					frtAmt = rs.getDouble("FRT_AMT");
					currCodeIns = checkNull(rs.getString("CURR_CODE__INS"));
					exchRateIns = rs.getDouble("EXCH_RATE__INS");
					insAmt = rs.getDouble("INS_AMT");
					remarks = checkNull(rs.getString("REMARKS"));
					remarks2 = checkNull(rs.getString("REMARKS2"));
					remarks3 = checkNull(rs.getString("REMARKS3"));
					dlv3 = checkNull(rs.getString("DLV_ADD3"));
				}

				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;

				xmlValues = "";
				xmlValues = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
				xmlValues = xmlValues + "<Header></Header>\r\n";
				xmlValues = xmlValues + "<Detail1>\r\n";
				xmlValues = xmlValues + "<tran_id></tran_id>\r\n";
				xmlValues = xmlValues + "<site_code>" + sordSite + "</site_code>\r\n";
				xmlValues = xmlValues + "<desp_date>" + getCurrdateAppFormat() + "</desp_date>\r\n";
				xmlValues = xmlValues + "</Detail1>\r\n</Root>";
				System.out.println("xmlValues  :[" + xmlValues + "]");

				tg = new TransIDGenerator(xmlValues, "BASE", CommonConstants.DB_NAME);
				generatedTranId = tg.generateTranSeqID("S-DSP", "desp_id", keyStrFrDepatch, conn);

				System.out.println(" generatedTranId for despatch is =" + generatedTranId);
				if ("ERROR".equalsIgnoreCase(generatedTranId))
				{
					retString = itmDBAccessLocal.getErrorString("", "VTTRANID", "","",conn);
					return retString;
				}

				sql = "INSERT INTO DESPATCH(DESP_ID,DESP_DATE, SITE_CODE, SORD_NO, SORD_DATE, CUST_CODE, CUST_CODE__DLV, EFF_DATE, DLV_ADD1, DLV_ADD2, DLV_ADD3, DLV_CITY, DLV_PIN, COUNT_CODE__DLV, CURR_CODE, EXCH_RATE, CURR_CODE__FRT, EXCH_RATE__FRT, FREIGHT, CURR_CODE__INS, EXCH_RATE__INS, INSURANCE, REMARKS,GP_SER, STAN_CODE, TRAN_CODE, TRANS_MODE, STAN_CODE__INIT, TOT_VALUE, GROSS_WEIGHT, TARE_WEIGHT, NETT_WEIGHT,STATUS) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				pstmtDspHdr = conn.prepareStatement(sql);

				pstmtDspHdr.setString(1, generatedTranId);
				pstmtDspHdr.setTimestamp(2, this.getCurrtDate());
				pstmtDspHdr.setString(3, sordSite);
				pstmtDspHdr.setString(4, saleOrder);
				pstmtDspHdr.setTimestamp(5, ordDate);
				pstmtDspHdr.setString(6, custCode);
				pstmtDspHdr.setString(7, custCodeDlv);
				pstmtDspHdr.setTimestamp(8, this.getCurrtDate());
				pstmtDspHdr.setString(9, dlv1);
				pstmtDspHdr.setString(10, dlv2);
				pstmtDspHdr.setString(11, dlv3);
				pstmtDspHdr.setString(12, dlvCity);
				pstmtDspHdr.setString(13, dlvPin);
				pstmtDspHdr.setString(14, dlvCnt);
				pstmtDspHdr.setString(15, currCode);
				pstmtDspHdr.setDouble(16, exchRate);
				pstmtDspHdr.setString(17, currCodeFrt);
				pstmtDspHdr.setDouble(18, exchRateFrt);
				pstmtDspHdr.setDouble(19, frtAmt);
				pstmtDspHdr.setString(20, currCodeIns);
				pstmtDspHdr.setDouble(21, exchRateIns);
				pstmtDspHdr.setDouble(22, insAmt);
				pstmtDspHdr.setString(23, remarks);
				/*
				 * pstmtDspHdr.setString(24, remarks2 );
				 * pstmtDspHdr.setString(25, remarks3 );
				 */

				if (("E").equals(orderType))
				{
					pstmtDspHdr.setNull(24, Types.CHAR);
				} else
				{
					pstmtDspHdr.setString(24, orderType);
				}
				pstmtDspHdr.setString(25, stanCode);
				pstmtDspHdr.setString(26, tranCode);
				pstmtDspHdr.setString(27, transMode);
				pstmtDspHdr.setString(28, stanCodeInit);

				// * Despatch Detail Data*\\

				sql = "SELECT ITEM_CODE, LINE_NO ,LOT_NO, LOT_SL,LOC_CODE  FROM PORCPDET WHERE TRAN_ID = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				int linePoRcpDetInInt = 0;
				sql = "INSERT INTO DESPATCHDET(DESP_ID, LINE_NO, ITEM_CODE__ORD, QUANTITY__ORD, SORD_NO, LINE_NO__SORD, SITE_CODE, EXP_LEV, ITEM_CODE, LOT_NO, LOT_SL, UNIT__STD, CONV__QTY_STDUOM, UNIT, QUANTITY__STDUOM, GROSS_WEIGHT,TARE_WEIGHT, NETT_WEIGHT, QUANTITY, QUANTITY_REAL, RATE__STDUOM, RATE__CLG, LOC_CODE, PACK_INSTR, NO_ART, TAX_AMT) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				pstmtDspDtl = conn.prepareStatement(sql);

				while (rs.next())
				{
					itemDes = checkNull(rs.getString("ITEM_CODE"));
					linePoRcpDet = checkNull(rs.getString("LINE_NO"));

					if (linePoRcpDet.length() > 0)// Added By Kunal on 3/oct/13
						// to trim line_no
					{
						linePoRcpDet = linePoRcpDet.trim();
					}
					System.out.println("linePoRcpDet==" + linePoRcpDet);

					currRow = currRow + 1;
					linePoRcpDetInInt = Integer.parseInt(linePoRcpDet);

					sql = "SELECT BOM_CODE FROM PORDDET WHERE PURC_ORDER = ? AND ITEM_CODE = ? ";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, purcOrder);
					// pstmt1.setString(2,
					// itemCodeRcp.get(linePoRcpDet));//replaced by swati
					pstmt1.setString(2, ((outerHashMap.get(linePoRcpDet))).get("itemCodeRcp"));
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						bomCode = checkNull(rs1.getString("BOM_CODE"));
					}
					pstmt1.close();
					pstmt1 = null;
					rs1.close();
					rs1 = null;

					sql = "SELECT LINE_NO, SITE_CODE, TAX_CLASS, TAX_CHAP, TAX_ENV, DISCOUNT, RATE__STDUOM,	RATE__CLG, UNIT__STD, CONV__QTY_STDUOM, UNIT,PACK_INSTR, (CASE WHEN NO_ART IS NULL THEN 0 ELSE NO_ART END)AS NO_ART FROM	SORDDET WHERE SALE_ORDER = ? AND ITEM_CODE  = ?";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, saleOrder);
					// pstmt1.setString(2, itemCodeRcp.get(linePoRcpDetInInt));
					// //replaced by swati
					pstmt1.setString(2, ((outerHashMap.get(linePoRcpDet))).get("itemCodeRcp"));
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						lineNoSord = checkNull(rs1.getString("LINE_NO"));
						siteCodeDet = checkNull(rs1.getString("SITE_CODE"));
						taxCl = checkNull(rs1.getString("TAX_CLASS"));
						taxCh = checkNull(rs1.getString("TAX_CHAP"));
						taxEn = checkNull(rs1.getString("TAX_ENV"));
						discountDes = rs1.getDouble("DISCOUNT");
						rateStd = rs1.getDouble("RATE__STDUOM");
						rateClg = rs1.getDouble("RATE__CLG");
						stUnit = checkNull(rs1.getString("UNIT__STD"));
						conQtyStd = rs1.getDouble("CONV__QTY_STDUOM");
						unitStd = checkNull(rs1.getString("UNIT"));
						packIns = checkNull(rs1.getString("PACK_INSTR"));
						noArt = rs1.getDouble("NO_ART");
					}
					pstmt1.close();
					pstmt1 = null;
					rs1.close();
					rs1 = null;

					sql = "SELECT SUM(CASE WHEN QTY_DESP IS NULL THEN 0 ELSE QTY_DESP END) AS QTY_DESP, SUM(CASE WHEN QUANTITY IS NULL THEN 0 ELSE QUANTITY END) AS QUANTITY  FROM  SORDITEM WHERE	SALE_ORDER = ? AND	LINE_NO = ? AND LINE_TYPE  <> 'B'";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, saleOrder);
					pstmt1.setString(2, lineNoSord);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						despatchedQty = rs1.getDouble("QTY_DESP");
						orderQty = rs1.getDouble("QUANTITY");
					}

					pstmt1.close();
					pstmt1 = null;
					rs1.close();
					rs1 = null;

					pendingQty = orderQty - despatchedQty;
					minusQty = 0.0;

					sql = "SELECT SUM(QUANTITY) AS QUANTITY FROM DESPATCHDET WHERE	SORD_NO = ? AND DESP_ID = ?  AND LINE_NO__SORD = ?";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, saleOrder);
					pstmt1.setString(2, generatedTranId);
					pstmt1.setString(3, lineNoSord);
					rs1 = pstmt1.executeQuery();

					if (rs1.next())
					{
						minusQty = rs1.getDouble("QUANTITY");
					}

					pstmt1.close();
					pstmt1 = null;
					rs1.close();
					rs1 = null;

					sql = "SELECT ITEM_CODE__ORD, QUANTITY,	EXP_LEV	, QTY_ALLOC FROM SORDITEM WHERE  SALE_ORDER = ? AND	 LINE_NO= ? AND LINE_TYPE = 'I'";// Manoj
					// dtd
					// 03/10/2013
					// removed
					// trim
					// from
					// line_no
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, saleOrder);
					pstmt1.setString(2, lineNoSord);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						itemCodeOrd = checkNull(rs1.getString("ITEM_CODE__ORD"));
						qtyDesp = rs1.getDouble("QUANTITY");
						expLev = checkNull(rs1.getString("EXP_LEV"));
						qtyAlloc = rs1.getDouble("QTY_ALLOC");
					}
					pstmt1.close();
					pstmt1 = null;
					rs1.close();
					rs1 = null;

					/* get Quantity Deapatch by sachin on 22-oct-13 */

					qtyDesp = Double.parseDouble(((outerHashMap.get(linePoRcpDet))).get("quantityPoRcpDet"));

					System.out.println("qtyDesp is =" + qtyDesp);
					/* End Get Quantity Deapatch by sachin on 22-oct-13 */

					qtyStduomList = distCommon.getConvQuantityFact(unitStd, stUnit, itemDes, qtyDesp, conQtyStd, conn);
					conQtyStd = Double.parseDouble(qtyStduomList.get(0).toString());
					quantityStduom = Double.parseDouble(qtyStduomList.get(1).toString());

					System.out.println("quantityStduom is =" + quantityStduom);
					System.out.println("conQtyStd is =" + conQtyStd);

					// * Check Method Parameter**\\

					/* Commented by sachin on 21-oct-13 */

					// quantityStduom= distCommon.convQtyFactor(unitStd, stUnit,
					// itemDes, conQtyStd, conn);

					// qtyDesp = quantityPoRcpDet.get(linePoRcpDetInInt);
					// //replaced by swati

					hGrWgh = hGrWgh + grWgh;
					hNetWgh = hNetWgh + netWgh;
					hTareWgh = hTareWgh + tareWgh;

					lineNoSord = "    " + lineNoSord;
					lineNoSord = lineNoSord.substring(lineNoSord.length() - 3, lineNoSord.length());
					System.out.println(" lineNoSord is =[" + lineNoSord + "]");

					pstmtDspDtl.setString(1, generatedTranId);
					pstmtDspDtl.setString(2, "" + currRow);
					pstmtDspDtl.setString(3, itemCodeOrd);
					pstmtDspDtl.setDouble(4, orderQty);
					pstmtDspDtl.setString(5, saleOrder);
					pstmtDspDtl.setString(6, lineNoSord);
					pstmtDspDtl.setString(7, siteCodeDet);
					pstmtDspDtl.setString(8, expLev);
					// pstmtDspDtl.setString(9,
					// itemCodeRcp.get(linePoRcpDetInInt)); //replaced by swati
					pstmtDspDtl.setString(9, ((outerHashMap.get(linePoRcpDet))).get("itemCodeRcp"));
					// pstmtDspDtl.setString(10,
					// lotNoMap.get(linePoRcpDetInInt));
					pstmtDspDtl.setString(10, rs.getString("LOT_NO"));

					// pstmtDspDtl.setString(11, "1S" );

					pstmtDspDtl.setString(11, rs.getString("LOT_SL"));
					pstmtDspDtl.setString(12, stUnit);
					pstmtDspDtl.setDouble(13, conQtyStd);
					pstmtDspDtl.setString(14, unitStd);
					pstmtDspDtl.setDouble(15, quantityStduom);
					pstmtDspDtl.setDouble(16, grWgh);
					pstmtDspDtl.setDouble(17, tareWgh);
					pstmtDspDtl.setDouble(18, netWgh);
					// pstmtDspDtl.setDouble(19, pendingQty-minusQty);

					if (bomCode == null || bomCode.length() == 0)
					{
						sql = "SELECT COST_RATE FROM ITEM WHERE ITEM_CODE = ?";
						pstmt1 = conn.prepareStatement(sql);
						// pstmt1.setString(1,itemCodeRcp.get(linePoRcpDetInInt));
						// //replaced by swati
						pstmt1.setString(1, ((outerHashMap.get(linePoRcpDet))).get("itemCodeRcp"));
						rs1 = pstmt1.executeQuery();
						rateNoBom = 0;
						if (rs1.next())
						{
							rateNoBom = rs1.getDouble("COST_RATE");
						}

						pstmt1.close();
						pstmt1 = null;
						rs1.close();
						rs1 = null;

						pstmtDspDtl.setDouble(19, qtyDesp * -1);
						pstmtDspDtl.setDouble(20, qtyDesp * -1);
						pstmtDspDtl.setDouble(21, rateNoBom);
						pstmtDspDtl.setDouble(22, rateNoBom);
						// pstmtDspDtl.setDouble(24, 0.0);
						itemRate = rateStd;
						amtHdrDesp = amtHdrDesp + (qtyDesp * itemRate);
					} else
					{
						pstmtDspDtl.setDouble(19, qtyDesp);
						pstmtDspDtl.setDouble(20, qtyDesp);
						pstmtDspDtl.setDouble(21, rateStd);
						pstmtDspDtl.setDouble(22, rateClg);
						// pstmtDspDtl.setDouble(24, discountDes );

						itemRate = rateStd;
						amtHdrDesp = amtHdrDesp + ((qtyDesp * itemRate) - (qtyDesp * itemRate * discountDes / 100));

					}

					// pstmtDspDtl.setString(25, taxCl );
					// pstmtDspDtl.setString(26, taxCh );
					// pstmtDspDtl.setString(27, taxEn );
					// pstmtDspDtl.setString(23, locHdr );
					pstmtDspDtl.setString(23, rs.getString("LOC_CODE"));
					pstmtDspDtl.setString(24, packIns);
					pstmtDspDtl.setDouble(25, noArt);

					additionalCost = 0;
					if (ordType.equalsIgnoreCase(jobWorkType) || ordType.equalsIgnoreCase(subContractType))
					{
						sql = "SELECT RATE FROM	STOCK WHERE ITEM_CODE = ? AND SITE_CODE = ? AND	LOC_CODE  = ? AND LOT_NO = ? AND LOT_SL = ? ";
						pstmt1 = conn.prepareStatement(sql);
						// pstmt1.setString(1,
						// itemCodeRcp.get(linePoRcpDetInInt)); //replaced by
						// swati
						pstmt1.setString(1, ((outerHashMap.get(linePoRcpDet))).get("itemCodeRcp"));
						pstmt1.setString(2, siteCodeDet);
						pstmt1.setString(3, locHdr);
						// pstmt1.setString(4, lotNoMap.get(linePoRcpDetInInt));
						pstmt1.setString(4, rs.getString("LOT_NO"));
						pstmt1.setString(5, rs.getString("LOT_SL"));

						rs1 = pstmt1.executeQuery();
						if (rs1.next())
						{
							additionalCost = rs1.getDouble("RATE");
						}
						pstmt1.close();
						pstmt1 = null;
						rs1.close();
						rs1 = null;
					}

					additionalCost = additionalCost * qtyDesp;
					totalAdditionalCost = totalAdditionalCost + additionalCost;

					sql = " UPDATE PORCPDET SET ADDITIONAL_COST = ADDITIONAL_COST + ? WHERE TRAN_ID = ? AND TRIM(LINE_NO) = ? ";
					pstmtUpd = conn.prepareStatement(sql);
					pstmtUpd.setDouble(1, additionalCost);
					pstmtUpd.setString(2, tranId);
					pstmtUpd.setString(3, linePoRcpDet);

					System.out.println("update couunt is =" + pstmtUpd.executeUpdate());

					/*
					 * if ( updCnt <=0 ) { pstmtUpd.close(); pstmtUpd = null;
					 * 
					 * retString =
					 * itmDBAccessLocal.getErrorString("","UPDCNTNOT1","","",conn);
					 * return retString; }
					 */
					pstmtUpd.close();
					pstmtUpd = null;

					// * Alocationg Stock*\\

					hashMap.put("tran_date", this.getCurrtDate());
					hashMap.put("ref_ser", "S-DSP");
					hashMap.put("ref_id", generatedTranId);
					hashMap.put("ref_line", lineNo);
					hashMap.put("site_code", sordSite);
					hashMap.put("item_code", itemCodeOrd);
					// hashMap.put("loc_code",locHdr);
					hashMap.put("loc_code", rs.getString("LOC_CODE"));
					hashMap.put("lot_no", rs.getString("LOT_NO"));
					hashMap.put("lot_sl", rs.getString("LOT_SL"));
					hashMap.put("alloc_qty", qtyDesp);
					hashMap.put("chg_user", this.userId);
					hashMap.put("chg_term", this.termId);
					hashMap.put("chg_win", "W_DESPATCH");

					retString = allocTraceBean.updateInvallocTrace(hashMap, conn);

					hashMap.clear();
					System.out.println("retString----" + retString);
					if (retString != null && retString.trim().length() > 0)
					{
						retString = itmDBAccessLocal.getErrorString("", retString, "","",conn);
						return retString;
					}

					// lc_tax_amt = gf_calc_tax_ds(desp_det, lds_tax, 'S-DSP',
					// ls_tran_id, ldt_today, "rate__stduom",
					// "quantity__stduom",0,ls_curr_code);

					sql = "SELECT INV_STAT FROM LOCATION WHERE LOC_CODE = ?";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, locHdr);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						invStat = rs1.getString("INV_STAT");
					}

					pstmt1.close();
					pstmt1 = null;
					rs1.close();
					rs1 = null;

					sql = "SELECT COUNT(*) AS CNT FROM SORDALLOC WHERE SALE_ORDER = ? AND LINE_NO = ?  AND	EXP_LEV = ? AND	 ITEM_CODE__ORD = ? AND ITEM_CODE = ? AND LOT_NO = ? AND LOT_SL = ?  AND LOC_CODE = ?";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, saleOrder);
					pstmt1.setString(2, lineNoSord);
					pstmt1.setString(3, expLev);
					pstmt1.setString(4, itemCodeOrd);
					// pstmt1.setString(5, itemCodeRcp.get(linePoRcpDetInInt));
					// // replaced by swati
					pstmt1.setString(5, ((outerHashMap.get(linePoRcpDet))).get("itemCodeRcp"));
					pstmt1.setString(6, rs.getString("LOT_NO"));
					pstmt1.setString(7, rs.getString("LOT_SL"));
					pstmt1.setString(8, locHdr);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						count = rs1.getInt("CNT");

						if (count == 0)
						{
							allocated = "N";
						} else
						{
							allocated = "Y";
						}

						if (("Y").equalsIgnoreCase(allocated))
						{
							sql = "UPDATE SORDALLOC SET STATUS = 'D' WHERE  SALE_ORDER 	 = ? AND LINE_NO = ? AND EXP_LEV = ?  AND ITEM_CODE__ORD = ? AND ITEM_CODE = ? AND LOT_NO = ? AND LOT_SL = ? AND LOC_CODE = ? ";
							pstmtUpd = conn.prepareStatement(sql);
							pstmtUpd.setString(1, saleOrder);
							pstmtUpd.setString(2, lineNoSord);
							pstmtUpd.setString(3, expLev);
							pstmtUpd.setString(4, itemCodeOrd);
							// pstmtUpd.setString(5,
							// itemCodeRcp.get(linePoRcpDetInInt)); // replaced
							// by swati
							pstmtUpd.setString(5, ((outerHashMap.get(linePoRcpDet))).get("itemCodeRcp"));
							pstmtUpd.setString(6, rs.getString("LOT_NO"));
							pstmtUpd.setString(7, rs.getString("LOT_SL"));
							pstmtUpd.setString(8, locHdr);
							updCnt = pstmtUpd.executeUpdate();
							if (updCnt != 1)
							{
								retString = itmDBAccessLocal.getErrorString("", "DS000NR", "","",conn);// Added
								// by
								// chandrashekar
								// on
								// 17-sep-2014
							}
							pstmtUpd.close();
							pstmtUpd = null;
						}

						/*
						 * comment SORDITEM updation logic by sachin Dated
						 * 24-10-13
						 */

						/*
						 * sql =
						 * "UPDATE SORDITEM SET QTY_DESP = QTY_DESP + ?, DATE_DESP = ? WHERE	SALE_ORDER = ? AND 	LINE_NO = ? AND		   SITE_CODE  = ? AND EXP_LEV = ?"
						 * ; pstmtUpd = conn.prepareStatement(sql);
						 * pstmtUpd.setDouble(1, qtyDesp);
						 * pstmtUpd.setTimestamp(2, this.getCurrtDate());
						 * pstmtUpd.setString(3, saleOrder);
						 * pstmtUpd.setString(4, lineNoSord);
						 * pstmtUpd.setString(5, siteCodeDet);
						 * pstmtUpd.setString(6, expLev); updCnt =
						 * pstmtUpd.executeUpdate();
						 * 
						 * pstmtUpd.close(); pstmtUpd = null;
						 */

						sql = "UPDATE SORDITEM SET QTY_ALLOC = QTY_ALLOC - ? WHERE	SALE_ORDER = ? AND LINE_NO = ? AND SITE_CODE  = ?        AND	EXP_LEV = ? AND QTY_ALLOC > ? ";
						pstmtUpd = conn.prepareStatement(sql);
						pstmtUpd.setDouble(1, qtyDesp);
						pstmtUpd.setString(2, saleOrder);
						pstmtUpd.setString(3, lineNoSord);
						pstmtUpd.setString(4, siteCodeDet);
						pstmtUpd.setString(5, expLev);
						pstmtUpd.setString(6, "0");
						updCnt = pstmtUpd.executeUpdate();
						if (updCnt != 1)
						{
							retString = itmDBAccessLocal.getErrorString("", "DS000NR", "","",conn);// Added
							// by
							// chandrashekar
							// on
							// 17-sep-2014
						}
						pstmtUpd.close();
						pstmtUpd = null;

						/*
						 * sql =
						 * "SELECT SUM(QUANTITY) - SUM(QTY_DESP) FROM SORDITEM WHERE SALE_ORDER = ? AND LINE_TYPE ='I'"
						 * ; pstmt2 = conn.prepareStatement(sql);
						 * pstmt2.setString(1, saleOrder ); rs2 =
						 * pstmt2.executeQuery(); if ( rs2.next() ) { qtyChk =
						 * rs2.getDouble(1); } pstmt2.close(); pstmt2 = null;
						 * rs2.close(); rs2 = null;
						 * 
						 * if ( qtyChk > 0 ) { status = "P"; } else { status =
						 * "C"; } sql =
						 * "UPDATE SORDER SET ALLOC_FLAG = 'Y', STATUS = ?, STATUS_DATE = ? WHERE SALE_ORDER = ?"
						 * ; pstmtUpd = conn.prepareStatement(sql);
						 * pstmtUpd.setString(1, status);
						 * pstmtUpd.setTimestamp(2, this.getCurrtDate());
						 * pstmtUpd.setString(3, saleOrder); updCnt =
						 * pstmtUpd.executeUpdate();
						 * 
						 * pstmtUpd.close(); pstmtUpd = null;
						 */
					}
					pstmt1.close();
					pstmt1 = null;
					rs1.close();
					rs1 = null;

					amtHdrDesp = amtHdrDesp + taxAmt;
					pstmtDspDtl.setDouble(26, taxAmt);

					pstmtDspDtl.addBatch();
					pstmtDspDtl.clearParameters();

				}

				// * End Despatch Detail Data*\\
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;

				pstmtDspHdr.setDouble(29, amtHdrDesp);
				pstmtDspHdr.setDouble(30, hGrWgh);
				pstmtDspHdr.setDouble(31, hTareWgh);
				pstmtDspHdr.setDouble(32, hNetWgh);
				pstmtDspHdr.setString(33, " ");

				pstmtDspHdr.addBatch();
				pstmtDspHdr.clearParameters();

				// ---*** End Despatch Start ***---\\

				sql = " UPDATE PORCP SET TOTAL_ADDITIONAL_COST = TOTAL_ADDITIONAL_COST + ? WHERE TRAN_ID = ? ";
				pstmtUpd = conn.prepareStatement(sql);
				pstmtUpd.setDouble(1, totalAdditionalCost);
				pstmtUpd.setString(2, tranId);
				updCnt = pstmtUpd.executeUpdate();
				if (updCnt != 1)
				{
					retString = itmDBAccessLocal.getErrorString("", "DS000NR", "","",conn);// Added
					// by
					// chandrashekar
					// on
					// 17-sep-2014
				}
				pstmtUpd.close();
				pstmtUpd = null;

				pstmtDspHdr.executeBatch();
				pstmtDspHdr.clearBatch();
				pstmtDspDtl.executeBatch();
				pstmtDspDtl.clearBatch();

				conn.commit();
				// Confirmation logic

				// retString=shipmentConf.confirm(generatedTranId, xtraParams,
				// "", conn);
				retString = this.confirmWebService("despatch", generatedTranId, xtraParams, "", conn);

				// ls_errcode =
				// nvo_despatch.gbf_retrieve_despatch(desp_hdr.describe("datawindow.data")
				// ,desp_det.describe("datawindow.data"),1,sqlca_cp)

			}//end monika salla on 9 oct 2020

		} catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("PoRcpConf:autoBackflush( String tranId, Connection conn):" + e.getMessage());
			throw new ITMException(e);
		} finally
		{
			try
			{
				if (pstmt != null)
				{
					pstmt

					.close();
					pstmt = null;
				}
				if (pstmt1 != null)
				{
					pstmt1.close();
					pstmt1 = null;
				}

				if (pstmt2 != null)
				{
					pstmt2.close();
					pstmt2 = null;
				}

				if (pstmtRbHdr != null)
				{
					pstmtRbHdr.close();
					pstmtRbHdr = null;
				}

				if (pstmtRbDet != null)
				{
					pstmtRbDet.close();
					pstmtRbDet = null;
				}

				if (pstmtDspHdr != null)
				{
					pstmtDspHdr.close();
					pstmtDspHdr = null;
				}

				if (pstmtDspDtl != null)
				{
					pstmtDspDtl.close();
					pstmtDspDtl = null;
				}

				if (pstmtUpd != null)
				{
					pstmtUpd.close();
					pstmtUpd = null;
				}
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (rs1 != null)
				{
					rs1.close();
					rs1 = null;
				}
				if (rs2 != null)
				{
					rs2.close();
					rs2 = null;
				}
			} catch (Exception e)
			{
				System.out.println("PoRcpConf:autoBackflush( String tranId, Connection conn):" + e.getMessage());
				e.printStackTrace();//Modified by Anjali R. on [25/10/2018]
				throw new ITMException(e);
			}
		}
		System.out.println("Returning Result ::" + retString);
		return retString;
	}

	/*
	 * public String stockAllocatePur( String purRcpt, String tranType,
	 * Connection con) throws RemoteException, ITMException { PreparedStatement
	 * pstmtSql = null; PreparedStatement pstmtUpd = null; ResultSet rs = null;
	 * 
	 * double Quantity = 0.0; double quantityAlloc = 0.0; double convQty = 0.0;
	 * double lotsQty = 0.0; double lotsSamQty = 0.0;
	 * 
	 * long count = 0; long soCnt = 0; long i = 0; long lineNo = 0; long cnt= 0;
	 * 
	 * String purcOrd = ""; String saleOrder = ""; String status = ""; String
	 * grade = ""; String siteCodeMfg = ""; String itemCode = ""; String lotNo =
	 * ""; String lotSl = ""; String locCode = ""; String retString = ""; String
	 * unit = ""; String unitStd = ""; String lineSord = ""; String itemRef =
	 * ""; String expLev = ""; String siteCode = ""; String itemOrd = ""; String
	 * linepOrder = ""; String qcReqd = ""; String poRcpNo = ""; String
	 * linePoRcp = ""; //String tranType = ""; String qcCreateType = ""; String
	 * sql = "";
	 * 
	 * int updCnt = 0;
	 * 
	 * Date allocDate = null; Date expDate = null; Date mfgDate = null;
	 * 
	 * try { ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
	 * ibase.utility.E12GenericUtility genericUtility= new
	 * ibase.utility.E12GenericUtility(); DateFormat dtFormat = new
	 * SimpleDateFormat(genericUtility.getApplDateTimeFormat()); dbName =
	 * CommonConstants.DB_NAME;
	 * 
	 * if ( tranType.equalsIgnoreCase("P") ) { allocDate = new Date(); int
	 * rowCount = 0; sql = "SELECT COUNT(*) AS COUNT FROM PORCPDET"; pstmtSql =
	 * conn.prepareStatement(sql); rs = pstmtSql.executeQuery(); if ( rs.next()
	 * ) { rowCount = rs.getInt("COUNT"); } if(pstmtSql != null) {
	 * pstmtSql.close(); pstmtSql = null; } if ( rs != null ) { rs.close(); rs =
	 * null; }
	 * 
	 * for ( i = 1; i <= rowCount; i++) { sql =
	 * "SELECT PORCPDET.ITEM_CODE FROM PORCPDET, ITEM ITEM_A, ITEM ITEM_B, ACCOUNTS, SPECIFICATION, PORCP WHERE (		   PORCPDET.ITEM_CODE__MFG = ITEM_B.ITEM_CODE (+)) AND  ( PORCPDET.ACCT_CODE__DR = ACCOUNTS.ACCT_CODE (+)) AND        ( PORCPDET.SPEC_REF = SPECIFICATION.SPEC_REF (+)) AND ( PORCPDET.ITEM_CODE = ITEM_A.ITEM_CODE ) AND (		   PORCPDET.TRAN_ID = PORCP.TRAN_ID ) AND ( ( PORCPDET.TRAN_ID = ? ) ) ORDER BY PORCPDET.LINE_NO ASC "
	 * ; pstmtSql = conn.prepareStatement(sql); pstmtSql.setString(1, purRcpt );
	 * rs = pstmtSql.executeQuery(); if ( rs.next() ) { itemCode =
	 * checkNull(rs.getString("ITEM_CODE")); } if(pstmtSql != null) {
	 * pstmtSql.close(); pstmtSql = null; } if ( rs != null ) { rs.close(); rs =
	 * null; }
	 * 
	 * sql = "SELECT QC_REQD FROM  ITEM WHERE ITEM_CODE = ?"; pstmtSql =
	 * conn.prepareStatement(sql); pstmtSql.setString(1, itemCode ); rs =
	 * pstmtSql.executeQuery(); if ( rs.next() ) { qcReqd =
	 * checkNull(rs.getString("QC_REQD")); } if(pstmtSql != null) {
	 * pstmtSql.close(); pstmtSql = null; } if ( rs != null ) { rs.close(); rs =
	 * null; }
	 * 
	 * if ( qcReqd.equalsIgnoreCase("N") ) { sql =
	 * "SELECT  PORCPDET.LINE_NO, PORCPDET.PURC_ORDER, PORCPDET.QUANTITY, PORCPDET.LOC_CODE, PORCPDET.LOT_NO,          PORCPDET.LOT_SL, PORCPDET.LINE_NO__ORD FROM PORCPDET, ITEM ITEM_A, ITEM ITEM_B, ACCOUNTS,                      SPECIFICATION, PORCP WHERE ( PORCPDET.ITEM_CODE__MFG = ITEM_B.ITEM_CODE (+)) AND (						   PORCPDET.ACCT_CODE__DR = ACCOUNTS.ACCT_CODE (+)) AND ( PORCPDET.SPEC_REF = SPECIFICATION.SPEC_REF (+))         AND ( PORCPDET.ITEM_CODE = ITEM_A.ITEM_CODE ) AND ( PORCPDET.TRAN_ID = PORCP.TRAN_ID ) AND ( (			   PORCPDET.TRAN_ID = ? ) )  ORDER BY PORCPDET.LINE_NO ASC"
	 * ; pstmtSql = conn.prepareStatement(sql); pstmtSql.setString(1, purRcpt );
	 * rs = pstmtSql.executeQuery(); if ( rs.next() ) { linePoRcp =
	 * checkNull(rs.getString("LINE_NO")); purcOrd =
	 * checkNull(rs.getString("PURC_ORDER")); lotNo =
	 * checkNull(rs.getString("LOT_NO")); lotSl =
	 * checkNull(rs.getString("LOT_SL")); locCode =
	 * checkNull(rs.getString("LOC_CODE")); quantityAlloc =
	 * rs.getDouble("QUANTITY"); linepOrder =
	 * checkNull(rs.getString("LINE_NO__ORD")); } if(pstmtSql != null) {
	 * pstmtSql.close(); pstmtSql = null; } if ( rs != null ) { rs.close(); rs =
	 * null; }
	 * 
	 * sql = "SELECT SALE_ORDER FROM PORDER WHERE PURC_ORDER = ?"; pstmtSql =
	 * conn.prepareStatement(sql); pstmtSql.setString(1, purcOrd ); rs =
	 * pstmtSql.executeQuery(); if ( rs.next() ) { saleOrder =
	 * checkNull(rs.getString("SALE_ORDER")); } if(pstmtSql != null) {
	 * pstmtSql.close(); pstmtSql = null; } if ( rs != null ) { rs.close(); rs =
	 * null; }
	 * 
	 * sql =
	 * "SELECT LINE_NO__SORD FROM PORDDET WHERE PURC_ORDER = ? AND LINE_NO = ?";
	 * pstmtSql = conn.prepareStatement(sql); pstmtSql.setString(1, purcOrd );
	 * pstmtSql.setString(2, linepOrder ); rs = pstmtSql.executeQuery(); if (
	 * rs.next() ) { lineSord = checkNull(rs.getString("LINE_NO__SORD")); }
	 * if(pstmtSql != null) { pstmtSql.close(); pstmtSql = null; } if ( rs !=
	 * null ) { rs.close(); rs = null; }
	 * 
	 * if ( lineSord == null || lineSord.length() == 0 ) { return ""; }
	 * 
	 * sql =
	 * "SELECT ITEM_CODE__ORD, EXP_LEV, ITEM_REF, SITE_CODE, QUANTITY - QTY_DESP, UNIT, STATUS FROM SORDITEM		   WHERE SALE_ORDER = ? AND LINE_NO = ? AND LINE_TYPE = ?"
	 * ; pstmtSql = conn.prepareStatement(sql); pstmtSql.setString(1, saleOrder
	 * ); pstmtSql.setString(2, lineSord ); pstmtSql.setString(3, "I" ); rs =
	 * pstmtSql.executeQuery(); if ( rs.next() ) { itemOrd =
	 * checkNull(rs.getString("ITEM_CODE__ORD")); expLev =
	 * checkNull(rs.getString("EXP_LEV")); itemRef =
	 * checkNull(rs.getString("ITEM_REF")); siteCode =
	 * checkNull(rs.getString("SITE_CODE")); quantity = rs.getDouble(5); unit =
	 * checkNull(rs.getString("UNIT")); status =
	 * checkNull(rs.getString("STATUS")); } if(pstmtSql != null) {
	 * pstmtSql.close(); pstmtSql = null; } if ( rs != null ) { rs.close(); rs =
	 * null; }
	 * 
	 * sql =
	 * "SELECT GRADE, EXP_DATE, MFG_DATE, SITE_CODE__MFG FROM STOCK WHERE ITEM_CODE = ?	AND  SITE_CODE = ? 		   AND  LOC_CODE = ? AND LOT_NO = ? AND LOT_SL = ? "
	 * ; pstmtSql = conn.prepareStatement(sql); pstmtSql.setString(1, itemCode
	 * ); pstmtSql.setString(2, siteCode ); pstmtSql.setString(3, locCode );
	 * pstmtSql.setString(4, lotNo ); pstmtSql.setString(5, lotSl); rs =
	 * pstmtSql.executeQuery(); if ( rs.next() ) { grade =
	 * checkNull(rs.getString("GRADE")); expDate =
	 * dtFormat.parse(rs.getString("EXP_DATE")); mfgDate =
	 * dtFormat.parse(rs.getString("MFG_DATE")); siteCodeMfg =
	 * checkNull(rs.getString("SITE_CODE__MFG")); } if(pstmtSql != null) {
	 * pstmtSql.close(); pstmtSql = null; } if ( rs != null ) { rs.close(); rs =
	 * null; }
	 * 
	 * sql =
	 * " SELECT CONV__QTY_STDUOM, UNIT__STD FROM SORDDET WHERE SALE_ORDER = ? AND LINE_NO = ? "
	 * ; pstmtSql = conn.prepareStatement(sql); pstmtSql.setString(1, saleOrder
	 * ); pstmtSql.setString(2, lineSord ); rs = pstmtSql.executeQuery(); if (
	 * rs.next() ) { convQty = rs.getDouble("CONV__QTY_STDUOM"); unitStd =
	 * checkNull(rs.getString("UNIT__STD")); } if(pstmtSql != null) {
	 * pstmtSql.close(); pstmtSql = null; } if ( rs != null ) { rs.close(); rs =
	 * null; }
	 * 
	 * sql =
	 * "SELECT COUNT(*)  AS COUNT FROM SORDALLOC WHERE SALE_ORDER = ? AND LINE_NO = ? AND EXP_LEV = ? AND 		   ITEM_CODE__ORD = ? AND ITEM_CODE = ? AND LOT_NO = ? AND LOT_SL = ? AND LOC_CODE = ?"
	 * ; pstmtSql = conn.prepareStatement(sql); pstmtSql.setString(1, saleOrder
	 * ); pstmtSql.setString(2, lineSord ); pstmtSql.setString(3, expLev );
	 * pstmtSql.setString(4, itemOrd ); pstmtSql.setString(5, itemCode );
	 * pstmtSql.setString(6, lotNo ); pstmtSql.setString(7, lotSl );
	 * pstmtSql.setString(8, locCode ); rs = pstmtSql.executeQuery(); if (
	 * rs.next() ) { soCnt = rs.getLong("COUNT"); } if(pstmtSql != null) {
	 * pstmtSql.close(); pstmtSql = null; } if ( rs != null ) { rs.close(); rs =
	 * null; }
	 * 
	 * if ( soCnt == 0 ) { sql =
	 * "INSERT INTO SORDALLOC (SALE_ORDER, LINE_NO, EXP_LEV, ITEM_CODE__ORD, ITEM_CODE, LOT_NO,				   LOT_SL, LOC_CODE, ITEM_REF, QUANTITY, UNIT, QTY_ALLOC, DATE_ALLOC,									   STATUS, ITEM_GRADE, EXP_DATE, ALLOC_MODE, SITE_CODE, CONV__QTY_STDUOM,								   UNIT__STD, QUANTITY__STDUOM, MFG_DATE, SITE_CODE__MFG, REF_ID__ALLOC, REF_LINE__NO) VALUES ( ?		   , ? , ?, ?, ? , ?, ? , ? , ? , ? , ? , ? ,  ? , ? , ? , ? , ? , ? , ? , ? , ? , ?, ? , ? , ?)"
	 * ; pstmtSql = conn.prepareStatement(sql); pstmtSql.setString(1, saleOrder
	 * ); pstmtSql.setString(2, lineSord ); pstmtSql.setString(3, expLev);
	 * pstmtSql.setString(4, itemOrd ); pstmtSql.setString(5, itemCode );
	 * pstmtSql.setString(6, lotNo ); pstmtSql.setString(7, lotSl );
	 * pstmtSql.setString(8, locCode ); pstmtSql.setString(9, itemRef );
	 * pstmtSql.setDouble(10, quantity ); pstmtSql.setString(11, unit );
	 * pstmtSql.setDouble(12, quantityAlloc ); pstmtSql.setDate(13, allocDate );
	 * pstmtSql.setString(14, status ); pstmtSql.setString(15, grade );
	 * pstmtSql.setDate(16, expDate ); pstmtSql.setString(17, "A" );
	 * pstmtSql.setString(18, siteCode ); pstmtSql.setDouble(19, convQty );
	 * pstmtSql.setString(20, unitStd ); pstmtSql.setDouble(21, quantityAlloc *
	 * convQty ); pstmtSql.setDate(22, mfgDate ); pstmtSql.setString(23,
	 * siteCodeMfg ); pstmtSql.setString(24, purRcpt ); pstmtSql.setString(25,
	 * linePoRcp ); rs = pstmtSql.executeQuery(); if(pstmtSql != null) {
	 * pstmtSql.close(); pstmtSql = null; } if ( rs != null ) { rs.close(); rs =
	 * null; } } else { sql =
	 * "UPDATE SORDALLOC SET QTY_ALLOC = QTY_ALLOC + ?, QUANTITY__STDUOM = (QTY_ALLOC + ? ) * ?, 			   STATUS = ?, REF_ID__ALLOC = ? , REF_LINE__NO	= ?  WHERE SALE_ORDER = ? AND LINE_NO = ? 				   AND EXP_LEV = ? AND ITEM_CODE__ORD = ? AND ITEM_CODE = ? AND LOT_NO = ? AND LOT_SL = ? 				   AND LOC_CODE = ?"
	 * ; pstmtUpd = conn.prepareStatement(sql); pstmtUpd.setDouble(1,
	 * quantityAlloc ); pstmtUpd.setDouble(2, quantityAlloc );
	 * pstmtUpd.setDouble(3, convQty ); pstmtUpd.setString(4, "D" );
	 * pstmtUpd.setString(5, purRcpt ); pstmtUpd.setString(6, linePoRcp );
	 * pstmtUpd.setString(7, saleOrder ); pstmtUpd.setString(8, lineSord );
	 * pstmtUpd.setString(9, expLev ); pstmtUpd.setString(10, itemOrd );
	 * pstmtUpd.setString(11, itemCode ); pstmtUpd.setString(12, lotNo );
	 * pstmtUpd.setString(13, lotSl ); pstmtUpd.setString(14, locCode ); updCnt
	 * = pstmtUpd.executeUpdate(); if(pstmtUpd != null) { pstmtUpd.close();
	 * pstmtUpd = null; } }
	 * 
	 * sql = "UPDATE SORDER SET ALLOC_FLAG = ?	WHERE SALE_ORDER = ?"; pstmtUpd =
	 * conn.prepareStatement(sql); pstmtUpd.setDouble(1, "Y" );
	 * pstmtUpd.setDouble(2, saleOrder ); updCnt = pstmtUpd.executeUpdate();
	 * if(pstmtUpd != null) { pstmtUpd.close(); pstmtUpd = null; }
	 * 
	 * sql =
	 * "UPDATE SORDITEM SET QTY_ALLOC = QTY_ALLOC + ? WHERE SALE_ORDER = ? AND LINE_NO = ? AND EXP_LEV = ?"
	 * ; pstmtUpd = conn.prepareStatement(sql); pstmtUpd.setDouble(1,
	 * quantityAlloc ); pstmtUpd.setString(2, saleOrder ); pstmtUpd.setString(3,
	 * lineSord ); pstmtUpd.setString(4, expLev ); updCnt =
	 * pstmtUpd.executeUpdate(); if(pstmtUpd != null) { pstmtUpd.close();
	 * pstmtUpd = null; }
	 * 
	 * 
	 * if ( quantityAlloc == null ) { quantityAlloc = 0.0; }
	 * //lstr_allocate.tran_date = datetime(today()) //lstr_allocate.ref_ser =
	 * 'P-RCP' //lstr_allocate.ref_id = as_pur_rcpt // 23-09-04 manoharan
	 * line_no is char(3) //lstr_allocate.ref_line = string(li_line_no)
	 * //lstr_allocate.ref_line = ls_line_porcp // end 23-09-04 manoharan
	 * //lstr_allocate.site_code = ls_sitecode //lstr_allocate.item_code =
	 * ls_itemcode //lstr_allocate.loc_code = ls_loc_code //lstr_allocate.lot_no
	 * = ls_lotno //lstr_allocate.lot_sl = ls_lotsl //lstr_allocate.alloc_qty =
	 * lc_quantity_alloc //lstr_allocate.chg_user = userid
	 * //lstr_allocate.chg_term = termid //lstr_allocate.chg_win = "W_PORCP"
	 * 
	 * //retString = lnvo_allocate.gbf_upd_alloc_trace(lstr_allocate)
	 * 
	 * if ( retString != null && retString.trim().length() > 0 ) { break; } } }
	 * } else if ( tranType.equalsIgnoreCase("Q") ) { // just commented not
	 * clear as it is incomplete // allocDate = new Date(); // //sql = "";
	 * //ls_itemcode = lds_pur_rcpt_dtl.getitemstring(1,"item_code__new")
	 * //ls_porcpno = lds_pur_rcpt_dtl.getitemstring(1,"porcp_no")
	 * //ls_line_porcp = lds_pur_rcpt_dtl.getitemstring(1,"porcp_line_no") // }
	 * } catch(Exception e) { e.printStackTrace();
	 * System.out.println("Exception ::"+e.getMessage()); throw new
	 * ITMException(e); } finally { try { if(pstmtSql != null) {
	 * pstmtSql.close(); pstmtSql = null; } if( pstmtUpd != null) {
	 * pstmtUpd.close(); pstmtUpd = null; } } catch(Exception e) {
	 * System.out.println(e.getMessage()); throw new ITMException(e); } }
	 * System.out.println("Returning Result ::"+retString); return retString; }
	 */

	public String createAssetInstall(String tranId, String qcOrderNo, Connection conn, String xtraParams) throws RemoteException, ITMException
	{
		PreparedStatement pstmt = null, pstmt1 = null, pstmtHdr = null, pstmtDet = null;
		ResultSet rs = null, rs1 = null;

		String retString = "", key = "", keyStr = "", siteCode = "", tranSer = "", itemSer = "", suppCode = "";
		String currCode = "", acctCode = "", cctrCode = "", grpCode = "", itemCode = "", remarks = "", assetCode = "";
		String lineNo = "", locCode = "", lotNo = "", lotSl = "", suppName = "", token = "", string = "", varValue = "";
		String sql = "", sqlHdr = "", sqlDet = "";
		String cctrCodeDr = "";
		String alocCode = "";
		String excTaxCodeAsset = "";
		String excTaxCode = "";
		String excTaxCodeSep = "";
		String assetInstallTax = "", assetInstallTaxSep = "", acctCodeAp = "", cwipSubGroup = "", sgroupCode = "", subGroup = "";
		String tranType = "", purcOrder = "", lineNoOrd = "", projCode = "", billNo = "", dcNo = "", invoiceNo = "";
		String locCodeAprv = "", qcLocCode = "", qcItemCode = "", qcLotNoNew = "", qcLotNo = "", commVarValue = "", octroiVarValue = "";
		String octroiVarValueSep = "", commVarValueSep = "", generatedTranId = "", loginEmpCode = "";

		double othChg = 0.0, exchRate = 0.0, rate = 0.0, netAmt = 0.0, taxRecoAmt = 0.0;
		double disc = 0.0, quantity = 0.0, passed = 0.0, totQuantity = 0.0;
		double octroiChgs = 0.0, commChgs = 0.0, taxExciseAmt = 0.0, taxExciseRecoAmt = 0.0;
		double rcprecoAmt = 0.0, rcptaxAmt = 0.0, exciseAmt = 0.0, taxAmt = 0.0, excrecoAmt = 0.0;
		double discAmt = 0.0, taxInstallChgs = 0.0, taxAmount = 0.0, originalValue = 0.0;
		String itmser = "", assetclass = "", finentity = "";
		double usefullife = 0.0;
		Timestamp tranDate = null, billDate = null, dcDate = null, invoiceDate = null;

		String arrStr[] = null;
		int len = 0, count = 0;
		int cnt = 0, cnt1 = 0;

		//added by Nandkumar Gadkari on 08/03/18-- Start 	
		StringBuffer xmlBuff = null;
		SimpleDateFormat sdf  = null;
		ArrayList<String> assetInstallList = null;
		double  instChgsBc= 0.0 , originalValueBc= 0.0, taxRecoAmtexc=0.0;
		String returnString="";
		int lNo = 0;
		//added by Nandkumar Gadkari on 08/03/18 ------- end

		try
		{
			FinCommon finCommon = new FinCommon();
			DistCommon distCommon = new DistCommon();
			ITMDBAccessEJB itmDbAccess = new ITMDBAccessEJB();
			ibase.utility.E12GenericUtility genericUtility = new ibase.utility.E12GenericUtility();

			loginEmpCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			DateFormat dtFormat = new SimpleDateFormat(genericUtility.getApplDateTimeFormat());
			String today = dtFormat.format(new java.util.Date());
			AssetInstall assetInstall= new AssetInstall();// object Created by Nandkumar Gadkari on 08/03/18 

			if ((qcOrderNo == null) || (qcOrderNo.trim().length() == 0))
			{
				qcOrderNo = "";
			}
			System.out.println("In createAssetInstall Function ");
			sql = "SELECT TRAN_DATE, SITE_CODE, TRAN_SER, ITEM_SER, SUPP_CODE, CURR_CODE, OTHER_CHARGES, REMARKS, CASE WHEN EXCH_RATE IS NULL THEN 0 ELSE EXCH_RATE END AS EXCH_RATE, DC_NO, DC_DATE, INVOICE_NO, INVOICE_DATE FROM PORCP WHERE TRAN_ID = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				tranDate = rs.getTimestamp("TRAN_DATE");
				siteCode = checkNull(rs.getString("SITE_CODE"));
				tranSer = checkNull(rs.getString("TRAN_SER"));
				itemSer = checkNull(rs.getString("ITEM_SER"));
				suppCode = checkNull(rs.getString("SUPP_CODE"));
				currCode = checkNull(rs.getString("CURR_CODE"));
				othChg = rs.getDouble("OTHER_CHARGES");
				remarks = checkNull(rs.getString("REMARKS"));
				exchRate = rs.getDouble("EXCH_RATE");
				dcNo = checkNull(rs.getString("DC_NO"));
				dcDate = rs.getTimestamp("DC_DATE");
				invoiceNo = checkNull(rs.getString("INVOICE_NO"));
				invoiceDate = rs.getTimestamp("INVOICE_DATE");
				tranType = "C";
			} else
			{
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;
				retString = itmDbAccess.getErrorString("", "VTPORCP1", "","",conn);
				return retString;
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;

			excTaxCode = distCommon.getDisparams("999999", "EXC_TAX_CODE", conn);
			if (excTaxCode != null && excTaxCode.trim().length() > 0 && !("NULLFOUND").equalsIgnoreCase(excTaxCode))
			{
				excTaxCodeSep = "";
				arrStr = excTaxCode.split(",");
				len = arrStr.length;

				for (int i = 0; i < len; i++)
				{
					if (i == (len - 1))
					{
						excTaxCodeSep = excTaxCodeSep + "'" + arrStr[i] + "'";
					} else
					{
						excTaxCodeSep = excTaxCodeSep + "'" + arrStr[i] + "',";
					}
				}
			} else
			{
				excTaxCode = "";
			}

			assetInstallTax = distCommon.getDisparams("999999", "ASSET_REGISTER_INSTALL_TAXCODE", conn);

			if (assetInstallTax != null && assetInstallTax.trim().length() > 0 && !("NULLFOUND").equalsIgnoreCase(assetInstallTax))
			{
				assetInstallTaxSep = "";
				arrStr = assetInstallTax.split(",");
				len = arrStr.length;
				for (int i = 0; i < len; i++)
				{
					if (i == (len - 1))
					{
						assetInstallTaxSep = assetInstallTaxSep + "'" + arrStr[i] + "'";
					} else
					{
						assetInstallTaxSep = assetInstallTaxSep + "'" + arrStr[i] + "',";
					}
				}
			} else
			{
				assetInstallTaxSep = "";
			}

			octroiVarValue = distCommon.getDisparams("999999", "ASSET_REGISTER_OCTROI_TAXCODE", conn);
			if (octroiVarValue != null && octroiVarValue.trim().length() > 0 && !("NULLFOUND").equalsIgnoreCase(octroiVarValue))
			{
				octroiVarValueSep = "";
				arrStr = octroiVarValue.split(",");
				len = arrStr.length;
				for (int i = 0; i < len; i++)
				{
					if (i == (len - 1))
					{
						octroiVarValueSep = octroiVarValueSep + "'" + arrStr[i] + "'";
					} else
					{
						octroiVarValueSep = octroiVarValueSep + "'" + arrStr[i] + "',";
					}
				}
			} else
			{
				octroiVarValueSep = "";
			}

			commVarValue = distCommon.getDisparams("999999", "ASSET_REGISTER_COMM_TAXCODE", conn);
			if (commVarValue != null && commVarValue.trim().length() > 0 && !("NULLFOUND").equalsIgnoreCase(commVarValue))
			{
				commVarValueSep = "";
				arrStr = commVarValue.split(",");
				len = arrStr.length;
				for (int i = 0; i < len; i++)
				{
					if (i == (len - 1))
					{
						commVarValueSep = commVarValueSep + "'" + arrStr[i] + "'";
					} else
					{
						commVarValueSep = commVarValueSep + "'" + arrStr[i] + "',";
					}
				}
			} else
			{
				commVarValueSep = "";
			}
			if (invoiceNo != null && invoiceNo.trim().length() > 0)
			{
				billNo = invoiceNo;
				billDate = invoiceDate;
			} else
			{
				billNo = dcNo;
				billDate = dcDate;
			}

			/*
			 * sql =
			 * "SELECT KEY_STRING FROM TRANSETUP WHERE UPPER(TRAN_WINDOW) = ?";
			 * pstmt = conn.prepareStatement(sql); pstmt.setString(1,
			 * "W_ASSET_INSTALL"); rs = pstmt.executeQuery(); if (rs.next()) {
			 * key = rs.getString("KEY_STRING"); } else { sql =
			 * "SELECT KEY_STRING FROM TRANSETUP WHERE UPPER(TRAN_WINDOW) = ?";
			 * pstmt = conn.prepareStatement(sql); pstmt.setString(1,
			 * "GENERAL"); rs = pstmt.executeQuery();
			 * 
			 * if (rs.next()) { key = rs.getString("KEY_STRING"); } else {
			 * retString = itmDBAccessLocal.getErrorString("", "NOKEYSTRING",
			 * "","",conn); //break; } if (pstmt != null) { pstmt.close(); pstmt = null;
			 * } if (rs != null) { rs.close(); rs = null; } } if (pstmt != null)
			 * { pstmt.close(); pstmt = null; } if (rs != null) { rs.close(); rs
			 * = null; }
			 */
			/*
			 * sql =
			 * "SELECT KEY_STRING FROM TRANSETUP WHERE UPPER(TRAN_WINDOW) = ?";
			 * pstmt = conn.prepareStatement(sql); pstmt.setString(1,
			 * "W_ASSET_REGISTER"); rs = pstmt.executeQuery(); if (rs.next()) {
			 * keyStr = rs.getString("KEY_STRING"); } else { retString =
			 * itmDBAccessLocal.getErrorString("", "VTSEQ", "","",conn); //break; } if
			 * (pstmt != null) { pstmt.close(); pstmt = null; } if (rs != null)
			 * { rs.close(); rs = null; }
			 */

			//commented by Nandkumar Gadkari on 08/03/18 
			/*sqlHdr = "INSERT INTO ASSET_INSTALL(TRAN_ID,TRAN_DATE, SITE_CODE, ASSET_CODE, ASSET_CODE__PAR, ALOC_CODE, ASSET_TYPE, DESCR,INST_DATE, USE_DATE, REMARKS, EMP_CODE, ITEM_CODE, GRP_CODE, CURR_CODE, EXCH_RATE, CHG_USER, CHG_DATE, CHG_TERM, ASSET_SALE_TYPE, CONFIRMED, TRAN_TYPE, PROJ_CODE, TRAN_ID__RCP,CCTR_CODE,EXCH_RATE__INST,INSTALL_CHGS,OCTROI_CHGS,COMM_CHGS,INST_CHGS__BC,ACCT_CODE__INST,CCTR_CODE__INST,EXCRECO_AMT,ORIGINAL_VALUE,ORIGINAL_VALUE__BC,asset_class,useful_life) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			pstmtHdr = conn.prepareStatement(sqlHdr);

			sqlDet = "INSERT INTO ASSET_INSTALLDET(TRAN_ID, LINE_NO, TRAN_ID__RCP, LINE_NO__RCP, ITEM_CODE, LOC_CODE, LOT_NO, LOT_SL,QUANTITY, RATE, SUPP_CODE, SUPP_NAME, RCP_DATE, BILL_NO, BILL_DATE, CURR_CODE, EXCH_RATE, GRP_CODE, ASSET_CODE, TAX_AMT, TAX_RECO_AMT, EXCISE_AMT, EXCRECO_AMT, ORIGINAL_VALUE )	VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			pstmtDet = conn.prepareStatement(sqlDet);*/
			//commented end by  nandkumar Gadkari on 08/03/18 
			sql = "SELECT LINE_NO, ITEM_CODE, QUANTITY, RATE, NET_AMT, LOC_CODE, LOT_NO, LOT_SL, DISCOUNT, CCTR_CODE__DR, PURC_ORDER, LINE_NO__ORD FROM PORCPDET WHERE TRAN_ID = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			totQuantity = 0.0;
			assetInstallList = new ArrayList<String>(); 	//Added by Nandkumar Gadkari on 08/03/18 
			while (rs.next())
			{
				count++;
				lineNo = checkNull(rs.getString("LINE_NO"));
				itemCode = checkNull(rs.getString("ITEM_CODE"));
				quantity = rs.getDouble("QUANTITY");
				rate = rs.getDouble("RATE");
				netAmt = rs.getDouble("NET_AMT");
				locCode = checkNull(rs.getString("LOC_CODE"));
				lotNo = checkNullFrLotSl(rs.getString("LOT_NO"));
				lotSl = checkNullFrLotSl(rs.getString("LOT_SL"));
				disc = rs.getDouble("DISCOUNT");
				cctrCodeDr = checkNull(rs.getString("CCTR_CODE__DR"));
				purcOrder = checkNull(rs.getString("PURC_ORDER"));
				lineNoOrd = checkNull(rs.getString("LINE_NO__ORD"));

				taxExciseAmt = 0.0;
				taxExciseRecoAmt = 0.0;
				rcprecoAmt = 0.0;
				rcptaxAmt = 0.0;
				exciseAmt = 0.0;
				taxAmt = 0.0;
				excrecoAmt = 0.0;
				discAmt = 0.0;
				taxInstallChgs = 0.0;
				taxAmount = 0.0;
				originalValue = 0.0;
				taxRecoAmt = 0.0;

				if (qcOrderNo != null && qcOrderNo.trim().length() > 0)
				{
					sql = "SELECT LOC_CODE__APRV, LOC_CODE, ITEM_CODE, (CASE WHEN QTY_PASSED IS NULL THEN 0 ELSE QTY_PASSED END) AS QTY_PASSED,LOT_NO__NEW, LOT_NO FROM QC_ORDER WHERE QORDER_NO = ? ";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, qcOrderNo);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						locCodeAprv = checkNull(rs1.getString("LOC_CODE__APRV"));
						qcLocCode = checkNull(rs1.getString("LOC_CODE"));
						qcItemCode = checkNull(rs1.getString("ITEM_CODE"));
						passed = rs1.getDouble("QTY_PASSED");
						qcLotNoNew = checkNullFrLotSl(rs1.getString("LOT_NO__NEW"));
						qcLotNo = checkNull(rs1.getString("LOT_NO"));
					}
					pstmt1.close();
					pstmt1 = null;
					rs1.close();
					rs1 = null;

					if ((qcLocCode == locCode) && (qcItemCode == itemCode) && (qcLotNo == lotNo) && (totQuantity < passed))
					{
						locCode = locCodeAprv;
						lotNo = qcLotNoNew;
						totQuantity = totQuantity + quantity;
					}
				}

				sql = "SELECT PROJ_CODE FROM PORDDET WHERE PURC_ORDER = ? AND LINE_NO  = ?";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, purcOrder);
				pstmt1.setString(2, lineNoOrd);

				rs1 = pstmt1.executeQuery();
				if (rs1.next())
				{
					projCode = checkNull(rs1.getString("PROJ_CODE"));
				} else
				{
					pstmt1.close();
					pstmt1 = null;
					rs1.close();
					rs1 = null;
					retString = itmDbAccess.getErrorString("", "NOPROJCODE", "","",conn);
					return retString;
				}
				pstmt1.close();
				pstmt1 = null;
				rs1.close();
				rs1 = null;

				sql = "SELECT GRP_CODE FROM ITEM  WHERE ITEM_CODE = ? ";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, itemCode);
				rs1 = pstmt1.executeQuery();
				if (rs1.next())
				{
					grpCode = checkNull(rs1.getString("GRP_CODE"));
				}
				pstmt1.close();
				pstmt1 = null;
				rs1.close();
				rs1 = null;

				// *****
				sql = "SELECT fin_entity FROM site  WHERE site_code = ? ";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, siteCode);
				rs1 = pstmt1.executeQuery();
				if (rs1.next())
				{
					// finentity = checkNull(rs1.getString("fin_entity"));
					finentity = rs1.getString(1);
				}
				pstmt1.close();
				pstmt1 = null;
				rs1.close();
				rs1 = null;
				sql = "SELECT count(*) FROM asset_class WHERE  item_ser=? and grp_code=? and fin_entity=? ";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, itemSer);
				pstmt1.setString(2, grpCode);
				pstmt1.setString(3, finentity);
				rs1 = pstmt1.executeQuery();
				if (rs1.next())
				{
					cnt = rs1.getInt(1);
				}
				rs1.close();
				rs1 = null;
				pstmt1.close();
				pstmt1 = null;
				System.out.println("@@@@@@@@111 cnt[" + cnt + "]");
				if (cnt == 1)
				{
					sql = "select asset_class,useful_life from asset_class where item_ser=? and grp_code=? and fin_entity=? ";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, itemSer);
					pstmt1.setString(2, grpCode);
					pstmt1.setString(3, finentity);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						assetclass = rs1.getString(1);
						System.out.println("Asset class" + rs1.getDouble(2));
						usefullife = rs1.getDouble(2);
					}
					pstmt1.close();
					pstmt1 = null;
					rs1.close();
					rs1 = null;

				} else
				{
					sql = "SELECT count(*) FROM asset_class WHERE  item_ser=? and grp_code=?";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, itemSer);
					pstmt1.setString(2, grpCode);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						cnt1 = rs1.getInt(1);
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					System.out.println("@@@@@@@@2222222222 cnt[" + cnt1 + "]");
					if (cnt1 == 1)
					{
						sql = "select asset_class,useful_life from asset_class where item_ser=? and grp_code=?";
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1, itemSer);
						pstmt1.setString(2, grpCode);
						rs1 = pstmt1.executeQuery();
						if (rs1.next())
						{
							assetclass = rs1.getString(1);
							usefullife = rs1.getDouble(2);
						}
						pstmt1.close();
						pstmt1 = null;
						rs1.close();
						rs1 = null;
					}
				}
				assetCode = this.generateTranId("w_asset_register", siteCode, today, conn);
				System.out.println("Asset code is =" + assetCode);
				if (assetCode == null || ("ERROR").equals(assetCode) || assetCode.trim().length() == 0)
				{
					retString = itmDbAccess.getErrorString("", "VTASSETCD", "","",conn);
					return retString;
				}

				generatedTranId = this.generateTranId("w_asset_install", siteCode, today, conn);

				System.out.println("GeneratedTranId  is =" + generatedTranId);
				sql = "SELECT ALOC_CODE FROM ASSET_LOCATION WHERE SITE_CODE = ? ";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, siteCode);
				rs1 = pstmt1.executeQuery();
				if (rs1.next())
				{
					alocCode = checkNull(rs1.getString("ALOC_CODE"));
				} else
				{
					pstmt1.close();
					pstmt1 = null;
					rs1.close();
					rs1 = null;
					retString = itmDbAccess.getErrorString("", "NOALOCCODE", "","",conn);
					return retString;
				}
				pstmt1.close();
				pstmt1 = null;
				rs1.close();
				rs1 = null;

				if (assetInstallTaxSep != null && assetInstallTaxSep.trim().length() > 0)
				{
					sql = "SELECT SUM(CASE WHEN TAX_AMT IS NULL THEN 0 ELSE TAX_AMT END - CASE WHEN RECO_AMOUNT IS NULL THEN 0 ELSE	 RECO_AMOUNT END) FROM   TAXTRAN  WHERE  TRAN_CODE = 'P-RCP'  AND TRAN_ID = ? AND LINE_NO = ? AND TAX_CODE IN (?)";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, tranId);
					pstmt1.setString(2, lineNo);
					pstmt1.setString(3, assetInstallTaxSep);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						taxInstallChgs = rs1.getDouble(1);
					}
					pstmt1.close();
					pstmt1 = null;
					rs1.close();
					rs1 = null;
				}

				if (octroiVarValueSep != null && octroiVarValueSep.trim().length() > 0)
				{
					sql = " SELECT SUM(CASE WHEN TAX_AMT IS NULL THEN 0 ELSE TAX_AMT END - CASE WHEN RECO_AMOUNT IS NULL THEN 0 ELSE RECO_AMOUNT END) FROM  TAXTRAN WHERE  TRAN_CODE = 'P-RCP' AND TRAN_ID = ? AND LINE_NO = ? AND TAX_CODE IN (?)";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, tranId);
					pstmt1.setString(2, lineNo);
					pstmt1.setString(3, octroiVarValueSep);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						octroiChgs = rs1.getDouble(1);
					}
					pstmt1.close();
					pstmt1 = null;
					rs1.close();
					rs1 = null;
				}

				if (commVarValueSep != null && commVarValueSep.trim().length() > 0)
				{
					sql = " SELECT SUM(CASE WHEN TAX_AMT IS NULL THEN 0 ELSE TAX_AMT END - CASE WHEN RECO_AMOUNT IS NULL THEN 0 ELSE RECO_AMOUNT END) FROM  TAXTRAN WHERE  TRAN_CODE = 'P-RCP' AND TRAN_ID = ? AND LINE_NO = ? AND TAX_CODE IN(?)";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, tranId);
					pstmt1.setString(2, lineNo);
					pstmt1.setString(3, commVarValueSep);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						commChgs = rs1.getDouble(1);
					}
					pstmt1.close();
					pstmt1 = null;
					rs1.close();
					rs1 = null;
				}

				cctrCode = finCommon.getFinparams("999999", "ACCT_CWIP", conn);
				if (cctrCode != null && cctrCode.trim().length() > 0 && !("NULLFOUND").equalsIgnoreCase(cctrCode))
				{
					String[] cctrStr = cctrCode.split(";");
					int length = cctrStr.length - 1;
					System.out.println("@@@cr len[" + length + "]");
					if (length > -1)
					{
						acctCode = cctrStr[0];
					} else
					{
						acctCode = "";
					}

					if (len > 0)
					{
						cctrCode = cctrStr[1];
					} else
					{
						cctrCode = "";
					}

				}

				sql = "SELECT SUM(CASE WHEN RECO_AMOUNT IS NULL THEN 0 ELSE RECO_AMOUNT END) AS RECO_AMOUNT, SUM(CASE WHEN TAX_AMT IS NULL THEN 0 ELSE TAX_AMT END) AS TAX_AMT  FROM  TAXTRAN WHERE  TRAN_CODE = 'P-RCP' AND TRAN_ID = ? AND LINE_NO = ?";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, tranId);
				pstmt1.setString(2, lineNo);
				rs1 = pstmt1.executeQuery();
				if (rs1.next())
				{
					taxRecoAmt = rs1.getDouble("RECO_AMOUNT");
					taxAmt = rs1.getDouble("TAX_AMT");
				}
				pstmt1.close();
				pstmt1 = null;
				rs1.close();
				rs1 = null;

				if (excTaxCodeSep != null && excTaxCodeSep.trim().length() > 0)
				{
					sql = "SELECT (CASE WHEN SUM(RECO_AMOUNT) IS NULL THEN 0 ELSE SUM(RECO_AMOUNT) END) AS RECO_AMOUNT, (CASE WHEN SUM(TAX_AMT) IS NULL THEN 0 ELSE SUM(TAX_AMT) END) AS TAX_AMT FROM TAXTRAN WHERE TRAN_CODE = 'P-RCP' AND TRAN_ID = ? AND LINE_NO = ? AND TAX_CODE IN(" + excTaxCodeSep + ") ";

					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, tranId);
					pstmt1.setString(2, lineNo);
					// pstmt1.setString(3, excTaxCodeSep);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						taxExciseRecoAmt = rs1.getDouble("RECO_AMOUNT");
						taxExciseAmt = rs1.getDouble("TAX_AMT");
					}
					pstmt1.close();
					pstmt1 = null;
					rs1.close();
					rs1 = null;
				}

				if (excTaxCodeAsset != null && excTaxCodeAsset.trim().length() > 0)
				{
					sql = "SELECT (CASE WHEN  SUM(RECO_AMOUNT) IS NULL THEN 0 ELSE SUM(RECO_AMOUNT) END) AS RECO_AMOUNT, (CASE WHEN SUM(TAX_AMT) IS NULL THEN 0	ELSE SUM(TAX_AMT) END) AS TAX_AMT FROM   TAXTRAN WHERE  TRAN_CODE = 'P-RCP' AND TRAN_ID = ? AND LINE_NO = ? AND TAX_CODE IN	(" + excTaxCodeAsset + ")";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, tranId);
					pstmt1.setString(2, lineNo);
					// pstmt1.setString(3, excTaxCodeAsset);
					rs1 = pstmt1.executeQuery();
					if (rs1.next())
					{
						rcprecoAmt = rs1.getDouble("RECO_AMOUNT");
						rcptaxAmt = rs1.getDouble("TAX_AMT");
					}
					pstmt1.close();
					pstmt1 = null;
					rs1.close();
					rs1 = null;
				}
				taxAmount = (taxAmt - taxRecoAmt - taxInstallChgs - octroiChgs - commChgs);
				exciseAmt = (taxExciseAmt - taxExciseRecoAmt) + (rcptaxAmt - rcprecoAmt);
				excrecoAmt = taxExciseRecoAmt + rcprecoAmt;
				discAmt = (rate * quantity * disc) / 100;
				originalValue = (rate * quantity) + taxAmount - discAmt;

				//commented by Nandkumar Gadkari on 08/03/18 
				/*pstmtHdr.setString(1, generatedTranId);
				pstmtHdr.setTimestamp(2, this.getCurrtDate());
				pstmtHdr.setString(3, siteCode);
				pstmtHdr.setString(4, assetCode);
				pstmtHdr.setString(5, assetCode);
				pstmtHdr.setString(6, alocCode);
				pstmtHdr.setString(7, "D");
				pstmtHdr.setString(8, remarks);
				pstmtHdr.setTimestamp(9, tranDate);
				pstmtHdr.setTimestamp(10, tranDate);
				pstmtHdr.setString(11, remarks);
				pstmtHdr.setString(12, loginEmpCode);
				pstmtHdr.setString(13, itemCode);
				pstmtHdr.setString(14, grpCode);
				pstmtHdr.setString(15, currCode);
				pstmtHdr.setDouble(16, exchRate);
				pstmtHdr.setString(17, this.userId);
				pstmtHdr.setTimestamp(18, this.getCurrtDate());
				pstmtHdr.setString(19, this.termId);
				pstmtHdr.setString(20, "N");
				pstmtHdr.setString(21, "N");
				pstmtHdr.setString(22, tranType);
				pstmtHdr.setString(23, projCode);
				pstmtHdr.setString(24, tranId);
				pstmtHdr.setString(25, cctrCodeDr);
				// pstmtHdr.setString(26, currCode);
				pstmtHdr.setDouble(26, exchRate);
				pstmtHdr.setDouble(27, taxInstallChgs);
				pstmtHdr.setDouble(28, octroiChgs);
				pstmtHdr.setDouble(29, commChgs);
				pstmtHdr.setDouble(30, taxInstallChgs * exchRate);
				pstmtHdr.setString(31, acctCode);
				pstmtHdr.setString(32, cctrCode);
				pstmtHdr.setDouble(33, excrecoAmt);
				pstmtHdr.setDouble(34, originalValue);
				pstmtHdr.setDouble(35, originalValue * exchRate);
				pstmtHdr.setString(36, assetclass);
				pstmtHdr.setDouble(37, usefullife);

				pstmtHdr.addBatch();
				pstmtHdr.clearParameters();

				pstmtDet.setString(1, generatedTranId);
				pstmtDet.setString(2, "1");
				pstmtDet.setString(3, tranId);
				pstmtDet.setString(4, lineNo);
				pstmtDet.setString(5, itemCode);
				pstmtDet.setString(6, locCode);
				pstmtDet.setString(7, lotNo);
				pstmtDet.setString(8, lotSl);
				pstmtDet.setDouble(9, quantity);
				pstmtDet.setDouble(10, rate);
				pstmtDet.setString(11, suppCode);
				pstmtDet.setString(12, suppName);
				pstmtDet.setTimestamp(13, tranDate);
				pstmtDet.setString(14, billNo);
				pstmtDet.setTimestamp(15, billDate);
				pstmtDet.setString(16, currCode);
				pstmtDet.setDouble(17, exchRate);
				pstmtDet.setString(18, grpCode);
				pstmtDet.setString(19, assetCode);
				pstmtDet.setDouble(20, taxAmount);
				pstmtDet.setDouble(21, taxRecoAmt - excrecoAmt);
				pstmtDet.setDouble(22, exciseAmt);
				pstmtDet.setDouble(23, excrecoAmt);
				pstmtDet.setDouble(24, originalValue);

				pstmtDet.addBatch();
				pstmtDet.clearParameters();*/
				//commented end by Nandkumar Gadkari on 08/03/18 

				//added by Nandkumar Gadkari on 08/03/18  --start---Asset installation to be made independent of purchase receipt confirmation.
				if( xmlBuff == null )
				{
					xmlBuff = new StringBuffer();
				}

				xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
				xmlBuff.append("<DocumentRoot>");
				xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
				xmlBuff.append("<group0>");
				xmlBuff.append("<description>").append("Group0 description").append("</description>");
				xmlBuff.append("<Header0>");
				xmlBuff.append("<objName><![CDATA[").append("asset_install").append("]]></objName>");      
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
				xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"asset_install\" objContext=\"1\">"); 
				xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
				xmlBuff.append("<tran_id/>");
				sdf =  new SimpleDateFormat(genericUtility.getApplDateFormat());
				xmlBuff.append("<tran_date><![CDATA["+ sdf.format(new java.util.Date()).toString() +"]]></tran_date>");
				xmlBuff.append("<site_code><![CDATA["+ siteCode +"]]></site_code>");
				xmlBuff.append("<asset_code><![CDATA["+ assetCode +"]]></asset_code>");
				xmlBuff.append("<asset_code__par><![CDATA["+ assetCode +"]]></asset_code__par>");
				xmlBuff.append("<aloc_code><![CDATA["+ alocCode +"]]></aloc_code>");
				xmlBuff.append("<asset_type><![CDATA["+ "D" +"]]></asset_type>");
				xmlBuff.append("<descr><![CDATA["+ remarks +"]]></descr>");
				xmlBuff.append("<inst_date><![CDATA["+ sdf.format(tranDate) +"]]></inst_date>");
				xmlBuff.append("<use_date><![CDATA["+ sdf.format(tranDate) +"]]></use_date>");
				xmlBuff.append("<remarks><![CDATA["+ remarks +"]]></remarks>");
				xmlBuff.append("<emp_code><![CDATA["+ loginEmpCode +"]]></emp_code>");
				xmlBuff.append("<item_code><![CDATA["+ itemCode +"]]></item_code>");
				xmlBuff.append("<grp_code><![CDATA["+ grpCode +"]]></grp_code>");
				xmlBuff.append("<curr_code><![CDATA["+ currCode +"]]></curr_code>");
				xmlBuff.append("<exch_rate><![CDATA["+ exchRate +"]]></exch_rate>");
				xmlBuff.append("<curr_code__inst><![CDATA["+ currCode +"]]></curr_code__inst>");
				xmlBuff.append("<chg_user><![CDATA["+ this.userId +"]]></chg_user>");
				xmlBuff.append("<chg_date><![CDATA["+ sdf.format(new java.util.Date()).toString() +"]]></chg_date>");
				xmlBuff.append("<chg_term><![CDATA["+ this.termId +"]]></chg_term>");
				xmlBuff.append("<asset_sale_type><![CDATA["+ "N" +"]]></asset_sale_type>");
				xmlBuff.append("<confirmed><![CDATA["+ "N" +"]]></confirmed>");
				xmlBuff.append("<tran_type><![CDATA["+ tranType +"]]></tran_type>");
				xmlBuff.append("<proj_code><![CDATA["+ projCode +"]]></proj_code>");
				xmlBuff.append("<tran_id__rcp><![CDATA["+ tranId +"]]></tran_id__rcp>");
				xmlBuff.append("<cctr_code><![CDATA["+ cctrCodeDr +"]]></cctr_code>");
				xmlBuff.append("<exch_rate__inst><![CDATA["+ exchRate +"]]></exch_rate__inst>");
				xmlBuff.append("<install_chgs><![CDATA["+ taxInstallChgs +"]]></install_chgs>");
				xmlBuff.append("<octroi_chgs><![CDATA["+ octroiChgs +"]]></octroi_chgs>");
				xmlBuff.append("<comm_chgs><![CDATA["+ commChgs +"]]></comm_chgs>");
				instChgsBc= taxInstallChgs * exchRate;
				xmlBuff.append("<inst_chgs__bc><![CDATA["+ instChgsBc +"]]></inst_chgs__bc>");
				xmlBuff.append("<acct_code__inst><![CDATA["+ acctCode +"]]></acct_code__inst>");
				xmlBuff.append("<cctr_code__inst><![CDATA["+ cctrCode +"]]></cctr_code__inst>");
				xmlBuff.append("<excreco_amt><![CDATA["+ excrecoAmt +"]]></excreco_amt>");
				xmlBuff.append("<original_value><![CDATA["+ originalValue +"]]></original_value>");
				originalValueBc=originalValue * exchRate;
				xmlBuff.append("<original_value__bc><![CDATA["+ originalValueBc +"]]></original_value__bc>");
				xmlBuff.append("<asset_class><![CDATA["+ assetclass +"]]></asset_class>");
				xmlBuff.append("<useful_life><![CDATA["+ usefullife +"]]></useful_life>");
				xmlBuff.append("</Detail1>");
				xmlBuff.append("<Detail2 dbID=''  domID=\"1\" objName=\"asset_install\" objContext=\"2\">"); 
				xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" updateFlag=\"A\" status=\"N\" />");
				xmlBuff.append("<tran_id/>");
				xmlBuff.append("<line_no><![CDATA["+ "1" +"]]></line_no>");
				xmlBuff.append("<tran_id__rcp><![CDATA["+ tranId +"]]></tran_id__rcp>");
				xmlBuff.append("<line_no__rcp><![CDATA["+ lineNo +"]]></line_no__rcp>");
				xmlBuff.append("<item_code><![CDATA["+ itemCode +"]]></item_code>");
				xmlBuff.append("<loc_code><![CDATA["+ locCode +"]]></loc_code>");
				xmlBuff.append("<lot_no><![CDATA["+ lotNo +"]]></lot_no>");
				xmlBuff.append("<lot_sl><![CDATA["+ lotSl +"]]></lot_sl>");
				xmlBuff.append("<quantity><![CDATA["+ quantity +"]]></quantity>");
				xmlBuff.append("<rate><![CDATA["+ rate +"]]></rate>");
				xmlBuff.append("<supp_code><![CDATA["+ suppCode +"]]></supp_code>");
				xmlBuff.append("<supp_name><![CDATA["+ suppName +"]]></supp_name>");
				xmlBuff.append("<rcp_date><![CDATA["+ sdf.format(tranDate) +"]]></rcp_date>");
				xmlBuff.append("<bill_no><![CDATA["+ billNo +"]]></bill_no>");
				if( billDate != null )
				{

					xmlBuff.append("<bill_date><![CDATA["+ sdf.format(billDate) +"]]></bill_date>");
				}
				xmlBuff.append("<curr_code><![CDATA["+ currCode +"]]></curr_code>");
				xmlBuff.append("<exch_rate><![CDATA["+ exchRate +"]]></exch_rate>");
				xmlBuff.append("<grp_code><![CDATA["+ grpCode +"]]></grp_code>");
				xmlBuff.append("<asset_code><![CDATA["+ assetCode +"]]></asset_code>");
				xmlBuff.append("<tax_amt><![CDATA["+ taxAmount +"]]></tax_amt>");
				taxRecoAmtexc= taxRecoAmt - excrecoAmt;
				xmlBuff.append("<tax_reco_amt><![CDATA["+ taxRecoAmtexc +"]]></tax_reco_amt>");
				xmlBuff.append("<excise_amt><![CDATA["+ exciseAmt +"]]></excise_amt>");
				xmlBuff.append("<excreco_amt><![CDATA["+ excrecoAmt +"]]></excreco_amt>");
				xmlBuff.append("<original_value><![CDATA["+ originalValue +"]]></original_value>");
				xmlBuff.append("</Detail2>");	
				xmlBuff.append("</Header0>");
				xmlBuff.append("</group0>");
				xmlBuff.append("</DocumentRoot>");


				if( xmlBuff != null  && (xmlBuff.toString()).length() > 0 )
				{

					assetInstallList.add(xmlBuff.toString());
					xmlBuff = null;	

				}

			}
			returnString= assetInstall.assetInstallData(tranId,conn,xtraParams,assetInstallList);
			if(returnString!=null)
			{
				return returnString;
			}
			System.out.println("returnString : assetInstallData:" + returnString);
			// added by Nandkumar Gadkari on 08/03/18  --end--

			if (count == 0)
			{
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;
				retString = itmDbAccess.getErrorString("", "VTOPENCUR", "","",conn);
				return retString;
			}

			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;
			// commented by Nandkumar Gadkari on 08/03/18  --start--
			/*pstmtHdr.executeBatch();
			pstmtDet.executeBatch();
			pstmtHdr.clearBatch();
			pstmtDet.clearBatch();*/
			// commented by Nandkumar Gadkari on 08/03/18  --end--

		} catch (Exception e)
		{
			System.out.println("PoRcpConf : createAssetInstall(String tranId, String orderNo,String logEmpCode, Connection conn) :" + e.getMessage());
			try
			{
				// conn.rollback();
			} catch (Exception e1)
			{
				//Added by Anjali R.  on[4/10/2018][Start]
				throw new ITMException(e1);
				//Added by Anjali R.  on[4/10/2018][End]
			}
			e.printStackTrace();
			throw new ITMException(e);
		} finally
		{
			try
			{
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}

				if (pstmt1 != null)
				{
					pstmt1.close();
					pstmt1 = null;
				}

				if (pstmtDet != null)
				{
					pstmtDet.close();
					pstmtDet = null;
				}

				if (pstmtHdr != null)
				{
					pstmtHdr.close();
					pstmtHdr = null;
				}
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (rs1 != null)
				{
					rs1.close();
					rs1 = null;
				}
			} catch (Exception e)
			{
				System.out.println("PoRcpConf : createAssetInstall(String tranId, String orderNo,String logEmpCode, Connection conn) :" + e.getMessage());
				e.printStackTrace();//Modified by Anjali R. on[25/10/2018]
				throw new ITMException(e);
			}
		}
		System.out.println("Returning Result in createAssetInstall::" + retString);
		return retString;
	}

	private String checkNull(String input)
	{
		if (input == null)
		{
			input = "";
		}
		/*
		 * else { input = input.trim(); }
		 */
		return input;
	}

	private String checkNullFrLotSl(String input)
	{
		if (input == null)
		{
			input = "";
		}
		return input;
	}

	private String generateTranId(String windowName, String siteCode, String tranDateStr, Connection conn) throws ITMException
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String selSql = "";
		String tranId = "";
		String tranSer = "";
		String keyString = "";
		String keyCol = "";
		String xmlValues = "";
		String paySiteCode = "";
		String effectiveDate = "";
		java.sql.Date effDate = null;
		try
		{
			System.out.println("generateTranId() called");
			selSql = "SELECT KEY_STRING, TRAN_ID_COL, REF_SER FROM TRANSETUP WHERE TRAN_WINDOW = ? ";
			// System.out.println("selSql :"+selSql);
			pstmt = conn.prepareStatement(selSql);
			pstmt.setString(1, windowName);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				keyString = rs.getString("KEY_STRING");
				keyCol = rs.getString("TRAN_ID_COL");
				tranSer = rs.getString("REF_SER");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("keyString :" + keyString);
			System.out.println("keyCol :" + keyCol);
			System.out.println("tranSer :" + tranSer);
			xmlValues = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
			xmlValues = xmlValues + "<Header></Header>";
			xmlValues = xmlValues + "<Detail1>";
			xmlValues = xmlValues + "<tran_id></tran_id>";
			xmlValues = xmlValues + "<site_code>" + siteCode + "</site_code>";
			xmlValues = xmlValues + "<tran_date>" + tranDateStr + "</tran_date>";
			xmlValues = xmlValues + "</Detail1></Root>";
			System.out.println("xmlValues :[" + xmlValues + "]");
			TransIDGenerator generatedTranid = new TransIDGenerator(xmlValues, "BASE", CommonConstants.DB_NAME);
			tranId = generatedTranid.generateTranSeqID(tranSer, keyCol, keyString, conn);

			System.out.println("tranId :" + tranId);
		} catch (SQLException ex)
		{
			System.out.println("Exception ::" + selSql + ex.getMessage() + ":");
			ex.printStackTrace();
			throw new ITMException(ex);
		} catch (Exception e)
		{
			System.out.println("Exception ::" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		} finally
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
			} catch (Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return tranId;
	}

	private String confirmWebService(String businessObj, String tranIdFr, String xtraParams, String forcedFlag, Connection conn) throws ITMException
	{
		String methodName = "";
		String compName = "";
		String retString = "";
		String serviceCode = "";
		String serviceURI = "";
		String actionURI = "";
		String sql = "";
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		System.out.println("confirmReceipt(String businessObj, String tranIdFr,String xtraParams, String forcedFlag, Connection conn) called >>><!@#>");
		try
		{
			methodName = "gbf_post";
			actionURI = "http://NvoServiceurl.org/" + methodName;

			sql = "SELECT SERVICE_CODE,COMP_NAME FROM SYSTEM_EVENTS WHERE OBJ_NAME = ? AND EVENT_CODE = 'pre_confirm' ";
			pStmt = conn.prepareStatement(sql);
			pStmt.setString(1, businessObj);
			rs = pStmt.executeQuery();
			if (rs.next())
			{
				serviceCode = rs.getString("SERVICE_CODE");
				compName = rs.getString("COMP_NAME");
			}
			System.out.println("serviceCode = " + serviceCode + " compName " + compName);
			// Changed by Manish on 01/04/16 for max cursor issue [start]
			if (pStmt != null)
			{
				pStmt.close();
				pStmt=null;
			}
			if (rs !=null)
			{
				rs.close();
				rs=null;
			}
			// Changed by Manish on 01/04/16 for max cursor issue [end]
			sql = "SELECT SERVICE_URI FROM SYSTEM_EVENT_SERVICES WHERE SERVICE_CODE = ? ";
			pStmt = conn.prepareStatement(sql);
			pStmt.setString(1, serviceCode);
			rs = pStmt.executeQuery();
			if (rs.next())
			{
				serviceURI = rs.getString("SERVICE_URI");
			}
			System.out.println("serviceURI = " + serviceURI + " compName = " + compName);
			// Changed by Manish on 01/04/16 for max cursor issue [start]
			if (pStmt != null)
			{
				pStmt.close();
				pStmt=null;
			}
			if (rs !=null)
			{
				rs.close();
				rs=null;
			}
			// Changed by Manish on 01/04/16 for max cursor issue [end]
			Service service = new Service();
			Call call = (Call) service.createCall();
			call.setTargetEndpointAddress(new java.net.URL(serviceURI));
			call.setOperationName(new javax.xml.namespace.QName("http://NvoServiceurl.org", methodName));
			call.setUseSOAPAction(true);
			call.setSOAPActionURI(actionURI);
			Object[] aobj = new Object[4];

			call.addParameter(new javax.xml.namespace.QName("http://NvoServiceurl.org", "component_name"), XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter(new javax.xml.namespace.QName("http://NvoServiceurl.org", "tran_id"), XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter(new javax.xml.namespace.QName("http://NvoServiceurl.org", "xtra_params"), XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter(new javax.xml.namespace.QName("http://NvoServiceurl.org", "forced_flag"), XMLType.XSD_STRING, ParameterMode.IN);

			aobj[0] = new String(compName);
			aobj[1] = new String(tranIdFr);
			aobj[2] = new String(xtraParams);
			aobj[3] = new String(forcedFlag);
			// System.out.println("@@@@@@@@@@loginEmpCode:"
			// +genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode")+":");

			call.setReturnType(XMLType.XSD_STRING);

			retString = (String) call.invoke(aobj);

			System.out.println("Confirm Complete @@@@@@@@@@@Return string from NVO is:==>[" + retString + "]");

		} catch (Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		} finally
		{
			try
			{
				if (pStmt != null)
				{
					pStmt.close();
					pStmt = null;
				}
				if (rs !=null)
				{
					rs.close();
					rs=null;
				}
				/*
				 * if( conn != null ){ conn.close(); conn = null; }
				 */
			} catch (Exception e)
			{
				System.out.println("Exception inCalling confirmed");
				e.printStackTrace();
				try
				{
					conn.rollback();

				} catch (Exception s)
				{
					System.out.println("Unable to rollback");
					s.printStackTrace();
					//Added by Anjali R.  on[4/10/2018][Start]
					throw new ITMException(s);
					//Added by Anjali R.  on[4/10/2018][End]
				}
				//Added by Anjali R.  on[4/10/2018][Start]
				throw new ITMException(e);
				//Added by Anjali R.  on[4/10/2018][End]
			}
		}
		return retString;
	}

	public double getUnroundDecimal(double actVal, int prec)
	{

		String fmtStr = "############0";
		String strValue = null;
		double retVal = 0;
		if (prec > 0)
		{
			fmtStr = fmtStr + "." + "000000000".substring(0, prec + 1);
		}
		System.out.println("fmtStr value [" + fmtStr + "]");
		DecimalFormat decFormat = new DecimalFormat(fmtStr);
		strValue = decFormat.format(actVal);
		System.out.println(" actVal [" + actVal + "] integer [" + strValue.substring(0, strValue.indexOf(".") + 1) + "]");
		System.out.println("decimal [" + strValue.substring(strValue.indexOf(".") + 1, strValue.indexOf(".") + prec + 1) + "]");
		retVal = Double.parseDouble(strValue.substring(0, strValue.indexOf(".") + 1) + strValue.substring(strValue.indexOf(".") + 1, strValue.indexOf(".") + prec + 1));
		System.out.println("rounded value [" + retVal + "]");

		return retVal;
	}

	int InsertQcOrderLots(String qcNo, String tranId, String itemCode, String unit, String lotNoRcp, String qcLotLocCode, String batchNo, String spec, Timestamp today, Timestamp mfgDate, Timestamp expiryDate, Connection conn) throws ITMException
	{
		String sql = "", lotSl = "", locCode = "";
		PreparedStatement pstmt = null, pstmtInsert = null;
		ResultSet rs = null;
		double qty = 0, noArt = 0, netWeight = 0;
		int lineNo = 1;
		int updCnt1 = 0;
		try
		{
			System.out.println("qcNo is= " + qcNo + "and  tranId is =" + tranId +"itemCode is= " + itemCode + "and  unit is =" + unit +"lotNoRcp is= " + lotNoRcp + "and  qcLotLocCode is =" + qcLotLocCode  + "and  batchNo is =" + batchNo + "and  spec is =" + spec + "and  today is =" + today + "and  mfgDate is =" + mfgDate + "and  expiryDate is =" + expiryDate );
			System.out.println("mfgDate========" + mfgDate);
			sql = "select * from porcpdet where tran_id=? and item_code=? and lot_no=? and (CASE WHEN BATCH_NO IS NULL THEN ' ' ELSE BATCH_NO END) = ? AND		   (CASE WHEN SPEC_REF IS NULL THEN ' ' ELSE SPEC_REF END)	= ?  AND (CASE WHEN	 MFG_DATE IS NULL THEN ? ELSE MFG_DATE END) 	= ? AND (CASE WHEN	 EXPIRY_DATE IS NULL THEN ? ELSE EXPIRY_DATE END) 	= ?  ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			pstmt.setString(2, itemCode);
			pstmt.setString(3, lotNoRcp);
			pstmt.setString(4, batchNo);
			pstmt.setString(5, spec);
			pstmt.setTimestamp(6, today);
			pstmt.setTimestamp(7, mfgDate);
			pstmt.setTimestamp(8, today);
			pstmt.setTimestamp(9, expiryDate);
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				System.out.println("Enter in while==============");
				lotSl = rs.getString("LOT_SL");
				qty = rs.getDouble("QUANTITY");
				noArt = rs.getDouble("NO_ART");
				netWeight = rs.getDouble("NET_WEIGHT");
				locCode = rs.getString("LOC_CODE");

				sql = " Insert into qc_order_lots (QC_ORDER,LINE_NO,ITEM_CODE,LOT_NO,LOT_SL,LOC_CODE,QUANTITY," + "UNIT,LOCTYPE,LOC_CODE__ISSUE,QORDER_NO,NO_ART,NET_WEIGHT) " + " values (?,?,?,?,?,?,?,?,?,?,?,?,?)";
				pstmtInsert = conn.prepareStatement(sql);
				pstmtInsert.setString(1, qcNo);
				pstmtInsert.setInt(2, lineNo);
				pstmtInsert.setString(3, itemCode);
				pstmtInsert.setString(4, lotNoRcp);
				pstmtInsert.setString(5, lotSl);
				// pstmtInsert.setString(6, locCode);
				pstmtInsert.setString(6, qcLotLocCode);
				qty = getUnroundDecimal(qty, 3);
				pstmtInsert.setDouble(7, qty);
				pstmtInsert.setString(8, unit);
				// pstmtInsert.setDouble(9, qtySample);
				pstmtInsert.setString(9, "A");
				pstmtInsert.setString(10, locCode);
				pstmtInsert.setString(11, qcNo);
				pstmtInsert.setDouble(12, noArt);
				pstmtInsert.setDouble(13, netWeight);
				updCnt1 = pstmtInsert.executeUpdate();
				pstmtInsert.close();
				pstmtInsert = null;
				lineNo++;

			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		} catch (Exception e)
		{
			e.printStackTrace();
			//Added by Anjali R.  on[4/10/2018][Start]
			throw new ITMException(e);
			//Added by Anjali R.  on[4/10/2018][End]
		} finally
		{
			try
			{
				if (pstmt != null)
				{

					pstmt.close();
					pstmt = null;
				}
				if (pstmtInsert != null)
				{
					pstmtInsert.close();
					pstmtInsert = null;
				}
			} catch (SQLException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				//Added by Anjali R.  on[4/10/2018][Start]
				throw new ITMException(e);
				//Added by Anjali R.  on[4/10/2018][End]
			}
		}
		return updCnt1;

	}
	private static String checkNullAndTrim(String input)
	{
		if (input==null)
		{
			input="";
		}
		return input.trim();
	}

	//Changed by Jagruti Shinde Request id:[W16CSUN009][Start]
	private String updateLotNo(String lotNoRcp,String tranId, String itemCode, String locCode,String lineNo, Connection conn) throws Exception
	{	
		System.out.println("lotNoRcp::"+lotNoRcp +"tranId::"+tranId +"itemCode::" +itemCode +"locCode::"+locCode + "lineNo::"+lineNo);
		PreparedStatement pstmtUpd = null;
		String sql = "";
		String retString = "";
		int updCnt = 0;
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		System.out.println("Enter in UpdateLotNo");

		try
		{
			sql = "UPDATE PORCPDET SET 	LOT_NO = ? WHERE TRAN_ID = ? AND ITEM_CODE = ? AND   LOC_CODE  = ? AND LINE_NO = ? ";
			pstmtUpd = conn.prepareStatement(sql);
			pstmtUpd.setString(1, lotNoRcp);
			pstmtUpd.setString(2, tranId);
			pstmtUpd.setString(3, itemCode);
			pstmtUpd.setString(4, locCode);
			pstmtUpd.setString(5, lineNo);

			updCnt = pstmtUpd.executeUpdate();

			if (updCnt == 0)
			{
				retString = itmDBAccessLocal.getErrorString("", "DS000NR", "","",conn);

			}
		}
		catch(Exception e)
		{
			System.out.println("Exception in updateLotNo ::::::: " + e);
			e.printStackTrace();
			throw new Exception(e);
		}
		finally
		{
			if(pstmtUpd!=null)
			{
				pstmtUpd.close();
				pstmtUpd = null;
			}
		}
		return retString;

	}
    //Changed by Jagruti Shinde Request id:[W16CSUN009][End]
    public double doublevalue(String str) {
		if (str == null || str.trim().length() == 0) { return 0.0D; }
		else
		{
			return Double.parseDouble(str);
		}
	}
}