/********************************************************
	Title : RoadPermitIC[DI3HSUN004]
	Date  : 17/12/13
	Developer: Chandrashekar

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
import java.sql.Timestamp;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.ejb.Stateless; 
@Stateless 

public class RoadPermitIC extends ValidatorEJB implements RoadPermitICLocal, RoadPermitICRemote
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
			System.out.println("Exception : [RoadPermit][wfValData( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e);
		}
		return(errString);
	}

	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String description = "", stationcodefrom = "", stationcodeto = "", permitdate = "", expirydate = "";
		String sitecodefrom = "", sitecodeto = "",statecodefrom = "", statecodeto="",finentity="",stancode="";
		String roadpermitno="";
		Timestamp fromDate=null, toDate=null;
		String childNodeName = null;
		String keyFlag="";
		String sql="";
		
		
		String errString = "";
		String errCode = "";
		String userId = "";
		String errorType = "";

		int ct=0;
		int count = 0;
		int ctr=0;
		int ctr1=0;
		int ctr2=0;
		int currentFormNo = 0;
		int childNodeListLength;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;	
		Node parentNode = null;
		Node childNode = null;
		boolean charFlag=false;
		boolean charFlag1=false;
		ArrayList<String> errList = new ArrayList<String>();
		ArrayList<String> errFields = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement pstmt = null ;
		ResultSet rs = null;
		ConnDriver connDriver = new ConnDriver();
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
		System.out.println("editFlag="+editFlag);
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
			case 1 :

				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength = childNodeList.getLength();


				for( ctr = 0; ctr < childNodeListLength;ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();

					if(childNodeName.equalsIgnoreCase("descr"))
					{   
						description = checkNull(genericUtility.getColumnValue("descr", dom));

						if(description == null || description.trim().length() == 0)
						{
							errCode = "VMDESCR";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}					
					}
					else
						if(childNodeName.equalsIgnoreCase("rd_permit_no"))
						{   
							roadpermitno = checkNull(genericUtility.getColumnValue("rd_permit_no", dom));
							System.out.println("RD_PERMIT_NO="+roadpermitno);

							sql = "select key_flag from transetup where tran_window='w_roadpermit'";
							pstmt = conn.prepareStatement(sql);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								keyFlag = rs.getString("key_flag");									
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if("M".equalsIgnoreCase(keyFlag) && "A".equalsIgnoreCase(editFlag))
							{
								if(roadpermitno == null || roadpermitno.trim().length() == 0  )
								{
									errCode = "VTRDPT";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
								else
								{
									sql = " select count (*) from roadpermit where rd_permit_no = ?   ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, roadpermitno);
									rs = pstmt.executeQuery();
									if(rs.next())
									{
										ct = rs.getInt(1);	
										System.out.println("ct>>>>>>>>>>>"+ct);
									}
									pstmt.close();
									pstmt = null;
									rs.close();
									rs = null;
									System.out.println("@@ct>>>>>>>>>>"+ct);
									if(ct > 0)
									{
										errCode = "VTRDPT2";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());
									}

								}

							}


						}
						else
							if(childNodeName.equalsIgnoreCase("fin_entity"))
							{   
								finentity = checkNull(genericUtility.getColumnValue("fin_entity", dom));
								if(finentity == null || finentity.trim().length() == 0)
								{
									errCode = "VMFINENT2";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}else 
									if(!(isExist(conn, "finent", "fin_entity" ,finentity)))
									{
										errCode = "VMFINENTM2";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());   
									}

							}else
								if(childNodeName.equalsIgnoreCase("site_code__fr"))
								{
									sitecodefrom = checkNull(genericUtility.getColumnValue("site_code__fr", dom));
									stancode = checkNull(genericUtility.getColumnValue("stan_code", dom));
									if (sitecodefrom == null || sitecodefrom.trim().length() == 0 )					
									{
										errCode = "VMSITECDFR";
										errList.add(errCode);
										errFields.add(childNodeName.toLowerCase());

									}else
										if(!(isExist(conn, "site", "site_code" ,sitecodefrom)))
										{
											errCode = "VMSITEFR";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());   
										}
								}else
									if(childNodeName.equalsIgnoreCase("site_code__to"))
									{
										sitecodeto = checkNull(genericUtility.getColumnValue("site_code__to", dom));
										if (sitecodeto == null || sitecodeto.trim().length() == 0 )					
										{
											errCode = "VMSITECDTO";
											errList.add(errCode);
											errFields.add(childNodeName.toLowerCase());

										}else
											if(!(isExist(conn, "site", "site_code" ,sitecodeto)))
											{
												errCode = "VMSITETO";
												errList.add(errCode);
												errFields.add(childNodeName.toLowerCase());   
											}						
									}else
										if(childNodeName.equalsIgnoreCase("state_code__fr"))
										{
											statecodefrom = genericUtility.getColumnValue("state_code__fr", dom);
											sitecodefrom = checkNull(genericUtility.getColumnValue("site_code__fr", dom));
											if (statecodefrom == null || statecodefrom.trim().length() == 0 )					
											{
												errCode = "VMSTATCDFR  ";
												errList.add(errCode);
												errFields.add(childNodeName.toLowerCase());

											}else
											{
												if(!(isExist(conn, "state", "state_code" ,statecodefrom)))
												{
													errCode = "VMSTATFR";
													errList.add(errCode);
													errFields.add(childNodeName.toLowerCase());   
												}else
												{
													sql = "select count(*) from site where state_code = ? and site_code = ?";			
													pstmt = conn.prepareStatement(sql);
													pstmt.setString(1, statecodefrom);
													pstmt.setString(2, sitecodefrom);
													rs = pstmt.executeQuery();
													if(rs.next())
													{
														ctr2 = rs.getInt(1);
													}
													rs.close();
													rs = null;
													pstmt.close();
													pstmt = null;
													System.out.println("@@@ctr>>>>>>>["+ctr2+"]");
													if(ctr2 == 0)
													{
														errCode = "VMSTATFRNA ";
														errList.add(errCode);
														errFields.add(childNodeName.toLowerCase());
													}
												}
											}
										}else if(childNodeName.equalsIgnoreCase("state_code__to"))
										{
											statecodeto = genericUtility.getColumnValue("state_code__to", dom);
											sitecodeto = checkNull(genericUtility.getColumnValue("site_code__to", dom));
											if (statecodeto == null || statecodeto.trim().length() == 0 )					
											{
												errCode = "VMSTATCDTO  ";
												errList.add(errCode);
												errFields.add(childNodeName.toLowerCase());

											}else 
											{
												if(!(isExist(conn, "state", "state_code" ,statecodeto)))
												{
													errCode = "VMSTATTO ";
													errList.add(errCode);
													errFields.add(childNodeName.toLowerCase());   
												}else
												{
													sql = "select count(*) from site where state_code = ? and site_code = ?";			
													pstmt = conn.prepareStatement(sql);
													pstmt.setString(1, statecodeto);
													pstmt.setString(2, sitecodeto);
													rs = pstmt.executeQuery();
													if(rs.next())
													{
														ctr2 = rs.getInt(1);
													}
													rs.close();
													rs = null;
													pstmt.close();
													pstmt = null;
													System.out.println("@@@ctr>>>>>>>["+ctr2+"]");
													if(ctr2 == 0)
													{
														errCode = "VMSTATTONA ";
														errList.add(errCode);
														errFields.add(childNodeName.toLowerCase());
													}
												}
											}
										}
										else if(childNodeName.equalsIgnoreCase("stan_code__from"))
										{
											stationcodefrom = genericUtility.getColumnValue("stan_code__from", dom);
											statecodefrom = checkNull(genericUtility.getColumnValue("state_code__fr", dom));
											if(stationcodefrom != null)
											{	System.out.println("stationcodefrom>>>>>>["+stationcodefrom+"]");
												System.out.println("stationcodefrom length["+stationcodefrom.length()+"]");
												 if(stationcodefrom.trim().length() == 0)
												 {	
													 	errCode = "VMSTANFR";
														errList.add(errCode);
														errFields.add(childNodeName.toLowerCase());   
													 
												 }else
												 {
													if(!(isExist(conn, "station", "stan_code" ,stationcodefrom)))
													{
														errCode = "VMSTANFR";
														errList.add(errCode);
														errFields.add(childNodeName.toLowerCase());   
													}else
													{
														sql = "select count(*) from station where stan_code = ? and state_code = ?";			
														pstmt = conn.prepareStatement(sql);
														pstmt.setString(1, stationcodefrom);
														pstmt.setString(2, statecodefrom);
														rs = pstmt.executeQuery();
														if(rs.next())
														{
															ctr1 = rs.getInt(1);
														}
														rs.close();
														rs = null;
														pstmt.close();
														pstmt = null;
														System.out.println("@@@ctr>>>>>>>["+ctr1+"]");
														if(ctr1 == 0)
														{
															errCode = "VMSTANCDFX";
															errList.add(errCode);
															errFields.add(childNodeName.toLowerCase());
														}
													  }
													}
											}

										}
										else
											if(childNodeName.equalsIgnoreCase("stan_code__to"))
											{
												stationcodeto = genericUtility.getColumnValue("stan_code__to", dom);
												statecodeto = checkNull(genericUtility.getColumnValue("state_code__to", dom));
												if(stationcodeto != null)
												{	
													System.out.println("stationcodeTO>>>>>>>>>>["+statecodeto+"]");
													if(stationcodeto.trim().length() == 0)
													 {	
														 	errCode = "VMSTANTO";
															errList.add(errCode);
															errFields.add(childNodeName.toLowerCase());   
														 
													 }else	
													 	{
														 	if(!(isExist(conn, "station", "stan_code" ,stationcodeto)))
														 		{
														 		errCode = "VMSTANTO";
														 		errList.add(errCode);
																errFields.add(childNodeName.toLowerCase());   
														 		}else
														 		{	
														 			sql = "select count(*) from station where stan_code = ? and state_code = ?";			
														 			pstmt = conn.prepareStatement(sql);
														 			pstmt.setString(1, stationcodeto);
														 			pstmt.setString(2, statecodeto);
														 			rs = pstmt.executeQuery();
														 			if(rs.next())
														 			{
														 				ctr2 = rs.getInt(1);
														 			}
														 			rs.close();
														 			rs = null;
														 			pstmt.close();
														 			pstmt = null;
														 			System.out.println("@@@ctr>>>>>>>["+ctr2+"]");
														 			if(ctr2 == 0)
														 			{
														 				errCode = "VMSTANCDTX";
														 				errList.add(errCode);
														 				errFields.add(childNodeName.toLowerCase());
														 			}
														 		}
													 	}

													}

											}
											else if(childNodeName.equalsIgnoreCase("permit_date"))
											{   
												permitdate = checkNull(genericUtility.getColumnValue("permit_date", dom));

												if(permitdate == null || permitdate.trim().length() == 0)
												{
													errCode = "VMPDTNUL1";
													errList.add(errCode);
													errFields.add(childNodeName.toLowerCase());
												}					
											}
											else
												if(childNodeName.equalsIgnoreCase("expiry_date"))
												{   permitdate = checkNull(genericUtility.getColumnValue("permit_date", dom));
												expirydate = checkNull(genericUtility.getColumnValue("expiry_date", dom));
												System.out.println("expirydate>>>>>>>>>>>>>>>>>"+expirydate);
												if(expirydate == null || expirydate.trim().length() == 0)
												{
													errCode = "VMEXPDTNUL";
													errList.add(errCode);
													errFields.add(childNodeName.toLowerCase());
												}
												else
												{	
													if(permitdate == null || permitdate.trim().length() == 0)
													
													{
														errCode = "VMPDTNUL1";
														errList.add(errCode);
														errFields.add(childNodeName.toLowerCase());
													}
													else
													{
														fromDate = Timestamp.valueOf(genericUtility.getValidDateString(expirydate, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
														toDate = Timestamp.valueOf(genericUtility.getValidDateString(permitdate, genericUtility.getApplDateFormat(),genericUtility.getDBDateFormat()) + " 00:00:00.0");
														System.out.println("comapring expirydate>>>>>>>>"+fromDate);
														System.out.println("comapring permitdate>>>>>>>>"+toDate);
														if(fromDate!=null && toDate!=null && toDate.after(fromDate))
														{
															errCode = "VMEXPDT1";
															errList.add(errCode);
															errFields.add(childNodeName.toLowerCase());

														}
													}

												}
												}


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
				break;
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
			System.out.println("Exception : [RoadPermitIC][itemChanged( String, String )] :==>\n" + e.getMessage());
			throw new ITMException(e);
		}
		return valueXmlString;
	}

	// method for item change
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		String finentity = "",finentdesc = "", statecodefrdesc = "", statecodetodesc = "";
		String sitecodefrom = "", sitecodeto = "", stationfrom ="", stationto ="";
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
				System.out.println("childNodeName>>>>>>>>>>>>>>"+childNodeName);
				if( currentColumn.trim().equalsIgnoreCase( "fin_entity" ) )
				{
					finentity  = checkNull(genericUtility.getColumnValue("fin_entity", dom));
					System.out.println("finentity>>>>>>>>>>>>"+finentity);
					sql = "select descr from finent where fin_entity = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, finentity);
					rs = pstmt.executeQuery();
					if (rs.next())
					{
						finentdesc = rs.getString("descr")==null?"":rs.getString("descr");

					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;

					valueXmlString.append("<finent_descr>").append("<![CDATA["+finentdesc+"]]>").append("</finent_descr>");
				}					
				
				else 
				if(currentColumn.trim().equalsIgnoreCase("site_code__fr"))
				{
					sitecodefrom  = checkNull(genericUtility.getColumnValue("site_code__fr", dom));
					stationfrom  = checkNull(genericUtility.getColumnValue("stan_code__from", dom));
											
						sql = " select state_code from site where site_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,sitecodefrom);
						rs = pstmt.executeQuery();
						if(rs.next())
						{  
							statecodefrdesc =  rs.getString("state_code")==null?"":rs.getString("state_code");

						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;	
						System.out.println("statecodefrdesc>>>>>>>"+statecodefrdesc);
						valueXmlString.append("<state_code__fr>").append("<![CDATA[" + statecodefrdesc +"]]>").append("</state_code__fr>");
						
						
				}else
					if(currentColumn.trim().equalsIgnoreCase("site_code__to"))
				    {
					sitecodeto = checkNull(genericUtility.getColumnValue("site_code__to", dom));
					sql = " select state_code from site where site_code = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1,sitecodeto);
					rs = pstmt.executeQuery();
					if(rs.next())
					{  

						statecodetodesc =  rs.getString("state_code")==null?"":rs.getString("state_code");

					}

					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					System.out.println("statecodetodesc>>>>>>>>>"+statecodetodesc);
					valueXmlString.append("<state_code__to>").append("<![CDATA[" + statecodetodesc +"]]>").append("</state_code__to>");
						
						
				}


				valueXmlString.append("</Detail1>");
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
	private boolean isExist(Connection conn, String tableName, String columnName, String value) throws  ITMException, RemoteException
	{
		PreparedStatement pstmt = null ;
		ResultSet rs = null ; 
		String sql = "";
		boolean status = false;
		try
		{			
			sql = "SELECT count(*) from " + tableName + " where " + columnName +"  = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,value);
			rs = pstmt.executeQuery();

			if(rs.next())
			{					 
				if(rs.getBoolean(1))
				{					
					status = true;
				}
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;	        
		}
		catch(Exception e)
		{
			System.out.println("Exception in isExist ");
			e.printStackTrace();
			throw new ITMException(e); 
		}
		System.out.println("returning String from isExist ");
		return status;
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



}	
