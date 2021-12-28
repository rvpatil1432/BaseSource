package ibase.webitm.ejb.dis;

import ibase.system.config.AppConnectParm;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.MasterStatefulLocal;
import ibase.webitm.ejb.dis.adv.SorderConf;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.sys.UtilMethods;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import javax.naming.InitialContext;

/**
 * @author VALLABH KADAM
 * To generate Invoice from dispatch 
 */

public class PostOrdInvoiceGen extends ActionHandlerEJB
{
	DistCommon distCommon= new DistCommon();
	FinCommon finCommon=new FinCommon();
	String postUpto="";	
	E12GenericUtility genericUtility=new E12GenericUtility();
	FileOutputStream fos1 = null;
	Calendar calendar = Calendar.getInstance();
	java.util.Date startDate = new java.util.Date(System.currentTimeMillis());
	String startDateStr = null;

	/**
	 * 
	 * @param conn
	 * @return String
	 * @throws ITMException
	 * @throws Exception
	 */
	public String invoiceProcess(String salesOrdFrm,String salesOrdTo,String custCodeFrm,String custCodeTo,
			String descpIdFrm,String orderType,Timestamp frmDateStr,String clubOrder,String clubPendOrder,
			String adjDrCr,String adjCustAdv,String advAdjMode,String adjNewPrdInv,String xtraParams,Connection conn) throws ITMException, Exception
			{
		String retString="",frmDate="",frmCustCode="",currCode="",neworderType="",lrNo="",lrDate="",remarks="",sysDate="",toDateStr="";
		String toCustCode="",stanCodeInit="",descpIdTo="";
		String runOpt="*";
		boolean lbClubOrders=false,lbClub=false,lbDrCr=false,lbAdv=false,lbCustomer=false,lbNewPrdInv=false,tempflag=false;
		String sql="",	currCodefrtBase = "",siteCode = "";

		PreparedStatement pstmt=null;
		ResultSet rs=null;
		double exchangeRate=0.0;
		Timestamp tranDatetTStmp=null;
		HashMap<String, String> ldsInvHdrMp=new HashMap<String, String>();
		/**
		 * Select postUpto from table 'disparam'
		 * where var_name='POST_SORDER_UPTO'
		 * */
		//postUpto=checknull(distCommon.getDisparams("999999", "POST_SORDER_UPTO", conn));

		/**
		 * Intializing Log
		 * */
		//System.out.println("Intializing Log $$$$$..."+ intializingLog("Post_Ord_InvGen_log"));
		long startTime = 0, endTime = 0, totalTime = 0, totalHrs = 0, totlMts = 0, totSecs = 0; // Added
		try
		{
			startTime = System.currentTimeMillis();
			java.util.Date today = new java.util.Date();
			Calendar cal = Calendar.getInstance();
			cal.setTime(today);
			today = cal.getTime();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			sysDate = sdf.format(today);
			//System.out.println("System date  :- [" + sysDate + "]");

			SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getApplDateFormat());
			startDate = new java.util.Date(System.currentTimeMillis());
			calendar.setTime(new java.sql.Timestamp(System.currentTimeMillis()));
			startDateStr = sdf1.format(startDate)+" "+calendar.get(Calendar.HOUR)+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND);
			//fos1.write(("Invoice Genertion Started At " + startDateStr +"\r\n").getBytes());
			//System.out.println("postUpto::::::::::::::::::::::"+postUpto);
			/**
			 * Check postUpto is not 'D'
			 * */
			/*added by vishakha*/
			SimpleDateFormat dateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String fromDate  = dateFormat.format(frmDateStr);

			//System.out.println("@@@@@@@@@@@98::::::::Form Date["+fromDate+"]frmDateStr["+frmDateStr+"]postUpto["+postUpto+"]");

		/*	if(postUpto.equalsIgnoreCase("D"))
			{
				//System.out.println("@@@@@@@@@.........conn.commit()................");
				conn.commit();			
			}
			else
			{*/
				descpIdTo=descpIdFrm;

				//	        	frmDateStr="";//ld_from_date

/*
				if(orderType==null || orderType.trim().length()==0)
				{
					orderType="%";
				}
				else
				{*/
                /* changed by kaustubh on 6 nov 2017 as per told by pragyan sir added new else in below code  */
					if(orderType==null || orderType.trim().length()==0)
					{
						orderType="%";
					}
					else
					{
						orderType =orderType.substring(0,1)+"%";
						System.out.println("orderType::"+orderType);
					}	
					
				//	System.out.println("Insidingggg else part of order type:::::::::::"+orderType);
					/**
					 * select desp_date,cust_code, curr_code,
					 *  lr_no, lr_date, remarks from table 'despatch'
					 * */
					sql="select desp_date, cust_code, curr_code, lr_no, lr_date, remarks,exch_rate, site_code from despatch where desp_id=?";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, descpIdFrm);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						frmDate=checknull(rs.getString("desp_date"));
						frmCustCode=checknull(rs.getString("cust_code"));
						currCode=checknull(rs.getString("curr_code"));
						lrNo=checknull(rs.getString("lr_no"));
						//lrDate=checknull(rs.getString("lr_date"));
						remarks=checknull(rs.getString("remarks"));
						remarks=checknull(rs.getString("remarks"));
						exchangeRate=rs.getDouble("exch_rate");
						tranDatetTStmp=rs.getTimestamp("lr_date");
						siteCode = checknull(rs.getString("site_code"));
					}
					pstmt.close();
					pstmt=null;
					rs.close();
					rs=null;

					toDateStr=frmDate;
					toCustCode=frmCustCode;
					if(tranDatetTStmp != null)
					{
						lrDate=sdf.format(tranDatetTStmp).toString();
					}else
					{
						lrDate="";
					}
				//	System.out.println("lrDate>>>"+lrDate);
					/**
					 * Check lrDate is not null
					 * */
					if(lrDate!=null && lrDate.trim().length()>0)
					{
						exchangeRate=0.0;

						/**
						 * select exch_rate__sell 
						 * from table 'daily_exch_rate_sell_buy'
						 * for lrDate in between from_date and to_date
						 * and curr_code,curr_code__to='RS'
						 * */
						 /* 28-Nov-16 manoharan
						sql="select exch_rate__sell from daily_exch_rate_sell_buy where  ? between from_date and to_date"
							+ " and    curr_code = ? and    curr_code__to = 'RS'";
						pstmt=conn.prepareStatement(sql);
						pstmt.setTimestamp(1, tranDatetTStmp);
						pstmt.setString(2, currCode);
						rs=pstmt.executeQuery();
						if(rs.next())
						{
							exchangeRate=rs.getDouble("exch_rate__sell");
						}
						pstmt.close();
						pstmt=null;
						rs.close();
						rs=null;	        	
						*/						
						exchangeRate = finCommon.getDailyExchRateSellBuy(currCode, currCodefrtBase, siteCode,	lrDate, "S", conn);

					}
					//System.out.println("@@@@@@lrDate["+lrDate+"]fromDate["+fromDate+"]");
					ldsInvHdrMp.put("so_type", orderType);
					ldsInvHdrMp.put("desp_id__fr", descpIdFrm);
					ldsInvHdrMp.put("desp_id__to", descpIdTo);
					ldsInvHdrMp.put("desp_date__fr", ""+fromDate);
					ldsInvHdrMp.put("desp_date__to", toDateStr);
					ldsInvHdrMp.put("cust_code__fr", frmCustCode);
					ldsInvHdrMp.put("cust_code__to", toCustCode);
					ldsInvHdrMp.put("invoice__date", ""+fromDate);
					ldsInvHdrMp.put("lr_date", lrDate);
					ldsInvHdrMp.put("lr_no", lrNo);
					ldsInvHdrMp.put("remarks", remarks);


					ldsInvHdrMp.put("curr_code", currCode);
					ldsInvHdrMp.put("exch_rate", String.valueOf(exchangeRate));

					sql="select stan_code__init from   sorder where  sale_order =?";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, salesOrdFrm);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						stanCodeInit=checknull(rs.getString("stan_code__init"));
					}
					pstmt.close();
					pstmt=null;
					rs.close();
					rs=null;

					ldsInvHdrMp.put("stan_code__init", stanCodeInit);

					if(clubOrder.equalsIgnoreCase("Y"))
					{
						lbClubOrders=true;
					}
					else
					{
						lbClubOrders=false;
					}
					if(clubPendOrder.equalsIgnoreCase("Y"))
					{
						lbClub=true;
					}
					else
					{
						lbClub=false;
					}
					if(adjDrCr.equalsIgnoreCase("Y"))
					{
						lbDrCr=true;
					}
					else
					{
						lbDrCr=false;
					}
					if(adjCustAdv.equalsIgnoreCase("Y"))
					{
						lbAdv=true;
					}
					else
					{
						lbAdv=false;
					}
					if(advAdjMode.equalsIgnoreCase("Y"))
					{
						lbCustomer=true;
					}
					else
					{
						lbCustomer=false;
					}
					if(adjNewPrdInv.equalsIgnoreCase("Y"))
					{
						lbNewPrdInv=true;
					}
					else
					{
						lbNewPrdInv=false;
					}

					tempflag=true;
					/**
					 * Create Invoice header
					 * */				

					//System.out.println("@@@@@@@@@@260:::::ldsInvHdrMp["+ldsInvHdrMp+"]");
					//Changed By Nasruddin Start 04-11-16
					long startTime2 = System.currentTimeMillis();
					retString=createInvoice(ldsInvHdrMp,lbClub,lbDrCr,lbAdv,tempflag,runOpt,lbCustomer,lbNewPrdInv,xtraParams,conn);	// Missing 'lds_credit_check'
					long endTime2 = System.currentTimeMillis();
					System.out.println("DIFFERANCE IN TIME Creating INVoice IN SECONDS INSIDE POST ORDER ACTIVITY:::["+(endTime2-startTime2)/1000+"]");
					//Changed By Nasruddin END 04-11-16
					//fos1.write(("\n \n Final Return String :- [" + retString +"]\r\n").getBytes());

				//}

			//}
			endTime = System.currentTimeMillis();
			totalTime = endTime - startTime;

			totSecs = (int) (((double) 1 / 1000) * (totalTime));
			totalHrs = (int) (totSecs / 3600);
			totlMts = (int) (((totSecs - (totalHrs * 3600)) / 60));
			totSecs = (int) (totSecs - ((totalHrs * 3600) + (totlMts * 60)));

			System.out.println("Total Time Spend invoice generation [" + totalHrs + "] Hours [" + totlMts + "] Minutes [" + totSecs + "] seconds");
			
		} catch (SQLException e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage()); 
			throw new ITMException(e); 
		}
		//Modified by Anjali R. on [01/02/2018][Added catch block for all exceptions][Start]
		catch (Exception e1)
		{
			e1.printStackTrace();
			System.out.println("Exception :invoiceProcess:"+ e1.getMessage()); 
			throw new ITMException(e1); 
		}
		//Modified by Anjali R. on [01/02/2018][Added catch block for all exceptions][End]
		finally
		{
			try
			{
				if(fos1!=null)
				{
					fos1.close();
					fos1=null;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				System.out.println("Exception ::"+ e.getMessage()); 
				throw new ITMException(e); 
			}
		}

		return retString;
			}

	/**
	 * 
	 * @param ldsInvHdrMp
	 * @param lbClub
	 * @param lbDrCr
	 * @param lbAdv
	 * @param tempflag
	 * @param runOpt
	 * @param lbCustomer
	 * @param lbNewPrdInv
	 * @param conn
	 * @return String
	 * @throws ITMException 
	 */
	private String createInvoice(HashMap<String, String> ldsInvHdrMp, boolean lbClub, boolean lbDrCr, boolean lbAdv, boolean tempflag,
			String runOpt, boolean lbCustomer, boolean lbNewPrdInv,String xtraParams, Connection conn) throws ITMException
			{		
		// TODO Auto-generated method stub
		StringBuffer xmlBuff=new StringBuffer();
		//		HashMap<String, HashMap<String, String>> mainInvDespMap=new HashMap<String, HashMap<String, String>>();
		//		HashMap<String, HashMap<String, String>> mainInvDespDetMap=new HashMap<String, HashMap<String, String>>();
		HashMap<String, HashMap> mainInvDespDetMap=new HashMap<String, HashMap>();
		HashMap<String, String> invDespMap=new HashMap<String, String>();
		HashMap<String, Object> invDetBeanMap= new HashMap<String, Object>();
		//		HashMap<String, String> invDespDetMap=null;
		HashMap<String, String> invTraceDetMap=null;
		HashMap<String, String> acctSchemehdrMap=null;
		HashMap<String, String> sorderSalMap=null;
		HashMap<String, String> disItemserMap=null;
		HashMap<String, String> arCustomerMap=null;
		
		String sql="";
		PreparedStatement pstmt=null, pstmt1=null,pstmt2=null;
		ResultSet rs=null,rs2=null;

		String loginEmpCode = null,itemDescr="";
		String lc_ratestd="0",lc_cstrate="0";
		String round="",roundInvTo="", discAmtStr = "";
		double roundTo=0.0,netamt=0.0;
		double lc_disc_amt=0.00,ld_discount=0.00;
		double totalCommSp1=0.0,totalCommSp2=0.0,totalCommSp3=0.0,totalCommAmt=0.0;
		String despIdKey="",despId="",sysDate="",salesInvPstHdr="",despLineNo="",userId="",termId="",loginSite="",tranId="";
		String xmlInvString="",retString="", taxDateStr="";
		double lcCommAmtOc=0.0,tcommAmt=0.0,lcExchRate=0.0;
		int lineNo=0,lineNoDet=0;
		Timestamp dueDate=null,sysDateTs=null;
		String acctCodeDis="",cctrCodeDis="",acctCodeAr="",cctrCodeAr = "",acctCodePr="",cctrCodePr="",marketReg = "",mfgDateStr="",expDateStr="";
		double netAmt=0,taxAmt=0,commAmt=0,discAmt=0,invAmt=0;
		double ld_tot_amt=0.0,ld_disc_amt=0.0,ld_net_amt=0.0;
		HashMap commissionMap = null;
		/**
		 * Generate Bean object
		 * */
		InvoiceDetBean invoiceDetBean= null;
		InitialContext ctx = null;
		try
		{			
			/*UserInfoBean userInfo=new UserInfoBean(); 
			userId=userInfo.getLoginCode();
			termId=userInfo.getRemoteHost();
			loginSite=userInfo.getSiteCode();
			xtraParams = "loginCode="+userInfo.getLoginCode()+"~~termId="+userInfo.getRemoteHost()+"~~loginSiteCode="+userInfo.getSiteCode()+"~~loginEmpCode="+userInfo.getEmpCode();
*/
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			loginEmpCode =(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode"));
			termId =(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId"));
			loginSite=(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode"));
			SorderConf sordcnf=new SorderConf();
			java.util.Date today = new java.util.Date();
			Calendar cal = Calendar.getInstance();
			cal.setTime(today);
			today = cal.getTime();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			sysDate = sdf.format(today);
			//System.out.println("System date  :- [" + sysDate + "]");

			//			mainInvDespMap=getInvDespatch(ldsInvHdrMp,conn);
			invDespMap=getInvDespatch(ldsInvHdrMp,conn);
			//System.out.println("@@@@@@@@@invDespMap.size():-["+invDespMap.size()+"]invDespMap["+invDespMap+"]");				

			salesInvPstHdr=checknull(finCommon.getFinparams("999999", "SALES_INV_POST_HDR", conn));			

			/**
			 * Iterate MainInvDeapMap
			 * to get inner values
			 * */
			//			for(String key:mainInvDespMap.keySet())
			//			{
			//				despIdKey=key;

			//				invDespMap=new HashMap<String, String>();
			//				invDespMap=mainInvDespMap.get(despIdKey);
			//				System.out.println("Inner InvDespMap size:-["+invDespMap.size()+"]");

			despIdKey=invDespMap.get("desp_id");				
			//fos1.write(("\n Invoice in process for Dispatch Id :- [" + despIdKey +"]\r\n").getBytes());

			/**
			 * get dispat_det main map
			 * */
			ArrayList <Object>dataList= new ArrayList<Object>();

			dataList=getInvDespatchDet(invDespMap.get("sord_no"),invDespMap.get("status"),despIdKey,ldsInvHdrMp,
					invDespMap.get("desp_site_code"),conn);
			//				System.out.println("MainInvDespDetMap size :- ["+mainInvDespDetMap.size()+"]");

			//System.out.println("@@@@@@@@dataList.size:::["+dataList.size()+"]::::::::dataList["+dataList+"]");
			//System.out.println("@@@@@@@@:::::::ldsInvHdrMp[["+ldsInvHdrMp+"]]::::::::invDespMap["+invDespMap+"]");
			/**
			 * Generate XML string 
			 * for invoice header
			 * */

			xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?><DocumentRoot>");
			xmlBuff.append("<description>Datawindow Root</description>");
			xmlBuff.append("<group0>");
			xmlBuff.append("<description>Group0 description</description>");
			xmlBuff.append("<Header0>");
			xmlBuff.append("<objName><![CDATA[").append("invoice_po").append("]]></objName>");		
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
			xmlBuff.append("<description>Header0 members</description>");

			xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objContext=\"1\" objName=\"invoice_po\">");		
			xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" status=\"N\" updateFlag=\"A\"/>");
			xmlBuff.append("<invoice_id/>");

			if(ldsInvHdrMp.get("lr_date")==null || ldsInvHdrMp.get("lr_date").trim().length()==0)
			{
				xmlBuff.append("<tran_date><![CDATA[" + ldsInvHdrMp.get("invoice__date") + "]]></tran_date>");
				xmlBuff.append("<eff_date><![CDATA[" + ldsInvHdrMp.get("invoice__date") + "]]></eff_date>");
				taxDateStr = ldsInvHdrMp.get("invoice__date");
			}
			else
			{
				xmlBuff.append("<tran_date><![CDATA[" + ldsInvHdrMp.get("lr_date") + "]]></tran_date>");
				xmlBuff.append("<eff_date><![CDATA[" + ldsInvHdrMp.get("lr_date") + "]]></eff_date>");
				taxDateStr = ldsInvHdrMp.get("lr_date");
			}

			xmlBuff.append("<fin_entity><![CDATA[" +invDespMap.get("fin_entity")+ "]]></fin_entity>");		//fin_entity required 'site_fin_entity'
			xmlBuff.append("<sales_grp><![CDATA[" +""+ "]]></sales_grp>");			//sales_grp required 'sales_grp'

			acctSchemehdrMap= new HashMap<String, String>();
			acctSchemehdrMap=getAcctSchemehdr(invDespMap.get("sord_no"),conn);
			//System.out.println("AcctSchemehdrMap size :- ["+acctSchemehdrMap.size()+"]");
			xmlBuff.append("<acc_code__order><![CDATA[" +acctSchemehdrMap.get("ls_acct_schemehdr")+ "]]></acc_code__order>");

			xmlBuff.append("<tran_mode><![CDATA[" +"S"+ "]]></tran_mode>");
			//Modified by Anjali R. on [29/01/2019][set trans_mode from despatch ,if tran_mode get null or blank then get it from sale order][Start]
			String transMode = invDespMap.get("trans_mode"); 
			String saleOrder = invDespMap.get("sord_no");
			if(transMode == null || transMode.trim().length() == 0)
			{
				if(saleOrder != null && saleOrder.trim().length() > 0)
				{
					sql = "select trans_mode from sorder where sale_order = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, saleOrder);
					rs = pstmt.executeQuery();
					while(rs.next())
					{
						transMode = checknull(rs.getString("trans_mode"));
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
			}
			xmlBuff.append("<trans_mode><![CDATA[" +transMode+ "]]></trans_mode>");
			//Modified by Anjali R. on [29/01/2019][set trans_mode from despatch ,if tran_mode get null or blank then get it from sale order][End]
			xmlBuff.append("<desp_id><![CDATA[" +invDespMap.get("desp_id")+ "]]></desp_id>");
			xmlBuff.append("<desp_date><![CDATA[" +invDespMap.get("desp_date")+ "]]></desp_date>");
			xmlBuff.append("<sale_order><![CDATA[" +invDespMap.get("sord_no")+ "]]></sale_order>");
			xmlBuff.append("<site_code><![CDATA[" +invDespMap.get("desp_site_code")+ "]]></site_code>");
			xmlBuff.append("<cust_code><![CDATA[" +invDespMap.get("cust_code")+ "]]></cust_code>");
			xmlBuff.append("<cust_code__bil><![CDATA[" +invDespMap.get("cust_code__bil")+ "]]></cust_code__bil>");
			xmlBuff.append("<item_ser><![CDATA[" +invDespMap.get("item_ser")+ "]]></item_ser>");
			xmlBuff.append("<gp_no><![CDATA[" +invDespMap.get("gp_no")+ "]]></gp_no>");
			xmlBuff.append("<gp_date><![CDATA[" +invDespMap.get("gp_date")+ "]]></gp_date>");
			xmlBuff.append("<lr_no><![CDATA[" +ldsInvHdrMp.get("lr_no")+ "]]></lr_no>");
			xmlBuff.append("<lr_date><![CDATA[" +ldsInvHdrMp.get("lr_date")+ "]]></lr_date>");
			//				xmlBuff.append("<lr_date><![CDATA[" +ldsInvHdrMp.get("lr_date")+ "]]></lr_date>");
			sorderSalMap=new HashMap<String, String>();
			sorderSalMap=getsorderSal(invDespMap.get("sord_no"),conn);
			if(salesInvPstHdr.equalsIgnoreCase("NULLFOUND") || salesInvPstHdr.equalsIgnoreCase("H"))
			{
				
				System.out.println("SorderSalMap size :- ["+sorderSalMap.size()+"]");
				xmlBuff.append("<acct_code__sal><![CDATA[" +sorderSalMap.get("lsacct_sal")+ "]]></acct_code__sal>");
				xmlBuff.append("<cctr_code__sal><![CDATA[" +sorderSalMap.get("lscctr_sal")+ "]]></cctr_code__sal>");
				xmlBuff.append("<posttype><![CDATA[" +"H"+ "]]></posttype>");
				marketReg = checknull(sorderSalMap.get("ls_market_reg"));
				if(marketReg.trim().length()>0)
				{
				xmlBuff.append("<market_reg><![CDATA["+marketReg+"]]></market_reg>");
				}
			}
			else if(salesInvPstHdr.equalsIgnoreCase("D"))
			{
				xmlBuff.append("<acct_code__sal><![CDATA[]]></acct_code__sal>");		//set to null
				xmlBuff.append("<cctr_code__sal><![CDATA[]]></cctr_code__sal>");		// set to null
				xmlBuff.append("<posttype><![CDATA[" +"D"+ "]]></posttype>");
			}

			disItemserMap= new HashMap<String, String>();
			disItemserMap=getdisItemser(invDespMap.get("item_ser"),conn);
			acctCodeDis=checknull(disItemserMap.get("lsacct_dis"));
			cctrCodeDis=checknull(disItemserMap.get("lscctr_dis"));
			if(acctCodeDis.trim().length()>0)
			{
				xmlBuff.append("<acct_code__dis><![CDATA["+acctCodeDis+"]]></acct_code__dis>");
			}
			if(cctrCodeDis.trim().length()>0)
			{
				xmlBuff.append("<cctr_code__dis><![CDATA["+cctrCodeDis+"]]></cctr_code__dis>");
			}
			xmlBuff.append("<bank_code><![CDATA["+invDespMap.get("bank_code")+"]]></bank_code>");
			System.out.println("sorderSalMap["+sorderSalMap+"]");
			
			arCustomerMap=new HashMap<String, String>();
			arCustomerMap=getarCustomer(invDespMap.get("cust_code__bil"),conn);

			acctCodeAr=checknull(arCustomerMap.get("macctar"));
			cctrCodeAr=checknull(arCustomerMap.get("mcctrar"));
			if(acctCodeAr.trim().length()>0)
			{
				xmlBuff.append("<acct_code__ar><![CDATA["+acctCodeAr+"]]></acct_code__ar>");
			}
			/*if(cctrCodeAr.trim().length()>0)// commented by nandkumar gadkari on 16/03/20 for allowed blank spaces 
			{*/
				xmlBuff.append("<cctr_code__ar><![CDATA["+cctrCodeAr+"]]></cctr_code__ar>");
			//}
			xmlBuff.append("<cr_term><![CDATA["+invDespMap.get("cr_term")+"]]></cr_term>");
			xmlBuff.append("<cr_days><![CDATA["+invDespMap.get("cr_days")+"]]></cr_days>");

			//Modified by Anjali R. on [01/10/2018][lr_date set same as eff_date][Start]
			/*String dueDate1=getDueDate(invDespMap.get("cr_term"),ldsInvHdrMp.get("invoice__date"),invDespMap.get("eff_date"),ldsInvHdrMp.get("lr_date"),
					invDespMap.get("cr_days"),conn);*/
			String dueDate1=getDueDate(invDespMap.get("cr_term"),ldsInvHdrMp.get("invoice__date"),invDespMap.get("eff_date"),invDespMap.get("eff_date"),
					invDespMap.get("cr_days"),conn);
			//Modified by Anjali R. on [01/10/2018][lr_date set same as eff_date][End]
			xmlBuff.append("<due_date><![CDATA["+dueDate1+"]]></due_date>");			//Get due date from method "gbf_get_duedate()"
			System.out.println("563 invDespMap invoice tax_class::["+invDespMap.get("tax_class")+"]tax_chap::["+invDespMap.get("tax_chap")+"]tax_env::["+invDespMap.get("tax_env")+"]");
			
			//Changed by mayur on 27-02-18--------[START] 
			if("null".equalsIgnoreCase(invDespMap.get("tax_class")) || (invDespMap.get("tax_class")) == null)
			{
			       xmlBuff.append("<tax_class><![CDATA[ ]]></tax_class>");	
			}
			else
			{
				xmlBuff.append("<tax_class><![CDATA["+invDespMap.get("tax_class")+"]]></tax_class>");	
			}
			
			if("null".equalsIgnoreCase(invDespMap.get("tax_chap")) || (invDespMap.get("tax_chap")) == null)
			{
			      xmlBuff.append("<tax_chap><![CDATA[ ]]></tax_chap>");	
			}
			else
			{
				xmlBuff.append("<tax_chap><![CDATA["+invDespMap.get("tax_chap")+"]]></tax_chap>");		
			}
			
			if("null".equalsIgnoreCase(invDespMap.get("tax_env")) || (invDespMap.get("tax_env")) == null)
			{
				  xmlBuff.append("<tax_env><![CDATA[]]></tax_env>");				
			}
			else
			{
				xmlBuff.append("<tax_env><![CDATA["+invDespMap.get("tax_env")+"]]></tax_env>");			
			}			
			//Changed by mayur on 27-02-18--------[END] 		
			xmlBuff.append("<tran_code><![CDATA["+invDespMap.get("tran_code")+"]]></tran_code>"); //[to set tran_code from despatch]
			//xmlBuff.append("<tax_date><![CDATA["+invDespMap.get("tax_date")+"]]></tax_date>");//[to set tax_date as invoice tran_date instead of so date]
			xmlBuff.append("<tax_date><![CDATA["+taxDateStr+"]]></tax_date>");
			xmlBuff.append("<frt_amt><![CDATA["+invDespMap.get("frt_amt")+"]]></frt_amt>");			
			xmlBuff.append("<frt_type><![CDATA["+invDespMap.get("frt_term")+"]]></frt_type>");			
			xmlBuff.append("<curr_code><![CDATA["+invDespMap.get("sorder_curr_code")+"]]></curr_code>");			
			xmlBuff.append("<exch_rate><![CDATA["+invDespMap.get("sorder_exch_rate")+"]]></exch_rate>");			
			xmlBuff.append("<sales_pers><![CDATA["+invDespMap.get("sales_pers")+"]]></sales_pers>");			
			xmlBuff.append("<curr_code__frt><![CDATA["+invDespMap.get("curr_code__frt")+"]]></curr_code__frt>");			
			xmlBuff.append("<exch_rate__frt><![CDATA["+invDespMap.get("exch_rate__frt")+"]]></exch_rate__frt>");			
			xmlBuff.append("<print_status><![CDATA["+"N"+"]]></print_status>");			
			xmlBuff.append("<confirmed><![CDATA["+"N"+"]]></confirmed>");			
			xmlBuff.append("<chg_date><![CDATA["+sysDate+"]]></chg_date>");			
			xmlBuff.append("<chg_user><![CDATA["+userId+"]]></chg_user>");			
			xmlBuff.append("<chg_term><![CDATA["+termId+"]]></chg_term>");			
			xmlBuff.append("<inv_type><![CDATA["+sorderSalMap.get("ls_ordertype")+"]]></inv_type>");			
			xmlBuff.append("<curr_code__ins><![CDATA["+invDespMap.get("curr_code__ins")+"]]></curr_code__ins>");			
			xmlBuff.append("<exch_rate__ins><![CDATA["+invDespMap.get("exch_rate__ins")+"]]></exch_rate__ins>");			
			xmlBuff.append("<ins_amt><![CDATA["+invDespMap.get("ins_amt")+"]]></ins_amt>");			
			xmlBuff.append("<remarks><![CDATA["+ldsInvHdrMp.get("remarks")+"]]></remarks>");			
			xmlBuff.append("<stan_code__init><![CDATA["+ldsInvHdrMp.get("stan_code__init")+"]]></stan_code__init>");			
			//xmlBuff.append("<frt_amt><![CDATA["+invDespMap.get("frt_amt")+"]]></frt_amt>");			
			xmlBuff.append("<exch_rate__frt><![CDATA["+invDespMap.get("exch_rate__frt")+"]]></exch_rate__frt>");			

			acctCodePr=checknull(acctSchemehdrMap.get("ls_acct_code__pr"));
			cctrCodePr=checknull(acctSchemehdrMap.get("ls_cctr_code__pr"));
			if(acctCodePr.trim().length()>0)
			{
				xmlBuff.append("<acct_code__pr><![CDATA["+acctCodePr+"]]></acct_code__pr>");			
			}
			if(cctrCodePr.trim().length()>0)
			{
				xmlBuff.append("<cctr_code__pr><![CDATA["+cctrCodePr+"]]></cctr_code__pr>");			
			}
			xmlBuff.append("<comm_amt><![CDATA["+invDespMap.get("comm_amt")+"]]></comm_amt>");			
			xmlBuff.append("<tax_amt><![CDATA["+invDespMap.get("tax_amt")+"]]></tax_amt>");			
			//				xmlBuff.append("<disc_amt><![CDATA[]]></disc_amt>");		//Get disc_amt from inv_det 			
			//				xmlBuff.append("<inv_amt><![CDATA[]]></inv_amt>"); 			//Get inv_amt from inv_det 
			xmlBuff.append("<disc_schem_billback_amt><![CDATA["+invDespMap.get("disc_schem_billback_amt")+"]]></disc_schem_billback_amt>"); 			 
			xmlBuff.append("<disc_schem_offinv_amt><![CDATA["+invDespMap.get("disc_schem_offinv_amt")+"]]></disc_schem_offinv_amt>"); 			 
			//				xmlBuff.append("<disc_offinv_amt_hdr><![CDATA[]]></disc_offinv_amt_hdr>"); 	// get by method gbf_calc_hdrdisc_amt()		 
			//				xmlBuff.append("<disc_billback_amt_hdr><![CDATA[]]></disc_billback_amt_hdr>"); 		// get by method gbf_calc_hdrdisc_amt()

			lcCommAmtOc=Double.parseDouble(invDespMap.get("comm_amt"))/Double.parseDouble(ldsInvHdrMp.get("exch_rate"));
			if(!Double.isNaN(lcCommAmtOc))
			{
				xmlBuff.append("<comm_amt__oc><![CDATA["+lcCommAmtOc+"]]></comm_amt__oc>");
			}else
			{
				xmlBuff.append("<comm_amt__oc><![CDATA["+0+"]]></comm_amt__oc>");
			}
			xmlBuff.append("<sales_pers__1><![CDATA["+invDespMap.get("sales_pers__1")+"]]></sales_pers__1>"); 			 
			xmlBuff.append("<sales_pers__2><![CDATA["+invDespMap.get("sales_pers__2")+"]]></sales_pers__2>"); 			 
			xmlBuff.append("<sales_pers_comm_1><![CDATA["+invDespMap.get("sales_pers_comm_1")+"]]></sales_pers_comm_1>"); 			 
			xmlBuff.append("<sales_pers_comm_2><![CDATA["+invDespMap.get("sales_pers_comm_2")+"]]></sales_pers_comm_2>"); 			 
			xmlBuff.append("<sales_pers_comm_3><![CDATA["+invDespMap.get("sales_pers_comm_3")+"]]></sales_pers_comm_3>"); 			 
			xmlBuff.append("<sales_pers_comm_1><![CDATA["+invDespMap.get("sales_pers_comm_1")+"]]></sales_pers_comm_1>"); 			 
			xmlBuff.append("<sales_pers_comm_2><![CDATA["+invDespMap.get("sales_pers_comm_2")+"]]></sales_pers_comm_2>"); 			 
			xmlBuff.append("<sales_pers_comm_3><![CDATA["+invDespMap.get("sales_pers_comm_3")+"]]></sales_pers_comm_3>");
			//Changed By PriyankaC on 28DEC2018.[Start]
			/*sql="select round, case when round_to is null then 0.001 else round_to end  as round_to " +
					" from customer where cust_code = ?";
			pstmt2=	conn.prepareStatement(sql);
			pstmt2.setString(1,  invDespMap.get("cust_code") );
			rs2=pstmt2.executeQuery();
			if(rs2.next())
			{
				round =rs2.getString("round");
				roundTo =rs2.getDouble("round_to");
			}
			rs2.close();
			rs2 = null;
			pstmt2.close();
			pstmt2 = null;
			if(round ==null || round.trim().length()==0)
			{
				sql="select round_inv_to from itemser where item_ser = ?" ;
				pstmt2=	conn.prepareStatement(sql);
				pstmt2.setString(1, invDespMap.get("item_ser") );
				rs2=pstmt2.executeQuery();
				if(rs2.next())
				{
					roundInvTo =rs2.getString("round_inv_to");
				}
				rs2.close();
				rs2 = null;
				pstmt2.close();
				pstmt2 = null;
				if(roundInvTo ==null)
				{
					retString = "VTRND";
					return retString;
				}else
				{
					//netamt= round(netamt,roundTo);
					netamt= Math.round(netamt);
					xmlBuff.append("<round_adj><![CDATA["+(netamt-netAmt)+"]]></round_adj>");  			 
					xmlBuff.append("<net_amt><![CDATA["+netamt+"]]></net_amt>");
				}
			}else
			{
				netamt=geRndamt(netAmt,round,roundTo);
				xmlBuff.append("<round_adj><![CDATA["+(netamt-netAmt)+"]]></round_adj>");  			 
				xmlBuff.append("<net_amt><![CDATA["+netamt+"]]></net_amt>");
			}*/
			//Changed By PriyankaC on 28DEC2018.[END]
			xmlBuff.append("</Detail1>");


			mainInvDespDetMap=(HashMap<String, HashMap>) dataList.get(0);
			System.out.println("@@@@@@@@@525:::mainInvDespDetMap["+mainInvDespDetMap+"]");
			for(String keyStr:mainInvDespDetMap.keySet())
			{	
				System.out.println("Detail 2:::");
				//					if(!keyStr.contains("@"))
				//					{
				/**
				 * Generating XML for
				 * Invoice Trace 
				 * */

				invTraceDetMap= new HashMap<String, String>();

				despLineNo=keyStr;

				invTraceDetMap=mainInvDespDetMap.get(keyStr);
				System.out.println("Inner InvDespDetMap size:-["+invTraceDetMap.size()+"]");
				lineNo++;
				System.out.println("691 invTraceDetMap invoice tax_class::["+invTraceDetMap.get("tax_class")+"]tax_chap::["+invTraceDetMap.get("tax_chap")+"]tax_env::["+invTraceDetMap.get("tax_env")+"]");
				xmlBuff.append("<Detail2 dbID=\"\" domID=\""+lineNo+"\" objContext=\"2\" objName=\"invoice_po\">");		
				xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" status=\"N\" updateFlag=\"A\"/>");
				xmlBuff.append("<invoice_id/>");
				xmlBuff.append("<inv_line_no><![CDATA[" + lineNo + "]]></inv_line_no>");	
				xmlBuff.append("<desp_id><![CDATA["+invTraceDetMap.get("desp_id")+"]]></desp_id>");	
				xmlBuff.append("<desp_line_no><![CDATA["+invTraceDetMap.get("line_no")+"]]></desp_line_no>");
				xmlBuff.append("<item_code><![CDATA[" + invTraceDetMap.get("item_code") + "]]></item_code>");
				xmlBuff.append("<quantity><![CDATA[" + invTraceDetMap.get("quantity") + "]]></quantity>");	
				xmlBuff.append("<unit><![CDATA[" + invTraceDetMap.get("unit") + "]]></unit>");
				xmlBuff.append("<rate><![CDATA[" + invTraceDetMap.get("rate") + "]]></rate>");	
				xmlBuff.append("<unit__rate><![CDATA[" + invTraceDetMap.get("unit__rate") + "]]></unit__rate>");
				xmlBuff.append("<discount><![CDATA[" + invTraceDetMap.get("discount") + "]]></discount>");
				
				//Changed by mayur on 27-02-18--------[START] 
				if("null".equalsIgnoreCase(invTraceDetMap.get("tax_class")) || (invTraceDetMap.get("tax_class")) == null)
				{
				       xmlBuff.append("<tax_class><![CDATA[ ]]></tax_class>");	
				}
				else
				{					
					xmlBuff.append("<tax_class><![CDATA["+invTraceDetMap.get("tax_class")+"]]></tax_class>");	
				}
				
				if("null".equalsIgnoreCase(invTraceDetMap.get("tax_chap")) || (invTraceDetMap.get("tax_chap")) == null)
				{
				      xmlBuff.append("<tax_chap><![CDATA[ ]]></tax_chap>");	
				}
				else
				{
					xmlBuff.append("<tax_chap><![CDATA["+invTraceDetMap.get("tax_chap")+"]]></tax_chap>");				
				}
				
				if("null".equalsIgnoreCase(invTraceDetMap.get("tax_env")) || (invTraceDetMap.get("tax_env")) == null)
				{
					 xmlBuff.append("<tax_env><![CDATA[]]></tax_env>");				
				}
				else
				{					
                     xmlBuff.append("<tax_env><![CDATA["+invTraceDetMap.get("tax_env")+"]]></tax_env>");		
				}	
				//Changed by mayur on 27-02-18--------[END] 
				
				xmlBuff.append("<tax_amt><![CDATA[0]]></tax_amt>");				//tax_amt missing							
				//xmlBuff.append("<net_amt><![CDATA[0]]></net_amt>");				//net_amt missing
				xmlBuff.append("<unit__std><![CDATA[" + invTraceDetMap.get("sord_unit__std") + "]]></unit__std>");
				xmlBuff.append("<conv__qty_stduom><![CDATA[" + invTraceDetMap.get("sord_conv__qty_stduom") + "]]></conv__qty_stduom>");				
				xmlBuff.append("<quantity__stduom><![CDATA[" + invTraceDetMap.get("desp_quantity__stduom") + "]]></quantity__stduom>");
				xmlBuff.append("<conv__rtuom_stduom><![CDATA[" + invTraceDetMap.get("conv__rtuom_stduom") + "]]></conv__rtuom_stduom>");
				xmlBuff.append("<rate__stduom><![CDATA[" + invTraceDetMap.get("desp_rate__stduom") + "]]></rate__stduom>");	
				xmlBuff.append("<comm_amt__oc><![CDATA[0]]></comm_amt__oc>");		//comm_amt__oc missing
				xmlBuff.append("<sord_no><![CDATA[" + invTraceDetMap.get("sord_no") + "]]></sord_no>");					
				xmlBuff.append("<sord_line_no><![CDATA[" + invTraceDetMap.get("line_no__sord") + "]]></sord_line_no>");	
				xmlBuff.append("<item_code__ord><![CDATA[" + invTraceDetMap.get("item_code__ord") + "]]></item_code__ord>");
				xmlBuff.append("<no_art><![CDATA[" + invTraceDetMap.get("no_art") + "]]></no_art>");
				xmlBuff.append("<chg_date><![CDATA["+sysDate+"]]></chg_date>");			
				xmlBuff.append("<chg_user><![CDATA["+userId+"]]></chg_user>");			
				xmlBuff.append("<chg_term><![CDATA["+termId+"]]></chg_term>");		
				xmlBuff.append("<lot_no><![CDATA["+invTraceDetMap.get("lot_no")+"]]></lot_no>");	
				xmlBuff.append("<lot_sl><![CDATA["+invTraceDetMap.get("lot_sl")+"]]></lot_sl>");	
				xmlBuff.append("<site_code__mfg><![CDATA["+invTraceDetMap.get("site_code__mfg")+"]]></site_code__mfg>");	
				
				mfgDateStr = checknull(invTraceDetMap.get("mfg_date"));
				if(mfgDateStr.trim().length()>0)
				{
				xmlBuff.append("<mfg_date><![CDATA["+mfgDateStr+"]]></mfg_date>");	
				}
				expDateStr = checknull(invTraceDetMap.get("exp_date"));
				if(expDateStr.trim().length()>0)
				{
				xmlBuff.append("<exp_date><![CDATA["+expDateStr+"]]></exp_date>");	
				}
				xmlBuff.append("<exp_lev><![CDATA["+invTraceDetMap.get("exp_lev")+"]]></exp_lev>");	
				xmlBuff.append("<fob_value><![CDATA[]]></fob_value>");	
				xmlBuff.append("<rate__clg><![CDATA["+invTraceDetMap.get("rate__clg")+"]]></rate__clg>");
				xmlBuff.append("<line_no><![CDATA["+lineNo+"]]></line_no>");
				xmlBuff.append("<item_ser__prom><![CDATA[]]></item_ser__prom>");
				xmlBuff.append("<curr_code><![CDATA["+invDespMap.get("sorder_curr_code")+"]]></curr_code>");
				xmlBuff.append("<exch_rate><![CDATA["+invDespMap.get("sorder_exch_rate")+"]]></exch_rate>");
				
				
				/**
				 * select rate__std,cost_rate
				 * from despatchdet
				 * 
				 * */
				 /*
				sql="select (case when rate__std is null then 0 else rate__std end ) as lc_ratestd,"
						+ "(case when cost_rate is null then 0 else cost_rate end )as lc_cstrate, disc_amt "
						+ " from despatchdet "
						+ "where desp_id = ? "
						+ " and line_no = ? ";
						//+ " and item_code = ?";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1, invTraceDetMap.get("desp_id"));
				pstmt.setString(2, invTraceDetMap.get("line_no"));
				//pstmt.setString(3, invTraceDetMap.get("item_code"));
				rs=pstmt.executeQuery();
				if(rs.next())
				{
					lc_ratestd=rs.getString("lc_ratestd");
					lc_cstrate=rs.getString("lc_cstrate");
					lc_disc_amt=rs.getDouble("disc_amt");					
				}
				pstmt.close();
				pstmt=null;
				rs.close();
				rs=null;
				*/
				discAmtStr = invTraceDetMap.get("disc_amt");
				if (discAmtStr == null || "null".equals(discAmtStr) || discAmtStr.trim().length() == 0)
				{
					discAmtStr = "0";
				}
				lc_disc_amt = Double.parseDouble(discAmtStr);
				xmlBuff.append("<rate__stk><![CDATA[]]></rate__stk>");
				discAmtStr = invTraceDetMap.get("rate__std");
				if (discAmtStr == null || "null".equals(discAmtStr) || discAmtStr.trim().length() == 0)
				{
					discAmtStr = "0";
				}
				xmlBuff.append("<rate__std><![CDATA["+discAmtStr+"]]></rate__std>");
				discAmtStr = invTraceDetMap.get("cost_rate");
				if (discAmtStr == null || "null".equals(discAmtStr) || discAmtStr.trim().length() == 0)
				{
					discAmtStr = "0";
				}
				xmlBuff.append("<cost_rate><![CDATA["+discAmtStr+"]]></cost_rate>");
				
				
				/**
				 * select dicsount 
				 * from despatchdet
				 * */
				 /* //28-nov-16 manoharan commented and included above
				sql="select disc_amt "
						+ "from despatchdet "
						+ " where desp_id = ?"
						+ " and line_no = ?";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1, invTraceDetMap.get("desp_id"));
				pstmt.setString(2, invTraceDetMap.get("line_no"));
				rs=pstmt.executeQuery();
				if(rs.next())
				{
					lc_disc_amt=rs.getDouble("disc_amt");					
				}
				pstmt.close();
				pstmt=null;
				rs.close();
				rs=null;
				*/
				//change by kunal on 4/9/2018 fix for dicount calculated as infinity if rate is zero for free item
				/*double tempqty= Double.parseDouble(invTraceDetMap.get("desp_rate__stduom")==null?"0.00":invTraceDetMap.get("desp_rate__stduom"));
				if(lc_disc_amt > 0 && tempqty > 0)
				{
					ld_discount=(lc_disc_amt * 100)/(Double.parseDouble(invTraceDetMap.get("desp_quantity__stduom")==null?"0.00":invTraceDetMap.get("desp_quantity__stduom")) * Double.parseDouble(invTraceDetMap.get("desp_rate__stduom")==null?"0.00":invTraceDetMap.get("desp_rate__stduom")));
					xmlBuff.append("<discount><![CDATA["+ld_discount+"]]></discount>");
				}*/	//commented by nandkumar Gadkari on 28/11/18 			
				
				xmlBuff.append("<line_type><![CDATA["+invTraceDetMap.get("line_type")+"]]></line_type>");
				xmlBuff.append("<cust_item__ref><![CDATA["+invTraceDetMap.get("cust_item__ref")+"]]></cust_item__ref>");
				xmlBuff.append("<disc_schem_billback_amt><![CDATA[" + invTraceDetMap.get("disc_schem_billback_amt") + "]]></disc_schem_billback_amt>");				
				xmlBuff.append("<disc_schem_offinv_amt><![CDATA[" + invTraceDetMap.get("disc_schem_offinv_amt") + "]]></disc_schem_offinv_amt>");
				xmlBuff.append("<item_flg><![CDATA[I]]></item_flg>");
				
				ld_tot_amt = (Double.parseDouble(invTraceDetMap.get("desp_quantity__stduom")==null?"0.00":invTraceDetMap.get("desp_quantity__stduom")) * Double.parseDouble(invTraceDetMap.get("desp_rate__stduom")==null?"0.00":invTraceDetMap.get("desp_rate__stduom")));
				ld_disc_amt = (ld_tot_amt * Double.parseDouble(invTraceDetMap.get("discount")==null?"0.00":invTraceDetMap.get("discount"))) / 100;
				ld_net_amt = (ld_tot_amt + 0 - ld_disc_amt) - (Double.parseDouble(invTraceDetMap.get("disc_schem_offinv_amt")==null?"0.00":invTraceDetMap.get("disc_schem_offinv_amt"))) ;
				xmlBuff.append("<net_amt><![CDATA[" +ld_net_amt+ "]]></net_amt>");
				xmlBuff.append("</Detail2>");
				
				/*sql="update despatchdet set invoice_id = ?,quantity_inv=?  where desp_id = ? and line_no = ? ";// added by Abhijit on 16-05-2017
				pstmt=conn.prepareStatement(sql); // 
				pstmt.setString(1,tranId);
				pstmt.setDouble(2,Double.parseDouble(invTraceDetMap.get("quantity")));
				pstmt.setString(3,invTraceDetMap.get("desp_id"));
				pstmt.setString(4,invTraceDetMap.get("desp_line_no"));
				pstmt.executeUpdate();
				pstmt.close();
				pstmt=null;*/ //Commented by Nandkumar gadkari on 08/08/18

				//					}
				//					else
				//					{

				//					}

			}	// END of Detail [mainInvDespDetMap] for loop



			/**
			 * Generating XML for
			 * Invoice Det
			 * */						
			//				invDetBeanMap=mainInvDespDetMap.get(keyStr);
			invDetBeanMap=(HashMap<String, Object>) dataList.get(1);
			System.out.println("@@@@@@@@@@@ invDetBeanMap["+invDetBeanMap.toString()+"]");
			for(String keyStr:invDetBeanMap.keySet())
			{
				System.out.println("Detail 3:::::::::::");
				lineNoDet++;

				invoiceDetBean=(InvoiceDetBean) invDetBeanMap.get(keyStr);
				
				/*sql="select descr  from item where item_code = ?";
				pstmt2=	conn.prepareStatement(sql);
				pstmt2.setString(1,  invoiceDetBean.getItem_code() );
				rs2=pstmt2.executeQuery();
				if(rs2.next())
				{
					itemDescr =rs2.getString("descr");
				}
				rs2.close();
				rs2 = null;
				pstmt2.close();
				pstmt2 = null;
				*/
				
				/*String lineStr=String.valueOf(lineNoDet);
				lineStr="   "+lineStr;
				lineStr=lineStr.substring(lineStr.length()-3, lineStr.length());
				commissionMap=sordcnf.calcCommission(invDespMap.get("sord_no"),lineStr , conn);
				retString= checknull((String)commissionMap.get("errorStr"));
				 if(retString.trim().length() > 0)
				 {
					 return retString;
				 }
				 totalCommSp1= checkDouble((Double)commissionMap.get("sp1Comm"));
				 totalCommSp2= checkDouble((Double)commissionMap.get("sp2Comm"));
				 totalCommSp3= checkDouble((Double)commissionMap.get("sp3Comm"));
				 totalCommAmt = checkDouble((Double)commissionMap.get("netComm"));*/
				 
				xmlBuff.append("<Detail3 dbID=\"\" domID=\""+lineNoDet+"\" objContext=\"3\" objName=\"invoice_po\">");		
				xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" status=\"N\" updateFlag=\"A\"/>");
				xmlBuff.append("<invoice_id/>");
				xmlBuff.append("<line_no><![CDATA[" + lineNoDet + "]]></line_no>");					
				xmlBuff.append("<sord_no><![CDATA[" + invoiceDetBean.getSord_no()+ "]]></sord_no>");					
				xmlBuff.append("<sord_line_no><![CDATA[" + invoiceDetBean.getLine_no__sord() + "]]></sord_line_no>");					
				xmlBuff.append("<item_code><![CDATA[" + invoiceDetBean.getItem_code() + "]]></item_code>");
				xmlBuff.append("<item_descr><![CDATA[" + invoiceDetBean.getItemDescr() + "]]></item_descr>");//ItemDescr set from bean by Nandkumar Gadkari on 28/08/18
				xmlBuff.append("<item_code__ord><![CDATA[" + invoiceDetBean.getItem_code__ord() + "]]></item_code__ord>");					
				xmlBuff.append("<item_flg><![CDATA[I]]></item_flg>");											//item_flag missing				
				xmlBuff.append("<quantity><![CDATA[" + invoiceDetBean.getmTotDespQty()+ "]]></quantity>");				
				xmlBuff.append("<unit><![CDATA[" + invoiceDetBean.getUnit()+ "]]></unit>");				
				xmlBuff.append("<unit__std><![CDATA[" + invoiceDetBean.getSord_unit__std() + "]]></unit__std>");				
				xmlBuff.append("<conv__qty_stduom><![CDATA[" + invoiceDetBean.getSord_conv__qty_stduom() + "]]></conv__qty_stduom>");				
				xmlBuff.append("<quantity__stduom><![CDATA[" + invoiceDetBean.getmTotStdQty() + "]]></quantity__stduom>");				
				xmlBuff.append("<no_art><![CDATA[" + invoiceDetBean.getmNoArt() + "]]></no_art>");				
				xmlBuff.append("<rate><![CDATA[" + invoiceDetBean.getRate() + "]]></rate>");				
				xmlBuff.append("<unit__rate><![CDATA[" + invoiceDetBean.getUnit__rate() + "]]></unit__rate>");				
				xmlBuff.append("<conv__rtuom_stduom><![CDATA[" + invoiceDetBean.getConv__rtuom_stduom() + "]]></conv__rtuom_stduom>");				
				xmlBuff.append("<rate__stduom><![CDATA[" + invoiceDetBean.getDesp_rate__stduom() + "]]></rate__stduom>");				
				xmlBuff.append("<discount><![CDATA[" + invoiceDetBean.getDiscount() + "]]></discount>");				
				ld_disc_amt=0;
				ld_disc_amt =(invoiceDetBean.getmTotStdQty() * (Double.parseDouble(invoiceDetBean.getDesp_rate__stduom()==null?"0.00":invoiceDetBean.getDesp_rate__stduom()))*(Double.parseDouble(invoiceDetBean.getDiscount()==null?"0.00":invoiceDetBean.getDiscount())))/100;
				xmlBuff.append("<disc_amt><![CDATA[" + ld_disc_amt + "]]></disc_amt>");
				//xmlBuff.append("<disc_amt><![CDATA[" + invoiceDetBean.getDisc_amt() + "]]></disc_amt>");				
				xmlBuff.append("<disc_schem_billback_amt><![CDATA[" + invoiceDetBean.getDisc_schem_billback_amt()+ "]]></disc_schem_billback_amt>");				
				xmlBuff.append("<disc_schem_offinv_amt><![CDATA[" + invoiceDetBean.getDisc_schem_offinv_amt() + "]]></disc_schem_offinv_amt>");				
				xmlBuff.append("<acc_code__item><![CDATA[" + invoiceDetBean.getAcc_code__item() + "]]></acc_code__item>");
				//Start added by chandrashekar on 01-09-2016
				 xmlBuff.append("<comm_amt><![CDATA["+invoiceDetBean.getNetComm()+"]]></comm_amt>");			//comm_amt missing							
				 xmlBuff.append("<sales_pers_comm_1><![CDATA["+invoiceDetBean.getSp1Comm()+"]]></sales_pers_comm_1>");										
				 xmlBuff.append("<sales_pers_comm_2><![CDATA["+invoiceDetBean.getSp2Comm()+"]]></sales_pers_comm_2>");										
				 xmlBuff.append("<sales_pers_comm_3><![CDATA["+invoiceDetBean.getSp3Comm()+"]]></sales_pers_comm_3>");										
				//End added by chandrashekar on 01-09-2016
				/*xmlBuff.append("<comm_amt><![CDATA[]]></comm_amt>");			//comm_amt missing							
				xmlBuff.append("<sales_pers_comm_1><![CDATA["+invDespMap.get("sales_pers_comm_1")+"]]></sales_pers_comm_1>");										
				xmlBuff.append("<sales_pers_comm_2><![CDATA["+invDespMap.get("sales_pers_comm_2")+"]]></sales_pers_comm_2>");										
				xmlBuff.append("<sales_pers_comm_3><![CDATA["+invDespMap.get("sales_pers_comm_3")+"]]></sales_pers_comm_3>");										
				*/
				//xmlBuff.append("<sales_pers_comm_3><![CDATA["+invDespMap.get("sales_pers_comm_3")+"]]></sales_pers_comm_3>");										
				xmlBuff.append("<comm_amt__oc><![CDATA[]]></comm_amt__oc>");		//comm_amt__oc missing									
//				xmlBuff.append("<tax_class><![CDATA["+invoiceDetBean.getTax_class()+"]]></tax_class>");											
//				xmlBuff.append("<tax_chap><![CDATA["+invoiceDetBean.getTax_chap()+"]]></tax_chap>");											
//				xmlBuff.append("<tax_env><![CDATA["+invoiceDetBean.getTax_env()+"]]></tax_env>");											
				xmlBuff.append("<desp_id><![CDATA["+invoiceDetBean.getDesp_id()+"]]></desp_id>");											
				xmlBuff.append("<line_no_desp><![CDATA["+"1"+"]]></line_no_desp>");											
				xmlBuff.append("<rate__clg><![CDATA["+invoiceDetBean.getRate__clg()+"]]></rate__clg>");											
				xmlBuff.append("<fin_scheme><![CDATA["+invoiceDetBean.getFin_scheme()+"]]></fin_scheme>");											
				xmlBuff.append("<cust_item__ref><![CDATA["+invoiceDetBean.getCust_item__ref()+"]]></cust_item__ref>");											
				xmlBuff.append("<tax_amt><![CDATA[0]]></tax_amt>");				//tax_amt missing							
				xmlBuff.append("<net_amt><![CDATA[0]]></net_amt>");				//net_amt missing								
				xmlBuff.append("<chg_date><![CDATA["+sysDate+"]]></chg_date>");			
				xmlBuff.append("<chg_user><![CDATA["+userId+"]]></chg_user>");			
				xmlBuff.append("<chg_term><![CDATA["+termId+"]]></chg_term>");	
				xmlBuff.append("</Detail3>");
			}

			xmlBuff.append("</Header0>");
			xmlBuff.append("</group0>");
			xmlBuff.append("</DocumentRoot>");
			xmlInvString=xmlBuff.toString();
			System.out.println("XML generated  :- ["+xmlInvString+"]");
			
			/**
			 * Master statefull
			 * Save data
			 * */
			retString = saveData(loginSite, xmlInvString, xtraParams, conn);
			System.out.println("Return string after save data :- ["+retString+"]");

			//fos1.write(("Return String after save data is :- [" + retString +"]\r\n").getBytes());

			/**
			 * Check Save return string
			 * */
			if (retString.indexOf("Success") > -1)
			{
				//System.out.println("Master statefull SAVE success..");
				//					conn.commit();

				String[] arrayForTranIdIssue = retString.split("<TranID>");
				int endIndexIssue = arrayForTranIdIssue[1].indexOf("</TranID>");
				tranId = arrayForTranIdIssue[1].substring(0, endIndexIssue);
				System.out.println("@V@ Tran id :- [" + tranId + "]");
				//---------------------------Changes by--Nandkumar gadkari on 08/01/19---------------start --
				sql="update invoice_trace t set t.INV_LINE_NO = (select d.line_no from invdet d " + 
					" where d.invoice_id = t.invoice_id " + 
					" and d.SORD_NO = t.SORD_NO " + 
					" and d.SORD_LINE_NO = t.SORD_LINE_NO " + 
					" and d.ITEM_CODE = t.ITEM_CODE) " +
					" where t.invoice_id = ?";
				pstmt=conn.prepareStatement(sql); // 
				pstmt.setString(1,tranId);				
				pstmt.executeUpdate();
				pstmt.close();
				pstmt=null;
				//---------------------------Changes by--Nandkumar gadkari on 08/01/19---------------end --
				//---------------------------Changes by--Nandkumar gadkari on 08/08/18---------------start --
				mainInvDespDetMap=(HashMap<String, HashMap>) dataList.get(0);
				System.out.println("::::::::::::::::mainInvDespDetMap["+mainInvDespDetMap+"]");
				for(String keyStr:mainInvDespDetMap.keySet())
				{	
					System.out.println("Detail 2:::");

					invTraceDetMap= new HashMap<String, String>();

					despLineNo=keyStr;

					invTraceDetMap=mainInvDespDetMap.get(keyStr);
				sql="update despatchdet set status='I',invoice_id = ?,quantity_inv=?  where desp_id = ? and line_no = ? ";// added by Abhijit on 16-05-2017
				pstmt=conn.prepareStatement(sql); // 
				pstmt.setString(1,tranId);
				pstmt.setDouble(2,Double.parseDouble(invTraceDetMap.get("quantity")));
				pstmt.setString(3,invTraceDetMap.get("desp_id"));
				pstmt.setString(4,invTraceDetMap.get("line_no"));
				pstmt.executeUpdate();
				pstmt.close();
				pstmt=null;
				}
				//--------------------------Changes by---Nandkumar gadkari on 08/08/18---------------end --				
				
				sql="update invdet set net_amt=(quantity__stduom*rate__stduom)- ( case when disc_amt is null then 0 else disc_amt end ) +tax_amt where invoice_id=?";
				//sql="update invdet set net_amt=(quantity__stduom*rate__stduom)-disc_amt+tax_amt where invoice_id=?";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1,tranId);
				pstmt.executeUpdate();
				pstmt.close();
				pstmt=null;
				sql="select sum(net_amt),sum(tax_amt),sum(comm_amt),sum(disc_amt) from invdet where invoice_id=?";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1,tranId);
				rs=pstmt.executeQuery();
				if(rs.next())
				{
				//	netAmt = rs.getDouble(1); 
					netAmt =getUnroundDecimal(rs.getDouble(1), 3);
					taxAmt=rs.getDouble(2);
					commAmt=rs.getDouble(3);
					discAmt=rs.getDouble(4);
				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
				// 12-Apr-2021 Manoharan discount value to be taken from invoice_trace
				sql="select sum(quantity__stduom * rate__stduom * discount /100) from invoice_trace where invoice_id=?";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1,tranId);
				rs=pstmt.executeQuery();
				if(rs.next())
				{
					discAmt=rs.getDouble(1);
				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
				
				//PC [Start] on 28DEC2018.
				sql="select round, case when round_to is null then 0.001 else round_to end  as round_to " +
						" from customer where cust_code = ?";
				pstmt2=	conn.prepareStatement(sql);
				pstmt2.setString(1,  invDespMap.get("cust_code") );
				rs2=pstmt2.executeQuery();
				if(rs2.next())
				{
					round =rs2.getString("round");
					roundTo =rs2.getDouble("round_to");
				}
				rs2.close();
				rs2 = null;
				pstmt2.close();
				pstmt2 = null;
				if(round ==null || round.trim().length()==0)
				{
					sql="select round_inv_to from itemser where item_ser = ?" ;
					pstmt2=	conn.prepareStatement(sql);
					pstmt2.setString(1, invDespMap.get("item_ser") );
					rs2=pstmt2.executeQuery();
					if(rs2.next())
					{
						roundInvTo =rs2.getString("round_inv_to");
					}
					rs2.close();
					rs2 = null;
					pstmt2.close();
					pstmt2 = null;
					if(roundInvTo ==null)
					{
						retString = "VTRND";
						return retString;
					}else
					{
						netamt= Math.round(netAmt);
					}
				}else
				{
					netamt=geRndamt(netAmt,round,roundTo);
				}
				System.out.println("totTaxAmt [: netamt : " + netamt +" [ : ]" + netAmt);

				//sql = "update invoice set net_amt=?,tax_amt=?,comm_amt=?,disc_amt=?,inv_amt=? where invoice_id=?";
				sql = "update invoice set net_amt=?,tax_amt=?,comm_amt=?,disc_amt=?,inv_amt = ?,round_adj = ? where invoice_id=?";
				//PriyankaC [End].
				pstmt=conn.prepareStatement(sql);
				pstmt.setDouble(1,netamt);
				pstmt.setDouble(2,taxAmt);
				pstmt.setDouble(3,commAmt);
				pstmt.setDouble(4,discAmt);
				pstmt.setDouble(5,getUnroundDecimal(((netamt+discAmt)-taxAmt),3)); //Added By PriyankaC
				pstmt.setDouble(6,getUnroundDecimal((netamt - netAmt ),3)); // Added By Priyankac 0n 28DEC2018.
				pstmt.setString(7,tranId); 
				pstmt.executeUpdate();
				pstmt.close();
				pstmt=null;
				netamt=0;
				taxAmt=0;
				commAmt=0;
				discAmt=0;
				//fos1.write(("Invoice successful for Dispatch id :- ["+despIdKey+"] TranId generated is:- [" + tranId +"]\n END \r\n\n").getBytes());

				/**
				 * Update invoice_trace
				 * net_amount 
				 * */
//				pstmt=conn.prepareStatement("update invoice_trace set net_amount=(quantity__stduom*rate__stduom)+tax_amt where invoice_id=? ");
//				pstmt.setString(1, tranId);
//				pstmt.executeUpdate();
//				pstmt.close();
//				pstmt=null;
				
				String sordNo="",soLineNo="",taxClass="",taxChap="",taxEnv="",itemCode="";
				//double taxAmt=0,netAmt=0;
				double totTaxAmt=0,totNetAmt=0 ,roundNetamt=0;
				//fos1.write(("Invoice successful for Dispatch id :- ["+despIdKey+"] TranId generated is:- [" + tranId +"]\n END \r\n\n").getBytes());
				
				/*pstmt=conn.prepareStatement("update invoice_trace set net_amt=(quantity__stduom*rate__stduom) where invoice_id=? ");
				pstmt.setString(1, tranId);
				pstmt.executeUpdate();
				pstmt.close();
				pstmt=null;*/
				
				sql="update invdet set tax_class=?,tax_chap=?,tax_env=?,tax_amt=0,net_amt=0 where invoice_id=?";
				pstmt1=conn.prepareStatement(sql);
				pstmt1.setString(1,"");
				pstmt1.setString(2,"");
				pstmt1.setString(3,"");
				pstmt1.setString(4,tranId);
				pstmt1.executeUpdate();
				pstmt1.close();
				pstmt1=null;
				
				sql="update invoice_trace set net_amt = ( (QUANTITY__STDUOM * RATE__STDUOM) - (QUANTITY__STDUOM * RATE__STDUOM * DISCOUNT / 100 ) + TAX_AMT - DISC_SCHEM_OFFINV_AMT ), chg_term = ?,chg_user=? ,chg_date=? where invoice_id = ? ";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1,termId);
				pstmt.setString(2,userId);
				pstmt.setTimestamp(3, sysDateTs);
				pstmt.setString(4,tranId);
				pstmt.executeUpdate();
				pstmt.close();
				pstmt=null;

				/*pstmt=conn.prepareStatement("select sord_no,sord_line_no,tax_class,tax_chap,tax_env,sum(tax_amt),sum(net_amt)"
						+ " from invoice_trace"
						+ " where invoice_id=? "
						+ " group by sord_no,sord_line_no,tax_class,tax_chap,tax_env");*/
				/*pstmt=conn.prepareStatement("select sord_no,line_no,tax_class,tax_chap,tax_env,sum(tax_amt),sum(net_amt)"
						+ " from invoice_trace"
						+ " where invoice_id=? "
						+ " group by sord_no,line_no,tax_class,tax_chap,tax_env");*/
				pstmt=conn.prepareStatement("select sord_no,sord_line_no,item_code,tax_class,tax_chap,tax_env,sum(tax_amt),sum(net_amt)"
						+ " from invoice_trace"
						+ " where invoice_id=? "
						+ " group by sord_no,sord_line_no,item_code,tax_class,tax_chap,tax_env");
				pstmt.setString(1,tranId);
				rs=pstmt.executeQuery();
				//Changed by Pavan R 10oct18[to handle open cursor issue]
				sql="update invdet set tax_class=?,tax_chap=?,tax_env=?,tax_amt=tax_amt+?,net_amt=net_amt+?"
						+ " where invoice_id=? "
						+ " and sord_no=? "
						+ " and sord_line_no=?"						
						+ " and item_code=?";
				pstmt1=conn.prepareStatement(sql);
				//Changed by Pavan R 10oct18 end
				while(rs.next())
				{
					sordNo=rs.getString(1);
					soLineNo=rs.getString(2);
					itemCode=rs.getString(3);
					taxClass=rs.getString(4);
					taxChap=rs.getString(5);
					taxEnv=rs.getString(6);
					taxAmt=rs.getDouble(7);
					netAmt=rs.getDouble(8);
					totTaxAmt+=taxAmt;
					totNetAmt+=netAmt;
					totTaxAmt = getUnroundDecimal((totTaxAmt),3);	//Changed By PriyankaC on 2JAN2019.
					totNetAmt = getUnroundDecimal((totNetAmt),3);	//Changed By PriyankaC on 2JAN2019.
					System.out.println("totTaxAmt [: totNetAmt : " + totTaxAmt +" [ : ]" + totNetAmt);
					
					/*sql="update invdet set tax_class=?,tax_chap=?,tax_env=?,tax_amt=tax_amt+?,net_amt=net_amt+?+?"
							+ " where invoice_id=? "
							+ " and sord_no=? "
							//+ " and sord_line_no=?";
							+ " and line_no=?";*/
					//Changes and Commented By Ajay on 18-01-2018:START
					/*sql="update invdet set tax_class=?,tax_chap=?,tax_env=?,tax_amt=tax_amt+?,net_amt=net_amt+?"
							+ " where invoice_id=? "
							+ " and sord_no=? "
							+ " and sord_line_no=?"
							//+ " and line_no=?";
							+ " and item_code=?";
					pstmt1=conn.prepareStatement(sql);*/
					pstmt1.setString(1,taxClass);
					pstmt1.setString(2,taxChap);
					pstmt1.setString(3,taxEnv);
					pstmt1.setDouble(4,taxAmt);
					pstmt1.setDouble(5,netAmt);
					//pstmt1.setDouble(6,taxAmt);
					//Changes and Commented By Ajay on 18-01-2018:END
					pstmt1.setString(6,tranId);
					pstmt1.setString(7,sordNo);
					pstmt1.setString(8,soLineNo);
					pstmt1.setString(9,itemCode);
					pstmt1.executeUpdate();
					pstmt1.clearParameters();
					
				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
				pstmt1.close();	//[pstmt1 close and nulled buy Pavan R ]
				pstmt1 = null;
				// Added PriyankaC  for update invoice
				sql="select round, case when round_to is null then 0.001 else round_to end  as round_to " +
						" from customer where cust_code = ?";
				pstmt2=	conn.prepareStatement(sql);
				pstmt2.setString(1,  invDespMap.get("cust_code") );
				rs2=pstmt2.executeQuery();
				if(rs2.next())
				{
					round =rs2.getString("round");
					roundTo =rs2.getDouble("round_to");
				}
				rs2.close();
				rs2 = null;
				pstmt2.close();
				pstmt2 = null;
				if(round ==null || round.trim().length()==0)
				{
					sql="select round_inv_to from itemser where item_ser = ?" ;
					pstmt2=	conn.prepareStatement(sql);
					pstmt2.setString(1, invDespMap.get("item_ser") );
					rs2=pstmt2.executeQuery();
					if(rs2.next())
					{
						roundInvTo =rs2.getString("round_inv_to");
					}
					rs2.close();
					rs2 = null;
					pstmt2.close();
					pstmt2 = null;
					if(roundInvTo ==null)
					{
						retString = "VTRND";
						return retString;
					}else
					{
						roundNetamt= Math.round(totNetAmt);
					}
				}else
				{
					roundNetamt=geRndamt(totNetAmt,round,roundTo);
				}
				System.out.println("totTaxAmtBfrUdt [: roundNetamt : " + roundNetamt +" [ : ]" + totNetAmt);

				// Added PriyankaC  for update invoice [END]
				sql = "update invoice set tax_amt=?,net_amt=?,inv_amt=? , round_adj = ? where invoice_id=? " ;
				//sql="update invoice set tax_amt=?,net_amt=?,inv_amt=? where invoice_id=? ";
				pstmt=conn.prepareStatement(sql);
				pstmt.setDouble(1, totTaxAmt);
				//pstmt.setDouble(2, totTaxAmt);
				pstmt.setDouble(2, roundNetamt);
				pstmt.setDouble(3, getUnroundDecimal(((roundNetamt+discAmt)-totTaxAmt),3)); //Added By Priyankac to round the decimal.
				pstmt.setDouble(4, getUnroundDecimal((roundNetamt-totNetAmt),3)); //Added By Priyankac to round the decimal.
				pstmt.setString(5,tranId);
				pstmt.executeUpdate();
				pstmt.close();
				pstmt=null;
				
				/*sql="update despatchdet set invoice_id = ?,quantity_inv=?  where desp_id = ? and line_no = ? ";
				pstmt=conn.prepareStatement(sql); // 
				pstmt.setString(1,tranId);
				pstmt.setDouble(2,Double.parseDouble(invTraceDetMap.get("quantity")));
				pstmt.setString(3,invTraceDetMap.get("desp_id"));
				pstmt.setString(4,invTraceDetMap.get("desp_line_no"));
				pstmt.executeUpdate();
				pstmt.close();
				pstmt=null;*/
				if(sysDate != null)
				{
					sysDateTs = java.sql.Timestamp.valueOf(genericUtility.getValidDateString(sysDate, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
				}
				
				
				/*
				sql="update invoice_trace set net_amt = ( (QUANTITY__STDUOM * RATE__STDUOM) - (QUANTITY__STDUOM * RATE__STDUOM * DISCOUNT / 100 ) + TAX_AMT - DISC_SCHEM_OFFINV_AMT), chg_term = ?,chg_user=? ,chg_date=? where invoice_id = ? ";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1,termId);
				pstmt.setString(2,userId);
				pstmt.setTimestamp(3, sysDateTs);
				pstmt.setString(4,tranId);
				pstmt.executeUpdate();
				pstmt.close();
				pstmt=null;
				*/
				sql="update invdet set chg_term = ?,chg_user=? ,chg_date=? where invoice_id = ? ";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1,termId);
				pstmt.setString(2,userId);
				pstmt.setTimestamp(3, sysDateTs);
				pstmt.setString(4,tranId);
				pstmt.executeUpdate();
				pstmt.close();
				pstmt=null;
				sql="update invoice set chg_term = ?,chg_user=? ,chg_date=? where invoice_id = ? ";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1,termId);
				pstmt.setString(2,userId);
				pstmt.setTimestamp(3, sysDateTs);
				pstmt.setString(4,tranId);
				pstmt.executeUpdate();
				pstmt.close();
				pstmt=null;
				/**
				 * Call Credit check component
				 * PostOrdCreditChk.java
				 * */
				//					PostOrdCreditChk pCrdChk=new PostOrdCreditChk();
				//net amount is not exist in
				//invDespMap
				//					retString=pCrdChk.creditCheck(invDespMap.get("cust_code"),invDespMap.get("cust_code__bil"), invDespMap.get("item_ser"), "0.00",runOpt,//Net amount missing
				//							invDespMap.get("sord_no"), ldsInvHdrMp.get("invoice__date"), invDespMap.get("desp_site_code"), "I", despId,conn);

				// temp cpatil	
				/**
				 * Invoice Posting
				 * */
				/*System.out.println("@@@@@@@@@@@cpatil Invoice posting start....................");
				
				PostOrdInvoicePost PostOrdInvoicePost = new PostOrdInvoicePost();
				String forcedFlag="N";
				String retString1=PostOrdInvoicePost.invoicePosting(tranId, xtraParams, forcedFlag,conn);
				System.out.println("PostOrdInvoicePost return string >>>>"+retString1);
				
				
				PostOrdInvoicePost invPost=new PostOrdInvoicePost();
				String retString1=invPost.invoicePosting(tranId, conn);
				System.out.println("Invoice posting ret string :- ["+retString1+"]");
				
				if( retString1 != null && retString1.trim().length() > 0 )
				{
					return retString1;
				}
				
				
				sql="update invoice set confirmed = 'Y' where invoice_id = ? ";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1,tranId);
				pstmt.executeUpdate();
				pstmt.close();
				pstmt=null;*/

			}
			else
			{
				//fos1.write(("Invoice fail for Dispatch id :- ["+despIdKey+"]\n END \r\n\n").getBytes());
			}
			//			}	// END of Header [mainInvDespMap] for loop

		} catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage()); 
			throw new ITMException(e); 
		}

		return retString;
			}

	private String getDueDate(String crTerm, String invDate, String effDate, String lrDate, String crDays,Connection conn) throws ITMException, Exception
	{
		Timestamp dueDate=null,ldTranDate=null,ldTodate=null;
		// TODO Auto-generated method stub
		String sql="",startFrom="",lsMonth="",liDueDays="",lsOvrdDueDate="",liDays="";
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		int liMonth=0;
		long liCrDays=0,liCurrDays=0,liDaysNo=0;
		try
		{
			UtilMethods utlMethods= new UtilMethods();
			
			//Modified by Anjali R. on [01/02/2019][In case of cr_days got null or blank][Start]
			if(crDays == null || crDays.trim().length() == 0)
			{
				crDays = "0";
			}
			//Modified by Anjali R. on [01/02/2019][In case of cr_days got null or blank][End]
			
		 //System.out.println("crterm :::::::::::::::::::::::"+crTerm);
			sql="select start_from from crterm where cr_term =?";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, crTerm);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				startFrom=rs.getString("start_from");
			}
			pstmt.close();
			pstmt=null;
			rs.close();
			rs=null;

		//	System.out.println("Startfrom value!!!"+startFrom);
		//	System.out.println("Effective date:::::::::::"+effDate);
		//	System.out.println("LR date:::::::::::"+lrDate);
			if("D".equalsIgnoreCase(startFrom) || "Q".equalsIgnoreCase(startFrom) || "B".equalsIgnoreCase(startFrom))
			{
				if(effDate != null && effDate.trim().length() > 0)
				{
					ldTranDate  = Timestamp.valueOf(genericUtility.getValidDateString(effDate, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0"); //Timestamp.valueOf(effDate);
				}
			}
			else if("L".equalsIgnoreCase(startFrom))
			{
				if(lrDate != null && lrDate.trim().length() > 0)
				{
					ldTranDate = Timestamp.valueOf(genericUtility.getValidDateString(lrDate, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0"); //Timestamp.valueOf(lrDate);
				}
			}
			//added by Pavan Rane 01feb2019 [to handle NPE in case of ldTranDate found null based on start_from from crterm]
			if(ldTranDate == null)
			{					
				if(invDate != null && invDate.trim().length() > 0)
				{
					ldTranDate  = Timestamp.valueOf(genericUtility.getValidDateString(invDate, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
				}
				else if(effDate != null && effDate.trim().length() > 0) 
				{
					ldTranDate  = Timestamp.valueOf(genericUtility.getValidDateString(effDate, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
				}else if(lrDate != null && lrDate.trim().length() > 0)
				{
					ldTranDate  = Timestamp.valueOf(genericUtility.getValidDateString(lrDate, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
				}
			}
			//Pavan Rane 01feb2019 end
			//pending for R

			//System.out.println("ldTranDate::::::::::::"+ldTranDate);

			// 02/dec-16 manoharan avoid deprecated
			/*if(ldTranDate != null)
			{
				liMonth=ldTranDate.getMonth();
			}*/
			Calendar cal = Calendar.getInstance();
			cal.setTime(ldTranDate);
			//li_mth = ld_desp_date.getMonth();
			liMonth = cal.get(Calendar.MONTH);
			
			//System.out.println("Li Month :- ["+liMonth+"]");

			if(liMonth==12)
			{
				liMonth=1;
				lsMonth="01";
			}
			else
			{
				liMonth=liMonth+1;

				if(liMonth>9)
				{
					lsMonth=String.valueOf(liMonth);
				}
				else
				{
					lsMonth="0"+String.valueOf(liMonth);
				}
			}

			sql="select c.cr_days ,d.override_due_date from crterm c,crterm_disc d where c.cr_term = d.cr_term and c.cr_term = ?"
				+ " and d.cr_month=? and d.override_due_date is not null";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, crTerm);
			pstmt.setString(2, lsMonth);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				liDueDays=rs.getString("cr_days");
				lsOvrdDueDate=rs.getString("override_due_date");
			}
			pstmt.close();
			pstmt=null;
			rs.close();
			rs=null;

			if(lsOvrdDueDate!=null && lsOvrdDueDate.trim().length()>0)
			{
				liCrDays=Long.valueOf(lsOvrdDueDate.substring(3, 2));
			}
			else
			{
				sql="select c.cr_days ,d.override_due_date from crterm c,crterm_disc d where c.cr_term = d.cr_term and c.cr_term = ?"
					+ " and d.cr_month='99' and d.override_due_date is not null";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1, crTerm);
				rs=pstmt.executeQuery();
				if(rs.next())
				{
					liDueDays=rs.getString("cr_days");
					lsOvrdDueDate=rs.getString("override_due_date");
				}
				pstmt.close();
				pstmt=null;
				rs.close();
				rs=null;

				if(lsOvrdDueDate!=null && lsOvrdDueDate.trim().length()>0)
				{
					liCrDays=Long.valueOf(lsOvrdDueDate.substring(3, 2));
				}
				else
				{
					sql="select cr_days from crterm where cr_term =?";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, crTerm);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						liDays=rs.getString("cr_days");
					}
					pstmt.close();
					pstmt=null;
					rs.close();
					rs=null;
				}
			}

			if(liDueDays==null || liDueDays.trim().length()==0)
			{
				liDueDays="0";
			}
			if(liDays==null || liDays.trim().length()==0)
			{
				liDays="0";
			}

			if(lsOvrdDueDate!=null && lsOvrdDueDate.trim().length()>0)
			{
				sql="select to_date from period where fr_date <=? and to_date >=?";
				pstmt=conn.prepareStatement(sql);
				pstmt.setTimestamp(1, ldTranDate);
				pstmt.setTimestamp(2, ldTranDate);
				rs=pstmt.executeQuery();
				if(rs.next())
				{
					ldTodate=rs.getTimestamp("to_date");
				}
				pstmt.close();
				pstmt=null;
				rs.close();
				rs=null;

				liCurrDays= utlMethods.DaysAfter(ldTranDate, ldTodate);			
				liDaysNo=liCurrDays+Long.valueOf(liCrDays)+Long.valueOf(liDueDays);
			}
		//	System.out.println("crDays:::::::::::::::::::::::::"+crDays);
			if(Integer.parseInt(crDays)!= -999)
			{
				dueDate=utlMethods.RelativeDate(ldTranDate, Integer.parseInt(crDays));
			}
			else
			{
				dueDate=utlMethods.RelativeDate(ldTranDate,(int)liDaysNo);
			}
			System.out.println("Due Date is :- ["+dueDate+"]");

		} catch (SQLException e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage()); 
			throw new ITMException(e); 
		}
		//Modified by Anjali R. on [01/02/2019][Added catch block][Start]
		catch (Exception e1)
		{
			e1.printStackTrace();
			System.out.println("Exception getDueDate::"+ e1.getMessage()); 
			throw new ITMException(e1); 
		}
		//Modified by Anjali R. on [01/02/2019][Added catch block][End]
		System.out.println("@@@@@@@@@@@901::::::::dueDate["+dueDate+"]");

		SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
		String dueDate1 = sdf.format(dueDate);
		System.out.println("System date  :- [" + dueDate + "]");

		System.out.println("@@@@@@@@@@@901::::::::dueDate1["+dueDate1+"]");
		return dueDate1;
	}

	/**
	 * 
	 * @param ldsInvHdrMp
	 * @param conn
	 * @return HashMap<String, String>
	 * @throws ITMException 
	 */
	//	private HashMap<String, HashMap<String, String>> getInvDespatch(HashMap<String, String> ldsInvHdrMp,Connection conn)
	private HashMap<String, String> getInvDespatch(HashMap<String, String> ldsInvHdrMp,Connection conn) throws ITMException
	{
		// TODO Auto-generated method stub
		HashMap<String, String> invDespMap=null;
		HashMap<String, HashMap<String, String>> mainInvDespMap=new HashMap<String, HashMap<String,String>>();
		String invDespSql="",mFrDespDt="",mToDespDt="",despDtStr="",sordDtStr="",lrDtStr="",gpDtStr="",taxDtStr="",effDtStr="";
		String fromDate = "";
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		Timestamp fromDateTimestamp = null;
		try
		{
			if(ldsInvHdrMp.get("lr_date")!=null && ldsInvHdrMp.get("lr_date").trim().length()>0)
			{
				mFrDespDt=ldsInvHdrMp.get("lr_date");
				mToDespDt=ldsInvHdrMp.get("lr_date");
			}


			//added by vishakha for date since query was not getting executed
			if(ldsInvHdrMp.get("desp_date__fr")!=null && ldsInvHdrMp.get("desp_date__fr").trim().length()>0)
			{
				fromDate=ldsInvHdrMp.get("desp_date__fr");
				//	mToDespDt=ldsInvHdrMp.get("lr_date");
			}

			/*	 fromDateTimestamp =  Timestamp.valueOf(fromDate,genericUtility.getDBDateFormat(),genericUtility.getApplDateFormat());//(complDate, dateFormat,genericUtility.getDBDateFormat()) + " 00:00:00.0");
			System.out.println("fromDateTimestamp:::::::::::::::::::::"+fromDateTimestamp);
			 */


			invDespSql="SELECT despatch.desp_id,despatch.desp_date,despatch.sord_no,despatch.sord_date,despatch.cust_code,"
				+ "despatch.cust_code__dlv,despatch.tran_code,despatch.lr_no,despatch.lr_date,despatch.gp_ser,"
				+ "despatch.gp_no,despatch.gp_date,despatch.stan_code,despatch.chg_date,despatch.chg_user,"
				+ "despatch.chg_term,despatch.status,despatch.site_code,sorder.sales_pers,sorder.tax_class,"
				+ "sorder.tax_chap,sorder.tax_env,sorder.tax_date,sorder.cr_term,sorder.site_code,"
				+ "sorder.curr_code," 
				//+ " sorder.exch_rate, " 
				+ " sorder.ord_amt,sorder.tax_amt,sorder.tot_amt,"
				+ "sorder.comm_amt,sorder.frt_amt,sorder.curr_code__frt,sorder.exch_rate__frt,site.fin_entity,"
				+ "crterm.cr_days,sorder.cust_code__bil,sorder.frt_term,sorder.part_qty,sorder.comm_perc,"
				+ "sorder.comm_perc__on,despatch.eff_date,sorder.item_ser,sorder.bank_code,sorder.order_type,"
				+ "despatch.trans_mode,sorder.curr_code__ins,sorder.exch_rate__ins,sorder.ins_amt,sorder.sales_pers__1,"
				+ "sorder.sales_pers__2,sorder.sales_pers_comm_1,sorder.sales_pers_comm_2,sorder.sales_pers_comm_3,despatch.freight,"
				+ "despatch.curr_code__frt,despatch.exch_rate__frt,despatch.exch_rate,despatch.curr_code,despatch.disc_schem_billback_amt,"
				+ "despatch.disc_schem_offinv_amt,despatch.disc_offinv_amt_det,despatch.disc_billback_amt_det,despatch.freight_amt_add"
				+ " FROM despatch,sorder,site,crterm"
				+ " WHERE ( sorder.cr_term = crterm.cr_term ) "
				+ " and( despatch.sord_no = sorder.sale_order ) "
				+ " and( despatch.site_code = site.site_code ) "
				+ " AND( despatch.desp_id >= ? ) "
				+ " AND( despatch.desp_id <= ? ) "
				+ " AND( despatch.cust_code >= ? ) "
				+ " AND( despatch.cust_code <= ? ) "
				//	+ " AND( despatch.status = ?) "
				//	+ " OR( despatch.status = 'H' ) "
				+ " AND( despatch.confirmed = 'Y' ) "
				+ " AND( sorder.order_type like (?)  )"
				+ " ORDER BY despatch.sord_no ASC,despatch.desp_id ASC";
			pstmt=conn.prepareStatement(invDespSql);
			//	        pstmt.setString(1, ldsInvHdrMp.get("desp_date__fr"));
			//	        pstmt.setString(2, ldsInvHdrMp.get("desp_date__to"));
			//    pstmt.setString(1, mFrDespDt);
			//  pstmt.setString(2, mToDespDt);

			//  pstmt.setTimestamp(1, fromDateTimestamp);//changed by vishakha
			// pstmt.setTimestamp(2, fromDateTimestamp);//changed by vishakha
			pstmt.setString(1, ldsInvHdrMp.get("desp_id__fr"));
			//	        pstmt.setString(4, ldsInvHdrMp.get("desp_id__to"));
			pstmt.setString(2, ldsInvHdrMp.get("desp_id__fr"));
			pstmt.setString(3, ldsInvHdrMp.get("cust_code__fr"));
			pstmt.setString(4, ldsInvHdrMp.get("cust_code__to"));
			//  pstmt.setString(7, "");								// Blank Status
			pstmt.setString(5, ldsInvHdrMp.get("so_type"));
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				//System.out.println("@@@@@@@@@@@@ for deaspatch header data.......data found........ ");
				invDespMap=new HashMap<String, String>();

				invDespMap.put("desp_id", checknull(rs.getString("desp_id")));

				if((rs.getString("desp_date"))!= null)
				{
					despDtStr=genericUtility.getValidDateString(checknull(rs.getString("desp_date")), genericUtility.getDBDateFormat(),
							genericUtility.getApplDateFormat());
				}


				invDespMap.put("desp_date", despDtStr);

				invDespMap.put("sord_no", checknull(rs.getString("sord_no")));

				if((rs.getString("sord_date"))!= null)
				{
					System.out.println("inside if of sord_date");
					sordDtStr=genericUtility.getValidDateString(checknull(rs.getString("sord_date")), genericUtility.getDBDateFormat(),
							genericUtility.getApplDateFormat());
				}
				invDespMap.put("sord_date", sordDtStr);

				invDespMap.put("cust_code", checknull(rs.getString("cust_code")));
				invDespMap.put("cust_code__dlv", checknull(rs.getString("cust_code__dlv")));
				invDespMap.put("tran_code", checknull(rs.getString("tran_code")));
				invDespMap.put("lr_no", checknull(rs.getString("lr_no")));

				if((rs.getString("lr_date")) != null)
				{
					System.out.println("inside if of lr_date");
					lrDtStr=genericUtility.getValidDateString(checknull(rs.getString("lr_date")), genericUtility.getDBDateFormat(),
							genericUtility.getApplDateFormat());
				}
				invDespMap.put("lr_date", lrDtStr);

				invDespMap.put("gp_ser", checknull(rs.getString("gp_ser")));
				invDespMap.put("gp_no", checknull(rs.getString("gp_no")));

				if((rs.getString("gp_date"))!=null)
				{
					System.out.println("inside if of gp_date");
					gpDtStr=genericUtility.getValidDateString(checknull(rs.getString("gp_date")), genericUtility.getDBDateFormat(),
							genericUtility.getApplDateFormat());
				}


				invDespMap.put("gp_date", gpDtStr);

				invDespMap.put("stan_code", checknull(rs.getString("stan_code")));
				invDespMap.put("status", checknull(rs.getString("status")));
				invDespMap.put("desp_site_code", checknull(rs.getString("site_code")));
				invDespMap.put("sales_pers", checknull(rs.getString("sales_pers")));
				invDespMap.put("tax_class", checknull(rs.getString("tax_class")));
				invDespMap.put("tax_chap", checknull(rs.getString("tax_chap")));
				invDespMap.put("tax_env", checknull(rs.getString("tax_env")));

				if((rs.getString("tax_date")) != null)
				{
					System.out.println("inside if of tax_date");
					taxDtStr=genericUtility.getValidDateString(checknull(rs.getString("tax_date")), genericUtility.getDBDateFormat(),
							genericUtility.getApplDateFormat());
				}
				invDespMap.put("tax_date", taxDtStr);

				invDespMap.put("cr_term", checknull(rs.getString("cr_term")));
				invDespMap.put("sorder_site_code", checknull(rs.getString("site_code")));
				invDespMap.put("sorder_curr_code", checknull(rs.getString("curr_code")));
				invDespMap.put("sorder_exch_rate", checknull(rs.getString("exch_rate")));
				invDespMap.put("ord_amt", checknull(rs.getString("ord_amt")));
				invDespMap.put("tax_amt", checknull(rs.getString("tax_amt")));
				invDespMap.put("tot_amt", checknull(rs.getString("tot_amt")));
				invDespMap.put("comm_amt", checknull(rs.getString("comm_amt")));
				invDespMap.put("frt_amt", checknull(rs.getString("frt_amt")));
				invDespMap.put("curr_code__frt", checknull(rs.getString("curr_code__frt")));
				invDespMap.put("exch_rate__frt", checknull(rs.getString("exch_rate__frt")));
				invDespMap.put("fin_entity", checknull(rs.getString("fin_entity")));
				invDespMap.put("cr_days", checknull(rs.getString("cr_days")));
				invDespMap.put("cust_code__bil", checknull(rs.getString("cust_code__bil")));
				invDespMap.put("frt_term", checknull(rs.getString("frt_term")));
				invDespMap.put("part_qty", checknull(rs.getString("part_qty")));
				invDespMap.put("comm_perc", checknull(rs.getString("comm_perc")));
				invDespMap.put("comm_perc__on", checknull(rs.getString("comm_perc__on")));

				if((rs.getString("eff_date"))!= null)
				{
					System.out.println("inside if of eff_date");
					effDtStr=genericUtility.getValidDateString(checknull(rs.getString("eff_date")), genericUtility.getDBDateFormat(),
							genericUtility.getApplDateFormat());
				}
				invDespMap.put("eff_date", effDtStr);

				invDespMap.put("item_ser", checknull(rs.getString("item_ser")));
				invDespMap.put("bank_code", checknull(rs.getString("bank_code")));
				invDespMap.put("order_type", checknull(rs.getString("order_type")));
				invDespMap.put("trans_mode", checknull(rs.getString("trans_mode")));
				invDespMap.put("curr_code__ins", checknull(rs.getString("curr_code__ins")));
				invDespMap.put("exch_rate__ins", checknull(rs.getString("exch_rate__ins")));
				invDespMap.put("ins_amt", checknull(rs.getString("ins_amt")));
				invDespMap.put("sales_pers__1", checknull(rs.getString("sales_pers__1")));
				invDespMap.put("sales_pers__2", checknull(rs.getString("sales_pers__2")));
				invDespMap.put("sales_pers_comm_1", checknull(rs.getString("sales_pers_comm_1")));
				invDespMap.put("sales_pers_comm_2", checknull(rs.getString("sales_pers_comm_2")));
				invDespMap.put("sales_pers_comm_3", checknull(rs.getString("sales_pers_comm_3")));
				invDespMap.put("freight", checknull(rs.getString("freight")));
				invDespMap.put("curr_code__frt", checknull(rs.getString("curr_code__frt")));
				invDespMap.put("exch_rate__frt", checknull(rs.getString("exch_rate__frt")));
				invDespMap.put("exch_rate", checknull(rs.getString("exch_rate")));
				invDespMap.put("curr_code", checknull(rs.getString("curr_code")));
				invDespMap.put("disc_schem_billback_amt", checknull(rs.getString("disc_schem_billback_amt")));
				invDespMap.put("disc_schem_offinv_amt", checknull(rs.getString("disc_schem_offinv_amt")));
				invDespMap.put("disc_offinv_amt_det", checknull(rs.getString("disc_offinv_amt_det")));
				invDespMap.put("disc_billback_amt_det", checknull(rs.getString("disc_billback_amt_det")));
				invDespMap.put("freight_amt_add", checknull(rs.getString("freight_amt_add")));


				System.out.println("MAP VALUES:::::::::::::::"+invDespMap);
				//	        	mainInvDespMap.put(rs.getString("desp_id"), invDespMap);

				//	        	invDespMap=null;
			}
			pstmt.close();
			pstmt=null;
			rs.close();
			rs=null;
		} catch (SQLException e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage()); 
			throw new ITMException(e); 
		} catch (ITMException e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage()); 
			throw new ITMException(e); 
		} catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage()); 
			throw new ITMException(e); 
		}

		//	    return mainInvDespMap;
		return invDespMap;
	}

	/**
	 * 
	 * @param sordNo
	 * @param status
	 * @param despId
	 * @param ldsInvHdrMp
	 * @param despSiteCode 
	 * @param conn
	 * @return HashMap<String, String>
	 * @throws ITMException 
	 */
	//	private HashMap<String, HashMap<String, String>> getInvDespatchDet(String sordNo, String status,String despId,HashMap<String, String> ldsInvHdrMp, Connection conn)
	private ArrayList<Object> getInvDespatchDet(String sordNo, String status,String despId,HashMap<String, String> ldsInvHdrMp, String despSiteCode, Connection conn) throws ITMException
	{
		// TODO Auto-generated method stub
		//		HashMap<String, String> invDespDetMap=null;
		HashMap<String, String> invTraceMap=null;
		//		HashMap<String, HashMap<String, String>> mainInvDespDetMap=new HashMap<String, HashMap<String,String>>();
		HashMap<String, HashMap> mainInvDespDetMap=new HashMap<String, HashMap>();
		HashMap<String, Object> invDetBeanMap= new HashMap<String, Object>();
		HashMap<String, String> stockDetMap=new HashMap<String, String>();
		String invDespDetSql="",mFrDespDt="",mToDespDt="",lsAcctSchemedet="",lsFinscheme="",lsCustitemRef="",mfgDtStr="",ldExpDtStr="",ld_mfg_date = "",ld_exp_date = "",ld_mfg_date1 = "",ld_exp_date1 = "";
		String invDespDetStockSql="",site_code__mfg="",stk_opt="";
		double lcDiscAmt=0.0,mTotDespQty=0.0,mTotStdQty=0.0,mRealQty=0.0;
		int mNoArt=0,schemeCount=0,finSchemeCount = 0;
		String desp_id="",line_no="",sord_no="", preSordNo = "",line_no__sord="",exp_lev="",item_code__ord="",item_code="",lot_no="",lot_sl="",quantity__ord="";
		String quantity="",loc_code="",desp_conv__qty_stduom="",desp_unit__std="",unit="",desp_quantity__stduom="",tax_class="",tax_chap="",tax_env="";
		String discount="",unit__rate="",sord_conv__qty_stduom="",conv__rtuom_stduom="",sord_unit__std="",sord_quantity__stduom="",sord_rate__stduom="";
		String rate="",quantity_real="",quantity_inv="",invoice_id="",no_art="",rate__clg="",desp_rate__stduom="",disc_amt="",cust_item__ref="";
		String disc_schem_billback_amt="",disc_schem_offinv_amt="",retString="",site_code="";
		double totalCommSp1=0.0,totalCommSp2=0.0,totalCommSp3=0.0,totalCommAmt=0.0;
		double lc_desp_std_rate=0.0,totAmount = 0,costRate=0,rateStd = 0;
		PreparedStatement pstmt=null, pstmt1 = null;
		ResultSet rs=null, rs1 = null;
		String beanMapKey="",lsLineType="", sql= "";
		ArrayList<Object> mainArrayList= new ArrayList<Object>();
		HashMap commissionMap = null;
		/**
		 * Generate Bean object
		 * */
		InvoiceDetBean invoiceDetBean= null;
		try
		{
			/*invDespDetSql="SELECT despatchdet.desp_id,despatchdet.line_no,despatchdet.sord_no,despatchdet.line_no__sord,despatchdet.exp_lev,"
				+ "despatchdet.item_code__ord,despatchdet.item_code,despatchdet.lot_no,despatchdet.lot_sl,despatchdet.quantity__ord,"
				+ "despatchdet.quantity,despatchdet.loc_code,despatchdet.status,despatchdet.conv__qty_stduom,despatchdet.unit__std,"
				+ "despatchdet.unit,despatchdet.quantity__stduom as dquantity__stduom,despatchdet.tax_class,despatchdet.tax_chap,despatchdet.tax_env,"
				+ "sorddet.discount,sorddet.unit__rate,sorddet.conv__qty_stduom,sorddet.conv__rtuom_stduom,sorddet.unit__std,"
				+ "sorddet.quantity__stduom as squantity__stduom,sorddet.rate__stduom as srate__stduom,sorddet.rate,despatchdet.quantity_real,despatchdet.quantity_inv,"
				+ "despatchdet.invoice_id,despatchdet.no_art,despatchdet.rate__clg,despatchdet.rate__stduom as drate__stduom ,despatchdet.disc_amt,"
				+ "despatchdet.cust_item__ref,despatchdet.disc_schem_billback_amt,despatchdet.disc_schem_offinv_amt, sorditem.nature, "
				+ " (case when despatchdet.rate__std is null then 0 else despatchdet.rate__std end ) as rate__std,"
				+ " (case when despatchdet.cost_rate is null then 0 else despatchdet.cost_rate end ) as cost_rate, sorddet.ITEM_DESCR, "
				+ " stock.site_code__mfg, stock.mfg_date, stock.exp_date "
				+ " FROM despatchdet,sorddet, sorditem, stock "
				+ " WHERE ( despatchdet.sord_no = sorddet.sale_order )"
				+ " and( despatchdet.line_no__sord = sorddet.line_no )"
				+ " and( sorditem.sale_order = sorddet.sale_order )"
				+ " and( sorditem.line_no = sorddet.line_no )"
				+ " and( sorditem.exp_lev = despatchdet.exp_lev )"
				+ " and stock.item_code = despatchdet.item_code "
				+ " and stock.site_code = sorditem.site_code "
				+ " and stock.loc_code = despatchdet.loc_code "
				+ " and stock.lot_no = despatchdet.lot_no "
				+ " and stock.lot_sl = despatchdet.lot_sl "
				+ " and( ( case when  DESPATCHDET.STATUS is null then ' ' else despatchdet.STATUS end = ? )"
				//+ " AND( DESPATCHDET.SORD_NO = ?)"
				+ " AND( DESPATCHDET.DESP_ID >= ?)"
				+ " AND( DESPATCHDET.DESP_ID <= ? ) )"
				+ " ORDER BY despatchdet.sord_no ASC,despatchdet.line_no__sord ASC,despatchdet.exp_lev ASC";*/// commented by nandkumar gadkari ON 20/06/18 
		
			//changes in sql  by nandkumar gadkari on 20/06/18 
			invDespDetSql="SELECT despatchdet.desp_id,despatchdet.line_no,despatchdet.sord_no,despatchdet.line_no__sord,despatchdet.exp_lev,"
					+ "despatchdet.item_code__ord,despatchdet.item_code,despatchdet.lot_no,despatchdet.lot_sl,despatchdet.quantity__ord,"
					+ "despatchdet.quantity,despatchdet.loc_code,despatchdet.status,despatchdet.conv__qty_stduom,despatchdet.unit__std,"
					+ "despatchdet.unit,despatchdet.quantity__stduom as dquantity__stduom,despatchdet.tax_class,despatchdet.tax_chap,despatchdet.tax_env,"
					+ "sorddet.discount,sorddet.unit__rate,sorddet.conv__qty_stduom,sorddet.conv__rtuom_stduom,sorddet.unit__std,"
					+ "sorddet.quantity__stduom as squantity__stduom,sorddet.rate__stduom as srate__stduom,sorddet.rate,despatchdet.quantity_real,despatchdet.quantity_inv,"
					+ "despatchdet.invoice_id,despatchdet.no_art,despatchdet.rate__clg,despatchdet.rate__stduom as drate__stduom ,despatchdet.disc_amt,"
					+ "despatchdet.cust_item__ref,despatchdet.disc_schem_billback_amt,despatchdet.disc_schem_offinv_amt, sorditem.nature, "
					+ " (case when despatchdet.rate__std is null then 0 else despatchdet.rate__std end ) as rate__std,"
					+ " (case when despatchdet.cost_rate is null then 0 else despatchdet.cost_rate end ) as cost_rate, sorddet.ITEM_DESCR,"
					+ "item.stk_opt,sorditem.site_code"
					+ " FROM despatchdet,sorddet, sorditem ,item"
					+ " WHERE ( despatchdet.sord_no = sorddet.sale_order )"
					+ " and( despatchdet.line_no__sord = sorddet.line_no )"
					+ " and( sorditem.sale_order = sorddet.sale_order )"
					+ " and( sorditem.line_no = sorddet.line_no )"
					+ " and( sorditem.exp_lev = despatchdet.exp_lev )"
					+ " and item.item_code = despatchdet.item_code "
					+ " and( ( case when  DESPATCHDET.STATUS is null then ' ' else despatchdet.STATUS end = ? )"
					+ " AND( DESPATCHDET.DESP_ID >= ?)"
					+ " AND( DESPATCHDET.DESP_ID <= ? ) )"
					+ " ORDER BY despatchdet.sord_no ASC,despatchdet.line_no__sord ASC,despatchdet.exp_lev ASC"; 
			pstmt=conn.prepareStatement(invDespDetSql);
			pstmt.setString(1," ");// changes by Nandkumar gadkari on 08/08/18 Status = space set removed
			pstmt.setString(2,despId);
			pstmt.setString(3,despId);
			rs=pstmt.executeQuery();
			while(rs.next())
			{	
				
				
				invTraceMap=new HashMap<String, String>();

				desp_id=checknull(rs.getString("desp_id"));
				line_no=checknull(rs.getString("line_no"));
				sord_no=checknull(rs.getString("sord_no"));
				line_no__sord=checknull(rs.getString("line_no__sord"));
				exp_lev=checknull(rs.getString("exp_lev"));
				item_code__ord=checknull(rs.getString("item_code__ord"));
				item_code=checknull(rs.getString("item_code"));
				lot_no=checknull(rs.getString("lot_no"));
				lot_sl=checknull(rs.getString("lot_sl"));
				quantity__ord=checknull(rs.getString("quantity__ord"));
				quantity=checknull(rs.getString("quantity"));
				loc_code=checknull(rs.getString("loc_code"));
				status=checknull(rs.getString("status"));
				desp_conv__qty_stduom=checknull(rs.getString("conv__qty_stduom"));
				desp_unit__std=checknull(rs.getString("unit__std"));
				unit=checknull(rs.getString("unit"));
				desp_quantity__stduom=checknull(rs.getString("dquantity__stduom"));
				tax_class=checknull(rs.getString("tax_class"));
				tax_chap=checknull(rs.getString("tax_chap"));
				tax_env=checknull(rs.getString("tax_env"));
				discount=checkDoubleNull(rs.getString("discount"));
				// Manoharan as instructed by manoj in case of scheme unit__rate is going wrong
				if (!item_code__ord.trim().equals(item_code.trim()) )
				{
					unit__rate = unit;
				}
				else
				{
					unit__rate=checknull(rs.getString("unit__rate"));
				}
				// end Manoharan as instructed by manoj in case of scheme unit__rate is going wrong
				sord_conv__qty_stduom=checknull(rs.getString("conv__qty_stduom"));
				conv__rtuom_stduom=checknull(rs.getString("conv__rtuom_stduom"));
				sord_unit__std=checknull(rs.getString("unit__std"));
				sord_quantity__stduom=checknull(rs.getString("squantity__stduom"));
				sord_rate__stduom=checknull(rs.getString("srate__stduom"));
				//rate=checknull(rs.getString("rate"));
				lc_desp_std_rate=Double.parseDouble(checkNullDouble(rs.getString("drate__stduom")));
				if(lc_desp_std_rate>0)
				{
					rate	=""+(lc_desp_std_rate/Double.parseDouble(checkNullDouble(conv__rtuom_stduom)));
				}else
				{
					rate =""+(lc_desp_std_rate);
				}
				quantity_real=checknull(rs.getString("quantity_real"));
				quantity_inv=checknull(rs.getString("quantity_inv"));
				invoice_id=checknull(rs.getString("invoice_id"));
				no_art=checknull(rs.getString("no_art"));
				rate__clg=checknull(rs.getString("rate__clg"));
				desp_rate__stduom=checknull(rs.getString("drate__stduom"));
				disc_amt=checknull(rs.getString("disc_amt"));
				cust_item__ref=checknull(rs.getString("cust_item__ref"));
				disc_schem_billback_amt=checknull(rs.getString("disc_schem_billback_amt"));
				disc_schem_offinv_amt=checknull(rs.getString("disc_schem_offinv_amt"));
				
				lsLineType=checknull(rs.getString("nature"));
				/*ld_mfg_date=rs.getString("mfg_date");
				ld_exp_date=rs.getString("exp_date");*/// commented by nandkumar gadkari 
				site_code=checknull(rs.getString("site_code"));//added by nandkumar gadkari on 20/06/18  
				stk_opt=checknull(rs.getString("stk_opt"));//added by nandkumar gadkari on 20/06/18
				costRate=rs.getDouble("cost_rate");
				rateStd = rs.getDouble("rate__std");
				invTraceMap.put("rate__std", "" + rateStd);	        	
				invTraceMap.put("cost_rate", "" + costRate);	        	
				invTraceMap.put("disc_amt", rs.getString("disc_amt"));
				invTraceMap.put("item_descr", rs.getString("item_descr"));
				////changes  by nandkumar gadkari on 20/06/18----------------------Start------------------------------ for stk_opt <>0 item ---------
				if (!"0".equalsIgnoreCase(stk_opt.trim()))
				{
					
					invDespDetStockSql="select site_code__mfg,mfg_date, exp_date from stock where item_code = ? and site_code =? and loc_code = ? "
							+ "and lot_no = ? and lot_sl = ? ";
			
					pstmt1=conn.prepareStatement(invDespDetStockSql);
					pstmt1.setString(1,item_code);
					pstmt1.setString(2,site_code);
					pstmt1.setString(3,loc_code);
					pstmt1.setString(4,lot_no);
					pstmt1.setString(5,lot_sl);
					rs1=pstmt1.executeQuery();
					if(rs1.next())
					{
						site_code__mfg=checknull(rs1.getString("site_code__mfg"));
						ld_mfg_date=rs1.getString("mfg_date");
						ld_exp_date=rs1.getString("exp_date");
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					/*if(ld_mfg_date!=null)
					{
						ld_mfg_date=genericUtility.getValidDateString(rs.getString("mfg_date"), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat());
					}
					if(ld_exp_date!=null)
					{
						ld_exp_date=genericUtility.getValidDateString(rs.getString("exp_date"), genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat());
					}*/ //commented by nandkumar 
					if(ld_mfg_date!=null)
					{
						ld_mfg_date1=genericUtility.getValidDateString(ld_mfg_date, genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat());
					}
					if(ld_exp_date!=null)
					{
						ld_exp_date1=genericUtility.getValidDateString(ld_exp_date, genericUtility.getDBDateFormat(), genericUtility.getApplDateFormat());
					}
			   }		
				//Timestamp ld_exp_date =  rs.getTimestamp("exp_date");
				//System.out.println("@@@@@@@@@ ld_mfg_date["+ld_mfg_date+"]ld_exp_date["+ld_exp_date+"]");
				stockDetMap=new HashMap<String, String>();
				stockDetMap.put("ls_site_code__mfg",/* checknull(rs.getString("site_code__mfg"))*/site_code__mfg );
				stockDetMap.put("ld_mfg_date", ld_mfg_date1);
				stockDetMap.put("ld_exp_date", ld_exp_date1);
			////changes  by nandkumar gadkari on 20/06/18----------------------end ------------------------------ for stk_opt <>0 item ---------
				
				if(disc_schem_offinv_amt == null || disc_schem_offinv_amt.trim().length()==0)
				{
					disc_schem_offinv_amt="0";
				}
				System.out.println("invTraceMap tax_class::["+tax_class+"]tax_chap::["+tax_chap+"]tax_env::["+tax_env+"]");
				invTraceMap.put("desp_id", desp_id);	        	
				invTraceMap.put("line_no", line_no);	        	
				invTraceMap.put("sord_no", sord_no);	        	
				invTraceMap.put("line_no__sord",line_no__sord);	        	
				invTraceMap.put("exp_lev",exp_lev );	        	
				invTraceMap.put("item_code__ord", item_code__ord);	        	
				invTraceMap.put("item_code",item_code );	        	
				invTraceMap.put("lot_no",lot_no );	        	
				invTraceMap.put("lot_sl", lot_sl);	        	
				invTraceMap.put("quantity__ord",quantity__ord );	        	
				invTraceMap.put("quantity",quantity );	        		        	
				invTraceMap.put("loc_code", loc_code);	        	
				invTraceMap.put("status", status);	        	
				invTraceMap.put("desp_conv__qty_stduom", desp_conv__qty_stduom);	        	
				invTraceMap.put("desp_unit__std",desp_unit__std);	        	
				invTraceMap.put("unit", unit);	        	
				invTraceMap.put("desp_quantity__stduom",desp_quantity__stduom );	        	
				//invTraceMap.put("tax_class", tax_class);	        	
				//invTraceMap.put("tax_chap", tax_chap);	        	
				//invTraceMap.put("tax_env", tax_env);	 
	   
				//Commented by mayur on 05-03-18-------start
				if("null".equalsIgnoreCase(tax_class) || tax_class == null || tax_class.trim().length()==0)
				{

					invTraceMap.put("tax_class"," ");	     
				}
				else
				{

					invTraceMap.put("tax_class", tax_class);	     
				}  
				
				
				if("null".equalsIgnoreCase(tax_chap) || tax_chap == null || tax_chap.trim().length()==0)
				{

					invTraceMap.put("tax_chap"," ");	 
				}
				else
				{

					invTraceMap.put("tax_chap", tax_chap);	
				}   
					
				if("null".equalsIgnoreCase(tax_env) || tax_env == null || tax_env.trim().length()==0)
				{

					invTraceMap.put("tax_env"," ");	 
				}
				else
				{

					invTraceMap.put("tax_env", tax_env);	
				}   			 
				//Commented by mayur on 05-03-18-------end
			   	
				invTraceMap.put("discount", discount);	        	
				invTraceMap.put("unit__rate", unit__rate);	        	
				invTraceMap.put("sord_conv__qty_stduom", sord_conv__qty_stduom);	        	
				invTraceMap.put("conv__rtuom_stduom", conv__rtuom_stduom);	        	
				invTraceMap.put("sord_unit__std", sord_unit__std);	        	
				invTraceMap.put("sord_quantity__stduom", sord_quantity__stduom);	        	
				invTraceMap.put("sord_rate__stduom", sord_rate__stduom);	        	
				invTraceMap.put("rate", rate);	        	
				invTraceMap.put("quantity_real", quantity_real);	        	
				invTraceMap.put("quantity_inv", quantity_inv);	        	
				invTraceMap.put("invoice_id", invoice_id);	        	
				invTraceMap.put("no_art", no_art);	        	
				invTraceMap.put("rate__clg", rate__clg);	        	
				invTraceMap.put("desp_rate__stduom", desp_rate__stduom);	        	
				invTraceMap.put("disc_amt", disc_amt);	        	
				invTraceMap.put("cust_item__ref", cust_item__ref);	        	
				invTraceMap.put("disc_schem_billback_amt", disc_schem_billback_amt);	        	
				invTraceMap.put("disc_schem_offinv_amt", disc_schem_offinv_amt);

				/*System.out.println("Detal map Size::::::::::::::"+invTraceMap.size());
				System.out.println("Map Values :::::::::::::::::::"+invTraceMap);*/
				if (!preSordNo.trim().equals(sord_no.trim()))
				{
					totAmount = 0;
					schemeCount = 0;
					finSchemeCount = 0;
					sql="select sum(quantity * rate) as amount from sorddet where sale_order = ? ";
					pstmt1=conn.prepareStatement(sql);
					pstmt1.setString(1, sord_no);   
					rs1=pstmt1.executeQuery();
					if(rs1.next())
					{
						totAmount = rs1.getDouble("amount");
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					
					sql="select count(1) as schemeCount from sorderdet_scheme where tran_id = ? ";
					pstmt1=conn.prepareStatement(sql);
					pstmt1.setString(1, sord_no);   
					rs1=pstmt1.executeQuery();
					if(rs1.next())
					{
						schemeCount = rs1.getInt("schemeCount");
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					
					lsFinscheme=getLsFinscheme(rs.getString("sord_no"),conn);
					invTraceMap.put("fin_scheme", lsFinscheme);
					
					preSordNo = sord_no;
					
				}
				if (totAmount > 0)
				{

					CalcSchemeAmt calcSchemeAmt=new CalcSchemeAmt();
					calcSchemeAmt.updateAmt(sord_no, conn);
				}
				if (schemeCount >0)
				{
					lsAcctSchemedet=getLsAcctSchemedet(rs.getString("sord_no"),rs.getString("line_no__sord"),conn);
					invTraceMap.put("acc_code__item", lsAcctSchemedet);
				}
				
				//lsLineType=getLineType(rs.getString("sord_no"),rs.getString("line_no__sord"),rs.getString("exp_lev"),conn);
				invTraceMap.put("line_type",lsLineType);

				//stockDetMap=getStockDetail(rs.getString("item_code"),despSiteCode,rs.getString("loc_code"),rs.getString("lot_no"),rs.getString("lot_sl"),conn);
			//	System.out.println("@@@@@@@@stockDetMap["+stockDetMap+"]");
				invTraceMap.put("site_code__mfg",stockDetMap.get("ls_site_code__mfg"));

				//mfgDtStr=genericUtility.getValidDateString(stockDetMap.get("ld_mfg_date"), genericUtility.getDBDateFormat(),genericUtility.getApplDateFormat());
				invTraceMap.put("mfg_date",stockDetMap.get("ld_mfg_date"));
				//invTraceMap.put("mfg_date",mfgDtStr);

				//ldExpDtStr=genericUtility.getValidDateString(stockDetMap.get("ld_exp_date"), genericUtility.getDBDateFormat(),genericUtility.getApplDateFormat());
				invTraceMap.put("exp_date",stockDetMap.get("ld_exp_date"));
				//invTraceMap.put("exp_date",ldExpDtStr);

				//	        	lsCustitemRef=getlsCustitemRef(rs.getString("sord_no"),rs.getString("item_code"),conn);
				//	        	invDespDetMap.put("cust_item__ref", lsCustitemRef);

				//	        	mainInvDespDetMap.put(rs.getString("desp_id"), invDespDetMap);
				
				//Start Added by chandrashekar on 01-09-2016
				double ld_tot_amt=0.0,ld_net_amt=0.0,ld_tax_amt=0.0,ld_disc_amt=0.0;
				ld_tot_amt = (Double.parseDouble(desp_quantity__stduom) * Double.parseDouble(desp_rate__stduom));
				ld_disc_amt = (ld_tot_amt * Double.parseDouble(discount)) / 100;
				ld_net_amt = (ld_tot_amt + ld_tax_amt - ld_disc_amt) - (Double.parseDouble(disc_schem_offinv_amt));
				invTraceMap.put("tax_amt",String.valueOf(ld_tax_amt));
				invTraceMap.put("net_amt",String.valueOf(ld_net_amt));
			//	System.out.println("line_no["+line_no+"]");
				String lineStr="";
				lineStr="   "+checknull(line_no).trim();
				lineStr=lineStr.substring(lineStr.length()-3, lineStr.length());
			//	System.out.println("lineStr>>>["+lineStr+"]");
				commissionMap=calcCommission(invTraceMap,lineStr,conn);
				retString= checknull((String)commissionMap.get("errorStr"));
				totalCommSp1= checkDouble((Double)commissionMap.get("sp1Comm"));
				totalCommSp2= checkDouble((Double)commissionMap.get("sp2Comm"));
				totalCommSp3= checkDouble((Double)commissionMap.get("sp3Comm"));
				totalCommAmt = checkDouble((Double)commissionMap.get("netComm"));
				invTraceMap.put("sp1Comm", commissionMap.get("sp1Comm").toString());
				invTraceMap.put("sp2Comm", commissionMap.get("sp2Comm").toString());
				invTraceMap.put("sp3Comm", commissionMap.get("sp3Comm").toString());
				invTraceMap.put("netComm", commissionMap.get("netComm").toString());
				
				//End Added by chandrashekar on 01-09-2016
				System.out.println("@@@@@@@@@@@@@@@@@ invTraceMap["+invTraceMap+"]");

				mainInvDespDetMap.put(rs.getString("line_no"), invTraceMap);

			System.out.println("@@@@@@@@@@@@@@@@@ mainInvDespDetMap["+mainInvDespDetMap+"]");

			//beanMapKey=rs.getString("sord_no").trim()+"@".trim()+rs.getString("line_no__sord").trim();
			//Changes done by Pavan R on 09/JAN/18 Start to sort desp line and sord line wise
			beanMapKey=rs.getString("sord_no").trim()+"@".trim()+rs.getString("line_no__sord").trim()+"@".trim()+rs.getString("item_code").trim();
			//beanMapKey=rs.getString("sord_no").trim()+"@".trim()+rs.getString("line_no").trim();	
			//beanMapKey=rs.getString("sord_no").trim()+"@".trim()+rs.getString("line_no").trim()+"@".trim()+rs.getString("line_no__sord").trim();
			//Changes done by Pavan R on 09/JAN/18 End
				System.out.println("beanMapKey>>>>["+beanMapKey+"]");
				/**
				 * Check generated key is exist in
				 * Bean map
				 * */
				if(invDetBeanMap.get(beanMapKey)==null)
				{
					invoiceDetBean= new InvoiceDetBean();
					System.out.println("1945@@ invDetBeanMap tax_class::["+tax_class+"]tax_chap::["+tax_chap+"]tax_env::["+tax_env+"]");
					invoiceDetBean.setmTotDespQty(Double.parseDouble(quantity==null?"0.00":quantity));
					invoiceDetBean.setmTotStdQty(Double.parseDouble(desp_quantity__stduom==null?"0.00":desp_quantity__stduom));
					invoiceDetBean.setmNoArt(Integer.parseInt(no_art==null?"0":no_art));
					invoiceDetBean.setmRealQty(Double.parseDouble(quantity_real==null?"0.00":quantity_real));

					invoiceDetBean.setDesp_id(checknull(desp_id));
					invoiceDetBean.setLine_no(checknull(line_no));
					invoiceDetBean.setSord_no(checknull(sord_no));
					invoiceDetBean.setLine_no__sord(checknull(line_no__sord));
					invoiceDetBean.setExp_lev(checknull(exp_lev));
					invoiceDetBean.setItem_code__ord(checknull(item_code__ord));
					invoiceDetBean.setItem_code(checknull(item_code));
					invoiceDetBean.setLot_no(checknull(lot_no));
					invoiceDetBean.setLot_sl(checknull(lot_sl));
					invoiceDetBean.setQuantity__ord(checknull(quantity__ord));
					//		        	invDespDetMap.put("quantity", checknull(rs.getString("quantity")));
					invoiceDetBean.setLoc_code(checknull(loc_code));
					invoiceDetBean.setStatus(checknull(status));
					invoiceDetBean.setDesp_conv__qty_stduom(checknull(desp_quantity__stduom));
					invoiceDetBean.setDesp_unit__std(checknull(desp_unit__std));
					invoiceDetBean.setUnit(checknull(unit));
					invoiceDetBean.setDesp_quantity__stduom(checknull(sord_quantity__stduom));
					invoiceDetBean.setTax_class(checknull(tax_class));
					invoiceDetBean.setTax_chap(checknull(tax_chap));
					invoiceDetBean.setTax_env(checknull(tax_env));
					invoiceDetBean.setDiscount(checknull(discount));
					invoiceDetBean.setUnit__rate(checknull(unit__rate));
					invoiceDetBean.setSord_conv__qty_stduom(checknull(sord_conv__qty_stduom));
					invoiceDetBean.setConv__rtuom_stduom(checknull(conv__rtuom_stduom));
					invoiceDetBean.setSord_unit__std(checknull(sord_unit__std));
					//		        	invDespDetMap.put("sord_quantity__stduom", checknull(rs.getString("quantity__stduom")));
					invoiceDetBean.setSord_rate__stduom(checknull(sord_rate__stduom));
					invoiceDetBean.setRate(checknull(rate));
					//		        	invDespDetMap.put("quantity_real", checknull(rs.getString("quantity_real")));
					invoiceDetBean.setQuantity_inv(checknull(quantity_inv));
					invoiceDetBean.setInvoice_id(checknull(invoice_id));
					//		        	invDespDetMap.put("no_art", checknull(rs.getString("no_art")));
					invoiceDetBean.setRate__clg(checknull(rate__clg));
					invoiceDetBean.setDesp_rate__stduom(checknull(desp_rate__stduom));
					invoiceDetBean.setDisc_amt(checknull(disc_amt));
					invoiceDetBean.setCust_item__ref(checknull(cust_item__ref));
					invoiceDetBean.setDisc_schem_billback_amt(checknull(disc_schem_billback_amt));
					invoiceDetBean.setDisc_schem_offinv_amt(checknull(disc_schem_offinv_amt));
					invoiceDetBean.setAcc_code__item(lsAcctSchemedet);
					invoiceDetBean.setFin_scheme(lsFinscheme);
					invoiceDetBean.setSp1Comm(commissionMap.get("sp1Comm").toString());//Start Added by chandrashekar on 01-09-2016
					invoiceDetBean.setSp2Comm(commissionMap.get("sp2Comm").toString());//Start Added by chandrashekar on 01-09-2016
					invoiceDetBean.setSp3Comm(commissionMap.get("sp3Comm").toString());//Start Added by chandrashekar on 01-09-2016
					invoiceDetBean.setNetComm(commissionMap.get("netComm").toString());//Start Added by chandrashekar on 01-09-2016
					invoiceDetBean.setItemDescr(invTraceMap.get("item_descr"));
					invDetBeanMap.put(beanMapKey, invoiceDetBean);
					System.out.println("@@@@@1634 invoiceDetBean added into invDetBeanMap........["+invDetBeanMap+"]");
				}
				else
				{
					invoiceDetBean=(InvoiceDetBean) invDetBeanMap.get(beanMapKey);

					mTotDespQty=Double.parseDouble(quantity==null?"0.00":quantity)+invoiceDetBean.getmTotDespQty();
					mTotStdQty=Double.parseDouble(desp_quantity__stduom==null?"0.00":desp_quantity__stduom)+invoiceDetBean.getmTotStdQty();
					mNoArt=Integer.parseInt(no_art==null?"0":no_art)+invoiceDetBean.getmNoArt();
					mRealQty=(Double.parseDouble(quantity_real==null?"0.00":quantity_real)+invoiceDetBean.getmRealQty());					
					System.out.println("mTotDespQty:["+mTotDespQty+"]mTotStdQty:["+mTotStdQty+"]mNoArt:["+mNoArt+"]mRealQty:["+mRealQty+"]");
					
					invoiceDetBean.setmTotDespQty(mTotDespQty);//==null?"0.00":mTotDespQty);
					invoiceDetBean.setmTotStdQty(mTotStdQty);
					invoiceDetBean.setmNoArt(mNoArt);
					invoiceDetBean.setmRealQty(mRealQty);
					
					invDetBeanMap.put(beanMapKey, invoiceDetBean);
				}
			}// DESP_DET while loop END
			/**
			 * Set invDetBeanMap
			 * in mainInvDespDetMap
			 * */
			System.out.println("@@@@@@@@@@@1384:::before:mainArrayList.size()["+mainArrayList.size()+"]:::mainInvDespDetMap[["+mainInvDespDetMap+"]]::::::::invDetBeanMap[["+invDetBeanMap+"]]");

			mainArrayList.add(mainInvDespDetMap);
			mainArrayList.add(invDetBeanMap);
			System.out.println("@@@@@@@@@@@1388:::after:mainArrayList.size()["+mainArrayList.size()+"]"); 




			//	        mainInvDespDetMap.put(beanMapKey, invDetBeanMap);
			pstmt.close();
			pstmt=null;
			rs.close();
			rs=null;

		} catch (SQLException e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage()); 
			throw new ITMException(e); 
		} catch (ITMException e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage()); 
			throw new ITMException(e); 
		} catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage()); 
			throw new ITMException(e); 
		}

		//	    return mainInvDespDetMap;
		return mainArrayList;
	}

	private HashMap<String, String> getStockDetail(String itemCode, String despSiteCode, String locCode, String lotNo, String lotSl, Connection conn) throws ITMException, Exception
	{
		HashMap<String, String> stockMap=new HashMap<String, String>();
		String sql="";
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		String ld_mfg_date="",ld_exp_date="";
		// TODO Auto-generated method stub
		try
		{
			sql="select site_code__mfg, mfg_date, exp_date from stock where item_code =? and site_code = ?"
				+ " and loc_code=? and lot_no=? and lot_sl=?";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, itemCode);
			pstmt.setString(2, despSiteCode);
			pstmt.setString(3, locCode);
			pstmt.setString(4, lotNo);
			pstmt.setString(5, lotSl);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				//stockMap.put("ls_site_code__mfg", checknull(rs.getString("site_code__mfg")));
				//String ld_mfg_date =  rs.getString("mfg_date");
				ld_mfg_date=rs.getString("mfg_date");
				ld_exp_date=rs.getString("exp_date");
				if(ld_mfg_date!=null)
				{
					ld_mfg_date=genericUtility.getValidDateString(rs.getString("mfg_date"), genericUtility.getDBDateFormat(),
							genericUtility.getApplDateFormat());
				}
				if(ld_exp_date!=null)
				{
					ld_exp_date=genericUtility.getValidDateString(rs.getString("exp_date"), genericUtility.getDBDateFormat(),
							genericUtility.getApplDateFormat());
				}

				//Timestamp ld_exp_date =  rs.getTimestamp("exp_date");
				//System.out.println("@@@@@@@@@ ld_mfg_date["+ld_mfg_date+"]ld_exp_date["+ld_exp_date+"]");
				stockMap.put("ls_site_code__mfg", checknull(rs.getString("site_code__mfg")));
				stockMap.put("ld_mfg_date", ld_mfg_date);
				stockMap.put("ld_exp_date", ld_exp_date);
			}
			pstmt.close();
			pstmt=null;
			rs.close();
			rs=null;
		} catch (SQLException e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage()); 
			throw new ITMException(e); 
		}

		return stockMap;
	}

	private String getLineType(String sordNo, String sordNoLineno, String expLev, Connection conn) throws ITMException
	{
		String lsLineType="",sql="";
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		// TODO Auto-generated method stub
		try
		{
			sql="select nature from sorditem where sale_order =? and line_no    =? and exp_lev= ?";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, sordNo);
			pstmt.setString(2, sordNoLineno);
			pstmt.setString(3, expLev);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				lsLineType=checknull(rs.getString("nature"));
			}
			pstmt.close();
			pstmt=null;
			rs.close();
			rs=null;

		} catch (SQLException e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage()); 
			throw new ITMException(e); 
		}

		return lsLineType;
	}

	class InvoiceDetBean
	{
		private double mTotDespQty;
		private double mTotStdQty;
		private double mDescOffInvTotAmt;
		private double mDescbbTotAmt;
		private int mNoArt;
		private double mRealQty;
		private double mRealQtyConv;
		private String desp_id;		
		private String line_no;
		private String sord_no;
		private String line_no__sord;
		private String exp_lev;
		private String item_code__ord;
		private String item_code;
		private String lot_no;
		private String lot_sl;
		private String quantity__ord;
		private String quantity;
		private String loc_code;
		private String status;
		private String desp_conv__qty_stduom;
		private String desp_unit__std;
		private String unit;
		private String desp_quantity__stduom;
		private String tax_class;
		private String tax_chap;
		private String tax_env;
		private String discount;
		private String unit__rate;
		private String sord_conv__qty_stduom;
		private String sord_rate__stduom;
		private String rate;
		private String quantity_real;
		private String quantity_inv;
		private String invoice_id;
		private String no_art;
		private String rate__clg;
		private String desp_rate__stduom;
		private String disc_amt;
		private String cust_item__ref;
		private String disc_schem_billback_amt;
		private String disc_schem_offinv_amt;
		private String acc_code__item;
		private String fin_scheme;
		private String conv__rtuom_stduom;
		private String sord_unit__std;
		
		private String sp1Comm;
		private String sp2Comm;
		private String sp3Comm;
		private String netComm;
		private String itemDescr;
		
		public String getItemDescr()
        {
        	return itemDescr;
        }
		public void setItemDescr(String itemDescr)
        {
        	this.itemDescr = itemDescr;
        }
		public String getNetComm()
        {
        	return netComm;
        }
		public void setNetComm(String netComm)
        {
        	this.netComm = netComm;
        }
		public double getmTotDespQty()
		{
			return mTotDespQty;
		}
		public void setmTotDespQty(double mTotDespQty)
		{
			this.mTotDespQty = mTotDespQty;
		}
		public double getmTotStdQty()
		{
			return mTotStdQty;
		}
		public void setmTotStdQty(double mTotStdQty)
		{
			this.mTotStdQty = mTotStdQty;
		}
		public double getmDescOffInvTotAmt()
		{
			return mDescOffInvTotAmt;
		}
		public void setmDescOffInvTotAmt(double mDescOffInvTotAmt)
		{
			this.mDescOffInvTotAmt = mDescOffInvTotAmt;
		}
		public double getmDescbbTotAmt()
		{
			return mDescbbTotAmt;
		}
		public void setmDescbbTotAmt(double mDescbbTotAmt)
		{
			this.mDescbbTotAmt = mDescbbTotAmt;
		}
		public int getmNoArt()
		{
			return mNoArt;
		}
		public void setmNoArt(int mNoArt)
		{
			this.mNoArt = mNoArt;
		}
		public double getmRealQty()
		{
			return mRealQty;
		}
		public void setmRealQty(double mRealQty)
		{
			this.mRealQty = mRealQty;
		}
		public double getmRealQtyConv()
		{
			return mRealQtyConv;
		}
		public void setmRealQtyConv(double mRealQtyConv)
		{
			this.mRealQtyConv = mRealQtyConv;
		}	
		public String getDesp_id()
		{
			return desp_id;
		}
		public void setDesp_id(String desp_id)
		{
			this.desp_id = desp_id;
		}
		public String getLine_no()
		{
			return line_no;
		}
		public void setLine_no(String line_no)
		{
			this.line_no = line_no;
		}
		public String getSord_no()
		{
			return sord_no;
		}
		public void setSord_no(String sord_no)
		{
			this.sord_no = sord_no;
		}
		public String getLine_no__sord()
		{
			return line_no__sord;
		}
		public void setLine_no__sord(String line_no__sord)
		{
			this.line_no__sord = line_no__sord;
		}
		public String getExp_lev()
		{
			return exp_lev;
		}
		public void setExp_lev(String exp_lev)
		{
			this.exp_lev = exp_lev;
		}
		public String getItem_code__ord()
		{
			return item_code__ord;
		}
		public void setItem_code__ord(String item_code__ord)
		{
			this.item_code__ord = item_code__ord;
		}
		public String getItem_code()
		{
			return item_code;
		}
		public void setItem_code(String item_code)
		{
			this.item_code = item_code;
		}
		public String getLot_no()
		{
			return lot_no;
		}
		public void setLot_no(String lot_no)
		{
			this.lot_no = lot_no;
		}
		public String getLot_sl()
		{
			return lot_sl;
		}
		public void setLot_sl(String lot_sl)
		{
			this.lot_sl = lot_sl;
		}
		public String getQuantity__ord()
		{
			return quantity__ord;
		}
		public void setQuantity__ord(String quantity__ord)
		{
			this.quantity__ord = quantity__ord;
		}
		public String getQuantity()
		{
			return quantity;
		}
		public void setQuantity(String quantity)
		{
			this.quantity = quantity;
		}
		public String getLoc_code()
		{
			return loc_code;
		}
		public void setLoc_code(String loc_code)
		{
			this.loc_code = loc_code;
		}
		public String getStatus()
		{
			return status;
		}
		public void setStatus(String status)
		{
			this.status = status;
		}
		public String getDesp_conv__qty_stduom()
		{
			return desp_conv__qty_stduom;
		}
		public void setDesp_conv__qty_stduom(String desp_conv__qty_stduom)
		{
			this.desp_conv__qty_stduom = desp_conv__qty_stduom;
		}
		public String getDesp_unit__std()
		{
			return desp_unit__std;
		}
		public void setDesp_unit__std(String desp_unit__std)
		{
			this.desp_unit__std = desp_unit__std;
		}
		public String getUnit()
		{
			return unit;
		}
		public void setUnit(String unit)
		{
			this.unit = unit;
		}
		public String getDesp_quantity__stduom()
		{
			return desp_quantity__stduom;
		}
		public void setDesp_quantity__stduom(String desp_quantity__stduom)
		{
			this.desp_quantity__stduom = desp_quantity__stduom;
		}
		public String getTax_class()
		{
			return tax_class;
		}
		public void setTax_class(String tax_class)
		{
			this.tax_class = tax_class;
		}
		public String getTax_chap()
		{
			return tax_chap;
		}
		public void setTax_chap(String tax_chap)
		{
			this.tax_chap = tax_chap;
		}
		public String getTax_env()
		{
			return tax_env;
		}
		public void setTax_env(String tax_env)
		{
			this.tax_env = tax_env;
		}
		public String getDiscount()
		{
			return discount;
		}
		public void setDiscount(String discount)
		{
			this.discount = discount;
		}
		public String getUnit__rate()
		{
			return unit__rate;
		}
		public void setUnit__rate(String unit__rate)
		{
			this.unit__rate = unit__rate;
		}
		public String getSord_conv__qty_stduom()
		{
			return sord_conv__qty_stduom;
		}
		public void setSord_conv__qty_stduom(String sord_conv__qty_stduom)
		{
			this.sord_conv__qty_stduom = sord_conv__qty_stduom;
		}
		public String getSord_rate__stduom()
		{
			return sord_rate__stduom;
		}
		public void setSord_rate__stduom(String sord_rate__stduom)
		{
			this.sord_rate__stduom = sord_rate__stduom;
		}
		public String getRate()
		{
			return rate;
		}
		public void setRate(String rate)
		{
			this.rate = rate;
		}
		public String getQuantity_real()
		{
			return quantity_real;
		}
		public void setQuantity_real(String quantity_real)
		{
			this.quantity_real = quantity_real;
		}
		public String getQuantity_inv()
		{
			return quantity_inv;
		}
		public void setQuantity_inv(String quantity_inv)
		{
			this.quantity_inv = quantity_inv;
		}
		public String getInvoice_id()
		{
			return invoice_id;
		}
		public void setInvoice_id(String invoice_id)
		{
			this.invoice_id = invoice_id;
		}
		public String getNo_art()
		{
			return no_art;
		}
		public void setNo_art(String no_art)
		{
			this.no_art = no_art;
		}
		public String getRate__clg()
		{
			return rate__clg;
		}
		public void setRate__clg(String rate__clg)
		{
			this.rate__clg = rate__clg;
		}
		public String getDesp_rate__stduom()
		{
			return desp_rate__stduom;
		}
		public void setDesp_rate__stduom(String desp_rate__stduom)
		{
			this.desp_rate__stduom = desp_rate__stduom;
		}
		public String getDisc_amt()
		{
			return disc_amt;
		}
		public void setDisc_amt(String disc_amt)
		{
			this.disc_amt = disc_amt;
		}
		public String getCust_item__ref()
		{
			return cust_item__ref;
		}
		public void setCust_item__ref(String cust_item__ref)
		{
			this.cust_item__ref = cust_item__ref;
		}
		public String getDisc_schem_billback_amt()
		{
			return disc_schem_billback_amt;
		}
		public void setDisc_schem_billback_amt(String disc_schem_billback_amt)
		{
			this.disc_schem_billback_amt = disc_schem_billback_amt;
		}
		public String getDisc_schem_offinv_amt()
		{
			return disc_schem_offinv_amt;
		}
		public void setDisc_schem_offinv_amt(String disc_schem_offinv_amt)
		{
			this.disc_schem_offinv_amt = disc_schem_offinv_amt;
		}
		public String getAcc_code__item()
		{
			return acc_code__item;
		}
		public void setAcc_code__item(String acc_code__item)
		{
			this.acc_code__item = acc_code__item;
		}
		public String getFin_scheme()
		{
			return fin_scheme;
		}
		public void setFin_scheme(String fin_scheme)
		{
			this.fin_scheme = fin_scheme;
		}
		public String getConv__rtuom_stduom()
		{
			return conv__rtuom_stduom;
		}
		public void setConv__rtuom_stduom(String conv__rtuom_stduom)
		{
			this.conv__rtuom_stduom = conv__rtuom_stduom;
		}
		public String getSord_unit__std()
		{
			return sord_unit__std;
		}
		public void setSord_unit__std(String sord_unit__std)
		{
			this.sord_unit__std = sord_unit__std;
		}
		public String getSp1Comm()
        {
        	return sp1Comm;
        }
		public void setSp1Comm(String sp1Comm)
        {
        	this.sp1Comm = sp1Comm;
        }
		public String getSp2Comm()
        {
        	return sp2Comm;
        }
		public void setSp2Comm(String sp2Comm)
        {
        	this.sp2Comm = sp2Comm;
        }
		public String getSp3Comm()
        {
        	return sp3Comm;
        }
		public void setSp3Comm(String sp3Comm)
        {
        	this.sp3Comm = sp3Comm;
        }
	}
	/**
	 * 
	 * @param custCodeBill
	 * @param conn
	 * @return HashMap<String, String>
	 * @throws ITMException 
	 */
	private HashMap<String, String> getarCustomer(String custCodeBill, Connection conn) throws ITMException
	{
		HashMap<String, String> arCustomerMap=new HashMap<String, String>();
		String sql="";
		PreparedStatement pstmt=null;
		ResultSet rs=null;

		// TODO Auto-generated method stub

		try
		{
			sql="select acct_code__ar , cctr_code__ar from 	 customer where  cust_code = ?";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, custCodeBill);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				arCustomerMap.put("macctar", rs.getString("acct_code__ar"));
				arCustomerMap.put("mcctrar", rs.getString("cctr_code__ar"));
			}
			pstmt.close();
			pstmt=null;
			rs.close();
			rs=null;
		} catch (SQLException e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage()); 
			throw new ITMException(e); 
		}

		return arCustomerMap;
	}

	/**
	 * 
	 * @param itemSer
	 * @param conn
	 * @return HashMap<String, String>
	 * @throws ITMException 
	 */
	private HashMap<String, String> getdisItemser(String itemSer, Connection conn) throws ITMException
	{
		HashMap<String, String> disItemserMap=new HashMap<String, String>();
		String sql="";
		PreparedStatement pstmt=null;
		ResultSet rs=null;

		// TODO Auto-generated method stub

		try
		{
			sql="select acct_code__dis, cctr_code__dis from itemser where item_ser = ?";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, itemSer);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				disItemserMap.put("lsacct_dis", checknull(rs.getString("acct_code__dis")));
				disItemserMap.put("lscctr_dis", checknull(rs.getString("cctr_code__dis")));
			}
			pstmt.close();
			pstmt=null;
			rs.close();
			rs=null;
		} catch (SQLException e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage()); 
			throw new ITMException(e); 
		}

		return disItemserMap;
	}

	/**
	 * 
	 * @param sordId
	 * @param itemCode
	 * @param conn
	 * @return String
	 * @throws ITMException 
	 */
	private String getlsCustitemRef(String sordId, String itemCode, Connection conn) throws ITMException
	{ 
		String lsCustitemRef="",sql="";
		// TODO Auto-generated method stub

		PreparedStatement pstmt=null;
		ResultSet rs=null;

		try
		{
			sql="select item_code__ref from customeritem where item_code = ? and cust_code = (select cust_code from sorder where sale_order  =?)";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, itemCode);
			pstmt.setString(2, sordId);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				lsCustitemRef=checknull(rs.getString("item_code__ref"));
			}
			pstmt.close();
			pstmt=null;
			rs.close();
			rs=null;
		} catch (SQLException e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage()); 
			throw new ITMException(e); 
		}

		return lsCustitemRef;
	}

	/**
	 * 
	 * @param sorderNo
	 * @param conn
	 * @return HashMap<String, String>
	 * @throws ITMException 
	 */
	private HashMap<String, String> getsorderSal(String sorderNo, Connection conn) throws ITMException
	{
		HashMap<String, String> sorderSalMap=new HashMap<String, String>();
		String sql="";
		PreparedStatement pstmt=null;
		ResultSet rs=null;

		// TODO Auto-generated method stub
		try
		{
			sql="select acct_code__sal, cctr_code__sal, market_reg, order_type from sorder where sale_order =?";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, sorderNo);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				sorderSalMap.put("lsacct_sal", rs.getString("acct_code__sal"));
				sorderSalMap.put("lscctr_sal", rs.getString("cctr_code__sal"));
				sorderSalMap.put("ls_market_reg", rs.getString("market_reg"));
				sorderSalMap.put("ls_ordertype", rs.getString("order_type"));
			}
			pstmt.close();
			pstmt=null;
			rs.close();
			rs=null;
		} catch (SQLException e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage()); 
			throw new ITMException(e); 
		}

		return sorderSalMap;
	}

	/**
	 * 
	 * @param sordId
	 * @param conn
	 * @return String
	 * @throws ITMException 
	 */
	private String getLsFinscheme(String sordId, Connection conn) throws ITMException
	{
		String lsFinscheme="",sql="";
		// TODO Auto-generated method stub

		PreparedStatement pstmt=null;
		ResultSet rs=null;

		try
		{
			sql="select fin_scheme from sorder where sale_order = ? ";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, sordId);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				lsFinscheme=checknull(rs.getString("fin_scheme"));
			}
			pstmt.close();
			pstmt=null;
			rs.close();
			rs=null;
		} catch (SQLException e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage()); 
			throw new ITMException(e); 
		}

		return lsFinscheme;
	}

	/**
	 * 
	 * @param sorderNo
	 * @param conn
	 * @return HashMap<String, String>
	 * @throws ITMException 
	 */
	private HashMap<String, String> getAcctSchemehdr(String sorderNo, Connection conn) throws ITMException
	{
		HashMap<String, String> acctSchemehdrMap=new HashMap<String, String>();
		String sql="";
		PreparedStatement pstmt=null;
		ResultSet rs=null;

		// TODO Auto-generated method stub
		try
		{
			sql="select a.acc_code__order,a.acct_code__pr,a.cctr_code__pr from bom a,sorder_scheme b where a.bom_code = b.scheme_code"
				+ " and b.tran_id = ?";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, sorderNo);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				acctSchemehdrMap.put("ls_acct_schemehdr", rs.getString("acc_code__order"));
				acctSchemehdrMap.put("ls_acct_code__pr", rs.getString("acct_code__pr"));
				acctSchemehdrMap.put("ls_cctr_code__pr", rs.getString("cctr_code__pr"));
			}
			pstmt.close();
			pstmt=null;
			rs.close();
			rs=null;
		} catch (SQLException e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage()); 
			throw new ITMException(e); 
		}

		return acctSchemehdrMap;
	}

	/**<NEWLINE>
	 * 
	 * @param sordId
	 * @param lineNoSord
	 * @param conn
	 * @return String
	 * @throws ITMException 
	 */
	private String getLsAcctSchemedet(String sordId, String lineNoSord, Connection conn) throws ITMException
	{
		String lsAcctSchemedet="",sql="";
		// TODO Auto-generated method stub

		PreparedStatement pstmt=null;
		ResultSet rs=null;

		try
		{
			sql="select a.acc_code__item from bom a,sorderdet_scheme b where a.bom_code = b.scheme_code "
				+ "and tran_id = ? and line_no_form = ? ";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, sordId);
			pstmt.setString(2, lineNoSord);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				lsAcctSchemedet=checknull(rs.getString("acc_code__item"));
			}
			pstmt.close();
			pstmt=null;
			rs.close();
			rs=null;
		} catch (SQLException e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage()); 
			throw new ITMException(e); 
		}

		return lsAcctSchemedet;
	}

	/**
	 * Save XML data
	 * @param siteCode
	 * @param xmlString
	 * @param xtraParams
	 * @param conn
	 * @return String
	 * @throws ITMException
	 */
	private String saveData(String siteCode, String xmlString, String xtraParams, Connection conn) throws ITMException
	{
		//System.out.println("saving data...........");
		InitialContext ctx = null;
		String retString = null;
		MasterStatefulLocal masterStateful = null; // for ejb3
		ibase.utility.UserInfoBean userInfo = new UserInfoBean();
		String chgUser = "", chgTerm = "";
		String loginCode = "", loginEmpCode = "", loginSiteCode = "";
		E12GenericUtility genericUtility = new E12GenericUtility();
		try
		{
			AppConnectParm appConnect = new AppConnectParm();
			ctx = new InitialContext(appConnect.getProperty());
			masterStateful = (MasterStatefulLocal) ctx.lookup("ibase/MasterStatefulEJB/local");
			//System.out.println("-----------masterStateful------- " + masterStateful);
			/* Changed By PriyankaC on 05JAN18
			 * String[] authencate = new String[2];
			authencate[0] = "";
			authencate[1] = ""; 
			*/
			
			//System.out.println("xmlString to masterstateful [" + xmlString + "]");
			//	userInfo = new ibase.utility.UserInfoBean();
			//System.out.println("xtraParams>>>>" + xtraParams);
			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "chgUser");
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "chgTerm");
			loginCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			loginEmpCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			userInfo.setEmpCode(loginEmpCode);
			userInfo.setRemoteHost(chgTerm);
			userInfo.setSiteCode(loginSiteCode);
			userInfo.setLoginCode(loginCode);
			userInfo.setEntityCode(loginEmpCode);
			//Added By PriyankaC on 05JAN18 [START]
			String[] authencate = new String[2];
			authencate[0] = loginCode;
			authencate[1] = "";
			//Added By PriyankaC on 05JAN18 [END]
			/*System.out.println("userInfo>>>>>" + userInfo);
			System.out.println("chgUser :" + chgUser);
			System.out.println("chgTerm :" + chgTerm);
			System.out.println("loginCode :" + loginCode);
			System.out.println("loginEmpCode :" + loginEmpCode);*/

			retString = masterStateful.processRequest(authencate, siteCode, true, xmlString, true, conn);
			//retString = masterStateful.processRequest(userInfo, xmlString, true, conn);
			//System.out.println("--retString - -" + retString);
		} catch (ITMException e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage()); 
			throw new ITMException(e); 
		} catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception :CreateDistOrder :saveData :==>");
			throw new ITMException(e);
		}
		return retString;
	}

	/**
	 * Initialise and generate log file
	 * @param fileName
	 * @return String
	 * @throws ITMException 
	 */
	private String intializingLog(String fileName) throws ITMException
	{
		String log="intializingLog_Failed";
		String strToWrite = "";
		String currTime = null;
		try{
			SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
			try
			{
				currTime = sdf1.format(new Timestamp(System.currentTimeMillis())).toString();
				currTime = currTime.replaceAll("-","");
				calendar.setTime(new java.sql.Timestamp(System.currentTimeMillis()));
				fileName = fileName+currTime+calendar.get(Calendar.HOUR)+""+calendar.get(Calendar.MINUTE)+".log";
				fos1 = new FileOutputStream(CommonConstants.JBOSSHOME + File.separator +"EDI"+File.separator+fileName);

			}
			catch(Exception e)
			{
				e.printStackTrace();
				System.out.println("Exception ::"+ e.getMessage()); 
				throw new ITMException(e); 
			}
			startDate = new java.util.Date(System.currentTimeMillis());
			calendar.setTime(new java.sql.Timestamp(System.currentTimeMillis()));
			startDateStr = sdf1.format(startDate)+" "+calendar.get(Calendar.HOUR)+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND);
			//fos1.write(("Fetching Records Started At " + startDateStr +"\r\n").getBytes());

		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage()); 
			throw new ITMException(e); 
		}

		log ="intializingLog_Successesfull";
		return log;
	}
	/**
	 * Check null String
	 * @param inputStr
	 * @return String
	 */
	private String checknull(String inputStr)
	{

		// TODO Auto-generated method stub
		if(inputStr==null)
		{
			inputStr="";
		}
		return inputStr;
	}
	private String checkDoubleNull(String input) {
		if (input == null || input.trim().length() == 0 ) 
		{
		input = "0";
		}
		return input;
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
	private double checkDouble(Double double1)
	{
		if (double1 == null) 
		{
			double1 = 0.0;
		}
		return double1;
		
	}
	public HashMap calcCommission(HashMap calcCommission,String lineNo ,Connection conn) throws ITMException
	{
		String sql="" ,currCode = "",baseCurrency = "",errorCode="",errString="";
		String commPercOn="",currCodeComm="",commPercOn1="",currCodeComm1="",commPercOn2="",currCodeComm2="",dlvTerm="";
		String commHdr = "N",commPercOnDet1="",commPercOnDet2="",commPercOnDet3="",itemCode="",siteCode="",finEntity="",insReqd="",frtReqd="";
		String custCode="",itemSer="",salesPers="",salesPers1="",salesPers2="",priceListDate="";
		String saleOrder="";
		String despId="";
		double exchRate=0,commPerc=0,exchRateComm=0,commPerc1=0,exchRateComm1=0,commPerc2=0,exchRateComm2=0,commPercDet1=0,commPercDet2=0,commPercDet3=0;
		double taxAmt=0,netAmt=0,qtyStduom=0,rateStduom=0,frtAmt=0,exchFrtRate=0,insAmt=0,exchInsRate=0,fobAmt=0,netComm=0,ordPrice=0;
		double commPerUnit=0,commBl1=0,commBl2=0,commBl3=0,commPerUnit1=0.0,commPerUnit2=0.0,commPerUnit3=0.0,qtyComm=0.0,qtyComm1=0.0,qtyComm2=0.0,qtyComm3=0.0;
		double sp1Comm=0.0,sp2Comm=0.0,sp3Comm=0.0,totalCommBl=0.0,commAmt=0.0,commAmt1=0.0,commAmt2=0.0,commAmt3=0.0;
		double baseAmtComm=0.0,baseAmtComm1=0.0,baseAmtComm2=0.0,baseAmtComm3=0.0,assessAmt=0.0,asesAmtComm=0.0,asesAmtComm1=0.0,asesAmtComm2=0.0,asesAmtComm3=0.0;
		double salesAmt=0.0,taxAmtComm=0.0,taxAmtComm1=0.0,taxAmtComm2=0.0,taxAmtComm3=0.0,fobComm=0.0,fobComm1=0.0,fobComm2=0.0,fobComm3=0.0;
	    double fobQtyComm=0.0,fobQtyComm1=0.0,fobQtyComm2=0.0,fobQtyComm3=0.0,amtQtyComm=0.0,amtQtyComm1=0.0,amtQtyComm2=0.0,amtQtyComm3=0.0;
	    double taxAmtHdr=0.0,taxAmtDet=0.0,netAmtHdr=0.0,netAmtDet=0.0;
	    
	    String ls_sitecd_d="",ls_currcd_d="",ls_currcd_frt_d="",ls_currcd_ins_d="",ls_curr_code__base="";
		double lc_exchrt_d=0.0,lc_exchrt_frt_d=0.0,lc_exchrt_ins_d=0.0,lc_exch_rate=0.0;
		double lc_exchrt=0.0;
		String ldt_despdt="";
		Timestamp despdt = null;
		Date plDate=null;
		PreparedStatement pstmt = null ;
		ResultSet rs = null;
		HashMap commissionMap = null;
		HashMap commPercMap = null;
		HashMap commPercSalesMap=null;
		String sOrdLineNo="";//added 	by nandkumar gadkari on 15/04/19
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		try
		{
			commissionMap = new HashMap();
			saleOrder=(String) calcCommission.get("sord_no");
			// added by nandkumar gadkari on 15/04/19--------------start-------------------
			sOrdLineNo=(String) calcCommission.get("line_no__sord");
			sOrdLineNo="   "+checknull(sOrdLineNo).trim();
			sOrdLineNo=sOrdLineNo.substring(sOrdLineNo.length()-3, sOrdLineNo.length());
			// added by nandkumar gadkari on 15/04/19--------------end-------------------
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
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
				pstmt.setString(2,sOrdLineNo);//desptch line no removed and sorder line set by nandkumar gadkari on 15/04/19
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
				/*System.out.println(">>>>>> in detail commPercOn:"+commPercOn+"   "+commPercOn1+"     "+commPercOn2);
				System.out.println(">>>>>> in detail commPerc:"+commPerc+"   "+commPerc1+"     "+commPerc2);*/
			}
			despId=(String) calcCommission.get("desp_id");
			
			if(despId !=null && despId.trim().length()>0)
			{
				
				sql="select desp_date,site_code,curr_code,curr_code__frt,curr_code__ins,"+
						"exch_rate,exch_rate__frt,exch_rate__ins" +
		  				" from despatch	where desp_id = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,despId);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						//changed by Pavan R on 1jun2k18 for timestamp format to applDate format to handle the unparseble exception while getDailyExchRateSellBuy in fincommon  						  					
						//ldt_despdt =rs.getString("desp_date");
						despdt =rs.getTimestamp("desp_date");
						//Pavan R end
						ls_sitecd_d =rs.getString("site_code");
						ls_currcd_d =rs.getString("curr_code");
						ls_currcd_frt_d   =rs.getString("curr_code__frt");
						ls_currcd_ins_d   =rs.getString("curr_code__ins");
						lc_exchrt_d   =rs.getDouble("exch_rate");
						lc_exchrt_frt_d = rs.getDouble("exch_rate__frt");
						lc_exchrt_ins_d = rs.getDouble("exch_rate__ins");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					ldt_despdt = sdf.format(despdt);
					
					sql="select curr_code  	from finent f, site s" +
							"	where f.fin_entity = s.fin_entity	and	s.site_code  = ? ";		
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,ls_sitecd_d);	
					rs = pstmt.executeQuery();
					if(rs.next())
					{  
						ls_curr_code__base =  rs.getString(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if(calcCommission.get("exch_rate")!= null)
					{
						lc_exchrt_d=checkDouble((Double) calcCommission.get("exch_rate"));
					}
					if(lc_exchrt_d>0)
					{
						lc_exchrt_d=lc_exchrt_d;
					}
					if(lc_exchrt_d==0)//// For Order Exch rate
					{
						lc_exchrt_d=finCommon.getDailyExchRateSellBuy(ls_currcd_d, ls_curr_code__base, ls_sitecd_d, ldt_despdt, "S", conn);
						if(lc_exchrt_d>0)
						{
							lc_exch_rate = lc_exchrt_d;
						}
					}else
					{
						lc_exch_rate = lc_exchrt_d;
					}
				
					if(ls_currcd_frt_d.equalsIgnoreCase(ls_currcd_d))// For Frt Exch rate
					{
						exchFrtRate = lc_exch_rate;
					}else if (lc_exchrt_frt_d==0)
					{
						lc_exchrt_frt_d=finCommon.getDailyExchRateSellBuy(ls_currcd_frt_d, ls_curr_code__base, ls_sitecd_d, ldt_despdt, "S", conn);
						if(lc_exchrt_frt_d>0)
						{
							exchFrtRate = lc_exchrt_frt_d;
						}
					}else
					{
						exchFrtRate = lc_exchrt_frt_d;
					}
					
					if(ls_currcd_ins_d.equalsIgnoreCase(ls_currcd_d)) // For Ins Exch rate
					{
						exchInsRate = lc_exch_rate;
					}else if(lc_exchrt_ins_d==0) 
					{
	
						lc_exchrt_ins_d = finCommon.getDailyExchRateSellBuy(ls_currcd_ins_d, ls_curr_code__base, ls_sitecd_d, ldt_despdt, "S", conn);
	
						if (lc_exchrt_ins_d > 0)
						{
							exchInsRate = lc_exchrt_ins_d;
						}
					}else
					{
						exchInsRate = lc_exchrt_ins_d;
					}
					
					if (currCodeComm != null && currCodeComm.trim().length()>0 ) // For Comm_1 Exch rate
					{
						if (currCodeComm.equalsIgnoreCase(ls_currcd_d))
						{
							exchRateComm = lc_exch_rate;
						} else
						{
							lc_exchrt = finCommon.getDailyExchRateSellBuy(currCodeComm, ls_curr_code__base, ls_sitecd_d, ldt_despdt, "S", conn);
							if (lc_exchrt > 0)
							{
								exchRateComm = lc_exchrt;
							}
						}
					}
					lc_exch_rate=0;	
					if (currCodeComm1 != null && currCodeComm1.trim().length()>0 ) // For Comm_2 Exch rate
					{
						if (currCodeComm1.equalsIgnoreCase(ls_currcd_d))
						{
							exchRateComm1 = lc_exch_rate;
						} else
						{
							lc_exchrt = finCommon.getDailyExchRateSellBuy(currCodeComm1, ls_curr_code__base, ls_sitecd_d, ldt_despdt, "S", conn);
							if (lc_exchrt > 0)
							{
								exchRateComm1 = lc_exchrt;
							}
						}
					}
					lc_exch_rate=0;	
					if (currCodeComm2 != null && currCodeComm2.trim().length()>0 ) // For Comm_2 Exch rate
					{
						if (currCodeComm2.equalsIgnoreCase(ls_currcd_d))
						{
							exchRateComm2 = lc_exch_rate;
						} else
						{
							lc_exchrt = finCommon.getDailyExchRateSellBuy(currCodeComm2, ls_curr_code__base, ls_sitecd_d, ldt_despdt, "S", conn);
							if (lc_exchrt > 0)
							{
								exchRateComm2 = lc_exchrt;
							}
						}
					}
					currCode = ls_currcd_d;
				
			}
			
			ls_curr_code__base = currCode;
			if (lc_exch_rate==0)
			{
				lc_exch_rate = 1;
			}
			if (exchRate==0)
			{
				exchRate = 1;
			}
			if (exchFrtRate==0)
			{
				exchFrtRate = 1;
			}
			if (exchInsRate==0)
			{
				exchInsRate = 1;
			}
			currCode=checknull(currCode);
			commPercOn=checknull(commPercOn);
			commPercOn1=checknull(commPercOn1);
			commPercOn2=checknull(commPercOn2);
			currCodeComm=checknull(currCodeComm);
			currCodeComm1=checknull(currCodeComm1);
			currCodeComm2=checknull(currCodeComm2);
			exchRateComm=(exchRateComm==0)?1:exchRateComm;
			exchRateComm1=(exchRateComm1==0)?1:exchRateComm1;
			exchRateComm2=(exchRateComm2==0)?1:exchRateComm2;
			salesPers=checknull(salesPers);
			salesPers1=checknull(salesPers1);
			salesPers2=checknull(salesPers2);
			//lineNo=checknull(lineNo);
			
			if(despId== null || despId.trim().length()==0)
			{
				if ("Y".equalsIgnoreCase(commHdr))
				{
					netAmt = netAmtHdr;
					taxAmt = taxAmtHdr;
				} else
				{
					netAmt = netAmtDet;
					taxAmt = taxAmtDet;
				}
			}else
			{
				netAmt = Double.parseDouble(calcCommission.get("net_amt").toString());
				taxAmt =  Double.parseDouble(calcCommission.get("tax_amt").toString());
			}
			/*if(lineNo.trim().length()>0)
			{
				if(despId==null || despId.trim().length()==0)
				{
					
				}else
				{
					if(netAmt<=0 && Double.parseDouble(calcCommission.get("desp_rate__stduom").toString()) >0)
					{
						//errString = itmDBAccessLocal.getErrorString("","''VTCOMMERR''","");
						errString = itmDBAccessLocal.getErrorString("", "VTCOMMERR", "", "", conn);//removed '' from error msg by nandkumar gadkari on 15/04/19
						commPercMap.put("errorStr", errString);
						return commPercMap;
					}
				}
				
			}else
			{
				if(netAmt <= 0)
				{
					//errString = itmDBAccessLocal.getErrorString("","''VTCOMMERR''","");
					errString = itmDBAccessLocal.getErrorString("", "VTCOMMERR", "", "", conn);//removed '' from error msg by nandkumar gadkari on 15/04/19
					commPercMap.put("errorStr", errString);
					return commPercMap;

				}
			}*///COMMENTED BY NANDKUMAR GADKARI ON 16/04/19 FOR GETTING  VTCOMMERR ERROR IF 100 % DISCOUNT ON ORDER
			
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
			
			/*System.out.println("netAmt>>>"+netAmt);
			System.out.println("exchRate>>>"+exchRate);*/
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
			   // System.out.println(">>>>>>qty for detail");
				sql="SELECT quantity__stduom, rate__stduom, item_code FROM  despatchdet where desp_id  =? and	line_no	= ?";		
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,despId);	
				pstmt.setString(2,lineNo.trim());	
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
						//errString = itmDBAccessLocal.getErrorString("","VTNOPERC","");
						errString = itmDBAccessLocal.getErrorString("", "VTNOPERC", "", "", conn);
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
						//errString = itmDBAccessLocal.getErrorString("","VTNOPERC","");
						errString = itmDBAccessLocal.getErrorString("", "VTNOPERC", "", "", conn);
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
				//System.out.println(">>>>>");
				sql="SELECT SUM(quantity__stduom) AS quantity__stduom FROM  FROM  despatchdet where desp_id  =? ";		
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,despId);	
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
				pstmt.setString(2,sOrdLineNo);//desptch line no removed and sorder line set by nandkumar gadkari on 15/04/19
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
				pstmt.setString(2,sOrdLineNo);//desptch line no removed and sorder line set by nandkumar gadkari on 15/04/19
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
			commissionMap.put("sp3Comm", sp3Comm);
			commissionMap.put("netComm", netComm);
			//System.out.println("commissionMap:::"+commissionMap.toString());
		}
		catch(Exception e)
		{
			System.out.println("Exception ::calcCommission:"+e);
			throw new ITMException(e);
		}
		return commissionMap;
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
					//	System.out.println(">>>>>>>>>>>>>>>defaultPriceList:"+defaultPriceList);
						if("NULLFOUND".equalsIgnoreCase(defaultPriceList) || defaultPriceList == null || defaultPriceList.trim().length()==0)
						{
							//errString = itmDBAccessLocal.getErrorString("","VTNOPL","Missing DISPARM Environment Variable: BASE_PRICE_LIST");
							errString = itmDBAccessLocal.getErrorString("", "VTNOPL", "","Missing DISPARM Environment Variable: BASE_PRICE_LIST", conn);							
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
						// System.out.println(">>>>>>>>>>>>>>>defaultPriceList:"+defaultPriceList);
						 if("NULLFOUND".equalsIgnoreCase(defaultPriceList) || defaultPriceList == null || defaultPriceList.trim().length()==0)
						 {
							 //errString = itmDBAccessLocal.getErrorString("","VTNOPL","");
							 errString = itmDBAccessLocal.getErrorString("", "VTNOPL", "", "", conn);
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
					// System.out.println(">>>>>>>>>>>>>>>defaultPriceList:"+defaultPriceList);
					 if(defaultPriceList.equals("NULLFOUND") || defaultPriceList==null || defaultPriceList.trim().length()==0 )
					 {
						 // = 'VTNOPL' + "~t" + " Missing DISPARM Environment Variable: BASE_PRICE_LIST ";
						 //errString = itmDBAccessLocal.getErrorString("","VTNOPL","");
						 errString = itmDBAccessLocal.getErrorString("", "VTNOPL", "", "", conn);
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
				// System.out.println(">>>>>>>>>>>>>>>defaultPriceList:"+defaultPriceList);
				 if(defaultPriceList.equals("NULLFOUND") || defaultPriceList == null || defaultPriceList.trim().length()==0 )
				 {
					 //errString = itmDBAccessLocal.getErrorString("","VTNOPL","");
					 errString = itmDBAccessLocal.getErrorString("", "VTNOPL", "", "", conn);
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
				//errString = itmDBAccessLocal.getErrorString("","VTNOPL","");
				errString = itmDBAccessLocal.getErrorString("", "VTNOPL", "", "", conn);
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
	private String checkNullDouble(String input)	
	{
		if (input == null || input.trim().length()==0 )
		{
			input="0";
		}
		return input;
	}
	//Added  BY PriyanaC on 02JAN2018
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
		System.out.println(" actVal [" + actVal + "] integer [" + strValue.substring(0,strValue.indexOf(".") +1) + "]");
		System.out.println("decimal [" + strValue.substring(strValue.indexOf(".") + 1, strValue.indexOf(".") + prec + 1) + "]");
		retVal = Double.parseDouble( strValue.substring(0,strValue.indexOf(".") +1) + strValue.substring(strValue.indexOf(".") + 1, strValue.indexOf(".") + prec + 1));
		System.out.println("rounded value [" + retVal + "]");
		
		return retVal;
	}
	//Added  BY PriyanaC on 02JAN2018

}
