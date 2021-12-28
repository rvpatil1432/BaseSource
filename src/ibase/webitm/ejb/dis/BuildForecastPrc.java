package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.w3c.dom.Document;

import ibase.utility.BaseException;
import ibase.utility.BaseLogger;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ProcessEJB;
import ibase.webitm.ejb.mfg.InvDemSuppTraceBean;
import ibase.webitm.ejb.sys.UtilMethods;
import ibase.webitm.utility.ITMException;
import ibase.webitm.utility.TransIDGenerator;

/**
 * * 
 * And object of this class acts as to set data,delete data,to get quantity for sales forecast, . 
 * * 
 * @author Shaikh Sadique
 */
public class BuildForecastPrc extends ProcessEJB
{
	private E12GenericUtility genericUtility = new E12GenericUtility();
	private UtilMethods utilMethods = new UtilMethods();
	
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		String retStr = "";
		Document detailDom = null;
		Document detailAllDom = null;
		try
		{	
			BaseLogger.log( "3", getUserInfo(), null, "Header XML String : ["+ xmlString +"]" );
			if( xmlString != null && xmlString.trim().length() > 0 )
			{
				detailDom = genericUtility.parseString(xmlString); 
			}
			BaseLogger.log( "3", getUserInfo(), null, "Detail XML String : ["+ xmlString2 +"]" );
			if( xmlString2 != null && xmlString2.trim().length() > 0 )
			{
				detailAllDom = genericUtility.parseString(xmlString2); 
			}
			retStr = process( detailDom, detailAllDom, windowName, xtraParams );
		}
		catch (Exception e)
		{
			BaseLogger.log( "0", getUserInfo(), null, "Exception : BuildForecast :: process : ["+ e.getMessage() +"]" );
			throw new ITMException(e);
		}
		return retStr;
	}

	public String process( Document detailDom, Document detailAllDom, String windowName, String xtraParams ) throws RemoteException,ITMException
	{
		String resultString = "";
		Connection conn = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		boolean errFlag = false;
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		try 
		{
			String period = "", maxPeriod = "", minPeriod = "", siteCode = "", itemCode = "", itemSer = "", compStr = "",
					keyStr = "", chkDup = "", oldChkDup = "", insert = "", oldCompStr = "", oldKeyStr = "", unit = "", prdListStr = "", tranId = "";

			String prdCodeFrom = checkNull( genericUtility.getColumnValue( "prd_code__fr", detailDom ) );
			String prdCodeTo = checkNull( genericUtility.getColumnValue( "prd_code__to", detailDom ) );
			String siteCodeFrom = checkNull( genericUtility.getColumnValue( "site_code__fr", detailDom ) );
			String siteCodeTo = checkNull( genericUtility.getColumnValue( "site_code__to", detailDom ) );
			String itemSerFrom = checkNull( genericUtility.getColumnValue( "item_ser__fr", detailDom ) );
			String itemSerTo = checkNull( genericUtility.getColumnValue( "item_ser__to", detailDom ) );
			String prdCodeUpto = checkNull( genericUtility.getColumnValue( "prd_code__upto", detailDom ) );
			String overwrite = checkNull( genericUtility.getColumnValue( "overwrite", detailDom ) );
			int scanVal = Integer.parseInt( checkNull( genericUtility.getColumnValue( "scan", detailDom ) ) );

			conn = getConnection();
			conn.setAutoCommit( false );

			Date fromDate = null;
			String sql = "SELECT FR_DATE FROM PERIOD WHERE CODE = ?";
			pstm = conn.prepareStatement(sql);
			pstm.setString(1, prdCodeFrom);
			rs = pstm.executeQuery();
			while ( rs.next() ) 
			{
				fromDate = rs.getDate("FR_DATE");
			}
			if ( pstm != null )
			{
				pstm.close();
				pstm = null;
			}
			if ( rs != null )
			{
				rs.close();
				rs = null;
			}

			for ( int ctr = 1; ctr <= scanVal; ctr++ )
			{
				period = getPeriodCode( prdCodeFrom, fromDate, ctr, conn );
				if ( ctr == 1 )
				{
					maxPeriod = period;
					prdListStr = "'"+period+"'";
				}
				else
				{
					prdListStr = prdListStr + ",'" + period +"'";
				}
				if ( prdCodeUpto.length() > 0 )
				{
					if ( Integer.parseInt( maxPeriod ) > Integer.parseInt( prdCodeUpto ) )
					{
						maxPeriod = prdCodeUpto;
					}
				}
				if ( ctr == scanVal )
				{
					minPeriod = period;
				}
			}

			HashMap<Integer, ForecastBean> periodDataMap = getPeriodCode( prdCodeFrom, prdCodeTo, detailDom, conn );
			
			//Arguement change by sadique shaikh 21-6-2019
			HashMap<Integer, DWHSalesSumBean> dwhSalesSumBeanMap = buildDWHSalesSumData( siteCodeFrom, siteCodeTo, minPeriod, maxPeriod, itemSerFrom, itemSerTo, conn );

			windowName = checkNull( windowName );
			if ( windowName.length() == 0 )
			{
				windowName = "W_SALESFORECAST";
			}
			HashMap<String, String> transetupDetailMap = getTransetupDetails( windowName, conn );

			for ( Map.Entry<Integer, DWHSalesSumBean> entry : dwhSalesSumBeanMap.entrySet() )
			{
				BaseLogger.log( "3", getUserInfo(), null, "dwhSalesSumBeanMap Key : ["+ entry.getKey() +"]" );
				DWHSalesSumBean dwhSalesSumBean = entry.getValue();

				siteCode = dwhSalesSumBean.getSiteCode();
				itemCode = dwhSalesSumBean.getItemCode();
				itemSer = dwhSalesSumBean.getItemSer();
				unit = dwhSalesSumBean.getItemUnit();
				
				compStr = checkNull(siteCode) + checkNull(itemCode) + checkNull(itemSer);
				keyStr = checkNull(siteCode) + checkNull(itemSer) + checkNull(prdCodeFrom);
				chkDup = checkNull(siteCode) + checkNull(itemSer);

				insert = "Y";
				BaseLogger.log( "3", getUserInfo(), null, "ls_comp_str : ["+ compStr +"], ls_key_str : ["+ keyStr +"], ls_chk_dup : ["+ chkDup +"]" );
				BaseLogger.log( "3", getUserInfo(), null, "ls_old_chk_dup : ["+ oldChkDup +"]" );
				if ( ! chkDup.equals( oldChkDup ) && "Y".equals( overwrite ) )
				{
					resultString = removeSalesforecast( siteCode, itemSer, prdCodeFrom, conn );
				}
				oldCompStr = checkNull( oldCompStr );
				BaseLogger.log( "3", getUserInfo(), null, "ls_old_comp_str : ["+ oldCompStr +"]" );
				if ( ! oldCompStr .equals( compStr ) )
				{
					BaseLogger.log( "3", getUserInfo(), null, "ls_old_key_str : ["+ oldKeyStr +"]" );
					if ( ! oldKeyStr.equals( keyStr ) )
					{
						insert = "Y";
						tranId = generateTranId( transetupDetailMap, getUserInfo().getSiteCode(), getUserInfo().getLoginCode(), conn );
					}
					else
					{
						insert = "N";
					}
					resultString = insertSalesForecast( detailDom, detailAllDom, tranId, siteCode, itemCode, prdCodeFrom, prdCodeTo, period, scanVal, itemSer, insert, unit, prdListStr, periodDataMap, conn,fromDate );//fromDate added by nandkumar gadkari on 30/10/19
					BaseLogger.log( "3", getUserInfo(), null, "resultString : ["+ resultString +"]" );
					if( resultString != null && resultString.length() > 0 )
					{
						break;
					}
				}
				oldCompStr = compStr;
				oldKeyStr = keyStr;
				oldChkDup = chkDup;
			}
		}
		catch ( Exception e ) 
		{
			BaseLogger.log( "0", getUserInfo(), null, "BuildForecast.process()["+ e.getMessage() +"]" );
			try 
			{
				if ( conn != null )
				{
					conn.rollback();
				}
				errFlag = true;
			}
			catch (SQLException e1) 
			{
				e1.printStackTrace();
			}
			throw new ITMException( e );
		}
		finally
		{
			try 
			{
				if( errFlag )
				{
					resultString = "PROCFAIL";
					resultString = itmDBAccessEJB.getErrorString( "", resultString, getUserInfo().getLoginCode(), "", conn );
				}
				else
				{
					if( resultString == null || resultString.length() == 0 )
					{
						resultString = "PROCSUCC";
					}
					resultString = itmDBAccessEJB.getErrorString( "", resultString, getUserInfo().getLoginCode(), "", conn );
				}
				if ( pstm != null )
				{
					pstm.close();
					pstm = null;
				}
				if ( rs != null )
				{
					rs.close();
					rs = null;
				}
				if ( conn != null )
				{
					conn.commit();

					conn.close();
					conn = null;
				}
			}
			catch (SQLException e) 
			{
				e.printStackTrace();
			}
		}
		return resultString;
	}

	private HashMap<Integer, DWHSalesSumBean> buildDWHSalesSumData( String siteCodeFrom, String siteCodeTo, String fromPeriod, String toPeriod, String fromItemSer, String toItemSer, Connection conn ) throws ITMException
	{
		PreparedStatement pstm = null;
		ResultSet rs = null;
		HashMap<Integer, DWHSalesSumBean> dwhSalesSumBeanMap = new LinkedHashMap<Integer, DWHSalesSumBean>();

		try 
		{
			String sql = "SELECT SALES_SUM.SITE_CODE, SALES_SUM.ITEM_CODE, SALES_SUM.ITEM_SER, ITEM.UNIT "
					+ "FROM DWH_SALES_SUM SALES_SUM, ITEM "
					+ "WHERE SALES_SUM.ITEM_CODE = ITEM.ITEM_CODE "
					+ "AND SALES_SUM.SITE_CODE >= ? AND SALES_SUM.SITE_CODE <= ? "
					+ "AND TO_CHAR(SALES_SUM.DOC_DATE, 'YYYYMM') >= ? AND TO_CHAR(SALES_SUM.DOC_DATE, 'YYYYMM') <= ? "
					+ "AND SALES_SUM.ITEM_SER >= ? AND SALES_SUM.ITEM_SER <= ? "
					+ "AND SALES_SUM.SITE_CODE IS NOT NULL";

			pstm = conn.prepareStatement(sql);
			pstm.setString(1, siteCodeFrom);
			pstm.setString(2, siteCodeTo);
			pstm.setString(3, fromPeriod);
			pstm.setString(4, toPeriod);
			pstm.setString(5, fromItemSer);
			pstm.setString(6, toItemSer);

			rs = pstm.executeQuery();
			int rowCnt = 1;
			while ( rs.next() ) 
			{
				DWHSalesSumBean dwhSalesSumBean = new DWHSalesSumBean();
				dwhSalesSumBean.setSiteCode( rs.getString( "SITE_CODE" ) );
				dwhSalesSumBean.setItemCode( rs.getString( "ITEM_CODE" ) );
				dwhSalesSumBean.setItemSer( rs.getString( "ITEM_SER" ) );
				dwhSalesSumBean.setItemUnit( rs.getString( "UNIT" ) );

				dwhSalesSumBeanMap.put( rowCnt++, dwhSalesSumBean );
			}
		}
		catch (Exception e) 
		{
			throw new ITMException( e );
		}
		finally
		{
			try 
			{
				if ( pstm != null )
				{
					pstm.close();
					pstm = null;
				}
				if ( rs != null )
				{
					rs.close();
					rs = null;
				}
			}
			catch (SQLException e) 
			{
				e.printStackTrace();
			}
		}
		return dwhSalesSumBeanMap;
	}

	private String getPeriodCode( String prdCode, Date fromDate, int months, Connection conn ) throws ITMException 
	{
		String code = "";
		PreparedStatement pstm = null;
		ResultSet rs = null;
		String sql = "";
		try
		{
			Date frDate = null, rtDt = null;
			sql = "SELECT FR_DATE FROM PERIOD WHERE CODE = ?";
			pstm = conn.prepareStatement(sql);
			pstm.setString(1, prdCode);
			rs = pstm.executeQuery();

			if(rs.next())
			{
				frDate = rs.getDate( "FR_DATE" );
			}
			pstm.close();
			pstm = null;

			rs.close();
			rs = null;

			int i = months * (-1);
			rtDt = utilMethods.AddMonths( frDate, i );
			String rtdt = genericUtility.getValidDateString( rtDt, "dd-MMM-yyyy" );
			if( rtDt != null )
			{
				sql = "SELECT CODE FROM PERIOD WHERE FR_DATE <= ? AND TO_DATE >= ?";
				pstm = conn.prepareStatement(sql);
				pstm.setString( 1, rtdt );
				pstm.setString( 2, rtdt );
				rs = pstm.executeQuery();

				if( rs.next() )
				{
					code = rs.getString( "CODE" );
				}

				if ( pstm != null )
				{
					pstm.close();
					pstm = null;
				}
				if ( rs != null )
				{
					rs.close();
					rs = null;
				}
			}
		}
		catch(Exception e)
		{
			BaseLogger.log( "0", getUserInfo(), null, "BuildForecast.gbf_prd()["+ e.getMessage() +"]" );
			throw new ITMException( e );
		}
		finally
		{
			try 
			{
				if ( pstm != null )
				{
					pstm.close();
					pstm = null;
				}
				if ( rs != null )
				{
					rs.close();
					rs = null;
				}
			}
			catch (SQLException e) 
			{
				e.printStackTrace();
			}
		}
		return code;
	}

	private HashMap<Integer, ForecastBean> getPeriodCode( String prdFrom, String prdTo, Document headerDom, Connection conn ) throws ITMException 
	{
		HashMap<Integer, ForecastBean> periodDataMap = new LinkedHashMap<Integer, ForecastBean>();
		
		PreparedStatement pstm = null;
		ResultSet rs = null;
		PreparedStatement pstm1 = null;
		ResultSet rs1 = null;

		try 
		{
			Date frDate = null, rtDt = null;
			String ls_prd = "", ls_code = "";
			float lc_def_perc = 0;
			int li_check = 0, li_months = 0, li_ctr = 0;
			
			String sql = "SELECT FR_DATE FROM PERIOD WHERE CODE = ?";
			pstm = conn.prepareStatement(sql);
			pstm.setString(1, prdFrom);
			rs = pstm.executeQuery();
			if ( rs.next() ) 
			{
				frDate = rs.getDate( "FR_DATE" );
			}
			pstm.close();
			pstm = null;
			rs.close();
			rs = null;

			sql = "SELECT COUNT(CODE) FROM PERIOD WHERE CODE >= ? AND CODE <= ?";
			pstm = conn.prepareStatement(sql);
			pstm.setString(1, prdFrom);
			pstm.setString(2, prdTo);
			rs = pstm.executeQuery();
			if ( rs.next() ) 
			{
				li_check = rs.getInt( 1 );
			}
			pstm.close();
			pstm = null;
			rs.close();
			rs = null;

			li_ctr = li_check;
			
			String defPerc = checkNull( genericUtility.getColumnValue( "def_perc", headerDom ) );
			String defPerc2 = checkNull( genericUtility.getColumnValue( "def_perc2", headerDom ) );
			String defPerc3 = checkNull( genericUtility.getColumnValue( "def_perc3", headerDom ) );
			String defPerc4 = checkNull( genericUtility.getColumnValue( "def_perc4", headerDom ) );

			sql = "SELECT CODE FROM PERIOD WHERE CODE >= ? AND CODE <= ? ORDER BY CODE DESC";
			pstm = conn.prepareStatement(sql);
			pstm.setString(1, prdFrom);
			pstm.setString(2, prdTo);
			rs = pstm.executeQuery();
			while ( rs.next() ) 
			{
				ls_prd = rs.getString( "CODE" );
				ls_code = "";

				if ( li_check == 1 ) 
				{
					// all Integer parsing chnage to float 
					if ( defPerc != null && defPerc.length() > 0 )
					{
						//lc_def_perc = Integer.parseInt( defPerc );
						lc_def_perc = Float.parseFloat( defPerc );
					}
				}
				else if ( li_check == 2 ) 
				{
					if ( defPerc2 != null && defPerc2.length() > 0 )
					{
						//lc_def_perc = Integer.parseInt( defPerc2 );
						lc_def_perc = Float.parseFloat( defPerc2 );
					}
				}
				else if ( li_check == 3 ) 
				{
					if ( defPerc3 != null && defPerc3.length() > 0 )
					{
						//lc_def_perc = Integer.parseInt( defPerc3 );
						lc_def_perc = Float.parseFloat( defPerc3 );
					}
				}
				else if (li_check == 4) 
				{
					if ( defPerc4 != null && defPerc4.length() > 0 )
					{
						//lc_def_perc = Integer.parseInt( defPerc4 );
						lc_def_perc = Float.parseFloat( defPerc4 );
					}
				}

				li_months = (li_ctr - li_check + 1) * (-1);
				rtDt = utilMethods.AddMonths( frDate, li_months );
				String rtdt = genericUtility.getValidDateString( rtDt, "dd-MMM-yyyy" );
				String sql1 = "SELECT CODE FROM PERIOD WHERE FR_DATE <= ? AND TO_DATE >= ?";
				pstm1 = conn.prepareStatement(sql1);
				pstm1.setString( 1, rtdt );
				pstm1.setString( 2, rtdt );
				rs1 = pstm1.executeQuery();
				if ( rs1.next() ) 
				{
					ls_code = rs.getString( "CODE" );
				}
				pstm1.close();
				pstm1 = null;
				rs1.close();
				rs1 = null;

				ForecastBean forecastBean = new ForecastBean();
				forecastBean.setPeriod( prdFrom );
				forecastBean.setFr_date( frDate );
				forecastBean.setCnt( li_check );
				forecastBean.setNextperiod( ls_code );
				forecastBean.setCurrperiod( ls_prd );
				forecastBean.setPercent( lc_def_perc );
				periodDataMap.put( li_check, forecastBean );

				li_check --;
			}

			pstm.close();
			pstm = null;
			rs.close();
			rs = null;
		}
		catch (Exception e) 
		{
			BaseLogger.log( "0", getUserInfo(), null, "BuildForecast.gbf_period()["+ e.getMessage() +"]" );
			throw new ITMException( e );
		}
		finally
		{
			try 
			{
				if ( pstm != null )
				{
					pstm.close();
					pstm = null;
				}
				if ( rs != null )
				{
					rs.close();
					rs = null;
				}
				if ( pstm1 != null )
				{
					pstm1.close();
					pstm1 = null;
				}
				if ( rs1 != null )
				{
					rs1.close();
					rs1 = null;
				}
			} 
			catch (SQLException e) 
			{
				e.printStackTrace();
			}
		}

		return periodDataMap;
	}

	private String removeSalesforecast( String site, String itemSer, String prdCodeFrom, Connection conn ) throws ITMException 
	{
		String retString = "";
		PreparedStatement pstm = null;
		try 
		{
			String sql = "DELETE FROM SALESFORECAST_DET "
					+ "WHERE TRAN_ID IN (SELECT TRAN_ID FROM SALESFORECAST_HDR WHERE SITE_CODE = ? AND ITEM_SER  = ? AND PRD_CODE__FROM = ?)";
			pstm = conn.prepareStatement(sql);
			pstm.setString(1, site);
			pstm.setString(2, itemSer);
			pstm.setString(3, prdCodeFrom);

			int updateCnt = pstm.executeUpdate();
			BaseLogger.log( "3", getUserInfo(), null, "SALESFORECAST_DET deleteCount : ["+ updateCnt +"]" );

			pstm.close();
			pstm = null;

			if ( updateCnt > 0 )
			{
				sql = "UPDATE SALESFORECAST_HDR SET PRD_CODE__FROM = '999999' "
						+ "WHERE SITE_CODE = ? AND ITEM_SER  = ? AND PRD_CODE__FROM = ?";

				pstm = conn.prepareStatement(sql);
				pstm.setString(1, site);
				pstm.setString(2, itemSer);
				pstm.setString(3, prdCodeFrom);

				updateCnt = pstm.executeUpdate();
				BaseLogger.log( "3", getUserInfo(), null, "SALESFORECAST_HDR updateCnt : ["+ updateCnt +"]" );

				pstm.close();
				pstm = null;
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new ITMException( e );
		}
		finally
		{
			try 
			{
				if ( pstm != null )
				{
					pstm.close();
					pstm = null;
				}
			}
			catch (SQLException e) 
			{
				e.printStackTrace();
			}
		}
		return retString;
	}

	private String insertSalesForecast( Document detailDom, Document detailAllDom, String tranId, String siteCode, String itemCode, String prdCodeFrom, String prdCodeTo, String period, int scanVal, String itemSer, String insert, String unit, String prdListStr, HashMap<Integer, ForecastBean> periodDataMap, Connection conn,Date fromDate ) throws ITMException //fromDate added by nandkumar gadkari on 30/10/19
	{
		String retString = "";

		PreparedStatement pstm = null;
		ResultSet rs = null;
		String sql = "";
		//added by nandkumar gadkari on 30/10/19
		double salesQty=0,salesQty1=0,salesQty2=0,salesQty3=0;
		int cnt=0;
		String period1="",period2="",period3="";
		try 
		{
			InvDemSuppTraceBean invDemSupTrcBean = new InvDemSuppTraceBean();
		    HashMap demandSupplyMap = new HashMap();
			String userid = getUserInfo().getLoginCode();
			String termid = getUserInfo().getLoginCode();
			float lc_quantity = 0, lc_perc = 1, qty = 0;

			int hdrInsCont = 0;
			int detInsCont = 0;

			BaseLogger.log( "3", getUserInfo(), null, "tran_id ["+ tranId +"]" );
			if ( "Y".equals( insert ) ) 
			{
				sql = "INSERT INTO SALESFORECAST_HDR (TRAN_ID,TRAN_DATE,SITE_CODE,PRD_CODE__FROM,PRD_CODE__TO,ITEM_SER,EMP_CODE__APRV,CONFIRMED,CONF_DATE,CHG_DATE,CHG_USER,CHG_TERM)" + 
						"values(?,sysdate,?,?,?,?,?,?,?,sysdate,?,?)";
				pstm = conn.prepareStatement(sql);
				pstm.setString(1, tranId);
				pstm.setString(2, siteCode);
				pstm.setString(3, prdCodeFrom);
				pstm.setString(4, prdCodeTo);
				pstm.setString(5, itemSer);
				pstm.setString(6, "");
				pstm.setString(7, "N");
				pstm.setString(8, "");
				pstm.setString(9, userid);
				pstm.setString(10, termid);

				hdrInsCont = pstm.executeUpdate();

				pstm.close();
				pstm = null;
			}
			qty = getQuantity( detailDom, detailAllDom, siteCode, itemCode, prdListStr, itemSer, conn ,scanVal);// scanVal parameter added by nandkumar gadkari on 06/11/19
			BaseLogger.log( "3", null, null, "lc_qty ["+ qty +"]" );
			
			for ( Map.Entry<Integer, ForecastBean> entry : periodDataMap.entrySet() )
			{
				ForecastBean forecastBean = entry.getValue();

				sql = "SELECT GROWTH_PERC  FROM FORECAST_INDICATOR WHERE SITE_CODE = ? AND ITEM_CODE = ? AND PRD_CODE = ?";
				pstm = conn.prepareStatement(sql);
				pstm.setString(1, siteCode);
				pstm.setString(2, itemCode);
				pstm.setString(3, forecastBean.getCurrperiod());
				rs = pstm.executeQuery();
				int rsCnt = 0;
				if( rs.next() )
				{
					lc_perc = rs.getFloat( 1 );
					rsCnt++;
				}
				rs.close();
				rs = null;
				pstm.close();
				pstm = null;

				if ( rsCnt == 0 )
				{
					sql = "SELECT GROWTH_PERC FROM FORECAST_INDICATOR WHERE SITE_CODE = ? AND ITEM_CODE = ? AND PRD_CODE = ?";
					pstm = conn.prepareStatement(sql);
					pstm.setString(1, siteCode);
					pstm.setString(2, "X");
					pstm.setString(3, forecastBean.getCurrperiod());
					rs = pstm.executeQuery();
					rsCnt = 0;
					if( rs.next() )
					{
						lc_perc = rs.getFloat( 1 );
						rsCnt++;
					}
					rs.close();
					rs = null;
					pstm.close();
					pstm = null;

					if ( rsCnt == 0 )
					{
						lc_quantity = qty + (qty * forecastBean.getPercent())/100;
					}
					else if ( lc_perc > 0 )
					{
						lc_quantity = qty + (qty * lc_perc)/100;
					}
				}
				else if ( lc_perc > 0 )
				{
					lc_quantity = qty + (qty * lc_perc)/100;
				}
				lc_quantity = Math.round(lc_quantity);
				BaseLogger.log("3", null, null, " lc_quantity insert value ="+lc_quantity );

				//ADDED BY NANDKUMAR GADKARI ON 30/10/19-------START------------
						
				cnt=0;
				
					
					 sql ="SELECT SUM(NVL(SALES_QTY,0) - NVL(SALEABLE_RETURN_QTY,0)) QTY ,TO_CHAR(DOC_DATE,'YYYYMM') FROM DWH_SALES_SUM "
						+ " WHERE ITEM_CODE = ? AND SITE_CODE = ? AND TO_CHAR(DOC_DATE,'YYYYMM') IN ( "+ prdListStr +" ) "
						+ " AND ITEM_SER = ? GROUP BY TO_CHAR(DOC_DATE,'YYYYMM') ORDER BY TO_CHAR(DOC_DATE,'YYYYMM')";
					pstm = conn.prepareStatement(sql);
					pstm.setString(1, itemCode);
					pstm.setString(2, siteCode);
					pstm.setString(3, itemSer);
					rs = pstm.executeQuery();
					while( rs.next() )
					{
						cnt++;
						salesQty = rs.getDouble(1);
						period = rs.getString(2);
						
						if(cnt==1)
						{
							period1=period;
							salesQty1=salesQty;
						}
						if(cnt==2)
						{
							period2=period;
							salesQty2=salesQty;
						}
						if(cnt==3)
						{
							period3=period;
							salesQty3=salesQty;
						}
						
					}
					rs.close();
					rs = null;
					pstm.close();
					pstm = null;
					
					//ADDED BY NANDKUMAR GADKARI ON 30/10/19-------END------------
				
				try 
				{
					sql = "INSERT INTO SALESFORECAST_DET(TRAN_ID,ITEM_CODE,UNIT,PRD_CODE__PLAN,PRD_CODE__FOR,QUANTITY,QUANTITY_ORG"
							+ ",AVG_SALES,PRD_CODE__1,SALES_QTY_1,PRD_CODE__2,SALES_QTY_2,PRD_CODE__3,SALES_QTY_3,GROWTH_PERC )" + //COLUMNS ADDED BY NANDKUMAR GADKARI ON 30/10/19
							"values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
					pstm = conn.prepareStatement(sql);
					pstm.setString(1, tranId);
					pstm.setString(2, itemCode);
					pstm.setString(3, unit);
					pstm.setString(4, prdCodeFrom);
					pstm.setString(5, forecastBean.getCurrperiod());
					pstm.setFloat(6, lc_quantity);
					pstm.setFloat(7, lc_quantity);
					//ADDED BY NANDKUMAR GADKARI ON 30/10/19-------start------------
					pstm.setFloat(8, qty);
					pstm.setString(9, period1);
					pstm.setDouble(10, salesQty1);
					pstm.setString(11, period2);
					pstm.setDouble(12, salesQty2);
					pstm.setString(13, period3);
					pstm.setDouble(14, salesQty3);
					if ( rsCnt == 0 )
					{
						pstm.setDouble(15, forecastBean.getPercent()); 
					}
					else
					{
						pstm.setDouble(15, lc_perc); 
					}
					
					//ADDED BY NANDKUMAR GADKARI ON 30/10/19-------END------------

					detInsCont += pstm.executeUpdate();
				}
				catch (SQLException e) 
				{
					e.printStackTrace();
				}
				finally
				{
					try 
					{
						if ( pstm != null )
						{
							pstm.close();
							pstm = null;
						}
						if ( rs != null )
						{
							rs.close();
							rs = null;
						}
					} 
					catch (SQLException e) 
					{
						e.printStackTrace();
					}
				}
				/**Added by Pavan Rane 24dec19 start[to update demand/supply in summary table(RunMRP process) related changes]*/
				Timestamp dueDate = null;
				sql = "select fr_date from period where code = ?";
				pstm = conn.prepareStatement(sql);
				pstm.setString(1, forecastBean.getCurrperiod());
				rs = pstm.executeQuery();					
				if( rs.next() )
				{
					dueDate = rs.getTimestamp("fr_date");
				}
				rs.close();
				rs = null;
				pstm.close();
				pstm = null;
			    demandSupplyMap.put("site_code", siteCode);
				demandSupplyMap.put("item_code", itemCode);		
				demandSupplyMap.put("ref_ser", "S-FST");
				demandSupplyMap.put("ref_id", tranId);
				demandSupplyMap.put("ref_line", "NA");
				demandSupplyMap.put("due_date", dueDate);		
				demandSupplyMap.put("demand_qty", (double)lc_quantity);
				demandSupplyMap.put("supply_qty", 0.0);
				demandSupplyMap.put("change_type", "A");
				demandSupplyMap.put("chg_process", "T");
				demandSupplyMap.put("chg_user", userid);
			    demandSupplyMap.put("chg_term", termid);	
			    retString = invDemSupTrcBean.updateDemandSupply(demandSupplyMap, conn);
			    demandSupplyMap.clear();	
			    if(retString != null && retString.trim().length() > 0)
			    {
			    	System.out.println("retString["+retString+"]");
	                return retString;
			    }
			  	/**Added by Pavan Rane 24dec19 end[to update demand/supply in summary table(RunMRP process) related changes]*/
			}
			BaseLogger.log( "3", getUserInfo(), null, "hdrInsCont ; ["+ hdrInsCont +"], detInsCont : ["+ detInsCont +"]" );
		}
		catch (Exception e) 
		{
			BaseLogger.log( "0", getUserInfo(), null, "BuildForecast.gbf_insert_salesforecast()["+ e.getMessage() +"]" );
			throw new ITMException( e );
		}
		finally
		{
			try 
			{
				if ( pstm != null )
				{
					pstm.close();
					pstm = null;
				}
				if ( rs != null )
				{
					rs.close();
					rs = null;
				}
			} 
			catch (SQLException e) 
			{
				e.printStackTrace();
			}
		}
		return retString;
	}

	private HashMap<String, String> getTransetupDetails( String winName, Connection conn ) throws ITMException
	{
		HashMap<String, String> transetupDetailMap = new HashMap<String, String>();
		PreparedStatement lstmt = null;
		ResultSet lrs = null;

		try
		{
			String keyStringQuery = null;
			String tranSer = "";
			String keyString = "";
			String keyCol = "";

			keyStringQuery = "SELECT KEY_STRING, TRAN_ID_COL, REF_SER FROM TRANSETUP WHERE UPPER(TRAN_WINDOW) = ?";
			BaseLogger.log( "3", null, null, "keyStringQuery  :: " + keyStringQuery );
			lstmt = conn.prepareStatement( keyStringQuery );
			lstmt.setString(1,winName);
			lrs = lstmt.executeQuery();
			if( lrs.next() )
			{
				keyString = lrs.getString( "KEY_STRING" );
				keyCol = lrs.getString( "TRAN_ID_COL" );
				tranSer = lrs.getString( "REF_SER" );
			}
			else
			{
				if( lrs != null )
				{
					lrs.close();
					lrs = null;
				}
				if( lstmt != null )
				{
					lstmt.close();
					lstmt = null;
				}
				String sqlStr = "SELECT  KEY_STRING, TRAN_ID_COL, REF_SER  FROM  TRANSETUP WHERE  TRAN_WINDOW = 'GENERAL' ";
				BaseLogger.log("3", null, null, " sqlStr : " + sqlStr );

				lstmt = conn.prepareStatement( sqlStr );
				lrs = lstmt.executeQuery();

				if( lrs.next() )
				{
					keyString = lrs.getString( "KEY_STRING" );
					keyCol = lrs.getString( "TRAN_ID_COL" );
					tranSer = lrs.getString( "REF_SER" );
				}
			}
			lrs.close();
			lrs = null;
			lstmt.close();
			lstmt = null;

			transetupDetailMap.put( "KEY_STRING", keyString );
			transetupDetailMap.put( "TRAN_ID_COL", keyCol );
			transetupDetailMap.put( "REF_SER", tranSer );
		}
		catch(SQLException se)
		{
			//BaseLogger.log("3", userInfo, null,"SQLException :Generating id[failed] : " + "\n" +se.getMessage());
			se.printStackTrace();
			throw new ITMException(se);
		}
		catch(Exception ex)
		{
			//BaseLogger.log("3", userInfo, null,"Exception8:Generating id [failed]:" + "\n" +ex.getMessage());
			ex.printStackTrace();
		}
		finally
		{
			try 
			{
				if (lstmt != null )
				{
					lstmt.close();
					lstmt = null;
				}
				if( lrs != null )
				{
					lrs.close();
					lrs = null;
				}
			}
			catch (SQLException e) 
			{
				e.printStackTrace();
			}
		}
		BaseLogger.log( "3", null, null, "transetupDetailMap :: ["+ transetupDetailMap +"]" );
		return transetupDetailMap;
	}

	private String generateTranId( HashMap<String, String> transetupDetailMap, String xsiteCode, String userID, Connection conn ) throws ITMException
	{
		String tranId = null;
		try
		{
			String tranSer = transetupDetailMap.get( "REF_SER" );
			String keyString = transetupDetailMap.get( "KEY_STRING" );
			String keyCol = transetupDetailMap.get( "TRAN_ID_COL" );

			String tranDate = getCurrdateAppFormat();
			StringBuffer xmlValues = new StringBuffer("<?xml version=\"1.0\" encoding=\"").append(CommonConstants.ENCODING).append("\"?><Root>");
			xmlValues.append("<Header></Header><Detail1><tran_id></tran_id><site_code>").append( xsiteCode ).append("</site_code>"); //added by rupali on 29/03/17 to solving tran id generation issue
			xmlValues.append("<tran_date>").append( tranDate ).append("</tran_date></Detail1></Root>");

			TransIDGenerator tg = new TransIDGenerator( xmlValues.toString(), userID, CommonConstants.DB_NAME );
			tranId = tg.generateTranSeqID( tranSer, keyCol, keyString, conn );
		}
		catch(SQLException se)
		{
			//BaseLogger.log("3", userInfo, null,"SQLException :Generating id[failed] : " + "\n" +se.getMessage());
			se.printStackTrace();
			throw new ITMException(se);
		}
		catch(Exception ex)
		{
			//BaseLogger.log("3", userInfo, null,"Exception8:Generating id [failed]:" + "\n" +ex.getMessage());
			ex.printStackTrace();
		}
		return tranId;
	}

	private String getCurrdateAppFormat()
	{
		String currAppdate ="";
		try
		{
			java.sql.Timestamp currDate = new java.sql.Timestamp(System.currentTimeMillis()) ;
			SimpleDateFormat sdf = new SimpleDateFormat( genericUtility.getDBDateFormat() );
			Object date = sdf.parse( currDate.toString() );
			currDate = java.sql.Timestamp.valueOf( sdf.format(date).toString() + " 00:00:00.0");
			currAppdate = new SimpleDateFormat( genericUtility.getApplDateFormat() ).format( currDate ).toString();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return (currAppdate);
	}

	private float getQuantity( Document dom, Document detailAllDom, String siteCode, String itemCode, String prdListStr, String itemSer, Connection conn,int scanVal ) throws BaseException, RemoteException
	{
		BaseLogger.log("3", null, null, " gbf_quantity method called" );
		PreparedStatement pstm = null;
		ResultSet rs = null;
		
		//remove unused variable by sadique 21-6-2019

		float lc_sales_quantity = 0;
		try 
		{
			int li_ctr = 0;
			String ls_consider_zero = checkNull( genericUtility.getColumnValue( "consider_zero", dom ) );
			
			String sql = "SELECT SUM(NVL(SALES_QTY,0) - NVL(SALEABLE_RETURN_QTY,0)) QTY, COUNT(SALES_QTY) CTR FROM DWH_SALES_SUM "
					+ "WHERE ITEM_CODE = ? AND SITE_CODE = ? AND TO_CHAR(DOC_DATE,'YYYYMM') IN ( "+ prdListStr +" ) AND ITEM_SER = ?";
			pstm = conn.prepareStatement(sql);
			pstm.setString(1, itemCode);
			pstm.setString(2, siteCode);
			pstm.setString(3, itemSer);

			rs = pstm.executeQuery();

			if( rs.next() )
			{
				lc_sales_quantity = rs.getInt("QTY");
				li_ctr = rs.getInt("CTR");

				if ( ls_consider_zero.equalsIgnoreCase( "N" ) )
				{
					//if ( li_ctr > 0 )condition commented and scanVal by nandkumar gadkari on 06/11/19
					if ( scanVal > 0 )
					{
						//lc_sales_quantity = lc_sales_quantity / li_ctr;
						lc_sales_quantity = lc_sales_quantity / scanVal;
					}
					else
					{
						lc_sales_quantity = 0;
					}
				}
				else if ( ls_consider_zero.equalsIgnoreCase( "Y" ) )
				{
					//if ( li_ctr > 0 )condition commented and scanVal by nandkumar gadkari on 06/11/19
					if ( scanVal > 0 )
					{
						//lc_sales_quantity = lc_sales_quantity / li_ctr;
						lc_sales_quantity = lc_sales_quantity / scanVal;
					}
					else
					{
						lc_sales_quantity = 0;
					}
				}
			}
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		finally
		{
			try 
			{
				if ( pstm != null )
				{
					pstm.close();
					pstm = null;
				}
				if ( rs != null )
				{
					rs.close();
					rs = null;
				}
			} 
			catch (SQLException e) 
			{
				e.printStackTrace();
			}
		}
		BaseLogger.log( "3", null, null, "lc_sales_quantity = ["+ lc_sales_quantity +"]" );
		return lc_sales_quantity;
	}
	
	private String checkNull( String input )
	{
		return E12GenericUtility.checkNull( input );
	}
}