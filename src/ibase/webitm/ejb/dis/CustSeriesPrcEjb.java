package ibase.webitm.ejb.dis;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ProcessEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.ejb.Stateless;

import org.w3c.dom.Document;

@Stateless // added for ejb3 


public class CustSeriesPrcEjb extends ProcessEJB  implements CustSeriesPrcEjbLocal ,CustSeriesPrcEjbRemote
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();

	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		String retStr = "";
		Document detailDom = null;
		Document headerDom = null;

		//GenericUtility genericUtility = GenericUtility.getInstance();

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
			System.out.println("Exception :ConsolidationPrcEJB :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			e.printStackTrace();
			System.out.println("exception :"+e.getMessage());
			/*retStr = e.getMessage();*/ // Commented By Mukesh Chauhan on 02/08/19
			throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
		}
		return retStr;

	}
	//END
	public String process(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{

		//variable declaration 
		String sql = "";
		String sqlquery = "";
		String errString = "";
		String custCode = "";
		String crTerm = "";
		String crLimit = "";
		String nwProdCrTerm = "";
		String businessLimit = "";
		String discPerc = "";
		String commPerc = "";
		String commPerc1 = "";
		String commPerc2 = "";
		String creditPrd = "";
		String ignoreDays = "";
		String ignoreCredit = "";
		boolean sqlstr = false ;
		//end of variable declaration
		Connection conn = null;
		PreparedStatement pstmt = null, pstmt1 = null;
		ResultSet rs = null;

		int count = 0;
		boolean isError = false;
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();

		try
		{
			System.out.println("*********inside try block*******");
			ConnDriver connDriver = new ConnDriver();
			StringBuffer strqry = new StringBuffer(); 
			//get connection
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
			//get data from header DOM

			custCode = genericUtility.getColumnValue("cust_code", headerDom)==null?"null":genericUtility.getColumnValue("cust_code", headerDom);
			System.out.println("custCode--------"+custCode);
			crTerm = genericUtility.getColumnValue("cr_term", headerDom);
			if(crTerm!=null && crTerm.length()!=0 )
			{
				strqry.append("cr_term = '"+crTerm+"',");
			}
			System.out.println("crTerm--------"+crTerm);
			crLimit = genericUtility.getColumnValue("credit_lmt", headerDom);
			if(crLimit!=null && crLimit.length()!=0 )
			{
				strqry.append("credit_lmt = '"+crLimit+"',");
			}
			System.out.println("crLimit--------"+crLimit);
			nwProdCrTerm = genericUtility.getColumnValue("cr_term__np", headerDom);
			if(	nwProdCrTerm!=null && 	nwProdCrTerm.length()!=0 )
			{
				strqry.append("cr_term__np = '"+nwProdCrTerm+"',");
			}
			System.out.println("nwProdCrTerm--------"+nwProdCrTerm);
			businessLimit = genericUtility.getColumnValue("busi_limit", headerDom);
			if(	businessLimit!=null && 	businessLimit.length()!=0 )
			{
				strqry.append("busi_limit = '"+businessLimit+"',");
			}
			System.out.println("businessLimit--------"+businessLimit);
			discPerc = genericUtility.getColumnValue("disc_perc", headerDom);
			if(	discPerc!=null && discPerc.length()!=0 )
			{
				strqry.append("disc_perc = '"+discPerc+"',");
			}
			System.out.println("discPerc--------"+discPerc);
			commPerc = genericUtility.getColumnValue("comm_perc", headerDom);
			if(	commPerc!=null && commPerc.length()!=0 )
			{
				strqry.append("comm_perc = '"+commPerc+"',");
			}
			System.out.println("commPerc--------"+commPerc);
			commPerc1 = genericUtility.getColumnValue("comm_perc__1", headerDom);
			if(	commPerc1!=null && commPerc1.length()!=0 )
			{
				strqry.append("comm_perc__1 = '"+commPerc1+"',");
			}
			System.out.println("commPerc1--------"+commPerc1);
			commPerc2 = genericUtility.getColumnValue("comm_perc__2", headerDom);//
			if(	commPerc2!=null && commPerc2.length()!=0 )
			{
				strqry.append("comm_perc__2 = '"+commPerc2+"',");
			}
			System.out.println("commPerc2--------"+commPerc2);
			ignoreDays = genericUtility.getColumnValue("ignore_days", headerDom) ;
			if(	ignoreDays!=null && ignoreDays.length()!=0 )
			{
				strqry.append("ignore_days = '"+ignoreDays+"',");
			}
			System.out.println("ignoreDays--------"+ignoreDays);
			ignoreCredit = genericUtility.getColumnValue("ignore_credit", headerDom);
			if(	ignoreCredit!=null && ignoreCredit.length()!=0 )
			{
				strqry.append("ignore_credit = '"+ignoreCredit+"',");
			}
			System.out.println("ignoreCredit--------"+ignoreCredit);
			creditPrd = genericUtility.getColumnValue("credit_prd", headerDom);
			if(	creditPrd!=null && creditPrd.length()!=0 )
			{
				strqry.append("credit_prd = '"+creditPrd+"',");
			}
			System.out.println("credit period-------"+creditPrd);
			//IF CUSTOMER CODE IS NOT NULL THEN IT SHOULD BE EXIST IN CUSTOMER TABLE  
			if(custCode!=null)
			{
				sql = "SELECT COUNT(*) FROM CUSTOMER WHERE CUST_CODE = ?";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, custCode );
				rs = pstmt1.executeQuery();
				while( rs.next() )
				{
					System.out.println("count1----"+rs.getInt(1));
					count = rs.getInt(1);
				}
				System.out.println("count----"+count);
				rs.close();
				rs = null;
				pstmt1.close();
				pstmt1 = null;
				if(count == 0)
				{
					System.out.println("customer code in master ");
					errString = itmDBAccessEJB.getErrorString("","VMCUST1","","",conn);
					return errString;
				}
				else
				{
					sql = "SELECT COUNT(*)  FROM CUSTOMER_SERIES WHERE CUST_CODE = ?";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, custCode );
					rs = pstmt1.executeQuery();
					while( rs.next() )
					{
						count = rs.getInt(1);
					}
					rs.close();
					rs = null;
					pstmt1.close();
					pstmt1 = null;
					if(count == 0)
					{
						System.out.println("customer code in Customer Series master ");
						errString = itmDBAccessEJB.getErrorString("","VTINVCUSCO","","",conn);
						return errString;
					}
				}
			}
			if(crTerm!=null && crTerm.trim().length()!=0 )
			{
				sql = "SELECT COUNT(*) FROM CRTERM WHERE CR_TERM = ?";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, crTerm );
				rs = pstmt1.executeQuery();
				while( rs.next() )
				{
					count = rs.getInt(1);
				}
				rs.close();
				rs = null;
				pstmt1.close();
				pstmt1 = null;
				if(count == 0)
				{
					System.out.println("CREDIT TERM IN MASTER ");
					errString = itmDBAccessEJB.getErrorString("","VMCRTER1","","",conn);
					return errString;
				}


			}
			if(nwProdCrTerm!=null && nwProdCrTerm.trim().length()!=0 )
			{
				sql = "SELECT COUNT(*)  FROM CRTERM WHERE CR_TERM = ?";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, nwProdCrTerm );
				rs = pstmt1.executeQuery();
				while( rs.next() )
				{
					count = rs.getInt(1);
				}
				rs.close();
				rs = null;
				pstmt1.close();
				pstmt1 = null;
				if(count == 0)
				{
					System.out.println("CREDIT TERM IN MASTER ");
					errString = itmDBAccessEJB.getErrorString("","VMCRTER1","","",conn);
					return errString;
				}

			}
			//if string buffer is not null then convert sql string to to string 
			if(strqry.length()!=0)
			{
				sqlquery= strqry.toString();
				System.out.println("SQL QUERY FOR BUFER ---["+sqlquery+"]");
				sql = sqlquery.substring(0,sqlquery.lastIndexOf(','));
				System.out.println("SQL FOR INDEX------["+sql+"]");
				sqlstr = true;
			}
			if(strqry.length()==0)
			{
				System.out.println("no updation for customer series table ");
				errString = itmDBAccessEJB.getErrorString("","VTPRCOMPIU","","",conn);
				sqlstr = false;
			}
			if(sqlstr)
			{
				sql="update customer_series set "+sql+
						" where cust_code =?"; 
				System.out.println("SQL FOR UPDATION ["+sql+"]");
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, custCode);
				count = pstmt.executeUpdate();
				pstmt.close();
				if(count > 0)
				{
					System.out.println("update row "+count);
					errString = itmDBAccessEJB.getErrorString("","VTPRCOMPLT","","",conn);
					//return errString;
				}
				System.out.println("SQL STATEMENT "+sql);
			}
			//update customer _series table against customer code (cust_code)


			//			sql="update customer_series set cr_term =  ( case when '"+crTerm+"' = 'X' then cr_term else '"+crTerm+"' end)" +
			//			", credit_lMT = ( case when '"+crLimit+"' = 'X' then credit_lmt else "+crLimit+" end)" +
			//			",cr_term__np = ( case when '"+nwProdCrTerm+"' = 'X' then cr_term__np else '"+nwProdCrTerm+"' end)"+
			//			",busi_limit = ( case when '"+businessLimit+"' = 'X' then busi_limit else "+businessLimit+" end)"+
			//			",disc_perc = ( case when '"+discPerc+"' = 'X' then disc_perc else "+discPerc+" end) "+
			//			",comm_perc = ( case when '"+commPerc+"' = 'X' then comm_perc else "+commPerc+" end) "+
			//			",comm_perc__1 = ( case when '"+commPerc1+"' = 'X' then comm_perc__1 else "+commPerc1+" end) "+
			//			",comm_perc__2 = ( case when '"+commPerc2+"' = 'X' then comm_perc__2 else "+commPerc2+" end) "+
			//			",ignore_days = ( case when '"+ignoreDays+"' = 'X' then ignore_days else "+ignoreDays+" end) "+
			//			",ignore_credit = ( case when '"+ignoreCredit+"' = 'X' then ignore_credit else "+ignoreCredit+" end)"+
			//			",credit_prd = ( case when '"+creditPrd+"' = 'X' then credit_prd else "+creditPrd+" end)"+
			//			" where cust_code =?";


		}		
		catch(Exception se)
		{
			isError = true;
			System.out.println("SQLException :" + se);
			se.printStackTrace();
			System.out.println("error ****"+se.getMessage());
			errString = se.getMessage();
			try
			{
				conn.rollback();
			}
			catch(SQLException e1)
			{
				se = e1;
			}
			return errString;
		}

		finally
		{
			try
			{
				if( !isError  )
				{
					conn.commit(); 
				}

				if(conn != null)
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
					conn.close();
					conn = null;
				}
			}
			catch(Exception e)
			{
				errString = e.getMessage();
				e.printStackTrace();
				System.out.println("error =>"+e.getMessage());
				return errString ;
			}
		}
		return errString;
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub

	}

}

