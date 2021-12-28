package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;

import javax.ejb.*;
import java.text.SimpleDateFormat;

import ibase.webitm.utility.ITMException;
//import ibase.webitm.utility.GenericUtility;
import ibase.system.config.*;
import ibase.webitm.utility.*;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;

import javax.ejb.Stateless; // added for ejb3


//public class DistStkUpdEJB implements SessionBean
@Stateless // added for ejb3
public class DistStkUpd implements DistStkUpdLocal, DistStkUpdRemote
{
	/*SessionContext cSessionContext;
	public void ejbCreate() throws RemoteException, CreateException 
	{
	}

	public void ejbRemove()
	{
	}

	public void ejbActivate() 
	{
	}

	public void ejbPassivate() 
	{
	}

	public void setSessionContext(SessionContext mSessionContext)
	{
		this.cSessionContext = mSessionContext;
	}*/
	/* Remark: The following function calls updateAllocTrace() with connection parameter*/
    public int updAllocTrace(HashMap hmp) throws RemoteException,ITMException
	{
		int retVal = 0;
		Connection conn = null;
		ConnDriver connDriver = new ConnDriver();
		try
		{
			conn = connDriver.getConnectDB("DriverITM");
			//conn = getConnection();
			retVal = updAllocTrace(hmp, conn);
		}
		catch(Exception e)
		{
			System.out.println("The SQLException occurs in DistStkUpdEJB :"+e);			
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("Closing Connection.....");
				if(conn != null)
				{
					conn.close();
					conn = null;
				}
			}catch(Exception se){}
		}
		return retVal;
	}
	/* Remark: This follwing function calls from Pre & Post Save with the Connection Parameter*/
    public int updAllocTrace(HashMap hmp, Connection conn) throws RemoteException,ITMException
	{
		String stkOpt = "", tranID = "", keyString = "", sql = "";
		String refId = "", refLine = "", refSer = "", itemCode = "", siteCode = "", locCode = "", lotNo = "",	lotSl = "";
		String chgWin="", chgUser = "", chgTerm = "", errString;
		String sqlUpd = "";
		PreparedStatement pstmt = null;
		Statement stmt = null, stmt1 = null;
		ResultSet rs = null;
		HashMap keyGenerator = null;
		double allocQty = 0d;
		int updateCnt = 0, retVal = 0;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility= new  E12GenericUtility();
		TransIDGenerator transIDGenerator = null;
		try
		{
			// 01/11/13 manoharan commented the local code and  called the new component
                    InvAllocTraceBean invBean = new InvAllocTraceBean(); 
                    errString = invBean.updateInvallocTrace(hmp,conn);
                    if(errString != null && errString.trim().length() > 0)
					{
						retVal = 0;
					}
					else
					{
						retVal = 1;
					}
                    
			/*stmt = conn.createStatement();
			sql = "SELECT STK_OPT FROM ITEM WHERE ITEM_CODE = '" +hmp.get("item_code").toString()+"'";
			System.out.println("sql :"+sql);
			rs = stmt.executeQuery(sql);
			if (rs.next())
			{
				stkOpt = rs.getString(1);
				if (stkOpt.equals("0"))
				{
					retVal = 0;
					return retVal;
				}
			}
			sql="SELECT KEY_STRING FROM TRANSETUP WHERE UPPER(TRAN_WINDOW) = 'T_ALLOCTRACE'";
			System.out.println("UpdAllocTrace: sql:"+sql);
			rs = stmt.executeQuery(sql);
			if (rs.next())
			{
				keyString = rs.getString(1);
			}
			else
			{
				sql = "SELECT KEY_STRING FROM TRANSETUP WHERE UPPER(TRAN_WINDOW) = 'GENERAL' ";
				System.out.println("sql :"+sql);
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					keyString = rs.getString(1);
					System.out.println("keyString :"+keyString);
				}
			}

			java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
			String tranDate1 = "";
			siteCode = hmp.get("site_code").toString();
			SimpleDateFormat sdf = new SimpleDateFormat (genericUtility.getApplDateFormat());
			tranDate1 = sdf.format(hmp.get("tran_date"));
			String transIDXmlString = "<?xml version=\"1.0\"?>\r\n<Root>\r\n" +
									  "<header>\r\n</header>" +
									  "<Detail1>" +
									  "<tran_id></tran_id>" +
									  "<tran_date>" +tranDate1 + "</tran_date>" +
									  "<site_code>" + siteCode + "</site_code>" +
									  "</Detail1></Root>";
			
			System.out.println("transIDXmlString : " + transIDXmlString);
			//GETTING DATABASE NAME
			CommonConstants.setIBASEHOME();			
			TransIDGenerator tg = new TransIDGenerator(transIDXmlString, "BASE", CommonConstants.DB_NAME);
			String tranId = tg.generateTranSeqID("ATRACE","tran_id", keyString, conn);
			System.out.println("tranId :"+tranId);
			sql= "INSERT INTO INVALLOC_TRACE (TRAN_ID, TRAN_DATE, REF_SER, REF_ID, REF_LINE, " 
				+"ITEM_CODE, SITE_CODE, LOC_CODE, LOT_NO, LOT_SL, ALLOC_QTY, CHG_WIN, CHG_USER, "
				+"CHG_TERM, CHG_DATE)VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			System.out.println("sql :"+sql);
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,tranId);
			pstmt.setDate(2,java.sql.Date.valueOf(new SimpleDateFormat(genericUtility.getDBDateFormat()).format(hmp.get("tran_date"))));
			pstmt.setString(3,hmp.get("ref_ser").toString());
			pstmt.setString(4,hmp.get("ref_id").toString());
			pstmt.setString(5,hmp.get("ref_line").toString());
			pstmt.setString(6,hmp.get("item_code").toString());
			pstmt.setString(7,hmp.get("site_code").toString());
			pstmt.setString(8,hmp.get("loc_code").toString());
			System.out.println("loc_code: '" + hmp.get("loc_code").toString() + "'");
			pstmt.setString(9,hmp.get("lot_no").toString());
			pstmt.setString(10,hmp.get("lot_sl").toString());
			pstmt.setDouble(11,((Double)hmp.get("alloc_qty")).doubleValue());
			pstmt.setString(12,hmp.get("chg_win").toString());
			pstmt.setString(13,hmp.get("chg_user").toString());
			pstmt.setString(14,hmp.get("chg_term").toString());
			pstmt.setDate(15,today);
			updateCnt = pstmt.executeUpdate();
			System.out.println("insertCnt :"+updateCnt);
			if (updateCnt <= 0)
			{
				retVal = 0;
			}
			else
			{
				retVal = 1;
			}
			//Added By Gulzar 18/01/07 - 
			System.out.println("AllocQty :"+hmp.get("alloc_qty")+"\n item_Code :"+hmp.get("item_code")+"\n sitecode :"+hmp.get("site_code")+"\nlocCode :"+hmp.get("loc_code"));
			System.out.println("lotno :"+hmp.get("lot_no")+"\n lotSl :"+hmp.get("lot_sl"));

			sqlUpd =  "UPDATE STOCK SET ALLOC_QTY = (CASE WHEN ALLOC_QTY IS NULL THEN 0 ELSE ALLOC_QTY END) + "+((Double)hmp.get("alloc_qty")).doubleValue()+" " +
					 " WHERE ITEM_CODE = '"+hmp.get("item_code").toString()+"'" +
					 " AND SITE_CODE = '"+hmp.get("site_code").toString()+"' " +
					 " AND LOC_CODE = '"+hmp.get("loc_code").toString()+"' " +
					 " AND LOT_NO = '"+hmp.get("lot_no").toString()+"' " +
					 " AND LOT_SL = '"+hmp.get("lot_sl").toString()+"' ";
			System.out.println("Update sql :"+sqlUpd);
			stmt1 = conn.createStatement();
			updateCnt = stmt1.executeUpdate(sqlUpd);
			//End Add Gulzar 18/01/07
			System.out.println("updateCnt  :"+updateCnt);
			if (updateCnt <= 0)
			{
				retVal = 0;
			}
			else
			{
				retVal = 1;
			}
			*/
		}
		catch (SQLException sqx)
		{
			System.out.println("The SQLException occurs in DistStkUpdEJB :"+sqx);
			sqx.printStackTrace();
			throw new ITMException(sqx);
		}
		catch(Exception e)
		{
			System.out.println("The SQLException occurs in DistStkUpdEJB :"+e);			
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("Closing Statements.....");
				if(stmt != null)
				{
					stmt.close();
					stmt = null;
				}
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if (stmt1 != null)
				{
					stmt1.close();
					stmt1 = null;
				}
			}catch(Exception se){}
		}
		System.out.println("retVal from DistStkUpdEJB :"+retVal);
		return retVal;
	}
}