/*  DEVELOP BY RITESH ON 04-FEB-16 
 * PURPOSE: To Migrate  A. Scheme Applicability
 * */
package ibase.webitm.ejb.dis;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.fin.FinCommon;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.ejb.Stateless; 
import java.sql.Timestamp;
@Stateless  

public class SchemeApplIC extends ValidatorEJB implements SchemeApplICLocal, SchemeApplICRemote
{
	//changed by nasruddin 05-10-16
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility = new E12GenericUtility();
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = "";
		System.out.println("wfValdata() called for SchemeApplIC");
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
		String childNodeName = null;
		String errString = "";
		String errCode = "", errCodeGenMst="";
		String errStrign = "";
		String userId = "";
		String sql = "";
		String errorType = "";
		int cnt = 0;
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
		ResultSet rs = null;
		ConnDriver connDriver = new ConnDriver();
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
		String keyFlag = "";
		int currentFormNo =0,recCnt=0,count= 0;
		Timestamp amdDate=null,dlvDate=null,reqDate=null,ordDate=null;
		int count2= 0;
		String empCode = "";
		String updateFlg = "";
		try
		{
			System.out.println("@@@@@@@@ wfvaldata called");
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			connDriver = null;
			userId = getValueFromXTRA_PARAMS(xtraParams,"loginCode");
			sql = " select key_flag from transetup where tran_window = 'w_scheme_appl' ";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				keyFlag = rs.getString(1)==null?"": rs.getString(1);
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			switch(currentFormNo)
			{
			case 1 :
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();

				for(ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if( childNodeName.equalsIgnoreCase("scheme_code") && "M".equals(keyFlag)  )
					{
						String schemeCode = checkNull(genericUtility.getColumnValue("scheme_code", dom));
						updateFlg =  getAttributeValue(parentNode);//Added by Jaffar S on 19-02-19
						System.out.println("updateFlg["+updateFlg+"]editFlag["+editFlag+"]");
						if(schemeCode.trim().length() <= 0)
						{
							errCode = "VMSCHCD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						//else if(!"E".equalsIgnoreCase(editFlag))-------- By Jaffar S on 19-02-19
						else if("A".equalsIgnoreCase(updateFlg))
						{
							sql = " select count(1) from scheme_applicability where scheme_code= ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,schemeCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count2 = rs.getInt(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(count2 > 0)
							{
								errCode = "VMSCHNT1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					if(childNodeName.equalsIgnoreCase("app_from")  )
					{
						String appFrom = checkNull(genericUtility.getColumnValue("app_from", dom));

						if(appFrom.trim().length() <= 0)
						{
							errCode = "VMAPPFRM";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						
					}
					if(childNodeName.equalsIgnoreCase("item_code")  )
					{	
						String itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
						// Changes and Commented By Nandkumar Gadkari on 03-01-2018 :START
						String prodSch = checkNull(genericUtility.getColumnValue("prod_sch", dom));
						System.out.println("Pordsch---["+checkNull(genericUtility.getColumnValue("prod_sch", dom1)+"]"));
						
						System.out.println("itemCode["+itemCode+"]prodSch["+prodSch+"]");
						if("N".equalsIgnoreCase(prodSch))
						{
							System.out.println("prodSch::::::::::::::inside if ");
							if(itemCode.trim().length() <= 0)
							{
							errCode = "VMITEMCD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							}
						}
						
						if(itemCode.trim().length() > 0)
						{
							errStrign = isExist("item", "item_code", itemCode, conn,"");
							if("TRUE".equalsIgnoreCase(errStrign))
							{
								errStrign = isExist("item", "item_code", itemCode, conn,"active");
								if("N".equalsIgnoreCase(errStrign))
								{
									errCode = "VTITEM4";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
							else
							{
								errCode = "VMITEM_CD";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					//Nandkumar Gadkari----------------- END--------------------- 
					if(childNodeName.equalsIgnoreCase("min_value")  )
					{
						double minValueInt = 0d;
						double maxValueInt = 0d;
						String minValue = checkNull(genericUtility.getColumnValue("min_value", dom));
						String maxValue = checkNull(genericUtility.getColumnValue("max_value", dom));
						if(minValue.trim().length() > 0 && maxValue.trim().length() > 0)
						{
							minValueInt = Double.parseDouble(minValue);
							maxValueInt = Double.parseDouble(maxValue);
							if(maxValueInt < minValueInt)
							{
							errCode = "VMMINAMT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							}
							
						}

					}
					if(childNodeName.equalsIgnoreCase("valid_upto")  )
					{
						String validUpto = checkNull(genericUtility.getColumnValue("valid_upto", dom));
						String appFrom = checkNull(genericUtility.getColumnValue("app_from", dom));

						if(validUpto.trim().length() <= 0)
						{
							errCode = "VMDTTO";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}else if(appFrom.trim().length() <= 0)
						{
							errCode = "VMAPPFRM";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}else
						{
						    DateFormat df = new SimpleDateFormat(genericUtility.getApplDateFormat(),Locale.ENGLISH);
							java.util.Date validUptoDate = df.parse(validUpto);
							java.util.Date appFromDate = df.parse(appFrom);
							if(validUptoDate.before(appFromDate))
							{
								errCode = "VMVAL_UPTO";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
					}
					
				} // end for
				break;  // case 1 end
			case 2:
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				System.out.println("@@@@@@@@@@@@childNodeListLength["+childNodeListLength+"]");
				for(ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();

					if(childNodeName.equalsIgnoreCase("scheme_code") && "M".equals(keyFlag) )
					{
						String schemeCode = checkNull(genericUtility.getColumnValue("scheme_code", dom));
						updateFlg =  getAttributeValue(parentNode);//Added by Jaffar S on 19-02-19
						System.out.println("updateFlg == "+updateFlg+" editFlag ["+editFlag+"]");
						if(schemeCode.trim().length() <= 0)
						{
							errCode = "VMSCHCD2";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					if( childNodeName.equalsIgnoreCase("site_code"))
					{
						
						String siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						String stateCode = checkNull(genericUtility.getColumnValue("state_code", dom));
						String countCode = checkNull(genericUtility.getColumnValue("count_code", dom));
						String schemeCode = checkNull(genericUtility.getColumnValue("scheme_code", dom));
						updateFlg =  getAttributeValue(parentNode);//Added by Jaffar S on 19-02-19
						System.out.println("updateFlg == "+updateFlg+" editFlag ["+editFlag+"]");
						
						if(siteCode.trim().length()>0)
						{
						errStrign = isExist("site", "site_code", siteCode, conn,"");
						if("TRUE".equalsIgnoreCase(errStrign))
						{
//							errStrign = isExist("item", "item_code", itemCode, conn,"active");
							if(siteCode.trim().length() > 0 && stateCode.trim().length() > 0 && countCode.trim().length() > 0)
							{
								errCode = "VMSCHEME2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else if(siteCode.trim().length() > 0 && countCode.trim().length() > 0)
							{
								errCode = "VMSCHEME8";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else if(siteCode.trim().length() > 0 && stateCode.trim().length() > 0 )
							{
								errCode = "VMSCHEME9";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							//else if(!"E".equals(editFlag))-----By Jaffar S on 19-02-19
							//commented by-monika-19-july-2019
						//	else if("A".equalsIgnoreCase(updateFlg))
							//addedd by -monika-19-july-2019
							if(siteCode.trim().length() > 0)//end
							{
								System.out.println("EditFlag value === "+editFlag);
								System.out.println("updateFlg value === "+updateFlg);
								
								sql = "select count(1)  from scheme_applicability_det where scheme_code = '"+schemeCode+"'"+
									  " and (length(rtrim(state_code)) > 0 or length(rtrim(count_code)) > 0 )";
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								
								if(rs.next())
								{
									count = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								System.out.println("Scheme Code is ===== "+schemeCode);
								if(count > 0 )
								{
									errCode = "VMSCHEME3";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								//commented by-monika-19-july-2019
								/*else
								{
									sql = " select count(*)  from scheme_applicability_det where scheme_code = '"+schemeCode+"'"+
										  " and length(rtrim(site_code)) > 0 ";
										pstmt = conn.prepareStatement(sql);
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											count = rs.getInt(1);
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										if(count > 0 )
										{
											errCode = "VMSCHEME4";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
								}*///end
								
							}
						}
						else
						{
							errCode = "VTSITECD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					if(siteCode.trim().length() <= 0 && stateCode.trim().length() <= 0 && countCode.trim().length() <= 0)
					{
						errCode = "VMSCHEME6";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
					} 
					if( childNodeName.equalsIgnoreCase("state_code"))
					{
						String siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						String stateCode = checkNull(genericUtility.getColumnValue("state_code", dom));
						String countCode = checkNull(genericUtility.getColumnValue("count_code", dom));
						String schemeCode = checkNull(genericUtility.getColumnValue("scheme_code", dom));
						updateFlg =  getAttributeValue(parentNode);//Added by Jaffar S on 19-02-19
						System.out.println("updateFlg == "+updateFlg+" editFlag ["+editFlag+"]");
						
						if(stateCode.trim().length()>0)
						{
						errStrign = isExist("state", "state_code", stateCode, conn,"");
						if("TRUE".equalsIgnoreCase(errStrign))
						{
//							errStrign = isExist("item", "item_code", itemCode, conn,"active");
							if(siteCode.trim().length() > 0 && stateCode.trim().length() > 0 && countCode.trim().length() > 0)
							{
								errCode = "VMSCHEME2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else if( stateCode.trim().length() > 0 && countCode.trim().length() > 0)
							{
								errCode = "VMSCHEME10";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else if(siteCode.trim().length() > 0 && stateCode.trim().length() > 0)
							{
								errCode = "VMSCHEME9";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							//else if(!"E".equals(editFlag))-----By Jaffar S on 19-02-19
							//commented by-monika-19-july-2019
						//	else if("A".equalsIgnoreCase(updateFlg))
							//changes by-monika-19-july-2019//end
							if(stateCode.trim().length() > 0)//end
							{
									sql = "select count(1)  from scheme_applicability_det where scheme_code = '"+schemeCode+"'"+
										  " and (length(rtrim(site_code)) > 0 or length(rtrim(count_code)) > 0 ) ";
									pstmt = conn.prepareStatement(sql);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										count = rs.getInt(1);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									if(count > 0 )
									{
										errCode = "VMSCHEME4";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									//commented by-monika-19 july 2019
									/*else
									{
										sql = "select count(1)  from scheme_applicability_det where scheme_code = '"+schemeCode+"'"+
											  " and length(rtrim(state_code)) > 0";
										pstmt = conn.prepareStatement(sql);
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											count = rs.getInt(1);
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										if(count > 0 )
										{
											errCode = "VMSCHEME4";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
									
								}*///end
							}
						}
						else
						{
							errCode = "VTSTATE1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());	
						}
					}
					if(siteCode.trim().length() <= 0 && stateCode.trim().length() <= 0 && countCode.trim().length() <= 0)
					{
						errCode = "VMSCHEME6";
						errList.add(errCode);
						errFields.add(childNodeName.toLowerCase());
					}
					}
					if( childNodeName.equalsIgnoreCase("count_code"))
					{
						String siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
						String stateCode = checkNull(genericUtility.getColumnValue("state_code", dom));
						String countCode = checkNull(genericUtility.getColumnValue("count_code", dom));
						String schemeCode = checkNull(genericUtility.getColumnValue("scheme_code", dom));
						updateFlg =  getAttributeValue(parentNode);//Added by Jaffar S on 19-02-19
						System.out.println("updateFlg == "+updateFlg+" editFlag ["+editFlag+"]");
						
						if(countCode.trim().length()>0)
						{
						errStrign = isExist("country", "count_code", countCode, conn,"");
						if("TRUE".equalsIgnoreCase(errStrign))
						{
//							errStrign = isExist("item", "item_code", itemCode, conn,"active");
							if(siteCode.trim().length() > 0 && stateCode.trim().length() > 0 && countCode.trim().length() > 0)
							{
								errCode = "VMSCHEME2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else if( stateCode.trim().length() > 0 && countCode.trim().length() > 0)
							{
								errCode = "VMSCHEME10";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else if(siteCode.trim().length() > 0 && countCode.trim().length() > 0)
							{
								errCode = "VMSCHEME8";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							//else if(!"E".equals(editFlag))-------By Jaffar S on 19-02-19
							else if("A".equalsIgnoreCase(updateFlg))
							{
									sql = "select count(1)  from scheme_applicability_det where scheme_code = '"+schemeCode+"'"+
										  " and (length(rtrim(site_code)) > 0 or length(rtrim(state_code)) > 0 )";
									pstmt = conn.prepareStatement(sql);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										count = rs.getInt(1);
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
									if(count > 0 )
									{
										errCode = "VMSCHEME7";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									else
									{
										sql = "select count(1)  from scheme_applicability_det where scheme_code = '"+schemeCode+"'"+
											  " and length(rtrim(state_code)) > 0";
										pstmt = conn.prepareStatement(sql);
										rs = pstmt.executeQuery();
										if(rs.next())
										{
											count = rs.getInt(1);
										}
										rs.close();
										rs = null;
										pstmt.close();
										pstmt = null;
										if(count > 0 )
										{
											errCode = "VMSCHEME4";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());
										}
									
								}
								
							}
						}
						else
						{
							errCode = "VMCOUNT1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
							
						}
						}
						if(siteCode.trim().length() <= 0 && stateCode.trim().length() <= 0 && countCode.trim().length() <= 0)
						{
							errCode = "VMSCHEME6";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
				}
			}

			int errListSize = errList.size();
			cnt = 0;
			String errFldName = null;
			if(errList != null && errListSize > 0)
			{
				for(cnt = 0; cnt < errListSize; cnt ++)
				{
					errCode = errList.get(cnt);
					errFldName = errFields.get(cnt);
					System.out.println("errCode .........." + errCode);
					errString = getErrorString(errFldName, errCode, userId);
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

	private String isExist(String table, String field, String value,Connection conn,String isField) throws SQLException
	{
		
		String sql = "",retStr="",result="";
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		isField = checkNull(isField);
		int cnt=0;
		try
		{
		if(isField.trim().length() <= 0)
		{
		sql = " SELECT COUNT(1) FROM "+ table + " WHERE " + field + " = ? ";
		pstmt =  conn.prepareStatement(sql);
		pstmt.setString(1,value);
		rs = pstmt.executeQuery();
		if(rs.next())
		{
			cnt = rs.getInt(1);
		}
		rs.close();
		rs = null;
		pstmt.close(); 
		pstmt = null;
		if( cnt > 0)
		{
			retStr = "TRUE";
		}
		if( cnt == 0 )
		{
			retStr = "FALSE";
		}
		System.out.println("@@@@ isexist["+value+"]:::["+retStr+"]:::["+cnt+"]");
		}
		else
		{
			sql = " SELECT "+isField+" FROM "+ table + " WHERE " + field + " = ? ";
			pstmt =  conn.prepareStatement(sql);
			pstmt.setString(1,value);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				result = rs.getString(1);
			}
			rs.close();
			rs = null;
			pstmt.close(); 
			pstmt = null;
			retStr= checkNull(result);
		}
		}
		catch(SQLException e)
		{
			retStr = "False";
		}
		return retStr;
	}
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		System.out.println("itemChanged() called for SchemeApplIC");
		String valueXmlString = "";
		try
		{   
			System.out.println("xmlString::060515--"+xmlString);
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
//			if(dom != null)
//			{
			System.out.println("if(dom != null) :: N");
			valueXmlString = itemChanged(dom, dom1, dom2, objContext, currentColumn, editFlag, xtraParams);
//			}
		}
		catch(Exception e)
		{
			System.out.println("Exception : [SchemeApplIC][itemChanged( String, String )] :==>\n" + e.getMessage());
		}
		return valueXmlString;
	}

	// method for item change
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
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
		String columnValue="";
		int currentFormNo =0;
		String sitecdFmDescr = "",finEntity = "",finADescr= "",finBDescr="",sitecdToDescr="";
		String bankDescr = "",acctDescr = "";
		String empLname = "",empFname= "";
		String empCodeLogin = "",loginSite="";
		String ordetType = "",schemeCode="",itemCode="",itemDescr="",slabOn="",lineNo="",siteCodeDescr="",siteCode="";
		String stateCode="",stateCodeDescr = "",countCode="",countCodeDescr="";
		try
		{
			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateTimeFormat());
//			String sysDate = sdf.format(currentDate.getTime());
			String sysDate = sdf.format(new Date());
			System.out.println("Current Date ::["+new Date()+"]");
			System.out.println("Application date format is :=>  " + sysDate);

			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver = null;

			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext.trim());
			}
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");

			System.out.println("**********ITEMCHANGE FOR CASE"+currentFormNo+"**************");
			switch(currentFormNo)
			{
			case 1 :
			
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();

				ctr = 0; 
				valueXmlString.append("<Detail1>");
				int childNodeListLength = childNodeList.getLength();
				do
				{ 
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if(childNodeName.equals(currentColumn))
					{
						if (childNode.getFirstChild()!= null)
						{
							columnValue = childNode.getFirstChild().getNodeValue().trim();
						}
					}
					ctr++;
				}
				while(ctr < childNodeListLength  && !childNodeName.equals(currentColumn));
				System.out.println("[" + currentColumn + "] ==> '" + columnValue + "'");

				String userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");  System.out.println("--login code--"+userId);
				
				loginSite = getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");

				String chgUser = this.genericUtility.getValueFromXTRA_PARAMS(xtraParams, "chgUser");
			     
			    String chgTerm = this.genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");
			    
				
				if(currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					System.out.println("@@@@@@@@ itm_default called @@@@@@@@");
					//ordetType = checkNull(genericUtility.getColumnValue("order_type", dom));
					slabOn = checkNull(genericUtility.getColumnValue("slab_on", dom));
					ordetType = new DistCommon().getDisparams("999999", "SCHEME_ORDTYPE", conn);
					if(!"NULLFOUND".equals(ordetType))
						valueXmlString.append("<order_type>").append("<![CDATA["+ordetType+"]]>").append("</order_type>");
					if("N".equals(slabOn))
					{
						valueXmlString.append("<min_value protect = \"1\">").append("<![CDATA[]]>").append("</min_value>");
						valueXmlString.append("<max_value protect = \"1\">").append("<![CDATA[]]>").append("</max_value>");
					}
				}
				else if(currentColumn.trim().equalsIgnoreCase("itm_defaultedit"))
				{
					schemeCode = checkNull(genericUtility.getColumnValue("scheme_code", dom));
					slabOn = checkNull(genericUtility.getColumnValue("slab_on", dom));
					valueXmlString.append("<scheme_code protect = \"1\">").append("<![CDATA["+schemeCode+"]]>").append("</scheme_code>");
				
					if("N".equals(slabOn))
					{
						valueXmlString.append("<min_value protect = \"1\">").append("<![CDATA[]]>").append("</min_value>");
						valueXmlString.append("<max_value protect = \"1\">").append("<![CDATA[]]>").append("</max_value>");
					}
					
				}
//				else if(currentColumn.trim().equalsIgnoreCase("cust_name"))
//				{
//					
//				}
//				else if(currentColumn.trim().equalsIgnoreCase("stan_code"))
//				{
//					
//				}
				
//				else if(currentColumn.trim().equalsIgnoreCase("sales_pers"))
//				{
//					
//				}
				else if(currentColumn.trim().equalsIgnoreCase("item_code"))
				{
					itemCode = checkNull(genericUtility.getColumnValue("item_code", dom));
					itemDescr = isExist("item", "item_code", itemCode, conn, "descr");
					valueXmlString.append("<item_descr>").append("<![CDATA["+itemDescr+"]]>").append("</item_descr>");
				}
				else if(currentColumn.trim().equalsIgnoreCase("slab_on"))
				{
					slabOn = checkNull(genericUtility.getColumnValue("slab_on", dom));
					if("N".equals(slabOn))
					{
						valueXmlString.append("<min_value protect = \"1\">").append("<![CDATA[]]>").append("</min_value>");
						valueXmlString.append("<max_value protect = \"1\">").append("<![CDATA[]]>").append("</max_value>");
					}
					else
					{
						valueXmlString.append("<min_value protect = \"0\">").append("<![CDATA[]]>").append("</min_value>");
						valueXmlString.append("<max_value protect = \"0\">").append("<![CDATA[]]>").append("</max_value>");
					}
				}
				

				valueXmlString.append("</Detail1>");
				break;
				// case 2 start
			case 2 :
				System.out.println("**********************In case 2 ***********************8");
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				ctr = 0; 
				valueXmlString.append("<Detail2>");
				childNodeListLength = childNodeList.getLength();
				do
				{ 
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if(childNodeName.equals(currentColumn))
					{
						if (childNode.getFirstChild()!= null)
						{
							columnValue = childNode.getFirstChild().getNodeValue().trim();
						}
					}
					ctr++;
				}
				while(ctr < childNodeListLength  && !childNodeName.equals(currentColumn));
				System.out.println("IN DETAILS column name is %%%%%%%%%%%%%[" + currentColumn + "] ==> '" + columnValue + "'");
				if( currentColumn.trim().equalsIgnoreCase( "itm_default" ) )
				{
					lineNo = checkNull(genericUtility.getColumnValue("line_no", dom));
				}	
				if( currentColumn.trim().equalsIgnoreCase( "site_code" ) )
				{
					siteCode = checkNull(genericUtility.getColumnValue("site_code", dom));
					siteCodeDescr = isExist("site", "site_code", siteCode, conn, "descr");
					valueXmlString.append("<site_descr>").append("<![CDATA["+siteCodeDescr+"]]>").append("</site_descr>");
				}	
				if( currentColumn.trim().equalsIgnoreCase( "state_code" ) )
				{
					stateCode = checkNull(genericUtility.getColumnValue("state_code", dom));
					stateCodeDescr = isExist("state", "state_code", stateCode, conn, "descr");
					valueXmlString.append("<state_descr>").append("<![CDATA["+stateCodeDescr+"]]>").append("</state_descr>");
				}	
				if( currentColumn.trim().equalsIgnoreCase( "count_code" ) )
				{
					countCode = checkNull(genericUtility.getColumnValue("count_code", dom));
					countCodeDescr = isExist("country", "count_code", countCode, conn, "descr");
					valueXmlString.append("<country_descr>").append("<![CDATA["+countCodeDescr+"]]>").append("</country_descr>");
				}	
				valueXmlString.append("</Detail2>");
				break;
			}	// case 2 end
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
	
	//Added by Jaffar S on 19-02-19 to get editflag which is not getting set
	private String getAttributeValue(Node node) throws Exception
	{
		String attVal = null;
		NodeList nodeList = null;
		Node detaulNode = null;
		Node detailNode = null;
		nodeList = node.getChildNodes();
			
		for(int ctr = 0; ctr < nodeList.getLength(); ctr++ )
		{
			detailNode = nodeList.item(ctr);
			if(detailNode.getNodeName().equalsIgnoreCase("attribute") )
			{
				attVal = detailNode.getAttributes().getNamedItem("updateFlag").getNodeValue();
				System.out.println("attVal["+attVal+"]");
			}
		}
			
		return attVal;

	}
}