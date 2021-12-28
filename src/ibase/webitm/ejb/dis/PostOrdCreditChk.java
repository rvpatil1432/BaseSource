package ibase.webitm.ejb.dis;

import ibase.planner.utility.ITMException;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.utility.EMail;
import ibase.webitm.ejb.fin.FinCommon;
//import ibase.utility.MailInfo;
//import ibase.webitm.ejb.ITMDBAccessEJB;
//import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.sys.UtilMethods;
//import ibase.webitm.utility.GenericUtility;

import java.io.File;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;
//import org.w3c.dom.NodeList;

//import bsh.ParseException;

public class PostOrdCreditChk 
{
	FinCommon fincommon = new FinCommon();
	//public HashMap CreditCheck (HashMap paramMap, Connection conn) throws RemoteException,ITMException//Amit 27/10/04. This function is a copy of gf_credit_check function.
	//public String CreditCheck (HashMap paramMap, Connection conn) throws RemoteException,ITMException
	public ArrayList<String> CreditCheck (HashMap paramMap, Connection conn) throws RemoteException,ITMException//Amit 27/10/04. This function is a copy of gf_credit_check function.
	{
		
		int mCount=0, mChkDays=0, llIgnGays=0, llSerCount=0, llPolicyNo=0 ;
		String errString = " ", mStat="", lsCreditTerm="", lsVarValue="", lsItemSerCrPolicy="", lsItemSer="", lsConsiderPbo="", lsOrdNewPrd="" ;
		String lsAcctPrd="", lsPriceList="", lsItemCode="", lsCrPolicy="",lsInvCallFr="",lsItemSerOrig="" ;
		String lsOrderType="", lsLocType="", lsLink="",lsItemSerCommon="",lsItemSerDescr="", lsContactCode="" ;
		double lcRate = 0, lcPickRate = 0, lcOrderValue= 0, lcAdNetAmtOrig= 0, lcAdvAmt= 0, lcAdvVal= 0	, lcAdvPer = 0;
		double mIgnCr = 0, mTotalOsAmt = 0, mTotOsAmt = 0, mCrLimit = 0, mOsAmt = 0, lcNetAdv = 0, lcPendOrd = 0, lcTaxAmt = 0 ,lcDrAmtBase = 0 ,lcCrAmtBase = 0;
		double lcBusiLimit = 0, lcNetAmt = 0, lcMinInvAmt = 0;
		double lcCheckAmt=0, totOsAmt=0;	//, ld_old_ad_net_amt , lc_used_amt
		Timestamp mDueDate=null, ldtFromDate=null, ldtToDate=null, ldToday=null;
		String lsSalesPers="",lsSalesPers1="", lsTaxCode="", lsCrTerm="", lsAdvCrTerm="";
		String lsSql="" ,lsToken ="",lsString ="",taxCode="" ,itemSer="" ,itemSerCrpolicy ="",lsOthSeries="" ;  //for instr and length replacement  ** kiran 28/06/05
		String lsReasCode = "", lsReasDetail = "";
		String lsPolicySql="", lsPolicyInput="", lsPolicyResult="", lsPolicyCondition="" , lsResult ="";
		String lsStr="" , lsStr1="" , lsStr2="" , lsCustName="" , lsSlpersName="" , lsContactName="", lsStr3="";
		String lsCustCodeBill = "",lsCustNameBillTo ="",lsCustNameSoldTo= ""; // Added By PriyankaC on 03June2019
		String lsValue="" , lsFail="", lsStatus="";//, lsMail[];
		double lcCustCrlmt = 0, lcContCrlmt = 0	,lcPendOrdSord = 0;
		ArrayList PolicyList = null;
		int llCnt = 0;
		HashMap s_pass1 = null, returnMap = null;
		ArrayList mailList = null, failedPolicyList = null, asCheckList = null;
		double totAmt = 0,adjAmt=0;//Added by Rohini T
		String tranSer="",refNo="",jsonStr="";
		int updCnt=0;
		String asCustCodeBil="",asCustCodeSoldTo="";
		String asItemSer="";
		double adNetAmt=0;
		String asRunOpt="";
		String asSorder="";
		Timestamp adtTranDate = null;
		String refDateStr;     //added by manish mhatre on 28/6/2019
		String asSiteCode="";
		String asApplyTime="";
		String asDespId="";
		//String asCheck[] // This has to be returned to the calling routine
		String arrStr[], arrSql[];
		Timestamp ldDateTime = null,ldtResultResult=null;
		//FinCommon finCommon = null;
		DistCommon disCommon = null;
		E12GenericUtility genericUtility = null;
		//ITMDBAccessEJB itmDBAccessEJB = null;
		PreparedStatement pstmt = null,pstmt1 = null, pstmtInsert = null, pstmtUpd = null;
		ResultSet rs = null, rs1 = null;

		UtilMethods utilMethods = new UtilMethods();
		String ordTypeNewPrd="",sql="";
		int ctr=0;
		String lsMail[]=null,asCheck[]=null,gs_inv_call_fr="";
		//E12GenericUtility e12GenericUtility = new E12GenericUtility();		
		ArrayList<String> retArrayList = new ArrayList<String>();
		long startTime = 0, endTime = 0, totalTime = 0, totalHrs = 0, totlMts = 0, totSecs = 0; // Added
		String crChkReq="",ordType="";//added by nandkumar gakari on 10/12/19-
		JSONArray flogicDetails=null;//Modified by Rohini T on 26/04/2021
		JSONObject tempObj=null,jsonObjflogic=null;
		try
		{
			startTime = System.currentTimeMillis();
			Timestamp currDate = new java.sql.Timestamp(System.currentTimeMillis());
			mailList = new ArrayList();
			asCheckList = new ArrayList();
			failedPolicyList = new ArrayList();// This has to be returned to the calling routine part of the returnMap
			asCustCodeBil = (String) paramMap.get("as_cust_code_bil");
			asCustCodeSoldTo = (String)paramMap.get("as_cust_code_sold_to");
			asItemSer  = (String) paramMap.get("as_item_ser");
			adNetAmt = Double.parseDouble((String) paramMap.get("ad_net_amt"));
			//asRunOpt = (String) paramMap.get("as_runopt"); 19-Mar-2016 not used
			asSorder = (String) paramMap.get("as_sorder");
			adtTranDate = (Timestamp) paramMap.get("adt_tran_date");
			asSiteCode = (String) paramMap.get("as_site_code");
			asApplyTime = (String) paramMap.get("as_apply_time");
			asDespId = (String) paramMap.get("as_despid");

			returnMap = new HashMap();

			//finCommon = new FinCommon();
			disCommon = new DistCommon();
			genericUtility = new E12GenericUtility();
	//		itmDBAccessEJB = new ITMDBAccessEJB();
			
			
			//added by manish mhatre on 28-6-2019 
			
			/*Calendar currentDate = Calendar.getInstance();*/
			SimpleDateFormat sdf;
			SimpleDateFormat simpleDateFormat = null;
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			refDateStr = sdf.format(adtTranDate.getTime());
			
			//end manish mhatre
			
			/// new product order type should not be consider.
			/// amish 01-10-03
			//added by nandkumar gakari on 10/12/19--------------start-----------------------
			lsSql = "select order_type from sorder where sale_order = ?";
			pstmt = conn.prepareStatement(lsSql);
			pstmt.setString(1,asSorder);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				ordType = rs.getString("order_type");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			lsSql = "select cr_chk__req  from sordertype where order_type =  ?" ;
			pstmt = conn.prepareStatement(lsSql);
			pstmt.setString(1,ordType);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				crChkReq = rs.getString("cr_chk__req");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			crChkReq = crChkReq== null || crChkReq.trim().length() == 0 ? "Y" : crChkReq;
			if("Y".equalsIgnoreCase(crChkReq))
			{
				//added by nandkumar gakari on 10/12/19--------------end-----------------------
				lsOrdNewPrd = disCommon.getDisparams("999999","ORD_TYPE_NEWPRD",conn);
				if ("NULLFOUND".equals(lsOrdNewPrd) || lsOrdNewPrd.trim().length() == 0 )
				{
					ordTypeNewPrd = "FN";
				}
				ordTypeNewPrd = ordTypeNewPrd.trim();
				//Added By PriyankaC on 03June2019 [Start]
				lsSql = "select cust_name from customer where cust_code = ?";
				pstmt = conn.prepareStatement(lsSql);
				pstmt.setString(1,asCustCodeBil);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					lsCustNameBillTo = rs.getString("cust_name");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
				lsSql = "select cust_name from customer where cust_code =  ?";
				pstmt = conn.prepareStatement(lsSql);
				pstmt.setString(1,asCustCodeSoldTo);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					lsCustNameSoldTo = rs.getString("cust_name");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				//Added By PriyankaC on 03June2019 [END].
				///amish 21-08-03 common script for order is having cash discount or not.
				///tax amount will be used in cash discount credit policy.
				// pick up tax code of cash discount from dis parm
				lsTaxCode = disCommon.getDisparams("999999","TAX_CODE_CASH_DISCOUNT",conn);
				taxCode = lsTaxCode;
				arrStr = taxCode.split(",");
				lsString ="";
				for (int i = 0; i < arrStr.length; i++)
				{
					lsString = lsString+"'"+arrStr[i]+"',";
				}
				lsString =  "(" + lsString.substring(0,lsString.length()-1) + ")";
	
				//Sharon 04-Jan-2005 //Closing round Bracket missing
	
				lsSql = "	select abs(sum(tax_amt)) tax_amt "
						+ "	from taxtran "
						+ "	where  tran_code = 'S-ORD' "
						+ " and tran_id =  ? "
						+ "	and tax_code IN " + lsString ;
	
				pstmt = conn.prepareStatement(lsSql);
				pstmt.setString(1,asSorder);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					lcTaxAmt = rs.getDouble("tax_amt");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				///amish 21-08-03
	
				//**************SCRIPT FOR CREDIT CHECK FOR CUST CODE BILLED*********************
				mStat = "C"; // N = Not Confirmed , C = Confirmed
				llIgnGays = 0;
	
				////End Commented Ruchira 15/02/2k6, Not being used.
	
				//PICKING OF CR POLICY BASED ON ITEM SERIES
				//******************* lsCrPolicy = gf_get_cr_policy(asItemSer,asApplyTime); // This is pending
				//******************* //migrated by cpatil	
				PolicyList = getCrPolicyList(asItemSer,asApplyTime,conn); //to be changed to return an array list just empty method created
				
				//System.out.println("@@@@@@@@@@ PolicyList["+PolicyList+"]");
				
				writeLog(asSorder, "Credit check for [" + asSorder + "] Policies [" + lsCrPolicy + "] for item_ser [" + asItemSer + "]"); // 23/01/11 manoharan write the log
				//NEW LOGIC TO INITIALISE ITEM SERIES TO PARENT JUST FOR RETRIEVING ALL RECORDS OF THAT GROUP
				//IF PARENT ENTERED AND IS DIFFERENT FROM PASSED ITEM
				//SERIES AND IS NOT LINKED AND OTHER SERIES IS SELECTED AS G ONLY
				lsSql = "select descr from itemser where item_ser = ? " ;
	
				pstmt = conn.prepareStatement(lsSql);
				pstmt.setString(1,asItemSer);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					lsItemSerDescr = rs.getString("descr");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
	
				llSerCount = 0;
				//select count(*) into :llSerCount from itemser
				//where item_ser = :asItemSer
				//  and rtrim(item_ser) <> rtrim(item_ser__crpolicy)
				//  and length(rtrim(item_ser__crpolicy)) > 0
				//  and oth_series = 'G';
				//commented above and changed to below for length replacement on manoharanji's instruction  ** kiran  28/06/05
				lsSql = " select item_ser,item_ser__crpolicy,oth_series from itemser where item_ser = ? " ;
				
	
				pstmt = conn.prepareStatement(lsSql);
				pstmt.setString(1,asItemSer);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					itemSer = rs.getString("item_ser");
					itemSerCrpolicy = checknull(rs.getString("item_ser__crpolicy"));
					lsOthSeries = rs.getString("oth_series");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
				System.out.println("@@@@@@@@@ itemSer["+itemSer+"]itemSerCrpolicy["+itemSerCrpolicy+"]lsOthSeries["+lsOthSeries+"]");
				
				if ((!itemSer.trim().equals(itemSerCrpolicy.trim())) && itemSerCrpolicy.trim().length() > 0 && "G".equals(lsOthSeries) )
				{
					llSerCount = 1;
				}
				if (llSerCount > 0 )
				{
					
					//	ls_orig_item_ser = asItemSer
					//PICKING ITEM SER CR POLICY FROM ITEM SER MASTER FOR THE PASSED ITEMSER
					lsSql = "select item_ser__crpolicy   from itemser "
							+ " where item_ser =  ?" ;
	
					pstmt = conn.prepareStatement(lsSql);
					pstmt.setString(1,asItemSer);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						lsItemSerCrPolicy = rs.getString("item_ser__crpolicy");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					//System.out.println("@@@@@@@@@@ lsItemSerCrPolicy["+lsItemSerCrPolicy+"]");
					//ROWNUM IS USED TO PICK VALUE OF 1ST RECORD WHICH WILL ALWAYS BE 'N' IF RECORD EXISTS DUE TO ORDER BY CLAUSE
					//MANOJ ...... 25/10/02
					//ADDED BY MANOJ 25/07/03 FOR PICKING GLOBAL DATABASE
					// to avoid rownum  ** kiran 28/06/05
					//	if gs_database = 'db2' then
					//		select link_yn into :lsLink from itemser
					//		where item_ser__crpolicy = :lsItemSerCrPolicy
					//		  order by link_yn
					//		  fetch first row only;
					//	else
					//		select link_yn into :lsLink from itemser
					//		where item_ser__crpolicy = :lsItemSerCrPolicy
					//		  and rownum = 1
					//		  order by link_yn;
					//	end if
					lsSql = "select link_yn from itemser  "
							+ " where item_ser__crpolicy = ? order by link_yn  " ;
	
					pstmt = conn.prepareStatement(lsSql);
					pstmt.setString(1,lsItemSerCrPolicy);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						lsLink = rs.getString("link_yn");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					// end kiran
					if ("N".equals(lsLink))
					{
						asItemSer = lsItemSerCrPolicy;
					}
				}
				//end of new logic
				// DIVISION WISE CHECK
				// CHECKING WHETHER CUST CODE & ITEM SER EXISTS IN CUSTOMERSERIES TABLE
				//where decode(item_ser__crpolicy,null,item_ser,item_ser__crpolicy) = :asItemSer);
				//where nvl(item_ser__crpolicy,item_ser) = :asItemSer); //Manoj ....25/7/03 (db2)
				
				//changed by manish 24-05-2019 start
				/*lsSql = "select count(*)  from customer_series "
						+ " where cust_code = ? and item_ser in (select item_ser from itemser "
						+ " where (case when item_ser__crpolicy is null then item_ser else item_ser__crpolicy end ) = ?) ";*/
				lsSql = "select count(*)  from customer_series" + " where cust_code =?  and item_ser in (select item_ser from itemser" + 
						" where (item_ser__crpolicy is null or item_ser__crpolicy =? ))"; 
				//changed by manish 24-05-2019 end
				pstmt = conn.prepareStatement(lsSql);
				pstmt.setString(1,asCustCodeBil);
				pstmt.setString(2,asItemSer);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					mCount = rs.getInt(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				// IF RECORD FOR CUST & ITEM SER EXISTS IN CUSTSER TABLE THEN CHECK CREDIT
				//System.out.println("@@@@@@@@@ mCount["+mCount+"]PolicyList["+PolicyList+"]");
				
				if (mCount > 0 )
				{
					
					// 23-dec-2020 manoharan as per mail from KB to delete old failed rows and do a fresh check
					// to avoid override in case bank receipt made later after earlier credit check against the order
					lsSql = "delete from  business_logic_check where sale_order = ? and aprv_stat = 'F' ";
	
					pstmt = conn.prepareStatement(lsSql);
					pstmt.setString(1,asSorder);
					int delCount = pstmt.executeUpdate();
					if(pstmt!=null)//Modified by Rohini T on 10/05/2021
					{
						pstmt.close();
						pstmt=null;
					}
					// end 23-dec-2020 manoharan 
					
					for(int policyCtr = 0; policyCtr < PolicyList.size(); policyCtr++)
					{
						lsCrPolicy = PolicyList.get(policyCtr).toString();
						System.out.println("@@@@@@@@@ PolicyList.Size()["+PolicyList.size()+"]");
						System.out.println("@@@@@@@@@ lsCrPolicy["+lsCrPolicy+"]");
						//Changed By PriyankaC on 03June2019 [Start]
					/*	lsSql = "select cust_name  from customer where cust_code =  ?" ;
						pstmt = conn.prepareStatement(lsSql);
						pstmt.setString(1,asCustCodeBil);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
	
							lsCustName = rs.getString("cust_name");
							
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
	*/
						//Changed By PriyankaC on 03June2019[END]
						if ("P01".equals(lsCrPolicy.trim()) || "P21".equals(lsCrPolicy.trim()))
						{
	
							//	//if adNetAmt > 0 then		//Added Ruchira 27/06/2k6, //Commented to check for amt = 0 (batch pricelist) case, Ruchira 28/08/2k6
							//		gbf_credit_check_update(asSorder,'P01',adNetAmt,asApplyTime,lcCheckAmt,'C',lsStatus)
							//		if lcCheckAmt <= 0 then
							//			//All Amount got adjusted with the overriden records.
							//			gbf_credit_check_update(asSorder,'P01',adNetAmt,asApplyTime,lcCheckAmt,'U',lsStatus)  //////Added Ruchira 20/02/2k6
							//			goto ls_next_check2
							//		elseif lcCheckAmt > 0 then
							//			//Amount pending after adjusting the overriden records or no overriden records exist.
							//			adNetAmt = lcCheckAmt
							//		end if
							//	//end if
							//	////End Added by Ruchira 20/02/2k6.
							//	//PICKING ITEM SER CR POLICY FROM ITEM SER MASTER FOR THE PASSED ITEMSER
							//	select item_ser__crpolicy into :lsItemSerCrPolicy from itemser
							//	where item_ser = :asItemSer;
							//	if get_sqlcode() < 0 then
							//		errString = 'DS000'+String(sqlca.sqldbcode)
							//		return errString
							//	end if
							// PICKING IGNORE_CREDIT & CREDIT PERIOD FROM CUSTOMER SERIES
							//	select sum(ignore_credit), sum(nvl(ignore_days,0)) into :mIgnCr, :llIgnGays  // for nvl replacement kiran 28/06/05
	
							lsSql = "select sum(ignore_credit), "
									+ " sum(case when ignore_days is null then 0 else ignore_days end ) "
									+ " from customer_series where cust_code = ? "
									+ " and item_ser in (select item_ser from itemser "
									+ " where (case when item_ser__crpolicy is null then item_ser else item_ser__crpolicy end ) = ? ) " ;
	
							pstmt = conn.prepareStatement(lsSql);
							if("P01".equals(lsCrPolicy.trim()))
							{
								pstmt.setString(1,asCustCodeBil);
							}
							else
							{
								pstmt.setString(1,asCustCodeSoldTo);
							}
							pstmt.setString(2,asItemSer);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								mIgnCr = rs.getDouble(1);
								llIgnGays = rs.getInt(2);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
	
							// PICKING UP O/S AMOUNT FOR CUST & ITEM SER
							// PICKING UP O/S AMOUNT FOR CUST & ITEM SER
	
							flogicDetails=new JSONArray();//Modified by Rohini T on 26/04/2021
							jsonObjflogic = new JSONObject();
							//lsSql = "select due_date, tot_amt - dispute_amt - adj_amt as os_amt "//Modified by Rohini T on 23/04/2021
							lsSql = "select due_date, tot_amt - dispute_amt - adj_amt as os_amt,tot_amt,tran_ser,ref_no,adj_amt "
									+ " from  receivables "
									+ " where cust_code = ? "
									+ " and item_ser in (select item_ser from itemser "
									+ " where (case when item_ser__crpolicy is null then item_ser else "
									+ " item_ser__crpolicy end ) = ? )"
									+ " and tot_amt - dispute_amt - adj_amt > 0 and ref_type <> ? " ;
	
							pstmt = conn.prepareStatement(lsSql);
							if("P01".equals(lsCrPolicy.trim()))
							{
								pstmt.setString(1,asCustCodeBil);
							}
							else
							{
								pstmt.setString(1,asCustCodeSoldTo);
							}
							
							pstmt.setString(2,asItemSer);
							pstmt.setString(3,lsOrdNewPrd);
							rs = pstmt.executeQuery();
							while (rs.next())
							{
	
								mDueDate = rs.getTimestamp("due_date");
								mOsAmt = rs.getDouble("os_amt");
								totAmt = rs.getDouble("tot_amt");//Modified by Rohini T on 23/04/2021
								tranSer = rs.getString("tran_ser");
								refNo = rs.getString("ref_no");
								adjAmt = rs.getDouble("adj_amt");
								tempObj = new JSONObject();
								{
									tempObj.put("due_date",mDueDate);
									tempObj.put("os_amt",mOsAmt);
									tempObj.put("tot_amt",totAmt);
									tempObj.put("tran_ser",tranSer);
									tempObj.put("ref_no",refNo);
									tempObj.put("adj_amt",adjAmt);
								}
								//jsonObjflogic.put("FailedList", tempObj);
								flogicDetails.put(tempObj);
								//jsonObjflogic.put("FailedList", flogicDetails);
								//Modified by Rohini T on 26/04/2021
								//***************	 mChkDays = daysafter(date(mDueDate),today())  // migrated by cpatil
								// ibase.webitm.ejb.sys.UtilMethods.Relativedate() to be called
								mChkDays = (int) utilMethods.DaysAfter(mDueDate,currDate);
								if (mChkDays > llIgnGays)
								{
									mTotalOsAmt = mTotalOsAmt + mOsAmt;
								}
								totOsAmt = totOsAmt + mOsAmt; //Pavan Rane 19sep19[to get total outstanding amt]
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							//flogicDetails.put(jsonObjflogic);
							//jsonObjflogic.put("ItemList", flogicDetails);
							System.out.println("flogicDetails...."+flogicDetails);
							jsonStr = flogicDetails.toString();//Modified by Rohini T on 23/04/2021
							//jsonStr = jsonObjflogic.toString();
							System.out.println("jsonStr....@@@"+jsonStr);
						//	System.out.println("@@@@@@@@@@@@ mTotalOsAmt["+mTotalOsAmt+"]mIgnCr["+mIgnCr+"]");
							// CHECKING TOTAL O/S AMOUNT WITH IGNORE CREDIT
							if (mTotalOsAmt > mIgnCr)
							{
	
								////Checking for overriden / failed / balance amt, Ruchira 28/08/2k6
								//******************							gbf_credit_check_update(asSorder,'P01',adNetAmt,asApplyTime,lcCheckAmt,'C',lsStatus);
								
								
								retArrayList = credit_check_update(asSorder,lsCrPolicy,adNetAmt,asApplyTime,lcCheckAmt,"C",lsStatus,conn);
								
								// added on 19/04/16 for status and amount
								System.out.println("@@@@@@@@@409  retArrayList.size()["+ retArrayList.size()+"]");
								System.out.println("@@@@@@@@@409  retArrayList["+ retArrayList+"]");
								if( retArrayList.size() > 0)
								{
									lsStatus = retArrayList.get(0);
								}
								if( retArrayList.size() > 1)
								{
									lcCheckAmt = Double.parseDouble(retArrayList.get(1)==null?"0":retArrayList.get(1));
								}	
								//System.out.println("@@@@@ lsCrPolicy["+lsCrPolicy+"]lcCheckAmt["+lcCheckAmt+"]lsStatus["+lsStatus+"]");
								//Pavan Rane 19sep19 start[to insert proper message while P01 policy fail]
								if("P01".equals(lsCrPolicy.trim()))
								{
									lsStr1 = "Divisional over due exceeds limit for Bill to: "+lsCustNameBillTo +", Sold to: "+lsCustNameSoldTo +"  Total O/S Amt.=>" +utilMethods.getReqDecString(totOsAmt,3);
								}
								else
								{
									lsStr1 = "Divisional over due exceeds limit for Sold to: "+lsCustNameSoldTo +", Bill to: "+lsCustNameBillTo +", Total O/S Amt.=>" +utilMethods.getReqDecString(totOsAmt,3);
								}
								lsStr2 = "Over Due Outstanding Amt "+ utilMethods.getReqDecString(mTotalOsAmt,3) + " over "+ llIgnGays  +" days exceeds ignore Amt=> "+String(mIgnCr);
								//Pavan Rane 19sep19 end[to insert proper message while P01 policy fail]
								//To show the Invoice/SO amt in credit chek fail description, Added Ruchira 21/08/2k6
								if ("I".equalsIgnoreCase(asApplyTime))
								{
									lsStr3 = ", Invoice Amt.=> "+String(adNetAmt);
								}
								else
								{
									lsStr3 = ", Sales Order Amt.=> "+String(adNetAmt);
								}
								lsStr = lcCheckAmt +"\t" +lsStr1 + ", " + lsStr2 +", " + lsStr3;
	
								if (lcCheckAmt > 0)
								{
									lsStr = lsStr + "Overriden amount exceeds.";
								}
	
								////Always when credit check fails it must get added into as_mail.
								if (!"O".equals(lsStatus))
								{
									mailList.add(lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr);
	
								}
								System.out.println("@@@@@@@@@555 lsCrPolicy [" + lsCrPolicy + "] lcCheckAmt["+ lcCheckAmt+"] lsStatus [" + lsStatus + "]");
								if (lcCheckAmt > 0 || "F".equals(lsStatus) || lsStatus.trim().length() == 0)
								{
									System.out.println("@@@@@@@@@558 inside lsCrPolicy [" + lsCrPolicy + "] lcCheckAmt["+ lcCheckAmt+"] lsStatus [" + lsStatus + "]");
									//Modified by Rohini T on 26/04/2021[Start]
									//failedPolicyList.add(lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);     //added by manish mhatre on 28-6-2019
									failedPolicyList.add(lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr+"\t"+jsonStr);
									//Modified by Rohini T on 26/04/2021[End]
									/*failedPolicyList.add(lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);*/
									/*failedPolicyList.add(lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lcCheckAmt+"\t"+lsStr+"\t"+lsStatus+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr); */
								}
								writeLog(asSorder, (lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr));
	
							}
							///if the cr check is failed or not failed.
							if ("I".equalsIgnoreCase(asApplyTime) )
							{
								//******************								gbf_credit_check_update(asSorder,"P01",adNetAmt,asApplyTime,lcCheckAmt,'U',lsStatus);
								credit_check_update(asSorder,lsCrPolicy,adNetAmt,asApplyTime,lcCheckAmt,"U",lsStatus,conn);
							}
						}
						else if ("P02".equals(lsCrPolicy.trim()) || "P22".equalsIgnoreCase(lsCrPolicy.trim()))
						{
							flogicDetails=new JSONArray();//Modified by Rohini T on 26/04/2021
							jsonObjflogic = new JSONObject();
							//PICKING TOTAL O/S AMT AND CREDIT LIMIT & COMPARING
							lsSql = "select sum((case when tot_amt is null then 0 else tot_amt end) - (case when dispute_amt is null then 0 else dispute_amt end)) - sum(case when adj_amt is null then 0 else adj_amt end ) os_amt "
									+ " from  receivables " 
									+ " where cust_code =  ? "
									+ " and item_ser in (select item_ser from itemser "
									+ " where (case when item_ser__crpolicy is null then item_ser else item_ser__crpolicy end ) = ?) "
									+ " and ref_type <> ?";
	
							pstmt = conn.prepareStatement(lsSql);
							if("P02".equals(lsCrPolicy.trim()))
							{
								pstmt.setString(1,asCustCodeBil);
							}
							else
							{
								pstmt.setString(1,asCustCodeSoldTo);
							}
							pstmt.setString(2,asItemSer);
							pstmt.setString(3,lsOrdNewPrd);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								mTotOsAmt = rs.getDouble("os_amt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							//Modified by Rohini T on 23/04/2021[Start]
							lsSql = "select tot_amt,tran_ser,ref_no,adj_amt,due_date "
									+ " from  receivables " 
									+ " where cust_code =  ? "
									+ " and item_ser in (select item_ser from itemser "
									+ " where (case when item_ser__crpolicy is null then item_ser else item_ser__crpolicy end ) = ?) "
									+ " and ref_type <> ? and tot_amt - adj_amt <> 0";
							pstmt = conn.prepareStatement(lsSql);
							if("P02".equals(lsCrPolicy.trim()))
							{
								pstmt.setString(1,asCustCodeBil);
							}
							else
							{
								pstmt.setString(1,asCustCodeSoldTo);
							}
							pstmt.setString(2,asItemSer);
							pstmt.setString(3,lsOrdNewPrd);
							rs = pstmt.executeQuery();
							while (rs.next())
							{
								totAmt = rs.getDouble("tot_amt");
								tranSer = rs.getString("tran_ser");
								refNo = rs.getString("ref_no");
								adjAmt = rs.getDouble("adj_amt");
								mDueDate = rs.getTimestamp("due_date");
								tempObj = new JSONObject();
								{
									tempObj.put("tot_amt",totAmt);//Modified by Rohini T on 26/04/2021
									tempObj.put("tran_ser",tranSer);
									tempObj.put("ref_no",refNo);
									tempObj.put("adj_amt",adjAmt);
									tempObj.put("due_date",mDueDate);
									tempObj.put("os_amt",mTotOsAmt);
								}
								flogicDetails.put(tempObj);
								//jsonObjflogic.put("FailedList", tempObj);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							//flogicDetails.put(tempObj);
							//flogicDetails.put(jsonObjflogic);
							//jsonObjflogic.put("ItemList", flogicDetails);
							System.out.println("flogicDetails....###"+flogicDetails);
							jsonStr = flogicDetails.toString();
							//jsonStr = jsonObjflogic.toString();
							System.out.println("jsonStr....p02"+jsonStr);
							//Modified by Rohini T on 23/04/2021[End]
							/// select consider pending purchase order or not
							/// amish 3-10-03
							lsSql = "select consider_pbo "
									+ " from itemser_cr_policy " 
									+ " where item_ser = ? and cr_policy = '"+lsCrPolicy+"' ";
	
							pstmt = conn.prepareStatement(lsSql);
							pstmt.setString(1,asItemSer);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								lsConsiderPbo = rs.getString("consider_pbo");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
	
							if (lsConsiderPbo == null || lsConsiderPbo.trim().length() == 0 )
							{
								lsConsiderPbo = "Y";
							}
							System.out.println("***mTotOsAmt VAlues before addidng["+adNetAmt+"]mCrLimit["+mTotOsAmt);
							if ("Y".equalsIgnoreCase(lsConsiderPbo))
							{
								/// amish 13-08-03
								/// pick up the outstanding order amount. on the basis of the despatch percentage.
								/// order should be pending and order should not be the same as the current order.
								//Changed By PriyankaC on 03June2019.[Start]
								lsSql = "SELECT round(sum( sorddet.net_amt * (100 - ( (case when sorditem.qty_desp is null then 0 else sorditem.qty_desp end ) / sorditem.qty_ord * 100) ) / 100 ),3) pend_amt "
										+ " FROM sorddet, sorder, sorditem " 
										+ " WHERE ( sorddet.sale_order = sorditem.sale_order ) "
										+ " and ( sorddet.line_no = sorditem.line_no ) "
										+ " and ( sorder.sale_order = sorddet.sale_order ) "
										+ " and ( sorder.cust_code__bil = ? ) "
										+ " and ( sorder.status = 'P' ) "
										+ " and ( sorder.confirmed = 'Y' )"
										+ " and ( sorder.order_type <> ? ) "
										+ " and ( sorditem.qty_ord > 0 ) "
										+ " and ( sorder.item_ser in (select item_ser from itemser "
										+ " where (case when item_ser__crpolicy is null then item_ser else item_ser__crpolicy end ) = ?)) ";
	
								/*lsSql = "SELECT round(sum( (case when sorddet.net_amt = 0 then sorddet.ord_value else sorddet.net_amt end )  * (100 - ( (case when sorditem.qty_desp is null then 0 else sorditem.qty_desp end ) / sorditem.qty_ord * 100) ) / 100 ),3) pend_amt "
										+ " FROM sorddet, sorder, sorditem " 
										+ " WHERE ( sorddet.sale_order = sorditem.sale_order ) "
										+ " and ( sorddet.line_no = sorditem.line_no ) "
										+ " and ( sorder.sale_order = sorddet.sale_order ) "
										+ " and ( sorder.cust_code__bil = ? ) "
										+  "and ( sorder.status not in ('C','X') )"
	  								    + " and ( sorder.confirmed = 'Y' )";
								        +" and ( sorder.order_type <> ? ) "
								        + " and ( sorditem.qty_ord > 0 ) "
								        + " and ( sorder.item_ser in (select item_ser from itemser "
								        + " where (case when item_ser__crpolicy is null then item_ser else item_ser__crpolicy end ) = ?)) ";
	
								//Changed By PriyankaC on 03June2019.[END]
	*/							pstmt = conn.prepareStatement(lsSql);
								if("P02".equals(lsCrPolicy.trim()))
								{
									pstmt.setString(1,asCustCodeBil);
								}
								else
								{
									pstmt.setString(1,asCustCodeSoldTo);
								}
	
								pstmt.setString(2,lsOrdNewPrd);
								pstmt.setString(3,asItemSer);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									lcPendOrd = rs.getDouble("pend_amt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
	
							}
							else
							{
								lcPendOrd = 0;
							}
							//System.out.println("Total Pending Amount after sql executation["+lcPendOrd);
						 	System.out.println("***mTotOsAmt["+mTotOsAmt+"]adNetAmt["+adNetAmt  +"lcPendOrd"+ lcPendOrd);
							//TAKING INTO CONSIDERATION THE CURRENT INVOICE AMOUNT
							mTotOsAmt = mTotOsAmt + adNetAmt + lcPendOrd;
							lsSql = "select sum(case when credit_lmt is null then 0 else credit_lmt end ) cr_limit "
									+ " from customer_series " 
									+ " where cust_code = ? "
									+ " and item_ser in (select item_ser from itemser "
									+ " where (case when item_ser__crpolicy is null then item_ser else item_ser__crpolicy end ) = ?)" ;
	
							pstmt = conn.prepareStatement(lsSql);
							if("P02".equals(lsCrPolicy.trim()))
							{
								pstmt.setString(1,asCustCodeBil);
							}
							else
							{
								pstmt.setString(1,asCustCodeSoldTo);
							}
							pstmt.setString(2,asItemSer);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								mCrLimit = rs.getDouble("cr_limit");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("*** P22 mTotOsAmt["+mTotOsAmt+"]mCrLimit["+mCrLimit+"]mStat["+mStat+"]");
							if (mTotOsAmt > mCrLimit && "C".equals(mStat))
							{
								//******************						gbf_credit_check_update(asSorder,"P02",adNetAmt,asApplyTime,lcCheckAmt,"C",lsStatus);
	
								retArrayList = credit_check_update(asSorder,lsCrPolicy,adNetAmt,asApplyTime,lcCheckAmt,"C",lsStatus,conn);
								
								// added on 19/04/16 for status and amount
								System.out.println("@@@@@@@@@  retArrayList.size()["+ retArrayList.size()+"]");
								if( retArrayList.size() > 0)
								{
									lsStatus = retArrayList.get(0);
								}
								if( retArrayList.size() > 1)
								{
									lcCheckAmt = Double.parseDouble(retArrayList.get(1)==null?"0":retArrayList.get(1));
								}	
								System.out.println("@@@@@ lsCrPolicy["+lsCrPolicy+"]lcCheckAmt["+lcCheckAmt+"]lsStatus["+lsStatus+"]");
								
	                             //Changed By PriyankaC on 03June2019.[Start]
								//lsStr1 = "Outstanding amount exceeds limit for customer "+lsCustName;
								if("P02".equals(lsCrPolicy.trim()))
								{
									lsStr1 = "Divisionwise outstanding amount exceeds limit for  Bill to "+lsCustNameBillTo +", Sold to "+lsCustNameSoldTo  ;
								}
								else
								{
									lsStr1 = "Divisionwise outstanding amount exceeds limit for Sold to "+lsCustNameSoldTo +" Bill to "+lsCustNameBillTo ;
								}
								 //Changed By PriyankaC on 03June2019.[END]
								lsStr2 = " , Total O/S Amt: " + utilMethods.getReqDecString(mTotOsAmt,3) + " for division "+ lsItemSerDescr.trim() + " exceeds limit: "+ mCrLimit ; 
								//To show the Invoice/SO amt in credit chek fail description, Added Ruchira 21/08/2k6
								if ("I".equals(asApplyTime))
								{
									lsStr3 = " , Invoice Amt.=> "+adNetAmt;
								}
								else
								{
									lsStr3 = " , Sales Order Amt.=> "+adNetAmt ;
								}
								lsStr = lcCheckAmt +"\t"+lsStr1+", "+lsStr2+", "+lsStr3;
	
								if( lcCheckAmt > 0 )
								{
									lsStr = lsStr + ", Overriden amount exceeds.";
								}
								////Always when credit check fails it must get added into as_mail.
								if (!"O".equals(lsStatus))
								{
									mailList.add(lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr);
								}
								System.out.println("@@@@@@@@@767 lsCrPolicy [" + lsCrPolicy + "] lcCheckAmt["+ lcCheckAmt+"] lsStatus [" + lsStatus + "]");
								if (lcCheckAmt > 0 || "F".equalsIgnoreCase(lsStatus) || lsStatus.trim().length() == 0 )
								{
									//Modified by Rohini T on 26/04/2021[Start]
									//failedPolicyList.add(lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);   //added by manish mhatre on 28-6-2019
									failedPolicyList.add(lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr+"\t"+jsonStr);
									//Modified by Rohini T on 26/04/2021[End]
									/*failedPolicyList.add(lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);*/
									/*failedPolicyList.add(lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lcCheckAmt+"\t"+lsStr+"\t"+lsStatus+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);*/ 
								}
								System.out.println("refDate String:"+refDateStr);
								writeLog(asSorder, (lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr));
							}
							///if the cr check is failed or not failed.
							if ("I".equals(asApplyTime))
							{
								//******************						gbf_credit_check_update(asSorder,'P02',adNetAmt,asApplyTime,lcCheckAmt,'U',lsStatus);
	
								credit_check_update(asSorder,lsCrPolicy,adNetAmt,asApplyTime,lcCheckAmt,"U",lsStatus,conn);
	
							}
						}
						else if ("P03".equals(lsCrPolicy.trim()) || "P23".equals(lsCrPolicy.trim()))
						{
	
							mIgnCr = 0;
							mOsAmt = 0;
							mTotalOsAmt = 0;
							llIgnGays = 0;
							// PICKING IGNORE_CREDIT & CREDIT PERIOD FROM CUSTOMER
							lsSql = "select ignore_credit , "
									+ " (case when ignore_days is null then 0 else ignore_days end) ignore_days "
									+ " from customer where cust_code = ? " ;
	
							pstmt = conn.prepareStatement(lsSql);
							if("P03".equals(lsCrPolicy.trim()))
							{
								pstmt.setString(1,asCustCodeBil);
							}
							else
							{
								pstmt.setString(1,asCustCodeSoldTo);
							}
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								mIgnCr = rs.getDouble("ignore_credit");
								llIgnGays = rs.getInt("ignore_days");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							// PICKING UP O/S AMOUNT FOR CUSTOMER
							flogicDetails=new JSONArray();//Modified by Rohini T on 26/04/2021
							jsonObjflogic = new JSONObject();
							//lsSql = "select due_date, tot_amt - dispute_amt - adj_amt as os_amt "//Modified by Rohini T on 23/04/2021
							lsSql = "select due_date, tot_amt - dispute_amt - adj_amt as os_amt,tot_amt,tran_ser,ref_no,adj_amt "
									+ " from  receivables "
									+ " where cust_code =  ? " 
									+ " and tot_amt - dispute_amt - adj_amt > 0 "
									+ " and ref_type <> ?" ;
	
							pstmt = conn.prepareStatement(lsSql);
							
							if("P03".equals(lsCrPolicy.trim()))
							{
								pstmt.setString(1,asCustCodeBil);
							}
							else
							{
								pstmt.setString(1,asCustCodeSoldTo);
							}
							pstmt.setString(2,lsOrdNewPrd);
							rs = pstmt.executeQuery();
							while (rs.next())
							{
								mDueDate = rs.getTimestamp("due_date");
								mOsAmt = rs.getDouble("os_amt");
								totAmt = rs.getDouble("tot_amt");//Modified by Rohini T on 23/04/2021
								tranSer = rs.getString("tran_ser");
								refNo = rs.getString("ref_no");
								adjAmt = rs.getDouble("adj_amt");
								tempObj = new JSONObject();
								{
									tempObj.put("due_date",mDueDate);
									tempObj.put("os_amt",mOsAmt);
									tempObj.put("tot_amt",totAmt);
									tempObj.put("tran_ser",tranSer);
									tempObj.put("ref_no",refNo);
									tempObj.put("adj_amt",adjAmt);
								}
								flogicDetails.put(tempObj);
								//***************							mChkDays = daysafter(date(mDueDate),today()); // ibase.webitm.ejb.sys.UtilMethods.Relativedate() to be called
								mChkDays = (int) utilMethods.DaysAfter( mDueDate, currDate);
								if (mChkDays > llIgnGays)
								{
									mTotalOsAmt = mTotalOsAmt + mOsAmt;
								}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("flogicDetails....12"+flogicDetails);
							jsonStr = flogicDetails.toString();//Modified by Rohini T on 26/04/2021
							System.out.println("jsonStr...."+jsonStr);
							if( mTotalOsAmt > mIgnCr && "C".equals(mStat))
							{
								////Checking for overriden / failed / balance amt, Ruchira 28/08/2k6
								//******************					gbf_credit_check_update(asSorder,'P03',adNetAmt,asApplyTime,lcCheckAmt,'C',lsStatus)
	
								retArrayList = credit_check_update(asSorder,lsCrPolicy,adNetAmt,asApplyTime,lcCheckAmt,"C",lsStatus,conn);
	
								// added on 19/04/16 for status and amount
								//System.out.println("@@@@@@@@@  retArrayList.size()["+ retArrayList.size()+"]");
								if( retArrayList.size() > 0)
								{
									lsStatus = retArrayList.get(0);
								}
								if( retArrayList.size() > 1)
								{
									lcCheckAmt = Double.parseDouble(retArrayList.get(1)==null?"0":retArrayList.get(1));
								}	
								//System.out.println("@@@@@ lsCrPolicy["+lsCrPolicy+"]lcCheckAmt["+lcCheckAmt+"]lsStatus["+lsStatus+"]");
								
								//Changed By PriyankaC on 03June2019.[Start]
									//	lsStr1 = "Overall total credit check failed for "+lsCustName;
								if("P03".equals(lsCrPolicy.trim()))
								{
									lsStr1 = "Overall over due amount exceeds limit for Bill to "+lsCustNameBillTo  +", Sold to "+ lsCustNameSoldTo;
								}
								else
								{
									lsStr1 = "Overall over due amount exceeds limit for Sold to "+lsCustNameSoldTo +" Bill to "+lsCustNameBillTo ;
								}
									//Changed By PriyankaC on 03June2019.[END]
								lsStr2 = " , Total O/S Amt over and above "+ llIgnGays  +" days is " + utilMethods.getReqDecString(mTotalOsAmt,3) + " exceeds limit: "+String(mIgnCr);
								//To show the Invoice/SO amt in credit chek fail description, Added Ruchira 21/08/2k6
								if ("I".equals(asApplyTime))
								{
									lsStr3 = " , Invoice Amt.=> "+adNetAmt;
								}
								else
								{
									lsStr3 = " , Sales Order Amt.=> "+adNetAmt;
								}
								lsStr = lcCheckAmt +"\t"+lsStr1+", "+lsStr2+", "+lsStr3;
	
								if (lcCheckAmt > 0)
								{
									lsStr = lsStr + ", Overriden amount exceeds.";
								}
	
								////Always when credit check fails it must get added into as_mail.
								if (!"O".equals(lsStatus))
								{
									//lsMail[lsMail.length + 1] = "P03"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr;
									mailList.add(lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr);
								}
								System.out.println("@@@@@@@@@904 lsCrPolicy [" + lsCrPolicy + "] lcCheckAmt["+ lcCheckAmt+"] lsStatus [" + lsStatus + "]");
								if (lcCheckAmt > 0 || "F".equals(lsStatus) || lsStatus.trim().length() == 0)
								{
									//Modified by Rohini T on 26/04/2021[Start]
									//failedPolicyList.add(lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr); //added by manish mhatre on 28-6-2019
									failedPolicyList.add(lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr+"\t"+jsonStr);
									//Modified by Rohini T on 26/04/2021[End]
									/*failedPolicyList.add(lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);*/
									/*failedPolicyList.add(lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lcCheckAmt+"\t"+lsStr+"\t"+lsStatus+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);*/ 
								}
								writeLog(asSorder, (lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr));
	
							}
	
							////Added Ruchira 29/08/2k6, to update the used_amt in business_logic_check table even
							///if the cr check is failed or not failed.
							if ("I".equals(asApplyTime))
							{
								//******************					gbf_credit_check_update(asSorder,'P03',adNetAmt,asApplyTime,lcCheckAmt,'U',lsStatus);
	
								credit_check_update(asSorder,lsCrPolicy,adNetAmt,asApplyTime,lcCheckAmt,"U",lsStatus,conn);
							}
							////End Added Ruchira 29/08/2k6
	
							//////Added Ruchira 20/02/2k6, re-assigned adNetAmt by ld_old_adNetAmt.
							//adNetAmt = ld_old_adNetAmt
						}
						else if ("P04".equals(lsCrPolicy.trim()) || "P24".equals(lsCrPolicy.trim()))
						{
							
							//PICKING TOTAL O/S AMT AND CREDIT LIMIT & COMPARING
							flogicDetails=new JSONArray();//Modified by Rohini T on 26/04/2021
							jsonObjflogic = new JSONObject();
							lsSql = "select sum((case when tot_amt is null then 0 else tot_amt end) - (case when dispute_amt is null then 0 else dispute_amt end)) - sum(case when adj_amt is null then 0 else adj_amt end ) as os_amt "
									+ " from  receivables " 
									+ " where cust_code = ? "
									+ " and ref_type <> ? ";
	
							pstmt = conn.prepareStatement(lsSql);
							// 18-Apr-2019 wrongly compared as  P03 insead of P04
							//if("P03".equals(lsCrPolicy.trim()))
							if("P04".equals(lsCrPolicy.trim()))
							{
								pstmt.setString(1,asCustCodeBil);
							}
							else
							{
								pstmt.setString(1,asCustCodeSoldTo);
							}
							pstmt.setString(2,lsOrdNewPrd);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								mTotOsAmt = rs.getDouble("os_amt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							//Modified by Rohini T on 23/04/2021[Start]
							lsSql = "select tot_amt,tran_ser,ref_no,adj_amt,due_date "
									+ " from  receivables " 
									+ " where cust_code = ? "
									+ " and ref_type <> ? ";
							pstmt = conn.prepareStatement(lsSql);
							if("P04".equals(lsCrPolicy.trim()))
							{
								pstmt.setString(1,asCustCodeBil);
							}
							else
							{
								pstmt.setString(1,asCustCodeSoldTo);
							}
							pstmt.setString(2,lsOrdNewPrd);
							rs = pstmt.executeQuery();
							while (rs.next())
							{
								totAmt = rs.getDouble("tot_amt");
								tranSer = rs.getString("tran_ser");
								refNo = rs.getString("ref_no");
								adjAmt = rs.getDouble("adj_amt");
								mDueDate = rs.getTimestamp("due_date");
								tempObj = new JSONObject();
								{
									tempObj.put("tot_amt",totAmt);//Modified by Rohini T on 26/04/2021
									tempObj.put("tran_ser",tranSer);
									tempObj.put("ref_no",refNo);
									tempObj.put("adj_amt",adjAmt);
									tempObj.put("due_date",mDueDate);
									tempObj.put("os_amt",mTotOsAmt);
								}
								//jsonObjflogic.put("FailedList", tempObj);
								flogicDetails.put(tempObj);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							//flogicDetails.put(tempObj);
							//flogicDetails.put(jsonObjflogic);
							System.out.println("flogicDetails....123"+flogicDetails);
							jsonStr = flogicDetails.toString();
							System.out.println("jsonStr...."+jsonStr);
							//Modified by Rohini T on 23/04/2021[End]
							/// select consider pending purchase order or not
							/// amish 3-10-03
							lsSql = "select consider_pbo "
									+ " from itemser_cr_policy " 
									+ " where item_ser = ? and cr_policy = '"+lsCrPolicy+"'";
	
							pstmt = conn.prepareStatement(lsSql);
							pstmt.setString(1,asItemSer);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								lsConsiderPbo = rs.getString("consider_pbo");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
	
							if (lsConsiderPbo == null || lsConsiderPbo.trim().length() == 0 )
							{
								lsConsiderPbo = "Y";
							}
							if ("Y".equals(lsConsiderPbo))
							{
								/// amish 13-08-03
								/// pick up the outstanding order amount. on the basis of the despatch percentage.
								/// order should be pending and order should not be the same as the current order.
								lsSql = "SELECT round(sum( sorddet.net_amt * (100 - ( ( case when sorditem.qty_desp is null then 0 else sorditem.qty_desp end ) / sorditem.qty_ord * 100) ) / 100 ),3) as pend_ord "
										+ " FROM sorddet, sorder, sorditem "
										+ " WHERE ( sorddet.sale_order = sorditem.sale_order ) "
										+ " and ( sorddet.line_no = sorditem.line_no ) "
										+ " and ( sorder.sale_order = sorddet.sale_order ) "
										+ " and	( sorder.cust_code__bil = ? ) "
										+ " and ( sorder.status = 'P' ) "
										+ " and ( sorder.order_type <> ? ) "
										+ " and ( sorditem.qty_ord > 0 ) "
										+ " and ( sorder.confirmed = 'Y' )" ;
	
								pstmt = conn.prepareStatement(lsSql);
								if("P04".equals(lsCrPolicy.trim()))
								{
									pstmt.setString(1,asCustCodeBil);
								}
								else
								{
									pstmt.setString(1,asCustCodeSoldTo);
								}
								pstmt.setString(2,lsOrdNewPrd);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									lcPendOrd = rs.getDouble("pend_ord");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
	
							}
							else
							{
								lcPendOrd = 0;
							}
	
							//TAKING INTO CONSIDERATION THE CURRENT INVOICE AMOUNT
							mTotOsAmt = mTotOsAmt + adNetAmt + lcPendOrd;
	
							lsSql = "select credit_lmt "
									+ " from customer " 
									+ " where cust_code = ?";
	
							pstmt = conn.prepareStatement(lsSql);
							if("P04".equals(lsCrPolicy.trim()))
							{
								pstmt.setString(1,asCustCodeBil);
							}
							else
							{
								pstmt.setString(1,asCustCodeSoldTo);
							}
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								mCrLimit = rs.getDouble("credit_lmt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if (mTotOsAmt > mCrLimit && "C".equals(mStat))
							{
								////Checking for overriden / failed / balance amt, Ruchira 28/08/2k6
								//******************					gbf_credit_check_update(asSorder,'P04',adNetAmt,asApplyTime,lcCheckAmt,'C',lsStatus);
								retArrayList = credit_check_update(asSorder,lsCrPolicy,adNetAmt,asApplyTime,lcCheckAmt,"C",lsStatus,conn);
	
								// added on 19/04/16 for status and amount
								//System.out.println("@@@@@@@@@  retArrayList.size()["+ retArrayList.size()+"]");
								if( retArrayList.size() > 0)
								{
									lsStatus = retArrayList.get(0);
								}
								if( retArrayList.size() > 1)
								{
									lcCheckAmt = Double.parseDouble(retArrayList.get(1)==null?"0":retArrayList.get(1));
								}	
								//System.out.println("@@@@@ lsCrPolicy["+lsCrPolicy+"]lcCheckAmt["+lcCheckAmt+"]lsStatus["+lsStatus+"]");
								
								//Changed By PriyankaC on 03June2019.[Start]
							//	lsStr1 = "Overall total credit check failed for "+lsCustName;
								if("P04".equals(lsCrPolicy.trim()))
								{
									lsStr1 = "Overall outstanding exceeds limit for Bill to "+ lsCustNameBillTo  +" Sold to "+ lsCustNameSoldTo ;
								}
								else
								{
									lsStr1 = "Overall outstanding exceeds limit for Sold to "+lsCustNameSoldTo +" Bill to "+lsCustNameBillTo ;
								}
								////Changed By PriyankaC on 03June2019.[END]
								lsStr2 = " , Total O/S Amt: "+ utilMethods.getReqDecString(mTotOsAmt,3) + " exceeds credit limit: "+ mCrLimit; 
								//To show the Invoice/SO amt in credit chek fail description, Added Ruchira 21/08/2k6
								if ("I".equals(asApplyTime))
								{
									lsStr3 = " , Invoice Amt.=> "+ adNetAmt;
								}
								else
								{
									lsStr3 = " , Sales Order Amt.=> " + adNetAmt ;
								}
								lsStr = lcCheckAmt +"\t"+lsStr1+", "+lsStr2+", "+lsStr3;
	
								if (lcCheckAmt > 0)
								{
									lsStr = lsStr + ", Overriden amount exceeds.";
								}
								////Always when credit check fails it must get added into as_mail.
								if (!"O".equals(lsStatus))
								{
									mailList.add(lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr);
								}
								System.out.println("@@@@@@@@@1098 lsCrPolicy [" + lsCrPolicy + "] lcCheckAmt["+ lcCheckAmt+"] lsStatus [" + lsStatus + "]");
								if (lcCheckAmt > 0 ||  "F".equalsIgnoreCase(lsStatus) || lsStatus.trim().length() == 0 )
								{
									//Modified by Rohini T on 26/04/2021[Start]
									//failedPolicyList.add(lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);   //added by manish mhatre on 28-6-2019
									failedPolicyList.add(lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr+"\t"+jsonStr);
									//Modified by Rohini T on 26/04/2021[End]
									/*failedPolicyList.add(lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);*/
									/*failedPolicyList.add(lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lcCheckAmt+"\t"+lsStr+"\t"+lsStatus+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);*/ 
								}
								writeLog(asSorder, (lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr));
							}
							////Added Ruchira 29/08/2k6, to update the used_amt in business_logic_check table even
							///if the cr check is failed or not failed.
							if ("I".equals(asApplyTime))
							{
								//******************					gbf_credit_check_update(asSorder,'P04',adNetAmt,asApplyTime,lcCheckAmt,'U',lsStatus);
								credit_check_update(asSorder,lsCrPolicy,adNetAmt,asApplyTime,lcCheckAmt,"U",lsStatus,conn);
							}
							////End Added Ruchira 29/08/2k6
						}
						else if ("P05".equals(lsCrPolicy.trim()))
						{
	
							//CHECKING OF CUSTOMER IF CR TERM IS SAME AS RECEIPT DISHONOUR	TERM AS IN FINPARM
							//PICKING ITEM SER BASED ON ITEM SER CRPOLICY
							lsVarValue = disCommon.getDisparams("999999","DISHONOUR_CR_TERM",conn);
							if ("NULLFOUND".equals(lsVarValue) || lsVarValue.trim().length() == 0 )
							{
								lsVarValue = "";
							}
							lsVarValue = lsVarValue.trim();
							lsCreditTerm = "";
							lsSql = "select item_ser from itemser "
									//+ " from customer " //commented by Pavan R on 2K18/FEB/01 bug fix
									+ " where case when item_ser__crpolicy is null then item_ser else item_ser__crpolicy end  = ?";
	
							pstmt = conn.prepareStatement(lsSql);
							pstmt.setString(1,asItemSer);
							rs = pstmt.executeQuery();
							while (rs.next())
							{
								lsItemSer = rs.getString("item_ser");
	
								lsSql = "select case when a.cr_term is null then b.cr_term else a.cr_term end as cr_term "
										+ " from customer_series a, customer b " 
										+ "	where a.cust_code = b.cust_code "
										+ " and a.cust_code = ? "
										+ " and a.item_ser = ? ";
	
								pstmt1 = conn.prepareStatement(lsSql);
								pstmt1.setString(1,asCustCodeBil);
								pstmt1.setString(2,lsItemSer);
								rs1 = pstmt1.executeQuery();
								if (rs1.next())
								{
									lsCreditTerm = rs1.getString("cr_term");
								}
								rs1.close();
								rs1 = null;
								pstmt1.close();
								pstmt1 = null;
								if (lsCreditTerm.trim().equals(lsVarValue.trim()))
								{
									break;								
								}
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
	
							if (lsCreditTerm.trim().equals(lsVarValue.trim()))
							{
								//MODIFIED BY MANOJ 22/10/2001 AS PER KANDARP
								//PICKING ACCT PRD ON BASIS OF SYSDATE
	
								lsSql = "select fn_sysdate() from dual";
	
								pstmt = conn.prepareStatement(lsSql);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									ldDateTime = rs.getTimestamp(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
								//SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
								//SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
								ldDateTime = java.sql.Timestamp.valueOf(sdf1.format(ldDateTime) + " 00:00:00.0");
								lsSql = "select acct_prd "
										+ " from period " 
										+ " where ? between fr_date and to_date ";
	
								pstmt = conn.prepareStatement(lsSql);
								pstmt.setTimestamp(1,ldDateTime);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									lsAcctPrd = rs.getString("acct_prd");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
	
								//PICKING NET ADVANCE AMT FROM SUNDRYBAL
								lsSql = "select sum(case when cr_amt__base is null then 0 else cr_amt__base end ) as cr_amt,"
										+ " sum(case when dr_amt__base is null then 0 else dr_amt__base end ) as dr_amt " 
										+ " from sundrybal " 
										+ " where acct_prd = ? "
										+ "  and prd_code = 'zzzzzz' "
										+ "  and site_code = ? "
										+ "  and sundry_type  = 'C' "
										+ "  and sundry_code = ? ";
	
								pstmt = conn.prepareStatement(lsSql);
								pstmt.setString(1,lsAcctPrd);
								pstmt.setString(2,asSiteCode);
								pstmt.setString(3,asCustCodeBil);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									lcCrAmtBase = rs.getDouble("cr_amt");
									lcDrAmtBase = rs.getDouble("dr_amt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								lcNetAdv = lcCrAmtBase - lcDrAmtBase;
	
								// CHECKING IF NET ADVANCE IS LESS THEN INVOICE AMT
								if ((lcNetAdv < adNetAmt) && adNetAmt > 0 )
								{
									//	added by deepali 26.05.04 to add credit note in despatch from gnl
									////Checking for overriden / failed / balance amt, Ruchira 28/08/2k6
									//******************						gbf_credit_check_update(asSorder,'P05',adNetAmt,asApplyTime,lcCheckAmt,'C',lsStatus);
	
									retArrayList = credit_check_update(asSorder,"P05",adNetAmt,asApplyTime,lcCheckAmt,"C",lsStatus,conn);
	
									// added on 19/04/16 for status and amount
									//	System.out.println("@@@@@@@@@  retArrayList.size()["+ retArrayList.size()+"]");
										if( retArrayList.size() > 0)
										{
											lsStatus = retArrayList.get(0);
										}
										if( retArrayList.size() > 1)
										{
											lcCheckAmt = Double.parseDouble(retArrayList.get(1)==null?"0":retArrayList.get(1));
										}	
									//	System.out.println("@@@@@ lsCrPolicy["+lsCrPolicy+"]lcCheckAmt["+lcCheckAmt+"]lsStatus["+lsStatus+"]");
										
									 //Changed By PriyankaC on 03June2019.[Start]
									//lsStr1 = lsCustName + " is on Advance Term";
									lsStr1 = "for Sold to "+lsCustNameSoldTo +" Bill to "+lsCustNameBillTo +" is on Advance Term";
									//Changed By PriyankaC on 03June2019.[END]
									lsStr2 = " , Credit Balance: "+ utilMethods.getReqDecString(lcNetAdv,3) + " is less than invoice amount: "+ adNetAmt;
									//To show the Invoice/SO amt in credit chek fail description, Added Ruchira 21/08/2k6
									if ("I".equals(asApplyTime))
									{
										lsStr3 = " , Invoice Amt.=> "+ adNetAmt;
									}
									else
									{
										lsStr3 = " , Sales Order Amt.=> "+ adNetAmt;
									}
									lsStr = lcCheckAmt + "\t"+lsStr1 + ", " + lsStr2 + ", " + lsStr3;
	
									if (lcCheckAmt > 0)
									{
										lsStr = lsStr + ", Overriden amount exceeds.";
									}
	
									////Always when credit check fails it must get added into as_mail.
									if (!"O".equals(lsStatus))
									{
										mailList.add("P05"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr);
									}
									if (lcCheckAmt > 0 || "F".equals(lsStatus) || lsStatus.trim().length() == 0)
									{
										//Modified by Rohini T on 26/04/2021[Start]
										//failedPolicyList.add("P05"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);  //added by manish mhatre on 28-6-2019
										failedPolicyList.add("P05"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr+"\t"+"");
										//Modified by Rohini T on 26/04/2021[End]
										/*failedPolicyList.add("P05"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);*/
										/*failedPolicyList.add("P05"+"\t"+asDespId+"\t"+asSorder+"\t"+lcCheckAmt+"\t"+lsStr+"\t"+lsStatus+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);*/ 
									}
									writeLog(asSorder, ("P05"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr));
								}
							}
	
							////Added Ruchira 29/08/2k6, to update the used_amt in business_logic_check table even
							///if the cr check is failed or not failed.
							if ("I".equals(asApplyTime))
							{
								//******************					gbf_credit_check_update(asSorder,'P05',adNetAmt,asApplyTime,lcCheckAmt,'U',lsStatus);
								credit_check_update(asSorder,"P05",adNetAmt,asApplyTime,lcCheckAmt,"U",lsStatus,conn);
							}
							////End Added Ruchira 29/08/2k6
						}
	
						else if ("P06".equals(lsCrPolicy.trim()) && "S".equals(asApplyTime) )
						{
							// business limit to be chkd during confirmation of sales order ONLY
	
	
							//LOGIC FOR COMMON INVOICING MANOJ......17/10/02
							//select b.item_ser,sum(decode(a.net_amt,0,a.ord_value,a.net_amt)) ord_value from sorddet a, item b,  // to avoid decode  ** kiran 28/06/05
							lsSql = "select b.item_ser, "
									+ " sum(case when a.net_amt = 0 then a.ord_value else a.net_amt end) as ord_value " 
									+ " from sorddet a, item b, sorder c "
									+ " where c.sale_order = a.sale_order "
									+ " and a.item_code__ord = b.item_code " //Added for item_code gets stored in item_code__ord but not in item_code column in sorddet table,Rutuja 26/12/14 D14ISUN009).
									+ " and a.sale_order = ? "
									+ " and c.order_type <> ? group by b.item_ser";
	
							pstmt = conn.prepareStatement(lsSql);
							pstmt.setString(1,asSorder);
							pstmt.setString(2,lsOrdNewPrd);
							rs = pstmt.executeQuery();
							lsItemSerOrig = asItemSer;
							lcAdNetAmtOrig = adNetAmt;
							while (rs.next())
							{
								//Changed and Commented By Bhushan on 27-05-16 :START
								//lsItemSerCommon = rs.getString("cr_amt");
								//lcOrderValue = rs.getDouble("dr_amt");
								lsItemSerCommon = rs.getString(1);
								lcOrderValue = rs.getDouble(2);
								//Changed and Commented By Bhushan on 27-05-16 :END
								asItemSer = lsItemSerCommon;
								lcNetAmt = 0;
								//CHECKING FOR CREDIT CHECK IF HE HAS CROSSED BUSINESS LIMIT FOR PARTICULAR DIV / PERIOD
								lcBusiLimit = 0;
	
								lsSql = "select sum(case when busi_limit is null then 0 else busi_limit end) as busi_limit "
										+ " from customer_series "
										+ " where cust_code = ? "
										+ " and item_ser = ? ";
	
								pstmt1 = conn.prepareStatement(lsSql);
								pstmt1.setString(1,asCustCodeBil);
								pstmt1.setString(2,lsItemSer);
								rs1 = pstmt1.executeQuery();
								if (rs1.next())
								{
									lcBusiLimit = rs1.getDouble("busi_limit");
								}
								rs1.close();
								rs1 = null;
								pstmt1.close();
								pstmt1 = null;
	
								if (lcBusiLimit == 0)
								{
									continue;
								}
								//PICKING DATE FOR PASSED ITEM SER SINCE DATE WILL BE COMMON FOR GROUP ITEM SER
								//*******************							s_pass1 = gf_get_prd_date(asItemSer,asSiteCode,adtTranDate);
								/*if ( (String) s_pass1.mvar1.trim().length() > 0)
								{
									errString = (String) s_pass1.get("mvar1");
									break;
								}*/
								s_pass1 = gf_get_prd_date(asItemSer,asSiteCode,adtTranDate,conn);
								if ( s_pass1.get("mvar1").toString().trim().length() > 0 )
								{
									errString = (String) s_pass1.get("mvar1");
									break;
								}
	
								ldtFromDate = (Timestamp) s_pass1.get("date1");
								ldtToDate = (Timestamp) s_pass1.get("date2");
	
								lsSql = "select sum(case when net_amt is null then 0 else net_amt end) as net_amt "
										+ " from invoice "
										+ " where tran_date >= ? "
										+ " and tran_date <=  ? "
										+ " and cust_code = ? "
										+ " and item_ser  = ? ";
	
								pstmt1 = conn.prepareStatement(lsSql);
								pstmt1.setTimestamp(1,ldtFromDate);
								pstmt1.setTimestamp(2,ldtToDate);
								pstmt1.setString(3,asCustCodeBil);
								pstmt1.setString(4,asItemSer);
								rs1 = pstmt1.executeQuery();
								if (rs1.next())
								{
									lcNetAmt = rs1.getDouble("net_amt");
								}
								rs1.close();
								rs1 = null;
								pstmt1.close();
								pstmt1 = null;
	
								//ADDED CHECK FOR INITIALISING NET AMT TO ORDER VALUE ONLY IS CREDIT CHECK IS AT SORDER LEVEL
								adNetAmt = lcOrderValue;
								//atul 30.01.02
								//if called from 'invoice' then do not add current invoice amount.
								lsInvCallFr = gs_inv_call_fr;
								if (lsInvCallFr == null )
								{
									lsInvCallFr=" ";
								}
								if ("I".equals(lsInvCallFr))
								{
									lcNetAmt = lcNetAmt + adNetAmt;
								}
								// Changed On : 24/08/01 : Surender
								// If Business Limit is ZERO then don't check the credit
								if (lcBusiLimit > 0 )
								{
									if (lcNetAmt > lcBusiLimit)
									{
										////Checking for overriden / failed / balance amt, Ruchira 28/08/2k6
										//******************							gbf_credit_check_update(asSorder,'P06',adNetAmt,asApplyTime,lcCheckAmt,'C',lsStatus)
										retArrayList = credit_check_update(asSorder,"P06",adNetAmt,asApplyTime,lcCheckAmt,"C",lsStatus,conn);
	
										// added on 19/04/16 for status and amount
										//System.out.println("@@@@@@@@@  retArrayList.size()["+ retArrayList.size()+"]");
										if( retArrayList.size() > 0)
										{
											lsStatus = retArrayList.get(0);
										}
										if( retArrayList.size() > 1)
										{
											lcCheckAmt = Double.parseDouble(retArrayList.get(1)==null?"0":retArrayList.get(1));
										}	
										//System.out.println("@@@@@ lsCrPolicy["+lsCrPolicy+"]lcCheckAmt["+lcCheckAmt+"]lsStatus["+lsStatus+"]");
										
										//Changed By PriyankaC on 03June2019.[Start]
										//lsStr1 = lsCustName + " is crossing the business limit";
										lsStr1 = "for Sold to "+lsCustNameSoldTo +" Bill to "+lsCustNameBillTo + " is crossing the business limit";
										//Changed By PriyankaC on 03June2019 [END]
										lsStr2 = " , Sales for the month: " + utilMethods.getReqDecString(lcNetAmt,3) + " exceeds defined business limit: "+ lcBusiLimit +"*"+asItemSer;
										//To show the Invoice/SO amt in credit chek fail description, Added Ruchira 21/08/2k6
										if ("I".equals(asApplyTime))
										{
											lsStr3 = " , Invoice Amt.=> "+ adNetAmt;
										}
										else
										{
											lsStr3 = " , Sales Order Amt.=> " +  adNetAmt;
										}
										lsStr = lcCheckAmt + "\t" + lsStr1 + ", " + lsStr2 + ", " + lsStr3 ;
	
										if (lcCheckAmt > 0 )
										{
											lsStr = lsStr + ", Overriden amount exceeds.";
										}
										////Always when credit check fails it must get added into as_mail.
										if (!"O".equals(lsStatus))
										{
											mailList.add("P06"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr);
										}
										if (lcCheckAmt > 0 || "F".equals(lsStatus) || lsStatus.trim().length() == 0)
										{
											//Modified by Rohini T on 26/04/2021[Start]
											//failedPolicyList.add("P06"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);  //added by manish mhatre on 28-6-2019
											failedPolicyList.add("P06"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr+"\t"+"");
											//Modified by Rohini T on 26/04/2021[End]
										/*failedPolicyList.add("P06"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);*/
											/*failedPolicyList.add("P06"+"\t"+asDespId+"\t"+asSorder+"\t"+lcCheckAmt+"\t"+lsStr+"\t"+lsStatus+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);*/ 
										}
										writeLog(asSorder, ("P06"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr));
	
										////End Checking for overriden / failed / balance amt, Ruchira 28/08/2k6
									}
								}
	
								////Added Ruchira 29/08/2k6, to update the used_amt in business_logic_check table even
								///if the cr check is failed or not failed.
								if ("I".equals(asApplyTime))
								{
									//******************						gbf_credit_check_update(asSorder,'P06',adNetAmt,asApplyTime,lcCheckAmt,'U',lsStatus);
									credit_check_update(asSorder,"P06",adNetAmt,asApplyTime,lcCheckAmt,"U",lsStatus,conn);
								}
								////End Added Ruchira 29/08/2k6
	
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
	
							lsItemSerOrig = asItemSer;
							lcAdNetAmtOrig = adNetAmt;
							adNetAmt = lcAdNetAmtOrig;
							asItemSer = lsItemSerOrig;
						}
						else if ("P07".equals(lsCrPolicy.trim()) )
						{
	
							lsSql = "select case when order_type is null then '' else order_type end as order_type "
									+ " from sorder "
									+ " where sale_order = ? ";
	
							pstmt = conn.prepareStatement(lsSql);
							pstmt.setString(1,asSorder);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								lsOrderType = rs.getString("order_type");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
	
	
							lsSql = "select case when b.loc_type is null then '' else b.loc_type end as loc_type "
									+ " from sorddet a, item b "
									+ " where a.item_code__ord = b.item_code "
									+ " and a.sale_order = ? " ;
	
							pstmt = conn.prepareStatement(lsSql);
							pstmt.setString(1,asSorder);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								lsLocType = rs.getString("loc_type");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
	
							//****************						lsLocType = lsLocType.substring(0,1); //Added by jasmina DI78SUN074-30/07/08
							lsLocType = lsLocType.substring(0,1); 
							writeLog(asSorder, "P07 for [" + asSorder + "] order_type  [" + lsOrderType.trim() + "] new product [" + lsOrdNewPrd + "] loc_type [" + lsLocType + "]");
	
	
							if (!lsOrderType.trim().equals(lsOrdNewPrd.trim())) //or ( lsLocType <> 'N' and lsLocType <> 'C') then Added by jasmina DI78SUN074-30/07/08 commented by ajit on date 11-mar-2015 request#D14JSUN010
							{
	
								lcMinInvAmt = 0;
								lsSql = "select sum(min_inv_amt) as min_inv_amt "
										+ " from customer where cust_code = ? ";
								pstmt = conn.prepareStatement(lsSql);
								pstmt.setString(1,asCustCodeBil);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									lcMinInvAmt = rs.getDouble("min_inv_amt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
	
								if (lcMinInvAmt == 0 ) //if not found
								{
	
									lsSql = "select sum(min_inv_amt) as min_inv_amt "
											+ " from itemser "
											+ " where case when item_ser__crpolicy is null then item_ser else item_ser__crpolicy end = ? "
											+ " and min_inv_amt is not null";
									pstmt = conn.prepareStatement(lsSql);
									pstmt.setString(1,asItemSer);
									rs = pstmt.executeQuery();
									if (rs.next())
									{
										lcMinInvAmt = rs.getDouble("min_inv_amt");
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								}
								////Added to check first in customer master then in itemser master, as per Manoharan sir, to check for non-channel partner customer only, Ruchira 20/10/2k6(DIS5000763).
	
								writeLog(asSorder, "P07 for [" + asSorder + "] min inv amt  [" + String(lcMinInvAmt) + "] net amt [" + String(adNetAmt) + "]");
	
								//perform this check only if 'adNetAmt' > 0 atul 23.01.02
								if ( adNetAmt < lcMinInvAmt && adNetAmt > 0 )
								{
	
									////Checking for overriden / failed / balance amt, Ruchira 28/08/2k6
									//******************					gbf_credit_check_update(asSorder,'P07',adNetAmt,asApplyTime,lcCheckAmt,'C',lsStatus);
									retArrayList = credit_check_update(asSorder,"P07",adNetAmt,asApplyTime,lcCheckAmt,"C",lsStatus,conn);
	
									// added on 19/04/16 for status and amount
									//System.out.println("@@@@@@@@@  retArrayList.size()["+ retArrayList.size()+"]");
									if( retArrayList.size() > 0)
									{
										lsStatus = retArrayList.get(0);
									}
									if( retArrayList.size() > 1)
									{
										lcCheckAmt = Double.parseDouble(retArrayList.get(1)==null?"0":retArrayList.get(1));
									}	
									//System.out.println("@@@@@ lsCrPolicy["+lsCrPolicy+"]lcCheckAmt["+lcCheckAmt+"]lsStatus["+lsStatus+"]");
									
									//Changed By PriyankaC on 03June2019. [Start].
									//lsStr1 = lsCustName + " is crossing the minimum invoice limit";
									lsStr1 =	"for Sold to "+lsCustNameSoldTo +" Bill to "+lsCustNameBillTo+ " is less then the minimum invoice limit";
									//Changed By PriyankaC on 03June2019. [END]
									lsStr2 = " , Net Invoice Amt: " + adNetAmt + " is below the minimum sales value: " + lcMinInvAmt;
									//To show the Invoice/SO amt in credit chek fail description, Added Ruchira 21/08/2k6
									if ("I".equals(asApplyTime))
									{
										lsStr3 = " , Invoice Amt.=> " + adNetAmt;
									}
									else
									{
										lsStr3 = " , Sales Order Amt.=> " + adNetAmt;
									}
									lsStr =  lcCheckAmt + "\t" + lsStr1 + ", " + lsStr2 + ", " + lsStr3;
	
									if (lcCheckAmt > 0)
									{
										lsStr = lsStr + ", Overriden amount exceeds.";
									}
	
									////Always when credit check fails it must get added into as_mail.
									if (!"O".equals(lsStatus))
									{
										mailList.add("P07"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr);
									}
									if (lcCheckAmt > 0 || "F".equals(lsStatus) || lsStatus.trim().length() == 0)
									{
										//Modified by Rohini T on 26/04/2021[Start]
										//failedPolicyList.add("P07"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);  //added by manish mhatre on 28-6-2019
										failedPolicyList.add("P07"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr+"\t"+"");
										//Modified by Rohini T on 26/04/2021[End]
										/*failedPolicyList.add("P07"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);*/
										/*failedPolicyList.add("P07"+"\t"+asDespId+"\t"+asSorder+"\t"+lcCheckAmt+"\t"+lsStr+"\t"+lsStatus+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);*/ 
										writeLog(asSorder, ("P07"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr));
									}
								}
							}
	
							////Added Ruchira 29/08/2k6, to update the used_amt in business_logic_check table even
							///if the cr check is failed or not failed.
							if ("I".equals(asApplyTime))
							{
								//******************					gbf_credit_check_update(asSorder,'P07',adNetAmt,asApplyTime,lcCheckAmt,'U',lsStatus);
								credit_check_update(asSorder,"P07",adNetAmt,asApplyTime,lcCheckAmt,"U",lsStatus,conn);
							}
							////End Added Ruchira 29/08/2k6
						}
						else if ("P08".equals(lsCrPolicy.trim()) )
						{
							// checking rate of sales order with price list
							lsSql = "select price_list from sorder "
									+ " where sale_order = ?";
							pstmt = conn.prepareStatement(lsSql);
							pstmt.setString(1,asSorder);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								lsPriceList = rs.getString("price_list");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
	
							if (lsPriceList != null && lsPriceList.trim().length() > 0)
							{
								lsSql = "select item_code,rate from sorddet "
										+ " where sale_order = ?";
								pstmt = conn.prepareStatement(lsSql);
								pstmt.setString(1,asSorder);
								rs = pstmt.executeQuery();
								while (rs.next())
								{
	
									lsItemCode = rs.getString("item_code");
									lcRate = rs.getDouble("rate");
	
									lcPickRate = 0;
									lsSql = "select (case when fn_pick_rate(?, ?, ?, ' ', 'L') is null then 0 "
											+ "	else fn_pick_rate(?, ?, ?, ' ', 'L') end) as pick_rate "
											+ " from dual";
									pstmt1 = conn.prepareStatement(lsSql);
									pstmt1.setString(1,lsPriceList);
									pstmt1.setTimestamp(2,adtTranDate);
									pstmt1.setString(3,lsItemCode);
									pstmt1.setString(4,lsPriceList);
									pstmt1.setTimestamp(5,adtTranDate);
									pstmt1.setString(6,lsItemCode);
									rs1 = pstmt1.executeQuery();
									if (rs1.next())
									{
										lcPickRate = rs1.getDouble("pick_rate");
									}
									rs1.close();
									rs1 = null;
									pstmt1.close();
									pstmt1 = null;
									if ( lcPickRate != lcRate)
									{
										////Checking for overriden / failed / balance amt, Ruchira 28/08/2k6
										//******************							gbf_credit_check_update(asSorder,'P08',adNetAmt,asApplyTime,lcCheckAmt,'C',lsStatus);
										retArrayList = credit_check_update(asSorder,"P08",adNetAmt,asApplyTime,lcCheckAmt,"C",lsStatus,conn);
	
										// added on 19/04/16 for status and amount
										//System.out.println("@@@@@@@@@  retArrayList.size()["+ retArrayList.size()+"]");
										if( retArrayList.size() > 0)
										{
											lsStatus = retArrayList.get(0);
										}
										if( retArrayList.size() > 1)
										{
											lcCheckAmt = Double.parseDouble(retArrayList.get(1)==null?"0":retArrayList.get(1));
										}	
										//System.out.println("@@@@@ lsCrPolicy["+lsCrPolicy+"]lcCheckAmt["+lcCheckAmt+"]lsStatus["+lsStatus+"]");
										
										
										lsStr1 = "Rate is not matching with price list master";
										lsStr2 = " , Rate From Master: " + lcPickRate + " does not match with sales order detail rate: " + lcRate;
										//To show the Invoice/SO amt in credit chek fail description, Added Ruchira 21/08/2k6
										if ("I".equals(asApplyTime))
										{
											lsStr3 = " , Invoice Amt.=> " + adNetAmt;
										}
										else
										{
											lsStr3 = " , Sales Order Amt.=> " + adNetAmt;
										}
										lsStr =  lcCheckAmt + "\t" + lsStr1 + ", " + lsStr2 + ", " + lsStr3;
	
										if (lcCheckAmt > 0 )
										{
											lsStr = lsStr + ", Overriden amount exceeds.";
										}
										////Always when credit check fails it must get added into as_mail.
										if (!"O".equals(lsStatus))
										{
											mailList.add("P08"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr);
										}
										if (lcCheckAmt > 0 || "F".equals(lsStatus) || lsStatus.trim().length() == 0)
										{
											//Modified by Rohini T on 26/04/2021[Start]
											//failedPolicyList.add("P08"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);  //added by manish mhatre on 28-6-2019
											failedPolicyList.add("P08"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr+"\t"+"");
											//Modified by Rohini T on 26/04/2021[End]
											/*failedPolicyList.add("P08"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);*/
											/*failedPolicyList.add("P08"+"\t"+asDespId+"\t"+asSorder+"\t"+lcCheckAmt+"\t"+lsStr+"\t"+lsStatus+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);*/ 
											writeLog(asSorder, ("P08"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr));
										}
									}
									////End Checking for overriden / failed / balance amt, Ruchira 28/08/2k6
	
									///if the cr check is failed or not failed.
									if ("I".equals(asApplyTime))
									{
										//*****************							gbf_credit_check_update(asSorder,'P08',adNetAmt,asApplyTime,lcCheckAmt,'U',lsStatus);
										credit_check_update(asSorder,"P08",adNetAmt,asApplyTime,lcCheckAmt,"U",lsStatus,conn);
									}
									////End Added Ruchira 29/08/2k6
	
								}
								rs.close();//Added by Jasmina 14.05.08- DI89SUN020
								rs = null;
								pstmt.close();
								pstmt = null;
	
							}
						}
	
						else if ("P09".equals(lsCrPolicy.trim()) )
						{
							lsSql = "select sales_pers__1 from sorder where sale_order = ?";
							pstmt = conn.prepareStatement(lsSql);
							pstmt.setString(1,asSorder);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								lsSalesPers1 = rs.getString("sales_pers__1");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
	
							if (lsSalesPers1 == null )
							{
								lsSalesPers1 = " ";
							}
	
							if (lsSalesPers1.trim().length() > 0)
							{
								mTotOsAmt = 0;
								//PICKING TOTAL O/S AMT AND CREDIT LIMIT & COMPARING
								lsSql = "select sum(tot_amt - dispute_amt) - sum(adj_amt) as tot_os_amt "
										+ " from  receivables "
										+ " where sales_pers__1 = ? "
										+ " and ref_type <> ?";
								pstmt = conn.prepareStatement(lsSql);
								pstmt.setString(1,lsSalesPers1);
								pstmt.setString(2,lsOrdNewPrd);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									mTotOsAmt = rs.getDouble("tot_os_amt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
	
								/// select consider pending purchase order or not
								/// amish 3-10-03
								lsSql = "select consider_pbo "
										+ " from itemser_cr_policy " 
										+ " where item_ser = ? and cr_policy = 'P09'";
	
								pstmt = conn.prepareStatement(lsSql);
								pstmt.setString(1,asItemSer);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									lsConsiderPbo = rs.getString("consider_pbo");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
	
								if (lsConsiderPbo == null || lsConsiderPbo.trim().length() == 0 )
								{
									lsConsiderPbo = "Y";
								}
	
								if ("Y".equals(lsConsiderPbo))
								{
									/// amish 13-08-03
									/// pick up the outstanding order amount. on the basis of the despatch percentage.
									/// order should be pending and order should not be the same as the current order.
									lsSql = "SELECT round(sum( sorddet.net_amt * (100 - ( (case when sorditem.qty_desp is null then 0 else sorditem.qty_desp end ) / sorditem.qty_ord * 100) ) / 100 ),3) as pend_amt "
											+ " FROM sorddet, sorder, sorditem "
											+ " WHERE ( sorddet.sale_order = sorditem.sale_order ) "
											+ " and	( sorddet.line_no = sorditem.line_no ) "
											+ " and ( sorder.sale_order = sorddet.sale_order ) "
											+ " and	( sorder.sales_pers__1 = ? ) "
											+ " and	( sorder.status = 'P' ) "
											+ " and ( sorder.order_type <> ? ) "
											+ " and ( sorditem.qty_ord > 0 ) "
											+ " and ( sorder.confirmed = 'Y' ) ";
	
									pstmt = conn.prepareStatement(lsSql);
									pstmt.setString(1,lsSalesPers1);
									pstmt.setString(2,lsOrdNewPrd);
									rs = pstmt.executeQuery();
									if (rs.next())
									{
										lcPendOrd = rs.getDouble("pend_amt");
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
	
								}
								else
								{
									lcPendOrd = 0;
								}
	
								//TAKING INTO CONSIDERATION THE CURRENT INVOICE AMOUNT
								mTotOsAmt = mTotOsAmt + adNetAmt + lcPendOrd;
	
								lsSql = "select (case when credit_lmt is null then 0 else credit_lmt end) as credit_lmt "
										+ " from sales_pers where sales_pers = ? ";
	
								pstmt = conn.prepareStatement(lsSql);
								pstmt.setString(1,lsSalesPers1);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									mCrLimit = rs.getDouble("credit_lmt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
	
								if (mTotOsAmt > mCrLimit && "C".equals(mStat))
								{
	
									////Checking for overriden / failed / balance amt, Ruchira 28/08/2k6
									//******************						gbf_credit_check_update(asSorder,'P09',adNetAmt,asApplyTime,lcCheckAmt,'C',lsStatus);
									retArrayList = credit_check_update(asSorder,"P09",adNetAmt,asApplyTime,lcCheckAmt,"C",lsStatus,conn);
	
									// added on 19/04/16 for status and amount
									//System.out.println("@@@@@@@@@  retArrayList.size()["+ retArrayList.size()+"]");
									if( retArrayList.size() > 0)
									{
										lsStatus = retArrayList.get(0);
									}
									if( retArrayList.size() > 1)
									{
										lcCheckAmt = Double.parseDouble(retArrayList.get(1)==null?"0":retArrayList.get(1));
									}	
									//System.out.println("@@@@@ lsCrPolicy["+lsCrPolicy+"]lcCheckAmt["+lcCheckAmt+"]lsStatus["+lsStatus+"]");
									
									
									lsSql = "select sp_name from sales_pers where sales_pers = ?";
	
									pstmt = conn.prepareStatement(lsSql);
									pstmt.setString(1,lsSalesPers1);
									rs = pstmt.executeQuery();
									if (rs.next())
									{
										lsSlpersName = rs.getString("sp_name");
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
	
									lsStr1 = "Overall credit check failed for sales person " + lsSlpersName;
									lsStr2 = " , Total O/S Amt : " + utilMethods.getReqDecString(mTotOsAmt,3) + " exceeds credit limit: " + mCrLimit;
									//To show the Invoice/SO amt in credit chek fail description, Added Ruchira 21/08/2k6
									if ("I".equals(asApplyTime))
									{
										lsStr3 = " , Invoice Amt.=> " + adNetAmt;
									}
									else
									{
										lsStr3 = " , Sales Order Amt.=> " + adNetAmt;
									}
									lsStr =  lcCheckAmt + "\t" + lsStr1 + ", " + lsStr2 + ", " + lsStr3;
	
									if (lcCheckAmt > 0)
									{
										lsStr = lsStr + ", Overriden amount exceeds.";
									}
									////Always when credit check fails it must get added into as_mail.
									if (!"O".equals(lsStatus))
									{
										mailList.add("P09"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr);
									}
									if (lcCheckAmt > 0 || "F".equals(lsStatus) || lsStatus.trim().length() == 0)
									{
										//Modified by Rohini T on 26/04/2021[Start]
										//failedPolicyList.add("P09"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);   //added by manish mhatre on 28-6-2019
										failedPolicyList.add("P09"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr+"\t"+"");
										//Modified by Rohini T on 26/04/2021[End]
										/*failedPolicyList.add("P09"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);*/
										/*failedPolicyList.add("P09"+"\t"+asDespId+"\t"+asSorder+"\t"+lcCheckAmt+"\t"+lsStr+"\t"+lsStatus+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);*/ 
										writeLog(asSorder, ("P09"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr));
									}
	
								}
								////Added Ruchira 29/08/2k6, to update the used_amt in business_logic_check table even
								///if the cr check is failed or not failed.
								if ("I".equals(asApplyTime))
								{
									//******************						gbf_credit_check_update(asSorder,'P09',adNetAmt,asApplyTime,lcCheckAmt,'U',lsStatus)
									credit_check_update(asSorder,"P09",adNetAmt,asApplyTime,lcCheckAmt,"U",lsStatus,conn);
								}
								////End Added Ruchira 29/08/2k6
							}
						}
						else if ("P10".equals(lsCrPolicy.trim()) )
						{
							/// cash discount credit check for sales person 1
	
							// amish 12-08-03
							// Credit check for --
							// if the over due outstanding amount is grater then 0 and if the cash discount is allowed then
							// select the cash tax coe of cash discount from disparm
	
							lsSql = "select sales_pers__1 from sorder where sale_order = ?";
							pstmt = conn.prepareStatement(lsSql);
							pstmt.setString(1,asSorder);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								lsSalesPers1 = rs.getString("sales_pers__1");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
	
							if (lsSalesPers1 == null )
							{
								lsSalesPers1 = " ";
							}
	
							if (lsSalesPers1.trim().length() > 0)
							{
	
								lsSql = "select fn_sysdate() from dual";
	
								pstmt = conn.prepareStatement(lsSql);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									ldToday = rs.getTimestamp(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
								//SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
								
								ldToday = java.sql.Timestamp.valueOf(sdf1.format(ldToday) + " 00:00:00.0");
								//PICKING TOTAL O/S AMT AND Cash disc & COMPARING
								//Nvl Added by Sharon on 22-Sep-2004
								lsSql = "select sum((case when tot_amt is null then 0 else tot_amt end) - (case when dispute_amt is null then 0 else dispute_amt end)) - sum(case when adj_amt is null then 0 else adj_amt end) as tot_os_amt "
										+ " from  receivables "
										+ " where sales_pers__1 = ? "
										+ " and ref_type <> ? and due_date < ?";
								pstmt = conn.prepareStatement(lsSql);
								pstmt.setString(1,lsSalesPers1);
								pstmt.setString(2,lsOrdNewPrd);
								pstmt.setTimestamp(3,ldToday);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									mTotOsAmt = rs.getDouble("tot_os_amt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
	
								/// if total cash discount is grater then zero and over due outstanding is exists.
								if (lcTaxAmt > 0 && mTotOsAmt > 0)
								{
	
									////Checking for overriden / failed / balance amt, Ruchira 28/08/2k6
									//******************					gbf_credit_check_update(asSorder,'P10',adNetAmt,asApplyTime,lcCheckAmt,'C',lsStatus)
									retArrayList = credit_check_update(asSorder,"P10",adNetAmt,asApplyTime,lcCheckAmt,"C",lsStatus,conn);
	
									// added on 19/04/16 for status and amount
									//System.out.println("@@@@@@@@@  retArrayList.size()["+ retArrayList.size()+"]");
									if( retArrayList.size() > 0)
									{
										lsStatus = retArrayList.get(0);
									}
									if( retArrayList.size() > 1)
									{
										lcCheckAmt = Double.parseDouble(retArrayList.get(1)==null?"0":retArrayList.get(1));
									}	
									//System.out.println("@@@@@ lsCrPolicy["+lsCrPolicy+"]lcCheckAmt["+lcCheckAmt+"]lsStatus["+lsStatus+"]");
									
									
									
									lsSql = "select sp_name from sales_pers where sales_pers = ?";
	
									pstmt = conn.prepareStatement(lsSql);
									pstmt.setString(1,lsSalesPers1);
									rs = pstmt.executeQuery();
									if (rs.next())
									{
										lsSlpersName = rs.getString("sp_name");
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
	
									lsStr1 = "Cash discount credit check failed for sales person " + lsSlpersName;
									lsStr2 = " , Total over due O/S Amt: " + utilMethods.getReqDecString(mTotOsAmt,3);
									//To show the Invoice/SO amt in credit chek fail description, Added Ruchira 21/08/2k6
									if ("I".equals(asApplyTime))
									{
										lsStr3 = " , Invoice Amt.=> " + adNetAmt;
									}
									else
									{
										lsStr3 = " , Sales Order Amt.=> " + adNetAmt;
									}
									lsStr =  lcCheckAmt + "\t" + lsStr1 + ", " + lsStr2 + ", " + lsStr3;
	
									if (lcCheckAmt > 0 )
									{
										lsStr = lsStr + ", Overriden amount exceeds.";
									}
									////Always when credit check fails it must get added into as_mail.
									if (!"O".equals(lsStatus))
									{
										mailList.add("P10"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr);
									}
									if (lcCheckAmt > 0 || "F".equals(lsStatus) || lsStatus.trim().length() == 0)
									{
										//Modified by Rohini T on 26/04/2021[Start]
										//failedPolicyList.add("P10"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);   //added by manish mhatre on 28-6-2019
										failedPolicyList.add("P10"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr+"\t"+"");
										//Modified by Rohini T on 26/04/2021[End]
										/*failedPolicyList.add("P10"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);*/
										/*failedPolicyList.add("P10"+"\t"+asDespId+"\t"+asSorder+"\t"+lcCheckAmt+"\t"+lsStr+"\t"+lsStatus+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);*/ 
										writeLog(asSorder, ("P10"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr));
									}
	
								}
	
								////Added Ruchira 29/08/2k6, to update the used_amt in business_logic_check table even
								///if the cr check is failed or not failed.
								if ("I".equals(asApplyTime))
								{
									//******************					gbf_credit_check_update(asSorder,'P10',adNetAmt,asApplyTime,lcCheckAmt,'U',lsStatus)
									credit_check_update(asSorder,"P10",adNetAmt,asApplyTime,lcCheckAmt,"U",lsStatus,conn);
								}
								////End Added Ruchira 29/08/2k6
							}
						}
						else if ("P11".equals(lsCrPolicy.trim()) )
						{
							/// credit check for sales person.
	
							lsSql = "select sales_pers from sorder where sale_order = ?";
							pstmt = conn.prepareStatement(lsSql);
							pstmt.setString(1,asSorder);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								lsSalesPers = rs.getString("sales_pers");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
	
							if (lsSalesPers == null )
							{
								lsSalesPers = " ";
							}
	
							if (lsSalesPers.trim().length() > 0)
							{
								mTotOsAmt = 0;
								//PICKING TOTAL O/S AMT AND CREDIT LIMIT & COMPARING
								//Nvl Added by Sharon on 22-Sep-2004
								lsSql = "select sum((case when tot_amt is null then 0 else tot_amt end) - (case when dispute_amt is null then 0 else dispute_amt end)) - sum(case when adj_amt is null then 0 else adj_amt end) as tot_os_amt "
										+ " from  receivables "
										+ " where sales_pers = ? "
										+ " and ref_type <> ? ";
								pstmt = conn.prepareStatement(lsSql);
								pstmt.setString(1,lsSalesPers);
								pstmt.setString(2,lsOrdNewPrd);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									mTotOsAmt = rs.getDouble("tot_os_amt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
	
								/// select consider pending purchase order or not
								/// amish 3-10-03
								lsSql = "select consider_pbo "
										+ " from itemser_cr_policy " 
										+ " where item_ser = ? and cr_policy = 'P11'";
	
								pstmt = conn.prepareStatement(lsSql);
								pstmt.setString(1,asItemSer);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									lsConsiderPbo = rs.getString("consider_pbo");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
	
								if (lsConsiderPbo == null || lsConsiderPbo.trim().length() == 0 )
								{
									lsConsiderPbo = "Y";
								}
	
								if ("Y".equals(lsConsiderPbo))
								{
									/// amish 13-08-03
									/// pick up the outstanding order amount. on the basis of the despatch percentage.
									/// order should be pending and order should not be the same as the current order.
									lsSql = "sum( sorddet.net_amt * (100 - ( (case when sorditem.qty_desp is null then 0 else sorditem.qty_desp end ) / sorditem.qty_ord * 100) ) / 100 ) as pend_amt "
											+ " FROM sorddet, sorder, sorditem "
											+ " WHERE ( sorddet.sale_order = sorditem.sale_order ) "
											+ " and	( sorddet.line_no = sorditem.line_no ) "
											+ " and ( sorder.sale_order = sorddet.sale_order ) "
											+ " and	( sorder.sales_pers = ? ) "
											+ " and	( sorder.status = 'P' ) "
											+ " and ( sorder.order_type <> ? ) "
											+ " and ( sorditem.qty_ord > 0 ) "
											+ " and ( sorder.confirmed = 'Y' ) ";
	
									pstmt = conn.prepareStatement(lsSql);
									pstmt.setString(1,lsSalesPers1);
									pstmt.setString(2,lsOrdNewPrd);
									rs = pstmt.executeQuery();
									if (rs.next())
									{
										lcPendOrd = rs.getDouble("pend_amt");
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								}
								else
								{
									lcPendOrd = 0;
								}
	
								//TAKING INTO CONSIDERATION THE CURRENT INVOICE AMOUNT
								mTotOsAmt = mTotOsAmt + adNetAmt + lcPendOrd;
								lsSql = "select (case when credit_lmt is null then 0 else credit_lmt end) as credit_lmt "
										+ " from sales_pers where sales_pers = ? ";
	
								pstmt = conn.prepareStatement(lsSql);
								pstmt.setString(1,lsSalesPers1);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									mCrLimit = rs.getDouble("credit_lmt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
	
								if (mTotOsAmt > mCrLimit && "C".equals(mStat))
								{
	
									////Checking for overriden / failed / balance amt, Ruchira 28/08/2k6
									//******************						gbf_credit_check_update(asSorder,'P11',adNetAmt,asApplyTime,lcCheckAmt,'C',lsStatus);
									retArrayList = credit_check_update(asSorder,"P11",adNetAmt,asApplyTime,lcCheckAmt,"C",lsStatus,conn);
	
									// added on 19/04/16 for status and amount
									//System.out.println("@@@@@@@@@  retArrayList.size()["+ retArrayList.size()+"]");
									if( retArrayList.size() > 0)
									{
										lsStatus = retArrayList.get(0);
									}
									if( retArrayList.size() > 1)
									{
										lcCheckAmt = Double.parseDouble(retArrayList.get(1)==null?"0":retArrayList.get(1));
									}	
									//System.out.println("@@@@@ lsCrPolicy["+lsCrPolicy+"]lcCheckAmt["+lcCheckAmt+"]lsStatus["+lsStatus+"]");
									
									
									lsSql = "select sp_name from sales_pers where sales_pers = ?";
	
									pstmt = conn.prepareStatement(lsSql);
									pstmt.setString(1,lsSalesPers);
									rs = pstmt.executeQuery();
									if (rs.next())
									{
										lsSlpersName = rs.getString("sp_name");
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
	
									lsStr1 = "Overall credit check failed for sales person " + lsSlpersName;
									lsStr2 = " , Total O/S Amt : " + utilMethods.getReqDecString(mTotOsAmt,3) + " exceeds credit limit: " + mCrLimit;
									//To show the Invoice/SO amt in credit chek fail description, Added Ruchira 21/08/2k6
									if ("I".equals(asApplyTime))
									{
										lsStr3 = " , Invoice Amt.=> " + adNetAmt;
									}	
									else
									{		
										lsStr3 = " , Sales Order Amt.=> " + adNetAmt;
									}
									lsStr =  lcCheckAmt + "\t" + lsStr1 + ", " + lsStr2 + ", " + lsStr3;
	
									if (lcCheckAmt > 0 )
									{
										lsStr = lsStr + ", Overriden amount exceeds.";
									}
									////Always when credit check fails it must get added into as_mail.
									if (!"O".equals(lsStatus))
									{
										mailList.add("P11"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr);
									}
									if (lcCheckAmt > 0 || "F".equals(lsStatus) || lsStatus.trim().length() == 0)
									{
										//Modified by Rohini T on 26/04/2021[Start]
										//failedPolicyList.add("P11"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);    //added by manish mhatre on 28-6-2019
										failedPolicyList.add("P11"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr+"\t"+"");
										//Modified by Rohini T on 26/04/2021[End]
										/*failedPolicyList.add("P11"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);*/
										/*failedPolicyList.add("P11"+"\t"+asDespId+"\t"+asSorder+"\t"+lcCheckAmt+"\t"+lsStr+"\t"+lsStatus+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);*/ 
										writeLog(asSorder, ("P11"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr));
									}
	
								}
	
								////Added Ruchira 29/08/2k6, to update the used_amt in business_logic_check table even
								///if the cr check is failed or not failed.
								if ("I".equals(asApplyTime))
								{
									//**********						gbf_credit_check_update(asSorder,'P11',adNetAmt,asApplyTime,lcCheckAmt,'U',lsStatus)
									credit_check_update(asSorder,"P11",adNetAmt,asApplyTime,lcCheckAmt,"U",lsStatus,conn);
								}
								////End Added Ruchira 29/08/2k6
							}
						}
						else if ("P12".equals(lsCrPolicy.trim()) )
						{
	
							/// cash discount credit check for sales person
	
							// amish 12-08-03
							// Credit check for --
							// if the over due outstanding amount is grater then 0 and if the cash discount is allowed then
							// select the cash tax coe of cash discount from disparm
							lsSql = "select sales_pers from sorder where sale_order = ?";
							pstmt = conn.prepareStatement(lsSql);
							pstmt.setString(1,asSorder);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								lsSalesPers = rs.getString("sales_pers");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
	
							if (lsSalesPers == null )
							{
								lsSalesPers = " ";
							}
	
							if (lsSalesPers.trim().length() > 0)
							{
								mTotOsAmt = 0;
	
								lsSql = "select fn_sysdate() from dual";
	
								pstmt = conn.prepareStatement(lsSql);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									ldToday = rs.getTimestamp(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
	
								//SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
								SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
								ldToday = java.sql.Timestamp.valueOf(sdf1.format(ldToday) + " 00:00:00.0");
	
								//PICKING TOTAL O/S AMT AND Cash disc & COMPARING
								//Nvl Added by Sharon on 22-Sep-2004
								lsSql = "select sum((case when tot_amt is null then 0 else tot_amt end) - (case when dispute_amt is null then 0 else dispute_amt end)) - sum(case when adj_amt is null then 0 else adj_amt end) as tot_os_amt "
										+ " from  receivables "
										+ " where sales_pers = ? "
										+ " and ref_type <> ? and due_date < ?";
								pstmt = conn.prepareStatement(lsSql);
								pstmt.setString(1,lsSalesPers);
								pstmt.setString(2,lsOrdNewPrd);
								pstmt.setTimestamp(3,ldToday);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									mTotOsAmt = rs.getDouble("tot_os_amt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
	
								/// if total cash discount is grater then zero and over due outstanding is exists.
								if (lcTaxAmt > 0 && mTotOsAmt > 0)
								{
									////Checking for overriden / failed / balance amt, Ruchira 28/08/2k6
									//******************					gbf_credit_check_update(asSorder,'P12',adNetAmt,asApplyTime,lcCheckAmt,'C',lsStatus);
									retArrayList =  credit_check_update(asSorder,"P12",adNetAmt,asApplyTime,lcCheckAmt,"C",lsStatus,conn);
	
									// added on 19/04/16 for status and amount
									//System.out.println("@@@@@@@@@  retArrayList.size()["+ retArrayList.size()+"]");
									if( retArrayList.size() > 0)
									{
										lsStatus = retArrayList.get(0);
									}
									if( retArrayList.size() > 1)
									{
										lcCheckAmt = Double.parseDouble(retArrayList.get(1)==null?"0":retArrayList.get(1));
									}	
									//System.out.println("@@@@@ lsCrPolicy["+lsCrPolicy+"]lcCheckAmt["+lcCheckAmt+"]lsStatus["+lsStatus+"]");
									
									
									lsSql = "select sp_name from sales_pers where sales_pers = ?";
	
									pstmt = conn.prepareStatement(lsSql);
									pstmt.setString(1,lsSalesPers);
									rs = pstmt.executeQuery();
									if (rs.next())
									{
										lsSlpersName = rs.getString("sp_name");
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
	
									lsStr1 = "Cash discount credit check failed for sales person " + lsSlpersName;
									lsStr2 = " , Total over due O/S Amt: " + utilMethods.getReqDecString(mTotOsAmt,3);
									//To show the Invoice/SO amt in credit chek fail description, Added Ruchira 21/08/2k6
									if ("I".equals(asApplyTime))
									{
										lsStr3 = " , Invoice Amt.=> " + adNetAmt;
									}
									else
									{
										lsStr3 = " , Sales Order Amt.=> " + adNetAmt;
									}
									lsStr = lcCheckAmt + "\t" + lsStr1 + ", " + lsStr2 + ", " + lsStr3;
									if( lcCheckAmt > 0 )
									{
										lsStr = lsStr + ", Overriden amount exceeds.";
									}
									////Always when credit check fails it must get added into as_mail.
									if (!"O".equals(lsStatus))
									{
										mailList.add("P12"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr);
									}
									if (lcCheckAmt > 0 || "F".equals(lsStatus) || lsStatus.trim().length() == 0)
									{
										//Modified by Rohini T on 26/04/2021[Start]
										//failedPolicyList.add("P12"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);   //added by manish mhatre on 28-6-2019
										failedPolicyList.add("P12"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr+"\t"+"");
										//Modified by Rohini T on 26/04/2021[End]
										/*failedPolicyList.add("P12"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);*/
										/*failedPolicyList.add("P12"+"\t"+asDespId+"\t"+asSorder+"\t"+lcCheckAmt+"\t"+lsStr+"\t"+lsStatus+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);*/ 
										writeLog(asSorder, ("P12"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr));
									}
	
								}
	
								////Added Ruchira 29/08/2k6, to update the used_amt in business_logic_check table even
								///if the cr check is failed or not failed.
								if ("I".equals(asApplyTime))
								{
									//******************					gbf_credit_check_update(asSorder,'P12',adNetAmt,asApplyTime,lcCheckAmt,'U',lsStatus)
									credit_check_update(asSorder,"P12",adNetAmt,asApplyTime,lcCheckAmt,"U",lsStatus,conn);
								}
							}
						}
						else if ("P13".equals(lsCrPolicy.trim()) )
						{
							// credit limit check for contact code.
	
							// amish 12-08-03
							// changes made for contact code //
							// pick up the contact code from customer table and check the credit limit.
							lsSql = "select customer.contact_code as contact_code from sorder , customer "
									+ " where sorder.cust_code = customer.cust_code "
									+ " and sorder.sale_order = ? ";
	
							pstmt = conn.prepareStatement(lsSql);
							pstmt.setString(1,asSorder);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								lsContactCode = rs.getString("contact_code");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if (lsContactCode == null )
							{
								lsContactCode = " ";
							}
							if (lsContactCode.trim().length() > 0 )
							{
								mTotOsAmt = 0;
								//PICKING TOTAL O/S AMT AND CREDIT LIMIT & COMPARING
								//Nvl Added by Sharon on 22-Sep-2004
								lsSql = "select sum((case when tot_amt is null then 0 else tot_amt end) - (case when dispute_amt is null then 0 else dispute_amt end)) - sum(case when adj_amt is null then 0 else adj_amt end) as tot_os_amt "
										+ " from  receivables "
										+ " where contact_code = ? "
										+ " and ref_type <> ?";
	
								pstmt = conn.prepareStatement(lsSql);
								pstmt.setString(1,lsContactCode);
								pstmt.setString(2,lsOrdNewPrd);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									mTotOsAmt = rs.getDouble("tot_os_amt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
	
								/// select consider pending purchase order or not
								/// amish 3-10-03
								lsSql = "select consider_pbo "
										+ " from itemser_cr_policy " 
										+ " where item_ser = ? and cr_policy = 'P13'";
	
								pstmt = conn.prepareStatement(lsSql);
								pstmt.setString(1,asItemSer);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									lsConsiderPbo = rs.getString("consider_pbo");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
	
								if (lsConsiderPbo == null || lsConsiderPbo.trim().length() == 0 )
								{
									lsConsiderPbo = "Y";
								}
	
								if ("Y".equals(lsConsiderPbo))
								{
									/// amish 13-08-03
									/// pick up the outstanding order amount. on the basis of the despatch percentage.
									/// order should be pending and order should not be the same as the current order.
									//Modified by Sana S on 22/05/20 [start][and clause is missing in given query] 
									/*
									 * lsSql =
									 * "SELECT round(sum( sorddet.net_amt * (100 - ( (case when sorditem.qty_desp is null then 0 else sorditem.qty_desp end ) / sorditem.qty_ord * 100) ) / 100 ),3) as pend_amt "
									 * + " FROM customer, sorddet, sorder, sorditem " +
									 * " WHERE ( sorddet.sale_order = sorditem.sale_order ) " +
									 * " ( sorddet.line_no = sorditem.line_no ) " +
									 * " and ( sorder.sale_order = sorddet.sale_order ) " +
									 * " and ( sorder.cust_code__bil = customer.cust_code ) " +
									 * " and ( customer.contact_code = ? ) " + " and ( sorder.status = 'P' ) " +
									 * " and (sorder.order_type <> ? ) " + " and ( sorditem.qty_ord > 0 ) " +
									 * " and ( sorder.confirmed = 'Y' )";
									 */
									lsSql = "SELECT round(sum( sorddet.net_amt * (100 - ( (case when sorditem.qty_desp is null then 0 else sorditem.qty_desp end ) / sorditem.qty_ord * 100) ) / 100 ),3) as pend_amt "
											+ " FROM customer, sorddet, sorder, sorditem "
											+ " WHERE ( sorddet.sale_order = sorditem.sale_order ) "
											+ " and ( sorddet.line_no = sorditem.line_no ) "
											+ " and ( sorder.sale_order = sorddet.sale_order ) "
											+ " and ( sorder.cust_code__bil = customer.cust_code ) "
											+ " and ( customer.contact_code = ? ) "
											+ " and ( sorder.status = 'P' ) "
											+ " and (sorder.order_type <> ? ) "
											+ " and ( sorditem.qty_ord > 0 ) "
											+ " and ( sorder.confirmed = 'Y' )";
									//Modified by Sana S on 22/05/20 [end][and clause is missing in given query] 
									pstmt = conn.prepareStatement(lsSql);
									pstmt.setString(1,lsContactCode);
									pstmt.setString(2,lsOrdNewPrd);
									rs = pstmt.executeQuery();
									if (rs.next())
									{
										lcPendOrd = rs.getDouble("pend_amt");
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
	
								}
								else
								{
									lcPendOrd = 0;
								}
	
	
								//TAKING INTO CONSIDERATION THE CURRENT INVOICE AMOUNT
								mTotOsAmt = mTotOsAmt + adNetAmt + lcPendOrd;
	
								lsSql = "select case when credit_lmt is null then 0 else credit_lmt end as credit_lmt "
										+ " from contact " 
										+ " where contact_code = ? ";
	
								pstmt = conn.prepareStatement(lsSql);
								pstmt.setString(1,lsContactCode);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									mCrLimit = rs.getDouble("credit_lmt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
	
								if (mTotOsAmt > mCrLimit &&  "C".equals(mStat))
								{
									////Checking for overriden / failed / balance amt, Ruchira 28/08/2k6
									//******************						gbf_credit_check_update(asSorder,'P13',adNetAmt,asApplyTime,lcCheckAmt,'C',lsStatus);
									retArrayList = credit_check_update(asSorder,"P13",adNetAmt,asApplyTime,lcCheckAmt,"C",lsStatus,conn);
	
									// added on 19/04/16 for status and amount
									//System.out.println("@@@@@@@@@  retArrayList.size()["+ retArrayList.size()+"]");
									if( retArrayList.size() > 0)
									{
										lsStatus = retArrayList.get(0);
									}
									if( retArrayList.size() > 1)
									{
										lcCheckAmt = Double.parseDouble(retArrayList.get(1)==null?"0":retArrayList.get(1));
									}	
									//System.out.println("@@@@@ lsCrPolicy["+lsCrPolicy+"]lcCheckAmt["+lcCheckAmt+"]lsStatus["+lsStatus+"]");
									
									
									lsSql = "select name from contact where contact_code = ? ";
	
									pstmt = conn.prepareStatement(lsSql);
									pstmt.setString(1,lsContactCode);
									rs = pstmt.executeQuery();
									if (rs.next())
									{
										//Modified by Sana S on 22/05/20 [start][invalid column name]
										//lsContactName = rs.getString("contact_code");
										lsContactName = rs.getString("name");
										//Modified by Sana S on 22/05/20 [end][invalid column name]
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
	
									lsStr1 = "Overall credit check failed for contact person " + lsContactName;
									lsStr2 = " , Total O/S Amt : " + utilMethods.getReqDecString(mTotOsAmt,3) + " exceeds credit limit: " + mCrLimit;
									//To show the Invoice/SO amt in credit chek fail description, Added Ruchira 21/08/2k6
									if ("I".equals(asApplyTime))
									{
										lsStr3 = " , Invoice Amt.=> " + adNetAmt;
									}
									else
									{
										lsStr3 = " , Sales Order Amt.=> " + adNetAmt;
									}
									lsStr = lcCheckAmt + "\t" + lsStr1 + ", " + lsStr2 + ", " + lsStr3;
									if (lcCheckAmt > 0 )
									{
										lsStr = lsStr + ", Overriden amount exceeds.";
									}
									////Always when credit check fails it must get added into as_mail.
									if (!"O".equals(lsStatus))
									{
										mailList.add("P13"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr);
									}
									if (lcCheckAmt > 0 || "F".equals(lsStatus) || lsStatus.trim().length() == 0)
									{
										//Modified by Rohini T on 26/04/2021[Start]
										//failedPolicyList.add("P13"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);   //added by manish mhatre on 28-6-2019
										failedPolicyList.add("P13"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr+"\t"+"");
										//Modified by Rohini T on 26/04/2021[End]
										/*failedPolicyList.add("P13"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);*/
										/*failedPolicyList.add("P13"+"\t"+asDespId+"\t"+asSorder+"\t"+lcCheckAmt+"\t"+lsStr+"\t"+lsStatus+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);*/ 
										writeLog(asSorder, ("P13"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr));
									}
	
									///if the cr check is failed or not failed.
									if ("I".equals(asApplyTime))
									{
										//******************						gbf_credit_check_update(asSorder,'P13',adNetAmt,asApplyTime,lcCheckAmt,'U',lsStatus)
										credit_check_update(asSorder,"P13",adNetAmt,asApplyTime,lcCheckAmt,"U",lsStatus,conn);
									}
									////End Added Ruchira 29/08/2k6
								}
							}
						}
						else if ("P14".equals(lsCrPolicy.trim()) )
						{
	
							/// cash discount credit check for contact code
							// amish 12-08-03
							// Credit check for --
							// if the over due outstanding amount is grater then 0 and if the cash discount is allowed then
							// select the cash tax coe of cash discount from disparm
							lsSql = "select customer.contact_code as contact_code from sorder , customer "
									+ " where sorder.cust_code = customer.cust_code "
									+ " and sorder.sale_order = ? ";
	
							pstmt = conn.prepareStatement(lsSql);
							pstmt.setString(1,asSorder);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								lsContactCode = rs.getString("contact_code");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if (lsContactCode == null )
							{
								lsContactCode = " ";
							}
							if (lsContactCode.trim().length() > 0 )
							{
	
								lsSql = "select fn_sysdate() from dual";
	
								pstmt = conn.prepareStatement(lsSql);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									ldToday = rs.getTimestamp(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
	
								//SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
								SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
								ldToday = java.sql.Timestamp.valueOf(sdf1.format(ldToday) + " 00:00:00.0");
								//PICKING TOTAL O/S AMT AND Cash disc & COMPARING
								//Nvl Added by Sharon on 22-Sep-2004
								lsSql = "select sum((case when tot_amt is null then 0 else tot_amt end) - (case when dispute_amt is null then 0 else dispute_amt end)) - sum(case when adj_amt is null then 0 else adj_amt end) as tot_os_amt "
										+ " from  receivables "
										+ " where contact_code = ? "
										+ " and ref_type <> ? "
										+ " and due_date <  ? ";
	
								pstmt = conn.prepareStatement(lsSql);
								pstmt.setString(1,lsContactCode);
								pstmt.setString(2,lsOrdNewPrd);
								pstmt.setTimestamp(3,ldToday);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									mTotOsAmt = rs.getDouble("tot_os_amt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
	
								/// if total cash discount is grater then zero and over due outstanding is exists.
								if (lcTaxAmt > 0 && mTotOsAmt > 0)
								{
	
									////Checking for overriden / failed / balance amt, Ruchira 28/08/2k6
									//******************					gbf_credit_check_update(asSorder,'P14',adNetAmt,asApplyTime,lcCheckAmt,'C',lsStatus)
	
									retArrayList =  credit_check_update(asSorder,"P14",adNetAmt,asApplyTime,lcCheckAmt,"C",lsStatus,conn);
	
									// added on 19/04/16 for status and amount
									//System.out.println("@@@@@@@@@  retArrayList.size()["+ retArrayList.size()+"]");
									if( retArrayList.size() > 0)
									{
										lsStatus = retArrayList.get(0);
									}
									if( retArrayList.size() > 1)
									{
										lcCheckAmt = Double.parseDouble(retArrayList.get(1)==null?"0":retArrayList.get(1));
									}	
									//System.out.println("@@@@@ lsCrPolicy["+lsCrPolicy+"]lcCheckAmt["+lcCheckAmt+"]lsStatus["+lsStatus+"]");
									
									
									lsSql = "select name from contact where contact_code = ? ";
	
									pstmt = conn.prepareStatement(lsSql);
									pstmt.setString(1,lsContactCode);
									rs = pstmt.executeQuery();
									if (rs.next())
									{
										//Modified by Sana S on 22/05/20 [start][invalid column name]
										//lsContactName = rs.getString("contact_code");
										lsContactName = rs.getString("name");
										//Modified by Sana S on 22/05/20 [end][invalid column name]
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
	
									lsStr1 = "Cash discount credit check failed for contact person " + lsContactName;
									lsStr2 = " , Total over due O/S Amt: " + utilMethods.getReqDecString(mTotOsAmt,3);
									//To show the Invoice/SO amt in credit chek fail description, Added Ruchira 21/08/2k6
									if ("I".equals(asApplyTime))
									{
										lsStr3 = " , Invoice Amt.=> " + adNetAmt;
									}
									else
									{
										lsStr3 = " , Sales Order Amt.=> " + adNetAmt;
									}
									lsStr = lcCheckAmt + "\t" + lsStr1 + ", " + lsStr2 + ", " + lsStr3;
									if (lcCheckAmt > 0 )
									{
										lsStr = lsStr + ", Overriden amount exceeds.";
									}								
									/*if (lcCheckAmt > 0 )
									{
										lsStr = lsStr + ", Overriden amount exceeds.";
									}*/
									////Always when credit check fails it must get added into as_mail.
									if (!"O".equals(lsStatus))
									{
										mailList.add("P14"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr);
									}
									if (lcCheckAmt > 0 || "F".equals(lsStatus) || lsStatus.trim().length() == 0)
									{
										//Modified by Rohini T on 26/04/2021[Start]
										//failedPolicyList.add("P14"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);   //added by manish mhatre on 28-6-2019
										failedPolicyList.add("P14"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr+"\t"+"");
										//Modified by Rohini T on 26/04/2021[End]
										/*failedPolicyList.add("P14"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);*/
										/*failedPolicyList.add("P14"+"\t"+asDespId+"\t"+asSorder+"\t"+lcCheckAmt+"\t"+lsStr+"\t"+lsStatus+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);*/ 
										writeLog(asSorder, ("P14"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr));
									}
									///if the cr check is failed or not failed.
									if ("I".equals(asApplyTime))
									{
										//******************					gbf_credit_check_update(asSorder,'P14',adNetAmt,asApplyTime,lcCheckAmt,'U',lsStatus)
										credit_check_update(asSorder,"P14",adNetAmt,asApplyTime,lcCheckAmt,"'U",lsStatus,conn);
									}
									////End Added Ruchira 29/08/2k6
								}
							}
						}
						else if ("P15".equals(lsCrPolicy.trim()) )
						{
							// cash discount for customer.
							// amish 12-08-03
							// Credit check for --
							// if the over due outstanding amount is grater then 0 and if the cash discount is allowed then
							// select the cash tax coe of cash discount from disparm
							lsSql = "select fn_sysdate() from dual";
	
							pstmt = conn.prepareStatement(lsSql);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								ldToday = rs.getTimestamp(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
	
							//SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
							SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
							ldToday = java.sql.Timestamp.valueOf(sdf1.format(ldToday) + " 00:00:00.0");
							//PICKING TOTAL O/S AMT AND Cash disc & COMPARING
							//Nvl Added by Sharon on 22-Sep-2004
							//select sum(Nvl(tot_amt,0) - Nvl(dispute_amt,0)) - sum(Nvl(adj_amt,0)) into :mTotOsAmt// for nvl replacement  ** kiran  28/06/05
							lsSql = "select sum((case when tot_amt is null then 0 else tot_amt end) - (case when dispute_amt is null then 0 else dispute_amt end)) - sum(case when adj_amt is null then 0 else adj_amt end) as os_amt "
									+ " from  receivables " 
									+ " where cust_code = ? "
									+ " and ref_type <> ? "
									+ " and due_date < ? ";
	
							pstmt = conn.prepareStatement(lsSql);
							pstmt.setString(1,asCustCodeBil);
							pstmt.setString(2,lsOrdNewPrd);
							pstmt.setTimestamp(3,ldToday);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								mTotOsAmt = rs.getDouble("os_amt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
	
							/// if total cash discount is grater then zero and over due outstanding is exists.
							if (lcTaxAmt > 0 && mTotOsAmt > 0)
							{
								////Checking for overriden / failed / balance amt, Ruchira 28/08/2k6
								//******************					gbf_credit_check_update(asSorder,'P15',adNetAmt,asApplyTime,lcCheckAmt,'C',lsStatus);
								retArrayList =  credit_check_update(asSorder,"P15",adNetAmt,asApplyTime,lcCheckAmt,"C",lsStatus,conn);
	
								// added on 19/04/16 for status and amount
								//System.out.println("@@@@@@@@@  retArrayList.size()["+ retArrayList.size()+"]");
								if( retArrayList.size() > 0)
								{
									lsStatus = retArrayList.get(0);
								}
								if( retArrayList.size() > 1)
								{
									lcCheckAmt = Double.parseDouble(retArrayList.get(1)==null?"0":retArrayList.get(1));
								}	
								//System.out.println("@@@@@ lsCrPolicy["+lsCrPolicy+"]lcCheckAmt["+lcCheckAmt+"]lsStatus["+lsStatus+"]");
								
								//Changed By PriyankaC on 03June2019 [Start].
								//lsStr1 = "Cash discount credit check failed for customer " + lsCustName;
								lsStr1 = "Cash discount credit check failed for Sold to "+lsCustNameSoldTo +" Bill to "+lsCustNameBillTo ;
								//Changed By PriyankaC on 03June2019 [END]
								lsStr2 = " , Total over due O/S Amt: " + utilMethods.getReqDecString(mTotOsAmt,3);
								//To show the Invoice/SO amt in credit chek fail description, Added Ruchira 21/08/2k6
	
								if ("I".equals(asApplyTime))
								{
									lsStr3 = " , Invoice Amt.=> " + adNetAmt;
								}
								else
								{
									lsStr3 = " , Sales Order Amt.=> " + adNetAmt;
								}
								lsStr = lcCheckAmt + "\t" + lsStr1 + ", " + lsStr2 + ", " + lsStr3;
	
								if (lcCheckAmt > 0 )
								{
									lsStr = lsStr + ", Overriden amount exceeds.";
								}
								////Always when credit check fails it must get added into as_mail.
								if (!"O".equals(lsStatus))
								{
									mailList.add("P15"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr);
								}
								if (lcCheckAmt > 0 || "F".equals(lsStatus) || lsStatus.trim().length() == 0)
								{
									//Modified by Rohini T on 26/04/2021[Start]
									//failedPolicyList.add("P15"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);   //added by manish mhatre on 28-6-2019
									failedPolicyList.add("P15"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr+"\t"+"");
									//Modified by Rohini T on 26/04/2021[End]
								/*	failedPolicyList.add("P15"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);*/
									/*failedPolicyList.add("P15"+"\t"+asDespId+"\t"+asSorder+"\t"+lcCheckAmt+"\t"+lsStr+"\t"+lsStatus+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);*/ 
									writeLog(asSorder, ("P15"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr));
								}
	
							}
	
							////Added Ruchira 29/08/2k6, to update the used_amt in business_logic_check table even
							///if the cr check is failed or not failed.
							if ("I".equalsIgnoreCase(asApplyTime))
							{
								//******************					gbf_credit_check_update(asSorder,'P15',adNetAmt,asApplyTime,lcCheckAmt,'U',lsStatus);
								credit_check_update(asSorder,"P15",adNetAmt,asApplyTime,lcCheckAmt,"U",lsStatus,conn);
							}
							////End Added Ruchira 29/08/2k6
							//////Added Ruchira 20/02/2k6, re-assigned adNetAmt by ld_old_adNetAmt.
							////adNetAmt = ld_old_adNetAmt
						}
						else if ("P16".equals(lsCrPolicy.trim()) )
						{
	
							// amish 19-08-03
							// Credit check for --
							// if total advance received from customer and total sales order is executed.
							// (total advance received) > (total pending sales order for credit term is of advance type )
							// if the credit term of the current order is of advance type.
	
							/// select advance credit term from the disparm
							lsAdvCrTerm = disCommon.getDisparams("999999","ADVANCE_CR_TERM",conn);
							if ("NULLFOUND".equals(lsAdvCrTerm) || lsAdvCrTerm.trim().length() == 0 )
							{
								//ordTypeNewPrd = " "; // 07-oct-2019 Manoharan commented as wrong variable initialised
								lsAdvCrTerm = " ";
							}
							lsAdvCrTerm = lsAdvCrTerm.trim();
	
							/// count if current order is having the advance credit term.
							lsSql = "SELECT cr_term FROM sorder WHERE sorder.sale_order = ?";
	
							pstmt = conn.prepareStatement(lsSql);
							pstmt.setString(1,asSorder);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								lsCrTerm = rs.getString("cr_term");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
	
	
							/// if credit term is not of advance type then go to next.
							if (lsCrTerm.trim().indexOf(lsAdvCrTerm) > -1)
							{
								///pick up the total unadjusted advance. from the customer.
								//Nvl Added by Sharon on 22-Sep-2004
								//  SELECT sum(Nvl(tot_amt,0) - Nvl(dispute_amt,0) - Nvl(adj_amt,0))// for nvl replacement  ** kiran  28/06/05
								lsSql = "select sum((case when tot_amt is null then 0 else tot_amt end) - (case when dispute_amt is null then 0 else dispute_amt end)) - sum(case when adj_amt is null then 0 else adj_amt end) as tot_os_amt "
										+ " FROM receivables "
										+ " WHERE tran_ser = 'R-ADV'  and  cust_code = ? ";
	
								pstmt = conn.prepareStatement(lsSql);
								pstmt.setString(1,asCustCodeBil);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									mTotOsAmt = rs.getDouble("tot_os_amt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
	
								/// advance amount is stored in -ve
								/// so make it to +ve
								mTotOsAmt = Math.abs(mTotOsAmt);
	
								lsSql = " SELECT sum(sorder.tot_amt) as pend_ord_amt "
										+ " FROM sorder "
										+ " WHERE ( sorder.cust_code__bil = ? ) "
										+ " and ( sorder.status = 'P' ) "
										+ " and ( sorder.confirmed = 'Y' ) "
										+ " and ( sorder.cr_term = ? ) ";
	
								pstmt = conn.prepareStatement(lsSql);
								pstmt.setString(1,asCustCodeBil);
								pstmt.setString(2,lsCrTerm);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									lcPendOrdSord = rs.getDouble("pend_ord_amt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
	
								///if credit check is from sales order then add the sales order amount in the pending order.
								if ("S".equals(asApplyTime))
								{
									lcPendOrd = lcPendOrdSord + adNetAmt;
								}
								if ("I".equals(asApplyTime))
								{
									lcPendOrd = adNetAmt;
								}
	
								/// if advance amount is less then pending order amount then.
								if (mTotOsAmt < lcPendOrd)
								{
	
									////Checking for overriden / failed / balance amt, Ruchira 28/08/2k6
									//******************					gbf_credit_check_update(asSorder,'P16',adNetAmt,asApplyTime,lcCheckAmt,'C',lsStatus);
									retArrayList =  credit_check_update(asSorder,"P16",adNetAmt,asApplyTime,lcCheckAmt,"C",lsStatus,conn);
	
									// added on 19/04/16 for status and amount
									//System.out.println("@@@@@@@@@  retArrayList.size()["+ retArrayList.size()+"]");
									if( retArrayList.size() > 0)
									{
										lsStatus = retArrayList.get(0);
									}
									if( retArrayList.size() > 1)
									{
										lcCheckAmt = Double.parseDouble(retArrayList.get(1)==null?"0":retArrayList.get(1));
									}	
									//System.out.println("@@@@@ lsCrPolicy["+lsCrPolicy+"]lcCheckAmt["+lcCheckAmt+"]lsStatus["+lsStatus+"]");
									
									//Changed By PriyankaC on 03June2019 [Start].
									//lsStr1 = "Advance credit check failed for customer " + lsCustName;
									lsStr1 = "Advance credit check failed for Sold to "+lsCustNameSoldTo +" Bill to "+lsCustNameBillTo ;
									//Changed By PriyankaC on 03June2019 [END]
									lsStr2 = " , Total Advance Amt: " + utilMethods.getReqDecString(mTotOsAmt,3) + " exceeds order amount: " + utilMethods.getReqDecString(lcPendOrd,3);
									//To show the Invoice/SO amt in credit chek fail description, Added Ruchira 21/08/2k6
									if ("I".equals(asApplyTime))
									{
										lsStr3 = " , Invoice Amt.=> " + adNetAmt;
									}
									else
									{
										lsStr3 = " , Sales Order Amt.=> " + adNetAmt;
									}
									lsStr = lcCheckAmt + "\t" + lsStr1 + ", " + lsStr2 + ", " + lsStr3;
									if (lcCheckAmt > 0 )
									{
										lsStr = lsStr + ", Overriden amount exceeds.";
									}
									////Always when credit check fails it must get added into as_mail.
									if (!"O".equals(lsStatus))
									{
										mailList.add("P16"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr);
									}
									if (lcCheckAmt > 0 || "F".equals(lsStatus) || lsStatus.trim().length() == 0)
									{
										//Modified by Rohini T on 26/04/2021[Start]
										//failedPolicyList.add("P16"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+lsStatus+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);  //added by manish mhatre on 28-6-2019
										failedPolicyList.add("P16"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+lsStatus+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr+"\t"+"");
										//Modified by Rohini T on 26/04/2021[End]
										/*failedPolicyList.add("P16"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+lsStatus+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);*/
										/*failedPolicyList.add("P16"+"\t"+asDespId+"\t"+asSorder+"\t"+lcCheckAmt+"\t"+lsStr+"\t"+lsStatus+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);*/ 
										writeLog(asSorder, ("P16"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr));
									}
								}
	
								////Added Ruchira 29/08/2k6, to update the used_amt in business_logic_check table even
								///if the cr check is failed or not failed.
								if ("I".equals(asApplyTime))
								{
									//******************					gbf_credit_check_update(asSorder,'P16',adNetAmt,asApplyTime,lcCheckAmt,'U',lsStatus)
									credit_check_update(asSorder,"P16",adNetAmt,asApplyTime,lcCheckAmt,"U",lsStatus,conn);
								}
								////End Added Ruchira 29/08/2k6
	
								////Added Ruchira 20/02/2k6, re-assigned adNetAmt by ld_old_adNetAmt.
								////adNetAmt = ld_old_adNetAmt
							}
						}
						else if ("P17".equals(lsCrPolicy.trim()) )
						{
	
							//Amit 14/06/04: To restrict if Advance received from customer is less then advance
							//percentage specifeid in Sale Order.
							lsSql = "select sum(tot_amt - adj_amt - dispute_amt ) as adv_amt "
									+ " FROM receivables "
									+ " WHERE tran_ser = 'R-ADV'  and ref_no in (select tran_id from  receipt "
									+ " where sale_order = ?)"; //Mukesh chauhan Added missing end bracket on 05/02/2020 
	
							pstmt = conn.prepareStatement(lsSql);
							pstmt.setString(1,asSorder);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								lcAdvAmt = rs.getDouble("adv_amt");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
	
							lcAdvAmt = Math.abs(lcAdvAmt);
	
							lsSql = "select tot_amt, adv_perc "
									+ " from sorder "
									+ " where sale_order  = ? ";
	
							pstmt = conn.prepareStatement(lsSql);
							pstmt.setString(1,asSorder);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								lcNetAmt = rs.getDouble("tot_amt");
								lcAdvPer = rs.getDouble("adv_perc");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							lcAdvVal = lcNetAmt * (lcAdvPer/100);
							if (lcAdvAmt  < lcAdvVal)
							{
								////Checking for overriden / failed / balance amt, Ruchira 28/08/2k6
								//******************					gbf_credit_check_update(asSorder,'P17',adNetAmt,asApplyTime,lcCheckAmt,'C',lsStatus);
								retArrayList =  credit_check_update(asSorder,"P17",adNetAmt,asApplyTime,lcCheckAmt,"C",lsStatus,conn);
	
								// added on 19/04/16 for status and amount
								//System.out.println("@@@@@@@@@  retArrayList.size()["+ retArrayList.size()+"]");
								if( retArrayList.size() > 0)
								{
									lsStatus = retArrayList.get(0);
								}
								if( retArrayList.size() > 1)
								{
									lcCheckAmt = Double.parseDouble(retArrayList.get(1)==null?"0":retArrayList.get(1));
								}	
								//System.out.println("@@@@@ lsCrPolicy["+lsCrPolicy+"]lcCheckAmt["+lcCheckAmt+"]lsStatus["+lsStatus+"]");
								
								
								lsStr1 = "";
								lsStr2 = " Total Advance Amt: " + utilMethods.getReqDecString(lcAdvVal,3) + " less than advance percentage specified in sale order";
								//To show the Invoice/SO amt in credit chek fail description, Added Ruchira 21/08/2k6
								if ("I".equals(asApplyTime))
								{
									lsStr3 = " , Invoice Amt.=> " + adNetAmt;
								}
								else
								{
									lsStr3 = " , Sales Order Amt.=> " + adNetAmt;
								}
								lsStr = lcCheckAmt + "\t" + lsStr1 + ", " + lsStr2 + ", " + lsStr3;
	
								if (lcCheckAmt > 0 )
								{
									lsStr = lsStr + ", Overriden amount exceeds.";
								}
								////Always when credit check fails it must get added into as_mail.
								if (!"O".equals(lsStatus))
								{
									mailList.add("P17"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr);
								}
								if (lcCheckAmt > 0 || "F".equals(lsStatus) || lsStatus.trim().length() == 0)
								{
									//Modified by Rohini T on 26/04/2021[Start]
									//failedPolicyList.add("P17"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+lsStatus+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);    //added by manish mhatre on 28-6-2019
									failedPolicyList.add("P17"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+lsStatus+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr+"\t"+"");
									//Modified by Rohini T on 26/04/2021[End]
									/*failedPolicyList.add("P17"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+" "+"\t"+lsStatus+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);*/
									/*failedPolicyList.add("P17"+"\t"+asDespId+"\t"+asSorder+"\t"+lcCheckAmt+"\t"+lsStr+"\t"+lsStatus+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);*/ 
									writeLog(asSorder, ("P17"+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr));
								}
	
							}
	
							////Added Ruchira 29/08/2k6, to update the used_amt in business_logic_check table even
							///if the cr check is failed or not failed.
							if ("I".equals(asApplyTime))
							{
								//******************					gbf_credit_check_update(asSorder,'P17',adNetAmt,asApplyTime,lcCheckAmt,'U',lsStatus)
								credit_check_update(asSorder,"P17",adNetAmt,asApplyTime,lcCheckAmt,"U",lsStatus,conn);
							}
							////End Added Ruchira 29/08/2k6
							//End Amit 14/06/04
							//////Added Ruchira 20/02/2k6, re-assigned adNetAmt by ld_old_adNetAmt.
							////adNetAmt = ld_old_adNetAmt
						}
						//	******************	else if (lsCrPolicy.trim().compareTo("P9") > 0) // pending - to be corrected
						
						else if (lsCrPolicy.trim().compareTo("P9") > 0) // pending - to be corrected
						{
							System.out.println("Credit Policy within P9["+lsCrPolicy+"]");
							////Added Ruchira 18/03/2k6, added new dynamic credit check, which uses four new columns
							////added in itemser_cr_policy table to generate the result of an sql given in policy_sql
							////using the input parameters in policy_input column dynamicaly and compare it useing the
							////conditional operator in policy_condition with the given result in column policy_result.
	
							////Added For loop for multiple dynamic credit policies Ruchira 26/06/2k6.
							////ld_old_adNetAmt = adNetAmt
							////End Added For loop for multiple dynamic credit policies Ruchira 26/06/2k6.
							lsSql = "select policy_sql, policy_input, policy_result, policy_condition "
									+ " from   itemser_cr_policy "
									+ " where item_ser  = ? and cr_policy = ?";
	
							pstmt = conn.prepareStatement(lsSql);
							pstmt.setString(1,asItemSer);
							pstmt.setString(2,lsCrPolicy);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								lsPolicySql = rs.getString("policy_sql");
								lsPolicyInput = rs.getString("policy_input");
								lsPolicyResult = rs.getString("policy_result");
								lsPolicyCondition = rs.getString("policy_condition");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
	
							if (lsPolicyResult == null )
							{
								lsPolicyResult = "";
							}
							if (lsPolicyCondition ==  null)
							{
								lsPolicyCondition = "";
							}
							lsFail = "F";
	
							System.out.println("@@@@ lsPolicySql====["+lsPolicySql+"]\nlsPolicyInput["+lsPolicyInput+"]\nlsPolicyResult["+lsPolicyResult+"]\nlsPolicyCondition["+lsPolicyCondition+"]");
							////If policy_sql is null then credit check will not be done.
							if (lsPolicySql != null && lsPolicySql.trim().length() > 0 )
							{
	
								////Generating the sql using the input parameters in policy_input column
								if (lsPolicyInput != null && lsPolicyInput.trim().length() > 0)
								{
	
									arrStr = lsPolicyInput.split(",");
									arrSql = lsPolicySql.split ("\\?");
									lsSql = "";
									int arrCtr = 0;
									for (arrCtr = 0; arrCtr < arrStr.length; arrCtr++)
									{
										
	
										if ("cust_code__bil".equals(arrStr[arrCtr].trim()))
										{
											lsValue = "'" + asCustCodeBil.trim() + "'";
										}
										else if ("item_ser".equals(arrStr[arrCtr].trim()))
										{
											lsValue = "'" + asItemSer.trim() +"'";
										}
										else if ("sale_order".equals(arrStr[arrCtr].trim()))
										{
											lsValue = "'" + asSorder.trim() + "'";
										}
										else if ("site_code".equals(arrStr[arrCtr].trim()))
										{
											lsValue = "'" + asSiteCode.trim() + "'";
										}
										else if ("desp_id".equals(arrStr[arrCtr].trim()))
										{
											lsValue = "'" + asDespId.trim() + "'";
										}
										else if ("net_amt".equals(arrStr[arrCtr].trim()))
										{
											lsValue = "" + adNetAmt;
										}
										lsSql = lsSql + arrSql[arrCtr] + lsValue;
										System.out.println("@@@@1 lsSql====["+lsSql+"]arrCtr===["+arrCtr+"]");
									}
									if (arrStr.length < arrSql.length)
									{
										lsSql = lsSql + arrSql[arrCtr];
									}
	
								}
								else
								{
									lsSql = lsPolicySql;
								}
								System.out.println("@@@@2 lsSql====["+lsSql+"]");
								////Executing the dynamit sql to get the result.
								try 
								{ 
								pstmt = conn.prepareStatement(lsSql);
								rs = pstmt.executeQuery();
								ResultSetMetaData rsmd=rs.getMetaData();  
							//	int resultType = Integer.parseInt(rsmd.getColumnTypeName(1));
								int resultType = rsmd.getColumnType(1);
								System.out.println("@@@@@ resultType["+resultType+"]");
								lsResult = "";
								double lcResult = 0;
								int liResult = 9;
								Timestamp ldtResult = null;
								/*
									-7 	BIT
									-6 	TINYINT
									-5 	BIGINT
									-4 	LONGVARBINARY 
									-3 	VARBINARY
									-2 	BINARY
									-1 	LONGVARCHAR
									0 	NULL
									1 	CHAR
									2 	NUMERIC
									3 	DECIMAL
									4 	INTEGER
									5 	SMALLINT
									6 	FLOAT
									7 	REAL
									8 	DOUBLE
									12 	VARCHAR
									91 	DATE
									92 	TIME
									93 	TIMESTAMP
									1111  	OTHER
								 */
								if (rs.next())
								{
									if( resultType== 4 || resultType == 5)
									{									
										//liResult = rs.getInt(1);
										lsResult = rs.getString(1);
										
									} 
									else if( resultType >= 2 && resultType <= 8)
									{									
										//lcResult = rs.getDouble(1);
										lsResult = rs.getString(1);
										System.out.println("#### lsResult ["+lsResult+"]");
									}
									else if( resultType == 1 || resultType == 12)
									{									
										lsResult = ""+rs.getDouble(1);
									}
									else if( resultType == 91 || resultType == 93)
									{									
										ldtResultResult = rs.getTimestamp(1);
									}
	
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
	
								System.out.println("@@@@@@@lsPolicyCondition["+lsPolicyCondition+"]lsPolicyResult["+lsPolicyResult+"]@@@@@@lsResult["+lsResult+"]@@@@@@@ldtResultResult["+ldtResultResult+"]");
								////comparing the sql result with the given result in policy_result using the conditional
								////operater given in the policy_condition column.
								if (lsPolicyCondition != null && lsPolicyCondition.trim().length() > 0 && lsPolicyResult != null && lsPolicyResult.trim().length() > 0)
								{
									lsPolicyCondition = lsPolicyCondition.trim();
	
									if ("=".equals(lsPolicyCondition))
									{
										if( IsNumber(lsPolicyResult) )
										{
											if ( Double.parseDouble(lsPolicyResult) != Double.parseDouble(lsResult) )
											{
												lsFail = "T";
											}
										}
										else if ( IsDate(lsPolicyResult)) 
										{
	
											if(! date(lsPolicyResult).equals(date(lsResult) ))
											{
												lsFail = "T";
											}
										}
										else
										{	if(! lsPolicyResult.equalsIgnoreCase(lsResult) )
										{
											lsFail = "T";
										}	
										}
	
									}
									else if("<>".equals(lsPolicyCondition))
									{
										if( IsNumber(lsPolicyResult))
										{
											if ( Double.parseDouble(lsPolicyResult) == Double.parseDouble(lsResult) )
											{
												lsFail = "T";
	
											}
										}
										else if( IsDate(lsPolicyResult) )
										{ 
											if( date(lsPolicyResult).equals(date(lsResult) ))
											{
												lsFail = "T";
											}
										}
										else
										{	
											if( lsPolicyResult.equalsIgnoreCase(lsResult) )
											{
												lsFail = "T";
											}
										}
									}
									else if(">".equals(lsPolicyCondition))
									{
	
										if( IsNumber(lsPolicyResult)) 
										{	
											if( Double.parseDouble(lsPolicyResult) <= Double.parseDouble(lsResult) )
											{
												lsFail = "T";
											}
										}
										else if( IsDate(lsPolicyResult))
										{   		
											if( date(lsPolicyResult).before(date(lsResult) ))
											{
												lsFail = "T";
											}
										}
										else
										{ 
											if( lsPolicyResult.compareToIgnoreCase(lsResult) < 0  )
											{
												lsFail = "T";
											}
										}
									}
									else if("<".equals(lsPolicyCondition))
									{
										if( IsNumber(lsPolicyResult) )
										{
											if( Double.parseDouble(lsPolicyResult) >= Double.parseDouble(lsResult) )
											{
												lsFail = "T";
											}
										}
										else if( IsDate(lsPolicyResult) )
										{
											if (date(lsPolicyResult).after(date(lsResult) ))
											{
												lsFail = "T";
											}
										}
										else
										{
											if (lsPolicyResult.compareToIgnoreCase(lsResult) > 0 )
											{
												lsFail = "T";
											}
										}
									}
									else if(">=".equals(lsPolicyCondition))
									{
										if( IsNumber(lsPolicyResult) )
										{
											if( Double.parseDouble(lsPolicyResult) < Double.parseDouble(lsResult) )
											{
												lsFail = "T";
											}
										}
										else if( IsDate(lsPolicyResult))
										{
											if( date(lsPolicyResult).before(date(lsResult) ))
											{
												lsFail = "T";
											}
										}
										else
										{
											if( lsPolicyResult.compareToIgnoreCase(lsResult) < 0 )
											{
												lsFail = "T";
											}
										}
									}
									else if("<=".equals(lsPolicyCondition))
									{
										if( IsNumber(lsPolicyResult) )
										{
											if( Double.parseDouble(lsPolicyResult) > Double.parseDouble(lsResult) )
											{
												lsFail = "T";
											}
										}	
										else if( IsDate(lsPolicyResult) )
										{
											if( date(lsPolicyResult).after(date(lsResult) ))
											{
												lsFail = "T";
											}
										}
										else
										{
											if( lsPolicyResult.compareToIgnoreCase(lsResult) > 0 )
											{
												lsFail = "T";
											}
										}
									}
								}
								
								//Pavan Rane start 02/11/17 START
								if("T".equalsIgnoreCase(lsFail))
								{		//asCheck[ctr] = 'R99'+"\t"+asDespId+"\t"+asSorder+"\t"+'Dynamic credit check failed !!!'	//Commented Ruchira 08/06/2k6
									//lsStr1 = 'Dynamic credit check failed !!!'	//Commented Ruchira 27/06/2k6
									//Added Ruchira 27/06/2k6
									sql = " select title  from   itemser_cr_policy " +
											"	where  item_ser  =  ? " +
											" and    cr_policy = ?  ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1,asItemSer);
									pstmt.setString(2,lsCrPolicy);
									rs = pstmt.executeQuery();
									if (rs.next())
									{
										lsStr1 = rs.getString("title");
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									//lsStr1 = trim(lsStr1)
									if( lsStr1 == null ||  lsStr1.trim().length() == 0 )
									{
										lsStr1 = lsCrPolicy+" Dynamic credit check failed !!!";
									}
									System.out.println("@@@@ 3219 lsStr1["+lsStr1+"]");
									
	
									//To show the Invoice/SO amt in credit chek fail description, Added Ruchira 21/08/2k6
									if ("I".equals(asApplyTime))
									{
										//lsStr3 = " , Invoice Amt.=> "+String(adNetAmt);
										lsStr3 = " Invoice Amt.=> "+String(adNetAmt);
									}
									else
									{
										//lsStr3 = " , Sales Order Amt.=> "+String(adNetAmt);
										lsStr3 = " Sales Order Amt.=> "+String(adNetAmt);
									}
									//End To show the Invoice/SO amt in credit chek fail description, Added Ruchira 21/08/2k6
									
									System.out.println("@@@@@@3229 adNetAmt["+adNetAmt+"]lsStr1["+lsStr1+"]lsStr3["+lsStr3+"]");
									
									//lsStr = String(adNetAmt)+"\t"+lsStr1+", "+lsStr3;
									//lsStr = String(adNetAmt)+" "+lsStr1+","+lsStr3;
									lsStr = lcCheckAmt+"\t"+String(adNetAmt)+" "+lsStr1+","+lsStr3;
									System.out.println("@@@@@@3233 lsStr["+lsStr+"]");
									
									
									retArrayList =  credit_check_update(asSorder,lsCrPolicy,adNetAmt,asApplyTime,lcCheckAmt,"C",lsStatus,conn);
		
									// added on 19/04/16 for status and amount
									System.out.println("@@@@@@@@@  retArrayList.size()["+ retArrayList.size()+"]");
									System.out.println("@@@@@@@@@  retArrayList["+ retArrayList+"]");
									
									if( retArrayList.size() > 0)
									{
										lsStatus = retArrayList.get(0);
									}
									if( retArrayList.size() > 1)
									{
										lcCheckAmt = Double.parseDouble(retArrayList.get(1)==null?"0":retArrayList.get(1));
									}	
									System.out.println("@@@@@ lsCrPolicy["+lsCrPolicy+"]lcCheckAmt["+lcCheckAmt+"]lsStatus["+lsStatus+"]lsStr["+lsStr+"]");
									
									System.out.println("lsStatus::["+lsStatus+"]");
									////Always when credit check fails it must get added into as_mail.
									if (!"O".equals(lsStatus))
									{
											//lsMail[lsMail.length + 1] = lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr;
											mailList.add(lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr);
									}
									if (lcCheckAmt > 0 || "F".equals(lsStatus) || lsStatus.trim().length() == 0)
									{
										//asCheck[ctr] = lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr;
										//ctr = ctr + 1;
										//asCheckList.add(lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr);
										//Pavan R on 14sept18 [to handle NumberFormatException in writeBusinessLogicCheck()]
										//failedPolicyList.add(lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+lcCheckAmt+"\t"+lsStatus);
										//Modified by Rohini T on 26/04/2021[Start]
										//failedPolicyList.add(lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+lsStatus+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);  //added by manish mhatre on 28-6-2019
										failedPolicyList.add(lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+lsStatus+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr+"\t"+"");
										//Modified by Rohini T on 26/04/2021[End]
										/*failedPolicyList.add(lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+lsStatus+"\t"+" "+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);*/
										/*failedPolicyList.add(lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lcCheckAmt+"\t"+lsStr+"\t"+lsStatus+"\t"+asCustCodeBil+"\t"+asCustCodeSoldTo+"\t"+asSiteCode+"\t"+asItemSer+"\t"+refDateStr);*/ 
										System.out.println("@@@@@@@@@@@failedPolicyList Size["+failedPolicyList.size()+"]");
										System.out.println("@@@@@@@@@@@ failedPolicyList ["+failedPolicyList+"]");
									}
									System.out.println("@@@@ 3189 lsFail["+lsFail+"]::lsCrPolicy");
									
								}
								if ("I".equals(asApplyTime))
								{
									//******************							credit_check_update(asSorder,lsCrPolicy,adNetAmt,asApplyTime,lcCheckAmt,'U',lsStatus)
									
									long startTime2 = System.currentTimeMillis();
									credit_check_update(asSorder,lsCrPolicy,adNetAmt,asApplyTime,lcCheckAmt,"U",lsStatus,conn);
									//Changed By Nasruddin Start 04-11-16
									long endTime2 = System.currentTimeMillis();
									System.out.println("DIFFERANCE IN TIME credit_check_update DATA IN SECONDS INSIDE CREDIT CHECK METHOD:::["+(endTime2-startTime2)/1000+"]");
									//Changed By Nasruddin END 04-11-16
								}
								// Pavan Rane 27aug19 start[to display error message to front end]
							}catch(SQLException se)
							{
								System.out.println("SQLException::CreditCheck::>>>"+se.getMessage());
								se.printStackTrace();								
								String errStr = "User Defined Policy: "+lsCrPolicy +" Database Exception for Policy SQL: "+lsPolicySql+" Input: "+lsPolicyInput+" Result: "+lsPolicyResult+ " Actual Result: " +lsResult+ " Condition: "+lsPolicyCondition;
								String errMsg = fincommon.getErrorXML(errStr,se.getMessage(), "DS000", lsStr);
								failedPolicyList.add("Error");
								failedPolicyList.add(errMsg);														
								return failedPolicyList;
							}catch(Exception e)
							{
								System.out.println("Exception::CreditCheck::>>>"+e.getMessage());
								e.printStackTrace();							
								String errStr = "User Defined Policy: "+lsCrPolicy +" Database Exception for Policy SQL: "+lsPolicySql+" Input: "+lsPolicyInput+" Result: "+lsPolicyResult+ " Actual Result: " +lsResult+ " Condition: "+lsPolicyCondition;
								String errMsg = fincommon.getErrorXML(errStr,e.getMessage(), "DS000", lsStr);
								failedPolicyList.add("Error");
								failedPolicyList.add(errMsg);														
								return failedPolicyList;								
							}
							//Pavan Rane 27aug19 end[to display error message to front end]
							}
								//Pavan Rane start 02/11/17 end
						}
	
					}
				}
					System.out.println("@@@@ 3192 lsFail["+lsFail+"]::lsCrPolicy");
					//Added Ruchira 08/06/2k6
					//Credit check is failed so adding it into the asCheck array.
	//02/11/17 Pavan R start
		/*		if("T".equalsIgnoreCase(lsFail))
					{		//asCheck[ctr] = 'R99'+"\t"+asDespId+"\t"+asSorder+"\t"+'Dynamic credit check failed !!!'	//Commented Ruchira 08/06/2k6
						//lsStr1 = 'Dynamic credit check failed !!!'	//Commented Ruchira 27/06/2k6
						//Added Ruchira 27/06/2k6
						sql = " select title  from   itemser_cr_policy " +
								"	where  item_ser  =  ? " +
								" and    cr_policy = ?  ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,asItemSer);
						pstmt.setString(2,lsCrPolicy);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							lsStr1 = rs.getString("title");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						//lsStr1 = trim(lsStr1)
						if( lsStr1 == null ||  lsStr1.trim().length() == 0 )
						{
							lsStr1 = lsCrPolicy+" Dynamic credit check failed !!!";
						}
						System.out.println("@@@@ 3219 lsStr1["+lsStr1+"]");
					//}
	
					//To show the Invoice/SO amt in credit chek fail description, Added Ruchira 21/08/2k6
					if ("I".equals(asApplyTime))
					{
						//lsStr3 = " , Invoice Amt.=> "+String(adNetAmt);
						lsStr3 = " Invoice Amt.=> "+String(adNetAmt);
					}
					else
					{
						//lsStr3 = " , Sales Order Amt.=> "+String(adNetAmt);
						lsStr3 = " Sales Order Amt.=> "+String(adNetAmt);
					}
					//End To show the Invoice/SO amt in credit chek fail description, Added Ruchira 21/08/2k6
					
					System.out.println("@@@@@@3229 adNetAmt["+adNetAmt+"]lsStr1["+lsStr1+"]lsStr3["+lsStr3+"]");
					
					//lsStr = String(adNetAmt)+"\t"+lsStr1+", "+lsStr3;
					lsStr = String(adNetAmt)+" "+lsStr1+","+lsStr3;
					System.out.println("@@@@@@3233 lsStr["+lsStr+"]");
	*/
		//02/11/17 Pavan R end
					//			asCheck[ctr] = lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr
					//			ctr = ctr + 1
	
					////Checking for overriden / failed / balance amt, Ruchira 28/08/2k6
					//******************							gbf_credit_check_update(asSorder,lsCrPolicy,adNetAmt,asApplyTime,lcCheckAmt,'C',lsStatus)
					
	// Pavan Rane Start			
	/*				retArrayList =  credit_check_update(asSorder,lsCrPolicy,adNetAmt,asApplyTime,lcCheckAmt,"C",lsStatus,conn);
	
					// added on 19/04/16 for status and amount
					System.out.println("@@@@@@@@@  retArrayList.size()["+ retArrayList.size()+"]");
					
					if( retArrayList.size() > 0)
					{
						lsStatus = retArrayList.get(0);
					}
					if( retArrayList.size() > 1)
					{
						lcCheckAmt = Double.parseDouble(retArrayList.get(1)==null?"0":retArrayList.get(1));
					}	
					System.out.println("@@@@@ lsCrPolicy["+lsCrPolicy+"]lcCheckAmt["+lcCheckAmt+"]lsStatus["+lsStatus+"]lsStr["+lsStr+"]");
					
					
					////Always when credit check fails it must get added into as_mail.
					if (!"O".equals(lsStatus))
					{
							//lsMail[lsMail.length + 1] = lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr;
							mailList.add(lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr);
					}
					System.out.println("####lsCrPolicy ["+lsCrPolicy+"]");
					System.out.println("####lcCheckAmt ["+lcCheckAmt+"]");
					System.out.println("####lsstatus ["+lsStatus+"]");*/
					/*if (lcCheckAmt > 0 || "F".equals(lsStatus) || lsStatus.trim().length() == 0)
					{
						//asCheck[ctr] = lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr;
						//ctr = ctr + 1;
						//asCheckList.add(lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr);
						
						failedPolicyList.add(lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr);
						System.out.println("@@@@@@@@@@@failedPolicyList Size["+failedPolicyList.size()+"]");
						System.out.println("@@@@@@@@@@@ failedPolicyList ["+failedPolicyList+"]");
					}*/ 
					//Pavan Rane End
				
	
				////Added Ruchira 29/08/2k6, to update the used_amt in business_logic_check table even
				///if the cr check is failed or not failed.
				
		//02/11/17 Pavan R start
				
			/*	if ("I".equals(asApplyTime))
				{
					//******************							credit_check_update(asSorder,lsCrPolicy,adNetAmt,asApplyTime,lcCheckAmt,'U',lsStatus)
					
					long startTime2 = System.currentTimeMillis();
					credit_check_update(asSorder,lsCrPolicy,adNetAmt,asApplyTime,lcCheckAmt,"U",lsStatus,conn);
					//Changed By Nasruddin Start 04-11-16
					long endTime2 = System.currentTimeMillis();
					System.out.println("DIFFERANCE IN TIME credit_check_update DATA IN SECONDS INSIDE CREDIT CHECK METHOD:::["+(endTime2-startTime2)/1000+"]");
					//Changed By Nasruddin END 04-11-16
				}*/
		
				//02/11/17 Pavan R end
				
				
			//}
				////End Added Ruchira 29/08/2k6
	
				//End Added Ruchira 08/06/2k6
				//}
				//////Added Ruchira 08/06/2k6, re-assigned adNetAmt by ld_old_adNetAmt.
				////adNetAmt = ld_old_adNetAmt
				////End Added Ruchira 18/03/2k6, added new dynamic credit check.
				//}
				//} // 90 - 99
	
	
				//added by bhagyashree on 03-05-04
				//structpass:
	
				////Commented Ruchira 31/01/2k6
				//if len(trim(errString)) > 0 then // amish 22-07-04 if error not found then do not pass any thing
				//	errString = errString + '\t' + lsReasCode + '~r' + lsReasDetail + '~n' + asSorder
				//end if
				////End Commented Ruchira 31/01/2k6
	
				//s_log.as_reas_code 			= lsReasCode
				//s_log.as_reas_detail 			= lsReasDetail
				//s_log.as_table_name 			= 'sorditem'
				//s_log.keyfld1 					= asSorder
				//s_log.keyfld2					= lsNull
				//s_log.keyfld3 					= lsNull
				//s_log.keyfld4 					= lsNull
				//s_log.keyfld5 					= lsNull
				//s_log.as_tran_id 				= ''
				//s_log.as_tran_code				= ''
				//s_log.gencode_fldname			= ''
				//s_log.mod_name       			= ''
				//end by bhagyashree on 03-05-04
				//Added by shahid 30/03/2009 for DI89WAL003
				sql = "select contact_code ,case when credit_lmt is null then 0 else credit_lmt end " +
						//  " into :lsContactCode , :lcCustCrlmt " +
						" from customer " +
						" where cust_code = ? ";
				
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,asCustCodeBil);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					lsContactCode = rs.getString("contact_code");
					lcCustCrlmt = rs.getDouble(2);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
	
	
	
				sql = " select case when credit_lmt is null then 0 else credit_lmt end " +
						// " into :lcContCrlmt	" +
						" from contact where contact_code  = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,lsContactCode);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					lcContCrlmt = rs.getDouble(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
	
	
				//for( llCnt = 1 ; llCnt < asCheck.length; llCnt++ )
				//{//	asCheck[llCnt] = lsCrPolicy+"\t"+asDespId+"\t"+asSorder+"\t"+lsStr+"\t"+String(lcCustCrlmt)+"\t"+String(lcContCrlmt)
				//	asCheck[llCnt] = asCheck[llCnt]+"\t"+String(lcCustCrlmt)+"\t"+String(lcContCrlmt);
				//}
				//Ended by shahid 30/03/2009 for DI89WAL003
	
				// 01-08-2006 manoharan to send mail/sms
				//errString = gbf_sendmail (asCustCodeBil, asItemSer, adNetAmt, asSorder, adtTranDate, asSiteCode, asApplyTime, asDespId, lsMail[])
	
			// pending 
			//	errString = sendmail (asCustCodeBil, asItemSer, adNetAmt, asSorder, adtTranDate, asSiteCode, asApplyTime, asDespId, lsMail,conn);
	
				// end 01-08-2006 manoharan to send mail/sms
				//return errString
				//}
				//	}
				//	}
				//}
				endTime = System.currentTimeMillis();
				totalTime = endTime - startTime;
	
				totSecs = (int) (((double) 1 / 1000) * (totalTime));
				totalHrs = (int) (totSecs / 3600);
				totlMts = (int) (((totSecs - (totalHrs * 3600)) / 60));
				totSecs = (int) (totSecs - ((totalHrs * 3600) + (totlMts * 60)));
	
				System.out.println("Total Time Spend credit check [" + totalHrs + "] Hours [" + totlMts + "] Minutes [" + totSecs + "] seconds");
				System.out.println("3820  failedPolicyList size [" +failedPolicyList.size() + "] failedPolicyList [" +failedPolicyList.toString() + "]");
			}	
		}
		catch( Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
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
				if(rs1 != null)
				{
					rs1.close();
					rs1 = null;
				}
				if(pstmt1 != null)
				{
					pstmt1.close();
					pstmt1 = null;
				}
				if(pstmtInsert != null)
				{
					pstmtInsert.close();
					pstmtInsert = null;
				}
				//conn.close();
			}
			catch(Exception e)
			{
				System.out.println("Exception : "+e);e.printStackTrace();
				throw new ITMException(e);
			}

		}
//		return errString;
		return failedPolicyList;

	}


	private String sendmail(java.lang.String asCustCodeBil,
			java.lang.String asItemSer, double adNetAmt,
			java.lang.String asSorder, Timestamp adtTranDate,
			java.lang.String asSiteCode, java.lang.String asApplyTime,
			java.lang.String asDespId, String as_check[], Connection conn) throws ibase.webitm.utility.ITMException, Exception
	{


		String ls_errcode = "", ls_data = "", ls_crpolicy = "", ls_mailoption = "", ls_remarks = "", ls_temp = "", ls_despid = "", ls_saleorder = ""; 
		String ls_pass = "", ls_overriden = "", as_status = "";
		int  ll_ctr=0, ll_fcount=0, ll_count=0, ll_ocount=0 ;
		Timestamp ldt_today= null;
		double lc_aprvamt=0, lc_failedamt=0, lc_totamt=0;

		String userId="",termId="",sysDate="",emailAddr="",custName="",userType="",descr="",errString="";
		PreparedStatement pstmt = null;
		String sql = "", empCodeAprv = "",formatCode="";
		ResultSet rs = null;


		sql = "select cust_name from customer where cust_code= ? ";
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, asCustCodeBil);
		rs = pstmt.executeQuery();
		if(rs.next())
		{
			custName = checknull(rs.getString("cust_name"));
		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;

		sql = "select email_addr from site where site_code= ? ";
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, asSiteCode);
		rs = pstmt.executeQuery();
		if(rs.next())
		{
			emailAddr = checknull(rs.getString("email_addr"));
			//System.out.println("email_addr>>> "+emailAddr);
		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;

		/*sql="SELECT descr FROM gencodes WHERE MOD_NAME = 'X' AND FLD_NAME = 'EMAIL_ID_AML' " +
				"and sh_descr in(select registr_5 from customer c,receipt r " +
				"where c.cust_code=r.cust_code and r.tran_id= ?) ";
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, tranId);
		rs = pstmt.executeQuery();
		if(rs.next())
		{
			descr = checkNull(rs.getString("descr"));
			System.out.println("  descr  ::"+descr);
		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;*/


		//nvo_system_object_mail_handler lnvo_mailer
		//nvo_datastore lds_data
		//nvo_business_object_dist_sales_order lnvo_sorder
		//lnvo_sorder =  create nvo_business_object_dist_sales_order

		ls_errcode = "";

		E12GenericUtility genericUtility = new E12GenericUtility();
		java.util.Date currentDate = new java.util.Date();
		SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getDBDateFormat());
		ldt_today = java.sql.Timestamp.valueOf( sdf.format(currentDate)+" 00:00:00.0");
		//System.out.println("Now the date is ldt_today:=>  ["+ldt_today+"]");		

		//if upperbound(as_check[]) = 0 then
		//   return ls_errcode
		//end if

		//lnvo_mailer  = create nvo_system_object_mail_handler
		//lds_data = create nvo_datastore
		//lds_data.dataobject = "d_credit_check_mail"
		//lds_data.insertrow(0)
		/*
		lds_data.setitem(1,"site_code",as_site_code)
		lds_data.setitem(1,"item_ser",as_item_ser)
		lds_data.setitem(1,"cust_code",as_cust_code_bil)
		lds_data.setitem(1,"sale_order",as_sorder)
		lds_data.setitem(1,"amount",ad_net_amt)
		lds_data.setitem(1,"despatch_id",as_despid)
		lds_data.setitem(1,"chg_user",userid)
		lds_data.setitem(1,"chg_term",termid)
		lds_data.setitem(1,"chg_date",ldt_today)
		 */
		//	ls_data = lds_data.describe("datawindow.syntax") + '~r' + lds_data.describe("datawindow.syntax.data")

		for( ll_ctr = 0;ll_ctr < as_check.length; ll_ctr++)
		{  

			ls_remarks = as_check[ll_ctr];
			//ls_crpolicy = f_get_token(ls_remarks,'~t')

			//System.out.println("@@@@@@@@@@@ ls_remarks["+ls_remarks+"]");
			String ls_remarksArray[]=ls_remarks.split("\t");
			//System.out.println("@@@@@ ls_remarksArray.length["+ls_remarksArray.length+"]");
			for(int i=0;i<ls_remarksArray.length;i++)
			{

				ls_crpolicy =ls_remarksArray[0];
				
				sql =  " select mail_option  " +
						//	" into :ls_mailoption " +
						" from itemser_cr_policy where item_ser = ?  and cr_policy = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, asItemSer);
				pstmt.setString(2, ls_crpolicy);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					ls_mailoption = checknull(rs.getString("mail_option")== null?"0":rs.getString("mail_option"));
					//System.out.println("ls_mailoption>>> "+ls_mailoption);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				if(ls_mailoption == null )
				{
					ls_mailoption = "0";
				}

				if( Integer.parseInt(ls_mailoption) > 0 )
				{		    
					ls_crpolicy =ls_remarksArray[0];
					String despid =ls_remarksArray[1];   // despid
					String sorder =ls_remarksArray[2];   // sorder
					String net_amt =ls_remarksArray[3];   // net_amt
					String sl_str1 =ls_remarksArray[4];   // sl_str1


					//lds_data.setitem(1,"cr_policy",ls_crpolicy)

					//lds_data.setitem(1,"remarks",ls_temp)

					//ls_temp = f_get_token(ls_remarks,'~t') // sl_str1

					//	 ls_data = lds_data.describe("datawindow.syntax") + '~r' + lds_data.describe("datawindow.syntax.data")
					//   ls_errcode = lnvo_mailer.gsf_send_mail_transaction( 'w_credit_check', ls_data, ls_crpolicy)

						        StringBuffer commInfo = new StringBuffer();
					commInfo.append("<ROOT>");
					commInfo.append("<MAIL><EMAIL_TYPE>page</EMAIL_TYPE><ENTITY_CODE>BASE</ENTITY_CODE>");
					commInfo.append("<ENTITY_TYPE>"+userType+"</ENTITY_TYPE>");
					commInfo.append("<TO_ADD>"+descr+","+emailAddr+"</TO_ADD>");
					commInfo.append("<BCC_ADD></BCC_ADD>");
					commInfo.append("<FORMAT_CODE>"+formatCode+"</FORMAT_CODE>");							
					commInfo.append("<ATTACHMENT><BODY></BODY><LOCATION></LOCATION></ATTACHMENT>");
					commInfo.append("</MAIL>");
					commInfo.append("<XML_DATA><ROOT><Detail1>" +
							//	"<TRAN_ID>"+tranId+"</TRAN_ID><TRAN_DATE>"+tranDate+"</TRAN_DATE>" +
							"<site_code>"+asSiteCode+"</site_code><item_ser>"+asItemSer+"</item_ser>" +
							"<cust_code>"+asCustCodeBil+"</cust_code><sale_order>"+asSorder+"</sale_order>" +
							"<amount>"+adNetAmt+"</amount><despatch_id>"+asDespId+"</despatch_id>" +
							"<chg_user>"+userId+"</chg_user><chg_term>"+termId+"</chg_term><chg_date>"+sysDate+"</chg_date>" +
							"<emp_code>"+empCodeAprv+"</emp_code>");
					//		commInfo.append("<cc_to>"+travelCodeEmail+","+nsmCodeEmail+"</cc_to>");
					commInfo.append("</Detail1></ROOT></XML_DATA>");
					commInfo.append("</ROOT>");	
					EMail email = new EMail();
					email.sendMail(commInfo.toString(), "ITM"); 
						        
				}

			}
			/*
		
			 */
		}
			return errString;
		}


		private Timestamp date(String date) throws ibase.webitm.utility.ITMException, Exception 
		{

			E12GenericUtility e12GenericUtility = new E12GenericUtility();
			if( date != null && date.trim().length() > 0)
			{
				return	Timestamp.valueOf(e12GenericUtility.getValidDateString(date, e12GenericUtility.getApplDateFormat(),e12GenericUtility.getDBDateFormat()) + " 00:00:00.0");
			}
			else
				return null;
		}


		private boolean IsDate(java.lang.String lsPolicyResult ) throws ibase.webitm.utility.ITMException, Exception 
		{

			E12GenericUtility e12GenericUtility = new E12GenericUtility();

			String pattern = e12GenericUtility.getApplDateFormat();
			try
			{
				SimpleDateFormat sdf = new SimpleDateFormat(pattern);
				if (sdf.format(sdf.parse(lsPolicyResult)).equals(lsPolicyResult))
				{
					return true;
				}
			}
			catch (Exception e)
			{
				return false;

			}
			return false;

		}


		private HashMap gf_get_prd_date(java.lang.String asItemSer,
				java.lang.String asSiteCode, Timestamp adtTranDate, Connection conn) throws SQLException 
				{

			String as_status = "";
			double ac_check_amt = 0, ac_net_amt=0;
			PreparedStatement pstmt = null,pstmt2 = null;
			String sql = "", sql2 = "";
			ResultSet rs = null,rs2 = null;

			HashMap dateMap = new HashMap<String, Timestamp>();
			//s_pass s_pass1
			String ls_ref_prd="", ls_prd_tbl="";
			Timestamp ldt_fr_date=null, ldt_to_date=null,adt_tran_date=null;

			sql = "select ref_code__prd  from itemser where item_ser = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,asItemSer);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				ls_ref_prd = rs.getString(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			// Sanjeev - 25/09/02 -  Set To Space If Null For db2.
			if( ls_ref_prd == null || ls_ref_prd.trim().length() == 0 )
			{
				ls_ref_prd = " ";
			}

			sql = "	select prd_tblno  from period_appl " +
					"	where site_code = ? " +
					"	and ref_code = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,asSiteCode);
			pstmt.setString(2,ls_ref_prd);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				ls_prd_tbl = rs.getString(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;


			if( ls_prd_tbl == null || ls_prd_tbl.trim().length() == 0 )
			{
				ls_prd_tbl = " ";
			}	

			if("db2".equalsIgnoreCase(CommonConstants.DB_NAME) )
			{    
				if( adtTranDate == null)
				{
					adt_tran_date = ldt_fr_date;
				}
			}

			sql = " select fr_date, to_date " +
					// " into :ldt_fr_date, :ldt_to_date " +
					" from period_tbl " +
					"	where prd_tblno = ? " +
					" and fr_date <=  ? " +
					" and to_date >= ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,ls_prd_tbl);
			pstmt.setTimestamp(2,adt_tran_date);
			pstmt.setTimestamp(3,adt_tran_date);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				ldt_fr_date = rs.getTimestamp("fr_date");
				ldt_to_date = rs.getTimestamp("to_date");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null; 

			dateMap.put("date1", ldt_fr_date);
			dateMap.put("date2", ldt_to_date);

			//s_pass1.mdate1 = ldt_fr_date
			//s_pass1.mdate2 = ldt_to_date

			//return s_pass1



			return dateMap;
				}


		public void writeLog(String tranId, String msgString) throws Exception
		{
			String errCode = "", writeString="";
			E12GenericUtility genericUtility = new  E12GenericUtility();

			java.io.File logFile = null;
			java.io.FileWriter logFileWtr = null;
			String filePath = CommonConstants.JBOSSHOME + File.separator +"chcheckog";
			SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
			String currTime = null;
			Calendar calendar = Calendar.getInstance();

			try
			{

				logFile = new java.io.File( filePath );
				if(!(logFile.exists()))
				{
					logFile.mkdir();
				}
				filePath = filePath + File.separator+ tranId+".log";
				logFile = new java.io.File( filePath );
				logFileWtr = new java.io.FileWriter( logFile, true );
				currTime = sdf1.format(new Timestamp(System.currentTimeMillis())).toString();
				currTime = currTime.replaceAll("-","");

				calendar.setTime(new java.sql.Timestamp(System.currentTimeMillis()));

				if( logFile == null )
				{
					errCode = "VBFILEOPEN";
				}
				else
				{
					writeString = "{" + currTime  + " " + calendar.get(Calendar.HOUR)+":"+calendar.get(Calendar.MINUTE) + "}\t" + msgString + "\r\n";
					char writeCharArr[] = new char[ writeString.length() ];
					writeString.getChars( 0, writeString.length(), writeCharArr, 0 );
					logFileWtr.write( writeCharArr );
				}
			}catch( Exception ex )
			{
				errCode = "VFLEIOERR";
				ex.printStackTrace();
				throw new ITMException(ex); //Added By Mukesh Chauhan on 05/08/19
			}
			finally{
				try{
					logFileWtr.close();
					logFileWtr = null;
					logFile = null;
				}catch( Exception e ){
					errCode = "VEFCLERR";
					e.printStackTrace();
				}
			}
			//return errCode;
		}
		private ArrayList getCrPolicyList(String asItemSer,String asApplyTime, Connection conn)  throws RemoteException,ITMException, SQLException
		{
			ArrayList crPolicyList = new ArrayList();
			PreparedStatement pstmt = null;
			String sql = "";
			ResultSet rs = null;
			String ls_errcode = "", ls_cr_policy="", ls_tot_cr_policy="";

			sql = " select case when cr_policy is null then '' else cr_policy end " +
					" from itemser_cr_policy " +
					" where item_ser = ? " +
					" and ( apply_time = ? or apply_time = 'B')";    
			//end change  
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,asItemSer);
			pstmt.setString(2,asApplyTime);
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				ls_cr_policy = checknull(rs.getString(1));
				crPolicyList.add(ls_cr_policy);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;


			return crPolicyList;
		}

		public ArrayList<String> credit_check_update(String SOrder, String cr_policy,double adNetAmt, String  asApplyTime, double lcCheckAmt,String as_check_flag,String lsStatus, Connection conn) throws SQLException
		{
			System.out.println("@@@@@@@@@@@ credit_check_update() called.............as_check_flag["+as_check_flag+"]lsStatus["+lsStatus+"]cr_policy["+cr_policy+"]asApplyTime["+asApplyTime+"]");
			////Updating the overriden records in business_logic_check table by invoice amount.
			////Finding the amount for which a new record should get inserted in business_logic_check table.

			String ls_ErrCode="" , ls_tran_id="" ,  merrcode = " ";
			double  lc_aprv_amt=0 , lc_bal_amt=0 , lc_new_aprv_amt=0 , lc_failed_amt=0;
			int 	 ll_count=0, ll_ocount=0 , ll_fcount=0;

			////Added Ruchira 24/08/2k6 (DI6SUN0120), If approved amt = 0 or status = 'O' then not to make any credit check.
			////Total no of records with failed status for the credit policy.
			String as_status = "";
			double ac_check_amt = 0, ac_net_amt=0;
			PreparedStatement pstmt = null,pstmt2 = null;
			String sql = "", sql2 = "";
			ResultSet rs = null,rs2 = null;

			ArrayList<String> retArrayList = new ArrayList<String>();
			
			if("C".equalsIgnoreCase(as_check_flag)) // as_check_flag = 'C' then		//Calculate amount	
			{		
				sql = " select sum(aprv_amt), count(1)" +
						//  "	into   :lc_aprv_amt, :ll_count " +
						"	from   business_logic_check " +
						"	where  sale_order =  ?  " +
						" and    cr_policy  = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,SOrder);
				pstmt.setString(2,cr_policy);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					lc_aprv_amt = rs.getDouble(1);
					ll_count = rs.getInt(2);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				System.out.println("@@@@@@ cr_policy [" +cr_policy + "] lc_aprv_amt["+lc_aprv_amt+"]ll_count["+ll_count+"]");
				//Checking for status only.
				if( lc_aprv_amt == 0  && ll_count > 0 )	
				{	
					sql = " select count(1) " +
							//  " into   :ll_ocount " +
							" from   business_logic_check " +
							" where  sale_order = ? " +
							" and    cr_policy  = ? " +
							"	and    aprv_stat  = 'O' ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,SOrder);
					pstmt.setString(2,cr_policy);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						ll_ocount = rs.getInt(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println("@@@@@@ cr_policy [" +cr_policy + "] lc_aprv_amt["+lc_aprv_amt+"]ll_count["+ll_count+"] ll_ocount [" + ll_ocount + "]");
					if( ll_ocount == ll_count )
					{
						as_status = "O";
					}
					else if( ll_count - ll_ocount  > 0 )
					{
						as_status = "F";
					}
					else
					{
						as_status = " ";
					}

					ac_check_amt = 0;
					System.out.println("@@@@@@@@@@3907 as_status["+as_status+"]");
				}	
				else				//////Checking for Amount
				{	
					if( ll_count > 0 )	
					{	
						//Checking for aprv_amt overriden.
						sql = " select sum(aprv_amt) " +
								//	  " into   :lc_aprv_amt " +
								"	from   business_logic_check " +
								" where  sale_order = ? " +
								"	and    cr_policy  = ? " +
								" and    aprv_stat  = 'O' " +
								" and    aprv_amt > 0 ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,SOrder);
						pstmt.setString(2,cr_policy);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							lc_aprv_amt = rs.getDouble(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						System.out.println("@@@@@@@ lc_aprv_amt["+lc_aprv_amt+"]");
						if( lc_aprv_amt > 0 )	
						{
							ac_check_amt = ac_net_amt - lc_aprv_amt; 
						}

						//If full amount is overriden then status must become 'O'
						if( ac_check_amt <= 0 )
						{
							as_status = "O";	
						}

						////If any failed record exists the it should not add a new faild record 
						////in business_logic_check table.
						sql = " select count(1) " +
								//	  " into   :ll_fcount " +
								" from   business_logic_check " +
								"	where  sale_order = ? " +
								" and    cr_policy  = ? " +
								"	and    aprv_stat  = 'F' ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,SOrder);
						pstmt.setString(2,cr_policy);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							ll_fcount = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						System.out.println("@@@@@@@@@ ll_fcount["+ll_fcount+"]");
						if( ll_fcount > 0 )
						{	
							ac_check_amt = 0;
							as_status = "F";
						}
						////End Added Ruchira 29/08/2k6
					}
					else
					{	
						ac_check_amt = 0;
					}
				}//end if	
				if( ll_count == 0 )
				{
					as_status = " ";		//New Record to be added
				}
				System.out.println("@@@@@@ Inside credit_check_update as_status["+as_status+"]");
			} 
			else if("U".equalsIgnoreCase(as_check_flag) && "I".equalsIgnoreCase(asApplyTime ))
			{	

				sql = " select tran_id , (aprv_amt - used_amt) " +
						"	from   business_logic_check " +
						"	where  sale_order = ? " +
						"	and    cr_policy  = ? " +
						"	and    aprv_stat  = 'O' " +
						"	and    (aprv_amt - used_amt) > 0 ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,SOrder);
				pstmt.setString(2,cr_policy);
				rs = pstmt.executeQuery();
				while (rs.next())
				{
					ls_tran_id = rs.getString(1);
					lc_bal_amt = rs.getDouble(2);

					//Setting balance net amt = 0, for in case of status = 'O' record found.
					if( lc_bal_amt >= lc_new_aprv_amt )			
					{	
						//Updating the used amount with balance amount, before exit.
						sql2 = " update business_logic_check set used_amt = used_amt + ? " +
								" where tran_id  = ? ";
						pstmt2 = conn.prepareStatement(sql2);
						pstmt2.setDouble(1,lc_new_aprv_amt);
						pstmt2.setString(2,ls_tran_id);
						int cnt2 = pstmt2.executeUpdate();
						pstmt2.close();
						pstmt2 = null; 
						//System.out.println("@@@@@@1 cnt2["+cnt2+"]");
						lc_new_aprv_amt = 0;
					}else	
					{	
						//Updating the used amount with balance amount
						sql2 = " update business_logic_check set used_amt = used_amt + ? " +
								" where tran_id  = ? ";
						pstmt2 = conn.prepareStatement(sql2);
						pstmt2.setDouble(1,lc_bal_amt);
						pstmt2.setString(2,ls_tran_id);
						int cnt2 = pstmt2.executeUpdate();
						pstmt2.close();
						pstmt2 = null; 
					//	System.out.println("@@@@@@2 cnt2["+cnt2+"]");
						lc_new_aprv_amt = lc_new_aprv_amt - lc_bal_amt	;		
					}//end if

				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;


				
			}
			System.out.println("@@@ as_status["+as_status+"]ac_check_amt["+ac_check_amt+"]");
			retArrayList.add(as_status);
			retArrayList.add(""+ac_check_amt);
			
			
			return retArrayList;
		}//end if
		////End Added Ruchira 24/08/2k6 (DI6SUN0120), If approved amt = 0 or status = 'O' then not to make any credit check.


		public boolean IsNumber(String str) 
		{
			try {
				Integer.parseInt(str);
				return true;
			} catch (NumberFormatException nfe) {}
			return false;
		}	

		private String String(double inputStr)
		{
			return ""+inputStr;
		}



		private String checknull(String inputStr)
		{
			if(inputStr==null)
			{
				inputStr="";
			}
			return inputStr;
		}
		
	}