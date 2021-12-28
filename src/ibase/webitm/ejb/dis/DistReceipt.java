
/********************************************************
	Title :  Distribution Order
	Date  : 20/09/07
	Author: Mohammad Shoaib

********************************************************/

package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.DriverManager;
import java.sql.Timestamp;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
//import javax.ejb.SessionBean;
import javax.ejb.CreateException;
import ibase.webitm.utility.ITMException;
//import ibase.webitm.utility.GenericUtility;
import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.system.config.*;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;

import ibase.webitm.ejb.*;
import ibase.webitm.ejb.sys.SysCommon;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3

public class DistReceipt extends ValidatorEJB //implements SessionBean
 {
	/*public void ejbCreate() throws RemoteException, CreateException
	{
		System.out.println("<======= Dist Order EJB IS IN PROCESS ========>");
	}
	public void ejbRemove()
	{
	}
	public void ejbActivate()
	{
	}
	public void ejbPassivate()
	{
	}*/
	public String wfValData() throws RemoteException,ITMException
	{
		return "";
	}
	public String itemChanged() throws RemoteException,ITMException
	{
		return "";
	}
	public String updateStatus()throws RemoteException,ITMException
	{
		return "";
	}

	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		Document dom3 = null;
		String errString = "";
		System.out.println("<====== VALIDATION START =====>");
		try
		{
			System.out.println("xmlString:::" + xmlString);
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString("<Root>" + xmlString2+ "</Root>");
			}
			errString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : DistOrderEJB : wfValData(String xmlString) : ==>\n"+e.getMessage());
		}
		return (errString);
	}
	public String wfValData(Document dom, Document dom1,Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		int ctr = 0;
		String startDate = null,endDate = null;
		String columnValue = null;
		String childNodeName = null;
		String errString = "";
		String errCode = null;
		String userId = null,loginSite = null;
		String errcode = null;
		//field from pb
		int cc, xx, initialField = 1, lastField = 1, fldcase = 0, madvperc = 0, liCnt = 0;
		String fldname = "", Errcode = "", mval="", mVal = "", mVAL = "", mVal1 = "", fldnm = "", ItemLocty = "", LocTy = "", lsSiteCodeShip = "";
		String mitemcode = "", mcustcode = "", mstan = "", mstan1 = "", mtranIdIss = "", lsTranIdIss = "", lsDistOrder = "";
		String mcode = "", mcustCode = "", msiteCode = "", mactive = "", mitemSer = "", mitemSerHdr = "", lsSiteCode = "";
		String mothSer = "", mschemeCd = "", msiteCd = "", mstateCd = "", mtype = "", mtranCode = "", mcurrCode = "";
		String mconfirm = "", mlotNo = "", mshipSite = "", mdistIss = "", msiteCodeMfg = "", lsStatus = "", lsLoctype = "";
		String lsItemCode = "", lsSuppSour = "", lsTrack = "", lsQcreqd = "",lsQcreqdHdr = "", lsTranId = "", sFieldNo = "0";
		java.util.Date  mdate2 = null, mtrandate = null, morderDt = null, mdate = null;
		double mqty = 0.0, mintQty = 0.0, ldQty = 0.0, ldQtyConfirm = 0.0, ldQtyRcp = 0.0, ldRemQty = 0.0, ldOrigQty = 0.0, ldOverShip = 0.0;
		int cnt = 0, mcount = 0, mitemCnt = 0, mcnt = 0, llQcreqdCtr = 0, llLineNo = 0, llCount = 0;
		String lsGrade = "", lsLotNo = "", lsLotSl = "", lsLocCode = "", lsStkgrade = "", lsMfgparmgrade = "", lsSite = "";
		String lsCurrCodeBase = "", lsProjcode = "", lsProjcodeord = "", lsDistOrd = "", lsDist = "", lsDistRcp = "", lsSiteCodeBil = "";
		String mval1 = null;
		String appDateFormat = null;
		String saveflag = "2";
		int currentFormNo = 0;
		int childNodeListLength;
		Connection conn = null;
		PreparedStatement pstmt=null;
		ResultSet rs = null;
		String sql = null;
		Timestamp mdate1 =null;
		ITMDBAccessEJB  dbAccessRemote =null; // for ejb dbAccessRemote var is used every where so not change its varname

    	try
		{
			ConnDriver connDriver = new ConnDriver();
			conn = connDriver.getConnectDB( "DriverITM" );
			connDriver = null;
			userId = getValueFromXTRA_PARAMS( xtraParams, "loginCode" );
			loginSite = getValueFromXTRA_PARAMS( xtraParams, "loginSiteCode" );
			//GenericUtility genericUtility = GenericUtility.getInstance();
			E12GenericUtility genericUtility = new E12GenericUtility();
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			//getting jndi look for ITMDBAccess object
			 dbAccessRemote = new ITMDBAccessEJB();
			//ITMDBAccess dbAccessRemote = dbAccessHome.create();
			//end getting jndi look for ITMDBAccess object
			//making object of DistCommon
			DistCommon distCommon = new DistCommon();
			//
			appDateFormat = genericUtility.getApplDateFormat();

			switch( currentFormNo )
			{
				case 1 :
					parentNodeList = dom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
					for(ctr = 0; ctr < childNodeListLength; ctr++)
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();

						if ( childNodeName.equalsIgnoreCase( "dist_order" ) )
						{
							mcode = genericUtility.getColumnValue( "dist_order", dom );
							lsDistRcp = genericUtility.getColumnValue( "tran_id", dom );
							sql = "select count(*) cnt from distorder where dist_order = '" + mcode.trim() + "'";
							System.out.println( "sql ::[" + sql + "]" );
							pstmt = conn.prepareStatement( sql );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								cnt = rs.getInt( "cnt" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if( cnt == 0 )
							{
								errcode = "VTDIST2";
							}
							else
							{
								sql = "select confirmed mconfirm, status ls_status from distorder where dist_order = '" + mcode + "'";
								System.out.println( "sql ::[" + sql + "]" );
								pstmt = conn.prepareStatement( sql );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									mconfirm = rs.getString( "mconfirm" );
									lsStatus = rs.getString( "ls_status" );
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if( !( "Y".equalsIgnoreCase( mconfirm ) ) )
								{
									errcode = "VTDIST3";
								}
								else if( !( "P".equalsIgnoreCase( lsStatus ) ) )
								{
									errcode = "VTDIST20";
								}
							}
							if( errcode == null || errcode.trim().length() == 0 )
							{
								if( lsDistRcp == null )
								{
									lsDistRcp = " ";
								}
								sql = "Select count(*) cnt From distord_rcp "
									+" Where  dist_order   = '" + mcode.trim() + "'"
									+"	and tran_id <> '" + lsDistRcp.trim() + "'"
									+"	and confirmed = 'N' ";

								System.out.println( "sql ::[" + sql + "]" );
								pstmt = conn.prepareStatement( sql );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									cnt = rs.getInt( "cnt" );
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if( cnt > 0 )
								{
									errcode = "VTINVDO1";
								}
							}
							if( errcode != null && errcode.trim().length() > 0 )
							{
								errString = getErrorString( "dist_order", errcode, userId );
								break;
							}
						}
						if ( childNodeName.equalsIgnoreCase( "tran_date" ) )
						{
							String mdate1Str = null;
							mdate1Str = genericUtility.getColumnValue( "tran_date", dom );
							mdate1 = getDateInForamt( mdate1Str, appDateFormat );
							msiteCode = genericUtility.getColumnValue( "site_code", dom );
							//to be migrated msalam
							//errcode = nvo_functions_adv.nf_check_period( 'DIS', mdate1, msite_code)
							errcode = SysCommon.nfCheckPeriod( "DIS", mdate1, msiteCode, conn );
							if( errcode != null && errcode.trim().length() > 0 )
							{
								errString = getErrorString( "tran_date", errcode, userId );
								break;
							}
						}
						if ( childNodeName.equalsIgnoreCase( "tran_id__iss" ) )
						{
							mtranIdIss = genericUtility.getColumnValue( "tran_id__iss", dom );

							if( mtranIdIss != null && mtranIdIss.trim().length() > 0 )
							{

								sql = "select count(*) cnt from distord_iss where tran_id = '" + mtranIdIss.trim() + "'";

								System.out.println( "sql ::[" + sql + "]" );
								pstmt = conn.prepareStatement( sql );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									cnt = rs.getInt( "cnt" );
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if( cnt > 0 )
								{
									errcode = "VTDIST11";
								}
								else
								{
									sql = "select confirmed mconfirm from distord_iss where tran_id = '" + mtranIdIss + "'";

									System.out.println( "sql ::[" + sql + "]" );
									pstmt = conn.prepareStatement( sql );
									rs = pstmt.executeQuery();
									if( rs.next() )
									{
										mconfirm = rs.getString( "mconfirm" );
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									if( !( "Y".equalsIgnoreCase( mconfirm ) ) )
									{
										errcode = "VTDIST12";
									}
								}
							}
							if( errcode != null && errcode.trim().length() > 0 )
							{
								errString = getErrorString( "tran_id__iss", errcode, userId );
								break;
							}
						}
						if ( childNodeName.equalsIgnoreCase( "site_code" ) )
						{
							mVal = genericUtility.getColumnValue( "site_code", dom );
							sql = "Select Count(*) cnt from site where site_code = '" + mVal.trim() + "'";

							System.out.println( "sql ::[" + sql + "]" );
							pstmt = conn.prepareStatement( sql );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								cnt = rs.getInt( "cnt" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if( cnt == 0 )
							{
								errcode = "VTSITE1";
							}
							else
							{
								mdistIss = genericUtility.getColumnValue( "tran_id__iss", dom );
								mcode = "";
								mcode = genericUtility.getColumnValue( "dist_order", dom );
								if( mdistIss == null || mdistIss.trim().length() == 0 )
								{
									sql = "select site_code__dlv mship_site from distorder	where dist_order = '" + mcode.trim() + "'" ;
									System.out.println( "sql ::[" + sql + "]" );
									pstmt = conn.prepareStatement( sql );
									rs = pstmt.executeQuery();
									if( rs.next() )
									{
										mshipSite = rs.getString( "mship_site" );
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									if( !( mshipSite.trim().equalsIgnoreCase( mVal.trim() ) ) )
									{
										errcode = "VTDIST10";
									}
								}
								else
								{
									sql = "select site_code__dlv mship_site from distord_iss where tran_id = '" + mdistIss.trim() + "'";									System.out.println( "sql ::[" + sql + "]" );
									pstmt = conn.prepareStatement( sql );
									rs = pstmt.executeQuery();
									if( rs.next() )
									{
										mshipSite = rs.getString( "mship_site" );
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									if( !( mshipSite.trim().equalsIgnoreCase( mVal.trim() ) ) )
									{
										errcode = "VTDIST13";
									}
								}
							}

							if( errcode != null && errcode.trim().length() > 0 )
							{
								errString = getErrorString( "site_code", errcode, userId );
								break;
							}
						}
						if ( childNodeName.equalsIgnoreCase( "dist_route" ) )
						{
							mVal = genericUtility.getColumnValue( "dist_route", dom );
		 			 		if( mVal != null && mVal.trim().length() > 0 )
							{
						 		sql = "Select Count(*) cnt from distroute where dist_route ='" + mVal.trim() + "'";
								System.out.println( "sql ::[" + sql + "]" );
								pstmt = conn.prepareStatement( sql );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									cnt = rs.getInt( "cnt" );
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if( cnt == 0 )
								{
									errcode = "VTDISTRT1";
								}
							}

							if( errcode != null && errcode.trim().length() > 0 )
							{
								errString = getErrorString( "dist_route", errcode, userId );
								break;
							}
						}
						if ( childNodeName.equalsIgnoreCase( "site_code__ship" ) )
						{
							mVal = genericUtility.getColumnValue( "site_code__ship", dom );
		 			 		lsSiteCode = genericUtility.getColumnValue( "site_code", dom );
		 			 		lsDistOrder = genericUtility.getColumnValue( "dist_order", dom );

							sql = "Select Count(*) cnt from site where site_code ='" + mVal + "'";
							System.out.println( "sql ::[" + sql + "]" );
							pstmt = conn.prepareStatement( sql );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								cnt = rs.getInt( "cnt" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if( cnt == 0 )
							{
								errcode = "VTSITE1";
							}
							else
							{
								//to migrate by msalam
								//errcode = i_nvo_gbf_dissue.gbf_dist_order_site(ls_dist_order,mval,ls_site_code);
								errcode = gbfDistOrderSite( lsDistOrder, mval, lsSiteCode, conn );
							}

							if( errcode != null && errcode.trim().length() > 0 )
							{
								errString = getErrorString( "site_code__ship", errcode, userId );
								break;
							}
						}
						if ( childNodeName.equalsIgnoreCase( "tran_code" ) )
						{
							mtranCode = genericUtility.getColumnValue( "tran_code", dom );
		 			 		lsSiteCode = genericUtility.getColumnValue( "site_code", dom );
		 			 		lsDistOrder = genericUtility.getColumnValue( "dist_order", dom );

							sql = "select count(*) cnt from transporter where tran_code = '" + mtranCode + "'";
							System.out.println( "sql ::[" + sql + "]" );
							pstmt = conn.prepareStatement( sql );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								cnt = rs.getInt( "cnt" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if( cnt == 0 )
							{
								errcode = "VTTRANCD1";
							}
							if( errcode != null && errcode.trim().length() > 0 )
							{
								errString = getErrorString( "tran_code", errcode, userId );
								break;
							}
						}
						if ( childNodeName.equalsIgnoreCase( "loc_code__git" ) )
						{
							mVal = genericUtility.getColumnValue( "loc_code__git", dom );

							sql = "Select Count(*) cnt from location where loc_code = '" + mVal + "'";
							System.out.println( "sql ::[" + sql + "]" );
							pstmt = conn.prepareStatement( sql );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								cnt = rs.getInt( "cnt" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if( cnt == 0 )
							{
								errcode = "VTLOC1";
							}
							if( errcode != null && errcode.trim().length() > 0 )
							{
								errString = getErrorString( "tran_code", errcode, userId );
								break;
							}
						}
						if ( childNodeName.equalsIgnoreCase( "curr_code" ) )
						{
							mcurrCode = genericUtility.getColumnValue( "curr_code", dom );

							sql = "select count(*) cnt from currency where curr_code = '" + mcurrCode + "'";
							System.out.println( "sql ::[" + sql + "]" );
							pstmt = conn.prepareStatement( sql );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								cnt = rs.getInt( "cnt" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if( cnt == 0 )
							{
								errcode = "VTCURRCD1";
							}
							if( errcode != null && errcode.trim().length() > 0 )
							{
								errString = getErrorString( "curr_code", errcode, userId );
								break;
							}
						}
						if ( childNodeName.equalsIgnoreCase( "qc_reqd" ) )
						{
							lsQcreqdHdr = genericUtility.getColumnValue( "qc_reqd", dom );
							lsTranId = genericUtility.getColumnValue( "tran_id", dom );

							sql = "select count(1) ll_qcreqd_ctr "
								+"	from  distord_rcpdet a, item b  "
								+" where a.item_code = b.item_code  "
								+"	and   a.tran_id   = '" + lsTranId + "'"
								+"	and   b.qc_reqd   = 'Y' " ;

							System.out.println( "sql ::[" + sql + "]" );
							pstmt = conn.prepareStatement( sql );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								llQcreqdCtr = rs.getInt( "ll_qcreqd_ctr" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if( !( "Y".equalsIgnoreCase( lsQcreqdHdr ) ) && llQcreqdCtr > 0 )
							{
								errcode = "VTQCREQD";
							}
							if( errcode != null && errcode.trim().length() > 0 )
							{
								errString = getErrorString( "qc_reqd", errcode, userId );
								break;
							}
						}
						if ( childNodeName.equalsIgnoreCase( "proj_code" ) )
						{
							lsProjcode = genericUtility.getColumnValue( "proj_code", dom );
							if( lsProjcode != null && lsProjcode.trim().length() > 0 ){
								lsDistOrd = genericUtility.getColumnValue( "dist_order", dom );
								sql = "select ( case when proj_code is null then ' ' else proj_code end ) ls_projcodeord "
									 +"	from distorder where dist_order = '" + lsDistOrd;

								System.out.println( "sql ::[" + sql + "]" );
								pstmt = conn.prepareStatement( sql );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									lsProjcodeord = rs.getString( "ls_projcodeord" );
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if( lsProjcodeord != null && lsProjcodeord.trim().length() > 0 )
								{
									if( !( lsProjcodeord.trim().equalsIgnoreCase( lsProjcode.trim() ) ) )
									{
										errcode =  "VTPROMISMT";
									}
								}
								if( errcode != null && errcode.trim().length() > 0 )
								{
									sql = "select ( case when proj_status is null then ' ' else proj_status end ) ls_status from project where proj_code = '" + lsProjcode + "'";
									pstmt = conn.prepareStatement( sql );
									rs = pstmt.executeQuery( );
									if( rs.next() )
									{
										lsStatus = rs.getString( "ls_status" );
									}
									else
									{
										errcode = "VMPROJCDX";
									}
									if( !( "A".equalsIgnoreCase( lsStatus ) ) )
									{
										errcode = "VTPROJ2";
									}
								}
							}
							if( errcode != null && errcode.trim().length() > 0 )
							{
								errString = getErrorString( "proj_code", errcode, userId );
								break;
							}
						}
						if ( childNodeName.equalsIgnoreCase( "site_code__bil" ) )
						{
							lsSiteCodeBil = genericUtility.getColumnValue( "site_code__bil", dom );
							if ( lsSiteCodeBil != null && lsSiteCodeBil.trim().length() > 0 )
							{
								sql = "select count(1) ll_count from site where site_code = '" + lsSiteCodeBil + "'" ;
								System.out.println( "sql ::[" + sql + "]" );
								pstmt = conn.prepareStatement( sql );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									cnt = rs.getInt( "cnt" );
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if( llCount == 0 )
								{
									errcode = "VTBILSITE";
								}
							}
							if( errcode != null && errcode.trim().length() > 0 )
							{
								errString = getErrorString( "site_code__bil", errcode, userId );
								break;
							}
						}
					}
					break;
				case 2 :
					System.out.println("VALIDATION FOR DETAIL [ DOM2 ]..........");
					parentNodeList = dom.getElementsByTagName("Detail2");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
					for(ctr = 0; ctr < childNodeListLength; ctr++)
					{
						cnt = 0;
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						if (childNodeName.equalsIgnoreCase("line_no__sord"))
						{
							lsDistOrder = genericUtility.getColumnValue( "line_no__sord", dom, "2" );
							lsDist = genericUtility.getColumnValue( "dist_order", dom, "2" );
							lsDistRcp = genericUtility.getColumnValue( "tran_id", dom, "2" );
							sql = "select count(*) cnt from distorder where dist_order = '" + lsDistOrder + "'"
								+" and confirmed = 'Y' "
								+" and status = 'P'";
							pstmt = conn.prepareStatement( sql );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								cnt = rs.getInt( "cnt" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if( cnt == 0 )
							{
								errcode = "VTDIST2";
							}
							else
							{
								lsSiteCode  = genericUtility.getColumnValue( "site_code", dom, "1" );
								lsSiteCodeShip  = genericUtility.getColumnValue( "site_code__ship", dom, "1" );
								//to migrate by msalam
								//errcode = i_nvo_gbf_dissue.gbf_dist_order_site(ls_dist_order,ls_site_code__ship,ls_site_code);
								errcode = gbfDistOrderSite( lsDistOrder, lsSiteCodeShip, lsSiteCode, conn );
							}
							if( errcode != null || errcode.trim().length() > 0 )
							{
								if( lsDistRcp == null )
								{
									lsDistRcp = "";
								}
								sql = " Select count(*) cnt "
									+" From	 distord_rcp "
									+" Where  dist_order   = '" + lsDistOrder + "'"
									+" and 	 tran_id <> " + lsDistRcp.trim() + "'"
									+" and   confirmed = 'N' ";

								pstmt = conn.prepareStatement( sql );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									cnt = rs.getInt( "cnt" );
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if( cnt > 0 )
								{
									errcode = "VTINVDO1";
								}
							}
							if( errcode != null && errcode.trim().length() > 0 )
							{
								errString = getErrorString( "line_no__sord", errcode, userId );
								break;
							}
						}
						if( childNodeName.equalsIgnoreCase( "item_code" ) )
						{
							mVal = genericUtility.getColumnValue( "item_code", dom, "2" );
					 		msiteCode = "";
				    		String mtrandateStr = null;
							mtrandateStr = genericUtility.getColumnValue( "tran_date", dom, "1" );
							mtrandate = getDateInForamt( mtrandateStr, appDateFormat );
							//to be migrated by msalam
							//errcode = i_nvo_gbf_func.gbf_item(msite_code,mVal,transer);
							String transer =null;
							transer = "D-RCP";
							errcode = dbAccessRemote.isItem( msiteCode, mVal, transer, conn );

							lsQcreqdHdr = genericUtility.getColumnValue( "qc_reqd", dom, "1" );

							sql = "select count(1) ll_qcreqd_ctr "
								+" from   item   "
								+" where item_code = '" + mVal + "'"
								+" and   qc_reqd   = 'Y'";

							pstmt = conn.prepareStatement( sql );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								llQcreqdCtr = rs.getInt( "ll_qcreqd_ctr" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if( !( "Y".equalsIgnoreCase( lsQcreqdHdr ) ) && llQcreqdCtr > 0 )
							{
								errcode = "VTQCREQD";
							}
							if( "Y".equalsIgnoreCase( lsQcreqdHdr ) && ( errcode == null || errcode.trim().length() == 0 ) )
							{
								lsSiteCode = genericUtility.getColumnValue( "site_code", dom, "1" );
								sql = "select count(*) cnt from siteitem "
									+" where site_code = '" + lsSiteCode + "' and	item_code = '" + mVal + "'";
								pstmt = conn.prepareStatement( sql );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									cnt = rs.getInt( "cnt" );
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if(  cnt == 0 )
								{
									errcode = "VTITEM3";
								}
							}
							if( errcode != null && errcode.trim().length() > 0 )
							{
								errString = getErrorString( "item_code", errcode, userId );
								break;
							}
						}
						if( childNodeName.equalsIgnoreCase( "quantity" ) )
						{
							ldQty = Double.parseDouble( genericUtility.getColumnValue( "quantity", dom, "2" ) );
							lsItemCode = genericUtility.getColumnValue( "item_code", dom, "2" );

							llLineNo = Integer.parseInt( genericUtility.getColumnValue( "line_no_dist_order", dom, "2" ) ); //dw_detedit[ii_currformno].GetItemnumber(dw_detedit[ii_currformno].GetRow(),"line_no_dist_order")
							sql = "select qty_confirm ld_qty_confirm, over_ship_perc ld_over_ship "
								+" from   distorder_det "
								+" where  dist_order = '" + lsDistOrder + "'"
								+" and    item_code  = '" + lsItemCode + "'"
								+" and    line_no    = " + llLineNo;

							pstmt = conn.prepareStatement( sql );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								ldQtyConfirm = rs.getDouble( "ld_qty_confirm" );
								ldOverShip = rs.getDouble( "ld_over_ship" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							ldQtyConfirm = ldQtyConfirm + ( ldQtyConfirm * ( ldOverShip/100 ) );

							sql = " Select sum(quantity) ld_qty_rcp "
								+" From distord_rcpdet,distord_rcp "
								+" where distord_rcpdet.dist_order = '" + lsDistOrder + "' and "
								+"		distord_rcpdet.item_code = '" + lsItemCode + "' and and "
								+"		distord_rcp.tran_id = distord_rcpdet.tran_id and "
								+"		distord_rcpdet.line_no_dist_order = " + llLineNo;

							//If dw_detbrow[ii_currformno].rowcount() > 0 Then
							//	if dw_detbrow[ii_currformno].GetSelectedRow(0) > 0  and saveflag = '2' Then
							//		ld_orig_qty = dw_detbrow[ii_currformno].GetItemNumber(dw_detbrow[ii_currformno].GetSelectedRow(0), 'quantity')
							//	End If
							//End If

							if( saveflag.equalsIgnoreCase( "2" ) )
							{
								ldOrigQty = Double.parseDouble( genericUtility.getColumnValue( "quantity", dom, "2" ) );
							}
							//if Isnull(ld_qty_rcp) then ld_qty_rcp = 0
							//if IsNull(ld_orig_qty) then ld_orig_qty = 0
							//end how to do by msalam

							ldQtyRcp = ldQtyRcp - ldOrigQty;
							ldRemQty = ldQtyConfirm - ldQtyRcp;
							if( ldQty > ldRemQty )
							{
								errcode = "VTIQRCP";
							}
							if( errcode != null && errcode.trim().length() > 0 )
							{
								errString = getErrorString( "quantity", errcode, userId );
								break;
							}
						}
						if( childNodeName.equalsIgnoreCase( "loc_code" ) )
						{
							mVal = genericUtility.getColumnValue( "loc_code", dom, "2" );
							if( mVal != null && mVal.trim().length() > 0 )
							{
								sql = "Select count(1) cnt "
									+" From	 Location "
									+" Where	 loc_code = '" + mVal.trim() + "'";
								pstmt = conn.prepareStatement( sql );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									cnt = rs.getInt( "cnt" );
								}

								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if( cnt == 0 )
								{
									errcode = "VTLOC1";
								}
								if( errcode == null || errcode.trim().length() == 0 )
								{
									cnt = 0;
									lsItemCode = genericUtility.getColumnValue( "item_code", dom, "2" );

									sql = " select loc_type ls_loctype "
										  +" from item where item_code = '" + lsItemCode + "'" ;
									pstmt = conn.prepareStatement( sql );
									rs = pstmt.executeQuery();
									if( rs.next() )
									{
										lsLoctype = rs.getString( "ls_loctype" );
									}

									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									sql = "select count(*) cnt "
									+" from  location "
									+" where loc_type = '" + lsLoctype + "'"
									+" and   loc_code = '" + mval + "'";

									pstmt = conn.prepareStatement( sql );
									rs = pstmt.executeQuery();
									if( rs.next() )
									{
										cnt = rs.getInt( "cnt" );
									}

									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									//how to migrate by msalam
									if( cnt == 0 )
									{
										errcode = "VMLOCTYP1";
									}
									//how to migrate by msalam end
								}
								cnt =0;
							}
							else
							{
								errcode = "VTLOC1";
							}
							cnt =0;
							if( errcode == null || errcode.trim().length() == 0 )
							{
								lsQcreqd =  genericUtility.getColumnValue( "qc_reqd", dom, "1" );
								if( lsQcreqd == null || lsQcreqd.trim().length() == 0 )
								{
									lsQcreqd = "Y";
								}
								if( "Y".equalsIgnoreCase( lsQcreqd.trim() ) )
								{
									mval1 = genericUtility.getColumnValue( "item_code", dom, "2" );
									lsSite = genericUtility.getColumnValue( "site_code", dom, "1" );
									//to be migrated by msalam
									//lsQcreqd = gf_qc_reqd(lsSite,mval1);
									lsQcreqd = gfQcReqd( lsSite, mval1, conn );

									if( "Y".equalsIgnoreCase( lsQcreqd ) )
									{
										cnt = 0;
										sql = " select count(*) cnt "
										+" from   location a, invstat b "
										+" where  a.inv_stat  = b.inv_stat "
										+" and 	 b.available = 'N' "
										+" and 	 a.loc_code  = '" + mVal + "'";

										pstmt = conn.prepareStatement( sql );
										rs = pstmt.executeQuery();
										if( rs.next() )
										{
											cnt = rs.getInt( "cnt" );
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;

										if( cnt == 0 && ( errcode == null || errcode.trim().length() == 0 ) )
										{
											errcode = "VTLOCSL";
										}
									}
								}
							}
							if( errcode != null && errcode.trim().length() > 0 )
							{
								errString = getErrorString( "loc_code", errcode, userId );
								break;
							}
						}
						if( childNodeName.equalsIgnoreCase( "lot_sl" ) )
						{
							mVal = genericUtility.getColumnValue( "lot_sl", dom, "2" );
							if( mVal == null ||  mVal.trim().length() == 0 )
							{
								errcode = "VTLOTSL001";
							}
							if( errcode != null && errcode.trim().length() > 0 )
							{
								errString = getErrorString( "lot_sl", errcode, userId );
								break;
							}
						}
						if( childNodeName.equalsIgnoreCase( "tax_class" ) )
						{
							mVal = genericUtility.getColumnValue( "tax_class", dom, "2" );
							if( mVal != null &&  mVal.trim().length() > 0 )
							{
			             		sql = "Select Count(*) cnt from taxclass where tax_class = '" + mVal.trim() + "'" ;
								pstmt = conn.prepareStatement( sql );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									cnt = rs.getInt( "cnt" );
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if( cnt == 0 )
								{
						 	 		errcode = "VTTCLASS1";
								}
							}
							if( errcode != null && errcode.trim().length() > 0 )
							{
								errString = getErrorString( "tax_class", errcode, userId );
								break;
							}
						}
						if( childNodeName.equalsIgnoreCase( "tax_chap" ) )
						{
							mVal = genericUtility.getColumnValue( "tax_chap", dom, "2" );
							if( mVal != null &&  mVal.trim().length() > 0 )
							{
			             		sql = "Select Count(*) cnt from taxchap where tax_chap = '" + mVal.trim() + "'" ;
								pstmt = conn.prepareStatement( sql );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									cnt = rs.getInt( "cnt" );
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if( cnt == 0 )
								{
						 	 		errcode = "VTTCHAP1";
								}
							}
							if( errcode != null && errcode.trim().length() > 0 )
							{
								errString = getErrorString( "tax_chap", errcode, userId );
								break;
							}
						}
						if( childNodeName.equalsIgnoreCase( "tax_env" ) )
						{
							//mVal = genericUtility.getColumnValue( "tax_env", dom, "2" );
							mVal = distCommon.getParentColumnValue( "tax_env", dom, "2" );
							System.out.println("DORCP 2 tax_env["+mVal+"]");
							String mdate1Str = genericUtility.getColumnValue( "tran_date", dom, "2" );
							mdate1 = getDateInForamt( mdate1Str, appDateFormat );
							if( mVal != null &&  mVal.trim().length() > 0 )
							{
			             		sql = "Select Count(*) cnt from taxenv where tax_env = '" + mVal.trim() + "'" ;
								pstmt = conn.prepareStatement( sql );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									cnt = rs.getInt( "cnt" );
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if( cnt == 0 )
								{
						 	 		errcode = "VTTENV1";
								}
								else
								{
									//to be migrated by msalam
									//Pavan R 17sept19 start[to validate tax environment]									
									//errcode = gf_check_taxenv_status(mVAL,mdate1);
									//errcode = distCommon.getCheckTaxEnvStatus( mVAL, mdate1, conn );
									errcode = distCommon.getCheckTaxEnvStatus( mVAL, mdate1, "D",conn );
									//Pavan R 17sept19 end[to validate tax environment]
								}
							}
							if( errcode != null && errcode.trim().length() > 0 )
							{
								errString = getErrorString( "tax_env", errcode, userId );
								break;
							}
						}
						if( childNodeName.equalsIgnoreCase( "lot_no" ) )
						{
							mlotNo = genericUtility.getColumnValue( "lot_no", dom, "2" );
					 		lsTranIdIss = genericUtility.getColumnValue( "tran_id__iss", dom, "2" );//dw_header.getitemstring(1,"")
			          		if( mlotNo == null || mlotNo.trim().length() == 0 )
							{
								errcode = "VTDIST9";
							}
						 	else
							{
								if( lsTranIdIss != null && lsTranIdIss.trim().length() > 0 )
								{
									cnt = 0;
									sql = "select count(*) cnt from distord_issdet "
										+" where tran_id = '" + lsTranIdIss.trim() + "'"
										+" and lot_no = '" + mlotNo.trim() + "'";

									pstmt = conn.prepareStatement( sql );
									rs = pstmt.executeQuery();
									if( rs.next() )
									{
										cnt = rs.getInt( "cnt" );
									}

									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									if( cnt == 0 )
									{
										errcode = "VTLOT01";
									}
								}
							}
							if( errcode != null && errcode.trim().length() > 0 )
							{
								errString = getErrorString( "lot_no", errcode, userId );
								break;
							}
						}
						if( childNodeName.equalsIgnoreCase( "site_code__mfg" ) )
						{
							msiteCodeMfg = genericUtility.getColumnValue( "site_code__mfg", dom, "2" );
							mitemcode = genericUtility.getColumnValue( "item_code", dom, "2" ); //dw_detedit[ii_currformno].GetItemString(dw_detedit[ii_currformno].GetRow(),"")

							sql = "Select  (case when supp_sour is null then 'M' else supp_sour end) ls_supp_sour "
								+" From item Where item_code = '" + mitemcode + "'";
							pstmt = conn.prepareStatement( sql );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								lsSuppSour = rs.getString( "ls_supp_sour" );
							}

							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if( "M".equalsIgnoreCase( lsSuppSour ) )
							{
								if( msiteCodeMfg == null || msiteCodeMfg.trim().length() == 0 )
								{
									errcode = "VTSITEMFG1";
								}
							}
							if( errcode != null && errcode.trim().length() > 0 )
							{
								errString = getErrorString( "site_code__mfg", errcode, userId );
								break;
							}
						}
						if( childNodeName.equalsIgnoreCase( "mfg_date" ) )
						{

							//setnull(mdate)
							String mdateStr = null;
							mdateStr = genericUtility.getColumnValue( "mfg_date", dom, "2" );
							mdate = getDateInForamt( mdateStr, appDateFormat );
							mitemcode = genericUtility.getColumnValue( "item_code", dom, "2" );//dw_detedit[ii_currformno].GetItemString(dw_detedit[ii_currformno].GetRow(),"item_code")

							sql = "Select (case when track_shelf_life is null then 'N' else track_shelf_life end) ls_track "
								+" From item Where item_code = '" + mitemcode + "'";

							pstmt = conn.prepareStatement( sql );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								lsTrack = rs.getString( "ls_track" );
							}

							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							//End change Gautam
							if( "Y".equalsIgnoreCase( lsTrack.trim() ) )
							{
								//how to migrate, whereas i have done the following
								//if isnull(mdate) then
								//	errcode = 'VTMFGDATE3'
								//}
								if( mdateStr == null || mdateStr.trim().length() == 0 )
								{
									errcode = "VTMFGDATE3";
								}
							}
							if( errcode != null && errcode.trim().length() > 0 )
							{
								errString = getErrorString( "mfg_date", errcode, userId );
								break;
							}
						}
						if( childNodeName.equalsIgnoreCase( "exp_date" ) )
						{
							//setnull(mdate)
							String mdateStr = null;
							mdateStr = genericUtility.getColumnValue( "exp_date", dom, "2" );

							sql = "Select (case when track_shelf_life is null then 'N' else track_shelf_life end) ls_track "
								+" From item Where item_code = '" + mitemcode + "'";
							pstmt = conn.prepareStatement( sql );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								lsTrack = rs.getString( "ls_track" );
							}

							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if( "Y".equalsIgnoreCase( lsTrack.trim() ) )
							{
								//how to migrate, whereas i have done the following
								//if isnull(mdate) then
								//	errcode = 'VTEXPDATE1'
								//}
								if( mdateStr == null || mdateStr.trim().length() == 0 )
								{
									errcode = "VTEXPDATE1";
								}
							}
							if( errcode != null && errcode.trim().length() > 0 )
							{
								errString = getErrorString( "exp_date", errcode, userId );
								break;
							}
						}
						if( childNodeName.equalsIgnoreCase( "grade" ) )
						{
							lsGrade = genericUtility.getColumnValue( "grade", dom, "2" );// dw_detedit[ii_currformno].GetItemString(1,"grade")
							lsItemCode = genericUtility.getColumnValue( "item_code", dom, "2" );//dw_detedit[ii_currformno].getitemstring(1,"item_code")
							lsSiteCode = genericUtility.getColumnValue( "site_code", dom, "2" );//dw_header.getitemstring(1,"site_code")
							lsLotNo = genericUtility.getColumnValue( "lot_no", dom, "2" );//dw_detedit[ii_currformno].getitemstring(1,"lot_no")
							lsLotSl = genericUtility.getColumnValue( "lot_sl", dom, "2" );//dw_detedit[ii_currformno].getitemstring(1,"lot_sl")
							lsLocCode = genericUtility.getColumnValue( "loc_code", dom, "2" );//dw_detedit[ii_currformno].getitemstring(1,"loc_code")
							sql = "select count(*) li_cnt "
							+" from stock where item_code = '" + lsItemCode + "' and "
							+" site_code = '" + lsSiteCode + "' and "
							+" loc_code  = '" + lsLocCode + "' and "
							+" lot_no    = '" + lsLotNo + "' and "
							+" lot_sl    = '" + lsLotSl + "'" ;

							pstmt = conn.prepareStatement( sql );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								liCnt = rs.getInt( "li_cnt" );
							}

							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if( liCnt != 0 )
							{
								sql = "select grade ls_stkgrade "
									+" from stock where item_code = '" + lsItemCode + "' and "
									+" site_code = '" + lsSiteCode + "' and "
									+" loc_code = '" + lsLocCode + "' and "
									+" lot_no = '" + lsLotNo + "' and "
									+" lot_sl = '" + lsLotSl + "'";
								pstmt = conn.prepareStatement( sql );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									lsStkgrade = rs.getString( "ls_stkgrade" );
								}

								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if( lsGrade == null )
								{
									lsGrade = "   ";
								}

								if( lsStkgrade == null )
								{
									lsStkgrade = "   ";
								}
								if( !( lsGrade.equalsIgnoreCase( lsStkgrade ) ) )
								{
									//how to do this by mslam
									//errcode = "VTGRVLD~t Grade mismatch! Stock for this lot already exists with grade " + ls_stkgrade
									errcode = "VTGRVLD";
								}
								else if( ( lsGrade != null && lsGrade.trim().length() > 0 ) && liCnt == 0 )
								{
									sql = "select var_value ls_mfgparmgrade "
										+" from mfgparm where prd_code = '999999' "
										+" and var_name = 'MFG_GRADE' ";
									pstmt = conn.prepareStatement( sql );
									rs = pstmt.executeQuery();
									if( rs.next() )
									{
										lsMfgparmgrade = rs.getString( "ls_mfgparmgrade" );
									}

									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									if( lsMfgparmgrade == null )
									{
										lsMfgparmgrade = "   ";
									}
									if( !( lsGrade.equalsIgnoreCase( lsMfgparmgrade ) ) )
									{	//how to do it
										//errcode = "VTGRVLD~t Grade mismatch With Grade Defined In MFGPARM with var name MFG_GRADE"
										errcode = "VTGRVLD";
									}
								}
							}
							if( errcode != null && errcode.trim().length() > 0 )
							{
								errString = getErrorString( "grade", errcode, userId );
								break;
							}
						}
					}
			}//END SWITCH
		}//END TRY
		catch(Exception e)
		{
			System.out.println("Exception ::" +e);
			e.printStackTrace();
			errString = e.getMessage();
		}
		finally
		{
			try
			{
				if( pstmt != null )
				{
					pstmt.close();
					pstmt = null;
				}

				if( rs != null )
				{
					rs.close();
					rs = null;
				}
				if(conn != null)
				{
					conn.close();
					conn = null;
				}
			}
			catch(Exception d)
			{
			  d.printStackTrace();
			}
			System.out.println(" < DistOrderEJB > CONNECTION IS CLOSED");
		}
		System.out.println("ErrString ::" + errString);
		return errString;
	}//END OF VALIDATION

	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		Document dom3 = null;
		String valueXmlString = null;
		try
		{
			dom = parseString(xmlString);
			System.out.println("xmlString" + xmlString);
			dom1 = parseString(xmlString1);
			if( xmlString2.trim().length() > 0 )
			{
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [DistOrderEJB][itemChanged(String,String)] :==>\n"+e.getMessage());
		}
        return valueXmlString;
	}

	public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		StringBuffer valueXmlString = new StringBuffer();
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		int ctr = 0;
		String tranDate = null;
		String childNodeName = null;
		String columnValue = null;
		String loginCode = null;
		String loginCodeName = null;
		String flag	= null;
		String sorgCode = null ,sorgName = null,userCode = null ,userName = null ;
		String prdtCode = null ,crTerm = null ,compCode = null ,compDesc = null ,crDesc = null ,prdtDesc = null;
		String stermCode = null ,stermDesc = null ,stkCode = null ,stkName = null ,stkCode2 = null ,stkName2 = null ,productPack = null;
		String state1 = null;
		String state2 = null;
		String sorgType = null;
		int crDays = 0;
		int currentFormNo = 0;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		String  errString = "";
		String loginSite = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility = new E12GenericUtility();
        ConnDriver connDriver = new ConnDriver();
        String empCodePrm = getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode");

		String tranFrFlag = null;

		try
		{
			//Changes and Commented By Poonam on 08-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Poonam on 08-06-2016 :END

			loginSite = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode" );
			if(objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			System.out.println("FORM NO:::"+currentFormNo);

			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			switch(currentFormNo)
			{
				case 1 :
					valueXmlString.append("<Detail1>\r\n");
					parentNodeList = dom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					ctr = 0;
					int childNodeListLength = childNodeList.getLength();
					do
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						if(childNodeName.equals(currentColumn))
						{
							if (childNode.getFirstChild() != null)
							{
								columnValue = childNode.getFirstChild().getNodeValue().trim();
							}
						}
						ctr++;
					}
					while(ctr < childNodeListLength  && !childNodeName.equals(currentColumn));
					System.out.println(" Column Value=>" + columnValue);
					/*
					if (currentColumn.trim().equals("itm_defaultedit"))
					{
						SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
						tranDate = sdf.format(new Timestamp(System.currentTimeMillis()));
						loginCode = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
						sql = "SELECT NAME FROM USERS WHERE CODE = '" + loginCode +"'";
						System.out.println("SQL FOR USER'S NAME ====>" + sql);
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
						   loginCodeName = rs.getString(1) == null ? "":rs.getString(1);
						}
						pstmt.close(); pstmt = null;
						rs.close(); rs = null;

						valueXmlString.append("<user_code>").append("<![CDATA[" + loginCode + "]]>").append("</user_code>");
						valueXmlString.append("<name>").append("<![CDATA[" + loginCodeName + "]]>").append("</name>");
						valueXmlString.append("<tran_date>").append("<![CDATA[" + tranDate + "]]>").append("</tran_date>");

						String instituteClass = null;
						instituteClass = genericUtility.genericUtility.getColumnValue( "sorg_type", dom1 );

						if ("P".equalsIgnoreCase( instituteClass ))
						{
							valueXmlString.append("<tran_id__ref isVisible=\"0\">").append("<![CDATA[" + " " + "]]>").append("</tran_id__ref>");
						}
						else
						{
							valueXmlString.append("<tran_id__ref isVisible=\"1\">").append("<![CDATA[" + " " + "]]>").append("</tran_id__ref>");
						}
					}
					//itmdefault edit
					valueXmlString.append("</Detail1>\r\n");
					//valueXmlString.append("</Detail>");
					break;
				*/
			}//end of switch
			valueXmlString.append("</Root>");
		}//END OF TRY
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage());
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
				if(pstmt!= null)
				{
					pstmt.close();
					pstmt = null;
				}
				if( conn != null )
				{
					conn.close();
					conn = null;
				}
			}
			catch(Exception d)
				{
				  d.printStackTrace();
				}
			System.out.println("[DistOrderEJB] Connection is Closed");
		}
		System.out.println("valueXmlString:::::" + valueXmlString.toString());
		return valueXmlString.toString();
	}//END OF ITEMCHANGE
	private String gbfDistOrderSite( String asDistOrder, String asSiteShip, String asSiteDlv, Connection conn ) throws Exception
	{
		String lsErrcode = null, lsSiteCodeShip = null, lsSiteCodeDlv = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		sql = "select site_code__ship ls_site_code__ship, site_code__dlv ls_site_code__dlv from distorder "
			+" where dist_order = '" + asDistOrder + "'";

		pstmt = conn.prepareStatement( sql );
		rs = pstmt.executeQuery();
		if( rs.next() )
		{
			lsSiteCodeShip = rs.getString( "ls_site_code__ship" );
			lsSiteCodeDlv = rs.getString( "ls_site_code__dlv" );
		}

		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;

		if( !( lsSiteCodeShip.trim().equalsIgnoreCase( asSiteShip.trim() ) ) || !( lsSiteCodeDlv.equalsIgnoreCase( asSiteDlv.trim() ) ) )
		{
		  lsErrcode = "VTDIST10";
		}
		return lsErrcode;
	}
	private Timestamp getDateInForamt( String dateStr, String format ) throws Exception
	{
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility = new E12GenericUtility();
		Timestamp timestamp = null;
		SimpleDateFormat sdf = new SimpleDateFormat( format );//new SimpleDateFormat(genericUtility.getApplDateFormat());
		System.out.println( "dateStr[" + dateStr + "]" );
		java.util.Date frDate = null;
		frDate = sdf.parse( dateStr );
		SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
		timestamp = Timestamp.valueOf(sdf1.format(frDate).toString() + " 00:00:00.0");
		return timestamp;
	}
	public String gfQcReqd( String asSitecode, String asItemcode, Connection conn ) throws Exception
	{
		String lsQcReqd = null;
		String sql = null;

		PreparedStatement pstmt = null;
		ResultSet rs = null;

		PreparedStatement pstmt1 = null;
		ResultSet rs1 = null;

		sql = "select ( case when qc_reqd is null then 'N' else qc_reqd end ) ls_qc_reqd "
			+" from	siteitem "
			+" where	item_code = '" + asItemcode + "'"
			+" and	site_code = '" + asSitecode + "'";

		pstmt = conn.prepareStatement( sql );
		rs = pstmt.executeQuery( );
		if( rs.next() )
		{
			lsQcReqd = rs.getString( "ls_qc_reqd" );
		}
		else
		{
			String sql1 = "select ( case when qc_reqd is null then 'N' else qc_reqd end ) ls_qc_reqd "
						+" from	item where	item_code = '" + asItemcode + "'";
			pstmt1 = conn.prepareStatement( sql );
			rs1 = pstmt.executeQuery( );
			if( rs1.next() )
			{
				lsQcReqd = rs1.getString( "ls_qc_reqd" );
			}
			rs1.close();
			rs1 = null;
			pstmt1.close();
			pstmt1 = null;

		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;

		return lsQcReqd;
	}
}// END OF MAIN CLASS


