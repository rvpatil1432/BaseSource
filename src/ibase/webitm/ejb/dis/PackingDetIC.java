

/********************************************************
	Title : PackingDetIC
	Date  : 04/10/11
	Developer: Kunal Mandhre

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

public class PackingDetIC extends ValidatorEJB implements PackingDetICLocal, PackingDetICRemote
{
	
	E12GenericUtility genericUtility = new E12GenericUtility();
	//GenericUtility genericUtility = GenericUtility.getInstance();

	//method for validation
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = "";
		System.out.println("wfValdata() called for PackingDetIC");
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			if (xmlString != null && xmlString.trim().length() > 0 )
			{
				dom = parseString(xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0 )
			{
				dom1 = parseString(xmlString1);
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0 )
			{
				dom2 = parseString(xmlString2);
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
		String countCode = "";
		String packCode = "";
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
				//if(childNodeName.equalsIgnoreCase("pack_code"))
				//{
					//packCode = checkNull(genericUtility.getColumnValue("pack_code", dom));
					//if(packCode == null || packCode.trim().length() == 0)
					//{
						//errCode = "VMPACKCD";
						//errList.add(errCode);
						//errFields.add(childNodeName.toLowerCase());
					//}
					/*else if(editFlag != null && editFlag.equalsIgnoreCase("A") && packCode != null && packCode.trim().length() > 0 )
					{
						sql = "select count(*) from packing_det where pack_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,countCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt =  rs.getInt(1);

						}
						if(cnt > 0) 
						{
							errCode = "VMPKDDP";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}*/
				//}
				if(childNodeName.equalsIgnoreCase("count_code"))
				{    
					countCode = genericUtility.getColumnValue("count_code", dom);
					/* Comment By Nasruddin 20-sep-16 START
					if(countCode == null || countCode.trim().length() == 0)
					{
						errCode = "VMCOUNTCD ";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
					else Comment By Nasruddin 20-sep-16  END */
					
					//if(countCode != null && countCode.trim().length() > 0 )
					//{
						sql = "select count(*) from country where count_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,countCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cnt =  rs.getInt(1);

						}
						if(cnt == 0) 
						{
							errCode = "VMCONT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					//}
				}

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
	}//end of validation

	// method for item change
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		System.out.println("itemChanged() called for PackingDetIC");
		String valueXmlString = "";
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
			System.out.println("Exception : [PackingDetIC][itemChanged( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return valueXmlString;
	}

	// method for item change
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		String packCode = "";
		String countCode = "";
		String descr = "";
		String childNodeName = null;
		String sql = "";
		StringBuffer valueXmlString = new StringBuffer();
		int ctr = 0;
		int currentFormNo = 0;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		//changed by nasruddin 07-10-16
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility = new E12GenericUtility();
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

			parentNodeList = dom.getElementsByTagName("Detail1");
			parentNode = parentNodeList.item(0);
			childNodeList = parentNode.getChildNodes();
			valueXmlString.append("<Detail1>");
			int childNodeListLength = childNodeList.getLength();
			do
			{   
				childNode = childNodeList.item(ctr);
				childNodeName = childNode.getNodeName();
				ctr ++;
			}while(ctr < childNodeListLength  && ! childNodeName.equals(currentColumn));

			if(currentColumn.trim().equalsIgnoreCase("itm_default"))
			{
				packCode = genericUtility.getColumnValue("pack_code", dom);
				//changes by Prasad on 16/08/18 [pack code editable] START
				if( packCode != null && packCode.trim().length() > 0 )
				{
					valueXmlString.append("<pack_code protect = \"1\">").append("<![CDATA[" +packCode + "]]>").append ("</pack_code>");
				}
				else 
				{
					valueXmlString.append("<pack_code>").append("<![CDATA[]]>").append("</pack_code>");
				}
				//changes by Prasad on 16/08/18 [pack code editable] END
			}
			else if(currentColumn.trim().equalsIgnoreCase("count_code"))
			{
				countCode = genericUtility.getColumnValue("count_code", dom);
				System.out.println("count code = "+countCode);
				if(countCode != null && countCode.trim().length() > 0 )
				{

					sql = " Select descr  from country where count_code = ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,countCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{

						descr = rs.getString(1);
						
					}
					
					//else
					//{
						//valueXmlString.append("<country_descr>").append("<![CDATA[]]>").append("</country_descr>");
					//}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
				valueXmlString.append("<country_descr>").append("<![CDATA[" + descr +"]]>").append("</country_descr>");
				//else
				//{
					//valueXmlString.append("<country_descr>").append("<![CDATA[]]>").append("</country_descr>");
				//}
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
			}			
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
			throw new ITMException(ex); //Added By Mukesh Chauhan on 05/08/19
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
