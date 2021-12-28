
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
import java.sql.Statement;
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

import java.util.Properties;
import ibase.webitm.ejb.*;
import ibase.webitm.ejb.sys.SysCommon;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3

public class DistOrder extends ValidatorEJB //implements SessionBean
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
			System.out.println( "xmlString:::" + xmlString );
			System.out.println( "xmlString1:::" + xmlString1 );
			System.out.println( "xmlString2:::" + xmlString2 );
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
		//field from pb
		int cc, xx, initialField = 1, lastField = 1, fldcase = 0, madvperc, llCount;
		String fldname = "", errcode = " ";
		String lsSundryCode = null;

		String mVal = null, mVal1 = null, fldnm = null, itemLocty = null, LocTy = null, lsDlvsite = null, lsSource = null, lsSuppsite = null;

		String lsLoccode = null, lsLotno = null, lsLotsl = null, lsBackflushtype = null, lsPparentHdr = null, lsParentDet = null;

		String mitemcode = null, mcustcode = null, mstan = null, mstan1 = null,lsEtatus = null, lsEntfrom = null, lsEntto = null, lsItemCode = null;

		String mcode = null, mcustCode = null, msiteCode = null, mactive = null, mitem_ser = null, mitemSerHdr = null, lsCurrCode = null;

		String mothSer = null, mschemeCd = null, msiteCd = null, mstateCd = null, mtype = null, lsConf = null, lsSaleOrder = null,lsTranType = null;

		String lsLocCodeCons = null , lsTrantype = null, lsSundry_code = null, lsSundryType = null, lsGrpCode = null, lsSalesPers = null;

		String lsChgSite = null, lsProjcode = null;

		String lsOrderType, mslabOn, sFieldNo = "0";

		String lsCheckIntegralQty, lsCurrCodeBase, lsSiteCodeBil;
		Timestamp mdate1 = null;
		java.util.Date  mdate11 =null ,mdate2 = null, mtrandate = null, morderDate1 = null;

		java.util.Date mdueDate = null, morderDate = null, mshipDate = null;

		String lsDistOrder = null;

		int llLineno;

		String lsSiteCode = null,	lsPolicyNo = null,	lsDlvTerm = null, lsPolNo = null;

		double mqty = 0.0, mintQty = 0.0, lcQtyConfirm = 0.0, lcQtyOrder = 0.0, lcOldqty = 0.0, lcOoqty = 0.0, lcDsqty = 0.0;

		double ldExchRate, lcIntQty;
		String lsStatus = null;
		double lcSoqty = 0.0;
		String mVAL = null;
		String mval1 = null;
		long cnt = 0, mcount, mitemCnt, mcnt, lcCnt;
		String mval = null;
		String lsDdlvsite = null;
		String appDateFormat = null;
		int currentFormNo = 0;
		int childNodeListLength;
		Connection conn = null;
		PreparedStatement pstmt=null;
		ResultSet rs = null;
		String sql = null;
		ITMDBAccessEJB dbAccessRemote = null; // for ejb dbAccessRemote var is used every where so not chage is varname

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
			AppConnectParm appConnect = new AppConnectParm();
			Properties props = appConnect.getProperty();
	//		InitialContext ctx = new InitialContext( props );
			System.out.println("Looking Up for DBAccess EJB with Properties ==>\n"+props);			
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

						if ( childNodeName.equalsIgnoreCase( "sale_order" ))
						{
							mval = genericUtility.getColumnValue( "sale_order", dom );
							System.out.println( "mval ::[" + mval + "]" );
							if ( mval != null && mval.trim().length() > 0 )
							{
								sql = "select confirmed, status from sorder where sale_order = '" + mval.trim() + "'";
								System.out.println( "sql ::[" + sql + "]" );
								pstmt = conn.prepareStatement( sql );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									 lsConf = rs.getString( "confirmed" );
									 lsStatus = rs.getString( "status" );
								}
								else
								{
									errcode = "VTSORD1";
									errString = getErrorString( "sale_order", errcode, userId );
									break;

								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if( lsConf == null || "N".equalsIgnoreCase( lsConf.trim() ) )
								{
									errcode = "VTSORD1";
									errString = getErrorString( "sale_order", errcode, userId );
									break;

								}
								else if( "C".equalsIgnoreCase( lsStatus.trim() ) ||  "X".equalsIgnoreCase( lsStatus.trim() ) )
								{
									errcode = "VTSORDCX";
									errString = getErrorString( "sale_order", errcode, userId );
									break;
								}
							}
						}

						if (childNodeName.equalsIgnoreCase("order_date"))
						{

							String mdate1Str = genericUtility.getColumnValue( "order_date", dom, "1" );
							mdate1 = getDateInForamt( mdate1Str, appDateFormat );
							String msiteCodeStr = genericUtility.getColumnValue( "site_code", dom, "1" );

							System.out.println( "mdate1 ::[" + mdate1 + "] msiteCode::[" + msiteCodeStr + "]" );

							//errcode = dbAccessRemote.nfCheckPeriod( "DIS", mdate1, msiteCodeStr, conn );
							errcode = SysCommon.nfCheckPeriod( "DIS", mdate1, msiteCodeStr, conn );

							if( errcode != null && errcode.trim().length() > 0 )
							{
								errString = getErrorString( "order_date", errcode, userId );
								break;
							}
						}
						if ( childNodeName.equalsIgnoreCase( "site_code__ship" ))
						{
							mVal = genericUtility.getColumnValue( "site_code__ship", dom );

							sql = "Select Count(*) cnt from site where site_code = '" + mVal.trim() + "'";
							pstmt = conn.prepareStatement( sql );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								cnt = rs.getInt( "cnt" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							rs = null;
							/*
							if get_sqlcode() = -1 then

								errcode = 'DS000'+TRIM(STRING(SQLCA.SQLDBCODE))
							*/
							if( cnt == 0 )
							{
								errcode = "VTSITE1";
								errString = getErrorString( "site_code__ship", errcode, userId );
								break;
							}
						}
						if ( childNodeName.equalsIgnoreCase( "site_code__dlv" ) )
						{
							mVal = genericUtility.getColumnValue( "site_code__dlv", dom );
							mVal1 = genericUtility.getColumnValue( "site_code__ship", dom );

							lsTranType = genericUtility.getColumnValue( "tran_type", dom );

							sql = "Select Count(*) cnt from site where site_code = '" + mVal + "'";
							pstmt = conn.prepareStatement( sql );
							rs = pstmt.executeQuery();
							/*
							if get_sqlcode() = -1 then
								errcode = 'DS000'+TRIM(STRING(SQLCA.SQLDBCODE))
							else
							*/
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
								sql = "select chg_site lsChgSite from distorder_type	where tran_type = '" + lsTranType + "'";
								pstmt = conn.prepareStatement( sql );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									lsChgSite = rs.getString( "lsChgSite" ) == null ? "" :rs.getString( "lsChgSite" ).trim();
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								errString = getErrorString( "site_code__dlv", errcode, userId );
								break;
							}
							else if( "Y".equalsIgnoreCase( lsChgSite ) )
							{
								errcode = "VTSITE2";
								errString = getErrorString( "site_code__dlv", errcode, userId );
								break;
							}
							else if( "N".equalsIgnoreCase( lsChgSite ) )
							{
								errcode = "VTSITE6";
								errString = getErrorString( "site_code__dlv", errcode, userId );
								break;
							}

							lsSaleOrder = genericUtility.getColumnValue( "sale_order", dom );

							if( lsSaleOrder != null && lsSaleOrder.trim().length() > 0 )
							{
								sql = "select count(*) cnt from sorddet where sale_order = '" + lsSaleOrder + "' and site_code = '" + mval + "'";
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
									errcode = "VTDETSITE";
									errString = getErrorString( "site_code__dlv", errcode, userId );
									break;
								}
							}
						}
						if (childNodeName.equalsIgnoreCase( "purc_order" ) )
						{
							mVal = genericUtility.getColumnValue( "purc_order", dom );
							if ( mVal != null && mVal.trim().length() > 0 )
							{
								sql = "Select Count(*) cnt from porder where purc_order = '" + mVal + "'";
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
									errcode = "VTPONF";
									errString = getErrorString( "purc_order", errcode, userId );
									break;
								}
							}
						}
						if (childNodeName.equalsIgnoreCase( "dist_route" ) )
						{
							mVal = genericUtility.getColumnValue( "dist_route", dom );

							if( mVal != null && mVal.trim().length() > 0 )
							{
								sql = "Select Count(*) cnt from distroute where dist_route = '" + mVal.trim() + "'";

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
									errString = getErrorString( "dist_route", errcode, userId );
									break;
								}
							}
						}
						if ( childNodeName.equalsIgnoreCase( "loc_code__git" ) )
						{
							mVal = genericUtility.getColumnValue( "loc_code__git", dom );

							if( mVal != null && mVal.trim().length() > 0 )
							{
								sql = "Select Count(*) cnt from location where loc_code = '" + mVal.trim() + "'";

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
									errString = getErrorString( "loc_code__git", errcode, userId );
									break;
								}
							}
						}
						if (childNodeName.equalsIgnoreCase( "due_date" ) )
						{
							String mdueDateStr = genericUtility.getColumnValue( "due_date", dom );

							String morderDateStr = genericUtility.getColumnValue( "order_date", dom );

							String mshipDateStr = genericUtility.getColumnValue( "ship_date", dom );

							mdueDate = getDateInForamt( mdueDateStr, appDateFormat );
							morderDate =  getDateInForamt( morderDateStr, appDateFormat );
							mshipDate = getDateInForamt( morderDateStr, appDateFormat );

							if( mdueDate.compareTo( morderDate ) < 0 )
							{
								errcode = "VTDATE8";
								errString = getErrorString( "due_date", errcode, userId );
								break;
							}
							else if( mdueDate.compareTo( mshipDate ) < 0 )
							{
								errcode = "VTDATE13";
								errString = getErrorString( "due_date", errcode, userId );
								break;
							}
						}
						if ( childNodeName.equalsIgnoreCase( "ship_date" ) )
						{

							String mdueDateStr = genericUtility.getColumnValue( "due_date", dom );

							String morderDateStr = genericUtility.getColumnValue( "order_date", dom );

							String mshipDateStr = genericUtility.getColumnValue( "ship_date", dom );

							mdueDate = getDateInForamt( mdueDateStr, appDateFormat );
							morderDate = getDateInForamt( morderDateStr.trim(), appDateFormat );
							mshipDate = getDateInForamt( mshipDateStr.trim(), appDateFormat );

							if( mshipDate.compareTo( morderDate ) < 0 )
							{
								errcode = "VTDATE8";
								errString = getErrorString( "ship_date", errcode, userId );
								break;
							}
							else if( mdueDate.compareTo( mshipDate ) < 0 )
							{
								errcode = "VTDATE13";
								errString = getErrorString( "ship_date", errcode, userId );
								break;
							}
						}
						if ( childNodeName.equalsIgnoreCase( "curr_code" ) )
						{
							lsCurrCode = genericUtility.getColumnValue( "curr_code", dom );

							if( lsCurrCode != null && lsCurrCode.trim().length() > 0 )
							{
								sql = "select count(*) cnt from currency where curr_code = '" + lsCurrCode.trim() + "'";

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
									errcode = "VMCUR1";
								}
							}
							else
							{
								errcode = "VECUR2";
								errString = getErrorString( "curr_code", errcode, userId );
								break;
							}
						}
						if ( childNodeName.equalsIgnoreCase( "loc_code__cons" ) )
						{
							lsLocCodeCons = genericUtility.getColumnValue( "loc_code__cons", dom );

							lsTrantype = genericUtility.getColumnValue( "tran_type", dom );
							if( lsLocCodeCons != null && lsLocCodeCons.trim().length() > 0 )
							{
								sql = "select loc_group__cons ls_grp_code from distorder_type where tran_type = '" + lsTrantype.trim() + "'";

								pstmt = conn.prepareStatement( sql );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									lsGrpCode = rs.getString( "ls_grp_code" );
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if( ( lsGrpCode != null && lsGrpCode.trim().length() > 0 ) && ( lsLocCodeCons != null && lsLocCodeCons.trim().length() > 0 ) )
								{
									sql = " select count(*) cnt from location "
												+"	where loc_code = '" + lsLocCodeCons + "'"
												+"		and loc_group = '" + lsGrpCode + "'";

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
										errcode = "VMLOC5";
										errString = getErrorString( "loc_code__cons", errcode, userId );
										break;
									}
								}
							}
						}
						if ( childNodeName.equalsIgnoreCase( "sundry_code" ) )
						{
							lsSundryCode = genericUtility.getColumnValue( "sundry_code", dom );

							lsSundryType = genericUtility.getColumnValue( "sundry_type", dom );

							if( lsSundryCode != null && lsSundryCode.trim().length() > 0 )
							{
								if( "C".equalsIgnoreCase( lsSundryType.trim() ) )
								{
									sql = "select count(*) cnt from customer where cust_code = '" + lsSundryCode.trim() + "'";
									System.out.println("sql................"+ sql);
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

									if( lsSundryCode == null || lsSundryCode.trim().length() == 0 )
									{
										errcode = "VECUST2";
									}
									else if( cnt == 0 )
									{
										errcode = "VMCUST1";
									}
									else if( "S".equalsIgnoreCase( lsSundryType.trim() ) )
									{
										sql = "select count(*) cnt from supplier where supp_code = '" + lsSundryCode.trim() + "'";
										System.out.println("sql................"+ sql);
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
										if( lsSundryCode == null || lsSundryCode.trim().length() == 0 )
										{
											errcode = "VESUPP2";
										}
										else if ( cnt == 0 )
										{
											errcode = "VMSUPP1";
										}
									}
								}
							}
							if( errcode != null && errcode.trim().length() > 0 )
							{
								errString = getErrorString( "sundry_code", errcode, userId );
								break;
							}
						}
						if ( childNodeName.equalsIgnoreCase( "sales_pers" ) )
						{
							lsSalesPers = genericUtility.getColumnValue( "sales_pers", dom );

							if( lsSalesPers != null && lsSalesPers.trim().length() > 0 )
							{
								sql = "select count(*) cnt from sales_pers where sales_pers = '" + lsSalesPers + "'";
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
								if ( cnt == 0 )
								{
									errcode = "VMSLPERS1";
								}
							}
							if( errcode != null && errcode.trim().length() > 0 )
							{
								errString = getErrorString( "sales_pers", errcode, userId );
								break;
							}
						}
						if ( childNodeName.equalsIgnoreCase( "proj_code" ) )
						{
							lsProjcode = genericUtility.getColumnValue( "proj_code", dom );

							if( lsProjcode != null && lsProjcode.trim().length() > 0 )
							{
								sql = "select ( case when proj_status is null then ' ' else proj_status end ) ls_status from project where proj_code = '" + lsProjcode + "'";
								pstmt = conn.prepareStatement( sql );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									lsStatus = rs.getString( "ls_status" );
								}
								else
								{
									errcode = "VMPROJCDX";
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if( !( "A".equalsIgnoreCase( lsStatus.trim() ) ) )
								{
									errcode = "VTPROJ2";
								}
							}
							if( errcode != null && errcode.trim().length() > 0 )
							{
								errString = getErrorString( "proj_code", errcode, userId );
								break;
							}
						}
						if ( childNodeName.equalsIgnoreCase( "policy_no" ) )
						{
							lsPolicyNo = genericUtility.getColumnValue( "policy_no", dom );
							String morderDate1Str = genericUtility.getColumnValue( "order_date", dom );
							morderDate1 = getDateInForamt( morderDate1Str.trim(), appDateFormat );

							lsOrderType	= genericUtility.getColumnValue( "order_type", dom );

							if ( lsPolicyNo != null && lsPolicyNo.trim().length() > 0 )
							{
								String dlvTemSql = "select dlv_term ls_dlv_term from distorder_type where tran_type = '" + lsTranType + "'";

								pstmt = conn.prepareStatement( dlvTemSql );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									lsDlvTerm = rs.getString( "ls_dlv_term" );
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								String policyNoSql = null;
								policyNoSql = "select policy_no ls_pol_no from delivery_term where dlv_term = '" + lsDlvTerm.trim() + "'";

								pstmt = conn.prepareStatement( policyNoSql );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									lsPolNo = rs.getString( "ls_pol_no" );
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								sql = "select count(*) cnt from insurance where policy_no = '" + lsPolNo + "'";

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
									errcode = "VTPOLI1";
								}

								sql ="select status ls_status, from_date mdate1, valid_upto mdate2 "
											+"from insurance where policy_no = '" + lsPolNo.trim() + "'";

								pstmt = conn.prepareStatement( sql );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									lsStatus = rs.getString( "ls_status" );
									mdate11 = rs.getDate( "mdate1" );
									mdate2 = rs.getDate( "mdate2" );
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;

								if( "C".equalsIgnoreCase( lsStatus ) || "X".equalsIgnoreCase( lsStatus ) )
								{
									errcode = "VTCX";
								}
								else if( mdate11.compareTo( morderDate1 ) > 0 || mdate2.compareTo( morderDate1 ) < 0 )
								{
									errcode = "VTPOLEXP";
								}
							}
							if( errcode != null && errcode.trim().length() > 0 )
							{
								errString = getErrorString( "policy_no", errcode, userId );
								break;
							}
						}

						if ( childNodeName.equalsIgnoreCase( "site_code__bil" ) )
						{
							lsSiteCodeBil = genericUtility.getColumnValue( "site_code__bil", dom );

							if( lsSiteCodeBil != null && lsSiteCodeBil.trim().length() > 0 )
							{
								sql ="select count(1) ll_count from site where site_code = '" + lsSiteCodeBil.trim() + "'";

								pstmt = conn.prepareStatement( sql );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									cnt = rs.getInt( "ll_count" );
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if( cnt == 0 )
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
					} //END OF CASE1
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
							mval1 = genericUtility.getColumnValue( "sale_order", dom, "2" );

							if( mval1 != null && mval1.trim().length() > 0 )
							{
								mval = genericUtility.getColumnValue( "line_no__sord", dom, "2" );

								mval = mval.trim(); //right('    ' + mval , 3 );
								//what to do for next line
								//dw_detedit[ii_currformno].setitem(dw_detedit[ii_currformno].GetRow(),fldname,mval)

								if ( mval!= null && mval.trim().length() > 0 )
								{
									sql = "select count(*) cnt from sorddet "
												 +"where sale_order = '" + mval1.trim() + "' and line_no = '" + mval.trim() + "'";

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
										errcode = "VTSORD2";
									}
								}
							}
							if( errcode != null && errcode.trim().length() > 0 )
							{
								errString = getErrorString( "line_no__sord", errcode, userId );
								break;
							}
						}
						if (childNodeName.equalsIgnoreCase("item_code"))
						{
							mval1 = genericUtility.getColumnValue( "item_code", dom2, "2" );

							msiteCode = genericUtility.getColumnValue( "site_code__ship", dom1 );

							lsDlvsite = genericUtility.getColumnValue( "site_code__dlv", dom1 );

							String mTranDateStr = genericUtility.getColumnValue( "order_date", dom1 );
							mtrandate = getDateInForamt( mTranDateStr.trim(), appDateFormat );
							//what to do for following line
							//errcode = i_nvo_gbf_func.gbf_item(msite_code,mVal,transer)
							String transer = "D-ORD";
							errcode = dbAccessRemote.isItem( msiteCode, mval1, transer, conn );
							sql = "select supp_sour ls_source, site_code__supp ls_suppsite from siteitem "
										+" where site_code = '" + lsDlvsite.trim() + "' and item_code = '" + mval1.trim() + "'";

							pstmt = conn.prepareStatement( sql );
							rs = pstmt.executeQuery();
							if( rs.next() )
							{
								lsSource = rs.getString( "ls_source" );
								lsSuppsite = rs.getString( "ls_suppsite" );
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if( errcode.length() == 0 )
							{
								if( !( "D".equalsIgnoreCase( lsSource.trim() ) ) || !( lsSuppsite.trim().equalsIgnoreCase( msiteCode.trim() ) ) )
								{
									sql = "select supp_sour ls_source, site_code__supp ls_suppsite from siteitem "
												+" where site_code = '" + lsDlvsite.trim() + "' and item_code = '" + mval1.trim() + "'";

									pstmt = conn.prepareStatement( sql );
									rs = pstmt.executeQuery();
									if( rs.next() )
									{
										lsSource = rs.getString( "ls_source" );
										lsSuppsite = rs.getString( "ls_suppsite" );
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									sql = "select fin_entity ls_entfrom from site where site_code = '" + msiteCode + "'";

									pstmt = conn.prepareStatement( sql );
									rs = pstmt.executeQuery();
									if( rs.next() )
									{
										lsEntfrom = rs.getString( "ls_entfrom" );
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									sql = "select fin_entity ls_entto from site where site_code = '" + lsDlvsite + "'";

									pstmt = conn.prepareStatement( sql );
									rs = pstmt.executeQuery();
									if( rs.next() )
									{
										lsEntto = rs.getString( "ls_entto" );
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									if( lsEntto.equalsIgnoreCase( lsEntto ) )
									{
										errcode = "VTDISTCH";
									}
								}
							}
							if( errcode != null && errcode.trim().length() > 0 )
							{
								errString = getErrorString( "item_code", errcode, userId );
								break;
							}
						}

						if( childNodeName.equalsIgnoreCase( "tax_class" ) )
						{
							mVal = genericUtility.getColumnValue( "tax_class", dom, "2" );
							if( mVAL != null && mVal.trim().length() > 0 )
							{
								sql = "Select Count(*) cnt from taxclass where tax_class = '" + mVal + "'";

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
						if ( childNodeName.equalsIgnoreCase( "tax_chap") )
						{
							mVal = genericUtility.getColumnValue( "tax_chap", dom, "2" );

							if( mVAL != null && mVal.trim().length() > 0 )
							{
								sql = "Select Count(*) cnt from taxchap where tax_chap = '" + mVal + "'";

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
							mVal = distCommon.getParentColumnValue("tax_env", dom, "2" );
							String mDate1Str = genericUtility.getColumnValue( "order_date", dom, "2" );
							mdate1 = getDateInForamt( mDate1Str.trim(), appDateFormat );

							if( mVAL != null && mVal.trim().length() > 0 )
							{
								sql = "Select Count(*) cnt from taxenv where tax_env = '" + mVal + "'";

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
									//what to do for this
									//Pavan R 17sept19 start[to validate tax environment]
									//errcode = distCommon.getCheckTaxEnvStatus( mVAL, mdate1, conn );
									errcode = distCommon.getCheckTaxEnvStatus( mVAL, mdate1,"D", conn );
									//Pavan R 17sept19 end[to validate tax environment]
								}
							}
							if( errcode != null && errcode.trim().length() > 0 )
							{
								errString = getErrorString( "tax_env", errcode, userId );
								break;
							}
						}

						if( childNodeName.equalsIgnoreCase( "qty_order" ) )
						{
							lcQtyOrder = Double.parseDouble( genericUtility.getColumnValue( "qty_order", dom, "2" ) );

							lsItemCode = genericUtility.getColumnValue( "item_code", dom, "2" );

							lsTranType = genericUtility.getColumnValue( "tran_type", dom, "2" );

							lsDdlvsite = genericUtility.getColumnValue( "site_code__dlv", dom, "1" );

							if( lcQtyOrder <= 0 )
							{
								errcode = "VTQTY18";
							}
							else
							{
								mval1 = genericUtility.getColumnValue( "sale_order", dom, "2" );

								mval = genericUtility.getColumnValue( "line_no__sord", dom, "2" );

								if( mval != null && mval.trim().length() > 0 )
								{
									//mval = right('    '+mval,3)

									lsDistOrder = genericUtility.getColumnValue( "dist_order", dom, "1" );

									llLineno = Integer.parseInt( genericUtility.getColumnValue( "line_no", dom, "2" ) );

									sql = " select qty_order lc_oldqty from DISTORDER_DET "
												+" where dist_order = '" + lsDistOrder + "'"
												+" and line_no = '" + llLineno + "'";
									pstmt = conn.prepareStatement( sql );
									rs = pstmt.executeQuery();
									if( rs.next( ) )
									{
										lcOldqty = rs.getDouble( "lc_oldqty" );
									}
									else
									{
										lcOldqty = 0;
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									sql = "select sum(quantity) lc_soqty from sorddet "
										+"	where sale_order = '" + mval1.trim() + "' and line_no = '" + mval.trim() + "'";
									pstmt = conn.prepareStatement( sql );
									rs = pstmt.executeQuery();
									if( rs.next( ) )
									{
										lcSoqty = rs.getDouble( "lc_soqty" );
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									sql ="select sum(b.qty_order) lc_dsqty "
										+"	from distorder a, distorder_det b "
										+" where a.dist_order = b.dist_order and "
										+"		a.status <> 'X' and "
										+"		b.sale_order = '" + mval1.trim() + "' and "
										+"		b.line_no__sord = '" + mval.trim() + "'";
									pstmt = conn.prepareStatement( sql );
									rs = pstmt.executeQuery();
									if( rs.next( ) )
									{
										lcDsqty = rs.getDouble( "lc_dsqty" );
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;

									if ( (lcDsqty - lcOldqty) + lcQtyOrder > lcSoqty )
									{
										errcode = "VTSOQTY";
									}
								}
								if( errcode != null && errcode.trim().length() > 0 )
								{
									lsSiteCode = genericUtility.getColumnValue( "site_code__ship", dom, "2" );

									errcode = distCommon.gbfSetIntegralQty( lsItemCode, lsTranType, lcQtyOrder, lsSiteCode, conn );
								}
							}
							if( errcode != null && errcode.trim().length() > 0 )
							{
								errString = getErrorString( "qty_order", errcode, userId );
								break;
							}
						}
						if( childNodeName.equalsIgnoreCase( "qty_confirm" ) )
						{
							lcQtyConfirm = Double.parseDouble( genericUtility.getColumnValue("qty_confirm", dom, "2") );

							lcQtyOrder = Double.parseDouble( genericUtility.getColumnValue("qty_order", dom, "2") );

							if( lcQtyConfirm > lcQtyOrder )
							{
								errcode = "VTDIST18";
							}
							else
							{
								lsTranType = genericUtility.getColumnValue("tran_type", dom, "2"); //dw_edit.getitemstring(1,"tran_type")

								lsItemCode = genericUtility.getColumnValue("item_code", dom, "2");

								lsSiteCode = genericUtility.getColumnValue("site_code__ship", dom, "2");
								errcode = distCommon.gbfSetIntegralQty( lsItemCode, lsTranType, lcQtyConfirm, lsSiteCode, conn );
							}
							if( errcode != null && errcode.trim().length() > 0 )
							{
								errString = getErrorString( "qty_confirm", errcode, userId );
								break;
							}
						}
						if( childNodeName.equalsIgnoreCase( "ship_date" ) )
						{
							String mshipDateStr = genericUtility.getColumnValue("ship_date", dom, "2");
							mshipDate = getDateInForamt( mshipDateStr.trim(), appDateFormat );
							String morderDateStr = genericUtility.getColumnValue("order_date", dom, "2");
							morderDate = getDateInForamt( morderDateStr.trim(), appDateFormat );
							String mdueDateStr = genericUtility.getColumnValue( "due_date", dom, "2" );
							mdueDate = getDateInForamt( mdueDateStr.trim(), appDateFormat );

							if( mshipDate.compareTo( morderDate ) < 0 )
							{
								errcode = "VTDATE8";
							}
							else
							if( mshipDate.compareTo( mdueDate ) > 0 )
							{
								errcode = "VTDATE13";
							}
							if( errcode != null && errcode.trim().length() > 0 )
							{
								errString = getErrorString( "ship_date", errcode, userId );
								break;
							}
						}
					}//END FOR OF CASE2
	     			break;
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
	private java.util.Date getDateInForamt1( String dateStr, String format ) throws Exception
	{
		SimpleDateFormat sdf = new SimpleDateFormat( format );//new SimpleDateFormat(genericUtility.getApplDateFormat());
		System.out.println( "dateStr[" + dateStr + "]" );
		java.util.Date frDate = null;
		frDate = sdf.parse( dateStr );
		return frDate;
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
 }// END OF MAIN CLASS


