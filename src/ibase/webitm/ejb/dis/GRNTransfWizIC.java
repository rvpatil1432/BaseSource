/********************************************************
	Title 	 : 	GRNTransfWizIC[D14HFRA001]
	Date  	 : 	10/11/14
	Developer:  Chandrashekar

********************************************************/
package ibase.webitm.ejb.dis;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.ejb.Stateless; 
@Stateless 

public class GRNTransfWizIC extends ValidatorEJB implements GRNTransfWizICLocal, GRNTransfWizICRemote
{
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();

	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			System.out.println("wfValData xmlString ::"+xmlString);
			System.out.println("wfValData xmlString1 ::"+xmlString1);
			System.out.println("wfValData xmlString2 ::"+xmlString2);
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
			System.out.println("Exception : [GRNTransfWizIC][wfValData( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e);
		}
		return(errString);
	}

	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String childNodeName = null;
		String sql="";
		String columnValue="";
		String siteCode="";
		String errString = "";
		String errCode = "";
		String userId = "";
		String errorType = "";
		String grnNo="";
		String refSer="";
		String locCodeTo="";
		String invStat="";
		String loginSiteCode="";
		String orderType="";
		int cnt;
		int cnt1 =0;
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
				System.out.println("currentFormNo>>>>>>>>>>>>>>>>>:"+currentFormNo);
			}
			switch(currentFormNo)
			{
			case 1:
			{
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();

				for (ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNode != null && childNode.getFirstChild() != null)
					{
						columnValue = childNode.getFirstChild().getNodeValue();
					}
					System.out.println(" columnName [" + childNodeName + "] columnValue [" + columnValue + "]");

					if ("grn_no".equalsIgnoreCase(childNodeName))
					{
						grnNo = genericUtility.getColumnValue("grn_no", dom);
						orderType = genericUtility.getColumnValue("order_type", dom);
						System.out.println("orderType[" + orderType + "]");
						if("D".equalsIgnoreCase(orderType))
						{
							if (grnNo != null && grnNo.trim().length() > 0)
							{
								sql = " select count(1) cnt from distord_rcp where tran_id = ?  and (case when confirmed is null then 'N' else confirmed end = 'Y') ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, grnNo);
								rs = pstmt.executeQuery();
								cnt = 0;
								if (rs.next())
								{
									cnt = rs.getInt("cnt");
								}
								rs.close();
								rs = null;
								System.out.println("cnt:::[" + cnt + "]");
								pstmt.close();
								pstmt = null;
								if (cnt == 0)
								{
									errCode = "INVDGRNNO";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							} else
							{
								errCode = "VTGRNBLK";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							
						}
						else
						{
							if (grnNo != null && grnNo.trim().length() > 0)
							{
								sql = " select count(1) cnt from porcp where tran_id = ?  and (case when confirmed is null then 'N' else confirmed end = 'Y') ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, grnNo);
								rs = pstmt.executeQuery();
								cnt = 0;
								if (rs.next())
								{
									cnt = rs.getInt("cnt");
								}
								rs.close();
								rs = null;
								System.out.println("cnt:::[" + cnt + "]");
								pstmt.close();
								pstmt = null;
								if (cnt == 0)
								{
									//errCode = "INVGRNNO";
									errCode = "INVPGRNNO";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							} else
							{
								errCode = "VTGRNBLK";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
				}
			}// case1
				break;
			case 2:
			{
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				for (ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();

					if (childNode != null && childNode.getFirstChild() != null)
					{
						columnValue = childNode.getFirstChild().getNodeValue();
					}
					System.out.println(" detail columnName [" + childNodeName + "] detail columnValue [" + columnValue + "]");
					grnNo = genericUtility.getColumnValue("grn_no", dom1);
					orderType = genericUtility.getColumnValue("order_type", dom1);
					System.out.println("grnNodom1:"+grnNo);
					if("D".equalsIgnoreCase(orderType))
					{
						sql = "select site_code from distord_rcp where tran_id = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, grnNo);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							siteCode = rs.getString("site_code");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					else
					{
						sql = "select site_code from porcp where tran_id = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, grnNo);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							siteCode = rs.getString("site_code");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					if (childNodeName.equalsIgnoreCase("site_code"))
					{
						loginSiteCode = genericUtility.getColumnValue("site_code", dom);
						System.out.println("loginSiteCode:"+loginSiteCode);
						System.out.println("siteCode:"+siteCode);
						if (loginSiteCode != null && loginSiteCode.trim().length() > 0)
						{
							sql = " SELECT COUNT(1) cnt FROM site WHERE site_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, loginSiteCode);
							rs = pstmt.executeQuery();
							cnt = 0;
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;

							pstmt.close();
							pstmt = null;
							if (cnt == 0)
							{
								errCode = "VMINVSITE";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							if(!(siteCode.equalsIgnoreCase(loginSiteCode)))
							{
							errCode = "VTINVLOGN";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							}
						} else
						{
							errCode = "VMSITEBLK";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}
			}// case2 end
				break;
			case 3:
			{
				
				NodeList parentNodeList1 = dom2.getElementsByTagName("Detail3");
				Node parentNode1 = parentNodeList1.item(0);
				System.out.println(">>>>>>>>>>parentNode1.getNodeName().length():"+parentNode1.getNodeName().length());
				String selectValue = this.getSelectValue(parentNode1);
				
				childNodeList = parentNode1.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				childNode = childNodeList.item(0);
				childNodeName = childNode.getNodeName();
				System.out.println("childNodeName["+childNodeName+"]");
					
				System.out.println(">>>>>>>>>>>selectValue:["+selectValue+"]");
				if("N".equals(selectValue))
				{
					errCode = "VTINVSCAN";
					errList.add(errCode);
					errFields.add(childNodeName.toLowerCase());
				}
			}// case 3 end
				break;
			case 4:
			{
				parentNodeList = dom.getElementsByTagName("Detail4");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();

				for (ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNode != null && childNode.getFirstChild() != null)
					{
						columnValue = childNode.getFirstChild().getNodeValue();
					}
					System.out.println(" columnName [" + childNodeName + "] columnValue [" + columnValue + "]");
					if (childNodeName.equalsIgnoreCase("loc_code__to"))
					{
						locCodeTo = genericUtility.getColumnValue("loc_code__to", dom);
						System.out.println("locCodeTodom::"+locCodeTo);
						if (locCodeTo != null && locCodeTo.trim().length() > 0)
						{
							sql = " select count(1) cnt from location where loc_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, locCodeTo);
							rs = pstmt.executeQuery();
							cnt = 0;
							if (rs.next())
							{
								cnt = rs.getInt("cnt");
							}
							rs.close();
							rs = null;

							pstmt.close();
							pstmt = null;
							if (cnt == 0)
							{
								errCode = "VMINVLOC";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else
							{
								sql = " select inv_stat from location where loc_code = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, locCodeTo);
								rs = pstmt.executeQuery();
								cnt = 0;
								if (rs.next())
								{
									invStat = rs.getString("inv_stat").trim();
								}
								rs.close();
								rs = null;

								pstmt.close();
								pstmt = null;
								if (!("NOSL".equalsIgnoreCase(invStat)))
								{
									errCode = "VTINVSTAT";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						} else
						{
							errCode = "VTINVSCAN";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}
			}// case 4 end
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
				//break;
			//}
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
	}//end of validation

	// method for item change
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		System.out.println("xmlString............."+xmlString);
		System.out.println("xmlString1............"+xmlString1);
		System.out.println("xmlString2............"+xmlString2);
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
			System.out.println("Exception : [GRNTransfWizIC][itemChanged( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e);
		}
		return valueXmlString;
	}

	// method for item change
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		String loginCode = "",chgTerm = "", siteCode = "", siteDescr = "";
		String currDateStr = "";
		String sql = "",grnNo="";
		StringBuffer valueXmlString = new StringBuffer();
		int ctr = 0;int domID = 0,lineNo=0,noArt=0;
		int currentFormNo = 0;
		NodeList parentNodeList = null;
		Node parentNode = null;
		int parentNodeListLength = 0;
		int newInt = 0;
		String tranId="";
		String sql1="";
		String locCodeTo="",containerNo="",packRef="",orderType="";
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		PreparedStatement pstmt1 = null;
		ResultSet rs1 = null ;
		String itemCode="",itemDescr="",locCode="",locDescr = "";
		String lotNo = "",lotSl = "",acctCode = "",cctrCode = "",unit = "",isPartial= "";
		double quantity = 0d, qtyPerArt = 0d; 
		java.util.Date currDate = null;
		SimpleDateFormat sdf = null;
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
			String applDateFormat = genericUtility.getApplDateFormat();
			
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");
			System.out.println("currentFormNo::::::::::["+currentFormNo+"]");
			switch(currentFormNo)
			{

			case 2 : 
			{
				
				if( currentColumn.trim().equalsIgnoreCase( "itm_default" ) )							
				{	
					currDate = new java.util.Date();
					sdf = new SimpleDateFormat(applDateFormat);
					currDateStr = sdf.format(currDate);

					valueXmlString.append( "<Detail2 domID='1'>\r\n" );				
					loginCode = checkNull(genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginCode" ));
					chgTerm = checkNull(genericUtility.getValueFromXTRA_PARAMS( xtraParams, "chgTerm" ));
					siteCode = checkNull(genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginSiteCode" ));
					grnNo = genericUtility.getColumnValue( "grn_no", dom1 );
					orderType = genericUtility.getColumnValue("order_type", dom1);
					if("D".equalsIgnoreCase(orderType))
					{
						sql = "select site_code from distord_rcp where tran_id = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, grnNo);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							siteCode = rs.getString("site_code");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					else
					{
						sql = "select site_code from porcp where tran_id = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, grnNo);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							siteCode = rs.getString("site_code");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					System.out.println("siteCode = ["+siteCode+"]");
					valueXmlString.append("<site_code protect = \"1\">").append("<![CDATA[" ).append(  checkNull ( siteCode)).append( "]]>").append("</site_code>");

					valueXmlString.append( "<tran_id/>" );
					valueXmlString.append( "<tran_date><![CDATA[" ).append( currDateStr ).append( "]]></tran_date>\r\n" );
					valueXmlString.append( "<ref_ser__for><![CDATA[" ).append( "XFRX" ).append( "]]></ref_ser__for>\r\n" );
					//valueXmlString.append( "<site_code><![CDATA[" ).append(  checkNull ( siteCode)).append( "]]></site_code>\r\n" );
					//valueXmlString.append( "<item_ser><![CDATA[" ).append(  checkNull ( "NT")).append( "]]></item_ser>\r\n" );
					
					sql = "SELECT DESCR FROM SITE WHERE SITE_CODE = ?";
					pstmt = conn.prepareStatement( sql );
					pstmt.setString( 1, siteCode );						
					rs = pstmt.executeQuery();	
					if( rs.next() )
					{
						siteDescr = rs.getString("DESCR");
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;

					valueXmlString.append( "<site_descr><![CDATA[" ).append( checkNull( siteDescr )).append( "]]></site_descr>\r\n" );
					valueXmlString.append( "<confirmed><![CDATA[" ).append("N").append( "]]></confirmed>\r\n" );
					valueXmlString.append( "<chg_user><![CDATA[" ).append( loginCode ).append( "]]></chg_user>\r\n" );
					valueXmlString.append( "<chg_date><![CDATA[" ).append( currDateStr ).append( "]]></chg_date>\r\n" );
					valueXmlString.append( "<chg_term><![CDATA[" ).append( chgTerm ).append( "]]></chg_term>\r\n" );
					valueXmlString.append("</Detail2>\r\n");
				}
				/*if (currentColumn.trim().equalsIgnoreCase("site_code"))
				{
					siteCode = genericUtility.getColumnValue("site_code", dom);
					System.out.println("::siteCode @@::" + siteCode);
					sql = "SELECT DESCR FROM SITE WHERE SITE_CODE = ?";
					pstmt = conn.prepareStatement( sql );
					pstmt.setString( 1, siteCode );						
					rs = pstmt.executeQuery();	
					if( rs.next() )
					{
						siteDescr = rs.getString("DESCR");
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;
					
					
					String currDomStr = genericUtility.serializeDom(dom);
					System.out.println("currDomStr[" + currDomStr + "]");
					StringBuffer valueXmlStr = new StringBuffer();
					valueXmlStr.append("<site_descr>").append("<![CDATA[" + siteDescr + "]]>").append("</site_descr>");
					currDomStr = currDomStr.replace("</Detail2>", valueXmlStr.toString() + "</Detail2>");
					System.out.println("after currDomStr[" + currDomStr + "]");
					valueXmlString.append(currDomStr);
				}*/
			}//case 2 end here
			break;
			case 3 :
		  	{
		  		
		  		System.out.println("currentColumn!!!!!["+currentColumn+"]");
				if( currentColumn.trim().equalsIgnoreCase( "itm_default" ) )							
				{
					grnNo = genericUtility.getColumnValue( "grn_no", dom1 );
					orderType = genericUtility.getColumnValue( "order_type", dom1 );
					System.out.println("orderType::["+orderType+"]");
					if("D".equalsIgnoreCase(orderType))
					{
						sql = "select site_code from distord_rcp where tran_id = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, grnNo);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							siteCode = rs.getString("site_code");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						sql = "select line_no,item_code,loc_code,lot_sl,lot_no,unit,no_art,quantity from distord_rcpdet where tran_id= ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, grnNo);
						rs = pstmt.executeQuery();
						while (rs.next())
						{
							itemCode = rs.getString("item_code").trim();
							locCode = rs.getString("loc_code").trim();
							lotNo = rs.getString("lot_no").trim();
							lotSl = rs.getString("lot_sl").trim();
							quantity = rs.getDouble("quantity");
														
							sql1 = " SELECT stock.loc_code,stock.lot_sl,stock.lot_no, stock.no_art,stock.item_code,   "
									+ " stock.site_code, stock.qty_per_art, stock.quantity  - case "
									+ " when stock.alloc_qty is null then 0 else stock.alloc_qty end as quantity, "
									+ " stock.acct_code__inv,stock.cctr_code__inv FROM stock,invstat  "
									+ " WHERE ( stock.inv_stat = invstat.inv_stat ) and  " 
									+ " ( stock.item_code = ? ) AND  " 
									+ " ( stock.site_code = ?) AND  "
									// +" ( stock.inv_stat = 'GRL') AND  "
									+ " ( stock.loc_code = ?) AND  "
									+ " ( stock.lot_no = ?) AND  " 
									+ " ( stock.quantity  - case when stock.alloc_qty is null then 0 else stock.alloc_qty end >  0 )" 
									+ " order by stock.lot_no,stock.lot_sl ";
							pstmt1 = conn.prepareStatement(sql1);
							pstmt1.setString(1, itemCode);
							pstmt1.setString(2, siteCode);
							pstmt1.setString(3, locCode);
							pstmt1.setString(4, lotNo);
							rs1 = pstmt1.executeQuery();
							while (rs1.next())
							{
								locCode = rs1.getString("loc_code").trim();
								lotSl = rs1.getString("lot_sl").trim();
								siteCode = rs1.getString("site_code").trim();
								quantity = rs1.getDouble("quantity");
								noArt = rs1.getInt("no_art");
								qtyPerArt = rs1.getDouble("qty_per_art");
								acctCode = rs1.getString("acct_code__inv");
								cctrCode = rs1.getString("cctr_code__inv");
								domID++;
								lineNo++;
								itemDescr = findValue(conn, "descr", "item", "item_code", itemCode);
								locDescr = findValue(conn, "descr", "location", "loc_code", locCode);

								valueXmlString.append("<Detail3 domID='" + domID + "' selected=\"N\">\r\n");
								valueXmlString.append("<attribute  selected=\"N\"  status=\"Y\" pkNames=\"\" />\r\n");
								valueXmlString.append("<tran_id/>\r\n");
								valueXmlString.append("<line_no><![CDATA[").append(lineNo).append("]]></line_no>\r\n");
								valueXmlString.append("<item_code><![CDATA[").append(checkNull(itemCode)).append("]]></item_code>\r\n");
								valueXmlString.append("<item_descr><![CDATA[").append(checkNull(itemDescr)).append("]]></item_descr>\r\n");
								valueXmlString.append("<loc_code__fr><![CDATA[").append(checkNull(locCode)).append("]]></loc_code__fr>\r\n");
								valueXmlString.append("<loc_descr__fr><![CDATA[").append(checkNull(locDescr)).append("]]></loc_descr__fr>\r\n");
								valueXmlString.append("<loc_code__to><![CDATA[").append("").append("]]></loc_code__to>\r\n");
								valueXmlString.append("<lot_no__fr><![CDATA[").append(checkNull(lotNo)).append("]]></lot_no__fr>\r\n");
								valueXmlString.append("<lot_no__to><![CDATA[").append(checkNull(lotNo)).append("]]></lot_no__to>\r\n");
								valueXmlString.append("<lot_sl__fr><![CDATA[").append(checkNullAndTrim(lotSl)).append("]]></lot_sl__fr>\r\n");
								valueXmlString.append("<lot_sl__to><![CDATA[").append(checkNullAndTrim(lotSl)).append("]]></lot_sl__to>\r\n");
								valueXmlString.append("<quantity><![CDATA[").append(quantity).append("]]></quantity>\r\n");
								valueXmlString.append("<unit><![CDATA[").append(unit).append("]]></unit>\r\n");
								valueXmlString.append("<no_art><![CDATA[").append(noArt).append("]]></no_art>\r\n");
								valueXmlString.append("<line_no_sl><![CDATA[").append("1").append("]]></line_no_sl>\r\n");
								valueXmlString.append("<no_art><![CDATA[").append(noArt).append("]]></no_art>\r\n");
								valueXmlString.append("<acct_code__cr><![CDATA[").append(checkNull(acctCode)).append("]]></acct_code__cr>\r\n");
								valueXmlString.append("<cctr_code__cr><![CDATA[").append(checkNull(cctrCode)).append("]]></cctr_code__cr>\r\n");
								valueXmlString.append("<acct_code__dr><![CDATA[").append(checkNull(acctCode)).append("]]></acct_code__dr>\r\n");
								valueXmlString.append("<cctr_code__dr><![CDATA[").append(checkNull(cctrCode)).append("]]></cctr_code__dr>\r\n");
								valueXmlString.append("</Detail3>");
							}
						}
					}
					else
					{
						sql = "select site_code from porcp where tran_id = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, grnNo);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							siteCode = rs.getString("site_code");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						sql = "select line_no,item_code,loc_code,lot_sl,lot_no,unit,no_art,quantity from porcpdet where tran_id= ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, grnNo);
						rs = pstmt.executeQuery();
						while (rs.next())
						{
							itemCode = rs.getString("item_code").trim();
							locCode = rs.getString("loc_code").trim();
							lotNo = rs.getString("lot_no").trim();
							lotSl = rs.getString("lot_sl").trim();
							quantity = rs.getDouble("quantity");
							System.out.println("porcpdet quantity:" + quantity);

							sql1 = " SELECT stock.loc_code,stock.lot_sl,stock.lot_no, stock.no_art,stock.item_code,   "
									+ " stock.site_code, stock.qty_per_art, stock.quantity  - case "
									+ " when stock.alloc_qty is null then 0 else stock.alloc_qty end as quantity, "
									+ " stock.acct_code__inv,stock.cctr_code__inv FROM stock,invstat  "
									+ " WHERE ( stock.inv_stat = invstat.inv_stat ) and  " 
									+ " ( stock.item_code = ? ) AND  " 
									+ " ( stock.site_code = ?) AND  "
									// +" ( stock.inv_stat = 'GRL') AND  "
									+ " ( stock.loc_code = ?) AND  "
									+ " ( stock.lot_no = ?) AND  " 
									+ " ( stock.quantity  - case when stock.alloc_qty is null then 0 else stock.alloc_qty end >  0 )" 
									+ " order by stock.lot_no,stock.lot_sl ";
							pstmt1 = conn.prepareStatement(sql1);
							pstmt1.setString(1, itemCode);
							pstmt1.setString(2, siteCode);
							pstmt1.setString(3, locCode);
							pstmt1.setString(4, lotNo);
							rs1 = pstmt1.executeQuery();
							while (rs1.next())
							{
								locCode = rs1.getString("loc_code").trim();
								lotSl = rs1.getString("lot_sl").trim();
								siteCode = rs1.getString("site_code").trim();
								quantity = rs1.getDouble("quantity");
								noArt = rs1.getInt("no_art");
								qtyPerArt = rs1.getDouble("qty_per_art");
								acctCode = rs1.getString("acct_code__inv");
								cctrCode = rs1.getString("cctr_code__inv");
								domID++;
								lineNo++;
								// for (int i = 1; i <= noArt; i++)
								// {
								itemDescr = findValue(conn, "descr", "item", "item_code", itemCode);
								locDescr = findValue(conn, "descr", "location", "loc_code", locCode);

								valueXmlString.append("<Detail3 domID='" + domID + "' selected=\"N\">\r\n");
								valueXmlString.append("<attribute  selected=\"N\"  status=\"Y\" pkNames=\"\" />\r\n");
								valueXmlString.append("<tran_id/>\r\n");
								valueXmlString.append("<line_no><![CDATA[").append(lineNo).append("]]></line_no>\r\n");
								valueXmlString.append("<item_code><![CDATA[").append(checkNull(itemCode)).append("]]></item_code>\r\n");
								valueXmlString.append("<item_descr><![CDATA[").append(checkNull(itemDescr)).append("]]></item_descr>\r\n");
								valueXmlString.append("<loc_code__fr><![CDATA[").append(checkNull(locCode)).append("]]></loc_code__fr>\r\n");
								valueXmlString.append("<loc_descr__fr><![CDATA[").append(checkNull(locDescr)).append("]]></loc_descr__fr>\r\n");
								valueXmlString.append("<loc_code__to><![CDATA[").append("").append("]]></loc_code__to>\r\n");
								valueXmlString.append("<lot_no__fr><![CDATA[").append(checkNull(lotNo)).append("]]></lot_no__fr>\r\n");
								valueXmlString.append("<lot_no__to><![CDATA[").append(checkNull(lotNo)).append("]]></lot_no__to>\r\n");
								valueXmlString.append("<lot_sl__fr><![CDATA[").append(checkNullAndTrim(lotSl)).append("]]></lot_sl__fr>\r\n");
								valueXmlString.append("<lot_sl__to><![CDATA[").append(checkNullAndTrim(lotSl)).append("]]></lot_sl__to>\r\n");
								valueXmlString.append("<quantity><![CDATA[").append(quantity).append("]]></quantity>\r\n");
								valueXmlString.append("<unit><![CDATA[").append(unit).append("]]></unit>\r\n");
								valueXmlString.append("<no_art><![CDATA[").append(noArt).append("]]></no_art>\r\n");
								valueXmlString.append("<line_no_sl><![CDATA[").append("1").append("]]></line_no_sl>\r\n");
								valueXmlString.append("<no_art><![CDATA[").append(noArt).append("]]></no_art>\r\n");
								valueXmlString.append("<acct_code__cr><![CDATA[").append(checkNull(acctCode)).append("]]></acct_code__cr>\r\n");
								valueXmlString.append("<cctr_code__cr><![CDATA[").append(checkNull(cctrCode)).append("]]></cctr_code__cr>\r\n");
								valueXmlString.append("<acct_code__dr><![CDATA[").append(checkNull(acctCode)).append("]]></acct_code__dr>\r\n");
								valueXmlString.append("<cctr_code__dr><![CDATA[").append(checkNull(cctrCode)).append("]]></cctr_code__dr>\r\n");
								valueXmlString.append("</Detail3>");
								// }
							}
							rs1.close();
							rs1 = null;
							pstmt1.close();
							pstmt1 = null;
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					
				}//itm_detault
				break;				  		
		  	}// case 3
			case 4: 
			{	
				if( currentColumn.trim().equalsIgnoreCase( "itm_default" ) )
				{
					System.out.println("=======CALL CASE 4 ITEM CHANGE=========");
					parentNodeList = dom2.getElementsByTagName("Detail3");
					parentNodeListLength = parentNodeList.getLength();

					for(ctr = 0; ctr < parentNodeListLength ; ctr++ )
					{
						System.out.println("TEST LENGTH="+parentNodeListLength);
						parentNode = parentNodeList.item(ctr);
						Element  parentNode1 = (Element)parentNodeList.item(ctr);
						System.out.println("parentNode:::"+parentNode+"]");
						String selected = getAttribValue(parentNode, "attribute", "selected");
						System.out.println("status ="+getAttribValue(parentNode, "attribute", "status"));
						System.out.println("value of selected the detail["+selected+"]");
						//if("Y".equalsIgnoreCase(selected))
						//{
							newInt++;
							tranId = genericUtility.getColumnValueFromNode("tran_id", parentNode) ;
							System.out.println("tranId::["+tranId+"]");
							lineNo = Integer.parseInt(genericUtility.getColumnValueFromNode("line_no", parentNode).trim());
							System.out.println("lineNo::["+lineNo+"]");
							itemCode =  genericUtility.getColumnValueFromNode("item_code", parentNode);
							itemDescr = genericUtility.getColumnValueFromNode("item_descr", parentNode);
							locCode = genericUtility.getColumnValueFromNode("loc_code__fr", parentNode);
							locDescr = genericUtility.getColumnValueFromNode("loc_descr__fr", parentNode);
							lotNo = genericUtility.getColumnValueFromNode("lot_no__fr", parentNode);
							lotNo = genericUtility.getColumnValueFromNode("lot_no__to", parentNode);
							lotSl = genericUtility.getColumnValueFromNode("lot_sl__fr", parentNode);
							lotSl = genericUtility.getColumnValueFromNode("lot_sl__to", parentNode);
							unit = genericUtility.getColumnValueFromNode("unit", parentNode);
							locCodeTo = genericUtility.getColumnValueFromNode("loc_code__to", parentNode);
							quantity = Double.parseDouble(genericUtility.getColumnValueFromNode("quantity", parentNode).trim());
							System.out.println("locCodeTo:::["+locCodeTo+"]");
							noArt = Integer.parseInt(genericUtility.getColumnValueFromNode("no_art", parentNode).trim());
							containerNo = genericUtility.getColumnValueFromNode("line_no_sl", parentNode);
							acctCode = genericUtility.getColumnValueFromNode("acct_code__cr", parentNode);		
							cctrCode = genericUtility.getColumnValueFromNode("cctr_code__cr", parentNode);
							acctCode = genericUtility.getColumnValueFromNode("acct_code__dr", parentNode);
							cctrCode = genericUtility.getColumnValueFromNode("cctr_code__dr", parentNode);
							System.out.println("containerNo:::["+containerNo+"]");
							
							
							valueXmlString.append("<Detail4 domID='" + newInt+ "' selected=\"N\">\r\n");
							valueXmlString.append("<attribute  selected=\"N\" updateFlag=\"A\"  status=\"N\" pkNames=\"\" />\r\n");
							valueXmlString.append("<tran_id/>\r\n");
							valueXmlString.append("<line_no><![CDATA[").append(lineNo).append("]]></line_no>\r\n");
							valueXmlString.append("<item_code><![CDATA[").append(checkNull(itemCode)).append("]]></item_code>\r\n");
							valueXmlString.append("<item_descr><![CDATA[").append(checkNull(itemDescr)).append("]]></item_descr>\r\n");
							valueXmlString.append("<loc_code__fr><![CDATA[").append(checkNull(locCode)).append("]]></loc_code__fr>\r\n");
							valueXmlString.append("<loc_descr__fr><![CDATA[").append(checkNull(locDescr)).append("]]></loc_descr__fr>\r\n");
							valueXmlString.append("<loc_code__to><![CDATA[").append(checkNull(locCodeTo)).append("]]></loc_code__to>\r\n");
							valueXmlString.append("<lot_no__fr><![CDATA[").append(checkNull(lotNo)).append("]]></lot_no__fr>\r\n");
							valueXmlString.append("<lot_no__to><![CDATA[").append(checkNull(lotNo)).append("]]></lot_no__to>\r\n");								
							valueXmlString.append("<lot_sl__fr><![CDATA[").append(checkNullAndTrim(lotSl)).append("]]></lot_sl__fr>\r\n");
							valueXmlString.append("<lot_sl__to><![CDATA[").append(checkNullAndTrim(lotSl)).append("]]></lot_sl__to>\r\n");
							valueXmlString.append("<quantity><![CDATA[").append(quantity).append("]]></quantity>\r\n");
							valueXmlString.append("<unit><![CDATA[").append(unit).append("]]></unit>\r\n");
							valueXmlString.append("<no_art><![CDATA[").append(noArt).append("]]></no_art>\r\n");
							valueXmlString.append("<line_no_sl><![CDATA[").append(containerNo).append("]]></line_no_sl>\r\n");
							valueXmlString.append("<acct_code__cr><![CDATA[").append(checkNull(acctCode)).append("]]></acct_code__cr>\r\n");
							valueXmlString.append("<cctr_code__cr><![CDATA[").append(checkNull(cctrCode)).append("]]></cctr_code__cr>\r\n");
							valueXmlString.append("<acct_code__dr><![CDATA[").append(checkNull(acctCode)).append("]]></acct_code__dr>\r\n");
							valueXmlString.append("<cctr_code__dr><![CDATA[").append(checkNull(cctrCode)).append("]]></cctr_code__dr>\r\n");
							valueXmlString.append("</Detail4>");
							
						//}
					}
					
				}//itm_default END
				break;	
			}//case 4 end

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
			catch(Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
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
	private String findValue(Connection conn, String columnName ,String tableName, String columnName2, String value) throws  ITMException, RemoteException
	{
		PreparedStatement pstmt = null ;
		ResultSet rs = null ; 
		String sql = "";
		String findValue = "";
		try
		{			
			sql = "SELECT " + columnName + " from " + tableName + " where " + columnName2 +"= ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,value);
			rs = pstmt.executeQuery();
			if(rs.next())
			{					
				findValue = rs.getString(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;	        
		}
		catch(Exception e)
		{
			System.out.println("Exception in findValue ");
			e.printStackTrace();
			throw new ITMException(e); 
		}
		System.out.println("returning String from findValue " + findValue);
		return findValue;
	}
	private String checkNullAndTrim( String inputVal )
	{
		if ( inputVal == null )
		{
			inputVal = "";
		}
		else
		{
			inputVal = inputVal.trim();
		}
		return inputVal;
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
	private String getAttribValue(Node detailNode, String nodeName, String attribStr) throws ITMException
	{
		String attribValue = "";
		try
		{
			String domStr = genericUtility.serializeDom(detailNode);
			Document dom = genericUtility.parseString(domStr);
			if( dom != null /*&& dom.getAttributes() != null*/)
			{
				Node attributeNode = dom.getElementsByTagName( nodeName ).item(0);
				attribValue = getAttribValue(attributeNode, attribStr);
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception : getAttribValue :" + e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		return attribValue;
	}
	private String getAttribValue(Node detailNode, String attribStr) throws ITMException
	{
		String attribValue = "";
		try
		{
			if( detailNode != null && detailNode.getAttributes() != null)
			{
				Node attribNode = detailNode.getAttributes().getNamedItem( attribStr );
				if( attribNode != null )
				{
					attribValue = checkNull( attribNode.getNodeValue() );
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception : getAttribValue :" + e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		return attribValue;
	}
	private String getSelectValue(Node node) 
	{
				String selectValue = "";
				int selectLength=0;
				NamedNodeMap attrMap = node.getAttributes();
				selectLength=attrMap.getLength();
				System.out.println(">>>>>>>>>In getWinName attrMap.getLength():"+selectLength);
				if(selectLength == 5)
				{
					selectValue = attrMap.getNamedItem("selected").getNodeValue();
				}
				else
				{
					selectValue="N";
				}
				return selectValue;
	}

}

