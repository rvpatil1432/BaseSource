package ibase.webitm.ejb.dis;

import ibase.system.config.AppConnectParm;
import ibase.system.config.ConnDriver;
import ibase.utility.BaseLogger;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.E12CreateBatchLoadEjb;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.MasterStatefulLocal;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.fin.MiscDrCrRcpConf;
import ibase.webitm.ejb.mfg.MfgCommon;
import ibase.webitm.ejb.sys.CreateRCPXML;
import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.utility.TransIDGenerator;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.naming.InitialContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

public class PostOrderActivity {
	DistCommon distCommon = new DistCommon();
	FinCommon finCommon = new FinCommon();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	E12GenericUtility genericUtility=new E12GenericUtility();
	public String createPORCP(String despId,String xtraParams,Connection conn,Connection connCP) throws ITMException
	{
		
		String retString="";
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		String sql="";
		String purIntegrate="",siteCode="",custCode="";
		String chPartner="",disLink="",dataStr="";
		String filename="",jbossHome="",chanelPartnerFile="";
		int ediOption=0;
		//Connection connCP=null;
		//ConnDriver connDriver = new ConnDriver();
		long startTime = 0, endTime = 0, totalTime = 0, totalHrs = 0, totlMts = 0, totSecs = 0; // Added
		try
		{
			startTime = System.currentTimeMillis();
			purIntegrate=distCommon.getDisparams("999999", "PUR_INTEGRATED", conn);
			sql="select edi_option from transetup where tran_window = 'w_despatch'";
			pstmt=conn.prepareStatement(sql);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				ediOption=rs.getInt(1);
			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;
			if("Y".equalsIgnoreCase(purIntegrate) || ediOption > 0)
			{
				sql="select cust_code,site_code from despatch where desp_id=?";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1,despId);
				rs=pstmt.executeQuery();
				if(rs.next())
				{
					siteCode=rs.getString("site_code");
					custCode=rs.getString("cust_code");
				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
				sql="select channel_partner, dis_link "
						+ " from site_customer "
						+ " where cust_code = ? "
						+ " and site_code = ?";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1,custCode);
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
					sql="select channel_partner, dis_link  from customer "
							+ " where cust_code = ?";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1,custCode);
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
					if("Y".equalsIgnoreCase(chPartner)|| ediOption > 0)
					{
						if(disLink == "E" || ediOption > 0 )
						{
	
							if(ediOption == 2)
							{
								CreateRCPXML createRCPXML = new CreateRCPXML("w_despatch", "tran_id");
								dataStr = createRCPXML.getTranXML(despId, conn);
								System.out.println("dataStr =[ " + dataStr + "]");
								Document ediDataDom = genericUtility.parseString(dataStr);
	
								E12CreateBatchLoadEjb e12CreateBatchLoad = new E12CreateBatchLoadEjb();
								retString = e12CreateBatchLoad.createBatchLoad(ediDataDom, "w_despatch", ""+ediOption , xtraParams, conn);
								createRCPXML = null;
								e12CreateBatchLoad = null;
	
								if (retString != null && "SUCCESS".equals(retString))
								{
									System.out.println("retString from batchload = [" + retString + "]");
								}
	
							}
						}
						if (("A".equalsIgnoreCase(disLink)|| "S".equalsIgnoreCase(disLink) || "C".equalsIgnoreCase(disLink) ) && "Y".equalsIgnoreCase(purIntegrate))
						{
							connCP = chaneParnerExist(despId,disLink,xtraParams,conn);
							System.out.println("connCP["+connCP+"]");
							//Changed By Nasruddin Start 04-11-16
							long startTime2 = System.currentTimeMillis();
							//retString=createPORCP(despId,disLink,xtraParams,conn,connCP);
							if(connCP != null)
							{
								System.out.println("INSIDE CONNCP CONNECTION");
								retString = createPORCP(despId,disLink,xtraParams,conn,connCP);
							}
							else
							{
								System.out.println("INSIDE CONN CONNECTION");
								retString = createPORCP(despId,disLink,xtraParams,conn,conn);
							}
							long endTime2 = System.currentTimeMillis();
							System.out.println("DIFFERANCE IN TIME createPORCP DATA IN SECONDS INSIDE createPORCP():::["+(endTime2-startTime2)/1000+"]");
							//Changed By Nasruddin END 04-11-16
						}
							
					}
			
			}
			endTime = System.currentTimeMillis();
			totalTime = endTime - startTime;

			totSecs = (int) (((double) 1 / 1000) * (totalTime));
			totalHrs = (int) (totSecs / 3600);
			totlMts = (int) (((totSecs - (totalHrs * 3600)) / 60));
			totSecs = (int) (totSecs - ((totalHrs * 3600) + (totlMts * 60)));

			System.out.println("Total Time Spend for createPORCP [" + totalHrs + "] Hours [" + totlMts + "] Minutes [" + totSecs + "] seconds");
			
		}catch(Exception e)
		{
			e.printStackTrace();
			// retString=e.toString();
			throw new ITMException(e);
		}
		return retString;
		
	}
	public String createPORCP(String despId,String disLink,String xtraParams,Connection conn,Connection connCP) throws ITMException
	{
		String retString ="";
		PreparedStatement pstmt=null,pstmt1=null;
		ResultSet rs=null,rs1=null;
		String sql="";
		String defReasCode="";
		String siteCode="",sordNo="",currCode="",lrNo="",lorryNo="",tranCode="",transMode="",remarks2="",remarks3="",remarks="",stanCodeInit="",custCode="",gpNo="";
		Timestamp despDate=null,today=null,gpDate=null;
		Timestamp lrDate=null;
		double exchRate=0;
		String siteCodeCh="",suppCodeCh="";
		String errCode="",asnReqd="",custPord="",poStatus="";
		String itemSer="";
		int countPO=0;
		String pordType="",poCurrCode="";
		String jobWorkType="",subContType="",acceptCriteria="";
		StringBuffer xmlBuff=null;
		SimpleDateFormat sdf=null;
		GenericUtility genericUtility = GenericUtility.getInstance();
		FinCommon fcommon=new FinCommon();
		double calcExchRate=0;
		String currCodeBase="",qcReqd="";
		String grade="",batchNo="",suppCodeMfg="";
		double potencyPerc=0;
		String suppCodemnfr="";
		boolean isItemLotAvail=false;
		double additionalCost=0,totaddnlost=0,discount=0;
		int lnno=0;
		String lineNo="";
		String itemQcReqd="";
		String sitelocCode="";
		String cancBoMode="";
		String stkOpt="",lineNoPO="",packCode="";
		double ordPerc=0;
		String acctCodeCr="",cctrCodeCr="",acctCodeDr="",cctrCodeDr="";
		HashMap<String,Double> detMap=new HashMap<String,Double>();
		Double prevQty=0.0;
		double ordQty=0,dlvQty=0,PendingQty=0;
		String xmlString="",chgUser="",chgTerm="",loginEmpCode="";
		Timestamp sysDate=null;
		String userId = "";//Added By Pavan R 27/DEC/17
		String custPordPO = "", itemCodeDesp = "", isChannelPartner = "", confPasswd = "", custCodeEnd = "", freeProductLoc="";
		try
		{
			//Modified by Anjali R. on [23/11/2018][Start]
			ITMDBAccessEJB itmDbAccess = new ITMDBAccessEJB();
			//Modified by Anjali R. on [23/11/2018][End]

			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String sysDateStr = sdf1.format(currentDate.getTime());
			//System.out.println("Now the date is :=>  " + sysDateStr);
			sysDate = Timestamp.valueOf(genericUtility.getValidDateString(sysDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			chgUser = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
			chgTerm = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId"));
			loginEmpCode =  genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");	
			//added by Pavan R on 27/DEC/17 userId passwed to savData() and processRequest()
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			System.out.println("userId::["+userId+"]");
			MfgCommon mfgCommon=new MfgCommon();
			/*lb_new_cp_conn = false
					if as_dis_link = 'C' then
						ls_dbms = ProfileString (inifile, "Database_CP", "dbms", "")
						If isnull(ls_dbms) or len(trim(ls_dbms)) = 0 then
							ls_errcode ='VTCPCONN' // Channel Partner database connection has to be defined in itm2.ini 
							goto errfound
						else
							ls_errcode = gbf_create_cp_connection(sqlca_cp)
							if not isnull(ls_errcode) and len(trim(ls_errcode)) > 0 then
								goto errfound
							else
								lb_new_cp_conn = true
							end if
						end if
					end if
			 */
			defReasCode=distCommon.getDisparams("999999", "DEFAULT_REAS_CODE", conn);
			jobWorkType=distCommon.getDisparams("999999", "JOBWORK_TYPE", conn);
			subContType=distCommon.getDisparams("999999", "SUBCONTRACT_TYPE", conn);
			acceptCriteria=distCommon.getDisparams("999999", "ACCEPT_CRITERIA", conn);
			//Manish Mhatre 16oct19 start [to de-allocate free qty customer stock]
			freeProductLoc = distCommon.getDisparams("999999", "FREE_PRODUCT_LOCATION", conn);
			BaseLogger.log("3", null, null, "FREE_PRODUCT_LOCATION["+freeProductLoc+"]");
			//Manish Mhatre 16oct19 end [to de-allocate free qty customer stock]
			if("NULLFOUND".equalsIgnoreCase(acceptCriteria) || acceptCriteria==null)
			{
				acceptCriteria="E";
			}

			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			today = new java.sql.Timestamp(System.currentTimeMillis());
			//16.11.17 added  by Nandkumar gp_no and gp_date 
			sql="select d.desp_date,d.site_code,d.sord_no,d.curr_code,d.exch_rate,"
					+ "d.lr_no,d.lr_date,d.lorry_no,d.tran_code,d.trans_mode,"
					+ "d.gp_no,d.gp_date,"
					+ "s.remarks2,s.remarks3,d.stan_code__init,d.remarks, d.conf_passwd "
					+ " from despatch d, sorder s where d.sord_no=s.sale_order and d.desp_id=?";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1,despId);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				despDate=rs.getTimestamp("desp_date");
				siteCode=checkNull(rs.getString("site_code"));
				sordNo=checkNull(rs.getString("sord_no"));
				currCode=rs.getString("curr_code");
				exchRate=rs.getDouble("exch_rate");
				lrNo=rs.getString("lr_no");
				lrDate=rs.getTimestamp("lr_date");
				lorryNo=rs.getString("lorry_no");
				tranCode=checkNull(rs.getString("tran_code"));
				transMode=checkNull(rs.getString("trans_mode"));
				remarks2=rs.getString("remarks2");
				remarks3=rs.getString("remarks3");
				gpNo=rs.getString("gp_no");
				gpDate=rs.getTimestamp("gp_date");
				stanCodeInit=rs.getString("stan_code__init");
				remarks=rs.getString("remarks");
				confPasswd = rs.getString("conf_passwd");//Pavan Rane 11jun19 start [to store the channel partner flag]
				System.out.println("CPRCP PWD #"+confPasswd);
				// 28/Nov-16 manoharan cust_pord included and separate select commented
				sql="select item_ser, cust_code,cust_pord from sorder where sale_order = ? ";
				pstmt1=conn.prepareStatement(sql);
				pstmt1.setString(1,sordNo);
				rs1=pstmt1.executeQuery();
				if(rs1.next())
				{
					itemSer=checkNull(rs1.getString("item_ser")).trim();
					custCode=rs1.getString("cust_code");
					//Changed by wasim on 14-FEB-17 as getting correct result set 
					//custPord=checkNull(rs.getString("cust_pord"));
					custPord=checkNull(rs1.getString("cust_pord"));						
					System.out.println("sorder custPord["+custPord+"]");
				}
				rs1.close();
				rs1=null;
				pstmt1.close();
				pstmt1=null;
			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;
			sql="select site_code__ch from site_customer "
					+ " where site_code = ? and cust_code = ?"
					+ " and channel_partner = 'Y' and dis_link in ('A','C') ";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1,siteCode);
			pstmt.setString(2,custCode);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				siteCodeCh=checkNull(rs.getString("site_code__ch")).trim();
			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;
			if(siteCodeCh.trim().length()==0)
			{
				sql="select site_code from customer "
						+ " where  cust_code = ? ";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1,custCode);
				rs=pstmt.executeQuery();
				if(rs.next())
				{
					siteCodeCh=checkNull(rs.getString("site_code")).trim();
				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;

			}
			sql=" select supp_code from site_supplier "
					+ " where site_code = ? and site_code__ch = ? "
					+ " and channel_partner = 'Y' and dis_link in ('A','C') ";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1,siteCodeCh);
			pstmt.setString(2,siteCode);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				suppCodeCh=checkNull(rs.getString("supp_code")).trim();
				suppCodemnfr=suppCodeCh;
				isChannelPartner = "Y";
			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;
			if(suppCodeCh.trim().length()==0)
			{
				sql=" select supp_code from supplier "
						+ " where site_code = ?  "
						+ " and channel_partner = 'Y' and dis_link in ('A','C') ";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1,siteCode);
				rs=pstmt.executeQuery();
				if(rs.next())
				{
					suppCodeCh=checkNull(rs.getString("supp_code")).trim();
					suppCodemnfr=suppCodeCh;
					isChannelPartner = "Y";
				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
			}
			if(suppCodeCh.trim().length()==0)
			{
				errCode="VTSUPPCH";
				//Modified by Anjali R. on [23/11/2018][Start]
				//errCode = itmDbAccess.getErrorString("", errCode, userId);
				errCode = itmDbAccess.getErrorString("", errCode, userId,"",conn);
				//Modified by Anjali R. on [23/11/2018][eND]
				return errCode;
			}
			sql="select asn_reqd from supplier where supp_code=? ";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1,suppCodeCh);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				asnReqd=checkNull(rs.getString(1));
			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;
			if("Y".equalsIgnoreCase(asnReqd))
			{
				return retString;
			}
			//added by Pavan R 04Jun19 start[to get first item from despatchdet ]
			sql= "select item_code from despatchdet where desp_id = ?";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1,despId);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				itemCodeDesp = checkNull(rs.getString(1));
				System.out.println("custPord::> despatch itemCode["+itemCodeDesp+"]");
			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;
			//Pavan R 04Jun19 end			
			/* //  28-Nov-16 manoharan commented
				sql=" select cust_pord  from sorder "
						+ " where sale_order = ? ";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1,sordNo);
				rs=pstmt.executeQuery();
				if(rs.next())
				{
					custPord=checkNull(rs.getString(1));
				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
			 */
			if(custPord.trim().length()>0)
			{
				sql="select status  from porder where purc_order = ?";
				pstmt=connCP.prepareStatement(sql);
				pstmt.setString(1,custPord);
				rs=pstmt.executeQuery();
				if(rs.next())
				{
					poStatus=checkNull(rs.getString(1));	
				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
				if(!"O".equalsIgnoreCase(poStatus))
				{
					//Modified by Pavan R 04Jun19 start
					/*sql="select purc_order from	porder "
							+ "	where supp_code = ? "
							+ " and	site_code__dlv = ?	and	item_ser = ? and	status = 'O' "
							+ "	order by purc_order ";*/
					sql = "select porder.purc_order from porder , porddet"
							+ " where porder.purc_order = porddet.purc_order"
							+ " and porder.supp_code = ? and porder.site_code__dlv = ?"	                
							+ " and	porder.item_ser = ? and porddet.item_code = ?"
							+ " and	porder.status = 'O' order by porder.purc_order";
					pstmt=connCP.prepareStatement(sql);
					pstmt.setString(1, suppCodeCh);
					pstmt.setString(2, siteCodeCh);
					pstmt.setString(3, itemSer);
					pstmt.setString(4, itemCodeDesp);
					rs=pstmt.executeQuery();
					if(rs.next())
					{
						custPordPO=checkNull(rs.getString(1));
						System.out.println("After sorder custPord["+custPord+"]");
					}
					rs.close();
					rs=null;
					pstmt.close();
					pstmt=null;
					//if(custPord.trim().length()==0)
					if(custPordPO.trim().length()==0)
					{
						errCode="VTPOINV";
						//Modified by Anjali R. on [23/11/2018][Start]
						//errCode = itmDbAccess.getErrorString("", errCode, userId);
						errCode = itmDbAccess.getErrorString("", errCode, userId,"",conn);
						//Modified by Anjali R. on [23/11/2018][END]
						return errCode;
					}
				}
			}else
			{
				sql="select count(*) from	porder "
						+ "	where supp_code = ? "
						+ " and	site_code__dlv = ?	and	item_ser = ? and	status = 'O' "
						+ "	order by purc_order ";
				pstmt=connCP.prepareStatement(sql);
				pstmt.setString(1, suppCodeCh);
				pstmt.setString(2, siteCodeCh);
				pstmt.setString(3, itemSer);
				rs=pstmt.executeQuery();						
				if(rs.next())
				{
					countPO=rs.getInt(1);
				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
				if(countPO==0)
				{
					errCode="VTPONF";
					//Modified by Anjali R. on [23/11/2018][Start]
					//errCode = itmDbAccess.getErrorString("", errCode, userId);
					errCode = itmDbAccess.getErrorString("", errCode, userId,"",conn);
					//Modified by Anjali R. on [23/11/2018][end]
					return errCode;
				}
				//added and modifed by Pavan R 04Jun19 [to get populate PO if there are multiple open PO and depatch item is in different PO] start
				if(countPO > 1)
				{
					sql = "select a.purc_order from porder a, porddet b, sorder s, despatchdet d where" 
						+" a.purc_order = b.purc_order"
						+" and b.item_code = d.item_code"
						+" and s.sale_order = d.sord_no"
						+" and d.desp_id= ? and a.supp_code = ?"
						+" and a.site_code__dlv = ? and a.item_ser = ? "
						+" and d.item_code= ? and  a.status = 'O'" 
						+" order by a.purc_order ";
					pstmt=connCP.prepareStatement(sql);
					pstmt.setString(1, despId);
					pstmt.setString(2, suppCodeCh);
					pstmt.setString(3, siteCodeCh);
					pstmt.setString(4, itemSer);
					pstmt.setString(5, itemCodeDesp);
					rs=pstmt.executeQuery();						
					if(rs.next())
					{
						custPordPO = checkNull(rs.getString(1));	
					}
					rs.close();
					rs=null;
					pstmt.close();
					pstmt=null;
				}
				else 
				{					
				/*	sql="select purc_order from	porder "
							+ "	where supp_code = ? "
							+ " and	site_code__dlv = ?	and	item_ser = ? and	status = 'O' "
							+ "	order by purc_order ";*/
					sql = "select a.purc_order from	porder a, porddet b"
							+ "	where a.purc_order =  b.purc_order"
							+ "	and a.supp_code = ? and	a.site_code__dlv = ? "	
							+ "	and	a.item_ser = ? and b.item_code = ? "
							+ "	and	a.status = 'O' order by purc_order ";
					pstmt=connCP.prepareStatement(sql);
					pstmt.setString(1, suppCodeCh);
					pstmt.setString(2, siteCodeCh);
					pstmt.setString(3, itemSer);
					pstmt.setString(4, itemCodeDesp);
					rs=pstmt.executeQuery();						
					if(rs.next())
					{
						//custPord=checkNull(rs.getString(1));
						custPordPO=checkNull(rs.getString(1));
					}
					rs.close();
					rs=null;
					pstmt.close();
					pstmt=null;
				}				
			}
			if (custPordPO == null || custPordPO.trim().length() == 0)
			{
				custPordPO = custPord; 
			}
			//added and modifed by Pavan R 04Jun19 end
			sql=   //"select pord_type ,curr_code "
					"select pord_type ,curr_code, accept_criteria "//Changed By PriyankaC on 05JAN18.
					+ " from porder where purc_order = ? ";
			pstmt=connCP.prepareStatement(sql);
			//pstmt.setString(1,custPord); //modifed by Pavan R 04Jun19 end
			pstmt.setString(1,custPordPO);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				pordType=E12GenericUtility.checkNull(rs.getString(1));
				poCurrCode=E12GenericUtility.checkNull(rs.getString(2));
				acceptCriteria = E12GenericUtility.checkNull(rs.getString(3));//Added By PriyankaC on 05JAN18.
			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;
			sql="select f.curr_code  from finent f where"
					+ " f.fin_entity = (select s.fin_entity from site s where "
					+ " s.site_code = ? ) ";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1,siteCodeCh);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				currCodeBase=rs.getString(1);	
			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;
			
			//Added By PriyankaC on 09MAY2019 to get qc_reqd from itemser.. .
			sql="select case when qc_reqd is null then 'N' else qc_reqd end from site_supplier where site_code=? and supp_code= ? ";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1,siteCodeCh);
			pstmt.setString(2,suppCodeCh);;
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				qcReqd=checkNull(rs.getString(1));
			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;
			
			if(qcReqd == null ||qcReqd.trim().length()==0)
			{
				sql="select case when qc_reqd is null then 'N' else qc_reqd end from supplier where supp_code= ?";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1,suppCodeCh);;
				rs=pstmt.executeQuery();
				if(rs.next())
				{
					qcReqd=checkNull(rs.getString(1));
				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
				
			
		}
			if(qcReqd == null ||qcReqd.trim().length()==0)
			{
				sql="select case when qc_reqd is null then 'N' else qc_reqd end from itemser where item_ser = ?";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1,itemSer);
				rs=pstmt.executeQuery();
				if(rs.next())
				{
					qcReqd=checkNull(rs.getString(1));
				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
			//Added By PriyankaC on 09MAY2019. [END]
			}
			calcExchRate=fcommon.getDailyExchRateSellBuy(poCurrCode, currCodeBase, siteCodeCh, sdf.format(despDate), "B", conn);
			xmlBuff = new StringBuffer();
			xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
			xmlBuff.append("<DocumentRoot>");
			xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
			xmlBuff.append("<group0>");
			xmlBuff.append("<description>").append("Group0 description").append("</description>");
			xmlBuff.append("<Header0>");
			xmlBuff.append("<objName><![CDATA[").append("porcp").append("]]></objName>");  
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
			xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"porcp\" objContext=\"1\">");  
			xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
			xmlBuff.append("<tran_id/>");
			xmlBuff.append("<tran_ser><![CDATA[P-RCP]]></tran_ser>");
			//Modified by Anjali R. on [20/11/2018][If purchase receipt generated through channel partner then tran_mode should be 'A' (Generated Automatic)][Start]
			xmlBuff.append("<tran_mode><![CDATA[A]]></tran_mode>");
			//Modified by Anjali R. on [20/11/2018][If purchase receipt generated through channel partner then tran_mode should be 'A' (Generated Automatic)][End]
			xmlBuff.append("<tran_type><![CDATA[FOR]]></tran_type>");
			xmlBuff.append("<item_ser><![CDATA["+ checkNull(itemSer).trim() +"]]></item_ser>");
			xmlBuff.append("<site_code><![CDATA["+ checkNull(siteCodeCh).trim() +"]]></site_code>");
			//xmlBuff.append("<purc_order><![CDATA["+ checkNull(custPord).trim() +"]]></purc_order>");//modifed by Pavan R 04Jun19 end
			xmlBuff.append("<purc_order><![CDATA["+ checkNull(custPordPO).trim() +"]]></purc_order>");
			xmlBuff.append("<supp_code><![CDATA["+ checkNull(suppCodeCh).trim() +"]]></supp_code>");
			xmlBuff.append("<tran_code><![CDATA["+ checkNull(tranCode).trim() +"]]></tran_code>");
			xmlBuff.append("<trans_mode><![CDATA["+ checkNull(transMode).trim() +"]]></trans_mode>");
			xmlBuff.append("<curr_code><![CDATA["+ checkNull(poCurrCode).trim() +"]]></curr_code>");
			xmlBuff.append("<exch_rate><![CDATA["+  calcExchRate +"]]></exch_rate>");
			xmlBuff.append("<lr_no><![CDATA["+  lrNo +"]]></lr_no>");
			if(lrDate != null){
				xmlBuff.append("<lr_date><![CDATA["+  sdf.format(lrDate) +"]]></lr_date>");
			}else
			{
				xmlBuff.append("<lr_date><![CDATA[]]></lr_date>");

			}
			if(despDate !=null)
			{
				xmlBuff.append("<tran_date><![CDATA["+ sdf.format(despDate) +"]]></tran_date>");
			}else
			{
				xmlBuff.append("<tran_date><![CDATA[]]></tran_date>");
			}
			if(despDate !=null)
			{
				xmlBuff.append("<eff_date><![CDATA["+ sdf.format(despDate) +"]]></eff_date>");
			}else
			{
				xmlBuff.append("<eff_date><![CDATA[]]></eff_date>");
			}
			xmlBuff.append("<remarks2><![CDATA["+ remarks2 +"]]></remarks2>");
			xmlBuff.append("<remarks3><![CDATA["+ remarks3 +"]]></remarks3>");
			xmlBuff.append("<stan_code__init><![CDATA["+ stanCodeInit +"]]></stan_code__init>");
			xmlBuff.append("<lorry_no><![CDATA["+ lorryNo +"]]></lorry_no>");
			xmlBuff.append("<dc_no><![CDATA["+ despId +"]]></dc_no>");
			if(despDate !=null)
			{
				xmlBuff.append("<dc_date><![CDATA["+ sdf.format(despDate) +"]]></dc_date>");
			}else
			{
				xmlBuff.append("<dc_date><![CDATA[]]></dc_date>");
			}
			xmlBuff.append("<reciept_type><![CDATA[F]]></reciept_type>");
			xmlBuff.append("<supp_code__ship><![CDATA["+suppCodeCh+"]]></supp_code__ship>");
			xmlBuff.append("<accept_criteria><![CDATA["+acceptCriteria+"]]></accept_criteria>");
			xmlBuff.append("<confirmed><![CDATA[N]]></confirmed>");
			xmlBuff.append("<qc_reqd><![CDATA["+qcReqd+"]]></qc_reqd>");
			xmlBuff.append("<frt_amt><![CDATA[0]]></frt_amt>");
			xmlBuff.append("<insurance_amt><![CDATA[0]]></insurance_amt>");
			xmlBuff.append("<clearing_charges><![CDATA[0]]></clearing_charges>");
			xmlBuff.append("<total_addl_cost><![CDATA[0]]></total_addl_cost>");
			xmlBuff.append("<chg_date><![CDATA[" + sdf.format(sysDate) + "]]></chg_date>");
			xmlBuff.append("<chg_user><![CDATA[" + chgUser + "]]></chg_user>");
			xmlBuff.append("<chg_term><![CDATA[" + chgTerm + "]]></chg_term>");
			xmlBuff.append("<excise_ref><![CDATA["+ checkNull(gpNo).trim() +"]]></excise_ref>");
			xmlBuff.append("<invoice_no><![CDATA["+ checkNull(gpNo).trim() +"]]></invoice_no>");
			//Changes done by mayur on 26-02-18------START
			if(gpDate !=null)
			{
				xmlBuff.append("<excise_ref_date><![CDATA["+sdf.format(gpDate) +"]]></excise_ref_date>");
			}
			//Changes done by mayur on 26-02-18------END
			//xmlBuff.append("<excise_ref_date><![CDATA["+sdf.format(gpDate) +"]]></excise_ref_date>");
			xmlBuff.append("<freight_status><![CDATA[T]]></freight_status>");
			//Pavan Rane 11jun19 start [to store the channel partner flag and generated password]
			if("Y".equals(isChannelPartner))
			{
				xmlBuff.append("<channel_partner><![CDATA[" + "Y" + "]]></channel_partner>");
				xmlBuff.append("<conf_passwd><![CDATA[" + confPasswd + "]]></conf_passwd>");
			}else {
				xmlBuff.append("<channel_partner><![CDATA[" + "N" + "]]></channel_partner>");
				xmlBuff.append("<conf_passwd><![CDATA[ ]]></conf_passwd>");
			}			
			//Pavan Rane 11jun19 end
			xmlBuff.append("</Detail1>");

			String itemCode="",sordNoDet="",soLineNoDet="",taxClass="",taxChap="",taxEnv="", taxChapDesp="",explev="";
			String unit="",unitStd="",locCode="",lotNo="",lotSl="",siteMfg="",dimension="",packInstr="";
			Timestamp  mfgDate=null,expDate=null,retestDate=null;
			double quantity=0,qtyStdUom=0,convQtyStdUom=0,rateStdUom=0,rateClg=0,noArt=0,grossWt=0,tareWt=0,netWt=0,palletWt=0;
			int cntPO=0;
			String errorType="";
			String mapKey="";

			//Added and Commented by sarita to remove tax_class , tax_chap,tax_env data getting from SORDDET table on 26 DEC 2018 [START]
			/*sql="select despdt.line_no,despdt.item_code,despdt.sord_no,despdt.line_no__sord," +
						"sdet.tax_class,sdet.tax_chap,sdet.tax_env,"
						+ "despdt.unit,despdt.unit__std,despdt.quantity,despdt.quantity__stduom,despdt.conv__qty_stduom,"
						+ "despdt.loc_code,despdt.lot_no,despdt.lot_sl,despdt.mfg_date,despdt.exp_date,despdt.rate__stduom,"
						+ "despdt.rate__clg,despdt.no_art,despdt.site_code__mfg," +
						  "despdt.GROSS_WEIGHT,despdt.TARE_WEIGHT,despdt.NETT_WEIGHT,"
						+ "despdt.retest_date,despdt.dimension,despdt.pallet_wt,despdt.pack_instr " +
						  " from  despatchdet despdt,SORDDET sdet where despdt.sord_no =sdet.sale_order and " +
						  " despdt.line_no__sord = sdet.line_no and  despdt.desp_id=? ";*/
			sql="select despdt.line_no,despdt.item_code,despdt.sord_no,despdt.line_no__sord," 
					+ "despdt.unit,despdt.unit__std,despdt.quantity,despdt.quantity__stduom,despdt.conv__qty_stduom,"
					+ "despdt.loc_code,despdt.lot_no,despdt.lot_sl,despdt.mfg_date,despdt.exp_date,despdt.rate__stduom,"
					+ "despdt.rate__clg,despdt.no_art,despdt.site_code__mfg," +
					"despdt.GROSS_WEIGHT,despdt.TARE_WEIGHT,despdt.NETT_WEIGHT,"
					+ "despdt.retest_date,despdt.dimension,despdt.pallet_wt,despdt.pack_instr, despdt.tax_chap,despdt.exp_lev, sdet.cust_code__end " + 
					" from  despatchdet despdt,SORDDET sdet where despdt.sord_no =sdet.sale_order and " +
					" despdt.line_no__sord = sdet.line_no and  despdt.desp_id=? ";
			//Added and Commented by sarita to remove tax_class , tax_chap,tax_env data getting from SORDDET table on 26 DEC 2018 [END]
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1,despId);
			rs=pstmt.executeQuery();
			while(rs.next())
			{
				itemCode=rs.getString("item_code");
				sordNoDet=rs.getString("sord_no");
				soLineNoDet=rs.getString("line_no__sord");
				//Commented by sarita to remove tax_class , tax_chap,tax_env data getting from SORDDET table on 26 DEC 2018 [START]
				/*taxClass=checkNull(rs.getString("tax_class"));
					taxChap=checkNull(rs.getString("tax_chap"));
					taxEnv=checkNull(rs.getString("tax_env"));*/
				//Commented by sarita to remove tax_class , tax_chap,tax_env data getting from SORDDET table on 26 DEC 2018 [END]
				unit=rs.getString("unit");
				unitStd=rs.getString("unit__std");
				quantity=rs.getDouble("quantity");
				qtyStdUom=rs.getDouble("quantity__stduom");
				convQtyStdUom=rs.getDouble("conv__qty_stduom");
				locCode=rs.getString("loc_code");
				lotNo=rs.getString("lot_no");
				lotSl=rs.getString("lot_sl");
				mfgDate=rs.getTimestamp("mfg_date");
				expDate=rs.getTimestamp("exp_date");
				rateStdUom=rs.getDouble("rate__stduom");
				rateClg=rs.getDouble("rate__clg");
				noArt=rs.getDouble("no_art");
				// Modified by Piyush on 04/02/2019 [To check null for site code mfg as suggested by KB Sir]
				// siteMfg=rs.getString("site_code__mfg");
				siteMfg=checkNull(rs.getString("site_code__mfg"));
				grossWt=rs.getDouble("GROSS_WEIGHT");
				tareWt=rs.getDouble("TARE_WEIGHT");
				netWt=rs.getDouble("NETT_WEIGHT");
				retestDate=rs.getTimestamp("mfg_date");
				dimension=rs.getString("dimension");
				palletWt=rs.getDouble("pallet_wt");
				packInstr=rs.getString("pack_instr");
				taxChapDesp=checkNull(rs.getString("tax_chap")); //Pavan Rane 26jul19 [tax chap to be set based on deps or pord in CP Auto-GRN]
				System.out.println("@@Commented by mayur on 28-02-18@@");
				System.out.println("siteMfg["+siteMfg+"]");
				//Addded by Manish Mhatre 16oct19 start [to de-allocate free qty customer stock]
				explev=rs.getString("exp_lev"); 
				custCodeEnd=checkNull(rs.getString("cust_code__end"));
				//Addded by Manish Mhatre 16oct19 end [to de-allocate free qty customer stock]
				//Commented by sarita on 26 DEC 2018 [START]
				/*System.out.println("taxClass["+taxClass+"]");*/
			 System.out.println("taxChapDesp["+taxChapDesp+"]");
					  /*System.out.println("taxEnv["+taxEnv+"]");*/
				//Commented by sarita on 26 DEC 2018 [END]

				//Added By PriyankaC  to get qc_reqd from siteItem. [Start]
				sql="select case when qc_reqd is null then 'N' else qc_reqd end"
						+ " from siteitem where item_code = ? and  site_code = ?";
				pstmt1=conn.prepareStatement(sql);
				pstmt1.setString(1,itemCode);
				pstmt1.setString(2,siteCodeCh);
				rs1=pstmt1.executeQuery();
				if(rs1.next())
				{
					itemQcReqd=rs1.getString(1);
				}
				rs1.close();
				rs1=null;
				pstmt1.close();
				pstmt1=null;
				if(itemQcReqd == null || itemQcReqd.length() == 0)
				{
					sql=      "select case when qc_reqd is null then 'N' else qc_reqd end"
							+ " from item where item_code = ? ";
					pstmt1=conn.prepareStatement(sql);
					pstmt1.setString(1,itemCode);
					rs1=pstmt1.executeQuery();
					if(rs1.next())
					{
						itemQcReqd=rs1.getString(1);
					}
					rs1.close();
					rs1=null;
					pstmt1.close();
					pstmt1=null;
				}
				//Added By PriyankaC  to get qc_reqd from siteItem  [END].
				/*sql="select case when qc_reqd is null then 'N' else qc_reqd end,"
						+ "(case when ordc_perc is null then 0 else ordc_perc end), "
						+ " (case when canc_bo_mode is null then 'A' else canc_bo_mode end)"
						+ " from item where item_code=? ";*/
				
				sql= "select case when ordc_perc is null then 0 else ordc_perc end, "
						+ " (case when canc_bo_mode is null then 'A' else canc_bo_mode end)"
						+ " from item where item_code=? ";
				pstmt1=conn.prepareStatement(sql);
				pstmt1.setString(1,itemCode);
				rs1=pstmt1.executeQuery();
				if(rs1.next())
				{
					ordPerc=rs1.getDouble(1);
					cancBoMode=rs1.getString(2);
				}
				rs1.close();
				rs1=null;
				pstmt1.close();
				pstmt1=null;

				System.out.println("custPord>>>>>"+custPord);

				if(custPord.trim().length()>0)				
				{
					cntPO=0; //added by arun 06-OCT-2017
					//Added and Commented by sarita to set tax_class , tax_chap and tax_env from porddet table on 26 DEC 2018 [START]	
					/*sql="select d.line_no as line_no, d.acct_code__dr as acct_code__dr, d.cctr_code__dr as cctr_code__dr, d.acct_code__cr as  acct_code__cr, d.cctr_code__cr as cctr_code__cr,"
							+ " d.quantity, (case when d.dlv_qty is null then 0 else d.dlv_qty end) as dlvqty "
							+ " from porder h, porddet d where d.purc_order = h.purc_order "
							+ " and   d.purc_order = ? and  d.line_no__sord = ? "
							+ " and	d.status = 'O' and	d.item_code = ?";*/
					sql="select d.line_no as line_no, d.acct_code__dr as acct_code__dr, d.cctr_code__dr as cctr_code__dr, d.acct_code__cr as  acct_code__cr, d.cctr_code__cr as cctr_code__cr,"
							+ " d.quantity, (case when d.dlv_qty is null then 0 else d.dlv_qty end) as dlvqty, "
							+ " d.tax_class, d.tax_chap, d.tax_env "
							+ " from porder h, porddet d where d.purc_order = h.purc_order "
							//+ " and   d.purc_order = ? and  d.line_no__sord = ? "
							+ " and  d.line_no__sord = ? " //Pavan R 04jun19
							+ " and	d.status = 'O' and	d.item_code = ?";
					//Added and Commented by sarita to set tax_class , tax_chap and tax_env from porddet table on 26 DEC 2018 [END]
					pstmt1=connCP.prepareStatement(sql);
					//pstmt1.setString(1,custPord);
					pstmt1.setString(1,soLineNoDet);
					pstmt1.setString(2,itemCode);
					rs1=pstmt1.executeQuery();

					if(rs1.next())
					{
						cntPO++;
						acctCodeCr=rs1.getString("acct_code__cr");
						cctrCodeCr=rs1.getString("cctr_code__cr");
						acctCodeDr=rs1.getString("acct_code__dr");
						cctrCodeDr=rs1.getString("cctr_code__dr");
						lineNoPO=rs1.getString("line_no");
						ordQty=rs1.getDouble("quantity");
						dlvQty=rs1.getDouble("dlvqty");
						//Added by sarita to get tax_class , tax_chap and tax_env from porddet table on 26 DEC 2018[START]
						taxClass=checkNull(rs1.getString("tax_class"));
						taxChap=checkNull(rs1.getString("tax_chap"));
						taxEnv=checkNull(rs1.getString("tax_env"));
						//Added by sarita to get tax_class , tax_chap and tax_env from porddet table on 26 DEC 2018 [END]
					}
					rs1.close();
					rs1=null;
					pstmt1.close();
					pstmt1=null;
					if(cntPO==0)
					{
						//Added and Commented by sarita to set tax_class , tax_chap and tax_env from porddet table on 26 DEC 2018 [START]
						/*sql=" select d.line_no as line_no, d.acct_code__dr as acct_code__dr, d.cctr_code__dr as cctr_code__dr, d.acct_code__cr as acct_code__cr, d.cctr_code__cr as cctr_code__cr, "
									+ " d.quantity, (case when d.dlv_qty is null then 0 else d.dlv_qty end) as dlvqty "
									+ " from porder h, porddet d where d.purc_order = h.purc_order "
									+ " and   d.purc_order = ? and	d.status = 'O' and	d.item_code = ? ";*/
						sql=" select d.line_no as line_no, d.acct_code__dr as acct_code__dr, d.cctr_code__dr as cctr_code__dr, d.acct_code__cr as acct_code__cr, d.cctr_code__cr as cctr_code__cr, "
								+ " d.quantity, (case when d.dlv_qty is null then 0 else d.dlv_qty end) as dlvqty, "
								+ " d.tax_class, d.tax_chap, d.tax_env "
								+ " from porder h, porddet d where d.purc_order = h.purc_order "
								+ " and   d.purc_order = ? and	d.status = 'O' and	d.item_code = ? ";
						//Added and Commented by sarita to set tax_class , tax_chap and tax_env from porddet table on 26 DEC 2018 [END]
						pstmt1=connCP.prepareStatement(sql);
						//pstmt1.setString(1,custPord);
						pstmt1.setString(1,custPordPO);
						pstmt1.setString(2,itemCode);
						rs1=pstmt1.executeQuery();
						// 28-Nov-16 Manoharan
						//while(rs1.next())
						if(rs1.next())
						{
							cntPO++;	
							acctCodeCr=rs1.getString("acct_code__cr");
							cctrCodeCr=rs1.getString("cctr_code__cr");
							acctCodeDr=rs1.getString("acct_code__dr");
							cctrCodeDr=rs1.getString("cctr_code__dr");
							lineNoPO=rs1.getString("line_no");
							ordQty=rs1.getDouble("quantity");
							dlvQty=rs1.getDouble("dlvqty");
							//Added by sarita to get tax_class , tax_chap and tax_env from porddet table on 26 DEC 2018[START]
							taxClass=checkNull(rs1.getString("tax_class"));
							taxChap=checkNull(rs1.getString("tax_chap"));
							taxEnv=checkNull(rs1.getString("tax_env"));
							//Added by sarita to get tax_class , tax_chap and tax_env from porddet table on 26 DEC 2018 [END]
						}
						rs1.close();
						rs1=null;
						pstmt1.close();
						pstmt1=null;
						if(cntPO==0)
						{
							errCode="VTPURCDET";
							//Modified by Anjali R. on [23/11/2018][Start]
							//errCode = itmDbAccess.getErrorString("", errCode, userId);
							errCode = itmDbAccess.getErrorString("", errCode, userId,"",conn);
							//Modified by Anjali R. on [23/11/2018][End]
							return errCode;
						}
					}
					custPord = custPordPO;
				}else
				{
					cntPO=0;
					//Added and Commented by sarita to set tax_class , tax_chap and tax_env from porddet table on 26 DEC 2018 [START]
					/*sql="select d.purc_order, d.line_no as line_no, d.acct_code__dr as acct_code__dr, d.cctr_code__dr as cctr_code__dr, d.acct_code__cr as acct_code__cr, d.cctr_code__cr as cctr_code__cr, "
								+ " d.quantity, (case when d.dlv_qty is null then 0 else d.dlv_qty end) dlvqty from porder h, porddet d "
								+ " where h.supp_code = ? "
								+ " and	h.site_code__dlv = ? "
								+ " and	h.item_ser = ? "
								+ " and	h.status = 'O' "
								+ " and	d.purc_order = h.purc_order "
								+ " and	d.status = 'O' "
								+ " and	d.item_code = ? ";*/
					sql="select d.purc_order, d.line_no as line_no, d.acct_code__dr as acct_code__dr, d.cctr_code__dr as cctr_code__dr, d.acct_code__cr as acct_code__cr, d.cctr_code__cr as cctr_code__cr, "
							+ " d.quantity, (case when d.dlv_qty is null then 0 else d.dlv_qty end) as dlvqty, "
							+ " d.tax_class, d.tax_chap, d.tax_env "
							+ " from porder h, porddet d "
							+ " where h.supp_code = ? "
							+ " and	h.site_code__dlv = ? "
							+ " and	h.item_ser = ? "
							+ " and	h.status = 'O' "
							+ " and	d.purc_order = h.purc_order "
							+ " and	d.status = 'O' "
							+ " and	d.item_code = ? ";
					//Added and Commented by sarita to set tax_class , tax_chap and tax_env from porddet table on 26 DEC 2018 [END]
					pstmt1=connCP.prepareStatement(sql);
					pstmt1.setString(1, suppCodeCh);
					pstmt1.setString(2, siteCodeCh);
					pstmt1.setString(3, itemSer);
					pstmt1.setString(4, itemCode);
					rs1=pstmt1.executeQuery();
					if(rs1.next())
					{
						cntPO++;
						acctCodeCr=rs1.getString("acct_code__cr");
						cctrCodeCr=rs1.getString("cctr_code__cr");
						acctCodeDr=rs1.getString("acct_code__dr");
						cctrCodeDr=rs1.getString("cctr_code__dr");
						lineNoPO=rs1.getString("line_no");
						custPord=rs1.getString("purc_order");
						ordQty=rs1.getDouble("quantity");
						dlvQty=rs1.getDouble("dlvqty");
						//Added by sarita to get tax_class , tax_chap and tax_env from porddet table on 26 DEC 2018[START]
						taxClass=checkNull(rs1.getString("tax_class"));
						taxChap=checkNull(rs1.getString("tax_chap"));
						taxEnv=checkNull(rs1.getString("tax_env"));
						//Added by sarita to get tax_class , tax_chap and tax_env from porddet table on 26 DEC 2018 [END]
					}
					rs1.close();
					rs1=null;
					pstmt1.close();
					pstmt1=null;
					if(cntPO==0)
					{
						errorType=errorType(conn, "VTPURCDET1");
						//errCode="VTPURCDET1";
					}
					if(!"W".equalsIgnoreCase(errorType) && errorType.trim().length() != 0)
					{
						errCode="VTPURCDET1";
						//Modified by Anjali R. on [23/11/2018][Start]
						//errCode = itmDbAccess.getErrorString("", errCode, userId);
						errCode = itmDbAccess.getErrorString("", errCode, userId,"",conn);
						//Modified by Anjali R. on [23/11/2018][End]
						return errCode;
					}
					if(cntPO==0)
					{
						cntPO=0;
						//Added and Commented by sarita to set tax_class , tax_chap and tax_env from porddet table on 26 DEC 2018 [START]
						/*sql="select d.purc_order, d.line_no as line_no, d.acct_code__dr as acct_code__dr, d.cctr_code__dr as cctr_code__dr, d.acct_code__cr as acct_code__cr, d.cctr_code__cr as cctr_code__cr, "
									+ " d.quantity, (case when d.dlv_qty is null then 0 else d.dlv_qty end) as dlvqty  from porder h, porddet d " 
									+ " where h.supp_code = ? "
									+ " and	h.site_code__dlv = ? "
									+ " and	h.item_ser = ? "
									+ " and	h.status = 'O' "
									+ " and	d.purc_order = h.purc_order "
									+ " and	d.status = 'C' "
									+ " and	d.item_code = ? ";*/
						sql="select d.purc_order, d.line_no as line_no, d.acct_code__dr as acct_code__dr, d.cctr_code__dr as cctr_code__dr, d.acct_code__cr as acct_code__cr, d.cctr_code__cr as cctr_code__cr, "
								+ " d.quantity, (case when d.dlv_qty is null then 0 else d.dlv_qty end) as dlvqty, "
								+ " d.tax_class, d.tax_chap, d.tax_env "
								+ " from porder h, porddet d " 
								+ " where h.supp_code = ? "
								+ " and	h.site_code__dlv = ? "
								+ " and	h.item_ser = ? "
								+ " and	h.status = 'O' "
								+ " and	d.purc_order = h.purc_order "
								+ " and	d.status = 'C' "
								+ " and	d.item_code = ? ";
						//Added and Commented by sarita to set tax_class , tax_chap and tax_env from porddet table on 26 DEC 2018 [END]
						pstmt1=connCP.prepareStatement(sql);
						pstmt1.setString(1,suppCodeCh);
						pstmt1.setString(2,siteCodeCh);
						pstmt1.setString(3,itemSer);
						pstmt1.setString(4,itemCode);
						rs1=pstmt1.executeQuery();
						if(rs1.next())
						{
							cntPO++;
							acctCodeCr=rs1.getString("acct_code__cr");
							cctrCodeCr=rs1.getString("cctr_code__cr");
							acctCodeDr=rs1.getString("acct_code__dr");
							cctrCodeDr=rs1.getString("cctr_code__dr");
							lineNoPO=rs1.getString("line_no");
							custPord=rs1.getString("purc_order");
							ordQty=rs1.getDouble("quantity");
							dlvQty=rs1.getDouble("dlvqty");
							//Added by sarita to get tax_class , tax_chap and tax_env from porddet table on 26 DEC 2018[START]
							taxClass=checkNull(rs1.getString("tax_class"));
							taxChap=checkNull(rs1.getString("tax_chap"));
							taxEnv=checkNull(rs1.getString("tax_env"));
							//Added by sarita to get tax_class , tax_chap and tax_env from porddet table on 26 DEC 2018 [END]
						}
						rs1.close();
						rs1=null;
						pstmt1.close();
						pstmt1=null;
						if(cntPO==0)
						{
							errCode="VTPURCDET";
							//Modified by Anjali R. on [23/11/2018][Start]
							//errCode = itmDbAccess.getErrorString("", errCode, userId);
							errCode = itmDbAccess.getErrorString("", errCode, userId,"",conn);
							//Modified by Anjali R. on [23/11/2018][End]
							return errCode;
						}
					}
				}

				//Added by sarita on 26 DEC 2018 [START]
				System.out.println("taxClass["+taxClass+"]");
				System.out.println("taxChap["+taxChap+"]");
				System.out.println("taxEnv["+taxEnv+"]");
				//Added by sarita on 26 DEC 2018 [END]

				sql=" select grade,potency_perc,batch_no,supp_code__mfg,rate  from stock"
						+ "	where item_code = ? and site_code = ? and loc_code = ? "
						+ " and lot_no = ? and lot_sl = ?" ;
				pstmt1=conn.prepareStatement(sql);
				pstmt1.setString(1,itemCode);
				pstmt1.setString(2,siteCodeCh);
				pstmt1.setString(3,locCode);
				pstmt1.setString(4,lotNo);
				pstmt1.setString(5,lotSl);
				rs1=pstmt1.executeQuery();
				if(rs1.next())
				{
					grade=rs1.getString("grade");
					potencyPerc=rs1.getDouble("potency_perc");
					batchNo=rs1.getString("batch_no");
					suppCodeMfg=rs1.getString("supp_code__mfg");
					additionalCost=rs1.getDouble("rate");

					System.out.println("@@@@Commented by mayur on 28-02-18@@@");
					System.out.println("suppCodeMfg["+suppCodeMfg+"]");
				}
				rs1.close(); rs1 = null;//[rs1 and pstmt1 closed and nulled by Pavan R]
				pstmt1.close(); pstmt1 = null;
				isItemLotAvail=false;
				sql=" select supp_code__mfg from item_lot_info "
						+ " where item_code = ? and lot_no = ?";
				pstmt1=conn.prepareStatement(sql);
				pstmt1.setString(1,itemCode);
				pstmt1.setString(2,lotNo);
				rs1=pstmt1.executeQuery();
				if(rs1.next())
				{
					isItemLotAvail=true;
					suppCodemnfr=checkNull(rs1.getString("supp_code__mfg"));
				}
				rs1.close();
				rs1=null;
				pstmt1.close();
				pstmt1=null;
				if(isItemLotAvail)
				{
					//Modified by Anjali R. On [12/11/2018][Start]
					//if(suppCodemnfr.trim().length()==0)
					if(suppCodemnfr == null || suppCodemnfr.trim().length()==0)
					{
						//suppCodemnfr= suppCodeMfg;
						suppCodemnfr= checkNull(suppCodeMfg);
						//Modified by Anjali R. On [12/11/2018][End]
					}
				}
				sql="select pack_code		,	discount from 	 sorddet "
						+ " where  sale_order = ? and line_no = ?";
				pstmt1=conn.prepareStatement(sql);
				pstmt1.setString(1, sordNoDet);
				pstmt1.setString(2, soLineNoDet);
				rs1=pstmt1.executeQuery();
				while(rs1.next())
				{
					packCode=rs1.getString(1);
					discount=rs1.getDouble(2);

				}
				rs1.close();
				rs1=null;
				pstmt1.close();
				pstmt1=null;
				if(pordType.equalsIgnoreCase(jobWorkType) ||pordType.equalsIgnoreCase(subContType))
				{
					additionalCost=additionalCost*quantity;
					totaddnlost+=additionalCost;
				}else 
				{
					additionalCost = 0;
				}
				//above else condition added by Pavan R on 07/Feb/2K18 additionalCost is set to zero

				if("Y".equalsIgnoreCase(itemQcReqd))
				{
					sql="select loc_code__insp  from siteitem where item_code = ? "
							+ "	and site_code = ?";
				}
				else
				{
					sql=" select loc_code__aprv from siteitem where item_code = ? "
							+ " and site_code = ?";
				}
				pstmt1=conn.prepareStatement(sql);
				pstmt1.setString(1,itemCode);
				pstmt1.setString(2,siteCodeCh);
				rs1=pstmt1.executeQuery();
				if(rs1.next())
				{
					sitelocCode=checkNull(rs1.getString(1));
				}
				rs1.close();
				rs1=null;
				pstmt1.close();
				pstmt1=null;	
				if(sitelocCode.trim().length()==0)
				{
					sitelocCode=locCode;
				}

				//stkOpt = mfgCommon.chkStkOpt(itemCode,siteCodeCh,conn);
				stkOpt = mfgCommon.chkStkOpt(siteCodeCh,itemCode,conn);

				lnno++;
				lineNo=String.valueOf(lnno);
				lineNo=lineNo.trim();
				lineNo="   "+lineNo;
				//System.out.println("---"+lineNo+"---");
				lineNo = lineNo.substring(lineNo.length() - 3);
				//System.out.println("--@@@@@-"+lineNo+"---");
				mapKey=custPord+"@"+lineNoPO;
				if(detMap.containsKey(mapKey))
				{
					prevQty=detMap.get(mapKey);
					detMap.put(mapKey, prevQty+quantity);

				}
				else
				{
					detMap.put(mapKey,Double.valueOf(quantity));
					//prevQty=quantity;
					// changes by arun pal 30/05/17 start		
					prevQty=0.0;
					// changes by arun pal 30/05/17 end
					
				}
				//Manish Mhatre 16oct19 start [to de-allocate free qty customer stock]
				if(custCodeEnd != null && custCodeEnd.trim().length() > 0)
				{
					sql = "select b.nature from sorder a, sorditem b"
							+ " where a.sale_order  = b.sale_order"
							+ "	and b.sale_order = ? "
							+ "	and b.line_no = ? "
							+ "	and b.item_code = ? "
							+ " and b.exp_lev = ? "; 
							//+ " and a.cust_code__end is not null";
					pstmt1=conn.prepareStatement(sql);
					pstmt1.setString(1,sordNo );
					pstmt1.setString(2, soLineNoDet);
					pstmt1.setString(3, itemCode);
					pstmt1.setString(4, explev);
					rs1=pstmt1.executeQuery();
					if(rs1.next())
					{
						String nature= rs1.getString("nature");
						
						if(!"C".equalsIgnoreCase(nature) && rateStdUom == 0)
						{						
							if(!"NULLFOUND".equalsIgnoreCase(freeProductLoc))
							{
								sitelocCode = freeProductLoc;
							}	
						}	
					}
					rs1.close();
					rs1=null;
					pstmt1.close();
					pstmt1=null;
				}
				//Manish Mhatre 16oct19 end [to de-allocate free qty customer stock]
				noArt=5;
				xmlBuff.append("<Detail2 dbID='' domID='"+lineNo+"' objName=\"porcp\" objContext=\"2\">"); 
				xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" updateFlag=\"A\" status=\"N\" />");
				xmlBuff.append("<tran_id/>");
				xmlBuff.append("<line_no><![CDATA["+ lineNo +"]]></line_no>");
				xmlBuff.append("<item_code><![CDATA["+ itemCode +"]]></item_code>");
				xmlBuff.append("<purc_order><![CDATA["+ custPord +"]]></purc_order>");
				xmlBuff.append("<line_no__ord><![CDATA["+ lineNoPO +"]]></line_no__ord>");
				xmlBuff.append("<unit><![CDATA["+ unit +"]]></unit>");
				xmlBuff.append("<unit__std><![CDATA["+ unitStd +"]]></unit__std>");
				xmlBuff.append("<unit__rate><![CDATA["+ unitStd +"]]></unit__rate>");
				xmlBuff.append("<rate__clg><![CDATA["+ rateClg +"]]></rate__clg>");
				xmlBuff.append("<rate__clg><![CDATA["+ rateClg +"]]></rate__clg>");
				xmlBuff.append("<discount><![CDATA["+ discount +"]]></discount>");
				xmlBuff.append("<quantity><![CDATA["+ quantity +"]]></quantity>");
				xmlBuff.append("<quantity__stduom><![CDATA["+ qtyStdUom +"]]></quantity__stduom>");
				xmlBuff.append("<conv__qty_stduom><![CDATA["+ convQtyStdUom +"]]></conv__qty_stduom>");
				xmlBuff.append("<rate><![CDATA["+ rateStdUom +"]]></rate>");
				xmlBuff.append("<rate__stduom><![CDATA["+ rateStdUom +"]]></rate__stduom>");
				xmlBuff.append("<conv__rtuom_stduom><![CDATA[1]]></conv__rtuom_stduom>");
				xmlBuff.append("<grade><![CDATA["+ grade +"]]></grade>");
				xmlBuff.append("<net_weight><![CDATA["+ netWt +"]]></net_weight>");
				xmlBuff.append("<gross_weight><![CDATA["+ grossWt +"]]></gross_weight>");
				xmlBuff.append("<tare_weight><![CDATA["+ tareWt +"]]></tare_weight>");
				xmlBuff.append("<potency_perc><![CDATA["+ potencyPerc +"]]></potency_perc>");
				xmlBuff.append("<batch_no><![CDATA["+ batchNo +"]]></batch_no>");
				xmlBuff.append("<supp_code__mnfr><![CDATA["+ suppCodemnfr +"]]></supp_code__mnfr>");
				xmlBuff.append("<pack_instr><![CDATA["+ packInstr +"]]></pack_instr>");
				xmlBuff.append("<dimension><![CDATA["+ dimension +"]]></dimension>");
				xmlBuff.append("<pallet_wt><![CDATA["+ palletWt +"]]></pallet_wt>");
				xmlBuff.append("<loc_code><![CDATA["+ sitelocCode +"]]></loc_code>");
				xmlBuff.append("<lot_no><![CDATA["+ lotNo +"]]></lot_no>");
				xmlBuff.append("<lot_sl><![CDATA["+ lotSl +"]]></lot_sl>");
				xmlBuff.append("<tax_class><![CDATA["+ taxClass +"]]></tax_class>");				
				//Pavan Rane 26jul19 start [tax chap to be set based on despatch in CP Auto-GRN]
				xmlBuff.append("<tax_chap><![CDATA["+ taxChapDesp +"]]></tax_chap>");											
				//Pavan Rane 26jul19 end [tax chap to be set based on despatch in CP Auto-GRN]				
				xmlBuff.append("<tax_env><![CDATA["+ taxEnv +"]]></tax_env>");
				xmlBuff.append("<pack_code><![CDATA["+ packCode +"]]></pack_code>");

				//Changes done by mayur on 29-JAN-2018------[START]
				if(mfgDate!= null)
				{
					xmlBuff.append("<mfg_date><![CDATA["+ sdf.format(mfgDate) +"]]></mfg_date>");
				}			
				//xmlBuff.append("<mfg_date><![CDATA["+ sdf.format(mfgDate) +"]]></mfg_date>");

				if(expDate!= null)
				{
					xmlBuff.append("<expiry_date><![CDATA["+ sdf.format(expDate) +"]]></expiry_date>");
				}						
				//xmlBuff.append("<expiry_date><![CDATA["+ sdf.format(expDate) +"]]></expiry_date>");
				//Changes done by mayur on 29-JAN-2018------[END]


				xmlBuff.append("<site_code__mfg><![CDATA["+ siteMfg +"]]></site_code__mfg>");			              						
				xmlBuff.append("<supp_code__mnfr><![CDATA["+ suppCodemnfr +"]]></supp_code__mnfr>");
				xmlBuff.append("<no_art><![CDATA["+ noArt +"]]></no_art>");
				xmlBuff.append("<reas_code><![CDATA["+ defReasCode +"]]></reas_code>");

				//Changes done by mayur on 26-FEB-2018------[START]
				if(retestDate!= null)
				{
					xmlBuff.append("<retest_date><![CDATA["+ sdf.format(retestDate) +"]]></retest_date>");
				}
				//Changes done by mayur on 26-FEB-2018------[END]		
				//xmlBuff.append("<retest_date><![CDATA["+ sdf.format(retestDate) +"]]></retest_date>");
				xmlBuff.append("<additional_cost><![CDATA["+ additionalCost +"]]></additional_cost>");
				xmlBuff.append("<realised_qty><![CDATA["+ quantity +"]]></realised_qty>");
				xmlBuff.append("<supp_challan_qty><![CDATA["+ quantity +"]]></supp_challan_qty>");
				xmlBuff.append("<excess_short_qty><![CDATA[0]]></excess_short_qty>");
				xmlBuff.append("<acct_code__dr><![CDATA["+ acctCodeDr +"]]></acct_code__dr>");
				xmlBuff.append("<cctr_code__dr><![CDATA["+ cctrCodeDr+"]]></cctr_code__dr>");
				xmlBuff.append("<acct_code__cr><![CDATA["+ acctCodeCr +"]]></acct_code__cr>");
				xmlBuff.append("<cctr_code__cr><![CDATA["+ cctrCodeCr +"]]></cctr_code__cr>");
				if("0".equalsIgnoreCase(stkOpt))
				{
					xmlBuff.append("<effect_stock><![CDATA[N]]></effect_stock>");
				}
				else
				{
					xmlBuff.append("<effect_stock><![CDATA[Y]]></effect_stock>");
				}
				PendingQty=ordQty - (dlvQty + quantity + prevQty);
				custPord="";
				if("A".equalsIgnoreCase(cancBoMode))
				{
					if(ordQty>0)
					{
						if((PendingQty/ordQty)*100<=ordPerc)
						{
							xmlBuff.append("<canc_bo><![CDATA[Y]]></canc_bo>");
						}
						else
						{
							xmlBuff.append("<canc_bo><![CDATA[N]]></canc_bo>");
						}

					}
				}
				else if("M".equalsIgnoreCase(cancBoMode))
				{
					xmlBuff.append("<canc_bo><![CDATA[N]]></canc_bo>");
				}
				//added  by Manish Mhatre 16oct19 start [to set cust_code__end in porcpdet]
				xmlBuff.append("<cust_code__end><![CDATA[" + custCodeEnd + "]]></cust_code__end>");
				//added  by Manish Mhatre 16oct19 end [to set cust_code__end in porcpdet]
				xmlBuff.append("</Detail2>");

			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;
			xmlBuff.append("</Header0>");
			xmlBuff.append("</group0>");
			xmlBuff.append("</DocumentRoot>");
			xmlString = xmlBuff.toString();
			System.out.println("XmlString:::["+xmlBuff.toString()+"]");
			retString = saveData(siteCodeCh,xmlString,userId,connCP);
			System.out.println("XmlString:::["+retString+"]");
			if (retString.indexOf("Success") > -1)
			{
				//System.out.println("@@@@@@3: retString from Purchase Receipt"+retString);
				String[] arrayForTranId = retString.split("<TranID>");
				int endIndex = arrayForTranId[1].indexOf("</TranID>");
				String tranIdFoPoRcp = arrayForTranId[1].substring(0,endIndex);
				//System.out.println("-tranIdFoPoRcp-"+tranIdFoPoRcp);
				//Added by Pavan R on 06/Feb/2K18[Start] to update the total additional cost to porcp 
				sql = "update porcp set total_additional_cost = ? where tran_id = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setDouble(1, totaddnlost);
				pstmt.setString(2, tranIdFoPoRcp);
				int cntPoRcp = pstmt.executeUpdate();					
				pstmt.close();
				pstmt=null;
				//Added by Pavan R on 06/Feb/2K18[End]
				//retString="";//Modified by Anjali R. on[12/11/2018][To return success string in case of success]
			}
			//	System.out.println("--XML CREATION --");
			//}
		}catch(Exception e)
		{
			e.printStackTrace();
			retString=e.getMessage();
			try
			{
				conn.rollback();
			} catch (SQLException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			throw new ITMException(e);
		}
		return retString;
	}
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
	private String saveData(String siteCode,String xmlString,String userId, Connection conn) throws ITMException
	{
		//System.out.println("saving data...........");
		InitialContext ctx = null;
		String retString = null;
		MasterStatefulLocal masterStateful = null; // for ejb3
		try
		{
			AppConnectParm appConnect = new AppConnectParm();
			ctx = new InitialContext(appConnect.getProperty());
			masterStateful = (MasterStatefulLocal)ctx.lookup("ibase/MasterStatefulEJB/local");
			//System.out.println("-----------masterStateful------- " + masterStateful);
			String [] authencate = new String[2];
			authencate[0] = userId;
			authencate[1] = "";
			//System.out.println("xmlString to masterstateful [" + xmlString + "]");
			//Changed By Nasruddin Start 04-11-16
			long startTime2 = System.currentTimeMillis();
			retString = masterStateful.processRequest(authencate, siteCode, true, xmlString,true,conn);
			long endTime2 = System.currentTimeMillis();
			System.out.println("DIFFERANCE IN TIME processRequest DATA IN SECONDS INSIDE saveData():::["+(endTime2-startTime2)/1000+"]");
			//Changed By Nasruddin END 04-11-16
			System.out.println("--retString - -"+retString);
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
	
	public String prdSchemeTraceUpd(String invoiceId,String xtraParams,Connection conn) throws ITMException
	{
		String retString="",sql="";
		PreparedStatement pstmt=null,pstmt1=null;
		ResultSet rs=null,rs1=null;
		String itemCdParent="",itemCode="";
		int cntItem=0;
		String sordNo="",soLineNo="",custCode="";
		Timestamp tranDate=null,effFrom=null,validUpto=null,sysdate=null;
		double rate=0,quantity=0;
		double totChargeQty=0,totFreeQty=quantity,totBonusQty=0,totSampleQty=0,effNetAmt=0;
		int cntSchTrace=0;
		String orderType="",siteCode="",stateDlv="",nature="",countCodeDlv="",priceList="",schemeCode="",chgUser="",chgTerm="";
		int rowUpdate=0;
		String stdSoPL="";
		try
		{
			GenericUtility genericUtility = GenericUtility.getInstance();
			SimpleDateFormat sdf=new SimpleDateFormat(genericUtility.getApplDateFormat());
			Date currentDateval = new Date();
			sysdate=new Timestamp(currentDateval.getTime());
			chgUser = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
			chgTerm = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId"));
			sql="select tran_date, cust_code from invoice "
					+ " where invoice_id = ?";
			
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1,invoiceId);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				tranDate=rs.getTimestamp("tran_date");
				custCode=rs.getString("cust_code");
			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;
			sql=" select a.sord_line_no,	a.sord_no,	a.item_code,a.line_type, "
					+ "	 sum(a.quantity__stduom) as quantity, sum(a.quantity__stduom * a.rate__stduom) as effNetAmt, b.item_code__parent "
					+ " from invoice_trace a, item b "
					+ " where  a.item_code = b.item_code "
					+ " and b.item_code__parent is not null "  // 06-Dec-16 manoharan unnecessary all items are considered
					+ " and a.invoice_id = ? "
					+" and a.line_type  not in ('I','V')  " // Added By PriyankaC on 09OCt2018.
					+ "	group by a.sord_no, a.sord_line_no, a.item_code, a.item_code__ord, b.item_code__parent, a.line_type "
					+ " order by a.sord_no, a.sord_line_no ";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1,invoiceId);
			rs=pstmt.executeQuery();
			while(rs.next())
			{
				itemCdParent=checkNull(rs.getString("item_code__parent"));
				itemCode=checkNull(rs.getString("item_code"));
				sordNo=checkNull(rs.getString("sord_no"));
				soLineNo=checkNull(rs.getString("sord_line_no"));
				quantity=rs.getDouble("quantity");
				effNetAmt=rs.getDouble("effNetAmt");
				if((itemCdParent.trim()).length()==0)
				{
					sql="select count(1) from item where item_code__parent = ?";
					pstmt1=conn.prepareStatement(sql);
					pstmt1.setString(1, itemCode);
					rs1=pstmt1.executeQuery();
					if(rs1.next())
					{
						cntItem=rs1.getInt(1);
					}
					rs1.close();
					rs1=null;
					pstmt1.close();
					pstmt1=null;
					if(cntItem>0)
					{
						itemCdParent=itemCode;
					}
					if((itemCdParent.trim()).length()==0)
					{
						continue;
					}
				}
				
				sql=" select a.order_type, a.site_code, a.state_code__dlv, b.nature, a.count_code__dlv, a.price_list "
						+ "  from sorder a,sorddet b "
						+ " where a.sale_order = b.sale_order "
						+ " and b.sale_order= ? "
						+ " and b.line_no= ?"; 
				pstmt1=conn.prepareStatement(sql);
				pstmt1.setString(1,sordNo);
				pstmt1.setString(2,soLineNo);
				rs1=pstmt1.executeQuery();
				if(rs1.next())
				{
					orderType=checkNull(rs1.getString("order_type"));
					siteCode=checkNull(rs1.getString("site_code"));
					stateDlv=checkNull(rs1.getString("state_code__dlv"));
					nature=checkNull(rs1.getString("nature"));
					countCodeDlv=checkNull(rs1.getString("count_code__dlv"));
					priceList=checkNull(rs1.getString("price_list"));
				}
				rs1.close();
				rs1=null;
				pstmt1.close();
				pstmt1=null;
				schemeCode = checkScheme(itemCdParent,orderType, custCode,siteCode,stateDlv,countCodeDlv,tranDate,conn);
				String temp=checkNull(schemeCode);
				System.out.println("schemeCode:::::::::::::::::"+schemeCode);	
				if(schemeCode==null ||  temp.trim().length()==0)
				{
					continue;
				}
				if(schemeCode !=null && (schemeCode.trim()).length()>0)
				{
					sql="select app_from, valid_upto "
							+ "	from scheme_applicability where scheme_code = ?";
					pstmt1=conn.prepareStatement(sql);
					pstmt1.setString(1,schemeCode);
					rs1=pstmt1.executeQuery();
					if(rs1.next())
					{
						effFrom=rs1.getTimestamp(1);
						validUpto=rs1.getTimestamp(2);
					}
					rs1.close();
					rs1=null;
					pstmt1.close();
					pstmt1=null;
					if(priceList.trim().length()>0)
					{
						rate=distCommon.pickRate(priceList, sdf.format(tranDate), itemCdParent, "", "L", quantity, conn);	
					}
					if(rate<=0)
					{
						stdSoPL=distCommon.getDisparams("999999", "STD_SO_PL", conn);
						if(!"NULLFOUND".equalsIgnoreCase(stdSoPL) && (stdSoPL.trim()).length()>0)
						{
							
						}
						rate=distCommon.pickRate(stdSoPL, sdf.format(tranDate), itemCdParent, "", "L", quantity, conn);	
					}
					}
				
				totChargeQty=0;
				totFreeQty=0;
				totBonusQty=0;
				totSampleQty=0;
				if("F".equalsIgnoreCase(nature))
				{
					totChargeQty=0;
					totFreeQty=quantity;
					totBonusQty=0;
					totSampleQty=0;
				} else if("B".equalsIgnoreCase(nature))
				{
					totChargeQty=0;
					totBonusQty=quantity;
					totFreeQty=0;
					totSampleQty=0;
				}else if("S".equalsIgnoreCase(nature))
				{
					totChargeQty=0;
					totBonusQty=0;
					totFreeQty=0;
					totSampleQty=quantity;
				}
				else
				{
					totChargeQty=quantity;
					totBonusQty=0;
					totFreeQty=0;
					totSampleQty=0;
				}
				sql=" select count(1) from prd_scheme_trace "
						+ " where site_code= 	? and cust_code	=	? "
						+ " and item_code	=	? and scheme_code=	? ";
				pstmt1=conn.prepareStatement(sql);
				pstmt1.setString(1,siteCode);
				pstmt1.setString(2,custCode);
				pstmt1.setString(3,itemCdParent);
				pstmt1.setString(4,schemeCode);
				rs1=pstmt1.executeQuery();
				if(rs1.next())
				{
					cntSchTrace=rs1.getInt(1);	
				}
				rs1.close();
				rs1=null;
				pstmt1.close();
				pstmt1=null;
				if(cntSchTrace==0)
				{
					sql="insert into prd_scheme_trace(	site_code, cust_code, scheme_code, item_code, "
							+ " eff_from, valid_upto, tot_charge_qty, tot_free_qty, "
							+ " eff_net_amount, chg_date, chg_user, chg_term,rate,tot_bonus_qty,tot_sample_qty) "
							+ " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
					pstmt1=conn.prepareStatement(sql);
					pstmt1.setString(1,siteCode);
					pstmt1.setString(2,custCode);
					pstmt1.setString(3,schemeCode);
					pstmt1.setString(4,itemCdParent);
					pstmt1.setTimestamp(5,effFrom);
					pstmt1.setTimestamp(6,validUpto);
					pstmt1.setDouble(7,totChargeQty);
					pstmt1.setDouble(8,totFreeQty);
					pstmt1.setDouble(9,effNetAmt);
					pstmt1.setTimestamp(10,sysdate);
					pstmt1.setString(11,chgUser);
					pstmt1.setString(12,chgTerm);
					pstmt1.setDouble(13,rate);
					pstmt1.setDouble(14,totBonusQty);
					pstmt1.setDouble(15,totSampleQty);
					
					rowUpdate=pstmt1.executeUpdate();
					
					
				}
				else
				{
					sql=" update prd_scheme_trace "
							+ " set tot_charge_qty =tot_charge_qty+?,"
							+ "tot_free_qty	= tot_free_qty+?,"
							+ "eff_net_amount	= eff_net_amount+?,"
							+ "eff_from			= ?,"
							+ "valid_upto 		= ?,"
							+ "chg_date			= ?,"
							+ "chg_user			= ?,"
							+ "chg_term			= ?,"
							+ "tot_bonus_qty  =tot_bonus_qty+?,"
							+ "tot_sample_qty  = tot_sample_qty+? "
							+ "	where site_code= 	?"
							+ " and cust_code	=	? "
							+ " and item_code	=	? "
							+ " and scheme_code=	?";
					
					pstmt1=conn.prepareStatement(sql);
					pstmt1.setDouble(1,totChargeQty);
					pstmt1.setDouble(2,totFreeQty);
					pstmt1.setDouble(3,effNetAmt);
					pstmt1.setTimestamp(4,effFrom);
					pstmt1.setTimestamp(5,validUpto);
					pstmt1.setTimestamp(6,sysdate);
					pstmt1.setString(7,chgUser);
					pstmt1.setString(8,chgTerm);
					pstmt1.setDouble(9,totBonusQty);
					pstmt1.setDouble(10,totSampleQty);
					pstmt1.setString(11,siteCode);
					pstmt1.setString(12,custCode);
					pstmt1.setString(13,itemCdParent);
					pstmt1.setString(14,schemeCode);
					rowUpdate=pstmt1.executeUpdate();
				}
				
			}	
			
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;
			
					

			
		}catch(Exception e)
		{
			e.printStackTrace();
			// retString=e.toString();
			throw new ITMException(e);
		}
		return retString;
	}
	
	public String checkScheme(String itemCdParent,String orderType,String custCode,String siteCode,String stateDlv,String countCodeDlv,Timestamp tranDate,Connection conn) throws ITMException
	{
		String schemeCode="";
		String sql="";
		PreparedStatement pstmt=null,pstmt1=null;
		ResultSet rs=null,rs1=null;
		String applyCustList="",noapplyCustList="",appOrderTypes="",prevSchemeCode="";
		boolean toProceed=false;
		String retString="";
		try
		{
			sql=" select a.scheme_code  from scheme_applicability a,scheme_applicability_det  b"
					+ " where a.scheme_code	= b.scheme_code and a.item_code 		= ? "
					+ " and a.app_from 			<= ? "
					+ " and a.valid_upto 		>= ? "
					+ " and (b.site_code 		= ? or b.state_code 		= ?  or b.count_code 		= ?)";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, itemCdParent);
			pstmt.setTimestamp(2, tranDate);
			pstmt.setTimestamp(3, tranDate);
			pstmt.setString(4,siteCode);
			pstmt.setString(5,stateDlv);
			pstmt.setString(6,countCodeDlv);
			rs=pstmt.executeQuery();
			while(rs.next())
			{
				schemeCode=rs.getString(1);
				retString=schemeCode;
				sql="select (case when apply_cust_list is null then ' ' else apply_cust_list end), "
						+ " (case when noapply_cust_list is null then ' ' else noapply_cust_list end), "
						+ " order_type from scheme_applicability where scheme_code = ?";
				pstmt1=conn.prepareStatement(sql);
				pstmt1.setString(1,schemeCode);
				rs1=pstmt1.executeQuery();
				
				while(rs1.next())
				{
					applyCustList=rs1.getString(1);
					noapplyCustList=rs1.getString(2);
					appOrderTypes=checkNull(rs1.getString(3));
					if("NE".equalsIgnoreCase(orderType) && (appOrderTypes.trim()).length()==0)
					{
						continue;
					}
					else if((appOrderTypes.trim()).length()>0)
					{
						String ordTypeArr[]=appOrderTypes.split(",");
						ArrayList <String>ordTypeList=new ArrayList<String>(Arrays.asList(ordTypeArr));
						if(ordTypeList.contains(orderType))
						{
							toProceed=true;
							break;
						}
						else
						{
							retString="";
						}
						
					}
						
				}
				rs1.close();
				rs1=null;
				pstmt1.close();
				pstmt1=null;
				if(toProceed)
				{
					
					if(applyCustList.trim().length()>0)
					{
						String appCustArr[]=applyCustList.split(",");
						ArrayList <String>appCustList=new ArrayList<String>(Arrays.asList(appCustArr));
						if(appCustList.contains(custCode))
						{
							retString=schemeCode;
							break;
						}
					}
					if(noapplyCustList.trim().length()>0)
					{
						String noappCustArr[]=noapplyCustList.split(",");
						ArrayList <String>noappCustList=new ArrayList<String>(Arrays.asList(noappCustArr));
						if(noappCustList.contains(custCode))
						{
							retString="";
							break;
						}
					}
				}
			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;
			
		}catch(Exception e)
		{
			e.printStackTrace();
			// retString=e.toString();
			throw new ITMException(e);
		}
		return retString;
		
	}
	
	public String finSchemeInvAdj(String siteCode,String itemSer,String custCode,String invoiceId,double drAmt,boolean abDrCr,boolean abAdv,Connection conn) throws ITMException
    {
        String retString="";
        PreparedStatement pstmt=null,pstmt1=null;
        ResultSet rs=null;
        String sql="",invType="",saleOrder="",ordTypeNewPrd="",finScheme="",tranSer="",refNo="",tranId="",generatedId="";
        Timestamp tranDateTS=null;
        String ignoreDays="";
        int intignoreDays=0;
        String stat="";
        double netAmt=0;
        java.sql.Date refDate=null;
        double totAmt=0,adjAmt=0,diffAmt=0;
        try
        {


            netAmt=drAmt;
            ignoreDays=finCommon.getFinparams("999999", "IGNORE_DR_DAYS", conn);
            ordTypeNewPrd=finCommon.getFinparams("999999", "ORD_TYPE_NEWPRD", conn);

            if(ignoreDays==null ||"NULLFOUND".equalsIgnoreCase(ignoreDays) )
            {
                ignoreDays="9999999";
            }
            intignoreDays=Integer.parseInt(ignoreDays);
            sql="select tran_date, inv_type,sale_order    from invoice where invoice_id = ?";
            pstmt=conn.prepareStatement(sql);
            pstmt.setString(1,invoiceId);
            rs=pstmt.executeQuery();
			// 28-Nov-16 manoharan
            //while(rs.next())
			if(rs.next())
            {
                //tranDateTS=rs.getTimestamp(1);
                refDate=rs.getDate(1);
                invType=rs.getString(2);
                saleOrder=rs.getString(3);

            }
            rs.close();
            rs=null;
            pstmt.close();
            pstmt=null;

            sql="select fin_scheme from sorder where sale_order=?";
            pstmt=conn.prepareStatement(sql);
            pstmt.setString(1,saleOrder);
            rs=pstmt.executeQuery();
            if(rs.next())
            {
                finScheme=rs.getString(1);
            }
            rs.close();
            rs=null;
            pstmt.close();
            pstmt=null;
            if(abDrCr)
            {
                sql="select tran_id, tran_ser, ref_no, tot_amt, adj_amt from receivables"
                        + " where (tran_ser = 'DRNRCP' or tran_ser = 'MDRCRD') "
                        + " and ( ((ref_type = 'FC' or ref_type = ? ) "
                        + " and ( (? - ref_date ) >= ?  )) "
                        + " OR (ref_type <> 'FC' and ref_type <> ?) ) "
                        + "  and (tot_amt - adj_amt) > 0 "
                        + " and site_code = ? "
                        + "  and item_ser  = ? "
                        + " and cust_code = ? "
                        + " and fin_scheme = ? "
                        + " order by tran_id ";
                pstmt=conn.prepareStatement(sql);
                pstmt.setString(1,ordTypeNewPrd);
                pstmt.setDate(2,refDate);
                pstmt.setInt(3, intignoreDays);
                pstmt.setString(4,ordTypeNewPrd);
                pstmt.setString(5,siteCode);
                pstmt.setString(6,itemSer);
                pstmt.setString(7,custCode);
                pstmt.setString(8,finScheme);
                rs=pstmt.executeQuery();
                while(rs.next())
                {
                    totAmt=rs.getDouble("tot_amt");
                    adjAmt=rs.getDouble("adj_amt");
                    tranSer=rs.getString("tran_ser");
                    refNo=rs.getString("ref_no");
                    tranId=rs.getString("tran_id");
                    diffAmt=totAmt-adjAmt;
                    drAmt=drAmt+diffAmt;
                    generatedId=generateId(siteCode,conn);
                    sql="insert into receivables_adj "
                            + " (tran_id, ref_ser, ref_no, tot_amt, adj_amt, net_amt,"
                            + "ref_ser_adj,ref_no_adj, tran_id__rcv) "
                            + " values "
                            + "(?,?,?,?,?,?,?,?,?)";
                    pstmt1=conn.prepareStatement(sql);
                    pstmt1.setString(1,generatedId);
                    pstmt1.setString(2,tranSer);
                    pstmt1.setString(3,refNo);
                    pstmt1.setDouble(4,diffAmt);
                    pstmt1.setDouble(5,diffAmt);
                    pstmt1.setDouble(6,0);
                    pstmt1.setString(7,"S-INV");
                    pstmt1.setString(8,invoiceId);
                    pstmt1.setString(9,tranId);
                    pstmt1.executeUpdate();
                    pstmt1.close();
                    pstmt1=null;
                }
                rs.close();
                rs=null;
                pstmt.close();
                pstmt=null;
            }
                tranSer="";
                diffAmt=0;
                if(abDrCr && abAdv)
                {
                    tranSer="'CRNRCP','MDRCRC', 'R-ADV'";
                }
                else if(abDrCr && !abAdv)
                {
                    tranSer="'CRNRCP', 'MDRCRC'";
                }
                else if(!abDrCr)
                {
                    tranSer="'R-ADV'";
                }
                sql="select tran_id, tran_ser, ref_no, tot_amt, adj_amt from receivables "
                    +"    where ((tran_ser in (" +tranSer+ ")) and (tot_amt - adj_amt) < 0 and fin_scheme =  '"+finScheme +"' )    "
                    +"  and ((site_code = ?  and item_ser  = ?  and cust_code = ?) "
                    +"          OR  ( site_code = ?  and cust_code = ?)) order by tran_id ";
                pstmt=conn.prepareStatement(sql);
                pstmt.setString(1,siteCode);
                pstmt.setString(2,itemSer);
                pstmt.setString(3,custCode);
                pstmt.setString(4,siteCode);
                pstmt.setString(5,custCode);
                rs=pstmt.executeQuery();
                while(rs.next())
                {
                    totAmt=rs.getDouble("tot_amt");
                    adjAmt=rs.getDouble("adj_amt");
                    tranSer=rs.getString("tran_ser");
                    refNo=rs.getString("ref_no");
                    tranId=rs.getString("tran_id");
                    if(drAmt==0)
                    {
                        break;
                    }
                    else
                    {
                        diffAmt=totAmt-adjAmt;
                        if(drAmt>=Math.abs(diffAmt))
                        {
                            drAmt=drAmt-Math.abs(diffAmt);
                            adjAmt=diffAmt;
                            stat="A";
                        }
                        else
                        {
                            adjAmt=-1*drAmt;
                            drAmt=0;
                            stat="P";
                        }

                        generatedId=generateId(siteCode,conn);
                        sql="insert into receivables_adj "
                                + " (tran_id, ref_ser, ref_no, tot_amt, adj_amt, net_amt,ref_ser_adj,ref_no_adj, tran_id__rcv) "
                                + " values("
                                + " ?, ?, ?,?, ?, ?,?, ? ,?)";
                                pstmt1=conn.prepareStatement(sql);
                        pstmt1.setString(1,generatedId);
                        pstmt1.setString(2,tranSer);
                        pstmt1.setString(3,refNo);
                        pstmt1.setDouble(4,diffAmt);
                        pstmt1.setDouble(5,adjAmt);
                        pstmt1.setDouble(6,(diffAmt-adjAmt));
                        pstmt1.setString(7,"S-INV");
                        pstmt1.setString(8,invoiceId);
                        pstmt1.setString(9,tranId);
                        pstmt1.executeUpdate();
                        pstmt1.close();
                        pstmt1=null;
                    }
                }
                rs.close();
                rs=null;
                pstmt.close();
                pstmt=null;
                generatedId=generateId(siteCode,conn);
                sql="insert into receivables_adj "
                        + " (tran_id, ref_ser, ref_no, tot_amt, adj_amt, net_amt,ref_ser_adj,ref_no_adj, tran_id__rcv) "
                        + " values("
                        + " ?, ?, ?,?, ?, ?,?, ? ,?)";
                        pstmt1=conn.prepareStatement(sql);
                        pstmt1.setString(1,generatedId);
                        pstmt1.setString(2,"S-INV");
                        pstmt1.setString(3,invoiceId);
                        pstmt1.setDouble(4,netAmt);
                        pstmt1.setDouble(5,-1*(drAmt-netAmt));
						pstmt1.setDouble(6,netAmt-(-1*(drAmt-netAmt)));
                        pstmt1.setString(7," ");
                        pstmt1.setString(8," ");
                        pstmt1.setString(9," ");
                        pstmt1.executeUpdate();
                        pstmt1.close();
                        pstmt1=null;

                        pstmt1=conn.prepareStatement("update invoice set adj_amount = ((? - ?) * -1)"
                                + " where invoice_id = ?");
                        pstmt1.setDouble(1,drAmt);
                        pstmt1.setDouble(2,netAmt);
                        pstmt1.setString(3,invoiceId);
                        pstmt1.executeUpdate();
                        pstmt1.close();
                        pstmt1=null;

        }catch(Exception e)
        {
            e.printStackTrace();
            // retString=e.toString();
            throw new ITMException(e);
        }
        return retString;
    }
	
	public String generateId(String siteCode,Connection conn) throws ITMException
	{
	String sql="";
	PreparedStatement pstmt=null;
	ResultSet rs=null;
	String xmlValues="",nextID="",keystr="";

	try
	{
		sql ="select key_String from transetup "
		+ "where upper(tran_window) = 'W_REC_ADJ' " ;
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
			sql ="select key_String from transetup "
			+ "where upper(tran_window) = 'GENERAL' " ;
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
		xmlValues = xmlValues +"</Detail1></Root>";
		//System.out.println("xmlValues :["+xmlValues+"]");
		TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", CommonConstants.DB_NAME);
		nextID = tg.generateTranSeqID("R-ADJ", "tran_id", keystr, conn);
		//System.out.println("nextID ["+nextID + "]");

	}catch(Exception e)
	{
		e.printStackTrace();
		throw new ITMException(e);
	}
	return nextID;
	}
	//gbf_scheme_disc_trace(minvoiceid)
	public String schemeDiscTrace(String invoiceId,String xtraParams,Connection conn) throws ITMException
    {
        PreparedStatement pstmt=null,pstmt1=null,pstmt2=null;
        ResultSet rs=null,rs1=null,rs2=null;
        String retString="",sql="";
        String siteCode="",custCode="",promoTerm="",discType="";
        String sordNo="",sordLineNo="",despId="",despLineNo="",itemCode="",schemeCode="",appFrom="",tranId="";
        String chgUser="",chgTerm="";
        double quantityStduom=0.0,effNetAmount=0.0,totNetAmt=0.0,discPerc=0.0;
        String schemeCodeHdr="";
        double discSchemeOffinvAmt=0.0,discSchemeBillbackAmt=0.0,ordQty=0.0,rateStd=0.0,rateStduom=0.0;
        double discRate=0.0,cashValueItem=0.0;
        double totBBAmtHdr=0.0,totOffinvAmtHdr=0.0;
        double offinvAmtHdr=0.0,billbackAmtHdr=0.0;
        int cnt=0,hdrCnt=0, schemeCount = 0;
        Timestamp validUpto=null,invoiceDate=null,effFrom=null;
        Timestamp sysDate = null;
        E12GenericUtility genericUtility= new  E12GenericUtility();
		String prevSordNo = "";
        try
        {
            
            Calendar currentDate = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
            String sysDateStr = sdf.format(currentDate.getTime());
           // System.out.println("Now the date is :=>  " + sysDateStr);
            sysDate = Timestamp.valueOf(genericUtility.getValidDateString(sysDateStr, genericUtility.getApplDateFormat(),
                    genericUtility.getDBDateFormat()) + " 00:00:00.0");
            chgUser = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
            chgTerm = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId"));
            
            sql="select site_code,cust_code from invoice where invoice_id = ?";
            pstmt1=conn.prepareStatement(sql);
            pstmt1.setString(1,invoiceId);
            rs1=pstmt1.executeQuery();
            if(rs1.next())
            {
                siteCode=checkNull(rs1.getString("site_code"));
                custCode=checkNull(rs1.getString("cust_code"));    
            }
            rs1.close();
            rs1=null;
            pstmt1.close();
            pstmt1=null;
            
            sql="select sord_no,sord_line_no,desp_id,desp_line_no,item_code," +
                    "quantity__stduom, (quantity__stduom * rate__stduom) as eff_net_amount " +
                    " from Invoice_trace where invoice_id = ?";
            pstmt1=conn.prepareStatement(sql);
            pstmt1.setString(1,invoiceId);
            rs1=pstmt1.executeQuery();
            while(rs1.next())
            {
                sordNo=checkNull(rs1.getString("sord_no"));
				if (!prevSordNo.trim().equals(sordNo.trim()))
				{
					schemeCount = 0;
					sql="select count(1)  from sorderdet_scheme  where tran_id = ?";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1,sordNo);
					rs=pstmt.executeQuery();
					if(rs.next())
					{	
						schemeCount = rs.getInt(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					prevSordNo = sordNo;
					
				}
                sordLineNo=checkNull(rs1.getString("sord_line_no"));
                despId=checkNull(rs1.getString("desp_id"));
                despLineNo=checkNull(rs1.getString("desp_line_no"));
                itemCode=checkNull(rs1.getString("item_code"));
                quantityStduom=rs1.getDouble("quantity__stduom");
                effNetAmount=rs1.getDouble("eff_net_amount");
                totNetAmt = totNetAmt+effNetAmount;
                
                if (schemeCount == 0)
				{
					continue;
				}
                sql="select scheme_code  from sorderdet_scheme  where tran_id = ? and line_no_form = ?";
                pstmt=conn.prepareStatement(sql);
                pstmt.setString(1,sordNo);
                pstmt.setString(2,sordLineNo);
                rs=pstmt.executeQuery();
                while(rs.next())
                {
                    schemeCode=checkNull(rs.getString("scheme_code"));
                    if(schemeCode !=null && schemeCode.trim().length()>0)
                    {
                        
                        sql="select count(*)  from scheme_discount_trace where site_code= ? and cust_code    =? " +
                                " and item_code    = ? and sale_order = ? and sord_line_no = ? and desp_id = ? " +
                                " and desp_line_no = ? AND scheme_code = ?";
                        pstmt2=conn.prepareStatement(sql);
                        pstmt2.setString(1,siteCode);
                        pstmt2.setString(2,custCode);
                        pstmt2.setString(3,itemCode);
                        pstmt2.setString(4,sordNo);
                        pstmt2.setString(5,sordLineNo);
                        pstmt2.setString(6,despId);
                        pstmt2.setString(7,despLineNo);
                        pstmt2.setString(8,schemeCode);
                        rs2=pstmt2.executeQuery();
                        if(rs2.next())
                        {
                            cnt = rs2.getInt(1);
                        }
                        rs2.close();
                        rs2=null;
                        pstmt2.close();
                        pstmt2=null;
                        
                        sql = "select app_from, valid_upto from scheme_applicability where scheme_code =  ?";
                        pstmt2 = conn.prepareStatement(sql);
                        pstmt2.setString(1, schemeCode);
                        rs2 = pstmt2.executeQuery();
                        if (rs2.next())
                        {
                            effFrom=rs2.getTimestamp("app_from");
                            validUpto= rs2.getTimestamp("valid_upto");
                            
                        }
                        rs2.close();
                        rs2 = null;
                        pstmt2.close();
                        pstmt2 = null;
                        
                        sql = "select promo_term,(case when  disc_perc is null then 0 else disc_perc end)  as disc_perc ,"
                                + "disc_type  from bom where bom_code =   ?";
                        pstmt2 = conn.prepareStatement(sql);
                        pstmt2.setString(1, schemeCode);
                        rs2 = pstmt2.executeQuery();
                        if (rs2.next())
                        {
                            promoTerm=checkNull(rs2.getString("promo_term"));
                            discPerc= rs2.getDouble("disc_perc");
                            discType=checkNull(rs2.getString("disc_type"));
                            
                        }
                        rs2.close();
                        rs2 = null;
                        pstmt2.close();
                        pstmt2 = null;
                        
                        discSchemeOffinvAmt=0.0;
                        discSchemeBillbackAmt=0.0;
                        
                        
                      //  System.out.println("@V@ disc type :- ["+discType+"]");
                        
                        if("F".equalsIgnoreCase(discType))
                        {
                            sql = "select sum(qty_ord) as ord_qty from sorditem where sale_order =  ? and line_no = ?";
                            pstmt2 = conn.prepareStatement(sql);
                            pstmt2.setString(1, sordNo);
                            pstmt2.setString(2, sordLineNo);
                            rs2 = pstmt2.executeQuery();
                            if (rs2.next())
                            {
                                ordQty= rs2.getDouble("ord_qty");
                                
                            }
                            rs2.close();
                            rs2 = null;
                            pstmt2.close();
                            pstmt2 = null;
                            if(quantityStduom==ordQty)
                            {
                                if("0".equalsIgnoreCase(promoTerm))
                                {
                                    discSchemeOffinvAmt=discPerc;
                                }else if("1".equalsIgnoreCase(promoTerm))
                                {
                                    discSchemeBillbackAmt=discPerc;
                                }
                            }else
                            {
                                discPerc=(quantityStduom*discPerc)/ordQty;
                                if("0".equalsIgnoreCase(promoTerm))
                                {
                                    discSchemeOffinvAmt=discPerc;
                                }else if("1".equalsIgnoreCase(promoTerm))
                                {
                                    discSchemeBillbackAmt=discPerc;
                                }
                            }
                        }
                        else if("P".equalsIgnoreCase(discType))
                        {
                            
                            sql = "select (case when  rate__std is null then 0 else rate__std end)" +
                                    " as  rate__std from despatchdet " +
                                    " where sord_no = ? and line_no__sord = ? " +
                                    " and desp_id = ? and line_no = ?";
                            pstmt2 = conn.prepareStatement(sql);
                            pstmt2.setString(1, sordNo);
                            pstmt2.setString(2, sordLineNo);
                            pstmt2.setString(3, despId);
                            pstmt2.setString(4, despLineNo);
                            rs2 = pstmt2.executeQuery();
                            if (rs2.next())
                            {
                                rateStd= rs2.getDouble("rate__std");
                                
                            }
                            rs2.close();
                            rs2 = null;
                            pstmt2.close();
                            pstmt2 = null;
                            if(rateStd==0)//need to clarify
                            {
                                sql = "select (case when  rate__stduom is null then 0 else rate__stduom end) " +
                                        "as  rate__stduom from despatchdet  where sord_no = ? and line_no__sord = ? " +
                                        " and desp_id = ? and line_no = ?";
                                pstmt2 = conn.prepareStatement(sql);
                                pstmt2.setString(1, sordNo);
                                pstmt2.setString(2, sordLineNo);
                                pstmt2.setString(3, despId);
                                pstmt2.setString(4, despLineNo);
                                rs2 = pstmt2.executeQuery();
                                if (rs2.next())
                                {
                                    rateStd= rs2.getDouble("rate__stduom");
                                    
                                }
                                rs2.close();
                                rs2 = null;
                                pstmt2.close();
                                pstmt2 = null;
                            }
                            
                            discRate=(rateStd*discPerc)/100;
                            if("0".equalsIgnoreCase(promoTerm))
                            {
                                discSchemeOffinvAmt=quantityStduom*discRate;
                            }else if("1".equalsIgnoreCase(promoTerm))
                            {
                                discSchemeBillbackAmt=quantityStduom*discRate;
                            }
                        }
                        else if("C".equalsIgnoreCase(discType))
                        {
                            sql = "select (case when  rate__std is null then 0 else rate__std end)" +
                                    " as  rate__std from despatchdet " +
                                    " where sord_no = ? and line_no__sord = ? " +
                                    " and desp_id = ? and line_no = ?";
                            pstmt2 = conn.prepareStatement(sql);
                            pstmt2.setString(1, sordNo);
                            pstmt2.setString(2, sordLineNo);
                            pstmt2.setString(3, despId);
                            pstmt2.setString(4, despLineNo);
                            rs2 = pstmt2.executeQuery();
                            if (rs2.next())
                            {
                                rateStd= rs2.getDouble("rate__std");
                                
                            }
                            rs2.close();
                            rs2 = null;
                            pstmt2.close();
                            pstmt2 = null;
                            if(rateStd==0)//need to clarify
                            {
                                sql = "select (case when  rate__stduom is null then 0 else rate__stduom end) " +
                                        "as  rate__stduom from despatchdet  where sord_no = ? and line_no__sord = ? " +
                                        " and desp_id = ? and line_no = ?";
                                pstmt2 = conn.prepareStatement(sql);
                                pstmt2.setString(1, sordNo);
                                pstmt2.setString(2, sordLineNo);
                                pstmt2.setString(3, despId);
                                pstmt2.setString(4, despLineNo);
                                rs2 = pstmt2.executeQuery();
                                if (rs2.next())
                                {
                                    rateStd= rs2.getDouble("rate__stduom");
                                    
                                }
                                rs2.close();
                                rs2 = null;
                                pstmt2.close();
                                pstmt2 = null;
                            }
                            sql = "select (case when  cash_value_item is null then 0 else cash_value_item end)  as cash_value_item " +
                                    " from bom where  bom_code  = ?";
                            pstmt2 = conn.prepareStatement(sql);
                            pstmt2.setString(1, schemeCode);
                            rs2 = pstmt2.executeQuery();
                            if (rs2.next())
                            {
                                cashValueItem= rs2.getDouble("cash_value_item");
                                
                            }
                            rs2.close();
                            rs2 = null;
                            pstmt2.close();
                            pstmt2 = null;
                            if("0".equalsIgnoreCase(promoTerm))
                            {
                                discSchemeOffinvAmt=cashValueItem*rateStd;
                            }else if("1".equalsIgnoreCase(promoTerm))
                            {
                                discSchemeBillbackAmt=cashValueItem*rateStd;
                            }
                        }
                        
                        if(cnt==0)
                        {
                            tranId=generateTranId("t_scheme_discount_trace",siteCode,conn);
                            
                            sql="insert into scheme_discount_trace(    TRAN_ID  , SITE_CODE , CUST_CODE, SCHEME_CODE,  SALE_ORDER,INVOICE_ID," +
                                    "INVOICE_DATE, DESP_ID, DISC_SCHEM_BILLBACK_AMT, DISC_SCHEM_OFFINV_AMT, EFF_FROM, VALID_UPTO," +
                                    " EFF_NET_AMOUNT, DISC_PERC, DISC_TYPE,promo_term, CHG_DATE, CHG_USER, CHG_TERM)" +
                                    "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                            pstmt2 = conn.prepareStatement(sql);
                            pstmt2.setString(1, tranId);
                            pstmt2.setString(2, siteCode);
                            pstmt2.setString(3, custCode);
                            pstmt2.setString(4, schemeCode);
                            pstmt2.setString(5, sordNo);
                            pstmt2.setString(6, invoiceId);
                            pstmt2.setTimestamp(7, invoiceDate);
                            pstmt2.setString(8, despId);
                            pstmt2.setDouble(9, discSchemeBillbackAmt);
                            pstmt2.setDouble(10, discSchemeOffinvAmt);
                            pstmt2.setTimestamp(11, effFrom);
                            pstmt2.setTimestamp(12, validUpto);
                            pstmt2.setDouble(13, effNetAmount);
                            pstmt2.setDouble(14, discPerc);
                            pstmt2.setString(15, discType);
                            pstmt2.setString(16, promoTerm);
                            pstmt2.setTimestamp(17, sysDate);
                            pstmt2.setString(18, chgUser);
                            pstmt2.setString(19, chgTerm);
                            pstmt2.executeUpdate();
                            pstmt2.close();
                            pstmt2=null;
                        }else
                        {
                            sql="update scheme_discount_trace set     DISC_SCHEM_BILLBACK_AMT = ?," +
                                    "DISC_SCHEM_OFFINV_AMT    = ?,EFF_NET_AMOUNT    = ?," +
                                    "eff_from            = ?,valid_upto         = ?, " +
                                    "chg_date            = ?,chg_user            = ?," +
                                    "chg_term            = ? where site_code=     ? " +
                                    "and cust_code    =    ? and scheme_code=    ? " +
                                    "AND invoice_id =  ? and desp_id = ? ";
                            pstmt2 = conn.prepareStatement(sql);
                            pstmt2.setDouble(1, discSchemeBillbackAmt);
                            pstmt2.setDouble(2, discSchemeOffinvAmt);
                            pstmt2.setDouble(3, effNetAmount);
                            pstmt2.setTimestamp(4, effFrom);
                            pstmt2.setTimestamp(5, validUpto);
                            pstmt2.setTimestamp(6, sysDate);
                            pstmt2.setString(7, chgUser);
                            pstmt2.setString(8, chgTerm);
                            pstmt2.setString(9, siteCode);
                            pstmt2.setString(10, custCode);
                            pstmt2.setString(11, schemeCode);
                            pstmt2.setString(12, invoiceId);
                            pstmt2.setString(13, despId);
                            pstmt2.executeUpdate();
                            pstmt2.close();
                            pstmt2=null;
                        }
                        
                    }//schemeCode end
                    
                    sql=" update sorderdet_scheme  set amount = (case when amount is null then 0 else amount end)+? + ? " +
                            " where tran_id = ? and scheme_code =  ?  and line_no_form =? ";
                    pstmt2 = conn.prepareStatement(sql);
                    pstmt2.setDouble(1, discSchemeBillbackAmt);
                    pstmt2.setDouble(2, discSchemeOffinvAmt);
                    pstmt2.setString(3, sordNo);
                    pstmt2.setString(4, schemeCode);
                    pstmt2.setString(5, sordLineNo);
                    pstmt2.executeUpdate();
                    pstmt2.close();
                    pstmt2=null;
                    
                }//sorderdet_scheme loop
                rs.close();
                rs=null;
                pstmt.close();
                pstmt=null;
            }//Invoice_trace loop
            rs1.close();
            rs1=null;
            pstmt1.close();
            pstmt1=null;
            // calculating header discount 
            sql="select desp_id,tran_date,net_amt,site_code from invoice where invoice_id = ?";
            pstmt1=conn.prepareStatement(sql);
            pstmt1.setString(1,invoiceId);
            rs1=pstmt1.executeQuery();
            if(rs1.next())
            {
                siteCode=checkNull(rs1.getString("site_code"));
                despId=checkNull(rs1.getString("desp_id"));    
                invoiceDate=rs1.getTimestamp("tran_date");
            }
            rs1.close();
            rs1=null;
            pstmt1.close();
            pstmt1=null;
            
            sql="select sord_no from despatchdet where desp_id = ?";
            pstmt1=conn.prepareStatement(sql);
            pstmt1.setString(1,despId);
            rs1=pstmt1.executeQuery();
            if(rs1.next())
            {
                sordNo=checkNull(rs1.getString("sord_no"));
            }
            rs1.close();
            rs1=null;
            pstmt1.close();
            pstmt1=null;
            
            
            sql="select scheme_code  from sorder_scheme  where tran_id  = ?";
            pstmt1=conn.prepareStatement(sql);
            pstmt1.setString(1,sordNo);
            rs1=pstmt1.executeQuery();
            while(rs1.next())
            {
                schemeCodeHdr=checkNull(rs1.getString("scheme_code"));
                if(schemeCodeHdr !=null && schemeCodeHdr.trim().length()>0)
                {
                    
                    sql="select count(*)  from scheme_discount_trace where site_code=  ? " +
                            " and cust_code    = ?  and scheme_code= ? AND sale_order = ? and desp_id =  ?";
                    pstmt2=conn.prepareStatement(sql);
                    pstmt2.setString(1,siteCode);
                    pstmt2.setString(2,custCode);
                    pstmt2.setString(3,schemeCodeHdr);
                    pstmt2.setString(4,sordNo);
                    pstmt2.setString(5,despId);
                    rs2=pstmt2.executeQuery();
                    if(rs2.next())
                    {
                        hdrCnt = rs2.getInt(1);
                    }
                    rs2.close();
                    rs2=null;
                    pstmt2.close();
                    pstmt2=null;
                    
                    sql = "select app_from, valid_upto from scheme_applicability where scheme_code =  ?";
                    pstmt2 = conn.prepareStatement(sql);
                    pstmt2.setString(1, schemeCodeHdr);
                    rs2 = pstmt2.executeQuery();
                    if (rs2.next())
                    {
                        effFrom=rs2.getTimestamp("app_from");
                        validUpto= rs2.getTimestamp("valid_upto");
                        
                    }
                    rs2.close();
                    rs2 = null;
                    pstmt2.close();
                    pstmt2 = null;
                    
                    sql = "select promo_term,(case when  disc_perc is null then '0' else disc_perc end)  as disc_perc ,disc_type  from bom where bom_code =   ?";
                    pstmt2 = conn.prepareStatement(sql);
                    pstmt2.setString(1, schemeCodeHdr);
                    rs2 = pstmt2.executeQuery();
                    if (rs2.next())
                    {
                        promoTerm=checkNull(rs2.getString("promo_term"));
                        discPerc= rs2.getDouble("disc_perc");
                        discType=checkNull(rs2.getString("disc_type"));
                        
                    }
                    rs2.close();
                    rs2 = null;
                    pstmt2.close();
                    pstmt2 = null;
                    
                    offinvAmtHdr = 0.0;
                    billbackAmtHdr = 0.0;
                    if("P".equalsIgnoreCase(discType))
                    {
                        if("2".equalsIgnoreCase(promoTerm))
                        {
                            offinvAmtHdr=(totNetAmt*discPerc)/100;
                        }else if("3".equalsIgnoreCase(promoTerm))
                        {
                            billbackAmtHdr=(totNetAmt*discPerc)/100;
                        }
                    }else if("F".equalsIgnoreCase(discType))
                    {
                        if("2".equalsIgnoreCase(promoTerm))
                        {
                            offinvAmtHdr=discPerc;
                        }else if("3".equalsIgnoreCase(promoTerm))
                        {
                            billbackAmtHdr=discPerc;
                        }
                    }
                    if(hdrCnt==0)
                    {
                        tranId=generateTranId("t_scheme_discount_trace",siteCode,conn);
                        
                        sql="insert into scheme_discount_trace(    TRAN_ID  , SITE_CODE , CUST_CODE, SCHEME_CODE,  SALE_ORDER,INVOICE_ID," +
                                "INVOICE_DATE, DESP_ID, DISC_SCHEM_BILLBACK_AMT, DISC_SCHEM_OFFINV_AMT, EFF_FROM, VALID_UPTO," +
                                " EFF_NET_AMOUNT, DISC_PERC, DISC_TYPE,promo_term, CHG_DATE, CHG_USER, CHG_TERM)" +
                                "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                        pstmt2 = conn.prepareStatement(sql);
                        pstmt2.setString(1, tranId);
                        pstmt2.setString(2, siteCode);
                        pstmt2.setString(3, custCode);
                        pstmt2.setString(4, schemeCodeHdr);
                        pstmt2.setString(5, sordNo);
                        pstmt2.setString(6, invoiceId);
                        pstmt2.setTimestamp(7, invoiceDate);
                        pstmt2.setString(8, despId);
                        pstmt2.setDouble(9, billbackAmtHdr);
                        pstmt2.setDouble(10, offinvAmtHdr);
                        pstmt2.setTimestamp(11, effFrom);
                        pstmt2.setTimestamp(12, validUpto);
                        pstmt2.setDouble(13, effNetAmount);
                        pstmt2.setDouble(14, discPerc);
                        pstmt2.setString(15, discType);
                        pstmt2.setString(16, promoTerm);
                        pstmt2.setTimestamp(17, sysDate);
                        pstmt2.setString(18, chgUser);
                        pstmt2.setString(19, chgTerm);
                        pstmt2.executeUpdate();
                        pstmt2.close();
                        pstmt2=null;
                    }else
                    {
                        sql="update scheme_discount_trace set     DISC_SCHEM_BILLBACK_AMT = ?," +
                                "DISC_SCHEM_OFFINV_AMT    = ?,EFF_NET_AMOUNT    = ?," +
                                "eff_from            = ?,valid_upto         = ?, " +
                                "chg_date            = ?,chg_user            = ?," +
                                "chg_term            = ? where site_code=     ? " +
                                "and cust_code    =    ? and scheme_code=    ? " +
                                "AND invoice_id =  ? and desp_id = ? ";
                        pstmt2 = conn.prepareStatement(sql);
                        pstmt2.setDouble(1, billbackAmtHdr);
                        pstmt2.setDouble(2, offinvAmtHdr);
                        pstmt2.setDouble(3, effNetAmount);
                        pstmt2.setTimestamp(4, effFrom);
                        pstmt2.setTimestamp(5, validUpto);
                        pstmt2.setTimestamp(6, sysDate);
                        pstmt2.setString(7, chgUser);
                        pstmt2.setString(8, chgTerm);
                        pstmt2.setString(9, siteCode);
                        pstmt2.setString(10, custCode);
                        pstmt2.setString(11, schemeCodeHdr);
                        pstmt2.setString(12, invoiceId);
                        pstmt2.setString(13, despId);
                        pstmt2.executeUpdate();
                        pstmt2.close();
                        pstmt2=null;
                        
                        totBBAmtHdr=totBBAmtHdr+billbackAmtHdr;
                        totOffinvAmtHdr=totOffinvAmtHdr+offinvAmtHdr;

                    }
                    
                    sql=" update sorder_scheme  set amount = ? + ? " +
                            " where tran_id = ? and scheme_code =  ? ";
                    pstmt2 = conn.prepareStatement(sql);
                    pstmt2.setDouble(1, billbackAmtHdr);
                    pstmt2.setDouble(2, offinvAmtHdr);
                    pstmt2.setString(3, sordNo);
                    pstmt2.setString(4, schemeCodeHdr);
                    pstmt2.executeUpdate();
                    pstmt2.close();
                    pstmt2=null;
                }
            }//sorder_scheme
            rs1.close();
            rs1=null;
            pstmt1.close();
            pstmt1=null;
            
            sql=" update despatch set disc_schem_offinv_amt = ? ,disc_schem_billback_amt = ? where desp_id = ? ";
            pstmt2 = conn.prepareStatement(sql);
            pstmt2.setDouble(1, totOffinvAmtHdr);
            pstmt2.setDouble(2, totBBAmtHdr);
            pstmt2.setString(3, despId);
            pstmt2.executeUpdate();
            pstmt2.close();
            pstmt2=null;
            
        }catch(Exception e)
        {
            e.printStackTrace();
            // retString=e.toString();
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
		E12GenericUtility genericUtility= new  E12GenericUtility();
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
			/*System.out.println("keyString :"+keyString);
			System.out.println("keyCol :"+keyCol);
			System.out.println("tranSer :"+tranSer);*/

			xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
			xmlValues = xmlValues + "<Header></Header>";
			xmlValues = xmlValues + "<Detail1>";
			xmlValues = xmlValues +        "<tran_id></tran_id>";
			xmlValues = xmlValues +        "<site_code>" + siteCode + "</site_code>";
			if("w_drcrrcp_dr".equalsIgnoreCase(windowName))
			{
				xmlValues = xmlValues +        "<drcr_flag>" + "D" + "</drcr_flag>";
				xmlValues = xmlValues +        "<tran_date>" + currDateStr + "</tran_date>";
			}else
			{
				xmlValues = xmlValues +        "<tran_date>" + currDateStr + "</tran_date>";
			}
			xmlValues = xmlValues + "</Detail1></Root>";
			//System.out.println("xmlValues  :["+xmlValues+"]");
			TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", CommonConstants.DB_NAME);
			tranId = tg.generateTranSeqID(tranSer, keyCol, keyString, conn);
			//System.out.println("tranId :"+tranId);
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
	public String gbfCreateFrtDrn(String despId,String xtraParams,Connection conn) throws ITMException
    {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
	    String errString = "";
	    String sql = "",itemSer="",custCode="",siteCode="",tranType="",finEntity="",currcodeBase="";
	    String drcrFlag="",transer="",windowName="",drcrTranid="";
	    String salesInvPostHdr="",postType="",hdrCctrArray="";
	    String acctCodeAr="",cctrCodeAr="",currCode="",sysDate="";
	    String loginEmpCode="",loginSiteCode="",userId="",chgTerm="";
	    String rndStr="",rndOff="",reasCode="",remarks="",rndToStr="";
	    String detCctrArray="",detAcct="",detCctr="";
	    int lineNo=0,hdrcnt=0,detCnt=0;
	    double frtAmt=0.0,exchangeRate= 0.0,rndto=0.0;
	    boolean isCustomerFound=false;
	    Date currentDateval = new Date();
	    Timestamp dbSysDate= null;
	    //ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
	    E12GenericUtility genericUtility= new  E12GenericUtility();
	    MiscDrCrRcpConf MiscDrCrRcp = new MiscDrCrRcpConf();
	    ibase.webitm.ejb.fin.FinCommon finCommon = new ibase.webitm.ejb.fin.FinCommon();
	    ibase.webitm.ejb.dis.DistCommon discmn = new ibase.webitm.ejb.dis.DistCommon();
	    InitialContext ctx = null;
		long startTime = 0, endTime = 0, totalTime = 0, totalHrs = 0, totlMts = 0, totSecs = 0; // Added
		 try
			{
				startTime = System.currentTimeMillis();
			 	SimpleDateFormat sdf2 = new SimpleDateFormat(genericUtility.getApplDateFormat());
				sysDate = sdf2.format(currentDateval.getTime());
				dbSysDate = Timestamp.valueOf(genericUtility.getValidDateString(sysDate, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
				//System.out.println("dbSysDate>>>>>>>"+dbSysDate);
				userId = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode"));
				loginSiteCode = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode"));
				loginEmpCode = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode"));
				chgTerm = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId"));
				
			 	sql = "select case when d.FREIGHT_AMT_ADD is null then 0 else d.FREIGHT_AMT_ADD end as lc_frtamt, s.item_ser as ls_item_ser," +
			 			"s.CUST_CODE__BIL as ls_cust_code, d.site_code as ls_site_code, s.order_type as ls_trantype " +
			 			"from despatch d, sorder s where d.sord_no = s.sale_order " +
			 			" and desp_id = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, despId);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					frtAmt=rs.getDouble("lc_frtamt");
					itemSer = rs.getString("ls_item_ser");
					custCode = rs.getString("ls_cust_code");
					siteCode = rs.getString("ls_site_code");
					tranType = rs.getString("ls_trantype");
				}
				rs.close();
				rs = null;
				pstmt.close();
				
				if (frtAmt <= 0)
				{
					System.out.println("frtAmt>>>>>"+frtAmt);
					return "";//errString; // 23-Nov-16 Manoharan no error as freight amount is 0
				}
				sql = "select f.fin_entity as ls_fin_entity, f.curr_code as ls_currcode_base " +
						" from site s, finent f where f.fin_entity = s.fin_entity and s.site_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, siteCode);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{

					finEntity =checkNull(rs.getString("ls_fin_entity"));
					currcodeBase =checkNull(rs.getString("ls_currcode_base"));
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
				drcrFlag="D";
				transer="MDRCRD";
				windowName="w_misc_drcr_rcp_dr";
				//Changed By PriyankaC on 23April2018[START].
				//drcrTranid=generateTranId(windowName,siteCode,"",conn);
				drcrTranid=generateTranId(windowName,siteCode,drcrFlag,conn);
				//Changed By PriyankaC on 23April2018[END].
				
				salesInvPostHdr = finCommon.getFinparams("999999", "SALES_INV_POST_HDR", conn);
				//System.out.println("salesInvPostHdr.." + salesInvPostHdr);
				if (("NULLFOUND".equalsIgnoreCase(salesInvPostHdr) || salesInvPostHdr == null || salesInvPostHdr.trim().length() == 0) )
				{
					postType = "H";
				}
				
				
				sql = "select acct_code__ar, cctr_code__ar, curr_code   from customer where cust_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, custCode);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{

					acctCodeAr =checkNull(rs.getString("acct_code__ar"));
					cctrCodeAr =checkNull(rs.getString("cctr_code__ar"));
					currCode =checkNull(rs.getString("curr_code"));
					isCustomerFound=true;
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
				if((acctCodeAr.trim().length()== 0 || cctrCodeAr.trim().length()== 0) && isCustomerFound)
				{
					sql = "select acct_code__ar, cctr_code__ar  from itemser where item_ser = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, itemSer);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{

						acctCodeAr =checkNull(rs.getString("acct_code__ar"));
						cctrCodeAr =checkNull(rs.getString("cctr_code__ar"));
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
				if(acctCodeAr.trim().length()== 0 || cctrCodeAr.trim().length()== 0)
				{
					hdrCctrArray = finCommon.getAcctDetrTtype(" ",itemSer,"AR", tranType,conn);
					//System.out.println("hdrCctrArray>>>>"+hdrCctrArray);
					if(hdrCctrArray.trim().length() >0)
					{
						String[] arrStr =hdrCctrArray.split(",");
						if(arrStr.length>0)
						{
							acctCodeAr =arrStr[0];
						}
						if(arrStr.length>1)
						{
							cctrCodeAr =arrStr[1];
						}
					}
				}
				
				
				exchangeRate = finCommon.getDailyExchRateSellBuy(currCode,currcodeBase,siteCode, checkNull(sysDate), "S", conn);
				rndStr=transer+"-RND";
				rndOff=checkNull(finCommon.getFinparams("999999", rndStr, conn));
				if (!"NULLFOUND".equalsIgnoreCase(rndOff))
				{
					rndStr = transer + "-RNDTO";
					rndToStr =finCommon.getFinparams("999999", rndStr, conn);
					if (!"NULLFOUND".equalsIgnoreCase(rndToStr))
					{
						rndto=Double.parseDouble(rndToStr);
					}
				}
				
				reasCode = discmn.getDisparams("999999", "DEFAULT_REAS_CODE", conn);
				if ("NULLFOUND".equalsIgnoreCase(reasCode) || reasCode == null)
				{
					reasCode="";
				}
				remarks="Auto Debit Note for Despatch Freight "+despId;
				
				sql = " INSERT INTO MISC_DRCR_RCP ( TRAN_ID, TRAN_DATE, SITE_CODE, FIN_ENTITY, SUNDRY_TYPE," +
						" SUNDRY_CODE, ITEM_SER, ACCT_CODE, CCTR_CODE, EFF_DATE ,CURR_CODE ,EXCH_RATE ,DRCR_FLAG ," +
						"EMP_CODE__APRV ,TRAN_TYPE ,TRAN_SER ,DUE_DATE ,CHG_DATE ,CHG_USER ,CHG_TERM ,CUST_REF_NO," +
						"RND_OFF,RND_TO,REMARKS,SRETURN_NO ,AMOUNT,AMOUNT__BC,ADJ_MISC_CRN,CONFIRMED) " +
						" VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? , ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,? ,? ,? ,?,?,?) " ; 
			pstmt = conn.prepareStatement(sql);
			pstmt.setString( 1, drcrTranid); 
			pstmt.setTimestamp( 2, dbSysDate ); 
			pstmt.setString( 3, siteCode );
			pstmt.setString( 4, finEntity );
			pstmt.setString( 5, "C");
			pstmt.setString( 6, custCode );
			pstmt.setString( 7, itemSer);
			pstmt.setString( 8,acctCodeAr );
			pstmt.setString( 9, cctrCodeAr ); 
			pstmt.setTimestamp( 10, dbSysDate );
			pstmt.setString( 11, currCode ); 
			pstmt.setDouble( 12, exchangeRate ); 
			pstmt.setString( 13, drcrFlag ); 
			pstmt.setString( 14, "" ); 
			pstmt.setString( 15, tranType ); 
			pstmt.setString( 16, transer ); 
			pstmt.setTimestamp( 17, dbSysDate ); 
			pstmt.setTimestamp( 18, dbSysDate ); 
			pstmt.setString( 19, userId ); 
			pstmt.setString( 20, chgTerm ); 
			pstmt.setString( 21, "" ); 
			pstmt.setString( 22, rndOff );
			pstmt.setDouble( 23, rndto );
			pstmt.setString( 24, remarks );
			pstmt.setString( 25, despId );
			pstmt.setDouble( 26, frtAmt );
			pstmt.setDouble( 27, frtAmt );
			pstmt.setString( 28, "NA" );
			pstmt.setString( 29, "N" );
			hdrcnt=pstmt.executeUpdate();
			if(pstmt != null)
			{
				pstmt.close();pstmt = null;
			}
				
			detCctrArray = finCommon.getAcctDetrTtype(" ",itemSer,"FRECOST", tranType,conn);
			//System.out.println("detCctrArray>>>>"+detCctrArray);
			if(detCctrArray.trim().length() >0)
			{
				String[] arrStr =detCctrArray.split(",");
				if(arrStr.length>0)
				{
					detAcct =arrStr[0];
				}
				if(arrStr.length>1)
				{
					detCctr =arrStr[1];
				}
			}
			
			lineNo=lineNo++;
			sql = " INSERT INTO MISC_DRCR_RDET ( TRAN_ID,LINE_NO,ACCT_CODE,CCTR_CODE,AMOUNT,TAX_AMT,NET_AMT,REAS_CODE)"+
					 " VALUES ( ?,?,?,?,?,0,?,?)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString( 1, drcrTranid); 
			pstmt.setInt( 2, lineNo); 
			pstmt.setString( 3, detAcct); 
			pstmt.setString( 4, detCctr); 
			pstmt.setDouble( 5, frtAmt); 
			pstmt.setDouble( 6, frtAmt); 
			pstmt.setString( 7, reasCode); 
			detCnt=pstmt.executeUpdate();
			if(pstmt != null)
			{
				pstmt.close();pstmt = null;
			}
				
			
			if(hdrcnt>0 && detCnt>0)
			{
				errString = MiscDrCrRcp.confirm(drcrTranid, xtraParams, "", conn);
				//System.out.println("MiscDrCrRcp Confirm errString>>>>>"+errString);
			}
			
			endTime = System.currentTimeMillis();
			totalTime = endTime - startTime;

			totSecs = (int) (((double) 1 / 1000) * (totalTime));
			totalHrs = (int) (totSecs / 3600);
			totlMts = (int) (((totSecs - (totalHrs * 3600)) / 60));
			totSecs = (int) (totSecs - ((totalHrs * 3600) + (totlMts * 60)));

			System.out.println("Total Time Spend freight DR[" + totalHrs + "] Hours [" + totlMts + "] Minutes [" + totSecs + "] seconds");
			
			} catch (Exception e) 
			{	
				if(MiscDrCrRcp != null)
				{
					MiscDrCrRcp = null;
				}
				if(conn!=null)
				{
					try {
						conn.rollback();
						errString=e.toString();
						
					} catch (SQLException ex) {

						e.printStackTrace();
						
						throw new ITMException(e);
					}
				}
				e.printStackTrace();
				throw new ITMException(e);
			} 
	    return errString;
    }
	private String checkNullAndTrim(String inputVal)
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
	private String checkNull(String str)
	{
		//Modified by Anjali R. on[12/11/2018][Start]
		//if(str == null)
		// Modified by Piyush on 04/02/2019 [To check null with ignorecase
		// if(str == null || "NULL".equals(str))
		if(str == null || "NULL".equalsIgnoreCase(str))
		//Modified by Anjali R. on[12/11/2018][End]
		{
			return "";
		}
		else
		{
			//Modified by Anjali R. on[12/11/2018][Start]
			//return str;
			return str;
			//Modified by Anjali R. on[12/11/2018][Start]
		}

	}
	private String generateTranId( String windowName, String siteCode,String tranType, Connection conn )throws ITMException
    {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String selSql = "";
		String tranId = "";
		String tranSer = "";
		String keyString = "";
		String keyCol = "";
		String xmlValues = "";
		java.sql.Timestamp currDate = null; //Added By PriyankaC on 24April2018.
		 try
         {
			 //Added By PriyankaC on 24April2018.[START]
			 SimpleDateFormat sdfAppl = new SimpleDateFormat(genericUtility.getApplDateFormat());
			 currDate = new java.sql.Timestamp(System.currentTimeMillis());
			 String currDateStr = sdfAppl.format(currDate);
			 // //Added By PriyankaC on 24April2018.[end]
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
			/*System.out.println("keyString :"+keyString);
			System.out.println("keyCol :"+keyCol);
			System.out.println("tranSer :"+tranSer);*/

			xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
			xmlValues = xmlValues + "<Header></Header>";
			xmlValues = xmlValues + "<Detail1>";
			xmlValues = xmlValues + "<tran_id></tran_id>";
			//xmlValues = xmlValues +"<site_code>" + siteCode + "</site_code>";
			//Changed By PriyankaC on 19April2018 [START]
			xmlValues = xmlValues +"<site_code>" + siteCode + "</site_code>";
			xmlValues = xmlValues + "<drcr_flag>" + tranType + "</drcr_flag>";
			xmlValues = xmlValues + "<tran_date>" + currDateStr + "</tran_date>";
			//Changed By PriyankaC on 19April2018 [END]
			xmlValues = xmlValues + "</Detail1></Root>";
			System.out.println("xmlValues  :["+xmlValues+"]");
			TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", CommonConstants.DB_NAME);
			tranId = tg.generateTranSeqID(tranSer, keyCol, keyString, conn);
			System.out.println("tranId created:"+tranId);
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
	
	//gbf_auto_excise_drnote(minv_from)
	public String autoExciseDrNote(String invoiceId,String xtraParams,Connection conn) throws ITMException
	{
		String retString="",chgUser="",chgTerm="",sql="",itemSer="",itemSerExDrNote="",itemSerValue="",loginEmpCode="";
		String crTerm="",acctRnd="",cctrRnd="",tranType="",excise="",cess="",hsEdu="",exciseCctr="",exciseCctrFin="";
		String itemSerSplit="",exciseSplit="",excise1="",resultExcise="",exciseAcct="",cess1="",cessSplit="",resultCess="";
		String transer = "DRNRCP",cessCctr="",cessCctrFin="",hsEduCctrFin="",cessAcct="",sql1="",hsEdu1="",hsEduSplit="",resultHsEdu="";
		String hsEduCctr="",hsEduAcct="",type="",xmlString="",errString="";
		String siteCode="",finEntity="",custCodeBil="",currCode="",acctCodeAr="",cctrCodeAr="",gpNo="",analCode="",salesPers="",sql2="";
		String rndStr="",rndOff="",rndToStr="",acct="",cctr="";
		String tempSplitCode="";
		String splitCode="";
		String drcrTranid="",insertsql="",exciseDrAcct="",exciseDrAcctcd="",exciseDrCctrCd="";
		double rndto=0.0;
		double exchRate=0.0;
		double taxAmt=0.0,totAmount=0.0;
		double amount=0;
		int lineNo=0,cnt=0;
		int hdrcnt=0,detCnt=0;
		PreparedStatement pstmt = null,pstmt1=null,pstmt2=null;
		ResultSet rs = null,rs1=null,rs2=null;
		Timestamp sysDate = null;
		Timestamp tranDate=null,dueDate=null,gpDate=null,effDate=null;
		boolean isExciseItemser = false,isheader=true,isHeaderPrepared=false;
		String[] arrItemSerExDrNote={};
		E12GenericUtility genericUtility= new  E12GenericUtility();
		ibase.webitm.ejb.fin.FinCommon finCommon = new ibase.webitm.ejb.fin.FinCommon();
		ArrayList<String> itemserArry=new ArrayList<String>();
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		StringBuffer xmlBuff = new StringBuffer();
		StringBuffer advBuff = new StringBuffer();
		StringBuffer adv1Buff = new StringBuffer();
		StringBuffer finalBuff = new StringBuffer();
		HashMap acctCctrMap =  new HashMap(), tempMap = null,tempMapAmount=null;
		ArrayList tempList = null;
		InitialContext ctx = null;
		long startTime = 0, endTime = 0, totalTime = 0, totalHrs = 0, totlMts = 0, totSecs = 0; // Added
		try
		{
			startTime = System.currentTimeMillis();
			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String sysDateStr = sdf.format(currentDate.getTime());
			//System.out.println("Now the date is :=>  " + sysDateStr);
			sysDate = Timestamp.valueOf(genericUtility.getValidDateString(sysDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
			chgUser = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
			chgTerm = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId"));
			loginEmpCode =  genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");		
			
			
			itemSerExDrNote=checkNull(distCommon.getDisparams("999999", "ITEM_SER_EXCISE_DEBIT_NOTE", conn));
			if ((!"NULLFOUND".equalsIgnoreCase(itemSerExDrNote) && itemSerExDrNote != null && itemSerExDrNote.trim().length() > 0) )
			{
				 arrItemSerExDrNote =itemSerExDrNote.split(",");
				
			}else
			{
				return retString;
			}
			sql="select distinct item_ser from sorder where sale_order " +
					"in (select distinct sord_no from invoice_trace where invoice_id =? )";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1,invoiceId);
			rs=pstmt.executeQuery();
			while(rs.next())
			{
				itemSer=checkNull(rs.getString("item_ser"));
				//System.out.println("itemSer>>>"+itemSer+"]");
				for(int i =0;i<arrItemSerExDrNote.length;i++)
				{
					itemSerValue =arrItemSerExDrNote[i];
					//System.out.println("itemSerValue>>>"+itemSerValue+"]");
					if(itemSerValue.trim().equalsIgnoreCase(itemSer.trim()))
					{
						isExciseItemser = true;
					}
				}
			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;
			
			if(!isExciseItemser)
			{
				return retString;
			}
			
			crTerm=checkNull(finCommon.getFinparams("999999", "CR_PRD_ZERO", conn)).trim();
			acctRnd=checkNull(finCommon.getFinparams("999999", "ROUND_ADJUST_ACCT", conn)).trim();
			cctrRnd=checkNull(finCommon.getFinparams("999999", "ROUND_ADJUST_CCTR", conn)).trim();
			tranType=checkNull(finCommon.getFinparams("999999", "EXCISE_DR_TRAN_TYPE", conn)).trim();
			excise=checkNull(distCommon.getDisparams("999999", "EXC_TAX_CODE_MODVAT", conn)).trim();
			cess=checkNull(distCommon.getDisparams("999999", "CESS_TAX_CODE_MODVAT", conn)).trim();
			hsEdu=checkNull(distCommon.getDisparams("999999", "HS_EDU_CESS_TAXCODE", conn)).trim();
			// below finparm define for excise act code req id -F16FUNI001 - dt- 27-sep-2016
			exciseDrAcct=checkNull(finCommon.getFinparams("999999", "EXCISE_DR_ACCT", conn)).trim();
			if ((!"NULLFOUND".equalsIgnoreCase(exciseDrAcct) && exciseDrAcct != null && exciseDrAcct.trim().length() > 0) )
			{
				if(exciseDrAcct.trim().length() >0)
				{
					String[] arrStr1 =exciseDrAcct.split(";");
					if(arrStr1.length>0)
					{
						exciseDrAcctcd =arrStr1[0];
					}
					if(arrStr1.length>1)
					{
						exciseDrCctrCd =arrStr1[1];
					}
				}
			}
			//Excise Tax Code, selected from despatch
			if ((!"NULLFOUND".equalsIgnoreCase(excise) && excise != null && excise.trim().length() > 0) )
			{
					String[] arrStr =excise.split(",");
					int len =arrStr.length;
					for(int i =0;i<len;i++)
					{
						excise1 =arrStr[i];
						exciseSplit=exciseSplit+"'"+excise1+"',";
					}
					resultExcise = exciseSplit.substring(0, exciseSplit.length() - 1);
				
					sql="select Sum(case when tax_amt is null then 0 else tax_amt end) as tax_amt ,'E' as type from " +
							" taxtran Where  tran_code = 'S-DSP' " +
							" and tran_id IN ( select desp_id from invoice_trace where invoice_id = '"+invoiceId+"' )" +
							" and tax_code  in("+resultExcise+") and tax_amt <> 0 ";
					exciseCctrFin=checkNull(finCommon.getFinparams("999999", "EXC_MODVAT_ACCOUNT", conn)).trim();
					if ((!"NULLFOUND".equalsIgnoreCase(exciseCctrFin) && exciseCctrFin != null && exciseCctrFin.trim().length() > 0) )
					{
						if(exciseCctrFin.trim().length() >0)
						{
							String[] arrStr1 =exciseCctrFin.split(";");
							if(arrStr1.length>0)
							{
								exciseAcct =arrStr1[0];
							}
							if(arrStr1.length>1)
							{
								exciseCctr =arrStr1[1];
							}
						}
					}else
					{
						//retString = itmDBAccessLocal.getErrorString("", "VSENVAR1", "for finparm : EXC_MODVAT_ACCOUNT ");
						retString = itmDBAccessLocal.getErrorString("", "VSENVAR1", "", "for finparm : EXC_MODVAT_ACCOUNT" , conn);
						return retString;
					}
			}
			//Cess Tax code for Excise, selected from despatch
			if ((!"NULLFOUND".equalsIgnoreCase(cess) && cess != null && cess.trim().length() > 0) )
			{
				String[] arrStr =cess.split(",");
				int len =arrStr.length;
				for(int i =0;i<len;i++)
				{
					cess1 =arrStr[i];
					cessSplit=cessSplit+"'"+cess1+"',";
				}
				resultCess = cessSplit.substring(0, cessSplit.length() - 1);
				sql1="select Sum(case when tax_amt is null then 0 else tax_amt end) as tax_amt ,'C' as type from " +
						" taxtran Where  tran_code = 'S-DSP' " +
						" and tran_id IN ( select desp_id from invoice_trace where invoice_id = '"+invoiceId+"' )" +
						" and tax_code  in("+resultCess+") and tax_amt <> 0 ";
				
				if(sql !=null && sql.trim().length()>0)
				{
					sql=sql +" union all "+sql1;
				}
				cessCctrFin=checkNull(finCommon.getFinparams("999999", "CESS_MODVAT_ACCOUNT", conn)).trim();
				if ((!"NULLFOUND".equalsIgnoreCase(cessCctrFin) && cessCctrFin != null && cessCctrFin.trim().length() > 0) )
				{
					if(cessCctrFin.trim().length() >0)
					{
						String[] arrStr1 =cessCctrFin.split(";");
						if(arrStr1.length>0)
						{
							cessAcct =arrStr1[0];
						}
						if(arrStr1.length>1)
						{
							cessCctr =arrStr1[1];
						}
					}
				}else
				{
					//retString = itmDBAccessLocal.getErrorString("", "VSENVAR1", "for finparm : CESS_MODVAT_ACCOUNT ");
					retString = itmDBAccessLocal.getErrorString("", "VSENVAR1", "", "for finparm : CESS_MODVAT_ACCOUNT ", conn);
					return retString;
				}
				
			}
			//Higher and Secondary Edu. Cess, selected from despatch
			sql1="";
			if ((!"NULLFOUND".equalsIgnoreCase(hsEdu) && hsEdu != null && hsEdu.trim().length() > 0) )
			{
				String[] arrStr =hsEdu.split(",");
				int len =arrStr.length;
				for(int i =0;i<len;i++)
				{
					hsEdu1 =arrStr[i];
					hsEduSplit=hsEduSplit+"'"+hsEdu1+"',";
				}
				resultHsEdu = hsEduSplit.substring(0, hsEduSplit.length() - 1);
				sql1="select Sum(case when tax_amt is null then 0 else tax_amt end) as tax_amt ,'H' as type  from " +
						" taxtran Where  tran_code = 'S-DSP' " +
						" and tran_id IN ( select desp_id from invoice_trace where invoice_id = '"+invoiceId+"' )" +
						" and tax_code  in("+resultHsEdu+") and tax_amt <> 0 ";
				
				if(sql !=null && sql.trim().length()>0)
				{
					sql=sql +" union all "+sql1;
				}else
				{
					sql=sql1;
				}
				hsEduCctrFin=checkNull(finCommon.getFinparams("999999", "HS_EDU_CESS_ACCOUNT", conn)).trim();
				if ((!"NULLFOUND".equalsIgnoreCase(hsEduCctrFin) && hsEduCctrFin != null && hsEduCctrFin.trim().length() > 0) )
				{
					if(hsEduCctrFin.trim().length() >0)
					{
						String[] arrStr1 =hsEduCctrFin.split(";");
						if(arrStr1.length>0)
						{
							hsEduAcct =arrStr1[0];
						}
						if(arrStr1.length>1)
						{
							hsEduCctr =arrStr1[1];
						}
					}
				}else
				{
					//retString = itmDBAccessLocal.getErrorString("", "VSENVAR1", "for finparm : HS_EDU_CESS_ACCOUNT  ");
					retString = itmDBAccessLocal.getErrorString("", "VSENVAR1", "", "for finparm : HS_EDU_CESS_ACCOUNT  ", conn);
					return retString;
				}
				if(sql == null || sql.trim().length()==0)
				{
					return retString;
				}
				sql2 = "select tran_date, site_code,fin_entity,cust_code__bil,item_ser, curr_code, exch_rate," +
						"acct_code__ar,cctr_code__ar,due_date,gp_no,gp_date	,eff_date,anal_code	,sales_pers  " +
						"from invoice where invoice_id = ?";
				pstmt2 = conn.prepareStatement(sql2);
				pstmt2.setString(1, invoiceId);
				rs2 = pstmt2.executeQuery();
				if (rs2.next())
				{
					tranDate= rs2.getTimestamp("tran_date");
					siteCode=checkNull(rs2.getString("site_code"));
					finEntity=checkNull(rs2.getString("fin_entity"));
					custCodeBil=checkNull(rs2.getString("cust_code__bil"));
					itemSer=checkNull(rs2.getString("item_ser"));
					currCode=checkNull(rs2.getString("curr_code"));
					exchRate=rs2.getDouble("exch_rate");
					acctCodeAr=checkNull(rs2.getString("acct_code__ar"));
					cctrCodeAr=checkNull(rs2.getString("cctr_code__ar"));
					dueDate=rs2.getTimestamp("due_date");
					gpNo=checkNull(rs2.getString("gp_no"));
					gpDate=rs2.getTimestamp("gp_date");
					effDate=rs2.getTimestamp("eff_date");
					analCode=checkNull(rs2.getString("anal_code"));
					salesPers=checkNull(rs2.getString("sales_pers"));
				}
				else
				{
					return retString;
				}
				rs2.close();
				rs2 = null;
				pstmt2.close();
				pstmt2 = null;
				
				isheader=true;
				pstmt=conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				while(rs.next())
				{
					lineNo++;
					taxAmt=rs.getDouble("tax_amt");
					type=checkNull(rs.getString("type"));
					//taxAmt=100;
				//	System.out.println("taxAmt>>>"+taxAmt+"<<<lineNo>>"+lineNo);
					if(taxAmt==0)
					{
						continue;
					}
					if(isheader)
					{
							isHeaderPrepared=true;
							
						drcrTranid=generateTranId("w_drcrrcp_dr",siteCode,conn);	
						// below finparm define for excise act code req id -F16FUNI001 - dt- 27-sep-2016
						if(exciseDrAcctcd !=null && checkNull(exciseDrAcctcd).trim().length()>0)
						{
							acctCodeAr=exciseDrAcctcd;
						}
						if(exciseDrAcctcd !=null && checkNull(exciseDrAcctcd).trim().length()>0)
						{
							cctrCodeAr=exciseDrCctrCd;
						}
						insertsql = " INSERT INTO DRCR_RCP ( tran_id, tran_date, site_code, fin_entity, tran_type," +
								" tran_ser, drcr_flag, cust_code, item_ser, invoice_id ,acct_code ,cctr_code ,eff_date ," +
								"due_date ,curr_code ,exch_rate ,anal_code ,cr_term ,emp_code__aprv ,chg_date ,chg_user," +
								"chg_term,remarks,gp_no,gp_date ,rnd_off,rnd_to,amount) " +
								" VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? , ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,? ,? ,? ,?,?) " ; 
						pstmt1 = conn.prepareStatement(insertsql);
						pstmt1.setString( 1, drcrTranid); 
						pstmt1.setTimestamp( 2, tranDate ); 
						pstmt1.setString( 3, siteCode );
						pstmt1.setString( 4, finEntity );
						pstmt1.setString( 5, tranType);
						pstmt1.setString( 6, transer );
						pstmt1.setString( 7, "D");
						pstmt1.setString( 8,custCodeBil );
						pstmt1.setString( 9, itemSer ); 
						pstmt1.setString( 10, invoiceId );
						pstmt1.setString( 11, acctCodeAr ); 
						pstmt1.setString( 12, cctrCodeAr ); 
						pstmt1.setTimestamp( 13, effDate ); 
						pstmt1.setTimestamp( 14, dueDate ); 
						pstmt1.setString( 15, currCode ); 
						pstmt1.setDouble( 16, exchRate ); 
						pstmt1.setString( 17, analCode ); 
						pstmt1.setString( 18, crTerm ); 
						pstmt1.setString( 19, loginEmpCode ); 
						pstmt1.setTimestamp( 20, sysDate ); 
						pstmt1.setString( 21, chgUser );
						pstmt1.setString( 22, chgTerm );
						pstmt1.setString( 23, "Auto Generated Excise Debit note from Invoice " + invoiceId  );
						pstmt1.setString( 24, gpNo );
						pstmt1.setTimestamp( 25, gpDate );
						pstmt1.setString( 26, rndOff );
						pstmt1.setDouble( 27, rndto );
						pstmt1.setDouble( 28, taxAmt );
					
						hdrcnt=pstmt1.executeUpdate();
						if(pstmt1 != null)
						{
							pstmt1.close();pstmt1 = null;
						}
						//Direct insert
					isheader=false;
					}
					insertsql = " INSERT INTO DRCR_RDET ( tran_id, line_no, invoice_id, line_no__inv, sales_pers," +
							" drcr_amt, net_amt, tax_amt ) " +
							" VALUES ( ?, ?, ?, ?, ?, ?, ?, ? ) " ; 
					pstmt1 = conn.prepareStatement(insertsql);
					pstmt1.setString( 1, drcrTranid); 
					pstmt1.setInt( 2, lineNo ); 
					pstmt1.setString( 3, invoiceId );
					pstmt1.setDouble( 4, 1 );
					pstmt1.setString( 5, salesPers);
					pstmt1.setDouble( 6, taxAmt );
					pstmt1.setDouble( 7, taxAmt);
					pstmt1.setDouble( 8,0 );
					detCnt=pstmt1.executeUpdate();
					if(pstmt1 != null)
					{
						pstmt1.close();pstmt1 = null;
					}
					
						//insert into account detail
						if("E".equalsIgnoreCase(type))
						{
							acct=exciseAcct;
							cctr=exciseCctr;
						}else if("C".equalsIgnoreCase(type))
						{
							acct=cessAcct;
							cctr=cessCctr;
						}else if("H".equalsIgnoreCase(type))
						{
							acct=hsEduAcct;
							cctr=hsEduCctr;
						}
						if(cctr== null || cctr.trim().length()==0)
						{
							cctr=cctrCodeAr;
						}
						tempSplitCode = acct.trim()+"@"+cctr.trim();
						if(acctCctrMap.containsKey(tempSplitCode))
						{
							tempMap = new HashMap();
							tempMap =  (HashMap)acctCctrMap.get(tempSplitCode);
							tempMap.put("amount", taxAmt+(Double)tempMap.get("amount"));
							
							
						}else
						{	tempMap = new HashMap();
							tempMap.put("amount", taxAmt);
							tempMap.put("line_no", lineNo);
							tempMap.put("acct_code", acct);
							tempMap.put("cctr_code", cctr);
						}
						
						acctCctrMap.put(tempSplitCode,tempMap);
						totAmount=totAmount+taxAmt;
						
				}//while
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
				double totAmount1=0.0,rndAmt=0.0;
				totAmount1=totAmount;
				if(!"N".equalsIgnoreCase(rndOff))
				{
					//lc_tot_amount = gf_get_rndamt(lc_tot_amount1, ls_rndoff, integer(ls_rndto))
				}
				rndAmt=totAmount+totAmount1;
				if(isHeaderPrepared)
				{
					sql = "update drcr_rcp set round_adj = ?,amount=?,amount__bc=? where tran_id = ?";

					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setDouble(1,rndAmt);
					pstmt1.setDouble(2,totAmount);
					pstmt1.setDouble(3,(totAmount*exchRate));
					pstmt1.setString(4,drcrTranid);
					pstmt1.executeUpdate();

					pstmt1.close();
					pstmt1 = null;
					
				if(rndAmt != 0)
				{
					
					if ("NULLFOUND".equalsIgnoreCase(acctRnd) || ("NULLFOUND".equalsIgnoreCase(cctrRnd)))
					{
						//retString = itmDBAccessLocal.getErrorString("", "VSENVAR1", "");
						retString = itmDBAccessLocal.getErrorString("", "VSENVAR1", "", "", conn);
						return retString;
					}
					if(cctrRnd == null || cctrRnd.trim().length()==0)
					{
						cctrRnd="";
					}
					
					
					tempSplitCode = acctRnd.trim()+"@"+cctrRnd.trim();
					if(acctCctrMap.containsKey(tempSplitCode))
					{
						tempMap = new HashMap();
						tempMap =  (HashMap)acctCctrMap.get(tempSplitCode);
						tempMap.put("amount", rndAmt+(Double)tempMap.get("amount"));
						
						
					}else
					{	tempMap = new HashMap();
						tempMap.put("amount", rndAmt);
						tempMap.put("line_no", lineNo);
						tempMap.put("acct_code", acct);
						tempMap.put("cctr_code", cctr);
					}
					
					acctCctrMap.put(tempSplitCode,tempMap);
				}
				
				
				Set setAcctCctr = acctCctrMap.entrySet();
				java.util.Iterator itrItem =  setAcctCctr.iterator();
				while(itrItem.hasNext())
				{
					Map.Entry itemMapEntry = (Map.Entry)itrItem.next();
					splitCode = (String)itemMapEntry.getKey();
						
					tempMap = (HashMap)acctCctrMap.get(splitCode);
					
					insertsql = " INSERT INTO DRCR_RACCT ( tran_id, line_no, acct_code, cctr_code, amount ) " +
							" VALUES ( ?, ?, ?, ?, ? ) " ; 
					pstmt1 = conn.prepareStatement(insertsql);
					pstmt1.setString( 1, drcrTranid); 
					pstmt1.setInt( 2, (Integer)tempMap.get("line_no") ); 
					pstmt1.setString( 3, (String)tempMap.get("acct_code") );
					pstmt1.setString( 4, (String)tempMap.get("cctr_code") );
					pstmt1.setDouble( 5, (Double)tempMap.get("amount") );
					pstmt1.executeUpdate();
					if(pstmt1 != null)
					{
						pstmt1.close();pstmt1 = null;
					}
					
				}
				
				if(hdrcnt>0)
				{
					/*MiscDrCrRcpConfLocal MiscDrCrRcpConfirm = null;
					AppConnectParm appConnect = new AppConnectParm();
					ctx = new InitialContext(appConnect.getProperty());
					MiscDrCrRcpConfirm =  new MiscDrCrRcpConf();					
					errString = MiscDrCrRcpConfirm.confirm(drcrTranid, xtraParams, "", conn);
					System.out.println("MiscDrCrRcp Confirm errString>>>>>"+errString);*/
				}
				
				
			 }	
			}
			endTime = System.currentTimeMillis();
			totalTime = endTime - startTime;

			totSecs = (int) (((double) 1 / 1000) * (totalTime));
			totalHrs = (int) (totSecs / 3600);
			totlMts = (int) (((totSecs - (totalHrs * 3600)) / 60));
			totSecs = (int) (totSecs - ((totalHrs * 3600) + (totlMts * 60)));

			System.out.println("Total Time Spend Excise DR[" + totalHrs + "] Hours [" + totlMts + "] Minutes [" + totSecs + "] seconds");
			
		}
		catch (Exception e)
		{
			System.out.println("Exception ::" + e.getMessage() + ":");
			e.printStackTrace();
			// retString=e.toString();
			throw new ITMException(e);
		}
		
		return retString;
		
	}
	
	public String schemeHistoryUpd(String invoiceId, String siteCode, String tranType, Connection conn) throws ITMException, Exception
    {
        String retString = "", sql = "", sqlDetData = "", lsScheme = "", lsDocValue = "", lsSchemeCode = "",sinvoiceDate="";
        PreparedStatement pstmt = null, pstmt1 = null, pstmt2 = null,pstmtLot = null;
        ResultSet rs = null, rs1 = null, rs2 = null,rsLot = null;
        String custCode = "", xFldValue = "", lsItemCodeparent = "", lsOriginalItemCodeOrd = "";
        Timestamp tranDate = null,invoiceDate=null;
        String itemCodeOld = "",itemCode = "", itemCodeOrd = "", refNo = "", refLineNo = "", priceList = "", nature = "", lsOrderType = "", lsReturnable = "", lsReasCode = "", lotNo = "", lsVarValue = "";
        double totAmt = 0, totQty = 0, rate = 0, lcDiscount = 0.00, lcTotChargeQty = 0.00, lcTotFreeQty = 0.00, lcEffNetAmount = 0.00, ldSchCost = 0.00;
        String custCodeBill = "", lsStateCode = "", lsCountCode = "";
        Timestamp sysdate = new Timestamp((new Date()).getTime());
        String freeItem = "",refNoOld ="", lsColnameStr = "", lsSelectSql = "";
        String lsItemAry[] = null, lsColname[] = null;
        double FreeQty = 0, lcEffCostFree = 0.00, lcTotFreeEffCost = 0.00, ldChgAmt = 0.00, ldAmt = 0.00, ldTotAmt = 0.00,  ldEffCost = 0.00, lcFreeEffCost = 0.00;
        double ldQtyAry[] = null, ldAmtAry[] = null;
        int cnt = 0, llCtr = 0, llctrCol = 0, llCnt = 0, psCount = 0;
        boolean lbOtherFreeItem = false, lbItemCharge = false, lbChkNull = false;
        double ldQtyStd = 0.00;
        long llSchemeNum = 0L;
        UserInfoBean userInfo = new UserInfoBean();

        E12GenericUtility genericUtility = new E12GenericUtility();
        Calendar currentDate = Calendar.getInstance();
        SimpleDateFormat simpleDateFormatObj = new SimpleDateFormat(genericUtility.getApplDateFormat());
        String chgDateStr = simpleDateFormatObj.format(currentDate.getTime());
        String logInCode = userInfo.getLoginCode();
        String loginSite = userInfo.getSiteCode();
        String termId = userInfo.getRemoteHost();
        ArrayList lsItemAry1=new ArrayList();
        ArrayList ldQtyAry1=new ArrayList();
        ArrayList ldAmtAry1=new ArrayList();
        Timestamp chgDate=null;
		ArrayList <String> schemeKeyList= new ArrayList<String>();
        boolean periodScheme = false;
		String prevRefNo = "";
		//Added by Santosh on 04/01/17
		double invQty = 0.0;
		int lotCnt=0;
		String sqlCnt="";
		//added by nandkumar gadkari on 04/07/19
		int schemeCnt=0;
        double totItemLotQuantity=0,totItemLotQtyEffCost=0;
        double netAmt = 0;//Added by Rohini T on [19/11/2020]
        try
        {
			// 06-Dec-16 manoharan moved from inside loop
			lsVarValue = distCommon.getDisparams("999999", "SCHEME_HIST_NUM", conn);
			if (lsVarValue == null || lsVarValue.trim().length() == 0 || lsVarValue.equalsIgnoreCase("NULLFOUND"))
			{
				llSchemeNum = 1;
				//lbChkNull = true; // 13-jan-17 manoharan not required
				// 13-Jan-17 manoharan
				lsVarValue = "site_code,item_code,lot_no";
				schemeKeyList.add(lsVarValue);
			} else
			{
				llSchemeNum = Long.parseLong(lsVarValue);
				//lbChkNull = false; // 13-jan-17 manoharan not required
				//Changed by Santosh on 04/01/17 to change condition in for loop
				//for (int llCtrSch = 1; llCtrSch < llSchemeNum; llCtrSch++) // FOR LOOP [1]
				for (int llCtrSch = 1; llCtrSch <= llSchemeNum; llCtrSch++) // FOR LOOP [1]
				{
					lsScheme = "SCHEME_HIST_KEY" + llCtrSch;
					lsVarValue = distCommon.getDisparams("999999", lsScheme, conn);
					schemeKeyList.add(lsVarValue);
				}

			}
        	
        	chgDate = Timestamp.valueOf(genericUtility.getValidDateString(chgDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");
            // GenericUtility genericUtility = GenericUtility.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
            if ("R".equalsIgnoreCase(tranType))
            {
                sql = "select tran_date, cust_code,cust_code__bill as cust_code__bil from sreturn " + "    where tran_id = ?";
                //Changed by wasim on 17-MAY-2017 for correcting SQL for group by clause [START]
                /*sqlDetData = "select tran_id refNo,line_no refLineNo, item_code, sum(net_amt - tax_amt) totamt,sum(quantity__stduom) totqty"
                + " from sreturndet    where tran_id = ? and ret_rep_flag = 'P' "
                        + " group by line_no,item_code order by line_no desc";*/
                
                sqlDetData = "select tran_id as refNo,line_no as refLineNo, item_code, sum(net_amt - tax_amt) as totamt,sum(quantity__stduom) as totqty"
                        + " from sreturndet where tran_id = ? and ret_rep_flag = 'P' "
                                + " group by tran_id,line_no,item_code order by line_no desc";
                //Changed by wasim on 17-MAY-2017 for correcting SQL for group by clause [END]
                
            } else if ("I".equalsIgnoreCase(tranType))
            {

                sql = "select count(1) from invoice_trace t, item i where t.item_code = i.item_code and i.item_code__parent is not null  and t.invoice_id = ? "
                        + " group by t.sord_no,t.sord_line_no,t.item_code "
                + " order by t.sord_no, t.sord_line_no  desc ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, invoiceId);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					psCount = rs.getInt(1);
					periodScheme = true;
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if (psCount > 0 )
				{
				
				}

				sql = "select tran_date, cust_code,cust_code__bil as cust_code__bil from invoice " + "    where invoice_id = ?";
                sqlDetData = "select sord_no refNo,sord_line_no refLineNo,item_code,sum(net_amt - tax_amt) totamt,sum(quantity) totqty "
                + " from invoice_trace where invoice_id = ? "
                        + " group by sord_no,sord_line_no,item_code "
                + " order by sord_no, sord_line_no  desc ";
				
            }
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, invoiceId);
            rs = pstmt.executeQuery();
            if (rs.next())
            {
                tranDate = rs.getTimestamp(1);
                custCode = rs.getString(2);
                custCodeBill = rs.getString(3);
            }
            rs.close();
            rs = null;
            pstmt.close();
            pstmt = null;

            pstmt = conn.prepareStatement(sqlDetData);
            pstmt.setString(1, invoiceId);
            rs = pstmt.executeQuery();
            while (rs.next())
            {
				//ldEffCost = 0;
                itemCode = rs.getString("item_code");
                refNo = rs.getString("refNo");
                refLineNo = rs.getString("refLineNo");
                totAmt = rs.getDouble("totamt");
                totQty = rs.getDouble("totqty");
				ldQtyStd = totQty; // 13-Jan-17 manoharan
				ldAmt = totAmt;
				
				if(!itemCodeOld.trim().equals(itemCode.trim()) || !refNoOld.trim().equals(refNo.trim()))
				{
					ldTotAmt = 0;
					ldChgAmt = 0;
					llCtr = 0;
					lsItemAry1 = null;
					ldQtyAry1 = null;
					ldAmtAry1 = null;
					lsItemAry1=new ArrayList();
					ldQtyAry1=new ArrayList();
					ldAmtAry1=new ArrayList();
				}
				
                if ("R".equalsIgnoreCase(tranType))
                {
                    itemCodeOrd = itemCode;
                } else if ("I".equalsIgnoreCase(tranType))
                {
                    sql = "select price_list from sorder where sale_order = ?";
                    pstmt1 = conn.prepareStatement(sql);
                    pstmt1.setString(1, refNo);
                    rs1 = pstmt1.executeQuery();
                    if (rs1.next())
                    {
                        priceList = checkNull(rs1.getString("price_list"));
                    }
                    rs1.close();
                    rs1 = null;
                    pstmt1.close();
                    pstmt1 = null;
					// 13-jan-17 manoharan lot_no required to pick rate, taken below inside distinct lot_no loop
                    //if ((priceList.trim()).length() > 0)
                    //{
                    //    rate = distCommon.pickRate(priceList, sdf.format(sysdate), itemCode, "", "B", conn);
                    //}
                    sql = "select item_code__ord  from sorditem  where sale_order = ? and line_no = ? and item_code =? ";
                    pstmt1 = conn.prepareStatement(sql);
                    pstmt1.setString(1, refNo);
                    pstmt1.setString(2, refLineNo);
                    pstmt1.setString(3, itemCode);
                    rs1 = pstmt1.executeQuery();
                    if (rs1.next())
                    {
                        itemCodeOrd = rs1.getString(1);
                    }
                    rs1.close();
                    rs1 = null;
                    pstmt1.close();
                    pstmt1 = null;
					
                }
                
                llCtr++;
                //lsItemAry[llCtr] = itemCode;
                lsItemAry1.add(itemCode);
                //ldQtyAry[llCtr] = totQty;
                ldQtyAry1.add(totQty);
                ldAmt = totAmt;
                //ldAmtAry[llCtr] = ldAmt;
                ldAmtAry1.add(ldAmt);
                ldTotAmt = ldTotAmt + ldAmt;

                if ("I".equalsIgnoreCase(tranType) && !prevRefNo.trim().equals(refNo.trim())) // 06-dec-16 to avoid repeating for same order again
                {
						
					// 06-dec-16 manoharan commented and taken out side loop
					prevRefNo = refNo;
					sql = "select order_type, state_code__dlv, count_code__dlv from sorder where sale_order =?";
					pstmt2 = conn.prepareStatement(sql);
					pstmt2.setString(1, refNo);
					rs2 = pstmt2.executeQuery();
					if (rs2.next())
					{
						lsOrderType = rs2.getString("order_type");
						lsStateCode = rs2.getString("state_code__dlv");
						lsCountCode = rs2.getString("count_code__dlv");
					}
					pstmt2.close();
					pstmt2 = null;
					rs2.close();
					rs2 = null;

					sql = "select returnable,reas_code from sordertype where order_type =?";
					pstmt2 = conn.prepareStatement(sql);
					pstmt2.setString(1, lsOrderType);
					rs2 = pstmt2.executeQuery();
					if (rs2.next())
					{
						lsReturnable = rs2.getString("returnable");
						lsReasCode = rs2.getString("reas_code");
					}
					pstmt2.close();
					pstmt2 = null;
					rs2.close();
					rs2 = null;

				

                    sql = "select a.item_code as freeItem, sum(a.quantity) as FreeQty from sorditem a "
						+ " where a.sale_order = ? and a.line_no= ? and a.nature = 'F' "
                        + " and not exists  (select b.item_code  from sorditem b where b.sale_order = ? "
						+ " and b.line_no= ? and a.item_code = b.item_code and nature = 'C'     ) group by a.item_code ";

                    pstmt1 = conn.prepareStatement(sql);
                    pstmt1.setString(1, refNo);
                    pstmt1.setString(2, refLineNo);
                    pstmt1.setString(3, refNo);
                    pstmt1.setString(4, refLineNo);
                    rs1 = pstmt1.executeQuery();
                    while (rs1.next())
                    {
                        freeItem = rs1.getString("freeItem");
                        FreeQty = rs1.getDouble("FreeQty");

                        sql = "select count(1) as cnt, avg(eff_cost) as lc_eff_cost_free"
                        + "    from bomdet where bom_code = ?  and item_code = ? and nature = 'F'";
                        pstmt2 = conn.prepareStatement(sql);
                        pstmt2.setString(1, itemCodeOrd);
                        pstmt2.setString(2, freeItem);
                        rs2 = pstmt2.executeQuery();
                        if (rs2.next())
                        {
                            cnt = rs2.getInt("cnt");
                            lcEffCostFree = rs2.getDouble("lc_eff_cost_free");
                        }
                        pstmt2.close();
                        pstmt2 = null;
                        rs2.close();
                        rs2 = null;

                        if (cnt > 0)
                        {
                            lcEffCostFree = FreeQty * lcEffCostFree;
                            lcTotFreeEffCost = lcTotFreeEffCost + lcEffCostFree;
                            lbOtherFreeItem = true;
                        }

                    }
                    rs1.close();
                    rs1 = null;
                    pstmt1.close();
                    pstmt1 = null;
                }

                if (llCtr == 1)
                {
                    ldChgAmt = ldChgAmt + totAmt;
                }

                if ("I".equalsIgnoreCase(tranType))
                {
                    if (lbOtherFreeItem && (llCtr == 1 || lcTotFreeEffCost > 0))
                    {
                        ldChgAmt = ldChgAmt - lcTotFreeEffCost;
                    }
                }

                for (int i = 0; i < llCtr; i++)
                {
                   // System.out.println("@V@ In External FOR LOOP");
                    /*itemCode = lsItemAry[i];
                    ldQtyStd = ldQtyAry[i];
                    ldAmt = ldAmtAry[i];*/
                    itemCode = (String) lsItemAry1.get(i);
                    ldQtyStd = (Double) ldQtyAry1.get(i);
                    ldAmt = (Double) ldAmtAry1.get(i);

                    if ("R".equalsIgnoreCase(tranType))
                    {
                        lbItemCharge = true;
                        ldEffCost = 0;
                    } else
                    {
                        sql = "select avg(eff_cost) as eff_cost from bomdet where bom_code = ? and item_code =?";
                        pstmt2 = conn.prepareStatement(sql);
                        pstmt2.setString(1, itemCodeOrd);
                        pstmt2.setString(2, itemCode);
                        rs2 = pstmt2.executeQuery();
                        if (rs2.next())
                        {
                            ldEffCost = rs2.getDouble("eff_cost");
                        }
                        pstmt2.close();
                        pstmt2 = null;
                        rs2.close();
                        rs2 = null;

                        lcFreeEffCost = ldEffCost;
						// 13-jan-17 manoharan blindly assigned true
                        //lbItemCharge = true;
						lbItemCharge = false;

                        sql = "select nature from bomdet where bom_code = ? and item_code =?";
                        pstmt2 = conn.prepareStatement(sql);
                        pstmt2.setString(1, itemCodeOrd);
                        pstmt2.setString(2, itemCode);
                        rs2 = pstmt2.executeQuery();
                        if (rs2.next())
                        {
                            nature = rs2.getString("nature");
                        }
                        pstmt2.close();
                        pstmt2 = null;
                        rs2.close();
                        rs2 = null;
						if ("C".equals(nature))
						{
							lbItemCharge = true;
						}
                    }
                    if (ldEffCost == 0 && lbItemCharge)
                    {
                        if (ldTotAmt == 0)
                        {
                            ldTotAmt = 1;
                        }
                        if (ldQtyStd == 0)
                        {
                            ldQtyStd = 1;
                        }
                        ldEffCost = (ldAmt / ldTotAmt) * (ldChgAmt / ldQtyStd);
                    } else if (!lbItemCharge)
                    {
                        ldEffCost = lcFreeEffCost;
                    }
                    if ("I".equalsIgnoreCase(tranType))
                    {
                        sql = "select discount from invoice_trace where sord_no = ? and sord_line_no =? and item_code = ? and invoice_id =?";
                        pstmt2 = conn.prepareStatement(sql);
                        pstmt2.setString(1, refNo);
                        pstmt2.setString(2, refLineNo);
                        pstmt2.setString(3, itemCode);
                        pstmt2.setString(4, invoiceId);
                        rs2 = pstmt2.executeQuery();
                        if (rs2.next())
                        {
                            lcDiscount = rs2.getDouble("discount");
                        }
                        pstmt2.close();
                        pstmt2 = null;
                        rs2.close();
                        rs2 = null;
						/* // 06-dec-16 manoharan commented and taken out side loop
                        sql = "select order_type from sorder where sale_order =?";
                        pstmt2 = conn.prepareStatement(sql);
                        pstmt2.setString(1, refNo);
                        rs2 = pstmt2.executeQuery();
                        if (rs2.next())
                        {
                            lsOrderType = rs2.getString("order_type");
                        }
                        pstmt2.close();
                        pstmt2 = null;
                        rs2.close();
                        rs2 = null;

                        sql = "select returnable,reas_code from sordertype where order_type =?";
                        pstmt2 = conn.prepareStatement(sql);
                        pstmt2.setString(1, lsOrderType);
                        rs2 = pstmt2.executeQuery();
                        if (rs2.next())
                        {
                            lsReturnable = rs2.getString("returnable");
                            lsReasCode = rs2.getString("reas_code");
                        }
                        pstmt2.close();
                        pstmt2 = null;
                        rs2.close();
                        rs2 = null;
						*/
                    }

                    if (ldEffCost == 0)
                    {
                        if (lcDiscount > 0)
                        {
                            ldEffCost = (ldEffCost * lcDiscount / 100);
                        }
                    }
					
					// 13-jan-17 manoharan in case there is no scheme or discount then effe_cost is total value / total quantity
					ldEffCost = totAmt / totQty;
	

                    if ("R".equalsIgnoreCase(tranType))
                    {
                        sql = "select distinct lot_no ,quantity from sreturndet where tran_id = ?  and line_no = ? and item_code = ? order by lot_no";//,quantity COLUMN ADDED BY NANDKUMAR GADKARI ON 10/07/19
                    } 
                    else if ("I".equalsIgnoreCase(tranType))
                    {
                        //added by nandkumar gadkari on 04/07/19--------start------------------for not to update average effective cost +
                    	lotCnt =0;
                    	sql = "select count(distinct EXP_LEV) as ll_scheme from invoice_trace	where invoice_id = ? "
                    			+ " and SORD_NO =?	and SORD_LINE_NO = ? and item_code = ? ";
                        pstmt2 = conn.prepareStatement(sql);
                        pstmt2.setString(1, invoiceId);
                        pstmt2.setString(2, refNo);
                        pstmt2.setString(3, refLineNo);
                        pstmt2.setString(4, itemCode);
                        rs2 = pstmt2.executeQuery();
                        if (rs2.next())
                        {
                        	schemeCnt = rs2.getInt(1);
                        }
                        pstmt2.close();
                        pstmt2 = null;
                        rs2.close();
                        rs2 = null;
                    	
                      if(schemeCnt == 1)
                      {
                    	  sql = "select lot_no,sum(quantity),Round(sum((quantity * rate) - ( ((quantity * rate)*discount)/100)) / sum(quantity),4) as inv_rate 	from invoice_trace 	"//change round 2 to 4  by nandkumar gadkari on 26-05-2020
                    	  		+ "	where invoice_id = ? 	and SORD_NO = ? 	and SORD_LINE_NO = ? 		and item_code = ? 		group by lot_no";
                      }
                    	
                      else
                      {
                    	//added by nandkumar gadkari on 04/07/19--------end------------------for not to update average effective cost   
                    	sql = "select distinct lot_no ,quantity from invoice_trace where invoice_id = ?  and sord_no = ?"//,quantity COLUMN ADDED BY NANDKUMAR GADKARI ON 10/07/19
                                + " and sord_line_no = ?  and item_code = ? order by lot_no";
                    	//added by nandkumar gadkari on 14/08/19--------start---------
                    	lotCnt =0;
                    	sqlCnt = "select count(distinct lot_no) from invoice_trace where invoice_id = ?  and sord_no = ? and sord_line_no = ?  and item_code = ? ";
                    	 pstmt2 = conn.prepareStatement(sqlCnt);
                         pstmt2.setString(1, invoiceId);
                         pstmt2.setString(2, refNo);
                         pstmt2.setString(3, refLineNo);
                         pstmt2.setString(4, itemCode);
                         rs2 = pstmt2.executeQuery();
                         if (rs2.next())
                         {
                         	lotCnt = rs2.getInt(1);
                         }
                         pstmt2.close();
                         pstmt2 = null;
                         rs2.close();
                         rs2 = null;
                    	//added by nandkumar gadkari on 14/08/19--------end---------
                    	
                      }
                        
                    }
                    pstmtLot = conn.prepareStatement(sql);
                    //Changed by wasim on 17-05-2017 for setting correct parameter for prepared statements [START]
                    /*pstmtLot.setString(1, invoiceId);
                    pstmtLot.setString(2, refNo);
                    pstmtLot.setString(3, refLineNo);
                    pstmtLot.setString(4, itemCode);*/
                    if ("R".equalsIgnoreCase(tranType))
                    {
                    	pstmtLot.setString(1, invoiceId);
                    	pstmtLot.setString(2, refLineNo);
                    	pstmtLot.setString(3, itemCode);
                    }
                    else if ("I".equalsIgnoreCase(tranType))
                    {
                    	pstmtLot.setString(1, invoiceId);
                        pstmtLot.setString(2, refNo);
                        pstmtLot.setString(3, refLineNo);
                        pstmtLot.setString(4, itemCode);
                    }
                  //Changed by wasim on 17-05-2017 for setting correct parameter for prepared statements [END]
                    
                    rsLot = pstmtLot.executeQuery();
                    while (rsLot.next())
                    {
                        lotNo = rsLot.getString("lot_no");
                        //added by nandkumar gadkari on 04/07/19--------start------------------for not to update average effective cost 
                        invQty =rsLot.getDouble(2);
                        if(schemeCnt == 1)
                        {
                        	ldEffCost = rsLot.getDouble("inv_rate");
                        	  //added by nandkumar gadkari on 20/09/19--------start------------------for  total  quantity update  
                        	/*sql = " select SUM(quantity)  from invoice_trace where "
									+ " invoice_id = ?  and sord_no = ? and  item_code = ? and lot_no = ? and line_type not in ('I','V','P')  ";
								
								pstmt2 = conn.prepareStatement(sql);
								pstmt2.setString(1, invoiceId);
								pstmt2.setString(2, refNo);
								pstmt2.setString(3, itemCode);
								pstmt2.setString(4, lotNo);
								rs2 = pstmt2.executeQuery();
								if (rs2.next())
								{
									totItemLotQuantity = rs2.getDouble(1);
									
								}
								pstmt2.close();
								pstmt2 = null;
								rs2.close();
								rs2 = null;
								
								if(totItemLotQuantity > invQty)
								{
									invQty=totItemLotQuantity;
								}*/
								  //added by nandkumar gadkari on 20/09/19--------end------------------for  total  quantity update
                        }
                       
                        //added by nandkumar gadkari on 04/07/19--------end------------------for not to update average effective cost 
						// 13-jan-17 manoharan moved from above as lot_no not available
                      //added by nandkumar gadkari on 14/08/19--------Start---------
                       if(lotCnt == 1)
                       {
                    	   invQty=totQty;
                       }
                      //added by nandkumar gadkari on 14/08/19--------end---------
                      //added by nandkumar gadkari on 01/10/19--------start------------------for  total  quantity update  
                      /*sql = " select SUM(quantity), Round(sum((quantity * rate) - ( ((quantity * rate)*discount)/100)) / sum(quantity),2) from invoice_trace where "
								+ " invoice_id = ?  and sord_no = ? and  item_code = ? and lot_no = ? and line_type not in ('I','V','P')  ";
					  *///SQL COMMENTED  AND NEW ADDED BY NANDKUMAR GADKARI ON 07/01/20 
                       // 15-dec-2020 manoharan if scheme is there and more then only lot with one scheme and another  without scheme
                       // for the same item then only average for item to be done  
                    	sql = " select SUM(quantity), Round(sum((quantity * rate) - ( ((quantity * rate)*discount)/100)) / sum(quantity),2) from invoice_trace where "
								+ " invoice_id = ?   and  item_code = ? and lot_no = ? and line_type not in ('I','V','P')  ";//commented by nandkumar gadkari on 20/01/20
                    	/*sql = " select SUM(quantity) from invoice_trace where "
								+ " invoice_id = ?   and  item_code = ? and lot_no = ? and line_type not in ('I','V','P')  ";*/// 
							pstmt2 = conn.prepareStatement(sql);
							pstmt2.setString(1, invoiceId);
							//pstmt2.setString(2, refNo);
							pstmt2.setString(2, itemCode);
							pstmt2.setString(3, lotNo);
							rs2 = pstmt2.executeQuery();
							if (rs2.next())
							{
								totItemLotQuantity = rs2.getDouble(1);
		                       // 15-dec-2020 manoharan if scheme is there and more then only lot with one scheme and another  without scheme
		                       // for the same item then only average for item to be done, following condition added  
								if (schemeCnt == 1)
								{
								
									totItemLotQtyEffCost = rs2.getDouble(2); //commented by nandkumar gadkari on 20/01/20
								}
								
							}
							pstmt2.close();
							pstmt2 = null;
							rs2.close();
							rs2 = null;
							
							if(totItemLotQuantity > invQty)
							{
								invQty=totItemLotQuantity;
			                    // 15-dec-2020 manoharan if scheme is there and more then only lot with one scheme and another  without scheme
			                    // for the same item then only average for item to be done, following condition added  
								if (schemeCnt == 1)
								{
									ldEffCost =totItemLotQtyEffCost; //commented by nandkumar gadkari on 20/01/20
								}
								
							}
							//added by nandkumar gadkari on 20/01/20---------------start--------------------
		                    // 15-dec-2020 manoharan if scheme is there and more then only lot with one scheme and another  without scheme
		                    // for the same item then only average for item to be done  
							if (schemeCnt > 1)
							{
	
								sql = " select Round(sum((quantity * rate) - ( ((quantity * rate)*discount)/100)) / sum(quantity),4) from invoice_trace where "//change round 2 to 4  by nandkumar gadkari on 26-05-2020
										+ " invoice_id = ?   and  item_code = ? and line_type not in ('I','V','P')  ";
									pstmt2 = conn.prepareStatement(sql);
									pstmt2.setString(1, invoiceId);
									pstmt2.setString(2, itemCode);
									
									rs2 = pstmt2.executeQuery();
									if (rs2.next())
									{
										totItemLotQtyEffCost = rs2.getDouble(1);
										
									}
									pstmt2.close();
									pstmt2 = null;
									rs2.close();
									rs2 = null;
							}
								
							ldEffCost =totItemLotQtyEffCost;
									
								
								//added by nandkumar gadkari on 20/01/20---------------end--------------------
							
							  //added by nandkumar gadkari on 01/10/19--------end------------------for  total  quantity update
						if ((priceList.trim()).length() > 0)
						{
							rate = distCommon.pickRate(priceList, sdf.format(sysdate), itemCode, lotNo, "B", conn);
						}
						
						//System.out.println("@V@ llSchemeNum before for LOOP 1 :- ["+llSchemeNum+"]");
						//for (int llCtrSch = 1; llCtrSch < llSchemeNum; llCtrSch++) // FOR LOOP [1]
						for (int llCtrSch = 0; llCtrSch < schemeKeyList.size(); llCtrSch++) 
						{
						   // System.out.println("@V@ FOR LOOP 1");
							//if (!lbChkNull) // 13-jan-17 manoharan not required
							//{
								llctrCol = 0;
								//lsScheme = "SCHEME_HIST_KEY" + llCtrSch;
								lsVarValue = schemeKeyList.get(llCtrSch); //distCommon.getDisparams("999999", lsScheme, conn);
								lsColname = lsVarValue.split(",");
								llctrCol = lsColname.length;
							//}
							lsDocValue = "";
							llCnt = 0;
							lsSchemeCode = "";

						//    System.out.println("@V@ llctrCol before for LOOP 2 :- ["+llctrCol+"]");
							for (llCnt = 0; llCnt < llctrCol; llCnt++) // FOR LOOP [2]
							{
								xFldValue = "";
							    
								lsColnameStr = lsColname[llCnt];
								//System.out.println("@V@ FOR LOOP (1) llCnt [" + llCnt+ "] lsColnameStr ["+ lsColnameStr+"] llCtrSch ["+ llCtrSch+ "] lsVarValue [" +lsVarValue +"]");
								if (lsColnameStr.trim().equalsIgnoreCase("site_code"))
								{
									lsDocValue = lsDocValue + ',' + siteCode.trim();
								} else if (lsColnameStr.trim().equalsIgnoreCase("cust_code"))
								{
									lsDocValue = lsDocValue + ',' + custCode.trim();
								} 
								else if (lsColnameStr.trim().equalsIgnoreCase("cust_code__bil"))
								{
									lsDocValue = lsDocValue + ',' + custCodeBill.trim();
								}
								//Added by Santosh on 04/01/17 to add Invoice Id in the doc key [Start]
								else if (lsColnameStr.trim().equalsIgnoreCase("invoice_id"))
								{
									lsDocValue = lsDocValue + ',' + invoiceId.trim();
								}
								//Added by Santosh on 04/01/17 to add Invoice Id in the doc key [End]
								else if (lsColnameStr.trim().equalsIgnoreCase("lot_no") || lsColnameStr.trim().equalsIgnoreCase("item_code"))
								{
									if ( lsColnameStr.trim().equalsIgnoreCase("item_code"))
									{
										xFldValue = itemCode;
									}
									else if ( lsColnameStr.trim().equalsIgnoreCase("lot_no"))
									{
										xFldValue = lotNo;
									}
									else
									{
										if ("R".equalsIgnoreCase(tranType))
										{
											lsSelectSql = "select " + lsColnameStr + " from sreturndet where tran_id = '" + invoiceId + "'" + " and line_no = " + refLineNo + " and lot_no ='" + lotNo + " and item_code ='" + itemCode + "'";
										} else
										{
											lsSelectSql = "select " + lsColnameStr + " from invoice_trace where invoice_id = '" + invoiceId + "'" + " and sord_line_no = " + refLineNo + " and lot_no ='" + lotNo + "'" + " and item_code ='" + itemCode + "' and item_code__ord = '" + itemCodeOrd + "' and sord_no ='" + refNo + "'";
										}
										pstmt2 = conn.prepareStatement(lsSelectSql);
										rs2 = pstmt2.executeQuery();
										if (rs2.next())
										{
											xFldValue = checkNull(rs2.getString(1));
										}
										pstmt2.close();
										pstmt2 = null;
										rs2.close();
										rs2 = null;
									}

									if ("I".equalsIgnoreCase(tranType) && periodScheme) // 06-Dec-16 manoharan periodScheme boolean variable considered to avoid checking for all item if no period scheme
									{
										if (lsColnameStr.equalsIgnoreCase("item_code") && (xFldValue != null && xFldValue.trim().length() > 0))
										{
											sql = "select item_code__parent from item where item_code = ?";
											pstmt2 = conn.prepareStatement(sql);
											pstmt2.setString(1, xFldValue);
											rs2 = pstmt2.executeQuery();
											if (rs2.next())
											{
												lsItemCodeparent = rs2.getString("item_code__parent");
											}
											pstmt2.close();
											pstmt2 = null;
											rs2.close();
											rs2 = null;

											if (lsItemCodeparent != null && lsItemCodeparent.trim().length() > 0)
											{
												// 06-dec-16 manoharan commented and taken out side loop
											   /* sql = "select order_type, state_code__dlv, count_code__dlv from sorder where sale_order = ?";
												pstmt2 = conn.prepareStatement(sql);
												pstmt2.setString(1, refNo);
												rs2 = pstmt2.executeQuery();
												if (rs2.next())
												{
													lsOrderType = rs2.getString("order_type");
													lsStateCode = rs2.getString("state_code__dlv");
													lsCountCode = rs2.getString("count_code__dlv");
												}
												pstmt2.close();
												pstmt2 = null;
												rs2.close();
												rs2 = null;
												*/
												lsSchemeCode = checkScheme(lsItemCodeparent, lsOrderType, custCode, siteCode, lsStateCode, lsCountCode, tranDate, conn);

												if (lsSchemeCode != null && lsSchemeCode.trim().length() > 0)
												{
													//Changed by wasim to correct the SQL 
													/*sql = "select sum(tot_charge_qty), sum(tot_free_qty), sum(eff_net_amount), max(scheme_code)"
												+ " from prd_scheme_trace where site_code= ? and cust_code= ? and item_code=?";*/
													
													sql = " select sum(tot_charge_qty) as tot_charge_qty, sum(tot_free_qty) as tot_free_qty, "
														+ " sum(eff_net_amount) as eff_net_amount, max(scheme_code) as scheme_code"
														+ " from prd_scheme_trace where site_code= ? and cust_code= ? and item_code=?";
													
													pstmt2 = conn.prepareStatement(sql);
													pstmt2.setString(1, siteCode);
													pstmt2.setString(2, custCode);
													pstmt2.setString(3, lsItemCodeparent);
													rs2 = pstmt2.executeQuery();
													if (rs2.next())
													{
														lcTotChargeQty = rs2.getDouble("tot_charge_qty");
														lcTotFreeQty = rs2.getDouble("tot_free_qty");
														lcEffNetAmount = rs2.getDouble("eff_net_amount");
														lsScheme = rs2.getString("scheme_code");
													}
													pstmt2.close();
													pstmt2 = null;
													rs2.close();
													rs2 = null;

													xFldValue = lsItemCodeparent;
													lsSchemeCode = lsScheme;

													if ((lcTotChargeQty + lcTotFreeQty) > 0)
													{
														ldEffCost = lcEffNetAmount / (lcTotChargeQty + lcTotFreeQty);
													} else
													{
														ldEffCost = 0;
													}
												}
											}
										}
									}
									//Changed by Santosh on 03/01/17 to trim xFldValue
									//lsDocValue = lsDocValue + "," + xFldValue;
									lsDocValue = lsDocValue + "," + xFldValue.trim();
								} else
								{
									if ("R".equalsIgnoreCase(tranType))
									{
										lsSelectSql = "select " + lsColnameStr + " from sreturndet where tran_id = '" + invoiceId + "'" + " and line_no = " + refLineNo + " and lot_no ='" + lotNo + "' and item_code ='" + itemCode + "'";
									} else
									{
										lsSelectSql = "select " + lsColnameStr + " from invoice_trace where invoice_id = '" + invoiceId + "'" + " and sord_line_no = '" + refLineNo + "' and lot_no ='" + lotNo + "' and rate = " + rate + "" + " and item_code ='" + itemCode + "' and item_code__ord = '" + itemCodeOrd + "' and sord_no ='" + refNo + "'";
									}
									pstmt2 = conn.prepareStatement(lsSelectSql);
									rs2 = pstmt2.executeQuery();
									if (rs2.next())
									{
										xFldValue = checkNull(rs2.getString(1));
									}
									pstmt2.close();
									pstmt2 = null;
									rs2.close();
									rs2 = null;

									if ("I".equalsIgnoreCase(tranType))
									{
										if (lsColnameStr.equalsIgnoreCase("item_code") && (xFldValue != null && xFldValue.trim().length() > 0))
										{
											sql = "select item_code__parent from item where item_code = ?";
											pstmt2 = conn.prepareStatement(sql);
											pstmt2.setString(1, xFldValue);
											rs2 = pstmt2.executeQuery();
											if (rs2.next())
											{
												lsItemCodeparent = rs2.getString("item_code__parent");
											}
											pstmt2.close();
											pstmt2 = null;
											rs2.close();
											rs2 = null;

											if (lsItemCodeparent != null && lsItemCodeparent.trim().length() > 0)
											{
												/* 17-jan-17 manoharan already taken out side
												sql = "select order_type, state_code__dlv, count_code__dlv from sorder where sale_order = ?";
												pstmt2 = conn.prepareStatement(sql);
												pstmt2.setString(1, refNo);
												rs2 = pstmt2.executeQuery();
												if (rs2.next())
												{
													lsOrderType = rs2.getString("order_type");
													lsStateCode = rs2.getString("state_code__dlv");
													lsCountCode = rs2.getString("count_code__dlv");
												}
												pstmt2.close();
												pstmt2 = null;
												rs2.close();
												rs2 = null;
												*/
												lsSchemeCode = checkScheme(lsItemCodeparent, lsOrderType, custCode, siteCode, lsStateCode, lsCountCode, tranDate, conn);

												if (lsSchemeCode != null && lsSchemeCode.trim().length() > 0)
												{
													sql = "select sum(tot_charge_qty), sum(tot_free_qty), sum(eff_net_amount), max(scheme_code)"
												+ " from prd_scheme_trace where site_code= ? and cust_code= ? and item_code=?";
													pstmt2 = conn.prepareStatement(sql);
													pstmt2.setString(1, siteCode);
													pstmt2.setString(2, custCode);
													pstmt2.setString(3, lsItemCodeparent);
													rs2 = pstmt2.executeQuery();
													if (rs2.next())
													{
														lcTotChargeQty = rs2.getDouble("tot_charge_qty");
														lcTotFreeQty = rs2.getDouble("tot_free_qty");
														lcEffNetAmount = rs2.getDouble("eff_net_amount");
														lsScheme = rs2.getString("scheme_code");
													}
													pstmt2.close();
													pstmt2 = null;
													rs2.close();
													rs2 = null;

													xFldValue = lsItemCodeparent;
													lsSchemeCode = lsScheme;

													if ((lcTotChargeQty + lcTotFreeQty) > 0)
													{
														ldEffCost = lcEffNetAmount / (lcTotChargeQty + lcTotFreeQty);
													} else
													{
														ldEffCost = 0;
													}
												}
											}
										}
									}
									lsDocValue = lsDocValue + "," + xFldValue;
								}

							}// FOR LOOP [2] --END--
							//System.out.println("13-jan-17 lsDocValue>>>>>>["+lsDocValue+"]");
							lsDocValue = lsDocValue.substring(1, lsDocValue.length());

							if ("I".equalsIgnoreCase(tranType))
							{
								if (lsSchemeCode != null && lsSchemeCode.trim().length() > 0)
								{
									lsOriginalItemCodeOrd = itemCodeOrd;
									itemCodeOrd = lsSchemeCode;
								}
							}
							//Added by Santosh on 04/01/17 to get invoice qty from invoice[Start]
							/*sql = "SELECT SUM(QUANTITY) AS QUANTITY FROM INVDET WHERE INVOICE_ID = ? AND ITEM_CODE = ? GROUP BY ITEM_CODE";
							pstmt2 = conn.prepareStatement(sql);
							pstmt2.setString(1, invoiceId);
							pstmt2.setString(2, itemCode);
							rs2 = pstmt2.executeQuery();
							if (rs2.next())
							{
								invQty = rs2.getDouble("QUANTITY");
							}
							if(pstmt2!=null)
							{
								 pstmt2.close();
								 pstmt2 = null;
							}
							if(rs2 != null)
							{
								rs2.close();
								rs2 = null;
							}*/// commented by nandkumar gadkari on 10/07/19
							//Added by NANDKUMAR GADKARI on 01/10/18 to get invoice dATE from invoice[START]
                            //sql = "SELECT TRAN_DATE FROM  INVOICE WHERE INVOICE_ID = ?";
                            sql = "SELECT TRAN_DATE,NET_AMT FROM  INVOICE WHERE INVOICE_ID = ?";//Added and Commented by Rohini T on 18/11/2020 [To get net_amt from invoice]
							pstmt2 = conn.prepareStatement(sql);
							pstmt2.setString(1, invoiceId);
							rs2 = pstmt2.executeQuery();
							if (rs2.next())
							{
                                invoiceDate = rs2.getTimestamp("TRAN_DATE");
                                netAmt =rs2.getDouble("net_amt");//Added and Commented by Rohini T on 18/11/2020 [To get net_amt from invoice]
							}
							if(pstmt2!=null)
							{
								 pstmt2.close();
								 pstmt2 = null;
							}
							if(rs2 != null)
							{
								rs2.close();
								rs2 = null;
							}
							//Added by NANDKUMAR GADKARI on 01/10/18 to get invoice dATE from invoice[END]
                            //Added by Santosh on 04/01/17 to get invoice qty from invoice[End]
                            if(lsDocValue.indexOf(invoiceId.trim()) != -1)//Added and Commented by Rohini T on 18/11/2020 [Added condition to check invoice id is the part of doc key][Start]
							{
                                //if (ldEffCost < rate && ldEffCost != 0)
                                if ((ldEffCost < rate && ldEffCost != 0)||netAmt == 0)	//Added and Commented by Rohini T on 18/11/2020 [Added condition to check order is special type order]
							    {
								    sql = "select count(*) as cnt from min_rate_history where doc_key = ?";
								    pstmt2 = conn.prepareStatement(sql);
								    pstmt2.setString(1, lsDocValue);
								    rs2 = pstmt2.executeQuery();
								    if (rs2.next())
								    {
								    	cnt = rs2.getInt("cnt");
							    	}
								    pstmt2.close();
								    pstmt2 = null;
							    	rs2.close();
								    rs2 = null;

							    	if (cnt == 0)
								    {
									    sql = "insert into min_rate_history (doc_key, eff_cost, scheme_code, invoice_id, invoice_date, cust_code, item_code,"
										    	+ " site_code, lot_no, returnable,reas_code, chg_date, chg_user, chg_term, quantity, quantity_adj,status, history_type)"//status added by nandkumar gadkari on 08/01/20
											    + " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";//history_type added by Rohini T on [18/11/2020]
									    pstmt2 = conn.prepareStatement(sql);
									    pstmt2.setString(1, lsDocValue);
									    pstmt2.setDouble(2, ldEffCost);
									    pstmt2.setString(3, itemCodeOrd);
									    pstmt2.setString(4, invoiceId);
									    pstmt2.setTimestamp(5, tranDate);
									    pstmt2.setString(6, custCode);
									    pstmt2.setString(7, itemCode);
									    pstmt2.setString(8, siteCode);
									    pstmt2.setString(9, lotNo);
									    pstmt2.setString(10, lsReturnable);
									    pstmt2.setString(11, lsReasCode);
									    pstmt2.setTimestamp(12, chgDate);
									    pstmt2.setString(13, logInCode);
									    pstmt2.setString(14, termId);
								    	//Added by Santosh on 04/01/17 to insert qty and qty_adj [Start]
								    	pstmt2.setDouble(15, invQty);
								    	pstmt2.setDouble(16, 0.0);
									    //Added by Santosh on 04/01/17 to insert qty and qty_adj End]
                                        pstmt2.setString(17,"A");//status added by nandkumar gadkari on 08/01/20
                                        pstmt2.setString(18,"S");//history_type added by Rohini T on [18/11/2020]
									    cnt = pstmt2.executeUpdate();

									    pstmt2.close();
									    pstmt2 = null;
								    } else
								    {
								    	sql = "select eff_cost from min_rate_history where doc_key = ?";
									    pstmt2 = conn.prepareStatement(sql);
									    pstmt2.setString(1, lsDocValue);
									    rs2 = pstmt2.executeQuery();
									    if (rs2.next())
									    {
									    	ldSchCost = rs2.getDouble("eff_cost");
									    }
									    pstmt2.close();
									    pstmt2 = null;
									    rs2.close();
									    rs2 = null;

									    if (ldEffCost < ldSchCost && ldEffCost != 0)
									    {
									    	sql = "update min_rate_history set eff_cost =?, scheme_code =?, chg_date =?, chg_user =?,chg_term =?,"
									    			+ "returnable = ?,reas_code =? , invoice_id= ?, invoice_date = ? where doc_key =?";//invoice_id and invoice_date Added by NANDKUMAR GADKARI on 01/10/18 
									    	pstmt2 = conn.prepareStatement(sql);
									    	pstmt2.setDouble(1, ldEffCost);
									    	pstmt2.setString(2, itemCodeOrd);
										    pstmt2.setTimestamp(3, chgDate);
										    pstmt2.setString(4, logInCode);
										    pstmt2.setString(5, termId);
										    pstmt2.setString(6, lsReturnable);
										    pstmt2.setString(7, lsReasCode);
										    pstmt2.setString(8, invoiceId);
										    pstmt2.setTimestamp(9, invoiceDate);
										    pstmt2.setString(10, lsDocValue);

										    cnt = pstmt2.executeUpdate();

										    pstmt2.close();
										    pstmt2 = null;
								    	}
								    }
							    } else
							    {
								    sql = "select count(*) as cnt from min_rate_history where doc_key = ?";
								    pstmt2 = conn.prepareStatement(sql);
								    pstmt2.setString(1, lsDocValue);
								    rs2 = pstmt2.executeQuery();
							    	if (rs2.next())
								    {
								    	cnt = rs2.getInt("cnt");
								    }
								    pstmt2.close();
								    pstmt2 = null;
								    rs2.close();
								    rs2 = null;

								    if (cnt == 0)
								    {
									    if (ldEffCost != 0)
									    {
										    sql = "insert into min_rate_history (doc_key, eff_cost, scheme_code, invoice_id, invoice_date, cust_code,"
										    		+ " item_code, site_code, lot_no, returnable,reas_code, chg_date, chg_user, chg_term, quantity, quantity_adj,status, history_type)"//status added by nandkumar gadkari on 08/01/20
										    		+ " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";//history_type added by Rohini T on [18/11/2020]
										    pstmt2 = conn.prepareStatement(sql);
										    pstmt2.setString(1, lsDocValue);
										    pstmt2.setDouble(2, ldEffCost);
										    pstmt2.setString(3, itemCodeOrd);
										    pstmt2.setString(4, invoiceId);
										    pstmt2.setTimestamp(5, tranDate);
										    pstmt2.setString(6, custCode);
										    pstmt2.setString(7, itemCode);
										    pstmt2.setString(8, siteCode);
										    pstmt2.setString(9, lotNo);
										    pstmt2.setString(10, lsReturnable);
										    pstmt2.setString(11, lsReasCode);
										    pstmt2.setTimestamp(12, chgDate);
										    pstmt2.setString(13, logInCode);
										    pstmt2.setString(14, termId);
										    //Added by Santosh on 04/01/17 to insert qty and qty_adj [Start]
										    pstmt2.setDouble(15, invQty);
										    pstmt2.setDouble(16, 0.0);
										    //Added by Santosh on 04/01/17 to insert qty and qty_adj End]
                                            pstmt2.setString(17,"A");//status added by nandkumar gadkari on 08/01/20
                                            pstmt2.setString(18,"G");//history_type added by Rohini T on [18/11/2020]
										    cnt = pstmt2.executeUpdate();

										    pstmt2.close();
										    pstmt2 = null;
									    }
								    } else
								    {
									    sql = "select eff_cost from min_rate_history where doc_key = ?";
									    pstmt2 = conn.prepareStatement(sql);
									    pstmt2.setString(1, lsDocValue);
									    rs2 = pstmt2.executeQuery();
									    if (rs2.next())
									    {
										    ldSchCost = rs2.getDouble("eff_cost");
									    }
									    pstmt2.close();
								    	pstmt2 = null;
									    rs2.close();
									    rs2 = null;

									    if (ldEffCost < ldSchCost && ldEffCost != 0)
									    {
										    sql = "update min_rate_history set eff_cost =?, scheme_code =?, chg_date =?, chg_user =?,chg_term =?,"
											    	+ " returnable = ?,reas_code =? , invoice_id= ?, invoice_date = ? where doc_key =?";//invoice_id and invoice_date Added by NANDKUMAR GADKARI on 01/10/18 
										    pstmt2 = conn.prepareStatement(sql);
										    pstmt2.setDouble(1, ldEffCost);
										    pstmt2.setString(2, itemCodeOrd);
										    pstmt2.setTimestamp(3, chgDate);
										    pstmt2.setString(4, logInCode);
										    pstmt2.setString(5, termId);
										    pstmt2.setString(6, lsReturnable);
										    pstmt2.setString(7, lsReasCode);
										    pstmt2.setString(8, invoiceId);
										    pstmt2.setTimestamp(9, invoiceDate);
										    pstmt2.setString(10, lsDocValue);

										    cnt = pstmt2.executeUpdate();

										    pstmt2.close();
										    pstmt2 = null;
									    }
								    }
							    }
                            }//Added and Commented by Rohini T on 18/11/2020 [Added condition to check invoice id is the part of doc key][End]
							if ("I".equalsIgnoreCase(tranType))
							{
								if (lsSchemeCode != null && lsSchemeCode.trim().length() > 0)
								{
									itemCodeOrd = lsOriginalItemCodeOrd;
									lsOriginalItemCodeOrd = "";
									lsSchemeCode = "";
								}
							}
						}// FOR LOOP [1] --END--
						
                    }
                    pstmtLot.close();
                    pstmtLot = null;
                    rsLot.close();
                    rsLot = null;

                }
				itemCodeOld = itemCode;
				refNoOld = refNo;
            }
            rs.close();
            rs = null;
            pstmt.close();
            pstmt = null;
        } catch (Exception e)
        {
            e.printStackTrace();
            // retString=e.toString();
            throw new ITMException(e);
        }
        return retString;
    }
	private Connection chaneParnerExist(String despId,String disLink,String xtraParams,Connection conn) throws ITMException
	{
		System.out.println("Inside chaneParnerExist...........");
		String purIntegrate="";
		Connection connCP = null;
		ConnDriver connDriver = new ConnDriver();
		DistCommon distCommon = new DistCommon();
		try
		{
			purIntegrate=distCommon.getDisparams("999999", "PUR_INTEGRATED", conn);
			if ("C".equalsIgnoreCase(disLink) )
			{
					String dirPath="";
					if ( CommonConstants.APPLICATION_CONTEXT != null )
					{
						dirPath = CommonConstants.APPLICATION_CONTEXT + CommonConstants.SETTINGS;
						System.out.println("dirPath1>>>>"+dirPath);
					}
					else
					{
						dirPath = CommonConstants.JBOSSHOME + File.separator + "server" + File.separator + "default" + File.separator + "deploy" + File.separator + "ibase.ear" + File.separator + "ibase.war" + File.separator + CommonConstants.SETTINGS;
						System.out.println("dirPath2>>>>>>"+dirPath);
					}
					File xmlFile = new File( dirPath + File.separator + "DriverITMCP" + ".xml" );
					System.out.println("xmlFile>>>>>"+xmlFile);
					if(xmlFile.exists())
					{
							System.out.println("file exist new connection is creating");
							connCP = connDriver.getConnectDB("DriverITMCP");
							return connCP;
					}
				}
			
		
		}catch(Exception e)
		{
			System.out.println("Exception :conf ::" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return connCP;
   	}
	
	// Added By PriyankaC update scheme_balance as per the nature  [Start] 09OCt2018.
	
	public String updateSchemBalance(String invoiceId,String xtraParams,Connection conn) throws ITMException, Exception
	{
		String retString="",sql="";
		PreparedStatement pstmt=null,pstmt1=null;
		ResultSet rs=null,rs1=null;
		String itemCdParent="",itemCode="";
		int cntItem=0;
		String sordNo="",soLineNo="",custCode="";
		Timestamp tranDate=null,effFrom=null,validUpto=null,sysdate=null ,orderDate = null;
		double rate=0,quantity=0;
		double totChargeQty=0,totFreeQty=quantity,totBonusQty=0,totSampleQty=0,effNetAmt=0;
		int cntSchTrace=0;
		String orderType="",siteCode="",stateDlv="",nature="",countCodeDlv="",priceList="",schemeCode="",chgUser="",chgTerm="";
		int rowUpdate=0;
		String stdSoPL="";
		PreparedStatement pstmt9=null,pstmt90=null ,pstmt8 = null; 
		ResultSet rs8=null,rs9=null; 
		//double quentityschm =0,freeValue=0, rateschm=0 ,freequentity = 0 ,balValBfr = 0 ,balQtyBfr = 0 ,balQutAfr =0 ,balValAfr = 0 ,RembalVAl =0,RembalQty =0 ,balFreeVal = 0 ,balFreeQty=0;
		String tranId = "" ,tranIdbal = "", invLineType = ""; // Added By PriyankaC on 7OCt2018.
		int update = 0 ,invLineNo= 0,cnt=0,cnt1=0; // Added By PriyankaC on 7OCt2018.
		String sordNoschm = "",lotNoschm = "" , priceListschm ="",itemCodeschm ="" ,rtlSchmRateBase="",mrpPriceList = "" , errString = ""; // Added By PriyankaC2018
		String stateCodeDlv="",schemeCodePur="",schemeCodeOffer="",itemCodeOrd="",sordNoDet="",schemeCode1="";//added by nandkumar gadkari on 31/05/19
		double offerPoints=0,totalpoints=0,freePoints=0,reqPoints=0,totalusedpoints=0,quantityStduom=0,balFreeValue=0;//added by nandkumar gadkari on 31/05/19
	double schUseFreeVal=0,schBalFreeVal=0;
		try
		{
			GenericUtility genericUtility = GenericUtility.getInstance();
			SimpleDateFormat sdf=new SimpleDateFormat(genericUtility.getApplDateFormat());
			Date currentDateval = new Date();
			sysdate=new Timestamp(currentDateval.getTime());
			chgUser = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
			chgTerm = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId"));
			sql="select tran_date, cust_code from invoice "
					+ " where invoice_id = ?";
			
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1,invoiceId);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				tranDate=rs.getTimestamp("tran_date");
				custCode=rs.getString("cust_code");
			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;
			 
			sql = "select a.sord_line_no, a.sord_no,	a.item_code,sum( a.quantity__stduom )as quantity, "
					//+"  (a.quantity__stduom * a.rate__stduom) as effNetAmt,"
				//	+ " b.item_code__parent , "
					+"  case when trim(a.line_type) in ('I' , 'V') then a.line_type else '0' end as inv_line_type  from invoice_trace a, item b " 
	                +"  where  a.item_code = b.item_code  and (b.item_code__parent is not null  or a.line_type in ('I','V'))  "
	                +"  and a.invoice_id = ? group by a.sord_no, a.sord_line_no, a.item_code, a.item_code__ord,  "
	               // + "b.item_code__parent ,a.quantity__stduom,"
	                + "a.rate__stduom, " 
	                +"   case when trim(a.line_type) in ('I' , 'V') then a.line_type else '0' end  order by a.sord_no, a.sord_line_no ";
			
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1,invoiceId);
			rs=pstmt.executeQuery();
			int count =0 ;

			while(rs.next())
			{
                System.out.println("count loop : " +count);
                count++;
				double quentityschm =0,freeValue=0, rateschm=0 ,freequentity = 0 ,balValBfr = 0 ,balQtyBfr = 0 ,balQutAfr =0 ,balValAfr = 0 ,RembalVAl =0,RembalQty =0 ,balFreeVal = 0 ,balFreeQty=0;
			//	itemCdParent=checkNullAndTrim(rs.getString("item_code__parent"));
				itemCode=checkNullAndTrim(rs.getString("item_code"));
				sordNo=checkNullAndTrim(rs.getString("sord_no"));
				soLineNo=checkNull(rs.getString("sord_line_no"));
				quantity=rs.getDouble("quantity");
			//	effNetAmt=rs.getDouble("effNetAmt");
				invLineType=checkNullAndTrim(rs.getString("inv_line_type"));
				
				
				sql=" select a.order_type, a.site_code, a.state_code__dlv, b.nature, a.count_code__dlv, a.order_date,a.price_list "
						+ "  from sorder a,sorddet b "
						+ " where a.sale_order = b.sale_order "
						+ " and b.sale_order= ? "
						+ " and b.line_no= ?"; 
				pstmt1=conn.prepareStatement(sql);
				pstmt1.setString(1,sordNo);
				pstmt1.setString(2,soLineNo);
				rs1=pstmt1.executeQuery();
				if(rs1.next())
				{
					orderType=checkNullAndTrim(rs1.getString("order_type"));
					orderDate= (rs1.getTimestamp("order_date"));
					siteCode=checkNullAndTrim(rs1.getString("site_code"));
					stateDlv=checkNullAndTrim(rs1.getString("state_code__dlv"));
					nature=checkNullAndTrim(rs1.getString("nature"));
					countCodeDlv=checkNullAndTrim(rs1.getString("count_code__dlv"));
					priceList=checkNullAndTrim(rs1.getString("price_list"));
				}
				rs1.close();
				rs1=null;
				pstmt1.close();
				pstmt1=null;
				// = checkScheme(itemCdParent,orderType, custCode,siteCode,stateDlv,countCodeDlv,tranDate,conn);
			//	String temp=checkNullAndTrim(schemeCode);
				System.out.println("schemeCode:::::::::::::::::"+schemeCode);	
				
				
				System.out.println("Nature Type:::::::::::::::::"+nature);	
				if("V".equalsIgnoreCase(nature) || "I".equalsIgnoreCase(nature))
				{
					sql = "select tran_id ,used_free_value ,used_free_qty,balance_free_value,balance_free_qty,scheme_code from scheme_balance where cust_code = ? and Item_code = ? and  ? >= EFF_FROM and  ? <= VALID_UPTO  for update nowait ";

					pstmt9=conn.prepareStatement(sql);
					pstmt9.setString(1,custCode);
					if("I".equalsIgnoreCase(nature))
					{
						pstmt9.setString(2,itemCode);
					}
					else
					{
						pstmt9.setString(2,"X");

					}
					pstmt9.setTimestamp(3,orderDate);
					pstmt9.setTimestamp(4,orderDate);
					rs8=pstmt9.executeQuery();
					if(rs8.next())
					{
						tranIdbal  =   checkNullAndTrim(rs8.getString("tran_id"));
						balValBfr =    rs8.getDouble("used_free_value");
						balQtyBfr =    rs8.getDouble("used_free_qty");
						schemeCode  =  checkNullAndTrim(rs8.getString("scheme_code"));
						balFreeVal  =  rs8.getDouble("balance_free_value");
						balFreeQty  =  rs8.getDouble("balance_free_qty");
					}
					rs8.close();
					rs8=null;
					pstmt9.close();
					pstmt9=null;


					rtlSchmRateBase = checkNullAndTrim(distCommon.getDisparams("999999", "RETL_SCHM_RATE_BASE", conn));

					quentityschm = 0;
					freeValue=0 ;
					rateschm=0 ;
					freequentity = 0;

					sql =  "update scheme_balance set  used_free_qty = ?, used_free_value  = ? where cust_code = ? and Item_code = ? and  ? >= EFF_FROM and  ? <= VALID_UPTO ";
					pstmt9=conn.prepareStatement(sql);

					sql =    " Insert into FREE_BALANCE_TRACE (tran_id, bal_qty_before, bal_qty_after ,val_bal_before ,val_bal_after ,sale_order , invoice_id , tran_id__bal ,cust_code , line_no__sord ,item_code  , used_qty, used_amount, scheme_code,line_no_invoicetrace) "
							+" values (?, ?, ?, ?,?,?, ?,?,?,?,?, ?,?,?,?)" ;
					pstmt90 = conn.prepareStatement(sql);

					sql=" select a.sord_no,a.lot_no , b.price_list ,a.item_code	,a.quantity__stduom ,a.rate__stduom, a.inv_line_no "
							+ "	  "
							+ " from invoice_trace a , sorder b "
							+ " where   "
							+  "a.sord_line_no = ? and "
							+ " a.sord_no = b.sale_order "  
							+ " and a.invoice_id = ? order by a.inv_line_no ";


					pstmt8=conn.prepareStatement(sql);
					pstmt8.setString(1,soLineNo);
					pstmt8.setString(2,invoiceId);
					rs8=pstmt8.executeQuery();
					while(rs8.next())
					{
						sordNoschm=checkNullAndTrim(rs8.getString("sord_no"));
						quentityschm=rs8.getDouble("quantity__stduom");
						System.out.println("quentityschm :" +quentityschm);
						lotNoschm=checkNullAndTrim(rs8.getString("lot_no"));
						priceListschm=checkNullAndTrim(rs8.getString("price_list"));
						rateschm = rs8.getDouble("rate__stduom") ;
						itemCodeschm=checkNullAndTrim(rs8.getString("item_code"));
						invLineNo = rs8.getInt("inv_line_no");

						double schmUsedval= 0, schmUsedqut = 0;

						if("V".equalsIgnoreCase(nature))
						{
							if("M".equalsIgnoreCase(rtlSchmRateBase))
							{
								mrpPriceList = checkNullAndTrim(distCommon.getDisparams("999999", "MRP", conn));
								priceListschm= mrpPriceList;

								rateschm = distCommon.pickRate(priceListschm, sdf.format(sysdate), itemCodeschm, lotNoschm, "B", conn);
								if(rateschm < 0)
								{
									rateschm = 0;
								}
							}
							else  
							{
								rateschm = distCommon.pickRate(priceListschm,sdf.format(sysdate), itemCodeschm, "", "L", quentityschm, conn);

								if(rateschm < 0)
								{
									rateschm = 0;
								}
							} 
							schmUsedqut = 0;
							schmUsedval =   quentityschm  * rateschm ;

							RembalVAl   =  balFreeVal - balValBfr;
							System.out.println("RembalVAl : balFreeVal " +RembalVAl +" : " +balValBfr);

							if(schmUsedval > RembalVAl)
							{
								retString = itmDBAccessEJB.getErrorString("","VTFREEQTY1","","",conn);
								break;
							}
						}

						else if("I".equalsIgnoreCase(nature)) 
						{
							schmUsedval= 0;
							schmUsedqut = quentityschm;
                            RembalQty = balFreeQty - balQtyBfr;
							if(quentityschm > RembalQty)
							{
								retString = itmDBAccessEJB.getErrorString("","VTFREEQTY1","","",conn);
								break;
							}
						}

						balQutAfr = schmUsedqut +  balQtyBfr ;
						balValAfr = schmUsedval  +  balValBfr ;
						System.out.println("balQtyBfr1 :  : schmUsedval" +balQtyBfr +" :" +schmUsedval +" "+balQutAfr);
						System.out.println("balValBfr 1:  : schmUsedval" +balValBfr +" :" +schmUsedval +" "+" "+balQutAfr);
						//System.out.println("balQutAfr : balValAfr " +balQutAfr +" : " +balValAfr);
						pstmt9.setDouble(1,balQutAfr);
						pstmt9.setDouble(2,balValAfr);
						pstmt9.setString(3,custCode);
						if("V".equalsIgnoreCase(nature))
						{
							pstmt9.setString(4,"X");

						}
						else
						{
							pstmt9.setString(4,itemCode);
						}
						pstmt9.setTimestamp(5,orderDate);
						pstmt9.setTimestamp(6,orderDate);
						update = pstmt9.executeUpdate();
						pstmt9.clearParameters();
						System.out.println("Update Successfully");
						
						// Generate tran id and insert sql FREE_BALANCE_TRACE..
						tranId = generateTranId("t_free_balance_trace",siteCode,conn);

						System.out.println("tranId  :" +tranId);

						pstmt90.setString(1,tranId);
						pstmt90.setDouble(2,balQtyBfr);
						pstmt90.setDouble(3,balQutAfr);
						pstmt90.setDouble(4,balValBfr);
						pstmt90.setDouble(5,balValAfr);
						pstmt90.setString(6,sordNo);
						pstmt90.setString(7,invoiceId);
						pstmt90.setString(8,tranIdbal);
						pstmt90.setString(9,custCode);
						pstmt90.setString(10,soLineNo);
						pstmt90.setString(11,itemCodeschm);
						pstmt90.setDouble(12,quantity);
						pstmt90.setDouble(13,freeValue);
						pstmt90.setString(14,schemeCode);
						pstmt90.setInt(15,invLineNo);
						
						update = pstmt90.executeUpdate();
						pstmt90.clearParameters();
						
						balValBfr = schmUsedval  +  balValBfr ;
						balQtyBfr = schmUsedqut +  balQtyBfr ;
						System.out.println("No recore Insert FREE_BALANCE_TRACE ::- ["+update+"]");
						
						System.out.println("balQtyBfr2 :  : schmUsedval" +balQtyBfr +" :" +schmUsedval);
						System.out.println("balValBfr 2:  : schmUsedval" +balValBfr +" :" +schmUsedval);
				}										
					if (rs8 != null)
					{
						rs8.close();
						//rs9=null;
						rs8=null; //[nulled rs8 by Pavan R]
					}
					if (pstmt8 != null)	{
						pstmt8.close();						
						pstmt8=null; //[nulled rs8 by Pavan R]
					}
					if (pstmt9 != null)
					{
						pstmt9.close();
						pstmt9=null;
					}
					if (pstmt90 != null)
					{
						pstmt90.close();
						pstmt90=null;
					}
				}
			}	
			rs.close();
			rs=null;
			pstmt.close();
			pstmt=null;
			//added by nandkumar gadkari on 31/05/19---------------for point base scheme -------------Start-----------
			sql =    " Insert into FREE_BALANCE_TRACE (tran_id, bal_qty_before, bal_qty_after ,val_bal_before ,val_bal_after ,sale_order , invoice_id , tran_id__bal ,cust_code , line_no__sord ,item_code  , used_qty, used_amount, scheme_code,line_no_invoicetrace) "
					+" values (?, ?, ?, ?,?,?, ?,?,?,?,?, ?,?,?,?)" ;
			pstmt90 = conn.prepareStatement(sql);
			
			sql=" select sord_line_no, sord_no,	 item_code, quantity__stduom, line_type,inv_line_no,line_no from invoice_trace where invoice_id =?  "
				//	+ " order by INV_LINE_NO ";
					+ " order by line_no ";
			pstmt8=conn.prepareStatement(sql);
			pstmt8.setString(1,invoiceId);
			rs8=pstmt8.executeQuery();
			while(rs8.next())
			{
				
				itemCodeOrd=checkNullAndTrim(rs8.getString("item_code"));
				sordNoDet=checkNullAndTrim(rs8.getString("sord_no"));
				soLineNo=checkNull(rs8.getString("sord_line_no"));
				quantityStduom=rs8.getDouble("quantity__stduom");
				nature=checkNull(rs8.getString("line_type"));
				invLineNo=(rs8.getInt("line_no"));// added by nandkyumar gadkari on 16/12/19
				
				sql = "select order_date,state_code__dlv,count_code__dlv,site_code from sorder where sale_order = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,sordNoDet);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
	
					orderDate = rs.getTimestamp(1);
					stateCodeDlv = rs.getString(2);
					countCodeDlv = rs.getString(3);
					siteCode = rs.getString(4);
					
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;
					
					cnt= 0;
					sql = "select a.scheme_code from scheme_applicability a,scheme_applicability_det  b "
							+ " where a.scheme_code= b.scheme_code and a.app_from <= ? and a.valid_upto>= ? "
							+ " and (b.site_code= ? or b.state_code = ?  or b.count_code= ?) and PROD_SCH = ?";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setTimestamp(1, orderDate);
					pstmt1.setTimestamp(2, orderDate);
					pstmt1.setString(3, siteCode);
					pstmt1.setString(4, stateCodeDlv);
					pstmt1.setString(5, countCodeDlv);
					pstmt1.setString(6, "Y");
					rs1 = pstmt1.executeQuery();
					while (rs1.next()) {
						schemeCode1 = rs1.getString("scheme_code");
					
						if(schemeCode1 !=null && schemeCode1.trim().length() > 0)
						{
							if("C".equalsIgnoreCase(nature) )
							{
							
								sql = "select count (*) as cnt from SCH_PUR_ITEMS  where SCHEME_CODE =? and item_code=?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, schemeCode1);
								pstmt.setString(2, itemCodeOrd);
								rs = pstmt.executeQuery();
								if (rs.next()) 
								{
									cnt = rs.getInt("cnt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
									
								if(cnt==0)
								{
									continue;
								}
								else {
									cnt1++;
									schemeCode=schemeCode1;
								}
															
							}
							if("P".equalsIgnoreCase(nature) )
							{
								sql = "select count (*) as cnt from SCH_OFFER_ITEMS  where SCHEME_CODE =? and item_code=?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, schemeCode1);
								pstmt.setString(2, itemCodeOrd);
								rs = pstmt.executeQuery();
								if (rs.next()) 
								{
									cnt = rs.getInt("cnt");
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
									
								if(cnt==0)
								{
									continue;
								}
								else {
									cnt1++;
									schemeCode=schemeCode1;
								}
							}
							
						}		
								
						}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1= null;
					if(cnt1 > 0)
					{
						
						// added by nandkumar gadkari on 16/12/19---------------start----------
						sql = "SELECT BALANCE_FREE_VALUE,USED_FREE_VALUE FROM SCHEME_BALANCE  WHERE   CUST_CODE = ?  AND ITEM_CODE= ? and  ? >= EFF_FROM and  ? <= VALID_UPTO  ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, custCode);
						pstmt.setString(2, "X");
						pstmt.setTimestamp(3, orderDate);
						pstmt.setTimestamp(4, orderDate);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							schBalFreeVal = rs.getDouble(1);
							schUseFreeVal = rs.getDouble(2);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						// added by nandkumar gadkari on 16/12/19---------------end----------
						
						
						if("C".equalsIgnoreCase(nature) )
						{
							sql = "select offer_points from SCH_PUR_ITEMS  where SCHEME_CODE =? and item_code=?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, schemeCode);
							pstmt.setString(2, itemCodeOrd);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								offerPoints = rs.getDouble(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							schemeCodePur=schemeCode;
							totalpoints =totalpoints + (quantityStduom * offerPoints);
							tranId = generateTranId("t_free_balance_trace",siteCode,conn);

							System.out.println("tranId  :" +tranId);

							pstmt90.setString(1,tranId);
							pstmt90.setDouble(2,0);
							pstmt90.setDouble(3,0);
							//pstmt90.setDouble(4,(quantityStduom * offerPoints));
							//pstmt90.setDouble(5,totalpoints);
							pstmt90.setDouble(4,schBalFreeVal);
							pstmt90.setDouble(5,schBalFreeVal + totalpoints);
							pstmt90.setString(6,sordNoDet);
							pstmt90.setString(7,invoiceId);
							pstmt90.setString(8,"");
							pstmt90.setString(9,custCode);
							pstmt90.setString(10,soLineNo);
							pstmt90.setString(11,itemCodeOrd);
							pstmt90.setDouble(12,quantityStduom);
							pstmt90.setDouble(13,offerPoints);
							pstmt90.setString(14,schemeCode);
							pstmt90.setInt(15,invLineNo);

							update = pstmt90.executeUpdate();
							pstmt90.clearParameters();
						}
						if("P".equalsIgnoreCase(nature) )
						{
							sql = "select required_points from SCH_OFFER_ITEMS  where SCHEME_CODE =? and item_code=?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, schemeCode);
							pstmt.setString(2, itemCodeOrd);
							rs = pstmt.executeQuery();
							if (rs.next()) 
							{
								reqPoints = rs.getDouble(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							schemeCodeOffer=schemeCode;
							totalusedpoints =totalusedpoints + integralPartQty(quantityStduom * reqPoints);
							tranId = generateTranId("t_free_balance_trace",siteCode,conn);

							System.out.println("tranId  :" +tranId);

							pstmt90.setString(1,tranId);
							pstmt90.setDouble(2,0);
							pstmt90.setDouble(3,0);
							//pstmt90.setDouble(4, integralPartQty(quantityStduom * reqPoints));
							//pstmt90.setDouble(5,totalusedpoints);
							pstmt90.setDouble(4, schUseFreeVal);
							pstmt90.setDouble(5, schUseFreeVal + totalusedpoints);
							pstmt90.setString(6,sordNoDet);
							pstmt90.setString(7,invoiceId);
							pstmt90.setString(8,"");
							pstmt90.setString(9,custCode);
							pstmt90.setString(10,soLineNo);
							pstmt90.setString(11,itemCodeOrd);
							pstmt90.setDouble(12,quantityStduom);
							pstmt90.setDouble(13,reqPoints);
							pstmt90.setString(14,schemeCode);
							pstmt90.setInt(15,invLineNo);

							update = pstmt90.executeUpdate();
							pstmt90.clearParameters();
							
						}
					}
			}										
			if (rs8 != null)
			{
				rs8.close();
				//rs9=null;
				rs8=null; //[nulled rs8 by Pavan R]
			}
			if (pstmt8 != null)	{
				pstmt8.close();						
				pstmt8=null; //[nulled rs8 by Pavan R]
			}
			if (pstmt90 != null)
			{
				pstmt90.close();
				pstmt90=null;
			}
			
			
			
			if(totalpoints > 0)
			{
				sql = "SELECT count (*) as cnt FROM SCHEME_BALANCE  WHERE   CUST_CODE = ?  AND ITEM_CODE= ? and  ? >= EFF_FROM and  ? <= VALID_UPTO  ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, custCode);
				pstmt.setString(2, "X");
				pstmt.setTimestamp(3, orderDate);
				pstmt.setTimestamp(4, orderDate);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					cnt = rs.getInt("cnt");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
					
				if(cnt==0)
				{
					sql = "select APP_FROM,VALID_UPTO from scheme_applicability  WHERE  SCHEME_CODE= ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, schemeCode);
					rs = pstmt.executeQuery();
					if (rs.next()) 
					{
						effFrom = rs.getTimestamp(1);
						validUpto = rs.getTimestamp(2);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					String transId = generateTranId("w_schema_balance",siteCode,conn);
					StringBuilder schemebalsb = new StringBuilder();
					schemebalsb.append("INSERT INTO SCHEME_BALANCE(TRAN_ID,SCHEME_CODE,CUST_CODE,ITEM_CODE,EFF_FROM,VALID_UPTO,BALANCE_FREE_QTY,");
					schemebalsb.append("BALANCE_FREE_VALUE,USED_FREE_QTY,USED_FREE_VALUE,CHG_USER,CHG_DATE,CHG_TERM,ENTRY_SOURCE, SITE_CODE)");
					schemebalsb.append("VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
					pstmt90 = conn.prepareStatement(schemebalsb.toString());
					pstmt90.setString(1, transId);
					pstmt90.setString(2, schemeCode);
					pstmt90.setString(3, custCode);
					pstmt90.setString(4, "X");
					pstmt90.setTimestamp(5, effFrom);
					pstmt90.setTimestamp(6, validUpto);
					pstmt90.setDouble(7, 0);
					pstmt90.setDouble(8, 0);
					pstmt90.setDouble(9, 0);
					pstmt90.setDouble(10,0);
					pstmt90.setString(11, chgUser);
					pstmt90.setTimestamp(12, sysdate);
					pstmt90.setString(13, chgTerm);
					pstmt90.setString(14, "A");
					pstmt90.setString(15,siteCode);
					count = pstmt90.executeUpdate();
					if(pstmt90!=null)
					{
						pstmt90.close();
						pstmt90 = null;
					}
				}
				
				sql="UPDATE SCHEME_BALANCE SET BALANCE_FREE_VALUE = BALANCE_FREE_VALUE + ?  WHERE CUST_CODE = ?  AND ITEM_CODE= ? AND ? >= EFF_FROM and  ? <= VALID_UPTO";
				pstmt1=conn.prepareStatement(sql);
				pstmt1.setDouble(1, totalpoints);
				pstmt1.setString(2, custCode);
				pstmt1.setString(3, "X");
				pstmt1.setTimestamp(4, orderDate);
				pstmt1.setTimestamp(5, orderDate);
				pstmt1.executeUpdate();
				if (pstmt1 != null){
					pstmt1.close();
					pstmt1=null;
				}
			}
			if(totalusedpoints > 0)
			{
				
				sql = "select balance_free_value-used_free_value from scheme_balance where  cust_code = ? "
						+ "  and Item_code = ? and  ? >= EFF_FROM and  ? <= VALID_UPTO  for update nowait ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, custCode);
				pstmt.setString(2, "X");
				pstmt.setTimestamp(3, orderDate);
				pstmt.setTimestamp(4, orderDate);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					balFreeValue = rs.getDouble(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
				if(totalusedpoints > balFreeValue )
				{
					retString = itmDBAccessEJB.getErrorString("","VTFREEPOIN","","",conn);
				}
				else {
					sql="UPDATE SCHEME_BALANCE SET USED_FREE_VALUE = USED_FREE_VALUE + ?  WHERE  CUST_CODE = ?  AND ITEM_CODE= ? AND ? >= EFF_FROM and  ? <= VALID_UPTO";
					pstmt1=conn.prepareStatement(sql);
					pstmt1.setDouble(1, totalusedpoints);
					pstmt1.setString(2, custCode);
					pstmt1.setString(3, "X");
					pstmt1.setTimestamp(4, orderDate);
					pstmt1.setTimestamp(5, orderDate);
					pstmt1.executeUpdate();
					if (pstmt1 != null){
						pstmt1.close();
						pstmt1=null;
					}
				}
			}
		//added by nandkumar gadkari on 31/05/19---------------for point base scheme -------------end-----------
			
			
		}catch(Exception e)
		{
			System.out.println("Exception :conf ::" + e.getMessage() + ":");
			e.printStackTrace();
			// retString=e.toString();
			throw new ITMException(e);
		}
		finally //finally block added closed curesor and pstmt inside  
		{
			try
			{									
				if (rs != null) {
					rs.close();					
					rs=null;
				}
				if (pstmt != null) {
					pstmt.close();					
					pstmt = null;
				}
				if (rs1 != null) {
					rs1.close();					
					rs1	=null;
				}
				if (pstmt1 != null) {
					pstmt1.close();					
					pstmt1=null;
				}
				if (rs8 != null) {
					rs8.close();				
					rs8=null;
				}
				if (pstmt8 != null)	{
					pstmt8.close();						
					pstmt8=null;
				}
				if (rs9 != null) {
					rs9.close();					
					rs9	=null;
				}
				if (pstmt9 != null) {
					pstmt9.close();
					pstmt9=null;
				}
				if (pstmt90 != null) {
					pstmt90.close();
					pstmt90=null;
				}
			}
			catch(Exception ef)
			{				
				ef.printStackTrace();				
			}

		}
		return retString;
	}
	private double integralPartQty(double value) {
		double fractionalPart = value % 1;
		double integralPart = value - fractionalPart;
		System.out.println(integralPart +" integralPart     "+ fractionalPart);
		return integralPart;
	}
	// Added By PriyankaC update scheme_balance as per the nature  [END].
}