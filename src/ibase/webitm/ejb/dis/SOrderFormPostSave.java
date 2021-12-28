
/********************************************************
	Title : SOrderFormPostsaveEJB
	Date  : 28/08/08
	Author: pankaj singh

********************************************************/

package ibase.webitm.ejb.dis;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.util.Date;
import javax.ejb.CreateException;
//import javax.ejb.SessionBean;
import java.text.SimpleDateFormat;
import java.sql.Timestamp;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.ejb.Stateless; // added for ejb3


//public class SOrderFormPostSaveEJB extends Validator implements SessionBean
@Stateless // added for ejb3
public class SOrderFormPostSave extends ValidatorEJB implements SOrderFormPostSaveLocal, SOrderFormPostSaveRemote
{
	E12GenericUtility genericUtility= new  E12GenericUtility();
	/*public void ejbCreate() throws RemoteException, CreateException
	{
		System.out.println("<======= SOrderFormPostSave DISPLAY IS IN PROCESS ! \n Welcome!========>");
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
	
	public String postSave() throws RemoteException,ITMException
	{
		return "";
	}
	public String postSave(String winName,String editFlag,Document dom,  String xtraParams,Connection conn) throws RemoteException,ITMException
	{
		return "";
	}
	public String postSave(String winName,String editFlag,String tranID,  String xtraParams,Connection conn) throws RemoteException,ITMException
	{
		String updateFlag = "",sql = "", errorString = "" ;
		PreparedStatement pstmt = null;
		int opInt = 0;
		int opDomInt = 0,mapCnt = 0;
		String issueType = "";
		try
		{
			sql = "update sordform f "
					+ " set f.tot_value = (select sum(ORD_VALUE) from sordformdet d where d.tran_id = f.tran_id) "
					+ " where f.tran_id = ? ";
			System.out.println("SOrderFormPostSavesql [" +sql + "] tran id [" + tranID + "]");
			pstmt= conn.prepareStatement(sql);
			pstmt.setString(1,tranID);
			pstmt.executeUpdate();
			pstmt.close();
			pstmt = null;		
		}
		catch(Exception e)
		{
			e.printStackTrace();
			errorString = e.getMessage();
			throw new ITMException( e );
		}
		finally
		{
			try
			{
				if(pstmt!=null)
				{
					if(pstmt != null)pstmt.close();
					pstmt = null;
				}
			}catch(Exception d)
			{
				d.printStackTrace();
				errorString = d.getMessage();
				throw new ITMException( d );
			  
			}
		}
		return errorString;
		
	}
 }// END OF MAIN CLASS