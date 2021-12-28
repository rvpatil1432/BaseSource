package ibase.webitm.ejb.dis.adv;

import ibase.webitm.utility.*;
import ibase.webitm.ejb.*;
import ibase.system.config.*;

import java.sql.*;
import java.rmi.*;
import java.util.*;
import org.nfunk.jep.JEP; // for ejb3 

import javax.ejb.*;
import javax.annotation.*;

import org.w3c.dom.*;
//import java
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class SuppEvalAct extends ActionHandlerEJB implements SuppEvalActLocal, SuppEvalActRemote 
{
	/*public void ejbCreate() throws RemoteException,CreateException
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

	public String actionHandler(String actionType, String xmlString, String xmlString1,String objContext, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null, dom1 = null;
		String retString = "";
		try
		{
			if (xmlString != null && xmlString.trim().length() > 0)
			{
				dom = new  ibase.utility.E12GenericUtility().parseString(xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom1 = new  ibase.utility.E12GenericUtility().parseString(xmlString1);
			}
			System.out.println("actionType ::"+actionType);
			if (actionType != null && actionType.equalsIgnoreCase("Default"))
			{
				retString = actionDefault(dom,dom1,objContext,xtraParams);
			}
		}
		catch (Exception e)
		{
			System.out.println("Exception ..."+e.getMessage());
			e.printStackTrace();
			throw new ITMException(e);
		}
		return 	retString;
	}

	private String actionDefault(Document dom,Document dom1,String objContext,String xtraParams) throws Exception
	{
		String tranId = "",tableNo = "",tranDate = "";
		Connection conn = null;
		Statement stmt = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		String calcMethod = "",apprSpec = "";
		String xmlString = "",chgUser = "",chgTerm = "";
		int upd = 0;
		StringBuffer returnXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		ConnDriver connDriver = new ConnDriver();
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			tranId = new  ibase.utility.E12GenericUtility().getColumnValue("tran_id",dom1);
			tableNo = new  ibase.utility.E12GenericUtility().getColumnValue("table_no",dom1);
			tranDate = new  ibase.utility.E12GenericUtility().getColumnValue("tran_date",dom1);
			sql = "SELECT CALC_METHOD, A.APPR_SPEC FROM APPRAISAL_SPEC A, APPRAISAL_SPEC_TABLE B "
					+" WHERE A.APPR_SPEC = B.APPR_SPEC AND TRIM(B.APPR_TBLNO) = TRIM('"+tableNo+"')";
			System.out.println("SQL ::"+sql);
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while (rs.next())
			{
				calcMethod = rs.getString("CALC_METHOD");
				apprSpec = rs.getString("APPR_SPEC");
				xmlString = supplyEval(dom1,tranId,tranDate,apprSpec,conn);
				returnXmlString.append(xmlString);
				xmlString = "";
			}
			stmt.close();
			chgUser = new  ibase.utility.E12GenericUtility().getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			chgTerm = new  ibase.utility.E12GenericUtility().getValueFromXTRA_PARAMS(xtraParams,"termId");

			sql = "UPDATE SUPP_EVAL SET CHG_USER = ? , CHG_DATE = ? , CHG_TERM = ? WHERE TRAN_ID = ?";
			System.out.println("SQL ::"+sql);
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,chgUser);
			pstmt.setTimestamp(2,new java.sql.Timestamp(System.currentTimeMillis()));
			pstmt.setString(3,chgTerm);
			pstmt.setString(4,tranId);
			upd = pstmt.executeUpdate();
			System.out.println("Updated "+upd);
			returnXmlString.append("</Root>");			
			System.out.println("Return Final String "+returnXmlString.toString());
		}
		catch (Exception e)
		{
			System.out.println("Exception..."+e);
			throw(e);
		}
		return returnXmlString.toString();
	}

	private String supplyEval(Document dom, String tranId, String tranDate, String apprSpec, Connection conn) throws Exception
	{
		Statement stmt = null;
		ResultSet rs = null;
		String sql = "";
		String calcMethod = "",methodType = "",methodInput = "";
		StringBuffer valueXmlString = new StringBuffer();
		int totRow = 0;
		double value = 0d,overallPerc = 0d,fixed = 0d;
		try
		{
			sql = "SELECT COUNT(*) AS COUNT FROM APPRAISAL_SPEC WHERE APPR_SPEC = '"+apprSpec+"'";
			System.out.println("SQL :: "+sql);
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			if (rs.next())
			{
				totRow = rs.getInt("COUNT");
			}
			stmt.close();
			if (totRow > 0)
			{
				sql = "SELECT CALC_METHOD,METHOD_TYPE,METHOD_INPUT, SPEC_DETAILS, OVERALL_PERC " 
						+"FROM APPRAISAL_SPEC WHERE APPR_SPEC =  '"+apprSpec+"'";
				System.out.println("SQL ::"+sql);
				stmt = conn.createStatement();
				rs = stmt.executeQuery(sql);
				while (rs.next())
				{
					calcMethod = rs.getString("CALC_METHOD");
					methodType = rs.getString("METHOD_TYPE");
					overallPerc = rs.getDouble("OVERALL_PERC");
					methodInput = rs.getString("METHOD_INPUT");
					System.out.println("METHOD_TYPE : "+methodType);
					switch(Integer.parseInt(methodType))
					{
						case 1://Direct
							break;
						case 2://Formula
							value = evaluateExpression(calcMethod);
							break;
						case 3://SQL
							value = getResultOfSql(dom,calcMethod,methodInput,conn);
							break;
						case 4://Fixed
							fixed = Double.parseDouble(calcMethod);
							value = fixed;
							break;
					}
					value = value / 100 * overallPerc;
					valueXmlString.append("<Detail>");
					//valueXmlString.append("<tran_id isSrvCallOnChg='0'>").append("<![CDATA[").append(tranId).append("]]>").append("</tran_id>");
					//valueXmlString.append("<line_no isSrvCallOnChg='0'>").append("<![CDATA[").append().append("]]>").append("</line_no>");
					valueXmlString.append("<rating isSrvCallOnChg='0'>").append("<![CDATA[").append(value).append("]]>").append("</rating>");
					valueXmlString.append("<appr_spec isSrvCallOnChg='1'>").append("<![CDATA[").append(apprSpec).append("]]>").append("</appr_spec>");
					valueXmlString.append("</Detail>");
				}				
			}
			System.out.println("Return String :: "+valueXmlString.toString());
		}
		catch (Exception e)
		{
			System.out.println("Exception "+e);
			throw(e);
		}
		return valueXmlString.toString();
	}

	private double evaluateExpression(String expr) throws Exception
	{
		double value = 0.0;
		org.nfunk.jep.JEP exprJEP = null;
		try
		{
			exprJEP = new org.nfunk.jep.JEP();
			exprJEP.addStandardFunctions();
			exprJEP.addStandardConstants();
			exprJEP.parseExpression(expr);
			value = exprJEP.getValue();
			System.out.println("Expression Result :: "+value);
			return value;
		}
		catch(Exception e)
		{
			throw(e);
		}
	}

	private double getResultOfSql(Document dom,String calcMethod,String methodInput, Connection conn) throws Exception
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ArrayList tokenList = new ArrayList();
		double retValue = 0.0;
		try
		{
			tokenList = new  ibase.utility.E12GenericUtility().getTokenList(methodInput,",");
			System.out.println("Calculated Method :: "+calcMethod);
			System.out.println("Input Columns :: "+tokenList);
			pstmt = conn.prepareStatement(calcMethod);
			for (int i = 0;i < tokenList.size() ;i++ )
			{
				System.out.println("Column : "+tokenList.get(i).toString()+" :: Value :"+new  ibase.utility.E12GenericUtility().getColumnValue(tokenList.get(i).toString(),dom));
				pstmt.setString(i+1,new  ibase.utility.E12GenericUtility().getColumnValue(tokenList.get(i).toString(),dom));
			}
			rs = pstmt.executeQuery();	
			if (rs.next())
			{
				retValue = rs.getDouble(1);
			}
		}
		catch (Exception e)
		{
			throw(e);
		}
		return retValue;
	}
}
