

/********************************************************
	Title : ItemTypeIC
	Date  : 13/4/12
	Developer: Neelam Salunkhe

 ********************************************************/ 

package ibase.webitm.ejb.dis;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
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

public class ItemTypeIC extends ValidatorEJB implements ItemTypeICLocal, ItemTypeICRemote
{
	//changed by nasruddin 07-10-16
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
		String itemType = "";
		String routeCode = "";
		String siteCode = "";
		String itemCode = "";
		String unit = "";
		String unitStd = "";
		String taxClass = "";
		String taxChap = "";
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
		System.out.println("ItemType Val Start");
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
			/* Comment By Nasruddin 19-SEP-16
			 * if(editFlag.equalsIgnoreCase("A"))
			{*/	
				if(childNodeName.equalsIgnoreCase("item_type"))
				{
					itemType = checkNull(genericUtility.getColumnValue("item_type", dom));
					System.out.println("Item Type="+itemType);
					if(itemType == null || itemType.trim().length() == 0)
					{
						errCode = "VMIMC";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
					else if(itemType != null && itemType.trim().length() > 0 && "A".equals(editFlag))
					{
						sql = "select count(*) from item_type where item_type = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,itemType);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							count =  rs.getInt(1);
						}
						if(count >= 1) 
						{
							errCode = "VMUMC";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				}
			//}	
			/* Comment By Nasruddin 19-SEP-16 START
				else if(childNodeName.equalsIgnoreCase("unit"))
				{
					unit = checkNull(genericUtility.getColumnValue("unit", dom));
					System.out.println("Unit code="+unit);
					if(unit != null && unit.trim().length() > 0)
					{
						sql = "select count(*) from uom where unit = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,unit);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							count =  rs.getInt(1);
						}
						if(count <= 0) 
						{
							errCode = "VMITPEUNIT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				}
				else if(childNodeName.equalsIgnoreCase("unit__std"))
				{
					unitStd = checkNull(genericUtility.getColumnValue("unit__std", dom));
					System.out.println("unit__std="+unitStd);
					if(unitStd != null && unitStd.trim().length() > 0)
					{
						sql = "select count(*) from uom where unit = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,unitStd);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							count =  rs.getInt(1);
						}
						if(count <= 0) 
						{
							errCode = "VMIPUNSTD";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				}
				else if(childNodeName.equalsIgnoreCase("tax_class"))
				{
					taxClass = checkNull(genericUtility.getColumnValue("tax_class", dom));
					System.out.println("TaxClass="+taxClass);
					if(taxClass != null && taxClass.trim().length() > 0)
					{
						sql = "select count(*) from taxclass where tax_class = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,taxClass);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							count =  rs.getInt(1);
						}
						if(count <= 0) 
						{
							errCode = "VMTAXCLASS";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				}
				else if(childNodeName.equalsIgnoreCase("tax_chap"))
				{
					taxChap = checkNull(genericUtility.getColumnValue("tax_chap", dom));
					System.out.println("TaxChap="+taxChap);
					if(taxChap != null && taxChap.trim().length() > 0)
					{
						sql = "select count(*) from taxchap where tax_chap = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,taxChap);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							count =  rs.getInt(1);
						}
						if(count <= 0) 
						{
							errCode = "VMTAXCHAP";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				}
				
				else if(childNodeName.equalsIgnoreCase("site_code__subctr"))
				{
					siteCode = checkNull(genericUtility.getColumnValue("site_code__subctr", dom));
					System.out.println("Site code="+siteCode);
					if(siteCode != null && siteCode.trim().length() > 0)
					{
						sql = "select count(*) from site where site_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,siteCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							count =  rs.getInt(1);
						}
						if(count <= 0) 
						{
							errCode = "VMSIECOD";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				}
				else if(childNodeName.equalsIgnoreCase("item_code__rate"))
				{
					itemCode = checkNull(genericUtility.getColumnValue("item_code__rate", dom));
					System.out.println("Route code="+routeCode);
					if(itemCode != null && itemCode.trim().length() > 0)
					{
						sql = "select count(*) from item where item_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,itemCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							count =  rs.getInt(1);
						}
						if(count <= 0) 
						{
							errCode = "VMITMCOD";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				} Comment By Nasruddin 19-SEP-16 END*/
				else if(childNodeName.equalsIgnoreCase("route_code"))
				{
					routeCode = checkNull(genericUtility.getColumnValue("route_code", dom));
					System.out.println("Route code="+routeCode);
					if(routeCode != null && routeCode.trim().length() > 0)
					{
						sql = "select count(*) from route where route_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,routeCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							count =  rs.getInt(1);
						}
						if(count <= 0) 
						{
							errCode = "VTROUT1";
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
}	
