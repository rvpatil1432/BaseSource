
/********************************************************
	Title : DeptRate
	Date  : 7/04/2012
	Developer: Navanath Nawale

 ********************************************************/

package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.ejb.Stateless;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Stateless 
public class DeptRate extends ValidatorEJB implements DeptRateLocal,DeptRateRemote {
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
			System.out.println("Val Xtraparam :: " + xtraParams );
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
		String itemCode = "";
		String validUpto = "";
		String effFrom = "";
		String userId = "";
		String sql="";
		String errCode="";
		String errorType = "";
		String childNodeName = null;
		String errString = "";
		java.util.Date validUpto1 = null;
		java.util.Date effFrom1 = null;
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
				if(childNodeName.equalsIgnoreCase("item_code__depb"))
				{    
					itemCode = genericUtility.getColumnValue("item_code__depb", dom);
					//if(itemCode != null && itemCode.trim().length() > 0) Comment by nasruddin [16-sep-16]
					//{
						sql = "select count(*) from item where item_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,itemCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							count =  rs.getInt(1);															
						}
						if(count == 0) 
						{
							errCode = "VMITEM1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}	
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					//}
				}

				else if(childNodeName.equalsIgnoreCase("valid_upto"))
				{   
					validUpto = genericUtility.getColumnValue("valid_upto", dom);
					effFrom = genericUtility.getColumnValue("eff_from", dom);
					validUpto1 = getDateValue(validUpto);
					effFrom1 = getDateValue(effFrom);
					if(validUpto1.before(effFrom1))
					{
						errCode = "VMVALIDDT";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
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
			System.out.println("Exception : [DeptRate][itemChanged( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		return valueXmlString;
	}

	// method for item change
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		//Declare variable
		String itemCode = "";
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
				valueXmlString.append("<eff_from>").append("<![CDATA[" + new java.util.Date() +"]]>").append("</eff_from>");
				valueXmlString.append("<valid_upto>").append("<![CDATA[" + new java.util.Date() +"]]>").append("</valid_upto>");
			}
			else if(currentColumn.trim().equalsIgnoreCase("item_code__depb"))	
			{
				itemCode =genericUtility.getColumnValue("item_code__depb", dom);
				sql = " select descr from item where item_code = ?";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,itemCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					descr = rs.getString("descr");
				}
				valueXmlString.append("<item_descr>").append("<![CDATA[" + descr + "]]>").append("</item_descr>");
				valueXmlString.append("<custom_descr>").append("<![CDATA[" + descr + "]]>").append("</custom_descr>");
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
			System.out.println("DATE EXCEPTION:--->" +exc);
		}
		System.out.println("Date is ["+date+"]");
		return date;
	} 
}  




