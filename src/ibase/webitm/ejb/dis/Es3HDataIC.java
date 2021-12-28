
package ibase.webitm.ejb.dis;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Stateless
public class Es3HDataIC extends ValidatorEJB implements Es3HDataICRemote,Es3HDataICLocal {

	E12GenericUtility genericUtility = new E12GenericUtility();
	
	public String wfValData(String currFrmXmlStr, String hdrFrmXmlStr,String allFrmXmlStr, String objContext, String editFlag,String xtraParams) throws RemoteException 
	{
		System.out.println("In wfValData");
		Document currDom = null;
		Document hdrDom = null;
		Document allDom = null;
		String errString = "";
		try 
		{
			System.out.println("currFrmXmlStr..." + currFrmXmlStr);
			System.out.println("hdrFrmXmlStr..." + hdrFrmXmlStr);
			System.out.println("allFrmXmlStr..." + allFrmXmlStr);
			if ((currFrmXmlStr != null) && (currFrmXmlStr.trim().length() != 0)) 
			{
				currDom = parseString(currFrmXmlStr);
			}
			if ((hdrFrmXmlStr != null) && (hdrFrmXmlStr.trim().length() != 0)) 
			{
				hdrDom = parseString(hdrFrmXmlStr);
			}
			if ((allFrmXmlStr != null) && (allFrmXmlStr.trim().length() != 0)) 
			{
				allDom = parseString(allFrmXmlStr);
			}
			errString = validate(currDom, hdrDom, allDom, objContext, editFlag, xtraParams);
		} 
		catch (Exception e) 
		{
			System.out.println("Exception : [PurcRCPIc][wfValData(String currFrmXmlStr)] : ==>\n" + e.getMessage());
		}
		return errString;
	}

	public String validate(Document currDom, Document hdrDom, Document allDom,String objContext, String editFlag, String xtraParams)throws RemoteException, ITMException 
	{
		System.out.println("In validate Data");
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		ArrayList<String> errCustList = new ArrayList<String>();
		int count = 0;
		String errString = "", errorType = "", errCode = "",custCode="";
		StringBuffer errStringXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root><Errors>");
		String childNodeName = "";
		String sql = "";
		int noOfChilds = 0;
		ResultSet rs = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		int currentFormNo = 0;
		int cnt = 0;
		ConnDriver connDriver = null;
		Node childNode = null;
		String itemSer="",prdCode="",fromDateDom="",toDateDom="";
		java.sql.Timestamp toDate=null,fromDate=null;
		ArrayList<String> custArray=null;
		int divCount=0;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			System.out.println("************xtraParams*************" + xtraParams);
			//connDriver = new ConnDriver();
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			System.out.println("In wfValData Distribution receipt:::");
			String userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			
			System.out.println("**************loginCode************" + userId);
			
			if ((objContext != null) && (objContext.trim().length() > 0)) 
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			NodeList parentList = currDom.getElementsByTagName("Detail"+ currentFormNo);
			NodeList childList = null;
			System.out.println("hdrDom..." + hdrDom.toString());
			switch (currentFormNo)
			{
			case 1:
			{
				childList = parentList.item(0).getChildNodes();
				noOfChilds = childList.getLength();
				for (int ctr = 0; ctr < noOfChilds; ctr++) 
				{
					childNode = childList.item(ctr);
					if (childNode.getNodeType() != 1) 
					{
						continue;
					}
					childNodeName = childNode.getNodeName();
					System.out.println("Editflag =" + editFlag);
					System.out.println("parentList = " + parentList);
					System.out.println("childList = " + childList);
					if ("prd_code".equalsIgnoreCase(childNodeName) ) 
					{
						prdCode = checkNull(genericUtility.getColumnValue("prd_code", currDom));
						if(prdCode==null || prdCode.trim().length()==0)
						{
							errList.add("VTNULLPC");//Invalid-Division can not be blank 
							errFields.add(childNodeName.toLowerCase());
							break;
						}
					}
					if ("item_ser".equalsIgnoreCase(childNodeName) ) 
					{
						itemSer = checkNull(genericUtility.getColumnValue("item_ser", currDom));
						prdCode = checkNull(genericUtility.getColumnValue("prd_code", currDom));
						
						if(itemSer==null || itemSer.trim().length()==0)
						{
							errList.add("VTNULLDV");//Invalid-Division can not be blank 
							errFields.add(childNodeName.toLowerCase());
							break;
						}
						else
						{
							sql = "SELECT COUNT(*) AS COUNT FROM CUST_STOCK WHERE item_ser = ?  and prd_code=? and pos_code is not null ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, itemSer);
								pstmt.setString(2, prdCode);
								rs = pstmt.executeQuery();
								if (rs.next()) 
								{
									count = rs.getInt("COUNT");
								}
								System.out.println("Count: " + count);
								if (count == 0) 
								{
									errList.add("VTINVSEL");//Record Not found
									errFields.add(childNodeName.toLowerCase());
									break;
								}
						}
					}
					if ("from_date".equalsIgnoreCase(childNodeName) ) 
					{
						fromDateDom = checkNull(genericUtility.getColumnValue("from_date", currDom));
						if(fromDateDom==null || fromDateDom.trim().length()==0)
						{
							errList.add("VPBLKFRDT");
							errFields.add(childNodeName.toLowerCase());
							break;
						}
					}
					if ("to_date".equalsIgnoreCase(childNodeName) ) 
					{
						fromDateDom = checkNull(genericUtility.getColumnValue("from_date", currDom));
						toDateDom = checkNull(genericUtility.getColumnValue("to_date", currDom));
						if(toDateDom==null || toDateDom.trim().length()==0)
						{
							errList.add("VPBLKTODT");
							errFields.add(childNodeName.toLowerCase());
							break;
						}
						else
						{
							fromDate = java.sql.Timestamp.valueOf(genericUtility.getValidDateString(fromDateDom, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
							toDate = java.sql.Timestamp.valueOf(genericUtility.getValidDateString(toDateDom, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
							System.out.println("fromDate>>>>"+fromDate+"toDate::::"+toDate);
						    if(toDate.before(fromDate))
						    {
						    	errList.add("INVTODT");
								errFields.add(childNodeName.toLowerCase());
								break;
						    }
						}
					}
					if ("cust_code".equalsIgnoreCase(childNodeName) ) 
					{
						itemSer = checkNull(genericUtility.getColumnValue("item_ser", currDom));
						prdCode = checkNull(genericUtility.getColumnValue("prd_code", currDom));
						custCode = checkNull(genericUtility.getColumnValue("cust_code", currDom));
						fromDateDom = checkNull(genericUtility.getColumnValue("from_date", currDom));
						toDateDom = checkNull(genericUtility.getColumnValue("to_date", currDom));
						fromDate = java.sql.Timestamp.valueOf(genericUtility.getValidDateString(fromDateDom, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
						toDate = java.sql.Timestamp.valueOf(genericUtility.getValidDateString(toDateDom, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
						if(custCode==null || custCode.trim().length()==0)
						{
							errList.add("VPBLKCUSCD");
							errFields.add(childNodeName.toLowerCase());
							break;
						}
						else
						{
							if (!custCode.matches("[A-Za-z0-9, ]*")) 
							{
								errList.add("VPINVCCDS");
								errFields.add(childNodeName.toLowerCase());
								break;
							}
							if(custCode.contains(","))
							{
								custArray= new ArrayList<String>(Arrays.asList(custCode.split(",")));
								for (int i=0;i<custArray.size();i++)
								{
									sql = "SELECT COUNT(*) AS COUNT FROM CUSTOMER WHERE CUST_CODE=? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, custArray.get(i));
									rs = pstmt.executeQuery();
									if (rs.next()) 
									{
										divCount = rs.getInt("COUNT");
									}
									callPstRs(pstmt, rs);
									System.out.println("divCount: " + divCount);
									if (divCount == 0) 
									{
										errCustList.add(custArray.get(i));
										errList.add("VPINVCSCDM");
										errFields.add(childNodeName.toLowerCase());
										break;
									}
									else
									{
										sql = "SELECT COUNT(*) AS COUNT FROM CUST_STOCK WHERE CUST_CODE=? AND PRD_CODE=? AND ITEM_SER=? AND TRAN_DATE BETWEEN ? AND ? ";
										pstmt = conn.prepareStatement(sql);
										pstmt.setString(1, custArray.get(i));
										pstmt.setString(2, prdCode);
										pstmt.setString(3, itemSer);
										pstmt.setTimestamp(4, fromDate);
										pstmt.setTimestamp(5, toDate);
										rs = pstmt.executeQuery();
										if (rs.next()) 
										{
											count = rs.getInt("COUNT");
										}
										callPstRs(pstmt, rs);
										System.out.println("Count: " + count);
										if (count == 0) 
										{
											errCustList.add(custArray.get(i));
											errList.add("VPINVCSCD");
											errFields.add(childNodeName.toLowerCase());
											break;
										}
									}
							    }
							}
							else
							{
								sql = "SELECT COUNT(*) AS COUNT FROM CUSTOMER WHERE CUST_CODE=? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, custCode);
								rs = pstmt.executeQuery();
								if (rs.next()) 
								{
									divCount = rs.getInt("COUNT");
								}
								callPstRs(pstmt, rs);
								System.out.println("divCount: " + divCount);
								if (divCount == 0) 
								{
									errCustList.add(custCode);
									errList.add("VPINVCSCDM");
									errFields.add(childNodeName.toLowerCase());
									break;
								}
								else
								{
									sql = "SELECT COUNT(*) AS COUNT FROM CUST_STOCK WHERE CUST_CODE=? AND PRD_CODE=? AND ITEM_SER=? AND TRAN_DATE BETWEEN ? AND ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, custCode);
									pstmt.setString(2, prdCode);
									pstmt.setString(3, itemSer);
									pstmt.setTimestamp(4, fromDate);
									pstmt.setTimestamp(5, toDate);
									rs = pstmt.executeQuery();
									if (rs.next()) 
									{
										count = rs.getInt("COUNT");
									}
									callPstRs(pstmt, rs);
									System.out.println("Count: " + count);
									if (count == 0) 
									{
										errCustList.add(custCode);
										errList.add("VPINVCSCD");
										errFields.add(childNodeName.toLowerCase());
										break;
									}
								}
							}
						}
						
					}
					
				}
			}
			break;
			
			}
			int errListSize = errList.size();
			cnt = 0;
			String errFldName = "";
			if ((errList != null) && (errListSize > 0)) 
			{
				for (cnt = 0; cnt < errListSize; cnt++) 
				{
					errCode = (String) errList.get(cnt);
					errFldName = (String) errFields.get(cnt);
					errString = getErrorString(errFldName, errCode, userId);
					errorType = errorType(conn, errCode);
					if(errCustList.size()>0 && errString.length() > 0 )
					{
						 String begPart = errString.substring( 0, errString.indexOf("]]></description>") );
						  String mainStr="";
						    for(int i=0;i<errCustList.size();i++)
						    {
						    	mainStr=mainStr+ errCustList.get(i)+",";
						    }
						    
						    String endPart=errString.substring( errString.indexOf("]]></description>"), errString.length() );
						    mainStr=" Following customers are invalid :: "+mainStr.substring(0,mainStr.length()-1);
						    errString = begPart+mainStr +  endPart;
					}
					if (errString.length() > 0) 
					{
						String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 8,errString.indexOf("<trace>"));
						bifurErrString = bifurErrString + errString.substring(errString.indexOf("</trace>") + 8, errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						System.out.println("errStringXml .........." + errStringXml);
						errString = "";
					}
					if (errorType.equalsIgnoreCase("E")) 
					{
						break;
					}
				}
				errList.clear();
				errList = null;
				errFields.clear();
				errFields = null;
			}
			errStringXml.append("</Errors></Root>\r\n");
			errString = errStringXml.toString();
			
		}
		catch (Exception e) 
		{
			System.out.println("Exception in "+this.getClass().getSimpleName()+"  == >");
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally 
		{
			try 
			{
				if (rs != null) 
				{
					rs.close();
					rs = null;
				}
				if (pstmt != null) 
				{
					pstmt.close();
					pstmt = null;
				}
				if ((conn != null) && (!conn.isClosed()))
					conn.close();
			}
			catch (Exception e) 
			{
				System.out.println("Exception :"+this.getClass().getSimpleName()+":wfValData :==>\n" + e.getMessage());
				throw new ITMException(e);
			}
		}
		return errString;
	}
	
	public String itemChanged(String currFrmXmlStr, String hdrFrmXmlStr,String allFrmXmlStr, String objContext, String currentColumn,String editFlag, String xtraParams) throws RemoteException,ITMException 
	{
		Document currDom = null;
		Document hdrDom = null;
		Document allDom = null;
		String errString = null;
		try 
		{
			if ((currFrmXmlStr != null) && (currFrmXmlStr.trim().length() != 0)) 
			{
				currDom = genericUtility.parseString(currFrmXmlStr);
				System.out.println("currFrmXmlStr : " + currFrmXmlStr);
			}
			if ((hdrFrmXmlStr != null) && (hdrFrmXmlStr.trim().length() != 0)) 
			{
				hdrDom = genericUtility.parseString(hdrFrmXmlStr);
				System.out.println("hdrFrmXmlStr : " + hdrFrmXmlStr);
			}
			if ((allFrmXmlStr != null) && (allFrmXmlStr.trim().length() != 0)) 
			{
				allDom = genericUtility.parseString(allFrmXmlStr);
				System.out.println("allFrmXmlStr : " + allFrmXmlStr);
			}
			errString = itemChanged(currDom, hdrDom, allDom, objContext,currentColumn, editFlag, xtraParams);
			System.out.println("ErrString :" + errString);
		}
		catch (Exception e) 
		{
			System.out.println("Exception :"+this.getClass().getSimpleName()+":itemChanged :==>\n" + e.getMessage());
			errString = genericUtility.createErrorString(e);
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		return errString;
	}

	public String itemChanged(Document currDom, Document hdrDom,Document allDom, String objContext, String currentColumn,String editFlag, String xtraParams) throws RemoteException,ITMException 
	{
		int currentFormNo = 0,ctr = 0,childNodeListLength = 0;
		String childNodeName = null;
		Connection conn = null;
		StringBuffer valueXmlString = new StringBuffer();
		String fromDateDom="";
		try 
		{
			try {
				//ConnDriver connDriver = new ConnDriver();
				//conn = connDriver.getConnectDB("DriverITM");
				conn = getConnection();
			} catch (Exception e) {
				System.out.println("Exception :Es3HDataUpdPrc :ejbCreate :==>" + e);
				e.printStackTrace();
			}
			NodeList parentNodeList = null;
			NodeList childNodeList = null;
			Node parentNode = null;
			Node childNode = null;

			if ((objContext != null) && (objContext.trim().length() > 0)) 
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n<Header>\r\n<editFlag>");
			valueXmlString.append(editFlag).append("</editFlag>\r\n</Header>\r\n");

			System.out.println("currentFormNo-------*************** = "+ currentFormNo);
			switch (currentFormNo) 
			{
			case 1:
				System.out.println("currentFormNo-------*************** = "+ currentFormNo);
				parentNodeList = currDom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail1>");
				childNodeListLength = childNodeList.getLength();
				do 
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					ctr++;
				}while ((ctr < childNodeListLength) && (!childNodeName.equals(currentColumn)));

				System.out.println(" currentColumn : "+ currentColumn);

				if (currentColumn.equalsIgnoreCase("itm_default")) 
				{
					valueXmlString.append("<prd_code>").append("<![CDATA[]]>").append("</prd_code>\r\n");
					valueXmlString.append("<item_ser>").append("<![CDATA[]]>").append("</item_ser>\r\n");
					valueXmlString.append("<from_date>").append("").append("</from_date>\r\n");
					valueXmlString.append("<to_date>").append("").append("</to_date>\r\n");
					valueXmlString.append("<cust_code>").append("<![CDATA[]]>").append("</cust_code>\r\n");
				}
				else if(currentColumn.equalsIgnoreCase("from_date"))
				{
					fromDateDom = checkNull(genericUtility.getColumnValue("from_date", currDom));
					if(fromDateDom!=null && fromDateDom.trim().length()>0){
					valueXmlString.append("<to_date>").append(fromDateDom).append("</to_date>\r\n");
					}
					else
					{
						valueXmlString.append("<to_date>").append("").append("</to_date>\r\n");
					}
				}
								
				valueXmlString.append("</Detail1>\r\n");
			}
		}
		catch (Exception localException) 
		{
			localException.printStackTrace();
			throw new ITMException(localException); //Added By Mukesh Chauhan on 07/08/19
		}
		finally
		{
			try{
				conn.close();
				conn=null;
				}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		
		}
		valueXmlString.append("</Root>\r\n");
		System.out.println("\n****ValueXmlString :" + valueXmlString.toString()+ ":********");
		return valueXmlString.toString();
	}
	
	public void callPstRs(PreparedStatement pstmt, ResultSet rs) 
	{
		try 
		{
			if (pstmt != null) 
			{
				pstmt.close();
				pstmt = null;
			}
			if (rs != null) 
			{
				rs.close();
				rs = null;
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	private String checkNull(String inputVal) 
	{
		if (inputVal == null) 
		{
			inputVal = "";
		}
		return inputVal;
	}
	private String errorType(Connection conn, String errorCode) throws ITMException 
	{
		String msgType = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try 
		{
			String sql = " SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO =   ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, errorCode);
			rs = pstmt.executeQuery();
			while (rs.next())
				msgType = rs.getString("MSG_TYPE");
		}
		catch (Exception ex) 
		{
			ex.printStackTrace();
			try 
			{
				if (rs != null) 
				{
					rs.close();
					rs = null;
				}
				if (pstmt != null) 
				{
					pstmt.close();
					pstmt = null;
				}
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			try 
			{
				if (rs != null) 
				{
					rs.close();
					rs = null;
				}
				if (pstmt != null) 
				{
					pstmt.close();
					pstmt = null;
				}
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			throw new ITMException(ex); //Added By Mukesh Chauhan on 07/08/19
		}
		finally 
		{
			try 
			{
				if (rs != null) 
				{
					rs.close();
					rs = null;
				}
				if (pstmt != null) 
				{
					pstmt.close();
					pstmt = null;
				}
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
		return msgType;
	}
}
