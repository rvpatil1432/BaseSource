package ibase.webitm.ejb.dis;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
//import ibase.webitm.utility.GenericUtility;
import ibase.ejb.*;
import ibase.system.config.*;
import ibase.utility.BaseLogger;
import ibase.utility.E12GenericUtility;

import java.util.*;
import java.sql.*;
import java.io.*;

import java.rmi.RemoteException;
import javax.ejb.*;
import javax.naming.InitialContext;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import javax.ejb.Stateless; // added for ejb3


//public class SalesRealisationEJB extends ValidatorEJB implements SessionBean
@Stateless // added for ejb3
public class SalesRealisation extends ValidatorEJB implements SalesRealisationLocal, SalesRealisationRemote
{
	long timeRequire;
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
		BaseLogger.info("\n\n IN WF_VALDATA OF SalesRealisationEJB \n\n" );

		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;		
		String errString = "";

		BaseLogger.debug("\nxmlString : "  + xmlString);
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
			BaseLogger.error("Exception :SalesRealisationEJB :wfValData(String xmlString):" + e + ":");
			errString = genericUtility.createErrorString(e);
			throw new ITMException(e);
		}
		BaseLogger.debug("Return String :" + errString);
		return (errString);
	}

	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String ls_site_code=null;
		String ls_prod_line=null;
		String ls_wsheet_code=null;
		String ldt_tran_date_from=null;
		String ldt_tran_date_to=null;
		String ls_reprocess=null;
		String ls_cust_code_fr=null,ls_item_ser_fr=null,ls_state_code_fr=null,ls_area_code_fr=null;      
		String ls_cust_code_to=null,ls_item_ser_to=null,ls_state_code_to=null,ls_area_code_to=null;

        String userId = null;  
		String errString = "";

		NodeList parentList = null;
		NodeList childList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		int ctr;
		int noOfChilds = -1;

		Connection connectionObject = null;
		String sql=null;		
		ResultSet rs = null;
		PreparedStatement pstmt = null;
	//	ITMDBAccessHome itmDBAccessHome = null; // for ejb3
		ITMDBAccessEJB itmDBAccess = null;

		//GenericUtility genericUtility=GenericUtility.getInstance();
		try
		{
			this.timeRequire=System.currentTimeMillis();
			itmDBAccess = new ITMDBAccessEJB();
			//itmDBAccess = itmDBAccessHome.create();
			
			BaseLogger.debug("*******************[ SalesRealisationEJB ] xtraParams ************ \n"+xtraParams);
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			       
		    parentList = dom.getElementsByTagName("Detail1");
     		parentNode = parentList.item(0);
			childList = parentNode.getChildNodes();
			noOfChilds = childList.getLength();
			
			for(ctr = 0; ctr < noOfChilds; ctr++)
			{
				childNode = childList.item(ctr);
				childNodeName = childNode.getNodeName();

				if (childNodeName.equals("site_code"))
				{
					if (childNode.getFirstChild()==null || childNode.getFirstChild().getNodeValue()==null  )
					{
						errString = itmDBAccess.getErrorString("site_code","DSSRSITECD",userId,errString,connectionObject); 
						break;
					}
					else
					{
						String str=childNode.getFirstChild().getNodeValue().trim();
						StringBuffer strBuff = new StringBuffer();
						ArrayList tokenList = genericUtility.getTokenList(str, ",");
						for (int i = 0; i < tokenList.size(); i++ )
						{
							String currToken = (String)tokenList.get(i);
							strBuff.append("'").append(currToken).append("',");
						}
						if (strBuff.charAt(strBuff.length()-1) == ',')
						{
							strBuff.deleteCharAt(strBuff.length() - 1);
						}
						ls_site_code = strBuff.toString();
                   	}
	            }
				else if (childNodeName.equals("from_date"))
				{
					if (childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue() !=null)
					{
						ldt_tran_date_from = childNode.getFirstChild().getNodeValue().trim();
					}
					else
                    {
						errString = itmDBAccess.getErrorString("from_date","DSSRDATE",userId,errString,connectionObject); 
						break;
					}
				}
				else if (childNodeName.equals("to_date"))
				{
					if (childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue() !=null)
					{
						ldt_tran_date_to = childNode.getFirstChild().getNodeValue().trim();
					}	
					else
                    {
						errString = itmDBAccess.getErrorString("to_date","DSSRDATE",userId,errString,connectionObject); 
						break;
					}
				}
				else if (childNodeName.equals("cust_code_fr"))
				{
					if (childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue() !=null)
					{
						ls_cust_code_fr = childNode.getFirstChild().getNodeValue().trim();
					}
					else
                    {
						ls_cust_code_fr="00";
					}
				}
				else if (childNodeName.equals("cust_code_to"))
				{
					if (childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue() !=null)
					{
						ls_cust_code_to = childNode.getFirstChild().getNodeValue().trim();
					}
					else
                    {
						ls_cust_code_fr="ZZ";
					}
				}
				else if (childNodeName.equals("item_ser_fr"))
				{
					if (childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue() !=null)
					{
						ls_item_ser_fr = childNode.getFirstChild().getNodeValue().trim();
					}	
					else
                    {
						ls_cust_code_fr="00";
					}
				}
				else if (childNodeName.equals("item_ser_to"))
				{
					if (childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue() !=null)
					{
						ls_item_ser_to = childNode.getFirstChild().getNodeValue().trim();
					}
					else
                    {
						ls_cust_code_fr="ZZ";
					}
				}
				else if (childNodeName.equals("state_code_fr"))
				{
					if (childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue() !=null)
					{
						ls_state_code_fr = childNode.getFirstChild().getNodeValue().trim();
					}
					else
                    {
						ls_cust_code_fr="00";
					}
				}
				else if (childNodeName.equals("state_code_to"))
				{
					if (childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue() !=null)
					{
						ls_state_code_to = childNode.getFirstChild().getNodeValue().trim();
					}	
					else
                    {
						ls_cust_code_fr="ZZ";
					}
				}
				else if (childNodeName.equals("area_code_fr"))
				{			
					if (childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue() !=null)
					{
						ls_area_code_fr = childNode.getFirstChild().getNodeValue().trim();
					
					}
					else
					{
						ls_cust_code_fr="00";
					}

				}
				else if (childNodeName.equals("area_code_to"))
				{
					if (childNode.getFirstChild() != null)
					{
						ls_area_code_to = childNode.getFirstChild().getNodeValue().trim();
			
					}
					else
					{
						ls_cust_code_fr="ZZ";
					}
				}
				else if (childNodeName.equals("product_line"))
				{
					if (childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue() !=null)
					{
						ls_prod_line = childNode.getFirstChild().getNodeValue().trim();
					
					}	
					else
					{
						errString = itmDBAccess.getErrorString("product_line","DSPRODLN",userId,errString,connectionObject); 
						 break;
					}
				}
				else if (childNodeName.equals("worksheet_code"))
				{	
					if (childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue() !=null)
					{
						ls_wsheet_code=childNode.getFirstChild().getNodeValue().trim();						
					}
					else
					{
						errString = itmDBAccess.getErrorString("worksheet_code","DSWSHEETCD",userId,errString,connectionObject); 
						break;
					}
				}
				else if (childNodeName.equals("reprocess"))
				{
					
					if (childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue() !=null)
					{
						ls_reprocess= childNode.getFirstChild().getNodeValue().trim();
				
					}	
					else
					{
						ls_reprocess = "N";
					}
				}
				
	  		} 
			
           BaseLogger.info("Err String :"+errString+":");   
           if(errString.equals(""))
			{ 
				 sql="SELECT WSHEET_CODE FROM SALES_REALISATION"+ 
					" WHERE WSHEET_CODE='" +ls_wsheet_code+"'"+
					" AND  SITE_CODE IN ("+ls_site_code +")"+
					" AND TRAN_DATE BETWEEN ? AND ?"+
					" AND PRODUCT_LINE='"+ls_prod_line+"'"+ 
					" AND ITEM_SER BETWEEN '"+ls_item_ser_fr+"' AND '"+ls_item_ser_to+"'"+
					" AND ITEM_CODE BETWEEN '00' AND 'ZZ'"+
					" AND EXP_CODE BETWEEN '00' AND 'ZZ'"+
					" AND CUST_CODE BETWEEN '"+ls_cust_code_fr+"' AND '"+ls_cust_code_to+"'"+
					" AND  STATE_CODE BETWEEN '"+ls_state_code_fr+"' AND '"+ls_state_code_to+"'"+
					" AND AREA_CODE BETWEEN '"+ls_area_code_fr+"' AND '"+ls_area_code_to+"'";
				
				BaseLogger.debug(" sql :\n"+sql);
				connectionObject = itmDBAccess.getConnection(); 
				System.out.println("SQL ::"+sql);
				pstmt = connectionObject.prepareStatement(sql);
				pstmt.setTimestamp(1,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(ldt_tran_date_from,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
				pstmt.setTimestamp(2,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(ldt_tran_date_to,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
				
				rs = pstmt.executeQuery();			

				if(rs.next())  
				{
					if(ls_reprocess.equals("Y"))
					{
						sql="delete"+sql.substring(18);
						BaseLogger.info("Delete Query :\n"+sql);					
						pstmt = connectionObject.prepareStatement(sql);
						pstmt.setTimestamp(1,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(ldt_tran_date_from,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
						pstmt.setTimestamp(2,java.sql.Timestamp.valueOf(genericUtility.getValidDateTimeString(ldt_tran_date_to,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())));
						pstmt.executeUpdate();
					}   
					else
						errString = itmDBAccess.getErrorString("reprocess","DSSRPROCES",userId,errString,connectionObject); 
				}
                this.timeRequire=System.currentTimeMillis()-this.timeRequire;   
				BaseLogger.debug("\nThe Time Required For The Process : "+(this.timeRequire/60000)+" Min  and "+(this.timeRequire/1000)%60+" Sec\n\n");					
				pstmt.close();
				pstmt=null;
			}
		}
		catch(Exception e)
		{
			BaseLogger.error("Exception :SalesRealisationEJB :wfValData(Document dom):" + e + ":");
			errString = genericUtility.createErrorString(e);
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(connectionObject != null)
				{					
					if(pstmt != null)
					{
						pstmt.close();
						pstmt=null;
					}
					connectionObject.close();
					connectionObject = null;
				}				
			}
			catch(Exception e)
			{
				BaseLogger.error("Exception :SalesRealisationEJB :wfValData(Document dom):" + e + ":");
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
			BaseLogger.info("Exception :SalesRealisationEJB :itemChanged(String,String):" + e + ":");
			valueXmlString = genericUtility.createErrorString(e);			
			throw new ITMException(e);
		}
		BaseLogger.info("valueXmlString.length():"+valueXmlString.length());
		BaseLogger.debug("valueXmlString :"+valueXmlString);
		return (valueXmlString);
	}

	public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn,String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String columnValue =null;
		String userId = null;		
		StringBuffer valueXmlString =null;
		//GenericUtility genericUtility = GenericUtility.getInstance();

		try
		{
			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			columnValue = genericUtility.getColumnValue(currentColumn,dom);	
			
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><Header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></Header>");
			valueXmlString.append("<Detail1>");

			if (currentColumn.trim().equals("itm_default"))
			{
				valueXmlString.append("<cust_code_fr>").append("00").append("</cust_code_fr>");
				valueXmlString.append("<cust_code_to>").append("ZZ").append("</cust_code_to>");
				valueXmlString.append("<item_ser_fr>").append("00").append("</item_ser_fr>");
				valueXmlString.append("<item_ser_to>").append("ZZ").append("</item_ser_to>");
				valueXmlString.append("<state_code_fr>").append("00").append("</state_code_fr>");
				valueXmlString.append("<state_code_to>").append("ZZ").append("</state_code_to>");
				valueXmlString.append("<area_code_fr>").append("00").append("</area_code_fr>");
				valueXmlString.append("<area_code_to>").append("ZZ").append("</area_code_to>");
            }
            valueXmlString.append("</Detail1>");
			valueXmlString.append("</Root>");	
		}
		catch(Exception e)
		{
			BaseLogger.error("Exception :SalesRealisationEJB :itemChanged(Document,String):" + e+ ":");
			valueXmlString.delete(0,valueXmlString.length());			
			valueXmlString = valueXmlString.append(genericUtility.createErrorString(e));			
			throw new ITMException(e);
		}
		BaseLogger.info("valueXmlString.length():"+valueXmlString.length());
		BaseLogger.debug("valueXmlString :"+valueXmlString.toString());
		return valueXmlString.toString();
	}
}
