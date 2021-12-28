
package ibase.webitm.ejb.dis;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.dis.PoReceiptIcLocal;
import ibase.webitm.ejb.dis.PoReceiptIcRemote;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.sys.UtilMethods;
import ibase.webitm.utility.ITMException;
import ibase.utility.BaseLogger;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import javax.ejb.Stateless;
//import javax.jcr.Value;

import org.w3c.dom.CDATASection;
//import org.apache.log4j.spi.ErrorCode;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ibase.utility.UserInfoBean;

@Stateless
public class PoReceiptIc extends ValidatorEJB implements  PoReceiptIcLocal,PoReceiptIcRemote  
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	FinCommon finCommon = new FinCommon();
	DistCommon distcommon=new DistCommon();
	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag,String xtraParams) throws RemoteException, ITMException 
	{
		String errString = "";
		System.out.println("wfValdata() called for PoReceiptIc:");
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try 
		{
			if (xmlString != null && xmlString.trim().length() > 0) {
				dom = parseString(xmlString);
				System.out.println("xmlString["+xmlString+"]");
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0) {
				dom1 = parseString(xmlString1);
				System.out.println("xmlString1["+xmlString1+"]");
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0) {
				dom2 = parseString(xmlString2);
				System.out.println("xmlString2["+xmlString2+"]");
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
		String exchRateStr="";
		double exchRate=0;
		int cnt = 0;
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
		StringBuffer errStringXml = new StringBuffer(
				"<?xml version = \"1.0\"?> \r\n <Root> <Errors>");

		int currentFormNo = 0, recCnt = 0;
		FinCommon fincommon = new FinCommon();
		DistCommon discommon = new DistCommon();
		Timestamp TranDate = null, dlvDate = null, reqDate = null, ordDate = null,trandatePO1=null,Date1=null,Date2=null;
		String tranDateStr="",siteCode="",modName="",purcOrder="",isNullPo="",tranidPO="",tranDateStrPO="";
		String status="",empCodeaprv="",ordDate1="",lsconfirmed="",lsproviid="",lspotype="",tranType="",cwipTranType="",porcpTranType="";
		String lsitemcode="",prdCode="",siteCodlogin="",statFin="",errorType="",sitecode;
		String itemSer="",loccode="",grNo="",tranidGno="",currCode="",tranCode="",dcNo="";
		String qtyStr = "", qtyStdStr = "";    // Added by Mahesh Saggam on 04/07/19
		double dqty = 0, dqtyStduom = 0;
		double llquantity=0,llrate=0;
		String lsmsg="",lsmsg1="",startStr="",endStr="",descrStr="",descrStart="",descrEnd="",value="";
		String lineNoOrd;
		ArrayList lcstdqty = null;
		String allowUnavailLoc="",purcorderType="";    //added by manish mhatre on 14-aug-20
        String channelPartner = "";//Modified by Rohini T on 25-02/2021
        String tranMode="";//added by anagha
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			System.out.println("@@@@@@@@ wfvaldata called");
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			connDriver = null;
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			siteCodlogin =(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode"));

			modName = "P-RCP"; // 14-sep-2018 manoharan

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
					errCode = ""; // 14-sep-18 manoharan initialise errCode
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					System.out.println("childNodeName"+childNodeName);
					if (childNodeName.equalsIgnoreCase("tran_date"))
					{
						tranDateStr = checkNull(genericUtility.getColumnValue("tran_date", dom));
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
					}					
					if (childNodeName.equalsIgnoreCase("site_code")) 
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
					}
					if (childNodeName.equalsIgnoreCase("purc_order")) 
					{
						purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom));
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						if(isNullPo !="Y")
						{
							tranidPO = genericUtility.getColumnValue("tran_id", dom);
							tranDateStrPO = checkNull(genericUtility.getColumnValue("tran_date", dom));
							if(tranidPO == null)
							{
								tranidPO =" ";
							}
							if(purcOrder != null && purcOrder.trim().length() > 0)
							{

								sql = "	select count(*) from porder where purc_order = ? ";
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
									sql="select status ," +
											"emp_code__aprv," +
											"ord_date," +
											"confirmed," +
											"provi_tran_id," +
											"pord_type  from porder where purc_order = ?";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, purcOrder);
									rs = pstmt.executeQuery();
									if (rs.next()) 
									{
										status=rs.getString(1);
										empCodeaprv=rs.getString(2);
										ordDate=rs.getTimestamp(3);
										lsconfirmed=rs.getString(4);
										lsproviid=rs.getString(5);
										lspotype=rs.getString(6);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									System.out.println("status"+status);
									System.out.println("empCodeaprv"+empCodeaprv);
									System.out.println("lsconfirmed"+lsconfirmed);
									System.out.println("lsproviid"+lsproviid);
									System.out.println("lspotype"+lspotype);
									System.out.println("ordDate"+ordDate);
									if(status == null || status.trim().length() == 0)
									{
										status="O";
									}
									if(lsconfirmed == null || lsconfirmed.trim().length() == 0)
									{
										lsconfirmed="N";
									}
									System.out.println("status"+status);
									if( "C".equals(status) || "X".equals(status))
									{
										errCode = "VTPORD2";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									/*if(errCode == null || errCode.trim().length() == 0)
									{*/ //COMMENTED BY NANDKUMAR GADKARI ON 03/05/18
									//if( "O".equals(status) && empCodeaprv==null|| empCodeaprv.trim().length() == 0)
									if( "O".equals(status) && "N".equals(lsconfirmed))
									{
										errCode = "VTPONAPRV";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									/*}
									if(errCode == null || errCode.trim().length() == 0)
									{*///COMMENTED BY NANDKUMAR GADKARI ON 03/05/18
									if ("N".equals(lsconfirmed) || lsconfirmed == null )
									{
										errCode = "VTPONCONF";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									/*}
									else if(errCode == null || errCode.trim().length() == 0)
									{*///COMMENTED BY NANDKUMAR GADKARI ON 03/05/18

									else if (ordDate.after(TranDate)) 
									{
										errCode = "VTRCPDT";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									//}
								}
							}
						}
						else if(purcOrder != null && purcOrder.trim().length() > 0 )
						{
							tranidPO = genericUtility.getColumnValue("tran_id", dom);
							tranDateStrPO = checkNull(genericUtility.getColumnValue("tran_date", dom));
							if(tranidPO == null)
							{
								tranidPO =" ";
							}
							if(purcOrder != null && purcOrder.trim().length() > 0)
							{
								sql = "select count(*) from porder where purc_order = ? ";
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
								else
								{
									sql="select  status," +
											"emp_code__aprv," +
											"ord_date," +
											"confirmed," +
											"provi_tran_id," +
											"pord_type  from porder where purc_order = ?";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, purcOrder);
									rs = pstmt.executeQuery();
									if (rs.next()) 
									{
										status=checkNull(rs.getString(1));
										empCodeaprv=checkNull(rs.getString(2));
										ordDate=rs.getTimestamp(3);
										lsconfirmed=checkNull(rs.getString(4));
										lsproviid=checkNull(rs.getString(5));
										lspotype=checkNull(rs.getString(6));
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									System.out.println("status"+status);
									System.out.println("empCodeaprv"+empCodeaprv);
									System.out.println("lsconfirmed"+lsconfirmed);
									System.out.println("lsproviid"+lsproviid);
									System.out.println("lspotype"+lspotype);
									System.out.println("ordDate"+ordDate);
									if(status == null || status.trim().length() == 0)
									{
										status="O";
									}

									System.out.println("status"+status);
									if(lsconfirmed == null || lsconfirmed.trim().length() == 0)
									{
										status="N";
									}
									if( "C".equals(status) || "X".equals(status))
									{
										errCode = "VTPORD2";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									/*if(errCode == null || errCode.trim().length() == 0)
									{*/ //COMMENTED BY NANDKUMAR GADKARI ON 03/05/18
									if( "O".equals(status) && empCodeaprv==null|| empCodeaprv.trim().length() == 0)
									{
										errCode = "VTPONAPRV";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									//}
									/*if(errCode == null || errCode.trim().length() == 0)
									{*/ //COMMENTED BY NANDKUMAR GADKARI ON 03/05/18
									if ("N".equals(lsconfirmed) || lsconfirmed == null )
									{
										errCode = "VTPONCONF";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									/*}
									else if(errCode == null || errCode.trim().length() == 0)
									{*///COMMENTED BY NANDKUMAR GADKARI ON 03/05/18

									else if (ordDate.after(TranDate)) 
									{
										errCode = "VTRCPDT";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}

						}
						/*if(errCode == null || errCode.trim().length() == 0)
						{*/ //COMMENTED BY NANDKUMAR GADKARI ON 03/05/18
						if (!" P".equals(lspotype) && lsproviid!= null && lsproviid.trim().length() > 0)
						{
							errCode = "VTPROPO";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						/*}
						if(errCode == null || errCode.trim().length() == 0)
						{*///COMMENTED BY NANDKUMAR GADKARI ON 03/05/18
						System.out.println("lspotype : "+lspotype);
						if(lspotype != null)
						{
							lspotype = lspotype.trim();
						}
						System.out.println("lspotype after : "+lspotype);
						if("Q".equals(lspotype) || "H".equals(lspotype))
						{

							tranType = discommon.getDisparams("999999", "ASSET_PORCP_TRAN_TYPE", conn);
							cwipTranType = discommon.getDisparams("999999","ASSET_PORCP_CWIP_TRAN_TYPE", conn);
							porcpTranType = genericUtility.getColumnValue("tran_type", dom);

							if (cwipTranType == null || cwipTranType.trim().length() == 0)
							{
								cwipTranType = "";
							}
							if (tranType == null || tranType.trim().length() == 0)
							{
								tranType = "";
							}

							if (cwipTranType.trim().length() == 0 && tranType.trim().length() == 0)
							{
								errCode = "VMRCPPARM";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());	
							} else
							{
								System.out.println(" porcpTranType is " + porcpTranType.trim() + "tranType is = " + tranType.trim());
								if (!porcpTranType.trim().equalsIgnoreCase(tranType.trim()))
								{
									if (!porcpTranType.trim().equalsIgnoreCase(cwipTranType.trim()))
									{
										errCode = "VTRCPTYPE1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}

						}

						/*}
								if(errCode.trim().length() == 0)
								{*///COMMENTED BY NANDKUMAR GADKARI ON 03/05/18
						sql="select count(*) from porddet where purc_order = ?";
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
							errCode = "VTPODET";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
						}

						/*}

								if(errCode.trim().length() == 0)
								{*/ //COMMENTED BY NANDKUMAR GADKARI ON 03/05/18
						String quantity="",rate="";
						if(tranidPO == null || tranidPO.trim().length() == 0)
						{
							sql= "select count(*) from porcp  where purc_order =? and confirmed = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, purcOrder);
							pstmt.setString(2, "N");
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
								sql="select item_code,(case when quantity is null then 0 else quantity end), " +
										"(case when rate is null then 0 else rate end) " +
										"from 	 porcp,porcpdet" +
										" where ( porcp.tran_id = porcpdet.tran_id ) and  " +
										" porcpdet.purc_order = ? and" +
										"( porcp.confirmed <> 'Y' ) and" +
										"( porcp.tran_id <> ?)";
								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, purcOrder);
								pstmt1.setString(2, tranidPO);
								rs1 = pstmt1.executeQuery();
								while (rs1.next()) 
								{
									lsitemcode=rs1.getString(1);
									quantity=rs1.getString(2);
									rate=rs1.getString(3);
									System.out.println("ItemCOde["+lsitemcode+"]");
									System.out.println("quantity["+quantity+"]");
									System.out.println("rate["+rate+"]");
									if((lsitemcode !=null && lsitemcode.trim().length()>0) && (quantity !=null && quantity.trim().length()>0) && (quantity !=null && quantity.trim().length()>0))
									{
										lsmsg = "Unconfirmed Receipt details are : Item Code  " + lsitemcode + "  Quantity  " + quantity+"  Rate  " + rate+ "\n";
										lsmsg1=lsmsg+lsmsg;

									}
								}
								rs1.close();
								rs1 = null;
								pstmt1.close();
								pstmt1= null;
								System.out.println("lsmsg tran_id["+lsmsg.length()+"]");
								errCode = "VTUREC1"; // item series cannot be blank
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
							/*										System.out.println("Error Fired");
										errCode = "VTUREC1"; // item series cannot be blank
										errList.add( errCode );
										errFields.add( childNodeName.toLowerCase() );*/

						}
						else
						{
							sql= "select count(*) from porcp  where purc_order =? and confirmed = ? and tran_id <> ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, purcOrder);
							pstmt.setString(2, "N");
							pstmt.setString(3, tranidPO	);
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
								sql="select item_code,(case when quantity is null then 0 else quantity end), " +
										"(case when rate is null then 0 else rate end) " +
										"from 	 porcp,porcpdet" +
										" where ( porcp.tran_id = porcpdet.tran_id ) and  " +
										" porcpdet.purc_order = ? and" +
										"( porcp.confirmed <> 'Y' ) and" +
										"( porcp.tran_id <> ?)";
								pstmt1 = conn.prepareStatement(sql);
								pstmt1.setString(1, purcOrder);
								pstmt1.setString(2, tranidPO);
								rs1 = pstmt1.executeQuery();
								while (rs1.next()) 
								{
									lsitemcode=rs1.getString(1);
									quantity=rs1.getString(2);
									rate=rs1.getString(3);														
									//if (lsmsg !=null && lsmsg.trim().length()>0 )
									if((lsitemcode !=null && lsitemcode.trim().length()>0) && (quantity !=null && quantity.trim().length()>0) && (quantity !=null && quantity.trim().length()>0))
									{
										lsmsg += "Unconfirmed Receipt details are : Item Code  " + lsitemcode + "  Quantity  " + quantity+"  Rate  " + rate+ "\n";
									}
								}
								rs1.close();
								rs1 = null;
								pstmt1.close();
								pstmt1 = null;
								errCode = "VTUREC1"; // item series cannot be blank
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
							/*System.out.println("ErrorFired");
											errCode = "VTUREC1"; // item series cannot be blank
											errList.add( errCode );
											errFields.add( childNodeName.toLowerCase() );*/
						}
						System.out.println("purcOrder["+purcOrder+"]");
						System.out.println("siteCode["+siteCode+"]");
						if (tranidPO.trim().length()==0 && purcOrder != null && purcOrder.trim().length() >0) 
						{
							errCode = gbfcheckpodetsite(purcOrder,siteCode,conn);
							System.out.println("ErrorCodein gbfcheckpodetsite["+errCode+"]");
							if("NOTFOUND".equalsIgnoreCase(errCode))
							{
								System.out.println("ErorCode is null");
							}
							else
							{
								System.out.println("Else Add error Code");
								errList.add(errCode);
								errFields.add( childNodeName.toLowerCase() );
							}
						}
						//}//COMMENTED BY NANDKUMAR GADKARI for checking errCode== null condition   ON 03/05/18
					}
					if (childNodeName.equalsIgnoreCase("item_ser")) 
					{
						itemSer = checkNull(genericUtility.getColumnValue("item_ser",dom));
						System.out.println("Item Series--->["+itemSer+"]");

						if(itemSer == null || itemSer.length()==0)
						{
							errCode = "VTITEMSER5";
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						else{
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
								errCode = "VMITEMSER1";
								errList.add( errCode );
								errFields.add( childNodeName.toLowerCase() );
							}
						}

					}
					if (childNodeName.equalsIgnoreCase("supp_code")) 
					{
						String mVal="",mVal1="",lspo="",mval3="";
						mVal = checkNull(genericUtility.getColumnValue("supp_code", dom));
						System.out.println("Supp code in Dom "+mVal);
						mVal1 = checkNull(genericUtility.getColumnValue("site_code", dom)); 
						System.out.println("Supp code in Dom "+mVal1);
						lspo = checkNull(genericUtility.getColumnValue("purc_order", dom));
						System.out.println("Supp code in Dom "+lspo);
						errCode = fincommon.isSupplier(mVal1, mVal,"PORCP", conn);
						System.out.println("Return Supp Code"+errCode);
						if (errCode != null && errCode.trim().length() > 0)
						{
							System.out.println("Inside wfval supp_code errcode>>> "+ errCode);
							/*errString = getErrorString("supp_code",errCode,userId);
							break;*/
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						} 
						else
						{

							if(lspo !=null &&  lspo.trim().length() > 0)
							{
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
								//if(mVal != mval3)
								if(!mVal.trim().equalsIgnoreCase(mval3.trim()))
								{
									errCode="VTPORD4";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									/*errCode = "VTPORD4";
							errString = getErrorString("supp_code",errCode,userId);
							break;*/
								}	
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("gr_no")) 
					{
						String poGno="";
						grNo = checkNull(genericUtility.getColumnValue("gr_no", dom));
						System.out.println("@@@@ gr_no[" + grNo	+ "]");
						tranidGno= genericUtility.getColumnValue("tran_id", dom);
						System.out.println("@@@@ tran_id[" + tranidGno	+ "]");
						poGno = checkNull(genericUtility.getColumnValue("purc_order", dom));
						if( grNo!= null && grNo.trim().length() > 0)
						{
							sql="select count(*) from gate_register where tran_id =?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, grNo);
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
								errCode = "VTNOGR";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());	
							}
							/*if(errCode.trim().length() == 0)
							{*/ //COMMENTED BY NANDKUMAR GADKARI ON 03/05/18
							sql="	select count(*) from porcp where gr_no =? and tran_id <> ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, grNo);
							pstmt.setString(2, tranidGno);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("gr_no--# cnt > 0"+cnt);
							if(cnt > 0)
							{
								//									errCode = "VTGR1";
								//									errString = getErrorString("gr_no",errCode,userId);
								//									break;
								errCode = "VTGR1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());	
							}
							//}

						}
					}
					if (childNodeName.equalsIgnoreCase("curr_code")) 
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
					if (childNodeName.equalsIgnoreCase("tran_code")) 
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
						}
					}
					if (childNodeName.equalsIgnoreCase("dc_no")) 
					{	
						String suppCode="",dcDate="",tranId="";
						int cnt1=0;
						Timestamp dcDate1=null;
						dcNo = checkNull(genericUtility.getColumnValue("dc_no", dom));
						System.out.println("@@@@ dc_no[" + dcNo	+ "]");
						if( dcNo!= null && dcNo.trim().length() > 0)
						{
							suppCode = genericUtility.getColumnValue("supp_code", dom);
							dcDate = genericUtility.getColumnValue("dc_date", dom);
							if (dcDate != null && dcDate.trim().length() > 0) {
								dcDate1 = Timestamp.valueOf(genericUtility
										.getValidDateString(dcDate,
												genericUtility.getApplDateFormat(),
												genericUtility.getDBDateFormat())
										+ " 00:00:00.0");
							}
							tranId = genericUtility.getColumnValue("tran_id", dom);
							if(tranId==null)
							{
								tranId = "@@@@";
							}
							sql="select count(*) from porcp where tran_id <> ? and supp_code = ? and dc_no =? and dc_date = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, tranId);
							pstmt.setString(2, suppCode);
							pstmt.setString(3, dcNo);
							pstmt.setTimestamp(4, dcDate1);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								cnt1 = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("dc_no--# cnt > 0"+cnt1);
							if(cnt1 > 0)
							{
								errCode = "VTDUPDCNO";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								//								errCode = "VTDUPDCNO";
								//								errString = getErrorString("dc_no",errCode,userId);
								//								break;
							}

						}
					}
					if (childNodeName.equalsIgnoreCase("dc_date")) 
					{
						Timestamp dcDate1 = null, recDate1 =null;
						String dcDate="",recDate="";
						dcDate = checkNull(genericUtility.getColumnValue("dc_date", dom));
						System.out.println("@@@@ dcDate[" + dcDate	+ "]");
						recDate = checkNull(genericUtility.getColumnValue("rec_date", dom));
						System.out.println("@@@@ recDate[" + recDate	+ "]");
						if ((dcDate != null && dcDate.trim().length() > 0))

						{
							dcDate1 = Timestamp.valueOf(genericUtility.getValidDateString(dcDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+ " 00:00:00.0");
							recDate1 = Timestamp.valueOf(genericUtility.getValidDateString(recDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+ " 00:00:00.0");
							System.out.println("dcDate1"+dcDate1+".after (recDate1)"+recDate1);
							if (dcDate1.after(recDate1)) 
							{
								errCode = "VTCHEXPDT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("rec_date")) 
					{
						Timestamp trandate1 = null, recDate1 =null;
						String tranDate="",recDate="";
						recDate = checkNull(genericUtility.getColumnValue("rec_date", dom));
						tranDate = checkNull(genericUtility.getColumnValue("tran_date", dom));
						System.out.println("@@@@ recDate[" + recDate	+ "]");
						System.out.println("@@@@ tran_date[" + tranDate	+ "]");
						if ((recDate != null && recDate.trim().length() > 0))

						{
							recDate1 = Timestamp.valueOf(genericUtility.getValidDateString(recDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+ " 00:00:00.0");
							trandate1 = Timestamp.valueOf(genericUtility.getValidDateString(tranDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+ " 00:00:00.0");
							System.out.println("recDate1"+recDate1+".after (trandate1)"+trandate1);
							if (recDate1.after(trandate1)) 
							{
								errCode = "VTCHEXPDT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("frt_amt")) 
					{
						String purcOrderAmt="",frtAmt="",frtAmt1="";
						double qty=0,qty1=0;

						frtAmt = checkNull(genericUtility.getColumnValue("frt_amt", dom));
						frtAmt = frtAmt == null ? "0" : frtAmt.trim();
						purcOrderAmt = checkNull(genericUtility.getColumnValue("purc_order", dom));
						System.out.println("@@@@ frtAmt[" + frtAmt	+ "]");
						System.out.println("@@@@ purcOrderAmt[" + purcOrderAmt	+ "]");
						sql="select	frt_amt from porder where purc_order = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, purcOrderAmt);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							frtAmt1 = checkNull(rs.getString(1));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						frtAmt1 = frtAmt1 == "" ? "0" : frtAmt1.trim();

						if(frtAmt == null || frtAmt.trim().length() == 0)
						{
							frtAmt = "0";
						}
						if(frtAmt1 == null || frtAmt1.trim().length() == 0)
						{
							frtAmt1 = "0";
						}
						System.out.println("@@@@ frtAmt111111[" + frtAmt1	+ "]");
						qty=Double.parseDouble(frtAmt);
						qty1=Double.parseDouble(frtAmt1);
						if(qty > qty1)
						{
							errCode = "VTAMTGREAT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					if (childNodeName.equalsIgnoreCase("reciept_type")) 
					{
						String tranIdref="",recieptType="";
						recieptType = checkNull(genericUtility.getColumnValue("reciept_type", dom));
						tranIdref = checkNull(genericUtility.getColumnValue("tran_id__ref", dom));
						System.out.println("@@@@ recieptType[" + recieptType	+ "]");
						System.out.println("@@@@ tranIdref[" + tranIdref	+ "]");
						if ((recieptType == null || recieptType.trim().length() == 0))
						{
							/*errCode = "VTNOTFND";
							errString = getErrorString("reciept_type",errCode,userId);
							break;*/
							errCode = "VTNOTFND";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						if("R".equals(recieptType))
						{
							if(tranIdref== null || tranIdref.trim().length() == 0) 
							{
								errCode = "VTNOTFND";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else
							{
								sql="select count(*) from porcp where tran_id=? and tran_ser=?";
								pstmt=conn.prepareStatement(sql);
								pstmt.setString(1,tranIdref );
								pstmt.setString(2,"P-RET" );
								rs=pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								System.out.println("@@@@reciept_type--> cnt==0[" + tranIdref	+ "]");
								if(cnt==0)
								{
									errCode = "VTINVRET";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());

								}
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("accept_criteria")) 
					{
						String acceptCriteria="";
						acceptCriteria = genericUtility.getColumnValue("accept_criteria", dom);
						if(acceptCriteria == null || acceptCriteria.trim().length() == 0)
						{
							errCode = "VTNULCRT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());

						}
					}
					if (childNodeName.equalsIgnoreCase("invoice_no")) 
					{
						String invoiceNo="",suppCode="",invoiceDate="",tranSer="",tranId;
						Timestamp invoiceDate1=null;
						invoiceNo = checkNull(genericUtility.getColumnValue("invoice_no", dom));
						System.out.println("@@@@invoiceNo[" + invoiceNo	+ "]");
						if ((invoiceNo != null && invoiceNo.trim().length() > 0))
						{
							suppCode = checkNull(genericUtility.getColumnValue("supp_code", dom));
							invoiceDate = genericUtility.getColumnValue("invoice_date", dom);
							if (invoiceDate != null && invoiceDate.trim().length() > 0) {
								invoiceDate1 = Timestamp.valueOf(genericUtility
										.getValidDateString(invoiceDate,
												genericUtility.getApplDateFormat(),
												genericUtility.getDBDateFormat())
										+ " 00:00:00.0");
							}
							tranSer = checkNull(genericUtility.getColumnValue("tran_ser", dom));
							System.out.println("@@@@suppCode[" + suppCode	+ "]");
							System.out.println("@@@@invoiceDate[" + invoiceDate	+ "]");
							System.out.println("@@@@tranSer[" + tranSer	+ "]");
							if(! "P-RET".equals(tranSer))
							{
								sql="select count(*) from voucher where bill_no = ? and supp_code = ? and bill_date = ?" +
										"and vouch_type <> ? and  confirmed='N'" ;// confirmed= N added by nandkumar gadkari on 13/12/18
								pstmt=conn.prepareStatement(sql);
								pstmt.setString(1,invoiceNo );
								pstmt.setString(2,suppCode );
								pstmt.setTimestamp(3,invoiceDate1 );
								pstmt.setString(4,"A" );
								rs=pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								System.out.println("@@@@invoice_no--> cnt > 0[" + cnt	+ "]");
								if(cnt > 0)
								{

									errCode = "VTBILL1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							tranId = genericUtility.getColumnValue("tran_id", dom);
							if(tranId == null)
							{
								tranId = "@@@@";
							}
							if(errCode.trim().length() == 0)
							{
								sql="select count(*) from porcp where supp_code = ? and invoice_no=? and invoice_date=? and tran_id <> ?";
								pstmt=conn.prepareStatement(sql);
								pstmt.setString(1, suppCode);
								pstmt.setString(2, invoiceNo);
								pstmt.setTimestamp(3, invoiceDate1);
								pstmt.setString(4, tranId);
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
								//}// COMMENTED BY NANDKUMAR GADKARI ON 13/12/18
								else
								{
									sql="select count(*) from misc_payables where sundry_type = ? and sundry_code=? and bill_no=? and bill_date= ? and tran_ser not in ('P-ADV','M-ADV')";
									pstmt=conn.prepareStatement(sql);
									pstmt.setString(1, "S");
									pstmt.setString(2, suppCode);
									pstmt.setString(3, invoiceNo);
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
										sql="select count(*) from  misc_voucher where sundry_code = ? and bill_no = ? and bill_date = ? and sundry_type = ? and  confirmed='N'";// confirmed= N added by nandkumar gadkari on 13/12/18
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
											sql="select count(*) from  misc_vouchdet where sundry_code__for = ? and bill_no = ? and bill_date = ? and sundry_type__for = ?";
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
						}
					}
					//Added by sarita for invoice_date validation on 09JAN2018[start]
					if(childNodeName.equalsIgnoreCase("invoice_date"))
					{
						String poinvoiceDate="",creditTerm="",startFrom="";
						poinvoiceDate = checkNull(genericUtility.getColumnValue("invoice_date", dom));
						creditTerm = checkNull(genericUtility.getColumnValue("cr_term", dom));
						System.out.println("Invoice Date is :::"+poinvoiceDate +"\t"+ "credit term is :::"+creditTerm);						
						sql = "select start_from from crterm where cr_term=?";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1, creditTerm);
						rs=pstmt.executeQuery();
						if(rs.next())
						{
							startFrom = checkNull(rs.getString("start_from"));
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
						if("B".equalsIgnoreCase(startFrom))
						{
							if(poinvoiceDate == null || poinvoiceDate.trim().length() == 0)
							{
								errCode = "VTINVDBLDT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}

					}//end of validation for invoice_date
					//Added by sarita for invoice_date validation on 09JAN2018[end]\

					//Added by monika salla for exch_rate validation on 08NOV2019
					if (childNodeName.equalsIgnoreCase("exch_rate")) 
					{

						exchRateStr = checkNull(genericUtility.getColumnValue("exch_rate", dom));
						System.out.println("@@@@ exch_rate[" + exchRate + "]");
						if (exchRateStr != null && exchRateStr.length() > 0) 
						{
							if (Double.parseDouble(exchRateStr) <= 0) 
							{
								errCode = "VTEXCHRATE";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						else
						{
							errCode = "VTEXCHRATE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}//end
					//Modified by Rohini T on 25/02/2021[Start]
					if (childNodeName.equalsIgnoreCase("channel_partner")) 
					{
						channelPartner = checkNull(genericUtility.getColumnValue("channel_partner", dom));
						System.out.println("@@@@ channelPartner[" + channelPartner + "]");
                        tranMode = checkNull(genericUtility.getColumnValue("tran_mode", dom));
						if(tranMode.trim().length() == 0)
						{
							tranMode = "M";
						}
                        if(!"A".contentEquals(tranMode)){//Added by Anagha R on 22/03/2021 for Error while posting Sales order (If tran_mode is 'A' then this validation should not be done)

						if( "Y".equals(channelPartner ) )
						{ 
							errCode = "VTSUPPCHPT"; //Entered supplier is channel partner manual entry of purchase receipt not allowed.
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
                    }
                }
					//Modified by Rohini T on 25/02/2021[End]
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
					if (childNodeName.equalsIgnoreCase("lot_sl")) 
					{
						String lotSl=(genericUtility.getColumnValue("lot_sl",dom));
						//removed trim function by Varsha V for allowing space
						//if(lotSl == null || lotSl.trim().length() == 0)
						if(lotSl==null || lotSl.length()==0)
						{
							errCode = "VTLOTSL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());

						}
					}
					if (childNodeName.equalsIgnoreCase("purc_order")) 
					{

						String tranId="",tranDate="",tranIdHdr="",acceptCriteria="",suppCode="",confirmed="",suppCodeHdr="",currCodeHdr="",acceptCriteria1="",lsvalue="";

						purcOrder = checkNull(genericUtility.getColumnValue("purc_order",dom));
						System.out.println("Validation Po Dom"+purcOrder.length());
						purcOrder=purcOrder.trim();
						System.out.println("Validation Po Trim"+purcOrder.length());
						tranId =checkNull(genericUtility.getColumnValue("tran_id", dom)); 
						tranDate = checkNull(genericUtility.getColumnValue("tran_date", dom1));
						System.out.println("Tran date in dom "+tranDate);
						if(tranDate.length() > 0)//completion date is not null
						{

							trandatePO1 = Timestamp.valueOf(genericUtility.getValidDateString(tranDate.toString(),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");
							System.out.println("trandatePO1 ====parsed into sql format:::::"+trandatePO1);
						}
						acceptCriteria = checkNull(genericUtility.getColumnValue("accept_criteria", dom1));
						System.out.println("acceptCriteria"+acceptCriteria);

						if (purcOrder != null) 
						{
							sql="select count(*) from porder where purc_order = ?";
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
							if(cnt == 0)
							{
								errCode = "VTPORD3";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							}
							else
							{
								sql="select supp_code, curr_code,status,emp_code__aprv,ord_date," +
										"(case when confirmed is null then 'N' else confirmed end)," +
										"accept_criteria from porder where purc_order = ?";
								pstmt=conn.prepareStatement(sql);
								pstmt.setString(1, purcOrder);
								rs=pstmt.executeQuery();
								if(rs.next())
								{
									suppCode=rs.getString(1);
									currCode=rs.getString(2);
									status=rs.getString(3);
									empCodeaprv=rs.getString(4);
									ordDate=rs.getTimestamp(5);
									confirmed=rs.getString(6);
									acceptCriteria1=rs.getString(7);

								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								System.out.println("ordDate ====parsed into sql format:::::"+ordDate);
								suppCodeHdr = checkNull(genericUtility.getColumnValue("supp_code", dom1)); 
								currCodeHdr= checkNull(genericUtility.getColumnValue("curr_code", dom1));
								System.out.println("suppCodeHdr"+suppCodeHdr);
								System.out.println("currCodeHdr"+currCodeHdr);
								if(! suppCode.trim().equals(suppCodeHdr.trim()))
								{
									errCode = "VTPORD4";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());

								}
								else if(!currCode.trim().equals(currCodeHdr.trim()))
								{
									errCode = "VTPORD5";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());

								}
								else if("C".equals(status) || "X".equals(status))
								{
									errCode = "VTPORD2";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());

								}
								else if(status == null || status.trim().length()==0)
								{
									errCode = "VTPONAPRV";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());

								}
								else if("N".equals(confirmed)|| confirmed==null)
								{
									errCode = "VTPONCONF";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());

								}
								else if(!acceptCriteria.trim().equals(acceptCriteria1.trim()))
								{
									errCode = "VTINVCRI";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());

								}
								else if("O".equals(status))
								{
									if(empCodeaprv==null ||empCodeaprv.trim().length()==0)
									{
										errCode = "VTPONAPRV";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());

									}
								}
								else if(!acceptCriteria.trim().equals(acceptCriteria1.trim()))
								{
									errCode = "VTINVCRI";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());

								}
								else if (ordDate.before(trandatePO1)) 
								{
									errCode = "VTRCPDT";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());

								}
							}
							lsvalue = discommon.getDisparams("999999", "UNCONF_RCP", conn);
							if("NULLFOUND".equals(lsvalue))
							{
								errCode = "VTUOMVARPARM";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());


							}
							/*if( errCode.trim().length() ==0  &&  "Y".equals(lsvalue))*/ //changes in condition BY NANDKUMAR GADKARI ON 03/05/18
							if( "Y".equals(lsvalue))
							{
								tranIdHdr =checkNull(genericUtility.getColumnValue("tran_id", dom1)); 
								sql="select count(*) from porcp,porcpdet " +
										"where ( porcp.tran_id = porcpdet.tran_id ) " +
										"and  ( porcpdet.purc_order = ? ) " +
										"and ( porcp.confirmed = ? ) and " +
										"( porcp.tran_id <> ?)     ";
								pstmt=conn.prepareStatement(sql);
								pstmt.setString(1,purcOrder);
								pstmt.setString(2,"N" );
								pstmt.setString(3, tranIdHdr);
								rs=pstmt.executeQuery();
								if(rs.next())
								{
									cnt=rs.getInt(1);
								}
								rs.close();
								rs=null;
								pstmt.close();
								pstmt = null;
								if(cnt > 0)
								{
									tranIdHdr =checkNull(genericUtility.getColumnValue("tran_id", dom1)); 
									sql="select item_code,(case when quantity is null then 0 else quantity end), (case when rate is null then 0 else rate end)" +
											"from porcp , " +
											"porcpdet where  porcp.tran_id = porcpdet.tran_id " +
											"and  	 porcpdet.purc_order = ?" +
											"and	 porcp.confirmed    <> ? " +
											"and	 porcp.tran_id 	  <> ?";
									pstmt=conn.prepareStatement(sql);
									pstmt.setString(1, purcOrder);
									pstmt.setString(2, "Y");
									pstmt.setString(3, tranIdHdr);
									rs=pstmt.executeQuery();
									while(rs.next())
									{
										lsitemcode=rs.getString(1);
										llquantity=rs.getDouble(2);
										llrate=rs.getDouble(3);
									}
									rs.close();
									rs=null;
									pstmt.close();
									pstmt = null;
								}
							}


						}
					}//end purchase order case
					if(childNodeName.equalsIgnoreCase("line_no__ord"))
					{
						String POlineNo="",site="";
						lineNoOrd = checkNull(genericUtility.getColumnValue("line_no__ord", dom));
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom1));
						if (lineNoOrd != null && lineNoOrd.length() > 0) 
						{
							if ((lineNoOrd.trim().equalsIgnoreCase("0")))
							{
								errCode = "VTINVPOLIN";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							} 
							else
							{
								purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom));
								lineNoOrd = "    " + lineNoOrd;
								lineNoOrd = lineNoOrd.substring(lineNoOrd.length() - 3,lineNoOrd.length());

								sql = "select site_code from porddet where purc_order = ?and  line_no = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, purcOrder);
								pstmt.setString(2, lineNoOrd);
								rs = pstmt.executeQuery();
								if (rs.next()) 
								{
									//Change By PriyankaC on 30JAN2018..[START]
									//site = rs.getString("site_code");
									site = checkNullandTrim(rs.getString("site_code"));
									//Change By PriyankaC on 30JAN2018..[END]

								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(site == null || site.trim().length() == 0)
								{
									errCode = "VTORDDT1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());

								}
								//if (!(siteCode).trim().equalsIgnoreCase(site)) //Change By PriyankaC on 30JAN2018..
								if (!checkNullandTrim(siteCode).equalsIgnoreCase(site.trim())) 
								{
									errCode = "VTPORD6";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
					}
					if (childNodeName.equalsIgnoreCase("item_code")) 
					{
						String trandateStr="",suppCode="",itemCode="",lineNoItem="";
						itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom1));
						trandateStr = checkNull(genericUtility.getColumnValue("tran_date", dom1));
						itemSer = checkNull(genericUtility.getColumnValue("item_ser", dom));
						suppCode = checkNull(genericUtility.getColumnValue("supp_code", dom));
						int llexists=0;
						String lsstkopt="",lsqcreqd="",mstatus2="",channelpartner="",sitecodech="";
						purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom));
						lineNoItem = checkNull(genericUtility.getColumnValue("line_no", dom));

						//ADDED BY MONIKA-15-JULY-2019
						lineNoOrd = checkNull(genericUtility.getColumnValue("line_no__ord", dom));
						//Added by Anagha R on 14-07-2020 for VISION - Purchase Receipt Voucher Alteration Error
						lineNoOrd = "    " + lineNoOrd;
						lineNoOrd = lineNoOrd.substring(lineNoOrd.length() - 3,lineNoOrd.length());
						//Added by Anagha R on 14-07-2020 for VISION - Purchase Receipt Voucher Alteration Error
						System.out.println("purcOrder"+purcOrder);
						if(purcOrder != null && purcOrder.trim().length() >0 )
						{
							//COMMENTED BY MONIKA ON -15 JULY 2019
							//sql="select count(*) from porddet where purc_order=? and item_code=? and line_no =?";
							//CHANGES MADE BY MONIKA-ADDED LINE_NO-ON -15 JULY 2019..
							sql="select count(*) from porddet where purc_order=? and item_code=? and line_no =?";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1, purcOrder);
							pstmt.setString(2, itemCode);
							//ADDED BY MONIKA-15-JULY-2019
							pstmt.setString(3, lineNoOrd);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								llexists = rs.getInt(1);
							}
							System.out.println("llexists@@"+llexists);
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(llexists == 0)
							{
								//errCode = "INVITEM";//Commented by Anagha R on 14-07-2020 for Purchase Receipt Voucher Alteration Error
								errCode = "VTPOLINEIT";//Added by Anagha R on 14-07-2020 for Purchase Receipt Voucher Alteration Error 
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							}

						}

						/*if ( errCode == null || errCode.trim().length()==0)
						{*/ //COMMENTED BY NANDKUMAR GADKARI ON 03/05/18

						lsqcreqd = gfautoqcreqd(siteCode,itemCode,conn);
						sql="select case when stk_opt is null then '0' else stk_opt end from item where item_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							lsstkopt = rs.getString(1);
						}
						System.out.println("lsstkopt@@"+lsstkopt);
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if("Y".equalsIgnoreCase(lsqcreqd) &&  !"2".equals(lsstkopt))
						{
							errCode = "VTSTKOPT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());

						}

						/*}
						if((errCode == null || errCode.trim().length()==0))
						{*/ //COMMENTED BY NANDKUMAR GADKARI ON 03/05/18

						if (( purcOrder == null || purcOrder.trim().length()==0) && "N".equals(lsqcreqd))
						{
							errCode = "VTPORD7";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());

						}

						/*}
						if ( errCode == null || errCode.trim().length()==0)
						{*/ //COMMENTED BY NANDKUMAR GADKARI ON 03/05/18

						//Commented by Anagha R on 14-07-2020 for Purchase Receipt Voucher Alteration Error START
						/*	purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom));
						lineNoItem = "    " + lineNoItem;
						lineNoItem = lineNoItem.substring(lineNoItem.length() - 3,lineNoItem.length());
						if(lineNoItem== null || lineNoItem.trim().length() == 0)
						{
							sql="select count(1) from porddet where  purc_order   = ? and line_no =? and  item_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, purcOrder);
							pstmt.setString(2, lineNoItem);
							pstmt.setString(3, itemCode);	
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt(1);

							}
							System.out.println("cnt OF PO-->"+cnt);
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(cnt==0)
							{
								errCode = "VTPOLINEIT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							}
						}
						 */
						//Commented by Anagha R on 14-07-2020 for Purchase Receipt Voucher Alteration Error END

						//}
						/*if ( errCode == null || errCode.trim().length()==0)
						{*/ //COMMENTED BY NANDKUMAR GADKARI ON 03/05/18

						//	errCode = nvo_dis.gbf_item(mval1,mval,transer)
						/*if (errCode.trim().length()==0)
							{*/ //COMMENTED BY NANDKUMAR GADKARI ON 03/05/18

						sql="select oth_series  from itemser where item_ser = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemSer);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							mstatus2 = rs.getString("oth_series");

						}
						System.out.println("cnt OF PO-->"+cnt);
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if("N".equalsIgnoreCase(mstatus2))
						{
							sql="select channel_partner,site_code__ch from site_supplier where site_code = ? and supp_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, siteCode);
							pstmt.setString(2, suppCode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								channelpartner = rs.getString("channel_partner");
								sitecodech = rs.getString("site_code__ch");

							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(channelpartner== null)
							{
								sql="select channel_partner,site_code from supplier where supp_code = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, suppCode);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									channelpartner = rs.getString("channel_partner");
									sitecodech = rs.getString("site_code__ch");

								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
							/*if (errCode.trim().length() == 0)
									{*///COMMENTED BY NANDKUMAR GADKARI ON 03/05/18

							if("Y".equalsIgnoreCase(channelpartner))
							{
								siteCode = sitecodech;
							}
							/*}
									if (errCode.trim().length()==0)
									{*/
							itemSer = itmDBAccess.getItemSeries(itemCode, siteCode,trandateStr,suppCode,'S',null);
							//}//COMMENTED BY NANDKUMAR GADKARI ON 03/05/18

						}
						//}
						/*}
						if (errCode == null || errCode.trim().length()==0)
						{*/ //COMMENTED BY NANDKUMAR GADKARI ON 03/05/18

						String lsinvacct="",lsloccode="",lslotno="",lslotsl="",lsacctcodeporcp="",lscctrcodeporcp="",lsacctcodedr="",lscctrcodedr="";
						lsinvacct = fincommon.getFinparams("999999", "INVENTORY_ACCT", conn);
						if ( "Y".equalsIgnoreCase(lsinvacct)) 
						{
							lsloccode = checkNull(genericUtility.getColumnValue("loc_code", dom));
							lslotno = checkNull(genericUtility.getColumnValue("lot_no", dom));
							lslotsl =checkNull(genericUtility.getColumnValue("lot_sl", dom));
							lsacctcodeporcp = checkNull(genericUtility.getColumnValue("acct_code__dr", dom));
							lscctrcodeporcp=checkNull(genericUtility.getColumnValue("cctr_code__dr", dom)); 
							sql="select count(*) from stock where item_code=? and site_code=? and loc_code=? and lot_no=? and lot_sl=?";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);
							pstmt.setString(2, siteCode);
							pstmt.setString(3, lsloccode);
							pstmt.setString(4, lslotno);
							pstmt.setString(5, lslotsl);
							rs=pstmt.executeQuery();
							if(rs.next())
							{
								cnt=rs.getInt(1);
							}
							rs.close();
							rs=null;
							pstmt.close();
							rs=null;
							if(cnt== 1)
							{
								sql="select (case when acct_code__inv is null then '    ' else acct_code__inv end), " +
										"(case when cctr_code__inv is null then '    ' else cctr_code__inv end) " +
										" from stock where item_code =? and site_code = ? and loc_code  = ? and lot_no 	 = ? and lot_sl 	 = ? ";
								pstmt=conn.prepareStatement(sql);
								pstmt.setString(1, itemCode);
								pstmt.setString(2, siteCode);
								pstmt.setString(3, lsloccode);
								pstmt.setString(4, lslotno);
								pstmt.setString(5, lslotsl);
								rs=pstmt.executeQuery();
								if(rs.next())
								{
									// changed by nasruddin check null and trim 9-12-16 start 
									lsacctcodedr=checkNull(rs.getString(1)).trim();
									lscctrcodedr=checkNull(rs.getString(2)).trim();
									// changed by nasruddin check null and trim 9-12-16 End 

								}
								rs.close();
								rs=null;
								pstmt.close();
								rs=null;
								/*if(errCode == null || errCode.trim().length() == 0)  
									{*/ //COMMENTED BY NANDKUMAR GADKARI ON 03/05/18

								System.out.println("lsacctcodedr"+lsacctcodedr);
								System.out.println("lsacctcodeporcp"+lsacctcodeporcp);
								System.out.println("lscctrcodedr"+lscctrcodedr);
								System.out.println("lscctrcodeporcp"+lscctrcodeporcp);
								if((!lsacctcodedr.equalsIgnoreCase(lsacctcodeporcp) )|| (!lscctrcodedr.equalsIgnoreCase(lscctrcodeporcp)))
								{
									errCode = "VTACTMIS";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());

								}
								//}
							} 
						}

						//}//COMMENTED BY NANDKUMAR GADKARI ON 03/05/18

					}//end item code case childNodeName.equalsIgnoreCase("line_no__ord")
					if (childNodeName.equalsIgnoreCase("quantity")) 
					{
						System.out.println("Validation Quantity");
						String quantity="",purcorder="",qtybrowStr="",lcvariencevalue1="",linenoord="",itemcode="",lineno="",tranid="",temppono="",templinenoord="",templineno="";
						double qty=0,lcrcpqty=0,qtybrow=0,currqty=0,lcdlvqty=0,lcordqty=0,mqty2=0,mperc=0,ldqtytol=0,lcvariencevalue=0;
						double lcqty=0 ,lcqtystd =0,lcrate=0,cratestd =0,lcvalue=0,lcvolume=0;
						String  lcqty1="" ,lcqtystd1 ="",lcrate1="",cratestd1 ="";
						String lsunit="",lsunitstd="",lsunitrate="",lsvariencetype="",lslorryno="";
						int noOfParent = 0;
						lsunit =checkNull(genericUtility.getColumnValue("unit", dom));    //added by manish mhatre on 16-dec-2019
						quantity = checkNull(genericUtility.getColumnValue("quantity",dom));
						System.out.println("quantity"+quantity);


						qty= (quantity == null || quantity.trim().length() == 0) ? 0 :Double.parseDouble(quantity);
						System.out.println("Quantity double parse"+qty);
						if(qty <= 0)
						{
							errCode = "VTQTY18";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());

						}
						else
						{

							purcorder =genericUtility.getColumnValue("purc_order",dom);
							linenoord =genericUtility.getColumnValue("line_no__ord", dom);
							linenoord = "    " + linenoord;
							linenoord = linenoord.substring(linenoord.length() - 3,linenoord.length());
							itemcode =genericUtility.getColumnValue("item_code", dom);
							lineno =genericUtility.getColumnValue("line_no", dom);
							lineno = "    " + lineno;
							lineno = lineno.substring(lineno.length() - 3,lineno.length());
							tranid = checkNull(genericUtility.getColumnValue("tran_id", dom));
							if(purcorder != null && purcorder.trim().length() >0)
							{
								sql="select sum(case when a.quantity is null then 0 else a.quantity end) from  " +
										" porcpdet a, porcp b where a.tran_id = b.tran_id and 	" +
										"(case when b.confirmed is null then 'N' else b.confirmed end) <> 'Y' and  " +
										" a.purc_order = ? and 	a.tran_id <> ? and	a.line_no__ord = ?";
								pstmt=conn.prepareStatement(sql);
								pstmt.setString(1, purcorder);
								pstmt.setString(2, tranid);
								pstmt.setString(3, linenoord);
								rs=pstmt.executeQuery();
								if(rs.next())
								{
									lcrcpqty=rs.getDouble(1);
								}
								rs.close();
								rs=null;
								pstmt.close();
								rs=null;
								NodeList detail2ListTemp = dom2.getElementsByTagName("Detail2");
								NodeList detail1ListTemp = dom2.getElementsByTagName("Detail1");
								ArrayList linenoords = new ArrayList();
								noOfParent = detail2ListTemp.getLength();
								System.out.println("@@@@@@noOfParent["+ noOfParent + "]");
								for (int k = 0; k < noOfParent; k++) 
								{
									temppono = genericUtility.getColumnValueFromNode("purc_order",detail2ListTemp.item(k));
									System.out.println("@@@@@@temppono["+ temppono + "]");
									templinenoord = genericUtility.getColumnValueFromNode("line_no__ord",detail2ListTemp.item(k));
									System.out.println("@@@@@@templinenoord["+ templinenoord + "]");
									templineno = genericUtility.getColumnValueFromNode("line_no",detail2ListTemp.item(k));
									System.out.println("@@@@@@templineno["+ templineno + "]");

									//System.out.println("@@@@@tempindno["+ temppono+ "]::templinenoord["+ templinenoord	+ "]::templineno["+ templineno+ "]");
									if (templinenoord != null && templinenoord.length() > 0) 
									{
										linenoords.add(linenoords.size(),templinenoord);
									}
									System.out.println("@@@@@temppono["+ temppono+ "]::templinenoord["+ templinenoord	+ "]::templineno["+ templineno+ "]");
									System.out.println("@@@@@temppono["+ temppono+ "]::linenoord["+ linenoord	+ "]::lineno["+ lineno+ "]");
									//Modified by Anjali R. on[15/12/2017][Start][To add quantity if purchase order receipt line number same as current line number.]
									/*if(purcorder.equalsIgnoreCase(temppono) && linenoord.equalsIgnoreCase(templinenoord) && lineno.equalsIgnoreCase(templineno) )
									{
									System.out.println("VTTRKQTY Skip");
									}else
									{
										qtybrowStr =checkNull( genericUtility.getColumnValueFromNode("quantity",detail2ListTemp.item(k)));
										qtybrow = (qtybrowStr == null || qtybrowStr.trim().length() == 0 )? 0: Double.parseDouble(qtybrowStr);
										currqty = currqty + qtybrow;
									}*/
									if( (lineno.trim()).equalsIgnoreCase(templineno.trim()) )
									{
										System.out.println("VTTRKQTY Skip for current line no.");//quantity skip due to current order receipt line number same as current row line number.
									}
									else
									{
										if( (purcorder.trim()).equalsIgnoreCase(temppono.trim()) && (linenoord.trim()).equalsIgnoreCase(templinenoord.trim()) )
										{
											qtybrowStr =checkNull( genericUtility.getColumnValueFromNode("quantity",detail2ListTemp.item(k)));
											qtybrow = (qtybrowStr == null || qtybrowStr.trim().length() == 0 )? 0: Double.parseDouble(qtybrowStr);
											currqty = currqty + qtybrow;
										}
										else
										{
											System.out.println("VTTRKQTY Skip different PO/Order Line no");//quantity skip due to different purchase order receipt line number and current row line number.
										}
									}
									//Modified by Anjali R. on[15/12/2017][End][To add quantity if purchase order receipt line number same as current line number.]

								}
								lcrcpqty	=	lcrcpqty + currqty;
								System.out.println("lcrcpqty@@@@@@"+lcrcpqty);
								System.out.println("currqty@@@@@@"+currqty);
								sql="select quantity,(case when dlv_qty is null then 0 else dlv_qty end)  from porddet where purc_order = ? and  line_no    = ?";
								pstmt=conn.prepareStatement(sql);
								pstmt.setString(1, purcorder);
								pstmt.setString(2, linenoord);
								rs=pstmt.executeQuery();
								if(rs.next())
								{
									lcordqty=rs.getDouble(1);
									lcdlvqty=rs.getDouble(2);
								}
								rs.close();
								rs=null;
								pstmt.close();
								rs=null;
								System.out.println("lcordqty@@@@@@"+lcordqty);
								System.out.println("lcdlvqty@@@@@@"+currqty);
								System.out.println("lcrcpqty@@@@@@"+lcrcpqty);
								mqty2 = lcdlvqty + qty + lcrcpqty;
								System.out.println("mqty2@@@@@@"+mqty2);
								//if (lcordqty > 0) // 31-may-19 manoharan commented as always quantity to be compared with PO pending quantity
								//{
								if (mqty2 > lcordqty )
								{
									sql="select (case when ordc_perc is null then 0 else ordc_perc end), " +
											"(case when qty_tol_perc is null then 0 else qty_tol_perc end) " +
											"from 	 item where  item_code = ?";
									pstmt=conn.prepareStatement(sql);
									pstmt.setString(1, itemcode);	
									rs=pstmt.executeQuery();
									if(rs.next())
									{
										mperc=rs.getDouble(1);
										ldqtytol=rs.getDouble(2);
									}
									rs.close();
									rs=null;
									pstmt.close();
									rs=null;
									double totalQty=0;
									System.out.println("mqty2 "+mqty2+"- lcordqty"+lcordqty+") / lcordqty"+lcordqty+") * 100  > ldqtytol"+ldqtytol);
									totalQty= ( ((mqty2 - lcordqty) / lcordqty) * 100 );
									System.out.println("totalQty"+totalQty);
									if ( totalQty > ldqtytol )
									{
										errCode = "VTPOQTY2";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());

									}
								}
								//}

							}

							//added by manish mhatre on 16-dec-2019[for allow 3 decimal places]
							//start manish
							errCode=distcommon.checkDecimal(qty, lsunit, conn);
							if(errCode!=null && errCode.trim().length()>0)
							{
								errCode = "VTUOMDEC3";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}  //end manish	
						}
						/*	if isnull(ls_errcode) or len(trim(ls_errcode)) = 0 then 
						ls_errcode=gbf_val_tolerance()
					end if*/

						lcqty1 = checkNull(genericUtility.getColumnValue("quantity", dom));
						lcqty= (lcqty1 == null || lcqty1.trim().length() == 0) ? 0 :Double.parseDouble(lcqty1);
						lcqtystd1 =checkNull(genericUtility.getColumnValue("quantity__stduom", dom));
						lcqtystd= (lcqtystd1 == null || lcqtystd1.trim().length() == 0) ? 0 :Double.parseDouble(lcqtystd1);
						lcrate1 = checkNull(genericUtility.getColumnValue("rate", dom));

						lcrate= (lcrate1 == null || lcrate1.trim().length() == 0) ? 0 :Double.parseDouble(lcrate1);

						cratestd1 =checkNull(genericUtility.getColumnValue("rate__stduom", dom));

						cratestd= (cratestd1 == null || cratestd1.trim().length() == 0) ? 0 : Double.parseDouble(cratestd1);
						lsunit =checkNull(genericUtility.getColumnValue("unit", dom));
						lsunitstd =checkNull(genericUtility.getColumnValue("unit__std", dom));
						lsunitrate =checkNull(genericUtility.getColumnValue("unit__rate", dom));
						lsunit= lsunit == null ? " " : lsunit.trim();
						lsunitrate= lsunitrate== null ? " " :  lsunitrate.trim();
						System.out.println("Unit STd is"+lsunit);
						System.out.println("Unit Rate is"+lsunitrate);
						System.out.println("@@@@@@ lcqty "+lcqty);
						System.out.println("@@@@@@ lcqtystd1 "+lcqtystd);
						System.out.println("@@@@@@ lcrate1 "+lcrate);
						System.out.println("@@@@@@ cratestd1 "+cratestd);

						sql="select varience_value , varience_type  from uomconv where unit__fr = ? and unit__to = ? ";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1, lsunit);	
						pstmt.setString(2, lsunitstd);	
						rs=pstmt.executeQuery();
						if(rs.next())
						{

							lcvariencevalue1=checkNull(rs.getString(1));
							lsvariencetype=checkNull(rs.getString(2));
						}
						rs.close();
						rs=null;
						pstmt.close();
						rs=null;
						System.out.println("lcvariencevalue is"+lcvariencevalue);
						System.out.println("lsvariencetype is"+lsvariencetype);
						if (  "P".equalsIgnoreCase(lsvariencetype)) 
						{
							lcvalue =  (lcqty * lcrate )/ 100;
							System.out.println("(lcqty "+lcqty+"* lcrate"+lcrate+ ")");
							System.out.println("lcvalue is"+lcvalue);
						}
						else 
						{
							System.out.println("lcvariencevalue1"+lcvariencevalue1.trim().length());
							System.out.println("lcvariencevalue1"+lcvariencevalue1);

							lcvariencevalue=(lcvariencevalue1==null|| lcvariencevalue1.trim().length()==0) ? 0 : Double.parseDouble(lcvariencevalue1);
							lcvalue = lcvariencevalue;	
							System.out.println("lcvariencevalue"+lcvariencevalue);
						}

						if (lsunitrate.equalsIgnoreCase(lsunit))
						{
							double ChkAbs=0,qtyRate=0,stdQtyRate=0;

							qtyRate=lcqty * lcrate;
							stdQtyRate=lcqtystd * cratestd;
							System.out.println("qtyRate"+qtyRate);
							System.out.println("stdQtyRate"+stdQtyRate);
							ChkAbs=Math.abs(qtyRate - stdQtyRate);
							System.out.println("ChkAbs"+ChkAbs);
							if (ChkAbs > lcvalue) 
							{
								errCode = "VTCONV";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());


							}

						}
						String lcvolume1="";
						lslorryno = genericUtility.getColumnValue("lorry_no", dom1);
						System.out.println("lslorryno"+lslorryno);
						if(lslorryno == null || lslorryno.trim().length()==0)
						{
							lslorryno = " ";
						}

						sql="select volume from vehicle where vehicle_no = ?";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1, lslorryno);	
						rs=pstmt.executeQuery();
						if(rs.next())
						{

							lcvolume1=checkNull(rs.getString(1));

						}
						rs.close();
						rs=null;
						pstmt.close();
						rs=null;

						if(lcvolume1 != null && lcvolume1.trim().length()>0)
						{
							lcvolume= Double.parseDouble(lcvolume1);
							if( qty > lcvolume )
							{
								errCode = "VTTRKQTY";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							}

						}

						sql="select pord_type from porder where purc_order = ?";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1, purcorder);	
						rs=pstmt.executeQuery();
						if(rs.next())
						{

							lspotype=checkNull(rs.getString(1));

						}
						//Add by Ajay on 18/04/18:START
						rs.close();
						rs=null;
						pstmt.close();
						rs=null;
						//END
						if( "Q".equalsIgnoreCase(lspotype.trim()) || "H".equalsIgnoreCase(lspotype.trim())	) 
						{

							tranType = discommon.getDisparams("999999", "ASSET_PORCP_TRAN_TYPE", conn);
							porcpTranType = genericUtility.getColumnValue("tran_type", dom);
							if("NULLFOUND".equalsIgnoreCase(tranType) || tranType.trim().length()==0)
							{
								tranType=" ";
							}
							if(porcpTranType==null || porcpTranType.trim().length() == 0 )
							{
								porcpTranType=" ";
							}
							if(tranType.equals(porcpTranType))
							{
								if (qty > 1 )
								{
									errCode = "VTRCPQTY";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());

								}
							}
						}
					}// end quantity
					if (childNodeName.equalsIgnoreCase("unit")) 
					{
						String unit="",unitStd="",lcconvqtystduom1="";
						double lcconvqtystduom=0;
						unit = genericUtility.getColumnValue("unit", dom);
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
							// 25/08/00 manoharan checking should be done if
							// conv__qty_stduom = 0

							lcconvqtystduom1 = genericUtility.getColumnValue("conv__qty_stduom", dom);
							if(lcconvqtystduom1 != null&& lcconvqtystduom1.trim().length() > 0)
							{
								lcconvqtystduom = Double.parseDouble(lcconvqtystduom1);
							}

							if(lcconvqtystduom == 0)
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
								System.out.println("cnt**********-->"+cnt);
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(cnt ==0)
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
									System.out.println("cnt**********-->"+cnt);
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									if(cnt ==0)
									{
										errCode = "VTUNIT3";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());

									}
								}
							}
						}
						/*if(errCode == null || errCode.trim().length()==0)
						{*/ //COMMENTED BY NANDKUMAR GADKARI ON 03/05/18

						String lstranid="", lslinenorcp="", lsloccode="",lsstkopt="";
						lsitemcode = genericUtility.getColumnValue("item_code", dom);  
						lstranid = genericUtility.getColumnValue("tran_id", dom1); 
						lslinenorcp = genericUtility.getColumnValue("line_no", dom);
						lsloccode = genericUtility.getColumnValue("loc_code", dom);
						sql="select (case when stk_opt is null then '0' else stk_opt end) from item where item_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsitemcode);		
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							lsstkopt = rs.getString(1);

						}
						System.out.println("lsstkopt**********-->"+lsstkopt);
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if ("1".equalsIgnoreCase(lsstkopt))
						{
							lslinenorcp = "    " + lslinenorcp;
							lslinenorcp = lslinenorcp.substring(lslinenorcp.length() - 3,lslinenorcp.length());
							sql="select count(*) from porcpdet where tran_id = ? " +
									"and line_no <> ? " +
									"and unit <> ? " +
									"and item_code = ?" +
									" and loc_code = ?	";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, lstranid);
							pstmt.setString(2, lslinenorcp);		
							pstmt.setString(3, unit);		
							pstmt.setString(4, lsitemcode);		
							pstmt.setString(5, lsloccode);		
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt(1);

							}
							System.out.println("cnt UNit**********-->"+cnt);
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if( cnt > 0 )
							{
								errCode = "VTUNIT4";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							}
						}
						//}//COMMENTED BY NANDKUMAR GADKARI ON 03/05/18

					}//end unit
					if (childNodeName.equalsIgnoreCase("conv__qty_stduom")) 
					{

						String unit="",unitStd="",convQtyStduomStr="",itemCode="";
						double convQtyStduom=0;
						System.out.println("@@@@@ validation of conv__qty_stduom executed......");
						convQtyStduomStr = checkNull(genericUtility.getColumnValue("conv__qty_stduom", dom));
						unit = checkNull(genericUtility.getColumnValue("unit",dom));
						unitStd = checkNull(genericUtility.getColumnValue("unit__std", dom));
						if(convQtyStduomStr != null && convQtyStduomStr.trim().length() > 0)
						{
							convQtyStduom = convQtyStduomStr == null ? 0 : Double.parseDouble(convQtyStduomStr);
						}

						// Added by Mahesh Saggam on 04/07/2019 Start [if conv_qty__stduom is 1 then quantiy and quantity__stduom should be same]

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

						// Added by Mahesh Saggam on 04/07/2019 end

						if (unitStd == null || unitStd.length() == 0) 
						{
							itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
							unit = setDescription("unit", "item", "item_code",itemCode, conn);
						}
						unit = unit == null ? "" : unit.trim();
						unitStd = unitStd == null ? "" : unitStd.trim();
						System.out.println("@@@@@3 unit[" + unit+ "]::unitStd[" + unitStd + "]::convQtyStduom["
								+ convQtyStduom + "]");
						if ((unit.equalsIgnoreCase(unitStd))&& (convQtyStduom != 1)) 
						{
							errCode = "VTUCON1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());

						}
						else if (!(unit.equalsIgnoreCase(unitStd))) 
						{
							itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
							// errcode = gf_check_conv_fact(mitemcode, mval,
							// mval1, lc_conv,conn);
							errCode = gf_check_conv_fact(itemCode, unit,unitStd, convQtyStduom, conn);
							if (errCode != null && errCode.trim().length() > 0)
							{
								//errCode = "VTUCON1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());;
							}
						}

					}// end case conv__qty_stduom
					if (childNodeName.equalsIgnoreCase("conv__rtuom_stduom")) //
					{
						String unit="",unitStd="",convRtuomStduomStr="",itemCode="",lcconvqtystduom1="";
						double convRtuomStduom=0;
						String rateStr = "", ratestdStr = "";  // added by mahesh saggam on 04/07/2019
						double pordRate = 0, stdRate = 0;
						System.out.println("@@@@@ validation of conv__rtuom_stduom executed......");
						convRtuomStduomStr = checkNull(genericUtility.getColumnValue("conv__rtuom_stduom", dom));
						unit = checkNull(genericUtility.getColumnValue("unit__rate", dom));
						unitStd = checkNull(genericUtility.getColumnValue("unit__std", dom));

						if(convRtuomStduomStr != null && convRtuomStduomStr.trim().length() > 0)
						{
							convRtuomStduom = convRtuomStduomStr == null ? 0: Double.parseDouble(convRtuomStduomStr);
						}

						// Added by Mahesh Saggam on 04/07/2019 Start [if conv_rate__stduom is 1 then rate and rate__stduom should be same]

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

						// Added by Mahesh Saggam on 04/07/2019 end

						if (unitStd == null || unitStd.length() == 0) 
						{
							itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
							unit = setDescription("unit", "item", "item_code",itemCode, conn);
						}
						unit = unit == null ? "" : unit.trim();
						unitStd = unitStd == null ? "" : unitStd.trim();
						System.out.println("@@@@@1 unitRate[" + unit+ "]::unitStd[" + unitStd + "]::convRtuomStduom["
								+ convRtuomStduom + "]");
						if ((unit.equalsIgnoreCase(unitStd)) && (convRtuomStduom != 1)) 
						{
							errCode = "VTUCON1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());

						} else if (!(unit.equalsIgnoreCase(unitStd)))
						{
							itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
							// errcode = gf_check_conv_fact(mitemcode, mval,
							// mval1, lc_conv,conn);
							errCode = gf_check_conv_fact(itemCode, unitStd,unit, convRtuomStduom, conn);
							if (errCode != null && errCode.trim().length() > 0)
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}// end case convRtuomStduom
					if (childNodeName.equalsIgnoreCase("unit__rate"))
					{
						String unitrate="",unitstd="",lcconvqtystduom1="",lsstkopt="";
						double lcconvqtystduom=0;
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
							lcconvqtystduom1 = checkNull(genericUtility.getColumnValue("conv__rtuom_stduom", dom));

							if(lcconvqtystduom1 == null ||  lcconvqtystduom1.trim().length() == 0)
							{
								lcconvqtystduom1 = "0" ;
							}
							lcconvqtystduom=Double.parseDouble(lcconvqtystduom1);
							if(lcconvqtystduom == 0)
							{
								sql="select count(*) from uomconv where unit__fr = ? and unit__to = ?"	;
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, lsitemcode);	
								pstmt.setString(2, lsitemcode);
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

					}// end unit rate
					if(childNodeName.equalsIgnoreCase("loc_code"))
					{
						String msite="",lsstkopt="",lsqcreqd="",itemCode="",lsfaciloccode="",lsfacisitecode="";
						int cnt1=0,cnt2=0,cnt3=0,cnt4;
						loccode=genericUtility.getColumnValue("loc_code", dom);
						lsitemcode=genericUtility.getColumnValue("item_code", dom);
						msite=genericUtility.getColumnValue("site_code", dom1);
						System.out.println("loccode"+loccode);
						System.out.println("lsitemcode"+lsitemcode);//ibase3-webitm-dis5-12-116-1
						System.out.println("msite"+msite);
						sql="select stk_opt  from item where item_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsitemcode);		
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							lsstkopt = rs.getString(1);

						}
						if(lsstkopt == null || lsstkopt.trim().length() == 0)
						{
							lsstkopt="0";
						}
						System.out.println("lsstkopt**********-->"+lsstkopt);
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if( loccode != null && loccode.trim().length()> 0)
						{
							sql="select count(*)  from location where loc_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, loccode);	
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt1 = rs.getInt(1);

							}
							System.out.println("CNt loccode **********-->"+cnt1);
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if (cnt1 == 0)
							{
								errCode = "VMLOC1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							}
						}
						else if(!"0".equalsIgnoreCase(lsstkopt))
						{
							System.out.println("In If Else COndition");
							errCode = "VMLOC1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());;
						}
						/*if (errCode== null ||  errCode.trim().length() == 0)
				    	{*/ //COMMENTED BY NANDKUMAR GADKARI ON 03/05/18

						System.out.println("Is Error COde is Null");
						System.out.println("lsstkopt"+lsstkopt);
						lsqcreqd=genericUtility.getColumnValue("qc_reqd", dom1);
						System.out.println("lsqcreqd"+lsqcreqd);
						if (lsqcreqd== null) 
						{
							lsqcreqd = "Y";
						}
						if ("Y".equalsIgnoreCase(lsqcreqd))
						{  System.out.println("Inside If qc req Y>>>");
						itemCode=genericUtility.getColumnValue("item_code", dom);
						//Added by Varsha V on 01-06-18 for reassigning value
						lsqcreqd = "";
						//Ended by Varsha V on 01-06-18 for reassigning value
						sql="select qc_reqd  from siteitem where item_code = ? and site_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemCode);	
						pstmt.setString(2, msite);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							lsqcreqd = rs.getString(1);

						}
						System.out.println("lsqcreqde**********-->"+lsqcreqd);
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if( lsqcreqd == null || lsqcreqd.trim().length()==0)
						{
							sql="Select (case when qc_reqd is null then 'N' else qc_reqd end) from item where item_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemCode);	
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								lsqcreqd = rs.getString(1);

							}
							System.out.println("lsqcreqde**********-->"+lsqcreqd);
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						if("Y".equalsIgnoreCase(lsqcreqd))
						{
							sql="select count(*) from   location a , " +
									" invstat b where  a.inv_stat  = b.inv_stat and 	 b.available = 'N'" +
									"  and 	 a.loc_code  = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, loccode);	
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt1 = rs.getInt(1);

							}
							System.out.println("lsqcreqd(Y) cnt**********-->"+cnt1);
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(cnt1 == 0)
							{
								errCode = "VTLOCSL";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							}
						}
						System.out.println("lsqcreqd**********-->["+lsqcreqd+"]");
						if("N".equalsIgnoreCase(lsqcreqd))
						{

							//added by manish mhatre on 14-aug-20[If qc required N then the system allow to store inventory in non available location]
							//start manish
							purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom));

							sql="select pord_type from porder where purc_order= ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, purcOrder);	
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								purcorderType=rs.getString("pord_type");

							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							sql="select allow_unavail_loc from pordertype where order_type= ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, purcorderType);	
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								allowUnavailLoc=rs.getString("allow_unavail_loc");

							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							System.out.println("allow_unavail_loc-->"+allowUnavailLoc+" purcorderType"+purcorderType);
							//end manish

							sql="select count(*) from " +
									"  location a,invstat b where" +
									"  a.inv_stat  = b.inv_stat and 	 b.available = 'Y' and 	 a.loc_code  = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, loccode);	
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt2 = rs.getInt(1);

							}
							System.out.println("lsqcreqd(N) cnt in Lc required Yes**********-->"+cnt2);
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							//if(cnt2 == 0)           //commented by manish mhatre on 17-aug-20
							if(cnt2==0 && (!"Y".equalsIgnoreCase(allowUnavailLoc)))  //added by manish mhatre on 14-aug-20
							{
								//errCode = "VTLOCSL";    //commented by manish mhatre on 17-aug-20
								errCode="VTLOCSLN";       //added by manish mhatre on 17-aug-20
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());


							}
						}
						}
						else
						{   System.out.println("Inside If qc req N>>>");
						lsitemcode=genericUtility.getColumnValue("item_code", dom);         
						cnt = 0;
						//added by manish mhatre on 14-aug-20[If qc required N then the system allow to store inventory in non available location]
						//start manish
						purcOrder = checkNull(genericUtility.getColumnValue("purc_order", dom));

						sql="select pord_type from porder where purc_order= ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, purcOrder);	
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							purcorderType=rs.getString("pord_type");

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						sql="select allow_unavail_loc from pordertype where order_type= ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, purcorderType);	
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							allowUnavailLoc=rs.getString("allow_unavail_loc");

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("allow_unavail_loc-->"+allowUnavailLoc+" purcorderType"+purcorderType);
						//end manish

						sql="select count(*) from location a,invstat b where a.inv_stat  = b.inv_stat and   " +
								" b.available = 'Y' and  a.loc_code  = ?";//added from keyword in sql query
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, loccode);	
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							cnt = rs.getInt(1);

						}
						System.out.println("lsqcreqd(N) cnt**********-->"+cnt);
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						//if(cnt==0)      //commented by manish mhatre on 14-aug-20
						if(cnt==0 && (!"Y".equalsIgnoreCase(allowUnavailLoc)))  //added by manish mhatre on 14-aug-20
						{
							//errCode = "VTLOCSL";   //commented by manish mhatre on 14-aug-20
							errCode = "VTLOCSLN";   //added by manish mhatre on 17-aug-20
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());

						}
						}
						/*}
				    		 if (errCode == null || errCode.trim().length() == 0)
				    		 {*/ //COMMENTED BY NANDKUMAR GADKARI ON 03/05/18

						sql="select facility_code from location where loc_code=?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, loccode);	
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							lsfaciloccode = rs.getString(1);

						}
						System.out.println("facility_code from location**********-->["+lsfaciloccode+"]");
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						sql="select facility_code from site where site_code=?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, msite);	
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							lsfacisitecode = rs.getString(1);

						}
						System.out.println("facility_code from site**********-->["+lsfaciloccode+"]");
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						//}//COMMENTED BY NANDKUMAR GADKARI ON 03/05/18

						if ((lsfaciloccode !=null && lsfaciloccode.trim().length() >0) && (lsfacisitecode !=null && lsfacisitecode.trim().length() >0))
						{
							if(!lsfaciloccode.equalsIgnoreCase(lsfacisitecode))
							{
								errCode = "VMFACI2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							}
						}

					}//end case loc code
					if (childNodeName.equalsIgnoreCase("lot_no")) 
					{

						String lotno="",lotnoType="",itemcode="",lsstkopt="", suppCode = "", useSuppLot = "";
						//ADDED BY MUKESH CHAUHAN ON 05/07/19 START
						String lsitemser="", lsgenlotauto="",lsqcreqd="",lsautogeneratelotno="",lsdis="",lssite="";
						//END
						lotno = genericUtility.getColumnValue("lot_no", dom);
						lotnoType = discommon.getDisparams("999999", "PO_RECEIPT_LOT", conn);
						System.out.println("lotnoType>>>>>>>>>>>"+lotnoType);

						itemcode = genericUtility.getColumnValue("item_code",dom);
						lsautogeneratelotno =discommon.getDisparams("999999","GENERATE_LOT_NO_AUTO", conn);
						sitecode = checkNull(genericUtility.getColumnValue("site_code", dom1));
						suppCode = checkNull(genericUtility.getColumnValue("supp_code", dom1));
						loccode  = genericUtility.getColumnValue("loc_code",dom);

						sql="select ITEM_SER  from item  where item_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, itemcode);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							lsitemser = rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						sql="Select auto_gen_lot  from itemser where item_ser = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lsitemser);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							lsgenlotauto = rs.getString(1);
							System.out.println("Lot auto_gen>>>>>>>>>>>...."+lsgenlotauto);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if(!"A".equalsIgnoreCase(lotnoType) || "N".equalsIgnoreCase(lsgenlotauto))
						{
							if(lotno == null || lotno.trim().length() == 0)     //END
							{
								sql="select stk_opt from item where item_code = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, itemcode);		
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									lsstkopt = rs.getString(1);

								}
								System.out.println("lsstkopt**********-->"+lsstkopt);
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if("2".equalsIgnoreCase(lsstkopt))
								{
									errCode = "VTLOTEMPTY";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							//else commented by nandkumar gadkari on 18/07/19
							/*else
							{

								sql="select count(*) from stock where site_code = ? " +
										"and item_code = ? and loc_code = ?  and lot_no =?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, sitecode);		
								pstmt.setString(2, itemcode);
								pstmt.setString(3, loccode);
								pstmt.setString(4, lotno);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									cnt = rs.getInt(1);

								}
								System.out.println("COunt lot number**********-->"+lsstkopt);
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(cnt >= 1)
								{
									errCode = "VTNEWLOT";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}	
							}*/
						}//Added else block by Varsha V on 30-10-18 if stkopt is 2 and use_supplier_lot is Y the lot_no is mandatory
						else
						{

							if(lotno == null || lotno.trim().length() == 0)
							{
								lsstkopt = "";
								sql="select stk_opt from item where item_code = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, itemcode);		
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									lsstkopt = rs.getString(1);

								}
								System.out.println("lsstkopt**********-->"+lsstkopt);
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if("2".equalsIgnoreCase(lsstkopt))
								{
									useSuppLot = "";
									sql = "SELECT (CASE WHEN USE_SUPPLIER_LOT IS NULL THEN 'N' ELSE USE_SUPPLIER_LOT END) AS USE_SUPPLIER_LOT FROM SUPPLIER WHERE SUPP_CODE= ?";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, suppCode);
									rs = pstmt.executeQuery();
									if (rs.next())
									{
										useSuppLot = checkNull(rs.getString("USE_SUPPLIER_LOT"));
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									System.out.println(">>>>>>>>>>>>>useSuppLot:" + useSuppLot);
									if("Y".equalsIgnoreCase(useSuppLot))
									{
										errCode = "VTLOTEMPTY";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
							if(!"N".equalsIgnoreCase(lsgenlotauto))//ADDED BY MUKESH CHAUHAN ON 05/07/19 START
							{


								sql="select qc_reqd from siteitem where item_code = ? and site_code = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, itemcode);
								pstmt.setString(2, sitecode);
								rs = pstmt.executeQuery();
								if (rs.next()) 
								{
									lsqcreqd = rs.getString(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if(lsqcreqd == null || lsqcreqd.trim().length() == 0)
								{
									sql="select (case when qc_reqd is null then 'N' else qc_reqd end)  from item where item_code = ?";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, itemcode);
									rs = pstmt.executeQuery();
									if (rs.next()) 
									{
										lsqcreqd = rs.getString(1);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								}
								if(lsqcreqd == null || lsqcreqd.trim().length() == 0)
								{
									lsqcreqd="N";
								}
								if("M".equalsIgnoreCase(lsautogeneratelotno))
								{

									if(!"Y".equalsIgnoreCase(lsqcreqd))
									{
										if(lotno == null || lotno.trim().length() == 0)    
										{
											itemcode = genericUtility.getColumnValue("item_code",dom);
											sql="select stk_opt from item where item_code = ?";
											pstmt = conn.prepareStatement(sql);
											pstmt.setString(1, itemcode);		
											rs = pstmt.executeQuery();
											if (rs.next())
											{
												lsstkopt = rs.getString(1);

											}
											System.out.println("lsstkopt**********-->"+lsstkopt);
											rs.close();
											rs = null;
											pstmt.close();
											pstmt = null;
											if("2".equalsIgnoreCase(lsstkopt))
											{
												errCode = "VTLOTEMPTY";
												errList.add(errCode);
												errFields.add(childNodeName.toLowerCase());
											}
										}
									}
								}
								else// else part added by nandkumar gadkari on 18/07/19
								{
									if("Y".equalsIgnoreCase(lsqcreqd))
									{
										if(!"Y".equalsIgnoreCase(lsautogeneratelotno))
										{

											if(lotno == null || lotno.trim().length() == 0)    
											{
												itemcode = genericUtility.getColumnValue("item_code",dom);
												sql="select stk_opt from item where item_code = ?";
												pstmt = conn.prepareStatement(sql);
												pstmt.setString(1, itemcode);		
												rs = pstmt.executeQuery();
												if (rs.next())
												{
													lsstkopt = rs.getString(1);

												}
												System.out.println("lsstkopt**********-->"+lsstkopt);
												rs.close();
												rs = null;
												pstmt.close();
												pstmt = null;
												if("2".equalsIgnoreCase(lsstkopt))
												{
													errCode = "VTLOTEMPTY";
													errList.add(errCode);
													errFields.add(childNodeName.toLowerCase());
												}
											}
										}
									}
								}
							} //END
						}//Ended else block by Varsha V on 30-10-18 if stkopt is 2 and use_supplier_lot is Y the lot_no is mandatory
					}// end lot no case
					if (childNodeName.equalsIgnoreCase("acct_code__dr")) 
					{
						String acctCodeDr="";
						acctCodeDr = genericUtility.getColumnValue("acct_code__dr", dom);
						siteCode = genericUtility.getColumnValue("site_code",dom1);
						System.out.println("@@@@@ acctCodeDr [" + acctCodeDr+ "]");
						if (acctCodeDr != null && acctCodeDr.length() > 0) 
						{
							errCode = fincommon.isAcctCode(siteCode,acctCodeDr, modName, conn);
							System.out.println("errCode acct"+errCode);
							if (errCode != null && errCode.trim().length() > 0) 
							{
								//errCode = "VTNEWLOT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							}
						} 
					}
					if (childNodeName.equalsIgnoreCase("acct_code__cr")) 
					{
						String acctCodeCr="";
						acctCodeCr = genericUtility.getColumnValue("acct_code__cr", dom);
						siteCode = genericUtility.getColumnValue("site_code",dom1);
						System.out.println("@@@@@ acctCodeDr [" + acctCodeCr+ "]");
						if (acctCodeCr != null && acctCodeCr.length() > 0) 
						{
							errCode = fincommon.isAcctCode(siteCode,acctCodeCr, modName, conn);
							System.out.println("errCode acct cr"+errCode);
							if (errCode != null && errCode.trim().length() > 0) 
							{
								//errCode = "VTNEWLOT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						} 
					}
					if (childNodeName.equalsIgnoreCase("cctr_code__dr")) 
					{

						String cctrCodeDr="",acctCodeDr="";
						cctrCodeDr = genericUtility.getColumnValue("cctr_code__dr", dom);
						System.out.println("@@@@@ cctrCodeDr [" + cctrCodeDr+ "]");
						if (cctrCodeDr != null && cctrCodeDr.length() > 0) 
						{
							acctCodeDr = genericUtility.getColumnValue("acct_code__dr", dom);
							// errCode = this.isCctrCode(acctCodeDr, cctrCodeDr,
							// modName);
							System.out.println("@@@@@ cctrCodeDr acctCodeDr ["+ acctCodeDr + "]");
							errCode = fincommon.isCctrCode(acctCodeDr,cctrCodeDr, modName, conn);
							if (errCode != null && errCode.trim().length() > 0)
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

						}
					}
					if (childNodeName.equalsIgnoreCase("cctr_code__cr")) 
					{
						String cctrCodeCr="",acctCodeCr="";
						cctrCodeCr = genericUtility.getColumnValue("cctr_code__cr", dom);
						System.out.println("@@@@@ cctrCodeCr [" + cctrCodeCr+ "]");
						if (cctrCodeCr != null && cctrCodeCr.length() > 0) 
						{
							acctCodeCr = genericUtility.getColumnValue("acct_code__cr", dom);
							// errCode = this.isCctrCode(acctCodeCr, cctrCodeCr,
							// modName);
							System.out.println("@@@@@ cctrCodeCr acctCodeCr ["+ acctCodeCr + "]");
							errCode = fincommon.isCctrCode(acctCodeCr,cctrCodeCr, modName, conn);
							if (errCode != null && errCode.trim().length() > 0)
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						} 

					}
					if(childNodeName.equalsIgnoreCase("dept_code"))
					{
						String deptCode="",acctCode="",acctSpec="";
						deptCode = genericUtility.getColumnValue("dept_code", dom); 
						acctCode = genericUtility.getColumnValue("acct_code", dom); 
						if(deptCode != null && (deptCode.trim().length() > 0 ))
						{
							sql = "SELECT COUNT(1) FROM department WHERE dept_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,deptCode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if (cnt == 0 )
							{
								//errCode = "VMDEPT1";
								errCode = "VMDEPT1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else
							{
								sql = "SELECT VAR_VALUE FROM FINPARM WHERE PRD_CODE = ? AND VAR_NAME = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1,"999999");
								pstmt.setString (2, "ACCT_SPECIFIC_DEPT");
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									acctSpec = rs.getString(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(acctSpec != null && "Y".equalsIgnoreCase(acctSpec.trim().toUpperCase()))
								{
									sql = "SELECT COUNT(1) FROM accounts_dept WHERE acct_code = ?" + "AND dept_code = ?";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1,acctCode);
									pstmt.setString(2,deptCode);
									rs = pstmt.executeQuery();
									if (rs.next())
									{
										cnt = rs.getInt(1);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									if (cnt == 0 )
									{
										sql = "SELECT COUNT(1) FROM accounts_dept WHERE acct_code = ?";
										pstmt =  conn.prepareStatement(sql);
										pstmt.setString(1,acctCode);
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
											if(deptCode != null && deptCode.trim().length() > 0)
											{
												//errCode = "VMDEPT2";
												errCode = "VMDEPT2";
												errList.add(errCode);
												errFields.add(childNodeName.toLowerCase());
											}
										}
										else if(cnt > 0)
										{
											errCode = "VMDEPT2";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
									}
								}
							}								 
						}
					}
					if(childNodeName.equalsIgnoreCase("pack_code"))
					{
						String packcode="";
						packcode =genericUtility.getColumnValue("pack_code", dom);  
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
						}
					}
					if(childNodeName.equalsIgnoreCase("site_code__mfg"))
					{
						String sitecodemfg="",itemcode="",suppsour="";
						sitecodemfg=checkNull(genericUtility.getColumnValue("site_code__mfg",dom));
						itemcode=genericUtility.getColumnValue("item_code", dom);
						sitecode=genericUtility.getColumnValue("site_code", dom1);
						//Added By PriyankaC on 19july2019.[Start]
						System.out.println("sitecodemfg"+sitecodemfg);
						if(sitecodemfg != null && sitecodemfg.trim().length() > 0)
						{
							System.out.println("sitecodemfg@@"+sitecodemfg);
							errCode = this.isSiteCode(sitecodemfg, modName);
							System.out.println("errCode@@"+errCode);
							if(errCode!=null && errCode.trim().length()>0)
							{
								System.out.println("errCode@if@"+errCode);
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						//Added By PriyankaC on 19july2019.[END]
						sql="select supp_sour from   siteitem where  site_code = ? and item_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,sitecode);
						pstmt.setString(2,itemcode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							//changed By Nasruddin [28-10-16]
							//suppsour = rs.getString(1);
							suppsour = checkNull(rs.getString(1));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						System.out.println("suppsour ["+ suppsour +"]");
						//changed By Nasruddin [28-10-16]
						// suppsour=suppsour.trim();
						if(suppsour == null || suppsour.trim().length()==0)
						{
							sql="select (case when supp_sour is null then 'P' else supp_sour end) from   item where  item_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,itemcode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								suppsour = rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						if("M".equalsIgnoreCase(suppsour))
						{
							errCode = this.isSiteCode(sitecodemfg, modName);
							System.out.println("SiteCode Error code is"+errCode);
							if (errCode != null && errCode.trim().length() > 0) 
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}//end case site_code__mfg
					if (childNodeName.equalsIgnoreCase("rate")) 
					{
						String rateStr="",purcorder="",linenoord="",pricelist="",lslisttype="",lsporateoption="";
						double rate=0,lcrate1=0,lcratevarperc=0,lcratevarval=0;
						rateStr = checkNull(genericUtility.getColumnValue("rate", dom));

						rateStr= (rateStr == null ||  rateStr.trim().length() == 0) ? "0": rateStr ;
						rate= Double.parseDouble(rateStr);
						purcorder = checkNull(genericUtility.getColumnValue("purc_order", dom));
						linenoord = checkNull(genericUtility.getColumnValue("line_no__ord", dom));
						linenoord = "    " + linenoord;
						linenoord = linenoord.substring(linenoord.length() - 3, linenoord.length());
						//Commented by Ajay Jadhav on 03/04/18:START
						/*  if(rate ==0)
						  {
							  errCode = "VTINVRATE3";											
							  errList.add( errCode );
							  errFields.add( childNodeName.toLowerCase() );

						  }
						  else*/
						//Commented by Ajay Jadhav on 03/04/18:END

						if(rate<0)
						{
							errCode = "VTINVRATE2";											
							errList.add( errCode );
							errFields.add( childNodeName.toLowerCase() );
						}
						else if(purcorder !=null && purcorder.trim().length() >0)
						{
							sql="select (case when rate is null then 0 else rate end) from  porddet where  purc_order = ? and 	 line_no 	= ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,purcorder);
							pstmt.setString(2,linenoord);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								lcrate1 = rs.getDouble(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(lcrate1 == 0)
							{
								sql="select price_list from porder where purc_order = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1,purcorder);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									pricelist = rs.getString(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(pricelist != null && pricelist.trim().length() >0) 	
								{
									sql="select list_type  from pricelist where price_list = ?";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1,pricelist);
									rs = pstmt.executeQuery();
									if (rs.next())
									{
										lslisttype = rs.getString(1);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									if("L".equalsIgnoreCase(lslisttype))
									{
										if(rate != lcrate1)
										{
											errCode = "VTRATE4";											
											errList.add( errCode );
											errFields.add( childNodeName.toLowerCase() );

										}
									}
								}

							}
							if( lcrate1 != 0)
							{
								/*if(errCode== null || errCode.trim().length() == 0)
						    	{*///COMMENTED BY NANDKUMAR GADKARI ON 03/05/18

								lsitemcode =genericUtility.getColumnValue("item_code", dom);
								sql="select (case when po_rate_option is null then 'N' else po_rate_option end)," +
										"(case when po_rate_varience is null then 0 else po_rate_varience end) " +
										" from item where item_code = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1,lsitemcode);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									lsporateoption = rs.getString(1);
									lcratevarperc = rs.getDouble(2);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if("N".equals(lsporateoption))
								{
									if (rate != lcrate1 )
									{
										errCode = "VTRATE4";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());;
									}

								}
								else if("L".equals(lsporateoption))
								{
									if (rate != lcrate1 )
									{
										errCode = "VTRATE4";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								/*if( errCode== null || errCode.trim().length() == 0) 
						    		{*/ //COMMENTED BY NANDKUMAR GADKARI ON 03/05/18

								if( rate != lcrate1) 
								{
									lcratevarval = lcrate1 * lcratevarperc / 100;
									if (Math.abs(lcratevarval) < Math.abs(lcrate1 - rate) )
									{
										errCode = "VTRTVAR";											
										errList.add( errCode );
										errFields.add( childNodeName.toLowerCase() );

									}
								}
								//}
								//}//COMMENTED BY NANDKUMAR GADKARI ON 03/05/18

							}
						}
					}//END CASE RATE
					if (childNodeName.equalsIgnoreCase("quantity__stduom")) 
					{
						String quantityStduomStr="";
						quantityStduomStr = checkNull(genericUtility.getColumnValue("quantity__stduom", dom));

						if ("-999999999".equalsIgnoreCase(quantityStduomStr))
						{
							errCode = "VTPOQTY3";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());

						}
					}//END CASE QUNTITY STDUM
					if (childNodeName.equalsIgnoreCase("rate__stduom")) 
					{
						String rateStduomStr="";
						rateStduomStr = checkNull(genericUtility.getColumnValue("rate__stduom", dom));
						if ("-999999999".equalsIgnoreCase(rateStduomStr)) 
						{
							errCode = "VTPORATE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());

						}
					}
					if (childNodeName.equalsIgnoreCase("no_art"))
					{
						String noart="",itemcode="",lsstkopt="";
						double noArt=0;
						noart = checkNull(genericUtility.getColumnValue("no_art", dom));


						noArt = (noart == null ||  noart.trim().length() == 0) ? 0 : Double.parseDouble(noart);

						//Added by Anagha R on 23/12/2020 for SYNCOM Reciept transaction vlidation for qty_per art can not be zero START
						if(noArt == 0){
							errCode = "UVNOARTNZ";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						//Added by Anagha R on 23/12/2020 for SYNCOM Reciept transaction vlidation for qty_per art can not be zero END

						itemcode = checkNull(genericUtility.getColumnValue("item_code", dom));
						sql="select stk_opt from item where item_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,itemcode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							lsstkopt = rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if("2".equalsIgnoreCase(lsstkopt))
						{
							if(noArt <= 0 )
							{
								errCode = "VTNARTNULL";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}// end case no art
					if (childNodeName.equalsIgnoreCase("expiry_date"))
					{
						String expirydate="",mdretestdt="",mdate2="",itemcode="",lsqcreqd="",lsautoreqc="",lstrackshelflife="";

						//Modified by Anjali R. on [22/11/2018][Start]
						String mfgDate = "";
						java.sql.Timestamp mfgDt = null;
						java.sql.Timestamp expDt = null;
						//Modified by Anjali R. on [22/11/2018][End]

						expirydate=checkNull(genericUtility.getColumnValue("expiry_date", dom));	
						mdretestdt=genericUtility.getColumnValue("retest_date", dom);
						mdate2=checkNull(genericUtility.getColumnValue("rec_date", dom1));
						itemcode=genericUtility.getColumnValue("item_code", dom);
						sitecode=genericUtility.getColumnValue("site_code", dom1);

						//Modified by Anjali R. on [22/11/2018][Exp_date should not be less than mfg_date][Start]
						mfgDate = checkNull(genericUtility.getColumnValue("mfg_date", dom));
						if((mfgDate != null && mfgDate.trim().length() > 0) && (expirydate != null && expirydate.trim().length() > 0))
						{
							mfgDt = discommon.getTimeStamp(mfgDate);
							expDt = discommon.getTimeStamp(expirydate);
							if(expDt.before(mfgDt))
							{
								errCode = "VTMFGEXPDT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						//Modified by Anjali R. on [22/11/2018][Exp_date should not be less than mfg_date][End]

						lsqcreqd = gfqcreqd(sitecode,itemcode,conn);
						lsautoreqc = gfautoqcreqd(sitecode,itemcode,conn);
						sql= "select (case when track_shelf_life is null then 'Y' else track_shelf_life end) from item where item_code = ? ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,itemcode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							lstrackshelflife = rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if("Y".equalsIgnoreCase(lstrackshelflife))
						{
							if(expirydate == null || expirydate.trim().length() == 0)
							{
								errCode = "VTEXPDTSHL";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else
							{
								if(mdate2 == null || mdate2.trim().length() == 0)

								{
									errCode = "VMPDTNUL1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								else
								{
									Date1 = Timestamp.valueOf(genericUtility.getValidDateString(expirydate, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
									Date2 = Timestamp.valueOf(genericUtility.getValidDateString(mdate2, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
									System.out.println("comapring expirydate>>>>>>>>"+Date1);
									System.out.println("comapring permitdate>>>>>>>>"+Date2);
									if(Date1!=null && Date2!=null && Date2.after(Date1))
									{
										errCode = "VTCHEXPDT";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());

									}
								}


							}
						}
						else if("N".equalsIgnoreCase(lstrackshelflife) &&  "Y".equalsIgnoreCase(lsqcreqd) && "Y".equalsIgnoreCase(lsautoreqc) )
						{
							if(mdretestdt== null || mdretestdt.trim().length() == 0)
							{
								errCode = "VTRETSTDT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							}

						}
					}//end
					if(childNodeName.equalsIgnoreCase("mfg_date"))
					{
						String mdate1="",itemcode="",lstrackshelflife="";
						String mtrandateStr = null;
						mtrandateStr = genericUtility.getColumnValue( "tran_date", dom1);
						mdate1=genericUtility.getColumnValue("mfg_date", dom);
						itemcode=genericUtility.getColumnValue("item_code", dom);
						sql="select (case when track_shelf_life is null then 'Y' else track_shelf_life end) from item where item_code = ? ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,itemcode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							lstrackshelflife = rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if("Y".equalsIgnoreCase(lstrackshelflife))
						{
							if(mdate1== null || mdate1.trim().length() == 0)
							{
								errCode = "VTMFGDATE4";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							}//Added by Anagha R on 15/01/2020 for validation rquired on posting or despatch making START
							String stkOpt = "";
							sql = "select (case when stk_opt is null then '0' else stk_opt end) as stk_opt from item where item_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, itemcode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								stkOpt = checkNull(rs.getString("stk_opt")).trim();
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							System.out.println("Stk_opt:: "+stkOpt);
							if(mdate1 != null && mdate1.trim().length()> 0 && stkOpt != "0")
							{
								Date date1 = sdf.parse(mdate1);
								Date date2 = sdf.parse(mtrandateStr);
								System.out.println("mfg_date:: "+mdate1+"Today:: "+mtrandateStr);
								if(date1.compareTo(date2)>0){
									System.out.println("mfg date could not be greater than current date");
									errCode = "VTMFGDATE6";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}	
							}//Added by Anagha R on 15/01/2020 for validation rquired on posting or despatch making END
						}
					}// mfg_date
					if(childNodeName.equalsIgnoreCase("batch_no"))
					{
						String lsbatchno="",itemcode="",lstemp = "",lstranid="",lslineno="",lsrcpline="",lspurcorder="";
						lsbatchno  =checkNull(genericUtility.getColumnValue("batch_no", dom));
						itemcode  =checkNull(genericUtility.getColumnValue("item_code", dom));
						lstranid  =checkNull(genericUtility.getColumnValue("tran_id", dom1));
						lslineno  =checkNull(genericUtility.getColumnValue("line_no__ord", dom));
						lsrcpline  =checkNull(genericUtility.getColumnValue("line_no", dom));
						lspurcorder  =checkNull(genericUtility.getColumnValue("purc_order", dom));
						if(lsbatchno== null || lsbatchno.trim().length() == 0)
						{
							sql="select	count(*) from porcpdet where tran_id = ? and purc_order = ? and " +
									" item_code = ? and length(case when batch_no is null then '' else batch_no end) = 0 " +
									"and line_no__ord = ? and line_no <>?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,lstranid);
							pstmt.setString(2,lspurcorder);
							pstmt.setString(3,itemcode);
							pstmt.setString(4,lslineno);
							pstmt.setString(5,lsrcpline);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

						}
						else
						{
							sql="select	count(*) from porcpdet where tran_id = ? and purc_order = ? and" +
									" item_code = ? and  batch_no = ?" +
									"and line_no__ord = ? and line_no <> ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,lstranid);
							pstmt.setString(2,lspurcorder);
							pstmt.setString(3,itemcode);
							pstmt.setString(4,lsbatchno.trim());
							pstmt.setString(5,lslineno);
							pstmt.setString(6,lsrcpline);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						if(cnt > 0)
						{
							errCode = "VTDUPBATCH";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());

						}
						if(lsbatchno!=null && lsbatchno.trim().length()>0)
						{
							lslineno = "    " + lslineno;
							lslineno = lslineno.substring(lslineno.length() - 3, lslineno.length()); 
							sql="select count(*) from porcpdet where" +
									" tran_id = ?" +
									" and item_code <> ? " +
									"and batch_no = ? " +
									"and line_no <> ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,lstranid);
							pstmt.setString(2,itemcode);
							pstmt.setString(3,lsbatchno);
							pstmt.setString(4,lslineno);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						if(cnt > 0)
						{
							errCode = "VTDUPBATCH";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}

					}// end case 
					if(childNodeName.equalsIgnoreCase("loc_code__excess_short"))
					{
						String loccodeexcessshort="",lcqty="";
						double qty=0;
						loccodeexcessshort =genericUtility.getColumnValue("loc_code__excess_short", dom);     
						if(loccodeexcessshort == null || loccodeexcessshort.trim().length() == 0)
						{
							lcqty = checkNull(genericUtility.getColumnValue("excess_short_qty", dom));

							if(lcqty == null ||  lcqty.trim().length() == 0)
							{
								lcqty = "0" ;
							}
							qty=Double.parseDouble(lcqty);
							if(qty != 0)
							{
								errCode = "VTLOCEX";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							}
						}
						else
						{
							sql="Select count(*)  from location where loc_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,loccodeexcessshort);
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
								errCode = "VMLOC1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							}
						}
					}// end case
					if(childNodeName.equalsIgnoreCase("excess_short_qty"))
					{
						String excessshortqty="",itemcode="",lcquantity="";
						double excessshortqty1=0,quantity=0,lcqtytolperc=0,lcqtytol=0;
						excessshortqty = genericUtility.getColumnValue("excess_short_qty", dom); 

						if(excessshortqty == null ||  excessshortqty.trim().length() == 0)
						{
							excessshortqty = "0" ;
						}
						excessshortqty1=Double.parseDouble(excessshortqty);
						itemcode =genericUtility.getColumnValue("item_code", dom);       
						lcquantity = checkNull(genericUtility.getColumnValue("quantity", dom));

						if(lcquantity == null ||  lcquantity.trim().length() == 0)
						{
							lcquantity = "0" ;
						}
						quantity= lcquantity == null ? 0 : Double.parseDouble(lcquantity);
						if(quantity !=0)
						{
							sql="select case when rcp_tol_perc is null then 0 else rcp_tol_perc end from item where item_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,itemcode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								lcqtytolperc = rs.getDouble(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							lcqtytol = quantity * lcqtytolperc / 100;
							if( Math.abs(excessshortqty1) > Math.abs(lcqtytol) )	
							{
								errCode = "VTPKQTY";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							}

						}

					}// end case
					if(childNodeName.equalsIgnoreCase("acct_code__prov_dr"))
					{
						String acctcodprovdr="",postprov="";
						acctcodprovdr=genericUtility.getColumnValue("acct_code__prov_dr", dom);
						sitecode=genericUtility.getColumnValue("site_code", dom1);
						postprov=genericUtility.getColumnValue("post_prov", dom);
						if("Y".equalsIgnoreCase(postprov))
						{
							errCode=fincommon.isAcctCode(sitecode, acctcodprovdr, modName, conn);
							if(errCode == null || errCode.trim().length()==0)
							{
								errCode=fincommon.isAcctType(acctcodprovdr, "", "O", conn);
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							}
							else
							{
								errCode = "VTPROVACCT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());


							}

						}
					}// end case acct_code__prov_cr
					if(childNodeName.equalsIgnoreCase("acct_code__prov_cr"))
					{
						String acctcodprovcr="",postprov="";
						acctcodprovcr=genericUtility.getColumnValue("acct_code__prov_cr", dom);
						sitecode=genericUtility.getColumnValue("site_code", dom1);
						postprov=genericUtility.getColumnValue("post_prov", dom);
						if("Y".equalsIgnoreCase(postprov))
						{
							errCode=fincommon.isAcctCode(sitecode, acctcodprovcr, modName, conn);
							if(errCode == null || errCode.trim().length()==0)
							{
								errCode=fincommon.isAcctType(acctcodprovcr, "", "O", conn);
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							}
							else
							{
								errCode = "VTPROVACCT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

						}
					}// end case acct_code__prov_cr
					if(childNodeName.equalsIgnoreCase("cctr_code__prov_dr"))
					{
						String cctrcodeprovdr="",acctcodeprovdr="",postprov="";
						cctrcodeprovdr=genericUtility.getColumnValue("cctr_code__prov_dr", dom);
						acctcodeprovdr=genericUtility.getColumnValue("acct_code__prov_dr", dom);
						postprov=genericUtility.getColumnValue("post_prov", dom);
						if("Y".equalsIgnoreCase(postprov))
						{
							errCode=fincommon.isCctrCode(acctcodeprovdr, cctrcodeprovdr, modName, conn);
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}//end case cctr_code__prov_dr
					if(childNodeName.equalsIgnoreCase("cctr_code__prov_cr"))
					{
						String cctrcodeprovcr="",acctcodeprovcr="",postprov="";
						cctrcodeprovcr=genericUtility.getColumnValue("cctr_code__prov_cr", dom);
						acctcodeprovcr=genericUtility.getColumnValue("acct_code__prov_cr", dom);
						postprov=genericUtility.getColumnValue("post_prov", dom);
						if("Y".equalsIgnoreCase(postprov))
						{
							errCode=fincommon.isCctrCode(acctcodeprovcr, cctrcodeprovcr, modName, conn);
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}//end case cctr_code__prov_dr
					if(childNodeName.equalsIgnoreCase("rate__clg"))
					{
						String lcrate="";
						double rate=0;
						lcrate = checkNull(genericUtility.getColumnValue("rate__clg", dom));

						if(lcrate == null ||  lcrate.trim().length() == 0)
						{
							lcrate = "0" ;
						}
						rate=lcrate == null ? 0 :Double.parseDouble(lcrate);
						System.out.println("Rate :" +rate);
						// Changed By PriyankaC As per the suggest by SM sir remove the validation.[START]
						/*if(rate == 0)
						{
			    		    errCode = "VTINVRTCL2";
			    		    errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}*/
						// Changed By PriyankaC As per the suggest by  SM sir remove the validation.[END]
						if(rate < 0)
						{
							errCode = "VTINVRTCL1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					if(childNodeName.equalsIgnoreCase("form_no"))
					{
						String formNo="",eou="",lspurcorder="",mdate1="",lcquantity="",lsdutypaid="",itemCode="",suppCode="",lsstatus="",lstranid="",lslinenorcp="";
						Timestamp Date=null;
						int noOfParent=0;
						double lcct3qty=0,qtyUsed=0,preQty=0,qtybrow=0,totQty=0,quantity=0;
						formNo = checkNull(genericUtility.getColumnValue("form_no", dom));
						siteCode = checkNull(genericUtility.getColumnValue("site_code", dom1));
						lspurcorder = checkNull(genericUtility.getColumnValue("purc_order", dom));
						mdate1 = checkNull(genericUtility.getColumnValue("tran_date", dom1));
						Date = Timestamp.valueOf(genericUtility.getValidDateString(mdate1.toString(),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())
								+ " 00:00:00.0");
						lcquantity =checkNull(genericUtility.getColumnValue("quantity", dom));

						if(lcquantity == null ||  lcquantity.trim().length() == 0)
						{
							lcquantity = "0" ;
						}
						quantity=lcquantity == null ? 0 :Double.parseDouble(lcquantity);
						lsdutypaid = checkNull(genericUtility.getColumnValue("duty_paid", dom));
						sql = "Select case when eou is null then 'N' else eou end  From site Where site_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, siteCode);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							eou = rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if("Y".equalsIgnoreCase(eou) && "N".equalsIgnoreCase(lsdutypaid))
						{
							if (formNo == null || formNo.trim().length() == 0) 
							{
								errCode = "VTCT3FORM";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else
							{
								itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
								suppCode = checkNull(genericUtility.getColumnValue("supp_code", dom));
								lineNoOrd = checkNull(genericUtility.getColumnValue("line_no__ord", dom));
								if (lineNoOrd == null) 
								{
									lineNoOrd = "   ";
								}
								else 
								{
									lineNoOrd = "    " + lineNoOrd;
									lineNoOrd = lineNoOrd.substring(lineNoOrd.length() - 3,lineNoOrd.length());
									sql="Select  a.status, case when b.quantity is null then 0 else b.quantity end, " +
											"case when b.qty_used is null then 0 else b.qty_used end from ct3form_hdr a ," +
											" ct3form_det b where a.form_no = b.form_no And a.form_no = ? And a.site_code = ? " +
											"And b.supp_code = ? And b.item_code = ? And ? >= a.eff_from " +
											"And ? <= a.valid_upto And b.purc_order = ? " +
											"And b.line_no__ord = ? " +
											"And case when a.confirmed is null then 'N' else a.confirmed end = 'Y'";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, formNo);
									pstmt.setString(2, siteCode);
									pstmt.setString(3, suppCode);
									pstmt.setString(4, itemCode);
									pstmt.setTimestamp(5, Date);
									pstmt.setTimestamp(6, Date);
									pstmt.setString(7, lspurcorder);
									pstmt.setString(8, lineNoOrd);
									rs = pstmt.executeQuery();
									if (rs.next()) 
									{ 
										lsstatus = checkNull(rs.getString(1));
										lcct3qty = rs.getDouble(2);
										qtyUsed = rs.getDouble(3);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									if("O".equalsIgnoreCase(lsstatus))
									{
										errCode = "VTCT3FORM2";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									else
									{
										lstranid = checkNull(genericUtility.getColumnValue("tran_id", dom));
										lslinenorcp =checkNull(genericUtility.getColumnValue("line_no", dom));
										sql="select sum(case when b.quantity is null then 0 else b.quantity end)  from" +
												"porcp a, porcpdet b where a.tran_id = b.tran_id and " +
												"a.tran_id <> ? and	b.form_no = ? " +
												"and case when a.confirmed is null then 'N' else a.confirmed end = 'N' ";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, lstranid);
										pstmt.setString(2, formNo);
										rs = pstmt.executeQuery();
										if (rs.next()) 
										{
											preQty = rs.getDouble(1);
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										NodeList detail2List = dom2.getElementsByTagName("Detail2");
										NodeList detail1List = dom2.getElementsByTagName("Detail1");
										noOfParent = detail2List.getLength();
										for ( ctr = 1; ctr <= noOfParent; ctr++) 
										{
											String lineNoBrow="",formNoTemp="",lineNoTemp="",quantitybrow="";
											System.out.println("noOfParent@@@@@@@@@@@@"+ noOfParent);
											lineNoBrow = genericUtility.getColumnValueFromNode("line_no__ord",detail2List.item(ctr));
											formNoTemp = genericUtility.getColumnValueFromNode("form_no",detail2List.item(ctr));
											lineNoTemp = genericUtility.getColumnValueFromNode("line_no",detail2List.item(ctr));
											if (formNo.equalsIgnoreCase(formNoTemp)&& (lineNoBrow == null || lineNoBrow.length() == 0)&& (!(lslinenorcp == lineNoTemp))) 
											{
												quantitybrow = checkNull(genericUtility.getColumnValueFromNode("quantity",detail2List.item(ctr)));

												if(quantitybrow == null ||  quantitybrow.trim().length() == 0)
												{
													quantitybrow = "0" ;
												}
												qtybrow = quantitybrow == null ? 0: Double.parseDouble(quantitybrow);
												totQty = totQty + qtybrow;
											}
										}

										if ((preQty + totQty + quantity) > (lcct3qty - qtyUsed)) 
										{
											errCode = "VTCT3QTY";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
									}

								}
							}

						}
						else if("Y".equalsIgnoreCase(eou) && "Y".equalsIgnoreCase(lsdutypaid))
						{
							if (formNo != null && formNo.trim().length() >0)
							{
								errCode = "VTCT3DUTY";  
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

							}
						}
					}
					if(childNodeName.equalsIgnoreCase("batch_size"))
					{
						String batchsize="",lssitecode="",lsitem="",mdate1="",suppcodemnfr="";
						double lcbatchsize=0,lcmbatchsize=0;
						Timestamp Date=null;
						batchsize = checkNull(genericUtility.getColumnValue("batch_size", dom));

						if(batchsize == null ||  batchsize.trim().length() == 0)
						{
							batchsize = "0" ;
						}
						lcbatchsize = Double.parseDouble(batchsize);
						lssitecode  = checkNull(genericUtility.getColumnValue("site_code", dom1));
						lsitem 		  = checkNull(genericUtility.getColumnValue("item_code", dom));
						mdate1 		  = checkNull(genericUtility.getColumnValue("tran_date", dom1));
						Date = Timestamp.valueOf(genericUtility.getValidDateString(mdate1,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())
								+ " 00:00:00.0");
						suppcodemnfr= checkNull(genericUtility.getColumnValue("supp_code__mnfr", dom));
						sql="select count(1) from batchsize_aprv " +
								"where item_code 		= ? " +
								"and 	site_code__mfg = ? " +
								"and 	supp_code__mnfr= ? and " +
								"eff_from   	  <= ? " +
								"and 	valid_upto 	  >= ?" +
								"and 	confirmed 		= 'Y'";
						pstmt = conn.prepareStatement(sql);
						//Commented and Added by varsha v because wrong parameter were appended
						//pstmt.setString(1, lssitecode);
						pstmt.setString(1, lsitem);
						//Commented and Added by varsha v because wrong parameter were appended
						pstmt.setString(2, lssitecode);
						pstmt.setString(3, suppcodemnfr);
						pstmt.setTimestamp(4, Date);
						pstmt.setTimestamp(5, Date);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							cnt = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (cnt > 0 && lcbatchsize == 0) 
						{
							errCode = "VMBATCHBLK"; // xQuantity exceeds the balance quantity of  CT3 Form 
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						if( lcbatchsize > 0)  
						{
							sql="select batch_size from batchsize_aprv " +
									"where item_code 		 = ? " +
									"and site_code__mfg  = ? " +
									"and supp_code__mnfr = ? " +
									"and eff_from   		<= ? " +
									"and valid_upto 		>= ? " +
									"and confirmed 		 = 'Y'";
							pstmt = conn.prepareStatement(sql);
							//Commented and Added by varsha v because wrong parameter were appended
							//pstmt.setString(1, lssitecode);
							pstmt.setString(1, lsitem);
							//Commented and Added by varsha v because wrong parameter were appended
							pstmt.setString(2, lssitecode);
							pstmt.setString(3, suppcodemnfr);
							pstmt.setTimestamp(4, Date);
							pstmt.setTimestamp(5, Date);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								lcmbatchsize = rs.getDouble(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(lcbatchsize > lcmbatchsize )
							{
								errCode = "VTBTHSIZE"; 
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());


							}
						}	

					}
					if(childNodeName.equalsIgnoreCase("anal_code"))
					{
						String analcode="",acctcodedr="";
						analcode = checkNull(genericUtility.getColumnValue("anal_code", dom));
						if(analcode != null && analcode.trim().length() >0)
						{
							acctcodedr=checkNull(genericUtility.getColumnValue("acct_code__dr", dom));
							errCode=fincommon.isAnalysis(acctcodedr, analcode, "", conn);

							//changed by kunal on 16/2/18 to check if errorCode is null
							if(errCode!=null && errCode.trim().length()>0)
							{
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

						}	
					}
					if (childNodeName.equalsIgnoreCase("tax_class")) 
					{
						String taxClass="";
						taxClass = genericUtility.getColumnValue("tax_class",dom);
						if (taxClass != null && taxClass.trim().length() > 0) 
						{
							errCode = isExist("taxclass", "tax_class",taxClass, conn);
							if ("FALSE".equalsIgnoreCase(errCode)) 
							{
								errCode = "VTTCLASS1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					if (childNodeName.equalsIgnoreCase("tax_chap")) 
					{
						String taxChap="";
						taxChap = genericUtility.getColumnValue("tax_chap", dom);
						if (taxChap != null && taxChap.trim().length() > 0) 
						{
							errCode = isExist("taxchap", "tax_chap", taxChap,conn);
							if ("FALSE".equalsIgnoreCase(errCode)) 
							{
								errCode = "VTTCHAP1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}

					if (childNodeName.equalsIgnoreCase("tax_env")) 
					{
						String taxEnv="",ordDateStr="";
						//taxEnv = genericUtility.getColumnValue("tax_env", dom);
						taxEnv = discommon.getParentColumnValue("tax_env", dom, "2");
						System.out.println("PORCP 2 TaxEnv:"+taxEnv+"]");
						ordDateStr = checkNull(genericUtility.getColumnValue("tran_date", dom1));
						System.out.println("ordDateStr"+ordDateStr);
						ordDate = Timestamp.valueOf(genericUtility.getValidDateString(ordDateStr.toString(),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+
								" 00:00:00.0");
						System.out.println("ordDate"+ordDate);
						if (taxEnv != null && taxEnv.trim().length() > 0)
						{
							errCode = isExist("taxenv", "tax_env", taxEnv, conn);
							if ("FALSE".equalsIgnoreCase(errCode)) 
							{
								errCode = "VTTENV1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else 
							{ // //getCheckTaxEnvStatus
								//Pavan R 17sept19 start[to validate tax environment]
								/*sql = " select (case when status is null then 'A' else status end) "
										+ "from   taxenv where  tax_env      =  ?  and    status_date  <= ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, taxEnv);
								pstmt.setTimestamp(2, ordDate);
								rs = pstmt.executeQuery();
								if (rs.next()) 
								{
									status = checkNull(rs.getString(1));

								}

								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if ("C".equalsIgnoreCase(status)) 
								{
									errCode = "VTTAXENVCL";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}*/
								errCode = discommon.getCheckTaxEnvStatus(taxEnv, ordDate, "P", conn);
								if(errCode != null && errCode.trim().length() > 0) {
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());									
								}
								//Pavan R 17sept19 end[to validate tax environment]
							}
						}
					}//end for loop
				}
			}// end Switch case

			int errListSize = errList.size();
			cnt = 0;
			String errFldName = null;
			System.out.println("errListSize [" + errListSize+ "] errFields size [" + errFields.size() + "]");
			if (errList != null && errListSize > 0) 
			{
				System.out.println("errList["+ errList.toString() +"]");
				for (cnt = 0; cnt < errListSize; cnt++) {
					errCode = errList.get(cnt);
					errFldName = errFields.get(cnt);
					System.out.println("errFldName ..........[" + errFldName+"]");
					System.out.println("errCode .........." + errCode);
					errString = getErrorString(errFldName, errCode, userId);
					System.out.println("errString ..........[" + errString+"]");
					errorType = errorType(conn, errCode);
					if("VTUREC1".equalsIgnoreCase(errCode))
					{
						startStr = errString.substring(0,errString.indexOf("<description>") + 13);
						endStr = errString.substring(errString.indexOf("</description>"),errString.length());
						descrStr = errString.substring(errString.indexOf("<description>") + 13,errString.indexOf("</description>"));
						descrStart = descrStr.substring(0, descrStr.indexOf("]"));
						descrEnd = descrStr.substring(descrStr.indexOf("]"),descrStr.length());
						System.out.println("lsmsg1@@@["+lsmsg1+"]");
						value ="" + lsmsg1;
						System.out.println("Value ::: "+ value);
						descrStart = descrStart.concat(value).concat(descrEnd);
						System.out.println("descrStart@@@["+descrStart+"]");
						errString = startStr.concat(descrStart).concat(endStr);
					}

					System.out.println("errorType ..........[" + errorType+"]");
					if (errString.length() > 0) 
					{
						String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8,
								errString.indexOf("<trace>"));
						bifurErrString = bifurErrString + errString.substring(errString.indexOf("</trace>") + 8,
								errString.indexOf("</Errors>"));
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
			}
			else
			{
				errStringXml = new StringBuffer("");
			}

		}
		catch (Exception e)
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
	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn,String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		System.out.println("###########ITEMCHANGE FOR CASE FIrst Method###################");
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		System.out.println("XmlString is "+ xmlString);
		System.out.println("XmlString1 is "+ xmlString1);
		System.out.println("XmlString2 is "+ xmlString2);
		System.out.println("itemChanged() called for PorcpIC");
		String valueXmlString = "";
		try {
			if (xmlString != null && xmlString.trim().length() > 0) 
			{
				System.out.println("XmlString is "+ xmlString);
				dom = parseString(xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0) {
				dom1 = parseString(xmlString1);
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0) {
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext,currentColumn, editFlag, xtraParams);
		} catch (Exception e) {
			System.out.println("Exception : [PorcpIC][itemChanged( String, String )] :==>\n"+ e.getMessage());
		}
		return valueXmlString;
	}
	/* (non-Javadoc)
	 * @see ibase.webitm.ejb.ValidatorEJB#itemChanged(org.w3c.dom.Document, org.w3c.dom.Document, org.w3c.dom.Document, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public String itemChanged(Document dom, Document dom1, Document dom2,String objContext, String currentColumn, String editFlag,String xtraParams) throws RemoteException, ITMException
	{
		System.out.println("###########ITEMCHANGE FOR CASE###################");
		String childNodeName = null;
		StringBuffer valueXmlString = new StringBuffer();
		int ctr = 0,cnt = 0;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String valueXmlStringStr = "";
		int currentFormNo = 0;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		ConnDriver connDriver = new ConnDriver();
		String colname="", mcode="",mcode1="", descr="", mTrancd="", mSdesc="", mScode="", mCurr="";
		String mgrno="",scode="",tcode="",lscrterm="",lssf="",lstranname="",lsitemser="";
		String lsisdescr="",lspo="", lscurrcodefrt="", lscurrcodeins="", lschallanno="", lsoctroino="";
		String lsempcode="", lsempname="", lsqcreqd="", lstransmode="", lsstancode="", lspordtype="",lsfname="",lsmname="",lslname="";
		String   lsfrttype="",lslrno="",lslorryno="" ,lscri="",lsvar="",lscurrcode="", lsposttype="", errstr="", lsbankcdpay="", lsbname="", lsacctno="";
		double lcinsuranceamt=0, lcfrtamt=0,lcfrtrate=0 , lcfreight=0, mPcnt=0, lcexchrate=0,lcexchrate2=0,lctemp=0,lcqtystduom=0,lcconv=0;
		Timestamp mtaxdt=null,tdate=null,ldttrandt=null,ldtdc=null,ldtlrdate=null, ldtchallandt=null,ldtmfgdate=null,ldtexpirydate=null,ldttrandate=null;
		double mDiscount=0,mTax=0,mTotAmt=0,mCancperc=0,mOrdQty=0,mAdjQty=0,mDlvQty=0,lcStdqty=0,lcqty=0,mpending=0,mnoart=0;
		double lcgrosswt=0, lctarewt=0, lcnetwt=0, lcchallanqty=0,lcrealisedqty=0,lcsuppchallanqty=0,lcRate=0, lcratestduom=0, lcrateclg=0, lcstdrate=0,   lcQtyConv=0, lcRtConv=0, lcconvtemp=0,lishelflife=0;
		String Errcode = "",lssitecode="", lscurrcodebase="",lspurcorder="",sql = "";
		int liCount=0,lsexists=0;
		String mdescr="",mPordNo="", mPordLine="", lsitemcode="", mUom="", mLocation="", mTaxClass="", mTaxChap="", mTaxEnv="", macdr="", macc="";
		String mctdr="", mctcr="", mVal="", mVal1="", mStdUom="", mRateUom="",lscancbo="",mvarvalue="",lsitemremarks="",lsQcReqd="",lsdisparmVarVal="",lsvalue="";
		String lsspecificinstr = "", lsspecialinstr="", lspackinstr="", lsporateoption="",lssuppcodemnfr="", lsporcplin="" ,mlotno="" ,mlotsl="" ,mitem="" ,lssite="" , lssitemfg="";
		String lspack="", lsmfgitemcd="",lsacct="",lsacct1="", lsinvacctqc="",lsloccode="", lslotsl="",lstrackshelflife="", lsunit="", lsunitstd="", lspricelist="",lspackcode="", lssitecodemfg="",lspotype="",lsudf="",varrcpt="",lsnull="",lsprotect="",lsloginsite="",lscrtermdescr="",lslisttype="" ,lsparentitem="",columnValue="";
		String lsspec="",lsspecdescr="",lsdis="",lssource="",lstranid="",lsstkopt="" , lstrancode="", lsloc="", lsuom="", lsunitpur="", lspack1="", lspackinstr1="", lsaccdesc="", lsanalcode="",lsinvacct="";  
		String lscostctr="",lscostctrasloccode="",emp="",lscctrloccode="", lsuomrnd="",lssitedescr="",loginSiteCode="",siteCode="",lsPostType="";
		String suppCode="",exchRate="",exchRateFrt="";
		String projectCode = ""; //Added by sarita on 29 MARCH 2019
		DistCommon disscommon = new DistCommon();
		FinCommon fincommon = new FinCommon();
		ArrayList lcstdqty1 = null;
		Timestamp timestamp = null;
		SimpleDateFormat sdf=null;
		SimpleDateFormat sdf1=null;
		ArrayList acctDetrList = new ArrayList();
		UtilMethods utilMethod = new UtilMethods();
		boolean cpFlag = false;
		try {
			System.out.println("**********ITEMCHANGE FOR CASE*********************");
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			connDriver = null;
			DistCommon distComm = new DistCommon();
			FinCommon finCommon = new FinCommon();
			ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
			java.util.Date today=new java.util.Date();
			Calendar cal = Calendar.getInstance(); 
			cal.setTime(today); 
			today = cal.getTime();
			sdf=new SimpleDateFormat(genericUtility.getApplDateFormat());
			String sysDate=sdf.format(today);
			System.out.println("\nInside header..dom.."+genericUtility.serializeDom(dom));
			System.out.println("\nInside header..dom1.."+genericUtility.serializeDom(dom1));
			System.out.println("\nInside header..dom2.."+genericUtility.serializeDom(dom2));
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			loginSiteCode = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			String chguser = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			String chgtermhdr = getValueFromXTRA_PARAMS(xtraParams, "chgTerm");
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			timestamp = new Timestamp(System.currentTimeMillis());
			String date = (sdf.format(timestamp).toString()).trim();
			System.out.println("loginSite["+loginSiteCode+"][chguserhdr "+chguser +"][ld_date"+date+"]");
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			System.out.println("Current Form No ["+currentFormNo+"]");							
			System.out.println("**********ITEMCHANGE FOR CASE" + currentFormNo+ "**************");
			switch (currentFormNo)
			{
			case 1:
				System.out.println("**********************In case 1 ***********************");

				parentNodeList = dom.getElementsByTagName("Detail1");
				System.out.println("ParentNodeList"+ parentNodeList);
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0;
				valueXmlString.append("<Detail1>");
				int childNodeListLength = childNodeList.getLength();
				do {
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equals(currentColumn)) 
					{
						if (childNode.getFirstChild() != null)
						{
							columnValue = childNode.getFirstChild().getNodeValue().trim();
						}
					}
					ctr++;
				} while (ctr < childNodeListLength&& !childNodeName.equals(currentColumn));
				System.out.println("@@@@@@@[" + currentColumn + "] ==> '"+ currentColumn + "'");

				if (currentColumn.trim().equalsIgnoreCase("itm_default")) 
				{
					siteCode  = getAbsString(genericUtility.getColumnValue("site_code",dom));	  
					if(loginSiteCode == null || loginSiteCode.trim().length() <= 0)						
					{
						loginSiteCode = siteCode;
					}																														
					//get login site description 
					sql = " select descr from site where site_code = ? ";
					pstmt= conn.prepareStatement( sql );
					pstmt.setString( 1, loginSiteCode );
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						lssitedescr = rs.getString( "descr" ); 
					}
					rs.close();
					pstmt.close();
					pstmt = null;
					rs = null;
					valueXmlString.append("<site_code>").append("<![CDATA["+loginSiteCode.trim()+"]]>").append("</site_code>");
					valueXmlString.append("<site_descr>").append("<![CDATA["+lssitedescr.trim()+"]]>").append("</site_descr>");
					String currAppdate ="";
					java.sql.Timestamp currDate = null;
					currDate = new java.sql.Timestamp(System.currentTimeMillis()) ;
					currAppdate = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(currDate).toString();
					System.out.println("Tran date is"+currAppdate);

					valueXmlString.append("<tran_date>").append("<![CDATA["+currAppdate.trim()+"]]>").append("</tran_date>");
					valueXmlString.append("<eff_date>").append("<![CDATA["+currAppdate.trim()+"]]>").append("</eff_date>");
					valueXmlString.append("<tax_date>").append("<![CDATA["+currAppdate.trim()+"]]>").append("</tax_date>");
					valueXmlString.append("<rec_date>").append("<![CDATA["+currAppdate.trim()+"]]>").append("</rec_date>");
					String tranSer="P-RCP";
					valueXmlString.append("<tran_ser>").append("<![CDATA["+tranSer+"]]>").append("</tran_ser>");
					valueXmlString.append("<amount>").append("<![CDATA[0]]>").append("</amount>");	
					valueXmlString.append("<tax>").append("<![CDATA[0]]>").append("</tax>");	
					valueXmlString.append("<discount>").append("<![CDATA[0]]>").append("</discount>");
					valueXmlString.append("<insurance_amt>").append("<![CDATA[0]]>").append("</insurance_amt>");
					valueXmlString.append("<frt_amt>").append("<![CDATA[0]]>").append("</frt_amt>");
					valueXmlString.append("<exch_rate>").append("<![CDATA[0]]>").append("</exch_rate>");
					valueXmlString.append("<curr_code__frt>").append("<![CDATA[]]>").append("</curr_code__frt>");	
					valueXmlString.append("<curr_code__ins>").append("<![CDATA[]]>").append("</curr_code__ins>");
					valueXmlString.append("<supp_code>").append("<![CDATA[]]>").append("</supp_code>");	
					valueXmlString.append("<supp_name>").append("<![CDATA[]]>").append("</supp_name>");	
					valueXmlString.append("<tran_code>").append("<![CDATA[]]>").append("</tran_code>");
					valueXmlString.append("<transporter_name>").append("<![CDATA[]]>").append("</transporter_name>");
					valueXmlString.append("<curr_code>").append("<![CDATA[]]>").append("</curr_code>");
					valueXmlString.append("<item_ser>").append("<![CDATA[]]>").append("</item_ser>");
					valueXmlString.append("<itemser_descr>").append("<![CDATA[]]>").append("</itemser_descr>");
					valueXmlString.append("<vouch_created >").append("<![CDATA[N]]>").append("</vouch_created>");
					valueXmlString.append("<reciept_type >").append("<![CDATA[F]]>").append("</reciept_type>");
					valueXmlString.append("<accept_criteria >").append("<![CDATA[P]]>").append("</accept_criteria>");
					lsPostType = disscommon.getDisparams("999999", "POST_PORCP_SA", conn);
					if("NULLFOUND".equalsIgnoreCase(lsPostType) || lsPostType == null || lsPostType.trim().length() ==0)
					{
						lsPostType="A";
					}
					valueXmlString.append("<post_type >").append("<![CDATA["+lsPostType+"]]>").append("</post_type>");
					sql = "select emp_code from users where code = ?";
					pstmt = conn.prepareStatement( sql );
					pstmt.setString(1,chguser);
					rs = pstmt.executeQuery();
					if( rs.next() )
					{
						emp = checkNull(rs.getString( "emp_code" ));
					}	
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<emp_code>").append("<![CDATA[" + emp + "]]>").append("</emp_code>");
					lsqcreqd = disscommon.getDisparams("999999", "PORCP_QCREQUIRED_EDITABLE", conn);
					if("NULLFOUND".equalsIgnoreCase(lsqcreqd))
					{
						System.out.println("Null is found");
						lsqcreqd="";
					}
					if ( !"Y".equalsIgnoreCase(lsqcreqd))
					{
						valueXmlString.append("<qc_reqd protect =\"1\">").append("<![CDATA[]]>").append("</qc_reqd>");
					}
					else
					{
						valueXmlString.append("<qc_reqd protect =\"0\">").append("<![CDATA[]]>").append("</qc_reqd>");
					}
					lsinvacct =disscommon.getDisparams("999999", "INV_ACCT_PORCP", conn);
					if("NULLFOUND".equalsIgnoreCase(lsPostType) || lsPostType == null || lsPostType.trim().length() ==0)
					{
						lsinvacct = "N";
					}
					lsinvacctqc =disscommon.getDisparams("999999", "INV_ACCT_QCORDER", conn);
					if("NULLFOUND".equalsIgnoreCase(lsinvacctqc) || lsinvacctqc == null || lsinvacctqc.trim().length() ==0)
					{
						lsinvacctqc = "N";
					}
					if("Y".equalsIgnoreCase(lsinvacct) && "Y".equalsIgnoreCase(lsinvacctqc))
					{
						valueXmlString.append("<post_prov>").append("<![CDATA[Y]]>").append("</post_prov>");
					} else
					{
						valueXmlString.append("<post_prov>").append("<![CDATA[N]]>").append("</post_prov>");
					}

				}
				else if (currentColumn.trim().equalsIgnoreCase("itm_defaultedit"))
				{
					System.out.println("Inside itm_default");
					suppCode=genericUtility.getColumnValue("supp_code", dom);
					lsqcreqd=genericUtility.getColumnValue("qc_reqd", dom);
					exchRate=genericUtility.getColumnValue("exch_rate", dom);
					exchRateFrt = genericUtility.getColumnValue("exch_rate__frt", dom);
					lspurcorder=	genericUtility.getColumnValue("purc_order", dom);
					//Commented and Added by sarita to set add proj_code and set to header on 29 MARCH 2019 [START]
					projectCode = genericUtility.getColumnValue("proj_code", dom);
					//added by manish mhatre on 20-aug-2019 [For purchase order protect when count=0]
					//start manish
					lstranid = genericUtility.getColumnValue("tran_id", dom);
					sql = "select count(*) as CNT from porcpdet where tran_id = ?"; 
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lstranid);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						cnt = rs.getInt("CNT");
					}
					pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;
					//end manish

					//Commented and Added by sarita to set add proj_code and set to header on 29 MARCH 2019 [END]

					valueXmlString.append("<supp_code protect =\"1\">").append("<![CDATA["+suppCode+"]]>").append("</supp_code>");
					valueXmlString.append("<qc_reqd protect =\"1\">").append("<![CDATA["+lsqcreqd+"]]>").append("</qc_reqd>");
					valueXmlString.append("<exch_rate protect =\"1\">").append("<![CDATA["+exchRate+"]]>").append("</exch_rate>");
					valueXmlString.append("<exch_rate__frt protect =\"1\">").append("<![CDATA["+exchRateFrt+"]]>").append("</exch_rate__frt>");
					//added by manish mhatre on 20-aug-2019 [For purchase order protect when count>0]
					//start manish
					if( cnt > 0)
					{
						valueXmlString.append("<purc_order protect =\"1\">").append("<![CDATA["+lspurcorder+"]]>").append("</purc_order>");
					}else {
						valueXmlString.append("<purc_order protect =\"0\">").append("<![CDATA["+lspurcorder+"]]>").append("</purc_order>");
					}
					//end manish
					//Commented and Added by sarita to set add proj_code and set to header on 29 MARCH 2019 [START]
					valueXmlString.append("<proj_code>").append("<![CDATA["+projectCode+"]]>").append("</proj_code>");
					//Commented and Added by sarita to set add proj_code and set to header on 29 MARCH 2019 [END]

				}
				else if (currentColumn.trim().equalsIgnoreCase("site_code"))
				{
					String suppcode="",sitecode="",itemser="",msdesc="",purcorder="";
					suppcode = genericUtility.getColumnValue("supp_code", dom);
					sitecode = genericUtility.getColumnValue("site_code", dom);
					itemser = genericUtility.getColumnValue("itemser", dom);
					if(sitecode != null && sitecode.trim().length() >0)
					{
						sql=" select descr,stan_code from site where site_code = ?";
						pstmt= conn.prepareStatement( sql );
						pstmt.setString( 1,sitecode );
						rs = pstmt.executeQuery(); 
						if( rs.next() )
						{
							msdesc = checkNull(rs.getString("descr")); 
							lsstancode = checkNull(rs.getString("stan_code")); 
						}
						rs.close();
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<site_descr>").append("<![CDATA["+msdesc+"]]>").append("</site_descr>");
						valueXmlString.append("<stan_code__dest>").append("<![CDATA["+lsstancode+"]]>").append("</stan_code__dest>");

					}
					if ((suppcode != null && suppcode.trim().length()>0) && (sitecode != null && sitecode.trim().length()>0)  && (itemser != null && itemser.trim().length()>0))
					{
						sql=" Select count(1) From porder Where supp_code =? and   site_code__dlv = ? and item_ser = ? and status = 'O'";
						pstmt= conn.prepareStatement( sql );
						pstmt.setString( 1,suppcode );
						pstmt.setString( 2,sitecode );
						pstmt.setString( 3,itemser );
						rs = pstmt.executeQuery(); 
						if( rs.next() )
						{
							cnt=rs.getInt(1);
						}
						rs.close();
						pstmt.close();
						pstmt = null;
						if(cnt>0)
						{
							sql="select purc_order from porder where supp_code = ? and site_code__dlv = ? and item_ser = ? and status = 'O' order by purc_order desc";
							pstmt= conn.prepareStatement( sql );
							pstmt.setString( 1,suppcode );
							pstmt.setString( 2,sitecode );
							pstmt.setString( 3,itemser );
							rs = pstmt.executeQuery(); 
							if( rs.next() )
							{
								purcorder = checkNull(rs.getString( "purc_order" )); 
							}
							rs.close();
							pstmt.close();
							pstmt = null;
							valueXmlString.append("<purc_order>").append("<![CDATA["+purcorder.trim()+"]]>").append("</purc_order>");
						}
						//Pavan Rane 11jun19 start [to store the channel partner flag]
						cpFlag = isChannelPartnerSupp(suppcode, sitecode, conn);
						if(cpFlag)
						{
							valueXmlString.append("<channel_partner><![CDATA[" + "Y" + "]]></channel_partner>");
						}else 
						{
							valueXmlString.append("<channel_partner><![CDATA[" + "N" + "]]></channel_partner>");					
						}
						//Pavan Rane 11jun19 end
					}
				}
				else if (currentColumn.trim().equalsIgnoreCase("supp_code"))
				{
					String suppcode="",sitecode="",itemser="",msdesc="",purcorder="",suppname="",trancode="",stancode="",currcode="",trandt="";
					double exchrate=0;
					suppcode = genericUtility.getColumnValue("supp_code", dom);
					sitecode = genericUtility.getColumnValue("site_code", dom);
					itemser = genericUtility.getColumnValue("itemser", dom);
					if(suppcode == null || suppcode.trim().length() == 0)
					{
						valueXmlString.append("<supp_name>").append("<![CDATA[]]>").append("</supp_name>");
					}
					if(suppcode != null && suppcode.trim().length() >0)
					{
						sql="select supp_name, tran_code, stan_code, curr_code from supplier where supp_code = ?";
						pstmt= conn.prepareStatement( sql );
						pstmt.setString( 1,suppcode );
						rs = pstmt.executeQuery(); 
						if( rs.next() )
						{
							suppname = checkNull(rs.getString( "supp_name" ));
							trancode = checkNull(rs.getString( "tran_code" )); 
							stancode = checkNull(rs.getString( "stan_code" )); 
							currcode = checkNull(rs.getString( "curr_code" )); 
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<supp_name>").append("<![CDATA["+suppname+"]]>").append("</supp_name>");
						valueXmlString.append("<tran_code>").append("<![CDATA["+trancode+"]]>").append("</tran_code>");
						valueXmlString.append("<stan_code__load>").append("<![CDATA["+stancode+"]]>").append("</stan_code__load>");
						valueXmlString.append("<curr_code>").append("<![CDATA["+currcode+"]]>").append("</curr_code>");
						valueXmlString.append("<supp_code__ship>").append("<![CDATA["+suppcode+"]]>").append("</supp_code__ship>");
						valueXmlString.append("<supp_name__ship>").append("<![CDATA["+suppname+"]]>").append("</supp_name__ship>");
					}
					purcorder = genericUtility.getColumnValue("purc_order", dom);
					if(purcorder == null || purcorder.trim().length()==0)
					{
						System.out.println("purcorder"+purcorder);
						sql="select curr_code,tran_code, stan_code from SUPPLIER where supp_code = ?";
						pstmt= conn.prepareStatement( sql );
						pstmt.setString( 1,suppcode );
						rs = pstmt.executeQuery(); 
						if( rs.next() )
						{
							currcode = checkNull(rs.getString( "curr_code" ));
							trancode = checkNull(rs.getString( "tran_code" ));
							stancode = checkNull(rs.getString( "stan_code" )); 
							System.out.println("currcode"+currcode);
							System.out.println("trancode"+trancode);
							System.out.println("stancode"+stancode);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<curr_code>").append("<![CDATA["+currcode+"]]>").append("</curr_code>");
						valueXmlString.append("<tran_code>").append("<![CDATA["+trancode+"]]>").append("</tran_code>");
						valueXmlString.append("<stan_code__load>").append("<![CDATA["+stancode+"]]>").append("</stan_code__load>");
						trandt = genericUtility.getColumnValue("eff_date", dom);
						ldttrandt = Timestamp.valueOf(genericUtility.getValidDateString(trandt.toString(),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");
						System.out.println("Date is"+ldttrandt);
						sql="select a.curr_code  from finent a, site b where b.fin_entity = a.fin_entity and b.site_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, sitecode);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lscurrcodebase = checkNull(rs.getString(1));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if(currcode != null && currcode.trim().length() >0)
						{
							lcexchrate =fincommon.getDailyExchRateSellBuy(currcode, lscurrcodebase, sitecode, trandt, "B", conn);
							if(exchrate >0)
							{
								valueXmlString.append("<exch_rate>").append("<![CDATA["+exchrate+"]]>").append("</exch_rate>");
							}
						}
					}
					if ((suppcode != null && suppcode.trim().length()>0) && (sitecode != null && sitecode.trim().length()>0)  && (itemser != null && itemser.trim().length()>0))
					{
						sql=" Select count(1) From porder Where supp_code =? and   site_code__dlv = ? and item_ser = ? and status = 'O'";
						pstmt= conn.prepareStatement( sql );
						pstmt.setString( 1,suppcode );
						pstmt.setString( 2,sitecode );
						pstmt.setString( 3,itemser );
						rs = pstmt.executeQuery(); 
						if( rs.next() )
						{
							cnt=rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if(cnt>0)
						{
							sql="select purc_order from porder where supp_code = ? and site_code__dlv = ? and item_ser = ? and status = 'O' order by purc_order desc";
							pstmt= conn.prepareStatement( sql );
							pstmt.setString( 1,suppcode );
							pstmt.setString( 2,sitecode );
							pstmt.setString( 3,itemser );
							rs = pstmt.executeQuery(); 
							if( rs.next() )
							{
								purcorder = checkNull(rs.getString( "purc_order" )); 
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							valueXmlString.append("<purc_order>").append("<![CDATA["+purcorder.trim()+"]]>").append("</purc_order>");

						}
					}
					if((suppcode != null && suppcode.trim().length() >0) && (sitecode != null && sitecode.trim().length() >0))
					{
						//Added by PriyankaC on 23JULY2019 to get qc_reqd from itemser [START].

						sql=" select (case when qc_reqd is null then 'N' else qc_reqd end) from site_supplier where site_code = ? and supp_code = ?";
						pstmt= conn.prepareStatement( sql );
						pstmt.setString( 1,sitecode);
						pstmt.setString( 2,suppcode);
						rs = pstmt.executeQuery(); 
						if( rs.next() )
						{
							lsqcreqd = rs.getString(1); 
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;



						if(lsqcreqd.trim().length()==0)
						{
							sql="select (case when qc_reqd is null then 'N' else qc_reqd end) from supplier where supp_code =?";
							pstmt= conn.prepareStatement( sql );
							pstmt.setString(1,suppcode);
							rs = pstmt.executeQuery(); 
							if( rs.next() )
							{
								lsqcreqd = checkNull(rs.getString(1)); 
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

						}
						if (lsqcreqd == null || lsqcreqd.trim().length() == 0)
						{
							sql="select case when qc_reqd is null then 'N' else qc_reqd end from itemser where item_ser = ?";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1,itemser);;
							rs=pstmt.executeQuery();
							if(rs.next())
							{
								lsqcreqd=checkNull(rs.getString(1));
							}
							rs.close();
							rs=null;
							pstmt.close();
							pstmt=null;


						}
						//Added by PriyankaC on 23JULY2019 to get qc_reqd from itemser [END].

						if (lsqcreqd != null && lsqcreqd.trim().length()>0)
						{
							if("Y".equalsIgnoreCase(lsqcreqd))
							{
								valueXmlString.append("<qc_reqd>").append("<![CDATA["+lsqcreqd+"]]>").append("</qc_reqd>");
							}
							else
							{
								valueXmlString.append("<qc_reqd>").append("<![CDATA["+lsqcreqd+"]]>").append("</qc_reqd>");
							}
						}
						//Pavan Rane 11jun19 start [to store the channel partner flag]
						cpFlag = isChannelPartnerSupp(suppcode, sitecode, conn);
						if(cpFlag)
						{
							valueXmlString.append("<channel_partner><![CDATA[" + "Y" + "]]></channel_partner>");
						}else 
						{
							valueXmlString.append("<channel_partner><![CDATA[" + "N" + "]]></channel_partner>");					
						}
						//Pavan Rane 11jun19 end
					}
				}
				else if (currentColumn.trim().equalsIgnoreCase("supp_code__ship"))
				{

					mcode = genericUtility.getColumnValue("supp_code__ship", dom);
					sql="select supp_name from supplier where supp_code =?";
					pstmt= conn.prepareStatement( sql );
					pstmt.setString(1,mcode);
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						descr = checkNull(rs.getString(1)); 
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<supp_name__ship>").append("<![CDATA["+descr+"]]>").append("</supp_name__ship>");
				}
				else if (currentColumn.trim().equalsIgnoreCase("purc_order"))
				{
					//site_code__mfg
					String purcorder="",suppcode="",ldtdc1="",ldttrandt1="",mtaxdate="",mtrancd="",Effdate1="";
					Timestamp effdate=null;
					purcorder = genericUtility.getColumnValue("purc_order", dom);
					System.out.println("PO DOm "+purcorder.length());
					purcorder=purcorder.trim();
					System.out.println("PO Trim "+purcorder.length());
					suppcode = genericUtility.getColumnValue("supp_code", dom);
					ldtdc1 =genericUtility.getColumnValue("dc_date", dom);
					System.out.println("String dc_date is"+ldtdc1);
					ldttrandt1 =genericUtility.getColumnValue("tran_date", dom);
					ldttrandt1 = ldttrandt1 == null ? "" : ldttrandt1.trim();
					System.out.println("String Effdate is"+ldttrandt1);
					//Added By Priyankac TO SET TRAN DATE ID CHALLAN DATE IS NULL [START]
					// ldtdc1 = ldtdc1 == null ? "" : ldtdc1.trim();
					ldtdc1 = ldtdc1 == null ? ldttrandt1 : ldtdc1.trim();
					System.out.println("String dc_date is after null"+ldtdc1);
					//Added By Priyankac TO SET TRAN DATE ID CHALLAN DATE IS NULL [END]
					//Commented and Added by sarita to set add proj_code and set to header on 29 MARCH 2019 [START]
					/*sql="select supp_code, tax_date, curr_code, exch_rate,cr_term,tran_code,item_ser, emp_code, trans_mode, pord_type," +
		      		"accept_criteria, anal_code, bank_code__pay from porder where purc_order =?";*/
					sql="select supp_code, tax_date, curr_code, exch_rate,cr_term,tran_code,item_ser, emp_code, trans_mode, pord_type," +
							"accept_criteria, anal_code, bank_code__pay,proj_code from porder where purc_order =?";
					//Commented and Added by sarita to set add proj_code and set to header on 29 MARCH 2019 [END]
					pstmt= conn.prepareStatement( sql );
					pstmt.setString(1,purcorder);
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						mScode = checkNull(rs.getString(1)); 
						mtaxdate = checkNull(rs.getString(2)); 
						mCurr= checkNull(rs.getString(3));
						mPcnt= rs.getDouble(4);
						lscrterm= checkNull(rs.getString(5));
						mtrancd= checkNull(rs.getString(6));
						lsitemser= checkNull(rs.getString(7));
						lsempcode= checkNull(rs.getString(8));
						lstransmode= checkNull(rs.getString(9));
						lspordtype= checkNull(rs.getString(10));
						lscri= checkNull(rs.getString(11));
						lsanalcode= checkNull(rs.getString(12));
						lsbankcdpay= checkNull(rs.getString(13));
						//Commented and Added by sarita to set add proj_code and set to header on 29 MARCH 2019 [START]
						projectCode = checkNull(rs.getString(14));
						//Commented and Added by sarita to set add proj_code and set to header on 29 MARCH 2019 [END]
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<purc_order>").append("<![CDATA["+purcorder+"]]>").append("</purc_order>");
					valueXmlString.append("<supp_code>").append("<![CDATA["+mScode+"]]>").append("</supp_code>");
					valueXmlString.append("<curr_code>").append("<![CDATA["+mCurr+"]]>").append("</curr_code>");
					valueXmlString.append("<tran_code>").append("<![CDATA["+mtrancd+"]]>").append("</tran_code>");
					valueXmlString.append("<item_ser protect =\"1\">").append("<![CDATA["+lsitemser+"]]>").append("</item_ser>");
					valueXmlString.append("<trans_mode>").append("<![CDATA["+lstransmode+"]]>").append("</trans_mode>");
					valueXmlString.append("<pord_type>").append("<![CDATA["+lspordtype+"]]>").append("</pord_type>");
					valueXmlString.append("<accept_criteria>").append("<![CDATA["+lscri+"]]>").append("</accept_criteria>");
					valueXmlString.append("<supp_code__ship>").append("<![CDATA["+mScode+"]]>").append("</supp_code__ship>");
					valueXmlString.append("<anal_code>").append("<![CDATA["+lsanalcode+"]]>").append("</anal_code>");
					valueXmlString.append("<cr_term>").append("<![CDATA["+lscrterm+"]]>").append("</cr_term>");
					//Commented and Added by sarita to set add proj_code and set to header on 29 MARCH 2019 [START]
					valueXmlString.append("<proj_code>").append("<![CDATA["+projectCode+"]]>").append("</proj_code>");
					//Commented and Added by sarita to set add proj_code and set to header on 29 MARCH 2019 [END]
					if(lscrterm != null && lscrterm.trim().length()>0 )
					{  
						sql=" select descr from crterm   where cr_term=?";
						pstmt= conn.prepareStatement( sql );
						pstmt.setString(1,lscrterm);
						rs = pstmt.executeQuery(); 
						if( rs.next() )
						{
							lscrtermdescr = checkNull(rs.getString(1)); 
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

					}
					valueXmlString.append("<crterm_descr>").append("<![CDATA["+lscrtermdescr+"]]>").append("</crterm_descr>");
					if(lsbankcdpay == null ||lsbankcdpay.trim().length() == 0 )
					{
						valueXmlString.append("<bank_code__pay>").append("<![CDATA[]]>").append("</bank_code__pay>");	
						valueXmlString.append("<bank_name__ben>").append("<![CDATA[]]>").append("</bank_name__ben>");	
						valueXmlString.append("<bank_acct_no__ben>").append("<![CDATA[]]>").append("</bank_acct_no__ben>");
					}
					else
					{
						sql="select bank_name__ben, bank_acct_no__ben from supplier_bank where" +
								" supp_code = ? and    bank_code__ben = ? " +
								"and case when confirmed is null then 'N' else confirmed end = 'Y' and  " +
								"case when active_yn is null then 'Y' else active_yn end = 'Y' ";
						pstmt= conn.prepareStatement( sql );
						pstmt.setString(1,mScode);
						pstmt.setString(2,lsbankcdpay);
						rs = pstmt.executeQuery(); 
						if( rs.next() )
						{
							lsbname = checkNull(rs.getString(1)); 
							lsacctno = checkNull(rs.getString(2)); 
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<bank_code__pay>").append("<![CDATA["+lsbankcdpay+"]]>").append("</bank_code__pay>");	
						valueXmlString.append("<bank_name__ben>").append("<![CDATA["+lsbname+"]]>").append("</bank_name__ben>");	
						valueXmlString.append("<bank_acct_no__ben>").append("<![CDATA["+lsacctno+"]]>").append("</bank_acct_no__ben>");

					}
					sql="select descr from itemser where item_ser = ?";
					pstmt= conn.prepareStatement( sql );
					pstmt.setString(1,lsitemser);
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						lsisdescr = checkNull(rs.getString(1)); 
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println("descr is["+lsposttype+"]");
					System.out.println("Cr Term is"+lscrterm);
					sql=" select start_from from crterm where cr_term =?";
					pstmt= conn.prepareStatement( sql );
					pstmt.setString(1,lscrterm);
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						lssf = checkNull(rs.getString(1)); 

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println("lssf"+lssf);
					if("D".equalsIgnoreCase(lssf))
					{
						//Modified by Anjali R. on [30/08/2018][To check dc_date is null][Start]
						if(ldtdc1 != null && ldtdc1.trim().length() > 0)
						{
							System.out.println("ldtdc1"+ldtdc1);
							ldtdc = Timestamp.valueOf(genericUtility.getValidDateString(ldtdc1.toString(),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");
							//Modified by Azhar K. on [06-05-2019][To set effective date when dc date is not null][Start]
							valueXmlString.append("<eff_date>").append("<![CDATA["+ldtdc1+"]]>").append("</eff_date>");
							//Modified by Azhar K. on [06-05-2019][To set effective date when dc date is not null][End]
						}
						//Modified by Anjali R. on [30/08/2018][To check dc_date is null][End]
						//System.out.println("ldtdc is"+ldtdc);
						//valueXmlString.append("<eff_date>").append(ldtdc1).append("</eff_date>");
						//valueXmlString.append("<eff_date>").append("<![CDATA["+ldtdc1+"]]>").append("</eff_date>");

						//Modified by Azhar K. on [06-05-2019][To set tran date as effective date when dc date is null][Start]
						else
						{
							System.out.println("ldttrandt1"+ldttrandt1);
							//ldttrandt = Timestamp.valueOf(genericUtility.getValidDateString(ldttrandt1.toString(),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");
							valueXmlString.append("<eff_date>").append("<![CDATA["+ldttrandt1+"]]>").append("</eff_date>");
						}
						//Modified by Azhar K. on [06-05-2019][To set tran date as effective date when dc date is null][End]

					}
					else
					{
						//ldttrandt = Timestamp.valueOf(genericUtility.getValidDateString(ldttrandt1.toString(),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");
						System.out.println("ldttrandt1 is"+ldttrandt1);
						valueXmlString.append("<eff_date>").append("<![CDATA["+ldttrandt1+"]]>").append("</eff_date>");
					}
					System.out.println("mScode"+mScode);
					sql="select supp_name  from supplier where supp_code =?";
					pstmt= conn.prepareStatement( sql );
					pstmt.setString(1,mScode);
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						descr = checkNull(rs.getString(1)); 

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					sql="  select tran_name from transporter where tran_code =?";
					pstmt= conn.prepareStatement( sql );
					pstmt.setString(1,mtrancd);
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						lstranname = checkNull(rs.getString(1)); 

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					sql="select porder.curr_code__frt, porder.curr_code__ins ,frt_type,frt_rate,frt_amt," +
							"insurance_amt from porder where porder.purc_order = ? ";
					pstmt= conn.prepareStatement( sql );
					pstmt.setString(1,purcorder);
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{

						lscurrcodefrt = rs.getString(1);
						lscurrcodeins = rs.getString(2); 
						lsfrttype = rs.getString(3); 
						lcfrtrate = rs.getDouble(4); 
						lcfrtamt = rs.getDouble(5); 
						lcinsuranceamt = rs.getDouble(6); 
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println("lscurrcodefrt @@"+lscurrcodefrt);
					System.out.println("lscurrcodeins @"+lscurrcodeins);
					System.out.println("lsfrttype"+lsfrttype);
					System.out.println("lcfrtrate @"+lcfrtrate);
					System.out.println("lcfrtamt @"+lcfrtamt);
					System.out.println("lcinsuranceamt @"+lcinsuranceamt);
					sql = " select case when employee.emp_fname is null then ' ' else employee.emp_fname end as fname,"
							+ " case when employee.emp_mname is null then ' ' else employee.emp_mname end as mname,"
							+ " case when employee.emp_lname is null then ' ' else employee.emp_lname end as lname"
							+ " from employee where emp_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,lsempcode);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						lsfname = checkNull(rs.getString(1));
						lsmname = checkNull(rs.getString(2));
						lslname = checkNull(rs.getString(3));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println("First name"+lsfname);
					System.out.println("Second name"+lsmname);
					System.out.println("Last name"+lslname);
					valueXmlString.append("<emp_fname>").append("<![CDATA[" + lsfname + "]]>").append("</emp_fname>");
					valueXmlString.append("<emp_mname>").append("<![CDATA[" + lsmname + "]]>").append("</emp_mname>");
					valueXmlString.append("<emp_lname>").append("<![CDATA[" + lslname + "]]>").append("</emp_lname>");
					valueXmlString.append("<curr_code__frt>").append("<![CDATA[" + lscurrcodefrt + "]]>").append("</curr_code__frt>");
					valueXmlString.append("<insurance_amt>").append("<![CDATA[" + lcinsuranceamt + "]]>").append("</insurance_amt>");
					if(lsfrttype == null || lsfrttype.trim().length() ==0)
					{
						lsfrttype=" ";
						valueXmlString.append("<frt_type>").append("<![CDATA[" + lsfrttype + "]]>").append("</frt_type>");
					}else
					{
						valueXmlString.append("<frt_type>").append("<![CDATA[" + lsfrttype + "]]>").append("</frt_type>");
					}
					valueXmlString.append("<frt_rate>").append("<![CDATA[" + lcfrtrate + "]]>").append("</frt_rate>");
					valueXmlString.append("<frt_amt>").append("<![CDATA[" + lcfrtamt + "]]>").append("</frt_amt>");
					if(lscurrcodeins == null || lscurrcodeins.trim().length() ==0)
					{
						valueXmlString.append("<curr_code__ins>").append("<![CDATA[ ]]>").append("</curr_code__ins>");
					}else
					{
						valueXmlString.append("<curr_code__ins>").append("<![CDATA[" + lscurrcodeins + "]]>").append("</curr_code__ins>");
					}
					valueXmlString.append("<supp_code>").append("<![CDATA[" + mScode + "]]>").append("</supp_code>");
					valueXmlString.append("<supp_name>").append("<![CDATA[" + descr + "]]>").append("</supp_name>");
					valueXmlString.append("<supp_name__ship>").append("<![CDATA[" + descr + "]]>").append("</supp_name__ship>");
					valueXmlString.append("<trans_mode>").append("<![CDATA[" + lstransmode + "]]>").append("</trans_mode>");
					valueXmlString.append("<tran_code>").append("<![CDATA[" + mtrancd + "]]>").append("</tran_code>");
					valueXmlString.append("<transporter_name>").append("<![CDATA[" + lstranname + "]]>").append("</transporter_name>");
					valueXmlString.append("<curr_code>").append("<![CDATA[" + mCurr + "]]>").append("</curr_code>");
					valueXmlString.append("<item_ser>").append("<![CDATA[" + lsitemser + "]]>").append("</item_ser>");
					valueXmlString.append("<itemser_descr>").append("<![CDATA[" + lsisdescr + "]]>").append("</itemser_descr>");
					lssitecode = checkNull(genericUtility.getColumnValue("site_code", dom));
					Effdate1 =genericUtility.getColumnValue("eff_date", dom);
					System.out.println("ldtdc is"+ldtdc);
					sql="select a.curr_code  from finent a, site b where b.fin_entity = a.fin_entity and b.site_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lssitecode);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						lscurrcodebase = checkNull(rs.getString(1));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if(mCurr != null && mCurr.trim().length() >0)
					{
						lcexchrate=fincommon.getDailyExchRateSellBuy(mCurr, lscurrcodebase, lssitecode, Effdate1, "B", conn);
						if(lcexchrate > 0)
						{
							valueXmlString.append("<exch_rate protect =\"1\">").append("<![CDATA["+lcexchrate+"]]>").append("</exch_rate>");
						}
						else
						{
							valueXmlString.append("<exch_rate protect =\"1\">").append("<![CDATA["+mPcnt+"]]>").append("</exch_rate>");
						}
					}
					// COMMENTED by Nandkumar Gadkari on 17MAR2018
					// valueXmlString.append("<exch_rate protect =\"1\">").append(mPcnt).append("</exch_rate>");
					System.out.println("curr_code__clr"+lscurrcodebase);
					valueXmlString.append("<curr_code__clr>").append("<![CDATA[" + lscurrcodebase + "]]>").append("</curr_code__clr>");
					sql="select a.curr_code from finent a, site b where b.fin_entity = a.fin_entity and b.site_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lssitecode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lscurrcode = checkNull(rs.getString(1));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println("lscurrcode["+lscurrcode+"]");
					if(lscurrcodebase !=null && lscurrcodebase.trim().length()>0)
					{
						lcexchrate=fincommon.getDailyExchRateSellBuy(lscurrcodebase, lscurrcode, lssitecode, Effdate1, "B", conn);
						valueXmlString.append("<exch_rate__clr protect =\"1\">").append("<![CDATA[" + lcexchrate + "]]>").append("</exch_rate__clr>");
					}
					System.out.println("lscurrcodefrt"+lscurrcodefrt);
					if(lscurrcodefrt !=null && lscurrcodefrt.trim().length() >0)
					{
						lcexchrate=fincommon.getDailyExchRateSellBuy(lscurrcodefrt, lscurrcodebase, lssitecode, Effdate1, "B", conn);
						valueXmlString.append("<exch_rate__frt protect =\"1\">").append("<![CDATA[" + lcexchrate + "]]>").append("</exch_rate__frt>");
					}
					if(lscurrcodeins !=null && lscurrcodeins.trim().length() >0)
					{
						lcexchrate=fincommon.getDailyExchRateSellBuy(lscurrcodeins, lscurrcodebase, lssitecode, Effdate1, "B", conn);
						valueXmlString.append("<exch_rate__ins protect =\"1\">").append("<![CDATA[" + lcexchrate + "]]>").append("</exch_rate__ins>");
						//valueXmlString.append("<exch_rate__ins>").append("<![CDATA[" + lcexchrate + "]]>").append("</exch_rate__ins>");
					}
					String qcSiteCode ="" ,itemser=""; 
					qcSiteCode= genericUtility.getColumnValue("site_code", dom);
					System.out.println("Supp COde["+mScode+"]");
					System.out.println("Site Code["+qcSiteCode+"]");
					//Pavan Rane 11jun19 start [to store the channel partner flag]									
					cpFlag = isChannelPartnerSupp(mScode, qcSiteCode, conn);					
					if(cpFlag)
					{
						valueXmlString.append("<channel_partner><![CDATA[" + "Y" + "]]></channel_partner>");
					}else 
					{
						valueXmlString.append("<channel_partner><![CDATA[" + "N" + "]]></channel_partner>");					
					}
					//Pavan Rane 11jun19 end										
					//Added by PriyankaC on 23JULY2019 to get qc_reqd from itemser [START].
					itemser = genericUtility.getColumnValue("itemser", dom);
					System.out.println("itemser["+itemser+"]");
					sql=" select (case when qc_reqd is null then 'N' else qc_reqd end) from site_supplier where site_code = ? and supp_code = ?";
					pstmt= conn.prepareStatement( sql );
					pstmt.setString( 1,qcSiteCode);
					pstmt.setString( 2,mScode);
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						lsqcreqd = rs.getString(1); 
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if(lsqcreqd == null ||lsqcreqd.trim().length()==0)
					{
						sql="select (case when qc_reqd is null then 'N' else qc_reqd end) from supplier where supp_code =?";
						pstmt= conn.prepareStatement( sql );
						pstmt.setString(1,mScode);
						rs = pstmt.executeQuery(); 
						if( rs.next() )
						{
							lsqcreqd = checkNull(rs.getString(1)); 
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;


					}
					if (lsqcreqd == null || lsqcreqd.trim().length() == 0)
					{
						sql="select case when qc_reqd is null then 'N' else qc_reqd end from itemser where item_ser = ?";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1,itemser);;
						rs=pstmt.executeQuery();
						if(rs.next())
						{
							lsqcreqd=checkNull(rs.getString(1));
						}
						rs.close();
						rs=null;
						pstmt.close();
						pstmt=null;

						//Added by PriyankaC on 23JULY2019 to get qc_reqd from itemser [END].

					}
					if (lsqcreqd != null && lsqcreqd.trim().length()>0)
					{
						if("Y".equalsIgnoreCase(lsqcreqd))
						{
							valueXmlString.append("<qc_reqd>").append("<![CDATA[" + lsqcreqd + "]]>").append("</qc_reqd>");
						}
						else
						{
							valueXmlString.append("<qc_reqd>").append("<![CDATA[" + lsqcreqd + "]]>").append("</qc_reqd>");
						}
					}
				}
				else if (currentColumn.trim().equalsIgnoreCase("curr_code__frt"))
				{

					String Effdate1="";
					Timestamp effdate=null;
					String lcadditionalcost1="";
					String lcexchrate1="",lcfreight1="",lcinsurance1="",lcclearing1="",lcother1="";
					double acfreight=0,acinsurance=0,acclearing=0,acother=0;
					double exchratefrt=0,exchratefrtins=0,exchratefrtclr=0,exchratetoth=0;
					lscurrcode = genericUtility.getColumnValue( "curr_code__frt", dom );
					lssitecode = genericUtility.getColumnValue( "site_code", dom );
					lcfreight1 =  checkNull(genericUtility.getColumnValue( "frt_amt", dom ));
					acfreight= (lcfreight1==null  ||  lcfreight1.trim().length() == 0)?0:Double.parseDouble(lcfreight1);
					lcinsurance1 = checkNull( genericUtility.getColumnValue( "insurance_amt", dom ));
					acinsurance=(lcinsurance1==null ||  lcinsurance1.trim().length() == 0) ?0:Double.parseDouble(lcinsurance1);
					lcclearing1 =  checkNull(genericUtility.getColumnValue( "clearing_charges", dom ));
					acclearing=(lcclearing1==null ||  lcclearing1.trim().length() == 0)?0:Double.parseDouble(lcclearing1); 
					lcother1 =  genericUtility.getColumnValue( "other_charges", dom );

					acother=(lcother1==null ||  lcother1.trim().length() == 0)?0:Double.parseDouble(lcother1); 
					Effdate1 =genericUtility.getColumnValue("eff_date", dom);
					System.out.println("curr_code__frt-->frt_amt["+acfreight+"]");
					System.out.println("curr_code__frt-->insurance_amt["+acinsurance+"]");
					System.out.println("curr_code__frt-->clearing_charges["+acclearing+"]");
					System.out.println("curr_code__frt-->other_charges["+lcother1+"]");
					sql="select a.curr_code from finent a, site b where b.fin_entity = a.fin_entity and b.site_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lssitecode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						lscurrcodebase = checkNull(rs.getString(1));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if(lscurrcode !=null && lscurrcode.trim().length()>0)
					{
						lcexchrate=fincommon.getDailyExchRateSellBuy(lscurrcode, lscurrcodebase, lssitecode, Effdate1, "B", conn);
						valueXmlString.append("<exch_rate__frt protect =\"1\">").append("<![CDATA[" + lcexchrate + "]]>").append("</exch_rate__frt>");
					}
					/*		//commented by-monika-29-may-2019
				String lcexchrate3="",lcexchrate4="",lcexchrate5="",lcexchrate6="";
				lcexchrate3 = checkNull(genericUtility.getColumnValue( "exch_rate__frt", dom));
				exchratefrt=(lcexchrate3==null ||  lcexchrate3.trim().length() == 0) ?0:Double.parseDouble(lcexchrate3);
				lcexchrate4 = checkNull(genericUtility.getColumnValue( "exch_rate__ins", dom ));
				exchratefrtins=(lcexchrate4==null  ||  lcexchrate4.trim().length() == 0)?0:Double.parseDouble(lcexchrate4);
				lcexchrate5 = checkNull(genericUtility.getColumnValue( "exch_rate__clr", dom ));
				exchratefrtclr=(lcexchrate3==null ||  lcexchrate5.trim().length() == 0) ?0:Double.parseDouble(lcexchrate5);
				lcexchrate6 = checkNull(genericUtility.getColumnValue( "exch_rate__othch", dom ));
				exchratetoth=(lcexchrate4==null   ||  lcexchrate6.trim().length() == 0)?0:Double.parseDouble(lcexchrate6);*/

					//String lcadditionalcost1= toTalCostAddition(acfreight,acinsurance,acclearing,acother,lcexchrate,exchratefrtins,exchratefrtclr,exchratetoth ,conn);

					//changes-made by-Monika-29-may-2019
					lcadditionalcost1= toTalCostAddition(dom,conn);
					valueXmlString.append("<total_addl_cost>").append("<![CDATA[" + lcadditionalcost1 + "]]>").append("</total_addl_cost>");

				}
				else if (currentColumn.trim().equalsIgnoreCase("curr_code__ins"))
				{	

					String Effdate1="";
					Timestamp effdate=null;
					String lcadditionalcost1="";
					String lcexchrate1="",lcfreight1="",lcinsurance1="",lcclearing1="",lcother1="";
					double exchratefrt=0,exchratefrtins=0,exchratefrtclr=0,exchratetoth=0;
					double acfreight=0,acinsurance=0,acclearing=0,acother=0;
					lscurrcode = genericUtility.getColumnValue( "curr_code__ins", dom );
					lssitecode = genericUtility.getColumnValue( "site_code", dom );
					Effdate1 =genericUtility.getColumnValue("eff_date", dom);
					lcfreight1 =  checkNull(genericUtility.getColumnValue( "frt_amt", dom ));
					acfreight=(lcfreight1==null ||  lcfreight1.trim().length() == 0)?0:Double.parseDouble(lcfreight1);
					lcinsurance1 =  checkNull(genericUtility.getColumnValue( "insurance_amt", dom ));
					acinsurance=(lcinsurance1==null ||  lcinsurance1.trim().length() == 0) ?0:Double.parseDouble(lcinsurance1);
					lcclearing1 =  checkNull(genericUtility.getColumnValue( "clearing_charges", dom ));

					acclearing=(lcclearing1==null ||  lcclearing1.trim().length() == 0) ?0:Double.parseDouble(lcclearing1);
					lcother1 =  checkNull(genericUtility.getColumnValue( "other_charges", dom ));

					if(lcother1 == null ||  lcother1.trim().length() == 0)
					{
						lcother1 = "0" ;
					}
					acother=lcother1==null ?0:Double.parseDouble(lcother1);
					System.out.println("curr_code__ins-->frt_amt["+acfreight+"]");
					System.out.println("curr_code__ins-->insurance_amt["+acinsurance+"]");
					System.out.println("curr_code__ins-->clearing_charges["+acclearing+"]");
					System.out.println("curr_code__ins-->other_charges["+lcother1+"]");
					sql="select a.curr_code from finent a, site b where b.fin_entity = a.fin_entity and b.site_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lssitecode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						lscurrcodebase = checkNull(rs.getString(1));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if(lscurrcode !=null && lscurrcode.trim().length()>0)
					{
						lcexchrate=fincommon.getDailyExchRateSellBuy(lscurrcode, lscurrcodebase, lssitecode, Effdate1, "B", conn);
						valueXmlString.append("<exch_rate__ins protect =\"1\">").append("<![CDATA[" + lcexchrate + "]]>").append("</exch_rate__ins>");
					}
					//commented  by-Monika-29-may-2019
					/*
				String lcexchrate3="",lcexchrate4="",lcexchrate5="",lcexchrate6="";
				lcexchrate3 = checkNull(genericUtility.getColumnValue( "exch_rate__frt", dom));
				exchratefrt=(lcexchrate3 == null ||  lcexchrate3.trim().length() == 0)  ?0:Double.parseDouble(lcexchrate3);
				lcexchrate4 = checkNull(genericUtility.getColumnValue( "exch_rate__ins", dom ));

				exchratefrtins=(lcexchrate4==null ||  lcexchrate4.trim().length() == 0)?0:Double.parseDouble(lcexchrate4);
				lcexchrate5 = checkNull(genericUtility.getColumnValue( "exch_rate__clr", dom ));

				exchratefrtclr=(lcexchrate3==null ||  lcexchrate5.trim().length() == 0) ?0:Double.parseDouble(lcexchrate5);
				lcexchrate6 = checkNull(genericUtility.getColumnValue( "exch_rate__othch", dom ));

				exchratetoth=(lcexchrate4==null ||  lcexchrate6.trim().length() == 0) ?0:Double.parseDouble(lcexchrate6);*/
					//String lcadditionalcost1= toTalCostAddition(acfreight,acinsurance,acclearing,acother,lcexchrate,exchratefrtins,exchratefrtclr,exchratetoth ,conn);

					//changes-made by-Monika-29-may-2019
					lcadditionalcost1= toTalCostAddition(dom,conn);
					System.out.println("curr_code__ins-->total_addl_cost["+lcadditionalcost1+"]");
					valueXmlString.append("<total_addl_cost>").append("<![CDATA[" + lcadditionalcost1 + "]]>").append("</total_addl_cost>");
				}
				else if (currentColumn.trim().equalsIgnoreCase("curr_code__othch"))
				{
					String Effdate1="";
					Timestamp effdate=null;
					String lcadditionalcost1="";
					String lcexchrate1="",lcfreight1="",lcinsurance1="",lcclearing1="",lcother1="";
					double acfreight=0,acinsurance=0,acclearing=0,acother=0;
					double exchratefrt=0,exchratefrtins=0,exchratefrtclr=0,exchratetoth=0;
					lscurrcode = genericUtility.getColumnValue( "curr_code__othch", dom );
					lssitecode = genericUtility.getColumnValue( "site_code", dom );
					Effdate1 = checkNull(genericUtility.getColumnValue("eff_date", dom));
					lcfreight1 =  checkNull(genericUtility.getColumnValue( "frt_amt", dom ));

					acfreight= (lcfreight1==null ||  lcfreight1.trim().length() == 0)?0:Double.parseDouble(lcfreight1);
					lcinsurance1 = checkNull( genericUtility.getColumnValue( "insurance_amt", dom ));

					acinsurance=(lcinsurance1==null ||  lcinsurance1.trim().length() == 0)?0:Double.parseDouble(lcinsurance1);
					lcclearing1 = checkNull( genericUtility.getColumnValue( "clearing_charges", dom ));
					acclearing=lcclearing1==null ?0:Double.parseDouble(lcclearing1);
					lcother1 = checkNull( genericUtility.getColumnValue( "other_charges", dom ));

					acother=(lcother1==null ||  lcother1.trim().length() == 0) ?0:Double.parseDouble(lcother1);       
					System.out.println("curr_code__othch-->frt_amt["+acfreight+"]");
					System.out.println("curr_code__othch-->insurance_amt["+acinsurance+"]");
					System.out.println("curr_code__othch-->clearing_charges["+acclearing+"]");
					System.out.println("curr_code__othch-->other_charges["+lcother1+"]");
					sql="select a.curr_code from finent a, site b where b.fin_entity = a.fin_entity and b.site_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lssitecode);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						lscurrcodebase = checkNull(rs.getString(1));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if(lscurrcode !=null && lscurrcode.trim().length()>0)
					{
						lcexchrate=fincommon.getDailyExchRateSellBuy(lscurrcode, lscurrcodebase, lssitecode, Effdate1, "B", conn);
						valueXmlString.append("<exch_rate__othch protect =\"1\">").append("<![CDATA[" + lcexchrate + "]]>").append("</exch_rate__othch>");
					}
					/*commented by-monika-on-29-may-2019
				String lcexchrate3="",lcexchrate4="",lcexchrate5="",lcexchrate6="";
				lcexchrate3 = checkNull(genericUtility.getColumnValue( "exch_rate__frt", dom));	
				exchratefrt=(lcexchrate3==null ||  lcexchrate3.trim().length() == 0)?0:Double.parseDouble(lcexchrate3);


				lcexchrate4 = checkNull(checkNull(genericUtility.getColumnValue( "exch_rate__ins", dom )));
				exchratefrtins=(lcexchrate4==null ||  lcexchrate4.trim().length() == 0) ?0:Double.parseDouble(lcexchrate4);
				lcexchrate5 = checkNull(genericUtility.getColumnValue( "exch_rate__clr", dom ));
				exchratefrtclr=(lcexchrate3==null ||  lcexchrate5.trim().length() == 0) ?0:Double.parseDouble(lcexchrate5);
				lcexchrate6 = checkNull(genericUtility.getColumnValue( "exch_rate__othch", dom ));
				exchratetoth=(lcexchrate4==null  ||  lcexchrate6.trim().length() == 0)?0:Double.parseDouble(lcexchrate6);
				lcadditionalcost1= toTalCostAddition(dom,dom1,dom2,xtraParams,conn);*/
					lcadditionalcost1= toTalCostAddition(dom,conn);
					System.out.println("curr_code__ins-->total_addl_cost["+lcadditionalcost1+"]");
					valueXmlString.append("<total_addl_cost>").append("<![CDATA[" + lcadditionalcost1 + "]]>").append("</total_addl_cost>");
				}
				else if (currentColumn.trim().equalsIgnoreCase("curr_code__clr"))
				{
					String Effdate1="";
					Timestamp effdate=null;
					String lcadditionalcost1="";
					String lcexchrate1="",lcfreight1="",lcinsurance1="",lcclearing1="",lcother1="";
					double acfreight=0,acinsurance=0,acclearing=0,acother=0;
					double exchratefrt=0,exchratefrtins=0,exchratefrtclr=0,exchratetoth=0;
					lscurrcode =genericUtility.getColumnValue( "curr_code__clr", dom ); 
					lssitecode =genericUtility.getColumnValue( "site_code", dom ); 
					Effdate1 =genericUtility.getColumnValue("eff_date", dom);
					lcfreight1 = checkNull( genericUtility.getColumnValue( "frt_amt", dom ));

					acfreight=(lcfreight1==null ||  lcfreight1.trim().length() == 0) ?0:Double.parseDouble(lcfreight1);
					lcinsurance1 =  checkNull(genericUtility.getColumnValue( "insurance_amt", dom ));

					acinsurance=(lcinsurance1==null ||  lcinsurance1.trim().length() == 0) ?0:Double.parseDouble(lcinsurance1);
					lcclearing1 =  checkNull(genericUtility.getColumnValue( "clearing_charges", dom ));

					acclearing=(lcclearing1==null ||  lcclearing1.trim().length() == 0) ?0:Double.parseDouble(lcclearing1);
					lcother1 =  checkNull(genericUtility.getColumnValue( "other_charges", dom ));

					acother=(lcother1==null  ||  lcother1.trim().length() == 0)?0:Double.parseDouble(lcother1);
					System.out.println("curr_code__clr-->frt_amt["+acfreight+"]");
					System.out.println("curr_code__clr-->insurance_amt["+acinsurance+"]");
					System.out.println("curr_code__clr-->clearing_charges["+acclearing+"]");
					System.out.println("curr_code__clr-->other_charges["+lcother1+"]");
					effdate = Timestamp.valueOf(genericUtility.getValidDateString(Effdate1.toString(),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+ " 00:00:00.0");
					sql="select a.curr_code from finent a, site b where b.fin_entity = a.fin_entity and b.site_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lssitecode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lscurrcodebase = checkNull(rs.getString(1));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if(lscurrcode !=null && lscurrcode.trim().length()>0)
					{
						lcexchrate=fincommon.getDailyExchRateSellBuy(lscurrcode, lscurrcodebase, lssitecode, Effdate1, "B", conn);
						valueXmlString.append("<exch_rate__clr protect =\"1\">").append("<![CDATA[" + lcexchrate + "]]>").append("</exch_rate__clr>");
					}
					/* commented-by-Monika-29-may-2019
				String lcexchrate3="",lcexchrate4="",lcexchrate5="",lcexchrate6="";
				lcexchrate3 = checkNull(genericUtility.getColumnValue( "exch_rate__frt", dom));

				exchratefrt=(lcexchrate3==null ||  lcexchrate3.trim().length() == 0)?0:Double.parseDouble(lcexchrate3);
				lcexchrate4 = checkNull(genericUtility.getColumnValue( "exch_rate__ins", dom ));

				exchratefrtins=(lcexchrate4==null ||  lcexchrate4.trim().length() == 0)?0:Double.parseDouble(lcexchrate4);
				lcexchrate5 = checkNull(genericUtility.getColumnValue( "exch_rate__clr", dom ));

				exchratefrtclr=(lcexchrate3==null ||  lcexchrate5.trim().length() == 0) ?0:Double.parseDouble(lcexchrate5);
				lcexchrate6 = checkNull(genericUtility.getColumnValue( "exch_rate__othch", dom ));

				exchratetoth=(lcexchrate4==null ||  lcexchrate6.trim().length() == 0) ?0:Double.parseDouble(lcexchrate6);
				String lcadditionalcost1= toTalCostAddition(acfreight,acinsurance,acclearing,acother,lcexchrate,exchratefrtins,exchratefrtclr,exchratetoth ,conn);
					 */
					//changes made by-Monika-29-may-2019
					lcadditionalcost1= toTalCostAddition(dom,conn);//end
					System.out.println("curr_code__ins-->total_addl_cost["+lcadditionalcost1+"]");
					valueXmlString.append("<total_addl_cost>").append("<![CDATA[" + lcadditionalcost1 + "]]>").append("</total_addl_cost>");
				}
				else if (currentColumn.trim().equalsIgnoreCase("exch_rate__frt"))
				{
					//commented-by-monika-29-05-2019
					/*String Effdate1="";
				Timestamp effdate=null;
				String lcadditionalcost1="";
				String lcexchrate1="",lcfreight1="",lcinsurance1="",lcclearing1="",lcother1="";
				double acfreight=0,acinsurance=0,acclearing=0,acother=0;
				double exchratefrt=0,exchratefrtins=0,exchratefrtclr=0,exchratetoth=0;
				lscurrcode =genericUtility.getColumnValue( "curr_code__frt", dom ); 
	            lssitecode =genericUtility.getColumnValue( "site_code", dom ); 
		        Effdate1 =genericUtility.getColumnValue("eff_date", dom);
		        lcfreight1 = checkNull( genericUtility.getColumnValue( "frt_amt", dom ));
		        if(lcfreight1 == null ||  lcfreight1.trim().length() == 0)
				{
		        	lcfreight1 = "0" ;
				}
	            acfreight=lcfreight1==null ?0:Double.parseDouble(lcfreight1);
	            lcinsurance1 =  checkNull(genericUtility.getColumnValue( "insurance_amt", dom ));
	            if(lcinsurance1 == null ||  lcinsurance1.trim().length() == 0)
				{
	            	lcinsurance1 = "0" ;
				}
	            acinsurance=lcinsurance1==null ?0:Double.parseDouble(lcinsurance1);
	            lcclearing1 =  checkNull(genericUtility.getColumnValue( "clearing_charges", dom ));
	            if(lcclearing1 == null ||  lcclearing1.trim().length() == 0)
				{
	            	lcclearing1 = "0" ;
				}
	            acclearing=lcclearing1==null ?0:Double.parseDouble(lcclearing1);
	            lcother1 =  checkNull(genericUtility.getColumnValue( "other_charges", dom ));
	            if(lcother1 == null ||  lcother1.trim().length() == 0)
				{
	            	lcother1 = "0" ;
				}
	            acother=lcother1==null ?0:Double.parseDouble(lcother1);
	            System.out.println("exch_rate__frt-->frt_amt["+acfreight+"]");
	            System.out.println("exch_rate__frt-->insurance_amt["+acinsurance+"]");
	            System.out.println("exch_rate__frt-->clearing_charges["+acclearing+"]");
	            System.out.println("exch_rate__frt-->other_charges["+lcother1+"]");
		        effdate = Timestamp.valueOf(genericUtility.getValidDateString(Effdate1.toString(),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+ " 00:00:00.0");
		        sql="select a.curr_code from finent a, site b where b.fin_entity = a.fin_entity and b.site_code = ? ";
		        pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lssitecode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					lscurrcodebase = checkNull(rs.getString(1));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(lscurrcode !=null && lscurrcode.trim().length()>0)
				{
					 lcexchrate=fincommon.getDailyExchRateSellBuy(lscurrcode, lscurrcodebase, lssitecode, Effdate1, "B", conn);
					 valueXmlString.append("<exch_rate__frt protect =\"1\">").append("<![CDATA[" + lcexchrate + "]]>").append("</exch_rate__frt>");
					// gbf_exchrate_protect(ls_currcode,ls_sitecode,'exch_rate__clr') remaining  in code
				}
				String lcexchrate3="",lcexchrate4="",lcexchrate5="",lcexchrate6="";
				lcexchrate3 = checkNull(genericUtility.getColumnValue( "exch_rate__frt", dom));	
				if(lcexchrate3 == null ||  lcexchrate3.trim().length() == 0)
				{
					lcexchrate3 = "0" ;
				}
				exchratefrt=lcexchrate3==null ?0:Double.parseDouble(lcexchrate3);
				lcexchrate4 = checkNull(genericUtility.getColumnValue( "exch_rate__ins", dom ));
				if(lcexchrate4 == null ||  lcexchrate4.trim().length() == 0)
				{
					lcexchrate4 = "0" ;
				}
				exchratefrtins=lcexchrate4==null ?0:Double.parseDouble(lcexchrate4);
				lcexchrate5 = checkNull(genericUtility.getColumnValue( "exch_rate__clr", dom ));
				if(lcexchrate5 == null ||  lcexchrate5.trim().length() == 0)
				{
					lcexchrate5 = "0" ;
				}
				exchratefrtclr=lcexchrate3==null ?0:Double.parseDouble(lcexchrate5);
				lcexchrate6 = checkNull(genericUtility.getColumnValue( "exch_rate__othch", dom ));
				if(lcexchrate6 == null ||  lcexchrate6.trim().length() == 0)
				{
					lcexchrate6 = "0" ;
				}
				exchratetoth=lcexchrate4==null ?0:Double.parseDouble(lcexchrate6);*/
					//	lcadditionalcost1= toTalCostAddition(acfreight,acinsurance,acclearing,acother,lcexchrate,exchratefrtins,exchratefrtclr,exchratetoth ,conn);
					String	lcadditionalcost1=toTalCostAddition(dom,conn);//end
					System.out.println("curr_code__ins-->total_addl_cost["+lcadditionalcost1+"]");
					valueXmlString.append("<total_addl_cost>").append("<![CDATA[" + lcadditionalcost1 + "]]>").append("</total_addl_cost>");
				}
				else if (currentColumn.trim().equalsIgnoreCase("exch_rate__ins"))
				{
					//commented-by-monika-29-may-2019
					/*String Effdate1="";
				Timestamp effdate=null;
				String lcadditionalcost1="";
				String lcexchrate1="",lcfreight1="",lcinsurance1="",lcclearing1="",lcother1="";
				double acfreight=0,acinsurance=0,acclearing=0,acother=0;
				double exchratefrt=0,exchratefrtins=0,exchratefrtclr=0,exchratetoth=0;
				lscurrcode =genericUtility.getColumnValue( "curr_code__ins", dom ); 
	            lssitecode =genericUtility.getColumnValue( "site_code", dom ); 
		        Effdate1 =genericUtility.getColumnValue("eff_date", dom);
		        lcfreight1 =  checkNull(genericUtility.getColumnValue( "frt_amt", dom ));
		        if(lcfreight1 == null ||  lcfreight1.trim().length() == 0)
				{
		        	lcfreight1 = "0" ;
				}
	            acfreight=lcfreight1==null ?0:Double.parseDouble(lcfreight1);
	            lcinsurance1 =  checkNull(genericUtility.getColumnValue( "insurance_amt", dom ));
	            if(lcinsurance1 == null ||  lcinsurance1.trim().length() == 0)
				{
	            	lcinsurance1 = "0" ;
				}
	            acinsurance=lcinsurance1==null ?0:Double.parseDouble(lcinsurance1);
	            lcclearing1 = checkNull( genericUtility.getColumnValue( "clearing_charges", dom ));
	            if(lcclearing1 == null ||  lcclearing1.trim().length() == 0)
				{
	            	lcclearing1 = "0" ;
				}
	            acclearing=lcclearing1==null ?0:Double.parseDouble(lcclearing1);
	            lcother1 =  checkNull(genericUtility.getColumnValue( "other_charges", dom ));
	            if(lcother1 == null ||  lcother1.trim().length() == 0)
				{
	            	lcother1 = "0" ;
				}
	            acother=lcother1==null ?0:Double.parseDouble(lcother1);
	            System.out.println("exch_rate__ins-->frt_amt["+acfreight+"]");
	            System.out.println("exch_rate__ins-->insurance_amt["+acinsurance+"]");
	            System.out.println("exch_rate__ins-->clearing_charges["+acclearing+"]");
	            System.out.println("exch_rate__ins-->other_charges["+lcother1+"]");
		        effdate = Timestamp.valueOf(genericUtility.getValidDateString(Effdate1.toString(),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+ " 00:00:00.0");
		        sql="select a.curr_code from finent a, site b where b.fin_entity = a.fin_entity and b.site_code = ? ";
		        pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lssitecode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					lscurrcodebase = checkNull(rs.getString(1));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(lscurrcode !=null && lscurrcode.trim().length()>0)
				{
					 lcexchrate=fincommon.getDailyExchRateSellBuy(lscurrcode, lscurrcodebase, lssitecode, Effdate1, "B", conn);
					 valueXmlString.append("<exch_rate__ins protect =\"1\">").append("<![CDATA[" + lcexchrate + "]]>").append("</exch_rate__ins>");
					// gbf_exchrate_protect(ls_currcode,ls_sitecode,'exch_rate__clr') remaining  in code
				}
				String lcexchrate3="",lcexchrate4="",lcexchrate5="",lcexchrate6="";
				lcexchrate3 = checkNull(genericUtility.getColumnValue( "exch_rate__frt", dom));
				if(lcexchrate3 == null ||  lcexchrate3.trim().length() == 0)
				{
					lcexchrate3 = "0" ;
				}
				exchratefrt=lcexchrate3==null ?0:Double.parseDouble(lcexchrate3);
				lcexchrate4 = checkNull(genericUtility.getColumnValue( "exch_rate__ins", dom ));
				if(lcexchrate4 == null ||  lcexchrate4.trim().length() == 0)
				{
					lcexchrate4 = "0" ;
				}
				exchratefrtins=lcexchrate4==null ?0:Double.parseDouble(lcexchrate4);
				lcexchrate5 = checkNull(genericUtility.getColumnValue( "exch_rate__clr", dom ));
				if(lcexchrate5 == null ||  lcexchrate5.trim().length() == 0)
				{
					lcexchrate5 = "0" ;
				}
				exchratefrtclr=lcexchrate3==null ?0:Double.parseDouble(lcexchrate5);
				lcexchrate6 = checkNull(genericUtility.getColumnValue( "exch_rate__othch", dom ));
				if(lcexchrate6 == null ||  lcexchrate6.trim().length() == 0)
				{
					lcexchrate6 = "0" ;
				}
				exchratetoth=lcexchrate4==null ?0:Double.parseDouble(lcexchrate6);*/
					//lcadditionalcost1= toTalCostAddition(acfreight,acinsurance,acclearing,acother,exchratefrt,lcexchrate,exchratefrtclr,exchratetoth ,conn);
					//changes-done-by-Monika-29-15-2019
					String lcadditionalcost1=toTalCostAddition(dom,conn);//end
					System.out.println("curr_code__ins-->total_addl_cost["+lcadditionalcost1+"]");
					valueXmlString.append("<total_addl_cost>").append("<![CDATA[" + lcadditionalcost1 + "]]>").append("</total_addl_cost>");
				}
				else if (currentColumn.trim().equalsIgnoreCase("exch_rate__clr"))
				{//commented by-monika-29-may-2019
					/*String Effdate1="";
				Timestamp effdate=null;
				String lcadditionalcost1="";
				String lcexchrate1="",lcfreight1="",lcinsurance1="",lcclearing1="",lcother1="";
				double acfreight=0,acinsurance=0,acclearing=0,acother=0;
				double exchratefrt=0,exchratefrtins=0,exchratefrtclr=0,exchratetoth=0;
				lscurrcode =genericUtility.getColumnValue( "curr_code__clr", dom ); 
	            lssitecode =genericUtility.getColumnValue( "site_code", dom ); 
		        Effdate1 =genericUtility.getColumnValue("eff_date", dom);
		        lcfreight1 =  checkNull(genericUtility.getColumnValue( "frt_amt", dom ));
		        if(lcfreight1 == null ||  lcfreight1.trim().length() == 0)
				{
		        	lcfreight1 = "0" ;
				}
	            acfreight=lcfreight1==null ?0:Double.parseDouble(lcfreight1);
	            lcinsurance1 = checkNull( genericUtility.getColumnValue( "insurance_amt", dom ));
	            if(lcinsurance1 == null ||  lcinsurance1.trim().length() == 0)
				{
	            	lcinsurance1 = "0" ;
				}
	            acinsurance=lcinsurance1==null ?0:Double.parseDouble(lcinsurance1);
	            lcclearing1 = checkNull( genericUtility.getColumnValue( "clearing_charges", dom ));
	            if(lcclearing1 == null ||  lcclearing1.trim().length() == 0)
				{
	            	lcclearing1 = "0" ;
				}
	            acclearing=lcclearing1==null ?0:Double.parseDouble(lcclearing1);
	            lcother1 =  checkNull(genericUtility.getColumnValue( "other_charges", dom ));

	            if(lcother1 == null ||  lcother1.trim().length() == 0)
				{
	            	lcother1 = "0" ;
				}
	            acother=lcother1==null ?0:Double.parseDouble(lcother1);
	            System.out.println("curr_code__clr-->frt_amt["+acfreight+"]");
	            System.out.println("curr_code__clr-->insurance_amt["+acinsurance+"]");
	            System.out.println("curr_code__clr-->clearing_charges["+acclearing+"]");
	            System.out.println("curr_code__clr-->other_charges["+lcother1+"]");
		        effdate = Timestamp.valueOf(genericUtility.getValidDateString(Effdate1.toString(),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+ " 00:00:00.0");
		        sql="select a.curr_code from finent a, site b where b.fin_entity = a.fin_entity and b.site_code = ? ";
		        pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lssitecode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					lscurrcodebase = checkNull(rs.getString(1));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(lscurrcode !=null && lscurrcode.trim().length()>0)
				{
					 lcexchrate=fincommon.getDailyExchRateSellBuy(lscurrcode, lscurrcodebase, lssitecode, Effdate1, "B", conn);
					 valueXmlString.append("<exch_rate__clr protect =\"1\">").append("<![CDATA[" + lcexchrate + "]]>").append("</exch_rate__clr>");
					// gbf_exchrate_protect(ls_currcode,ls_sitecode,'exch_rate__clr') remaining  in code
				}
				String lcexchrate3="",lcexchrate4="",lcexchrate5="",lcexchrate6="";
				lcexchrate3 = checkNull(genericUtility.getColumnValue( "exch_rate__frt", dom));
				if(lcexchrate3 == null ||  lcexchrate3.trim().length() == 0)
				{
					lcexchrate3 = "0" ;
				}
				exchratefrt=lcexchrate3==null ?0:Double.parseDouble(lcexchrate3);
				lcexchrate4 = checkNull(genericUtility.getColumnValue( "exch_rate__ins", dom ));
				if(lcexchrate4 == null ||  lcexchrate4.trim().length() == 0)
				{
					lcexchrate4 = "0" ;
				}
				exchratefrtins=lcexchrate4==null ?0:Double.parseDouble(lcexchrate4);
				lcexchrate5 = checkNull(genericUtility.getColumnValue( "exch_rate__clr", dom ));
				if(lcexchrate5 == null ||  lcexchrate5.trim().length() == 0)
				{
					lcexchrate5 = "0" ;
				}
				exchratefrtclr=lcexchrate3==null ?0:Double.parseDouble(lcexchrate5);
				lcexchrate6 = checkNull(genericUtility.getColumnValue( "exch_rate__othch", dom ));
				if(lcexchrate6 == null ||  lcexchrate6.trim().length() == 0)
				{
					lcexchrate6 = "0" ;
				}
				exchratetoth=lcexchrate4==null ?0:Double.parseDouble(lcexchrate6);
				lcadditionalcost1= toTalCostAddition(acfreight,acinsurance,acclearing,acother,exchratefrt,exchratefrtins,lcexchrate,exchratetoth ,conn);*/
					//changes-made-by -monika-29-may-2019
					String lcadditionalcost1= toTalCostAddition(dom,conn);//end
					System.out.println("curr_code__ins-->total_addl_cost["+lcadditionalcost1+"]");
					valueXmlString.append("<total_addl_cost>").append("<![CDATA[" + lcadditionalcost1 + "]]>").append("</total_addl_cost>");
				}
				else if (currentColumn.trim().equalsIgnoreCase("insurance_amt"))
				{
					//commented-by-Monika-29-may-2019
					/*String Effdate1="";
				Timestamp effdate=null;
				String lcadditionalcost1="";
				String lcexchrate1="",lcfreight1="",lcinsurance1="",lcclearing1="",lcother1="";
				double acfreight=0,acinsurance=0,acclearing=0,acother=0;
				double exchratefrt=0,exchratefrtins=0,exchratefrtclr=0,exchratetoth=0;
				lscurrcode =genericUtility.getColumnValue( "curr_code__ins", dom ); 
	            lssitecode =genericUtility.getColumnValue( "site_code", dom ); 
		        Effdate1 =genericUtility.getColumnValue("eff_date", dom);
		        lcfreight1 =  checkNull(genericUtility.getColumnValue( "frt_amt", dom ));
		        if(lcfreight1 == null ||  lcfreight1.trim().length() == 0)
				{
		        	lcfreight1 = "0" ;
				}
	            acfreight=lcfreight1==null ?0:Double.parseDouble(lcfreight1);
	            lcinsurance1 =   checkNull(genericUtility.getColumnValue( "insurance_amt", dom ));
	            if(lcinsurance1 == null ||  lcinsurance1.trim().length() == 0)
				{
	            	lcinsurance1 = "0" ;
				}
	            acinsurance=lcinsurance1==null ?0:Double.parseDouble(lcinsurance1);
	            lcclearing1 =   checkNull(genericUtility.getColumnValue( "clearing_charges", dom ));
	            if(lcclearing1 == null ||  lcclearing1.trim().length() == 0)
				{
	            	lcclearing1 = "0" ;
				}
	            acclearing=lcclearing1==null ?0:Double.parseDouble(lcclearing1);
	            lcother1 =   checkNull(genericUtility.getColumnValue( "other_charges", dom ));
	            if(lcother1 == null ||  lcother1.trim().length() == 0)
				{
	            	lcother1 = "0" ;
				}
	            acother=lcother1==null ?0:Double.parseDouble(lcother1);
	            System.out.println("insurance_amt-->frt_amt["+acfreight+"]");
	            System.out.println("insurance_amt-->insurance_amt["+acinsurance+"]");
	            System.out.println("insurance_amt-->clearing_charges["+acclearing+"]");
	            System.out.println("insurance_amt-->other_charges["+lcother1+"]");
	            effdate = Timestamp.valueOf(genericUtility.getValidDateString(Effdate1.toString(),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+ " 00:00:00.0");
		        sql="select a.curr_code from finent a, site b where b.fin_entity = a.fin_entity and b.site_code = ? ";
		        pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lssitecode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					lscurrcodebase = checkNull(rs.getString(1));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(lscurrcode !=null && lscurrcode.trim().length()>0)
				{
					 lcexchrate=fincommon.getDailyExchRateSellBuy(lscurrcode, lscurrcodebase, lssitecode, Effdate1, "B", conn);
					 valueXmlString.append("<exch_rate__ins protect =\"1\">").append("<![CDATA[" + lcexchrate + "]]>").append("</exch_rate__ins>");
				}
				String lcexchrate3="",lcexchrate4="",lcexchrate5="",lcexchrate6="";
				lcexchrate3 = checkNull(genericUtility.getColumnValue( "exch_rate__frt", dom));
				if(lcexchrate3 == null ||  lcexchrate3.trim().length() == 0)
				{
					lcexchrate3 = "0" ;
				}
				exchratefrt=lcexchrate3==null ?0:Double.parseDouble(lcexchrate3);
				lcexchrate4 = checkNull(genericUtility.getColumnValue( "exch_rate__ins", dom ));
				if(lcexchrate4 == null ||  lcexchrate4.trim().length() == 0)
				{
					lcexchrate4 = "0" ;
				}
				exchratefrtins=lcexchrate4==null ?0:Double.parseDouble(lcexchrate4);
				lcexchrate5 = checkNull(genericUtility.getColumnValue( "exch_rate__clr", dom ));
				if(lcexchrate5 == null ||  lcexchrate5.trim().length() == 0)
				{
					lcexchrate5 = "0" ;
				}
				exchratefrtclr=lcexchrate3==null ?0:Double.parseDouble(lcexchrate5);
				lcexchrate6 = checkNull(genericUtility.getColumnValue( "exch_rate__othch", dom ));
				if(lcexchrate6 == null ||  lcexchrate6.trim().length() == 0)
				{
					lcexchrate6 = "0" ;
				}
				exchratetoth=lcexchrate4==null ?0:Double.parseDouble(lcexchrate6);
				lcadditionalcost1= toTalCostAddition(acfreight,acinsurance,acclearing,acother,exchratefrt,lcexchrate,exchratefrtclr,exchratetoth ,conn);*/
					//changes-made by-Monika-29-may-2019
					String lcadditionalcost1= toTalCostAddition(dom,conn);//end
					System.out.println("curr_code__ins-->total_addl_cost["+lcadditionalcost1+"]");
					valueXmlString.append("<total_addl_cost>").append("<![CDATA[" + lcadditionalcost1 + "]]>").append("</total_addl_cost>");
				}
				else if (currentColumn.trim().equalsIgnoreCase("frt_amt"))
				{
					//commented by-Monika-29-05-2019
					/*String Effdate1="";
				Timestamp effdate=null;
				String lcadditionalcost1="";
				String lcexchrate1="",lcfreight1="",lcinsurance1="",lcclearing1="",lcother1="";
				double acfreight=0,acinsurance=0,acclearing=0,acother=0;
				double exchratefrt=0,exchratefrtins=0,exchratefrtclr=0,exchratetoth=0;
				lscurrcode =genericUtility.getColumnValue( "curr_code__frt", dom ); 
	            lssitecode =genericUtility.getColumnValue( "site_code", dom ); 
		        Effdate1 =genericUtility.getColumnValue("eff_date", dom);
		        lcfreight1 =  checkNull(genericUtility.getColumnValue( "frt_amt", dom ));
		        if(lcfreight1 == null ||  lcfreight1.trim().length() == 0)
				{
		        	lcfreight1 = "0" ;
				}
	            acfreight=lcfreight1==null ?0:Double.parseDouble(lcfreight1);
	            lcinsurance1 = checkNull( genericUtility.getColumnValue( "insurance_amt", dom ));
	            if(lcinsurance1 == null ||  lcinsurance1.trim().length() == 0)
				{
	            	lcinsurance1 = "0" ;
				}
	            acinsurance=lcinsurance1==null ?0:Double.parseDouble(lcinsurance1);
	            lcclearing1 =  checkNull(genericUtility.getColumnValue( "clearing_charges", dom ));
	            if(lcclearing1 == null ||  lcclearing1.trim().length() == 0)
				{
	            	lcclearing1 = "0" ;
				}
	            acclearing=lcclearing1==null ?0:Double.parseDouble(lcclearing1);
	            lcother1 =  checkNull(genericUtility.getColumnValue( "other_charges", dom ));
	            if(lcother1 == null ||  lcother1.trim().length() == 0)
				{
	            	lcother1 = "0" ;
				}
	            acother=lcother1==null ?0:Double.parseDouble(lcother1);
	            System.out.println("frt_amt-->frt_amt["+acfreight+"]");
	            System.out.println("frt_amt-->insurance_amt["+acinsurance+"]");
	            System.out.println("frt_amt-->clearing_charges["+acclearing+"]");
	            System.out.println("frt_amt-->other_charges["+lcother1+"]");
		        effdate = Timestamp.valueOf(genericUtility.getValidDateString(Effdate1.toString(),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+ " 00:00:00.0");
		        sql="select a.curr_code from finent a, site b where b.fin_entity = a.fin_entity and b.site_code = ? ";
		        pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lssitecode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					lscurrcodebase = checkNull(rs.getString(1));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(lscurrcode !=null && lscurrcode.trim().length()>0)
				{
					 lcexchrate=fincommon.getDailyExchRateSellBuy(lscurrcode, lscurrcodebase, lssitecode, Effdate1, "B", conn);
					 valueXmlString.append("<exch_rate__frt protect =\"1\">").append("<![CDATA[" + lcexchrate + "]]>").append("</exch_rate__frt>");

				}
				String lcexchrate3="",lcexchrate4="",lcexchrate5="",lcexchrate6="";
				lcexchrate3 = checkNull(genericUtility.getColumnValue( "exch_rate__frt", dom));	
				if(lcexchrate3 == null ||  lcexchrate3.trim().length() == 0)
				{
					lcexchrate3 = "0" ;
				}
				exchratefrt=lcexchrate3==null ?0:Double.parseDouble(lcexchrate3);
				lcexchrate4 =checkNull(genericUtility.getColumnValue( "exch_rate__ins", dom ));
				if(lcexchrate4 == null ||  lcexchrate4.trim().length() == 0)
				{
					lcexchrate4 = "0" ;
				}
				exchratefrtins=lcexchrate4==null ?0:Double.parseDouble(lcexchrate4);
				lcexchrate5 = checkNull(genericUtility.getColumnValue( "exch_rate__clr", dom ));
				if(lcexchrate5 == null ||  lcexchrate5.trim().length() == 0)
				{
					lcexchrate5 = "0" ;
				}
				exchratefrtclr=lcexchrate3==null ?0:Double.parseDouble(lcexchrate5);
				lcexchrate6 = checkNull(genericUtility.getColumnValue( "exch_rate__othch", dom ));
				if(lcexchrate6 == null ||  lcexchrate6.trim().length() == 0)
				{
					lcexchrate6 = "0" ;
				}
				exchratetoth=lcexchrate4==null ?0:Double.parseDouble(lcexchrate6);
				lcadditionalcost1= toTalCostAddition(acfreight,acinsurance,acclearing,acother,lcexchrate,exchratefrtins,exchratefrtclr,exchratetoth ,conn);*/
					//changes -made by-29-may-2019
					String lcadditionalcost1=toTalCostAddition(dom,conn);//end

					valueXmlString.append("<total_addl_cost>").append("<![CDATA[" + lcadditionalcost1 + "]]>").append("</total_addl_cost>");
				}
				else if (currentColumn.trim().equalsIgnoreCase("clearing_charges"))
				{/*
				commented by-Monika-29-may-2019
				String Effdate1="";
				Timestamp effdate=null;
				String lcadditionalcost1="";
				String lcexchrate1="",lcfreight1="",lcinsurance1="",lcclearing1="",lcother1="";
				double acfreight=0,acinsurance=0,acclearing=0,acother=0;
				double exchratefrt=0,exchratefrtins=0,exchratefrtclr=0,exchratetoth=0;
				lscurrcode =genericUtility.getColumnValue( "curr_code__clr", dom ); 
	            lssitecode =genericUtility.getColumnValue( "site_code", dom ); 
		        Effdate1 =genericUtility.getColumnValue("eff_date", dom);
		        lcfreight1 =  checkNull(genericUtility.getColumnValue( "frt_amt", dom ));
		        if(lcfreight1 == null ||  lcfreight1.trim().length() == 0)
				{
		        	lcfreight1 = "0" ;
				}
	            acfreight=lcfreight1==null ?0:Double.parseDouble(lcfreight1);
	            lcinsurance1 = checkNull( genericUtility.getColumnValue( "insurance_amt", dom ));
	            if(lcinsurance1 == null ||  lcinsurance1.trim().length() == 0)
				{
	            	lcinsurance1 = "0" ;
				}
	            acinsurance=lcinsurance1==null ?0:Double.parseDouble(lcinsurance1);
	            lcclearing1 =  checkNull(genericUtility.getColumnValue( "clearing_charges", dom ));
	            if(lcclearing1 == null ||  lcclearing1.trim().length() == 0)
				{
	            	lcclearing1 = "0" ;
				}
	            acclearing=lcclearing1==null ?0:Double.parseDouble(lcclearing1);
	            lcother1 =  checkNull(genericUtility.getColumnValue( "other_charges", dom ));
	            if(lcother1 == null ||  lcother1.trim().length() == 0)
				{
	            	lcother1 = "0" ;
				}
	            acother=lcother1==null ?0:Double.parseDouble(lcother1);
	            System.out.println("clearing_charges-->frt_amt["+acfreight+"]");
	            System.out.println("clearing_charges-->insurance_amt["+acinsurance+"]");
	            System.out.println("clearing_charges-->clearing_charges["+acclearing+"]");
	            System.out.println("clearing_charges-->other_charges["+lcother1+"]");
		        effdate = Timestamp.valueOf(genericUtility.getValidDateString(Effdate1.toString(),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+ " 00:00:00.0");
		        sql="select a.curr_code from finent a, site b where b.fin_entity = a.fin_entity and b.site_code = ? ";
		        pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lssitecode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					lscurrcodebase = checkNull(rs.getString(1));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(lscurrcode !=null && lscurrcode.trim().length()>0)
				{
					 lcexchrate=fincommon.getDailyExchRateSellBuy(lscurrcode, lscurrcodebase, lssitecode, Effdate1, "B", conn);
					 valueXmlString.append("<exch_rate__clr protect =\"1\">").append("<![CDATA[" + lcexchrate + "]]>").append("</exch_rate__clr>");

				}
				String lcexchrate3="",lcexchrate4="",lcexchrate5="",lcexchrate6="";
				lcexchrate3 = checkNull(genericUtility.getColumnValue( "exch_rate__frt", dom));	
				if(lcexchrate3 == null ||  lcexchrate3.trim().length() == 0)
				{
					lcexchrate3 = "0" ;
				}
				exchratefrt=lcexchrate3==null ?0:Double.parseDouble(lcexchrate3);
				lcexchrate4 = checkNull(genericUtility.getColumnValue( "exch_rate__ins", dom ));
				if(lcexchrate4 == null ||  lcexchrate4.trim().length() == 0)
				{
					lcexchrate4 = "0" ;
				}
				exchratefrtins=lcexchrate4==null ?0:Double.parseDouble(lcexchrate4);
				lcexchrate5 = checkNull(genericUtility.getColumnValue( "exch_rate__clr", dom ));
				if(lcexchrate5 == null ||  lcexchrate5.trim().length() == 0)
				{
					lcexchrate5 = "0" ;
				}
				exchratefrtclr=lcexchrate3==null ?0:Double.parseDouble(lcexchrate5);
				lcexchrate6 = checkNull(genericUtility.getColumnValue( "exch_rate__othch", dom ));
				if(lcexchrate6 == null ||  lcexchrate6.trim().length() == 0)
				{
					lcexchrate6 = "0" ;
				}
				exchratetoth=lcexchrate4==null ?0:Double.parseDouble(lcexchrate6);*/
					//lcadditionalcost1= toTalCostAddition(acfreight,acinsurance,acclearing,acother,exchratefrt,exchratefrtins,lcexchrate,exchratetoth ,conn);
					//changes-made-by-Monika-29-05-2019
					String lcadditionalcost1=toTalCostAddition(dom,conn);//end
					System.out.println("curr_code__ins-->total_addl_cost["+lcadditionalcost1+"]");
					valueXmlString.append("<total_addl_cost>").append("<![CDATA[" + lcadditionalcost1 + "]]>").append("</total_addl_cost>");
				}
				else if (currentColumn.trim().equalsIgnoreCase("exch_rate__othch"))
				{
					//commented-by-Monika-29-may-2019
					/*String Effdate1="";
				Timestamp effdate=null;
				String lcadditionalcost1="";
				String lcexchrate1="",lcfreight1="",lcinsurance1="",lcclearing1="",lcother1="";
				double acfreight=0,acinsurance=0,acclearing=0,acother=0;
				double exchratefrt=0,exchratefrtins=0,exchratefrtclr=0,exchratetoth=0;
				lscurrcode =genericUtility.getColumnValue( "curr_code__othch", dom ); 
	            lssitecode =genericUtility.getColumnValue( "site_code", dom ); 
		        Effdate1 =genericUtility.getColumnValue("eff_date", dom);
		        lcfreight1 = checkNull( genericUtility.getColumnValue( "frt_amt", dom ));
		        if(lcfreight1 == null ||  lcfreight1.trim().length() == 0)
				{
		        	lcfreight1 = "0" ;
				}
	            acfreight=lcfreight1==null ?0:Double.parseDouble(lcfreight1);
	            lcinsurance1 =  checkNull(genericUtility.getColumnValue( "insurance_amt", dom ));
	            if(lcinsurance1 == null ||  lcinsurance1.trim().length() == 0)
				{
	            	lcinsurance1 = "0" ;
				}
	            acinsurance=lcinsurance1==null ?0:Double.parseDouble(lcinsurance1);
	            lcclearing1 = checkNull( genericUtility.getColumnValue( "clearing_charges", dom ));

	            acclearing=(lcclearing1==null ||  lcclearing1.trim().length() == 0) ?0:Double.parseDouble(lcclearing1);
	            lcother1 =  checkNull(genericUtility.getColumnValue( "other_charges", dom ));
	            if(lcother1 == null ||  lcother1.trim().length() == 0)
				{
	            	lcother1 = "0" ;
				}
	            acother=lcother1==null ?0:Double.parseDouble(lcother1);
	            System.out.println("curr_code__clr-->frt_amt["+acfreight+"]");
	            System.out.println("curr_code__clr-->insurance_amt["+acinsurance+"]");
	            System.out.println("curr_code__clr-->clearing_charges["+acclearing+"]");
	            System.out.println("curr_code__clr-->other_charges["+lcother1+"]");
		        effdate = Timestamp.valueOf(genericUtility.getValidDateString(Effdate1.toString(),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+ " 00:00:00.0");
		        sql="select a.curr_code from finent a, site b where b.fin_entity = a.fin_entity and b.site_code = ? ";
		        pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lssitecode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					lscurrcodebase = checkNull(rs.getString(1));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(lscurrcode !=null && lscurrcode.trim().length()>0)
				{
					 lcexchrate=fincommon.getDailyExchRateSellBuy(lscurrcode, lscurrcodebase, lssitecode, Effdate1, "B", conn);
					 valueXmlString.append("<exch_rate__othch protect =\"1\">").append("<![CDATA[" + lcexchrate + "]]>").append("</exch_rate__othch>");
				}
				String lcexchrate3="",lcexchrate4="",lcexchrate5="",lcexchrate6="";
				lcexchrate3 = checkNull( genericUtility.getColumnValue( "exch_rate__frt", dom));

				lcexchrate3 = (lcexchrate3 == null ||  lcexchrate3.trim().length() == 0) ? "0" : lcexchrate3;

				exchratefrt= (lcexchrate3==null || lcexchrate3.trim().length() == 0) ? 0 :Double.parseDouble(lcexchrate3);
				lcexchrate4 = checkNull( genericUtility.getColumnValue( "exch_rate__ins", dom ));

				exchratefrtins=(lcexchrate4==null ||  lcexchrate4.trim().length() == 0) ?0:Double.parseDouble(lcexchrate4);
				lcexchrate5 = checkNull( genericUtility.getColumnValue( "exch_rate__clr", dom ));
				exchratefrtclr=(lcexchrate3==null  ||  lcexchrate5.trim().length() == 0)?0:Double.parseDouble(lcexchrate5);
				lcexchrate6 = checkNull( genericUtility.getColumnValue( "exch_rate__othch", dom ));

				exchratetoth=(lcexchrate4==null ||  lcother1.trim().length() == 0)?0:Double.parseDouble(lcexchrate6);*/
					//lcadditionalcost1= toTalCostAddition(acfreight,acinsurance,acclearing,acother,exchratefrt,exchratefrtins,exchratefrtclr,lcexchrate ,conn);
					//changes-made by-Monika-29-may-2019
					String lcadditionalcost1=toTalCostAddition(dom,conn);//end
					System.out.println("curr_code__ins-->total_addl_cost["+lcadditionalcost1+"]");
					valueXmlString.append("<total_addl_cost>").append("<![CDATA[" + lcadditionalcost1 + "]]>").append("</total_addl_cost>");
				}
				else if (currentColumn.trim().equalsIgnoreCase("other_charges"))
				{/* commented -by-monika-29-may-2019
					String Effdate1="";
					Timestamp effdate=null;
					String lcadditionalcost1="";
					String lcexchrate1="",lcfreight1="",lcinsurance1="",lcclearing1="",lcother1="";
					double acfreight=0,acinsurance=0,acclearing=0,acother=0;
					double exchratefrt=0,exchratefrtins=0,exchratefrtclr=0,exchratetoth=0;
					lscurrcode =genericUtility.getColumnValue( "curr_code__othch", dom ); 
					lssitecode =genericUtility.getColumnValue( "site_code", dom ); 
					Effdate1 =genericUtility.getColumnValue("eff_date", dom);
					lcfreight1 = checkNull( genericUtility.getColumnValue( "frt_amt", dom ));
					acfreight=(lcfreight1==null || lcfreight1.trim().length() == 0) ?0:Double.parseDouble(lcfreight1);
					lcinsurance1 =  checkNull(genericUtility.getColumnValue( "insurance_amt", dom ));
					acinsurance= (lcinsurance1 ==null || lcinsurance1.trim().length() == 0)?0:Double.parseDouble(lcinsurance1);
					lcclearing1 =  checkNull(genericUtility.getColumnValue( "clearing_charges", dom ));
					acclearing= (lcclearing1==null || lcclearing1.trim().length() == 0) ?0:Double.parseDouble(lcclearing1);
					lcother1 =  checkNull(genericUtility.getColumnValue( "other_charges", dom ));
					acother=lcother1==null ?0:Double.parseDouble(lcother1);
					System.out.println("other_charges-->frt_amt["+acfreight+"]");
					System.out.println("other_charges-->insurance_amt["+acinsurance+"]");
					System.out.println("other_charges-->clearing_charges["+acclearing+"]");
					System.out.println("other_charges-->other_charges["+lcother1+"]");
					effdate = Timestamp.valueOf(genericUtility.getValidDateString(Effdate1.toString(),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+ " 00:00:00.0");
					sql="select a.curr_code from finent a, site b where b.fin_entity = a.fin_entity and b.site_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lssitecode);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lscurrcodebase = checkNull(rs.getString(1));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if(lscurrcode !=null && lscurrcode.trim().length()>0)
					{
						lcexchrate=fincommon.getDailyExchRateSellBuy(lscurrcode, lscurrcodebase, lssitecode, Effdate1, "B", conn);
						System.out.println("lcexchrate["+lcexchrate+"]");
						valueXmlString.append("<exch_rate__othch protect =\"1\">").append("<![CDATA[" + lcexchrate + "]]>").append("</exch_rate__othch>");
					}
					String lcexchrate3="",lcexchrate4="",lcexchrate5="",lcexchrate6="";
					lcexchrate3 = checkNull(genericUtility.getColumnValue( "exch_rate__frt", dom));	
					exchratefrt=(lcexchrate3==null || lcclearing1.trim().length() == 0) ?0:Double.parseDouble(lcexchrate3);
					lcexchrate4 = checkNull(genericUtility.getColumnValue( "exch_rate__ins", dom ));
					exchratefrtins=(lcexchrate4==null || lcexchrate4.trim().length() == 0)?0:Double.parseDouble(lcexchrate4);
					lcexchrate5 = checkNull(genericUtility.getColumnValue( "exch_rate__clr", dom ));
					exchratefrtclr=(lcexchrate5==null || lcexchrate5.trim().length() == 0) ?0:Double.parseDouble(lcexchrate5);
					lcexchrate6 = checkNull(genericUtility.getColumnValue( "exch_rate__othch", dom ));
					exchratetoth=(lcexchrate6==null || lcexchrate6.trim().length() == 0) ?0:Double.parseDouble(lcexchrate6);
					lcadditionalcost1= toTalCostAddition(acfreight,acinsurance,acclearing,acother,lcexchrate,exchratefrtins,exchratefrtclr,exchratetoth ,conn);;*/
					//changes-made by-Monika-29-may-2019
					String lcadditionalcost1= toTalCostAddition(dom,conn);//end
					System.out.println("curr_code__ins-->total_addl_cost["+lcadditionalcost1+"]");
					valueXmlString.append("<total_addl_cost>").append("<![CDATA[" + lcadditionalcost1 + "]]>").append("</total_addl_cost>");
				}
				else if (currentColumn.trim().equalsIgnoreCase("gr_no"))
				{
					String ldtlrdate1="",ldtchallandt1="",grNo="";
					grNo = genericUtility.getColumnValue( "gr_no", dom ); 
					lstrancode = genericUtility.getColumnValue( "tran_code", dom );
					lspurcorder = genericUtility.getColumnValue( "purc_order", dom );
					sql="select tran_date,supp_code,tran_code,lr_no,lr_date,lorry_no, dc_no,dc_date,octroi_no,freight from gate_register where tran_id = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, grNo);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						tdate = rs.getTimestamp(1);
						scode= rs.getString(2);
						tcode= rs.getString(3);
						lslrno= rs.getString(4);
						ldtlrdate= rs.getTimestamp(5);
						lslorryno= rs.getString(6);
						lschallanno= rs.getString(7);
						ldtchallandt= rs.getTimestamp(8);
						lsoctroino = rs.getString(9);
						lcfreight = rs.getDouble(10);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println("ldtlrdate1"+ldtlrdate1);
					System.out.println("ldtchallandt1"+ldtchallandt1);
					if(lstrancode == null &&(grNo !=null && grNo.trim().length()>0))
					{
						valueXmlString.append("<tran_code>").append("<![CDATA[" + tcode + "]]>").append("</tran_code>");
						sql=" select tran_name from  transporter where tran_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, tcode);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							descr = checkNull(rs.getString(1));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<transporter_name>").append("<![CDATA[" + descr + "]]>").append("</transporter_name>");
					}
					if(lspurcorder == null)
					{																		
						//Pavan R 08jun19 start[to update channel P flag]		
						siteCode = genericUtility.getColumnValue("site_code", dom);
						cpFlag = isChannelPartnerSupp(scode, siteCode, conn);						
						if(cpFlag)
						{
							valueXmlString.append("<channel_partner><![CDATA[" + "Y" + "]]></channel_partner>");
						}else 
						{
							valueXmlString.append("<channel_partner><![CDATA[" + "N" + "]]></channel_partner>");					
						}
						//Pavan R 08jun19 end
						valueXmlString.append("<supp_code>").append("<![CDATA[" + scode + "]]>").append("</supp_code>");
						valueXmlString.append("<supp_code__ship>").append("<![CDATA[" + scode + "]]>").append("</supp_code__ship>");
						sql=" select supp_name from supplier where supp_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,scode);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							descr = checkNull(rs.getString(1));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<supp_name>").append("<![CDATA[" + descr + "]]>").append("</supp_name>");
						valueXmlString.append("<supp_name__ship>").append("<![CDATA[" + descr + "]]>").append("</supp_name__ship>");
						valueXmlString.append("<tran_code>").append("<![CDATA[" + tcode + "]]>").append("</tran_code>");
						sql="   select tran_name from  transporter where tran_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,tcode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							descr = checkNull(rs.getString(1));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<transporter_name>").append("<![CDATA[" + tcode + "]]>").append("</transporter_name>");
						sql=" select curr_code  from supplier where supp_code =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,tcode);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							lscurrcode = checkNull(rs.getString(1));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<curr_code>").append("<![CDATA[" + lscurrcode + "]]>").append("</curr_code>");

						valueXmlString.append("<lr_no>").append("<![CDATA[" + lslrno + "]]>").append("</lr_no>");
						if(ldtlrdate != null)
						{
							System.out.println("ldtlrdate"+ldtlrdate);
							ldtlrdate1=sdf.format(ldtlrdate);
							System.out.println("ldtlrdate1"+ldtlrdate1);
							valueXmlString.append("<lr_date>").append("<![CDATA[" + ldtlrdate1 + "]]>").append("</lr_date>");
						}
						valueXmlString.append("<lorry_no>").append("<![CDATA[" + lslorryno + "]]>").append("</lorry_no>");
						valueXmlString.append("<dc_no>").append("<![CDATA[" + lschallanno + "]]>").append("</dc_no>");
						if(ldtchallandt !=null)
						{
							System.out.println("ldtchallandt"+ldtchallandt);
							ldtchallandt1=sdf.format(ldtchallandt);
							System.out.println("ldtchallandt1"+ldtchallandt1);
							valueXmlString.append("<dc_date>").append("<![CDATA[" + ldtchallandt1 + "]]>").append("</dc_date>");
						}
						valueXmlString.append("<octroi_no>").append("<![CDATA[" + lsoctroino + "]]>").append("</octroi_no>");
						valueXmlString.append("<frt_amt>").append("<![CDATA[" + lcfreight + "]]>").append("</frt_amt>");
					}
					valueXmlString.append("<lr_no>").append("<![CDATA[" + lslrno + "]]>").append("</lr_no>");
					if(ldtlrdate != null)
					{
						System.out.println("ldtlrdate"+ldtlrdate);
						ldtlrdate1=sdf.format(ldtlrdate);
						System.out.println("ldtlrdate1"+ldtlrdate1);
						valueXmlString.append("<lr_date>").append("<![CDATA[" + ldtlrdate1 + "]]>").append("</lr_date>");
					}
					//valueXmlString.append("<lr_date>").append("<![CDATA[" + ldtlrdate + "]]>").append("</lr_date>");
					valueXmlString.append("<lorry_no>").append("<![CDATA[" + lslorryno + "]]>").append("</lorry_no>");
					valueXmlString.append("<dc_no>").append("<![CDATA[" + lschallanno + "]]>").append("</dc_no>");
					if(ldtchallandt !=null)
					{
						System.out.println("ldtchallandt["+ldtchallandt+"]");
						ldtchallandt1=sdf.format(ldtchallandt);
						System.out.println("ldtchallandt1"+ldtchallandt1);
						valueXmlString.append("<dc_date>").append("<![CDATA[" + ldtchallandt1 + "]]>").append("</dc_date>");
					}
					valueXmlString.append("<octroi_no>").append("<![CDATA[" + lsoctroino + "]]>").append("</octroi_no>");
					valueXmlString.append("<frt_amt>").append("<![CDATA[" + lcfreight + "]]>").append("</frt_amt>");
				}
				else if (currentColumn.trim().equalsIgnoreCase("dc_date"))
				{	
					String dcDate="",tranDate="",invoicedate="",trandate="",poOrder="";
					Date date1 = null,date2=null;
					poOrder=checkNull(genericUtility.getColumnValue("purc_order", dom));
					dcDate =genericUtility.getColumnValue("dc_date", dom);
					//dcDate = genericUtility.getColumnValue("dc_date", dom);
					//xmlBuff.append("<dc_date><![CDATA[").append(dcDate==null?"":dcDate).append("]]></dc_date>");
					System.out.println("Purchase Order is"+poOrder);
					System.out.println("dcDate  is"+dcDate);
					valueXmlString.append("<invoice_date><![CDATA[").append(dcDate==null?"":dcDate).append("]]></invoice_date>");
					valueXmlString.append("<excise_ref_date><![CDATA[").append(dcDate==null?"":dcDate +" 00:00:00").append("]]></excise_ref_date>");

					tranDate =genericUtility.getColumnValue("tran_date", dom);
					System.out.println("tranDate"+tranDate);

					sql="select cr_term from Porder where Purc_order =?";
					pstmt= conn.prepareStatement( sql );
					pstmt.setString(1,poOrder);
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						lscrterm = rs.getString(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					sql=" select start_from from crterm where cr_term =?";
					pstmt= conn.prepareStatement( sql );
					pstmt.setString(1,lscrterm);
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						lssf = rs.getString(1); 

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if("D".equalsIgnoreCase(lssf))
					{
						System.out.println("dcDate--["+dcDate+"]tranDate--["+tranDate+"]");
						//Modified by Azhar K. on [06-05-2019][To set tran date as effective date when dc date is null][Start]
						if(dcDate == null || dcDate.trim().length() == 0)
						{
							valueXmlString.append("<eff_date><![CDATA[").append(tranDate).append("]]></eff_date>");
						}
						else
						{
							//valueXmlString.append("<eff_date>").append(checkDate(genericUtility.getColumnValue("dc_date", dom))).append("</eff_date>");
							valueXmlString.append("<eff_date><![CDATA[").append( dcDate==null?"":dcDate).append("]]></eff_date>");
						}
						//Modified by Azhar K. on [06-05-2019][To set tran date as effective date when dc date is null][End]
					}
					else
					{
						//valueXmlString.append("<eff_date>").append(checkDate(genericUtility.getColumnValue("dc_date", dom))).append("</eff_date>");
						valueXmlString.append("<eff_date><![CDATA[").append( tranDate==null?"":tranDate).append("]]></eff_date>");
					}

				}
				else if (currentColumn.trim().equalsIgnoreCase("item_ser"))
				{
					mcode =genericUtility.getColumnValue("item_ser", dom);
					sql="select descr from itemser where item_ser = ?";
					pstmt= conn.prepareStatement( sql );
					pstmt.setString(1,mcode);
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						descr = checkNull(rs.getString(1)); 

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<itemser_descr>").append("<![CDATA[" + descr + "]]>").append("</itemser_descr>");
				}
				else if (currentColumn.trim().equalsIgnoreCase("tran_code"))
				{
					mcode =genericUtility.getColumnValue("tran_code", dom);
					sql=" select tran_name from transporter where tran_code = ?";
					pstmt= conn.prepareStatement( sql );
					pstmt.setString(1,mcode);
					rs = pstmt.executeQuery(); 
					if( rs.next() )
					{
						descr = checkNull(rs.getString(1)); 

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<transporter_name>").append("<![CDATA[" + descr + "]]>").append("</transporter_name>");
				}
				/*            
        case "benefit_type"    
        mcode = dw_edit.getitemstring(1,colname)*/
				else if (currentColumn.trim().equalsIgnoreCase("frt_type")) 
				{
					String frtType="",frtRateStr="",frtAmtQtyStr="",frtAmtFixedStr="";
					System.out.println("@@@@@@@@ item change call for "+ currentColumn);
					frtType = checkNull(genericUtility.getColumnValue("frt_type", dom));
					frtRateStr = checkNull(genericUtility.getColumnValue("frt_rate", dom));
					frtAmtQtyStr = checkNull(genericUtility.getColumnValue("frt_amt__qty", dom));
					frtAmtFixedStr = checkNull(genericUtility.getColumnValue("frt_amt__fixed", dom));
					System.out.println("frtType[" + frtType+ "]:::frtAmtFixed[" + frtAmtFixedStr + "]");

					frtAmtQtyStr = frtAmtQtyStr == null ? "0" : frtAmtQtyStr;
					frtAmtFixedStr = frtAmtFixedStr == null ? "0"
							: frtAmtFixedStr;

					if ("Q".equalsIgnoreCase(frtType)) {
						// valueXmlString.append("<frt_amt__fixed protect = \"1\">").append("<![CDATA["+(
						// frtAmtFixedStr )+"]]>").append("</frt_amt__fixed>");
						valueXmlString.append("<frt_amt__fixed protect = \"1\">").append("<![CDATA[0]]>").append("</frt_amt__fixed>");
						valueXmlString.append("<frt_rate protect = \"0\">").append("<![CDATA[" + frtRateStr + "]]>").append("</frt_rate>");
						valueXmlString.append("<frt_amt__qty protect = \"1\">").append("<![CDATA[" + frtAmtQtyStr + "]]>").append("</frt_amt__qty>");
					} else if ("F".equalsIgnoreCase(frtType)) {
						valueXmlString.append("<frt_amt__fixed  protect = \"0\">").append("<![CDATA[" + frtAmtFixedStr + "]]>").append("</frt_amt__fixed>");
						valueXmlString.append("<frt_rate protect = \"1\">").append("<![CDATA[0]]>").append("</frt_rate>");
						valueXmlString.append("<frt_amt__qty protect = \"1\">").append("<![CDATA[0]]>").append("</frt_amt__qty>");
					}
				}
				/*    case "frt_type"
            gbf_frttypeoption()
        case "frt_rate"
            gbf_calculatefrtamt()*/
				else if (currentColumn.trim().equalsIgnoreCase("excise_ref_date"))
				{
					String trandt="";
					lsvar=disscommon.getDisparams("999999", "EXCH_RATE_PORCP", conn);
					System.out.println("LsVarvalue is"+lsvar);
					if("excise_ref_date".equalsIgnoreCase(lsvar))
					{
						mCurr = genericUtility.getColumnValue("curr_code", dom);
						lssitecode = genericUtility.getColumnValue("site_code", dom);
						trandt =genericUtility.getColumnValue("eff_date", dom);
						sql="select a.curr_code from finent a, site b where b.fin_entity = a.fin_entity and b.site_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lssitecode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							lscurrcodebase = checkNull(rs.getString(1));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if(mCurr !=null && mCurr.trim().length()>0)
						{
							lcexchrate=fincommon.getDailyExchRateSellBuy(mCurr, lscurrcodebase, lssitecode, trandt, "B", conn);
							valueXmlString.append("<exch_rate__clr>").append("<![CDATA[" + lcexchrate + "]]>").append("</exch_rate__clr>");
						}

					}
				}
				valueXmlString.append("</Detail1>");
				break;
				// case 2 start
			case 2:

				//valueXmlString.append(itemChangedDetail(dom, dom1, dom2, objContext,currentColumn, editFlag, xtraParams ,conn));
				valueXmlStringStr = itemChangedDetail(dom, dom1, dom2, objContext,currentColumn, editFlag, xtraParams ,conn);

				System.out.println("valueXmlString>>>>"+valueXmlString);
				valueXmlString.append(valueXmlStringStr);
			}
			valueXmlString.append("</Root>");
		} catch (Exception e) {
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
			} catch (Exception d) {
				d.printStackTrace();
			}
		}
		return valueXmlString.toString();
	}

	public String itemChangedDetail(Document dom, Document dom1, Document dom2, String objContext, String currentColumn,String editFlag, String xtraParams ,Connection conn) throws RemoteException,ITMException
	{
		System.out.println("###########ITEMCHANGE FOR CASE FIrst Method###################");
		System.out.println("itemChanged() called for PorcpIC");
		//String valueXmlString = "";
		DistCommon disscommon = new DistCommon();
		FinCommon finCommon = new FinCommon();
		UtilMethods utilMethod = new UtilMethods();
		String childNodeName = null;
		StringBuffer valueXmlString = new StringBuffer();
		int ctr = 0,cnt = 0 ,childNodeListLength = 0;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		PreparedStatement pstmt = null,pstmt1=null;
		ResultSet rs = null,rs1=null;
		SimpleDateFormat sdf1 = null ;

		ArrayList lcstdqty1 = new ArrayList();
		ArrayList acctDetrList = new ArrayList();
		String columnValue = "" ,lsuomrnd = "",sql = "" ,lsqcreqd = "" , mPordNo = "" , mPordLine ="" , lspurcorder= "", lssite= "" ,
				lsudf ="" ,lsitemcode  =""  , lsitemser = "" , lstrackshelflife = "", lspackinstr= "" ,lsanalcode ="" ,
				lspotype= "" ,lsitemremarks = "" ,lsporateoption = "" , mctcr = "" , lsspec = "" ,lsmfgitemcd = "" ,lsspecificinstr = "" ,lssuppcodemnfr = "" ,
				mdescr= "" ,mRateUom = "" ,macdr = "" ,lsspecialinstr = "" , mctdr = "" ,lsspecdescr = "" ,mStdUom = "" ,lscancbo = "" ,
				lsunitstd= "" , lsunit = "" , lslotsl= "" , lspricelist= "" ,lslisttype  = "" ,lscostctr = "" , lscctrloccode= "" ,lssitemfg = "" ,
				mlotno  = "" ,lscostctrasloccode = "" ,mcode = "", lspack = "" , lssitecode = "" ,lssource = "" ,lsloc = "", lstranid= "" ,
				lsdis= "" , lsstkopt = "", lsunitpur= "" , lsuom = "",mVal= "" ,mVal1 = "",lspackinstr1 = "" ,
				mUom= "" , lspack1= "" , lsaccdesc= "" , lsvalue= "" , mlotsl= "" ,lsacct = "" , lsloccode= "" ,sysDate = "" , mitem= "" ,
				lsacct1= "";
		String stkOpt="",suppCode="",exchRateFrt="",reStr="",sql1="";
		int currentFormNo = 0,pos=0;
		Timestamp ldtmfgdate  = null ;
		double lcexchrate = 0.0 ,lcqty = 0.0, mOrdQty= 0.0  ,lcQtyConv = 0 , lcRtConv = 0 ,lishelflife = 0.0 ,lcratestduom = 0.0 , mpending = 0 ,lcrateclg=0, 
				lcstdrate = 0 ,mCancperc = 0 , lcconvtemp= 0 ,lctemp = 0 ,lcconv = 0 ,mDlvQty = 0 ,lcrealisedqty = 0 , lcgrosswt = 0 ,lctarewt = 0 ,
				lcnetwt= 0 ;// = 0 , = 0 , = 0;
		String projectCode = ""; // Added by sarita on 29 MARCH 2019
		String grossWeight="",netWeight="",netWtItem="",	netWt="", integrlQty="",lcMode="";
		double mGrossWeight=0d, mNetWeight=0d, mGrosswt=0d,mNetWt=0d, mTareWt=0d,mNoart=0d,	mNetWtItem=0d, mIntegrlQty=0d;
		boolean PoLineItemFlag=false;//added by manish mhatre on 13-aug-2019
		try {


			String isNullPo="",singleord="",lsvarvalue="",tranid="",mpord="",exchrate="",lotno="";
			double liretestperiod=0;;
			double lcgrossweight=0,lctareweight=0,netAmt=0;
			System.out.println("**********************In case 2 ***********************");
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			parentNodeList = dom.getElementsByTagName("Detail2");
			parentNode = parentNodeList.item(0);
			childNodeList = parentNode.getChildNodes();
			ctr = 0;

			suppCode=genericUtility.getColumnValue("supp_code", dom1);
			lsqcreqd=genericUtility.getColumnValue("qc_reqd", dom1);
			exchrate=genericUtility.getColumnValue("exch_rate", dom1);
			exchRateFrt = genericUtility.getColumnValue("exch_rate__frt", dom1);
			lspurcorder=	genericUtility.getColumnValue("purc_order", dom1);
			//added by manish mhatre on 13-aug-2019[For protect purchase order when flag==true]
			//start manish
			int parentNodeListLengthTemp = 0;
			NodeList parentNodeListTemp = null;				
			Node parentNodeTemp = null;
			parentNodeListTemp = dom2.getElementsByTagName("Detail2");
			parentNodeListLengthTemp = parentNodeListTemp.getLength();	
			for (int selectedRow = 0; selectedRow < parentNodeListLengthTemp; selectedRow++)
			{
				parentNodeTemp = parentNodeListTemp.item(selectedRow);
				String lineItemcode = checkNull(genericUtility.getColumnValueFromNode("item_code", parentNodeTemp));
				System.out.println("outside lineItemcode>>>>>>>>>>>>>>>"+lineItemcode);
				if(lineItemcode != null && lineItemcode.trim().length() > 0)		
				{		
					System.out.println("inside if lineItemcode>>>>>>>>>>>>>>>"+lineItemcode);
					PoLineItemFlag = true;
				}
			}
			//end manish
			valueXmlString.append("<Detail1>");
			valueXmlString.append("<supp_code protect =\"1\">").append("<![CDATA["+suppCode+"]]>").append("</supp_code>");
			valueXmlString.append("<qc_reqd protect =\"1\">").append("<![CDATA["+lsqcreqd+"]]>").append("</qc_reqd>");
			valueXmlString.append("<exch_rate protect =\"1\">").append("<![CDATA["+exchrate+"]]>").append("</exch_rate>");
			valueXmlString.append("<exch_rate__frt protect =\"1\">").append("<![CDATA["+exchRateFrt+"]]>").append("</exch_rate__frt>");
			//			valueXmlString.append("<purc_order protect =\"1\">").append("<![CDATA["+lspurcorder+"]]>").append("</purc_order>"); //commented by manish mhatre on 13-aug-2019

			//added by manish mhatre on 13-aug-2019 [For protect purchase order when flag==true]
			//start manish
			if(PoLineItemFlag==true)
			{
				System.out.println("Inside if flag");
				valueXmlString.append("<purc_order protect =\"1\">").append("<![CDATA["+lspurcorder+"]]>").append("</purc_order>");
			}
			else
			{
				System.out.println("Inside else flag");
				valueXmlString.append("<purc_order protect=\"0\">").append("<![CDATA["+lspurcorder+"]]>").append("</purc_order>");
			}
			//end manish

			valueXmlString.append("</Detail1>");

			valueXmlString.append("<Detail2>");
			childNodeListLength = childNodeList.getLength();
			do {
				childNode = childNodeList.item(ctr);
				childNodeName = childNode.getNodeName();
				if (childNodeName.equals(currentColumn)) 
				{
					if (childNode.getFirstChild() != null)
					{
						columnValue = childNode.getFirstChild().getNodeValue().trim();
					}
				}
				ctr++;
			} while (ctr < childNodeListLength&& !childNodeName.equals(currentColumn));
			System.out.println("IN DETAILS column name is %%%%%%%%%%%%%["+ currentColumn + "] ==> '" + columnValue + "'");
			lsuomrnd =disscommon.getDisparams("999999", "UOM_ROUND", conn);
			if("NULLFOUND".equalsIgnoreCase(lsuomrnd))
			{
				lsuomrnd = "B";
			}
			isNullPo = disscommon.getDisparams("999999", "RCP_WO_PO", conn);
			if("NULLFOUND".equalsIgnoreCase(lsuomrnd))
			{
				isNullPo = "N";
			}
			System.out.println("lsuomrnd ["+lsuomrnd+"]");
			System.out.println("lsuomrnd ["+isNullPo+"]");
			if (currentColumn.trim().equalsIgnoreCase("itm_default"))
			{
				mpord=checkNull(genericUtility.getColumnValue("purc_order",dom1)).trim();
				System.out.println("PO DOm itm_default "+mpord.length());
				mpord=mpord.trim();
				System.out.println("PO Trim itm_default "+mpord.length());
				System.out.println("Purch Order is "+mpord);
				valueXmlString.append("<purc_order>").append("<![CDATA["+mpord+ "]]>").append("</purc_order>");
				String lsgatereg="";
				lsvarvalue = disscommon.getDisparams("999999", "RCPT_SCHEDULE", conn);
				System.out.println("lsvarvalue"+lsvarvalue);
				if("A".equalsIgnoreCase(lsvarvalue));
				{
					valueXmlString.append("<line_no__ord>").append("<![CDATA[ ]]>").append("</line_no__ord>");
				}
				valueXmlString.append("<canc_bo>").append("<![CDATA[N]]>").append("</canc_bo>");


				String str ="                    ";
				valueXmlString.append("<batch_no>").append("<![CDATA["+ str+ "]]>").append("</batch_no>");
				lsgatereg=checkNull(genericUtility.getColumnValue("gr_no", dom1)); 
				if(lsgatereg == null || lsgatereg.trim().length() == 0)
				{
					sql="select gross_weight,tare_weight from gate_register where tran_id = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsgatereg);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						lcgrossweight = rs.getDouble(1);
						lctareweight = rs.getDouble(2);

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<gross_weight>").append("<![CDATA["+ lcgrossweight+ "]]>").append("</gross_weight>");
					valueXmlString.append("<tare_weight>").append("<![CDATA["+ lctareweight+ "]]>").append("</tare_weight>");
					netAmt=lcgrossweight - lctareweight;
					valueXmlString.append("<net_weight>").append("<![CDATA["+ netAmt+ "]]>").append("</net_weight>");
				}
				exchrate=genericUtility.getColumnValue("exch_rate", dom1); 
				exchrate = (exchrate == null || exchrate.trim().length() == 0) ? "0" : exchrate ;
				lcexchrate = Double.parseDouble(exchrate);
				System.out.println("exchrate"+lcexchrate);
				valueXmlString.append("<exch_rate>").append("<![CDATA["+ lcexchrate+ "]]>").append("</exch_rate>");

				lotno=checkNull(genericUtility.getColumnValue("lot_no", dom1)); 
				valueXmlString.append("<lot_no>").append("<![CDATA["+ lotno	+ "]]>").append("</lot_no>");
				valueXmlString.append("<mfg_date protect =\"0\">").append("<![CDATA[]]>").append("</mfg_date>");
				valueXmlString.append("<expiry_date protect =\"0\">").append("<![CDATA[]]>").append("</expiry_date>");
				valueXmlString.append("<reas_code protect =\"1\">").append("<![CDATA[]]>").append("</reas_code>");
				//gbf_itemchg_modifier_ds(dw_detedit[ii_currformno],"mfg_date","protect","0") //Added by fatema - 05/06/2007
				//gbf_itemchg_modifier_ds(dw_detedit[ii_currformno],"expiry_date","protect","0")  //Added by fatema - 05/06/2007
			} //remaining code
			else if (currentColumn.trim().equalsIgnoreCase("itm_defaultedit"))
			{
				lsitemcode =checkNull (genericUtility.getColumnValue("item_code", dom)); 
				String 	lsautogeneratelotno="",	lsNull="",lsLotNoManualSite="",	lssitestring=""	,lsexit="",lsgenlotauto="";
				lssite = checkNull(genericUtility.getColumnValue("site_code", dom1));
				String lotNo=checkNull(genericUtility.getColumnValue("lot_no", dom));
				String rate=(genericUtility.getColumnValue("rate", dom));
				String reasCode=(genericUtility.getColumnValue("reas_code", dom));
				mcode =genericUtility.getColumnValue("item_ser", dom);
				sql="select qc_reqd from siteitem where item_code = ? and site_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsitemcode);
				pstmt.setString(2, lssite);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					lsqcreqd = rs.getString(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				if(lsqcreqd == null || lsqcreqd.trim().length() == 0)
				{
					sql="select (case when qc_reqd is null then 'N' else qc_reqd end)  from item where item_code =  ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsitemcode);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						lsqcreqd = rs.getString(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
				if(lsqcreqd == null || lsqcreqd.trim().length() == 0)
				{
					lsqcreqd="N";
				}
				lsautogeneratelotno =disscommon.getDisparams("999999","GENERATE_LOT_NO_AUTO", conn);
				if("NULLFOUND".equalsIgnoreCase(lsautogeneratelotno))
				{
					lsautogeneratelotno="N";
				}
				lsLotNoManualSite =disscommon.getDisparams("999999","LOT_NO_MANUAL_SITE", conn);
				if("NULLFOUND".equalsIgnoreCase(lsautogeneratelotno))
				{
					lsLotNoManualSite=" ";
				}
				sql="select ITEM_SER  from item  where item_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsitemcode);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					lsitemser = rs.getString(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				sql="Select auto_gen_lot  from itemser where item_ser = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsitemser);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					lsgenlotauto = rs.getString(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if("N".equalsIgnoreCase(lsgenlotauto))
				{
					valueXmlString.append("<lot_no protect =\"0\">").append("<![CDATA["+lotNo+"]]>").append("</lot_no>");
				}
				else
				{
					if("M".equalsIgnoreCase(lsautogeneratelotno))
					{
						if("Y".equalsIgnoreCase(lsqcreqd))
						{
							valueXmlString.append("<lot_no protect =\"1\">").append("<![CDATA["+lotNo+"]]>").append("</lot_no>");
						}
						else
						{
							valueXmlString.append("<lot_no protect =\"0\">").append("<![CDATA["+lotNo+"]]>").append("</lot_no>");
						}
					}
					else
					{
						if("Y".equalsIgnoreCase(lsqcreqd))
						{
							if("Y".equalsIgnoreCase(lsautogeneratelotno))
							{
								valueXmlString.append("<lot_no >").append("<![CDATA["+lotNo+"]]>").append("</lot_no>");
								valueXmlString.append("<lot_no protect =\"1\">").append("<![CDATA[").append(lotNo).append("]]>").append("</lot_no>");
							}
							else
							{
								valueXmlString.append("<lot_no protect =\"0\">").append("<![CDATA["+lotNo+"]]>").append("</lot_no>");
							}
						}
						else if("Y".equalsIgnoreCase(lsautogeneratelotno))
						{
							lsexit = "F";
						}
					}
				}

				//Add by Ajay on 10/04/18:START
				sql = "select (case when stk_opt is null then '0' else stk_opt end) as stk_opt, item_ser  from item where item_code = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsitemcode);
				rs = pstmt.executeQuery();
				System.out.println("default edit sql ");
				if (rs.next())
				{
					stkOpt = checkNull(rs.getString("stk_opt")).trim();
					mcode = rs.getString("item_ser");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if("1".equalsIgnoreCase(stkOpt))
				{

					valueXmlString.append("<lot_no protect=\"1\"><![CDATA[").append("               ").append("]]></lot_no>\r\n");
					valueXmlString.append("<lot_sl protect=\"1\" ><![CDATA[").append("               ").append("]]></lot_sl>\r\n");

				}

				//Add by Ajay on 10/04/18:END

				sql="select case when po_rate_option is null then 'N' else po_rate_option end" +
						" from	item where item_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsitemcode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					lsporateoption = checkNull(rs.getString(1));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(lsporateoption == null || lsporateoption.trim().length() == 0)
				{
					lsporateoption="N";
				}
				if("N".equalsIgnoreCase(lsporateoption))
				{
					valueXmlString.append("<rate protect =\"1\">").append("<![CDATA["+rate+"]]>").append("</rate>");
				}
				else
				{
					valueXmlString.append("<rate protect =\"0\">").append("<![CDATA["+rate+"]]>").append("</rate>");
				}
				valueXmlString.append("<reas_code protect =\"1\">").append("<![CDATA["+reasCode+"]]>").append("</reas_code>");

//				Modified By Aniket C. [11th/APR/2021] MAKE EXP_DATE NON EDITABLE IN EDIT MODE [SATRT]
				String trackShelfLife = "";
				String expiryDate = checkNull(genericUtility.getColumnValue("expiry_date", dom));
				System.out.println("EXPIRY DATE IN EDIT MODE:"+expiryDate);
				
				sql = "select (case when track_shelf_life is null then 'N' else track_shelf_life end) "
						+ "from item where item_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsitemcode);
				
				rs = pstmt.executeQuery();
				
				if (rs.next()) {
					trackShelfLife = rs.getString(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
				System.out.println("Item Code In Edit Mode: "+lsitemcode);
				System.out.println("TRACK SHELF LIFE IN EDIT MODE:"+trackShelfLife);
				
				if ("N".equalsIgnoreCase(trackShelfLife)) 
				{
					valueXmlString.append("<expiry_date protect =\"1\">").append("<![CDATA["+expiryDate+"]]>").append("</expiry_date>");
				}
				else
				{
					valueXmlString.append("<expiry_date protect =\"0\">").append("<![CDATA["+expiryDate+"]]>").append("</expiry_date>");
				}
//				Modified By Aniket C. [11th/APR/2021] MAKE EXP_DATE NON EDITABLE IN EDIT MODE [END] 

			}
			else if (currentColumn.trim().equalsIgnoreCase("purc_order"))
			{
				lspurcorder = genericUtility.getColumnValue("purc_order", dom);
				System.out.println("PO ItemChange Dom"+lspurcorder);
				lspurcorder=lspurcorder.trim();
				System.out.println("PO ItemChange Trim:"+lspurcorder);
				sql="select pord_type from porder where purc_order = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lspurcorder);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					lspotype = checkNull(rs.getString(1));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				sql="select udf_str1 from gencodes where fld_name='PORD_TYPE' and mod_name='W_PORDER' and fld_value = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lspotype);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					lsudf = checkNull(rs.getString(1));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(lsudf== null || lsudf.trim().length() == 0)
				{
					sql="select udf_str1 from gencodes where fld_name='PORD_TYPE' and mod_name='X' and fld_value = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lspotype);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						lsudf = checkNull(rs.getString(1));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
				valueXmlString.append("<grade>").append("<![CDATA[" + lsudf + "]]>").append("</grade>");
			}
			else if(currentColumn.trim().equalsIgnoreCase("line_no__ord"))
			{     

				String muom="",lstrantype="", lcrtconv1="",mdateStr="",expDate1="",expDate="",mordqty1="",postprov="",maccr="",mlocation="",mtaxclass="",mtaxchap="",mtaxenv="",lsdeptcode="",lsbenefittype="",lsporcpline="";
				String lslicenceno="",reciepttype="",lsacctpdr="",lsporzateoption="",varvalue="",lsanalcod="",lsacctpcr="",lscctrpdr="",lscctrpcr="",lsformno="",lsdutypaid="";
				double lcrtconv=0,lcrate=0,mordqty=0,mdiscount=0,mtax=0,mtotamt=0,inputQty=0,inputQty1=0, lcOdrQty=0;
				mPordNo = checkNull(genericUtility.getColumnValue("purc_order", dom));
				mPordLine = checkNull(genericUtility.getColumnValue("line_no__ord", dom));
				lspurcorder= checkNull(genericUtility.getColumnValue("purc_order", dom));
				lsporcpline =checkNull( genericUtility.getColumnValue("line_no", dom));
				lsitemcode=checkNull(genericUtility.getColumnValue( "item_code", dom ));
				String rate=(genericUtility.getColumnValue("rate", dom));
				String reasCode=(genericUtility.getColumnValue("reas_code", dom));
				System.out.println("LINE NUMBER IN DOM"+mPordLine);
				mPordLine = "    " + mPordLine;
				mPordLine = mPordLine.substring(mPordLine.length() - 3,mPordLine.length());
				System.out.println("APPEND LINE NUMBER"+mPordLine);
				valueXmlString.append("<line_no__ord >").append("<![CDATA[" + mPordLine + "]]>").append("</line_no__ord>");
				//26 may changes

				//Commented and added by sarita to set proj_code in sql on 03 APRIL 2019 [START]
				/* sql="Select	Item_code,(quantity - (case when dlv_qty is null then 0 else dlv_qty end)),unit,	" +
	            	  		"rate, discount, tax_amt, tot_amt, loc_code, tax_class, tax_chap," +
	            	  		" tax_env, conv__qty_stduom, conv__rtuom_stduom, unit__rate ,acct_code__dr," +
	            	  		" cctr_code__dr,acct_code__cr,cctr_code__cr,remarks,rate__stduom , specific_instr, " +
	            	  		"special_instr, pack_instr, rate__clg, supp_code__mnfr, item_code__mfg,spec_ref," +
	            	  		"std_rate,dept_code,benefit_type,licence_no," +                                     						
	            	  		"acct_code__prov_dr, acct_code__prov_cr, cctr_code__prov_dr, cctr_code__prov_cr,"+
	            	  		"(quantity - (case when dlv_qty is null then 0 else dlv_qty end)) as lcodrqty," +
	            	  		" form_no, duty_paid,anal_code ,quantity " +
	            	  		" From PordDet Where Purc_order = ? and Line_no    =? ";*/
				sql="Select	Item_code,(quantity - (case when dlv_qty is null then 0 else dlv_qty end)) as QTY ,unit,	" +
						"rate, discount, tax_amt, tot_amt, loc_code, tax_class, tax_chap," +
						" tax_env, conv__qty_stduom, conv__rtuom_stduom, unit__rate ,acct_code__dr," +
						" cctr_code__dr,acct_code__cr,cctr_code__cr,remarks,rate__stduom , specific_instr, " +
						"special_instr, pack_instr, rate__clg, supp_code__mnfr, item_code__mfg,spec_ref," +
						"std_rate,dept_code,benefit_type,licence_no," +                                     						
						"acct_code__prov_dr, acct_code__prov_cr, cctr_code__prov_dr, cctr_code__prov_cr,"+
						"(quantity - (case when dlv_qty is null then 0 else dlv_qty end)) as lcodrqty," +
						" form_no, duty_paid,anal_code ,quantity,proj_code " +
						" From PordDet Where Purc_order = ? and Line_no    =? ";
				//Commented and added by sarita to set proj_code in sql on 03 APRIL 2019 [END]
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, mPordNo);
				pstmt.setString(2, mPordLine);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					//Modify by PriyankaC on 21Oct2019.[Start]
					/*lsitemcode = checkNull(rs.getString("Item_code"));
					lcqty = rs.getDouble(2);
					muom = checkNull(rs.getString(3));
					lcrate = rs.getDouble(4);
					mdiscount = rs.getDouble(5);
					mtax = rs.getDouble(6);
					mtotamt =  rs.getDouble(7);
					mlocation = checkNull(rs.getString(8));
					mtaxclass = checkNull(rs.getString(9));
					mtaxchap = checkNull(rs.getString(10));
					mtaxenv = checkNull(rs.getString(11));
					lcQtyConv = rs.getDouble(12);
					lcRtConv =rs.getDouble(13);
					mRateUom = checkNull(rs.getString(14));
					macdr = checkNull(rs.getString(15));
					mctdr = checkNull(rs.getString(16));
					maccr = checkNull(rs.getString(17));
					mctcr = checkNull(rs.getString(18));
					lsitemremarks = checkNull(rs.getString(19));
					lcratestduom = rs.getDouble(20);
					lsspecificinstr = checkNull(rs.getString(21));
					lsspecialinstr = checkNull(rs.getString(22));
					lspackinstr = checkNull(rs.getString(23));
					lcrateclg = rs.getDouble(24);
					lssuppcodemnfr = checkNull(rs.getString(25));
					lsmfgitemcd = checkNull(rs.getString(26));
					lsspec = checkNull(rs.getString(27));
					lcstdrate = rs.getDouble(28);
					lsdeptcode = checkNull(rs.getString(29));
					lsbenefittype = checkNull(rs.getString(30));
					lslicenceno = checkNull(rs.getString(31));
					lsacctpdr = checkNull(rs.getString(32));
					lsacctpcr = checkNull(rs.getString(33));
					lscctrpdr = checkNull(rs.getString(34));
					lscctrpcr = checkNull(rs.getString(35));
					lsformno = checkNull(rs.getString(36));
					lsdutypaid = checkNull(rs.getString(37));
					lsanalcode = checkNull(rs.getString(38));
					//26 may changes
					mOrdQty = rs.getDouble("quantity");
					lcOdrQty = rs.getDouble("lcodrqty");
					//Added by sarita to get ProjectCode Value on 03 APR 2019 [START]
					projectCode = checkNull(rs.getString("proj_code"));
					System.out.println("PROJECTCODEDETAIL :: ["+projectCode+"]");*/
					//Added by sarita to get ProjectCode Value on 03 APR 2019 [END]
					lsitemcode = checkNull(rs.getString("Item_code"));
					lcqty = 	rs.getDouble("QTY");
					muom = checkNull(rs.getString("unit"));
					lcrate = rs.getDouble("rate");
					mdiscount = rs.getDouble("discount");
					mtax = rs.getDouble("tax_amt");
					mtotamt =  rs.getDouble("tot_amt");
					mlocation = checkNull(rs.getString("loc_code"));
					mtaxclass = checkNull(rs.getString("tax_class"));
					mtaxchap = checkNull(rs.getString("tax_chap"));
					mtaxenv = checkNull(rs.getString("tax_env"));
					lcQtyConv = rs.getDouble("conv__qty_stduom");
					lcRtConv =rs.getDouble("conv__rtuom_stduom");
					mRateUom = checkNull(rs.getString("unit__rate"));
					macdr = checkNull(rs.getString("acct_code__dr"));
					mctdr = checkNull(rs.getString("cctr_code__dr"));
					maccr = checkNull(rs.getString("acct_code__cr"));
					mctcr = checkNull(rs.getString("cctr_code__cr"));
					lsitemremarks = checkNull(rs.getString("remarks"));
					lcratestduom = rs.getDouble("rate__stduom");
					lsspecificinstr = checkNull(rs.getString("specific_instr"));
					lsspecialinstr = checkNull(rs.getString("special_instr"));
					lspackinstr = checkNull(rs.getString("pack_instr"));
					lcrateclg = rs.getDouble("rate__clg");
					lssuppcodemnfr = checkNull(rs.getString("supp_code__mnfr"));
					lsmfgitemcd = checkNull(rs.getString("item_code__mfg"));
					lsspec = checkNull(rs.getString("spec_ref"));
					lcstdrate = rs.getDouble("std_rate");
					lsdeptcode = checkNull(rs.getString("dept_code"));
					lsbenefittype = checkNull(rs.getString("benefit_type"));
					lslicenceno = checkNull(rs.getString("licence_no"));
					lsacctpdr = checkNull(rs.getString("acct_code__prov_dr"));
					lsacctpcr = checkNull(rs.getString("acct_code__prov_cr"));
					lscctrpdr = checkNull(rs.getString("cctr_code__prov_dr"));
					lscctrpcr = checkNull(rs.getString("cctr_code__prov_cr"));
					lcOdrQty = rs.getDouble("lcodrqty");
					lsformno = checkNull(rs.getString("form_no"));
					lsdutypaid = checkNull(rs.getString("duty_paid"));
					lsanalcode = checkNull(rs.getString("anal_code"));
					mOrdQty = rs.getDouble("quantity");
					projectCode = checkNull(rs.getString("proj_code"));
					//Modify By PriyankaC on 21Oct2019 [END].
				}
				System.out.println("Analisys Code :: ["+lsanalcode+"]");
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				//<quantity
				//Added by sarita to get ProjectCode Value on 03 APR 2019 [START]
				valueXmlString.append("<proj_code protect =\"1\">").append("<![CDATA[" + projectCode + "]]>").append("</proj_code>");
				//Added by sarita to get ProjectCode Value on 03 APR 2019 [END]
				valueXmlString.append("<anal_code >").append("<![CDATA[" + lsanalcode + "]]>").append("</anal_code>");
				if(lsanalcode != null && lsanalcode.trim().length() >0)
				{
					sql="select descr from analysis where anal_code =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsanalcode);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						mdescr = checkNull(rs.getString(1));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<analysis_descr >").append("<![CDATA[" + mdescr + "]]>").append("</analysis_descr>");
				}
				valueXmlString.append("<item_code >").append("<![CDATA[" + lsitemcode + "]]>").append("</item_code>");
				setNodeValue(dom,"item_code" , lsitemcode);

				//added by manish mhatre on 24-03-21
				//start manish
				String dimension="";
				double noArt=0;
				sql="select dimension,no_art From PordDet Where Purc_order = ? and Line_no    =? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, mPordNo);
				pstmt.setString(2, mPordLine);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					dimension=rs.getString("dimension");
					noArt=rs.getDouble("no_art");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				if(dimension!=null && dimension.trim().length()>0)
				{
					valueXmlString.append("<dimension>").append("<![CDATA["+dimension+"]]>").append("</dimension>");
					setNodeValue( dom, "dimension", dimension );
				}
				if(noArt>0)
				{
					valueXmlString.append("<no_art>").append("<![CDATA["+noArt+"]]>").append("</no_art>");
					setNodeValue(dom,"no_art" , getAbsString(String.valueOf(noArt)));
				}
				//end manish

				String 	lsautogeneratelotno="",lsNull="",lsLotNoManualSite="",lssitestring=""	,lsexit="",lsgenlotauto="";
				lssite = checkNull(genericUtility.getColumnValue("site_code", dom1));
				sql="select qc_reqd from siteitem where item_code = ? and site_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsitemcode);
				pstmt.setString(2, lssite);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					lsqcreqd = rs.getString(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(lsqcreqd == null || lsqcreqd.trim().length() == 0)
				{
					sql="select (case when qc_reqd is null then 'N' else qc_reqd end)  from item where item_code =  ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsitemcode);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						lsqcreqd = rs.getString(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
				if(lsqcreqd == null || lsqcreqd.trim().length() == 0)
				{
					lsqcreqd="N";
				}
				lsautogeneratelotno =disscommon.getDisparams("999999","GENERATE_LOT_NO_AUTO", conn);
				if("NULLFOUND".equalsIgnoreCase(lsautogeneratelotno))
				{
					lsautogeneratelotno="N";
				}
				lsLotNoManualSite =disscommon.getDisparams("999999","LOT_NO_MANUAL_SITE", conn);
				if("NULLFOUND".equalsIgnoreCase(lsLotNoManualSite))
				{
					lsLotNoManualSite=" ";
				}
				sql="select ITEM_SER  from item  where item_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsitemcode);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					lsitemser = rs.getString(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				sql="Select auto_gen_lot  from itemser where item_ser = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsitemser);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					lsgenlotauto = rs.getString(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if("N".equalsIgnoreCase(lsgenlotauto))
				{
					valueXmlString.append("<lot_no protect =\"0\">").append("<![CDATA[]]>").append("</lot_no>");
					//valueXmlString.append("<lot_no protect =\"0\">").append("</lot_no>");

				}
				else
				{
					if("M".equalsIgnoreCase(lsautogeneratelotno))
					{
						if("Y".equalsIgnoreCase(lsqcreqd))
						{
							valueXmlString.append("<lot_no >").append("<![CDATA[ ]]>").append("</lot_no>");	
							valueXmlString.append("<lot_no >").append("<![CDATA[]]>").append("</lot_no>");
							//valueXmlString.append("<lot_no protect =\"1\">").append("</lot_no>");
						}
						else
						{
							valueXmlString.append("<lot_no >").append("<![CDATA[]]>").append("</lot_no>");
							//valueXmlString.append("<lot_no protect =\"0\">").append("</lot_no>");
						}
					}
					else
					{
						if("Y".equalsIgnoreCase(lsqcreqd))
						{
							if("Y".equalsIgnoreCase(lsautogeneratelotno))
							{
								System.out.println("lsautogeneratelotno["+lsautogeneratelotno+"]");
								valueXmlString.append("<lot_no >").append("<![CDATA[ ]]>").append("</lot_no>");	
								//valueXmlString.append("<lot_no >").append(" ").append("</lot_no>");
								//	valueXmlString.append("<lot_no protect =\"1\">").append("</lot_no>");
								valueXmlString.append("<lot_no protect =\"1\">").append("<![CDATA[]]>").append("</lot_no>");	

							}
							else
							{
								//valueXmlString.append("<lot_no protect =\"0\">").append("</lot_no>");
								valueXmlString.append("<lot_no protect =\"0\">").append("<![CDATA[]]>").append("</lot_no>");	

							}
						}
					}
				}


				//gbf_CheckItemFor_lotno_noeditable(ls_itemcode) remaining code
				sql="Select 	descr,(case when ordc_perc is null then 0 else ordc_perc end),unit, " +
						"(case when canc_bo_mode is null then 'A' else canc_bo_mode end)," +
						" (case when po_rate_option is null then 'N' else po_rate_option end)" +
						" from item where item_code = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsitemcode);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					mdescr = checkNull(rs.getString(1));
					mCancperc = rs.getDouble(2);
					mStdUom = checkNull(rs.getString(3));
					lscancbo = checkNull(rs.getString(4));
					lsporzateoption = checkNull(rs.getString(5));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				//	varvalue=disscommon.getDisparams("999999", "MFG_LOT_SL", conn);
				valueXmlString.append("<item_descr >").append("<![CDATA[" + mdescr + "]]>").append("</item_descr>");
				valueXmlString.append("<spec_ref >").append("<![CDATA[" + lsspec + "]]>").append("</spec_ref>");
				sql="select descr from specification where spec_ref = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsspec);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					lsspecdescr = checkNull(rs.getString(1));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				valueXmlString.append("<specification_descr >").append("<![CDATA[" + lsspecdescr + "]]>").append("</specification_descr>");
				valueXmlString.append("<acct_code__dr >").append("<![CDATA[" + macdr + "]]>").append("</acct_code__dr>");
				valueXmlString.append("<acct_code__cr >").append("<![CDATA[" + maccr + "]]>").append("</acct_code__cr>");
				valueXmlString.append("<cctr_code__dr >").append("<![CDATA[" + mctdr + "]]>").append("</cctr_code__dr>");
				valueXmlString.append("<cctr_code__cr >").append("<![CDATA[" + mctcr + "]]>").append("</cctr_code__cr>");

				sql="select descr from accounts where acct_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, macdr);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					mdescr = checkNull(rs.getString(1));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				valueXmlString.append("<accounts_descr >").append("<![CDATA[" + mdescr + "]]>").append("</accounts_descr>");
				postprov=checkNull( genericUtility.getColumnValue("post_prov", dom));
				System.out.println("PostProv is ....."+postprov);
				if("Y".equalsIgnoreCase(postprov))
				{
					valueXmlString.append("<acct_code__prov_dr >").append("<![CDATA[" + lsacctpdr + "]]>").append("</acct_code__prov_dr>");
					valueXmlString.append("<acct_code__prov_cr >").append("<![CDATA[" + lsacctpcr + "]]>").append("</acct_code__prov_cr>");
					valueXmlString.append("<cctr_code__prov_dr >").append("<![CDATA[" + lscctrpdr + "]]>").append("</cctr_code__prov_dr>");
					valueXmlString.append("<cctr_code__prov_cr >").append("<![CDATA[" + lscctrpcr + "]]>").append("</cctr_code__prov_cr>");
				}
				else
				{
					valueXmlString.append("<acct_code__prov_dr >").append("").append("</acct_code__prov_dr>");
					valueXmlString.append("<acct_code__prov_cr >").append("").append("</acct_code__prov_cr>");
					valueXmlString.append("<cctr_code__prov_dr >").append("").append("</cctr_code__prov_dr>");
					valueXmlString.append("<cctr_code__prov_cr >").append("").append("</cctr_code__prov_cr>");
				}
				valueXmlString.append("<form_no >").append("<![CDATA[" + lsformno + "]]>").append("</form_no>");
				valueXmlString.append("<duty_paid >").append("<![CDATA[" + lsdutypaid + "]]>").append("</duty_paid>");
				System.out.println("Department code is"+lsdeptcode);
				System.out.println("Department code length is"+lsdeptcode.length());
				if(lsdeptcode !=null && lsdeptcode.trim().length() >0)
				{
					valueXmlString.append("<dept_code >").append("<![CDATA[" + lsdeptcode + "]]>").append("</dept_code>");
				}
				else
				{
					System.out.println("Else");
					valueXmlString.append("<dept_code >").append("<![CDATA[]]>").append("</dept_code>");
				}
				valueXmlString.append("<remarks >").append("<![CDATA[" + lsitemremarks + "]]>").append("</remarks>");
				valueXmlString.append("<quantity >").append("<![CDATA[" +lcqty + "]]>").append("</quantity>");
				valueXmlString.append("<item_code__mfg >").append("<![CDATA[" + lsmfgitemcd + "]]>").append("</item_code__mfg>");
				valueXmlString.append("<realised_qty >").append("<![CDATA[" + lcqty + "]]>").append("</realised_qty>");
				valueXmlString.append("<supp_challan_qty >").append("<![CDATA[" + lcqty + "]]>").append("</supp_challan_qty>");
				valueXmlString.append("<unit >").append("<![CDATA[" + muom + "]]>").append("</unit>");
				valueXmlString.append("<discount >").append("<![CDATA[" + mdiscount + "]]>").append("</discount>");
				valueXmlString.append("<std_rate >").append("<![CDATA[" + lcstdrate + "]]>").append("</std_rate>");
				reciepttype=checkNull( genericUtility.getColumnValue("reciept_type", dom));
				if(!"R".equalsIgnoreCase(reciepttype))
				{
					valueXmlString.append("<tax_class >").append("<![CDATA[" + mtaxclass + "]]>").append("</tax_class>");
					valueXmlString.append("<tax_chap >").append("<![CDATA[" + mtaxchap + "]]>").append("</tax_chap>");
					valueXmlString.append("<tax_env >").append("<![CDATA[" + mtaxenv + "]]>").append("</tax_env>");
				}
				valueXmlString.append("<loc_code >").append("<![CDATA[" + mlocation + "]]>").append("</loc_code>");
				valueXmlString.append("<unit__std >").append("<![CDATA[" +mStdUom + "]]>").append("</unit__std>");
				valueXmlString.append("<unit__rate >").append("<![CDATA[" + mRateUom + "]]>").append("</unit__rate>");
				valueXmlString.append("<conv__qty_stduom >").append("<![CDATA[" + lcQtyConv + "]]>").append("</conv__qty_stduom>");
				valueXmlString.append("<conv__rtuom_stduom >").append("<![CDATA[" + lcRtConv + "]]>").append("</conv__rtuom_stduom>");
				valueXmlString.append("<supp_code__mnfr >").append("<![CDATA[" + lssuppcodemnfr + "]]>").append("</supp_code__mnfr>");
				valueXmlString.append("<benefit_type >").append("<![CDATA[" + lsbenefittype + "]]>").append("</benefit_type>");
				valueXmlString.append("<licence_no >").append("<![CDATA[" + lslicenceno + "]]>").append("</licence_no>");
				if(!"C".equalsIgnoreCase(lsporzateoption))
				{
					valueXmlString.append("<rate protect =\"1\">").append("<![CDATA[" + lcrate + "]]>").append("</rate>");
					valueXmlString.append("<rate__clg >").append("<![CDATA[" + lcrateclg + "]]>").append("</rate__clg>");
					valueXmlString.append("<rate__stduom >").append("<![CDATA[" + lcratestduom + "]]>").append("</rate__stduom>");
				}
				else
				{
					valueXmlString.append("<rate protect =\"1\">").append("0").append("</rate>");
					valueXmlString.append("<rate__stduom >").append("0").append("</rate__stduom>");
				}
				valueXmlString.append("<specific_instr >").append("<![CDATA[" + lsspecificinstr + "]]>").append("</specific_instr>");
				valueXmlString.append("<special_instr >").append("<![CDATA[" + lsspecialinstr + "]]>").append("</special_instr>");
				valueXmlString.append("<pack_instr >").append("<![CDATA[" + lspackinstr + "]]>").append("</pack_instr>");

				if("Q".equalsIgnoreCase(lsuomrnd) || "B".equalsIgnoreCase(lsuomrnd) )
				{
					lcstdqty1 = null;
					lcstdqty1 = new ArrayList();
					lcstdqty1 =disscommon.getConvQuantityFact(muom, mStdUom, lsitemcode, lcqty, lcQtyConv, conn); 
					System.out.println("inside if lcstdqty1>>>>>>"+lcstdqty1);
					if(lcstdqty1 != null && lcstdqty1.size()> 0)
					{
						inputQty = Double.parseDouble(lcstdqty1.get(1).toString());
					}
					//gf_conv_qty_fact1(mUom, mStdUom, ls_itemcode, lc_Qty, lc_QtyConv,'Y');
				}
				else
				{
					lcstdqty1 =disscommon.getConvQuantityFact(muom, mStdUom, lsitemcode, lcqty, lcQtyConv, conn);
					System.out.println("inside else lcstdqty1>>>>>>"+lcstdqty1);
					if(lcstdqty1 != null && lcstdqty1.size()> 0)
					{
						inputQty = Double.parseDouble(lcstdqty1.get(1).toString());
					}

				}
				System.out.println("inputQty>>>>>>7453 manish"+inputQty);
				valueXmlString.append("<quantity__stduom >").append("<![CDATA[" + inputQty + "]]>").append("</quantity__stduom>");

				mpending = lcOdrQty - lcqty ;
				System.out.println("mpending"+mpending+"= lcqty "+lcqty+" + mordqty"+mordqty);

				if("A".equalsIgnoreCase(lscancbo))
				{
					if(mOrdQty>0)
					{
						if((mpending / mOrdQty * 100) <= mCancperc)
						{
							valueXmlString.append("<canc_bo >").append("<![CDATA[Y]]>").append("</canc_bo>");
						}
						else
						{
							valueXmlString.append("<canc_bo >").append("<![CDATA[N]]>").append("</canc_bo>");
						}
					}

				}

				int llmfgval=0;
				Timestamp expDate12= null,expDate13=null;
				String lsshelflifetype="",lsmfgset="",lsmfgval="";
				sql="select (case when po_rate_option is null then 'N' else po_rate_option end), " +
						"(case when track_shelf_life is null then 'N' else track_shelf_life end), " +
						"(case when shelf_life is null then 0 else shelf_life end), " +
						"(case when retest_period is null then 0 else retest_period end) ," +
						" (case when shelf_life__type is null then 'E' else shelf_life__type end) " +
						" from item where item_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsitemcode);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					lsporateoption = checkNull(rs.getString(1));
					lstrackshelflife = checkNull(rs.getString(2));
					lishelflife = rs.getDouble(3);
					liretestperiod = rs.getDouble(4);
					lsshelflifetype = checkNull(rs.getString(5));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				System.out.println("lsporateoption"+lsporateoption);
				System.out.println("lstrackshelflife"+lstrackshelflife);
				System.out.println("lishelflife"+lishelflife);
				System.out.println("liretestperiod"+liretestperiod);
				System.out.println("lsshelflifetype"+lsshelflifetype);

				if(lsporateoption == null || lsporateoption.trim().length() == 0)
				{
					lsporateoption="N";	
				}
				if ("Y".equalsIgnoreCase(lstrackshelflife)) 
				{
					valueXmlString.append("<shelf_life__type >").append("<![CDATA[E]]>").append("</shelf_life__type>");
				}
				else
				{
					valueXmlString.append("<shelf_life__type >").append("<![CDATA[" + lsshelflifetype + "]]>").append("</shelf_life__type>");
				}
				if ("N".equalsIgnoreCase(lsporateoption)) 
				{
					/*gbf_itemchg_modifier_ds(dw_detedit[ii_currformno], "rate", "protect", "1")					
								dw_detedit[ii_currformno].modify("rate.Background.Color = " + dw_detedit[ii_currformno].describe("item_descr.background.color"))
								dw_detedit[ii_currformno].modify("rate.Color = " + string(RGB(255, 0, 0)))*/
				}
				if ("Y".equalsIgnoreCase(lstrackshelflife)) 
				{
					java.sql.Timestamp ldmfgdate=null,mfgcalExp=null;
					String mfgParseDate="";
					lsmfgset=disscommon.getDisparams("999999", "PRCP_MFG_EXP_DT_SET", conn);
					System.out.println("lsmfgset"+lsmfgset);
					mdateStr = genericUtility.getColumnValue("rec_date", dom1);
					System.out.println("mdateStr"+mdateStr);
					ldtmfgdate = java.sql.Timestamp.valueOf(genericUtility.getValidDateString(mdateStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");
					System.out.println("ldtmfgdate["+ldtmfgdate+"]");
					/*mfgParseDate=sdf1.format(ldmfgdate);
								System.out.println("mfgParseDate["+mfgParseDate+"]");*/
					if("Y".equalsIgnoreCase(lsmfgset))
					{
						lsmfgval =  disscommon.getDisparams("999999","PRCP_MFG_DATE_VALUE",conn);
						System.out.println("lsmfgval.." + lsmfgval);

						if( lsmfgval.equalsIgnoreCase("NULLFOUND") )
						{
							System.out.println("varValue is NULLFOUND" + lsmfgval);
							llmfgval=0;
						}
						else
						{
							llmfgval = Integer.parseInt(lsmfgval);
						}

						System.out.println("llmfgval = .. " + llmfgval);
						llmfgval = (-1) * (llmfgval *30);
						System.out.println("changed llmfgval>>>>>>>>>>>>>>>>  = .. " + llmfgval);
						ldmfgdate = utilMethod.RelativeDate(ldtmfgdate,llmfgval);
						System.out.println("ldtmfgdate Calculate["+ldtmfgdate+"]");
						sdf1 = new SimpleDateFormat(genericUtility.getApplDateFormat());
						mfgParseDate=sdf1.format(ldmfgdate);
						System.out.println("mfgParseDate F["+mfgParseDate+"]");
						valueXmlString.append("<expiry_date >").append("<![CDATA[]]>").append("</expiry_date>");
					}
					else if("U".equalsIgnoreCase(lsmfgset ))
					{
						valueXmlString.append("<expiry_date >").append("<![CDATA[]]>").append("</expiry_date>");
					}
					else
					{
						System.out.println("ldtmfgdate if lsmfgset null["+ldtmfgdate+"]");
						//mfgParseDate=sdf1.format(ldmfgdate);
						//Changed by Nandkumar Gadkari on 16/8/2018 to set rec_date as mfg_date
						mfgParseDate=checkNull(genericUtility.getColumnValue("rec_date",dom1));
						System.out.println("mfgParseDate recDate["+mfgParseDate+"]");
					}
					valueXmlString.append("<mfg_date >").append("<![CDATA[" + mfgParseDate + "]]>").append("</mfg_date>");
					mfgcalExp=java.sql.Timestamp.valueOf(genericUtility.getValidDateString(mfgParseDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");
					String mdateStr1 = genericUtility.getColumnValue( "expiry_date",dom);
					if(mdateStr1 == null && "Y".equalsIgnoreCase(lsmfgset))
					{
						System.out.println("mfgcalExp["+mfgcalExp+"]");
						System.out.println("lishelflife["+lishelflife+"]");
						expDate13=CalcExpiry1(mfgcalExp, lishelflife);
						System.out.println("expDate13["+expDate13+"]");
						String TestExpDate=sdf.format(expDate13);
						System.out.println("TestExpDate ["+TestExpDate+"]");
						valueXmlString.append("<expiry_date >").append("<![CDATA[" + TestExpDate + "]]>").append("</expiry_date>");
					}
					if("E".equalsIgnoreCase(lsshelflifetype))
					{
						if(mdateStr1== null)
						{
							mdateStr1="";
						}
						valueXmlString.append("<retest_date >").append("<![CDATA[" + mdateStr1 + "]]>").append("</retest_date>");
					}
					else
					{
						System.out.println("Else");
						System.out.println("mfgcalExp"+mfgcalExp);
						System.out.println("liretestperiod"+liretestperiod);
						expDate12=CalcExpiry1(mfgcalExp, liretestperiod);
						System.out.println("expDate12["+expDate12+"]");
						String TestExpDate1=sdf.format(expDate12);
						System.out.println("TestRetestDate ["+TestExpDate1+"]");
						valueXmlString.append("<retest_date >").append("<![CDATA[" + TestExpDate1 + "]]>").append("</retest_date>");
					}

				}//Added by Anagha Rane 25-02-2020 Start>> To set expiry date null
				else if("N".equalsIgnoreCase(lstrackshelflife))
				{
					valueXmlString.append("<expiry_date protect =\"1\">").append("<![CDATA[]]>").append("</expiry_date>");
					System.out.println("Setting Expiry date Null");
				}
				//Added by Anagha Rane 25-02-2020 End
				String lcrate1="",lspricelistclg="",lcqty1="",ldttrandate1="";
				lcrate1 = checkNull(genericUtility.getColumnValue( "rate", dom ));
				//Commented by Ajay on 25/04/18:START 
				//lcrate = (lcrate1== null || lcrate1.trim().length() == 0) ? 0 : Double.parseDouble(lcrate1);
				//Commented by Ajay on 25/04/18:END
				lsunit = checkNull(genericUtility.getColumnValue( "unit", dom ));
				lcqty1 = checkNull(genericUtility.getColumnValue( "quantity", dom ));
				lcqty= (lcqty1== null || lcqty1.trim().length() == 0) ? 0 : Double.parseDouble(lcqty1);
				ldttrandate1 = checkNull(genericUtility.getColumnValue( "tran_date", dom1 ));
				System.out.println("TRAN DAte is"+ldttrandate1);
				//ldttrandate = Timestamp.valueOf(genericUtility.getValidDateString(ldttrandate1,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");
				if(lcrate <= 0)
				{
					sql="select price_list from porder where purc_order = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mPordNo);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						lspricelist = checkNull(rs.getString(1));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if(lspricelist != null && lspricelist.trim().length() >0)
					{
						lcrate=disscommon.pickRate(lspricelist, ldttrandate1, lsitemcode, "", "L", lcqty, lsunit, conn);
						if(lcrate > 0)
						{
							valueXmlString.append("<rate protect =\"1\">").append("<![CDATA[" + lcrate + "]]>").append("</rate>");
							valueXmlString.append("<rate__clg >").append("<![CDATA[" + lcrate + "]]>").append("</rate__clg>");
							lsunitstd = checkNull(genericUtility.getColumnValue( "unit__std", dom ));
							lcrtconv1 = checkNull(genericUtility.getColumnValue( "conv__rtuom_stduom", dom ));
							lcrtconv=(lcrtconv1== null || lcrtconv1.trim().length() == 0) ? 0 : Double.parseDouble(lcrtconv1);
							if("R".equalsIgnoreCase(lsuomrnd))
							{
								lcstdqty1 =disscommon.getConvQuantityFact(lsunitstd, lsunit, lsitemcode, lcrate, lcrtconv, conn);
								if(lcstdqty1 != null && lcstdqty1.size() >0)
								{
									inputQty1 = Double.parseDouble(lcstdqty1.get(1).toString());
								}
							}
							else
							{
								lcstdqty1 =disscommon.getConvQuantityFact(lsunitstd, lsunit, lsitemcode, lcrate, lcrtconv, conn);
								if(lcstdqty1 != null && lcstdqty1.size() >0)
								{
									inputQty1 = Double.parseDouble(lcstdqty1.get(1).toString());
								}
							}
							if (lcconvtemp == 0)	
							{
								valueXmlString.append("<conv__rtuom_stduom >").append("<![CDATA[" + lcrtconv + "]]>").append("</conv__rtuom_stduom>");
							}	
							valueXmlString.append("<rate__stduom >").append("<![CDATA[" + inputQty1 + "]]>").append("</rate__stduom>");
						}

					}
				}
				String lcrateclg1="";
				lstrantype = checkNull(genericUtility.getColumnValue( "tran_type", dom ));
				lcrateclg1 =(genericUtility.getColumnValue( "rate__clg", dom ));
				lcrateclg=(lcrateclg1== null || lcrateclg1.trim().length() == 0)? 0 : Double.parseDouble(lcrateclg1);
				if(lcrateclg <=0)
				{
					sql="select udf_str2 from gencodes" +
							" where upper(fld_name) = 'TRAN_TYPE'   and upper(mod_name) = 'W_PORCP' and fld_value = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lstrantype);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						lspricelistclg = checkNull(rs.getString(1));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					sql="SELECT list_type from pricelist where price_list = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lspricelistclg);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						lslisttype = checkNull(rs.getString(1));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					lcrateclg=disscommon.pickRate(lspricelistclg, ldttrandate1, lsitemcode, "", lslisttype, lcqty, lsunit, conn);
					if(lcrateclg >0)
					{
						valueXmlString.append("<rate__clg >").append("<![CDATA[" + lcrateclg + "]]>").append("</rate__clg>");
					}
				}

				lslotsl=checkNull( genericUtility.getColumnValue("lot_sl", dom));
				System.out.println("LotSl is["+lslotsl+"]");
				varvalue=disscommon.getDisparams("999999", "MFG_LOT_SL", conn);
				System.out.println("LotSl disparam is"+varvalue);
				if(!"NULLFOUND".equalsIgnoreCase(varvalue) && varvalue != null && varvalue.trim().length() >0 && lslotsl.trim().length() == 0)
				{
					valueXmlString.append("<lot_sl >").append("<![CDATA[" + varvalue + "]]>").append("</lot_sl>");
				}
				else
				{
					varvalue=" ";
					valueXmlString.append("<lot_sl >").append("<![CDATA[" + varvalue + "]]>").append("</lot_sl>");
				}
				lslotsl=varvalue;

				mlotno = checkNull(genericUtility.getColumnValue("lot_no",dom ));
				//mlotsl = checkNull(genericUtility.getColumnValue("lot_sl", dom ));
				lssite = checkNull(genericUtility.getColumnValue("site_code", dom1));
				System.out.println("ItemCOde for MfgSIte["+lsitemcode+"]");
				System.out.println("lssite for MfgSIte["+lssite+"]");
				System.out.println("lslotsl for MfgSIte["+lslotsl+"]");
				System.out.println("mlotno for MfgSIte["+mlotno+"]");
				lssitemfg=gfgetmfgsite(lsitemcode,lssite,mlocation,mlotno,lslotsl,"M",conn);
				System.out.println("lssitemfg"+lssitemfg);
				System.out.println("ItemCOde for PackCode["+lsitemcode+"]");
				System.out.println("lssite for PackCode["+lssite+"]");
				System.out.println("lslotsl for PackCode["+lslotsl+"]");
				System.out.println("mlotno for PackCode["+mlotno+"]");
				lspack=gfgetmfgsite(lsitemcode,lssite,mlocation,mlotno,lslotsl,"P",conn);
				System.out.println("lspack"+lspack);
				valueXmlString.append("<site_code__mfg >").append("<![CDATA[" + lssitemfg + "]]>").append("</site_code__mfg>");
				if(!"NOTFOUND".equalsIgnoreCase(lspack))
				{
					valueXmlString.append("<pack_code >").append("<![CDATA[" + lspack + "]]>").append("</pack_code>");
				}
				if("NOTFOUND".equalsIgnoreCase(lssitemfg))
				{
					//Modified by Anjali R. on[09/10/2018][Remove space from tag value][Start]
					//valueXmlString.append("<site_code__mfg >").append("<![CDATA[ ]]> ").append("</site_code__mfg>");
					valueXmlString.append("<site_code__mfg >").append("<![CDATA[]]> ").append("</site_code__mfg>");
					//Modified by Anjali R. on[09/10/2018][Remove space from tag value][End]
				}
				lscostctr = checkNull(genericUtility.getColumnValue( "cctr_code__dr", dom ));
				if(lscostctr == null || lscostctr.trim().length()==0)
				{
					lscostctr = checkNull(genericUtility.getColumnValue( "cctr_code__cr", dom ));
				}
				lscostctrasloccode =disscommon.getDisparams("999999", "CCENTER_AS_LOCATION", conn);
				if(!"NULLFOUND".equalsIgnoreCase(lscostctrasloccode) && "Y".equalsIgnoreCase(lscostctrasloccode))
				{
					//mcode = dw_detedit[ii_currformno].GetItemString(dw_detedit[ii_currformno].getrow(), '')
					mcode=checkNull(genericUtility.getColumnValue( "item_code", dom ));
					sql="select (case when qc_reqd is null then 'N' else qc_reqd end) from item where item_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mcode);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						lsqcreqd = checkNull(rs.getString(1));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if("Y".equalsIgnoreCase(lsqcreqd))
					{
						lscctrloccode = lscostctr.trim()+"Q";
						valueXmlString.append("<loc_code >").append("<![CDATA[" + lscctrloccode + "]]>").append("</loc_code>");
					}
					else
					{
						lscctrloccode = lscostctr;
						valueXmlString.append("<loc_code >").append("<![CDATA[" + lscctrloccode + "]]>").append("</loc_code>");
					}
				}
				sql="select pord_type from porder where purc_order = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, mPordNo);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					lspotype = checkNull(rs.getString(1));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				sql="select udf_str1 from gencodes where fld_name='PORD_TYPE' and mod_name='W_PORDER' and fld_value = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lspotype);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					lsudf = checkNull(rs.getString(1));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(lsudf== null || lsudf.trim().length() == 0)
				{
					sql="select udf_str1 from gencodes where fld_name='PORD_TYPE' and mod_name='X' and fld_value = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lspotype);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						lsudf = checkNull(rs.getString(1));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
				valueXmlString.append("<grade>").append("<![CDATA[" + lsudf + "]]>").append("</grade>");
				lssite = checkNull(genericUtility.getColumnValue("site_code", dom1));
				sql="select qc_reqd from siteitem where item_code = ? and site_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsitemcode);
				pstmt.setString(2, lssite);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					lsqcreqd = rs.getString(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(lsqcreqd == null || lsqcreqd.trim().length() == 0)
				{
					sql="select (case when qc_reqd is null then 'N' else qc_reqd end)  from item where item_code =  ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsitemcode);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						lsqcreqd = rs.getString(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
				if(lsqcreqd == null || lsqcreqd.trim().length() == 0)
				{
					lsqcreqd="N";
				}
				lsautogeneratelotno =disscommon.getDisparams("999999","GENERATE_LOT_NO_AUTO", conn);
				if("NULLFOUND".equalsIgnoreCase(lsautogeneratelotno))
				{
					lsautogeneratelotno="N";
				}
				lsLotNoManualSite =disscommon.getDisparams("999999","LOT_NO_MANUAL_SITE", conn);
				if("NULLFOUND".equalsIgnoreCase(lsautogeneratelotno))
				{
					lsLotNoManualSite=" ";
				}
				sql="select ITEM_SER  from item  where item_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsitemcode);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					lsitemser = rs.getString(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				sql="Select auto_gen_lot  from itemser where item_ser = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsitemser);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					lsgenlotauto = rs.getString(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if("N".equalsIgnoreCase(lsgenlotauto))
				{
					valueXmlString.append("<lot_no protect =\"0\">").append("</lot_no>");
				}
				else
				{
					if("M".equalsIgnoreCase(lsautogeneratelotno))
					{
						if("Y".equalsIgnoreCase(lsqcreqd))
						{
							valueXmlString.append("<lot_no >").append("<![CDATA[ ]]>").append("</lot_no>");
							valueXmlString.append("<lot_no protect =\"1\">").append("</lot_no>");
						}
						else
						{
							valueXmlString.append("<lot_no protect =\"0\">").append("</lot_no>");
						}
					}
					else
					{
						if("Y".equalsIgnoreCase(lsqcreqd))
						{
							if("Y".equalsIgnoreCase(lsautogeneratelotno))
							{
								valueXmlString.append("<lot_no >").append("<![CDATA[ ]]> ").append("</lot_no>");
								valueXmlString.append("<lot_no protect =\"1\">").append("</lot_no>");
							}
							else
							{
								valueXmlString.append("<lot_no protect =\"0\">").append("</lot_no>");
							}
						}
					}
				}

				sql="select var_value  from disparm where  prd_code = '999999' and var_name = 'PORCP_SET_LOTNO' ";
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					lsdis = rs.getString(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if( "Y".equalsIgnoreCase(lsdis))
				{
					sql="select supp_sour,stk_opt from item where item_code =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsitemcode);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						lsgenlotauto = rs.getString(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if (("S".equalsIgnoreCase(lssource) ||  "P".equalsIgnoreCase(lssource)) &&  "2".equalsIgnoreCase(lsstkopt))
					{
						lstranid = checkNull(genericUtility.getColumnValue("tran_id", dom));
						//valueXmlString.append("<lot_no >").append(lstranid).append("</lot_no>");
						valueXmlString.append("<lot_no >").append("<![CDATA["+lstranid+"]]>").append("</lot_no>");

					}
				}

				lsitemcode=checkNull(genericUtility.getColumnValue( "item_code", dom ));
				lssitecode=checkNull(genericUtility.getColumnValue( "site_code", dom ));
				lsstkopt=gfChkStkOpt(lsitemcode,lssitecode,conn);
				System.out.println("StkOpt is"+lsstkopt);



				if("0".equalsIgnoreCase(lsstkopt))
				{
					valueXmlString.append("<effect_stock >").append("<![CDATA[N]]>").append("</effect_stock>");
				}
				else
				{
					valueXmlString.append("<effect_stock >").append("<![CDATA[Y]]>").append("</effect_stock>");
				}
				String lsbudgetamtanal1="",lsconsumedamtanal1="";
				double lsbudgetamtanal=0,lsconsumedamtanal=0,lcbudgetamt=0;
				if("1".equalsIgnoreCase(lsstkopt))
				{
					String str ="               ";
					String str1 ="     ";
					valueXmlString.append("<lot_no >").append("<![CDATA[" + str + "]]>").append("</lot_no>");
					valueXmlString.append("<lot_sl >").append("<![CDATA[" + str1 + "]]>").append("</lot_sl>");

				}
				lsanalcode=checkNull(genericUtility.getColumnValue( "anal_code", dom ));
				lsdeptcode=checkNull(genericUtility.getColumnValue( "dept_code", dom ));
				sql = "select FN_GET_BUDGET_AMT('P-RCP','" + lssitecode
						+ "','" + macdr + "','" + mctdr + "','"
						+ lsanalcode + "','" + lsdeptcode
						+ "','A') from dual";
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					lsbudgetamtanal1 = rs.getString(1);
				}
				rs.close();
				pstmt.close();
				rs = null;
				pstmt = null;
				System.out.println("budget amount.................. !["+ lsbudgetamtanal + "]");
				sql = "select FN_GET_CONS_AMT('P-RCP','" + lssitecode
						+ "','" + macdr + "','" + mctdr + "','"
						+ lsanalcode + "','" + lsdeptcode
						+ "','A') from dual";
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					lsconsumedamtanal1 = rs.getString(1);
				}
				rs.close();
				pstmt.close();
				rs = null;
				pstmt = null;
				lsbudgetamtanal= (lsbudgetamtanal1== null || lsbudgetamtanal1.trim().length() == 0) ?0 : Double.parseDouble(lsbudgetamtanal1);
				lsconsumedamtanal= (lsconsumedamtanal1== null || lsconsumedamtanal1.trim().length() == 0 ) ? 0 :Double.parseDouble(lsconsumedamtanal1);
				valueXmlString.append("<budget_amt_anal >").append("<![CDATA[" + lsbudgetamtanal + "]]>").append("</budget_amt_anal>");
				valueXmlString.append("<consumed_amt_anal >").append("<![CDATA[" + lsconsumedamtanal + "]]>").append("</consumed_amt_anal>");
				System.out.println("Budget AMount is"+lsbudgetamtanal);
				System.out.println("Consumed AMount is"+lsconsumedamtanal);
				lcbudgetamt = lsbudgetamtanal -lsconsumedamtanal;
				System.out.println("TOtal AMount is"+lcbudgetamt);
				valueXmlString.append("<budget_amt >").append("<![CDATA[" + lcbudgetamt + "]]>").append("</budget_amt>");
				//Add by Ajay on 10/04/18:START
				lsitemcode=checkNull(genericUtility.getColumnValue( "item_code", dom ));
				System.out.println("item_code:"+lsitemcode);
				sql = "select (case when stk_opt is null then '0' else stk_opt end) as stk_opt, item_ser  from item where item_code = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsitemcode);
				rs = pstmt.executeQuery();
				System.out.println("default edit sql ");
				if (rs.next())
				{
					stkOpt = checkNull(rs.getString("stk_opt")).trim();
					mcode = rs.getString("item_ser");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if("1".equalsIgnoreCase(stkOpt))
				{

					valueXmlString.append("<lot_no protect=\"1\"><![CDATA[").append("               ").append("]]></lot_no>\r\n");
					valueXmlString.append("<lot_sl protect=\"1\" ><![CDATA[").append("               ").append("]]></lot_sl>\r\n");

				}
				/*else
									{
										valueXmlString.append("<lot_no protect=\"0\"><![CDATA[").append(" ").append("]]></lot_no>\r\n");
				                        valueXmlString.append("<lot_sl protect=\"0\" ><![CDATA[").append(" ").append("]]></lot_sl>\r\n");
									}*/	//ELSE CONDITION COMMENTED BY NANDKUMAR GADKARI ON 19/07/18 FOR APPEND VALUE OF LOT_SL OVERRIDE 
				//Add by Ajay on 10/04/18:END

				//Add by Ajay on 19/04/18:START
				sql="select case when po_rate_option is null then 'N' else po_rate_option end" +
						" from	item where item_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsitemcode);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					lsporateoption = checkNull(rs.getString(1));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(lsporateoption == null || lsporateoption.trim().length() == 0)
				{
					lsporateoption="N";
				}
				if("N".equalsIgnoreCase(lsporateoption))
				{
					valueXmlString.append("<rate protect =\"1\">").append("<![CDATA["+lcrate+"]]>").append("</rate>");
				}
				else
				{
					valueXmlString.append("<rate protect =\"0\">").append("<![CDATA["+lcrate+"]]>").append("</rate>");
				}
				valueXmlString.append("<reas_code protect =\"1\">").append("<![CDATA["+reasCode+"]]>").append("</reas_code>");
				//Add by Ajay on 19/04/18:END



			}

			else if(currentColumn.trim().equalsIgnoreCase("item_code"))
			{
				System.out.println(" inside ItemCode  : " );
				String mpordno="",maccr="",lsunitrate="",lcqty1="",invAcct="",lssuppcd="",lsitemserhdr="",mpordline="",lsshelflifetype="";
				String acctDetrType="",lssuppcode="";
				String acctcodedr="",cctrcodedr="",acctcodecr="",cctrcodecr="";
				String lsmfgset="",mdateStr="",lsmfgval="",expDate="",expDate1="";
				int llmfgval=0;
				Timestamp expDate22=null,expDate24=null;

				double inputQty=0;
				mcode =  checkNull(genericUtility.getColumnValue( "item_code", dom ));
				System.out.println("ItemCode  : " +mcode);
				mpordno =  checkNull(genericUtility.getColumnValue( "purc_order", dom ));


				sql="Select item.item_ser,item.descr, item.ordc_perc, itemser.acct_code__pr, itemser.cctr_code__pr," +
						" itemser.acct_code__in, itemser.cctr_code__in, item.loc_code, item.unit, item.unit__pur, " +
						"item.pack_code,item.pack_instr, item.unit__rate from item, itemser " +
						"where item.item_code = ? and item.item_ser = itemser.item_ser ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, mcode);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					lsitemser = checkNull(rs.getString(1));

					mdescr = checkNull(rs.getString(2));
					mCancperc = rs.getDouble(3);
					maccr =  checkNull(rs.getString(4));
					mctcr = checkNull(rs.getString(5));
					macdr = checkNull(rs.getString(6));
					mctdr =  checkNull(rs.getString(7));
					lsloc =  checkNull(rs.getString(8));
					lsuom =  checkNull(rs.getString(9));
					lsunitpur =  checkNull(rs.getString(10));
					lspack1 =  checkNull(rs.getString(11));
					lspackinstr1 =  checkNull(rs.getString(12));
					lsunitrate = checkNull(rs.getString(13));


				}
				rs.close();
				pstmt.close();
				rs = null;
				pstmt = null;


				if(mpordno == null && mpordno.trim().length() >0)
				{
					valueXmlString.append("<item_descr >").append("<![CDATA[" + mdescr + "]]>").append("</item_descr>");
					valueXmlString.append("<loc_code >").append("<![CDATA[" + lsloc + "]]>").append("</loc_code>");
					if(lsunitrate == null || lsunitrate.trim().length() == 0)
					{
						lsunitrate = lsunitpur;
					}
					valueXmlString.append("<unit__std >").append("<![CDATA[" + lsuom + "]]>").append("</unit__std>");
					valueXmlString.append("<unit >").append("<![CDATA[" + lsunitpur + "]]>").append("</unit>");
					valueXmlString.append("<unit__rate >").append("<![CDATA[" + lsunitrate + "]]>").append("</unit__rate>");
					if(!lsuom.equalsIgnoreCase(lsunitpur))
					{
						lcqty1    = checkNull(genericUtility.getColumnValue( "quantity", dom ));
						lcqty=(lcqty1 == null || lcqty1.trim().length() == 0) ? 1 : Double.parseDouble(lcqty1);
						lctemp = lcqty;
						lcconv    = 0;
						if("Q".equalsIgnoreCase(lsuomrnd) || "B".equalsIgnoreCase(lsuomrnd) )
						{
							lcstdqty1 = null;
							lcstdqty1 = new ArrayList();
							lcstdqty1 =disscommon.getConvQuantityFact(mUom, mStdUom, lsitemcode, lcqty, lcQtyConv, conn); 
							if(lcstdqty1 != null && lcstdqty1.size() >0)
							{
								inputQty = Double.parseDouble(lcstdqty1.get(1).toString());
							}
						}
						else
						{
							lcstdqty1 =disscommon.getConvQuantityFact(mUom, mStdUom, lsitemcode, lcqty, lcQtyConv, conn); 
							if(lcstdqty1 != null && lcstdqty1.size() >0)
							{
								inputQty = Double.parseDouble(lcstdqty1.get(1).toString());
							}

						}
						valueXmlString.append("<conv__qty_stduom >").append("<![CDATA[" + lcconv + "]]>").append("</conv__qty_stduom>");
						System.out.println("inputQty>>> 8175"+inputQty);
						if(lctemp != 0)
						{
							valueXmlString.append("<quantity__stduom >").append("<![CDATA[" + inputQty + "]]>").append("</quantity__stduom>");
						}
						String lcrate1="";

						double lcrate=0;
						lcrate1   = checkNull(genericUtility.getColumnValue( "rate", dom ));
						lcrate=(lcrate1 == null || lcrate1.trim().length() == 0 )? 1 : Double.parseDouble(lcrate1);
						lctemp = lcqty;
						lcconv    = 0;
						if("R".equalsIgnoreCase(lsuomrnd) || "B".equalsIgnoreCase(lsuomrnd) )
						{
							lcstdqty1 = null;
							lcstdqty1 = new ArrayList();
							lcstdqty1 =disscommon.getConvQuantityFact(mUom, mStdUom, lsitemcode, lcqty, lcQtyConv, conn);
							if(lcstdqty1 != null && lcstdqty1.size()> 0)
							{
								inputQty = Double.parseDouble(lcstdqty1.get(1).toString());
							}
						}
						else
						{
							lcstdqty1 =disscommon.getConvQuantityFact(mUom, mStdUom, lsitemcode, lcqty, lcQtyConv, conn); 
							if(lcstdqty1 != null && lcstdqty1.size()> 0)
							{
								inputQty = Double.parseDouble(lcstdqty1.get(1).toString());
							}

						}
						valueXmlString.append("<conv__qty_stduom >").append("<![CDATA[" + lcconv + "]]>").append("</conv__qty_stduom>");
						System.out.println("inputQty>>>>>>8207 manish"+inputQty);
						if(lctemp != 0)
						{
							valueXmlString.append("<quantity__stduom >").append("<![CDATA[" + inputQty + "]]>").append("</quantity__stduom>");
						}
					}//end if trim(ls_uom) <> trim(ls_unitpur) then


					acctDetrType = acctDetrTType(mcode,lsitemser,"IN"," ");
					acctDetrList = new  ibase.utility.E12GenericUtility().getTokenList(acctDetrType,"\t");
					//valueXmlString.append("<acct_code__dr'>").append(acctDetrList.get(0)).append("</acct_code__dr>");
					acctcodedr=(String) acctDetrList.get(0);
					System.out.println("acctcodedr"+acctcodedr);
					//valueXmlString.append("<cctr_code__dr>").append(acctDetrList.get(1)).append("</cctr_code__dr>");
					cctrcodedr=(String) acctDetrList.get(1);
					invAcct = finCommon.getFinparams("999999","INV_ACCT_PORCP",conn);
					if (invAcct != null && invAcct.trim().equalsIgnoreCase("Y"))
					{
						acctDetrType = acctDetrTType(mcode,lsitemser,"PORCP"," "); //return acctCode and cctrCode
					}
					else
					{	
						acctDetrType = acctDetrTType(mcode,lsitemser,"PO"," "); //return acctCode and cctrCode
					}
					acctDetrList.clear();
					acctDetrList = new  ibase.utility.E12GenericUtility().getTokenList(acctDetrType,"\t");
					//valueXmlString.append("<acct_code__cr>").append(acctDetrList.get(0)).append("</acct_code__cr>");
					acctcodecr=(String) acctDetrList.get(0);
					System.out.println("acctcodecr"+acctcodecr);
					//valueXmlString.append("<cctr_code__cr>").append(acctDetrList.get(1)).append("</cctr_code__cr>");
					cctrcodecr=(String) acctDetrList.get(1);
					System.out.println("cctrcodecr"+cctrcodecr);
					if(acctcodedr == null || acctcodedr.trim().length() == 0)
					{
						lssuppcode = genericUtility.getColumnValue("supp_code",dom);
						sql="select acct_code__ap , cctr_code__ap from   supplier where  supp_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,acctcodecr);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							acctcodedr = rs.getString(1);
							cctrcodedr = rs.getString(2);
						}
						rs.close();
						pstmt.close();
						rs = null;
						pstmt = null;

					}
				}
				valueXmlString.append("<item_descr>").append(mdescr).append("</item_descr>");
				if(mpordno ==null || mpordno.trim().length() ==0)
				{
					System.out.println("acct_code__dr"+acctcodedr);
					System.out.println("acct_code__dr@@@@@@@@@@@@s"+acctDetrList.get(0));
					valueXmlString.append("<cctr_code__dr>").append("<![CDATA["+cctrcodedr+"]]>").append("</cctr_code__dr>");
					valueXmlString.append("<acct_code__cr>").append("<![CDATA["+acctcodecr+"]]>").append("</acct_code__cr>");
					valueXmlString.append("<cctr_code__cr>").append("<![CDATA["+cctrcodecr+"]]>").append("</cctr_code__cr>");
					valueXmlString.append("<dept_code>").append("<![CDATA[]]>").append("</dept_code>");
					sql="Select accounts.descr From accounts Where accounts.acct_code= ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,acctcodecr);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						lsaccdesc = rs.getString(1);
					}
					rs.close();
					pstmt.close();
					rs = null;
					pstmt = null;
					valueXmlString.append("<accounts_descr>").append(lsaccdesc).append("</accounts_descr>");

				}
				else
				{
					lsvalue = checkNull(genericUtility.getColumnValue( "acct_code__dr", dom ));
					if(lsvalue == null || lsvalue.trim().length() == 0)
					{
						valueXmlString.append("<acct_code__dr>").append("<![CDATA["+acctcodedr+"]]>").append("</acct_code__dr>");
					}
					lsvalue = checkNull(genericUtility.getColumnValue( "acct_code__cr", dom ));
					if(lsvalue == null || lsvalue.trim().length() == 0)
					{
						valueXmlString.append("<acct_code__cr>").append("<![CDATA["+acctcodecr+"]]>").append("</acct_code__cr>");
					}
					lsvalue = checkNull(genericUtility.getColumnValue( "cctr_code__dr", dom ));
					if(lsvalue == null || lsvalue.trim().length() == 0)
					{
						valueXmlString.append("<cctr_code__dr>").append("<![CDATA["+cctrcodedr+"]]>").append("</cctr_code__dr>");
					}
					lsvalue = checkNull(genericUtility.getColumnValue( "cctr_code__cr", dom ));
					if(lsvalue == null || lsvalue.trim().length() == 0)
					{
						valueXmlString.append("<cctr_code__cr>").append("<![CDATA["+cctrcodecr+"]]>").append("</cctr_code__cr>");
					}
					lsvalue = checkNull(genericUtility.getColumnValue( "dept_code", dom ));
					if(lsvalue == null || lsvalue.trim().length() == 0)
					{
						valueXmlString.append("<dept_code>").append("<![CDATA[]]>").append("</dept_code>");
					}
					mPordLine = checkNull(genericUtility.getColumnValue( "line_no__ord", dom ));
					if( mPordLine == null || mPordLine.trim().length() == 0)
					{

						lssuppcd =checkNull(genericUtility.getColumnValue( "supp_code", dom )); 
						lssite = checkNull(genericUtility.getColumnValue( "site_code", dom ));
						lsitemserhdr =checkNull(genericUtility.getColumnValue( "item_ser", dom ));
						sql="select d.purc_order, d.line_no from porder h, porddet d " +
								"where h.supp_code = ? " +
								"and	h.site_code__dlv = ? " +
								"and	h.item_ser = ? " +
								"and	h.status = 'O' " +
								"and	d.purc_order = h.purc_order " +
								"and	d.status = 'O'  " +
								"and	d.item_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lssuppcd);
						pstmt.setString(2, lssite);
						pstmt.setString(3, lsitemserhdr);
						pstmt.setString(4, mcode);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							mpordno = rs.getString(1);
							mPordLine = rs.getString(1);
						}
						rs.close();
						pstmt.close();
						rs = null;
						pstmt = null;
						valueXmlString.append("<purc_order>").append("<![CDATA["+mpordno.trim()+"]]>").append("</purc_order>");
						mPordLine = "    " + mPordLine;
						mPordLine = mPordLine.substring(mPordLine.length() - 3,mPordLine.length());
						valueXmlString.append("<line_no__ord>").append("<![CDATA["+mPordLine+"]]>").append("</line_no__ord>");
					}

					sql="select (case when po_rate_option is null then 'N' else po_rate_option end)," +
							"(case when track_shelf_life is null then 'N' else track_shelf_life end)," +
							" (case when shelf_life is null then 0 else shelf_life end)," +
							" (case when retest_period is null then 0 else retest_period end), " +
							"(case when shelf_life__type is null then 'E' else shelf_life__type end) from item where item_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,mcode);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						lsporateoption = checkNull(rs.getString(1));
						lstrackshelflife = checkNull(rs.getString(2));
						lishelflife = rs.getDouble(3);
						liretestperiod = rs.getDouble(4);
						lsshelflifetype = checkNull(rs.getString(5));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println("lsshelflifetype"+lsshelflifetype);
					valueXmlString.append("<shelf_life__type >").append("<![CDATA["+lsshelflifetype+"]]>").append("</shelf_life__type>");
					if(lsporateoption == null || lsporateoption.trim().length() == 0)
					{
						lsporateoption="N";	
					}
					if ("N".equalsIgnoreCase(lsporateoption)) 
					{
						/*gbf_itemchg_modifier_ds(dw_detedit[ii_currformno], "rate", "protect", "1")					
									dw_detedit[ii_currformno].modify("rate.Background.Color = " + dw_detedit[ii_currformno].describe("item_descr.background.color"))
									dw_detedit[ii_currformno].modify("rate.Color = " + string(RGB(255, 0, 0)))*/

					}
					Timestamp expDate12= null,expDate13=null;
					if ("Y".equalsIgnoreCase(lstrackshelflife)) 
					{

						java.sql.Timestamp ldmfgdate=null,mfgcalExp=null;
						String mfgParseDate="";
						lsmfgset=disscommon.getDisparams("999999", "PRCP_MFG_EXP_DT_SET", conn);
						System.out.println("lsmfgset"+lsmfgset);
						mdateStr = genericUtility.getColumnValue( "rec_date", dom1);
						System.out.println("mdateStr"+mdateStr);
						ldtmfgdate = java.sql.Timestamp.valueOf(genericUtility.getValidDateString(mdateStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");
						System.out.println("ldtmfgdate["+ldtmfgdate+"]");
						if("Y".equalsIgnoreCase(lsmfgset))
						{
							lsmfgval =  disscommon.getDisparams("999999","PRCP_MFG_DATE_VALUE",conn);
							System.out.println("lsmfgval.." + lsmfgval);

							if( lsmfgval.equalsIgnoreCase("NULLFOUND") )
							{
								System.out.println("varValue is NULLFOUND" + lsmfgval);
								llmfgval=0;
							}
							else
							{
								llmfgval = Integer.parseInt(lsmfgval);
							}

							System.out.println("llmfgval = .. " + llmfgval);
							llmfgval = (-1) * (llmfgval *30);
							System.out.println("changed llmfgval>>>>>>>>>>>>>>>>  = .. " + llmfgval);
							ldmfgdate = utilMethod.RelativeDate(ldtmfgdate,llmfgval);
							System.out.println("ldtmfgdate Calculate["+ldtmfgdate+"]");
							sdf1 = new SimpleDateFormat(genericUtility.getApplDateFormat());
							mfgParseDate=sdf1.format(ldmfgdate);
							System.out.println("mfgParseDate F["+mfgParseDate+"]");

							/*lsmfgval=disscommon.getDisparams("999999", "PRCP_MFG_DATE_VALUE", conn);
										if("NULLFOUND".equalsIgnoreCase(lsmfgval))
										{
											llmfgval=0;
										}
										llmfgval = Integer.parseInt(lsmfgval);
										System.out.println("lsmfgval["+lsmfgval+"]");
										System.out.println("llmfgval["+llmfgval+"]");
										ldmfgdate = utilMethod.RelativeDate(ldtmfgdate,(int) (-1 * (llmfgval *30)));*/
							valueXmlString.append("<expiry_date >").append("<![CDATA[]]>").append("</expiry_date>");
						}
						else if("U".equalsIgnoreCase(lsmfgset ))
						{
							valueXmlString.append("<expiry_date >").append("<![CDATA[]]>").append("</expiry_date>");
						}
						else
						{
							System.out.println("ldtmfgdate if lsmfgset null["+ldtmfgdate+"]");
							//mfgParseDate=sdf1.format(ldmfgdate);
							//Changed by Nandkumar Gadkari on 16/8/2018 to set rec_date as mfg_date
							mfgParseDate=checkNull(genericUtility.getColumnValue("rec_date",dom1));
							System.out.println("mfgParseDate recDate["+mfgParseDate+"]");
						}
						valueXmlString.append("<mfg_date >").append("<![CDATA[" + mfgParseDate + "]]>").append("</mfg_date>");
						mfgcalExp=java.sql.Timestamp.valueOf(genericUtility.getValidDateString(mfgParseDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+" 00:00:00.0");
						String mdateStr1 = genericUtility.getColumnValue( "expiry_date",dom);
						if(mdateStr1 == null && "Y".equalsIgnoreCase(lsmfgset))
						{
							System.out.println("mfgcalExp["+mfgcalExp+"]");
							System.out.println("lishelflife["+lishelflife+"]");
							expDate13=CalcExpiry1(mfgcalExp, lishelflife);
							System.out.println("expDate13["+expDate13+"]");
							String TestExpDate=sdf.format(expDate13);
							System.out.println("TestExpDate ["+TestExpDate+"]");
							valueXmlString.append("<expiry_date >").append("<![CDATA[" + TestExpDate + "]]>").append("</expiry_date>");
						}
						if("E".equalsIgnoreCase(lsshelflifetype))
						{
							if(mdateStr1== null)
							{
								mdateStr1="";
							}
							valueXmlString.append("<retest_date >").append("<![CDATA[" + mdateStr1 + "]]>").append("</retest_date>");
						}
						else
						{
							System.out.println("Else");
							System.out.println("mfgcalExp"+mfgcalExp);
							System.out.println("liretestperiod"+liretestperiod);
							expDate12=CalcExpiry1(mfgcalExp, liretestperiod);
							System.out.println("expDate12["+expDate12+"]");
							String TestExpDate1=sdf.format(expDate12);
							System.out.println("TestExpDate ["+TestExpDate1+"]");
							valueXmlString.append("<retest_date >").append("<![CDATA[" + TestExpDate1 + "]]>").append("</retest_date>");
						}


					}
					double lcrate=0,lcrtconv=0,inputQty1=0;
					String lcrate1="",lspricelistclg="",ldttrandate1="",lcrtconv1="";
					lsitemcode = checkNull(genericUtility.getColumnValue( "item_code", dom ));
					lcrate1 = checkNull(genericUtility.getColumnValue( "rate", dom ));
					lcrate= (lcrate1== null || lcrate1.trim().length() == 0)? 0 :Double.parseDouble(lcrate1);
					lsunit = checkNull(genericUtility.getColumnValue( "unit", dom ));
					lcqty1 = checkNull(genericUtility.getColumnValue( "quantity", dom ));
					lcqty= (lcqty1== null || lcqty1.trim().length() == 0)? 0 :Double.parseDouble(lcqty1);
					ldttrandate1 = checkNull(genericUtility.getColumnValue( "tran_date", dom1 ));
					System.out.println("TRAN DAte is"+ldttrandate1);
					if(lcrate <= 0)
					{
						System.out.println("mpordno"+mpordno);
						sql="select price_list from porder where purc_order = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mpordno);
						rs = pstmt.executeQuery();
						if (rs.next()) {
							lspricelist = checkNull(rs.getString(1));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if(lspricelist != null && lspricelist.trim().length() >0)
						{
							lcrate=disscommon.pickRate(lspricelist, ldttrandate1, lsitemcode, "", "L", lcqty, lsunit, conn);
							System.out.println("LC RATE"+lcrate);
							if(lcrate > 0)
							{
								valueXmlString.append("<rate >").append("<![CDATA[" + lcrate + "]]>").append("</rate>");
								valueXmlString.append("<rate__clg >").append("<![CDATA[" + lcrate + "]]>").append("</rate__clg>");
								lsunitstd = checkNull(genericUtility.getColumnValue( "unit__std", dom ));
								lcrtconv1 = checkNull(genericUtility.getColumnValue( "conv__rtuom_stduom", dom ));
								lcrtconv=(lcrtconv1 == null || lcrtconv1.trim().length() == 0) ? 0 :Double.parseDouble(lcrtconv1);
								if("R".equalsIgnoreCase(lsuomrnd) || "B".equalsIgnoreCase(lsuomrnd))
								{
									lcstdqty1 =disscommon.getConvQuantityFact(lsunitstd, lsunit, lsitemcode, lcrate, lcrtconv, conn); 
									if(lcstdqty1 != null )
									{
										inputQty1 = Double.parseDouble(lcstdqty1.get(1).toString());
									}
								}
								else
								{
									lcstdqty1 =disscommon.getConvQuantityFact(lsunitstd, lsunit, lsitemcode, lcrate, lcrtconv, conn);
									if(lcstdqty1 != null )
									{
										inputQty1 = Double.parseDouble(lcstdqty1.get(1).toString());
									}
								}
								if (lcconvtemp == 0)	
								{
									valueXmlString.append("<conv__rtuom_stduom >").append("<![CDATA[" + lcrtconv + "]]>").append("</conv__rtuom_stduom>");
								}	
								valueXmlString.append("<rate__stduom >").append("<![CDATA[" + inputQty1 + "]]>").append("</rate__stduom>");
							}

						}
					}
					String lcrateclg1="",lstrantype="",mlocation="";
					lstrantype = checkNull(genericUtility.getColumnValue( "tran_type", dom1 ));
					lcrateclg1 = checkNull(genericUtility.getColumnValue( "rate__clg", dom ));

					lcrateclg= (lcrateclg1 == null || lcrateclg1.trim().length() == 0 ) ? 0 : Double.parseDouble(lcrateclg1);
					if(lcrateclg <=0)
					{
						sql="select udf_str2 from gencodes" +
								" where upper(fld_name) = 'TRAN_TYPE'   and upper(mod_name) = 'W_PORCP' and fld_value = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lstrantype);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							lspricelistclg = checkNull(rs.getString(1));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						sql="SELECT list_type from pricelist where price_list = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lspricelistclg);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							lslisttype = checkNull(rs.getString(1));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						lcrateclg=disscommon.pickRate(lspricelistclg, ldttrandate1, lsitemcode, "", lslisttype, lcqty, lsunit, conn);
						System.out.println("lcrateclg"+lcrateclg);
						if(lcrateclg >0)
						{
							valueXmlString.append("<rate__clg >").append("<![CDATA[" + lcrateclg + "]]>").append("</rate__clg>");
						}
					}
					mcode = checkNull(genericUtility.getColumnValue( "pack_code", dom ));
					mlocation = checkNull(genericUtility.getColumnValue("loc_code",dom ));
					mlotno = checkNull(genericUtility.getColumnValue("lot_no",dom ));
					mlotsl = checkNull(genericUtility.getColumnValue("lot_sl", dom ));
					lssite = checkNull(genericUtility.getColumnValue("site_code", dom ));
					lssitemfg=gfgetmfgsite(lsitemcode,lssite,mlocation,mlotno,mlotsl,"M",conn);
					lspack=gfgetmfgsite(lsitemcode,lssite,mlocation,mlotno,mlotsl,"P",conn);
					valueXmlString.append("<site_code__mfg >").append("<![CDATA[" + lssitemfg + "]]>").append("</site_code__mfg>");
					if("NULLFOUND".equalsIgnoreCase(lspack))
					{
						valueXmlString.append("<pack_code >").append("<![CDATA[" + lspack + "]]>").append("</pack_code>");
					}
					if("NOTFOUND".equalsIgnoreCase(lssitemfg))
					{
						//valueXmlString.append("<site_code__mfg >").append(" ").append("</site_code__mfg>");
						//Modified by Anjali R. on[09/10/2018][Remove space from tag value][Start]
						//valueXmlString.append("<site_code__mfg >").append("<![CDATA[ ]]> ").append("</site_code__mfg>");
						valueXmlString.append("<site_code__mfg >").append("<![CDATA[]]> ").append("</site_code__mfg>");
						//Modified by Anjali R. on[09/10/2018][Remove space from tag value][End]

					}
					lscostctr = checkNull(genericUtility.getColumnValue( "cctr_code__dr", dom ));
					if(lscostctr == null || lscostctr.trim().length()==0)
					{
						lscostctr = checkNull(genericUtility.getColumnValue( "cctr_code__cr", dom ));
					}
					lscostctrasloccode =disscommon.getDisparams("999999", "CCENTER_AS_LOCATION", conn);
					if(!"NULLFOUND".equalsIgnoreCase(lscostctrasloccode) && "Y".equalsIgnoreCase(lscostctrasloccode))
					{
						//mcode = dw_detedit[ii_currformno].GetItemString(dw_detedit[ii_currformno].getrow(), '')
						mcode=checkNull(genericUtility.getColumnValue( "item_code", dom ));
						sql="select (case when qc_reqd is null then 'N' else qc_reqd end) from item where item_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mcode);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							lsqcreqd = checkNull(rs.getString(1));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						if("Y".equalsIgnoreCase(lsqcreqd))
						{
							lscctrloccode = lscostctr.trim()+"Q";
							valueXmlString.append("<loc_code >").append("<![CDATA[" + lscctrloccode + "]]>").append("</loc_code>");
						}
						else
						{
							lscctrloccode = lscostctr;
							valueXmlString.append("<loc_code >").append("<![CDATA[" + lscctrloccode + "]]>").append("</loc_code>");
						}
					}
					lsitemcode=checkNull(genericUtility.getColumnValue( "item_code", dom ));
					lssitecode=checkNull(genericUtility.getColumnValue( "site_code", dom ));
					lsstkopt=gfChkStkOpt(lsitemcode,lssitecode,conn);
					System.out.println("StkOpt is"+lsstkopt);
					if("0".equalsIgnoreCase(lsstkopt))
					{
						//valueXmlString.append("<effect_stock >").append("N").append("</effect_stock>");
						valueXmlString.append("<effect_stock >").append("<![CDATA[N]]>").append("</effect_stock>");

					}
					else
					{
						//valueXmlString.append("<effect_stock >").append("Y").append("</effect_stock>");
						valueXmlString.append("<effect_stock >").append("<![CDATA[Y]]>").append("</effect_stock>");

					}
					String lsbudgetamtanal1="",lsconsumedamtanal1="",lsdeptcode="";
					double lsbudgetamtanal=0,lsconsumedamtanal=0,lcbudgetamt=0;
					if("1".equalsIgnoreCase(lsstkopt))
					{
						String str ="               ";
						String str1 ="     ";
						valueXmlString.append("<lot_no >").append("<![CDATA[" + str + "]]>").append("</lot_no>");
						valueXmlString.append("<lot_sl >").append("<![CDATA[" + str1 + "]]>").append("</lot_sl>");

					}
					lsanalcode=checkNull(genericUtility.getColumnValue( "anal_code", dom ));
					lsdeptcode=checkNull(genericUtility.getColumnValue( "dept_code", dom ));
					sql = "select FN_GET_BUDGET_AMT('P-RCP','" + lssitecode
							+ "','" + macdr + "','" + mctdr + "','"
							+ lsanalcode + "','" + lsdeptcode
							+ "','A') from dual";
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						lsbudgetamtanal1 = rs.getString(1);
					}
					rs.close();
					pstmt.close();
					rs = null;
					pstmt = null;
					System.out.println("budget amount.................. !["+ lsbudgetamtanal + "]");
					sql = "select FN_GET_CONS_AMT('P-RCP','" + lssitecode
							+ "','" + macdr + "','" + mctdr + "','"
							+ lsanalcode + "','" + lsdeptcode
							+ "','A') from dual";
					pstmt = conn.prepareStatement(sql);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						lsconsumedamtanal1 = rs.getString(1);
					}
					rs.close();
					pstmt.close();
					rs = null;
					pstmt = null;
					lsbudgetamtanal=( lsbudgetamtanal1== null || lsbudgetamtanal1.trim().length()== 0)? 0 :Double.parseDouble(lsbudgetamtanal1);
					lsconsumedamtanal=(lsconsumedamtanal1 == null || lsconsumedamtanal1.trim().length()== 0) ? 0 :Double.parseDouble(lsconsumedamtanal1);
					valueXmlString.append("<budget_amt_anal >").append("<![CDATA[" + lsbudgetamtanal + "]]>").append("</budget_amt_anal>");
					valueXmlString.append("<consumed_amt_anal >").append("<![CDATA[" + lsconsumedamtanal + "]]>").append("</consumed_amt_anal>");
					System.out.println("Budget AMount is"+lsbudgetamtanal);
					System.out.println("Consumed AMount is"+lsconsumedamtanal);
					lcbudgetamt = lsbudgetamtanal -lsconsumedamtanal;
					System.out.println("TOtal AMount is"+lcbudgetamt);
					valueXmlString.append("<budget_amt >").append("<![CDATA[" + lcbudgetamt + "]]>").append("</budget_amt>");

				}

			}
			else if(currentColumn.trim().equalsIgnoreCase("acct_code__dr"))
			{
				String postprov="",lsacctprov="";
				lsacct =  checkNull(genericUtility.getColumnValue( "acct_code__dr", dom ));
				sql="select descr from accounts where acct_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsacct);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					mdescr = rs.getString(1);
				}
				rs.close();
				pstmt.close();
				rs = null;
				pstmt = null;
				valueXmlString.append("<accounts_descr >").append("<![CDATA[" + mdescr + "]]>").append("</accounts_descr>");
				postprov =  checkNull(genericUtility.getColumnValue( "post_prov", dom ));
				if("Y".equalsIgnoreCase(postprov))
				{
					sql="select acct_code__prov  from item_acctdetr_prov where  acct_type = 'IN' and	 tran_ser  = 'P-RCP' and	 acct_code =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsacct);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						lsacctprov = rs.getString(1);
					}
					rs.close();
					pstmt.close();
					rs = null;
					pstmt = null;
				}
				if(lsacctprov == null || lsacctprov.trim().length() == 0)
				{
					lsacctprov=" ";
				}
				valueXmlString.append("<acct_code__prov_dr >").append("<![CDATA[" + lsacctprov + "]]>").append("</acct_code__prov_dr>");


			}
			else if(currentColumn.trim().equalsIgnoreCase("acct_code__cr"))
			{
				String postprov="",lsacctprov="";
				lsacct =  checkNull(genericUtility.getColumnValue( "acct_code__cr", dom ));
				sql="select descr from accounts where acct_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,lsacct);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					mdescr = checkNull(rs.getString(1));
				}
				rs.close();
				pstmt.close();
				rs = null;
				pstmt = null;
				valueXmlString.append("<accounts_descr >").append("<![CDATA[" + mdescr + "]]>").append("</accounts_descr>");
				postprov =  checkNull(genericUtility.getColumnValue( "post_prov", dom ));
				if("Y".equalsIgnoreCase(postprov))
				{
					sql="select acct_code__prov  from item_acctdetr_prov where  acct_type = 'IN' and	 tran_ser  = 'P-RCP' and	 acct_code =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,lsacct);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						lsacctprov = rs.getString(1);
					}
					rs.close();
					pstmt.close();
					rs = null;
					pstmt = null;
				}
				if(lsacctprov == null || lsacctprov.trim().length() == 0)
				{
					lsacctprov=" ";
				}
				valueXmlString.append("<acct_code__prov_cr >").append("<![CDATA[" + lsacctprov + "]]>").append("</acct_code__prov_cr>");


			}
			else if(currentColumn.trim().equalsIgnoreCase("cctr_code__dr"))
			{
				String postprov="";
				lscostctr =  checkNull(genericUtility.getColumnValue( "cctr_code__dr", dom ));
				postprov =  checkNull(genericUtility.getColumnValue( "post_prov", dom ));
				if(!"Y".equalsIgnoreCase(postprov))
				{
					lscostctr=" ";
				}
				valueXmlString.append("<cctr_code__prov_dr >").append("<![CDATA[" + lscostctr + "]]>").append("</cctr_code__prov_dr>");
			}
			else if(currentColumn.trim().equalsIgnoreCase("cctr_code__cr"))
			{
				String postprov="";
				lscostctr =  checkNull(genericUtility.getColumnValue( "cctr_code__cr", dom ));
				postprov =  checkNull(genericUtility.getColumnValue( "post_prov", dom ));
				if(!"Y".equalsIgnoreCase(postprov))
				{
					lscostctr=" ";
				}
				valueXmlString.append("<cctr_code__prov_cr >").append("<![CDATA[" + lscostctr + "]]>").append("</cctr_code__prov_cr>");
			}
			else if(currentColumn.trim().equalsIgnoreCase("anal_code"))
			{
				lsanalcode =  checkNull(genericUtility.getColumnValue( "anal_code", dom ));
				sql="select descr from analysis where anal_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsanalcode);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					mdescr = rs.getString(1);
				}
				rs.close();
				pstmt.close();
				rs = null;
				pstmt = null;
				valueXmlString.append("<analysis_descr >").append("<![CDATA[" + mdescr + "]]>").append("</analysis_descr>");
			}
			else if(currentColumn.trim().equalsIgnoreCase("quantity"))
			{
				String lcQty1="",lcQtyConv1="",unit="",purcorder="",linenoord="";
				double lcQty=0,lcpreqty=0,lcqty1=0,inputQty=0;
				String lcrate1="",ldttrandate1="",lcqty11="",lcrtconv1="",lstrantype="",lcrateclg1="",lspricelistclg="";
				double lcrate=0,lcrtconv=0;
				lcQty1 = checkNull(genericUtility.getColumnValue( "quantity", dom ));
				System.out.println("Quantity is"+lcQty1);
				lcQty = (lcQty1 == null || lcQty1.trim().length() == 0) ? 0 : Double.parseDouble(lcQty1);
				System.out.println("Quantity Check null conditionis"+lcQty);
				valueXmlString.append("<realised_qty >").append("<![CDATA[" + lcQty + "]]>").append("</realised_qty>");
				valueXmlString.append("<supp_challan_qty >").append("<![CDATA[" + lcQty + "]]>").append("</supp_challan_qty>");
				valueXmlString.append("<excess_short_qty >").append("0").append("</excess_short_qty>");
				purcorder   =checkNull(genericUtility.getColumnValue( "purc_order", dom ));
				System.out.println("purcorder Check null conditionis"+purcorder);
				linenoord = checkNull(genericUtility.getColumnValue( "line_no__ord", dom ));
				System.out.println("line_no__ord"+linenoord);
				mcode=checkNull(genericUtility.getColumnValue( "item_code", dom));
				System.out.println("item_code conditionis"+mcode);
				System.out.println("Item code in detail "+mcode);
				linenoord = "    " + linenoord;
				linenoord = linenoord.substring(linenoord.length() - 3,linenoord.length());
				sql="	Select quantity , dlv_qty From PordDet where purc_order = ? and line_no = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, purcorder);
				pstmt.setString(2, linenoord);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					mOrdQty = rs.getDouble(1);
					mDlvQty= rs.getDouble(2);
				}
				rs.close();
				pstmt.close();
				rs = null;
				pstmt = null;
				lstranid = checkNull(genericUtility.getColumnValue( "tran_id", dom ));
				if(lstranid == null)
				{
					lstranid = "@@@@";
				}
				sql="select sum(porcpdet.quantity) from porcpdet , porcp where porcp.tran_id = porcpdet.tran_id and   porcpdet.tran_id <> ? and   porcpdet.Purc_order = ? and   porcpdet.line_no__ord = ? and   porcp.CONFIRMED = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lstranid);
				pstmt.setString(2, purcorder);
				pstmt.setString(3, linenoord);
				pstmt.setString(4, "N");
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					lcqty1 = rs.getDouble(1);
				}
				rs.close();
				pstmt.close();
				rs = null;
				pstmt = null;
				lcpreqty=lcpreqty+lcqty1;
				System.out.println("lcpreqty"+lcpreqty);
				System.out.println("lcqty1"+lcqty1);
				mpending=mOrdQty - (mDlvQty + lcQty + lcpreqty);
				System.out.println("mpending"+mpending+"= mOrdQty "+mOrdQty+" + -(mDlvQty"+mDlvQty+"+ lcQty +  "+lcQty+" lcpreqty"+lcpreqty);
				sql="Select (case when ordc_perc is null then 0 else ordc_perc end), (case when canc_bo_mode is null then 'A' else canc_bo_mode end) from item where item_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, mcode);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					mCancperc = rs.getDouble(1);
					lscancbo = rs.getString(2);
				}
				rs.close();
				pstmt.close();
				rs = null;
				pstmt = null;
				System.out.println("mCancperc"+mCancperc);
				System.out.println("lscancbo"+lscancbo);
				if("A".equalsIgnoreCase(lscancbo))
				{
					if(mOrdQty>0)
					{
						if((mpending / mOrdQty * 100) <= mCancperc)
						{
							//valueXmlString.append("<canc_bo >").append("Y").append("</canc_bo>");
							valueXmlString.append("<canc_bo >").append("<![CDATA[Y]]>").append("</canc_bo>");

						}
						else
						{
							//	valueXmlString.append("<canc_bo >").append("N").append("</canc_bo>");
							valueXmlString.append("<canc_bo >").append("<![CDATA[N]]>").append("</canc_bo>");

						}
					}

				}
				mVal  = checkNull(genericUtility.getColumnValue( "unit", dom ));
				mVal1 =checkNull(genericUtility.getColumnValue( "unit__std", dom )); 
				lsitemcode =checkNull(genericUtility.getColumnValue( "item_code", dom )); 
				System.out.println("unit"+mVal);
				System.out.println("lsitemcode"+lsitemcode);
				System.out.println("mVal1"+mVal1);
				lcQtyConv1 = checkNull(genericUtility.getColumnValue( "conv__qty_stduom", dom )); 
				lcQtyConv = (lcQtyConv1 == null || lcQtyConv1.trim().length() == 0) ? 0 : Double.parseDouble(lcQtyConv1);
				lcconvtemp = lcQtyConv;
				System.out.println("lcQtyConv:::::"+lcQtyConv);
				System.out.println("lsuomrnd:::::::"+lsuomrnd);
				double a=0;
				if(mVal1 == null || mVal1.trim().length() == 0)
				{
					sql="select unit from item where item_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsitemcode);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{

						unit = rs.getString(1);
					}
					rs.close();
					pstmt.close();
					rs = null;
					pstmt = null;
					if("Q".equalsIgnoreCase(lsuomrnd) || "B".equalsIgnoreCase(lsuomrnd))
					{
						lcstdqty1 = null;
						lcstdqty1 = new ArrayList();
						lcstdqty1 =disscommon.getConvQuantityFact(mVal, mVal1, lsitemcode, lcQty, lcQtyConv,conn);
						if(lcstdqty1 != null)
						{
							inputQty = Double.parseDouble(lcstdqty1.get(1).toString());

							System.out.println("inputQty#######"+inputQty);

							a = Double.parseDouble(lcstdqty1.get(0).toString());
							System.out.println("a#########"+a);
						}
					}
				}
				else
				{
					lcstdqty1 = null;
					lcstdqty1 = new ArrayList();
					lcstdqty1 =disscommon.getConvQuantityFact(mVal, mVal1, lsitemcode, lcQty, lcQtyConv, conn); 
					if(lcstdqty1 != null)
					{
						inputQty = Double.parseDouble(lcstdqty1.get(1).toString());
						System.out.println("inputQty else #######"+inputQty);
						a = Double.parseDouble(lcstdqty1.get(0).toString());
						System.out.println("a#########"+a);
					}
				}
				valueXmlString.append("<unit__std >").append("<![CDATA[" + mVal1 + "]]>").append("</unit__std>");
				if (lcconvtemp == 0)
				{
					valueXmlString.append("<conv__qty_stduom >").append("<![CDATA[" + lcQtyConv + "]]>").append("</conv__qty_stduom>");
				}
				System.out.println("inputQty"+inputQty);
				valueXmlString.append("<quantity__stduom >").append("<![CDATA[" + inputQty + "]]>").append("</quantity__stduom>");
				lsitemcode =checkNull(genericUtility.getColumnValue( "item_code", dom ));
				lcrate1 =genericUtility.getColumnValue( "rate", dom );
				lsunit =checkNull(genericUtility.getColumnValue( "unit", dom )); 
				lcqty11 =genericUtility.getColumnValue( "quantity", dom );
				purcorder =checkNull(genericUtility.getColumnValue( "purc_order", dom ));
				ldttrandate1 =checkNull(genericUtility.getColumnValue( "tran_date", dom1)); 
				lcrate = (lcrate1 == null || lcrate1.trim().length() == 0) ? 0 : Double.parseDouble(lcrate1);
				lcqty = (lcqty11 == null || lcqty11.trim().length() == 0) ? 0 : Double.parseDouble(lcqty11);
				if(lcrate <=0)
				{
					sql="select price_list from porder where purc_order =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, purcorder);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{

						lspricelist = rs.getString(1);
					}
					rs.close();
					pstmt.close();
					rs = null;
					pstmt = null;
					System.out.println("PriceList" +lspricelist);
					if(lspricelist !=null && lspricelist.trim().length()>0)
					{
						lcrate = disscommon.pickRate(lspricelist, ldttrandate1, lsitemcode, "", "L", lcqty, lsunit, conn) ;
						System.out.println("Lc Rate in PicRate is"+lcrate);
						if(lcrate >0)
						{
							valueXmlString.append("<rate >").append("<![CDATA[" + lcrate + "]]>").append("</rate>");
							lsunitstd =checkNull(genericUtility.getColumnValue( "unit__std", dom ));
							lcrtconv1 = genericUtility.getColumnValue( "conv__rtuom_stduom", dom );
							lcrtconv = (lcrtconv1 == null || lcrtconv1.trim().length() == 0 ) ? 0 : Double.parseDouble(lcrtconv1);
							lcconvtemp    = lcrtconv;
							if("Q".equalsIgnoreCase(lsuomrnd) || "B".equalsIgnoreCase(lsuomrnd))
							{
								lcstdqty1 = null;
								lcstdqty1 = new ArrayList();
								lcstdqty1 =disscommon.getConvQuantityFact(mVal, mVal1, lsitemcode, lcrate, lcrtconv,conn); 
								if(lcstdqty1 != null)
								{inputQty = Double.parseDouble(lcstdqty1.get(1).toString());}
							}
							else
							{
								lcstdqty1 = null;
								lcstdqty1 = new ArrayList();
								lcstdqty1 =disscommon.getConvQuantityFact(mVal, mVal1, lsitemcode, lcrate, lcrtconv, conn);
								if(lcstdqty1 != null)
								{inputQty = Double.parseDouble(lcstdqty1.get(1).toString());}
							}
							if(lcconvtemp == 0 )
							{
								valueXmlString.append("<conv__rtuom_stduom >").append("<![CDATA[" + lcrtconv + "]]>").append("</conv__rtuom_stduom>");

							}
							valueXmlString.append("<rate__stduom >").append("<![CDATA[" + inputQty + "]]>").append("</rate__stduom>");
						}
					}
				}
				lstrantype =checkNull(genericUtility.getColumnValue( "tran_type", dom1));
				lcrateclg1 =genericUtility.getColumnValue( "rate__clg", dom );
				System.out.println("lstrantype"+lstrantype);
				System.out.println("rate__clg"+lcrateclg1);
				if(lcrateclg1.trim().length() ==0)
				{
					lcrateclg1 = null;
				}
				lcrateclg = (lcrateclg1 == null || lcrateclg1.trim().length() == 0) ? 0 : Double.parseDouble(lcrateclg1);
				if(lcrateclg < 0)
				{
					sql="select udf_str2 from gencodes where upper(fld_name) = 'TRAN_TYPE'  and upper(mod_name) = 'W_PORCP' and fld_value = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lstrantype);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{

						lspricelistclg = rs.getString(1);
					}
					rs.close();
					pstmt.close();
					rs = null;
					pstmt = null;
					if(lspricelistclg == null || lspricelistclg.trim().length() ==0)
					{
						sql="select price_list__clg  from porder where purc_order = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, purcorder);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{

							lspricelistclg = rs.getString(1);
						}
						rs.close();
						pstmt.close();
						rs = null;
						pstmt = null;

					}
					if(lspricelistclg !=null && lspricelistclg.trim().length() >0)
					{
						sql="SELECT list_type from pricelist where price_list = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, purcorder);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{

							lspricelistclg = rs.getString(1);
						}
						rs.close();
						pstmt.close();
						rs = null;
						pstmt = null;

						lcrateclg =disscommon.pickRate(lspricelistclg, ldttrandate1, lsitemcode, "", lslisttype, lcqty, lsunit, conn);
						System.out.println("lcrateclg"+lcrateclg);
					}
					if(lcrateclg <=0)
					{
						lcrateclg = lcrate;
					}
					if(lcrateclg > 0)
					{
						valueXmlString.append("<rate__clg >").append("<![CDATA[" + lcrateclg + "]]>").append("</rate__clg>");
					}

				}
				//Pavan R 26aug19 start [no of art to consider from item_lot_packsize, item and if not found then packing master]
				lotno=checkNull(genericUtility.getColumnValue("lot_no",dom));
				if(lotno != null )
				{
					valueXmlString.append("<lot_no >").append("<![CDATA[" + lotno + "]]>").append("</lot_no>");
					setNodeValue( dom, "lot_no", lotno );
					reStr=itemChanged(dom,dom1, dom2, objContext,"lot_no",editFlag,xtraParams); //done
					System.out.println("after lot_no itemchanged.......");
					pos = reStr.indexOf("<Detail2>");
					reStr = reStr.substring(pos + 9);
					pos = reStr.indexOf("</Detail2>");
					reStr = reStr.substring(0,pos);
					valueXmlString.append(reStr);
				}
				//Pavan R 26aug19 end

			}
			else if(currentColumn.trim().equalsIgnoreCase("realised_qty"))
			{
				String lcrealisedqty1="",lcqty1="";
				double excessshortqty=0;
				lcrealisedqty1 =checkNull(genericUtility.getColumnValue( "realised_qty", dom )); 
				lcrealisedqty = (lcrealisedqty1 == null || lcrealisedqty1.trim().length() == 0) ? 0 : Double.parseDouble(lcrealisedqty1);
				lcqty1 = checkNull(genericUtility.getColumnValue( "quantity", dom ) );
				lcqty = (lcqty1 == null || lcqty1.trim().length() == 0 )? 0 : Double.parseDouble(lcqty1);
				System.out.println("excessshortqty=lcrealisedqty"+lcrealisedqty+" - lcqty"+lcqty);
				excessshortqty=lcrealisedqty - lcqty;
				System.out.println(excessshortqty+"="+lcrealisedqty+"-"+lcqty);
				valueXmlString.append("<excess_short_qty >").append("<![CDATA[" + excessshortqty + "]]>").append("</excess_short_qty>");
				if(excessshortqty != 0 )
				{
					lsloccode=disscommon.getDisparams("999999", "LOC_CODE__EX_SHT", conn);
				}
				if("NULLFOUND".equalsIgnoreCase(lsloccode))
				{
					lsloccode="";
				}
				valueXmlString.append("<loc_code__excess_short >").append("<![CDATA[" + lsloccode.trim() + "]]>").append("</loc_code__excess_short>");
			}
			else if(currentColumn.trim().equalsIgnoreCase("excess_short_qty"))
			{
				String lcqty1="";
				lcqty1 =genericUtility.getColumnValue( "excess_short_qty", dom ); 
				lcqty = (lcqty1 == null  || lcqty1.trim().length() == 0 ) ? 0 : Double.parseDouble(lcqty1);
				if(lcqty != 0)
				{
					lsloccode=disscommon.getDisparams("999999", "LOC_CODE__EX_SHT", conn);
				}
				if("NULLFOUND".equalsIgnoreCase(lsloccode))
				{
					lsloccode="";
				}
				valueXmlString.append("<loc_code__excess_short >").append("<![CDATA[" + lsloccode.trim() + "]]>").append("</loc_code__excess_short>");
			}
			else if(currentColumn.trim().equalsIgnoreCase("unit"))
			{
				String lcqty1="",mVal11="";
				double inputQty=0 ,qty=0,lcrate=0,lcrtconv=0;
				mcode =checkNull(genericUtility.getColumnValue( "unit", dom )); 
				mVal1 =checkNull(genericUtility.getColumnValue( "unit__std", dom )); 
				lsitemcode =checkNull(genericUtility.getColumnValue( "item_code", dom )); 
				lcqty1 =genericUtility.getColumnValue( "quantity", dom );
				qty=(lcqty1 == null || lcqty1.trim().length() == 0)? 0: Double.parseDouble(lcqty1);
				lcconvtemp = 0;
				if(mVal1 == null || mVal1.trim().length() ==0)
				{
					sql="Select unit from item where item_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsitemcode);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{

						mVal1 = rs.getString(1);
					}
					rs.close();
					pstmt.close();
					rs = null;
					pstmt = null;
					System.out.println("unit__std [unit]"+mVal1);
					valueXmlString.append("<unit__std >").append("<![CDATA[" + mVal1.trim() + "]]>").append("</unit__std>");
				}
				double inputQty1=0;
				lcstdqty1 = null;
				lcstdqty1 = new ArrayList();
				lcstdqty1 =disscommon.convQtyFactor(mcode, mVal1, lsitemcode, qty, lcconvtemp, conn); 
				if(lcstdqty1 != null && lcstdqty1.size() > 0 )
				{
					inputQty = Double.parseDouble(lcstdqty1.get(1).toString());
					lcconvtemp = Double.parseDouble(lcstdqty1.get(0).toString());
				}
				System.out.println("inputQty["+inputQty+"]");
				System.out.println("lcconvtemp["+lcconvtemp+"]");
				valueXmlString.append("<conv__qty_stduom >").append("<![CDATA[" + lcconvtemp + "]]>").append("</conv__qty_stduom>");
				valueXmlString.append("<quantity__stduom >").append("<![CDATA[" + inputQty + "]]>").append("</quantity__stduom>"); 
			}//ibase3-webitm-dis5-12-105-1
			else if(currentColumn.trim().equalsIgnoreCase("conv__qty_stduom"))
			{
				String lcqty1="",mVal11="",lcqtyconv1="",lcrate1="",lcrtconv1="";
				double inputQty=0 ,lcqtyconv=0,lcrate=0,lcrtconv=0;
				lcqtyconv1 =genericUtility.getColumnValue( "conv__qty_stduom", dom ); 
				lcqtyconv=(lcqtyconv1 == null || lcqtyconv1.trim().length()== 0) ? 0: Double.parseDouble(lcqtyconv1);
				mcode =checkNull(genericUtility.getColumnValue( "unit", dom )); 
				mVal1 =checkNull(genericUtility.getColumnValue( "unit__std", dom )); 
				lsitemcode =checkNull(genericUtility.getColumnValue( "item_code", dom )); 
				lcqty1 =genericUtility.getColumnValue( "quantity", dom ); 
				lcqty=(lcqty1 == null || lcqty1.trim().length() == 0)? 0: Double.parseDouble(lcqty1);
				lcconvtemp = 0;
				if(mVal1 == null || mVal1.trim().length() ==0)
				{
					sql="Select unit from item where item_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsitemcode);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{

						mVal1 = rs.getString(1);
					}
					rs.close();
					pstmt.close();
					rs = null;
					pstmt = null;
					System.out.println("unit__std[conv__qty_stduom]"+mVal1);
					valueXmlString.append("<unit__std >").append("<![CDATA[" + mVal1.trim() + "]]>").append("</unit__std>");
				}
				System.out.println("lcqtyconv["+lcqtyconv+"]");
				double inputQty1=0;
				lcstdqty1 = null;
				lcstdqty1 = new ArrayList();
				lcstdqty1 =disscommon.getConvQuantityFact(mcode, mVal1, lsitemcode, lcqty, lcqtyconv, conn); 
				if(lcstdqty1 != null)
				{
					inputQty = Double.parseDouble(lcstdqty1.get(1).toString());
					lcqtyconv = Double.parseDouble(lcstdqty1.get(0).toString());
				}
				System.out.println("inputQty["+inputQty+"]");
				System.out.println("lcqtyconv return["+lcqtyconv+"]");
				valueXmlString.append("<quantity__stduom >").append("<![CDATA[" + inputQty + "]]>").append("</quantity__stduom>"); 
				lcrate1 =checkNull(genericUtility.getColumnValue( "rate", dom )); 
				lcrate=(lcrate1 == null || lcrate1.trim().length() == 0) ? 0: Double.parseDouble(lcrate1);
				lcrtconv = 1 / lcqtyconv;
				lcratestduom  = lcrate * lcrtconv;
				valueXmlString.append("<conv__rtuom_stduom >").append("<![CDATA[" + lcrtconv + "]]>").append("</conv__rtuom_stduom>");
				valueXmlString.append("<rate__stduom >").append("<![CDATA[" + lcratestduom + "]]>").append("</rate__stduom>");
			}
			else if(currentColumn.trim().equalsIgnoreCase("rate"))
			{
				String lcrate1="",mVal11="",lcrtconv1="";
				double lcrtconv=0,mval=0,inputQty=0, lcrate=0;
				lcrate1  = checkNull(genericUtility.getColumnValue( "rate", dom )); 
				lcrate=(lcrate1 == null ||  lcrate1.trim().length() == 0) ? 0 : Double.parseDouble(lcrate1);
				mVal =checkNull(genericUtility.getColumnValue( "unit__rate", dom ));
				mVal1 =checkNull(genericUtility.getColumnValue( "unit__std", dom )); 
				lsitemcode = checkNull(genericUtility.getColumnValue( "item_code", dom ));
				if(mVal1 == null || mVal1.trim().length() ==0 )
				{
					sql="Select unit from item where item_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsitemcode);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{

						mVal1 = rs.getString(1);
					}
					rs.close();
					pstmt.close();
					rs = null;
					pstmt = null;
					System.out.println("unit__std[rate]"+mVal1);
					valueXmlString.append("<unit__std >").append("<![CDATA[" + mVal1.trim() + "]]>").append("</unit__std>");
				}
				lcrtconv1=checkNull(genericUtility.getColumnValue( "conv__rtuom_stduom", dom )); 
				lcrtconv=(lcrtconv1==null || lcrtconv1.trim().length() == 0 )? 0 : Double.parseDouble(lcrtconv1);
				lcconvtemp = lcrtconv;
				System.out.println("lcrtconv(conv__rtuom_stduom)["+lcrtconv+"]");
				if( mVal == null || mVal.trim().length() == 0)
				{
					sql="Select unit from item where item_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsitemcode);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{

						mVal11 = rs.getString(1);
					}
					rs.close();
					pstmt.close();
					rs = null;
					pstmt = null;
					lcstdqty1 = null;
					lcstdqty1 = new ArrayList();
					lcstdqty1 =disscommon.getConvQuantityFact(mVal1, mVal, lsitemcode, lcrate, lcrtconv, conn); 
					if(lcstdqty1 != null)
					{
						inputQty = Double.parseDouble(lcstdqty1.get(1).toString());
						lcrtconv = Double.parseDouble(lcstdqty1.get(0).toString());
						System.out.println("inputQty["+inputQty+"]");
						System.out.println("lcrtconv["+lcrtconv+"]");
					}
					valueXmlString.append("<unit__rate >").append("<![CDATA[" + mVal + "]]>").append("</unit__rate>");
				}
				else
				{
					lcstdqty1 = null;
					lcstdqty1 = new ArrayList();
					lcstdqty1 =disscommon.getConvQuantityFact(mVal1, mVal, lsitemcode, lcrate, lcrtconv, conn); 
					if(lcstdqty1 != null)
					{
						inputQty = Double.parseDouble(lcstdqty1.get(1).toString());
						lcrtconv = Double.parseDouble(lcstdqty1.get(0).toString());
						System.out.println("Else Rate inputQty["+inputQty+"]");
						System.out.println("Else Rate inputQty["+lcrtconv+"]");
					}
				}
				if(lcconvtemp ==  0)
				{
					valueXmlString.append("<conv__rtuom_stduom >").append("<![CDATA[" + lcrtconv + "]]>").append("</conv__rtuom_stduom>");
				}
				System.out.println("inputQty Append["+inputQty+"]");
				valueXmlString.append("<rate__stduom >").append("<![CDATA[" + inputQty + "]]>").append("</rate__stduom>");
			}	
			else if(currentColumn.trim().equalsIgnoreCase("unit__rate"))
			{
				String lcrate1="",mVal11="",lcrtconv1="";
				double lcrate=0,lcrtconv=0,inputQty=0;
				mcode  =checkNull(genericUtility.getColumnValue( "unit__rate", dom )); 
				mVal1 =checkNull(genericUtility.getColumnValue( "unit__std", dom )); 
				lsitemcode = checkNull(genericUtility.getColumnValue( "item_code", dom ));
				lcrate1=checkNull(genericUtility.getColumnValue( "rate", dom )); 
				lcrate = (lcrate1 == null || lcrate1.trim().length() == 0)  ? 0 : Double.parseDouble(lcrate1);
				if(mVal1 == null || mVal1.trim().length() == 0)
				{
					sql="Select unit from item where item_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsitemcode);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{

						mVal1 = rs.getString(1);
					}
					rs.close();
					pstmt.close();
					rs = null;
					pstmt = null;
					System.out.println("unit__std [unit__rate]"+mVal1);
					valueXmlString.append("<unit__std >").append("<![CDATA[" + mVal1.trim() + "]]>").append("</unit__std>");
				}
				lcrtconv = 0;
				double inputQty1=0;
				lcstdqty1 = null;
				lcstdqty1 = new ArrayList();
				lcstdqty1 =disscommon.getConvQuantityFact(mVal1, mcode, lsitemcode, lcrate, lcrtconv, conn); 
				System.out.println("lcstdqty1["+lcstdqty1+"]");
				if(lcstdqty1 != null)
				{
					inputQty = Double.parseDouble(lcstdqty1.get(1).toString());
					lcrtconv = Double.parseDouble(lcstdqty1.get(0).toString());
				}
				System.out.println("lcstdqty1["+inputQty+"]");
				System.out.println("lcrtconv["+lcrtconv+"]");
				valueXmlString.append("<conv__rtuom_stduom >").append("<![CDATA[" + lcrtconv + "]]>").append("</conv__rtuom_stduom>");
				valueXmlString.append("<rate__stduom >").append("<![CDATA[" + inputQty + "]]>").append("</rate__stduom>");
			}
			else if(currentColumn.trim().equalsIgnoreCase("conv__rtuom_stduom"))
			{
				String lcrate1="",mVal11="",lcrtconv1="", ratestduom1="";
				double lcrate=0,lcrtconv=0,inputQty=0,ratestduom=0;
				lcrtconv1  =checkNull(genericUtility.getColumnValue( "conv__rtuom_stduom", dom ));  
				lcrtconv= (lcrtconv1 == null || lcrtconv1.trim().length() == 0)? 0 : Double.parseDouble(lcrtconv1);
				mcode  =checkNull(genericUtility.getColumnValue( "unit__rate", dom )); 
				mVal1 =checkNull(genericUtility.getColumnValue( "unit__std", dom )); 
				lsitemcode = checkNull(genericUtility.getColumnValue( "item_code", dom ));
				if(mVal1 == null || mVal1.trim().length() == 0)
				{
					sql="Select unit from item where item_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lsitemcode);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{

						mVal1 = rs.getString(1);
					}
					rs.close();
					pstmt.close();
					rs = null;
					pstmt = null;
					System.out.println("unit__std"+mVal1);
					valueXmlString.append("<unit__std >").append("<![CDATA[" + mVal1.trim() + "]]>").append("</unit__std>");
				}
				lcrate1 = checkNull (genericUtility.getColumnValue( "rate", dom ));
				lcrate= (lcrate1 == null || lcrate1.trim().length() == 0) ? 0 : Double.parseDouble(lcrate1);

				if(lcrtconv > 0)
				{
					double inputQty1 =0;
					lcstdqty1 = null;

					lcstdqty1 = new ArrayList();
					lcstdqty1 =disscommon.getConvQuantityFact(mVal1, mVal, lsitemcode, lcrate, lcrtconv, conn); 
					System.out.println("lcstdqty1["+lcstdqty1+"]");
					if(lcstdqty1 != null)
					{
						inputQty = Double.parseDouble(lcstdqty1.get(1).toString());
						inputQty1 = Double.parseDouble(lcstdqty1.get(0).toString());
					}
					System.out.println("inputQty["+inputQty+"]");
					System.out.println("inputQty1["+inputQty1+"]");
					valueXmlString.append("<rate__stduom >").append("<![CDATA[" + inputQty + "]]>").append("</rate__stduom>");
				}
				valueXmlString.append("<rate__stduom >").append("<![CDATA[" + inputQty + "]]>").append("</rate__stduom>");

			}
			else if(currentColumn.trim().equalsIgnoreCase("gross_weight"))
			{
				String lcgrosswt1="", lctarewt1="";

				lcgrosswt1 =checkNull(genericUtility.getColumnValue( "gross_weight", dom ));  
				lctarewt1 = checkNull(genericUtility.getColumnValue( "tare_weight", dom ));
				if(lcgrosswt1==null || lcgrosswt1.trim().length() == 0)
				{
					// valueXmlString.append("<gross_weight >").append("0").append("</gross_weight>");
					valueXmlString.append("<gross_weight >").append("<![CDATA[0]]>").append("</gross_weight>");

				}
				if(lctarewt1==null || lctarewt1.trim().length() == 0)
				{
					// valueXmlString.append("<tare_weight >").append("0").append("</tare_weight>");
					valueXmlString.append("<tare_weight >").append("<![CDATA[0]]>").append("</tare_weight>");

				}
				lcgrosswt=(lcgrosswt1 == null || lcgrosswt1.trim().length() == 0 )  ? 0 : Double.parseDouble(lcgrosswt1);
				lctarewt=(lctarewt1 == null || lctarewt1.trim().length() == 0) ? 0 : Double.parseDouble(lctarewt1);
				System.out.println("lcgrosswt["+lcgrosswt+"]");
				System.out.println("tare_weight["+lctarewt+"]");
				if(lcgrosswt == 0 && lctarewt ==0)
				{
					lcnetwt=0;
					valueXmlString.append("<net_weight >").append("<![CDATA[" + lcnetwt + "]]>").append("</net_weight>");
				}
				lcnetwt = Math.abs(lcgrosswt - lctarewt);
				System.out.println("lcnetwt["+lcnetwt+"]");
				valueXmlString.append("<net_weight >").append("<![CDATA[" + lcnetwt + "]]>").append("</net_weight>");

			}
			else if(currentColumn.trim().equalsIgnoreCase("tare_weight"))
			{
				String lcgrosswt2="", lctarewt2="";
				double lcnetwt2=0;
				lcgrosswt2 = checkNull(genericUtility.getColumnValue( "gross_weight", dom ));  
				lctarewt2 =checkNull(genericUtility.getColumnValue( "tare_weight", dom ));
				System.out.println("lcgrosswt2["+lcgrosswt2+"]");
				System.out.println("tare_weight2["+lctarewt2+"]");
				if(lcgrosswt2==null || lcgrosswt2.trim().length() == 0)
				{
					//  valueXmlString.append("<gross_weight >").append("0").append("</gross_weight>");
					valueXmlString.append("<gross_weight >").append("<![CDATA[0]]>").append("</gross_weight>");


				}
				if(lctarewt2==null || lctarewt2.trim().length() == 0)
				{
					// valueXmlString.append("<tare_weight >").append("0").append("</tare_weight>");
					valueXmlString.append("<tare_weight >").append("<![CDATA[0]]>").append("</tare_weight>");

				}
				lcgrosswt=(lcgrosswt2 == null ||  lcgrosswt2.trim().length() == 0) ? 0 : Double.parseDouble(lcgrosswt2);
				lctarewt=(lctarewt2 == null || lctarewt2.trim().length() == 0) ? 0 : Double.parseDouble(lctarewt2);
				if(lcgrosswt == 0 && lctarewt ==0)
				{
					lcnetwt2=0;
					valueXmlString.append("<net_weight >").append("<![CDATA[" + lcnetwt2 + "]]>").append("</net_weight>");
				}
				lcnetwt2 = Math.abs(lcgrosswt - lctarewt);
				System.out.println("lcnetwt2["+lcnetwt2+"]");
				valueXmlString.append("<net_weight >").append("<![CDATA[" + lcnetwt2 + "]]>").append("</net_weight>");
			}
			else if(currentColumn.trim().equalsIgnoreCase("mfg_date"))
			{
				java.sql.Timestamp mmfgDate = null;//,expirydate=null;
				Timestamp expDate11=null,expDate12=null;
				String dtStr2="";
				String lsshelflifetype="",lsmfgset="",expDate="";
				lsitemcode = genericUtility.getColumnValue( "item_code", dom ); //  dw_detedit[ii_currformno].getitemstring(1,"item_code")
				String dtStr = genericUtility.getColumnValue( "mfg_date", dom ); //dw_detedit[ii_currformno].getitemdatetime(1,"mfg_date")
				System.out.println("dtStr[mfg_date]["+dtStr+"]");
				if(dtStr != null)
				{
					mmfgDate = Timestamp.valueOf(genericUtility.getValidDateString(dtStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
					System.out.println("Format mmfgDate[mfg_date]["+mmfgDate+"]");
				}
				sql="select (case when track_shelf_life is null then 'N' else track_shelf_life end)," +
						"(case when shelf_life is null then 0 else shelf_life end )," +
						"(case when retest_period is null then 0 else retest_period end )" +
						" ,(case when shelf_life__type is null then 'E' else shelf_life__type end ) " +
						"from item where item_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsitemcode);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{

					lstrackshelflife = rs.getString(1);
					lishelflife = rs.getInt(2);
					liretestperiod = rs.getInt(3);
					lsshelflifetype = rs.getString(4);

				}
				rs.close();
				pstmt.close();
				rs = null;
				pstmt = null;
				valueXmlString.append("<shelf_life__type >").append("<![CDATA[" + lsshelflifetype + "]]>").append("</shelf_life__type>");
				if("Y".equalsIgnoreCase(lstrackshelflife))
				{
					if( dtStr == null)
					{
						Calendar currentDate = Calendar.getInstance();
						sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
						sysDate = sdf.format(currentDate.getTime());
						System.out.println("Now the date is :=>  " + sysDate);
						valueXmlString.append("<mfg_date >").append("<![CDATA[" + sysDate + "]]>").append("</mfg_date>");
					}
					lsmfgset = disscommon.getDisparams("999999", "PRCP_MFG_EXP_DT_SET", conn);
					String dtStr1 = genericUtility.getColumnValue( "expiry_date", dom ); //dw_detedit[ii_currformno].getitemdatetime(1,"mfg_date")
					System.out.println("dtStr1["+dtStr1+"]");//dhiraj
					if(dtStr1 != null)
					{
						/*expirydate = Timestamp.valueOf(genericUtility.getValidDateString(dtStr1, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
									System.out.println("expirydate@D["+expirydate+"]");//dhiraj
						 */								   dtStr2=dtStr1;
						 System.out.println("dtStr2["+dtStr2+"]");
					}
					if(!"Y".equalsIgnoreCase(lsmfgset) && !"U".equalsIgnoreCase(lsmfgset))
					{
						//expDate=Dis  calcExpiry(dtStr,lishelflife);
						expDate11 = CalcExpiry1(mmfgDate, lishelflife);
						System.out.println("expDate11["+expDate11+"]");
						String expDate4=sdf.format(expDate11);
						System.out.println("expDate4 ["+expDate4+"]");
						valueXmlString.append("<expiry_date >").append("<![CDATA[" + expDate4 + "]]>").append("</expiry_date>");
					}
					else if("Y".equalsIgnoreCase(lsmfgset))
					{
						valueXmlString.append("<expiry_date >").append("</expiry_date>");
					}
					if("E".equalsIgnoreCase(lsshelflifetype))
					{
						System.out.println("dtStr1 Exp Date "+dtStr1);
						if(dtStr1 != null)
						{
							/*expirydate = Timestamp.valueOf(genericUtility.getValidDateString(dtStr1, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
										System.out.println("expirydate@D@E["+expirydate.toString()+"]");//dhiraj
							 */									     dtStr2=dtStr1;
							 System.out.println("dtStr2["+dtStr2+"]");
							 //expirydate = CalcExpiry1(mmfgDate, lishelflife);//DHIRAJ
							 //System.out.println("expirydate@CalcX@E["+expirydate+"]");//dhiraj
						}
						valueXmlString.append("<retest_date >").append("<![CDATA[" + dtStr2 + "]]>").append("</retest_date>");
					}
					else
					{
						expDate12=CalcExpiry1(mmfgDate,lishelflife);
						System.out.println("expDate12["+expDate12+"]");
						String expDate5=sdf.format(expDate12);
						System.out.println("expDate5@["+expDate5+"]");
						valueXmlString.append("<retest_date >").append("<![CDATA[" + expDate5 + "]]>").append("</retest_date>");
					}

				}
			}
			else if(currentColumn.trim().equalsIgnoreCase("loc_code"))
			{
				String mlocation="";
				Timestamp mfgDate = null, expDate = null, retestDate = null;
				mlocation = checkNull(genericUtility.getColumnValue( "loc_code", dom ));
				mlotno = checkNull(genericUtility.getColumnValue( "lot_no", dom ));
				mlotsl = checkNull(genericUtility.getColumnValue( "lot_sl", dom ));
				mitem = checkNull(genericUtility.getColumnValue( "item_code", dom ));
				lssite = genericUtility.getColumnValue( "site_code", dom1 );
				System.out.println("mlocation "+mlocation);
				System.out.println("mlotno "+mlotno);
				System.out.println("mlotsl "+mlotsl);
				System.out.println("mitem "+mitem);
				System.out.println("lssite "+lssite);
				lssitemfg = gfgetmfgsite(mitem,lssite,mlocation,mlotno,mlotsl,"M",conn);
				System.out.println("lssitemfg "+lssitemfg);
				lspack = gfgetmfgsite(mitem,lssite,mlocation,mlotno,mlotsl,"P",conn);
				System.out.println("lspack "+lspack);
				valueXmlString.append("<site_code__mfg >").append("<![CDATA[" + lssitemfg + "]]>").append("</site_code__mfg>");
				if(!"NOTFOUND".equalsIgnoreCase(lspack)	)
				{
					valueXmlString.append("<pack_code >").append("<![CDATA[" + lspack + "]]>").append("</pack_code>");
				}
				//Modified by Anjali R. on [Change condition][Start]
				//if("NOTFOUND".equalsIgnoreCase(lssitemfg));
				if("NOTFOUND".equalsIgnoreCase(lssitemfg))
				{
					//Modified by Anjali R. on [Change condition][End]
					//Modified by Anjali R. on[09/10/2018][Remove space from tag value][Start]
					//valueXmlString.append("<site_code__mfg >").append(" ").append("</site_code__mfg>");
					valueXmlString.append("<site_code__mfg>").append("").append("</site_code__mfg>");
					//Modified by Anjali R. on[09/10/2018][Remove space from tag value][End]
				}
				String lcqty1="",ldttrandate1="",lcrate1="",lcrateclg1="",lstrantype="";
				double lcrate=0;
				double inputQty=0;
				mPordNo = genericUtility.getColumnValue( "purc_order", dom );
				lsunit = genericUtility.getColumnValue( "unit", dom );
				lcqty1 = genericUtility.getColumnValue( "quantity", dom );
				lcqty= (lcqty1==null || lcqty1.trim().length() == 0) ? 0 : Double.parseDouble(lcqty1);
				ldttrandate1 = genericUtility.getColumnValue( "tran_date", dom1 );
				lcrate1 = genericUtility.getColumnValue( "rate", dom );
				System.out.println("mPordNo "+mPordNo);
				System.out.println("lsunit "+lsunit);
				System.out.println("lcqty1 "+lcqty1);
				System.out.println("lcqty "+lcqty);
				System.out.println("ldttrandate1 "+ldttrandate1);
				System.out.println("lcrate1 "+lcrate1);
				lcrate=( lcrate1==null || lcrate1.trim().length() == 0) ? 0 : Double.parseDouble(lcrate1);
				System.out.println("lcrate "+lcrate);
				if( lcrate <= 0 )
				{
					sql="select price_list__clg  from porder where purc_order = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mPordNo);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{

						lspricelist = rs.getString(1);
					}
					rs.close();
					pstmt.close();
					rs = null;
					pstmt = null;
					if(lspricelist != null && lspricelist.trim().length() >0)
					{
						sql="SELECT list_type  from pricelist where price_list = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lspricelist);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							lslisttype = checkNull(rs.getString(1));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						lcrate=disscommon.pickRate(lspricelist, ldttrandate1, lsitemcode, "", lslisttype, lcqty, lsunit, conn);
						if(lcrate >0)
						{
							valueXmlString.append("<rate >").append("<![CDATA[" + lcrate + "]]>").append("</rate>");
						}
					}
				}
				String ldtmfgdate1="",ldtexpirydate1="",ldtretestdate1="",lspricelistclg="";
				sql="select  mfg_date,exp_date ,retest_date from stock where item_code = ? and 	site_code = ? and 	loc_code  = ? and 	lot_no 	 = ? and lot_sl= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, mitem);
				pstmt.setString(2, lssite);
				pstmt.setString(3, mlocation);
				pstmt.setString(4, mlotno);
				pstmt.setString(5, mlotsl);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					mfgDate = rs.getTimestamp(1);
					expDate =rs.getTimestamp(2);
					retestDate = rs.getTimestamp(3);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				System.out.println("mfgDate"+mfgDate);
				System.out.println("expDate"+expDate);
				System.out.println("retestDate"+retestDate);

				if( mfgDate != null )
				{
					ldtmfgdate1 = sdf.format(mfgDate);
					System.out.println("ldtmfgdate1"+ldtmfgdate1);
				}
				if(ldtmfgdate1.trim().length() >0)
				{

					valueXmlString.append("<mfg_date protect =\"1\">").append("<![CDATA[" + ldtmfgdate1 + "]]>").append("</mfg_date>");
				}
				if(expDate != null)
				{
					ldtexpirydate1=sdf.format(expDate);
					System.out.println("ldtexpirydate1"+ldtexpirydate1);
				}
				if(ldtexpirydate1.trim().length() >0)
				{

					valueXmlString.append("<expiry_date protect =\"1\">").append("<![CDATA[" + ldtexpirydate1 + "]]>").append("</expiry_date>");
				}
				if(retestDate != null)
				{
					ldtretestdate1=sdf.format(retestDate);
					System.out.println("ldtexpirydate1"+ldtretestdate1);
				}
				if(ldtretestdate1.trim().length() >0)
				{
					valueXmlString.append("<retest_date protect =\"1\">").append("<![CDATA[" + ldtretestdate1 + "]]>").append("</retest_date>");
				}
				lstrantype = checkNull(genericUtility.getColumnValue( "tran_type", dom1 ));
				lcrateclg1 = checkNull(genericUtility.getColumnValue( "rate__clg", dom ));
				lcrateclg= (lcrateclg1==null || lcrateclg1.trim().length() == 0) ? 0 : Double.parseDouble(lcrateclg1);
				if(lcrateclg < 0)
				{
					sql="select udf_str2  from " +
							"gencodes where upper(fld_name) = 'TRAN_TYPE' " +
							"and upper(mod_name) = 'W_PORCP' and fld_value =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lstrantype);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						lspricelistclg = checkNull(rs.getString(1));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if(lspricelistclg == null || lspricelistclg.trim().length() ==0)
					{
						sql="select price_list__clg  from porder where purc_order = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mPordNo);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{

							lspricelistclg = rs.getString(1);
						}
						rs.close();
						pstmt.close();
						rs = null;
						pstmt = null;

					}
					if(lspricelistclg !=null && lspricelistclg.trim().length() >0)
					{
						sql="SELECT list_type from pricelist where price_list = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mPordNo);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{

							lspricelistclg = rs.getString(1);
						}
						rs.close();
						pstmt.close();
						rs = null;
						pstmt = null;
						lcrateclg =disscommon.pickRate(lspricelistclg, ldttrandate1, lsitemcode, "", lslisttype, lcqty, lsunit, conn);
					}
					if(lcrateclg <=0)
					{
						lcrateclg = lcrate;
					}
					if(lcrateclg > 0)
					{
						valueXmlString.append("<rate__clg >").append("<![CDATA[" + lcrateclg + "]]>").append("</rate__clg>");
					}
				}
				String lcrtconv1="",unitrate="",unitstd="",ratestduom1="";
				double lcrtconv=0,ratestduom=0;

				lcrtconv1 = checkNull(genericUtility.getColumnValue( "conv__rtuom_stduom", dom ));
				lcrtconv= (lcrtconv1 == null || lcrtconv1.trim().length() == 0) ?0 : Double.parseDouble(lcrtconv1);
				ratestduom1 = genericUtility.getColumnValue( "rate__stduom", dom );
				ratestduom= (ratestduom1 == null || ratestduom1.trim().length() == 0) ?0 : Double.parseDouble(ratestduom1);
				unitrate = checkNull(genericUtility.getColumnValue( "unit__rate", dom ));
				unitstd = checkNull(genericUtility.getColumnValue( "unit__std", dom ));
				lcrate1 = checkNull(genericUtility.getColumnValue( "rate", dom ));
				lcrate= (lcrate1 == null || lcrate1.trim().length() == 0) ?0 : Double.parseDouble(lcrate1);
				if("R".equalsIgnoreCase(lsuomrnd) || "B".equalsIgnoreCase(lsuomrnd))
				{
					lcstdqty1 = null;
					lcstdqty1 = new ArrayList();
					lcstdqty1 =disscommon.getConvQuantityFact(unitrate, unitstd, lsitemcode, lcrate, lcrtconv,conn); 
					if(lcstdqty1 != null)
					{
						inputQty = Double.parseDouble(lcstdqty1.get(1).toString());
					}
					valueXmlString.append("<rate__stduom >").append("<![CDATA[" + inputQty + "]]>").append("</rate__stduom>");
				}
				else
				{
					lcstdqty1 = null;
					lcstdqty1 = new ArrayList();
					lcstdqty1 =disscommon.getConvQuantityFact(unitrate, unitstd, lsitemcode, lcrate, lcrtconv, conn); 
					if(lcstdqty1 != null)
					{
						inputQty = Double.parseDouble(lcstdqty1.get(1).toString());
					}
				}
				System.out.println("rate__stduom ratestduom=["+ratestduom+"]");
				System.out.println("rate__stduom inputQty=["+inputQty+"]");
				valueXmlString.append("<conv__rtuom_stduom >").append("<![CDATA[" + lcrtconv1 + "]]>").append("</conv__rtuom_stduom>");
				valueXmlString.append("<rate__stduom >").append("<![CDATA[" + ratestduom + "]]>").append("</rate__stduom>");


			}
			else if(currentColumn.trim().equalsIgnoreCase("lot_no"))
			{
				String mlocation="";
				String dimension="";  //added by manish mhatre
				Timestamp mfgDate = null, expDate = null, retestDate = null;
				mlocation = checkNull(genericUtility.getColumnValue( "loc_code", dom ));
				mlotno = checkNull(genericUtility.getColumnValue( "lot_no", dom ));
				mlotsl = checkNull(genericUtility.getColumnValue( "lot_sl", dom ));
				mitem = checkNull(genericUtility.getColumnValue( "item_code", dom ));
				lssite = genericUtility.getColumnValue( "site_code", dom1 );
				dimension = checkNull(genericUtility.getColumnValue( "dimension", dom ));  //added by manish mhatre on 30-3-2021
				System.out.println("mlocation "+mlocation);
				System.out.println("mlotno "+mlotno);
				System.out.println("mlotsl "+mlotsl);
				System.out.println("mitem "+mitem);
				System.out.println("lssite "+lssite);
				lssitemfg = gfgetmfgsite(mitem,lssite,mlocation,mlotno,mlotsl,"M",conn);
				System.out.println("lssitemfg "+lssitemfg);
				lspack = gfgetmfgsite(mitem,lssite,mlocation,mlotno,mlotsl,"P",conn);
				System.out.println("lspack "+lspack);
				valueXmlString.append("<site_code__mfg >").append("<![CDATA[" + lssitemfg + "]]>").append("</site_code__mfg>");
				if(!"NOTFOUND".equalsIgnoreCase(lspack)	)
				{
					valueXmlString.append("<pack_code >").append("<![CDATA[" + lspack + "]]>").append("</pack_code>");
				}
				//Modified by Anjali R. on [Change condition][Start]
				//if("NOTFOUND".equalsIgnoreCase(lssitemfg));
				if("NOTFOUND".equalsIgnoreCase(lssitemfg))
				{
					//Modified by Anjali R. on [Change condition][End]
					//Modified by Anjali R. on[09/10/2018][Remove space from tag value][Start]
					//valueXmlString.append("<site_code__mfg >").append(" ").append("</site_code__mfg>");
					valueXmlString.append("<site_code__mfg>").append("").append("</site_code__mfg>");
					//Modified by Anjali R. on[09/10/2018][Remove space from tag value][End]
				}
				String lcqty1="",ldttrandate1="",lcrate1="",lcrateclg1="",lstrantype="";
				double lcrate=0;
				double inputQty=0;
				mPordNo = genericUtility.getColumnValue( "purc_order", dom );
				lsunit = genericUtility.getColumnValue( "unit", dom );
				lcqty1 = genericUtility.getColumnValue( "quantity", dom );
				lcqty= (lcqty1==null || lcqty1.trim().length() == 0)? 0 : Double.parseDouble(lcqty1);
				ldttrandate1 = genericUtility.getColumnValue( "tran_date", dom1 );
				lcrate1 = genericUtility.getColumnValue( "rate", dom );
				System.out.println("mPordNo "+mPordNo);
				System.out.println("lsunit "+lsunit);
				System.out.println("lcqty1 "+lcqty1);
				System.out.println("lcqty "+lcqty);
				System.out.println("ldttrandate1 "+ldttrandate1);
				System.out.println("lcrate1 "+lcrate1);
				lcrate= (lcrate1==null || lcrate1.trim().length() == 0) ? 0 : Double.parseDouble(lcrate1);
				System.out.println("lcrate "+lcrate);
				if( lcrate <= 0 )
				{
					sql="select price_list__clg  from porder where purc_order = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mPordNo);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{

						lspricelist = rs.getString(1);
					}
					rs.close();
					pstmt.close();
					rs = null;
					pstmt = null;
					if(lspricelist != null && lspricelist.trim().length() >0)
					{
						sql="SELECT list_type  from pricelist where price_list = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lspricelist);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							lslisttype = checkNull(rs.getString(1));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						lcrate=disscommon.pickRate(lspricelist, ldttrandate1, lsitemcode, "", lslisttype, lcqty, lsunit, conn);
						if(lcrate >0)
						{
							valueXmlString.append("<rate >").append("<![CDATA[" + lcrate + "]]>").append("</rate>");
						}
					}
				}
				String ldtmfgdate1="",ldtexpirydate1="",ldtretestdate1="",lspricelistclg="";

				//added by manish mhatre on 21-aug-2019 [For Expiry and mfg date  pick up in item_lot_info table]
				//start manish
				sql="select mfg_date, exp_date, retest_date from item_lot_info where item_code = ? and lot_no = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, mitem);
				pstmt.setString(2, mlotno);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					mfgDate = rs.getTimestamp(1);
					expDate =rs.getTimestamp(2);
					retestDate = rs.getTimestamp(3);

					if( mfgDate != null ){
						ldtmfgdate1 = sdf.format(mfgDate);
						System.out.println("ldtmfgdate1"+ldtmfgdate1);
						valueXmlString.append("<mfg_date protect =\"1\">").append("<![CDATA[" + ldtmfgdate1 + "]]>").append("</mfg_date>");
					}
					if(expDate != null)
					{
						ldtexpirydate1=sdf.format(expDate);
						System.out.println("ldtexpirydate1"+ldtexpirydate1);
						valueXmlString.append("<expiry_date protect =\"1\">").append("<![CDATA[" + ldtexpirydate1 + "]]>").append("</expiry_date>");
					}
					if(retestDate != null)
					{
						ldtretestdate1=sdf.format(retestDate);
						System.out.println("ldtexpirydate1"+ldtretestdate1);
						valueXmlString.append("<retest_date protect =\"1\">").append("<![CDATA[" + ldtretestdate1 + "]]>").append("</retest_date>");
					}

				} //end manish
				else 
				{
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					sql="select  mfg_date,exp_date ,retest_date from stock where item_code = ? and 	site_code = ? and 	loc_code  = ? and 	lot_no 	 = ? and lot_sl= ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mitem);
					pstmt.setString(2, lssite);
					pstmt.setString(3, mlocation);
					pstmt.setString(4, mlotno);
					pstmt.setString(5, mlotsl);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						mfgDate = rs.getTimestamp(1);
						expDate =rs.getTimestamp(2);
						retestDate = rs.getTimestamp(3);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println("mfgDate"+mfgDate);
					System.out.println("expDate"+expDate);
					System.out.println("retestDate"+retestDate);

					if( mfgDate != null )
					{
						ldtmfgdate1 = sdf.format(mfgDate);
						System.out.println("ldtmfgdate1"+ldtmfgdate1);
						valueXmlString.append("<mfg_date protect =\"1\">").append("<![CDATA[" + ldtmfgdate1 + "]]>").append("</mfg_date>");
					}
					//if(ldtmfgdate1.trim().length() >0)
					//{

					//valueXmlString.append("<mfg_date protect =\"1\">").append("<![CDATA[" + ldtmfgdate1 + "]]>").append("</mfg_date>");
					//}     //commented by manish mhatre on 21-aug-2019
					if(expDate != null)
					{
						ldtexpirydate1=sdf.format(expDate);
						System.out.println("ldtexpirydate1"+ldtexpirydate1);
						valueXmlString.append("<expiry_date protect =\"1\">").append("<![CDATA[" + ldtexpirydate1 + "]]>").append("</expiry_date>");
					}
					//if(ldtexpirydate1.trim().length() >0)
					//{

					//valueXmlString.append("<expiry_date protect =\"1\">").append("<![CDATA[" + ldtexpirydate1 + "]]>").append("</expiry_date>");
					//}     //commented by manish mhatre on 21-aug-2019
					if(retestDate != null)
					{
						ldtretestdate1=sdf.format(retestDate);
						System.out.println("ldtexpirydate1"+ldtretestdate1);
						valueXmlString.append("<retest_date protect =\"1\">").append("<![CDATA[" + ldtretestdate1 + "]]>").append("</retest_date>");
					}
					//if(ldtretestdate1.trim().length() >0)
					//{
					//valueXmlString.append("<retest_date protect =\"1\">").append("<![CDATA[" + ldtretestdate1 + "]]>").append("</retest_date>");
					//}     //commented by manish mhatre on 21-aug-2019

				}
				if(rs != null) {
					rs.close();
					rs = null;}
				if(pstmt != null) {
					pstmt.close();
					pstmt = null;
				}


				lstrantype = genericUtility.getColumnValue( "tran_type", dom1 );
				lcrateclg1 = genericUtility.getColumnValue( "rate__clg", dom );
				lcrateclg= (lcrateclg1==null || lcrateclg1.trim().length() == 0) ? 0 : Double.parseDouble(lcrateclg1);
				if(lcrateclg < 0)
				{
					sql="select udf_str2  from " +
							"gencodes where upper(fld_name) = 'TRAN_TYPE' " +
							"and upper(mod_name) = 'W_PORCP' and fld_value =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lstrantype);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						lspricelistclg = checkNull(rs.getString(1));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if(lspricelistclg == null || lspricelistclg.trim().length() ==0)
					{
						sql="select price_list__clg  from porder where purc_order = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mPordNo);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{

							lspricelistclg = rs.getString(1);
						}
						rs.close();
						pstmt.close();
						rs = null;
						pstmt = null;

					}
					if(lspricelistclg !=null && lspricelistclg.trim().length() >0)
					{
						sql="SELECT list_type from pricelist where price_list = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mPordNo);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{

							lspricelistclg = rs.getString(1);
						}
						rs.close();
						pstmt.close();
						rs = null;
						pstmt = null;
						lcrateclg =disscommon.pickRate(lspricelistclg, ldttrandate1, lsitemcode, "", lslisttype, lcqty, lsunit, conn);
					}
					if(lcrateclg <=0)
					{
						lcrateclg = lcrate;
					}
					if(lcrateclg > 0)
					{
						valueXmlString.append("<rate__clg >").append("<![CDATA[" + lcrateclg + "]]>").append("</rate__clg>");
					}
				}
				String lcrtconv1="",unitrate="",unitstd="",ratestduom1="";
				double lcrtconv=0,ratestduom=0;

				lcrtconv1 = genericUtility.getColumnValue( "conv__rtuom_stduom", dom );
				lcrtconv=( lcrtconv1 == null || lcrtconv1.trim().length() == 0 ) ?0 : Double.parseDouble(lcrtconv1);
				ratestduom1 = genericUtility.getColumnValue( "rate__stduom", dom );
				ratestduom= (ratestduom1 == null || ratestduom1.trim().length() == 0 )?0 : Double.parseDouble(ratestduom1);
				unitrate = checkNull(genericUtility.getColumnValue( "unit__rate", dom ));
				unitstd = checkNull(genericUtility.getColumnValue( "unit__std", dom ));
				lcrate1 = genericUtility.getColumnValue( "rate", dom );
				lcrate= (lcrate1 == null  || lcrate1.trim().length() == 0 )?0 : Double.parseDouble(lcrate1);
				if("R".equalsIgnoreCase(lsuomrnd) || "B".equalsIgnoreCase(lsuomrnd))
				{
					lcstdqty1 = null;
					lcstdqty1 = new ArrayList();
					lcstdqty1 =disscommon.getConvQuantityFact(unitrate, unitstd, lsitemcode, lcrate, lcrtconv,conn); 
					if(lcstdqty1  != null)
					{
						inputQty = Double.parseDouble(lcstdqty1.get(1).toString());
					}
					valueXmlString.append("<rate__stduom >").append("<![CDATA[" + inputQty + "]]>").append("</rate__stduom>");
				}
				else
				{
					lcstdqty1 = null;
					lcstdqty1 = new ArrayList();
					lcstdqty1 =disscommon.getConvQuantityFact(unitrate, unitstd, lsitemcode, lcrate, lcrtconv, conn); 
					if(lcstdqty1  != null)
					{
						inputQty = Double.parseDouble(lcstdqty1.get(1).toString());
					}
				}
				System.out.println("rate__stduom ratestduom=["+ratestduom+"]");
				System.out.println("rate__stduom inputQty=["+inputQty+"]");
				valueXmlString.append("<conv__rtuom_stduom >").append("<![CDATA[" + lcrtconv1 + "]]>").append("</conv__rtuom_stduom>");
				valueXmlString.append("<rate__stduom >").append("<![CDATA[" + ratestduom + "]]>").append("</rate__stduom>");
				//Pavan R 10jul19 start [no of art to consider from item_lot_packsize, item and if not found then packing master]												
				double noOfArt = 0d, shipperSize = 0d;
				double mCapacity=0d;				
				System.out.println("Pavan Rane DOM[  "+genericUtility.serializeDom(dom)+"]\n\n\n");
				System.out.println("Quantity["+lcqty+"]ItemCode["+mitem+"]mlotno["+mlotno+"]lspack["+lspack+"]");				

				sql = "select (case when shipper_size is null then 0 else shipper_size end) as shipper_size, "
						+ "(case when gross_weight is null then 0 else gross_weight end) as lc_gross_weigth, "
						+ "(case when net_weight is null then 0 else net_weight end) as lc_net_weight "
						+ "from item_lot_packsize where item_code = ? "
						+ "and ? >= lot_no__from and ? <= lot_no__to";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, mitem);
				pstmt.setString(2, mlotno);
				pstmt.setString(3, mlotno);
				rs = pstmt.executeQuery();							
				if(rs.next())
				{
					shipperSize = rs.getDouble("shipper_size");							
					mGrossWeight= rs.getDouble("LC_GROSS_WEIGTH");
					mNetWeight= rs.getDouble("LC_NET_WEIGHT");
					System.out.println("Pavan R shipperSize["+shipperSize+"] grossWeight [ "+mGrossWeight+" ] netWeight[ "+mNetWeight+" ]");	
					if (shipperSize > 0) 
					{		
						System.out.println("Inside if shipper size > 0");
						double mod = lcqty/shipperSize;
						noOfArt = getRndamt(mod , "X", 1);
						System.out.println("noOfArt["+noOfArt+"]");
						if(dimension==null || dimension.trim().length()==0)   //added by manish mhatre on 31-3-2021
						{
						valueXmlString.append("<no_art><![CDATA[").append(noOfArt).append("]]></no_art>\r\n");   
						setNodeValue(dom,"no_art" , getAbsString(String.valueOf(noOfArt)));//added by manish mhatre on 30-3-21
						System.out.println("no art 10141>>>>"+noOfArt);
						}

						//added by manish mhatre on 30-3-21
						//start manish
						//String noArtStr=String.valueOf(noOfArt);
						//String unit="";
						//double quantity=0;

						//System.out.println("dimension>>"+dimension+"\n no of articles in string>>"+noArtStr);

						/*if(dimension!=null && dimension.trim().length()>0 && noArtStr!=null && noArtStr.trim().length()>0)
					{
						sql1 = "SELECT UNIT FROM ITEM WHERE ITEM_CODE = ? ";
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1, mitem);
						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{
							unit = rs1.getString("UNIT");
						}
						if(pstmt1 != null)
						{
							pstmt1.close();
							pstmt1 = null;
						}
						if(rs1 != null)
						{
							rs1.close();
							rs1 = null;
						}
						System.out.println("unit 10163>>"+unit);

						System.out.println("dimension>>"+dimension+"\n no of articles>>"+noOfArt);

						if("CFT".equalsIgnoreCase(unit) || "SQM".equalsIgnoreCase(unit))
						{

							quantity=disscommon.getQuantity(dimension,noOfArt,unit,conn);

							valueXmlString.append("<quantity>").append("<![CDATA["+quantity+"]]>").append("</quantity>");
							setNodeValue(dom,"quantity" , getAbsString(String.valueOf(quantity)));

							reStr = itemChanged(dom, dom1, dom2, objContext, "quantity", editFlag, xtraParams);
							System.out.println("before quantity itemchanged 10180.......");
							pos = reStr.indexOf("<Detail2>");
							reStr = reStr.substring(pos + 9);
							pos = reStr.indexOf("</Detail2>");
							reStr = reStr.substring(0,pos);
							valueXmlString.append(reStr);
							System.out.println("after quantity itemchanged 10189.......");
						}
                    }*/
						//end manish
						if(noOfArt > 0)
						{
							mGrosswt = ((mGrossWeight/shipperSize) * lcqty); 						
							mNetWt = ((mNetWeight/shipperSize) * lcqty);
							System.out.println("mGrosswt [ "+mGrosswt+" ]");
							if(mGrossWeight > 0)
							{
								valueXmlString.append("<gross_weight ><![CDATA[").append(getRequiredDecimal(mGrosswt,3)).append("]]></gross_weight>\r\n");
								valueXmlString.append("<net_weight ><![CDATA[").append(getRequiredDecimal(mNetWt,3)).append("]]></net_weight>\r\n");										
								mTareWt=mGrosswt - mNetWt;
								System.out.println("mGrossWeight > 0 mTareWt [ "+mTareWt+" ]");
								valueXmlString.append("<tare_weight ><![CDATA[").append(getRequiredDecimal(mTareWt, 3)).append("]]></tare_weight>\r\n");
							}									
						} //if(noOfArt > 0)																
					}//if(shipperSize > 0)  						
				}						
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;															
				if(shipperSize == 0)
				{					
					System.out.println("Inside if shipper size = 0");		
					System.out.println("Pavan Rane After ShipperSize ==0 PackingCode ["+lspack+"]");					
					if(lspack != null && lspack.trim().length()>0)
					{
						double mlcMode = 0d;
						sql="select capacity from packing where pack_code = ? ";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1,lspack);
						rs = pstmt.executeQuery();								
						if( rs.next())
						{								 
							mCapacity= rs.getDouble("capacity");
							System.out.println("Capacity::["+mCapacity+"]");
						}
						rs.close();
						rs = null;
						pstmt.close(); 
						pstmt=null;
					}					
					if((lspack!=null && lspack.trim().length()>0) && (mCapacity > 0))
					{
						System.out.println("Inside if pack code > 0 and capacity>0");
						double mod = lcqty/mCapacity;
						mNoart = getRndamt(mod, "X", 1);
						System.out.println("Pavan R mNoart["+mNoart+"]");

						if(dimension==null || dimension.trim().length()==0) //added by manish mhatre on 31-3-2021
						{
						valueXmlString.append("<no_art ><![CDATA[").append(mNoart).append("]]></no_art>\r\n");	
						setNodeValue(dom,"no_art" , getAbsString(String.valueOf(mNoart)));   //added by manish mhatre on 30-3-21
						System.out.println("no art 10243>>>>"+mNoart);
						}
						//added by manish mhatre on 30-3-21
						//start manish
						//String noArtStr=String.valueOf(mNoart);
						//String unit="";
						//double quantity=0;

						//System.out.println("dimension>>"+dimension+"\n no of articles in string>>"+noArtStr);

						/*if(dimension!=null && dimension.trim().length()>0 && noArtStr!=null && noArtStr.trim().length()>0)
					{
						sql = "SELECT UNIT FROM ITEM WHERE ITEM_CODE = ? ";
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1, mitem);
						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{
							unit = rs1.getString("UNIT");
						}
						if(pstmt1 != null)
						{
							pstmt1.close();
							pstmt1 = null;
						}
						if(rs1 != null)
						{
							rs1.close();
							rs1 = null;
						}
						System.out.println("unit 10163>>"+unit);

						System.out.println("dimension>>"+dimension+"\n no of articles>>"+mNoart);

						if("CFT".equalsIgnoreCase(unit) || "SQM".equalsIgnoreCase(unit))
						{

							quantity=disscommon.getQuantity(dimension,mNoart,unit,conn);

							valueXmlString.append("<quantity>").append("<![CDATA["+quantity+"]]>").append("</quantity>");
							setNodeValue(dom,"quantity" , getAbsString(String.valueOf(quantity)));

							reStr = itemChanged(dom, dom1, dom2, objContext, "quantity", editFlag, xtraParams);
							System.out.println("before quantity itemchanged 10285.......");
							pos = reStr.indexOf("<Detail2>");
							reStr = reStr.substring(pos + 9);
							pos = reStr.indexOf("</Detail2>");
							reStr = reStr.substring(0,pos);
							valueXmlString.append(reStr);
							System.out.println("after quantity itemchanged 10291.......");
						}
                    }*/
						//end manish					
						if(( mNoart > 0))
						{
							sql="SELECT (CASE WHEN GROSS_WEIGHT IS NULL THEN 0 ELSE GROSS_WEIGHT END) AS LC_GROSS_WEIGTH, (CASE WHEN NET_WEIGHT IS NULL THEN 0 ELSE NET_WEIGHT END) AS LC_NET_WEIGHT FROM 	ITEM_LOT_PACKSIZE 	WHERE ITEM_CODE = ?	AND  ? BETWEEN LOT_NO__FROM AND LOT_NO__TO ";
							pstmt=conn.prepareStatement(sql);
							pstmt.setString(1,mitem);
							pstmt.setString(2,mlotno);
							rs = pstmt.executeQuery();						
							if( rs.next())
							{									 
								grossWeight= checkNull(rs.getString("LC_GROSS_WEIGTH"));
								netWeight= checkNull(rs.getString("LC_NET_WEIGHT"));
								System.out.println("Pavan Rane grossWeight ["+grossWeight+"]netWeight["+netWeight+"]");
							}
							rs.close();
							rs = null;
							pstmt.close(); 
							pstmt=null;
							if(grossWeight.trim().length() > 0)
							{
								mGrossWeight=Double.parseDouble(grossWeight);
							}
							if(netWeight.trim().length() > 0)
							{
								mNetWeight=Double.parseDouble(netWeight);
							}

							mGrosswt = ((mGrossWeight/mCapacity) * lcqty); 								
							mNetWt = ((mNetWeight/mCapacity) * lcqty);

							//mGrosswt= mNoart *  mGrossWeight;
							System.out.println("Pavan Rane total mGrosswt["+mGrosswt+"]  mGrossWeight["+mGrossWeight+"]");
							if(mGrossWeight > 0)
							{
								valueXmlString.append("<gross_weight ><![CDATA[").append(getRequiredDecimal(mGrosswt, 3)).append("]]></gross_weight>\r\n");							
								valueXmlString.append("<net_weight ><![CDATA[").append(getRequiredDecimal(mNetWt, 3)).append("]]></net_weight>\r\n");
								mTareWt=mGrosswt - mNetWt;
								valueXmlString.append("<tare_weight ><![CDATA[").append(getRequiredDecimal(mTareWt, 3)).append("]]></tare_weight>\r\n");
							}							
						}
					}
					else 
					{
						sql="SELECT (CASE WHEN NET_WEIGHT IS NULL THEN 0 ELSE NET_WEIGHT END) AS NET_WT_ITEM, INTEGRAL_QTY FROM ITEM WHERE ITEM_CODE = ? ";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1,mitem);
						rs = pstmt.executeQuery();						
						if( rs.next())
						{									 
							netWtItem= checkNull(rs.getString("NET_WT_ITEM"));
							integrlQty= checkNull(rs.getString("INTEGRAL_QTY"));
							System.out.println("Pavan Rane else netWtItem["+netWtItem+"]  integrlQty["+integrlQty+"]");	 
						}
						rs.close();
						rs = null;
						pstmt.close(); 
						pstmt=null;

						sql="SELECT (CASE WHEN NET_WEIGHT IS NULL THEN 0 ELSE NET_WEIGHT END) AS NET_WT FROM ITEM_LOT_PACKSIZE	WHERE ITEM_CODE = ?	AND	? BETWEEN LOT_NO__FROM AND LOT_NO__TO ";
						pstmt=conn.prepareStatement(sql);
						pstmt.setString(1,mitem);
						pstmt.setString(2,mlotno);
						rs = pstmt.executeQuery();						
						if( rs.next())
						{	

							netWt= checkNull(rs.getString("NET_WT"));
							System.out.println("Pavan Rane netWt["+netWt+"]");
						}
						rs.close();
						rs = null;
						pstmt.close(); 
						pstmt=null;						

						if(netWtItem.trim().length() > 0)
						{
							mNetWtItem=Double.parseDouble(netWtItem);
						}
						if(netWt.trim().length() > 0)
						{
							mNetWt=Double.parseDouble(netWt);
						}
						if(integrlQty.trim().length()>0)
						{
							mIntegrlQty=Double.parseDouble(integrlQty);
						}

						if(mNetWt==0)
						{
							mNetWt=mNetWtItem;
						}

						if(mNetWt==0)
						{

						}
						else
						{
							mNetWt=mNetWt * lcqty;
							if(mIntegrlQty>0)
							{
								sql="SELECT FN_MOD( ? ,? ) AS LC_MODE FROM DUAL ";
								pstmt=conn.prepareStatement(sql);
								pstmt.setString(1,lcqty1);
								pstmt.setString(2,integrlQty);
								rs = pstmt.executeQuery();								
								if( rs.next())
								{	
									lcMode= checkNull(rs.getString("LC_MODE"));	
									System.out.println("intered in rs item descr  hhho.."+lcMode);
								}
								rs.close();
								rs = null;
								pstmt.close(); 
								pstmt=null;
								if(lcMode.length()>0)
								{
									mNoart=(lcqty / mIntegrlQty) + 1; //in pb int is used before open brackets
								}
								else 
								{
									mNoart=(lcqty / mIntegrlQty) ; //in pb int is used before open brackets
								}
							}
							else
							{
								mNoart=lcqty;
							}
						}
						valueXmlString.append("<gross_weight ><![CDATA[").append(getRequiredDecimal(mNetWt, 3)).append("]]></gross_weight>\r\n");
						valueXmlString.append("<net_weight ><![CDATA[").append(getRequiredDecimal(mNetWt, 3)).append("]]></net_weight>\r\n");
						valueXmlString.append("<tare_weight ><![CDATA[").append("0").append("]]></tare_weight>\r\n");
						if(dimension==null || dimension.trim().length()==0)   //added by manish mhatre on 31-3-2021
						{
						valueXmlString.append("<no_art ><![CDATA[").append(mNoart).append("]]></no_art>\r\n");	
						setNodeValue(dom, "no_art", getAbsString(String.valueOf(mNoart)));   //added by manish mhatre on 30-3-21
						System.out.println("no art 10428>>>>"+mNoart);
						}

						//added by manish mhatre on 30-3-21
						//start manish
						//String noArtStr=String.valueOf(mNoart);
						//String unit="";
						//double quantity=0;

						//System.out.println("dimension>>"+dimension+"\n no of articles in string>>"+noArtStr);

						/*if(dimension!=null && dimension.trim().length()>0 && noArtStr!=null && noArtStr.trim().length()>0)
					{
						sql = "SELECT UNIT FROM ITEM WHERE ITEM_CODE = ? ";
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString(1, mitem);
						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{
							unit = rs1.getString("UNIT");
						}
						if(pstmt1 != null)
						{
							pstmt1.close();
							pstmt1 = null;
						}
						if(rs1 != null)
						{
							rs1.close();
							rs1 = null;
						}
						System.out.println("unit 10163>>"+unit);

						System.out.println("dimension>>"+dimension+"\n no of articles>>"+mNoart);

						if("CFT".equalsIgnoreCase(unit) || "SQM".equalsIgnoreCase(unit))
						{

							quantity=disscommon.getQuantity(dimension,mNoart,unit,conn);

							valueXmlString.append("<quantity>").append("<![CDATA["+quantity+"]]>").append("</quantity>");
							setNodeValue(dom,"quantity" , getAbsString(String.valueOf(quantity)));

							reStr = itemChanged(dom, dom1, dom2, objContext, "quantity", editFlag, xtraParams);
							System.out.println("before quantity itemchanged 10470.......");
							pos = reStr.indexOf("<Detail2>");
							reStr = reStr.substring(pos + 9);
							pos = reStr.indexOf("</Detail2>");
							reStr = reStr.substring(0,pos);
							valueXmlString.append(reStr);
							System.out.println("after quantity itemchanged 10476.......");
						}
                    }*/
						//end manish					
					}
				}								
				//Pavan R 10jul19 end
			}
			else if(currentColumn.trim().equalsIgnoreCase("lot_sl"))
			{
				String mlocation="";
				Timestamp mfgDate = null, expDate = null, retestDate = null;
				mlocation = checkNull(genericUtility.getColumnValue( "loc_code", dom ));
				mlotno = checkNull(genericUtility.getColumnValue( "lot_no", dom ));
				mlotsl = checkNull(genericUtility.getColumnValue( "lot_sl", dom ));
				mitem = checkNull(genericUtility.getColumnValue( "item_code", dom ));
				lssite = genericUtility.getColumnValue( "site_code", dom1 );
				System.out.println("mlocation "+mlocation);
				System.out.println("mlotno "+mlotno);
				System.out.println("mlotsl "+mlotsl);
				System.out.println("mitem "+mitem);
				System.out.println("lssite "+lssite);
				lssitemfg = gfgetmfgsite(mitem,lssite,mlocation,mlotno,mlotsl,"M",conn);
				System.out.println("lssitemfg "+lssitemfg);
				lspack = gfgetmfgsite(mitem,lssite,mlocation,mlotno,mlotsl,"P",conn);
				System.out.println("lspack "+lspack);
				valueXmlString.append("<site_code__mfg >").append("<![CDATA[" + lssitemfg + "]]>").append("</site_code__mfg>");
				if(!"NOTFOUND".equalsIgnoreCase(lspack)	)
				{
					valueXmlString.append("<pack_code >").append("<![CDATA[" + lspack + "]]>").append("</pack_code>");
				}
				//Modified by Anjali R. on [Change condition][Start]
				//if("NOTFOUND".equalsIgnoreCase(lssitemfg));
				if("NOTFOUND".equalsIgnoreCase(lssitemfg))
				{
					//Modified by Anjali R. on [Change condition][End]
					//Modified by Anjali R. on[09/10/2018][Remove space from tag value][Start]
					//valueXmlString.append("<site_code__mfg >").append(" ").append("</site_code__mfg>");
					valueXmlString.append("<site_code__mfg>").append("").append("</site_code__mfg>");
					//Modified by Anjali R. on[09/10/2018][Remove space from tag value][End]
				}
				String lcqty1="",ldttrandate1="",lcrate1="",lcrateclg1="",lstrantype="";
				double lcrate=0;
				double inputQty=0;
				mPordNo = genericUtility.getColumnValue( "purc_order", dom );
				lsunit = genericUtility.getColumnValue( "unit", dom );
				lcqty1 = genericUtility.getColumnValue( "quantity", dom );
				lcqty= (lcqty1==null || lcqty1.trim().length() == 0)? 0 : Double.parseDouble(lcqty1);
				ldttrandate1 = genericUtility.getColumnValue( "tran_date", dom1 );
				lcrate1 = genericUtility.getColumnValue( "rate", dom );
				System.out.println("mPordNo "+mPordNo);
				System.out.println("lsunit "+lsunit);
				System.out.println("lcqty1 "+lcqty1);
				System.out.println("lcqty "+lcqty);
				System.out.println("ldttrandate1 "+ldttrandate1);
				System.out.println("lcrate1 "+lcrate1);
				lcrate= (lcrate1==null || lcrate1.trim().length() == 0) ? 0 : Double.parseDouble(lcrate1);
				System.out.println("lcrate "+lcrate);
				if( lcrate <= 0 )
				{
					sql="select price_list__clg  from porder where purc_order = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, mPordNo);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{

						lspricelist = rs.getString(1);
					}
					rs.close();
					pstmt.close();
					rs = null;
					pstmt = null;
					if(lspricelist != null && lspricelist.trim().length() >0)
					{
						sql="SELECT list_type  from pricelist where price_list = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lspricelist);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							lslisttype = checkNull(rs.getString(1));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						lcrate=disscommon.pickRate(lspricelist, ldttrandate1, lsitemcode, "", lslisttype, lcqty, lsunit, conn);
						if(lcrate >0)
						{
							valueXmlString.append("<rate >").append("<![CDATA[" + lcrate + "]]>").append("</rate>");
						}
					}
				}
				String ldtmfgdate1="",ldtexpirydate1="",ldtretestdate1="",lspricelistclg="";
				sql="select  mfg_date,exp_date ,retest_date from stock where item_code = ? and 	site_code = ? and 	loc_code  = ? and 	lot_no 	 = ? and lot_sl= ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, mitem);
				pstmt.setString(2, lssite);
				pstmt.setString(3, mlocation);
				pstmt.setString(4, mlotno);
				pstmt.setString(5, mlotsl);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					mfgDate = rs.getTimestamp(1);
					expDate =rs.getTimestamp(2);
					retestDate = rs.getTimestamp(3);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				System.out.println("mfgDate"+mfgDate);
				System.out.println("expDate"+expDate);
				System.out.println("retestDate"+retestDate);

				if( mfgDate != null )
				{
					ldtmfgdate1 = sdf.format(mfgDate);
					System.out.println("ldtmfgdate1"+ldtmfgdate1);
				}
				if(ldtmfgdate1.trim().length() >0)
				{

					valueXmlString.append("<mfg_date protect =\"1\">").append("<![CDATA[" + ldtmfgdate1 + "]]>").append("</mfg_date>");
				}
				if(expDate != null)
				{
					ldtexpirydate1=sdf.format(expDate);
					System.out.println("ldtexpirydate1"+ldtexpirydate1);
				}
				if(ldtexpirydate1.trim().length() >0)
				{

					valueXmlString.append("<expiry_date protect =\"1\">").append("<![CDATA[" + ldtexpirydate1 + "]]>").append("</expiry_date>");
				}
				if(retestDate != null)
				{
					ldtretestdate1=sdf.format(retestDate);
					System.out.println("ldtexpirydate1"+ldtretestdate1);
				}
				if(ldtretestdate1.trim().length() >0)
				{
					valueXmlString.append("<retest_date protect =\"1\">").append("<![CDATA[" + ldtretestdate1 + "]]>").append("</retest_date>");
				}
				lstrantype = genericUtility.getColumnValue( "tran_type", dom1 );
				lcrateclg1 = genericUtility.getColumnValue( "rate__clg", dom );
				lcrateclg= (lcrateclg1==null ||  lcrateclg1.trim().length() == 0) ? 0 : Double.parseDouble(lcrateclg1);
				if(lcrateclg < 0)
				{
					sql="select udf_str2  from " +
							"gencodes where upper(fld_name) = 'TRAN_TYPE' " +
							"and upper(mod_name) = 'W_PORCP' and fld_value =?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, lstrantype);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						lspricelistclg = checkNull(rs.getString(1));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if(lspricelistclg == null || lspricelistclg.trim().length() ==0)
					{
						sql="select price_list__clg  from porder where purc_order = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mPordNo);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{

							lspricelistclg = rs.getString(1);
						}
						rs.close();
						pstmt.close();
						rs = null;
						pstmt = null;

					}
					if(lspricelistclg !=null && lspricelistclg.trim().length() >0)
					{
						sql="SELECT list_type from pricelist where price_list = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mPordNo);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{

							lspricelistclg = rs.getString(1);
						}
						rs.close();
						pstmt.close();
						rs = null;
						pstmt = null;
						lcrateclg =disscommon.pickRate(lspricelistclg, ldttrandate1, lsitemcode, "", lslisttype, lcqty, lsunit, conn);
					}
					if(lcrateclg <=0)
					{
						lcrateclg = lcrate;
					}
					if(lcrateclg > 0)
					{
						valueXmlString.append("<rate__clg >").append("<![CDATA[" + lcrateclg + "]]>").append("</rate__clg>");
					}
				}
				String lcrtconv1="",unitrate="",unitstd="",ratestduom1="";
				double lcrtconv=0,ratestduom=0;
				ratestduom1 =checkNull( genericUtility.getColumnValue( "rate__stduom", dom ));
				ratestduom= (ratestduom1 == null || ratestduom1.trim().length() == 0) ?0 : Double.parseDouble(ratestduom1);
				lcrtconv1 = genericUtility.getColumnValue( "conv__rtuom_stduom", dom );
				lcrtconv= (lcrtconv1 == null || lcrtconv1.trim().length() == 0) ?0 : Double.parseDouble(lcrtconv1);
				unitrate = checkNull(genericUtility.getColumnValue( "unit__rate", dom ));
				unitstd = checkNull(genericUtility.getColumnValue( "unit__std", dom ));
				lcrate1 = genericUtility.getColumnValue( "rate", dom );
				lcrate= (lcrate1 == null || lcrate1.trim().length() == 0)?0 : Double.parseDouble(lcrate1);
				if("R".equalsIgnoreCase(lsuomrnd) || "B".equalsIgnoreCase(lsuomrnd))
				{
					lcstdqty1 = null;
					lcstdqty1 = new ArrayList();
					lcstdqty1 =disscommon.getConvQuantityFact(unitrate, unitstd, lsitemcode, lcrate, lcrtconv,conn); 
					if(lcstdqty1 != null)
					{
						inputQty = Double.parseDouble(lcstdqty1.get(1).toString());
					}
					valueXmlString.append("<rate__stduom >").append("<![CDATA[" + inputQty + "]]>").append("</rate__stduom>");
				}
				else
				{
					lcstdqty1 = null;
					lcstdqty1 = new ArrayList();
					lcstdqty1 =disscommon.getConvQuantityFact(unitrate, unitstd, lsitemcode, lcrate, lcrtconv, conn); 
					if(lcstdqty1 != null)
					{
						inputQty = Double.parseDouble(lcstdqty1.get(1).toString());
					}
				}

				valueXmlString.append("<conv__rtuom_stduom >").append("<![CDATA[" + lcrtconv1 + "]]>").append("</conv__rtuom_stduom>");
				valueXmlString.append("<rate__stduom >").append("<![CDATA[" + ratestduom + "]]>").append("</rate__stduom>");


			}
			else if(currentColumn.trim().equalsIgnoreCase("acct_code__dr"))
			{

				lsacct = checkNull(genericUtility.getColumnValue( "acct_code__dr", dom ));
				sql="Select descr From Accounts Where  acct_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsacct);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					lsacct1 = rs.getString(1);
				}
				rs.close();
				pstmt.close();
				rs = null;
				pstmt = null;
				valueXmlString.append("<descr >").append("<![CDATA[" + lsacct1 + "]]>").append("</descr>");
			}
			else if(currentColumn.trim().equalsIgnoreCase("spec_ref"))
			{
				lsspec = checkNull(genericUtility.getColumnValue( "spec_ref", dom ));
				sql="select descr  from specification where spec_ref =?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, lsspec);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					lsspecdescr = rs.getString(1);
				}
				rs.close();
				pstmt.close();
				rs = null;
				pstmt = null;
				valueXmlString.append("<specification_descr >").append("<![CDATA[" + lsspecdescr + "]]>").append("</specification_descr>");
			}


			//added by manish mhatre on 17-03-2021
			//start manish
			else if(currentColumn.trim().equalsIgnoreCase( "no_art" ) || currentColumn.trim().equalsIgnoreCase( "dimension" ) )
			{
				String dimension="",noArtStr="",itemCode="",unit="";
				double quantity=0,noArt=0;

				System.out.println("Inside poreceipt no_art block");

				itemCode= genericUtility.getColumnValue("item_code", dom);
				dimension=genericUtility.getColumnValue("dimension", dom);
				noArtStr= genericUtility.getColumnValue("no_art", dom);
				System.out.println("item code>>"+itemCode+"\ndimension>>"+dimension+"\nno of articles>>"+noArtStr);

				if(dimension!=null && dimension.trim().length()>0)
				{
					sql = "SELECT UNIT FROM ITEM WHERE ITEM_CODE = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						unit = rs.getString("UNIT");
					}
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
					System.out.println("unit>>"+unit);

					if(noArtStr!=null && noArtStr.trim().length()>0)
					{
						noArt=Double.parseDouble(noArtStr);
					}
					else
					{
						noArt=1;
					}
					System.out.println("dimension>>"+dimension+"\n no of articles>>"+noArt);

					if("CFT".equalsIgnoreCase(unit) || "SQM".equalsIgnoreCase(unit))
					{

						System.out.println("Inside if dimension not null in no art");

						quantity=disscommon.getQuantity(dimension,noArt,unit,conn);

						System.out.println("quantity in dimension block>>"+quantity);

						valueXmlString.append("<quantity>").append("<![CDATA["+quantity+"]]>").append("</quantity>");
						setNodeValue(dom,"quantity" , getAbsString(String.valueOf(quantity)));
						reStr = itemChanged(dom, dom1, dom2, objContext, "quantity", editFlag, xtraParams);
						System.out.println("after quantity itemchanged 10614.......");
						pos = reStr.indexOf("<Detail2>");
						reStr = reStr.substring(pos + 9);
						pos = reStr.indexOf("</Detail2>");
						reStr = reStr.substring(0,pos);
						valueXmlString.append(reStr);

					}
				}
			}
			//end manish


			valueXmlString.append("</Detail2>");

			System.out.println("valueXmlString In detail"+valueXmlString.toString());	

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception : [PorcpIC][itemChangedDetail( String, String )] :==>\n"+ e.getMessage());
			throw new ITMException(e); 
		}
		//Add by Ajay Jadhav on 27/04/18:START
		finally {
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
		//Add by Ajay Jadhav on 27/04/18:END
		return valueXmlString.toString();
	}
	private static String getAbsString( String str )
	{
		return ( str == null || str.trim().length() == 0 || "null".equalsIgnoreCase( str.trim() ) ? "" : str.trim() );
	}
	private String gfChkStkOpt(String lsitemcode,String lssitecode,Connection conn)
	{
		String lsstkopt="";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		try
		{
			sql="select case when stk_opt is null then '0' else stk_opt end from item where item_code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, lsitemcode);
			rs = pstmt.executeQuery();
			if (rs.next()) 
			{
				lsstkopt = checkNull(rs.getString(1));
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		}
		catch(Exception e)
		{
			System.out.println("The Exception occurs in gfChkStkOpt :"+e);
		}
		System.out.println("retStrInDate :"+lsstkopt);

		return lsstkopt;
	}
	private String gfgetmfgsite(String asitem, String assite, String asloc, String aslotno, String aslotsl, String astype,Connection conn )
	{
		String lsmfgsite="", lserrcode="";
		PreparedStatement pstmt = null,pstmt1=null,pstmt2=null;
		ResultSet rs = null,rs1=null,rs2=null;
		String sql = "",sql2="",sql3="";
		int asType=2;
		try
		{
			System.out.println("aslotno["+aslotno+"]");
			System.out.println("aslotno length["+aslotno.trim().length()+"]");
			//removed by Varsha V for allowing space in lot no.
			//if(aslotno== null || aslotno.trim().length()==0)
			if(aslotno== null || aslotno.length()==0)
			{
				aslotno="               ";
				System.out.println("aslotno null ["+aslotno+"]");
			}
			if("M".equalsIgnoreCase(astype))
			{
				sql="select site_code__mfg  from stock where item_code = ? and	site_code = ? and loc_code  = ? and lot_no = ? and lot_sl	 = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, asitem);
				pstmt.setString(2, assite);
				pstmt.setString(3, asloc);
				pstmt.setString(4, aslotno);
				pstmt.setString(5, aslotsl);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					lsmfgsite = rs.getString(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				System.out.println("Sql1["+sql+"]");
				System.out.println(" mfgSIte COde is"+lsmfgsite);
				System.out.println("mfgSiteCode Length is"+lsmfgsite.trim().length());
				if(lsmfgsite == null || lsmfgsite.trim().length() ==0)
				{
					sql2="select site_code  from item where item_code = ?";
					pstmt1 = conn.prepareStatement(sql2);
					pstmt1.setString(1, asitem);
					rs1 = pstmt1.executeQuery();
					if (rs1.next()) 
					{
						lsmfgsite = checkNull(rs1.getString(1));
					}
					else
					{
						lsmfgsite = "NOTFOUND";	
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					System.out.println("Sql3["+sql2+"]");
				}
				//}
			}

			if("P".equalsIgnoreCase(astype))
			{
				sql="select pack_code from stock where item_code = ? and site_code = ? and loc_code  = ? and lot_no = ? and	lot_sl= ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, asitem);
				pstmt.setString(2, assite);
				pstmt.setString(3, asloc);
				pstmt.setString(4, aslotno);
				pstmt.setString(5, aslotsl);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					lsmfgsite = rs.getString(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				System.out.println("Packing COde is"+lsmfgsite);
				System.out.println("Packing COde Length is"+lsmfgsite.trim().length());
				if(lsmfgsite== null || lsmfgsite.trim().length() ==0)
				{
					sql2="select pack_code from item where item_code = ? ";
					pstmt1 = conn.prepareStatement(sql2);
					pstmt1.setString(1, asitem);
					rs1 = pstmt1.executeQuery();
					if (rs1.next()) 
					{
						lsmfgsite = checkNull(rs1.getString(1));
					}
					else
					{
						lsmfgsite = "NOTFOUND";	
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;

				}
			}
		}
		catch(Exception e)
		{
			System.out.println("The Exception occurs in gfgetmfgsite :"+e);
		}
		System.out.println("Packing COde || site_code :["+lsmfgsite+"]");
		return lsmfgsite;
	}
	private String checkNull(String input) {
		if (input == null) {
			input = "";
		}
		return input;
	}

	private String checkNullandTrim(String input) {
		if (input == null) {
			input = "";
		}
		return input.trim();
	}
	private String  gbfcheckpodetsite(String purcOrder,String siteCode,Connection conn)
	{

		String errcode="", lssitecode="",lssiteparm=""; 
		int cnt=0;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		try
		{
			DistCommon discommon = new DistCommon();
			sql="select count(*) from porddet " +
					"where purc_order = ? " +
					"and site_code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, purcOrder);
			pstmt.setString(2, siteCode);
			rs = pstmt.executeQuery();
			if (rs.next()) 
			{
				cnt = rs.getInt(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("gbfcheckpodetsite cnt is ["+cnt+"]");
			lssiteparm = discommon.getDisparams("999999", "RCP_SITE", conn);
			if( cnt == 0 )
			{
				errcode = "VTCPORDS";
			}
			else
			{
				errcode = "NOTFOUND";
			}
		}catch (Exception e)
		{

			try {
				throw new ITMException(e);
			} catch (ITMException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		return errcode;
	}
	private String gf_check_conv_fact(String itemCode, String unitfrom,String unitto, Double convfact, Connection conn)
			throws SQLException
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int recCnt = 0;
		String errCode = "", variencetype = "", order = "NOTFOUND", sql = "";
		double varience = 0, mastfact = 0;

		System.out.println("@@@@@2 unitfrom[" + unitfrom + "]::unitto["+ unitto + "]::convfact[" + convfact + "]");
		if (unitfrom.equalsIgnoreCase(unitto) && (!(convfact == 1)))
		{
			errCode = "VTUCON1";
			return errCode;
		}

		sql = " select fact, varience_type, varience_value "
				+ " from uomconv  where ( uomconv.unit__fr = ? ) and"
				+ " ( uomconv.unit__to = ? ) and ( uomconv.item_code = ? )   ";
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, unitfrom);
		pstmt.setString(2, unitto);
		pstmt.setString(3, itemCode);
		rs = pstmt.executeQuery();
		if (rs.next())
		{
			mastfact = rs.getDouble(1);
			variencetype = rs.getString(1);
			varience = rs.getDouble(1);
			recCnt++;
		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;
		System.out.println("recCnt == 0["+recCnt+"]");
		if (recCnt == 0) 
		{
			// 2. Check in the reverse order (TO - FROM) for the item
			sql = " select fact, varience_type, varience_value "
					+ " from uomconv  where ( uomconv.unit__fr = ? ) and ( uomconv.unit__to = ? ) "
					+ " and ( uomconv.item_code = ? )  ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, unitto);
			pstmt.setString(2, unitfrom);
			pstmt.setString(3, itemCode);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				mastfact = rs.getDouble(1);
				variencetype = rs.getString(1);
				varience = rs.getDouble(1);
				recCnt++;
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("recCnt == 1["+recCnt+"]");
			if (recCnt == 0) 
			{
				sql = " select fact, varience_type, varience_value "
						+ " from uomconv  where ( uomconv.unit__fr = ? ) and ( uomconv.unit__to = ? ) "
						+ " and ( uomconv.item_code = 'X' )  ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, unitfrom);
				pstmt.setString(2, unitto);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					mastfact = rs.getDouble(1);
					variencetype = rs.getString(1);
					varience = rs.getDouble(1);
					recCnt++;
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				System.out.println("recCnt == 2["+recCnt+"]");
				if (recCnt == 0)
				{

					sql = " select fact, varience_type, varience_value "
							+ " from uomconv  where ( uomconv.unit__fr = ? ) and ( uomconv.unit__to = ? ) "
							+ " and ( uomconv.item_code = 'X' ) ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, unitto);
					pstmt.setString(2, unitfrom);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						mastfact = rs.getDouble(1);
						variencetype = rs.getString(1);
						varience = rs.getDouble(1);
						recCnt++;
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if (recCnt == 0) {
						order = "NOTFOUND";
					} else 
					{
						order = "REVORD";
					}
				} 
				else 
				{

					order = "ACTORD";
				}
			}
			else 
			{ 
				order = "REVORD";
			}
		} 
		else 
		{ 
			order = "ACTORD";
		}


		if (variencetype == null || variencetype.length() == 0) 
		{
			variencetype = "";
		}
		System.out.println("Order"+order);
		if ("NOTFOUND".equalsIgnoreCase(order))
		{
			errCode = "VTUOMCONV";
		} 
		else
		{
			if ("REVORD".equalsIgnoreCase(order)) 
			{
				if (!(mastfact == 0)) {
					mastfact = 1 / mastfact;
				}
				if (!(varience == 0)) {
					varience = 1 / varience;
				}
			}

			if ("P".equalsIgnoreCase(variencetype)) 

			{
				varience = mastfact * varience / 100;
			} else if ("F".equalsIgnoreCase(variencetype)) 

			{
			}
			if (convfact > mastfact + varience) {
				errCode = "VTUOMVAR";
			}
		}

		return errCode;

	}
	private String setDescription(String descr, String table, String field,String value, Connection conn) throws SQLException 
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		System.out.println("@@@@@@@@table[" + table + "]:::field[" + field
				+ "]::value[" + value + "]");
		sql = "select " + descr + " from " + table + " where " + field
				+ " = ? ";
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, value);
		rs = pstmt.executeQuery();
		if (rs.next())
		{
			descr = checkNull(rs.getString(1));
		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;
		System.out.print("========>::descr[" + descr + "]");
		return descr;
	}
	private String gfqcreqd( String sitecode,String itemcode,Connection conn) throws SQLException 
	{
		String lsqcreqd="";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		sql="select case when qc_reqd is null then 'N' else qc_reqd end from siteitem where item_code = ? and  site_code = ?";
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, itemcode);
		pstmt.setString(2, sitecode);
		rs = pstmt.executeQuery();
		if (rs.next())
		{
			lsqcreqd =checkNull(rs.getString(1));
		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;
		return lsqcreqd;//
	}
	//commented -by -monika-29-may-2019

	//private String toTalCostAddition(double acfreight,double acinsurance,double acclearing,double acother,double exchratefrt,double exchratefrtins,double exchratefrtclr,double exchratetoth ,Connection conn) throws SQLException 

	private String toTalCostAddition(Document dom,Connection conn) throws Exception
	{// setNodeValue( dom, "exch_rate__ins", lcexchrate );
		/*String lcexchrate1="",lcexchrate2="",lcexchrate3="",lcexchrate4="";
			String lcadditionalcost1="";*/
		//changes -made-by monika
		String lcexchrate2="",lcexchrate3="",lcexchrate4="",lcexchrate5="",lcexchrate6="",sql="",lscurrcodebase="",lscurrcode="",lssitecode="";
		conn = getConnection();
		double lcexchrate;
		PreparedStatement pstmt = null;
		ResultSet rs = null;	
		StringBuffer valueXmlString = new StringBuffer();
		String Effdate1="";
		Timestamp effdate=null;
		String lcadditionalcost1="";
		String lcexchrate1="",lcfreight1="",lcinsurance1="",lcclearing1="",lcother1="";
		double acfreight=0,acinsurance=0,acclearing=0,acother=0;
		double exchratefrt=0,exchratefrtins=0,exchratefrtclr=0,exchratetoth=0;
		lscurrcode =genericUtility.getColumnValue( "curr_code__ins", dom ); 
		lssitecode =genericUtility.getColumnValue( "site_code", dom ); 
		Effdate1 =genericUtility.getColumnValue("eff_date", dom);
		lcfreight1 =  checkNull(genericUtility.getColumnValue( "frt_amt", dom ));
		if(lcfreight1 == null ||  lcfreight1.trim().length() == 0)
		{
			lcfreight1 = "0" ;
		}
		acfreight=lcfreight1==null ?0:Double.parseDouble(lcfreight1);
		lcinsurance1 =  checkNull(genericUtility.getColumnValue( "insurance_amt", dom ));
		if(lcinsurance1 == null ||  lcinsurance1.trim().length() == 0)
		{
			lcinsurance1 = "0" ;
		}
		acinsurance=lcinsurance1==null ?0:Double.parseDouble(lcinsurance1);
		lcclearing1 = checkNull( genericUtility.getColumnValue( "clearing_charges", dom ));
		if(lcclearing1 == null ||  lcclearing1.trim().length() == 0)
		{
			lcclearing1 = "0" ;
		}
		acclearing=lcclearing1==null ?0:Double.parseDouble(lcclearing1);
		lcother1 =  checkNull(genericUtility.getColumnValue( "other_charges", dom ));
		if(lcother1 == null ||  lcother1.trim().length() == 0)
		{
			lcother1 = "0" ;
		}
		acother=lcother1==null ?0:Double.parseDouble(lcother1);
		System.out.println("exch_rate__ins-->frt_amt["+acfreight+"]");
		System.out.println("exch_rate__ins-->insurance_amt["+acinsurance+"]");
		System.out.println("exch_rate__ins-->clearing_charges["+acclearing+"]");
		System.out.println("exch_rate__ins-->other_charges["+lcother1+"]");
		effdate = Timestamp.valueOf(genericUtility.getValidDateString(Effdate1.toString(),genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+ " 00:00:00.0");
		sql="select a.curr_code from finent a, site b where b.fin_entity = a.fin_entity and b.site_code = ? ";
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, lssitecode);
		rs = pstmt.executeQuery();
		if (rs.next()) {
			lscurrcodebase = checkNull(rs.getString(1));
		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;
		if(lscurrcode !=null && lscurrcode.trim().length()>0)
		{
			lcexchrate=finCommon.getDailyExchRateSellBuy(lscurrcode, lscurrcodebase, lssitecode, Effdate1, "B", conn);
			valueXmlString.append("<exch_rate__ins protect =\"1\">").append("<![CDATA[" + lcexchrate + "]]>").append("</exch_rate__ins>");
			// gbf_exchrate_protect(ls_currcode,ls_sitecode,'exch_rate__clr') remaining  in code
		}

		lcexchrate3 = checkNull(genericUtility.getColumnValue( "exch_rate__frt", dom));
		if(lcexchrate3 == null ||  lcexchrate3.trim().length() == 0)
		{
			lcexchrate3 = "0" ;
		}
		exchratefrt=lcexchrate3==null ?0:Double.parseDouble(lcexchrate3);
		lcexchrate4 = checkNull(genericUtility.getColumnValue( "exch_rate__ins", dom ));
		if(lcexchrate4 == null ||  lcexchrate4.trim().length() == 0)
		{
			lcexchrate4 = "0" ;
		}
		exchratefrtins=lcexchrate4==null ?0:Double.parseDouble(lcexchrate4);
		lcexchrate5 = checkNull(genericUtility.getColumnValue( "exch_rate__clr", dom ));
		if(lcexchrate5 == null ||  lcexchrate5.trim().length() == 0)
		{
			lcexchrate5 = "0" ;
		}
		exchratefrtclr=lcexchrate3==null ?0:Double.parseDouble(lcexchrate5);
		lcexchrate6 = checkNull(genericUtility.getColumnValue( "exch_rate__othch", dom ));
		if(lcexchrate6 == null ||  lcexchrate6.trim().length() == 0)
		{
			lcexchrate6 = "0" ;
		}
		exchratetoth=lcexchrate4==null ?0:Double.parseDouble(lcexchrate6);


		E12GenericUtility genericUtility= new  E12GenericUtility();
		double lcadditionalcost=0;
		try
		{
			System.out.println("TotalCost :acfreight["+acfreight+"]");
			if (acfreight != 0)
			{
				acfreight = acfreight * exchratefrt;

			}
			System.out.println("TotalCost :acinsurance["+acinsurance+"]");
			if (acinsurance != 0)
			{
				acinsurance = acinsurance * exchratefrtins;

			}
			System.out.println("TotalCost :acclearing["+acclearing+"]");
			if (acclearing != 0)
			{
				acclearing = acclearing * exchratefrtclr;

			}
			System.out.println("TotalCost :acother["+acother+"]");
			if (acother != 0)
			{
				acother = acother * exchratetoth;
			}
			System.out.println("frt_amt exchrate["+exchratefrt+"]");
			System.out.println("insurance_amt exchrate["+exchratefrtins+"]");
			System.out.println("clearing_charges exchrate["+exchratefrtclr+"]");
			System.out.println("other_charges exchrate["+exchratetoth+"]");
			System.out.println("lcadditionalcost = acfreight["+acfreight+"] + acinsurance ["+acinsurance+"] + acclearing["+acclearing+"] + acother"+acother+"]");
			lcadditionalcost = acfreight + acinsurance  + acclearing + acother;
			System.out.println("lcadditionalcost[Method]["+lcadditionalcost+"]");
			lcadditionalcost1=String.valueOf(lcadditionalcost);
			System.out.println("lcadditionalcost[Method] After Parse["+lcadditionalcost+"]");
		}
		catch (Exception e)
		{

			try {
				throw new ITMException(e);
			} catch (ITMException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return lcadditionalcost1;
	}
	private String gfautoqcreqd( String sitecode,String itemcode,Connection conn) throws SQLException 
	{
		String lsautoreqc="";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		sql="select case when  auto_reqc is null then 'Y' else auto_reqc end " +
				" from siteitem where" +
				" site_code = ? and item_code = ?";
		pstmt = conn.prepareStatement(sql);
		//Commented and added by Varsha V on 01-06-18 because wrong parameters were appended
		//pstmt.setString(1, itemcode);
		//pstmt.setString(2, sitecode);
		pstmt.setString(1, sitecode);
		pstmt.setString(2, itemcode);
		//Commented and added by Varsha V on 01-06-18 because wrong parameters were appended
		rs = pstmt.executeQuery();
		if (rs.next()) 
		{
			lsautoreqc = rs.getString(1);
		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;

		if( lsautoreqc== null || lsautoreqc.trim().length() == 0) 
		{
			sql="select case when auto_reqc is null then 'Y' else auto_reqc end  from item where item_code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, itemcode);
			rs = pstmt.executeQuery();
			if (rs.next()) 
			{
				lsautoreqc = checkNull(rs.getString(1));
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		}

		return lsautoreqc;
	}
	private String isExist(String table, String field, String value,
			Connection conn) throws SQLException {
		String sql = "", retStr = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int cnt = 0;

		sql = " SELECT COUNT(1) FROM " + table + " WHERE " + field + " = ? ";
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, value);
		rs = pstmt.executeQuery();
		if (rs.next()) {
			cnt = rs.getInt(1);
		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;
		if (cnt > 0) {
			retStr = "TRUE";
		}
		if (cnt == 0) {
			retStr = "FALSE";
		}
		System.out.println("@@@@ isexist[" + value + "]:::[" + retStr + "]:::["
				+ cnt + "]");
		return retStr;
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
	private String checkDate( String input ) throws ITMException
	{
		try
		{
			if(input != null && input.trim().length() > 0 && (! input.trim().equals("null")))
			{
				input =	genericUtility.getValidDateString(input, getApplDateFormat());
			}
			else
				input = "";
		}
		catch (Exception e) 
		{
			input = "";
			System.out.println("Error in date format at 1283"+e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);

		}


		return input;
	}
	private  java.sql.Timestamp CalcExpiry1(java.sql.Timestamp date, double shelfLife) throws Exception
	{


		UtilMethods utilMethods = UtilMethods.getInstance();
		java.sql.Timestamp expDate = null;
		int lastDay = 0, iShelfLife = 0;
		iShelfLife = (int)shelfLife;
		System.out.println("iShelfLife["+iShelfLife+"]");
		if (iShelfLife < 0)
		{
			iShelfLife = iShelfLife + 1;
		}
		else
		{
			iShelfLife = iShelfLife - 1;
		}
		//dhiraj
		System.out.println("iShelfLife < 0["+iShelfLife+"]");
		expDate = utilMethods.AddMonths(date, iShelfLife);
		System.out.println("expDate ===["+expDate+"]");
		Calendar cal = Calendar.getInstance();
		cal.setTime(expDate);
		System.out.println("cal.setTime(expDate);["+cal.getTime()+"]");

		lastDay = cal.getActualMaximum(cal.DAY_OF_MONTH);
		System.out.println("lastDay@ABHI ===["+lastDay+"]");
		cal.set(Calendar.DAY_OF_MONTH,lastDay);
		System.out.println("cal ===["+cal+"]");
		if(iShelfLife<0)//dhiraj
		{
			cal.set( cal.get(cal.YEAR), cal.get(cal.MONTH), 1);//dhiraj
		}
		java.util.Date newDate = cal.getTime();
		System.out.println("newDate ===["+newDate+"]");
		SimpleDateFormat sdt = new SimpleDateFormat("yyyy-MM-dd");
		expDate = java.sql.Timestamp.valueOf(sdt.format(newDate) + " 00:00:00.000");
		utilMethods = null;
		System.out.println("return expDate ===["+expDate+"]");


		return expDate;

	}
	private String acctDetrTType(String itemCode, String itemSer, String purpose, String tranType)throws Exception
	{
		System.out.println("acctDetrTType Calling................");
		System.out.println("The values of parameters are :\n itemCode :"+itemCode+" \n itemSer :"+itemSer+" \n purpose :"+purpose+" \n tranType :"+tranType);
		String sql = "", stkOption = "", acctCode = "", cctrCode = "", itemSer1 = "", retStr = "";
		Statement stmt = null;
		ResultSet rs = null;
		Connection conn = null;
		ConnDriver connDriver = new ConnDriver();
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		try
		{
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			stmt = conn.createStatement();
			if (purpose.equals("IN"))
			{ 
				sql = "SELECT STK_OPT FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
				System.out.println("sql :"+sql);
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					stkOption = rs.getString(1);
					System.out.println("stkOption :"+stkOption);
				}
				if (stkOption.equals("0"))
				{
					sql = "SELECT ACCT_CODE__PH,CCTR_CODE__PH FROM ITEM_ACCT_DETR "
							+"WHERE ITEM_CODE = '"+itemCode+"' "
							+"AND ITEM_SER = '"+itemSer+"' "
							+"AND TRAN_TYPE = '"+tranType+"'";
					System.out.println("sql from if part :"+sql);
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						acctCode = rs.getString(1);
						System.out.println("acctCode :"+acctCode);
						cctrCode = rs.getString(2);
						System.out.println("cctrCode :"+cctrCode);
					}
					if (acctCode == null || acctCode.equals(""))
					{
						sql = "SELECT ACCT_CODE__PH,CCTR_CODE__PH FROM ITEM_ACCT_DETR "
								+"WHERE ITEM_SER = ' ' AND ITEM_CODE = '"+itemCode+"' "
								+"AND TRAN_TYPE = '"+tranType+"'";
						System.out.println("sql :"+sql);
						rs = stmt.executeQuery(sql);
						if (rs.next())
						{
							acctCode = rs.getString(1);
							System.out.println("acctCode :"+acctCode);
							cctrCode = rs.getString(2);
							System.out.println("cctrCode :"+cctrCode);
						}
						if (acctCode == null || acctCode.equals(""))
						{
							sql = "SELECT ACCT_CODE__PH,CCTR_CODE__PH FROM ITEM_ACCT_DETR "
									+"WHERE ITEM_SER = ' ' AND ITEM_CODE = '"+itemCode+"' "
									+"AND TRAN_TYPE = ' '";
							System.out.println("sql :"+sql);
							rs = stmt.executeQuery(sql);
							if (rs.next())
							{
								acctCode = rs.getString(1);
								System.out.println("acctCode :"+acctCode);
								cctrCode = rs.getString(2);
								System.out.println("cctrCode :"+cctrCode);
							}
							if (acctCode == null || acctCode.equals(""))
							{
								if (itemSer == null && itemSer.trim().length() == 0)
								{
									sql = "SELECT ITEM_SER FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
									System.out.println("sql :"+sql);
									rs = stmt.executeQuery(sql);
									if (rs.next())
									{
										itemSer1 = rs.getString(1);
										System.out.println("itemSer1 :"+itemSer1);
									}
								}
								else
								{
									itemSer1 = itemSer;
									System.out.println("itemSer1 :"+itemSer1);
								}
								sql = "SELECT ACCT_CODE__PH,CCTR_CODE__PH FROM ITEM_ACCT_DETR "
										+"WHERE ITEM_SER = '"+itemSer1+"' "
										+"AND ITEM_CODE = ' ' AND TRAN_TYPE = '"+tranType+"'";
								System.out.println("sql :"+sql);
								rs = stmt.executeQuery(sql);
								if (rs.next())
								{
									acctCode = rs.getString(1);
									System.out.println("acctCode :"+acctCode);
									cctrCode = rs.getString(2);
									System.out.println("cctrCode :"+cctrCode);
								}
								if (acctCode == null || acctCode.equals(""))
								{
									sql = "SELECT ACCT_CODE__PH,CCTR_CODE__PH FROM ITEM_ACCT_DETR "
											+"WHERE ITEM_SER = '"+itemSer1+"' "
											+"AND ITEM_CODE = ' ' AND TRAN_TYPE = ' '";
									System.out.println("sql :"+sql);
									rs = stmt.executeQuery(sql);
									if (rs.next())
									{
										acctCode = rs.getString(1);
										System.out.println("acctCode :"+acctCode);
										cctrCode = rs.getString(2);
										System.out.println("cctrCode :"+cctrCode);
									}
									if (acctCode == null || acctCode.equals(""))
									{
										sql = "SELECT ACCT_CODE__PH,CCTR_CODE__PH FROM ITEMSER "
												+"WHERE ITEM_SER = '"+itemSer;
										System.out.println("sql :"+sql);
										rs = stmt.executeQuery(sql);
										if (rs.next())
										{
											acctCode = rs.getString(1);
											System.out.println("acctCode :"+acctCode);
											cctrCode = rs.getString(2);
											System.out.println("cctrCode :"+cctrCode);
										}
									}
								}
							}
						}						
					} // end if III
				} // end if II
				else
				{
					sql = "SELECT ACCT_CODE__IN,CCTR_CODE__IN FROM ITEM_ACCT_DETR "
							+"WHERE ITEM_CODE = '"+itemCode+"' "
							+"AND ITEM_SER = '"+itemSer+"' "
							+"AND TRAN_TYPE = '"+tranType+"'";
					System.out.println("sql from else part :"+sql);
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						acctCode = rs.getString(1);
						System.out.println("acctCode :"+acctCode);
						cctrCode = rs.getString(2);
						System.out.println("cctrCode :"+cctrCode);
					}
					if (acctCode == null || acctCode.trim().length() == 0)
					{// if I
						sql = "SELECT ACCT_CODE__IN,CCTR_CODE__IN FROM ITEM_ACCT_DETR "
								+"WHERE ITEM_SER = ' ' AND ITEM_CODE = '"+itemCode+"' "
								+"AND TRAN_TYPE = '"+tranType+"'";
						System.out.println("sql from else part :"+sql);
						rs = stmt.executeQuery(sql);
						if (rs.next())
						{
							acctCode = rs.getString(1);
							System.out.println("acctCode :"+acctCode);
							cctrCode = rs.getString(2);
							System.out.println("cctrCode :"+cctrCode);
						}
						if (acctCode == null || acctCode.trim().length() == 0)
						{// if II
							sql = "SELECT ACCT_CODE__IN,CCTR_CODE__IN FROM ITEM_ACCT_DETR "
									+"WHERE ITEM_SER = ' ' AND ITEM_CODE = '"+itemCode+"' "
									+"AND TRAN_TYPE = ' '";
							System.out.println("sql from else part :"+sql);
							rs = stmt.executeQuery(sql);
							if (rs.next())
							{
								acctCode = rs.getString(1);
								System.out.println("acctCode :"+acctCode);
								cctrCode = rs.getString(2);
								System.out.println("cctrCode :"+cctrCode);
							}
							if (acctCode == null || acctCode.trim().length() == 0)
							{// if III
								if (itemSer == null || itemSer.trim().length() == 0)
								{
									sql = "SELECT ITEM_SER FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
									System.out.println("sql :"+sql);
									rs = stmt.executeQuery(sql);
									if (rs.next())
									{
										itemSer1 = rs.getString(1);
										System.out.println("itemSer1 :"+itemSer1);
									}
								}
								else
								{
									itemSer1 = itemSer;
									System.out.println("itemSer1 :"+itemSer1);
								}
								sql = "SELECT ACCT_CODE__IN,CCTR_CODE__IN FROM ITEM_ACCT_DETR "
										+"WHERE ITEM_SER = '"+itemSer1+"' "
										+"AND ITEM_CODE = ' ' AND TRAN_TYPE = '"+tranType+"'";
								System.out.println("sql from else part :"+sql);
								rs = stmt.executeQuery(sql);
								if (rs.next())
								{
									acctCode = rs.getString(1);
									System.out.println("acctCode :"+acctCode);
									cctrCode = rs.getString(2);
									System.out.println("cctrCode :"+cctrCode);
								}
								if (acctCode == null || acctCode.trim().length() == 0)
								{// if IV
									sql = "SELECT ACCT_CODE__IN,CCTR_CODE__IN FROM ITEM_ACCT_DETR "
											+"WHERE ITEM_SER = '"+itemSer1+"' "
											+"AND ITEM_CODE = ' ' AND TRAN_TYPE = ' '";
									System.out.println("sql :"+sql);
									rs = stmt.executeQuery(sql);
									if (rs.next())
									{
										acctCode = rs.getString(1);
										System.out.println("acctCode :"+acctCode);
										cctrCode = rs.getString(2);
										System.out.println("cctrCode :"+cctrCode);
									}
									if (acctCode == null || acctCode.trim().length() == 0)
									{// IF V
										sql = "SELECT ACCT_CODE__IN,CCTR_CODE__IN FROM ITEMSER "
												+"WHERE ITEM_SER = '"+itemSer+"'";
										System.out.println("sql :"+sql);
										rs = stmt.executeQuery(sql);
										if (rs.next())
										{
											acctCode = rs.getString(1);
											System.out.println("acctCode :"+acctCode);
											cctrCode = rs.getString(2);
											System.out.println("cctrCode :"+cctrCode);
										}
									}// end if V
								}// end if IV
							}//end if III
						}// end if II
					}// end if I
				}//end else
			}// end if I
			else if (purpose.equals("PO"))
			{
				sql = "SELECT ACCT_CODE__AP,CCTR_CODE__AP FROM ITEM_ACCT_DETR WHERE ITEM_CODE = '"+itemCode+"' AND "+
						"ITEM_SER = '"+itemSer+"' AND TRAN_TYPE = '"+tranType+"'";		
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					acctCode = rs.getString("ACCT_CODE__AP");
					cctrCode = rs.getString("CCTR_CODE__AP");
				}
				stmt.close();
				if (acctCode == null || acctCode.trim().length() == 0)
				{
					sql = "SELECT ACCT_CODE__AP,CCTR_CODE__AP FROM ITEM_ACCT_DETR WHERE ITEM_SER = ' ' AND ITEM_CODE = '"+itemCode+"' AND TRAN_TYPE = '"+tranType+"'";
					stmt = conn.createStatement();
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						acctCode = rs.getString("ACCT_CODE__AP");
						cctrCode = rs.getString("CCTR_CODE__AP");
					}
					stmt.close();
					if (acctCode == null || acctCode.trim().length() == 0)
					{
						sql = "SELECT ACCT_CODE__AP,CCTR_CODE__AP FROM ITEM_ACCT_DETR WHERE ITEM_SER = ' ' AND ITEM_CODE = '"+itemCode+"' AND TRAN_TYPE = ' '";
						stmt = conn.createStatement();
						rs = stmt.executeQuery(sql);
						if (rs.next())
						{
							acctCode = rs.getString("ACCT_CODE__AP");
							cctrCode = rs.getString("CCTR_CODE__AP");
						}
						stmt.close();
						if (acctCode == null || acctCode.trim().length() == 0)
						{
							if (itemSer == null || itemSer.trim().length() == 0)
							{
								sql = "SELECT ITEM_SER FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
								stmt = conn.createStatement();
								rs = stmt.executeQuery(sql);
								if (rs.next())
								{
									itemSer1 = rs.getString("ITEM_SER");
								}
							}
							else
							{
								itemSer1 = itemSer;
							}
							stmt = conn.createStatement();
							sql = "SELECT ACCT_CODE__AP,CCTR_CODE__AP FROM ITEM_ACCT_DETR WHERE ITEM_SER = '"+itemSer1+"' AND ITEM_CODE = ' ' AND TRAN_TYPE = '"+tranType+"'";
							rs = stmt.executeQuery(sql);
							if (rs.next())
							{
								acctCode = rs.getString("ACCT_CODE__AP");
								cctrCode = rs.getString("CCTR_CODE__AP");
							}
							stmt.close();
							if (acctCode == null || acctCode.trim().length() == 0)
							{
								sql = "SELECT ACCT_CODE__AP,CCTR_CODE__AP FROM ITEM_ACCT_DETR WHERE ITEM_SER = '"+itemSer1+"' AND ITEM_CODE = ' ' AND TRAN_TYPE = ' '";
								stmt = conn.createStatement();
								rs = stmt.executeQuery(sql);
								if (rs.next())
								{
									acctCode = rs.getString("ACCT_CODE__AP");
									cctrCode = rs.getString("CCTR_CODE__AP");	
								}
								stmt.close();
								if (acctCode == null || acctCode.trim().length() == 0)
								{
									sql = "SELECT ACCT_CODE__AP,CCTR_CODE__AP FROM ITEMSER WHERE ITEM_SER = '"+itemSer1+"'";
									stmt = conn.createStatement();
									rs = stmt.executeQuery(sql);
									if (rs.next())
									{	
										acctCode = rs.getString("ACCT_CODE__AP");
										cctrCode = rs.getString("CCTR_CODE__AP");
									}
								}
							}
						}
					}
				}				
			}
			else if (purpose.equals("PORCP"))
			{
				sql = "SELECT ACCT_CODE__PR,CCTR_CODE__PR FROM ITEM_ACCT_DETR WHERE ITEM_CODE = '"+itemCode+"' AND ITEM_SER = '"+itemSer+"' AND TRAN_TYPE = '"+tranType+"'";		
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					acctCode = rs.getString("ACCT_CODE__PR");
					cctrCode = rs.getString("CCTR_CODE__PR");
				}
				stmt.close();
				if (acctCode == null || acctCode.trim().length() == 0)
				{
					stmt = conn.createStatement();
					sql = "SELECT ACCT_CODE__PR,CCTR_CODE__PR FROM ITEM_ACCT_DETR WHERE ITEM_SER = ' ' AND ITEM_CODE = '"+itemCode+"' AND TRAN_TYPE = '"+tranType+"'";
					rs = stmt.executeQuery(sql);
					if (rs.next())
					{
						acctCode = rs.getString("ACCT_CODE__PR");
						cctrCode = rs.getString("CCTR_CODE__PR");
					}
					stmt.close();
					if (acctCode == null || acctCode.trim().length() == 0)
					{
						stmt = conn.createStatement();
						sql = "SELECT ACCT_CODE__PR,CCTR_CODE__PR FROM ITEM_ACCT_DETR WHERE ITEM_SER = ' ' AND ITEM_CODE = '"+itemCode+"' AND TRAN_TYPE = ' '";
						rs = stmt.executeQuery(sql);
						if (rs.next())
						{
							acctCode = rs.getString("ACCT_CODE__PR");
							cctrCode = rs.getString("CCTR_CODE__PR");
						}
						stmt.close();
						if (acctCode == null || acctCode.trim().length() == 0)
						{
							if (itemSer == null || itemSer.trim().length() == 0)
							{
								stmt = conn.createStatement();
								sql = "SELECT ITEM_SER FROM ITEM WHERE ITEM_CODE = '"+itemCode+"'";
								rs = stmt.executeQuery(sql);
								if (rs.next())
								{
									itemSer1 = rs.getString("ITEM_SER");
								}
								stmt.close();
							}
							else
							{
								itemSer1 = itemSer;	
							}
							sql = "SELECT ACCT_CODE__PR,CCTR_CODE__PR FROM ITEM_ACCT_DETR WHERE ITEM_SER = '"+itemSer1+"' AND ITEM_CODE = ' ' AND TRAN_TYPE = '"+tranType+"'";
							stmt = conn.createStatement();
							rs = stmt.executeQuery(sql);
							if (rs.next())
							{
								acctCode = rs.getString("ACCT_CODE__PR");
								cctrCode = rs.getString("CCTR_CODE__PR");
							}
							stmt.close();
							if (acctCode == null || acctCode.trim().length() == 0)
							{
								sql = "SELECT ACCT_CODE__PR,CCTR_CODE__PR FROM ITEM_ACCT_DETR WHERE ITEM_SER = '"+itemSer1+"' AND ITEM_CODE = ' ' AND TRAN_TYPE = ' '";
								stmt = conn.createStatement();
								rs = stmt.executeQuery(sql);
								if (rs.next())
								{
									acctCode = rs.getString("ACCT_CODE__PR");
									cctrCode = rs.getString("CCTR_CODE__PR");
								}
								stmt.close();
								if (acctCode == null || acctCode.trim().length() == 0)
								{
									sql = "SELECT ACCT_CODE__PR,CCTR_CODE__PR FROM ITEMSER WHERE ITEM_SER = '"+itemSer1+"'";
									stmt = conn.createStatement();
									rs = stmt.executeQuery(sql);
									if (rs.next())
									{
										acctCode = rs.getString("ACCT_CODE__PR");
										cctrCode = rs.getString("CCTR_CODE__PR");
									}
								}
							}
						}
					}
				}
			}			
		}//try 
		catch (SQLException sqx)
		{
			System.out.println("The exception occurs in acctDetrTType() :"+sqx);
			throw new ITMException(sqx);
		}
		catch (Exception e)
		{
			System.out.println("The exception occurs in acctDetrTType() :"+e);
			throw new ITMException(e);
		}
		finally 
		{
			try
			{
				conn.close();
				conn = null;
			}
			catch (Exception e){}
		}
		if (acctCode == null)
		{
			acctCode = "";
		}
		if (cctrCode == null)
		{
			cctrCode = "";
		}
		retStr = acctCode + "\t" + cctrCode;
		System.out.println("retStr :"+retStr);
		return retStr;
	}
	//Add by Ajay Jadhav on 12/04/2018:START
	private static void setNodeValue( Document dom, String nodeName, String nodeVal ) throws Exception
	{
		Node tempNode = dom.getElementsByTagName( nodeName ).item(0);

		if( tempNode != null )
		{
			if( tempNode.getFirstChild() == null )
			{
				CDATASection cDataSection = dom.createCDATASection( nodeVal );
				tempNode.appendChild( cDataSection );
			}
			else
			{
				tempNode.getFirstChild().setNodeValue(nodeVal);
			}
		}
		tempNode = null;
	}
	//Add by Ajay Jadhav on 12/04/2018:END
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
			BaseLogger.log("0", null, null, "SQLException :PoReceiptIc :isChannelPartnerSupp()::" + se.getMessage());
			se.printStackTrace();			
		}

		catch (Exception e)
		{
			BaseLogger.log("0", null, null, "Exception :PoReceiptIc :isChannelPartnerSupp()::" + e.getMessage());
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
	//Pavan Rane 11jun19 end
	//Pavan R 10jul19 start [below method added for no of art to consider from item_lot_packsize, item and if not found then packing master]
	public double getRndamt(double newQty, String round, double roundTo) {
		System.out.println(newQty+"~~~"+round+"~~~"+roundTo);
		double lcMultiply = 1;
		try {
			round = round.toUpperCase();
			if (newQty < 0) {
				lcMultiply = -1;
				newQty = Math.abs(newQty);
			} else if (newQty == 0) {
				return newQty;
			} else if (round.trim().equals("N")) {
				return newQty;
			} else if (roundTo == 0) {
				return newQty;
			}
			if (round.trim().equals("X")) {

				if(newQty == (newQty - (newQty % roundTo)))
				{
					return newQty;
				}
				else 
				{
					newQty = ((newQty - (newQty % roundTo)) + roundTo);
				}				
			}
			if (round.trim().equals("P")) {
				newQty = (newQty - (newQty % roundTo));
			}
			if (round.trim().equals("R")) {
				if ((newQty % roundTo) < (roundTo / 2)) {
					newQty = (newQty - (newQty % roundTo));
				} else {
					newQty = (newQty - (newQty % roundTo) + roundTo);
				}
			}
			System.out.println("newQty[" + newQty + "]");
			System.out.println("lcMultiply[" + lcMultiply + "]");
			newQty = newQty * lcMultiply;
			System.out.println("newQty * lcMultiply[" + newQty + "]");
			return newQty;
		} catch (Exception e) {
			System.out.println("Exception :Conversion Qty ::" + e.getMessage()
			+ ":");

		}
		if (roundTo == 1) {
			newQty = getRequiredDecimal(newQty, 0);
		} else if (roundTo == .1) {
			newQty = getRequiredDecimal(newQty, 1);
		} else if (roundTo == .01) {
			newQty = getRequiredDecimal(newQty, 2);
		} else if (roundTo == .001) {
			newQty = getRequiredDecimal(newQty, 3);
		} else if (roundTo == .0001) {
			newQty = getRequiredDecimal(newQty, 4);
		}
		return newQty;
	}	
	public double getRequiredDecimal(double actVal, int prec) {
		NumberFormat numberFormat = NumberFormat.getIntegerInstance();
		Double DoubleValue = new Double(actVal);
		numberFormat.setMaximumFractionDigits(3);
		String strValue = numberFormat.format(DoubleValue);
		strValue = strValue.replaceAll(",", "");
		double reqVal = Double.parseDouble(strValue);
		return reqVal;
	}
	//Pavan R 10jul19 end--
}