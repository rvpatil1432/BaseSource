/********************************************************
	Title : CustStockGWTWizIC[D15ESUN013]
	Date  : 09/03/16
	Developer: Chandrashekar

 ********************************************************/
package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;

import java.text.SimpleDateFormat;
import java.util.*;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import ibase.utility.E12GenericUtility;
import java.text.DecimalFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.ejb.Stateless; 
@Stateless 

public class CustStockGWTWizIC extends ValidatorEJB implements CustStockGWTWizICLocal,CustStockGWTWizICRemote //implements SessionBean
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
			if (xmlString != null && xmlString.trim().length() > 0 )
			{
				dom = parseString(xmlString);
				System.out.println("xmlString["+xmlString+"]");
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0 )
			{
				dom1 = parseString(xmlString1);
				System.out.println("xmlString1["+xmlString1+"]");
			}
			if (xmlString2 != null && xmlString2.trim().length() > 0 )
			{
				dom2 = parseString(xmlString2);
				System.out.println("xmlString2["+xmlString2+"]");				
			}
			
			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception : [CustStockGWTWizIC][wfValData( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e);
		}
		return(errString);
	}
	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
			String childNodeName = null;
			String errString = "";
			String errCode = "";
			String userId = "",lineNo="",sales="";
			String errorType = "";
			String itemCode="";
			int cnt = 0;
			int ctr=0;
			int childNodeListLength;
			double secSales=0.0;
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
			int currentFormNo =0;
			SimpleDateFormat dateFormat2 = null;
			String dummyProduct="";
			ibase.webitm.ejb.dis.DistCommon dist = new ibase.webitm.ejb.dis.DistCommon();
			try
			{	dateFormat2 = new SimpleDateFormat(genericUtility.getApplDateFormat());
				System.out.println("@@@@@@@@ wfvaldata called");
				//conn = connDriver.getConnectDB("DriverITM");
				conn = getConnection();
				connDriver = null;
				userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
				if(objContext != null && objContext.trim().length()>0)
				{
					currentFormNo = Integer.parseInt(objContext);
				}
				System.out.println("currentFormNo>>>>>"+currentFormNo);
				switch(currentFormNo)
				{
				
				case 3:
					System.out.println("Case 3::::::dom2 >>>>"+genericUtility.serializeDom(dom2));
					parentNodeList = dom2.getElementsByTagName("Detail3");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
	
					for(ctr = 0; ctr < childNodeListLength; ctr++)
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						
						//Added by saurabh[04/01/17]
						if(childNodeName.equals("attribute"))
						{
							String updateFlag = "";
							updateFlag = childNode.getAttributes().getNamedItem("updateFlag").getNodeValue();
							System.out.println("updateFlag>>>"+updateFlag);
							if ("D".equalsIgnoreCase(updateFlag))
							{
								System.out.println("Break from here as the record is deleted");
								break;
							}
						}
						//Added by saurabh[04/01/17]
						
						if (childNodeName.equalsIgnoreCase("item_code"))
						{

						itemCode = this.genericUtility.getColumnValue("item_code", dom);
						lineNo = this.genericUtility.getColumnValue("line_no", dom);
						System.out.println("itemCode>>>>>>" + itemCode);
						if (itemCode == null || itemCode.trim().length() == 0)
						{
							System.out.println("Error : No data found in item details");
							errCode = "VTEMTITM";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							errCode = isExist("item", "item_code", itemCode, conn);
							if ("FALSE".equalsIgnoreCase(errCode))
							{
								errCode = "VMITMNOTEX";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								
							}else
							{
								dummyProduct =  dist.getDisparams("999999","DUMMY_PRODUCT",conn);
								if (("NULLFOUND".equalsIgnoreCase(dummyProduct) || dummyProduct == null || dummyProduct.trim().length() == 0) )
								{
									System.out.println("Disparm not defined for dummy item!!!!");
									if("FALSE".equalsIgnoreCase(isFrequent(itemCode,conn)))
									{
										errCode = "VMFRITMCHK";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());	
									}
								}
								else
								{
									if(!dummyProduct.trim().equalsIgnoreCase(itemCode.trim())){
										if("FALSE".equalsIgnoreCase(isFrequent(itemCode,conn)))
										{
											errCode = "VMFRITMCHK";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());	
										}
										}
								}
								if (isDulplicateFrmDom(dom2,itemCode,lineNo))
								{
									errCode = "VTDUPITMCD";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}

						}
					}
						 /*if (childNodeName.equalsIgnoreCase("sales"))
						 {
							 sales = this.genericUtility.getColumnValue("sales", dom);
							 if(sales != null && sales.trim().length()>0)
							 {
								 secSales = Double.parseDouble(sales);
							 }
							 else
							 {
								 secSales = 0.0;
							 }
							 if(secSales < 0)
								{
								 	errCode = "VTSALNEG";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									System.out.println("Secondary sales is negitive");
								}
						 }*/
						 
				}// end for
					break;  // case 1 end
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
		}

	//end of validation
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
			if(rs.next())
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
			throw new ITMException(ex);
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
				throw new ITMException(e);
			}
		}		
		return msgType;
	}
	private String isExist(String table, String field, String value,Connection conn) throws SQLException
	{
		String sql = "",retStr="";
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		int cnt=0;

		sql = " SELECT COUNT(1) FROM "+ table + " WHERE " + field + " = ? ";
		pstmt =  conn.prepareStatement(sql);
		pstmt.setString(1,value);
		rs = pstmt.executeQuery();
		if(rs.next())
		{
			cnt = rs.getInt(1);
		}
		rs.close();
		rs = null;
		pstmt.close(); 
		pstmt = null;
		if( cnt > 0)
		{
			retStr = "TRUE";
		}
		if( cnt == 0 )
		{
			retStr = "FALSE";
		}
		System.out.println("@@@@ isexist["+value+"]:::["+retStr+"]:::["+cnt+"]");
		return retStr;
	}
	
	private boolean isDulplicateFrmDom(Document dom,String itemCode, String lineNo) throws ITMException
    {
		NodeList parentList = null;
		NodeList childList = null;
		Node parentNode = null;
		Node childNode = null;

		String lineNoDom = "";
		boolean isDulplicate = false;
		String itemCodeDom= "";
		System.out.println("---inside isDulplicateFrmDom--");
		try
		{

			parentList = dom.getElementsByTagName("Detail3");
			int parentNodeListLength = parentList.getLength();
			//System.out.println("parentNodeListLength>>>>>>>"+parentNodeListLength);
			for (int prntCtr = parentNodeListLength; prntCtr > 0; prntCtr-- )
			{	
				parentNode = parentList.item(prntCtr-1);
				childList = parentNode.getChildNodes();
				//System.out.println("childList length>>>"+childList.getLength());
				for (int ctr = childList.getLength(); ctr >= 0; ctr--)
				//for (int ctr = 0; ctr < childList.getLength(); ctr++)
				{
					//Added by Pratheek[when click on previous button duplicate item code exception issue]-Start
					childNode = childList.item(ctr-1);
					//Added by Pratheek[when click on previous button duplicate item code exception issue]-End
					
					if(childNode != null &&  childNode.getNodeName().equalsIgnoreCase("attribute"))
					{
						String updateFlag = "";
						updateFlag = childNode.getAttributes().getNamedItem("updateFlag").getNodeValue();
						if (updateFlag.equalsIgnoreCase("D"))
						{
							System.out.println("Break from here as the record is deleted");
							break;
						}
					}	
					if ( childNode != null && childNode.getFirstChild() != null &&  
					childNode.getNodeName().equalsIgnoreCase("line_no") )
					{
						lineNoDom = childNode.getFirstChild().getNodeValue().trim();
						System.out.println("lineNo["+lineNo.trim()+"]lineNoDom["+lineNoDom+"]");
						if (lineNo.trim().equalsIgnoreCase(lineNoDom))
						{
							System.out.println("Break from here as line No match");
							break;
							
						}	
					}

					if ( childNode != null && childNode.getFirstChild() != null &&  
					childNode.getNodeName().equalsIgnoreCase("item_code") )
					{
						itemCodeDom = childNode.getFirstChild().getNodeValue().trim();
					}
					//System.out.println("itemCodeDom loop"+itemCodeDom);
					
				}
				System.out.println("itemCodeDom>>>>"+itemCodeDom+"@@@@@@itemCode>>>"+itemCode);
				//Added by Pratheek[when click on previous button duplicate item code exception issue]-Start
				if (itemCode.trim().equalsIgnoreCase(itemCodeDom.trim()) && !lineNo.trim().equalsIgnoreCase(lineNoDom))
				{
					isDulplicate = true;
					break;
				}
				//Added by Pratheek[when click on previous button duplicate item code exception issue]-End
				
			}//for loop
			
		
		}catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
		}
		finally
		{
			try
			{
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}	
		System.out.println("isDulplicate>>>>>> ["+isDulplicate+"]");
		return isDulplicate;
	}
	//Commented by saurabh 040117
	private String isFrequent( String itemCode,Connection conn) throws SQLException
	{
		String sql = "",retStr="";
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		int cnt=0;
		sql = " SELECT COUNT(1) FROM item WHERE item_code = ? and item_usage='F' ";//Added by saurabh[22/12/16]
		pstmt =  conn.prepareStatement(sql);
		pstmt.setString(1,itemCode);
		rs = pstmt.executeQuery();
		if(rs.next())
		{
			cnt = rs.getInt(1);
		}
		rs.close();
		rs = null;
		pstmt.close(); 
		pstmt = null;
		if( cnt > 0)
		{
			retStr = "TRUE";
		}
		if( cnt == 0 )
		{
			retStr = "FALSE";
		}
		System.out.println("@@@@ isexist["+itemCode+"]:::["+retStr+"]:::["+cnt+"]");
		return retStr;
	}
	//Commented by saurabh 040117
}	
