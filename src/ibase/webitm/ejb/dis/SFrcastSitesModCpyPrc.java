	
/********************************************************
	Title : SFrcastSitesModCpyPrcEJB
********************************************************/
package ibase.webitm.ejb.dis;
import ibase.webitm.utility.*;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.*;
import ibase.system.config.*;
import java.rmi.RemoteException;
import java.util.*;
import java.lang.*;
import java.sql.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.ejb.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class SFrcastSitesModCpyPrc extends ProcessEJB implements SFrcastSitesModCpyPrcLocal, SFrcastSitesModCpyPrcRemote
{	
	E12GenericUtility genericUtility= new  E12GenericUtility();
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
	public String process() throws RemoteException,ITMException
	{
		return "";
	}
	
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams)
		throws RemoteException,ITMException
	{
		Document detailDom = null;
		Document headerDom = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		String retStr = "";
		System.out.println("Process method called......");
		
		try
		{	
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				headerDom = genericUtility.parseString(xmlString); 
				System.out.println("headerDom" + headerDom);
			}
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				detailDom = genericUtility.parseString(xmlString2); 
				System.out.println("detailDom" + detailDom);
			}
			retStr = process(headerDom, detailDom, windowName, xtraParams);
		}
		catch (Exception e)
		{			
			System.out.println("Exception :SFrcastSitesModCpyPrcEJB :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return retStr;
	}//END OF PROCESS (1)

	public String process(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		
		Connection conn = null;
		ConnDriver connDriver = new ConnDriver();
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		PreparedStatement updtPstmt = null;
		ResultSet rs = null;
		ResultSet rs1 = null;
		String sql = "";
		String resultString = "";
		String errString = "";
		boolean isError = true;
		boolean updtFlag = false;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		String userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
		String option  = genericUtility.getColumnValue("option",headerDom);
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 			
			connDriver = null;
			conn.setAutoCommit(false);
			String tranId = "", itemCode = "";
			double  quantity = 0.0, updtQty = 0.0;
			int updtCnt = 0;
			String chgBy  = genericUtility.getColumnValue("chg_by",headerDom);
			String chgByDpdwn  = genericUtility.getColumnValue("chg_by_ddl",headerDom);
			String itemSeries  = genericUtility.getColumnValue("item_ser",headerDom);
			String prdFrom  = genericUtility.getColumnValue("prd_from",headerDom);
			String prdTo  = genericUtility.getColumnValue("prd_to",headerDom);
			String newPrdPln  = genericUtility.getColumnValue("new_period_plan",headerDom);
			String newPrdFor  = genericUtility.getColumnValue("new_prd_for",headerDom);
			String siteCdFr  = genericUtility.getColumnValue("site_code_from",headerDom);
			String siteCdTo  = genericUtility.getColumnValue("site_code_to",headerDom);
				
			if( option.equals("M") )
			{
				//String siteCode  = genericUtility.getColumnValue("site_code",headerDom);
				String itemCdFr  = genericUtility.getColumnValue("item_from",headerDom);
				String itemCdTo  = genericUtility.getColumnValue("item_to",headerDom);
				
				sql = "Select a.tran_id, b.item_code, b.quantity from salesforecast_hdr a, "
					 +" salesforecast_det b, item c where "
					 +" a.tran_id = b.tran_id "
					 +" and a.item_ser = ? "
					 +" and b.item_code = c.item_code "
					 +" and (b.item_code between ? and ?) "
					 +" and (a.site_code between ? and ? ) "
					 +" and b.prd_code__for = ? "
					 +" and a.prd_code__from = ? "
					 +" and a.prd_code__to = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString( 1, itemSeries.trim() );
				pstmt.setString( 2, itemCdFr );
				pstmt.setString( 3, itemCdTo );
				pstmt.setString( 4, siteCdFr );
				pstmt.setString( 5, siteCdTo );
				pstmt.setString( 6, prdFrom.trim() );
				pstmt.setString( 7, prdFrom.trim() );
				pstmt.setString( 8, newPrdPln.trim() );
				rs = pstmt.executeQuery();
				while( rs.next() )
				{
					tranId = rs.getString("tran_id");
					itemCode = rs.getString("item_code");
					quantity = rs.getDouble("quantity");
					
					System.out.println("quantity==>"+quantity);
					
					if( chgByDpdwn.equals("P") )
					{
						updtQty = quantity + ( (Double.parseDouble(chgBy.trim())* quantity ) / 100 );
					}
					if( chgByDpdwn.equals("R") )
					{
						updtQty = Double.parseDouble(chgBy.trim());
					}
					if( chgByDpdwn.equals("F") )
					{
						updtQty = quantity + Double.parseDouble(chgBy.trim());
					}
					
					System.out.println("updtQty==>"+updtQty);
					
					sql = "update salesforecast_det set quantity_org = ?, quantity = ? "
							 +" where tran_id = ? "
							 +" and item_code = ? "
							// +" and prd_code__plan = ?"
							 +" and prd_code__for = ? ";
					updtPstmt = conn.prepareStatement(sql);
					updtPstmt.setDouble( 1, quantity );
					updtPstmt.setDouble( 2, updtQty );
					updtPstmt.setString( 3, tranId.trim() );
					updtPstmt.setString( 4,  itemCode.trim() );
					updtPstmt.setString( 5, prdFrom.trim() );
					
					//updtPstmt.addBatch();					
					updtCnt = updtPstmt.executeUpdate();
				}	
				
				if( updtCnt > 0 )
				{
					errString = "PROCSUCC";
					isError = false;						
				}
				else
				{
					errString = "NORECINDTL";
				}	
			}
			if( option.equals("C") )
			{
				String siteCode = "";
				String unit = "";
				
				sql = "select site_code , tran_id from salesforecast_hdr"
				     +" where prd_code__from = ? and item_ser = ? order by site_code";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString( 1, prdFrom.trim() );
				pstmt.setString( 2, itemSeries.trim() );
				rs = pstmt.executeQuery();
				while( rs.next() )
				{
					siteCode = rs.getString("site_code");
					tranId = rs.getString("tran_id");
				
					if( tranId != null )
					{
						sql = "SELECT SALESFORECAST_DET.ITEM_CODE , SALESFORECAST_DET.UNIT , SALESFORECAST_DET.QUANTITY"
							 +" FROM SALESFORECAST_DET "
							 +" WHERE SALESFORECAST_DET.TRAN_ID = ? "
							 +" and prd_code__plan = ? and prd_code__for = ?";
						pstmt1 = conn.prepareStatement(sql);
						pstmt1.setString( 1, tranId.trim() );
						pstmt1.setString( 2, prdFrom.trim() );
						pstmt1.setString( 3, prdTo.trim() );
						//pstmt1.setString( 3, newPrdPln.trim() );
						rs1 = pstmt1.executeQuery();
						while( rs1.next() )
						{
							itemCode = rs1.getString("ITEM_CODE");
							unit = rs1.getString("UNIT");
							quantity = rs1.getDouble("QUANTITY");
							
							System.out.println("quantity==>"+quantity+"  ==>"+itemCode);
							updtQty = quantity + ( (Double.parseDouble(chgBy.trim()) * quantity) / 100 );
							System.out.println("updtQty==>"+updtQty);
							
							sql = "update salesforecast_det set quantity = ? "
								 +" where tran_id = ? "
								 +" and item_code = ? "
								 +" and prd_code__plan = ?"
								 +" and prd_code__for = ? ";
							updtPstmt = conn.prepareStatement(sql);
							updtPstmt.setDouble( 1, updtQty );
							updtPstmt.setString( 2, tranId.trim() );
							updtPstmt.setString( 3, itemCode.trim() );
							updtPstmt.setString( 4, newPrdPln.trim() );
							updtPstmt.setString( 5, newPrdFor.trim() );
							
							updtCnt = updtPstmt.executeUpdate();
							
							System.out.println("updtCnt is ==>"+updtCnt);
							updtPstmt.close();
							updtPstmt = null;
							if( updtCnt > 0 )
							{
								System.out.println("Inside if of updtCnt > 0");
								updtFlag = true;
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
				
				if( updtFlag )
				{
					System.out.println("Inside if of if( updtFlag )");
					errString = "PROCSUCC";
					isError = false;						
				}
				else
				{
					System.out.println("Inside else of if( updtFlag )");
					errString = "NORECFND";
				}
			}
		}
		catch(Exception e)
		{
			isError = true;
			e.printStackTrace();
			errString = e.getMessage();
			throw new ITMException(e);
		}		
		finally
		{
			try
			{
				if( isError )
				{
					System.out.println("Inside if of if( isError )");
					conn.rollback();
					System.out.println("connection rollback.............");
				}
				else
				{
					System.out.println("Inside else of if( isError )");
					conn.commit();
					System.out.println("Commiting connection.............");
				}
				if( conn != null )
				{
					conn.close();
					conn = null;
				}
				if( updtPstmt != null )
				{
					updtPstmt.close();
					updtPstmt = null;
				}
				if( pstmt1 != null )
				{
					pstmt1.close();
					pstmt1 = null;
				}
				if( pstmt != null )
				{
					pstmt.close();
					pstmt = null;
				}	
				resultString = itmDBAccessEJB.getErrorString("",errString,userId,"",conn);
			}
			catch(Exception sqle)
			{
				sqle.printStackTrace();
			//	throw new  Exception(sqle);
			}
		}	
		return resultString;
	} //end process
}//end class
