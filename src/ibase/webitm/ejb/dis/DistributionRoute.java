

/********************************************************
	Title : DistributionRoute
	Date  : 31/05/12
	Developer: Kunal Mandhre

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
import javax.ejb.Stateless;
import javax.swing.text.DefaultEditorKit.CutAction;
@Stateless  
public class DistributionRoute extends ValidatorEJB implements DistributionRouteLocal,DistributionRouteRemote
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
		String StanCodeFrom = "";
		String StanCodeTo = "";
		String stateCodeTo = "";
		String stateCodeFrom = "";
		String currCode = "";
		String tranCode = "";
		String descr = "";
		String distRoute = "";
		String custCode = "";
		String pinCodeTo = "";
		String pinCodeFrom = "";
		String locType = "";
		String frtAmount="";
		String maxWeight="";
		String minWeight="";
		String childNodeName = null;
		String errString = "";
		String errCode = "";
		String userId = "";
		String sql = "";
		String errorType = "";
		String minCase = "", maxCase = "";//Changed by sumit on 02/01/13
		int count = 0;
		int ctr=0;
		int currentFormNo = 0;
		int childNodeListLength;
		boolean fromBlank = false;
		boolean toBlank = false;
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
		boolean flag=false;
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
					if(childNodeName.equalsIgnoreCase("dist_route"))
					{
						distRoute = checkNull(genericUtility.getColumnValue("dist_route", dom));

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
					}
					else if(childNodeName.equalsIgnoreCase("cust_code"))
					{
						custCode = checkNull(genericUtility.getColumnValue("cust_code", dom));

						if(custCode != null && custCode.trim().length() > 0)
						{
							sql = "Select Count(*) from customer where cust_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,custCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count =  rs.getInt(1);
							}
							if(count == 0) 
							{
								errCode = "VTCUSTCD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					else if(childNodeName.equalsIgnoreCase("curr_code"))
					{
						currCode = checkNull(genericUtility.getColumnValue("curr_code", dom));
						if(currCode == null || currCode.trim().length() == 0)
						{
							errCode = "VTCURRBK";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else if(currCode != null && currCode.trim().length() > 0)
						{
							sql = "select count(*) from currency where curr_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,currCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count =  rs.getInt(1);
							}
							if(count == 0) 
							{
								errCode = "VTCURRCD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					else if(childNodeName.equalsIgnoreCase("frt_amt"))
					{
						frtAmount = checkNull(genericUtility.getColumnValue("frt_amt", dom));
						System.out.println("frtAmount ["+frtAmount+"]");
						if(frtAmount == null || frtAmount.trim().length() == 0)
						{
							System.out.println(" in side if");
							errCode = "VFRAMTBK";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					/*else if(currCode != null && currCode.trim().length() > 0)
						{
							sql = "select count(*) from distroute where frt_amt = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,frtAmount);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count =  rs.getInt(1);
							}
							if(count == 0) 
							{
								errCode = "VTFRAMT1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}*/

					else if(childNodeName.equalsIgnoreCase("stan_code__fr"))
					{
						StanCodeFrom = checkNull(genericUtility.getColumnValue("stan_code__fr", dom));
						custCode = genericUtility.getColumnValue("cust_code", dom);
						if(custCode == null)
						{
							custCode = "          ";
						}

						if(StanCodeFrom != null && StanCodeFrom.trim().length() > 0)
						{
							sql = "select count(*)  from station where stan_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,StanCodeFrom);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count =  rs.getInt(1);
							}
							if(count == 0) 
							{
								errCode = "VTSTAN1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					else if(childNodeName.equalsIgnoreCase("stan_code__to"))
					{
						StanCodeTo = checkNull(genericUtility.getColumnValue("stan_code__to", dom));
						custCode = genericUtility.getColumnValue("cust_code", dom);
						/*if(custCode == null)
						{
							custCode = "          ";
						}*/

						if(StanCodeTo != null && StanCodeTo.trim().length() > 0)
						{
							sql = "select count(*)  from station where stan_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,StanCodeTo);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count =  rs.getInt(1);
							}
							if(count == 0) 
							{
								errCode = "VTSTAN";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;

							if(editFlag != null && editFlag.trim().equalsIgnoreCase("A"))
							{
								if(custCode != null && custCode.trim().length() > 0)
								{

									sql = "select count(*) from distroute where stan_code__fr = ?   and stan_code__to = ?   and cust_code =  ? ";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1,StanCodeFrom);
									pstmt.setString(2,StanCodeTo);
									pstmt.setString(3,custCode); //added by kunal on 22/05/13 
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										count =  rs.getInt(1);
									}
									if(count > 0) 
									{
										errCode = "VTSTANDB";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								}
								else
								{
									sql = "select count(*) from distroute where stan_code__fr = ?   and stan_code__to = ?    ";
									pstmt =  conn.prepareStatement(sql);
									pstmt.setString(1,StanCodeFrom);
									pstmt.setString(2,StanCodeTo);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										count =  rs.getInt(1);
									}
									if(count > 0) 
									{
										errCode = "VTSTANDB";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}
									rs.close();
									rs = null;
									pstmt.close();
									pstmt = null;
								}
							}


						}
					}

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
					if(childNodeName.equalsIgnoreCase("stan_code__fr"))
					{
						StanCodeFrom = checkNull(genericUtility.getColumnValue("stan_code__fr", dom));
						if(StanCodeFrom == null || StanCodeFrom.trim().length() == 0)
						{
							//errCode = "VMSTANCOD ";        
							//errList.add(errCode);
							//errFields.add(childNodeName.toLowerCase());
							/*    //Changed by Sankara on 01/10/12 statin code from allowed blank [start]   
							errCode = "STNCDFRBK ";                       
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());   */
							//Changed by Sankara on 01/10/12 statin code from allowed blank [end]   
						}
						else if(StanCodeFrom != null && StanCodeFrom.trim().length() > 0)
						{
							sql = "select count(*)  from station where stan_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,StanCodeFrom);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count =  rs.getInt(1);
							}
							if(count == 0) 
							{
								errCode = "VTSTAN1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					else if(childNodeName.equalsIgnoreCase("stan_code__to"))
					{
						StanCodeTo = checkNull(genericUtility.getColumnValue("stan_code__to", dom));
						if(StanCodeTo == null || StanCodeTo.trim().length() == 0)
						{
							//errCode = "VMSTANCOD ";
							//errList.add(errCode);
							//errFields.add(childNodeName.toLowerCase());
							//Changed by Sankara on 01/10/12 station code to allowed blank [start]   
							/*       	errCode = "STNCODTOBK ";                         
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());       */
							//Changed by Sankara on 01/10/12 station code to allowed blank [end] 
						}
						else if(StanCodeTo != null && StanCodeTo.trim().length() > 0)
						{
							sql = "select count(*)  from station where stan_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,StanCodeTo);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count =  rs.getInt(1);
							}
							if(count == 0) 
							{
								errCode = "VTSTAN";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					else if(childNodeName.equalsIgnoreCase("tran_code"))
					{
						tranCode = checkNull(genericUtility.getColumnValue("tran_code", dom));
						if(tranCode == null || tranCode.trim().length() == 0)
						{
							errCode = "VMTRANCD1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else if(tranCode != null && tranCode.trim().length() > 0)
						{
							sql = "select count(*) from transporter where tran_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,tranCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count =  rs.getInt(1);
							}
							if(count == 0) 
							{
								errCode = "VTTRANCD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					else if(childNodeName.equalsIgnoreCase("frt_amt"))
					{
						frtAmount = checkNull(genericUtility.getColumnValue("frt_amt", dom));
						System.out.println("frtAmount ["+frtAmount+"]");
						if(frtAmount == null || frtAmount.trim().length() == 0)
						{
							System.out.println(" in side if");
							errCode = "VFRAMTBK";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if(childNodeName.equalsIgnoreCase("curr_code"))
					{
						currCode = checkNull(genericUtility.getColumnValue("curr_code", dom));
						if(currCode == null || currCode.trim().length() == 0)
						{
							/*errCode = "VTCURRBK";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase()); */
						}
						else if(currCode != null && currCode.trim().length() > 0)
						{
							sql = "select count(*) from currency where curr_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,currCode);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								count =  rs.getInt(1);
							}
							if(count == 0) 
							{
								errCode = "VTCURRCD1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}    

					else if(childNodeName.equalsIgnoreCase("min_weight"))    
					{
						minWeight = checkNull(genericUtility.getColumnValue("min_weight", dom));
						if(minWeight == null || minWeight.trim().length() == 0)
						{
							errCode = "VTMINWTBK";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}  
					else if(childNodeName.equalsIgnoreCase("max_weight")) 
					{
						maxWeight = checkNull(genericUtility.getColumnValue("max_weight", dom));
						minWeight = checkNull(genericUtility.getColumnValue("min_weight", dom));
						if(maxWeight == null || maxWeight.trim().length() == 0)
						{
							errCode = "VTMAXWTBK";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						//Changed by sumit on 21/01/13 adding validation to prevent not less than min weight start
						else
						{
							if( minWeight != null && minWeight.trim().length() > 0 && Double.parseDouble(maxWeight) < Double.parseDouble(minWeight))
							{
								errCode = "VTLESMAXWA";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						//Changed by sumit on 21/01/13 adding validation to prevent not less than min case end
					}
					//Changed by sumit on 02/01/13 adding validation for max case and min case start.
					else if(childNodeName.equalsIgnoreCase("min_case")) 
					{
						minCase = checkNull(genericUtility.getColumnValue("min_case", dom));
						if(minCase == null || minCase.trim().length() == 0)
						{
							errCode = "VTMINCASE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if(childNodeName.equalsIgnoreCase("max_case")) 
					{
						maxCase = checkNull(genericUtility.getColumnValue("max_case", dom));
						minCase = checkNull(genericUtility.getColumnValue("min_case", dom));
						System.out.println(" maxCase ["+maxCase+"] minCase ["+minCase+"]");
						if(maxCase == null || maxCase.trim().length() == 0)
						{
							errCode = "VTMAXCASE";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						//Changed by sumit on 21/01/13 adding validation to prevent not less than min case start
						else
						{
							if( minCase != null && minCase.trim().length() > 0 && Double.parseDouble(maxCase) < Double.parseDouble(minCase))
							{
								errCode = "VTLESMAXCA";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
						}
						//Changed by sumit on 21/01/13 adding validation to prevent not less than min case end
					}

					//Changed by sumit on 02/01/13 adding validation for max case and min case end.
					/*else if(childNodeName.equalsIgnoreCase("state_code__fr"))
					 {
						stateCodeFrom = checkNull(genericUtility.getColumnValue("state_code__fr", dom));
						if(stateCodeFrom == null || stateCodeFrom.trim().length() == 0)
						{
							errCode = "VTSTATBK";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else if(stateCodeFrom != null && stateCodeFrom.trim().length() > 0)
						{
						sql = "Select count(*) from state where state_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,stateCodeFrom);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							count =  rs.getInt(1);
						}
						if(count == 0) 
						{
							errCode = "VTSTATE1  ";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						}
					}
					else if(childNodeName.equalsIgnoreCase("state_code__to"))
					{
						stateCodeTo = checkNull(genericUtility.getColumnValue("state_code__to", dom));
						if(stateCodeTo == null || stateCodeTo.trim().length() == 0)
						{
							errCode = "VTSTATEBK";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else if(stateCodeTo != null && stateCodeTo.trim().length() > 0)
						{

						sql = "Select count(*) from state where state_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,stateCodeTo);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							count =  rs.getInt(1);
						}
						if(count == 0) 
						{
							errCode = "VTSTATE2  ";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						}
					}*/
					/*
						sql = "Select count(*) from station where stan_code = ? and state_code = ? ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,StanCodeTo);
						pstmt.setString(2,stateCodeTo);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							count =  rs.getInt(1);
						}
						if(count == 0) 
						{
							errCode = "VTSTATETO";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

				        sql = "Select count(*) from station where stan_code = ? and state_code = ? ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,StanCodeFrom);
						pstmt.setString(2,stateCodeFrom);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							count =  rs.getInt(1);
						}
						if(count == 0) 
						{
							errCode = "VTSTATEFR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					 */
					else if(childNodeName.equalsIgnoreCase("pin__fr"))    
					{
						pinCodeFrom = checkNull(genericUtility.getColumnValue("pin__fr", dom));
						if(pinCodeFrom == null || pinCodeFrom.trim().length() == 0)
						{
							errCode = "VTPINFRBK";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if(childNodeName.equalsIgnoreCase("pin__to"))   
					{
						pinCodeTo = checkNull(genericUtility.getColumnValue("pin__to", dom));
						if(pinCodeTo == null || pinCodeTo.trim().length() == 0)
						{
							errCode = "VTPINTOBK";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					else if(childNodeName.equalsIgnoreCase("loc_type"))
					{
						locType = checkNull(genericUtility.getColumnValue("loc_type", dom));

						if(locType != null && locType.trim().length() > 0)
						{
							sql = "SELECT trim(FLD_VALUE), descr FROM GENCODES WHERE  FLD_NAME  IN ('LOC_TYPE') AND fld_value= ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,locType.trim());
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								flag=true;
								//count =  rs.getInt(1);
							}
							if(flag==false) 
							{
								errCode = "VTLOCINV";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}

					else
					{
						stateCodeFrom = checkNull(genericUtility.getColumnValue("state_code__fr", dom));
						StanCodeFrom = checkNull(genericUtility.getColumnValue("stan_code__fr", dom));
						stateCodeTo = checkNull(genericUtility.getColumnValue("state_code__to", dom));
						StanCodeTo = checkNull(genericUtility.getColumnValue("stan_code__to", dom));
						pinCodeTo = checkNull(genericUtility.getColumnValue("pin__to", dom));
						pinCodeFrom = checkNull(genericUtility.getColumnValue("pin__fr", dom));

						if(StanCodeFrom.trim().length() == 0 && stateCodeFrom.trim().length() == 0 && pinCodeFrom.trim().length() == 0)
						{
							fromBlank = true;
						}
						if(StanCodeTo.trim().length() == 0 && stateCodeTo.trim().length() == 0 && pinCodeTo.trim().length() == 0)
						{
							toBlank = true;
						}
						if(fromBlank)
						{
							errCode = "VTFRVAL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						if(toBlank)
						{
							errCode = "VTTOVAL";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
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
		}//end try
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
	}

	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
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
			System.out.println("Exception : [DistributionRoute][itemChanged( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		return valueXmlString;
	}

	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		String stanCodeFr = "";
		String stanCodeTo = "";
		String stateCodeTo = "";
		String stateCodeFr = "";
		String currCode = ""; 
		String custCode = "";
		String descr = "";
		String distRoute = "";
		String tranCode = "";
		String stateDescr = "";
		String childNodeName = null;
		String sql = "";
		String pinCodeTo = "";
		String pinCodeFrom = "";
		StringBuffer valueXmlString = new StringBuffer();
		int ctr = 0;
		int lineNo = 0;
		int currentFormNo = 0;
		int childNodeListLength = 0;
		double exchRate  = 0.0;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
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
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");
			switch(currentFormNo)
			{
			case 1 : 
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				valueXmlString.append("<Detail1>");
				childNodeListLength = childNodeList.getLength();
				do
				{   
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					ctr ++;
				}while(ctr < childNodeListLength  && ! childNodeName.equals(currentColumn));

				if(currentColumn.trim().equalsIgnoreCase("stan_code__fr"))
				{
					stanCodeFr = checkNull(genericUtility.getColumnValue("stan_code__fr", dom));
					System.out.println("stanCodeFr = "+stanCodeFr);
					sql = "Select descr  from station where stan_code = ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,stanCodeFr);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						descr = rs.getString(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					//	valueXmlString.append("<stationa_descr>").append("<![CDATA[" + descr +"]]>").append("</stationa_descr>");
					valueXmlString.append("<stationa_descr>").append("<![CDATA[" + descr +"]]>").append("</stationa_descr>"); 
				}
				else if(currentColumn.trim().equalsIgnoreCase("stan_code__to"))
				{
					stanCodeTo = checkNull(genericUtility.getColumnValue("stan_code__to", dom));
					sql = "Select descr  from station where stan_code = ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,stanCodeTo);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						descr = rs.getString(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<station_descr_1>").append("<![CDATA[" + descr +"]]>").append("</station_descr_1>");
				}
				else if(currentColumn.trim().equalsIgnoreCase("curr_code"))
				{
					currCode = checkNull(genericUtility.getColumnValue("curr_code", dom));
					sql = "Select descr  from currency where curr_code = ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,currCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						descr = rs.getString(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<currency_descr>").append("<![CDATA[" + descr +"]]>").append("</currency_descr>");
				}
				else if(currentColumn.trim().equalsIgnoreCase("cust_code"))
				{
					custCode = checkNull(genericUtility.getColumnValue("cust_code", dom));
					if(custCode != null && custCode.trim().length() > 0)
					{
						sql = "select cust_name from customer where cust_code = ? ";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,custCode);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descr = rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					valueXmlString.append("<cust_name>").append("<![CDATA[" + descr +"]]>").append("</cust_name>");
				}
				valueXmlString.append("</Detail1>");
				break;       
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
					System.out.println("itm dedault called........");
					distRoute = checkNull(genericUtility.getColumnValue("dist_route", dom1));
					System.out.println("693 distRoute = "+distRoute);					
					valueXmlString.append("<dist_route protect = \"1\">").append("<![CDATA[" + distRoute +"]]>").append("</dist_route>");

					stateCodeFr = checkNull(genericUtility.getColumnValue("state_code__fr", dom));
					stanCodeFr = checkNull(genericUtility.getColumnValue("stan_code__fr", dom));
					stateCodeTo = checkNull(genericUtility.getColumnValue("state_code__to", dom));
					stanCodeTo = checkNull(genericUtility.getColumnValue("stan_code__to", dom));
					pinCodeTo = checkNull(genericUtility.getColumnValue("pin__to", dom));
					pinCodeFrom = checkNull(genericUtility.getColumnValue("pin__fr", dom));
					if(stateCodeFr.trim().length() == 0)
					{
						valueXmlString.append("<state_code__fr>").append("<![CDATA[ ]]>").append("</state_code__fr>");
					}
					if(stateCodeTo.trim().length() == 0)
					{
						valueXmlString.append("<state_code__to>").append("<![CDATA[ ]]>").append("</state_code__to>");
					}
					if(stanCodeFr.trim().length() == 0)
					{
						valueXmlString.append("<stan_code__fr>").append("<![CDATA[ ]]>").append("</stan_code__fr>");
					}
					if(stanCodeTo.trim().length() == 0)
					{
						valueXmlString.append("<stan_code__to>").append("<![CDATA[ ]]>").append("</stan_code__to>");
					}
					if(pinCodeTo.trim().length() == 0)
					{
						valueXmlString.append("<pin__to>").append("<![CDATA[ ]]>").append("</pin__to>");
					}
					if(pinCodeFrom.trim().length() == 0)
					{
						valueXmlString.append("<pin__fr>").append("<![CDATA[ ]]>").append("</pin__fr>");
					}
					//Changed by sumit on 30/01/13 setting default value start
					valueXmlString.append("<frt_amt>").append("<![CDATA[0.0]]>").append("</frt_amt>");
					valueXmlString.append("<min_weight>").append("<![CDATA[0.0]]>").append("</min_weight>");
					valueXmlString.append("<max_weight>").append("<![CDATA[0.0]]>").append("</max_weight>");
					valueXmlString.append("<min_case>").append("<![CDATA[0]]>").append("</min_case>");
					valueXmlString.append("<max_case>").append("<![CDATA[0]]>").append("</max_case>");
					//Changed by sumit on 30/01/13 setting default value start

				}
				else if(currentColumn.trim().equalsIgnoreCase("stan_code__fr"))
				{
					stanCodeFr = checkNull(genericUtility.getColumnValue("stan_code__fr", dom));
					stateCodeFr = "";
					stateDescr = "";
					descr = "";
					if(stanCodeFr != null && stanCodeFr.trim().length() > 0)
					{
						sql = "Select descr,state_code from station where stan_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,stanCodeFr.trim());
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descr = checkNull(rs.getString("descr"));
							stateCodeFr = checkNull(rs.getString("state_code"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if(stateCodeFr != null && stateCodeFr.trim().length() > 0)
						{
							sql = "select  descr from state where state_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,stateCodeFr);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								stateDescr = rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					valueXmlString.append("<state_code__fr>").append("<![CDATA[" + stateCodeFr +"]]>").append("</state_code__fr>");
					valueXmlString.append("<descr>").append("<![CDATA[" + stateDescr +"]]>").append("</descr>");
					valueXmlString.append("<stationa_descr>").append("<![CDATA[" + descr +"]]>").append("</stationa_descr>");

					if(stateCodeFr.trim().length() == 0)
					{
						valueXmlString.append("<state_code__fr>").append("<![CDATA[ ]]>").append("</state_code__fr>");
					}
					if(stanCodeFr.trim().length() == 0)
					{
						valueXmlString.append("<stan_code__fr>").append("<![CDATA[ ]]>").append("</stan_code__fr>");
					}

				}
				else if(currentColumn.trim().equalsIgnoreCase("stan_code__to"))
				{
					stanCodeTo = checkNull(genericUtility.getColumnValue("stan_code__to", dom));
					stateCodeTo = "";
					descr = "";
					stateDescr = "";
					if(stanCodeTo != null && stanCodeTo.trim().length() > 0)
					{
						sql = "Select descr,state_code from station where stan_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,stanCodeTo.trim());
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descr = checkNull(rs.getString("descr"));
							stateCodeTo = checkNull(rs.getString("state_code"));
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if(stateCodeTo != null && stateCodeTo.trim().length() > 0)
						{
							sql = "select  descr from state where state_code = ?";
							pstmt =  conn.prepareStatement(sql);
							pstmt.setString(1,stateCodeTo);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								stateDescr = rs.getString(1);
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
					}
					valueXmlString.append("<state_code__to>").append("<![CDATA[" + stateCodeTo +"]]>").append("</state_code__to>");
					valueXmlString.append("<station_descr_1>").append("<![CDATA[" + descr +"]]>").append("</station_descr_1>");
					valueXmlString.append("<descr_1>").append("<![CDATA[" + stateDescr +"]]>").append("</descr_1>");

					if(stanCodeTo.trim().length() == 0)
					{
						valueXmlString.append("<stan_code__to>").append("<![CDATA[ ]]>").append("</stan_code__to>");
					}
					if(stateCodeTo.trim().length() == 0)
					{
						valueXmlString.append("<state_code__to>").append("<![CDATA[ ]]>").append("</state_code__to>");
					}
				}
				else if(currentColumn.trim().equalsIgnoreCase("tran_code"))
				{
					tranCode = checkNull(genericUtility.getColumnValue("tran_code", dom));
					sql = "Select tran_name from transporter where tran_code = ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,tranCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						descr = rs.getString(1);
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;           
					//valueXmlString.append("<tran_name>").append(descr).append("</tran_name>");
					valueXmlString.append("<transporter_tran_name>").append("<![CDATA[" + descr +"]]>").append("</transporter_tran_name>\r\n");
				}
				else if(currentColumn.trim().equalsIgnoreCase("curr_code"))
				{
					currCode = checkNull(genericUtility.getColumnValue("curr_code", dom));
					sql = "select descr,std_exrt from currency where curr_code =  ?";
					pstmt =  conn.prepareStatement(sql);
					pstmt.setString(1,currCode);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						descr = rs.getString("descr")== null ? "":rs.getString("descr");
						exchRate = rs.getDouble("std_exrt");
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					valueXmlString.append("<currency_descr>").append("<![CDATA[" + descr +"]]>").append("</currency_descr>");
					valueXmlString.append("<exch_rate>").append("<![CDATA[" + exchRate +"]]>").append("</exch_rate>");
				}
				else if(currentColumn.trim().equalsIgnoreCase("state_code__to"))
				{
					stateCodeTo = checkNull(genericUtility.getColumnValue("state_code__to", dom));

					if(stateCodeTo != null && stateCodeTo.trim().length() > 0)
					{
						sql = "select  descr from state where state_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,stateCodeTo);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descr = rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					valueXmlString.append("<descr_1>").append("<![CDATA[" + descr +"]]>").append("</descr_1>");
					if(stateCodeTo.trim().length() == 0)
					{
						valueXmlString.append("<state_code__to>").append("<![CDATA[ ]]>").append("</state_code__to>");
					}
				}
				else if(currentColumn.trim().equalsIgnoreCase("state_code__fr"))
				{
					stateCodeFr = checkNull(genericUtility.getColumnValue("state_code__fr", dom));
					if(stateCodeFr != null && stateCodeFr.trim().length() > 0)
					{
						sql = "select  descr from state where state_code = ?";
						pstmt =  conn.prepareStatement(sql);
						pstmt.setString(1,stateCodeFr);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descr = rs.getString(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
					valueXmlString.append("<descr>").append("<![CDATA[" + descr +"]]>").append("</descr>");
					if(stateCodeFr.trim().length() == 0)
					{
						valueXmlString.append("<state_code__fr>").append("<![CDATA[ ]]>").append("</state_code__fr>");
					}
				}

				valueXmlString.append("</Detail2>");
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
			while(rs.next())
			{
				msgType = rs.getString("MSG_TYPE");
			}			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new ITMException(ex); //Added By Mukesh Chauhan on 07/08/19
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