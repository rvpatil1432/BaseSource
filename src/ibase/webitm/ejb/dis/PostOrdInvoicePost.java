package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;

import org.w3c.dom.Document;

import ibase.system.config.ConnDriver;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.DBAccessEJB;
import ibase.webitm.ejb.E12GenerateEDIEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.TransactionEmailTempltEJB;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.fin.InvAcct;
import ibase.webitm.ejb.fin.adv.CalculateCommission;
import ibase.webitm.ejb.fin.adv.RcvIbcaConf;
import ibase.webitm.ejb.sys.CreateRCPXML;
import ibase.webitm.ejb.sys.UtilMethods;
import ibase.webitm.utility.ITMException;
import ibase.webitm.utility.TransIDGenerator;

@Stateless
public class PostOrdInvoicePost extends ActionHandlerEJB implements PostOrdInvoicePostLocal,PostOrdInvoicePostRemote
{
	E12GenericUtility genericUtility= new E12GenericUtility();
	DistCommon distCommon= new DistCommon();
	FinCommon finCommon=new FinCommon();
	//Added By PriyankaC on 16Oct2019 [START]
//	ibase.utility.UserInfoBean userInfo = new UserInfoBean();
	TransactionEmailTempltEJB TransactionEmailTempltEJB = new TransactionEmailTempltEJB();
	//Added By PriyankaC on 16Oct2019 [END]
	//Added by wasim on 7-JUN-17 [START]
	String xtraParamsStr = "";
	//Added by wasim on 7-JUN-17 [END]
	String chgUser="";

	@Override
	//public String invoicePosting(String invoiceId, Connection conn) throws ITMException
	public String invoicePosting(String invoiceId,String xtraParams,String forcedFlag,Connection conn) throws ITMException
	{
		String postType = "H",retString="",sql="",itemSer="",lsCctrCodeSal="",lsAcctCodeSal="",lsAnalysis="",lsTemp="",tempInvId="",sqlUpd="";
		String detInvId="",detAcctCode="",detCctrCode="",hdrAnalCode="",detAnalysis1="",detAnalysis2="",detAnalysis3="",detItemCd="";
		String analysis1__dr="",analysis2__dr="",analysis3__dr="",analysis1__cr="",analysis2__cr="",analysis3__cr="";
		String lsCctrCodeDis="",lsAcctCodeDis="",lsAcctSchemedet="",lsAcctCodePr="",lsCctrCodePr="",lsAcctCodeTax="",lsCctrCodeTax="";
		String sql1="",lsTaxRecoCctr="",lsTaxRecoAcct="",lsAcctReco="",lsCctrReco="",lsAnalysis1="",lsAnalysis2="",lsAnalysis3="";
		String custCode="",round="",roundInvTo="",ls_acct_code__radj="",ls_cctr_code__radj="",errString="";
		double netamt=0.0,netamtr=0.0,roundTo=0.0,ln_amount_radj=0.0;
		int detLineNo=0,iActCnt=0,uActCnt=0,schemeCount=0;
		double detAmount=0.0,tempAmount=0.0,lnAmountDis=0.0,lcSchemeDiscount=0.0,lnAmountTax=0.0,lcRecoAmt=0.0,lnAmountSal=0.0;
		PreparedStatement pstmt=null,pstmtU=null,pstmt1=null,pstmt2=null;
		ResultSet rs=null,rs1=null,rs2=null;
		boolean connFlag=false;
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB(); // added by Mahesh Saggam on 29-07-2019

		// TODO Auto-generated method stub
		HashMap<String, String> InvoiceHrdMap=new HashMap<String, String>();

		//		HashMap<String, HashMap<String, String>> InvoiceAcctMap=new HashMap<String, HashMap<String,String>>();
		ArrayList<Object> invoiceAcctList=new ArrayList<Object>();
		ArrayList<Object> invoiceDeapList=new ArrayList<Object>();
		ArrayList<Object> invoiceDetList=new ArrayList<Object>();
		HashMap<String, String>InvoiceDetMap=null;
		String analysisStr []=null;

		String lineNoTemp="", prevSordNo = "";

		HashMap<String, String> invAcctMap = null;
		ArrayList<HashMap<String, String>> invAcctList = new ArrayList<HashMap<String,String>>();
		long startTime = 0, endTime = 0, totalTime = 0, totalHrs = 0, totlMts = 0, totSecs = 0; // Added
		double totRecoAmt = 0, roundAdj = 0;
		try
		{
			//Added by wasim on 07-JUN-2017 to get GS_MODE from Xtra Params 
			xtraParamsStr = xtraParams; 
			 
			startTime = System.currentTimeMillis();
			if(conn==null)
			{
				//Added and replace by sarita on 28DEC2017
				/*ConnDriver connDriver = new ConnDriver();			
				conn = connDriver.getConnectDB("DriverITM");
				//conn = getConnection();
				conn.setAutoCommit(false);
				connDriver = null;
				connFlag=true;*/
				conn = getConnection();
			}
			InvAcct invAcct=new InvAcct();
			
            ///Start of round code
			//Modified by Anjali R. on [29/01/2019][To Update tran_date as todays date in case of ledg_post_conf is "Y"][Start]
			String ledgPostConf = "";
			int rowCount = 0;
			sql = "select ledg_post_conf from transetup where tran_window = 'w_invoice'";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				ledgPostConf = checkNull(rs.getString("ledg_post_conf"));
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
			if("Y".equalsIgnoreCase(ledgPostConf)) 
			{
				sql = "update invoice set tran_date = trunc(sysdate) where invoice_id = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, invoiceId);
				rowCount  = pstmt.executeUpdate();
				System.out.println("count--["+rowCount+"]");
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
			}
			//Modified by Anjali R. on [29/01/2019][To Update tran_date as todays date in case of ledg_post_conf is "Y"][End]
			sql="select cust_code,item_ser,net_amt, round_adj from invoice where invoice_id=?";
			pstmt2=	conn.prepareStatement(sql);
			pstmt2.setString(1,invoiceId );
			rs2=pstmt2.executeQuery();
			//while(rs2.next())
			if(rs2.next())
			{
				custCode=rs2.getString("cust_code");
				itemSer=rs2.getString("item_ser");
				netamt=rs2.getDouble("net_amt");
				roundAdj=rs2.getDouble("round_adj");
			}
			rs2.close();//added by Pavan R 10oct18[to handle open cursor issue]
			rs2 = null;	pstmt2.close();	pstmt2 = null;
			if(roundAdj == 0)
			{
				sql="select round, case when round_to is null then 0.001 else round_to end  as round_to " +
						" from customer where cust_code = ?";
				pstmt2=	conn.prepareStatement(sql);
				pstmt2.setString(1,custCode ) ;
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
					pstmt2.setString(1,itemSer);
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
						netamtr= Math.round(netamt);
						sql="Update invoice set net_amt=?,round_adj=? where invoice_id=?";
								pstmt2=	conn.prepareStatement(sql);
								pstmt2.setDouble(1,netamtr);
								pstmt2.setDouble(2,netamtr-netamt);
								pstmt2.setString(3,invoiceId);
								int count=pstmt2.executeUpdate();
								pstmt2.close();//added by Pavan R 10oct18[to handle open cursor issue]
								pstmt2 = null;
								System.out.println("count++++"+count);
								
						//xmlBuff.append("<round_adj><![CDATA["+(netamt-netAmt)+"]]></round_adj>");  			 
						//xmlBuff.append("<net_amt><![CDATA["+netamt+"]]></net_amt>");
					}
				}else
				{
					netamtr=distCommon.getRndamt(netamt,round,roundTo);
							sql="Update invoice set net_amt=?,round_adj=? where invoice_id=?";
					pstmt2=	conn.prepareStatement(sql);
					pstmt2.setDouble(1,netamtr);
					pstmt2.setDouble(2,netamtr-netamt);
					pstmt2.setString(3,invoiceId);
					int count=pstmt2.executeUpdate();
					pstmt2.close();//added by Pavan R 10oct18[to handle open cursor issue]
					pstmt2 = null;
					System.out.println("count1++++"+count);
					//xmlBuff.append("<round_adj><![CDATA["+(netamt-netamtr)+"]]></round_adj>");  			 
					//xmlBuff.append("<net_amt><![CDATA["+netamt+"]]></net_amt>");
				}
			}
			///End of round code

			/**
			 * Select from
			 * Invoice header
			 * */
			InvoiceHrdMap=getInvoiceHdr(invoiceId,conn);
			postType = InvoiceHrdMap.get("posttype");
			/**
			 * Select from
			 * Invoice trace
			 * */
			invoiceDeapList=getInvoiceDesp(invoiceId,conn);
			//System.out.println("Invoice trace list size:- ["+invoiceDeapList.size()+"]");

			//	        invoiceDetList=getInvoiceDet(invoiceId,conn);

			/**
			 * Invoice trace list size
			 * */
			if(invoiceDeapList.size()>0)
			{
				/**
				 * Insert in Table 'invacct'
				 * for each 'invdet' line 
				 * */
				//System.out.println("manohar invoiceDeapList ["+invoiceDeapList+"]");
				for(int itr=0;itr<invoiceDeapList.size();itr++)
				{
					InvoiceDetMap=(HashMap<String, String>) invoiceDeapList.get(itr);
					//System.out.println("@@@@@91 itr["+itr+"] InvoiceDetMap["+InvoiceDetMap+"]");
					detInvId=InvoiceDetMap.get("invoice_id");
					detItemCd=InvoiceDetMap.get("item_code");

					hdrAnalCode=InvoiceHrdMap.get("anal_code");
					detLineNo=Integer.parseInt(InvoiceDetMap.get("line_no"));

					detAmount=Double.parseDouble(InvoiceDetMap.get("quantity__stduom")) * Double.parseDouble(InvoiceDetMap.get("rate__stduom"));
					//System.out.println("@@@@@@@99 detAmount["+detAmount+"]");

					sql="select item_ser from item where item_code=? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, detItemCd);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						itemSer=rs.getString("item_ser");
					}
					pstmt.close();
					pstmt=null;
					rs.close();
					rs=null;

					/**
					 * Select 'ls_acct_schemedet','ls_acct_code__pr','ls_cctr_code__pr'
					 * from 'bom' and 'sorderdet_scheme' 
					 * */
					if (!prevSordNo.trim().equals(InvoiceDetMap.get("sord_no").trim()))
					{
						prevSordNo = InvoiceDetMap.get("sord_no");
						sql="select count(1) from bom a,sorderdet_scheme b"
								+ " where a.bom_code = b.scheme_code"
								+ " and tran_id = ?";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1, InvoiceDetMap.get("sord_no"));
						rs=pstmt.executeQuery();
						if(rs.next())
						{
							schemeCount = rs.getInt(1);
						}
						pstmt.close();
						pstmt=null;
						rs.close();
						rs=null;
						lsAnalysis=invAcct.AcctAnalysisType(" ","S-INV",InvoiceHrdMap.get("inv_type"),"CR",conn);				
						lsTemp="CR"+lsAnalysis;

						analysisStr = lsAnalysis.split("@");
						int lenCr = analysisStr.length-1;
						//System.out.println("@@@cr len["+len+"]");
						if( lenCr > -1 )
						{
							analysis1__cr = analysisStr[0];
						}
						else
						{
							analysis1__cr = "";	
						}	
						if( lenCr > 0 )
						{
							analysis2__cr = analysisStr[1];	
						}
						else
						{	
							analysis2__cr = "";
						}
						if( lenCr > 1)
						{
							analysis3__cr = analysisStr[2];
						}
						else
						{
							analysis3__cr ="";	
						}

						lsAnalysis=invAcct.AcctAnalysisType(" ","S-INV",InvoiceHrdMap.get("inv_type"),"DR",conn);				
						lsTemp="DR"+lsAnalysis;

						analysisStr = lsAnalysis.split("@");
						int lenDr = analysisStr.length-1;
						//System.out.println("@@@cr len["+len+"]");
						if( lenDr > -1 )
						{
							analysis1__dr = analysisStr[0];
						}
						else
						{
							analysis1__dr = "";	
						}	
						if( lenDr > 0 )
						{
							analysis2__dr = analysisStr[1];	
						}
						else
						{	
							analysis2__dr = "";
						}
						if( lenDr > 1)
						{
							analysis3__dr = analysisStr[2];
						}
						else
						{
							analysis3__dr ="";	
						}

					}
					if ("D".equals(postType)) // 06-dec-16 manoharan posttype to be considered
					{
						if (schemeCount > 0)
						{
							sql="select a.acc_code__item,a.acct_code__pr,a.cctr_code__pr from bom a,sorderdet_scheme b"
									+ " where a.bom_code = b.scheme_code"
									+ " and tran_id = ? and line_no_form = ?";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1, InvoiceDetMap.get("sord_no"));
							pstmt.setString(2, InvoiceDetMap.get("sord_line_no"));
							rs=pstmt.executeQuery();
							if(rs.next())
							{
								lsAcctSchemedet=checkNull(rs.getString("acc_code__item"));
								lsAcctCodePr=checkNull(rs.getString("acct_code__pr"));
								lsCctrCodePr=checkNull(rs.getString("cctr_code__pr"));
							}
							pstmt.close();
							pstmt=null;
							rs.close();
							rs=null;
						}

						lsCctrCodeSal=checkNull(finCommon.getAcctDetrTtype(InvoiceDetMap.get("item_code"), itemSer, "SAL", InvoiceHrdMap.get("inv_type"), conn));  // Changes by Nandkumar Gadkari on 10/08/18
						//detCctrCode=lsCctrCodeSal;
						//System.out.println("@@@@@@@@@@@@@@ lsCctrCodeSal["+lsCctrCodeSal+"]");
						String ls_acct_code__sal[]=lsCctrCodeSal.split(",");
						if( ls_acct_code__sal.length > 0)
						{
							lsAcctCodeSal=ls_acct_code__sal[0];
						}
						if( ls_acct_code__sal.length > 1)
						{
							lsCctrCodeSal=ls_acct_code__sal[1];
						}
					}
					else
					{
					
						lsAcctCodeSal =  InvoiceHrdMap.get("acct_code__sal");
						lsCctrCodeSal =  InvoiceHrdMap.get("cctr_code__sal");
						lsAcctCodeDis = InvoiceHrdMap.get("acct_code__dis");
						lsCctrCodeDis = InvoiceHrdMap.get("cctr_code__dis");
						lsAcctCodePr = InvoiceHrdMap.get("acct_code__pr");
						lsCctrCodePr = InvoiceHrdMap.get("cctr_code__pr");
					}
					//		        	detAcctCode=lsAcctCodeSal;
					//System.out.println("@@@@@@@@@@@@@@lsAcctCodeSal["+lsAcctCodeSal+"]:::lsCctrCodeSal["+lsCctrCodeSal+"]");
					System.out.println("@@@@@@213 detAmount["+detAmount+"]");


					invAcctMap = new HashMap<String, String>();

					invAcctMap.put("invoice_id", detInvId);
					//invAcctMap.put("line_no",detLineNo);
					invAcctMap.put("acct_code", lsAcctCodeSal);
					if(lsAcctCodeSal == null || lsAcctCodeSal.trim().length() == 0) // Added by Mahesh Saggam on 29-07-2019 [account code should not be null]
					{
						retString = itmDBAccessEJB.getErrorString("", "VTSLACCT", "","", conn);
						//return retString;
						//added by monika 13 dec 19 to check retstring null or not
						if(retString != null && retString.trim().length() > 0)
						{
							return retString;
						}//end
					}
					invAcctMap.put("cctr_code", lsCctrCodeSal);
					invAcctMap.put("anal_code", hdrAnalCode);
					invAcctMap.put("amount", ""+detAmount);
					invAcctMap.put("analysis1", analysis1__cr); 
					invAcctMap.put("analysis2", analysis2__cr);
					invAcctMap.put("analysis3", analysis3__cr);
					//System.out.println("@@@@@@@1 invAcctMap["+invAcctMap+"]");
					//invAcctList.add(invAcctMap);

					invAcctList = modifyinvAcctList(invAcctList,invAcctMap,lsAcctCodeSal,lsCctrCodeSal , detAmount, checkNullSpace(hdrAnalCode) );

					//lnAmountDis=(Double.parseDouble(InvoiceDetMap.get("rate__stduom"))*Double.parseDouble(InvoiceDetMap.get("quantity__stduom"))) * Double.parseDouble(InvoiceDetMap.get("rate__stduom"))/100;
					lnAmountDis=(Double.parseDouble(InvoiceDetMap.get("rate__stduom"))*Double.parseDouble(InvoiceDetMap.get("quantity__stduom"))) * Double.parseDouble(InvoiceDetMap.get("discount"))/100;
					//System.out.println("@@@@@@243 lnAmountDis["+lnAmountDis+"]");
					if(lnAmountDis!=0)
					{
						/**
						 * find 'ls_cctr_code__dis'
						 * and 'ls_acct_code__dis'
						 * */
						if ("D".equals(postType))
						{

							lsCctrCodeDis=checkNull(finCommon.getAcctDetrTtype(InvoiceDetMap.get("item_code"), itemSer, "DIS", InvoiceHrdMap.get("inv_type"), conn));	 // Changes by Nandkumar Gadkari on 10/08/18	        	
							String ls_acct_code__dis[]=lsCctrCodeDis.split(",");
							if( ls_acct_code__dis.length > 0)
							{
								lsAcctCodeDis=ls_acct_code__dis[0];
							}
							if( ls_acct_code__dis.length > 1)
							{
								lsCctrCodeDis=ls_acct_code__dis[1];
							}
							//			        	detAcctCode=lsAcctCodeDis;

							if(lsCctrCodeDis==null || lsCctrCodeDis.trim().length()==0)
							{
								lsCctrCodeDis=InvoiceHrdMap.get("cctr_code__dis");
							}
						}

						invAcctMap = new HashMap<String, String>();

						invAcctMap.put("invoice_id", detInvId);
						//invAcctMap.put("line_no",detLineNo);
						invAcctMap.put("acct_code", lsAcctCodeDis);
						if(lsAcctCodeDis == null || lsAcctCodeDis.trim().length() == 0) // Added by Mahesh Saggam on 29-07-2019 [account code should not be null]
						{
							retString = itmDBAccessEJB.getErrorString("", "VTDISACCT", "","", conn);
						//	return retString;
							//added by monika 13 dec 19 to check retstring null or not
							if(retString != null && retString.trim().length() > 0)
							{
								return retString;
							}//end
						}
						invAcctMap.put("cctr_code", lsCctrCodeDis);
						invAcctMap.put("anal_code", hdrAnalCode);
						invAcctMap.put("amount", ""+lnAmountDis*(-1));
						//invAcctMap.put("amount", ""+lnAmountDis);//CHANGE BY CHANDRASHEKAR
						invAcctMap.put("analysis1", ""); 
						invAcctMap.put("analysis2", "");
						invAcctMap.put("analysis3", "");
						//System.out.println("@@@@@@@2 invAcctMap["+invAcctMap+"]");
						//invAcctList.add(invAcctMap);
						invAcctList = modifyinvAcctList(invAcctList,invAcctMap,lsAcctCodeDis,lsCctrCodeDis , -1 * lnAmountDis, checkNullSpace(hdrAnalCode) );


					}//lnAmountDis amount not 0 END

					/**
					 * Find lc_scheme_discount
					 * */
					lcSchemeDiscount=Double.parseDouble(InvoiceDetMap.get("disc_schem_billback_amt"))+Double.parseDouble(InvoiceDetMap.get("disc_schem_offinv_amt"));
					//System.out.println("@@@@@@304 lcSchemeDiscount["+lcSchemeDiscount+"]");
					if(lcSchemeDiscount!=0)
					{        		

						invAcctMap = new HashMap<String, String>();

						invAcctMap.put("invoice_id", detInvId);
						//invAcctMap.put("line_no",detLineNo);
						invAcctMap.put("acct_code", lsAcctCodePr);
						if(lsAcctCodePr == null || lsAcctCodePr.trim().length() == 0) // Added by Mahesh Saggam on 29-07-2019 [account code should not be null]
						{
							retString = itmDBAccessEJB.getErrorString("", "VTPRACCT", "","", conn);
						//	return retString;
							//added by monika 13 dec 19 to check retstring null or not
							if(retString != null && retString.trim().length() > 0)
							{
								return retString;
							}//end
						}
						invAcctMap.put("cctr_code", lsCctrCodePr);
						invAcctMap.put("anal_code", hdrAnalCode);
						invAcctMap.put("amount", ""+lnAmountDis);
						invAcctMap.put("analysis1", analysis1__dr); 
						invAcctMap.put("analysis2", analysis2__dr);
						invAcctMap.put("analysis3", analysis3__dr);
						//System.out.println("@@@@@@@3 invAcctMap["+invAcctMap+"]");
						//invAcctList.add(invAcctMap);
						invAcctList = modifyinvAcctList(invAcctList,invAcctMap,lsAcctCodePr,lsCctrCodePr , lnAmountDis, checkNullSpace(hdrAnalCode) );

					}// lc_scheme_discount>0 END

					/**
					 * check lc_billback_amt>0
					 * */
					//System.out.println("@@@@@@345 InvoiceDetMap.get(disc_schem_billback_amt)["+InvoiceDetMap.get("disc_schem_billback_amt")+"]");

					if(Double.parseDouble(InvoiceDetMap.get("disc_schem_billback_amt"))>0)
					{

						invAcctMap = new HashMap<String, String>();

						invAcctMap.put("invoice_id", detInvId);
						//invAcctMap.put("line_no",detLineNo);
						invAcctMap.put("acct_code", lsAcctCodePr);
						if(lsAcctCodePr == null || lsAcctCodePr.trim().length() == 0) // Added by Mahesh Saggam on 29-07-2019 [account code should not be null]
						{
							retString = itmDBAccessEJB.getErrorString("", "VTPRACCT", "","", conn);
						//	return retString;
							//added by monika 13 dec 19 to check retstring null or not
							if(retString != null && retString.trim().length() > 0)
							{
								return retString;
							}//end
						}
						invAcctMap.put("cctr_code", lsCctrCodePr);
						invAcctMap.put("anal_code", hdrAnalCode);
						invAcctMap.put("amount", InvoiceDetMap.get("disc_schem_billback_amt"));
						invAcctMap.put("analysis1", analysis1__dr); 
						invAcctMap.put("analysis2", analysis2__dr);
						invAcctMap.put("analysis3", analysis3__dr);
						//System.out.println("@@@@@@@4 invAcctMap["+invAcctMap+"]");
						//invAcctList.add(invAcctMap);
						invAcctList = modifyinvAcctList(invAcctList,invAcctMap,lsAcctCodePr,lsCctrCodePr , Double.parseDouble(InvoiceDetMap.get("disc_schem_billback_amt")), checkNullSpace(hdrAnalCode) );

					}// lc_billback_amt>0 END

					/**
					 * Select 'taxtran'details
					 * */
					System.out.println("08-Mar-17 manohar before populating tax invAcctList ["+invAcctList+"]" );
					lineNoTemp = "   "+InvoiceDetMap.get("line_no"); 
					lineNoTemp = lineNoTemp.substring(lineNoTemp.length()-3, lineNoTemp.length());
					//System.out.println("@@@@@@@ lineNoTemp["+lineNoTemp+"]");
					totRecoAmt = 0;
					sql="select acct_code,cctr_code,sum(tax_amt) as tax_amt, case when sum(reco_amount) is null then 0 else sum(reco_amount) end as reco_amount  "
							+ " from taxtran where tran_code = 'S-INV'  and tran_id	 = ? and line_no = ?  " 
							+ " and  effect <> 'N' group BY acct_code,cctr_code";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, InvoiceDetMap.get("invoice_id"));
					pstmt.setString(2, lineNoTemp);
					rs=pstmt.executeQuery();
					while(rs.next())
					{
						lsAcctCodeTax=checkNull(rs.getString("acct_code")).trim();
						lsCctrCodeTax=checkNull(rs.getString("cctr_code"));
						lnAmountTax=rs.getDouble("tax_amt");
						lcRecoAmt=rs.getDouble("reco_amount");
						totRecoAmt += lcRecoAmt;
						System.out.println("08-Mar-17 manohar 1 posting tax for line ["+lineNoTemp+"] lsAcctCodeTax [" + lsAcctCodeTax + "] lsCctrCodeTax [" + lsCctrCodeTax+ "] lnAmountTax [" + lnAmountTax + "] lcRecoAmt[" + lcRecoAmt+ "] totRecoAmt [" + totRecoAmt+ "]" );
						if( lsAcctCodeTax == null || lsAcctCodeTax.trim().length() == 0)
						{
							lsAcctCodeTax = lsAcctCodeSal;
							lsCctrCodeTax = lsCctrCodeSal;
						}
						if( lsCctrCodeTax == null || lsCctrCodeTax.trim().length() == 0 )
						{ 
							lsCctrCodeTax = " ";
						}
						if (lcRecoAmt != 0 )
						{
							lnAmountTax=lnAmountTax-lcRecoAmt;
							
						}
						System.out.println("08-Mar-17 manohar 2 posting tax for line ["+lineNoTemp+"] lsAcctCodeTax [" + lsAcctCodeTax + "] lsCctrCodeTax [" + lsCctrCodeTax+ "] lnAmountTax [" + lnAmountTax + "] lcRecoAmt[" + lcRecoAmt+ "] totRecoAmt [" + totRecoAmt+ "]" );
						if (lnAmountTax != 0 )
						{
							invAcctMap = new HashMap<String, String>();

							invAcctMap.put("invoice_id", detInvId);
							//invAcctMap.put("line_no",detLineNo);
							invAcctMap.put("acct_code", lsAcctCodeTax);
							if(lsAcctCodeTax == null || lsAcctCodeTax.trim().length() == 0) // Added by Mahesh Saggam on 29-07-2019 [account code should not be null]
							{
								retString = itmDBAccessEJB.getErrorString("", "VTTXACCT", "","", conn);
								return retString;
							}
							invAcctMap.put("cctr_code", lsCctrCodeTax);
							invAcctMap.put("anal_code", hdrAnalCode);
							invAcctMap.put("amount", ""+lnAmountTax);
							invAcctMap.put("analysis1", ""); 
							invAcctMap.put("analysis2", "");
							invAcctMap.put("analysis3", "");
							//System.out.println("@@@@@@@5 invAcctMap["+invAcctMap+"]");
							//invAcctList.add(invAcctMap);

							invAcctList = modifyinvAcctList(invAcctList,invAcctMap,lsAcctCodeTax,lsCctrCodeTax , lnAmountTax, checkNullSpace(hdrAnalCode) );
						}

					}//'taxtran'details while END
					pstmt.close();
					pstmt=null;
					rs.close();
					rs=null;
					System.out.println("08-Mar-17 manohar after populating tax invAcctList ["+invAcctList+"]" );

					/**
					 * tax reco details
					 * from 'taxtran'
					 * */
					if(totRecoAmt != 0) // 07-dec-16 manoharan
					{
						lsTaxRecoCctr=checkNull(finCommon.getAcctDetrTtype(InvoiceDetMap.get("item_code"), itemSer, "TAXRECO", InvoiceHrdMap.get("inv_type"), conn)); // Changes by Nandkumar Gadkari on 10/08/18
						//		        	detCctrCode=lsCctrCodeSal;

						String ls_tax_reco_acct[]=lsTaxRecoCctr.split(",");
						if( ls_tax_reco_acct.length > 0)
						{
							lsTaxRecoAcct=ls_tax_reco_acct[0];
						}
						if( ls_tax_reco_acct.length > 1)
						{
							lsTaxRecoCctr =ls_tax_reco_acct[1];
						}
						//		        	detAcctCode=lsAcctCodeSal;
					}
					lineNoTemp = "   "+InvoiceDetMap.get("line_no"); 
					lineNoTemp = lineNoTemp.substring(lineNoTemp.length()-3, lineNoTemp.length());
					//System.out.println("@@@@@@@ lineNoTemp["+lineNoTemp+"]");
					totRecoAmt = 0;
					sql="select acct_code__reco, cctr_code__reco, case when sum(reco_amount) is null then 0 else sum(reco_amount) end as reco_amount "
							+ " from taxtran"
							+ " where tran_code = 'S-INV'  and tran_id	 = ?"
							+ " and line_no = ? and   effect <> 'N'"
							+ " and (case when reco_amount is null then 0 else reco_amount end) <> 0"
							+ " group BY acct_code__reco, cctr_code__reco  ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, InvoiceDetMap.get("invoice_id"));
					pstmt.setString(2, lineNoTemp);
					rs=pstmt.executeQuery();
					while(rs.next())
					{
						lsAcctReco=checkNull(rs.getString("acct_code__reco"));
						lsCctrReco=checkNull(rs.getString("cctr_code__reco"));
						lcRecoAmt=rs.getDouble("reco_amount");
						totRecoAmt += lcRecoAmt;
						System.out.println("08-Mar-17 manohar 1 posting reco tax for line ["+lineNoTemp+"] lsAcctReco [" + lsAcctReco + "] lsCctrReco [" + lsCctrReco+ "] lnAmountTax [" + lnAmountTax + "] lcRecoAmt[" + lcRecoAmt+ "] totRecoAmt [" + totRecoAmt+ "]" );
						if (lcRecoAmt != 0) // 07-dec-16 manoharan
						{
							if( lsAcctReco == null || lsAcctReco.trim().length() == 0 )
							{		
								lsAcctReco = lsTaxRecoAcct;
								lsCctrReco = lsTaxRecoCctr;
							}
							if( lsCctrReco == null || lsCctrReco.trim().length() == 0 )
							{ 
								lsCctrReco = " ";
							}


							invAcctMap = new HashMap<String, String>();

							invAcctMap.put("invoice_id", detInvId);
							//invAcctMap.put("line_no",detLineNo);
							invAcctMap.put("acct_code", lsAcctReco);
							if(lsAcctReco == null || lsAcctReco.trim().length() == 0) // Added by Mahesh Saggam on 29-07-2019 [account code should not be null]
							{
								retString = itmDBAccessEJB.getErrorString("", "VTRECACCT", "","", conn);
								return retString;
							}
							invAcctMap.put("cctr_code", lsCctrReco);
							invAcctMap.put("anal_code", hdrAnalCode);
							invAcctMap.put("amount", ""+lcRecoAmt);
							invAcctMap.put("analysis1", ""); 
							invAcctMap.put("analysis2", "");
							invAcctMap.put("analysis3", "");
							//System.out.println("@@@@@@@6 invAcctMap["+invAcctMap+"]");
							//invAcctList.add(invAcctMap);

							invAcctList = modifyinvAcctList(invAcctList,invAcctMap,lsAcctReco,lsCctrReco , lcRecoAmt, checkNullSpace(hdrAnalCode) );
						}

					}//tax reco details while END
					pstmt.close();
					pstmt=null;
					rs.close();
					rs=null;
					System.out.println("08-Mar-17 manohar after populating reco tax invAcctList ["+invAcctList+"]" );


 
				}//invoiceDeapList FOR loop END
			}
			else// Invoice Trace is empty
			{
				/**
				 * If invoice trace is empty
				 * select from invoice det
				 * */
				invoiceDetList=getInvoiceDet(invoiceId,conn);
				//System.out.println("Invoice det list size :- ["+invoiceDetList.size()+"]");

				/**
				 * If invoice trace is empty
				 * invoiceDetList loop
				 * */
				for(int itr=0;itr<invoiceDetList.size();itr++)
				{
					InvoiceDetMap=(HashMap<String, String>) invoiceDetList.get(itr);
					//System.out.println("@@@@@@526 itr["+itr+"]InvoiceDetMap["+InvoiceDetMap+"]");
					detInvId=InvoiceDetMap.get("invoice_id");
					detItemCd=InvoiceDetMap.get("item_code");

					hdrAnalCode=InvoiceHrdMap.get("anal_code");
					detLineNo=Integer.parseInt(InvoiceDetMap.get("line_no"));

					lnAmountSal=Double.parseDouble(InvoiceDetMap.get("quantity__stduom")) * Double.parseDouble(InvoiceDetMap.get("rate__stduom"));
					lnAmountDis=Double.parseDouble(InvoiceDetMap.get("disc_amt"));

					lsAnalysis1=InvoiceDetMap.get("analysis1");
					lsAnalysis2=InvoiceDetMap.get("analysis2");
					lsAnalysis3 =InvoiceDetMap.get("analysis3");

					sql="select item_ser from item where item_code=? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, detItemCd);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						itemSer=rs.getString("item_ser");
					}
					pstmt.close();
					pstmt=null;
					rs.close();
					rs=null;

					if ("D".equals(postType)) // 06-dec-16 manoharan posttype to be considered
					{

						lsCctrCodeSal=checkNull(finCommon.getAcctDetrTtype(InvoiceDetMap.get("item_code"), itemSer, "SAL", InvoiceHrdMap.get("inv_type"), conn)); // Changes by Nandkumar Gadkari on 10/08/18

						String ls_acct_code__sal[]=lsCctrCodeSal.split(",");
						if( ls_acct_code__sal.length > 0)
						{
							lsAcctCodeSal=ls_acct_code__sal[0];
						}
						if( ls_acct_code__sal.length > 1)
						{
							lsCctrCodeSal=ls_acct_code__sal[1];
						}
					}
					else
					{
						lsAcctCodeSal =  InvoiceHrdMap.get("acct_code__sal");
						lsCctrCodeSal =  InvoiceHrdMap.get("cctr_code__sal");
						lsAcctCodeDis = InvoiceHrdMap.get("acct_code__dis");
						lsCctrCodeDis = InvoiceHrdMap.get("cctr_code__dis");
						lsAcctCodePr = InvoiceHrdMap.get("acct_code__pr");
						lsCctrCodePr = InvoiceHrdMap.get("cctr_code__pr");
					}
					if (lnAmountSal != 0) // 07-dec-16 manoharan
						{

						invAcctMap = new HashMap<String, String>();

						invAcctMap.put("invoice_id", detInvId);
						//invAcctMap.put("line_no",detLineNo);
						invAcctMap.put("acct_code", lsAcctCodeSal); 
						if(lsAcctCodeSal == null || lsAcctCodeSal.trim().length() == 0)// Added by Mahesh Saggam on 29-07-2019 [account code should not be null]
						{
							retString = itmDBAccessEJB.getErrorString("", "VTSLACCT", "","", conn);
							//return retString;
							//added by monika 13 dec 19 to check retstring null or not
							if(retString != null && retString.trim().length() > 0)
							{
								return retString;
							}//end
						}
						invAcctMap.put("cctr_code", lsCctrCodeSal);
						invAcctMap.put("anal_code", hdrAnalCode);
						invAcctMap.put("amount", ""+lnAmountSal);
						invAcctMap.put("analysis1", lsAnalysis1); 
						invAcctMap.put("analysis2", lsAnalysis2);
						invAcctMap.put("analysis3", lsAnalysis3);
						//System.out.println("@@@@@@@7 invAcctMap["+invAcctMap+"]");
						//invAcctList.add(invAcctMap);

						invAcctList = modifyinvAcctList(invAcctList,invAcctMap,lsAcctCodeSal,lsCctrCodeSal , lnAmountSal, checkNullSpace(hdrAnalCode) );
					}

					if(lnAmountDis!=0)
					{
						/**
						 * find 'ls_cctr_code__dis'
						 * and 'ls_acct_code__dis'
						 * */
						if ("D".equals(postType)) // 06-dec-16 manoharan posttype to be considered
						{
							lsCctrCodeDis=checkNull(finCommon.getAcctDetrTtype(InvoiceDetMap.get("item_code"), itemSer, "DIS", InvoiceHrdMap.get("inv_type"), conn));// Changes by Nandkumar Gadkari on 10/08/18		        	
							String ls_acct_code__dis[]=lsCctrCodeDis.split(",");
							if( ls_acct_code__dis.length > 0 )
							{
								lsAcctCodeDis=ls_acct_code__dis[0];
							}
							if( ls_acct_code__dis.length > 1 )
							{
								lsCctrCodeDis=ls_acct_code__dis[1];
							}
						}
						else
						{
							lsAcctCodeDis = InvoiceHrdMap.get("acct_code__dis");
							lsCctrCodeDis = InvoiceHrdMap.get("cctr_code__dis");
						}

						invAcctMap = new HashMap<String, String>();

						invAcctMap.put("invoice_id", detInvId);
						//invAcctMap.put("line_no",detLineNo);
						invAcctMap.put("acct_code", lsAcctCodeDis);
						if(lsAcctCodeDis == null || lsAcctCodeDis.trim().length() == 0)// Added by Mahesh Saggam on 29-07-2019 [account code should not be null]
						{
							retString = itmDBAccessEJB.getErrorString("", "VTDISACCT", "","", conn);
							//return retString;
							//added by monika 13 dec 19 to check retstring null or not
							if(retString != null && retString.trim().length() > 0)
							{
								return retString;
							}//end
						}
						invAcctMap.put("cctr_code", lsCctrCodeDis);
						invAcctMap.put("anal_code", hdrAnalCode);
						invAcctMap.put("amount", ""+lnAmountDis);
						invAcctMap.put("analysis1", lsAnalysis1); 
						invAcctMap.put("analysis2", lsAnalysis2);
						invAcctMap.put("analysis3", lsAnalysis3);
						//System.out.println("@@@@@@@8 invAcctMap["+invAcctMap+"]");
						//invAcctList.add(invAcctMap);
						invAcctList = modifyinvAcctList(invAcctList,invAcctMap,lsAcctCodeDis,lsCctrCodeDis , lnAmountDis, checkNullSpace(hdrAnalCode) );

					}// lnAmountDis!=0 END
					/**
					 * select 'taxtran' details
					 * */
					lineNoTemp = "   "+InvoiceDetMap.get("line_no"); 
					lineNoTemp = lineNoTemp.substring(lineNoTemp.length()-3, lineNoTemp.length());
					//System.out.println("@@@@@@@ lineNoTemp["+lineNoTemp+"]");
					totRecoAmt = 0;
					sql="select acct_code,cctr_code,sum(tax_amt) as tax_amt , case when sum(reco_amount) is null then 0 else sum(reco_amount) end as reco_amount "
							+ " from taxtran"
							+ " where tran_code = 'S-INV'  and tran_id	 = ?"
							+ " and line_no = ?  and effect <> 'N'"
							+ " group BY acct_code,cctr_code";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, InvoiceDetMap.get("invoice_id"));
					pstmt.setString(2, lineNoTemp);
					rs=pstmt.executeQuery();
					while(rs.next())
					{
						lsAcctCodeTax=checkNull(rs.getString("acct_code"));
						lsCctrCodeTax=checkNull(rs.getString("cctr_code"));
						lnAmountTax=rs.getDouble("tax_amt");
						lcRecoAmt=rs.getDouble("reco_amount");
						totRecoAmt += lcRecoAmt;
						
						//if (totRecoAmt != 0) // 07-dec-16 manoharan 09apr2019 pavan
						if (lnAmountTax != 0) // 09apr19 Pavan R
						{
							if( lsAcctCodeTax == null || lsAcctCodeTax.trim().length() == 0)
							{
								lsAcctCodeTax = lsAcctCodeSal;
								lsCctrCodeTax = lsCctrCodeSal;
							}
							if( lsCctrCodeTax == null || lsCctrCodeTax.trim().length() == 0 )
							{ 
								lsCctrCodeTax = " ";
							}

							invAcctMap = new HashMap<String, String>();

							invAcctMap.put("invoice_id", detInvId);
							//invAcctMap.put("line_no",detLineNo);
							invAcctMap.put("acct_code", lsAcctCodeTax);
							if(lsAcctCodeTax == null || lsAcctCodeTax.trim().length() == 0) // Added by Mahesh Saggam on 29-07-2019 [account code should not be null]
							{
								retString = itmDBAccessEJB.getErrorString("", "VTTXACCT", "","", conn);
								//return retString;
								//added by monika 13 dec 19 to check retstring null or not
								if(retString != null && retString.trim().length() > 0)
								{
									return retString;
								}//end
							}
							invAcctMap.put("cctr_code", lsCctrCodeTax);
							invAcctMap.put("anal_code", hdrAnalCode);
							invAcctMap.put("amount", ""+lnAmountTax);
							invAcctMap.put("analysis1", lsAnalysis1); 
							invAcctMap.put("analysis2", lsAnalysis2);
							invAcctMap.put("analysis3", lsAnalysis3);
						//	System.out.println("@@@@@@@9 invAcctMap["+invAcctMap+"]");
							//invAcctList.add(invAcctMap);

							invAcctList = modifyinvAcctList(invAcctList,invAcctMap,lsAcctCodeTax,lsCctrCodeTax , lnAmountTax, checkNullSpace(hdrAnalCode) );
						}


					}
					pstmt.close();
					pstmt=null;
					rs.close();
					rs=null;

					/**
					 * tax_reco details from
					 * 'taxtran'
					 * */
					if (totRecoAmt != 0) // 07-dec-16 manoharan
					{
						lsTaxRecoCctr=checkNull(finCommon.getAcctDetrTtype(InvoiceDetMap.get("item_code"), itemSer, "TAXRECO", InvoiceHrdMap.get("inv_type"), conn));// Changes by Nandkumar Gadkari on 10/08/18
						//		        	detCctrCode=lsCctrCodeSal;

						String ls_tax_reco_acct[]=lsTaxRecoCctr.split(",");
						if( ls_tax_reco_acct.length > 0)
						{
							lsTaxRecoAcct=ls_tax_reco_acct[0];
						}
						if( ls_tax_reco_acct.length > 1)
						{
							lsTaxRecoCctr=ls_tax_reco_acct[1];
						}
						//		        	detAcctCode=lsAcctCodeSal;
					}

					lineNoTemp = "   "+InvoiceDetMap.get("line_no"); 
					lineNoTemp = lineNoTemp.substring(lineNoTemp.length()-3, lineNoTemp.length());
					//System.out.println("@@@@@@@ lineNoTemp["+lineNoTemp+"]");
					totRecoAmt = 0;
					sql="select acct_code__reco, cctr_code__reco, case when sum(reco_amount) is null then 0 else sum(reco_amount) end as reco_amount "
							+ " from taxtran"
							+ " where tran_code = 'S-INV'  and tran_id	 = ?"
							+ " and line_no = ? and   effect <> 'N'"
							+ " and (case when reco_amount is null then 0 else reco_amount end) <> 0"
							+ " group BY acct_code__reco, cctr_code__reco  ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, InvoiceDetMap.get("invoice_id"));
					pstmt.setString(2, lineNoTemp);
					rs=pstmt.executeQuery();
					while(rs.next())
					{
						lsAcctReco=checkNull(rs.getString("acct_code__reco")).trim();
						lsCctrReco=checkNull(rs.getString("cctr_code__reco"));
						lcRecoAmt=rs.getDouble("reco_amount");
						totRecoAmt += lcRecoAmt;
						if (lcRecoAmt != 0) // 07-dec-16 manoharan
						{
							if( lsAcctReco == null || lsAcctReco.trim().length() == 0 )
							{		
								lsAcctReco = lsTaxRecoAcct;
								lsCctrReco = lsTaxRecoCctr;
							}
							if( lsCctrReco == null || lsCctrReco.trim().length() == 0 )
							{ 
								lsCctrReco = " ";
							}

							invAcctMap = new HashMap<String, String>();

							invAcctMap.put("invoice_id", detInvId);
							//invAcctMap.put("line_no",detLineNo);
							invAcctMap.put("acct_code", lsAcctReco);
							if(lsAcctReco == null || lsAcctReco.trim().length() == 0) // Added by Mahesh Saggam on 29-07-2019 [account code should not be null]
							{
								retString = itmDBAccessEJB.getErrorString("", "VTRECACCT", "","", conn);
							//	return retString;
								//added by monika 13 dec 19 to check retstring null or not
								if(retString != null && retString.trim().length() > 0)
								{
									return retString;
								}//end
							}
							invAcctMap.put("cctr_code", lsCctrReco);
							invAcctMap.put("anal_code", hdrAnalCode);
							invAcctMap.put("amount", ""+lcRecoAmt);
							invAcctMap.put("analysis1", lsAnalysis1); 
							invAcctMap.put("analysis2", lsAnalysis2);
							invAcctMap.put("analysis3", lsAnalysis3);
							//System.out.println("@@@@@@@10 invAcctMap["+invAcctMap+"]");
							//invAcctList.add(invAcctMap);

							invAcctList = modifyinvAcctList(invAcctList,invAcctMap,lsAcctReco,lsCctrReco , lcRecoAmt, checkNullSpace(hdrAnalCode) );
						}

					}
					pstmt.close();
					pstmt=null;
					rs.close();
					rs=null;
				}//invoiceDetList FOR loop END
			}
			
			sql="select case when round_adj is null then 0 else round_adj end as round_adj from invoice where invoice_id=?";
			pstmt2=	conn.prepareStatement(sql);
			pstmt2.setString(1,invoiceId );
			rs2=pstmt2.executeQuery();
			
			//while(rs2.next())
			if(rs2.next())
			{
				ln_amount_radj=(rs2.getDouble("round_adj"));
				System.out.println("ln_amount_radj+++++++"+ln_amount_radj);
				
			}
			rs2.close();//added by Pavan R 10oct18[to handle open cursor issue]
			rs2 = null;	pstmt2.close();	pstmt2 = null;
			ls_acct_code__radj =checkNull(finCommon.getFinparams("999999","ROUND_ADJUST_ACCT", conn));
			ls_cctr_code__radj = checkNull(finCommon.getFinparams("999999","ROUND_ADJUST_CCTR", conn));
			
			if( "NULLFOUND".equalsIgnoreCase(ls_acct_code__radj ))
			{
				ls_acct_code__radj=null;
			}
			else
			{
				ls_acct_code__radj=ls_acct_code__radj.trim();
			}
			if( "NULLFOUND".equalsIgnoreCase(ls_cctr_code__radj ))
			{
				ls_cctr_code__radj=null;
			}
			else
			{
				ls_cctr_code__radj=ls_cctr_code__radj;//.trim(); //Pavan R 09apr19[to allow space in cctr]
			}
			
			System.out.println("ls_acct_code__radj["+ls_acct_code__radj+"]");
			System.out.println("ls_cctr_code__radj["+ls_cctr_code__radj+"]");
				
				if (ln_amount_radj != 0) // 07-dec-16 manoharan
				{ 
					
					System.out.println("Inside IF insert"+ln_amount_radj);

					invAcctMap = new HashMap<String, String>();

					invAcctMap.put("invoice_id", detInvId);
					//invAcctMap.put("line_no",detLineNo);
					//invAcctMap.put("amount", ""+lnAmountTax);
					invAcctMap.put("acct_code", ls_acct_code__radj); 
					if(ls_acct_code__radj == null || ls_acct_code__radj.trim().length() == 0) // Added by Mahesh Saggam on 29-07-2019 [account code should not be null]
					{
						retString = itmDBAccessEJB.getErrorString("", "VTRADJACCT", "","", conn);
					//	return retString;
						//added by monika 13 dec 19 to check retstring null or not
						if(retString != null && retString.trim().length() > 0)
						{
							return retString;
						}//end
					}
					invAcctMap.put("cctr_code", ls_cctr_code__radj);
					invAcctMap.put("amount",""+ ln_amount_radj);
					invAcctMap.put("analysis1", lsAnalysis1); 
					invAcctMap.put("analysis2", lsAnalysis2);
					invAcctMap.put("analysis3", lsAnalysis3);
				//	System.out.println("@@@@@@@9 invAcctMap["+invAcctMap+"]");
					//invAcctList.add(invAcctMap);

					invAcctList = modifyinvAcctList(invAcctList,invAcctMap,ls_acct_code__radj,ls_cctr_code__radj , ln_amount_radj, checkNullSpace(hdrAnalCode) );
					System.out.println("After Modify"+ln_amount_radj);
				}
				
				System.out.println("Round ArrayList["+invAcctList+"]");
				//pstmt2.close();
				//pstmt2=null;
				//rs2.close();
				//rs2=null;
			

			retString = insertInvAcctList( invAcctList,conn );
			//System.out.println("@@@@@1032 retString["+retString+"]");
			//added by monika 13 dec 19 to check retstring null or not
			if(retString != null && retString.trim().length() > 0)
			{
				return retString;
			}//end
			//calling poonam code here start 13/06/16 cpatil

			boolean lb_comm= false;
			String commCalcOnOff="",confDrcrOpt="",confJvOpt="",gs_run_mode="";


			if( retString == null || retString.trim().length() == 0 )
			{		
				if( gs_run_mode == null  || !"B".equalsIgnoreCase(gs_run_mode))
				{		

					//ls_comm_calc_on_off = trim(gf_getfinparm('999999','COMM_CALC_ON_OFF'))
					commCalcOnOff = finCommon.getFinparams("999999", "COMM_CALC_ON_OFF", conn);
					//System.out.println("@@@@@1046 commCalcOnOff["+commCalcOnOff+"]");

					if("Y".equalsIgnoreCase(commCalcOnOff))					
					{
						lb_comm = true; 

						if( lb_comm == true )
						{	
							lb_comm = false;
							//nvo_commission_calculation lnvo_commission
							//lnvo_commission = create nvo_commission_calculation
							//ls_conf_drcr_opt = trim(gf_getfinparm('999999','COMM_DRCR_CONF'))
							//ls_conf_jv_opt   = trim(gf_getfinparm('999999','COMM_JV_CONF'))

							confDrcrOpt = finCommon.getFinparams("999999", "COMM_DRCR_CONF", conn);
							confJvOpt = finCommon.getFinparams("999999", "COMM_JV_CONF", conn);
							//System.out.println("@@@@@1061 confDrcrOpt["+confDrcrOpt+"]confJvOpt["+confJvOpt+"]");

							CalculateCommission calculateCommission = new CalculateCommission(); 
							//ls_errcode = lnvo_commission.gbf_calc_commission(ls_invoice_id[ll_cntr],'I','',ls_conf_drcr_opt,ls_conf_jv_opt)
							retString = calculateCommission.CalCommission(invoiceId, "I", "", confDrcrOpt, confJvOpt, xtraParams, conn);
							//System.out.println("@@@@@1065 calculateCommission retString["+retString+"]");
							//added by monika 13 dec 19 to check retstring null or not
							if(retString != null && retString.trim().length() > 0)
							{
								return retString;
							}//end
						}//end if
					}

				}
			}
			// end cpatil

			//System.out.println("@@@@@1073 retString["+retString+"]");

			if( retString == null || retString.trim().length() == 0)
			{
				/**
				 * Select from
				 * Invoice acct
				 * */
				invoiceAcctList=getInvoiceAcct(invoiceId,conn);
			//	System.out.println("Invoice Acct list size:- ["+invoiceAcctList.size()+"]");

				/**
				 * Post Invoice
				 * */
				retString=gbfPostInvoice(InvoiceHrdMap,invoiceAcctList,conn);
			//	System.out.println("Invoice post return String :- ["+retString+"]");
				
				
				/**
				 * Local connection commit 
				 * */
			}
			if(connFlag)
			{
				conn.commit();
			}
			endTime = System.currentTimeMillis();
			totalTime = endTime - startTime;

			totSecs = (int) (((double) 1 / 1000) * (totalTime));
			totalHrs = (int) (totSecs / 3600);
			totlMts = (int) (((totSecs - (totalHrs * 3600)) / 60));
			totSecs = (int) (totSecs - ((totalHrs * 3600) + (totlMts * 60)));

			System.out.println("Total Time Spend invoice posting[" + totalHrs + "] Hours [" + totlMts + "] Minutes [" + totSecs + "] seconds");
			

		} catch (ITMException e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage()); 
			throw new ITMException(e); 
		} catch (SQLException e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage()); 
			throw new ITMException(e); 
		} catch (RemoteException e)
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

		return retString;
	}

	private String gbfPostDrcradj(String invoiceId, String lineNoRef, Connection conn) throws Exception
    {
		PreparedStatement pstmt = null,pstmt1=null,pstmt2=null;
		ResultSet rs = null,rs1=null,rs2=null;
		String retString="",ls_tran_id__rcv="",sqlStr2="";
		String sql="",sqlStr="",ls_linenoref="",ls_contactcode="";
		String ls_tranid="",ls_refser="",ls_refno="",ls_refseradj="",ls_refnoadj="",ls_tranidrcv="";
		String ls_status="",ls_curr_code="";
		String ls_fin_entity="",ls_site_code="",ls_cust_code="",ls_acct_code="",ls_cctr_code="";
		double lc_exch_rate=0.0;
		double lc_totamt=0.0,lc_adjamt=0.0,lc_netamt=0.0,lc_recadj=0.0,lc_rectot=0.0,lc_lineadjamt=0.0;
		double lc_balamt=0.0;
		int rcvadjcnt=0,ll_refcount=0;
		Timestamp sysDate=null;
		HashMap sundryBalMap=null;
		Timestamp ld_trandate=null,ld_effdate=null;
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB(); // Added by Mahesh Saggam on 29-07-2019
		try
        {
			DistCommon distCommon = new DistCommon();
			FinCommon finCommon = new FinCommon();
			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String sysDateStr = sdf.format(currentDate.getTime());
			//System.out.println("Now the date is :=>  " + sysDateStr);
			sysDate = Timestamp.valueOf(genericUtility.getValidDateString(sysDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			
			sql ="select tran_id  from receivables " +
					"where tran_ser = 'S-INV' and ref_no = ? and line_no__ref = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, invoiceId);
			pstmt.setString(2, lineNoRef);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				ls_tran_id__rcv = checkNull(rs.getString("tran_id"));
			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt = null;
			if(ls_tran_id__rcv.trim().length()<=0)
			{
				retString="DS000";
				return retString;
			}
			sql="update receivables_adj  set tran_id__rcv = ?  WHERE ref_ser = 'S-INV' AND" +
					" ref_no = ? AND	tran_id__rcv = ' '";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1,ls_tran_id__rcv);
			pstmt.setString(2,invoiceId);
			pstmt.executeUpdate();
			pstmt.close();
			pstmt=null;
			
			
			sql ="SELECT receivables_adj.tran_id,receivables_adj.ref_ser,receivables_adj.ref_no, " +
					"receivables_adj.tot_amt,receivables_adj.adj_amt,receivables_adj.ref_ser_adj, " +
					"receivables_adj.ref_no_adj,receivables_adj.net_amt,receivables_adj.tran_id__rcv " +
					"FROM receivables_adj  WHERE ((receivables_adj.ref_ser = 'S-INV' " +
					"AND receivables_adj.ref_no = ?) OR (receivables_adj.ref_ser_adj = 'S-INV' " +
					" AND  receivables_adj.ref_no_adj = ?))";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, invoiceId);
			pstmt.setString(2, invoiceId);
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				rcvadjcnt++;
				ls_tranid = checkNull(rs.getString("tran_id"));
				ls_refser = checkNull(rs.getString("ref_ser"));
				ls_refno = checkNull(rs.getString("ref_no"));
				lc_totamt = rs.getDouble("tot_amt");
				lc_adjamt = rs.getDouble("adj_amt");
				ls_refseradj = checkNull(rs.getString("ref_ser_adj"));
				ls_refnoadj = checkNull(rs.getString("ref_no_adj"));
				lc_netamt = rs.getDouble("net_amt");
				ls_tranidrcv = checkNull(rs.getString("tran_id__rcv"));
				
				//Added by wasim on 09-JUN-2017 to trim the refSer
				ls_refser = ls_refser.trim();
				
				sqlStr ="select tot_amt, adj_amt from receivables	where tran_ser = ? " +
						" and ref_no = ? and line_no__ref = ?  ";
				pstmt1 = conn.prepareStatement(sqlStr);
				pstmt1.setString(1, ls_refser);
				pstmt1.setString(2, ls_refno);
				pstmt1.setString(3, lineNoRef);
				rs1 = pstmt1.executeQuery();
				if(rs1.next())
				{
					lc_rectot = rs1.getDouble("tot_amt");
					lc_recadj = rs1.getDouble("adj_amt");
				}
				rs1.close();
				rs1=null;
				pstmt1.close();
				pstmt1 = null;
				
				ll_refcount = 1;
				if("S-INV".equalsIgnoreCase(ls_refser))
				{
					sqlStr ="select count(1)  as ll_refcount from receivables " +
							" where tran_ser = ? and ref_no = ? and line_no__ref = ? ";
					pstmt1 = conn.prepareStatement(sqlStr);
					pstmt1.setString(1, ls_refser);
					pstmt1.setString(2, ls_refno);
					pstmt1.setString(3, lineNoRef);
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						ll_refcount = rs1.getInt("ll_refcount");
					}
					rs1.close();
					rs1=null;
					pstmt1.close();
					pstmt1 = null;
				}
				if("S-INV".equalsIgnoreCase(ls_refser) && lc_adjamt>0 &&  ll_refcount > 1)
				{
					lc_balamt=lc_adjamt;
					sqlStr ="select tot_amt -  adj_amt as amt, line_no__ref from receivables " +
							" where tran_ser = ?	and ref_no = ? " +
							"	and tot_amt -  adj_amt > 0 ";
					pstmt1 = conn.prepareStatement(sqlStr);
					pstmt1.setString(1, ls_refser);
					pstmt1.setString(2, ls_refno);
					rs1 = pstmt1.executeQuery();
					while(rs1.next())
					{
						lc_lineadjamt = rs1.getDouble("amt");
						ls_linenoref = checkNull(rs1.getString("line_no__ref"));
						
						sqlStr2 ="select tot_amt, adj_amt from receivables" +
								" where tran_ser = ? and ref_no = ? and line_no__ref = ? ";
						pstmt2 = conn.prepareStatement(sqlStr2);
						pstmt2.setString(1, ls_refser);
						pstmt2.setString(2, ls_refno);
						pstmt2.setString(3, ls_linenoref);
						rs2 = pstmt2.executeQuery();
						if(rs2.next())
						{
							lc_rectot = rs2.getDouble("tot_amt");
							lc_recadj = rs2.getDouble("adj_amt");
						}
						rs2.close();
						rs2=null;
						pstmt2.close();
						pstmt2 = null;
						
						if(lc_balamt < lc_lineadjamt)
						{
							lc_lineadjamt = lc_balamt;
						}
						lc_balamt = lc_balamt - lc_lineadjamt;
						if(lc_rectot == (lc_recadj + lc_lineadjamt))
						{
							ls_status = "A";
						}else
						{
							ls_status = "P";
						}
						//Added by Varsha V on 26-10-2020 Receivables trace update during receivables creation and update[START]
						double totAmtRcv = 0, adjAmtRcv = 0;
						HashMap receivablesMap = null;
						sql = "select tot_amt, adj_amt from receivables "
								+ "where TRAN_SER = ? AND REF_NO = ?";
						pstmt2 = conn.prepareStatement(sql);
						
						pstmt2.setString(1,ls_refser);
						pstmt2.setString(2,ls_refno);
						rs2 = pstmt2.executeQuery();
						
						if(rs2.next())
						{
							totAmtRcv = rs2.getDouble("tot_amt");
							adjAmtRcv = rs2.getDouble("adj_amt");
						}
						rs2.close();
						rs2 = null;
						pstmt2.close();
						pstmt2= null;
						
						receivablesMap = new HashMap();
						receivablesMap.put("tran_ser",ls_refser);
						receivablesMap.put("ref_no",ls_refno);
						receivablesMap.put("line_no__ref",ls_linenoref);
						receivablesMap.put("tot_amt",totAmtRcv);
						receivablesMap.put("adj_amt",adjAmtRcv);
						//Added by Varsha V on 26-10-2020 Receivables trace update during receivables creation and update[END]
							
						/*commented by Varsha V on 27-10-2020 as code added in fincommon.ReceivablesUpdate
						 * sql="update receivables	set adj_amt = adj_amt + ? , status = ?," +
						 * " stat_date = ?	where tran_ser = ? " +
						 * "and ref_no = ? and line_no__ref = ? "; pstmt2=conn.prepareStatement(sql);
						 * pstmt2.setDouble(1,lc_lineadjamt); pstmt2.setString(2,ls_status);
						 * pstmt2.setTimestamp(3, sysDate); pstmt2.setString(4,ls_refser);
						 * pstmt2.setString(5,ls_refno); pstmt2.setString(6,ls_linenoref);
						 * pstmt2.executeUpdate(); pstmt2.close(); pstmt2=null; if(lc_balamt <=0) {
						 * return retString; }
						 */
						//Added by Varsha V on 26-10-2020 Receivables trace update during receivables creation and update[START]
						adjAmtRcv = adjAmtRcv + lc_lineadjamt;
						receivablesMap.put("amt_after", adjAmtRcv);
						receivablesMap.put("status", ls_status);
						retString = finCommon.ReceivablesUpdate(receivablesMap, conn);
						if(retString != null && retString.trim().length() > 0)
						{
							return retString;
						}
						//Added by Varsha V on 26-10-2020 Receivables trace update during receivables creation and update[END]
						
					}
					rs1.close();
					rs1=null;
					pstmt1.close();
					pstmt1 = null;
					if(lc_balamt > 0)
					{ 
						retString="VTBALAMT";
						return retString;
					}
					if(retString.trim().length()>0)
					{
						return retString;
					}
				}else
				{
					if("1".equalsIgnoreCase(checkNull(lineNoRef).trim()))
					{
						if(lc_rectot == (lc_adjamt + lc_recadj))
						{
							ls_status = "A";
						}else
						{
							ls_status = "P";
						}
						
						//Added by Varsha V on 26-10-2020 Receivables trace update during receivables creation and update[START]
						double totAmtRcv = 0, adjAmtRcv = 0;
						HashMap receivablesMap = null;
						sql = "select tot_amt, adj_amt from receivables "
								+ "where TRAN_SER = ? AND REF_NO = ?";
						pstmt2 = conn.prepareStatement(sql);
						
						pstmt2.setString(1,ls_refser);
						pstmt2.setString(2,ls_refno);
						rs2 = pstmt2.executeQuery();
						
						if(rs2.next())
						{
							totAmtRcv = rs2.getDouble("tot_amt");
							adjAmtRcv = rs2.getDouble("adj_amt");
						}
						rs2.close();
						rs2 = null;
						pstmt2.close();
						pstmt2= null;
						
						receivablesMap = new HashMap();
						receivablesMap.put("tran_ser",ls_refser);
						receivablesMap.put("ref_no",ls_refno);
						receivablesMap.put("line_no__ref",ls_linenoref);
						receivablesMap.put("tot_amt",totAmtRcv);
						receivablesMap.put("adj_amt",adjAmtRcv);
						//Added by Varsha V on 26-10-2020 Receivables trace update during receivables creation and update[END]
						
						/*commented by Varsha V on 27-10-2020 as code added in fincommon.ReceivablesUpdate
						 * sql="update receivables set adj_amt = adj_amt + ?,status = ?," +
						 * "stat_date = ? where tran_ser = ? and 	ref_no = ? and " +
						 * " line_no__ref = ? "; pstmt2=conn.prepareStatement(sql);
						 * pstmt2.setDouble(1,lc_adjamt); pstmt2.setString(2,ls_status);
						 * pstmt2.setTimestamp(3, sysDate); pstmt2.setString(4,ls_refser);
						 * pstmt2.setString(5,ls_refno); pstmt2.setString(6,lineNoRef);
						 * pstmt2.executeUpdate(); pstmt2.close(); pstmt2=null;
						 */
						//Added by Varsha V on 26-10-2020 Receivables trace update during receivables creation and update[START]
						adjAmtRcv = adjAmtRcv + lc_adjamt;
						receivablesMap.put("amt_after", adjAmtRcv);
						receivablesMap.put("status", ls_status);
						retString = finCommon.ReceivablesUpdate(receivablesMap, conn);
						if(retString != null && retString.trim().length() > 0)
						{
							return retString;
						}
						//Added by Varsha V on 26-10-2020 Receivables trace update during receivables creation and update[END]
						
					}
						
				}
				
				if("R-ADV".equalsIgnoreCase(ls_refser))
				{
					int radvCnt=0;
					sqlStr ="select fin_entity, site_code, cust_code, acct_code, cctr_code,curr_code, exch_rate " +
							" from receivables where tran_ser = ? and ref_no = ? and line_no__ref = '1'";
					pstmt1 = conn.prepareStatement(sqlStr);
					pstmt1.setString(1, ls_refser);
					pstmt1.setString(2, ls_refno);
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						radvCnt++;
						ls_fin_entity = rs1.getString("fin_entity");
						ls_site_code = rs1.getString("site_code");
						ls_cust_code = rs1.getString("cust_code");
						ls_acct_code = rs1.getString("acct_code");
						ls_cctr_code = rs1.getString("cctr_code");
						ls_curr_code = rs1.getString("curr_code");
						lc_exch_rate = rs1.getDouble("exch_rate");
					}
					rs1.close();
					rs1=null;
					pstmt1.close();
					pstmt1 = null;
					if(radvCnt==0)
					{
						retString="DS000";
						return retString;
					}
					sqlStr ="select tran_date,eff_date from invoice " +
							" where invoice_id = ? ";
					pstmt1 = conn.prepareStatement(sqlStr);
					pstmt1.setString(1, invoiceId);
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						ld_trandate = rs1.getTimestamp("tran_date");
						ld_effdate = rs1.getTimestamp("eff_date");
					}
					rs1.close();
					rs1=null;
					pstmt1.close();
					pstmt1 = null;
					
					sundryBalMap = new HashMap();
					sundryBalMap.put("tran_date",ld_trandate);
					sundryBalMap.put("eff_date",ld_effdate);
					sundryBalMap.put("fin_entity", ls_fin_entity);
					sundryBalMap.put("site_code", ls_site_code);
					sundryBalMap.put("sundry_type", "C");
					sundryBalMap.put("sundry_code", ls_cust_code);
					sundryBalMap.put("acct_code", ls_acct_code);
					if(ls_acct_code == null || ls_acct_code.trim().length() == 0) // Added by Mahesh Saggam on 29-07-2019 [account code should not be null]
					{
						retString = itmDBAccessEJB.getErrorString("", "VTACCT", "","", conn);
						return retString;
					}
					sundryBalMap.put("cctr_code", ls_cctr_code);
					sundryBalMap.put("curr_code", ls_curr_code);
					sundryBalMap.put("exch_rate", lc_exch_rate);
					sundryBalMap.put("dr_amt", Double.parseDouble("0"));
					sundryBalMap.put("cr_amt", Double.parseDouble("0"));
					sundryBalMap.put("adv_amt", lc_adjamt);
					ls_contactcode = finCommon.getContactCode("C", ls_cust_code, conn)	;
					sundryBalMap.put("contact_code", ls_contactcode);

				//	System.out.println("@@@@@@@@@@@ ls_refser R-ADV sundryBalMap["+sundryBalMap+"]");
					/**
					 * sundry_bal update
					 * */
					retString = finCommon.gbf_sundrybal_upd(sundryBalMap,conn);
					//added by monika 13 dec 19 to check retstring null or not
					if(retString != null && retString.trim().length() > 0)
					{
						return retString;
					}//end
				}
			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt = null;
			/*if(rcvadjcnt==0)
			{
				retString="DS000";
				return retString;
			}
	        */
        } catch (SQLException e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage()); 
			throw new ITMException(e); 
		}
		
	    // TODO Auto-generated method stub
	    return retString;
    }

	private String insertInvAcctList(ArrayList<HashMap<String, String>> invAcctList, Connection conn) throws ITMException 
	{
		int detLineNo=0;
		String sql="",errCode="";
		PreparedStatement pstmt=null;

		try
		{
			System.out.println("manoharan invAcctList ["+invAcctList+"]");
			// 28-nov-16 manoharan moved from below (inside loop
			sql="insert into invacct (invoice_id,line_no,acct_code,cctr_code,anal_code,amount,analysis1,analysis2,analysis3)"
					+ " values (?,?,?,?,?,?,?,?,?)";
			pstmt=conn.prepareStatement(sql);
			for( int i=0; i < invAcctList.size() ; i++ )
			{
				detLineNo++;

				HashMap<String, String> tempMap = new HashMap<String, String>();

				tempMap = invAcctList.get(i);

				//System.out.println("@@@@@@@@@ i["+i+"]tempMap["+tempMap+"]");

				if( tempMap != null)
				{
					pstmt.setString(1, tempMap.get("invoice_id"));
					pstmt.setInt(2, detLineNo);
					pstmt.setString(3, tempMap.get("acct_code"));
					pstmt.setString(4, tempMap.get("cctr_code"));
					pstmt.setString(5, tempMap.get("anal_code"));
					pstmt.setDouble(6, Double.parseDouble(tempMap.get("amount")==null?"0":tempMap.get("amount")));
					pstmt.setString(7, tempMap.get("analysis1"));
					pstmt.setString(8, tempMap.get("analysis2"));
					pstmt.setString(9, tempMap.get("analysis3"));
					//int insertCnt=pstmt.executeUpdate();
					//System.out.println("@@@@@@@@@ i["+i+"]insertCnt["+insertCnt+"]");
					//pstmt.close();
					//pstmt=null;
					pstmt.addBatch();
					pstmt.clearParameters();

				}
			}
			pstmt.executeBatch();
			pstmt.clearBatch();
			pstmt.close();
			pstmt = null;
			
		} catch (SQLException e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage()); 
			throw new ITMException(e); 
		}

		return errCode;
			}

	private ArrayList<HashMap<String, String>> modifyinvAcctList( ArrayList<HashMap<String, String>> invAcctList, HashMap<String, String> invAcctMap, String lsAcctCodeSal, String lsCctrCodeSal, double detAmount, String analCode) 
	{
		boolean findFlag=false;
		HashMap<String, String> tempMap = null;
		//System.out.println("@@@@@@@@1131 invAcctMap["+invAcctMap+"]");
		for( int i=0; i < invAcctList.size() ; i++ )
		{

			//HashMap<String, String> tempMap = new HashMap<String, String>();
			tempMap  = invAcctList.get(i);
			//System.out.println("@@@@@@@@1135 tempMap["+tempMap+"]");
			//if(tempMap.containsKey(lsAcctCodeSal+lsCctrCodeSal+analCode))
			if (lsAcctCodeSal.trim().equals(tempMap.get("acct_code").trim()) && lsCctrCodeSal.trim().equals(tempMap.get("cctr_code").trim()) && analCode.trim().equals(tempMap.get("anal_code").trim()) )
			{
				detAmount=detAmount + Double.parseDouble(tempMap.get("amount"));
				tempMap.put("amount", ""+detAmount);
				findFlag = true;	

				//invAcctList.add(tempMap);
				invAcctList.set(i,tempMap);
				break;
			}
		}

		if(!findFlag )
		{
			invAcctList.add(invAcctMap);
		}
		return invAcctList;
			}

	private int updateAcctAmt(String tempInvId, double amount, Connection conn) throws ITMException
	{
		int uActCnt=0;
		String sqlUpd="";
		PreparedStatement pstmtU=null;

		// TODO Auto-generated method stub
		try
		{
			sqlUpd="update invacct set amount=? where invoice_id=?";
			pstmtU=conn.prepareStatement(sqlUpd);
			pstmtU.setDouble(1, amount);
			pstmtU.setString(2, tempInvId);	        		
			uActCnt=pstmtU.executeUpdate();

			pstmtU.close();
			pstmtU=null;
		} catch (SQLException e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage()); 
			throw new ITMException(e); 
		}

		return uActCnt;
	}
	/*
	private int insertInvAcct(String detInvId, int detLineNo, String detAcctCode, String detCctrCode, String hdrAnalCode,
			double detAmount, String detAnalysis1, String detAnalysis2, String detAnalysis3, Connection conn) throws ITMException
			{
		// TODO Auto-generated method stub
		int insertCnt=0;
		String sql="";
		PreparedStatement pstmt=null;

		try
		{
			sql="insert into invacct (invoice_id,line_no,acct_code,cctr_code,anal_code,amount,analysis1,analysis2,analysis3)"
					+ " values (?,?,?,?,?,?,?,?,?)";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, detInvId);
			pstmt.setInt(2, detLineNo);
			pstmt.setString(3, detAcctCode);
			pstmt.setString(4, detCctrCode);
			pstmt.setString(5, hdrAnalCode);
			pstmt.setDouble(6, detAmount);
			pstmt.setString(7, detAnalysis1);
			pstmt.setString(8, detAnalysis2);
			pstmt.setString(9, detAnalysis3);
			insertCnt=pstmt.executeUpdate();

			pstmt.close();
			pstmt=null;

		} catch (SQLException e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage()); 
			throw new ITMException(e); 
		}

		return insertCnt;
	}
	*/
	private ArrayList<Object> getInvoiceDet(String invoiceId, Connection conn) throws ITMException
	{
		// TODO Auto-generated method stub
		ArrayList<Object> invoiceDetList=new ArrayList<Object>();
		HashMap<String, String>InvoiceDetMap=null;
		String sql="",chgDtStr="";
		PreparedStatement pstmt=null;
		ResultSet rs=null;

		try
		{
			sql="SELECT invdet.invoice_id,invdet.line_no,invdet.sord_line_no,invdet.item_code,invdet.item_flg,"
					+ "invdet.item_descr,invdet.quantity,invdet.unit,invdet.rate,invdet.unit__rate,invdet.discount,invdet.tax_class,"
					+ "invdet.tax_chap,invdet.tax_env,invdet.tax_amt,invdet.net_amt,invdet.unit__std,"
					+ "invdet.conv__qty_stduom,invdet.quantity__stduom,invdet.conv__rtuom_stduom,invdet.rate__stduom,invdet.chg_date,"
					+ "invdet.chg_user,invdet.chg_term,invdet.comm_amt,invdet.sord_no,invdet.no_art,"
					+ "invdet.disc_amt,invdet.item_code__ord,invdet.rate__clg,invdet.comm_amt__oc,invdet.analysis1,"
					+ "invdet.analysis2,invdet.analysis3,'                    ' custitem_desc,"
					+ "invdet.disc_schem_billback_amt,invdet.disc_schem_offinv_amt"
					+ " FROM invdet"
					+ " WHERE invdet.invoice_id =?"
					+ " ORDER BY invdet.invoice_id ASC,invdet.line_no ASC,invdet.sord_line_no ASC";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, invoiceId);
			rs=pstmt.executeQuery();
			while(rs.next())
			{
				InvoiceDetMap=new HashMap<String, String>();
				InvoiceDetMap.put("invoice_id", checkNull(rs.getString("invoice_id")));
				InvoiceDetMap.put("line_no", checkNull(rs.getString("line_no")));
				InvoiceDetMap.put("sord_line_no", checkNull(rs.getString("sord_line_no")));
				InvoiceDetMap.put("item_code", checkNull(rs.getString("item_code")));
				InvoiceDetMap.put("item_flg", checkNull(rs.getString("item_flg")));
				InvoiceDetMap.put("item_descr", checkNull(rs.getString("item_descr")));
				InvoiceDetMap.put("quantity", checkNull(rs.getString("quantity")==null?"0.0":rs.getString("quantity")));
				InvoiceDetMap.put("unit", checkNull(rs.getString("unit")));
				InvoiceDetMap.put("unit__rate", checkNull(rs.getString("unit__rate")));
				InvoiceDetMap.put("discount", checkNull(rs.getString("discount")));
				InvoiceDetMap.put("tax_class", checkNull(rs.getString("tax_class")));
				InvoiceDetMap.put("tax_chap", checkNull(rs.getString("tax_chap")));
				InvoiceDetMap.put("tax_env", checkNull(rs.getString("tax_env")));
				InvoiceDetMap.put("tax_amt", checkNull(rs.getString("tax_amt")));
				InvoiceDetMap.put("net_amt", checkNull(rs.getString("net_amt")));
				InvoiceDetMap.put("unit__std", checkNull(rs.getString("unit__std")));
				InvoiceDetMap.put("conv__qty_stduom", checkNull(rs.getString("conv__qty_stduom")==null?"0.0":rs.getString("conv__qty_stduom")));
				InvoiceDetMap.put("quantity__stduom", checkNull(rs.getString("quantity__stduom")==null?"0.0":rs.getString("quantity__stduom")));
				InvoiceDetMap.put("conv__rtuom_stduom", checkNull(rs.getString("conv__rtuom_stduom")));
				InvoiceDetMap.put("rate__stduom", checkNull(rs.getString("rate__stduom")==null?"0.0":rs.getString("rate__stduom")));

				chgDtStr=genericUtility.getValidDateString(rs.getString("chg_date"), genericUtility.getDBDateFormat(),
						genericUtility.getApplDateFormat());
				InvoiceDetMap.put("chg_date", chgDtStr);

				InvoiceDetMap.put("chg_user", checkNull(rs.getString("chg_user")));
				InvoiceDetMap.put("chg_term", checkNull(rs.getString("chg_term")));
				InvoiceDetMap.put("comm_amt", checkNull(rs.getString("comm_amt")));
				InvoiceDetMap.put("sord_no", checkNull(rs.getString("sord_no")));
				InvoiceDetMap.put("no_art", checkNull(rs.getString("no_art")==null?"0":rs.getString("no_art")));
				InvoiceDetMap.put("disc_amt", checkNull(rs.getString("disc_amt")==null?"0.0":rs.getString("disc_amt")));
				InvoiceDetMap.put("item_code__ord", checkNull(rs.getString("item_code__ord")));
				InvoiceDetMap.put("rate__clg", checkNull(rs.getString("rate__clg")));
				InvoiceDetMap.put("comm_amt__oc", checkNull(rs.getString("comm_amt__oc")==null?"0.0":rs.getString("comm_amt__oc")));
				InvoiceDetMap.put("analysis1", checkNull(rs.getString("analysis1")));
				InvoiceDetMap.put("analysis2", checkNull(rs.getString("analysis2")));
				InvoiceDetMap.put("analysis3", checkNull(rs.getString("analysis3")));
				InvoiceDetMap.put("disc_schem_billback_amt", checkNull(rs.getString("disc_schem_billback_amt")==null?"0.0":rs.getString("disc_schem_billback_amt")));
				InvoiceDetMap.put("disc_schem_offinv_amt", checkNull(rs.getString("disc_schem_offinv_amt")==null?"0.0":rs.getString("disc_schem_offinv_amt")));

				invoiceDetList.add(InvoiceDetMap);
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

		return invoiceDetList;
	}

	private String gbfPostInvoice(HashMap<String, String> InvoiceHrdMap, ArrayList<Object> invoiceAcctList,Connection conn) throws Exception
	{
		String retString="";
		// TODO Auto-generated method stub
		String sql="",chgDtStr="";
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		double drAmt = 0, crAmt = 0,netAmtDet = 0,netAmtHdr = 0;
		/**
		 * Posting Invoice header
		 * */
		try
		{
			retString=gbfPostInvoiceHdr(InvoiceHrdMap,conn);
			//System.out.println("@@@@@@@@997 retString gbfPostInvoiceHdr["+retString+"]");
			if( retString == null || retString.trim().length() == 0)
			{
				retString=gbfPostInvoiceDet(InvoiceHrdMap,invoiceAcctList,conn);
			}
			//added by monika-12 dec-19-to  return retstring 
			if(retString != null && retString.trim().length() > 0)
			{
				return retString;
			}//end.
			
			//System.out.println("@@@@@@@@1002 retString gbfPostInvoiceDet["+retString+"]");
			// 28-nov-16 manoharan 
			sql = "select sum(case when dr_amt is null then 0 else dr_amt end  * exch_rate) as dr_amt,sum(case when cr_amt is null then 0 else cr_amt end  * exch_rate) as cr_amt from gltrace where ref_ser = 'S-INV' and ref_id = ? ";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, InvoiceHrdMap.get("invoice_id"));
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				drAmt =rs.getDouble("dr_amt");
				crAmt =rs.getDouble("cr_amt");
			}
			pstmt.close();
			pstmt=null;
			rs.close();
			rs=null;
			sql = "select sum(net_amt) as net_amt from invoice_trace where invoice_id = ? ";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, InvoiceHrdMap.get("invoice_id"));
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				netAmtDet =rs.getDouble("net_amt");
			}
			pstmt.close();
			pstmt=null;
			rs.close();
			rs=null;
			sql = "select net_amt as net_amt from invoice where invoice_id = ? ";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, InvoiceHrdMap.get("invoice_id"));
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				netAmtHdr =rs.getDouble("net_amt");
			}
			pstmt.close();
			pstmt=null;
			rs.close();
			rs=null;
	
			System.out.println("Invoice # [" + InvoiceHrdMap.get("invoice_id") + "] Debit [" + drAmt + "] Credit [" + crAmt + "] netAmtHdr [" + netAmtHdr + "] netAmtDet ["+ netAmtDet+ "]" ); 
			if( retString == null || retString.trim().length() == 0)
			{
				retString = finCommon.checkGlTranDrCr("S-INV",InvoiceHrdMap.get("invoice_id"),conn);
			}
			
			//Changed by wasim on 07-JUN-2017 for creating IBCA tranaction (Migration) [START]
			if(retString != null && retString.trim().length() > 0)
			{
				return retString;
			}
			retString = createRIBCA( InvoiceHrdMap, invoiceAcctList, conn);
			System.out.println("After createRIBCA retString ["+retString+"]");
			//Changed by wasim on 07-JUN-2017 for creating IBCA tranaction (Migration) [END]
			//added by monika 13 dec 19 to check retstring null or not
			if(retString != null && retString.trim().length() > 0)
			{
				return retString;
			}//end
		}
		catch(Exception e)
		{
			System.out.println("Expcetion inside gbfPostInoice ==>"+e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(rs!=null)
				{
					rs.close();rs= null;
				}
				if(pstmt!=null)
				{
					pstmt.close();pstmt= null;
				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
		
		return retString;
	}
	
	//Changed by wasim on 07-JUN-2017 for creating IBCA tranaction (Migration) [START]
	public String createRIBCA(HashMap<String, String> hdrMap, ArrayList<Object> invoiceAcctList, Connection conn) throws ITMException
	{
		String retString = "";
		String sql = "";
		PreparedStatement pstmt = null,pstmtHdr = null,pstmtDet = null;
		ResultSet rs = null;
		String ediOption = "",invIbcaGen = "",custCode = "",siteCode = "",siteCodeRcv = "",keyStr = "",finEntityFrom = "",finEntityTo = "",
			   linkType = "",cctrCodePay = "",acctCodeRcp = "",cctrCodeRcp = "",acctCodePay = "",ibcaID = "",remarks = "",invoiceID = "";
		
		Timestamp refDate = null,dueDate = null,custRefDate = null,gpDate = null;
		String currCode = "",acctCode = "",cctrCode = "",autoRcp = "",bankCode = "",recd = "",refType = "",tranSer = "",tranType = "",
				crTerm = "",itemSer = "",salesPers = "",salesPers1 = "",salesPers2 = "",custRefNo = "",gpNo = "",chgUser = "",chgTerm = "";
		double exchRate = 0,discount = 0,taxAmount = 0,custRefAmount = 0,amountBc = 0,amount = 0;
		String gs_run_mode = "";
		Timestamp tranDate = null,discountDate = null;
		
		try
		{
			gs_run_mode = genericUtility.getValueFromXTRA_PARAMS(xtraParamsStr, "runMode"); 
			chgUser = genericUtility.getValueFromXTRA_PARAMS( xtraParamsStr, "loginCode" );
			chgTerm = genericUtility.getValueFromXTRA_PARAMS( xtraParamsStr, "termId" );
			
			invoiceID = (String) hdrMap.get("invoice_id");
			
			System.out.println("Inside createRIBCA----->["+invoiceID+"]");
			
			tranDate = new java.sql.Timestamp(System.currentTimeMillis());
			SimpleDateFormat sdfAppl = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String currDateStr = sdfAppl.format(tranDate);
			
			System.out.println("Run mode--->["+gs_run_mode+"]");
			
			if("I".equals(gs_run_mode))
			{
				invIbcaGen = finCommon.getFinparams("999999","INV_IBCA_GEN",conn);
				
				System.out.println("createRIBCA--->INV_IBCA_GEN["+invIbcaGen+"]");
				
				if("NULLFOUND".equals(invIbcaGen))
				{
					//invIbcaGen = "Y";
					invIbcaGen = "N";
				}
				
				if("Y".equals(invIbcaGen))
				{
					custCode = (String) hdrMap.get("cust_code__bil");
					siteCode = (String) hdrMap.get("site_code");
					
					sql = " select site_customer.site_code__rcp from site_customer "
						+ " where ( site_customer.site_code = ? ) and ( site_customer.cust_code = ? ) ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,siteCode);
					pstmt.setString(2,custCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						siteCodeRcv = rs.getString("site_code__rcp");
					}
					if(pstmt != null)
					{
						pstmt.close();pstmt = null;
					}
					if(rs != null)
					{
						rs.close();rs = null;
					}
					
					if(siteCodeRcv == null || siteCodeRcv.trim().length() == 0)
					{
						sql = " select site_code__rcp from customer where cust_code = ?  ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,custCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							siteCodeRcv = rs.getString("site_code__rcp");
						}
						if(pstmt != null)
						{
							pstmt.close();pstmt = null;
						}
						if(rs != null)
						{
							rs.close();rs = null;
						}
					}
					
					if(siteCodeRcv == null || siteCodeRcv.trim().length() == 0)
					{
						siteCodeRcv = siteCode ;
					}
					
					if(!siteCodeRcv.equals(siteCode) && !"I".equals(siteCodeRcv))
					{	
						sql = " select key_string from transetup where upper(tran_window) = ?  ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,"W_RCP_IBCA");
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							keyStr = rs.getString("key_string");
						}
						if(pstmt != null)
						{
							pstmt.close();pstmt = null;
						}
						if(rs != null)
						{
							rs.close();rs = null;
						}
						
						sql = " select fin_entity from site where site_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,siteCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							finEntityFrom = rs.getString("fin_entity");
						}
						if(pstmt != null)
						{
							pstmt.close();pstmt = null;
						}
						if(rs != null)
						{
							rs.close();rs = null;
						}
						
						sql = " select fin_entity from site where site_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,siteCodeRcv);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							finEntityTo = rs.getString("fin_entity");
						}
						if(pstmt != null)
						{
							pstmt.close();pstmt = null;
						}
						if(rs != null)
						{
							rs.close();rs = null;
						}
						
						sql = " select link_type, acct_code__pay, cctr_code__pay, acct_code__rcp, cctr_code__rcp "
							 +" from ibca_rcp_ctrl where site_code__from = ? and site_code__to = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,siteCode);
						pstmt.setString(2,siteCodeRcv);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							linkType = rs.getString("link_type");
							acctCodePay = rs.getString("acct_code__pay");
							cctrCodePay = rs.getString("cctr_code__pay");
							acctCodeRcp = rs.getString("acct_code__rcp");
							cctrCodeRcp = rs.getString("cctr_code__rcp");
						}
						if(pstmt != null)
						{
							pstmt.close();pstmt = null;
						}
						if(rs != null)
						{
							rs.close();rs = null;
						}
					
						String xmlValues = "";
						xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
						xmlValues = xmlValues + "<Header></Header>";
						xmlValues = xmlValues + "<Detail1>";
						xmlValues = xmlValues +	"<tran_id/>";
						xmlValues = xmlValues + "<site_code__from>" + siteCode + "</site_code__from>";
						xmlValues = xmlValues + "<site_code__to>" + siteCodeRcv + "</site_code__to>";
						xmlValues = xmlValues + "<tran_date>"+ currDateStr + "</tran_date>";
						xmlValues = xmlValues + "<link_type>"+linkType+"</link_type>";
						xmlValues = xmlValues + "</Detail1></Root>";
						TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", CommonConstants.DB_NAME);
						ibcaID = tg.generateTranSeqID("R-IBCA", "tran_id", keyStr, conn);

						if("ERROR".equalsIgnoreCase(ibcaID))
						{
							ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
							retString = itmDBAccessLocal.getErrorString("", "VTTRANID", "", "",conn);
							return retString;
						}
						
						remarks = " Auto IBCA transfer of invoice of " + custCode;

						sql = " select ref_date, curr_code, exch_rate, cust_code, acct_code, "
						     +" cctr_code, due_date, (tot_amt - adj_amt) as amt , discount, tax_amt, auto_rcp, "
						     +" bank_code, recd, ref_type, cr_term, item_ser, sales_pers, "
						     +" sales_pers__1, sales_pers__2, tran_date,cust_ref_no,cust_ref_date,cust_ref_amt,gp_no,gp_date,discount_dt "
						     +" from receivables where tran_ser = 'S-INV' and  ref_no = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,invoiceID);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								System.out.println("Inside createRIBCA----->rs.Next()");
								
								refDate = rs.getTimestamp("ref_date");
								currCode = rs.getString("curr_code");
								exchRate = rs.getDouble("exch_rate");
								custCode = rs.getString("cust_code");
								acctCode = rs.getString("acct_code");
								cctrCode = rs.getString("cctr_code");
								dueDate = rs.getTimestamp("due_date");
								amount = rs.getDouble("amt");
								discount = rs.getDouble("discount");
								taxAmount = rs.getDouble("tax_amt");
								autoRcp = rs.getString("auto_rcp");
								bankCode = rs.getString("bank_code");
								recd = rs.getString("recd");
								refType = rs.getString("ref_type");
								crTerm = rs.getString("cr_term");
								itemSer = rs.getString("item_ser");
								salesPers = rs.getString("sales_pers");
								salesPers1 = rs.getString("sales_pers__1");
								salesPers2 = rs.getString("sales_pers__2");
								tranDate = rs.getTimestamp("tran_date");
								custRefNo = rs.getString("cust_ref_no");
								custRefDate = rs.getTimestamp("cust_ref_date");
								custRefAmount = rs.getDouble("cust_ref_amt");
								gpNo = rs.getString("gp_no");
								gpDate = rs.getTimestamp("gp_date");
								discountDate =  rs.getTimestamp("discount_dt");
							}
							if(pstmt != null)
							{
								pstmt.close();pstmt = null;
							}
							if(rs != null)
							{
								rs.close();rs = null;
							}
							
							System.out.println("Inside createRIBCA----->amount["+amount+"] * exchRate["+exchRate+"]* ");
							
							amountBc = amount * exchRate ;
							
							System.out.println("Inside createRIBCA----->amountBc--["+amountBc+"]");
							if(amountBc != 0)
							{
								sql = " insert into rcp_ibca (tran_id, tran_date, eff_date, site_code__from, fin_entity__from, "
									 +" site_code__to, fin_entity__to, amount, curr_code, exch_rate, confirmed, acct_code__ifr," 
									 +" cctr_code__ifr, acct_code__ito, cctr_code__ito, chg_date, chg_user, chg_term, amount__bc, "
									 +" tran_type, tran_ser, link_type, remarks) " //23
									 +" values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, ibcaID);
								pstmt.setTimestamp(2, tranDate);
								pstmt.setTimestamp(3, tranDate);
								pstmt.setString(4, siteCode);
								pstmt.setString(5, finEntityFrom);
								pstmt.setString(6, siteCodeRcv);
								pstmt.setString(7, finEntityTo);
								pstmt.setDouble(8, amount);
								pstmt.setString(9, currCode);
								pstmt.setDouble(10, exchRate);
								pstmt.setString(11, "N");
								pstmt.setString(12, acctCodePay);
								pstmt.setString(13, cctrCodePay);
								pstmt.setString(14, acctCodeRcp);
								pstmt.setString(15, cctrCodeRcp);
								pstmt.setTimestamp(16, tranDate);
								pstmt.setString(17, chgUser);
								pstmt.setString(18, chgTerm);
								pstmt.setDouble(19, amountBc);
								pstmt.setString(20, tranType);
								pstmt.setString(21, "I");
								pstmt.setString(22, linkType);
								pstmt.setString(23, remarks);
								pstmt.executeUpdate();
								pstmt.close();//added by Pavan R 10oct18[to handle open cursor issue]
								pstmt = null;
								sql = " insert into rcp_ibca_det (tran_id, ref_ser, ref_no, ref_date, curr_code, exch_rate, "
									 +" cust_code, acct_code, cctr_code, due_date, tot_amt, discount,tax_amt, bank_code, "
									 +" ref_type, auto_rcp, sales_pers, item_ser, sales_pers__1, sales_pers__2, cr_term, "
									 +" recd, ref_ser__org, line_no__ref, cust_ref_no, cust_ref_date, cust_ref_amt, gp_no, gp_date,discount_dt) " //30
									 +" values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, ibcaID);
								pstmt.setString(2, "S-INV");
								pstmt.setString(3, invoiceID);
								pstmt.setTimestamp(4, refDate);
								pstmt.setString(5, currCode);
								pstmt.setDouble(6, exchRate);
								pstmt.setString(7, custCode);
								pstmt.setString(8, acctCode);
								pstmt.setString(9, cctrCode);
								pstmt.setTimestamp(10, dueDate);
								pstmt.setDouble(11, amount);
								pstmt.setDouble(12, discount);
								pstmt.setDouble(13, taxAmount);
								pstmt.setString(14, bankCode);
								pstmt.setString(15, refType);
								pstmt.setString(16, autoRcp);
								pstmt.setString(17, salesPers);
								pstmt.setString(18, itemSer);
								pstmt.setString(19, salesPers1);
								pstmt.setString(20, salesPers2);
								pstmt.setString(21, crTerm);
								pstmt.setString(22, recd);
								pstmt.setString(23, tranSer);
								pstmt.setString(24, "1");
								pstmt.setString(25, custRefNo);
								pstmt.setTimestamp(26, custRefDate);
								pstmt.setDouble(27, custRefAmount);
								pstmt.setString(28, gpNo);
								pstmt.setTimestamp(29, gpDate);
								pstmt.setTimestamp(30, discountDate);
								pstmt.executeUpdate();
								pstmt.close();//Changed by Pavan R 10oct18[to handle open cursor issue]
								pstmt = null;
								RcvIbcaConf ibcaObj = new RcvIbcaConf();
								retString = ibcaObj.retreiveRibca (ibcaID, siteCode, xtraParamsStr, conn);
								
								System.out.println("After createRIBCA retreiveRibca ----->["+retString+"]");
								
								if(retString != null && retString.trim().length() > 0)
								{
									return retString;
								}
							}
					}
				}
			}

		}
		catch(Exception e)
		{
			System.out.println("Expcetion inside createRIBCA ==>"+e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(rs!=null)
				{
					rs.close();rs= null;
				}
				if(pstmt!=null)
				{
					pstmt.close();pstmt= null;
				}
				if(pstmtDet!=null)
				{
					pstmtDet.close();pstmtDet=null;
				}
				if(pstmtHdr!=null)
				{
					pstmtHdr.close();pstmtHdr=null;
				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
		return retString;
	}
	//Changed by wasim on 07-JUN-2017 for creating IBCA tranaction (Migration) [END]

	private String gbfPostInvoiceDet(HashMap<String, String> InvoiceHrdMap, ArrayList<Object> invoiceAcctList, Connection conn) throws Exception
	{
		// TODO Auto-generated method stub
		String retString="",lsPostdate="",ldPostdate="",sql="",lsProjCode="",partyDocRef="";
		String actCode = "";  // added by Mahesh Saggam
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		HashMap glTraceMap = null;
		HashMap<String, String>InvoiceAcctMap=null;
		Timestamp ldPostdate2=null;
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();  // Added by Mahesh Saggam on 29/jul/2019
		try
		{
			lsPostdate=checkNull(finCommon.getFinparams("999999", "POST_DATE_INV", conn));
			if(lsPostdate==null || lsPostdate.equalsIgnoreCase("NULLFOUND") || lsPostdate.trim().length()==0)
			{
				ldPostdate=InvoiceHrdMap.get("tran_date");
			}
			if(lsPostdate.contains("TRAN"))
			{
				ldPostdate=InvoiceHrdMap.get("tran_date");
			}
			else if(lsPostdate.contains("EFF"))
			{
				ldPostdate=InvoiceHrdMap.get("eff_date");
			}
			else
			{
				ldPostdate=InvoiceHrdMap.get("tran_date");
			}

			sql="select b.proj_code from 	invoice a, sorder b where a.sale_order = b.sale_order and	a.invoice_id =?";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, InvoiceHrdMap.get("invoice_id"));
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				lsProjCode=checkNull(rs.getString("proj_code"));
			}
			pstmt.close();
			pstmt=null;
			rs.close();
			rs=null;

			for(int itr=0; itr<invoiceAcctList.size();itr++)
			{
				InvoiceAcctMap=new HashMap<String, String>();
				InvoiceAcctMap=(HashMap<String, String>) invoiceAcctList.get(itr);
				//System.out.println("@@@@@@@1063 itr["+itr+"]InvoiceAcctMap["+InvoiceAcctMap+"].......................InvoiceHrdMap["+InvoiceHrdMap+"]");
				/**
				 * Generate gl_trace Map 
				 * */
				glTraceMap=new HashMap();

				glTraceMap.put("ref_type", "D");
				glTraceMap.put("ref_ser", "S-INV");
				glTraceMap.put("ref_id", InvoiceHrdMap.get("invoice_id"));
				//glTraceMap.put("ref_id", InvoiceHrdMap.get("invoice_id"));

				partyDocRef = finCommon.gfGetPartyDocRef((String)glTraceMap.get("ref_ser"), (String)glTraceMap.get("ref_id"),conn);
				glTraceMap.put("party_doc_ref", partyDocRef);

				if( ldPostdate != null && ldPostdate.trim().length() > 0 )
				{
					ldPostdate2 = Timestamp.valueOf(genericUtility.getValidDateString(ldPostdate, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
				}
				glTraceMap.put("tran_date", ldPostdate2);
				// 02-dec-16 manoharan avoid deprecated
				//glTraceMap.put("eff_date",new Timestamp(Timestamp.parse(InvoiceHrdMap.get("eff_date"))));
				glTraceMap.put("eff_date", Timestamp.valueOf(genericUtility.getValidDateString(InvoiceHrdMap.get("eff_date"), genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0"));
				glTraceMap.put("fin_entity", InvoiceHrdMap.get("fin_entity"));
				glTraceMap.put("site_code", InvoiceHrdMap.get("site_code"));
				glTraceMap.put("sundry_type","O");
				glTraceMap.put("sundry_code"," ");
				glTraceMap.put("acct_code",InvoiceAcctMap.get("acct_code"));
				actCode = InvoiceAcctMap.get("acct_code");
				if(actCode == null || actCode.trim().length() == 0)  // Added by Mahesh Saggam on 29-07-2019 [account code should not be null]
				{
					retString = itmDBAccessEJB.getErrorString("", "VTACCT", "","", conn);
					return retString;
				}
				glTraceMap.put("cctr_code",InvoiceAcctMap.get("cctr_code"));
				glTraceMap.put("anal_code",InvoiceAcctMap.get("anal_code"));
				glTraceMap.put("curr_code",InvoiceHrdMap.get("curr_code"));
				//glTraceMap.put("exch_rate",InvoiceHrdMap.get("exch_rate"));
				glTraceMap.put("exch_rate", Double.parseDouble(InvoiceHrdMap.get("exch_rate").toString()));
				if(Double.parseDouble(InvoiceAcctMap.get("amount"))>0)
				{
					//glTraceMap.put("dr_amt",0);
					glTraceMap.put("dr_amt", Double.parseDouble("0"));
					//glTraceMap.put("cr_amt",InvoiceAcctMap.get("amount"));
					//glTraceMap.put("dr_amt", Double.parseDouble("0"));

					glTraceMap.put("cr_amt", Double.parseDouble(InvoiceAcctMap.get("amount").toString()));
					//glTraceMap.put("dr_amt", Double.parseDouble(InvoiceAcctMap.get("amount").toString()));
					//glTraceMap.put("cr_amt", Double.parseDouble("0"));
				}
				else
				{					
					//glTraceMap.put("dr_amt",(0-Double.parseDouble(InvoiceAcctMap.get("amount"))));
					glTraceMap.put("dr_amt", (0-Double.parseDouble(InvoiceAcctMap.get("amount"))));
					//glTraceMap.put("cr_amt",0);
					glTraceMap.put("cr_amt", Double.parseDouble("0"));

					//glTraceMap.put("dr_amt", Double.parseDouble("0"));
					//glTraceMap.put("cr_amt", (0-Double.parseDouble(InvoiceAcctMap.get("amount"))));
				}
				//System.out.println("@@@@@1103 InvoiceHrdMap.get(remarks)["+InvoiceHrdMap.get("remarks")+"]");
				//glTraceMap.put("remarks", InvoiceHrdMap.get("remarks").substring(0, 60));
				glTraceMap.put("remarks", InvoiceHrdMap.get("remarks"));
				glTraceMap.put("proj_code", lsProjCode);
				glTraceMap.put("analysis1", InvoiceAcctMap.get("analysis1"));
				glTraceMap.put("analysis2", InvoiceAcctMap.get("analysis2"));
				glTraceMap.put("analysis3", InvoiceAcctMap.get("analysis3"));

				//System.out.println("@@@@@@@ glTraceMap["+glTraceMap+"]");
				/**
				 * gl_trace update
				 * */
				retString=finCommon.glTraceUpdate(glTraceMap,conn);
				
				//added by monika-13 dec-19-to  return retstring 
				if(retString != null && retString.trim().length() > 0)
				{
					return retString;
				}//end.
				
				
			}


		} catch (ITMException e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage()); 
			throw new ITMException(e); 
		} catch (SQLException e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage()); 
			throw new ITMException(e); 
		}

		return retString;
	}

	private String gbfPostInvoiceHdr(HashMap<String, String> InvoiceHrdMap,Connection conn) throws ITMException
	{
		// TODO Auto-generated method stub
		String retSting="",sysDate="",lsPostdate="",sql="",lsProjCode="",partyDocRef="",lsAnalysis="",lsTemp="",llRelAfter="",lsAmtType="";
		String analysis1__dr="",analysis2__dr="",analysis3__dr="",analysis1__cr="",analysis2__cr="",analysis3__cr="",sql1="";
		String lineNoRef="";
		double lcCramt=0.0,lc_dramt=0.0,lcSchemehdrAmt=0.0,lcRelAmt=0.0,lcTotAmt=0.0;
		Timestamp ldDueDate=null;
		PreparedStatement pstmt=null,pstmt1=null;
		ResultSet rs=null,rs1=null;
		HashMap sundryBalMap = null;
		HashMap glTraceMap = null;
		HashMap receivablesMap = null;
		String[] analysisStr =null;
		int cnt=0,lsNo=0;
		UtilMethods utlMethods= new UtilMethods();
		Timestamp ldDiscdt=null;
		Timestamp gpDateTstamp = null;
		String gpDate="";
		String actCode = ""; // added by Mahesh Saggam
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB(); // added by Mahesh Saggam on 29/jul/2019
		try
		{
			java.util.Date today = new java.util.Date();
			Calendar cal = Calendar.getInstance();
			cal.setTime(today);
			today = cal.getTime();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			SimpleDateFormat dbf = new SimpleDateFormat(genericUtility.getDBDateFormat());
			sysDate = sdf.format(today);
			//System.out.println("System date  :- [" + sysDate + "]");

			lsPostdate=checkNull(finCommon.getFinparams("999999", "POST_DATE_INV", conn));	
			if(lsPostdate==null || lsPostdate.trim().length()==0 || "NULLFOUND".equalsIgnoreCase(lsPostdate))
			{
				lsPostdate=InvoiceHrdMap.get("tran_date");
			}
			else if(lsPostdate.contains("TRAN"))
			{
				lsPostdate=InvoiceHrdMap.get("tran_date");
			}
			else if(lsPostdate.contains("EFF"))
			{
				lsPostdate=InvoiceHrdMap.get("eff_date");
			}
			else
			{
				lsPostdate=InvoiceHrdMap.get("tran_date");
			}
			/**
			 * Generate sundry_bal Map
			 * */
			sundryBalMap = new HashMap();
			// 02-dec-16 manoharan avoid deprecated
			//sundryBalMap.put("tran_date",new Timestamp(Timestamp.parse(InvoiceHrdMap.get("tran_date"))));
			//sundryBalMap.put("eff_date",new Timestamp(Timestamp.parse(InvoiceHrdMap.get("eff_date"))));
			sundryBalMap.put("tran_date", Timestamp.valueOf(genericUtility.getValidDateString(InvoiceHrdMap.get("tran_date"), genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0"));
			sundryBalMap.put("eff_date",  Timestamp.valueOf(genericUtility.getValidDateString(InvoiceHrdMap.get("eff_date"), genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0"));
			sundryBalMap.put("fin_entity", InvoiceHrdMap.get("fin_entity"));
			sundryBalMap.put("site_code", InvoiceHrdMap.get("site_code"));
			sundryBalMap.put("sundry_type", "C");
			sundryBalMap.put("sundry_code", InvoiceHrdMap.get("cust_code__bil"));
			sundryBalMap.put("acct_code", InvoiceHrdMap.get("acct_code__ar"));
			actCode = InvoiceHrdMap.get("acct_code__ar");
			if(actCode == null || actCode.trim().length() == 0) // Added by Mahesh Saggam on 29-07-2019 [account code should not be null]
			{
				retSting = itmDBAccessEJB.getErrorString("", "VTARACCT", "","", conn);
				return retSting;
			}
			sundryBalMap.put("cctr_code", InvoiceHrdMap.get("cctr_code__ar"));
			sundryBalMap.put("curr_code", InvoiceHrdMap.get("curr_code"));
			//sundryBalMap.put("exch_rate", InvoiceHrdMap.get("exch_rate"));
			sundryBalMap.put("exch_rate", Double.parseDouble(InvoiceHrdMap.get("exch_rate").toString()));
			//sundryBalMap.put("dr_amt", InvoiceHrdMap.get("net_amt"));
			sundryBalMap.put("dr_amt", Double.parseDouble(InvoiceHrdMap.get("net_amt").toString()));
			//sundryBalMap.put("cr_amt", 0);
			sundryBalMap.put("cr_amt", Double.parseDouble("0"));
			//sundryBalMap.put("adv_amt", 0.0);
			sundryBalMap.put("adv_amt", Double.parseDouble("0"));

			sundryBalMap.put("contact_code", "");

			//System.out.println("@@@@@@@@@@@ sundryBalMap["+sundryBalMap+"]");
			/**
			 * sundry_bal update
			 * */
			retSting = finCommon.gbf_sundrybal_upd(sundryBalMap,conn);

			sql="select b.proj_code from 	invoice a, sorder b where a.sale_order = ? and	a.invoice_id = ?";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, InvoiceHrdMap.get("sale_order"));
			pstmt.setString(2, InvoiceHrdMap.get("invoice_id"));
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				lsProjCode=checkNull(rs.getString("proj_code"));
			}
			pstmt.close();
			pstmt=null;
			rs.close();
			rs=null;

			/**
			 * Generate gl_trace Map 
			 * */
			glTraceMap = new HashMap();  // added by cpatil for null pointer error on 23/05/16

			glTraceMap.put("ref_type", "D");
			glTraceMap.put("ref_ser", "S-INV");
			glTraceMap.put("ref_id", InvoiceHrdMap.get("invoice_id"));

			partyDocRef = finCommon.gfGetPartyDocRef((String)glTraceMap.get("ref_ser"), (String)glTraceMap.get("ref_id"),conn);
			glTraceMap.put("party_doc_ref", partyDocRef);

			//System.out.println("@@@@@ lsPostdate["+lsPostdate+"]");

			Timestamp lsPostdateTemp = Timestamp.valueOf(genericUtility.getValidDateString(lsPostdate, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");

			//System.out.println("@@@@@ lsPostdateTemp["+lsPostdateTemp+"]");

			//glTraceMap.put("tran_date", lsPostdate);
			glTraceMap.put("tran_date", lsPostdateTemp);
			// 02-dec-16 manoharan avoid deprecated
			//glTraceMap.put("eff_date",new Timestamp(Timestamp.parse(InvoiceHrdMap.get("eff_date"))));
			glTraceMap.put("eff_date", Timestamp.valueOf(genericUtility.getValidDateString(InvoiceHrdMap.get("eff_date"), genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0"));
			glTraceMap.put("fin_entity", InvoiceHrdMap.get("fin_entity"));
			glTraceMap.put("site_code", InvoiceHrdMap.get("site_code"));
			glTraceMap.put("sundry_type", "C");
			glTraceMap.put("sundry_code", InvoiceHrdMap.get("cust_code__bil"));
			glTraceMap.put("acct_code", InvoiceHrdMap.get("acct_code__ar"));
			actCode = InvoiceHrdMap.get("acct_code__ar");
			if(actCode == null || actCode.trim().length() == 0) // Added by Mahesh Saggam on 29-07-2019 [account code should not be null]
			{
				retSting = itmDBAccessEJB.getErrorString("", "VTARACCT", "","", conn);
				return retSting;
			}
			glTraceMap.put("cctr_code", InvoiceHrdMap.get("cctr_code__ar"));
			glTraceMap.put("anal_code", InvoiceHrdMap.get("anal_code"));
			glTraceMap.put("curr_code", InvoiceHrdMap.get("curr_code"));
			//glTraceMap.put("exch_rate", InvoiceHrdMap.get("exch_rate"));
			glTraceMap.put("exch_rate", Double.parseDouble(InvoiceHrdMap.get("exch_rate").toString()));
			//glTraceMap.put("dr_amt", InvoiceHrdMap.get("net_amt"));
			glTraceMap.put("dr_amt", Double.parseDouble(InvoiceHrdMap.get("net_amt").toString()));
			//glTraceMap.put("cr_amt", 0);
			glTraceMap.put("cr_amt", Double.parseDouble("0"));

			//System.out.println("@@@@@@ InvoiceHrdMap.get(remarks)["+InvoiceHrdMap.get("remarks").toString()+"]");
			String remarksTemp ="";
			if( InvoiceHrdMap.get("remarks").toString() != null && InvoiceHrdMap.get("remarks").toString().trim().length() > 0 )
			{
				if(InvoiceHrdMap.get("remarks").toString().trim().length() > 60)
				{
					remarksTemp = InvoiceHrdMap.get("remarks").substring(0, 60);
				}
				else
				{
					remarksTemp = InvoiceHrdMap.get("remarks").toString();
				}
			}
			//System.out.println("@@@@@@ remarksTemp["+remarksTemp+"]");
			glTraceMap.put("remarks", remarksTemp);
			glTraceMap.put("proj_code", lsProjCode);
			lcCramt = 0;
			//sql="select sum(case when tax_amt is null then 0 else tax_amt end) as lcCramt from taxtran where tran_code = 'S-INV'"
			sql="select (case when sum(tax_amt) is null then 0 else sum(tax_amt) end) as lcCramt from taxtran where tran_code = 'S-INV'"
					+ " and tran_id = ? and	effect <> 'N' and	pay_tax = 'Y' ";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, glTraceMap.get("ref_id").toString());
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				lcCramt=rs.getDouble("lcCramt");
			}
			pstmt.close();
			pstmt=null;
			rs.close();
			rs=null;

			//System.out.println("@@@@@@@1262 glTraceMap["+glTraceMap+"]");

			if(lcCramt==0)
			{
				lc_dramt=(Double)glTraceMap.get("dr_amt")-lcCramt;
				glTraceMap.put("dr_amt",lc_dramt);
			}
			/**
			 * gl_trace update
			 * */
			retSting=finCommon.glTraceUpdate(glTraceMap,conn);
		//added by monika 13 dec 19 to check retstring null or not
			if(retSting != null && retSting.trim().length() > 0)
			{
				return retSting;
			}//end

			if(Math.abs(lcCramt) > 0)
			{
				glTraceMap.put("dr_amt",0);

				lcCramt=(-1)*lcCramt;
				glTraceMap.put("cr_amt",lcCramt);

				/**
				 * gl_trace update
				 * */
				retSting=finCommon.glTraceUpdate(glTraceMap,conn);
				//added by monika 13 dec 19 to check retstring null or not
				if(retSting != null && retSting.trim().length() > 0)
				{
					return retSting;
				}//end
			}

			//System.out.println("@@@@@@@@ disc_offinv_amt_hdr["+InvoiceHrdMap.get("disc_offinv_amt_hdr")+"]");
			//System.out.println("@@@@@@@@ disc_billback_amt_hdr["+InvoiceHrdMap.get("disc_billback_amt_hdr")+"]");

			if((InvoiceHrdMap.get("disc_offinv_amt_hdr") ==null) || (InvoiceHrdMap.get("disc_offinv_amt_hdr") !=null && InvoiceHrdMap.get("disc_offinv_amt_hdr").trim().length()==0))
			{
				InvoiceHrdMap.put("disc_offinv_amt_hdr","0");
			}

			if((InvoiceHrdMap.get("disc_billback_amt_hdr") ==null) || (InvoiceHrdMap.get("disc_offinv_amt_hdr") !=null && InvoiceHrdMap.get("disc_billback_amt_hdr").trim().length()==0))
			{
				InvoiceHrdMap.put("disc_billback_amt_hdr","0");
			}	

			lcSchemehdrAmt=Double.parseDouble(InvoiceHrdMap.get("disc_offinv_amt_hdr")==null?"0":InvoiceHrdMap.get("disc_offinv_amt_hdr"))+Double.parseDouble(InvoiceHrdMap.get("disc_billback_amt_hdr")==null?"0":InvoiceHrdMap.get("disc_billback_amt_hdr"));

			//System.out.println("@@@@@ lcSchemehdrAmt["+lcSchemehdrAmt+"]");

			if(lcSchemehdrAmt>0)
			{
				InvAcct invAcct=new InvAcct();
				lsAnalysis=invAcct.AcctAnalysisType(" ","S-INV",InvoiceHrdMap.get("inv_type"),"DR",conn);				
				lsTemp="Invoice Header Posting"+lsAnalysis;

				analysisStr = lsAnalysis.split("@");
				int len = analysisStr.length-1;
				//System.out.println("@@@cr len["+len+"]");
				if( len > -1 )
				{
					analysis1__dr = analysisStr[0];
				}
				else
				{
					analysis1__dr = "";	
				}	
				if( len > 0 )
				{
					analysis2__dr = analysisStr[1];	
				}
				else
				{	
					analysis2__dr = "";
				}
				if( len > 1)
				{
					analysis3__dr = analysisStr[2];
				}
				else
				{
					analysis3__dr ="";	
				}

				glTraceMap.put("sundry_code", ' ');
				glTraceMap.put("acct_code", InvoiceHrdMap.get("acc_code__order"));
				actCode = InvoiceHrdMap.get("acc_code__order");
				if(actCode == null || actCode.trim().length() == 0) // Added by Mahesh Saggam on 29-07-2019 [account code should not be null]
				{
					retSting = itmDBAccessEJB.getErrorString("", "VTORDACCT", "","", conn);
					return retSting;
				}
				if(InvoiceHrdMap.get("cctr_code__sal")!=null && InvoiceHrdMap.get("cctr_code__sal").trim().length()>0)
				{
					glTraceMap.put("cctr_code", InvoiceHrdMap.get("cctr_code__sal"));
				}
				else
				{
					glTraceMap.put("cctr_code", InvoiceHrdMap.get("cctr_code__ar"));
				}

				glTraceMap.put("sundry_type", "O");				
				glTraceMap.put("dr_amt", lcSchemehdrAmt);				
				glTraceMap.put("cr_amt", 0);				
				glTraceMap.put("ANALYSIS1", analysis1__dr);				
				glTraceMap.put("ANALYSIS2", analysis2__dr);				
				glTraceMap.put("ANALYSIS3", analysis3__dr);						

				/**
				 * gl_trace update
				 * */
				retSting=finCommon.glTraceUpdate(glTraceMap,conn);
				//added by monika 13 dec 19 to check retstring null or not
				if(retSting != null && retSting.trim().length() > 0)
				{
					return retSting;
				}//end
			}

			if(Double.parseDouble(InvoiceHrdMap.get("disc_billback_amt_hdr"))>0)
			{
				glTraceMap.put("sundry_code", ' ');
				glTraceMap.put("acct_code", InvoiceHrdMap.get("acct_code__pr"));
				actCode = InvoiceHrdMap.get("acct_code__pr");
				if(actCode == null || actCode.trim().length() == 0) // Added by Mahesh Saggam on 29-07-2019 [account code should not be null]
				{
					retSting = itmDBAccessEJB.getErrorString("", "VTPRACCT", "","", conn);
					return retSting;
				}
				glTraceMap.put("cctr_code", InvoiceHrdMap.get("cctr_code__pr"));
				glTraceMap.put("dr_amt", 0);
				glTraceMap.put("cr_amt", InvoiceHrdMap.get("disc_billback_amt_hdr"));

				/**
				 * gl_trace update
				 * */
				retSting=finCommon.glTraceUpdate(glTraceMap,conn);
				//added by monika 13 dec 19 to check retstring null or not
				if(retSting != null && retSting.trim().length() > 0)
				{
					return retSting;
				}//end
			}

			sql="select count(*) as cnt from sord_cr_terms where sale_order = ? and cr_type = '03'";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, InvoiceHrdMap.get("sale_order"));
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				cnt=rs.getInt("cnt");
			}
			pstmt.close();
			pstmt=null;
			rs.close();
			rs=null;

			if(cnt>0)
			{
				sql="select line_no,rel_after,rel_amt,amt_type from sord_cr_terms where sale_order = ? and cr_type = '03'";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1, InvoiceHrdMap.get("sale_order"));
				rs=pstmt.executeQuery();
				while(rs.next())
				{
					lsNo=rs.getInt("line_no");
					llRelAfter=checkNull(rs.getString("rel_after"));
					lcRelAmt=rs.getDouble("rel_amt");
					lsAmtType=checkNull(rs.getString("amt_type"));

					if(lsAmtType.equalsIgnoreCase("01"))
					{
						lcRelAmt=(lcRelAmt/100)*(Double)glTraceMap.get("dr_amt");
					}
					else if(lsAmtType.equalsIgnoreCase("03"))
					{
						sql1="select sum(rel_amt) as lcTotAmt from sord_cr_terms where sale_order = ? and cr_type = '03' and amt_type = '02'";
						pstmt1=conn.prepareStatement(sql1);
						pstmt1.setString(1, InvoiceHrdMap.get("sale_order"));
						rs1=pstmt1.executeQuery();
						if(rs1.next())
						{
							//lcTotAmt=rs.getDouble("lcTotAmt");
							lcTotAmt=rs1.getDouble("lcTotAmt"); //Chnaged by Pavan R on 21aug18[to handle Invalid column name]
						}
						pstmt1.close();
						pstmt1=null;
						rs1.close();
						rs1=null;

						lcRelAmt=(Double)glTraceMap.get("dr_amt")-lcTotAmt;
					}
					//Changed By Pavan Rane on 21aug18 [to handle IllegalArgumentException on date format change]					
					//ldDueDate=Timestamp.valueOf(lsPostdate);					
					ldDueDate = lsPostdateTemp;
					System.out.println("Pavan Rane :: lsPostdate["+lsPostdate+"] lsPostdateTemp["+lsPostdateTemp+"]");
					if(!llRelAfter.equalsIgnoreCase("0"))
					{
						ldDueDate=utlMethods.RelativeDate(ldDueDate,Integer.parseInt(llRelAfter));
					}
					receivablesMap=new HashMap();
					receivablesMap.put("tran_ser", "S-INV");
					//receivablesMap.put("tran_date", Timestamp.valueOf(lsPostdate)); //Chnaged by Pavan R on 21aug18[to handle Exception on Date]
					receivablesMap.put("tran_date", lsPostdateTemp);
					receivablesMap.put("fin_entity", InvoiceHrdMap.get("fin_entity"));
					receivablesMap.put("site_code", InvoiceHrdMap.get("site_code"));
					receivablesMap.put("ref_no", InvoiceHrdMap.get("invoice_id"));
					//receivablesMap.put("ref_date", Timestamp.valueOf(lsPostdate));//Chnaged by Pavan R on 21aug18[to handle Exception on Date]
					receivablesMap.put("ref_date", lsPostdateTemp);
					receivablesMap.put("due_date", ldDueDate);
					receivablesMap.put("curr_code", InvoiceHrdMap.get("curr_code"));
					receivablesMap.put("exch_rate", InvoiceHrdMap.get("exch_rate"));
					receivablesMap.put("cust_code", InvoiceHrdMap.get("cust_code__bil"));
					receivablesMap.put("acct_code", InvoiceHrdMap.get("acct_code__ar"));
					actCode = InvoiceHrdMap.get("acct_code__ar");
					if(actCode == null || actCode.trim().length() == 0) // Added by Mahesh Saggam on 29-07-2019 [account code should not be null]
					{
						retSting = itmDBAccessEJB.getErrorString("", "VTARACCT", "","", conn);
						return retSting;
					}
					receivablesMap.put("cctr_code", InvoiceHrdMap.get("cctr_code__ar"));
					receivablesMap.put("tax_amt", InvoiceHrdMap.get("tax_amt"));
					receivablesMap.put("discount", InvoiceHrdMap.get("disc_amt"));
					receivablesMap.put("tot_amt",lcRelAmt);
					receivablesMap.put("auto_rcp", "N");
					receivablesMap.put("bank_code",InvoiceHrdMap.get("bank_code"));
					receivablesMap.put("ref_type", InvoiceHrdMap.get("inv_type"));
					receivablesMap.put("cr_term", InvoiceHrdMap.get("cr_term"));
					receivablesMap.put("sales_pers", InvoiceHrdMap.get("sales_pers"));
					//receivablesMap.put("eff_date", InvoiceHrdMap.get("eff_date"));
					// 02-dec-16 manoharan avoid deprecated
					//receivablesMap.put("eff_date", new Timestamp(Timestamp.parse(InvoiceHrdMap.get("eff_date"))));
					receivablesMap.put("eff_date", Timestamp.valueOf(genericUtility.getValidDateString(InvoiceHrdMap.get("eff_date"), genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0"));
					receivablesMap.put("sales_pers__1", InvoiceHrdMap.get("sales_pers__1"));
					receivablesMap.put("sales_pers__2", InvoiceHrdMap.get("sales_pers__2"));
					receivablesMap.put("item_ser", InvoiceHrdMap.get("item_ser"));					
					//receivablesMap.put("line_no__ref", lsNo);//changed[to handle ClassCastException]
					receivablesMap.put("line_no__ref", String.valueOf(lsNo));					
					receivablesMap.put("gp_no", InvoiceHrdMap.get("gp_no"));
					//receivablesMap.put("gp_date", InvoiceHrdMap.get("gp_date"));
					// 02-dec-16 manoharan avoid deprecated
					//receivablesMap.put("gp_date", new Timestamp(Timestamp.parse(InvoiceHrdMap.get("gp_date"))));
					//receivablesMap.put("gp_date", Timestamp.valueOf(genericUtility.getValidDateString(InvoiceHrdMap.get("gp_date"), genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0"));
					//changed by nasruddin start 8-12-16 start
					if( InvoiceHrdMap.get("gp_date")!= null)
					{
						gpDateTstamp =	Timestamp.valueOf(genericUtility.getValidDateString(InvoiceHrdMap.get("gp_date").toString(), genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
					}
					System.out.println("gpDateTstamp IF["+gpDateTstamp+"]");
					receivablesMap.put("gp_date",gpDateTstamp);
					//changed by nasruddin start 8-12-16 End
					receivablesMap.put("mrp_value","0");

					ldDiscdt=calcDiscountDate(InvoiceHrdMap.get("cr_term"), InvoiceHrdMap.get("tran_date"), InvoiceHrdMap.get("eff_date"),
							InvoiceHrdMap.get("tran_date"),conn);
					receivablesMap.put("discount_date", ldDiscdt);

					/**
					 * receivable update
					 * */
					retSting = finCommon.gbfReceivablesUpd(receivablesMap,conn);
					//Start Added by chandrashekar on 12-sep-2016
					//System.out.println("gbfReceivablesUpd retSting 1>>>"+retSting);
					if(retSting == null ||retSting.trim().length()==0)
					{
						
						lineNoRef=""+lsNo;
						retSting=gbfPostDrcradj(InvoiceHrdMap.get("invoice_id"),lineNoRef,conn);
					}
					//End Added by chandrashekar on 12-sep-2016
					//added by monika 13 dec 19 to check retstring null or not
					if(retSting != null && retSting.trim().length() > 0)
					{
						return retSting;
					}//end
				}
				pstmt.close();
				pstmt=null;
				rs.close();
				rs=null;
			}
			else
			{
				receivablesMap=new HashMap();
				receivablesMap.put("tran_ser", "S-INV");
				//				receivablesMap.put("tran_date", Timestamp.valueOf(lsPostdate));
				receivablesMap.put("tran_date", Timestamp.valueOf(genericUtility.getValidDateString(lsPostdate, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0"));
				receivablesMap.put("fin_entity", InvoiceHrdMap.get("fin_entity"));
				receivablesMap.put("site_code", InvoiceHrdMap.get("site_code"));
				receivablesMap.put("ref_no", InvoiceHrdMap.get("invoice_id"));
				//				receivablesMap.put("ref_date", Timestamp.valueOf(lsPostdate));
				receivablesMap.put("ref_date", Timestamp.valueOf(genericUtility.getValidDateString(lsPostdate, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0"));
				//				receivablesMap.put("due_date", InvoiceHrdMap.get("due_date"));
				receivablesMap.put("due_date", Timestamp.valueOf(genericUtility.getValidDateString(InvoiceHrdMap.get("due_date").toString(), genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0"));
				receivablesMap.put("curr_code", InvoiceHrdMap.get("curr_code"));
				receivablesMap.put("exch_rate", InvoiceHrdMap.get("exch_rate"));
				receivablesMap.put("cust_code", InvoiceHrdMap.get("cust_code__bil"));
				receivablesMap.put("acct_code", InvoiceHrdMap.get("acct_code__ar"));
				actCode = InvoiceHrdMap.get("acct_code__ar");
				if(actCode == null || actCode.trim().length() == 0) // Added by Mahesh Saggam on 29-07-2019 [account code should not be null]
				{
					retSting = itmDBAccessEJB.getErrorString("", "VTARACCT", "","", conn);
					return retSting;
				}
				receivablesMap.put("cctr_code", InvoiceHrdMap.get("cctr_code__ar"));
				receivablesMap.put("tax_amt", InvoiceHrdMap.get("tax_amt"));
				receivablesMap.put("discount", InvoiceHrdMap.get("disc_amt"));
				//receivablesMap.put("tot_amt",lcRelAmt);
				receivablesMap.put("tot_amt",InvoiceHrdMap.get("net_amt"));//Change by chandrashekar on 09-sep-2016
				receivablesMap.put("auto_rcp", "N");
				receivablesMap.put("bank_code",InvoiceHrdMap.get("bank_code"));
				receivablesMap.put("ref_type", InvoiceHrdMap.get("inv_type"));
				receivablesMap.put("cr_term", InvoiceHrdMap.get("cr_term"));
				receivablesMap.put("sales_pers", InvoiceHrdMap.get("sales_pers"));
				//				receivablesMap.put("eff_date", InvoiceHrdMap.get("eff_date"));
				receivablesMap.put("eff_date", Timestamp.valueOf(genericUtility.getValidDateString(InvoiceHrdMap.get("eff_date").toString(), genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0"));
				receivablesMap.put("sales_pers__1", InvoiceHrdMap.get("sales_pers__1"));
				receivablesMap.put("sales_pers__2", InvoiceHrdMap.get("sales_pers__2"));
				receivablesMap.put("item_ser", InvoiceHrdMap.get("item_ser"));
				receivablesMap.put("gp_no", InvoiceHrdMap.get("gp_no"));
				//receivablesMap.put("gp_date", InvoiceHrdMap.get("gp_date"));// Change By Nasruddin on 14-oct-16
				//Changed By Nasruddin start 08-12-16 Start
				//receivablesMap.put("gp_date", Timestamp.valueOf(genericUtility.getValidDateString(InvoiceHrdMap.get("gp_date").toString(), genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0"));
				
				if( InvoiceHrdMap.get("gp_date")!= null)
				{
					gpDateTstamp =	Timestamp.valueOf(genericUtility.getValidDateString(InvoiceHrdMap.get("gp_date").toString(), genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
				}
				System.out.println("gpDateTstamp["+gpDateTstamp+"]");
				receivablesMap.put("gp_date",gpDateTstamp);
				//Changed By Nasruddin start 08-12-16 End
				receivablesMap.put("mrp_value","0");
				ldDiscdt=calcDiscountDate(InvoiceHrdMap.get("cr_term"), InvoiceHrdMap.get("tran_date"), InvoiceHrdMap.get("eff_date"),
						InvoiceHrdMap.get("tran_date"),conn);
				receivablesMap.put("discount_date", ldDiscdt);

				/**
				 * receivable update
				 * */
				//System.out.println("@@@@@@@@ receivablesMap["+receivablesMap+"]");
				retSting = finCommon.gbfReceivablesUpd(receivablesMap,conn);
				//Start Added by chandrashekar on 12-sep-2016
				//System.out.println("gbfReceivablesUpd retSting 2 >>>"+retSting);
				if(retSting == null ||retSting.trim().length()==0)
				{

					lineNoRef="1";
					retSting=gbfPostDrcradj(InvoiceHrdMap.get("invoice_id"),lineNoRef,conn);
				}
				//End Added by chandrashekar on 12-sep-2016
				//added by monika 13 dec 19 to check retstring null or not
				if(retSting != null && retSting.trim().length() > 0)
				{
					return retSting;
				}//end
			}

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


		return retSting;
	}

	private Timestamp calcDiscountDate(String crTerm, String vouchDate, String effDate, String billDate,Connection conn) throws ITMException
	{
		String discountDate="",sql="",lsStart="",lsMonth="",lsOverridediscdt="";
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		Timestamp ldDuedate=null,ldTranDt=null;
		long llCount,liMonth,liCrDays,liCurrDays,liDueDays;
		int sqlCount=0,liDays=0;
		UtilMethods utlMethods= new UtilMethods();
		try
		{
			// TODO Auto-generated method stub
			sql="select start_from from crterm where cr_term = ?";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, crTerm);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				lsStart=checkNull(rs.getString("start_from"));
			}
			pstmt.close();
			pstmt=null;
			rs.close();
			rs=null;

			if(lsStart.equalsIgnoreCase("R"))
			{
				System.out.println("vouchDate:["+vouchDate+"]");
				//ldTranDt=Timestamp.valueOf(vouchDate);
				ldTranDt=Timestamp.valueOf(genericUtility.getValidDateString(vouchDate, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
			}
			else if(lsStart.equalsIgnoreCase("D")||lsStart.equalsIgnoreCase("Q"))
			{
				//pavan r 23jul18 start[to handle exception]
				//System.out.println("effDate:["+effDate+"]");				
				//ldTranDt=Timestamp.valueOf(effDate);
				ldTranDt=Timestamp.valueOf(genericUtility.getValidDateString(effDate, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
				//pavan r 23jul18 end
			}
			else if(lsStart.equalsIgnoreCase("B"))
			{
				//	ldTranDt=Timestamp.valueOf(billDate);
				ldTranDt=Timestamp.valueOf(genericUtility.getValidDateString(billDate, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
			}
			// 02-dec-16 manoharan avoid deprecated
			//liMonth=ldTranDt.getMonth();
			Calendar cal = Calendar.getInstance();
			cal.setTime(ldTranDt);
			liMonth = cal.get(Calendar.MONTH);
			
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

			sql="select override_discount_date from crterm_disc where cr_term = ?"
					+ " and cr_month= ? and override_discount_date is not null";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, crTerm);
			pstmt.setString(2, lsMonth);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				sqlCount++;
				lsOverridediscdt=checkNull(rs.getString("override_discount_date"));
			}
			pstmt.close();
			pstmt=null;
			rs.close();
			rs=null;
			//System.out.println("@@@@@@@@1613 lsOverridediscdt["+lsOverridediscdt+"]");//
			if(sqlCount>0)
			{
				if(lsOverridediscdt!=null && lsOverridediscdt.trim().length()>0)
				{
					liCrDays=Long.parseLong(lsOverridediscdt.substring(2));
				}	        	
			}
			else
			{
				sql="select crtermfc.max_day,crterm_disc.override_discount_date"
						+ " from crterm_disc,crtermfc"
						+ " where crterm_disc.cr_term = crtermfc.cr_term"
						+ "  and trim(crtermfc.line_no) = '1' and crterm_disc.cr_term =?"
						+ " and crterm_disc.cr_month = '99' and crterm_disc.override_discount_date is not null";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1, crTerm);
				rs=pstmt.executeQuery();
				if(rs.next())
				{
					sqlCount++;
					lsOverridediscdt=checkNull(rs.getString("override_discount_date"));
					liDays=rs.getInt("max_day");
				}
				pstmt.close();
				pstmt=null;
				rs.close();
				rs=null;
				//System.out.println("@@@@@@@@1614 lsOverridediscdt["+lsOverridediscdt+"]");
				if(sqlCount>0)
				{
					liCrDays=Long.parseLong(lsOverridediscdt.substring(2));
				}
				else
				{
					sql="select max_day from crtermfc where cr_term = ? and crtermfc.line_no = '1'";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, crTerm);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						sqlCount++;
						liDays=rs.getInt("max_day");
					}
					pstmt.close();
					pstmt=null;
					rs.close();
					rs=null;

					if(sqlCount>0)
					{
						ldDuedate=null;
						return ldDuedate;
					}
				}
			}

			ldDuedate=utlMethods.RelativeDate(ldTranDt,liDays);

		} catch (SQLException e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage()); 
			throw new ITMException(e); 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ldDuedate;
	}

	private ArrayList<Object> getInvoiceDesp(String invoiceId, Connection conn) throws ITMException
	{
		HashMap<String, String> InvoiceDespMap = null;
		ArrayList<Object> invoiceDeapList=new ArrayList<Object>();
		double ln_amount_dis=0.00,ln_amount_sal=0.00;
		String rateStd="",qtyStd="",discount="",ls_cctr_code__sal="",mfgDtStr="",expDtStr="",excDtStr="";
		// TODO Auto-generated method stub
		String sql="";
		PreparedStatement pstmt=null;
		ResultSet rs=null;

		try
		{
			sql="SELECT invoice_trace.invoice_id,invoice_trace.inv_line_no,invoice_trace.desp_id,invoice_trace.desp_line_no,invoice_trace.item_code,"
					+ "invoice_trace.quantity,invoice_trace.unit,invoice_trace.rate,invoice_trace.unit__rate,invoice_trace.discount,"
					+ "invoice_trace.tax_class,invoice_trace.tax_chap,invoice_trace.tax_env,invoice_trace.tax_amt,invoice_trace.net_amt,"
					+ "invoice_trace.unit__std,invoice_trace.conv__qty_stduom,invoice_trace.quantity__stduom,invoice_trace.conv__rtuom_stduom,invoice_trace.rate__stduom,"
					+ "invoice_trace.comm_amt,invoice_trace.sord_no,invoice_trace.sord_line_no,invoice_trace.item_code__ord,invoice_trace.no_art,"
					+ "invoice_trace.lot_no,invoice_trace.lot_sl,invoice_trace.site_code__mfg,invoice_trace.mfg_date,invoice_trace.exp_date,"
					+ "invoice_trace.exp_lev,item.descr,invoice_trace.fob_value,invoice_trace.rate__clg,invoice_trace.line_no,"
					+ "invoice_trace.item_ser__prom,invoice_trace.curr_code,invoice_trace.exch_rate,invoice_trace.comm_amt__oc,invoice_trace.rate__std,"
					+ "invoice_trace.cost_rate,invoice_trace.line_type,invoice_trace.cust_item__ref,"
					+ "FN_INV_CUSTITM_DSC( INVOICE_TRACE.INVOICE_ID,INVOICE_TRACE.ITEM_CODE,INVOICE_TRACE.CUST_ITEM__REF)  custitem_desc,"
					+ "invoice_trace.disc_schem_billback_amt,invoice_trace.disc_schem_offinv_amt"
					+ " FROM invoice_trace,item"
					+ " WHERE ( invoice_trace.item_code = item.item_code )"
					+ " and( ( invoice_trace.invoice_id = ?) )"
					+ "ORDER BY invoice_trace.invoice_id ASC,invoice_trace.line_no ASC   ";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, invoiceId);
			rs=pstmt.executeQuery();
			while(rs.next())
			{
				InvoiceDespMap=new HashMap<String, String>(); // 06-dec-16 manoharan same object was added in all the list as many times
				
				InvoiceDespMap.put("invoice_id", checkNull(rs.getString("invoice_id")));
				InvoiceDespMap.put("inv_line_no", checkNull(rs.getString("inv_line_no")));
				InvoiceDespMap.put("desp_id", checkNull(rs.getString("desp_id")));
				InvoiceDespMap.put("desp_line_no", checkNull(rs.getString("desp_line_no")));
				InvoiceDespMap.put("item_code", checkNull(rs.getString("item_code")));
				InvoiceDespMap.put("quantity", checkNull(rs.getString("quantity")==null?"0.00":rs.getString("quantity")));
				InvoiceDespMap.put("unit", checkNull(rs.getString("unit")));
				InvoiceDespMap.put("rate", checkNull(rs.getString("rate")));
				InvoiceDespMap.put("unit__rate", checkNull(rs.getString("unit__rate")));

				discount=checkNull(rs.getString("discount")==null?"0":rs.getString("discount"));
				InvoiceDespMap.put("discount", checkNull(rs.getString("discount")==null?"0":rs.getString("discount")));

				InvoiceDespMap.put("tax_class", checkNull(rs.getString("tax_class")));
				InvoiceDespMap.put("tax_chap", checkNull(rs.getString("tax_chap")));
				InvoiceDespMap.put("tax_amt", checkNull(rs.getString("tax_amt")));
				InvoiceDespMap.put("net_amt", checkNull(rs.getString("net_amt")==null?"0":rs.getString("net_amt")));
				InvoiceDespMap.put("unit__std", checkNull(rs.getString("unit__std")));
				InvoiceDespMap.put("conv__qty_stduom", checkNull(rs.getString("conv__qty_stduom")));

				qtyStd=checkNull(rs.getString("quantity__stduom")==null?"0":rs.getString("quantity__stduom"));
				InvoiceDespMap.put("quantity__stduom", checkNull(rs.getString("quantity__stduom")==null?"0":rs.getString("quantity__stduom")));

				InvoiceDespMap.put("conv__rtuom_stduom", checkNull(rs.getString("conv__rtuom_stduom")));
				InvoiceDespMap.put("rate__stduom", checkNull(rs.getString("rate__stduom")));
				InvoiceDespMap.put("comm_amt", checkNull(rs.getString("comm_amt")));
				InvoiceDespMap.put("sord_no", checkNull(rs.getString("sord_no")));
				InvoiceDespMap.put("sord_line_no", checkNull(rs.getString("sord_line_no")));
				InvoiceDespMap.put("no_art", checkNull(rs.getString("no_art")));
				InvoiceDespMap.put("lot_no", checkNull(rs.getString("lot_no")));
				InvoiceDespMap.put("lot_sl", checkNull(rs.getString("lot_sl")));
				InvoiceDespMap.put("site_code__mfg", checkNull(rs.getString("site_code__mfg")));
				if( rs.getString("mfg_date") != null)
				{
					mfgDtStr=genericUtility.getValidDateString(rs.getString("mfg_date"), genericUtility.getDBDateFormat(),
							genericUtility.getApplDateFormat());
				}
				InvoiceDespMap.put("mfg_date", mfgDtStr);
				if( rs.getString("exp_date") != null)
				{
					expDtStr=genericUtility.getValidDateString(rs.getString("exp_date"), genericUtility.getDBDateFormat(),
							genericUtility.getApplDateFormat());
				}
				InvoiceDespMap.put("exp_date", expDtStr);

				InvoiceDespMap.put("exp_lev", checkNull(rs.getString("exp_lev")));
				InvoiceDespMap.put("item_descr", checkNull(rs.getString("descr")));
				InvoiceDespMap.put("fob_value", checkNull(rs.getString("fob_value")));
				InvoiceDespMap.put("rate__clg", checkNull(rs.getString("rate__clg")));
				InvoiceDespMap.put("line_no", checkNull(rs.getString("line_no")));
				InvoiceDespMap.put("item_ser__prom", checkNull(rs.getString("item_ser__prom")));
				InvoiceDespMap.put("curr_code", checkNull(rs.getString("curr_code")));
				/*
	        	excDtStr=genericUtility.getValidDateString(rs.getString("exch_rate"), genericUtility.getDBDateFormat(),
						genericUtility.getApplDateFormat());
				 */
				InvoiceDespMap.put("exch_rate", rs.getString("exch_rate")==null?"0":rs.getString("exch_rate"));

				InvoiceDespMap.put("comm_amt__oc", checkNull(rs.getString("comm_amt__oc")));

				rateStd=checkNull(rs.getString("rate__std")==null?"0":rs.getString("rate__std"));
				InvoiceDespMap.put("rate__std", checkNull(rs.getString("rate__std")==null?"0":rs.getString("rate__std")));

				InvoiceDespMap.put("cost_rate", checkNull(rs.getString("cost_rate")));
				InvoiceDespMap.put("line_type", checkNull(rs.getString("line_type")));
				InvoiceDespMap.put("cust_item__ref", checkNull(rs.getString("cust_item__ref")));
				InvoiceDespMap.put("custitem_desc", checkNull(rs.getString("custitem_desc")));
				InvoiceDespMap.put("disc_schem_billback_amt", checkNull(rs.getString("disc_schem_billback_amt")==null?"0":rs.getString("disc_schem_billback_amt")));
				InvoiceDespMap.put("disc_schem_offinv_amt", checkNull(rs.getString("disc_schem_offinv_amt")==null?"0":rs.getString("disc_schem_offinv_amt")));

				ln_amount_dis=(Double.parseDouble(rateStd)*Double.parseDouble(qtyStd))*Double.parseDouble(discount)/100;
				InvoiceDespMap.put("ln_amount_dis", String.valueOf(ln_amount_dis));

				ln_amount_sal=Double.parseDouble(qtyStd) * Double.parseDouble(rateStd);
				InvoiceDespMap.put("ln_amount_sal", String.valueOf(ln_amount_sal));
				//System.out.println("@@@@@1826 InvoiceDespMap["+InvoiceDespMap+"]");
				invoiceDeapList.add(InvoiceDespMap);
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

		return invoiceDeapList;
	}

	private ArrayList<Object> getInvoiceAcct(String invoiceId, Connection conn) throws ITMException
	{
		//		HashMap<String, HashMap<String, String>> InvoiceAcctExpMap=new HashMap<String, HashMap<String,String>>();

		ArrayList<Object> invoiceAcctList=new ArrayList<Object>();
		String sql="",lineNo="";
		PreparedStatement pstmt=null;
		ResultSet rs=null;

		try
		{
			sql="SELECT invacct.invoice_id,invacct.line_no,invacct.acct_code,invacct.cctr_code,invacct.emp_code,"
					+ "invacct.anal_code,invacct.amount,invacct.analysis1,invacct.analysis2,invacct.analysis3"
					+ " FROM invacct WHERE INVACCT.INVOICE_ID =?";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, invoiceId);
			rs=pstmt.executeQuery();
			while(rs.next())
			{
				HashMap<String, String>InvoiceAcctMap=new HashMap<String, String>();
				lineNo=checkNull(rs.getString("line_no"));

				InvoiceAcctMap.put("invoice_id", checkNull(rs.getString("invoice_id")));
				InvoiceAcctMap.put("line_no", checkNull(rs.getString("line_no")));
				InvoiceAcctMap.put("acct_code", checkNull(rs.getString("acct_code")));
				InvoiceAcctMap.put("cctr_code", checkNull(rs.getString("cctr_code")));
				InvoiceAcctMap.put("emp_code", checkNull(rs.getString("emp_code")));
				InvoiceAcctMap.put("anal_code", checkNull(rs.getString("anal_code")));
				InvoiceAcctMap.put("amount", checkNull(rs.getString("amount")));
				InvoiceAcctMap.put("analysis1", checkNull(rs.getString("analysis1")));
				InvoiceAcctMap.put("analysis2", checkNull(rs.getString("analysis2")));
				InvoiceAcctMap.put("analysis3", checkNull(rs.getString("analysis3")));

				//	        	InvoiceAcctExpMap.put(lineNo, InvoiceAcctMap);
				invoiceAcctList.add(InvoiceAcctMap);
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

		// TODO Auto-generated method stub
		return invoiceAcctList;
	}

	private HashMap<String, String> getInvoiceHdr(String invoiceId,Connection conn) throws ITMException
	{
		// TODO Auto-generated method stub
		HashMap<String, String> InvoiceHrdMap=new HashMap<String, String>();
		String sql="",trnDtStr="",despDtStr="",dueDtStr="",effDtStr="",taxDtStr="",confDtStr="",chgDtStr="", gpDtStr = null;
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		String blDtStr=null,lcDtStr=null,deliDtStr=null,lrDtStr=null,octRcpDtStr=null,invExpDtStr=null;//blDtStr,lcDtStr,deliDtStr added by nandkumar gadkari on 13/01/20
		try
		{
			sql="SELECT invoice.invoice_id,invoice.tran_date,invoice.sale_order,invoice.desp_id,invoice.tran_mode,"
					+ "invoice.desp_date,invoice.site_code,invoice.eff_date,invoice.fin_entity,invoice.item_ser,"
					+ "invoice.cust_code,invoice.cust_code__bil,invoice.cr_term,invoice.cr_days,invoice.due_date,"
					+ "invoice.tax_date,invoice.tax_class,invoice.tax_chap,invoice.tax_env,invoice.inv_amt,"
					+ "invoice.tran_id__fb,invoice.inv_type,invoice.tax_amt,invoice.disc_amt,invoice.net_amt,"
					+ "invoice.curr_code,invoice.anal_code,invoice.frt_type,invoice.frt_amt,invoice.curr_code__frt,"
					+ "invoice.sales_pers,invoice.comm_amt,invoice.acct_code__sal,invoice.cctr_code__sal,invoice.acct_code__ar,"
					+ "invoice.cctr_code__ar,invoice.acct_code__dis,invoice.cctr_code__dis,invoice.bank_code,invoice.adj_amount,"
					+ "invoice.confirmed,invoice.conf_date,invoice.emp_code__aprv,invoice.remarks,invoice.chg_date,"
					+ "invoice.chg_user,invoice.chg_term,finent.descr,site.descr,customer.cust_name,"
					+ "site.city,sales_pers.sp_name,invoice.exch_rate,invoice.exch_rate__frt,invoice.agent_code,"
					+ "ins_agent.sagent_name,invoice.round_adj,invoice.comm_amt__oc,invoice.sales_pers__1,invoice.sales_pers__2,"
					+ "invoice.sales_pers_comm_1,invoice.sales_pers_comm_2,invoice.sales_pers_comm_3,invoice.posttype,invoice.stan_code__init,"
					+ "' ' as advance,fn_sundry_name( 'P',invoice.sales_pers__1,'')  as salespers1_descr,"
					+ "fn_sundry_name( 'P',invoice.sales_pers__2,'')  as salespers2_descr,invoice.doc_status,invoice.gp_no,invoice.gp_date,"
					+ "invoice.sales_grp,customer.edi_addr,"
					+ "case when adj_amount < 0  then  abs(to_char(adj_amount,'9999999.99')) ||  ' Dr. ' else  abs(to_char(adj_amount,'9999999.99')) ||  ' Cr. ' end as adjust_amount,"
					+ "invoice.disc_schem_billback_amt,invoice.disc_schem_offinv_amt,invoice.acc_code__order,invoice.disc_offinv_amt_hdr,"
					+ "invoice.disc_billback_amt_hdr,invoice.acct_code__pr,invoice.cctr_code__pr"
					+ ",invoice.tran_id__rcv,invoice.bl_no,invoice.bl_date,invoice.exch_rate__ins,invoice.curr_code__ins,invoice.ins_amt"// added by nandkumar gadkari on 13/01/20
					+ ",invoice.print_status,invoice.lc_ref,invoice.lc_date,invoice.prd_code,invoice.recd_yn,invoice.market_reg,invoice.delivered,invoice.delivery_date" // added by nandkumar gadkari on 13/01/20
					+ ",invoice.lr_no,invoice.lr_date,invoice.tran_code,invoice.octroi_rcp_no,invoice.octroi_rcp_date,invoice.no_art,invoice.lorry_no,invoice.trans_mode,invoice.gross_weight" // added by nandkumar gadkari on 13/01/20
					+ ",invoice.parent__tran_id,invoice.custstock_id,invoice.inv_ackno,invoice.cd_tranno,invoice.tran_id__crn" // added by nandkumar gadkari on 13/01/20
					+ ",invoice.processed, invoice.download_flag,invoice.download_file_seq,invoice.inv_exp_date,invoice.edi_stat,invoice.edi_stat_asn" // added by nandkumar gadkari on 13/01/20
					+ " FROM invoice,sales_pers,finent,site,customer,ins_agent"
					+ " WHERE ( invoice.sales_pers = sales_pers.sales_pers (+))"
					+ " and( invoice.agent_code = ins_agent.agent_code (+))"
					+ " and( invoice.fin_entity = finent.fin_entity )"
					+ " and( invoice.site_code = site.site_code )"
					+ " and( invoice.cust_code = customer.cust_code )"
					+ " and( ( invoice.invoice_id = ? ) )";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, invoiceId);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				InvoiceHrdMap.put("invoice_id", checkNull(rs.getString("invoice_id")));

				trnDtStr=genericUtility.getValidDateString(rs.getString("tran_date"), genericUtility.getDBDateFormat(),
						genericUtility.getApplDateFormat());
				InvoiceHrdMap.put("tran_date", trnDtStr);

				InvoiceHrdMap.put("sale_order", checkNull(rs.getString("sale_order")));
				InvoiceHrdMap.put("desp_id", checkNull(rs.getString("desp_id")));
				InvoiceHrdMap.put("tran_mode", checkNull(rs.getString("tran_mode")));
				despDtStr = rs.getString("desp_date");
				//Commented and added line by Varsha V on 17-08-18 becuase giving error if desp_date is null
				//despDtStr=genericUtility.getValidDateString(rs.getString("desp_date"), genericUtility.getDBDateFormat(),
				//		genericUtility.getApplDateFormat());
				despDtStr = (despDtStr == null || despDtStr.trim().length()==0) ? despDtStr : (genericUtility.getValidDateString(despDtStr, genericUtility.getDBDateFormat(),genericUtility.getApplDateFormat()));
				InvoiceHrdMap.put("desp_date", despDtStr);

				InvoiceHrdMap.put("site_code", checkNull(rs.getString("site_code")));

				effDtStr=genericUtility.getValidDateString(rs.getString("eff_date"), genericUtility.getDBDateFormat(),
						genericUtility.getApplDateFormat());	        	
				InvoiceHrdMap.put("eff_date", effDtStr);

				InvoiceHrdMap.put("fin_entity", checkNull(rs.getString("fin_entity")));
				InvoiceHrdMap.put("item_ser", checkNull(rs.getString("item_ser")));
				InvoiceHrdMap.put("cust_code", checkNull(rs.getString("cust_code")));
				InvoiceHrdMap.put("cust_code__bil", checkNull(rs.getString("cust_code__bil")));
				InvoiceHrdMap.put("cr_term", checkNull(rs.getString("cr_term")));
				InvoiceHrdMap.put("cr_days", checkNull(rs.getString("cr_days")));

				dueDtStr=genericUtility.getValidDateString(rs.getString("due_date"), genericUtility.getDBDateFormat(),
						genericUtility.getApplDateFormat());
				InvoiceHrdMap.put("due_date",dueDtStr);

				taxDtStr=genericUtility.getValidDateString(rs.getString("tax_date"), genericUtility.getDBDateFormat(),
						genericUtility.getApplDateFormat());
				InvoiceHrdMap.put("tax_date", taxDtStr);

				InvoiceHrdMap.put("tax_class", checkNull(rs.getString("tax_class")));
				InvoiceHrdMap.put("tax_chap", checkNull(rs.getString("tax_chap")));
				InvoiceHrdMap.put("tax_env", checkNull(rs.getString("tax_env")));
				InvoiceHrdMap.put("inv_amt", checkNull(rs.getString("inv_amt")));
				InvoiceHrdMap.put("tran_id__fb", checkNull(rs.getString("tran_id__fb")));
				InvoiceHrdMap.put("inv_type", checkNull(rs.getString("inv_type")));
				InvoiceHrdMap.put("tax_amt", checkNull(rs.getString("tax_amt")));
				InvoiceHrdMap.put("disc_amt", checkNull(rs.getString("disc_amt")));
				InvoiceHrdMap.put("net_amt", checkNull(rs.getString("net_amt")));
				InvoiceHrdMap.put("curr_code", checkNull(rs.getString("curr_code")));
				InvoiceHrdMap.put("anal_code", checkNull(rs.getString("anal_code")));
				InvoiceHrdMap.put("frt_type", checkNull(rs.getString("frt_type")));
				InvoiceHrdMap.put("frt_amt", checkNull(rs.getString("frt_amt")));
				InvoiceHrdMap.put("curr_code__frt", checkNull(rs.getString("curr_code__frt")));
				InvoiceHrdMap.put("sales_pers", checkNull(rs.getString("sales_pers")));
				InvoiceHrdMap.put("comm_amt", checkNull(rs.getString("comm_amt")));
				InvoiceHrdMap.put("acct_code__sal", checkNull(rs.getString("acct_code__sal")));
				InvoiceHrdMap.put("cctr_code__sal", checkNull(rs.getString("cctr_code__sal")));
				InvoiceHrdMap.put("acct_code__ar", checkNull(rs.getString("acct_code__ar")));
				InvoiceHrdMap.put("cctr_code__ar", checkNull(rs.getString("cctr_code__ar")));
				InvoiceHrdMap.put("acct_code__dis", checkNull(rs.getString("acct_code__dis")));
				InvoiceHrdMap.put("cctr_code__dis", checkNull(rs.getString("cctr_code__dis")));
				InvoiceHrdMap.put("bank_code", checkNull(rs.getString("bank_code")));
				InvoiceHrdMap.put("adj_amount", checkNull(rs.getString("adj_amount")));
				InvoiceHrdMap.put("confirmed", checkNull(rs.getString("confirmed")));

				if( rs.getString("conf_date") != null )
				{  
					confDtStr=genericUtility.getValidDateString(rs.getString("conf_date"), genericUtility.getDBDateFormat(),
							genericUtility.getApplDateFormat());
				}
				InvoiceHrdMap.put("conf_date", confDtStr);

				InvoiceHrdMap.put("emp_code__aprv", checkNull(rs.getString("emp_code__aprv")));
				InvoiceHrdMap.put("remarks", checkNull(rs.getString("remarks")));

				chgDtStr=genericUtility.getValidDateString(rs.getString("chg_date"), genericUtility.getDBDateFormat(),
						genericUtility.getApplDateFormat());
				InvoiceHrdMap.put("chg_date", chgDtStr);

				InvoiceHrdMap.put("chg_user", checkNull(rs.getString("chg_user")));
				InvoiceHrdMap.put("chg_term", checkNull(rs.getString("chg_term")));
				InvoiceHrdMap.put("descr", checkNull(rs.getString("descr")));
				InvoiceHrdMap.put("finent_descr", checkNull(rs.getString("descr")));
				InvoiceHrdMap.put("site_descr", checkNull(rs.getString("descr")));
				InvoiceHrdMap.put("cust_name", checkNull(rs.getString("cust_name")));
				InvoiceHrdMap.put("city", checkNull(rs.getString("city")));
				InvoiceHrdMap.put("sp_name", checkNull(rs.getString("sp_name")));
				InvoiceHrdMap.put("exch_rate", checkNull(rs.getString("exch_rate")));
				InvoiceHrdMap.put("exch_rate__frt", checkNull(rs.getString("exch_rate__frt")));
				InvoiceHrdMap.put("agent_code", checkNull(rs.getString("agent_code")));
				InvoiceHrdMap.put("sagent_name", checkNull(rs.getString("sagent_name")));
				InvoiceHrdMap.put("round_adj", checkNull(rs.getString("round_adj")));
				InvoiceHrdMap.put("comm_amt__oc", checkNull(rs.getString("comm_amt__oc")));
				InvoiceHrdMap.put("sales_pers__1", checkNull(rs.getString("sales_pers__1")));
				InvoiceHrdMap.put("sales_pers__2", checkNull(rs.getString("sales_pers__2")));
				InvoiceHrdMap.put("sales_pers_comm_1", checkNull(rs.getString("sales_pers_comm_1")));
				InvoiceHrdMap.put("sales_pers_comm_2", checkNull(rs.getString("sales_pers_comm_2")));
				InvoiceHrdMap.put("sales_pers_comm_3", checkNull(rs.getString("sales_pers_comm_3")));
				InvoiceHrdMap.put("posttype", checkNull(rs.getString("posttype")));
				InvoiceHrdMap.put("stan_code__init", checkNull(rs.getString("stan_code__init")));
				InvoiceHrdMap.put("advance", checkNull(rs.getString("advance")));
				InvoiceHrdMap.put("salespers1_descr", checkNull(rs.getString("salespers1_descr")));
				InvoiceHrdMap.put("salespers2_descr", checkNull(rs.getString("salespers2_descr")));
				InvoiceHrdMap.put("doc_status", checkNull(rs.getString("doc_status")));
				InvoiceHrdMap.put("gp_no", checkNull(rs.getString("gp_no")));
				//Changed By Nasruddin 14-10-16  Strat
				//InvoiceHrdMap.put("gp_date", checkNull(rs.getString("gp_date")));
				//changed By Nasruddin khan 07-12-16 Start
				if(rs.getString("gp_date")!= null)
				{
					gpDtStr=genericUtility.getValidDateString(rs.getString("gp_date"), genericUtility.getDBDateFormat(),
							genericUtility.getApplDateFormat());	 
				}
				//changed By Nasruddin khan 07-12-16 END
				InvoiceHrdMap.put("gp_date", gpDtStr);
				//Changed By Nasruddin 14-10-16  End
				InvoiceHrdMap.put("sales_grp", checkNull(rs.getString("sales_grp")));
				InvoiceHrdMap.put("edi_addr", checkNull(rs.getString("edi_addr")));
				InvoiceHrdMap.put("adj_amount", checkNull(rs.getString("adj_amount")));
				InvoiceHrdMap.put("disc_schem_billback_amt", checkNull(rs.getString("disc_schem_billback_amt")));
				InvoiceHrdMap.put("disc_schem_offinv_amt", checkNull(rs.getString("disc_schem_offinv_amt")));
				InvoiceHrdMap.put("acc_code__order", checkNull(rs.getString("acc_code__order")));
				InvoiceHrdMap.put("disc_offinv_amt_hdr", checkNull(rs.getString("disc_offinv_amt_hdr")));
				InvoiceHrdMap.put("disc_billback_amt_hdr", checkNull(rs.getString("disc_billback_amt_hdr")));
				InvoiceHrdMap.put("acct_code__pr", checkNull(rs.getString("acct_code__pr")));
				InvoiceHrdMap.put("cctr_code__pr", checkNull(rs.getString("cctr_code__pr")));
				
				// added by nandkumar gadkari on 13/01/20-------------start----------
				InvoiceHrdMap.put("tran_id__rcv", checkNull(rs.getString("tran_id__rcv")));
				InvoiceHrdMap.put("bl_no", checkNull(rs.getString("bl_no")));
				if(rs.getString("bl_date") !=null )
				{
					blDtStr=genericUtility.getValidDateString(rs.getString("bl_date"), genericUtility.getDBDateFormat(),genericUtility.getApplDateFormat());
						InvoiceHrdMap.put("bl_date", blDtStr);
				}
				else
				{
					InvoiceHrdMap.put("bl_date", blDtStr);
				}
				InvoiceHrdMap.put("exch_rate__ins", checkNull(rs.getString("exch_rate__ins")));
				InvoiceHrdMap.put("curr_code__ins", checkNull(rs.getString("curr_code__ins")));
				InvoiceHrdMap.put("ins_amt", checkNull(rs.getString("ins_amt")));
				InvoiceHrdMap.put("print_status", checkNull(rs.getString("print_status")));
				InvoiceHrdMap.put("lc_ref", checkNull(rs.getString("lc_ref")));
				if(rs.getString("lc_date") !=null )
				{
					lcDtStr=genericUtility.getValidDateString(rs.getString("lc_date"), genericUtility.getDBDateFormat(),genericUtility.getApplDateFormat());
						InvoiceHrdMap.put("lc_date", lcDtStr);
				}
				else
				{
					InvoiceHrdMap.put("lc_date", lcDtStr);
				}
				InvoiceHrdMap.put("prd_code", checkNull(rs.getString("prd_code")));
				InvoiceHrdMap.put("recd_yn", checkNull(rs.getString("recd_yn")));
				
				InvoiceHrdMap.put("market_reg", checkNull(rs.getString("market_reg")));
				InvoiceHrdMap.put("delivered", checkNull(rs.getString("delivered")));
				if(rs.getString("delivery_date") !=null )
				{
					deliDtStr=genericUtility.getValidDateString(rs.getString("delivery_date"), genericUtility.getDBDateFormat(),genericUtility.getApplDateFormat());
						InvoiceHrdMap.put("delivery_date", deliDtStr);
				}
				else
				{
					InvoiceHrdMap.put("delivery_date", deliDtStr);
				}	
				InvoiceHrdMap.put("lr_no", checkNull(rs.getString("lr_no")));
				if(rs.getString("lr_date") !=null )
				{
					lrDtStr=genericUtility.getValidDateString(rs.getString("lr_date"), genericUtility.getDBDateFormat(),genericUtility.getApplDateFormat());
						InvoiceHrdMap.put("lr_date", lrDtStr);
				}
				else
				{
					InvoiceHrdMap.put("lr_date", lrDtStr);
				}	
				InvoiceHrdMap.put("tran_code", checkNull(rs.getString("tran_code")));
				InvoiceHrdMap.put("octroi_rcp_no", checkNull(rs.getString("octroi_rcp_no")));
				if(rs.getString("octroi_rcp_date") !=null )
				{
					octRcpDtStr=genericUtility.getValidDateString(rs.getString("octroi_rcp_date"), genericUtility.getDBDateFormat(),genericUtility.getApplDateFormat());
						InvoiceHrdMap.put("octroi_rcp_date", octRcpDtStr);
				}
				else
				{
					InvoiceHrdMap.put("octroi_rcp_date", octRcpDtStr);
				}	
				InvoiceHrdMap.put("no_art", checkNull(rs.getString("no_art")));
				InvoiceHrdMap.put("lorry_no", checkNull(rs.getString("lorry_no")));
				InvoiceHrdMap.put("trans_mode", checkNull(rs.getString("trans_mode")));
				InvoiceHrdMap.put("gross_weight", checkNull(rs.getString("gross_weight")));
				
				InvoiceHrdMap.put("parent__tran_id", checkNull(rs.getString("parent__tran_id")));
				InvoiceHrdMap.put("custstock_id", checkNull(rs.getString("custstock_id")));
				InvoiceHrdMap.put("inv_ackno", checkNull(rs.getString("inv_ackno")));
				InvoiceHrdMap.put("cd_tranno", checkNull(rs.getString("cd_tranno")));
				
				InvoiceHrdMap.put("tran_id__crn", checkNull(rs.getString("tran_id__crn")));
				InvoiceHrdMap.put("processed", checkNull(rs.getString("processed")));
				InvoiceHrdMap.put("download_flag", checkNull(rs.getString("download_flag")));
				InvoiceHrdMap.put("download_file_seq", checkNull(rs.getString("download_file_seq")));
				InvoiceHrdMap.put("edi_stat", checkNull(rs.getString("edi_stat")));
				InvoiceHrdMap.put("edi_stat_asn", checkNull(rs.getString("edi_stat_asn")));
				if(rs.getString("inv_exp_date") !=null )
				{
					invExpDtStr=genericUtility.getValidDateString(rs.getString("inv_exp_date"), genericUtility.getDBDateFormat(),genericUtility.getApplDateFormat());
						InvoiceHrdMap.put("inv_exp_date", invExpDtStr);
				}
				else
				{
					InvoiceHrdMap.put("inv_exp_date", invExpDtStr);
				}	
				// added by nandkumar gadkari on 13/01/20-------------end------------
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

		return InvoiceHrdMap;
	}
	//added confirm method by Varsha V on 16-08-18 to confirm the transaction on clicking button
	public String confirm(String invoiceId, String xtraParams, String forcedFlag) throws RemoteException,ITMException
	{
		System.out.println("----------------confirmed method (Invoice confirm)----through button-----");
		String retString = "";		
		Connection conn = null;
		ConnDriver connDriver = null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		try
		{
			conn = getConnection() ;
			itmDBAccessEJB = new ITMDBAccessEJB();
			retString = confirm( invoiceId, xtraParams, forcedFlag,conn);	//calling and creating connection	
			System.out.println("retString from confirm method of PostOrdInvoicePost:::"+retString);
			if( retString != null && retString.trim().length() > 0  && !retString.contains("VTINVCONF3"))
			{
				conn.rollback();
				return retString;
			}
			else
			{
				System.out.println("@@@@@@@@@@@118:::::::::::commiting record........");
				conn.commit();
				retString = itmDBAccessEJB.getErrorString("","CONFSUCCES","","",conn);
				return retString;
			}
		}
		catch(Exception exception)
		{
			try {
				conn.rollback();
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("Exception in [Despatch Confirmation] confirm " + exception.getMessage());
		}
		finally
		{
			try
			{
				if( conn != null)
				{
					conn.close();
					conn = null;
				}
			}
			catch(Exception ef)
			{
				System.out.println(ef.getMessage());
				ef.printStackTrace();
				throw new ITMException(ef);
			}
		}
		return retString;
	}
	public String confirm(String invoiceId,String xtraParams, String forcedFlag ,Connection conn) throws RemoteException,ITMException
	{
		String sql = "", retString = "", loginEmpCode = "", ediOption = "";
		String confirmed = "", errString = "", commJvConf = "", commDrcrConf = "",custCode="";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int updCnt = 0;
		//Added By PriyankaC on 16OCt2019.[Start]
		String toAddr = "",ccAddr = "",bccAddr = "",subject = "",body = "",templateName = "",attachObjLinks = "",attachments = "";
	    //String templateCode = "SALE INVOICE1" ;
		
		String templateCode  = finCommon.getFinparams("999999","GET_MAIL_FORMAT", conn);
	    String SendEmailOnNotify = "";
		String xmlString = "",reportType = "PDF",usrLevel = "";
		
	   //Added By PriyankaC on 16OCt2019.[END]
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		PostOrderActivity postordact = new PostOrderActivity();
		System.out.println("----------------confirmed method (Invoice confirm)---- with Connection-----");
		Calendar currentDate = Calendar.getInstance();
		boolean successFlag = false;
		String commCalcOnOff = "";
		try
		{
			//Added By PriyankaC on 16Oct2019 [START]
			DBAccessEJB dbAccess = new DBAccessEJB();
			String loginCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			UserInfoBean userInfo = dbAccess.createUserInfo(loginCode);
			
			sql = "select usr_lev from users where code = ? " ;
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, loginCode);
			rs = pstmt.executeQuery();
			if (rs.next()) 
			{
				usrLevel = checkNull(rs.getString("usr_lev"));
			    userInfo.setUserLevel(usrLevel);
			}
			else
			{
				userInfo.setUserLevel("0");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			//Added By PriyankaC on 16Oct2019 [END].
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String sysDateStr = sdf.format(currentDate.getTime());
			System.out.println("Now the date is :=>  " + sysDateStr);
			Timestamp todayDate = Timestamp.valueOf(genericUtility.getValidDateString(sysDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			System.out.println("xtraParams>>>"+xtraParams);
			loginEmpCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			
			//Modified by Anjali R. on [29/01/2019][This code has been shifted in invoicePosting method][Start]
			//Modified by Anjali R. on [23/01/2019][To Update tran_date as todays date in case of ledg_post_conf is "Y"][Start]
			/*String ledgPostConf = "";
			int count = 0;
			sql = "select ledg_post_conf from transetup where tran_window = 'w_invoice'";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				ledgPostConf = checkNull(rs.getString("ledg_post_conf"));
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
			if("Y".equalsIgnoreCase(ledgPostConf)) 
			{
				sql = "update invoice set tran_date = trunc(sysdate) where invoice_id = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, invoiceId);
				count  = pstmt.executeUpdate();
				System.out.println("count--["+count+"]");
			}*/
			//Modified by Anjali R. on [23/01/2019][To Update tran_date as todays date in case of ledg_post_conf is "Y"][End]
			//Modified by Anjali R. on [29/01/2019][This code has been shifted in invoicePosting method][End]
			//Modify By PriyankaC on 17Oct2019 [Start]
			//sql = "select confirmed from invoice where invoice_id = ?  ";
			sql = "select confirmed ,cust_code from invoice where invoice_id = ?  ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, invoiceId);
			rs = pstmt.executeQuery();
			if (rs.next()) 
			{
				confirmed = checkNull(rs.getString("confirmed"));
				custCode = checkNull(rs.getString("cust_code"));
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			if("Y".equalsIgnoreCase(confirmed))
			{
				errString = itmDBAccess.getErrorString("", "VTINVCONF2", "", "", conn);
				return errString;
			}
			forcedFlag="N";
			errString = invoicePosting(invoiceId, xtraParams, forcedFlag, conn);
			System.out.println("PostOrdInvoicePost invoicePosting return string >>>>"+errString);
			if( errString != null && errString.trim().length() > 0 )
			{
				return errString;
			}
			sql=" update invoice set confirmed = 'Y',conf_date = ?, emp_code__aprv = ?  where invoice_id = ? ";
			pstmt=conn.prepareStatement(sql);
			pstmt.setTimestamp(1,todayDate);
			pstmt.setString(2,loginEmpCode);
			pstmt.setString(3,invoiceId);
			updCnt = pstmt.executeUpdate();
			pstmt.close();
			pstmt=null;
			
			//Added By PriyankaC to send the mail on invoice confirmation to customer on 16Oct2019.[Start]
			sql = " select email_notify from customer where cust_code =  ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, custCode);
			rs = pstmt.executeQuery();
			if (rs.next()) 
			{
				SendEmailOnNotify = checkNull(rs.getString("email_notify"));
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if("Y".equalsIgnoreCase(SendEmailOnNotify))
			{
				System.out.println("After confirm Calling SendEmail");
				sql = "select  SEND_TO ,COPY_TO ,BLIND_COPY ,SUBJECT , BODY_TEXT , MAIL_DESCR ,ATTACH_TEXT ,ATTACH_TYPE  from MAIL_FORMAT  WHERE FORMAT_CODE = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, templateCode);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					toAddr = checkNull(rs.getString("SEND_TO"));
					ccAddr = checkNull(rs.getString("COPY_TO"));
					bccAddr = checkNull(rs.getString("BLIND_COPY"));
					subject = checkNull(rs.getString("SUBJECT"));
					body = checkNull(rs.getString("BODY_TEXT"));
					templateName = checkNull(rs.getString("MAIL_DESCR"));
					attachments	 = checkNull(rs.getString("ATTACH_TEXT"));
					attachObjLinks = checkNull(rs.getString("ATTACH_TYPE"));
					//confirmed = checkNull(rs.getString("confirmed"));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			    System.out.println("Before confirm Calling SendEmail with attachment");
				errString =  TransactionEmailTempltEJB.sendMail("invoice",userInfo,toAddr, ccAddr,bccAddr,subject,body,"","",invoiceId,attachments,"true",templateName,templateCode,"");
				//errString =  TransactionEmailTempltEJB.sendMail("invoice",userInfo,toAddr, ccAddr,bccAddr,subject,body,"","",invoiceId,attachments,"",templateName,templateCode,"");
				System.out.println("After confirm Calling SendEmail with attachment" +errString);
				
				 if( errString != null && errString.trim().length() > 0 )
				{
					 String begPart = errString.substring(errString.indexOf("<STATUS>")+8,errString.indexOf("</STATUS>"));
					 System.out.println("<STATUS> ::: " +begPart);
					 if("N".equalsIgnoreCase(begPart))
					 {
							return errString;
					 }
					 else
					 {
						 errString="";
					 }
				}
			}
			//Added By PriyankaC to send the mail on invoice confirmation to customer on 16Oct2019.[End]
			
			sql = "select edi_option from transetup where tran_window = ?  ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, "w_invoice");
			rs = pstmt.executeQuery();
			if (rs.next()) 
			{
				ediOption = rs.getString("edi_option");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("ediOption:["+ediOption+"]");
			System.out.println("invoiceId:["+invoiceId+"]");
			if("1".equalsIgnoreCase(ediOption))
			{
				CreateRCPXML createRCPXML = new CreateRCPXML("w_invoice", "tran_id");
				String dataStr = createRCPXML.getTranXML(invoiceId, conn);
				System.out.println("dataStr =[ " + dataStr + "]");
				Document ediDataDom = genericUtility.parseString(dataStr);
				System.out.println("xtraParams:["+xtraParams+"]");
				E12GenerateEDIEJB e12GenerateEDIEJB = new E12GenerateEDIEJB();
				retString = e12GenerateEDIEJB.nfCreateEdiMultiLogic(ediDataDom,"w_invoice", xtraParams);
				System.out.println("retString from E12GenerateEDIEJB before = ["+ retString + "]");
				if (retString != null && "SUCCESS".equalsIgnoreCase(retString)) 
				{
					System.out.println("retString from E12GenerateEDIEJB = ["+ retString + "]");
				}
			}
			//Pavan R 09apr19[to CalculateCommission based on COMM_CALC_ON_OFF finparm ]
			commCalcOnOff = finCommon.getFinparams("999999", "COMM_CALC_ON_OFF", conn);
			System.out.println("@@@@@1046 commCalcOnOff["+commCalcOnOff+"]");
			if("Y".equalsIgnoreCase(commCalcOnOff))					
			{
				CalculateCommission calCom=new CalculateCommission();
				commDrcrConf = checkNull(finCommon.getFinparams("999999", "COMM_DRCR_CONF", conn));
				commJvConf = checkNull(finCommon.getFinparams("999999", "COMM_JV_CONF", conn));
				errString =  calCom.CalCommission(invoiceId,"I","",commDrcrConf,commJvConf,xtraParams, conn);
			}
			if( errString != null && errString.trim().length() > 0 )
			{
				return errString;
			}
			if( errString == null || errString.trim().length() == 0 )
			{
				errString =  postordact.autoExciseDrNote(invoiceId,xtraParams, conn);
			}
			else
			{
				return errString;
			}
			System.out.println("errString in Invoice confirm===="+errString);
			if(errString.indexOf("Success") > -1)
			{
				errString = "";
			}
			System.out.println("errString in Invoice confirm===="+errString);
			if( errString == null || errString.trim().length() == 0)
			{
				System.out.println(">>The selected transaction is confirmed!!!!");
				errString="VTINVCONF3";
				System.out.println("@@@@@ retString:[" + errString+"]");
			}
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			System.out.println("Exception in [Despatch Confirmation] confirm " + exception.getMessage());
			throw new ITMException(exception); 
		}
		finally 
		{
			try {
					if (rs != null) {
						rs.close();
						rs = null;
					}
					if (pstmt != null) {
						pstmt.close();
						pstmt = null;
					}
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return errString;
	}
	//Ended confirm method by Varsha V on 16-08-18 to confirm the transaction on clicking button
	//added by nandkumar gadkari on 13/01/20-----------------------start---------------------
	public String gbfCreateInvHdrOth(String invoiceId, String siteCode ,String asRunOpt,String refDate,String xtraParams, Connection conn) throws ITMException
	{
		String retSting="",sysDate="",transer="",sql="",custCode="",taxEnvInv="",finEntity="",trnofld="",insertSql="";
		String loginEmpCode="",forcedFlag="";
		String lineNoRef="";
		double netAmt=0.0,taxAmt=0.0,netAmtTrace=0.0,taxAmtTrace=0.0,lcTotAmt=0.0,netAmtHdr=0,taxAmtHdr=0;
		PreparedStatement pstmt=null,pstmt1=null;
		ResultSet rs=null,rs1=null;
		int cnt=0,lsNo=0;
		UtilMethods utlMethods= new UtilMethods();
		Timestamp today=null;
		Timestamp tranDate = null,effDate=null,despDate=null,dueDate=null,taxDate=null,blDate=null,lcDate=null,delvDate=null,lrDate=null,octRcpDate=null,gpDate=null,invExpDate=null;
		String xmlValues="",newInvoiceId="",keystr="";
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB(); 
		HashMap<String, String>InvoiceDetMap=null;
		
		try
		{
			java.util.Date currentD = new java.util.Date();
			Calendar cal = Calendar.getInstance();
			cal.setTime(currentD);
			currentD = cal.getTime();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			sysDate = sdf.format(currentD);
			today= Timestamp.valueOf(genericUtility.getValidDateString(sysDate, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			transer = "S-INV";
			trnofld = "Invoice_id";
			HashMap<String, String> InvoiceHrdMap=new HashMap<String, String>();
			ArrayList<Object> invoiceDetList=new ArrayList<Object>();
		 

			try
			{
				sql ="select key_String from transetup where upper(tran_window) = 'W_INVOICE_PO' " ;
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					keystr = rs.getString("key_string");
				}
				else
				{
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					sql ="select key_String from transetup where upper(tran_window) = 'GENERAL' " ;
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						keystr = rs.getString("key_string");
					}

				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
				xmlValues = xmlValues + "<Header></Header>";
				xmlValues = xmlValues + "<Detail1>";
				xmlValues = xmlValues + "<tran_id></tran_id>";
				xmlValues = xmlValues + "<site_code>" + siteCode + "</site_code>";
				xmlValues = xmlValues + "<tran_date>" + sysDate + "</tran_date>";
				xmlValues = xmlValues +"</Detail1></Root>";
				//System.out.println("xmlValues :["+xmlValues+"]");
				TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", CommonConstants.DB_NAME);
				newInvoiceId = tg.generateTranSeqID("S-INV", "tran_id", keystr, conn);
				//System.out.println("nextID ["+newInvoiceId + "]");

			}catch(Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
			
			
			InvoiceHrdMap=getInvoiceHdr(invoiceId,conn);
			
			sql="select count(*) from site_customer where site_code__ch = ?	and site_code = ? and channel_partner = 'Y'";
			pstmt = conn.prepareStatement( sql );
			pstmt.setString( 1, siteCode );							
			pstmt.setString( 2, InvoiceHrdMap.get("site_code") );
			
			rs = pstmt.executeQuery();
			if( rs.next() )
			{
				cnt = rs.getInt(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			
			if(cnt == 0)
			{
				sql="select count(*) from customer where site_code = ? and case when channel_partner is null then 'N' else channel_partner end = 'Y'";
				pstmt = conn.prepareStatement( sql );
				pstmt.setString( 1, siteCode );							
							
				rs = pstmt.executeQuery();
				if( rs.next() )
				{
					cnt = rs.getInt(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
				if(cnt > 1)
				{
					
					retSting = itmDBAccessEJB.getErrorString("", "ERRORVTCPC", "","", conn);
					return retSting;
				}
				if(cnt == 0)
				{
					
					retSting = itmDBAccessEJB.getErrorString("", "VTCUSTCD4", "","", conn);
					return retSting;
				}
				if(cnt == 1)
				{
					sql="select cust_code  from customer where site_code = ?	and channel_partner = 'Y'";
					pstmt = conn.prepareStatement( sql );
					pstmt.setString( 1, siteCode );							
								
					rs = pstmt.executeQuery();
					if( rs.next() )
					{
						custCode = rs.getString(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				
				}
			}
			else if(cnt == 1)
			{
				sql="select cust_code from site_customer	where site_code__ch =?	and site_code =?	and channel_partner = 'Y' ";
				pstmt = conn.prepareStatement( sql );
				pstmt.setString( 1, siteCode );							
				pstmt.setString( 2, InvoiceHrdMap.get("site_code") );					
							
				rs = pstmt.executeQuery();
				if( rs.next() )
				{
					custCode = rs.getString(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}
			
			
			taxEnvInv=finCommon.getFinparams("999999", "SRETURN_TAX_ENV", conn);	
			if(taxEnvInv==null || taxEnvInv.trim().length()==0 || "NULLFOUND".equalsIgnoreCase(taxEnvInv))
			{
				taxEnvInv="";
			}
			
			
			sql="select fin_entity  from site where site_code = ? ";
			pstmt = conn.prepareStatement( sql );
			pstmt.setString( 1, siteCode );							
						
			rs = pstmt.executeQuery();
			if( rs.next() )
			{
				finEntity = rs.getString(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			if(finEntity == null || finEntity.trim().length()== 0)
			{
				retSting = itmDBAccessEJB.getErrorString("", "DS000", "","", conn);
				return retSting;
			}
			
			tranDate=Timestamp.valueOf(genericUtility.getValidDateString(InvoiceHrdMap.get("tran_date"), genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			if(InvoiceHrdMap.get("eff_date") != null )
			{
				effDate=Timestamp.valueOf(genericUtility.getValidDateString(InvoiceHrdMap.get("eff_date"), genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			}
			if(InvoiceHrdMap.get("desp_date") != null )
			{
				despDate=Timestamp.valueOf(genericUtility.getValidDateString(InvoiceHrdMap.get("desp_date"), genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			}
			if(InvoiceHrdMap.get("due_date") != null )
			{
				dueDate=Timestamp.valueOf(genericUtility.getValidDateString(InvoiceHrdMap.get("due_date"), genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			}
			if(InvoiceHrdMap.get("tax_date") != null )
			{
			
				taxDate=Timestamp.valueOf(genericUtility.getValidDateString(InvoiceHrdMap.get("tax_date"), genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			}
			if(InvoiceHrdMap.get("bl_date") != null )
			{
				blDate=Timestamp.valueOf(genericUtility.getValidDateString(InvoiceHrdMap.get("bl_date"), genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			}
			if(InvoiceHrdMap.get("lc_date") != null )
			{
				lcDate=Timestamp.valueOf(genericUtility.getValidDateString(InvoiceHrdMap.get("lc_date"), genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			}
			if(InvoiceHrdMap.get("delivery_date") != null )
			{
				delvDate=Timestamp.valueOf(genericUtility.getValidDateString(InvoiceHrdMap.get("delivery_date"), genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			}
			if(InvoiceHrdMap.get("lr_date") != null )
			{
				lrDate=Timestamp.valueOf(genericUtility.getValidDateString(InvoiceHrdMap.get("lr_date"), genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			}
			if(InvoiceHrdMap.get("octroi_rcp_date") != null )
			{
				octRcpDate=Timestamp.valueOf(genericUtility.getValidDateString(InvoiceHrdMap.get("octroi_rcp_date"), genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			}
			if(InvoiceHrdMap.get("gp_date") != null )
			{
				gpDate=Timestamp.valueOf(genericUtility.getValidDateString(InvoiceHrdMap.get("gp_date"), genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			}
			if(InvoiceHrdMap.get("inv_exp_date") != null )
			{
				invExpDate=Timestamp.valueOf(genericUtility.getValidDateString(InvoiceHrdMap.get("inv_exp_date"), genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			}
		
		netAmtHdr=InvoiceHrdMap.get("net_amt")== null ? 0 : Double.parseDouble(InvoiceHrdMap.get("net_amt"));
		taxAmtHdr=InvoiceHrdMap.get("tax_amt")== null ? 0 : Double.parseDouble(InvoiceHrdMap.get("tax_amt"));
		
		netAmtHdr=netAmtHdr-taxAmtHdr;
		sql = "insert into invoice ( INVOICE_ID,TRAN_DATE,FIN_ENTITY,EFF_DATE,TRAN_MODE,SALE_ORDER,DESP_ID,DESP_DATE," + 
					"SITE_CODE,CUST_CODE,CUST_CODE__BIL,ACCT_CODE__AR,CCTR_CODE__AR,CR_TERM,CR_DAYS,DUE_DATE," + 
					"TAX_CLASS,TAX_CHAP,TAX_ENV,INV_AMT,TAX_AMT,DISC_AMT,TAX_DATE,FRT_AMT,FRT_TYPE,NET_AMT," + 
					"CURR_CODE,EXCH_RATE,SALES_PERS,COMM_AMT,CURR_CODE__FRT,EXCH_RATE__FRT,CHG_DATE,CHG_USER,CHG_TERM," + 
					"CONFIRMED,ROUND_ADJ,TRAN_ID__RCV,BL_NO,BL_DATE,TRAN_ID__FB,REMARKS,ANAL_CODE," + 
					"ADJ_AMOUNT,INV_TYPE,ITEM_SER,ACCT_CODE__SAL,CCTR_CODE__SAL,ACCT_CODE__DIS,CCTR_CODE__DIS,BANK_CODE," + 
					"EMP_CODE__APRV,EXCH_RATE__INS,CURR_CODE__INS,INS_AMT,PRINT_STATUS,LC_REF,LC_DATE," + 
					"PRD_CODE,RECD_YN,MARKET_REG,DELIVERED,DELIVERY_DATE,AGENT_CODE,RATE__CLG,COMM_AMT__OC," + 
					"SALES_PERS__1,SALES_PERS__2,SALES_PERS_COMM_1,SALES_PERS_COMM_2,SALES_PERS_COMM_3,LR_NO,LR_DATE," + 
					"TRAN_CODE,OCTROI_RCP_NO,OCTROI_RCP_DATE,NO_ART,LORRY_NO,TRANS_MODE,GROSS_WEIGHT,POSTTYPE," + 
					"PARENT__TRAN_ID,CUSTSTOCK_ID,INV_ACKNO,CD_TRANNO,TRAN_ID__CRN,STAN_CODE__INIT,PROCESSED," + 
					"DOC_STATUS,GP_NO,GP_DATE,SALES_GRP,DOWNLOAD_FLAG,DOWNLOAD_FILE_SEQ,INV_EXP_DATE," + 
					"DISC_SCHEM_BILLBACK_AMT,DISC_SCHEM_OFFINV_AMT,DISC_BILLBACK_AMT_HDR,DISC_OFFINV_AMT_HDR," + 
					"ACC_CODE__ORDER,ACCT_CODE__PR,CCTR_CODE__PR,EDI_STAT,EDI_STAT_ASN)" + 
					"values" + 
					"(?,?,?,?,?,?,?,?," + //8
					"?,?,?,?,?,?,?,?," + //16
					"?,?,?,?,?,?,?,?,?,?," + //26
					"?,?,?,?,?,? ,? ,? ,?," +// 35
					"?,?,?,?,?,?,?,?,?," + //44
					"?,?,?,?,?,?,?," + //51
					"?,?,?,?,?,?,?," + //58
					"?,?,?,?,?,?,?,?," + //66
					"?,?,?,?,?,?,?," + //73
					"?,?,?,?,?,?,?,?," + //81
					"?,?,?,?,?,?,?," + //88
					"?,?,?,?,?,?,?," + //95
					"?,?,?,?," + //99
					"?,?,?,?,?)";//104
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, newInvoiceId);
				pstmt.setTimestamp(2,tranDate);
				pstmt.setString(3, finEntity);
				pstmt.setTimestamp(4, effDate);
				pstmt.setString(5, InvoiceHrdMap.get("tran_mode"));
				pstmt.setString(6, InvoiceHrdMap.get("sale_order"));
				pstmt.setString(7, InvoiceHrdMap.get("desp_id"));
				pstmt.setTimestamp(8, despDate);
				pstmt.setString(9, siteCode);
				pstmt.setString(10, custCode);
				pstmt.setString(11, custCode);
				pstmt.setString(12, InvoiceHrdMap.get("acct_code__ar"));
				pstmt.setString(13, InvoiceHrdMap.get("cctr_code__ar"));
				pstmt.setString(14, InvoiceHrdMap.get("cr_term"));
				pstmt.setString(15, InvoiceHrdMap.get("cr_days"));
				pstmt.setTimestamp(16, dueDate);
				pstmt.setString(17, InvoiceHrdMap.get("tax_class"));
				pstmt.setString(18, InvoiceHrdMap.get("tax_chap"));
				pstmt.setString(19, InvoiceHrdMap.get("tax_env"));
				pstmt.setString(20, InvoiceHrdMap.get("inv_amt"));
				pstmt.setDouble(21, 0.0);
				pstmt.setString(22, InvoiceHrdMap.get("disc_amt"));
				pstmt.setTimestamp(23, taxDate);
				pstmt.setString(24, InvoiceHrdMap.get("frt_amt"));
				pstmt.setString(25, InvoiceHrdMap.get("frt_type"));
				pstmt.setDouble(26, netAmtHdr);
				pstmt.setString(27, InvoiceHrdMap.get("curr_code"));
				pstmt.setString(28, InvoiceHrdMap.get("exch_rate"));
				pstmt.setString(29, InvoiceHrdMap.get("sales_pers"));
				pstmt.setString(30, InvoiceHrdMap.get("comm_amt"));
				pstmt.setString(31, InvoiceHrdMap.get("curr_code__frt"));
				pstmt.setString(32, InvoiceHrdMap.get("exch_rate__frt"));
				pstmt.setTimestamp(33, today);
				pstmt.setString(34, InvoiceHrdMap.get("chg_user"));
				pstmt.setString(35, InvoiceHrdMap.get("chg_term"));
				pstmt.setString(36, "N");
				pstmt.setDouble(37, 0.0);
				pstmt.setString(38, InvoiceHrdMap.get("tran_id__rcv"));
				pstmt.setString(39, InvoiceHrdMap.get("bl_no"));
				pstmt.setTimestamp(40, blDate);
				pstmt.setString(41, InvoiceHrdMap.get("tran_id__fb"));
				pstmt.setString(42, InvoiceHrdMap.get("remarks"));
				pstmt.setString(43, InvoiceHrdMap.get("anal_code"));
				pstmt.setString(44,InvoiceHrdMap.get("adj_amount"));
				pstmt.setString(45, InvoiceHrdMap.get("inv_type"));
				pstmt.setString(46, InvoiceHrdMap.get("item_ser"));
				pstmt.setString(47, InvoiceHrdMap.get("acct_code__sal"));
				pstmt.setString(48, InvoiceHrdMap.get("cctr_code__sal"));
				pstmt.setString(49, InvoiceHrdMap.get("acct_code__dis"));
				pstmt.setString(50,InvoiceHrdMap.get("cctr_code__dis"));
				pstmt.setString(51, InvoiceHrdMap.get("bank_code"));
				pstmt.setString(52, InvoiceHrdMap.get("emp_code__aprv"));
				pstmt.setString(53, InvoiceHrdMap.get("exch_rate__ins"));
				pstmt.setString(54, InvoiceHrdMap.get("curr_code__ins"));
				pstmt.setString(55, InvoiceHrdMap.get("ins_amt"));
				pstmt.setString(56,InvoiceHrdMap.get("print_status"));
				pstmt.setString(57, InvoiceHrdMap.get("lc_ref"));
				pstmt.setTimestamp(58,lcDate);
				pstmt.setString(59, InvoiceHrdMap.get("prd_code"));
				pstmt.setString(60, InvoiceHrdMap.get("recd_yn"));
				pstmt.setString(61, InvoiceHrdMap.get("market_reg"));
				pstmt.setString(62,InvoiceHrdMap.get("delivered"));
				pstmt.setTimestamp(63, delvDate);
				pstmt.setString(64, InvoiceHrdMap.get("agent_code"));
				pstmt.setString(65, InvoiceHrdMap.get("rate__clg"));
				pstmt.setString(66, InvoiceHrdMap.get("comm_amt__oc"));
				pstmt.setString(67,InvoiceHrdMap.get("sales_pers__1"));
				pstmt.setString(68, InvoiceHrdMap.get("sales_pers__2"));
				pstmt.setString(69, InvoiceHrdMap.get("sales_pers_comm_1"));
				pstmt.setString(70, InvoiceHrdMap.get("sales_pers_comm_2"));
				pstmt.setString(71, InvoiceHrdMap.get("sales_pers_comm_3"));
				pstmt.setString(72, InvoiceHrdMap.get("lr_no"));
				pstmt.setTimestamp(73,lrDate);
				pstmt.setString(74, InvoiceHrdMap.get("tran_code"));
				pstmt.setString(75, InvoiceHrdMap.get("octroi_rcp_no"));
				pstmt.setTimestamp(76,octRcpDate);
				pstmt.setString(77, InvoiceHrdMap.get("no_art"));
				pstmt.setString(78, InvoiceHrdMap.get("lorry_no"));
				pstmt.setString(79, InvoiceHrdMap.get("trans_mode"));
				pstmt.setString(80, InvoiceHrdMap.get("gross_weight"));
				pstmt.setString(81, InvoiceHrdMap.get("posttype"));
//				pstmt.setString(82, InvoiceHrdMap.get("parent__tran_id"));  //commented by manish mhatre on 21-jan-2020
				pstmt.setString(82, invoiceId); 						//added by manish mhatre on 21-jan-2020
				pstmt.setString(83, InvoiceHrdMap.get("custstock_id"));
				pstmt.setString(84, InvoiceHrdMap.get("inv_ackno"));
				pstmt.setString(85, InvoiceHrdMap.get("cd_tranno"));
				pstmt.setString(86, InvoiceHrdMap.get("tran_id__crn"));
				pstmt.setString(87, InvoiceHrdMap.get("stan_code__init"));
				pstmt.setString(88, InvoiceHrdMap.get("processed"));
				pstmt.setString(89, InvoiceHrdMap.get("doc_status"));
				pstmt.setString(90, InvoiceHrdMap.get("gp_no"));
				pstmt.setTimestamp(91, gpDate);
				pstmt.setString(92, InvoiceHrdMap.get("sales_grp"));
				pstmt.setString(93, InvoiceHrdMap.get("download_flag"));
				pstmt.setString(94, InvoiceHrdMap.get("download_file_seq"));
				pstmt.setTimestamp(95, invExpDate);
				pstmt.setString(96, InvoiceHrdMap.get("disc_schem_billback_amt"));
				pstmt.setString(97, InvoiceHrdMap.get("disc_schem_offinv_amt"));
				pstmt.setString(98, InvoiceHrdMap.get("disc_billback_amt_hdr"));
				pstmt.setString(99, InvoiceHrdMap.get("disc_offinv_amt_hdr"));
				pstmt.setString(100, InvoiceHrdMap.get("acc_code__order"));
				pstmt.setString(101, InvoiceHrdMap.get("acct_code__pr"));
				pstmt.setString(102, InvoiceHrdMap.get("cctr_code__pr"));
				pstmt.setString(103, InvoiceHrdMap.get("edi_stat"));
				pstmt.setString(104, InvoiceHrdMap.get("edi_stat_asn"));
				
				pstmt.executeUpdate();
				pstmt.close();
				pstmt = null;
			
			//pb code not migrated --------

/*lc_check_amt = 0
ls_status = ''
For ll_cnt = 1 to UpperBound(ls_check)			
	ls_descr = ls_check[ll_cnt]		
	ls_cr_policy  = f_get_token(ls_descr, "~t")
	ls_desp_id    = f_get_token(ls_descr, "~t")
	ls_sale_order = f_get_token(ls_descr, "~t")		
	lc_aprv_amt   = dec(f_get_token(ls_descr, "~t"))			
	//i_nvo_sales.gbf_credit_check_update(ls_sale_order,ls_cr_policy,0,'S',lc_check_amt,'C',ls_status) //Commented Ruchira 29/08/2k6, for invoice amt not getting passed.
	i_nvo_sales.gbf_credit_check_update(ls_sale_order,ls_cr_policy,lc_net_amt,'S',lc_check_amt,'C',ls_status) 	//Added Ruchira 29/08/2k6, to pass invoice amt.
	if lc_check_amt > 0 or ls_status = '' then	
		//Insert record for balance amount 	
		ll_row = ads_credit_check.InsertRow(0)
		ads_credit_check.SetItem(ll_row,'tran_type','S')
		ads_credit_check.SetItem(ll_row,'cr_policy', ls_cr_policy)
		ads_credit_check.SetItem(ll_row,'descr', ls_descr)
		ads_credit_check.SetItem(ll_row,'sale_order', ls_sale_order)
		ads_credit_check.SetItem(ll_row,'aprv_stat','F')
		ads_credit_check.SetItem(ll_row,'aprv_amt',0)
		ads_credit_check.SetItem(ll_row,'used_amt',0)	
	end if 
Next	
if UpperBound(ls_check) > 0 then
	ls_errcode = 'VTCREDIT02'

end if*/
			
			
			
			
				/*invoiceDetList=getInvoiceDet(invoiceId,conn);
			
				for(int itr=0;itr<invoiceDetList.size();itr++)
				{
					InvoiceDetMap=(HashMap<String, String>) invoiceDetList.get(itr);
			
				
				
				
				}*/
			
			insertSql="INSERT INTO invdet (invoice_id,line_no,sord_line_no,item_code,item_flg,item_descr,quantity,unit,rate,"+
					  "unit__rate,discount,tax_class,tax_chap,tax_env,tax_amt,net_amt,unit__std,conv__qty_stduom,"+
				      "quantity__stduom,conv__rtuom_stduom,rate__stduom,comm_amt,"+
				      "sord_no,no_art,disc_amt,item_code__ord,rate__clg,comm_amt__oc,sales_pers_comm_1,"+
				      "sales_pers_comm_2,sales_pers_comm_3,cust_item__ref,analysis3,down_payment,down_payment_int,"+
				      "inst_amount,inst_int_amount,no_of_inst,frequency,analysis1,analysis2,disc_schem_billback_amt,"+
				      "disc_schem_offinv_amt,acc_code__item,chg_date,chg_user,chg_term) "+
				      "values (?,?,?,?,?,?,?," + //7
				      "    ?,?,?,?,?,?,?,?,?," + //16
				      "    ?,?,?,?,?,?,?," +//23 
				      "    ?,?,?,?,?,?,?,?," + //31
				      "    ?,?,?,?,?,?," + //37
				      "    ?,?,?,?,?,?,?," + //44
				      "    ?,?,?)";	//47
			pstmt1 = conn.prepareStatement(insertSql);
			
			
			sql="select invoice_id,line_no,sord_line_no,item_code,item_flg,item_descr,quantity,unit,rate," + 
						"unit__rate,discount,tax_class,tax_chap,tax_env,tax_amt,net_amt,unit__std,conv__qty_stduom," + 
						"quantity__stduom,conv__rtuom_stduom,rate__stduom,comm_amt,sord_no,no_art,disc_amt," + 
						"item_code__ord,rate__clg,comm_amt__oc,sales_pers_comm_1,sales_pers_comm_2,sales_pers_comm_3," + 
						"cust_item__ref,analysis3,down_payment,down_payment_int,inst_amount,inst_int_amount,no_of_inst," + 
						"frequency,analysis1,analysis2,disc_schem_billback_amt,disc_schem_offinv_amt,acc_code__item,chg_date,chg_user,chg_term  " + 
						"from invdet " + 
						"where invoice_id =? ";
						
				pstmt = conn.prepareStatement( sql );
				pstmt.setString( 1, invoiceId );							
							
				rs = pstmt.executeQuery();
				while( rs.next() )
				{
					netAmt = rs.getDouble("net_amt");
					taxAmt = rs.getDouble("tax_amt");
					
					netAmt=netAmt-taxAmt;
					
					pstmt1.setString(1,newInvoiceId);
                    pstmt1.setString(2,rs.getString("line_no"));
                    pstmt1.setString(3,rs.getString("sord_line_no"));
                    pstmt1.setString(4,rs.getString("item_code"));
                    pstmt1.setString(5,rs.getString("item_flg"));
					pstmt1.setString(6,rs.getString("item_descr"));
                    pstmt1.setDouble(7,rs.getDouble("quantity"));
                    pstmt1.setString(8,rs.getString("unit"));
                    pstmt1.setDouble(9,rs.getDouble("rate"));
                    
                    pstmt1.setString(10,rs.getString("unit__rate"));
                    pstmt1.setDouble(11,rs.getDouble("discount"));
                    pstmt1.setString(12,rs.getString("tax_class"));
                    pstmt1.setString(13,rs.getString("tax_chap"));
					pstmt1.setString(14,rs.getString("tax_env"));
					pstmt1.setDouble(15,0);
                    pstmt1.setDouble(16,netAmt);
                    pstmt1.setString(17,rs.getString("unit__std"));
                    pstmt1.setDouble(18,rs.getDouble("conv__qty_stduom"));
					pstmt1.setDouble(19,rs.getDouble("quantity__stduom"));
					pstmt1.setDouble(20,rs.getDouble("conv__rtuom_stduom"));
                    pstmt1.setDouble(21,rs.getDouble("rate__stduom"));
                    pstmt1.setDouble(22,rs.getDouble("comm_amt"));
                    pstmt1.setString(23,rs.getString("sord_no"));
					pstmt1.setDouble(24,rs.getDouble("no_art"));
					pstmt1.setDouble(25,rs.getDouble("disc_amt"));
                    pstmt1.setString(26,rs.getString("item_code__ord"));
                    pstmt1.setDouble(27,rs.getDouble("rate__clg"));
					pstmt1.setDouble(28,rs.getDouble("comm_amt__oc"));
					pstmt1.setDouble(29,rs.getDouble("sales_pers_comm_1"));
                    pstmt1.setDouble(30,rs.getDouble("sales_pers_comm_2"));
                    pstmt1.setDouble(31,rs.getDouble("sales_pers_comm_3"));
					pstmt1.setString(32,rs.getString("cust_item__ref"));
					pstmt1.setString(33,rs.getString("analysis3"));
                    pstmt1.setDouble(34,rs.getDouble("down_payment"));
                    pstmt1.setDouble(35,rs.getDouble("down_payment_int"));
					pstmt1.setDouble(36,rs.getDouble("inst_amount"));
					pstmt1.setDouble(37,rs.getDouble("inst_int_amount"));
					pstmt1.setInt(38,rs.getInt("no_of_inst"));
                    pstmt1.setString(39,rs.getString("frequency"));
                    pstmt1.setString(40,rs.getString("analysis1"));
					pstmt1.setString(41,rs.getString("analysis2"));
					pstmt1.setDouble(42,rs.getDouble("disc_schem_billback_amt"));
                    pstmt1.setDouble(43,rs.getDouble("disc_schem_offinv_amt"));
                    pstmt1.setString(44,rs.getString("acc_code__item"));
                    pstmt1.setTimestamp(45,rs.getTimestamp("chg_date"));
                    pstmt1.setString(46,rs.getString("chg_user"));
					pstmt1.setString(47,rs.getString("chg_term"));
					pstmt1.addBatch();
					pstmt1.clearParameters();
                   
               //pb code not migrated 
                    
                    /*  if len(trim(ls_tax_env_inv)) > 0 then
                    inv_det.retrieve(ls_invoice_id_new , ll_line_no)
//                    ls_filter = "tran_ser = '"+mtranser+"' and vouch_no='"+mvouchno+"'"
//                    lds_det.setfilter(ls_filter)
//                    lds_det.filter()            
                    ds_tax_detbrow.reset()
                    lc_tax_amtdet = gf_calc_tax_ds(inv_det,ds_tax_detbrow,'S-INV',ls_invoice_id_new,ld_tran_date,"rate__stduom", "quantity__stduom",1,ls_curr_code,'3')  
                    
                    if ds_tax_detbrow.update() <> 1 then
                        i++
                        populateerror(9999,'populateerror')
                        ls_errcode = 'DS000' + string(sqlca.sqldbcode)+' ~t'+sqlca.sqlerrtext
                        ls_errcode = gf_error_location(ls_errcode)
                        exit                 
                    end if            
                    
                end if        
                /// for tax calculation        
                IF isnull(lc_tax_amtdet) then lc_tax_amtdet = 0
                if isnull(lc_tax_amtdet) or lc_tax_amtdet = -999999999 then lc_tax_amtdet = 0             
                 IF isnull(lc_net_amt) then lc_net_amt = 0
                 IF isnull(lc_tottax) then lc_tottax = 0
                 IF isnull(lc_tot_net_amt) then lc_tot_net_amt = 0
                 IF isnull(lc_totdiscamt) then lc_totdiscamt = 0    
                lc_tot_net_amt = lc_tot_net_amt+ lc_net_amt + lc_tax_amtdet  - lc_disc_amt
                lc_tottax = lc_tottax + lc_tax_amtdet
//                mtotamt = lc_net_amt - lc_disc_amt   
                lc_totdiscamt = lc_totdiscamt + lc_disc_amt

                update invdet
                set tax_amt = :lc_tax_amtdet,
                     net_amt = (net_amt + (:lc_tax_amtdet - :lc_disc_amt))
                where INVOICE_ID = :ls_invoice_id_new
            	 and LINE_NO = :ll_line_no ;*/
                    
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				pstmt1.executeBatch();
				pstmt1.close();
                pstmt1=null;
                 
                 
                insertSql="INSERT INTO invoice_trace (invoice_id,inv_line_no,desp_id,desp_line_no,item_code,quantity,unit,rate," + 
                		"    unit__rate,line_no,discount,tax_class,tax_chap,tax_env,tax_amt,net_amt,unit__std,conv__qty_stduom," + 
                		"    quantity__stduom,conv__rtuom_stduom,rate__stduom,comm_amt,sord_no,sord_line_no," + 
                		"    item_code__ord,no_art,lot_no,lot_sl,site_code__mfg,mfg_date,exp_date" + 
                		"    ,exp_lev,fob_value,rate__clg,item_ser__prom,curr_code,exch_rate,comm_amt__oc," + 
                		"    rate__stk,rate__std,cost_rate,line_type,cust_item__ref,disc_schem_billback_amt,disc_schem_offinv_amt,chg_user,chg_term,chg_date)     " + 
                		"    VALUES(?,?,?,?,?,?,?,?," + //8
                		"    ?,?,?,?,?,?,?,?,?," + //17
                		"    ?,?,?,?,?,?," +// 23
                		"    ?,?,?,?,?,?,?,?,?,?" + //33
                		"    ,?,?,?,?,?,?,?,?," + //41
                		"    ?,?,?,?,?,?,?)";//48
  			pstmt1 = conn.prepareStatement(insertSql);
  			
  			
  			sql="select invoice_id,inv_line_no,desp_id,desp_line_no,item_code,quantity,unit,rate," + 
  					"unit__rate,line_no,discount,tax_class,tax_chap,tax_env,tax_amt,net_amt,unit__std,conv__qty_stduom," + 
  					"quantity__stduom,conv__rtuom_stduom,rate__stduom,comm_amt,sord_no,sord_line_no," + 
  					"item_code__ord,no_art,lot_no,lot_sl,site_code__mfg,mfg_date,exp_date" + 
  					",exp_lev,fob_value,rate__clg,item_ser__prom,curr_code,exch_rate,comm_amt__oc," + 
  					"rate__stk,rate__std,cost_rate,line_type,cust_item__ref,disc_schem_billback_amt,disc_schem_offinv_amt,chg_date,chg_user,chg_term  " + 
  					"from invoice_trace " + 
  					"where invoice_id =  ?  ";
  						
  				pstmt = conn.prepareStatement( sql );
  				pstmt.setString( 1, invoiceId );							
  							
  				rs = pstmt.executeQuery();
  				while( rs.next() )
  				{
  					netAmtTrace = rs.getDouble("net_amt");
  					taxAmtTrace = rs.getDouble("tax_amt");
  					
  					netAmtTrace=netAmtTrace-taxAmtTrace;
  					
					pstmt1.setString(1, newInvoiceId);
					pstmt1.setString(2, rs.getString("inv_line_no"));
					pstmt1.setString(3, rs.getString("desp_id"));
					pstmt1.setString(4, rs.getString("desp_line_no"));
					pstmt1.setString(5, rs.getString("item_code"));
					pstmt1.setDouble(6, rs.getDouble("quantity"));
					pstmt1.setString(7, rs.getString("unit"));
					pstmt1.setDouble(8, rs.getDouble("rate"));
					pstmt1.setString(9, rs.getString("unit__rate"));
	
					pstmt1.setString(10, rs.getString("line_no"));
					pstmt1.setDouble(11, rs.getDouble("discount"));
					pstmt1.setString(12, rs.getString("tax_class"));
					pstmt1.setString(13, rs.getString("tax_chap"));
					pstmt1.setString(14, rs.getString("tax_env"));
					pstmt1.setDouble(15, 0);
					pstmt1.setDouble(16, netAmtTrace);
					pstmt1.setString(17, rs.getString("unit__std"));
					pstmt1.setDouble(18, rs.getDouble("conv__qty_stduom"));
					pstmt1.setDouble(19, rs.getDouble("quantity__stduom"));
					pstmt1.setDouble(20, rs.getDouble("conv__rtuom_stduom"));
					pstmt1.setDouble(21, rs.getDouble("rate__stduom"));
					pstmt1.setDouble(22, rs.getDouble("comm_amt"));
					pstmt1.setString(23, rs.getString("sord_no"));
					pstmt1.setString(24, rs.getString("sord_line_no"));
					pstmt1.setString(25, rs.getString("item_code__ord"));
					pstmt1.setDouble(26, rs.getDouble("no_art"));
					pstmt1.setString(27, rs.getString("lot_no"));
					pstmt1.setString(28, rs.getString("lot_sl"));
					pstmt1.setString(29, rs.getString("site_code__mfg"));
	
					pstmt1.setTimestamp(30, rs.getTimestamp("mfg_date"));
					pstmt1.setTimestamp(31, rs.getTimestamp("exp_date"));
	
					pstmt1.setString(32, rs.getString("exp_lev"));
					pstmt1.setString(33, rs.getString("fob_value"));
					pstmt1.setDouble(34, rs.getDouble("rate__clg"));
					pstmt1.setString(35, rs.getString("item_ser__prom"));
					pstmt1.setString(36, rs.getString("curr_code"));
					pstmt1.setDouble(37, rs.getDouble("exch_rate"));
					pstmt1.setDouble(38, rs.getDouble("comm_amt__oc"));
					pstmt1.setDouble(39, rs.getDouble("rate__stk"));
					pstmt1.setDouble(40, rs.getDouble("rate__std"));
					pstmt1.setDouble(41, rs.getDouble("cost_rate"));
	
					pstmt1.setString(42, rs.getString("line_type"));
					pstmt1.setString(43, rs.getString("cust_item__ref"));
	
					pstmt1.setDouble(44, rs.getDouble("disc_schem_billback_amt"));
					pstmt1.setDouble(45, rs.getDouble("disc_schem_offinv_amt"));
					pstmt1.setString(46, rs.getString("chg_user"));
					pstmt1.setString(47, rs.getString("chg_term"));
					pstmt1.setTimestamp(48, rs.getTimestamp("chg_date"));
  					pstmt1.addBatch();
  					pstmt1.clearParameters();
                     
                
  				}
  				rs.close();
  				rs = null;
  				pstmt.close();
  				pstmt = null;
  				pstmt1.executeBatch();
  				pstmt1.close();
                  pstmt1=null;
             
                  forcedFlag="N";
                  retSting= invoicePosting(newInvoiceId, xtraParams, forcedFlag,conn);
                  loginEmpCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
                  if(retSting==null || retSting.trim().length() ==0)
	              {
	                  sql="update invoice set confirmed = 'Y',conf_date=?, emp_code__aprv = ?  where invoice_id = ? ";
						pstmt=conn.prepareStatement(sql);
						pstmt.setTimestamp(1,today);
						pstmt.setString(2,loginEmpCode);
						pstmt.setString(3,newInvoiceId);
						pstmt.executeUpdate();
						pstmt.close();
						pstmt=null;
	              }
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


		return retSting;
	}
	//added by nandkumar gadkari on 13/01/20-----------------------end---------------------
	private String checkNull(String inputStr)
	{
		// TODO Auto-generated method stub
		if(inputStr==null)
		{
			inputStr="";
		}
		return inputStr;
	}
	private String checkNullSpace(String inputStr)
	{
		// TODO Auto-generated method stub
		if(inputStr==null || inputStr.trim().length() == 0 )
		{
			inputStr=" ";
		}
		return inputStr;
	}

}