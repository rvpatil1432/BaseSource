

/********************************************************
	Title : SupplierDefault
	Date  : 12/09/18
	Developer: Amey W

 ********************************************************/


package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;

import ibase.webitm.ejb.ValidatorEJB;

import java.util.*;

/**
 * Session Bean implementation class SupplierDefaultEJB
 */
@Stateless
public class SupplierDefaultEJB extends ValidatorEJB implements SupplierDefaultEJBRemote, SupplierDefaultEJBLocal 
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
		String suppType = "";
		String suppCode = "";
		String currCode = "";
		String currCode1 = "";
		String sundryType = "";
		String taxChap = "";
		String taxClass = "";
		String contactCode = "";
		String acctCode = "";
		String siteCode = "";
		String cctrCode = "";
		String crTerm = "";
		String userId = "";
		String sql="";
		String errCode="";
		String errorType = "";
		String childNodeName = null;
		String errString = "";
		String existFlag="",dlvTerm="",lockGroup="";
		int ctr=0;
		int childNodeListLength;
		long count = 0;
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement pstmt = null ;
		ResultSet rs = null;
		ConnDriver connDriver = new ConnDriver();
		int currentFormNo = 0;
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>"); 
		String commTable = "";
		String ssi = "", msmeType = "";
		try
		{
			conn = getConnection();
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			if (objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			System.out.println(">>>>>>>>>>>>>>>>currentFormNo In validation:"+currentFormNo);
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
					
					if(childNodeName.equalsIgnoreCase("supp_type"))
					{    
						suppType = genericUtility.getColumnValue("supp_type", dom);
						if(suppType == null || suppType.trim().length() == 0)
						{
							errCode = "VMSUPPTP";      
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					
					else if(childNodeName.equalsIgnoreCase("curr_code"))
					{
						currCode =genericUtility.getColumnValue("curr_code", dom);
							existFlag = isExist("currency", "curr_code", currCode, conn);
							if ("FALSE".equals(existFlag))
							{
								errCode = "VMCURRCND";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else if("E".equalsIgnoreCase(editFlag))
							{
								suppCode = genericUtility.getColumnValue("supp_code", dom);
								currCode1 = findValue(conn, "curr_code", "supplier", "supp_code", suppCode);
								if((!(currCode.trim().equals(currCode1.trim())) && (currCode1 != null && currCode1.trim().length() > 0)))
								{
									sundryType = "S";
									sql = " Select count(*) from sundrybal where sundry_type = ? and sundry_code = ? and (dr_amt != 0 or cr_amt != 0)";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1, sundryType);
									pstmt.setString(2, suppCode);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										count = rs.getInt(1);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									
									if(count != 0)
									{
										errCode = "VXCURRCD1";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
							}
					}
					else if(childNodeName.equalsIgnoreCase("cr_term"))
					{
						
						crTerm = genericUtility.getColumnValue("cr_term", dom);
						existFlag = isExist("crterm", "cr_term", crTerm, conn);
						if ("FALSE".equals(existFlag))
						{
							errCode = "VMCRTER1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if(childNodeName.equalsIgnoreCase("tax_chap"))
					{
						taxChap = genericUtility.getColumnValue("tax_chap", dom);
						if(taxChap != null && taxChap.trim().length() != 0)
						{
							existFlag = isExist("taxchap", "tax_chap", taxChap, conn);
							if ("FALSE".equals(existFlag))
							{
								errCode = "VMTAXCHP";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}                       
					else if(childNodeName.equalsIgnoreCase("tax_class"))
					{
						taxClass = genericUtility.getColumnValue("tax_class", dom);
						if(taxClass != null && taxClass.trim().length() != 0)
						{
							existFlag = isExist("taxclass", "tax_class", taxClass, conn);
							if ("FALSE".equals(existFlag))
							{
								errCode = "VTTCLASS1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if(childNodeName.equalsIgnoreCase("acct_code__ap"))
					{
						System.out.println(">>>>>>acct_code__ap:");
						acctCode = genericUtility.getColumnValue("acct_code__ap", dom);
						siteCode = genericUtility.getColumnValue("site_code",dom);
						errCode = supplier_acct(siteCode, acctCode, conn);
						System.out.println(">>>>>>>>>>acct_code__ap error:"+errCode);
						if(errCode != null && errCode.trim().length() > 0 )
						{
							System.out.println(">>>>>> Addd acct_code__ap error:"+errCode);
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if(childNodeName.equalsIgnoreCase("acct_code__ap_adv"))
					{
						System.out.println(">>>>>>acct_code__ap_adv:");
						acctCode = genericUtility.getColumnValue("acct_code__ap_adv", dom);
						siteCode = genericUtility.getColumnValue("site_code",dom);
						errCode = supplier_acct(siteCode, acctCode, conn);
						System.out.println(">>>>>>>>>>acct_code__ap_adv error:"+errCode);
						if(errCode != null && errCode.trim().length() > 0 )
						{
							System.out.println(">>>>>> Addd acct_code__ap_adv error:"+errCode);
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if(childNodeName.equalsIgnoreCase("cctr_code__ap"))
					{
						System.out.println(">>>>>>cctr_code__ap:");
						cctrCode = genericUtility.getColumnValue("cctr_code__ap", dom);
						acctCode = genericUtility.getColumnValue("acct_code__ap",dom);
						if(cctrCode != null && cctrCode.trim().length() > 0)
						{
							errCode = supplier_cctr( acctCode, cctrCode,  conn);
							System.out.println(">>>>>>>>>>cctr_code__ap error:"+errCode);
							if(errCode != null && errCode.trim().length() > 0 )
							{
								System.out.println(">>>>>> Addd cctr_code__ap error:"+errCode);
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if(childNodeName.equalsIgnoreCase("cctr_code__ap_adv"))
					{
						System.out.println(">>>>>>cctr_code__ap_adv:");
						cctrCode = genericUtility.getColumnValue("cctr_code__ap_adv", dom);
						acctCode = genericUtility.getColumnValue("acct_code__ap_adv",dom);
						if(cctrCode != null && cctrCode.trim().length() > 0)
						{
							errCode = supplier_cctr( acctCode, cctrCode,  conn);
							System.out.println(">>>>>>>>>>cctr_code__ap_adv error:"+errCode);
							if(errCode != null && errCode.trim().length() > 0 )
							{
								System.out.println(">>>>>> Addd cctr_code__ap_adv error:"+errCode);
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if(childNodeName.equalsIgnoreCase("contact_code"))
					{
						contactCode = genericUtility.getColumnValue("contact_code", dom);
						existFlag = isExist("contact", "contact_code", contactCode, conn);
						if ("FALSE".equals(existFlag))
						{
							errCode = "VMCONTCD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if(childNodeName.equalsIgnoreCase("dlv_term"))
					{
						dlvTerm = genericUtility.getColumnValue("dlv_term", dom);
						if(dlvTerm != null && dlvTerm.trim().length() > 0)
						{
							existFlag = isExist("delivery_term", "dlv_term", dlvTerm, conn);
							if ("FALSE".equals(existFlag))
							{
								errCode = "VMDLVTERM1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if(childNodeName.equalsIgnoreCase("lock_group"))
					{
						lockGroup = genericUtility.getColumnValue("lock_group", dom);
						if(lockGroup != null && lockGroup.trim().length() > 0)
						{
							existFlag = isExist("lock_group", "lock_group", lockGroup, conn);
							if ("FALSE".equals(existFlag))
							{
								errCode = "VMLOCKCODE";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					else if( childNodeName.equalsIgnoreCase("comm_table"))
					{
						
						commTable = checkNull(this.genericUtility.getColumnValue("comm_table", dom));
						
						if( commTable != null && commTable.trim().length() > 0){
							count = 0;
							sql = "select count(1) as count from  comm_hdr where comm_table = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, commTable);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								count = rs.getInt("count");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if (count == 0)
							{
								errCode = "VMCOMMTBCD";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						
						}
					else if(childNodeName.equalsIgnoreCase("msme_type"))
					{
						ssi = genericUtility.getColumnValue("ssi", dom);
						if("Y".equals(ssi))
						{
							msmeType = genericUtility.getColumnValue("msme_type", dom);
							if(msmeType == null || msmeType.trim().length() == 0 )
							{
								errCode = "VMSUPSSI02";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
				}
				break; 
				
			} 
			
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
	}

	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		System.out.println("amey x==>");
		
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
			System.out.println("Exception : [supplier][itemChanged( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return valueXmlString;
	}

	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		String crTerm = "";
		String crDays = "";
		StringBuffer valueXmlString = new StringBuffer();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
        ConnDriver connDriver = new ConnDriver();
        int currentFormNo = 0;
        int ctr = 0;
        NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String suppType = "" , suppTypeDescr = "";
		try
		{  
			conn = getConnection();
			conn.setAutoCommit(false);
			connDriver = null;
            
			if (objContext != null && objContext.trim().length() > 0)
			{
				currentFormNo = Integer.parseInt(objContext.trim());
			}
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");
			System.out.println("**********ITEMCHANGE FOR CASE" + currentFormNo + "**************");
			switch (currentFormNo)
			{
			case 1:
				
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0;
				
				valueXmlString.append("<Detail1>");
				
				 if(currentColumn.trim().equalsIgnoreCase("cr_term"))	
				 {
					 System.out.println(">>>>>>>>>>Itemchange cr_term:");
					crTerm = genericUtility.getColumnValue("cr_term", dom);
					crDays = findValue(conn, "cr_days", "crterm", "cr_term", crTerm);
					System.out.println(">>>>>>>>crDays IC:"+crDays);
					if(crDays!=null && crDays.trim().length() > 0)
					{
						valueXmlString.append("<credit_prd>").append("<![CDATA[" + crDays + "]]>").append("</credit_prd>");
					}
					else
					{
						crDays="0";
						valueXmlString.append("<credit_prd>").append("<![CDATA[" + crDays + "]]>").append("</credit_prd>");
					}
				 }
				 valueXmlString.append("</Detail1>");
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
			}			
		}
		return valueXmlString.toString();
	}	 

	public String getfinparm(String pCode , String varName ,Connection conn) throws ITMException
	{
		String sql = "";
		String varValue = "";
		String addValue = "";
		PreparedStatement pstmt = null ;
		ResultSet rs = null;
		try
		{
			sql = "select var_value, addl_value from finparm where prd_code = ? and var_name = ?" ;
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,pCode);
			pstmt.setString(2,varName);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				varValue = rs.getString("var_value");
				addValue = rs.getString("addl_value");
			}

		}
		catch (Exception e) 
		{
			System.out.println("Catch block of Supplier.getfinparm()====> "+e);
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
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
		// ---- End ----
		
		if(addValue == null || addValue.trim().length() == 0)
		{
			return varValue;
		}
		else	
		{
			return varValue.trim()+";"+addValue.trim();
		}
	}
	public String supplier_cctr(String acctCode , String cctrCode, Connection conn) throws ITMException
	{
		String sql = "";
		String errCode = "";
		String finparamCcterm ="";
		int count = 1;
		PreparedStatement pstmt = null ;
		ResultSet rs = null;
		try
		{
			finparamCcterm = getfinparm("999999","CCTR_CHECK",conn);
			if(finparamCcterm.equals("Y"))
			{
				if(acctCode.trim().length() > 0 && acctCode != null)
				{
					sql = "select count(*) from costctr where cctr_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,cctrCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						count = rs.getInt(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					//------- End -------
					
					if(count == 0)
					{
						errCode = "VMCCTR1";
					}
				}
				sql = "select count(*) from accounts_cctr where acct_code = ? and cctr_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,acctCode);
				pstmt.setString(2,cctrCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					count = rs.getInt(1);
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				
				if(count == 0)
				{
					sql = "select count(*) from accounts_cctr where acct_code = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,acctCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						count = rs.getInt(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					//---- End ----
					
					if(count > 0)
					{
						errCode = "VMCCTR2";;
					}
					else if(count == 0)
					{
						errCode = "VMCCTR2";
					}
				}

			}
		}
		catch (Exception e) 
		{
			System.out.println("Catch block of Supplier.supplier_cctr()=== "+e);
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
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
		return errCode;
	}

	public String supplier_acct( String siteCode ,String acctcode ,Connection conn ) throws ITMException
	{
		String sql = "";
		PreparedStatement pstmt = null ;
		ResultSet rs = null;
		String siteSpec = "";
		String active = "";
		String errCode="";
		int count =0;
		try
		{
			sql = "select var_value from finparm where prd_code = '999999' and var_name = 'SITE_SPECIFIC_ACCT' ";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				siteSpec = rs.getString(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			sql = "select count(*) from accounts where acct_code = ?" ;
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,acctcode);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				count = rs.getInt(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if(count != 0)
			{
				sql="select active from accounts where acct_code = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,acctcode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					active = rs.getString("active");
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(!active.equals("Y"))
				{
					errCode = "VMACCTA";
				}
				else if(siteSpec != null && siteSpec.equals("Y"))
				{
					sql = "select count(*) from site_account where site_code = ? and acct_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,siteCode);
					pstmt.setString(2,acctcode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						count = rs.getInt(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					
					if(count == 0)
					{
						errCode = "VMACCT3";
					}
				}

			}		
			else
			{
				errCode = "VMACCT1";

			}
		}
		catch (Exception e) 
		{
			System.out.println("Catch block of Supplier.supplier_acct()=== "+e);
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
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
		//---- End ----
		
		return errCode;
	}
	public String mid(String fname ,int i , int j)
	{
		String name = fname.substring(1, 40);
		return name; 
	}
	private String checkNull(String input)
	{
		if (input == null)
		{
			input = "";
		}
		return input;
	}
	
	private String isExist(String table, String field, String value, Connection conn) throws SQLException
	{
		String sql = "", retStr = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int cnt = 0;

		sql = " SELECT COUNT(1) FROM " + table + " WHERE " + field + " = ? ";
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, value);
		rs = pstmt.executeQuery();
		if (rs.next())
		{
			cnt = rs.getInt(1);
		}
		rs.close();
		rs = null;
		pstmt.close();
		pstmt = null;
		if (cnt > 0)
		{
			retStr = "TRUE";
		}
		if (cnt == 0)
		{
			retStr = "FALSE";
		}
		System.out.println("@@@@ isexist[" + value + "]:::[" + retStr + "]:::[" + cnt + "]");
		return retStr;
	}
	private String findValue(Connection conn, String columnName, String tableName, String columnName2, String value) throws ITMException, RemoteException
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		String findValue = "";
		try
		{
			sql = "SELECT " + columnName + " from " + tableName + " where " + columnName2 + "= ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, value);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				findValue = rs.getString(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if (findValue == null || findValue.trim().length()== 0)
			{
				findValue = "";
			}
		} catch (Exception e)
		{
			System.out.println("Exception in findValue ");
			e.printStackTrace();
			throw new ITMException(e);
		}
		//Added by Jaffar on 4th Apr 18 ---> Start
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
		//---- End ----
		
		System.out.println("returning String from findValue " + findValue);
		return findValue;
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
