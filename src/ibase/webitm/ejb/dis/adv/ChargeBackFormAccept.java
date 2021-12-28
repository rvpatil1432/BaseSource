/**
 * Developed by Ritesh On 07/02/14 
 * Purpose: Upload charge back data (req : DI3FSUN023)
 * */

package ibase.webitm.ejb.dis.adv;

import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.*;
import ibase.webitm.ejb.*;
import ibase.system.config.*;

import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.rmi.*;
import java.util.*;

import org.w3c.dom.*;

import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class ChargeBackFormAccept extends ActionHandlerEJB implements ChargeBackFormVerifyLocal, ChargeBackFormVerifyRemote 
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	static
	{
		System.out.println(" ChargeBackFormAccept getch()");
	}
	public String actionHandler() throws RemoteException,ITMException
	{
		System.out.println(" nO ARGS FOUND ::");
		return "";
	}

	public String actionHandler(String tranId, String xtraParams, String forcedFlag) throws RemoteException, ITMException
	{
		String returnString=null;

		System.out.println(".......tranId......."+tranId);
		System.out.println(".......xtraParams..."+xtraParams);
		System.out.println(".......forcedFlag..."+forcedFlag);
		try
		{
			if(tranId!=null && tranId.trim().length()>0)
			{
				returnString = chargeBackAccept(tranId,xtraParams,forcedFlag);
			}
		}
		catch (Exception e)
		{
			System.out.println("Exception ..."+e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		return returnString;
	}	

	private String chargeBackAccept(String tranId,String xtraParams,String forcedFlag) throws RemoteException,ITMException
	{
		System.out.println("chargeBackAccept (1) called........"+tranId);
		double qty=0.0;
		double saleqty=0.0,saleretqty=0.0,unconfclaimed=0.0,confclaimed=0.0;
		String itemCode="",lotNo="",porderNo="",porderDate= "",itemSer= "",rateContr="";
		String errString = null,sql="";
		String siteCode="",custCode="",tranDatestr="",tranType="",verifyFlag="",siteCodeLog="";
		String  custCodeCredit = "",loginSite;
		Connection conn = null;
		ConnDriver connDriver = null;
		PreparedStatement pStmt = null,pStmt1=null;
		ResultSet rs = null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		String childNodeName = "";
		Node parentNode = null;
		NodeList childNodeList = null;
		NodeList parentNodeList = null;
		int parentNodeListLength = 0;
		int childNodeListLength = 0;
		Node childNode = null;
		StringBuffer errCode = new StringBuffer("");
		StringBuffer errCodeDet = null;
		String errCodeString = "";
		ArrayList list = new ArrayList();
		boolean rejectedFlag = false;
		int elements = 0;
		 int lineNo =0 ;
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		int cnf=0,cnt=0,chbHed=0,chbDet=0,upConf=0;
		java.sql.Timestamp tranDateTs = null;
		String prdCode = "",statSal = "";
		double amount=0,netAmt=0,totamt=0,totnetAmt=0;
		Timestamp sysDate = null;
		DecimalFormat df = new DecimalFormat("0.000");
		try
		{
			connDriver = null;
			genericUtility = new ibase.utility.E12GenericUtility();
			itmDBAccessEJB = new ITMDBAccessEJB();
			connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver = null;            			
			conn.setAutoCommit(false);
			//parentNodeList = dom1.getElementsByTagName("Detail1");
			//parentNodeListLength = parentNodeList.getLength(); 
			loginSite =  genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");System.out.println(" loginSite::"+loginSite);
		//	System.out.println("parentNodeListLength------------------->"+parentNodeListLength);
			//for (int selectedRow = 0; selectedRow < parentNodeListLength; selectedRow++)
			//{
				chbHed=0;chbDet=0;
			//	parentNode = parentNodeList.item(selectedRow);
			//	childNodeList = parentNode.getChildNodes();
			//	childNodeListLength = childNodeList.getLength();
			//	System.out.println("childNodeListLength---->>> "+ childNodeListLength);
			//	
				sql = " select  count(*) from charge_back_form where tran_id = ? and confirmed = 'Y'";
				pStmt = conn.prepareStatement(sql);
				pStmt.setString(1, tranId.trim());
				rs = pStmt.executeQuery();
				if(rs.next()) 
				{
					cnf  = rs.getInt(1);
				}if(rs != null)
					rs.close();
				rs = null;
				if(pStmt != null)
					pStmt.close();
				pStmt=null;
				if(cnf > 0)
				{
					errString = itmDBAccessEJB.getErrorString("","VTACCALR   ","","",conn);
				    return errString;
				}
				String varflag  = "";
				sql = " select  VERIFY_FLAG,site_code__log from charge_back_form where tran_id = ? ";
				pStmt = conn.prepareStatement(sql);
				pStmt.setString(1, tranId.trim());
				rs = pStmt.executeQuery();
				if(rs.next()) 
				{
					varflag  = rs.getString(1);
					siteCodeLog  = rs.getString(2);//Added by chandrashekar on 16-12-14
				}if(rs != null)
					rs.close();
				rs = null;
				if(pStmt != null)
					pStmt.close();
				pStmt=null;
				if("R".equalsIgnoreCase(varflag))
				{
					errString = itmDBAccessEJB.getErrorString("","VTREJ2   ","","",conn);
				    return errString;
				}
				else if(varflag == null || varflag.trim().length() == 0)
				{
				
					errString = itmDBAccessEJB.getErrorString("","VTNTVERFY   ","","",conn);
				    return errString;
				}
				Calendar curDate = Calendar.getInstance();
				SimpleDateFormat sdfc = new SimpleDateFormat(genericUtility.getApplDateFormat());
			    String sysDtStr = sdfc.format(curDate.getTime());
			    System.out.println(">>>>>>>Now sysDateStr :=>  " + sysDtStr);	
			    sysDate= Timestamp.valueOf(genericUtility.getValidDateString(sysDtStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
			    System.out.println(">>>>>>>>sysDate:"+sysDate);	
				
				//String chbtranID = generateTranId("w_charge_back",loginSite,conn );
				String chbtranID = generateTranId("w_charge_back",loginSite,siteCodeLog,conn );//added by chandrashekar on 16-12-2014
				System.out.println(" charge_back_tran_id ::"+chbtranID);
				sql = " SELECT TRAN_DATE,CUST_CODE__CREDIT,TRAN_TYPE,REMARKS  ,PRISE_LIST ,CURR_CODE ,EXCH_RATE,AMOUNT ,NET_AMT,NET_AMT__BC,"+
					  "	EMP_CODE__APRV ,CHG_DATE ,CHG_USER,CHG_TERM ,TRAN_ID__CRN,EFF_DATE ,TAX_AMT,PORDER_NO ,PORDER_DATE  ,VENDOR_NO ,"+
					  " INT_VENDOR_NO ,CUST_NAME_BAT ,CUST_PNAME_BAT  ,TEST_PROD ,CUST_CODE__SHIP ,CUST_NAME__SHIP ,CUST_CODE__END,CUST_NAME,CONTRACT_NO ,"+
					  " PRICE_LIST,CUST_CODE_DLV,SITE_CODE__LOG,CLAUM_AMT ,ITEM_SER,PARENT__TRAN_ID ,REV__TRAN ,SPEC_REASON ,DIRECT_INV ,"+
					  " SALE_PERIOD ,SITE_CODE__CR  ,DISCOUNT_INV    ,EMP_CODE,CUST_CODE,SITE_CODE,TYPE   "+
					  "	FROM CHARGE_BACK_FORM  WHERE TRAN_ID = ?  and VERIFY_FLAG IN( 'Y','P') " +
					  " AND (length(error_code) = 0 or error_code is null) ";
				pStmt = conn.prepareStatement(sql);
				pStmt.setString(1, tranId.trim());
				rs = pStmt.executeQuery();
				if(rs.next()) 
				{
					
					System.out.println("CLAUM_AMT>>>>>>>>>>>"+rs.getString("CLAUM_AMT"));
					sql = " INSERT INTO CHARGE_BACK( TRAN_DATE,CUST_CODE__CREDIT,TRAN_TYPE,REMARKS  ,PRISE_LIST ,CURR_CODE ,EXCH_RATE,AMOUNT ,NET_AMT,NET_AMT__BC,"+
							"	EMP_CODE__APRV ,CHG_DATE ,CHG_USER,CHG_TERM ,TRAN_ID__CRN,EFF_DATE ,TAX_AMT,PORDER_NO ,PORDER_DATE  ,VENDOR_NO ,"+
							" INT_VENDOR_NO ,CUST_NAME_BAT ,CUST_PNAME_BAT  ,TEST_PROD ,CUST_CODE__SHIP ,CUST_NAME__SHIP ,CUST_CODE__END,CUST_NAME,CONTRACT_NO ,"+
							  " PRICE_LIST,CUST_CODE_DLV,SITE_CODE__LOG,CLAUM_AMT ,ITEM_SER,PARENT__TRAN_ID ,REV__TRAN ,SPEC_REASON ,DIRECT_INV ,"+
							  " SALE_PERIOD ,SITE_CODE__CR  ,DISCOUNT_INV    ,EMP_CODE,TRAN_ID,CUST_CODE,SITE_CODE,TYPE,STATUS,STATUS_DATE ) "+
							  " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
					pStmt1 = conn.prepareStatement(sql);
					pStmt1.setTimestamp(1,rs.getTimestamp(1));
					pStmt1.setString(2,rs.getString(2));
					pStmt1.setString(3,rs.getString(3));
					pStmt1.setString(4,rs.getString(4));
					pStmt1.setString(5,rs.getString(5));
					pStmt1.setString(6,rs.getString(6));
					pStmt1.setDouble(7,rs.getDouble(7));
					pStmt1.setDouble(8,rs.getDouble(8));
					pStmt1.setDouble(9,rs.getDouble(9));
					pStmt1.setDouble(10,rs.getDouble(10));
					pStmt1.setString(11,rs.getString(11));
					pStmt1.setTimestamp(12,rs.getTimestamp(12));
					pStmt1.setString(13,rs.getString(13));
					pStmt1.setString(14,rs.getString(14));
					pStmt1.setString(15,rs.getString(15));
					pStmt1.setTimestamp(16,rs.getTimestamp(16));
					pStmt1.setDouble(17,rs.getDouble(17));
					pStmt1.setString(18,rs.getString(18));
					pStmt1.setTimestamp(19,rs.getTimestamp(19));
					pStmt1.setString(20,rs.getString(20));
					pStmt1.setString(21,rs.getString(21));
					pStmt1.setString(22,rs.getString(22));
					pStmt1.setString(23,rs.getString(23));
					pStmt1.setString(24,rs.getString(24));
					pStmt1.setString(25,rs.getString(25));
					pStmt1.setString(26,rs.getString(26));
					pStmt1.setString(27,rs.getString(27));
					pStmt1.setString(28,rs.getString(28));
					pStmt1.setString(29,rs.getString(29));
					pStmt1.setString(30,rs.getString(30));
					pStmt1.setString(31,rs.getString(31));
					pStmt1.setString(32,rs.getString(32));
					pStmt1.setDouble(33,rs.getDouble(33));
					pStmt1.setString(34,rs.getString(34));
					pStmt1.setString(35,rs.getString(35));
					pStmt1.setString(36,rs.getString(36));
					pStmt1.setString(37,rs.getString(37));
					pStmt1.setString(38,rs.getString(38));
					pStmt1.setDouble(39,rs.getDouble(39));
					pStmt1.setString(40,rs.getString(40));
					pStmt1.setString(41,rs.getString(41));
					pStmt1.setString(42,rs.getString(42));
					pStmt1.setString(43,chbtranID);
					pStmt1.setString(44,rs.getString(43));
					pStmt1.setString(45,rs.getString(44));
					pStmt1.setString(46,rs.getString(45));
					pStmt1.setString(47,"O"); // VALLABH KADAM STATUS  is 'O' as OPEN
					pStmt1.setTimestamp(48,sysDate); // VALLABH KADAM STATUS DATE
					chbHed = pStmt1.executeUpdate();
					if(pStmt1 != null)
						pStmt1.close();
					pStmt1=null;
					System.out.println("No. of uploaded header records in a line ::"+chbHed);
						
				}
				if(rs != null)
					rs.close();
				rs = null;
				if(pStmt != null)
					pStmt.close();
				pStmt=null;  
				/*sql = " SELECT LINE_NO,CONTRACT_NO ,LINE_NO__CONTR  ,CUST_CODE__END   ,QUANTITY  ,LOT_NO ,RATE__SELL ,RATE__CONTR,RATE__DIFF,AMOUNT,TAX_AMT ,TAX_CLASS,TAX_CHAP ,"+
						" TAX_ENV,INVOICE_ID,ITEM_SER,DISCOUNT_PER ,DISCOUNT_AMT   ,PORDER_NO,PORDER_DATE  , BUYERS_PROD_CODE ,DISC_AMT ,CUST_GRP    ,ITEM_REF,   NET_AMT  ,TRAN_ID__CRN    ,"+
						" ERROR_MSG,PRICELIST_RATE,CONTRACT_RATE ,ERROR_CODE ,ITEM_CODE__NDC,UNCONF_CLAIMED,DISCOUNT_PER_UNIT ,"+
						" CONF_CLAIMED ,SALE_QTY ,SALE_RET_QTY,ITEM_CODE  FROM CHARGE_BACK_FORM_DET WHERE TRAN_ID IN(SELECT TRAN_ID FROM CHARGE_BACK_FORM WHERE TRAN_ID = ? AND VERIFY_FLAG IN ( 'Y','P')) " +
						" AND VERIFY_FLAG='Y' "; */ //comment by sagar on 02/07/14 remove ERROR_MSG and ERROR_CODE
				sql = " SELECT LINE_NO,CONTRACT_NO ,LINE_NO__CONTR  ,CUST_CODE__END   ,QUANTITY  ,LOT_NO ,RATE__SELL ,RATE__CONTR,RATE__DIFF,AMOUNT,TAX_AMT ,TAX_CLASS,TAX_CHAP ,"+
						" TAX_ENV,INVOICE_ID,ITEM_SER,DISCOUNT_PER ,DISCOUNT_AMT   ,PORDER_NO,PORDER_DATE  , BUYERS_PROD_CODE ,DISC_AMT ,CUST_GRP    ,ITEM_REF,   NET_AMT  ,TRAN_ID__CRN    ,"+
						" PRICELIST_RATE,CONTRACT_RATE ,ITEM_CODE__NDC,UNCONF_CLAIMED,DISCOUNT_PER_UNIT ,"+
						" CONF_CLAIMED ,SALE_QTY ,SALE_RET_QTY,ITEM_CODE  FROM CHARGE_BACK_FORM_DET WHERE TRAN_ID IN(SELECT TRAN_ID FROM CHARGE_BACK_FORM WHERE TRAN_ID = ? AND VERIFY_FLAG IN ( 'Y','P')) " +
						" AND VERIFY_FLAG='Y' "; 
				pStmt = conn.prepareStatement(sql);
				pStmt.setString(1, tranId.trim());
				rs = pStmt.executeQuery();
				
				while(rs.next())
				{
					lineNo ++;System.out.println(" LINInO ::"+lineNo);
					amount=rs.getDouble("AMOUNT");
					netAmt=rs.getDouble("NET_AMT");
					totamt+=amount;
					totnetAmt+=netAmt;
					/*sql = " INSERT INTO CHARGE_BACK_DET(LINE_NO,CONTRACT_NO ,LINE_NO__CONTR  ,CUST_CODE__END   ,QUANTITY  ,LOT_NO ,RATE__SELL ,RATE__CONTR,RATE__DIFF,AMOUNT,TAX_AMT ,TAX_CLASS,TAX_CHAP ,"+
							" TAX_ENV,INVOICE_ID,ITEM_SER,DISCOUNT_PER ,DISCOUNT_AMT   ,PORDER_NO,PORDER_DATE  , BUYERS_PROD_CODE ,DISC_AMT ,CUST_GRP    ,ITEM_REF,   NET_AMT  ,TRAN_ID__CRN    ,"+
							" ERROR_MSG,PRICELIST_RATE,CONTRACT_RATE ,ERROR_CODE,ITEM_CODE__NDC,UNCONF_CLAIMED,DISCOUNT_PER_UNIT ,"+
							" CONF_CLAIMED ,SALE_QTY ,SALE_RET_QTY,TRAN_ID,ITEM_CODE) "+
							" values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";*/ //comment by sagar on 02/07/14 remove ERROR_MSG and ERROR_CODE
					
					sql = " INSERT INTO CHARGE_BACK_DET(LINE_NO,CONTRACT_NO ,LINE_NO__CONTR  ,CUST_CODE__END   ,QUANTITY  ,LOT_NO ,RATE__SELL ,RATE__CONTR,RATE__DIFF,AMOUNT,TAX_AMT ,TAX_CLASS,TAX_CHAP ,"+
							" TAX_ENV,INVOICE_ID,ITEM_SER,DISCOUNT_PER ,DISCOUNT_AMT   ,PORDER_NO,PORDER_DATE  , BUYERS_PROD_CODE ,DISC_AMT ,CUST_GRP    ,ITEM_REF,   NET_AMT  ,TRAN_ID__CRN    ,"+
							" PRICELIST_RATE,CONTRACT_RATE ,ITEM_CODE__NDC,UNCONF_CLAIMED,DISCOUNT_PER_UNIT ,"+
							" CONF_CLAIMED ,SALE_QTY ,SALE_RET_QTY,TRAN_ID,ITEM_CODE) "+
							" values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
					
					pStmt1 = conn.prepareStatement(sql);
					pStmt1.setDouble(1,lineNo);
					pStmt1.setString(2,rs.getString(2));
					pStmt1.setString(3,rs.getString(3));
					pStmt1.setString(4,rs.getString(4));
					pStmt1.setDouble(5,rs.getDouble(5));
					pStmt1.setString(6,rs.getString(6));
					pStmt1.setDouble(7,rs.getDouble(7));
					pStmt1.setDouble(8,rs.getDouble(8));
					pStmt1.setDouble(9,rs.getDouble(9));
					pStmt1.setDouble(10,rs.getDouble(10));
					pStmt1.setDouble(11,rs.getDouble(11));
					pStmt1.setString(12,rs.getString(12));
					pStmt1.setString(13,rs.getString(13));
					pStmt1.setString(14,rs.getString(14));
					pStmt1.setString(15,rs.getString(15));
					pStmt1.setString(16,rs.getString(16));
					pStmt1.setDouble(17,rs.getDouble(17));
					pStmt1.setDouble(18,rs.getDouble(18));
					pStmt1.setString(19,rs.getString(19));
					pStmt1.setTimestamp(20,rs.getTimestamp(20));
					pStmt1.setString(21,rs.getString(21));
					pStmt1.setDouble(22,rs.getDouble(22));
					pStmt1.setString(23,rs.getString(23));
					pStmt1.setString(24,rs.getString(24));
					pStmt1.setDouble(25,rs.getDouble(25));
					pStmt1.setString(26,rs.getString(26));
					pStmt1.setDouble(27,rs.getDouble(27));
					pStmt1.setDouble(28,rs.getDouble(28));
					pStmt1.setString(29,rs.getString(29));
					pStmt1.setDouble(30,rs.getDouble(30));
					pStmt1.setDouble(31,Double.parseDouble(df.format(rs.getDouble(31))));   // CHANGE ON 13/08/14 BY RITESH 
					pStmt1.setDouble(32,rs.getDouble(32));
					pStmt1.setDouble(33,rs.getDouble(33));
					pStmt1.setDouble(34,rs.getDouble(34));	
					pStmt1.setString(35,chbtranID);
					pStmt1.setString(36,rs.getString(35));
					chbDet = chbDet + pStmt1.executeUpdate();
					if(pStmt1 != null)
						pStmt1.close();
					pStmt1=null;
				}
				System.out.println("No. of uploaded detail records in a line::"+chbDet);
				if(rs != null)
					rs.close();
				rs = null;
				if(pStmt != null)
					pStmt.close();
				pStmt=null;
				if(chbHed ==0 && chbDet == 0)
				{
//					rejectedFlag = true;
//					list.add(tranId);
//					list.add(custCode);
//					elements ++;
					errString = itmDBAccessEJB.getErrorString("","VTACCNT   ","","",conn);
				}
				else
				{
				    // errString = "Accepted";
					Calendar currentDate = Calendar.getInstance();
					SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
				    String sysDateStr = sdf.format(currentDate.getTime());
				    System.out.println(">>>>>>>Now sysDateStr :=>  " + sysDateStr);	
				    sysDate= Timestamp.valueOf(genericUtility.getValidDateString(sysDateStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
				    System.out.println(">>>>>>>>sysDate:"+sysDate);	
				    
				    sql = " update charge_back_form set tran_id__cb = ? , confirmed = 'Y', conf_date= ?, STATUS='A', STATUS_DATE=? where tran_id = ?";
				    pStmt1 = conn.prepareStatement(sql);
				    pStmt1.setString(1,chbtranID);
				    pStmt1.setTimestamp(2, sysDate);//conf_date is set by sagar on 04/07/14
				    pStmt1.setTimestamp(3,sysDate);
				    pStmt1.setString(4,tranId);
				    upConf = upConf + pStmt1.executeUpdate();
				    if(pStmt1 != null)
						pStmt1.close();
					pStmt1=null;
				    //Added by Manoj dtd 15/05/2014 to set amount,net_amt,claum_amt,net_amt__bc in header for verified lines only	
//				   sql="update charge_back set amount=?,net_amt=?,claum_amt=?,net_amt__bc=exch_rate*? where tran_id=? ";
				
					
					//sql="update charge_back set net_amt=?,claum_amt=?,net_amt__bc=exch_rate*? where tran_id=? ";//commented by priyanka as per manoj sir instruction on 21/01/15
					sql="update charge_back set net_amt=?,net_amt__bc=exch_rate*? where tran_id=? ";//added by priyanka as per manoj sir instruction on 21/01/15
					pStmt1=conn.prepareStatement(sql);
//				   pStmt1.setDouble(1,totamt);
				   pStmt1.setDouble(1,totnetAmt);
				  // pStmt1.setDouble(2,totnetAmt);
				   pStmt1.setDouble(2,totnetAmt);
				   pStmt1.setString(3,chbtranID);
				   pStmt1.executeUpdate();
				   if(pStmt1 != null)
					pStmt1.close();
				   pStmt1=null;
				   errString = itmDBAccessEJB.getErrorString("","VTACCSUCC   ","","",conn);
				   errString=setTranId(errString, chbtranID);// VALLABH KADAM To set TRAN_ID in success message [D15BSUN003] 28/MAY/15
				}				
			System.out.println(" No. of successfully accepted records in complete trans."+upConf);
//			if(rejectedFlag)
//			{
//				errString = itmDBAccessEJB.getErrorString("","VTREJCHB","","",conn);	
//				String begPart = errString.substring( 0, errString.indexOf("<trace>") + 7 );
//				String endPart = errString.substring( errString.indexOf("</trace>"));
//				String mainStr = begPart + "Following error has occured\n" ;
//				if(elements > 0 )
//				{ 
//					mainStr	= mainStr + "Transaction not varified: \n";
//				}
//				for(int i = 0; i < elements * 2; i++ )
//				{
//					if( i > 0)
//					{
//						i -= 1;
//					}
//					mainStr = mainStr + 
//							"Tran Id :"+list.get(i++)+ ",cust code :"+ list.get(i++) + "\n" ;
//				}
//				mainStr = mainStr +  endPart;	
//				errString = mainStr;
//				begPart =null;
//				endPart =null;
//				mainStr =null;	
//				//return errString;
//			}
//			if(errString.indexOf("rejected")>-1)
//			{
//				errString = itmDBAccessEJB.getErrorString("","PROCNTSUCC   ","","",conn);
//			}
//			else if(errString.indexOf("Accepted")>-1)
//			{
//				conn.commit();
//				errString = itmDBAccessEJB.getErrorString("","PROCSUCC   ","","",conn);
//			}
		}
		catch (Exception e)
		{
			errString = e.getMessage();
			System.out.println("Exception..."+e);
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				//if(errString.indexOf("VTREJCHB")>-1 || errString.indexOf("PROCSUCC")>-1)
				if(errString.indexOf("VTACCSUCC")>-1)
				{
					conn.commit();
				}
				if( conn != null && ! conn.isClosed() )
				{
					conn.close();
					conn = null;
				}
				if(pStmt != null)
				{
					pStmt.close();
					pStmt = null;					
				}	
				if(pStmt1 != null)
				{
					pStmt1.close();
					pStmt1 = null;					
				}	
				if(rs != null)
				{
					rs.close();
					rs = null;
				}
			}
			catch(Exception e)
			{
				errString = e.getMessage();
				System.out.println("Exception : "+e);e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return errString;
	}
	/**
	 * VALLABH KADAM 28/MAY/15
	 * After successful ACCEPT
	 * append tran_id in success message
	 * [D15BSUN003]
	 * */
	private String setTranId(String errString,String tranId) throws ITMException
    {
            Document errorDom = null;
            NodeList parentNodeList = null;
            NodeList childNodeList = null;        
            Node parentNode = null;
            Node childNode = null;
            String errorType = "";
            String msgDescr="";
            String childNodeName = null;
            int currentFormNo = 0,ctr=0;
            int childNodeListLength;
           // GenericUtility genericUtility = GenericUtility.getInstance();
            try
            {
            	StringBuffer valueXmlString = new StringBuffer();
               	errorDom = genericUtility.parseString(errString);
               	parentNodeList = errorDom.getElementsByTagName("Errors");
               	Node tempNode = errorDom.getElementsByTagName( "description" ).item(0);
               	String descr = checkNull(genericUtility.getColumnValue("description", errorDom));
                descr=descr+" CB Tran ID:- "+tranId;
                setNodeValue(errorDom, "description", descr);
                errString=genericUtility.serializeDom(errorDom);
                valueXmlString = new StringBuffer("<?xml version = \"1.0\"?>");
                valueXmlString.append(errString);
                errString=valueXmlString.toString();
            }
            catch(Exception e)
            {
                    e.printStackTrace();
                    throw new ITMException(e);
            }
            return errString;
    }
	
	private String checkNull(String input)
    {
            if (input == null) {
                    input = "";
            }
            return input;
    }
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
	
	private String generateTranId( String windowName,String siteCode,String siteCodeLog, Connection conn )throws ITMException
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
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
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
			System.out.println("keyString :"+keyString);
			System.out.println("keyCol :"+keyCol);
			System.out.println("tranSer :"+tranSer);

			xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
			xmlValues = xmlValues + "<Header></Header>";
			xmlValues = xmlValues + "<Detail1>";
			xmlValues = xmlValues +        "<tran_id></tran_id>";
			xmlValues = xmlValues +        "<site_code>" + siteCode + "</site_code>";
			xmlValues = xmlValues +        "<tran_date>" + currDateStr + "</tran_date>";
			xmlValues = xmlValues +        "<site_code__log>" + siteCodeLog + "</site_code__log>";
			xmlValues = xmlValues + "</Detail1></Root>";
			System.out.println("xmlValues  :["+xmlValues+"]");
			TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", CommonConstants.DB_NAME);
			tranId = tg.generateTranSeqID(tranSer, keyCol, keyString, conn);
			System.out.println("tranId :"+tranId);
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
		 System.out.println("@@@@@@@@@@@@@@@@@@@@@@tranId[[[[[[[[[[["+tranId+"]]]]]]]]]]]]]]]");
        return tranId;
     }//end of generateTranTd()
}
