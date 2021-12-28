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
import java.io.*;
import java.rmi.RemoteException;
import javax.ejb.*;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class ProfMstSeq extends ValidatorEJB implements ProfMstSeqLocal, ProfMstSeqRemote
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
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
	}   */
	
    public String wfValData() throws RemoteException,ITMException
	{
		return "";
	}  
	
	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		BaseLogger.info("!!!!! IN WF_VALDATA OF MSTSEQ SCREEN !!!!!!!!!!!!!!!!!!!!!!!" );
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
			BaseLogger.error("Exception :ProfMstSeqEJB :wfValData(String xmlString):" + e + ":");
			errString = genericUtility.createErrorString(e);
			throw new ITMException(e);
		}
		BaseLogger.debug("Return String :" + errString);
		return (errString);
	}

	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
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
					case 1:
							
						if(childNodeName.equals("wsheet_code") )
						{
							if(childNode.getFirstChild() == null || childNode.getFirstChild().getNodeValue() ==null)
							{
								errString = itmDBAccess.getErrorString("wsheet_code","VMWSHEET1",userId,errString,connectionObject); 
							}
							else
							{
								columnValue = childNode.getFirstChild().getNodeValue().trim();
								sql = "SELECT WSHEET_CODE FROM  PROF_WSHEET_COMP WHERE WSHEET_CODE ='"+columnValue+"'";
								BaseLogger.debug("SQL "+sql); 
								stmt = connectionObject.createStatement();
								rs = stmt.executeQuery(sql);
								if(rs.next()==false)
								{
									errString = itmDBAccess.getErrorString("wsheet_code","VMWSHEET1",userId,errString,connectionObject); 
								}
								stmt.close();
								stmt=null;
						    }
						}
						else if(childNodeName.equals("hier_ref"))
						{
							if(childNode.getFirstChild() == null || childNode.getFirstChild().getNodeValue() ==null )
							{
								errString = itmDBAccess.getErrorString("hier_ref","VMHIER",userId,errString,connectionObject); 
							}
							else
							{
								columnValue = childNode.getFirstChild().getNodeValue().trim();
								
								sql = "SELECT FLD_VALUE FROM GENCODES WHERE FLD_NAME = 'HIER_REF' AND FLD_VALUE = '"+columnValue+"'";
								BaseLogger.debug("SQL "+sql); 
								stmt = connectionObject.createStatement();
								rs = stmt.executeQuery(sql);
								if(rs.next()==false)
							    {
									errString = itmDBAccess.getErrorString("hier_ref","VMHIER",userId,errString,connectionObject); 
								}
								stmt.close();
								stmt=null;
								/*

								if(editFlag.equals("A")) 
								{
									columnValue1 = genericUtility.getColumnValue("wsheet_code",dom); 
									columnValue2 = genericUtility.getColumnValue("acct_code",dom);	 
									sql = "SELECT HIER_KEY FROM PROF_WSHEET_MSTSEQ WHERE WSHEET_CODE ='"+ columnValue1 +"' AND ACCT_CODE ='"+ columnValue2 +"' AND HIER_REF ='"+ columnValue +"'";
									stmt = connectionObject.createStatement();
									rs = stmt.executeQuery(sql);
									if(rs.next())
									{
										errString = itmDBAccess.getErrorString("hier_ref","VMHIERREF",userId,errString,connectionObject); 
									}
								}  
								*/
						    }
						}
						//Milind 03-10-2006
						else if(childNodeName.equals("hier_ref_1"))
						{
							if(childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue() !=null )
							{
								columnValue = childNode.getFirstChild().getNodeValue().trim();
								sql = "SELECT FLD_VALUE FROM GENCODES WHERE FLD_NAME = 'HIER_REF_1' AND FLD_VALUE = '"+columnValue+"'";
								BaseLogger.debug("SQL "+sql); 
								stmt = connectionObject.createStatement();
								rs = stmt.executeQuery(sql);
								if(rs.next()==false)
							    {
									errString = itmDBAccess.getErrorString("hier_ref_1","VMHIER",userId,errString,connectionObject); 
								}
								stmt.close();
								stmt=null;
								/*

								if(editFlag.equals("A")) 
								{
									columnValue1 = genericUtility.getColumnValue("wsheet_code",dom); 
									columnValue2 = genericUtility.getColumnValue("acct_code",dom);	 
									sql = "SELECT HIER_KEY FROM PROF_WSHEET_MSTSEQ WHERE WSHEET_CODE ='"+ columnValue1 +"' AND ACCT_CODE ='"+ columnValue2 +"' AND HIER_REF ='"+ columnValue +"'";
									stmt = connectionObject.createStatement();
									rs = stmt.executeQuery(sql);
									if(rs.next())
									{
										errString = itmDBAccess.getErrorString("hier_ref","VMHIERREF",userId,errString,connectionObject); 
									}
								}  
								*/
						    }
						}
						else if(childNodeName.equals("hier_ref_2"))
						{
							if(childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue() !=null )
							{
								columnValue = childNode.getFirstChild().getNodeValue().trim();
								sql = "SELECT FLD_VALUE FROM GENCODES WHERE FLD_NAME = 'HIER_REF_2' AND FLD_VALUE = '"+columnValue+"'";
								BaseLogger.debug("SQL "+sql); 
								stmt = connectionObject.createStatement();
								rs = stmt.executeQuery(sql);
								if(rs.next()==false)
							    {
									errString = itmDBAccess.getErrorString("hier_ref_2","VMHIER",userId,errString,connectionObject); 
								}
								stmt.close();
								stmt=null;
								/*
								if(editFlag.equals("A")) 
								{
									columnValue1 = genericUtility.getColumnValue("wsheet_code",dom); 
									columnValue2 = genericUtility.getColumnValue("acct_code",dom);	 
									sql = "SELECT HIER_KEY FROM PROF_WSHEET_MSTSEQ WHERE WSHEET_CODE ='"+ columnValue1 +"' AND ACCT_CODE ='"+ columnValue2 +"' AND HIER_REF ='"+ columnValue +"'";
									stmt = connectionObject.createStatement();
									rs = stmt.executeQuery(sql);
									if(rs.next())
									{
										errString = itmDBAccess.getErrorString("hier_ref","VMHIERREF",userId,errString,connectionObject); 
									}
								}  
								*/
						    }
						}   
						else if(childNodeName.equals("hier_ref_3"))
						{
							if(childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue() !=null )
							{
								columnValue = childNode.getFirstChild().getNodeValue().trim();
								sql = "SELECT FLD_VALUE FROM GENCODES WHERE FLD_NAME = 'HIER_REF_3' AND FLD_VALUE = '"+columnValue+"'";
								BaseLogger.debug("SQL "+sql); 
								stmt = connectionObject.createStatement();
								rs = stmt.executeQuery(sql);
								if(rs.next()==false)
							    {
									errString = itmDBAccess.getErrorString("hier_ref_3","VMHIER",userId,errString,connectionObject); 
								}
								stmt.close();
								stmt=null;
								/*
								if(editFlag.equals("A")) 
								{
									columnValue1 = genericUtility.getColumnValue("wsheet_code",dom); 
									columnValue2 = genericUtility.getColumnValue("acct_code",dom);	 
									sql = "SELECT HIER_KEY FROM PROF_WSHEET_MSTSEQ WHERE WSHEET_CODE ='"+ columnValue1 +"' AND ACCT_CODE ='"+ columnValue2 +"' AND HIER_REF ='"+ columnValue +"'";
									stmt = connectionObject.createStatement();
									rs = stmt.executeQuery(sql);
									if(rs.next())
									{
										errString = itmDBAccess.getErrorString("hier_ref","VMHIERREF",userId,errString,connectionObject); 
									}
								}  
								*/
						    }
						}   
						//Milind 03-10-2006

						else if(childNodeName.equals("acct_code"))
						{
							if(childNode.getFirstChild() == null || childNode.getFirstChild().getNodeValue() ==null)
							{
								errString = itmDBAccess.getErrorString("acct_code","VMWSHEET1",userId,errString,connectionObject); 
							}
							else
						    {
								columnValue1 = childNode.getFirstChild().getNodeValue().trim();
								sql = "SELECT ACCT_CODE FROM  PROF_WSHEET_COMP WHERE WSHEET_CODE ='"+columnValue+"' AND ACCT_CODE='"+columnValue1+"'" ;
								BaseLogger.debug("SQL "+sql); 
								stmt = connectionObject.createStatement();
								rs = stmt.executeQuery(sql);
								if(rs.next()==false)
								{
									errString = itmDBAccess.getErrorString("acct_code","VMACTCODE",userId,errString,connectionObject); 
								}
								stmt.close();
								stmt=null;
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
			BaseLogger.error("Exception :ProfMstSeqEJB :wfValData(Document dom):" + e + ":");
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
				BaseLogger.error("Exception :ProfMstSeqEJB:getItemSer :==>"+e);
				errString = genericUtility.createErrorString(e);
				throw new ITMException(e);
			}
        }  
		BaseLogger.debug("Return String :" + errString);
		return (errString);
	}

	public String itemChanged() throws RemoteException,ITMException
	{   
		return "";
	}

	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		System.out.println("IN itemChanged FOR PROFMSTSEQ !!!!!!!!!!!!!!!! "  );
		Document dom = null;
		Document dom1 = null;	
		Document dom2 = null;	
		String valueXmlString = "";
		//GenericUtility genericUtility = GenericUtility.getInstance();

		BaseLogger.debug("xmlString : "  + xmlString);
		BaseLogger.debug("xmlString1 : "  + xmlString1);
		BaseLogger.debug("xmlString2 : "  + xmlString2);
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
			valueXmlString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams);
			BaseLogger.info("ErrString :" + valueXmlString);
		}
		catch(Exception e)
		{
			BaseLogger.error("Exception :ProfMstSeqEJB :itemChanged(String,String):" + e + ":");
			valueXmlString = genericUtility.createErrorString(e);
			throw new ITMException(e);
		}
		BaseLogger.info("valueXmlString.length():"+valueXmlString.length());
		BaseLogger.debug("valueXmlString :"+valueXmlString.toString());
		return (valueXmlString);
	}

	public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn,String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String userId = null;
		String columnValue = null;
		String columnValue1 = null;  
		String returnValue = null;
		//String returnValue1 = "";   

		String sql = null;
		ResultSet rs = null;
		Connection connectionObject = null;
		Statement stmt = null;
		Statement stmtLineNo = null; 
		ResultSet rsLineNo = null;  

		//ITMDBAccessHome itmDBAccessHome = null; //for ejb3
		ITMDBAccessEJB itmDBAccess = null;

		StringBuffer valueXmlString = new StringBuffer();
		int currentFormNo = 0;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		try
		{		
			itmDBAccess = new ITMDBAccessEJB();
			//itmDBAccess = itmDBAccessHome.create();
			connectionObject = itmDBAccess.getConnection(); 

			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			columnValue = genericUtility.getColumnValue(currentColumn,dom);

			BaseLogger.info("Current Column for Item Changed  = >" +currentColumn);
			BaseLogger.info("ColumnValue for Item Changed  = >" +columnValue);

			columnValue = genericUtility.getColumnValue(currentColumn,dom);
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><Header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></Header>");

			switch (currentFormNo)
			{
				case 1:

					valueXmlString.append("<Detail1>");
				    
					if(currentColumn.trim().equals("itm_defaultedit"))   
					{
						columnValue = genericUtility.getColumnValue("wsheet_code",dom);
						valueXmlString.append("<wsheet_code protect='1'>").append(columnValue).append("</wsheet_code>"); 
						columnValue = genericUtility.getColumnValue("acct_code",dom);
						valueXmlString.append("<acct_code protect='1'>").append(columnValue).append("</acct_code>"); 
						columnValue = genericUtility.getColumnValue("hier_ref",dom);
						columnValue = ( columnValue != null )? columnValue : "";
						valueXmlString.append("<hier_ref protect='1'>").append(columnValue).append("</hier_ref>"); 
						columnValue = genericUtility.getColumnValue("hier_ref_1",dom);
						columnValue = ( columnValue != null )? columnValue : "";
						valueXmlString.append("<hier_ref_1 protect='1'>").append(columnValue).append("</hier_ref_1>"); 
						columnValue = genericUtility.getColumnValue("hier_ref_2",dom);
						columnValue = ( columnValue != null )? columnValue : "";
						valueXmlString.append("<hier_ref_2 protect='1'>").append(columnValue).append("</hier_ref_2>"); 
						columnValue = genericUtility.getColumnValue("hier_ref_3",dom);
						columnValue = ( columnValue != null )? columnValue : "";
						valueXmlString.append("<hier_ref_3 protect='1'>").append(columnValue).append("</hier_ref_3>"); 

					}	
					else if(currentColumn.trim().equals("hier_ref"))   
					{
						if(columnValue==null)
						{
							valueXmlString.append("<cc_hier_ref_descr>").append("").append("</cc_hier_ref_descr>");
						}
						else
                        { 
							sql = "SELECT DESCR FROM GENCODES WHERE FLD_VALUE ='"+columnValue+"' AND FLD_NAME = 'HIER_REF' ";
							BaseLogger.info("the query is ----->"+sql);
							stmt = connectionObject.createStatement();
							rs = stmt.executeQuery(sql);
							if(rs.next())
							{
								returnValue = rs.getString(1);
								valueXmlString.append("<cc_hier_ref_descr>").append(returnValue).append("</cc_hier_ref_descr>");
							}
							else
							{
								valueXmlString.append("<cc_hier_ref_descr>").append("").append("</cc_hier_ref_descr>");
							}
							stmt.close();
							stmt=null;
						}						
					}
					//Milind 03-10-2006
					else if(currentColumn.trim().equals("hier_ref_1"))   
					{
						if(columnValue==null)
						{
							valueXmlString.append("<cc_hier_ref_1_descr>").append("").append("</cc_hier_ref_1_descr>");
						}
						else
                        { 
							sql = "SELECT DESCR FROM GENCODES WHERE FLD_VALUE ='"+columnValue+"' AND FLD_NAME = 'HIER_REF_1' ";
							BaseLogger.info("the query is ----->"+sql);
							stmt = connectionObject.createStatement();
							rs = stmt.executeQuery(sql);
							if(rs.next())
							{
								returnValue = rs.getString(1);
								valueXmlString.append("<cc_hier_ref_1_descr>").append(returnValue).append("</cc_hier_ref_1_descr>");
							}
							else
							{
								valueXmlString.append("<cc_hier_ref_1_descr>").append("").append("</cc_hier_ref_1_descr>");
							}
							stmt.close();
							stmt=null;
						}						
					}  
					else if(currentColumn.trim().equals("hier_ref_2"))   
					{
						if(columnValue==null)
						{
							valueXmlString.append("<cc_hier_ref_2_descr>").append("").append("</cc_hier_ref_2_descr>");
						}
						else
                        { 
							sql = "SELECT DESCR FROM GENCODES WHERE FLD_VALUE ='"+columnValue+"' AND FLD_NAME = 'HIER_REF_2' ";
							BaseLogger.info("the query is ----->"+sql);
							stmt = connectionObject.createStatement();
							rs = stmt.executeQuery(sql);
							if(rs.next())
							{
								returnValue = rs.getString(1);
								valueXmlString.append("<cc_hier_ref_2_descr>").append(returnValue).append("</cc_hier_ref_2_descr>");
							}
							else
							{
								valueXmlString.append("<cc_hier_ref_2_descr>").append("").append("</cc_hier_ref_2_descr>");
							}
							stmt.close();
							stmt=null;
						}						
					}  
					else if(currentColumn.trim().equals("hier_ref_3"))   
					{
						if(columnValue==null)
						{
							valueXmlString.append("<cc_hier_ref_3_descr>").append("").append("</cc_hier_ref_3_descr>");
						}
						else
                        { 
							sql = "SELECT DESCR FROM GENCODES WHERE FLD_VALUE ='"+columnValue+"' AND FLD_NAME = 'HIER_REF_3' ";
							BaseLogger.info("the query is ----->"+sql);
							stmt = connectionObject.createStatement();
							rs = stmt.executeQuery(sql);
							if(rs.next())
							{
								returnValue = rs.getString(1);
								valueXmlString.append("<cc_hier_ref_3_descr>").append(returnValue).append("</cc_hier_ref_3_descr>");
							}
							else
							{
								valueXmlString.append("<cc_hier_ref_3_descr>").append("").append("</cc_hier_ref_3_descr>");
							}
							stmt.close();
							stmt=null;
						}						
					}  
//End Milind 03-10-2006

					else if(currentColumn.trim().equals("wsheet_code"))
					{				
						if(columnValue==null)
						{
							valueXmlString.append("<prof_wsheet_descr>").append("").append("</prof_wsheet_descr>");
						}
						else
                        {
							sql = "SELECT DESCR FROM PROF_WSHEET WHERE WSHEET_CODE ='"+columnValue+"'";
							BaseLogger.info("the query is ----->"+sql);
							stmt = connectionObject.createStatement();
							rs = stmt.executeQuery(sql);
							if(rs.next())
							{
								returnValue = rs.getString(1);	
								valueXmlString.append("<prof_wsheet_descr>").append(returnValue).append("</prof_wsheet_descr>");
							}
							else
							{
								valueXmlString.append("<prof_wsheet_descr>").append("").append("</prof_wsheet_descr>");
							}
							stmt.close();
							stmt=null;						
						} 
					}	
					else if(currentColumn.trim().equals("acct_code"))
					{
						if(columnValue==null)
						{
								valueXmlString.append("<prof_accounts_descr>").append("").append("</prof_accounts_descr>");
						}
						else
                        { 
							sql = "SELECT DESCR FROM PROF_ACCOUNTS WHERE ACCT_CODE ='"+columnValue+"'";
							BaseLogger.info("the query is ----->"+sql);
							stmt = connectionObject.createStatement();
							rs = stmt.executeQuery(sql);
							if(rs.next())
							{
								returnValue = rs.getString(1);                               
								valueXmlString.append("<prof_accounts_descr>").append(returnValue).append("</prof_accounts_descr>");
								columnValue1 = genericUtility.getColumnValue("wsheet_code",dom);            
								sql = "SELECT MAX(LINE_NO ) FROM PROF_WSHEET_MSTSEQ WHERE WSHEET_CODE='"+columnValue1+"' AND ACCT_CODE = '"+columnValue +"'";
								stmtLineNo = connectionObject.createStatement();
								rsLineNo = stmtLineNo.executeQuery(sql);
								if(rsLineNo.next())
								{
									int lineNo= rsLineNo.getInt(1) +1;  
									valueXmlString.append("<line_no>").append(lineNo).append("</line_no>");
								}
								stmtLineNo.close();
								stmtLineNo = null;		 								
							}
							else
							{
								valueXmlString.append("<prof_accounts_descr>").append("").append("</prof_accounts_descr>");
							}
							stmt.close();
							stmt=null;		
						}	
					}                        				
					valueXmlString.append("</Detail1>");
					break;
			}			
			valueXmlString.append("</Root>");	
		}
		catch(Exception e)
		{
				BaseLogger.error("Exception :ProfMstSeqEJB :itemChanged(Document,String):" + e + ":");
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
					if(stmtLineNo != null)  
					{
						stmtLineNo.close();
						stmtLineNo = null;	
					}								
					connectionObject.close();
					connectionObject = null;
				}
			}
			catch(Exception e)
			{
				BaseLogger.error("Exception :ProfMstSeqEJB :itemChanged :["+e+"]");
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
