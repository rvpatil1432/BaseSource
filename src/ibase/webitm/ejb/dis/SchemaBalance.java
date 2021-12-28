 /**
 * Author : Saiprasad
 * Date   : 
 * 
 * */
 
package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.*;
import java.text.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import org.w3c.dom.*;

import ibase.system.config.*;
import ibase.webitm.ejb.*; 

import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.dis.*;
import ibase.webitm.utility.TransIDGenerator;
import ibase.utility.CommonConstants;

@javax.ejb.Stateless
public class SchemaBalance extends ValidatorEJB 
{
	/**
	* The method defined with no paramter and returns nothing
	*/
	E12GenericUtility genericUtility=new E12GenericUtility();
	public String wfValData() throws RemoteException, ITMException
	{
		return "";
	}
	
	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		System.out.println("*************WhServiceContract wfValData(String xmlString, String xmlString1, String objContext, String editFlag, String xtraParams)********");
		String errString = null;
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			System.out.println("xmlString:-" + xmlString );
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			System.out.println("xmlString1:-"+xmlString1);
			dom2=parseString(xmlString2);
			System.out.println("xmlString2:-"+xmlString2);
			errString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams);
			System.out.println("errString[" + errString+"]" );
		}
		catch(Exception e)
		{
			throw new ITMException( e );
		}
		return (errString);
	}
	
	public String wfValData(Document dom, Document dom1,Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{	
		NodeList parentNodeList = null;NodeList childNodeList = null;
		Node parentNode = null;Node childNode = null;
		String childNodeName = null;
		Connection conn = null;
		PreparedStatement pstmt = null ;
		ResultSet rs = null , rs1 = null;
		int cnt = 0, currentFormNo=0,childNodeListLength ,ctr=0;
        ConnDriver connDriver = new ConnDriver();
		String sql = "",errString = "", errCode  = "",userId = "", tranId = "";
		ArrayList <String> errList = new ArrayList<String>();
		ArrayList <String>errFields = new ArrayList <String> ();
		String errorType = "";
		String custCode = "", effFrom="",effUpto="",itemCode="",balaceFreeQty="",usedFreeQty="",schmeCode="",autoReplCredit="",custCodeRepl="";
		Timestamp eff_from=null,eff_upto=null,toDate=null,fromDate=null;
		//String rate=null,unit=null;
		StringBuffer errStringXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root><Errors>"); 
		try
		{
			connDriver = null;
			conn = getConnection();
			userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			System.out.println("userId:- "  + userId);
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
				System.out.println("current Form No:"+currentFormNo);
			}
			switch(currentFormNo)
			{
			case 1:
				parentNodeList = dom.getElementsByTagName("Detail1");
				int parentNodeListLen = parentNodeList.getLength();
				System.out.println("Number of node in parentNodeListLen : "+parentNodeListLen);
				for ( int rowCnt=0; rowCnt < parentNodeListLen; rowCnt++ )
				{
					parentNode = parentNodeList.item(rowCnt);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
					for(ctr = 0; ctr < childNodeListLength; ctr++)
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						System.out.println("childNodeName R : "+childNodeName);
						if(childNodeName.equalsIgnoreCase("scheme_code"))
						{
							schmeCode=E12GenericUtility.checkNull(genericUtility.getColumnValue("scheme_code", dom));
							System.out.println("Scheme code:"+schmeCode);
							if(E12GenericUtility.checkNull(schmeCode).length() <= 0)
						{
								errCode = "VTSCHBLNK";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;	
							}
						}
						else if (childNodeName.equalsIgnoreCase("cust_code"))
						{
							custCode = E12GenericUtility.checkNull(genericUtility.getColumnValue("cust_code", dom));
							System.out.println("Customer code:"+custCode);
							if(E12GenericUtility.checkNull(custCode).length() <= 0)
							{
								errCode = "VTCUSTBLNK";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;	
							}
							else
							{
								String str = "select count(1) from customer where cust_code = ?";
								int count = 0;
								pstmt = conn.prepareStatement(str);
								pstmt.setString(1, custCode);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									count = rs.getInt(1);
								}
								System.out.println("count : "+count);
								pstmt.close();
								pstmt = null;
								rs.close();
								rs = null;
								if(count == 0)
								{
									errCode = "CUSTNOTEXT";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									break;	
								}
							}
						}
						else if (childNodeName.equalsIgnoreCase("item_code"))
						{
							itemCode = E12GenericUtility.checkNull(genericUtility.getColumnValue("item_code", dom));
							System.out.println("Item code:"+itemCode);
							if(E12GenericUtility.checkNull(itemCode).length() <= 0)
							{
								errCode = "VTITEMBLNK";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;	
							}
							else
							{
								if(!"X".equalsIgnoreCase(itemCode))
									{
										String str = "select count(1) from item where item_code = ?";
										int count = 0;
										pstmt = conn.prepareStatement(str);
										pstmt.setString(1, itemCode);
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											count = rs.getInt(1);
										}
										System.out.println("count : "+count);
										pstmt.close();
										pstmt = null;
										rs.close();
										rs = null;
										if(count == 0)
										{
											errCode = "ITEMNOTEXT";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
											break;	
										}
									}
							}
						}
						else if (childNodeName.equalsIgnoreCase("eff_from"))
						{
							effFrom = E12GenericUtility.checkNull(genericUtility.getColumnValue("eff_from", dom));
							System.out.println("EffFrom:"+effFrom);
							if(effFrom.length() <= 0 )
							{
								errCode = "VTINVFRDAT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
							fromDate = Timestamp.valueOf(genericUtility.getValidDateString(effFrom,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())
									+ " 00:00:00.0");
							System.out.println("from Date"+fromDate);
							if(fromDate == null)
							{
								errCode = "VTINVFRDAT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
						}
						else if (childNodeName.equalsIgnoreCase("valid_upto"))
						{
							effFrom = E12GenericUtility.checkNull(genericUtility.getColumnValue("eff_from", dom));
							System.out.println("EffFrom:"+effFrom);
							effUpto = E12GenericUtility.checkNull(genericUtility.getColumnValue("valid_upto", dom));
							System.out.println("Effupto:"+effUpto);
							if(effFrom.length() <= 0 || effUpto.length() <= 0)
							{
								errCode = "VTINVVLDAT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
							fromDate = Timestamp.valueOf(genericUtility.getValidDateString(effFrom,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())
									+ " 00:00:00.0");
							System.out.println("from Date"+fromDate);
							toDate = Timestamp.valueOf(genericUtility.getValidDateString(effUpto,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())
									+ " 00:00:00.0");
                           System.out.println("to Date"+toDate);
							if(toDate == null || fromDate == null)
							{
								errCode = "VTINVVLDAT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
							if(toDate.before(fromDate))
							{
								errCode = "INVFROMUP";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
							
						}
						else if(childNodeName.equalsIgnoreCase("balance_free_qty"))
						{
							balaceFreeQty=E12GenericUtility.checkNull(genericUtility.getColumnValue("balance_free_qty", dom));
							itemCode=E12GenericUtility.checkNull(genericUtility.getColumnValue("item_code", dom));
							int balance = 0;
							if(balaceFreeQty.length() > 0)
							{
								try
								{
									balance = Integer.parseInt(balaceFreeQty);
								}
								catch(NumberFormatException nfe)
								{
									errCode = "INVBALQTY";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								catch(Exception e)
								{
									errCode = "INVBALQTY";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							
							if(balance <= 0)
							{
								if(! "X".equalsIgnoreCase(itemCode))
								{
									errCode = "INVBALQTY";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									break;
								}
							}
						}
						else if(childNodeName.equalsIgnoreCase("used_free_qty"))
						{
							usedFreeQty=E12GenericUtility.checkNull(genericUtility.getColumnValue("used_free_qty", dom));
							itemCode=E12GenericUtility.checkNull(genericUtility.getColumnValue("item_code", dom));
							int usedFree = 0;
							if(usedFreeQty.length() > 0)
							{
								try
								{
									usedFree = Integer.parseInt(usedFreeQty);
								}
								catch(NumberFormatException nfe)
								{
									errCode = "INVUSEQTY";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								catch(Exception e)
								{
									errCode = "INVUSEQTY";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							//Commented by Vikas L to remove validation [17/10/2018]start
							/*if(usedFree <= 0)
							{
								if(! "X".equalsIgnoreCase(itemCode))
								{
									errCode = "INVUSEQTY";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									break;
								}
							}*/
							//Commented by Vikas L to remove validation [17/10/2018]end
						}
						else if(childNodeName.equalsIgnoreCase("cust_code__repl"))
						{
							custCodeRepl = E12GenericUtility.checkNull(genericUtility.getColumnValue("cust_code__repl", dom));
							System.out.println("Customer code:"+custCodeRepl);
							if(E12GenericUtility.checkNull(custCodeRepl).length() <= 0)
							{
								errCode = "VTRPLCST";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;	
							}
							else
							{
								String str = "select count(1) from customer where cust_code = ?";
								int count = 0;
								pstmt = conn.prepareStatement(str);
								pstmt.setString(1, custCodeRepl);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									count = rs.getInt(1);
								}
								System.out.println("count : "+count);
								pstmt.close();
								pstmt = null;
								rs.close();
								rs = null;
								if(count == 0)
								{
									errCode = "RPLNOEXT";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									break;	
								}
							}
						
						}
					} 
				}
				break;
			}
			int errListSize = errList.size();
			cnt =0;
			String errFldName = null;
			if ( errList != null && errListSize > 0 )
			{
				System.out.println("Inside errList >"+errList);
				for (cnt = 0; cnt < errListSize; cnt++)
				{
					errCode = (String)errList.get(cnt);
					System.out.println("errCode :"+errCode);
					int pos = errCode.indexOf("~");
					System.out.println("pos :"+pos);
					if(pos>-1)
					{
						errCode=errCode.substring(0,pos);
					}
					
					System.out.println("error code is :"+errCode);
					errFldName = (String)errFields.get(cnt);
					System.out.println(" cnt [" + cnt + "] errCode [" + errCode + "] errFldName [" + errFldName + "]");
					errString = getErrorString(errFldName, errCode, userId);
					errorType = errorType(conn, errCode);
					System.out.println("errorType :"+errorType);
					if (errString.length() > 0)
					{
						String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 
								8, errString.indexOf("<trace>"));
						bifurErrString = bifurErrString + 
								errString.substring(errString.indexOf("</trace>") + 
										8, errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						errString = "";
					}
					if (errorType.equalsIgnoreCase("E"))
					{
						break;
					}
				}

				errStringXml.append("</Errors> </Root> \r\n");
			}
			else
			{
				errStringXml = new StringBuffer( "" );
			}
		}//END TRY
		catch(Exception e)
		{
			System.out.println("Exception ::"+e);
			e.printStackTrace();
			errString=e.getMessage();
			throw new ITMException( e );
		}
		finally
		{
			try
			{
				if(conn!=null)
				{
					if(rs != null )rs.close();
					if(rs1 != null )rs.close();
					rs = null;
					rs1 = null;
					if(pstmt != null )pstmt.close();
					pstmt =null;
					conn.close();
				}
				conn = null;
			}catch(Exception d)
				{
				  d.printStackTrace();
				  throw new ITMException( d );
				}
		}
		System.out.println("ErrString ::[ "+errStringXml.toString()+" ]");
		return errStringXml.toString();
	}
	
	public String itemChanged() throws RemoteException, ITMException
	{
		return "";
	}
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String errString = null;
		E12GenericUtility genericUtility = new E12GenericUtility();
		try
		{
			if ( xmlString != null && xmlString.trim().length()!=0 )
			{
				dom = genericUtility.parseString(xmlString); 
			}
			if ( xmlString1 != null && xmlString1.trim().length()!=0 )
			{
				dom1 = genericUtility.parseString(xmlString1); 
			}
			if ( xmlString2 != null && xmlString2.trim().length()!=0 )
			{
				dom2 = genericUtility.parseString(xmlString2); 
			}
			errString = itemChanged( dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
			System.out.println ( "ErrString :" + errString);
		}
		catch ( Exception e )
		{
			System.out.println ( "Exception :itemChanged(String,String):" + e.getMessage() + ":" );
			errString = genericUtility.createErrorString(e);
		}
		System.out.println ( "returning from AsnLotStatIC itemChanged" );
		return (errString);
	}
	/**
	 * The public overloded method is used for itemchange of required fields 
	 * Returns itemchange string in XML format
	 * @param dom contains the current form data 
	 * @param dom1 contains always header form data
	 * @param dom2 contains all forms data 
	 * @param objContext represents form no
	 * @param editFlag the mode of the transaction(A-Add or E-Edit)
	 * @param xtraParams contains additional information such as loginCode etc
	 */
	public String itemChanged( Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String siteCode="";
		String custCode ="";
		String siteCodePbus="";
		String sql = "";
		String columnValue = "";
		String loginSiteCode = "";
		String custName="",siteDescr="",itemDescr="",schemeDescr="";
		String userId="";
        String itemCode="";
		Date currentDate = null;
		SimpleDateFormat sdf = null;
		int initValue=0;
		Connection conn = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		StringBuffer valueXmlString = new StringBuffer();
		int currentFormNo = 0;
		E12GenericUtility genericUtility = new E12GenericUtility();
		try
		{
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			userId = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginCode" );
			siteCode = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginSiteCode" );
			columnValue = genericUtility.getColumnValue( currentColumn, dom );
			conn = getConnection();
			if( objContext != null && objContext.trim().length() > 0 )
			{
				currentFormNo = Integer.parseInt( objContext );
			}
			valueXmlString = new StringBuffer( "<?xml version=\"1.0\"?>\r\n<Root>\r\n<Header>\r\n<editFlag>" );
			valueXmlString.append( editFlag ).append( "</editFlag>\r\n</Header>\r\n" );
			switch( currentFormNo )
			{
			case 1:
				valueXmlString.append( "<Detail1>\r\n" );
				if("itm_default".equalsIgnoreCase(currentColumn))
				{
					//Added by Vikas on 27-dec-2018[start]
					sql="select cust_code,site_code__pbus,cust_name from customer where site_code= ? and channel_partner=?";
					pStmt=conn.prepareStatement(sql);
					pStmt.setString(1, siteCode);
					pStmt.setString(2, "Y");
					rs=pStmt.executeQuery();
					int count=0;
					while(rs.next())
					{
						custCode=rs.getString("cust_code");
						siteCodePbus=rs.getString("site_code__pbus");
						custName=rs.getString("cust_name");
						count++;
					}
					System.out.println("Count :::::"+count);
					if( count == 1 )
					{
						valueXmlString.append( "<cust_code__repl>").append(custCode).append( "</cust_code__repl>\r\n" );
						valueXmlString.append( "<cust_repl_name>").append(custName).append( "</cust_repl_name>\r\n" );
						valueXmlString.append( "<site_code__repl>").append(siteCodePbus).append( "</site_code__repl>\r\n" );
					}
					sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
					String loginCode=getValueFromXTRA_PARAMS(xtraParams,"loginCode");
					String chg_term=getValueFromXTRA_PARAMS( xtraParams, "chgTerm" );
					currentDate = new Date();
					valueXmlString.append( "<chg_date><![CDATA[" ).append(sdf.format(currentDate).toString()).append( "]]></chg_date>\r\n" );
					valueXmlString.append( "<chg_user><![CDATA[" ).append(loginCode).append( "]]></chg_user >\r\n" );
					valueXmlString.append( "<chg_term><![CDATA[" ).append(chg_term).append( "]]></chg_term>\r\n" );
					valueXmlString.append( "<item_descr><![CDATA[" ).append(itemDescr).append( "]]></item_descr>\r\n" );
					//Added by Saiprasad G.on [20-DEC-18] START
					valueXmlString.append( "<eff_from><![CDATA[").append(sdf.format(currentDate).toString()).append( "]]></eff_from>\r\n" );
					valueXmlString.append( "<balance_free_qty>").append("0").append( "</balance_free_qty>\r\n" );
					valueXmlString.append( "<used_free_qty>").append("0").append( "</used_free_qty>\r\n" );
					valueXmlString.append( "<balance_free_value>").append("0").append( "</balance_free_value>\r\n" );
					valueXmlString.append( "<used_free_value>").append("0").append( "</used_free_value>\r\n" );
					//Added by Saiprasad G.on [20-DEC-18] END
					valueXmlString.append( "<site_code>").append(siteCode).append( "</site_code>\r\n" );
					valueXmlString.append( "<site_code__repl>").append(siteCodePbus).append( "</site_code__repl>\r\n" );
					valueXmlString.append( "<auto_repl_credit ><![CDATA[").append("Y").append( "]]></auto_repl_credit>\r\n" );
					//Added by Vikas L on 27-dec-18 [end]
				}
				else if("itm_defaultedit".equalsIgnoreCase(currentColumn))
				{
					itemCode = E12GenericUtility.checkNull(genericUtility.getColumnValue("item_code", dom)); 
					//valueXmlString.append( "<item_descr><![CDATA[" ).append(itemDescr).append( "]]></item_descr>\r\n" );
					if("X".equalsIgnoreCase(itemCode))
					{
						System.out.println("Inside the if loop:for item_defaultedit"+itemCode);
						valueXmlString.append( "<item_descr><![CDATA[" ).append("").append( "]]></item_descr>\r\n" );
						valueXmlString.append( "<balance_free_qty protect= \"1\"><![CDATA[" ).append("0").append( "]]></balance_free_qty>\r\n" );
						valueXmlString.append( "<used_free_qty protect= \"1\"><![CDATA[").append("0").append( "]]></used_free_qty>\r\n" );
					}
					else
					{
						System.out.println("Inside the else loop:for item_defaultedit");
						sql = "select descr from item where item_code =?";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, itemCode);
						rs = pStmt.executeQuery();
						if( rs.next() )
						{
							itemDescr = checkNull(rs.getString("descr"));
							System.out.println("Item schemadescr:"+itemDescr);
						}
						rs.close();
						rs=null;
						pStmt.close();
						pStmt=null;
						valueXmlString.append( "<item_descr><![CDATA[" ).append(itemDescr).append( "]]></item_descr>\r\n" );
						valueXmlString.append( "<balance_free_value protect= \"1\"><![CDATA[" ).append("0").append( "]]></balance_free_value>\r\n" );
						valueXmlString.append( "<used_free_value protect= \"1\"><![CDATA[").append("0").append( "]]></used_free_value>\r\n" );				
						}
					String schemeType=E12GenericUtility.checkNull(genericUtility.getColumnValue("scheme_type", dom));
					System.out.println("scheme type:"+schemeType);
					sql="select descr from gencodes where mod_name='W_SCHEMA_BALANCE' and fld_value =?";
					pStmt=conn.prepareStatement(sql);
					pStmt.setString(1, schemeType);
					rs=pStmt.executeQuery();
					if(rs.next())
					{
						schemeDescr=rs.getString("descr");
						System.out.println("descr form scheme type:"+schemeDescr);
					}
					rs.close();
					rs=null;
					pStmt.close();
					pStmt=null;
					valueXmlString.append("<gencodes_descr>").append(schemeDescr).append("</gencodes_descr>");
				}

				else if( "cust_code".equalsIgnoreCase(currentColumn) )
				{
					sql = "select cust_name from customer where cust_code =?";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, columnValue);
					rs = pStmt.executeQuery();
					if( rs.next() )
					{
						custName = checkNull(rs.getString("cust_name")); 
					}
					valueXmlString.append( "<cust_name><![CDATA[" ).append(custName).append( "]]></cust_name>\r\n" );
				}
				else if( "item_code".equalsIgnoreCase(currentColumn) )
				{
					itemCode = E12GenericUtility.checkNull(genericUtility.getColumnValue("item_code", dom)); 
					System.out.println("ItemCode:"+itemCode);
					valueXmlString.append( "<item_descr><![CDATA[" ).append(itemDescr).append( "]]></item_descr>\r\n" );
					if("X".equalsIgnoreCase(itemCode))
					{
						System.out.println("Inside the if loop:for item_code");
						valueXmlString.append( "<balance_free_qty protect= \"1\"><![CDATA[" ).append("0").append( "]]></balance_free_qty>\r\n" );
						valueXmlString.append( "<used_free_qty protect= \"1\"><![CDATA[").append("0").append( "]]></used_free_qty>\r\n" );
						valueXmlString.append( "<balance_free_value protect= \"0\"><![CDATA[" ).append("0").append( "]]></balance_free_value>\r\n" );
						valueXmlString.append( "<used_free_value protect= \"0\"><![CDATA[" ).append("0").append( "]]></used_free_value>\r\n" );
					}
					else
					{
						System.out.println("Inside the else loop:for item_code");
						sql = "select descr from item where item_code =?";
						pStmt = conn.prepareStatement(sql);
						pStmt.setString(1, columnValue);
						rs = pStmt.executeQuery();
						if( rs.next() )
						{
							itemDescr = checkNull(rs.getString("descr")); 
						}
						valueXmlString.append( "<item_descr><![CDATA[" ).append(itemDescr).append( "]]></item_descr>\r\n" );
						valueXmlString.append( "<balance_free_qty protect= \"0\"><![CDATA[" ).append("0").append( "]]></balance_free_qty>\r\n" );
						valueXmlString.append( "<used_free_qty protect= \"0\"><![CDATA[" ).append("0").append( "]]></used_free_qty>\r\n" );
						valueXmlString.append( "<balance_free_value protect= \"1\"><![CDATA[" ).append("0").append( "]]></balance_free_value>\r\n" );
						valueXmlString.append( "<used_free_value protect= \"1\"><![CDATA[" ).append("0").append( "]]></used_free_value>\r\n" );
						
					}

				}
				else if("scheme_type".equalsIgnoreCase(currentColumn))
				{
					String schemeType=E12GenericUtility.checkNull(genericUtility.getColumnValue("scheme_type", dom));
					System.out.println("scheme type:"+schemeType);
					sql="select descr from gencodes where mod_name='W_SCHEMA_BALANCE' and fld_value =?";
					pStmt=conn.prepareStatement(sql);
					pStmt.setString(1, schemeType);
					rs=pStmt.executeQuery();
					if(rs.next())
					{
						schemeDescr=rs.getString("descr");
						System.out.println("descr form scheme type:"+schemeDescr);
					}
					valueXmlString.append("<gencodes_descr>").append(schemeDescr).append("</gencodes_descr>");
				}
				//Added By Vikas L on 27-Dec-18[start]
				else if(("CUST_CODE__REPL").equalsIgnoreCase(currentColumn))
				{
					custCode="";	
					//siteCode = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginSiteCode" );
					System.out.println("site_code<<<<<<<<<"+siteCode);
					String siteCodeRepl="";
					String cust_code_repl=E12GenericUtility.checkNull(genericUtility.getColumnValue("cust_code__repl", dom));
					sql= "select site_code__pbus,cust_name from customer where cust_code = ?";
					pStmt=conn.prepareStatement(sql);
					pStmt.setString(1,cust_code_repl);
					rs=pStmt.executeQuery();
					if(rs.next())
					{
						siteCodeRepl=E12GenericUtility.checkNull(rs.getString("site_code__pbus"));
						custName=rs.getString("cust_name");
						System.out.println("site_code__pbus ::::"+siteCodeRepl);
					}
					if( siteCodeRepl.equals("")) 
					{
						valueXmlString.append("<site_code__repl>").append(siteCode).append("</site_code__repl>");
					}
					else
					{
						valueXmlString.append("<site_code__repl>").append(siteCodeRepl).append("</site_code__repl>");
					}
					valueXmlString.append("<cust_repl_name>").append(custName).append("</cust_repl_name>");
				}
				//Added By Vikas L on 27-Dec-18[start]

				valueXmlString.append( "</Detail1>\r\n" );
			}
		}//end of second form
		catch( Exception e )
		{
			throw new ITMException(e);
		}
		finally
		{
			try
			{	
				if( rs != null )
				{
					rs.close();
					rs = null;
				}
				if( pStmt != null && !pStmt.isClosed() )
				{
					pStmt.close();
					pStmt = null;
				}
				if( conn != null && !conn.isClosed() )
				{
					conn.close();
					conn = null;
				}
			}
			catch( Exception e )
			{
				System.out.println( "Exception :AsnLotStatIC:itemChanged :==>\n"+e.getMessage());
				throw new ITMException(e);
			}
		}
		valueXmlString.append( "</Root>\r\n" );	
		return valueXmlString.toString();
	}//end of  itemchange
		
	private String checkNull( String input )
	{
		if ( input == null )
		{
			input = "";
		}
		return input;
	}
	private String errorType( Connection conn , String errorCode )
	{
		String msgType = "";
		PreparedStatement pstmt = null ; 
		ResultSet rs = null;
		try
		{			
			String  sql = " SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO =   ? ";
			
			pstmt = conn.prepareStatement( sql );			
			pstmt.setString(1, errorCode);			
			rs = pstmt.executeQuery();
			while( rs.next() )
			{
				msgType = rs.getString("MSG_TYPE");
			}			
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}		
		finally
		{
			try
			{
				if ( rs != null )
				{
					rs.close();
					rs = null;
				}
				if ( pstmt != null )
				{
					pstmt.close();
					pstmt = null;
				}
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}
		}		
		return msgType;
	}
}
