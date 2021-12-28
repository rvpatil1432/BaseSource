

/********************************************************
	Title : DistributionRouteIC
	Date  : 14/04/12
	Developer: Kunal Mandhre

 ********************************************************/

package ibase.webitm.ejb.dis;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.GenericUtility;
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
public class DistributionRouteIC extends ValidatorEJB implements DistributionRouteICLocal,DistributionRouteICRemote
{
	//Comment By Nasruddin 07-10-16
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
		String StanCodeFrom = "";
		String StanCodeTo = "";
		String currCode = "";
		String tranCode = "";
		String descr = "";
		String distRoute = "";
		String childNodeName = null;
		String errString = "";
		String errCode = "";
		String userId = "";
		String sql = "";
		String errorType = "";
		int count = 0;
		int ctr=0;
		int currentFormNo = 0;
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
			if(objContext != null && objContext.trim().length() > 0)
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
				for(ctr = 0; ctr < childNodeListLength; ctr ++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();

					/* Comment By Nasruddin [16-sep-19] START
					if(childNodeName.equalsIgnoreCase("dist_route"))
					{
						distRoute = checkNull(genericUtility.getColumnValue("dist_route", dom));
						//if(distRoute == null || distRoute.trim().length() == 0)
						//{

						//		errCode = "VTDISRTBK";
						//		errList.add(errCode);
						//		errFields.add(childNodeName.toLowerCase());

						//}
						if(editFlag.equalsIgnoreCase("A") && distRoute != null && distRoute.trim().length() > 0)
						{
							sql = "select count(*)  from distroute where dist_route = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,distRoute);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count =  rs.getInt(1);
							}
							if(count > 0) 
							{
								errCode = "VTDISRTDB";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}

					}
					else if(childNodeName.equalsIgnoreCase("descr"))
					{
						descr = checkNull(genericUtility.getColumnValue("descr", dom));
						if(descr == null || descr.trim().length() == 0)
						{

								errCode = "VMDESCR";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());

						}
					} Comment By Nasruddin [16-sep-19] END */
					if(childNodeName.equalsIgnoreCase("stan_code__fr"))
					{
						StanCodeFrom = checkNull(genericUtility.getColumnValue("stan_code__fr", dom));
						/* Comment By Nasruddin [16-sep-19] Start
						if(StanCodeFrom == null || StanCodeFrom.trim().length() == 0)
						{
							errCode = "VMSTANCOD ";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						Comment By Nasruddin [16-sep-19] End */
						if(StanCodeFrom != null && StanCodeFrom.trim().length() > 0)
						{
							sql = "select count(*)  from station where stan_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,StanCodeFrom);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count =  rs.getInt(1);
							}
							if(count == 0) 
							{
								errCode = "VTSTAN1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					else if(childNodeName.equalsIgnoreCase("stan_code__to"))
					{
						StanCodeTo = checkNull(genericUtility.getColumnValue("stan_code__to", dom));
						/* Comment By Nasruddin [16-sep-19] Start
						if(StanCodeTo == null || StanCodeTo.trim().length() == 0)
						{
							errCode = "VMSTANCOD ";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						Comment By Nasruddin [16-sep-19] End */
						if(StanCodeTo != null && StanCodeTo.trim().length() > 0)
						{
							sql = "select count(*)  from station where stan_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,StanCodeTo);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count =  rs.getInt(1);
							}
							if(count == 0) 
							{
								errCode = "VTSTAN1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					else if(childNodeName.equalsIgnoreCase("curr_code"))
					{
						currCode = checkNull(genericUtility.getColumnValue("curr_code", dom));
						if(currCode == null || currCode.trim().length() == 0)
						{
							errCode = "VMCURRCD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else if(currCode != null && currCode.trim().length() > 0)
						{
							sql = "select count(*) from currency where curr_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,currCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count =  rs.getInt(1);
							}
							if(count == 0) 
							{
								errCode = "VTCURRCD1";
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
				break;
			case 2 :
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				for(ctr = 0; ctr < childNodeListLength; ctr ++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if(childNodeName.equalsIgnoreCase("stan_code__fr"))
					{
						StanCodeFrom = checkNull(genericUtility.getColumnValue("stan_code__fr", dom));
						/* Comment By Nasruddin [16-sep-19] Start
						if(StanCodeFrom == null || StanCodeFrom.trim().length() == 0)
						{
							errCode = "VMSTANCOD ";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());

						}Comment By Nasruddin [16-sep-19] End */
						if(StanCodeFrom != null && StanCodeFrom.trim().length() > 0)
						{
							sql = "select count(*)  from station where stan_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,StanCodeFrom);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count =  rs.getInt(1);
							}
							if(count == 0) 
							{
								errCode = "VTSTAN1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					else if(childNodeName.equalsIgnoreCase("stan_code__to"))
					{
						StanCodeTo = checkNull(genericUtility.getColumnValue("stan_code__to", dom));
						/* Comment By Nasruddin [16-sep-19] Start
						if(StanCodeTo == null || StanCodeTo.trim().length() == 0)
						{
							errCode = "VMSTANCOD ";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}Comment By Nasruddin [16-sep-19] End */
						if(StanCodeTo != null && StanCodeTo.trim().length() > 0)
						{
							sql = "select count(*)  from station where stan_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,StanCodeTo);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count =  rs.getInt(1);
							}
							if(count == 0) 
							{
								errCode = "VTSTAN1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					else if(childNodeName.equalsIgnoreCase("tran_code"))
					{
						tranCode = checkNull(genericUtility.getColumnValue("tran_code", dom));
						if(tranCode == null || tranCode.trim().length() == 0)
						{
							errCode = "VMTRANCD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else if(tranCode != null && tranCode.trim().length() > 0)
						{
							sql = "select count(*) from transporter where tran_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,tranCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count =  rs.getInt(1);
							}
							if(count == 0) 
							{
								errCode = "VTTRANCD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					else if(childNodeName.equalsIgnoreCase("curr_code"))
					{
						currCode = checkNull(genericUtility.getColumnValue("curr_code", dom));
						if(currCode != null && currCode.trim().length() > 0)
						{
							sql = "select count(*) from currency where curr_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,currCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count =  rs.getInt(1);
							}
							if(count == 0) 
							{
								errCode = "VTCURRCD1";
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
				break;
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
		}//end try
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
			System.out.println("Exception : [DistributionRouteIC][itemChanged( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		return valueXmlString;
	}

	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		String stanCodeFr = "";
		String stanCodeTo = "";
		String currCode = ""; 
		String descr = "";
		String distRoute = "";
		String tranCode = "";
		String childNodeName = null;
		String sql = "";
		StringBuffer valueXmlString = new StringBuffer();
		int ctr = 0;
		int lineNo = 0;
		int currentFormNo = 0;
		int childNodeListLength = 0;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		// Comment by Nasruddin 07-10-16 
		E12GenericUtility genericUtility = new E12GenericUtility();
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
			if(objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");
			switch(currentFormNo)
			{
			case 1 : 
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail1>");
				childNodeListLength = childNodeList.getLength();
				/*do
				{   
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					ctr ++;
				}while(ctr < childNodeListLength  && ! childNodeName.equals(currentColumn));
				*/
				if(currentColumn.trim().equalsIgnoreCase("stan_code__fr"))
				{
					stanCodeFr = checkNull(genericUtility.getColumnValue("stan_code__fr", dom));
					System.out.println("stanCodeFr = "+stanCodeFr);
					sql = "Select descr  from station where stan_code = ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,stanCodeFr);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						descr = rs.getString(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<stationa_descr>").append("<![CDATA[" + descr +"]]>").append("</stationa_descr>");
				}
				else if(currentColumn.trim().equalsIgnoreCase("stan_code__to"))
				{
					stanCodeTo = checkNull(genericUtility.getColumnValue("stan_code__to", dom));
					sql = "Select descr  from station where stan_code = ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,stanCodeTo);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						descr = rs.getString(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<stationb_descr>").append("<![CDATA[" + descr +"]]>").append("</stationb_descr>");
				}
				else if(currentColumn.trim().equalsIgnoreCase("curr_code"))
				{
					currCode = checkNull(genericUtility.getColumnValue("curr_code", dom));
					sql = "Select descr  from currency where curr_code = ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,currCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						descr = rs.getString(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<currency_descr>").append("<![CDATA[" + descr +"]]>").append("</currency_descr>");
				}
				valueXmlString.append("</Detail1>");
				break;       
			case 2 : 
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail2>");
				childNodeListLength = childNodeList.getLength();
				/*do
				{   
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					ctr ++;
				}while(ctr < childNodeListLength  && ! childNodeName.equals(currentColumn));
				*/
				/*if(currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					
					distRoute = checkNull(genericUtility.getColumnValue("dist_route", dom));
					valueXmlString.append("<dist_route>").append("<![CDATA[" + distRoute +"]]>").append("</dist_route>");
					//li_line_no = long(gbf_get_argval(is_extra_arg, "line_no"))
					if(lineNo != 0)
					{
						valueXmlString.append("<dist_route>").append("<![CDATA[" + lineNo +"]]>").append("</dist_route>");
					}
					if(lineNo == 1)
					{
						stanCodeFr = checkNull(genericUtility.getColumnValue("stan_code__fr", dom));
						sql = "select descr from station where stan_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,stanCodeFr);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descr = rs.getString(1);
						}
						valueXmlString.append("<stan_code__fr>").append("<![CDATA[" + stanCodeFr +"]]>").append("</stan_code__fr>");
						valueXmlString.append("<stationa_descr>").append("<![CDATA[" + descr +"]]>").append("</stationa_descr>");
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					else
					{
						sql = "select max(line_no) from distroutedet where dist_route = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,distRoute);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							lineNo = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						sql = "select stan_code__to  from distroutedet where dist_route = ? and line_no = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,distRoute);
						pstmt.setInt(2, lineNo);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							stanCodeTo = rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						sql = "select descr from station where stan_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,stanCodeTo);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descr = rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<stan_code__fr>").append("<![CDATA[" + stanCodeFr +"]]>").append("</stan_code__fr>");
						valueXmlString.append("<stan_code__fr>").append("<![CDATA[" + stanCodeFr +"]]>").append("</stan_code__fr>");
					}
				}*/
				if(currentColumn.trim().equalsIgnoreCase("stan_code__fr"))
				{
					stanCodeFr = checkNull(genericUtility.getColumnValue("stan_code__fr", dom));
					sql = "Select descr  from station where stan_code = ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,stanCodeFr);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						descr = rs.getString(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<stationa_descr>").append("<![CDATA[" + descr +"]]>").append("</stationa_descr>");
				}
				else if(currentColumn.trim().equalsIgnoreCase("stan_code__to"))
				{
					stanCodeTo = checkNull(genericUtility.getColumnValue("stan_code__to", dom));
					sql = "Select descr  from station where stan_code = ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,stanCodeTo);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						descr = rs.getString(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<stationb_descr>").append("<![CDATA[" + descr +"]]>").append("</stationb_descr>");
				}
				else if(currentColumn.trim().equalsIgnoreCase("tran_code"))
				{
					tranCode = checkNull(genericUtility.getColumnValue("tran_code", dom));
					sql = "Select tran_name from transporter where tran_code = ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,tranCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						descr = rs.getString(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<transporter_tran_name>").append("<![CDATA[" + descr +"]]>").append("</transporter_tran_name>");
				}
				else if(currentColumn.trim().equalsIgnoreCase("curr_code"))
				{
					currCode = checkNull(genericUtility.getColumnValue("curr_code", dom));
					sql = "Select descr  from currency where curr_code = ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,currCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						descr = rs.getString(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<currency_descr>").append("<![CDATA[" + descr +"]]>").append("</currency_descr>");
				}
				valueXmlString.append("</Detail2>");
				break;
			}
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
