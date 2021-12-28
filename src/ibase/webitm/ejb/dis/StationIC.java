

/********************************************************
	Title : StationIC
	Date  : 16/11/11
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

public class StationIC extends ValidatorEJB implements StationICLocal, StationICRemote
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
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
		String stateCode = "";
		String bankCode = "";
		String desc = "";
		String stanCode = "";
		String childNodeName = null;
		String errString = "";
		String errCode = "";
		String userId = "";
		String sql = "";
		String errorType = "";
		int count = 0;
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
		System.out.println("Station Val Start");
		String modName = "w_station" , keyFlag = ""; // Changed By Nasruddin 21-SEP-16
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
			for(ctr = 0; ctr < childNodeListLength; ctr++)
			{
				childNode = childNodeList.item(ctr);
				childNodeName = childNode.getNodeName();
				if(childNodeName.equalsIgnoreCase("state_code"))
				{
					stateCode = checkNull(genericUtility.getColumnValue("state_code", dom));

					System.out.println("State code="+stateCode);
					/* Comment By Nasruddin 21-SEP-16 START
					if(stateCode == null || stateCode.trim().length() == 0)
					{
						errCode = "VMSTATCD2";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
					 Comment By Nasruddin 21-SEP-16 END */
					if(stateCode != null && stateCode.trim().length() > 0)
					{
						sql = "select count(*) from state where state_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,stateCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							count =  rs.getInt(1);
						}
						if(count == 0) 
						{
							errCode = "VMSTATCD2";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				}
				else if(childNodeName.equalsIgnoreCase("stan_code"))
				{
					stanCode = checkNull(genericUtility.getColumnValue("stan_code", dom));
					System.out.println("stancode"+stanCode);
					//if(stanCode == null || stanCode.trim().length() == 0)
					//{
					//errCode = "VMSTNBK";
					//errList.add(errCode);
					//errFields.add(childNodeName.toLowerCase());
					//}
					// Changed By Nasruddin 21-SEP-16 START
					sql = "SELECT KEY_FLAG  FROM TRANSETUP WHERE TRAN_WINDOW = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, modName);
					rs = pstmt.executeQuery();
					if( rs.next() )
					{
						keyFlag = rs.getString("KEY_FLAG");

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					keyFlag = keyFlag == null ? "M" : keyFlag.trim();
					if( keyFlag.equalsIgnoreCase("M")  && (stanCode == null || stanCode.trim().length() == 0))
					{
						errCode = "VMSTAN1";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
					else
					{
						if(editFlag.equalsIgnoreCase("A")) 
						{
							sql = "select count(*) from station where stan_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,stanCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count =  rs.getInt(1);
								System.out.println("COUNT = "+count);

							}
							if(count > 0)  
							{
								errCode = "VMDUPL1";
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
				/* Comment BY Nasruddin 21-sep-16 start
				else if(childNodeName.equalsIgnoreCase("descr"))
				{
					desc = checkNull(genericUtility.getColumnValue("descr", dom));
					if(desc == null || desc.trim().length() == 0)
					{
						errCode = "VEADN3";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
				}
				Comment BY Nasruddin 21-sep-16 end */
				else if(childNodeName.equalsIgnoreCase("bank_code"))
				{
					bankCode = checkNull(genericUtility.getColumnValue("bank_code", dom));
					System.out.println("bank code="+bankCode);
					
					/* Comment BY Nasruddin 21-sep-16 start
					if(bankCode == null || bankCode.trim().length() == 0)
					{
						errCode = "VMBANK1";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
					Comment BY Nasruddin 21-sep-16 end */
					
					if(bankCode != null && bankCode.trim().length() > 0)
					{
						sql = "select count(*) from bank where bank_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,bankCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							count =  rs.getInt(1);
						}
						if(count == 0) 
						{
							errCode = "VMBANK1";
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
			int errListSize = errList.size();
			count = 0;
			String errFldName = null;
			if(errList != null && errListSize > 0)
			{
				for(count = 0; count < errListSize; count ++)
				{
					errCode = errList.get(count);
					errFldName = errFields.get(count);
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

	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
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
			System.out.println("Exception : [Station][itemChanged( String, String )] :==>\n" + e.getMessage());
		}
		return valueXmlString;
	}

	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		String stateCode = "";
		String bankCode = "";
		String descr = "";
		String childNodeName = null;
		String sql = "";
		StringBuffer valueXmlString = new StringBuffer();
		int ctr = 0;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		//GenericUtility genericUtility = GenericUtility.getInstance();
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
			if(currentColumn.trim().equalsIgnoreCase("state_code"))
			{
				stateCode = checkNull(genericUtility.getColumnValue("state_code", dom));
				System.out.println("436 StateCode"+stateCode);
				sql = "select descr from state where state_code = ?";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,stateCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					descr = rs.getString(1);
					
				}
				valueXmlString.append("<state_descr>").append("<![CDATA[" + descr +"]]>").append("</state_descr>");
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}
			else if(currentColumn.trim().equalsIgnoreCase("bank_code"))
			{
				bankCode = checkNull(genericUtility.getColumnValue("bank_code", dom));		
				System.out.println("454 BankCode"+bankCode);
				descr="";
				sql = "select bank_name  from bank where bank_code = ?";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,bankCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					descr = rs.getString(1);
					
				}
				valueXmlString.append("<bank_name>").append("<![CDATA[" + descr +"]]>").append("</bank_name>");
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
				if(conn != null)
				{
					if(pstmt != null)
						pstmt.close();
					if(rs != null)
						rs.close();
					rs = null;
					pstmt = null;
					conn.close();
					conn = null;
				}
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
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
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
