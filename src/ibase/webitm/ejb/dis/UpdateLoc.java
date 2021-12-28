
/********************************************************
	Title : 
	Date  22 sept 08
	Author: Mukesh

********************************************************/

package ibase.webitm.ejb.dis;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import javax.ejb.*;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import javax.naming.InitialContext;

import ibase.webitm.ejb.*;
import ibase.webitm.utility.*;
import ibase.system.config.*;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.TransIDGenerator;
import ibase.webitm.ejb.ProcessEJB;
import org.xml.sax.InputSource;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class UpdateLoc extends ProcessEJB implements UpdateLocLocal, UpdateLocRemote
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	CommonConstants commonConstants = new CommonConstants();
	ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
	Connection conn = null;
	String profileId = null;
	String processStr = null;
	

	/*public void Create() throws RemoteException, CreateException
	{
          
	}
	public void Remove()
	{
	    
	}
	public void Activate()
	{
	   
	}
	public void Passivate()
	{
	   
	
	}*/
	public String process() throws RemoteException,ITMException
	{
		return "";
	}
	
	//public String getData(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException
	//public String getData(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	//  end of getData()

	
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
			}
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				detailDom = genericUtility.parseString(xmlString2);
			}
		    retStr = process(headerDom, detailDom, windowName, xtraParams);
		}
		catch (Exception e)
		{
			System.out.println("Exception :UserRights :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return retStr;
	}// end of process
	public String process(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		processStr = null;
		ConnDriver connDriver = new ConnDriver();
		Connection conn = null;
		PreparedStatement pUpdateStmt = null;
		Statement stmt = null;
		int updatedRows=0;
		String returnString = "";
		String profileId = null;
		String application = null;
		
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = "";
        boolean noError= true;
		
		int parentNodeListLength = 0;
		int childNodeListLength = 0;
		String delSql = "",errCode="",errString="";
		String actualIssue="",workOrder="";
		String locationCodeFromUser=null;
		String tranId="",lineNo="";
		Statement selctStmnt= null,stmtValidate= null;
		ResultSet rs = null;
		String sqlSelectQuery=null,sqlSelectConfQuery=null;
		String sqlValidate=null;
		String pUpdateSql = null;
		String searchKey = null,confirmedStr=null;
		int operation = 0;
		int cnt = 0;
		ResultSet rsValidate=null;
	
		try
		{
			
			//GenericUtility genericUtility = GenericUtility.getInstance();
			ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
			
			if(conn == null)
			{
				connDriver = new ConnDriver();
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
				conn.setAutoCommit(false);
				connDriver = null;
			}
			
			//printallTags(headerDom);
			parentNodeList = detailDom.getElementsByTagName("Detail1");
			parentNodeListLength = parentNodeList.getLength();
			
			String userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			
			for (int selectedRow = 0; selectedRow < parentNodeListLength; selectedRow++)
			{ 
				operation = 0;
				parentNode = parentNodeList.item(selectedRow);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				
				for (int childRow = 0; childRow < childNodeListLength; childRow++)
				{
					childNode = childNodeList.item(childRow);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equalsIgnoreCase("tran_id"))
					{
						
						if (childNode.getFirstChild() != null) // ****************
						{
							tranId = (childNode.getFirstChild().getNodeValue());
							
						}

					}//end of tran id
					else if (childNodeName.equalsIgnoreCase("loc_code"))
					{
						if (childNode.getFirstChild() != null) // ****************
						{
							locationCodeFromUser = (childNode.getFirstChild().getNodeValue()).trim();
							if(locationCodeFromUser!=null)
							{
								sqlValidate = "select count(1)  from location where  loc_code ='"+locationCodeFromUser+"'";
								stmtValidate =conn.createStatement();
								rsValidate = stmtValidate.executeQuery(sqlValidate);
								if(rsValidate.next())
								{
									cnt = rsValidate.getInt(1);
								}
								stmtValidate.close();
								rsValidate.close();
								stmtValidate = null;
								rsValidate = null;
								
								if (cnt == 0)
								{
									errCode = "VLLOCCODE";
									noError=false;
									returnString = itmDBAccess.getErrorString("",errCode,"","",conn);
									
									break;
								}// end if (cnt > 0)
							}//
						
						}

					}//end of loc code 
					
				}//inner for
			
			}//outer for 
			
			sqlSelectConfQuery ="select confirmed  from distord_rcp "+
			"  where tran_id = '"+tranId+"'";
			selctStmnt =	conn.createStatement();
			rs = selctStmnt.executeQuery(sqlSelectConfQuery);
			if(rs.next())
			{
			   confirmedStr = rs.getString("confirmed");
			  
			}
			rs.close();
			rs=null;
			if(confirmedStr !=null &&  "Y".equalsIgnoreCase( confirmedStr.trim()))
			{
			 
    		  noError=false;
			  errCode = "VMCONFCOD";
			  returnString = itmDBAccess.getErrorString("",errCode,"","",conn);
			  
			}
			
				
			if(noError )
			{
				pUpdateSql = 
				" UPDATE distord_rcpdet SET  " +
				" loc_code = ? 	"  +
				" where tran_id='"+tranId+"' ";
				
				pUpdateStmt = conn.prepareStatement(pUpdateSql);
                pUpdateStmt.setString(1,locationCodeFromUser);
				//  pUpdateStmt= conn.prepareStatement(batchUpdateSql);
				updatedRows= pUpdateStmt.executeUpdate();// returns no of updates rows 
				
				if(updatedRows == 0)
				{
					noError=false; //dont remove this .ejb.a flag to check while giving error message to user
					errCode = "VMNODET";
				    returnString = itmDBAccess.getErrorString("",errCode,"","",conn);
					  
				}
				else
				{
				    conn.commit();
					System.out.println("After Commit");
				}
				
		    }
		}
		catch(BatchUpdateException be)
		{
			be.printStackTrace();
			returnString = be.getMessage();
			System.out.println("BatchUpdateException :: " + returnString);
			System.out.println("Detail Exception : " + be.getNextException());
		}
		catch (SQLException ex)
		{
		    	
			returnString = ex.getMessage();
			ex.printStackTrace();
			try 
			{
			     conn.rollback();
			}
			catch(Exception e)
			{		
			}
			ex.printStackTrace();

		}
		catch (Exception e)
		{
			returnString = e.getMessage();
			try
			{
				conn.rollback();
			}catch(Exception ec){}

			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(conn!=null)
				{
				 conn.close();
				 conn = null;
				}
				if(selctStmnt!=null )
				{
				 selctStmnt.close();
				 selctStmnt = null;
				}
				if( pUpdateStmt!=null)
				{
				 pUpdateStmt.close();
				 pUpdateStmt = null;
				}
				if(rsValidate!=null)
				{
				  rsValidate.close();
				  rsValidate=null;
				}
				if(rs!=null)
				{
				  rs.close();
				  rs = null;
				}
                
			}catch(Exception e)
			{
			    e.printStackTrace();
			}
			//return returnString;
		}

		if (returnString == null || returnString.trim().length() == 0)
		{
			
			returnString="VTCOMPL";
			returnString = itmDBAccess.getErrorString("",returnString,"","",conn);
			return returnString;
		}
		else
		{
			if(noError)
			{
				
				returnString="VTPRCERR";
				returnString = itmDBAccess.getErrorString("",returnString,"","",conn);
				return returnString;
			
			}
			else
			{
			   
				return returnString;
			
			}
		
		}

	}
	
	
	///the item change starts here
	
	//public void printallTags(Document headerDom )
	
	
}//class




