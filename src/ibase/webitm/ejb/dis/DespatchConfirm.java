/******
 * created by Vishakha D
 * Request ID:D15KSUN031
 * post order migration
 * 
 */

package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.DBAccessEJB;
import ibase.webitm.ejb.E12CreateBatchLoadEjb;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.TransactionEmailTempltEJB;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.fin.InvAcct;
import ibase.webitm.ejb.sys.CreateRCPXML;
import ibase.webitm.utility.ITMException;
import ibase.webitm.utility.TransIDGenerator;
import ibase.webitm.ejb.TransactionEmailTempltEJB;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import org.w3c.dom.Document;
@javax.ejb.Stateless
public class DespatchConfirm extends ActionHandlerEJB implements DespatchConfirmLocal,DespatchConfirmRemote{
	
	 TransactionEmailTempltEJB TransactionEmailTempltEJB = new TransactionEmailTempltEJB();
	 FinCommon fnComm=new FinCommon();
     E12GenericUtility genericUtility = new E12GenericUtility();
	//Calling GR NO GEneration and Retreiving Despatch Code Here ...
	
	//GP No Generation code...

	public String generateGPNO(String xmlValues,String site,String SOrderNo,Connection conn) throws Exception
	{
		String GPno = "",orderType = "",orderTypes = "",keyString = "",keyCol = "",tranSer = "";
		DistCommon dis = new DistCommon();
		//SELECTING ORDER TYPE OF SALES ORDER

		PreparedStatement pstmt3 = null;

		ResultSet rs = null, rs1 = null,rs3 = null;
		String sql = "",sql1 = "";

		sql1 = "select order_type from sorder where  sale_order = ?  ";
		pstmt3 = conn.prepareStatement(sql1);
		pstmt3.setString(1,SOrderNo);
		rs3 = pstmt3.executeQuery();
		while(rs3.next())
		{
			orderType = rs3.getString("order_type");

		}
		rs3.close();
		rs3 = null;
		pstmt3.close();
		pstmt3 = null;
		System.out.println("Site Code is :["+site+"]");
		sql1 = "select key_string, TRAN_ID_COL, REF_SER  from	 transetup where  tran_window = 'gpno'";
		pstmt3 = conn.prepareStatement(sql1);
		rs3 = pstmt3.executeQuery();
		while(rs3.next())
		{
			keyString = rs3.getString("key_string");
			keyCol = rs3.getString("TRAN_ID_COL");
			tranSer = rs3.getString("REF_SER");

		}
		rs3.close();
		rs3 = null;
		pstmt3.close();
		pstmt3 = null;

		//SELECTING ORDER TYPES FROM DISPARM
		orderTypes = dis.getDisparams("999999", "GP_NO", conn);

		//IF DATA FOUND IN DISPARM THEN ONLY GENERATE GP NO ELSE NOT REQUIRED
		//COMPARING VALUES FROM SORDER AND DISPARM IF ORDER TYPE FROM SORDER DOES EXIST
		//IN DISPARM THEN GP_NO SHOULD NOT BE GENERATED
		/*TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", CommonConstants.DB_NAME);
		GPno =  tg.generateTranSeqID(site, keyCol, keyString, conn);
		System.out.println("GP NO Generated :::::::::"+GPno);*/
		//Added & replace by sarita on 28DEC2017 to check orderTypes to generate GP NO.
		if(orderTypes != null && !("NULLFOUND".equalsIgnoreCase(orderTypes)) && orderTypes.trim().length() > 0 && (orderTypes.indexOf(orderType)== -1))
		{
			TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", CommonConstants.DB_NAME);
			GPno =  tg.generateTranSeqID(site, keyCol, keyString, conn);
			System.out.println("GP NO Generated :::::::::"+GPno);
		}

		return GPno;
	}
	//Confirmation through Button ..



	//Calling Despatch Confirmation Code Here....



	public String confirm(String tranID,String xtraParams, String forcedFlag) throws RemoteException,ITMException
	{
		System.out.println("----------------confirmed method (Despatch confirm)----through button-----");
		String retString = "";		
		Connection conn = null;
		Connection connCP = null;	
		ConnDriver connDriver = null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		//Added By PriyankaC on 16OCt2019.[Start]
		       
		        ResultSet rs = null;
		        PreparedStatement pstmt = null;
				String toAddr = "",ccAddr = "",bccAddr = "",subject = "",body = "",templateName = "",attachObjLinks = "",attachments = "";
				String SendEmailOnNotify = "",errString="";
				String xmlString = "",reportType = "PDF",usrLevel = "",sordListStr="",sql = "",invoiceId="",fromCustCode ="";
				DBAccessEJB dbAccess = new DBAccessEJB();
				String loginCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
				
				//Added By PriyankaC on 16Oct2019 [END].
		try
		{
			//connDriver = new ConnDriver();
			//conn = connDriver.getConnectDB("Driver");
			//conn.setAutoCommit(false);
			conn = getConnection() ;
			UserInfoBean userInfo = dbAccess.createUserInfo(loginCode);
			sql = "select usr_lev from users where code = ? " ;
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, loginCode);
			rs = pstmt.executeQuery();
			if (rs.next()) 
			{
				usrLevel = checkNull(rs.getString("usr_lev"));
				userInfo.setUserLevel(usrLevel);
			}
			else
			{
				userInfo.setUserLevel("0");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			itmDBAccessEJB = new ITMDBAccessEJB();
			retString = confirm( tranID, xtraParams, forcedFlag,conn,connCP);	//calling and creating connection	
			if( retString != null && retString.trim().length() > 0  && !retString.contains("VTPOSTDES"))
			{
				conn.rollback();
				//return retString;
			}
			else
			{
				System.out.println("@@@@@@@@@@@118:::::::::::commiting record........");
				conn.commit();
				//Added By PriyankaC to sent auto mail on invoice conformation.[Start]
				sql = "select invoice_id , cust_code from invoice where desp_id = ? and confirmed = 'Y' ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranID);
				rs = pstmt.executeQuery();
				if (rs.next()) 
				{
					invoiceId = checkNull(rs.getString(1));
					fromCustCode = checkNull(rs.getString(2));
				}
				rs.close();
				rs = null;
				pstmt.close();
				
				System.out.println("invoiceId value " +invoiceId);
				if(invoiceId != null && invoiceId.trim().length() > 0)
				{
					System.out.println("invoiceId value INSIDE " +invoiceId);
					String templateCode  = fnComm.getFinparams("999999","GET_MAIL_FORMAT", conn);
					errString =  sendMailonConfirm(invoiceId,fromCustCode,templateCode,userInfo,conn);
					if( errString != null && errString.trim().length() > 0 )
					{
						String begPart = errString.substring(errString.indexOf("<STATUS>")+8,errString.indexOf("</STATUS>"));
						System.out.println("<STATUS> ::: " +begPart);
						if("N".equalsIgnoreCase(begPart))
						{
							return errString;
						}
						else
						{
							errString="";
						}
					}
					System.out.println("ierrString Prinat " +errString);
				}
				//Added By PriyankaC to sent auto mail on invoice confirmation.[END]
				retString = itmDBAccessEJB.getErrorString("","CONFSUCC","","",conn);
				return retString;
			}

		}
		catch(Exception exception)
		{
			try 
			{
				conn.rollback();
			} catch (Exception e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new ITMException(e);
			}
			System.out.println("Exception in [Despatch Confirmation] confirm " + exception.getMessage());
			exception.printStackTrace();
			throw new ITMException(exception);
		}

		finally
		{
			try
			{

				if( conn != null && ! conn.isClosed() )
				{
					conn.close();
					conn = null;
				}
			}
			catch(Exception ef)
			{
				System.out.println(ef.getMessage());
				ef.printStackTrace();
				throw new ITMException(ef);
			}

		}
		return retString;
	}
	 //Modified by Azhar K. on [07-05-2019][Start]
	public String confirm(String tranId,String xtraParams, String forcedFlag ,Connection conn,Connection connCP) throws RemoteException, ITMException
	{
		HashMap hm = new HashMap();
		String retStr = "";
		try
		{
			retStr = confirm(tranId,xtraParams, forcedFlag ,conn,connCP,hm);
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+e.getMessage());
			throw new ITMException(e);
		}
		return retStr;
	}
	public String confirm(String tranId,String xtraParams, String forcedFlag ,Connection conn,Connection connCP,HashMap additionalMap) throws RemoteException,ITMException
	 //Modified by Azhar K. on [07-05-2019][End]
	{
		System.out.println("----------------confirmed method (Despatch confirm)---- with Connection-----");

		DistCommon distCommon = new DistCommon();	
		PreparedStatement pstmt = null, pstmt1 = null ,pstmt3 = null ;
		ResultSet rs = null, rs1 = null,rs3 = null;
		String sql = "",sql1 = "",sql3 = "";
		String confirm = "", ledgPostConf = "";
		String errString = "",holdFlag = "",holdFlagNew = "",invStat = "",grade = "",tranSer = "",gpSer = "",gpNo = "",siteCodeExc = ""; 
		String statusDesp = "",channelPartner = "",lockLevel = "",isAllocated = "",varValue = "",disLink = "";
		String availableYN = "",tranid = "",siteCode = "",disparmVal = "",sordNoDet="",sordNo = "",lineNo = "",lineNoSord = "",expLev = "",itemCodeOrd = "",itemCode = "",lotNo = "",lotSl = "",locCode = "",unitStd = "";
		double quantityStduom = 0.0,quantity = 0.0,rateStduom = 0.0,hldQty = 0.0,qtyAlloc = 0.0,OrderQty = 0.0,mQtyCheck = 0.0;
		double mCancperc = 0.0,bondTaxAmt = 0.0,bondValue = 0.0,bankGuarantee = 0.0;
		double grossWeight = 0.0,tareWeight = 0.0,netweight = 0.0;
		String 	exportOrderType = "",bondTaxGroup = "",eou = "";
		String noArt = "",custCode = "",custCodeDlv = "",lockCode = "",varValuePurInteg = "",dataStr = "",siteCodeDlv = "";
		String status ="",allocFlag = "",pendOrder = "";
		String varValueTransit = "",invStatTransit = "",bondNo ="";
		HashMap updDesp = new HashMap();
		HashMap updDespTransit = new HashMap();
		E12GenericUtility genericUtility = new E12GenericUtility();
		//ConnDriver connDriver = null;
		ITMDBAccessEJB itmDBAccessEJB = null;
		Timestamp despDate = null,gpDate = null;
		int cnt = 0,editOption = 0,detCount = 0;
		boolean isLocal=false;
		String autoInvOnDesp="";
		boolean lockSuccess = false;
		Timestamp lrDate = null;
		PostOrderActivity postordact=null;
		String ordType = "",itemSer = "",acctCodeDr = "",cctrCodeDr = "",acctCodeCr = "",cctrCodeCr = "",invLock = "",holdTranId = "";
		Timestamp expDate = null, mfgDate = null;
		String siteCodeMfg = "", packCode = "";
		Timestamp tranDate = null;
		String confPasswd = "";
		String poRcpTranId = "";//Modified by Anjali R. on [12/11/2018]
		String gitUpdate = "N";// 10-oct-2019 manoharan to update GIT stock based on channel_partner flag in header 
		try
		{
			postordact=new PostOrderActivity();
			itmDBAccessEJB = new ITMDBAccessEJB();
			
		    Date today = new java.sql.Timestamp(System.currentTimeMillis()) ;// Added by Abhijit  on 15/05/17
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getDBDateFormat());
			SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getApplDateFormat());
			tranDate = java.sql.Timestamp.valueOf(sdf.format(today) + " 00:00:00.000");

			// Changed by Manish on 28/04/16 for update nowait [start]
			//	sql = "select confirmed from despatch where desp_id = ?";
			// 10-oct-2019 manoharan to update GIT stock based on channel_partner flag in header
			if ("db2".equalsIgnoreCase(CommonConstants.DB_NAME ))
			{
				sql = "select confirmed, lr_date,channel_partner from despatch where desp_id = ? for update ";
				//sql = "SELECT confirmed,(CASE WHEN status IS NULL THEN 'P' ELSE status END) as status , lr_date from despatch where desp_id = ? for update "; 
			}

			else if ( "mssql".equalsIgnoreCase(CommonConstants.DB_NAME ))
			{
				sql = "select confirmed, lr_date,channel_partner from despatch (updlock) where desp_id = ? " ;
				//sql = "SELECT confirmed, (CASE WHEN status IS NULL THEN 'P' ELSE status END) as status, lr_date from despatch (updlock) where desp_id = ? " ;
			}
			
			else
			{
			     sql = "select confirmed, lr_date,channel_partner from despatch where desp_id = ? for update nowait" ;
			     //sql = "select confirmed, (CASE WHEN status IS NULL THEN 'P' ELSE status END) as status,lr_date from despatch where desp_id = ? for update nowait" ;  
			    		 
			}

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,tranId);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				lockSuccess = true;
				confirm = rs.getString("confirmed");
				lrDate = rs.getTimestamp("lr_date");
				gitUpdate = rs.getString("channel_partner");// 10-oct-2019 manoharan to update GIT stock based on channel_partner flag in header
				
			}
			else
			{
				lockSuccess = false;
				throw new Exception("Transaction is locked, Please try after some time");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			// 10-oct-2019 manoharan to update GIT stock based on channel_partner flag in header
			System.out.println("confirm despatch....gitUpdate before["+gitUpdate + "]");
			if (gitUpdate == null || gitUpdate.trim().length() == 0)
			{
				gitUpdate = "N";
			}
			System.out.println("confirm despatch....gitUpdate after["+gitUpdate + "]");
			
			sql = "select ledg_post_conf from transetup where tran_window = 'w_despatch' ";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				ledgPostConf = rs.getString("ledg_post_conf");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			if("Y".equalsIgnoreCase(ledgPostConf))
			{
				System.out.println("Manish lrDate is["+lrDate+"]");	
				sql =  "update despatch set desp_date = ? "
					+" where desp_id = ? ";

				pstmt1 = conn.prepareStatement(sql);
				if(lrDate != null)
				{
					pstmt1.setTimestamp(1,lrDate);
				}
				else
				{
					pstmt1.setTimestamp(1, tranDate);
				}
				pstmt1.setString(2,tranId);
				int count = pstmt1.executeUpdate();

				System.out.println("Count value after updating despatch...."+count);

				pstmt1.close();
				pstmt1 = null;		
			}
			
			
			// Changed by Manish on 28/04/16 for update nowait [end]

			if(confirm != null  && "Y".equalsIgnoreCase(confirm))
			{
				System.out.println("The Selected transaction is already confirmed");
				errString = itmDBAccessEJB.getErrorString("","VTMCONF1","","",conn);
				return errString;
			}
			//else
			//{
				autoInvOnDesp = distCommon.getDisparams("999999", "AUTO_INV_ON_DESPATCH", conn);

				varValueTransit = distCommon.getDisparams("999999", "TRANSIT_LOC", conn);
				/*sql = "select var_value from disparm where prd_code='999999' and   var_name='TRANSIT_LOC'";
				pstmt = conn.prepareStatement(sql);

				rs = pstmt.executeQuery();
				if(rs.next())
				{

					varValueTransit = rs.getString("var_value");//location code
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;*/

				sql = "select inv_stat  from location where loc_code = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,varValueTransit);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					invStatTransit = rs.getString("inv_stat");
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;
				varValue=	distCommon.getDisparams("999999", "GP_NO_REQ", conn);
				if("NULLFOUND".equalsIgnoreCase(varValue))
				{
					varValue = "Y";
				}
				//Changes and Commented By Ajay on 25-12-2017:START
			//	sql = "select status,desp_date,available_yn,desp_id,site_code,sord_no,cust_code__dlv,cust_code,gp_date,gp_ser,gp_no "
			//		+ " from despatch where desp_id = ? ";// QUERY OF SRD TO BE COPIED...........
				sql ="select (CASE WHEN status IS NULL THEN 'P' ELSE status END) as status ,desp_date,available_yn,desp_id,site_code,sord_no,cust_code__dlv,cust_code,gp_date,gp_ser,gp_no "
					+ " from despatch where desp_id = ?";
				//Changes and Commented By Ajay on 25-12-2017:END
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				if ( rs.next() )
				{
					statusDesp = checkNull(rs.getString("status"));
					//	tranSer = rs.getString("tran_ser");
					//despDate = new Timestamp(rs.getDate("tran_Date").getTime());
					despDate = rs.getTimestamp("desp_date");
					availableYN = checkNull(rs.getString("available_yn"));
					tranid = checkNull(rs.getString("desp_id"));
					siteCode = checkNull(rs.getString("site_code"));
					sordNo = checkNull(rs.getString("sord_no"));
					custCode = checkNull(rs.getString("cust_code"));
					custCodeDlv = checkNull(rs.getString("cust_code__dlv"));
					gpDate = rs.getTimestamp("gp_date");
					gpSer = checkNull(rs.getString("gp_ser"));
					gpNo = checkNull(rs.getString("gp_no"));

				}
				pstmt.close();
				pstmt = null;					
				rs.close();
				rs = null;

				/**************************For despatch GP NO Generation********************************************************************/	

				varValue=	distCommon.getDisparams("999999", "GP_NO_REQ", conn);
				if("NULLFOUND".equalsIgnoreCase(varValue))
				{
					varValue = "Y";
				}
				if("Y".equals(varValue))
				{
					sql1 = "select site_code__exc "
						+" from site where site_code = ? ";

					pstmt1 = conn.prepareStatement(sql1);
					pstmt1.setString(1,siteCode);
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						siteCodeExc = checkNull(rs1.getString("site_code__exc"));
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					System.out.println("desp_date["+despDate+"] gp_date["+gpDate+"]");
					System.out.println("gp_date Format["+sdf.format(gpDate)+"]");
					System.out.println("gp_date Format["+sdf1.format(gpDate)+"]");
					String xmlValues = "";
					xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
					xmlValues = xmlValues + "<Header></Header>";
					xmlValues = xmlValues + "<Detail1>";
					xmlValues = xmlValues +	"<tran_id/>";
					xmlValues = xmlValues + "<site_code>" + siteCodeExc + "</site_code>";
					xmlValues = xmlValues + "<desp_date>"+ despDate + "</desp_date>";
					xmlValues = xmlValues + "<gp_ser>"+ gpSer + "</gp_ser>";
					xmlValues = xmlValues + "<gp_date>"+ sdf1.format(gpDate) + "</gp_date>";
					xmlValues = xmlValues + "</Detail1></Root>";


					varValue=	distCommon.getDisparams("999999", "EXC_SITE_NO", conn);
					System.out.println("varValue===="+varValue);

					if(	varValue.contains(siteCodeExc))
					{
						 
                          
						gpNo = generateGPNO(xmlValues,siteCodeExc+gpSer,sordNo,conn);//Code need to be migrated

					}
					else{

						gpNo = generateGPNO(xmlValues,siteCodeExc,sordNo,conn);//Code need to be migrated

					}

					sql =  "update despatch set gp_no = ? , gp_date = ? "
						+" where desp_id = ? ";

					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1,gpNo);
					pstmt1.setTimestamp(2,gpDate);
					pstmt1.setString(3,tranId);

					int cnt2 = pstmt1.executeUpdate();

					System.out.println("Count value after updating despatch...."+cnt2);

					pstmt1.close();
					pstmt1 = null;

				}
				//Changed by Pavan R 20jun2019 start
				sql = "select channel_partner,dis_link,site_code__ch from  site_customer where  cust_code = ? and site_code = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,custCodeDlv);
				pstmt.setString(2,siteCode);
				rs = pstmt.executeQuery();
				//23-Nov-16 Manoharan
				//while(rs.next())
				if(rs.next())
				{
					channelPartner = checkNull(rs.getString("channel_partner"));
					disLink = checkNull(rs.getString("dis_link"));
					siteCodeDlv = checkNull(rs.getString("site_code__ch"));

					if(channelPartner == null)//not found in above query
					{
						sql1 = "select channel_partner,dis_link, site_code	from  customer where  cust_code = ? ";
						pstmt1 = conn.prepareStatement(sql1);
						pstmt1.setString(1,custCodeDlv);
						rs1 = pstmt1.executeQuery();
						if(rs1.next())
						{
							channelPartner = checkNull(rs1.getString("channel_partner"));
							disLink = checkNull(rs1.getString("dis_link"));
							siteCodeDlv = checkNull(rs1.getString("site_code__ch"));

							if(channelPartner == null)
							{
								channelPartner = "N";
							}
						}
						pstmt1.close();
						pstmt1 = null;
						rs1.close();
						rs1 = null;

					}	
				}
				pstmt.close();
				pstmt = null;
				rs.close();
				rs = null;
				//Pavan Rane 20jun19 start [to generate password channel partner auto receitp]
				if("Y".equalsIgnoreCase(channelPartner) &&  ("E".equalsIgnoreCase(disLink) || "A".equalsIgnoreCase(disLink)))
				{										
					Random pwdGenerator = new Random();
					confPasswd = "" + pwdGenerator.nextInt(12345678);
					System.out.println("CPDespatch PWD #"+confPasswd);
				}					
				sql = "update despatch set conf_passwd = ?  where desp_id = ?";
				pstmt = conn.prepareStatement(sql);			
				pstmt.setString(1, confPasswd);
				pstmt.setString(2, tranId);
				pstmt.executeUpdate();
				pstmt.close();
				pstmt = null;
				//Pavan Rane 20jun19 end [to generate password channel partner auto receitp]
				
				//despatch_edit.setitem(ll_ctr,"confirmed", 'Y')	//Ruchira 21/09/2k5 taken out of abovve if ... endif statement.
				//		CHECKING WHETHER PURCHASE IS INTEGRATED WITH SALES OR NOT

				errString=postordact.createPORCP(tranId, xtraParams, conn,connCP);
				//Modified by Anjali R. on [12/11/2018][To get receipt no][Start]
				//if(errString != null && errString.trim().length()>0)
				if(errString != null && errString.trim().length()>0 && !(errString.indexOf("Success") > -1))
				{
					return errString;
				}
				else
				{
					//Modified by Anjali R. on[14/11/2018][Start]
					if(errString != null && errString.trim().length() > 0)
					{
					//Modified by Anjali R. on[14/11/2018][End]
						String[] arrayForTranId = errString.split("<TranID>");
						int endIndex = arrayForTranId[1].indexOf("</TranID>");
						poRcpTranId = arrayForTranId[1].substring(0,endIndex);
						System.out.println("poRcpTranId--["+poRcpTranId+"]");
					}
				}
				//Modified by Anjali R. on [12/11/2018][To get receipt no][End]

				exportOrderType = checkNull(distCommon.getDisparams("999999","EXPORT_DESPATCH_ORDER_TYPE",conn));//gf_getenv_dis('999999',"EXPORT_DESPATCH_ORDER_TYPE")

				if(exportOrderType != null && !"NULLFOUND".equals(exportOrderType) && exportOrderType.trim().length() > 0)
				{
					bondTaxGroup = 	checkNull(distCommon.getDisparams("999999","B17_BOND_TAX_GROUP",conn));
				}



				sql3 = "select sord_no,line_no,line_no__sord,exp_lev,item_code__ord,item_code,lot_no,lot_sl," +
				"loc_code,unit__std,quantity__stduom,quantity,rate__stduom,no_art,lock_code " +
				"from despatchdet where desp_id = ?";
				pstmt3 = conn.prepareStatement(sql3);
				pstmt3.setString(1,tranId);
				rs3 = pstmt3.executeQuery();
				//MAYUR KALIDAS NAIR -----START--[25-12-17]
				while ( rs3.next() )
				{
					if(statusDesp != "X") 
					{
						// Changed by Sneha on 22-02-2017, to remove checknull [Start]
						/*lineNo = checkNull(rs3.getString("line_no"));
						lineNoSord = checkNull(rs3.getString("line_no__sord"));						
						expLev = checkNull(rs3.getString("exp_lev"));*/
						sordNoDet = checkNull(rs3.getString("sord_no"));
						lineNo = rs3.getString("line_no");
						lineNoSord = rs3.getString("line_no__sord");
						expLev = rs3.getString("exp_lev");
						// Changed by Sneha on 22-02-2017, to remove checknull [End]
						
						itemCodeOrd = checkNull(rs3.getString("item_code__ord"));
						itemCode = checkNull(rs3.getString("item_code"));
						lotNo = rs3.getString("lot_no");
						lotSl = rs3.getString("lot_sl");
						locCode = checkNull(rs3.getString("loc_code"));
						unitStd = checkNull(rs3.getString("unit__std"));
						quantityStduom = rs3.getDouble("quantity__stduom");
						quantity = rs3.getDouble("quantity");
						rateStduom = rs3.getDouble("rate__stduom");
						noArt = checkNull(rs3.getString("no_art"));

						lockCode = checkNull(rs3.getString("lock_code"));
						disparmVal = checkNull(distCommon.getDisparams("999999","QUARNTINE_LOCKCODE",conn));//gf_getenv_dis('999999','QUARNTINE_LOCKCODE')

						/* //23-Nov-16 Manoharan
						sql = " Select hold_qty as lc_hold_qty "
							+" From stock Where item_code  = ? and site_code = ? and loc_code  = ? and  lot_no = ? and lot_sl = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,itemCode);
						pstmt.setString(2,siteCode);
						pstmt.setString(3,locCode);
						pstmt.setString(4,lotNo);
						pstmt.setString(5,lotSl);
						rs = pstmt.executeQuery();
						while(rs.next())
						{
							hldQty = rs.getDouble("lc_hold_qty");

						}
						pstmt.close();
						pstmt = null;
						rs.close();
						rs = null;
						*/
						sql = "  select grade ,acct_code__inv,cctr_code__inv, exp_date,mfg_date,site_code__mfg,pack_code, hold_qty "
							+" from   stock where  item_code = ? and site_code = ? and       loc_code = ? and    lot_no = ? and    lot_sl = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,itemCode);
						pstmt.setString(2,siteCode);
						pstmt.setString(3,locCode);
						pstmt.setString(4,lotNo);
						pstmt.setString(5,lotSl);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							grade = rs.getString("grade");
							acctCodeCr = rs.getString("acct_code__inv");
							cctrCodeCr = rs.getString("cctr_code__inv");
							expDate = rs.getTimestamp("exp_date");
							mfgDate = rs.getTimestamp("mfg_date");
							siteCodeMfg = rs.getString("site_code__mfg");
							packCode = rs.getString("pack_code");
							hldQty = rs.getDouble("hold_qty");
							
						}
						pstmt.close();
						pstmt = null;
						rs.close();
						rs = null;
						
						if(hldQty > 0.0)
						{
							sql =  "select count(*) as ll_cnt from inv_hold a, inv_hold_det b , inv_hold_rel_trace c"
								+" where a.tran_id = b.tran_id and b.tran_id = c.ref_no and c.item_code  = ? and c.site_code = ? and c.loc_code  = ?"
								+" and c.lot_no = ? and c.lot_sl  = ? and b.lot_no  = ?"
								+" and b.lot_sl = ? and (b.line_no_sl = 0 or b.line_no_sl is null)"
								+" and  a.confirmed='Y' 	and b.hold_status ='H' and a.lock_code = ? ";

							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,itemCode);
							pstmt.setString(2,siteCode);
							pstmt.setString(3,locCode);
							pstmt.setString(4,lotNo);
							pstmt.setString(5,lotSl);
							pstmt.setString(6,lotNo);
							pstmt.setString(7,lotSl);
							pstmt.setString(8,disparmVal);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt("ll_cnt");
							}
							pstmt.close();
							pstmt = null;
							rs.close();
							rs = null;

							if(cnt > 0)
							{
								lockCode = disparmVal;
							}


						}//end of hld condn...


						/*sql = "select channel_partner,dis_link,site_code__ch from  site_customer where  cust_code = ? and site_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,custCodeDlv);
						pstmt.setString(2,siteCode);
						rs = pstmt.executeQuery();
						//23-Nov-16 Manoharan
						//while(rs.next())
						if(rs.next())
						{
							channelPartner = checkNull(rs.getString("channel_partner"));
							disLink = checkNull(rs.getString("dis_link"));
							siteCodeDlv = checkNull(rs.getString("site_code__ch"));

							if(channelPartner == null)//not found in above query
							{
								sql1 = "select channel_partner,dis_link, site_code	from  customer where  cust_code = ? ";
								pstmt1 = conn.prepareStatement(sql1);
								pstmt1.setString(1,custCodeDlv);
								rs1 = pstmt1.executeQuery();
								if(rs1.next())
								{
									channelPartner = checkNull(rs1.getString("channel_partner"));
									disLink = checkNull(rs1.getString("dis_link"));
									siteCodeDlv = checkNull(rs1.getString("site_code__ch"));

									if(channelPartner == null)
									{
										channelPartner = "N";
									}
								}
								pstmt1.close();
								pstmt1 = null;
								rs1.close();
								rs1 = null;

							}	
						}
						pstmt.close();
						pstmt = null;
						rs.close();
						rs = null;
*/						//23-Nov-16 Manoharan
						//if(channelPartner == "N")
						if("N".equals(channelPartner))
						{
							sql = "select lock_level from inv_lock where lock_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,lockCode);

							rs = pstmt.executeQuery();
							//23-Nov-16 Manoharan
							//while(rs.next())
							if(rs.next())
							{
								lockLevel = checkNull(rs.getString("lock_level"));

							}
							pstmt.close();
							pstmt = null;
							rs.close();
							rs = null;
							//23-Nov-16 Manoharan
							//if(lockLevel == "I")
							if("I".equals(lockLevel))
							{
								sql = "select hold_flag  from item where item_code = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,itemCode);

								rs = pstmt.executeQuery();
								//23-Nov-16 Manoharan
								//while(rs.next())
								if(rs.next())
								{
									holdFlag = checkNull(rs.getString("hold_flag"));

								}
								pstmt.close();
								pstmt = null;
								rs.close();
								rs = null;
								//23-Nov-16 Manoharan
								//if(holdFlag == "Y")
								if("Y".equals(holdFlag))
								{
									rs3.close(); rs3 = null; //23feb19[to close the cursor and pstmt while retuing string]
									pstmt3.close(); pstmt3 = null;
									errString = itmDBAccessEJB.getErrorString("","VTCODE1","","",conn);
									return errString;
								}


							}
							//23-Nov-16 Manoharan
							//else if(lockLevel == "L")
							else if("L".equals(lockLevel))
							{
								sql = "select hold_flag  from item_lot_info  where item_code = ?  and lot_no = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,itemCode);
								pstmt.setString(2,lotNo);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									holdFlagNew = checkNull(rs.getString("hold_flag"));
								}
								pstmt.close();
								pstmt = null;
								rs.close();
								rs = null;
								//23-Nov-16 Manoharan
								//if(holdFlagNew == "Y")
								if("Y".equals(holdFlagNew))
								{
									rs3.close(); rs3 = null; //23feb19[to close the cursor and pstmt while retuing string]
									pstmt3.close(); pstmt3 = null;
									errString = itmDBAccessEJB.getErrorString("","VTCODE2","","",conn);
									return errString ;
								}

							}

						}

						sql = " select inv_stat  from location where loc_code = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,locCode);

						rs = pstmt.executeQuery();
						//23-Nov-16 Manoharan
						//while(rs.next())
						if(rs.next())
						{
							invStat = rs.getString("inv_stat");
						}
						pstmt.close();
						pstmt = null;
						rs.close();
						rs = null;

						sql = " select count(*) as count1 from sordalloc "
							+" where  sale_order 	 	= ? "
							+" and 	 line_no 		 	= ? "
							+" and 	 exp_lev 			= ? "
							+" and 	 item_code__ord 	= ? "
							+" and 	 item_code 			= ? "
							+" and 	 lot_no 				= ? "
							+" and 	 lot_sl 				= ? "
							+" and 	 loc_code 			= ? ";

						pstmt = conn.prepareStatement(sql);
						//pstmt.setString(1,sordNo);
						pstmt.setString(1,sordNoDet);
						pstmt.setString(2,lineNo);
						pstmt.setString(3,expLev);
						pstmt.setString(4,itemCodeOrd);
						pstmt.setString(5,itemCode);
						pstmt.setString(6,lotNo);
						pstmt.setString(7,lotSl);
						pstmt.setString(8,locCode);

						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt = rs.getInt("count1");
						}
						pstmt.close();
						pstmt = null;
						rs.close();
						rs = null;

						if(cnt == 0)
						{
							isAllocated = "N";
						}
						else{
							isAllocated = "Y";
						}
						//23-Nov-16 Manoharan
						//if(isAllocated == "Y")
						if("Y".equals(isAllocated))
						{
							sql ="select qty_alloc  from sordalloc "
								+"  where sale_order 	 = ? and line_no 		 = ? and exp_lev 		 = ? 	and item_code__ord = ? and item_code 	 	 = ? "
								+"	and lot_no 			 = ? and lot_sl 			 = ? and loc_code 		 = ? ";

							pstmt = conn.prepareStatement(sql);
							//pstmt.setString(1,sordNo);
							pstmt.setString(1,sordNoDet);
							pstmt.setString(2,lineNo);
							pstmt.setString(3,expLev);
							pstmt.setString(4,itemCodeOrd);
							pstmt.setString(5,itemCode);
							pstmt.setString(6,lotNo);
							pstmt.setString(7,lotSl);
							pstmt.setString(8,locCode);

							rs = pstmt.executeQuery();
							if(rs.next())
							{
								qtyAlloc = rs.getDouble("qty_alloc");
							}
							pstmt.close();
							pstmt = null;
							rs.close();
							rs = null;

							if((qtyAlloc - quantityStduom) <= 0.0)
							{
								sql =" delete from sordalloc where sale_order  = ? and line_no = ? and exp_lev = ? and item_code__ord = ? and item_code  = ?"
									+" and lot_no = ? and lot_sl = ? and loc_code  = ? ";

								pstmt = conn.prepareStatement(sql);
								//pstmt.setString(1,sordNo);
								pstmt.setString(1,sordNoDet);
								pstmt.setString(2,lineNo);
								pstmt.setString(3,expLev);
								pstmt.setString(4,itemCodeOrd);
								pstmt.setString(5,itemCode);
								pstmt.setString(6,lotNo);
								pstmt.setString(7,lotSl);
								pstmt.setString(8,locCode);
								// 23-Nov-16 manoharan should update
								//pstmt.executeQuery();
								pstmt.executeUpdate();

								pstmt.close();
								pstmt = null;

							}
							else{
								sql = "Update sordalloc 		set status 		= 'D', qty_alloc 	=  qty_alloc - ? "
									+" where sale_order 	 = ? and line_no 		 = ?  and   exp_lev 		 = ? and item_code__ord = ? and item_code 	 	 = ? "
									+"	and lot_no 			 = ? and lot_sl 			 = ? and loc_code 		 = ? ";
								pstmt = conn.prepareStatement(sql);
								//pstmt.setDouble(1,qtyAlloc - quantityStduom);
								pstmt.setDouble(1,quantityStduom);
								//pstmt.setString(2,sordNo);
								pstmt.setString(2,sordNoDet);
								pstmt.setString(3,lineNo);
								pstmt.setString(4,expLev);
								pstmt.setString(5,itemCodeOrd);
								pstmt.setString(6,itemCode);
								pstmt.setString(7,lotNo);
								pstmt.setString(8,lotSl);
								pstmt.setString(9,locCode);

								// 23-Nov-16 manoharan should update
								//pstmt.executeQuery();
								pstmt.executeUpdate();
								
								pstmt.close();
								pstmt = null;

							}

						}

						sql = " update sorditem	set status	  = 'D', qty_alloc = qty_alloc - ? , " +
						"  qty_desp  = (case when qty_desp is null then 0 else qty_desp end )  + ? , "+
						"  date_desp = ? where sale_order = ? and line_no    = ? and exp_lev	  = ? ";

						pstmt = conn.prepareStatement(sql);
						//	pstmt.setDouble(1,quantity);
						//	pstmt.setDouble(2,qtyAlloc - quantityStduom);
						pstmt.setDouble(1, quantityStduom);
						pstmt.setDouble(2,quantity);
						pstmt.setTimestamp(3,despDate);
						//pstmt.setString(4,sordNo);
						pstmt.setString(4,sordNoDet);
						
						//changes lineNo to lineNoSord By Pavan Rane on 30/05/17 START
						pstmt.setString(5, lineNoSord);						
						//pstmt.setString(5,lineNo);
						//changes lineNo to lineNoSord By Pavan Rane on 30/05/17 END
						
						pstmt.setString(6,expLev);
						pstmt.executeUpdate();

						pstmt.close();
						pstmt = null;

						// CALLING FUNCTION FOR UPDATING STOCK AFTER DESPATCH	PICKING GRADE,ACCT_CODE__CR AND CCTR_CODE__CR FROM STOCK
						// 23-Nov-16 manoharan commented and included in previous select
						/*
						sql = "  select grade ,acct_code__inv,cctr_code__inv, exp_date,mfg_date,site_code__mfg,pack_code "
							+" from   stock where  item_code = ? and site_code = ? and       loc_code = ? and    lot_no = ? and    lot_sl = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,itemCode);
						pstmt.setString(2,siteCode);
						pstmt.setString(3,locCode);
						pstmt.setString(4,lotNo);
						pstmt.setString(5,lotSl);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							grade = rs.getString("grade");
							acctCodeCr = rs.getString("acct_code__inv");
							cctrCodeCr = rs.getString("cctr_code__inv");
							expDate = rs.getTimestamp("exp_date");
							mfgDate = rs.getTimestamp("mfg_date");
							siteCodeMfg = rs.getString("site_code__mfg");
							packCode = rs.getString("pack_code");
							
						}
						pstmt.close();
						pstmt = null;
						rs.close();
						rs = null;
						*/
						// 23/Nov-16 manoharan pending_order included and commented below
						// PICKING ORDER TYPE, ITEM SER FROM SORDER 
						sql = "   select order_type, item_ser, pending_order  from sorder  "
							+" where  sale_order = ?";
						pstmt = conn.prepareStatement(sql);
						//pstmt.setString(1,sordNo);
						pstmt.setString(1,sordNoDet);
						
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							ordType = rs.getString("order_type");
							itemSer = rs.getString("item_ser");
							pendOrder = checkNull(rs.getString("pending_order"));
						}
						pstmt.close();
						pstmt = null;
						rs.close();
						rs = null;

						//PICKING ACCT_CODE AND CCTR_CODE COGS. IF CCTR CODE COGS IS BLANK THEN REPLACE WITH CREDIT CCTR CODE AS PER SHIRI	

						sql   =     " select acct_code__cogs, cctr_code__cogs from   item_acct_detr 	where  item_ser = ?  and    grp_code = ' '"
							+" and    item_code = ' ' and    tran_type = ? ";

						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,itemSer);
						pstmt.setString(2,ordType);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							acctCodeDr = rs.getString("acct_code__cogs");
							cctrCodeDr = rs.getString("cctr_code__cogs");
						}
						pstmt.close();
						pstmt = null;
						rs.close();
						rs = null;

						if(cctrCodeDr == null || cctrCodeDr.trim().length() == 0)
						{
							cctrCodeDr = cctrCodeCr;
						}
						updDesp.put("hold_lock", "N");
						updDesp.put("tran_date",despDate);
						updDesp.put("tran_ser","S-DSP");
						updDesp.put("tran_id", tranId);
						updDesp.put("tran_type", "ID");
						updDesp.put("line_no", lineNo);
						//updDesp.put("sorder_no",sordNo);
						updDesp.put("sorder_no",sordNoDet);
						updDesp.put("item_code", itemCode);
						updDesp.put("site_code", siteCode);
						updDesp.put("loc_code", locCode);
						updDesp.put("unit", unitStd);
						updDesp.put("lot_no", lotNo);
						updDesp.put("lot_sl", lotSl);

						updDesp.put("quantity", quantity);
						updDesp.put("qty_stduom", quantityStduom);
						updDesp.put("rate", rateStduom);
						updDesp.put("item_ser",itemSer);
						updDesp.put("inv_stat", invStat);
						updDesp.put("grade", grade);
						updDesp.put("acct_code__dr", acctCodeDr);
						updDesp.put("cctr_code__dr", cctrCodeDr);
						updDesp.put("acct_code__cr",acctCodeCr);
						updDesp.put("cctr_code__cr", cctrCodeCr);

						updDesp.put("gross_weight", grossWeight);
						updDesp.put("tare_weight", tareWeight);
						updDesp.put("net_weight", netweight);
						updDesp.put("no_art",noArt);



						if(hldQty > 0.0)
						{
							sql =  "  Select a.lock_code ,a.tran_id "
								+ "	From inv_hold a, inv_hold_det b , inv_hold_rel_trace c"
								+ "	Where a.tran_id = b.tran_id "
								+ "	And b.tran_id = c.ref_no "
								+ "	And c.item_code  = ? "
								+ "	And c.site_code = ? "
								+ "	And c.loc_code  = ? "
								+ "	And c.lot_no    = ? "
								+ "	And c.lot_sl    = ? "
								+ "	And b.lot_no    = ? "
								+ "	And b.lot_sl    = ? "
								+ "	And (b.line_no_sl = 0 or b.line_no_sl is null) "
								+ "	And a.confirmed='Y' "
								+ "	And b.hold_status ='H' ";

							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,itemCode);
							pstmt.setString(2,siteCode);
							pstmt.setString(3,locCode);
							pstmt.setString(4,lotNo);
							pstmt.setString(5,lotSl);
							pstmt.setString(6,lotNo);
							pstmt.setString(7,lotSl);

							rs = pstmt.executeQuery();
							if(rs.next())
							{
								invLock = rs.getString("lock_code");
								holdTranId = rs.getString("tran_id");
							}
							pstmt.close();
							pstmt = null;
							rs.close();
							rs = null;			

							// 23-Nov-16 manoharan
							//if(invLock != null && invLock.trim().length() > 0 && availableYN == "N")
							if(invLock != null && invLock.trim().length() > 0 && "N".equals(availableYN))
							{
								if(invLock.equals(disparmVal))
								{
									updDesp.put("hold_lock", "Y");
								}
								else
								{
									updDesp.put("hold_lock", "N");
								}
							}
							if("Y".equalsIgnoreCase(updDesp.get("hold_lock")==null?"":updDesp.get("hold_lock").toString()))
							{
								updDesp.put("hold_qty", quantityStduom);
							}

							//System.out.println("updDesp nandu" +updDesp );
						}
						//added by nandkumar gadkari on 22/04/19----------start----------------\
						String logMsg= tranId +" "+expLev+" "+lineNo+ " "+"Deallocation of stock from DespatchConfirm";
						updDesp.put("update_ref",logMsg);
						//added by nandkumar gadkari on 22/04/19----------start----------------\
						StockUpdate stckupd = new StockUpdate();
						errString = stckupd.updateStock(updDesp, xtraParams, conn);
						//Added by Pavan R on 01aug18 [errString msg is returned from stock update, this message was ignored in a specific situation]
						if(errString != null && errString.trim().length()>0)
						{
							rs3.close(); rs3 = null; //23feb19[to close the cursor and pstmt while retuing string]
							pstmt3.close(); pstmt3 = null;
							return errString;
						}
						//CALLING UPDATE OF STOCK FOR TRANSIT ENTRY
						// 23-Nov-16 manoharan wrong syntax
						//if(channelPartner == "Y" &&  disLink == "E")
						// 01-Mar-2019 in case dis_link A also to be updated
						// 10-oct-2019 manoharan to update GIT stock based on channel_partner flag in header
						//if("Y".equalsIgnoreCase(channelPartner) &&  ("E".equalsIgnoreCase(disLink) || "A".equalsIgnoreCase(disLink)))
						if("Y".equalsIgnoreCase(gitUpdate) &&  ("E".equalsIgnoreCase(disLink) || "A".equalsIgnoreCase(disLink)))
						{
							// 23-Nov-16 manoharan included in previous select and commented
							/*sql = "select exp_date,mfg_date,site_code__mfg,pack_code,acct_code__inv,cctr_code__inv "
								+"	from stock where site_code = ? and item_code = ? and loc_code = ? and lot_no = ? and lot_sl = ? ";

							pstmt = conn.prepareStatement(sql);

							pstmt.setString(1,siteCode);
							pstmt.setString(2,itemCode);
							pstmt.setString(3,locCode);
							pstmt.setString(4,lotNo);
							pstmt.setString(5,lotSl);
							rs = pstmt.executeQuery();
							while(rs.next())
							{*/
							//Commented by nandkumar gadkari on 07/03/19 to  replace updDespTransit map to updDesp
								/*updDespTransit.put("acct_code_inv", acctCodeCr);
								updDespTransit.put("cctr_code_inv", cctrCodeCr);
								updDespTransit.put("exp_date", expDate);
								updDespTransit.put("mfg_date", mfgDate);
								updDespTransit.put("site_code__mfg", siteCodeMfg);
								updDespTransit.put("pack_code", packCode);
								updDespTransit.put("inv_stat", invStatTransit);
								updDespTransit.put("loc_code",varValueTransit);
								updDespTransit.put("tran_type","R");
								updDespTransit.put("site_code",siteCodeDlv);
								updDespTransit.put("hold_qty",0.0);*/
							//added  by nandkumar gadkari on 07/03/19 to  replace updDespTransit map to updDesp	
								
							updDesp.put("acct_code_inv", acctCodeCr);
							updDesp.put("cctr_code_inv", cctrCodeCr);
							updDesp.put("exp_date", expDate);
							updDesp.put("mfg_date", mfgDate);
							updDesp.put("site_code__mfg", siteCodeMfg);
							updDesp.put("pack_code", packCode);
							updDesp.put("inv_stat", invStatTransit);
							updDesp.put("loc_code",varValueTransit);
							updDesp.put("tran_type","R");
							updDesp.put("site_code",siteCodeDlv);
							updDesp.put("hold_qty",0.0);
						//	System.out.println("updDespTransit" +updDesp );
								errString = stckupd.updateStock(updDesp, xtraParams, conn);// updDespTransit map  replace with updDesp by nandkumar gadkari on 07/03/19
								//Added by Pavan R on 01aug18 [errString msg is returned from stock update, this message was ignored in a specific situation]
								if(errString != null && errString.trim().length()>0)
								{
									rs3.close(); rs3 = null; //23feb19[to close the cursor and pstmt while retuing string]
									pstmt3.close(); pstmt3 = null;
									return errString;
								}


							/*}
							pstmt.close();
							pstmt = null;
							rs.close();
							rs = null;	
							*/							



							//following code added by ajit on date 19/08/2014
							if(errString == null || errString.trim().length() == 0 && "Y".equalsIgnoreCase(updDesp.get("hold_lock")==null?"":updDesp.get("hold_lock").toString()) )

							{
								sql = 	" Update stock 	set  hold_qty = case when hold_qty is null then 0 else hold_qty end + ? Where item_code  = ?  And site_code = ? "
									+" And loc_code  = ? And lot_no  = ? And lot_sl  = ? ";

								pstmt = conn.prepareStatement(sql);
								pstmt.setDouble(1,quantityStduom);
								pstmt.setString(2,itemCode);
								pstmt.setString(3,siteCodeDlv);
								pstmt.setString(4,invStatTransit);
								pstmt.setString(5,lotNo);
								pstmt.setString(6,lotSl);
								pstmt.executeUpdate();

								pstmt.close();
								pstmt = null;

								sql = 	"update inv_hold_det	set loc_code = ?, site_code  = ? Where tran_id   = ? And item_code = ? And lot_no  = ? "
									+"  And lot_sl  = ? And loc_code  = ? And hold_status ='H'" ;

								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,invStatTransit);
								pstmt.setString(2,itemCode);
								pstmt.setString(3,holdTranId);
								pstmt.setString(4,itemCode);
								pstmt.setString(5,lotNo);
								pstmt.setString(6,lotSl);
								pstmt.setString(7,locCode);

								pstmt.executeUpdate();

								pstmt.close();
								pstmt = null;

								sql = "	update inv_hold_rel_trace 	set loc_code    = ?,	site_code  = ? Where ref_no   = ? And site_code   = ? "
									+"	And item_code   = ? 	And lot_no   = ? And lot_sl  = ? And loc_code = ? And hold_qty    > 0  ";

								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,invStatTransit);
								pstmt.setString(2,siteCodeDlv);
								pstmt.setString(3,holdTranId);
								pstmt.setString(4,siteCode);
								pstmt.setString(5,itemCode);
								pstmt.setString(6,lotNo);
								pstmt.setString(7,lotSl);
								pstmt.setString(8,locCode);
								pstmt.executeUpdate();

								pstmt.close();
								pstmt = null;



							}

						}//end of transit entry
						//Updating sorder status.	
						stckupd = null;

						sql = "select sum(quantity) as lc_order_qty , sum(quantity) - sum(qty_desp) as mqty_chk from sorditem "
							+ "where sale_order = ?  and line_type = 'I'";
						pstmt = conn.prepareStatement(sql);
						//pstmt.setString(1,sordNo);
						pstmt.setString(1,sordNoDet);
						rs = pstmt.executeQuery();
						if(rs.next())
						{

							OrderQty = rs.getDouble("lc_order_qty");
							mQtyCheck = rs.getDouble("mqty_chk");
						}
						pstmt.close();
						pstmt = null;
						rs.close();
						rs = null;


						if(mQtyCheck > 0.0)	  //PARTIAL DESPATCH	  
						{
							status = "P";
							allocFlag = "N";
						}
						else
						{
							status = "C";
							allocFlag = "N";// ALLOCFLAG IS SET Y TO N BY NANDKUMAR GADKARI ON 08/01/19 
						}

						sql = " update sorder	set  status = ?,order_status = ?,  status_date = ?,alloc_flag  = ? "
							+" where  sale_order = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,status);
						pstmt.setString(2,status);
						pstmt.setTimestamp(3,despDate);
						pstmt.setString(4,allocFlag);
						//pstmt.setString(5,sordNo);
						pstmt.setString(5,sordNoDet);

						pstmt.executeUpdate();

						pstmt.close();
						pstmt = null;

						//End of updating sorder status. 

						/* // 23-Nov-16 manoharan commented and included in previous select
						sql = " select pending_order 	from sorder	where  sale_order = ? ";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,sordNo);
						rs = pstmt.executeQuery();
						while(rs.next())
						{
							pendOrder = checkNull(rs.getString("pending_order"));
						}
						pstmt.close();
						pstmt = null;
						rs.close();
						rs = null;
						*/
						// 23-Nov-16 Manoharan
						//if(pendOrder == "I")
						if("I".equals(pendOrder))
						{
							sql = "Select (case when ordc_perc is null then 0 else ordc_perc end) as mCancperc "
								+"	from item where item_code = ? "	;	
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,itemCode);
							rs = pstmt.executeQuery();
							// 23-Nov-16 manoharan
							//while(rs.next())
							if(rs.next())
							{
								mCancperc = rs.getDouble("mCancperc");
							}
							pstmt.close();
							pstmt = null;
							rs.close();
							rs = null;
							

							if(OrderQty > 0.0)
							{
								if((mQtyCheck/OrderQty * 100) <= mCancperc)
								{
									sql = "	update sorditem	set status  = 'C' where sale_order = ? and line_no = ?  and exp_lev= ? ";
									pstmt = conn.prepareStatement(sql);
									//pstmt.setString(1,sordNo);
									pstmt.setString(1,sordNoDet);
									pstmt.setString(2,lineNoSord);
									pstmt.setString(3,expLev);
									pstmt.executeUpdate();

									pstmt.close();
									pstmt = null;


									sql = "update sorddet	set status  = 'C' where  sale_order = ?	and line_no    = ? ";

									pstmt = conn.prepareStatement(sql);
									//pstmt.setString(1,sordNo);
									pstmt.setString(1,sordNoDet);
									pstmt.setString(2,lineNoSord);

									pstmt.executeUpdate();

									pstmt.close();
									pstmt = null;



								}


								sql = "select count(1) as ll_cnt from sorddet	where sale_order = ? and status  <> 'C'";	
								pstmt = conn.prepareStatement(sql);
								//pstmt.setString(1,sordNo);
								pstmt.setString(1,sordNoDet);
								rs = pstmt.executeQuery();
								while(rs.next())
								{
									detCount = rs.getInt("ll_cnt");
								}
								pstmt.close();
								pstmt = null;
								rs.close();
								rs = null;
								if(detCount == 0)
								{
									sql = "update sorder set   status   = 'C', status_date = ?, order_status = 'C'  " //, alloc_flag  = 'Y' REMOVED FROM SQL BY NANDKUMAR GADKARI ON 08/01/19
										+"where  sale_order = ? " ;	

									pstmt = conn.prepareStatement(sql);
									pstmt.setTimestamp(1,despDate);
									//pstmt.setString(2,sordNo);
									pstmt.setString(2,sordNoDet);

									pstmt.executeUpdate();

									pstmt.close();
									pstmt = null;

								}

							}

						}//end of if pending order == "I"
						// 23-Nov-16 manoharan
						//else	if(pendOrder == "Y")
						else if("Y".equals(pendOrder))
						{
							sql = "update sorder	set  status  = 'C', status_date = ? , order_status = 'C' where  sale_order = ?"; //, alloc_flag  = 'Y' REMOVED FROM SQL BY NANDKUMAR GADKARI ON 08/01/19

							pstmt = conn.prepareStatement(sql);
							pstmt.setTimestamp(1,despDate);
							//pstmt.setString(2,sordNo);
							pstmt.setString(2,sordNoDet);  
							pstmt.executeUpdate();

							pstmt.close();
							pstmt = null;
						}

						//If eou is site and EXPORT order type than Updating Bond Value, bond value shd be Credited
						sql = "Select case when eou is null then 'N' else eou end as ls_eou From site Where site_code = ? "	;
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,siteCode);
						rs = pstmt.executeQuery();
						// 23-Nov-16 Manoharan
						//while(rs.next())
						if(rs.next())
						{
							eou = rs.getString("ls_eou");
						}
						pstmt.close();
						pstmt = null;
						rs.close();
						rs = null;
						// 23-Nov-16 Manoharan
						//if("Y".equalsIgnoreCase(eou) && (exportOrderType == ordType) &&  bondTaxGroup!= null && bondTaxGroup.trim().length() > 0)
						if("Y".equalsIgnoreCase(eou) && (exportOrderType.trim().equals(ordType.trim())) &&  bondTaxGroup!= null && bondTaxGroup.trim().length() > 0)
						{
							bondTaxAmt = 0.0;
							sql = "select Sum(case when tax_amt is null then 0 else tax_amt end) as tax_amt from   taxtran Where  tran_code = 'S-DSP'  and tran_id = ? "
								+ "and line_no = ? and tax_code IN ( select tax_code from tax where tax_group in ( ? )" ;
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,tranId);
							pstmt.setString(2,lineNo);
							pstmt.setString(3,bondTaxGroup);
							rs = pstmt.executeQuery();
							// 23-Nov-16 Manoharan
							//while(rs.next())
							if(rs.next())
							{
								bondTaxAmt = rs.getDouble("tax_amt");
							}
							pstmt.close();
							pstmt = null;
							rs.close();
							rs = null;	

							if(bondTaxAmt != 0.0)	
							{
								int cnt4=0;
								bondValue = 0.0;
								sql = "select bond_no, case when  bond_value is null then 0 else bond_value end as bond_value,case when  bank_guarantee is null then 0 else bank_guarantee end " 
									+ " as lc_bank_guarantee from b17_bond where site_code = ? and ? >= eff_from and ? <= valid_upto "
									+"	and case when confirmed is null then 'N' else confirmed end = 'Y'	and bond_type = 'B' " ;
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,siteCode);
								pstmt.setTimestamp(2,despDate);
								pstmt.setTimestamp(3,despDate);
								pstmt.setString(3,bondTaxGroup);
								rs = pstmt.executeQuery();
								while(rs.next())
								{
									cnt4++;
									bondNo = rs.getString("bond_no");
									bondValue = rs.getDouble("bond_value");
									bankGuarantee = rs.getDouble("lc_bank_guarantee");

								}
								pstmt.close();
								pstmt = null;
								rs.close();
								rs = null;
								if(cnt4 == 0)
								{
									rs3.close(); rs3 = null; //23feb19[to close the cursor and pstmt while retuing string]
									pstmt3.close(); pstmt3 = null;
									errString = itmDBAccessEJB.getErrorString("","VTB17ERR1","","",conn);
									return errString ;
								}
								else if(bankGuarantee < (bondValue+bondTaxAmt))
								{
									rs3.close(); rs3 = null; //23feb19[to close the cursor and pstmt while retuing string]
									pstmt3.close(); pstmt3 = null;
									errString = itmDBAccessEJB.getErrorString("","VTB17ERR2","","",conn);
									return errString ;
								}
								else
								{
									sql = "update b17_bond set bond_value = ? where bond_no = ?";

									pstmt = conn.prepareStatement(sql);
									pstmt.setDouble(1,bondValue+bondTaxAmt);
									pstmt.setString(2,bondNo);

									pstmt.executeUpdate();

									pstmt.close();
									pstmt = null;
								}
							}
						}
					}//status not cancelled		
				}
				//MA YUR KALIDAS NAIR -----END--[25-12-17]
				pstmt3.close();
				pstmt3 = null;					
				rs3.close();
				rs3 = null;
			//}

			System.out.println("ERROR STRING WHILE DESPATCH CONFIRMATION!!!!!!!!!!!!!!"+errString);

			if(errString == null || errString.trim().length()== 0)
			{
				System.out.println("Insiding Despatch Confirmation!!!!!!");
				
				sql = "update despatch set confirmed = 'Y', conf_date = ? where desp_id = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setTimestamp(1,new Timestamp(System.currentTimeMillis()));				
				pstmt.setString(2,tranId);
				int confirmCnt = pstmt.executeUpdate();

				pstmt.close();
				pstmt = null;

				if(confirmCnt > 0)
				{
					InvAcct inv = new InvAcct();
					errString = inv.despatchPost(tranId,"S-DSP",conn);
					if(errString.trim().length() == 0 || errString == null)
					{
						PostOrderActivity pstordact=new PostOrderActivity();
						errString=pstordact.gbfCreateFrtDrn(tranId, xtraParams, conn);
					}
					inv = null;
					if(errString.indexOf("CONFSUCCES") > -1 || errString == null || errString.trim().length()==0)
					{
						if("Y".equalsIgnoreCase(autoInvOnDesp))
						{
							String fromSaleOrder="", fromCustCode="", orderType="", siteCodeShip="";
							Timestamp fromDate = null;

							sql = " select d.sord_no,d.cust_code,so.order_type, d.desp_date, so.site_code__ship " +
							" from despatch d, sorder so " +
							" where d.desp_id = ? and d.sord_no = so.sale_order ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, tranId );
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								fromSaleOrder = checkNull(rs.getString("sord_no"));
								fromCustCode = checkNull(rs.getString("cust_code"));
								orderType = checkNull(rs.getString("order_type"));
								fromDate = rs.getTimestamp("desp_date");
								siteCodeShip = checkNull(rs.getString("site_code__ship"));

							}
							pstmt.close();
							pstmt = null;
							rs.close();
							rs = null;
							System.out.println("@@@@@@@@@ fromSaleOrder["+fromSaleOrder+"]fromCustCode["+fromCustCode+"]orderType["+orderType+"]fromDate["+fromDate+"]siteCodeShip["+siteCodeShip+"]");

							// setting default value like post order default
							String clubOrder = "",clubPendingOrd="",adjDrcr="",adjCustAdv="",advAdjMode="",adjNewProdInv="";
							//Boolean adjDrcrFlag= false, adjAdv=false, adjNewProdInvFlag=false;
							clubOrder = "N";
							clubPendingOrd="N";
							//adjDrcr="N";
							//adjCustAdv="N";
							//advAdjMode="C";
							//adjNewProdInv="N";
							
							//Modified by Azhar K. on [07-05-2019][Start]
							System.out.println("checking map additionalMap: "+additionalMap);
							if(additionalMap != null && additionalMap.size() > 0)
							{
								if(additionalMap.containsKey("ADJ_DRCR"))
								{
									adjDrcr = (String) additionalMap.get("ADJ_DRCR");
									 if(adjDrcr == null || adjDrcr.trim().length() == 0)
									 {
										 adjDrcr = "N"; 
									 }
								}
								if(additionalMap.containsKey("ADJ_CUST_ADV"))//CONDITION ADDED BY NANDKUMAR GADKARI 06/08/19
								{
									adjCustAdv = (String) additionalMap.get("ADJ_CUST_ADV");
									 if(adjCustAdv == null || adjCustAdv.trim().length() == 0)
									 {
										 adjCustAdv = "N"; 
									 }
								}
							}
							else
							{
								adjDrcr = checkNull(distCommon.getDisparams("999999","ADJUST_DR_CR_NOTE",conn));
								adjCustAdv = checkNull(distCommon.getDisparams("999999","ADJUST_CUST_ADV",conn)); //added by nandkumar gadkari on 06/08/19
							}
							//Modified by Azhar K. on [07-05-2019][End]
							//adjCustAdv = checkNull(distCommon.getDisparams("999999","ADJUST_CUST_ADV",conn)); //COMMENTED  by nandkumar gadkari on 06/08/19
							adjNewProdInv = checkNull(distCommon.getDisparams("999999","ADJUST_NEW_PRODUCT_INVOICE",conn));
							advAdjMode = checkNull(distCommon.getDisparams("999999","ADJ_ADV_CUST_SALE",conn));

							System.out.println("@@@@@adjDrcr["+adjDrcr+"]adjCustAdv["+adjCustAdv+"]adjNewProdInv["+adjNewProdInv+"]advAdjMode["+advAdjMode+"]");

							PostOrderProcess postOrderProcess = new PostOrderProcess();
							// fromSaleOrder,   toSaleOrder, fromCustCode,   toCustCode, tranIdDespatch,  orderType, fromDate, clubOrder, clubPendingOrd, adjDrcr, adjCustAdv, advAdjMode, adjNewProdInv, siteCodeShip, conn
							long startTime = System.currentTimeMillis();
							System.out.println("START TIME FOR POST ORDER PROCESS IN SECONDS:::["+startTime/1000+"]");

							//Modified by Anjali R. on [12/11/2018][Passed despatch confirm class object][Start]
							//errString= postOrderProcess.invPosting( fromSaleOrder, fromSaleOrder, fromCustCode, fromCustCode, tranId ,  orderType, fromDate, clubOrder, clubPendingOrd, adjDrcr, adjCustAdv, advAdjMode, adjNewProdInv, siteCodeShip,xtraParams, conn);
							System.out.println("@@@@@@@@@@@@ invPosting() called frm despactconf.........");
							errString= postOrderProcess.invPosting( fromSaleOrder, fromSaleOrder, fromCustCode, fromCustCode, tranId ,  orderType, fromDate, clubOrder, clubPendingOrd, adjDrcr, adjCustAdv, advAdjMode, adjNewProdInv, siteCodeShip,xtraParams,poRcpTranId, conn);
							//Modified by Anjali R. on [12/11/2018][Passed despatch confirm class object][End]
							
							long endTime = System.currentTimeMillis();
							System.out.println("END TIME FOR POST ORDER PROCESS IN SECONDS:::["+endTime/1000+"]");
							System.out.println("DIFFERANCE IN TIME FOR POST ORDER PROCESS IN SECONDS:::["+(endTime-startTime)/1000+"]for SALE_ORDER["+fromSaleOrder+"]");

							System.out.println("@@@@@@@@@689 invPosting() errString::::::["+errString+"]");
							if(errString.indexOf("Success") > -1)
							{
								errString = "";
							}
						}
					}
					System.out.println("@@@@@@@@@689 out:::::["+errString+"]");
					if( errString == null || errString.trim().length() == 0)
					{
						System.out.println("errCode in despatch posting===="+errString);
						System.out.println(">>The selected transaction is confirmed!!!!");
						//errString=	"<?xml version='1.0'?><Root><message>VTPOSTDES</message><TranID>"+tranId+"</TranID></errors></Root>";
						//errString="VTPOSTDES";
						errString = itmDBAccessEJB.getErrorString("","VTPOSTDES","","",conn);
						System.out.println("@@@@@ retString:[" + errString+"]");
					}
				}

				/*sql = "select confirmed from despatch where desp_id = ?";
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1,tranId);
		rs = pstmt.executeQuery();
		if(rs.next())
		{
			confirm = rs.getString("confirmed");
		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;
		System.out.println("@@@@@@@@@@@@test:::desp_id["+tranId+"]confirm["+confirm+"]");
				 */
			}

		}
		catch(Exception e)
		{
			try
			{
				conn.rollback();
			}
			catch (Exception e1)
			{
			}
			e.printStackTrace();
			System.out.println("Exception ::"+e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{


				if(pstmt1 != null)
				{
					pstmt1.close();
					pstmt1 = null;					
				}	
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;					
				}		
				/*if( conn != null && ! conn.isClosed() )
		{
			conn.close();
			conn = null;
		}*/
			}
			catch(Exception ef)
			{
				System.out.println(ef.getMessage());
				ef.printStackTrace();
				throw new ITMException(ef);
			}
		}
		return errString;
	}


	private String checkNull(String inp)
	{
		if(inp == null)
			inp = " ";
		return inp.trim();
	}
	//Added By PriyankaC to send the mail on invoice confirmation to customer on 16Oct2019.[START]
	private String sendMailonConfirm(String invoiceId, String fromCustCode , String templateCode ,UserInfoBean userInfo,Connection conn ) throws SQLException, ITMException
	{
		PreparedStatement pstmt=null;
		ResultSet rs = null;
		String SendEmailOnNotify = "",sql="";
		String errString = "";
		String toAddr = "",ccAddr = "",bccAddr = "",subject = "",body = "",templateName = "",attachObjLinks = "",attachments = "";
		
		sql = " select email_notify from customer where cust_code =  ? ";
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, fromCustCode);
		rs = pstmt.executeQuery();
		if (rs.next()) 
		{
			SendEmailOnNotify = checkNull(rs.getString("email_notify"));
		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;
		if("Y".equalsIgnoreCase(SendEmailOnNotify))
		{
			System.out.println("After confirm Calling SendEmail");
			sql = "select  SEND_TO ,COPY_TO ,BLIND_COPY ,SUBJECT , BODY_TEXT , MAIL_DESCR ,ATTACH_TEXT ,ATTACH_TYPE  from MAIL_FORMAT  WHERE FORMAT_CODE = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, templateCode);
			rs = pstmt.executeQuery();
			if (rs.next()) 
			{
				toAddr = checkNull(rs.getString("SEND_TO"));
				ccAddr = checkNull(rs.getString("COPY_TO"));
				bccAddr = checkNull(rs.getString("BLIND_COPY"));
				subject = checkNull(rs.getString("SUBJECT"));
				body = checkNull(rs.getString("BODY_TEXT"));
				templateName = checkNull(rs.getString("MAIL_DESCR"));
				attachments	 = checkNull(rs.getString("ATTACH_TEXT"));
				attachObjLinks = checkNull(rs.getString("ATTACH_TYPE"));
				//confirmed = checkNull(rs.getString("confirmed"));
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("Before confirm Calling SendEmail with attachment");
			errString =  TransactionEmailTempltEJB.sendMail("invoice",userInfo,toAddr, ccAddr,bccAddr,subject,body,"","",invoiceId,attachments,"true",templateName,templateCode,"");
			//errString =  TransactionEmailTempltEJB.sendMail("invoice",userInfo,toAddr, ccAddr,bccAddr,subject,body,"","",invoiceId,attachments,"",templateName,templateCode,"");
			System.out.println("After confirm Calling SendEmail with attachment" +errString);

			if( errString != null && errString.trim().length() > 0 )
			{
				String begPart = errString.substring(errString.indexOf("<STATUS>")+8,errString.indexOf("</STATUS>"));
				System.out.println("<STATUS> ::: " +begPart);
				if("N".equalsIgnoreCase(begPart))
				{
					return errString;
				}
				else
				{
					errString="";
				}
			}
		}
		//Added By PriyankaC to send the mail on invoice confirmation to customer on 16Oct2019.[End]
		return errString;
		
	}


}