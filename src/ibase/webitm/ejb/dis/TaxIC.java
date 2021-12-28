

/********************************************************
	Title : TaxIC
	Date  : 19/11/11
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

public class TaxIC extends ValidatorEJB implements TaxICLocal, TaxICRemote
{
		//changed by nasruddin 05-10-16
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility = new E12GenericUtility();

	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = "";
		System.out.println("wfValData() called for Tax");
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
		String acctCode = "", acctCodeRevr = "";
		String active = "";
		String cctrCode = "", cctrCodeRevr = "";
		String tauthCode = "";
		String taxGroup = "";
		String taxCode = "";
		String balGroup = "";
		String crnType = "";
		String autoCredit = "";
		String currCode = "";
		String descr = "";
		String taxMethod = "";
		String childNodeName = null;
		String errString = "";
		String errCode = "";
		String userId = "";
		String sql = "";
		String sql2 = "";
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
		PreparedStatement pstmt2 = null ;
		ResultSet rs = null;
		ResultSet rs2 = null;
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
			for(ctr = 0; ctr < childNodeListLength; ctr++)
			{
				childNode = childNodeList.item(ctr);
				childNodeName = childNode.getNodeName();
				if(childNodeName.equalsIgnoreCase("tax_code"))
				{
					taxCode = checkNull(genericUtility.getColumnValue("tax_code", dom));
					System.out.println("Edit flag = "+editFlag);
	
					/*
						if(taxCode == null || taxCode.trim().length() == 0)
						{
							errCode = "VMTAXCB";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else if(editFlag.equalsIgnoreCase("A"))
						{
							sql = "select count(*) from tax where tax_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,taxCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count =  rs.getInt(1);
							}
							if(count > 0) 
							{
								errCode = "VMTAXDP";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}*/

				}
				/* Comment By Nasruddin 21-SEp-16 Start
				else if(childNodeName.equalsIgnoreCase("curr_code"))
				{
					currCode = checkNull(genericUtility.getColumnValue("curr_code", dom));
					System.out.println("119 curr code="+currCode);
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
							errCode = "VECUR2 ";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				}Comment By Nasruddin 21-SEp-16 End*/
				else if(childNodeName.equalsIgnoreCase("tax_group"))
				{
					taxGroup = checkNull(genericUtility.getColumnValue("tax_group", dom));
					taxCode = checkNull(genericUtility.getColumnValue("tax_code", dom));
					System.out.println("212 taxGroup="+taxGroup);
					System.out.println("213 taxCode="+taxCode);
					/* Comment BY Nasruddin 21-SEP-16 START
					if(taxGroup == null || taxGroup.trim().length() == 0)
					{
						errCode = "VMTAXGRP";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					} Comment BY Nasruddin 21-SEP-16 END*/
					
				   if(!taxCode.equalsIgnoreCase(taxGroup))
					{
						sql = "select count(*) from tax where tax_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,taxGroup);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							count =  rs.getInt(1);
						}
						if(count == 0) 
						{
							errCode = "VMTAXGRP";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				}
				/* Comment By Nasruddin 21-Sep-16 Start
				else if(childNodeName.equalsIgnoreCase("descr"))
				{
					descr = checkNull(genericUtility.getColumnValue("descr", dom));
					System.out.println("descr="+descr);
					if(descr == null || descr.trim().length() == 0)
					{
						errCode = "VELVE2";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
				}Comment By Nasruddin 21-Sep-16 End*/
				else if(childNodeName.equalsIgnoreCase("acct_code"))
				{
					acctCode = checkNull(genericUtility.getColumnValue("acct_code", dom));
					System.out.println("acctCode="+acctCode);
					if(acctCode != null && acctCode.trim().length() > 0)
					{
						sql = "SELECT count(*) from accounts where acct_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,acctCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							count =  rs.getInt(1);
						}
						if(count == 0)
						{
							errCode = "VMACCTA";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else 
						{
							sql2 = "select active  from accounts where acct_code = ?";
							pstmt2 =  conn.prepareStatement(sql2);
							pstmt2.setString(1,acctCode);
							rs2 = pstmt2.executeQuery();
							if(rs2.next())
							{
								active = rs2.getString(1);

							}
							if(active != null && (!active.equalsIgnoreCase("Y")))
							{
								errCode = "VMACCTA";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs2.close();
							rs2 = null;
							pstmt2.close();
							pstmt2 = null; 
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					//ADDED BY NASRUDDIN 12-OCT-16 START
					else
					{
						errCode = "VMACCTCD1";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
					//ADDED BY NASRUDDIN 12-OCT-16 END
				}
				else if(childNodeName.equalsIgnoreCase("cctr_code"))
				{
					cctrCode = checkNull(genericUtility.getColumnValue("cctr_code", dom));
					if(cctrCode != null && cctrCode.trim().length() > 0)
					{
						sql = "select count(*) from costctr where cctr_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,cctrCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							count =  rs.getInt(1);
						}
						if(count == 0) 
						{
							errCode = "VMCCTRCD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				}
				// Changed By nasruddin  21-SEp-16 Start
				else if(childNodeName.equalsIgnoreCase("cctr_code__revr"))
				{
					cctrCodeRevr = checkNull(genericUtility.getColumnValue("cctr_code__revr", dom));
					if(cctrCodeRevr != null && cctrCodeRevr.trim().length() > 0)
					{
						sql = "select count(*) from costctr where cctr_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,cctrCodeRevr);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							count =  rs.getInt(1);
						}
						if(count == 0) 
						{
							errCode = "VMCCTRCD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				}
				else if(childNodeName.equalsIgnoreCase("acct_code__revr"))
				{
					acctCodeRevr = checkNull(genericUtility.getColumnValue("acct_code__revr", dom));
					System.out.println("acctCodeRevr="+acctCodeRevr);
					if(acctCodeRevr != null && acctCodeRevr.trim().length() > 0)
					{
						sql = "SELECT count(*) from accounts where acct_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,acctCodeRevr);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							count =  rs.getInt(1);
						}
						if(count == 0)
						{
							errCode = "VMACCTA";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else 
						{
							sql2 = "select active  from accounts where acct_code = ?";
							pstmt2 =  conn.prepareStatement(sql2);
							pstmt2.setString(1,acctCodeRevr);
							rs2 = pstmt2.executeQuery();
							if(rs2.next())
							{
								active = rs2.getString(1);

							}
							if(active != null && !active.equalsIgnoreCase("Y"))
							{
								errCode = "VMACCTA";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs2.close();
							rs2 = null;
							pstmt2.close();
							pstmt2 = null; 
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					//ADDED BY NASRUDDIN 12-OCT-16 START
					else
					{
						errCode = "VMACCTCD1";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
					//ADDED BY NASRUDDIN 12-OCT-16 END
				}
				//// Changed By nasruddin  21-SEp-16 END
				else if(childNodeName.equalsIgnoreCase("tauth_code"))
				{
					tauthCode = checkNull(genericUtility.getColumnValue("tauth_code", dom));
					if(tauthCode != null && tauthCode.trim().length() > 0)
					{
						sql = "select count(*) from tax_authority where tauth_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,tauthCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							count =  rs.getInt(1);
						}
						if(count == 0) 
						{
							errCode = "VMTAUTHCD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				}
				/*Comment By Nasruddin  21-SEp-16 START
				else if(childNodeName.equalsIgnoreCase("tax_method"))
				{
					taxMethod = checkNull(genericUtility.getColumnValue("tax_method", dom));
					System.out.println("241 taxMeth="+taxMethod);
					if(taxMethod != null && taxMethod.trim().length() > 0)
					{
						sql = "select count(*) from taxmethod where tax_method = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,taxMethod);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							count =  rs.getInt(1);
						}
						if(count == 0) 
						{
							errCode = "VMTAX";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				}Comment By Nasruddin  21-SEp-16 END*/
				else if(childNodeName.equalsIgnoreCase("bal_group"))
				{
					balGroup = checkNull(genericUtility.getColumnValue("bal_group", dom));
					if(balGroup != null && balGroup.trim().length() > 0)
					{
						sql = "select count(*) from tax_balance_grp where bal_group = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,balGroup);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							count =  rs.getInt(1);
						}
						if(count == 0) 
						{
							errCode = "VMBALGRPN";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				}
				else if(childNodeName.equalsIgnoreCase("crn_type"))
				{
					crnType = checkNull(genericUtility.getColumnValue("crn_type", dom));
					autoCredit = checkNull(genericUtility.getColumnValue("auto_credit", dom));
					System.out.println("271 autoCredit="+autoCredit);
					System.out.println("272 crnType="+crnType);
					if(autoCredit != null && autoCredit.trim().equalsIgnoreCase("Y"))
					{
						if(crnType == null || crnType.trim().length() == 0)
						{
							errCode = "VMCRNTYPE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
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
				//Commented and Added by sarita on 13NOV2017
				/*if(conn != null)
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
				conn = null;*/
				if(conn != null)
				{
					conn.close();
					conn = null;
				}
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
			System.out.println("Exception : [TAX][itemChanged( String, String )] :==>\n" + e.getMessage());
		}
		return valueXmlString;
	}

	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		String round = "";
		String taxMethod = "";
		String balGroup = "";
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
		//changed by nasruddin 05-10-16
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
			//do
			//{   
			//	childNode = childNodeList.item(ctr);
			//	childNodeName = childNode.getNodeName();
			//	ctr ++;
			//}while(ctr < childNodeListLength  && ! childNodeName.equals(currentColumn));
			if(currentColumn.trim().equalsIgnoreCase("round"))
			{
				round = checkNull(genericUtility.getColumnValue("round", dom));
				if(round != null && round.equalsIgnoreCase("N"))
				{
					valueXmlString.append("<round_to protect = \"1\">").append("<![CDATA[" + round +"]]>").append("</round_to>");
				}
				else
				{
					valueXmlString.append("<round_to protect = \"0\">").append("<![CDATA[" + round +"]]>").append("</round_to>");
				}
			}
			/*else if(currentColumn.trim().equalsIgnoreCase("tax_code")) add comment  because Tax_code is auto genarate.
			{
				taxCode = checkNull(genericUtility.getColumnValue("tax_code", dom));
				//valueXmlString.append("<tax_group>").append("<![CDATA[" + taxCode +"]]>").append("</tax_group>");
				//change bz Tax_code is auto genarate. 
			}*/
			else if(currentColumn.trim().equalsIgnoreCase("tax_method"))
			{
				taxMethod = checkNull(genericUtility.getColumnValue("tax_method", dom));							
				System.out.println("451 taxMethod"+taxMethod);
				descr="";
				if(taxMethod != null && taxMethod.trim().length() > 0)
				{
					sql = "select descr from taxmethod where tax_method = ? ";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,taxMethod);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						descr = rs.getString(1);
						System.out.println("456 DESCR"+descr);
						
					}
					valueXmlString.append("<taxmethod_descr>").append("<![CDATA[" + descr +"]]>").append("</taxmethod_descr>");
					//else
					//{
					//	valueXmlString.append("<taxmethod_descr>").append("<![CDATA[" + "" +"]]>").append("</taxmethod_descr>");
					//}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
				//else
				//{
				//	valueXmlString.append("<taxmethod_descr>").append("<![CDATA[" + "" +"]]>").append("</taxmethod_descr>");
				//}
			}
			else if(currentColumn.trim().equalsIgnoreCase("bal_group"))
			{
				balGroup = checkNull(genericUtility.getColumnValue("bal_group", dom));							
				System.out.println("478 balGroup"+balGroup);
				descr="";
				if(balGroup != null && balGroup.trim().length() > 0)
				{
					
					sql = "select descr from tax_balance_grp where bal_group = ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,balGroup);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						descr = rs.getString(1);
						
					}
					
					//else
					//{
					//	valueXmlString.append("<tax_balance_grp_descr>").append("<![CDATA[" + "" +"]]>").append("</tax_balance_grp_descr>");
					//}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
				valueXmlString.append("<tax_balance_grp_descr>").append("<![CDATA[" + descr +"]]>").append("</tax_balance_grp_descr>");
				//else
				//{
				//	valueXmlString.append("<tax_balance_grp_descr>").append("<![CDATA[" + "" +"]]>").append("</tax_balance_grp_descr>");
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
