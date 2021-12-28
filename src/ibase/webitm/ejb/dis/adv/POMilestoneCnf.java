package ibase.webitm.ejb.dis.adv;
import ibase.system.config.AppConnectParm;
import ibase.system.config.ConnDriver;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.MasterStatefulLocal;
import ibase.webitm.ejb.dis.POrderMilestone;
import ibase.webitm.ejb.dis.adv.CreatePoVoucherAdvance;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.sys.UtilMethods;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.utility.TransIDGenerator;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.apache.axis.client.Service;
import org.apache.axis.client.Call;
import org.apache.axis.encoding.XMLType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.naming.InitialContext;
import javax.xml.rpc.ParameterMode;
//------


public class POMilestoneCnf 
{
	// Following variable defined by Sagar on 26/11/15 Start.
	int minDaysDlvTerm=0,maxDaysDlvTerm=0;
	double minAmtDlvTerm=0.0,maxAmtDlvTerm=0.0,finChrgeDlvTerm=0.0;
	String fchgTypeDlvTerm="";
	// Following variable defined by Sagar on 26/11/15 End.

	public String confMilestone( String tranId, String xmlDataAll, String editFlag ) throws Exception	
	{
		String returnStr="";
		System.out.println("@@@@@@@@@ POMilestoneCnf:: confMilestone() called ...tranId["+tranId+"]");
		Connection conn = null;
		try
		{
			ConnDriver connDriver = new ConnDriver();

			if(conn==null)
			{
				conn = connDriver.getConnectDB("DriverITM");
				//conn = getConnection();
				conn.setAutoCommit(false);
			}

			System.out.println("@@@@@@@@@ workflowForPenalty() called ...tranId["+tranId+"]");
			ResultSet rs = null;
			PreparedStatement pstmt = null;
			String sql="",purcOrder="",lineNoOrd="",tranType="",siteCode="",suppCode="",acctCode="",cctrCode="",taxClass="",taxChap="";
			String taxEnv="",taskCode="",taskStatus="",paymentOpt="",remarks="",tranIdVch="",empCodeAprv="",wfStatus="",allowOverride="";
			double amount=0,amountOrig=0;
			Timestamp tranDate=null,confDate=null,complDate=null,statusDate=null,dueDateTs=null;
			Date dueDate=null;

			Date currentDate = new Date();
			E12GenericUtility genericUtility = new E12GenericUtility();
			Calendar currentDate1 = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String sysDate = sdf.format(currentDate1.getTime());
			System.out.println("Now the date is :=>  " + sysDate);


			System.out.println("@@@@@@@@@ xmlDataAll["+xmlDataAll+"]");

			sql = " select tran_type from PUR_MILSTN where tran_id = ?  " ;
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId );
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				tranType=checkNull(rs.getString("tran_type"));
			}	
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			System.out.println("@@@@@@@@@tranId["+tranId+"]tranType["+tranType+"]");

			//vishakha method called 


			if(!"D".equalsIgnoreCase(tranType))
			{ 
				POrderMilestone pOrderMilestone = new POrderMilestone();
				returnStr = pOrderMilestone.recalcDueDate(tranId, xmlDataAll, conn);
				System.out.println("@@@@@@@@@ pOrderMilestone.recalcDueDate() returnStr["+returnStr+"]");
			}

			if(returnStr == null || returnStr.trim().length() == 0 || "SUCCESS".equalsIgnoreCase(returnStr))
			{
				returnStr = workflowForPenalty(tranId, xmlDataAll,  conn);
				System.out.println("@@@@@@ returnStr["+returnStr+"]");
				if(returnStr.indexOf("Success") > -1  ||  returnStr.indexOf("VTSUCC1") > -1  || returnStr.indexOf("SucAmtZero") > -1  )
				{
					System.out.println("@@@@@@@@@ workflowForPenalty return successfully.....");
					returnStr =  "Y"; 
				}
				else
				{
					System.out.println("@@@@@@@@@ workflowForPenalty failed.....");
					//	returnStr =  "N";
				}
			}	

			System.out.println("@@@@@@ returnStr["+returnStr+"]");
			return returnStr;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			if( conn != null)
			{
				conn.close();
				conn = null;
			}


		}
	}


	public String workflowForPenalty(String tranId,String xmlDataAll, Connection conn) throws Exception
	{
		String returnStr="";
		System.out.println("@@@@@@@@@ workflowForPenalty() called ...tranId["+tranId+"]");
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		String sql="",purcOrder="",lineNoOrd="",tranType="",siteCode="",suppCode="",acctCode="",cctrCode="",taxClass="",taxChap="";
		String taxEnv="",taskCode="",taskStatus="",paymentOpt="",remarks="",tranIdVch="",empCodeAprv="",wfStatus="",allowOverride="";
		String daySlab="",chargesSlab="",finChrgTypeDescr="",taskCodeDescr="";
		double amount=0,amountOrig=0;
		double ordAmtPord=0.0, taxAmtPord=0.0, totAmtPord=0.0;
		Timestamp tranDate=null,confDate=null,complDate=null,statusDate=null,dueDateTs=null;
		Date dueDate=null;

		StringBuffer xmlBuff = null;
		Date currentDate = new Date();

		E12GenericUtility genericUtility = new E12GenericUtility();
		Calendar currentDate1 = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
		String sysDate = sdf.format(currentDate1.getTime());
		System.out.println("Now the date is :=>  " + sysDate);
		//	Timestamp newsysDate = java.sql.Timestamp.valueOf( sdf.format(currentDate)+" 00:00:00.0");
		Timestamp newsysDate = getCurrdateAppFormat();
		System.out.println("Now the date is :=>  ["+newsysDate+"]");

		// wfStatusDate code Added by Sagar on 23/11/15
		java.util.Date date1= new java.util.Date();
		Timestamp wfStatusDate = new Timestamp(date1.getTime());
		System.out.println(">>wfStatusDate["+wfStatusDate+"]");

		java.sql.Timestamp today = null;
		java.util.Date date = null;
		today = new java.sql.Timestamp(System.currentTimeMillis());
		java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat(genericUtility.getDBDateFormat());
		date = sdf1.parse(today.toString());
		today =	java.sql.Timestamp.valueOf(sdf1.format(date).toString() + " 00:00:00.0");
		System.out.println(" date   today:=>  ["+today+"]");

		String xtraParams="",tranIdForVoucher="",relAgnst="",returnStr1="" ;

		String chgTerm   = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"CHG_TERM");
		String  chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"CHG_USER");
		String suppName="",taskDescr="";
		String mileStoneVouchAutoCnf="",mileStoneDebitAutoCnf="";

		sql = " select tran_id,tran_date,purc_order,line_no__ord,tran_type,site_code,supp_code,acct_code,cctr_code," +
		" tax_class,tax_chap,tax_env, task_code, amount,due_date,task_status,payment_opt,remarks,tran_id__vch," +
		" conf_date,compl_date,emp_code__aprv,wf_status,status_date,compl_date,allow_override,amount_orig " +
		" from PUR_MILSTN where tran_id = ?  " ;
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, tranId );
		rs = pstmt.executeQuery();
		if (rs.next())
		{
			tranDate=rs.getTimestamp("tran_date");
			purcOrder=checkNull(rs.getString("purc_order"));
			lineNoOrd=checkNull(rs.getString("line_no__ord"));
			tranType=checkNull(rs.getString("tran_type"));
			siteCode=checkNull(rs.getString("site_code"));
			suppCode=checkNull(rs.getString("supp_code"));
			acctCode=checkNull(rs.getString("acct_code"));
			cctrCode= rs.getString("cctr_code");
			taxClass=checkNull(rs.getString("tax_class"));
			taxChap=checkNull(rs.getString("tax_chap"));
			taxEnv=checkNull(rs.getString("tax_env"));
			taskCode=checkNull(rs.getString("task_code"));
			amount=rs.getDouble("amount");
			dueDateTs=rs.getTimestamp("due_date");
			dueDate=rs.getDate("due_date");
			taskStatus=checkNull(rs.getString("task_status"));
			paymentOpt=checkNull(rs.getString("payment_opt"));
			//remarks=checkNull(rs.getString("remarks"));
			tranIdVch=checkNull(rs.getString("tran_id__vch"));
			confDate=rs.getTimestamp("conf_date");
			complDate=rs.getTimestamp("compl_date");
			empCodeAprv=checkNull(rs.getString("emp_code__aprv"));
			wfStatus=checkNull(rs.getString("wf_status"));
			statusDate=rs.getTimestamp("status_date");
			allowOverride=checkNull(rs.getString("allow_override"));
			amountOrig=rs.getDouble("amount_orig");

		}	
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;

		int timeDiff =0;

		// 	sql = "  select delay_days from pur_milstn_resch where tran_id = ? ";
		/*sql =  " select pmr.delay_days from pur_milstn_resch pmr, pur_milstn pm " +
				" where pmr.tran_id = ? and  pmr.tran_id = pm.tran_id " +
				" and pm.wf_status <> 'C'  ";
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, tranId );
		rs = pstmt.executeQuery();
		if (rs.next())
		{
			timeDiff=rs.getLong("delay_days");
		}	
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;
		 */
		// xml iterate
		Document xmlDataAllDom= null;
		if(xmlDataAll != null && xmlDataAll.trim().length()!=0)
		{
			xmlDataAllDom = genericUtility.parseString(xmlDataAll); 
			System.out.println("xmlDataAll" + xmlDataAll);
		}

		timeDiff = calculateDelayDays(xmlDataAll);


		amount = 0;  // testing
		paymentOpt = "";
		System.out.println("@@@@ before paymentOpt["+paymentOpt+"]amount["+amount+"]");
		amount = Double.parseDouble(genericUtility.getColumnValue("amount",xmlDataAllDom)==null?"":genericUtility.getColumnValue("amount",xmlDataAllDom));
		paymentOpt = genericUtility.getColumnValue("payment_opt",xmlDataAllDom);
		System.out.println("@@@@ paymentOpt["+paymentOpt+"]amount["+amount+"]");
		String amountStr = ""+amount;
		String complDateStr = checkNull(genericUtility.getColumnValue("compl_date",xmlDataAllDom));

		// xml end	

		/* 
		if( complDate != null && dueDate!= null && complDate.after(dueDate))
		{
			UtilMethods utilMethod = new UtilMethods();
			timeDiff = utilMethod.DaysAfter(dueDateTs,complDate);
			//timeDiff = Double.parseDouble(""+timeDiff(dueDate,complDate));
			System.out.println("complDate["+complDate+"]dueDate["+dueDate+"]timeDiff["+timeDiff+"]");
		}
		 */

		System.out.println("tranId["+tranId+"]tranType["+tranType+"]timeDiff["+timeDiff+"]paymentOpt["+paymentOpt+"]taskCode["+taskCode+"]complDateStr["+complDateStr+"]amountStr["+amountStr+"]");


		if( "P".equalsIgnoreCase(tranType))
		{    // voucher created for tran type 'P'
			System.out.println("@@@@@@@@ voucher created for tran type 'P' ");
			sql= " select rel_agnst from PORD_PAY_TERM where purc_order=? and line_no=? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, purcOrder);
			pstmt.setString(2, lineNoOrd);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				relAgnst = checkNull(rs.getString("rel_agnst"));
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			if("P".equalsIgnoreCase(paymentOpt))
			{	

				String returnStr2 =  voucherValidate ( purcOrder, amount, conn);
				System.out.println("voucherValidate returnStr2["+returnStr2+"]");

				if(!(returnStr2.indexOf("Success") > -1))
				{
					//conn.rollback();
					System.out.println("voucherValidate() failed...rollback()...");
					return returnStr2;
				}

				CreatePoVoucherAdvance createPoVouchAdv = new CreatePoVoucherAdvance();
				returnStr =  createPoVouchAdv.createPoVoucher(purcOrder,xtraParams,conn,"ML", amount,today ,taskCode,relAgnst,lineNoOrd);
				System.out.println("CreatePoVoucherAdvance returnStr["+returnStr+"]");

				if(returnStr.indexOf("Success") > -1 )
				{
					System.out.println("CreatePoVoucherAdvance created Successfully["+returnStr+"]");

					// Code added by Sagar on 20/11/15, Start..

					String[] arrayForTranId = returnStr.split("<TranID>");
					int endIndex = arrayForTranId[1].indexOf("</TranID>");
					tranIdForVoucher = arrayForTranId[1].substring(0,endIndex);
					tranIdForVoucher=tranIdForVoucher==null ? "" : tranIdForVoucher.trim();
					System.out.println(">>tranIdForVoucher["+tranIdForVoucher+"]");

					/*java.util.Date date1= new java.util.Date();
				    Timestamp ts_now = new Timestamp(date1.getTime());
				    System.out.println(">>ts_now["+ts_now+"]");*/

					sql = " update pur_milstn set wf_status = 'C' ,conf_date = ? ,status_date = ? , confirmed = 'Y', tran_id__vch= ?  where tran_id = ? ";
					System.out.println("sql :"+sql );
					pstmt = conn.prepareStatement(sql);
					pstmt.setTimestamp(1, newsysDate);
					pstmt.setTimestamp(2, wfStatusDate); 
					pstmt.setString(3, tranIdForVoucher);
					pstmt.setString(4, tranId);
					int cntVouch = pstmt.executeUpdate();
					pstmt.close();
					pstmt = null;

					System.out.println(">>>cntVouch:"+cntVouch);
					// Code added by Sagar on 20/11/15, End..

					conn.commit();
					/*String[] arrayForTranId = returnStr.split("<TranID>");
					int endIndex = arrayForTranId[1].indexOf("</TranID>");
					tranIdForVoucher = arrayForTranId[1].substring(0,endIndex);
					tranIdForVoucher=tranIdForVoucher==null ? "" : tranIdForVoucher.trim();
					System.out.println("tranIdForVoucher["+tranIdForVoucher+"]");	
					 */

					//returnStr = confirmTranscation("voucher_adv",tranIdForVoucher,xtraParams,conn); // Comment added by Sagar on 20/11/15

					//Code added by Sagar on 20/11/15 Start..
					FinCommon fincommon = new FinCommon();
					mileStoneVouchAutoCnf= fincommon.getFinparams("999999", "MLSTNVOUCH_AUTOCNF", conn);
					System.out.println(">>>mileStoneVouchAutoCnf:"+mileStoneVouchAutoCnf);
					if(mileStoneVouchAutoCnf!= null && mileStoneVouchAutoCnf.trim().length() > 0)
					{
						mileStoneVouchAutoCnf= mileStoneVouchAutoCnf.trim();
						if("NULLFOUND".equals(mileStoneVouchAutoCnf))
						{
							mileStoneVouchAutoCnf ="N";
						}
					}
					else
					{
						mileStoneVouchAutoCnf ="N";
					}

					if("Y".equalsIgnoreCase(mileStoneVouchAutoCnf)) 
					{
						returnStr = confirmTranscation("voucher_adv",tranIdForVoucher,xtraParams,conn);
					}
					else
					{
						returnStr = "Success";
					}
					//Code added by Sagar on 20/11/15 End.
					System.out.println("CreatePoVoucherAdvance confirmTranscation returnStr["+returnStr+"]");

					if(returnStr.indexOf("Success") > -1 || returnStr.indexOf("VTSUCC1") > -1   )
					{
						// Comment added by Sagar on 20/11/15, Start.
						/*sql = " update pur_milstn set  tran_id__vch  = ?  where tran_id = ? ";
						System.out.println("sql :"+sql );
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,tranIdForVoucher);
						pstmt.setString(2,tranId);
						int cnt = pstmt.executeUpdate();
						pstmt.close();
						pstmt = null;
						System.out.println("@@@@@ updated sucessfully pur_milstn tranId["+tranId+"]tranIdForVoucher["+tranIdForVoucher+"]cnt["+cnt+"]");*/
						// Comment added by Sagar on 20/11/15, End.

						//	conn.commit();
					}
					else
					{
						conn.rollback();
						System.out.println("CreatePoVoucherAdvance confirmTranscation failed ...rollback()...");
						return returnStr;
					}

				}
				else
				{
					conn.rollback();
					System.out.println("Voucher not created ...rollback()...");
					return returnStr;
				}

			}
			else
			{
				System.out.println("paymentOpt is 'N' .....");
				//conn.rollback();
				//return returnStr;

				//Code added by Sagar on 23/11/15 Start.

				sql = " update pur_milstn set wf_status = 'C' ,conf_date = ? ,status_date = ? , confirmed = 'Y' where tran_id = ? ";
				System.out.println("sql :"+sql );
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, newsysDate);
				pstmt.setTimestamp(2, wfStatusDate); 
				pstmt.setString(3, tranId);
				int cntPurMlstn = pstmt.executeUpdate();
				pstmt.close();
				pstmt = null;
				System.out.println(">>>If paymentOpt is 'N' cntPurMlstn:"+cntPurMlstn);
				conn.commit();

				//Code added by Sagar on 23/11/15 End.
				returnStr = "Success";
			}


		}
		if( "P".equalsIgnoreCase(tranType) && timeDiff > 0 )
		{  // insert into purc_milstn with tran type as 'D'
			System.out.println("@@@@@@@@ insert into purc_milstn with tran type as 'D' ");

			double penalatyAmount = penalatyAmount(xmlDataAllDom,purcOrder,amountStr,timeDiff,taskCode,conn );

			sql = "  select supp_name from supplier where supp_code = ? ";

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, suppCode );
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				suppName= checkNull(rs.getString("supp_name"));
			}	
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			//fn_gencode_desc('TASK_CODE__DELAY','W_PUR_MILSTN', rtrim(PUR_MILSTN_RESCH.TASK_CODE__DELAY),'D') as task_descr
			sql = " select fn_gencode_desc('TASK_CODE','W_PORDER', rtrim(?),'D') as task_descr from dual ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, taskCode );
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				taskDescr= checkNull(rs.getString(1));
			}	
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			// remarks = "Penalty for "+taskCode+"("+taskDescr+") delayed by "+timeDiff+" days."; // Comment added by Sagar on 24/11/15
			//remarks = "Penalty request generated for "+taskCode+"("+taskDescr+") delayed by "+timeDiff+" days."; // Comment added by Sagar on 26/11/15

			// Code added by Sagar on 26/11/15 Start.


			if(taskCode!= null && taskCode.trim().length() > 0)
			{
				taskCode=taskCode.trim();
			}

			daySlab= ""+minDaysDlvTerm+"-"+maxDaysDlvTerm+"";
			chargesSlab= ""+minAmtDlvTerm+"-"+maxAmtDlvTerm+"";

			if(fchgTypeDlvTerm!=null && fchgTypeDlvTerm.trim().length() > 0)
			{
				fchgTypeDlvTerm=fchgTypeDlvTerm.trim();

				if("F".equals(fchgTypeDlvTerm))
				{
					finChrgTypeDescr="Fixed";
				}
				else if("P".equals(fchgTypeDlvTerm))
				{
					finChrgTypeDescr="Percentage";

				}
				else if("Q".equals(fchgTypeDlvTerm))
				{
					finChrgTypeDescr="Per Quantity";
				}
			}
			remarks="Request Generated for Task: ("+taskCode+"), Days slab: ("+daySlab+"), Charges slab("+chargesSlab+"), Type: ("+finChrgTypeDescr+"), Financial Charges: ("+finChrgeDlvTerm+") task delayed by "+timeDiff+" days";

			System.out.println(">>>daySlab:"+daySlab);
			System.out.println(">>>chargesSlab:"+chargesSlab);
			System.out.println(">>>finChrgTypeDescr:"+finChrgTypeDescr);
			System.out.println(">>>finChrgeDlvTerm:"+finChrgeDlvTerm);
			System.out.println(">>>remarks:"+remarks);

			// Code added by Sagar on 26/11/15 End.

			// Code added by Sagar on 27/11/15 Start.

			sql = " select ord_amt, tax_amt, tot_amt from porder where purc_order= ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, purcOrder);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				ordAmtPord= rs.getDouble("ord_amt");
				taxAmtPord= rs.getDouble("tax_amt");
				totAmtPord= rs.getDouble("tot_amt");
			}	
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			System.out.println(">>>Delay ordAmtPord:"+ ordAmtPord);
			System.out.println(">>>Delay taxAmtPord:"+ taxAmtPord);
			System.out.println(">>>Delay totAmtPord:"+ totAmtPord);

			sql = " select descr from gencodes where fld_name='TASK_CODE' and mod_name='W_PORDER' and fld_value= ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, taskCode);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				taskCodeDescr= checkNull(rs.getString("descr"));
			}	
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			System.out.println(">>>taskCodeDescr:"+ taskCodeDescr);
			// Code added by Sagar on 27/11/15 End.


			xmlBuff = new StringBuffer();
			System.out.println("--XML CREATION !!!!!--");
			xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
			xmlBuff.append("<DocumentRoot>");
			xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
			xmlBuff.append("<group0>");
			xmlBuff.append("<description>").append("Group0 description").append("</description>");
			xmlBuff.append("<Header0>");
			xmlBuff.append("<objName><![CDATA[").append("pur_milstn").append("]]></objName>");  
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
			/*---Detail1 screen,starts----*/
			xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"pur_milstn\" objContext=\"1\">");  
			xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
			xmlBuff.append("<tran_id/>");
			xmlBuff.append("<tran_date><![CDATA["+ sdf.format(currentDate).toString() +"]]></tran_date>");
			xmlBuff.append(	"<purc_order><![CDATA["+purcOrder+"]]></purc_order>");
			xmlBuff.append(	"<line_no__ord><![CDATA["+lineNoOrd+"]]></line_no__ord>");
			xmlBuff.append(	"<tran_type><![CDATA[D]]></tran_type>");
			xmlBuff.append(	"<site_code><![CDATA["+siteCode+"]]></site_code>");
			xmlBuff.append(	"<supp_code><![CDATA["+suppCode+"]]></supp_code>");
			xmlBuff.append(	"<supp_name><![CDATA["+suppName+"]]></supp_name>");
			xmlBuff.append(	"<acct_code><![CDATA["+acctCode+"]]></acct_code>");
			xmlBuff.append(	"<cctr_code><![CDATA["+cctrCode+"]]></cctr_code>");
			xmlBuff.append(	"<tax_class><![CDATA["+taxClass+"]]></tax_class>");
			xmlBuff.append(	"<tax_chap><![CDATA["+taxChap+"]]></tax_chap>");
			xmlBuff.append(	"<tax_env><![CDATA["+taxEnv+"]]></tax_env>");
			xmlBuff.append(	"<task_code><![CDATA["+taskCode+"]]></task_code>");
			xmlBuff.append(	"<task_descr><![CDATA["+taskCodeDescr+"]]></task_descr>"); // Code added by Sagar on 27/11/15
			//	xmlBuff.append(	"<amount><![CDATA["+amount+"]]></amount>");  //penalatyAmount
			xmlBuff.append(	"<amount><![CDATA["+penalatyAmount+"]]></amount>");  //penalatyAmount
			xmlBuff.append(	"<due_date><![CDATA["+sdf.format(dueDate).toString()+"]]></due_date>");
			//	xmlBuff.append(	"<due_date><![CDATA["+dueDate+"]]></due_date>");
			xmlBuff.append(	"<task_status><![CDATA[P]]></task_status>");
			xmlBuff.append(	"<payment_opt><![CDATA[P]]></payment_opt>");
			xmlBuff.append(	"<remarks><![CDATA["+remarks+"]]></remarks>");
			xmlBuff.append(	"<tran_id__vch><![CDATA[]]></tran_id__vch>");
			xmlBuff.append(	"<confirmed><![CDATA[N]]></confirmed>");
			xmlBuff.append(	"<conf_date><![CDATA[]]></conf_date>");
			xmlBuff.append(	"<emp_code__aprv><![CDATA[]]></emp_code__aprv>");
			xmlBuff.append(	"<wf_status><![CDATA[S]]></wf_status>");
			xmlBuff.append(	"<status_date><![CDATA[]]></status_date>");
			//xmlBuff.append(	"<compl_date><![CDATA[]]></compl_date>");
			xmlBuff.append(	"<compl_date><![CDATA["+complDateStr+"]]></compl_date>");
			xmlBuff.append(	"<allow_override><![CDATA["+allowOverride+"]]></allow_override>");
			xmlBuff.append(	"<amount_orig><![CDATA["+penalatyAmount+"]]></amount_orig>");
			//		xmlBuff.append(	"<amount_orig><![CDATA["+amountOrig+"]]></amount_orig>");
			xmlBuff.append(	"<date_format><![CDATA["+genericUtility.getApplDateFormat()+"]]></date_format>");
			xmlBuff.append(	"<add_user><![CDATA["+chgUser+"]]></add_user>");
			xmlBuff.append(	"<add_date><![CDATA["+ sdf.format(currentDate).toString() +"]]></add_date>");
			xmlBuff.append(	"<add_term><![CDATA["+chgTerm+"]]></add_term>");
			xmlBuff.append(	"<chg_user><![CDATA["+chgUser+"]]></chg_user>");
			xmlBuff.append(	"<chg_date><![CDATA["+ sdf.format(currentDate).toString() +"]]></chg_date>");
			xmlBuff.append(	"<chg_term><![CDATA["+chgTerm+"]]></chg_term>");

			xmlBuff.append(	"<ord_amt><![CDATA["+ordAmtPord+"]]></ord_amt>"); // Code added by Sagar on 27/11/15 Start.
			xmlBuff.append(	"<tax_amt><![CDATA["+taxAmtPord+"]]></tax_amt>");
			xmlBuff.append(	"<tot_amt><![CDATA["+totAmtPord+"]]></tot_amt>"); // Code added by Sagar on 27/11/15 End.
			xmlBuff.append("</Detail1>");
			/*---Detail1 screen,end----*/

			/*	xmlBuff.append("<Detail2 dbID='' domID=\"1\" objName=\"drcrpay_dr\" objContext=\"2\">");
			xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" updateFlag=\"A\" status=\"N\" />");
			xmlBuff.append("<tran_id/>");
			xmlBuff.append("<line_no><![CDATA[]]></line_no>");
			xmlBuff.append("<delay_days><![CDATA[]]></delay_days>");
			xmlBuff.append("<task_code__delay><![CDATA[]]></task_code__delay>");
			xmlBuff.append("<due_date_org><![CDATA[]]></due_date_org>");
			xmlBuff.append("<due_date_resch><![CDATA[]]></due_date_resch>");
			xmlBuff.append("</Detail2>");*/

			xmlBuff.append("</Header0>");
			xmlBuff.append("</group0>");
			xmlBuff.append("</DocumentRoot>");
			String 	xmlString = xmlBuff.toString();

			System.out.println("xmlString["+xmlString+"]");
			//Changes and Commented By Ajay on 08-01-2018:START
            String userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode"); 
            System.out.println("--login code--"+userId);
			//returnStr1 = saveData(siteCode,xmlString,conn);
			returnStr1 = saveData(siteCode,xmlString,userId,conn);
			//Changes and Commented By Ajay on 08-01-2018:END
			System.out.println(" saveData returnStr1["+returnStr1+"]");
			minDaysDlvTerm=0;
			maxDaysDlvTerm=0;
			minAmtDlvTerm=0.0;
			maxAmtDlvTerm=0.0;
			finChrgeDlvTerm=0.0;
			fchgTypeDlvTerm="";
		}
		if( "D".equalsIgnoreCase(tranType))
		{  // debit note created  created
			System.out.println("@@@@@@@@  debit note created  created for tran type 'D' ");

			HashMap<String , String>  drcrMap = new HashMap<String, String>();

			// sql= "  select  tran_id__vch  from pur_milstn where tran_id = ? ";
			sql = " select tran_id__vch from pur_milstn  where tran_id = ? and tran_id__vch is not null ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				tranIdVch = checkNull(rs.getString("tran_id__vch"));
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("@@@@@@@@@tranId["+tranId+"]tranIdVch["+tranIdVch+"]");

			if( tranIdVch == null || tranIdVch.trim().length() == 0)
			{
				sql = " select tran_id from voucher where purc_order = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, purcOrder);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					tranIdVch = checkNull(rs.getString("tran_id"));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				System.out.println("@@@@@@@@@tranId["+tranId+"]tranIdVch["+tranIdVch+"]");

			}

			drcrMap.put("tran_id", tranId);
			drcrMap.put("voucher_id", tranIdVch);  
			drcrMap.put("purc_order", purcOrder);
			drcrMap.put("task_code", taskCode);
			drcrMap.put("due_date", String.valueOf(dueDate));
			drcrMap.put("process_date", sysDate);
			drcrMap.put("rel_amt", String.valueOf(amount));
			drcrMap.put("date_diff", String.valueOf(timeDiff));

			if( amount > 0 )
			{
				returnStr = createDebitNote(drcrMap,conn);
				System.out.println("createDebitNote tranId["+tranId+"]returnStr["+returnStr+"]");
				if( returnStr.indexOf("Success") > -1 )
				{
					System.out.println("createDebitNote created Successfully["+returnStr+"]");
					// Code added by Sagar on 20/11/15, Start..
					String[] arrayForTranId = returnStr.split("<TranID>");
					int endIndex = arrayForTranId[1].indexOf("</TranID>");
					String tranIdForDebit = arrayForTranId[1].substring(0,endIndex);
					tranIdForDebit=tranIdForDebit==null ? "" : tranIdForDebit.trim();
					System.out.println(">>tranIdForDebit["+tranIdForDebit+"]");	

					/*java.util.Date date1= new java.util.Date();
				    Timestamp ts_now = new Timestamp(date1.getTime());
				    System.out.println(">>ts_now["+ts_now+"]");*/

					sql = " update pur_milstn set wf_status = 'C' ,conf_date = ? ,status_date = ? , confirmed = 'Y', tran_id__vch= ?  where tran_id = ? ";
					System.out.println("sql :"+sql );
					pstmt = conn.prepareStatement(sql);
					pstmt.setTimestamp(1, newsysDate);
					pstmt.setTimestamp(2, wfStatusDate); 
					pstmt.setString(3, tranIdForDebit);
					pstmt.setString(4, tranId);
					int cntDebit = pstmt.executeUpdate();
					pstmt.close();
					pstmt = null;
					System.out.println(">>>cntDebit:"+cntDebit);
					// Code added by Sagar on 20/11/15, End.

					conn.commit();

					// Comment added by Sagar on 20/11/15
					/*String[] arrayForTranId = returnStr.split("<TranID>");
					int endIndex = arrayForTranId[1].indexOf("</TranID>");
					String tranIdForDebit = arrayForTranId[1].substring(0,endIndex);
					tranIdForDebit=tranIdForDebit==null ? "" : tranIdForDebit.trim();
					System.out.println("-tranIdForDebit["+tranIdForDebit+"]");	*/

					//returnStr = confirmTranscation("drcrpay_dr",tranIdForDebit,xtraParams,conn); // Comment added by Sagar on 20/11/15 

					// Code added by Sagar on 20/11/15, Start.

					FinCommon fincommon = new FinCommon();
					mileStoneDebitAutoCnf= fincommon.getFinparams("999999", "MLSTNDEBIT_AUTOCNF", conn);
					System.out.println(">>>mileStoneDebitAutoCnf:"+mileStoneDebitAutoCnf);
					if(mileStoneDebitAutoCnf!= null && mileStoneDebitAutoCnf.trim().length() > 0)
					{
						mileStoneDebitAutoCnf= mileStoneDebitAutoCnf.trim();
						if("NULLFOUND".equals(mileStoneDebitAutoCnf))
						{
							mileStoneDebitAutoCnf ="N";
						}
					}
					else
					{
						mileStoneDebitAutoCnf ="N";
					}

					if("Y".equalsIgnoreCase(mileStoneDebitAutoCnf)) 
					{
						returnStr = confirmTranscation("drcrpay_dr",tranIdForDebit,xtraParams,conn);
					}
					else
					{
						returnStr = "Success";
					}
					// Code added by Sagar on 20/11/15, End.

					System.out.println("confirmTranscation debit node returnStr["+returnStr+"]");


					//	conn.commit();

					if(returnStr.indexOf("Success") > -1 || returnStr.indexOf("VTSUCC1") > -1   )
					{
						// Comment added by Sagar on 20/11/15, Start
						/*sql = " update pur_milstn set  tran_id__vch  = ?  where tran_id = ? ";
						System.out.println("sql :"+sql );
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,tranIdForDebit);
						pstmt.setString(2,tranId);
						int cnt = pstmt.executeUpdate();
						pstmt.close();
						pstmt = null;
						System.out.println("@@@@@ updated sucessfully pur_milstn tranId["+tranId+"]tranIdForDebit["+tranIdForDebit+"]cnt["+cnt+"]");
						 */
						// Comment added by Sagar on 20/11/15, End

						//	conn.commit();
					}
					else
					{
						System.out.println("confirmTranscation debit node failed ...returnStr... rollback()..["+returnStr+"]");
						conn.rollback();
					}

				}
				else
				{
					System.out.println("createDebitNote returnStr... rollback()..["+returnStr+"]");
					conn.rollback();
				}

			} // end amount condition
			else
			{
				// Code added by Sagar on 23/11/15, Start.
				System.out.println(">>>If tranType 'D' and amount in less than ZERO then update....");

				sql = " update pur_milstn set wf_status = 'C' ,conf_date = ? ,status_date = ? , confirmed = 'Y' where tran_id = ? ";
				System.out.println("sql :"+sql );
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, newsysDate);
				pstmt.setTimestamp(2, wfStatusDate); 
				pstmt.setString(3, tranId);
				int cntPurMlstn = pstmt.executeUpdate();
				pstmt.close();
				pstmt = null;
				System.out.println(">>>If paymentOpt is 'N' cntPurMlstn:"+ cntPurMlstn);
				conn.commit();
				// Code added by Sagar on 23/11/15, End.
				returnStr = "SucAmtZero";
			}
		}

		System.out.println("@@@@@@@@@@@ before status update pur_milstn returnStr["+returnStr+"]");


		//if(returnStr.indexOf("Success") > -1 || returnStr.indexOf("VTSUCC1") > -1  || returnStr.indexOf("SucAmtZero") > -1 ) // Comment Added by Sagar on 20/11/15 
		if(returnStr.indexOf("SucAmtZero") > -1 ) // Condition added by Sagar on 20/11/15
		{
			/*java.util.Date date1= new java.util.Date();
		     Timestamp ts_now = new Timestamp(date1.getTime());
		     System.out.println("ts_now["+ts_now+"]");    
			 */
			sql = " update pur_milstn set wf_status = 'C' ,conf_date = ? ,status_date = ? , confirmed = 'Y'  where tran_id = ? ";
			System.out.println("sql :"+sql );
			pstmt = conn.prepareStatement(sql);
			pstmt.setTimestamp(1,newsysDate);
			pstmt.setTimestamp(2,wfStatusDate); 
			pstmt.setString(3,tranId);
			int cnt = pstmt.executeUpdate();
			pstmt.close();
			pstmt = null;
			System.out.println("@@@@@ updated sucessfully pur_milstn tranId["+tranId+"]cnt["+cnt+"]wf_status='c',confirmed='Y'");
			conn.commit();
		}

		System.out.println("@@@@@@@@@@@ final returnStr["+returnStr+"]");
		return returnStr;

	}

	private int calculateDelayDays(String xmlDataAll) throws Exception 
	{
		UtilMethods utilmethod = new UtilMethods();
		System.out.println("@@@@@@@ calculateDelayDays Due Date ******");
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String sql = "";
		int delayDays=0,count = 0,count1 = 0;
		Timestamp parsedReschDate = null;
		int lineNo = 0;
		String complDate="";
		String dueDateOrgStr = "",taskCode = "";
		Document dom = null;
		PreparedStatement pstmt = null,pstmt1 = null,pstmt2 = null;		
		ResultSet rs=null,rs2=null;
		int ctr=0;		
		int childNodeListLength;
		String childNodeName = null,sqlMax = "",tranIdPending = "";
		Timestamp dueDateHeader = null,reschDate = null;
		Timestamp complDateNew = null,dueDateOrg = null;
		String retString = "SUCCESS";
		String sqlResch ="",dateFormat = "";
		System.out.println("xmlString DOM-->>["+xmlDataAll+"]");
		System.out.println("recalcDueDate called................");

		E12GenericUtility genericUtility = new E12GenericUtility();

		dom =  genericUtility.parseString(xmlDataAll);// read xmldataAll

		parentNodeList = dom.getElementsByTagName("Detail1");
		parentNode = parentNodeList.item(0);
		childNodeList = parentNode.getChildNodes();
		childNodeListLength = childNodeList.getLength();

		for(ctr = 0; ctr < childNodeListLength; ctr++)
		{
			childNode = childNodeList.item(ctr);
			childNodeName = childNode.getNodeName();
			System.out.println("Child name --->> "+childNodeName);	

			if(childNodeName.equalsIgnoreCase("compl_date")) 
			{
				complDate = checkNull(genericUtility.getColumnValue("compl_date",dom));
				System.out.println("complDate--->["+complDate+"]");

			}
			if(childNodeName.equalsIgnoreCase("task_code")) 
			{
				taskCode = checkNull(genericUtility.getColumnValue("task_code",dom));
				System.out.println("taskCode--->["+taskCode+"]");

			}
			if(childNodeName.equalsIgnoreCase("due_date")) 
			{
				dueDateOrgStr = checkNull(genericUtility.getColumnValue("due_date",dom));
				System.out.println("dueDateOrgStr--->["+dueDateOrgStr+"]");

			}
			if(childNodeName.equalsIgnoreCase("date_format")) 
			{
				dateFormat = checkNull(genericUtility.getColumnValue("date_format",dom));
				System.out.println("dateFormat--->["+dateFormat+"]");

			}

		}
		if(complDate.length() > 0)//completion date is not null
		{
			complDateNew =  Timestamp.valueOf(genericUtility.getValidDateString(complDate, dateFormat,genericUtility.getDBDateFormat()) + " 00:00:00.0");

			System.out.println("completionDate ====parsed into sql format:::::"+complDateNew);
		}
		if(dueDateOrgStr.length() > 0)
		{
			dueDateOrg = Timestamp.valueOf(genericUtility.getValidDateString(dueDateOrgStr,dateFormat,genericUtility.getDBDateFormat()) + " 00:00:00.0");

			System.out.println("Due Date Original ====parsed into sql format:::==="+dueDateOrg);
		}
		if(dueDateOrg.before(complDateNew))//if completion date is greater than  due date original
		{
			System.out.println("days-----"+delayDays);
			delayDays =  (int)utilmethod.DaysAfter(dueDateOrg, complDateNew); //(int)countDaysBetween(dueDateOrg,complDateNew);
			System.out.println("delayed days-----"+delayDays);
		}


		return delayDays;
	}


	private double penalatyAmount(Document xmlDataAllDom,String purcOrder,String relAmt,double datediff,String taskCode,Connection conn ) throws SQLException 
	{
		System.out.println("penalatyAmount().calculation.....called...datediff :"+datediff);
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		String sql="";
		int cnt = 0,count=0,detailsCnt =0,shipmentleadTime=0,lineNo=0,minDays=0,maxDays=0;
		//ConnDriver connDriver = new ConnDriver();
		//ITMDBAccessLocal itmDBAccessLocal = new ITMDBAccessEJB();
		//	long diff=0,datediff=0;
		double finChrge=0.0,minAmt=0.0,maxAmt=0.0,amtDet=0.0,taxAmtDet=0.0,amount=0.0,pentlyAmt=0.0;
		double exchRate = 0.0,billAmt=0.0,taxAmt=0.0,totAmt=0.0,netAMt=0.0;
		String fchgType="" ;


		sql = " select min_day,max_day,fin_chg,fchg_type,min_amt,max_amt from pord_dlv_term where purc_order= ? " +
		" and REF_CODE=? and ? between min_day and max_day  ";
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, purcOrder);
		pstmt.setString(2, taskCode);
		pstmt.setDouble(3, datediff);
		rs = pstmt.executeQuery();
		if (rs.next())
		{

			//siteCode = rs.getString("SITE_CODE");
			minDays=rs.getInt("min_day");
			maxDays=rs.getInt("max_day");
			finChrge=rs.getDouble("fin_chg");
			fchgType=rs.getString("fchg_type");
			minAmt=rs.getDouble("min_amt");
			maxAmt=rs.getDouble("max_amt");

			fchgType=fchgType==null ? "" :fchgType;
			System.out.println("minDays :"+minDays);
			System.out.println("maxDays :"+maxDays);
			System.out.println("finChrge :"+finChrge);
			System.out.println("fchgType :"+fchgType);
			System.out.println("minAmt :"+minAmt);
			System.out.println("maxAmt :"+maxAmt);

			// Code added by Sagar on 26/11/15 Start.
			minDaysDlvTerm= minDays;
			maxDaysDlvTerm= maxDays;
			minAmtDlvTerm= minAmt;
			maxAmtDlvTerm= maxAmt;
			finChrgeDlvTerm= finChrge;
			fchgTypeDlvTerm= fchgType;

			// Code added by Sagar on 26/11/15 End.

			System.out.println("========Penalty amount calculation=======");
			System.out.println("Purchase order :"+purcOrder);
			System.out.println("relAmt :"+relAmt);
			System.out.println("finChrge:"+finChrge);
			if(fchgType.equalsIgnoreCase("P"))
			{
				amount=Double.parseDouble(relAmt);
				System.out.println("Formula :pentlyAmt=(finChrge * amount)/100;");
				pentlyAmt=(finChrge * amount)/100;//finChrge conside as percentage
				System.out.println("Calculated pentlyAmt :"+pentlyAmt);
			}
			else if(fchgType.equalsIgnoreCase("Q"))
			{
				System.out.println("Formula :pentlyAmt=finChrge * amount");
				amount=Double.parseDouble(relAmt);
				pentlyAmt=finChrge * amount;//finChrge conside as Quantity
			}
			else
			{
				System.out.println("Formula:pentlyAmt=finChrge;");
				System.out.println("Formula :pentlyAmt=finChrge");
				pentlyAmt=finChrge;//finChrge conside as fixed amount
			}

			System.out.println("calculate penalaty amount :"+pentlyAmt);

			if(pentlyAmt > maxAmt)
			{
				pentlyAmt=maxAmt;
			}
			else if(pentlyAmt < minAmt)
			{
				pentlyAmt=minAmt;
			}
			//	flag=true;

			System.out.println("selected pentlyAmt from min to max amount:"+pentlyAmt);
		}
		else
		{
			System.out.println("data not found in pord_dlv_term for penalty calculation!!");
			//	flag=false;
		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;

		return pentlyAmt;
	}


	private String voucherValidate( String purcOrder, double vouchAmt, Connection conn ) throws SQLException 
	{
		System.out.println("@@@@@@ voucherValidate called............");
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		String sql="",  returnStr= ""; // tranIdForVoucher="",
		double vouchTotAmt=0;
		double ordAmt=0 ;

		sql = " select sum(net_amt)  from voucher where purc_order = ? " ;
		// "and confirmed = 'Y' " ;
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, purcOrder);
		rs = pstmt.executeQuery();
		if (rs.next())
		{
			vouchTotAmt = rs.getDouble(1);
		}	
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;

		sql = "select ord_amt from porder where purc_order= ? " ;
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, purcOrder);
		rs = pstmt.executeQuery();
		if (rs.next())
		{
			ordAmt = rs.getDouble("ord_amt");
		}	
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;


		double differenceAmt = ordAmt - vouchTotAmt ;

		System.out.println("ordAmt["+ordAmt+"]-vouchNetAmt["+vouchTotAmt+"]=vouchAmt["+vouchAmt+"]>differenceAmt["+differenceAmt+"]");

		if(vouchAmt > differenceAmt)
		{
			returnStr = "RELAMTGRT";
			return returnStr;
		}
		return "Success";
	}


	private Timestamp getCurrdateAppFormat()
	{
		//String s = "";	
		Timestamp timestamp = null;		
		E12GenericUtility genericUtility = new E12GenericUtility();
		try
		{
			java.util.Date date = null;
			timestamp = new Timestamp(System.currentTimeMillis());

			SimpleDateFormat simpledateformat = new SimpleDateFormat(genericUtility.getDBDateFormat());
			date = simpledateformat.parse(timestamp.toString());
			timestamp = Timestamp.valueOf(simpledateformat.format(date).toString() + " 00:00:00.0");
		}
		catch(Exception exception)
		{
			System.out.println("Exception in  getCurrdateAppFormat " + exception.getMessage());
		}
		return timestamp;
	}


	public String createDebitNote(HashMap<String,String> drcrMap,Connection conn)
	throws RemoteException, ITMException {

		StringBuffer xmlBuff = null;
		String xmlString = null;
		// Connection conn = null;

		PreparedStatement pstmt = null,pstmt1 = null,pstmt2 = null,pstmt3 = null;
		ResultSet rs = null, rs1 = null,rs2 = null,rs3 = null;

		String retString = "",errString = "",siteCode="",chgUser="",chgTerm="",loginEmpCode="",acctCodeDr="",cctrCodeDr="";
		String 	suppCode="",currCode="",acctCode="",cctrCode="",tranSer="",bankCode="",finEntity="",vouchType="",projCode="";
		String dueDate="",processDate="",voucherId="",xtraParams="",lineNoStr = "", frtTerm = "";
		String noArticle="0",entityNameDlv="",relAmt="",purcOrder="",taskCode="",retString1="",sql="",fchgType="";
		String lineNoDet="",acctCdDet="",cctrCdDet="",exchRateDet="",analysisDet1="",analysisDet2="",analysisDet3="",taxClassDet="",taxChapDet="",taxEnvDet="",taxChepDet="";
		Date dispatchDate=null,processDate1=null,dueDate1=null;
		int cnt = 0,count=0,detailsCnt =0,shipmentleadTime=0,lineNo=0,minDays=0,maxDays=0;
		//ConnDriver connDriver = new ConnDriver();
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		long diff=0,datediff=0;
		double finChrge=0.0,minAmt=0.0,maxAmt=0.0,amtDet=0.0,taxAmtDet=0.0,amount=0.0,pentlyAmt=0.0;
		double exchRate = 0.0,billAmt=0.0,taxAmt=0.0,totAmt=0.0,netAMt=0.0;
		Date taxDate=null,effDate=null;
		//	HashMap<String,String> vocDetMap = new HashMap<String,String>();
		boolean flag=false;
		E12GenericUtility genericUtility = new E12GenericUtility();

		String taxClass="",taxChap="",taxEnv="";
		String tranIdDet="",acctCodeCr="",cctrCodeCr="",acctap="",cctrap="";
		String ordDateApp="";
		String remark="",lineNoOrd="",purcOrdNo="";
		String taskCodePurMilstn="",tranIdVouch="";
		double amountPurMilstn=0.0;
		java.sql.Timestamp ordDate = null;

		try 
		{
			System.out.println("createDebitNote called....");
			Date currentDate = new Date();
			xmlBuff = null;
			xmlBuff = new StringBuffer();

			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
			SimpleDateFormat sdfApp= new SimpleDateFormat(genericUtility.getApplDateFormat());

			System.out.println("application date format :"+genericUtility.getApplDateFormat());

			dueDate=drcrMap.get("due_date");
			System.out.println("dueDate :"+dueDate);
			processDate=drcrMap.get("process_date");
			System.out.println("processDate :"+processDate);
			voucherId=drcrMap.get("voucher_id");
			purcOrder=drcrMap.get("purc_order");
			relAmt=drcrMap.get("rel_amt");
			taskCode=drcrMap.get("task_code");
			chgUser=drcrMap.get("chg_user");
			chgTerm=drcrMap.get("chg_term");
			System.out.println("date_diff!!! : "+drcrMap.get("date_diff"));
			datediff=Long.parseLong(drcrMap.get("date_diff"));

			String tranId=drcrMap.get("tran_id");

			System.out.println("dueDate :"+dueDate);
			System.out.println("processDate :"+processDate);
			System.out.println("voucherId :"+voucherId);
			System.out.println("purcOrder :"+purcOrder);
			System.out.println("relAmt :"+relAmt);
			System.out.println("taskCode :"+taskCode);
			System.out.println("dueDate :"+dueDate);
			System.out.println("chgUser :"+chgUser);
			System.out.println("chgTerm :"+chgTerm);


			System.out.println("processDate :"+processDate);
			System.out.println("dueDate1 :"+dueDate1);
			System.out.println("processDate1:"+processDate);

			System.out.println("datediff :"+datediff);

			pentlyAmt = Double.parseDouble(relAmt== null ?"0":relAmt);

			System.out.println("@@@@@@@@@@@@ final pentlyAmt["+pentlyAmt+"]");

			/*
			select * from TARODEV.pord_dlv_term where purc_order='718PNF0299'
			and min_day <=14 and max_day>=14
			 */

			/*	sql = "select * from (select min_day,max_day,fin_chg,fchg_type,min_amt,max_amt from pord_dlv_term where " +
						"purc_order=? and REF_CODE=?  ORDER BY ABS( min_day - ? ) ,ABS( max_day - ? ) ) where rownum <=1 ";*/

			/*
			sql = " select min_day,max_day,fin_chg,fchg_type,min_amt,max_amt from pord_dlv_term where purc_order= ? " +
					" and REF_CODE=? and ? between min_day and max_day  ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, purcOrder);
			pstmt.setString(2, taskCode);
			pstmt.setLong(3, datediff);
			//pstmt.setLong(4, datediff);
			rs = pstmt.executeQuery();
			if (rs.next())
			{

				//siteCode = rs.getString("SITE_CODE");
				minDays=rs.getInt("min_day");
				maxDays=rs.getInt("max_day");
				finChrge=rs.getDouble("fin_chg");
				fchgType=rs.getString("fchg_type");
				minAmt=rs.getDouble("min_amt");
				maxAmt=rs.getDouble("max_amt");

				fchgType=fchgType==null ? "" :fchgType;
				System.out.println("minDays :"+minDays);
				System.out.println("maxDays :"+maxDays);
				System.out.println("finChrge :"+finChrge);
				System.out.println("fchgType :"+fchgType);
				System.out.println("minAmt :"+minAmt);
				System.out.println("maxAmt :"+maxAmt);

				System.out.println("========Penalty amount calculation=======");
				System.out.println("Purchase order :"+purcOrder);
				System.out.println("relAmt :"+relAmt);
				System.out.println("finChrge:"+finChrge);
				if(fchgType.equalsIgnoreCase("P"))
				{
					amount=Double.parseDouble(relAmt);
					System.out.println("Formula :pentlyAmt=(finChrge * amount)/100;");
					pentlyAmt=(finChrge * amount)/100;//finChrge conside as percentage
					System.out.println("Calculated pentlyAmt :"+pentlyAmt);
				}
				else if(fchgType.equalsIgnoreCase("Q"))
				{
					System.out.println("Formula :pentlyAmt=finChrge * amount");
					amount=Double.parseDouble(relAmt);
					pentlyAmt=finChrge * amount;//finChrge conside as Quantity
				}
				else
				{
					System.out.println("Formula:pentlyAmt=finChrge;");
					System.out.println("Formula :pentlyAmt=finChrge");
					pentlyAmt=finChrge;//finChrge conside as fixed amount
				}

				System.out.println("calculate penalaty amount :"+pentlyAmt);

				if(pentlyAmt > maxAmt)
				{
					pentlyAmt=maxAmt;
				}
				else if(pentlyAmt < minAmt)
				{
					pentlyAmt=minAmt;
				}
				flag=true;

				System.out.println("selected pentlyAmt from min to max amount:"+pentlyAmt);
			}
			else
			{
				System.out.println("data not found in pord_dlv_term for penalty calculation!!");
				flag=false;
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			 */
			/*		
			if flag is true then only debit node generated.
			flag true means purc_order data exist in pord_dlv_term and penalty is greater than 0.	
			 */	
			if( pentlyAmt > 0 )
			{

				sql = " select SUPP_CODE,PURC_ORDER,ACCT_CODE,CCTR_CODE,DUE_DATE,SITE_CODE " +
				" from pur_milstn  where tran_id = ? "; 
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				if (rs.next())
				{

					suppCode = rs.getString("SUPP_CODE");
					purcOrder = rs.getString("PURC_ORDER");
					acctCode = rs.getString("ACCT_CODE");
					cctrCode = rs.getString("CCTR_CODE");
					dueDate1 = rs.getDate("DUE_DATE");
					siteCode = checkNull(rs.getString("SITE_CODE"));

				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				sql = "  select fin_entity from site  where site_code = ? "; 
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, siteCode);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					finEntity = checkNull(rs.getString("fin_entity"));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				System.out.println("siteCode["+siteCode+"]finEntity["+finEntity+"]suppCode["+suppCode+"]");

				sql = "select  acct_code__ap,cctr_code__ap  from  supplier where supp_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, suppCode);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					acctap = rs.getString("acct_code__ap");
					cctrap = rs.getString("cctr_code__ap");

				}

				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;


				sql = " select CURR_CODE,EXCH_RATE , " +
				" TAX_AMT,TOT_AMT,TAX_DATE ,TAX_CLASS,TAX_CHAP,TAX_ENV ," +
				" PROJ_CODE  from porder where PURC_ORDER = ?" ;

				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, purcOrder);
				rs = pstmt.executeQuery();
				if (rs.next())
				{

					//	suppCode = rs.getString("SUPP_CODE");
					currCode = rs.getString("CURR_CODE");
					exchRate = rs.getDouble("EXCH_RATE");
					taxAmt = rs.getDouble("TAX_AMT");
					totAmt = rs.getDouble("TOT_AMT");
					taxDate = rs.getDate("TAX_DATE");

					//	taxClass = rs.getString("TAX_CLASS");
					//	taxChap = rs.getString("TAX_CHAP");
					//	taxEnv = rs.getString("TAX_ENV");

					projCode = rs.getString("PROJ_CODE");



					//	suppCode=suppCode==null ? "" :suppCode.trim();
					purcOrder=purcOrder==null ? "" :purcOrder.trim();
					currCode=currCode==null ? "" :currCode.trim();
					acctCode=acctCode==null ? "" :acctCode.trim();
					cctrCode=cctrCode==null ? " " :cctrCode;
					//	bankCode=bankCode==null ? "" :bankCode.trim();
					siteCode=siteCode==null ? "" :siteCode.trim();
					//	finEntity=finEntity==null ? "" :finEntity.trim();
					//	vouchType=vouchType==null ? "" :vouchType.trim();
					projCode=projCode==null ? "" :projCode.trim();



					//	System.out.println("effDate :"+effDate);
					System.out.println("suppCode :"+suppCode);
					System.out.println("purcOrder :"+purcOrder);
					System.out.println("finChrge :"+finChrge);
					System.out.println("fchgType :"+fchgType);
					System.out.println("currCode :"+currCode);
					System.out.println("exchRate :"+exchRate);
					System.out.println("acctCode :"+acctCode);
					System.out.println("cctrCode :"+cctrCode);
					//		System.out.println("bankCode :"+bankCode);
					System.out.println("dueDate :"+dueDate);
					System.out.println("dueDate1 :"+dueDate1);
					System.out.println("siteCode :"+siteCode);
					System.out.println("finEntity :"+finEntity);
					//		System.out.println("netAMt :"+netAMt);

				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;


				sql = " select acct_code__dr,cctr_code__dr,acct_code__cr,cctr_code__cr from porddet where purc_order= ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,purcOrder);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					acctCodeDr = rs.getString("acct_code__dr");
					cctrCodeDr = rs.getString("cctr_code__dr");

					acctCodeCr = rs.getString("acct_code__cr");
					cctrCodeCr = rs.getString("cctr_code__cr");

					acctCodeDr=acctCodeDr==null ? "" :acctCodeDr.trim();
					cctrCodeDr=cctrCodeDr==null ? "" :cctrCodeDr ;

					acctCodeCr=acctCodeCr==null ? "" :acctCodeCr.trim();
					cctrCodeCr=cctrCodeCr==null ? "" :cctrCodeCr ;

					System.out.println("acctCodeDr :"+acctCodeDr);
					System.out.println("cctCodeDr :"+cctrCodeDr);

					System.out.println("acctCodeCr :"+acctCodeCr);
					System.out.println("cctrCodeCr :"+cctrCodeCr);

				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				FinCommon fincommon = new FinCommon();

				String delayPenaltyAcct = fincommon.getFinparams("999999", "DELAY_PENALTY_ACCT", conn);

				if(delayPenaltyAcct != null && delayPenaltyAcct.trim().length()>0 && !("NULLFOUND".equalsIgnoreCase(delayPenaltyAcct)))
				{

					acctCodeDr = delayPenaltyAcct;
					cctrCodeDr = cctrCodeCr;

				}

				/*

				sql = " select line_no,site_code,tax_amt,tax_chap,tax_class,tax_env,acct_code__dr," +
						" cctr_code__dr from porddet where PURC_ORDER= ? " ;

				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, purcOrder);
				rs = pstmt.executeQuery();
				while(rs.next())
				{
					cnt++;
					lineNoDet = rs.getString("line_no");
					//	acctCdDet = rs.getString("ACCT_CODE");
					//	cctrCdDet = rs.getString("CCTR_CODE");
					//	amtDet = rs.getDouble("AMOUNT");
					taxAmtDet = rs.getDouble("TAX_AMT");
					//	exchRateDet = rs.getString("exch_rate");
					//	analysisDet1 = rs.getString("analysis1");
					//	analysisDet2 = rs.getString("analysis2");
					//	analysisDet3 = rs.getString("analysis3");
					//	taxChepDet = rs.getString("tax_chep");


					//		taxClassDet = rs.getString("tax_class");
					//		taxEnvDet = rs.getString("tax_env");
					//		taxChapDet = rs.getString("tax_chap");

					acctCodeDr = rs.getString("acct_code__dr");
					cctrCodeDr = rs.getString("cctr_code__dr");


					lineNoDet=lineNoDet==null ? "" :lineNoDet.trim();
					//	acctCdDet=acctCdDet==null ? "" :acctCdDet.trim();
					//	cctrCdDet=cctrCdDet==null ? "" :cctrCdDet.trim();
					//	exchRateDet=exchRateDet==null ? "" :exchRateDet.trim();
					//	analysisDet1=analysisDet1==null ? "" :analysisDet1.trim();
					//	analysisDet2=analysisDet2==null ? "" :analysisDet2.trim();
					//	analysisDet3=analysisDet3==null ? "" :analysisDet3.trim();
					taxClassDet=taxClassDet==null ? "" :taxClassDet.trim();
					taxChapDet=taxChapDet==null ? "" :taxChapDet.trim();
					//	taxChepDet=taxChepDet==null ? "" :taxChepDet.trim();
					taxEnvDet=taxEnvDet==null ? "" :taxEnvDet.trim();
					System.out.println("taxEnvDet :"+taxEnvDet);
					acctCodeDr=acctCodeDr==null ? "" :acctCodeDr.trim();
					cctrCodeDr=cctrCodeDr==null ? " " :cctrCodeDr;

					System.out.println("lineNoDet :"+lineNoDet);
					//	System.out.println("acctCdDet :"+acctCdDet);
					//	System.out.println("cctrCdDet :"+cctrCdDet);
					//	System.out.println("exchRateDet :"+exchRateDet);
					//	System.out.println("analysisDet1 :"+analysisDet1);
					//	System.out.println("analysisDet2 :"+analysisDet2);
					//	System.out.println("analysisDet3 :"+analysisDet3);
					System.out.println("taxClassDet :"+taxClassDet);
					System.out.println("taxChapDet :"+taxChapDet);

					System.out.println("acctCodeDr :"+acctCodeDr);
					System.out.println("cctCodeDr :"+cctrCodeDr);

					vocDetMap.put("line_no"+cnt, lineNoDet);
					vocDetMap.put("acct_code"+cnt, acctCode);
					vocDetMap.put("cctr_code"+cnt, cctrCode);
					vocDetMap.put("amount"+cnt, String.valueOf(amtDet));
					vocDetMap.put("tax_amt"+cnt, String.valueOf(taxAmtDet));
					vocDetMap.put("exch_rate"+cnt, exchRateDet);
					//	vocDetMap.put("analysis1"+cnt, analysisDet1);
					//	vocDetMap.put("analysis2"+cnt, analysisDet2);
					//	vocDetMap.put("analysis3"+cnt, analysisDet3);
					vocDetMap.put("tax_class"+cnt, taxClassDet);
					vocDetMap.put("tax_chap"+cnt, taxChapDet);
					//	vocDetMap.put("tax_chep"+cnt, taxChepDet);
					vocDetMap.put("tax_env"+cnt, taxEnvDet);

				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				 */
				/*
				double penaltyAmtDummy= pentlyAmt;
				sql = " select tran_id,LINE_NO,ACCT_CODE,CCTR_CODE,AMOUNT,TAX_AMT,EXCH_RATE," +
						"ANALYSIS1,ANALYSIS2,ANALYSIS3," +
						" TAX_CLASS,TAX_CHEP,TAX_ENV,TAX_CHAP " +
						" from vouchdet  where tran_id in ( select tran_id from voucher where purc_order = ? )" +
						" and AMOUNT > 0  " ;

				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, purcOrder);
				rs = pstmt.executeQuery();
				while(rs.next())
				{
					if( penaltyAmtDummy > 0 )
					{	
					cnt++;
					tranIdDet = rs.getString("tran_id");
					lineNoDet = rs.getString("line_no");
					acctCdDet = rs.getString("ACCT_CODE");
					cctrCdDet = rs.getString("CCTR_CODE");
					amtDet = rs.getDouble("AMOUNT");
					if( amtDet > penaltyAmtDummy)
					{
						amtDet = penaltyAmtDummy;
					}
					System.out.println("voucher :: tranIdDet["+tranIdDet+"]lineNoDet["+lineNoDet+"]amtDet["+amtDet+"]penaltyAmtDummy["+penaltyAmtDummy+"]");
					taxAmtDet = rs.getDouble("TAX_AMT");
					exchRateDet = rs.getString("exch_rate");
					analysisDet1 = rs.getString("analysis1");
					analysisDet2 = rs.getString("analysis2");
					analysisDet3 = rs.getString("analysis3");
					taxChepDet = rs.getString("tax_chep");


					taxClassDet = rs.getString("tax_class");
					taxEnvDet = rs.getString("tax_env");
					taxChapDet = rs.getString("tax_chap");

				//	acctCodeDr = rs.getString("acct_code__dr");
				//	cctrCodeDr = rs.getString("cctr_code__dr");

					tranIdDet=tranIdDet==null ? "" :tranIdDet.trim();
					lineNoDet=lineNoDet==null ? "" :lineNoDet.trim();
					acctCdDet=acctCdDet==null ? "" :acctCdDet.trim();
					cctrCdDet=cctrCdDet==null ? "" :cctrCdDet.trim();
					exchRateDet=exchRateDet==null ? "" :exchRateDet.trim();
					analysisDet1=analysisDet1==null ? "" :analysisDet1.trim();
					analysisDet2=analysisDet2==null ? "" :analysisDet2.trim();
					analysisDet3=analysisDet3==null ? "" :analysisDet3.trim();
					taxClassDet=taxClassDet==null ? "" :taxClassDet.trim();
					taxChapDet=taxChapDet==null ? "" :taxChapDet.trim();
					taxChepDet=taxChepDet==null ? "" :taxChepDet.trim();
					taxEnvDet=taxEnvDet==null ? "" :taxEnvDet.trim();
					System.out.println("taxEnvDet :"+taxEnvDet);
					acctCodeDr=acctCodeDr==null ? "" :acctCodeDr.trim();
					cctrCodeDr=cctrCodeDr==null ? " " :cctrCodeDr;

					System.out.println("tranIdDet :"+tranIdDet);
					System.out.println("lineNoDet :"+lineNoDet);
					System.out.println("acctCdDet :"+acctCdDet);
					System.out.println("cctrCdDet :"+cctrCdDet);
					System.out.println("exchRateDet :"+exchRateDet);
					System.out.println("analysisDet1 :"+analysisDet1);
					System.out.println("analysisDet2 :"+analysisDet2);
					System.out.println("analysisDet3 :"+analysisDet3);
					System.out.println("taxClassDet :"+taxClassDet);
					System.out.println("taxChapDet :"+taxChapDet);

					System.out.println("acctCodeDr :"+acctCodeDr);
					System.out.println("cctCodeDr :"+cctrCodeDr);

					vocDetMap.put("tran_id"+cnt, tranIdDet);
					vocDetMap.put("line_no"+cnt, lineNoDet);
					vocDetMap.put("acct_code"+cnt, acctCode);
					vocDetMap.put("cctr_code"+cnt, cctrCode);
					vocDetMap.put("amount"+cnt, String.valueOf(amtDet));
					vocDetMap.put("tax_amt"+cnt, String.valueOf(taxAmtDet));
					vocDetMap.put("exch_rate"+cnt, exchRateDet);
					vocDetMap.put("analysis1"+cnt, analysisDet1);
					vocDetMap.put("analysis2"+cnt, analysisDet2);
					vocDetMap.put("analysis3"+cnt, analysisDet3);
					vocDetMap.put("tax_class"+cnt, taxClassDet);
					vocDetMap.put("tax_chap"+cnt, taxChapDet);
					vocDetMap.put("tax_chep"+cnt, taxChepDet);
					vocDetMap.put("tax_env"+cnt, taxEnvDet);

					penaltyAmtDummy = penaltyAmtDummy - amtDet;
					}
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				 */

				//	System.out.println("@@@@@@@@@@@@@@ vocDetMap["+vocDetMap+"]");

				// Code added by Sagar on 26/11/15 Start.
				sql = "select ord_date from porder where purc_order= ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, purcOrder);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					ordDate = rs.getTimestamp("ord_date");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				System.out.println(">>>ordDate:"+ordDate);

				if(ordDate!= null)
				{
					ordDateApp = sdfApp.format(ordDate);
				}
				System.out.println(">>>ordDateApp:"+ordDateApp);
				// Code added by Sagar on 26/11/15 End.

				tranSer="DRNPAY";

				siteCode = siteCode == null ? "" : siteCode.trim();


				// Code added by Sagar on 27/11/15 Start
				System.out.println(">>>tranId:"+tranId);
				//getting line_no__ord
				sql = "select line_no__ord, purc_order from pur_milstn where tran_id= ? and tran_type='D' ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					lineNoOrd = checkNull(rs.getString("line_no__ord"));
					purcOrdNo = checkNull(rs.getString("purc_order"));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				System.out.println(">>>for drcrpay_dr lineNoOrd:"+lineNoOrd);
				System.out.println(">>>for drcrpay_dr purcOrdNo:"+purcOrdNo);

				//getting amout,voucher no, task_code
				sql = " select amount, tran_id__vch, task_code from pur_milstn where purc_order= ? and line_no__ord= ? and tran_type='P' ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, purcOrdNo);
				pstmt.setString(2, lineNoOrd);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					amountPurMilstn = rs.getDouble("amount");
					tranIdVouch = checkNull(rs.getString("tran_id__vch"));
					taskCodePurMilstn = checkNull(rs.getString("task_code"));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				System.out.println(">>>for drcrpay_dr amountPurMilstn:"+amountPurMilstn);
				System.out.println(">>>for drcrpay_dr tranIdVouch:"+tranIdVouch);
				System.out.println(">>>for drcrpay_dr taskCodePurMilstn:"+taskCodePurMilstn);

				//remark="Generated Against Voucher No: "+tranIdVouch+" , Voucher Amt: "+amountPurMilstn+" , For Task: "+taskCodePurMilstn+" ";
				remark="Voucher:"+tranIdVouch+",Amt:"+amountPurMilstn+",Task:"+taskCodePurMilstn+"";  // added for maintain remarks lenght not more than 60 character by cpatil on 30/11/15 

				System.out.println(">>>for drcrpay_dr remark:"+remark);
				// Code added by Sagar on 27/11/15 End

				// create xml for track information
				xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
				xmlBuff.append("<DocumentRoot>");
				xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
				xmlBuff.append("<group0>");
				xmlBuff.append("<description>").append("Group0 description").append("</description>");
				xmlBuff.append("<Header0>");
				xmlBuff.append("<objName><![CDATA[").append("drcrpay_dr").append("]]></objName>");
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

				System.out.println("details 1 start!!!!!");

				xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"drcrpay_dr\" objContext=\"1\">");
				xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
				xmlBuff.append("<tran_id/>");
				xmlBuff.append("<tran_date><![CDATA["+ sdf.format(currentDate).toString() + "]]></tran_date>");
				xmlBuff.append("<tran_ser><![CDATA[" + tranSer.trim()+ "]]></tran_ser>");
				xmlBuff.append("<fin_entity><![CDATA[" + finEntity.trim()+ "]]></fin_entity>");
				xmlBuff.append("<supp_code><![CDATA[" + suppCode.trim()+ "]]></supp_code>");
				xmlBuff.append("<acct_code><![CDATA[" + acctCodeCr.trim()+ "]]></acct_code>");
				xmlBuff.append("<cctr_code><![CDATA[" + cctrCodeCr+ "]]></cctr_code>");
				if(effDate == null)
				{
					xmlBuff.append("<eff_date><![CDATA[" +  sdf.format(currentDate).toString() + "]]></eff_date>");			
				}
				else
				{
					xmlBuff.append("<eff_date><![CDATA[" + sdf.format(effDate)+ "]]></eff_date>");
				}

				xmlBuff.append("<vouch_ser><![CDATA[]]></vouch_ser>");
				//	xmlBuff.append("<vouch_no><![CDATA[" + checkNull(voucherId)+ "]]></vouch_no>");
				xmlBuff.append("<vouch_no><![CDATA[]]></vouch_no>");
				xmlBuff.append("<amount><![CDATA[" + pentlyAmt + "]]></amount>");
				xmlBuff.append("<curr_code><![CDATA[" + checkNull(currCode)+ "]]></curr_code>");
				xmlBuff.append("<exch_rate><![CDATA[" + exchRate+ "]]></exch_rate>");
				//xmlBuff.append("<remarks><![CDATA[]]></remarks>");  // Comment added by Sagar on 26/11/15
				xmlBuff.append("<remarks><![CDATA["+remark+"]]></remarks>");  // Code added by Sagar on 26/11/15
				xmlBuff.append("<chg_user><![CDATA[" + checkNull(chgUser)+ "]]></chg_user>");
				xmlBuff.append("<chg_date><![CDATA[" +  sdf.format(currentDate).toString() + "]]></chg_date>");
				xmlBuff.append("<chg_term><![CDATA[" + checkNull(chgTerm)+ "]]></chg_term>");
				xmlBuff.append("<site_code><![CDATA[" + checkNull(siteCode)+ "]]></site_code>");
				xmlBuff.append("<anal_code><![CDATA[]]></anal_code>");
				//xmlBuff.append("<bill_no><![CDATA[]]></bill_no>"); // Comment added by Sagar on 26/11/15
				xmlBuff.append("<bill_no><![CDATA["+purcOrder+"]]></bill_no>"); // Code added by Sagar on 26/11/15
				xmlBuff.append("<drcr_flag><![CDATA[D]]></drcr_flag>");
				xmlBuff.append("<tran_id__pay><![CDATA[]]></tran_id__pay>");

				xmlBuff.append("<confirmed><![CDATA[N]]></confirmed>");
				xmlBuff.append("<conf_date><![CDATA[]]></conf_date>");
				xmlBuff.append("<cr_term><![CDATA[]]></cr_term>");
				/*		if(dueDate1 == null)
			{
				xmlBuff.append("<due_date><![CDATA[]]></due_date>");
			}
			else
			{
				xmlBuff.append("<due_date><![CDATA[" + sdf.format(dueDate1) + "]]></due_date>");
			}
				 */		

				xmlBuff.append("<due_date><![CDATA[" + sdf.format(currentDate) + "]]></due_date>");
				xmlBuff.append("<emp_code__aprv><![CDATA[]]></emp_code__aprv>");
				xmlBuff.append("<amount__bc><![CDATA[]]></amount__bc>");
				//xmlBuff.append("<bill_date><![CDATA[]]></bill_date>"); // Comment added by Sagar on 26/11/15
				xmlBuff.append("<bill_date><![CDATA["+ordDateApp+"]]></bill_date>");  // Code added by Sagar on 26/11/15
				xmlBuff.append("<vouch_adj><![CDATA[]]></vouch_adj>");
				xmlBuff.append("<parent__tran_id><![CDATA[]]></parent__tran_id>");
				xmlBuff.append("<rev__tran><![CDATA[]]></rev__tran>");

				xmlBuff.append("</Detail1>");
				lineNo = 0;



				System.out.println("end of details 1");

				//			for (int itemCtr = 1; itemCtr <= cnt; itemCtr++)
				//			{
				lineNo++;
				System.out.println("lineNo is " + lineNo);
				// tempMap = (HashMap)tempList.get(itemCtr);

				System.out.println("start of details 2");

				xmlBuff.append("<Detail2 dbID='' domID=\"1\" objName=\"drcrpay_dr\" objContext=\"2\">");
				xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" updateFlag=\"A\" status=\"N\" />");
				xmlBuff.append("<tran_id/>");
				xmlBuff.append("<line_no>" + lineNo + "</line_no>");

				xmlBuff.append("<vouch_no><![CDATA[]]></vouch_no>");
				xmlBuff.append("<line_no__vou><![CDATA[]]></line_no__vou>");

				//	xmlBuff.append("<vouch_no><![CDATA["+vocDetMap.get("tran_id" + itemCtr)+"]]></vouch_no>");
				//	xmlBuff.append("<line_no__vou><![CDATA["+vocDetMap.get("line_no" + itemCtr)+"]]></line_no__vou>");
				//	xmlBuff.append("<vouch_no><![CDATA["+voucherId+"]]></vouch_no>");
				//	xmlBuff.append("<line_no__vou><![CDATA["+lineNo+"]]></line_no__vou>");
				xmlBuff.append("<item_code><![CDATA[]]></item_code>");
				//	xmlBuff.append("<tax_class><![CDATA["+vocDetMap.get("tax_class" + itemCtr)+"]]></tax_class>");
				//	xmlBuff.append("<tax_chap><![CDATA["+vocDetMap.get("tax_chap" + itemCtr)+"]]></tax_chap>");
				//	xmlBuff.append("<tax_env><![CDATA["+vocDetMap.get("tax_env" + itemCtr)+"]]></tax_env>");
				xmlBuff.append("<tax_class><![CDATA[]]></tax_class>");
				xmlBuff.append("<tax_chap><![CDATA[]]></tax_chap>");
				xmlBuff.append("<tax_env><![CDATA[]]></tax_env>");

				xmlBuff.append("<drcr_amt><![CDATA["+pentlyAmt+"]]></drcr_amt>");
				//	xmlBuff.append("<tax_amt><![CDATA["+vocDetMap.get("tax_amt" + itemCtr)+"]]></tax_amt>");
				xmlBuff.append("<tax_amt><![CDATA[0]]></tax_amt>");
				//	xmlBuff.append("<net_amt><![CDATA["+vocDetMap.get("amount" + itemCtr)+"]]></net_amt>");
				//	xmlBuff.append("<net_amt><![CDATA[0]]></net_amt>");
				xmlBuff.append("<net_amt><![CDATA["+pentlyAmt+"]]></net_amt>");
				xmlBuff.append("<reas_code><![CDATA[]]></reas_code>");
				xmlBuff.append("<acct_code><![CDATA["+acctCodeDr.trim()+"]]></acct_code>");
				xmlBuff.append("<cctr_code><![CDATA["+cctrCodeDr+"]]></cctr_code>");
				//	xmlBuff.append("<amount><![CDATA["+vocDetMap.get("amount" + itemCtr)+"]]></amount>");
				xmlBuff.append("<amount><![CDATA["+pentlyAmt+"]]></amount>");
				//			xmlBuff.append("<analysis3><![CDATA["+vocDetMap.get("analysis3" + itemCtr)+"]]></analysis3>");
				//			xmlBuff.append("<analysis2><![CDATA["+vocDetMap.get("analysis2" + itemCtr)+"]]></analysis2>");
				//			xmlBuff.append("<analysis1><![CDATA["+vocDetMap.get("analysis1" + itemCtr)+"]]></analysis1>");
				xmlBuff.append("</Detail2>");


				//}// end of for loop


				System.out.println("end of details 2");

				xmlBuff.append("</Header0>");
				xmlBuff.append("</group0>");
				xmlBuff.append("</DocumentRoot>");
				xmlString = xmlBuff.toString();
				System.out.println("@@@@@2: retString  :" + xmlString);
				//Changes and Commented By Ajay on 08-01-2018:START
                String userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
                System.out.println("--login code--"+userId);
				//errString = saveData(siteCode,xmlString,conn);
				errString = saveData(siteCode,xmlString,userId,conn);
				//Changes and Commented By Ajay on 08-01-2018:END
				System.out.println("Passed xml in  master State full saveData errString["+errString+"]");



				/*

				sql = " select EFF_DATE,SUPP_CODE,PURC_ORDER,CURR_CODE,EXCH_RATE ,ACCT_CODE,CCTR_CODE,BANK_CODE," +
						" DUE_DATE,SITE_CODE,FIN_ENTITY ,BILL_AMT,TAX_AMT,TOT_AMT,TAX_DATE,VOUCH_TYPE ," +
						" PROJ_CODE,NET_AMT from voucher where TRAN_ID = ?" ;

				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, voucherId);
				rs = pstmt.executeQuery();
				if (rs.next())
				{

					effDate = rs.getDate("EFF_DATE");
					suppCode = rs.getString("SUPP_CODE");
					purcOrder = rs.getString("PURC_ORDER");
					currCode = rs.getString("CURR_CODE");
					exchRate = rs.getDouble("EXCH_RATE");
					acctCode = rs.getString("ACCT_CODE");
					cctrCode = rs.getString("CCTR_CODE");
					bankCode = rs.getString("BANK_CODE");
					//	dueDate = rs.getDate("DUE_DATE");
					siteCode = rs.getString("SITE_CODE");
					finEntity = rs.getString("FIN_ENTITY");
					billAmt = rs.getDouble("BILL_AMT");
					taxAmt = rs.getDouble("TAX_AMT");
					totAmt = rs.getDouble("TOT_AMT");
					taxDate = rs.getDate("TAX_DATE");
					vouchType = rs.getString("VOUCH_TYPE");
					projCode = rs.getString("PROJ_CODE");
					netAMt = rs.getDouble("NET_AMT");



					suppCode=suppCode==null ? "" :suppCode.trim();
					purcOrder=purcOrder==null ? "" :purcOrder.trim();
					currCode=currCode==null ? "" :currCode.trim();
					acctCode=acctCode==null ? "" :acctCode.trim();
					cctrCode=cctrCode==null ? "" :cctrCode.trim();
					bankCode=bankCode==null ? "" :bankCode.trim();
					siteCode=siteCode==null ? "" :siteCode.trim();
					finEntity=finEntity==null ? "" :finEntity.trim();
					vouchType=vouchType==null ? "" :vouchType.trim();
					projCode=projCode==null ? "" :projCode.trim();



					System.out.println("effDate :"+effDate);
					System.out.println("suppCode :"+suppCode);
					System.out.println("purcOrder :"+purcOrder);
					System.out.println("finChrge :"+finChrge);
					System.out.println("fchgType :"+fchgType);
					System.out.println("currCode :"+currCode);
					System.out.println("exchRate :"+exchRate);
					System.out.println("acctCode :"+acctCode);
					System.out.println("cctrCode :"+cctrCode);
					System.out.println("bankCode :"+bankCode);
					System.out.println("dueDate :"+dueDate);
					System.out.println("siteCode :"+siteCode);
					System.out.println("finEntity :"+finEntity);
					System.out.println("netAMt :"+netAMt);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				sql = " select acct_code__dr,cctr_code__dr from porddet where purc_order= ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,purcOrder);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					acctCodeDr = rs.getString("acct_code__dr");
					cctrCodeDr = rs.getString("cctr_code__dr");

					acctCodeDr=acctCodeDr==null ? "" :acctCodeDr.trim();
					cctrCodeDr=cctrCodeDr==null ? "" :cctrCodeDr.trim();

					System.out.println("acctCodeDr :"+acctCodeDr);
					System.out.println("cctCodeDr :"+cctrCodeDr);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				sql = "select LINE_NO,ACCT_CODE,CCTR_CODE,AMOUNT,TAX_AMT,EXCH_RATE ,ANALYSIS1 " +
						" ,ANALYSIS2,ANALYSIS3,TAX_CLASS,TAX_CHEP,TAX_ENV,TAX_CHAP " +
						" from vouchdet where TRAN_ID= ? " ;

				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, voucherId);
				rs = pstmt.executeQuery();
				while(rs.next())
				{
					cnt++;
					lineNoDet = rs.getString("LINE_NO");
					acctCdDet = rs.getString("ACCT_CODE");
					cctrCdDet = rs.getString("CCTR_CODE");
					amtDet = rs.getDouble("AMOUNT");
					taxAmtDet = rs.getDouble("TAX_AMT");
					exchRateDet = rs.getString("exch_rate");
					analysisDet1 = rs.getString("analysis1");
					analysisDet2 = rs.getString("analysis2");
					analysisDet3 = rs.getString("analysis3");
					taxClassDet = rs.getString("tax_class");
					taxChepDet = rs.getString("tax_chep");
					taxEnvDet = rs.getString("tax_env");
					taxChapDet = rs.getString("tax_chap");

					lineNoDet=lineNoDet==null ? "" :lineNoDet.trim();
					acctCdDet=acctCdDet==null ? "" :acctCdDet.trim();
					cctrCdDet=cctrCdDet==null ? "" :cctrCdDet.trim();
					exchRateDet=exchRateDet==null ? "" :exchRateDet.trim();
					analysisDet1=analysisDet1==null ? "" :analysisDet1.trim();
					analysisDet2=analysisDet2==null ? "" :analysisDet2.trim();
					analysisDet3=analysisDet3==null ? "" :analysisDet3.trim();
					taxClassDet=taxClassDet==null ? "" :taxClassDet.trim();
					taxChapDet=taxChapDet==null ? "" :taxChapDet.trim();
					taxChepDet=taxChepDet==null ? "" :taxChepDet.trim();
					taxEnvDet=taxEnvDet==null ? "" :taxEnvDet.trim();
					System.out.println("taxEnvDet :"+taxEnvDet);

					System.out.println("lineNoDet :"+lineNoDet);
					System.out.println("acctCdDet :"+acctCdDet);
					System.out.println("cctrCdDet :"+cctrCdDet);
					System.out.println("exchRateDet :"+exchRateDet);
					System.out.println("analysisDet1 :"+analysisDet1);
					System.out.println("analysisDet2 :"+analysisDet2);
					System.out.println("analysisDet3 :"+analysisDet3);
					System.out.println("taxClassDet :"+taxClassDet);
					System.out.println("taxChapDet :"+taxChapDet);
					System.out.println("analysisDet2 :"+analysisDet2);

					vocDetMap.put("line_no"+cnt, lineNoDet);
					vocDetMap.put("acct_code"+cnt, acctCdDet);
					vocDetMap.put("cctr_code"+cnt, cctrCdDet);
					vocDetMap.put("amount"+cnt, String.valueOf(amtDet));
					vocDetMap.put("tax_amt"+cnt, String.valueOf(taxAmtDet));
					vocDetMap.put("exch_rate"+cnt, exchRateDet);
					vocDetMap.put("analysis1"+cnt, analysisDet1);
					vocDetMap.put("analysis2"+cnt, analysisDet2);
					vocDetMap.put("analysis3"+cnt, analysisDet3);
					vocDetMap.put("tax_class"+cnt, taxClassDet);
					vocDetMap.put("tax_chap"+cnt, taxChapDet);
					vocDetMap.put("tax_chep"+cnt, taxChepDet);
					vocDetMap.put("tax_env"+cnt, taxEnvDet);

				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;




				tranSer="DRNPAY";

				siteCode = siteCode == null ? "" : siteCode.trim();

				// create xml for track information
				xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
				xmlBuff.append("<DocumentRoot>");
				xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
				xmlBuff.append("<group0>");
				xmlBuff.append("<description>").append("Group0 description").append("</description>");
				xmlBuff.append("<Header0>");
				xmlBuff.append("<objName><![CDATA[").append("drcrpay_dr").append("]]></objName>");
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

				System.out.println("details 1 start!!!!!");

				xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"drcrpay_dr\" objContext=\"1\">");
				xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
				xmlBuff.append("<tran_id/>");
				xmlBuff.append("<tran_date><![CDATA["+ sdf.format(currentDate).toString() + "]]></tran_date>");
				xmlBuff.append("<tran_ser><![CDATA[" + tranSer.trim()+ "]]></tran_ser>");
				xmlBuff.append("<fin_entity><![CDATA[" + finEntity.trim()+ "]]></fin_entity>");
				xmlBuff.append("<supp_code><![CDATA[" + suppCode.trim()+ "]]></supp_code>");
				xmlBuff.append("<acct_code><![CDATA[" + acctCode.trim()+ "]]></acct_code>");
				xmlBuff.append("<cctr_code><![CDATA[" + cctrCode.trim()+ "]]></cctr_code>");
				if(effDate == null)
				{
					xmlBuff.append("<eff_date><![CDATA[]]></eff_date>");			
				}
				else
				{
					xmlBuff.append("<eff_date><![CDATA[" + sdf.format(effDate)+ "]]></eff_date>");
				}

				xmlBuff.append("<vouch_ser><![CDATA[]]></vouch_ser>");
				xmlBuff.append("<vouch_no><![CDATA[" + checkNull(voucherId)+ "]]></vouch_no>");
				xmlBuff.append("<amount><![CDATA[" + pentlyAmt + "]]></amount>");
				xmlBuff.append("<curr_code><![CDATA[" + checkNull(currCode)+ "]]></curr_code>");
				xmlBuff.append("<exch_rate><![CDATA[" + exchRate+ "]]></exch_rate>");
				xmlBuff.append("<remarks><![CDATA[]]></remarks>");
				xmlBuff.append("<chg_user><![CDATA[" + checkNull(chgUser)+ "]]></chg_user>");
				xmlBuff.append("<chg_date><![CDATA[" +  sdf.format(currentDate).toString() + "]]></chg_date>");
				xmlBuff.append("<chg_term><![CDATA[" + checkNull(chgTerm)+ "]]></chg_term>");
				xmlBuff.append("<site_code><![CDATA[" + checkNull(siteCode)+ "]]></site_code>");
				xmlBuff.append("<anal_code><![CDATA[]]></anal_code>");
				xmlBuff.append("<bill_no><![CDATA[]]></bill_no>");
				xmlBuff.append("<drcr_flag><![CDATA[D]]></drcr_flag>");
				xmlBuff.append("<tran_id__pay><![CDATA[]]></tran_id__pay>");

				xmlBuff.append("<confirmed><![CDATA[N]]></confirmed>");
				xmlBuff.append("<conf_date><![CDATA[]]></conf_date>");
				xmlBuff.append("<cr_term><![CDATA[]]></cr_term>");
						if(dueDate1 == null)
			{
				xmlBuff.append("<due_date><![CDATA[]]></due_date>");
			}
			else
			{
				xmlBuff.append("<due_date><![CDATA[" + sdf.format(dueDate1) + "]]></due_date>");
			}


				xmlBuff.append("<due_date><![CDATA[" + sdf.format(currentDate) + "]]></due_date>");
				xmlBuff.append("<emp_code__aprv><![CDATA[]]></emp_code__aprv>");
				xmlBuff.append("<amount__bc><![CDATA[]]></amount__bc>");
				xmlBuff.append("<bill_date><![CDATA[]]></bill_date>");
				xmlBuff.append("<vouch_adj><![CDATA[]]></vouch_adj>");
				xmlBuff.append("<parent__tran_id><![CDATA[]]></parent__tran_id>");
				xmlBuff.append("<rev__tran><![CDATA[]]></rev__tran>");

				xmlBuff.append("</Detail1>");
				lineNo = 0;



				System.out.println("end of details 1");

				for (int itemCtr = 1; itemCtr <= cnt; itemCtr++)
				{
					lineNo++;
					System.out.println("lineNo is " + lineNo);
					// tempMap = (HashMap)tempList.get(itemCtr);

					System.out.println("start of details 2");

					xmlBuff.append("<Detail2 dbID='' domID=\"1\" objName=\"drcrpay_dr\" objContext=\"2\">");
					xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" updateFlag=\"A\" status=\"N\" />");
					xmlBuff.append("<tran_id/>");
					xmlBuff.append("<line_no>" + lineNo + "</line_no>");


					xmlBuff.append("<vouch_no><![CDATA["+voucherId+"]]></vouch_no>");
					xmlBuff.append("<line_no__vou><![CDATA["+vocDetMap.get("line_no" + itemCtr)+"]]></line_no__vou>");
					xmlBuff.append("<item_code><![CDATA[]]></item_code>");
					xmlBuff.append("<tax_class><![CDATA["+vocDetMap.get("tax_class" + itemCtr)+"]]></tax_class>");
					xmlBuff.append("<tax_chap><![CDATA["+vocDetMap.get("tax_chap" + itemCtr)+"]]></tax_chap>");
					xmlBuff.append("<tax_env><![CDATA["+vocDetMap.get("tax_env" + itemCtr)+"]]></tax_env>");
					xmlBuff.append("<drcr_amt><![CDATA["+pentlyAmt+"]]></drcr_amt>");
					xmlBuff.append("<tax_amt><![CDATA["+vocDetMap.get("tax_amt" + itemCtr)+"]]></tax_amt>");
					xmlBuff.append("<net_amt><![CDATA["+pentlyAmt+"]]></net_amt>");
					xmlBuff.append("<reas_code><![CDATA[]]></reas_code>");
					xmlBuff.append("<acct_code><![CDATA["+acctCodeDr.trim()+"]]></acct_code>");
					xmlBuff.append("<cctr_code><![CDATA["+cctrCodeDr.trim()+"]]></cctr_code>");
					xmlBuff.append("<amount><![CDATA["+pentlyAmt+"]]></amount>");
					xmlBuff.append("<analysis3><![CDATA["+vocDetMap.get("analysis3" + itemCtr)+"]]></analysis3>");
					xmlBuff.append("<analysis2><![CDATA["+vocDetMap.get("analysis2" + itemCtr)+"]]></analysis2>");
					xmlBuff.append("<analysis1><![CDATA["+vocDetMap.get("analysis1" + itemCtr)+"]]></analysis1>");
					xmlBuff.append("</Detail2>");

				}// end of for loop


				System.out.println("end of details 2");

				xmlBuff.append("</Header0>");
				xmlBuff.append("</group0>");
				xmlBuff.append("</DocumentRoot>");
				xmlString = xmlBuff.toString();
				System.out.println("@@@@@2: retString  :" + xmlString);

				errString = saveData(siteCode,xmlString,conn);

				System.out.println("Passed xml in  master State full saveData errString["+errString+"]");

				 */
			}else
			{
				errString = itmDBAccessLocal.getErrorString("", "VTTRINERR", "","",conn);
			}

		} catch (Exception e)
		{

			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			errString = itmDBAccessLocal.getErrorString("", "VTTRINERR", "","",conn);
			return errString;

		}
		finally
		{
			try
			{
				if(rs != null)
				{
					rs.close();
				}
				if(pstmt != null)
				{
					pstmt.close();
				}
				/*if(conn != null)
				{
					conn.close();
				}*/
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

		}

		return errString;
	}


	public String confirmTranscation(String businessObj, String tranIdFr,String xtraParams, Connection conn) throws ITMException
	{
		String methodName = "";
		String compName = "";
		String retString = "";
		String serviceCode = "";
		String serviceURI = "";
		String actionURI = "";
		String sql = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		System.out.println("confirmVoucher(String businessObj, String tranIdFr,String xtraParams, String forcedFlag, Connection conn) called >>><!@#>");

		try
		{
			//ConnDriver connDriver = new ConnDriver();
			//conn = connDriver.getConnectDB("DriverITM");

			methodName = "gbf_post";
			actionURI = "http://NvoServiceurl.org/" + methodName;

			sql = "SELECT SERVICE_CODE,COMP_NAME FROM SYSTEM_EVENTS WHERE OBJ_NAME = ? AND EVENT_CODE = 'pre_confirm' ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,businessObj);
			rs = pstmt.executeQuery();
			if ( rs.next() )
			{
				serviceCode = rs.getString("SERVICE_CODE");
				compName = rs.getString("COMP_NAME");
			}
			System.out.println("serviceCode = "+serviceCode+" compName "+compName);
			// Changed by Manish on 01/04/16 for max cursor issue [start]
			if (pstmt != null)
			{
				pstmt.close();
				pstmt=null;
			}
			if (rs !=null)
			{
				rs.close();
				rs=null;
			}
			// Changed by Manish on 01/04/16 for max cursor issue [end]
			sql = "SELECT SERVICE_URI FROM SYSTEM_EVENT_SERVICES WHERE SERVICE_CODE = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,serviceCode);
			rs = pstmt.executeQuery();
			if ( rs.next() )
			{
				serviceURI = rs.getString("SERVICE_URI");
			}
			System.out.println("serviceURI = "+serviceURI+" compName = "+compName);
			// Changed by Manish on 01/04/16 for max cursor issue [start]
			if (pstmt != null)
			{
				pstmt.close();
				pstmt=null;
			}
			if (rs !=null)
			{
				rs.close();
				rs=null;
			}
			// Changed by Manish on 01/04/16 for max cursor issue [end]
			Service service = new Service();
			Call call = (Call)service.createCall();
			call.setTargetEndpointAddress(new java.net.URL(serviceURI));
			call.setOperationName( new javax.xml.namespace.QName("http://NvoServiceurl.org", methodName ) );
			call.setUseSOAPAction(true);
			call.setSOAPActionURI(actionURI);
			Object[] aobj = new Object[4];

			call.addParameter( new javax.xml.namespace.QName("http://NvoServiceurl.org", "component_name"), XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter( new javax.xml.namespace.QName("http://NvoServiceurl.org", "tran_id"), XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter( new javax.xml.namespace.QName("http://NvoServiceurl.org", "xtra_params"), XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter( new javax.xml.namespace.QName("http://NvoServiceurl.org", "forced_flag"), XMLType.XSD_STRING, ParameterMode.IN);

			aobj[0] = new String(compName);
			aobj[1] = new String(tranIdFr);
			aobj[2] = new String(xtraParams);
			System.out.println("aobj 0 :"+aobj[0]);
			System.out.println("aobj 1 :"+aobj[1]);
			System.out.println("aobj 2 :"+aobj[2]);
			//aobj[3] = new String(forcedFlag);
			//System.out.println("@@@@@@@@@@loginEmpCode:" +genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode")+":");
			System.out.println("@@@@@@ call.setReturnType(XMLType.XSD_STRING) executed........");
			call.setReturnType(XMLType.XSD_STRING);
			retString = (String)call.invoke(aobj);
			System.out.println("Confirm Complete @@@@@@@@@@@ Return string from NVO is:==>["+retString+"]");

		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{		
			try{


				if (pstmt != null )
				{
					pstmt.close();
					pstmt = null;
				}
				if (rs !=null)
				{
					rs.close();
					rs=null;
				}
				/*if( conn != null ){
					conn.close();
					conn = null;
				}*/
			}
			catch(Exception e)
			{
				System.out.println("Exception inCalling confirmed");
				e.printStackTrace();
				try{
					conn.rollback();

				}catch (Exception s)
				{
					System.out.println("Unable to rollback");
					s.printStackTrace();
				}
				throw new ITMException(e);
			}
		}
		return retString;
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
			authencate[0] = userId;
			authencate[1] = "";
			System.out.println("xmlString to masterstateful [" + xmlString + "]");
			retString = masterStateful.processRequest(authencate, siteCode, true, xmlString,true,conn);
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




	private String generateTranId( String windowName, String siteCode, Connection conn )throws ITMException
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String selSql = "";
		String tranId = "";
		String tranSer = "";
		String keyString = "";
		String keyCol = "";
		String xmlValues = "";
		java.sql.Timestamp currDate = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility = new E12GenericUtility();

		try
		{

			SimpleDateFormat sdfAppl = new SimpleDateFormat(genericUtility.getApplDateFormat());

			currDate = new java.sql.Timestamp(System.currentTimeMillis());
			String currDateStr = sdfAppl.format(currDate);

			selSql = "SELECT KEY_STRING, TRAN_ID_COL, REF_SER FROM TRANSETUP WHERE TRAN_WINDOW = ? ";
			pstmt = conn.prepareStatement(selSql);
			pstmt.setString( 1, windowName );
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				keyString = rs.getString("KEY_STRING");
				keyCol = rs.getString("TRAN_ID_COL");
				tranSer = rs.getString("REF_SER");
			}
			rs.close();rs = null;
			pstmt.close();pstmt = null;
			System.out.println("keyString :"+keyString);
			System.out.println("keyCol :"+keyCol);
			System.out.println("tranSer :"+tranSer);

			xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
			xmlValues = xmlValues + "<Header></Header>";
			xmlValues = xmlValues + "<Detail1>";
			xmlValues = xmlValues +        "<tran_id></tran_id>";
			xmlValues = xmlValues +        "<site_code>" + siteCode + "</site_code>";
			xmlValues = xmlValues +        "<tran_date>" + currDateStr + "</tran_date>";
			xmlValues = xmlValues + "</Detail1></Root>";
			System.out.println("xmlValues  :["+xmlValues+"]");
			TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", CommonConstants.DB_NAME);
			tranId = tg.generateTranSeqID(tranSer, keyCol, keyString, conn);
			System.out.println("tranId :"+tranId);
		}
		catch (SQLException ex)
		{
			System.out.println("Exception ::" +selSql+ ex.getMessage() + ":");
			ex.printStackTrace();
			throw new ITMException(ex);
		}
		catch (Exception e)
		{
			System.out.println("Exception ::" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
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
			catch(Exception e){}
		}
		return tranId;
	}//generateTranTd()

	private String checkNull(String input)
	{
		if (input == null)
		{
			input = "";
		}
		return input;
	}

	public double timeDiff(Timestamp d1, Timestamp d2)throws RemoteException,ITMException
	{
		long t1 = d1.getTime();
		long t2 = d2.getTime();
		long t3 = t1 - t2;
		double hr  = (double)((double)t3/(60.00f*1000.00f*60.00f));
		double min = ((int)((hr - (int)hr) * 60))/100.00f;
		double time = (int)(hr) + min;
		return time;
	}


}
