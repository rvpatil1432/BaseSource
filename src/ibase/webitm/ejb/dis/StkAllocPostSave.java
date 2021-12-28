/***************
 * VALLABH KADAM 
 * StkAllocPostSave 
 * request id [D14JSUN005]
 * 19/JAN/15
 * ********************/
package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.util.*;
import java.util.Date;
import java.text.DateFormat;
import java.sql.*;

import javax.ejb.*;
import javax.naming.InitialContext;

import org.w3c.dom.*;

import javax.xml.parsers.*;

import java.text.SimpleDateFormat;

import ibase.webitm.ejb.*;
import ibase.webitm.utility.*;
import ibase.system.config.*;
import ibase.webitm.utility.ITMException;
//import ibase.webitm.utility.GenericUtility;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;


import ibase.webitm.utility.TransIDGenerator;

import javax.ejb.Stateless; // added for ejb3
@Stateless // added for ejb3

public class StkAllocPostSave extends ValidatorEJB implements StkAllocPostSaveLocal,StkAllocPostSaveRemote{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	
	public String postSave()throws RemoteException,ITMException
	{
		return "";
	}
		
public String postSave( String domString, String editFlag, String xtraParams, Connection conn ) throws RemoteException,ITMException
	
	{
		String retString = "";
		boolean isLocalConn = false;
	
	PreparedStatement pstmt = null;
	Document dom = null;                
	ResultSet rs =null;
	boolean isError = false;
	int cnt=0;

	String tranId = "";
	String sql = null;
	String batchId="",siteCode="",keyString="",keyCol="",tranSer1="";
	CommonConstants commonConstants = new CommonConstants();
	
		try
		{
			//GenericUtility genericUtility = GenericUtility.getInstance();
			dom = genericUtility.parseString(domString);

			tranId = genericUtility.getColumnValue("tran_id",dom);
			System.out.println("Tran id :- ["+tranId+"]");
			
			/**
			 * IF BATCH_ID from SORD_ALLOC
			 * is null for selected tranId
			 * Generate new tranId  
			 * */
			
			sql="select batch_id from sord_alloc where tran_id=?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString( 1, tranId);
			rs = pstmt.executeQuery();			
			while( rs.next())
			{
				batchId=rs.getString("batch_id");
			}
			rs.close();
			rs=null;
			pstmt.close();
			pstmt =null;
			
			if(batchId==null || batchId.trim().length()<=0)
			{
				System.out.println("********************Batch id found null Generating Batch id now ************************");
				//Generate new BATCH_ID here 
				sql = "SELECT KEY_STRING, TRAN_ID_COL, REF_SER FROM TRANSETUP WHERE lower(TRAN_WINDOW) = 'w_sordalloc'";
				System.out.println("keyStringQuery--------->>"+sql);
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				if(rs.next())
				{ 
					keyString = rs.getString(1);
					keyCol = rs.getString(2);
					tranSer1 = rs.getString(3);				
				}
				rs.close();
				rs=null;
				pstmt.close();
				pstmt =null;

				siteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
				String xmlValues = "";
				String tranDateStr = getCurrdateAppFormat();
				xmlValues ="<?xml version=\"1.0\" encoding=\"utf-8\"?><Root>";
				xmlValues = xmlValues + "<Header></Header>";
				xmlValues = xmlValues + "<Detail1>";
				xmlValues = xmlValues +	"<tran_id></tran_id>";
				xmlValues = xmlValues + "<site_code>" + siteCode + "</site_code>";	
				xmlValues = xmlValues + "<tran_date>" + tranDateStr + "</tran_date>"; 
				xmlValues = xmlValues +"</Detail1></Root>";
				System.out.println("xmlValues  :["+xmlValues+"]");
				TransIDGenerator tg = new TransIDGenerator(xmlValues, "BASE", commonConstants.DB_NAME);
				batchId = tg.generateTranSeqID(tranSer1, keyCol, keyString, conn);
				System.out.println("@@@@ generated BATCH ID ******** :["+batchId+"]");
				
				
				// UPDATE auto generated BATCH_ID for tranId.
				sql="update sord_alloc set batch_id=? where tran_id=?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,batchId.trim());
				pstmt.setString(2,tranId );
				cnt = pstmt.executeUpdate();
				pstmt.close();
				pstmt = null;
				
				System.out.println("Update count of BATCH_ID :- "+cnt);
				// UPDATE END
			}
	}
		catch(Exception e)
		{
			System.out.println("Exception  :==>\n"+e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("isError ["+isError+"]");
				if( conn != null )
				{

					if( isError )
					{
						conn.rollback();
					}
					if(pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}
					if ( isLocalConn )
					{
						if ( ! isError )
						{
							conn.commit();
						}
						conn.close();
						conn = null;
					}
					
				}
			}
			catch(Exception e)
			{
				System.out.println("Exception :==>\n"+e.getMessage());
				throw new ITMException(e);
			}
		}
		System.out.println("Return string :"+retString);
		return retString;
	}
private String getCurrdateAppFormat() throws ITMException
{
	String s = "";
	//GenericUtility genericUtility = GenericUtility.getInstance();
	try
	{
		java.util.Date date = null;
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		System.out.println(genericUtility.getDBDateFormat());
		SimpleDateFormat simpledateformat = new SimpleDateFormat(genericUtility.getDBDateFormat());
		date = simpledateformat.parse(timestamp.toString());
		timestamp = Timestamp.valueOf(simpledateformat.format(date).toString() + " 00:00:00.0");
		s = (new SimpleDateFormat(genericUtility.getApplDateFormat())).format(timestamp).toString();
	}
	catch(Exception exception)
	{
		System.out.println("Exception in  getCurrdateAppFormat " + exception.getMessage());
		throw new ITMException(exception); //Added By Mukesh Chauhan on 05/08/19
	}
	return s;
}	
}

	


