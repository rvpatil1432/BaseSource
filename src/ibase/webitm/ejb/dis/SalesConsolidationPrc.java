package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import ibase.system.config.AppConnectParm;
import ibase.system.config.ConnDriver;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.MasterStatefulLocal;
import ibase.webitm.ejb.ProcessEJB;
import ibase.webitm.utility.ITMException;
import ibase.webitm.utility.TransIDGenerator;

import javax.ejb.Stateless;
import javax.naming.InitialContext;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


@Stateless
public class SalesConsolidationPrc extends ProcessEJB implements SalesConsolidationPrcLocal , SalesConsolidationPrcRemote 
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();

	@Override
	public String process(String xmlString, String xmlString2,String windowName, String xtraParams) throws RemoteException,ITMException
	{
		String rtStr = "";
		Document dom = null;
		Document dom2 = null;
		try {
			if (xmlString != null && xmlString.trim().length() != 0) {
				dom = genericUtility.parseString(xmlString);
				System.out.println("Process Dom::::::::::::::::"+dom );
			}
			if (xmlString2 != null && xmlString2.trim().length() != 0) {
				dom2 = genericUtility.parseString(xmlString2);
				System.out.println("Process Dom2::::::::::::::::"+dom2 );
			}
			rtStr = process(dom, dom2, windowName, xtraParams);
		} catch (Exception e) {
			System.out.println("::::"+this.getClass().getSimpleName()+"::processDataString" + e.getMessage());
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return rtStr;
	}
	
	@Override
	public String process(Document dom, Document dom2, String windowName,String xtraParams) throws RemoteException, ITMException {
		
		String errString = "", userID = "", prdCode = "" , itemSer = "" ,  tranId = "" , loginSiteCode = "" , sql = "" , sql1 = "" , userId = "" , xmlInEditMode = "" ,
				unit = "" , empCode = "" , posCode = "" , custCode = "" , itemCode = "" , tranDateStr = "" , retString = "" , xmlParseStr = "" , versionId = ""
				, stanCode = "" , stanCodeNew = "" , terrCode = "" , terrDescr = "" , lastYrPrdCode = "";
		double opStock = 0.0 , clStock = 0.0 , OpValue = 0.0 , clValue = 0.0 , sales = 0.0 , salesValue = 0.0 , rcpBillQty = 0.0 , rcpBillVal = 0.0 , tranBillQty = 0.0,
				tranBillVal = 0.0 , retQty = 0.0 , retVal = 0.0 , tranRepQty = 0.0 , tranRepVal = 0.0 ,  tranBonusQty = 0.0 , lastYrsSale = 0.0 , lastYrsSaleVal = 0.0  
				, tranBonusVal = 0.0 ,  rcpBonusQty = 0.0 ,rcpBonusVal=0.0, rcpRplQty = 0.0 , rcpRplVal = 0.0 , grossQty = 0.0 , grossRate=0.0 , grossVal = 0.0; 
		Connection conn=null;
		int ctr = 0 , count = 0 , cnt = 0 , updCnt = 0 , period = 0;
		String chgTerm="",chgUser="",overWrite="",countryCode="";
		PreparedStatement pstmt = null , pstmt1 = null;
		ResultSet rs = null , rs1 = null;
		Timestamp tranDate = null , frDate = null , toDate = null ;
		StringBuffer xmlBuff = null;
		System.out.println("Current DOM [" + genericUtility.serializeDom(dom) + "]");
		System.out.println("Header DOM [" + genericUtility.serializeDom(dom2) + "]");
		java.sql.Date sysDate=null;
		try {
			System.out.println("In process Sales Consolidation:::");
			userId = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "userID"));
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId");
			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			loginSiteCode = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode"));
			prdCode = checkNull(genericUtility.getColumnValue("prd_code", dom));
			itemSer = checkNull(genericUtility.getColumnValue("item_ser", dom));
			overWrite = checkNull(genericUtility.getColumnValue("overwrite", dom));
			System.out.println("prdCode>>>"+prdCode+">>itemSer>>"+itemSer+">>overWrite>>"+overWrite);
			System.out.println("userId : "+userId);
			System.out.println("xtraParams ::: "+xtraParams);
			ConnDriver con = new ConnDriver();
			//conn = con.getConnectDB("DriverITM");
			conn = getConnection() ;
			sql1 = "select sysdate from dual";
			pstmt = conn.prepareStatement(sql1);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				sysDate = rs.getDate(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			sql= "select count_code from state where " +
					"state_code in (select state_code from site where site_code=?)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, loginSiteCode );
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				countryCode = checkNull(rs.getString("count_code")).trim();
				System.out.println("countryCode >>> :"+countryCode);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			sql = " SELECT FR_DATE,TO_DATE FROM PERIOD_TBL WHERE PRD_CODE=? AND PRD_TBLNO=? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, prdCode);
			pstmt.setString(2, countryCode+"_"+itemSer.trim());
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				frDate = rs.getTimestamp("FR_DATE");
				toDate = rs.getTimestamp("TO_DATE");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("frDate :::"+frDate+">>toDate :::"+toDate);

			sql = "SELECT VERSION_ID FROM VERSION WHERE EFF_FROM < = ? AND VALID_UPTO > = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setTimestamp(1, frDate);
			pstmt.setTimestamp(2, toDate);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				versionId = checkNull(rs.getString("VERSION_ID"));
				System.out.println("versionId ::: "+versionId);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			if("Y".equalsIgnoreCase(overWrite)){
				cnt=0;
				sql = "DELETE FROM SALES_CONSOLIDATION WHERE PRD_CODE = ? AND ITEM_SER=? AND SOURCE='E'";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, prdCode);
				pstmt.setString(2, itemSer);	
				cnt=pstmt.executeUpdate();
				System.out.println("Delete count>>"+cnt);
				pstmt.close();
				pstmt = null;
			}
				System.out.println("Inserting data ");
				sql = " SELECT A.TRAN_ID , A.TRAN_DATE , A.PRD_CODE, A.FROM_DATE , A.TO_DATE , A.CUST_CODE , A.POS_CODE ,  A.ITEM_SER , A.EMP_CODE , B.ITEM_CODE , B.UNIT , B.OP_STOCK, B.OP_VALUE , " +
					  " B.PURC_RCP , B.RCP_VAL , B.TRANSIT_QTY , B.TRANSIT_BILL_VAL , B.PURC_RCP__REPL , B.RCP_REPL_VAL , B.TRANSIT_QTY__REPL , B.TRANSIT_REPL_VAL," +
					  " B.CL_STOCK , B.CL_VALUE , B.PURC_RET , B.RET_VAL , B.SALES , B.SALES_VALUE , B.PURC_RCP__FREE , B.RCP_FREE_VAL , B.TRANSIT_QTY__FREE," +
					  " B.TRANSIT_FREE_VAL , B.SALES__ORG , B.RATE__ORG , D.STAN_CODE FROM CUST_STOCK A JOIN CUST_STOCK_DET B ON A.TRAN_ID = B.TRAN_ID " +
					  " JOIN CUSTOMER D ON D.CUST_CODE = A.CUST_CODE  WHERE A.PRD_CODE = ? AND A.ITEM_SER=? AND A.POS_CODE IS NOT NULL " ;
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, prdCode);
				pstmt.setString(2, itemSer);
				rs = pstmt.executeQuery();
				while(rs.next())
				{
					prdCode = checkNull(rs.getString("PRD_CODE"));
					custCode = checkNull(rs.getString("CUST_CODE"));
					itemSer = checkNull(rs.getString("ITEM_SER"));
					empCode = checkNull(rs.getString("EMP_CODE"));
					posCode = checkNull(rs.getString("POS_CODE"));
					itemCode = checkNull(rs.getString("ITEM_CODE"));
					frDate = rs.getTimestamp("FROM_DATE");
					toDate = rs.getTimestamp("TO_DATE");
					unit = checkNull(rs.getString("UNIT"));
					stanCode = checkNull(rs.getString("STAN_CODE"));
					opStock = rs.getDouble("OP_STOCK");
					OpValue = rs.getDouble("OP_VALUE");
					rcpBillQty = rs.getDouble("PURC_RCP");
					rcpBillVal = rs.getDouble("RCP_VAL");
					tranBillQty = rs.getDouble("TRANSIT_QTY");
					tranBillVal = rs.getDouble("TRANSIT_BILL_VAL");
					rcpRplQty = rs.getDouble("PURC_RCP__REPL");
					rcpRplVal = rs.getDouble("RCP_REPL_VAL");
					tranRepQty = rs.getDouble("TRANSIT_QTY__REPL");
					tranRepVal = rs.getDouble("TRANSIT_REPL_VAL");
					clStock = rs.getDouble("CL_STOCK");
					clValue = rs.getDouble("CL_VALUE");
					retQty = rs.getDouble("PURC_RET");
					retVal = rs.getDouble("RET_VAL");
					sales = rs.getDouble("SALES");
					salesValue = rs.getDouble("SALES_VALUE");
					grossQty = rs.getDouble("SALES__ORG");
					grossRate = rs.getDouble("RATE__ORG");
					rcpBonusQty = rs.getDouble("PURC_RCP__FREE");
					rcpBonusVal = rs.getDouble("RCP_FREE_VAL");
					tranBonusQty = rs.getDouble("TRANSIT_QTY__FREE");
					tranBonusVal = rs.getDouble("TRANSIT_FREE_VAL");
					cnt ++ ;
					grossVal=0.0;
					grossVal = grossQty*grossRate;
					System.out.println("grossVal :::::::: "+grossVal);
					System.out.println("grossQty :::::::: "+grossQty);
					System.out.println("prdCode ::: "+prdCode);
					System.out.println("custCode ::: "+custCode);
					System.out.println("itemSer ::: "+itemSer);
					System.out.println("empCode ::: "+empCode);
					System.out.println("posCode ::: "+posCode);
					System.out.println("itemCode ::: "+itemCode);
					System.out.println("unit ::: "+unit);
					System.out.println("stanCode ::: "+stanCode);
					System.out.println("frDate ::: "+frDate);
					System.out.println("toDate ::: "+toDate);
					/*versionId="";
					sql1 = "SELECT VERSION_ID FROM VERSION WHERE EFF_FROM < = ? AND VALID_UPTO > = ?";
					pstmt1 = conn.prepareStatement(sql1);
					pstmt1.setTimestamp(1, frDate);
					pstmt1.setTimestamp(2, toDate);
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						versionId = checkNull(rs1.getString("VERSION_ID"));
						System.out.println("versionId ::: "+versionId);
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;*/
					terrCode="";terrDescr="";
					sql1 = "  SELECT A.POOL_CODE , B.LEVEL_CODE , B.LEVEL_DESCR FROM ORG_STRUCTURE A , HIERARCHY " +
							" B WHERE A.POOL_CODE = B.LEVEL_CODE AND  A.VERSION_ID = ? AND A.POS_CODE = ? AND A.TABLE_NO = ? ";
					pstmt1 = conn.prepareStatement(sql1);
					pstmt1.setString(1, versionId);
					pstmt1.setString(2, posCode);
					pstmt1.setString(3, itemSer);
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						terrCode = checkNull(rs1.getString("LEVEL_CODE"));
						terrDescr = checkNull(rs1.getString("LEVEL_DESCR"));
						System.out.println("terrCode ::: "+terrCode+ " terrDescr :::: "+terrDescr);
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					lastYrsSale=0.0;lastYrsSaleVal=0.0;
					period = Integer.parseInt(prdCode);
					period = period - 100;
					System.out.println("period :::: "+period);
					lastYrPrdCode = period+"";
					System.out.println("lastYrPrdCode :::: "+lastYrPrdCode);
					sql1 = " SELECT B.SALES , B.SALES_VALUE FROM CUST_STOCK A , CUST_STOCK_DET B WHERE A.TRAN_ID = B.TRAN_ID AND   " +
							" A.CUST_CODE = ? AND B.ITEM_CODE = ? AND A.ITEM_SER = ?  AND A.PRD_CODE = ? ";
					pstmt1 = conn.prepareStatement(sql1);
					pstmt1.setString(1, custCode);
					pstmt1.setString(2, itemCode);
					pstmt1.setString(3, itemSer);
					pstmt1.setString(4, lastYrPrdCode.trim());
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						lastYrsSale = rs.getDouble("SALES");
						lastYrsSaleVal = rs.getDouble("SALES_VALUE");
						System.out.println("lastYrPrdCode ::::: "+lastYrPrdCode+"lastYrsSale ::: "+lastYrsSale+ "lastYrsSaleVal :::: "+lastYrsSaleVal);
					}
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					lastYrPrdCode = "";
					tranId=generateTranIDForSalesConsolidationProcess("sales_consolidate",loginSiteCode,itemSer,conn);
					sql1="insert into SALES_CONSOLIDATION(TRAN_ID,TRAN_DATE,CUST_CODE,PRD_CODE,TERR_CODE,TERR_DESCR,VERSION_ID,POS_CODE," +
							"EMP_CODE,SOURCE,ITEM_CODE,UNIT,ITEM_SER,ITEM_SER_NEW,STAN_CODE,STAN_CODE_NEW," +
							"OP_STOCK,OP_VALUE,PURC_RCP,PUR_VALUE,TRANSIT_QTY,TRANSIT_BILL_VAL,PURC_RCP__REPL,RCP_REPL_VAL,TRANSIT_QTY__REPL," +
							"TRANSIT_REPL_VAL,CL_STOCK,CL_VALUE,PURC_RET,RET_VAL,SALES,SALES_VALUE," +
							"PURC_RCP__FREE,RCP_FREE_VAL,TRANSIT_QTY__FREE,TRANSIT_FREE_VAL,GROSS_SALES_QTY,GROSS_SALES_VAL," +
							//"NET_SALES_QTY,NET_SALES_VAL,LYCM_SALES_QTY,LYCM_SALES_VAL," +
							"LYSLS_SALES_QTY,LYSLS_SALES_VAL," +
							//"REMARKS1,REMARKS2,REMARKS3,REMARKS4," +
							"ADD_DATE,ADD_USER,ADD_TERM,CHG_DATE,CHG_USER,CHG_TERM)" +
							"values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
					pstmt1 = conn.prepareStatement(sql1);
					pstmt1.setString(1, tranId);
					pstmt1.setDate(2, sysDate);
					pstmt1.setString(3, custCode);
					pstmt1.setString(4, prdCode);
					pstmt1.setString(5, terrCode);
					pstmt1.setString(6, terrDescr);
					pstmt1.setString(7, versionId);
					pstmt1.setString(8, posCode);
					pstmt1.setString(9, empCode);
					pstmt1.setString(10, "E");
					pstmt1.setString(11, itemCode);
					pstmt1.setString(12, unit);
					pstmt1.setString(13, itemSer);
					pstmt1.setString(14, itemSer);
					pstmt1.setString(15, stanCode);
					pstmt1.setString(16, stanCode);
					pstmt1.setDouble(17, opStock);
					pstmt1.setDouble(18, OpValue);
					pstmt1.setDouble(19, rcpBillQty);
					pstmt1.setDouble(20, rcpBillVal);
					pstmt1.setDouble(21, tranBillQty);
					pstmt1.setDouble(22, tranBillVal);
					pstmt1.setDouble(23, rcpRplQty);
					pstmt1.setDouble(24, rcpRplVal);
					pstmt1.setDouble(25, tranRepQty);
					pstmt1.setDouble(26, tranRepVal);
					pstmt1.setDouble(27, clStock);
					pstmt1.setDouble(28, clValue);
					pstmt1.setDouble(29, retQty);
					pstmt1.setDouble(30, retVal);
					pstmt1.setDouble(31, sales);
					pstmt1.setDouble(32, salesValue);
					pstmt1.setDouble(33, rcpBonusQty);
					pstmt1.setDouble(34, rcpBonusVal);
					pstmt1.setDouble(35, tranBonusQty);
					pstmt1.setDouble(36, tranBonusVal);
					pstmt1.setDouble(37, grossQty);
					pstmt1.setDouble(38, grossVal);//
					//NET_SALES_QTY,NET_SALES_VAL,LYCM_SALES_QTY,LYCM_SALES_VAL,
					pstmt1.setDouble(39, lastYrsSale);
					pstmt1.setDouble(40, lastYrsSaleVal);
					//REMARKS1,REMARKS2,REMARKS3,REMARKS4,
					pstmt1.setDate(41, sysDate);
					pstmt1.setString(42, chgUser);
					pstmt1.setString(43, chgTerm);
					pstmt1.setDate(44, sysDate);
					pstmt1.setString(45, chgUser);
					pstmt1.setString(46, chgTerm);
					updCnt = pstmt1.executeUpdate();
					if(updCnt>0)
					{
						errString="";
						System.out.println("Data inserted!!!");
					}
					else
					{
						System.out.println("Data insertion fail!!!");
						errString = itmDBAccessEJB.getErrorString("", "VTDATAFAIL", "","", conn);
					}
					pstmt1.close();
					pstmt1= null;
				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt = null;
		}
		catch (Exception e) 
		{
			System.out.println("::::Exception::::"+this.getClass().getSimpleName()+":::::" + e.getMessage());
			e.printStackTrace();
			errString = itmDBAccessEJB.getErrorString("", "VTDATAFAIL", "","", conn);
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		finally 
		{
			try 
			{
				if (errString == null || errString.trim().length()==0) 
				{
					System.out.println("Connection Commited");
					errString = itmDBAccessEJB.getErrorString("", "VTDATASUCC","", "", conn);
					conn.commit();
				}
				else
				{
					errString = itmDBAccessEJB.getErrorString("", "VTDATAFAIL","", "", conn);
				}
				if (conn != null)
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
					conn.close();
				}
				conn = null;
			} catch (Exception d) {
				d.printStackTrace();
			}
		}
		return errString;
	}
	
	public String generateTranIDForSalesConsolidationProcess(String objName,String loginSiteCode,String itemSer,Connection conn) throws ITMException
	{
		String retString = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String keyString = "", refSer = "",sysDate="";
		E12GenericUtility genericUtility =new E12GenericUtility();
		try
		{
			SimpleDateFormat sdf= new SimpleDateFormat(genericUtility.getApplDateFormat());
			sysDate = sdf.format(new java.util.Date());
			System.out.println("SalesConsolidationProcess-ES3 :: objName =>"+objName);
			HashMap<String, String> transetupMap = new HashMap<String, String>();
			transetupMap = getTransetupMap("w_"+objName, conn);
			keyString = (String)transetupMap.get("key_string");
			refSer = (String)transetupMap.get("ref_ser");
			String xmlValues = "";
			xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
			xmlValues = xmlValues + "<Header></Header>";
			xmlValues = xmlValues + "<Detail1>";
			xmlValues = xmlValues +	"<TRAN_ID></TRAN_ID>";
			xmlValues = xmlValues +	"<TRAN_DATE>"+sysDate+"</TRAN_DATE>";
			xmlValues = xmlValues +	"<SITE_CODE>"+loginSiteCode+"</SITE_CODE>";
			xmlValues = xmlValues +	"<ITEM_SER>"+itemSer+"</ITEM_SER>";
			xmlValues = xmlValues + "</Detail1></Root>";
			System.out.println("xmlValues for Sales Consolidation :["+xmlValues+"]");
			System.out.println("keyString>>>>"+keyString+">>>refSer>>>"+refSer);
			TransIDGenerator tranIdGenerator = new TransIDGenerator(xmlValues, "SYSTEM", CommonConstants.DB_NAME);
			String tranIdGenerated = tranIdGenerator.generateTranSeqID(refSer, "tran_id", keyString, conn);
			System.out.println("tranIdGenerated for SalesConsolidationProcess-ES3 => "+tranIdGenerated);
			retString = tranIdGenerated;
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
			}
			catch(Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		return retString;
	}
	
	private HashMap<String, String> getTransetupMap(String winName, Connection conn) throws ITMException
	{
		String keyString = "";
		String refSer = "";
		String sql = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		HashMap<String, String> transetupMap = null;
		try 
		{
			sql = "SELECT KEY_STRING, REF_SER FROM TRANSETUP WHERE TRAN_WINDOW = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, winName);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				keyString = rs.getString("KEY_STRING") ;
				refSer = rs.getString("REF_SER");
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
			System.out.println("ITWizardBean :: getKeyString :: keyString =>"+keyString);
			System.out.println("ITWizardBean :: getKeyString :: refSer =>"+refSer);
			transetupMap = new HashMap<String, String>();
			transetupMap.put("key_string", keyString);
			transetupMap.put("ref_ser", refSer);
			
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
			}
			catch(Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		return transetupMap;
	}
	
	private String checkNull(String input)
	{
		return input == null ? "" : input.trim();
	}
}

