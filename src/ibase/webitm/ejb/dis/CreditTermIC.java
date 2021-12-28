

/********************************************************
	Title : CreditTermIC
	Date  : 28/04/12
	Developer: Kunal Mandhre

 ********************************************************/

package ibase.webitm.ejb.dis;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.fin.FinCommon;
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
import javax.ejb.Stateless; 
@Stateless 

public class CreditTermIC extends ValidatorEJB implements CreditTermICLocal, CreditTermICRemote
{
	//Comment By Nasruddin 07-10-16 GenericUtility
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility = new E12GenericUtility();
	FinCommon finCommon=new FinCommon();
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = "";
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
		String descr = "";
		String crTerm = "";
		String startFrom = "";
		String lineNo = "";
		String taxCode = "";
		String acctCode = "";
		String cctrCode = "";
		String financialCharge = ""; 
		String childNodeName = null;
		String errString = "";
		String errCode = "";
		String userId = "";
		String sql = "";
        String errorType = "";
        String crType ="";
		int count = 0;
		int ctr=0;
		int currentFormNo = 0;
		int childNodeListLength;
		double minDay = 0;
		double maxDay = 0;
		double minCrAmount = 0;
		double maxCrAmount = 0; 
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
			}
			switch(currentFormNo)
			{
			case 1 :
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				for(ctr = 0; ctr < childNodeListLength; ctr ++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if(childNodeName.equalsIgnoreCase("cr_term"))
					{
						crTerm = checkNull(genericUtility.getColumnValue("cr_term", dom));
						// changed By Nasruddin 14/SEP/16 START
						if(crTerm == null || crTerm.trim().length() == 0)
						{
							errCode = "VTCRTERM";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						// changed By Nasruddin 14/SEP/16 END
						if(editFlag.equalsIgnoreCase("A") && crTerm != null && crTerm.trim().length() > 0)
						{
							sql = "select count(*) from crterm where cr_term = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,crTerm);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count =  rs.getInt(1);
							}
							if(count > 0) 
							{
								errCode = "VMPMKY";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}

					}
					else if(childNodeName.equalsIgnoreCase("descr"))
					{
						descr = checkNull(genericUtility.getColumnValue("descr", dom));
						if(descr == null || descr.trim().length() == 0)
						{
							errCode = "VMDESCR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						// changed By Nasruddin 14/SEP/16 Start
						else
						{
							crTerm = checkNull(genericUtility.getColumnValue("cr_term", dom));
							
							sql = "select count(1) from crterm where cr_term <> ? and descr = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,crTerm);
							pstmt.setString(2, descr);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count =  rs.getInt(1);
							}
							if(count > 0) 
							{
								errCode = "VMDUPDESCR";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						// changed By Nasruddin 14/SEP/16 END 
					}
					else if(childNodeName.equalsIgnoreCase("start_from"))
					{    
						startFrom = genericUtility.getColumnValue("start_from", dom);
						if(startFrom == null || startFrom.trim().length() == 0)
						{
							errCode = "VTSTARTERR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
                    }
                    else if(childNodeName.equalsIgnoreCase("cr_type")) //Added By mukesh on 17/09/2020 Start
                    {
 
                        crType = checkNull(genericUtility.getColumnValue("cr_type", dom));
                        startFrom = genericUtility.getColumnValue("start_from", dom);

                        if ("R".equalsIgnoreCase(crType))
                        {
                            //changed condition by Varsha V on 16-10-2020 as per suggested by PP Sir because condition was wrong
                            if(!"D".equalsIgnoreCase(startFrom) && !"L".equalsIgnoreCase(startFrom))
                            {
                               errCode = "VMCRTEMREC";
                               errList.add(errCode);
                               errFields.add(childNodeName.toLowerCase());
                            }

                        }
                        if ("P".equalsIgnoreCase(crType)) 
                        {
                            //changed condition by Varsha V on 19-10-2020 as per suggested by PP Sir because condition wrong
                            if(!"R".equalsIgnoreCase(startFrom) && !"B".equalsIgnoreCase(startFrom) && !"Q".equalsIgnoreCase(startFrom))
                            {
                               errCode = "VMCRTEMPAY";
                               errList.add(errCode);
                               errFields.add(childNodeName.toLowerCase());
                            }
                        }  
                    }//END
				}
				break;


			case 2 :
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				for(ctr = 0; ctr < childNodeListLength; ctr ++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if(childNodeName.equalsIgnoreCase("min_day"))
					{
						//crTerm = checkNull(genericUtility.getColumnValue("cr_term", dom));
						crTerm = crTerm;
						lineNo = checkNull(genericUtility.getColumnValue("line_no", dom));
						minDay = convertInt(genericUtility.getColumnValue("min_day", dom));
						maxDay = convertInt(genericUtility.getColumnValue("max_day", dom));
						minCrAmount = convertInt(genericUtility.getColumnValue("min_cramt", dom));
						maxCrAmount = convertInt(genericUtility.getColumnValue("max_cramt", dom));

						if(maxDay < minDay)
						{
							errCode = "VMMINDAY";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							sql = "select count(*) from crtermfc where cr_term = ? and "
									+" (min_day <> ?  or min_cramt <> ?) and ? between min_day and max_day and "
									+" (? between min_cramt and max_cramt or ? between min_cramt and max_cramt) and "
									+" line_no <> ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,crTerm);
							pstmt.setDouble(2, minDay);
							pstmt.setDouble(3, minCrAmount);
							pstmt.setDouble(4, minDay);
							pstmt.setDouble(5, minCrAmount);
							pstmt.setDouble(6, maxCrAmount);
							pstmt.setString(7, lineNo);

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
								errCode = "VTCRTERM2";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							else
							{
								sql = "select count(*) from crtermfc where cr_term = ? and "
										+" (min_day <> ?  or min_cramt <> ?) and max_day between ? and ? and "
										+" (min_cramt between ? and ? or max_cramt between ? and ?) and "
										+" line_no <> ?";
								pstmt =  conn.prepareStatement(sql);
								pstmt.setString(1,crTerm);
								pstmt.setDouble(2, minDay);
								pstmt.setDouble(3, minCrAmount);
								pstmt.setDouble(4, minDay);
								pstmt.setDouble(5, maxDay);
								pstmt.setDouble(6, minCrAmount);
								pstmt.setDouble(7, maxCrAmount);
								pstmt.setDouble(8, minCrAmount);
								pstmt.setDouble(9, maxCrAmount);
								pstmt.setString(10, lineNo);
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
									errCode = "VTCRTERM2";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								else
								{
									
								// Changed By Nasruddin 14/SEP/16  START 
									
									sql = "select count(*) from crtermfc where cr_term = ? and "
											+" (min_day <> ?  or min_cramt <> ?) and max_day between ? and ? and "
											+" (min_cramt between ? and ? or max_cramt between ? and ?) and "
											+" line_no <> ?";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1,crTerm);
									pstmt.setDouble(2, minDay);
									pstmt.setDouble(3, minCrAmount);
									pstmt.setDouble(4, minDay);
									pstmt.setDouble(5, maxDay);
									pstmt.setDouble(6, minCrAmount);
									pstmt.setDouble(7, maxCrAmount);
									pstmt.setDouble(8, minCrAmount);
									pstmt.setDouble(9, maxCrAmount);
									pstmt.setString(10, lineNo);
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
										errCode = "VTCRTERM2";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
								}
								// Changed By Nasruddin 14/SEP/16  END 
							}
						}
					}
					else if(childNodeName.equalsIgnoreCase("min_cramt"))
					{
						minCrAmount = convertInt(genericUtility.getColumnValue("min_cramt", dom));
						maxCrAmount = convertInt(genericUtility.getColumnValue("max_cramt", dom));
						if(minCrAmount < 0)
						{
							errCode = "VMMINAMT1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						if(maxCrAmount < minCrAmount)
						{
							errCode = "VMMINAMT";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if(childNodeName.equalsIgnoreCase("tax_code"))
					{
						taxCode = checkNull(genericUtility.getColumnValue("tax_code", dom));
						if(taxCode != null && taxCode.trim().length() > 0)
						{
							sql = "Select Count(*) from tax where tax_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,taxCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count =  rs.getInt(1);
							}
							if(count == 0) 
							{
								errCode = "VTTAX1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
			         /*
					else if(childNodeName.equalsIgnoreCase("fin_chg"))
					{
						financialCharge = checkNull(genericUtility.getColumnValue("fin_chg", dom));
						System.out.println("352fIN CH"+financialCharge);
						if(financialCharge == null && taxCode.trim().length() == 0)
						{
							errCode = "VMFINENT1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}*/
				}
				break;
			case 3 :
				parentNodeList = dom.getElementsByTagName("Detail3");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();
				for(ctr = 0; ctr < childNodeListLength; ctr ++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if(childNodeName.equalsIgnoreCase("acct_code"))
					{
						acctCode = checkNull(genericUtility.getColumnValue("acct_code", dom));
						if(acctCode != null && acctCode.trim().length() > 0)
						{
							sql = "select count(*) from accounts where acct_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,acctCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count =  rs.getInt(1);
							}
							if(count == 0) 
							{
								errCode = "VMACCT1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					else if(childNodeName.equalsIgnoreCase("cctr_code"))
					{
						cctrCode = checkNull(genericUtility.getColumnValue("cctr_code", dom));
						if(cctrCode != null && cctrCode.trim().length() > 0)
						{
							/*sql = "select count(*) from costctr where cctr_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,cctrCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count =  rs.getInt(1);
							}
							if(count == 0) 
							{
								errCode = "VMCCTRCD";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;*/
							//added by manish mhatre on 3-jan-2020
							errCode = finCommon.isCctrCode(acctCode, cctrCode, " ", conn);
							if (errCode != null && errCode.trim().length() > 0) {
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}//end manish
						}
					}
				}
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
					System.out.println("errCode .........." + errCode);
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
		System.out.println("xmlString1............"+xmlString);
		System.out.println("xmlString2............"+xmlString);
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
			System.out.println("Exception : [CreditTermIc][itemChanged( String, String )] :==>\n" + e.getMessage());
		}
		return valueXmlString;
	}

	// method for item change
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		String crTerm = "";
		String lineNo = ""; 
		String countCode = "";
		String taxCode = "";
		String descr = "";
		String type= "";
		String childNodeName = null;
		String sql = "";
		StringBuffer valueXmlString = new StringBuffer();
		int ctr = 0;
		int currentFormNo = 0;
		int childNodeListLength = 0;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		//Comment By Nasruddin 07-10-16 GenericUtility
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility = new E12GenericUtility();
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
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");
			switch(currentFormNo)
			{

			case 2 : 
				parentNodeList = dom.getElementsByTagName("Detail2");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail2>");
				childNodeListLength = childNodeList.getLength();
				do
				{   
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					ctr ++;
				}while(ctr < childNodeListLength  && ! childNodeName.equals(currentColumn));

				if(currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					crTerm = genericUtility.getColumnValue("cr_term", dom1);
					valueXmlString.append("<cr_term>").append("<![CDATA[" +  crTerm + "]]>").append("</cr_term>");
					//lineNo = genericUtility.getColumnValue("line_no", dom);
					lineNo = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams,"lineNo"));
					/*valueXmlString.append("<line_no>").append("<![CDATA[" +  lineNo + "]]>").append("</line_no>");*/
				}
				else if(currentColumn.trim().equalsIgnoreCase("tax_code"))
				{
					taxCode = genericUtility.getColumnValue("tax_code", dom);
					if(taxCode != null && taxCode.trim().length() > 0)
					{

						sql = "Select descr  from tax where tax_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,taxCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{

							descr = rs.getString(1);
							valueXmlString.append("<tax_descr>").append("<![CDATA[" + descr +"]]>").append("</tax_descr>");
						}
						else
						{
							valueXmlString.append("<tax_descr>").append("<![CDATA[]]>").append("</tax_descr>");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					else
					{
						valueXmlString.append("<tax_descr>").append("<![CDATA[]]>").append("</tax_descr>");
					}
				}
				valueXmlString.append("</Detail2>");
				break; 
			case 3 : 
				parentNodeList = dom.getElementsByTagName("Detail3");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail3>");
				childNodeListLength = childNodeList.getLength();
				do
				{   
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					ctr ++;
				}while(ctr < childNodeListLength  && ! childNodeName.equals(currentColumn));

				if(currentColumn.trim().equalsIgnoreCase("itm_default"))
				{
					crTerm = genericUtility.getColumnValue("cr_term", dom1);
					valueXmlString.append("<cr_term>").append("<![CDATA[" +  crTerm + "]]>").append("</cr_term>");
				}
				else if(currentColumn.trim().equalsIgnoreCase("itm_defaultedit"))
				{
					type = genericUtility.getColumnValue("type", dom);
					System.out.println("637 TYPE = "+type);
					if(type != null && type.trim().length() > 0)
					{
						valueXmlString.append("<type protect = \"1\">").append("<![CDATA[" +  type + "]]>").append("</type>");
					}
					else 
						valueXmlString.append("<type protect = \"1\">").append("<![CDATA[]]>").append("</type>");
				}
				valueXmlString.append("</Detail3>");
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
	private int convertInt(String input)
	{
		if(input == null || input.trim().length() == 0)
		{
			return 0;
		}
		else
		{
			return Integer.parseInt(input);
		}
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
