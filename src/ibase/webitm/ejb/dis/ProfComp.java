package ibase.webitm.ejb.dis;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
//import ibase.webitm.utility.GenericUtility;
import ibase.ejb.*;
import ibase.system.config.*;
import ibase.utility.BaseLogger;
import ibase.utility.E12GenericUtility;

import java.util.*;
import java.util.Date;
import java.sql.*;
import java.text.*;
import java.io.*;

import java.rmi.RemoteException;
import javax.ejb.*;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.ejb.Stateless; // added for ejb3
//public class ProfCompEJB extends ValidatorEJB implements SessionBean
@Stateless // added for ejb3
public class ProfComp extends ValidatorEJB implements ProfCompLocal, ProfCompRemote
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
 /*
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
	}  */
	
    public String wfValData() throws RemoteException,ITMException
	{
		return "";
	}    

	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		BaseLogger.info("\n\n IN WF_VALDATA OF PROFWSHEET SCREEN \n\n" );
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;		
		String errString = "";
		BaseLogger.debug("xmlString : "  + xmlString);
		BaseLogger.debug("xmlString1 : "  + xmlString1);
		BaseLogger.debug("xmlString2 : "  + xmlString2);
		//GenericUtility genericUtility = GenericUtility.getInstance();
		try
		{
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				dom = genericUtility.parseString(xmlString); 
			}
			if(xmlString1 != null && xmlString1.trim().length()!=0)
			{
				dom1 = genericUtility.parseString(xmlString1); 
			}
			errString = wfValData(dom,dom1,objContext,editFlag,xtraParams);			
		}
		catch(Exception e)
		{
			BaseLogger.error("Exception :[ProfMstdetEJB] :[wfValData]:[" + e + "]");
			errString = genericUtility.createErrorString(e);
			throw new ITMException(e);
		}
		BaseLogger.debug("Return String :" + errString);
		return (errString);
	}

	public String wfValData(Document dom, Document dom1,  String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String columnName = null;
		String columnValue = null;
		String columnValue1 = null;
		String columnValue2 = null;

		String userId = null;
		String sql = null;
		String errString = "";

		Connection connectionObject = null;
		Statement stmt = null;
		ResultSet rs = null;
		//ITMDBAccessHome itmDBAccessHome = null; //for ejb3
		ITMDBAccessEJB itmDBAccess = null;

		NodeList parentList = null;
		NodeList childList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		int ctr;
		int noOfChilds = -1;

		int currentFormNo =-1;
		//GenericUtility genericUtility = GenericUtility.getInstance();

		try
		{
			itmDBAccess = new ITMDBAccessEJB();
			//itmDBAccess = itmDBAccessHome.create();
			connectionObject = itmDBAccess.getConnection();			
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}        
			parentList = dom.getElementsByTagName("Detail" + currentFormNo);
     		parentNode = parentList.item(0);
			childList = parentNode.getChildNodes();
			noOfChilds = childList.getLength();
			for(ctr = 0; ctr < noOfChilds; ctr++)
			{
				childNode = childList.item(ctr);
				childNodeName = childNode.getNodeName();
				switch(currentFormNo)
				{
					case 2:
						if(childNodeName.equals("acct_code"))
					    {
							if (childNode.getFirstChild() == null || childNode.getFirstChild().getNodeValue() ==null)
							{
								errString = itmDBAccess.getErrorString("acct_code","DSACCTCD",userId,errString,connectionObject); 
							}
							else
							{
								columnValue = childNode.getFirstChild().getNodeValue().trim();
								sql ="SELECT ACCT_CODE,DESCR FROM PROF_ACCOUNTS WHERE ACCT_CODE='"+ columnValue+"'";
								BaseLogger.debug("Checking wsheet code in prof_wsheet table\n Query  :"+sql);
								stmt = connectionObject.createStatement();
								rs = stmt.executeQuery(sql);
								if(!rs.next())
								{
									errString = itmDBAccess.getErrorString("acct_code","DSACCTCD",userId,errString,connectionObject); 
									break;
								}
								stmt.close();
								stmt=null;
								columnValue1 = genericUtility.getColumnValue("wsheet_code",dom);
								columnValue2 = genericUtility.getColumnValue("line_no",dom);
								sql ="SELECT WSHEET_CODE,ACCT_CODE FROM PROF_WSHEET_COMP WHERE WSHEET_CODE='"+ columnValue1+ "' AND ACCT_CODE='"+ columnValue + "' AND LINE_NO!="+columnValue2;
								BaseLogger.debug("Checking wsheet code in prof_wsheet table\n Query  :\n"+sql);
								stmt = connectionObject.createStatement();
								rs = stmt.executeQuery(sql);
								if(rs.next())
								{
									errString = itmDBAccess.getErrorString("wsheet_code","DSACCTCD",userId,errString,connectionObject); 
									break;
								}
								stmt.close();
								stmt=null;
						     }
						}	
					    else if (childNodeName.equals("seq_no"))
						{
							if (childNode.getFirstChild() == null)
							{
								errString = itmDBAccess.getErrorString("seq_no","VMSEQUNO",userId,errString,connectionObject); 
							}
						}
					     else if (childNodeName.equals("active_yn"))
						 {
							if (childNode.getFirstChild() == null)
							 {
								errString = itmDBAccess.getErrorString("active_yn","VMACTIVEYN",userId,errString,connectionObject); 
							 }
						 }
					    else if (childNodeName.equals("pick_source"))
						{
							if (childNode.getFirstChild() == null)
							{
								errString = itmDBAccess.getErrorString("pick_source","VMPICKSRC",userId,errString,connectionObject); 
							}
						}
						break;
				}
				if (errString != null && errString.trim().length()>0)
				{
					break;
				}				
			}
		}
		catch(Exception e)
		{
			BaseLogger.error("Exception :[ProfCompEJB] :[wfValData]:[" + e + "]:");
			errString = genericUtility.createErrorString(e);
			throw new ITMException(e);
		}
		finally
        {
			try
            {
				if(connectionObject != null)
				{
					if(stmt != null)
					{
						stmt.close();
						stmt=null;
					}
					connectionObject.close();
					connectionObject = null;
				}
		    }
			catch(Exception e)
			{
				BaseLogger.error("Exception :ProfCompEJB : [wfValData]:[" + e+ "]:");
				errString = genericUtility.createErrorString(e);
				throw new ITMException(e);
			}
        }  
		BaseLogger.debug("Return String :" + errString);
		return errString;
	}

	public String itemChanged() throws RemoteException,ITMException
	{
		return "";
	}

	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		BaseLogger.info(" ************* Inside [ProfCompEJB] item Changed() for "+currentColumn+" ************* ");

		Document dom = null;
		Document dom1 = null;	
		Document dom2 = null;	
		String valueXmlString = "";
		//GenericUtility genericUtility = GenericUtility.getInstance();
		BaseLogger.debug("[ProfCompEJB] xmlString : "  + xmlString);
		BaseLogger.debug("[ProfCompEJB] xmlString1 : "  + xmlString1);
		BaseLogger.debug("[ProfCompEJB] xmlString2 : "  + xmlString2);
		try
		{
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				dom = genericUtility.parseString(xmlString); 
			}
			if(xmlString1 != null && xmlString1.trim().length()!=0)
			{
				dom1 = genericUtility.parseString(xmlString1); 
			}
			valueXmlString = itemChanged(dom,dom1,objContext,currentColumn,editFlag,xtraParams);
			BaseLogger.info("ErrString :" + valueXmlString);
		}
		catch(Exception e)
		{
			BaseLogger.error("Exception :[ProfCompEJB] :[itemChanged]:[" + e + "]:");
			valueXmlString = genericUtility.createErrorString(e);
			throw new ITMException(e);
		}
		BaseLogger.info("valueXmlString.length():"+valueXmlString.length());
		BaseLogger.debug("valueXmlString :"+valueXmlString);
		return (valueXmlString);
	}

	public String itemChanged(Document dom, Document dom1, String objContext, String currentColumn,String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		ResultSet rs = null;
		String userId = null;
	
		String sql = null;
		String columnValue = null;
		String returnValue = null;
		String returnValue1 = "";
		Connection connectionObject = null;
		Statement stmt=null;
		StringBuffer valueXmlString = new StringBuffer();
		int currentFormNo = 0;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		//ITMDBAccessHome itmDBAccessHome = null;
		ITMDBAccessEJB itmDBAccess = null;
		try
		{					
			itmDBAccess = new ITMDBAccessEJB();
			//itmDBAccess = itmDBAccessHome.create();
			connectionObject = itmDBAccess.getConnection(); 

			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			columnValue = genericUtility.getColumnValue(currentColumn,dom);
			BaseLogger.info("Current Column for Item Changed  = >" +currentColumn);
			BaseLogger.info("ColumnValue for Item Changed  = >" +columnValue);
			
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><Header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></Header>");

			if (columnValue != null)
			{	
				switch (currentFormNo)
				{
					case 2:					   
						valueXmlString.append("<Detail2>");
						if (currentColumn.trim().equals("itm_default"))
						{				    
							valueXmlString.append("<active_yn>").append("Yes").append("</active_yn>");
							valueXmlString.append("<pick_source>").append("Master").append("</pick_source>");                        
						}
						else if (currentColumn.trim().equals("acct_code"))
						{						
							sql = "SELECT DESCR FROM   PROF_ACCOUNTS WHERE  ACCT_CODE='" + columnValue+ "'";
							BaseLogger.debug("Sql For Acct Code :"+sql);
							stmt = connectionObject.createStatement();
							rs = stmt.executeQuery(sql);
							if(rs.next())
							{
								returnValue = rs.getString(1);
								valueXmlString.append("<accounts_descr>").append(returnValue).append("</accounts_descr>");
							}
							else
                            {								
								valueXmlString.append("<accounts_descr>").append("").append("</accounts_descr>");
							}
							stmt.close();
							stmt=null;						                         
						}
						else if (currentColumn.trim().equals("wsheet_code"))
						{
							sql = "SELECT DESCR FROM PROF_WSHEET WHERE  WSHEET_CODE='" + columnValue+ "'";
							stmt = connectionObject.createStatement();
							rs = stmt.executeQuery(sql);
							if(rs.next())
							{
								returnValue = rs.getString(1);								
								valueXmlString.append("<wsheet_descr>").append(returnValue).append("</wsheet_descr>");                      
							}
							else
                            {
								valueXmlString.append("<wsheet_descr>").append("").append("</wsheet_descr>");
							}
							stmt.close();
							stmt=null;				
						}
						valueXmlString.append("</Detail2>");   
						break;
				}
			}			
			valueXmlString.append("</Root>");	
			connectionObject.close();
			connectionObject = null;
		}
		catch(Exception e)
		{
			BaseLogger.error("Exception :ProfCompEJB :itemChanged()[" + e + "]");
			valueXmlString.delete(0,valueXmlString.length());			
			valueXmlString = valueXmlString.append(genericUtility.createErrorString(e));			
			throw new ITMException(e);
		}
		finally
        {
			try
            {
				if(connectionObject != null)
				{				
					if(stmt != null)
					{
						stmt.close();
						stmt=null;
					}
					connectionObject.close();
					connectionObject = null;
				}
		    }
			catch(Exception e)
			{
				BaseLogger.error("Exception :ProfCompEJB :itemChanged :["+e+"]");
				valueXmlString.delete(0,valueXmlString.length());			
				valueXmlString = valueXmlString.append(genericUtility.createErrorString(e));		
				throw new ITMException(e);
			}
        } 
		BaseLogger.info("valueXmlString.length():"+valueXmlString.length());
		BaseLogger.debug("valueXmlString :"+valueXmlString.toString());
		return valueXmlString.toString();
	}
}
