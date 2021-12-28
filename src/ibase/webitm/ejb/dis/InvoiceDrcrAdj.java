package ibase.webitm.ejb.dis;

import ibase.planner.utility.ITMException;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.utility.EMail;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.fin.FinCommon;
//import ibase.utility.MailInfo;
//import ibase.webitm.ejb.ITMDBAccessEJB;
//import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.sys.UtilMethods;
import ibase.webitm.utility.TransIDGenerator;
//import ibase.webitm.utility.GenericUtility;

import java.io.File;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class InvoiceDrcrAdj 
{
	public String invoiceDrcrAdj(String siteCode, String invoiceId, String custCode, String itemSer, double netAmt, boolean adjDrCr, boolean adjAdv, String advAdj, boolean adjNewProdInv, String frtDrnArr[], Connection conn)  throws RemoteException,ITMException
	{
		///////////////////////////////////////////
		//14/03/01 manoharan update of receivables commented
		//only insert of receivables_adj records will be taken place in
		//this function. The related updates will be done when the invoice
		//is confirmed
		//
		//what if one invoice is not confirmed and another invoice is
		//generated for the same customer/site ?????????????????

		String errString = "", tranID = "", tranSer = "", refNo = "", adjTranSer = "", adjRefNo = "",mOrderNo = "",finScheme = "";
		String nextID = "", status = "", refType = "",ignoreDrDays = "",ordTypeNewPrd = "", sqlStr = "", tranSeries = "", asjCustAdv = "";
		double netAmount = 0, mTotAmt = 0, mAdjAmt = 0, mDiffAmt = 0, mSetAmt = 0;//, mAmt = 0
		double mChkAmt = 0, mDrAmt = 0;//, mAmtAdj
		int mctr = 1, mChkRec = 0,llIgnoreDrDays = 0, llCrDays = 0;
		Timestamp mRefDate = null;
		String rcpNo = "",siteCodeRcp = "",siteCodeDlv ="",subsNo = "",drnNotIn = "";
		String tempStr;
		//09-01-02 Arif
		String keystr = "",adjustNewProduct = "";//added radhakrishnan on 16-04-04
		//09-01-02 End Arif
		//03-08-2005 manoharan moved from below
		String tranTypeFinChrgDrn = "", trnTypArray[], ordTypArray[], sType = "", tempStrType = "";
		FinCommon finCommon = null;
		DistCommon distCommon = null;
		E12GenericUtility genericUtility = null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		PreparedStatement pstmt = null,pstmt1 = null, pstmtInsert = null, pstmtUpd = null;
		ResultSet rs = null, rs1 = null;
		//String ls_transer, ls_refno, ls_tranid
		double lcAdj = 0;
		String xmlValues = null;
		String mTranId="", mTranSer="", mRefNo="";
		String  sqlStr1 = "";
		String 	invAutoAdjCrtp=""; //variable declared  by nandkumar gadkari on 08/04/19
		try
		{

			finCommon = new FinCommon();
			distCommon = new DistCommon();
			genericUtility = new E12GenericUtility();
			itmDBAccessEJB = new ITMDBAccessEJB();

			//24/01/14 manoharan FI3JSUN007 adjust freight dr note alsof

			if (frtDrnArr.length == 1)
			{
				if ( frtDrnArr[0] == null || frtDrnArr[0].trim().length() == 0)
				{
					frtDrnArr[0] = "   ";
				}
			}

			drnNotIn ="";
			for(int i = 0; i < frtDrnArr.length; i++)
			{
				if(  frtDrnArr[i] != null && frtDrnArr[i].trim().length() > 0)
				{
					drnNotIn =drnNotIn+"'"+frtDrnArr[i]+"',";
				}
			}
			if( drnNotIn != null && drnNotIn.trim().length() > 0)
			{
				drnNotIn =drnNotIn.substring(0,drnNotIn.length()-1);
			}System.out.println("12-Mar-16 manoharan drnNotIn after ["+drnNotIn + "]");
			adjustNewProduct = distCommon.getDisparams("999999","ADJUST_NEWPRODUCT",conn);
			if (!"NULLFOUND".equals(adjustNewProduct) || "Y".equals(adjustNewProduct) )
			{
				ordTypeNewPrd = distCommon.getDisparams("999999","ORD_TYPE_NEWPRD",conn);
			}
			else
			{
				ordTypeNewPrd = " ";
			}
			ordTypeNewPrd = ordTypeNewPrd.trim();
			tranTypeFinChrgDrn = distCommon.getDisparams("999999","IGNORE_TYPE_FINCHRG",conn);

			if ("NULLFOUND".equals(tranTypeFinChrgDrn))
			{
				tranTypeFinChrgDrn = " ";
			}
			//end 03-08-2005 manoharan moved from below

			adjTranSer = "S-INV";

			//in below Sql cr_days added by Jasmina 29/08/08-DI89SUN015
			sqlStr ="select invoice_id, tran_date, inv_type,sale_order, cr_days from invoice "
				+ "where invoice_id = ? " ;
			pstmt = conn.prepareStatement(sqlStr);
			pstmt.setString(1,invoiceId);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				adjRefNo	= rs.getString("invoice_id");
				mRefDate	= rs.getTimestamp("tran_date");
				refType		= rs.getString("inv_type");
				mOrderNo	= rs.getString("sale_order");
				llCrDays	= rs.getInt("cr_days");

			}
			else
			{
				errString = itmDBAccessEJB.getErrorString("","VTNOINV","","",conn);
				return errString	;
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;


			//commented by Amit 04/05/05 as FN type checking is done thru disparm further.
			//elseif (trim(refType) = 'FN' ) 
			//return errString
			//03-08-2005 manoharan new product order type taken from
			//elseif (pos(ordTypeNewPrd,trim(refType)) > 0 )
			System.out.println("12-Mar-16 manoharan ordTypeNewPrd ["+ordTypeNewPrd + "]");
			if (ordTypeNewPrd.trim().length() > 0 )
			{
				String arrStr[] = ordTypeNewPrd.split(",");
				for(int i = 0; i<arrStr.length; i++)
				{
					if (refType.trim().equals(arrStr[i].trim()))
					{
						return "";
					}
				}
				//end 03-08-2005 manoharan new product order type taken from
			}
			netAmount = netAmt;
			mDrAmt = netAmt;
			System.out.println("@@@@@@@@162 mDrAmt["+mDrAmt+"]");
			//09-01-02 Arif
			sqlStr ="select key_String from transetup "
				+ "where upper(tran_window) = 'W_REC_ADJ' " ;
			pstmt = conn.prepareStatement(sqlStr);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				keystr	= rs.getString("key_string");
			}
			else
			{
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				sqlStr ="select key_String from transetup "
					+ "where upper(tran_window) = 'GENERAL' " ;
				pstmt = conn.prepareStatement(sqlStr);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					keystr	= rs.getString("key_string");
				}
				else
				{
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					errString = itmDBAccessEJB.getErrorString("","VTNOINV","","",conn);
					return errString	;
				}
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			//09-01-02 End Arif
			//formulation new products hard coded and 15 days
			//Sanjeev - 14/10/03 - Removed harcoding done by Arif (for 15 days and used finparm variable
			//in sql below.

			ignoreDrDays = finCommon.getFinparams("999999", "IGNORE_DR_DAYS",conn);

			//if (get_sqlcode() = 100 )
			if ("NULLFOUND".equals(ignoreDrDays) )
			{
				//    llIgnoreDrDays = 9999999//decreased on manoharanji's instruction as mssql is giving error of arithmatic overflow  ** kiran 29/06/05
				llIgnoreDrDays = 999999;
			}
			else if (ignoreDrDays == null )
			{
				//    llIgnoreDrDays = 9999999//decreased on manoharanji's instruction as mssql is giving error of arithmatic overflow  ** kiran 29/06/05
				llIgnoreDrDays = 999999;
			}
			else
			{
				//added by Jasmina 29/08/08-DI89SUN015
				if ("BEFORE_CR_DAYS".equals(ignoreDrDays.trim()) )
				{
					llIgnoreDrDays = llCrDays * -1;
				}
				else  //added end by Jasmina 29/08/08-DI89SUN015
				{
					llIgnoreDrDays = Integer.valueOf(ignoreDrDays);
				}
			}
			System.out.println("12-Mar-16 manoharan llIgnoreDrDays final ["+llIgnoreDrDays + "]");
			//03-08-2005 manoharan commented and taken above
			//
			////added by radhakrishnan on 16-04-04
			//
			//adjustNewProduct = gf_getenv_dis('999999','ADJUST_NEWPRODUCT')
			//adjustNewProduct = upper(trim(adjustNewProduct))
			//if (adjustNewProduct='NULLFOUND' or adjustNewProduct = 'Y' )
			//ordTypeNewPrd = gf_getenv_dis('999999','ORD_TYPE_NEWPRD')
			//}
			////end radhakrishnan on 16-04-04
			//End Sanjeev - 14/10/03
			//end 03-08-2005 manoharan commented and taken above
			//added by bhavin
			sqlStr ="select (case when fin_scheme is null then ' ' else fin_scheme end ) fin_scheme from sorder "
				+ "where sale_order = ? " ;
			pstmt = conn.prepareStatement(sqlStr);
			pstmt.setString(1,mOrderNo);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				finScheme	= rs.getString(1);

			}
			else
			{
				errString = itmDBAccessEJB.getErrorString("","VTNOSORD","","",conn);
				return errString	;
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			//ended by bhavin
			System.out.println("@@@@@@@@264 drnNotIn["+drnNotIn+"]");
			//24/01/14 Manoharan FI3JSUN007 adjust all the freight drn
			if (drnNotIn.trim().length() > 0  )
			{
				sqlStr1 = "insert into receivables_adj "
					+ "(tran_id, ref_ser, ref_no, tot_amt, adj_amt, net_amt,ref_ser_adj,ref_no_adj, tran_id__rcv) "
					+ " values (?, ?, ?, ?, ?, 0, ?, ? ,?) ";
				pstmtInsert  = conn.prepareStatement(sqlStr1);
				//old logic kept in this Condition
				sqlStr = "select tran_id, tran_ser, ref_no, tot_amt, adj_amt from receivables "
					+ " where  tran_ser = 'MDRCRD' and ref_no in ( "+drnNotIn+" ) "  
					+ " and tot_amt - adj_amt > 0 " 
					+ " and site_code = '" + siteCode + "' " 
					+ " and item_ser  = '" + itemSer + "' " 
					+ " and cust_code = '" + custCode + "' order by tran_id ";
				pstmt = conn.prepareStatement(sqlStr);
				//pstmt.setString(1,drnNotIn);
				rs = pstmt.executeQuery();
				while(rs.next())
				{
					tranID = rs.getString("tran_id");
					tranSer= rs.getString("tran_ser");
					refNo= rs.getString("ref_no");
					mTotAmt= rs.getDouble("tot_amt");
					mAdjAmt= rs.getDouble("adj_amt");
					System.out.println("@@@@@@@@289 mDrAmt["+mDrAmt+"]");
					mDiffAmt = mTotAmt - mAdjAmt;
					mDrAmt = mDrAmt + mDiffAmt;
					System.out.println("@@@@@@@@292 mDrAmt["+mDrAmt+"]");
					xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
					xmlValues = xmlValues + "<Header></Header>";
					xmlValues = xmlValues + "<Detail1>";
					xmlValues = xmlValues +	"<tran_id></tran_id>";
					xmlValues = xmlValues + "<site_code>" + siteCode + "</site_code>";	
					xmlValues = xmlValues +"</Detail1></Root>";
					System.out.println("xmlValues  :["+xmlValues+"]");
					TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", CommonConstants.DB_NAME);
					nextID = tg.generateTranSeqID("R-ADJ", "tran_id", keystr, conn);
					System.out.println("nextID ["+nextID + "]");
					// 09-01-02 End Arif
					pstmtInsert.setString(1,nextID);
					pstmtInsert.setString(2,tranSer);
					pstmtInsert.setString(3,refNo);
					pstmtInsert.setDouble(4,mDiffAmt);
					pstmtInsert.setDouble(5,mDiffAmt);
					pstmtInsert.setString(6,adjTranSer);
					pstmtInsert.setString(7,adjRefNo);
					pstmtInsert.setString(8,tranID);
					pstmtInsert.addBatch();
					pstmtInsert.clearParameters();

					mChkRec++;
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				pstmtInsert.executeBatch();
				pstmtInsert.clearParameters();
				//pstmtInsert.close();


				mTotAmt = 0;
				mAdjAmt = 0;
				mDiffAmt = 0;
			}

			//end 24/01/14 manoharan

			//04-09-2005 manoharan
			//reverted on 15-09-2005 as per sun requirement
			//if (adjDrCr = TRUE ) //Commented by jasmina-19/01/10-DI89SUN228
			System.out.println("@@@@@@@@335 adjDrCr["+adjDrCr+"]adjNewProdInv["+adjNewProdInv+"]adjustNewProduct["+adjustNewProduct+"]ordTypeNewPrd["+ordTypeNewPrd+"]");
		//commeted by nandkumar gadkari on 06/01/20 for duplicate block 
			/*	if (adjDrCr  || (adjNewProdInv  && ("Y".equals(adjustNewProduct) && ordTypeNewPrd != null && ordTypeNewPrd.trim().length() > 0) ) ) //Added by jasmina-19/01/10-DI89SUN228
			{
				//if (adjDrCr or adjustNewProduct = 'Y' )
				// 03-08-2005 manoharan dynamic cursor used to have multiple
				// order/transaction types specified in disparm for new product/ financial charges
				//declare c_recv cursor for
				//select tran_id, tran_ser, ref_no, tot_amt, adj_amt from receivables
				//    where (tran_ser = 'DRNRCP' or tran_ser = 'MDRCRD')
				//      and ( ((ref_type = 'FC' or ref_type = :ordTypeNewPrd )
				//                and ( (:mRefDate - ref_date ) >= :llIgnoreDrDays  )) OR (ref_type <> 'FC' and ref_type <> :ordTypeNewPrd) )
				//      and (tot_amt - adj_amt) > 0
				//      and site_code = :siteCode
				//      and item_ser  = :itemSer
				//      and cust_code = :custCode
				//      and (case when fin_scheme is null ) ' ' else fin_scheme end ) = :finScheme
				//order by tran_id;
				//open c_recv;
				//if (get_sqlcode() < 0 )
				//    errString = 'DS000' + String(sqlca.sqldbcode)
				//    return errString
				//}
				//

				String[] arrStr;
				System.out.println("@@@@@@@@360 adjDrCr["+adjDrCr+"]adjNewProdInv["+adjNewProdInv+"]");
				if (adjDrCr && adjNewProdInv ) //if (condition Added by jasmina-19/01/10-DI89SUN228
				{
					System.out.println("1@@@@@@@@ tranTypeFinChrgDrn["+tranTypeFinChrgDrn+"]");
					//old logic kept in this Condition
					sqlStr = "select tran_id, tran_ser, ref_no, tot_amt, adj_amt from receivables ";
					if (tranTypeFinChrgDrn != null && tranTypeFinChrgDrn.trim().length() > 0) 
					{
						arrStr = tranTypeFinChrgDrn.split(",");
						tranTypeFinChrgDrn ="";
						for (int i = 0; i < frtDrnArr.length; i++)
						{
							tranTypeFinChrgDrn = tranTypeFinChrgDrn+"'"+frtDrnArr[i]+"',";
						}// loop commented and change by arrStr string array  by nandkumar gadkari on 06/01/20
						for (int i = 0; i < arrStr.length; i++)
						{
							tranTypeFinChrgDrn = tranTypeFinChrgDrn+"'"+arrStr[i]+"',";
						}
						tranTypeFinChrgDrn = tranTypeFinChrgDrn.substring(0,tranTypeFinChrgDrn.length()-1);
						sqlStr = sqlStr + " where ( ( (tran_ser = 'DRNRCP') ";
						if (drnNotIn.trim().length() > 0 ) // 24/01/14 manoharan to FI3JSUN007 do not consider ther freight drn
						{
							sqlStr = sqlStr + " or ( tran_ser = 'MDRCRD' and ref_no not in ( " + drnNotIn + ") ) ) ";
							sqlStr = sqlStr + " and ref_type not in  (" + tranTypeFinChrgDrn + ") ) ";
						}
						else
						{
							sqlStr = sqlStr + " ) ) ";
						}
					}
					else
					{
						sqlStr = sqlStr + " where ( ( (tran_ser = 'DRNRCP' )  ";
						if (drnNotIn.trim().length() > 0 ) // 24/01/14 manoharan to FI3JSUN007 do not consider ther freight drn
						{
							sqlStr = sqlStr + " or ( tran_ser = 'MDRCRD' and ref_no not in ( " + drnNotIn + ") ) ) ";
						}
						else
						{
							sqlStr = sqlStr + " ) ) ";
						}
					}
					//if (len(trim(drnNotIn)) > 0 ) // 24/01/14 manoharan to FI3JSUN007 do not consider ther freight drn
					//sqlStr = sqlStr + " and ( tran_ser = 'MDRCRD' and ref_no not in ( " + drnNotIn + ") ) "
					//}
					// 04-09-2005 manoharan
					//if ((not isnull(ordTypeNewPrd)) and (len(trim(ordTypeNewPrd)) > 0) )
					System.out.println("@@@@@@@@ ordTypeNewPrd["+ordTypeNewPrd+"]adjustNewProduct["+adjustNewProduct+"]tranTypeFinChrgDrn["+tranTypeFinChrgDrn+"]");
					if (ordTypeNewPrd != null && ordTypeNewPrd.trim().length() > 0 && "Y".equals(adjustNewProduct) )
					{
						// end 04-09-2005 manoharan

						arrStr = ordTypeNewPrd.split(",");
						ordTypeNewPrd ="";
						for (int i = 0; i < frtDrnArr.length; i++)
						{
							ordTypeNewPrd = ordTypeNewPrd+"'"+frtDrnArr[i]+"',";
						}
						// loop commented and change by arrStr string array  by nandkumar gadkari on 06/01/20
						for (int i = 0; i < arrStr.length; i++)
						{
							ordTypeNewPrd = ordTypeNewPrd+"'"+arrStr[i]+"',";
						}
						ordTypeNewPrd = ordTypeNewPrd.substring(0,ordTypeNewPrd.length()-1);

						sqlStr = sqlStr + " or ( tran_ser = 'S-INV' " 
						+ " and ref_type in  (" + ordTypeNewPrd + ") "  
						+ " and  ? - due_date >=  " + (llIgnoreDrDays) + " ) ";
					}
					else
					{
						if (tranTypeFinChrgDrn != null && tranTypeFinChrgDrn.trim().length() > 0 && drnNotIn.trim().length() > 0 )
						{
							// NOTHING
						}
						else
						{
							//	sqlStr = sqlStr + " ) ";//TEST
						}
					}
					sqlStr =  sqlStr + " and tot_amt - adj_amt > 0 " 
					+ " and site_code = '" + siteCode + "' " 
					+ " and item_ser  = '" + itemSer + "' " 
					+ " and cust_code = '" + custCode + "' " 
					+ " and case when fin_scheme is null then ' ' else fin_scheme end = '" + finScheme +"' " 
					+ " order by tran_id ";
				}
				else if (adjDrCr && (!adjNewProdInv) )  //Else if (Condtion Added by jasmina 22/01/10-DI89SUN228
				{
					System.out.println("2@@@@@@@@ ");
					sqlStr = "select tran_id, tran_ser, ref_no, tot_amt, adj_amt from receivables ";
					if (tranTypeFinChrgDrn != null && tranTypeFinChrgDrn.trim().length() > 0) 
					{
						arrStr = tranTypeFinChrgDrn.split(",");
						tranTypeFinChrgDrn ="";
						for (int i = 0; i < frtDrnArr.length; i++)
						{
							tranTypeFinChrgDrn = tranTypeFinChrgDrn+"'"+frtDrnArr[i]+"',";
						}
						// loop commented and change by arrStr string array  by nandkumar gadkari on 06/01/20
						for (int i = 0; i < arrStr.length; i++)
						{
							tranTypeFinChrgDrn = tranTypeFinChrgDrn+"'"+arrStr[i]+"',";
						}
						tranTypeFinChrgDrn = tranTypeFinChrgDrn.substring(0,tranTypeFinChrgDrn.length()-1);

						sqlStr = sqlStr + " where ( ( (tran_ser = 'DRNRCP' ) ";
						if (drnNotIn.trim().length() > 0 ) // 24/01/14 manoharan to FI3JSUN007 do not consider ther freight drn
						{
							sqlStr = sqlStr + " or ( tran_ser = 'MDRCRD' and ref_no not in ( " + drnNotIn + ") ) ) ";
						}
						else
						{
							sqlStr = sqlStr + " ) ";
						}
						sqlStr = sqlStr + " and ref_type not in  (" + tranTypeFinChrgDrn + ") ) ";
					}
					else
					{
						sqlStr = sqlStr + " where ( (tran_ser = 'DRNRCP' ) ";
						if (drnNotIn.trim().length() > 0 ) // 24/01/14 manoharan to FI3JSUN007 do not consider ther freight drn
						{
							sqlStr = sqlStr + " or ( tran_ser = 'MDRCRD' and ref_no not in ( " + drnNotIn + ") ) ) ";
						}
						else
						{
							sqlStr = sqlStr + " ) ";
						}
					}
					//if (len(trim(drnNotIn)) > 0 ) // 24/01/14 manoharan to FI3JSUN007 do not consider ther freight drn
					//sqlStr = sqlStr + " and ( tran_ser = 'MDRCRD' and ref_no not in ( " + drnNotIn + ") ) "
					//}
					sqlStr =  sqlStr + " and tot_amt - adj_amt > 0 " 
					+ " and site_code = '" + siteCode + "' " 
					+ " and item_ser  = '" + itemSer + "' " 
					+ " and cust_code = '" + custCode + "' " 
					+ " and case when fin_scheme is null then ' ' else fin_scheme end = '" + finScheme +"' " 
					+ " order by tran_id ";
				}
				else if (!adjDrCr && adjNewProdInv ) //Else if (Condtion Added by jasmina 22/01/10-DI89SUN228
				{
					System.out.println("3@@@@@@@@ ");
					if ( ordTypeNewPrd != null && ordTypeNewPrd.trim().length() > 0 && "Y".equals(adjustNewProduct) )
					{

						arrStr = ordTypeNewPrd.split(",");
						ordTypeNewPrd ="";
						for (int i = 0; i < frtDrnArr.length; i++)
						{
							ordTypeNewPrd = ordTypeNewPrd+"'"+frtDrnArr[i]+"',";
						}
						// loop commented and change by arrStr string array  by nandkumar gadkari on 06/01/20
						for (int i = 0; i < arrStr.length; i++)
						{
							ordTypeNewPrd = ordTypeNewPrd+"'"+arrStr[i]+"',";
						}
						ordTypeNewPrd = ordTypeNewPrd.substring(0,ordTypeNewPrd.length()-1);


						sqlStr = "select tran_id, tran_ser, ref_no, tot_amt, adj_amt from receivables "
							+ " where tran_ser = 'S-INV' "  
							+ " and ref_type in  (" + ordTypeNewPrd + ") "  
							+ " and  ? - due_date >=  " + llIgnoreDrDays + " "
							+ " and tot_amt - adj_amt > 0 "
							+ " and site_code = '" + siteCode + "' " 
							+ " and item_ser  = '" + itemSer + "' "
							+ " and cust_code = '" + custCode + "' " 
							+ " and case when fin_scheme is null then ' ' else fin_scheme end = '" + finScheme +"' " 
							+ " order by tran_id ";
					}
				}



				sqlStr1 = "insert into receivables_adj "
					+ "(tran_id, ref_ser, ref_no, tot_amt, adj_amt, net_amt,ref_ser_adj,ref_no_adj, tran_id__rcv) "
					+ " values (?, ?, ?, ?, ?, 0, ?, ? ,?) ";
				pstmtInsert  = conn.prepareStatement(sqlStr1);

				pstmt = conn.prepareStatement(sqlStr);
				rs = pstmt.executeQuery();
				while(rs.next())
				{
					mTranId = rs.getString("tran_id");
					mTranSer = rs.getString("tran_ser");
					mRefNo = rs.getString("ref_no");
					mTotAmt = rs.getDouble("tot_amt");
					mAdjAmt = rs.getDouble("adj_amt");

					System.out.println("@@@@@@@@530 mDrAmt["+mDrAmt+"]");
					mDiffAmt = mTotAmt - mAdjAmt;
					mDrAmt = mDrAmt + mDiffAmt;
					System.out.println("@@@@@@@@533 mDrAmt["+mDrAmt+"]");
					xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
					xmlValues = xmlValues + "<Header></Header>";
					xmlValues = xmlValues + "<Detail1>";
					xmlValues = xmlValues +	"<tran_id></tran_id>";
					xmlValues = xmlValues + "<site_code>" + siteCode + "</site_code>";	
					xmlValues = xmlValues +"</Detail1></Root>";
					System.out.println("xmlValues  :["+xmlValues+"]");
					TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", CommonConstants.DB_NAME);
					nextID = tg.generateTranSeqID("R-ADJ", "tran_id", keystr, conn);
					System.out.println("nextID ["+nextID + "]");

					// 09-01-02 End Arif
					pstmtInsert.setString(1,nextID);
					pstmtInsert.setString(2,mTranSer);
					pstmtInsert.setString(3,mRefNo);
					pstmtInsert.setDouble(4,mDiffAmt);
					pstmtInsert.setDouble(5,mDiffAmt);
					pstmtInsert.setString(6,adjTranSer);
					pstmtInsert.setString(7,adjRefNo);
					pstmtInsert.setString(8,mTranId);
					pstmtInsert.addBatch();
					pstmtInsert.clearParameters();
					// 09-01-02 End Arif
					mChkRec++;
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				pstmtInsert.executeBatch();
				pstmtInsert.clearParameters();
				//pstmtInsert.close();
				mTotAmt = 0;
				mAdjAmt = 0;
				mDiffAmt = 0;
			}*/
			//commeted by nandkumar gadkari on 06/01/20 for duplicate block 
			// end 24/01/14 manoharan

			// 04-09-2005 manoharan
			// reverted on 15-09-2005 as per sun requirement
			//if ab_drcr = TRUE then //Commented by jasmina-19/01/10-DI89SUN228

			String[] arrStr;
			if (adjDrCr  || (adjNewProdInv  && ("Y".equals(adjustNewProduct) && ordTypeNewPrd != null && ordTypeNewPrd.trim().length() > 0) )) //Added by jasmina-19/01/10-DI89SUN228
			{

				//if (adjDrCr or adjustNewProduct = 'Y' then
				// 03-08-2005 manoharan dynamic cursor used to have multiple
				// order/transaction types specified in disparm for new product/ financial charges
				//declare c_recv cursor for
				//select tran_id, tran_ser, ref_no, tot_amt, adj_amt from receivables
				//    where (tran_ser = 'DRNRCP' or tran_ser = 'MDRCRD')
				//      and ( ((ref_type = 'FC' or ref_type = :ordTypeNewPrd )
				//                and ( (:mrefdate - ref_date ) >= :ll_ignore_dr_days  )) OR (ref_type <> 'FC' and ref_type <> :ordTypeNewPrd) )
				//      and (tot_amt - adj_amt) > 0
				//      and site_code = :as_site_code
				//      and item_ser  = :as_item_ser
				//      and cust_code = :as_cust_code
				//      and (case when fin_scheme is null then ' ' else fin_scheme end ) = :ls_finscheme
				//order by tran_id;
				//open c_recv;
				//if (get_sqlcode() < 0 then
				//    merrcode = 'DS000' + string(sqlca.sqldbcode)
				//    return merrcode
				//end if
				//

				//String[] arrStr;
				if (adjDrCr && adjNewProdInv) //if (condition Added by jasmina-19/01/10-DI89SUN228
				{
					System.out.println("4@@@@@@@@ ");
					//old logic kept in this Condition
					sqlStr = "select tran_id, tran_ser, ref_no, tot_amt, adj_amt from receivables ";
					if (tranTypeFinChrgDrn != null && tranTypeFinChrgDrn.trim().length() > 0)
					{

						arrStr = tranTypeFinChrgDrn.split(",");
						tranTypeFinChrgDrn ="";
						/*for (int i = 0; i < frtDrnArr.length; i++)
						{
							tranTypeFinChrgDrn = tranTypeFinChrgDrn+"'"+frtDrnArr[i]+"',";
						}*/
						// loop commented and change by arrStr string array  by nandkumar gadkari on 06/01/20
						for (int i = 0; i < arrStr.length; i++)
						{
							tranTypeFinChrgDrn = tranTypeFinChrgDrn+"'"+arrStr[i]+"',";
						}
						tranTypeFinChrgDrn = tranTypeFinChrgDrn.substring(0,tranTypeFinChrgDrn.length()-1);



						sqlStr = sqlStr + " where ( ( (tran_ser = 'DRNRCP') "  ;
						if (drnNotIn.trim().length() > 0 ) // 24/01/14 manoharan to FI3JSUN007 do not consider ther freight drn
						{
							sqlStr = sqlStr + " or ( tran_ser = 'MDRCRD' and ref_no not in ( " + drnNotIn + ") ) ) " ;
						}
						else
						{
							sqlStr = sqlStr + " or ( tran_ser = 'MDRCRD' ) )  ";//added by nandkumar gadkari on 06/01/20
						}
						sqlStr = sqlStr + " and ref_type not in  (" + tranTypeFinChrgDrn + ") ) " ;
					}
					else
					{
						sqlStr = sqlStr + " where ( ( (tran_ser = 'DRNRCP' )  ";
						if (drnNotIn.trim().length() > 0 ) // 24/01/14 manoharan to FI3JSUN007 do not consider ther freight drn
						{
							sqlStr = sqlStr + " or ( tran_ser = 'MDRCRD' and ref_no not in ( " + drnNotIn + ") ) ) ";
						}	
						else
						{
							sqlStr = sqlStr + " or ( tran_ser = 'MDRCRD' ) ) ) ";//added by nandkumar gadkari on 06/01/20
						}
					}
					//        if (len(trim(drnNotIn)) > 0 then // 24/01/14 manoharan to FI3JSUN007 do not consider ther freight drn
					//            sqlStr = sqlStr + " and ( tran_ser = 'MDRCRD' and ref_no not in ( " + drnNotIn + ") ) "
					//        }
					// 04-09-2005 manoharan
					//if ((not isnull(ordTypeNewPrd)) and (len(trim(ordTypeNewPrd)) > 0) then
					if (ordTypeNewPrd != null && ordTypeNewPrd.trim().length() > 0 && "Y".equals(adjustNewProduct))
					{
						arrStr = ordTypeNewPrd.split(",");
						ordTypeNewPrd ="";
						/*for (int i = 0; i < frtDrnArr.length; i++)
						{
							ordTypeNewPrd = ordTypeNewPrd+"'"+frtDrnArr[i]+"',";
						}*/
						// loop commented and change by arrStr string array  by nandkumar gadkari on 06/01/20
						for (int i = 0; i < arrStr.length; i++)
						{
							ordTypeNewPrd = ordTypeNewPrd+"'"+arrStr[i]+"',";
						}
						ordTypeNewPrd = ordTypeNewPrd.substring(0,ordTypeNewPrd.length()-1);
						// end 04-09-2005 manoharan
						sqlStr = sqlStr + " or ( tran_ser = 'S-INV' "  + 
						" and ref_type in  (" + ordTypeNewPrd + ") "  + 
						" and  cast( ? as date)  - due_date >=  " + llIgnoreDrDays + " ) ";// cast( ?  as date)  added by nandkumar gadkari on 06/01/19
					}
					else
					{
						if (tranTypeFinChrgDrn != null && tranTypeFinChrgDrn.trim().length() > 0 && drnNotIn.trim().length() > 0 )
						{
							// NOTHING
						}
						else
						{
							//sqlStr = sqlStr + " ) ";
						}
					}
					sqlStr =  sqlStr + " and tot_amt - adj_amt > 0 " 
					+ " and site_code = '" + siteCode + "' " 
					+ " and item_ser  = '" + itemSer + "' " 
					+ " and cust_code = '" + custCode + "' "
					+ " and case when fin_scheme is null then ' ' else fin_scheme end = '" + finScheme +"' " 
					+ " order by tran_id ";
				}
				else if (adjDrCr && !adjNewProdInv)  //Else if (Condtion Added by jasmina 22/01/10-DI89SUN228
				{
					System.out.println("5@@@@@@@@ tranTypeFinChrgDrn["+tranTypeFinChrgDrn+"]");
					sqlStr = "select tran_id, tran_ser, ref_no, tot_amt, adj_amt from receivables ";
					if (tranTypeFinChrgDrn != null && tranTypeFinChrgDrn.trim().length() > 0)
					{
						arrStr = tranTypeFinChrgDrn.split(",");
						tranTypeFinChrgDrn ="";
						/*for (int i = 0; i < frtDrnArr.length; i++)
						{
							tranTypeFinChrgDrn = tranTypeFinChrgDrn+"'"+frtDrnArr[i]+"',";
						}*/
						// loop commented and change by arrStr string array  by nandkumar gadkari on 06/01/20
						for (int i = 0; i < arrStr.length; i++)
						{
							tranTypeFinChrgDrn = tranTypeFinChrgDrn+"'"+arrStr[i]+"',";
						}
						tranTypeFinChrgDrn = tranTypeFinChrgDrn.substring(0,tranTypeFinChrgDrn.length()-1);

						sqlStr = sqlStr + " where ( ( (tran_ser = 'DRNRCP' )";
						if ( drnNotIn.trim().length() > 0 ) // 24/01/14 manoharan to FI3JSUN007 do not consider ther freight drn
						{
							sqlStr = sqlStr + " or ( tran_ser = 'MDRCRD' and ref_no not in ( " + drnNotIn + ") ) )  "; 
						}	
						else
						{
							sqlStr = sqlStr + " or ( tran_ser = 'MDRCRD' ) ) ";//sqlStr = sqlStr + " ) "; 
						}
						sqlStr = sqlStr + " and ref_type not in  (" + tranTypeFinChrgDrn + ") ) ";
					}
					else
					{
						sqlStr = sqlStr + " where ( (tran_ser = 'DRNRCP' ) ";
						if ( drnNotIn.trim().length() > 0 ) // 24/01/14 manoharan to FI3JSUN007 do not consider ther freight drn
						{
							sqlStr = sqlStr + " or ( tran_ser = 'MDRCRD' and ref_no not in ( " + drnNotIn + ") ) ) ";
						}
						else
						{
							sqlStr = sqlStr + " or ( tran_ser = 'MDRCRD' ) ) ";//sqlStr = sqlStr + " ) ";
						}
					}
					//if (len(trim(drnNotIn)) > 0 then // 24/01/14 manoharan to FI3JSUN007 do not consider ther freight drn
					//sqlStr = sqlStr + " and ( tran_ser = 'MDRCRD' and ref_no not in ( " + drnNotIn + ") ) "
					//}
					sqlStr =  sqlStr + " and tot_amt - adj_amt > 0 "
					+ " and site_code = '" + siteCode + "' "
					+ " and item_ser  = '" + itemSer + "' "
					+ " and cust_code = '" + custCode + "' " 
					+ " and case when fin_scheme is null then ' ' else fin_scheme end = '" + finScheme +"' " 
					+ " order by tran_id ";
				}	
				else if (!adjDrCr && adjNewProdInv) //Else if (Condtion Added by jasmina 22/01/10-DI89SUN228
				{
					System.out.println("6@@@@@@@@ ");
					if ((ordTypeNewPrd != null && ordTypeNewPrd.trim().length() > 0) && "Y".equals(adjustNewProduct))
					{
						arrStr = ordTypeNewPrd.split(",");
						ordTypeNewPrd ="";
						/*for (int i = 0; i < frtDrnArr.length; i++)
						{
							ordTypeNewPrd = ordTypeNewPrd+"'"+frtDrnArr[i]+"',";
						}*/
						// loop commented and change by arrStr string array  by nandkumar gadkari on 06/01/20
						for (int i = 0; i < arrStr.length; i++)
						{
							ordTypeNewPrd = ordTypeNewPrd+"'"+arrStr[i]+"',";
						}
						ordTypeNewPrd = ordTypeNewPrd.substring(0,ordTypeNewPrd.length()-1);

						sqlStr = "select tran_id, tran_ser, ref_no, tot_amt, adj_amt from receivables "
							+ " where tran_ser = 'S-INV' "  
							+ " and ref_type in  (" + ordTypeNewPrd + ") " 
							+ " and  cast( ? as date) - due_date >=  " + llIgnoreDrDays + " " // cast( ?  as date)  added by nandkumar gadkari on 06/01/19
							+ " and tot_amt - adj_amt > 0 " 
							+ " and site_code = '" + siteCode + "' " 
							+ " and item_ser  = '" + itemSer + "' " 
							+ " and cust_code = '" + custCode + "' " 
							+ " and case when fin_scheme is null then ' ' else fin_scheme end = '" + finScheme +"' " 
							+ " order by tran_id ";
					}
				}
				//sqlstr11 added by nandkumar gadkari on 06/01/20
				sqlStr1 = "insert into receivables_adj "
						+ "(tran_id, ref_ser, ref_no, tot_amt, adj_amt, net_amt,ref_ser_adj,ref_no_adj, tran_id__rcv) "
						+ " values (?, ?, ?, ?, ?, 0, ?, ? ,?) ";
				pstmtInsert  = conn.prepareStatement(sqlStr1);
					
				pstmt = conn.prepareStatement(sqlStr);
				if (adjNewProdInv && ordTypeNewPrd != null && ordTypeNewPrd.trim().length() > 0 && "Y".equals(adjustNewProduct)) //Added by jasmina 22/01/10-DI89SUN228
				{
					pstmt.setTimestamp(1,mRefDate);
				}

				rs = pstmt.executeQuery();
				
				while(rs.next())
				{
					// end 03-08-2005 manoharan dynamic cursor used to have multiple
					// order/transaction types specified in disparm for new product/ financial charges

					mTranId = rs.getString("tran_id");
					mTranSer = rs.getString("tran_ser");
					mRefNo = rs.getString("ref_no");
					mTotAmt = rs.getDouble("tot_amt");
					mAdjAmt = rs.getDouble("adj_amt");
					System.out.println("@@@@@@@@771 mDrAmt["+mDrAmt+"]");
					mDiffAmt = mTotAmt - mAdjAmt;
					mDrAmt = mDrAmt + mDiffAmt;
					System.out.println("@@@@@@@@773 mDrAmt["+mDrAmt+"]");
					// 09-01-02 Arif (to get the tran id from nvo_keygen
					xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
					xmlValues = xmlValues + "<Header></Header>";
					xmlValues = xmlValues + "<Detail1>";
					xmlValues = xmlValues +	"<tran_id></tran_id>";
					xmlValues = xmlValues + "<site_code>" + siteCode + "</site_code>";	
					xmlValues = xmlValues +"</Detail1></Root>";
					System.out.println("xmlValues  :["+xmlValues+"]");
					TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", CommonConstants.DB_NAME);
					nextID = tg.generateTranSeqID("R-ADJ", "tran_id", keystr, conn);
					System.out.println("nextID ["+nextID + "]");

					// 09-01-02 End Arif
					pstmtInsert.setString(1,nextID);
					pstmtInsert.setString(2,mTranSer);
					pstmtInsert.setString(3,mRefNo);
					pstmtInsert.setDouble(4,mDiffAmt);
					pstmtInsert.setDouble(5,mDiffAmt);
					pstmtInsert.setString(6,adjTranSer);
					pstmtInsert.setString(7,adjRefNo);
					pstmtInsert.setString(8,mTranId);
					pstmtInsert.addBatch();
					pstmtInsert.clearParameters();

					// 09-01-02 End Arif
					//////////////////////////////////////////////////////////////////
					// 14/03/01 manoharan update of receivables commented
					// only insert of receivables_adj records will be taken place in
					// this function. The related updates will be done when the invoice
					// is confirmed
					//update receivables
					//  set adj_amt = tot_amt, status = 'A'
					//  where tran_id = :mTranId;
					//if (get_sqlcode() < 0 then
					//    merrcode = 'DS000' + string(sqlca.sqldbcode)
					//    return merrcode
					//elseif (sqlca.sqlnrows <> 1 then
					//    merrcode = 'DS000NR'
					//} (     
					mChkRec++;
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				pstmtInsert.executeBatch();
				pstmtInsert.clearParameters();
				//pstmtInsert.close();
				mTotAmt = 0;
				mAdjAmt = 0;
				mDiffAmt = 0;
			}
			// CRNRCP Processing
			//declare c_credit cursor for
			//select tran_id, tran_ser, ref_no, tot_amt, adj_amt from receivables
			//    where ((tran_ser = 'CRNRCP' or tran_ser = 'MDRCRC' or tran_ser = 'R-ADV' ) and (tot_amt - adj_amt) < 0)
			//      and site_code = :as_site_code   and item_ser  = :as_item_ser
			//      and cust_code = :as_cust_code  order by tran_id;

			// 04-09-2005 manoharan if (dr/cr adjustment and advance adjustment not selected
			// no need to do the following adjustment
			// end 04-09-2005 manoharan
			if (adjDrCr || adjAdv)
			{

				if (adjDrCr && adjAdv)
				{
					tranSeries = "'CRNRCP','MDRCRC', 'R-ADV'";
				}
				else if (adjDrCr && !adjAdv)
				{
					tranSeries = "'CRNRCP', 'MDRCRC'";
				}
				else if (!adjDrCr && adjAdv)
				{
					tranSeries = "'R-ADV'";
				}   
				//Amit 21/01/05 'CUST_ADV'
				asjCustAdv = distCommon.getDisparams("999999","CUST_ADVADJ_TYPE",conn);
				//added by nandkumar gadkari on 08/04/19-----------start----for ref_type filter--------
				invAutoAdjCrtp = distCommon.getDisparams("999999","INV_AUTO_ADJ_CRTYPE",conn);
				if ("NULLFOUND".equals(invAutoAdjCrtp) || invAutoAdjCrtp == null )
				{
					invAutoAdjCrtp = " ";
				}
				arrStr = invAutoAdjCrtp.split(",");
				invAutoAdjCrtp="";
				for(int i = 0; i<arrStr.length; i++)
				{
					if(  arrStr[i] != null && arrStr[i].trim().length() > 0)
					{
						invAutoAdjCrtp =invAutoAdjCrtp+"'"+arrStr[i]+"',";
					}
				}
				if( invAutoAdjCrtp != null && invAutoAdjCrtp.trim().length() > 0)
				{
					invAutoAdjCrtp =invAutoAdjCrtp.substring(0,invAutoAdjCrtp.length()-1);
				}
				System.out.println("invAutoAdjCrtp="+invAutoAdjCrtp);
				//added by nandkumar gadkari on 08/04/19-----------end-----for ref_type filter-------
				if ("C".equals(advAdj))  //prince 11/11/05
				{
					if ("I".equals(asjCustAdv))
					{
						sqlStr = "select tran_id, tran_ser, ref_no, tot_amt, adj_amt from receivables "
							+ " where ((tran_ser in (" + tranSeries  + ")) and (tot_amt - adj_amt) < 0 and (fin_scheme is null or fin_scheme = ' ')) "
							+ "  and (site_code = ?  and item_ser  = ?  and cust_code = ?) ";
					// if condition added by nandkumar gadkari on 08/04/19 for ref_type filter-------
						if(invAutoAdjCrtp.trim().length()> 0)
							{
								sqlStr =sqlStr + "  and ref_type in ("+invAutoAdjCrtp+")";
							}
						sqlStr= sqlStr + " order by tran_id ";
						pstmt = conn.prepareStatement(sqlStr);
						pstmt.setString(1,siteCode);
						pstmt.setString(2,itemSer);
						pstmt.setString(3,custCode);
						//changes done by Vyankatesh on 24-01-05 for subscription    
					}
					else if ("S".equals(asjCustAdv))
					{
						sqlStr = "select cust_pord from	sorder where sale_order = ?";
						pstmt1 = conn.prepareStatement(sqlStr);
						pstmt1.setString(1,mOrderNo);
						rs1 = pstmt1.executeQuery();
						if (rs1.next())
						{
							subsNo = rs1.getString("cust_pord");
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;

						sqlStr = "select rcp_no, site_code__rec,site_code__dlv "
							+ " from subscrip_rcp a,subscrip_rcpdet b "
							+ " where a.tran_id = b.tran_id and b.subs_no = ?";
						pstmt1 = conn.prepareStatement(sqlStr);
						pstmt1.setString(1,subsNo);
						rs1 = pstmt1.executeQuery();
						if (rs1.next())
						{
							rcpNo = rs1.getString("cust_pord");
							siteCodeRcp = rs1.getString("cust_pord");
							siteCodeDlv = rs1.getString("cust_pord");
						}
						rs1.close();
						rs1 = null;
						pstmt1.close();
						pstmt1 = null;


						if (!siteCodeRcp.trim().equals(siteCodeDlv.trim()))
						{
							tranSeries ="'R-IBCA'";
						}

						sqlStr = "select tran_id, tran_ser, ref_no, tot_amt, adj_amt from receivables "
							+ " where ((tran_ser in (" +tranSeries  + ")) and (tot_amt - adj_amt) < 0 and (fin_scheme is null or fin_scheme = ' ')) "
							+ " and (site_code = ?  and ref_no  = ?  and cust_code = ?) " ;
							// if condition added by nandkumar gadkari on 08/04/19 for ref_type filter-------
							if(invAutoAdjCrtp.trim().length()> 0)
								{
									sqlStr =sqlStr + "  and ref_type in ("+invAutoAdjCrtp+")";
								}
							sqlStr= sqlStr + " order by tran_id ";
						pstmt = conn.prepareStatement(sqlStr);
						pstmt.setString(1,siteCode);
						pstmt.setString(2,rcpNo);
						pstmt.setString(3,custCode);
						//changes end by Vyankatesh on 24-01-05 for subscription    
					}
					else
					{
						sqlStr = "select tran_id, tran_ser, ref_no, tot_amt, adj_amt from receivables "
							+ " where ((tran_ser in (" +tranSeries  + ")) and (tot_amt - adj_amt) < 0 and (fin_scheme is null or fin_scheme = ' ')) "
							+ " and ((site_code = ?  and item_ser  = ?  and cust_code = ?) "
							+ " OR  ( site_code = ?  and cust_code = ?))  ";
						// if condition added by nandkumar gadkari on 08/04/19 for ref_type filter-------
						if(invAutoAdjCrtp.trim().length()> 0)
							{
								sqlStr =sqlStr + "  and ref_type in ("+invAutoAdjCrtp+")";
							}
						sqlStr= sqlStr + " order by tran_id ";
						pstmt = conn.prepareStatement(sqlStr);
						pstmt.setString(1,siteCode);
						pstmt.setString(2,itemSer);
						pstmt.setString(3,custCode);
						pstmt.setString(4,siteCode);
						pstmt.setString(5,custCode);
					}
				}
				else
				{
					//adjust advance thorugh sale order wise prince 10/11/05
					sqlStr = "select tran_id, tran_ser, ref_no, tot_amt, adj_amt from receivables "
						+ " where ((tran_ser in (" +tranSeries  + ")) and (tot_amt - adj_amt) < 0  ) "
						+ " and ((site_code = ?  and item_ser  = ?  and cust_code = ?) "
						+ " OR  ( site_code = ?  and cust_code = ?)) "
						+ " and ( sale_order = ?)  ";
					// if condition added by nandkumar gadkari on 08/04/19 for ref_type filter-------
					if(invAutoAdjCrtp.trim().length()> 0)
						{
							sqlStr =sqlStr + "  and ref_type in ("+invAutoAdjCrtp+")";
						}
					sqlStr= sqlStr + " order by tran_id ";
					pstmt = conn.prepareStatement(sqlStr);
					pstmt.setString(1,siteCode);
					pstmt.setString(2,itemSer);
					pstmt.setString(3,custCode);
					pstmt.setString(4,siteCode);
					pstmt.setString(5,custCode);
					pstmt.setString(6,mOrderNo);
				}
				//PREPARE SQLSA FROM :sqlStr ;
				//OPEN DYNAMIC c_credit using :siteCode, :itemSer, :custCode, :siteCode, :custCode ;
				//end Amit 21/01/05 'CUST_ADV'      

				rs = pstmt.executeQuery();
				while(rs.next())
				{

					mTranId = rs.getString("tran_id");
					mTranSer = rs.getString("tran_ser");
					mRefNo = rs.getString("ref_no");
					mTotAmt = rs.getDouble("tot_amt");
					mAdjAmt = rs.getDouble("adj_amt");
					System.out.println("@@@@ mDrAmt["+mDrAmt+"]mTranId["+mTranId+"]mTranSer["+mTranSer+"]mRefNo["+mRefNo+"]mTotAmt["+mTotAmt+"]mAdjAmt["+mAdjAmt+"]");
					if (mDrAmt == 0)
					{
						break;
					}
					mDiffAmt = mTotAmt - mAdjAmt;

					if (mDrAmt >= Math.abs(mDiffAmt))
					{
						mDrAmt = mDrAmt - Math.abs(mDiffAmt);
						mAdjAmt = mDiffAmt;
						status = "A";
					}
					else
					{
						mAdjAmt = (mDrAmt * -1);
						mDrAmt = 0;
						status = "P";
					}
					// 09-01-02 Arif (to get the tran id from nvo_keygen
					xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
					xmlValues = xmlValues + "<Header></Header>";
					xmlValues = xmlValues + "<Detail1>";
					xmlValues = xmlValues +	"<tran_id></tran_id>";
					xmlValues = xmlValues + "<site_code>" + siteCode + "</site_code>";	
					xmlValues = xmlValues +"</Detail1></Root>";
					System.out.println("xmlValues  :["+xmlValues+"]");
					TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", CommonConstants.DB_NAME);
					nextID = tg.generateTranSeqID("R-ADJ", "tran_id", keystr, conn);
					System.out.println("nextID ["+nextID + "]");
					// 09-01-02 End Arif
					if(pstmtInsert != null)
					{
						pstmtInsert.close();
						pstmtInsert = null;
					}
					sqlStr1 = "insert into receivables_adj "
						+ " (tran_id, ref_ser, ref_no, tot_amt, adj_amt, net_amt,ref_ser_adj,ref_no_adj, tran_id__rcv) "
						+ " values (?, ?, ?, ?, ?, ?, ?, ? ,?) ";
					pstmtInsert = conn.prepareStatement(sqlStr1);

					pstmtInsert.setString(1,nextID);
					pstmtInsert.setString(2,mTranSer);
					pstmtInsert.setString(3,mRefNo);
					pstmtInsert.setDouble(4,mDiffAmt);
					pstmtInsert.setDouble(5,mAdjAmt);
					pstmtInsert.setDouble(6,(mDiffAmt - mAdjAmt));
					pstmtInsert.setString(7,adjTranSer);
					pstmtInsert.setString(8,adjRefNo);
					pstmtInsert.setString(9,mTranId);

					pstmtInsert.executeUpdate();
					pstmtInsert.close();
					pstmtInsert = null;

					mChkRec++;
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				//pstmtInsert.executeBatch();
				//pstmtInsert.close();
			}
			System.out.println("@@@@@@ mChkRec["+mChkRec+"]");
			//**************************      
			if (mChkRec > 0 )
			{
				// 09-01-02 Arif (to get the tran id from nvo_keygen
				xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
				xmlValues = xmlValues + "<Header></Header>";
				xmlValues = xmlValues + "<Detail1>";
				xmlValues = xmlValues +	"<tran_id></tran_id>";
				xmlValues = xmlValues + "<site_code>" + siteCode + "</site_code>";	
				xmlValues = xmlValues +"</Detail1></Root>";
				System.out.println("xmlValues  :["+xmlValues+"]");
				TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", CommonConstants.DB_NAME);
				nextID = tg.generateTranSeqID("R-ADJ", "tran_id", keystr, conn);
				System.out.println("nextID ["+nextID + "]");
				// 09-01-02 End Arif
				sqlStr1 = "INSERT INTO receivables_adj "
					+ " (tran_id, ref_ser, ref_no, tot_amt, "
					+ " adj_amt, ref_ser_adj, ref_no_adj, "
					+ " net_amt, tran_id__rcv ) "
					+ " values (?, ?, ?, ?, ?, ' ', ' ', ?, ' ') ";
				pstmtInsert = conn.prepareStatement(sqlStr1);

				pstmtInsert.setString(1,nextID);
				pstmtInsert.setString(2,adjTranSer);
				pstmtInsert.setString(3,adjRefNo);
				pstmtInsert.setDouble(4,netAmt);
				pstmtInsert.setDouble(5,((mDrAmt - netAmt) * -1));
				pstmtInsert.setDouble(6,(netAmt - (mDrAmt - netAmt) * -1));
				pstmtInsert.executeUpdate();
				pstmtInsert.close();
				//ended by prince                
				sqlStr = "update invoice "
					+ " set adj_amount = ? "
					+ " where invoice_id  =  ? ";
				pstmtInsert = conn.prepareStatement(sqlStr);

				pstmtInsert.setDouble(1,(mDrAmt - netAmt) * -1);
				pstmtInsert.setString(2,invoiceId);
				pstmtInsert.executeUpdate();

			}


		}
		catch( Exception e)
		{
			e.printStackTrace();
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
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if(rs1 != null)
				{
					rs1.close();
					rs1 = null;
				}
				if(pstmt1 != null)
				{
					pstmt1.close();
					pstmt1 = null;
				}
				if(pstmtInsert != null)
				{
					pstmtInsert.close();
					pstmtInsert = null;
				}
				//conn.close();
			}
			catch(Exception e)
			{
				System.out.println("Exception : "+e);e.printStackTrace();
				throw new ITMException(e);
			}

		}
		return errString;
	}
}