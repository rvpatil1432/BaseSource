/********************************************************
	Title : ShipmentLocConf
	Date  : 25/04/2014
	Developer: chandrakant patil
	req id : DI3GSUN047
 ********************************************************/

package ibase.webitm.ejb.dis.adv;

import ibase.system.config.AppConnectParm;
import ibase.system.config.ConnDriver;
import ibase.utility.CommonConstants;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.E12CreateBatchLoadEjb;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.MasterStatefulLocal;
import ibase.webitm.ejb.sys.CreateRCPXML;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.utility.TransIDGenerator;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.xml.rpc.ParameterMode;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
//Changed by sumit on 06/10/12
import org.w3c.dom.*;


@Stateless
public class ShipmentLocConf extends ActionHandlerEJB implements ShipmentLocConfLocal, ShipmentLocConfRemote
{
	ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
	public String confirm(String tranId, String xtraParam, String forcedFlag) throws RemoteException,ITMException
	{
		String retString="";
		String errString = "";
		String sql = "";
		String confirmed = "";
		ConnDriver connDriver = new ConnDriver();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null; 
		String errCode="" ;

		try
		{
			//Changes and Commented By Bhushan on 13-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 13-06-2016 :END
			String userID = genericUtility.getValueFromXTRA_PARAMS( xtraParam, "LoginCode" );
			System.out.println(" forced flag ["+forcedFlag+"]");

			ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();


			// check whether already confirmed or not
			sql = " select confirmed  from   shipment  where  shipment_id = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if( rs.next())
			{
				confirmed = checkNull(rs.getString("confirmed"));
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("@@@@shipment_id["+tranId+"]confirmed["+confirmed+"]");

			if( "Y".equalsIgnoreCase(confirmed))
			{
				errString = itmDBAccessLocal.getErrorString("","VTDIST26","","",conn);
				return errString;
			}
			else
			{


				errCode = confirmShipmentLoc(tranId,1, xtraParam,conn);
				System.out.println("@@@@ confirmQcTransfer::errCode["+errCode+"]");
				if( errCode != null && errCode.trim().length() > 0 )
				{
					conn.rollback();
					System.out.println("@@@@@@@@@ rollback successful...........");
					errString = itmDBAccessLocal.getErrorString("",errCode,"","",conn);
					System.out.println("@@@@@@@@@ confirm failed.....");
				}
				else
				{
					conn.commit();
					//	retString=generateTrackInfo(tranId,conn);  // comment this method by mahendra
					System.out.println("@@@@@@@@@ commit successful...........");
					errString = itmDBAccessLocal.getErrorString("","VTCONFIRM","","",conn);
					System.out.println("@@@@@@@@@ confirm successful...........");
				}
			}
		}
		catch(Exception e)
		{
			try{
				conn.rollback();
			}
			catch(SQLException es)
			{
				System.out.println("Rollback exception : " + es.getMessage() + ":");
			}
			System.out.println("Exception : Shipment Confirm():" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		finally
		{
			System.out.println(" Closing Connection in Shipment Confirm() !!");
			try
			{
				conn.close();
				conn = null;
			}
			catch(Exception se){
				System.out.println(se.getMessage());
				throw new ITMException(se);
			}
		}
		System.out.println("final errString @@@@@"+errString);
		return errString;
	}


	private String confirmShipmentLoc(String tranId, int i, String xtraParams, Connection conn)
			throws RemoteException, ITMException

			{
		PreparedStatement pstmt = null, pstmt1 = null;
		ResultSet rs = null;
		int cnt = 0,updCnt=0;
		String sql = "", sql1 = "", errCode = "";
		Timestamp sysDate = null, lrDate=null,lrDateUpd=null;
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		//ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
		int cntDist=0,cntConsIss=0;
		String confirmed="",lrNo="",lorryNo="",recallFrt="",despId="",lrNoUpd="",lorryNoUpd="",distOrdIssId="",conOrdIssId="";
		double totalFreight=0,gwtDesp=0,gwtDist=0,totGwt=0,grossWeight=0,calcFrt=0,totFrt=0,frtAmt=0,grossWeightCIss=0;

		try 
		{

			System.out.println("@@@@@@@@@@@@@@@ confirmShipmentLoc method called next.....1, tranId["+tranId+"].........");
			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String sysDateStr = sdf.format(currentDate.getTime());
			System.out.println("Now the date is :=>  " + sysDateStr);
			sysDate= Timestamp.valueOf(genericUtility.getValidDateString(sysDateStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");

			
			sql = " select confirmed	, total_freight , lr_no		, lr_date		, recall_frt, lorry_no " +
					" from   shipment  where  shipment_id = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if( rs.next())
			{
				confirmed = checkNull(rs.getString("confirmed"));
				totalFreight = rs.getDouble("total_freight");
				lrNo = checkNull(rs.getString("lr_no"));
				lrDate = rs.getTimestamp("lr_date");
				recallFrt = checkNull(rs.getString("recall_frt"));
				lorryNo = checkNull(rs.getString("lorry_no"));
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			System.out.println("@@@@confirmed["+confirmed+"]totalFreight["+totalFreight+"]lrNo["+lrNo+"]");
			System.out.println("@@@@lrDate["+lrDate+"]recallFrt["+recallFrt+"]lorryNo["+lorryNo+"]");

			// check shipment allocated to at least one despatch or dist issue
			sql = " select count(1) " +
					" from 	 despatch where  shipment_id = ?  and    confirmed = 'Y' ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				cnt = rs.getInt(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			System.out.println("cnt["+cnt+"]");
			
			if( cnt == 0 )  // ll_cnt = 0 then
			{	
				sql = " select count(1) " +
						" from distord_iss where  shipment_id = ? and confirmed = 'Y' ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					cntDist = rs.getInt(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				System.out.println("cntDist["+cntDist+"]");

				if( cntDist == 0 )
				{

					sql = " select count(1) from   CONSUME_ISS " +
							"  where  cons_issue in ( select ref_id from ship_docs where shipment_id = ? ) " +
							" and confirmed = 'Y' ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, tranId);
					rs = pstmt.executeQuery();
					while (rs.next())
					{
						cntConsIss = rs.getInt(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					if( cntConsIss == 0 )  // ll_cnt_dist = 0 then  /// cntDist == 0  change 
					{	
						errCode = "VTSHPALL";   
						return errCode;
					}
				}//end if
			} //end if

			//allocating despatches and distord issue, freight proportionate to the gross weight
			sql = " select nvl(sum(gross_weight),0) " +
					//" into   :lc_gwt_desp " +
					" from   despatch  where  shipment_id = ? and confirmed = 'Y' ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				gwtDesp = rs.getDouble(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			System.out.println("@@@@gwtDesp["+gwtDesp+"]");

			sql = " select nvl(sum(gross_weight),0) " +
					"from   distord_iss where  shipment_id = ? and    confirmed = 'Y' ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				gwtDist = rs.getDouble(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			System.out.println("@@@@gwtDist["+gwtDist+"]");

			// added by cpatil on 25/04/14

			sql = "	 select nvl(sum(gross_weight),0)   from   CONSUME_ISS " +
					"  where  cons_issue in ( select ref_id from ship_docs where shipment_id = ? ) and confirmed = 'Y' ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if( rs.next())
			{
				grossWeightCIss = rs.getDouble(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("@@@@@@@@grossWeightCIss["+grossWeightCIss+"]");

			// end 25/04/14 

			totGwt = gwtDesp + gwtDist + grossWeightCIss;

			System.out.println("@@@@totGwt["+totGwt+"]");


			// for despatch 
			sql = " select desp_id,gross_weight, lr_date, lr_no, lorry_no from   despatch " +
					" where  shipment_id =  ?  and    confirmed = 'Y' ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				despId = rs.getString("desp_id");
				grossWeight = rs.getDouble("gross_weight");
				lrDate = rs.getTimestamp("lr_date");
				lrNo = rs.getString("lr_no");
				lorryNo = rs.getString("lorry_no");


				System.out.println("@@@@totGwt["+totGwt+"]::totalFreight["+totalFreight+"]gwtDesp["+gwtDesp+"]");
				//calculating the freight amt proportionately
				if ( totGwt != 0 )   // lc_tot_gwt <> 0 then
				{
					//lc_calc_frt = round(((lc_frt_amt * lc_gwt_desp) / lc_tot_gwt),0)
					calcFrt = Math.round(((totalFreight * gwtDesp ) / totGwt));
				}
				else
				{
					calcFrt = 0;
				} 

				System.out.println("@@@["+calcFrt+"]");
				//end if
				//this is to check whether total calculated freight doesnot exceed 
				//the total freight during rounding the figure
				totFrt = totFrt + calcFrt;
				System.out.println("@@@totFrt["+totFrt+"]");
				if( totFrt > frtAmt )  // lc_tot_frt > lc_frt_amt then
				{
					calcFrt = frtAmt - ( totFrt - calcFrt );
				} //end if

				System.out.println("@@@calcFrt["+calcFrt+"]");

				if( lrDate != null )
				{
					lrDateUpd = lrDate;
				}

				if( lrNo != null )
				{
					lrNoUpd = lrNo;
				}

				if( lorryNo != null )
				{
					lorryNoUpd = lorryNo;
				}

				System.out.println("@@@@@@[despatch]::lrDate["+lrDate+"]lrNo["+lrNo+"]lorryNo["+lorryNo+"]");
				System.out.println("@@@@@@[despatch]::lrDateUpd["+lrDateUpd+"]lrNoUpd["+lrNoUpd+"]lorryNoUpd["+lorryNoUpd+"]");

				sql1 = " update despatch set freight = ?, lr_no 	= ?	, lr_date = ? ," +
						" lorry_no = ? 	where  desp_id = ? ";
				pstmt1 = conn.prepareStatement(sql1);
				pstmt1.setDouble(1, calcFrt);
				pstmt1.setString(2, lrNoUpd);
				pstmt1.setTimestamp(3, lrDateUpd);
				pstmt1.setString(4, lorryNoUpd);
				pstmt1.setString(5, despId);
				updCnt = pstmt1.executeUpdate();
				pstmt1.close();
				pstmt1 = null;

			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;


			// for distord_iss 
			sql = " select tran_id,gross_weight,lr_date, lr_no, lorry_no from   distord_iss " +
					" where  shipment_id =  ?  and    confirmed = 'Y' ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				distOrdIssId = rs.getString("tran_id");
				grossWeight = rs.getDouble("gross_weight");
				lrDate = rs.getTimestamp("lr_date");
				lrNo = rs.getString("lr_no");
				lorryNo = rs.getString("lorry_no");


				System.out.println("@@@@distOrdIssId["+distOrdIssId+"]::grossWeight["+grossWeight+"]lrDate["+lrDate+"]");

				//calculating the freight amt proportionately
				System.out.println("@@@@totGwt["+totGwt+"]::totalFreight["+totalFreight+"]lrNo["+lrNo+"]");
				if( totGwt != 0 )  // lc_tot_gwt <> 0 then
				{
					calcFrt = Math.round((( totalFreight * grossWeight) / totGwt));
				}//end if

				System.out.println("calcFrt["+calcFrt+"]");
				//this is to check whether total calculated freight doesnot exceed 
				//the total freight during rounding the figure

				totFrt = totFrt + calcFrt;
				System.out.println("totFrt["+totFrt+"]");
				if( totFrt > totalFreight ) // lc_tot_frt > lc_frt_amt then
				{
					calcFrt = totalFreight - ( totFrt - calcFrt ) ;
				}

				System.out.println("calcFrt["+calcFrt+"]totalFreight["+totalFreight+"]totFrt["+totFrt+"]");

				if( lrDate != null )
				{
					lrDateUpd = lrDate;
				}

				if( lrNo != null )
				{
					lrNoUpd = lrNo;
				}

				if( lorryNo != null )
				{
					lorryNoUpd = lorryNo;
				}

				System.out.println("@@@@@@[distord_iss]::lrDate["+lrDate+"]lrNo["+lrNo+"]lorryNo["+lorryNo+"]");
				System.out.println("@@@@@@[distord_iss]::lrDateUpd["+lrDateUpd+"]lrNoUpd["+lrNoUpd+"]lorryNoUpd["+lorryNoUpd+"]");

				sql1 = " update distord_iss set frt_amt = ?, lr_no 	= ?	, " +
						" lr_date = ?, lorry_no = ?  " +
						" where  tran_id = ? ";
				pstmt1 = conn.prepareStatement(sql1);
				pstmt1.setDouble(1, calcFrt);
				pstmt1.setString(2, lrNoUpd);
				pstmt1.setTimestamp(3, lrDateUpd);
				pstmt1.setString(4, lorryNoUpd);
				pstmt1.setString(5, distOrdIssId);
				updCnt = pstmt1.executeUpdate();
				pstmt1.close();
				pstmt1 = null;

			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			////

			// for cons_issue 
			sql = " select cons_issue,gross_weight,lr_date, lr_no, lorry_no from   CONSUME_ISS " +
					"  where  cons_issue in ( select ref_id from ship_docs where shipment_id = ? ) and confirmed = 'Y' ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				conOrdIssId = rs.getString("cons_issue");
				grossWeight = rs.getDouble("gross_weight");
				lrDate = rs.getTimestamp("lr_date");
				lrNo = rs.getString("lr_no");
				lorryNo = rs.getString("lorry_no");


				System.out.println("@@@@conOrdIssId["+conOrdIssId+"]::grossWeight["+grossWeight+"]lrDate["+lrDate+"]");

				System.out.println("@@@@totGwt["+totGwt+"]::totalFreight["+totalFreight+"]lrNo["+lrNo+"]");
				if( totGwt != 0 )  // lc_tot_gwt <> 0 then
				{
					calcFrt = Math.round((( totalFreight * grossWeight) / totGwt));
				}//end if

				System.out.println("calcFrt["+calcFrt+"]");

				totFrt = totFrt + calcFrt;
				System.out.println("totFrt["+totFrt+"]");
				if( totFrt > totalFreight ) // lc_tot_frt > lc_frt_amt then
				{
					calcFrt = totalFreight - ( totFrt - calcFrt ) ;
				}

				System.out.println("calcFrt["+calcFrt+"]totalFreight["+totalFreight+"]totFrt["+totFrt+"]");

				if( lrDate != null )
				{
					lrDateUpd = lrDate;
				}

				if( lrNo != null )
				{
					lrNoUpd = lrNo;
				}

				if( lorryNo != null )
				{
					lorryNoUpd = lorryNo;
				}

				System.out.println("@@@@@@[distord_iss]::lrDate["+lrDate+"]lrNo["+lrNo+"]lorryNo["+lorryNo+"]");
				System.out.println("@@@@@@[distord_iss]::lrDateUpd["+lrDateUpd+"]lrNoUpd["+lrNoUpd+"]lorryNoUpd["+lorryNoUpd+"]");

				sql1 = " update CONSUME_ISS set FREIGHT_AMT = ?, lr_no 	= ?	, " +
						" lr_date = ?, lorry_no = ?  " +
						"  where  cons_issue = ?  ";
				pstmt1 = conn.prepareStatement(sql1);
				pstmt1.setDouble(1, calcFrt);
				pstmt1.setString(2, lrNoUpd);
				pstmt1.setTimestamp(3, lrDateUpd);
				pstmt1.setString(4, lorryNoUpd);
				pstmt1.setString(5, conOrdIssId);
				updCnt = pstmt1.executeUpdate();
				pstmt1.close();
				pstmt1 = null;

			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			// END TEST


			/*

			//calculating the freight amt proportionately
			System.out.println("@@@@totGwt["+totGwt+"]::totalFreight["+totalFreight+"]lrNo["+lrNo+"]");
			if( totGwt != 0 )  // lc_tot_gwt <> 0 then
			{
				calcFrt = Math.round((( totalFreight * grossWeight) / totGwt));
			}//end if

			System.out.println("calcFrt["+calcFrt+"]");
			//this is to check whether total calculated freight doesnot exceed 
			//the total freight during rounding the figure

			totFrt = totFrt + calcFrt;
			System.out.println("totFrt["+totFrt+"]");
			if( totFrt > totalFreight ) // lc_tot_frt > lc_frt_amt then
			{   //  lc_calc_frt = lc_frt_amt - (lc_tot_frt - lc_calc_frt)
				calcFrt = totalFreight - ( totFrt - calcFrt ) ;
			}

			System.out.println("calcFrt["+calcFrt+"]totalFreight["+totalFreight+"]totFrt["+totFrt+"]");

			if( lrDate != null )
			{
				lrDateUpd = lrDate;
			}

			if( lrNo != null )
			{
				lrNoUpd = lrNo;
			}

			if( lorryNo != null )
			{
				lorryNoUpd = lorryNo;
			}

			System.out.println("@@@@@@[distord_iss]::lrDate["+lrDate+"]lrNo["+lrNo+"]lorryNo["+lorryNo+"]");
			System.out.println("@@@@@@[distord_iss]::lrDateUpd["+lrDateUpd+"]lrNoUpd["+lrNoUpd+"]lorryNoUpd["+lorryNoUpd+"]");

			sql = " update distord_iss set frt_amt = ?, lr_no 	= ?	, " +
					" lr_date = ?, lorry_no = ?  " +
					" where  tran_id = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setDouble(1, calcFrt);
			pstmt.setString(2, lrNoUpd);
			pstmt.setTimestamp(3, lrDateUpd);
			pstmt.setString(4, lorryNoUpd);
			pstmt.setString(5, distOrdIssId);
			updCnt = pstmt.executeUpdate();
			pstmt.close();
			pstmt = null;
			 */
			/// test

			// everything's OK, update shipment.
			if( errCode == null || errCode.trim().length() == 0 )
			{	

				sql = " update shipment set confirmed = 'Y', conf_date 	 = ? where  shipment_id = ? ";

				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1, sysDate);
				pstmt.setString(2, tranId);
				updCnt = pstmt.executeUpdate();
				pstmt.close();
				pstmt = null;

			}

		}  // end try
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			throw new ITMException(e);
		}

		return errCode;
			}


	/*public String actionHandler(String tranId) throws RemoteException, ITMException
	{
		String retString = "";

		try
		{
			//retString = checkNull(split(tranId, xtraParams, forcedFlag,conn,subSQL));
			retString = checkNull(split(tranId));
			System.out.println("@@@@@@retString is :"+retString);

		}
		catch(Exception e)
		{

			e.printStackTrace();
			System.out.println("Exception ::"+e.getMessage());

		}


		return retString;
	}*/



	//public String split(String tranId,String xtraParams,String forcedFlag, Connection conn,String subSQL) throws RemoteException, ITMException
	public String generateTrackInfo(String tranId,String xtraParams,Connection conn) throws RemoteException, ITMException
	{

		String retString = "";
		String errString="";
		StringBuffer xmlBuff = null;
		String xmlString = null;
		//Connection conn = null;
		String sql="";
		PreparedStatement pstmt = null;
		ResultSet rs = null, rs1 = null;
		String lineNoStr = "",frtTerm = "";

		String refId="",custCodeDlv="",dlvAdd1="",dlvAdd2="",dlvCity="",dlvPin="",dlvCountry="",siteDescr="",siteAdd1="",siteAdd2="",siteCity="",sitePin="",noArt="";
		String tranCode="",grossWt="",tarewt="",netwt="",unitShip="",bolNo="",scac="",planStartDt="",planEndDt="",shipMtDt="",expDt="";
		String despatchId="",invoiceId="",itemCode="",lotNo="",lotSl="",quantity="",quantityOrd="",convQtyStduom="",unitStd="";
		String unit="",qtyStduom="",packQty="",tareWt="",netWt="",sscc_18="",refSer="",locCode="",itmDescr="",itmCodeUPC ="",itmCodeNDC="",custProd="",purcDate="",drugLicNo="";
		Date planStart=null;
		Date planEnd=null;
		Date shipmentDate=null;
		Date prodDate=null;
		Date expDate=null;
		String siteCode="";
		String descr="";
		int lineNo=0;
		int cnt=0;
		HashMap splitCodeWiseMap =  new HashMap(), tempMapHdr = null;
		HashMap<String,String> tempMap = new HashMap<String,String>();
		ArrayList tempList = null;
		ConnDriver connDriver = new ConnDriver();
		ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();

		try
		{
			System.out.println("mahendra testing ------inside split method");
			System.out.println("shipment id@@@@@ ::::"+tranId);
			Date currentDate = new Date();
			xmlBuff = null;	
			xmlBuff = new StringBuffer();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
			//Changes and Commented By Bhushan on 13-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 13-06-2016 :END



			sql = "select shipment_date from shipment where shipment_id=?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if (rs.next()) 
			{
				shipmentDate = rs.getDate("shipment_date");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("shipmentDate:"+shipmentDate);


			sql = "select SITE_CODE,CUST_CODE__DLV,DLV_ADD1,DLV_ADD2,DLV_CITY,DLV_PIN,COUNT_CODE__DLV,TRAN_CODE  , ";
			sql = sql + "GROSS_WEIGHT,TARE_WEIGHT ,NETT_WEIGHT,UNIT__SHIP,BOL_NO,SCAC,NO_ART,DESP_ID from despatch where shipment_id=? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if (rs.next()) 
			{
				siteCode = rs.getString("SITE_CODE");
				custCodeDlv = rs.getString("CUST_CODE__DLV");
				dlvAdd1 = rs.getString("DLV_ADD1");
				dlvAdd2 = rs.getString("DLV_ADD2");
				dlvCity = rs.getString("DLV_CITY");
				dlvPin = rs.getString("DLV_PIN");
				dlvCountry = rs.getString("COUNT_CODE__DLV");
				tranCode = rs.getString("TRAN_CODE");
				grossWt = rs.getString("GROSS_WEIGHT");
				tarewt = rs.getString("TARE_WEIGHT");
				netwt = rs.getString("NETT_WEIGHT");
				unitShip = rs.getString("UNIT__SHIP");
				bolNo = rs.getString("BOL_NO");
				scac = rs.getString("SCAC");
				noArt= rs.getString("NO_ART");
				despatchId=rs.getString("DESP_ID");
			}
			else
			{
				System.out.println("Shipment Id not found in despatch table");
				errString = itmDBAccessLocal.getErrorString("","VTSHIDEXST","","",conn);
				return errString;
			}

			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("siteCode @@@"+siteCode);
			System.out.println("custCodeDlv @@@"+custCodeDlv);
			System.out.println("tranCode @@@"+tranCode);
			System.out.println("noArt @@@"+noArt);
			System.out.println("scac @@@"+scac);




			sql = "select descr,add1,add2,city,pin from site where site_code=?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, siteCode);
			rs = pstmt.executeQuery();
			if (rs.next()) 
			{
				siteDescr = rs.getString("descr");
				siteAdd1 = rs.getString("add1");
				siteAdd2 = rs.getString("add2");
				siteCity = rs.getString("city");
				sitePin = rs.getString("pin");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("siteDescr:"+siteDescr);
			System.out.println("siteAdd1:"+siteAdd1);
			System.out.println("siteAdd2:"+siteAdd2);
			System.out.println("siteCity:"+siteCity);
			System.out.println("sitePin:"+sitePin);



			sql = "select DRUG_LIC_NO from customer where cust_code=?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, custCodeDlv);
			rs = pstmt.executeQuery();
			if (rs.next()) 
			{
				drugLicNo = rs.getString("DRUG_LIC_NO");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("drugLicNo:"+drugLicNo);





			System.out.println("siteCode!!!!!  :"+siteCode);
			System.out.println("siteDescr  :"+siteDescr);
			System.out.println("siteAdd1  :"+siteAdd1);
			System.out.println("siteAdd2  :"+siteAdd2);
			System.out.println("siteCity  :"+siteCity);
			System.out.println("sitePin  :"+sitePin);
			System.out.println("custCodeDlv  :"+custCodeDlv);
			System.out.println("drugLicNo  :"+drugLicNo);
			System.out.println("dlvAdd1  :"+dlvAdd1);
			System.out.println("dlvAdd2  :"+dlvAdd2);
			System.out.println("dlvCity  :"+dlvCity);
			System.out.println("dlvPin  :"+dlvPin);
			System.out.println("dlvCountry  :"+dlvCountry);
			System.out.println("tranCode  :"+tranCode);
			System.out.println("grossWt  :"+grossWt);
			System.out.println("tarewt  :"+tarewt);
			System.out.println("netwt  :"+netwt);
			System.out.println("unitShip  :"+unitShip);
			System.out.println("bolNo  :"+bolNo);
			System.out.println("scac  :"+scac);
			System.out.println("noArt  :"+noArt);


			sql = "select plan_end,plan_start  from shipment_route where shipment_id=?  ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if (rs.next()) 
			{
				planStart = rs.getDate("plan_start");
				planEnd = rs.getDate("plan_end");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("planStart:"+planStart);
			System.out.println("planEnd:"+planEnd);




			/*get records for details !!!*/

			sql = "select ref_id,ref_ser from ship_docs where shipment_id=?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if (rs.next()) 
			{
				//despatchId = rs.getString(1);
				refId= rs.getString(1);
				refSer=rs.getString(2);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("refId:"+refId);
			System.out.println("refSer:"+refSer);

			if(despatchId == null)
			{
				System.out.println("despatchId found null in ship_docs");
				errString = itmDBAccessLocal.getErrorString("","VTDSIDEXST","","",conn);
				return errString;

			}


			sql = "select INVOICE_ID ,ITEM_CODE ,LOT_NO ,LOT_SL,QUANTITY ,QUANTITY__ORD,LOC_CODE,CONV__QTY_STDUOM , ";
			sql = sql + "UNIT__STD,UNIT ,QUANTITY__STDUOM ,PACK_QTY,GROSS_WEIGHT,TARE_WEIGHT,NETT_WEIGHT,SSCC_18  ";
			sql = sql + " from despatchdet where desp_id=?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, despatchId);
			rs = pstmt.executeQuery();
			while (rs.next()) 
			{
				cnt++;
				invoiceId = rs.getString("INVOICE_ID");
				itemCode = rs.getString("ITEM_CODE");
				lotNo = rs.getString("LOT_NO");
				lotSl = rs.getString("LOT_SL");
				quantity = rs.getString("QUANTITY");
				quantityOrd = rs.getString("QUANTITY__ORD");
				convQtyStduom = rs.getString("CONV__QTY_STDUOM");
				unitStd = rs.getString("UNIT__STD");
				unit = rs.getString("UNIT");
				qtyStduom = rs.getString("QUANTITY__STDUOM");
				locCode= rs.getString("LOC_CODE");
				packQty = rs.getString("PACK_QTY");
				grossWt = rs.getString("GROSS_WEIGHT");
				tareWt = rs.getString("TARE_WEIGHT");
				netWt = rs.getString("NETT_WEIGHT");
				sscc_18 = rs.getString("SSCC_18");


				invoiceId = invoiceId==null?"":invoiceId.trim();
				itemCode = itemCode==null?"":itemCode.trim();
				lotNo = lotNo==null?"":lotNo.trim();
				lotSl = lotSl==null?"":lotSl.trim();
				quantity = quantity==null?"":quantity.trim();
				quantityOrd = quantityOrd==null?"":quantityOrd.trim();
				convQtyStduom = convQtyStduom==null?"":convQtyStduom.trim();
				unitStd = unitStd==null?"":unitStd.trim();
				unit = unit==null?"":unit.trim();
				qtyStduom = qtyStduom==null?"":qtyStduom.trim();
				locCode = locCode==null?"":locCode.trim();
				sscc_18 = sscc_18==null?"":sscc_18.trim();

				System.out.println("invoiceId @@"+cnt+" "+invoiceId);
				System.out.println("itemCode @@"+cnt+" "+itemCode);
				System.out.println("lotNo @@"+cnt+" "+lotNo);
				System.out.println("lotSl @@"+cnt+" "+lotSl);
				System.out.println("quantity @@"+cnt+" "+quantity);
				System.out.println("quantityOrd @@"+cnt+" "+quantityOrd);
				System.out.println("convQtyStduom @@"+cnt+" "+convQtyStduom);
				System.out.println("unitStd @@"+cnt+" "+unitStd);
				System.out.println("unit @@"+cnt+" "+unit);

				tempMap.put("invoice_id"+cnt, invoiceId);
				tempMap.put("item_code"+cnt, itemCode);
				tempMap.put("lot_no"+cnt, lotNo);
				tempMap.put("lot_sl"+cnt, lotSl);
				tempMap.put("quantity"+cnt, quantity);
				tempMap.put("quantity_ord"+cnt, quantityOrd);
				tempMap.put("conv_qty_std"+cnt, convQtyStduom);
				tempMap.put("unit_std"+cnt, unitStd);
				tempMap.put("unit"+cnt, unit);
				tempMap.put("qty_stduom"+cnt, qtyStduom);
				tempMap.put("loc_code"+cnt, locCode);
				tempMap.put("pack_qty"+cnt, packQty);
				tempMap.put("gross_wt"+cnt, grossWt);
				tempMap.put("tare_wt"+cnt, tareWt);
				tempMap.put("net_wt"+cnt, netWt);

			}

			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;


			System.out.println("value of cnt :"+cnt);


			sql = "select CUST_PORD,PORD_DATE from sorder where sale_order = (select sord_no from despatch where desp_id=?)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, despatchId);
			rs = pstmt.executeQuery();
			if (rs.next()) 
			{
				custProd = rs.getString("CUST_PORD");
				prodDate = rs.getDate("PORD_DATE");

			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("custProd: "+custProd);
			System.out.println("prodDate: "+prodDate);


			siteCode = siteCode==null?"":siteCode.trim();
			siteDescr = siteDescr==null?"":siteDescr.trim();
			siteAdd1 = siteAdd1==null?"":siteAdd1.trim();
			siteAdd2 = siteAdd2==null?"":siteAdd2.trim();
			siteCity = siteCity==null?"":siteCity.trim();
			sitePin = sitePin==null?"":sitePin.trim();
			custCodeDlv = custCodeDlv==null?"":custCodeDlv.trim();
			drugLicNo = drugLicNo==null?"":drugLicNo.trim();
			dlvAdd1 = dlvAdd1==null?"":dlvAdd1.trim();
			dlvAdd2 = dlvAdd2==null?"":dlvAdd2.trim();
			dlvCity = dlvCity==null?"":dlvCity.trim();
			dlvPin = dlvPin==null?"":dlvPin.trim();
			dlvCountry = dlvCountry==null?"":dlvCountry.trim();
			tranCode = tranCode==null?"":tranCode.trim();
			grossWt = grossWt==null?"":grossWt.trim();
			tarewt = tarewt==null?"":tarewt.trim();
			netwt = netwt==null?"":netwt.trim();
			unitShip = unitShip==null?"":unitShip.trim();
			bolNo = bolNo==null?"":bolNo.trim();
			scac = scac==null?"":scac.trim();
			noArt = noArt==null?"":noArt.trim();
			planStartDt=planStart==null?"":sdf.format(planStart);
			planEndDt=planEnd==null?"":sdf.format(planEnd);
			shipMtDt=shipmentDate==null?"":sdf.format(shipmentDate);
			custProd = custProd==null?"":custProd.trim();
			purcDate= prodDate==null?"":sdf.format(prodDate);






			//create xml for track information
			xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
			xmlBuff.append("<DocumentRoot>");
			xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
			xmlBuff.append("<group0>");
			xmlBuff.append("<description>").append("Group0 description").append("</description>");
			xmlBuff.append("<Header0>");
			xmlBuff.append("<objName><![CDATA[").append("tnt_tran_info").append("]]></objName>");         
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

			System.out.println("details 1 start!!!!!");
			/*
			 * deatails 1 start
			 * */

			xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"tnt_tran_info\" objContext=\"1\">");  
			xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
			xmlBuff.append("<tran_id/>");
			xmlBuff.append("<tran_date><![CDATA["+ sdf.format(currentDate).toString() +"]]></tran_date>");
			xmlBuff.append("<site_code><![CDATA["+ siteCode.trim()  +"]]></site_code>");
			xmlBuff.append("<site_descr><![CDATA["+ siteDescr +"]]></site_descr>");
			xmlBuff.append("<site_add1><![CDATA["+ siteAdd1 +"]]></site_add1>");
			xmlBuff.append("<site_add2><![CDATA["+ siteAdd2  +"]]></site_add2>");
			xmlBuff.append("<site_city><![CDATA["+ siteCity +"]]></site_city>");
			xmlBuff.append("<site_pin><![CDATA["+ sitePin +"]]></site_pin>");
			xmlBuff.append("<count_code__site><![CDATA[]]></count_code__site>");
			xmlBuff.append("<shipment_id><![CDATA["+ tranId +"]]></shipment_id>");
			xmlBuff.append("<shipment_date><![CDATA["+ shipMtDt +"]]></shipment_date>");
			xmlBuff.append("<no_art><![CDATA["+ noArt +"]]></no_art>");
			xmlBuff.append("<gross_weight><![CDATA["+ grossWt +"]]></gross_weight>");
			xmlBuff.append("<net_weight><![CDATA["+ netwt +"]]></net_weight>");
			xmlBuff.append("<tare_weight><![CDATA["+ tarewt +"]]></tare_weight>");
			xmlBuff.append("<unit__ship><![CDATA["+ unitShip +"]]></unit__ship>");
			xmlBuff.append("<tran_code><![CDATA["+ tranCode +"]]></tran_code>");
			xmlBuff.append("<bol_no><![CDATA["+ bolNo +"]]></bol_no>");
			xmlBuff.append("<cust_code__dlv><![CDATA["+ custCodeDlv +"]]></cust_code__dlv>");
			xmlBuff.append("<scac><![CDATA[]]></scac>");
			xmlBuff.append("<drug_lic_no><![CDATA["+ drugLicNo +"]]></drug_lic_no>");
			xmlBuff.append("<dlv_add1><![CDATA["+ dlvAdd1 +"]]></dlv_add1>");
			xmlBuff.append("<dlv_add2><![CDATA["+ dlvAdd2 +"]]></dlv_add2>");
			xmlBuff.append("<dlv_city><![CDATA["+ dlvCity +"]]></dlv_city>");
			xmlBuff.append("<dlv_pin><![CDATA["+ dlvPin +"]]></dlv_pin>");
			xmlBuff.append("<count_code__dlv><![CDATA["+ dlvCountry +"]]></count_code__dlv>");
			xmlBuff.append("<plan_start><![CDATA["+ planStartDt +"]]></plan_start>");
			xmlBuff.append("<plan_end><![CDATA["+ planEndDt +"]]></plan_end>");
			//	xmlBuff.append("<remarks><![CDATA["+ tranId +"]]></remarks>");
			xmlBuff.append("</Detail1>");
			lineNo = 0;



			/*
			 * deatils1 end
			 * 
			 * */


			System.out.println("end of details 1");


			for(int itemCtr = 1; itemCtr <= cnt; itemCtr++)
			{
				lineNo++;
				System.out.println("lineNo is "+lineNo);
				//	tempMap = (HashMap)tempList.get(itemCtr);

				/*
				 * start of Details 2
				 * */
				System.out.println("start of details 2");

				xmlBuff.append("<Detail2 dbID='' domID=\"1\" objName=\"tnt_tran_info\" objContext=\"2\">"); 
				xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" updateFlag=\"A\" status=\"N\" />");
				xmlBuff.append("<tran_id/>");
				lineNoStr = "   " + lineNo;
				lineNoStr = lineNoStr.substring( lineNoStr.length()-3 );
				System.out.println("lineNoStr "+lineNoStr);
				//  xmlBuff.append("<line_no>" + new Integer(lineNo).toString().length() + "</line_no>");
				xmlBuff.append("<line_no>" + lineNoStr + "</line_no>");


				xmlBuff.append("<ref_ser><![CDATA["+ refSer +"]]></ref_ser>");
				xmlBuff.append("<ref_id><![CDATA["+ refId+"]]></ref_id>");
				System.out.println("val of invoice_id "+(String)tempMap.get("invoice_id"+itemCtr) );
				System.out.println("val of item_code "+(String)tempMap.get("item_code"+itemCtr) );
				xmlBuff.append("<invoice_id><![CDATA["+ (String)tempMap.get("invoice_id"+itemCtr) +"]]></invoice_id>");
				xmlBuff.append("<item_code><![CDATA["+ (String)tempMap.get("item_code"+itemCtr) +"]]></item_code>");

				sql = "select DESCR,ITEM_CODE__UPC,ITEM_CODE__NDC from item where item_code=?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tempMap.get("item_code"+itemCtr));
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					itmDescr = rs.getString("DESCR");
					itmCodeUPC = rs.getString("ITEM_CODE__UPC");
					itmCodeNDC = rs.getString("ITEM_CODE__NDC");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				System.out.println("itmDescr: "+itmDescr);
				System.out.println("itmCodeUPC: "+itmCodeUPC);
				System.out.println("itmCodeNDC: "+itmCodeNDC);

				itmDescr = itmDescr==null?"":itmDescr.trim();
				itmCodeUPC = itmCodeUPC==null?"":itmCodeUPC.trim();
				itmCodeNDC = itmCodeNDC==null?"":itmCodeNDC.trim();




				sql = "select exp_date from ITEM_LOT_INFO where item_code=? and lot_no=? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tempMap.get("item_code"+itemCtr));
				pstmt.setString(2, tempMap.get("lot_no"+itemCtr));
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					expDate = rs.getDate("exp_date");

				}
				expDt=expDate==null?"":sdf.format(expDate);
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				System.out.println("itmDescr: "+itmDescr);
				System.out.println("itmCodeUPC: "+itmCodeUPC);
				System.out.println("itmCodeNDC: "+itmCodeNDC);

				itmDescr = itmDescr==null?"":itmDescr.trim();
				itmCodeUPC = itmCodeUPC==null?"":itmCodeUPC.trim();
				itmCodeNDC = itmCodeNDC==null?"":itmCodeNDC.trim();



				xmlBuff.append("<item_descr><![CDATA["+ itmDescr +"]]></item_descr>");
				xmlBuff.append("<item_code__ndc><![CDATA["+ itmCodeNDC +"]]></item_code__ndc>");
				xmlBuff.append("<item_code__upc><![CDATA["+ itmCodeUPC +"]]></item_code__upc>");
				xmlBuff.append("<item_strength><![CDATA[]]></item_strength>");
				xmlBuff.append("<lot_no><![CDATA["+ (String)tempMap.get("lot_no"+itemCtr) +"]]></lot_no>");
				xmlBuff.append("<exp_date><![CDATA["+ expDt +"]]></exp_date>");
				xmlBuff.append("<lot_sl><![CDATA["+ (String)tempMap.get("lot_sl"+itemCtr) +"]]></lot_sl>");
				xmlBuff.append("<quantity__ord><![CDATA["+ (String)tempMap.get("quantity_ord"+itemCtr) +"]]></quantity__ord>");
				xmlBuff.append("<quantity><![CDATA["+ (String)tempMap.get("quantity"+itemCtr) +"]]></quantity>");
				xmlBuff.append("<loc_code><![CDATA["+ (String)tempMap.get("loc_code"+itemCtr) +"]]></loc_code>");
				xmlBuff.append("<conv__qty_stduom><![CDATA["+ (String)tempMap.get("conv_qty_std"+itemCtr) +"]]></conv__qty_stduom>");
				xmlBuff.append("<unit__std><![CDATA["+ (String)tempMap.get("unit_std"+itemCtr) +"]]></unit__std>");
				xmlBuff.append("<unit><![CDATA["+ (String)tempMap.get("unit"+itemCtr) +"]]></unit>");
				xmlBuff.append("<quantity__stduom><![CDATA["+ (String)tempMap.get("qty_stduom"+itemCtr) +"]]></quantity__stduom>");
				xmlBuff.append("<pack_qty><![CDATA["+ (String)tempMap.get("pack_qty"+itemCtr) +"]]></pack_qty>");
				xmlBuff.append("<gross_weight><![CDATA["+ (String)tempMap.get("gross_wt"+itemCtr) +"]]></gross_weight>");
				xmlBuff.append("<net_weight><![CDATA["+ (String)tempMap.get("net_wt"+itemCtr) +"]]></net_weight>");
				xmlBuff.append("<tare_weight><![CDATA["+ (String)tempMap.get("tare_wt"+itemCtr) +"]]></tare_weight>");
				xmlBuff.append("<cust_pord><![CDATA["+ custProd +"]]></cust_pord>");
				xmlBuff.append("<pord_date><![CDATA["+  purcDate  +"]]></pord_date>");
				xmlBuff.append("<sscc_18><![CDATA["+ sscc_18 +"]]></sscc_18>");




				xmlBuff.append("</Detail2>");	

			}//end of for loop	

			/*
			 * end of details 2
			 * 
			 * */

			System.out.println("end of details 2");

			xmlBuff.append("</Header0>");
			xmlBuff.append("</group0>");
			xmlBuff.append("</DocumentRoot>");
			xmlString = xmlBuff.toString();
			System.out.println("@@@@@2: retString:"+retString);
			System.out.println("...............just before savdata()");
			//siteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");

			//retString = saveData(siteCode,xmlString,conn);
			//Changes and Commented By Ajay on 08-01-2018:START
            String userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode"); 
            System.out.println("--login code--"+userId);
			//retString = saveData(siteCode,xmlString,conn);
            retString = saveData(siteCode,xmlString,userId,conn);
          //Changes and Commented By Ajay on 08-01-2018:END
			System.out.println("#########2: retString:"+retString);



			if (retString.indexOf("Success") > -1)
			{
				System.out.println("Successfully!!!!!!");
				conn.commit();
				conn.close();
			}
			else
			{
				System.out.println("failed!!!!");
				conn.rollback();

				errString = itmDBAccessLocal.getErrorString("","VTGTRTIFLD","","",conn);
				return errString;

			}


		}
		catch(Exception e)
		{

			e.printStackTrace();
			System.out.println("Exception ::"+e.getMessage());

		}

		return retString;
	}



	private String saveData(String siteCode,String xmlString,String userId, Connection conn) throws ITMException
	{
		System.out.println("mahendra testing !!!!saving data...........");
		System.out.println("siteCode  "+siteCode);
		System.out.println("xmlString  "+xmlString);
		InitialContext ctx = null;
		String retString = null;
		MasterStatefulLocal masterStateful = null; // for ejb3
		try
		{
			AppConnectParm appConnect = new AppConnectParm();
			ctx = new InitialContext(appConnect.getProperty());
			masterStateful = (MasterStatefulLocal)ctx.lookup("ibase/MasterStatefulEJB/local");
			System.out.println("-----------masterStateful------- " + masterStateful);
			String [] authencate = new String[2];
			authencate[0] = userId;
			authencate[1] = "";
			System.out.println("xmlString to masterstateful [" + xmlString + "]");
			retString = masterStateful.processRequest(authencate, siteCode, true, xmlString,true,conn);
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






	private String checkNull( String input )	
	{
		if ( input == null )
		{
			input = "";
		}
		return input;
	}



}
