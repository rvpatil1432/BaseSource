
/********************************************************
	Title 	 : CustItemUpdateIC [D14LSUN003]
	Date  	 : 10/MAR/15
	Developer: Priyanka Shinde
 ********************************************************/
package ibase.webitm.ejb.dis;


import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.ejb.Stateless;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.fin.FinCommon;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
@Stateless
public class CustItemUpdateIC extends ValidatorEJB implements CustItemUpdateICLocal,CustItemUpdateICRemote  
{

	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
	String winName = null;
	FinCommon finCommon = null;
	ValidatorEJB validator = null;

	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		System.out.println("Priyanka testing : Inside wfValData 0 ");
		System.out.println("Priyanka testing : xmlString :"+xmlString);
		System.out.println("Priyanka testing : xmlString1 :"+xmlString1);
		System.out.println("Priyanka testing : xmlString2 :"+xmlString2);
		System.out.println("Priyanka testing : objContext :"+objContext);
		System.out.println("Priyanka testing : editFlag :"+editFlag);
		System.out.println("Priyanka testing : xtraParams :"+xtraParams);
		try
		{
			if (xmlString != null && xmlString.trim().length() > 0)
			{
				dom = parseString(xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom1 = parseString(xmlString1);
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0)
			{
				dom2 = parseString(xmlString2);
			}
			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
		} catch (Exception e)
		{
			System.out.println("Exception : [CustItemUpdateIC][wfValData( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e);
		}
		return (errString);
	}

	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		int currentFormNo = 0;
		int childNodeListLength;
		int ctr = 0;
		int cnt=0;
		String childNodeName = null;
		String errString = "";
		String errCode = "";
		String userId = "";
		String sql = "";
		String errorType = "";
		String custCodeFrom="",custCodeTo="";
		String siteCodeFrom="",siteCodeTo="";
		String itemCodeFrom="",itemCodeTo="";
		//double integralQty=0;
		String integralQty="";
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ConnDriver connDriver = new ConnDriver();
		
		System.out.println("Priyanka testing : Inside wfValData 1 ");
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
		System.out.println("Priyanka testing : editFlag :" + editFlag);

		try
		{
	
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			if (objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext);
				System.out.println("Priyanka testing : currentFormNo :"+currentFormNo);
			}
	
			switch (currentFormNo)
			{
			 case 1:
				System.out.println("Priyanka testing case 1 for validation ");
	
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				System.out.println("Priyanka testing :parentNode : "+parentNode);
				System.out.println("Priyanka testing :childNodeListLength : "+childNodeListLength);
				for (ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					System.out.println("Priyanka testing :childNodeName : "+childNodeName);
						
								
					//added for site_code_from
					if(childNodeName.equalsIgnoreCase("site_code__fr"))
			    	{
						siteCodeFrom = checkNull(genericUtility.getColumnValue("site_code__fr", dom));
						if(siteCodeFrom == null ||  siteCodeFrom.trim().length() == 0)
						{
							errCode = "VMSITECDFR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else 
						{
							sql = "select count(1) from site where site_code= ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, siteCodeFrom);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
					    	
							 if(cnt == 0)
							 {	
								 errCode = "VMSITEFR";
				    			errList.add(errCode);
				    			errFields.add(childNodeName.toLowerCase());		
								
							 }	
						}

					}
					
					//added for site_code_to
					else if(childNodeName.equalsIgnoreCase("site_code__to"))
			    	{
						siteCodeTo = checkNull(genericUtility.getColumnValue("site_code__to", dom));
						if(siteCodeTo == null ||  siteCodeTo.trim().length() == 0)
						{
							errCode = "VMSITECDTO";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else 
						{
							sql = "select count(1) from site where site_code= ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, siteCodeTo);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								cnt = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
					    	
							 if(cnt == 0)
							 {	
								 errCode = "VMSITETO";
				    			errList.add(errCode);
				    			errFields.add(childNodeName.toLowerCase());		
								
							 }	
						}

					}
					
	
					//item_code__from
					else if(childNodeName.equalsIgnoreCase("item_code__from"))
					{
						itemCodeFrom = checkNull(genericUtility.getColumnValue("item_code__from",dom));
						System.out.println(" itemCodeFrom ====" + itemCodeFrom);
	
						if (itemCodeFrom == null || itemCodeFrom.trim().length() == 0)
						{
							errCode = "VMITMCDFR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							
						}
						else 
						{
							if(!"00".equalsIgnoreCase(itemCodeFrom))
							{
								sql = "select count(1) from item where item_code= ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, itemCodeFrom);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
						    	
								 if(cnt == 0)
								 {	
									 errCode = "VMINVITMFR";
						    		 errList.add(errCode);
						    		 errFields.add(childNodeName.toLowerCase());	
									
								 }	
						    }
														
						}						
						
					}
					//item_code__to
					else if(childNodeName.equalsIgnoreCase("item_code__to"))
					{
						itemCodeTo = checkNull(genericUtility.getColumnValue("item_code__to",dom));
						System.out.println(" itemCodeTo ====" + itemCodeTo);
	
						if (itemCodeTo == null || itemCodeTo.trim().length() == 0)
						{
							errCode = "VMITMCDTO";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							
						}
						else 
						{
							
							if(!"ZZ".equalsIgnoreCase(itemCodeTo))
							{
								sql = "select count(1) from item where item_code= ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, itemCodeTo);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
						    	
								 if(cnt == 0)
								 {	
									    errCode = "VMINVITMTO";
						    			errList.add(errCode);
						    			errFields.add(childNodeName.toLowerCase());		
									
								 }	
							}							
						}	
					}
					//integral Qty
					else if(childNodeName.equalsIgnoreCase("integral_qty"))
					{
						integralQty   = checkNull(genericUtility.getColumnValue("integral_qty",dom ));
						if(integralQty==null|| integralQty.trim().length()==0)
						{
							errCode = "VMNULLQTY";
			    			errList.add(errCode);
			    			errFields.add(childNodeName.toLowerCase());		
						} 
						
					}
								
				  }//end of else  if loop
					//end of for 	
			     break;//end of switch
					
				} 
	
				int errListSize = errList.size();
				int count = 0;
				String errFldName = null;
				if (errList != null && errListSize > 0)
				{
					for (count = 0; count < errListSize; count++)
					{
						errCode = errList.get(count);
						errFldName = errFields.get(count);
						System.out.println(" testing :errCode .:" + errCode);
						errString = getErrorString(errFldName, errCode, userId);
						errorType = errorType(conn, errCode);
						if (errString.length() > 0)
						{
							String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8, errString.indexOf("<trace>"));
							bifurErrString = bifurErrString + errString.substring(errString.indexOf("</trace>") + 8, errString.indexOf("</Errors>"));
							errStringXml.append(bifurErrString);
							errString = "";
						}
						if (errorType.equalsIgnoreCase("E"))
						{
							break;
						}
					}
					errList.clear();
					errList = null;
					errFields.clear();
					errFields = null;
					errStringXml.append("</Errors> </Root> \r\n");
				} else
				{
					errStringXml = new StringBuffer("");
				}
	
		}//end of try
		catch (Exception e)
		{
			e.printStackTrace();
			errString = e.getMessage();
			throw new ITMException(e);
		} finally
		{
			try
			{
				if (conn != null)
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
					conn.close();
				}
				conn = null;
			} 
			catch (Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		errString = errStringXml.toString();
		System.out.println("testing : final errString : "+errString);
		return errString;

	}//end of validation method


	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams)	throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		System.out.println("hELLO PRINT");
		try
		{
			System.out.println("xmlString@@@@@@@"+xmlString);

			if(xmlString != null && xmlString.trim().length() > 0)
			{
				dom = parseString(xmlString);
			}
			System.out.println("dom@@@@@@@"+dom);

			System.out.println("xmlString1@@@@@@@"+xmlString1);

			if(xmlString1 != null && xmlString1.trim().length() > 0)
			{
				dom1 = parseString(xmlString1);
			}

			System.out.println("xmlString2@@@@@@@"+xmlString2);

			if(xmlString2 != null && xmlString2.trim().length() > 0)
			{
				dom2 = parseString(xmlString2);
			}
			System.out.println("HELLO1 PRINT");
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
			System.out.println("VALUE HELLO PRINT["+valueXmlString+"]");
		}
		catch (Exception e)
		{
			System.out.println("Exception : [FreightRateIC][itemChanged( String, String )] :==>\n" + 
					e.getMessage());
			throw new ITMException(e);
		}
		System.out.println("VALUE HELLO PRINTA["+valueXmlString+"]");
		return valueXmlString;
  }

	
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams)
			throws RemoteException, ITMException
	{
		System.out.println("sTART PRINT ");
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		String sql = "";	
		int ctr = 0;
		int currentFormNo = 0;
		int childNodeListLength = 0;
		String  chguser = null;
		String  chgterm = null;
	
		SimpleDateFormat sdf;
		StringBuffer valueXmlString = new StringBuffer();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		//Comment By Nasruddin 07-10-16 GenericUtility
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility= new  E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();		
		try
		{
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;
			//this.finCommon = new FinCommon();
			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(genericUtility.getApplDateFormat());
			if ((objContext != null) && (objContext.trim().length() > 0))
			{
				currentFormNo = Integer.parseInt(objContext);
				System.out.println("Priyanka itemchanged 1 currentFormNo : "+currentFormNo); 
			}
			System.out.println("XCtra Parameter============"+xtraParams);
			chguser = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			chgterm = getValueFromXTRA_PARAMS(xtraParams, "termId");
			
			System.out.println("Change User================"+chguser);
			System.out.println("Change date================"+currentDate);
			System.out.println("Change Term================"+chgterm);
			
			System.out.println("Curren5t date================"+currentDate);
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");
			switch (currentFormNo)
			{
			  case 1:
				System.out.println("CustItemUpdateIC itemchanged case 1");
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail1>");
				childNodeListLength = childNodeList.getLength();				

				System.out.println("CURRENT COLUMN Case 1 CustItemUpdateIC *******["+currentColumn+"]");
				if (currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
								
					valueXmlString.append("<item_code__from>").append("00").append("</item_code__from>");
					valueXmlString.append("<item_code__to>").append("ZZ").append("</item_code__to>");

				}

				valueXmlString.append("</Detail1>");
		     break;

			}
			valueXmlString.append("</Root>");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if (conn != null)
				{
					if (pstmt != null)
						pstmt.close();
					if (rs != null)
						rs.close();
					rs = null;
					pstmt = null;
					conn.close();
					conn = null;
				}
			}
			catch (Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		return valueXmlString.toString();
			}


	private String checkNull(String str)
	{
		if(str == null)
		{
			return "";
		}
		else
		{
			return str ;
		}

	}
	
	private String errorType(Connection conn, String errorCode) throws ITMException
	{
		String msgType = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			String sql = "SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, errorCode);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				msgType = rs.getString("MSG_TYPE");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		} 
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw new ITMException(ex);
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
			}
			catch (Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return msgType;
	}

}
