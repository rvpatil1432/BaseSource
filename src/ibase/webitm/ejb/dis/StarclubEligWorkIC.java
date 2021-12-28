/********************************************************
	Title : StarclubEligWorkIC [T16ASUN001]
	Date  : 27 - APR - 2016
	Author: Poonam Gole

 ********************************************************/

package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.text.*;
import java.util.ArrayList;
import java.util.Date;
import java.sql.*;
import java.text.SimpleDateFormat;
import org.w3c.dom.*;
import javax.ejb.*;

import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.*;
import ibase.system.config.*;
import ibase.webitm.ejb.*;

@Stateless 
public class StarclubEligWorkIC extends ValidatorEJB implements StarclubEligWorkICRemote,StarclubEligWorkICLocal
{

	E12GenericUtility genericUtility = new E12GenericUtility();
	
	public String wfValData() throws RemoteException,ITMException
	{
		return "";
	}

	public String itemChanged() throws RemoteException,ITMException
	{
		return "";
	}
	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String  errString = null;
		System.out.println("Validation Start..........");
		try
		{
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1); 
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString(xmlString2);
			}
			errString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : StarclubEligWorkIC() : wfValData(String xmlString) : ==>\n"+e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return (errString);
	}
	public String wfValData(Document dom, Document dom1,Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = " ";
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		//String columnValue = null;
		String childNodeName = null;
		//ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		String errCode = "";
		String userId = null;
		int cnt = 0;
		int ctr = 0;
		int currentFormNo = 0;
		int childNodeListLength=0;
		Connection conn = null;
		PreparedStatement pstmt=null;
		ResultSet rs = null;
		String sql = null;
		ConnDriver connDriver = new ConnDriver();
		
		String division = "";
		String empCode = ""; 
		String siteCode = "";
		String fromDate = "";
		String toDate ,acctPrd= "";
		String sqlDiv = "";
		int cntdiv = 0;
		ResultSet divrs = null;
		String errorType = "";
		Date frDate = null;
		Date todate = null;
		ArrayList <String> errList = new ArrayList<String>();
		ArrayList <String>errFields = new ArrayList <String> ();
		StringBuffer errStringXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root><Errors>"); 
		try
		{
			System.out.println( "wfValData called" );
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			//userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			//loginSite = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			switch(currentFormNo)
			{
				case 1 :
					System.out.println("VALIDATION FOR DETAIL [ 1 ]..........");
					parentNodeList = dom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
					for(ctr = 0; ctr < childNodeListLength; ctr++)
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						System.out.println("childNodeName["+childNodeName+"]");
						if ( childNodeName.equalsIgnoreCase( "site_code" ) )
						{
							siteCode = checkNull(genericUtility.getColumnValue( "site_code", dom, "1" ));
							System.out.println("Inside wfval siteCode>>>"+siteCode);
							if(siteCode!=null && siteCode.trim().length() > 0)
							{
								sql = "SELECT COUNT(1) FROM SITE WHERE SITE_CODE = ?";
								pstmt = conn.prepareStatement( sql );
								pstmt.setString(1,siteCode);
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									cnt = rs.getInt( 1 );
								} 
								System.out.println(" COUNT =====> [" + cnt +editFlag+ "]");
								if( cnt == 0 )
								{
									errCode = "INVDSITECD";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								rs.close();
								rs = null;

								pstmt.close();
								pstmt = null;	 
							}
					    } 
						if ( childNodeName.equalsIgnoreCase( "division" ) )
						{
							System.out.println("Inside wfval division>>>");
							if ( childNode.getFirstChild() == null )
							{
								errCode = "VMDIVISNUL";
								errList.add(errCode);
								errFields.add( childNodeName.toLowerCase());
							}
							else
							{
								
								division = checkNull(genericUtility.getColumnValue( "division", dom, "1" ));
								System.out.println("Inside wfval division else>>>"+division);
								
								sql = " SELECT COUNT(*) FROM ITEMSER WHERE ITEM_SER ='" + division.trim() + "'";
								pstmt = conn.prepareStatement( sql );
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									cnt = rs.getInt( 1 );
								} 
								if( cnt == 0 )
								{
									errCode = "VTNOTDIVIN";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								rs.close();
								rs = null;

								pstmt.close();
								pstmt = null;
							}
						}
						if ( childNodeName.equalsIgnoreCase( "emp_code" ) )
						{
							System.out.println("Inside wfval emp_code>>>");
							if ( childNode.getFirstChild() == null )
							{
								System.out.println("Inside wfval childNode emp_code>>>"+childNode.getFirstChild());
								errCode = "VMEMP01";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else
							{
								System.out.println("Inside wfval emp_code else>>>");
								empCode = genericUtility.getColumnValue( "emp_code", dom);
								sql = "SELECT COUNT(1) FROM EMPLOYEE WHERE EMP_CODE = ?";
								pstmt = conn.prepareStatement( sql );
								pstmt.setString(1,empCode);
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									cnt = rs.getInt( 1 );
								} 
								System.out.println(" COUNT =====> [" + cnt +editFlag+ "]");
								if( cnt == 0 )
								{
									errCode = "VMEMPCDMT";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								rs.close();
								rs = null;

								pstmt.close();
								pstmt = null;	
								
								
								
								
								
							}
						}
						if ( childNodeName.equalsIgnoreCase( "acct_prd" ) )
						{
							acctPrd = checkNull(genericUtility.getColumnValue( "acct_prd", dom));
							empCode = checkNull(genericUtility.getColumnValue( "emp_code", dom));
							division = checkNull(genericUtility.getColumnValue( "division", dom));;
							if ( childNode.getFirstChild() == null )
							{
								System.out.println("Inside wfval childNode acct_prd>>>"+childNode.getFirstChild());
								errCode = "NULLACCPRD";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else 
							{
								empCode = genericUtility.getColumnValue( "emp_code", dom);
								
								sql = "SELECT COUNT(1) FROM ACCTPRD WHERE CODE = ?";
								pstmt = conn.prepareStatement( sql );
								pstmt.setString(1,acctPrd);
								rs = pstmt.executeQuery();
								if( rs.next() )
								{
									cnt = rs.getInt( 1 );
								} 
								System.out.println(" COUNT =====> [" + cnt +editFlag+ "]");
								if( cnt == 0 )
								{
									errCode = "INVDACCPR";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								rs.close();
								rs = null;

								pstmt.close();
								pstmt = null;	 
								
								if ("A".equalsIgnoreCase(editFlag) && childNode.getFirstChild() != null  )
								{
									System.out.println("Inside empCode emp_code else>>>"+empCode+"division"+division+"editFlag"+editFlag);
									//Added by saiprasad G. on 24-APR-19[For removing uniques constraint error when inserting the duplicates]START
									//sqlDiv = "SELECT COUNT(1) FROM STARCLUB_ELIGIBILITY_WORKING WHERE DIVISION = ? AND EMP_CODE = ? AND ACCT_PRD  = ?";
									sqlDiv = "SELECT COUNT(1) FROM STARCLUB_ELIGIBILITY_WORKING WHERE DIVISION = ? AND EMP_CODE = ?";
									//Added by saiprasad G. on 24-APR-19[For removing uniques constraint error when inserting the duplicates]END
									pstmt = conn.prepareStatement( sqlDiv );
									pstmt.setString(1,division);
									pstmt.setString(2,empCode);
									//Commented by saiprasad G,
									//pstmt.setString(3,acctPrd);
									divrs = pstmt.executeQuery();
									if( divrs.next() )
									{
										cntdiv = divrs.getInt( 1 );
									} 
									System.out.println(" COUNT =====> [" + cntdiv + "]");
									if( cntdiv > 0 )
									{
										errCode = "VTDUPDIVIN";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									divrs.close();
									divrs = null;

									pstmt.close();
									pstmt = null;
								}
							}
								
							
						}
						
					
						if ( childNodeName.equalsIgnoreCase( "to_dt" ) )
						{
							toDate = checkNull(genericUtility.getColumnValue( "to_dt", dom, "1" ));
							fromDate = checkNull(genericUtility.getColumnValue( "from_dt", dom, "1" ));
							System.out.println("Inside wfval toDate>>>"+toDate + "fromDate" +fromDate);
							if(toDate!=null && toDate.trim().length() > 0)
							{
								if(fromDate!=null && fromDate.trim().length() > 0)
								{
									frDate= Timestamp.valueOf(genericUtility.getValidDateString(fromDate, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
									todate= Timestamp.valueOf(genericUtility.getValidDateString(toDate, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
									if( todate.before(frDate) )
									{
										errCode = "INVDFRDATE";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
							    }
							}
					    }
							
			}
					break;
		   }
			
			int errListSize = errList.size();
			cnt = 0;
			String errFldName = null;
			System.out.println("errListSize [" + errListSize + "] errFields size [" + errFields.size() + "]");
			if ((errList != null) && (errListSize > 0))
			{
				System.out.println("Inside errList >"+errList);
				for (cnt = 0; cnt < errListSize; cnt++)
				{
					errCode = (String)errList.get(cnt);
					System.out.println("errCode :"+errCode);
					int pos = errCode.indexOf("~");
					System.out.println("pos :"+pos);
					if(pos>-1)
					{
					errCode=errCode.substring(0,pos);
					}
					
					System.out.println("error code is :"+errCode);
					errFldName = (String)errFields.get(cnt);
					System.out.println(" cnt [" + cnt + "] errCode [" + errCode + "] errFldName [" + errFldName + "]");
					errString = getErrorString(errFldName, errCode, userId);
					errorType = errorType(conn, errCode);
					System.out.println("errorType :"+errorType);
					if (errString.length() > 0)
					{
						String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 
								8, errString.indexOf("<trace>"));
						bifurErrString = bifurErrString + 
								errString.substring(errString.indexOf("</trace>") + 
										8, errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						errString = "";
					}
					if (errorType.equalsIgnoreCase("E"))
					{
						break;
					}
				}

				errStringXml.append("</Errors> </Root> \r\n");
			}
			else
			{
				errStringXml = new StringBuffer("");
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception ::"+e);
			e.printStackTrace();
			errString = e.getMessage();
            throw new ITMException(e);									
		}
		finally
		{
			try
			{
				if(conn != null)
				{
					if( pstmt != null )
					{
						pstmt.close();
						pstmt = null;
					}
					
					if( rs != null )
					{
						rs.close();
						rs = null;
					}
					conn.close();
				}
				conn = null;
			}
			catch(Exception d)
				{
				  d.printStackTrace();
				}			
			System.out.println(" < StarclubEligWorkIC > CONNECTION IS CLOSED");
		}
		
		System.out.println("ErrString ::[ "+errStringXml.toString()+" ]");
		return errStringXml.toString();
	}

	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = null;
		try
		{
			System.out.println("xmlString" + xmlString);
			dom = parseString(xmlString); 
			System.out.println("xmlString1" + xmlString1);
			dom1 = parseString(xmlString1); 

			if (xmlString2.trim().length() > 0 )
			{
				System.out.println("xmlString2" + xmlString2);
				dom2 = parseString("<Root>" + xmlString2 + "</Root>");
			}
			valueXmlString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [StarclubEligWorkIC][itemChanged] :==>\n"+e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
        return valueXmlString; 
	}
	
	public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Connection conn = null;
		ConnDriver connDriver = new ConnDriver();
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		String sql = null;
		int currentFormNo = 0;
		StringBuffer valueXmlString = new StringBuffer();
		String columnValue = null;
		NodeList parentNodeList = null;
		Node parentNode = null; 
		NodeList childNodeList = null;
		int childNodeListLength = 0;
		int ctr = 0;
		SimpleDateFormat sdf = null;
		
		String divDescr = "";
		String division  = "";
		String empCode = "";
		String empName = "";
		String siteCode = "";
		String siteDescr = "";
		
		try
		{
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver = null;
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			
			System.out.println("Current Form No ["+currentFormNo+"]");							
			switch (currentFormNo)
			{
				case 1:
					valueXmlString.append("<Detail1>");	
					parentNodeList = dom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					ctr = 0;
					childNodeListLength = childNodeList.getLength();
					System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");
					if (currentColumn.trim().equals( "site_code" ))
					{
						System.out.println("Inside itmChanged site_code...");
						siteCode = checkNull(genericUtility.getColumnValue( "site_code", dom ));
						sql = "SELECT  DESCR SITE_DESCR FROM SITE  WHERE  SITE_CODE = ? ";
						pStmt = conn.prepareStatement( sql );
						pStmt.setString(1,siteCode);
						rs = pStmt.executeQuery();
						if( rs.next() )
						{
							siteDescr = rs.getString( "SITE_DESCR" );
						}	
						
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
						valueXmlString.append("<site_descr>").append("<![CDATA[" + ( siteDescr != null ? siteDescr.trim() : ""  )+ "]]>").append("</site_descr>");	
					}
					if (currentColumn.trim().equals( "division" ))
					{
						System.out.println("Inside itmChanged division...");
						division = checkNull(genericUtility.getColumnValue( "division", dom ));
						sql = "SELECT  DESCR ITEM_DESCR FROM ITEMSER  WHERE  ITEM_SER = ? ";
						pStmt = conn.prepareStatement( sql );
						pStmt.setString(1,division);
						rs = pStmt.executeQuery();
						if( rs.next() )
						{
							divDescr = rs.getString( "ITEM_DESCR" );
						}	
						
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
						valueXmlString.append("<itemser_descr>").append("<![CDATA[" + ( divDescr != null ? divDescr.trim() : ""  )+ "]]>").append("</itemser_descr>");	
					}
					if (currentColumn.trim().equals( "emp_code" ))
					{
						System.out.println("Inside itmChanged emp_code...");
						empCode = checkNull(genericUtility.getColumnValue( "emp_code", dom ));
						sql = "SELECT TRIM(EMP_FNAME)||' '||TRIM(EMP_MNAME)||' '||TRIM(EMP_LNAME) AS EMP_NAME FROM EMPLOYEE  WHERE EMP_CODE = ? ";
						pStmt = conn.prepareStatement( sql );
						pStmt.setString(1,empCode);
						rs = pStmt.executeQuery();
						if( rs.next() )
						{
							empName = rs.getString( "EMP_NAME" );
						}	
						
						rs.close();
						rs = null;
						pStmt.close();
						pStmt = null;
						valueXmlString.append("<emp_name>").append("<![CDATA[" + ( empName != null ? empName.trim() : ""  )+ "]]>").append("</emp_name>");	
					}
					
					
				
					valueXmlString.append("</Detail1>");
					valueXmlString.append("</Root>");	
					break;
				
			}//END OF TRY
		}
		catch(Exception e)
		{
			System.out.println("StarclubEligWorkIC Exception ::"+ e.getMessage());
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		finally
		{
			try
			{
				if ( rs != null )
				{
					rs.close();
					rs = null;
				}
				if ( pStmt != null )
				{
					pStmt.close();
					pStmt = null;					
				}
				if ( conn != null )
				{
					conn.close();
					conn = null;
				}
			}
			catch(Exception e)
			{
				System.out.println("Exception ::" + e);
				e.printStackTrace();
			}			
		}
		return valueXmlString.toString();
	}
	
	private String errorType(Connection conn, String errorCode) throws ITMException
    {
		String msgType = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, errorCode);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				msgType = rs.getString("MSG_TYPE");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			throw new ITMException(ex); //Added By Mukesh Chauhan on 05/08/19
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return msgType;
	}
	public String checkNull( String inputVal )
	{
		if ( inputVal == null )
		{
			inputVal = "";
		}
		return inputVal;
	}
	
}
