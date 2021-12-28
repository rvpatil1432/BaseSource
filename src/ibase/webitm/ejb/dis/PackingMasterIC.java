

/********************************************************
	Title : PackingIC
	Date  : 16/04/12
	Developer: Neelam Salunkhe

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

public class PackingMasterIC extends ValidatorEJB implements PackingMasterICLocal, PackingMasterICRemote
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
		String unit = "";
		String unitGwt = "";
		String unitNwt = "";
		String unitDmn = "";
		String unitTwt ="";
		String descr = "";
		String capacity ="";
		String childNodeName = null;
		String errString = "";
		String errCode = "";
		String userId = "";
		String sql = "";
		String errorType = "";
		String packCode = "", keyFalg = "";
		int count = 0;
		int ctr=0;
		int childNodeListLength;
		int currentFormNo = 1;
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
		System.out.println("Packing Val Start");
		try
		{
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			//Added By nasruddin start 06-oct-16 add switch case
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
				for(ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					//System.out.println("ALL NODE .........." + childNodeName);//NEELAM
					if(childNodeName.equalsIgnoreCase("pack_code"))
					{
						packCode = checkNull(genericUtility.getColumnValue("pack_code", dom));
						System.out.println("Pack Code="+packCode);
						/* Changed  BY Nasruddin 22-SEP-16 START
						if(packCode == null || packCode.trim().length() == 0)
						{
							errCode = "VMPACNU";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else 
						{
							sql = "select count(*) from packing where pack_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,unit);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count =  rs.getInt(1);
							}
							if(count > 0) 
							{
								errCode = "VMPACUNI";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						 */
						sql = "SELECT KEY_FLAG  FROM TRANSETUP WHERE TRAN_WINDOW = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,"w_packing");
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							keyFalg =  rs.getString("KEY_FLAG");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						keyFalg = keyFalg == null ?"M" : keyFalg.trim();
						if(keyFalg.equals("M") && packCode == null || packCode.trim().length() == 0)
						{
							errCode = "VMCODNULL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else 
						{
							if(editFlag.equals("A"))
							{
								sql = "select count(1) from packing where pack_code = ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1,packCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									count =  rs.getInt(1);
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
						// Changed By Nasruddin 22-SEP-16 END
					}
					else if(childNodeName.equalsIgnoreCase("descr"))
					{
						descr = checkNull(genericUtility.getColumnValue("descr", dom));
						System.out.println("Descr="+descr);
						if(descr == null || descr.trim().length() == 0)
						{
							errCode = "VMDESCR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						// Changed By Nasruddin 22-SEP-16 START
						else
						{
							count = 0;
							packCode = checkNull(genericUtility.getColumnValue("pack_code", dom));
							sql = "SELECT COUNT(1) FROM PACKING WHERE PACK_CODE <> ? AND DESCR = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,packCode);
							pstmt.setString(2,descr);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count =  rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if(count > 0) 
							{
								errCode = "VMDUPDESCR";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}

						}
						//Changed By Nasruddin END [22-SEP-16]
					}
					/* Comment By Nasruddin START [22-SEP-16] START
					else if(childNodeName.equalsIgnoreCase("capacity"))
					{
						capacity = checkNull(genericUtility.getColumnValue("capacity", dom));
						System.out.println("capacity="+capacity);

						if(capacity != null && capacity.trim().length() > 0)
						{   
							count =Integer.parseInt(capacity);
							System.out.println("count="+count);
							if(count < 0){
							errCode = "VMCAPNEG";//need to be chanage errorcode
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						      }
						}
					}Comment By Nasruddin START [22-SEP-16] END */
					else if(childNodeName.equalsIgnoreCase("unit"))
					{
						unit = checkNull(genericUtility.getColumnValue("unit", dom));
						System.out.println("Unit code="+unit);
						if(unit == null || unit.trim().length() == 0)
						{
							errCode = "VMUNIT1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else 
						{
							sql = "select count(*) from uom where unit = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,unit);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count =  rs.getInt(1);
							}
							if(count == 0) 
							{
								errCode = "VTUNIT1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					/* Comment By Nasruddin START [22-SEP-16] START
					else if(childNodeName.equalsIgnoreCase("unit__dimn"))
					{
						unitDmn = checkNull(genericUtility.getColumnValue("unit__dimn", dom));
						System.out.println("unit__dimn code="+unitDmn);
						if(unitDmn != null && unitDmn.trim().length() > 0)
						{
							sql = "select count(*) from uom where unit = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,unitDmn);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count =  rs.getInt(1);
							}
							if(count == 0) 
							{
								errCode = "VTUNIT1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					Comment By Nasruddin START [22-SEP-16] END */
					else if(childNodeName.equalsIgnoreCase("unit__nwt"))
					{
						unitNwt = checkNull(genericUtility.getColumnValue("unit__nwt", dom));
						System.out.println("unit__nwt code="+unitNwt);
						if(unitNwt != null && unitNwt.trim().length() > 0)
						{
							sql = "select count(*) from uom where unit = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,unitNwt);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count =  rs.getInt(1);
							}
							if(count == 0) 
							{
								errCode = "VTUNIT1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					else if(childNodeName.equalsIgnoreCase("unit__gwt"))
					{
						unitGwt = checkNull(genericUtility.getColumnValue("unit__gwt", dom));
						System.out.println("unit__gwt code="+unitGwt);

						if(unitGwt != null && unitGwt.trim().length() > 0)
						{
							sql = "select count(*) from uom where unit = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,unitGwt);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count =  rs.getInt(1);
							}
							if(count == 0) 
							{
								errCode = "VTUNIT1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}

					else if(childNodeName.equalsIgnoreCase("unit__twt"))
					{
						unitTwt = checkNull(genericUtility.getColumnValue("unit__twt", dom));
						System.out.println("unit code="+unitTwt);

						if(unitTwt != null && unitTwt.trim().length() > 0)
						{
							sql = "select count(*) from uom where unit = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,unitTwt);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count =  rs.getInt(1);
							}
							if(count == 0) 
							{
								errCode = "VTUNIT1";
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
			case 2:
				System.out.println("Inside Case 2");
			case 3:
				System.out.println("Inside case 3");
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
			System.out.println("Exception : [Packing][itemChanged( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return valueXmlString;
	}

	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		String unit = "";
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
			if(currentColumn.trim().equalsIgnoreCase("unit"))
			{
				unit = checkNull(genericUtility.getColumnValue("unit", dom));
				System.out.println("Unit Code"+unit);
				sql = "select descr from uom where unit = ?";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,unit);
				rs = pstmt.executeQuery();
				if(rs != null && rs.next())
				{
					descr = rs.getString(1);
					
				}
				valueXmlString.append("<uom_descr>").append("<![CDATA[" + descr +"]]>").append("</uom_descr>");
				valueXmlString.append("<unit__twt>").append("<![CDATA[" + unit +"]]>").append("</unit__twt>");
				valueXmlString.append("<unit__gwt>").append("<![CDATA[" + unit +"]]>").append("</unit__gwt>");
				valueXmlString.append("<unit__nwt>").append("<![CDATA[" + unit +"]]>").append("</unit__nwt>");
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
