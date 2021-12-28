package ibase.webitm.ejb.dis;

import ibase.ejb.*;
import ibase.webitm.ejb.*;
import ibase.webitm.utility.ITMException;
//import ibase.webitm.utility.GenericUtility;
import ibase.system.config.*;
import ibase.utility.BaseLogger;
import ibase.utility.E12GenericUtility;

import java.util.*;
import java.util.Date;
import java.io.*;
import java.sql.*;

import java.rmi.RemoteException;
import javax.ejb.*;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.ejb.Stateless; // added for ejb3


//public class ProfMstDetEJB extends ValidatorEJB implements SessionBean
@Stateless // added for ejb3
public class ProfMstDet extends ValidatorEJB implements ProfMstDetLocal, ProfMstDetRemote
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
		BaseLogger.info("\n\n IN WF_VALDATA OF MSTDET SCREEN \n\n" );
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
			BaseLogger.error("Exception :ProfMstdetEJB :wfValData(String xmlString):" + e + ":");
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
		String columnValue1 =null;
		String columnValue2 = null;
		String columnValue3 = null; 
		String returnValue=null;

		String userId = null;
		String errString = "";

		Connection connectionObject = null;
		Statement stmt = null;
		ResultSet rs = null;
		ResultSet rs2 = null;
		String sql = null;
		String sql2 = null;
		
		java.util.Date to_date = null;
		java.util.Date from_date = null;
		java.util.Date eff_from_date = null;
		java.util.Date columnDate = null;
		
		NodeList parentList = null;
		NodeList childList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		int ctr;
		int noOfChilds = -1;
		int currentFormNo = -1;

		//GenericUtility genericUtility = GenericUtility.getInstance();
		//ITMDBAccessHome itmDBAccessHome = null; // for ejb3
		ITMDBAccessEJB itmDBAccess = null; //for ejb3
		try
		{
			itmDBAccess = new ITMDBAccessEJB();
			//itmDBAccess = itmDBAccessHome.create();
			connectionObject = itmDBAccess.getConnection(); 			

			System.out.println("******* [ ProfMstdetEJB ] Edit Flag :["+	editFlag+"]");
			BaseLogger.debug("*******************[ ProfMstdetEJB ] xtraParams ************ \n"+xtraParams);

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
						if (childNodeName.equals("hier_key"))
						{
							if (childNode.getFirstChild() == null || childNode.getFirstChild().getNodeValue()==null)
							{
								errString = itmDBAccess.getErrorString("hier_key","VMHIERKEY",userId,errString,connectionObject); 
								break;
							}										
							else
							{
								columnValue = childNode.getFirstChild().getNodeValue().trim();
							    sql = "SELECT HIER_KEY FROM PROF_WSHEET_MSTSEQ WHERE HIER_KEY='" + columnValue+"'";
							    BaseLogger.debug("Checking hier_key in prof_wsheet_mstdet table\n Query:"+sql);
								stmt = connectionObject.createStatement();
							    rs = stmt.executeQuery(sql);
							    if(!rs.next())
							    {
									errString = itmDBAccess.getErrorString("hier_key","VMHIERKEY",userId,errString,connectionObject); 
									break;
							    }
							    stmt.close();
							    stmt=null;                         
							}                            
				        }
						else if(childNodeName.equals("line_no"))
						{
							columnValue1 = genericUtility.getColumnValue("hier_key",dom);
							columnValue = childNode.getFirstChild().getNodeValue();
							sql="SELECT HIER_KEY FROM PROF_WSHEET_MSTDET WHERE HIER_KEY='"+columnValue1+"' AND LINE_NO="+columnValue ;
							stmt = connectionObject.createStatement();
							rs = stmt.executeQuery(sql);
							if(rs.next() && editFlag.equals("A"))
							{											
								errString = itmDBAccess.getErrorString("line_no","VMLINENO",userId,errString,connectionObject); 
								break;
							}  
							stmt.close();
							stmt=null;   

						}
                        else if(childNodeName.equals("hier_value"))
						{							
							if (childNode.getFirstChild() == null || childNode.getFirstChild().getNodeValue()==null )
							{
								columnValue1 = genericUtility.getColumnValue("hier_key",dom);
								columnValue2=genericUtility.getColumnValue("line_no",dom);   
								sql="SELECT HIER_KEY FROM PROF_WSHEET_MSTDET WHERE HIER_KEY='"+columnValue1+"' AND LINE_NO!="+columnValue2+" AND HIER_VALUE IS NULL "  ;
								stmt = connectionObject.createStatement();
								rs = stmt.executeQuery(sql);
								if(rs.next())
								{
									errString = itmDBAccess.getErrorString("hier_value","VMHIERVAL",userId,errString,connectionObject); 
									break;
								}  
							    stmt.close();
							    stmt=null;                         
							}
							else
							{
								//Milind 03-10-2006
/*
								columnValue1 = genericUtility.getColumnValue("hier_ref",dom);
								columnValue = childNode.getFirstChild().getNodeValue().trim();

								returnValue = getHierValueDescription("HIER_REF",columnValue1 ,columnValue , "V");
								BaseLogger.debug("Return Value from fuction ddf_get_hiervalue_descr :"+returnValue);
								if(!returnValue.equals("OK"))
								{
									errString = itmDBAccess.getErrorString("hier_value","VMHIERVAL",userId,errString,connectionObject); 
									break;
								}	
*/
							}
						}
						else if (childNodeName.equals("data_type"))
						{
							if (childNode.getFirstChild() == null || childNode.getFirstChild().getNodeValue()==null)
							{
								errString = itmDBAccess.getErrorString("apply_on","VMDTTYPE",userId,errString,connectionObject); 
								break;
							}
   				        }
						else if (childNodeName.equals("apply_on"))
						{
							if (childNode.getFirstChild() == null || childNode.getFirstChild().getNodeValue()==null)
							{
								errString = itmDBAccess.getErrorString("apply_on","VMAPPLYON",userId,errString,connectionObject); 
								break;
							}
							else
							{
								columnValue = childNode.getFirstChild().getNodeValue().trim();
								columnValue1 = genericUtility.getColumnValue("data_type",dom);
								if( ( columnValue1.equals("F")) && (columnValue.equals("B")||columnValue.equals("V")||columnValue.equals("T")||columnValue.equals("I")))
								{
									errString = itmDBAccess.getErrorString("apply_on","VMAPPLYON",userId,errString,connectionObject); 
								    break;
								}
							}
   				        }
					    else if (childNodeName.equals("eff_from"))
						{	
							System.out.println("eff_from : getFirstChild : [" + childNode.getFirstChild()+"]");
							if(childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue()!=null )
							{
								columnValue = childNode.getFirstChild().getNodeValue().trim();
								columnValue1 = genericUtility.getColumnValue("hier_key",dom);
								columnDate= genericUtility.getDateObject(columnValue); 
								columnValue2 = genericUtility.getColumnValue("line_no",dom);
								columnValue3 = genericUtility.getColumnValue("hier_value",dom); 
																																						
								sql="SELECT EFF_FROM,VALID_UPTO FROM PROF_WSHEET_MSTDET WHERE HIER_KEY='"+columnValue1+"' AND LINE_NO!="+columnValue2 + " AND HIER_VALUE IS NOT NULL AND HIER_VALUE ='"+columnValue3 +"'"; 
								BaseLogger.debug("\n  Sql for From Date Check :"+sql);
								stmt = connectionObject.createStatement();
							    rs = stmt.executeQuery(sql);
							    while(rs.next())
								{
									from_date=(java.util.Date)rs.getDate(1);
									to_date=(java.util.Date)rs.getDate(2);
									if((columnDate.compareTo(from_date)>=0) && (columnDate.compareTo(to_date)<=0))
									{
										BaseLogger.info("\n  ------------ eff_from Date Falls in Already present Date Range ------------ \n");  
										errString = itmDBAccess.getErrorString("eff_from","VMDATEFRTO",userId,errString,connectionObject); 
										break;
									}	
								}
								stmt.close();
								stmt=null;
							}
							else
                            {
								errString = itmDBAccess.getErrorString("eff_from","DSSRDATE",userId,errString,connectionObject); 
								break;
							}
						}
					    else if (childNodeName.equals("valid_upto"))
						{
							System.out.println("valid_upto : getFirstChild : [" + childNode.getFirstChild()+"]");
							if(childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue()!=null)
							{
								columnValue = childNode.getFirstChild().getNodeValue().trim();
								columnDate= genericUtility.getDateObject(columnValue);
								columnValue1 = genericUtility.getColumnValue("eff_from",dom);
								to_date = genericUtility.getDateObject(columnValue);
								eff_from_date = genericUtility.getDateObject(columnValue1);
								if  (to_date.compareTo(eff_from_date)<0)
								{
									errString = itmDBAccess.getErrorString("Valid_upto","VMVALIDTO",userId,errString,connectionObject);
									 break;
								}

								columnValue1 = genericUtility.getColumnValue("hier_key",dom);
								columnValue2=genericUtility.getColumnValue("line_no",dom);
								columnValue3 = genericUtility.getColumnValue("hier_value",dom); 

								stmt = connectionObject.createStatement();																					
								sql="SELECT EFF_FROM,VALID_UPTO FROM PROF_WSHEET_MSTDET WHERE HIER_KEY='"+columnValue1+"' AND LINE_NO!="+columnValue2+ " AND HIER_VALUE IS NOT NULL AND HIER_VALUE ='"+columnValue3 +"'"; 
								BaseLogger.debug("\n  Sql for valid_upto Date Check :\n"+sql);
								rs = stmt.executeQuery(sql);								
								while(rs.next())
								{
									from_date=(java.util.Date)rs.getDate(1);
									to_date=(java.util.Date)rs.getDate(2);
									if(((columnDate.compareTo(from_date)>=0) && (columnDate.compareTo(to_date)<=0)) ||  ((eff_from_date.compareTo(from_date)<=0) && (columnDate.compareTo(to_date)>=0)) )
									{
										BaseLogger.debug("\n  ------------valid_upto Date Falls in Already present Dates  ------------ \n");  
										errString = itmDBAccess.getErrorString("valid_upto","VMDATEFRTO",userId,errString,connectionObject); 
										break;
									}	
								}
								stmt.close();
								stmt=null;
							}
							else
                            {
								errString = itmDBAccess.getErrorString("valid_upto","DSSRDATE",userId,errString,connectionObject); 
								break;
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
			BaseLogger.error("Exception :ProfMstdetEJB :wfValData : [" + e + "]");
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
					//Milind 

					connectionObject.close();
					connectionObject = null;
				}
		    }
			catch(Exception e)
			{
				BaseLogger.error("Exception :ProfMstdetEJB :getItemSer :==>"+e);
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
		BaseLogger.info(" ************* Inside [ProfMstdetEJB] item Changed() for "+currentColumn+" ************* ");
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
		}
		catch(Exception e)
		{
			BaseLogger.error("Exception :ProfMstdetEJB :itemChanged(String,String):" + e + ":");
			valueXmlString = genericUtility.createErrorString(e);
			throw new ITMException(e);
		}	
		BaseLogger.info("valueXmlString.length():"+valueXmlString.length());
		BaseLogger.debug("valueXmlString :"+valueXmlString);
		return (valueXmlString);
	}

	public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn,String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		int n;
		ResultSet rs = null;
		String columnValue = null;  
		String returnValue = null;  
		String columnValue1 = null; 
		StringBuffer valueXmlString = new StringBuffer();
		int currentFormNo = 0;
		//GenericUtility genericUtility = GenericUtility.getInstance();

		Connection connectionObject = null;
		Statement stmt=null;
		//Milind 05-10-2006
		PreparedStatement pstmt=null;
		Statement stmtLineNo = null; 
		ResultSet rsLineNo = null;
		//Milind 05-10-2006
		ResultSet rsFunct = null;
		String sql = null; 
		//ITMDBAccessHome itmDBAccessHome = null; // for ejb3
		ITMDBAccessEJB itmDBAccess = null;
		try
		{
			itmDBAccess = new ITMDBAccessEJB();
			//itmDBAccess = itmDBAccessHome.create();
			connectionObject = itmDBAccess.getConnection();  			
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
					if (currentColumn.trim().equals("itm_default"))
					{
						valueXmlString.append("<data_value>").append("00.00").append("</data_value>");		
					}
					if(currentColumn.trim().equals("itm_defaultedit")) 
					{
						columnValue = genericUtility.getColumnValue("hier_key",dom);
						valueXmlString.append("<hier_key protect='1'>").append(columnValue).append("</hier_key>"); 
						columnValue = genericUtility.getColumnValue("line_no",dom);
						valueXmlString.append("<line_no protect='1'>").append(columnValue).append("</line_no>"); 
					}   
					else if(currentColumn.trim().equals("hier_key"))
					{
//MILIND 10/5/2006
						//sql="SELECT  PROF_WSHEET_MSTSEQ.WSHEET_CODE,PROF_WSHEET_MSTSEQ.ACCT_CODE,PROF_WSHEET_MSTSEQ.LINE_NO,PROF_WSHEET_MSTSEQ.HIER_REF,PROF_WSHEET.DESCR,PROF_ACCOUNTS.DESCR,GENCODES.DESCR,PROF_WSHEET_MSTSEQ.HIER_KEY,PROF_WSHEET_MSTSEQ.HIER_REF_1,PROF_WSHEET_MSTSEQ.HIER_REF_2,PROF_WSHEET_MSTSEQ.HIER_REF_3  FROM  PROF_ACCOUNTS,PROF_WSHEET_MSTSEQ,PROF_WSHEET,GENCODES WHERE ( PROF_ACCOUNTS.ACCT_CODE = PROF_WSHEET_MSTSEQ.ACCT_CODE ) and ( PROF_WSHEET_MSTSEQ.WSHEET_CODE = PROF_WSHEET.WSHEET_CODE ) and (GENCODES.FLD_VALUE = PROF_WSHEET_MSTSEQ.HIER_REF ) and    ( GENCODES.FLD_NAME = 'HIER_REF' ) and PROF_WSHEET_MSTSEQ.HIER_KEY = '" +columnValue+ "'";
						sql="SELECT  PROF_WSHEET_MSTSEQ.WSHEET_CODE,PROF_WSHEET_MSTSEQ.ACCT_CODE,PROF_WSHEET_MSTSEQ.LINE_NO,PROF_WSHEET_MSTSEQ.HIER_REF,PROF_WSHEET.DESCR,PROF_ACCOUNTS.DESCR,PROF_WSHEET_MSTSEQ.HIER_REF_1,PROF_WSHEET_MSTSEQ.HIER_REF_2,PROF_WSHEET_MSTSEQ.HIER_REF_3  FROM  PROF_ACCOUNTS,PROF_WSHEET_MSTSEQ,PROF_WSHEET WHERE ( PROF_ACCOUNTS.ACCT_CODE = PROF_WSHEET_MSTSEQ.ACCT_CODE ) and ( PROF_WSHEET_MSTSEQ.WSHEET_CODE = PROF_WSHEET.WSHEET_CODE )  and   PROF_WSHEET_MSTSEQ.HIER_KEY = '" +columnValue+ "'";

						stmt = connectionObject.createStatement();
						rs = stmt.executeQuery(sql);
						if(rs.next())
						{
							returnValue = rs.getString(1);
							//changed by msalam on 220708 for name changed in data window
							
							//valueXmlString.append("<prof_wsheet_mstseq_wsheet_code>").append(returnValue).append("</prof_wsheet_mstseq_wsheet_code>");
							valueXmlString.append("<wsheet_code>").append(returnValue).append("</wsheet_code>");
							returnValue = rs.getString(2);
							//valueXmlString.append("<prof_wsheet_mstseq_acct_code>").append(returnValue).append("</prof_wsheet_mstseq_acct_code>");
							valueXmlString.append("<acct_code>").append(returnValue).append("</acct_code>");
							//changed by msalam on 220708 for name changed in data window and 
							returnValue = rs.getString(3);
							valueXmlString.append("<prof_wsheet_mstseq_line_no>").append(returnValue).append("</prof_wsheet_mstseq_line_no>");

							returnValue = rs.getString(4);
							valueXmlString.append("<prof_wsheet_mstseq_hier_ref>").append(returnValue).append("</prof_wsheet_mstseq_hier_ref>");

							returnValue = ( returnValue != null )? returnValue : "";
							//changed by msalam on 220708 for unable to set value 
							String functStr = null;
							functStr = "select descr " +  
									   " from gencodes  " +
									   " where fld_value = ?" +
									   " and fld_name= ? " +
									   " and mod_name= ?";
							//functStr = "select fn_getdescr_gencod( '" + "HIER_REF" + "','" + "W_PROF_WSHEET_MSTDET" + "', '" + returnValue.trim() +"') from dual";
							//pstmt = connectionObject.prepareStatement("select fn_getdescr_gencod(?,?,?) from dual");
							pstmt = connectionObject.prepareStatement( functStr );
							pstmt.setString(1, "HIER_REF");
							pstmt.setString(2, "W_PROF_WSHEET_MSTDET");
							pstmt.setString(3, returnValue);
							//changed by msalam on 220708 for unable to set value  end 
							rsFunct = pstmt.executeQuery();
							if( rsFunct.next())
							{
								returnValue = rsFunct.getString(1);
							}
							//added by msalam on 220708 as result set was not closed
							rsFunct.close();
							rsFunct = null;
							//end by msalam on 220708
							returnValue = ( returnValue != null )? returnValue : "";
							valueXmlString.append("<cc_hier_ref_descr>").append(returnValue).append("</cc_hier_ref_descr>");
							

							returnValue = rs.getString(5);
							valueXmlString.append("<prof_wsheet_descr>").append(returnValue).append("</prof_wsheet_descr>");

							returnValue = rs.getString(6);
							valueXmlString.append("<prof_accounts_descr>").append(returnValue).append("</prof_accounts_descr>");


							returnValue = rs.getString("HIER_REF_1");
							returnValue = ( returnValue != null )? returnValue : "";
							valueXmlString.append("<prof_wsheet_mstseq_hier_ref_1>").append(returnValue).append("</prof_wsheet_mstseq_hier_ref_1>");
							
							pstmt.clearParameters();
							pstmt.setString(1, "HIER_REF_1");
							pstmt.setString(2, "W_PROF_WSHEET_MSTDET");
							pstmt.setString(3, returnValue);
							rsFunct = pstmt.executeQuery();
							if( rsFunct.next())
							{
								returnValue = rsFunct.getString(1);
							}
							returnValue = ( returnValue != null )? returnValue : "";
							valueXmlString.append("<cc_hier_ref_1_descr>").append(returnValue).append("</cc_hier_ref_1_descr>");

							returnValue = rs.getString("HIER_REF_2");
							returnValue = ( returnValue != null )? returnValue : "";
							valueXmlString.append("<prof_wsheet_mstseq_hier_ref_2>").append(returnValue).append("</prof_wsheet_mstseq_hier_ref_2>");

							pstmt.clearParameters();
							pstmt.setString(1, "HIER_REF_2");
							pstmt.setString(2, "W_PROF_WSHEET_MSTDET");
							pstmt.setString(3, returnValue);
							rsFunct = pstmt.executeQuery();
							if( rsFunct.next())
							{
								returnValue = rsFunct.getString(1);
							}
							//added by msalam on 220708 as result set was not closed
							rsFunct.close();
							rsFunct = null;
							//end by msalam on 220708

							returnValue = ( returnValue != null )? returnValue : "";
							valueXmlString.append("<cc_hier_ref_2_descr>").append(returnValue).append("</cc_hier_ref_2_descr>");

							returnValue = rs.getString("HIER_REF_3");
							returnValue = ( returnValue != null )? returnValue : "";
							valueXmlString.append("<prof_wsheet_mstseq_hier_ref_3>").append(returnValue).append("</prof_wsheet_mstseq_hier_ref_3>");

							pstmt.clearParameters();
							pstmt.setString(1, "HIER_REF_3");
							pstmt.setString(2, "W_PROF_WSHEET_MSTDET");
							pstmt.setString(3, returnValue);
							rsFunct = pstmt.executeQuery();
							if( rsFunct.next())
							{
								returnValue = rsFunct.getString(1);
							}
							//added by msalam on 220708 as result set was not closed
							rsFunct.close();
							rsFunct = null;
							//end by msalam on 220708							
							returnValue = ( returnValue != null )? returnValue : "";
							valueXmlString.append("<cc_hier_ref_3_descr>").append(returnValue).append("</cc_hier_ref_3_descr>");
							pstmt.close();
							pstmt = null;
                 //End Milind 04-10-2006
							
							sql = "SELECT MAX(LINE_NO ) FROM PROF_WSHEET_MSTDET WHERE HIER_KEY = '"+columnValue +"'";  
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
							//changed by msalam on 220708 as name changed in data window 
							//valueXmlString.append("<prof_wsheet_mstseq_wsheet_code>").append("").append("</prof_wsheet_mstseq_wsheet_code>");
							//valueXmlString.append("<prof_wsheet_mstseq_acct_code>").append("").append("</prof_wsheet_mstseq_acct_code>");
							valueXmlString.append("<wsheet_code>").append("").append("</wsheet_code>");
							valueXmlString.append("<acct_code>").append("").append("</acct_code>");
							//changed by msalam on 220708 as name changed in data window  end 
							valueXmlString.append("<prof_wsheet_mstseq_line_no>").append("").append("</prof_wsheet_mstseq_line_no>");
							valueXmlString.append("<prof_wsheet_mstseq_hier_ref>").append("").append("</prof_wsheet_mstseq_hier_ref>");
							//Milind 04-10-2006
							valueXmlString.append("<cc_hier_ref_descr>").append("").append("</cc_hier_ref_descr>");
							valueXmlString.append("<prof_wsheet_mstseq_hier_ref_1>").append("").append("</prof_wsheet_mstseq_hier_ref_1>");
							valueXmlString.append("<cc_hier_ref_1_descr>").append("").append("</cc_hier_ref_1_descr>");
							valueXmlString.append("<prof_wsheet_mstseq_hier_ref_2>").append("").append("</prof_wsheet_mstseq_hier_ref_2>");
							valueXmlString.append("<cc_hier_ref_2_descr>").append("").append("</cc_hier_ref_2_descr>");
							valueXmlString.append("<prof_wsheet_mstseq_hier_ref_3>").append("").append("</prof_wsheet_mstseq_hier_ref_3>");
							valueXmlString.append("<cc_hier_ref_3_descr>").append("").append("</cc_hier_ref_3_descr>");

							valueXmlString.append("<wsheet_descr>").append("").append("</wsheet_descr>");
							valueXmlString.append("<accounts_descr>").append("").append("</accounts_descr>");
							
						}
						stmt.close();
						stmt=null;
					}
					else if(currentColumn.trim().equals("hier_value"))
					{
						if(columnValue==null)
						{
							valueXmlString.append("<cc_hier_value_descr>").append("").append("</cc_hier_value_descr>");
						}
						else
						{ 
							columnValue1 = genericUtility.getColumnValue("prof_wsheet_mstseq_hier_ref",dom);
							//Milind 03-10-2006
							//changed by msalam on 220708 for error in setting value
							//pstmt = connectionObject.prepareStatement("SELECT FN_GETHRVALUEDESCR(?,?) FROM DUAL");
							CallableStatement cs2 = connectionObject.prepareCall("{? = call FN_GETHRVALUEDESCR(?,?,?)}");
							cs2.registerOutParameter( 1, java.sql.Types.VARCHAR );
							cs2.setString( 1, columnValue1 );
							cs2.setString( 2, columnValue );
							
							cs2.execute();
							returnValue = cs2.getString(1);
							//pstmt.setString( 1, columnValue1 );
							//pstmt.setString( 2, columnValue );
							//rsFunct = pstmt.executeQuery( );
							//if( rsFunct.next())
							//{
							//	returnValue = rsFunct.getString(1);
							//}
							//rsFunct.close();
							//rsFunct = null;
							//end by msalam on 220708
							//pstmt.close();
							//pstmt = null;
							
							returnValue = ( returnValue != null )? returnValue : "";
							if( returnValue.indexOf("NotExist")!= -1)
							{								 
								valueXmlString.append("<cc_hier_value_descr>").append(" Invalid Hier Value ").append("</cc_hier_value_descr>");
							}
							else
							{                              
								valueXmlString.append("<cc_hier_value_descr>").append(returnValue).append("</cc_hier_value_descr>");
							}
						}
					}
					else if(currentColumn.trim().equals("hier_value_1"))
					{
						if(columnValue==null)
						{
							valueXmlString.append("<cc_hier_value_1_descr>").append("").append("</cc_hier_value_1_descr>");
						}
						else
						{ 
							columnValue1 = genericUtility.getColumnValue("prof_wsheet_mstseq_hier_ref_1",dom);
							//Milind 03-10-2006
							pstmt = connectionObject.prepareStatement("SELECT FN_GETHRVALUEDESCR(?,?) FROM DUAL");
							pstmt.setString(1, columnValue1);
							pstmt.setString(2, columnValue);
							rsFunct = pstmt.executeQuery();

							if( rsFunct.next())
							{
								returnValue = rsFunct.getString(1);
							}
							//added by msalam on 220708 as result set was not closed
							rsFunct.close();
							rsFunct = null;
							//end by msalam on 220708
							pstmt.close();
							pstmt = null;

							returnValue = ( returnValue != null )? returnValue : "";
							if(returnValue.indexOf("NotExist")!= -1)
							{								 
								valueXmlString.append("<cc_hier_value_1_descr>").append(" Invalid Hier Value ").append("</cc_hier_value_1_descr>");
							}
							else
							{                              
								valueXmlString.append("<cc_hier_value_1_descr>").append(returnValue).append("</cc_hier_value_1_descr>");
							}							
						}
					}
					else if(currentColumn.trim().equals("hier_value_2"))
					{
						if(columnValue==null)
						{
							valueXmlString.append("<cc_hier_value_2_descr>").append("").append("</cc_hier_value_2_descr>");
						}
						else
						{ 
							columnValue1 = genericUtility.getColumnValue("prof_wsheet_mstseq_hier_ref_2",dom);
							//Milind 03-10-2006
							pstmt = connectionObject.prepareStatement("SELECT FN_GETHRVALUEDESCR(?,?) FROM DUAL");
							pstmt.setString(1, columnValue1);
							pstmt.setString(2, columnValue);
							rsFunct = pstmt.executeQuery();

							if( rsFunct.next())
							{
								returnValue = rsFunct.getString(1);
							}
							pstmt.close();
							pstmt = null;
							returnValue = ( returnValue != null )? returnValue : "";
							if(returnValue.indexOf("NotExist")!= -1)
							{								 
								valueXmlString.append("<cc_hier_value_2_descr>").append(" Invalid Hier Value ").append("</cc_hier_value_2_descr>");
							}
							else
							{                              
								valueXmlString.append("<cc_hier_value_2_descr>").append(returnValue).append("</cc_hier_value_2_descr>");
							}
						}
					}
					else if(currentColumn.trim().equals("hier_value_3"))
					{
						if(columnValue==null)
						{
							valueXmlString.append("<cc_hier_value_3_descr>").append("").append("</cc_hier_value_3_descr>");
						}
						else
						{ 
							columnValue1 = genericUtility.getColumnValue("prof_wsheet_mstseq_hier_ref_3",dom);
							//Milind 03-10-2006
							pstmt = connectionObject.prepareStatement("SELECT FN_GETHRVALUEDESCR(?,?) FROM DUAL");
							pstmt.setString(1, columnValue1);
							pstmt.setString(2, columnValue);
							rsFunct = pstmt.executeQuery();

							if( rsFunct.next())
							{
								returnValue = rsFunct.getString(1);
							}
							//added by msalam on 220708 as result set was not closed
							rsFunct.close();
							rsFunct = null;
							//end by msalam on 220708
							
							pstmt.close();
							pstmt = null;
							returnValue = ( returnValue != null )? returnValue : "";
							if(returnValue.indexOf("NotExist")!= -1)
							{								 
								valueXmlString.append("<cc_hier_value_3_descr>").append(" Invalid Hier Value ").append("</cc_hier_value_3_descr>");
							}
							else
							{                              
								valueXmlString.append("<cc_hier_value_3_descr>").append(returnValue).append("</cc_hier_value_3_descr>");
							}
						}
					}
					//added by msalam on 220708 for ic on 2 fields
					else if(currentColumn.trim().equals("acct_code"))
					{
						if(columnValue==null)
						{
							valueXmlString.append("<prof_accounts_descr>").append("").append("</prof_accounts_descr>");
						}
						else
						{ 
							columnValue1 = genericUtility.getColumnValue("acct_code",dom);
							//Milind 03-10-2006
							String descrSql = null;
							descrSql = "select 	DESCR from  prof_accounts where  ACCT_CODE = '" + columnValue1.trim() + "'";
							pstmt = connectionObject.prepareStatement( descrSql );
							rsFunct = pstmt.executeQuery();

							if( rsFunct.next())
							{
								returnValue = rsFunct.getString(1);
							}
							rsFunct.close();
							rsFunct = null;							
							pstmt.close();
							pstmt = null;
							returnValue = ( returnValue != null )? returnValue : "";
							if(returnValue.indexOf("NotExist")!= -1)
							{								 
								valueXmlString.append("<prof_accounts_descr>").append(" Invalid Account Value ").append("</prof_accounts_descr>");
							}
							else
							{                              
								valueXmlString.append("<prof_accounts_descr>").append(returnValue).append("</prof_accounts_descr>");
							}
						}
					}					
					else if(currentColumn.trim().equals("wsheet_code"))
					{
						if(columnValue==null)
						{
							valueXmlString.append("<prof_accounts_descr>").append("").append("</prof_accounts_descr>");
						}
						else
						{ 
							columnValue1 = genericUtility.getColumnValue("wsheet_code",dom);
							//Milind 03-10-2006
							String descSql = null;
							descSql = "select 	DESCR from  prof_wsheet where  WSHEET_CODE = '" + columnValue1.trim() + "'";
							pstmt = connectionObject.prepareStatement( descSql );
							rsFunct = pstmt.executeQuery();

							if( rsFunct.next())
							{
								returnValue = rsFunct.getString(1);
							}
							rsFunct.close();
							rsFunct = null;							
							pstmt.close();
							pstmt = null;
							returnValue = ( returnValue != null )? returnValue : "";
							if(returnValue.indexOf("NotExist")!= -1)
							{								 
								valueXmlString.append("<prof_wsheet_descr>").append(" Invalid WSheet Value ").append("</prof_wsheet_descr>");
							}
							else
							{                              
								valueXmlString.append("<prof_wsheet_descr>").append(returnValue).append("</prof_wsheet_descr>");
							}
						}
					}					
					//end added by msalam on 220708

					valueXmlString.append("</Detail1>");
				break;
			}
			valueXmlString.append("</Root>");	
		}
		catch(Exception e)
		{
			BaseLogger.error("Exception :ProfMstdetEJB :itemChanged(Document,String):" + e + ":");
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
					if(pstmt  != null)
					{
						pstmt.close();
						pstmt = null;
					}
					connectionObject.close();
					connectionObject = null;
				}
		    }
			catch(Exception e)
			{
				BaseLogger.error("Exception :ProfMstDetEJB :getItemSer :==>"+e);
				valueXmlString.delete(0,valueXmlString.length());			
				valueXmlString = valueXmlString.append(genericUtility.createErrorString(e));
				throw new ITMException(e);
			}
        }		
		BaseLogger.info("valueXmlString.length():"+valueXmlString.length());
		BaseLogger.debug("valueXmlString :"+valueXmlString.toString());
		return valueXmlString.toString();
	}
	//Milind 03-10-2006
	private String getHierValueDescription(String fieldname ,String hierRef, String hierValue , String action )throws RemoteException,ITMException
	{
		System.out.println("hierRef :"+hierRef + "  hierValue : "+ hierValue + " action : "+ action);

		Connection connectionObject = null;
		Statement stmt = null;		
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		//ITMDBAccessHome itmDBAccessHome = null; // for ejb3
		ITMDBAccessEJB itmDBAccess = null;
		String sql=null;

		String tableName = null;
		String whereColumns = null;
		String dispColumns =null;
		String hierValueDescr = null;

		try
		{
			itmDBAccess = new ITMDBAccessEJB();
			//itmDBAccess = itmDBAccessHome.create();			
			//sql = "SELECT UDF_STR1,UDF_STR2,UDF_STR3  FROM GENCODES WHERE FLD_NAME ='HIER_REF' AND FLD_VALUE = '"+hierRef+"'";
			sql = "SELECT UDF_STR1,UDF_STR2,UDF_STR3  FROM GENCODES WHERE FLD_NAME ='"+ fieldname +"' AND FLD_VALUE = '"+hierRef+"'";
			//BaseLogger.debug(" getHierValueDescription : Sql :==> \n " +sql );
			System.out.println(" getHierValueDescription : Sql :==> \n " +sql );
			connectionObject = itmDBAccess.getConnection();
			stmt = connectionObject.createStatement();
			rs = stmt.executeQuery(sql);
			if(rs.next())
			{
				tableName = rs.getString(1);
				whereColumns = rs.getString(2);
				dispColumns = rs.getString(3);
			}
			else
			{
				return "ERROR" ;
			}
			
			stmt.close();
			stmt=null; 
			
			sql = "SELECT "+ dispColumns +" FROM "+ tableName +" WHERE "+whereColumns +" = '" + hierValue + "'" ;
			BaseLogger.debug("getHierValueDescription : Sql :==>\n " +sql );
			stmt = connectionObject.createStatement();
			rs = stmt.executeQuery(sql);
			rsmd = rs.getMetaData();
			if(rs.next())
			{
				hierValueDescr = rs.getString(1); 			
			}
			else
			{
				return "ERROR" ;
			}			
			stmt.close();
			stmt=null; 
		}
		catch(Exception e)
		{
			BaseLogger.error("Exception :ProfMstDetEJB :getItemSer :==>"+e);
			return "ERROR" ;
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
				BaseLogger.error("Exception :ProfMstDetEJB :getHierValueDescription :==>"+e);
				throw new ITMException(e);
			}		
		}
		if (action.equals("V"))
		{
			return "OK";
		}
		return hierValueDescr;
	}
}
