package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ProcessEJB;
import ibase.webitm.utility.ITMException;

public class DistRcpTranDatePrc extends ProcessEJB implements DistRcpTranDatePrcLocal , DistRcpTranDatePrcRemote {
	Connection conn = null;
	E12GenericUtility genericUtility = new E12GenericUtility();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
	boolean isError = false;

	/*
	 * public String process(String xmlString, String windowName, String xtraParams)
	 * throws RemoteException, ITMException { Document headerDom = null; String
	 * retStr = ""; try { System.out.println("xmlString[process]::::::::::;;;" +
	 * xmlString); System.out.println("windowName[process]::::::::::;;;" +
	 * windowName); System.out.println("xtraParams[process]:::::::::;;;" +
	 * xtraParams); } catch (Exception e) { System.out.println(e.getMessage());
	 * e.printStackTrace(); } try { if (xmlString != null &&
	 * xmlString.trim().length() != 0) { headerDom =
	 * genericUtility.parseString(xmlString); System.out.println("headerDom" +
	 * headerDom); }
	 * 
	 * retStr = process(headerDom, windowName, xtraParams); } catch (Exception e) {
	 * System.out.println(
	 * "Exception :ImportDataPrcEJB :process(String xmlString,String windowName, String xtraParams):"
	 * + e.getMessage() + ":"); e.printStackTrace(); throw new ITMException(e); }
	 * return retStr; }
	 */
	
	public String process() throws RemoteException,ITMException
	{
		return "";
	}
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams)
			throws RemoteException,ITMException
	{
		Document detailDom = null;
		Document headerDom = null;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		String retStr = "";
		try
		{				
			System.out.println("xmlString[process]::::::::::;;;"+xmlString);
			System.out.println("xmlString2[process]::::::::::;;;"+xmlString2);
			System.out.println("windowName[process]::::::::::;;;"+windowName);
			System.out.println("xtraParams[process]:::::::::;;;"+xtraParams);
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		try
		{	
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				headerDom = genericUtility.parseString(xmlString); 
				System.out.println("headerDom" + headerDom);
			}
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				detailDom = genericUtility.parseString(xmlString2); 
				System.out.println("detailDom" + detailDom);
			}
			retStr = process(headerDom, detailDom, windowName, xtraParams);
		}
		catch (Exception e)
		{			
			System.out.println("Exception :ImportDataPrcEJB :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 06/08/19
		}
		return retStr;
	}//END OF PROCESS (1)

	/*
	 * public String process(Document headerDom, String windowName, String
	 * xtraParams) throws RemoteException, ITMException {
	 * 
	 */	
	public String process(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		String retStr = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "", errString = "";
		String retString = "", errCode = "", tranId = "";
		int count = 0;
		String loginSiteCode = "", empCode = "", chgUser = "", chgTerm = "", loginCode = "";
		ConnDriver connDriver = new ConnDriver();
		Timestamp tranDateTimestmp=null;
		String tranDate="";
		String tranIddom="";
		String confirmed="";
		
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		int ctr = 0,cnt = 0;
		String childNodeName = null;
		
		int currentFormNo=0;
		int childNodeListLength;
		int parentNodeListLength = 0;
		
		int no = 0;		
		
		// end
		try {

			/* System.out.println("Inside the component-----"); */
			System.out.println("xtraParams :::::::::::::::::::::::::::::::::: " + xtraParams);
			loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			empCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "userId");
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");
			loginCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			conn = getConnection();  
			conn.setAutoCommit(false);
			connDriver = null;
			StringBuffer  valueXmlString= new StringBuffer();
			tranId = genericUtility.getColumnValue("tran_id",headerDom);
			System.out.println("tranId>>>>>>>>>>>>>"+tranId);
			System.out.println("tranid new is" + tranId);
			
		    sql = "select confirmed from distord_rcp where tran_id = '"+tranId+"' ";
			pstmt = conn.prepareStatement(sql);
			
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				confirmed =  rs.getString("confirmed") == null ? "N" : rs.getString("confirmed");
			
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs= null;
			if("Y".equalsIgnoreCase(confirmed))
			{
				errCode = "VTCNF";
				errString =itmDBAccessEJB.getErrorString("",errCode,chgUser,"",conn);	
				return errString;
			}	
			parentNodeList = detailDom.getElementsByTagName("Detail1");
			parentNodeListLength = parentNodeList.getLength(); 
			System.out.println("parentNodeListLength:::::::::"+parentNodeListLength);
			for (int selectedRow = 0; selectedRow < parentNodeListLength; selectedRow++)
			{
				parentNode = parentNodeList.item(selectedRow);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				System.out.println("childNodeListLength::: "+ childNodeListLength+"\n");			

				for (int childRow = 0; childRow < childNodeListLength; childRow++)
				{					
					childNode = childNodeList.item(childRow);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equals("tran_date"))
					{
						if(childNode.getFirstChild()!=null)
						{
							tranDate = childNode.getFirstChild().getNodeValue();
							System.out.println("deliveryNoOld code ......"+tranDate);
						}						

					}

				}
			}
			tranDateTimestmp = Timestamp.valueOf(genericUtility.getValidDateString(tranDate,genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat()) + " 00:00:00.0");

			
			
			//if(!"Y".equalsIgnoreCase(confirmed))
			//{
				//System.out.println("receipt status:"+confirmed);
				//if("Y".equalsIgnoreCase(confirmed))
				//{
					sql = "update distord_rcp set tran_date = ? where tran_id=? ";
					System.out.println("update sql--->"+sql);
					pstmt = conn.prepareStatement(sql);
					pstmt.setTimestamp (1,tranDateTimestmp);  
					pstmt.setString(2, tranId);
					count = pstmt.executeUpdate();

					pstmt.close();
					pstmt = null;

					/* System.out.println("count of update---"+count); */
					if(count >0 )
					{
						errCode = "SUCCESSPRC";
						errString =itmDBAccessEJB.getErrorString("updated",errCode,chgUser,"",conn);						
						conn.commit();					
					}	

					else
					{
						conn.rollback();
						errString = itmDBAccessEJB.getErrorString("","VTPRCERR","","",conn);
						return errString;
			
					}
				//}
			//}
			
		}

		catch(Exception e)
		{
			System.out.println("Exception in ImportDataPrcEJB..."+e.getMessage());
			e.printStackTrace();
			throw new ITMException(e); 
		}
		finally
		{
			try
			{
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if (conn != null)
				{
					conn.close();
					conn = null;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}

		System.out.println("returning from  ImportDataPrcEJB   "+errString);
		return (errString);
	}

}
