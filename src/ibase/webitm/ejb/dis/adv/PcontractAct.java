package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import java.sql.*;
import org.w3c.dom.*;
import javax.ejb.*;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.utility.E12GenericUtility;
import ibase.system.config.*;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class PcontractAct extends ActionHandlerEJB implements PcontractActLocal, PcontractActRemote
{
	/*public void ejbCreate() throws RemoteException, CreateException 
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
	}*/

    public String actionHandler() throws RemoteException,ITMException
	{
		return "";
	}

	public String actionHandler(String actionType, String xmlString, String objContext, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		String  resString = null;
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		try
		{
			if (actionType.equalsIgnoreCase("TermTable"))
			{
				System.out.println("xmlString :"+xmlString);
				dom = genericUtility.parseString(xmlString);
				resString = actionTermTable(dom, objContext, xtraParams);
			}
		}
	   	catch(Exception e)
		{
			System.out.println("Exception :PctrTermTable :actionHandler(String xmlString):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		System.out.println("returning from actionPickList actionHandler"+resString);
	    return (resString);
	}

	private String actionTermTable(Document dom, String objContext, String xtraParams) throws RemoteException , ITMException
	{
		String sql = "";
		int cnt = 0;
		String termTable = "";
		String table = "";
		ResultSet rs = null;
		Connection conn = null;
		Statement stmt = null;
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			stmt = conn.createStatement();
			termTable = genericUtility.getColumnValue("term_table",dom);	
			System.out.println("termTable :"+termTable);
			if (termTable == null)
			{
				termTable = "DEF_PTERM_PO";
			}
			if (termTable.equals("DEF_PTERM_PO"))  
			{
				sql="SELECT VAR_VALUE FROM DISPARM WHERE VAR_NAME ='"+termTable+"'";
				System.out.println("sql :"+sql);
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					table = rs.getString(1);
					System.out.println("table :"+table);
				}
				// Changed by Sneha on 12-09-2016, for Closing the Open Cursor [Start]
				if ( stmt != null )
				{
					stmt.close();
					stmt = null;
				}
				if ( rs != null )
				{
					rs.close();
					rs = null;
				}
				// Changed by Sneha on 12-09-2016, for Closing the Open Cursor [End]
			}
			else
			{
				table = termTable;
				System.out.println("table :"+table);
			}

			sql = "SELECT COUNT(*) FROM PUR_TERM_TABLE WHERE TERM_TABLE = '"+table+"'";
			System.out.println("sql :"+sql);
			rs = stmt.executeQuery(sql);
			if (rs.next())
			{
				cnt = rs.getInt(1);
				System.out.println("cnt :"+cnt);
			}
			sql = "SELECT TERM_CODE FROM PUR_TERM_TABLE WHERE TERM_TABLE ='"+table+"'";
			System.out.println("sql :"+sql);
			rs = stmt.executeQuery(sql);
			for (int i = 0; i < cnt; i++)
			{
				while (rs.next())
				{
					valueXmlString.append("<Detail>\r\n");
					valueXmlString.append("<term_code>").append("<![CDATA[").append(rs.getString(1).trim()).append("]]>").append("</term_code>\r\n");
					valueXmlString.append("</Detail>\r\n");
				}
				// Changed by Sarita on 15-11-2017, for Closing the Open Cursor [Start]
				if ( stmt != null )
				{
					stmt.close();
					stmt = null;
				}
				if ( rs != null )
				{
					rs.close();
					rs = null;
				}
				// Changed by Sarita on 15-11-2017, for Closing the Open Cursor [End]
			}
			//stmt.close();
			// Commented and Changed by Sarita on 15-11-2017, for Closing the Open Cursor [Start]
			if ( stmt != null )
			{
				stmt.close();
				stmt = null;
			}
			if ( rs != null )
			{
				rs.close();
				rs = null;
			}
			// Changed by Sarita on 15-11-2017, for Closing the Open Cursor [End]
			valueXmlString.append("</Root>\r\n");			
		}
		catch(SQLException e)
		{
			System.out.println("Exception : PctrTermTable : actionVoucher " +e.getMessage());
			throw new ITMException(e);
		}
		catch(Exception e)
		{
			System.out.println("Exception : PctrTermTable : actionHandler " +e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("Closing Connection....... ");
				conn.close();
				conn = null;
			}catch(Exception e){}
		}
		System.out.println("valueXmlString.toString() "+valueXmlString.toString());
		return valueXmlString.toString();		
	}
}