package ibase.webitm.ejb.dis;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.ejb.Stateless;

import org.w3c.dom.Document;


import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
//import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.utility.ITMException;
@Stateless


public class PoamdDlvDefault extends ActionHandlerEJB implements PoamdDlvDefaultLocal, PoamdDlvDefaultRemote 

{
	ibase.utility.E12GenericUtility genericUtility= new ibase.utility.E12GenericUtility();
	ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();	
	public String actionHandler() throws RemoteException,ITMException
	{
		return "";
	}
	public String actionHandler(String actionType, String xmlString, String objContext, String xtraParams) throws RemoteException,ITMException
	{
		String retString = "";
		Document dom = null;

		try
		{
			System.out.println("Call method =Action handler");
			System.out.println("action Type====="+actionType);
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				System.out.println("XML String :"+xmlString);
				dom = new  ibase.utility.E12GenericUtility().parseString(xmlString); 
				System.out.println("dom :"+dom);
			}
			if (actionType.equalsIgnoreCase("Dlv Term"))
			{
				retString = actionDlvTerm(dom,objContext,xtraParams);
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception actionHandler(String xmlString):" + e.getMessage() + ":");
			throw new ITMException(e);
		}
		return retString;
	}
	private String actionDlvTerm(Document dom, String objContext, String xtraParams) throws RemoteException , ITMException
	{
		String sql = "", sql1="";
		String dlvterm="";
		ResultSet rs = null,rs1=null;
		Connection conn = null;
		Statement stmt = null;
		PreparedStatement pstmt = null;
		int cnt = 0;
		StringBuffer valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		ConnDriver connDriver = new ConnDriver();
		try
		{
			    //Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			    dlvterm = genericUtility.getColumnValue("dlv_term",dom);	
				System.out.println("dlv_term :"+dlvterm);
				String errCode="";
				if(dlvterm != null && dlvterm.trim().length() > 0)
				{
					System.out.println("errrrrrrrrrrrrrrrrrrrrrrrr");
				sql1="select count(*) from DLTERMFC where dlv_term=?";
				pstmt = conn.prepareStatement(sql1);
				pstmt.setString(1, dlvterm);
				rs1 = pstmt.executeQuery();
				if (rs1.next())
				{
					cnt = rs1.getInt(1);
				}
				rs1.close();
				rs1 = null;
				pstmt.close();
				pstmt = null;
				if (cnt== 0)
				{
					System.out.println("insert into eerrorr");
					errCode = "VTNORECDLV";
					errCode = itmDBAccess.getErrorString("", errCode, "","",conn);
					return errCode;
				}
				
                sql =" select DLTERMFC.line_no ,DLTERMFC.min_day,DLTERMFC.max_day,DLTERMFC.min_cramt AS min_amt ,DLTERMFC.max_cramt AS max_amt,DLTERMFC.fin_chg,DLTERMFC.fchg_type from DLTERMFC where DLTERMFC.dlv_term='"+ dlvterm +"'";
				System.out.println("Delivery Term SQL :="+sql);
				/*pord_dlv_term.purc_order,pord_dlv_term.line_no,pord_dlv_term.min_day,pord_dlv_term.max_day,***DLTERMFC.line_no=pord_dlv_term.line_no and ****pord_dlv_term.min_amt,pord_dlv_term.max_amt,pord_dlv_term.fin_chg,pord_dlv_term.fchg_type,pord_dlv_term.ref_code*/
				stmt = conn.createStatement();
				rs = stmt.executeQuery(sql);
				while (rs.next())
				{
					valueXmlString.append("<Detail>\r\n");
					valueXmlString.append("<dlv_term>").append("<![CDATA[").append(dlvterm).append("]]>").append("</dlv_term>\r\n");
					valueXmlString.append("<line_no>").append("<![CDATA[").append(rs.getString(1).trim()).append("]]>").append("</line_no>\r\n");
					valueXmlString.append("<min_day>").append("<![CDATA[").append(rs.getString(2).trim()).append("]]>").append("</min_day>\r\n");
					valueXmlString.append("<max_day>").append("<![CDATA[").append(rs.getString(3).trim()).append("]]>").append("</max_day>\r\n");
					valueXmlString.append("<min_amt>").append("<![CDATA[").append(rs.getString(4).trim()).append("]]>").append("</min_amt>\r\n");
					valueXmlString.append("<max_amt>").append("<![CDATA[").append(rs.getString(5).trim()).append("]]>").append("</max_amt>\r\n");
					valueXmlString.append("<fin_chg>").append("<![CDATA[").append(rs.getString(6).trim()).append("]]>").append("</fin_chg>\r\n");
					valueXmlString.append("<fchg_type>").append("<![CDATA[").append(rs.getString(7).trim()).append("]]>").append("</fchg_type>\r\n");
					valueXmlString.append("</Detail>\r\n");
				}
				stmt.close();
				stmt=null;
				valueXmlString.append("</Root>\r\n");
				
				}
				
		}
		
		catch(SQLException e)
		{
			System.out.println("Exception : Delivery Detail...." +e.getMessage());
			throw new ITMException(e);
		}
		catch(Exception e)
		{
			System.out.println("Exception" +e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				conn.close();
				conn = null;
			}
			catch(Exception e){}
		}
		System.out.println("valueXmlString.toString() "+valueXmlString.toString());
		return valueXmlString.toString();		
	}


}
