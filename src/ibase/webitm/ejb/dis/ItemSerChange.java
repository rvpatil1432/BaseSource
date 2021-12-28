
/********************************************************
	Title : ItemSerChange
	Date  : 28/03/2012
	Developer: Navanath Nawale

 ********************************************************/

package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

import java.text.SimpleDateFormat;
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
public class ItemSerChange extends ValidatorEJB implements ItemSerChangeLocal,ItemSerChangeRemote {
	
	//changed by nasruddin 07-10-16
	E12GenericUtility genericUtility = new E12GenericUtility();
	//GenericUtility genericUtility = GenericUtility.getInstance();
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
		String itemCode ="";
		String itemSer = "";
		String itemSerOld = "";
		String effDate = "";
		String userId = "";
		String sql="";
		String errCode="";
		String errorType = "";
		String childNodeName = null;
		String errString = "";
		java.util.Date maxDate = null;
		java.util.Date effPdate = null;
		int ctr=0;
		int childNodeListLength;
		long count = 0;
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement pstmt = null ;
		ResultSet rs = null;
		ConnDriver connDriver = new ConnDriver();
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>"); 
		try       
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");

			parentNodeList = dom.getElementsByTagName("Detail1");
			parentNode = parentNodeList.item(0);
			childNodeList = parentNode.getChildNodes();
			childNodeListLength = childNodeList.getLength();
			for(ctr = 0; ctr < childNodeListLength; ctr ++)
			{
				childNode = childNodeList.item(ctr);
				childNodeName = childNode.getNodeName();
				if(childNodeName.equalsIgnoreCase("item_ser"))
				{    
					itemSer = genericUtility.getColumnValue("item_ser", dom);
					itemSerOld = genericUtility.getColumnValue("item_ser__old", dom);
					sql = "select count(*) from itemser where item_ser = ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,itemSer);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						count =  rs.getInt(1);
					}
					if(count == 0) 
					{
						errCode = "VTITEMSER1";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
						System.out.println("Error is VTITEMSER1" +errCode);
					} 
					else if((itemSer.equals(itemSerOld)))
					{
						errCode = "VTITEMSER2";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
					rs.close();
					rs = null;             
					pstmt.close();
					pstmt = null;
				}
				else if(childNodeName.equalsIgnoreCase("eff_date"))
				{    
					effDate = genericUtility.getColumnValue("eff_date", dom);
					effPdate = getDateValue(effDate);
					itemCode = genericUtility.getColumnValue("item_code", dom);
					sql = "select max(eff_date) as maxdate from itemser_change where item_code = ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,itemCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{    
						maxDate = getDateValue(rs.getDate("maxdate"));
						System.out.println("Effective Date :"+effPdate);
						System.out.println("Max Date :"+maxDate);
					  if( effDate != null && maxDate != null )//Changed By Nasruddin 29-SEp-16
					  {
						  if( effPdate.before(maxDate)) 
							{
								errCode = "VTLVECD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							} 
					  }
																						
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
			}//end of for
		

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
		System.out.println("Exception : [ItemSerChange][itemChanged( String, String )] :==>\n" + e.getMessage());
		throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
	}
	return valueXmlString;
}

// method for item change
public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
{
	//Declare variable
	String itemSer = "";
	String itemSer1 = "";
	String effDate = "";
	String itemCode = "";
	String descr = "";
	String descr1 = "";
	String descrItm = "";
	String sql = "";
	String sql1 = "";
	String sql2 = "";
	String sql3 = "";
	java.util.Date effPdate = null;
	StringBuffer valueXmlString = new StringBuffer();
	Connection conn = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null ;
	String sysDate = "";
	ConnDriver connDriver = new ConnDriver();
	try
	{  
		//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
		conn.setAutoCommit(false);
		connDriver = null;
		//Changed By Nasruddin 04-10-16 Start
		//SimpleDateFormat dbDateFormat = new SimpleDateFormat("dd/MM/yy");
		Calendar currentDate = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
		 sysDate = sdf.format(currentDate.getTime());
		
		//Changed By Nasruddin 04-10-16 End
		valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
		valueXmlString.append(editFlag).append("</editFlag> </header>");
		valueXmlString.append("<Detail1>");
			if(currentColumn.trim().equalsIgnoreCase("itm_default"))
			{   //Comment By Nasruddin 04-10-16
				//effDate = genericUtility.getColumnValue("eff_date", dom);
				//effPdate = dbDateFormat.parse(effDate);
				System.out.println("sysDate ["+sysDate+"]");
				valueXmlString.append("<eff_date>").append("<![CDATA[" + sysDate +"]]>").append("</eff_date>");
			}
			else if(currentColumn.trim().equalsIgnoreCase("item_code"))	
			{
				itemCode =genericUtility.getColumnValue("item_code", dom);
				//Comment By Nasruddin Start 4-10-16 
				//sql = " select descr from item where item_code  = ? " ;
				sql = " select item_ser from itemser_change where item_code  = ?  and valid_upto is null";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,itemCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					itemSer1 = rs.getString("item_ser");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(itemSer1 == null  || itemSer1.trim().length() == 0)
				{
					sql = " select item_ser from item where item_code  = ? " ;
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,itemCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						itemSer1 = rs.getString("item_ser");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

				}
				sql = " select descr from item where item_code  = ? " ;
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,itemCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					descr = rs.getString("descr");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
				sql = " select descr from itemSer where item_ser  = ? " ;
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,itemSer1);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					descr1 = rs.getString("descr");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
				valueXmlString.append("<item_descr>").append("<![CDATA[" + descr + "]]>").append("</item_descr>");
				valueXmlString.append("<item_ser__old>").append("<![CDATA[" + itemSer1 + "]]>").append("</item_ser__old>");
				valueXmlString.append("<descr>").append("<![CDATA[" + descr1 + "]]>").append("</descr>");
			
				
				/*Comment By Nasruddin Start 04-10-16
				 * sql = " select item_ser from itemser_change where item_code  = ?  and valid_upto is null";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,itemCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					itemSer = rs.getString(1);	
					System.out.println("IN ITEM_CHANGE ITEM_CODE"+itemSer);
					if(itemSer == null || itemSer.trim().length() == 0 )
					{
						sql1 = " select item_ser from item where item_code  = ?";
						pstmt =  conn.prepareStatement(sql1);
						pstmt.setString(1,itemCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							itemSer1 = rs.getString("item_ser");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}

					sql3 = " select descr from itemser where item_ser  = ?";
					pstmt =  conn.prepareStatement(sql3);
					pstmt.setString(1,itemSer1);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						descr1 = rs.getString("descr");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}/*Comment By Nasruddin End 04-10-16
				*/
				
			}
			else if(currentColumn.trim().equalsIgnoreCase("item_ser"))	
			{
				itemSer =genericUtility.getColumnValue("item_ser", dom);
				sql = " select descr from itemser where item_ser  = ?";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,itemSer);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					descr = rs.getString("descr");
				}
				valueXmlString.append("<itemser_descr>").append("<![CDATA[" + descr + "]]>").append("</itemser_descr>");
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
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
		while(rs.next())
		{
			msgType = rs.getString("MSG_TYPE");
		}			
	}
	catch(Exception ex)
	{
		ex.printStackTrace();
		throw new ITMException(ex); //Added By Mukesh Chauhan on 07/08/19
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
private Date getDateValue(String colunm) {
	Date date = null;
	SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yy");
	try
	{
		date = new java.sql.Date(format1.parse( format1.format(format1.parse(colunm))).getTime());
	}
	catch(Exception exc)
	{	
		date = null;
	}
	System.out.println("Date is ["+date+"]");
	return date;
} 

private Date getDateValue(Date date) {

	SimpleDateFormat format2 = new SimpleDateFormat("dd/MMM/yyyy");
	try
	{
		date = new java.sql.Date(format2.parse( format2.format(date)).getTime());
	}
	catch(Exception exc)
	{	
		date = null;
	}
	System.out.println("Date is ["+date+"]");
	return date;
}
}  


