/********************************************************
	Title : InvstatIC
	Date  : 23/09/2016
	Developer: Nasruddin Khan
 ********************************************************/

package ibase.webitm.ejb.dis;

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import javax.ejb.Stateless;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Stateless
public class InvRetrIC extends ValidatorEJB implements InvstatICLocal, InvstatICRemote 
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
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString("<Root>" + xmlString2 + "</Root>");
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
		int ctr = 0;
		int childNodeListLength;
		int currentFormNo = 0;
		String childNodeName = null;
		String errString = "";
		String errCode = "";
		String sql = "";
		String errFldName = "";
		String userId = "";
		String errorType = "";
		String invStat = "";
		String refSer = "";
		long cnt = 0;
		int cntv = 0;
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement pstmt = null ;
		ResultSet rs = null;
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>"); 
		try
		{
			conn = getConnection();
			if (objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			switch (currentFormNo)
			{
			case 1:
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				for(ctr = 0; ctr < childNodeListLength; ctr ++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if(childNodeName.equalsIgnoreCase("inv_stat"))
					{
						cntv = 0;
						sql = " SELECT COUNT(1) from INVSTAT WHERE INV_STAT = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, invStat);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cntv = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if(cntv == 0)
						{
							errCode = "VMINVSTAT1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if(childNodeName.equalsIgnoreCase("ref_ser"))
					{
						refSer = checkNull(genericUtility.getColumnValue("ref_ser", dom));
						cntv = 0;
						sql = " SELECT COUNT(1) from REFSER WHERE REF_SER = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, refSer);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							cntv = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if(cntv == 0)
						{
							errCode = "VMREFSER";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}

					}
				} 						
			}
			int errListSize = errList.size();
			cnt = 0;
			if(errList != null && errListSize > 0)
			{
				for(cnt = 0; cnt < errListSize; cnt ++)
				{
					errCode = errList.get((int) cnt);
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
	private String errorType(Connection conn , String errorCode) throws ITMException
	{
		String msgType = "";
		PreparedStatement pstmt = null ; 
		ResultSet rs = null;
		try
		{			
			String sql = " SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ? ";
			pstmt = conn.prepareStatement(sql);			
			pstmt.setString(1, errorCode);			
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
			System.out.println("Exception : [TransporterIC][itemChanged( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		return valueXmlString;
	} 
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Connection conn = null;
		String sql = "",  refSer = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		StringBuffer valueXmlString = new StringBuffer();
		int currentFormNo = 0;
		String  descr = "";
		try 
		{
			conn = getConnection();
			conn.setAutoCommit(false);
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			if (objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			switch (currentFormNo) 
			{
			case 1 :
				valueXmlString.append("<Detail1>");
				System.out.println("currentColumn: " + currentColumn);
				if (currentColumn.equalsIgnoreCase("ref_ser"))
				{
					refSer = checkNull(genericUtility.getColumnValue("ref_ser", dom));
					sql = "SELECT DESCR FROM REFSER WHERE REF_SER  = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, refSer);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						descr = rs.getString("DESCR");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
				valueXmlString.append("<refser_descr><![CDATA["+ descr + "]]></refser_descr>");
				valueXmlString.append("</Detail1>\r\n");
				break;
			}
		}
		catch(Exception e)
		{
			System.out.println("::" + this.getClass().getSimpleName() + "::::" + e.getMessage());
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		finally 
		{
			try 
			{
				if( conn != null)
				{
					if( pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}
					if( rs != null)
					{
						rs.close();
						rs = null;
					}
					conn.close();
				}
				conn = null; 		
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
		valueXmlString.append("</Root>\r\n");
		System.out.println("ValueXmlString:::::" + valueXmlString.toString());
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
			return str.trim() ;
		}
	}
}
