/**
 * PURPOSE : Pre Save Logic 
 * AUTHOR : Priyanka Chavan.
 */

package ibase.webitm.ejb.dis;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import javax.naming.InitialContext;

import ibase.ejb.CommonDBAccessEJB;
import ibase.ejb.CommonDBAccessRemote;
import ibase.system.config.AppConnectParm;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.MasterStatefulLocal;
import ibase.webitm.ejb.XML2DBEJB;
import ibase.webitm.ejb.dis.adv.AdjIssueRcpConf;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.utility.ITMException;
@javax.ejb.Stateless
public class SRLContainerSplit extends ActionHandlerEJB implements SRLContainerSplitLocal, SRLContainerSplitRemote
{
	E12GenericUtility genericUtility = new E12GenericUtility();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	@Override
	public String confirm() throws RemoteException, ITMException 
	{
		return "";
	}
	@Override

	public String confirm(String tranID,String xtraParams, String forcedFlag)throws RemoteException,ITMException
	{		
		String errString = null;
		Connection conn = null; 

		try
		{		
			conn = getConnection();
			System.out.println("Inside confirm method of [SRLContainerSplit] class");
			System.out.println("[Transaction ID :: ["+tranID+"]] \n [XtraParams :: ["+xtraParams+"]] \n [Forced Flag ["+forcedFlag+"]]");

			errString = issueAdjReceiptAndIssue(tranID, xtraParams, forcedFlag,conn);

			System.out.println("[Errorstring :::::::::["+errString+"]]");			
			
			if (errString == null || errString.trim().length() == 0) 
			{
				errString  = itmDBAccessEJB.getErrorString("", "VTAUTOSPLT", "", "", conn);
				conn.commit();
				/*if(conn != null)
				{
					conn.close();
					conn = null;
				}*/
			} 
			else 
			{
				conn.rollback();
				/*if(conn != null)
				{
					conn.close();
					conn = null;
				}*/
				//return errString;
			}
		}
		catch(Exception e)
		{
			//Added by Anjali on 05 NOV 2018
			try 
			{
				conn.rollback();
			} 
			catch (SQLException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
				throw new ITMException(e1);
			}
			System.out.println("Exception Inside [SRLContainerSplit [confirm] Method] ::["+e+"]");
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try 
			{
				if (conn != null) 
				{
					System.out.println("InSide finally..  rollbacking >>>>>>>>>>>>>>>>> ");
					conn.rollback();
					conn.close();
					System.out.println("InSide finally.. Connection has been closed >>>>>>>>>>>>>>>>>>>>>>>>> ");
				}
				conn = null;
			} 
			catch (SQLException se) 
			{
				System.out.println("SQL Exception in finaly >>>> "+ se.getMessage());
				throw new ITMException(se);
			} 
			catch (Exception d) 
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		return errString;		
	}// end of method **confirm**


	private String issueAdjReceiptAndIssue(String tranId,String xtraParams,String forcedFlag,Connection conn) throws RemoteException, ITMException
	{

		String retSting = "" , sql = "" , itemCodeHdr = "" , lotNoHdr = "" , lotSlHdr = "" , userId = "";
		PreparedStatement pstmt = null , pstmtStock = null;
		ResultSet rs = null , rsStock = null;
		String siteCode = "", locationCode="",itemSer="",invStat="",acctCodeOh="",cctrCodeOh="",considerAllocate="",itemCodeDet="",lotNodet = "",lotSlDet =""; 
		Double quantity = 0.0, allocQty = 0.0, potencyPerc=0.0, grossWeight=0.0, tare_weight=0.0, net_weight=0.0, rateOh=0.0,noArt=0.0  ; 
		String acctCodeDr="",cctrCodeDr="",packCode="",siteCodeMfg="",packInstr="",suppCodeMfg="",unitAlt="",batchNo="",unit="",grade="",remarks="",dimension="";
		double stkGrossRate = 0, stkRate=0, convQtyStduom=0, batchSize=0, holdQuantity=0, actualRate=0,splitQty =0;
		java.sql.Timestamp expDate = null;
		java.sql.Timestamp mfgDate = null;
		java.sql.Timestamp restestDate = null;
		java.sql.Timestamp creaDate = null;
		java.sql.Timestamp ltranDate = null;
		java.sql.Timestamp lastRcpDate = null;
		java.sql.Timestamp lastIssDate = null;
		java.sql.Timestamp lastPhyDate = null;
		java.sql.Timestamp tranSerialDate = null;
		StringBuffer valueXmlforIssue = new StringBuffer();
		StringBuffer valueXmlforReceipt = new StringBuffer();
		boolean issueCondition = false;
		boolean receiptCondition = false , isError = false;
		String flagSaveString = "" , tranIdForIssue = "",tranIdForReciept ="";
		String invSrnoReqd=""; //Added by sarita on 14APR2018
		FinCommon finCommon = new FinCommon();
		//changes by sarita 19MARCH2018
		String statusSrl = "";
		int cnt = 0 , stkCnt = 0;
		//Added by sarita on 29 OCT 2018 [START]
		String avalibleFrLocCode = "";
		//Added by sarita on 29 OCT 2018 [END]
		//Added by sarita on 02 NOV 2018 [START]
		//HashMap stockMap = new HashMap<>();//Commented by sarita as it was not working in JDK1.6 on 14 NOV 18
		HashMap stockMap = new HashMap();
		ArrayList stockDataList = new ArrayList();
		String refSer = "", chgTerm = "",wrkflwInit = "", wrkflwOpt = "", transactionID = "";
		XML2DBEJB xml2DBLocal = new XML2DBEJB();
		boolean isWrkflwToBeInitiated = false;
		UserInfoBean userInfo = null;
		int formCnt = 0;
		//Added by sarita on 02 NOV 2018 [END]
		try
		{
			//conn = getConnection();
			userId = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode"));
			chgTerm = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId"));
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());
			//Added by sarita on 18MARCH2018
			sql = "select item_code , lot_no , lot_sl,serial_date,status from srl_container where serial_no=?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				itemCodeHdr = rs.getString("item_code");
				lotNoHdr = rs.getString("lot_no");
				lotSlHdr = rs.getString("lot_sl");
				tranSerialDate = rs.getTimestamp("serial_date");
				statusSrl = rs.getString("status");
				System.out.println("For Header Data ::::[item_code ["+itemCodeHdr+"]] \t [lot_no ["+lotNoHdr+"]] \t [lot_sl ["+lotSlHdr+"]] \t [status ["+statusSrl+"]]");
			}
			if(rs != null){rs.close();rs = null;}
			if(pstmt != null){pstmt.close();pstmt = null;}

			
			//Showing Error Message Transaction Splitted Already.[start]
			if("C".equalsIgnoreCase(statusSrl))
			{
				//retSting = itmDBAccessEJB.getErrorString("", "VTCONFMSP", "");
				retSting = itmDBAccessEJB.getErrorString("", "VTCONFMSP", "", "", conn);
				//Added by sarita on 19MARCH2018
				isError = true;
				return retSting;
			}	
			//Added by sarita to check transaction is confirmed or not on 14MARCH2019 [START]
			else if(!"A".equalsIgnoreCase(statusSrl))
			{
				retSting = itmDBAccessEJB.getErrorString("", "UNCONTRAN", "", "", conn);//This transaction is unconfirmed, Can not Split Record, Please Confirmed Transaction.
				isError = true;
				return retSting;
			}
			//Added by sarita to check transaction is confirmed or not on 14MARCH2019 [END]
			//Added by sarita on 14APR2018 [start] to validate item_code as inv_srno_reqd is 'Y' or 'N'
			else
			{
				sql = "select inv_srno_reqd from item where item_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,itemCodeHdr);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					invSrnoReqd = rs.getString("inv_srno_reqd");
					System.out.println("inv_srno_reqd ::: ["+invSrnoReqd+"]");
				}
				if(rs != null){rs.close();rs = null;}
				if(pstmt != null){pstmt.close();pstmt = null;}
				if("Y".equalsIgnoreCase(invSrnoReqd) == false)
				{
					//retSting = itmDBAccessEJB.getErrorString("", "IVITMSRRQD", "");
					retSting = itmDBAccessEJB.getErrorString("", "IVITMSRRQD", "", "", conn);
					isError = true;
					return retSting;  
				}			
			}
			//Added by sarita on 14APR2018 [end]to validate item_code as inv_srno_reqd is 'Y' or 'N'
			//Showing Error Message Transaction Splitted Already.[end]

			//Commented & Added by sarita to perform validation on location as location should not be GIT Location on 29 OCT 2018 [START]
			//sql = "select count(*) as cnt from stock where ITEM_CODE = ? AND LOT_NO = ? AND LOT_SL = ? AND quantity > 0";
			sql = "select a.available "
					+ "from invstat a , Location b , Stock c "
					+ "where a.inv_stat = b.inv_stat "
					+ "AND b.loc_code = c.loc_code "
					+ "AND c.ITEM_CODE = ? "
					+ "AND c.LOT_NO = ? "
					+ "AND c.LOT_SL = ? "
					+ "AND c.quantity > 0 "
					+ "group by a.available ,c.loc_code,c.item_code,c.lot_no,c.lot_sl,c.quantity";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, itemCodeHdr);
			pstmt.setString(2, lotNoHdr);
			pstmt.setString(3, lotSlHdr);
			rs = pstmt.executeQuery();
			//Added by sarita to store data into ArrayList on 02 NOV 2018 [START]
			//if(rs.next())
			while(rs.next())
			{
				stkCnt++;
				//stockMap = new HashMap<>();//Commented by sarita as it was not working in JDK1.6 on 14 NOV 18
				stockMap = new HashMap();
				avalibleFrLocCode = rs.getString("available");					
				/*stockDataList.add(stkCnt);
				stockDataList.add(avalibleFrLocCode);*/
				stockMap.put("count", stkCnt);
				stockMap.put("available", avalibleFrLocCode);
				stockDataList.add(stockMap);
				//Added by sarita to store data into ArrayList on 02 NOV 2018 [END]
			}
			System.out.println("[SRLContainerSplit] Count is ["+cnt+"] && Available in invstat is ["+avalibleFrLocCode+"]");
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
			//changes by sarita on 18MARCH2018
			//if(cnt == 0)  -- Commented and Added by sarita on 02 NOV 2018
			if(stockDataList.isEmpty() == true)
			{
				//retSting = itmDBAccessEJB.getErrorString("", "VTNOSTK", "");//itmDBAccessEJB.getErrorString("","VTBLNKDTL","","",conn);
				retSting = itmDBAccessEJB.getErrorString("", "VTNOSTK", "", "", conn);
				//Added by sarita on 19MARCH2018
				isError = true;
				return retSting;

			}
			//if(cnt > 1) -- Commented and Added by sarita on 02 NOV 2018
			if(stockDataList.size() > 1)
			{
				//retSting = itmDBAccessEJB.getErrorString("", "INVRCDSTOC", "");//itmDBAccessEJB.getErrorString("","VTBLNKDTL","","",conn);
				retSting = itmDBAccessEJB.getErrorString("", "INVRCDSTOC", "", "", conn);
				//Added by sarita on 19MARCH2018
				isError = true;
				return retSting;
			}
			// Commented and Added by sarita on 02 NOV 2018 [START]
			/*else if("N".equalsIgnoreCase(avalibleFrLocCode))
			{
				System.out.println("COUNT if available is N ["+cnt+"]");
				retSting = itmDBAccessEJB.getErrorString("", "VTINVGIT", "", "", conn);//Please check the GIT Location Code, it does not exists or not for internal use.
				isError = true;
				return retSting;
			}*/
			else
			{
				//HashMap newMap = new HashMap<>();//Commented by sarita as it was not working in JDK1.6 on 14 NOV 18
				HashMap newMap = new HashMap();
				newMap = (HashMap) stockDataList.get(0);
				/*stkCnt = (Integer)stockDataList.get(0);
				avalibleFrLocCode = (String)stockDataList.get(1);*/
				//stkCnt = (int) newMap.get("count");//Commented by sarita as it was not working in JDK1.6 on 14 NOV 18
				stkCnt = (Integer) newMap.get("count");
				avalibleFrLocCode = (String) newMap.get("available");
				System.out.println("avalibleFrLocCode ["+avalibleFrLocCode+"]");
				if("N".equalsIgnoreCase(avalibleFrLocCode))
				{
					retSting = itmDBAccessEJB.getErrorString("", "VTINVGIT", "", "", conn);//Please check the GIT Location Code, it does not exists or not for internal use.
					isError = true;
					return retSting;
				}
			}
			// Commented and Added by sarita on 02 NOV 2018 [END]
			//Commented & Added by sarita to perform validation on location as location should not be GIT Location on 29 OCT 2018 [START]


			//changes by sarita (quantity > 0) on 18MARCH 2018			
			sql =     "SELECT SITE_CODE , LOC_CODE , ITEM_SER, QUANTITY, UNIT, GRADE , CREA_DATE , EXP_DATE , LTRAN_DATE , REMARKS , INV_STAT ," +
					"ALLOC_QTY , PACK_CODE , MFG_DATE , SITE_CODE__MFG , POTENCY_PERC , LAST_RCP_DATE , LAST_ISS_DATE , RATE , LAST_PHYC_DATE , " +
					"GROSS_WEIGHT , TARE_WEIGHT , NET_WEIGHT , PACK_INSTR , DIMENSION , SUPP_CODE__MFG , ACCT_CODE__INV , CCTR_CODE__INV , RATE__OH ," +
					"ACCT_CODE__OH , CCTR_CODE__OH , RETEST_DATE , GROSS_RATE , CONV__QTY_STDUOM , UNIT__ALT , BATCH_NO , NO_ART , GROSS_WT_PER_ART ," +
					"TARE_WT_PER_ART , QTY_PER_ART , ACTUAL_RATE , WGHT_RATE , PALLET_WT , PACK_REF , PARTIAL_USED , BATCH_SIZE , LOT_SL__ORG , HOLD_QTY," +
					"CONSIDER_ALLOCATE , PALLET_NO FROM STOCK "
					+ "WHERE ITEM_CODE = ? "
					+ "AND LOT_NO = ? "
					+ "and LOT_SL = ? and quantity > 0";

			pstmtStock = conn.prepareStatement(sql);
			pstmtStock.setString(1,itemCodeHdr);
			pstmtStock.setString(2,lotNoHdr);
			pstmtStock.setString(3,lotSlHdr);
			rsStock = pstmtStock.executeQuery();

			if(rsStock.next())
			{
				siteCode = checkNull(rsStock.getString("site_code"));
				locationCode = checkNull(rsStock.getString("loc_code"));
				itemSer = checkNull(rsStock.getString("item_ser"));
				quantity = rsStock.getDouble("quantity"); 
				unit = checkNull(rsStock.getString("unit"));
				grade = checkNull(rsStock.getString("grade"));
				creaDate = rsStock.getTimestamp("crea_date");
				expDate = rsStock.getTimestamp("exp_date");
				ltranDate = rsStock.getTimestamp("ltran_date");
				remarks = checkNull(rsStock.getString("remarks"));
				invStat = checkNull(rsStock.getString("inv_stat"));
				allocQty = rsStock.getDouble("alloc_qty"); 
				packCode = checkNull(rsStock.getString("pack_code"));
				mfgDate = rsStock.getTimestamp("mfg_date");
				siteCodeMfg = checkNull(rsStock.getString("site_code__mfg"));
				potencyPerc = rsStock.getDouble("potency_perc");
				lastRcpDate = rsStock.getTimestamp("last_rcp_date");
				lastIssDate = rsStock.getTimestamp("last_iss_date");
				stkRate = rsStock.getDouble("rate");
				lastPhyDate = rsStock.getTimestamp("last_phyc_date");
				grossWeight = rsStock.getDouble("gross_weight");
				tare_weight = rsStock.getDouble("tare_weight");
				net_weight = rsStock.getDouble("net_weight");
				packInstr = checkNull(rsStock.getString("pack_instr"));
				dimension = checkNull(rsStock.getString("dimension"));
				suppCodeMfg = checkNull(rsStock.getString("supp_code__mfg"));
				acctCodeDr = checkNull(rsStock.getString("acct_code__inv"));
				cctrCodeDr = checkNull(rsStock.getString("cctr_code__inv"));
				rateOh = rsStock.getDouble("rate__oh");
				acctCodeOh = checkNull(rsStock.getString("acct_code__oh"));
				cctrCodeOh = checkNull(rsStock.getString("cctr_code__oh"));
				restestDate = rsStock.getTimestamp("retest_date");
				stkGrossRate = rsStock.getDouble("gross_rate");
				convQtyStduom = rsStock.getDouble("conv__qty_stduom");
				unitAlt = checkNull(rsStock.getString("unit__alt"));
				batchNo = checkNull(rsStock.getString("batch_no"));
				noArt = rsStock.getDouble("no_art");
				batchSize = rsStock.getDouble("batch_size");
				holdQuantity = rsStock.getDouble("hold_qty");
				actualRate = rsStock.getDouble("actual_rate");
				considerAllocate = checkNull(rsStock.getString("consider_allocate"));
				System.out.println("Value of Quantity is :: ["+quantity+"]");

				if(!issueCondition && !(quantity==0))
				{

					int cntISS = 1;

					issueCondition = true;
					valueXmlforIssue.append("<?xml version='1.0' encoding='UTF-8'?>\n");
					valueXmlforIssue.append("<Root>\n");
					valueXmlforIssue.append("<DocumentRoot>\n");
					valueXmlforIssue.append("<description>").append("DatawindowRoot").append("</description>\n");
					valueXmlforIssue.append("<group0>\n");
					valueXmlforIssue.append("<description>").append("Group0description").append("</description>\n");
					valueXmlforIssue.append("<Header0>\n");
					valueXmlforIssue.append("<objName><![CDATA[").append("adj_iss").append("]]></objName>\n");
					valueXmlforIssue.append("<pageContext><![CDATA[").append("1").append("]]></pageContext>\n");
					valueXmlforIssue.append("<objContext><![CDATA[").append("1").append("]]></objContext>\n");
					valueXmlforIssue.append("<editFlag><![CDATA[").append("A").append("]]></editFlag>\n");
					valueXmlforIssue.append("<focusedColumn><![CDATA[").append("").append("]]></focusedColumn>\n");
					valueXmlforIssue.append("<action><![CDATA[").append("SAVE").append("]]></action>\n");
					valueXmlforIssue.append("<elementName><![CDATA[").append("").append("]]></elementName>\n");
					valueXmlforIssue.append("<keyValue><![CDATA[").append("1").append("]]></keyValue>\n");
					valueXmlforIssue.append("<pkValues><![CDATA[").append("").append("]]></pkValues>\n");
					valueXmlforIssue.append("<taxKeyValue><![CDATA[").append("").append("]]></taxKeyValue>\n");
					valueXmlforIssue.append("<saveLevel><![CDATA[").append("1").append("]]></saveLevel>\n");
					valueXmlforIssue.append("<forcedSave><![CDATA[").append(false).append("]]></forcedSave>\n");
					valueXmlforIssue.append("<taxInFocus><![CDATA[").append(false).append("]]></taxInFocus>\n");
					valueXmlforIssue.append("<Detail1 dbID=\"\" domID=\"1\" objContext=\"1\" objName=\"adj_iss\">\n");
					valueXmlforIssue.append("<attribute pkNames=\"\" selected=\"N\" status=\"O\" updateFlag=\"A\"/>\n");
					valueXmlforIssue.append("<tran_id/>\n");
					//valueXmlforIssue.append("<tran_date><![CDATA[").append((ltranDate==null)?"":simpleDateFormat.format(ltranDate)).append("]]></tran_date>\n");
					valueXmlforIssue.append("<tran_date><![CDATA[").append((tranSerialDate==null)?"":simpleDateFormat.format(tranSerialDate)).append("]]></tran_date>\n");
					valueXmlforIssue.append("<eff_date><![CDATA[").append((tranSerialDate==null)?"":simpleDateFormat.format(tranSerialDate)).append("]]></eff_date>\n");
					//valueXmlforIssue.append("<eff_date><![CDATA[").append(simpleDateFormat.format(new Date())).append("]]></eff_date>\n");
					valueXmlforIssue.append("<ref_ser><![CDATA[").append("ADJISS").append("]]></ref_ser>\n");
					valueXmlforIssue.append("<order_id><![CDATA[").append("").append("]]></order_id>\n");
					valueXmlforIssue.append("<site_code><![CDATA[").append(siteCode).append("]]></site_code>\n");
					valueXmlforIssue.append("<ref_date/>\n");
					valueXmlforIssue.append("<ref_ser__for/>\n");
					valueXmlforIssue.append("<ref_id__for/>\n");
					valueXmlforIssue.append("<item_ser><![CDATA[").append(itemSer).append("]]></item_ser>\n");
					valueXmlforIssue.append("<remarks><![CDATA[").append(tranId).append("]]></remarks>\n");
					valueXmlforIssue.append("<confirmed><![CDATA[N]]></confirmed>");
					valueXmlforIssue.append("<reas_code><![CDATA[").append("AIDSC").append("]]></reas_code>");
					/*valueXmlforIssue.append("<reas_code><![CDATA[").append("ARSLR").append("]]></reas_code>");*/
					valueXmlforIssue.append("<conf_date/>\n");
					/*valueXmlforIssue.append("<chg_date><![CDATA[").append("").append("]]></chg_date>");
						valueXmlforIssue.append("<chg_user><![CDATA[").append("").append("]]></chg_user>");
						valueXmlforIssue.append("<chg_term><![CDATA[").append("").append("]]></chg_term>");*/
					valueXmlforIssue.append("<emp_code__aprv/>\n");
					valueXmlforIssue.append("<price_list/>\n");
					valueXmlforIssue.append("</Detail1>\n");
					System.out.println("ISSUE HEADER GENERATED");

					double amount = stkRate * quantity;
					valueXmlforIssue.append("<Detail2 dbID=\"\" domID=\""+cntISS+"\" objContext=\"2\" objName=\"adj_iss\">\n");
					valueXmlforIssue.append("<attribute pkNames=\"\" selected=\"N\" status=\"O\" updateFlag=\"A\"/>\n");
					valueXmlforIssue.append("<tran_id/>\n");
					valueXmlforIssue.append("<line_no><![CDATA[").append(Integer.toString(cntISS)).append("]]></line_no>\n");
					valueXmlforIssue.append("<item_code><![CDATA[").append(itemCodeHdr).append("]]></item_code>\n");
					valueXmlforIssue.append("<unit><![CDATA[").append(unit).append("]]></unit>\n");
					valueXmlforIssue.append("<loc_code><![CDATA[").append(locationCode).append("]]></loc_code>\n");
					valueXmlforIssue.append("<lot_no><![CDATA[").append(lotNoHdr).append("]]></lot_no>\n");
					valueXmlforIssue.append("<lot_sl><![CDATA[").append(lotSlHdr).append("]]></lot_sl>\n");
					valueXmlforIssue.append("<quantity><![CDATA[").append(quantity).append("]]></quantity>\n");
					valueXmlforIssue.append("<sundry_type/>\n");
					valueXmlforIssue.append("<sundry_code/>\n");
					valueXmlforIssue.append("<rate><![CDATA[").append(stkRate).append("]]></rate>\n");
					valueXmlforIssue.append("<gross_rate><![CDATA[").append(stkGrossRate).append("]]></gross_rate>\n");
					valueXmlforIssue.append("<grade><![CDATA[").append(grade).append("]]></grade>\n");
					valueXmlforIssue.append("<dimension><![CDATA[").append(dimension).append("]]></dimension>\n");
					valueXmlforIssue.append("<no_art><![CDATA[").append(noArt).append("]]></no_art>\n");
					valueXmlforIssue.append("<amount><![CDATA[").append(String.valueOf(amount)).append("]]></amount>\n");
					valueXmlforIssue.append("<gross_weight><![CDATA[").append(grossWeight).append("]]></gross_weight>\n");
					valueXmlforIssue.append("<tare_weight><![CDATA[").append(tare_weight).append("]]></tare_weight>\n");
					valueXmlforIssue.append("<net_weight><![CDATA[").append(net_weight).append("]]></net_weight>\n");
					valueXmlforIssue.append("<acct_code__dr><![CDATA[").append(finCommon.getFinparams("999999","ACCT_CODE_ISS_RCP",conn)).append("]]></acct_code__dr>\n");
					valueXmlforIssue.append("<cctr_code__dr><![CDATA[").append(finCommon.getFinparams("999999","COST_CEN_ISS_RCP",conn)).append("]]></cctr_code__dr>\n");
					valueXmlforIssue.append("<acct_code__cr><![CDATA[").append(acctCodeDr).append("]]></acct_code__cr>\n");
					valueXmlforIssue.append("<cctr_code__cr><![CDATA[").append(cctrCodeDr).append("]]></cctr_code__cr>\n");
					valueXmlforIssue.append("<potency_perc><![CDATA[").append(potencyPerc).append("]]></potency_perc>\n");
					valueXmlforIssue.append("<pack_code><![CDATA[").append(packCode).append("]]></pack_code>\n");
					valueXmlforIssue.append("<mfg_date><![CDATA[").append("").append("]]></mfg_date>\n");
					valueXmlforIssue.append("<exp_date><![CDATA[").append("").append("]]></exp_date>\n");
					valueXmlforIssue.append("<site_code__mfg><![CDATA[").append(siteCodeMfg).append("]]></site_code__mfg>\n");
					valueXmlforIssue.append("<conv__qty_stduom><![CDATA[").append(convQtyStduom).append("]]></conv__qty_stduom>\n");
					valueXmlforIssue.append("<unit__alt><![CDATA[").append(unitAlt).append("]]></unit__alt>\n");
					valueXmlforIssue.append("</Detail2>");
				}
				else
				{
					//retSting = itmDBAccessEJB.getErrorString("", "INVQUNTITY", "");//itmDBAccessEJB.getErrorString("","VTBLNKDTL","","",conn);
					retSting = itmDBAccessEJB.getErrorString("", "INVQUNTITY", "", "", conn);
					//Added by sarita on 19MARCH2018
					isError = true;
					return retSting;
				}
			}//if(rsStock.next())
			//Added by Anjali R. on 05 NOV 2018
			if(rsStock != null)
			{
				rsStock.close();
				rsStock = null;
			}
			if(pstmtStock != null)
			{
				pstmtStock.close();
				pstmtStock = null;
			}
			//Added by Anjali R. on 05 NOV 2018


			valueXmlforIssue.append("</Header0>\n");
			valueXmlforIssue.append("</group0>\n");
			valueXmlforIssue.append("</DocumentRoot>\n");
			valueXmlforIssue.append("</Root>\n");

			if(!receiptCondition)
			{
				receiptCondition = true;
				valueXmlforReceipt.append("<?xml version='1.0' encoding='UTF-8'?>\n");
				valueXmlforReceipt.append("<Root>\n");
				valueXmlforReceipt.append("<DocumentRoot>\n");
				valueXmlforReceipt.append("<description>").append("DatawindowRoot").append("</description>\n");
				valueXmlforReceipt.append("<group0>\n");
				valueXmlforReceipt.append("<description>").append("Group0description").append("</description>\n");
				valueXmlforReceipt.append("<Header0>\n");
				valueXmlforReceipt.append("<objName><![CDATA[").append("adj_rcp").append("]]></objName>\n");
				valueXmlforReceipt.append("<pageContext><![CDATA[").append("1").append("]]></pageContext>\n");
				valueXmlforReceipt.append("<objContext><![CDATA[").append("1").append("]]></objContext>\n");
				valueXmlforReceipt.append("<editFlag><![CDATA[").append("A").append("]]></editFlag>\n");
				valueXmlforReceipt.append("<focusedColumn><![CDATA[").append("").append("]]></focusedColumn>\n");
				valueXmlforReceipt.append("<action><![CDATA[").append("SAVE").append("]]></action>\n");
				valueXmlforReceipt.append("<elementName><![CDATA[").append("").append("]]></elementName>\n");
				valueXmlforReceipt.append("<keyValue><![CDATA[").append("1").append("]]></keyValue>\n");
				valueXmlforReceipt.append("<pkValues><![CDATA[").append("").append("]]></pkValues>\n");
				valueXmlforReceipt.append("<taxKeyValue><![CDATA[").append("").append("]]></taxKeyValue>\n");
				valueXmlforReceipt.append("<saveLevel><![CDATA[").append("1").append("]]></saveLevel>\n");
				valueXmlforReceipt.append("<forcedSave><![CDATA[").append(false).append("]]></forcedSave>\n");
				valueXmlforReceipt.append("<taxInFocus><![CDATA[").append(false).append("]]></taxInFocus>\n");
				valueXmlforReceipt.append("<Detail1 dbID=\"\" domID=\"1\" objContext=\"1\" objName=\"adj_rcp\">\n");
				valueXmlforReceipt.append("<attribute pkNames=\"\" selected=\"N\" status=\"N\" updateFlag=\"A\"/>\n");
				valueXmlforReceipt.append("<tran_id/>\n");
				//valueXmlforReceipt.append("<tran_date><![CDATA[").append((ltranDate==null)?"":simpleDateFormat.format(ltranDate)).append("]]></tran_date>\n");
				valueXmlforReceipt.append("<tran_date><![CDATA[").append((tranSerialDate==null)?"":simpleDateFormat.format(tranSerialDate)).append("]]></tran_date>\n");
				//valueXmlforReceipt.append("<eff_date><![CDATA[").append(simpleDateFormat.format(new Date())).append("]]></eff_date>\n");
				valueXmlforReceipt.append("<eff_date><![CDATA[").append((tranSerialDate==null)?"":simpleDateFormat.format(tranSerialDate)).append("]]></eff_date>\n");
				valueXmlforReceipt.append("<ref_ser><![CDATA[ADJRCP]]></ref_ser>\n");
				valueXmlforReceipt.append("<order_id><![CDATA[").append("").append("]]></order_id>\n");
				valueXmlforReceipt.append("<site_code><![CDATA[").append(siteCode).append("]]></site_code>\n");
				valueXmlforReceipt.append("<ref_date/>\n");
				valueXmlforReceipt.append("<ref_ser__for/>\n");
				valueXmlforReceipt.append("<ref_id__for/>\n");
				valueXmlforReceipt.append("<item_ser><![CDATA[").append(itemSer).append("]]></item_ser>\n");
				valueXmlforReceipt.append("<remarks><![CDATA[").append(tranId).append("]]></remarks>\n");
				valueXmlforReceipt.append("<confirmed><![CDATA[N]]></confirmed>\n");
				/*valueXmlforReceipt.append("<reas_code><![CDATA[").append("ARSLR").append("]]></reas_code>");*/
				valueXmlforReceipt.append("<reas_code><![CDATA[").append("PRRCP").append("]]></reas_code>");
				valueXmlforReceipt.append("<conf_date/>\n");
				valueXmlforReceipt.append("<chg_date><![CDATA[").append((tranSerialDate==null)?"":simpleDateFormat.format(tranSerialDate)).append("]]></chg_date>\n");
				valueXmlforReceipt.append("<chg_user><![CDATA[").append(userId).append("]]></chg_user>\n");
				valueXmlforReceipt.append("<chg_term><![CDATA[").append(chgTerm).append("]]></chg_term>\n");
				valueXmlforReceipt.append("<emp_code__aprv/>\n");
				valueXmlforReceipt.append("<price_list/>\n");
				valueXmlforReceipt.append("</Detail1>\n");
				System.out.println("RECIEPT HEADER GENERATED");					
			}
			/*valueXmlforIssue.append("</Header0>");
				valueXmlforIssue.append("</group0>");
				valueXmlforIssue.append("</DocumentRoot>");
				valueXmlforIssue.append("</Root>");*/

			int cntRCP =1;

			sql = "select item_code , lot_no , lot_sl, quantity from srl_contents where serial_no=?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				itemCodeDet = rs.getString("item_code");
				lotNodet = rs.getString("lot_no");
				lotSlDet = rs.getString("lot_sl");
				splitQty = rs.getDouble("quantity");
				System.out.println("For Header Data ::::[item_code ["+itemCodeHdr+"]] \t [lot_no ["+lotNoHdr+"]] \t [lot_sl ["+lotSlHdr+"]]");

				valueXmlforReceipt.append("<Detail2 dbID=\"\" domID=\""+cntRCP+"\" objContext=\"2\" objName=\"adj_rcp\">\n");
				valueXmlforReceipt.append("<attribute pkNames=\"\" selected=\"N\" status=\"N\" updateFlag=\"A\"/>\n");
				valueXmlforReceipt.append("<tran_id/>\n");
				valueXmlforReceipt.append("<line_no><![CDATA[").append(cntRCP).append("]]></line_no>\n");
				valueXmlforReceipt.append("<item_code><![CDATA[").append(itemCodeDet).append("]]></item_code>\n");
				valueXmlforReceipt.append("<unit><![CDATA[").append(unit).append("]]></unit>\n");
				valueXmlforReceipt.append("<loc_code><![CDATA[").append(locationCode).append("]]></loc_code>\n");
				valueXmlforReceipt.append("<lot_no><![CDATA[").append(lotNodet).append("]]></lot_no>\n");
				valueXmlforReceipt.append("<lot_sl><![CDATA[").append(lotSlDet).append("]]></lot_sl>\n");
				valueXmlforReceipt.append("<quantity><![CDATA[").append(String.valueOf(Math.abs(splitQty))).append("]]></quantity>\n");
				valueXmlforReceipt.append("<sundry_type/>\n");
				valueXmlforReceipt.append("<sundry_code/>\n");
				valueXmlforReceipt.append("<rate><![CDATA[").append(stkRate).append("]]></rate>\n");
				valueXmlforReceipt.append("<gross_rate><![CDATA[").append(stkGrossRate).append("]]></gross_rate>\n");
				valueXmlforReceipt.append("<grade/>\n");
				valueXmlforReceipt.append("<dimension/>\n");
				valueXmlforReceipt.append("<no_art><![CDATA[").append(noArt).append("]]></no_art>\n");
				valueXmlforReceipt.append("<amount/>\n");
				valueXmlforReceipt.append("<gross_weight><![CDATA[").append("0").append("]]></gross_weight>\n");
				valueXmlforReceipt.append("<tare_weight><![CDATA[").append("0").append("]]></tare_weight>\n");
				valueXmlforReceipt.append("<net_weight><![CDATA[").append("0").append("]]></net_weight>\n");
				//String[] acctCctrDetrValue = finCommon.getFromAcctDetr(itemCodeDet,itemSer,"STKINV",conn).split(","); commented and added new getAcctDetrTtype() method  by nandkuma gadkari on 11/11/19
				String[] acctCctrDetrValue = finCommon.getAcctDetrTtype(itemCodeDet,itemSer,"STKINV"," ",conn).split(","); 
				valueXmlforReceipt.append("<acct_code__dr><![CDATA[").append(acctCctrDetrValue[0]).append("]]></acct_code__dr>\n");
				valueXmlforReceipt.append("<cctr_code__dr><![CDATA[").append(acctCctrDetrValue[1]).append("]]></cctr_code__dr>\n");
				valueXmlforReceipt.append("<acct_code__cr><![CDATA[").append(finCommon.getFinparams("999999","ACCT_CODE_ISS_RCP",conn)).append("]]></acct_code__cr>\n");
				valueXmlforReceipt.append("<cctr_code__cr><![CDATA[").append(finCommon.getFinparams("999999","COST_CEN_ISS_RCP",conn)).append("]]></cctr_code__cr>\n");
				valueXmlforReceipt.append("<potency_perc><![CDATA[").append(potencyPerc).append("]]></potency_perc>");
				valueXmlforReceipt.append("<pack_code><![CDATA[").append(packCode).append("]]></pack_code>");
				valueXmlforReceipt.append("<mfg_date><![CDATA[]]></mfg_date>\n");
				valueXmlforReceipt.append("<exp_date><![CDATA[]]></exp_date>\n");
				valueXmlforReceipt.append("<site_code__mfg><![CDATA[").append(siteCodeMfg).append("]]></site_code__mfg>\n");
				valueXmlforReceipt.append("<conv__qty_stduom><![CDATA[").append(convQtyStduom).append("]]></conv__qty_stduom>\n");
				valueXmlforReceipt.append("<unit__alt><![CDATA[").append(unit).append("]]></unit__alt>\n");				
				valueXmlforReceipt.append("</Detail2>\n");
				cntRCP++;
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
			valueXmlforReceipt.append("</Header0>");
			valueXmlforReceipt.append("</group0>");
			valueXmlforReceipt.append("</DocumentRoot>");
			valueXmlforReceipt.append("</Root>");

			System.out.println("ReturnString Finally :::::::["+valueXmlforIssue+"]\n [valueXmlforReceipt ["+valueXmlforReceipt+"]]");
			//retSting = saveData(siteCode,valueXmlforIssue.toString(),conn,userId);


			//Else block will execute if Transaction is Not Splitted [start]
			if(!("C".equalsIgnoreCase(statusSrl)))
			{
				//flagSaveString = "";
				//if(issueCondition && flagSaveString.equals(""))
				if(issueCondition && (isError == false))
				{
					System.out.println("Saving Data for Issue");
					retSting = saveData(siteCode,valueXmlforIssue.toString(),conn,userId);
					System.out.println("Data Saved Successfully for Issue");
					if(retSting.indexOf("Success") > -1)
					{
						String[] arrayForTranId = retSting.split("<TranID>");
						int endIndex = arrayForTranId[1].indexOf("</TranID>");
						tranIdForIssue = arrayForTranId[1].substring(0,endIndex);
						System.out.println("tranIdForIssue ["+tranIdForIssue+"]");
						//Commented by sarita on 19MARCH2018
						//conn.commit();
						System.out.println("Before ::[retSting (confirmIssueAndReceipt)]"+userId);
						retSting = confirmIssueAndReceipt("adj_iss",tranIdForIssue,xtraParams,forcedFlag,conn,userId);
						System.out.println("ReturnString[Saving Data for Issue] ::"+retSting);
						if(retSting.indexOf("VTSUCC1") > -1 || retSting.indexOf("VTMCONF2") > -1)
						{
							//Getting Successfull Confirm message (VTMCONF2 - Prompt Message)
							System.out.println("Inside If Block of returnstring!!!!!!!!!!");
							retSting = "";
						}
						else 
						{
							System.out.println("Inside Else Block of returnstring!!!!!!!!!!");
							//flagSaveString = retSting;
							//Added by sarita to rollback connection
							isError = true;
						}
					}
					else 
					{
						//flagSaveString = retSting;
						System.out.println("Transaction rollbacking for Issue>>>>>>>>>");
						isError = true;
					}
				}

				//if(receiptCondition && flagSaveString.equals(""))
				if(receiptCondition && (isError == false))
				{
					System.out.println("Saving Data for Receipt");
					//Commented and Added by sarit aon 02 NOV 2018 [START]
					//retSting = saveData(siteCode,valueXmlforReceipt.toString(),conn,userId);
					//Modified by Anjali.Start on 05 NOV 2018
					//String sql1 = "SELECT REF_SER, AUTO_CONFIRM, TAX_FORMS, WRKFLW_INIT, WORKFLOW_OPT FROM TRANSETUP WHERE TRAN_WINDOW = 'w_adj_rcp'" ;
					String sql1 = "SELECT REF_SER,  WRKFLW_INIT, WORKFLOW_OPT FROM TRANSETUP WHERE TRAN_WINDOW = 'w_adj_rcp'" ;
					//Modified by Anjali.End on 05 NOV 2018
					pstmt = conn.prepareStatement(sql1);
					rs = pstmt.executeQuery();
					if( rs.next() )
					{						
						refSer = checkNull(rs.getString( "REF_SER" ));
						//autoConfirm = checkNull(rs.getString( "AUTO_CONFIRM" ));//Commented by anjali
						//taxForm = checkNull(rs.getString( "TAX_FORMS" ));//Commented by anjali
						wrkflwInit = checkNull(rs.getString( "WRKFLW_INIT" ));
						wrkflwOpt = checkNull(rs.getString( "WORKFLOW_OPT" ));
					}
					if ( pstmt != null )
					{
						pstmt.close();
						pstmt = null;
					}
					if ( rs != null )
					{
						rs.close();
						rs = null;
					}
					CommonDBAccessRemote dbAccessRemote = new CommonDBAccessEJB();
					userInfo = dbAccessRemote.createUserInfo(userId);

					//HashMap totFormsMap = getTransInfo(conn);

					formCnt = getTransInfo("w_adj_rcp",conn);

					/*if(totForms != null && totForms.trim().length() > 0)
					{
						formCnt = Integer.parseInt(totForms);
					}*/
					System.out.println("Form Count is == ["+formCnt+"]");
					retSting = xml2DBLocal.saveXML2DB(refSer,valueXmlforReceipt.toString(),"adj_rcp", "A", formCnt, true, userId, "tran_id",false,xtraParams,null,wrkflwInit,isWrkflwToBeInitiated,wrkflwOpt,null,"","",false, conn, false, userInfo);
					//transactionID = retSting;

					System.out.println("Data Saved Successfully for Receipt" +retSting  );
					//if(retSting.indexOf("Success") > -1)
					if(retSting.indexOf( "<Errors>" ) == -1 )
					{
						//Added and Commented by sarita on 02 NOV 2018 [START]
						/*String[] arrayForTranId = retSting.split("<TranID>");
						int endIndex = arrayForTranId[1].indexOf("</TranID>");
						tranIdForReciept = arrayForTranId[1].substring(0,endIndex); 
						System.out.println("tranIdForReciept["+tranIdForReciept+"]");
						//Commented by sarita on 19MARCH2018
						//conn.commit();*/
						tranIdForReciept = retSting;
						//Added and Commented by sarita on 02 NOV 2018 [END]
						retSting = confirmIssueAndReceipt("adj_rcp",tranIdForReciept,xtraParams,forcedFlag,conn,userId);						
						System.out.println("ReturnString[Saving Data for Issue] ::"+retSting);
						if(retSting.indexOf("VTSUCC1") > -1 || retSting.indexOf("VTMCONF2") > -1)
						{
							//Getting Successfull Confirm message (VTMCONF2 - Prompt Message)
							retSting = "";
						}
						else
						{
							System.out.println("Getting Error in Saving Data for Receipt");
							//flagSaveString = retSting;
							//Added by sarita to rollback connection
							isError = true;
						}
					}
					else 
					{
						//flagSaveString = retSting;
						System.out.println("Transaction rollbacking for Receipt>>>>>>>>>>");
						isError = true;
					}			
				}

				System.out.println("Receipt "+tranIdForReciept);

				//if(tranIdForReciept.length() > 0 && flagSaveString == "")
				if(tranIdForReciept.length() > 0 && (isError == false))
				{
					//changes by sarita on 18MARCH2018
					sql = "update srl_container set status ='C' where serial_no=?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, tranId);
					int j = pstmt.executeUpdate();
					//conn.commit();
					if(pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}

					if(j > 0)
					{
						//Commented by sarita on 14APR2018 [start] -- removed column status from srl_contents table
						/*sql = "update srl_contents set status ='C' where serial_no=?";
				    	  pstmt = conn.prepareStatement(sql);
				    	  pstmt.setString(1, tranId);
				    	  int i = pstmt.executeUpdate();
				    	  //conn.commit();
				    	  if(pstmt != null)
						  {
						  	pstmt.close();
						  	pstmt = null;
						  }				
				    	  System.out.println("Update ctr "+i); 
				    	  if(i > 0)
				    	  {
				    	  	//Prompt messages showing transaction Splitted Successfullly!!!!!
				    		retSting = itmDBAccessEJB.getErrorString("", "VTAUTOSPLT", "");//itmDBAccessEJB.getErrorString("","VTBLNKDTL","","",conn);
				    		return retSting;
				    	  }
				          else
				    	  {
			    		  	//Error messages showing status not updated!!!!!
				    		retSting = itmDBAccessEJB.getErrorString("", "VTREORG5", "");//itmDBAccessEJB.getErrorString("","VTBLNKDTL","","",conn);
				    		//Added by sarita to rollback connection
							isError = true;
				    		return retSting;
				    	  }*/
						//Commented by sarita on 14APR2018 [end] -- removed column status from srl_contents table
						//Prompt messages showing transaction Splitted Successfullly!!!!!
						//retSting = itmDBAccessEJB.getErrorString("", "VTAUTOSPLT", "");//itmDBAccessEJB.getErrorString("","VTBLNKDTL","","",conn);
						//Modified by Anjali .S
						/*retSting = itmDBAccessEJB.getErrorString("", "VTAUTOSPLT", "", "", conn);
						return retSting;*/
						retSting = "";
						//Modified by Anjali .E
					}
					else
					{
						//Error messages showing status not updated!!!!!
						//retSting = itmDBAccessEJB.getErrorString("", "VTREORG5", "");//itmDBAccessEJB.getErrorString("","VTBLNKDTL","","",conn);
						retSting = itmDBAccessEJB.getErrorString("", "VTREORG5", "", "", conn);
						//Added by sarita to rollback connection
						isError = true;
						return retSting;
					}
				}				    	
			}
			//Else block will execute if Transaction is Not Splitted [start]
			//}//if(rsStock.next())
		}//end of try block
		catch(Exception ex)
		{
			isError = true;
			System.out.println("Exception Inside [SRLContainerSplit [confirm] Method] ::["+ex+"]");
			ex.printStackTrace();
			throw new ITMException(ex);			
		}
		finally
		{
			try
			{
				if(isError)
				{
					conn.rollback();
					System.out.println("SRLContainerSplit connection rollback");
				}
				else
				{
					//Modified by Anjali R. [Ideally connection commit will not happend here.Connection was not created in this method seems this method will not be commited the transaction]
					//conn.commit();
					System.out.println("SRLContainerSplit connection committed");
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
				//Added by Anjali R. S
				if(rsStock != null)
				{
					rsStock.close();
					rsStock = null;
				}
				if(pstmtStock != null)
				{
					pstmtStock.close();
					pstmtStock = null;
				}
				
				/*if(conn != null)
				{
					conn.close();
					conn = null;
				}*/
				//Added by Anjali R. E
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		System.out.println("[retSting :: ["+retSting+"]]");
		return retSting;
	}

	private String checkNull(String str)
	{
		if(str == null)
		{
			return "";
		}
		else
		{
			return str.trim();
		}
	}

	public String saveData(String siteCode,String xmlString, Connection conn,String userId) throws ITMException
	{
		System.out.println("saving data...........");
		InitialContext ctx = null;
		String retString = null;
		MasterStatefulLocal masterStateful = null; // for ejb3
		try
		{			
			AppConnectParm appConnect = new AppConnectParm();
			ctx = new InitialContext(appConnect.getProperty());
			masterStateful = (MasterStatefulLocal)ctx.lookup("ibase/MasterStatefulEJB/local");
			String [] authencate = new String[2];
			authencate[0] = userId;
			authencate[1] = "";
			System.out.println("xmlString :::: " + xmlString+"END END END");
			retString = masterStateful.processRequest(authencate, siteCode, true, xmlString,true,conn);
			masterStateful = null;
			System.out.println("masterStateful.processRequest(authencate, siteCode, true, xmlString,true,conn) >>>>> " + retString+" shamim");
		}
		catch(ITMException itme)
		{
			System.out.println("ITMException :Physical Inv Fount : saveData :==>" + itme);
			throw new ITMException(itme);
		}
		catch(Exception e)
		{
			System.out.println("Exception :Physical Inv Fount : saveData :==>");
			throw new ITMException(e);
		}
		return retString;
	}

	public String confirmIssueAndReceipt(String businessObj, String tranIdFr,String xtraParams, String forcedFlag, Connection conn,String userId) throws ITMException
	{
		//Commented by Anjali R. Start[unused variables]
		/*String methodName = "";
		String compName = "";
		String retString = "";
		String serviceCode = "";
		String serviceURI = "";
		String actionURI = "";
		String sql = "";*/
		//Commented by Anjali R. End[unused variables]
		String retString = "";
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		System.out.println("confirmIssueAndReceipt(String businessObj, String tranIdFr,String xtraParams, String forcedFlag, Connection conn) called >>><!@#>");
		try
		{
			CommonDBAccessEJB dbAccessRemote = new CommonDBAccessEJB();
			UserInfoBean userInfo = dbAccessRemote.createUserInfo(userId); 

			AdjIssueRcpConf adjIssueRcpConfLocal = new AdjIssueRcpConf(); 
			adjIssueRcpConfLocal.setUserInfo(userInfo);
			retString = adjIssueRcpConfLocal.confirm(tranIdFr, xtraParams, forcedFlag,conn);
			adjIssueRcpConfLocal = null;
			userInfo = null;
			System.out.println("Transaction Confirmed Successfully!!!!!!!!!!");

		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{

				if (rs !=null)
				{
					rs.close();
					rs=null;
				} 
				if (pStmt != null )
				{
					pStmt.close();
					pStmt = null;
				}		
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return retString;
	}

	//Added by sarita method to get Total Forms Information on 02 NOV 2018 [START]
	public Integer getTransInfo(String winName , Connection conn) throws ITMException
	{
		PreparedStatement pstmtObj = null;
		ResultSet rsObj = null;
		String totalForms = "";	
		int totForms = 0;
		//HashMap objMap = new HashMap();
		try
		{
			//String winName = "w_adj_rcp";
			String objFormSql = "SELECT COUNT(*) AS COUNT FROM OBJ_FORMS WHERE WIN_NAME = ?";
			pstmtObj =  conn.prepareStatement(objFormSql);
			pstmtObj.setString(1,winName);
			rsObj = pstmtObj.executeQuery();
			if ( rsObj.next())
			{
				totalForms = checkNull(rsObj.getString("COUNT"));
			}
			if(rsObj != null)
			{
				rsObj.close();
				rsObj = null;
			}
			if(pstmtObj != null)
			{
				pstmtObj.close();
				pstmtObj = null;
			}
			if(totalForms == null || totalForms.trim().length() == 0)
			{
				totForms = 0;
			}
			else
			{
				try
				{
					totForms = Integer.parseInt(totalForms);
				}
				catch(Exception e)
				{
					System.err.println("getTransInfo--["+e.getMessage()+"]");
					e.printStackTrace();
					totForms = 0;
				}
			}
			//objMap.put("TOTAL_FORMS", ""+totalForms); System.out.println("objMap ::"+objMap);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(rsObj != null)
				{
					rsObj.close();
					rsObj = null;
				}
				if(pstmtObj != null)
				{
					pstmtObj.close();
					pstmtObj = null;
				}
			}
			catch(Exception e1)
			{
				e1.printStackTrace();
				throw new ITMException(e1);
			}
		}
		System.out.println("totForms--["+totForms+"]");
		return totForms ;
	}
	//Added by sarita method to get Total Forms Information on 02 NOV 2018  [END]
}