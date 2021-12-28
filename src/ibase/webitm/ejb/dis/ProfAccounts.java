package ibase.webitm.ejb.dis;
/*
** Purpose : Validate columns in wf_val_data function of window w_Prof_Accounts
**
*/
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

//public class ProfAccountsEJB extends ValidatorEJB implements SessionBean
@Stateless // added for ejb3
public class ProfAccounts extends ValidatorEJB implements ProfAccountsLocal, ProfAccountsRemote
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
	}*/   
	
    public String wfValData() throws RemoteException,ITMException
	{
		return "";
	}  
	
	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		BaseLogger.info("!!!!! IN WF_VALDATA OF PROF ACCOUNTS SCREEN !!!!!!!!!!!!!!!!!!!!!!!" );

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
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				dom2 = genericUtility.parseString(xmlString2); 
			}
			errString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			BaseLogger.error("[Exception] : [ProfAccountsEJB] :[wfValData] [" + e + "]");
			errString = genericUtility.createErrorString(e);
			throw new ITMException(e);
		}
		BaseLogger.debug("Return String :" + errString);
		return (errString);
	}

	public String wfValData(Document dom, Document dom1,Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
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
		//ITMDBAccessHome itmDBAccessHome = null; // for ejb3
		ITMDBAccessEJB itmDBAccess = null;			// for ejb3

		NodeList parentList = null;
		NodeList childList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		int ctr;
		int noOfChilds = -1;
		int currentFormNo =-1;
		
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
					case 1:
						
						if (childNodeName.equals("descr"))
						{
							if (childNode.getFirstChild() == null || childNode.getFirstChild().getNodeValue()==null)
							{
								errString = itmDBAccess.getErrorString("DESCR","DSACCTDSCR",userId,errString,connectionObject);
								
							}										
						}					
						break;			
				}				
			}
		}
		catch(Exception e)
		{
			BaseLogger.error("Exception :ProfAccountsEJB :wfValData():" + e + ":");
			errString = genericUtility.createErrorString(e);
			throw new ITMException(e);
		}
		BaseLogger.debug("Return String :" + errString);
		return (errString);
	}
}