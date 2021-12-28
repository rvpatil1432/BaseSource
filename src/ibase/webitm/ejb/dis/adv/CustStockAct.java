package ibase.webitm.ejb.dis.adv;
import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;
import org.w3c.dom.*;
import javax.ejb.*;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.utility.E12GenericUtility;
import ibase.system.config.*;
import ibase.webitm.ejb.sys.*;
import ibase.webitm.ejb.ITMDBAccessEJB;
import java.text.SimpleDateFormat;

import ibase.webitm.ejb.dis.DistCommon;

import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class CustStockAct extends ActionHandlerEJB implements CustStockActLocal, CustStockActRemote
{
	
	String invoiceDate = null;
	String invoiceDateStr = null;
	/*public void ejbCreate() throws RemoteException, CreateException 
	{
		//System.out.println("================ejbCreate() method called=====================");
	}

   	public void ejbRemove()
	{
		
		//System.out.println("================ejbRemove() method called=====================");
	}

   	public void ejbActivate() 
	{
		//System.out.println("================ejbActivate() method called=====================");
	}

   	public void ejbPassivate() 
	{
		
		//System.out.println("================ejbPassivate() method called=====================");
	}*/

    public String actionHandler() throws RemoteException,ITMException
	{
		return "";
	}

	public String actionHandler(String actionType, String xmlString, String xmlString1,String objContext, String xtraParams) throws RemoteException,ITMException
	{
	    System.out.println("Action called..............");
		Document dom = null;
		Document dom1 = null;
		
		String  resString = null;
		try
		{	
			ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility(); 
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				System.out.println( "XML String :" + xmlString );
				dom = genericUtility.parseString( xmlString ); 
			}
			if( xmlString1 != null && xmlString1.trim().length() !=0 )
			{
				System.out.println( "XML String1 :" + xmlString1 );
				dom1 = genericUtility.parseString( xmlString1 ); 
			}			
			System.out.println( "actionType:" + actionType + ":" );
			resString = actionDefault( dom, dom1,objContext, xtraParams );
		}catch(Exception e)										
		{
			System.out.println("Exception :Action :actionHandler(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning from actionMETHOD :actionHandler"+resString);
	    return resString;
	}

	private String actionDefault( Document dom, Document dom1, String objContext, String xtraParams ) throws RemoteException , ITMException
	{
		String schemeCode = null;
		String returnString = "";
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		String fromdate = null;
		Timestamp frmDate = null;
		String todate = null;
		Timestamp toDate = null;
		Timestamp prdTo = null;
		Timestamp prdFrom = null;
		String custCode = null;
		String stateDescr = null;
		String varValueStr = null;
		String invoiceId = null;
		Timestamp tranDate = null;
		double netAmt = 0.0;
		int varValue = 0;
		int cnt = 0;
		String sql = null;
		String tranId = null;
		String dlvFlag = null;
		StringBuffer valueXmlString = null;
		NodeList nlist = null;
		String todateStr = null;
		String fromdateStr = null;
		Timestamp ToDate = null;
		Timestamp FromDate = null;
		Timestamp invoiceDateT = null;
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB(); 
		
		System.out.println("\t =====================================================");
		System.out.println("\t actionDefault Starts .........");
		System.out.println("\t =====================================================");

		ConnDriver connDriver = new ConnDriver();
		try{
			ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility(); 
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver = null;
			if( "2".equalsIgnoreCase( objContext ) )
			{
				custCode = genericUtility.getColumnValue( "cust_code", dom1 );
				fromdate = genericUtility.getColumnValue( "from_date", dom1 );
				todate = genericUtility.getColumnValue( "to_date", dom1 );
				System.out.println("custCode..." + custCode + "...fromDate..." + fromdate + "...todate...." + todate);
				todateStr = genericUtility.getValidDateString(todate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat());
				fromdateStr = genericUtility.getValidDateString(fromdate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat());
				toDate = java.sql.Timestamp.valueOf(todateStr + " 00:00:00.00");
				frmDate = java.sql.Timestamp.valueOf(fromdateStr + " 00:00:00.00");
				System.out.println("dates in time stamp" + "...fromDate..." + frmDate + "...todate...." + toDate);
				ibase.webitm.ejb.dis.DistCommon dist = new ibase.webitm.ejb.dis.DistCommon();
				UtilMethods utilMethod = null;
				utilMethod = new UtilMethods();
				varValueStr =  dist.getDisparams("999999","SSD_TRANSIT_DAYS",conn);
				System.out.println("varValue.." + varValueStr);
				if( varValueStr.equalsIgnoreCase("NULLFOUND") )
				{
					System.out.println("varValue is NULLFOUND" + varValueStr);
				}
				else
				{
					varValue = Integer.parseInt(varValueStr);
				}
				Timestamp compDate = null;
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(genericUtility.getDBDateFormat());
				System.out.println("varValue = .. " + varValue);
				//prdFrom = utilMethod.RelativeDate(frmDate,varValue);
				// changed by pankaj singh to calculate prd_from  && prd_to for DI89SUN149
				varValue = (-1) * varValue; 
				System.out.println("changed varValue for to date>>>>>>>>>>>>>>>>  = .. " + varValue);
				compDate = utilMethod.RelativeDate(toDate,varValue);
				
				String chk;
				
				
				varValue = (-1) * 1; 
				System.out.println("changed varValue>>>>>>>>>>>>>>>>  = .. " + varValue);
				prdFrom = utilMethod.RelativeDate(frmDate,varValue);
				System.out.println("calculated prdFrom>>>>>>>>>>>>>>>>  = .. " + prdFrom);
				sql = " select FR_DATE , TO_DATE from period "
							+ " where ?"
							+ " between FR_DATE and TO_DATE";
				System.out.println("sql for frm_date& todate>>>>>>>>" +sql);
				pstmt = conn.prepareStatement(sql); 
				pstmt.setTimestamp(1,prdFrom);
				rs = pstmt.executeQuery();
				while( rs.next() )
				{
					prdFrom = rs.getTimestamp("FR_DATE"); 
					prdTo = rs.getTimestamp("TO_DATE"); 
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				System.out.println("prd from>>>>>>>> .. " + prdFrom + "..prd to>>>>>>>>> .." + prdTo);
				// end changed by pankaj singh to calculate prd_from  && prd_to for DI89SUN149 06/02/09
				sql = "SELECT"  
						+ " invoice.invoice_id invoice_id,"
						+ " invoice.net_amt net_amt,tran_date invoice_date"
						+ " FROM invoice invoice    "
						+ " WHERE ( invoice.cust_code = ?)"
						+ " and ( invoice.confirmed = 'Y' ) "
						+ " and ((invoice.tran_date >= ? )"
						+ " and ( invoice.tran_date <= ?) " 
						+ " or ((invoice.tran_date >= ? )" 
						+ " and ( invoice.tran_date <= ? ))" 
						+ " and (  exists "
						+ " ( SELECT cust_stock_inv.invoice_id "
						+ " FROM cust_stock_inv cust_stock_inv "
						+ " WHERE cust_stock_inv.invoice_id = invoice.invoice_id and cust_stock_inv.dlv_flg = 'N'))) ORDER BY TRAN_DATE"; // qry changed by pankaj on 03/02/209
				pstmt = conn.prepareStatement(sql); 
				pstmt.setString(1,custCode);
				System.out.println("custCode.." + custCode);
				pstmt.setTimestamp(2,frmDate);
				System.out.println("frmDate.." + frmDate);
				pstmt.setTimestamp(3,toDate);
				System.out.println("toDate.." + toDate);
				pstmt.setTimestamp(4,prdFrom);
				System.out.println("prdFrom.." + prdFrom);
				pstmt.setTimestamp(5,prdTo);
				System.out.println("custCode.." + custCode);
				System.out.println("prdTo............." + prdTo);
				rs = pstmt.executeQuery();
				while( rs.next() )
				{
					invoiceId = rs.getString("invoice_id");
					netAmt = rs.getDouble("net_amt");
					invoiceId = ( invoiceId == null || invoiceId.trim().length() == 0 ? "":invoiceId.trim());
					invoiceDateT = rs.getTimestamp("invoice_date"); 

					invoiceDateStr = genericUtility.getValidDateString( invoiceDateT.toString(),genericUtility.getDBDateFormat(),genericUtility.getApplDateFormat());
					valueXmlString.append("<Detail>\r\n");
					valueXmlString.append("<invoiceId>").append("<![CDATA["+ invoiceId + "]]>").append("</invoiceId>");
					// changed by pankaj singh to calculate prd_from  && prd_to for DI89SUN149
					System.out.println("invoiceDateT>>>>>>>>>>>>>>>>  " + invoiceDateT + " compDate>>>>>>>>>>> " + compDate);
					System.out.println("invoiceDateT.compareTo(compDate) " + invoiceDateT.compareTo(compDate));
					if(invoiceDateT.compareTo(compDate) < 0 )
					{
						chk = "Y";
					}
					else
					{
						chk = "N";
					}
					System.out.println("chk>>>>>>>>> " + chk);
					valueXmlString.append("<invoiceDate>").append("<![CDATA["+ invoiceDateStr + "]]>").append("</invoiceDate>");
					valueXmlString.append("<dlvFlag>").append(chk).append("</dlvFlag>");
					// end changed by pankaj singh to calculate prd_from  && prd_to for DI89SUN149 06/02/09
					valueXmlString.append("<netAmt>").append("<![CDATA["+ netAmt + "]]>").append("</netAmt>");
					valueXmlString.append("</Detail>\r\n");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}
			else if( "3".equalsIgnoreCase( objContext ) )
			{
				String hdrItemSer = null;
				//If in header there is item series entered then directly call transform
				
				hdrItemSer = genericUtility.getColumnValue( "item_ser", dom1 );
				System.out.println( "hdrItemSer :: " + hdrItemSer ); 
				String itemSer = null;
				String descr = null;
				if( hdrItemSer == null || hdrItemSer.trim().length() == 0 )
				{
					/*
					sql = "select ITEM_SER, DESCR from itemser";          
					pstmt = conn.prepareStatement( sql ); 
					rs = pstmt.executeQuery();
					while( rs.next() )
					{
						itemSer = rs.getString( "ITEM_SER" );
						descr = rs.getString( "DESCR" );
						valueXmlString.append("<Detail>\r\n");
						valueXmlString.append("<item_ser>").append("<![CDATA["+ itemSer + "]]>").append("</item_ser>");
						valueXmlString.append("<descr>").append("<![CDATA["+ descr + "]]>").append("</descr>");
	 					valueXmlString.append("</Detail>\r\n");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					*/
				}
				else
				{
					String tStr = actionTransformForItem( dom, dom1, objContext, xtraParams, null, conn ).toString();
					System.out.println( "tStr :: " + tStr ); 
					valueXmlString.append( tStr );
				}
				
			}
			
			valueXmlString.append("</Root>\r\n");
			System.out.println("valueXmlString..." + valueXmlString);
		}catch( Exception e )
		{
			e.printStackTrace();
		}
		finally
		{
				try
				{
					if( rs != null)
						rs.close();
					rs = null;
					if( pstmt != null )
						pstmt.close();
					pstmt = null;
					if( conn != null)
						conn.close();
					conn = null;	
				}
				catch(SQLException sqle)
				{
					sqle.printStackTrace();
				}
		}
		return valueXmlString.toString();
	}
	
	 public String actionHandlerTransform(String actionType, String xmlString, String xmlString1, String objContext, String xtraParams, String selDataStr) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document selDataDom = null;
		String  retString = null;
		System.out.println("xmlString......" + xmlString);
		System.out.println("selDataStr...." + selDataStr);
		try
		{
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				dom = new  ibase.utility.E12GenericUtility().parseString(xmlString); 
				dom1 = new  ibase.utility.E12GenericUtility().parseString(xmlString1);
				System.out.println("selDataStr......" + selDataStr);
				if(selDataStr != null && selDataStr.length() > 0)
				{
					System.out.println("selDataStr...." + selDataStr);
					selDataDom = new  ibase.utility.E12GenericUtility().parseString(selDataStr);
					System.out.println("selDataDom after parsing....." + selDataDom);
				}
			}
			System.out.println("actionType:"+actionType+":");
			System.out.println("selDataDom:"+selDataDom+":");
			
			retString = actionTransform(dom, dom1, objContext, xtraParams, selDataDom);
		}
	   	catch(Exception e)
		{
			System.out.println("Exception :CustStock :actionHandlerTransform(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning String from CustStock : actionHandlerTransform"+retString);
	    return retString;
	}
	
	private String actionTransform(Document dom, Document dom1, String objContext, String xtraParams, Document selDataDom)throws ITMException
	{
		String schemeCode = null;
		String returnString = "";
		Connection conn = null;
		ResultSet rs = null;
		Statement stmt = null;
		String invoiceId = null;
		String tranId = null;
		String dlvFlag = null;
		String netAmt = null;
		int cnt = 0;	
		NodeList detailList = null;
		int detailListLength = 0;
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		ConnDriver connDriver = new ConnDriver();
		ITMDBAccessEJB itmDbAccess = new ITMDBAccessEJB();  
		String userId = null;
		try
		{
			System.out.println("actionTransform called.........");
			//Changes and Commented By Bhushan on 13-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 13-06-2016 :END
			connDriver = null;
			
			userId = new  ibase.utility.E12GenericUtility().getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			detailList = selDataDom.getElementsByTagName("Detail");
			detailListLength = detailList.getLength();
			if( "2".equalsIgnoreCase( objContext ) )
			{
				for (int ctr = 0;ctr < detailListLength;ctr++)
				{
					invoiceId = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("invoiceId",detailList.item(ctr));
					dlvFlag = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("dlvFlag",detailList.item(ctr));
					invoiceDate = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("invoiceDate",detailList.item(ctr));
					netAmt = new  ibase.utility.E12GenericUtility().getColumnValueFromNode("netAmt",detailList.item(ctr));
					valueXmlString.append("<Detail>");
					valueXmlString.append("<invoice_id isSrvCallOnChg='1'>").append(invoiceId).append("</invoice_id>");
					valueXmlString.append("<invoice_date>").append(invoiceDate).append("</invoice_date>");
					valueXmlString.append("<dlv_flg>").append(dlvFlag).append("</dlv_flg>");
					valueXmlString.append("<net_amt>").append(netAmt).append("</net_amt>");
					valueXmlString.append("</Detail>");
				}
			}
			else if( "3".equalsIgnoreCase( objContext ) )
			{
				valueXmlString.append( actionTransformForItem( dom, dom1, objContext, xtraParams, selDataDom, conn ) );
			}
			
			valueXmlString.append("</Root>\r\n");				
		}
		catch (Exception e)
		{
			System.out.println("Exception CustStock "+e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if( conn != null)conn.close();
				conn = null;
			}
			catch (Exception se){}
		}
		
		System.out.println("valueXmlString11111111..." + valueXmlString);
		return valueXmlString.toString();
	} 
	
	private StringBuffer actionTransformForItem( Document dom, Document dom1, String objContext, String xtraParams, Document selDataDom, Connection conn )throws Exception
	{
		String lsCode = null, lsDescr = null, lsItemSer = null, kfld1 = null, errCode = null,
				lsunit = null, lslocType = null, lsSort = null;
		String lsCustCode = null, lsHdrSer = null,
			   lsSiteCode = null, lsOthSer = null, mTranIdLast = null, lsItemCode = null;
		String kFld1 = null;
		int rowNum = 0, mTotRows = 0, mCount = 0, mChk = 0, llCc = 0, llRc = 0;
		double mopStk = 0.0, mquant = 0.0;
		
		Timestamp ldOrderDate = null;
		boolean	lbFlag = false;
		String mCustCode = null, mSiteCode = null, mItemSer = null, mErrCode = null,
				mCode = null, mDescr = null, mUnit = null, mlType = null;
		String ldOrderDateStr = null;
		String mFrDateStr = null, mToDateStr = null;
		Timestamp ldTranDate = null, mFrDate = null, mToDate = null;

		String sql = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		ibase.utility.E12GenericUtility genericUtility= new ibase.utility.E12GenericUtility();
		StringBuffer strBuff = new StringBuffer();


		lsCustCode = genericUtility.getColumnValue( "cust_code", dom1 );
		lsHdrSer = genericUtility.getColumnValue( "item_ser", dom1 );
		lsHdrSer = ( lsHdrSer == null || lsHdrSer.trim().length() == 0 ? "" : lsHdrSer );
		
		mFrDateStr = genericUtility.getColumnValue( "from_date", dom1 );
		mToDateStr = genericUtility.getColumnValue( "to_date", dom1 );
		
		ldOrderDateStr = genericUtility.getColumnValue( "tran_date", dom1 );
		lsSiteCode = genericUtility.getColumnValue( "site_code", dom1 );
		String trnofld = "tran_id";
		kFld1 = genericUtility.getColumnValue( trnofld, dom1  );
		
		mTranIdLast = genericUtility.getColumnValue( "tran_id_last", dom1 );
		mCustCode 	= lsCustCode;
		mSiteCode 	= lsSiteCode;
		
		NodeList detBrowMain = dom1.getElementsByTagName( "Detail2" ); 
		mTotRows = detBrowMain.getLength();
		
		ldOrderDateStr = genericUtility.getValidDateString( ldOrderDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat() );
		ldOrderDate = java.sql.Timestamp.valueOf( ldOrderDateStr + " 00:00:00.00" );


		mFrDateStr = genericUtility.getValidDateString( mFrDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat() );
		mFrDate = java.sql.Timestamp.valueOf( mFrDateStr + " 00:00:00.00" );

		mToDateStr = genericUtility.getValidDateString( mToDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat() );
		mToDate = java.sql.Timestamp.valueOf( mToDateStr + " 00:00:00.00" );
		
		ldTranDate  = ldOrderDate;

		
		//if( mTotRows == 0 )
		//{
			/*
			if( lsHdrSer == null || lsHdrSer.trim().length() == 0 && selDataDom != null )
			{
				NodeList selItemSer = selDataDom.getElementsByTagName( "item_ser" );
				
				int selLength = selItemSer.getLength();
				
				System.out.println( "selLength :: " + selLength );
				lsSort = "";
				for( int selIdx = 0; selIdx < selLength ; selIdx++ )
				{
					String itemSer = selItemSer.item( selIdx ).getFirstChild().getNodeValue();
					lsSort = lsSort + itemSer + ( selIdx == selLength ? "" : "|" );
				}

				if( lsSort.equalsIgnoreCase( "C" ) )
				{
					return null;
				}
				lsHdrSer = lsSort;
				
				lbFlag = true;
			}
			*/
			ArrayList defData = null;
			
			defData = gbfGetDefaultData( lsCustCode, lsHdrSer, lsSiteCode, conn );

			llRc = defData.size();
			for( llCc = 0; llCc < llRc; llCc++ ) 
			{
				strBuff.append( "<Detail>" );
				strBuff.append( "<line_no>" ).append( llCc + 1 ).append( "</line_no>\n" );
				

				lsItemCode = ( ( ItemDescr )defData.get( llCc ) ).lsCode; //lds_custstock.getitemstring(ll_cc, "item_code")
				strBuff.append( "<item_code isSrvCallOnChg='1'>" ).append( lsItemCode ).append( "</item_code>" );
				
				mCode 	 	= lsItemCode;
				mItemSer = ( new DistCommon() ).getItemSer( mCode, mSiteCode, ldTranDate, mCustCode, "C", conn );
				strBuff.append( "<item_ser>" ).append( mItemSer ).append( "</item_ser>" );

				sql = "Select descr, unit, loc_type from item "
					+" 	where item_code = '" + mCode + "'";
					   
				pstmt = conn.prepareStatement( sql );
				
				rs = pstmt.executeQuery( );
				
				if( rs.next() )
				{
					mDescr = rs.getString( "descr" );
					mUnit = rs.getString( "unit" );
					mlType = rs.getString( "loc_type" );
				}
				
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
				strBuff.append( "<item_descr>" ).append( mDescr ).append( "</item_descr>" );
				strBuff.append( "<unit>" ).append( mUnit ).append( "</unit>" ); 
				strBuff.append( "<loc_type>" ).append( mlType ).append( "</loc_type>" ); 
								
				sql = "select cl_stock from cust_stock_det "
					  +" where tran_id = '" + mTranIdLast + "'"
					  +" 	and item_code = '" + mCode + "'";

				pstmt = conn.prepareStatement( sql );
				
				rs = pstmt.executeQuery( );
				
				if( rs.next() )
				{	 
					mopStk = rs.getDouble( "cl_stock" );
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				strBuff.append( "<op_stock>" ).append( mopStk ).append( "</op_stock>" ); 
				
				//strBuff.append( "<cl_stock>" ).append( mopStk ).append( "</cl_stock>" ); 
					  				
				sql = "select sum(b.quantity__stduom) qty_stduom from invoice a, invdet b "
					  +"	where a.invoice_id = b.invoice_id "
					  +"		and a.cust_code = '" + mCustCode + "'"
					  +"	    and a.tran_date >= ? "
					  +"	    and a.tran_date <= ? "
					  +"		and b.item_code = '" + mCode + "'";
					  
				pstmt = conn.prepareStatement( sql );
				
				pstmt.setTimestamp( 1, mFrDate );
				pstmt.setTimestamp( 2, mToDate );

				rs = pstmt.executeQuery( );
				
				if( rs.next() )
				{	
					mquant = rs.getDouble( "qty_stduom" );
				}
				else
				{
					mquant = 0;
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
				double purcRcp = mquant;

				strBuff.append( "<purc_rcp>" ).append( mquant ).append( "</purc_rcp>" ); 
								
				mquant = 0.0;
				sql = "select sum(b.quantity__stduom) qty_stduom from sreturn a, sreturndet b "
					+"	where a.tran_id = b.tran_id "
					+"	  and a.cust_code = '" + mCustCode + "'"
					+"    and a.tran_date >= ?"
					+"    and a.tran_date <= ?"
					+"	  and b.item_code = '" + mCode + "'";
					
				pstmt = conn.prepareStatement( sql );
				
				pstmt.setTimestamp( 1, mFrDate );
				pstmt.setTimestamp( 2, mToDate );
				
				rs = pstmt.executeQuery( );
				
				if( rs.next() )
				{	
					mquant = rs.getDouble("qty_stduom");
				}
				else
				{
					mquant = 0;
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
				strBuff.append( "<purc_ret>" ).append( mquant ).append( "</purc_ret>" ); 
				strBuff.append( "<tran_id>" ).append( kFld1 ).append( "</tran_id>" ); 
				
				System.out.println("Auto stock mopStk::::["+mopStk+"]purcRcp["+purcRcp+"]mquant["+mquant+"]");
				double clStk = mopStk + purcRcp - mquant;
				
				System.out.println("Auto stock clStk::::["+clStk+"]");
				strBuff.append( "<cl_stock>" ).append( clStk ).append( "</cl_stock>" ); 
									
				strBuff.append( "</Detail>" );
			}
		//}
			
			System.out.println("strBuff1111111..." + strBuff);
		return strBuff;
	}
	private ArrayList gbfGetDefaultData( String asCustCode, String asItmSer, String asSiteCode, Connection conn )
	{
		long llCount = 0, llRowNum = 0;
		String lsErrCode = null, lsCode = null, lsDescr = null, lsUnit = null, lsLocType = null,
				lsItemSer = null;
		String sql = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ItemDescr itemDescr = null;
		ArrayList defData = new ArrayList();
		//lds_custstock = create nvo_datastore
		//lds_custstock.reset()
		//lds_custstock.dataobject = 'd_cust_stock_det_brow'
		//lds_custstock.settransobject(sqlca)
		
		sql = "select count(*) cnt from customeritem where cust_code = '" + asCustCode + "'";

		try{
			pstmt = conn.prepareStatement( sql );
			
			rs = pstmt.executeQuery();
			
			if( rs.next() )
			{
				llCount = rs.getInt( "cnt" );
			}
			
		}catch( Exception ex )
		{
			llCount = 0;
			ex.printStackTrace();
		}
		finally
		{
			try{
				rs.close();
				rs = null;
				
				pstmt.close();
				pstmt = null;
			}catch( Exception e ){ e.printStackTrace(); }
		}
		
		if( llCount == 0 )
		{
			//declare site_item_cursor cursor for  
			sql = " select a.item_code, b.descr, b.unit, b.loc_type, b.item_ser "
				 +"		from siteitem a, item b "
				 +"	where a.item_code = b.item_code "
				 +"		and a.site_code = '" + asSiteCode + "'"
				 +"		and instr( '" + asItmSer + "', rtrim( b.item_ser ) ) > 0 "
				 +"		order by a.item_ser,b.descr ";
		}
		else
		{
			sql = "select a.item_code, b.descr, b.unit, b.loc_type, b.item_ser "
				+"		from customeritem a, item b "
				+"  where a.item_code = b.item_code "
				+"		and a.cust_code = '" + asCustCode + "'" 
				+"		and instr( '" + asItmSer + "', rtrim(b.item_ser)) > 0 " 
				+"		and b.active = 'Y' "
				+"	order by b.descr ";	
		}
		try
		{
			pstmt = conn.prepareStatement( sql );
			rs = pstmt.executeQuery();
			while( rs.next() )
			{
				itemDescr = new ItemDescr();
				
				itemDescr.lsCode = rs.getString( "item_code" );
				itemDescr.lsDescr = rs.getString( "descr" );
				itemDescr.lsUnit = rs.getString( "unit" );
				itemDescr.lsLocType = rs.getString( "loc_type" );
				itemDescr.lsItemSer = rs.getString( "item_ser" );
				
				defData.add( itemDescr );
				//ll_row_num = lds_custstock.insertrow(0)
				/*
				strBuff.append( "<Detail>" );
				strBuff.append( "<line_no>" ).append( llRowNum ).append( "</line_no>\n" );
				strBuff.append( "<item_code>" ).append( lsCode ).append( "</item_code>" );
				strBuff.append( "<item_descr>" ).append( lsDescr ).append( "</item_descr>" );
				strBuff.append( "<unit>" ).append( lsUnit ).append( "</unit>" ); 
				strBuff.append( "<loc_type>" ).append( lsLocType ).append( "</loc_type>" ); 
				strBuff.append( "<item_ser>" ).append( lsItemSer ).append( "</item_ser>" ); 
				strBuff.append( "</Detail>" );	
				*/
			}
		}catch( Exception ex )
		{
			ex.printStackTrace();
		}finally
		{
			try{
				rs.close();
				rs = null;
				
				pstmt.close();
				pstmt = null;
			}catch( Exception e ){ e.printStackTrace(); }
		}
		//return strBuff.toString();
		
		System.out.println("defData111111..." + defData);
		return defData;
	}
	private class ItemDescr
	{
		public String lsCode = null;
		public String lsDescr = null;
		public String lsUnit = null;
		public String lsLocType = null;
		public String lsItemSer = null;
	}
}       