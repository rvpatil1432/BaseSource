package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.ejb.Stateless;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ibase.system.config.ConnDriver;
import ibase.utility.BaseLogger;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.sys.UtilMethods;
import ibase.webitm.utility.ITMException;

@Stateless
public class PoReturnIC extends ValidatorEJB implements PoReturnICLocal,
PoReturnICRemote {

	E12GenericUtility genericUtility = new E12GenericUtility();
	FinCommon finCommon = new FinCommon();    
	public String wfValData(String xmlString, String xmlString1,
			String xmlString2, String objContext, String editFlag,
			String xtraParams) throws RemoteException, ITMException {
		String errString = "";
		System.out.println("wfValdata() called for PoReturnIc:");
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try {
			if (xmlString != null && xmlString.trim().length() > 0) {
				dom = parseString(xmlString);
				System.out.println("xmlString[" + xmlString + "]");
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0) {
				dom1 = parseString(xmlString1);
				System.out.println("xmlString1[" + xmlString1 + "]");
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0) {
				dom2 = parseString(xmlString2);
				System.out.println("xmlString2[" + xmlString2 + "]");
			}
			errString = wfValData(dom, dom1, dom2, objContext, editFlag,
					xtraParams);
		} catch (Exception e) {
			throw new ITMException(e);
		}
		return (errString);
	}

	public String wfValData(Document dom, Document dom1, Document dom2,String objContext, String editFlag, String xtraParams)throws RemoteException, ITMException 
	{


		String childNodeName = null;
		String errString = "";
		String errCode = "";
		String userId = "";
		String sql = "";
		//int cnt = 0;
		int ctr = 0;
		int childNodeListLength;
		NodeList parentNodeList = null;

		//GENERATE_LOT_NO_AUTO
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement pstmt = null,pstmt1=null;
		ResultSet rs = null,rs1=null;
		ConnDriver connDriver = new ConnDriver();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");

		int currentFormNo = 0, recCnt = 0;
		FinCommon fincommon = new FinCommon();
		DistCommon discommon = new DistCommon();
		//		Timestamp mdate1 = null, mdate2 = null, ldt_date = null, ordDate = null,trandatePO1=null,Date1=null,Date2=null;
		String tranDateStr="",siteCode="",modName="",purcOrder="",isNullPo="",tranidPO="",tranDateStrPO="";
		String status="",empCodeaprv="",ordDate1="",lsconfirmed="",lsproviid="",lspotype="",tranType="",cwipTranType="",porcpTranType="";
		String lsitemcode="",prdCode="",siteCodlogin="",statFin="",errorType="";
		String itemSer="",grNo="",tranidGno="",currCode="",tranCode="",dcNo="";
		double llquantity=0,llrate=0;
		/*String lsmsg="",startStr="",endStr="",descrStr="",descrStart="",descrEnd="",value="";*/
		ArrayList lcstdqty = null;
		String sitecodemfg="",itemcode="",sitecode="",suppsour="";
		String unitrate="",unitstd="",lcconvqtystduom1="",lsstkopt="";
		double lcconvqtystduom=0;
		String acctCodeCr="",expiryDate=""; 
		//String lcconvqtystduom1="";
		//double lcconvqtystduom=0;
		String unit="",unitStd="",rateDom="",itemCode="",priceList="";
		double convQtyStduom=0,rate=0;
		String quantity="",purcorder="",qtybrowStr="",lcvariencevalue1="",linenoord="",lineno="",tranid="",temppono="",templinenoord="",templineno="";
		double qty=0,lcrcpqty=0,qtybrow=0,currqty=0,lcdlvqty=0,lcordqty=0,ldqtytol=0,lcvariencevalue=0;
		double lcqty=0 ,lcqtystd =0,lcrate=0,cratestd =0,lcvalue=0,lcvolume=0;
		String  lcqty1="" ,lcqtystd1 ="",lcrate1="",cratestd1 ="";
		String lsunit="",lsunitstd="",lsunitrate="",lsvariencetype="",lslorryno="";
		int noOfParent = 0;
		String lineNoOrd="",tranIdRef="";
		String mfgDate="";
		Timestamp dcDate1 = null, recDate1 =null;
		String dcDate="",recDate="",retOpt="";
		String suppCode="",tranId="",suppCodeShip="";
		int cnt1=0;
		String 	lc_qty_std1="";
		//double qty=0;
		String trandateStr="",lineNoItem="";
		int llexists=0;
		String channelPartner="",siteCodeCh="",suppchannelPartner="",suppSiteCode="";
		String lsqcreqd="",mstatus2="",channelpartner="",sitecodech="";

		String tranIdRcp="",tranDate="",tranIdHdr="",acceptCriteria="",confirmed="",suppCodeHdr="",currCodeHdr="",acceptCriteria1="",lsvalue="";
		String acctCodeDr="",reasCode="";
		String packcode="";
		String POlineNo="",site="",poSuppCode="",poCurrCode="",hdrCurr_code="",supp_Code="",emp_code__aprv="";

		Timestamp trandate1 = null;
		String invoiceNo="",invoiceDate="",tranSer="";
		Timestamp invoiceDate1=null;
		//String tranDate="";
		//Timestamp dcDate1=null;
		int cc=0, xx=0, initialField = 1, lastField = 1, fldcase = 0;
		String 	fldname = "",Errcode="", mVal="",mVal1="", fldnm="", mstat="", mVal2="";
		String	msite="",err="",mval3="",ls_loc="",ls_lotno="",ls_lotsl="",ls_item="";
		String	mitem_ser="",msupp="",mstatus1="",ls_cp="",ls_cp_site="",ls_retopt="",ls_suppcode="";
		String	ls_channelpartner="",ls_stkopt="",ls_tranid="",group_ship="",group_supp="",ls_value="",ls_sitecode="";
		String	sFieldNo = "0", ls_purc_order="",ls_supp_code="",ls_form_no="",ls_eou="",ls_status="",ls_lot_no_old="";
		String	ls_lot_sl_old="", ls_loc_code_old="", ls_line_no="" ,ls_stock_lotno="",ls_item_ser="" ,ls_site_supplier="" ,ls_site="",ls_invno="",ls_supp="",ls_msg_type="",ls_win="";
		boolean purcStatus = false; //Added By PriyankaC on 06JUNE2018..
		//datetime mdate1, mdate2, ldt_date
		Timestamp mdate1 = null, mdate2 = null, ldt_date = null,TranDate=null;
		String poGno="",supp_code="";
		Timestamp tran_date=null;
		long cnt = 0, cntc = 0,ll_sr,ll_cnt=0, ll_sel_row=0 , li_return=0 ,ll_ret=0;
		String cctrCodeDr="",cctrCodeCr="";    //added by manish mhatre
		/*  decimal{3} mqty,mqty1,mqty2,mperc,ld_qtytol,lc_rcp_qty,lc_old_qty,lc_qty,&
		  lc_stk_qty,lc_qty_std, lc_order_qty , lc_porcpqty, lc_ct3_qty, lc_qty_used
		 */
		double mqty=0.0,mqty1=0.0,mqty2=0.0,mperc=0.0,ld_qtytol=0.0,lc_rcp_qty=0.0,lc_old_qty=0.0,lc_qty=0.0;
		double lc_stk_qty=0.0,lc_qty_std=0.0, lc_order_qty=0.0 , lc_porcpqty=0.0, lc_ct3_qty=0.0, lc_qty_used=0.0;
		String lsmsg="",startStr="",endStr="",descrStr="",descrStart="",descrEnd="",value="";
		String lineNoRcp = "";//Added by sarita on 16 OCT 2018
		int lineCnt = 0;//Added by sarita on 24 OCT 2018
		double qty_available = 0.0;
		try {
			System.out.println("@@@@@@@@ wfvaldata called");
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			connDriver = null;
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			siteCodlogin =(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode"));
			if (objContext != null && objContext.trim().length() > 0) 
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			switch (currentFormNo) 
			{
			case 1:
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();

				for (ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					System.out.println("childNodeName"+childNodeName);
					if (childNodeName.equalsIgnoreCase("tran_date"))
					{
						SimpleDateFormat dateFormat2=null;
						dateFormat2 = new SimpleDateFormat(genericUtility.getApplDateFormat());
						tranDateStr = (genericUtility.getColumnValue("tran_date", dom));
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						System.out.println("@@@@ Tran Date[" + tranDateStr + "]");
						TranDate = Timestamp.valueOf(genericUtility.getValidDateString(tranDateStr.toString(),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");
						System.out.println("Trandate is"+TranDate);
						//Changes and Commented By Ajay on 20-12-2017 :START
						//errCode = this.nfCheckPeriod("PUR", TranDate,siteCode);
						errCode = finCommon.nfCheckPeriod("PUR", TranDate,siteCode, conn);
						//Changes and Commented By Ajay on 20-12-2017 :END
						System.out.println("Errorcode in TranDate"+errCode);
						if (errCode != null && errCode.trim().length() > 0)
						{
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						System.out.println("trandate Success..");
					}else if (childNodeName.equalsIgnoreCase("item_ser")) 
					{
						itemSer = checkNull(genericUtility.getColumnValue("item_ser",dom));

						System.out.println("Item Series--->["+itemSer+"]");
						if(itemSer == null || itemSer.length()==0)
						{
							errCode = "VTITEMSER5";
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						else
						{
							sql= "select count(*) from itemser where item_ser = ?";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1,itemSer);
							rs=pstmt.executeQuery();
							if(rs.next()){
								cnt=rs.getInt(1);
							}
							if(rs!=null){
								rs.close();
								rs=null;
							}
							if(pstmt!=null){
								pstmt.close();
								pstmt=null;
							}
							System.out.println("Count value for Item Series--->"+cnt);	
							if(cnt == 0)
							{
								errCode = "VMITEMSER1"; // item series value must exist in item series
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
						}

					}else if (childNodeName.equalsIgnoreCase("site_code")) 
					{
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						System.out.println("@@@@ site_code[" + siteCode	+ "]");
						errCode = this.isSiteCode(siteCode, modName);
						System.out.println("SiteCode Error code is"+errCode);
						if (errCode != null && errCode.trim().length() > 0) 
						{
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}//end case site_code
					else if (childNodeName.equalsIgnoreCase("purc_order")) 
					{
						purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom));
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						tranidPO = genericUtility.getColumnValue("tran_id", dom);
						if(purcOrder != null && purcOrder.trim().length() > 0)
						{

							//sql = "	select count(*) from porder where purc_order = ? ";
							sql="select count(*)  from Porder where purc_order = ? and confirmed = 'Y' ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, purcOrder);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0) 
							{
								errCode = "VTPORD3";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else if(cnt > 0)
							{
								sql="select status,emp_code__aprv from porder where purc_order =?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, purcOrder);
								rs = pstmt.executeQuery();
								if (rs.next()) 
								{
									status=rs.getString(1);
									empCodeaprv=rs.getString(2);

								}
								System.out.println("status@@"+status);
								System.out.println("empCodeaprv@@"+empCodeaprv);
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								System.out.println("status"+status);
								System.out.println("empCodeaprv"+empCodeaprv);

								if(status == null || status.trim().length() == 0)
								{
									status="O";
								}

								System.out.println("status"+status);


								if( "O".equals(status) && (empCodeaprv==null|| empCodeaprv.trim().length() == 0))
								{
									errCode = "VTPONAPRV";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}

							}
						}

					}//end case purchase order
					else if (childNodeName.equalsIgnoreCase("supp_code")) 
					{
						String lspo="";
						mVal = checkNull(genericUtility.getColumnValue("supp_code", dom));
						System.out.println("Supp code in Dom "+mVal);
						mVal1 = checkNull(genericUtility.getColumnValue("site_code", dom)); 
						System.out.println("Supp code in Dom "+mVal1);
						lspo = checkNull(genericUtility.getColumnValue("purc_order", dom));
						System.out.println("Supp code in Dom "+lspo);
						errCode = fincommon.isSupplier(mVal1, mVal,"", conn);
						System.out.println("Return Supp Code"+errCode);
						if (errCode != null && errCode.trim().length() > 0)
						{
							System.out.println("Inside wfval supp_code errcode>>> "+ errCode);
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						} 
						sql="select supp_code from porder where purc_order = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lspo);
						rs = pstmt.executeQuery();
						while (rs.next()) 
						{
							mval3= checkNull(rs.getString(1));

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						System.out.println("mVal Supplier COde["+mVal+"]-----mval3 Supplier Code["+ mval3+"]");

						if(!mVal.trim().equalsIgnoreCase(mval3.trim()))
						{
							errCode="VTSUPP3";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());

						}	
					}
					else if (childNodeName.equalsIgnoreCase("tran_id__ref")) 
					{
						Timestamp TranDatedOM=null;
						tranIdRef = checkNull(genericUtility.getColumnValue("tran_id__ref", dom));
						System.out.println("@@@@ gr_no[" + grNo	+ "]");
						suppCode= genericUtility.getColumnValue("supp_code", dom);
						System.out.println("@@@@ tran_id[" + tranidGno	+ "]");
						poGno = checkNull(genericUtility.getColumnValue("purc_order", dom));
						tranDateStr = checkNull(genericUtility.getColumnValue("tran_date", dom));
						System.out.println("tranDateStr@Dom["+tranDateStr+"]");
						if(tranDateStr!=null)
						{
							TranDatedOM = Timestamp.valueOf(genericUtility.getValidDateString(tranDateStr.toString(),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");
						}
						System.out.println("TranDatedOM@Dom["+TranDatedOM+"]");
						if( tranIdRef!= null && tranIdRef.trim().length() > 0)
						{
							sql="select count(*)  from porcp where tran_id = ? and confirmed = 'Y' "; 
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, tranIdRef);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("Count in Gate Register is"+cnt);
							if(cnt ==0)
							{
								errCode = "VTTRANID";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());	

							}
							sql="	select supp_code,tran_date from porcp where  tran_id = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, tranIdRef);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								supp_code=rs.getString(1);
								tran_date=rs.getTimestamp(2);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("gr_no--# cnt > 0"+cnt);
							System.out.println("tran_date@Dom["+tran_date+"]");
							if(tranDateStr!=null)
							{
								TranDate = Timestamp.valueOf(genericUtility.getValidDateString(tranDateStr.toString(),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");
							}
							System.out.println("tran_date@Stamp["+TranDate+"]");
							if (!supp_code.trim().equalsIgnoreCase(suppCode.trim()))
							{		System.out.println("supp_code@if");
							errCode = "VTNOTSUPP";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
							}else if(TranDate.before(tran_date))//
							{		
								System.out.println("TranDatedOM@id-else");
								errCode = "VTRETDT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());	

							}

						}else
						{
							/*ls_value = discommon.gf_getenv_dis('999999', 'RCP_REQUIRED')*///--pravin
							ls_value=discommon.getDisparams("999999","RCP_REQUIRED", conn);
							System.out.println("ls_value@@"+ls_value);
							if(ls_value.equalsIgnoreCase("Y"))
							{
								errCode = "VTINVRCP";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());	
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("curr_code")) 
					{
						currCode = checkNull(genericUtility.getColumnValue("curr_code", dom));
						sql="select count(*) from currency where curr_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, currCode);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							cnt = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("curr_code"+currCode);
						System.out.println("curr_code--# cnt > 0"+cnt);
						if(cnt ==0)
						{
							errCode = "VTCURRCD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
						}
					}
					else if (childNodeName.equalsIgnoreCase("tran_code")) 
					{
						tranCode = checkNull(genericUtility.getColumnValue("tran_code", dom));
						System.out.println("@@@@ tranCode[" + tranCode	+ "]");
						if(tranCode != null && tranCode.trim().length() > 0)
						{
							sql="select count(*) from transporter where tran_code =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, tranCode);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("COunt trancode is"+cnt);
							if(cnt==0)
							{
								errCode = "VTTRANCD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}else
						{
							mVal = checkNull(genericUtility.getColumnValue("supp_code", dom));
							System.out.println("Supp code in Dom "+mVal);
							mVal1 = checkNull(genericUtility.getColumnValue("site_code", dom)); 
							System.out.println("Supp code in Dom "+mVal1);
							sql="select channel_partner	from site_supplier " +
									"	where site_code = ? and supp_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, mVal1);
							pstmt.setString(2, mVal);

							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								ls_channelpartner = rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(ls_channelpartner==null||ls_channelpartner.trim().length()==0)
							{
								sql="select case when channel_partner is null then 'N' else channel_partner end  from supplier where supp_code = ?" ; 
								//mVal
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, mVal);

								rs = pstmt.executeQuery();
								if (rs.next()) 
								{
									ls_channelpartner = rs.getString(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;		
								if(ls_channelpartner.equalsIgnoreCase("Y"))		
								{
									errCode = "VMTRANCD1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}	
							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("supp_code__ship")) 
					{	

						suppCodeShip = checkNull(genericUtility.getColumnValue("supp_code__ship", dom));
						System.out.println("@@@@ suppCodeShip[" + suppCodeShip	+ "]");
						mVal = checkNull(genericUtility.getColumnValue("supp_code", dom));
						System.out.println("Supp code in Dom "+mVal);
						mVal1 = checkNull(genericUtility.getColumnValue("site_code", dom)); 
						System.out.println("Supp code in Dom "+mVal1);

						//errcode = discommon.gbf_supplier(mval1,mval,transer)

						//errCode=discommon.
						errCode = fincommon.isSupplier(mVal1, mVal,"P-RET", conn);
						System.out.println("errCode@@@@"+errCode);
						if( errCode== null || errCode.trim().length()==0)
						{
							sql="select group_code  from supplier where supp_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, suppCodeShip);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								group_ship = checkNull(rs.getString(1));
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("group_ship@@"+group_ship);
							sql="select group_code  from supplier where supp_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, mVal);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								group_supp = checkNull(rs.getString(1));
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("group_supp@@@"+group_supp);
							if(!group_ship.equalsIgnoreCase(group_supp))
							{
								errCode = "VTINVSHIP";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());		
							} 

						}
					}
					else if (childNodeName.equalsIgnoreCase("ret_opt")) 
					{

						ls_retopt = checkNull(genericUtility.getColumnValue("ret_opt", dom));
						System.out.println("@@@@ retOpt[" + retOpt	+ "]");
						ls_suppcode = checkNull(genericUtility.getColumnValue("supp_code", dom));
						System.out.println("Supp code in Dom "+ls_suppcode);
						mVal1 = checkNull(genericUtility.getColumnValue("site_code", dom)); 
						System.out.println("Supp code in Dom "+mVal1);
						sql="select channel_partner from site_supplier " +
								"where site_code = ? and supp_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mVal1);
						pstmt.setString(2, ls_suppcode);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							ls_channelpartner = rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if ((ls_channelpartner == null || ls_channelpartner.trim().length() == 0))
						{
							sql="select case when channel_partner is null then 'N' else channel_partner end " +
									" from supplier where supp_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ls_suppcode);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								ls_channelpartner = rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if (ls_channelpartner.equalsIgnoreCase("Y"))
							{
								if (ls_retopt==null || ls_retopt.trim().length()==0)
								{
									errCode = "VTRETOPT";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());		
								}		

							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("invoice_no")) 
					{

						ls_invno = checkNull(genericUtility.getColumnValue("invoice_no", dom));
						System.out.println("@@@@invoiceNo[" + ls_invno	+ "]");
						if ((ls_invno != null && ls_invno.trim().length() > 0))
						{
							System.out.println("invNumner not null");
							ls_supp = checkNull(genericUtility.getColumnValue("supp_code", dom));
							invoiceDate = checkNull(genericUtility.getColumnValue("invoice_date", dom));
							if(invoiceDate!=null||invoiceDate.trim().length()>0)
							{
								invoiceDate1 = Timestamp.valueOf(genericUtility.getValidDateString(invoiceDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+ " 00:00:00.0");
							}
							System.out.println("invoiceDate@@before["+invoiceDate+"]"+"invoiceDate@@after["+invoiceDate1+"]");
							sql="select count(*)  from voucher " +
									"where bill_no = ? " +
									"and supp_code = ? " +
									"and bill_date = ?";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1,ls_invno );
							pstmt.setString(2,ls_supp );
							pstmt.setTimestamp(3,invoiceDate1 );
							rs=pstmt.executeQuery();
							if(rs.next())
							{
								ll_cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("@@@@invoice_no--> ll_cnt > 0[" + ll_cnt	+ "]");
							//if(cnt > 0)//if condn changed by Pavan r on 4/DEC/17 for duplicate bill no error  
							if(ll_cnt > 0)
							{
								errCode = "VTBILL1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						ls_tranid = genericUtility.getColumnValue("tran_id", dom);
						if(ls_tranid == null)
						{
							ls_tranid = "";
						}
						if(errCode.trim().length() == 0||errCode==null)
						{

							sql="select count(*) from porcp where supp_code = ? and invoice_no=? and invoice_date=? and tran_id <> ?";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1, ls_supp);
							pstmt.setString(2, ls_invno);
							pstmt.setTimestamp(3, invoiceDate1);
							pstmt.setString(4, ls_tranid);
							rs=pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(cnt > 0)
							{

								errCode = "VTINV";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

						}
						else
						{
							sql="select count(*) from misc_payables " +
									"where sundry_type = ? and sundry_code=? " +
									"and bill_no=? and bill_date= ? ";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1, "S");
							pstmt.setString(2, ls_supp);
							pstmt.setString(3, ls_invno);
							pstmt.setTimestamp(4, invoiceDate1);
							rs=pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("@@@@invoice_no--> misc_payables -->cnt > 0[" + cnt	+ "]");
							if(cnt > 0)
							{
								errCode = "VTINV";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else
							{
								sql="select count(*) from  misc_voucher where sundry_code = ? and bill_no = ? and bill_date = ? and sundry_type = ?";
								pstmt=conn.prepareStatement(sql);
								pstmt.setString(1, suppCode);
								pstmt.setString(2, invoiceNo);
								pstmt.setTimestamp(3, invoiceDate1);
								pstmt.setString(4, "S");
								rs=pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
								}
								System.out.println("@@@@invoice_no--> misc_payables -->cnt > 0[" + cnt	+ "]");
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(cnt > 0)
								{
									errCode = "VTINV";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								else if(cnt == 0)
								{
									System.out.println("THe Count is 0");
									sql="select count(*) from  misc_vouchdet " +
											"where sundry_code__for = ? " +
											"and bill_no = ? " +
											"and bill_date = ? " +
											"and sundry_type__for = ? ";
									pstmt=conn.prepareStatement(sql);
									pstmt.setString(1, ls_supp);
									pstmt.setString(2, ls_invno);
									pstmt.setTimestamp(3, invoiceDate1);
									pstmt.setString(4, "S");
									rs=pstmt.executeQuery();
									if(rs.next())
									{
										cnt = rs.getInt(1);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									System.out.println("@@@@invoice_no--> misc_voucher -->cnt > 0[" + cnt	+ "]");
									if(cnt > 0)
									{
										errCode = "VTINV";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
						}

					}
					//added by manish mhatre on  06-nov-2019
					//start manish [add validation for accept criteria]
					else if (childNodeName.equalsIgnoreCase("accept_criteria")) 
					{
						acceptCriteria = checkNull(genericUtility.getColumnValue("accept_criteria", dom));

						if(acceptCriteria== null  || acceptCriteria.trim().length() == 0)
						{
							errCode="VTNULCRT  ";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}	
					}  //end manish
					
				}// end for <purc_order>
				break;
			case 2:
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				System.out.println("@@@@@@@@@@@@childNodeListLength["+ childNodeListLength + "]");
				for (ctr = 0; ctr < childNodeListLength; ctr++) 
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					System.out.println("childNodeName"+childNodeName);
					if (childNodeName.equalsIgnoreCase("tran_id__rcp")) 
					{

						tranIdRcp = checkNull(genericUtility.getColumnValue("tran_id__rcp",dom));
						System.out.println("Validation Po Dom"+tranIdRcp.length());
						tranIdRcp=tranIdRcp.trim();
						System.out.println("Validation Po Trim"+tranIdRcp.length());
						tranId =checkNull(genericUtility.getColumnValue("tran_id", dom)); 
						System.out.println("Tran_Id@@["+tranId+"]");
						tranDate = checkNull(genericUtility.getColumnValue("tran_date", dom1));
						System.out.println("Tran date in dom "+tranDate);
						if (tranIdRcp.trim()!=null && tranIdRcp.trim().length() >0)
						{
							sql="select count(*)  from porcp where tran_id = ? and confirmed = 'Y' ";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1, tranIdRcp);

							rs=pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (cnt == 0)
							{
								errCode = "VTTRANID";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

						}

					}//end purchase order case
					else if(childNodeName.equalsIgnoreCase("purc_order"))
					{

						purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom));
						hdrCurr_code = checkNull(genericUtility.getColumnValue("curr_code", dom1));
						supp_Code = checkNull(genericUtility.getColumnValue("supp_code", dom1));
						System.out.println("supp_code from header"+supp_Code);
						sql="Select Count(*)  from porder where purc_order = ?";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1, purcOrder);

						rs=pstmt.executeQuery();
						if(rs.next())
						{
							cnt = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (cnt == 0) 
						{
							errCode = "VTPORD3";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());

						}else
						{
							sql="Select Supp_Code, Curr_code,status,emp_code__aprv from porder where purc_order = ? ";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1, purcOrder);

							rs=pstmt.executeQuery();
							if(rs.next())
							{

								poSuppCode=checkNull(rs.getString(1));
								poCurrCode=checkNull(rs.getString(2));
								status=checkNull(rs.getString(3));
								emp_code__aprv=checkNull(rs.getString(4));
								//cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(!poSuppCode.equalsIgnoreCase(supp_Code))
							{
								errCode = "VTPORD4";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							}else if(!poCurrCode.equalsIgnoreCase(hdrCurr_code))
							{
								errCode = "VTPORD5";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}else if(status.equalsIgnoreCase("O"))
							{
								if (emp_code__aprv==null){

									errCode = "VTPONAPRV";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								} 


							}
						}
					}
					else if (childNodeName.equalsIgnoreCase("item_code")) 
					{



						itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						System.out.println("itemCode@["+itemCode+"]");
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom1));
						System.out.println("siteCode@["+siteCode+"]");
						//trandateStr = checkNull(genericUtility.getColumnValue("tran_date", dom1));dhiraj
						System.out.println("siteCode@header@"+siteCode);
						tranDateStr =genericUtility.getColumnValue("tran_date", dom1);
						System.out.println("trandate@header@"+tranDateStr);
						itemSer = checkNull(genericUtility.getColumnValue("item_ser", dom1));
						System.out.println("itemSer@header@"+itemSer);
						suppCode = checkNull(genericUtility.getColumnValue("supp_code", dom1));
						System.out.println("suppCode@header@"+suppCode);

						System.out.println("purcOrder"+purcOrder);

						System.out.println("tranDateStr@@"+tranDateStr);
						if(tranDateStr!=null)
						{
							TranDate = Timestamp.valueOf(genericUtility.getValidDateString(tranDateStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");
						}
						System.out.println("TranDate@itemcode@2@"+TranDate);

						//System.out.println("Trandate is"+TranDate);
						//errcode = nvo_dis.gbf_item(mval1,mval,transer)
						//errCode=this.isItem(sitecodech, itemCode, modName, conn)
						errCode=this.isItem(siteCode, itemCode,modName, conn);
						System.out.println("ErrCode@item["+errCode+"]");

						sql = "select count(1) from item where item_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							cnt = rs.getInt(1);
						}

						System.out.println("$$$Wrong Item Code$$$$$"+cnt);
						pstmt.close();
						rs.close();
						pstmt = null;
						rs = null;
						if (cnt == 0)
						{
							errCode = "VTITEM1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}

						if ( errCode == null || errCode.trim().length()==0)
						{
							sql="select oth_series from itemser where item_ser = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemSer);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								mstatus1 = checkNull(rs.getString(1));
							}
							System.out.println("mstatus1@@"+mstatus1);
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if("N".equalsIgnoreCase(mstatus1))
							{
								//--
								sql="select channel_partner,site_code__ch from site_supplier where site_code = ? and supp_code = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, siteCode);
								pstmt.setString(2, suppCode);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									ls_cp = checkNull(rs.getString(1));
									ls_cp_site = checkNull(rs.getString(2));
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								System.out.println("mstatus1@@"+channelPartner);
								if(ls_cp==null)
								{
									sql="select channel_partner,site_code from supplier where supp_code = ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, suppCode);
									//pstmt.setString(2, suppCode);
									rs = pstmt.executeQuery();
									if (rs.next())
									{
										suppchannelPartner = rs.getString(1);
										suppSiteCode = rs.getString(2);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								}
								if(suppchannelPartner.equalsIgnoreCase("Y"))
								{
									siteCode=suppSiteCode;
								}
								if (errCode== null||errCode.trim().length()==0)
								{

									//Changed By PriyankaC on 19April2018 [START]
									mval3 = discommon.getItemSer(itemCode, siteCode, TranDate, suppCode, "S", conn);//gf_get_item_ser(mval,mval1,mdate1,msupp,'S');			
									System.out.println(mval3);

									/*mval3 = itmDBAccess.getItemSeries(itemCode, siteCode,trandateStr,suppCode,'S',conn);
									System.out.println("test@@"+mval3);*/

									//Changed By PriyankaC on 19April2018 [END]
									errCode=discommon.getToken(mval3, "~t");
									System.out.println("mval3@@"+mval3);
									System.out.println("errCode@@"+errCode);
									if (errCode== null||errCode.trim().length()==0)
									{
										if(!itemSer.equalsIgnoreCase(mval3))
										{
											errCode = "VTITEM2";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
									}

								}


							}
							//Added by Pavan Rane 21NOV19 start[validaiton for mismatch of po line and line_no__ord with item]
							tranIdRef = checkNull(genericUtility.getColumnValue("tran_id__ref", dom1));
							lineNoOrd = checkNull(genericUtility.getColumnValue("line_no__ord", dom));
							if (lineNoOrd != null && lineNoOrd.trim().length() > 0) {
								lineNoOrd = "    " + lineNoOrd;
								lineNoOrd = lineNoOrd.substring(lineNoOrd.length() - 3, lineNoOrd.length());
							}
							lineNoRcp = checkNull(genericUtility.getColumnValue("line_no__rcp", dom));
							if (lineNoRcp != null && lineNoRcp.trim().length() > 0) {
								lineNoRcp = "    " + lineNoRcp.trim();
								lineNoRcp = lineNoRcp.substring(lineNoRcp.length() - 3, lineNoRcp.length());
							}							
							System.out.println("@@tranIdRef@@[" + tranIdRef + "]lineNoOrd["+lineNoOrd+"]lineNoOrd["+lineNoOrd+"]");
							if (tranIdRef != null && tranIdRef.trim().length() > 0)
							{
								cnt = 0;
								sql = "select count(1)  from porcpdet where tran_id = ? and line_no = ? and line_no__ord = ? "
									+ "and item_code = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, tranIdRef);
								pstmt.setString(2, lineNoRcp);
								pstmt.setString(3, lineNoOrd);								
								pstmt.setString(4, itemCode);								
								rs = pstmt.executeQuery();
								if (rs.next()) {
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(cnt == 0)
								{
									errCode = "VTINVLINE";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							//Added by Pavan Rane 21NOV19 End[validaiton for mismatch of po line and line_no__ord with item]
						}

					}//end item code case childNodeName.equalsIgnoreCase("line_no__ord")
					else if (childNodeName.equalsIgnoreCase("line_no__ord")) 
					{
						System.out.println("Validation Quantity");
						lineNoOrd =genericUtility.getColumnValue("line_no__ord",dom);
						purcorder =genericUtility.getColumnValue("purc_order",dom);
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom1));
						lineNoOrd = "    " + lineNoOrd;
						lineNoOrd = lineNoOrd.substring(lineNoOrd.length() - 3,lineNoOrd.length());
						System.out.println("lineNoOrd@@["+lineNoOrd+"]");
						//PriyankaC  [START]
						sql = "select site_code from porddet where purc_order = ?and  line_no = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, purcOrder);
						pstmt.setString(2, lineNoOrd);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							//Change By PriyankaC on 30JAN2018..[START]
							//site = rs.getString("site_code");
							site = checkNull(rs.getString("site_code"));
							System.out.println("site@@ " +site);
							//Change By PriyankaC on 30JAN2018..[END]
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if(site == null || site.trim().length() == 0)
						{
							System.out.println("VTORDDT1@@ ");
							errCode = "VTORDDT1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						//if (!(siteCode).trim().equalsIgnoreCase(site)) //Change By PriyankaC on 30JAN2018..
						//if (!checkNull(siteCode).equalsIgnoreCase(site))
						if (!checkNull(siteCode.trim()).equalsIgnoreCase(site.trim())) //trimmed both sites
						{
							System.out.println("VTPORD6@@ ");
							errCode = "VTPORD6";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						//PriyankaC [END]
						tranIdRef = checkNull(genericUtility.getColumnValue("tran_id__ref", dom1));
						System.out.println("tranIdRef@@["+tranIdRef+"]");
						if(tranIdRef.trim().length()>0 && tranIdRef!=null)//len(trim(mval2)) > 0
						{
							sql="select count(*) from porcp,porcpdet " +
									"where porcp.tran_id = porcpdet.tran_id " +
									"and porcp.tran_id  = ? " +
									"and porcp.tran_ser = 'P-RCP' " +
									"and porcpdet.purc_order = ? " +
									"and porcpdet.line_no__ord = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, tranIdRef);
							pstmt.setString(2, purcorder);
							pstmt.setString(3, lineNoOrd);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;  
							System.out.println("sql@@"+sql);
							System.out.println("cnt@lin_no_ord"+cnt);
							if(cnt==0)
							{
								errCode = "VTINVLINE";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}// end line_no__ord
					else if (childNodeName.equalsIgnoreCase("unit")) 
					{

						unit = checkNull(genericUtility.getColumnValue("unit", dom));
						unitStd = genericUtility.getColumnValue("unit__std", dom);
						sql="select count(*) from uom where unit = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, unit);	
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							cnt = rs.getInt(1);

						}
						System.out.println("cnt**********-->"+cnt);
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if(cnt ==0)
						{
							errCode = "VTUNIT1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());

						}
						else if (! unit.equalsIgnoreCase(unitStd))
						{
							sql="select count(*) from uomconv where unit__fr = ? and unit__to = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, unit);	
							pstmt.setString(2, unitStd);	
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt(1);
							}
							System.out.println("cnt***unit*******-->"+cnt);
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(cnt ==0)
							{
								errCode = "VTUNIT2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					//end unit
					else if (childNodeName.equalsIgnoreCase("rate")) 
					{

						System.out.println("@@@@@ validation of conv__qty_stduom executed......");
						rateDom = checkNull(genericUtility.getColumnValue("rate", dom));
						unit = checkNull(genericUtility.getColumnValue("unit",dom));
						unitStd = checkNull(genericUtility.getColumnValue("unit__std", dom));
						rate = rateDom == null ? 0 : Double.parseDouble(rateDom);
						ls_retopt = checkNull(genericUtility.getColumnValue("ret_opt",dom1));
						purcOrder =genericUtility.getColumnValue("purc_order",dom);
						sql="select price_list from porder where purc_order = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, purcOrder);	
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							priceList =checkNull(rs.getString(1));
						}
						System.out.println("priceList***rate*******-->"+priceList);
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (priceList!=null || priceList.trim().length() > 0)
						{
							//if (rate <= 0)
							if (rate < 0)
							{
								errCode = "VTRATE2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

						}else
						{
							if (rate < 0)
							{
								errCode = "VTRATE2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

					}	
					else if (childNodeName.equalsIgnoreCase("unit__rate")) //
					{

						unitrate = checkNull(genericUtility.getColumnValue("unit__rate", dom));
						unitstd = checkNull(genericUtility.getColumnValue("unit__std", dom));
						System.out.println("unitrate"+unitrate);
						System.out.println("unitstd"+unitstd);
						sql="select count(*) from uom where unit =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, unitrate);		
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							cnt = rs.getInt(1);

						}
						System.out.println("cnt**********-->"+cnt);
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if(cnt ==0)
						{
							errCode = "VTUNIT1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());

						}
						else if (! unitrate.equalsIgnoreCase(unitstd))
						{

							sql="select count(*) from uomconv where unit__fr = ? and unit__to = ?"	;
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, unitrate);	
							pstmt.setString(2, unitstd);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt(1);
							}
							System.out.println("CNt Unit Rate**********-->"+cnt);
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(cnt == 0)
							{
								sql="select count(*) from uomconv where unit__fr = ? and unit__to = ?"	;
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, unitstd);	
								pstmt.setString(2, unitrate);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									cnt = rs.getInt(1);

								}
								System.out.println("CNt Unit Rate**********-->"+cnt);
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(cnt == 0)
								{

									errCode = "VTUNIT3";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
					}// end case unit rate
					else if (childNodeName.equalsIgnoreCase("pack_code"))
					{

						packcode =checkNull(genericUtility.getColumnValue("pack_code", dom));  
						if(packcode !=null && packcode.trim().length() >0)
						{
							sql="select count(*) from packing  where pack_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,packcode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(cnt == 0)
							{
								errCode = "VTPKCD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());


							}
						}else
						{
							ls_suppcode = checkNull(genericUtility.getColumnValue("supp_code", dom1));
							ls_sitecode = checkNull(genericUtility.getColumnValue("site_code", dom1));

							sql="select channel_partner from site_supplier where site_code = ? and supp_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,ls_sitecode);
							pstmt.setString(2,ls_suppcode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								ls_channelpartner = rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(ls_channelpartner == null||ls_channelpartner.trim().length()==0)
							{
								sql="select case when channel_partner is null then 'N' else channel_partner end  " +
										"from supplier " +
										"where supp_code = ? ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1,ls_suppcode);
								// pstmt.setString(2,ls_suppcode);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									ls_channelpartner = rs.getString(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
							if(ls_channelpartner.equalsIgnoreCase("Y"))
							{
								errCode = "VTPKCD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							}
						}
					}// end pack code
					else if(childNodeName.equalsIgnoreCase("quantity"))
					{

						purcOrder =checkNull(genericUtility.getColumnValue("purc_order",dom));	
						System.out.println("PURC_ORDER["+purcOrder+"]");
						lineNoOrd =checkNull(genericUtility.getColumnValue("line_no__ord",dom));	
						System.out.println("lineNoOrd["+lineNoOrd+"]");
						ls_tranid =checkNull(genericUtility.getColumnValue("tran_id",dom));
						if (ls_tranid == null || "null".equals(ls_tranid) || ls_tranid.trim().length() == 0)
						{
							ls_tranid = "@@@@@@@@@@";
						}
						System.out.println("ls_tranid["+ls_tranid+"]");
						lineNoOrd = "    " + lineNoOrd;

						lineNoOrd = lineNoOrd.substring(lineNoOrd.length() - 3,lineNoOrd.length());
						System.out.println("lineNoOrd["+lineNoOrd+"]");
						System.out.println("lineNoOrd@@"+lineNoOrd);
						quantity =genericUtility.getColumnValue("quantity",dom);
						System.out.println("quantity["+quantity+"]");

						System.out.println("quantity"+quantity);
						qty=quantity == null ? 0 :Double.parseDouble(quantity);
						System.out.println("Quantity double parse"+qty);
						tranIdRef =checkNull(genericUtility.getColumnValue("tran_id__ref",dom1));	
						System.out.println("tranIdRef["+tranIdRef+"]");
						ls_loc =checkNull(genericUtility.getColumnValue("loc_code",dom));
						System.out.println("ls_loc["+ls_loc+"]");
						ls_lotno = genericUtility.getColumnValue("lot_no",dom);   // changes done by vrushabh 
						if(ls_lotno==null)  // added By Vrushabh on 29-5-20 for setting of lotno as space start
                        {
							ls_lotno= " ";                     
                        }                   // added By Vrushabh on 29-5-20 for setting of lotno as space End
						System.out.println("ls_lotno["+ls_lotno+"]");
						ls_lotsl =checkNull(genericUtility.getColumnValue("lot_sl",dom));
                                   
						System.out.println("lot_sl@@["+ls_lotsl+"]");
						msite =checkNull(genericUtility.getColumnValue("site_code",dom1));
						System.out.println("msite["+msite+"]");
						ls_item =checkNull(genericUtility.getColumnValue("item_code",dom));
						System.out.println("ls_item["+ls_item+"]");
						lc_qty_std1 =genericUtility.getColumnValue("quantity__stduom",dom);
						System.out.println("lc_qty_std1@@"+lc_qty_std1);
						lc_qty_std=lc_qty_std1 == null ? 0 :Double.parseDouble(lc_qty_std1);
						System.out.println("lc_qty_std double parse"+lc_qty_std);
						if(qty < 0)
						{
							errCode = "VTNEGQTY";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						ls_line_no =checkNull(genericUtility.getColumnValue("line_no",dom));
						System.out.println("ls_line_no["+ls_line_no+"]");
						if(errCode==null||errCode.trim().length()==0)
						{

							sql="select stk_opt from item where item_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,ls_item);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								ls_stkopt = rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("ls_stkopt["+ls_stkopt+"]");
							if(ls_stkopt==null)
							{
								ls_stkopt="0";
							}
							System.out.println("ls_stkopt@IN if["+ls_stkopt+"]");
							if(!ls_stkopt.equalsIgnoreCase("0") )
							{
								sql="select count(1)  from porcpdet where tran_id = ? and line_no = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1,ls_tranid);
								pstmt.setString(2,lineNoOrd);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									ll_cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								System.out.println("ll_cnt["+ll_cnt+"]");
								if(ll_cnt > 0 )
								{
									sql="select quantity__stduom, lot_no, lot_sl, loc_code 	" +
											"from porcpdet where tran_id = ? and line_no = ?";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1,ls_tranid);
									pstmt.setString(2,ls_line_no);
									rs = pstmt.executeQuery();
									if (rs.next())
									{
										lc_old_qty = rs.getDouble(1);
										ls_lot_no_old= checkNull(rs.getString(2));
										ls_lot_sl_old=checkNull(rs.getString(3));
										ls_loc_code_old=checkNull(rs.getString(4));
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									System.out.println("lc_old_qty["+lc_old_qty+"]");
									System.out.println("ls_lot_no_old["+ls_lot_no_old+"]");
									System.out.println("ls_lot_sl_old["+ls_lot_sl_old+"]");
									System.out.println("ls_loc_code_old["+ls_loc_code_old+"]");

								}
								if(lc_old_qty==0 )
								{
									lc_old_qty=0;
								}
								if (!ls_loc_code_old.trim().equalsIgnoreCase(ls_loc.trim())
										|| !ls_lot_no_old.trim().equalsIgnoreCase(ls_lotno.trim())
										//|| ls_lot_sl_old.trim().equalsIgnoreCase(ls_lotsl.trim()))    //Commented by Anagha R on 20/07/2020 for On edit of purchase return details 'Item not found in stock' error occurs.  
										|| !ls_lot_sl_old.trim().equalsIgnoreCase(ls_lotsl.trim()))     //Added by Anagha R on 20/07/2020 for On edit of purchase return details 'Item not found in stock' error occurs.  
								{
									lc_old_qty = 0;
								}
								System.out.println("ls_item["+ls_item+"]@msite["+msite+"]@ls_loc"+ls_loc+"]ls_lotno@["+ls_lotno+"]ls_lotsl@"+ls_lotsl+"]");
								sql="select quantity - (case when alloc_qty is null then 0 else alloc_qty end) " +
										" from stock "+
										" where item_code = ? " +
										" and site_code = ? " +
										" and loc_code = ? " +
										" and lot_no = ? " +
										" and lot_sl = ? ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1,ls_item);
								pstmt.setString(2,msite);
								pstmt.setString(3,ls_loc);
								pstmt.setString(4,ls_lotno);
								pstmt.setString(5,ls_lotsl);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									lc_stk_qty = rs.getDouble(1);

								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								System.out.println("lc_stk_qty@@@@"+lc_stk_qty);
								//Commented by Anagha R on 20/07/2020 for On edit of purchase return details 'Item not found in stock' error occurs. Start
								/*
								 * if(lc_stk_qty==0) { System.out .println("lc_stk_qty@if@["+lc_stk_qty+"]");
								 * errCode = "VXSTKITM"; errList.add(errCode);
								 * errFields.add(childNodeName.toLowerCase()); } else if((lc_qty_std -
								 * lc_old_qty) > lc_stk_qty ) {
								 * 
								 * errCode = "VXSTK2"; errList.add(errCode);
								 * errFields.add(childNodeName.toLowerCase()); }
								 */
								//Commented by Anagha R on 20/07/2020 for On edit of purchase return details 'Item not found in stock' error occurs.  End
								//Added by Anagha R on 20/07/2020 for On edit of purchase return details 'Item not found in stock' error occurs.  Start
								qty_available= lc_stk_qty + lc_old_qty;
								
								if(qty_available==0)
								{		
									System.out
									.println("Qty_avilable@if@["+qty_available+"]");
									errCode = "VXSTKITM";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}										    
								else if(lc_qty_std  > qty_available )
								{		

									errCode = "VXSTK2";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								//Added by Anagha R on 20/07/2020 for On edit of purchase return details 'Item not found in stock' error occurs.  End
							}

						}
						if(tranIdRef.trim().length()>0 && tranIdRef!=null)
						{
							sql="select case when stk_opt is null then '0' else stk_opt end " +
									"from item " +
									"where item_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,ls_item);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								ls_stkopt = rs.getString(1);

							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("ls_stkopt@@"+ls_stkopt);        
							if(!ls_stkopt.equalsIgnoreCase("0"))
							{
								sql=" select count(1) " +
										" from stock where item_code = ? " +
										" and lot_no = ? ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1,ls_item);
								pstmt.setString(2,ls_lotno);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									ll_cnt = rs.getInt(1);

								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								System.out.println("ll_cnt@@"+ll_cnt);
								if(ll_cnt==0)
								{

									errCode = "VTNELOT";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());

								}else
								{
									sql="select case when quantity is null then 0 else quantity end " + 
											" from stock " +
											" where item_code = ? and site_code = ? and "+
											" loc_code = ? and lot_no = ? and " + 
											" lot_sl = ? ";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1,ls_item);
									pstmt.setString(2,msite);
									pstmt.setString(3,ls_loc);
									pstmt.setString(4,ls_lotno);
									pstmt.setString(5,ls_lotsl);
									rs = pstmt.executeQuery();
									if (rs.next())
									{
										lc_stk_qty = rs.getDouble(1);

									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									System.out.println("lc_stk_qty@@"+lc_stk_qty);
									System.out.println("@@ls_item["+ls_item+"]@msite["+msite+"]@ls_loc"+ls_loc+"]ls_lotno@["+ls_lotno+"]ls_lotsl@"+ls_lotsl+"]");
									if(lc_stk_qty==0)
									{
										errCode = "VXSTK2";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());

									}else if(lc_qty_std > lc_stk_qty)
									{
										errCode = "VTSTK5";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
						}
						//if((tranIdRef.trim().length()>0 && (errCode.trim().length()==0 || errCode==null) ) )  Commented by Mahesh Saggam on 13 June 2019
						 if(tranIdRef.trim().length()>0)   //Added by Mahesh Saggam on 13 June 2019
						{
							NodeList detlList = dom2.getElementsByTagName("Detail2");//Added by sarita on 22 OCT 18

							sql="select case when sum(quantity) is null then 0 else sum(quantity) end  from porcpdet " +
									" where tran_id=? and purc_order = ? and "+
									" line_no__ord = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,tranIdRef);
							pstmt.setString(2,purcOrder);
							pstmt.setString(3,lineNoOrd);
							/*pstmt.setString(4,ls_lotno);
							pstmt.setString(5,ls_lotsl);*/
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								lc_rcp_qty = rs.getDouble(1);	

							}
							System.out.println("lc_rcp_qty@@@["+lc_rcp_qty+"]");		
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							
							
							//Added by sarita on 22 OCT 18 [START]
							int QtyLineNo = 0;
							lineNoRcp =genericUtility.getColumnValue("line_no__rcp",dom); 
							if(lineNoRcp != null)
							{
								lineNoRcp = lineNoRcp.trim();//Added by sarita on 25 JANUARY 2018
								QtyLineNo = Integer.parseInt(lineNoRcp);
							}
							System.out.println("Line Number Receipt is ["+QtyLineNo+"]");
							double lineQty = 0.0;
							sql="select case when sum(quantity) is null then 0 else sum(quantity) end  from porcpdet " +
									" where tran_id=? and purc_order = ? and "+
									" line_no = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,tranIdRef);
							pstmt.setString(2,purcOrder);
							pstmt.setInt(3,QtyLineNo);	
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								lineQty = rs.getDouble(1);	
							}
							System.out.println("lineQty@@@["+lineQty+"]");		
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
													
							Double det2Qty = getSumOfDetail2Quantity(dom2,QtyLineNo);
							System.out.println("Detail 2 Final Value is === " + det2Qty );
							if(  det2Qty > lineQty )
							{
								errCode = "VTINVPQTY";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							//Added by sarita on 22 OCT 18 [END]
							
							// Added by Mahesh Saggam on 12 June 2019 [Start]
							double rcpLineQty = 0.0;
							sql="select case when sum(porcpdet.quantity) is null then 0 else sum(porcpdet.quantity) end as quantity from porcp, porcpdet " +
									" where porcp.tran_id = porcpdet.tran_id"
									+ " and porcp.tran_id__ref = ?"
									+ " and porcpdet.line_no = ?"
									+ " and porcp.tran_id <> ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, tranIdRef);																					
							pstmt.setInt(2,QtyLineNo);	
							pstmt.setString(3,ls_tranid);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								rcpLineQty = rs.getDouble(1);	
							}
							System.out.println("lineQty@@@["+rcpLineQty+"]");		
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
													
							det2Qty = getSumOfDetail2Quantity(dom2,QtyLineNo);							
							if(  det2Qty > rcpLineQty )
							{
								errCode = "VTINVPQTY";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							// Added by Mahesh Saggam on 12 June 2019 [End]
							
							
							sql="select case when sum(porcpdet.quantity) is null then 0 else sum(porcpdet.quantity) end  " + 
									"from porcp,porcpdet " +
									"where porcp.tran_id = porcpdet.tran_id "+ 
									" and porcp.tran_ser = 'P-RET' " +
									" and porcp.tran_id__ref = ? " +
									" and line_no__ord = ? " +
									" and porcp.tran_id <> ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,tranIdRef);
							pstmt.setString(2,lineNoOrd);
							pstmt.setString(3,ls_tranid);
							//pstmt.setString(4,ls_tranid);
							/*pstmt.setString(5,ls_lotsl);*/
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								lc_qty = rs.getDouble(1);

							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;	
							System.out.println("lc_qty@@["+lc_qty+"]");
							System.out.println("lc_qty@@["+lc_old_qty+"]");
							System.out.println("lc_qty@@["+qty+"]");
							System.out.println("lc_qty@@["+lc_rcp_qty+"]");
							System.out.println("total Calc["+(((lc_qty -lc_old_qty )+ mqty) +" > "+ (lc_rcp_qty)));							
							//if (((lc_qty -lc_old_qty )+ mqty) > (lc_rcp_qty)) Pavan Rane 10oct19 [changed to validate preturn line qty should not more than porcpdet line qty]
							if (((lc_qty -lc_old_qty )+ qty) > (lc_rcp_qty)) 
							{
								//errCode = "VTRCPQTY1";

								errCode = "VTRCPQTY1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}


						}else if (((purcOrder.trim().length()>0)) &&  (lineNoOrd.trim().length() >0) && (errCode==null || errCode.trim().length()==0) )	

						{
							System.out.println("Sarita123........");
							//changes by sarita to correct query on 13 NOV 2018 [START]
							/*sql="select b.quantity  from porder a, porddet b " +
									" where a.purc_order = b. purc_order " +
									" and a.purc_order = int lineCnt = 0;? and b.line_no = ? ";*/
							sql="select b.quantity  from porder a, porddet b " +
									" where a.purc_order = b. purc_order " +
									" and a.purc_order = ? and b.line_no = ? ";
							//changes by sarita to correct query on 13 NOV 2018 [END]
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,purcOrder);
							pstmt.setString(2,lineNoOrd);
							//pstmt.setString(3,ls_tranid);
							//pstmt.setString(4,ls_tranid);
							/*pstmt.setString(5,ls_lotsl);*/
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								lc_order_qty = rs.getDouble(1);

							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;	
							System.out.println("lc_order_qty@@["+lc_order_qty+"]");
							sql="select case when sum(b.quantity) is null then 0 else sum(b.quantity) end " +
									"from porcp a, porcpdet b " +
									" where a.tran_id = b.tran_id and a.tran_ser = 'P-RET' " +
									" and a.purc_order = ? and b.line_no__ord = ? and a.tran_id <> ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,purcOrder);
							pstmt.setString(2,lineNoOrd);
							pstmt.setString(3,ls_tranid);
							//pstmt.setString(4,ls_tranid);
							/*pstmt.setString(5,ls_lotsl);*/
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								lc_porcpqty = rs.getDouble(1);

							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("lc_porcpqty@@@["+lc_porcpqty+"]");
							if(lc_order_qty < (lc_porcpqty + mqty - lc_old_qty))
							{
								//errCode = "";
								errCode = "VTPOQTYEXCD";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							}
						}
					}//end case loc code
					
					// Added by Mahesh Saggam on 05/07/2019 start [if conv_qty__stduom is 1 then quantiy and quantity__stduom should be same]
					
					else if( childNodeName.equalsIgnoreCase( "conv__qty_stduom" ) )
					{
						String convQtyStduomStr = "", qtyStr = "", qtyStdStr = "";
						double dqty = 0, dqtyStduom = 0;
						
						convQtyStduomStr = checkNull(genericUtility.getColumnValue("conv__qty_stduom", dom));
						
						if(convQtyStduomStr != null && convQtyStduomStr.trim().length() > 0)
						{
							convQtyStduom = convQtyStduomStr == null ? 0 : Double.parseDouble(convQtyStduomStr);
						}
						
						if(convQtyStduom == 1)
						{
							
							qtyStr = checkNull(genericUtility.getColumnValue("quantity", dom));
							qtyStdStr = checkNull(genericUtility.getColumnValue("quantity__stduom", dom));	
							System.out.println("quantity = "+qtyStr+" std quantity = "+qtyStdStr);
							
							if (qtyStr != null && qtyStr.trim().length() > 0) 
							{
								dqty = Double.parseDouble(qtyStr);	
							}
							if (qtyStdStr != null && qtyStdStr.trim().length() > 0) 
							{
								dqtyStduom = Double.parseDouble(qtyStdStr);
							}
							if(dqty != dqtyStduom)
							{
								System.out.println("Error occured in validating quantity");
								errCode = "VTINVQTTY";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					
					else if( childNodeName.equalsIgnoreCase( "conv__rtuom_stduom" ) ) // [if conv_rate__stduom is 1 then rate and rate__stduom should be same]
					{
						String convRtuomStduomStr = "", rateStr = "", ratestdStr = "";
						double convRtuomStduom = 0,pordRate = 0, stdRate = 0;
						
						convRtuomStduomStr = checkNull(genericUtility.getColumnValue("conv__rtuom_stduom", dom));
						
						if(convRtuomStduomStr != null && convRtuomStduomStr.trim().length() > 0)
						{
							convRtuomStduom = convRtuomStduomStr == null ? 0: Double.parseDouble(convRtuomStduomStr);
						}
						
						if(convRtuomStduom == 1)
						{
							
							rateStr = checkNull(genericUtility.getColumnValue("rate", dom));
							ratestdStr = checkNull(genericUtility.getColumnValue("rate__stduom", dom));	
							System.out.println("rate = "+rateStr+" std rate = "+ratestdStr);
							
							if (rateStr != null && rateStr.trim().length() > 0) 
							{	
								pordRate = Double.parseDouble(rateStr);	
							}
							if (ratestdStr != null && ratestdStr.trim().length() > 0) 
							{
								stdRate = Double.parseDouble(ratestdStr);
							}
							if(pordRate != stdRate)
							{
								System.out.println("Error occured in validating rate");
								errCode = "VTINVRTE";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					
					// Added by Mahesh Saggam on 05/07/2019 [End]
					
					else if (childNodeName.equalsIgnoreCase("site_code__mfg")) 
					{


						sitecodemfg=checkNull(genericUtility.getColumnValue("site_code__mfg",dom));
						// this.i
						if(sitecodemfg.trim().length() > 0)
						{

							errCode = this.isSiteCode(sitecodemfg, modName);//(sitecodemfg,"P-RET"); // --pravin
							System.out.println("errCode@@"+errCode);
							if(errCode!=null && errCode.trim().length()>0)
							{

								System.out.println("errCode@if@"+errCode);
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						else
						{
							ls_suppcode=checkNull(genericUtility.getColumnValue("supp_code",dom1));
							ls_sitecode=checkNull(genericUtility.getColumnValue("site_code",dom1));
							sql="select channel_partner " + 
									" from site_supplier " +
									" where site_code = ? and supp_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,ls_sitecode);
							pstmt.setString(2,ls_suppcode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								ls_channelpartner = rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;	
							if(ls_channelpartner==null)
							{
								sql="select case when channel_partner is null then 'N' else channel_partner end from supplier " +
										" where supp_code = ? " ; 
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1,ls_suppcode);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									ls_channelpartner = rs.getString(1);

								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;	

							}
							if(ls_channelpartner.equalsIgnoreCase("Y"))
							{
								errCode = "VTSITEMFG1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}// end lot no case
					else if (childNodeName.equalsIgnoreCase("reas_code")) 
					{
						reasCode = checkNull(genericUtility.getColumnValue("reas_code", dom));
						if(reasCode==null||reasCode.trim().length()==0)
						{
							ls_suppcode = checkNull(genericUtility.getColumnValue("supp_code", dom1));		
							ls_sitecode = checkNull(genericUtility.getColumnValue("site_code", dom1));
							sql=" select channel_partner " + 
									" from site_supplier " +
									" where site_code = ? and supp_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,ls_sitecode);
							pstmt.setString(2,ls_suppcode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								ls_channelpartner = rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;	
							if(ls_channelpartner==null || ls_channelpartner.trim().length()==0)
							{
								sql="select case when channel_partner is null then 'N' else channel_partner end " +
										" from supplier " +						
										" where supp_code = ? ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1,ls_suppcode);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									ls_channelpartner = rs.getString(1);

								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
							if(ls_channelpartner.equalsIgnoreCase("Y"))
							{
								errCode = "VUREASON";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}	


					}	
					else if(childNodeName.equalsIgnoreCase("expiry_date")) 
					{

						expiryDate = genericUtility.getColumnValue("expiry_date", dom);
						System.out.println("expiryDate@@"+expiryDate);
						if(expiryDate!=null)
						{
							ldt_date = Timestamp.valueOf(genericUtility.getValidDateString(expiryDate.toString(), genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");
						}
						System.out.println("expiryDate@1@"+ldt_date);
						if (ldt_date==null)
						{

							ls_suppcode = checkNull(genericUtility.getColumnValue("supp_code", dom1));		
							// 16-04-03 manoharan first check site_supplier
							//ls_sitecode = dw_header.getitemstring(1,"site_code")
							ls_sitecode = checkNull(genericUtility.getColumnValue("site_code", dom1));
							sql="select channel_partner " + 
									" from site_supplier " +
									" where site_code = ? and supp_code = ? "; 
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,ls_sitecode);
							pstmt.setString(2,ls_suppcode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								ls_channelpartner = rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(ls_channelpartner==null)
							{
								sql=" select case when channel_partner is null then 'N' else channel_partner end " +
										" from supplier " +
										" where supp_code = ? ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1,ls_suppcode);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									ls_channelpartner = rs.getString(1);

								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

							}
							if(ls_channelpartner.equalsIgnoreCase("Y"))
							{
								errCode = "VTSITEMFG1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

						}
					}
					else if (childNodeName.equalsIgnoreCase("cctr_code__dr")) 
					{

						//Timestamp ldt_date=null;
						mfgDate = genericUtility.getColumnValue("mfg_date", dom);
						System.out.println("mfgDate@@"+mfgDate);
						ls_sitecode = checkNull(genericUtility.getColumnValue("site_code", dom1));
						ls_suppcode = checkNull(genericUtility.getColumnValue("supp_code", dom1));
						cctrCodeDr=checkNull(genericUtility.getColumnValue("cctr_code__dr",dom));
						acctCodeDr=checkNull(genericUtility.getColumnValue("acct_code__dr",dom));
						if(mfgDate!=null)
						{
							ldt_date = Timestamp.valueOf(genericUtility.getValidDateString(mfgDate.toString(), genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");
						}

						System.out.println("ldt_date@@"+ldt_date);
						if (ldt_date==null)
						{
							sql="select channel_partner " + 
									" from site_supplier " +
									" where site_code = ? and supp_code = ? "; 
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,ls_sitecode);
							pstmt.setString(2,ls_suppcode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								ls_channelpartner =checkNull(rs.getString(1));

							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(ls_channelpartner==null)
							{
								sql=" select case when channel_partner is null then 'N' else channel_partner end " +
										" from supplier " +
										" where supp_code = ? ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1,ls_suppcode);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									ls_channelpartner = rs.getString(1);

								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

							}
							if(ls_channelpartner.equalsIgnoreCase("Y"))
							{
								errCode = "VTSITEMFG1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						//added by manish mhatre  on 3-jan-2020
							if(cctrCodeDr!=null && cctrCodeDr.trim().length() > 0)
							{
								errCode = finCommon.isCctrCode(acctCodeDr, cctrCodeDr, " ", conn);
								if (errCode != null && errCode.trim().length() > 0) {
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								} 
							}//end manish
					}
					//added by manish mhatre  on 3-jan-2020
					else if (childNodeName.equalsIgnoreCase("cctr_code__cr")) 
					{
						cctrCodeCr=checkNull(genericUtility.getColumnValue("cctr_code__cr",dom));
						acctCodeCr=checkNull(genericUtility.getColumnValue("acct_code__cr",dom));
						
						if(cctrCodeCr!=null && cctrCodeCr.trim().length() > 0)
							{
								errCode = finCommon.isCctrCode(acctCodeCr, cctrCodeCr, " ", conn);
								if (errCode != null && errCode.trim().length() > 0) {
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								} 
							}//end manish
					}
					else if (childNodeName.equalsIgnoreCase("lot_no")) 
					{

						ls_item = checkNull(genericUtility.getColumnValue("item_code", dom));
						ls_stock_lotno = checkNull(genericUtility.getColumnValue("lot_no", dom));
						ls_item_ser = checkNull(genericUtility.getColumnValue("item_ser", dom1));
						ls_suppcode = checkNull(genericUtility.getColumnValue("supp_code", dom1));		
						ls_sitecode = checkNull(genericUtility.getColumnValue("site_code", dom1));
						System.out.println("ls_item"+ls_item+"ls_stock_lotno@"+ls_stock_lotno+"ls_item_ser@"+ls_item_ser+"ls_suppcode@"+ls_suppcode+"ls_sitecode@"+ls_sitecode);

						//ls_item = dw_detedit[ii_currformno].getitemstring(1,"item_code")	
						sql=" select site_code " +
								" from supplier where supp_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,ls_suppcode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							ls_site_supplier = checkNull(rs.getString(1));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						sql=" select site_supplier.site_code__ch , site_supplier.channel_partner " +
								" from site_supplier " +
								" where site_code = ? and supp_code = ? "; 
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,ls_sitecode);
						pstmt.setString(2,ls_suppcode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							ls_cp_site = checkNull(rs.getString(1));
							ls_channelpartner = checkNull(rs.getString(2));
							//:ls_cp_site , :ls_channelpartner

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("ls_cp_site@@["+ls_cp_site+"]");
						System.out.println("ls_channelpartner@@["+ls_channelpartner+"]");
						if(ls_channelpartner==null)
						{
							sql=" select case when channel_partner is null then 'N' else channel_partner end " +
									" from supplier " +
									" where supp_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,ls_suppcode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								ls_channelpartner = checkNull(rs.getString(1));

							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

						}
						else if(ls_cp_site==null)
						{
							sql="select  supplier.site_code " +
									" from supplier " +					
									" where supp_code = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,ls_suppcode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								ls_cp_site = checkNull(rs.getString(1));

							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}

						System.out.println("ls_cp_site@1@["+ls_cp_site+"]");
						System.out.println("ls_channelpartner@1@["+ls_channelpartner+"]");

						if((ls_cp_site!=null && ls_cp_site.trim().length()>0) && (ls_channelpartner.equalsIgnoreCase("Y")))
						{
							sql="select count(*) " +
									" from item_lot_own " +
									" where site_code       = ? and " +
									" item_code       = ? and " +
									" item_ser        = ? and " +
									" site_code__supp = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,ls_sitecode);
							pstmt.setString(2,ls_item);
							pstmt.setString(3,ls_item_ser);
							pstmt.setString(4,ls_site_supplier);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								li_return = rs.getInt(1);

							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(li_return==0)
							{
								sql="select count(*) " +
										" from item_lot_own "+
										"where site_code       = ? and " +
										" item_code       = ? and " +
										" site_code__supp = ? and " +
										" lot_no__from   <= ? and " +
										" lot_no__to     >= ? ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1,ls_sitecode);
								pstmt.setString(2,ls_item);
								pstmt.setString(3,ls_site_supplier);
								pstmt.setString(4,ls_stock_lotno);
								pstmt.setString(5,ls_stock_lotno);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									ll_ret = rs.getInt(1);

								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(ll_ret==0)
								{
									//VMSUPSITE1
									errCode = "VMSUPSITE1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}else
						{
							sql="select count(*)  " +
									" from item_lot_own " +
									" where site_code       = ? and "+
									" item_code       = ? and " +
									" item_ser        = ? and " +
									" site_code__supp = ? and " +
									" lot_no__from   <= ? and " +
									" lot_no__to     >= ? " ;
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,ls_sitecode);
							pstmt.setString(2,ls_item);
							pstmt.setString(3,ls_item_ser);
							pstmt.setString(4,ls_site_supplier);
							pstmt.setString(5,ls_stock_lotno);
							pstmt.setString(6,ls_stock_lotno);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								li_return = rs.getInt(1);

							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							/*if(li_return==0)
							{
								errCode = "VMLOTOWN";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}*/

						}	
						//Pavan Rane 09sep19 start[to validate line item lot on porcpdet]
						tranIdRcp = checkNull(genericUtility.getColumnValue("tran_id__rcp",dom));						
						lineNoRcp = checkNull(genericUtility.getColumnValue("line_no__rcp", dom));
						System.out.println("Validation LotNo["+tranIdRcp+"]lineNoRcp["+lineNoRcp+"]ls_item["+ls_item+"]ls_stock_lotno["+ls_stock_lotno+"]");
						if(tranIdRcp != null && tranIdRcp.trim().length() > 0)
						{
							if(lineNoRcp != null && lineNoRcp.trim().length() > 0)
							{
								lineNoRcp = "    " + lineNoRcp.trim();
								lineNoRcp = lineNoRcp.substring(lineNoRcp.length() - 3,lineNoRcp.length());
							}
							ll_ret = 0;
							sql="select count(*) from porcpdet where tran_id = ? and line_no = ? and item_code = ? and lot_no = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,tranIdRcp);
							pstmt.setString(2,lineNoRcp);
							pstmt.setString(3,ls_item);
							pstmt.setString(4,ls_stock_lotno);
							rs = pstmt.executeQuery();
							if (rs.next())
							{						
								ll_ret = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(ll_ret == 0)
							{
								System.out.println("Lot_no porcp line-item-lot mismatch...");
								errCode = "VTINVRCPNO";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

						}
						//Pavan Rane 09sep19 end[to validate line item lot on porcpdet]
					}	
					//Added by sarita for line_no__rcp Validation on 16 OCT 2018 [START]
					if("line_no__rcp".equalsIgnoreCase(childNodeName))
					{
						System.out.println(" ********* Inside [line_no__rcp] Validation ************");
						tranIdRef = checkNull(genericUtility.getColumnValue("tran_id__ref", dom1));
						lineNoRcp = checkNull(genericUtility.getColumnValue("line_no__rcp", dom));						
						System.out.println("tran_id__ref ["+tranIdRef+"] \t line_no__rcp ["+lineNoRcp+"]");

						if(tranIdRef != null && tranIdRef.trim().length() > 0)
						{
							if(lineNoRcp == null || lineNoRcp.trim().length() == 0)
							{
								errCode = "VTINVRCPNO";//Invalid line no Rcp
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							//Added by sarita on 24 OCT 2018 to validate line No for wrong lineNo[START]
							else
							{				
								ArrayList<Integer> linNoList = new ArrayList<Integer>();
								lineNoRcp = lineNoRcp.trim(); System.out.println("lineNoRcp["+lineNoRcp+"]");//Added by sarita on 25 JANUARY 2018
								int lineNo = Integer.parseInt(lineNoRcp);
								sql = "select line_no from porcpdet where tran_id = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1,tranIdRef);
								rs = pstmt.executeQuery();
								while(rs.next())
								{
									lineCnt = rs.getInt("line_no"); System.out.println("lineNo["+lineNo+"] \t lineCnt["+lineCnt+"]");
									linNoList.add(lineCnt);
								}
								if(rs != null) {
									rs.close();
									rs = null; }
								if(pstmt != null) {
									pstmt.close();
									pstmt = null; }
								System.out.println("linNoList ["+linNoList.isEmpty()+"] \t Size ["+linNoList.size()+"] \t ["+linNoList+"]");
								if((linNoList.isEmpty() == false) && (!(linNoList.contains(lineNo))))
								{
									errCode = "VTINVRCPNO";//Invalid line no Rcp
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							//Added by sarita on 24 OCT 2018 to validate line No for wrong lineNo[END]
						}
					}
					//Added by sarita for line_no__rcp Validation on 16 OCT 2018 [END]
					//Pavan R 04sept19 start[to validate tax environment]
					else if (childNodeName.equalsIgnoreCase("tax_env")) 
					{
						//String taxEnv  = genericUtility.getColumnValue("tax_env", dom);
						String taxEnv = discommon.getParentColumnValue("tax_env", dom, "2");
						System.out.println("POR 2 tax_env["+taxEnv+"]");
						tranDateStr = genericUtility.getColumnValue("tran_date", dom1);
						System.out.println("@@@@ Tran Date[" + tranDateStr + "]");
						TranDate = Timestamp.valueOf(genericUtility.getValidDateString(tranDateStr.toString(),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");
						if (taxEnv !=null && taxEnv.trim().length() > 0)
						{ 
							cnt = 0;
							sql = "Select Count(*) from taxenv  where tax_env = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, taxEnv);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt(1);							
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(cnt == 0)
							{
								errCode = "VTTAXENV1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else
							{
								errCode = discommon.getCheckTaxEnvStatus(taxEnv, TranDate, "P", conn);
								if(errCode != null && errCode.trim().length() > 0) {
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());									
								}
							}							
						}
					}
					//Pavan R 04sept19 end[to validate tax environment]
				}
			}
			int errListSize = errList.size();
			cnt = 0;
			String errFldName = null;
			System.out.println("errListSize [" + errListSize+ "] errFields size [" + errFields.size() + "]");
			if (errList != null && errListSize > 0) 
			{
				System.out.println("errList["+ errList.toString() +"]");
				for (cnt = 0; cnt < errListSize; cnt++) {
					errCode = errList.get((int) cnt);
					errFldName = errFields.get((int) cnt);
					System.out.println("errFldName ..........[" + errFldName+"]");
					System.out.println("errCode .........." + errCode);
					errString = getErrorString(errFldName, errCode, userId);
					System.out.println("errString ..........[" + errString+"]");
					errorType = errorType(conn, errCode);
					if("VTUREC1".equalsIgnoreCase(errCode))
					{
						errCode = "VTUREC1";
						errCode = new ValidatorEJB().getErrorString("", errCode, "");
						System.out.println("::: errCode["+errCode+"]");
						startStr = errCode.substring(0,errCode.indexOf("<description>") + 13);
						endStr = errCode.substring(errCode.indexOf("</description>"),errCode.length());
						System.out.println("endStr ["+endStr+"]");
						descrStr = errCode.substring(errCode.indexOf("<description>") + 13,errCode.indexOf("</description>"));
						System.out.println("::: descrStr ["+descrStr+"]");
						descrStart = descrStr.substring(0, descrStr.indexOf("]"));
						descrEnd = descrStr.substring(descrStr.indexOf("]"),descrStr.length());
						System.out.println("lsmsg["+lsmsg+"]");
						value ="" + lsmsg;
						System.out.println("Value ::: "+ value);
						descrStart = descrStart.concat(value).concat(descrEnd);
						errCode = startStr.concat(descrStart).concat(endStr);
					}

					System.out.println("errorType ..........[" + errorType+"]");
					if (errString.length() > 0) 
					{
						String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8,errString.indexOf("<trace>"));
						bifurErrString = bifurErrString + errString.substring(errString.indexOf("</trace>") + 8,errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						errString = "";
					}
					if (errorType.equalsIgnoreCase("E")) 
					{
						break;
					}
				}	
				errList.clear();
				errList = null;
				errFields.clear();
				errFields = null;
				errStringXml.append("</Errors> </Root> \r\n");
				System.out.println("Error String@@@"+errStringXml.toString());
			}
			else
			{
				errStringXml = new StringBuffer("");
			}




		}catch (Exception e)
		{
			e.printStackTrace();
			errString = e.getMessage();
			throw new ITMException(e);
		} finally {
			try {
				if (conn != null) {
					if (rs != null) {
						rs.close();
						rs = null;
					}
					if (pstmt != null) {
						pstmt.close();
						pstmt = null;
					}
					conn.close();
				}
				conn = null;
			} catch (Exception d) {
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		errString = errStringXml.toString();
		return errString;


	}

	private String errorType(Connection conn, String errorCode) {
		String msgType = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, errorCode);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				msgType = rs.getString("MSG_TYPE");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return msgType;
	}

	public String itemChanged(String xmlString, String xmlString1,
			String xmlString2, String objContext, String currentColumn,
			String editFlag, String xtraParams) throws RemoteException,
	ITMException {
		System.out
		.println("###########ITEMCHANGE FOR CASE FIrst Method###################");
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		System.out.println("XmlString is " + xmlString);
		System.out.println("XmlString1 is " + xmlString1);
		System.out.println("XmlString2 is " + xmlString2);
		System.out.println("itemChanged() called for PorcpIC");
		String valueXmlString = "";
		try {
			if (xmlString != null && xmlString.trim().length() > 0) {
				System.out.println("XmlString is " + xmlString);
				dom = parseString(xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0) {
				dom1 = parseString(xmlString1);
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0) {
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext,
					currentColumn, editFlag, xtraParams);
		} catch (Exception e) {
			System.out
			.println("Exception : [PoReturnIC][itemChanged( String, String )] :==>\n"
					+ e.getMessage());
			//Added by sarita to throw ITMException [START].
			throw new ITMException(e);
			//Added by sarita to throw ITMException [END]
		}
		return valueXmlString;
	}

	public void gbf_exchrate_protect(String as_currcode, String as_sitecode,
			String as_exchrate_col)// Added by fatema - 26/02/2007 - DI7SUN0035
	{/*
	 * String ls_currcode_base="",sql="",fin_entity=""; StringBuffer
	 * valueXmlString=new K(); Connection conn = null;
	 * PreparedStatement pstmt = null; ResultSet rs = null; String val="";
	 * 
	 * try {
	 * 
	 * if (as_currcode!=null || as_currcode.trim().length() > 0) {
	 * sql="select a.curr_code  " + " from finent a, site b " +
	 * " where b.fin_entity = a.fin_entity " + " and b.site_code = ? "; pstmt =
	 * conn.prepareStatement(sql); //pstmt.setString(1, fin_entity);
	 * pstmt.setString(1, as_sitecode); rs = pstmt.executeQuery(); if
	 * (rs.next()) { ls_currcode_base = checkNull(rs.getString(1)); }
	 * rs.close(); rs = null; pstmt.close(); pstmt = null;
	 * 
	 * if (ls_currcode_base !=null || ls_currcode_base.trim().length() > 0 &&
	 * ls_currcode_base.equalsIgnoreCase(as_currcode.trim())) {
	 * valueXmlString.append
	 * ("<as_exchrate_col protect =\"1\">").append("<![CDATA[]]>"
	 * ).append("</as_exchrate_col>"); } else { //gbf_itemchg_modifier_ds
	 * (dw_edit,as_exchrate_col,"protect","0");
	 * valueXmlString.append("<as_exchrate_col protect =\"0\">"
	 * ).append("<![CDATA[]]>").append("</as_exchrate_col>"); }
	 * 
	 * //val=valueXmlString.toString(); //return valueXmlString;
	 * 
	 * } } catch (Exception e) { // TODO: handle exception e.printStackTrace();
	 * }
	 */
	}

	public String itemChanged(Document dom, Document dom1, Document dom2,
			String objContext, String currentColumn, String editFlag,
			String xtraParams) throws RemoteException, ITMException {

		System.out.println("###########ITEMCHANGE FOR CASE###################");
		String childNodeName = null;
		String sql = "";
		StringBuffer valueXmlString = new StringBuffer();
		int ctr = 0, cnt = 0;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int currentFormNo = 0;
		// GenericUtility genericUtility = GenericUtility.getInstance();
		ConnDriver connDriver = new ConnDriver();
		String colname = "", mcode = "", mcode1 = "", descr = "", mTrancd = "", mSdesc = "", mScode = "", mCurr = "";
		DistCommon disscommon = new DistCommon();
		FinCommon fincommon = new FinCommon();
		ArrayList lcstdqty1 = null;
		Timestamp timestamp = null;
		SimpleDateFormat sdf = null;
		SimpleDateFormat sdf1 = null;
		ArrayList acctDetrList = new ArrayList();

		UtilMethods utilMethod = new UtilMethods();

		// //tdate=null, scode="",tcode=""
		/*** oldup */

		String ls_site = "", ls_po = "", ls_supp = "", ls_dcno = "";
		String ls_trancode = "", ls_curr = "", ls_tmode = "", ls_itemse = "", ls_is_descr = "", ls_tranname = "", ls_transer = "";
		String ls_site_dlv = "", ls_retop = "", ls_allowtax = "", ls_pord_type = "", ls_supp_ship = "", descr_ship = "", ls_posttype = "";
		String ls_currcode = "", ls_sitecode = "", ls_currcode_base = "", ls_post_prov = "";
		String ls_acct_pdr = "", ls_acct_pcr = "", ls_cctr_pdr = "", ls_cctr_pcr = "", ls_acdr = "", ls_accr = "";
		String ls_singleord = ""; // Added - Gulzar - 21/12/05
		double lc_exch = 0, lc_exchrate = 0;
		Timestamp mtaxdt = null, tdate = null, ldttrandt = null, ldtdc = null, ldtlrdate = null, ldtchallandt = null, ldtmfgdate = null, ldtexpirydate = null, ldttrandate = null;

		String columnValue = "";
		Timestamp ldt_dcdate = null, ld_tax_date = null;
		String ls_login_site = "", ls_site_descr = "", mpord = "", ls_tran_type = "", ls_qcreqd = "", ls_tran_id = "";
		String mItem = "";
		String maccr = "";
		String ls_tranid_ref = "";
		String ls_unit_std = "", ls_pack = "", ls_pack_instr = "", ls_batch = "", ls_cancelbo = "";
		String ls_supp_mnfr = "", ls_site_mfg = "", ls_invoice_id = "", fr_station = "", ls_site_code = "", ls_tax_chap = "";
		String ls_tax_class = "", ls_cust_code = "", ls_item_ser = "", ls_tax_env = "", to_station = "", ls_supp_code = "";
		String ls_grade = "", ls_pricelist__clg = "", ls_tax_ref = "", ls_dept_code = "";
		double mQty = 0, mPending = 0;
		double mNum = 0, mNum1 = 0, mNum2 = 0, mNum3 = 0, mStdqty = 0, lc_tare_wt = 0, lc_net_wt = 0;
		double lc_pt_perc = 0, lc_gross_wt = 0, lc_pot_perc = 0, lc_rate__clg = 0;
		String ls_itemser = "";
		String msdesc = "";
		Timestamp mtaxdt1 = null;
		String ld_tax_dateStr="",d_tax_ref_dateStr="",ldt_dcdateStr="";
		Timestamp d_tax_ref_date = null;
		double mQtyConv = 0, mRtConv = 0, lc_conv_temp = 0, lc_conv_qty_stduom = 0;
		double lc_rate_st = 0, mrate = 0, lc_stdrate = 0;
		String lsautogeneratelotno = "", lsNull = "", lsLotNoManualSite = "", lssitestring = "", lsexit = "", lsgenlotauto = "";
		double mdiscount = 0, mtax = 0, mtotamt = 0, mcancperc = 0, lc_no_art = 0;
		Timestamp ldt_mfgdt = null, ldt_expdt = null, ldt_trandt = null, ld_tax_ref_date = null;
		String ls_pricelist = "", ls_loccoderej = "", ls_lineno = "", ls_tranid_rcp = "", ls_lineno__rcp = "";
		String lc_ret_opt = "",acceptCriteria="";
		// 29/8/03 Jyoti. Making DB2 compliant by handling '' in setitem
		String ls_nullvar = "";
		String ls_tranidref = ""; // v-1.01-Added by Tara-15-02-05-For default
		// itemchange on edit mode
		long ll_cnt = 0, ll_tax = 0;
		String sysDate = "";
		boolean cpFlag = false;
		double mPcnt = 0;

		try {
			System.out
			.println("**********ITEMCHANGE FOR CASE*********************");
			//sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			connDriver = null;
			DistCommon distComm = new DistCommon();
			FinCommon finCommon = new FinCommon();
			ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();

			String currAppdate = "";
			java.sql.Timestamp currDate = null;
			currDate = new java.sql.Timestamp(System.currentTimeMillis());
			currAppdate = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(currDate).toString();
			System.out.println("Tran date is" + currAppdate);

			Calendar currentDate = Calendar.getInstance();
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			sysDate = sdf.format(currentDate.getTime());
			System.out.println("Now the date is :=>  " + sysDate);

			if (objContext != null && objContext.trim().length() > 0) {
				currentFormNo = Integer.parseInt(objContext);
			}
			String loginSiteCode = getValueFromXTRA_PARAMS(xtraParams,
					"loginSiteCode");
			String chguser = getValueFromXTRA_PARAMS(xtraParams, "loginCode");

			String chgtermhdr = getValueFromXTRA_PARAMS(xtraParams, "chgTerm");
			// sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			timestamp = new Timestamp(System.currentTimeMillis());
			String date = (sdf.format(timestamp).toString()).trim();
			System.out.println("loginSite[" + loginSiteCode + "][chguserhdr "
					+ chguser + "][ld_date" + date + "]");
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");

			System.out.println("Current Form No [" + currentFormNo + "]");

			System.out.println("**********ITEMCHANGE FOR CASE" + currentFormNo
					+ "**************");
			switch (currentFormNo) {
			case 1:
				System.out
				.println("**********************In case 1 ***********************");

				parentNodeList = dom.getElementsByTagName("Detail1");
				System.out.println("ParentNodeList" + parentNodeList);
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0;
				valueXmlString.append("<Detail1>");
				int childNodeListLength = childNodeList.getLength();
				do {
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equals(currentColumn)) {
						if (childNode.getFirstChild() != null) {
							columnValue = childNode.getFirstChild()
									.getNodeValue().trim();
						}
					}
					ctr++;
				} while (ctr < childNodeListLength
						&& !childNodeName.equals(currentColumn));
				System.out.println("@@@@@@@[" + currentColumn + "] ==> '"
						+ currentColumn + "'");

				if (currentColumn.trim().equalsIgnoreCase("itm_defaultedit")) {

					ls_tranidref = checkNull(genericUtility.getColumnValue(
							"tran_id__ref", dom));
					System.out.println("ls_tranidref[" + ls_tranidref + "]");
					ls_tran_id = checkNull(genericUtility.getColumnValue(
							"tran_id", dom));
					String po=checkNull(genericUtility.getColumnValue(
							"purc_order", dom));
					String itmSer=checkNull(genericUtility.getColumnValue(
							"item_ser", dom));
					//String po=checkNull(genericUtility.getColumnValue(
					//		"rec_date", dom));
					String dcNo=checkNull(genericUtility.getColumnValue(
							"dc_no", dom));
					String supp_code=checkNull(genericUtility.getColumnValue(
							"supp_code", dom));
					String qc_reqd=checkNull(genericUtility.getColumnValue(
							"qc_reqd", dom));
					String ret_opt=checkNull(genericUtility.getColumnValue(
							"ret_opt", dom));
					String reciept_type=checkNull(genericUtility.getColumnValue(
							"reciept_type", dom));
					String exch_rate=checkNull(genericUtility.getColumnValue(
							"exch_rate", dom));
					double exchRate=exch_rate==null?0:Double.parseDouble(exch_rate);


					if (ls_tranidref != null && ls_tranidref.trim().length() > 0) {

						valueXmlString.append("<purc_order protect =\"1\">").append("<![CDATA[" + po + "]]>").append("</purc_order>");
						valueXmlString.append("<item_ser protect =\"1\">").append("<![CDATA[" + itmSer + "]]>").append("</item_ser>");
						valueXmlString.append("<rec_date protect =\"1\">").append("<![CDATA["+""+ "]]>").append("</rec_date>");
						valueXmlString.append("<dc_no protect =\"1\">").append("<![CDATA[" + dcNo + "]]>").append("</dc_no>");
						valueXmlString.append("<dc_date protect =\"1\">").append("<![CDATA[" + "" + "]]>").append("</dc_date>");

					} else {
						valueXmlString.append("<purc_order protect =\"0\">").append("<![CDATA[" + po + "]]>").append("</purc_order>");
						valueXmlString.append("<item_ser protect =\"0\">").append("<![CDATA[" + itmSer + "]]>").append("</item_ser>");
						valueXmlString.append("<rec_date protect =\"0\">").append("<![CDATA[" + "" + "]]>").append("</rec_date>");
						valueXmlString.append("<dc_no protect =\"0\">").append("<![CDATA[" + dcNo + "]]>").append("</dc_no>");
						valueXmlString.append("<dc_date protect =\"0\">").append("<![CDATA[" + "" + "]]>").append("</dc_date>");

					}

					sql = "select count(1)  from porcpdet where tran_id = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, ls_tran_id);
					// pstmt.setString(2, lssite);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						ll_cnt = rs.getInt(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if (ll_cnt > 0) {
						//valueXmlString.append("<tran_id__ref protect =\"1\">").append("</tran_id__ref>");
						valueXmlString.append("<tran_id__ref protect =\"1\">").append("<![CDATA[" + ls_tranidref + "]]>").append("</tran_id__ref>");
						valueXmlString.append("<supp_code protect =\"1\">").append("<![CDATA[" + supp_code + "]]>").append("</supp_code>");

					}
					valueXmlString.append("<qc_reqd protect =\"1\">").append("<![CDATA[" + qc_reqd + "]]>").append("</qc_reqd>");
					valueXmlString.append("<ret_opt protect =\"0\">").append("<![CDATA[" + ret_opt + "]]>").append("</ret_opt>");
					valueXmlString.append("<reciept_type protect =\"1\">").append("<![CDATA[" + reciept_type + "]]>").append("</reciept_type>");
					valueXmlString.append("<exch_rate protect =\"1\">").append("<![CDATA[" + exchRate + "]]>").append("</exch_rate>");
					valueXmlString.append("<exch_rate__frt protect =\"1\">").append("</exch_rate__frt>");
					valueXmlString.append("<exch_rate__ins protect =\"1\">").append("</exch_rate__ins>");
					valueXmlString.append("<exch_rate__clr protect =\"1\">").append("</exch_rate__clr>");
					valueXmlString.append("<exch_rate__othch protect =\"1\">").append("</exch_rate__othch>");

				} else if (currentColumn.trim().equalsIgnoreCase("itm_default")) {
					String gs_run_mode = "B";
					String login_site = genericUtility.getValueFromXTRA_PARAMS(
							xtraParams, "loginSiteCode");
					String loginEmpCode = genericUtility
							.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
					valueXmlString.append("<purc_order protect =\"0\">").append("</purc_order>");

					sql = "select descr from site where site_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, login_site);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						ls_site_descr = rs.getString(1);
						// lctareweight = rs.getDouble(2);

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					java.util.Date date1 = new java.util.Date();
					SimpleDateFormat timeFormat = new SimpleDateFormat(" hh:mm:ss.M");
					System.out.println(timeFormat.format(date1));

					valueXmlString.append("<site_code>").append("<![CDATA[" + login_site + "]]>").append("</site_code>");
					valueXmlString.append("<site_descr>").append("<![CDATA[" + ls_site_descr + "]]>").append("</site_descr>");
					valueXmlString.append("<tran_date>").append("<![CDATA[" + sysDate + "]]>").append("</tran_date>");
					valueXmlString.append("<tax_date>").append("<![CDATA[" + sysDate + "]]>").append("</tax_date>");
					valueXmlString.append("<eff_date>").append("<![CDATA[" + sysDate + "]]>").append("</eff_date>");
					valueXmlString.append("<excise_ref_date><![CDATA[").append( currAppdate == null ? "" : currAppdate + timeFormat.format(date1)  ).append("]]></excise_ref_date>");//
					String val = "P-RET", val1 = "R", val2 = "N", val3 = "C";
					valueXmlString.append("<tran_ser>").append("<![CDATA[" + val + "]]>").append("</tran_ser>");
					valueXmlString.append("<trans_mode>").append(val1).append("</trans_mode>");
					valueXmlString.append("<qc_reqd>").append("<![CDATA[" + val2 + "]]>").append("</qc_reqd>");

					valueXmlString.append("<qc_reqd protect =\"1\">").append("<![CDATA[" + val2 + "]]>").append("</qc_reqd>");
					valueXmlString.append("<ret_opt>").append("<![CDATA[" + val3 + "]]>").append("</ret_opt>");
					valueXmlString.append("<reciept_type protect =\"1\">").append("<![CDATA[]]>").append("</reciept_type>");
					valueXmlString.append("<emp_code>").append("<![CDATA[" + loginEmpCode + "]]>").append("</emp_code>");
					if (!"B".equals(gs_run_mode)) {
						// dw_edit.Setitem(1, "emp_code", login_emp_code)
						valueXmlString.append("<emp_code>").append("<![CDATA[" + loginEmpCode + "]]>").append("</emp_code>");
					}

				} else if (currentColumn.trim()
						.equalsIgnoreCase("tran_id__ref")) {
					mcode = checkNull(genericUtility.getColumnValue(
							"tran_id__ref", dom));
					if (mcode != null && mcode.trim().length() > 0) {
						sql = " select     site_code    ,    purc_order    ,    supp_code        ,    dc_no    , "
								+ " dc_date        ,    tran_code    ,  curr_code        ,    exch_rate, "
								+ " trans_mode    ,    item_ser        ,    supp_code__ship,     post_type, "
								+ " tax            ,    tax_date        ,  tax_ref            ,    tax_ref_date, "
								+ " post_prov " +

								" from     porcp " + " where tran_id = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mcode);
						rs = pstmt.executeQuery();
						if (rs.next()) {

							ls_site = checkNull(rs.getString(1));
							ls_po = checkNull(rs.getString(2));
							ls_supp = checkNull(rs.getString(3));
							ls_dcno = checkNull(rs.getString(4));
							ldt_dcdate = rs.getTimestamp(5);
							ls_trancode = checkNull(rs.getString(6));
							ls_curr = checkNull(rs.getString(7));
							lc_exch = rs.getDouble(8);
							ls_tmode = checkNull(rs.getString(9));
							ls_itemser = checkNull(rs.getString(10));
							ls_supp_ship = checkNull(rs.getString(11));
							ls_posttype = checkNull(rs.getString(12));
							ll_tax = rs.getLong(13);
							ld_tax_date = rs.getTimestamp(14);
							ls_tax_ref = checkNull(rs.getString(15));
							d_tax_ref_date = rs.getTimestamp(16);
							ls_post_prov = checkNull(rs.getString(17));

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						System.out.println("ls_supp_ship@" + ls_supp_ship);
						System.out.println("ld_tax_date@[" + ld_tax_date+"]");
						System.out.println("d_tax_ref_date@[" + d_tax_ref_date+"]");
						System.out.println("ldt_dcdate@[" + ldt_dcdate+"]");
						if(ld_tax_date!=null)
						{
							ld_tax_dateStr=sdf.format(ld_tax_date).toString();
							//ld_tax_date = Timestamp.valueOf(genericUtility.getValidDateString(ld_tax_date.toString(),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()));
						}
						if(d_tax_ref_date!=null)
						{	
							d_tax_ref_dateStr=sdf.format(d_tax_ref_date).toString();
							//d_tax_ref_date = Timestamp.valueOf(genericUtility.getValidDateString(d_tax_ref_date.toString(),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+ " 00:00:00.0");
						}
						if(ldt_dcdate!=null)
						{
							ldt_dcdateStr=sdf.format(ldt_dcdate).toString();
							//ld_tax_date = Timestamp.valueOf(genericUtility.getValidDateString(ld_tax_date.toString(),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()));
						}
						System.out.println("ld_tax_dateStr@[" + ld_tax_dateStr+"]");
						System.out.println("d_tax_ref_dateStr@[" + d_tax_ref_dateStr+"]");
						System.out.println("ldt_dcdateStr@[" + ldt_dcdateStr+"]");

						// ldt_dcdate =
						// Timestamp.valueOf(genericUtility.getValidDateString(ldt_dcdate.toString(),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+
						// " 00:00:00.0");
						sql = "select supp_name from supplier where supp_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, ls_supp);
						// pstmt.setString(2, lssite);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							descr = checkNull(rs.getString(1));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("descr@supp_name@" + descr);
						sql = "select supp_name  from supplier where supp_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, ls_supp_ship);
						// pstmt.setString(2, lssite);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							descr_ship = checkNull(rs.getString(1));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("descr@descr_ship@" + descr_ship);
						sql = "Select descr from site where site_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, ls_site);
						// pstmt.setString(2, lssite);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							mSdesc = checkNull(rs.getString(1));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("descr@mSdesc@" + mSdesc);
						sql = "Select descr from itemser where item_ser = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, ls_itemser);
						// pstmt.setString(2, lssite);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							ls_is_descr = checkNull(rs.getString(1));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("descr@ls_is_descr@" + ls_is_descr);
						sql = "select tran_name  from transporter where tran_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, ls_trancode);
						// pstmt.setString(2, lssite);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							ls_tranname = checkNull(rs.getString(1));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("descr@ls_tranname@" + ls_tranname);
						valueXmlString.append("<site_code>")
						.append("<![CDATA[" + ls_site + "]]>")
						.append("</site_code>");
						valueXmlString.append("<site_descr>")
						.append("<![CDATA[" + mSdesc + "]]>")
						.append("</site_descr>");
						valueXmlString.append("<purc_order>")
						.append("<![CDATA[" + ls_po + "]]>")
						.append("</purc_order>");
						valueXmlString.append("<supp_code>")
						.append("<![CDATA[" + ls_supp + "]]>")
						.append("</supp_code>");
						valueXmlString.append("<supp_name>")
						.append("<![CDATA[" + descr + "]]>")
						.append("</supp_name>");
						valueXmlString.append("<tran_code>")
						.append("<![CDATA[" + ls_trancode + "]]>")
						.append("</tran_code>");
						valueXmlString.append("<transporter_name>")
						.append("<![CDATA[" + ls_tranname + "]]>")
						.append("</transporter_name>");
						valueXmlString.append("<item_ser>")
						.append("<![CDATA[" + ls_itemser + "]]>")
						.append("</item_ser>");
						valueXmlString.append("<itemser_descr>")
						.append("<![CDATA[" + ls_is_descr + "]]>")
						.append("</itemser_descr>");
						valueXmlString.append("<curr_code>")
						.append("<![CDATA[" + ls_curr + "]]>")
						.append("</curr_code>");

						valueXmlString.append("<exch_rate>")
						.append("<![CDATA[" + lc_exch + "]]>")
						.append("</exch_rate>");
						valueXmlString.append("<trans_mode>")
						.append("<![CDATA[" + ls_tmode + "]]>")
						.append("</trans_mode>");
						valueXmlString.append("<dc_no>")
						.append("<![CDATA[" + ls_dcno + "]]>")
						.append("</dc_no>");
						valueXmlString.append("<dc_date>")
						.append("<![CDATA[" + ldt_dcdateStr + "]]>")
						.append("</dc_date>");

						/*valueXmlString.append("<tax_date>")
						.append("<![CDATA[" + ld_tax_dateStr + "]]>")
                        .append("</tax_date>");*/
                        valueXmlString.append("<tax_date>")
						.append("<![CDATA[" + sysDate + "]]>")
						.append("</tax_date>"); //Added by Anagha R on 16/10/2020 to resolve Error while confirming Purchase Return(#2277)
						valueXmlString.append("<tax_ref>")
						.append("<![CDATA[" + ls_tax_ref + "]]>")
						.append("</tax_ref>");
						valueXmlString.append("<tax_ref_date>")
						.append("<![CDATA[" + d_tax_ref_dateStr + "]]>")
						.append("</tax_ref_date>");

						if (ls_post_prov == null
								|| ls_post_prov.trim().length() == 0) {
							ls_post_prov = "N";
						}
						valueXmlString.append("<post_prov>")
						.append("<![CDATA[" + ls_post_prov + "]]>")
						.append("</post_prov>");
						valueXmlString.append("<supp_code__ship>")
						.append("<![CDATA[" + ls_supp_ship + "]]>")
						.append("</supp_code__ship>");
						valueXmlString.append("<supp_name__ship>")
						.append("<![CDATA[" + descr_ship + "]]>")
						.append("</supp_name__ship>");

						valueXmlString.append("<purc_order protect =\"1\">")
						.append("<![CDATA["+ls_po+"]]>").append("</purc_order>");
						valueXmlString.append("<item_ser protect =\"1\">")
						.append("<![CDATA["+ls_itemser+"]]>").append("</item_ser>");
						valueXmlString.append("<rec_date protect =\"1\">")
						.append("<![CDATA[]]>").append("</rec_date>");

						//this.gbf_exchrate_protect(ls_curr, ls_site, "exch_rate");
						//Pavan Rane 11jun19 start [to store the channel partner flag]
						cpFlag = isChannelPartnerSupp(ls_supp, ls_site, conn);
						if(cpFlag)
						{
							valueXmlString.append("<channel_partner><![CDATA[" + "Y" + "]]></channel_partner>");
						}else 
						{
							valueXmlString.append("<channel_partner><![CDATA[" + "N" + "]]></channel_partner>");					
						}
						//Pavan Rane 11jun19 end
						if (ls_curr != null
								&& ls_curr.trim().length() > 0)
						{
							sql = "select a.curr_code  "
									+ " from finent a, site b "
									+ " where b.fin_entity = a.fin_entity "
									+ " and b.site_code = ? ";
							pstmt = conn.prepareStatement(sql);

							pstmt.setString(1, ls_site);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								ls_currcode_base = checkNull(rs.getString(1));
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (ls_currcode_base != null
									&& ls_currcode_base.trim().length() > 0
									&& ls_currcode_base.trim()
									.equalsIgnoreCase(ls_curr
											.trim())) {

								System.out.println("if@@ls_currcode_base["+ls_currcode_base+"]mCurr["+ls_curr);
								valueXmlString
								.append("<exch_rate protect =\"1\">")
								.append("<![CDATA["+lc_exch+"]]>")
								.append("</exch_rate>");
							} else {
								System.out.println("else@@ls_currcode_base["+ls_currcode_base+"]mCurr["+ls_curr);
								valueXmlString
								.append("<exch_rate protect =\"0\">")
								.append("<![CDATA["+lc_exch+"]]>")
								.append("</exch_rate>");
							}

						}


						sql = "select supp_code, tax_date, curr_code, exch_rate,tran_code, "
								+ " site_code__dlv,item_ser, pord_type "
								+ " from Porder where Purc_order = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, ls_po);
						// pstmt.setString(2, lssite);
						rs = pstmt.executeQuery();
						if (rs.next()) {

							mScode = checkNull(rs.getString(1));
							mtaxdt = rs.getTimestamp(2);
							mCurr = checkNull(rs.getString(3));
							mPcnt = rs.getDouble(4);
							ls_trancode = checkNull(rs.getString(5));
							ls_site_dlv = checkNull(rs.getString(6));
							ls_itemser = checkNull(rs.getString(7));
							ls_pord_type = checkNull(rs.getString(8));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						valueXmlString.append("<pord_type>")
						.append("<![CDATA[" + ls_pord_type + "]]>")
						.append("</pord_type>");
						valueXmlString.append("<post_type>")
						.append("<![CDATA[" + ls_posttype + "]]>")
						.append("</post_type>");

					} else {
						valueXmlString.append("<purc_order protect =\"0\">")
						.append("<![CDATA[]]>").append("</purc_order>");
						valueXmlString.append("<item_ser protect =\"0\">")
						.append("<![CDATA[]]>").append("</item_ser>");
						valueXmlString.append("<rec_date protect =\"0\">")
						.append("<![CDATA[]]>").append("</rec_date>");

					}
				} else if (currentColumn.trim().equalsIgnoreCase("supp_code")) {

					mcode = checkNull(genericUtility.getColumnValue(
							"supp_code", dom));
					sql = "select supp_name, tran_code from supplier "
							+ " where supp_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mcode);
					rs = pstmt.executeQuery();
					if (rs.next()) {

						descr = checkNull(rs.getString(1));
						ls_trancode = checkNull(rs.getString(2));

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					sql = "select tran_name from transporter where tran_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, ls_trancode);
					rs = pstmt.executeQuery();
					if (rs.next()) {

						ls_tranname = checkNull(rs.getString(1));

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					//Pavan Rane 11jun19 start [to store the channel partner flag]
					mcode1 = checkNull(genericUtility.getColumnValue("site_code", dom));
					cpFlag = isChannelPartnerSupp(mcode, mcode1, conn);
					if(cpFlag)
					{
						valueXmlString.append("<channel_partner><![CDATA[" + "Y" + "]]></channel_partner>");
					}else 
					{
						valueXmlString.append("<channel_partner><![CDATA[" + "N" + "]]></channel_partner>");					
					}
					//Pavan Rane 11jun19 end

					valueXmlString.append("<transporter_name>")
					.append("<![CDATA[" + ls_tranname + "]]>")
					.append("</transporter_name>");
					valueXmlString.append("<supp_name>")
					.append("<![CDATA[" + descr + "]]>")
					.append("</supp_name>");
					valueXmlString.append("<tran_code>")
					.append("<![CDATA[" + ls_trancode + "]]>")
					.append("</tran_code>");
					valueXmlString.append("<supp_code__ship>")
					.append("<![CDATA[" + mcode + "]]>")
					.append("</supp_code__ship>");
					valueXmlString.append("<supp_name__ship>")
					.append("<![CDATA[" + descr + "]]>")
					.append("</supp_name__ship>");

					if ((mcode != null && mcode.trim().length() > 0)
							&& (mcode1 != null && mcode1.trim().length() > 0)) {
						// 1. site_supplier
						sql = " select (case when qc_reqd is null then ' ' else qc_reqd end) "
								+ " from site_supplier "
								+ " where site_code = ? and supp_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mcode1);//
						pstmt.setString(2, mcode);
						rs = pstmt.executeQuery();
						if (rs.next()) {

							ls_qcreqd = checkNull(rs.getString(1));

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						// 2. supplier

						if (ls_qcreqd == null || ls_qcreqd.trim().length() == 0) {
							sql = "select (case when qc_reqd is null then ' ' else qc_reqd end) "
									+ " from supplier "
									+ " where supp_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, mcode);//
							// pstmt.setString(2, mcode);
							rs = pstmt.executeQuery();
							if (rs.next()) {

								ls_qcreqd = checkNull(rs.getString(1));

							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

						}
						if (ls_qcreqd != null && ls_qcreqd.trim().length() > 0)
						{

							if (ls_qcreqd.equals("Y")) {
								ls_qcreqd = "Y";
								valueXmlString
								.append("<qc_reqd>")
								.append("<![CDATA[" + ls_qcreqd + "]]>")
								.append("</qc_reqd>");
							} else if (ls_qcreqd.equals("N")) {
								ls_qcreqd = "N";
								valueXmlString
								.append("<qc_reqd>")
								.append("<![CDATA[" + ls_qcreqd + "]]>")
								.append("</qc_reqd>");

							}

						}

					}

				} else if (currentColumn.trim().equalsIgnoreCase(
						"supp_code__ship")) {
					mcode = checkNull(genericUtility.getColumnValue(
							"supp_code__ship", dom));
					System.out.println("supp_code__ship@@" + mcode);
					sql = "select supp_name  from supplier where supp_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mcode);//
					// pstmt.setString(2, mcode);
					rs = pstmt.executeQuery();
					if (rs.next()) {

						descr = checkNull(rs.getString(1));

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<supp_name__ship>")
					.append("<![CDATA[" + descr + "]]>")
					.append("</supp_name__ship>");

				} else if (currentColumn.trim().equalsIgnoreCase("site_code")) {
					mcode = checkNull(genericUtility.getColumnValue(
							"site_code", dom));
					sql = "Select descr " + "from Site where Site_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mcode);//
					// pstmt.setString(2, mcode);
					rs = pstmt.executeQuery();
					if (rs.next()) {

						mSdesc = checkNull(rs.getString(1));

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<site_descr>")
					.append("<![CDATA[" + mSdesc + "]]>")
					.append("</site_descr>");
					//Pavan Rane 11jun19 start [to store the channel partner flag]
					mScode = checkNull(genericUtility.getColumnValue("supp_code", dom));					
					cpFlag = isChannelPartnerSupp(mScode, mcode, conn);					
					if(cpFlag)
					{
						valueXmlString.append("<channel_partner><![CDATA[" + "Y" + "]]></channel_partner>");
					}else 
					{
						valueXmlString.append("<channel_partner><![CDATA[" + "N" + "]]></channel_partner>");					
					}
					//Pavan Rane 11jun19 end
				} else if (currentColumn.trim().equalsIgnoreCase("purc_order")) {
					mcode = checkNull(genericUtility.getColumnValue(
							"purc_order", dom));
					mcode1 = checkNull(genericUtility.getColumnValue(
							"supp_code", dom));
					//
					sql = " select     supp_code ,  tax_date ,curr_code ,exch_rate, "
							+ " tran_code , site_code__dlv , item_ser , pord_type ,accept_criteria " // accept_criteria added by nandkumar gadkari on 13/03/19
							+ "from     Porder " + " where Purc_order = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mcode);//
					// pstmt.setString(2, mcode);
					rs = pstmt.executeQuery();
					if (rs.next()) {

						// mSdesc = rs.getString(1);
						mScode = checkNull(rs.getString(1));
						mtaxdt1 = rs.getTimestamp(2);
						mCurr = checkNull(rs.getString(3));
						mPcnt = rs.getDouble(4);
						ls_trancode = checkNull(rs.getString(5));
						ls_site_dlv = checkNull(rs.getString(6));
						ls_itemser = checkNull(rs.getString(7));
						ls_pord_type = checkNull(rs.getString(8));
						acceptCriteria = checkNull(rs.getString(9));// accept_criteria added by nandkumar gadkari on 13/03/19

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println("mtaxdt1@before@[" + mtaxdt1 + "]");
					String currAppdate1 = sdf.format(mtaxdt1);
					// currAppdate = new
					// SimpleDateFormat(genericUtility.getApplDateFormat()).format(mtaxdt1).toString();
					System.out.println("Tax date is" + currAppdate1);

					if (mtaxdt1 == null) {
						mtaxdt1 = Timestamp.valueOf(genericUtility
								.getValidDateString(sysDate.toString(),
										genericUtility.getApplDateFormat(),
										genericUtility.getDBDateFormat())
								+ " 00:00:00.0");
						// mtaxdt1=timestamp.valueOf(sysDate);
						System.out.println("mtaxdt1@@[" + mtaxdt1 + "]");
					}
					sql = "Select descr from site where site_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, ls_site_dlv);//
					// pstmt.setString(2, mcode);
					rs = pstmt.executeQuery();
					if (rs.next()) {

						// mSdesc = rs.getString(1);
						mSdesc = checkNull(rs.getString(1));

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					sql = "select supp_name,tran_code " + " from     supplier "
							+ " where supp_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mScode);//
					// pstmt.setString(2, mcode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						descr = checkNull(rs.getString(1));
						ls_trancode = checkNull(rs.getString(2));

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<purc_order>")
					.append("<![CDATA[" + mcode + "]]>")
					.append("</purc_order>");
					valueXmlString.append("<supp_code>")
					.append("<![CDATA[" + mScode + "]]>")
					.append("</supp_code>");
					valueXmlString.append("<supp_name>")
					.append("<![CDATA[" + descr + "]]>")
					.append("</supp_name>");
					valueXmlString.append("<pord_type>")
					.append("<![CDATA[" + ls_pord_type + "]]>")
					.append("</pord_type>");
					valueXmlString.append("<supp_code__ship>")
					.append("<![CDATA[" + mScode + "]]>")
					.append("</supp_code__ship>");
					valueXmlString.append("<supp_name__ship>")
					.append("<![CDATA[" + descr + "]]>")
					.append("</supp_name__ship>");
					valueXmlString.append("<accept_criteria>")
					.append("<![CDATA[" + acceptCriteria + "]]>")
					.append("</accept_criteria>");// accept_criteria added by nandkumar gadkari on 13/03/19
					

					sql = "select tran_name from transporter "
							+ " where tran_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, ls_trancode);//
					// pstmt.setString(2, mcode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						ls_tranname = checkNull(rs.getString(1));
						// ls_trancode = rs.getString(2);

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					sql = "Select descr from itemser where item_ser = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, ls_itemser);//
					// pstmt.setString(2, mcode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						ls_is_descr = checkNull(rs.getString(1));
						// ls_trancode = rs.getString(2);

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<site_code>")
					.append("<![CDATA[" + ls_site_dlv + "]]>")
					.append("</site_code>");

					valueXmlString.append("<site_descr>")
					.append("<![CDATA[" + mSdesc + "]]>")
					.append("</site_descr>");
					valueXmlString
					.append("<tran_code>")
					.append("<![CDATA[" + checkNull(ls_trancode)
					+ "]]>").append("</tran_code>");
					valueXmlString.append("<transporter_name>")
					.append("<![CDATA[" + ls_tranname + "]]>")
					.append("</transporter_name>");
					String mTaxDt = "";
					/*valueXmlString.append("<tax_date>")
					.append("<![CDATA[" + currAppdate1 + "]]>")
                    .append("</tax_date>");*/
                    valueXmlString.append("<tax_date>")
					.append("<![CDATA[" + sysDate + "]]>")
					.append("</tax_date>");//Added by Anagha R on 16/10/2020 to resolve Error while confirming Purchase Return(#2277)
					valueXmlString.append("<curr_code>")
					.append("<![CDATA[" + mCurr + "]]>")
					.append("</curr_code>");
					//mPcnt=mPcnt==0? 0: mPcnt;

					valueXmlString.append("<exch_rate>")
					.append("<![CDATA[" + mPcnt + "]]>")
					.append("</exch_rate>");
					valueXmlString.append("<item_ser>")
					.append("<![CDATA[" + ls_itemser + "]]>")
					.append("</item_ser>");
					valueXmlString.append("<itemser_descr>")
					.append("<![CDATA[" + ls_is_descr + "]]>")
					.append("</itemser_descr>");

					///this.gbf_exchrate_protect(mCurr, ls_site_dlv, "ls_site_dlv");//gbf_exchrate_protect(mCurr,ls_site_dlv,'ls_site_dlv')

					// gbf_exchrate_protect(mCurr,ls_site_dlv,"exch_rate");//pravin
					//Pavan Rane 11jun19 start [to store the channel partner flag]
					cpFlag = isChannelPartnerSupp(mScode, ls_site_dlv, conn);
					if(cpFlag)
					{
						valueXmlString.append("<channel_partner><![CDATA[" + "Y" + "]]></channel_partner>");
					}else 
					{
						valueXmlString.append("<channel_partner><![CDATA[" + "N" + "]]></channel_partner>");					
					}
					//Pavan Rane 11jun19 end

					if (mCurr != null && mCurr.trim().length() > 0) {
						sql = "select a.curr_code  "
								+ " from finent a, site b "
								+ " where b.fin_entity = a.fin_entity "
								+ " and b.site_code = ? ";
						pstmt = conn.prepareStatement(sql);
						// pstmt.setString(1, fin_entity);
						pstmt.setString(1, ls_site_dlv);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							ls_currcode_base = checkNull(rs.getString(1));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						int rate = 1;
						System.out.println("before@@ls_currcode_base["+ls_currcode_base+"]mCurr["+mCurr);
						if ((ls_currcode_base != null && ls_currcode_base.trim().length() > 0) && ls_currcode_base.trim().equalsIgnoreCase(mCurr
								.trim())) {
							System.out.println("if@@ls_currcode_base["+ls_currcode_base+"]mCurr["+mCurr);
							valueXmlString.append("<exch_rate protect =\"1\">")
							.append("<![CDATA["+mPcnt+"]]>")
							.append("</exch_rate>");
						} else {
							System.out.println("else@@ls_currcode_base["+ls_currcode_base+"]mCurr["+mCurr);
							valueXmlString.append("<exch_rate protect =\"0\">")
							.append("<![CDATA["+mPcnt+"]]>")
							.append("</exch_rate>");
						}

					}

				} else if (currentColumn.trim().equalsIgnoreCase("curr_code")) {

					mcode = checkNull(genericUtility.getColumnValue(
							"curr_code", dom));
					sql = "Select std_exrt "
							+ " from currency where curr_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mcode);//
					// pstmt.setString(2, mcode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						mPcnt = rs.getInt(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<conv_fact>")
					.append("<![CDATA[" + mPcnt + "]]>")
					.append("</conv_fact>");

				} else if (currentColumn.trim().equalsIgnoreCase("gr_no")) {
					String scode = "", tcode = "";
					mcode = checkNull(genericUtility.getColumnValue("gr_no",
							dom));

					sql = " select tran_date,supp_code,tran_code  from gate_register "
							+ " where tran_id = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mcode);//
					rs = pstmt.executeQuery();
					if (rs.next()) {

						tdate = rs.getTimestamp(1);
						scode = rs.getString(2);
						tcode = rs.getString(3);

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					sql = "select supp_name  "
							+ " from supplier where supp_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mcode);//
					rs = pstmt.executeQuery();
					if (rs.next()) {

						descr = rs.getString(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<supp_name>")
					.append("<![CDATA[" + descr + "]]>")
					.append("</supp_name>");
					valueXmlString.append("<rec_date>")
					.append("<![CDATA[" + sdf.format(tdate).toString() + "]]>")
					.append("</rec_date>");
					valueXmlString.append("<supp_code>")
					.append("<![CDATA[" + sdf.format(scode).toString() + "]]>")
					.append("</supp_code>");
					valueXmlString.append("<tran_code>")
					.append("<![CDATA[" + sdf.format(tcode).toString() + "]]>")
					.append("</tran_code>");

				} else if (currentColumn.trim().equalsIgnoreCase("tran_code")) {

					mcode = checkNull(genericUtility.getColumnValue(
							"tran_code", dom));

					sql = "select tran_name from transporter where tran_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mcode);//
					rs = pstmt.executeQuery();
					if (rs.next())
					{

						ls_tranname = rs.getString(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<transporter_name>")
					.append("<![CDATA[" + ls_tranname + "]]>")
					.append("</transporter_name>");

				} else if (currentColumn.trim().equalsIgnoreCase("ret_opt")) 
				{
					String ls_retopt = checkNull(genericUtility.getColumnValue(
							"ret_opt", dom));
					if (ls_retopt.equalsIgnoreCase("C")) {
						ls_allowtax = "Y";
					} else
					{
						ls_allowtax = disscommon.getDisparams("999999",
								"CALC_TAX_ON_REPLACE", conn);
					}
					/*valueXmlString.append("<gp_ser>")
							.append("<![CDATA[" + ls_retopt + "]]>")
							.append("</gp_ser>");// dhiraj
					 */
				} else if (currentColumn.trim().equalsIgnoreCase("tran_type")) {
					// mcode = dw_edit.getitemstring(1,colname)
					mcode = checkNull(genericUtility.getColumnValue("ret_opt",
							dom));
					if (mcode == null || mcode.trim().length() == 0) {
						mcode = " ";
					}
					String retOpt = mcode.substring(0, 1);
					valueXmlString.append("<gp_ser>")
					.append("<![CDATA[" + retOpt + "]]>")
					.append("</gp_ser>");


				} else if (currentColumn.trim().equalsIgnoreCase(
						"curr_code__frt")) {
					String ldt_trandt1 = "";
					ls_currcode = checkNull(genericUtility.getColumnValue(
							"curr_code__frt", dom));
					ls_sitecode = checkNull(genericUtility.getColumnValue(
							"site_code", dom));
					ldt_trandt1 = checkNull(genericUtility.getColumnValue(
							"tran_date", dom));
					sql = "select a.curr_code    from finent a, site b "
							+ " where b.fin_entity = a.fin_entity "
							+ " and b.site_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, ls_sitecode);//
					rs = pstmt.executeQuery();
					if (rs.next()) {

						ls_currcode_base = rs.getString(1);

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					// get exchange rate
					if (ls_currcode != null && ls_currcode.trim().length() > 0) 
					{

						lc_exchrate = fincommon.getDailyExchRateSellBuy(
								ls_currcode, ls_currcode_base, ls_sitecode,
								ldt_trandt1, "B", conn);
						System.out.println("lc_exchrate@@"+lc_exchrate);
						valueXmlString.append("<exch_rate__frt>")
						.append("<![CDATA[" + lc_exchrate + "]]>")
						.append("</exch_rate__frt>");
						//this.gbf_exchrate_protect(ls_currcode,ls_sitecode,"exch_rate__frt");//pravin

						if (ls_currcode != null
								&& ls_currcode.trim().length() > 0) {
							sql = "select a.curr_code  "
									+ " from finent a, site b "
									+ " where b.fin_entity = a.fin_entity "
									+ " and b.site_code = ? ";
							pstmt = conn.prepareStatement(sql);
							// pstmt.setString(1, fin_entity);
							pstmt.setString(1, ls_sitecode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								ls_currcode_base = checkNull(rs.getString(1));
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (ls_currcode_base != null
									&& ls_currcode_base.trim().length() > 0
									&& ls_currcode_base.trim()
									.equalsIgnoreCase(ls_currcode
											.trim())) 
							{
								valueXmlString
								.append("<exch_rate__frt protect =\"1\">")
								.append("<![CDATA["+lc_exchrate+"]]>")
								.append("</exch_rate__frt>");
							} else
							{
								valueXmlString
								.append("<exch_rate__frt protect =\"0\">")
								.append("<![CDATA["+lc_exchrate+"]]>")
								.append("</exch_rate__frt>");
							}

						}

					}

				} else if (currentColumn.trim().equalsIgnoreCase(
						"curr_code__ins")) {
					String ldt_trandt1 = "";
					ls_currcode = checkNull(genericUtility.getColumnValue(
							"curr_code__ins", dom));

					ls_sitecode = checkNull(genericUtility.getColumnValue(
							"site_code", dom));
					ldt_trandt1 = checkNull(genericUtility.getColumnValue(
							"tran_date", dom));

					sql = "select a.curr_code  from finent a, site b "
							+ " where b.fin_entity = a.fin_entity "
							+ " and b.site_code = ? ";

					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, ls_sitecode);//
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						ls_currcode_base = rs.getString(1);

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if (ls_currcode != null && ls_currcode.trim().length() > 0) {

						lc_exchrate = fincommon.getDailyExchRateSellBuy(
								ls_currcode, ls_currcode_base, ls_sitecode,
								ldt_trandt1, "B", conn);

						valueXmlString.append("<exch_rate__ins>")
						.append("<![CDATA[" + lc_exchrate + "]]>")
						.append("</exch_rate__ins>");

						//this.gbf_exchrate_protect(ls_currcode,ls_sitecode,"exch_rate__ins");//pravin

						if (ls_currcode != null
								&& ls_currcode.trim().length() > 0) {
							sql = "select a.curr_code  "
									+ " from finent a, site b "
									+ " where b.fin_entity = a.fin_entity "
									+ " and b.site_code = ? ";
							pstmt = conn.prepareStatement(sql);

							pstmt.setString(1, ls_sitecode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								ls_currcode_base = checkNull(rs.getString(1));
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (ls_currcode_base != null
									&& ls_currcode_base.trim().length() > 0
									&& ls_currcode_base.trim()
									.equalsIgnoreCase(ls_currcode
											.trim())) {
								valueXmlString
								.append("<exch_rate__ins protect =\"1\">")
								.append("<![CDATA["+lc_exchrate+"]]>")
								.append("</exch_rate__ins>");
							} else {
								valueXmlString
								.append("<exch_rate__ins protect =\"0\">")
								.append("<![CDATA["+lc_exchrate+"]]>")
								.append("</exch_rate__ins>");
							}

						}

					}

				} else if (currentColumn.trim().equalsIgnoreCase(
						"curr_code__clr")) {
					String ldt_trandt1 = "";
					ls_currcode = checkNull(genericUtility.getColumnValue(
							"curr_code__clr", dom));

					ls_sitecode = checkNull(genericUtility.getColumnValue(
							"site_code", dom));
					ldt_trandt1 = checkNull(genericUtility.getColumnValue(
							"tran_date", dom));

					sql = "select a.curr_code  from finent a, site b "
							+ " where b.fin_entity = a.fin_entity "
							+ " and b.site_code = ? ";

					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, ls_sitecode);//
					rs = pstmt.executeQuery();
					if (rs.next()) {

						ls_currcode_base = rs.getString(1);

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;


					if (ls_currcode != null && ls_currcode.trim().length() > 0) 
					{

						lc_exchrate = fincommon.getDailyExchRateSellBuy(
								ls_currcode, ls_currcode_base, ls_sitecode,
								ldt_trandt1, "B", conn);
						valueXmlString.append("<exch_rate__clr>")
						.append("<![CDATA[" + lc_exchrate + "]]>")
						.append("</exch_rate__clr>");

						//this.gbf_exchrate_protect(ls_currcode,ls_sitecode,"exch_rate__clr");//pravin

						if (ls_currcode != null
								&& ls_currcode.trim().length() > 0) {
							sql = "select a.curr_code  "
									+ " from finent a, site b "
									+ " where b.fin_entity = a.fin_entity "
									+ " and b.site_code = ? ";
							pstmt = conn.prepareStatement(sql);
							// pstmt.setString(1, fin_entity);
							pstmt.setString(1, ls_sitecode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								ls_currcode_base = checkNull(rs.getString(1));
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (ls_currcode_base != null
									&& ls_currcode_base.trim().length() > 0
									&& ls_currcode_base.trim()
									.equalsIgnoreCase(ls_currcode
											.trim())) {
								valueXmlString
								.append("<exch_rate__clr protect =\"1\">")
								.append("<![CDATA[" + lc_exchrate
										+ "]]>")
								.append("</exch_rate__clr>");
							} else {
								valueXmlString
								.append("<exch_rate__clr protect =\"0\">")
								.append("<![CDATA["+lc_exchrate +"]]>")
								.append("</exch_rate__clr>");
							}
						}

					}

				} else if (currentColumn.trim().equalsIgnoreCase(
						"exch_rate__othch")) {
					String ldt_trandt1 = "";
					ls_currcode = checkNull(genericUtility.getColumnValue(
							"exch_rate__othch", dom));

					ls_sitecode = checkNull(genericUtility.getColumnValue(
							"site_code", dom));
					ldt_trandt1 = checkNull(genericUtility.getColumnValue(
							"tran_date", dom));

					sql = "select a.curr_code  from finent a, site b "
							+ " where b.fin_entity = a.fin_entity "
							+ " and b.site_code = ? ";

					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, ls_sitecode);//
					rs = pstmt.executeQuery();
					if (rs.next()) {

						ls_currcode_base = rs.getString(1);

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if (ls_currcode != null && ls_currcode.trim().length() > 0) {

						lc_exchrate = fincommon.getDailyExchRateSellBuy(
								ls_currcode, ls_currcode_base, ls_sitecode,
								ldt_trandt1, "B", conn);

						valueXmlString.append("<exch_rate__othch>")
						.append("<![CDATA[" + lc_exchrate + "]]>")
						.append("</exch_rate__othch>");

						//gbf_exchrate_protect(ls_currcode,ls_sitecode,"exch_rate__othch");//pravin


						if (ls_currcode != null
								&& ls_currcode.trim().length() > 0) {
							sql = "select a.curr_code  "
									+ " from finent a, site b "
									+ " where b.fin_entity = a.fin_entity "
									+ " and b.site_code = ? ";
							pstmt = conn.prepareStatement(sql);
							// pstmt.setString(1, fin_entity);
							pstmt.setString(1, ls_sitecode);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								ls_currcode_base = checkNull(rs.getString(1));
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (ls_currcode_base != null
									&& ls_currcode_base.trim().length() > 0
									&& ls_currcode_base
									.equalsIgnoreCase(ls_currcode
											.trim())) {
								valueXmlString
								.append("<exch_rate__othch protect =\"1\">")
								.append("<![CDATA[" + lc_exchrate
										+ "]]>")
								.append("</exch_rate__othch>");
							} else {
								valueXmlString
								.append("<exch_rate__othch protect =\"0\">")
								.append("<![CDATA[]]>")
								.append("</exch_rate__othch>");
							}


						}

					}

				}

				valueXmlString.append("</Detail1>");
				break;
				// case 2 start
			case 2:
				String isNullPo = "",
				singleord = "",
				lsvarvalue = "",
				exchrate = "",
				lotno = "";
				String ls_retopt = "";
				String ldt_trandt1 = "",
						mPordNo = "",
						mPordLine = "",
						mlotno = "",
						mUom = "";
				String mtaxclass="",
						mtaxchap="",
						mtaxenv="";
				String mLocation = "",
						mlotsl = "",
						macdr = "",
						mctdr = "",
						mctcr = "",
						mRateUom = "",
						mStdUom = "";
				String munitrateStr = "", mVal = "", mVal1 = "", grossWt = "", tareWt = "";
				String ldt_trandtStr = "", mrateStr = "", mNum1Str="",lc_rate__clgStr = "",supp_code="";
				double munitrate = 0, mRate = 0, mnum = 0, mnum1 = 0, mnum2 = 0;
				String mdescr = "", mCancperc = "";
				String blankVar=" ";
				String isNull=null;
				String quantity = "";
				String  mRtConvStr = "", mrate1 = "",taxChap="",taxClass="",taxEnv="",purcOrder="";
				String ls_tranID = "", ls_tranIdRef = "";
				String mQtyConvStr = "";
				String	mPordLine1="";
				double mOrdQty = 0, mDlvQty = 0;
				String temp="            ";
				SimpleDateFormat sdfNew = new SimpleDateFormat(genericUtility.getApplDateFormat());
				double mstdqty = 0,mrateuom=0;

				double liretestperiod = 0;

				double lcgrossweight = 0,
						lctareweight = 0,
						netAmt = 0;
				String reStr="";
				String tranIdRef = "", lineNoRcp = "";//Added by sarita on 16 OCT 2018
				System.out
				.println("**********************In case 2 ***********************");

				reStr = itemChanged(dom1, dom1, dom2, "1","itm_defaultedit", editFlag, xtraParams);

				reStr=reStr.substring(reStr.indexOf("<Detail1>"), reStr.indexOf("</Detail1>")+10);
				System.out.println("Detail 1String"+reStr);

				valueXmlString = new StringBuffer(
						"<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
				valueXmlString.append(editFlag).append("</editFlag></header>");
				valueXmlString.append(reStr);

				ls_tranidref = checkNull(genericUtility.getColumnValue("tran_id__ref", dom1));
				supp_code = checkNull(genericUtility.getColumnValue("supp_code", dom1));

				valueXmlString.append("<tran_id__ref protect =\"1\">").append("<![CDATA[" + ls_tranidref + "]]>").append("</tran_id__ref>");
				valueXmlString.append("<supp_code protect =\"1\">").append("<![CDATA[" + supp_code + "]]>").append("</supp_code>");



				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0;
				valueXmlString.append("<Detail2>");
				childNodeListLength = childNodeList.getLength();
				do {
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equals(currentColumn)) {
						if (childNode.getFirstChild() != null) {
							columnValue = childNode.getFirstChild()
									.getNodeValue().trim();
						}
					}
					ctr++;
				} while (ctr < childNodeListLength && !childNodeName.equals(currentColumn));

				if (currentColumn.trim().equalsIgnoreCase("itm_defaultedit")) 
				{
					System.out.println("Inside itm_defaultedit form no. 2 --");
					ls_retopt = checkNull(genericUtility.getColumnValue(
							"ret_opt", dom));
					System.out.println("ls_retopt@@["+ls_retopt+"]");
					ls_tran_id = checkNull(genericUtility.getColumnValue(
							"tran_id", dom));
					ls_tranid_rcp = checkNull(genericUtility.getColumnValue(
							"tran_id__rcp", dom));
					
					//added by monika-11 sept 2019--
					taxChap =checkNull(genericUtility.getColumnValue("tax_chap",dom));
					taxClass =checkNull(genericUtility.getColumnValue("tax_class",dom));
					taxEnv = checkNull(genericUtility.getColumnValue("tax_env",dom));
			    	purcOrder=checkNull(genericUtility.getColumnValue("purc_order",dom));
					//end
					
					if (ls_retopt.equalsIgnoreCase("C")) {
						ls_allowtax = "Y";
					} else {

						ls_allowtax = disscommon.getDisparams("999999",
								"CALC_TAX_ON_REPLACE", conn);
						System.out.println("ls_allowtax@@["+ls_allowtax+"]");
					}

					if (ls_allowtax.equalsIgnoreCase("Y")) {
						//commented by monika 10 sept 2019
						/*valueXmlString.append("<tax_chap protect =\"0\">")
						.append("<![CDATA[]]>").append("</tax_chap>");
						valueXmlString.append("<tax_class protect =\"0\">")
						.append("<![CDATA[]]>").append("</tax_class>");
						valueXmlString.append("<tax_env protect =\"0\">")
						.append("<![CDATA[]]>").append("</tax_env>");*/
						//added by monika-11 sept 2019--				
valueXmlString.append("<tax_chap protect =\"0\">").append("<![CDATA[" + taxChap + "]]>").append("</tax_chap>");
valueXmlString.append("<tax_class protect =\"0\">").append("<![CDATA[" + taxClass + "]]>").append("</tax_class>");
valueXmlString.append("<tax_env protect =\"0\">").append("<![CDATA[" + taxEnv + "]]>").append("</tax_env>");
//end
}
					else
					{
						//commented by monika 10 sept 2019
						/*valueXmlString.append("<tax_chap protect =\"1\">")
						.append("<![CDATA[]]>").append("</tax_chap>");
						valueXmlString.append("<tax_class protect =\"1\">")
						.append("<![CDATA[]]>").append("</tax_class>");
						valueXmlString.append("<tax_env protect =\"1\">")
						.append("<![CDATA[]]>").append("</tax_env>");
*/
						//added by monika-11 sept 2019--				
						valueXmlString.append("<tax_chap protect =\"0\">").append("<![CDATA[" + taxChap + "]]>").append("</tax_chap>");
						valueXmlString.append("<tax_class protect =\"0\">").append("<![CDATA[" + taxClass + "]]>").append("</tax_class>");
						valueXmlString.append("<tax_env protect =\"0\">").append("<![CDATA[" + taxEnv + "]]>").append("</tax_env>");
//end

					}

					sql = "Select single_ord_rcp  From PurcCtrl";
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if (rs.next()) {

						ls_singleord = checkNull(rs.getString(1));
						// tcode=rs.getString(3);

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if (ls_singleord.equals("Y")) {
						//commented by monika 10 sept 2019
						/*valueXmlString.append("<purc_order protect =\"1\">")
						.append("<![CDATA[]]>").append("</purc_order>");*/
			//added by monika 10 sept 2019
						valueXmlString.append("<purc_order protect =\"1\">").append("<![CDATA[" + purcOrder + "]]>").append("</purc_order>");
//end
					} else {
						//commented by monika 10 sept 2019
						/*valueXmlString.append("<purc_order protect =\"0\">")
						.append("<![CDATA[]]>").append("</purc_order>");*/
						valueXmlString.append("<purc_order protect =\"0\">").append("<![CDATA[" + purcOrder + "]]>").append("</purc_order>");
//end
					}

					//Added by sarita on 16 OCT 2018 to set rate using price_list type  [START]				
					tranIdRef = checkNull(genericUtility.getColumnValue(
							"tran_id__ref", dom1));
					lineNoRcp = checkNull(genericUtility.getColumnValue(
							"line_no__rcp", dom));
					mPordNo = checkNull(genericUtility.getColumnValue(
							"purc_order", dom1));
					mItem = checkNull(genericUtility.getColumnValue(
							"item_code", dom));
					mlotno = checkNull(genericUtility.getColumnValue(
							"lot_no", dom));
					mPordLine = checkNull(genericUtility.getColumnValue(
							"line_no__ord", dom));

					System.out.println("tranIdRef ["+tranIdRef+"] \t lineNoRcp["+lineNoRcp+"] \t mPordNo ["+mPordNo+"] \t mItem ["+mItem+"] \t mlotno ["+mlotno+"]\t mPordLine ["+mPordLine+"]");

					sql = "Select rate From porcpdet where purc_order = ? and line_no__ord = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mPordNo);
					pstmt.setString(2, mPordLine);
					rs = pstmt.executeQuery();
					if (rs.next()) {

						mRate = rs.getDouble("rate");
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

					if(tranIdRef == null || tranIdRef.trim().length() == 0)
					{
						if(lineNoRcp != null && lineNoRcp.trim().length() > 0)
						{
							sql = "select price_list  "
									+ " from porder where purc_order = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, mPordNo);//
							rs = pstmt.executeQuery();
							if (rs.next()) {

								ls_pricelist = checkNull(rs.getString(1));

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
							String listType = disscommon.getPriceListType(ls_pricelist,conn);
							System.out.println("listType is ["+listType+"]");
							String lsporateoption = "";
							if((mrate <= 0) && ("B".equalsIgnoreCase(listType)))
							{
								mrate = disscommon.pickRate(ls_pricelist,
										ls_tran_id, mItem, mlotno, "B", conn);
								System.out.println("mrate[" + mrate + "]");
								valueXmlString.append("<rate protect =\"1\">").append("<![CDATA["+mrate+"]]>").append("</rate>");
							}
							else
							{ 
								System.out.println("mrate[" + mrate + "]");
								sql="select case when po_rate_option is null then 'N' else po_rate_option end" +
										" from	item where item_code = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, mItem);
								rs = pstmt.executeQuery();
								if (rs.next()) 
								{
									lsporateoption = checkNull(rs.getString(1));
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
								if(lsporateoption == null || lsporateoption.trim().length() == 0)
								{
									lsporateoption="N";
								}
								if("N".equalsIgnoreCase(lsporateoption))
								{
									valueXmlString.append("<rate protect =\"1\">").append("<![CDATA["+mrate+"]]>").append("</rate>");
								}
								else
								{
									valueXmlString.append("<rate protect =\"0\">").append("<![CDATA["+mrate+"]]>").append("</rate>");
								}
							}
						}
					}
					//Added by sarita on 16 OCT 2018 to set rate using price_list type  [START]

				} else if (currentColumn.trim().equalsIgnoreCase("itm_default")) {

					String ls_retopt1="";
					ls_tranID = checkNull(genericUtility.getColumnValue(
							"tran_id", dom1));
					ls_tranIdRef = checkNull(genericUtility.getColumnValue(
							"tran_id__ref", dom1));
					mpord = checkNull(genericUtility.getColumnValue(
							"purc_order", dom1));
					System.out.println("mpord@po@" + mpord);
					valueXmlString.append("<tran_id>")
					.append("<![CDATA[" + ls_tranID + "]]>")
					.append("</tran_id>");
					valueXmlString.append("<tran_id__rcp>")
					.append("<![CDATA[" + ls_tranIdRef + "]]>")
					.append("</tran_id__rcp>");
					valueXmlString.append("<purc_order>")
					.append("<![CDATA[" + mpord + "]]>")
					.append("</purc_order>");// dhiraj
					ls_cancelbo = checkNull(genericUtility.getColumnValue(
							"canc_bo", dom));
					valueXmlString.append("<canc_bo>")
					.append("<![CDATA[Y]]>")
					.append("</canc_bo>");

					ls_lineno = checkNull(genericUtility.getColumnValue(
							"line_no", dom));
					System.out.println("ls_lineno@["+ls_lineno+"]");
					ls_retopt1 = checkNull(genericUtility.getColumnValue("ret_opt", dom1));
					System.out.println("ls_retopt1@["+ls_retopt1+"]");
					if (ls_lineno.trim().length() > 0) {
						valueXmlString.append("<line_no>")
						.append("<![CDATA[" + ls_lineno + "]]>")
						.append("</line_no>");

					}
					System.out.println("Edit Flag["+editFlag+"]");
					if (editFlag.equalsIgnoreCase("A")
							|| editFlag.equalsIgnoreCase("E")) {
						System.out.println("lot sl@EditFlag@["+blankVar+"]");
						valueXmlString.append("<lot_sl protect =\"0\">")
						.append("<![CDATA[]]>").append("</lot_sl>");
					}


					if (ls_retopt1.equalsIgnoreCase("C")) {
						ls_allowtax = "Y";
						System.out.println("ls_allowtax@if@[" + ls_allowtax+"]");
					} else {
						ls_allowtax = disscommon.getDisparams("999999","CALC_TAX_ON_REPLACE", conn);// f_getenv_dis('999999','CALC_TAX_ON_REPLACE');
						System.out.println("ls_allowtax@else@[" + ls_allowtax+"]");
					}
					//String isnull=null;
					if (ls_allowtax.equalsIgnoreCase("Y")) {

						valueXmlString.append("<tax_chap protect =\"0\">")
						.append("<![CDATA[]]>").append("</tax_chap>");
						valueXmlString.append("<tax_class protect =\"0\">")
						.append("<![CDATA[]]>").append("</tax_class>");
						valueXmlString.append("<tax_env protect =\"0\">")
						.append("<![CDATA[]]>").append("</tax_env>");

					} else {
						valueXmlString.append("<tax_chap protect =\"1\">")
						.append("<![CDATA[]]>").append("</tax_chap>");
						valueXmlString.append("<tax_class protect =\"1\">")
						.append("<![CDATA[]]>").append("</tax_class>");
						valueXmlString.append("<tax_env protect =\"1\">")
						.append("<![CDATA[]]>").append("</tax_env>");

					}
					sql = "Select single_ord_rcp  From PurcCtrl";
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if (rs.next()) {

						ls_singleord = checkNull(rs.getString(1));
						// tcode=rs.getString(3);

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println("ls_singleord@[" + ls_singleord+"]");
					if (ls_singleord.equalsIgnoreCase("Y")) {
						valueXmlString.append("<purc_order protect =\"1\">")
						.append("<![CDATA[" + mpord + "]]>")
						.append("</purc_order>");
					} else {
						valueXmlString.append("<purc_order protect =\"0\">")
						.append("<![CDATA[" + mpord + "]]>")
						.append("</purc_order>");
					}

					valueXmlString.append("<duty_paid protect =\"1\">")
					.append("<![CDATA[]]>").append("</duty_paid>");
					valueXmlString.append("<form_no protect =\"1\">")
					.append("<![CDATA[]]>").append("</form_no>");

				} // remaining code
				else if ((currentColumn.trim().equalsIgnoreCase("purc_order")))// Case
					// "purc_order","line_no__ord"
					// ,"line_no__rcp"
				{

					ls_tranid_ref = checkNull(genericUtility.getColumnValue(
							"tran_id__ref", dom1));
					System.out.println("ls_tranid_ref@@"+ls_tranid_ref);

					ls_tranid_rcp=checkNull(genericUtility.getColumnValue(
							"tran_id__rcp", dom));
					System.out.println("ls_tranid_rcp@@"+ls_tranid_rcp);
					ls_lineno__rcp = checkNull(genericUtility.getColumnValue(
							"line_no__rcp", dom));

					System.out.println("ls_lineno__rcp@@"+ls_lineno__rcp);

					mPordNo = checkNull(genericUtility.getColumnValue(
							"purc_order", dom));

					System.out.println("mPordNo@@"+mPordNo);
					mPordLine = checkNull(genericUtility.getColumnValue(
							"line_no__ord", dom));

					System.out.println("mPordLine@@"+mPordLine);
					ldt_trandt1 = checkNull(genericUtility.getColumnValue(
							"tran_date", dom1));// add get application format 

					System.out.println("ldt_trandt1@@"+ldt_trandt1);

					ls_site = checkNull(genericUtility.getColumnValue(
							"site_code", dom1));

					System.out.println("ls_site@@"+ls_site);

					mlotno = checkNull(genericUtility.getColumnValue("lot_no",
							dom));
					System.out.println("mlotno@"+mlotno);

					mPordLine = "    " + mPordLine;
					mPordLine = mPordLine.substring(mPordLine.length() - 3,
							mPordLine.length());
					System.out.println("mPordLine@@" + mPordLine);
					ls_lineno__rcp = "    " + ls_lineno__rcp;
					ls_lineno__rcp = ls_lineno__rcp.substring(ls_lineno__rcp.length() - 3,ls_lineno__rcp.length());
					System.out.println("ls_lineno__rcp@@" + ls_lineno__rcp);
					//SimpleDateFormat sdfNew = new SimpleDateFormat(genericUtility.getApplDateFormat());
					if ((ls_tranid_ref.trim().length() > 0)
							&& (ls_lineno__rcp.trim().length() == 0 || ls_lineno__rcp == null))

					{
						System.out.println("comming into if@1");
						sql = "select item_code,quantity,unit,rate, "
								+ " discount ,			loc_code,			lot_no,			lot_sl, "
								+ " acct_code__dr,	cctr_code__dr,		acct_code__cr, cctr_code__cr, "
								+ " unit__rate,		conv__qty_stduom,	conv__rtuom_stduom, unit__std, "
								+ " quantity__stduom,rate__stduom,		pack_code,				no_art, "
								+ " pack_instr,		batch_no,			mfg_date,				expiry_date, "
								+ " gross_weight,   	tare_weight,		net_weight,				potency_perc, "
								+ " supp_code__mnfr, site_code__mfg,	tax_amt,					grade, std_rate, "
								+ " rate__clg,dept_code, "
								+ // prince -- dept_code -- 17-04-06
								" acct_code__prov_dr, acct_code__prov_cr, cctr_code__prov_dr, cctr_code__prov_cr "
								+ " from porcpdet "
								+ " where ( tran_id = ? ) and "
								+ " ( purc_order = ? ) and "
								+ " ( line_no__ord = ? )  ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, ls_tranid_ref);
						pstmt.setString(2, mPordNo);
						pstmt.setString(3, mPordLine);
						rs = pstmt.executeQuery();
						if (rs.next()) {

							mItem = checkNull(rs.getString(1));
							mQty = rs.getDouble(2);
							mUom = checkNull(rs.getString(3));
							mrate = rs.getDouble(4);
							mdiscount = rs.getDouble(5);
							mLocation = checkNull(rs.getString(6));
							mlotno = checkNull(rs.getString(7));
							mlotsl = checkNull(rs.getString(8).trim());
							macdr = checkNull(rs.getString(9));
							mctdr = checkNull(rs.getString(10));
							maccr = checkNull(rs.getString(11));
							mctcr = checkNull(rs.getString(12));
							mRateUom = checkNull(rs.getString(13));
							mQtyConv = rs.getDouble(14);
							mRtConv = rs.getDouble(15);
							mStdUom = checkNull(rs.getString(16));
							mstdqty = rs.getDouble(17);
							lc_rate_st = rs.getDouble(18);
							ls_pack = checkNull(rs.getString(19));
							lc_no_art = rs.getDouble(20);
							ls_pack_instr = checkNull(rs.getString(21));
							ls_batch = checkNull(rs.getString(22));
							ldt_mfgdt = rs.getTimestamp(23);
							ldt_expdt = rs.getTimestamp(24);
							lc_gross_wt = rs.getDouble(25);
							lc_tare_wt = rs.getDouble(26);
							lc_net_wt = rs.getDouble(27);
							lc_pot_perc = rs.getDouble(28);
							ls_supp_mnfr = checkNull(rs.getString(29));
							ls_site_mfg = checkNull(rs.getString(30));
							mtax = rs.getDouble(31)==0? 0 :rs.getDouble(31);
							ls_grade = checkNull(rs.getString(32));
							lc_stdrate = rs.getDouble(33);
							lc_rate__clg = rs.getDouble(34);
							ls_dept_code = checkNull(rs.getString(35));
							ls_acct_pdr = checkNull(rs.getString(36));
							ls_acct_pcr = checkNull(rs.getString(37));
							ls_cctr_pdr = checkNull(rs.getString(38));
							ls_cctr_pcr = checkNull(rs.getString(39));

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						System.out.println("sql@"+sql);
						sql = "select tax_class,tax_chap,tax_env "
								+ " from porddet "
								+ " where purc_order = ? and line_no    = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mPordNo);//
						pstmt.setString(2, mPordLine);
						rs = pstmt.executeQuery();
						if (rs.next()) {

							mtaxclass = chkNull(rs.getString(1));
							mtaxchap = chkNull(rs.getString(2));
							mtaxenv = chkNull(rs.getString(3));

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println(mtaxclass+mtaxchap+mtaxenv);
						valueXmlString.append("<grade>")
						.append("<![CDATA[" + ls_grade + "]]>")
						.append("</grade>");

					} else if ((ls_tranid_ref.trim().length() > 0)
							&& (ls_lineno__rcp.trim().length() > 0)) 
					{

						System.out.println("comming into if-else");

						sql = " select item_code,            quantity,            unit,                rate, "
								+ " discount ,            loc_code,            lot_no,            lot_sl, "
								+ " acct_code__dr,    cctr_code__dr,        acct_code__cr, cctr_code__cr, "
								+ " unit__rate,        conv__qty_stduom,    conv__rtuom_stduom, unit__std, "
								+ " quantity__stduom,rate__stduom,        pack_code,                no_art, "
								+ " pack_instr,        batch_no,            mfg_date,                expiry_date, "
								+ " gross_weight,       tare_weight,        net_weight,                potency_perc, "
								+ " supp_code__mnfr, site_code__mfg,    tax_amt,                    grade, std_rate, "
								+ " rate__clg,dept_code, "
								+ " acct_code__prov_dr, acct_code__prov_cr, cctr_code__prov_dr, cctr_code__prov_cr, "
								+ " tax_class,tax_chap,tax_env  "
								+ // move below tax details in same sql which
								// coming from porcpdet instead of porddet
								// -D14ISUN003
								" from porcpdet "
								+ " where ( tran_id = ? ) and "
								+ " ( purc_order = ? ) and "
								+ " ( line_no = ? ) ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, ls_tranid_rcp);//
						pstmt.setString(2, mPordNo);
						pstmt.setString(3, ls_lineno__rcp);//
						rs = pstmt.executeQuery();
						if (rs.next()) {

							mItem = checkNull(rs.getString(1));
							mQty = rs.getDouble(2);
							mUom = checkNull(rs.getString(3));
							mrate = rs.getDouble(4);
							mdiscount = rs.getDouble(5);
							mLocation = checkNull(rs.getString(6));
							mlotno = checkNull(rs.getString(7));
							mlotsl = checkNull(rs.getString(8).trim());
							macdr = checkNull(rs.getString(9));
							mctdr = checkNull(rs.getString(10));
							maccr = checkNull(rs.getString(11));
							mctcr = checkNull(rs.getString(12));
							mRateUom = checkNull(rs.getString(13));
							mQtyConv = rs.getDouble(14);
							mRtConv = rs.getDouble(15);
							mStdUom = checkNull(rs.getString(16));
							mstdqty = rs.getDouble(17);
							lc_rate_st = rs.getDouble(18);
							ls_pack = checkNull(rs.getString(19));
							lc_no_art = rs.getDouble(20);
							ls_pack_instr = checkNull(rs.getString(21));
							ls_batch = checkNull(rs.getString(22));
							ldt_mfgdt = rs.getTimestamp(23);
							ldt_expdt = rs.getTimestamp(24);
							lc_gross_wt = rs.getDouble(25);
							lc_tare_wt = rs.getDouble(26);
							lc_net_wt = rs.getDouble(27);
							lc_pot_perc = rs.getDouble(28);
							ls_supp_mnfr = checkNull(rs.getString(29));
							ls_site_mfg = checkNull(rs.getString(30));
							mtax = rs.getDouble(31);
							ls_grade = checkNull(rs.getString(32));
							lc_stdrate = rs.getDouble(33);
							lc_rate__clg = rs.getDouble(34);
							ls_dept_code = checkNull(rs.getString(35));

							ls_acct_pdr = checkNull(rs.getString(36));
							ls_acct_pcr = checkNull(rs.getString(37));
							ls_cctr_pdr = checkNull(rs.getString(38));
							ls_cctr_pcr = checkNull(rs.getString(39));
							mtaxclass = chkNull(rs.getString(40));
							mtaxchap = chkNull(rs.getString(41));
							mtaxenv = chkNull(rs.getString(42));

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("sql@@"+sql);
						valueXmlString.append("<grade>")
						.append("<![CDATA[" + ls_grade + "]]>")
						.append("</grade>");



					} else 
					{
						System.out.println("comming into else@1");
						sql = "Select Item_code,quantity, unit,rate, "//4
								+ " discount,tax_amt, tot_amt,         loc_code, "//4
								+ " tax_class,             tax_chap,             tax_env,         conv__qty_stduom, " //4
								+ " conv__rtuom_stduom,    unit__rate ,        acct_code__dr, cctr_code__dr, "//4
								+ " acct_code__cr,        cctr_code__cr,        unit__std,        unit__rate, "//4
								+ " rate__stduom,            quantity__stduom, rate__clg,        dept_code, "//4
								+
								" acct_code__prov_dr, acct_code__prov_cr, cctr_code__prov_dr, cctr_code__prov_cr "//4
								+ " from PordDet "
								+ " Where Purc_order = ? and Line_no    = ? ";//CONV__RTUOM_STDUOM
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mPordNo);//
						pstmt.setString(2, mPordLine);
						//pstmt.setString(3, ls_lineno__rcp);//
						rs = pstmt.executeQuery();
						if (rs.next()) {

							mItem = checkNull(rs.getString(1));
							mQty = rs.getDouble(2);
							mUom = checkNull(rs.getString(3));
							mrate = rs.getDouble(4);
							mdiscount = rs.getDouble(5);
							mtax = rs.getDouble(6);
							mtotamt = rs.getDouble(7);
							mLocation = checkNull(rs.getString(8));
							mtaxclass = chkNull(rs.getString(9));
							mtaxchap =  chkNull(rs.getString(10));
							mtaxenv =  chkNull(rs.getString(11));
							mQtyConv = rs.getDouble(12);
							mRtConv = rs.getDouble(13);
							mRateUom = checkNull(rs.getString(14));
							macdr = checkNull(rs.getString(15));
							mctdr = checkNull(rs.getString(16));
							maccr = checkNull(rs.getString(17));
							mctcr = checkNull(rs.getString(18));
							mStdUom = checkNull(rs.getString(19));
							mRateUom = checkNull(rs.getString(20));
							lc_rate_st = rs.getDouble(21);
							mstdqty = rs.getDouble(22);
							lc_rate__clg = rs.getDouble(23);
							ls_dept_code = checkNull(rs.getString(24));
							ls_acct_pdr = checkNull(rs.getString(25));
							ls_acct_pcr = checkNull(rs.getString(26));
							ls_cctr_pdr = checkNull(rs.getString(27));
							ls_cctr_pcr = checkNull(rs.getString(28));

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("sql@@@"+sql);
					}
					if(mrate <= 0) {

						sql = "select price_list  "
								+ " from porder where purc_order = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mPordNo);//
						rs = pstmt.executeQuery();
						if (rs.next()) {

							ls_pricelist = checkNull(rs.getString(1));

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out
						.println("ls_pricelist@@@" + ls_pricelist);
						mrate = disscommon.pickRate(ls_pricelist,
								ldt_trandt1, mItem, mlotno, "B", conn);// pickRate(ls_pricelist,
						// ldt_trandt,
						// mItem,mlotno,"B",
						// conn);//gbf_pick_rate(ls_pricelist,ldt_trandt,mitem,mlotno,'B')
						System.out.println("mrate[" + mrate + "]");
					}

					sql = "Select descr, ordc_perc "
							+ " from item where item_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mItem);//
					rs = pstmt.executeQuery();
					if (rs.next()) {

						mdescr = checkNull(rs.getString(1));
						mCancperc = checkNull(rs.getString(2));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println(mdescr+"--"+mCancperc);
					System.out.println(mtaxclass+"--"+mtaxchap+"--"+mtaxenv);
					valueXmlString.append("<line_no__ord>")
					.append("<![CDATA[" + mPordLine + "]]>")
					.append("</line_no__ord>");
					valueXmlString.append("<item_code>")
					.append("<![CDATA[" + mItem + "]]>")
					.append("</item_code>");
					valueXmlString.append("<item_descr>")
					.append("<![CDATA[" + mdescr + "]]>")
					.append("</item_descr>");
					valueXmlString.append("<quantity>")
					.append("<![CDATA[" + mQty + "]]>")
					.append("</quantity>");
					valueXmlString.append("<quantity__stduom>")
					.append("<![CDATA[" + mstdqty + "]]>")//dhiraj
					.append("</quantity__stduom>");
					valueXmlString.append("<conv__qty_stduom>")
					.append("<![CDATA[" + mQtyConv + "]]>")
					.append("</conv__qty_stduom>");
					valueXmlString.append("<unit>")
					.append("<![CDATA[" + mUom + "]]>")
					.append("</unit>");
					valueXmlString.append("<unit__std>")
					.append("<![CDATA[" + mStdUom + "]]>")
					.append("</unit__std>");
					valueXmlString.append("<unit__rate>")
					.append("<![CDATA[" + mRateUom + "]]>")
					.append("</unit__rate>");

					valueXmlString.append("<rate>")
					.append("<![CDATA[" + mrate + "]]>")
					.append("</rate>");

					valueXmlString.append("<rate__clg>")
					.append("<![CDATA[" + lc_rate__clg + "]]>")
					.append("</rate__clg>");
					//lc_rate_st=lc_rate_st==0?0:lc_rate_st;
					valueXmlString.append("<rate__stduom>")
					.append("<![CDATA[" + lc_rate_st + "]]>")
					.append("</rate__stduom>");
					valueXmlString.append("<conv__rtuom_stduom>")
					.append("<![CDATA[" + mRtConv + "]]>")
					.append("</conv__rtuom_stduom>");
					valueXmlString.append("<discount>")
					.append("<![CDATA[" + mdiscount + "]]>")
					.append("</discount>");
					valueXmlString.append("<acct_code__dr>")
					.append("<![CDATA[" + macdr + "]]>")
					.append("</acct_code__dr>");
					valueXmlString.append("<acct_code__cr>")
					.append("<![CDATA[" + maccr + "]]>")
					.append("</acct_code__cr>");
					valueXmlString.append("<cctr_code__dr>")
					.append("<![CDATA[" + mctdr + "]]>")
					.append("</cctr_code__dr>");
					valueXmlString.append("<cctr_code__cr>")
					.append("<![CDATA[" + mctcr + "]]>")
					.append("</cctr_code__cr>");
					valueXmlString.append("<tax_amt>")
					.append("<![CDATA[" + mtax + "]]>")
					.append("</tax_amt>");
					valueXmlString.append("<conv__qty_stduom>")
					.append("<![CDATA[" + mQtyConv + "]]>")
					.append("</conv__qty_stduom>");
					valueXmlString.append("<std_rate>")
					.append("<![CDATA[" + lc_stdrate + "]]>")
					.append("</std_rate>");
					valueXmlString.append("<dept_code>")
					.append("<![CDATA[" + ls_dept_code + "]]>")
					.append("</dept_code>");
					valueXmlString.append("<acct_code__prov_dr>")
					.append("<![CDATA[" + ls_acct_pdr + "]]>")
					.append("</acct_code__prov_dr>");
					valueXmlString.append("<acct_code__prov_cr>")
					.append("<![CDATA[" + ls_acct_pcr + "]]>")
					.append("</acct_code__prov_cr>");
					valueXmlString.append("<cctr_code__prov_dr>")
					.append("<![CDATA[" + ls_cctr_pdr + "]]>")
					.append("</cctr_code__prov_dr>");
					valueXmlString.append("<cctr_code__prov_cr>")
					.append("<![CDATA[" + ls_cctr_pcr + "]]>")
					.append("</cctr_code__prov_cr>");

					lc_ret_opt = checkNull(genericUtility.getColumnValue(
							"ret_opt", dom1));

					if (lc_ret_opt.equalsIgnoreCase("C")) {
						valueXmlString.append("<tax_class>")
						.append("<![CDATA[" + chkNull(mtaxclass) + "]]>")
						.append("</tax_class>");
						valueXmlString.append("<tax_chap>")    
						.append("<![CDATA[" + chkNull(mtaxchap) + "]]>")
						.append("</tax_chap>");
						valueXmlString.append("<tax_env>")  
						.append("<![CDATA[" + chkNull(mtaxenv) + "]]>")
						.append("</tax_env>");
						System.out.println("In If@lc_ret_optIC@line_no__ord@["+mtaxchap+"]@@mtaxclass["+mtaxclass+"]mtaxenv@@"+mtaxenv+"]");
					} else {

						System.out.println("line_no_ord@else");
						valueXmlString.append("<tax_class>")
						.append("<![CDATA["+isNull+"]]>")
						.append("</tax_class>");
						valueXmlString.append("<tax_chap>")
						.append("<![CDATA[" + isNull + "]]>")
						.append("</tax_chap>");
						valueXmlString.append("<tax_env>")
						.append("<![CDATA[" + isNull + "]]>")
						.append("</tax_env>");
					}
					// //////
					valueXmlString.append("<net_amt>")
					.append("<![CDATA[" + mtotamt + "]]>")
					.append("</net_amt>");
					sql = " select loc_code__rej "
							+ " from siteitem where site_code = ? and "
							+ " item_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, ls_site);//
					pstmt.setString(2, mItem);
					// pstmt.setString(3, ls_lineno__rcp);//
					rs = pstmt.executeQuery();
					if (rs.next()) {

						ls_loccoderej = checkNull(rs.getString(1));
						// mCancperc=rs.getString(2);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println("ls_loccoderej@@"+ls_loccoderej);
					if (ls_loccoderej != null
							&& ls_loccoderej.trim().length() > 0) {
						System.out.println("inside if@"+ls_loccoderej);
						valueXmlString
						.append("<loc_code>")
						.append("<![CDATA[" + ls_loccoderej + "]]>")
						.append("</loc_code>");
					} else {
						System.out.println("inside else@"+ls_loccoderej);
						valueXmlString.append("<loc_code>")
						.append("<![CDATA[" + mLocation + "]]>")
						.append("</loc_code>");
					}
					/*ldt_mfgdt = Timestamp.valueOf(genericUtility.getValidDateString(ldt_mfgdt.toString(), genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()));
						ldt_expdt = Timestamp.valueOf(genericUtility.getValidDateString(ldt_expdt.toString(), genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()));
					 */

					if(mlotno!=null&&mlotno.trim().length()>0)
					{
						valueXmlString.append("<lot_no>")
						.append("<![CDATA[" + mlotno + "]]>")
						.append("</lot_no>");
					}else
					{
						valueXmlString.append("<lot_no>")
						.append("<![CDATA["+ temp +"]]>")
						.append("</lot_no>");
					}

					if(mlotsl!=null && mlotsl.trim().length()>0)
					{
						System.out.println("lot sl@@V["+mlotsl+"]");
						valueXmlString.append("<lot_sl>")
						.append("<![CDATA[" + mlotsl + "]]>")
						.append("</lot_sl>");
					}else
					{
						System.out.println("lot sl@@B["+temp+"]");
						valueXmlString.append("<lot_sl>")
						.append("<![CDATA["+ temp +"]]>")
						.append("</lot_sl>");
					}

					valueXmlString.append("<canc_bo>")
					.append("<![CDATA[Y]]>").append("</canc_bo>");

					String ldt_mfgdt1="",ldt_expdt1="";
					if(ldt_mfgdt!=null){
						ldt_mfgdt1=sdfNew.format(ldt_mfgdt).toString();
					}
					if(ldt_expdt!=null)
					{
						ldt_expdt1=sdfNew.format(ldt_expdt).toString();
					}


					valueXmlString.append("<mfg_date>")
					.append("<![CDATA[" + ldt_mfgdt1 + "]]>")
					.append("</mfg_date>");


					valueXmlString.append("<expiry_date>")
					.append("<![CDATA[" + ldt_expdt1 + "]]>")
					.append("</expiry_date>");



					/*
						valueXmlString.append("<mfg_date>")
								.append("<![CDATA[" + sdfNew.format(ldt_mfgdt).toString() + "]]>")
								.append("</mfg_date>");*/
					/*valueXmlString.append("<expiry_date>")
								.append("<![CDATA[" + sdfNew.format(ldt_expdt).toString() + "]]>")
								.append("</expiry_date>");*/
					valueXmlString.append("<no_art>")
					.append("<![CDATA[" + lc_no_art + "]]>")
					.append("</no_art>");
					valueXmlString.append("<tare_weight>")
					.append("<![CDATA[" + lc_tare_wt + "]]>")
					.append("</tare_weight>");
					valueXmlString.append("<gross_weight>")
					.append("<![CDATA[" + lc_gross_wt + "]]>")
					.append("</gross_weight>");
					valueXmlString.append("<net_weight>")
					.append("<![CDATA[" + lc_net_wt + "]]>")
					.append("</net_weight>");
					valueXmlString.append("<pack_code>")
					.append("<![CDATA[" + ls_pack + "]]>")
					.append("</pack_code>");
					valueXmlString.append("<pack_instr>")
					.append("<![CDATA[" + ls_pack_instr + "]]>")
					.append("</pack_instr>");
					valueXmlString.append("<batch_no>")
					.append("<![CDATA[" + ls_batch + "]]>")
					.append("</batch_no>");
					valueXmlString.append("<supp_code__mnfr>")
					.append("<![CDATA[" + ls_supp_mnfr + "]]>")
					.append("</supp_code__mnfr>");
					valueXmlString.append("<potency_perc>")
					.append("<![CDATA[" + lc_pot_perc + "]]>")
					.append("</potency_perc>");
					valueXmlString.append("<site_code__mfg>")
					.append("<![CDATA[" + ls_site_mfg + "]]>")
					.append("</site_code__mfg>");
					//String mLocation1="";
					mLocation = checkNull(genericUtility.getColumnValue(
							"loc_code", dom));
					System.out.println("mLocation@@"+mLocation);
					sql = " select conv__qty_stduom  from    stock "
							+ " where site_code = ? and "
							+ " item_code = ? and "
							+ " loc_code     = ? and "
							+ " lot_no     = ? and " + " lot_sl     = ? ";

					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, ls_site);//
					pstmt.setString(2, mItem);
					pstmt.setString(3, mLocation);
					pstmt.setString(4, mlotno);
					pstmt.setString(5, mlotsl);
					rs = pstmt.executeQuery();
					if (rs.next()) {

						lc_conv_qty_stduom = rs.getDouble(1);
						// mCancperc=rs.getString(2);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					// added the if condition as conv_qty_stduom is being
					// set to 0 in purchase receipt
					System.out.println("sql@@@@"+sql);
					System.out.println("lc_conv_qty_stduom@"+lc_conv_qty_stduom);
					if (lc_conv_qty_stduom > 0) {
						valueXmlString
						.append("<conv__qty_stduom>")
						.append("<![CDATA[" + lc_conv_qty_stduom
								+ "]]>")
						.append("</conv__qty_stduom>");
						System.out.println("lc_conv_qty_stduom@if@"+lc_conv_qty_stduom);
					}



					System.out.println("lot_no@@@@"+mlotno);
					System.out.println("lot_sl@@@@"+mlotsl);
					System.out.println("ls_tranid_ref@@"+ls_tranid_ref);
				}else if(currentColumn.trim().equalsIgnoreCase("line_no__ord"))
				{

					ls_tranid_ref = checkNull(genericUtility.getColumnValue(
							"tran_id__ref", dom1));
					System.out.println("ls_tranid_ref@@"+ls_tranid_ref);

					ls_tranid_rcp=checkNull(genericUtility.getColumnValue(
							"tran_id__rcp", dom));
					System.out.println("ls_tranid_rcp@@"+ls_tranid_rcp);
					ls_lineno__rcp = checkNull(genericUtility.getColumnValue(
							"line_no__rcp", dom));

					System.out.println("ls_lineno__rcp@@"+ls_lineno__rcp);

					mPordNo = checkNull(genericUtility.getColumnValue(
							"purc_order", dom));

					System.out.println("mPordNo@@"+mPordNo);
					mPordLine = checkNull(genericUtility.getColumnValue(
							"line_no__ord", dom));

					System.out.println("mPordLine@@"+mPordLine);
					ldt_trandt1 = checkNull(genericUtility.getColumnValue(
							"tran_date", dom1));// add get application format

					System.out.println("ldt_trandt1@@"+ldt_trandt1);

					ls_site = checkNull(genericUtility.getColumnValue(
							"site_code", dom1));

					System.out.println("ls_site@@"+ls_site);

					mlotno = checkNull(genericUtility.getColumnValue("lot_no",
							dom));
					System.out.println("mlotno@"+mlotno);

					mPordLine = "    " + mPordLine;
					mPordLine = mPordLine.substring(mPordLine.length() - 3,
							mPordLine.length());
					System.out.println("mPordLine@@" + mPordLine);
					ls_lineno__rcp = "    " + ls_lineno__rcp;
					ls_lineno__rcp = ls_lineno__rcp.substring(
							ls_lineno__rcp.length() - 3,ls_lineno__rcp.length());
					System.out.println("ls_lineno__rcp@@" + ls_lineno__rcp);
					//SimpleDateFormat sdfNew = new SimpleDateFormat(genericUtility.getApplDateFormat());
					if ((ls_tranid_ref.trim().length() > 0)
							&& (ls_lineno__rcp.trim().length() == 0 || ls_lineno__rcp == null))

					{
						System.out.println("comming into if@1");
						sql = "select item_code,quantity,unit,rate, "
								+ " discount ,			loc_code,			lot_no,			lot_sl, "
								+ " acct_code__dr,	cctr_code__dr,		acct_code__cr, cctr_code__cr, "
								+ " unit__rate,		conv__qty_stduom,	conv__rtuom_stduom, unit__std, "
								+ " quantity__stduom,rate__stduom,		pack_code,				no_art, "
								+ " pack_instr,		batch_no,			mfg_date,				expiry_date, "
								+ " gross_weight,   	tare_weight,		net_weight,				potency_perc, "
								+ " supp_code__mnfr, site_code__mfg,	tax_amt,					grade, std_rate, "
								+ " rate__clg,dept_code, "
								+ // prince -- dept_code -- 17-04-06
								" acct_code__prov_dr, acct_code__prov_cr, cctr_code__prov_dr, cctr_code__prov_cr "
								+ " from porcpdet "
								+ " where ( tran_id = ? ) and "
								+ " ( purc_order = ? ) and "
								+ " ( line_no__ord = ? )  ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, ls_tranid_ref);
						pstmt.setString(2, mPordNo);
						pstmt.setString(3, mPordLine);
						rs = pstmt.executeQuery();
						if (rs.next()) {

							mItem = checkNull(rs.getString(1));
							mQty = rs.getDouble(2);
							mUom = checkNull(rs.getString(3));
							mrate = rs.getDouble(4);
							mdiscount = rs.getDouble(5);
							mLocation = checkNull(rs.getString(6));
							mlotno = checkNull(rs.getString(7));
							mlotsl = checkNull(rs.getString(8).trim());
							macdr = checkNull(rs.getString(9));
							mctdr = checkNull(rs.getString(10));
							maccr = checkNull(rs.getString(11));
							mctcr = checkNull(rs.getString(12));
							mRateUom = checkNull(rs.getString(13));
							mQtyConv = rs.getDouble(14);
							mRtConv = rs.getDouble(15);
							mStdUom = checkNull(rs.getString(16));
							mstdqty = rs.getDouble(17);
							lc_rate_st = rs.getDouble(18);
							ls_pack = checkNull(rs.getString(19));
							lc_no_art = rs.getDouble(20);
							ls_pack_instr = checkNull(rs.getString(21));
							ls_batch = checkNull(rs.getString(22));
							ldt_mfgdt = rs.getTimestamp(23);
							ldt_expdt = rs.getTimestamp(24);
							lc_gross_wt = rs.getDouble(25);
							lc_tare_wt = rs.getDouble(26);
							lc_net_wt = rs.getDouble(27);
							lc_pot_perc = rs.getDouble(28);
							ls_supp_mnfr = checkNull(rs.getString(29));
							ls_site_mfg = checkNull(rs.getString(30));
							mtax = rs.getDouble(31)==0? 0 :rs.getDouble(31);
							ls_grade = checkNull(rs.getString(32));
							lc_stdrate = rs.getDouble(33);
							lc_rate__clg = rs.getDouble(34);
							ls_dept_code = checkNull(rs.getString(35));
							ls_acct_pdr = checkNull(rs.getString(36));
							ls_acct_pcr = checkNull(rs.getString(37));
							ls_cctr_pdr = checkNull(rs.getString(38));
							ls_cctr_pcr = checkNull(rs.getString(39));

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						System.out.println("sql@"+sql);
						sql = "select tax_class,tax_chap,tax_env "
								+ " from porddet "
								+ " where purc_order = ? and line_no    = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mPordNo);//
						pstmt.setString(2, mPordLine);
						rs = pstmt.executeQuery();
						if (rs.next()) {

							mtaxclass = chkNull(rs.getString(1));
							mtaxchap = chkNull(rs.getString(2));
							mtaxenv = chkNull(rs.getString(3));

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println(mtaxclass+mtaxchap+mtaxenv);
						valueXmlString.append("<grade>")
						.append("<![CDATA[" + ls_grade + "]]>")
						.append("</grade>");

					} else if ((ls_tranid_ref.trim().length() > 0)
							&& (ls_lineno__rcp.trim().length() > 0))
					{

						System.out.println("comming into if-else");

						sql = " select item_code,            quantity,            unit,                rate, "
								+ " discount ,            loc_code,            lot_no,            lot_sl, "
								+ " acct_code__dr,    cctr_code__dr,        acct_code__cr, cctr_code__cr, "
								+ " unit__rate,        conv__qty_stduom,    conv__rtuom_stduom, unit__std, "
								+ " quantity__stduom,rate__stduom,        pack_code,                no_art, "
								+ " pack_instr,        batch_no,            mfg_date,                expiry_date, "
								+ " gross_weight,       tare_weight,        net_weight,                potency_perc, "
								+ " supp_code__mnfr, site_code__mfg,    tax_amt,                    grade, std_rate, "
								+ " rate__clg,dept_code, "
								+ " acct_code__prov_dr, acct_code__prov_cr, cctr_code__prov_dr, cctr_code__prov_cr, "
								+ " tax_class,tax_chap,tax_env  "
								+ // move below tax details in same sql which
								// coming from porcpdet instead of porddet
								// -D14ISUN003
								" from porcpdet "
								+ " where ( tran_id = ? ) and "
								+ " ( purc_order = ? ) and "
								+ " ( line_no = ? ) ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, ls_tranid_rcp);//
						pstmt.setString(2, mPordNo);
						pstmt.setString(3, ls_lineno__rcp);//
						rs = pstmt.executeQuery();
						if (rs.next()) {

							mItem = checkNull(rs.getString(1));
							mQty = rs.getDouble(2);
							mUom = checkNull(rs.getString(3));
							mrate = rs.getDouble(4);
							mdiscount = rs.getDouble(5);
							mLocation = checkNull(rs.getString(6));
							mlotno = checkNull(rs.getString(7));
							mlotsl = checkNull(rs.getString(8).trim());
							macdr = checkNull(rs.getString(9));
							mctdr = checkNull(rs.getString(10));
							maccr = checkNull(rs.getString(11));
							mctcr = checkNull(rs.getString(12));
							mRateUom = checkNull(rs.getString(13));
							mQtyConv = rs.getDouble(14);
							mRtConv = rs.getDouble(15);
							mStdUom = checkNull(rs.getString(16));
							mstdqty = rs.getDouble(17);
							lc_rate_st = rs.getDouble(18);
							ls_pack = checkNull(rs.getString(19));
							lc_no_art = rs.getDouble(20);
							ls_pack_instr = checkNull(rs.getString(21));
							ls_batch = checkNull(rs.getString(22));
							ldt_mfgdt = rs.getTimestamp(23);
							ldt_expdt = rs.getTimestamp(24);
							lc_gross_wt = rs.getDouble(25);
							lc_tare_wt = rs.getDouble(26);
							lc_net_wt = rs.getDouble(27);
							lc_pot_perc = rs.getDouble(28);
							ls_supp_mnfr = checkNull(rs.getString(29));
							ls_site_mfg = checkNull(rs.getString(30));
							mtax = rs.getDouble(31);
							ls_grade = checkNull(rs.getString(32));
							lc_stdrate = rs.getDouble(33);
							lc_rate__clg = rs.getDouble(34);
							ls_dept_code = checkNull(rs.getString(35));

							ls_acct_pdr = checkNull(rs.getString(36));
							ls_acct_pcr = checkNull(rs.getString(37));
							ls_cctr_pdr = checkNull(rs.getString(38));
							ls_cctr_pcr = checkNull(rs.getString(39));
							mtaxclass = chkNull(rs.getString(40));
							mtaxchap = chkNull(rs.getString(41));
							mtaxenv = chkNull(rs.getString(42));

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("sql@@"+sql);
						valueXmlString.append("<grade>")
						.append("<![CDATA[" + ls_grade + "]]>")
						.append("</grade>");



					} else 
					{
						System.out.println("comming into else@1");
						sql = "Select Item_code,quantity, unit,rate, "//4
								+ " discount,tax_amt, tot_amt,         loc_code, "//4
								+ " tax_class,             tax_chap,             tax_env,         conv__qty_stduom, " //4
								+ " conv__rtuom_stduom,    unit__rate ,        acct_code__dr, cctr_code__dr, "//4
								+ " acct_code__cr,        cctr_code__cr,        unit__std,        unit__rate, "//4
								+ " rate__stduom,            quantity__stduom, rate__clg,        dept_code, "//4
								+
								" acct_code__prov_dr, acct_code__prov_cr, cctr_code__prov_dr, cctr_code__prov_cr "//4
								+ " from PordDet "
								+ " Where Purc_order = ? and Line_no    = ? ";//CONV__RTUOM_STDUOM
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mPordNo);//
						pstmt.setString(2, mPordLine);
						//pstmt.setString(3, ls_lineno__rcp);//
						rs = pstmt.executeQuery();
						if (rs.next()) {

							mItem = checkNull(rs.getString(1));
							mQty = rs.getDouble(2);
							mUom = checkNull(rs.getString(3));
							mrate = rs.getDouble(4);
							mdiscount = rs.getDouble(5);
							mtax = rs.getDouble(6);
							mtotamt = rs.getDouble(7);
							mLocation = checkNull(rs.getString(8));
							mtaxclass = chkNull(rs.getString(9));
							mtaxchap =  chkNull(rs.getString(10));
							mtaxenv =  chkNull(rs.getString(11));
							mQtyConv = rs.getDouble(12);
							mRtConv = rs.getDouble(13);
							mRateUom = checkNull(rs.getString(14));
							macdr = checkNull(rs.getString(15));
							mctdr = checkNull(rs.getString(16));
							maccr = checkNull(rs.getString(17));
							mctcr = checkNull(rs.getString(18));
							mStdUom = checkNull(rs.getString(19));
							mRateUom = checkNull(rs.getString(20));
							lc_rate_st = rs.getDouble(21);
							mstdqty = rs.getDouble(22);
							lc_rate__clg = rs.getDouble(23);
							ls_dept_code = checkNull(rs.getString(24));
							ls_acct_pdr = checkNull(rs.getString(25));
							ls_acct_pcr = checkNull(rs.getString(26));
							ls_cctr_pdr = checkNull(rs.getString(27));
							ls_cctr_pcr = checkNull(rs.getString(28));

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("sql@@@"+sql);
					}
					if(mrate <= 0)
					{

						sql = "select price_list  "
								+ " from porder where purc_order = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mPordNo);//
						rs = pstmt.executeQuery();
						if (rs.next()) {

							ls_pricelist = checkNull(rs.getString(1));

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out
						.println("ls_pricelist@@@" + ls_pricelist);
						mrate = disscommon.pickRate(ls_pricelist,
								ldt_trandt1, mItem, mlotno, "B", conn);// pickRate(ls_pricelist,
						// ldt_trandt,
						// mItem,mlotno,"B",
						// conn);//gbf_pick_rate(ls_pricelist,ldt_trandt,mitem,mlotno,'B')
						System.out.println("mrate[" + mrate + "]");
					}

					sql = "Select descr, ordc_perc "
							+ " from item where item_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mItem);//
					rs = pstmt.executeQuery();
					if (rs.next()) {

						mdescr = checkNull(rs.getString(1));
						mCancperc = checkNull(rs.getString(2));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println(mdescr+"--"+mCancperc);
					System.out.println(mtaxclass+"--"+mtaxchap+"--"+mtaxenv);
					valueXmlString.append("<line_no__ord>")
					.append("<![CDATA[" + mPordLine + "]]>")
					.append("</line_no__ord>");
					valueXmlString.append("<item_code>")
					.append("<![CDATA[" + mItem + "]]>")
					.append("</item_code>");
					valueXmlString.append("<item_descr>")
					.append("<![CDATA[" + mdescr + "]]>")
					.append("</item_descr>");
					valueXmlString.append("<quantity>")
					.append("<![CDATA[" + mQty + "]]>")
					.append("</quantity>");
					valueXmlString.append("<quantity__stduom>")
					.append("<![CDATA[" + mstdqty + "]]>")//dhiraj
					.append("</quantity__stduom>");
					valueXmlString.append("<conv__qty_stduom>")
					.append("<![CDATA[" + mQtyConv + "]]>")
					.append("</conv__qty_stduom>");
					valueXmlString.append("<unit>")
					.append("<![CDATA[" + mUom + "]]>")
					.append("</unit>");
					valueXmlString.append("<unit__std>")
					.append("<![CDATA[" + mStdUom + "]]>")
					.append("</unit__std>");
					valueXmlString.append("<unit__rate>")
					.append("<![CDATA[" + mRateUom + "]]>")
					.append("</unit__rate>");

					valueXmlString.append("<rate>")
					.append("<![CDATA[" + mrate + "]]>")
					.append("</rate>");

					valueXmlString.append("<rate__clg>")
					.append("<![CDATA[" + lc_rate__clg + "]]>")
					.append("</rate__clg>");
					//lc_rate_st=lc_rate_st==0?0:lc_rate_st;
					valueXmlString.append("<rate__stduom>")
					.append("<![CDATA[" + lc_rate_st + "]]>")
					.append("</rate__stduom>");
					valueXmlString.append("<conv__rtuom_stduom>")
					.append("<![CDATA[" + mRtConv + "]]>")
					.append("</conv__rtuom_stduom>");
					valueXmlString.append("<discount>")
					.append("<![CDATA[" + mdiscount + "]]>")
					.append("</discount>");
					valueXmlString.append("<acct_code__dr>")
					.append("<![CDATA[" + macdr + "]]>")
					.append("</acct_code__dr>");
					valueXmlString.append("<acct_code__cr>")
					.append("<![CDATA[" + maccr + "]]>")
					.append("</acct_code__cr>");
					valueXmlString.append("<cctr_code__dr>")
					.append("<![CDATA[" + mctdr + "]]>")
					.append("</cctr_code__dr>");
					valueXmlString.append("<cctr_code__cr>")
					.append("<![CDATA[" + mctcr + "]]>")
					.append("</cctr_code__cr>");
					valueXmlString.append("<tax_amt>")
					.append("<![CDATA[" + mtax + "]]>")
					.append("</tax_amt>");
					valueXmlString.append("<conv__qty_stduom>")
					.append("<![CDATA[" + mQtyConv + "]]>")
					.append("</conv__qty_stduom>");
					valueXmlString.append("<std_rate>")
					.append("<![CDATA[" + lc_stdrate + "]]>")
					.append("</std_rate>");
					valueXmlString.append("<dept_code>")
					.append("<![CDATA[" + ls_dept_code + "]]>")
					.append("</dept_code>");
					valueXmlString.append("<acct_code__prov_dr>")
					.append("<![CDATA[" + ls_acct_pdr + "]]>")
					.append("</acct_code__prov_dr>");
					valueXmlString.append("<acct_code__prov_cr>")
					.append("<![CDATA[" + ls_acct_pcr + "]]>")
					.append("</acct_code__prov_cr>");
					valueXmlString.append("<cctr_code__prov_dr>")
					.append("<![CDATA[" + ls_cctr_pdr + "]]>")
					.append("</cctr_code__prov_dr>");
					valueXmlString.append("<cctr_code__prov_cr>")
					.append("<![CDATA[" + ls_cctr_pcr + "]]>")
					.append("</cctr_code__prov_cr>");

					lc_ret_opt = checkNull(genericUtility.getColumnValue(
							"ret_opt", dom1));

					if (lc_ret_opt.equalsIgnoreCase("C")) {
						valueXmlString.append("<tax_class>")
						.append("<![CDATA[" + chkNull(mtaxclass) + "]]>")
						.append("</tax_class>");
						valueXmlString.append("<tax_chap>")    
						.append("<![CDATA[" + chkNull(mtaxchap) + "]]>")
						.append("</tax_chap>");
						valueXmlString.append("<tax_env>")  
						.append("<![CDATA[" + chkNull(mtaxenv) + "]]>")
						.append("</tax_env>");
						System.out.println("In If@lc_ret_optIC@line_no__ord@["+mtaxchap+"]@@mtaxclass["+mtaxclass+"]mtaxenv@@"+mtaxenv+"]");
					} else {

						System.out.println("line_no_ord@else");
						valueXmlString.append("<tax_class>")
						.append("<![CDATA["+isNull+"]]>")
						.append("</tax_class>");
						valueXmlString.append("<tax_chap>")
						.append("<![CDATA[" + isNull + "]]>")
						.append("</tax_chap>");
						valueXmlString.append("<tax_env>")
						.append("<![CDATA[" + isNull + "]]>")
						.append("</tax_env>");
					}
					// //////
					valueXmlString.append("<net_amt>")
					.append("<![CDATA[" + mtotamt + "]]>")
					.append("</net_amt>");
					sql = " select loc_code__rej "
							+ " from siteitem where site_code = ? and "
							+ " item_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, ls_site);//
					pstmt.setString(2, mItem);
					// pstmt.setString(3, ls_lineno__rcp);//
					rs = pstmt.executeQuery();
					if (rs.next()) {

						ls_loccoderej = checkNull(rs.getString(1));
						// mCancperc=rs.getString(2);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println("ls_loccoderej@@"+ls_loccoderej);
					if (ls_loccoderej != null
							&& ls_loccoderej.trim().length() > 0) {
						System.out.println("inside if@"+ls_loccoderej);
						valueXmlString
						.append("<loc_code>")
						.append("<![CDATA[" + ls_loccoderej + "]]>")
						.append("</loc_code>");
					} else {
						System.out.println("inside else@"+ls_loccoderej);
						valueXmlString.append("<loc_code>")
						.append("<![CDATA[" + mLocation + "]]>")
						.append("</loc_code>");
					}
					/*ldt_mfgdt = Timestamp.valueOf(genericUtility.getValidDateString(ldt_mfgdt.toString(), genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()));
						ldt_expdt = Timestamp.valueOf(genericUtility.getValidDateString(ldt_expdt.toString(), genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()));
					 */

					if(mlotno!=null&&mlotno.trim().length()>0)
					{
						valueXmlString.append("<lot_no>")
						.append("<![CDATA[" + mlotno + "]]>")
						.append("</lot_no>");
					}else
					{
						valueXmlString.append("<lot_no>")
						.append("<![CDATA["+ temp +"]]>")
						.append("</lot_no>");
					}

					if(mlotsl!=null && mlotsl.trim().length()>0)
					{
						System.out.println("lot sl@@V["+mlotsl+"]");
						valueXmlString.append("<lot_sl>")
						.append("<![CDATA[" + mlotsl + "]]>")
						.append("</lot_sl>");
					}else
					{
						System.out.println("lot sl@@B["+temp+"]");
						valueXmlString.append("<lot_sl>")
						.append("<![CDATA["+ temp +"]]>")
						.append("</lot_sl>");
					}

					valueXmlString.append("<canc_bo>")
					.append("<![CDATA[Y]]>").append("</canc_bo>");

					String ldt_mfgdt1="",ldt_expdt1="";
					if(ldt_mfgdt!=null){
						ldt_mfgdt1=sdfNew.format(ldt_mfgdt).toString();
					}
					if(ldt_expdt!=null)
					{
						ldt_expdt1=sdfNew.format(ldt_expdt).toString();
					}


					valueXmlString.append("<mfg_date>")
					.append("<![CDATA[" + ldt_mfgdt1 + "]]>")
					.append("</mfg_date>");


					valueXmlString.append("<expiry_date>")
					.append("<![CDATA[" + ldt_expdt1 + "]]>")
					.append("</expiry_date>");



					/*
						valueXmlString.append("<mfg_date>")
								.append("<![CDATA[" + sdfNew.format(ldt_mfgdt).toString() + "]]>")
								.append("</mfg_date>");*/
					/*valueXmlString.append("<expiry_date>")
								.append("<![CDATA[" + sdfNew.format(ldt_expdt).toString() + "]]>")
								.append("</expiry_date>");*/
					valueXmlString.append("<no_art>")
					.append("<![CDATA[" + lc_no_art + "]]>")
					.append("</no_art>");
					valueXmlString.append("<tare_weight>")
					.append("<![CDATA[" + lc_tare_wt + "]]>")
					.append("</tare_weight>");
					valueXmlString.append("<gross_weight>")
					.append("<![CDATA[" + lc_gross_wt + "]]>")
					.append("</gross_weight>");
					valueXmlString.append("<net_weight>")
					.append("<![CDATA[" + lc_net_wt + "]]>")
					.append("</net_weight>");
					valueXmlString.append("<pack_code>")
					.append("<![CDATA[" + ls_pack + "]]>")
					.append("</pack_code>");
					valueXmlString.append("<pack_instr>")
					.append("<![CDATA[" + ls_pack_instr + "]]>")
					.append("</pack_instr>");
					valueXmlString.append("<batch_no>")
					.append("<![CDATA[" + ls_batch + "]]>")
					.append("</batch_no>");
					valueXmlString.append("<supp_code__mnfr>")
					.append("<![CDATA[" + ls_supp_mnfr + "]]>")
					.append("</supp_code__mnfr>");
					valueXmlString.append("<potency_perc>")
					.append("<![CDATA[" + lc_pot_perc + "]]>")
					.append("</potency_perc>");
					valueXmlString.append("<site_code__mfg>")
					.append("<![CDATA[" + ls_site_mfg + "]]>")
					.append("</site_code__mfg>");
					//String mLocation1="";
					mLocation = checkNull(genericUtility.getColumnValue(
							"loc_code", dom));
					System.out.println("mLocation@@"+mLocation);
					sql = " select conv__qty_stduom  from    stock "
							+ " where site_code = ? and "
							+ " item_code = ? and "
							+ " loc_code     = ? and "
							+ " lot_no     = ? and " + " lot_sl     = ? ";

					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, ls_site);//
					pstmt.setString(2, mItem);
					pstmt.setString(3, mLocation);
					pstmt.setString(4, mlotno);
					pstmt.setString(5, mlotsl);
					rs = pstmt.executeQuery();
					if (rs.next()) {

						lc_conv_qty_stduom = rs.getDouble(1);
						// mCancperc=rs.getString(2);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					// added the if condition as conv_qty_stduom is being
					// set to 0 in purchase receipt
					System.out.println("sql@@@@"+sql);
					System.out.println("lc_conv_qty_stduom@"+lc_conv_qty_stduom);
					if (lc_conv_qty_stduom > 0) {
						valueXmlString
						.append("<conv__qty_stduom>")
						.append("<![CDATA[" + lc_conv_qty_stduom
								+ "]]>")
						.append("</conv__qty_stduom>");
						System.out.println("lc_conv_qty_stduom@if@"+lc_conv_qty_stduom);
					}



					System.out.println("lot_no@@@@"+mlotno);
					System.out.println("lot_sl@@@@"+mlotsl);
					System.out.println("ls_tranid_ref@@"+ls_tranid_ref);

				}else if(currentColumn.trim().equalsIgnoreCase("line_no__rcp"))
				{
					System.out.println("inside-line_no__rcp");
					ls_tranid_ref = checkNull(genericUtility.getColumnValue(
							"tran_id__ref", dom1));
					System.out.println("ls_tranid_ref@@"+ls_tranid_ref);

					ls_tranid_rcp=checkNull(genericUtility.getColumnValue(
							"tran_id__rcp", dom));
					System.out.println("ls_tranid_rcp@@"+ls_tranid_rcp);
					ls_lineno__rcp = checkNull(genericUtility.getColumnValue(
							"line_no__rcp", dom));

					System.out.println("ls_lineno__rcp@@"+ls_lineno__rcp);

					mPordNo = checkNull(genericUtility.getColumnValue(
							"purc_order", dom));

					System.out.println("mPordNo@@"+mPordNo);
					mPordLine = checkNull(genericUtility.getColumnValue(
							"line_no__ord", dom));

					System.out.println("mPordLine@@"+mPordLine);
					ldt_trandt1 = checkNull(genericUtility.getColumnValue(
							"tran_date", dom1));// add get application format

					System.out.println("ldt_trandt1@@"+ldt_trandt1);

					ls_site = checkNull(genericUtility.getColumnValue(
							"site_code", dom1));

					System.out.println("ls_site@@"+ls_site);

					mlotno = checkNull(genericUtility.getColumnValue("lot_no",
							dom));
					System.out.println("mlotno@"+mlotno);

					mPordLine = "    " + mPordLine;
					mPordLine = mPordLine.substring(mPordLine.length() - 3,
							mPordLine.length());
					System.out.println("mPordLine@@" + mPordLine);
					ls_lineno__rcp = "    " + ls_lineno__rcp;
					ls_lineno__rcp = ls_lineno__rcp.substring(
							ls_lineno__rcp.length() - 3,ls_lineno__rcp.length());
					System.out.println("ls_lineno__rcp@@" + ls_lineno__rcp);
					//SimpleDateFormat sdfNew = new SimpleDateFormat(genericUtility.getApplDateFormat());
					if ((ls_tranid_ref.trim().length() > 0)
							&& (ls_lineno__rcp.trim().length() == 0 || ls_lineno__rcp == null))

					{
						System.out.println("comming into if@1");
						sql = "select item_code,quantity,unit,rate, "
								+ " discount ,			loc_code,			lot_no,			lot_sl, "
								+ " acct_code__dr,	cctr_code__dr,		acct_code__cr, cctr_code__cr, "
								+ " unit__rate,		conv__qty_stduom,	conv__rtuom_stduom, unit__std, "
								+ " quantity__stduom,rate__stduom,		pack_code,				no_art, "
								+ " pack_instr,		batch_no,			mfg_date,				expiry_date, "
								+ " gross_weight,   	tare_weight,		net_weight,				potency_perc, "
								+ " supp_code__mnfr, site_code__mfg,	tax_amt,					grade, std_rate, "
								+ " rate__clg,dept_code, "
								+ // prince -- dept_code -- 17-04-06
								" acct_code__prov_dr, acct_code__prov_cr, cctr_code__prov_dr, cctr_code__prov_cr "

								+ " from porcpdet "
								+ " where ( tran_id = ? ) and "
								+ " ( purc_order = ? ) and "
								+ " ( line_no__ord = ? )  ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, ls_tranid_ref);
						pstmt.setString(2, mPordNo);
						pstmt.setString(3, mPordLine);
						rs = pstmt.executeQuery();
						if (rs.next()) {

							mItem = checkNull(rs.getString(1));
							mQty = rs.getDouble(2);
							mUom = checkNull(rs.getString(3));
							mrate = rs.getDouble(4);
							mdiscount = rs.getDouble(5);
							mLocation = checkNull(rs.getString(6));
							mlotno = checkNull(rs.getString(7));
							mlotsl = checkNull(rs.getString(8).trim());
							macdr = checkNull(rs.getString(9));
							mctdr = checkNull(rs.getString(10));
							maccr = checkNull(rs.getString(11));
							mctcr = checkNull(rs.getString(12));
							mRateUom = checkNull(rs.getString(13));
							mQtyConv = rs.getDouble(14);
							mRtConv = rs.getDouble(15);
							mStdUom = checkNull(rs.getString(16));
							mstdqty = rs.getDouble(17);
							lc_rate_st = rs.getDouble(18);
							ls_pack = checkNull(rs.getString(19));
							lc_no_art = rs.getDouble(20);
							ls_pack_instr = checkNull(rs.getString(21));
							ls_batch = checkNull(rs.getString(22));
							ldt_mfgdt = rs.getTimestamp(23);
							ldt_expdt = rs.getTimestamp(24);
							lc_gross_wt = rs.getDouble(25);
							lc_tare_wt = rs.getDouble(26);
							lc_net_wt = rs.getDouble(27);
							lc_pot_perc = rs.getDouble(28);
							ls_supp_mnfr = checkNull(rs.getString(29));
							ls_site_mfg = checkNull(rs.getString(30));
							mtax = rs.getDouble(31)==0? 0 :rs.getDouble(31);
							ls_grade = checkNull(rs.getString(32));
							lc_stdrate = rs.getDouble(33);
							lc_rate__clg = rs.getDouble(34);
							ls_dept_code = checkNull(rs.getString(35));
							ls_acct_pdr = checkNull(rs.getString(36));
							ls_acct_pcr = checkNull(rs.getString(37));
							ls_cctr_pdr = checkNull(rs.getString(38));
							ls_cctr_pcr = checkNull(rs.getString(39));


						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						System.out.println("sql@"+sql);
						sql = "select tax_class,tax_chap,tax_env "
								+ " from porddet "
								+ " where purc_order = ? and line_no    = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mPordNo);//
						pstmt.setString(2, mPordLine);
						rs = pstmt.executeQuery();
						if (rs.next()) {

							mtaxclass = chkNull(rs.getString(1));
							mtaxchap = chkNull(rs.getString(2));
							mtaxenv = chkNull(rs.getString(3));

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println(mtaxclass+mtaxchap+mtaxenv);
						valueXmlString.append("<grade>")
						.append("<![CDATA[" + ls_grade + "]]>")
						.append("</grade>");

					} else if ((ls_tranid_ref.trim().length() > 0)
							&& (ls_lineno__rcp.trim().length() > 0)) {

						System.out.println("comming into if-else2");

						sql = " select item_code,            quantity,            unit,                rate, "
								+ " discount ,            loc_code,            lot_no,            lot_sl, "
								+ " acct_code__dr,    cctr_code__dr,        acct_code__cr, cctr_code__cr, "
								+ " unit__rate,        conv__qty_stduom,    conv__rtuom_stduom, unit__std, "
								+ " quantity__stduom,rate__stduom,        pack_code,                no_art, "
								+ " pack_instr,        batch_no,            mfg_date,                expiry_date, "
								+ " gross_weight,       tare_weight,        net_weight,                potency_perc, "
								+ " supp_code__mnfr, site_code__mfg,    tax_amt,                    grade, std_rate, "
								+ " rate__clg,dept_code, "
								+ " acct_code__prov_dr, acct_code__prov_cr, cctr_code__prov_dr, cctr_code__prov_cr, "
								+ " tax_class,tax_chap,tax_env,  "
								+"LINE_NO__ORD  "//Add by Ajay on 08/05/18

								+ // move below tax details in same sql which
								// coming from porcpdet instead of porddet
								// -D14ISUN003
								" from porcpdet "
								+ " where ( tran_id = ? ) and "
								+ " ( purc_order = ? ) and "
								+ " ( line_no = ? ) ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, ls_tranid_rcp);//
						pstmt.setString(2, mPordNo);
						pstmt.setString(3, ls_lineno__rcp);//
						rs = pstmt.executeQuery();
						if (rs.next()) {

							mItem = checkNull(rs.getString(1));
							mQty = rs.getDouble(2);
							mUom = checkNull(rs.getString(3));
							mrate = rs.getDouble(4);
							mdiscount = rs.getDouble(5);
							mLocation = checkNull(rs.getString(6));
							mlotno = checkNull(rs.getString(7));
							mlotsl = checkNull(rs.getString(8).trim());
							macdr = checkNull(rs.getString(9));
							mctdr = checkNull(rs.getString(10));
							maccr = checkNull(rs.getString(11));
							mctcr = checkNull(rs.getString(12));
							mRateUom = checkNull(rs.getString(13));
							mQtyConv = rs.getDouble(14);
							mRtConv = rs.getDouble(15);
							mStdUom = checkNull(rs.getString(16));
							mstdqty = rs.getDouble(17);
							lc_rate_st = rs.getDouble(18);
							ls_pack = checkNull(rs.getString(19));
							lc_no_art = rs.getDouble(20);
							ls_pack_instr = checkNull(rs.getString(21));
							ls_batch = checkNull(rs.getString(22));
							ldt_mfgdt = rs.getTimestamp(23);
							ldt_expdt = rs.getTimestamp(24);
							lc_gross_wt = rs.getDouble(25);
							lc_tare_wt = rs.getDouble(26);
							lc_net_wt = rs.getDouble(27);
							lc_pot_perc = rs.getDouble(28);
							ls_supp_mnfr = checkNull(rs.getString(29));
							ls_site_mfg = checkNull(rs.getString(30));
							mtax = rs.getDouble(31);
							ls_grade = checkNull(rs.getString(32));
							lc_stdrate = rs.getDouble(33);
							lc_rate__clg = rs.getDouble(34);
							ls_dept_code = checkNull(rs.getString(35));

							ls_acct_pdr = checkNull(rs.getString(36));
							ls_acct_pcr = checkNull(rs.getString(37));
							ls_cctr_pdr = checkNull(rs.getString(38));
							ls_cctr_pcr = checkNull(rs.getString(39));
							mtaxclass = chkNull(rs.getString(40));
							mtaxchap = chkNull(rs.getString(41));
							mtaxenv = chkNull(rs.getString(42));
							mPordLine = chkNull(rs.getString(43));//Add by Ajay on 08/05/18

							System.out.println("mrate ["+mrate+"] \t mQty ["+mQty+"]");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("sql@@"+sql);
						valueXmlString.append("<grade>")
						.append("<![CDATA[" + ls_grade + "]]>")
						.append("</grade>");
						//Added by sarita to set quantity and rate on 16 OCT 2018 [START]
						/*valueXmlString.append("<quantity>")
						.append("<![CDATA[" + mQty + "]]>")
						.append("</quantity>"); -- Commented on 22 OCT 2018*/
						valueXmlString.append("<rate protect =\"1\">")
						.append("<![CDATA[" + mrate + "]]>")
						.append("</rate>");
						//Added by sarita to set quantity and rate on 16 OCT 2018 [END]
					} else 
					{
						System.out.println("comming into else@3");
						sql = "Select Item_code,quantity, unit,rate, "//4
								+ " discount,tax_amt, tot_amt,         loc_code, "//4
								+ " tax_class,             tax_chap,             tax_env,         conv__qty_stduom, " //4
								+ " conv__rtuom_stduom,    unit__rate ,        acct_code__dr, cctr_code__dr, "//4
								+ " acct_code__cr,        cctr_code__cr,        unit__std,        unit__rate, "//4
								+ " rate__stduom,            quantity__stduom, rate__clg,        dept_code, "//4
								+ 
								" acct_code__prov_dr, acct_code__prov_cr, cctr_code__prov_dr, cctr_code__prov_cr "//4
								+ " from PordDet "
								+ " Where Purc_order = ? and Line_no    = ? ";//CONV__RTUOM_STDUOM
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mPordNo);//
						pstmt.setString(2, mPordLine);
						//pstmt.setString(3, ls_lineno__rcp);//
						rs = pstmt.executeQuery();
						if (rs.next()) {

							mItem = checkNull(rs.getString(1));
							mQty = rs.getDouble(2);
							mUom = checkNull(rs.getString(3));
							mrate = rs.getDouble(4);
							mdiscount = rs.getDouble(5);
							mtax = rs.getDouble(6);
							mtotamt = rs.getDouble(7);
							mLocation = checkNull(rs.getString(8));
							mtaxclass = chkNull(rs.getString(9));
							mtaxchap =  chkNull(rs.getString(10));
							mtaxenv =  chkNull(rs.getString(11));
							mQtyConv = rs.getDouble(12);
							mRtConv = rs.getDouble(13);
							mRateUom = checkNull(rs.getString(14));
							macdr = checkNull(rs.getString(15));
							mctdr = checkNull(rs.getString(16));
							maccr = checkNull(rs.getString(17));
							mctcr = checkNull(rs.getString(18));
							mStdUom = checkNull(rs.getString(19));
							mRateUom = checkNull(rs.getString(20));
							lc_rate_st = rs.getDouble(21);
							mstdqty = rs.getDouble(22);
							lc_rate__clg = rs.getDouble(23);
							ls_dept_code = checkNull(rs.getString(24));
							ls_acct_pdr = checkNull(rs.getString(25));
							ls_acct_pcr = checkNull(rs.getString(26));
							ls_cctr_pdr = checkNull(rs.getString(27));
							ls_cctr_pcr = checkNull(rs.getString(28));



						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("sql@@@"+sql);
					}

					//Commented and Added by sarita on 16OCT2018 [START]
					/*if(mrate <= 0) {

								sql = "select price_list  "
										+ " from porder where purc_order = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, mPordNo);//
								rs = pstmt.executeQuery();
								if (rs.next()) {

									ls_pricelist = checkNull(rs.getString(1));

								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								System.out
										.println("ls_pricelist@@@" + ls_pricelist);
								mrate = disscommon.pickRate(ls_pricelist,
										ldt_trandt1, mItem, mlotno, "B", conn);// pickRate(ls_pricelist,
																				// ldt_trandt,
																				// mItem,mlotno,"B",
																				// conn);//gbf_pick_rate(ls_pricelist,ldt_trandt,mitem,mlotno,'B')
								System.out.println("mrate[" + mrate + "]");
							}*/

					sql = "select price_list  "
							+ " from porder where purc_order = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mPordNo);//
					rs = pstmt.executeQuery();
					if (rs.next()) {

						ls_pricelist = checkNull(rs.getString(1));

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
					String listType = disscommon.getPriceListType(ls_pricelist,conn);
					System.out.println("listType is ["+listType+"]");
					String lsporateoption = "";
					if((mrate <= 0) && ("B".equalsIgnoreCase(listType)))
					{
						mrate = disscommon.pickRate(ls_pricelist,
								ldt_trandt1, mItem, mlotno, "B", conn);
						System.out.println("mrate[" + mrate + "]");
						valueXmlString.append("<rate protect =\"1\">").append("<![CDATA["+mrate+"]]>").append("</rate>");
					}
					else
					{ 
						System.out.println("mrate[" + mrate + "]");
						sql="select case when po_rate_option is null then 'N' else po_rate_option end" +
								" from	item where item_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mItem);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							lsporateoption = checkNull(rs.getString(1));
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
						if(lsporateoption == null || lsporateoption.trim().length() == 0)
						{
							lsporateoption="N";
						}
						if("N".equalsIgnoreCase(lsporateoption))
						{
							valueXmlString.append("<rate protect =\"1\">").append("<![CDATA["+mrate+"]]>").append("</rate>");
						}
						else
						{
							valueXmlString.append("<rate protect =\"0\">").append("<![CDATA["+mrate+"]]>").append("</rate>");
						}
					}
					//Commented and Added by sarita on 16OCT2018 [END]					

					sql = "Select descr, ordc_perc "
							+ " from item where item_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mItem);//
					rs = pstmt.executeQuery();
					if (rs.next()) {

						mdescr = checkNull(rs.getString(1));
						mCancperc = checkNull(rs.getString(2));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println(mdescr+"--"+mCancperc);
					System.out.println(mtaxclass+"--"+mtaxchap+"--"+mtaxenv);
					valueXmlString.append("<line_no__ord>")
					.append("<![CDATA[" + mPordLine + "]]>")

					.append("</line_no__ord>");
					valueXmlString.append("<item_code>")
					.append("<![CDATA[" + mItem + "]]>")
					.append("</item_code>");
					valueXmlString.append("<item_descr>")
					.append("<![CDATA[" + mdescr + "]]>")
					.append("</item_descr>");
					valueXmlString.append("<quantity>")
					.append("<![CDATA[" + mQty + "]]>")
					.append("</quantity>");
					valueXmlString.append("<quantity__stduom>")
					.append("<![CDATA[" + mstdqty + "]]>")//dhiraj
					.append("</quantity__stduom>");
					valueXmlString.append("<conv__qty_stduom>")
					.append("<![CDATA[" + mQtyConv + "]]>")
					.append("</conv__qty_stduom>");
					valueXmlString.append("<unit>")
					.append("<![CDATA[" + mUom + "]]>")
					.append("</unit>");
					valueXmlString.append("<unit__std>")
					.append("<![CDATA[" + mStdUom + "]]>")
					.append("</unit__std>");
					valueXmlString.append("<unit__rate>")
					.append("<![CDATA[" + mRateUom + "]]>")
					.append("</unit__rate>");
					//Commented by sarita on 16 OCT 2018 [START]
					/*valueXmlString.append("<rate>")
								.append("<![CDATA[" + mrate + "]]>")  
								.append("</rate>");*/
					//Commented by sarita on 16 OCT 2018 [END]
					valueXmlString.append("<rate__clg>")
					.append("<![CDATA[" + lc_rate__clg + "]]>")
					.append("</rate__clg>");
					//lc_rate_st=lc_rate_st==0?0:lc_rate_st;
					valueXmlString.append("<rate__stduom>")
					.append("<![CDATA[" + lc_rate_st + "]]>")
					.append("</rate__stduom>");
					valueXmlString.append("<conv__rtuom_stduom>")
					.append("<![CDATA[" + mRtConv + "]]>")
					.append("</conv__rtuom_stduom>");
					valueXmlString.append("<discount>")
					.append("<![CDATA[" + mdiscount + "]]>")
					.append("</discount>");
					valueXmlString.append("<acct_code__dr>")
					.append("<![CDATA[" + macdr + "]]>")
					.append("</acct_code__dr>");
					valueXmlString.append("<acct_code__cr>")
					.append("<![CDATA[" + maccr + "]]>")
					.append("</acct_code__cr>");
					valueXmlString.append("<cctr_code__dr>")
					.append("<![CDATA[" + mctdr + "]]>")
					.append("</cctr_code__dr>");
					valueXmlString.append("<cctr_code__cr>")
					.append("<![CDATA[" + mctcr + "]]>")
					.append("</cctr_code__cr>");
					valueXmlString.append("<tax_amt>")
					.append("<![CDATA[" + mtax + "]]>")
					.append("</tax_amt>");
					valueXmlString.append("<conv__qty_stduom>")
					.append("<![CDATA[" + mQtyConv + "]]>")
					.append("</conv__qty_stduom>");
					valueXmlString.append("<std_rate>")
					.append("<![CDATA[" + lc_stdrate + "]]>")
					.append("</std_rate>");
					valueXmlString.append("<dept_code>")
					.append("<![CDATA[" + ls_dept_code + "]]>")
					.append("</dept_code>");
					valueXmlString.append("<acct_code__prov_dr>")
					.append("<![CDATA[" + ls_acct_pdr + "]]>")
					.append("</acct_code__prov_dr>");
					valueXmlString.append("<acct_code__prov_cr>")
					.append("<![CDATA[" + ls_acct_pcr + "]]>")
					.append("</acct_code__prov_cr>");
					valueXmlString.append("<cctr_code__prov_dr>")
					.append("<![CDATA[" + ls_cctr_pdr + "]]>")
					.append("</cctr_code__prov_dr>");
					valueXmlString.append("<cctr_code__prov_cr>")
					.append("<![CDATA[" + ls_cctr_pcr + "]]>")
					.append("</cctr_code__prov_cr>");

					lc_ret_opt = checkNull(genericUtility.getColumnValue(
							"ret_opt", dom1));

					if (lc_ret_opt.equalsIgnoreCase("C")) {
						valueXmlString.append("<tax_class>")
						.append("<![CDATA[" + chkNull(mtaxclass) + "]]>")
						.append("</tax_class>");
						valueXmlString.append("<tax_chap>")    
						.append("<![CDATA[" + chkNull(mtaxchap) + "]]>")
						.append("</tax_chap>");
						valueXmlString.append("<tax_env>")  
						.append("<![CDATA[" + chkNull(mtaxenv) + "]]>")
						.append("</tax_env>");
						System.out.println("In If@lc_ret_optIC@line_no__ord@["+mtaxchap+"]@@mtaxclass["+mtaxclass+"]mtaxenv@@"+mtaxenv+"]");
					} else {

						System.out.println("line_no_ord@else");
						valueXmlString.append("<tax_class>")
						.append("<![CDATA["+isNull+"]]>")
						.append("</tax_class>");
						valueXmlString.append("<tax_chap>")
						.append("<![CDATA[" + isNull + "]]>")
						.append("</tax_chap>");
						valueXmlString.append("<tax_env>")
						.append("<![CDATA[" + isNull + "]]>")
						.append("</tax_env>");
					}
					// //////
					valueXmlString.append("<net_amt>")
					.append("<![CDATA[" + mtotamt + "]]>")
					.append("</net_amt>");
					sql = " select loc_code__rej "
							+ " from siteitem where site_code = ? and "
							+ " item_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, ls_site);//
					pstmt.setString(2, mItem);
					// pstmt.setString(3, ls_lineno__rcp);//
					rs = pstmt.executeQuery();
					if (rs.next()) {

						ls_loccoderej = checkNull(rs.getString(1));
						// mCancperc=rs.getString(2);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println("ls_loccoderej@@"+ls_loccoderej);
					if (ls_loccoderej != null
							&& ls_loccoderej.trim().length() > 0) {
						System.out.println("inside if@"+ls_loccoderej);
						valueXmlString
						.append("<loc_code>")
						.append("<![CDATA[" + ls_loccoderej + "]]>")
						.append("</loc_code>");
					} else {
						System.out.println("inside else@"+ls_loccoderej);
						valueXmlString.append("<loc_code>")
						.append("<![CDATA[" + mLocation + "]]>")
						.append("</loc_code>");
					}
					/*ldt_mfgdt = Timestamp.valueOf(genericUtility.getValidDateString(ldt_mfgdt.toString(), genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()));
						ldt_expdt = Timestamp.valueOf(genericUtility.getValidDateString(ldt_expdt.toString(), genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()));
					 */

					if(mlotno!=null&&mlotno.trim().length()>0)
					{
						valueXmlString.append("<lot_no>")
						.append("<![CDATA[" + mlotno + "]]>")
						.append("</lot_no>");
					}else
					{
						valueXmlString.append("<lot_no>")
						.append("<![CDATA["+ temp +"]]>")
						.append("</lot_no>");
					}

					if(mlotsl!=null && mlotsl.trim().length()>0)
					{
						System.out.println("lot sl@@V["+mlotsl+"]");
						valueXmlString.append("<lot_sl>")
						.append("<![CDATA[" + mlotsl + "]]>")
						.append("</lot_sl>");
					}else
					{
						System.out.println("lot sl@@B["+temp+"]");
						valueXmlString.append("<lot_sl>")
						.append("<![CDATA["+ temp +"]]>")
						.append("</lot_sl>");
					}

					valueXmlString.append("<canc_bo>")
					.append("<![CDATA[Y]]>").append("</canc_bo>");

					String ldt_mfgdt1="",ldt_expdt1="";
					if(ldt_mfgdt!=null){
						ldt_mfgdt1=sdfNew.format(ldt_mfgdt).toString();
					}
					if(ldt_expdt!=null)
					{
						ldt_expdt1=sdfNew.format(ldt_expdt).toString();
					}


					valueXmlString.append("<mfg_date>")
					.append("<![CDATA[" + ldt_mfgdt1 + "]]>")
					.append("</mfg_date>");


					valueXmlString.append("<expiry_date>")
					.append("<![CDATA[" + ldt_expdt1 + "]]>")
					.append("</expiry_date>");



					/*
						valueXmlString.append("<mfg_date>")
								.append("<![CDATA[" + sdfNew.format(ldt_mfgdt).toString() + "]]>")
								.append("</mfg_date>");*/
					/*valueXmlString.append("<expiry_date>")
								.append("<![CDATA[" + sdfNew.format(ldt_expdt).toString() + "]]>")
								.append("</expiry_date>");*/
					valueXmlString.append("<no_art>")
					.append("<![CDATA[" + lc_no_art + "]]>")
					.append("</no_art>");
					valueXmlString.append("<tare_weight>")
					.append("<![CDATA[" + lc_tare_wt + "]]>")
					.append("</tare_weight>");
					valueXmlString.append("<gross_weight>")
					.append("<![CDATA[" + lc_gross_wt + "]]>")
					.append("</gross_weight>");
					valueXmlString.append("<net_weight>")
					.append("<![CDATA[" + lc_net_wt + "]]>")
					.append("</net_weight>");
					valueXmlString.append("<pack_code>")
					.append("<![CDATA[" + ls_pack + "]]>")
					.append("</pack_code>");
					valueXmlString.append("<pack_instr>")
					.append("<![CDATA[" + ls_pack_instr + "]]>")
					.append("</pack_instr>");
					valueXmlString.append("<batch_no>")
					.append("<![CDATA[" + ls_batch + "]]>")
					.append("</batch_no>");
					valueXmlString.append("<supp_code__mnfr>")
					.append("<![CDATA[" + ls_supp_mnfr + "]]>")
					.append("</supp_code__mnfr>");
					valueXmlString.append("<potency_perc>")
					.append("<![CDATA[" + lc_pot_perc + "]]>")
					.append("</potency_perc>");
					valueXmlString.append("<site_code__mfg>")
					.append("<![CDATA[" + ls_site_mfg + "]]>")
					.append("</site_code__mfg>");
					//String mLocation1="";
					mLocation = checkNull(genericUtility.getColumnValue(
							"loc_code", dom));
					System.out.println("mLocation@@"+mLocation);
					sql = " select conv__qty_stduom  from    stock "
							+ " where site_code = ? and "
							+ " item_code = ? and "
							+ " loc_code     = ? and "
							+ " lot_no     = ? and " + " lot_sl     = ? ";

					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, ls_site);//
					pstmt.setString(2, mItem);
					pstmt.setString(3, mLocation);
					pstmt.setString(4, mlotno);
					pstmt.setString(5, mlotsl);
					rs = pstmt.executeQuery();
					if (rs.next()) {

						lc_conv_qty_stduom = rs.getDouble(1);
						// mCancperc=rs.getString(2);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					// added the if condition as conv_qty_stduom is being
					// set to 0 in purchase receipt
					System.out.println("sql@@@@"+sql);
					System.out.println("lc_conv_qty_stduom@"+lc_conv_qty_stduom);
					if (lc_conv_qty_stduom > 0) {
						valueXmlString
						.append("<conv__qty_stduom>")
						.append("<![CDATA[" + lc_conv_qty_stduom
								+ "]]>")
						.append("</conv__qty_stduom>");
						System.out.println("lc_conv_qty_stduom@if@"+lc_conv_qty_stduom);
					}



					System.out.println("lot_no@@@@"+mlotno);
					System.out.println("lot_sl@@@@"+mlotsl);
					System.out.println("ls_tranid_ref@@"+ls_tranid_ref);

				}
				else if (currentColumn.trim().equalsIgnoreCase("item_code")) {

					mcode = checkNull(genericUtility.getColumnValue(
							"item_code", dom));
					mPordNo = checkNull(genericUtility.getColumnValue(
							"purc_order", dom));
					mPordLine = checkNull(genericUtility.getColumnValue(
							"line_no__ord", dom));
					sql = "Select     item.descr, item.ordc_perc, "
							+ " itemser.acct_code__pr, itemser.cctr_code__pr, "
							+ " itemser.acct_code__in, itemser.cctr_code__in "
							+ " from item, itemser "
							+ " where item.item_code = ? and "
							+ " item.item_ser = itemser.item_ser ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mcode);//

					rs = pstmt.executeQuery();
					if (rs.next()) {

						mdescr = checkNull(rs.getString(1));
						mcancperc = rs.getDouble(2);
						maccr = checkNull(rs.getString(3));
						mctcr = checkNull(rs.getString(4));
						macdr = checkNull(rs.getString(5));
						mctdr = checkNull(rs.getString(6));

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println("mdescr["+mdescr+"]macdr"+macdr+"]mctdr["+mctdr+"]maccr["+maccr+"]mctcr["+mctcr+"]");
					ls_acdr = checkNull(genericUtility.getColumnValue(
							"acct_code__dr", dom));
					ls_accr = checkNull(genericUtility.getColumnValue(
							"acct_code__cr", dom));
					if ((ls_acdr == null) || ls_acdr.trim().length() == 0) {

						valueXmlString.append("<acct_code__dr>")
						.append("<![CDATA[" + macdr + "]]>")
						.append("</acct_code__dr>");
						valueXmlString.append("<cctr_code__dr>")
						.append("<![CDATA[" + mctdr + "]]>")
						.append("</cctr_code__dr>");
					}
					if ((ls_accr == null) || ls_accr.trim().length() == 0) {

						valueXmlString.append("<acct_code__cr>")
						.append("<![CDATA[" + maccr + "]]>")
						.append("</acct_code__cr>");
						valueXmlString.append("<cctr_code__cr>")
						.append("<![CDATA[" + mctcr + "]]>")
						.append("</cctr_code__cr>");

					}
					valueXmlString.append("<item_descr>")
					.append("<![CDATA[" + mdescr + "]]>")
					.append("</item_descr>");
					if (lc_ret_opt.equalsIgnoreCase("C"))
					{
						ls_tranid_rcp = checkNull(genericUtility.getColumnValue("tran_id__rcp", dom1));
						ls_lineno__rcp = checkNull(genericUtility.getColumnValue("line_no__rcp", dom));
						if ((ls_tranid_rcp != null && ls_tranid_rcp.trim().length() > 0) && (ls_lineno__rcp.trim().length() > 0)) {
							sql = "select tax_class,tax_chap,tax_env "
									+ " from porcpdet " + " where tran_id = ? "
									+ " and line_no = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ls_tranid_rcp);//
							pstmt.setString(2, ls_lineno__rcp);
							rs = pstmt.executeQuery();
							if (rs.next()) {

								mtaxclass = chkNull(rs.getString(1));
								mtaxchap = chkNull(rs.getString(2));
								mtaxenv = chkNull(rs.getString(3));
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

						} else
						{
							sql = " select tax_class,tax_chap,tax_env "
									+
									" from porddet "
									+ " where purc_order = ? and line_no    = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, mPordNo);//
							pstmt.setString(2, mPordLine);
							rs = pstmt.executeQuery();
							if (rs.next()) {

								mtaxclass = chkNull(rs.getString(1));
								mtaxchap = chkNull(rs.getString(2));
								mtaxenv = chkNull(rs.getString(3));

							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

						}

						System.out.println("mtaxchap@@["+mtaxchap+"]@@mtaxclass["+mtaxclass+"]mtaxenv@@"+mtaxenv);
						valueXmlString.append("<tax_chap>")
						.append("<![CDATA[" + isNull + "]]>")
						.append("</tax_chap>");
						valueXmlString.append("<tax_class>")
						.append("<![CDATA[" + isNull + "]]>")
						.append("</tax_class>");
						valueXmlString.append("<tax_env>")
						.append("<![CDATA[" + isNull + "]]>")
						.append("</tax_env>");

					} else
					{


						System.out.println("In else@Item_codeIC@["+mtaxchap+"]@@mtaxclass["+mtaxclass+"]mtaxenv@@"+mtaxenv+"]");
						valueXmlString.append("<tax_chap>")
						.append("<![CDATA["+isNull+"]]>").append("</tax_chap>");
						valueXmlString.append("<tax_class>")
						.append("<![CDATA["+isNull+"]]>").append("</tax_class>");
						valueXmlString.append("<tax_env>")
						.append("<![CDATA["+isNull+"]]>").append("</tax_env>");

					}
					mPordNo = checkNull(genericUtility.getColumnValue(
							"purc_order", dom));
					mPordLine = checkNull(genericUtility.getColumnValue(
							"line_no__ord", dom));
					//Modified by Pavan Rane 21NOV19 start[values taken from dom1 instead of dom]
					ls_supp_code = checkNull(genericUtility.getColumnValue("supp_code", dom1));
					ls_site_code = checkNull(genericUtility.getColumnValue("site_code", dom1));
					ls_item_ser = checkNull(genericUtility.getColumnValue("item_ser", dom1));
					ls_tranidref = checkNull(genericUtility.getColumnValue("tran_id__ref", dom1));
					//Modified by Pavan Rane 21NOV19 end[values taken from dom1 instead of dom]
					if ((mPordLine == null || mPordLine.trim().length() == 0)
							&& (ls_tranidref.trim().length() == 0 || ls_tranidref == null)) {
						sql = "select d.purc_order, d.line_no  "
								+ " from porder h, porddet d "
								+ " where h.supp_code = ? "
								+ " and    h.site_code__dlv = ? "
								+ " and    h.item_ser = ? "
								+ " and    h.status = 'O' "
								+ " and    d.purc_order = h.purc_order "
								+ " and    d.status = 'O' "
								+ " and    d.item_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, ls_supp_code);//
						pstmt.setString(2, ls_site_code);
						pstmt.setString(3, ls_item_ser);
						pstmt.setString(4, mcode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							mPordNo = rs.getString(1);
							mPordLine = rs.getString(2);

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<purc_order>")
						.append("<![CDATA[" + mPordNo + "]]>")
						.append("</purc_order>");
						valueXmlString.append("<line_no__ord>")
						.append("<![CDATA[" + mPordLine + "]]>")
						.append("</line_no__ord>");

					} else if ((mPordLine == null || mPordLine.trim().length() == 0)
							&& (ls_tranidref.trim().length() > 0 && ls_tranidref != null)) {
						sql = " select d.purc_order, d.line_no " +

						" from porder h, porddet d ,PORCP P, PORCPDET c "
						+ " where H.purc_order = P.purc_order "
						+ " AND h.supp_code = ? "
						+ " and    h.site_code__dlv = ? "
						+ " and    h.item_ser = ? "
						+ " and    d.purc_order = h.purc_order "
						//Modified by Pavan Rane 21NOV19 start[join added as per suggested by sm sir]
						+ " and P.tran_id = c.tran_id and c.line_no__ord = d.line_no "
						//Modified by Pavan Rane 21NOV19 end[join added as per suggested by sm sir]
						+ " and    d.item_code = ? "
						+ " AND P.TRAN_ID = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, ls_supp_code);//
						pstmt.setString(2, ls_site_code);
						pstmt.setString(3, ls_item_ser);
						pstmt.setString(4, mcode);
						pstmt.setString(5, ls_tranidref);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							// into :mpordno, :mpordline
							mPordNo = checkNull(rs.getString(1));
							mPordLine = checkNull(rs.getString(2));


						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("mPordNo@" + mPordNo + "mPordLine@"
								+ mPordLine);
						valueXmlString.append("<purc_order>")
						.append("<![CDATA[" + mPordNo + "]]>")
						.append("</purc_order>");
						valueXmlString.append("<line_no__ord>")
						.append("<![CDATA[" + mPordLine + "]]>")
						.append("</line_no__ord>");

					}
					
					// Added by Mahesh Saggam on 28-June-2019 [Start]
					ls_site = checkNull(genericUtility.getColumnValue("site_code", dom1));
					System.out.println("$$Site Code = "+ls_site);
					if(mcode != null && mcode.trim().length() > 0)
					{
						sql = "select count(*) from stock where ITEM_CODE = ? and site_code = ? and quantity > 0";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mcode);
						pstmt.setString(2, ls_site);
						rs = pstmt.executeQuery();
						
						if(rs.next())
						{
							cnt = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						if(cnt == 1)
						{
							sql = "select loc_code,lot_no,lot_sl from stock where ITEM_CODE = ? and site_code = ? and quantity > 0";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, mcode);
							pstmt.setString(2, ls_site);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								mLocation = rs.getString(1);
								mlotno = rs.getString(2);
								mlotsl = rs.getString(3);
							}
							
							valueXmlString.append("<loc_code>").append("<![CDATA[" + mLocation + "]]>").append("</loc_code>");
							valueXmlString.append("<lot_no>").append("<![CDATA[" + mlotno + "]]>").append("</lot_no>");
							valueXmlString.append("<lot_sl>").append("<![CDATA[" + mlotsl + "]]>").append("</lot_sl>");
						}
					}
					
					// Added by Mahesh Saggam on 28-June-2019 [End]
					//Pavan Rane 09sep19 start [to set line_no__rcp on matching of lot no and itemchange]					
					tranIdRef = checkNull(genericUtility.getColumnValue("tran_id__ref", dom1));
					if(mlotno == null || mlotno.trim().length() == 0) {
						mlotno = checkNull(genericUtility.getColumnValue("lot_no", dom));
					}
					if(tranIdRef != null && tranIdRef.trim().length() > 0)
					{
						System.out.println("item_code.. ["+tranIdRef+"]mcode["+mcode+"]mlotno["+mlotno+"]...");
						cnt = 0;
						sql = "select count(*) from porcpdet where tran_id = ? and item_code = ? and lot_no = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, tranIdRef);
						pstmt.setString(2, mcode);						
						pstmt.setString(3, mlotno);
						rs = pstmt.executeQuery();						
						if(rs.next())
						{
							cnt = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if(cnt == 1)
						{
							sql = "select line_no from porcpdet where tran_id = ? and item_code = ? and lot_no = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, tranIdRef);
							pstmt.setString(2, mcode);						
							pstmt.setString(3, mlotno);
							rs = pstmt.executeQuery();						
							if(rs.next())
							{
								ls_lineno__rcp = rs.getString("line_no");
								System.out.println("porcpdet item line_no["+ls_lineno__rcp+"]");
								valueXmlString.append("<line_no__rcp>").append("<![CDATA[" + ls_lineno__rcp + "]]>").append("</line_no__rcp>");
								setNodeValue(dom, "line_no__rcp", ls_lineno__rcp);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					//Pavan Rane 09sep19 end [to set line_no__rcp on matching of lot no and itemchange]
				} else if (currentColumn.trim().equalsIgnoreCase("quantity")) 
				{
					quantity = genericUtility.getColumnValue("quantity", dom);
					System.out.println("quantity" + quantity);
					mQty = quantity == null ? 0 : Double.parseDouble(quantity);
					mPordNo = checkNull(genericUtility.getColumnValue(
							"purc_order", dom));
					mPordLine = checkNull(genericUtility.getColumnValue(
							"line_no__ord", dom));
					mPordLine = "    " + mPordLine;
					mPordLine = mPordLine.substring(mPordLine.length() - 3,
							mPordLine.length());
					System.out.println("mPordLine@[" + mPordLine + "]");
					mcode = checkNull(genericUtility.getColumnValue(
							"item_code", dom));
					sql = " Select Quantity , Dlv_qty  "
							+

							" From PordDet "
							+ " where Purc_order = ? and Line_no = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mPordNo);//
					pstmt.setString(2, mPordLine);
					rs = pstmt.executeQuery();
					if (rs.next()) {

						mOrdQty = rs.getDouble(1);
						mDlvQty = rs.getDouble(2);

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					mPending = mOrdQty - mDlvQty - mQty;
					System.out.println("mPending@@"+mPending);

					if (mPending > 0) {
						sql = "Select ordc_perc from item "
								+ " where item_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mcode);//

						rs = pstmt.executeQuery();
						if (rs.next()) {
							mcancperc = rs.getDouble(1);

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if ((mPending / mOrdQty * 100) <= mcancperc) {
							valueXmlString.append("<canc_bo>")
							.append("<![CDATA[Y]]>")
							.append("</canc_bo>");

						} else {
							valueXmlString.append("<canc_bo>")
							.append("<![CDATA[N]]>")
							.append("</canc_bo>");

						}

					} 
					else 
					{
						valueXmlString.append("<canc_bo>")
						.append("<![CDATA[Y]]>").append("</canc_bo>");

					}
					mVal = checkNull(genericUtility.getColumnValue("unit", dom));
					mVal1 = checkNull(genericUtility.getColumnValue(
							"unit__std", dom));
					mItem = checkNull(genericUtility.getColumnValue(
							"item_code", dom));
					mQtyConvStr = genericUtility.getColumnValue(
							"conv__qty_stduom", dom);
					System.out.println("mQtyConvStr" + mQtyConvStr);
					mQtyConv = mQtyConvStr == null ? 0 : Double
							.parseDouble(mQtyConvStr);
					lc_conv_temp = mQtyConv;

					if (mVal.trim().length() == 0)
					{
						sql = "Select unit  from item where item_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mItem);//

						rs = pstmt.executeQuery();
						if (rs.next()) {

							mVal = checkNull(rs.getString(1));


						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						mNum  = Double.parseDouble( disscommon.convQtyFactor(mVal, mVal1  , mItem, mQty, mQtyConv, conn ).get(1).toString() );
						System.out.println("mNum@"+mNum);
						//mNum = disscommon.convQtyFactor(mVal, mVal1, mItem,mQty,mQtyConv, conn);
						System.out.println("mNum@[" + mNum + "]");
						valueXmlString.append("<unit>")
						.append("<![CDATA[" + mVal + "]]>")
						.append("</unit>");

					} else 
					{
						mNum  = Double.parseDouble( disscommon.convQtyFactor(mVal, mVal1  , mItem, mQty, mQtyConv, conn ).get(1).toString() );
						System.out.println("mNum@@"+mNum);
					}
					if (lc_conv_temp == 0)
					{

						valueXmlString.append("<conv__qty_stduom>")
						.append("<![CDATA[" + mQtyConv + "]]>")
						.append("</conv__qty_stduom>");

					} else
					{
						valueXmlString.append("<quantity__stduom>")
						.append("<![CDATA[" + mNum + "]]>")
						.append("</quantity__stduom>");
					}

				} else if (currentColumn.trim().equalsIgnoreCase("unit")) {

					mcode = checkNull(genericUtility
							.getColumnValue("unit", dom));
					mVal1 = checkNull(genericUtility.getColumnValue(
							"unit__std", dom));
					mItem = checkNull(genericUtility.getColumnValue(
							"item_code", dom));
					String mQtyStr = "";

					mQtyStr = genericUtility.getColumnValue("quantity", dom);
					System.out.println("mQtyStr@@" + mQtyStr);
					mNum1 = mQtyStr == null ? 0 : Double.parseDouble(mQtyStr);
					mQtyConv = 0;

					double inputQty1=0;
					lcstdqty1 = null;
					lcstdqty1 = new ArrayList();
					lcstdqty1 =disscommon.convQtyFactor(mcode, mVal1, mItem, mNum1, mQtyConv, conn); 
					double lcstdqty2 = Double.parseDouble(lcstdqty1.get(1).toString());
					double lcconvtemp = Double.parseDouble(lcstdqty1.get(0).toString());
					System.out.println("lcstdqty2@@@["+lcstdqty2+"]");
					System.out.println("lcconvtemp["+lcconvtemp+"]");




					//System.out.println("mNum@[" + mNum + "]");
					valueXmlString.append("<conv__qty_stduom>")
					.append("<![CDATA[" + lcconvtemp + "]]>")
					.append("</conv__qty_stduom>");
					valueXmlString.append("<quantity__stduom>")
					.append("<![CDATA[" + lcstdqty2 + "]]>")
					.append("</quantity__stduom>");

				} else if (currentColumn.trim().equalsIgnoreCase(
						"conv__qty_stduom")) {
					String conv__qty_stduomStr = "";
					conv__qty_stduomStr = checkNull(genericUtility
							.getColumnValue("conv__qty_stduom", dom));
					mQtyConv = conv__qty_stduomStr == null ? 0 : Double
							.parseDouble(conv__qty_stduomStr);

					mVal = checkNull(genericUtility.getColumnValue("unit", dom));
					mVal1 = checkNull(genericUtility.getColumnValue(
							"unit__std", dom));
					mItem = checkNull(genericUtility.getColumnValue(
							"item_code", dom));
					String mQtyStr = "";
					mQtyStr = genericUtility.getColumnValue("quantity", dom);
					System.out.println("mQtyStr@@" + mQtyStr);
					mNum1 = mQtyStr == null ? 0 : Double.parseDouble(mQtyStr);
					//mNum2 = disscommon.convQtyFactor(mVal,
					//		mVal1, mItem, mNum1, conn);



					mNum2  = Double.parseDouble( disscommon.convQtyFactor(mVal, mVal1  , mItem, mNum1,mQtyConv,  conn ).get(1).toString() );
					System.out.println("mNum2@@@@["+mNum2+"]");




					//System.out.println("mNum@@" + mNum);
					valueXmlString.append("<quantity__stduom>")
					.append("<![CDATA[" + mNum2 + "]]>")
					.append("</quantity__stduom>");

				} else if (currentColumn.trim().equalsIgnoreCase("rate")) {

					mrateStr = checkNull(genericUtility.getColumnValue("rate",
							dom));
					mrate = mrateStr == null ? 0 : Double.parseDouble(mrateStr);
					mVal = checkNull(genericUtility.getColumnValue(
							"unit__rate", dom));
					mVal1 = checkNull(genericUtility.getColumnValue(
							"unit__std", dom));
					mItem = checkNull(genericUtility.getColumnValue(
							"item_code", dom));

					mRtConvStr = checkNull(genericUtility.getColumnValue(
							"conv__rtuom_stduom", dom));
					mRtConv = mRtConvStr == null ? 0 : Double
							.parseDouble(mRtConvStr);
					lc_conv_temp = mRtConv;
					double lc_rate_stTemp=0;
					if (mVal.trim().length() == 0) {
						sql = "Select unit from item where item_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mItem);//
						// pstmt.setString(2, mPordLine);
						rs = pstmt.executeQuery();
						if (rs.next()) {

							mVal = rs.getString(1);

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;


						lc_rate_st  = Double.parseDouble( disscommon.convQtyFactor(mVal1, mVal  , mItem, mrate, mRtConv, conn ).get(1).toString() );
						System.out.println("lc_rate_st@@["+lc_rate_st+"]");

						lc_rate_stTemp  = Double.parseDouble( disscommon.convQtyFactor(mVal1, mVal  , mItem, mrate, mRtConv, conn ).get(0).toString() );
						System.out.println("lc_rate_stTemp@@["+lc_rate_stTemp+"]");


						//System.out.println("mNum@[" + mNum + "]");
						valueXmlString.append("<unit__rate>")
						.append("<![CDATA[" + mVal + "]]>")
						.append("</unit__rate>");
					} else {


						lc_rate_st  = Double.parseDouble( disscommon.convQtyFactor(mVal1, mVal  , mItem, mrate, mRtConv, conn ).get(1).toString() );
						System.out.println("lc_rate_st@@["+lc_rate_st+"]");

						lc_rate_stTemp  = Double.parseDouble( disscommon.convQtyFactor(mVal1, mVal  , mItem, mrate, mRtConv, conn ).get(0).toString() );
						System.out.println("lc_rate_stTemp@@["+lc_rate_stTemp+"]");


					}
					if (lc_conv_temp == 0) {
						valueXmlString.append("<conv__rtuom_stduom>")
						.append("<![CDATA[" + lc_rate_stTemp + "]]>")
						.append("</conv__rtuom_stduom>");


					}
					valueXmlString.append("<rate__stduom>")
					.append("<![CDATA[" + lc_rate_st + "]]>")
					.append("</rate__stduom>");


				} else if (currentColumn.trim().equalsIgnoreCase("unit__rate")) {


					mVal = checkNull(genericUtility.getColumnValue(
							"unit__rate", dom));
					mVal1 = checkNull(genericUtility.getColumnValue(
							"unit__std", dom));
					mrate1 = checkNull(genericUtility.getColumnValue("rate",
							dom));
					mRate = mrate1 == null ? 0 : Double.parseDouble(mrate1);
					mRtConv = 0;
					mItem = checkNull(genericUtility.getColumnValue(
							"item_code", dom));


					double inputQty1=0;
					ArrayList lcstdqty3 = null;
					lcstdqty3 = new ArrayList();
					lcstdqty3 =disscommon.convQtyFactor(mVal,mVal1, mItem, mRate, mRtConv, conn); 
					double lcstdqty4 = Double.parseDouble(lcstdqty3.get(1).toString());
					double lcconvtemp4 = Double.parseDouble(lcstdqty3.get(0).toString());
					System.out.println("lcstdqty4@@@["+lcstdqty4+"]");
					System.out.println("lcconvtemp4["+lcconvtemp4+"]");

					valueXmlString.append("<conv__rtuom_stduom>")
					.append("<![CDATA[" + lcconvtemp4 + "]]>")
					.append("</conv__rtuom_stduom>");
					valueXmlString.append("<rate__stduom>")
					.append("<![CDATA[" + lcstdqty4 + "]]>")
					.append("</rate__stduom>");


				} else if (currentColumn.trim().equalsIgnoreCase(
						"conv__rtuom_stduom")) {



					//double munitrate = 0, mRate = 0;
					munitrateStr = checkNull(genericUtility.getColumnValue(
							"conv__rtuom_stduom", dom));
					munitrate = munitrateStr == null ? 0 : Double
							.parseDouble(munitrateStr);
					mVal = checkNull(genericUtility.getColumnValue(
							"unit__rate", dom));
					mVal1 = checkNull(genericUtility.getColumnValue(
							"unit__std", dom));
					mrate1 = checkNull(genericUtility.getColumnValue("rate",
							dom));

					mItem = checkNull(genericUtility.getColumnValue(
							"item_code", dom));

					mRate = mrate1 == null ? 0 : Double.parseDouble(mrate1);
					mRtConv = 0;
					//lc_rate_st = disscommon.convQtyFactor(mVal1, munitrateStr,mItem, mRate,mRate, conn);
					//lc_rate_st = gf_conv_qty_fact(mVal1, mVal, mitem, mrate, mRtConv)
					lc_rate_st  = Double.parseDouble( disscommon.convQtyFactor(mVal1,mVal,mItem, mRate , mRtConv, conn ).get(1).toString() );
					System.out.println("lc_rate_st@@@["+lc_rate_st+"]");

					/*
					double inputQty1=0;
	            	lcstdqty1 = null;
					lcstdqty1 = new ArrayList();
					lcstdqty1 =disscommon.convQtyFactor(mVal1, mVal, mItem, mRate, mRtConv, conn); 
					double lcstdqty2 = Double.parseDouble(lcstdqty1.get(1).toString());
					double lcconvtemp = Double.parseDouble(lcstdqty1.get(0).toString());
					System.out.println("lcstdqty2@@@["+lcstdqty2+"]");
					System.out.println("lcconvtemp["+lcconvtemp+"]");


					 */

					/*
					valueXmlString.append("<conv__rtuom_stduom>")
							.append("<![CDATA[" + lcconvtemp + "]]>")
							.append("</conv__rtuom_stduom>");*/
					valueXmlString.append("<rate__stduom>")
					.append("<![CDATA[" + lc_rate_st + "]]>")
					.append("</rate__stduom>");

				} else if ((currentColumn.trim().equalsIgnoreCase(
						"gross_weight"))) {

					/*String munitrateStr = "", mVal = "", mVal1 = "", grossWt = "", tareWt = "";
					double munitrate = 0, mRate = 0, mnum = 0, mnum1 = 0, mnum2 = 0;
					 */
					grossWt = checkNull(genericUtility.getColumnValue(
							"gross_weight", dom));
					mnum = grossWt == null ? 0 : Double.parseDouble(grossWt);
					tareWt = checkNull(genericUtility.getColumnValue(
							"tare_weight", dom));
					mnum1 = tareWt == null ? 0 : Double.parseDouble(tareWt);

					if (mnum == 0) {
						valueXmlString.append("<gross_weight>").append("0")
						.append("</gross_weight>");
					}
					if (mnum1 == 0) {

						valueXmlString.append("<tare_weight>").append("0")
						.append("</tare_weight>");

					}
					mnum2 = Math.abs(mnum - mnum1);
					valueXmlString.append("<net_weight>")
					.append("<![CDATA[" + mnum2 + "]]>")
					.append("</net_weight>");


				}else if ((currentColumn.trim().equalsIgnoreCase("tare_weight"))) {

					grossWt = checkNull(genericUtility.getColumnValue(
							"gross_weight", dom));
					mnum = grossWt == null ? 0 : Double.parseDouble(grossWt);
					tareWt = checkNull(genericUtility.getColumnValue(
							"tare_weight", dom));
					mnum1 = tareWt == null ? 0 : Double.parseDouble(tareWt);

					if (mnum == 0) {
						valueXmlString.append("<gross_weight>").append("0")
						.append("</gross_weight>");
					}
					if (mnum1 == 0) {

						valueXmlString.append("<tare_weight>").append("0")
						.append("</tare_weight>");

					}

					mnum2 = Math.abs(mnum - mnum1);
					valueXmlString.append("<net_weight>")
					.append("<![CDATA[" + mnum2 + "]]>")
					.append("</net_weight>");



				}else if (currentColumn.trim().equalsIgnoreCase("loc_code"))// "","",""
				{

					mLocation = checkNull(genericUtility.getColumnValue(
							"loc_code", dom));
					mlotno = checkNull(genericUtility.getColumnValue("lot_no",
							dom));
					mlotsl = checkNull(genericUtility.getColumnValue("lot_sl",
							dom));
					mItem = checkNull(genericUtility.getColumnValue(
							"item_code", dom));
					ls_site = checkNull(genericUtility.getColumnValue(
							"site_code", dom1));
					//Added by sarita on 24 OCT 2018 [START]
					/*ldt_trandtStr = checkNull(genericUtility.getColumnValue(
							"tran_date", dom));*/
					ldt_trandtStr = checkNull(genericUtility.getColumnValue(
							"tran_date", dom1));
					//Added by sarita on 24 OCT 2018 [END]
					mPordNo = checkNull(genericUtility.getColumnValue(
							"purc_order", dom));
					mrateStr = checkNull(genericUtility.getColumnValue("rate",
							dom));
					mrate = mrateStr == null ? 0 : Double.parseDouble(mrateStr);
					System.out.println("mrate@[" + mrate + "]");
					sql = "select  mfg_date,exp_date , batch_no " +

					" from stock " + " where item_code     = ? "
					+ " and site_code     = ? "
					+ " and loc_code      = ? "
					+ " and lot_no         = ? "
					+ " and lot_sl         = ? ";

					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mItem);//
					pstmt.setString(2, ls_site);
					pstmt.setString(3, mLocation);
					pstmt.setString(4, mlotno);
					pstmt.setString(5, mlotsl);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						ldt_mfgdt = rs.getTimestamp(1);
						ldt_expdt = rs.getTimestamp(2);
						ls_batch = rs.getString(3);

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					String ldt_mfgdtStr="",ldt_expdtStr="";
					if(ldt_mfgdt!=null)
					{
						ldt_mfgdtStr=sdfNew.format(ldt_mfgdt).toString();
						//ld_tax_date = Timestamp.valueOf(genericUtility.getValidDateString(ld_tax_date.toString(),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()));
					}
					if(ldt_expdt!=null)
					{
						ldt_expdtStr=sdfNew.format(ldt_expdt).toString();
						//ld_tax_date = Timestamp.valueOf(genericUtility.getValidDateString(ld_tax_date.toString(),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()));
					}
					// Changed by Mahesh Saggam on 23 jul 19 [Start] 
					ls_site_mfg = gfgetmfgsite(mItem, ls_site, mLocation,
							mlotno, mlotsl, "M", conn);
					System.out.println("ls_site_mfg@function[" + ls_site_mfg
							+ "]");
					if ("NOTFOUND".equalsIgnoreCase(ls_site_mfg))
					{
						valueXmlString.append("<site_code__mfg/>");
					}
					else
					{
						valueXmlString.append("<site_code__mfg>").append("<![CDATA[" + ls_site_mfg + "]]>").append("</site_code__mfg>");
					}
					
					
					valueXmlString.append("<mfg_date>")
					.append("<![CDATA[" + ldt_mfgdtStr + "]]>")
					.append("</mfg_date>");
					valueXmlString.append("<expiry_date>")
					.append("<![CDATA[" + ldt_expdtStr + "]]>")
					.append("</expiry_date>");
					valueXmlString.append("<batch_no>")
					.append("<![CDATA[" + ls_batch + "]]>")
					.append("</batch_no>");

					ls_pack = gfgetmfgsite(mItem, ls_site, mLocation, mlotno,mlotsl, "P", conn);
					System.out.println("ls_pack@function[" + ls_pack + "]");
					
					if (!"NOTFOUND".equalsIgnoreCase(ls_pack)) {
						valueXmlString.append("<pack_code>")
						.append("<![CDATA[" + ls_pack + "]]>")
						.append("</pack_code>");
					}
					// Mahesh Saggam [End]

					if (mrate <= 0) {


						mlotno = checkNull(genericUtility.getColumnValue(
								"lot_no", dom));

						// Get price list code from porder
						sql = "select price_list  "
								+ " from porder where purc_order = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mPordNo);//
						rs = pstmt.executeQuery();
						if (rs.next()) {
							ls_pricelist = rs.getString(1);

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						//Added By PriyankaC on 08JAN2019.
						if (ls_pricelist != null && ls_pricelist.trim().length() > 0)
						//if (ls_pricelist.trim().length() > 0)
						{
							mrate = disscommon.pickRate(ls_pricelist,
									ldt_trandtStr, mItem, mlotno, "B", conn);// pickRate(ls_pricelist,
							// ldt_trandt,
							// mItem,mlotno,"B",
							// conn);//gbf_pick_rate(ls_pricelist,ldt_trandt,mitem,mlotno,'B')
							System.out.println("mrate[" + mrate + "]");

						}
						valueXmlString.append("<rate>")
						.append("<![CDATA[" + mrate + "]]>")
						.append("</rate>");

						mcode = checkNull(genericUtility.getColumnValue(
								"unit__rate", dom));
						mVal1 = checkNull(genericUtility.getColumnValue(
								"unit__std", dom1));
						if (mcode.equalsIgnoreCase(mVal1)) {

							mItem = checkNull(genericUtility.getColumnValue(
									"item_code", dom));
							mNum1Str = checkNull(genericUtility.getColumnValue(
									"rate", dom));
							mNum1 = mNum1Str == null ? 0 : Double
									.parseDouble(mNum1Str);
							mNum2 = 0;

							mNum = disscommon.convQtyFactor(mVal1, mcode,
									mItem, mNum1, conn);
							valueXmlString.append("<conv__rtuom_stduom>")
							.append("<![CDATA[" + mNum2 + "]]>")
							.append("</conv__rtuom_stduom>");
							valueXmlString.append("<rate__stduom>")
							.append("<![CDATA[" + mNum + "]]>")
							.append("</rate__stduom>");

						} else {
							valueXmlString.append("<rate__stduom>")
							.append("<![CDATA[" + mrate + "]]>")
							.append("</rate__stduom>");
						}
					}

					ls_tran_type = checkNull(genericUtility.getColumnValue("tran_date", dom));
					
					// Added By PriyankaC on 07JAN2018 [START]
					/*lc_rate__clgStr = checkNull(genericUtility.getColumnValue("rate__clg", dom));
					lc_rate__clg = lc_rate__clgStr == null ? 0 : Double.parseDouble(lc_rate__clgStr);*/
					
				    lc_rate__clgStr = checkDoubleNull(genericUtility.getColumnValue("rate__clg", dom));
				    System.out.println("lc_rate__clgStr : " +lc_rate__clgStr);
				    lc_rate__clg = Double.parseDouble(lc_rate__clgStr);
				     // Added By PriyankaC on 07JAN2018 [END]

					if (lc_rate__clg == 0) {
						lc_rate__clg = 0;

					}
					if (lc_rate__clg <= 0) {
						sql = "select price_list__clg  " + " from porder "
								+ " where purc_order = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mPordNo);//
						rs = pstmt.executeQuery();
						if (rs.next()) {
							ls_pricelist__clg = rs.getString(1);

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (( ls_pricelist__clg == null || ls_pricelist__clg.trim().length() == 0))
						{
							sql = "select udf_str2  " + " from gencodes "
									+ " where upper(fld_name) = 'TRAN_TYPE' "
									+ " and    upper(mod_name) = 'W_PORCP' "
									+ " and    fld_value = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ls_tran_type);//
							rs = pstmt.executeQuery();
							if (rs.next()) {
								ls_pricelist__clg = rs.getString(1);

							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

						}
						lc_rate__clg = disscommon.pickRate(ls_pricelist__clg,
								ldt_trandtStr, mItem, mlotno, "B", conn);
						valueXmlString.append("<rate__clg>")
						.append("<![CDATA[" + lc_rate__clg + "]]>")
						.append("</rate__clg>");

					}

				} else if(currentColumn.trim().equalsIgnoreCase("lot_no"))
				{
					mLocation = checkNull(genericUtility.getColumnValue(
							"loc_code", dom));
					mlotno = checkNull(genericUtility.getColumnValue("lot_no",
							dom));
					mlotsl = checkNull(genericUtility.getColumnValue("lot_sl",
							dom));
					mItem = checkNull(genericUtility.getColumnValue(
							"item_code", dom));
					ls_site = checkNull(genericUtility.getColumnValue(
							"site_code", dom1));
					//Commented and added by sarita on 16 OCT 2018 as tran_date should get from dom1 [START]
					/*ldt_trandtStr = checkNull(genericUtility.getColumnValue(
							"tran_date", dom));*/
					ldt_trandtStr = checkNull(genericUtility.getColumnValue(
							"tran_date", dom1));
					//Commented and added by sarita on 16 OCT 2018 as tran_date should get from dom1 [END]
					mPordNo = checkNull(genericUtility.getColumnValue(
							"purc_order", dom));
					mrateStr = checkNull(genericUtility.getColumnValue("rate",
							dom));
					mrate = mrateStr == null ? 0 : Double.parseDouble(mrateStr);
					System.out.println("mrate@[" + mrate + "]");
					sql = "select  mfg_date,exp_date , batch_no " +

					" from stock " + " where item_code     = ? "
					+ " and site_code     = ? "
					+ " and loc_code      = ? "
					+ " and lot_no         = ? "
					+ " and lot_sl         = ? ";

					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mItem);//
					pstmt.setString(2, ls_site);
					pstmt.setString(3, mLocation);
					pstmt.setString(4, mlotno);
					pstmt.setString(5, mlotsl);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						ldt_mfgdt = rs.getTimestamp(1);
						ldt_expdt = rs.getTimestamp(2);
						ls_batch = rs.getString(3);

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if(ldt_mfgdt==null)
					{
						ldt_mfgdt=null;
					}
					if(ldt_expdt==null)
					{
						ldt_expdt=null;
					}


					String ldt_mfgdtStr="",ldt_expdtStr="";
					if(ldt_mfgdt!=null)
					{
						ldt_mfgdtStr=sdfNew.format(ldt_mfgdt).toString();
						//ld_tax_date = Timestamp.valueOf(genericUtility.getValidDateString(ld_tax_date.toString(),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()));
					}
					if(ldt_expdt!=null)
					{
						ldt_expdtStr=sdfNew.format(ldt_expdt).toString();
						//ld_tax_date = Timestamp.valueOf(genericUtility.getValidDateString(ld_tax_date.toString(),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()));
					}
					// Changed by Mahesh Saggam on 23 jul 19 [Start]  
					ls_site_mfg = gfgetmfgsite(mItem, ls_site, mLocation,mlotno, mlotsl, "M", conn);
					System.out.println("ls_site_mfg@function[" + ls_site_mfg+ "]");
					if ("NOTFOUND".equalsIgnoreCase(ls_site_mfg))
					{
						valueXmlString.append("<site_code__mfg/>");
					}else {
						valueXmlString.append("<site_code__mfg>").append("<![CDATA[" + ls_site_mfg + "]]>").append("</site_code__mfg>");	
					}														
					valueXmlString.append("<mfg_date>")
					.append("<![CDATA[" + ldt_mfgdtStr + "]]>")
					.append("</mfg_date>");
					valueXmlString.append("<expiry_date>")
					.append("<![CDATA[" + ldt_expdtStr + "]]>")
					.append("</expiry_date>");
					valueXmlString.append("<batch_no>")
					.append("<![CDATA[" + ls_batch + "]]>")
					.append("</batch_no>");
					System.out.println("LOT_NO@DATE Sucess");
					
					ls_pack = gfgetmfgsite(mItem, ls_site, mLocation, mlotno,mlotsl, "P", conn);
					System.out.println("ls_pack@function[" + ls_pack + "]");
					if (!"NOTFOUND".equalsIgnoreCase(ls_pack)) 
					{
						valueXmlString.append("<pack_code>").append("<![CDATA[" + ls_pack + "]]>").append("</pack_code>");
					}
					// Mahesh Saggam [End]
					if (mrate <= 0)
					{

						mlotno = checkNull(genericUtility.getColumnValue(
								"lot_no", dom));

						// Get price list code from porder
						sql = "select price_list  "
								+ " from porder where purc_order = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mPordNo);//
						rs = pstmt.executeQuery();
						if (rs.next()) {
							ls_pricelist = checkNull(rs.getString(1));

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (ls_pricelist!=null && ls_pricelist.trim().length() > 0)
						{
							System.out.println("ldt_trandtStr-1111-["+ldt_trandtStr+"]");
							mrate = disscommon.pickRate(ls_pricelist,
									ldt_trandtStr, mItem, mlotno, "B", conn);// pickRate(ls_pricelist,
							// ldt_trandt,
							System.out.println("mrate[" + mrate + "]");

							// conn);//gbf_pick_rate(ls_pricelist,ldt_trandt,mitem,mlotno,'B')
						}
						valueXmlString.append("<rate>")
						.append("<![CDATA[" + mrate + "]]>")
						.append("</rate>");
						mcode = checkNull(genericUtility.getColumnValue(
								"unit__rate", dom));
						mVal1 = checkNull(genericUtility.getColumnValue(
								"unit__std", dom1));
						if (mcode.equalsIgnoreCase(mVal1)) {

							mItem = checkNull(genericUtility.getColumnValue(
									"item_code", dom));
							mNum1Str = checkNull(genericUtility.getColumnValue(
									"rate", dom));
							mNum1 = mNum1Str == null ? 0 : Double
									.parseDouble(mNum1Str);
							mNum2 = 0;
							mNum = disscommon.convQtyFactor(mVal1, mcode,
									mItem, mNum1, conn);
							valueXmlString.append("<conv__rtuom_stduom>")
							.append("<![CDATA[" + mNum2 + "]]>")
							.append("</conv__rtuom_stduom>");
							valueXmlString.append("<rate__stduom>")
							.append("<![CDATA[" + mNum + "]]>")
							.append("</rate__stduom>");

						} else {
							valueXmlString.append("<rate__stduom>")
							.append("<![CDATA[" + mrate + "]]>")
							.append("</rate__stduom>");
						}
					}
					ls_tran_type = checkNull(genericUtility.getColumnValue(
							"tran_type", dom));
					lc_rate__clgStr = checkNull(genericUtility.getColumnValue(
							"rate__clg", dom));
					System.out.println("Rate String = "+lc_rate__clgStr);
					lc_rate__clg = lc_rate__clgStr == null || lc_rate__clgStr.trim().length() == 0 ? 0 : Double.parseDouble(lc_rate__clgStr);
					System.out.println("LOT_NO@ls_tran_type check["+ls_tran_type);
					if (lc_rate__clg == 0) {
						lc_rate__clg = 0;

					}
					if (lc_rate__clg <= 0) {
						sql = "select price_list__clg  " + " from porder "
								+ " where purc_order = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mPordNo);//
						rs = pstmt.executeQuery();
						if (rs.next()) {
							ls_pricelist__clg = checkNull(rs.getString(1));

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (ls_pricelist__clg == null || (ls_pricelist__clg.trim().length() == 0)) {
							sql = "select udf_str2  " + " from gencodes "
									+ " where upper(fld_name) = 'TRAN_TYPE' "
									+ " and    upper(mod_name) = 'W_PORCP' "
									+ " and    fld_value = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ls_tran_type);//
							rs = pstmt.executeQuery();
							if (rs.next()) {
								ls_pricelist__clg = rs.getString(1);

							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

						}
						lc_rate__clg = disscommon.pickRate(ls_pricelist__clg,
								ldt_trandtStr, mItem, mlotno, "B", conn);
						valueXmlString.append("<rate__clg>")
						.append("<![CDATA[" + lc_rate__clg + "]]>")
						.append("</rate__clg>");

					}
					
					// Added by Mahesh Saggam on 28-June-2019 [Start]
					mItem = checkNull(genericUtility.getColumnValue("item_code", dom));
					ls_site = checkNull(genericUtility.getColumnValue("site_code", dom1));
					if(mlotno != null && mlotno.trim().length() > 0)
					{
						sql = "select count(*) from stock where ITEM_CODE = ? and SITE_CODE = ? and LOT_NO = ? and quantity > 0";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mItem);
						pstmt.setString(2, ls_site);
						pstmt.setString(3, mlotno);
						rs = pstmt.executeQuery();
						
						if(rs.next())
						{
							cnt = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						if(cnt == 1)
						{
							sql = " select lot_sl  from stock where ITEM_CODE = ? and SITE_CODE = ? and LOT_NO = ? and quantity > 0";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, mItem);
							pstmt.setString(2, ls_site);
							pstmt.setString(3, mlotno);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								mlotsl = rs.getString(1);
							}
							
							valueXmlString.append("<lot_sl>").append("<![CDATA[" + mlotsl + "]]>").append("</lot_sl>");
						}
					}
					//Pavan Rane 09sep19 start [to set line_no__rcp on matching of lot no and itemchange]					
					tranIdRef = checkNull(genericUtility.getColumnValue("tran_id__ref", dom1));
					if(tranIdRef != null && tranIdRef.trim().length() > 0)
					{
						System.out.println("lot_no ["+tranIdRef+"]mItem["+mItem+"]mlotno["+mlotno+"]...");
						cnt = 0;
						sql = "select count(*) from porcpdet where tran_id = ? and item_code = ? and lot_no = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, tranIdRef);
						pstmt.setString(2, mItem);						
						pstmt.setString(3, mlotno);
						rs = pstmt.executeQuery();						
						if(rs.next())
						{
							cnt = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if(cnt == 1)
						{
							sql = "select line_no from porcpdet where tran_id = ? and item_code = ? and lot_no = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, tranIdRef);
							pstmt.setString(2, mItem);						
							pstmt.setString(3, mlotno);
							rs = pstmt.executeQuery();						
							if(rs.next())
							{
								ls_lineno__rcp = rs.getString("line_no");
								System.out.println("porcpdet line_no["+ls_lineno__rcp+"]");
								valueXmlString.append("<line_no__rcp>").append("<![CDATA[" + ls_lineno__rcp + "]]>").append("</line_no__rcp>");
								setNodeValue(dom, "line_no__rcp", ls_lineno__rcp);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					//Pavan Rane 09sep19 end [to set line_no__rcp on matching of lot no and itemchange]					
					// Added by Mahesh Saggam on 28-June-2019 [End]

				} else if(currentColumn.trim().equalsIgnoreCase("lot_sl"))
				{

					mLocation = checkNull(genericUtility.getColumnValue(
							"loc_code", dom));
					mlotno = checkNull(genericUtility.getColumnValue("lot_no",
							dom));
					mlotsl = checkNull(genericUtility.getColumnValue("lot_sl",
							dom));
					mItem = checkNull(genericUtility.getColumnValue(
							"item_code", dom));
					ls_site = checkNull(genericUtility.getColumnValue(
							"site_code", dom1));
					//Added and commented by sarita as tran_date should come from dom1 [START]
					/*ldt_trandtStr = checkNull(genericUtility.getColumnValue(
							"tran_date", dom));*/
					ldt_trandtStr = checkNull(genericUtility.getColumnValue(
							"tran_date", dom1));
					//Added and commented by sarita as tran_date should come from dom1 [END]
					mPordNo = checkNull(genericUtility.getColumnValue(
							"purc_order", dom));
					mrateStr = checkNull(genericUtility.getColumnValue("rate",
							dom));
					mrate = mrateStr == null ? 0 : Double.parseDouble(mrateStr);
					System.out.println("mrate@[" + mrate + "]");
					sql = "select  mfg_date,exp_date , batch_no " +

					" from stock " + " where item_code     = ? "
					+ " and site_code     = ? "
					+ " and loc_code      = ? "
					+ " and lot_no         = ? "
					+ " and lot_sl         = ? ";

					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mItem);//
					pstmt.setString(2, ls_site);
					pstmt.setString(3, mLocation);
					pstmt.setString(4, mlotno);
					pstmt.setString(5, mlotsl);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						ldt_mfgdt = rs.getTimestamp(1);
						ldt_expdt = rs.getTimestamp(2);
						ls_batch = rs.getString(3);

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if(ldt_mfgdt==null)
					{
						ldt_mfgdt=null;
					}
					if(ldt_expdt==null)
					{
						ldt_expdt=null;
					}
					String ldt_mfgdtStr="",ldt_expdtStr="";
					if(ldt_mfgdt!=null)
					{
						ldt_mfgdtStr=sdfNew.format(ldt_mfgdt).toString();
						//ld_tax_date = Timestamp.valueOf(genericUtility.getValidDateString(ld_tax_date.toString(),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()));
					}
					if(ldt_expdt!=null)
					{
						ldt_expdtStr=sdfNew.format(ldt_expdt).toString();
						//ld_tax_date = Timestamp.valueOf(genericUtility.getValidDateString(ld_tax_date.toString(),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()));
					}
					// Changed by Mahesh Saggam on 23 jul 19 [Start] 
					ls_site_mfg = gfgetmfgsite(mItem, ls_site, mLocation,mlotno, mlotsl, "M", conn);
					System.out.println("ls_site_mfg@function[" + ls_site_mfg+ "]");	
					
					if ("NOTFOUND".equalsIgnoreCase(ls_site_mfg))
					{
						valueXmlString.append("<site_code__mfg/>");
					}else {
						valueXmlString.append("<site_code__mfg>").append("<![CDATA[" + ls_site_mfg + "]]>").append("</site_code__mfg>");
					}
					valueXmlString.append("<mfg_date>")
					.append("<![CDATA[" + ldt_mfgdtStr + "]]>")
					.append("</mfg_date>");
					valueXmlString.append("<expiry_date>")
					.append("<![CDATA[" + ldt_expdtStr + "]]>")
					.append("</expiry_date>");
					valueXmlString.append("<batch_no>")
					.append("<![CDATA[" + ls_batch + "]]>")
					.append("</batch_no>");
					System.out.println("LOT_NO@DATE Sucess");
					
					ls_pack = gfgetmfgsite(mItem, ls_site, mLocation, mlotno,mlotsl, "P", conn);
					System.out.println("ls_pack@function[" + ls_pack + "]");
					if (!"NOTFOUND".equalsIgnoreCase(ls_pack)) 
					{
						valueXmlString.append("<pack_code>").append("<![CDATA[" + ls_pack + "]]>").append("</pack_code>");
					}
					// Mahesh Saggam [End]
					if (mrate <= 0) {

						mlotno = checkNull(genericUtility.getColumnValue(
								"lot_no", dom));

						// Get price list code from porder
						sql = "select price_list  "
								+ " from porder where purc_order = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mPordNo);//
						rs = pstmt.executeQuery();
						if (rs.next()) {
							ls_pricelist = checkNull(rs.getString(1));

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if (ls_pricelist!=null && ls_pricelist.trim().length() > 0) {
							mrate = disscommon.pickRate(ls_pricelist,
									ldt_trandtStr, mItem, mlotno, "B", conn);// pickRate(ls_pricelist,
							// ldt_trandt,
							// mItem,mlotno,"B",
							// conn);//gbf_pick_rate(ls_pricelist,ldt_trandt,mitem,mlotno,'B')
							System.out.println("mrate[" + mrate + "]");

						}
						valueXmlString.append("<rate>")
						.append("<![CDATA[" + mrate + "]]>")
						.append("</rate>");
						mcode = checkNull(genericUtility.getColumnValue(
								"unit__rate", dom));
						mVal1 = checkNull(genericUtility.getColumnValue(
								"unit__std", dom1));
						if (mcode.equalsIgnoreCase(mVal1)) {

							mItem = checkNull(genericUtility.getColumnValue(
									"item_code", dom));
							mNum1Str = checkNull(genericUtility.getColumnValue(
									"rate", dom));
							mNum1 = mNum1Str == null ? 0 : Double
									.parseDouble(mNum1Str);
							mNum2 = 0;
							mNum = disscommon.convQtyFactor(mVal1, mcode,
									mItem, mNum1, conn);
							valueXmlString.append("<conv__rtuom_stduom>")
							.append("<![CDATA[" + mNum2 + "]]>")
							.append("</conv__rtuom_stduom>");
							valueXmlString.append("<rate__stduom>")
							.append("<![CDATA[" + mNum + "]]>")
							.append("</rate__stduom>");

						} else {
							valueXmlString.append("<rate__stduom>")
							.append("<![CDATA[" + mrate + "]]>")
							.append("</rate__stduom>");
						}
					}
					ls_tran_type = checkNull(genericUtility.getColumnValue(
							"tran_type", dom));
					lc_rate__clgStr = checkNull(genericUtility.getColumnValue(
							"rate__clg", dom));
					lc_rate__clg = lc_rate__clgStr == null ? 0 : Double
							.parseDouble(lc_rate__clgStr);
					System.out.println("LOT_NO@ls_tran_type check["+ls_tran_type);
					if (lc_rate__clg == 0) {
						lc_rate__clg = 0;

					}
					if (lc_rate__clg <= 0) {
						sql = "select price_list__clg  " + " from porder "
								+ " where purc_order = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mPordNo);//
						rs = pstmt.executeQuery();
						if (rs.next()) {
							ls_pricelist__clg = rs.getString(1);

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (ls_pricelist__clg == null || (ls_pricelist__clg.trim().length() == 0)
								) {
							sql = "select udf_str2  " + " from gencodes "
									+ " where upper(fld_name) = 'TRAN_TYPE' "
									+ " and    upper(mod_name) = 'W_PORCP' "
									+ " and    fld_value = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, ls_tran_type);//
							rs = pstmt.executeQuery();
							if (rs.next()) {
								ls_pricelist__clg = rs.getString(1);

							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

						}
						lc_rate__clg = disscommon.pickRate(ls_pricelist__clg,
								ldt_trandtStr, mItem, mlotno, "B", conn);
						valueXmlString.append("<rate__clg>")
						.append("<![CDATA[" + lc_rate__clg + "]]>")
						.append("</rate__clg>");

					}
				}
				valueXmlString.append("</Detail2>");
			}
			valueXmlString.append("</Root>");

		}catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			throw new ITMException(e);
		} finally {
			try {
				if (conn != null) {
					if (rs != null) {
						rs.close();
						rs = null;
					}
					if (pstmt != null) {
						pstmt.close();
						pstmt = null;

					}
					conn.close();
				}
				conn = null;
			} catch (Exception d) 
			{
				d.printStackTrace();
				//Added by sarita to throw ITMException [START]
				throw new ITMException(d);
				//Added by sarita to throw ITMException [END]
			}
		}
		return valueXmlString.toString();

	}

	private String checkNull(String input) {
		if (input == null) {
			input = "";
		}
		return chkNull(input);
	}
	private String chkNull(String input) {
		if (input == null || "null".equalsIgnoreCase(input)) {
			input = "";
		}
		return input;
	}
//Added By PriyankaC  [Start]
	private String checkDoubleNull(String input) {
		if (input == null || input.trim().length() == 0 ) 
		{
		input = "0";
		}
		return input;
		}
// Added By PriyankaC [END]
	private String gfgetmfgsite(String asitem, String assite, String asloc,
			String aslotno, String aslotsl, String astype, Connection conn) throws ITMException {
		String lsmfgsite = "", lserrcode = "";
		PreparedStatement pstmt = null, pstmt1 = null, pstmt2 = null;
		ResultSet rs = null, rs1 = null, rs2 = null;
		String sql = "", sql2 = "", sql3 = "";
		int asType = 2;
		try {
			System.out.println("aslotno[" + aslotno + "]");
			System.out.println("aslotno length[" + aslotno.trim().length()
					+ "]");
			if (aslotno == null || aslotno.trim().length() == 0) {
				aslotno = "               ";
				System.out.println("aslotno null [" + aslotno + "]");
			}
			if ("M".equalsIgnoreCase(astype)) {
				sql = "select site_code__mfg  from stock where item_code = ? and	site_code = ? and loc_code  = ? and lot_no = ? and lot_sl	 = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, asitem);
				pstmt.setString(2, assite);
				pstmt.setString(3, asloc);
				pstmt.setString(4, aslotno);
				pstmt.setString(5, aslotsl);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					// lsmfgsite = rs.getString(1);
					lsmfgsite = checkNull(rs.getString(1));  // Added by Mahesh Saggam on  22-july-2019
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				System.out.println("Sql1[" + sql + "]");
				System.out.println(" mfgSIte COde is" + lsmfgsite);
				System.out.println("mfgSiteCode Length is"
						+ lsmfgsite.trim().length());
				if (lsmfgsite == null || lsmfgsite.trim().length() == 0) {
					sql2 = "select site_code  from item where item_code = ?";
					pstmt1 = conn.prepareStatement(sql2);
					pstmt1.setString(1, asitem);
					rs1 = pstmt1.executeQuery();
					if (rs1.next()) {
						lsmfgsite = checkNull(rs1.getString(1));
					} else {
						lsmfgsite = "NOTFOUND";
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					System.out.println("Sql3[" + sql2 + "]");
				}
				// }
			}

			if ("P".equalsIgnoreCase(astype)) {
				sql = "select pack_code from stock where item_code = ? and site_code = ? and loc_code  = ? and lot_no = ? and	lot_sl= ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, asitem);
				pstmt.setString(2, assite);
				pstmt.setString(3, asloc);
				pstmt.setString(4, aslotno);
				pstmt.setString(5, aslotsl);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					lsmfgsite = rs.getString(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				System.out.println("Packing COde is" + lsmfgsite);
				//Commented by sarita giving error where Packing COde is null [START] 
				/*System.out.println("Packing COde Length is"
						+ lsmfgsite.trim().length());*/
				//Commented by sarita giving error where Packing COde is null [END] 
				if (lsmfgsite == null || lsmfgsite.trim().length() == 0) {
					sql2 = "select pack_code from item where item_code = ? ";
					pstmt1 = conn.prepareStatement(sql2);
					pstmt1.setString(1, asitem);
					rs1 = pstmt1.executeQuery();
					if (rs1.next()) {
						lsmfgsite = checkNull(rs1.getString(1));
					} else {
						lsmfgsite = "NOTFOUND";
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;

				}
			}
		} catch (Exception e) 
		{		
			System.out.println("The Exception occurs in gfgetmfgsite :" + e);
			//Added by sarita to throw ITMException [START]
			throw new ITMException(e);
			//Added by sarita to throw ITMException [END]
		}
		System.out.println("Packing COde || site_code :[" + lsmfgsite + "]");
		return lsmfgsite;
	}

	//Added by sarita to get Detail2 total quantity on 22 OCT 2018 [START]
	private double getSumOfDetail2Quantity(Document dom2, int lineNoRcp) throws ITMException
	{
		double quantity = 0.0 ,qtyDet = 0.0 ,tblQtyVal = 0.0,tblQty = 0.0;
		NodeList detlList = null;
		int lineNo = 0 , QtyLineNo = 0;
		int cntr = 0;
		String lineNoStr = "";
		try
		{
			XPathFactory xpathFactory1 = XPathFactory.newInstance();
			XPath xpath1 = xpathFactory1.newXPath();
			lineNoStr = String.valueOf(lineNoRcp);
			System.out.println("ABCD ["+genericUtility.serializeDom(dom2)+"]");
			System.out.println("LINE NO RECEIPT 1 ["+lineNoRcp+"]");
			//detlList = dom2.getElementsByTagName("Detail2");
			detlList = getNodebyValue(dom2, xpath1, lineNoStr, "line_no__rcp");
			System.out.println("detlList---["+detlList+"]");
			if (detlList != null)
			{
				for (cntr = 0; cntr < detlList.getLength(); cntr++)
				{	
					//Added and Commented by sarita on 25 JANUARY 2018[START]
					lineNoStr = checkNull(genericUtility.getColumnValueFromNode("line_no__rcp", detlList.item(cntr)));
					if(lineNoStr != null && lineNoStr.trim().length() > 0)
					{
						lineNoStr = lineNoStr.trim();
						lineNo = Integer.parseInt(lineNoStr);
					}
					//lineNo = Integer.parseInt(genericUtility.getColumnValueFromNode("line_no__rcp", detlList.item(cntr)));
					//Added and Commented by sarita on 25 JANUARY 2018 [END]
					System.out.println("lineNo ["+lineNo+"]");
					if(QtyLineNo == lineNo)
					{
						qtyDet = Double.parseDouble(genericUtility.getColumnValueFromNode("quantity", detlList.item(cntr)));
						System.out.println("qtyDet ["+qtyDet+"]");
						quantity = quantity + qtyDet;
						System.out.println("IF getSumOfDetail2Quantity.....1 .. ["+quantity+"]");
					}								
				}
			}
			System.out.println("Value of qtyDet ["+quantity+"] + counter is ["+cntr+"]");
		}
		catch(Exception e)
		{
			System.out.println("[Inside getSumOfDetail2Quantity]" + e);
			throw new ITMException(e);
		}
		return quantity;
	}
	private static void setNodeValue( Document dom, String nodeName, String nodeVal ) throws Exception
	{
		Node tempNode = dom.getElementsByTagName( nodeName ).item(0);
		System.out.println("tempNode is ["+tempNode+"]");
		if( tempNode != null )
		{
			if( tempNode.getFirstChild() == null )
			{
				CDATASection cDataSection = dom.createCDATASection( nodeVal );
				tempNode.appendChild( cDataSection );
				System.out.println("tempNode is 1["+tempNode+"]");
			}
			else
			{
				tempNode.getFirstChild().setNodeValue(nodeVal);
				System.out.println("tempNode is 2["+tempNode+"]");
			}
		}
		tempNode = null;
	}
	private NodeList getNodebyValue(Document doc, XPath xpath,String value,String columnName)
	{
		NodeList nodes = null;
		XPathExpression expr = null;
		try
		{
			if("line_no__rcp".equalsIgnoreCase(columnName))
			{
				expr = xpath.compile("//Detail2 [line_no__rcp = '"+ value +"']");
			}

			System.out.println("expr ==>["+ expr +"]");
			Object result = expr.evaluate(doc, XPathConstants.NODESET);
			nodes = (NodeList) result;
			System.out.println(" NodeLength ==> ["+nodes.getLength()+"]");
		}
		catch (Exception  e)
		{
			System.out.println("in the Node Exception ==> ["+ e.getMessage() +"]");
		}
		return nodes;
	}
	//Added by sarita to get Detail2 total quantity on 22 OCT 2018 [END]
	//Pavan Rane 11jun19 start [to validate Channel Partner Supplier]
	private boolean isChannelPartnerSupp(String suppCode, String siteCode, Connection conn) throws ITMException
	{
		
		String sql = "";
		String disLink = "";
		String chPartner = "";
		boolean cpFlag = false;
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		try
		{
			sql="select channel_partner, dis_link from site_supplier "
					+ " where supp_code = ? and site_code = ?";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1,suppCode);
			pstmt.setString(2,siteCode);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				chPartner=checkNull(rs.getString("channel_partner"));
				disLink=checkNull(rs.getString("dis_link"));
			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;
			if(chPartner.trim().length()==0)
			{
				sql="select channel_partner, dis_link  from supplier "
						+ " where supp_code = ?";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1,suppCode);
				rs=pstmt.executeQuery();
				if(rs.next())
				{
					chPartner=checkNull(rs.getString("channel_partner"));
					disLink=checkNull(rs.getString("dis_link"));
				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
			}	
			if("Y".equalsIgnoreCase(chPartner))
			{
				if (("A".equalsIgnoreCase(disLink)|| "S".equalsIgnoreCase(disLink) || "C".equalsIgnoreCase(disLink) ))
				{
					cpFlag = true;
				}
			}
			
		} catch (SQLException se)
		{			
			BaseLogger.log("0", null, null, "SQLException :PoReturnIc :IsChannelPartnerSupp()::" + se.getMessage());
			se.printStackTrace();			
		}

		catch (Exception e)
		{
			BaseLogger.log("0", null, null, "Exception :PoReturnIc :IsChannelPartnerSupp()()::" + e.getMessage());
			e.printStackTrace();		
		} finally
		{
			try
			{
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
			} catch (Exception e)
			{
			}
		}
		return cpFlag;
	}

}