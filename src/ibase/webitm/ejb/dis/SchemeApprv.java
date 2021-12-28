package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;

public class SchemeApprv extends ValidatorEJB 
{
	E12GenericUtility genericUtility=new E12GenericUtility();
	public String wfValData() throws RemoteException, ITMException
	{
		return "";
	}

	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		System.out.println("*************schemeApprv wfValData(String xmlString, String xmlString1, String objContext, String editFlag, String xtraParams)********");
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
		String sql = "",errString = "", errCode  = "",userId = "";
		//Added by saiprasad G. for the secondary scheme validation[START]
		String effFrom = "",effUpto = "", itemCode = "", itemCodeRepl = "", quantity = "", freeQty = "", rate = "", amt = "";
		Timestamp fromDate = null ,toDate =null;
		//Added by saiprasad G. for the secondary scheme validation[END]
		ArrayList <String> errList = new ArrayList<String>();
		ArrayList <String>errFields = new ArrayList <String> ();
		String errorType = "";
		String custCode = "",salesPeron = "",amount = "",schemeCode = "";
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
						if (childNodeName.equalsIgnoreCase("cust_code"))
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
						else if(childNodeName.equalsIgnoreCase("sales_pers"))
						{
							salesPeron=E12GenericUtility.checkNull(genericUtility.getColumnValue("sales_pers", dom));
							System.out.println("salesPerson:"+salesPeron);
							if(E12GenericUtility.checkNull(salesPeron).length() <= 0)
							{
								errCode = "VTSLPER1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
							else
							{
								String str = "select count(1) from sales_pers where sales_pers = ?";
								int count = 0;
								pstmt = conn.prepareStatement(str);
								pstmt.setString(1, salesPeron);
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
									errCode = "VMSLPERS1";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									break;	
								}
							}
						}
						else if (childNodeName.equalsIgnoreCase("cust_code__bill"))
						{
							custCode = E12GenericUtility.checkNull(genericUtility.getColumnValue("cust_code__bill", dom));
							System.out.println("Stockiest code:"+custCode);
							if(E12GenericUtility.checkNull(custCode).length() <= 0)
							{
								errCode = "VTSTCKBLNK";
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
									errCode = "VMSTCK";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									break;
								}
							}
						}
						else if(childNodeName.equalsIgnoreCase("scheme_code"))
						{
							schemeCode=E12GenericUtility.checkNull(genericUtility.getColumnValue("scheme_code", dom));
							System.out.println("Scheme code:"+schemeCode);
							int count =0;
							if(schemeCode.length() <= 0)
							{
								errCode = "VTSCHEME";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
							else if(editFlag.equalsIgnoreCase("A"))
							{
								String str = "select count(1) from scheme_apprv where scheme_code=?";
								pstmt = conn.prepareStatement(str);
								pstmt.setString(1, schemeCode);
								rs = pstmt.executeQuery();
								while(rs.next())
								{
									count = rs.getInt(1);
								}
								System.out.println("count of the scheme apprv:"+count);
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if(count == 1)
								{
									errCode = "VTDUPSCHEM";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
									break;
								}
							}
						}
						//Added by saiprasad G. for the validating the 
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
							
						//Commented by Saiprasad G. for the adding the amount in second form [START]
						/*else if(childNodeName.equalsIgnoreCase("amount"))
						{
							amount=E12GenericUtility.checkNull(genericUtility.getColumnValue("amount", dom));
							System.out.println("amount:"+amount);
							if(amount.length() <= 0)
							{
								errCode = "VTAMOUNT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
							int amt=Integer.parseInt(amount);
							if(amt<=0)
							{
								errCode = "VMAMOUNT";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
								break;
							}
						}*/
						//Commented by Saiprasad G. for the adding the amount in second form [END]
					} 
				}
				break;
				//Added by saiprasad for secondary scheme validation[START]
			case 2:
			      parentNodeList = dom.getElementsByTagName("Detail2");
			      parentNodeListLen = parentNodeList.getLength();
			      for(int rowNum=0;rowNum<parentNodeListLen;rowNum++)
			      {
			    	  parentNode = parentNodeList.item(rowNum);
			    	  childNodeList = parentNode.getChildNodes();
			    	  childNodeListLength = childNodeList.getLength();
			    	  for(int rowCount=0;rowCount<childNodeListLength;rowCount++)
			    	  {
			    		  childNode = childNodeList.item(rowCount);
			    		  childNodeName = childNode.getNodeName();
			    		  System.out.println("childNodeName are:"+childNodeName);
			    		  String nature = genericUtility.getColumnValue("nature", dom2, "1");
			    		  System.out.println("nature:"+nature);
			    		  if(nature.equalsIgnoreCase("Q"))
			    		  {
			    			  if(childNodeName.equalsIgnoreCase("item_code"))
			    			  {
			    				  itemCode = genericUtility.getColumnValue("item_code", dom);
			    				  System.out.println("itemCode:"+itemCode);
			    				  if(E12GenericUtility.checkNull(itemCode).length()<=0)
			    				  {
			    					  errCode = "VTITMNUL";
			    					  errList.add(errCode);
			    					  errFields.add(childNodeName.toLowerCase());
			    					  break;	
			    				  }
			    				  else
			    				  {
			    					  String str = "select count(1) from item where item_code=?";
			    					  int count=0;
			    					  pstmt = conn.prepareStatement(str);
			    					  pstmt.setString(1, itemCode);
			    					  rs = pstmt.executeQuery();
			    					  while(rs.next())
			    					  {
			    						  count = rs.getInt(1);
			    					  }
			    					  rs.close();
			    					  rs = null;
			    					  pstmt.close();
			    					  pstmt = null;
			    					  System.out.println("count of the item code:"+count);
			    					  if(count==0)
			    					  {	
			    						  errCode = "VTINVITM";
			    						  errList.add(errCode);
			    						  errFields.add(childNodeName.toLowerCase());
			    						  break;	
			    					  }
			    				  }
			    			  }
			    			  else if(childNodeName.equalsIgnoreCase("quantity"))
			    			  {
			    				  quantity = genericUtility.getColumnValue("quantity", dom);
			    				  int qty = 0;
			    				  if(E12GenericUtility.checkNull(quantity).length()<=0)
			    				  {
			    					  errCode = "INVQTY";
			    					  errList.add(errCode);
			    					  errFields.add(childNodeName.toLowerCase());
			    					  break;
			    				  }
			    				  else if(E12GenericUtility.checkNull(quantity).length()>0)
			    				  {
			    					  try
			    					  {
			    						  qty=Integer.parseInt(quantity);
			    						  System.out.println("qty:"+qty);
			    						  if(qty<=0)
			    						  {
			    							  errCode = "INVQTY";
			    							  errList.add(errCode);
			    							  errFields.add(childNodeName.toLowerCase());
			    							  break;
			    						  }

			    					  }			    	
			    					  catch (Exception e) 
			    					  {
			    						  errCode = "INVQTY";
			    						  errList.add(errCode);
			    						  errFields.add(childNodeName.toLowerCase());
			    						  break;
			    					  }
			    				  }
			    			  }
			    			  else if(childNodeName.equalsIgnoreCase("item_code__repl"))
			    			  {
			    				  itemCodeRepl = genericUtility.getColumnValue("item_code__repl", dom);
			    				  if(E12GenericUtility.checkNull(itemCodeRepl).length()<=0)
			    				  {
			    					  errCode = "VTITMREP";
			    					  errList.add(errCode);
			    					  errFields.add(childNodeName.toLowerCase());
			    					  break;	
			    				  }
			    				  else
			    				  {
			    					  String str = "select count(1) from item where item_code=?";
			    					  int count=0;
			    					  pstmt = conn.prepareStatement(str);
			    					  pstmt.setString(1, itemCodeRepl);
			    					  rs = pstmt.executeQuery();
			    					  while(rs.next())
			    					  {
			    						  count = rs.getInt(1);
			    					  }
			    					  System.out.println("count of the item code:"+count);
			    					  rs.close();
			    					  rs = null;
			    					  pstmt.close();
			    					  pstmt = null;
			    					  if(count==0)
			    					  {	
			    						  errCode = "VTINVRPL";
			    						  errList.add(errCode);
			    						  errFields.add(childNodeName.toLowerCase());
			    						  break;	
			    					  }
			    				  }
			    			  }
			    			  else if(childNodeName.equalsIgnoreCase("free_qty"))
			    			  {
			    				  freeQty = genericUtility.getColumnValue("free_qty", dom);
			    				  int freeqtyInt = 0;
			    				  if(E12GenericUtility.checkNull(freeQty).length()<=0)
			    				  {
			    					  errCode = "VTNFREEQTY";
			    					  errList.add(errCode);
			    					  errFields.add(childNodeName.toLowerCase());
			    					  break;
			    				  }
			    				  else  if(E12GenericUtility.checkNull(freeQty).length()>0)
			    				  {
			    					  try
			    					  {
			    						  freeqtyInt=Integer.parseInt(freeQty);
			    						  System.out.println("freeQty"+freeqtyInt);
			    						  if(freeqtyInt<=0)
			    						  {
			    							  errCode = "VTNFREEQTY";
			    							  errList.add(errCode);
			    							  errFields.add(childNodeName.toLowerCase());
			    							  break;
			    						  }

			    					  }			    	
			    					  catch (Exception e) 
			    					  {
			    						  errCode = "VTNFREEQTY";
			    						  errList.add(errCode);
			    						  errFields.add(childNodeName.toLowerCase());
			    						  break;
			    					  }
			    				  }
			    			  }
			    			  else if(childNodeName.equalsIgnoreCase("rate"))
			    			  {
			    				  rate = genericUtility.getColumnValue("rate", dom);
			    				  double rateInt = 0;
			    				  if(E12GenericUtility.checkNull(rate).length()>0)
			    				  {
			    					  System.out.println("rate in valiadation2"+rate);
			    					  try
			    					  {
			    						  rateInt=Double.parseDouble(rate);
			    						  System.out.println("rate"+rateInt);
			    						  if(rateInt<=0)
			    						  {
			    							  errCode = "INVRATE";
			    							  errList.add(errCode);
			    							  errFields.add(childNodeName.toLowerCase());
			    							  break;
			    						  }

			    					  }			    	
			    					  catch (Exception e) 
			    					  {
			    						  errCode = "INVRATE";
			    						  errList.add(errCode);
			    						  errFields.add(childNodeName.toLowerCase());
			    						  break;
			    					  }
			    				  }
			    				  else if(E12GenericUtility.checkNull(rate).length()<=0)
			    				  {
			    					  errCode = "INVRATE";
		    						  errList.add(errCode);
		    						  errFields.add(childNodeName.toLowerCase());
		    						  break;
			    				  }
			    			  }
			    			  else if(childNodeName.equalsIgnoreCase("amount"))
			    			  {
			    				  amount = genericUtility.getColumnValue("amount", dom);
			    				  double amtInt = 0;
			    				  if(E12GenericUtility.checkNull(amount).length()>0)
			    				  {
			    					  System.out.println("amount in valiadation3"+amount);
			    					  try
			    					  {
			    						  amtInt=Double.parseDouble(amount);
			    						  System.out.println();
			    						  if(amtInt<=0)
			    						  {
			    							  errCode = "VTAMOUNT";
			    							  errList.add(errCode);
			    							  errFields.add(childNodeName.toLowerCase());
			    							  break;
			    						  }

			    					  }			    	
			    					  catch (Exception e) 
			    					  {
			    						  errCode = "VTAMOUNT";
			    						  errList.add(errCode);
			    						  errFields.add(childNodeName.toLowerCase());
			    						  break;
			    					  }
			    				  }
			    				  else if(E12GenericUtility.checkNull(amount).length()<=0)
			    				  {
			    					  errCode = "VTAMOUNT";
			    					  errList.add(errCode);
			    					  errFields.add(childNodeName.toLowerCase());
			    					  break;
			    				  }
			    			  }
			    		  }
			    		  else if(nature.equalsIgnoreCase("V"))
			    		  {
			    			  if(childNodeName.equalsIgnoreCase("amount"))
			    			  {
			    				  amount = genericUtility.getColumnValue("amount", dom);
			    				  System.out.println("amount in string:"+amount);
			    				  double amtInt = 0;
			    				  if(E12GenericUtility.checkNull(amount).length()>0)
			    				  {
			    					  try
			    					  {
			    						  amtInt=Double.parseDouble(amount);
			    						  System.out.println();
			    						  if(amtInt<=0)
			    						  {
			    							  errCode = "VTAMOUNT";
			    							  errList.add(errCode);
			    							  errFields.add(childNodeName.toLowerCase());
			    							  break;
			    						  }

			    					  }			    	
			    					  catch (Exception e) 
			    					  {
			    						  errCode = "VTAMOUNT";
			    						  errList.add(errCode);
			    						  errFields.add(childNodeName.toLowerCase());
			    						  break;
			    					  }
			    				  }
			    				  else  if(E12GenericUtility.checkNull(amount).length()<=0)
			    				  {
			    					  errCode = "VTAMOUNT";
			    					  errList.add(errCode);
			    					  errFields.add(childNodeName.toLowerCase());
			    					  break;
			    				  }
			    			  }
			    		  }
			    		  
			    	  }
			      }
				//Added by saiprasad for secondary scheme validation[END]
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
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
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
		String sql = "";
		String columnValue = "";
		String loginSiteCode = "";
		String custName="",salesPerName="",custName1="";
		String userId="";
		//Added by saiprasad G. START
		String itmCode="",itemCodeRepl="",itmDescr="",itemReplDescr="",priceList="";
		//Added by saiprasad G. END
		Date currentDate = null;
		SimpleDateFormat sdf = null,sdfDb=null;
		Connection conn = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		StringBuffer valueXmlString = new StringBuffer();
		int currentFormNo = 0;
		E12GenericUtility genericUtility = new E12GenericUtility();
		try
		{
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			sdfDb = new SimpleDateFormat(genericUtility.getDBDateFormat());
			userId = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginCode" );
			columnValue = genericUtility.getColumnValue( currentColumn, dom );
			conn = getConnection();
			if( objContext != null && objContext.trim().length() > 0 )
			{
				currentFormNo = Integer.parseInt( objContext );
			}
			valueXmlString = new StringBuffer( "<?xml version=\"1.0\"?>\r\n<Root>\r\n<Header>\r\n<editFlag>" );
			valueXmlString.append( editFlag ).append( "</editFlag>\r\n</Header>\r\n" );
			System.out.println("currentColumn"+currentColumn);
			switch( currentFormNo )
			{
			case 1:
				valueXmlString.append( "<Detail1>\r\n" );
				if("itm_default".equalsIgnoreCase(currentColumn))
				{
					sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
					loginSiteCode = genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginSiteCode" );
					String loginCode=getValueFromXTRA_PARAMS(xtraParams,"loginCode");
					String chg_term=getValueFromXTRA_PARAMS( xtraParams, "chgTerm" );
					currentDate = new Date();
					valueXmlString.append( "<tran_date><![CDATA[" ).append(sdf.format(currentDate).toString()).append( "]]></tran_date>\r\n" );
					valueXmlString.append( "<chg_date><![CDATA[" ).append(sdf.format(currentDate).toString()).append( "]]></chg_date>\r\n" );
					valueXmlString.append( "<chg_user><![CDATA[" ).append(loginCode).append( "]]></chg_user>\r\n" );
					valueXmlString.append( "<chg_term><![CDATA[" ).append(chg_term).append( "]]></chg_term>\r\n" );
					valueXmlString.append( "<site_code><![CDATA[" ).append(loginSiteCode).append( "]]></site_code>\r\n" );
					valueXmlString.append( "<confirmed><![CDATA[" ).append("N").append( "]]></confirmed>\r\n" );
					valueXmlString.append( "<amount><![CDATA[" ).append(0).append( "]]></amount>\r\n" );
					Calendar cal = Calendar.getInstance(); 
					cal.setTime(currentDate);
					cal.add(Calendar.MONTH, 1);
					Date oneMonth = cal.getTime();
				    Timestamp oneMonthLaterTs = new Timestamp(oneMonth.getTime());
				    Date validUptoDate = new Date(oneMonthLaterTs.getTime());
				    String oneMonthLaterStr = sdf.format(validUptoDate);
				    valueXmlString.append( "<eff_from><![CDATA[" ).append(sdf.format(currentDate).toString()).append( "]]></eff_from>\r\n" );
				    valueXmlString.append( "<valid_upto><![CDATA[" ).append(oneMonthLaterStr).append( "]]></valid_upto>\r\n" );
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
				else if("sales_pers".equalsIgnoreCase(currentColumn))
				{
					sql="SELECT SP_NAME FROM SALES_PERS WHERE SALES_PERS=?";
					pStmt=conn.prepareStatement(sql);
					pStmt.setString(1,columnValue);
					rs=pStmt.executeQuery();
					if(rs.next())
					{
						salesPerName=rs.getString("SP_NAME");
						System.out.println("sales person name:"+salesPerName);
					}
					valueXmlString.append("<sp_name><![CDATA[").append(salesPerName).append("]]></sp_name>");
				}
				else if( "cust_code__bill".equalsIgnoreCase(currentColumn) )
				{
					sql = "select cust_name from customer where cust_code =?";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, columnValue);
					rs = pStmt.executeQuery();
					if( rs.next() )
					{
						custName1 = checkNull(rs.getString("cust_name")); 
					}
					valueXmlString.append( "<customer_cust_name><![CDATA[" ).append(custName1).append( "]]></customer_cust_name>\r\n" );
				}
				else if("eff_from".equalsIgnoreCase(currentColumn))
				{
					String effFrom = genericUtility.getColumnValue("eff_from", dom);
					Date effFromDate = sdf.parse(effFrom);
					Calendar cal = Calendar.getInstance(); 
					cal.setTime(effFromDate);
					cal.add(Calendar.MONTH, 1);
					Date oneMonth = cal.getTime();
				    Timestamp oneMonthLaterTs = new Timestamp(oneMonth.getTime());
				    Date validUptoDate = new Date(oneMonthLaterTs.getTime());
				    String oneMonthLaterStr = sdf.format(validUptoDate);
				    valueXmlString.append( "<valid_upto><![CDATA[" ).append(oneMonthLaterStr).append( "]]></valid_upto>\r\n" );
				}

				valueXmlString.append( "</Detail1>\r\n" );
				break;
			case 2:
				System.out.println("In the second case2 itemchanged");
				valueXmlString.append( "<Detail2>\r\n" );
				
				if("itm_default".equalsIgnoreCase(currentColumn))
				{
					String nature = genericUtility.getColumnValue("nature", dom2, "1");
					if(nature.equalsIgnoreCase("V"))
					{
						valueXmlString.append( "<item_code protect= \"1\"><![CDATA[" ).append("").append( "]]></item_code>\r\n" );
						valueXmlString.append( "<quantity protect= \"1\"><![CDATA[" ).append("").append( "]]></quantity>\r\n" );
						valueXmlString.append( "<item_code__repl protect= \"1\"><![CDATA[" ).append("").append( "]]></item_code__repl>\r\n" );
						valueXmlString.append( "<free_qty protect= \"1\"><![CDATA[" ).append("").append( "]]></free_qty>\r\n" );
						valueXmlString.append( "<rate protect= \"1\"><![CDATA[" ).append("").append( "]]></rate>\r\n" );
					}
					else
					{
						valueXmlString.append( "<rate><![CDATA[" ).append(0).append( "]]></rate>\r\n" );
						valueXmlString.append( "<quantity><![CDATA[" ).append(0).append( "]]></quantity>\r\n" );
						valueXmlString.append( "<free_qty><![CDATA[" ).append(0).append( "]]></free_qty>\r\n" );
					}
					valueXmlString.append( "<amount><![CDATA[" ).append(0).append( "]]></amount>\r\n" );
				}
				else if("itm_defaultedit".equalsIgnoreCase(currentColumn))
				{
					String nature = genericUtility.getColumnValue("nature", dom2, "1");
					if(nature.equalsIgnoreCase("V"))
					{
						valueXmlString.append( "<item_code protect= \"1\"><![CDATA[" ).append("").append( "]]></item_code>\r\n" );
						valueXmlString.append( "<quantity protect= \"1\"><![CDATA[" ).append("").append( "]]></quantity>\r\n" );
						valueXmlString.append( "<item_code__repl protect= \"1\"><![CDATA[" ).append("").append( "]]></item_code__repl>\r\n" );
						valueXmlString.append( "<free_qty protect= \"1\"><![CDATA[" ).append("").append( "]]></free_qty>\r\n" );
						valueXmlString.append( "<rate protect= \"1\"><![CDATA[" ).append("").append( "]]></rate>\r\n" );
					}
				}
				else if("item_code".equalsIgnoreCase(currentColumn))
				{
					itmCode = genericUtility.getColumnValue("item_code", dom);
					sql = "select descr from item where item_code =?";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, itmCode);
					rs = pStmt.executeQuery();
					while(rs.next())
					{
						itmDescr = rs.getString("descr");
					}
					rs.close();
					pStmt.close();
				    priceList = "";
					sql = "select var_value from disparm where var_name=?";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, "MRP_GST");
					rs = pStmt.executeQuery();
					while(rs.next())
					{
						priceList = rs.getString("var_value");
					}
					rs.close();
					pStmt.close();
					System.out.println("priclist:"+priceList);
					double rate =0.0;
					sql = "select rate from pricelist where price_list=? and "
							+ "slab_no = (select max(slab_no) from pricelist where item_code=?) and item_code=?";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, priceList);
					pStmt.setString(2, itmCode);
					pStmt.setString(3, itmCode);
					rs = pStmt.executeQuery();
					while(rs.next())
					{
						rate = rs.getDouble("rate");
					}
					System.out.println("rate"+rate);
					valueXmlString.append( "<item_descr><![CDATA[" ).append(itmDescr).append( "]]></item_descr>\r\n" );
					valueXmlString.append( "<rate><![CDATA[" ).append(rate).append( "]]></rate>\r\n" );
					//valueXmlString.append( "<quantity><![CDATA[" ).append(0).append( "]]></quantity>\r\n" );
					valueXmlString.append( "<amount><![CDATA[" ).append(0).append( "]]></amount>\r\n" );
					if(rs!=null)
					{
						rs.close();
						rs = null;
					}
					if(pStmt!=null)
					{
						pStmt.close();
						pStmt = null;
					}
				}
				else if("item_code__repl".equalsIgnoreCase(currentColumn))
				{
					itemCodeRepl = genericUtility.getColumnValue("item_code__repl", dom);
					sql = "select descr from item where item_code =?";
					pStmt = conn.prepareStatement(sql);
					pStmt.setString(1, itemCodeRepl);
					rs = pStmt.executeQuery();
					while(rs.next())
					{
						itemReplDescr = rs.getString("descr");
					}
					rs.close();
					pStmt.close();
					valueXmlString.append( "<item_repl_descr><![CDATA[" ).append(itemReplDescr).append( "]]></item_repl_descr>\r\n" );
				}
				else if("free_qty".equalsIgnoreCase(currentColumn))
				{
					double amount,freeQty,rate;
					String rateStr,freeQtyStr;
					rateStr = genericUtility.getColumnValue("rate", dom);
					System.out.println("ratestr:"+rateStr);
					rate = Double.parseDouble(rateStr);
					freeQtyStr = genericUtility.getColumnValue("free_qty", dom);
					freeQty = Double.parseDouble(freeQtyStr);
					amount = freeQty * rate;
					valueXmlString.append( "<amount><![CDATA[" ).append(amount).append( "]]></amount>\r\n" );
				}
				else if("rate".equalsIgnoreCase(currentColumn))
				{
					double amount,freeQty,rate;
					String rateStr,freeQtyStr;
					rateStr = genericUtility.getColumnValue("rate", dom);
					System.out.println("ratestr:"+rateStr);
					rate = Double.parseDouble(rateStr);
					freeQtyStr = genericUtility.getColumnValue("free_qty", dom);
					freeQty = Double.parseDouble(freeQtyStr);
					amount = freeQty * rate;
					valueXmlString.append( "<amount><![CDATA[" ).append(amount).append( "]]></amount>\r\n" );
				}
				valueXmlString.append( "</Detail2>\r\n" );
			}
		}
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
	private String errorType( Connection conn , String errorCode ) throws ITMException
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
}
