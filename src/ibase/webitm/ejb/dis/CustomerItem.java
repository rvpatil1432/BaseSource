
/********************************************************
	Title : CustItem
	Date  : 4/11/11
	Developer: Navanath Nawale

 ********************************************************/

package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import java.util.*;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.ejb.Stateless; 

@Stateless 
public class CustomerItem extends ValidatorEJB implements CustomerItemLocal,CustomerItemRemote {
	//Comment By Nasruddin 07-10-16 GenericUtility
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility = new E12GenericUtility();
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			System.out.println("Val xmlString :: " + xmlString);
			System.out.println("Val xmlString1 :: " + xmlString1);
			System.out.println("Val xmlString2 :: " + xmlString2 );
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString("<Root>" + xmlString2+ "</Root>");
			}
			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			throw new ITMException(e);
		}
		return(errString);
	}
	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		NodeList parentNodeList = null;
		NodeList childNodeList = null;	
		Node parentNode = null;
		Node childNode = null;
		String custCode ="";
		String itemCode ="";
		String unit="";
		String siteCodeSupp = "";
		String userId = "";
		String sql="";
		String errCode="";
		String errorType = "";
		String childNodeName = null;
		String errString = "";
		String minQtyStr = "";
		String integralQtyStr = "";

		int ctr=0;
		int childNodeListLength;
		int minQty= 0;
		int integralQty=0;
		long count = 0;
		
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement pstmt = null ;
		ResultSet rs = null;
		ConnDriver connDriver = new ConnDriver();
		int currentFormNo = 0; 
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>"); 
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			//Changed By Nasruddin 12-oct-16 add objContext and switch case
			if (objContext != null && objContext.trim().length() > 0) 
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			switch (currentFormNo) 
			{
				case 1: 
				{
					parentNodeList = dom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
					for(ctr = 0; ctr < childNodeListLength; ctr ++)
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						if(childNodeName.equalsIgnoreCase("cust_code"))
						{    
							custCode = genericUtility.getColumnValue("cust_code", dom);
							sql = "select count(*) from customer where cust_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,custCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count =  rs.getInt(1);
							}
							if(count == 0) 
							{
								errCode = "VTCUST1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}																
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						else if(childNodeName.equalsIgnoreCase("item_code"))
						{    
							itemCode = genericUtility.getColumnValue("item_code", dom);
							sql = "select count(*) from item where item_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,itemCode);
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
								errCode = "VTITEM1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						// Changed By Nasruddin [9/SEP/16] Start
							else
							{
								if("A".equals(editFlag) )  
								{
									System.out.println("Olnly Add Mode editFlag ["+editFlag+"]");
									if(errCode == null || errCode.trim().length() == 0)
									{
										custCode = genericUtility.getColumnValue("cust_code", dom);
										sql = "select count(1) from customeritem where cust_code = ? and item_code = ?";
										pstmt =  conn.prepareStatement(sql);
										pstmt.setString(1,custCode);
										pstmt.setString(2, itemCode);
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											count =  rs.getInt(1);
										}
										if(count > 0) 
										{
											errCode = "VTDUPENTRY";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}																
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
									}
								}
							}
							// Changed By Nasruddin [9/SEP/16] End
							
						}
						else if(childNodeName.equalsIgnoreCase("unit"))
						{
							unit = genericUtility.getColumnValue("unit", dom);
							sql = "select count(*) from uom where unit = ? ";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,unit);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count =  rs.getInt(1);
							}
							if(count == 0) 
							{
								errCode = "VTUOM1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}									
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						else if(childNodeName.equalsIgnoreCase("min_qty"))
						{
							minQtyStr = genericUtility.getColumnValue("min_qty", dom);
							if ( minQtyStr != null && minQtyStr.trim().length() > 0 )
							{
								minQty = Integer.parseInt(minQtyStr);
							}
							if(minQty < 0)
							{
								errCode = "VMMIN_QTY1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						else if(childNodeName.equalsIgnoreCase("integral_qty"))
						{
							integralQtyStr = genericUtility.getColumnValue("integral_qty", dom);
							if ( integralQtyStr != null && integralQtyStr.trim().length() > 0 )
							{
								//changed by Varsha V on 29-06-19 to resolve number format exception
								//integralQty =Integer.parseInt(integralQtyStr);
								integralQty = Integer.parseInt(checkInteger(integralQtyStr));
							}
							//Commented by Varsha Von 29-06-19
							//integralQty =Integer.parseInt(integralQtyStr);
							if(integralQty < 0)
							{
								errCode = "VT_QTY1MIN";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());	
							}
						}
						else if(childNodeName.equalsIgnoreCase("site_code__supp"))
						{
							siteCodeSupp = genericUtility.getColumnValue("site_code__supp", dom);    
							if( siteCodeSupp != null && siteCodeSupp.trim().length() > 0 )
							{
								sql = "select count(*) from site where site_code = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1,siteCodeSupp);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									count =  rs.getInt(1);

								}
								if(count == 0) 
								{
									errCode = "VMSITE1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
							}
						}

					}//end of for
				}
				break;
				case 2:
				{
					
				}
				break;
			}



			int errListSize = errList.size();
			int cnt = 0;
			String errFldName = null;
			if(errList != null && errListSize > 0)
			{
				for(cnt = 0; cnt < errListSize; cnt ++)
				{
					errCode = errList.get((int) cnt);
					errFldName = errFields.get((int) cnt);
					System.out.println("errCode .........." + errCode);
					errString = getErrorString(errFldName, errCode, userId);
					errorType =  errorType(conn , errCode);
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

		}// End of try

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
				if(conn != null)
				{
					conn.close();
					conn = null;
				}
				connDriver = null;
			} 
			catch(Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		errString = errStringXml.toString();
		return errString;
	}//end of validation

	// method for item change
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try
		{   
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if(xmlString2.trim().length() > 0 )
			{
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [Custitem][itemChanged( String, String )] :==>\n" + e.getMessage());
		}
		return valueXmlString;
	}

	// method for item change
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		//Declare variable
		String custCode = "";
		String custName = "";
		String itemCode = "";
		String minShelfLife = "";
		String unit = "";
		String descr = "";
		String sql = "";
		StringBuffer valueXmlString = new StringBuffer();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null ;

		ConnDriver connDriver = new ConnDriver();
		try
		{  
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;

			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");

			valueXmlString.append("<Detail1>");

			if(currentColumn.trim().equalsIgnoreCase("itm_defaultedit"))
			{
				valueXmlString.append("<item_code protect = \"1\">").append ("</item_code>");	
			}
			else if(currentColumn.trim().equalsIgnoreCase("cust_code"))
			{
				custCode = genericUtility.getColumnValue("cust_code", dom);
				sql = " select cust_name from customer where cust_code = ?";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,custCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					custName = rs.getString("cust_name");
				}
				valueXmlString.append("<cust_name>").append("<![CDATA[" + custName +"]]>").append("</cust_name>");
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;		
			}
			else if(currentColumn.trim().equalsIgnoreCase("item_code"))	
			{
				itemCode =genericUtility.getColumnValue("item_code", dom);
				if( itemCode != null && itemCode.trim().length() > 0 )
				{
					sql = " select descr,unit,min_shelf_life from item where item_code  = ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,itemCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						descr = rs.getString("descr");
						unit = rs.getString("unit");
						minShelfLife = rs.getString("min_shelf_life");
					}
					valueXmlString.append("<descr>").append("<![CDATA[" + descr + "]]>").append("</descr>");
					valueXmlString.append("<unit>").append("<![CDATA[" + unit + "]]>").append("</unit>");
					valueXmlString.append("<min_shelf_life>").append("<![CDATA[" + minShelfLife + "]]>").append("</min_shelf_life>");
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
			}
			valueXmlString.append("</Detail1>");
			valueXmlString.append("</Root>");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+ e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(rs != null)
					rs.close();
				if(pstmt != null)
					pstmt.close();
				rs = null;
				pstmt = null;
				if(conn != null)
				{
					conn.close();
					conn = null;
				}
				connDriver = null;
			}
			catch(Exception d)
			{
				d.printStackTrace();
			}			
		}
		return valueXmlString.toString();
	}	 

	private String errorType(Connection conn , String errorCode)
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
			while(rs.next())
			{
				msgType = rs.getString("MSG_TYPE");
			}			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
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
			}
		}		
		return msgType;
	}
	//Method added by Varsha V on 29-06-19
	private String checkInteger(String input)
	{
		return (input == null || input.trim().length() ==0)? "0" : input;
	}
}  


