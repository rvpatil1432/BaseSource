/********************************************************
	Title : SiteItemUpdateIC[D14ISUN010]
	Date  : 09/03/15
	Developer: Chandrashekar

 ********************************************************/
package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.utility.E12GenericUtility;

import java.text.SimpleDateFormat;
import java.util.*;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.ejb.Stateless; 
@Stateless 

public class SiteItemUpdateIC extends ValidatorEJB implements SiteItemUpdateICLocal, SiteItemUpdateICRemote
{
	E12GenericUtility genericUtility= new  E12GenericUtility();

	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			if (xmlString != null && xmlString.trim().length() > 0 )
			{
				dom = parseString(xmlString);
				System.out.println("xmlString["+xmlString+"]");
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0 )
			{
				dom1 = parseString(xmlString1);
				System.out.println("xmlString1["+xmlString1+"]");
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0 )
			{
				dom2 = parseString(xmlString2);
				System.out.println("xmlString2["+xmlString2+"]");				
			}
			
			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [SiteItemUpdateIC][wfValData( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e);
		}
		return(errString);
	}
	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
			String childNodeName = null;
			String errString = "";
			String errCode = "";
			String userId = "";
			String sql = "";
			String errorType = "";
			int cnt = 0;
			int ctr=0;
			int childNodeListLength;
			NodeList parentNodeList = null;
			NodeList childNodeList = null;	
			Node parentNode = null;
			Node childNode = null;
			ArrayList<String> errList = new ArrayList<String>();
			ArrayList<String> errFields = new ArrayList<String>();
			Connection conn = null;
			PreparedStatement pstmt = null ;
			ResultSet rs = null;
			ConnDriver connDriver = new ConnDriver();
			StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
			
			int currentFormNo =0,count=0;
			String siteCodeFrom="",siteCode="",finEntity="",siteCodeTo="";
			String itemCodeFrom = "", itemCodeTo = "",suppSource = "", siteCodeSupp = "",itemSer="";
			try
			{
				System.out.println("@@@@@@@@ wfvaldata called");
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
				connDriver = null;
				userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
				if(objContext != null && objContext.trim().length()>0)
				{
					currentFormNo = Integer.parseInt(objContext);
				}
				switch(currentFormNo)
				{
				case 1 :
					parentNodeList = dom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
	
					for(ctr = 0; ctr < childNodeListLength; ctr++)
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						if(childNodeName.equalsIgnoreCase("fin_entity"))
				    	{
							finEntity = genericUtility.getColumnValue("fin_entity", dom);
							if(finEntity == null || (finEntity.length() == 0))
							{
								errList.add("VMFINENT1 ");
								errFields.add(childNodeName.toLowerCase());
							}
							if(finEntity != null && finEntity.trim().length() > 0)
							{
								sql = " select count(*) from finent where fin_entity = ?  ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1,finEntity);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									count =  rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(count == 0) 
								{
									errCode = "VMFINENTM2";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());	
								}
								
							}
						}
						else if(childNodeName.equalsIgnoreCase("site_code__fr"))
				    	{
							siteCodeFrom = checkNull(genericUtility.getColumnValue("site_code__fr", dom));
							if(siteCodeFrom == null ||  siteCodeFrom.trim().length() == 0)
							{
								errCode = "VMSITECDFR";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else if(siteCodeFrom.trim().length() > 0)
							{
								if(!isExist("site","site_code",siteCodeFrom,conn))
								{
									errCode = "VMSITEFR";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								sql = " select count(*) from site where fin_entity = ?  and site_code = ? ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1,finEntity);
								pstmt.setString(2,siteCodeFrom);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									count =  rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(count == 0) 
								{
									errCode = "VMSITEFR1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());	
								}
							}

						}
						else if(childNodeName.equalsIgnoreCase("site_code__to"))
				    	{
							siteCodeTo = checkNull(genericUtility.getColumnValue("site_code__to", dom));
							if( siteCodeTo == null || siteCodeTo.trim().length() == 0)
							{
								errCode = "VMSITECDTO";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else if(siteCodeTo.trim().length() > 0)
							{
								if(!isExist("site","site_code",siteCodeTo,conn))
								{
									errCode = "VMSITETO";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								sql = " select count(*) from site where fin_entity = ?  and site_code = ? ";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1,finEntity);
								pstmt.setString(2,siteCodeTo);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									count =  rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(count == 0) 
								{
									errCode = "VMSITETO1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());	
								}
							}
							

						}
						else if(childNodeName.equalsIgnoreCase("item_code__fr"))
				    	{
							itemCodeFrom = checkNull(genericUtility.getColumnValue("item_code__fr", dom));
							if(itemCodeFrom == null ||  itemCodeFrom.trim().length() == 0)
							{
								errCode = "VMITMCDFR";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else if(itemCodeFrom.trim().length() > 0 && !"00".equalsIgnoreCase(itemCodeFrom))
							{
								if(!isExist("item","item_code",itemCodeFrom,conn))
								{
									errCode = "VMINVITMFR";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
						else if(childNodeName.equalsIgnoreCase("item_code__to"))
				    	{
							itemCodeTo = checkNull(genericUtility.getColumnValue("item_code__to", dom));
							if(itemCodeTo == null ||  itemCodeTo.trim().length() == 0)
							{
								errCode = "VMITMCDTO";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else if(itemCodeTo.trim().length() > 0 && !"ZZ".equalsIgnoreCase(itemCodeTo))
							{
								if(!isExist("item","item_code",itemCodeTo,conn))
								{
									errCode = "VMINVITMTO";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
						else if(childNodeName.equalsIgnoreCase("site_code__supp"))
				    	{
							suppSource = checkNull(genericUtility.getColumnValue("supp_sour", dom));
							siteCodeSupp = checkNull(genericUtility.getColumnValue("site_code__supp", dom));
							System.out.println("suppSource["+suppSource+"]siteCodeSupp["+siteCodeSupp+"]");
							if(siteCodeSupp != null &&  siteCodeSupp.trim().length() > 0)
							{
								if("D".equalsIgnoreCase(suppSource))
								{
									if(!isExist("site","site_code",siteCodeSupp,conn))
									{
										errCode = "VMSITESUPP";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								else if("P".equalsIgnoreCase(suppSource))
								{
									if(!isExist("finent","fin_entity",siteCodeSupp,conn))
									{
										errCode = "VMFINSUPP";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								
								/*else if(suppSource!=null && ("P".equalsIgnoreCase(suppSource)||"D".equalsIgnoreCase(suppSource)))
								{
									System.out.println("Equal t P & D================");
									if(siteCodeSupp==null||siteCodeSupp.trim().length()==0)
									{
										errCode = "VMSUPPNUL";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}*/
								else if(suppSource!=null && "N".equalsIgnoreCase(suppSource))
								{
									System.out.println("Equal to N===============");
									errCode = "VMSUPNUL1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								
							}
							else								
							{
								if(("P".equalsIgnoreCase(suppSource)||"D".equalsIgnoreCase(suppSource)))
								{
									errCode = "VMSUPPNUL";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							
						}
						else  if(childNodeName.equalsIgnoreCase("item_ser"))
						{
							itemSer = checkNull(genericUtility.getColumnValue("item_ser", dom));
							if(itemSer != null &&  itemSer.trim().length() > 0)
							{
								if(!isExist("itemser","item_ser",itemSer,conn))
								{
									errCode = "VTITEMSER1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
						


				}// end for
					break;  // case 1 end
				}		
	
				int errListSize = errList.size();
				cnt = 0;
				String errFldName = null;
				if(errList != null && errListSize > 0)
				{
					for(cnt = 0; cnt < errListSize; cnt ++)
					{
						errCode = errList.get(cnt);
						errFldName = errFields.get(cnt);
						System.out.println("errCode .........." + errCode);
						errString = getErrorString(errFldName, errCode, userId);
						errorType = errorType(conn , errCode);
						if(errString.length() > 0)
						{
							String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8, errString.indexOf("<trace>"));
							bifurErrString = bifurErrString + errString.substring(errString.indexOf("</trace>") + 8, errString.indexOf("</Errors>"));
							errStringXml.append(bifurErrString);
							errString = "";
						}
						if(errorType.equalsIgnoreCase("E"))
						{
							break;
						}
					}
					errList.clear();
					errList = null;
					errFields.clear();
					errFields = null;
					errStringXml.append("</Errors> </Root> \r\n");
				}
				else
				{
					errStringXml = new StringBuffer("");
				}
			
		}
			catch(Exception e)
			{
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
						if(rs != null) 
						{
							rs.close();
							rs = null;
						}
						if(pstmt != null) 
						{
							pstmt.close();
							pstmt = null;
						}
						conn.close();
					}
					conn = null;
				} 
				catch(Exception d)
				{
					d.printStackTrace();
					throw new ITMException(d);
				}
			}
			errString = errStringXml.toString();
			return errString;
		}

	//end of validation
	// method for item change
		public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
		{
			Document dom = null;
			Document dom1 = null;
			Document dom2 = null;
			String valueXmlString = "";
			System.out.println("xmlString............."+xmlString);
			System.out.println("xmlString1............"+xmlString1);
			System.out.println("xmlString2............"+xmlString2);
			try
			{   
				if(xmlString != null && xmlString.trim().length() > 0)
				{
					dom = parseString(xmlString);
				}
				if(xmlString1 != null && xmlString1.trim().length() > 0)
				{
					dom1 = parseString(xmlString1);
				}
				if(xmlString2 != null && xmlString2.trim().length() > 0)
				{
					dom2 = parseString(xmlString2);
				}
				valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
			}
			catch(Exception e)
			{
				System.out.println("Exception : [SiteItemUpdateIC][itemChanged( String, String )] :==>\n" + e.getMessage());
				throw new ITMException(e);
			}
			return valueXmlString;
		}
		// method for item change
		public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
		{
		String loginSite = "";
		StringBuffer valueXmlString = new StringBuffer();
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		E12GenericUtility genericUtility= new  E12GenericUtility();
		int currentFormNo = 0;
		String columnValue = "";
		try
		{

			if (objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext.trim());
			}

			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");

			System.out.println("**********ITEMCHANGE FOR CASE" + currentFormNo + "**************");
			switch (currentFormNo)
			{
			case 1:
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail1>");
				System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");
				if (currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					loginSite = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSite");

					System.out.println("loginSite>>>["+loginSite+"]");
					valueXmlString.append("<site_code__fr>").append("<![CDATA[" + loginSite + "]]>").append("</site_code__fr>");
					valueXmlString.append("<site_code__to>").append("<![CDATA[" + loginSite + "]]>").append("</site_code__to>");
					valueXmlString.append("<item_code__fr><![CDATA[").append("00").append("]]></item_code__fr>");
					valueXmlString.append("<item_code__to><![CDATA[").append("ZZ").append("]]></item_code__to>");
					valueXmlString.append("<supp_sour><![CDATA[").append("D").append("]]></supp_sour>");
					valueXmlString.append("<integral_qty><![CDATA[").append("0").append("]]></integral_qty>");
					valueXmlString.append("<reo_qty><![CDATA[").append("0").append("]]></reo_qty>");
				}
				valueXmlString.append("</Detail1>");
				break;
			}
			valueXmlString.append("</Root>");
		} catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::" + e.getMessage());
			throw new ITMException(e);
		} 
		return valueXmlString.toString();
	}
		
	private String checkNull(String input) 
	{
		if(input == null)
		{
			input = "";
		}
		return input;
	}
	private boolean isExist(String table, String field, String value,Connection conn) throws SQLException
	{
		String sql = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		int cnt=0;
		boolean retStrng = false;

		sql = " SELECT COUNT(1) FROM "+ table + " WHERE " + field + " = ? ";
		pstmt =  conn.prepareStatement(sql);
		pstmt.setString(1,value);
		rs = pstmt.executeQuery();
		if(rs.next())
		{
			cnt = rs.getInt(1);
		}
		rs.close();
		rs = null;
		pstmt.close(); 
		pstmt = null;
		if( cnt > 0)
		{
			retStrng = true;
		}
		if( cnt == 0 )
		{
			retStrng = false;
		}
		System.out.println("@@@@ retStrng:::["+retStrng+"]");
		return retStrng;
	}
	private String errorType(Connection conn , String errorCode) throws ITMException
	{
		String msgType = "";
		PreparedStatement pstmt = null ; 
		ResultSet rs = null;
		try
		{			
			String  sql = "SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ?";
			pstmt = conn.prepareStatement(sql);			
			pstmt.setString(1,errorCode);			
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				msgType = rs.getString("MSG_TYPE");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new ITMException(ex);
		}		
		finally
		{
			try
			{
				if(rs != null)
				{
					rs.close();
					rs = null;
				}
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}		
		return msgType;
	}

}	
