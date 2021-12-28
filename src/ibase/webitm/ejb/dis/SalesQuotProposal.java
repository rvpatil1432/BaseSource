
/********************************************************
	Title : SalesQuotProposal
	Date  : 24/06/18
	Author: Pankaj R

********************************************************/
package ibase.webitm.ejb.dis;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;

import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
// added for ejb3

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;


@Stateless // added for ejb3
public class SalesQuotProposal extends ValidatorEJB implements SalesQuotProposalLocal, SalesQuotProposalRemote
{
	E12GenericUtility genericUtility = new E12GenericUtility();
	private ArrayList<String> itemCodeArr = new ArrayList<String>();
	private ArrayList<String> itemCodeArr2 = new ArrayList<String>();
	private NumberFormat formatter = new DecimalFormat("#0.00");
	
	private int globalNo = 0;
	private String oldRate = "";
	private String currentCustCode = "",currentFromDate = "",currentToDate = "";
	//private String preViousCustCode = "",preViousFromDate = "",preViousToDate = "";
	HashMap<String,String> firstFormDetailMap = new HashMap<String,String>();
	
	private Set<Integer> srNoSet = null;//added on 10-10-18 Add new Form no2 to get srNo.
	private Set<Integer> copyOfsrNoSet = null;
	private String globaldisPriceList = ""; 
	private String globalpriceList = ""; 
	private String globalcommTable = "";
	
	public String wfValData(String xmlString, String xmlString1, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		
		String errString = null;
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			errString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams);
			
		}
		catch(Exception e)
		{
			throw new ITMException( e );
		}
		return (errString);
	}
	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag, String xtraParams, String formName) throws RemoteException,ITMException
	{
		
		System.out.println("SalesQuotProposal formName : "+formName);
		String errString = null;
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{
			System.out.println("###### SalesQuotProposal xmlString:-" + xmlString );
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString("<Root>" + xmlString2+ "</Root>");
			}
			System.out.println("###### SalesQuotProposal objContext["+objContext+"]\nwfValData.xmlString1["+xmlString1+"]");
			System.out.println("###### wfValData.xmlString2["+xmlString2+"]");
			errString = wfValData(dom,dom1,dom2,objContext,editFlag,xtraParams, formName);
			System.out.println("###### errString[" + errString+"]" );
		}
		catch(Exception e)
		{
			throw new ITMException( e );
		}
		return (errString);
	}
	public String wfValData(Document dom, Document dom1,Document dom2, String objContext, String editFlag, String xtraParams, String formName) throws RemoteException,ITMException
	{
		System.out.println(" #### SalesQuotProposal :: wfValData 3333 (Document dom, Document dom1,Document dom2, String objContext, String editFlag, String xtraParams)***********" );
		NodeList parentNodeList = null;NodeList childNodeList = null;
		Node parentNode = null;Node childNode = null;
		String childNodeName = null,cildNodeValue = null;
		Connection conn = null;
		PreparedStatement pstmt = null ;
		ResultSet rs = null , rs1 = null;
		int cnt = 0, currentFormNo=0,childNodeListLength ,ctr=0;
        ConnDriver connDriver = new ConnDriver();
		String errString = "", errCode  = "",userId = "";
		ArrayList <String> errList = new ArrayList<String>();
		ArrayList <String>errFields = new ArrayList <String> ();
		String errorType = "";
		String custCode= "", currAppdateStr ="", validUptoStr = "",effFromStr = "", fromDtStr = "", toDtStr = "";
		double discount = 0, commPerc = 0, newRate = 0;
		Timestamp currentDate = null, toDate = null, fromDate = null, validUptoDate = null, effFromDate = null;
		StringBuffer errStringXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root><Errors>"); 
		
		int count2 = 0;
		Node node = null;
		Element element = null;
		String srNoStr = "", itemCodeStr = "", quantityStr = "", rateStr = "";
		String newRateStr = "";
	
		try
		{
			connDriver = null;
			conn = getConnection();
			userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			System.out.println("SalesQuotProposal userId:- "  + userId);

			java.sql.Timestamp currDate = null;
			currDate = new java.sql.Timestamp(System.currentTimeMillis()) ;
			currAppdateStr = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(currDate).toString();
			currentDate = Timestamp.valueOf(genericUtility.getValidDateString(currAppdateStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())
					+ " 00:00:00.0");
			System.out.println("####### currAppdate : "+currentDate);
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			System.out.println("currentFormNo...." + currentFormNo+"\nformName : "+formName);
			//sale_quot  sale_quotdet  sale_quotitems
			if("sale_quot".equalsIgnoreCase(formName))
			{

				parentNodeList = dom.getElementsByTagName("Detail1");
				int parentNodeListLen = parentNodeList.getLength();
				System.out.println("##### Number of node in parentNodeListLen : "+parentNodeListLen);
				for ( int rowCnt=0; rowCnt < parentNodeListLen; rowCnt++ )
				{
					parentNode = parentNodeList.item(rowCnt);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
					for(ctr = 0; ctr < childNodeListLength; ctr++)
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						System.out.println("##### childNodeName R : "+childNodeName);
						if (childNodeName.equalsIgnoreCase("cust_code"))
						{
							custCode = E12GenericUtility.checkNull(genericUtility.getColumnValue("cust_code", dom));
							String str = "select count(1) from customer where cust_code = ?";
							int count = 0;
							pstmt = conn.prepareStatement(str);
							pstmt.setString(1, custCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count = rs.getInt(1);
							}
							System.out.println("#### count : "+count);
							pstmt.close();
							pstmt = null;
							rs.close();
							rs = null;
							if(count == 0)
							{
								errCode = "VMREGCUST";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;	
							}
						}
						else if (childNodeName.equalsIgnoreCase("from_date"))
						{
							fromDtStr = E12GenericUtility.checkNull(genericUtility.getColumnValue("from_date", dom));
							toDtStr = E12GenericUtility.checkNull(genericUtility.getColumnValue("to_date", dom));
							if(fromDtStr.length() <= 0 || toDtStr.length() <= 0)
							{
								errCode = "VTINVFTDT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
							
							fromDate = Timestamp.valueOf(genericUtility.getValidDateString(fromDtStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())
									+ " 00:00:00.0");
							toDate = Timestamp.valueOf(genericUtility.getValidDateString(toDtStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())
									+ " 00:00:00.0");

							if(toDate == null || fromDate == null)
							{
								errCode = "VTINVFTDT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
							if(toDate.before(fromDate))
							{
								errCode = "VTINVFRDT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
						}
						else if (childNodeName.equalsIgnoreCase("valid_upto"))
						{
							validUptoStr = E12GenericUtility.checkNull(genericUtility.getColumnValue("valid_upto", dom));
							effFromStr = E12GenericUtility.checkNull(genericUtility.getColumnValue("eff_date", dom));
							
							if(validUptoStr.length() <= 0 || effFromStr.length() <= 0)
							{
								errCode = "VTINVEVDT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
							
							validUptoDate = Timestamp.valueOf(genericUtility.getValidDateString(validUptoStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())
									+ " 00:00:00.0");
							effFromDate = Timestamp.valueOf(genericUtility.getValidDateString(effFromStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())
									+ " 00:00:00.0");
							
							if(validUptoDate == null || effFromDate == null)
							{
								errCode = "VTINVEVDT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
							if(validUptoDate.before(effFromDate))
							{
								errCode = "VTINVEFDT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
							if(effFromDate.before(currentDate))
							{
								errCode = "EFCDTPST";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
						}
						/*else if(childNodeName.equalsIgnoreCase("item_ser")) 
						{
							String itemSeries = E12GenericUtility.checkNull(genericUtility.getColumnValue("item_ser", dom));
							System.out.println("##### itemSeries "+itemSeries);
							if( itemSeries.length() == 0)
							{
								errCode = "VMINVITMSR";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
						}*/
						
					}
				}
			
			}
			else if("sale_quotdet".equalsIgnoreCase(formName))
			{
				parentNodeList = dom.getElementsByTagName("Detail2");
				int parentNodeListLen = parentNodeList.getLength();
				System.out.println("##### Number of node in parentNodeListLen : "+parentNodeListLen);
				
				for ( int rowCnt=0; rowCnt < parentNodeListLen; rowCnt++ )
				{
					parentNode = parentNodeList.item(rowCnt);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
					
					for(ctr = 0; ctr < childNodeListLength; ctr++)
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						System.out.println("#### Detail 2 Name [ "+childNodeName+"]");
						
						//System.out.println("#### Detail3 Value [ "+childNode.getFirstChild().getNodeValue()+"]");
						
						if(childNodeName.equalsIgnoreCase("item_code")) 
						{
							itemCodeStr = E12GenericUtility.checkNull(genericUtility.getColumnValue("item_code", dom));
							System.out.println("###### item_code ["+itemCodeStr+"]");
							if(itemCodeStr.isEmpty())
							{
								errCode = "INVITMCO";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
						}
					}
					
					try 
					{
						System.out.println("#### IN FORM 2 ");
						parentNode = parentNodeList.item(rowCnt);
						
						Element childElement = (Element)parentNode;
						childNode = childElement.getElementsByTagName("sr_no").item(0); 
						//String nodeValStr = childNode.getFirstChild().getNodeValue();
						Node firstChildNode = childNode.getFirstChild();
						if(firstChildNode == null)
						{
							System.out.println("### IN IF @@@@");
							errCode = "VTBLNKDTL";
							errList.add(errCode);
							errFields.add("sr_no");
						}else 
						{
							System.out.println("#### In ELSE FOR SELECTED ");
							
							if(childElement.getElementsByTagName("selectbox") != null)
							{
								childNode = childElement.getElementsByTagName("selectbox").item(0);
								
								if(childNode != null && childNode.getFirstChild() != null)
								{
									String valStr = childNode.getFirstChild().getNodeValue();
									System.out.println("#### valStr "+valStr);
									if(valStr.equalsIgnoreCase("true")) 
									{
										count2 ++;
									}
								}
							}
							if(childElement.getElementsByTagName("selected") != null)
							{
								childNode = childElement.getElementsByTagName("selected").item(0);
								
								if(childNode != null && childNode.getFirstChild() != null)
								{
									String valStr = childNode.getFirstChild().getNodeValue();
									System.out.println("#### valStr "+valStr);
									if(valStr.equalsIgnoreCase("true")) 
									{
										count2 ++;
									}
								}
							}
							
						}
					} catch (Exception e)
					{
						e.printStackTrace();
					}
					
				}
				if(count2 == 0) 
				{
					System.out.println("###### COUNT "+count2);
					errCode = "VTBLNKDTL";
					errList.add(errCode);
					errFields.add("selected");
				}
				count2 = 0 ;
				
			}		
			else if("sale_quotitems".equalsIgnoreCase(formName))
			{
				System.out.println("##### APPLY VALIDATION @@@@@");
				parentNodeList = dom.getElementsByTagName("Detail3");
				int parentNodeListLen = parentNodeList.getLength();
				//Element childElem = null;
				for ( int rowCnt=0; rowCnt < parentNodeListLen; rowCnt++ )
				{
					parentNode = parentNodeList.item(rowCnt);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
					
					for(ctr = 0; ctr < childNodeListLength; ctr++)
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						System.out.println("#### Detail 3 Name [ "+childNodeName+"]");
						
						if(childNodeName.equalsIgnoreCase("sr_no")) 
						{
							srNoStr = E12GenericUtility.checkNull(genericUtility.getColumnValue("sr_no", dom));
							System.out.println("###### srNoStr ["+srNoStr+"]");
							if(srNoStr == null)
							{
								errCode = "INVSRNO";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
						}
						else if(childNodeName.equalsIgnoreCase("qty_per_month")) 
						{
							quantityStr = E12GenericUtility.checkNull(genericUtility.getColumnValue("qty_per_month", dom));
							System.out.println("###### quantityStr ["+quantityStr+"]");
							if(quantityStr.isEmpty())
							{
								errCode = "INVQTYPM";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
						}
						else if(childNodeName.equalsIgnoreCase("item_code")) 
						{
							itemCodeStr = E12GenericUtility.checkNull(genericUtility.getColumnValue("item_code", dom));
							System.out.println("###### item_code ["+itemCodeStr+"]");
							if(itemCodeStr.isEmpty())
							{
								errCode = "INVITMCO";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
						}
						else if(childNodeName.equalsIgnoreCase("rate")) 
						{
							rateStr = E12GenericUtility.checkNull(genericUtility.getColumnValue("rate", dom));
							System.out.println("###### rateStr ["+rateStr+"]");
							if(rateStr.isEmpty())
							{
								errCode = "INVRATE";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
						}
						else if (childNodeName.equalsIgnoreCase("rate__new"))
						{
							newRateStr = E12GenericUtility.checkNull(genericUtility.getColumnValue("rate__new", dom));
							System.out.println("###### newRateStr ["+newRateStr+"]");
							if(newRateStr.isEmpty())
							{
								errCode = "INVNEWRT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
						}
						
					}
				}
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
					
					System.out.println("##### error code is :"+errCode);
					errFldName = (String)errFields.get(cnt);
					System.out.println("##### cnt [" + cnt + "] errCode [" + errCode + "] errFldName [" + errFldName + "]");
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
			//System.out.println("Exception ::"+e);
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
	//END OF VALIDATION
	//START preValData
	public String preValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag, String xtraParams, String formName) throws RemoteException,ITMException
	{
		
		
		String errString = null;
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
			
			System.out.println("###### preValData xmlString2["+xmlString2+"]");
			errString = preValData(dom,dom1,dom2,objContext,editFlag,xtraParams, formName);
			
		}
		catch(Exception e)
		{
			throw new ITMException( e );
		}
		return (errString);
	}
	public String preValData(Document dom, Document dom1,Document dom2, String objContext, String editFlag, String xtraParams, String formName) throws RemoteException,ITMException
	{
		System.out.println(" ########### SalesQuotProposal ********** preValData 3333 " );
		NodeList parentNodeList = null;NodeList childNodeList = null;
		Node parentNode = null;Node childNode = null;
		String childNodeName = null,cildNodeValue = null;
		Connection conn = null;
		PreparedStatement pstmt = null ;
		ResultSet rs = null , rs1 = null;
		int cnt = 0, currentFormNo=0,childNodeListLength ,ctr=0;
        ConnDriver connDriver = new ConnDriver();
		String errString = "", errCode  = "",userId = "";
		ArrayList <String> errList = new ArrayList<String>();
		ArrayList <String>errFields = new ArrayList <String> ();
		String errorType = "";
		String custCode= "", currAppdateStr ="", validUptoStr = "",effFromStr = "", fromDtStr = "", toDtStr = "";
		double discount = 0, commPerc = 0, newRate = 0;
		Timestamp currentDate = null, toDate = null, fromDate = null, validUptoDate = null, effFromDate = null;
		StringBuffer errStringXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root><Errors>"); 
		
		int count2 = 0;
		Node node = null;
		Element element = null;
		
		
		String srNoStr = "", itemCodeStr = "", quantityStr = "", rateStr = "";
		String newRateStr = "";
		try
		{
			connDriver = null;
			conn = getConnection();
			userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			System.out.println("SalesQuotProposal userId:- "  + userId);

			java.sql.Timestamp currDate = null;
			currDate = new java.sql.Timestamp(System.currentTimeMillis()) ;
			currAppdateStr = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(currDate).toString();
			currentDate = Timestamp.valueOf(genericUtility.getValidDateString(currAppdateStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())
					+ " 00:00:00.0");
			
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			
			
			if("sale_quot".equalsIgnoreCase(formName))
			{

				parentNodeList = dom.getElementsByTagName("Detail1");
				int parentNodeListLen = parentNodeList.getLength();
				
				for ( int rowCnt=0; rowCnt < parentNodeListLen; rowCnt++ )
				{
					parentNode = parentNodeList.item(rowCnt);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
					for(ctr = 0; ctr < childNodeListLength; ctr++)
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						
						if (childNodeName.equalsIgnoreCase("cust_code"))
						{
							custCode = E12GenericUtility.checkNull(genericUtility.getColumnValue("cust_code", dom));
							String str = "select count(1) from customer where cust_code = ?";
							int count = 0;
							pstmt = conn.prepareStatement(str);
							pstmt.setString(1, custCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count = rs.getInt(1);
							}
							
							pstmt.close();
							pstmt = null;
							rs.close();
							rs = null;
							if(count == 0)
							{
								errCode = "VMREGCUST";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;	
							}
						}
						else if (childNodeName.equalsIgnoreCase("from_date"))
						{
							fromDtStr = E12GenericUtility.checkNull(genericUtility.getColumnValue("from_date", dom));
							toDtStr = E12GenericUtility.checkNull(genericUtility.getColumnValue("to_date", dom));
							if(fromDtStr.length() <= 0 || toDtStr.length() <= 0)
							{
								errCode = "VTINVFTDT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
							
							fromDate = Timestamp.valueOf(genericUtility.getValidDateString(fromDtStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())
									+ " 00:00:00.0");
							toDate = Timestamp.valueOf(genericUtility.getValidDateString(toDtStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())
									+ " 00:00:00.0");

							if(toDate == null || fromDate == null)
							{
								errCode = "VTINVFTDT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
							if(toDate.before(fromDate))
							{
								errCode = "VTINVFRDT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
						}
						else if (childNodeName.equalsIgnoreCase("valid_upto"))
						{
							validUptoStr = E12GenericUtility.checkNull(genericUtility.getColumnValue("valid_upto", dom));
							effFromStr = E12GenericUtility.checkNull(genericUtility.getColumnValue("eff_date", dom));
							
							if(validUptoStr.length() <= 0 || effFromStr.length() <= 0)
							{
								errCode = "VTINVEVDT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
							
							validUptoDate = Timestamp.valueOf(genericUtility.getValidDateString(validUptoStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())
									+ " 00:00:00.0");
							effFromDate = Timestamp.valueOf(genericUtility.getValidDateString(effFromStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())
									+ " 00:00:00.0");
							
							if(validUptoDate == null || effFromDate == null)
							{
								errCode = "VTINVEVDT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
							if(validUptoDate.before(effFromDate))
							{
								errCode = "VTINVEFDT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
							if(effFromDate.before(currentDate))
							{
								errCode = "EFCDTPST";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
						}
						else if(childNodeName.equalsIgnoreCase("item_ser")) 
						{
							String itemSeries = E12GenericUtility.checkNull(genericUtility.getColumnValue("item_ser", dom));
							System.out.println("##### itemSeries "+itemSeries);
							if( itemSeries.length() == 0)
							{
								errCode = "VMINVITMSR";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
						}
						
					}
				}
			
			}
			else if("sale_quotdet".equalsIgnoreCase(formName)) 
			{
				parentNodeList = dom.getElementsByTagName("Detail2");
				int parentNodeListLen = parentNodeList.getLength();
				System.out.println("##### Number of node in parentNodeListLen : "+parentNodeListLen);
				
				for ( int rowCnt=0; rowCnt < parentNodeListLen; rowCnt++ )
				{
					try {
						parentNode = parentNodeList.item(rowCnt);
						childNodeList = parentNode.getChildNodes();
						childNodeListLength = childNodeList.getLength();
						
						for(ctr = 0; ctr < childNodeListLength; ctr++)
						{
							childNode = childNodeList.item(ctr);
							childNodeName = childNode.getNodeName();
							System.out.println("#### Detail 2 Name [ "+childNodeName+"]");
															
							if(childNodeName.equalsIgnoreCase("item_code")) 
							{
								itemCodeStr = E12GenericUtility.checkNull(genericUtility.getColumnValue("item_code", dom));
								System.out.println("###### item_code ["+itemCodeStr+"]");
								if(itemCodeStr.isEmpty())
								{
									errCode = "INVITMCO";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									break;
								}
							}
						}
					} catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}		
			else if("sale_quotitems".equalsIgnoreCase(formName))
			{
				System.out.println("##### PreValData :: sale_quotitems ");
				parentNodeList = dom.getElementsByTagName("Detail3");
				int parentNodeListLen = parentNodeList.getLength();
				//Element childElem = null;
				for ( int rowCnt=0; rowCnt < parentNodeListLen; rowCnt++ )
				{
					parentNode = parentNodeList.item(rowCnt);
					childNodeList = parentNode.getChildNodes();
					childNodeListLength = childNodeList.getLength();
					
					for(ctr = 0; ctr < childNodeListLength; ctr++)
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						System.out.println("#### Detail 3 Name [ "+childNodeName+"]");
						
						if(childNodeName.equalsIgnoreCase("sr_no")) 
						{
							srNoStr = E12GenericUtility.checkNull(genericUtility.getColumnValue("sr_no", dom));
							System.out.println("###### srNoStr ["+srNoStr+"]");
							if(srNoStr == null)
							{
								errCode = "INVSRNO";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
						}
						else if(childNodeName.equalsIgnoreCase("qty_per_month")) 
						{
							quantityStr = E12GenericUtility.checkNull(genericUtility.getColumnValue("qty_per_month", dom));
							
							if(quantityStr.isEmpty())
							{
								errCode = "INVQTYPM";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
						}
						else if(childNodeName.equalsIgnoreCase("item_code")) 
						{
							itemCodeStr = E12GenericUtility.checkNull(genericUtility.getColumnValue("item_code", dom));
							System.out.println("###### item_code ["+itemCodeStr+"]");
							if(itemCodeStr.isEmpty())
							{
								errCode = "INVITMCO";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
						}
						else if(childNodeName.equalsIgnoreCase("rate")) 
						{
							rateStr = E12GenericUtility.checkNull(genericUtility.getColumnValue("rate", dom));
							System.out.println("###### rateStr ["+rateStr+"]");
							if(rateStr.isEmpty())
							{
								errCode = "INVRATE";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
						}
						else if (childNodeName.equalsIgnoreCase("rate__new"))
						{
							newRateStr = E12GenericUtility.checkNull(genericUtility.getColumnValue("rate__new", dom));
							System.out.println("###### newRateStr ["+newRateStr+"]");
							if(newRateStr.isEmpty())
							{
								errCode = "INVNEWRT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
						}
						
					}
				}
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
					
					System.out.println("##### error code is :"+errCode);
					errFldName = (String)errFields.get(cnt);
					System.out.println("##### cnt [" + cnt + "] errCode [" + errCode + "] errFldName [" + errFldName + "]");
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
	//END
	public String itemChanged(String xmlString, String xmlString1, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		System.out.println("xmlString...["+xmlString+"]");
		System.out.println("xmlString1.....["+xmlString1+"]");
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try
		{
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			valueXmlString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException( e );
		}
        return valueXmlString;
	}
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams, String formName) throws RemoteException,ITMException
	{
		
		System.out.println("###### Current Form Data DOM ["+xmlString+"]");
		System.out.println("###### Previuos Form Data DOM1 ["+xmlString1+"]");
		System.out.println("###### ALL FORM Data DOM2 ["+xmlString2+"]");
		System.out.println("###### formName["+formName+"]");
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try
		{
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			 if (xmlString2.trim().length() > 0 )
			 {
				 dom2 = parseString(xmlString2);
			 }
			valueXmlString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams,formName);
		}
		catch(Exception e)
		{
			throw new ITMException(e);
		}
        return valueXmlString;
	}
	
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams, String formName) throws RemoteException,ITMException
	{
		System.out.println("#### SalesQuotProposal :: itemChanged 3");
		System.out.println("#### SalesQuotProposal :: formName ["+formName+"]");
		System.out.println("#### objContext ["+objContext+"]");
		System.out.println("#### currentColumn ["+currentColumn+"]");
		System.out.println("#### editFlag ["+editFlag+"]");
		System.out.println("#### xtraParams ["+xtraParams+"]");
		
		
	
		StringBuffer valueXmlString = null;
		Connection conn = null;
		PreparedStatement pstmt = null; 
		ResultSet rs = null;
		ConnDriver connDriver = new ConnDriver();
		int currentFormNo = 0 ;
		String userId = "",siteCode = "", custCode = "",toDateStr = "", fromDateStr = "", chgTerm = "", custName = "";
		String qtyMonth = "", rate = "", discount = "", grossRate = "", commission = "",commissionPerUnit = "",basicPrice ="", newRate ="";
		String additionalCost = "",discountType = "";
		double  newCostRate  = 0.0, costRate = 0.0, profit =0.0, profitOnGoods = 0.0, 
				finalPrice = 0.0,  newRte = 0.0, discountDouble = 0.0;
		
		Double newGrossRate =0.0, commPerUnit = 0.0, percProfit = 0.0;
				
		Timestamp toDate = null, fromDate = null;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		
		
		try
		{
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");
			conn = getConnection();
			
			connDriver = null;
			
			java.sql.Timestamp currDate = null;
			currDate = new java.sql.Timestamp(System.currentTimeMillis()) ;
			
			InetAddress ownIP=InetAddress.getLocalHost();
			userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			siteCode = getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
			chgTerm = E12GenericUtility.checkNull(ownIP.getHostAddress());
			String currAppdate = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(currDate).toString();
			System.out.println("####### currAppdate : "+currAppdate);
			oldRate = checkNull(genericUtility.getColumnValue("rate",dom2));
			
			System.out.println("######## siteCode["+siteCode+"]");	
			if("sale_quot".equalsIgnoreCase(formName))
			{
				int domID = 1;
				if(currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					custCode = genericUtility.getColumnValue("cust_code", dom1);
					fromDateStr = genericUtility.getColumnValue("from_date", dom1);
					toDateStr = genericUtility.getColumnValue("to_date", dom1);
					String effDateStr = genericUtility.getColumnValue("eff_date", dom1);
					String validUptoStr = genericUtility.getColumnValue("valid_upto", dom1);
					String itemSeries = genericUtility.getColumnValue("item_ser", dom1);
					
					System.out.println("#### currAppdate ["+currAppdate+"]");
					System.out.println("#### fromDateStr ["+fromDateStr);
					System.out.println("#### toDateStr ["+toDateStr);
					System.out.println("#### effDateStr ["+effDateStr);
					System.out.println("#### validUptoStr ["+validUptoStr);
					
					valueXmlString.append("<Detail1 domID='"+ domID +"' selected=\"Y\">\r\n");
					valueXmlString.append("<attribute  selected=\"Y\" updateFlag=\"A\"  status=\"N\" pkNames=\"\" />\r\n");
					valueXmlString.append("<quot_no/>");
					valueXmlString.append( "<quot_date protect='1'><![CDATA[" ).append(currAppdate).append( "]]></quot_date>\r\n" );
					valueXmlString.append( "<quot_type protect='1'><![CDATA[P]]></quot_type>\r\n" );
					
					valueXmlString.append("<cust_code><![CDATA["+custCode+"]]></cust_code>");
					valueXmlString.append("<from_date><![CDATA["+fromDateStr+"]]></from_date>");
					valueXmlString.append("<to_date><![CDATA["+toDateStr+"]]></to_date>");
					//valueXmlString.append("<eff_date><![CDATA["+effDateStr+"]]></eff_date>");
					valueXmlString.append("<eff_date><![CDATA["+currAppdate+"]]></eff_date>");
					valueXmlString.append("<valid_upto><![CDATA["+validUptoStr+"]]></valid_upto>");
					valueXmlString.append("<wf_status><![CDATA["+"N"+"]]></wf_status>");
					//<<<<<<<<<<<<<<<<<<<<<<<<<<< Make SURE additional_fees added in sales_quot TABLE >>>
					valueXmlString.append("<addl_chrg><![CDATA["+getAdditionalCost(conn)+"]]></addl_chrg>");
					valueXmlString.append("<item_ser><![CDATA["+itemSeries+"]]></item_ser>");
					
					valueXmlString.append("<quot_date><![CDATA[" +currAppdate+ "]]></quot_date>");
					valueXmlString.append("<chg_date><![CDATA[" +currAppdate+ "]]></chg_date>");
					valueXmlString.append("<chg_user><![CDATA[" + userId+ "]]></chg_user>");
					valueXmlString.append("<chg_term><![CDATA[" + chgTerm + "]]></chg_term>");
					valueXmlString.append("</Detail1>\r\n");
				
				}
				else if(currentColumn.trim().equalsIgnoreCase("cust_code"))
				{

					custCode = checkNull(genericUtility.getColumnValue("cust_code",dom));
					fromDateStr = checkNull(genericUtility.getColumnValue("from_date",dom));// Added by AMOL
					toDateStr = checkNull(genericUtility.getColumnValue("to_date",dom)); //Added by AMOL
					
					String currDomStr = genericUtility.serializeDom(dom);
					StringBuffer valueXmlStr = new StringBuffer();
					custName = getDBColumnValue("CUSTOMER", "CUST_NAME", "CUST_CODE= '"+custCode+"'", conn);
					valueXmlStr.append( "<cust_name><![CDATA[" ).append(custName).append( "]]></cust_name>\r\n" );
					currDomStr = currDomStr.replace("</Detail1>", valueXmlStr.toString() + "</Detail1>");
					valueXmlString.append(currDomStr);
					
				}
			}	
			else if("sale_quotdet".equalsIgnoreCase(formName))
			{
				int domID = 1;
				if(currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					
					//DOM1 Data of 1st form DOM
					
					custCode = genericUtility.getColumnValue("cust_code", dom1);
					fromDateStr = genericUtility.getColumnValue("from_date", dom1);
					toDateStr = genericUtility.getColumnValue("to_date", dom1);
									
					currentCustCode = custCode;//
					currentFromDate = fromDateStr;//
					currentToDate = toDateStr;//
					
					
					boolean preDomExist = preDomExists(dom2, objContext);
					
					System.out.println("#### Does preDomExist : "+preDomExist );
					System.out.println("#### currentCustCode "+currentCustCode );
					System.out.println("#### preViousCustCode "+firstFormDetailMap.get("CustCode") );
					System.out.println("#### currentFromDate "+currentFromDate );
					System.out.println("#### preViousFromDate "+firstFormDetailMap.get("FromDate") );
					System.out.println("#### currentToDate "+currentToDate );
					System.out.println("#### preViousToDate "+firstFormDetailMap.get("ToDate") );
					
					
					if(preDomExist && currentCustCode.equals(firstFormDetailMap.get("CustCode")) && currentFromDate.equals(firstFormDetailMap.get("FromDate")) && currentToDate.equals(firstFormDetailMap.get("ToDate")))
					{
						System.out.println("<<<<<<<<<<< preDomExists Data >>>>>>>>>");
						//selectedItem = getPrevFormValues( dom2, formNo, new ArrayList<String>(Arrays.asList(temp)), "strg_code" ) ;
					}
					else
					{
						System.out.println(" !!!!!!!!!!!  New Data !!!!!!!!!!!!");
						
						List<String> invoiceIdArr = new ArrayList<String>();
						currAppdate = new SimpleDateFormat(genericUtility.getApplDateFormat()).format(currDate).toString();
						custCode = checkNull(genericUtility.getColumnValue("cust_code",dom1));
						fromDateStr = checkNull(genericUtility.getColumnValue("from_date", dom1));
						toDateStr = checkNull(genericUtility.getColumnValue("to_date", dom1));
						discountType = checkNull(genericUtility.getColumnValue("discount_type", dom1));
						
						/*preViousCustCode = custCode;
						preViousFromDate = fromDateStr;
						preViousToDate = toDateStr;*/
						firstFormDetailMap.put("CustCode", custCode);
						firstFormDetailMap.put("FromDate", fromDateStr);
						firstFormDetailMap.put("ToDate", toDateStr);
						
						System.out.println("#### preViousCustCode "+firstFormDetailMap.get("CustCode"));
						System.out.println("#### previous fromDate "+firstFormDetailMap.get("FromDate"));
						System.out.println("#### previous To date "+firstFormDetailMap.get("ToDate"));
						
						toDate = Timestamp.valueOf(genericUtility.getValidDateString(toDateStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())
																					+ " 00:00:00.0");
						fromDate = Timestamp.valueOf(genericUtility.getValidDateString(fromDateStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())
																					+ " 00:00:00.0");
	
						invoiceIdArr = getInvoiceDetail(conn, custCode, fromDate, toDate, valueXmlString,siteCode,discountType);
					}
				}
				//START 08-10-18
				else if(currentColumn.trim().equalsIgnoreCase("itm_default_add"))
				{
					System.out.println("@@@@@@      #### "+srNoSet);
					try
					{	
						
						String newSrNo = "";
						System.out.println("###### "+getLineNo(dom));
								System.out.println("###### "+getLineNo(dom1));
										System.out.println("###### "+getLineNo(dom2));
						
						int lineNo = Integer.parseInt(getLineNo(dom1));				
						String custCodeStrng = genericUtility.getColumnValue("cust_code", dom1);//Logic2
						String fromDateStrng = genericUtility.getColumnValue("from_date", dom1);//Logic2
						String toDateStrrng = genericUtility.getColumnValue("to_date", dom1);//Logic2
						if(srNoSet == null) //Logic2
						{
							getGoodsMap(custCodeStrng,fromDateStrng,toDateStrrng);
							getDomId(dom2);
							if(lineNo >= 1) 
							{
								if(srNoSet != null) 
								{
									//getDomId(dom2);
									//newSrNo = (srNoSet.size()+lineNo)+"";
									newSrNo = (srNoSet.size())+"";
								}else
								{
									newSrNo = ""+lineNo;
								}
							}
							
						}
						
						/*if(copyOfsrNoSet != null) 
						{
							srNoSet.addAll(copyOfsrNoSet);//Add new sr No Form 2
							copyOfsrNoSet.clear();////Add new sr No Form 2	
						}
						if(srNoSet != null) //Logic2
						{
							newSrNo = (srNoSet.size()+1)+"";
						}
						if(!srNoSet.isEmpty()) ////Logic1
						{
							newSrNo = (srNoSet.size()+1)+"";
														
						}
						else 
						{
							newSrNo = getLineNo(dom2);
							System.out.println("######### New SrNo "+newSrNo); 
							
							if("0".equals(newSrNo)) 
							{
								System.out.println("######## ZerO SeT...");
								newSrNo = "1";
							}	
						}*/
						
						System.out.println("######### AFter New SrNo "+newSrNo); 
						valueXmlString.append("<Detail2 domID=\""+newSrNo+"\" selected=\"N\" > \r\n");
			     		valueXmlString.append("<attribute  selected=\"N\" updateFlag=\"A\"  status=\"N\" pkNames=\"\" />\r\n");
			     		valueXmlString.append("<quot_no/>");
			     		valueXmlString.append( "<line_no protect='1'><![CDATA[" ).append(newSrNo).append( "]]></line_no>\r\n" );
			     		valueXmlString.append("<sr_no><![CDATA[" ).append(newSrNo).append( "]]></sr_no>\r\n" );
			     		
						valueXmlString.append("<item_code><![CDATA[" ).append("").append( "]]></item_code>\r\n" );
			     		valueXmlString.append("<item_descr><![CDATA[" ).append("").append( "]]></item_descr>\r\n" );
			     		valueXmlString.append("<unit><![CDATA[" ).append("").append( "]]></unit>\r\n" );
			     		valueXmlString.append("<qty_per_month><![CDATA[" ).append("").append( "]]></qty_per_month>\r\n" );
			     		valueXmlString.append("<rate><![CDATA[" ).append("").append( "]]></rate>\r\n" );
			     		valueXmlString.append("<discount><![CDATA[" ).append("").append( "]]></discount>\r\n" );
			     		valueXmlString.append("<gross_rate><![CDATA[" ).append("").append( "]]></gross_rate>\r\n" );
			     		valueXmlString.append("<comm_perc><![CDATA[" ).append("").append( "]]></comm_perc>\r\n" );
			     		valueXmlString.append("<comm_per_unit><![CDATA[" ).append("").append( "]]></comm_per_unit>\r\n" );
			     		valueXmlString.append("<rate__new><![CDATA[" ).append("").append( "]]></rate__new>\r\n" );
			     		valueXmlString.append("<basic_dist_price><![CDATA[" ).append("").append( "]]></basic_dist_price>\r\n" );
			     		valueXmlString.append("<cost_rate><![CDATA[" ).append("").append( "]]></cost_rate>\r\n" );
			     		valueXmlString.append("<profit><![CDATA[" ).append("").append( "]]></profit>\r\n" );
			     		valueXmlString.append("<profit_on_goods><![CDATA[" ).append("").append( "]]></profit_on_goods>\r\n" );
			     		valueXmlString.append("<final_price><![CDATA[" ).append("").append( "]]></final_price	>\r\n" );
			     		valueXmlString.append("<perc_profit><![CDATA[" ).append("").append( "]]></perc_profit>\r\n" );
			     		valueXmlString.append("<available_stock><![CDATA[" ).append("").append( "]]></available_stock>\r\n" );
			     		valueXmlString.append("</Detail2>");
			     		

					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				
				}
				else if(currentColumn.trim().equalsIgnoreCase("item_code"))
				{

					Map<String, String> itemDetailMap = new HashMap<String, String>();
					try
					{
						StringBuffer valXmlStr = new StringBuffer();
						String currDomStr = genericUtility.serializeDom(dom);
						String itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						double quantity = getDoubleValue(checkNull(genericUtility.getColumnValue("qty_per_month", dom)));
						discountType = checkNull(genericUtility.getColumnValue("discount_type",dom));
						
						itemDetailMap = getItemData(itemCode, conn);
						
						double discount2  = getRate(conn, globaldisPriceList, itemCode);
			            double rate2  = getRate(conn, globalpriceList, itemCode);
			     		
			     		Double grossRate2 = ((rate2 - ((rate2 * discount2) / 100)));
			     		if(grossRate2.isNaN() || grossRate2.isInfinite())
			     		{
			     			grossRate2 = 0.0;
			     		}
			     		double comm = 0;
			     		try
			     		{
			     			comm = getCommision(globalcommTable,currDate,conn,itemCode);
			     		}
			     		catch(Exception e) 
			     		{
			     			comm = 0;
			     		}
			     		
			     		double commPerUnit2 = (((grossRate2 * comm) / 100));
			     		double newCostRate2 = ((grossRate2 - commPerUnit2));
			     		double costRate2 = getCostRate(conn, itemCode);
			     		double availblStock = getStockDetails(conn,itemCode);
			     		double profit2 = (newCostRate - costRate);
			     		String profitParse = String.format("%.2f", profit);
						double profitOnGoods2 = (Double.parseDouble(profitParse) * quantity);
			     		double finalPrice2 = (grossRate2 * quantity);
			     		Double percProfit2 = ((profitOnGoods2 / finalPrice2) * 100);

			     		if(percProfit2.isNaN() || percProfit2.isInfinite())
			     		{
			     			percProfit2 = 0.0;
			     		}
						
						
			     		valXmlStr.append("<quot_no>").append("<![CDATA["+ checkNull(genericUtility.getColumnValue("quot_no",dom)) +"]]>").append("</quot_no>");
			     		valXmlStr.append("<line_no>").append("<![CDATA["+  checkNull(genericUtility.getColumnValue("line_no",dom)) +"]]>").append("</line_no>");
			     		valXmlStr.append("<item_code protect='1'><![CDATA[" ).append(itemCode).append( "]]></item_code>\r\n" );
			     		valXmlStr.append("<item_descr><![CDATA[" ).append(itemDetailMap.get("descr")).append( "]]></item_descr>\r\n" );
			     		valXmlStr.append("<unit><![CDATA[" ).append(itemDetailMap.get("unit")).append( "]]></unit>\r\n" );
			     		valXmlStr.append("<qty_per_month><![CDATA[" ).append(quantity).append( "]]></qty_per_month>\r\n" );
			     		valXmlStr.append("<rate><![CDATA[" ).append(String.format ("%.2f", rate2)).append( "]]></rate>\r\n" );
			     		valXmlStr.append("<discount_type><![CDATA[" ).append(discountType).append( "]]></discount_type>\r\n" );
			     		valXmlStr.append("<discount><![CDATA[" ).append(String.format ("%.2f", discount2)).append( "]]></discount>\r\n" );
			     		valXmlStr.append("<gross_rate><![CDATA[" ).append(String.format ("%.2f", grossRate2)).append( "]]></gross_rate>\r\n" );
			     		valXmlStr.append("<comm_perc><![CDATA[" ).append(String.format ("%.2f", comm)).append( "]]></comm_perc>\r\n" );
			     		valXmlStr.append("<comm_per_unit><![CDATA[" ).append(String.format ("%.2f",commPerUnit2)).append( "]]></comm_per_unit>\r\n" );
			     		valXmlStr.append("<rate__new><![CDATA[" ).append(Math.round(grossRate2)).append( "]]></rate__new>\r\n" );
			     		valXmlStr.append("<basic_dist_price><![CDATA[" ).append(getCommaSep(newCostRate2)).append( "]]></basic_dist_price>\r\n" );
			     		valXmlStr.append("<cost_rate><![CDATA[" ).append( String.format("%.2f", costRate2)).append( "]]></cost_rate>\r\n" );
			     		valXmlStr.append("<profit><![CDATA[" ).append( getCommaSep(profit2) ).append( "]]></profit>\r\n" );
			     		valXmlStr.append("<profit_on_goods><![CDATA[" ).append( getCommaSep( profitOnGoods2 ) ).append( "]]></profit_on_goods>\r\n" );
			     		valXmlStr.append("<final_price><![CDATA[" ).append( getCommaSep( finalPrice2 ) ).append( "]]></final_price	>\r\n" );
			     		valXmlStr.append("<perc_profit><![CDATA[" ).append( getCommaSep( percProfit2 )  ).append( "]]></perc_profit>\r\n" );
			     		valXmlStr.append("<available_stock><![CDATA[" ).append( getCommaSep( availblStock ) ).append( "]]></available_stock>\r\n" );
			     		valXmlStr.append("</Detail2>");
			     		currDomStr = currDomStr.replace("</Detail2>",valXmlStr.toString());
			     		
			     		valueXmlString.append(currDomStr);
			     		
					}
					catch(Exception e)
					{
						System.out.println("@@@@ Exception ON ITEMCHANGE ==> ITEM_CODE : "+e);
					}
					
					
					
				}
				else if(currentColumn.trim().equalsIgnoreCase("qty_per_month"))
				{
				
					String currDomStr = genericUtility.serializeDom(dom);
					StringBuffer valueXmlStr = new StringBuffer();
					try
					{
						qtyMonth = checkNull(genericUtility.getColumnValue("qty_per_month",dom));
						rate = checkNull(genericUtility.getColumnValue("rate",dom));
						discount = checkNull(genericUtility.getColumnValue("discount",dom));
						grossRate = checkNull(genericUtility.getColumnValue("gross_rate",dom));
						commission = checkNull(genericUtility.getColumnValue("comm_perc",dom));
						commissionPerUnit = checkNull(genericUtility.getColumnValue("comm_per_unit",dom));
						
						newCostRate = (getDoubleValue(checkNull(genericUtility.getColumnValue("rate__new",dom))));
						profit = (getDoubleValue(checkNull(genericUtility.getColumnValue("profit",dom))));
						newGrossRate = (getDoubleValue(grossRate));
						String profitParse = String.format("%.2f", profit);
						
						profitOnGoods = (getDoubleValue(profitParse) * getDoubleValue(qtyMonth));
						finalPrice = (newCostRate * getDoubleValue(qtyMonth));
						percProfit = ((profitOnGoods / finalPrice) * 100);
						
						if( percProfit.isNaN() || percProfit.isInfinite()) 
						{
							percProfit = 0.0;
						}
						
						valueXmlStr.append("<quot_no>").append("<![CDATA["+ checkNull(genericUtility.getColumnValue("quot_no",dom)) +"]]>").append("</quot_no>");
						valueXmlStr.append("<line_no>").append("<![CDATA["+  checkNull(genericUtility.getColumnValue("line_no",dom)) +"]]>").append("</line_no>");
						valueXmlStr.append("<qty_per_month><![CDATA[" ).append( qtyMonth).append( "]]></qty_per_month>\r\n" );
						
						valueXmlStr.append("<profit><![CDATA[" ).append( getCommaSep(profit)).append( "]]></profit>\r\n" );
						valueXmlStr.append("<profit_on_goods><![CDATA[" ).append( getCommaSep(profitOnGoods) ).append( "]]></profit_on_goods>\r\n" );
						valueXmlStr.append("<final_price><![CDATA[" ).append( getCommaSep(finalPrice) ).append( "]]></final_price>\r\n" );
						valueXmlStr.append("<perc_profit><![CDATA[" ).append( getCommaSep(percProfit) ).append( "]]></perc_profit>\r\n" );
						valueXmlStr.append("</Detail2>");
						currDomStr = currDomStr.replace("</Detail2>", valueXmlStr.toString());
						
						valueXmlString.append(currDomStr);
						
					}
					catch (Exception e)
					{
						System.out.println("@@@@ Exception ON ITEMCHANGE ==> qty_per_month :"+e);
					}
				}
				/*else if(currentColumn.trim().equalsIgnoreCase("rate")) 
				{
					//Require system entries.
					
		            custCode = checkNull(genericUtility.getColumnValue("cust_code",dom2));
		            String itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
		            qtyMonth = checkNull(genericUtility.getColumnValue("qty_per_month",dom));
					rate = checkNull(genericUtility.getColumnValue("rate",dom));
					discount = checkNull(genericUtility.getColumnValue("discount",dom));
					grossRate = checkNull(genericUtility.getColumnValue("gross_rate",dom));
					commission = checkNull(genericUtility.getColumnValue("comm_perc",dom));
					commissionPerUnit = checkNull(genericUtility.getColumnValue("comm_per_unit",dom));
		     		
		     		Double grossRate2 = ((getDoubleValue(rate) - ((getDoubleValue(rate) * getDoubleValue(discount)) / 100)));
		     		if(grossRate2.isNaN() || grossRate2.isInfinite())
		     		{
		     			grossRate2 = 0.0;
		     		}
		     		double comm = 0;
		     		try
		     		{	String commTable = getCommission(custCode, conn);
		     			comm = getCommision(commTable,currDate,conn,itemCode);
		     		}
		     		catch(Exception e) 
		     		{
		     			comm = 0;
		     		}
		     		
		     		double commPerUnit2 = (((grossRate2 * comm) / 100));
		     		double newCostRate2 = ((grossRate2 - commPerUnit2));
		     		double costRate2 = getCostRate(conn, itemCode);
		     		double availblStock = getStockDetails(conn,itemCode);
		     		double profit2 = (newCostRate2 - costRate2);
		     		String profitParse = String.format("%.2f", profit);
					double profitOnGoods2 = (Double.parseDouble(profitParse) * getDoubleValue(qtyMonth));
		     		double finalPrice2 = (grossRate2 * getDoubleValue(qtyMonth));
		     		Double percProfit2 = ((profitOnGoods2 / finalPrice2) * 100);

		     		if(percProfit2.isNaN() || percProfit2.isInfinite())
		     		{
		     			percProfit2 = 0.0;
		     		}

				}*/
				else if(currentColumn.trim().equalsIgnoreCase("discount"))
				{
					try
					{
						String currDomStr = genericUtility.serializeDom(dom);
						StringBuffer valueXmlStr = new StringBuffer();
						qtyMonth = checkNull(genericUtility.getColumnValue("qty_per_month",dom));
						rate = checkNull(genericUtility.getColumnValue("rate",dom));
						discount = checkNull(genericUtility.getColumnValue("discount",dom));
						grossRate = checkNull(genericUtility.getColumnValue("gross_rate",dom));
						commission = checkNull(genericUtility.getColumnValue("comm_perc",dom));
						commissionPerUnit = checkNull(genericUtility.getColumnValue("comm_per_unit",dom));
						
						 
						costRate = getDoubleValue(checkNull(genericUtility.getColumnValue("cost_rate",dom)));
						discountType = checkNull(genericUtility.getColumnValue("discount_type",dom));
						System.out.println("#### discountType "+discountType);
						if(discountType != null && discountType.equalsIgnoreCase("FIX"))
						{
							newGrossRate = (getDoubleValue(rate) -getDoubleValue(discount)); 
						}
						else
						{
							newGrossRate = (((getDoubleValue(rate) - ((getDoubleValue(rate) * getDoubleValue(discount)) / 100))));	
						}
						
						if( newGrossRate.isNaN() || newGrossRate.isInfinite()) 
						{
							newGrossRate = 0.0;
						}
						
						commPerUnit = (((newGrossRate * getDoubleValue(commission)) / 100));
						if( commPerUnit.isNaN() || commPerUnit.isInfinite()) 
						{
							commPerUnit = 0.0;
						}
						newCostRate = ((newGrossRate - commPerUnit));
						profit = (newCostRate - costRate);
						String profitParse = String.format("%.2f", profit);
						profitOnGoods = (getDoubleValue(profitParse) * getDoubleValue(qtyMonth));
						finalPrice = (newGrossRate * getDoubleValue(qtyMonth));
						percProfit = ((profitOnGoods / finalPrice) * 100);
						
						if( percProfit.isNaN() || percProfit.isInfinite()) 
						{
							percProfit = 0.0;
						}
						
						valueXmlStr.append("<quot_no>").append("<![CDATA["+ checkNull(genericUtility.getColumnValue("quot_no",dom)) +"]]>").append("</quot_no>");
						valueXmlStr.append("<line_no>").append("<![CDATA["+  checkNull(genericUtility.getColumnValue("line_no",dom)) +"]]>").append("</line_no>");
						valueXmlStr.append("<discount><![CDATA[" ).append(String.format ("%.2f", Double.parseDouble(discount))).append( "]]></discount>\r\n" );
						valueXmlStr.append("<gross_rate><![CDATA[" ).append(String.format ("%.2f", newGrossRate)).append( "]]></gross_rate>\r\n" );
						valueXmlStr.append("<comm_per_unit><![CDATA[" ).append(String.format ("%.2f", commPerUnit)).append( "]]></comm_per_unit>\r\n" );
						valueXmlStr.append("<rate__new><![CDATA[" ).append(Math.round(newGrossRate)).append( "]]></rate__new>\r\n" );
						
						valueXmlStr.append("<basic_dist_price><![CDATA[" ).append( getCommaSep(newCostRate) ).append( "]]></basic_dist_price>\r\n" );
						valueXmlStr.append("<cost_rate><![CDATA[" ).append(String.format("%.2f",costRate)).append( "]]></cost_rate>\r\n" );
						
						valueXmlStr.append("<profit><![CDATA[" ).append( getCommaSep(profit)).append( "]]></profit>\r\n" );
						valueXmlStr.append("<profit_on_goods><![CDATA[" ).append( getCommaSep(profitOnGoods) ).append( "]]></profit_on_goods>\r\n" );
						valueXmlStr.append("<final_price><![CDATA[" ).append( getCommaSep(finalPrice) ).append( "]]></final_price>\r\n" );
						valueXmlStr.append("<perc_profit><![CDATA[" ).append( getCommaSep(percProfit) ).append( "]]></perc_profit>\r\n" );
						valueXmlStr.append("</Detail2>");
						currDomStr = currDomStr.replace("</Detail2>", valueXmlStr.toString());
						
						valueXmlString.append(currDomStr);
						
					}
					catch (Exception e)
					{
						System.out.println("@@@@ Exception ON ITEMCHANGE ==> discount :"+e);
					}
				}
				else if(currentColumn.trim().equalsIgnoreCase("rate__new"))
				{
					try
					{
						StringBuffer valueXmlStr = new StringBuffer();
						String currDomStr = genericUtility.serializeDom(dom);
						
						newGrossRate = getDoubleValue(checkNull(genericUtility.getColumnValue("rate__new",dom)));
						commission = checkNull(genericUtility.getColumnValue("comm_perc",dom));
						qtyMonth = checkNull(genericUtility.getColumnValue("qty_per_month",dom));
						
						commPerUnit = (((newGrossRate * getDoubleValue(commission)) / 100));
						if(commPerUnit.isNaN() || commPerUnit.isInfinite())
						{
							commPerUnit = 0.0;
						}
						
						newCostRate = ((newGrossRate - commPerUnit));
						profit = (newCostRate - costRate);
						
						String profitParse = String.format("%.2f", profit);
						profitOnGoods = (getDoubleValue(profitParse) * getDoubleValue(qtyMonth));
						finalPrice = (newGrossRate * getDoubleValue(qtyMonth));
						percProfit = ((profitOnGoods / finalPrice) * 100);
						
						if(percProfit.isNaN() || percProfit.isInfinite())
						{
							percProfit = 0.0;
						}
						
						valueXmlStr.append("<comm_per_unit><![CDATA[" ).append(String.format ("%.2f", commPerUnit)).append( "]]></comm_per_unit>\r\n" );
						valueXmlStr.append("<rate__new><![CDATA[" ).append(Math.round(newGrossRate)).append( "]]></rate__new>\r\n" );
						valueXmlStr.append("<basic_dist_price><![CDATA[" ).append( getCommaSep(newCostRate)).append( "]]></basic_dist_price>\r\n" );
						valueXmlStr.append("<profit><![CDATA[" ).append( getCommaSep(profit)).append( "]]></profit>\r\n" );
						valueXmlStr.append("<profit_on_goods><![CDATA[" ).append( getCommaSep(profitOnGoods) ).append( "]]></profit_on_goods>\r\n" );
						valueXmlStr.append("<final_price><![CDATA[" ).append( getCommaSep(finalPrice) ).append( "]]></final_price>\r\n" );
						valueXmlStr.append("<perc_profit><![CDATA[" ).append( getCommaSep(percProfit) ).append( "]]></perc_profit>\r\n" );
						valueXmlStr.append("</Detail2>");
						currDomStr = currDomStr.replace("</Detail2>", valueXmlStr.toString());
						valueXmlString.append(currDomStr);
						
					}
					catch(Exception e)
					{
						System.out.println("@@@@ Exception ON ITEMCHANGE ==> New Rate :"+e);
					}
					
				}
				else if(currentColumn.trim().equalsIgnoreCase("comm_perc"))
				{
					try
					{
						
						String currDomStr = genericUtility.serializeDom(dom);
						StringBuffer valueXmlStr = new StringBuffer();
						qtyMonth = checkNull(genericUtility.getColumnValue("qty_per_month",dom));
						rate = checkNull(genericUtility.getColumnValue("rate",dom));
						discount = checkNull(genericUtility.getColumnValue("discount",dom));
						grossRate = checkNull(genericUtility.getColumnValue("gross_rate",dom));
						commission = checkNull(genericUtility.getColumnValue("comm_perc",dom));
						commissionPerUnit = checkNull(genericUtility.getColumnValue("comm_per_unit",dom));
						costRate = getDoubleValue(checkNull(genericUtility.getColumnValue("cost_rate",dom)));
						discountType = checkNull(genericUtility.getColumnValue("discount_type",dom));
						
						if(discountType != null && discountType.equalsIgnoreCase("FIX"))
						{
							newGrossRate = (getDoubleValue(rate) -getDoubleValue(discount)); 
						}
						else
						{
							newGrossRate = (((getDoubleValue(rate) - ((getDoubleValue(rate) * getDoubleValue(discount)) / 100))));	
						}
						
						if(newGrossRate.isNaN() || newGrossRate.isInfinite())
						{
							newGrossRate = 0.0;
						}
						
						commPerUnit = (((newGrossRate * getDoubleValue(commission)) / 100));
						
						if(commPerUnit.isNaN() || commPerUnit.isInfinite())
						{
							commPerUnit = 0.0;
						}
						
						newCostRate = ((newGrossRate - commPerUnit));
						profit = (newCostRate - costRate);
						String profitParse = String.format("%.2f", profit);
						profitOnGoods = (getDoubleValue(profitParse) * getDoubleValue(qtyMonth));
						finalPrice = (newGrossRate * getDoubleValue(qtyMonth));
						percProfit = ((profitOnGoods / finalPrice) * 100);
						
						if(percProfit.isNaN() || percProfit.isInfinite())
						{
							percProfit = 0.0;
						}

						valueXmlStr.append("<quot_no>").append("<![CDATA["+ checkNull(genericUtility.getColumnValue("quot_no",dom)) +"]]>").append("</quot_no>");
						valueXmlStr.append("<line_no>").append("<![CDATA["+  checkNull(genericUtility.getColumnValue("line_no",dom)) +"]]>").append("</line_no>");
						valueXmlStr.append("<discount><![CDATA[" ).append(String.format ("%.2f", Double.parseDouble(discount))).append( "]]></discount>\r\n" );
						valueXmlStr.append("<gross_rate><![CDATA[" ).append(String.format ("%.2f", newGrossRate)).append( "]]></gross_rate>\r\n" );
						valueXmlStr.append("<comm_per_unit><![CDATA[" ).append(String.format ("%.2f", commPerUnit)).append( "]]></comm_per_unit>\r\n" );
						valueXmlStr.append("<rate__new><![CDATA[" ).append(Math.round(newGrossRate)).append( "]]></rate__new>\r\n" );
						valueXmlStr.append("<basic_dist_price><![CDATA[" ).append( getCommaSep(newCostRate)).append( "]]></basic_dist_price>\r\n" );
						valueXmlStr.append("<cost_rate><![CDATA[" ).append(String.format("%.2f", costRate)).append( "]]></cost_rate>\r\n" );
						valueXmlStr.append("<profit><![CDATA[" ).append( getCommaSep(profit)).append( "]]></profit>\r\n" );
						valueXmlStr.append("<profit_on_goods><![CDATA[" ).append( getCommaSep(profitOnGoods) ).append( "]]></profit_on_goods>\r\n" );
						valueXmlStr.append("<final_price><![CDATA[" ).append( getCommaSep(finalPrice) ).append( "]]></final_price>\r\n" );
						valueXmlStr.append("<perc_profit><![CDATA[" ).append( getCommaSep(percProfit) ).append( "]]></perc_profit>\r\n" );
						valueXmlStr.append("</Detail2>");
						currDomStr = currDomStr.replace("</Detail2>", valueXmlStr.toString());
						
						valueXmlString.append(currDomStr);
						
					} 
					catch (Exception e)
					{
						System.out.println("@@@@ Exception ON ITEMCHANGE ==> comm_perc :"+e);
					}
				}
				//valueXmlString.append("</Root>");//It is for Final return.
			}
			
			else if("sale_quotitems".equals(formName) || "3".equalsIgnoreCase(objContext))
			{
				if(currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					try
					{
						parentNodeList = dom2.getElementsByTagName("Detail2");
						
						parentNode = parentNodeList.item(0);
						childNodeList = parentNode.getChildNodes();
						int childNodeListLength = childNodeList.getLength();
						
						int parentNodeListLen = parentNodeList.getLength();
						String childNodeName = null;
						String xmlString = "";
						int ctr=0;
						Element parentElem = null;
						int check = 0;
						for ( int rowCnt=0; rowCnt < parentNodeListLen; rowCnt++ )
						{
							parentNode = parentNodeList.item(rowCnt);
							parentElem = (Element)parentNode;
							if(parentElem.getElementsByTagName("selectbox").item(0) != null)
							{
								if((parentElem.getElementsByTagName("selectbox").item(0).getFirstChild().getNodeValue()).equals("true"))
								{
									ctr++;
									globalNo++;
									System.out.println("#### ITM_DEFAULT_GLOBAL_NO "+globalNo);
									xmlString = serializeDom(parentNode);
									String str11 = xmlString.substring(xmlString.indexOf("/>")+2);
									StringBuffer buffer = new StringBuffer(str11.substring(0, str11.indexOf("</Detail2>")));
									buffer.append( "<line_no protect='1'><![CDATA[" ).append(ctr).append( "]]></line_no>\r\n" );
									buffer.append("<sr_no><![CDATA[" ).append(ctr).append( "]]></sr_no>\r\n</Detail3>" );
									str11 = buffer.toString();
					
									valueXmlString.append("<Detail3 domID=\""+ctr+"\" selected=\"Y\" > \r\n");
						     		valueXmlString.append("<attribute  selected=\"Y\" updateFlag=\"A\"  status=\"N\" pkNames=\"\" />\r\n");
									valueXmlString.append(str11);
								}
							}
						}
					
					} catch (Exception e) 
					{
						System.out.println("@@@@ Exception ON ITEMCHANGE ==> itm_default :"+e);
					}
				}
				
				else if(currentColumn.trim().equalsIgnoreCase("discount"))
				{
					try
					{
						String currDomStr = genericUtility.serializeDom(dom);
						StringBuffer valueXmlStr = new StringBuffer();
						qtyMonth = checkNull(genericUtility.getColumnValue("qty_per_month",dom));
						rate = checkNull(genericUtility.getColumnValue("rate",dom));
						discount = checkNull(genericUtility.getColumnValue("discount",dom));
						grossRate = checkNull(genericUtility.getColumnValue("gross_rate",dom));
						commission = checkNull(genericUtility.getColumnValue("comm_perc",dom));
						commissionPerUnit = checkNull(genericUtility.getColumnValue("comm_per_unit",dom));
						
						//Changed on 21-08-18 
						costRate = getDoubleValue(checkNull(genericUtility.getColumnValue("cost_rate",dom)));
						discountType = checkNull(genericUtility.getColumnValue("discount_type",dom));
						System.out.println("#### discountType "+discountType);
						if(discountType != null && discountType.equalsIgnoreCase("FIX"))
						{
							newGrossRate = (getDoubleValue(rate) -getDoubleValue(discount)); 
						}
						else
						{
							newGrossRate = (((getDoubleValue(rate) - ((getDoubleValue(rate) * getDoubleValue(discount)) / 100))));	
						}
						
						if( newGrossRate.isNaN() || newGrossRate.isInfinite()) 
						{
							newGrossRate = 0.0;
						}
						
						commPerUnit = (((newGrossRate * getDoubleValue(commission)) / 100));
						if( commPerUnit.isNaN() || commPerUnit.isInfinite()) 
						{
							commPerUnit = 0.0;
						}
						newCostRate = ((newGrossRate - commPerUnit));
						profit = (newCostRate - costRate);
						String profitParse = String.format("%.2f", profit);
						profitOnGoods = (getDoubleValue(profitParse) * getDoubleValue(qtyMonth));
						finalPrice = (newGrossRate * getDoubleValue(qtyMonth));
						percProfit = ((profitOnGoods / finalPrice) * 100);
						
						if( percProfit.isNaN() || percProfit.isInfinite()) 
						{
							percProfit = 0.0;
						}
						
						valueXmlStr.append("<quot_no>").append("<![CDATA["+ checkNull(genericUtility.getColumnValue("quot_no",dom)) +"]]>").append("</quot_no>");
						valueXmlStr.append("<line_no>").append("<![CDATA["+  checkNull(genericUtility.getColumnValue("line_no",dom)) +"]]>").append("</line_no>");
						valueXmlStr.append("<discount><![CDATA[" ).append(String.format ("%.2f", Double.parseDouble(discount))).append( "]]></discount>\r\n" );
						valueXmlStr.append("<gross_rate><![CDATA[" ).append(String.format ("%.2f", newGrossRate)).append( "]]></gross_rate>\r\n" );
						valueXmlStr.append("<comm_per_unit><![CDATA[" ).append(String.format ("%.2f", commPerUnit)).append( "]]></comm_per_unit>\r\n" );
						valueXmlStr.append("<rate__new><![CDATA[" ).append(Math.round(newGrossRate)).append( "]]></rate__new>\r\n" );
						//valueXmlStr.append("<basic_dist_price><![CDATA[" ).append(String.format ("%.2f", newCostRate)).append( "]]></basic_dist_price>\r\n" );
						valueXmlStr.append("<basic_dist_price><![CDATA[" ).append( getCommaSep(newCostRate) ).append( "]]></basic_dist_price>\r\n" );
						valueXmlStr.append("<cost_rate><![CDATA[" ).append(String.format ("%.2f", costRate)).append( "]]></cost_rate>\r\n" );
						
						/*valueXmlStr.append("<profit><![CDATA[" ).append(String.format ("%.2f", profit)).append( "]]></profit>\r\n" );
						valueXmlStr.append("<profit_on_goods><![CDATA[" ).append(String.format ("%.2f", profitOnGoods)).append( "]]></profit_on_goods>\r\n" );
						valueXmlStr.append("<final_price><![CDATA[" ).append(String.format ("%.2f", finalPrice)).append( "]]></final_price>\r\n" );
						valueXmlStr.append("<perc_profit><![CDATA[" ).append(String.format ("%.2f", percProfit)).append( "]]></perc_profit>\r\n" );*/
						valueXmlStr.append("<profit><![CDATA[" ).append( getCommaSep(profit)).append( "]]></profit>\r\n" );
						valueXmlStr.append("<profit_on_goods><![CDATA[" ).append( getCommaSep(profitOnGoods) ).append( "]]></profit_on_goods>\r\n" );
						valueXmlStr.append("<final_price><![CDATA[" ).append( getCommaSep(finalPrice) ).append( "]]></final_price>\r\n" );
						valueXmlStr.append("<perc_profit><![CDATA[" ).append( getCommaSep(percProfit) ).append( "]]></perc_profit>\r\n" );
						valueXmlStr.append("</Detail3>");
						currDomStr = currDomStr.replace("</Detail3>", valueXmlStr.toString());
						//valueXmlStr.append("</Root>");
						//currDomStr = currDomStr.replace("</Detail3>", valueXmlStr.toString() + "</Detail3>");
						valueXmlString.append(currDomStr);
						
					}
					catch (Exception e)
					{
						System.out.println("@@@@ Exception ON ITEMCHANGE ==> discount :"+e);
					}
				}
				else if(currentColumn.trim().equalsIgnoreCase("qty_per_month"))
				{
				
					String currDomStr = genericUtility.serializeDom(dom);
					StringBuffer valueXmlStr = new StringBuffer();
					try
					{
						qtyMonth = checkNull(genericUtility.getColumnValue("qty_per_month",dom));
						rate = checkNull(genericUtility.getColumnValue("rate",dom));
						discount = checkNull(genericUtility.getColumnValue("discount",dom));
						grossRate = checkNull(genericUtility.getColumnValue("gross_rate",dom));
						commission = checkNull(genericUtility.getColumnValue("comm_perc",dom));
						commissionPerUnit = checkNull(genericUtility.getColumnValue("comm_per_unit",dom));
						
						newCostRate = (getDoubleValue(checkNull(genericUtility.getColumnValue("rate__new",dom))));
						profit = (getDoubleValue(checkNull(genericUtility.getColumnValue("profit",dom))));
						newGrossRate = (getDoubleValue(grossRate));
						String profitParse = String.format("%.2f", profit);
						
						profitOnGoods = (getDoubleValue(profitParse) * getDoubleValue(qtyMonth));
						finalPrice = (newCostRate * getDoubleValue(qtyMonth));
						percProfit = ((profitOnGoods / finalPrice) * 100);
						
						if( percProfit.isNaN() || percProfit.isInfinite()) 
						{
							percProfit = 0.0;
						}
						
						valueXmlStr.append("<quot_no>").append("<![CDATA["+ checkNull(genericUtility.getColumnValue("quot_no",dom)) +"]]>").append("</quot_no>");
						valueXmlStr.append("<line_no>").append("<![CDATA["+  checkNull(genericUtility.getColumnValue("line_no",dom)) +"]]>").append("</line_no>");
						valueXmlStr.append("<qty_per_month><![CDATA[" ).append( qtyMonth).append( "]]></qty_per_month>\r\n" );
						/*valueXmlStr.append("<profit><![CDATA[" ).append(String.format ("%.2f", profit)).append( "]]></profit>\r\n" );
						valueXmlStr.append("<profit_on_goods><![CDATA[" ).append(String.format ("%.2f", profitOnGoods)).append( "]]></profit_on_goods>\r\n" );
						valueXmlStr.append("<final_price><![CDATA[" ).append(String.format ("%.2f", finalPrice)).append( "]]></final_price>\r\n" );
						valueXmlStr.append("<perc_profit><![CDATA[" ).append(String.format ("%.2f", percProfit)).append( "]]></perc_profit>\r\n" );*/
						valueXmlStr.append("<profit><![CDATA[" ).append( getCommaSep(profit)).append( "]]></profit>\r\n" );
						valueXmlStr.append("<profit_on_goods><![CDATA[" ).append( getCommaSep(profitOnGoods) ).append( "]]></profit_on_goods>\r\n" );
						valueXmlStr.append("<final_price><![CDATA[" ).append( getCommaSep(finalPrice) ).append( "]]></final_price>\r\n" );
						valueXmlStr.append("<perc_profit><![CDATA[" ).append( getCommaSep(percProfit) ).append( "]]></perc_profit>\r\n" );
						valueXmlStr.append("</Detail3>");
						currDomStr = currDomStr.replace("</Detail3>", valueXmlStr.toString());
						//valueXmlStr.append("</Root>");
						//currDomStr = currDomStr.replace("</Detail3>", valueXmlStr.toString() + "</Detail3>");
						valueXmlString.append(currDomStr);
						
					}
					catch (Exception e)
					{
						System.out.println("@@@@ Exception ON ITEMCHANGE ==> qty_per_month :"+e);
					}
				}
				else if(currentColumn.trim().equalsIgnoreCase("comm_perc"))
				{
					try
					{
						
						String currDomStr = genericUtility.serializeDom(dom);
						StringBuffer valueXmlStr = new StringBuffer();
						qtyMonth = checkNull(genericUtility.getColumnValue("qty_per_month",dom));
						rate = checkNull(genericUtility.getColumnValue("rate",dom));
						discount = checkNull(genericUtility.getColumnValue("discount",dom));
						grossRate = checkNull(genericUtility.getColumnValue("gross_rate",dom));
						commission = checkNull(genericUtility.getColumnValue("comm_perc",dom));
						commissionPerUnit = checkNull(genericUtility.getColumnValue("comm_per_unit",dom));
						costRate = getDoubleValue(checkNull(genericUtility.getColumnValue("cost_rate",dom)));
						discountType = checkNull(genericUtility.getColumnValue("discount_type",dom));
						
						if(discountType != null && discountType.equalsIgnoreCase("FIX"))
						{
							newGrossRate = (getDoubleValue(rate) -getDoubleValue(discount)); 
						}
						else
						{
							newGrossRate = (((getDoubleValue(rate) - ((getDoubleValue(rate) * getDoubleValue(discount)) / 100))));	
						}
						
						if(newGrossRate.isNaN() || newGrossRate.isInfinite())
						{
							newGrossRate = 0.0;
						}
						
						commPerUnit = (((newGrossRate * getDoubleValue(commission)) / 100));
						
						if(commPerUnit.isNaN() || commPerUnit.isInfinite())
						{
							commPerUnit = 0.0;
						}
						
						newCostRate = ((newGrossRate - commPerUnit));
						profit = (newCostRate - costRate);
						String profitParse = String.format("%.2f", profit);
						profitOnGoods = (getDoubleValue(profitParse) * getDoubleValue(qtyMonth));
						finalPrice = (newGrossRate * getDoubleValue(qtyMonth));
						percProfit = ((profitOnGoods / finalPrice) * 100);
						
						if(percProfit.isNaN() || percProfit.isInfinite())
						{
							percProfit = 0.0;
						}

						valueXmlStr.append("<quot_no>").append("<![CDATA["+ checkNull(genericUtility.getColumnValue("quot_no",dom)) +"]]>").append("</quot_no>");
						valueXmlStr.append("<line_no>").append("<![CDATA["+  checkNull(genericUtility.getColumnValue("line_no",dom)) +"]]>").append("</line_no>");
						valueXmlStr.append("<discount><![CDATA[" ).append(String.format ("%.2f", Double.parseDouble(discount))).append( "]]></discount>\r\n" );
						valueXmlStr.append("<gross_rate><![CDATA[" ).append(String.format ("%.2f", newGrossRate)).append( "]]></gross_rate>\r\n" );
						valueXmlStr.append("<comm_per_unit><![CDATA[" ).append(String.format ("%.2f", commPerUnit)).append( "]]></comm_per_unit>\r\n" );
						valueXmlStr.append("<rate__new><![CDATA[" ).append(Math.round(newGrossRate)).append( "]]></rate__new>\r\n" );
						//valueXmlStr.append("<basic_dist_price><![CDATA[" ).append(String.format ("%.2f", newCostRate)).append( "]]></basic_dist_price>\r\n" );
						valueXmlStr.append("<basic_dist_price><![CDATA[" ).append( getCommaSep(newCostRate)).append( "]]></basic_dist_price>\r\n" );
						valueXmlStr.append("<cost_rate><![CDATA[" ).append(String.format ("%.2f", costRate)).append( "]]></cost_rate>\r\n" );
						
						/*valueXmlStr.append("<profit><![CDATA[" ).append(String.format ("%.2f", profit)).append( "]]></profit>\r\n" );
						valueXmlStr.append("<profit_on_goods><![CDATA[" ).append(String.format ("%.2f", profitOnGoods)).append( "]]></profit_on_goods>\r\n" );
						valueXmlStr.append("<final_price><![CDATA[" ).append(String.format ("%.2f", finalPrice)).append( "]]></final_price>\r\n" );
						valueXmlStr.append("<perc_profit><![CDATA[" ).append(String.format ("%.2f", percProfit)).append( "]]></perc_profit>\r\n" );*/
						valueXmlStr.append("<profit><![CDATA[" ).append( getCommaSep(profit)).append( "]]></profit>\r\n" );
						valueXmlStr.append("<profit_on_goods><![CDATA[" ).append( getCommaSep(profitOnGoods) ).append( "]]></profit_on_goods>\r\n" );
						valueXmlStr.append("<final_price><![CDATA[" ).append( getCommaSep(finalPrice) ).append( "]]></final_price>\r\n" );
						valueXmlStr.append("<perc_profit><![CDATA[" ).append( getCommaSep(percProfit) ).append( "]]></perc_profit>\r\n" );
						valueXmlStr.append("</Detail3>");
						currDomStr = currDomStr.replace("</Detail3>", valueXmlStr.toString());
						//valueXmlStr.append("</Root>");
						//currDomStr = currDomStr.replace("</Detail3>", valueXmlStr.toString() + "</Detail3>");
						valueXmlString.append(currDomStr);
						
					} 
					catch (Exception e)
					{
						System.out.println("@@@@ Exception ON ITEMCHANGE ==> comm_perc :"+e);
					}
				}
				else if(currentColumn.trim().equalsIgnoreCase("rate__new"))
				{
					try
					{
						StringBuffer valueXmlStr = new StringBuffer();
						String currDomStr = genericUtility.serializeDom(dom);
						
						newGrossRate = getDoubleValue(checkNull(genericUtility.getColumnValue("rate__new",dom)));
						commission = checkNull(genericUtility.getColumnValue("comm_perc",dom));
						qtyMonth = checkNull(genericUtility.getColumnValue("qty_per_month",dom));
						
						commPerUnit = (((newGrossRate * getDoubleValue(commission)) / 100));
						if(commPerUnit.isNaN() || commPerUnit.isInfinite())
						{
							commPerUnit = 0.0;
						}
						
						newCostRate = ((newGrossRate - commPerUnit));
						profit = (newCostRate - costRate);
						
						String profitParse = String.format("%.2f", profit);
						profitOnGoods = (getDoubleValue(profitParse) * getDoubleValue(qtyMonth));
						finalPrice = (newGrossRate * getDoubleValue(qtyMonth));
						percProfit = ((profitOnGoods / finalPrice) * 100);
						
						if(percProfit.isNaN() || percProfit.isInfinite())
						{
							percProfit = 0.0;
						}
						
						valueXmlStr.append("<comm_per_unit><![CDATA[" ).append(String.format ("%.2f", commPerUnit)).append( "]]></comm_per_unit>\r\n" );
						valueXmlStr.append("<rate__new><![CDATA[" ).append(Math.round(newGrossRate)).append( "]]></rate__new>\r\n" );
						valueXmlStr.append("<basic_dist_price><![CDATA[" ).append( getCommaSep(newCostRate)).append( "]]></basic_dist_price>\r\n" );
						valueXmlStr.append("<profit><![CDATA[" ).append( getCommaSep(profit)).append( "]]></profit>\r\n" );
						valueXmlStr.append("<profit_on_goods><![CDATA[" ).append( getCommaSep(profitOnGoods) ).append( "]]></profit_on_goods>\r\n" );
						valueXmlStr.append("<final_price><![CDATA[" ).append( getCommaSep(finalPrice) ).append( "]]></final_price>\r\n" );
						valueXmlStr.append("<perc_profit><![CDATA[" ).append( getCommaSep(percProfit) ).append( "]]></perc_profit>\r\n" );
						/*valueXmlStr.append("<basic_dist_price><![CDATA[" ).append(String.format ("%.2f", newCostRate)).append( "]]></basic_dist_price>\r\n" );
						valueXmlStr.append("<profit><![CDATA[" ).append(String.format ("%.2f", profit)).append( "]]></profit>\r\n" );
						valueXmlStr.append("<profit_on_goods><![CDATA[" ).append(String.format ("%.2f", profitOnGoods)).append( "]]></profit_on_goods>\r\n" );
						valueXmlStr.append("<final_price><![CDATA[" ).append(String.format ("%.2f", finalPrice)).append( "]]></final_price>\r\n" );
						valueXmlStr.append("<perc_profit><![CDATA[" ).append(String.format ("%.2f", percProfit)).append( "]]></perc_profit>\r\n" );*/
						valueXmlStr.append("</Detail3>");
						currDomStr = currDomStr.replace("</Detail3>", valueXmlStr.toString());
						//valueXmlStr.append("</Root>");
						//currDomStr = currDomStr.replace("</Detail3>", valueXmlStr.toString() + "</Detail3>");
						valueXmlString.append(currDomStr);
						
					}
					catch(Exception e)
					{
						System.out.println("@@@@ Exception ON ITEMCHANGE ==> New Rate :"+e);
					}
					
				}
				//Added by AMOL START
				else if(currentColumn.trim().equalsIgnoreCase("itm_default_add"))
				{
					try
					{
						globalNo = globalNo +1;
						
						String newSrNo = getSrNo(dom2);
						System.out.println("#### New SrNo "+newSrNo); 
						valueXmlString.append("<Detail3 domID=\""+newSrNo+"\" selected=\"N\" > \r\n");
			     		valueXmlString.append("<attribute  selected=\"N\" updateFlag=\"A\"  status=\"N\" pkNames=\"\" />\r\n");
			     		valueXmlString.append("<quot_no/>");
			     		valueXmlString.append( "<line_no protect='1'><![CDATA[" ).append(newSrNo).append( "]]></line_no>\r\n" );
			     		valueXmlString.append("<sr_no><![CDATA[" ).append(newSrNo).append( "]]></sr_no>\r\n" );
			     		valueXmlString.append("<item_code><![CDATA[" ).append("").append( "]]></item_code>\r\n" );
			     		valueXmlString.append("<item_descr><![CDATA[" ).append("").append( "]]></item_descr>\r\n" );
			     		valueXmlString.append("<unit><![CDATA[" ).append("").append( "]]></unit>\r\n" );
			     		valueXmlString.append("<qty_per_month><![CDATA[" ).append("").append( "]]></qty_per_month>\r\n" );
			     		valueXmlString.append("<rate><![CDATA[" ).append("").append( "]]></rate>\r\n" );
			     		valueXmlString.append("<discount><![CDATA[" ).append("").append( "]]></discount>\r\n" );
			     		valueXmlString.append("<gross_rate><![CDATA[" ).append("").append( "]]></gross_rate>\r\n" );
			     		valueXmlString.append("<comm_perc><![CDATA[" ).append("").append( "]]></comm_perc>\r\n" );
			     		valueXmlString.append("<comm_per_unit><![CDATA[" ).append("").append( "]]></comm_per_unit>\r\n" );
			     		valueXmlString.append("<rate__new><![CDATA[" ).append("").append( "]]></rate__new>\r\n" );
			     		valueXmlString.append("<basic_dist_price><![CDATA[" ).append("").append( "]]></basic_dist_price>\r\n" );
			     		valueXmlString.append("<cost_rate><![CDATA[" ).append("").append( "]]></cost_rate>\r\n" );
			     		valueXmlString.append("<profit><![CDATA[" ).append("").append( "]]></profit>\r\n" );
			     		valueXmlString.append("<profit_on_goods><![CDATA[" ).append("").append( "]]></profit_on_goods>\r\n" );
			     		valueXmlString.append("<final_price><![CDATA[" ).append("").append( "]]></final_price	>\r\n" );
			     		valueXmlString.append("<perc_profit><![CDATA[" ).append("").append( "]]></perc_profit>\r\n" );
			     		valueXmlString.append("<available_stock><![CDATA[" ).append("").append( "]]></available_stock>\r\n" );
			     		valueXmlString.append("</Detail3>");
			     		

					}
					catch (Exception e)
					{
						System.out.println("@@@@ Exception ON ITEMCHANGE ==> itm_default_add : "+e);
					}
				}
				else if(currentColumn.trim().equalsIgnoreCase("item_code"))
				{
					Map<String, String> itemDetailMap = new HashMap<String, String>();
					try
					{
						StringBuffer valXmlStr = new StringBuffer();
						String currDomStr = genericUtility.serializeDom(dom);
						String itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						double quantity = getDoubleValue(checkNull(genericUtility.getColumnValue("qty_per_month", dom)));
						discountType = checkNull(genericUtility.getColumnValue("discount_type",dom));
						
						itemDetailMap = getItemData(itemCode, conn);
						
						double discount2  = getRate(conn, globaldisPriceList, itemCode);
			            double rate2  = getRate(conn, globalpriceList, itemCode);
			     		
			     		Double grossRate2 = ((rate2 - ((rate2 * discount2) / 100)));
			     		if(grossRate2.isNaN() || grossRate2.isInfinite())
			     		{
			     			grossRate2 = 0.0;
			     		}
			     		double comm = 0;
			     		try
			     		{
			     			comm = getCommision(globalcommTable,currDate,conn,itemCode);
			     		}
			     		catch(Exception e) 
			     		{
			     			comm = 0;
			     		}
			     		
			     		double commPerUnit2 = (((grossRate2 * comm) / 100));
			     		double newCostRate2 = ((grossRate2 - commPerUnit2));
			     		double costRate2 = getCostRate(conn, itemCode);
			     		double availblStock = getStockDetails(conn,itemCode);
			     		double profit2 = (newCostRate - costRate);
			     		String profitParse = String.format("%.2f", profit);
						double profitOnGoods2 = (Double.parseDouble(profitParse) * quantity);
			     		double finalPrice2 = (grossRate2 * quantity);
			     		Double percProfit2 = ((profitOnGoods2 / finalPrice2) * 100);

			     		if(percProfit2.isNaN() || percProfit2.isInfinite())
			     		{
			     			percProfit2 = 0.0;
			     		}
						
						
			     		valXmlStr.append("<quot_no>").append("<![CDATA["+ checkNull(genericUtility.getColumnValue("quot_no",dom)) +"]]>").append("</quot_no>");
			     		valXmlStr.append("<line_no>").append("<![CDATA["+  checkNull(genericUtility.getColumnValue("line_no",dom)) +"]]>").append("</line_no>");
			     		valXmlStr.append("<item_code protect='1'><![CDATA[" ).append(itemCode).append( "]]></item_code>\r\n" );
			     		valXmlStr.append("<item_descr><![CDATA[" ).append(itemDetailMap.get("descr")).append( "]]></item_descr>\r\n" );
			     		valXmlStr.append("<unit><![CDATA[" ).append(itemDetailMap.get("unit")).append( "]]></unit>\r\n" );
			     		valXmlStr.append("<qty_per_month><![CDATA[" ).append(quantity).append( "]]></qty_per_month>\r\n" );
			     		valXmlStr.append("<rate><![CDATA[" ).append(String.format ("%.2f", rate2)).append( "]]></rate>\r\n" );
			     		valXmlStr.append("<discount_type><![CDATA[" ).append(discountType).append( "]]></discount_type>\r\n" );
			     		valXmlStr.append("<discount><![CDATA[" ).append(String.format ("%.2f", discount2)).append( "]]></discount>\r\n" );
			     		valXmlStr.append("<gross_rate><![CDATA[" ).append(String.format ("%.2f", grossRate2)).append( "]]></gross_rate>\r\n" );
			     		valXmlStr.append("<comm_perc><![CDATA[" ).append(String.format ("%.2f", comm)).append( "]]></comm_perc>\r\n" );
			     		valXmlStr.append("<comm_per_unit><![CDATA[" ).append(String.format ("%.2f",commPerUnit2)).append( "]]></comm_per_unit>\r\n" );
			     		valXmlStr.append("<rate__new><![CDATA[" ).append(Math.round(grossRate2)).append( "]]></rate__new>\r\n" );
			     		valXmlStr.append("<basic_dist_price><![CDATA[" ).append(getCommaSep(newCostRate2)).append( "]]></basic_dist_price>\r\n" );
			     		valXmlStr.append("<cost_rate><![CDATA[" ).append( String.format("%.2f", costRate2)).append( "]]></cost_rate>\r\n" );
			     		valXmlStr.append("<profit><![CDATA[" ).append( getCommaSep(profit2) ).append( "]]></profit>\r\n" );
			     		valXmlStr.append("<profit_on_goods><![CDATA[" ).append( getCommaSep( profitOnGoods2 ) ).append( "]]></profit_on_goods>\r\n" );
			     		valXmlStr.append("<final_price><![CDATA[" ).append( getCommaSep( finalPrice2 ) ).append( "]]></final_price	>\r\n" );
			     		valXmlStr.append("<perc_profit><![CDATA[" ).append( getCommaSep( percProfit2 )  ).append( "]]></perc_profit>\r\n" );
			     		valXmlStr.append("<available_stock><![CDATA[" ).append( getCommaSep( availblStock ) ).append( "]]></available_stock>\r\n" );
			     		valXmlStr.append("</Detail3>");
			     		currDomStr = currDomStr.replace("</Detail3>",valXmlStr.toString());
			     		
			     		valueXmlString.append(currDomStr);
			     		
					}
					catch(Exception e)
					{
						System.out.println("@@@@ Exception ON ITEMCHANGE ==> ITEM_CODE : "+e);
					}
					
				}
			
			}
			valueXmlString.append("</Root>");
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if(conn!=null)
				{
					if(rs != null)rs.close();
					rs = null;
					if(pstmt != null)pstmt.close();
					pstmt = null;
					conn.close();
				}
				conn = null;
			}catch(Exception d)
			{
			  d.printStackTrace();
			  throw new ITMException( d );
			}
		}
		System.out.println("#### FINAL RETURN :: valueXmlString : "+valueXmlString.toString());
		return ( valueXmlString == null || valueXmlString.toString().trim().length() == 0 ? "" : valueXmlString.toString() );
	 }
	
	//Added by AMOL SANT on 03-09-18	START
	private String getSrNo(Document docu)
	{
		//String srNo = "";
		NodeList parentNList = docu.getElementsByTagName("Detail3");
		int nListLen = parentNList.getLength();
		System.out.println("Detail3 Lenght "+nListLen);
		/*Node node = null;
		Element element = null;
		for( int i = 0; i < nListLen; i++) 
		{
			node = parentNList.item(i);
		}*/
		return String.valueOf(nListLen);
		
	}
	//Added by AMOL SANT on 03-09-18	END
	
	private String checkNull(String input)	
	{
		if (input == null)
		{
			input="";
		}
		return input;
	}
	
	private String errorType( Connection conn , String errorCode ) throws ITMException
	{
		String msgType = "";
		PreparedStatement pstmt = null ; 
		ResultSet rs = null;
		try
		{			
			String  sql = " SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO =   ? ";
			
			pstmt = conn.prepareStatement( sql );			
			pstmt.setString(1, checkNull(errorCode));			
			rs = pstmt.executeQuery();
			while( rs.next() )
			{
				msgType = rs.getString("MSG_TYPE");
			}			
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw new ITMException(ex); //Added By Mukesh Chauhan on 05/08/19
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
	
	public boolean preDomExists(Document dom, String currentFormNo) throws ITMException
	{
		System.out.println("##### Pre Dom Exist ALL DATA (DOM 2) "+dom);
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
	//Not Using....
	public String getPrevFormValues( Document dom, String currentFormNo, ArrayList<String> temp, String colName) throws ITMException
	{
		int domID = 1;
		E12GenericUtility genericUtility = new ibase.utility.E12GenericUtility();
		NodeList parentList = null;
		NodeList childList = null;
		Node parentNode = null;
		Node childNode = null;
		Element elementAttr = null;
		String childNodeName = "";
		String columnValue = "";
		String returnPrevStr = "";
		Document chgPreDom = null;
		try
		{
			chgPreDom = genericUtility.parseString("<Root/>");
			System.out.println("currentFormNo ["+currentFormNo+"]");
			parentList = dom.getElementsByTagName("Detail" + currentFormNo);
			int parentNodeListLength = parentList.getLength();
			for (int prntCtr = 0; prntCtr < parentNodeListLength; prntCtr++ )
			{							
				parentNode = parentList.item(prntCtr);
				childList = parentNode.getChildNodes();
				for (int ctr = 0; ctr < childList.getLength(); ctr++)
				{ 
					childNode = childList.item(ctr);

					if((childNode.getFirstChild() != null && childNode.getFirstChild().getNodeValue() != null))					
					{ 	
						columnValue = childNode.getFirstChild().getNodeValue().trim();
						System.out.println("columnValue :::: P "+columnValue);
						System.out.println("Child Nodename :::: P "+(childNode.getNodeName()));
						System.out.println("Child colName :::: P "+(colName));
						System.out.println("Child contains :::: P "+(temp.contains(columnValue)));
						System.out.println("temp :::: P "+(temp));
						if ( childNode.getNodeName().equalsIgnoreCase(colName) && temp.contains(columnValue))
						{
							elementAttr = (Element)parentNode;
							
							if ( parentNode.getAttributes().getNamedItem( "domID" ) != null )
							{
								System.out.println("Inside Attribute : "+elementAttr);
								
								Node node = elementAttr.getElementsByTagName("attribute").item(0);
								String str = node.getAttributes().getNamedItem("updateFlag").getTextContent();
								System.out.println("str :: "+str);
								if(str.equals("D"))
									elementAttr.setAttribute( "selected" , "N" );
								else
									elementAttr.setAttribute( "selected" , "Y" );
								domID++;
							}
							Node importedNode = chgPreDom.importNode( parentNode, true );
							chgPreDom.getDocumentElement().appendChild( importedNode );
							break;
						}

					}
				}

			}
			if(currentFormNo.equals("1"))
			{
				domID = 1;
			}
			System.out.println(">>>>>>>>> chgPreDom "+chgPreDom);
			System.out.println(">>>>>>>>> returnPrevStr "+returnPrevStr);
			returnPrevStr = genericUtility.serializeDom(chgPreDom);
			System.out.println(">>>>>>>>> After returnPrevStr "+returnPrevStr);
			System.out.println(">>>>>>>>> After returnPrevStr "+(returnPrevStr.indexOf("Detail"+currentFormNo)));
			if(returnPrevStr.indexOf("Detail"+currentFormNo) != -1)
			{
				returnPrevStr = returnPrevStr.substring(returnPrevStr.indexOf(">") + 1, returnPrevStr.lastIndexOf("</"));
			}
			returnPrevStr = returnPrevStr.indexOf("Detail"+currentFormNo) != -1 ? returnPrevStr : "";
			System.out.println(">>>>>>>>> After returnPrevStr>>>> "+returnPrevStr);
		}
		catch ( Exception e )
		{
			System.out.println("SalesQuotProposal.getPrevFormValues() : "+e);
			throw new ITMException(e);
		}
		return returnPrevStr;

	}
	//Not using.........
	private String getInvoiceData(Connection conn, StringBuffer valueXmlString, String custCode,String selectedCust, int domID, String loginSiteCode, String tranId) throws ITMException, Exception
	{
		String invoiceAmt="", invoiceId = "", discrpFalg = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		PreparedStatement pstmt1 = null;
		ResultSet rs1 = null;
		String formatedCustId = getFormattedCustId(E12GenericUtility.checkNull( selectedCust ));
		String custmerBill  = "";
		String updatedInvoiceId = "";
		SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
		String eventDateStr = "";
		Calendar calObj1 = Calendar.getInstance();
		calObj1.setTime(new java.util.Date());
		calObj1.add(Calendar.MONTH, -3);
		calObj1.add(Calendar.YEAR, -2);
		java.util.Date dateFrom = calObj1.getTime();
		Timestamp timestamp = new Timestamp(calObj1.getTimeInMillis());
		eventDateStr = sdf.format(dateFrom);
		String sDteFro = genericUtility.getValidDateString(eventDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
		Timestamp dateFro1 = java.sql.Timestamp.valueOf(sDteFro + " 00:00:00");
		System.out.println("dateFro1 Now :: "+dateFro1);
		ResultSet resultSet = null;
		PreparedStatement pstmt2 = null;
		try 
		{
			System.out.println("str1 : "+tranId);
			if(selectedCust != null && selectedCust.trim().length() > 0)
			{
				custmerBill = "SELECT * FROM INVOICE WHERE CUST_CODE = ? AND SITE_CODE = ? AND TRAN_DATE > ? AND INVOICE_ID NOT IN ("+ formatedCustId +") AND (DLV_STAT <> 1 OR DLV_STAT IS NULL)";
			}
			else if(tranId != null && tranId.trim().length() > 0)
			{
				//custmerBill = "SELECT * FROM INVOICE WHERE CUST_CODE = ? AND SITE_CODE = ? AND TRAN_DATE > ? AND (DLV_STAT <> 1 OR DLV_STAT IS NULL)";
				custmerBill = "SELECT * FROM INVOICE WHERE CUST_CODE = ? AND SITE_CODE = ? AND TRAN_DATE > ? AND INVOICE.SALE_ORDER= (SELECT SORDER.SALE_ORDER FROM SORDER WHERE SORDER.UDF__STR1 = ? ) AND (DLV_STAT <> 1 OR DLV_STAT IS NULL)";
			}
			else
			{
				custmerBill = "SELECT * FROM INVOICE WHERE CUST_CODE = ? AND SITE_CODE = ? AND TRAN_DATE > ? AND (DLV_STAT <> 1 OR DLV_STAT IS NULL)";
			}
			pstmt = conn.prepareStatement(custmerBill);
			if((selectedCust != null && selectedCust.trim().length() > 0) || (tranId == null || tranId.trim().length() <= 0))
			{
				pstmt.setString(1, custCode);
				pstmt.setString(2, loginSiteCode);
				pstmt.setTimestamp(3, dateFro1);
			}
			else
			{
				pstmt.setString(1, custCode);
				pstmt.setString(2, loginSiteCode);
				pstmt.setTimestamp(3, dateFro1);
				pstmt.setString(4, tranId);
			}
			
			rs = pstmt.executeQuery();
			int i = 0;
			while(rs.next())
			{
				invoiceAmt = rs.getString("inv_amt");
				invoiceId = rs.getString("invoice_id");
				System.out.println("invoiceAmt :: "+invoiceAmt);
				System.out.println("invoiceId :: "+invoiceId);
				int totalQuantity = getQuantity(conn, invoiceId);
				domID++;
				if(tranId != null && tranId.trim().length() > 0)
				{
					updatedInvoiceId = invoiceId;
					valueXmlString.append("<Detail2 domID=\""+domID+"\" selected=\"Y\" > \r\n");
					valueXmlString.append("<attribute  selected=\"Y\" updateFlag=\"A\"  status=\"N\" pkNames=\"\" />\r\n");
					valueXmlString.append("<tran_id/>");
					valueXmlString.append( "<line_no protect='1'><![CDATA[" ).append(domID).append( "]]></line_no>\r\n" );
					valueXmlString.append("<invoice_id><![CDATA[" ).append(invoiceId).append( "]]></invoice_id>\r\n" );
					valueXmlString.append("<invoice_amt><![CDATA[" ).append(invoiceAmt).append( "]]></invoice_amt>\r\n" );
					valueXmlString.append("<discr_flag><![CDATA[" ).append(discrpFalg).append( "]]></discr_flag>\r\n" );
					valueXmlString.append("<quantity><![CDATA[" ).append(totalQuantity).append( "]]></quantity>\r\n" );
					String custNameSql = "select sale_order from invoice where invoice_id = ?";
					pstmt2 = conn.prepareStatement(custNameSql);
					pstmt2.setString(1, invoiceId);
					resultSet= pstmt2.executeQuery();
					if(resultSet.next())
					{
						valueXmlString.append("<sale_order><![CDATA[" ).append(resultSet.getString("sale_order")).append( "]]></sale_order>\r\n" );
					}
					valueXmlString.append("</Detail2>");
					if(resultSet != null)
					{
						resultSet.close(); resultSet = null;
					}
					if(pstmt2 != null)
					{
						pstmt2.close(); pstmt2 = null;
					}
				}
				else
				{
					valueXmlString.append("<Detail2 domID=\""+domID+"\" selected=\"N\" > \r\n");
					valueXmlString.append("<attribute  selected=\"N\" updateFlag=\"A\"  status=\"N\" pkNames=\"\" />\r\n");
					valueXmlString.append("<tran_id/>");
					valueXmlString.append( "<line_no protect='1'><![CDATA[" ).append(domID).append( "]]></line_no>\r\n" );
					valueXmlString.append("<invoice_id><![CDATA[" ).append(invoiceId).append( "]]></invoice_id>\r\n" );
					valueXmlString.append("<invoice_amt><![CDATA[" ).append(invoiceAmt).append( "]]></invoice_amt>\r\n" );
					valueXmlString.append("<discr_flag><![CDATA[" ).append(discrpFalg).append( "]]></discr_flag>\r\n" );
					valueXmlString.append("<quantity><![CDATA[" ).append(totalQuantity).append( "]]></quantity>\r\n" );
					String custNameSql = "select sale_order from invoice where invoice_id = ?";
					pstmt2 = conn.prepareStatement(custNameSql);
					pstmt2.setString(1, invoiceId);
					resultSet= pstmt2.executeQuery();
					if(resultSet.next())
					{
						valueXmlString.append("<sale_order><![CDATA[" ).append(resultSet.getString("sale_order")).append( "]]></sale_order>\r\n" );
					}
					valueXmlString.append("</Detail2>");
					if(resultSet != null)
					{
						resultSet.close(); resultSet = null;
					}
					if(pstmt2 != null)
					{
						pstmt2.close(); pstmt2 = null;
					}
				}
			}
			if(tranId != null && tranId.trim().length() > 0)
			{
				System.out.println("updatedInvoiceId :: "+updatedInvoiceId);
				updatedInvoiceId = getFormattedCustId(E12GenericUtility.checkNull( updatedInvoiceId ));
				String unselectInvoice = "SELECT * FROM INVOICE WHERE CUST_CODE = ? AND SITE_CODE = ? AND TRAN_DATE > ? AND INVOICE_ID NOT IN ("+ updatedInvoiceId +") AND (DLV_STAT <> 1 OR DLV_STAT IS NULL)";
				pstmt1 = conn.prepareStatement(unselectInvoice);
				pstmt1.setString(1, custCode);
				pstmt1.setString(2, loginSiteCode);
				pstmt1.setTimestamp(3, dateFro1);
				rs1 = pstmt1.executeQuery();
				while(rs1.next())
				{
					invoiceAmt = rs1.getString("inv_amt");
					invoiceId = rs1.getString("invoice_id");
					int totalQuantity = getQuantity(conn, invoiceId);
					domID++;
					valueXmlString.append("<Detail2 domID=\""+domID+"\" selected=\"N\" > \r\n");
					valueXmlString.append("<attribute  selected=\"N\" updateFlag=\"A\"  status=\"N\" pkNames=\"\" />\r\n");
					valueXmlString.append("<tran_id/>");
					valueXmlString.append( "<line_no protect='1'><![CDATA[" ).append(domID).append( "]]></line_no>\r\n" );
					valueXmlString.append("<invoice_id><![CDATA[" ).append(invoiceId).append( "]]></invoice_id>\r\n" );
					valueXmlString.append("<invoice_amt><![CDATA[" ).append(invoiceAmt).append( "]]></invoice_amt>\r\n" );
					valueXmlString.append("<discr_flag><![CDATA[" ).append(discrpFalg).append( "]]></discr_flag>\r\n" );
					valueXmlString.append("<quantity><![CDATA[" ).append(totalQuantity).append( "]]></quantity>\r\n" );
					String custNameSql = "select sale_order from invoice where invoice_id = ?";
					pstmt2 = conn.prepareStatement(custNameSql);
					pstmt2.setString(1, invoiceId);
					resultSet= pstmt2.executeQuery();
					if(resultSet.next())
					{
						valueXmlString.append("<sale_order><![CDATA[" ).append(resultSet.getString("sale_order")).append( "]]></sale_order>\r\n" );
					}
					valueXmlString.append("</Detail2>");
					if(resultSet != null)
					{
						resultSet.close(); resultSet = null;
					}
					if(pstmt2 != null)
					{
						pstmt2.close(); pstmt2 = null;
					}
				}
				if(rs1 != null)
				{
					rs1.close(); rs1 = null;
				}
				if(pstmt1 != null)
				{
					pstmt1.close(); pstmt1 = null;
				}
				
			}
			if(rs != null)
			{
				rs.close(); rs = null;
			}
			if(pstmt != null)
			{
				pstmt.close(); pstmt = null;
			}
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
			System.out.println("InvoiceAckwIC.getInvoiceData() : "+e);
			try {
				custmerBill = "SELECT * FROM INVOICE WHERE CUST_CODE = ? AND SITE_CODE = ? AND TRAN_DATE > ? AND (DLV_STAT <> 1 OR DLV_STAT IS NULL)";
				pstmt = conn.prepareStatement(custmerBill);
				pstmt.setString(1, custCode);
				pstmt.setString(2, loginSiteCode);
				pstmt.setTimestamp(3, dateFro1);
				rs = pstmt.executeQuery();
				int i = 0;
				while(rs.next())
				{
					invoiceAmt = rs.getString("inv_amt");
					invoiceId = rs.getString("invoice_id");
					System.out.println("invoiceAmt :: "+invoiceAmt);
					System.out.println("invoiceId :: "+invoiceId);
					int totalQuantity = getQuantity(conn, invoiceId);
					domID++;
					
					valueXmlString.append("<Detail2 domID=\""+domID+"\" selected=\"N\" > \r\n");
					valueXmlString.append("<attribute  selected=\"N\" updateFlag=\"A\"  status=\"N\" pkNames=\"\" />\r\n");
					valueXmlString.append("<tran_id/>");
					valueXmlString.append( "<line_no protect='1'><![CDATA[" ).append(domID).append( "]]></line_no>\r\n" );
					valueXmlString.append("<invoice_id><![CDATA[" ).append(invoiceId).append( "]]></invoice_id>\r\n" );
					valueXmlString.append("<invoice_amt><![CDATA[" ).append(invoiceAmt).append( "]]></invoice_amt>\r\n" );
					valueXmlString.append("<discr_flag><![CDATA[" ).append(discrpFalg).append( "]]></discr_flag>\r\n" );
					valueXmlString.append("<quantity><![CDATA[" ).append(totalQuantity).append( "]]></quantity>\r\n" );
					valueXmlString.append("</Detail2>");
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
				throw new ITMException(e);
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			System.out.println("InvoiceAckwIC.getInvoiceData() : "+e);
			throw new ITMException(e);
		}
		System.out.println("InvoiceAckwIC.getInvoiceData() : "+valueXmlString.toString());
		return valueXmlString.toString();
	
	}
	//This method return all details against invoice id.
	public List<String> getInvoiceDetail(Connection conn, String custCode, Timestamp fromDate, 
			Timestamp toDate, StringBuffer valueXmlString, String siteCode,String discountType) throws Exception
	{
		System.out.println("#### getInvoiceDetail ....");
		srNoSet = new HashSet();//
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		ResultSet rs = null;
		ResultSet rs1 = null;
		String custmerBill  = "";
		ArrayList<String> invoiceArr = new ArrayList<String>();
		Map<String, Double> map = new HashMap<String, Double>();
		String disPriceList = E12GenericUtility.checkNull(getDBColumnValue("CUSTOMER", "PRICE_LIST__DISC", "CUST_CODE = '"+custCode+"'", conn));
		String priceList = E12GenericUtility.checkNull(getDBColumnValue("CUSTOMER", "PRICE_LIST", "CUST_CODE = '"+custCode+"'", conn));
		//String commTable = E12GenericUtility.checkNull(getDBColumnValue("CUSTOMER", "COMM_TABLE", "CUST_CODE = '"+custCode+"'", conn));
		String commTable = getCommission(custCode, conn);
		
		globaldisPriceList = disPriceList; 
		globalpriceList = priceList; 
		globalcommTable = commTable;
		
		SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
		String eventDateStr = "";
		//String discountType = "";
		Calendar calObj1 = Calendar.getInstance();
		calObj1.setTime(new java.util.Date());
		java.util.Date dateFrom = calObj1.getTime();
		eventDateStr = sdf.format(dateFrom);
		
		String currDateStr = genericUtility.getValidDateString(eventDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
		String qq = genericUtility.getValidDateTimeString(eventDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
		Timestamp currDate = java.sql.Timestamp.valueOf(currDateStr + " 00:00:00");
		custmerBill = "SELECT * FROM INVOICE WHERE CUST_CODE = ? AND TRAN_DATE >= ? AND TRAN_DATE <= ? AND INV_TYPE NOT IN ('DF','EF')";
		try
		{
			pstmt = conn.prepareStatement(custmerBill);
			pstmt.setString(1, custCode);
			pstmt.setTimestamp(2, fromDate);
			pstmt.setTimestamp(3, toDate);
			
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				String invoiceId = checkNull(rs.getString("invoice_id")).trim();
				//System.out.println("#### INVOICE ID :: "+invoiceId);
				String invocieData = "SELECT * FROM INVOICE_TRACE WHERE INVOICE_ID = ? ";
				try {
					pstmt1 = conn.prepareStatement(invocieData);
					pstmt1.setString(1, invoiceId);
					rs1 = pstmt1.executeQuery();
					while(rs1.next())
					{
						if(map.containsKey(rs1.getString("item_code")))
						{
							double i = map.put(rs1.getString("item_code"), Double.parseDouble(rs1.getString("quantity")));
							System.out.println(i);
							map.put(rs1.getString("item_code"), i + Double.parseDouble(rs1.getString("quantity")) );
						}
						else
						{
							map.put(rs1.getString("item_code"), Double.parseDouble(rs1.getString("quantity")));
						}
					}
					if(rs1 != null)
					{
						rs1.close(); rs1 = null;
					}
					if(pstmt1 != null)
					{
						pstmt1.close(); pstmt1 = null;
					}
				} catch (Exception e) 
				{
					System.out.println("@@@@@ Exception in SalesQuotProposal :: getInvoiceId() :  "+e);
				}
				invoiceArr.add(invoiceId);
			}
			if(rs != null)
			{
				rs.close(); rs = null;
			}
			if(pstmt != null)
			{
				pstmt.close(); pstmt = null;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("SalesQuotProposal.getInvoiceId() "+e);
			throw new ITMException(e);
		}
		
		String domID = "1";
		
		Iterator<Map.Entry<String, Double>> itr = map.entrySet().iterator();
        
		DistCommon distCommon = new DistCommon();
        
        int srNo = 0;
        
        Map<String, String> itemDetailMap = new HashMap<String, String>();
       
        while(itr.hasNext())
        {
        	srNo++;
        	srNoSet.add(srNo);//added on 10-10-18 Add new Form no2 to get srNo.
            Map.Entry<String, Double> entry = itr.next();
            
            String itemCode = E12GenericUtility.checkNull(entry.getKey());
            itemCodeArr.add(itemCode);
            double maxValue = entry.getValue();
            itemDetailMap = getItemData(itemCode, conn);
     		
     		double discount = (distCommon.pickRate(disPriceList, eventDateStr, entry.getKey(), conn));
     		//double rate = (distCommon.pickRate(priceList, eventDateStr, entry.getKey(), conn));*/
            System.out.println("##### Setting Discount disPriceList "+disPriceList);
           // double discount  = getRate(conn, disPriceList, itemCode);
            System.out.println("##### Setting rate priceList "+priceList);
            double rate  = getRate(conn, priceList, itemCode);
     		
     		Double grossRate = ((rate - ((rate * discount) / 100)));
     		if(grossRate.isNaN() || grossRate.isInfinite())
     		{
     			grossRate = 0.0;
     		}
     		double comm = 0;
     		try
     		{
     			comm = getCommision(commTable,currDate,conn,itemCode);
     		}
     		catch(Exception e) 
     		{
     			comm = 0;
     		}
     		
     		double commPerUnit = (((grossRate * comm) / 100));
     		double newCostRate = ((grossRate - commPerUnit));
     		double costRate = getCostRate(conn, itemCode);
     		double availblStock = getStockDetails(conn,itemCode);
     		double profit = (newCostRate - costRate);
     		String profitParse = String.format("%.2f", profit);
			double profitOnGoods = (Double.parseDouble(profitParse) * maxValue);
     		double finalPrice = (grossRate * maxValue);
     		Double percProfit = ((profitOnGoods / finalPrice) * 100);

     		if(percProfit.isNaN() || percProfit.isInfinite())
     		{
     			percProfit = 0.0;
     		}
     		/*System.out.println("#### Item Code :: "+itemCode);
     		System.out.println("#### OLD Rate :: "+rate);
     		System.out.println("#### Discount :: "+discount);
     		System.out.println("#### comm :: "+comm);
     		System.out.println("#### commPerUnit :: "+commPerUnit);
     		System.out.println("#### basic_dist_price : "+newCostRate);
     		System.out.println("#### Final cost Rate : "+costRate);
     		System.out.println("#### availblStock : "+availblStock);
     		System.out.println("#### Formated Profit "+profitParse);
			System.out.println("#### qtyMonth : "+maxValue);
     		System.out.println("#### finalPrice : "+finalPrice);
     		System.out.println("#### percProfit : "+percProfit);*/
     		
     		
     		valueXmlString.append("<Detail2 domID=\""+srNo+"\" selected=\"N\" > \r\n");
     		valueXmlString.append("<attribute  selected=\"N\" updateFlag=\"A\"  status=\"N\" pkNames=\"\" />\r\n");
     		valueXmlString.append("<quot_no/>");
     		valueXmlString.append( "<line_no protect='1'><![CDATA[" ).append(srNo).append( "]]></line_no>\r\n" );
     		valueXmlString.append("<sr_no><![CDATA[" ).append(srNo).append( "]]></sr_no>\r\n" );
     		valueXmlString.append("<item_code protect='1'><![CDATA[" ).append(itemCode).append( "]]></item_code>\r\n" );
     		valueXmlString.append("<item_descr><![CDATA[" ).append(itemDetailMap.get("descr")).append( "]]></item_descr>\r\n" );
     		valueXmlString.append("<unit><![CDATA[" ).append(itemDetailMap.get("unit")).append( "]]></unit>\r\n" );
     		valueXmlString.append("<qty_per_month><![CDATA[" ).append(maxValue).append( "]]></qty_per_month>\r\n" );
     		valueXmlString.append("<rate><![CDATA[" ).append(String.format ("%.2f", rate)).append( "]]></rate>\r\n" );
     		valueXmlString.append("<discount_type><![CDATA[" ).append(discountType).append( "]]></discount_type>\r\n" );
     		valueXmlString.append("<discount><![CDATA[" ).append(String.format ("%.2f", discount)).append( "]]></discount>\r\n" );
     		valueXmlString.append("<gross_rate><![CDATA[" ).append(String.format ("%.2f", grossRate)).append( "]]></gross_rate>\r\n" );
     		valueXmlString.append("<comm_perc><![CDATA[" ).append(String.format ("%.2f", comm)).append( "]]></comm_perc>\r\n" );
     		valueXmlString.append("<comm_per_unit><![CDATA[" ).append(String.format ("%.2f",commPerUnit)).append( "]]></comm_per_unit>\r\n" );
     		//valueXmlString.append("<rate__new><![CDATA[" ).append(Math.round(rate)).append( "]]></rate__new>\r\n" );//Changed on 04-10-18 Having Confusion 
     		valueXmlString.append("<rate__new><![CDATA[" ).append(Math.round(grossRate)).append( "]]></rate__new>\r\n" );
     		//valueXmlString.append("<basic_dist_price><![CDATA[" ).append(String.format ("%.2f", newCostRate)).append( "]]></basic_dist_price>\r\n" );
     		valueXmlString.append("<basic_dist_price><![CDATA[" ).append(getCommaSep(newCostRate)).append( "]]></basic_dist_price>\r\n" );
     		valueXmlString.append("<cost_rate><![CDATA[" ).append(String.format ("%.2f", costRate)).append( "]]></cost_rate>\r\n" );
     		
     		/*valueXmlString.append("<profit><![CDATA[" ).append(String.format ("%.2f", profit)).append( "]]></profit>\r\n" );
     		valueXmlString.append("<profit_on_goods><![CDATA[" ).append(String.format ("%.2f", profitOnGoods)).append( "]]></profit_on_goods>\r\n" );
     		valueXmlString.append("<final_price><![CDATA[" ).append(String.format ("%.2f", finalPrice)).append( "]]></final_price	>\r\n" );
     		valueXmlString.append("<perc_profit><![CDATA[" ).append(String.format ("%.2f", percProfit)).append( "]]></perc_profit>\r\n" );
     		valueXmlString.append("<available_stock><![CDATA[" ).append(String.format ("%.2f", availblStock)).append( "]]></available_stock>\r\n" );*/
     		valueXmlString.append("<profit><![CDATA[" ).append( getCommaSep(profit) ).append( "]]></profit>\r\n" );
     		valueXmlString.append("<profit_on_goods><![CDATA[" ).append( getCommaSep( profitOnGoods ) ).append( "]]></profit_on_goods>\r\n" );
     		valueXmlString.append("<final_price><![CDATA[" ).append( getCommaSep( finalPrice ) ).append( "]]></final_price	>\r\n" );
     		valueXmlString.append("<perc_profit><![CDATA[" ).append( getCommaSep( percProfit )  ).append( "]]></perc_profit>\r\n" );
     		valueXmlString.append("<available_stock><![CDATA[" ).append( getCommaSep( availblStock ) ).append( "]]></available_stock>\r\n" );
     		valueXmlString.append("</Detail2>");
     		
        }
		return invoiceArr;
		
	}
	
	private double getCommision(String commTable, Timestamp currDate, Connection conn, String itemCode) throws ITMException
	{
		System.out.println("#### In getCommision....");
		System.out.println("#### commTable ["+commTable+"]");
		System.out.println("#### currDate ["+currDate+"]");
		System.out.println("#### itemCode ["+itemCode+"]");
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		String sql1 = "";
		String sql2 = "";
		
		double commision = 0;
		Timestamp maxValid = null;
		if(commTable != null)
		{	
			try {
				sql= "SELECT COMM_PERC FROM COMM_DET WHERE COMM_TABLE = ? AND VALID_UPTO >= ? AND ITEM_CODE = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,commTable);
				pstmt.setTimestamp(2,currDate);
				pstmt.setString(3,itemCode);
				rs = pstmt.executeQuery();
				while(rs.next())
				{
					commision = rs.getDouble("COMM_PERC");
				}
				close(rs,pstmt);
				if( commision == 0 ) 
				{
					maxValid = getMaxValidity(conn,commTable,itemCode);
					if( maxValid != null) 
					{
						sql1 = "SELECT COMM_PERC FROM COMM_DET WHERE COMM_TABLE = ? AND ITEM_CODE = ? AND VALID_UPTO = ?";
						pstmt = conn.prepareStatement(sql1);
						pstmt.setString(1,commTable);
						pstmt.setString(2,itemCode);
						pstmt.setTimestamp(3,maxValid);
						rs = pstmt.executeQuery();
						while(rs.next())
						{
							commision = rs.getDouble("COMM_PERC");
						}
						close(rs,pstmt);
					}
					
					
				}
				
			} catch (SQLException e) 
			{
				System.out.println("#### Exception in SalesQuotProposal :: getCommision :"+e);
				e.printStackTrace();
				throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
			}
		}
		System.out.println("##### getCommision :: Commision "+commision);
		return commision;
	}
	
	private Timestamp getMaxValidity(Connection conn, String comm,String itemCode) throws ITMException 
	{
		Timestamp validDate = null;
		String sql = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		if(itemCode != null) 
		{
			sql = "SELECT MAX(VALID_UPTO) AS VALID_UPTO FROM COMM_DET WHERE COMM_TABLE = ? AND ITEM_CODE = ? ";
			try
			{
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, comm);
				pstmt.setString(2, itemCode);
				rs = pstmt.executeQuery();
				
				if(rs.next()) 
				{
					validDate = rs.getTimestamp("VALID_UPTO");
					System.out.println("####### IN IF COMMISION VALID_UPTO "+validDate);
				}
				close(rs,pstmt);
			} catch (SQLException e)
			{
				e.printStackTrace();
				throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
			}
		}
		else 
		{
			sql = "SELECT MAX(VALID_UPTO) AS VALID_UPTO FROM COMM_DET WHERE COMM_TABLE = ?";
			try 
			{
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, comm);
				rs = pstmt.executeQuery();
				if(rs.next()) 
				{
					validDate = rs.getTimestamp("VALID_UPTO");
					System.out.println("####### IN IF COMMISION VALID_UPTO "+validDate);
				}
				close(rs,pstmt);
			}
			catch (SQLException e)
			{
				e.printStackTrace();
				throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
			}
		}
		
		System.out.println("####### COMMISION VALID_UPTO "+validDate);
		return validDate;
		
	} 
	public String getFormattedCustId(String custId) 
	{
		StringBuffer custIdStrBuff = new StringBuffer();
		if( !"".equalsIgnoreCase( custId ) )
		{
			String[] custIdArr = custId.split(",");
			int len = custIdArr.length;
			for(int counter = 0; counter < len; counter++)
			{
				custIdStrBuff.append("'").append( E12GenericUtility.checkNull( custIdArr[counter] ) ).append("'");
				if( counter < len - 1 )
				{
					custIdStrBuff.append(",");
				}
			}
		}
		if( "".equalsIgnoreCase( custIdStrBuff.toString() ) )
		{
			custIdStrBuff.append("");
		}
		return custIdStrBuff.toString();
	}
	
	private int getQuantity(Connection connection, String invoiceId) throws ITMException
	{
		PreparedStatement pStmt = null;
		ResultSet resultSet = null;
		int totalQuantity = 0;
		String sql = "select quantity from invoice_trace where invoice_id = ? ";
		try {
			pStmt = connection.prepareStatement(sql);
			pStmt.setString(1, invoiceId);
			resultSet = pStmt.executeQuery();
			while(resultSet.next())
			{
				int quantity = resultSet.getInt("quantity");
				totalQuantity += quantity;
			}
			resultSet.close();
			pStmt.close();
			
		} catch (SQLException e)
		{
			System.out.println("@@@@@ Exception in getQuantity from invoice_trace : "+e);
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		
		return totalQuantity;
	}
	
	public String getDBColumnValue(String tableName, String columnName, String condition,Connection mConnection) throws ITMException
	{
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
			System.out.println("#### mQuery ["+mQuery+"]"); 
			mStmt = mConnection.createStatement();
			rs = mStmt.executeQuery(mQuery);

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
			System.out.println("@@@@ Exception in SalesQuotProposal.getDBColumnValue()"); 
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
				System.out.println("@@@@ SalesQuotProposal.getDBColumnValue() : "+e);
			}
		}
		 
		return columnValue.trim();
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
            System.out.println("@@@@ Exception : MasterStatefulEJB : serializeDom :"+e);
            throw new ITMException(e);
        }
        return retString;
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
			System.out.println("@@@@@ SQLException SalesQuotProposal.getItemData() : "+ex);
		}
		catch(Exception e)
		{
			System.out.println("@@@@@ Exception SalesQuotProposal.getItemData() : "+e);
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return itemDetailMap;
	}
	
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
			System.out.println("#### Actual Cost for Item Code "+itemCode);
			System.out.println("#### Total Valuation "+totalValue);
			System.out.println("#### Total Quant "+totalQuantity);
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
				//additionalCost =((getDoubleValue(getAdditionalCost(conn))/costRate) * 100 );
				//Note : Additional cost is 4.5 % on above cost rate.
				additionalCost =((getDoubleValue(getAdditionalCost(conn)) * costRate) /100 );
				System.out.println("#### additionalCost ["+additionalCost+"]");
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
			System.out.println("#### PROCESS_OVERHEAD_DET => TABLE_NO "+ohdTablePost);
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
					System.out.println("####  Over Head Value " +overHeadValue);
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
			System.out.println("#### FINAL costRate "+costRate);
		}
		
		catch(SQLException ex)
		{
			System.out.println("@@@@ SQLException SalesQuotProposal :: getCostRate() : "+ex);
		}
		catch(Exception e)
		{
			System.out.println("@@@@ Exception SalesQuotProposal :: getCostRate() : "+e);
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		
		return costRate;
	}
	
	private double getStockDetails(Connection conn, String itemCode) throws ITMException
	{
		PreparedStatement pstmt = null; 
		ResultSet rs = null;
		String sql = "";
		Double availblStock = 0.0;
		
		try 
		{
			sql = "SELECT SUM(QUANTITY) AS QUANTITY FROM STOCK WHERE QUANTITY <> 0 AND ITEM_CODE = ? AND LOC_CODE IN ('FGM','QUAR')";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, itemCode);
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				availblStock = rs.getDouble("QUANTITY");
			}
			close(rs,pstmt);
		}
		catch(Exception e) 
		{
			System.out.println("@@@@ Exception in SalesQuotProposal :: getStockDetails :"+e);
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		if(availblStock.isNaN() || availblStock.isInfinite())
 		{
			availblStock = 0.0;
 		}
		return availblStock;
	}
	//Wrkflw ....
	@Override
	public String approvePriceRate(String quotNo, String userInfo, String xmlData,String effDate, String validUpto)
	{
		System.out.println("###### approvePriceRate .....");
		System.out.println("###### effDate [ "+effDate);
		System.out.println("###### validUpto ["+validUpto);
		Document dom = null;		
		try
		{
			dom = parseString(xmlData);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		ConnDriver connDriver = new ConnDriver();
		Connection connection = null;
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		//String validUpto = "";
		String itemCode = "", proposedRate = "", custCode = "", priceList = "", unit = "", revisedRate = "";
		String termID = "",chgUser = "", chgTerm = "";
		NodeList parentNodeList = null;NodeList childNodeList = null;
		Node parentNode = null;Node childNode = null;
		String childNodeName = null;
		int cnt = 0, currentFormNo=0,childNodeListLength = 0 ,ctr=0;
		try
		{
			UserInfoBean userInfoBean = new UserInfoBean(userInfo);
			String transDB = userInfoBean.getTransDB();
			connection = connDriver.getConnectDB(transDB);
			connection.setAutoCommit(false);
			//Added by AMOL on 18-10-18  [To show transcation History to USER] START
			String sqlStr = "UPDATE SALES_QUOT SET WF_STATUS = ? WHERE QUOT_NO = ?";
			System.out.println("######### Updating Approve Satus ");
			try
			{
				pstmt = connection.prepareStatement(sqlStr);
				pstmt.setString(1, "S");
				pstmt.setString(2, quotNo);
				
				int updateCount = pstmt.executeUpdate();
				if(updateCount > 0) 
				{
					connection.commit();
				}
				System.out.println("#### Updating transetup No of rows "+updateCount);
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
			//Added by AMOL on 18-10-18  [To show transcation History to USER] END
			chgTerm = checkNull(genericUtility.getColumnValue("chg_term", dom, "1")).trim();
			chgUser = checkNull(genericUtility.getColumnValue("chg_user", dom, "1")).trim();
			custCode = checkNull(genericUtility.getColumnValue("cust_code", dom, "1")).trim();
			custCode = checkNull(genericUtility.getColumnValue("cust_code", dom, "1")).trim();
			priceList = getDBColumnValue("CUSTOMER", "PRICE_LIST", "CUST_CODE = '"+custCode+"'", connection);
			parentNodeList = dom.getElementsByTagName("Detail3");
			int parentNodeListLen = parentNodeList.getLength();
			
			for ( int rowCnt=0; rowCnt < parentNodeListLen; rowCnt++ )
			{
				parentNode = parentNodeList.item(rowCnt);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				for(ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
			
					if (childNodeName.equalsIgnoreCase("item_code"))
					{
						itemCode = childNode.getFirstChild().getNodeValue();
						//unit = getDBColumnValue("ITEM", "UNIT", "ITEM_CODE= '"+itemCode+"'", connection);
						//updatePriceList(priceList, itemCode, unit, chgTerm, chgUser, connection);
						//validUpto = getDBColumnValue("PRICELIST", "VALID_UPTO", "PRICE_LIST = '"+priceList+"' and ITEM_CODE = '"+itemCode+"'", connection);
					}
					else if (childNodeName.equalsIgnoreCase("rate__new"))
					{
						revisedRate = childNode.getFirstChild().getNodeValue();
					}
					else if (childNodeName.equalsIgnoreCase("unit"))
					{
						unit = childNode.getFirstChild().getNodeValue();
						System.out.println(":unit : in uniit "+unit);
					}
				}
				System.out.println("####");
				updatePriceList(priceList, itemCode, revisedRate, chgTerm,  chgUser, connection, effDate, validUpto);
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally 
		{
			if(connection != null)
			{
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return "1";
	}
	
	//This method called through workflow
	private void updatePriceList(String priceList, String itemCode, String revisedRate, String chgTerm, String chgUser, Connection connection, String effDate, String validUpto) throws ITMException
	{
		System.out.println("#### updatePriceList .....");
		PreparedStatement pStmt = null;
		ResultSet resultSet = null;
		PreparedStatement sPstmt = null;
		ResultSet sRst = null;
		int slabNo = 0;
		//String effFrom = "", validUpto = "";
		String listType = "", lotNoFrom = "", lotNoTo = "", rateType ="", priceListParent = "", calcBasis = "";
		String refNo = "", refOldNo = "", orderType = "", chgRefNo = "";
		String minQty = "", maxQty = "", rate = "", maxRate = "", minRate ="",unit = "";
		Timestamp validDate = null;
		
		//Added by AMOL on 06-11-18 START
		String listTypeSql = "SELECT LIST_TYPE FROM PRICELIST_MST WHERE PRICE_LIST = ?";
		try 
		{
			pStmt = connection.prepareStatement(listTypeSql);
			pStmt.setString(1, priceList);
			resultSet = pStmt.executeQuery();
			while(resultSet.next()) 
			{
				listType = resultSet.getString("LIST_TYPE");
			}
			close(resultSet, pStmt);
		}
		catch(Exception e) 
		{
			System.out.println("#### Exxception in updatePriceList :: listTypeSql :"+e);
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		//Added by AMOL on 06-11-18 END
		System.out.println("######## ListType ["+listType+"]");
		String priceSql = "SELECT MAX(SLAB_NO) as SLAB_NO FROM PRICELIST WHERE PRICE_LIST = ? AND ITEM_CODE = ? AND LIST_TYPE = ?";
		try 
		{
			pStmt = connection.prepareStatement(priceSql);
			pStmt.setString(1, priceList);
			pStmt.setString(2, itemCode);
			pStmt.setString(3, listType);
			resultSet = pStmt.executeQuery();
			if(resultSet.next())
			{
				slabNo = resultSet.getInt("SLAB_NO");
			}
			close(resultSet,pStmt);
			
			System.out.println("#### PRICE_LIST "+priceList);
			System.out.println("#### itemCode "+itemCode);
			System.out.println("#### listType "+listType);
			System.out.println("#### MAX Slab No "+slabNo);
			
			String validUptoSql = "SELECT * FROM PRICELIST WHERE PRICE_LIST = ? AND ITEM_CODE = ? AND LIST_TYPE = ? and slab_no = ?";
			sPstmt = connection.prepareStatement(validUptoSql);
			sPstmt.setString(1, priceList);
			sPstmt.setString(2, itemCode);
			sPstmt.setString(3, listType);
			sPstmt.setInt(4, slabNo);
			sRst = sPstmt.executeQuery();
			if(sRst.next())
			{
				System.out.println("#### In result Set ...");
				validDate = sRst.getTimestamp("VALID_UPTO");
				//listType = sRst.getString("LIST_TYPE");
				lotNoFrom = sRst.getString("LOT_NO__FROM");
				lotNoTo = sRst.getString("LOT_NO__TO");
				minQty = sRst.getString("MIN_QTY");
				maxQty = sRst.getString("MAX_QTY");
				rate = sRst.getString("RATE");
				minRate = sRst.getString("MIN_RATE");
				maxRate = sRst.getString("MAX_RATE");
				rateType = sRst.getString("RATE_TYPE");
				orderType = sRst.getString("ORDER_TYPE");
				priceListParent = sRst.getString("PRICE_LIST__PARENT");
				calcBasis = sRst.getString("CALC_BASIS");
				refNo = sRst.getString("REF_NO");
				refOldNo = sRst.getString("REF_NO_OLD");
				unit = sRst.getString("UNIT");
			}
			System.out.println("#### After result Set ...");
			close(sRst,sPstmt);
			try
			{
				System.out.println("#### In try  ...");
				Calendar cal = Calendar.getInstance();
				System.out.println("#### 111111  ...");
				Calendar currDate = Calendar.getInstance();
				System.out.println("#### 222222  ...");
				currDate.setTimeInMillis(new Date().getTime());
				System.out.println("#### 333333  ...");
				currDate.set(Calendar.HOUR_OF_DAY, 0);
				System.out.println("#### 44444  ...");
				currDate.set(Calendar.HOUR, 0);
				System.out.println("#### 55555  ...");
				System.out.println("currDate : "+currDate.getTime());
				System.out.println("#### In 6666  ...");
				cal.setTimeInMillis(validDate.getTime());
				System.out.println("#### In 77777  ...");
				cal.add(Calendar.DAY_OF_MONTH, -1);
				System.out.println("#### In 8888...");
				validDate = new Timestamp(cal.getTime().getTime());
				System.out.println("#### validDate After: "+validDate);
				
			} catch (Exception e) 
			{
				System.out.println("#### Exception in Calendar "+e);
				e.printStackTrace();
			}
		    
		    String updateSql = "UPDATE PRICELIST SET VALID_UPTO = ? WHERE PRICE_LIST = ? AND ITEM_CODE = ? AND LIST_TYPE = ? AND SLAB_NO = ?";
		    PreparedStatement updatePreStmt = connection.prepareStatement(updateSql);
		    updatePreStmt.setTimestamp(1, validDate);
		    updatePreStmt.setString(2, priceList);
		    updatePreStmt.setString(3, itemCode);
		    updatePreStmt.setString(4, listType);
		    updatePreStmt.setInt(5, slabNo);
		    int updatedCnt = updatePreStmt.executeUpdate();
		    
		    System.out.println(" ##### updatedCnt : "+updatedCnt);
		    System.out.println(" ##### Update Query : "+updatePreStmt);
		    
		    if(updatePreStmt != null) 
		    {
		    	updatePreStmt.close();
		    	updatePreStmt =null;
		    }
		    Timestamp effDateTime = getDate(effDate);
		    Timestamp validUptoTime = getDate(validUpto);
		    System.out.println("###### Creating insert Query ");
		    System.out.println("###### effDateTime "+effDateTime);
		    System.out.println("###### validUptoTime "+validUptoTime);
		    
		    String insertPriceListSql = "INSERT INTO PRICELIST(PRICE_LIST,ITEM_CODE,UNIT,LIST_TYPE,SLAB_NO,EFF_FROM,VALID_UPTO,LOT_NO__FROM,LOT_NO__TO,MIN_QTY,MAX_QTY,RATE,RATE_TYPE,MIN_RATE,CHG_DATE, CHG_USER, CHG_TERM, MAX_RATE, ORDER_TYPE,CHG_REF_NO,PRICE_LIST__PARENT,CALC_BASIS,REF_NO,REF_NO_OLD)"
		    		+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		    PreparedStatement insertPstmt = connection.prepareStatement(insertPriceListSql);
		    insertPstmt.setString(1, priceList);
		    insertPstmt.setString(2, itemCode);
		    insertPstmt.setString(3, unit);
		    insertPstmt.setString(4, listType);
		    insertPstmt.setInt(5, slabNo + 1);
		    insertPstmt.setTimestamp(6, effDateTime);//
		    insertPstmt.setTimestamp(7, validUptoTime);//
		    insertPstmt.setString(8, lotNoFrom);
		    insertPstmt.setString(9, lotNoTo);
		    insertPstmt.setString(10, minQty);
		    insertPstmt.setString(11, maxQty);
		    insertPstmt.setString(12, revisedRate);
		    insertPstmt.setString(13, rateType);
		    insertPstmt.setString(14, minRate);
		    insertPstmt.setTimestamp(15, validDate);// ?
		    insertPstmt.setString(16, chgTerm);
		    insertPstmt.setString(17, chgUser);
		    insertPstmt.setString(18, maxRate);
		    insertPstmt.setString(19, orderType);
		    insertPstmt.setString(20, chgRefNo);
		    insertPstmt.setString(21, priceListParent);
		    insertPstmt.setString(22, calcBasis);
		    insertPstmt.setString(23, refNo);
		    insertPstmt.setString(24, refOldNo);
		    
		    int insertRecord = insertPstmt.executeUpdate();
		    
		    System.out.println("###### No of record Inserted" +insertRecord);
		    System.out.println("###### Insert Query" +insertPstmt);
		  
		    if(updatedCnt > 0 || insertRecord > 0 )
		    {
		    	System.out.println("##### Connection Commited...");
		    	connection.commit();//To commit transactions. 
		    }
		    if(insertPstmt != null) 
		    {
				insertPstmt.close();
				insertPstmt = null;
		    }
		    
		    
		} catch (SQLException e)
		{
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		finally
		{
			try
			{
				if(connection != null) 
				{
					connection.close();
					connection = null;
				}
			} catch (SQLException e) 
			{
			
				e.printStackTrace();
			}
		}
		
	}
	//WrkFlw...
	public String rejectRate(String quotNo, String userInfo, String xmlData) throws ITMException 
	{

		System.out.println("###### New Rate Rejeced .....");
		
		ConnDriver connDriver = new ConnDriver();
		Connection connection = null;
		PreparedStatement pstmt = null;
		//ResultSet resultSet = null;
		String sqlStr = "UPDATE SALES_QUOT SET WF_STATUS = ? WHERE QUOT_NO = ?";
		
		try
		{
			UserInfoBean userInfoBean = new UserInfoBean(userInfo);
			String transDB = userInfoBean.getTransDB();
			connection = connDriver.getConnectDB(transDB);
			connection.setAutoCommit(false);
			
			pstmt = connection.prepareStatement(sqlStr);
			pstmt.setString(1, "R");
			pstmt.setString(2, quotNo);
			
			int updateCount = pstmt.executeUpdate();
			if(updateCount > 0) 
			{
				connection.commit();
			}
			System.out.println("#### Updating transetup No of rows "+updateCount);
			if(pstmt != null) 
			{
				pstmt.close();
				pstmt = null;
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		finally 
		{
			if(connection != null)
			{
				try 
				{
					connection.close();
					connection = null;
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
		return "1";
	}
	private String getCommission(String custCode, Connection connection) throws ITMException 
	{
		PreparedStatement pStmt = null;
		ResultSet resultSet = null;
		String commTable = null;
		String commSql = "SELECT COMM_TABLE__1, COMM_TABLE__3 FROM CUSTOMER WHERE CUST_CODE = ?";
		try 
		{
			pStmt = connection.prepareStatement(commSql);
			pStmt.setString(1, custCode);
			resultSet = pStmt.executeQuery();
			if(resultSet.next())
			{
				commTable = resultSet.getString("COMM_TABLE__1");
				if(E12GenericUtility.checkNull(commTable).length() <= 0 )
				{
					commTable = resultSet.getString("COMM_TABLE__3");
				}
			}
			close(resultSet,pStmt);
			
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return commTable;
	
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
			return 0.0;
		}
		return retVal;
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
			close(rs,pstmt);
			
		}
		catch(Exception e)
		{
			System.out.println("@@@@@ Exception in getAdditionalCost : "+e);
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return additionalCost;
	}
	
	/*
	 * This method take double value as a input.
	 * Apply format as provided then converted into double value.
	 * At last it return the value as a decimal format  
	 */
	private String getCommaSep(double num) 
	{
		String str = String.format("%.2f", num);
		double doubleVal = Double.parseDouble(str);
		System.out.println("@@@@@@@@@@@@@ Formated String ["+str);
		DecimalFormat dbf = new DecimalFormat();
		return dbf.format(doubleVal);
	}
	
	private double getRate(Connection conn, String priceList,String itemCode)  throws ITMException, SQLException
	{
		double rate = 0.0;
		String listType = "";
		String sql ="";
		String siteCode = "";
		String locCode = "";
		String lotSl = "";
		PreparedStatement pstmt = null;
		PreparedStatement pstmt2 = null;
		ResultSet rs = null;
		ResultSet rs2 = null;
		ResultSet rs3 = null;
		String priceListParent = "";
		
		System.out.println("##### Calling getRate priceList "+priceList);
		String sqlStr = "SELECT LIST_TYPE FROM PRICELIST_MST WHERE PRICE_LIST = ?";
		try
		{
			pstmt = conn.prepareStatement(sqlStr);
			pstmt.setString(1, priceList);
			rs = pstmt.executeQuery();
			while(rs.next()) 
			{
				listType = rs.getString(1);
			}
			close(rs,pstmt);
			
			if(listType != null) 
			{
				//String slabNo = "SELECT MAX(SLAB_NO) FROM PRICELIST WHERE PRICE_LIST = "+priceList+" and ITEM_CODE = "+itemCode+" AND LIST_TYPE = "+listType+"";
				int slabNo = getMaxSlabNo(conn, priceList, itemCode, listType);
				
				if(listType.trim().equals("L")) 
				{
					
					try
					{
						String sqlQuery = "SELECT RATE  FROM PRICELIST WHERE PRICE_LIST = ? and ITEM_CODE = ? AND LIST_TYPE = ? AND SLAB_NO = ?";
						
						pstmt = conn.prepareStatement(sqlQuery);
						pstmt.setString(1, priceList);
						pstmt.setString(2, itemCode);
						pstmt.setString(3, listType);
						pstmt.setInt(4, slabNo);
						rs = pstmt.executeQuery();
						
						if( rs.next())
						{
							rate = rs.getDouble("RATE");
							close(rs,pstmt);
						} 
						else 
						{
							close(rs,pstmt);
							String sqlStr2 = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END) FROM pricelist_mst WHERE PRICE_LIST = ?";
							pstmt = conn.prepareStatement(sqlStr2);
							pstmt.setString(1, priceList);
							rs = pstmt.executeQuery();
							
							while(rs.next()) 
							{
								priceListParent = rs.getString(1) == null ? "" : rs
										.getString(1);
								if (priceListParent.trim().length() > 0)
								{
									try
									{
										sql = "SELECT RATE FROM PRICELIST "
												+ "WHERE PRICE_LIST = '"
												+ priceListParent + "' "
												+ "AND ITEM_CODE = '" + itemCode
												+ "' " + "AND LIST_TYPE = ? "
												+ "AND SLAB_NO <= ? ";
												
										pstmt2 = conn.prepareStatement(sql);
										pstmt2.setString(1, listType);
										pstmt.setInt(2, slabNo);
										rs2 = pstmt2.executeQuery();
										
										if (rs2.next())
										{
											rate = rs2.getDouble(1);
											System.out.println("#### 2552 Rate" + rate);
											close(rs2,pstmt);
										} 
										else
										{
											close(rs2,pstmt);
											return -1;
										}

										if (rate > 0)
										{
											priceList = priceListParent;
										}
										else
										{
											priceList = priceListParent;
											priceListParent = "";
										}

									}
									catch (Exception e)
									{
										e.printStackTrace();
										throw new ITMException(e);
									}
								}

							}
							close(rs,pstmt);
						}
					} catch (Exception e)
					{
						e.printStackTrace();
					}
										
				}
				else if(listType.trim().equals("F")) 
				{
					try
					{
						sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '"
								+ priceList + "' " + "AND ITEM_CODE = '" + itemCode
								+ "' " + "AND LIST_TYPE = ? " + "AND  SLAB_NO =?";
						close(rs,pstmt);		
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, listType);
						pstmt.setInt(2, slabNo);
						rs = pstmt.executeQuery();
						
						if (rs.next())
						{
							rate = rs.getDouble(1);
							System.out.println("#### Rate" + rate);
							close(rs2,pstmt);
						}
						else
						{

							try
							{
								close(rs2,pstmt);
								sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END) "
										+ "FROM pricelist_mst WHERE PRICE_LIST = '"
										+ priceList + "' ";
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								while (rs.next())
								{
									priceListParent = rs.getString(1);
								}
								close(rs,pstmt);
								if ((priceListParent == null) || (priceListParent.trim().length() == 0))
								{
									priceListParent = "";
									return -1;
								}
								if (priceListParent.trim().length() > 0)
								{
									try
									{
										sql = "SELECT RATE FROM PRICELIST "
												+ "WHERE PRICE_LIST = '"
												+ priceListParent + "' "
												+ "AND ITEM_CODE = '" + itemCode + "' "
												+ "AND LIST_TYPE = ? "
												+ "AND SLAB_NO = ? ";
												
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, listType);
										pstmt.setInt(2, slabNo);
										
										rs2 = pstmt.executeQuery();
										if (rs2.next())
										{
											rate = rs2.getDouble(1);
											close(rs2,pstmt);
										}
										else
										{
											close(rs2,pstmt);
											return -1;
										}
									}
									catch (Exception e)
									{
										System.out.println("#### Exception...[getRate] "
												+ sql + e.getMessage());
										e.printStackTrace();
										throw new ITMException(e);
									}
								}
							}
							catch (Exception e)
							{
								System.out.println("#### Exception...[getRate] " + sql+ e.getMessage());
								e.printStackTrace();
								throw new ITMException(e);
							}
						}

						close(rs,pstmt);
					} 
					catch (Exception e)
					{
						System.out.println("#### Exception...[getRate] " + sql+ e.getMessage());
						e.printStackTrace();
						throw new ITMException(e);
					}
				
				}
				else if(listType.trim().equals("D")) 
				{
					try
					{
						sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '"
								+ priceList + "' " + "AND ITEM_CODE = '" + itemCode
								+ "' " + "AND LIST_TYPE = ? " + "AND SLAB_NO = ? ";
						close(rs,pstmt);		
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, listType);
						pstmt.setInt(2, slabNo);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							rate = rs.getDouble(1);
							close(rs,pstmt);
						}
						else
						{
							try
							{
								close(rs,pstmt);
								sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END) "
										+ "FROM pricelist_mst WHERE PRICE_LIST = '"
										+ priceList + "' ";
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									priceListParent = rs.getString(1);
									System.out.println("The priceListParent is .... "
											+ priceListParent);
								}
								rs.close();
								pstmt.close();
								rs = null;
								pstmt = null;
								if ((priceListParent == null) || (priceListParent.trim().length() == 0)) {
									priceListParent = "";
									return -1;
								}
								if (priceListParent.trim().length() > 0)// 1
								{
									try
									{
										sql = "SELECT RATE FROM PRICELIST "
												+ "WHERE PRICE_LIST = '"
												+ priceListParent + "' "
												+ "AND ITEM_CODE = '" + itemCode + "' "
												+ "AND LIST_TYPE = ? "
												+ "AND SLAB_NO = ? ";
												
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, listType);
										pstmt.setInt(2, slabNo);
										
										rs2 = pstmt.executeQuery();
										if (rs2.next()) {
											rate = rs2.getDouble(1);
											System.out.println("#### 2688 Rate:" + rate);
											close(rs2,pstmt);
										}
										else
										{

											try
											{
												close(rs2,pstmt);
												sql = "SELECT RATE FROM PRICELIST "
														+ "WHERE PRICE_LIST = '"
														+ priceList + "' "
														+ "AND ITEM_CODE = '"
														+ itemCode
														+ "' "
														+ "AND LIST_TYPE = ? "
														+ "AND SLAB_NO = ? ";
														
												pstmt = conn.prepareStatement(sql);
												pstmt.setString(1, listType);
												pstmt.setInt(2, slabNo);
												
												rs2 = pstmt.executeQuery();
												
												if (rs2.next())
												{
													rate = rs2.getDouble(1);
													close(rs2,pstmt);
												} else {

													try {
														close(rs2,pstmt);
														sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END) "
																+ "FROM pricelist_mst WHERE PRICE_LIST = '"
																+ priceList + "' ";
														pstmt = conn
																.prepareStatement(sql);
														rs2 = pstmt.executeQuery();
														if (rs2.next()) {
															priceListParent = rs2
																	.getString(1);
														}
														close(rs2,pstmt);
														if ((priceListParent == null)
																|| (priceListParent
																		.trim()
																		.length() == 0)) {
															priceListParent = "";
															return -1;
														}
														if (priceListParent.trim()
																.length() > 0)
														{
															try {
																sql = "SELECT RATE FROM PRICELIST "
																		+ "WHERE PRICE_LIST = '"
																		+ priceListParent
																		+ "' "
																		+ "AND ITEM_CODE = '"
																		+ itemCode
																		+ "' "
																		+ "AND LIST_TYPE = ? "
																		+ "AND SLAB_NO = ? ";
																		
																pstmt = conn.prepareStatement(sql);
																pstmt.setString(1,listType);
																pstmt.setInt(2, slabNo);
																
																rs3 = pstmt.executeQuery();
																if (rs3.next())
																{
																	rate = rs3.getDouble(1);
																	close(rs3,pstmt);
																} else
																{
																	close(rs3,pstmt);
																	return -1;
																}
															} catch (Exception e)
															{
																e.printStackTrace();
																throw new ITMException(e);
															}
														}
													}
													catch (Exception e)
													{
														e.printStackTrace();
														throw new ITMException(e);
													}
												}

											} catch (Exception e) {
												e.printStackTrace();
												throw new ITMException(e);
											}
										}

									} catch (Exception e) {
										e.printStackTrace();
										throw new ITMException(e);
									}
								}

							} catch (Exception e) {
								e.printStackTrace();
								throw new ITMException(e);
							}
						}
						close(rs,pstmt);
					} catch (Exception e) {
						e.printStackTrace();
						throw new ITMException(e);
					}
				
				}
				else if(listType.trim().equals("B")) 
				{

					rate = 0;
					try
					{
						sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '"
								+ priceList + "' " + "AND ITEM_CODE = '" + itemCode
								+ "' " + "AND LIST_TYPE = ? "
								+ "AND SLAB_NO = ? ";
						close(rs,pstmt);
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, listType);
						pstmt.setInt(2, slabNo);
						rs = pstmt.executeQuery();
						
						if (rs.next())
						{
							rate = rs.getDouble(1);
							close(rs,pstmt);
						} 
						else
						{
							try
							{
								close(rs,pstmt);
								sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END) "
										+ "FROM pricelist_mst WHERE PRICE_LIST = '"
										+ priceList + "' ";
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									priceListParent = rs.getString(1);
									close(rs,pstmt);
									if ((priceListParent == null)
											|| (priceListParent.trim().length() == 0))
									{
										priceListParent = "";
									}
									if (priceListParent.trim().length() > 0)
									{
										try
										{
											sql = "SELECT RATE FROM PRICELIST "
													+ "WHERE PRICE_LIST = '"
													+ priceListParent + "' "
													+ "AND ITEM_CODE = '" + itemCode
													+ "' "
													+ "AND LIST_TYPE = ? "
													+ "AND SLAB_NO = ? ";
													
											pstmt = conn.prepareStatement(sql);
											pstmt.setString(1, listType);
											pstmt.setInt(2, slabNo);
											
											rs2 = pstmt.executeQuery();
											if (rs2.next())
											{
												rate = rs2.getDouble(1);
												close(rs2,pstmt);
											}
											else
											{
												close(rs2,pstmt);
												return -1;
											}
											if (rate > 0)
											{
												priceList = priceListParent;
											}
											else
											{
												priceList = priceListParent;
												priceListParent = "";
											}
										}
										catch (Exception e)
										{
											e.printStackTrace();
											throw new ITMException(e);
										}
									}
								}

								else
								{
									close(rs,pstmt);
								}
							}
							catch (Exception e)
							{
								e.printStackTrace();
								throw new ITMException(e);
							}
							finally
							{
								close(rs,pstmt);
							}
						}

					}
					catch (Exception e)
					{

						e.printStackTrace();
						throw new ITMException(e);
					}
					
				}
				else if((listType.trim().equals("M")) || (listType.trim().equals("N"))) 
				{
					System.out.println("Inside type ::-<M><N>-::");
					try
					{
						sql = "SELECT RATE FROM PRICELIST " + "WHERE PRICE_LIST = '"
								+ priceList + "' " + "AND ITEM_CODE = '" + itemCode
								+ "' " + "AND LIST_TYPE = ? "+ "AND SLAB_NO = ? " ;
						
						close(rs,pstmt);
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, listType);
						pstmt.setInt(2, slabNo);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							rate = rs.getDouble(1);
							close(rs,pstmt);
						} else 
						{
							try
							{
								close(rs,pstmt);
								sql = "SELECT (CASE WHEN PRICE_LIST__PARENT IS NULL THEN '' ELSE PRICE_LIST__PARENT END) "
										+ "FROM pricelist_mst WHERE PRICE_LIST = '"
										+ priceList + "' ";
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									priceListParent = rs.getString(1);
								}
								close(rs,pstmt);
								if ((priceListParent == null) || (priceListParent.trim().length() == 0))
								{
									priceListParent = "";
									return -1;
								}
								if (priceListParent.trim().length() > 0)
								{
									
									sql = "SELECT LIST_TYPE FROM PRICELIST_MST WHERE PRICE_LIST = '"
											+ priceListParent + "' ";
									pstmt = conn.prepareStatement(sql);
									rs2 = pstmt.executeQuery();
									if (rs2.next())
									{
										listType = rs2.getString(1);
										close(rs2,pstmt);
									}

									try
									{
										sql = "SELECT RATE FROM PRICELIST "
												+ "WHERE PRICE_LIST = '"
												+ priceListParent + "' "
												+ "AND ITEM_CODE = '" + itemCode
												+ "' "
												+ "AND LIST_TYPE = ? " 
												+ "AND SLAB_NO = ? ";
												
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, listType);
										pstmt.setInt(2, slabNo);
										rs2 = pstmt.executeQuery();
										
										if (rs2.next())
										{
											rate = rs2.getDouble(1);
											close(rs2,pstmt);
										}
										else
										{
											close(rs2,pstmt);
											return -1;
										}
									}
									catch (Exception e)
									{
										System.out.println("@@@@@@ Exception getRate : "+ sql + e.getMessage());
										e.printStackTrace();
										throw new ITMException(e);
									}
								}
							} catch (Exception e)
							{
								System.out.println("@@@@@@ Exception getRate : " + sql+ e.getMessage());
								e.printStackTrace();
								throw new ITMException(e);
							}
						}

					} catch (Exception e) 
					{
						System.out.println("@@@@@@@ Exception getRate  " + sql
								+ e.getMessage());
						e.printStackTrace();
						throw new ITMException(e);
					}
				
				}
				else if(listType.trim().equals("I")) 
				{

					
					if ((lotSl == null) || (lotSl.trim().length() == 0))
					{
						System.out.println("Inside type ::-<I>-::");
						try
						{
							sql = "SELECT RATE FROM STOCK " + "WHERE ITEM_CODE = '"
									+ itemCode + "' " + "AND SITE_CODE = '" + siteCode + "' " + "AND LOC_CODE = '" + locCode + "' ";
							close(rs,pstmt);
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								rate = rs.getDouble(1);
								System.out.println("Rate is .*...." + rate);
							}
							close(rs,pstmt);
						}
						catch (Exception e)
						{
							System.out.println("Exception...[getRate] " + sql
									+ e.getMessage());
							e.printStackTrace();
							throw new ITMException(e);
						}
					}
					else
					{
						try
						{
							sql = "SELECT RATE FROM STOCK " + "WHERE ITEM_CODE = '"
									+ itemCode + "' " + "AND SITE_CODE = '" + siteCode
									+ "' " + "AND LOC_CODE = '" + locCode + "' "
									+ "AND LOT_SL = '" + lotSl + "'";
							close(rs,pstmt);
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								rate = rs.getDouble(1);
							}
							close(rs,pstmt);
						} catch (Exception e)
						{
							System.out.println("Exception...[getRate] " + sql
									+ e.getMessage());
							e.printStackTrace();
							throw new ITMException(e);
						}
					}
				
				}
				
			}
			
			
		} catch (SQLException e)
		{
			System.out.println("@@@@@ Exception in getRate "+e);
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		
		
		return rate;
	}
	
	private int getMaxSlabNo(Connection conn, String priceList, String itemCode, String listType) throws ITMException 
	{
		int slabNo = 0;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "SELECT MAX(SLAB_NO) AS SLAB_NO FROM PRICELIST WHERE PRICE_LIST = ? AND ITEM_CODE = ? AND LIST_TYPE = ?";
		try
		{
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,priceList);
			pstmt.setString(2,itemCode);
			pstmt.setString(3,listType);
			rs = pstmt.executeQuery();
			if( rs.next())
			{
				slabNo = rs.getInt("SLAB_NO");
				System.out.println("####### IN IF SLAB NO "+slabNo);
			}
			close(rs,pstmt);
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		close(rs,pstmt);
		System.out.println("####### SLAB NO "+slabNo);
		return slabNo;
		
		
	}
	private void close(ResultSet rs,PreparedStatement pstmt) 
	{
		try 
		{
			if(rs != null) 
			{
				rs.close();
				rs = null;
			}
			if( pstmt != null)
			{
				pstmt.close();
				pstmt = null;
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	//Added on 09-10-18 To calculate Total Summary.Below code get data for form no2 START
	
	// private Map<String,LinkedList<Double> > summaryProfitMap = null;
	
	private JSONObject finalSummaryProfitMap = null;
	
	public JSONObject getGoodsMap(String custCode, String fromDateStr, String toDateStr) throws ITMException
	{
		//
		srNoSet = new HashSet();
		finalSummaryProfitMap = new JSONObject();
		JSONObject summaryGoodsMap = new JSONObject();
		
		Connection conn = null;
		ConnDriver connDriver = null;
		
		try {
			connDriver = new ConnDriver();
			conn = getConnection();
			
			Timestamp toDate = null, fromDate = null;
			
			toDate = Timestamp.valueOf(genericUtility.getValidDateString(toDateStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())
					+ " 00:00:00.0");
			fromDate = Timestamp.valueOf(genericUtility.getValidDateString(fromDateStr,genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat())
					+ " 00:00:00.0");

			
			
			PreparedStatement pstmt = null;
			PreparedStatement pstmt1 = null;
			ResultSet rs = null;
			ResultSet rs1 = null;
			String custmerBill  = "";
			ArrayList<String> invoiceArr = new ArrayList<String>();
			Map<String, Double> map = new HashMap<String, Double>();
			String disPriceList = E12GenericUtility.checkNull(getDBColumnValue("CUSTOMER", "PRICE_LIST__DISC", "CUST_CODE = '"+custCode+"'", conn));
			String priceList = E12GenericUtility.checkNull(getDBColumnValue("CUSTOMER", "PRICE_LIST", "CUST_CODE = '"+custCode+"'", conn));
			
			String commTable = getCommission(custCode, conn);
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			String eventDateStr = "";
			
			Calendar calObj1 = Calendar.getInstance();
			calObj1.setTime(new java.util.Date());
			java.util.Date dateFrom = calObj1.getTime();
			eventDateStr = sdf.format(dateFrom);
			
			String currDateStr = genericUtility.getValidDateString(eventDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
			String qq = genericUtility.getValidDateTimeString(eventDateStr, genericUtility.getApplDateFormat(), genericUtility.getDBDateFormat());
			Timestamp currDate = java.sql.Timestamp.valueOf(currDateStr + " 00:00:00");
			
			custmerBill = "SELECT * FROM INVOICE WHERE CUST_CODE = ? AND TRAN_DATE >= ? AND TRAN_DATE <= ? AND INV_TYPE NOT IN ('DF','EF')";
			try
			{
				
				pstmt = conn.prepareStatement(custmerBill);
				pstmt.setString(1, custCode);
				pstmt.setTimestamp(2, fromDate);
				pstmt.setTimestamp(3, toDate);
				
				rs = pstmt.executeQuery();
				while(rs.next())
				{
					String invoiceId = checkNull(rs.getString("invoice_id")).trim();
					//System.out.println("#### INVOICE ID :: "+invoiceId);
					String invocieData = "SELECT * FROM INVOICE_TRACE WHERE INVOICE_ID = ? ";
					try {
						pstmt1 = conn.prepareStatement(invocieData);
						pstmt1.setString(1, invoiceId);
						rs1 = pstmt1.executeQuery();
						while(rs1.next())
						{
							if(map.containsKey(rs1.getString("item_code")))
							{
								double i = map.put(rs1.getString("item_code"), Double.parseDouble(rs1.getString("quantity")));
								System.out.println(i);
								map.put(rs1.getString("item_code"), i + Double.parseDouble(rs1.getString("quantity")) );
							}
							else
							{
								map.put(rs1.getString("item_code"), Double.parseDouble(rs1.getString("quantity")));
							}
						}
						close(rs1,pstmt1);
						
					} catch (Exception e) 
					{
						System.out.println("@@@@@ Exception in SalesQuotProposal :: getInvoiceId() :  "+e);
					}
					invoiceArr.add(invoiceId);
				}
				close(rs,pstmt);
				
			}
			catch(Exception e)
			{
				e.printStackTrace();
				System.out.println("SalesQuotProposal.getInvoiceId() "+e);
				throw new ITMException(e);
			}
			
			String domID = "1";
			
			Iterator<Map.Entry<String, Double>> itr = map.entrySet().iterator();
				        
			int srNo = 0;
			
			DistCommon distCommon = new DistCommon();
			
			Map<String, String> itemDetailMap = new HashMap<String, String>();
      
			while(itr.hasNext())
			{
				srNo++;
				srNoSet.add(srNo);
			    Map.Entry<String, Double> entry = itr.next();
			    
			    String itemCode = E12GenericUtility.checkNull(entry.getKey());
			    itemCodeArr2.add(itemCode);
			    double maxValue = entry.getValue();
			    itemDetailMap = getItemData(itemCode, conn);
			   
			    //double discount  = getRate(conn, disPriceList, itemCode);
			    double discount = (distCommon.pickRate(disPriceList, eventDateStr, entry.getKey(), conn));
			    double rate  = getRate(conn, priceList, itemCode);
				
				Double grossRate = ((rate - ((rate * discount) / 100)));
				if(grossRate.isNaN() || grossRate.isInfinite())
				{
					grossRate = 0.0;
				}
				double comm = 0;
				try
				{
					comm = getCommision(commTable,currDate,conn,itemCode);
				}
				catch(Exception e) 
				{
					comm = 0;
				}
				
				double commPerUnit = (((grossRate * comm) / 100));
				double newCostRate = ((grossRate - commPerUnit));
				double costRate = getCostRate(conn, itemCode);
				//double availblStock = getStockDetails(conn,itemCode);
				double profit = (newCostRate - costRate);
				String profitParse = String.format("%.2f", profit);
				double profitOnGoods = (Double.parseDouble(profitParse) * maxValue);
				double finalPrice = (grossRate * maxValue);
				
				String str = String.format("%.2f", profitOnGoods);
				double doubleVal = Double.parseDouble(str);
				summaryGoodsMap.put(itemCode, doubleVal);
				//profitOnGoodsList.add(doubleVal);
				
				String str2 = String.format("%.2f", finalPrice);
				double doubleVal2 = Double.parseDouble(str2);
				finalSummaryProfitMap.put(itemCode, doubleVal2);
				//finalPriceList.add(doubleVal2);
				
				
				System.out.println("@@@@@@@@ profitOnGoods ["+doubleVal+"] finalPrice["+doubleVal2+"]");
				
			}
			
			
		} catch (Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		finally
		{
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException ex) {}
			}
		}
		System.out.println("@@@@@@@ summaryGoodsMap ["+summaryGoodsMap+"]");
		//
		
		return summaryGoodsMap;
		
	}
	
	public JSONObject getFinalMap()
	{
		return finalSummaryProfitMap;
	}
	//Added on 09-10-18 To calculate Total Summary.Below code get data for form no2 END
	private String getLineNo(Document docu)
	{
		//String srNo = "";
		NodeList parentNList = docu.getElementsByTagName("Detail2");
		int nListLen = parentNList.getLength();
		System.out.println("####Detail2 Lenght "+nListLen);
		
		return String.valueOf(nListLen);
		
	}
	//Logic 2 
	private String getDomId(Document docu) 
	{
		NodeList parentNList = docu.getElementsByTagName("Detail2");
		int nListLen = parentNList.getLength();
		System.out.println("#### Detail2 Lenght @@@@@@@@"+nListLen);
		
		Node node = null;  
		//Element elem = null;
		
		for(int i = 0; i < nListLen; i++)
		{
			node = parentNList.item(i);
			if(node.getAttributes().getNamedItem( "domID" ) != null) 
			{
				Node n = node.getAttributes().getNamedItem( "domID" );
				if(n != null) 
				{
					String domID = n.getNodeValue();
					System.out.println("#### DOM ID "+domID);
					if(srNoSet != null) 
					{
						srNoSet.add(Integer.parseInt(domID));
					}
				}
			}
		}
		return "";
		
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
}