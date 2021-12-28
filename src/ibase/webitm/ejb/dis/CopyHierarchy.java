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
import java.text.SimpleDateFormat;
import javax.ejb.EJBObject;

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


//public class CopyHierarchyEJB extends ProcessEJB implements SessionBean
@Stateless // added for ejb3
public class CopyHierarchy extends ProcessEJB implements CopyHierarchyLocal, CopyHierarchyRemote
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	CommonConstants commonConstants = new CommonConstants();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();

	String processStr = null;

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
	public String process() throws RemoteException,ITMException
	{
		System.out.println("Called Empty process");
		return "";
	}
	//process()
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		
		System.out.println("!!!!!!!! PROCESS IS GOING TO BE START !!!!!!!!!!");
		String retStr = "";
		Document detailDom = null;
		Document headerDom = null;
		System.out.println("xmlString2-->"+ xmlString2);
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility= new  E12GenericUtility();
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
			System.out.println("Exception :HierarchyEJB :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return retStr;
	}
	public String process(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		    System.out.println("******** PROCESS IS STARTED *********");
		    //GenericUtility genericUtility = GenericUtility.getInstance();
		    E12GenericUtility genericUtility= new  E12GenericUtility();
			ConnDriver connDriver = null;
			Connection conn = null;

			PreparedStatement stmt = null;
			PreparedStatement pInStmt1 = null;
			PreparedStatement pInStmt2 = null;
			PreparedStatement pInStmt3 = null;
			PreparedStatement pInStmt4 = null;
			//PreparedStatement pInUpdStmt = null;
			ResultSet rs = null;
			ResultSet rs1 = null;
			//ResultSet loopRs = null;
			java.sql.Timestamp chgDate = null;
			chgDate =new java.sql.Timestamp(System.currentTimeMillis()) ;
			Timestamp EffFrom = null;
			//EffFrom =new java.sql.Timestamp(System.currentTimeMillis()) ;
			//Timestamp chgDate = null;
			Timestamp validUpto = null;
			validUpto =new java.sql.Timestamp(System.currentTimeMillis()) ;
			String versidOld = null;
			String versidNew = null;

			String retString = "" ;
			String errString = "";
			String errCode = "";
			String returnString = "";
			String lsErrcode = "";

			String levelCode = "";
			String levelcodeParent	= "";
			String tableNo ="";
			String levelDescr = "";
			String stanCode = "";
			String shDescr = "";
			String stancodeHq = "";
			String salePers = "";
			String chgUser = "";
			String chgTerm = "";


			boolean flag = false;
			// coding by rajesh kumar  for copy hierarchy
			//Variable decleration for process

			int cntr = 0,cntr1 = 0, cntr2 = 0;
			int sancStrength = 0;
			long levelNo = 0;
			double parcreditPerc = 0.0,salesPercent =0.0;
			String lsErr = null;
			String sql1 = null;
			String sql2 = null;
			String sql3 = null;
			String sqlStr1 = null;
			String sqlStr2 = null;
			String sqlStr3 = null;
			String sqlStr4 = null;


			String versionId = null;
			String levelNoStr = null;
			//int levelNo = - 1;

			versidOld =	genericUtility.getColumnValue("version_id__old", headerDom); 
		    versidNew =	genericUtility.getColumnValue("version_id__new", headerDom);
			System.out.println("**********process is executing**********"  + versidOld);
			System.out.println("**********process is executing**********"  + versidNew);
			
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

				//chgDate = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"chgDate");
				chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode"); // chguser  taken from xtraparam by rajesh k
				chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId");
				System.out.println("**********chgUser**********"  + chgUser);
				System.out.println("**********chgTerm**********"  + chgTerm);
				versidOld =	genericUtility.getColumnValue("version_id__old", headerDom);
		        versidNew =	genericUtility.getColumnValue("version_id__new", headerDom);
			    System.out.println("**********process is executing**********"  + versidOld);
			    System.out.println("**********process is executing**********"  + versidNew);
			
			    if(versidOld == null || versidOld.trim().length()== 0)
				{
				   errString = itmDBAccessEJB.getErrorString("","VERSIDOLD","","",conn);
				   System.out.println("@@@@@@@@@ errString@@@@@@@@" +errString);
				   return errString;
				}
				if(versidNew == null || versidNew.trim().length()== 0)
				{
				   errString = itmDBAccessEJB.getErrorString("","VERSIDNEW","","",conn);
				   System.out.println("******** errString ********" +errString);
				   return errString;
				   
				}
				if(versidOld.equals(versidNew))
				{
				   errString = itmDBAccessEJB.getErrorString("","VERSIDEQL","","",conn);
				   System.out.println("%%%%%%%%%% errString %%%%%%%%%%%" +errString);
				   return errString;
				}
				sql1 = "select count(1) as cnt from version where version_id = '"+versidOld+"'";
				stmt = conn.prepareStatement(sql1);
				System.out.println("sql1 is *******" + sql1);
				rs = stmt.executeQuery();
				if(rs.next())
				{
					cntr = rs.getInt(1);
				    System.out.println("cntr is ********"  + cntr);
				}
				rs.close();
				rs = null;
				stmt.close();
				stmt = null;
				sql2 ="select count(1) as cnt1 from version where version_id = '"+versidNew+"'";
				stmt =conn.prepareStatement(sql2);
				System.out.println("sql2 is******"  + sql2);
				rs = stmt.executeQuery();

				if(rs.next())
				{
				    cntr1 = rs.getInt(1);
					System.out.println("cntr1 is ======>"  + cntr);
				}
				rs.close();
				rs = null;
				stmt.close();
				stmt = null;

				sql3 = " select count(1) as cnt2 from hierarchy where version_id = '"+versidNew+"'";
				stmt = conn.prepareStatement(sql3);
				System.out.println("sql3 is *********" + sql3);
				rs = stmt.executeQuery();
				if(rs.next())
				{
					cntr2 = rs.getInt(1);
					System.out.println("cntr2 is =======>"  + cntr2);
				}
				rs.close();
				rs = null;
				stmt.close();
				stmt = null;

				/*if(cntr == 0 || cntr1 == 0 )
				{
					errString = itmDBAccessEJB.getErrorString("","VMDUPLIVER","","",conn);
					return errString;
				}

	            if (cntr2 > 0)
				{
				   
					errString = itmDBAccessEJB.getErrorString("","VMDUPLIVER","","",conn);
					return errString;
				}*/

	            
				int conut = 0;
	 			sqlStr1 = "select LEVEL_CODE,LEVEL_CODE__PARENT,TABLE_NO,LEVEL_DESCR,STAN_CODE,"
	                         + "SH_DESCR,LEVEL_NO,PARENT_CREDIT_PERC,STAN_CODE__HQ,EFF_FROM,"
				             + "VALID_UPTO,SANC_STRENGTH from HIERARCHY "
			                 + "where VERSION_ID = '" +versidOld + "' and VALID_UPTO >=?" ;
				pInStmt1 = conn.prepareStatement(sqlStr1);
				pInStmt1.setTimestamp(1,validUpto);
				System.out.println("sqlStr is =========> " +sqlStr1);
				rs = pInStmt1.executeQuery();
			   
			    		   
			    sqlStr2 = " insert into hierarchy(LEVEL_CODE,LEVEL_CODE__PARENT,TABLE_NO,LEVEL_DESCR,"
					        + "STAN_CODE,CHG_DATE,CHG_USER,CHG_TERM,SH_DESCR,LEVEL_NO,PARENT_CREDIT_PERC,"
							+ "STAN_CODE__HQ,EFF_FROM,VALID_UPTO,VERSION_ID,SANC_STRENGTH)"
							+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                pInStmt2 = conn.prepareStatement(sqlStr2);
                
				System.out.println("sql 2 *************" +sqlStr2 );
				sqlStr3 = " INSERT INTO HIERARCHY_DET(LEVEL_CODE,LEVEL_CODE__PARENT,TABLE_NO,SALES_PERS,"
				            + " CHG_DATE,CHG_USER,CHG_TERM,SALES_PERCENT,EFF_FROM,VALID_UPTO,VERSION_ID)"
				            + " VALUES(?,?,?,?,?,?,?,?,?,?,?)";


				pInStmt3 = conn.prepareStatement(sqlStr3);
			    while(rs.next())
					 {
						 conut ++;
						 System.out.println("printing count " + conut);
						 levelCode = rs.getString("LEVEL_CODE");
						 levelcodeParent = rs.getString("LEVEL_CODE__PARENT");
						 tableNo = rs.getString("TABLE_NO");
						 levelDescr = rs.getString("LEVEL_DESCR");
						 stanCode = rs.getString("STAN_CODE");
						 shDescr = rs.getString("SH_DESCR");
						 levelNo = rs.getLong("LEVEL_NO");
						 parcreditPerc = rs.getDouble("PARENT_CREDIT_PERC");
						 stancodeHq = rs.getString("STAN_CODE__HQ");
						 EffFrom = rs.getTimestamp("EFF_FROM");
						 validUpto = rs.getTimestamp("VALID_UPTO");
						 //versidNew = rs.getString("VERSION_ID");
						 sancStrength = rs.getInt("SANC_STRENGTH");


						sqlStr4 = "select SALES_PERS,SALES_PERCENT,EFF_FROM,VALID_UPTO FROM HIERARCHY_DET "
			            +" WHERE LEVEL_CODE = '"+levelCode+"' AND LEVEL_CODE__PARENT = '"+levelcodeParent+"' AND "
						+" TABLE_NO = '"+tableNo+"' AND VERSION_ID = '"+versidOld+"' AND VALID_UPTO >= ?" ;

						pInStmt4 = conn.prepareStatement(sqlStr4);
					    pInStmt4.setTimestamp(1,validUpto);
						rs1 = pInStmt4.executeQuery();

					    System.out.println("LEVEL_CODE is******" + levelCode);
					    System.out.println("levelcodeParent is******" + levelcodeParent);
			   		    /*System.out.println("tableNo is******" + tableNo);
			   		    System.out.println("levelDescr is******" + levelDescr);
			   		    System.out.println("stanCode is******" + stanCode);
			   		    System.out.println("shDescr is******" + shDescr);
			   		    System.out.println("levelNo is******" + levelNo);
			   		    System.out.println("parcreditPerc is******" + parcreditPerc);
			   		    System.out.println("stancodeHq is******" + stancodeHq);
			   		    System.out.println("EffFrom is******" + EffFrom);
			   		    System.out.println("validUpto is******" + validUpto);
			   		    System.out.println("sancStrength is******" + sancStrength);
			   		    System.out.println("salePers is******" + salePers);
			   		    System.out.println("versidOld is******" + versidOld);*/
			   		    
					    while(rs1.next())
				        {
						   //levelCode = rs1.getString("LEVEL_CODE");
						   //levelcodeParent = rs1.getString("LEVEL_CODE__PARENT");
						   //tableNo = rs1.getString("TABLE_NO");
						   salePers = rs1.getString("SALES_PERS");
						   salesPercent = rs1.getDouble("SALES_PERCENT");
						   EffFrom = rs1.getTimestamp("EFF_FROM");
						   validUpto = rs1.getTimestamp("VALID_UPTO");
						   
						   System.out.println("salePers is******" + salePers);
			   		       System.out.println("versidOld is******" + versidOld);
			   		    

						   pInStmt3.setString(1,levelCode);
					       pInStmt3.setString(2,levelcodeParent);
					       pInStmt3.setString(3,tableNo);
					       pInStmt3.setString(4,salePers);
						   pInStmt3.setTimestamp(5,chgDate);
						   pInStmt3.setString(6,chgUser);
						   pInStmt3.setString(7,chgTerm);
						   pInStmt3.setDouble(8,salesPercent);
						   pInStmt3.setTimestamp(9,EffFrom);
						   pInStmt3.setTimestamp(10,validUpto);
						   pInStmt3.setString(11,versidNew);

					       //pInStmt1.executeQuery();
						   pInStmt3.addBatch();
					    }
					    rs1.close();
					    rs1 = null;


						pInStmt2.setString(1,levelCode);
						pInStmt2.setString(2,levelcodeParent);
						pInStmt2.setString(3,tableNo);
						pInStmt2.setString(4,levelDescr);
						pInStmt2.setString(5,stanCode);
						pInStmt2.setTimestamp(6,chgDate);
						pInStmt2.setString(7,chgUser);
						pInStmt2.setString(8,chgTerm);
						pInStmt2.setString(9,shDescr);
						pInStmt2.setLong(10,levelNo);
						pInStmt2.setDouble(11,parcreditPerc);
						pInStmt2.setString(12,stancodeHq);
						pInStmt2.setTimestamp(13,EffFrom);
						pInStmt2.setTimestamp(14,validUpto);
						pInStmt2.setString(15,versidNew);
						pInStmt2.setInt(16,sancStrength);
						//pInStmt1.executeQuery();
						pInStmt2.addBatch();

			        }
				 	pInStmt2.executeBatch();
					pInStmt3.executeBatch();
											
					rs.close();														 
				    rs =null;
					pInStmt1.close();
				    pInStmt1 =null;
					pInStmt2.close();
					pInStmt2 = null;
				    pInStmt3.close();
					pInStmt3 = null;
					pInStmt4.close();
					pInStmt4 = null;

				if(errString == null || errString.trim().equals("") )
			    {
					conn.commit();
					errString =	itmDBAccessEJB.getErrorString("","VTCOMPLT","","",conn);
					System.out.println("Transaction Commit!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			    }
			    else
			    {
					errString =	itmDBAccessEJB.getErrorString("","VTROLLBCK","","",conn);
					conn.rollback();
					System.out.println("Transaction RollBack!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			    }
			}    					
			catch(SQLException ex)
			{
				try
				{
					conn.rollback();
				}
				catch(Exception ce){}
				ex.printStackTrace();  
				System.out.println("Exception in Process1 :: " + ex.getMessage());
				System.out.println("versionidold  and versionidnew both are present  for same level code ");
				errString = itmDBAccessEJB.getErrorString("","VMSAMEVER","","",conn);
		     	return errString;
				
			}
			catch(Exception e)
			{
				try
				{
					 conn.rollback();
				}
				catch(Exception ce){} 
				e.printStackTrace();  
			    System.out.println("Exception in Process2 :: " + e.getMessage());
				   
			}
		    finally
		    {
				try
				{
					if (pInStmt1 != null)
					{
						pInStmt1.close();
					}
					if (pInStmt2 != null)
					{
						pInStmt2.close();
					}
					if (pInStmt3 != null)
					{
						pInStmt3.close();
					}
					if (pInStmt4 != null)
					{
						pInStmt4.close();
					}
					if(conn != null)
					{
						conn.close();
						conn = null;
					}
			  	}
				catch(Exception e)
				{
			   		System.out.println("Error In closing connection::==> "+e);
		       		e.printStackTrace();
				}
		  	}
	      return errString;
    }//END OF PROCESS
 } // END OF EJB  



















