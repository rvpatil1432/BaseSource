package ibase.webitm.ejb.dis;
import ibase.scheduler.utility.interfaces.Schedule;

import java.rmi.RemoteException;
import java.util.*;
import java.util.Date;
import java.sql.*;
import java.io.*;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import org.omg.CORBA.ORB;
import org.w3c.dom.*;

import java.util.Properties;

import javax.swing.text.NumberFormatter;
import javax.xml.parsers.*;
import javax.xml.rpc.ParameterMode;
import javax.ejb.*;
import javax.naming.InitialContext;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.webitm.ejb.fin.GenerateReceiptPrc;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.TransIDGenerator;
import ibase.utility.BaseException;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.ejb.*;
import ibase.system.config.*;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.math.*;
import java.net.InetAddress;

import ibase.webitm.ejb.sys.UtilMethods;

public class PlistGenFrmDistIssueSch implements Schedule
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	FileOutputStream fos1 = null;
	Calendar calendar = Calendar.getInstance();
	java.util.Date startDate = new java.util.Date(System.currentTimeMillis());
	String startDateStr = null;
	String exclItemSerArr[];
	String finEntityArr[];
	ArrayList<String> exclItmSerList=new ArrayList<String>();
	ArrayList<String> finEntityList=new ArrayList<String>();
	 String autoConfReqd="",validUpto="",exclSeries="",finEntStr="",itemLotReqd="",defSiteOwn="";
	@Override
	public String schedule(String scheduleParamXML)throws Exception,ITMException
	{
		ibase.utility.UserInfoBean userInfo = null;
		String loginSiteCode="";
		try
		{
			Document dom = null;
			String xtraParams = "";
			
			Node currDetail = null ;
			int noOfParam=0;
			System.out.println("************ ["+scheduleParamXML+"]");
			userInfo = new ibase.utility.UserInfoBean( scheduleParamXML );
			loginSiteCode = userInfo.getSiteCode();
			System.out.println("intializingLog$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$........."+ intializingLog("Price_list_log"));
			System.out.println("Site code======**************====> "+loginSiteCode);
			xtraParams = "loginEmpCode="+userInfo.getEmpCode()+"~~loginCode="+userInfo.getLoginCode()+"~~termId="+userInfo.getRemoteHost()+"~~loginSiteCode="+loginSiteCode;
			System.out.println("xtraParams---["+xtraParams+"]");
		    //GenericUtility genericUtility=GenericUtility.getInstance();
			System.out.println("scheduleParamXML-----"+scheduleParamXML);
			priceListConfirm(xtraParams);	
				
		}
		catch (Exception e) 
		{

			System.out.println("Exception :SplitSchedularSOrder :schedule :Exception :==>\n"+e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
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
				System.out.println( "Exception"+e.getMessage());

			}
		}
		return "";
	}
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
				fileName = fileName+currTime+calendar.get(Calendar.HOUR)+""+calendar.get(Calendar.MINUTE)+".csv";
				fos1 = new FileOutputStream(CommonConstants.JBOSSHOME + File.separator +"EDI"+File.separator+fileName);
				//strToWrite="\"TRANID\",\"START TIME\",\"END TIME\",\"STATUS\"\r\n";
				//fos1.write(strToWrite.getBytes());
			}
			catch(Exception e)
			{
				System.out.println("Exception []::"+e.getMessage());
				e.printStackTrace();
			}
			startDate = new java.util.Date(System.currentTimeMillis());
			calendar.setTime(new java.sql.Timestamp(System.currentTimeMillis()));
			startDateStr = sdf1.format(startDate)+" "+calendar.get(Calendar.HOUR)+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND);
			fos1.write(("Fetching Records Started At " + startDateStr +"\r\n").getBytes());

		}
		catch(Exception e)
		{
			System.out.println("Exception []::"+e.getMessage());
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}

		log ="intializingLog_Successesfull";
		return log;
	}
	private String generateTranId(String windowName, String tranDate, String siteCode ,String signBy,Connection conn) throws ITMException
	{
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String generateTranIdSql = null;
		String tranId = null;
		String xmlValues = null;
		StringBuffer xmlValuesBuff = new StringBuffer();
		String refSer = "";
		String keyString = "";
		String tranIdCol = "";
		 try
	     {
			
			generateTranIdSql = "SELECT KEY_STRING, TRAN_ID_COL, REF_SER FROM TRANSETUP WHERE TRAN_WINDOW= ?";
			pstmt = conn.prepareStatement( generateTranIdSql );
			pstmt.setString(1, windowName);
			rs = pstmt.executeQuery();

			if( rs.next() )
			{
				keyString = rs.getString("KEY_STRING");
				tranIdCol = rs.getString("TRAN_ID_COL");
				refSer = rs.getString("REF_SER");
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;
			System.out.println("[Output of Tran generator Sql ][keyString]["+keyString+"][tranIdCol]["+tranIdCol+"][refSer]["+refSer+"]");	
			
			xmlValuesBuff.append("<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>");
			xmlValuesBuff.append("<Header></Header>");
			xmlValuesBuff.append("<Detail1>");
			xmlValuesBuff.append("<tran_id></tran_id>");
			xmlValuesBuff.append("<site_code><![CDATA["+siteCode+"]]></site_code>");
			xmlValuesBuff.append("<tran_date><![CDATA["+tranDate+"]]></tran_date>");
			xmlValuesBuff.append("</Detail1></Root>");
			xmlValues = xmlValuesBuff.toString();
			System.out.println("xmlValues  :[" + xmlValues + "]");
			TransIDGenerator tg = new TransIDGenerator(xmlValues,signBy, CommonConstants.DB_NAME);
			tranId = tg.generateTranSeqID(refSer, tranIdCol, keyString, conn);
			System.out.println("tranId :"+tranId);
			
		}
		catch (SQLException ex)
		{			
			ex.printStackTrace();
		}
		catch (Exception e)
		{		
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		finally
		{
			try
			{
				
				if( rs != null )
				{
					rs.close();
					rs = null;
				}
				if( pstmt != null )
				{
					pstmt.close();
					pstmt = null;
				}

			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return tranId;
	}
	private String checkNull(String input)
	{
		if (input == null)
		{
			input = "";
		}
		return input;
	}
	 public void priceListConfirm(String xtraParams) throws ITMException
	 {
		 ConnDriver connDriver = new ConnDriver();
		 Connection conn = null;
		 PreparedStatement pstmt=null,pstmt1=null,pstmt2=null;
		 ResultSet rs=null,rs1=null,rs2=null;
		 
		 String sql="", sql1="",sql2="";
		 String tranId="",retString="",priceListId="",userId="",loginSite="",sysDate="",termId="",empCode="",distOrder="",itemCode="",unit="",lotNo="",
				 lotSl="",rateClg="",tranType="",siteCodedlv="";
		 double quantity=0.0f,amount=0.0f;
		 int count=0,lineNo=0,insertDtl=0,insertGenCd=0;
		 
		 String siteCode="";
		 userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
		 loginSite = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
		 termId = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "termId" );
		 empCode = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginEmpCode" );
		 DistCommon distCommon=null;
		 String revTran="N";
		 int countRec=0;
		 String revTranStr="";
		 try
		 {
			 distCommon=new DistCommon();
			 conn = connDriver.getConnectDB("DriverITM");
			 //conn = getConnection();
			 conn.setAutoCommit(false);
			 connDriver = null;
			 validUpto=checkNull(distCommon.getDisparams("999999","VALIDUPTO_DT", conn));
			 exclSeries=checkNull(distCommon.getDisparams("999999","EXCL_SER_PLIST", conn));
			 finEntStr=checkNull(distCommon.getDisparams("999999","FINENT_PLIST", conn));
			 autoConfReqd=checkNull(distCommon.getDisparams("999999","AUTO_CONF_PLIST", conn));
			 itemLotReqd=checkNull(distCommon.getDisparams("999999","ITEM_LOTPACK_REQD", conn));
			 defSiteOwn=checkNull(distCommon.getDisparams("999999","DEF_SITE_OWN", conn));		 
			
			 java.util.Date today=new java.util.Date();
			 Calendar cal = Calendar.getInstance(); 
			 cal.setTime(today); 
			 today = cal.getTime();
			 SimpleDateFormat sdf=new SimpleDateFormat(genericUtility.getApplDateFormat());
			 sysDate=sdf.format(today);
			 System.out.println("System date  :- ["+sysDate+"]");
			 
			 if (validUpto == null || validUpto.trim().length() == 0 || validUpto.trim().equalsIgnoreCase("NULLFOUND"))
			 {
				 SimpleDateFormat sdf1=new SimpleDateFormat(genericUtility.getDBDateFormat());				 
				 validUpto=sdf1.format(today);
			 }
			 
			 if (exclSeries == null || exclSeries.trim().length() == 0 || exclSeries.trim().equalsIgnoreCase("NULLFOUND"))
			 {
				 exclSeries="";
			 }
			 if (finEntStr == null || finEntStr.trim().length() == 0 || finEntStr.trim().equalsIgnoreCase("NULLFOUND"))
			 {
				 finEntStr="";
			 }
			 if (autoConfReqd == null || autoConfReqd.trim().length() == 0 || autoConfReqd.trim().equalsIgnoreCase("NULLFOUND"))
			 {
				 autoConfReqd="N";
			 }
			 if (itemLotReqd == null || itemLotReqd.trim().length() == 0 || itemLotReqd.trim().equalsIgnoreCase("NULLFOUND"))
			 {
				 itemLotReqd="N";
			 }
			 if (defSiteOwn == null || defSiteOwn.trim().length() == 0 || defSiteOwn.trim().equalsIgnoreCase("NULLFOUND"))
			 {
				 defSiteOwn="";
			 }
				 
			 if(exclSeries.trim().length()>0)
				{
					exclItemSerArr= exclSeries.split(",");
					exclItmSerList.addAll(Arrays.asList(exclItemSerArr));
					//exclItmSerList=new ArrayList<String>(Arrays.asList(exclItemSerArr));
					
				}
			 if(finEntStr.trim().length()>0)
			 {
				 finEntityArr=finEntStr.split(",");
				 finEntityList.addAll(Arrays.asList(finEntityArr));
				 //finEntityList=new ArrayList<String>(Arrays.asList(finEntityArr));
			 }
			
				
			
			/* sql="select tran_id,tran_type,site_code__dlv,site_code," +
			 		"case when rev__tran is null then 'N' ELSE rev__tran END as rev__tran from distord_iss where confirmed='Y' and tran_type in ('TE','TB') " +
			 		"and case when rev__tran is null then 'N' ELSE rev__tran END <>'Y'  ";*/
			 ArrayList<String> tranIdList=new ArrayList<String>();
			 sql="select tran_id from distord_iss where confirmed='Y' and tran_type in ('TE','TB') " +
				 		"and case when rev__tran is null then 'N' ELSE rev__tran END <>'Y'  ";
			 pstmt=conn.prepareStatement(sql);
			 rs=pstmt.executeQuery();
			 while(rs.next())
			 {
				 tranIdList.add(rs.getString(1)); 
			 }
		
			 rs.close();
			 rs=null;
			 pstmt.close();
			 pstmt=null;
				 System.out.println("tranIdList["+tranIdList+"]");
				 if(tranIdList.size()>0)
				 {
					 for(int k=0;k<tranIdList.size();k++)
					 {
					 tranId=tranIdList.get(k);
					 
					 pstmt1=conn.prepareStatement("select tran_id from distord_iss where tran_id=? for update nowait ");
					 pstmt1.setString(1, tranId);
					 rs1=pstmt1.executeQuery();
					 if(rs1.next())
					 {
						 tranId=rs1.getString(1); 
						 
					 }
					 rs1.close();
					 rs1=null;
					 pstmt1.close();
					 pstmt1=null;
					 
					 
						 pstmt2=conn.prepareStatement("select tran_id,tran_type,site_code__dlv,site_code,"
						 		+ " case when rev__tran is null then 'N' ELSE rev__tran END as rev__tran from distord_iss where tran_id=?   ");
						 pstmt2.setString(1, tranId);
						 rs2=pstmt2.executeQuery();
						 if(rs2.next())
						 {
							 tranType=rs2.getString("tran_type");
							 siteCodedlv=rs2.getString("site_code__dlv");
							 siteCode=rs2.getString("site_code");
							 revTran=rs2.getString("rev__tran");
							 if("N".equalsIgnoreCase(revTran))
							 {
								 priceListId=inserPriceListHdr(tranId,tranType,userId,siteCode,termId,empCode,sysDate,conn,xtraParams,siteCodedlv);
							 }
						 }
						 rs2.close();
						 rs2=null;
						 pstmt2.close();
						 pstmt2=null;					
					 }				 
				 }
						 
		 }catch(Exception e)
		 {
			 e.printStackTrace();
			 try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			throw new ITMException(e);
		 }
		 finally
		 {
			 if(rs!=null)
			 {
				 try {
					rs.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			 }
			 if(pstmt!=null)
			 {
				 try {
					pstmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			 }
			 if(conn!=null)
			 {
				 try {
					 conn.commit();
					conn.close();
					conn=null;
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			 }
					 
		 }		 
	 }
	private int insertGenCodes(String itemCode, String lotNo, String loginSite,
			String termId, String empCode, String sysDate, Connection conn) throws Exception 
	{
		int insertCount=0;
		String sql="";
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		Timestamp systemdate=null;	
		int noRecords=0;
		
		try {
			systemdate = Timestamp.valueOf(genericUtility.getValidDateString(sysDate, genericUtility.getApplDateFormat(),
					genericUtility.getDBDateFormat())+ " 00:00:00.0");
			sql="select count(1) from gencodes where fld_name='BATCH_EXEMPTED' and mod_name='W_EXC_EXEMPTED' and fld_value=?";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, itemCode.trim()+","+lotNo.trim());	
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				noRecords=rs.getInt(1);	
			}
			pstmt.close();
			pstmt=null;
			rs.close();
			rs=null;
			if(noRecords==0)
			{
				sql="insert into gencodes (fld_name, mod_name, fld_value, descr, chg_date, chg_user, chg_term,active)"
						+ " values(?,?,?,?,?,?,?,?)";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1, "BATCH_EXEMPTED");
				pstmt.setString(2, "W_EXC_EXEMPTED");
				pstmt.setString(3, itemCode.trim()+","+lotNo.trim());	
				pstmt.setString(4, itemCode.trim()+","+lotNo.trim());
				pstmt.setTimestamp(5, systemdate);
				pstmt.setString(6, empCode);
				pstmt.setString(7, termId);		
				pstmt.setString(8, "Y");		// Set as hard code 'Y' 13/JAN/16		
				insertCount=pstmt.executeUpdate();
				
				pstmt.close();
				pstmt=null;
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(ITMException itm)
		{
			itm.printStackTrace();
			throw itm; // Added By Mukesh Chauhan on 05/08/19
		}
		return insertCount;
	}

	private int checkExistPriceList(String itemCode, String unit,String lotNo,Connection conn) throws ITMException 
	{
		int count=0;
		String sql="";
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		
		try {
//			sql="select count(*) as count from pricelist where ITEM_CODE=? and price_list='MRP' AND UNIT=? AND LIST_TYPE='B' AND SLAB_NO='1'";
			sql="select count(*) as count from pricelist where ITEM_CODE=? and price_list='MRP' AND ? between  lot_no__from AND lot_no__to";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, itemCode);
//			pstmt.setString(2, unit);
			pstmt.setString(2, lotNo);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				count=rs.getInt("count");
			}
			pstmt.close();
			pstmt=null;
			rs.close();
			rs=null;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}			
		return count;
	}
	private String inserPriceListHdr(String tranId,String tranType,String userId,String siteCode,String termId,String empCode,String sysDate, Connection conn, String xtraParams,String siteCodedlv) throws Exception
	{
		String prcLstId="",sql="",sql1="",sql2="",sql3="",sql4="",sql5="";
		String refNo="",distOrder="",itemCode="",unit="",lotNo="",lotSl="",rateClg="", itmSer="",disParamValue="",retString="",packInstr="",shelfLife="";
		PreparedStatement pstmt=null,pstmt1=null,pstmt2=null,pstmt3=null,pstmt4=null, pstmt5=null;
		ResultSet rs=null,rs1=null,rs2=null,rs3=null;	
		int count=0,lineNo=0,insertGenCd=0,insertCount=0,existCount=0,packListCnt=0,maxSlabNo=0,reccount=0,exclitemSerCount=0;
		double quantity=0.0f,amount=0.0f;
		Timestamp systemdate=null,validUptoTS=null;
		boolean isUpdate=false;
		
//		DistCommon disCommon=new DistCommon();
		int rowinserted=0;		
		
		try {
			
			systemdate = Timestamp.valueOf(genericUtility.getValidDateString(sysDate, genericUtility.getApplDateFormat(),
					genericUtility.getDBDateFormat())+ " 00:00:00.0");
			validUptoTS = Timestamp.valueOf(genericUtility.getValidDateString(validUpto, genericUtility.getApplDateFormat(),
					genericUtility.getDBDateFormat())+ " 00:00:00.0");
			
			
//			prcLstId=generateTranId("w_pricelist_tran", sysDate, siteCode, userId,conn);			
//			System.out.println("Tran id generated :- ["+prcLstId+"]");
			
			String dateArray[]=sysDate.split("/");
			System.out.println("dateArray Array length :-["+dateArray.length+"]");
			refNo=dateArray[2]+dateArray[1]+dateArray[0]+"RBX";
			System.out.println("Ref no generated is :- ["+refNo+"]");
			
			/**
			 * Check all the details 
			 * contains same item_ser as disparm 'EXCL_SER_PLIST'
			 * */
			sql5="select item_ser from distord_issdet a,item b where a.item_code=b.item_code and a.tran_id=?";
			pstmt5=conn.prepareStatement(sql5);
			pstmt5.setString(1, tranId);
			rs3=pstmt5.executeQuery();			
			while(rs3.next())
			{
				reccount++;
				itmSer=rs3.getString(1);
				 if( exclItmSerList.contains(itmSer.trim()))
				 {
					 exclitemSerCount++;
				 }				
			}
			pstmt5.close();
			pstmt5=null;
			rs3.close();
			rs3=null;
			System.out.println("exclitemSerCount :- ["+exclitemSerCount+"] for tran id  :- ["+tranId+"]");
			if(reccount>exclitemSerCount)
			{
				System.out.println("Tran id for header insertion  :- ["+tranId+"]");
				/**
				 * Generate tran id (prcLstId)
				 * for pricelist_hdr
				 * */
				prcLstId=generateTranId("w_pricelist_tran", sysDate, siteCode, userId,conn);				
				System.out.println("Price list id generated :- ["+prcLstId+"] for tran id :- ["+tranId+"]");
				
			sql="INSERT INTO pricelist_hdr (tran_id, tran_date, price_list, chg_date, chg_term, chg_user, ref_no, ref_no_old)" +
					" values(?,?,?,?,?,?,?,?)";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, prcLstId);
			pstmt.setTimestamp(2, systemdate);
			pstmt.setString(3, "MRP");
			pstmt.setTimestamp(4, systemdate);
			pstmt.setString(5, termId);
			pstmt.setString(6, empCode);
			pstmt.setString(7, refNo);
//			pstmt.setString(8, refNo);
			pstmt.setString(8, "MRP");		//Set as hard code 'MRP' 13/JAN/16
			count=pstmt.executeUpdate();
			
			pstmt.close();
			pstmt=null;
			
			
			}
			System.out.println("Header generated count :- ["+count+"]");
		
		//if(count>0)
		//{
			sql1="select di.tran_id,di.dist_order,di.item_code, di.quantity, di.unit, di.lot_no, di.lot_sl, di.amount,case when di.rate__clg is null then 0 else di.rate__clg end as rate_clg,i.item_ser,di.pack_instr,i.shelf_life" +
					" from distord_issdet di,item i where di.item_code=i.item_code and tran_id=?";
			 pstmt1=conn.prepareStatement(sql1);
			 pstmt1.setString(1, tranId);
			 rs1=pstmt1.executeQuery();
			 while(rs1.next())
			 {				 
				 distOrder=checkNull(rs1.getString("dist_order"));
				 itemCode=checkNull(rs1.getString("item_code"));
				 quantity=rs1.getDouble("quantity");
				 unit=checkNull(rs1.getString("unit"));
				 lotNo=checkNull(rs1.getString("lot_no"));
				 lotSl=checkNull(rs1.getString("lot_sl"));
				 amount=rs1.getDouble("amount");
				 rateClg=rs1.getDouble("rate_clg")+"";
				 itmSer=checkNull(rs1.getString("item_ser"));
				 packInstr=checkNull(rs1.getString("pack_instr"));
				 shelfLife=checkNull(rs1.getString("shelf_life"));
				 
				 /**
				  * Check price list is exist for
				  * item code and lot no
				  * */
				 existCount=checkExistPriceList(itemCode,unit,lotNo,conn);
				 System.out.println("Pricelist Exist count :- ["+existCount+"]");
				 if(existCount==0)
				 {				 
					 /**
					  * Check item_ser exist
					  * in disparm 
					  * */
					System.out.println("exclItmSerList["+exclItmSerList+"]");
					 if( exclItmSerList.contains(itmSer.trim()))
					 {
						 System.out.println("In pricelist insert for tranId :- ["+tranId+"]");
						 
						 maxSlabNo=getMaxSlabNo(itemCode, conn);
						 maxSlabNo=maxSlabNo+1;
						 System.out.println("Max slab found :- ["+maxSlabNo+"]");
						 
						 /**
						  * Insert into pricelist 
						  * */
						 sql2="insert into pricelist (price_list, item_code,unit, list_type, slab_no, lot_no__from, lot_no__to, min_qty, max_qty, rate, rate_type," +
						 		" min_rate, chg_date, chg_term, chg_user, max_rate, ref_no, ref_no_old,eff_from,valid_upto) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
						 pstmt2=conn.prepareStatement(sql2);
							pstmt2.setString(1, "MRP");
							pstmt2.setString(2, itemCode);
							pstmt2.setString(3, unit);
							pstmt2.setString(4, "B");	
							pstmt2.setInt(5, maxSlabNo);		// set max slab no from pricelist	
							pstmt2.setString(6, lotNo);
							pstmt2.setString(7, lotNo);
							pstmt2.setDouble(8, 1);
							pstmt2.setDouble(9, 999999999);		
							pstmt2.setDouble(10, Double.parseDouble(rateClg));		
							pstmt2.setString(11, "F");			
							pstmt2.setDouble(12, 1);		
							pstmt2.setTimestamp(13, systemdate);		
							pstmt2.setString(14, termId);		
							pstmt2.setString(15, empCode);		
							pstmt2.setDouble(16, Double.parseDouble(rateClg));		
							pstmt2.setString(17, refNo);		
							pstmt2.setString(18, refNo);		
							pstmt2.setTimestamp(19, systemdate);		
							pstmt2.setTimestamp(20, validUptoTS);		
							insertCount=pstmt2.executeUpdate();
							
							pstmt2.close();
							pstmt2=null;
							
							System.out.println("pricelist insert count :- ["+insertCount+"]");
							
							if(insertCount>0)
							{
							isUpdate=true;
							}
							rowinserted=rowinserted+insertCount;
							
					 }
					 else
					 {
						 System.out.println("In pricelist_det insert");
						 lineNo++;
						 /**
						  * Insert record in 
						  * pricelist_det 
						  * */
						 
						 sql2="insert into pricelist_det (TRAN_ID,LINE_NO,ITEM_CODE,lot_no__from, lot_no__to, min_qty, max_qty,rate,rate_type,"
						 		+ " min_rate, max_rate, unit,eff_from,valid_upto,chg_ref_no)" +
									"values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
						 	pstmt2=conn.prepareStatement(sql2);
							pstmt2.setString(1, prcLstId);
							pstmt2.setInt(2, lineNo);
							pstmt2.setString(3, itemCode);
							pstmt2.setString(4, lotNo);
							pstmt2.setString(5, lotNo);
							pstmt2.setDouble(6, 0);
							pstmt2.setDouble(7, 999999999);
							pstmt2.setDouble(8, Double.parseDouble(rateClg));
							pstmt2.setString(9, "F");		
							pstmt2.setDouble(10, 1);		
							pstmt2.setDouble(11, Double.parseDouble(rateClg));		
							pstmt2.setString(12, unit);	
							pstmt2.setTimestamp(13, systemdate);		
							pstmt2.setTimestamp(14, validUptoTS);
							pstmt2.setString(15, "MRP");		//Set as hard code 'MRP' 13/JAN/16
							insertCount=pstmt2.executeUpdate();
							
							rowinserted=rowinserted+insertCount;
							
							pstmt2.close();
							pstmt2=null;
							
							System.out.println("pricelist_det insert count :- ["+insertCount+"]");
							
							 if(insertCount>0 && tranType.equalsIgnoreCase("TB"))	// 
							 {
								 /**
								  * Insert gencodes
								  * */
								 insertGenCd=insertGenCodes(itemCode,lotNo,siteCode,termId,empCode,sysDate,conn);
								 System.out.println("Gen codes insert :- ["+insertGenCd+"]");
							 }							
					 }
					 /**
					  * insert into packsize
					  * */
					 if("Y".equalsIgnoreCase(itemLotReqd))
							 {
								 sql4="select count(*) as COUNT from ITEM_LOT_PACKSIZE where item_code=? and ? between lot_no__from and lot_no__to";
								 pstmt4=conn.prepareStatement(sql4);
								 pstmt4.setString(1, itemCode);
								 pstmt4.setString(2, lotNo);
//								 pstmt4.setString(3, lotNo);
								 rs2=pstmt4.executeQuery();
								 if(rs2.next())
								 {
									 packListCnt=rs2.getInt("COUNT");
								 }
								 pstmt4.close();
								 pstmt4=null;
								 rs2.close();
								 rs2=null;
								 
								 System.out.println("ITEM_LOT_PACKSIZE count :- ["+packListCnt+"]");
								 if(packListCnt==0)
								 {
								 insertCount= insertItemLotPackSize(itemCode,lotNo,packInstr,unit,siteCodedlv,termId,empCode,sysDate,conn,shelfLife,siteCode);
								 System.out.println("ITEM_LOT_PACKSIZE insert count :- ["+insertCount+"]");
								 }
							 }				 
				 }				 				 
			 } 
			 pstmt1.close();
			 pstmt1=null;
			 rs1.close();
			 rs1=null;
			
			 /**
			  * Confirm the price list
			  * */
			 if(rowinserted > 0 || existCount > 0)
			 {
				 
				 sql3="update distord_iss set rev__tran='Y', parent__tran_id=? where tran_id=?";
				 pstmt3=conn.prepareStatement(sql3);
				 pstmt3.setString(1, prcLstId);
				 pstmt3.setString(2, tranId);
				 int updateCount=pstmt3.executeUpdate();
				 
				 pstmt3.close();
				 pstmt3=null;	
				 conn.commit();
				 System.out.println("Update count :- ["+updateCount+"]");
				 
				 if("Y".equalsIgnoreCase(autoConfReqd))
				 {					 
					 System.out.println("Transaction is commited before confirm !!");
					 retString=confirmTran("pricelist_tran",prcLstId,xtraParams,"",conn);
					 System.out.println("Confirm return :- ["+retString+"]");
					 if(retString.indexOf("VTSUCC1") <= -1 )
					 {
						 conn.rollback();						 
					 }					 
				 }			 
				 
			}
			 else
			 {
				 conn.rollback();
			 }			 
	/*	}
		else
		{
			conn.rollback();
		}*/
		 
		} catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ITMException(e);
		}
		
		return prcLstId;
		
	}
private int getMaxSlabNo(String itemCode, Connection conn) throws ITMException 
{
	int maxSlabNo=0;
	String sql="";
	PreparedStatement pstmt=null;
	ResultSet rs=null;
	
	try {
		sql="select case when max(slab_no) is null then 0 else max(slab_no) end as max_slab from pricelist where item_code=? and price_list='MRP'";
		pstmt=conn.prepareStatement(sql);
		pstmt.setString(1, itemCode);
		rs=pstmt.executeQuery();
		if(rs.next())
		{
			maxSlabNo=rs.getInt("max_slab");
		}
		pstmt.close();
		pstmt=null;
		rs.close();
		rs=null;
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
	}
	
	return maxSlabNo;
}
private int insertItemLotPackSize(String itemCode,String lotNo,String packInstr,String unit,String siteCodedlv,String termId,
		String empCode,String sysDate,Connection conn,String shelfLife,String siteCode) throws ITMException
{
	String sqlStr="",finEntity="";
	PreparedStatement pstmt=null;
	ResultSet rs=null;
	int insertCoun=0;	
	String packArray[]=null;	
	
	String unitPack="",siteCodeMfg="",siteCodeOwn="",intigralQty="1",shipperSize="1";
	Timestamp systemdate=null;
	
	try
	{
		if(packInstr.trim().length()>0)
		{
			packArray=packInstr.split(",");
			intigralQty=packArray[0];
			shipperSize=packArray[1];
		}
		else
		{
			sqlStr="select case when integral_qty is null then 0 else integral_qty end as integral_qty from siteitem where site_code=? and item_code=?";
			pstmt=conn.prepareStatement(sqlStr);
			pstmt.setString(1, siteCodedlv);
			pstmt.setString(2, itemCode);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				intigralQty=rs.getString("integral_qty");
			}
			
			pstmt.close();
			pstmt=null;
			rs.close();
			rs=null;
			
			
			sqlStr="select max(shipper_size) from item_lot_packsize where item_code=? and lot_no__from " +
					"in(select max(lot_no__from) from item_lot_packsize where item_code=?)";
					pstmt=conn.prepareStatement(sqlStr);
			pstmt.setString(1, itemCode);
			pstmt.setString(2, itemCode);
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				shipperSize=rs.getString(1);
			}
			
			pstmt.close();
			pstmt=null;
			rs.close();
			rs=null;
		}
		/**
		 * Get unit pack
		 * from unit and intigralQty
		 * */		
		unitPack=getUnitPack(unit,intigralQty,conn);		
		
		/**
		 * check siteCodeDlv exist in
		 * DIEPARM 'FINENT_PLIST'
		 * */
		sqlStr="select fin_entity from site where site_code=?";
		pstmt=conn.prepareStatement(sqlStr);
//		pstmt.setString(1, siteCodedlv);
		pstmt.setString(1, siteCode);
		rs=pstmt.executeQuery();
		if(rs.next())
		{
			finEntity=rs.getString("fin_entity");
		}
		pstmt.close();
		pstmt=null;
		rs.close();
		rs=null;		
		
		System.out.println("Fin entity found  :-["+finEntity+"]");
			siteCodeMfg=siteCode;
			System.out.println("finEntityList.size :- ["+finEntityList.size()+"]");
			System.out.println("Defaylt site own:-["+defSiteOwn+"]");
			if(finEntityList.contains(finEntity.trim()))
			{		
				System.out.println("Fin entity match with disparm");
				siteCodeOwn=siteCode;
			}
			else
			{
				siteCodeOwn=defSiteOwn;
			}
		
		systemdate = Timestamp.valueOf(genericUtility.getValidDateString(sysDate, genericUtility.getApplDateFormat(),
				genericUtility.getDBDateFormat())+ " 00:00:00.0");
			
		
		sqlStr="INSERT INTO ITEM_LOT_PACKSIZE (ITEM_CODE,LOT_NO__FROM,LOT_NO__TO,UNIT__PACK,CHG_DATE,CHG_USER,CHG_TERM," +
				"SHIPPER_SIZE,SITE_CODE__MFG,SITE_CODE__OWN,UNIT__INNER_LABEL,SHELF_LIFE) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
		pstmt=conn.prepareStatement(sqlStr);
		pstmt.setString(1, itemCode);
		pstmt.setString(2, lotNo);
		pstmt.setString(3, lotNo);
		pstmt.setString(4, unitPack);
		pstmt.setTimestamp(5, systemdate);
		pstmt.setString(6, empCode);
		pstmt.setString(7, termId);
		pstmt.setString(8, shipperSize);
		pstmt.setString(9, siteCodeMfg);
		pstmt.setString(10, siteCodeOwn);
		pstmt.setString(11, unit);
		pstmt.setString(12, shelfLife);
		insertCoun=pstmt.executeUpdate();
		
		pstmt.close();
		pstmt=null;
		
	}catch(Exception e)
	{
		e.printStackTrace();
		throw new ITMException(e);
	}
	return insertCoun;
}
	private String getUnitPack(String unit, String intigralQty,Connection conn) throws ITMException 
	{
		String unitPack="",sql="";
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		
		try {
			sql="SELECT MAX(UNIT__FR) FROM UOMCONV WHERE UNIT__TO= ?  AND FACT= ?";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, unit);
			pstmt.setDouble(2, Double.parseDouble(intigralQty));
			rs=pstmt.executeQuery();
			if(rs.next())
			{
				unitPack=checkNull(rs.getString(1));
			}
			pstmt.close();
			pstmt=null;
			rs.close();
			rs=null;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}

	return unitPack;
}
	public String confirmTran(String businessObj,String tranId,String xtraParams,String forceFlag,Connection conn) throws ITMException
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

			try
			{				
				sql = "SELECT SERVICE_CODE,COMP_NAME FROM SYSTEM_EVENTS WHERE OBJ_NAME = ? AND EVENT_CODE = 'pre_confirm' ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,businessObj);
				rs = pstmt.executeQuery();
				if ( rs.next() )
				{
					serviceCode = rs.getString("SERVICE_CODE");
					compName = rs.getString("COMP_NAME");
				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
				System.out.println("serviceCode = "+serviceCode+" compName "+compName);
				
				sql = "SELECT SERVICE_URI,METHOD_NAME FROM SYSTEM_EVENT_SERVICES WHERE SERVICE_CODE = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,serviceCode);
				rs = pstmt.executeQuery();
				if ( rs.next() )
				{
					methodName= rs.getString("METHOD_NAME");
					serviceURI = rs.getString("SERVICE_URI");
				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt=null;
				actionURI = "http://NvoServiceurl.org/" + methodName;
				System.out.println("serviceURI = "+serviceURI+" compName = "+compName);
				
				Service service = new Service();
				Call call = (Call)service.createCall();
				call.setTargetEndpointAddress(new java.net.URL(serviceURI));
				call.setOperationName( new javax.xml.namespace.QName("http://NvoServiceurl.org", methodName ) );
				call.setUseSOAPAction(true);
				call.setSOAPActionURI(actionURI);
				Object[] aobj = new Object[4];

				call.addParameter(new javax.xml.namespace.QName("http://NvoServiceurl.org", "component_name"), XMLType.XSD_STRING, ParameterMode.IN);
				call.addParameter(new javax.xml.namespace.QName("http://NvoServiceurl.org", "tran_id"), XMLType.XSD_STRING, ParameterMode.IN);
				call.addParameter(new javax.xml.namespace.QName("http://NvoServiceurl.org", "xtra_params"), XMLType.XSD_STRING, ParameterMode.IN);
				call.addParameter(new javax.xml.namespace.QName("http://NvoServiceurl.org", "forced_flag"), XMLType.XSD_STRING, ParameterMode.IN);

				aobj[0] = new String(compName);
				aobj[1] = new String(tranId);
				aobj[2] = new String(xtraParams);
				aobj[3] = new String("");
				
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
		
	@Override
	public String schedule(HashMap arg0) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	//@Override
	public String schedulePriority(String arg0) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}

