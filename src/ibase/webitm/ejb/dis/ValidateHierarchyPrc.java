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
public class ValidateHierarchyPrc extends ProcessEJB implements ValidateHierarchyPrcLocal,ValidateHierarchyPrcRemote
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	CommonConstants commonConstants = new CommonConstants();
	ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
	
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
		System.out.println("Called Empty process");
		return "";
	}
	
	//process()
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		String retStr = "";
		Document detailDom = null;
		Document headerDom = null;
		System.out.println("xmlString2-->"+ xmlString2);
		//GenericUtility genericUtility = GenericUtility.getInstance();
		try
		{	
			System.out.println("xmlString   ["+xmlString+"]");
			System.out.println("xmlString2   ["+xmlString2+"]");
			System.out.println("windowName   ["+windowName+"]");
			System.out.println("xtraParams   ["+xtraParams+"]");			
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
			System.out.println("Exception :Hierarchy :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			e.printStackTrace();			
			throw new ITMException(e);
		}
		return retStr;
	}
	public String process(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{
			//GenericUtility genericUtility = GenericUtility.getInstance();
			ConnDriver connDriver = null;
			Connection conn = null;
		
			PreparedStatement stmt = null;
			PreparedStatement pInStmt1 = null;
			PreparedStatement pInUpdStmt = null;
			ResultSet rs = null;
			ResultSet loopRs = null;
			
			String errString = null;	
			String returnString = "";
	
			//Variable decleration for process
			int cntr = 0, llLevelNo = -1, lCnt = 0, llCnt = 0;
			String lsErr = null,lsLevelCodeParent = null, lsVersionId = null;
			String sqlStr = null;
			
			String versionId = null;
			String levelNoStr = null;
			int levelNo = - 1;
				
			lsErr = "";
					
			versionId = genericUtility.getColumnValue("version_id", headerDom);
			levelNoStr = genericUtility.getColumnValue("level_no", headerDom);
			
			try{
				if(!(levelNoStr == null || levelNoStr.equals("")))
					levelNo = Integer.parseInt(levelNoStr);
			}catch(Exception e){
				lsErr = "Parse Exception";
				System.out.println("Level No parse exception :: " + e.getMessage());			
			}
			
			if(!(lsErr.equals("")))
				return lsErr;
			
			System.out.println("Version Id.ejb..ejb..ejb..ejb..ejb..ejb..ejb.." + versionId);
			System.out.println("Level No.ejb..ejb..ejb..ejb..ejb..ejb..ejb.." + levelNo);
	
			boolean flag = false;
			try
			{
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
				///////////////////						
									
				sqlStr = "update hierarchy set 	" +
						" level_no = 99999 " +
						" where	version_id = '" + versionId + "'";
					
				stmt = conn.prepareStatement(sqlStr);
				
				System.out.println("Update SQL 1 :: " + sqlStr);
				
				int updCount = 0;
				
				updCount = stmt.executeUpdate(sqlStr);
							
				System.out.println("Update Count at 154 :: " + updCount);
					
				sqlStr = "update hierarchy " +
						" set level_no = 0 " +
						" where level_code__parent = 'END' and " +
						" version_id = '" + versionId + "'";
				
				System.out.println("Update SQL 2 :: " + sqlStr);

				updCount = stmt.executeUpdate(sqlStr);
				
				System.out.println("Update Count at 154 :: " + updCount);
						
				cntr = 0;
				
				while(cntr <= levelNo)//Start of outer loop
				{			
					sqlStr = " select " +
							 " level_code, version_id " + 
							 " from hierarchy " +
							 " where level_no = " + cntr + " and " +
							 " version_id = '" + versionId + "'"; 
					
					System.out.println("Sql Str outer loop iteration : " + cntr + " :  " + sqlStr);
					
					stmt = conn.prepareStatement(sqlStr);
					rs = stmt.executeQuery();
					
					if(rs == null){
						lsErr = "DS0003";
						System.out.println("Error Code : " + lsErr);
						return lsErr;
					}
					
					
				
					cntr++;	
					if(cntr > levelNo){
						//lsErr = "DS0004";
						System.out.println("Counter > LevelNo");
						flag = true;
						//return lsErr;
					}	
				
					while(rs.next() && flag == false)//Start of inner loop
					{
						lsLevelCodeParent = rs.getString(1);
						lsVersionId = rs.getString(2);
						
						System.out.println("lsLevelCodeParent at 196 :: [" + lsLevelCodeParent + "]");
						System.out.println("lsVersionId at 197 :: [" + lsVersionId + "]");
						
						sqlStr = "	select count(*) " +
								  " from hierarchy  " +
								  " where level_code__parent = '" + lsLevelCodeParent + "' " + 
								  " and version_id = '" + lsVersionId + "' " + 
								  " and level_no <> 99999 and parent_credit_perc = 100 ";
						
						System.out.println("Sql Str inner loop iteration : " + cntr + " :  " + sqlStr);
						pInStmt1 = conn.prepareStatement(sqlStr);
						loopRs = pInStmt1.executeQuery();
						
						if(loopRs.next())
							llCnt = loopRs.getInt(1);
						
						loopRs.close();
						loopRs = null;
						
						pInStmt1.close();
						pInStmt1 = null;
						
						if(llCnt > 0){
							System.out.println("Level  >> " + lsLevelCodeParent + " and Version id " + lsVersionId + " has childs which is already referred by another parent");
							lsErr = "VTHLNK";
							return lsErr;
						} 		
						
						sqlStr = " update hierarchy set " +
								 "	level_no = " + cntr +
								 " where level_code__parent = '" + lsLevelCodeParent + "' " + 
								 " 	and version_id = '" + lsVersionId + "'";

						System.out.println("Update inner loop iteration : " + cntr + " :  " + sqlStr);
						
						int updLoopCount = 0;
						pInUpdStmt = conn.prepareStatement(sqlStr);
						updLoopCount = pInUpdStmt.executeUpdate();
						
						pInUpdStmt.close();
						pInUpdStmt = null;
						System.out.println("Update Loop count = " + updLoopCount);
						
					}//End of inner loop
					
					rs.close();
					rs = null;
					
					stmt.close();
					stmt = null;
				}//End of outer loop	
													
				sqlStr = " update hierarchy set " +
						 "	level_no = 99999 " +
						 " where level_code in 	" +
						 " ( select level_code from hierarchy where version_id = '" + versionId + "'" +
						 " group by level_code " +
						 " having sum(nvl(parent_credit_perc, 100)) <> 100 ) ";

				int updCnt = 0;
				
				PreparedStatement pOUpdStmt = null;
				pOUpdStmt = conn.prepareStatement(sqlStr);
				updCnt = pOUpdStmt.executeUpdate(sqlStr);
				System.out.println("Out of loop update count : " + updCnt + " :");

				pOUpdStmt.close();
				pOUpdStmt = null;
				
				sqlStr = " select count(*) from hierarchy where level_no = 99999 and version_id = '" + versionId + "'";
				pOUpdStmt = conn.prepareStatement(sqlStr);
				rs = pOUpdStmt.executeQuery(sqlStr);
				if(rs.next()){
					lCnt = rs.getInt(1); 
					System.out.println("LCnt at 258 :: " + lCnt);
				}
				
				rs.close();
				rs = null;
				pOUpdStmt.close();
				pOUpdStmt = null;

				String tempErr = null;
				tempErr = "";
				if(lCnt > 0){
					tempErr = "VTPERC";
					System.out.println("LCnt > 0 ");
				}
				//////////////////							
				
				errString = lsErr;
				System.out.println("errString ::" + errString);
				if(errString.equals(""))
				{				
					errString="VHPRCCOMP";
					System.out.println("errString ::" + errString);
					errString = itmDBAccess.getErrorString("", errString, "", "",conn);
					System.out.println("errString ::::["+errString+"]");
				} 

				returnString = errString;
			}catch(SQLException ex){
				try{
					conn.rollback();
				}catch(Exception ce){}
				errString="VHPRCUCOMP1";
				errString = itmDBAccess.getErrorString("", errString, "", "",conn);
				System.out.println("errString ::::["+errString+"]");
				ex.printStackTrace();
				System.out.println("Exception in Process :: " + ex.getMessage());
				throw new ITMException(ex); //Added By Mukesh Chauhan on 06/08/19
			}catch(Exception e){
				try{
					conn.rollback();
				}catch(Exception ce){}
				errString="VHPRCUCOMP2";
				errString = itmDBAccess.getErrorString("", errString, "", "",conn);
				System.out.println("errString ::::["+errString+"]");
				e.printStackTrace();
				System.out.println("Exception in Process1 :: " + e.getMessage());
				throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
			}finally{
				try{
					if(lsErr == null || lsErr.equals("")){
						conn.commit();
						errString="VHPRCCOMP";
						System.out.println("errString ::" + errString);
						errString = itmDBAccess.getErrorString("", errString, "", "",conn);
						System.out.println("errString ::::["+errString+"]");

						System.out.println("Commit Transaction !!!!!!!!!!!!!!!!!!!!!!!");
					}
					else{ 
						conn.rollback();
						errString = itmDBAccess.getErrorString("", errString, "", "",conn);
						System.out.println("errString ::::["+errString+"]");

						System.out.println("Rollback Transaction !!!!!!!!!!!!!!!!!!!!!!!");
					}
					if(rs != null)
						rs.close();
					if(loopRs != null)
						loopRs.close();
					if(stmt != null)	
						stmt.close();	
					if(conn != null)
						conn.close();
				}catch(Exception ce){}
				rs = null;
				loopRs = null;
				stmt = null;
				conn = null;
			}
		
		returnString = errString;
		System.out.println("errString ::" + errString);
		System.out.println("Error String :: " + returnString);					
		return returnString;
	}//process()
}//class




