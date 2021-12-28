/* Developed By Vallabh Kadam
 * Date : 11-JAN-2017
 * Component Name : DistRcpVerifyPasswordPrc
 */
package ibase.webitm.ejb.dis;

import ibase.system.config.AppConnectParm;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ProcessEJB;
import ibase.webitm.ejb.dis.DistOrderRcpConfLocal;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import javax.ejb.Stateless;
import javax.naming.InitialContext;

import org.w3c.dom.Document;

/**
 * Session Bean implementation class EmpMaritalStatusConfPrc
 */
@Stateless
public class DistRcpVerifyPasswordPrc extends ProcessEJB implements DistRcpVerifyPasswordPrcRemote, DistRcpVerifyPasswordPrcLocal 
{
	E12GenericUtility genericUtility=new E12GenericUtility();
	public String process() throws RemoteException,ITMException
	{
		return "";
	}
	
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException, ITMException
	{
		String retString = "";
		Document detailDom = null;
		Document headerDom = null;
		try
		{	
			if(xmlString != null && xmlString.trim().length() != 0)
			{
				headerDom = genericUtility.parseString(xmlString); 
				System.out.println("headerDom" + headerDom);
			}
			if(xmlString2 != null && xmlString2.trim().length() != 0)
			{
				detailDom = genericUtility.parseString(xmlString2); 
				System.out.println("detailDom" + detailDom);
			}
			retString = process(headerDom, detailDom, windowName, xtraParams);
		}
		catch (Exception e)
		{
			System.out.println("Exception :EmpMaritalStatusConfPrc :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return retString;
	}
	
	public String process(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException, ITMException
	{
		String returnString = "",tranId="",sql="",lsPasswd="",lsConfirm="",lsEntPasswd="";
		String lsPwdVerified="N";
		Timestamp tranDateTimestamp=null;
		boolean isError = false;
		Connection connection = null;
		ConnDriver connDriver = null;
		AppConnectParm appConnect = null;
		InitialContext initialCtx = null;
		ITMDBAccessEJB itmDBAccess = null;
		DistOrderRcpConfLocal distOrderRcpConf=null;		
		PreparedStatement pstmt=null;
		ResultSet rs=null;		
		
		try
		{
			connDriver = new ConnDriver();
			//connection = connDriver.getConnectDB("DriverITM");
			connection  = getConnection();
			connection.setAutoCommit(false);
			//pavan R 20/jul/18 changed the lookup to creating instance of the class using new keyword.
			itmDBAccess = new ITMDBAccessEJB();			
		//	tranId = checkNull(genericUtility.getColumnValue("tran_id", headerDom));
			tranId = genericUtility.getColumnValue("tran_id", detailDom);
			if(tranId==null || tranId.trim().length()<=0)
			{
				returnString = itmDBAccess.getErrorString("", "VTTRNNULL", "", "", connection);
				return returnString;
			}
			else
			{
				sql = "select conf_passwd, confirmed from distord_rcp where tran_id = ?";
				pstmt = connection.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					lsPasswd = checkNull(rs.getString("conf_passwd"));				
					lsConfirm = checkNull(rs.getString("confirmed"));				
				}
				pstmt.close();
				pstmt=null;
				rs.close();
				rs=null;
				System.out.println("@V@ VALLABH lsPasswd :- ["+lsPasswd+"]");
				System.out.println("@V@ VALLABH lsConfirm :- ["+lsConfirm+"]");
				if("Y".equalsIgnoreCase(lsConfirm))
				{
					returnString = itmDBAccess.getErrorString("", "VTMCONF1", "", "", connection);
					return returnString;				
				}
				else
				{
					if(lsPasswd!=null && lsPasswd.trim().length()>0)
					{
						lsEntPasswd=checkNull(genericUtility.getColumnValue("conf_passwd", detailDom));
						if(lsEntPasswd == null ||lsEntPasswd.trim().length()<=0 || !(lsEntPasswd.equals(lsPasswd)))
						{
							returnString = itmDBAccess.getErrorString("", "VTPASS1", "", "", connection);
							return returnString;
						}
						else
						{
							lsPwdVerified="Y";
						}
					}
					else
					{
						lsPwdVerified="Y";
					}
					if("Y".equalsIgnoreCase(lsPwdVerified))
					{
//						returnString = distOrderRcpConf.confirm(tranId, xtraParams, "", connection);
						System.out.println("@V@ connection ["+connection);
						connection.commit();
						System.out.println("@V@ Commiting befor DistOrderRcpConf 144--->"+connection);
						DistOrderRcpConf distOrderRcpConfObj= new DistOrderRcpConf();
						returnString = distOrderRcpConfObj.actionConfirm(tranId, xtraParams, "", "Y", connection);
						System.out.println("distOrderRcpConfObj actionConfirm sucess ::[" + returnString + "]");

					}
				}
			}// TRAN_ID NOT NULL
			
			//if(returnString.length() > 0 && returnString.indexOf("VTCONF") > -1)
			if(returnString.length() > 0 &&  (returnString.indexOf("CONFSUCCES") > -1 || returnString.indexOf("VTCONF") > -1 || returnString.indexOf("SEND_SUCCESS") > -1))
            //CHANGED FOR GST RECOVERY ON 5 APRIL 2021 PASSWORD IS NOT SHOWING
			{
                System.out.println("DistRcpVerifyPasswordPrc commit sucess ::[" + isError + "] connection ["+connection);
				returnString = itmDBAccess.getErrorString("", "PRCSUCES", "", "", connection);
				System.out.println("PRCSUCES commit sucess ::[" + returnString + "]");
				
			}
			else
			{				
				isError = true;
				System.out.println("DistRcpVerifyPasswordPrc isError ::[" + isError + "]");
//				returnString = itmDBAccess.getErrorString("", "PRCFAIL", "", "", connection);
			}
			
		}
		catch(Exception e)
		{
			isError = true;
			System.out.println("Exception :DistRcpVerifyPasswordPrc :process(String xmlString2, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		} 
		finally
		{
			System.out.println("Closing Connection....");
			try
			{
				if(isError)
				{
					connection.rollback();
					System.out.println("DistRcpVerifyPasswordPrc connection rollback");
				}
				else
				{
					
					connection.commit();
					System.out.println("DistRcpVerifyPasswordPrc connection committed");
				}
				if(connection != null)
				{
					System.out.println("connection committed 234---"+connection);
					connection.close();
					System.out.println("connection committed 2345---"+connection);
					connection = null;
					System.out.println("DistRcpVerifyPasswordPrc connection closed");
				}
			}
			catch(Exception e)
			{
				//returnString = e.getMessage();//commented by monika on 7 april to get confirm message
				e.printStackTrace();
				System.out.println("DistRcpVerify catch print connection closed");

			}
		}
		System.out.println("errString...::DistRcpVerify [ "+returnString);
		return returnString;
	}
	
	private String checkNull(String input)
	{
		if (input == null || ("null").equalsIgnoreCase(input.trim()))
		{
			input = "";
		}
		else
		{
			input = input.trim();
		}
		return input;
	}
}
