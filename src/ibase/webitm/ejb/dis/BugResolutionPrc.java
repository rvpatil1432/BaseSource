
package ibase.webitm.ejb.dis;
/*
 * Author:Wasim Ansari
 * Date:24-JUNE-16
 * Request W16CBAS005 (Bug Resolution Process) 
 */
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ProcessEJB;
import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.utility.TransIDGenerator;
import java.rmi.RemoteException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import org.w3c.dom.Document;

@javax.ejb.Stateless
public class BugResolutionPrc extends ProcessEJB implements BugResolutionPrcLocal, BugResolutionPrcRemote
{
	//Comment By Nasruddin 07-10-16 GenericUtility
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility = new E12GenericUtility();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	boolean isError = false;

	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		System.out.println("Entered in bug resolution process.");		
		String retStr = "";
		Document detailDom = null;
		Document headerDom = null;
		E12GenericUtility genericUtility = new E12GenericUtility();
		//Comment By Nasruddin 07-10-16 GenericUtility
		//GenericUtility genericUtility = GenericUtility.getInstance();			
		try
		{	
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				headerDom = genericUtility.parseString(xmlString); 
				System.out.println("xmlString--->>" + xmlString);
			}
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				detailDom = genericUtility.parseString(xmlString2); 
				System.out.println("xmlString2 --->>" + xmlString2);
			}
			
			retStr = process(headerDom, detailDom, windowName, xtraParams);

		}
		catch (Exception e)
		{
			e.printStackTrace();
			/*retStr = e.getMessage();*/ //Commented By Mukesh Chauhan on 02/08/19
			throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
		}
		return retStr;
	}
	
	public String process(Document dom, Document dom2, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		String retString = "";
		String loginEmpCode = "",userId = "",loginSite = "",chgTerm = "",bufFixesTranID = "",objName = "",status = "";
		String errString = "";
		String bugID = "",tranID = "";
		//ConnDriver connDriver = new ConnDriver();
		Connection conn = null;
		String sql = "";
		ResultSet rs = null,rsInsert = null;
		PreparedStatement pstmt = null;
		CallableStatement cstmt = null;
		
		int count = 0;
		
		try
		{
			System.out.println("Inside Try Block in PreSave");
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			
			java.sql.Timestamp currDate = getCurrtDate();
			System.out.println("Current date is"+currDate);
			
			SimpleDateFormat sdfAppl = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String currDateStr = sdfAppl.format(currDate);
			
			loginEmpCode = checkNullAndTrim(genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode"));
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			loginSite = checkNullAndTrim((genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode")));
			chgTerm = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "chgTerm" );
			
			bugID = checkNullAndTrim(( genericUtility.getColumnValue( "bug_id", dom )));
			System.out.println("Bug Id="+bugID);
			
			tranID = checkNullAndTrim(( genericUtility.getColumnValue( "tran_id", dom )));
			System.out.println("Tran Id="+tranID);
			
			if(!bugID.equalsIgnoreCase("") && !tranID.equalsIgnoreCase(""))
			{
				sql = " SELECT OBJ_NAME,STATUS FROM BUGLIST WHERE BUG_ID = ?";
				pstmt = conn.prepareStatement(sql);			
				pstmt.setString(1, bugID);			
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					objName = checkNullAndTrim(rs.getString("OBJ_NAME"));
					status = checkNullAndTrim(rs.getString("STATUS"));
					count++;
				}
				if ( rs != null )
				{
					rs.close();rs = null;
				}
				if( pstmt != null )
				{
					pstmt.close();pstmt = null;
				}
				
				if(count == 0)
				{
					System.out.println("Bug Id is not found");
					errString = itmDBAccessEJB.getErrorString("","VTINVBUGID","","",conn);
					return errString;
				}
				else if("D".equalsIgnoreCase(status))//if already deployed
				{
					System.out.println("Transaction is already deployed");
					errString = itmDBAccessEJB.getErrorString("","VTDEPLOYED","","",conn);
					return errString;
				}
				
				String SQL = "{call bugfixes_proc (?, ?, ?)}";
				cstmt = conn.prepareCall (SQL);
				cstmt.setString(1, bugID);
				cstmt.setString(2, tranID);
				//cstmt.registerOutParameter(3, OracleTypes.CURSOR);
				cstmt.registerOutParameter(3, java.sql.Types.VARCHAR);
				cstmt.executeUpdate();
				
				String resultStr = cstmt.getString(3);
				System.out.println("Result String recived=["+resultStr+"]");
				//rs = (ResultSet) cstmt.getObject(3);//For cusrosr
				//while (rs.next()) 
				//{
					//String bugIdRes = rs.getString("BUG_ID");
					//String objName = rs.getString("OBJ_NAME");
					//String refID = rs.getString("REF_ID");
					//String executeDate = rs.getString("EXECUTE_DATE");
					
					bufFixesTranID = generateTranTd("w_bugfixes",currDateStr,loginSite,conn);
					
					sql = " INSERT INTO BUG_FIXES (TRAN_ID,BUG_ID,OBJ_NAME,REF_ID,EXECUT_DATE,RESULT_STR)" 
                         +" VALUES (?,?,?,?,?,?)";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, bufFixesTranID);
					pstmt.setString(2, bugID);
					pstmt.setString(3, objName);
					pstmt.setString(4, tranID);
					pstmt.setTimestamp(5, currDate);
					pstmt.setString(6, resultStr);
					int cnt = pstmt.executeUpdate();
					System.out.println("Insert count="+cnt);
					if( pstmt != null )
					{
						pstmt.close();pstmt = null;
					}
					
					if("SUCCESS".equalsIgnoreCase(resultStr))
					{	
						sql = "UPDATE BUGLIST SET STATUS = 'D' WHERE BUG_ID = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, bugID);
						pstmt.executeUpdate();
						cnt = pstmt.executeUpdate();
						System.out.println("Update count="+cnt);
						
						if( pstmt != null )
						{
							pstmt.close();pstmt = null;
						}
					}	
				//}
				if( cstmt != null )
				{
					cstmt.close();cstmt = null;
				}
			}
			else
			{
				if(bugID.length() == 0)
				{	
					System.out.println("Bug Id is blank");
					errString = itmDBAccessEJB.getErrorString("","VTBUGID","","",conn);
					return errString;
				}
				else if (tranID.length() == 0)
				{
					System.out.println("Tran Id is blank");
					errString = itmDBAccessEJB.getErrorString("","VMTRANID","","",conn);//Tran Id should not be null
					return errString;
				}
			}
		}
		catch(Exception e)
		{
			isError = true;
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if ( rs != null )
				{
					rs.close();
					rs = null;
				}
				if( cstmt != null )
				{
					cstmt.close();
					cstmt = null;
				}
				if( pstmt != null )
				{
					pstmt.close();
					pstmt = null;
				}
				if(isError)
				{
					System.out.println("Rollbacking.................");
					retString = itmDBAccessEJB.getErrorString("","VTPRCERR","","",conn);//Process Not Completed Successfully
					conn.rollback();
				}
				else
				{
					System.out.println("commiting.................");
					conn.commit();
					retString = itmDBAccessEJB.getErrorString("","VTCOMPL","","",conn);//Process Completed Successfully
				}
				if( conn != null )
				{
					conn.close();
					conn = null;
				}
			}
			catch( Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return retString;
	}
	private static String checkNullAndTrim(String input)
	{
		if (input==null)
		{
			input="";
		}
		return input.trim();
	}
	
	private String generateTranTd(String windowName,String tranDate,String siteCode, Connection  conn )throws ITMException
	{		
		System.out.println("inside generateTranTd@@.........");
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		String tranId = "";
		String newKeystring = "",tranSer1 = "",keyString = "",keyCol = "";
		CommonConstants commonConstants = new CommonConstants();
		try
		{		 	
			sql = "SELECT KEY_STRING, TRAN_ID_COL, REF_SER FROM TRANSETUP WHERE TRAN_WINDOW = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, windowName);

			rs = pstmt.executeQuery();
			System.out.println("keyString :"+sql);

			if (rs.next())
			{
				keyString = rs.getString(1);
				keyCol = rs.getString(2);
				tranSer1 = rs.getString(3);
			}
			else
			{
				sql = "SELECT KEY_STRING, TRAN_ID_COL, REF_SER FROM TRANSETUP WHERE TRAN_WINDOW = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, "GENERAL");
				rs = pstmt.executeQuery();
				System.out.println("keyString :"+sql);				
				if (rs.next())
				{
					keyString = rs.getString(1);
					keyCol = rs.getString(2);
					tranSer1 = rs.getString(3);
				}
				// Changed by Sneha on 01-09-2016, for Closing the Open Cursor [Start]
				if( pstmt != null )
				{
					pstmt.close();
					pstmt = null;
				}
				if ( rs != null )
				{
					rs.close();
					rs = null;
				}
				// Changed by Sneha on 01-09-2016, for Closing the Open Cursor [End]
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;

			System.out.println("keyString :"+keyString);
			System.out.println("keyCol :"+keyCol);
			System.out.println("tranSer1 :"+tranSer1);
			String xmlValues = "";
			xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
			xmlValues = xmlValues + "<Header></Header>";
			xmlValues = xmlValues + "<Detail1>";
			xmlValues = xmlValues +	"<tran_id></tran_id>";
			xmlValues = xmlValues + "<site_code>" + siteCode + "</site_code>";
			xmlValues = xmlValues + "<tran_date>"+ tranDate + "</tran_date>";
			xmlValues = xmlValues + "<tran_type>"+"U"+"</tran_type>";
			xmlValues = xmlValues + "</Detail1></Root>";
			System.out.println("xmlValues  :["+xmlValues+"]");
			TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", commonConstants.DB_NAME);
			tranId = tg.generateTranSeqID(tranSer1, keyCol, keyString, conn);

			System.out.println("tranId ::"+tranId);
		}
		catch (SQLException ex)
		{			
			System.out.println("Exception ::" +sql+ ex.getMessage() + ":");
			ex.printStackTrace();
			throw new ITMException(ex);
		}
		catch (Exception e)
		{		
			System.out.println("Exception ::" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}

		return tranId;
	}
	
	private java.sql.Timestamp getCurrtDate() throws RemoteException,ITMException 
	{
		java.sql.Timestamp currDate = null;
		try 
		{
			Object date = null;
			currDate = new java.sql.Timestamp(System.currentTimeMillis());
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(genericUtility.getDBDateFormat());
			date = sdf.parse(currDate.toString());
			currDate = java.sql.Timestamp.valueOf(sdf.format(date).toString()+ " 00:00:00.0");

		} 
		catch (Exception e) 
		{
			throw new ITMException(e);
		}
		return (currDate);
	}

	@Override
	public String process() throws RemoteException, ITMException {
		// TODO Auto-generated method stub
		return null;
	}
}

