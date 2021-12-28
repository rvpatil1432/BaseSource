
/********************************************************
	Title : SiteCustomer
	Date  : 15/10/11
	Developer: Navanath Nawale

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
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class SiteCustomer extends ValidatorEJB implements SiteCustomerLocal,SiteCustomerRemote {

		//changed by nasruddin 05-10-16
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
			System.out.println("Val xmlString :: " + xmlString);
			System.out.println("Val xmlString1 :: " + xmlString1);
			System.out.println("Val xmlString2 :: " + xmlString2 );
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
		String childNodeName = null;
		String siteCode = "";
		String siteCodeCh = "";
		String custCode ="";
		String terrCode = "";
		String taxClass="";
		String priceListDisc="";
		String priceList="";
		String priceListClg="";
		String channelPart="";
		String CurrCodeFrt="";
		String currCodeIns="";
		String userId = "";
		String sql="";
		String errCode="";
		String errorType = "";
		String errString = "";
		String salesPerson="";
		int ctr=0;
		int childNodeListLength;

		long count = 0;
		double adhoc = 0.0;
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
				if(childNodeName.equalsIgnoreCase("site_code"))
				{    
					siteCode = genericUtility.getColumnValue("site_code", dom);
					sql = "select count(*) from site where site_code = ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,siteCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						count =  rs.getInt(1);															
					}
					if(count == 0) 
					{
						errCode = "VMSITE1";
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
					sql = "select count(*) from customer  where cust_code = ? ";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,custCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						count =  rs.getInt(1);								
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if(count == 0) 
					{
						errCode = "VMCUST1";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}	
					// Changed By Nasruddin START [22-SEP-16]
					else
					{
						if("A".equals(editFlag) && errCode.trim().length() == 0)
						{
							custCode = genericUtility.getColumnValue("cust_code", dom);
							siteCode = genericUtility.getColumnValue("site_code", dom);
							
							sql = "SELECT COUNT(1) FROM SITE_CUSTOMER WHERE SITE_CODE = ? AND CUST_CODE = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1, siteCode);
							pstmt.setString(2, custCode);
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
								errCode = "VMPMKY";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}	
							
						}
					}
					// Changed By Nasruddin END [22-SEP-16]
					
				}
				else if(childNodeName.equalsIgnoreCase("terr_code"))
				{
					terrCode = genericUtility.getColumnValue("terr_code", dom);
					if(terrCode  != null && terrCode.trim().length() > 0)
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
				else if(childNodeName.equalsIgnoreCase("tax_class"))
				{
					taxClass = genericUtility.getColumnValue("tax_class", dom);
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
						if(count == 0) 
						{
							errCode = "VTTAXCLA1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}	
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				}
				else if(childNodeName.equalsIgnoreCase("price_list__disc"))
				{
					priceListDisc = genericUtility.getColumnValue("price_list__disc", dom);    
					if( priceListDisc != null && priceListDisc.trim().length() > 0)
					{
						//Added by sarita on 01DEC2017 
						//sql = "select count(*) from pricelist where price_list__disc = ?";
						sql = "select count(*) from pricelist where price_list = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,priceListDisc);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							count =  rs.getInt(1);
						}
						if(count == 0) 
						{
							errCode = "VTPLIST1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				}
				else if(childNodeName.equalsIgnoreCase("price_list"))
				{
					priceList = genericUtility.getColumnValue("price_list", dom);
					if(priceList != null && priceList.trim().length() > 0 )
					{
						sql = "select count(*) from pricelist where price_list = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,priceList);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							count =  rs.getInt(1);
						}
						if(count == 0) 
						{
							errCode = "VTPLIST1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}	   
				}
				else if(childNodeName.equalsIgnoreCase("price_list__clg"))
				{
					priceListClg = genericUtility.getColumnValue("price_list__clg", dom);
					if(priceListClg != null && priceListClg.trim().length() > 0)
					{
						sql = "select count(*) from pricelist_mst where price_list = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,priceListClg);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							count =  rs.getInt(1);
						}
						if(count == 0) 
						{
							errCode = "VTPLIST1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}	
				}  
				else if(childNodeName.equalsIgnoreCase("site_code__ch"))
				{
					siteCodeCh = checkNull(genericUtility.getColumnValue("site_code__ch", dom));
					channelPart =checkNull(genericUtility.getColumnValue("channel_partner", dom));
					//Changes by Nandkumar Gadkari on 09/03/2018 -----start-----------
					if(siteCodeCh == null || "Y".equalsIgnoreCase(channelPart.trim()) && siteCodeCh.trim().length()== 0  )
					{
						errCode = "VMSITECD1";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
					else if("N".equalsIgnoreCase(channelPart.trim()) && siteCodeCh.trim().length() > 0   )
					{
						errCode = "VNRSITE";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
					else if( "Y".equalsIgnoreCase(channelPart.trim()) && siteCodeCh.trim().length() > 0 )
					{
						sql = "select count(*) from site where site_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,siteCodeCh);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							count =  rs.getInt(1);
						}
						if(count == 0) 
						{
							errCode = "VTSITECD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}	
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				}
				//Changes by Nandkumar Gadkari on 09/03/2018 -----end-----------
				else if(childNodeName.equalsIgnoreCase("adhoc_repl_perc"))
				{
					/*Changed by Mayur Nair on 30-05-2017,to remove NULLPointerException [Start]
					  adhoc = Double.parseDouble(genericUtility.getColumnValue("adhoc_repl_perc", dom));*/
					
                   String adhoc1 = genericUtility.getColumnValue("adhoc_repl_perc", dom);
					
					if(adhoc1 == null)
					  {
						adhoc = 0;  
						
				        }
					else
					{
						adhoc = Double.parseDouble(adhoc1);
						
					}
					//Changed by Mayur Nair on 30-05-2017,to remove NULL Pointer Exception [End]
					if( adhoc != 0  &&  adhoc < 0 ||   adhoc > 100)
					{
						errCode = "VTADH";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}					   
				}

				else if(childNodeName.equalsIgnoreCase("curr_code__frt"))
				{
					CurrCodeFrt = genericUtility.getColumnValue("curr_code__frt", dom);
					
						sql = "select count(*) from currency  where curr_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,CurrCodeFrt);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							count =  rs.getInt(1);
						}
						if(count == 0) 
						{
							errCode = "VECUR2";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
				 
				}
				
				//Added by Shubham.S.B on 10-03-2021
				// sales person Validation
				else if(childNodeName.equalsIgnoreCase("sales_pers"))
				{
					salesPerson = genericUtility.getColumnValue("sales_pers", dom);
					if(salesPerson != null && salesPerson.trim().length() > 0)
					{
						sql = "select count(*) from sales_pers where sales_pers = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,salesPerson);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							count =  rs.getInt(1);
						}
						if(count == 0) 
						{
							errCode = "INVSALPERS";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				 
				}
				// sales person Validation ended
				
				//Added by Shubham.S.B on 10-03-2021
				// sales person 1 Validation
				else if(childNodeName.equalsIgnoreCase("sales_pers__1"))
				{
					salesPerson = genericUtility.getColumnValue("sales_pers__1", dom);
					
					if(salesPerson != null && salesPerson.trim().length() > 0)
					{
						sql = "select count(*) from sales_pers where sales_pers = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,salesPerson);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							count =  rs.getInt(1);
						}
						if(count == 0) 
						{
							errCode = "VTINVALSP1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				 
				}
				// sales person 1 Validation ended
				
				//Added by Shubham.S.B on 10-03-2021
				// sales person 2 Validation
				else if(childNodeName.equalsIgnoreCase("sales_pers__2"))
				{
					salesPerson = genericUtility.getColumnValue("sales_pers__2", dom);
					
					if(salesPerson != null && salesPerson.trim().length() > 0)
					{
						sql = "select count(*) from sales_pers where sales_pers = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,salesPerson);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							count =  rs.getInt(1);
						}
						if(count == 0) 
						{
							errCode = "VTINVALSP2";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
				 
				}
				// sales person 2 Validation ended
				
				else if(childNodeName.equalsIgnoreCase("curr_code__ins"))
				{
					currCodeIns = genericUtility.getColumnValue("curr_code__ins", dom);

					sql = "select count(*) from currency  where curr_code = ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,currCodeIns);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						count =  rs.getInt(1);
					}
					if(count == 0) 
					{
						errCode = "VECUR2";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
				}
					
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
			System.out.println("Exception : [MiscVal][itemChanged( String, String )] :==>\n" + e.getMessage());
		}
		return valueXmlString;
	}

	// method for item change
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		//Declare variable
		String custType = "";
		String custName = "";
		String siteCode = ""; 
		String custCode = "";
		String terrCode = "";
		String terrDescr = "";
		String channelPartner = "";
		String nullVar= "";
		String sql = "";
		String salesPers = "", salesPers1 = "", salesPers2 = "";  // variables declared by Mahesh Saggam on 24-05-2019
		
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
			
			if(currentColumn.trim().equalsIgnoreCase("itm_default"))
			{
				siteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
				valueXmlString.append("<site_code>").append("<![CDATA[" +  siteCode + "]]>").append("</site_code>");
				if(siteCode != null && siteCode.trim().length() > 0)
				{
					valueXmlString.append("<site_code protect = \"0\">").append ("</site_code>");//changed by mukesh chauhan on 09/05/2020
				}
			}
			else if(currentColumn.trim().equalsIgnoreCase("cust_code"))
			{
				custCode = genericUtility.getColumnValue("cust_code", dom);
				sql = " select cust_type,cust_name, sales_pers, sales_pers__1, sales_pers__2  from customer  where cust_code = ?";
				pstmt =  conn.prepareStatement(sql);
				pstmt.setString(1,custCode);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					custType = rs.getString("cust_type");
					custName = rs.getString("cust_name");
					// Added by Mahesh Saggam on 24-05-2019
					salesPers = rs.getString("sales_pers");
					salesPers1 = rs.getString("sales_pers__1");
					salesPers2 = rs.getString("sales_pers__2");
				}
				valueXmlString.append("<cust_type>").append("<![CDATA[" + custType +"]]>").append("</cust_type>");
				valueXmlString.append("<cust_name>").append("<![CDATA[" + custName +"]]>").append("</cust_name>");
				// Added by Mahesh Saggam on 24-05-2019
				valueXmlString.append("<sales_pers>").append("<![CDATA[" + salesPers +"]]>").append("</sales_pers>");
				valueXmlString.append("<sales_pers__1>").append("<![CDATA[" + salesPers1 +"]]>").append("</sales_pers__1>");
				valueXmlString.append("<sales_pers__2>").append("<![CDATA[" + salesPers2 +"]]>").append("</sales_pers__2>");
				
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}
			else if(currentColumn.trim().equalsIgnoreCase("terr_code"))	
			{
				terrCode =genericUtility.getColumnValue("terr_code", dom);
				if(terrCode != null && terrCode.trim().length() > 0 )
				{
					sql = " select descr from territory   where terr_code  = ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,terrCode);
					rs = pstmt.executeQuery();
					if(sql != null)
					{
						if(rs.next())
						{
							terrDescr = rs.getString("descr");
						}
					}
					valueXmlString.append("<terr_code>").append("<![CDATA[" + terrDescr + "]]>").append("</terr_code>");
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;		
				}
			}
			else if(currentColumn.trim().equalsIgnoreCase("channel_partner"))
			{
				String finlink="",sitecodech="",dislink="";
				channelPartner = genericUtility.getColumnValue("channel_partner", dom);
				sitecodech = genericUtility.getColumnValue("site_code__ch", dom);
				finlink = genericUtility.getColumnValue("fin_link", dom);
				dislink = genericUtility.getColumnValue("dis_link", dom);
				System.out.println("site_code__ch["+sitecodech+"]fin_link"+finlink+"]dis_link"+dislink+"]");
				if("Y".equalsIgnoreCase(channelPartner))
				{
					//valueXmlString.append("<site_code_ch protect = \"0\">").append ("</site_code_ch>");
					valueXmlString.append("<site_code__ch protect = \"0\">").append("<![CDATA[" +  nullVar + "]]>").append("</site_code__ch>");
					valueXmlString.append("<fin_link protect = \"0\">").append("<![CDATA[" +  finlink + "]]>").append("</fin_link>");
					valueXmlString.append("<dis_link protect = \"0\">").append("<![CDATA[" +  dislink + "]]>").append("</dis_link>");
					//valueXmlString.append("<fin_link protect = \"0\">").append ("</fin_link>");
					//valueXmlString.append("<dis_link protect = \"0\">").append ("</dis_link>");
				}
				else
				{  /* Changed By Nasruddin Start 03-10-16
					valueXmlString.append("<site_code__ch>").append("<site_code_ch protect = \"1\">").append("<![CDATA[" +  nullVar + "]]>").append("</site_code__ch>");
					valueXmlString.append("<fin_link>").append("<fin_link protect = \"1\">").append("<![CDATA[" +  nullVar + "]]>").append("</fin_link>");
					valueXmlString.append("<dis_link>").append("<dis_link protect = \"1\">").append("<![CDATA[" +  nullVar + "]]>").append("</dis_link>");*/
					valueXmlString.append("<site_code__ch protect = \"1\">").append("<![CDATA[" +  nullVar + "]]>").append("</site_code__ch>");
					valueXmlString.append("<fin_link protect = \"1\">").append("<![CDATA[" +  nullVar + "]]>").append("</fin_link>");
					valueXmlString.append("<dis_link protect = \"1\">").append("<![CDATA[" +  nullVar + "]]>").append("</dis_link>");
					//Changed By Nasruddin END 03-10-16
				}
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
	
	
	private String checkNull(String str)
	{
		if (str == null)
		{
			return "";
		}

		return str;
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



