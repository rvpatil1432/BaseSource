/********************************************************
	Title : SalesQuotRevision
	Date  : 24/06/18
	Author: AMOL SANT

********************************************************/


package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;

import java.io.ByteArrayOutputStream;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



@Stateless
public class SalesQuotRevision extends ValidatorEJB implements SalesQuotRevisionLocal,SalesQuotRevisionRemote
{
	
	E12GenericUtility genericUtility = new E12GenericUtility();
	
	private String currentCustCode = "",currentFromDate = "",currentToDate = "";
	private String preViousCustCode = "",preViousFromDate = "",preViousToDate = "";
	
	public String wfValData(String xmlString, String xmlString1, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		System.out.println("#### SalesQuotRevision :: wfValData 1...");
		String errString = null;
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			System.out.println("xmlString:-" + xmlString );
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			
			errString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams);
			System.out.println("errString[" + errString+"]" );
		}
		catch(Exception e)
		{
			throw new ITMException( e );
		}
		return (errString);
	}
	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag, String xtraParams, String formName) throws RemoteException,ITMException
	{
		System.out.println("#### SalesQuotRevision :: wfValData 2.. ");
		System.out.println("#### formName : "+formName);
		
		String errString = null;
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			System.out.println("xmlString:-" + xmlString );
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString("<Root>" + xmlString2+ "</Root>");
			}
			System.out.println("objContext["+objContext+"]\nwfValData.xmlString1["+xmlString1+"]");
			System.out.println("wfValData.xmlString2["+xmlString2+"]");
			errString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams, formName);
			System.out.println("errString[" + errString+"]" );
		}
		catch(Exception e)
		{
			throw new ITMException( e );
		}
		return (errString);
	}
	public String wfValData(Document dom, Document dom1,Document dom2, String objContext, String editFlag, String xtraParams, String formName) throws RemoteException,ITMException
	{
		System.out.println("#### SalesQuotRevision :: wfValData 3");
		
		Connection conn = null;
		//ConnDriver connDriver = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		String custCode = "", fromDateStr = "", toDateStr = "", effDateStr = "", validDateStr = "";
		int currentFormNo = 0 ;
		int count = 0;
		String query ="";
		String errorString="",errorCode ="",errorField ="",errorType;
		String userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
		
		List<String> errorCodeList = new ArrayList<String>();
		List<String> errorFieldList = new ArrayList<String>();
		
		NodeList nodeList = null;
		Node node = null;
		Node childNode = null;
		Element element = null;
		String childNodeName = "";
		StringBuffer errStringXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root><Errors>"); 
		StringBuffer formatedInvIds = new StringBuffer();
	
		/*if(objContext.trim().length() > 0)
		{
			currentFormNo = Integer.parseInt(objContext);
		}*/
		
		System.out.println("#### Form No ["+currentFormNo+"]");
		try
		{
			conn = getConnection();
			errorCodeList.clear();
			errorFieldList.clear();
			if("price_rev".equalsIgnoreCase(formName))
			{
				custCode = genericUtility.getColumnValue("cust_code", dom);
				fromDateStr = genericUtility.getColumnValue("from_date", dom1);
				toDateStr = genericUtility.getColumnValue("to_date", dom1);
				effDateStr = genericUtility.getColumnValue("eff_date", dom1);
				validDateStr = genericUtility.getColumnValue("valid_upto", dom1);
				
				query = "SELECT CUST_CODE FROM CUSTOMER WHERE CUST_CODE = ?";
				pstmt = conn.prepareStatement(query);
				pstmt.setString(1, custCode);
				
				rs = pstmt.executeQuery();
				
				while(rs.next())
				{
					count ++;
					custCode = rs.getString("CUST_CODE");
				}
				if(count == 0 )
				{
					errorCode = "VTCUSTNEX";
					errorCodeList.add(errorCode);
					errorFieldList.add("cust_code");
					
					//errorMsg = getErrorType(conn,errorCode);
					//errStringXml.append("<ErrorMessage><![CDATA["+errorMsg+"]]></ErrorMessage>");
				}
				count = 0;
				
				
				try
				{
					String currAppdate ="";
					java.sql.Timestamp currDate = null;
					currDate = new java.sql.Timestamp(System.currentTimeMillis()) ;
					if(currDate != null)
					{
						currAppdate = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(currDate).toString();
					}
					Timestamp currentDate = null, toDate = null, fromDate = null,effDate = null,validDate = null;
					
					if(currAppdate != null)
					{
						currentDate = getDate(currAppdate);
					}
					if(toDateStr != null)
					{
						toDate = getDate(toDateStr);
					}
					if(fromDateStr != null)
					{
						fromDate = getDate(fromDateStr);
					}
					if(effDateStr != null)
					{
						effDate = getDate(effDateStr);
					}
					if(validDateStr != null)
					{
						validDate = getDate(validDateStr);
					}
					
					System.out.println("#### Current Date" +currentDate);
					System.out.println("#### TO Date "+toDate);
					System.out.println("#### EFF Date "+effDate);
					System.out.println("#### VALID DATE Date "+validDate);
					if(fromDate == null)
					{
						errorCode = "INVFRMDATE";  
						errorCodeList.add(errorCode);
						errorFieldList.add("from_date");
					}
					else if(toDate == null)
					{
						errorCode = "VPSHFTCH04";  
						errorCodeList.add(errorCode);
						errorFieldList.add("to_date");
					}
					else if(effDate == null)
					{
						errorCode = "VMEFFDATE";  
						errorCodeList.add(errorCode);
						errorFieldList.add("eff_date");
					}
					else if(validDate == null)
					{
						errorCode = "VTDATEUPTO";  
						errorCodeList.add(errorCode);
						errorFieldList.add("valid_upto");
					}
					else if(fromDate !=null && toDate != null && fromDate.after(toDate))
					{
						errorCode = "VTTODATE";//VMDATETO  
						errorCodeList.add(errorCode);
						errorFieldList.add("to_date");
					}
					else if(currentDate != null && validDate != null && currentDate.after(validDate))
					{
						errorCode = "VTVLUPDATE";// BY ME...
						errorCodeList.add(errorCode);
						errorFieldList.add("valid_upto");
					}
					if(effDate != null && validDate != null && effDate.after(validDate))
					{
						errorCode = "VTDATE02";//VTDATE6   
						errorCodeList.add(errorCode);
						errorFieldList.add("valid_upto");
						
					}
					//For Ffrom DATE NULL INVFRMDATE;
					//For Valid null VPVDATE1 ,VTDATEUPTO
					//for effDate null VTDATEFF,VMEFFDATE 
				} catch (Exception e1)
				{
					e1.printStackTrace();
				}
				
				try
				{
					formatedInvIds = getInvoiceIds(formatedInvIds,custCode,fromDateStr,toDateStr,conn);
					System.out.println("#### Formatted IDs ["+formatedInvIds+"]");
					System.out.println("#### formatedInvIds.length ["+formatedInvIds.length()+"]");
					if(formatedInvIds.length() == 2 || formatedInvIds == null)
					{
						errorCode = "VTNORECFND";
						errorCodeList.add(errorCode);
						errorFieldList.add("to_date");//<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
						System.out.println("#### Executed ErrorCode");
					}
					
				} 
				catch(Exception e)
				{
					e.printStackTrace();
				}
				
				
								
			}
			else if("price_revdet".equalsIgnoreCase(formName))
			{
				nodeList = dom2.getElementsByTagName("Detail2");
				int nodeListLenght = nodeList.getLength();
				for(int ctr = 0; ctr < nodeListLenght; ctr++ )
				{
					node = nodeList.item(ctr);
					childNodeName = node.getNodeName();
					element = (Element) node;
					// <<<<<<<<<<< Need to disscussed......
					/*childNode = element.getElementsByTagName("selectbox").item(0);
					String selectBox = "";
					if(childNode != null)
					{
						selectBox = childNode.getChildNodes().item(0).getNodeValue();
					}*/
					if(element.getElementsByTagName("selectbox").item(0) != null)
					{
						count++;
					}
				}
				if(count == 0)
				{
					errorCode = "VTBLNKDTL";
					errorCodeList.add(errorCode);
					errorFieldList.add("selectbox");
				}
				count = 0;
			}
			else if("new_prc".equalsIgnoreCase(formName))
			{
				nodeList = dom2.getElementsByTagName("Detail3");
				int nodeListLength = nodeList.getLength();
				String newRate = "";
				boolean numeric = true;
				for(int ctr = 0; ctr < nodeListLength; ctr++)
				{
					node = nodeList.item(ctr);
					element = (Element) node;
					childNode = element.getElementsByTagName("rate__new").item(0);
					if(childNode != null)
					{
						try
						{
							newRate = childNode.getChildNodes().item(0).getNodeValue();
							Double rateNew = Double.parseDouble(newRate);
						}
						catch(NullPointerException nl)
						{
							numeric = false;
						}
						catch(NumberFormatException ne)
						{
							numeric = false;
						}
						catch(Exception e)
						{
							numeric = false;
						}
						
					}
					
				}
				if(!numeric)
				{
					System.out.println("Not a Number");
					errorCode = "VTINVRATE";
					errorCodeList.add(errorCode);
					errorFieldList.add(childNodeName.toLowerCase());
				}
			}
			int listSize = errorCodeList.size();
			
			if(errorCodeList != null && listSize > 0)
			{
				for(int ctr = 0; ctr < listSize; ctr++)
				{
					errorCode = (String) errorCodeList.get(ctr);
					errorField = (String) errorFieldList.get(ctr);
					errorString  = getErrorString(errorField, errorCode, userId);
					errorType = getErrorType(conn, errorCode);
					System.out.println("ERRORCODE : "+errorCode+"ERRORFIELD : "+errorField+"ERRORTYPE :"+errorType);
					System.out.println("#### ERROR STRING : "+errorString);
					if (errorString.length() > 0)
					{
						String bifurErrString = errorString.substring(errorString.indexOf("<Errors>") + 
								8, errorString.indexOf("<trace>"));
						bifurErrString = bifurErrString + 
								errorString.substring(errorString.indexOf("</trace>") + 
										8, errorString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						errorString = "";
					}
					if (errorType.equalsIgnoreCase("E"))
					{
						break;
					}
				}
			}
			errStringXml.append("</Errors></Root> \r\n");
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		finally
		{
			try
			{
				if(rs != null)
				{
					rs.close();
				}
				if(pstmt != null)
				{
					pstmt.close();
				}
				
				if(conn != null)
				{
					conn.close();
				}
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}

		return errStringXml.toString();
	}
	//END OF VALIDATION
	
	public String itemChanged(String xmlString, String xmlString1, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		System.out.println("#### SalesQuotRevision :: itemChanged 1");
		Document dom = null;
		Document dom1 = null;
		Document dom2 =null;
		String valXMLStr = "";
		try
		{
			dom = parseString(xmlString);
			dom = parseString(xmlString1);
			valXMLStr = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException( e );
		}
		
		return valXMLStr;
	}
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams, String formName) throws RemoteException,ITMException
	{
		System.out.println("#### SalesQuotRevision :: itemChanged 2");
		
		System.out.println("#### xmlString ["+xmlString+"]");
		System.out.println("#### xmlString1 ["+xmlString1+"]");
		System.out.println("#### xmlString2 ["+xmlString2+"]");
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valXMLStr = "";
		try
		{
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if(xmlString2.trim().length() > 0)
			{
				dom2 = parseString(xmlString2);
			}
			valXMLStr = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams,formName);
		}
		catch(Exception e)
		{
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		
		
		return valXMLStr;
	}
	//public String itemChanged(Document dom, Document dom1, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams, String formName) throws RemoteException,ITMException
	{
		System.out.println("#### SalesQuotRevision :: itemChanged 3");
		System.out.println("#### SalesQuotRevision :: formName ["+formName+"]");
		System.out.println("#### objContext ["+objContext+"]");
		System.out.println("#### currentColumn ["+currentColumn+"]");
		System.out.println("#### editFlag ["+editFlag+"]");
		System.out.println("#### xtraParams ["+xtraParams+"]");
		
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		String custCode = "",custName = "";
		String fromDateStr= "",toDateStr = "",effDateStr = "",validUptoStr = "";;
		Timestamp fromDate= null,toDate = null,effDate = null,validUpto = null;;

		String sql = "";
		String itemCode = "";
		String priceList = "",price_list_dis = "";
		String unit = "",descr = "",dis_count ="",slabNo ="";
		int quantity = 0;
		double rate = 0,discount = 0;
		Double grossRate = 0.0;
		double costRate = 0,commPerUnit = 0,commision = 0,newRate = 0;
		String commTable = "",net_amt = "";
		String siteCode = getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
		String chgUser = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
		String chgTerm = getValueFromXTRA_PARAMS(xtraParams, "chgTerm");
		String currAppdate ="";
		java.sql.Timestamp currDate = null;
		currDate = new java.sql.Timestamp(System.currentTimeMillis()) ;
		
		
		System.out.println("#### siteCode["+siteCode+"] chgUser ["+chgUser+"][ chgTerm "+chgTerm+"]");
		StringBuffer valXMLBuff = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
		valXMLBuff.append(editFlag).append("</editFlag></header>");
		try
		{
			conn = getConnection();
			
			currAppdate = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(currDate).toString();
			Timestamp currentDate = getDate(currAppdate);//
			
			if("price_rev".equalsIgnoreCase(formName))
			{	
				System.out.println("#### In price_rev : currentColumn :"+currentColumn);
				int domID = 1;
				 
				if(currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					System.out.println("#### price_rev :: itm_default :");
					custCode = genericUtility.getColumnValue("cust_code", dom1);
					fromDateStr = genericUtility.getColumnValue("from_date", dom1);
					toDateStr = genericUtility.getColumnValue("to_date", dom1);
					effDateStr = genericUtility.getColumnValue("eff_date", dom1);
					validUptoStr = genericUtility.getColumnValue("valid_upto", dom1);
					
					System.out.println("#### currAppdate ["+currAppdate+"]");
					System.out.println("#### fromDateStr ["+fromDateStr);
					System.out.println("#### toDateStr ["+toDateStr);
					System.out.println("#### effDateStr ["+effDateStr);
					System.out.println("#### validUptoStr ["+validUptoStr);
					
					valXMLBuff.append("<Detail1 domID='"+ domID +"' selected=\"Y\">\r\n");
					valXMLBuff.append("<attribute  selected=\"Y\" updateFlag=\"A\"  status=\"N\" pkNames=\"\" />\r\n");
					valXMLBuff.append("<quot_no/>");
					valXMLBuff.append("<quot_type><![CDATA[R]]></quot_type>");
					
					valXMLBuff.append("<cust_code><![CDATA["+custCode+"]]></cust_code>");
					valXMLBuff.append("<from_date><![CDATA["+fromDateStr+"]]></from_date>");
					valXMLBuff.append("<to_date><![CDATA["+toDateStr+"]]></to_date>");
					valXMLBuff.append("<eff_date><![CDATA["+effDateStr+"]]></eff_date>");
					valXMLBuff.append("<valid_upto><![CDATA["+validUptoStr+"]]></valid_upto>");
					
					valXMLBuff.append("<quot_date><![CDATA[" +currAppdate+ "]]></quot_date>");
					valXMLBuff.append("<chg_date><![CDATA[" +currAppdate+ "]]></chg_date>");
					valXMLBuff.append("<chg_user><![CDATA[" + chgUser+ "]]></chg_user>");
					valXMLBuff.append("<chg_term><![CDATA[" + chgTerm + "]]></chg_term>");
					valXMLBuff.append("</Detail1>\r\n");
				}
				else if(currentColumn.trim().equalsIgnoreCase("cust_code"))
				{
					custCode = checkNull(genericUtility.getColumnValue("cust_code",dom));
					System.out.println("Itemchanged in cust_code "+custCode);
					String currDomStr = genericUtility.serializeDom(dom);
					StringBuffer valueXmlStr = new StringBuffer();
					custName = getDBColumnValue("CUSTOMER", "CUST_NAME", "CUST_CODE= '"+custCode+"'", conn);
					valueXmlStr.append( "<cust_name><![CDATA[" ).append(custName).append( "]]></cust_name>\r\n" );
					System.out.println("TEST currDomStr[" +  valueXmlStr.toString() + "]");
					currDomStr = currDomStr.replace("</Detail1>", valueXmlStr.toString() + "</Detail1>");
					System.out.println("after currDomStr[" + currDomStr + "]");
					valXMLBuff.append(currDomStr);
					
				}
				
			}
			
			else if("price_revdet".equalsIgnoreCase(formName))
			{
				System.out.println("#### In price_revdet ...");
				int cnt = 0;
							
				if(currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
						
					custCode = checkNull(genericUtility.getColumnValue("cust_code",dom1));
					fromDateStr = checkNull(genericUtility.getColumnValue("from_date",dom1));
					toDateStr = checkNull(genericUtility.getColumnValue("to_date",dom1));
										
					currentCustCode = custCode;//
					currentFromDate = fromDateStr;//
					currentToDate = toDateStr;//
					
					
					boolean preDomExist = preDomExists(dom2, objContext);
					
					
					if(preDomExist && currentCustCode.equals(preViousCustCode) && currentFromDate.equals(preViousFromDate) && currentToDate.equals(preViousToDate))
					{
						System.out.println("<<<<<<<<<<< Re Draw Existing Data >>>>>>>>>");
						//selectedItem = getPrevFormValues( dom2, formNo, new ArrayList<String>(Arrays.asList(temp)), "strg_code" ) ;
					}
					else
					{	
						System.out.println(" !!!!!!!!!!!  New Data !!!!!!!!!!!!");
						
						preViousCustCode = custCode;
						preViousFromDate = fromDateStr;
						preViousToDate = toDateStr;
						
						System.out.println("#### preViousCustCode "+preViousCustCode );
						
						if(custCode != null && custCode.trim().length() > 0)
						{
							//sql = "SELECT PRICE_LIST,PRICE_LIST__DISC,COMM_TABLE FROM CUSTOMER WHERE CUST_CODE = ?";
							sql = "SELECT PRICE_LIST,PRICE_LIST__DISC,COMM_TABLE__1,COMM_TABLE__3 FROM CUSTOMER WHERE CUST_CODE = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, custCode);
							rs = pstmt.executeQuery();
							while(rs.next())
							{
								priceList = rs.getString("PRICE_LIST");
								price_list_dis = rs.getString("PRICE_LIST__DISC");
								//commTable = rs.getString("COMM_TABLE");
								commTable = rs.getString("COMM_TABLE__1");
								if(E12GenericUtility.checkNull(commTable).length() <= 0 )
								{
									commTable = rs.getString("COMM_TABLE__3");
								}
							}
							pstmt.close();
							rs = null;
							pstmt = null;
							
							
						}
						//03-07-18 END
						
						StringBuffer frmtdInvId = new StringBuffer();
						frmtdInvId = getInvoiceIds(frmtdInvId,custCode,fromDateStr,toDateStr,conn);	
						if(frmtdInvId != null && frmtdInvId.length() > 2)
						{
							sql ="SELECT ITEM_CODE,QUANTITY FROM INVOICE_TRACE WHERE INVOICE_ID IN"+frmtdInvId;
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();
							
							Map<String,Integer> itmQntHM = new HashMap<String,Integer>();
							StringBuffer frmtdItemCode = new StringBuffer();
							frmtdItemCode.append("(");
							while(rs.next())
							{
								itemCode = checkNull(rs.getString("ITEM_CODE"));
								quantity = rs.getInt("QUANTITY");
								
								if(itmQntHM.containsKey(itemCode))
								{
									itmQntHM.put(itemCode,itmQntHM.get(itemCode)+quantity);
								}
								else
								{
									itmQntHM.put(itemCode,quantity);
								}
								
								if(cnt > 0)
								{
									frmtdItemCode.append(",");
								}
								frmtdItemCode.append("'"+itemCode+"'");
								
								cnt++;
								
							}
							frmtdItemCode.append(")");
							cnt = 0;
							rs.close();
							pstmt.close();
							rs = null;
							pstmt = null;
							
							
							int domID = 0;
							System.out.println("itmQntHM ::: "+itmQntHM);
							
							DistCommon distCommon = new DistCommon();
							
							 Map<String, String> itemDetailMap = new HashMap<String, String>();
							
							for(String itemCodeKey : itmQntHM.keySet())
							{
								domID++;
								
								
								if(commTable != null)
								{	
									sql= "SELECT COMM_PERC FROM COMM_DET WHERE COMM_TABLE = ? AND VALID_UPTO >= ? AND ITEM_CODE = ?";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1,commTable);
									pstmt.setTimestamp(2,currentDate);
									pstmt.setString(3, itemCodeKey);
									rs = pstmt.executeQuery();
									while(rs.next())
									{
										commision = rs.getDouble("COMM_PERC");
									}
									pstmt.close();
									rs.close();
									rs = null;
									pstmt = null;
								}
								
								//for unit START
								sql="SELECT UNIT,DESCR FROM ITEM WHERE ITEM_CODE = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,itemCodeKey);//>>>>>>>>>>>>>> Item Code
								rs = pstmt.executeQuery();
								while(rs.next())
								{
									unit = rs.getString("UNIT");
									descr = rs.getString("DESCR");
								}
								pstmt.close();
								rs.close();
								rs = null;
								pstmt = null;
								
								System.out.println("#### Unit ["+unit+"#### Description ["+descr+"]");
								System.out.println("#### PriceList ["+priceList);				
								rate = (distCommon.pickRate(priceList, currAppdate, itemCodeKey, conn));
								System.out.println("#### PriceList_Dis ["+price_list_dis);
								discount = (distCommon.pickRate(price_list_dis, currAppdate, itemCodeKey, conn));
								itemDetailMap  = getItemData(itemCodeKey, conn);
								//costRate = getCostRate(conn, itemCode, itemDetailMap);
								costRate = getCostRate(conn, itemCode);
								
								
								grossRate = ((rate - ((rate * discount) / 100)));
					     		if(grossRate.isNaN() || grossRate.isInfinite())
					     		{
					     			grossRate = 0.0;
					     		}
					     		
					     		commPerUnit = (((grossRate * commision) / 100));
								double rateNew = (grossRate - commPerUnit);
								
								valXMLBuff.append("<Detail2 domID='"+ domID +"' selected=\"N\">\r\n");
								valXMLBuff.append("<attribute  selected=\"N\" updateFlag=\"A\"  status=\"N\" pkNames=\"\" />\r\n");
								valXMLBuff.append("<quot_no/>");
								valXMLBuff.append("<line_no><![CDATA[" + domID + "]]></line_no>");
								valXMLBuff.append("<item_code><![CDATA["+itemCodeKey+"]]></item_code>");
								valXMLBuff.append("<descr><![CDATA["+descr+"]]></descr>");
								valXMLBuff.append("<unit><![CDATA["+unit+"]]></unit>");
								valXMLBuff.append("<rate><![CDATA["+rate+"]]></rate>"); 
								valXMLBuff.append("<qty_per_month><![CDATA["+itmQntHM.get(itemCodeKey)+"]]></qty_per_month>"); 
								valXMLBuff.append("<discount><![CDATA["+discount+"]]></discount>"); 
								//valXMLBuff.append("<gross_rate><![CDATA["+Math.round(grossRate)+"]]></gross_rate>"); 
								valXMLBuff.append("<gross_rate><![CDATA[]]></gross_rate>"); 
								valXMLBuff.append("<comm_perc><![CDATA["+commision+"]]></comm_perc>"); 
								valXMLBuff.append("<comm_per_unit><![CDATA["+Math.round(commPerUnit)+"]]></comm_per_unit>"); 
								valXMLBuff.append("<rate__new><![CDATA["+rateNew+"]]></rate__new>");
								//valXMLBuff.append("<rate__new><![CDATA[]]></rate__new>");
								valXMLBuff.append("<cost_rate><![CDATA["+costRate+"]]></cost_rate>");
								valXMLBuff.append("</Detail2>\r\n");
							}
							System.out.println("domID :: "+domID);
						}//
					}
				}
				System.out.println("#### currentColumn "+currentColumn);
			}
			else if("new_prc".equalsIgnoreCase(formName) || "3".equalsIgnoreCase(objContext))
			{
					
				System.out.println("#### currentColumn "+currentColumn);
				if(currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					System.out.println("<<<< INSIDE >>>>");
					NodeList nodeList = null;
					Node node = null;
					Element elem = null;
					Node childNode = null;
					
					nodeList = dom2.getElementsByTagName("Detail2");
					node = nodeList.item(0);
					int nodeListLenght = nodeList.getLength();
					System.out.println("<<<< Lenght ["+nodeListLenght+"]");
					int ctr = 0;
					String str = "";
					for(int i = 0 ; i < nodeListLenght; i++)
					{
						node = nodeList.item(i);
						elem = (Element) node;
						childNode = elem.getElementsByTagName("selectbox").item(0);//<<<<<<<<<<<<<<<<<<<<<<<
						System.out.println("#### childNode of selectbox "+childNode);
						
						if( childNode != null)
						{
							
							if( childNode.getChildNodes().item(0).getNodeValue().equals("true"))
							{
								ctr++;
								str = serializeDom(node);
								
								System.out.println("#### SerialiseDOM ["+str+"]");
								
								String str11 = str.substring(str.indexOf("/>")+2);
								StringBuffer buffer = new StringBuffer(str11.substring(0, str11.indexOf("</Detail2>")));
								
								buffer.append( "<line_no protect='1'><![CDATA[" ).append(ctr).append( "]]></line_no>\r\n" );
								buffer.append("<sr_no><![CDATA[" ).append(ctr).append( "]]></sr_no>\r\n</Detail3>" );
								str11 = buffer.toString();
								System.out.println("str11 :"+str11);
								valXMLBuff.append("<Detail3 domID=\""+ctr+"\" selected=\"Y\" > \r\n");
								valXMLBuff.append("<attribute  selected=\"Y\" updateFlag=\"A\"  status=\"N\" pkNames=\"\" />\r\n");
								valXMLBuff.append(str11);
							}
						}
					}
				}
				else if(currentColumn.trim().equalsIgnoreCase("rate__new"))
				{
					System.out.println("<<<<<<<<< INSIDE NEW RATE >>>>>>>>>>");
					
					try
					{
						String serl = serializeDom(dom);
						System.out.println("##### Serialize DOM String["+serl);
						String newRateR = genericUtility.getColumnValue("rate__new", dom);
						System.out.println("@@@@@ NewRate ["+newRateR);
						newRate = Double.parseDouble(newRateR);
						System.out.println("#### Rate After parsing ["+newRate+"]");
						String discountR = genericUtility.getColumnValue("discount", dom);
						System.out.println("@@@@@ Discount ["+discountR+"]");
						discount = Double.parseDouble(discountR);
						System.out.println("#### Discount After parsing ["+discount+"]");
						//double grossRateN = (newRate - discount);
						//double grossRateN = ((newRate - ((newRate * discount) / 100)));
						
						
						System.out.println("#### ITEM CHN rate__new "+newRate);
						//System.out.println("#### ITEM CHN grossRate OLD"+grossRate);
						//System.out.println("#### ITEM CHN grossRate New "+grossRateN);
						String detailDomStr = "";
						detailDomStr = serializeDom(dom);
						System.out.println("#### Old detailDomStr  : "+detailDomStr);
						StringBuffer xmlBuff = new StringBuffer();
						//xmlBuff.append("<gross_rate><![CDATA["+Math.round(grossRateN)+"]]></gross_rate>\r\n"); 
						xmlBuff.append("<rate__new><![CDATA["+newRate+"]]></rate__new>\r\n");
						detailDomStr = detailDomStr.replace("</Detail3>",xmlBuff.toString()+"</Detail3>");
						valXMLBuff.append(detailDomStr);
						
						System.out.println("#### xmlBuff ["+xmlBuff.toString()+"]");
						System.out.println("#### detailDomStr "+detailDomStr);
						System.out.println("#### VAL XML BUFF "+valXMLBuff.toString());
					} catch (Exception e)
					{
						System.out.println("#### Exception in Item change New Rate "+e);
						e.printStackTrace();
					}
					
				}
			}
			valXMLBuff.append("</Root>");
		}
		catch(Exception e)
		{
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		finally
		{
			try
			{
				if(rs != null)
				{
					rs.close();
				}
				if(pstmt != null)
				{
					pstmt.close();
				}
				if(conn != null)
				{
					conn.close();
				}
			}
			catch(Exception ex)
			{
				System.out.println("Finally Exception : "+ex.getMessage());
				ex.printStackTrace();
			}
		}
		System.out.println("#### valXMlBuff "+valXMLBuff.toString());
		return ( valXMLBuff == null || valXMLBuff.toString().trim().length() == 0 ? "" : valXMLBuff.toString() );
	}
	
	private String checkNull(String columnValue) 
	{
		if(columnValue == null)
		{
			columnValue = "";
		}
		return columnValue;
	}
	
	@Override
	public String priceRevision(String custCode,String xmlData,String userInfoStr) throws RemoteException,ITMException
	{
		System.out.println("#### Calling PriceRevionWF :: ");
		System.out.println("#### custCode :: "+custCode);
		System.out.println("#### xmlData :: "+xmlData);
		String valPrevStr = "1";
		Connection conn = null;
		ConnDriver connDriver = new ConnDriver();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sqlStr = "";
		String validUpto ="";
		String effDate = "";
		String currAppdate ="";
		String priceList ="";
		String listType = "", slabNo = "", lotNoFrom = "", lotNoTo = "", minQty = "", maxQty = "",
				rate = "", rateType = "", minRate = "", maxRate = "", orderType = "",chgRefNo = "",
				priceListParent = "", calcBasis = "", refNo = "", refNoOld = "",valDate ="";
		Timestamp validDate = null;
		Document dom  = parseString(xmlData);
		String _custCode = genericUtility.getColumnValue("cust_code", dom);
		String chgTerm = checkNull(genericUtility.getColumnValue("chg_term", dom, "1")).trim();
		String chgUser = checkNull(genericUtility.getColumnValue("chg_user", dom, "1")).trim();
		System.out.println("#### custCode ["+_custCode);
		
		try
		{
			UserInfoBean userInfo = new UserInfoBean(userInfoStr);
		   	String transDB = userInfo.getTransDB();
		   //	String termimnal = userInfo.getHostIP();
		   //	String user = userInfo.getUserName();
		   	
		   	System.out.println("#### TransDB connection in : "+transDB);
		   	
		   	if (transDB != null && transDB.trim().length() > 0)
		   	{
		   		conn = connDriver.getConnectDB(transDB);
		   	}
		   	else
		   	{
		   		conn = connDriver.getConnectDB("DriverITM");
		   	}
		   	conn.setAutoCommit(false);
		   	connDriver = null;
		   	
		   	System.out.println("#### Connection establish ["+conn+"]");
		   	
		   	/*
			java.sql.Timestamp currDate = null;
			currDate = new java.sql.Timestamp(System.currentTimeMillis()) ;
			currAppdate = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(currDate).toString();//String Format 
			Timestamp currentDate = getDate(currAppdate);//Timestamp format.
			*/
			
			sqlStr = "SELECT PRICE_LIST FROM CUSTOMER WHERE CUST_CODE = ?";
			pstmt = conn.prepareStatement(sqlStr);
			pstmt.setString(1, custCode);
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				priceList = rs.getString("PRICE_LIST");
				//priceListG = priceList;
			}
			pstmt.close();
			rs = null;
			pstmt = null;
			
			NodeList nList1 = dom.getElementsByTagName("Detail1");
			for(int ctr = 0; ctr< nList1.getLength();ctr++)
			{
				Node node = nList1.item(0);
				System.out.println("nList1.getLength() "+nList1.getLength());
				Element elm = (Element) node;
				validUpto  = elm.getElementsByTagName("valid_upto").item(0).getTextContent();
				effDate  = elm.getElementsByTagName("eff_date").item(0).getTextContent();
				System.out.println("valid_upto "+validUpto);
				System.out.println("effDate "+effDate);
				
			}
			
			NodeList nList = dom.getElementsByTagName("Detail3");
			for(int ctr= 0; ctr < nList.getLength();ctr++)
			{
				Node node = nList.item(ctr);
						
				System.out.println("nList.getLength() "+nList.getLength());
				Element elm = (Element) node;
				
				String rateNew  = elm.getElementsByTagName("rate__new").item(0).getTextContent();
				String itemCode  = elm.getElementsByTagName("item_code").item(0).getTextContent();
				String unit  = elm.getElementsByTagName("unit").item(0).getTextContent();
				System.out.println("#### rate__new "+rateNew);
				System.out.println("#### item_code "+itemCode);
				System.out.println("#### unit "+unit);
				
				System.out.println("######## itemCode "+itemCode+" priceList "+priceList+" unit "+unit);
				String priceSql = "SELECT MAX(SLAB_NO) as SLAB_NO FROM PRICELIST WHERE PRICE_LIST = ? AND ITEM_CODE = ? AND UNIT = ?";
				
				pstmt = conn.prepareStatement(priceSql);
				pstmt.setString(1, priceList);
				pstmt.setString(2, itemCode);
				pstmt.setString(3, unit);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					slabNo = rs.getString("SLAB_NO");
				}
				pstmt.close();
				pstmt = null;
				rs = null;
				
				sqlStr = "SELECT * FROM PRICELIST WHERE ITEM_CODE = ? AND PRICE_LIST = ? AND UNIT = ? and SLAB_NO = ?";
				
				pstmt = conn.prepareStatement(sqlStr);
				pstmt.setString(1,itemCode);
				pstmt.setString(2,priceList);
				pstmt.setString(3,unit);
				pstmt.setString(4,slabNo);
				
				System.out.println("Executing query "+sqlStr);
				
				rs = pstmt.executeQuery();

				while(rs.next())
				{
					listType  = E12GenericUtility.checkNull(rs.getString("list_type"));
					slabNo  = E12GenericUtility.checkNull(rs.getString("slab_no"));
					validDate = rs.getTimestamp("VALID_UPTO");//
					lotNoFrom  = E12GenericUtility.checkNull(rs.getString("lot_no__from"));
					lotNoTo  = E12GenericUtility.checkNull(rs.getString("lot_no__to"));
					minQty  = E12GenericUtility.checkNull(rs.getString("min_qty"));
					maxQty  = E12GenericUtility.checkNull(rs.getString("max_qty"));
					rate  = E12GenericUtility.checkNull(rs.getString("rate"));
					rateType  = E12GenericUtility.checkNull(rs.getString("rate_type"));
					minRate  = E12GenericUtility.checkNull(rs.getString("min_rate"));
					maxRate  = E12GenericUtility.checkNull(rs.getString("max_rate"));
					orderType  = E12GenericUtility.checkNull(rs.getString("order_type"));
					chgRefNo  = E12GenericUtility.checkNull(rs.getString("chg_ref_no"));
					priceListParent  = E12GenericUtility.checkNull(rs.getString("price_list__parent"));
					calcBasis  = E12GenericUtility.checkNull(rs.getString("calc_basis"));
					refNo  = E12GenericUtility.checkNull(rs.getString("ref_no"));
					refNoOld  = E12GenericUtility.checkNull(rs.getString("ref_no_old"));
				}
				
				System.out.println("#### slabNo :"+slabNo+" valDate :"+valDate);
				pstmt.close();
				pstmt = null;
				rs = null;
				
				/*SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
				System.out.println("#### 111111111111111111");
				//Date date = sdf.parse(valDate);
				Date date = sdf.parse(effDate);
				Calendar cal = Calendar.getInstance();
				cal.add(cal.DAY_OF_YEAR,-1);
				Date pdate = cal.getTime();
				System.out.println("#### 22222222222222222");
				String prepDate = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(pdate).toString();
				Timestamp previousDate = getDate(prepDate);//Timestamp.valueOf(genericUtility.getValidDateString(prepDate,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())+"00:00:00.0");
				*/
				
				
				Calendar cal = Calendar.getInstance();
				Calendar currDate = Calendar.getInstance();
				currDate.setTimeInMillis(new Date().getTime());
				
				currDate.set(Calendar.HOUR_OF_DAY, 0);
				currDate.set(Calendar.HOUR, 0);
				System.out.println("currDate : "+currDate.getTime());
				cal.setTimeInMillis(validDate.getTime());
			    cal.add(Calendar.DAY_OF_MONTH, -1);
			    validDate = new Timestamp(cal.getTime().getTime());
			    System.out.println("validDate After: "+validDate);
				
				
				sqlStr = "UPDATE PRICELIST SET VALID_UPTO = ? WHERE ITEM_CODE = ? AND PRICE_LIST = ? AND UNIT = ? and SLAB_NO = ?";
				
				pstmt = conn.prepareStatement(sqlStr);
				
				pstmt.setTimestamp(1,validDate);
				pstmt.setString(2,itemCode);
				pstmt.setString(3,priceList);
				pstmt.setString(4,unit);
				pstmt.setString(5,slabNo);
				
				System.out.println("########## Updating PriceList");
				pstmt.executeUpdate();
				
				pstmt.close();
				pstmt = null;
				conn.rollback();
												
				sqlStr = "INSERT INTO PRICELIST " +
						"(PRICE_LIST,ITEM_CODE,UNIT,LIST_TYPE,SLAB_NO,EFF_FROM,VALID_UPTO,LOT_NO__FROM,LOT_NO__TO,MIN_QTY,MAX_QTY,RATE,RATE_TYPE," +
						"MIN_RATE,CHG_DATE,CHG_USER,CHG_TERM,MAX_RATE,ORDER_TYPE,CHG_REF_NO,PRICE_LIST__PARENT,CALC_BASIS,REF_NO,REF_NO_OLD)" +
						" VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				pstmt = conn.prepareStatement(sqlStr);
				
				pstmt.setString(1,priceList);
				pstmt.setString(2,itemCode);
				pstmt.setString(3,unit);
				pstmt.setString(4,listType);
				pstmt.setString(5,Integer.parseInt(slabNo)+1+"");//
				pstmt.setString(6,effDate);
				pstmt.setString(7,validUpto);
				pstmt.setString(8,lotNoFrom);
				pstmt.setString(9,lotNoTo);
				pstmt.setString(10,minQty);
				pstmt.setString(11,maxQty);
				pstmt.setString(12,rateNew);//
				pstmt.setString(13,rateType);
				pstmt.setString(14,rateNew);//minRate
				pstmt.setString(15,validUpto);
				pstmt.setString(16,chgTerm);
				pstmt.setString(17,chgUser);// Need to get from xtraParam
				pstmt.setString(18,rateNew);//maxRate
				pstmt.setString(19,orderType);
				pstmt.setString(20,chgRefNo);
				pstmt.setString(21,priceListParent);
				pstmt.setString(22,calcBasis);
				pstmt.setString(23,refNo);
				pstmt.setString(24,refNoOld);
				
				System.out.println("######## INsert into PriceList ..........");
				pstmt.executeUpdate();
				pstmt.close();
				pstmt = null;
				conn.rollback();
				
			}
		}
		catch(Exception e)
		{
			System.out.println("#### Exception in priceList " +e);
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		finally
		{
			try
			{
				if(rs != null)
				{
					rs.close();
				}
				if(pstmt != null)
				{
					pstmt.close();
				}
				if(conn != null)
				{
					conn.close();
				}
			}
			catch(Exception ex)
			{
				System.out.println("Finally Exception : "+ex.getMessage());
				ex.printStackTrace();
			}
		}
		System.out.println("#### return ["+valPrevStr+"]");
		return valPrevStr;
		
		
	}
	
	protected StringBuffer getInvoiceIds(StringBuffer frmtdInvId2, String custCode, String fromDateStr, String toDateStr, Connection conn2) throws Exception
	{

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		Timestamp toDate = null, fromDate = null;
		String sql = "";
		String invoiceId = "";
		int cnt = 0;
		if(
				(custCode !=null && custCode.trim().length() > 0) && 
				(fromDateStr !=null && fromDateStr.trim().length() > 0) && 
				(toDateStr != null && toDateStr.trim().length() > 0)
			)
		{
			try
			{
				toDate = getDate(toDateStr);//Timestamp.valueOf(genericUtility.getValidDateString(toDateStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) 00:00:00.0");
				fromDate = getDate(fromDateStr);//Timestamp.valueOf(genericUtility.getValidDateString(fromDateStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) 00:00:00.0");
				
				sql = "SELECT INVOICE_ID FROM INVOICE WHERE CUST_CODE = ? AND TRAN_DATE BETWEEN ? AND ? AND INV_TYPE NOT IN ('DF','EF')";
				
				pstmt = conn2.prepareStatement(sql);
				pstmt.setString(1,custCode);
				pstmt.setTimestamp(2,fromDate);
				pstmt.setTimestamp(3,toDate);
				rs = pstmt.executeQuery();
				
				frmtdInvId2.append("(");
				
				while(rs.next())
				{	
					invoiceId = checkNull(rs.getString("INVOICE_ID"));
					if(cnt > 0)
					{
						frmtdInvId2.append(",");
					}
					frmtdInvId2.append("'"+invoiceId+"'");
					
					cnt++;
				}
				frmtdInvId2.append(")");
				cnt = 0;
				
				System.out.println("#### formatted INvoice ID ["+frmtdInvId2+"]");
				rs.close();
				pstmt.close();
				rs = null;
				pstmt = null;
				System.out.println("#### Invoice ID Set ["+frmtdInvId2.toString()+"]");
				
			} catch (Exception e)
			{
				System.out.println("#### Exception in SalesQuotRevision :: getInvoiceIds :"+e);
				e.printStackTrace();
				throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
			}
			
			
		}
		return frmtdInvId2;
	}
	
	private String serializeDom(Node dom)throws ITMException
    {
        String retString = null;
        try
        {
        	ByteArrayOutputStream out = new ByteArrayOutputStream();
            Transformer serializer = TransformerFactory.newInstance().newTransformer();
            serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            serializer.transform(new DOMSource(dom), new javax.xml.transform.stream.StreamResult(out));
            retString = out.toString();
            out.flush();
            out.close();
            out = null;
        }
        catch (Exception e)
        {
            System.out.println("Exception : MasterStatefulEJB : serializeDom :"+e);
            throw new ITMException(e);
        }
        return retString;
    }
	
	private String getErrorType( Connection conn , String errorCode ) throws ITMException
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String errorType = "";
		try 
		{
			String sql = "SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, errorCode);
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				errorType = rs.getString("MSG_TYPE");
			}
			
		} 
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		finally
		{
			try
			{
				if(rs != null)
				{
					rs.close();
				}
				if(pstmt != null)
				{
					pstmt.close();
				}
				if(conn != null)
				{
					conn.close();
				}
			}
			catch(Exception ex)
			{
				System.out.println("Finally Exception : "+ex.getMessage());
				ex.printStackTrace();
			}
		}
		return errorType;
	}
	
	private Timestamp getDate(String date)
	{
		Timestamp timeDate = null;
		try 
		{
			timeDate = Timestamp.valueOf(genericUtility.getValidDateString(date,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())
					+ " 00:00:00.0");
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
		System.out.println("#### getDate ["+timeDate);
		return timeDate;
	}
	
	public String getDBColumnValue(String tableName, String columnName, String condition,Connection mConnection) throws ITMException
	{
		System.out.println("#### getDBColumnValue ");
		String columnValue = "";		 
		Statement mStmt = null;
		ResultSet rs = null;
		String selectColumns="";
		try
		{
			String columnNameAs = "";
			selectColumns=columnName;
			if( columnName.indexOf("~AS~") != -1 )
			{
				String[] columnNameArr = columnName.split("~AS~");
				if( columnNameArr.length > 1 )
				{
					columnName = columnNameArr[0];
					columnNameAs = columnNameArr[1];
					selectColumns=columnName+" AS "+columnNameAs;
				}
			}
			
			String mQuery =  " SELECT " +  selectColumns + " FROM " + tableName + " WHERE " + condition ;
			System.out.println("mQuery ["+mQuery+"]"); 
			mStmt = mConnection.createStatement();
			rs = mStmt.executeQuery(mQuery);
			System.out.println("columnNameAs::"+columnNameAs);

			if( columnNameAs.length() > 0 )
			{
				columnName = columnNameAs;
			}
			if (rs.next())
			{
				columnValue = checkNull( rs.getString( columnName.trim() ) ); 
			}
		}
		catch (Exception e)
		{
			columnValue = "";
			System.out.println("Exception in SalesQuotProposal.getDBColumnValue()"); 
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		finally
		{
			try
			{
				//To close ResultSet, Statement and Connection in Finally 
				if(rs != null)
				{
					rs.close();
					rs = null;
				}
				if(mStmt != null)
				{
					mStmt.close();
					mStmt = null;
				}
				
			}
			catch(Exception e)
			{
				System.out.println("SalesQuotProposal.getDBColumnValue() : "+e);
			}
		}
		System.out.println(tableName +"." + columnName + " ["+columnValue+"]"); 
		return columnValue.trim();
	}
	private Map<String, String> getItemData(String itemCode, Connection conn) throws ITMException
	{
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		Map<String, String> itemDetailMap = new HashMap<String, String>();
		try
		{
			String sql = "SELECT SITE_CODE, DESCR, UNIT, LOC_CODE FROM ITEM WHERE ITEM_CODE = ?";
			preparedStatement = conn.prepareStatement(sql);
			preparedStatement.setString(1, itemCode);
			resultSet = preparedStatement.executeQuery();
			if(resultSet.next())
			{
				itemDetailMap.put("site_code", resultSet.getString("SITE_CODE"));
				itemDetailMap.put("descr", resultSet.getString("DESCR"));
				itemDetailMap.put("unit", resultSet.getString("UNIT"));
				itemDetailMap.put("loc_code", resultSet.getString("LOC_CODE"));
			}
			if(resultSet != null)
			{
				resultSet.close();
				resultSet = null;
			}
			if(preparedStatement != null)
			{
				preparedStatement.close();
				preparedStatement = null;
			}
		}
		catch(SQLException ex)
		{
			System.out.println("SQLException SalesQuotProposal.getItemData() : "+ex);
			throw new ITMException(ex); //Added By Mukesh Chauhan on 05/08/19
		}
		catch(Exception e)
		{
			System.out.println("Exception SalesQuotProposal.getItemData() : "+e);
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		System.out.println("##### SalesQuotProposal:: getItemData() : itemDetailMap : "+itemDetailMap);
		return itemDetailMap;
	}
	//private double getCostRate(Connection conn, String itemCode, Map<String, String> itemMap)
	private double getCostRate(Connection conn, String itemCode) throws ITMException
	{
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		String rateStr= "", quantityStr = "";
		Double costRate = 0.0,additionalCost = 0.0; 
		double rate = 0, totalValue = 0,quantity = 0, totalQuantity = 0 ;
		int count = 0 ;
		double overHeadValue = 0.0, overHeadVal = 0.0;
		
		try
		{
			String sql = "SELECT QUANTITY, RATE FROM STOCK WHERE ITEM_CODE = ? AND LOC_CODE IN ('FGM','QUAR')";
			preparedStatement = conn.prepareStatement(sql);
			preparedStatement.setString(1, itemCode);
			resultSet = preparedStatement.executeQuery();
			
			while(resultSet.next())
			{
				try
				{
					rateStr = E12GenericUtility.checkNull(resultSet.getString("RATE"));
					rate = getDoubleValue(rateStr);
					
				} catch (Exception e) 
				{
					System.out.println("@@@@ Excedption in rate ["+e);
				}
				try 
				{
					quantityStr = E12GenericUtility.checkNull(resultSet.getString("QUANTITY"));
					quantity = getDoubleValue(quantityStr);
				}
				catch (Exception e)
				{
					System.out.println("@@@@ Exception in Quantity "+e);
				}
				totalQuantity = totalQuantity + quantity;
				totalValue = totalValue +(rate * quantity);
				count++;
			}
			if(resultSet != null)
			{
				resultSet.close();
				resultSet = null;
			}
			if(preparedStatement != null)
			{
				preparedStatement.close();
				preparedStatement = null;
			}
			if(count > 0)
			{
				costRate = (totalValue/totalQuantity);
				if(costRate.isNaN() || costRate.isInfinite())
	     		{
					costRate = 0.0;
	     		}
				System.out.println("#### cost rate By totalValue/totalQuantity "+costRate);
				additionalCost =((getDoubleValue(getAdditionalCost(conn))/costRate) * 100 );
				if(additionalCost.isNaN() || additionalCost.isInfinite())
	     		{
					additionalCost = 0.0;
	     		}
				costRate = (costRate + additionalCost);
				System.out.println("#### cost rate with AdditionalFees "+costRate);
			}
			System.out.println("#### Actual cost "+costRate);
			
			String processOverhead = "SELECT OHD_TABLE__POST FROM ITEM WHERE ITEM_CODE = ? ";
			preparedStatement = conn.prepareStatement(processOverhead);
			preparedStatement.setString(1, itemCode);
			resultSet = preparedStatement.executeQuery();
			String ohdTablePost = "";
			while(resultSet.next())
			{
				ohdTablePost = E12GenericUtility.checkNull(resultSet.getString("OHD_TABLE__POST"));
			}
			preparedStatement.close();
			resultSet.close();
			
			try 
			{
				if( ohdTablePost.trim().length() > 0) 
				{
					String sqlOverHead = "SELECT OVERHEAD_AMT FROM PROCESS_OVERHEAD_DET WHERE TABLE_NO = ?";
					preparedStatement = conn.prepareStatement(sqlOverHead);
					preparedStatement.setString(1, ohdTablePost);
					resultSet = preparedStatement.executeQuery();
					String overHeadDetails = "";
					
					while( resultSet.next()) 
					{
						overHeadDetails = E12GenericUtility.checkNull(resultSet.getString("OVERHEAD_AMT"));
						
						overHeadVal = Double.parseDouble(overHeadDetails);
						overHeadValue = overHeadValue + overHeadVal;
					}
					System.out.println("####  overHeadValue " +overHeadValue);
					preparedStatement.close();
					resultSet.close();
				}
			} catch (Exception e)
			{
				System.out.println("@@@@ Exception in overHeadValue "+e);
			}
			costRate = (costRate + overHeadValue);
			if(costRate.isNaN() || costRate.isInfinite())
     		{
				costRate = 0.0;
     		}
			System.out.println("#### costRate with overHeadValue "+costRate);
		}
		
		catch(SQLException ex)
		{
			System.out.println("@@@@ SQLException SalesQuotProposal :: getCostRate() : "+ex);
			throw new ITMException(ex); //Added By Mukesh Chauhan on 05/08/19
		}
		catch(Exception e)
		{
			System.out.println("@@@@ Exception SalesQuotProposal :: getCostRate() : "+e);
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		
		return costRate;
	}
	private String getAdditionalCost(Connection conObj) throws ITMException
	{
		String additionalCost = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sqlStr = "SELECT VAR_VALUE FROM DISPARM WHERE VAR_NAME = ?";
		
		try
		{
			pstmt = conObj.prepareStatement(sqlStr);
			pstmt.setString(1, "ADDITIONAL_FEES");
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				additionalCost = rs.getString("VAR_VALUE");
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
		catch(Exception e)
		{
			System.out.println("@@@@@ Exception in getAdditionalCost : "+e);
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		
		return additionalCost;
	}
	private Double getDoubleValue(String valStr)
	{
		double retVal = 0.0;
		try
		{
			if(valStr.length() <= 0 )
			{
				retVal = 0;
			}
			else
			{
				retVal = Double.parseDouble(valStr);
			}
		}
		catch (NumberFormatException e)
		{
			System.out.println("@@@@ Exception in getDoubleValue "+e);
		}
		return retVal;
	}
	
	public boolean preDomExists(Document dom, String currentFormNo) throws ITMException
	{
		NodeList parentList = null;
		NodeList childList = null;
		Node childNode = null;
		boolean selected = false;

		try
		{
			parentList = dom.getElementsByTagName("Detail" + currentFormNo);
			if ( parentList.item(0) != null )
			{
				childList = parentList.item(0).getChildNodes();
				for (int ctr = 0; ctr < childList.getLength(); ctr++)
				{
					childNode = childList.item(ctr);
					if((childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue() != null))					
					{
						System.out.println("Column found!!!" + childNode.getNodeName());
						// Added by AMOL Due to discount_type has initial value so condition matches
						// making selection true to other column value.
						if(childNode.getNodeName().equalsIgnoreCase("discount_type")) 
						{
							
						}else
						{
							selected = true; 
						}
						break;
					}
				}
			}
		}
		catch ( Exception e )
		{
			System.out.println("Exception InvoiceAckwIC.preDomExists() : "+e);
			throw new ITMException(e);
		}
		System.out.println("preDomExists =["+selected+"] and currentFormNo =["+currentFormNo+"]");

		return selected;
	}
	
	/*private String getSalesOrderValues(HashMap sqlxmlMap1 ,String tranIdHdr , Connection conn) throws Exception
	{}
	 public String getPriceListType(String priceList, Connection conn) throws Exception
	{}
	

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private HashMap getDefaultData(String option, Document dom1, Connection conn) throws ITMException
	{}
	
	private String checkNull(String input)	
	{}
	private String getLineNewNo(String lineNo)	
	{}
	public static boolean isNumeric(String str)  
    {}
	
	{}
	private String getItemSer(String itemCode,String siteCode,Timestamp tranDate,String custCode,Connection conn)  throws ITMException
	{}
		
	private double getIntegralQty(Connection conn, String custCode, String itemCode, String siteCode) throws ITMException
	{}
	
	private String getSchemeDescr( Connection conn, String custCd, String itemCode, Timestamp tranDate, String siteCode)  throws ITMException
	{}
	
	public boolean preDomExists(Document dom, String currentFormNo) throws ITMException
	{}
	
	public String getPrevFormValues( Document dom, String currentFormNo, ArrayList<String> temp, String colName) throws ITMException
	{}
	
	public String getBillToCustomer(Connection conn, StringBuffer valueXmlString, String custCode,String selectedCust, int domID) throws ITMException
	{}
	
	public List<String> getStockiest(Connection conn, String custCode) throws ITMException
	{}
	
	public String getFormattedCustId(String custId) 
	{}*/
			
 }
	
