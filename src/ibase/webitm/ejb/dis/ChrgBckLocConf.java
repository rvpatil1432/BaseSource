package ibase.webitm.ejb.dis;
import ibase.system.config.AppConnectParm;
import ibase.system.config.ConnDriver;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.EventManagerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.MasterStatefulLocal;
import ibase.webitm.ejb.dis.adv.SalesReturnConfirm;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.ejb.fin.MiscDrCrRcpConf;
import ibase.webitm.ejb.sys.UtilMethods;
import ibase.webitm.utility.ITMException;
import ibase.webitm.utility.TransIDGenerator;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.naming.InitialContext;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
//import ibase.webitm.ejb.MasterStateful;sd
//import ibase.webitm.ejb.MasterStatefulHome;
// added for ejb3

@Stateless // added for ejb3
public class ChrgBckLocConf extends ActionHandlerEJB implements ChrgBckLocConfLocal, ChrgBckLocConfRemote
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	String userId ="";
	String termId = "";
	String loginSite = "";
	String implMinRateHistory="";
	FinCommon finCommon = new FinCommon();
	PostOrderProcess postPrc= new PostOrderProcess();
	/* public void ejbCreate() throws RemoteException, CreateException
	{
		System.out.println("Create Method Called....");
	}
	public void ejbRemove()
	{
	}
	public void ejbActivate()
	{
	}
	public void ejbPassivate()
	{
	} */
	public String confirm() throws RemoteException,ITMException
	{
		System.out.println("confirm() Method Called....");
		return "";
	}
	public String confirm(String xmlString, String xtraParams, String objContext) throws RemoteException,ITMException
	{
		String  retString = null;
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		Connection conn = null;
		System.out.println("Xtra Params : " + xtraParams);
		//genericUtility = GenericUtility.getInstance();
		try
		{
			retString = actionConfirm(xmlString, xtraParams);
			System.out.println("returning from actionConfirm before "+retString);//added by kailasg on 17-feb-2021 for pop confirmation message
			if(retString == null || retString.trim().length() == 0)
			{
				System.out.println("returning from actionConfirm after"+retString);
				retString = itmDBAccessEJB.getErrorString("","CONFSUCCES",userId,"",conn);;
			}
			else 
			{
				return retString;
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :actionHandler(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning from  actionHandler"+retString);
		return (retString);
	}
	private String actionConfirm(String tranID, String xtraParams) throws RemoteException,ITMException, Exception
	{
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ResultSet rltSet = null;
		int cnt = 0;
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		ConnDriver connDriver = new ConnDriver();
		String sql = "";
		String siteCode = "";
		String custCode = "";
		String empCode = "";
		String confirmed = "";
		double netAmt = 0;  //Added for debit note creation
		userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
		loginSite = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
		empCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode");
		termId = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "termId" );
		java.sql.Timestamp currDate = null;
		java.sql.Timestamp tranDate = null;
		String returnString = "";
		boolean conFlag = false;
		String errStr = "";
		Timestamp sysDate = null;
		DistCommon disComm = new DistCommon();
		String autoConfirm="N";
		ArrayList<String> CRNxmlList=new ArrayList<String>();
		ArrayList<String> DRNxmlList=new ArrayList<String>();
		ArrayList<String> MDRCxmlList=new ArrayList<String>();
		ArrayList<String> MDRDxmlList=new ArrayList<String>();
		ArrayList<String> CRNIdList=new ArrayList<String>();
		ArrayList<String> DRNIdList=new ArrayList<String>();
		ArrayList<String> MDRCIdList=new ArrayList<String>();
		ArrayList<String> MDRDIdList=new ArrayList<String>();
		//HashMap<String,String> MDRCxmlMap=new HashMap<String,String>();
		//HashMap<String,String> MDRDxmlMap=new HashMap<String,String>();
		HashMap<String,ArrayList<String>> MDRCxmlMap=new HashMap<String,ArrayList<String>>();
		HashMap<String,ArrayList<String>> MDRDxmlMap=new HashMap<String,ArrayList<String>>();
		//HashMap<String,String> CRNxmlMap=new HashMap<String,String>();
		HashMap<String,ArrayList<String>> CRNxmlMap=new HashMap<String,ArrayList<String>>();
		HashMap<String,ArrayList<String>> DRNxmlMap=new HashMap<String,ArrayList<String>>();
		Document dom = null ;
		String generatedId="";
		String retString="";
		boolean isError=false,failValue=false ,failQty=false;
		Set mapSet=null;
		Iterator mapIterator=null;
		Map.Entry mapEntry=null;
		String keyString="";
		String valueString="";
		ArrayList<String> valueStringList=new ArrayList<String>();
		int countError = 0,count=0,updCnt=0;
		String chargeBakSql = "", secondarySchSql = "", schemeBalSql = "",drcrRcpSql="",customerSql="",customerSql1="",validUpto="";
		String custCode1 = "", itemCodeRepl = "", confDateStr = "", settleMethod = "", siteCode1 = "",custCode2="",siteCode2="",
				offer = "", vaildUpto = "",itemSer="";
		double freeQty = 0, freeVaule = 0; // chnage int to double by nandkumar gadkari on 1/10/19
		Map map1=new HashMap(); 
		Map map2=new HashMap(); 
		// added by nandkumar gadkari on 1/10/19
		PreparedStatement preparedStatement = null, preparedStatement1 = null;
		ResultSet resultSet = null;
		String schItemCodeRepl="",itemCode="",tranId="",schItemCode="",custCodeCredit="";
		double schFreeValue=0,freeValue=0,schQty=0,schFreeQty=0,quantity=0;
		//added by nandkumar gadkari on 17/04/20
		String siteType="",creatInvOthlist="",creatInvOth="",otherSite="",refDate="" ;
		try 
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection(); //Added by saiprasad on 22-Nov-18 for replacement form.
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
			currDate = new Timestamp( System.currentTimeMillis() );
			/*sql = "select tran_id, tran_date, site_code, cust_code, confirmed "
			  +"	from charge_back "
			  +" where tran_id = ? ";*/ // sql comment by sagar on 13/07/15
			System.out.println(">>>>CommonConstants.DB_NAME:"+ CommonConstants.DB_NAME);
			implMinRateHistory= disComm.getDisparams("999999", "IMPL_MIN_RATE_HISTORY", conn);
			if (implMinRateHistory.equalsIgnoreCase("NULLFOUND")|| implMinRateHistory.trim().length() == 0)
			{
				implMinRateHistory="N";
			}
			System.out.println("implMinRateHistory111["+implMinRateHistory+"]");
		//	item_ser and settle_mth added by nandkumar gadkari on 01/10/19
			if("db2".equalsIgnoreCase(CommonConstants.DB_NAME)) // conditions is added by sagar on 13/07/15 
			{
				sql = " select tran_id, tran_date, site_code, cust_code, confirmed,net_amt,item_ser,cust_code__credit,settle_mth " 
					+ " from charge_back where tran_id = ? for update ";

			}
			else if("mssql".equalsIgnoreCase(CommonConstants.DB_NAME))
			{
				sql = " select tran_id, tran_date, site_code, cust_code, confirmed,net_amt,item_ser,cust_code__credit,settle_mth "
					+ " from charge_back (updlock) where tran_id = ? ";

			}
			else 
			{
				System.out.println(">>>Oracle:");
				/*sql = " select tran_id, tran_date, site_code, cust_code, confirmed "
					+ " from charge_back where tran_id = ? for update nowait ";*/
				
				sql = " select tran_id, tran_date, site_code, cust_code, confirmed,net_amt,item_ser,cust_code__credit,settle_mth "
						+ " from charge_back where tran_id = ? for update nowait ";
			}
			System.out.println(">>>>ChrgBckLocConf sql:"+ sql);
			pstmt = conn.prepareStatement(sql);
			pstmt.setString( 1, tranID );
			rs = pstmt.executeQuery();
			if( rs.next() )
			{
				tranDate = rs.getTimestamp("tran_date");
				siteCode = rs.getString("site_code");
				custCode = rs.getString("cust_code");
				confirmed = rs.getString("confirmed");
				netAmt = rs.getDouble("net_amt"); //ADDED FOR AMOUNT COMPARISION FOR CREATION OF CR OR DR NOTE
				itemSer= rs.getString("item_ser");	//				item_ser added by nandkumar gadkari on 01/10/19
				custCodeCredit= rs.getString("cust_code__credit");	//				custCodeCredit added by nandkumar gadkari on 01/10/19
				settleMethod = rs.getString("Settle_Mth");	//				custCodeCredit added by nandkumar gadkari on 01/10/19
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if( confirmed != null && confirmed.trim().equals("Y") )
			{
				errStr = "TRNADYCONF";
				returnString = itmDBAccessEJB.getErrorString("",errStr,userId,"",conn);				
				return returnString;
			}
			/**
			 * VALLABH KADAM 22/JUN/15
			 * While verification
			 * If the Transaction is already 'CANCEL'
			 * that is STATUS='X'
			 * The Confirmed is not allow [VTCNCLCNFL]
			 * Req Id :- [D15BSUN003]
			 * */
			sql = " select  count(*) from charge_back where tran_id = ? and status = 'X'";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranID.trim());
			rs = pstmt.executeQuery();
			if(rs.next()) 
			{
				cnt  = rs.getInt(1);
			}if(rs != null)
				rs.close();
			rs = null;
			if(pstmt != null)
				pstmt.close();
			pstmt=null;
			if(cnt > 0)
			{
				errStr = "VTCNCLCNFL";
				returnString = itmDBAccessEJB.getErrorString("",errStr,userId,"",conn);
				return returnString;			    
			}

			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String sysDateStr = sdf.format(currentDate.getTime());
			System.out.println(">>>>>>>Now sysDateStr :=>  " + sysDateStr +"netAmt>>>"+netAmt);	
			sysDate= Timestamp.valueOf(genericUtility.getValidDateString(sysDateStr, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
			System.out.println(">>>>>>>>sysDate:"+sysDate);	
			// added by nandkumar gadkari  on 01/10/19--------------------start------------------
			if ("V".equalsIgnoreCase(settleMethod) || "Q".equalsIgnoreCase(settleMethod)) 
			{
				secondarySchSql = "SELECT ITEM_CODE__REPL,FREE_QTY,FREE_VALUE,OFFER ,ITEM_CODE,FREE_VALUE,QUANTITY FROM CHARGE_BACK_REPL WHERE TRAN_ID=?";
				preparedStatement1 = conn.prepareStatement(secondarySchSql);
				preparedStatement1.setString(1, tranID);
				rs = preparedStatement1.executeQuery();
				while (rs.next()) 
				{
					itemCodeRepl = (E12GenericUtility.checkNull(rs.getString("ITEM_CODE__REPL"))).trim();
					freeQty = rs.getDouble("FREE_QTY");
					freeVaule = rs.getDouble("FREE_VALUE");
					offer = E12GenericUtility.checkNull(rs.getString("OFFER"));
					quantity = rs.getDouble("QUANTITY");
	
					if (offer.length() <= 0) 
					{
						offer = "DEFAULT";
					}
					if (settleMethod.equalsIgnoreCase("V")) 
					{
						itemCodeRepl = "X";
					}
	
					
					failQty=false;
					failValue=false;
						
						itemCode = (E12GenericUtility.checkNull(rs.getString("ITEM_CODE"))).trim();
						freeValue = (rs.getDouble("FREE_VALUE"));
						sql = "select tran_id from scheme_apprv where aprv_status='A' and scheme_code=? "; 
						preparedStatement = conn.prepareStatement(sql);
						preparedStatement.setString(1,offer);
						resultSet = preparedStatement.executeQuery();
						if(resultSet.next())
						{
							tranId = resultSet.getString("tran_id");
						}
						resultSet.close();
						resultSet = null;
						preparedStatement.close();
						preparedStatement = null;
						
													
						
						sql = "SELECT ITEM_CODE,ITEM_CODE__REPL,QUANTITY,FREE_QTY,AMOUNT FROM SCHEME_APPRV_DET WHERE TRAN_ID = ? ";
						if(!"V".equalsIgnoreCase(settleMethod))
						{
							sql =sql + " AND ITEM_CODE= ?  "; 
						}
						preparedStatement = conn.prepareStatement(sql);
						preparedStatement.setString(1,tranId);
						if(!"V".equalsIgnoreCase(settleMethod))
						{
							preparedStatement.setString(2,itemCode);
						}
						resultSet = preparedStatement.executeQuery();
						if(resultSet.next())
						{
							countError = 0;
							schItemCode= (E12GenericUtility.checkNull(resultSet.getString(1))).trim();
							schItemCodeRepl = (E12GenericUtility.checkNull(resultSet.getString(2))).trim();
							schQty = resultSet.getDouble(3);
							schFreeQty = resultSet.getDouble(4);
							schFreeValue = resultSet.getDouble(5);
							
							if("Q".equalsIgnoreCase(settleMethod) && (!schItemCodeRepl.equalsIgnoreCase(itemCodeRepl) || schFreeQty !=freeQty )  )
							{
								failQty=true;
							}
							if("V".equalsIgnoreCase(settleMethod) && schFreeValue !=freeVaule )
							{
								failValue=true;
							}
							if(failValue || failQty )
							{
								
								sql  = "select COUNT(1) from BUSINESS_LOGIC_CHECK  where SALE_ORDER = ? and TRAN_TYPE = ? and APRV_STAT = ? ";
								
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, tranID);
								pstmt.setString(2, "C");
								pstmt.setString(3, "O");
								
								rltSet = pstmt.executeQuery();
								if(rltSet.next()) 
								{
									count=rltSet.getInt(1);
								}
								
								rltSet.close();
								rltSet=null;
								pstmt.close();
								pstmt=null;
								
								if(count <= 0)
								{	
									ArrayList BusinessLogicCheckList= new ArrayList();
									BusinessLogicCheckList.add(" "+"\t"+" "+"\t"+tranID+"\t"+"1"+"\t"+" "+"\t"+" "+"\t"+custCodeCredit+"\t"+custCode+"\t"+siteCode+"\t"+itemSer+"\t"+sysDateStr);  
									if(BusinessLogicCheckList.size() > 0)
									{						
										conn.rollback();
										
										countError = postPrc.writeBusinessLogicCheck(BusinessLogicCheckList, siteCode, "C", conn);
	
										if(countError  > 0)
										{
											conn.commit();
										}
										returnString =  itmDBAccessEJB.getErrorString("", "VTWBLGCCHK", "","",conn);
										
										return returnString;
									}
								}
								else
								{
									if(failQty )
									{
										sql  = "UPDATE SCHEME_APPRV_DET  SET  ITEM_CODE__REPL =? , FREE_QTY = ?,QUANTITY= ?  WHERE TRAN_ID = ? AND ITEM_CODE= ? ";
										
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, itemCodeRepl);
										pstmt.setDouble(2, freeQty);
										pstmt.setDouble(3, quantity);
										pstmt.setString(4, tranId);
										pstmt.setString(5, itemCode);
										updCnt=pstmt.executeUpdate();
										pstmt.close();
										pstmt=null;
									}
									if(failValue)
									{
										sql  = "UPDATE SCHEME_APPRV_DET  SET AMOUNT = ? WHERE TRAN_ID = ?  ";
										
										pstmt = conn.prepareStatement(sql);
										pstmt.setDouble(1, freeVaule);
										pstmt.setString(2, tranId);
										updCnt=pstmt.executeUpdate();
										pstmt.close();
										pstmt=null;
									}
								}
							
							}
						}
						resultSet.close();
						resultSet = null;
						preparedStatement.close();
						preparedStatement = null;
					
					
				}
				if(rs != null)
					rs.close();
				rs = null;
				preparedStatement1.close();
				preparedStatement1 = null;
			}
			// added by nandkumar gadkari  on 01/10/19--------------------end------------------
			sql = "update charge_back set "
				+" confirmed = 'Y', "
				+" VERIFY_FLAG = 'Y', "
				+" conf_date = ?, "
				+" emp_code__aprv = ?,"
				+ "STATUS='A',"//VALLABH KADAM STATUS='A' as APPROVE
				+ " STATUS_DATE=?" //VALLABH KADAM STATUS_DATE
				+" where tran_id = ? ";
			// +" and confirmed <> 'Y' ";
			System.out.println( "upd sql " + sql );
			pstmt = conn.prepareStatement( sql );
			pstmt.setTimestamp( 1, currDate );
			pstmt.setString( 2, empCode );
			pstmt.setTimestamp(3, sysDate);
			pstmt.setString( 4, tranID );
			int chkupdt = pstmt.executeUpdate();
			System.out.println("chkupdt==>"+ chkupdt);

			pstmt.close();
			pstmt = null;
			// 02/07/13 manoharan time value to be removed
			currDate = new java.sql.Timestamp(System.currentTimeMillis()) ;
			String currAppdate = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(currDate).toString();
			currDate = Timestamp.valueOf(genericUtility.getValidDateString( currAppdate, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0") ;
			
			autoConfirm = disComm.getDisparams( "999999", "GEN_CONF_DRCR_CB", conn );

			if( autoConfirm == null || autoConfirm.equalsIgnoreCase( "NULLFOUND" ) || autoConfirm.trim().length() == 0 )
			{
				autoConfirm="N";
			}
			
			// end 02/07/13 manoharan time value to be removed
			if( chkupdt > 0) // condition is added by sagar on 13/07/15 
			{
				if ("C".equalsIgnoreCase(settleMethod)) // added by nandkumar gadkari  on 01/10/19 for creditnote only
				{
						//Added by Tajuddin Mahadi on 10-OCT-2017-START
					double detNetAmt = 0, detInvNetAmt = 0;
					String detInvId = "";
					/*sql = "select sum(net_amt) as net_amt from charge_back_det where invoice_id is null and tran_id = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, tranID);
					
					rs = pstmt.executeQuery();
					if(rs.next()) {
						detNetAmt = rs.getDouble("net_amt");
					}
					rs.close();
					rs=null;
					pstmt.close();
					pstmt=null;*/
					
					//Added by Tajuddin Mahadi on 10-OCT-2017-END
					//if(netAmt > 0)
					/*Commented by manoj dtd 13/11/2017 to create Misc DR/CR amt based on detail amount
					 if(detNetAmt > 0)
					{
						returnString = createMiscCrnoteCback(currDate, "", siteCode, tranID, tranID, tranDate, tranDate, custCode, custCode, autoConfirm, xtraParams, conn );
						MDRCxmlList.add(returnString);
					}
					else if(detNetAmt < 0) // Added for Debit note generation if amt < 0: Start
					{
						returnString = createMiscDrnoteCback(currDate, "", siteCode, tranID, tranID, tranDate, tranDate, custCode, custCode, autoConfirm, xtraParams, conn );
						MDRDxmlList.add(returnString);
					}*/
					
					//MDRCxmlMap = createMiscCrnoteCback(currDate, "", siteCode, tranID, tranID, tranDate, tranDate, custCode, custCode, autoConfirm, xtraParams, conn );
					MDRCxmlMap.putAll(createMiscCrnoteCback(currDate, "", siteCode, tranID, tranID, tranDate, tranDate, custCode, custCode, autoConfirm, xtraParams, conn ));
					/*if(returnString.trim().length()>0)
					{
						MDRCxmlList.add(returnString);
					}*/
					
					
					//MDRDxmlMap = createMiscDrnoteCback(currDate, "", siteCode, tranID, tranID, tranDate, tranDate, custCode, custCode, autoConfirm, xtraParams, conn );
					MDRDxmlMap.putAll(createMiscDrnoteCback(currDate, "", siteCode, tranID, tranID, tranDate, tranDate, custCode, custCode, autoConfirm, xtraParams, conn ));
					/*if(returnString.trim().length()>0)
					{
						MDRDxmlList.add(returnString);
					}*/
					
					// Added for Debit note generation if amt < 0 :End
					
					sql = "select invoice_id, sum(net_amt) as net_amt from charge_back_det where invoice_id is not null and tran_id = ? and net_amt > 0 "
							+ " group by invoice_id";
					
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, tranID);
					
					rs = pstmt.executeQuery();
					while(rs.next())
					{
						detInvId = rs.getString("invoice_id");
						//CRNxmlMap = createCrNoteCback(currDate, "", siteCode, tranID, tranID, tranDate, tranDate, custCode, custCode, detInvId, autoConfirm, xtraParams, conn);
						CRNxmlMap.putAll(createCrNoteCback(currDate, "", siteCode, tranID, tranID, tranDate, tranDate, custCode, custCode, detInvId, autoConfirm, xtraParams, conn));
						/*returnString = createCrNoteCback(currDate, "", siteCode, tranID, tranID, tranDate, tranDate, custCode, custCode, detInvId, autoConfirm, xtraParams, conn);
						CRNxmlList.add(returnString);Commented by santosh*/
					}
					
					rs.close();
					rs=null;
					pstmt.close();
					pstmt=null;
					detInvId="";
					sql = "select invoice_id, sum(net_amt) as net_amt from charge_back_det where invoice_id is not null and tran_id = ? and net_amt < 0 "
							+ " group by invoice_id";
					
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, tranID);
					
					rs = pstmt.executeQuery();
					while(rs.next()) 
					{
							detInvId = rs.getString("invoice_id");
							//DRNxmlMap = createDrNoteCback(currDate, "", siteCode, tranID, tranID, tranDate, tranDate, custCode, custCode, detInvId, autoConfirm, xtraParams, conn);
							DRNxmlMap.putAll(createDrNoteCback(currDate, "", siteCode, tranID, tranID, tranDate, tranDate, custCode, custCode, detInvId, autoConfirm, xtraParams, conn));
							/*returnString = createDrNoteCback(currDate, "", siteCode, tranID, tranID, tranDate, tranDate, custCode, custCode, detInvId, autoConfirm, xtraParams, conn);
							DRNxmlList.add(returnString); Commented by santosh on 23/NOV/2017 */
					}
					
					rs.close();
					rs=null;
					pstmt.close();
					pstmt=null;
					
			
					
					System.out.println("MDRCxmlMap.size()["+MDRCxmlMap.size()+"]");
					System.out.println("MDRDxmlMap.size()["+MDRDxmlMap.size()+"]");
					System.out.println("CRNxmlMap.size()["+CRNxmlMap.size()+"]");
					System.out.println("DRNxmlMap.size()["+DRNxmlMap.size()+"]");
					
					if(MDRCxmlMap.size()>0)
					{
						 mapSet = (Set) MDRCxmlMap.entrySet();
				         mapIterator = mapSet.iterator();
				        while (mapIterator.hasNext()) 
				        {
				         mapEntry = (Map.Entry) mapIterator.next();
				         keyString = (String) mapEntry.getKey();
				         //valueString = (String) mapEntry.getValue();
				         valueStringList = (ArrayList<String>) mapEntry.getValue();
				        System.out.println("keyString["+keyString+"]valueString["+valueStringList+"]");
				        returnString="";
				        returnString = saveData(siteCode,keyString,xtraParams,conn);
				        System.out.println("@S@MDRCxmlMapreturnString["+returnString+"]");
				    	if (returnString.indexOf("Success") <= -1)
						{
							System.out.println("@@@@@@1: Failed"+returnString);
							isError=true;
							break;
							
						}
						else
						{
							System.out.println("@@@@@@2: Success"+returnString);
							isError=false;
							dom = genericUtility.parseString(returnString);
							System.out.println("dom>>>"+dom);
							generatedId = genericUtility.getColumnValue("TranID",dom);
							countError= 0;
							countError= updateIdinCB(generatedId,tranID,valueStringList,"M",conn);
							//if(countError == 0)
							if(countError < valueStringList.size())
							{
								System.out.println("@S@ inside misc credit ["+countError+"]");
								isError=true;
							}
							MDRCIdList.add(generatedId);
						}
				        
				        }
				 	}
					if(!isError)
					{
						if(MDRDxmlMap.size()>0)
						{
							 mapSet = (Set) MDRDxmlMap.entrySet();
					         mapIterator = mapSet.iterator();
					        while (mapIterator.hasNext()) 
					        {
					         mapEntry = (Map.Entry) mapIterator.next();
					         keyString = (String) mapEntry.getKey();
					         //valueString = (String) mapEntry.getValue();
					         valueStringList = (ArrayList<String>) mapEntry.getValue();
					        System.out.println("keyString["+keyString+"]valueString["+valueStringList+"]");
					        returnString="";
					        returnString = saveData(siteCode,keyString,xtraParams,conn);
					        System.out.println("@S@MDRDxmlMapreturnString["+returnString+"]");
					    	if (returnString.indexOf("Success") <= -1)
							{
								System.out.println("@@@@@@1: Failed"+returnString);
								isError=true;
								break;
								
							}
							else
							{
								System.out.println("@@@@@@2: Success"+returnString);
								isError=false;
								dom = genericUtility.parseString(returnString);
								System.out.println("dom>>>"+dom);
								generatedId = genericUtility.getColumnValue("TranID",dom);
								countError = 0;
								countError = updateIdinCB(generatedId,tranID,valueStringList,"M",conn);
								//if(countError == 0)
								if(countError < valueStringList.size())
								{
									System.out.println("@S@ inside misc debit ["+countError+"]");
									isError=true;
								}
								MDRDIdList.add(generatedId);
							}
					        
					        }
					 	}
					}
					//Commented by santosh on 23/NOV/2017
					//if(!isError)
	//				{
	//					for(int ctr=0;ctr<MDRDxmlList.size();ctr++)
	//					{
	//						
	//						returnString = saveData(siteCode,MDRDxmlList.get(ctr),xtraParams,conn);
	//						if (returnString.indexOf("Success") < -1)
	//						{
	//							System.out.println("@@@@@@1: Failed"+returnString);
	//							isError=true;
	//							break;
	//						}
	//						else
	//						{
	//							System.out.println("@@@@@@2: Success"+returnString);
	//							isError=false;
	//							dom = genericUtility.parseString(returnString);
	//							System.out.println("dom>>>"+dom);
	//							generatedId = genericUtility.getColumnValue("TranID",dom);
	//							MDRDIdList.add(generatedId);
	//							
	//							/*if( "Y".equalsIgnoreCase( autoConfirm )  )
	//								{
	//								dom = genericUtility.parseString(returnString);
	//								System.out.println("dom>>>"+dom);
	//								generatedId = genericUtility.getColumnValue("TranID",dom);
	//								MDRDIdList.add(generatedId);
	//									//retString = retrieveMiscDrcrRcp("drcrrcp_cr",drNtTranId,xtraParams,"N" );
	//									//retString = autoConfirmRecord("drcrrcp_cr", drNtTranId, xtraParams);
	//									retString = executeSystemEvent("misc_drcr_rcp_dr", "pre_confirm", generatedId, xtraParams, conn);
	//									System.out.println("retString ::: " + retString);
	//									if(retString.indexOf("VTSUCC1") > -1 || retString.indexOf("CONFSUCCES") > -1)
	//									{
	//										isError=false;
	//									}
	//									else
	//									{
	//										isError=true;
	//										break;
	//									}
	//									
	//								}*/
	//						}
	//					}	
	//				}
					/*if(!isError)
					{
						for(int ctr=0;ctr<CRNxmlList.size();ctr++)
						{
							
							returnString = saveData(siteCode,CRNxmlList.get(ctr),xtraParams,conn);
							if (returnString.indexOf("Success") < -1)
							{
								System.out.println("@@@@@@1: Failed"+returnString);
								isError=true;
								break;
							}
							else
							{
								System.out.println("@@@@@@2: Success"+returnString);
								isError=false;
								dom = genericUtility.parseString(returnString);
								System.out.println("dom>>>"+dom);
								generatedId = genericUtility.getColumnValue("TranID",dom);
								CRNIdList.add(generatedId);
								
								if( "Y".equalsIgnoreCase( autoConfirm )  )
									{
									dom = genericUtility.parseString(returnString);
									System.out.println("dom>>>"+dom);
									generatedId = genericUtility.getColumnValue("TranID",dom);
									CRNIdList.add(generatedId);
										//retString = retrieveMiscDrcrRcp("drcrrcp_cr",drNtTranId,xtraParams,"N" );
										//retString = autoConfirmRecord("drcrrcp_cr", drNtTranId, xtraParams);
										retString = executeSystemEvent("drcrrcp_cr", "pre_confirm", generatedId, xtraParams, conn);
										System.out.println("retString ::: " + retString);
										if(retString.indexOf("VTSUCC1") > -1 || retString.indexOf("CONFSUCCES") > -1)
										{
											isError=false;
										}
										else
										{
											isError=true;
											break;
										}
										
									}
							}
						}	
					}
	*/				
					if(!isError)
					{
						if(CRNxmlMap.size()>0)
						{
							mapSet = (Set) CRNxmlMap.entrySet();
							mapIterator = mapSet.iterator();
							while (mapIterator.hasNext()) 
							{
								mapEntry = (Map.Entry) mapIterator.next();
								keyString = (String) mapEntry.getKey();
								//valueString = (String) mapEntry.getValue();
								valueStringList = (ArrayList<String>) mapEntry.getValue();
								System.out.println("keyString["+keyString+"]valueString["+valueStringList+"]");
								returnString="";
								returnString = saveData(siteCode,keyString,xtraParams,conn);
								System.out.println("@S@CRNxmlMapreturnString["+returnString+"]");
								if (returnString.indexOf("Success") <= -1)
								{
									System.out.println("@@@@@@1: Failed"+returnString);
									isError=true;
									break;
	
								}
								else
								{
									System.out.println("@@@@@@2: Success"+returnString);
									isError=false;
									dom = genericUtility.parseString(returnString);
									System.out.println("dom>>>"+dom);
									generatedId = genericUtility.getColumnValue("TranID",dom);
									countError = 0;
									countError = updateIdinCB(generatedId,tranID,valueStringList,"I",conn);
									if(countError < valueStringList.size())
									{
										System.out.println("@S@ inside credit ["+countError+"]");
										isError=true;
									}
									CRNIdList.add(generatedId);
								}
	
							}
						}
					}
					if(!isError)
					{
						if(DRNxmlMap.size()>0)
						{
							mapSet = (Set) DRNxmlMap.entrySet();
							mapIterator = mapSet.iterator();
							while (mapIterator.hasNext()) 
							{
								mapEntry = (Map.Entry) mapIterator.next();
								keyString = (String) mapEntry.getKey();
								//valueString = (String) mapEntry.getValue();
								valueStringList = (ArrayList<String>) mapEntry.getValue();
								System.out.println("keyString["+keyString+"]valueString["+valueStringList+"]");
								returnString="";
								returnString = saveData(siteCode,keyString,xtraParams,conn);
								System.out.println("@S@DRNxmlMapreturnString["+returnString+"]");
								if (returnString.indexOf("Success") <= -1)
								{
									System.out.println("@@@@@@1: Failed"+returnString);
									isError=true;
									break;
	
								}
								else
								{
									System.out.println("@@@@@@2: Success"+returnString);
									isError=false;
									dom = genericUtility.parseString(returnString);
									System.out.println("dom>>>"+dom);
									generatedId = genericUtility.getColumnValue("TranID",dom);
									countError = 0;
									//countError = updateIdinCB(generatedId,tranID,valueString,conn);
									countError = updateIdinCB(generatedId,tranID,valueStringList,"I",conn);
									//if(countError == 0)
									if(countError < valueStringList.size())
									{
										System.out.println("@S@ inside debit ["+countError+"]");
										isError=true;
									}
									DRNIdList.add(generatedId);
								}
	
							}
						}
					}
				/*	Commented by santosh on 23/NOV/2017
				 * if(!isError)
					{
						for(int ctr=0;ctr<DRNxmlList.size();ctr++)
						{
							
							returnString = saveData(siteCode,DRNxmlList.get(ctr),xtraParams,conn);
							if (returnString.indexOf("Success") < -1)
							{
								System.out.println("@@@@@@1: Failed"+returnString);
								isError=true;
								break;
							}
							else
							{
								System.out.println("@@@@@@2: Success"+returnString);
								isError=false;
								dom = genericUtility.parseString(returnString);
								System.out.println("dom>>>"+dom);
								generatedId = genericUtility.getColumnValue("TranID",dom);
								DRNIdList.add(generatedId);
								
								if( "Y".equalsIgnoreCase( autoConfirm )  )
									{
									dom = genericUtility.parseString(returnString);
									System.out.println("dom>>>"+dom);
									generatedId = genericUtility.getColumnValue("TranID",dom);
									DRNIdList.add(generatedId);
										//retString = retrieveMiscDrcrRcp("drcrrcp_cr",drNtTranId,xtraParams,"N" );
										//retString = autoConfirmRecord("drcrrcp_cr", drNtTranId, xtraParams);
										retString = executeSystemEvent("drcrrcp_dr", "pre_confirm", generatedId, xtraParams, conn);
										System.out.println("retString ::: " + retString);
										if(retString.indexOf("VTSUCC1") > -1 || retString.indexOf("CONFSUCCES") > -1)
										{
											isError=false;
										}
										else
										{
											isError=true;
											break;
										}
										
									}
							}
						}
					
					}*/
					System.out.println("MDRCIdList["+MDRCIdList+"]");
					System.out.println("MDRDIdList["+MDRDIdList+"]");
					System.out.println("CRNIdList["+CRNIdList+"]");
					System.out.println("DRNIdList["+DRNIdList+"]");
					System.out.println("isError : ["+isError+"]");
			    }//end of  stm type  C added by nandkumar gadkari  on 01/10/19
				if(!isError)
				{
					if ("C".equalsIgnoreCase(settleMethod)) // added by nandkumar gadkari  on 01/10/19 for creditnote only
					{	
                        System.out.println("isError 1 : ["+isError+"]");
							generatedId="";
						if( "Y".equalsIgnoreCase( autoConfirm )  )
						{
                            System.out.println("autoConfirm  : ["+autoConfirm+"]");
							//conn.commit();commented by nandkumar gadkari on 20/04/20
							for(int idctr=0;idctr<MDRCIdList.size();idctr++)
							{
                                System.out.println("MDRCIdList : ["+MDRCIdList+"]");
								generatedId=MDRCIdList.get(idctr);
								//retString = executeSystemEvent("misc_drcr_rcp_cr", "pre_confirm", generatedId, xtraParams, conn);// Commented  and new object by nandkumar gadkari on 20/04/2020
								 MiscDrCrRcpConf confDebitNote = new MiscDrCrRcpConf();
								 retString= confDebitNote.confirm(generatedId,xtraParams, "" , conn);
								System.out.println("retString 1::: " + retString);
								if(retString.indexOf("VTSUCC1") > -1 || retString.indexOf("CONFSUCCES") > -1)
								{
									isError=false;
									//conn.commit();commented by nandkumar gadkari on 20/04/20
									//added by nandkumar gadkari on 17/04/20------------------Start-------------------
									 sql = " select site_type  from site where site_code = ? "; 
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, siteCode);
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											siteType= checkNullAndTrim(rs.getString(1));
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										
										
										creatInvOthlist = finCommon.getFinparams("999999", "ALOW_INV_OTH_SITE", conn); 
										
										if( "NULLFOUND".equalsIgnoreCase(creatInvOthlist) || creatInvOthlist == null)
										{
											creatInvOthlist="";
										}
										
										if(creatInvOthlist.trim().length() > 0)
										{
											String[] arrStr = creatInvOthlist.split(",");
											for (int i = 0; i < arrStr.length; i++) {
												creatInvOth = arrStr[i];
												System.out.println("creatInvOth>>>>>>>>" + creatInvOth);
												if(siteType.equalsIgnoreCase(creatInvOth.trim()))
												{
													otherSite = finCommon.getFinparams("999999", "INVOICE_OTHER_SITE", conn); 
													if( !"NULLFOUND".equalsIgnoreCase(creatInvOthlist) && creatInvOthlist != null && creatInvOthlist.trim().length() > 0)
													{
														SalesReturnConfirm salesReturnConfirm= new SalesReturnConfirm();
														retString=salesReturnConfirm.gbfAutoMiscCrnoteSreturnOth( generatedId,  otherSite ,  xtraParams,  conn);
														
														if( retString != null && retString.trim().length() > 0 )
														{
															isError=true;
															return retString;
														}
														
														
													}
												}
												
											}	
										} 
										//added by nandkumar gadkari on 17/04/20------------------end-------------------
									
								}
								else
								{
									isError=true;
									conn.rollback();
									//break;
								}
								
							}
							//if(!isError)
							//{
								for(int idctr=0;idctr<MDRDIdList.size();idctr++)
								{
									generatedId=MDRDIdList.get(idctr);
									retString = executeSystemEvent("misc_drcr_rcp_dr", "pre_confirm", generatedId, xtraParams, conn);
									System.out.println("retString 2::: " + retString);
									if(retString.indexOf("VTSUCC1") > -1 || retString.indexOf("CONFSUCCES") > -1)
									{
										isError=false;
										conn.commit();
									}
									else
									{
										isError=true;
										conn.rollback();
										//break;
									}
								}
							//}
							//if(!isError)
							//{
								for(int idctr=0;idctr<CRNIdList.size();idctr++)
								{
									generatedId=CRNIdList.get(idctr);
									retString = executeSystemEvent("drcrrcp_cr", "pre_confirm", generatedId, xtraParams, conn);
									System.out.println("retString 3::: " + retString);
									if(retString.indexOf("VTSUCC1") > -1 || retString.indexOf("CONFSUCCES") > -1)
									{
										isError=false;
										conn.commit();
									}
									else
									{
										isError=true;
										conn.rollback();
										break;
									}
								}
							//}
							//if(!isError)
							//{
								for(int idctr=0;idctr<DRNIdList.size();idctr++)
								{
									generatedId=DRNIdList.get(idctr);
									retString = executeSystemEvent("drcrrcp_dr", "pre_confirm", generatedId, xtraParams, conn);
									System.out.println("retString 4::: " + retString);
									if(retString.indexOf("VTSUCC1") > -1 || retString.indexOf("CONFSUCCES") > -1)
									{
										isError=false;
										conn.commit();
									}
									else
									{
										isError=true;
										conn.rollback();
										//break;
									}
								}
							//}
						}
                        return retString;   //added by manish mhatre on 18-sep-20
				}
					// Added by saiprasad G.START [When the settlement method is "value replacement"
					// & "quantity replacement" in charge back then data inserted into scheme
					// balance table]
					// For the charge_back
					int updatedCount = 0;
					System.out.println("tranId::[" + tranID + "]");
					//chargeBakSql = "SELECT CUST_CODE, SETTLE_MTH, SITE_CODE FROM CHARGE_BACK WHERE TRAN_ID=?";
					chargeBakSql = "SELECT CUST_CODE, SETTLE_MTH FROM CHARGE_BACK WHERE TRAN_ID=?";
					/*PreparedStatement preparedStatement = null, preparedStatement1 = null; 
					ResultSet resultSet = null;*///commented by nandkumar gadkari on 1/10/19
					preparedStatement = conn.prepareStatement(chargeBakSql);
					preparedStatement.setString(1, tranID);
					resultSet = preparedStatement.executeQuery();
					if (resultSet.next()) {
						custCode1 = resultSet.getString("CUST_CODE");
						settleMethod = resultSet.getString("SETTLE_MTH");
						//siteCode1 = resultSet.getString("SITE_CODE");
					}
					if(resultSet != null)
					{
						resultSet.close();
						resultSet = null;
					}
					if(preparedStatement != null)
					{
						preparedStatement.close();
						preparedStatement = null;
					}
					
					customerSql1="SELECT SITE_CODE__PBUS FROM CUSTOMER WHERE CUST_CODE=?";
					preparedStatement=conn.prepareStatement(customerSql1);
					preparedStatement.setString(1, custCode1);
					resultSet=preparedStatement.executeQuery();
					while(resultSet.next())
					{
						siteCode2=resultSet.getString("SITE_CODE__PBUS");
					}
					if(resultSet != null)
					{
						resultSet.close();
						resultSet = null;
					}
					if(preparedStatement != null)
					{
						preparedStatement.close();
						preparedStatement = null;
					}
					
					customerSql="SELECT CUST_CODE FROM CUSTOMER WHERE SITE_CODE=? AND CHANNEL_PARTNER=?";
					preparedStatement=conn.prepareStatement(customerSql);
					//preparedStatement.setString(1, siteCode1);
					preparedStatement.setString(1, siteCode2);
					preparedStatement.setString(2, "Y");
					resultSet=preparedStatement.executeQuery();
					if(resultSet.next())
					{
						custCode2=resultSet.getString("CUST_CODE");
					}
					if(resultSet != null)
					{
						resultSet.close();
						resultSet = null;
					}
					if(preparedStatement != null)
					{
						preparedStatement.close();
						preparedStatement = null;
					}
					/*customerSql1="SELECT SITE_CODE__PBUS FROM CUSTOMER WHERE CUST_CODE=?";
					preparedStatement=conn.prepareStatement(customerSql1);
					preparedStatement.setString(1, custCode2);
					resultSet=preparedStatement.executeQuery();
					while(resultSet.next())
					{
						siteCode2=resultSet.getString("SITE_CODE__PBUS");
					}
					if(resultSet != null)
					{
						resultSet.close();
						resultSet = null;
					}
					if(preparedStatement != null)
					{
						preparedStatement.close();
						preparedStatement = null;
					}*/
					// For the charge_back_repl
					secondarySchSql = "SELECT ITEM_CODE__REPL,FREE_QTY,FREE_VALUE,OFFER ,ITEM_CODE,FREE_VALUE FROM CHARGE_BACK_REPL WHERE TRAN_ID=?";// ITEM_CODE AND FREE_VALUE ADDED BY NANDKUMAR GADKARI ON 1/10/19
					preparedStatement1 = conn.prepareStatement(secondarySchSql);
					preparedStatement1.setString(1, tranID);
					rs = preparedStatement1.executeQuery();
					while (rs.next()) 
					{
						itemCodeRepl = (E12GenericUtility.checkNull(rs.getString("ITEM_CODE__REPL"))).trim();
						freeQty = rs.getDouble("FREE_QTY");// chnage int to double by nandkumar gadkari on 1/10/19
						freeVaule = rs.getDouble("FREE_VALUE");// chnage int to double by nandkumar gadkari on 1/10/19
						offer = E12GenericUtility.checkNull(rs.getString("OFFER"));

						if (offer.length() <= 0) 
						{
							offer = "DEFAULT";
						}
						if (settleMethod.equalsIgnoreCase("V")) 
						{
							itemCodeRepl = "X";
						}

						// effective from
						SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
						Date confDate = new Date();
						confDateStr = sdf1.format(confDate);
						Timestamp effFrom1 = new Timestamp(confDate.getTime());
						System.out.println("eff from is :" + effFrom1 + " confDate str is : " + confDateStr);

						/*Calendar calObj1 = Calendar.getInstance();
						calObj1.setTime(new java.util.Date());
						calObj1.add(Calendar.YEAR, 2);
						java.util.Date dateFrom = calObj1.getTime();
						Timestamp vaildUpto1 = new Timestamp(dateFrom.getTime());
                        Date validUptoDate=new Date(vaildUpto1.getTime());
                        validUpto=sdf1.format(validUptoDate);*/	
						Timestamp vaildUpto1 = null;
						String schemeApprSql = "select VALID_UPTO from SCHEME_APPRV where SCHEME_CODE=?";
						preparedStatement = conn.prepareStatement(schemeApprSql);
						preparedStatement.setString(1, offer);
						resultSet = preparedStatement.executeQuery();
						while(resultSet.next())
						{
							vaildUpto1 = resultSet.getTimestamp("VALID_UPTO");
							System.out.println("scheme apprv date:"+vaildUpto1);
						}
						Date validUptoDate=new Date(vaildUpto1.getTime());
                        validUpto=sdf1.format(validUptoDate);
                        if(resultSet != null)
                        {
                        	resultSet.close();
                        	resultSet=null;
                        }
                        if(preparedStatement != null)
                        {
                        	preparedStatement.close();
                        	preparedStatement=null;
                        }
                       	// insert into scheme balance
                        
						System.out.println("settle method2::[" + settleMethod + "]");
						if (settleMethod.equalsIgnoreCase("V") || settleMethod.equalsIgnoreCase("Q")) 
						{
							int noOfRecord = 0;
							String recordExistSql = "SELECT COUNT(1) FROM SCHEME_BALANCE WHERE CUST_CODE = ? AND ITEM_CODE = ? AND SITE_CODE = ? AND SCHEME_CODE = ? ";//Added by Vikas L on 31-12-18 
							preparedStatement = conn.prepareStatement(recordExistSql);
							preparedStatement.setString(1, custCode1);
							preparedStatement.setString(2, itemCodeRepl);
							preparedStatement.setString(3, siteCode1);
							preparedStatement.setString(4,offer);//Added by Vikas L on 31-12-18 
							resultSet = preparedStatement.executeQuery();
							if(resultSet.next())
							{
								noOfRecord = resultSet.getInt(1);
							}
							resultSet.close();
							preparedStatement.close();
							
							String freeQty1=String.valueOf(freeQty);
							String freeValue1=String.valueOf(freeVaule);
							
							if(noOfRecord <= 0)
							{
								//Added by saiprasad on 17-JAN-19 for replacement form. [START]
								map1=getStockiestData(offer, custCode1, itemCodeRepl, confDateStr, validUpto, freeQty1, freeValue1, "0", "0", userId, confDateStr, termId, "A", siteCode1);
								insertDataInSchemeBal(map1);
								map2=getStockiestData(offer, custCode2, itemCodeRepl, confDateStr, validUpto, freeQty1, freeValue1, "0", "0", userId, confDateStr, termId, "A", siteCode2);
								insertDataInSchemeBal(map2);
								//Added by saiprasad on 17-JAN-19 for replacement form. [END]
							}
							else
							{
								double freeBalQty = 0, freeBalVal = 0; // chnage int to double by nandkumar gadkari on 1/10/19
								String getBalanceItem = "SELECT BALANCE_FREE_QTY, BALANCE_FREE_VALUE from SCHEME_BALANCE WHERE CUST_CODE = ? AND ITEM_CODE = ? AND SCHEME_CODE = ? and SITE_CODE = ?";
								preparedStatement = conn.prepareStatement(getBalanceItem);
								preparedStatement.setString(1, custCode1);
								preparedStatement.setString(2, itemCodeRepl);
								preparedStatement.setString(3, offer);
								preparedStatement.setString(4, siteCode1);
								resultSet = preparedStatement.executeQuery();
								if(resultSet.next())
								{
									freeBalQty = resultSet.getDouble("BALANCE_FREE_QTY");
									freeBalVal = resultSet.getDouble("BALANCE_FREE_VALUE");
								}
								resultSet.close();
								preparedStatement.close();
								
								freeBalQty = freeBalQty + freeQty;
								freeBalVal = freeBalVal + freeVaule;
								
								String updateScheme = "UPDATE SCHEME_BALANCE SET EFF_FROM = ?, VALID_UPTO = ?, BALANCE_FREE_QTY = ?, BALANCE_FREE_VALUE = ? WHERE CUST_CODE = ? "
										+ "AND ITEM_CODE = ? AND SCHEME_CODE = ? and SITE_CODE = ?";//Added By Vikas L on 7-1-19
								PreparedStatement pStmt = conn.prepareStatement(updateScheme);
								pStmt.setTimestamp(1, effFrom1);
								pStmt.setTimestamp(2, vaildUpto1);
								pStmt.setDouble(3, freeBalQty);
								pStmt.setDouble(4, freeBalVal);
								pStmt.setString(5, custCode1);
								pStmt.setString(6, itemCodeRepl);
								pStmt.setString(7, offer);
								pStmt.setString(8, siteCode1);//Added By Vikas L on 7-1-19
								int updatedCnt = pStmt.executeUpdate();
								System.out.println(">>>>> updatedCnt : "+updatedCnt);
								pStmt.close();
								if(updatedCnt > 1)
								{
									conn.commit();
								}
								
								String updateScheme1 = "UPDATE SCHEME_BALANCE SET EFF_FROM = ?, VALID_UPTO = ?, BALANCE_FREE_QTY = ?, BALANCE_FREE_VALUE = ? WHERE CUST_CODE = ? "
										+ "AND ITEM_CODE = ? AND SCHEME_CODE = ? and SITE_CODE = ?";//Added By Vikas L on 7-1-19
								PreparedStatement pStmt1 = conn.prepareStatement(updateScheme1);
								pStmt1.setTimestamp(1, effFrom1);
								pStmt1.setTimestamp(2, vaildUpto1);
								pStmt1.setDouble(3, freeBalQty);
								pStmt1.setDouble(4, freeBalVal);
								pStmt1.setString(5, custCode2);
								pStmt1.setString(6, itemCodeRepl);
								pStmt1.setString(7, offer);
								pStmt1.setString(8, siteCode2);//Added By Vikas L on 7-1-19
								int updatedCnt1 = pStmt1.executeUpdate();
								System.out.println(">>>>> updatedCnt1 : "+updatedCnt1);
								pStmt.close();
								if(updatedCnt1 > 1)
								{
									conn.commit();
								}
							}
						}
						//Added by Saiprasad G. on 24-JAN-19 START [For calculating used amount and amount of scheme approval]
						double amt = 0, usedAmt = 0,usedAmtTrace=0,balanceAmt=0;// change int to double by nandkumar gadkari on 01/10/19
						double updatedFreeAmt = freeVaule; // change int to double by nandkumar gadkari on 01/10/19
						String apprvTranID = "";
						PreparedStatement updateApprvPstmt = null;
						String getApprvData = "SELECT TRAN_ID,AMOUNT,USED_AMT FROM SCHEME_APPRV WHERE SCHEME_CODE = ? AND CUST_CODE__BILL = ? AND APRV_STATUS = ? and AMOUNT > 0";
						pstmt = conn.prepareStatement(getApprvData);
						System.out.println("query:"+getApprvData);
						pstmt.setString(1, offer);
						pstmt.setString(2, custCode1);
						pstmt.setString(3, "A");
						System.out.println("offer"+offer);
						resultSet = pstmt.executeQuery();
						System.out.println("Custcode1"+custCode1);
						while(resultSet.next())
						{
							System.out.println("Custcode1"+custCode1);
							apprvTranID = resultSet.getString("TRAN_ID");
							amt = resultSet.getDouble("AMOUNT");
							usedAmt = resultSet.getDouble("USED_AMT");
							System.out.println("usedAmt : "+usedAmt);
							System.out.println("apprvTranID : "+apprvTranID);
							System.out.println("Amount  : "+amt);
							System.out.println("updatedFreeAmt  : "+updatedFreeAmt);
							String updateApprv = "update SCHEME_APPRV set AMOUNT = ?, USED_AMT = ? where TRAN_ID = ?";
							if(updatedFreeAmt > 0)
							{
								System.out.println("ChrgBckLocConf.actionConfirm() : updatedFreeAmt > 0 ");
								System.out.println("After calcul : "+updatedFreeAmt);
								if(updatedFreeAmt >= amt)
								{
									usedAmtTrace=amt+usedAmt;
									System.out.println("usedAmt1:"+usedAmtTrace);
									System.out.println("updatedFreeAmt >= amt ");
									updatedFreeAmt = updatedFreeAmt - amt;
									updateApprvPstmt = conn.prepareStatement(updateApprv);
									updateApprvPstmt.setDouble(1, 0);
									updateApprvPstmt.setDouble(2,usedAmtTrace);
									updateApprvPstmt.setString(3, apprvTranID);
									int partialUpd = updateApprvPstmt.executeUpdate();
									System.out.println("partialUpd :: "+partialUpd);
									insertSchemeTrace(tranDate, apprvTranID, custCode1, offer,usedAmtTrace,0);
								}
								else
								{
									System.out.println("Else of updatedFreeAmt >= amt");
									updatedFreeAmt = Math.abs(updatedFreeAmt);
									double chgAmt = amt - updatedFreeAmt;
									balanceAmt=updatedFreeAmt + usedAmt;
									updateApprvPstmt = conn.prepareStatement(updateApprv);
									updateApprvPstmt.setDouble(1, chgAmt);
									updateApprvPstmt.setDouble(2, balanceAmt);
									updateApprvPstmt.setString(3, apprvTranID);
									int fullUpda = updateApprvPstmt.executeUpdate();
									System.out.println("fullUpda :: "+fullUpda);
									updatedFreeAmt = 0;
									insertSchemeTrace(tranDate, apprvTranID, custCode1, offer,balanceAmt,chgAmt);
								}
								if(updateApprvPstmt != null)
								{
									updateApprvPstmt.close();
									updateApprvPstmt = null;
								}	
							}
						}
						if(resultSet != null)
						{
							resultSet.close();
							resultSet = null;
						}
						if(pstmt != null)
						{
							pstmt.close();
							pstmt = null;
						}
						//Added by Saiprasad G. on 24-JAN-19 END [For calculating used amount and amount of scheme approval]
					}
					if(rs != null)
					{
						rs.close();
						rs = null;
					}
					if(preparedStatement1 != null)
					{
						preparedStatement1.close();
						preparedStatement1 = null;
					}
					//Added by Saiprasad G. END
					
					//returnString = itmDBAccessEJB.getErrorString("","TRANCONFSC",userId); commented and added by kkg on 11-jan-2021 for confirm  message not occur on screen 
					returnString = itmDBAccessEJB.getErrorString("","TRANCONFSC",userId,"",conn);
				}
				else
				{
					returnString = itmDBAccessEJB.getErrorString("","VTTRNCNFM2",userId,"",conn);	
				}
			}
			else
			{
				System.out.println(">>>Record not updated for charge_back:");
				returnString = itmDBAccessEJB.getErrorString("", "VTTRNCNFM2", userId,"",conn);
			}
		}
		catch(Exception e)
		{
			System.out.println("ChrgBckLocConfEJB..."+e);
			e.printStackTrace();
			try
			{
				conn.rollback();
			}
			catch(Exception e1)
			{
				System.out.println("ChrgBckLocConfEJB..."+e1);
				e1.printStackTrace();
			}
			throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
		}
		finally
		{
			implMinRateHistory=null;
			try
			{
				System.out.println("isError:::::::"+isError);
				if(!isError)
				{
					System.out.println("Commiting connection.......");
					conn.commit();
				}
				else
				{
					System.out.println("Connection Rollback.......");
					conn.rollback();
				}
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if(conn != null)
				{
					conn.close();
					conn = null;
				}
			}
			catch(Exception e)
			{
				System.out.println("Exception ChrgBckLocConfEJB....... :\n"+e.getMessage());
			}
		}
		System.out.println("retString 5 ::"+returnString);
		return returnString;
	}
	private int updateIdinCB(String generatedId, String tranID,ArrayList<String> valueStringList,String drcrStr,Connection conn) throws ITMException,RemoteException
	{
		String sql="";
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		int updCnt=0;
		int totUpdCnt=0;//Changes made by ashutosh on 31-05-2018 
		String lineReference="";
		String[] lineReferenceArr=null;
		String cblineNo="";
		String drcrLineNo="";
		String refSrNo="",lineNoSrNo="",docKey="";
		try 
		{
			for(int i=0;i<valueStringList.size();i++)
			{
				lineReference=valueStringList.get(i);
				lineReferenceArr=lineReference.split(":");
				cblineNo=lineReferenceArr[0];
				drcrLineNo=lineReferenceArr[1];
				sql="update charge_back_det set tran_id__crn= ?,line_no__crn=? where tran_id= ? and line_no=?";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1, generatedId);
				pstmt.setString(2, drcrLineNo);
				pstmt.setString(3, tranID);
				pstmt.setString(4, cblineNo);
				updCnt=pstmt.executeUpdate();
				pstmt.close();
				pstmt=null;
				//updCnt++;
				totUpdCnt++; //Changes made by ashutosh on 31-05-2018 
				System.out.println("@S@ updCnt :- ["+updCnt+"]");
				System.out.println("@S@ totUpdCnt :- ["+totUpdCnt+"]");
				/*if("Y".equalsIgnoreCase(implMinRateHistory))
				{
				if("M".equalsIgnoreCase(drcrStr))
				{
					sql="select REF_NO__SRN,LINE_NO__SRN from charge_back_det" +
							" where tran_id=? and line_no=? ";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, tranID);
					pstmt.setString(2, cblineNo);
					rs=pstmt.executeQuery();
					while(rs.next())
					{
						refSrNo=checkNull(rs.getString("REF_NO__SRN"));
						lineNoSrNo=checkNull(rs.getString("LINE_NO__SRN"));						
					}
					rs.close();
					rs=null;
					pstmt.close();
					pstmt=null;
					pstmt=conn.prepareStatement("select doc_key from SRETURNDET where tran_id=? and line_no=? ");
					pstmt.setString(1,refSrNo);
					pstmt.setString(2, lineNoSrNo);
					rs=pstmt.executeQuery();
					rs=pstmt.executeQuery();
					while(rs.next())
					{
						docKey=checkNull(rs.getString("doc_key"));
												
					}
					rs.close();
					rs=null;
					pstmt.close();
					pstmt=null;
					sql="update MISC_DRCR_RDET set INVOICE_REF= ? where tran_id= ? and line_no=?";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, docKey);
					pstmt.setString(2, generatedId);
					pstmt.setString(3, drcrLineNo);
					pstmt.executeUpdate();
					pstmt.close();
					pstmt=null;
					
					
				}
				}*/
			}
			
		} 
		catch(Exception e)
		{
			System.out.println("Exception :" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				
			}
			catch(Exception e)
			{
				System.out.println("Exception \n"+e.getMessage());
			}
		}
		
		
		return totUpdCnt;//Changes made by ashutosh on 31-05-2018 
	}
	// commented by santosh on 23/NOV/2017
	//private String createDrNoteCback(Timestamp currDate, String str, String siteCode, String tranIdFr, String tranIdTo, Timestamp tranDateFr, Timestamp tranDateTo, String custCodeFr, String custCodeTo, String detInvId, String confirm, String xtraParams, Connection conn ) throws RemoteException,ITMException
	private HashMap<String,ArrayList<String>> createDrNoteCback(Timestamp currDate, String str, String siteCode, String tranIdFr, String tranIdTo, Timestamp tranDateFr, Timestamp tranDateTo, String custCodeFr, String custCodeTo, String detInvId, String confirm, String xtraParams, Connection conn ) throws RemoteException,ITMException
	{
		System.out.println("EJB ["+this.getClass().getSimpleName() + "] method [createDrNoteCback] - START");
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		PreparedStatement pstmt2 = null;
		PreparedStatement pstmtHdr = null;
		PreparedStatement pstmtDtl = null;
		ResultSet rs = null;
		Statement stmt = null;
		ResultSet rSet = null;
		ResultSet rSet1 = null;
		String sql = "", innerSql = "";
		String custCodeCr = "";
		String currCode = "";
		String finEntity = "";
		String acctCode = "";
		String cctrCode = "";
		String empCodeAprv = "";
		String tranIdSel = "";
		String pOrderNo = "";
		String siteCodeCr = "";
		String itemSer = "";
		String remarks = "";
		String detAcct = "";
		String errCode = "";
		String retString = "";
		String detCctr = "";
		String tranType = "";
		String reasonCd = "";
		String drNtTranId = "";
		String acctCodeTax = "";
		String cctrCodeTax = "";
		String round = "";
		String insDtlsql = "";
		String asPost = confirm;
		double roundTo = 0.0;
		double exchgRate = 0.0;
		double netAmt = 0.0;
		double claimAmt = 0.0;
		double grossAmount = 0.0;
		double drcrAmt = 0.0;
		double amtTax = 0.0;
		double total = 0.0;
		double roundAmt = 0.0;
		double diffAmt = 0.0;
		double discountAmt = 0.0 ,taxAmt = 0.0;
		int llLineNo = 0;
		java.sql.Timestamp tranDate = null;
		java.sql.Timestamp pOrderDate = null;
		
		StringBuffer xmlBuff = null;
		StringBuffer xmlBuffDet = null;
		String xmlString="",errString = "",taxClass = "",taxEnv = "",taxChap = "",analCode = "" ;
		Document dom = null ;
		String invId="",itemCode="";
		double invNetAmt=0.0;
		int invLineNo=0;
		double rateDiff=0.0;
		double cBackQty=0;
		String lotNo="",lotSl="";
		double rateClg=0;
		int invLineNo1=0;
		ArrayList diffAmtList = null;
		DistCommon disComm = new DistCommon();
		int countData=0;
		HashMap<String,ArrayList<String>> dataMap=new HashMap<String, ArrayList<String>>();
		String detlineNo="";
		String lineNo="";
		ArrayList<String> detLineNoList=new ArrayList<String>();
		try
		{	
			diffAmtList = new ArrayList();
			xmlBuff = new StringBuffer();		
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
			xmlBuff.append("<DocumentRoot>");
			xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
			xmlBuff.append("<group0>");
			xmlBuff.append("<description>").append("Group0 description").append("</description>");
			xmlBuff.append("<Header0>");
			xmlBuff.append("<objName><![CDATA[").append("drcrrcp_dr").append("]]></objName>");  
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
			xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"drcrrcp_dr\" objContext=\"1\">");  
			xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
			xmlBuff.append("<tran_id/>");	
			
			xmlBuffDet = new StringBuffer();
			sql = "select a.cust_code__credit, a.curr_code, a.exch_rate, a.emp_code__aprv, a.tran_id, b.net_amt,a.tax_amt, a.porder_no, "
					+ " a.porder_date, a.claum_amt, a.site_code__cr, a.item_ser, b.amount, a.remarks, b.discount_amt as discAmt, "
					+ " b.tax_class as tax_class,b.tax_chap as tax_chap,b.tax_env as tax_env, b.inv_line_no, b.line_no "
					+ " from charge_back a, charge_back_det b where a.tran_id = b.tran_id and a.tran_id = ? and b.invoice_id = ?  and b.net_amt < 0";
					/*+ " group by a.cust_code__credit, a.curr_code, a.exch_rate, a.emp_code__aprv, a.tran_id, b.net_amt, a.tax_amt, "
					+ " a.porder_no, a.porder_date, a.claum_amt, a.site_code__cr, a.item_ser, a.amount, a.remarks, b.tax_class, b.tax_chap, b.tax_env, b.inv_line_no";*/
			
			pstmt = conn.prepareStatement( sql );
			pstmt.setString( 1, tranIdFr );
			pstmt.setString( 2, detInvId );

			rs = pstmt.executeQuery();
			boolean isHeaderCreated = false;
			while( rs.next() )
			{
				countData++;//added by santosh on 23/NOV/2017
				custCodeCr = rs.getString("cust_code__credit");
				currCode = rs.getString("curr_code");
				exchgRate = rs.getDouble("exch_rate");
				empCodeAprv = checkNull(rs.getString("emp_code__aprv"));
				tranIdSel = rs.getString("tran_id");
				netAmt = rs.getDouble("net_amt");
				taxAmt = rs.getDouble("tax_amt");
				pOrderNo = rs.getString("porder_no");
				pOrderDate = rs.getTimestamp("porder_date");
				claimAmt = rs.getDouble("claum_amt");
				siteCodeCr = rs.getString("site_code__cr");
				itemSer = rs.getString("item_ser");
				grossAmount = rs.getDouble("amount");
				remarks = rs.getString("remarks");
				discountAmt = rs.getDouble("discAmt");
				taxClass = rs.getString("tax_class");
				taxChap = rs.getString("tax_chap");
				taxEnv = rs.getString("tax_env");
				//added by santosh on 23/NOV/2017
				lineNo =rs.getString("line_no");
				detlineNo+=lineNo+",";
				System.out.println("@S@ inside debit note["+detlineNo+"]");
				System.out.println("taxClass==>["+taxClass+"]");
				System.out.println("taxChap==>["+taxChap+"]");
				System.out.println("taxEnv==>["+taxEnv+"]");
				if( siteCodeCr == null || siteCodeCr.trim().length() == 0 )
				{
					siteCodeCr = siteCode;
				}

				sql = "select fin_entity from site where site_code = ?" ;
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, siteCodeCr.trim() );
				rSet = pstmt1.executeQuery();
				if( rSet.next() )
				{
					finEntity = rSet.getString( "fin_entity" );
				}
				rSet.close();
				rSet = null;
				pstmt1.close();
				pstmt1 = null;

				sql = "select acct_code__ar, cctr_code__ar from customer where cust_code = ? " ;
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, custCodeCr.trim() );
				rSet = pstmt1.executeQuery();
				if( rSet.next() )
				{
					acctCode = rSet.getString( "acct_code__ar" );
					cctrCode = rSet.getString( "cctr_code__ar" );
				}
				rSet.close();
				rSet = null;
				pstmt1.close();
				pstmt1 = null;
				// 21/07/10 manoharan make cctr_code blank in case null
				if (cctrCode == null)
				{
					cctrCode = "     ";
				}
				// end 21/07/10
				detAcct = disComm.getDisparams( "999999", "DRCR_ACCT_CBACK", conn );

				if( detAcct == null || detAcct.equalsIgnoreCase( "NULLFOUND" ) || detAcct.trim().length() == 0 )
				{
					errCode = "VMDRCRACCT"; //'DS000' + string(sqlca.sqldbcode)
					break;
				}

				detCctr = disComm.getDisparams( "999999", "DRCR_CCTR_CBACK", conn );

				if( detCctr == null || detCctr.equalsIgnoreCase( "NULLFOUND" ) || detCctr.trim().length() == 0 )
				{
					// 21/07/10 manoharan if cctr_code is null then assign as cctr_code__ar (blank included)
					//errCode = "VMLSCCTR"; //'DS000' + string(sqlca.sqldbcode)
					//break;
					detCctr = cctrCode;
					// end 21/07/10 manoharan if cctr_code is null then assign as cctr_code__ar (blank included)
				}

				tranType = disComm.getDisparams( "999999", "CHARGE_BACK_TRAN_TYPE", conn );

				if( tranType == null || tranType.equalsIgnoreCase( "NULLFOUND" ) || tranType.trim().length() == 0 )
				{
					errCode = "VMLSTRTYPE"; //'DS000' + string(sqlca.sqldbcode)
					break;
				}

				reasonCd = disComm.getDisparams( "999999", "CHARGE_BACK_REASON_CODE", conn );
				System.out.println( "reasonCd ::" + reasonCd );
				if( reasonCd == null || reasonCd.equalsIgnoreCase( "NULLFOUND" ) || reasonCd.trim().length() == 0 )
				{
					errCode = "VMREASON"; //'DS000' + string(sqlca.sqldbcode)
					break;
				}
				
				System.out.println("claimAmt::["+claimAmt+"]netAmt:::["+netAmt+"]");
				System.out.println("drcrAmt::["+drcrAmt+"]");
				
				//added by santosh on 23/NOV/2017
				if( claimAmt <= 0 )
				{
					claimAmt = 0;
					drcrAmt = netAmt;
				}
				else
				{
					drcrAmt = claimAmt < netAmt ? claimAmt : netAmt;
				}
				//cHANGED BY POONAM FOR TAX CALCULATION 
				if(!isHeaderCreated){
					xmlBuff.append("<tran_ser><![CDATA["+ "DRNRCP" +"]]></tran_ser>");
					xmlBuff.append("<tran_date><![CDATA["+ sdf.format(currDate).toString() +"]]></tran_date>");
					xmlBuff.append("<eff_date><![CDATA["+ sdf.format(currDate).toString() +"]]></eff_date>");
					xmlBuff.append("<fin_entity><![CDATA["+ finEntity   +"]]></fin_entity>");
					xmlBuff.append("<site_code><![CDATA["+siteCodeCr +"]]></site_code>");
					xmlBuff.append("<sundry_type><![CDATA["+ "C"  +"]]></sundry_type>");
					xmlBuff.append("<sundry_code><![CDATA["+ custCodeCr  +"]]></sundry_code>");
					xmlBuff.append("<acct_code><![CDATA["+checkNull(acctCode)  +"]]></acct_code>");
					xmlBuff.append("<cctr_code><![CDATA["+ checkNull(cctrCode) +"]]></cctr_code>");
					xmlBuff.append("<invoice_id><![CDATA[" + checkNull(detInvId) + "]]></invoice_id>");
				}
				sql = "select nvl(round,'N') ls_round, nvl(round_to,0.001) lc_round_to from customer where cust_code = ? ";
				pstmt1 = conn.prepareStatement( sql );
				pstmt1.setString(1, custCodeCr );
				rSet = pstmt1.executeQuery();
				if( rSet.next() )
				{
					round = rSet.getString( "ls_round" );
					roundTo = rSet.getDouble( "lc_round_to" );
				}
				rSet.close();
				rSet = null;
				pstmt1.close();
				pstmt1 = null;

				roundAmt = disComm.getRndamt( drcrAmt, round, roundTo );
				
				System.out.println("drcrAmt in debit note["+drcrAmt+"]roundAmt["+roundAmt);
				
				if(drcrAmt < 0)
				{
					drcrAmt = drcrAmt * (-1);
				}
				
				
				
				System.out.println("drcrAmt IN debit note"+drcrAmt);
				
				diffAmt = roundAmt - drcrAmt;
				
				if(!isHeaderCreated) {
					xmlBuff.append("<amount><![CDATA["+ drcrAmt +"]]></amount>");
					xmlBuff.append("<curr_code><![CDATA["+ checkNull(currCode ) +"]]></curr_code>");
					xmlBuff.append("<exch_rate><![CDATA["+ exchgRate +"]]></exch_rate>");
				}

				if( remarks == null || remarks.trim().length() == 0 )
				{
					String tRemStr = null;
					System.out.println("tranIdSel->"+tranIdSel+" pOrderNo->"+pOrderNo+" pOrderDate->"+pOrderDate);
					tRemStr = ( "CB " + tranIdSel );
					if(!isHeaderCreated) {
						xmlBuff.append("<remarks><![CDATA["+ tRemStr  +"]]></remarks>");
					}
				}
				else
				{
					if(!isHeaderCreated) {
						xmlBuff.append("<remarks><![CDATA["+ remarks.trim()  +"]]></remarks>");
					}
				}
				analCode = finCommon.getFinparams("999999", "ANAL_CODE", conn);
				
				System.out.println("analCode:::"+analCode);
				if(!isHeaderCreated) {	
					xmlBuff.append("<drcr_flag><![CDATA["+ "D"   +"]]></drcr_flag>");
					xmlBuff.append("<tran_id__rcv><![CDATA["+ ""   +"]]></tran_id__rcv>");
					xmlBuff.append("<confirmed><![CDATA["+ "N"   +"]]></confirmed>");
					xmlBuff.append("<chg_user><![CDATA["+ userId   +"]]></chg_user>");
					xmlBuff.append("<chg_date><![CDATA["+ sdf.format(currDate).toString()   +"]]></chg_date>");
					xmlBuff.append("<chg_term><![CDATA["+ termId   +"]]></chg_term>");
					xmlBuff.append("<conf_date><![CDATA["+ sdf.format(currDate).toString()   +"]]></conf_date>");
					xmlBuff.append("<emp_code__aprv><![CDATA["+ empCodeAprv   +"]]></emp_code__aprv>");
					xmlBuff.append("<due_date><![CDATA["+ sdf.format(currDate).toString()   +"]]></due_date>");
					xmlBuff.append("<tran_type><![CDATA["+ checkNull(tranType)   +"]]></tran_type>");
					xmlBuff.append("<item_ser><![CDATA["+ checkNull(itemSer )   +"]]></item_ser>");
					//xmlBuff.append("<sreturn_no><![CDATA["+ checkNull(tranIdSel)   +"]]></sreturn_no>");
					xmlBuff.append("<adj_misc_crn><![CDATA["+ ""   +"]]></adj_misc_crn>");
					xmlBuff.append("<adj_amount><![CDATA["+ 0.0   +"]]></adj_amount>");
					xmlBuff.append("<parent__tran_id><![CDATA["+ ""   +"]]></parent__tran_id>");
					xmlBuff.append("<rev__tran><![CDATA["+ ""   +"]]></rev__tran>");
					xmlBuff.append("<round_adj><![CDATA["+ 0.0   +"]]></round_adj>");
					xmlBuff.append("<cust_ref_no><![CDATA["+ ""   +"]]></cust_ref_no>");
					xmlBuff.append("<anal_code><![CDATA["+ analCode   +"]]></anal_code>");
				}
				xmlBuff.append("<amount__bc><![CDATA["+  roundAmt * exchgRate   +"]]></amount__bc>");
				System.out.println("drcrAmt in debit detail"+drcrAmt);
				System.out.println("grossAmount in debit detail"+grossAmount);
				//llLineNo = 0;//Commented by Manoj dtd 04/01/2018 not to reset llLineNo
				
				
				
				//Commented by Manoj dtd 15/12/2017 to initialize before outer loop
				//xmlBuffDet = new StringBuffer();
				//TODO
				
				innerSql = "select invoice_id, inv_line_no, net_amt, item_code, tax_class, tax_chap, tax_env,rate__diff,quantity from charge_back_det where tran_id = ? and invoice_id = ? and line_no="+lineNo+" ";
				pstmt1 = conn.prepareStatement(innerSql);
				pstmt1.setString(1, tranIdSel);
				pstmt1.setString(2, detInvId);
				rSet = pstmt1.executeQuery();
				while(rSet.next()){
					invId = rSet.getString("invoice_id");
					invLineNo = rSet.getInt("inv_line_no");
					invNetAmt = rSet.getDouble("net_amt");
					itemCode = rSet.getString("item_code");
					taxClass = rSet.getString("tax_class");
					taxChap = rSet.getString("tax_chap");
					taxEnv = rSet.getString("tax_env");
					rateDiff=rSet.getDouble("rate__diff");
					cBackQty=rSet.getDouble("quantity");
					pstmt2=conn.prepareStatement("select lot_no,lot_sl,rate__clg,inv_line_no from invoice_trace where invoice_id=? and LINE_NO=?");
					pstmt2.setString(1,invId);
					pstmt2.setInt(2,invLineNo);
					rSet1 = pstmt2.executeQuery();
					
					while(rSet1.next())
					{
						lotNo=rSet1.getString("lot_no");
						lotSl=rSet1.getString("lot_sl");
						rateClg=rSet1.getDouble("rate__clg");
						invLineNo1=rSet1.getInt("inv_line_no");
						
					}
					
					rSet1.close();
					rSet1=null;
					pstmt2.close();
					pstmt2=null;
					
					llLineNo++; //Changes made by ashutosh on 31-05-2018 
					xmlBuffDet.append("<Detail2 dbID=\"\" domID=\""+llLineNo+"\" objName=\"drcrrcp_dr\" objContext=\"2\">");  
					xmlBuffDet.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
					xmlBuffDet.append("<tran_id/>");	
					detLineNoList.add(lineNo+":"+llLineNo);
					xmlBuffDet.append("<line_no><![CDATA[" + llLineNo + "]]></line_no>");
					xmlBuffDet.append("<invoice_id><![CDATA[" + invId + "]]></invoice_id>");
					xmlBuffDet.append("<line_no__invtrace><![CDATA[" + invLineNo + "]]></line_no__invtrace>");
					xmlBuffDet.append("<item_code><![CDATA[" + itemCode + "]]></item_code>");
					///Commented set with sign dtd 10/11/2017 by manoj 
					 if(invNetAmt < 0) {
						invNetAmt = invNetAmt * (-1);
						//rateDiff= rateDiff * (-1);
					}
					if(rateDiff < 0)
					{
						rateDiff= rateDiff * (-1);
					}
					if(rateClg < 0)
					{
						rateClg= rateClg * (-1);
					}
					xmlBuffDet.append("<quantity><![CDATA[" + cBackQty + "]]></quantity>");
					xmlBuffDet.append("<rate><![CDATA[" + rateDiff + "]]></rate>");
					xmlBuffDet.append("<rate__std><![CDATA[" + rateDiff + "]]></rate__std>");
					xmlBuffDet.append("<rate__clg><![CDATA[" + rateClg + "]]></rate__clg>");
					xmlBuffDet.append("<line_no__inv><![CDATA[" + invLineNo1 + "]]></line_no__inv>");
					xmlBuffDet.append("<lot_no><![CDATA[" + lotNo + "]]></lot_no>");
					xmlBuffDet.append("<lot_sl><![CDATA[" + lotSl + "]]></lot_sl>");
					
					xmlBuffDet.append("<drcr_amt><![CDATA[" + invNetAmt + "]]></drcr_amt>");
					xmlBuffDet.append("<net_amt><![CDATA[" + invNetAmt + "]]></net_amt>");
					xmlBuffDet.append("<reas_code><![CDATA[" + checkNull(reasonCd) + "]]></reas_code>");
					xmlBuffDet.append("<tax_class><![CDATA[" + checkNullAndTrim(taxClass) + "]]></tax_class>");
					xmlBuffDet.append("<tax_chap><![CDATA[" + checkNullAndTrim(taxChap) + "]]></tax_chap>");
					xmlBuffDet.append("<tax_env><![CDATA[" + checkNullAndTrim(taxEnv) + "]]></tax_env>");
					xmlBuffDet.append("</Detail2>");
					taxClass = "";
					taxChap = "";
					taxEnv = "";
				}
				rSet.close();
				rSet = null;
				pstmt1.close();
				pstmt1 = null;
				isHeaderCreated = true;
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			xmlBuff.append("</Detail1>");
			xmlBuff.append(xmlBuffDet);
			xmlBuff.append("</Header0>");
			xmlBuff.append("</group0>");
			xmlBuff.append("</DocumentRoot>");
			xmlString = xmlBuff.toString();
//			System.out.println("@@@@@2: xmlString:"+xmlBuff.toString());
//			System.out.println("...............just before savdata distorder()");
//			siteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
//			System.out.println("== site code =="+siteCode);
//			errString = saveData(siteCode,xmlString,xtraParams,conn);
//			System.out.println("@@@@@2: retString:"+errString);
//			System.out.println("--retString finished--");
//			if (errString.indexOf("Success") > -1)
//			{
//				System.out.println("@@@@@@3: Success"+errString);
//				dom = genericUtility.parseString(errString);
//				System.out.println("dom>>>"+dom);
//				drNtTranId = genericUtility.getColumnValue("TranID",dom);
//			}
//			else
//			{
//				System.out.println("[SuccessSuccess" + errString + "]");	
//				conn.rollback();
//				return errString;
//			}
//			
//			
//			sql = "update charge_back set "
//					+" tran_id__crn = ? "
//					+" where tran_id = ? ";
//
//			pstmt1 = conn.prepareStatement( sql );
//			pstmt1.setString(1, drNtTranId );
//			pstmt1.setString(2, tranIdSel );
//			int updCount = 0 ;
//			updCount = pstmt1.executeUpdate();
//
//			if( updCount == 0 )
//			{
//				errCode = "DS000NR";
//					//break;
//			}
//			pstmt1.close();
//			pstmt1 = null;
//			
//			
//
//			if( "Y".equalsIgnoreCase( asPost )  )
//			{
//				conn.commit();
//				//retString = retrieveMiscDrcrRcp("drcrrcp_dr",drNtTranId,xtraParams,"N" );
//				//retString = autoConfirmRecord("drcrrcp_dr", drNtTranId, xtraParams);
//				retString = executeSystemEvent("drcrrcp_dr", "pre_confirm", drNtTranId, xtraParams, conn);
//				System.out.println("retString ::: " + retString);
//				/*if(retString.indexOf("VTSUCC1") > -1 || retString.indexOf("CONFSUCCES") > -1){
//					conn.commit();
//				}*/
//				errCode = getErrCodeFromErrStr(retString);
//			}
			
			if(countData==0)
			{
				xmlString="";	
				detlineNo="";
			}
			else
			{
				//dataMap.put(xmlString, detlineNo.substring(0,detlineNo.length()-1));
				dataMap.put(xmlString, detLineNoList);
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if(pstmtDtl != null)
				{
					pstmtDtl.close();
					pstmtDtl = null;
				}
				if(pstmtHdr != null)
				{
					pstmtHdr.close();
					pstmtHdr = null;
				}
			}
			catch(Exception e)
			{
				System.out.println("Exception ChrgBckLocConfEJB....... :\n"+e.getMessage());
			}
		}
		System.out.println("EJB ["+this.getClass().getSimpleName() + "] method [createDrNoteCback] - END Returning ["+errCode+"]");
		return dataMap;
	}
	private HashMap<String,ArrayList<String>>  createCrNoteCback(Timestamp currDate, String str, String siteCode, String tranIdFr, String tranIdTo, Timestamp tranDateFr, Timestamp tranDateTo, String custCodeFr, String custCodeTo, String detInvId, String confirm, String xtraParams, Connection conn ) throws RemoteException,ITMException
	{
		System.out.println("EJB ["+this.getClass().getSimpleName() + "] method [createCrNoteCback] - START");
		PreparedStatement pstmt = null, pstmt1 = null, pstmtHdr = null, pstmtDtl = null,pstmt2=null;
		ResultSet rs = null, rSet = null, rSet1 = null;
		String sql = "", innerSql = "";
		String custCodeCr = "";
		String currCode = "";
		String finEntity = "";
		String acctCode = "";
		String cctrCode = "";
		String empCodeAprv = "";
		String tranIdSel = "";
		String pOrderNo = "";
		String siteCodeCr = "";
		String itemSer = "";
		String remarks = "";
		String detAcct = "";
		String errCode = "";
		String retString = "";
		String detCctr = "";
		String tranType = "";
		String reasonCd = "";
		String drNtTranId = "";
		String acctCodeTax = "";
		String cctrCodeTax = "";
		String round = "";
		String insDtlsql = "";
		String asPost = confirm;
		double roundTo = 0.0;
		double exchgRate = 0.0;
		double netAmt = 0.0;
		double claimAmt = 0.0;
		double grossAmount = 0.0;
		double drcrAmt = 0.0;
		double amtTax = 0.0;
		double total = 0.0;
		double roundAmt = 0.0;
		double diffAmt = 0.0;
		double discountAmt = 0.0 ,taxAmt = 0.0;
		int llLineNo = 0;
		java.sql.Timestamp tranDate = null;
		java.sql.Timestamp pOrderDate = null;
		
		StringBuffer xmlBuff = null;
		StringBuffer xmlBuffDet = null;
		String xmlString="",errString = "",taxClass = "",taxEnv = "",taxChap = "",analCode = "" ;
		Document dom = null ;
		String invId="",itemCode="";
		double invNetAmt=0.0;
		int invLineNo=0;
		double rateDiff=0.0;
		double cBackQty=0;
		String lotNo="",lotSl="";	
		double rateClg=0;
		int invLineNo1=0;
		ArrayList diffAmtList = null;
		DistCommon disComm = new DistCommon();
		int countData=0;
		HashMap<String,ArrayList<String>> dataMap=new HashMap<String, ArrayList<String>>();
		String detlineNo="";
		String lineNo="";
		ArrayList<String> detLineNoList=new ArrayList<String>();
		try
		{
			diffAmtList = new ArrayList();
			xmlBuff = new StringBuffer();		
			System.out.println("--XML CREATION ------");
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			
			xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
			xmlBuff.append("<DocumentRoot>");
			xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
			xmlBuff.append("<group0>");
			xmlBuff.append("<description>").append("Group0 description").append("</description>");
			xmlBuff.append("<Header0>");
			xmlBuff.append("<objName><![CDATA[").append("drcrrcp_cr").append("]]></objName>");  
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
			xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"drcrrcp_cr\" objContext=\"1\">");  
			xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
			xmlBuff.append("<tran_id/>");	
			xmlBuffDet = new StringBuffer();
			sql = "select a.cust_code__credit, a.curr_code, a.exch_rate, a.emp_code__aprv, a.tran_id, b.net_amt,a.tax_amt, a.porder_no, "
					+ " a.porder_date, a.claum_amt, a.site_code__cr, a.item_ser, b.amount, a.remarks, b.discount_amt as discAmt, "
					+ " b.tax_class as tax_class,b.tax_chap as tax_chap,b.tax_env as tax_env, b.inv_line_no, b.line_no "
					+ " from charge_back a, charge_back_det b where a.tran_id = b.tran_id and a.tran_id = ? and b.invoice_id = ? and b.net_amt > 0 ";
				/*+ " group by a.cust_code__credit, a.curr_code, a.exch_rate, a.emp_code__aprv, a.tran_id, b.net_amt, a.tax_amt, "
				+ " a.porder_no, a.porder_date, a.claum_amt, a.site_code__cr, a.item_ser, a.amount, a.remarks, b.tax_class, b.tax_chap, b.tax_env, b.inv_line_no";*/
				
			pstmt = conn.prepareStatement( sql );
			pstmt.setString( 1, tranIdFr );
			pstmt.setString( 2, detInvId );
			
			rs = pstmt.executeQuery();
			boolean isHeaderCreated = false;
			while(rs.next())
			{
				//added by santosh to set Transaction id in front of invoice id
				countData++;
				custCodeCr = rs.getString("cust_code__credit");
				currCode = rs.getString("curr_code");
				exchgRate = rs.getDouble("exch_rate");
				empCodeAprv = checkNull(rs.getString("emp_code__aprv"));
				tranIdSel = rs.getString("tran_id");
				netAmt = rs.getDouble("net_amt");
				taxAmt = rs.getDouble("tax_amt");
				pOrderNo = rs.getString("porder_no");
				pOrderDate = rs.getTimestamp("porder_date");
				claimAmt = rs.getDouble("claum_amt");
				siteCodeCr = rs.getString("site_code__cr");
				itemSer = rs.getString("item_ser");
				grossAmount = rs.getDouble("amount");
				remarks = rs.getString("remarks");
				discountAmt = rs.getDouble("discAmt");
				taxClass = rs.getString("tax_class");
				taxChap = rs.getString("tax_chap");
				taxEnv = rs.getString("tax_env");
				lineNo =rs.getString("line_no");
				detlineNo+=lineNo+",";
				System.out.println("@S@ Inside Credit note ["+detlineNo+"]");
				System.out.println("taxClass==>["+taxClass+"]");
				System.out.println("taxChap==>["+taxChap+"]");
				System.out.println("taxEnv==>["+taxEnv+"]");
				
				System.out.println("netAmt==>["+netAmt+"]grossAmount["+grossAmount+"]taxAmt["+taxAmt+"]");
				if( siteCodeCr == null || siteCodeCr.trim().length() == 0 )
				{
					siteCodeCr = siteCode;
				}

				sql = "select fin_entity from site where site_code = ?" ;
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, siteCodeCr.trim() );
				rSet = pstmt1.executeQuery();
				if( rSet.next() )
				{
					finEntity = rSet.getString( "fin_entity" );
				}
				rSet.close();
				rSet = null;
				pstmt1.close();
				pstmt1 = null;

				sql = "select acct_code__ar, cctr_code__ar from customer where cust_code = ? " ;
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, custCodeCr.trim() );
				rSet = pstmt1.executeQuery();
				if( rSet.next() )
				{
					acctCode = rSet.getString( "acct_code__ar" );
					cctrCode = rSet.getString( "cctr_code__ar" );
				}
				rSet.close();
				rSet = null;
				pstmt1.close();
				pstmt1 = null;
				// 21/07/10 manoharan make cctr_code blank in case null
				if (cctrCode == null)
				{
					cctrCode = "     ";
				}
				// end 21/07/10
				detAcct = disComm.getDisparams( "999999", "DRCR_ACCT_CBACK", conn );

				if( detAcct == null || detAcct.equalsIgnoreCase( "NULLFOUND" ) || detAcct.trim().length() == 0 )
				{
					errCode = "VMDRCRACCT"; //'DS000' + string(sqlca.sqldbcode)
					break;
				}

				detCctr = disComm.getDisparams( "999999", "DRCR_CCTR_CBACK", conn );

				if( detCctr == null || detCctr.equalsIgnoreCase( "NULLFOUND" ) || detCctr.trim().length() == 0 )
				{
					detCctr = cctrCode;
				}

				tranType = disComm.getDisparams( "999999", "CHARGE_BACK_TRAN_TYPE", conn );

				if( tranType == null || tranType.equalsIgnoreCase( "NULLFOUND" ) || tranType.trim().length() == 0 )
				{
					errCode = "VMLSTRTYPE";
					break;
				}

				reasonCd = disComm.getDisparams( "999999", "CHARGE_BACK_REASON_CODE", conn );
				System.out.println( "reasonCd ::" + reasonCd );
				if( reasonCd == null || reasonCd.equalsIgnoreCase( "NULLFOUND" ) || reasonCd.trim().length() == 0 )
				{
					errCode = "VMREASON";
					break;
				}
				
				System.out.println("claimAmt::["+claimAmt+"]netAmt:::["+netAmt+"]");
				System.out.println("drcrAmt::["+drcrAmt+"]");
				
				
				if( claimAmt <= 0 )
				{
					claimAmt = 0;
					drcrAmt = netAmt;
				}
				else
				{
					drcrAmt = claimAmt < netAmt ? claimAmt : netAmt;
				}
				if (!isHeaderCreated) {
					xmlBuff.append("<tran_ser><![CDATA[" + "CRNRCP" + "]]></tran_ser>");
					xmlBuff.append("<tran_date><![CDATA[" + sdf.format(currDate).toString() + "]]></tran_date>");
					xmlBuff.append("<eff_date><![CDATA[" + sdf.format(currDate).toString() + "]]></eff_date>");
					xmlBuff.append("<fin_entity><![CDATA[" + finEntity + "]]></fin_entity>");
					xmlBuff.append("<site_code><![CDATA[" + siteCodeCr + "]]></site_code>");
					xmlBuff.append("<sundry_type><![CDATA[" + "C" + "]]></sundry_type>");
					xmlBuff.append("<sundry_code><![CDATA[" + custCodeCr + "]]></sundry_code>");
					xmlBuff.append("<acct_code><![CDATA[" + checkNull(acctCode) + "]]></acct_code>");
					xmlBuff.append("<cctr_code><![CDATA[" + checkNull(cctrCode) + "]]></cctr_code>");
					xmlBuff.append("<invoice_id><![CDATA[" + checkNull(detInvId) + "]]></invoice_id>");
				}
				
				sql = "select nvl(round,'N') ls_round, nvl(round_to,0.001) lc_round_to from customer where cust_code = ? ";
				pstmt1 = conn.prepareStatement( sql );
				pstmt1.setString(1, custCodeCr );
				rSet = pstmt1.executeQuery();
				if( rSet.next() )
				{
					round = rSet.getString( "ls_round" );
					roundTo = rSet.getDouble( "lc_round_to" );
				}
				rSet.close();
				rSet = null;
				pstmt1.close();
				pstmt1 = null;

				roundAmt = disComm.getRndamt( drcrAmt, round, roundTo );
				
				System.out.println("drcrAmt in debit note["+drcrAmt+"]roundAmt["+roundAmt);
				
				if(drcrAmt < 0)
				{
					drcrAmt = drcrAmt * (-1);
				}
				
				System.out.println("drcrAmt IN debit note"+drcrAmt);
				
				diffAmt = roundAmt - drcrAmt;
				
				//pstmtHdr.setDouble( 11, drcrAmt );//CHANGED BY POONAM
				/*pstmtHdr.setDouble( 11, roundAmt );//CHANGED BY POONAM
				pstmtHdr.setString( 12, currCode == null || currCode.trim().length() == 0 ? "" : currCode);//CHANGED BY POONAM
				pstmtHdr.setDouble( 13, exchgRate );*///CHANGED BY POONAM
				if (!isHeaderCreated) {
					xmlBuff.append("<amount><![CDATA["+ drcrAmt +"]]></amount>");
					xmlBuff.append("<curr_code><![CDATA["+ checkNull(currCode ) +"]]></curr_code>");
					xmlBuff.append("<exch_rate><![CDATA["+ exchgRate +"]]></exch_rate>");
				}
				

				if( remarks == null || remarks.trim().length() == 0 )
				{
					String tRemStr = null;
					System.out.println("tranIdSel->"+tranIdSel+" pOrderNo->"+pOrderNo+" pOrderDate->"+pOrderDate);
					tRemStr = ( "CB " + tranIdSel );
					if (!isHeaderCreated) {
						xmlBuff.append("<remarks><![CDATA["+ tRemStr  +"]]></remarks>");
					}
				}
				else
				{
					if (!isHeaderCreated) {
						xmlBuff.append("<remarks><![CDATA["+ remarks.trim()  +"]]></remarks>");
					}
				}
				analCode = finCommon.getFinparams("999999", "ANAL_CODE", conn);
				System.out.println("analCode:::"+analCode);
				
				if (!isHeaderCreated) {
					xmlBuff.append("<drcr_flag><![CDATA[" + "C" + "]]></drcr_flag>");
					xmlBuff.append("<tran_id__rcv><![CDATA[" + "" + "]]></tran_id__rcv>");
					xmlBuff.append("<confirmed><![CDATA[" + "N" + "]]></confirmed>");
					xmlBuff.append("<chg_user><![CDATA[" + userId + "]]></chg_user>");
					xmlBuff.append("<chg_date><![CDATA[" + sdf.format(currDate).toString() + "]]></chg_date>");
					xmlBuff.append("<chg_term><![CDATA[" + termId + "]]></chg_term>");
					xmlBuff.append("<conf_date><![CDATA[" + sdf.format(currDate).toString() + "]]></conf_date>");
					xmlBuff.append("<emp_code__aprv><![CDATA[" + empCodeAprv + "]]></emp_code__aprv>");
					xmlBuff.append("<due_date><![CDATA[" + sdf.format(currDate).toString() + "]]></due_date>");
					xmlBuff.append("<tran_type><![CDATA[" + checkNull(tranType) + "]]></tran_type>");
					xmlBuff.append("<item_ser><![CDATA[" + checkNull(itemSer) + "]]></item_ser>");
					//xmlBuff.append("<sreturn_no><![CDATA[" + checkNull(tranIdSel) + "]]></sreturn_no>");
					xmlBuff.append("<adj_misc_crn><![CDATA[" + "" + "]]></adj_misc_crn>");
					xmlBuff.append("<adj_amount><![CDATA[" + 0.0 + "]]></adj_amount>");
					xmlBuff.append("<parent__tran_id><![CDATA[" + "" + "]]></parent__tran_id>");
					xmlBuff.append("<rev__tran><![CDATA[" + "" + "]]></rev__tran>");
					xmlBuff.append("<round_adj><![CDATA[" + 0.0 + "]]></round_adj>");
					xmlBuff.append("<cust_ref_no><![CDATA[" + "" + "]]></cust_ref_no>");
					xmlBuff.append("<anal_code><![CDATA["+ analCode   +"]]></anal_code>");
				}
				xmlBuff.append("<amount__bc><![CDATA[" + roundAmt * exchgRate + "]]></amount__bc>");
				
				//llLineNo = 0;//Commented by Manoj dtd 04/01/2018 not to reset llLineNo variable
				//Commented by Manoj dtd 15/12/2017 to define before outerloop
				//xmlBuffDet = new StringBuffer();
				//TODO
				innerSql = "select invoice_id, inv_line_no, net_amt, item_code, nvl(quantity,0) as quantity, tax_class, tax_chap, tax_env,rate__diff,quantity from charge_back_det where tran_id = ? and invoice_id = ? and line_no="+lineNo+" ";
				pstmt1 = conn.prepareStatement(innerSql);
				pstmt1.setString(1, tranIdSel);
				pstmt1.setString(2, detInvId);
				rSet = pstmt1.executeQuery();
				while(rSet.next()){
					 invId = rSet.getString("invoice_id");
					 invLineNo = rSet.getInt("inv_line_no");
					 invNetAmt = rSet.getDouble("net_amt");
					 itemCode = rSet.getString("item_code");
					
					taxClass = rSet.getString("tax_class");
					taxChap = rSet.getString("tax_chap");
					taxEnv = rSet.getString("tax_env");
					rateDiff=rSet.getDouble("rate__diff");
					cBackQty=rSet.getDouble("quantity");
					pstmt2=conn.prepareStatement("select lot_no,lot_sl,rate__clg,inv_line_no from invoice_trace where invoice_id=? and LINE_NO=?");
					pstmt2.setString(1,invId);
					pstmt2.setInt(2,invLineNo);
					rSet1 = pstmt2.executeQuery();
					
					while(rSet1.next())
					{
						lotNo=rSet1.getString("lot_no");
						lotSl=rSet1.getString("lot_sl");
						rateClg=rSet1.getDouble("rate__clg");
						invLineNo1=rSet1.getInt("inv_line_no");
						
					}
					
					rSet1.close();
					rSet1=null;
					pstmt2.close();
					pstmt2=null;
					
					llLineNo++; //Changes made by ashutosh on 31-05-2018 
					xmlBuffDet.append("<Detail2 dbID=\"\" domID=\""+llLineNo+"\" objName=\"drcrrcp_cr\" objContext=\"2\">");  
					xmlBuffDet.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
					xmlBuffDet.append("<tran_id/>");	
					detLineNoList.add(lineNo+":"+llLineNo);
					xmlBuffDet.append("<line_no><![CDATA[" + llLineNo + "]]></line_no>");
					xmlBuffDet.append("<invoice_id><![CDATA[" + invId + "]]></invoice_id>");
					xmlBuffDet.append("<line_no__invtrace><![CDATA[" + invLineNo + "]]></line_no__invtrace>");
					xmlBuffDet.append("<item_code><![CDATA[" + itemCode + "]]></item_code>");
					/*if(invNetAmt < 0) {
						invNetAmt = invNetAmt * (-1);
						rateDiff = rateDiff * (-1);
					}*/
					xmlBuffDet.append("<quantity><![CDATA[" + cBackQty + "]]></quantity>");
					xmlBuffDet.append("<rate><![CDATA[" + rateDiff + "]]></rate>");
					xmlBuffDet.append("<rate__std><![CDATA[" + rateDiff + "]]></rate__std>");
					xmlBuffDet.append("<rate__clg><![CDATA[" + rateClg + "]]></rate__clg>");
					xmlBuffDet.append("<line_no__inv><![CDATA[" + invLineNo1 + "]]></line_no__inv>");
					xmlBuffDet.append("<lot_no><![CDATA[" + lotNo + "]]></lot_no>");
					xmlBuffDet.append("<lot_sl><![CDATA[" + lotSl + "]]></lot_sl>");
					xmlBuffDet.append("<drcr_amt><![CDATA[" + invNetAmt + "]]></drcr_amt>");
					xmlBuffDet.append("<net_amt><![CDATA[" + invNetAmt + "]]></net_amt>");
					xmlBuffDet.append("<reas_code><![CDATA[" + checkNull(reasonCd) + "]]></reas_code>");
					xmlBuffDet.append("<tax_class><![CDATA[" + checkNullAndTrim(taxClass) + "]]></tax_class>");
					xmlBuffDet.append("<tax_chap><![CDATA[" + checkNullAndTrim(taxChap) + "]]></tax_chap>");
					xmlBuffDet.append("<tax_env><![CDATA[" + checkNullAndTrim(taxEnv) + "]]></tax_env>");
					xmlBuffDet.append("</Detail2>");
					taxClass = "";
					taxChap = "";
					taxEnv = "";
				}
				rSet.close();
				rSet = null;
				pstmt1.close();
				pstmt1 = null;
				roundAmt = disComm.getRndamt( drcrAmt, round, roundTo );
				diffAmt = roundAmt - drcrAmt;
				System.out.println("diffAmt IN CR["+diffAmt+"]");
				isHeaderCreated = true;
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			xmlBuff.append("</Detail1>");
			xmlBuff.append(xmlBuffDet);
			xmlBuff.append("</Header0>");
			xmlBuff.append("</group0>");
			xmlBuff.append("</DocumentRoot>");
			xmlString = xmlBuff.toString();
			System.out.println("@@@@@2: xmlString:"+xmlBuff.toString());
//			System.out.println("...............just before savdata distorder()");
//			siteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
//			System.out.println("== site code =="+siteCode);
//			errString = saveData(siteCode,xmlString,xtraParams,conn);
//			System.out.println("@@@@@2: retString:"+errString);
//			System.out.println("--retString finished--");
//			if (errString.indexOf("Success") > -1)
//			{
//				System.out.println("@@@@@@3: Success"+errString);
//				dom = genericUtility.parseString(errString);
//				System.out.println("dom>>>"+dom);
//				drNtTranId = genericUtility.getColumnValue("TranID",dom);
//			}
//			else
//			{
//				System.out.println("[SuccessSuccess" + errString + "]");	
//				conn.rollback();
//				return errString;
//			}
//			
//			
//			/*sql = "update charge_back set "
//					+" tran_id__crn = ? "
//					+" where tran_id = ? ";
//
//			pstmt1 = conn.prepareStatement( sql );
//			pstmt1.setString(1, drNtTranId );
//			pstmt1.setString(2, tranIdSel );
//			int updCount = 0 ;
//			updCount = pstmt1.executeUpdate();
//
//			if( updCount == 0 )
//			{
//				errCode = "DS000NR";
//			}
//			pstmt1.close();
//			pstmt1 = null;	*/
//					
//
//			if( "Y".equalsIgnoreCase( asPost )  )
//			{
//				conn.commit();
//				//retString = retrieveMiscDrcrRcp("drcrrcp_cr",drNtTranId,xtraParams,"N" );
//				//retString = autoConfirmRecord("drcrrcp_cr", drNtTranId, xtraParams);
//				retString = executeSystemEvent("drcrrcp_cr", "pre_confirm", drNtTranId, xtraParams, conn);
//				System.out.println("retString ::: " + retString);
//				/*if(retString.indexOf("VTSUCC1") > -1 || retString.indexOf("CONFSUCCES") > -1){
//					conn.commit();
//				}*/
//				errCode = getErrCodeFromErrStr(retString);
//			}
			if(countData==0)
			{
				xmlString="";	
				detlineNo="";
			}
			else
			{
				//dataMap.put(xmlString, detlineNo.substring(0,detlineNo.length()-1));
				dataMap.put(xmlString, detLineNoList);
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if(pstmtDtl != null)
				{
					pstmtDtl.close();
					pstmtDtl = null;
				}
				if(pstmtHdr != null)
				{
					pstmtHdr.close();
					pstmtHdr = null;
				}
			}
			catch(Exception e)
			{
				System.out.println("Exception ChrgBckLocConfEJB....... :\n"+e.getMessage());
			}
		}
		System.out.println("EJB [" + this.getClass().getSimpleName() + "] method [createCrNoteCback] - END Returning [" + errCode + "]");
		return dataMap;
	}
	
	/*public String retrieveMiscDrcrRcp(String businessObj, String tranIdFr,String xtraParams, String forcedFlag) throws ITMException
	{
		String methodName = "";
		String compName = "";
		String retString = "";
		String serviceCode = "";
		String serviceURI = "";
		String actionURI = "";
		String sql = "";
		Connection conn = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		int cnt = 0;

		try
		{
			ConnDriver connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			methodName = "gbf_post";
			actionURI = "http://NvoServiceurl.org/" + methodName;

			sql = "SELECT SERVICE_CODE,COMP_NAME FROM SYSTEM_EVENTS WHERE OBJ_NAME = ? AND EVENT_CODE = 'pre_confirm' ";
			pStmt = conn.prepareStatement(sql);
			pStmt.setString(1,businessObj);
			rs = pStmt.executeQuery();
			if ( rs.next() )
			{
				serviceCode = rs.getString("SERVICE_CODE");
				compName = rs.getString("COMP_NAME");
			}
			// Changed by Manish on 01/04/16 for max cursor issue [start]
			if (pStmt != null)
			{
				pStmt.close();
				pStmt=null;
			}
			if (rs !=null)
			{
				rs.close();
				rs=null;
			}
			// Changed by Manish on 01/04/16 for max cursor issue [end]
			sql = "SELECT SERVICE_URI FROM SYSTEM_EVENT_SERVICES WHERE SERVICE_CODE = ? ";
			pStmt = conn.prepareStatement(sql);
			pStmt.setString(1,serviceCode);
			rs = pStmt.executeQuery();
			if ( rs.next() )
			{
				serviceURI = rs.getString("SERVICE_URI");
			}
			// Changed by Manish on 01/04/16 for max cursor issue [start]
			if (pStmt != null)
			{
				pStmt.close();
				pStmt=null;
			}
			if (rs !=null)
			{
				rs.close();
				rs=null;
			}
			// Changed by Manish on 01/04/16 for max cursor issue [end]
			Service service = new Service();
			Call call = (Call)service.createCall();
			call.setTargetEndpointAddress(new java.net.URL(serviceURI));			
			call.setOperationName( new javax.xml.namespace.QName("http://NvoServiceurl.org", methodName ) );
			call.setUseSOAPAction(true);
			call.setSOAPActionURI(actionURI); 
			Object[] aobj = new Object[4];

			call.addParameter( new javax.xml.namespace.QName("http://NvoServiceurl.org", "component_name"), XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter( new javax.xml.namespace.QName("http://NvoServiceurl.org", "tran_id"), XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter( new javax.xml.namespace.QName("http://NvoServiceurl.org", "xtra_params"), XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter( new javax.xml.namespace.QName("http://NvoServiceurl.org", "forced_flag"), XMLType.XSD_STRING, ParameterMode.IN);

			aobj[0] = new String(compName);
			aobj[1] = new String(tranIdFr);
			aobj[2] = new String(xtraParams);
			aobj[3] = new String(forcedFlag);

			call.setReturnType(XMLType.XSD_STRING);
			retString = (String)call.invoke(aobj);
			System.out.println("Return string from NVO is:==>["+retString+"]");
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
				if (pStmt != null )
				{
					pStmt.close();
					pStmt = null;
				}
				if (rs !=null)
				{
					rs.close();
					rs=null;
				}
				if( conn != null ){
					conn.close();
					conn = null;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return retString;
	}*/
	private String getCurrdateInAppFormat() throws ITMException
	{
		String currAppdate =null;
		java.sql.Timestamp currDate = null;
		Object date = null;
		SimpleDateFormat DBDate=null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility= new  E12GenericUtility();
		try
		{
			currDate =new java.sql.Timestamp(System.currentTimeMillis()) ;
			System.out.println( genericUtility.getDBDateFormat());
			DBDate= new SimpleDateFormat(genericUtility.getDBDateFormat());
			date = DBDate.parse(currDate.toString());
			currDate =	java.sql.Timestamp.valueOf(DBDate.format(date).toString() + " 00:00:00.0");
			currAppdate = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(currDate).toString();
		}
		catch(Exception e)
		{
			System.out.println("Exception in  getCurrdateInAppFormat:::"+e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
		}
		return (currAppdate);
	}
	private String generateTranId(String windowName, String tranDate, String siteCode ,String signBy,String tranType) throws ITMException
	{
		Connection conn = null;
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
			ConnDriver connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 13-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 13-06-2016 :END
			conn.setAutoCommit(false);
			generateTranIdSql = "SELECT KEY_STRING, TRAN_ID_COL, REF_SER FROM TRANSETUP WHERE UPPER(TRAN_WINDOW)= ?";
			System.out.println("[ChargeBackLocConfEJB :: generateTranId : input Paramaters][windowName]["+windowName+"][tranDate]["+tranDate+"][siteCode]["+siteCode+"]");
			System.out.println( "[ChargeBackLocConfEJB : generateTranId : Tran generator Sql[" + generateTranIdSql+"]" );
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
			xmlValuesBuff.append("<vouch_type><![CDATA[F]]></vouch_type>");
			xmlValuesBuff.append("<tran_type><![CDATA["+tranType+"]]></tran_type>");
			// 20/07/10 manoharan drcr_flag added
			xmlValuesBuff.append("<drcr_flag><![CDATA[C]]></drcr_flag>");
			// end 20/07/10 manoharan drcr_flag added
			xmlValuesBuff.append("</Detail1></Root>");
			xmlValues = xmlValuesBuff.toString();
			System.out.println("xmlValues  :[" + xmlValues + "]");
			TransIDGenerator tg = new TransIDGenerator(xmlValues,signBy, CommonConstants.DB_NAME);
			tranId = tg.generateTranSeqID(refSer, tranIdCol, keyString, conn);
			System.out.println("tranId :"+tranId);
			conn.commit();
		}
		catch (SQLException ex)
		{			
			ex.printStackTrace();
			throw new ITMException(ex); //Added By Mukesh Chauhan on 02/08/19
		}
		catch (Exception e)
		{		
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
		}
		finally
		{
			try
			{
				if( conn != null ){
					conn.close();
					conn = null;
				}
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
	private String getErrCodeFromErrStr( String errStr ) throws RemoteException,ITMException
	{
		String retErrCode = null;

		try
		{
			Document ParseRetString = genericUtility.parseString( errStr );
			NodeList RetStringNodeList = ParseRetString.getElementsByTagName("error");
			Node RetStringNode = RetStringNodeList.item(0);
			retErrCode = RetStringNode.getAttributes().getNamedItem("id").getNodeValue();
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			throw new ITMException( exception );
		}
		return retErrCode;		
	}
	
	private HashMap<String,ArrayList<String>> createMiscCrnoteCback(Timestamp currDate, String str, String siteCode, String tranIdFr, String tranIdTo, Timestamp tranDateFr, Timestamp tranDateTo, String custCodeFr, String custCodeTo, String confirm, String xtraParams, Connection conn ) throws RemoteException,ITMException
	{
		System.out.println("Inside createMiscCrnoteCback................");
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		PreparedStatement pstmtHdr = null;
		PreparedStatement pstmtDtl = null;
		ResultSet rs = null;
		Statement stmt = null;
		ResultSet rSet = null;
		String sql = null;
		String custCodeCr = "";
		String currCode = "";
		String finEntity = "";
		String acctCode = "";
		String cctrCode = "";
		String empCodeAprv = "";
		String tranIdSel = "";
		String pOrderNo = "";
		String siteCodeCr = "";
		String itemSer = "";
		String remarks = "";
		String detAcct = "";
		String errCode = "";
		String retString = "";
		String detCctr = "";
		String tranType = "";
		String reasonCd = "";
		String drNtTranId = "";
		String acctCodeTax = "";
		String cctrCodeTax = "";
		String round = "";
		String insDtlsql = "";
		String asPost = confirm;
		double roundTo = 0.0;
		double exchgRate = 0.0;
		double netAmt = 0.0;
		double claimAmt = 0.0;
		double grossAmount = 0.0;
		double drcrAmt = 0.0;
		double amtTax = 0.0;
		double total = 0.0;
		double roundAmt = 0.0;
		double diffAmt = 0.0;
		double discountAmt = 0.0 ,taxAmt = 0.0;
		int llLineNo = 0;
		java.sql.Timestamp tranDate = null;
		java.sql.Timestamp pOrderDate = null;
		
		StringBuffer xmlBuff = null;
		StringBuffer xmlBuffDet = null;
		String xmlString="",errString = "",taxClass = "",taxEnv = "",taxChap = "",analCode = "" ;
		Document dom = null ;
		int countData=0;
			
		
		ArrayList diffAmtList = null;
		HashMap<String,ArrayList<String>> dataMap=new HashMap<String, ArrayList<String>>();
		String detlineNo="";
		String lineNo="";
		ArrayList<String> detLineNoList=new ArrayList<String>();
		
		/*String insHdrSql = "insert into MISC_DRCR_RCP ( "
			+" TRAN_ID, TRAN_SER, TRAN_DATE, EFF_DATE, FIN_ENTITY, "
			+" SITE_CODE, SUNDRY_TYPE, SUNDRY_CODE, ACCT_CODE, CCTR_CODE, "
			+" AMOUNT, CURR_CODE, EXCH_RATE, REMARKS, DRCR_FLAG, "
			+" TRAN_ID__RCV, CONFIRMED, CHG_USER, CHG_DATE, CHG_TERM, "
			+" CONF_DATE, EMP_CODE__APRV, DUE_DATE, TRAN_TYPE, ITEM_SER, "
			+" AMOUNT__BC, SRETURN_NO, ADJ_MISC_CRN, ADJ_AMOUNT, PARENT__TRAN_ID, "
			+" REV__TRAN, ROUND_ADJ, "
			+" CUST_REF_NO, CUST_REF_DATE, CUST_REF_AMT, RND_OFF, RND_TO ) "
			+" values ( "
			+" ?, ?, ?, ?, ?, "
			+" ?, ?, ?, ?, ?, "
			+" ?, ?, ?, ?, ?, "
			+" ?, ?, ?, ?, ?, "
			+" ?, ?, ?, ?, ?, "
			+" ?, ?, ?, ?, ?, "
			+" ?, ?, ?, ?, ?, "
			+" ?, ?  ) ";

		String insertDtl = "insert into MISC_DRCR_RDET( "
			+" TRAN_ID, LINE_NO, ACCT_CODE, CCTR_CODE, AMOUNT, "
			+" NET_AMT, REF_NO, REAS_CODE, ANAL_CODE"
			+" ) values ( "
			+" ?, ?, ?, ?, ?, "
			+" ?, ?, ?, ? )";*/

		DistCommon disComm = new DistCommon();

		try
		{	
			//pstmtHdr = conn.prepareStatement( insHdrSql );
			//pstmtDtl = conn.prepareStatement( insertDtl );
			
			diffAmtList = new ArrayList();
			xmlBuff = new StringBuffer();		
			System.out.println("--XML CREATION ------");
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			xmlBuffDet = new StringBuffer();  //added by manish mhatre on 11-sep-20

			xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
			xmlBuff.append("<DocumentRoot>");
			xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
			xmlBuff.append("<group0>");
			xmlBuff.append("<description>").append("Group0 description").append("</description>");
			xmlBuff.append("<Header0>");
			xmlBuff.append("<objName><![CDATA[").append("misc_drcr_rcp_cr").append("]]></objName>");  
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
			xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"misc_drcr_rcp_cr\" objContext=\"1\">");  
			xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
			xmlBuff.append("<tran_id/>");	
			
			
			sql = "select a.cust_code__credit, a.curr_code, a.exch_rate, a.emp_code__aprv, a.tran_id, "
				+" b.net_amt,b.tax_amt, a.porder_no, a.porder_date, a.claum_amt, a.site_code__cr, a.item_ser, "
				//+" a.amount, a.remarks, sum(b.discount_amt) as discAmt ,b.tax_class as tax_class,b.tax_chap as tax_chap,b.tax_env as tax_env    "
				+" b.amount, a.remarks, b.discount_amt as discAmt ,b.tax_class as tax_class,b.tax_chap as tax_chap,b.tax_env as tax_env,b.line_no as line_no    "
				+" from charge_back a, charge_back_det b "
				+"	where a.tran_id = b.tran_id "
				+" and a.tran_id = ? "
				//+" and	a.tran_id <= ? "
				//+" and a.tran_date >= ? "
				//+" and a.tran_date <= ? "
				//+" and a.cust_code >= ? "
				//+" and a.cust_code <= ? "
				//+" and a.site_code = ? "
				// +" and a.confirmed = 'Y' "
				+" and b.invoice_id is null and b.net_amt > 0 ";
				//+" group by a.cust_code__credit, a.curr_code, a.exch_rate, a.emp_code__aprv, a.tran_id, "
				//+" a.net_amt,a.tax_amt, a.porder_no, a.porder_date, a.claum_amt, a.site_code__cr, a.item_ser, "
				//+" a.amount, a.remarks ,b.tax_class ,b.tax_chap ,b.tax_env";

			pstmt = conn.prepareStatement( sql );
			pstmt.setString( 1, tranIdFr );
			//pstmt.setString( 2, tranIdTo );
			//pstmt.setTimestamp( 3, tranDateFr );
			//pstmt.setTimestamp( 4, tranDateTo );
			//pstmt.setString( 5, custCodeFr );
			//pstmt.setString( 6, custCodeTo );
			//pstmt.setString( 7, siteCode );

			rs = pstmt.executeQuery();
			while( rs.next() )
			{
				countData++;
				custCodeCr = rs.getString("cust_code__credit");
				currCode = rs.getString("curr_code");
				exchgRate = rs.getDouble("exch_rate");
				empCodeAprv = checkNull(rs.getString("emp_code__aprv"));
				tranIdSel = rs.getString("tran_id");
				netAmt = rs.getDouble("net_amt");
				taxAmt = rs.getDouble("tax_amt");
				pOrderNo = rs.getString("porder_no");
				pOrderDate = rs.getTimestamp("porder_date");
				claimAmt = rs.getDouble("claum_amt");
				siteCodeCr = rs.getString("site_code__cr");
				itemSer = rs.getString("item_ser");
				grossAmount = rs.getDouble("amount");
				remarks = rs.getString("remarks");
				discountAmt = rs.getDouble("discAmt");
				taxClass = rs.getString("tax_class");
				taxChap = rs.getString("tax_chap");
				taxEnv = rs.getString("tax_env");
				lineNo =rs.getString("line_no");
				detlineNo+=lineNo+",";
				System.out.println("taxClass==>["+taxClass+"]");
				System.out.println("taxChap==>["+taxChap+"]");
				System.out.println("taxEnv==>["+taxEnv+"]");
				
				System.out.println("netAmt==>["+netAmt+"]grossAmount["+grossAmount+"]taxAmt["+taxAmt+"]");
				if( siteCodeCr == null || siteCodeCr.trim().length() == 0 )
				{
					siteCodeCr = siteCode;
				}

				sql = "select fin_entity from site where site_code = ?" ;
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, siteCodeCr.trim() );
				rSet = pstmt1.executeQuery();
				if( rSet.next() )
				{
					finEntity = rSet.getString( "fin_entity" );
				}
				rSet.close();
				rSet = null;
				pstmt1.close();
				pstmt1 = null;

				sql = "select acct_code__ar, cctr_code__ar from customer where cust_code = ? " ;
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, custCodeCr.trim() );
				rSet = pstmt1.executeQuery();
				if( rSet.next() )
				{
					acctCode = rSet.getString( "acct_code__ar" );
					cctrCode = rSet.getString( "cctr_code__ar" );
				}
				rSet.close();
				rSet = null;
				pstmt1.close();
				pstmt1 = null;
				// 21/07/10 manoharan make cctr_code blank in case null
				if (cctrCode == null)
				{
					cctrCode = "     ";
				}
				// end 21/07/10
				detAcct = disComm.getDisparams( "999999", "DRCR_ACCT_CBACK", conn );

				if( detAcct == null || detAcct.equalsIgnoreCase( "NULLFOUND" ) || detAcct.trim().length() == 0 )
				{
					errCode = "VMDRCRACCT"; //'DS000' + string(sqlca.sqldbcode)
					break;
				}

				detCctr = disComm.getDisparams( "999999", "DRCR_CCTR_CBACK", conn );

				if( detCctr == null || detCctr.equalsIgnoreCase( "NULLFOUND" ) || detCctr.trim().length() == 0 )
				{
					// 21/07/10 manoharan if cctr_code is null then assign as cctr_code__ar (blank included)
					//errCode = "VMLSCCTR"; //'DS000' + string(sqlca.sqldbcode)
					//break;
					detCctr = cctrCode;
					// end 21/07/10 manoharan if cctr_code is null then assign as cctr_code__ar (blank included)
				}

				tranType = disComm.getDisparams( "999999", "CHARGE_BACK_TRAN_TYPE", conn );

				if( tranType == null || tranType.equalsIgnoreCase( "NULLFOUND" ) || tranType.trim().length() == 0 )
				{
					errCode = "VMLSTRTYPE"; //'DS000' + string(sqlca.sqldbcode)
					break;
				}

				reasonCd = disComm.getDisparams( "999999", "CHARGE_BACK_REASON_CODE", conn );
				System.out.println( "reasonCd ::" + reasonCd );
				if( reasonCd == null || reasonCd.equalsIgnoreCase( "NULLFOUND" ) || reasonCd.trim().length() == 0 )
				{
					errCode = "VMREASON"; //'DS000' + string(sqlca.sqldbcode)
					break;
				}
				
				System.out.println("claimAmt::["+claimAmt+"]netAmt:::["+netAmt+"]");
				System.out.println("drcrAmt::["+drcrAmt+"]");
				
				//drcrAmt=netAmt;
				if( claimAmt <= 0 )
				{
					claimAmt = 0;
					drcrAmt = netAmt;
				}
				else
				{
					drcrAmt = claimAmt < netAmt ? claimAmt : netAmt;
				}
				//cHANGED BY POONAM FOR TAX CALCULATION 
				
				xmlBuff.append("<tran_ser><![CDATA["+ "MDRCRC" +"]]></tran_ser>");
				xmlBuff.append("<tran_date><![CDATA["+ sdf.format(currDate).toString() +"]]></tran_date>");
				xmlBuff.append("<eff_date><![CDATA["+ sdf.format(currDate).toString() +"]]></eff_date>");
				xmlBuff.append("<fin_entity><![CDATA["+ finEntity   +"]]></fin_entity>");
				xmlBuff.append("<site_code><![CDATA["+siteCodeCr +"]]></site_code>");
				xmlBuff.append("<sundry_type><![CDATA["+ "C"  +"]]></sundry_type>");
				xmlBuff.append("<sundry_code><![CDATA["+ custCodeCr  +"]]></sundry_code>");
				xmlBuff.append("<acct_code><![CDATA["+checkNull(acctCode)  +"]]></acct_code>");
				xmlBuff.append("<curr_code><![CDATA["+ checkNull(cctrCode) +"]]></curr_code>");
				
				
				
				
				
				
				
				
				//crNtTranId = generateTranId("W_MISC_DRCR_RCP_CR", getCurrdateInAppFormat(), loginSite, userId, tranType );
				/*drNtTranId = generateTranId("W_MISC_DRCR_RCP_DR", getCurrdateInAppFormat(), siteCodeCr, userId, tranType );
				//-----------------------Inserting into header-----------------------------------
				pstmtHdr.setString( 1, drNtTranId );
				pstmtHdr.setString( 2, "MDRCRD" );
				pstmtHdr.setTimestamp( 3, currDate );
				pstmtHdr.setTimestamp( 4, currDate );
				pstmtHdr.setString( 5, finEntity );
				pstmtHdr.setString( 6, siteCodeCr);
				pstmtHdr.setString( 7, "C" );
				pstmtHdr.setString( 8, custCodeCr );
				pstmtHdr.setString( 9, acctCode == null ? "" : acctCode );
				pstmtHdr.setString( 10, cctrCode == null ? "    " : cctrCode );*/
				sql = "select nvl(round,'N') ls_round, nvl(round_to,0.001) lc_round_to from customer where cust_code = ? ";
				pstmt1 = conn.prepareStatement( sql );
				pstmt1.setString(1, custCodeCr );
				rSet = pstmt1.executeQuery();
				if( rSet.next() )
				{
					round = rSet.getString( "ls_round" );
					roundTo = rSet.getDouble( "lc_round_to" );
				}
				rSet.close();
				rSet = null;
				pstmt1.close();
				pstmt1 = null;

				roundAmt = disComm.getRndamt( drcrAmt, round, roundTo );
				
				System.out.println("drcrAmt in debit note["+drcrAmt+"]roundAmt["+roundAmt);
				
				if(drcrAmt < 0)
				{
					drcrAmt = drcrAmt * (-1);
				}
				
				System.out.println("drcrAmt IN debit note"+drcrAmt);

                //added by manish mhatre on 9-sep-20
                //start manish
                if(netAmt < 0)
				{
					netAmt = netAmt * (-1);
                }
                //end manish
				
				diffAmt = roundAmt - drcrAmt;
				
				//pstmtHdr.setDouble( 11, drcrAmt );//CHANGED BY POONAM
				/*pstmtHdr.setDouble( 11, roundAmt );//CHANGED BY POONAM
				pstmtHdr.setString( 12, currCode == null || currCode.trim().length() == 0 ? "" : currCode);//CHANGED BY POONAM
				pstmtHdr.setDouble( 13, exchgRate );*///CHANGED BY POONAM
				
                //xmlBuff.append("<amount><![CDATA["+ drcrAmt +"]]></amount>");   //commented by manish mhatre on 9-sep-20
                xmlBuff.append("<amount><![CDATA["+ netAmt +"]]></amount>");   //added by manish mhatre on 9-sep-20[For create misc cr note from net amt in charge back details]
				xmlBuff.append("<curr_code><![CDATA["+ checkNull(currCode ) +"]]></curr_code>");
				xmlBuff.append("<exch_rate><![CDATA["+ exchgRate +"]]></exch_rate>");
				

				if( remarks == null || remarks.trim().length() == 0 )
				{
					String tRemStr = null;
					System.out.println("tranIdSel->"+tranIdSel+" pOrderNo->"+pOrderNo+" pOrderDate->"+pOrderDate);
					tRemStr = ( "CB " + tranIdSel );
					//pstmtHdr.setString( 14, tRemStr ); //CHANGED BY POONAM
					xmlBuff.append("<remarks><![CDATA["+ tRemStr  +"]]></remarks>");
				}
				else
				{
					//pstmtHdr.setString( 14, remarks.trim() );//CHANGED BY POONAM
					xmlBuff.append("<remarks><![CDATA["+ remarks.trim()  +"]]></remarks>");
				}
				
				xmlBuff.append("<drcr_flag><![CDATA["+ "C"   +"]]></drcr_flag>");
				xmlBuff.append("<tran_id__rcv><![CDATA["+ ""   +"]]></tran_id__rcv>");
				xmlBuff.append("<confirmed><![CDATA["+ "N"   +"]]></confirmed>");
				xmlBuff.append("<chg_user><![CDATA["+ userId   +"]]></chg_user>");
				xmlBuff.append("<chg_date><![CDATA["+ sdf.format(currDate).toString()   +"]]></chg_date>");
				xmlBuff.append("<chg_term><![CDATA["+ termId   +"]]></chg_term>");
				xmlBuff.append("<conf_date><![CDATA["+ sdf.format(currDate).toString()   +"]]></conf_date>");
				
				xmlBuff.append("<emp_code__aprv><![CDATA["+ empCodeAprv   +"]]></emp_code__aprv>");
				xmlBuff.append("<due_date><![CDATA["+ sdf.format(currDate).toString()   +"]]></due_date>");
				xmlBuff.append("<tran_type><![CDATA["+ checkNull(tranType)   +"]]></tran_type>");
				xmlBuff.append("<item_ser><![CDATA["+ checkNull(itemSer )   +"]]></item_ser>");
				xmlBuff.append("<amount__bc><![CDATA["+  roundAmt * exchgRate   +"]]></amount__bc>");
				xmlBuff.append("<sreturn_no><![CDATA["+ checkNull(tranIdSel)   +"]]></sreturn_no>");
				xmlBuff.append("<adj_misc_crn><![CDATA["+ ""   +"]]></adj_misc_crn>");
				xmlBuff.append("<adj_amount><![CDATA["+ 0.0   +"]]></adj_amount>");
				xmlBuff.append("<parent__tran_id><![CDATA["+ ""   +"]]></parent__tran_id>");
				xmlBuff.append("<rev__tran><![CDATA["+ ""   +"]]></rev__tran>");
				xmlBuff.append("<round_adj><![CDATA["+ 0.0   +"]]></round_adj>");
				//xmlBuff.append("<cust_ref_no><![CDATA["+ ""   +"]]></cust_ref_no>");//commented by manish mhatre on 23-sep-20
                xmlBuff.append("<cust_ref_no><![CDATA["+tranIdSel+"]]></cust_ref_no>");//added by manish mhatre on 23-sep-20 [For store the chargeback no in cust_ref_no field]
                System.out.println("charge back no>>>>"+tranIdSel);
				//xmlBuff.append("<cust_ref_date><![CDATA["+  sdf.format(currDate).toString() +"]]></cust_ref_date>");
				/*xmlBuff.append("<cust_ref_amt><![CDATA["+ 0.0   +"]]></cust_ref_amt>");
				xmlBuff.append("<rnd_off><![CDATA["+ 0.0   +"]]></rnd_off>");
				xmlBuff.append("<rnd_to><![CDATA["+ 0.0   +"]]></rnd_to>");*/
				
				//POONAM
				
				/*pstmtHdr.setString( 15, "D" );//Changed by Poonam to created debit note..
				pstmtHdr.setString( 16, "" );
				pstmtHdr.setString( 17, "N" );
				pstmtHdr.setString( 18, userId );
				pstmtHdr.setTimestamp( 19, currDate );
				pstmtHdr.setString( 20, termId );
				pstmtHdr.setNull( 21, java.sql.Types.DATE );
				pstmtHdr.setString( 22, empCodeAprv );
				pstmtHdr.setTimestamp( 23, currDate );
				pstmtHdr.setString( 24, ( tranType == null || tranType.trim().length() == 0 ? "" :tranType.trim() ) );
				pstmtHdr.setString( 25, ( itemSer == null || itemSer.trim().length() == 0 ? "" : itemSer.trim() ) );
				//pstmtHdr.setDouble( 26, drcrAmt );
				pstmtHdr.setDouble( 26, roundAmt * exchgRate);
				pstmtHdr.setString( 27, ( tranIdSel == null || tranIdSel.trim().length() == 0 ? "" :  tranIdSel.trim() ) );
				pstmtHdr.setString( 28, "" ); //ADJ_MISC_CRN
				pstmtHdr.setDouble( 29, 0.0 ); //ADJ_AMOUNT
				pstmtHdr.setString( 30, "" ); //PARENT__TRAN_ID
				pstmtHdr.setString( 31, "" ); //REV__TRAN
				pstmtHdr.setDouble( 32, 0.0 ); //ROUND_ADJ
				//pstmt.setInt( 33, 0 ); //LINE_NO__SRET
				//pstmt.setInt( 34, 0 ); //LINE_NO__SRET
				//pstmt.setString( 35, "" ); //LOT_NO
				pstmtHdr.setString( 33, "" ); //CUST_REF_NO
				pstmtHdr.setNull( 34, java.sql.Types.DATE ); //CUST_REF_DATE
				pstmtHdr.setDouble( 35, 0.0 ); //CUST_REF_AMT
				pstmtHdr.setDouble( 36, 0.0 ); //RND_OFF
				pstmtHdr.setDouble( 37, 0.0 ); //RND_TO

				int updtCnt = pstmtHdr.executeUpdate();*/
				//System.out.println("updtCnt==>"+updtCnt);
				//-----------------------Inserting into detail-----------------------------------
				
				System.out.println("drcrAmt in credit detail"+drcrAmt);
				System.out.println("grossAmount in debit detail"+grossAmount);
				//llLineNo = 0;//Commented by Manoj dtd 04/01/2018 not to reset variable
				
				analCode = finCommon.getFinparams("999999", "ANAL_CODE", conn);
				if( analCode == null || analCode.equalsIgnoreCase( "NULLFOUND" ) || analCode.trim().length() == 0 )//CONDITION ADDED BY NANDKUMAR GADKARI ON 20-04-2020
				{
					analCode="";
				}
				System.out.println("analCode:::"+analCode);
				
				//xmlBuffDet = new StringBuffer();    //commented by manish mhatre on 11-sep-20
				System.out.println("manish lline no:::"+llLineNo);
				llLineNo++; //Changes made by ashutosh on 31-05-2018  
				xmlBuffDet.append("<Detail2 dbID=\"\" domID=\""+llLineNo+"\" objName=\"misc_drcr_rcp_cr\" objContext=\"2\">");  
				xmlBuffDet.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
				xmlBuffDet.append("<tran_id/>");	
                detLineNoList.add(lineNo+":"+llLineNo);
				xmlBuffDet.append("<line_no><![CDATA["+ llLineNo   +"]]></line_no>");
				xmlBuffDet.append("<acct_code><![CDATA["+ detAcct   +"]]></acct_code>");
				xmlBuffDet.append("<cctr_code><![CDATA["+ detCctr   +"]]></cctr_code>");
				
				
				
				//CHANGED BY POONAM
				
				/*pstmtDtl.setString( 1, drNtTranId );
				llLineNo++;
				pstmtDtl.setInt( 2, llLineNo );
				pstmtDtl.setString( 3, detAcct );
				pstmtDtl.setString( 4, detCctr );*/
				
				if(drcrAmt < 0)
				{
					drcrAmt = drcrAmt * (-1);
				}
				if(grossAmount < 0)
				{
					grossAmount = grossAmount * (-1);
				}
				if(taxAmt < 0)
				{
					taxAmt = taxAmt * (-1);
				}
				
				System.out.println("drcrAmt in debit detail"+drcrAmt);
				System.out.println("grossAmount in debit detail"+grossAmount);
				System.out.println("taxAmt in debit detail"+taxAmt);

				//if( claimAmt > 0 && claimAmt < netAmt )
				//{
					//pstmtDtl.setDouble( 5, drcrAmt );
					//pstmtDtl.setDouble( 6, drcrAmt );
					//CHANGED BY POONAM
				if(claimAmt > 0 && claimAmt < netAmt)
				{
                    //xmlBuffDet.append("<amount><![CDATA["+ drcrAmt   +"]]></amount>");  //commented by manish mhatre on 9-sep-20
                    xmlBuffDet.append("<amount><![CDATA["+ netAmt   +"]]></amount>");  //added by manish mhatre on 9-sep-20[For create misc cr note from net_amt in charge_back_det]
					xmlBuffDet.append("<net_amt><![CDATA["+ drcrAmt   +"]]></net_amt>");
					//xmlBuffDet.append("<tax_amt><![CDATA["+ taxAmt   +"]]></tax_amt>");
				}
				else
				{
                    //xmlBuffDet.append("<amount><![CDATA["+ grossAmount+"]]></amount>");   //commented by manish mhatre on 9-sep-20
                    xmlBuffDet.append("<amount><![CDATA["+ netAmt   +"]]></amount>");  //added by manish mhatre on 9-sep-20[For create misc cr note from net_amt in charge_back_det]                    
					//xmlBuffDet.append("<net_amt><![CDATA["+ drcrAmt   +"]]></net_amt>");
					//xmlBuffDet.append("<tax_amt><![CDATA["+ taxAmt   +"]]></tax_amt>");
				}
				//}
				/*else
				{
					//pstmtDtl.setDouble( 5, grossAmount );
					//pstmtDtl.setDouble( 6, grossAmount );
					//CHANGED BY POONAM
					xmlBuffDet.append("<amount><![CDATA["+ grossAmount   +"]]></amount>");
					xmlBuffDet.append("<net_amt><![CDATA["+ grossAmount   +"]]></net_amt>");
				}*/
				//CHANGED BY POONAM
				/*pstmtDtl.setString( 7, tranIdSel ); //ref_no
				pstmtDtl.setString( 8, ( reasonCd == null || reasonCd.trim().length() == 0 ? "" : reasonCd.trim() ) ); //reas_code
				pstmtDtl.setString( 9, "" ); //ANAL_CODE
*/				
				
				xmlBuffDet.append("<ref_no><![CDATA["+ tranIdSel   +"]]></ref_no>");
				xmlBuffDet.append("<reas_code><![CDATA["+  checkNull(reasonCd)    +"]]></reas_code>");
				xmlBuffDet.append("<anal_code><![CDATA["+ analCode   +"]]></anal_code>");
				
				
				xmlBuffDet.append("<tax_class><![CDATA["+ checkNullAndTrim(taxClass) +"]]></tax_class>");
				xmlBuffDet.append("<tax_chap><![CDATA["+ checkNullAndTrim(taxChap) +"]]></tax_chap>");
				xmlBuffDet.append("<tax_env><![CDATA["+ checkNullAndTrim(taxEnv) +"]]></tax_env>");
				
                xmlBuffDet.append("</Detail2>");
                
				//Changed by poonam :taxtran not required
				
				/*pstmtDtl.addBatch();

				sql = "select acct_code, cctr_code, sum(tax_amt) taxamt "
					+" from taxtran "
					+" where tran_code = 'S-CHB' and tran_id = ? "
					+" and tax_amt <> 0 "
					+" and effect <> 'N' "
					+" group by acct_code, cctr_code " ;
				//
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, tranIdSel);
				rSet = pstmt1.executeQuery();
				while( rSet.next() )
				{
					acctCodeTax = rSet.getString( "acct_code" );
					cctrCodeTax = rSet.getString( "acct_code" );
					amtTax =  rSet.getDouble( "taxamt" );

					if( acctCodeTax == null || acctCodeTax.trim().length() == 0 )
					{
						acctCodeTax = detAcct;
					}
					if( cctrCodeTax == null || cctrCodeTax.trim().length() == 0 )
					{
						cctrCodeTax = detCctr;
					}

					total = total + amtTax;

					pstmtDtl.setString( 1, drNtTranId );

					llLineNo++;
					pstmtDtl.setInt( 2, llLineNo );
					pstmtDtl.setString( 3, acctCodeTax );
					pstmtDtl.setString( 4, acctCodeTax );
					pstmtDtl.setDouble( 5, amtTax);
					pstmtDtl.setDouble( 6, 0.0 );
					pstmtDtl.setString( 7, "" ); //ref_no
					pstmtDtl.setString( 8, "" ); //reas_code
					pstmtDtl.setString( 9, "" ); //ANAL_CODE

					pstmtDtl.addBatch();
					
				}
				rSet.close();
				rSet = null;
				pstmt1.close();
				pstmt1 = null;

				pstmtDtl.executeBatch();

				pstmtDtl.close();
				pstmtDtl = null;
				pstmtHdr.close();
				pstmtHdr = null;*/

				

				/*sql = "select nvl(round,'N') ls_round, nvl(round_to,0.001) lc_round_to from customer where cust_code = ? ";
				pstmt1 = conn.prepareStatement( sql );
				pstmt1.setString(1, custCodeCr );
				rSet = pstmt1.executeQuery();
				if( rSet.next() )
				{
					round = rSet.getString( "ls_round" );
					roundTo = rSet.getDouble( "lc_round_to" );
				}
				rSet.close();
				rSet = null;
				pstmt1.close();
				pstmt1 = null;*/

				roundAmt = disComm.getRndamt( drcrAmt, round, roundTo );
				diffAmt = roundAmt - drcrAmt;
				System.out.println("diffAmt IN DR["+diffAmt+"]");
			//if( diffAmt != 0 )
				//{
					//1. update header
					//2. insert record in detail
					
					
					
					
					/*insDtlsql = "insert into MISC_DRCR_RDET( "
						+" TRAN_ID, LINE_NO, ACCT_CODE, CCTR_CODE, AMOUNT, "
						+" NET_AMT, REF_NO, REAS_CODE, ANAL_CODE, TAX_AMT"
						+" ) values ( "
						+" ?, ?, ?, ?, ?, "
						+" ?, ?, ?, ?, ? )";
					pstmt1 = conn.prepareStatement( insDtlsql );
					pstmt1.setString( 1, drNtTranId );

					llLineNo++;

					pstmt1.setInt( 2, llLineNo );
					pstmt1.setString( 3, detAcct );
					pstmt1.setString( 4, detCctr );
					pstmt1.setDouble( 5, -diffAmt);
					pstmt1.setDouble( 6, -diffAmt );
					pstmt1.setString( 7, "" ); //ref_no
					pstmt1.setString( 8, reasonCd.trim() ); //reas_code
					pstmt1.setString( 9, "" ); //ANAL_CODE
					pstmt1.setDouble( 10, 0 );

					pstmt1.executeUpdate();

					pstmt1.close();
					pstmt1 = null;*/
				//}
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			xmlBuff.append("</Detail1>");
			xmlBuff.append(xmlBuffDet);
			xmlBuff.append("</Header0>");
			xmlBuff.append("</group0>");
			xmlBuff.append("</DocumentRoot>");
			xmlString = xmlBuff.toString();
			System.out.println("@@@@@2: xmlString: MISC CREDIT NOTE [ "+xmlBuff.toString()+"]]]]]]]]]");
//			System.out.println("...............just before savdata distorder()");
//			siteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
//			System.out.println("== site code =="+siteCode);
//			errString = saveData(siteCode,xmlString,xtraParams,conn);
//			System.out.println("@@@@@2: retString:"+errString);
//			System.out.println("--retString finished--");
//			if (errString.indexOf("Success") > -1)
//			{
//				System.out.println("@@@@@@3: Success"+errString);
//				dom = genericUtility.parseString(errString);
//				System.out.println("dom>>>"+dom);
//				drNtTranId = genericUtility.getColumnValue("TranID",dom);
//			}
//			else
//			{
//				System.out.println("[SuccessSuccess" + errString + "]");	
//				conn.rollback();
//				return errString;
//			}
//			
//			
//			/*sql = "update charge_back set "
//					+" tran_id__crn = ? "
//					+" where tran_id = ? ";
//
//			pstmt1 = conn.prepareStatement( sql );
//			pstmt1.setString(1, drNtTranId );
//			pstmt1.setString(2, tranIdSel );
//			int updCount = 0 ;
//			updCount = pstmt1.executeUpdate();
//
//			if( updCount == 0 )
//			{
//				errCode = "DS000NR";
//					//break;
//			}
//			pstmt1.close();
//			pstmt1 = null;	
//					*/
//
//			if( "Y".equalsIgnoreCase( asPost )  )
//			{
//				conn.commit();
//				//retString = retrieveMiscDrcrRcp( "misc_drcr_rcp_cr",drNtTranId,xtraParams,"N" );
//				//retString = autoConfirmRecord("misc_drcr_rcp_cr", drNtTranId, xtraParams);
//				retString = executeSystemEvent("misc_drcr_rcp_cr", "pre_confirm", drNtTranId, xtraParams, conn);
//				System.out.println("retString ::: " + retString);
//				/*if(retString.indexOf("VTSUCC1") > -1 || retString.indexOf("CONFSUCCES") > -1){
//					conn.commit();
//				}*/
//				errCode = getErrCodeFromErrStr(retString);
//			}
			if(countData==0)
			{
				xmlString="";	
				detlineNo="";
			}
			else
			{
				//dataMap.put(xmlString, detlineNo.substring(0,detlineNo.length()-1));
				dataMap.put(xmlString, detLineNoList);
			}
				
		}
		catch(Exception e)
		{
			System.out.println("Exception :" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if(pstmtDtl != null)
				{
					pstmtDtl.close();
					pstmtDtl = null;
				}
				if(pstmtHdr != null)
				{
					pstmtHdr.close();
					pstmtHdr = null;
				}
			}
			catch(Exception e)
			{
				System.out.println("Exception ChrgBckLocConfEJB....... :\n"+e.getMessage());
			}
		}
		return dataMap;
	}
	
	// Added for Debit note generation if amt < 0: Start
	
	private HashMap<String,ArrayList<String>> createMiscDrnoteCback(Timestamp currDate, String str, String siteCode, String tranIdFr, String tranIdTo, Timestamp tranDateFr, Timestamp tranDateTo, String custCodeFr, String custCodeTo, String confirm, String xtraParams, Connection conn ) throws RemoteException,ITMException
	{
		
		System.out.println("Inside createMiscDrnoteCback................");
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		PreparedStatement pstmtHdr = null;
		PreparedStatement pstmtDtl = null;
		ResultSet rs = null;
		Statement stmt = null;
		ResultSet rSet = null;
		String sql = null;
		String custCodeCr = "";
		String currCode = "";
		String finEntity = "";
		String acctCode = "";
		String cctrCode = "";
		String empCodeAprv = "";
		String tranIdSel = "";
		String pOrderNo = "";
		String siteCodeCr = "";
		String itemSer = "";
		String remarks = "";
		String detAcct = "";
		String errCode = "";
		String retString = "";
		String detCctr = "";
		String tranType = "";
		String reasonCd = "";
		String drNtTranId = "";
		String acctCodeTax = "";
		String cctrCodeTax = "";
		String round = "";
		String insDtlsql = "";
		String asPost = confirm;
		double roundTo = 0.0;
		double exchgRate = 0.0;
		double netAmt = 0.0;
		double claimAmt = 0.0;
		double grossAmount = 0.0;
		double drcrAmt = 0.0;
		double amtTax = 0.0;
		double total = 0.0;
		double roundAmt = 0.0;
		double diffAmt = 0.0;
		double discountAmt = 0.0 ,taxAmt = 0.0;
		int llLineNo = 0;
		java.sql.Timestamp tranDate = null;
		java.sql.Timestamp pOrderDate = null;
		
		StringBuffer xmlBuff = null;
		StringBuffer xmlBuffDet = null;
		String xmlString="",errString = "",taxClass = "",taxEnv = "",taxChap = "",analCode = "" ;
		Document dom = null ;
		int countData=0;
		
		ArrayList diffAmtList = null;
		HashMap<String,ArrayList<String>> dataMap=new HashMap<String, ArrayList<String>>();
		String detlineNo="";
		String lineNo="";
		ArrayList<String> detLineNoList=new ArrayList<String>();
		/*String insHdrSql = "insert into MISC_DRCR_RCP ( "
			+" TRAN_ID, TRAN_SER, TRAN_DATE, EFF_DATE, FIN_ENTITY, "
			+" SITE_CODE, SUNDRY_TYPE, SUNDRY_CODE, ACCT_CODE, CCTR_CODE, "
			+" AMOUNT, CURR_CODE, EXCH_RATE, REMARKS, DRCR_FLAG, "
			+" TRAN_ID__RCV, CONFIRMED, CHG_USER, CHG_DATE, CHG_TERM, "
			+" CONF_DATE, EMP_CODE__APRV, DUE_DATE, TRAN_TYPE, ITEM_SER, "
			+" AMOUNT__BC, SRETURN_NO, ADJ_MISC_CRN, ADJ_AMOUNT, PARENT__TRAN_ID, "
			+" REV__TRAN, ROUND_ADJ, "
			+" CUST_REF_NO, CUST_REF_DATE, CUST_REF_AMT, RND_OFF, RND_TO ) "
			+" values ( "
			+" ?, ?, ?, ?, ?, "
			+" ?, ?, ?, ?, ?, "
			+" ?, ?, ?, ?, ?, "
			+" ?, ?, ?, ?, ?, "
			+" ?, ?, ?, ?, ?, "
			+" ?, ?, ?, ?, ?, "
			+" ?, ?, ?, ?, ?, "
			+" ?, ?  ) ";

		String insertDtl = "insert into MISC_DRCR_RDET( "
			+" TRAN_ID, LINE_NO, ACCT_CODE, CCTR_CODE, AMOUNT, "
			+" NET_AMT, REF_NO, REAS_CODE, ANAL_CODE"
			+" ) values ( "
			+" ?, ?, ?, ?, ?, "
			+" ?, ?, ?, ? )";*/

		DistCommon disComm = new DistCommon();

		try
		{	
			//pstmtHdr = conn.prepareStatement( insHdrSql );
			//pstmtDtl = conn.prepareStatement( insertDtl );
			
			diffAmtList = new ArrayList();
			xmlBuff = new StringBuffer();		
			System.out.println("--XML CREATION ------");
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			
			xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
			xmlBuff.append("<DocumentRoot>");
			xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
			xmlBuff.append("<group0>");
			xmlBuff.append("<description>").append("Group0 description").append("</description>");
			xmlBuff.append("<Header0>");
			xmlBuff.append("<objName><![CDATA[").append("misc_drcr_rcp_dr").append("]]></objName>");  
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
			xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"misc_drcr_rcp_dr\" objContext=\"1\">");  
			xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
			xmlBuff.append("<tran_id/>");	
			xmlBuffDet = new StringBuffer();     //added by manish mhatre on 11-sep-20
			
			sql = "select a.cust_code__credit, a.curr_code, a.exch_rate, a.emp_code__aprv, a.tran_id, "
				+" b.net_amt, b.tax_amt,a.porder_no, a.porder_date, a.claum_amt, a.site_code__cr, a.item_ser, "
				+" b.amount, a.remarks, b.discount_amt as discAmt ,b.tax_class as tax_class,b.tax_chap as tax_chap,b.tax_env as tax_env ,b.line_no as  line_no  "
				+" from charge_back a, charge_back_det b "
				+"	where a.tran_id = b.tran_id "
				+" and a.tran_id = ? "
				//+" and	a.tran_id <= ? "
				//+" and a.tran_date >= ? "
				//+" and a.tran_date <= ? "
				//+" and a.cust_code >= ? "
				//+" and a.cust_code <= ? "
				//+" and a.site_code = ? "
				// +" and a.confirmed = 'Y' "
				+" and b.invoice_id is null and b.net_amt < 0";
				//+" group by a.cust_code__credit, a.curr_code, a.exch_rate, a.emp_code__aprv, a.tran_id, "
				//+" a.net_amt, a.tax_amt,a.porder_no, a.porder_date, a.claum_amt, a.site_code__cr, a.item_ser, "
				//+" a.amount, a.remarks ,b.tax_class ,b.tax_chap ,b.tax_env";

			pstmt = conn.prepareStatement( sql );
			pstmt.setString( 1, tranIdFr );
			/*pstmt.setString( 2, tranIdTo );
			pstmt.setTimestamp( 3, tranDateFr );
			pstmt.setTimestamp( 4, tranDateTo );
			pstmt.setString( 5, custCodeFr );
			pstmt.setString( 6, custCodeTo );
			pstmt.setString( 7, siteCode );*/

			rs = pstmt.executeQuery();
			while( rs.next() )
			{
				countData++;
				custCodeCr = rs.getString("cust_code__credit");
				currCode = rs.getString("curr_code");
				exchgRate = rs.getDouble("exch_rate");
				empCodeAprv = checkNull(rs.getString("emp_code__aprv"));
				tranIdSel = rs.getString("tran_id");
				netAmt = rs.getDouble("net_amt");
				taxAmt = rs.getDouble("tax_amt");
				pOrderNo = rs.getString("porder_no");
				pOrderDate = rs.getTimestamp("porder_date");
				claimAmt = rs.getDouble("claum_amt");
				siteCodeCr = rs.getString("site_code__cr");
				itemSer = rs.getString("item_ser");
				grossAmount = rs.getDouble("amount");
				remarks = rs.getString("remarks");
				discountAmt = rs.getDouble("discAmt");
				taxClass = rs.getString("tax_class");
				taxChap = rs.getString("tax_chap");
				taxEnv = rs.getString("tax_env");
				lineNo =rs.getString("line_no");
				detlineNo+=lineNo+",";
				System.out.println("taxClass==>["+taxClass+"]");
				System.out.println("taxChap==>["+taxChap+"]");
				System.out.println("taxEnv==>["+taxEnv+"]");
				if( siteCodeCr == null || siteCodeCr.trim().length() == 0 )
				{
					siteCodeCr = siteCode;
				}

				sql = "select fin_entity from site where site_code = ?" ;
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, siteCodeCr.trim() );
				rSet = pstmt1.executeQuery();
				if( rSet.next() )
				{
					finEntity = rSet.getString( "fin_entity" );
				}
				rSet.close();
				rSet = null;
				pstmt1.close();
				pstmt1 = null;

				sql = "select acct_code__ar, cctr_code__ar from customer where cust_code = ? " ;
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, custCodeCr.trim() );
				rSet = pstmt1.executeQuery();
				if( rSet.next() )
				{
					acctCode = rSet.getString( "acct_code__ar" );
					cctrCode = rSet.getString( "cctr_code__ar" );
				}
				rSet.close();
				rSet = null;
				pstmt1.close();
				pstmt1 = null;
				// 21/07/10 manoharan make cctr_code blank in case null
				if (cctrCode == null)
				{
					cctrCode = "     ";
				}
				// end 21/07/10
				detAcct = disComm.getDisparams( "999999", "DRCR_ACCT_CBACK", conn );

				if( detAcct == null || detAcct.equalsIgnoreCase( "NULLFOUND" ) || detAcct.trim().length() == 0 )
				{
					errCode = "VMDRCRACCT"; //'DS000' + string(sqlca.sqldbcode)
					break;
				}

				detCctr = disComm.getDisparams( "999999", "DRCR_CCTR_CBACK", conn );

				if( detCctr == null || detCctr.equalsIgnoreCase( "NULLFOUND" ) || detCctr.trim().length() == 0 )
				{
					// 21/07/10 manoharan if cctr_code is null then assign as cctr_code__ar (blank included)
					//errCode = "VMLSCCTR"; //'DS000' + string(sqlca.sqldbcode)
					//break;
					detCctr = cctrCode;
					// end 21/07/10 manoharan if cctr_code is null then assign as cctr_code__ar (blank included)
				}

				tranType = disComm.getDisparams( "999999", "CHARGE_BACK_TRAN_TYPE", conn );

				if( tranType == null || tranType.equalsIgnoreCase( "NULLFOUND" ) || tranType.trim().length() == 0 )
				{
					errCode = "VMLSTRTYPE"; //'DS000' + string(sqlca.sqldbcode)
					break;
				}

				reasonCd = disComm.getDisparams( "999999", "CHARGE_BACK_REASON_CODE", conn );
				System.out.println( "reasonCd ::" + reasonCd );
				if( reasonCd == null || reasonCd.equalsIgnoreCase( "NULLFOUND" ) || reasonCd.trim().length() == 0 )
				{
					errCode = "VMREASON"; //'DS000' + string(sqlca.sqldbcode)
					break;
				}
				
				System.out.println("claimAmt::["+claimAmt+"]netAmt:::["+netAmt+"]");
				System.out.println("drcrAmt::["+drcrAmt+"]");
				
				//drcrAmt=netAmt;
				if( claimAmt <= 0 )
				{
					claimAmt = 0;
					drcrAmt = netAmt;
				}
				else
				{
					drcrAmt = claimAmt < netAmt ? claimAmt : netAmt;
				}
				//cHANGED BY POONAM FOR TAX CALCULATION 
				
				xmlBuff.append("<tran_ser><![CDATA["+ "MDRCRD" +"]]></tran_ser>");
				xmlBuff.append("<tran_date><![CDATA["+ sdf.format(currDate).toString() +"]]></tran_date>");
				xmlBuff.append("<eff_date><![CDATA["+ sdf.format(currDate).toString() +"]]></eff_date>");
				xmlBuff.append("<fin_entity><![CDATA["+ finEntity   +"]]></fin_entity>");
				xmlBuff.append("<site_code><![CDATA["+siteCodeCr +"]]></site_code>");
				xmlBuff.append("<sundry_type><![CDATA["+ "C"  +"]]></sundry_type>");
				xmlBuff.append("<sundry_code><![CDATA["+ custCodeCr  +"]]></sundry_code>");
				xmlBuff.append("<acct_code><![CDATA["+checkNull(acctCode)  +"]]></acct_code>");
				xmlBuff.append("<curr_code><![CDATA["+ checkNull(cctrCode) +"]]></curr_code>");
				
				
				
				
				
				
				
				
				//crNtTranId = generateTranId("W_MISC_DRCR_RCP_CR", getCurrdateInAppFormat(), loginSite, userId, tranType );
				/*drNtTranId = generateTranId("W_MISC_DRCR_RCP_DR", getCurrdateInAppFormat(), siteCodeCr, userId, tranType );
				//-----------------------Inserting into header-----------------------------------
				pstmtHdr.setString( 1, drNtTranId );
				pstmtHdr.setString( 2, "MDRCRD" );
				pstmtHdr.setTimestamp( 3, currDate );
				pstmtHdr.setTimestamp( 4, currDate );
				pstmtHdr.setString( 5, finEntity );
				pstmtHdr.setString( 6, siteCodeCr);
				pstmtHdr.setString( 7, "C" );
				pstmtHdr.setString( 8, custCodeCr );
				pstmtHdr.setString( 9, acctCode == null ? "" : acctCode );
				pstmtHdr.setString( 10, cctrCode == null ? "    " : cctrCode );*/
				sql = "select nvl(round,'N') ls_round, nvl(round_to,0.001) lc_round_to from customer where cust_code = ? ";
				pstmt1 = conn.prepareStatement( sql );
				pstmt1.setString(1, custCodeCr );
				rSet = pstmt1.executeQuery();
				if( rSet.next() )
				{
					round = rSet.getString( "ls_round" );
					roundTo = rSet.getDouble( "lc_round_to" );
				}
				rSet.close();
				rSet = null;
				pstmt1.close();
				pstmt1 = null;

				roundAmt = disComm.getRndamt( drcrAmt, round, roundTo );
				
				System.out.println("drcrAmt in debit note["+drcrAmt+"]roundAmt["+roundAmt);
				
				if(drcrAmt < 0)
				{
					drcrAmt = drcrAmt * (-1);
				}
				
				
				
				System.out.println("drcrAmt IN debit note"+drcrAmt);
				
				diffAmt = roundAmt - drcrAmt;
				
				//pstmtHdr.setDouble( 11, drcrAmt );//CHANGED BY POONAM
				/*pstmtHdr.setDouble( 11, roundAmt );//CHANGED BY POONAM
				pstmtHdr.setString( 12, currCode == null || currCode.trim().length() == 0 ? "" : currCode);//CHANGED BY POONAM
				pstmtHdr.setDouble( 13, exchgRate );*///CHANGED BY POONAM
				
				xmlBuff.append("<amount><![CDATA["+ drcrAmt +"]]></amount>");
				xmlBuff.append("<curr_code><![CDATA["+ checkNull(currCode ) +"]]></curr_code>");
				xmlBuff.append("<exch_rate><![CDATA["+ exchgRate +"]]></exch_rate>");
				

				if( remarks == null || remarks.trim().length() == 0 )
				{
					String tRemStr = null;
					System.out.println("tranIdSel->"+tranIdSel+" pOrderNo->"+pOrderNo+" pOrderDate->"+pOrderDate);
					tRemStr = ( "CB " + tranIdSel );
					//pstmtHdr.setString( 14, tRemStr ); //CHANGED BY POONAM
					xmlBuff.append("<remarks><![CDATA["+ tRemStr  +"]]></remarks>");
				}
				else
				{
					//pstmtHdr.setString( 14, remarks.trim() );//CHANGED BY POONAM
					xmlBuff.append("<remarks><![CDATA["+ remarks.trim()  +"]]></remarks>");
				}
				
				xmlBuff.append("<drcr_flag><![CDATA["+ "D"   +"]]></drcr_flag>");
				xmlBuff.append("<tran_id__rcv><![CDATA["+ ""   +"]]></tran_id__rcv>");
				xmlBuff.append("<confirmed><![CDATA["+ "N"   +"]]></confirmed>");
				xmlBuff.append("<chg_user><![CDATA["+ userId   +"]]></chg_user>");
				xmlBuff.append("<chg_date><![CDATA["+ sdf.format(currDate).toString()   +"]]></chg_date>");
				xmlBuff.append("<chg_term><![CDATA["+ termId   +"]]></chg_term>");
				xmlBuff.append("<conf_date><![CDATA["+ sdf.format(currDate).toString()   +"]]></conf_date>");
				
				xmlBuff.append("<emp_code__aprv><![CDATA["+ empCodeAprv   +"]]></emp_code__aprv>");
				xmlBuff.append("<due_date><![CDATA["+ sdf.format(currDate).toString()   +"]]></due_date>");
				xmlBuff.append("<tran_type><![CDATA["+ checkNull(tranType)   +"]]></tran_type>");
				xmlBuff.append("<item_ser><![CDATA["+ checkNull(itemSer )   +"]]></item_ser>");
				xmlBuff.append("<amount__bc><![CDATA["+  roundAmt * exchgRate   +"]]></amount__bc>");
				xmlBuff.append("<sreturn_no><![CDATA["+ checkNull(tranIdSel)   +"]]></sreturn_no>");
				xmlBuff.append("<adj_misc_crn><![CDATA["+ ""   +"]]></adj_misc_crn>");
				xmlBuff.append("<adj_amount><![CDATA["+ 0.0   +"]]></adj_amount>");
				xmlBuff.append("<parent__tran_id><![CDATA["+ ""   +"]]></parent__tran_id>");
				xmlBuff.append("<rev__tran><![CDATA["+ ""   +"]]></rev__tran>");
				xmlBuff.append("<round_adj><![CDATA["+ 0.0   +"]]></round_adj>");
                xmlBuff.append("<cust_ref_no><![CDATA["+ ""   +"]]></cust_ref_no>");  
                
				//xmlBuff.append("<cust_ref_date><![CDATA["+  sdf.format(currDate).toString() +"]]></cust_ref_date>");
				/*xmlBuff.append("<cust_ref_amt><![CDATA["+ 0.0   +"]]></cust_ref_amt>");
				xmlBuff.append("<rnd_off><![CDATA["+ 0.0   +"]]></rnd_off>");
				xmlBuff.append("<rnd_to><![CDATA["+ 0.0   +"]]></rnd_to>");*/
				
				//POONAM
				
				/*pstmtHdr.setString( 15, "D" );//Changed by Poonam to created debit note..
				pstmtHdr.setString( 16, "" );
				pstmtHdr.setString( 17, "N" );
				pstmtHdr.setString( 18, userId );
				pstmtHdr.setTimestamp( 19, currDate );
				pstmtHdr.setString( 20, termId );
				pstmtHdr.setNull( 21, java.sql.Types.DATE );
				pstmtHdr.setString( 22, empCodeAprv );
				pstmtHdr.setTimestamp( 23, currDate );
				pstmtHdr.setString( 24, ( tranType == null || tranType.trim().length() == 0 ? "" :tranType.trim() ) );
				pstmtHdr.setString( 25, ( itemSer == null || itemSer.trim().length() == 0 ? "" : itemSer.trim() ) );
				//pstmtHdr.setDouble( 26, drcrAmt );
				pstmtHdr.setDouble( 26, roundAmt * exchgRate);
				pstmtHdr.setString( 27, ( tranIdSel == null || tranIdSel.trim().length() == 0 ? "" :  tranIdSel.trim() ) );
				pstmtHdr.setString( 28, "" ); //ADJ_MISC_CRN
				pstmtHdr.setDouble( 29, 0.0 ); //ADJ_AMOUNT
				pstmtHdr.setString( 30, "" ); //PARENT__TRAN_ID
				pstmtHdr.setString( 31, "" ); //REV__TRAN
				pstmtHdr.setDouble( 32, 0.0 ); //ROUND_ADJ
				//pstmt.setInt( 33, 0 ); //LINE_NO__SRET
				//pstmt.setInt( 34, 0 ); //LINE_NO__SRET
				//pstmt.setString( 35, "" ); //LOT_NO
				pstmtHdr.setString( 33, "" ); //CUST_REF_NO
				pstmtHdr.setNull( 34, java.sql.Types.DATE ); //CUST_REF_DATE
				pstmtHdr.setDouble( 35, 0.0 ); //CUST_REF_AMT
				pstmtHdr.setDouble( 36, 0.0 ); //RND_OFF
				pstmtHdr.setDouble( 37, 0.0 ); //RND_TO

				int updtCnt = pstmtHdr.executeUpdate();*/
				//System.out.println("updtCnt==>"+updtCnt);
				//-----------------------Inserting into detail-----------------------------------
				
				System.out.println("drcrAmt in debit detail"+drcrAmt);
				System.out.println("grossAmount in debit detail"+grossAmount);
				//llLineNo = 0;////Commented by Manoj dtd 04/01/2018 not to reset variable
				
				analCode = finCommon.getFinparams("999999", "ANAL_CODE", conn);
				
				System.out.println("analCode:::"+analCode);
				
				//xmlBuffDet = new StringBuffer();   //commented by manish mhatre on 11-sep-20
				
				xmlBuffDet.append("<Detail2 dbID=\"\" domID=\""+llLineNo+"\" objName=\"misc_drcr_rcp_dr\" objContext=\"2\">");//Changes made by ashutosh on 31-05-2018   
				xmlBuffDet.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
				xmlBuffDet.append("<tran_id/>");	
				llLineNo++;
				detLineNoList.add(lineNo+":"+llLineNo);
				xmlBuffDet.append("<line_no><![CDATA["+ llLineNo   +"]]></line_no>");
				xmlBuffDet.append("<acct_code><![CDATA["+ detAcct   +"]]></acct_code>");
				xmlBuffDet.append("<cctr_code><![CDATA["+ detCctr   +"]]></cctr_code>");
				
				
				
				//CHANGED BY POONAM
				
				/*pstmtDtl.setString( 1, drNtTranId );
				llLineNo++;
				pstmtDtl.setInt( 2, llLineNo );
				pstmtDtl.setString( 3, detAcct );
				pstmtDtl.setString( 4, detCctr );*/
				
				if(drcrAmt < 0)
				{
					drcrAmt = drcrAmt * (-1);
				}
				if(grossAmount < 0)
				{
					grossAmount = grossAmount * (-1);
				}
				if(taxAmt < 0)
				{
					taxAmt = taxAmt * (-1);
				}
				System.out.println("drcrAmt in debit detail"+drcrAmt);
				System.out.println("grossAmount in debit detail"+grossAmount);
				System.out.println("taxAmt in debit detail"+taxAmt);

				//if( claimAmt > 0 && claimAmt < netAmt )
				//{
					//pstmtDtl.setDouble( 5, drcrAmt );
					//pstmtDtl.setDouble( 6, drcrAmt );
					//CHANGED BY POONAM
					xmlBuffDet.append("<amount><![CDATA["+ grossAmount   +"]]></amount>");
					xmlBuffDet.append("<net_amt><![CDATA["+ drcrAmt   +"]]></net_amt>");
					xmlBuffDet.append("<net_amt><![CDATA["+ taxAmt   +"]]></net_amt>");
				//}
				/*else
				{
					//pstmtDtl.setDouble( 5, grossAmount );
					//pstmtDtl.setDouble( 6, grossAmount );
					//CHANGED BY POONAM
					xmlBuffDet.append("<amount><![CDATA["+ grossAmount   +"]]></amount>");
					xmlBuffDet.append("<net_amt><![CDATA["+ grossAmount   +"]]></net_amt>");
				}*/
				//CHANGED BY POONAM
				/*pstmtDtl.setString( 7, tranIdSel ); //ref_no
				pstmtDtl.setString( 8, ( reasonCd == null || reasonCd.trim().length() == 0 ? "" : reasonCd.trim() ) ); //reas_code
				pstmtDtl.setString( 9, "" ); //ANAL_CODE
*/				
				
				xmlBuffDet.append("<ref_no><![CDATA["+ tranIdSel   +"]]></ref_no>");
				xmlBuffDet.append("<reas_code><![CDATA["+  checkNull(reasonCd)    +"]]></reas_code>");
				xmlBuffDet.append("<anal_code><![CDATA["+ analCode   +"]]></anal_code>");
				xmlBuffDet.append("<tax_class><![CDATA["+ checkNullAndTrim(taxClass) +"]]></tax_class>");
				xmlBuffDet.append("<tax_chap><![CDATA["+ checkNullAndTrim(taxChap) +"]]></tax_chap>");
				xmlBuffDet.append("<tax_env><![CDATA["+ checkNullAndTrim(taxEnv) +"]]></tax_env>");
				
				xmlBuffDet.append("</Detail2>");

				//Changed by poonam :taxtran not required
				
				/*pstmtDtl.addBatch();

				sql = "select acct_code, cctr_code, sum(tax_amt) taxamt "
					+" from taxtran "
					+" where tran_code = 'S-CHB' and tran_id = ? "
					+" and tax_amt <> 0 "
					+" and effect <> 'N' "
					+" group by acct_code, cctr_code " ;
				//
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, tranIdSel);
				rSet = pstmt1.executeQuery();
				while( rSet.next() )
				{
					acctCodeTax = rSet.getString( "acct_code" );
					
					cctrCodeTax = rSet.getString( "acct_code" );
					amtTax =  rSet.getDouble( "taxamt" );

					if( acctCodeTax == null || acctCodeTax.trim().length() == 0 )
					{
						acctCodeTax = detAcct;
					}
					if( cctrCodeTax == null || cctrCodeTax.trim().length() == 0 )
					{
						cctrCodeTax = detCctr;
					}

					total = total + amtTax;

					pstmtDtl.setString( 1, drNtTranId );

					llLineNo++;
					pstmtDtl.setInt( 2, llLineNo );
					pstmtDtl.setString( 3, acctCodeTax );
					pstmtDtl.setString( 4, acctCodeTax );
					pstmtDtl.setDouble( 5, amtTax);
					pstmtDtl.setDouble( 6, 0.0 );
					pstmtDtl.setString( 7, "" ); //ref_no
					pstmtDtl.setString( 8, "" ); //reas_code
					pstmtDtl.setString( 9, "" ); //ANAL_CODE

					pstmtDtl.addBatch();
					
				}
				rSet.close();
				rSet = null;
				pstmt1.close();
				pstmt1 = null;

				pstmtDtl.executeBatch();

				pstmtDtl.close();
				pstmtDtl = null;
				pstmtHdr.close();
				pstmtHdr = null;*/

				

				/*sql = "select nvl(round,'N') ls_round, nvl(round_to,0.001) lc_round_to from customer where cust_code = ? ";
				pstmt1 = conn.prepareStatement( sql );
				pstmt1.setString(1, custCodeCr );
				rSet = pstmt1.executeQuery();
				if( rSet.next() )
				{
					round = rSet.getString( "ls_round" );
					roundTo = rSet.getDouble( "lc_round_to" );
				}
				rSet.close();
				rSet = null;
				pstmt1.close();
				pstmt1 = null;

				roundAmt = disComm.getRndamt( drcrAmt, round, roundTo );
				diffAmt = roundAmt - drcrAmt;*/
				
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			xmlBuff.append("</Detail1>");
			xmlBuff.append(xmlBuffDet);
			xmlBuff.append("</Header0>");
			xmlBuff.append("</group0>");
			xmlBuff.append("</DocumentRoot>");
			xmlString = xmlBuff.toString();
//			System.out.println("@@@@@2: xmlString:"+xmlBuff.toString());
//			System.out.println("...............just before savdata distorder()");
//			siteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
//			System.out.println("== site code =="+siteCode);
//			errString = saveData(siteCode,xmlString,xtraParams,conn);
//			System.out.println("@@@@@2: retString:"+errString);
//			System.out.println("--retString finished--");
//			if (errString.indexOf("Success") > -1)
//			{
//				System.out.println("@@@@@@3: Success"+errString);
//				dom = genericUtility.parseString(errString);
//				System.out.println("dom>>>"+dom);
//				drNtTranId = genericUtility.getColumnValue("TranID",dom);
//			}
//			else
//			{
//				System.out.println("[SuccessSuccess" + errString + "]");	
//				conn.rollback();
//				return errString;
//			}
//			
//			
//			/*sql = "update charge_back set "
//					+" tran_id__crn = ? "
//					+" where tran_id = ? ";
//
//			pstmt1 = conn.prepareStatement( sql );
//			pstmt1.setString(1, drNtTranId );
//			pstmt1.setString(2, tranIdSel );
//			int updCount = 0 ;
//			updCount = pstmt1.executeUpdate();
//
//			if( updCount == 0 )
//			{
//				errCode = "DS000NR";
//					//break;
//			}
//			pstmt1.close();
//			pstmt1 = null;
//			*/
//			
//
//			if( "Y".equalsIgnoreCase( asPost )  )
//			{
//				conn.commit();
//				//retString = retrieveMiscDrcrRcp( "misc_drcr_rcp_dr",drNtTranId,xtraParams,"N" );
//				//retString = autoConfirmRecord("misc_drcr_rcp_dr", drNtTranId, xtraParams);
//				retString = executeSystemEvent("misc_drcr_rcp_dr", "pre_confirm", drNtTranId, xtraParams, conn);
//				System.out.println("retString ::: " + retString);
//				/*if(retString.indexOf("VTSUCC1") > -1 || retString.indexOf("CONFSUCCES") > -1){
//					conn.commit();
//				}*/
//				errCode = getErrCodeFromErrStr(retString);
//			}
			if(countData==0)
			{
				xmlString="";	
				detlineNo="";
			}
			else
			{
				//dataMap.put(xmlString, detlineNo.substring(0,detlineNo.length()-1));
				dataMap.put(xmlString, detLineNoList);
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if(pstmtDtl != null)
				{
					pstmtDtl.close();
					pstmtDtl = null;
				}
				if(pstmtHdr != null)
				{
					pstmtHdr.close();
					pstmtHdr = null;
				}
			}
			catch(Exception e)
			{
				System.out.println("Exception ChrgBckLocConfEJB....... :\n"+e.getMessage());
			}
		}
		return dataMap;
	}
	// Added for Debit note generation if amt < 0: Start
	
	private String saveData(String siteCode,String xmlString, String xtraParams,Connection conn) throws ITMException
	{
		System.out.println("saving data...........");
		InitialContext ctx = null;
		String retString = null;
		MasterStatefulLocal masterStateful = null; // for ejb3
		ibase.utility.UserInfoBean userInfo;
		String chgUser = "", chgTerm = "";
		String loginCode = "", loginEmpCode = "", loginSiteCode = "";
		try
		{
			AppConnectParm appConnect = new AppConnectParm();
			ctx = new InitialContext(appConnect.getProperty());
			masterStateful = (MasterStatefulLocal)ctx.lookup("ibase/MasterStatefulEJB/local");
			System.out.println("-----------masterStateful------- " + masterStateful);
			String [] authencate = new String[2];
			authencate[0] = "";
			authencate[1] = "";
			System.out.println("xmlString to masterstateful [" + xmlString + "]");
			
			userInfo = new ibase.utility.UserInfoBean();
			System.out.println("xtraParams>>>>" + xtraParams);
			//chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "chgUser"); // 04-oct-2019 manoharan
			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode"); // 04-oct-2019 manoharan
			
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "chgTerm");
			loginCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			loginEmpCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			userInfo.setEmpCode(loginEmpCode);
			userInfo.setRemoteHost(chgTerm);
			userInfo.setSiteCode(loginSiteCode);
			userInfo.setLoginCode(loginCode);
			userInfo.setEntityCode(loginEmpCode);
			System.out.println("userInfo>>>>>" + userInfo);

			System.out.println("chgUser :" + chgUser);
			System.out.println("chgTerm :" + chgTerm);
			System.out.println("loginCode :" + loginCode);
			System.out.println("loginEmpCode :" + loginEmpCode);
			authencate[0] = chgUser; // 04-oct-2019 manoharan
			
			//retString = masterStateful.processRequest(userInfo, siteCode, true, xmlString,true,conn);
			//retString = masterStateful.processRequest(userInfo, xmlString, true, conn);
			retString = masterStateful.processRequest(authencate, siteCode, true, xmlString,true,conn); // 04-oct-2019 manoharan 
			System.out.println("--retString - -"+retString);
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
	private String checkNull(String input)	
	{
		if (input == null)
		{
			input="";
		}
		return input.trim();
	}
	private String checkNullAndTrim(String input)	
	{
		return (input == null || "null".equalsIgnoreCase(input.trim())) ? "" : input.trim();
	}
	private String autoConfirmRecord(String objName, String tranIdForIssue, String xtraParams) throws ITMException {
		InitialContext ctx = null;
		String xmlInEditMode = "", retString = "";
		MasterStatefulLocal masterStateful = null;
		AppConnectParm appConnect = new AppConnectParm();
		UserInfoBean userInfo = new UserInfoBean();
		try {
			userInfo.setLoginCode(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode"));
			userInfo.setEmpCode(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode"));
			userInfo.setSiteCode(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode"));
			userInfo.setEntityCode(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "entityCode"));
			userInfo.setProfileId(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "profileId"));
			userInfo.setUserType(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "userType"));
			userInfo.setRemoteHost(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId"));
			ctx = new InitialContext(appConnect.getProperty());
			masterStateful = (MasterStatefulLocal) ctx.lookup("ibase/MasterStatefulEJB/local");
			masterStateful.setUserInfo(userInfo);
			xmlInEditMode = masterStateful.getDetailXMLDomString(objName, "1", "E", tranIdForIssue, "");
			System.out.println("xmlInEditMode ::: " + xmlInEditMode);
			retString = masterStateful.executeConfirm(xmlInEditMode);
			System.out.println("retString ::: " + retString);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
		}
		return retString;
	}
	
	
	private String executeSystemEvent(String objName, String eventCode, String tranId, String xtraParams, Connection conn) throws ITMException {
		String retValue = "";
		System.out.println("Getting transaction in Edit Mode. START");
		String xmlInEditMode = getXmlInEditMode(objName, tranId, xtraParams);
		System.out.println("Getting transaction in Edit Mode. END");
		try {
			InitialContext ctx = null;
			AppConnectParm appConnect = new AppConnectParm();
			ctx = new InitialContext(appConnect.getProperty());
			EventManagerEJB eventManager = new EventManagerEJB();
			retValue = eventManager.executeSystemEvent(objName, eventCode, null, xmlInEditMode, xmlInEditMode, xtraParams, "1");
			eventManager = null;
		} catch (Exception e) {
			throw new ITMException(e);
		}
		return retValue;
	}
	
	private String getXmlInEditMode(String objName, String tranId, String xtraParams) throws ITMException {
		InitialContext ctx = null;
		String xmlInEditMode = "";
		MasterStatefulLocal masterStateful = null;
		AppConnectParm appConnect = new AppConnectParm();
		UserInfoBean userInfo = new UserInfoBean();
		try {
			userInfo.setLoginCode(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode"));
			userInfo.setEmpCode(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode"));
			userInfo.setSiteCode(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode"));
			userInfo.setEntityCode(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "entityCode"));
			userInfo.setProfileId(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "profileId"));
			userInfo.setUserType(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "userType"));
			userInfo.setRemoteHost(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId"));
			ctx = new InitialContext(appConnect.getProperty());
			masterStateful = (MasterStatefulLocal) ctx.lookup("ibase/MasterStatefulEJB/local");
			masterStateful.setUserInfo(userInfo);
			xmlInEditMode = masterStateful.getDetailXMLDomString(objName, "1", "E", tranId, "");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
		}
		System.out.println("Returning from MasterStateful xmlInEditMode ::: " + xmlInEditMode);
		return xmlInEditMode;
	}
	
	public void insertDataInSchemeBal(Map<String,String> map) throws ITMException
	{
		System.out.println("ChrgBckLocConf.insertDataInSchemeBal() : "+map);
		PreparedStatement pStmt=null;
		ResultSet rs=null;
		Connection conn=null;
		int updatedCount;
		try
		{
			conn=getConnection();
			String currentDate1=map.get("currentdate");
			SimpleDateFormat sdf=new SimpleDateFormat(genericUtility.getDBDateFormat());
			Date currentdate1=sdf.parse(currentDate1);
			Timestamp currentDateTs = new Timestamp(currentdate1.getTime());
			String vaildUpto1=map.get("validUpto");
			Date validUptoDate=sdf.parse(vaildUpto1);
			Timestamp validUptoTs=new Timestamp(validUptoDate.getTime());
			TransIDGenerator idGenerator = new TransIDGenerator("<Root></Root>", userId,getUserInfo().getTransDB());
			String transId = idGenerator.generateTranSeqID("S-BAL", "TRAN_ID", "seq10", conn);
			StringBuilder schemebalsb = new StringBuilder();
			schemebalsb.append("INSERT INTO SCHEME_BALANCE(TRAN_ID,SCHEME_CODE,CUST_CODE,ITEM_CODE,EFF_FROM,VALID_UPTO,BALANCE_FREE_QTY,");
			schemebalsb.append("BALANCE_FREE_VALUE,USED_FREE_QTY,USED_FREE_VALUE,CHG_USER,CHG_DATE,CHG_TERM,ENTRY_SOURCE, SITE_CODE)");
			schemebalsb.append("VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			pStmt = conn.prepareStatement(schemebalsb.toString());
			pStmt.setString(1, transId);
			pStmt.setString(2, map.get("offer"));
			pStmt.setString(3, map.get("custCode"));
			pStmt.setString(4, map.get("itemCodeRepl"));
			pStmt.setTimestamp(5, currentDateTs);
			pStmt.setTimestamp(6, validUptoTs);
			pStmt.setString(7, map.get("freeQty"));
			pStmt.setString(8, map.get("freeVaule"));
			pStmt.setString(9, map.get("balanceFreeValue"));
			pStmt.setString(10,map.get("balanceFreeQty"));
			pStmt.setString(11, map.get("chgUser"));
			pStmt.setTimestamp(12, currentDateTs);
			pStmt.setString(13, map.get("ChgTerm"));
			pStmt.setString(14, map.get("entrySource"));
			pStmt.setString(15, map.get("siteCode"));
			updatedCount = pStmt.executeUpdate();
			if(pStmt!=null)
			{
				pStmt.close();
				pStmt = null;
			}
			System.out.println("updated row:"+updatedCount);
			if (updatedCount > 0) 
			{
				conn.commit();
			} 
			else 
			{
				conn.rollback();
			}
		}
		catch(Exception e)
		{
			System.out.println(e);
			throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
		}
		finally
		{
			try
			{
				if(conn!=null)
				{
					conn.close();
				}
				if(pStmt != null)
				{
					pStmt.close();
					pStmt = null;
				}
				if(rs != null)
				{
					rs.close();
					rs = null;
				}
			}
			catch(Exception e)
			{
				System.out.println(e);
			}
		}
	}
	public Map getStockiestData(String offer,String custCode1,String itemCodeRepl,String currentDate,String validUpto,String freeQty,String freeVaule,String balanceFreeValue,String balanceFreeQty,String userId,String currDate,String termId,String entrySource,String siteCode)
	{
		HashMap map=new HashMap(); 
		map.put("offer", offer);
		map.put("custCode", custCode1);
		map.put("itemCodeRepl",itemCodeRepl);
		map.put("currentdate", currentDate);
		map.put("validUpto", validUpto);
		map.put("freeQty", freeQty);
		map.put("freeVaule", freeVaule);
		map.put("balanceFreeValue", balanceFreeValue);
		map.put("balanceFreeQty", balanceFreeQty);
		map.put("chgUser", userId);
		map.put("currDate", currDate);
		map.put("ChgTerm", termId);
		map.put("entrySource",entrySource);
		map.put("siteCode",siteCode);

		return map;
	}
	//Added by Saiprasad G. on 24-JAN-19 START [For inserting the data in scheme trace]
	public void insertSchemeTrace(Timestamp tranDate,String schemeAprvId,String custCode,String schemeCode,double usedAmt,double balanceAmt) throws ITMException
	{
		System.out.println("In ChrgBckLocConf.insertSchemeTrace():");
		PreparedStatement schemeTracePstmt=null;
		Connection con = null;
		ResultSet schemeTraceRS = null;
		try
		{
			con=getConnection();
			StringBuilder schemeTrace = new StringBuilder();
			schemeTrace.append("INSERT INTO SCHEME_TRACE(TRAN_ID,TRAN_DATE,SCHEME_APRV_ID,CUST_CODE,SCHEME_CODE,USED_AMT,BALANCE_AMT)");
			schemeTrace.append("VALUES (?,?,?,?,?,?,?)");
			TransIDGenerator tranIdGen = new TransIDGenerator("<Root></Root>", userId,getUserInfo().getTransDB());
			String transId = tranIdGen.generateTranSeqID("S-TRC", "TRAN_ID", "seq10", con);
			schemeTracePstmt=con.prepareStatement(schemeTrace.toString());
			schemeTracePstmt.setString(1,transId);
			schemeTracePstmt.setTimestamp(2, tranDate);
			schemeTracePstmt.setString(3, schemeAprvId);
			schemeTracePstmt.setString(4, custCode);
			schemeTracePstmt.setString(5, schemeCode);
			schemeTracePstmt.setDouble(6, usedAmt);
			schemeTracePstmt.setDouble(7, balanceAmt);
			int i = schemeTracePstmt.executeUpdate();
			System.out.println("data inserted in scheme_trace:"+i);
			if(schemeTracePstmt!=null)
			{
				schemeTracePstmt.close();
				schemeTracePstmt=null;
			}
			if(i>0)
			{
				con.commit();
			}
			else
			{
				con.rollback();
			}
		}
		catch(Exception e)
		{
			System.out.println("Excption in ChrgBckLocConf.insertSchemeTrace():");
			throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
		}
		finally
		{
			try
			{
				if(schemeTracePstmt!=null)
				{
					schemeTracePstmt.close();
					schemeTracePstmt=null;
				}
				if(con!=null)
				{
					con.close();
					con = null;
				}
			}
			catch(Exception e)
			{
				System.out.println("Exception closing connection"+e);
			}
		}
	}
	//Added by Saiprasad G. on 24-JAN-19 END [For inserting the data in scheme trace]
 
	
}