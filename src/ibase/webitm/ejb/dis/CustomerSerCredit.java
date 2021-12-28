
/********************************************************
	Title : CustomerSerCredit
	Date  : 1/04/2012
	Developer: Navanath Nawale

 ********************************************************/

package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.fin.FinCommon;
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
public class CustomerSerCredit extends ValidatorEJB implements CustomerSerCreditLocal,CustomerSerCreditRemote {
	GenericUtility genericUtility = GenericUtility.getInstance();
	FinCommon finCommon = new FinCommon();

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
			System.out.println("Val Xtraparam :: " + xtraParams );
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
		String itemSer = "";
		// Variables declared by Mahesh Saggam on 29-05-2019 [Start]
		String siteCode = "", modName = "";
		// Variables declared by Mahesh Saggam on 29-05-2019 [End]
		String custCode = "";
		String terrCode = "";
		String crTerm = "";
		String salesPers = "";
		String salesPers1 = "";
		String salesPers2 = "";
		String levelCode = "";
		String dlvTerm = "";
		String userId = "";
		String sql="";
		String errCode="";
		String errorType = "";
		String childNodeName = null;
		String errString = "";
		String diskList = "";// Change By Nasruddin [9/SEP/16]

		int ctr=0;
		int childNodeListLength;
		long count = 0;

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
			// Changes by Mahesh Saggam on 29-05-2019 [Start]
			modName = ("w_" + getValueFromXTRA_PARAMS(xtraParams, "obj_name")).toUpperCase();
			System.out.println("transer=============================" + modName);
			// Changes by Mahesh Saggam on 29-05-2019 [End]
			parentNodeList = dom.getElementsByTagName("Detail1");
			parentNode = parentNodeList.item(0);
			childNodeList = parentNode.getChildNodes();
			childNodeListLength = childNodeList.getLength();
			for(ctr = 0; ctr < childNodeListLength; ctr ++)
			{
				childNode = childNodeList.item(ctr);
				childNodeName = childNode.getNodeName();
				if(childNodeName.equalsIgnoreCase("item_ser"))
				{    
					itemSer = genericUtility.getColumnValue("item_ser", dom);
					sql = "select count(*) from itemser where item_ser = ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,itemSer);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						count =  rs.getInt(1);																
					}
					if(count == 0) 
					{
						errCode = "VTITEMSER1";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
				else if(childNodeName.equalsIgnoreCase("cust_code"))
				{    
					custCode = genericUtility.getColumnValue("cust_code", dom);
					sql = "select count(*) from customer where cust_code = ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,custCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						count =  rs.getInt(1);															
					}
					if(count == 0) 
					{
						errCode = "VMCUST1";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}	
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
				if(childNodeName.equalsIgnoreCase("terr_code"))
				{    
					terrCode = genericUtility.getColumnValue("terr_code", dom);
					if(terrCode != null && terrCode.trim().length() > 0)
					{
						sql = "select count(*) from territory where terr_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,terrCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							count =  rs.getInt(1);															
						}
						if(count == 0) 
						{
							errCode = "VTTERRCD";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}	
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				}
				if(childNodeName.equalsIgnoreCase("cr_term"))
				{    
					crTerm = genericUtility.getColumnValue("cr_term", dom);
					sql = "select count(*) from crterm where cr_term = ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,crTerm);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						count =  rs.getInt(1);															
					}
					if(count == 0) 
					{
						errCode = "VTCRTERM1";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}	
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;		
				}
				/*if(childNodeName.equalsIgnoreCase("sales_pers"))
				{    
					salesPers = genericUtility.getColumnValue("sales_pers", dom);
					sql = "select count(*) from sales_pers where sales_pers = ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,salesPers);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						count =  rs.getInt(1);															
					}
					if(count == 0) 
					{
						errCode = "VMSLPERS1";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}	
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;		
				}*/
				
				//sales_pers
				
				// Changes by Mahesh Saggam on 29-05-2019 [Start]
				if (childNodeName.equalsIgnoreCase("sales_pers"))
				{
					String mItemSer = "", lsSalesPersYn = "", orderDateStr = "";
					salesPers = genericUtility.getColumnValue("sales_pers", dom);
					mItemSer = genericUtility.getColumnValue("item_ser", dom);

					if (salesPers != null && salesPers.trim().length() > 0)
					{
						siteCode = genericUtility.getColumnValue("site_code", dom);
						orderDateStr = genericUtility.getColumnValue("order_date", dom);
						errCode = finCommon.isSalesPerson(siteCode, salesPers, modName, conn);
						if(errCode !=null && errCode.trim().length()> 0)//error code cheacking added by nandkuamr gadkari on 1/06/19
						{						
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else
					{
						sql = "select (case when sales_pers_yn is null then 'N' else sales_pers_yn end) as sales_pers_yn from itemser where  item_ser =?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, mItemSer);
						rs = pstmt.executeQuery();
						if (rs.next()) 
						{
							lsSalesPersYn = rs.getString("sales_pers_yn");
						}
						System.out.println("lsSalesPersYn = " + lsSalesPersYn);
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						
						if (lsSalesPersYn.equalsIgnoreCase("Y")) 
						{
							errCode = "VMSLPERS1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							System.out.println(" Value for lsSalesPersYn = " + lsSalesPersYn );
						}
					}
				}
					//Changed By Mahesh Saggam on 29-05-2019[End]
				if(childNodeName.equalsIgnoreCase("sales_pers__1"))
				{    
					salesPers1 = genericUtility.getColumnValue("sales_pers__1", dom);
					if(salesPers1 != null && salesPers1.trim().length() > 0)
					{
						sql = "select count(*) from sales_pers where sales_pers = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,salesPers1);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							count =  rs.getInt(1);															
						}
						if(count == 0) 
						{
							errCode = "VMSLPERS1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}	
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;		
					}
				}
				if(childNodeName.equalsIgnoreCase("sales_pers__2"))
				{    
					salesPers2 = genericUtility.getColumnValue("sales_pers__2", dom);
					if(salesPers2 != null && salesPers2.trim().length() > 0)
					{
						sql = "select count(*) from sales_pers where sales_pers = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,salesPers2);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							count =  rs.getInt(1);															
						}
						if(count == 0) 
						{
							errCode = "VMSLPERS1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}	
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;		
					}
				}
				//Changed by Amey W on 23/08/18 [remove column level_code__hier, suggested by KB Sir] START
				/*if(childNodeName.equalsIgnoreCase("level_code__hier"))
				{    
					levelCode = genericUtility.getColumnValue("level_code__hier", dom);
					sql = "select count(*) from hierarchy where level_code = ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,levelCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						count =  rs.getInt(1);															
					}
					if(count == 0) 
					{
						errCode = "VMHIER1";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}	
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;		
				}*/
				//Changed by Amey W on 23/08/18 [remove column level_code__hier, suggested by KB Sir] END
				if(childNodeName.equalsIgnoreCase("dlv_term"))
				{    
					dlvTerm = genericUtility.getColumnValue("dlv_term", dom);
					//if(dlvTerm != null && salesPers2.trim().length() > 0) 
					if(dlvTerm != null && dlvTerm.trim().length() > 0) // Changed By Nasruddin [9/SEP/16]
					{
						sql = "select count(*) from delivery_term where dlv_term = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,dlvTerm);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							count =  rs.getInt(1);															
						}
						if(count == 0) 
						{
							errCode = "VMDLVTERM1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}	
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;		
					}
				}
			// Changed By Nasruddin Khan [09/SEP/16] Start	
				if(childNodeName.equalsIgnoreCase("disc_list"))
				{    
					diskList = genericUtility.getColumnValue("disc_list", dom);
					if(diskList != null && diskList.trim().length() > 0)
					{
						sql = "SELECT COUNT(1) FROM DISC_LIST WHERE DISC_LIST = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,diskList);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							count =  rs.getInt(1);															
						}
						if(count == 0) 
						{
							errCode = "VTDISCLT1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}	
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;		
					}
				}
			// Changed By Nasruddin Khan [09/SEP/16] END	
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
			System.out.println("Exception : [CustomerSerCredit][itemChanged( String, String )] :==>\n" + e.getMessage());
		}
		return valueXmlString;
	}

	// method for item change
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		//Declare variable
		String itemSer = "";
		String siteCode = "";
		String terrCode = "";
		String siteCodePl = "";
		String custName = "";
		String custCode = "";
		String salesPers = "";
		String salesPers1 = "";
		String salesPers2 = "";
		String spName = "";
		String crTerm = "";
		String reasCode = "";
		String blackListed = "";
		String reasCodebk = "";
		String descr = "";
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

			if(currentColumn.trim().equalsIgnoreCase("itm_defaultedit"))
			{
				siteCode =genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
				if(siteCode != null && siteCode.trim().length() > 0)
				{
					valueXmlString.append("<cust_site_code>").append("<![CDATA[" + siteCode +"]]>").append("</cust_site_code>");
				}
				else 
				{
					valueXmlString.append("<cust_site_code>").append("<![CDATA[" + "" +"]]>").append("</cust_site_code>");
				}
				reasCode =genericUtility.getColumnValue("reas_code__bklist", dom);
				sql = " select descr from gencodes where fld_name = 'REAS_CODE__BKLIST' and fld_value = ?";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,reasCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					descr = rs.getString("descr");
				}
				valueXmlString.append("<bklist_reason>").append("<![CDATA[" + descr + "]]>").append("</bklist_reason>");
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				blackListed =genericUtility.getColumnValue("black_listed", dom);
				if("N".equalsIgnoreCase(blackListed))
				{
					//String isNull = null;
					String isNull = "";
					valueXmlString.append("<reas_code__bklist protect = \"1\">").append("<![CDATA[" + isNull + "]]>").append("</reas_code__bklist>");
					valueXmlString.append("<bklist_reason>").append("<![CDATA[" + isNull + "]]>").append("</bklist_reason>");
				}
				else 
				{
					valueXmlString.append("<reas_code__bklist protect = \"1\">").append("</reas_code__bklist>");
				}
			}
			else if(currentColumn.trim().equalsIgnoreCase("itm_default"))	
			{
				//itemSer =(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "item_ser"));
				valueXmlString.append("<item_ser>").append("<![CDATA[" + itemSer + "]]>").append("</item_ser>");
				reasCode =genericUtility.getColumnValue("reas_code__bklist", dom);
				sql = " select descr from gencodes where fld_name = 'REAS_CODE__BKLIST' and fld_value = ?";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,reasCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					descr = rs.getString("descr");
				}
				valueXmlString.append("<bklist_reason>").append("<![CDATA[" + descr + "]]>").append("</bklist_reason>");
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				blackListed =genericUtility.getColumnValue("black_listed", dom);
				if("N".equalsIgnoreCase(blackListed))
				{
					//String isNull = null;
					String isNull = "";
					valueXmlString.append("<reas_code__bklist protect = \"1\">").append("<![CDATA[" + isNull + "]]>").append("</reas_code__bklist>");
					valueXmlString.append("<bklist_reason>").append("<![CDATA[" + isNull + "]]>").append("</bklist_reason>");
				}
				else 
				{
					valueXmlString.append("<reas_code__bklist protect = \"1\">").append("</reas_code__bklist>");
				}
			}
			else if(currentColumn.trim().equalsIgnoreCase("cust_code"))	
			{
				custCode =genericUtility.getColumnValue("cust_code", dom);
				sql = "  Select cust_name,site_code,site_code__pbus from customer where cust_code = ?";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,custCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					custName = rs.getString("cust_name");
					siteCode = rs.getString("site_code");
					siteCodePl = rs.getString("site_code__pbus");
				}
				valueXmlString.append("<customer_cust_name>").append("<![CDATA[" + custName + "]]>").append("</customer_cust_name>");
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				//Changed By Nasruddin  01/10/16 
				//if(siteCode.trim().length() > 0)
				if(siteCode != null && siteCode.trim().length() > 0)
				{
					valueXmlString.append("<cust_site_code>").append("<![CDATA[" + siteCode + "]]>").append("</cust_site_code>");
				}
				else
				{
					valueXmlString.append("<cust_site_code>").append("<![CDATA[" + "" + "]]>").append("</cust_site_code>");
				}
				////Changed By Nasruddin  01/10/16 Start
				//if(siteCodePl.trim().length() > 0)
				if(siteCodePl != null && siteCodePl.trim().length() > 0)
				{
					valueXmlString.append("<site_code__pbus>").append("<![CDATA[" + siteCodePl + "]]>").append("</site_code__pbus>");
				}
				else 
				{
					valueXmlString.append("<site_code__pbus>").append("<![CDATA[" + "" + "]]>").append("</site_code__pbus>");
				}
			}
			else if(currentColumn.trim().equalsIgnoreCase("terr_code"))	
			{
				terrCode =genericUtility.getColumnValue("terr_code", dom);
				if(terrCode != null && terrCode.trim().length() > 0)
				{
					sql = " select descr from territory where terr_code  = ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,terrCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						descr = rs.getString("descr");
					}
					valueXmlString.append("<territory_descr>").append("<![CDATA[" + descr + "]]>").append("</territory_descr>");
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
			}
			else if(currentColumn.trim().equalsIgnoreCase("cr_term"))	
			{
				crTerm =genericUtility.getColumnValue("cr_term", dom);
				sql = " select descr from crterm  where cr_term  = ?";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,crTerm);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					descr = rs.getString("descr");
				}
				valueXmlString.append("<crterm_descr>").append("<![CDATA[" + descr + "]]>").append("</crterm_descr>");
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}
			else if(currentColumn.trim().equalsIgnoreCase("sales_pers"))	
			{
				salesPers =genericUtility.getColumnValue("sales_pers", dom);
				sql = " select sp_name from sales_pers  where sales_pers  = ?";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,salesPers);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					spName = rs.getString("sp_name");
				}
				valueXmlString.append("<sp_name>").append("<![CDATA[" + spName + "]]>").append("</sp_name>");
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}
			else if(currentColumn.trim().equalsIgnoreCase("sales_pers__1"))	
			{
				salesPers1 =genericUtility.getColumnValue("sales_pers__1", dom);
				sql = " select sp_name from sales_pers  where sales_pers  = ?";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,salesPers1);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					spName = rs.getString("sp_name");
				}
				//Rename sales_pers_sp_name to sales_pers_name_1 (suggested by KB sir) START
				//valueXmlString.append("<sales_pers_sp_name>").append("<![CDATA[" + spName + "]]>").append("</sales_pers_sp_name>");
				valueXmlString.append("<sales_pers_name_1>").append("<![CDATA[" + spName + "]]>").append("</sales_pers_name_1>");
				//Rename sales_pers_sp_name to sales_pers_name_1 suggested by KB sir) END
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}
			else if(currentColumn.trim().equalsIgnoreCase("sales_pers__2"))	
			{
				salesPers2 =genericUtility.getColumnValue("sales_pers__2", dom);
				sql = " select sp_name from sales_pers  where sales_pers  = ?";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,salesPers2);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					spName = rs.getString("sp_name");
				}
				//Rename sales_pers_sp_name_1 to sales_pers_name_2 (suggested by KB sir) START
				//valueXmlString.append("<sales_pers_sp_name_1>").append("<![CDATA[" + spName + "]]>").append("</sales_pers_sp_name_1>");
				valueXmlString.append("<sales_pers_name_2>").append("<![CDATA[" + spName + "]]>").append("</sales_pers_name_2>");
				//Rename sales_pers_sp_name_1 to sales_pers_name_2 (suggested by KB sir) END
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}
			else if(currentColumn.trim().equalsIgnoreCase("black_listed"))	
			{
				blackListed =genericUtility.getColumnValue("black_listed", dom);
				if("N".equalsIgnoreCase(blackListed))
				{
					//Comment by Nasruddin 13-10-16 set blank value in black_listed_date, bklist_reason field
					//java.util.Date ldDate = null;
					//java.util.Date lsNull = null;
					valueXmlString.append("<black_listed_date>").append("<![CDATA[]]>").append("</black_listed_date>");
					valueXmlString.append("<bklist_reason>").append("<![CDATA[]]>").append("</bklist_reason>");
					valueXmlString.append("<reas_code__bklist protect = \"1\">").append("<![CDATA[]]>").append("</reas_code__bklist>");
				}
				else 
				{
					valueXmlString.append("<reas_code__bklist protect = \"0\">").append("</reas_code__bklist>");
				}
			}
			else if(currentColumn.trim().equalsIgnoreCase("reas_code__bklist"))	
			{
				reasCodebk =genericUtility.getColumnValue("reas_code__bklist", dom);
				sql = " select descr from gencodes where fld_name = 'REAS_CODE__BKLIST' and fld_value = ?";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,reasCodebk);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					descr = rs.getString("descr");
				}
				valueXmlString.append("<bklist_reason>").append("<![CDATA[" + descr + "]]>").append("</bklist_reason>");
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




