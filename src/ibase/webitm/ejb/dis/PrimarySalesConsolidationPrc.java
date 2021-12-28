package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ProcessEJB;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import javax.ejb.Stateless;

import org.w3c.dom.Document;


@Stateless
public class PrimarySalesConsolidationPrc extends ProcessEJB implements PrimarySalesConsolidationPrcLocal , PrimarySalesConsolidationPrcRemote 
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
		
		String errString = "", prdCode = "" , itemSer = "" ,  tranId = "" , loginSiteCode = "" , sql = "" , sql1 = "" , userId = "" ,
				unit = "" , empCode = "" , posCode = "" , custCode = "" , itemCode = "" , versionId = ""
				, stanCode = "" , terrCode = "" , terrDescr = "" ,countryCode = "";
		double netSalesQty=0.0,netSalesVal=0.0,lycmSalesQty=0.0,lycmSalesVal=0.0;
		Connection conn=null;
		int cnt = 0 , updCnt = 0 ;
		String chgTerm="",chgUser="",overWrite="";
		PreparedStatement pstmt = null , pstmt1 = null;
		ResultSet rs = null , rs1 = null;
		Timestamp  frDate = null , toDate = null ;
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
				sql = "DELETE FROM SALES_CONSOLIDATION WHERE PRD_CODE = ? AND ITEM_SER=? AND SOURCE='S'";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, prdCode);
				pstmt.setString(2, itemSer);	
				cnt=pstmt.executeUpdate();
				System.out.println("Delete count>>"+cnt);
				pstmt.close();
				pstmt = null;
			}
				System.out.println("Inserting data ");
				
				sql=" SELECT S.CUST_CODE,S.PRD_CODE,S.ITEM_CODE,S.ITEM_SER,S.STAN_CODE,S.NET_SALES_QTY,S.NET_SALES_VAL,S.LYCM_SALES_QTY,S.LYCM_SALES_VAL,CS.POS_CODE,CS.EMP_CODE " +
					" FROM SALES_FACT S LEFT OUTER JOIN CUST_STOCK CS ON CS.PRD_CODE=S.PRD_CODE AND CS.CUST_CODE=S.CUST_CODE AND CS.ITEM_SER=S.ITEM_SER " +
					" WHERE  S.PRD_CODE=? AND S.ITEM_SER=? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, prdCode);
				pstmt.setString(2, itemSer);
				rs = pstmt.executeQuery();
				while(rs.next())
				{
					custCode = checkNull(rs.getString("CUST_CODE"));
					prdCode = checkNull(rs.getString("PRD_CODE"));
					itemCode = checkNull(rs.getString("ITEM_CODE"));
					itemSer = checkNull(rs.getString("ITEM_SER"));
					stanCode = checkNull(rs.getString("STAN_CODE"));
					netSalesQty = rs.getDouble("NET_SALES_QTY");
					netSalesVal = rs.getDouble("NET_SALES_VAL");
					lycmSalesQty = rs.getDouble("LYCM_SALES_QTY");
					lycmSalesVal = rs.getDouble("LYCM_SALES_VAL");
					posCode = checkNull(rs.getString("POS_CODE"));
					empCode = checkNull(rs.getString("EMP_CODE"));
					System.out.println("prdCode ::: "+prdCode+">>custCode ::: "+custCode);
					System.out.println("itemSer ::: "+itemSer+">>itemCode ::: "+itemCode);
					System.out.println("stanCode ::: "+stanCode);
					System.out.println("netSalesQty ::: "+netSalesQty+">>netSalesVal ::: "+netSalesVal);
					System.out.println("lycmSalesQty ::: "+lycmSalesQty+">>lycmSalesVal ::: "+lycmSalesVal);
					System.out.println("posCode :::"+posCode+">>empCode :::"+empCode);
					
					unit="";terrCode="";terrDescr="";
					sql1="select UNIT from item where item_code=?";
					pstmt1 = conn.prepareStatement(sql1);
					pstmt1.setString(1, itemCode);
					rs1 = pstmt1.executeQuery();
					if(rs1.next())
					{
						unit = checkNull(rs1.getString("UNIT"));
					}
					System.out.println("unit>>"+unit);
					rs1.close();
					rs1 = null;
					pstmt1.close();
					pstmt1 = null;
					
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
							
					SalesConsolidationPrc scp =new SalesConsolidationPrc();
					tranId=scp.generateTranIDForSalesConsolidationProcess("sales_consolidate", loginSiteCode, itemSer, conn);
					sql1="insert into SALES_CONSOLIDATION(TRAN_ID,TRAN_DATE,CUST_CODE,PRD_CODE,TERR_CODE,TERR_DESCR,VERSION_ID,POS_CODE," +
							"EMP_CODE,SOURCE,ITEM_CODE,UNIT,ITEM_SER,ITEM_SER_NEW,STAN_CODE,STAN_CODE_NEW," +
							"NET_SALES_QTY,NET_SALES_VAL,LYCM_SALES_QTY,LYCM_SALES_VAL," +
							"ADD_DATE,ADD_USER,ADD_TERM,CHG_DATE,CHG_USER,CHG_TERM)" +
							"values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
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
					pstmt1.setString(10, "S");
					pstmt1.setString(11, itemCode);
					pstmt1.setString(12, unit);
					pstmt1.setString(13, itemSer);
					pstmt1.setString(14, itemSer);
					pstmt1.setString(15, stanCode);
					pstmt1.setString(16, stanCode);
					pstmt1.setDouble(17, netSalesQty);
					pstmt1.setDouble(18, netSalesVal);
					pstmt1.setDouble(19, lycmSalesQty);
					pstmt1.setDouble(20, lycmSalesVal);
					pstmt1.setDate(21, sysDate);
					pstmt1.setString(22, chgUser);
					pstmt1.setString(23, chgTerm);
					pstmt1.setDate(24, sysDate);
					pstmt1.setString(25, chgUser);
					pstmt1.setString(26, chgTerm);
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

	private String checkNull(String input)
	{
		return input == null ? "" : input.trim();
	}
}

