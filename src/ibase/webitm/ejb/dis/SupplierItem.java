
/********************************************************
	Title : SupplierItem
	Date  : 21/03/2012
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
public class SupplierItem extends ValidatorEJB implements SupplierItemLocal,SupplierItemRemote {
		//changed by nasruddin 05-10-16
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
		String siteCode = "";
		String suppCode = "";
		String itemSer = "";
		String userId = "";
		String errCode="";
		String errorType = "";
		String childNodeName = null;
		String errString = "", lockGroup = "", sql = "";
		int ctr=0, count = 0;
		int childNodeListLength;
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
				if(childNodeName.equalsIgnoreCase("supp_code"))
				{   
					suppCode = genericUtility.getColumnValue("supp_code", dom);
					siteCode = genericUtility.getColumnValue("site_code", dom);
					errCode = fn_supplier(siteCode,suppCode,conn);
					//errList.add(errCode);
					//errFields.add(childNodeName.toLowerCase());
					if( errCode != null && errCode.trim().length() > 0)
					{
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());

					}
				}
				else if(childNodeName.equalsIgnoreCase("item_ser"))
				{    
					itemSer = genericUtility.getColumnValue("item_ser", dom);
					errCode = fn_itemser(itemSer,conn);
					//Changed By Nasruddin 03-10-16 
					//errList.add(errCode);
					//errFields.add(childNodeName.toLowerCase());
					if( errCode != null && errCode.trim().length() >0)
					{
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
				}
				//Changed By Nasruddin 22-SEP-16 START
				else if(childNodeName.equalsIgnoreCase("lock_group"))
				{    
					lockGroup = genericUtility.getColumnValue("lock_group", dom);
					
					if(lockGroup != null && lockGroup.trim().length() > 0)
					{
						sql = "SELECT COUNT(1) FROM LOCK_GROUP Where LOCK_GROUP = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, lockGroup);
						rs = pstmt.executeQuery();
						if( rs.next() )
						{
							count = rs.getInt(1);
							
						}
						pstmt.close();
						pstmt = null;
						rs.close();
						rs = null;
						
						if( count ==  0 )
						{
							errCode = "VMLOCKCODE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							
						}
					}
				}
				//Changed By Nasruddin 22-SEP-16 END
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
			System.out.println("xmlString"+xmlString);
			dom = parseString(xmlString);
			System.out.println("xmlString1"+xmlString1);
			dom1 = parseString(xmlString1);
			if(xmlString2.trim().length() > 0 )
			{
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [ItemLot][itemChanged( String, String )] :==>\n" + e.getMessage());
		}
		return valueXmlString;
	}

	// method for item change
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		//Declare variable
		String itemSer = "";
		String suppCode = "";
		String suppName = "";
		String contPers = "";
		String unit = "";
		String leadTime = "";
		String itemCode = "";
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

			if(currentColumn.trim().equalsIgnoreCase("itm_default"))
			{
				//Comment By Nasruddin 03-10-16
				//itemCode = genericUtility.getColumnValue("tran_id", dom);
				itemCode = genericUtility.getColumnValue("item_code", dom);
				valueXmlString.append("<item_code>").append("<![CDATA[" + itemCode +"]]>").append("</item_code>");
				sql = " select item_ser,unit,pur_lead_time from item where item_code = ?";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,itemCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					itemSer = rs.getString("item_ser");
					unit = rs.getString("unit");
					leadTime = rs.getString("pur_lead_time");
				}
				valueXmlString.append("<item_ser>").append("<![CDATA[" + itemSer +"]]>").append("</item_ser>");
				valueXmlString.append("<unit>").append("<![CDATA[" + unit +"]]>").append("</unit>");
				valueXmlString.append("<pur_lead_time>").append("<![CDATA[" + leadTime +"]]>").append("</pur_lead_time>");
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;		
			}
			else if(currentColumn.trim().equalsIgnoreCase("supp_code"))	
			{
				suppCode =genericUtility.getColumnValue("supp_code", dom);
				sql = " Select supp_name,cont_pers from supplier where supp_code = ?";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,suppCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					suppName = rs.getString("supp_name");
					contPers = rs.getString("cont_pers");
				}
				valueXmlString.append("<supp_name>").append("<![CDATA[" + suppName + "]]>").append("</supp_name>");
				valueXmlString.append("<cont_pers>").append("<![CDATA[" + contPers + "]]>").append("</cont_pers>");
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
	public String fn_supplier( String siteCode,String suppCode, Connection conn)
	{
		String sql = "";
		String errCode = "";
		String varValue = "";
		String blackList = "";
		int count = 0;
		PreparedStatement pstmt = null ;
		ResultSet rs = null;
		try
		{
			sql = "select var_value from disparm where prd_code = '999999' and var_name = 'SITE_SPECIFIC_SUPP'";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				varValue = rs.getString(1) == null ?"":rs.getString(1);
			}
			// Changed by Sneha on 12-09-2016, for Closing the Open Cursor [Start]
			if ( pstmt != null )
			{
				pstmt.close();
				pstmt = null;
			}
			if ( rs != null )
			{
				rs.close();
				rs = null;
			}
			System.out.println("varValue  ["+varValue+"]");
			// Changed by Sneha on 12-09-2016, for Closing the Open Cursor [End]
			if("Y".equalsIgnoreCase(varValue))
			{
				sql = "select count(*) from site_supplier where site_code = ? and supp_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,siteCode);
				pstmt.setString(2,suppCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					count = rs.getInt(1);
				}
				if(count== 0)
				{
					errCode = "VTSUPP2";
				}
				sql = "select count(*) from supplier where supp_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,suppCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					count = rs.getInt(1);
				}
				if(count == 0)
				{
					errCode = "VTSUPP1";
				}
				sql = "select case when  black_list is null then 'N' else black_list end from supplier  where supp_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,suppCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					blackList = rs.getString("black_list");
				}
				if("Y".equalsIgnoreCase(blackList))
				{
					errCode = "VTSUPPBL";;
				}

			}
		}
		catch (Exception e) {
			// TODO: handle exception
		}
		return errCode;
	}
	public String fn_itemser(String itemSer, Connection conn)
	{
		String errCode = "";
		String sql = "";
		int count = 0;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try{
			sql = "select count (*) from itemser where item_ser = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, itemSer);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				count = rs.getInt(1);
			}
			if(count == 0)
			{
				errCode = "VTITEMSER1";
			}
		}
		catch(Exception e){
			// TODO: handle exception
		}
		return errCode;
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
}  


